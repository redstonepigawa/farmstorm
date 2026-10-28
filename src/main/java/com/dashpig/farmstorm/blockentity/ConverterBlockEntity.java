package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.recipe.ConverterRecipe;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import com.dashpig.farmstorm.registry.FarmstormRecipes;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 转换器方块实体：双槽容器（输入 + 催化剂）+ 事务化转换（TECH §4.8.2，DEC D-11 / D-12）。
 * 校验顺序即安全性——空槽 → 配方 → 经验 → 催化剂全部通过，才动状态（步骤 5 / 6）。
 * 本类不含任何客户端判定分支：转换入口只接受 {@link ServerPlayer}（HC-03）。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public class ConverterBlockEntity extends BlockEntity implements Container {
    public static final int SLOT_INPUT = 0;
    /** 催化剂槽：只收下界之星（FIX-4 §2.2 对 DESIGN §3.1.2「单槽位」的有意偏离）。 */
    public static final int SLOT_CATALYST = 1;
    public static final int SLOT_COUNT = 2;

    /** ContainerData 下标（TECH §4.8.3）：[0]=xpCost [1]=canConvert [2]=reverse。 */
    public static final int DATA_XP_COST = 0;
    public static final int DATA_CAN_CONVERT = 1;
    public static final int DATA_REVERSE = 2;
    public static final int DATA_COUNT = 3;

    private static final String TAG_INPUT = "Input";
    private static final String TAG_CATALYST = "Catalyst";

    /** 配方代际（RT-07）：服务端每重载一次资源自增，所有实例的缓存随即作废。 */
    private static int recipeGeneration;

    private ItemStack input = ItemStack.EMPTY;
    private ItemStack catalyst = ItemStack.EMPTY;
    /** 命中方向所属的原始配方；byKey 校验用它的 id（1.21.1 的 id 挂在 RecipeHolder 上，不在 Recipe 上）。 */
    @Nullable
    private RecipeHolder<ConverterRecipe> cachedHolder;
    /** 进入匹配的方向视图 + 该方向的成本（TECH §4.8.2 的 cachedRecipe / cachedXpCost 合并成一个不可拆的整体）。 */
    @Nullable
    private Match cachedMatch;
    private int cachedGeneration;

    public ConverterBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.CONVERTER.get(), pos, state);
    }

    // ---- Container 契约 ----

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return this.input.isEmpty() && this.catalyst.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case SLOT_INPUT -> this.input;
            case SLOT_CATALYST -> this.catalyst;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.getItem(slot);
        ItemStack taken = stack.isEmpty() ? ItemStack.EMPTY : stack.split(amount);
        if (!taken.isEmpty()) {
            this.setChanged();
        }
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack current = this.getItem(slot);
        if (slot == SLOT_INPUT) {
            this.input = ItemStack.EMPTY;
        } else if (slot == SLOT_CATALYST) {
            this.catalyst = ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        this.clearCache();
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != SLOT_INPUT && slot != SLOT_CATALYST) {
            return;
        }
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        if (slot == SLOT_INPUT) {
            this.input = stack;
        } else {
            this.catalyst = stack;
        }
        this.clearCache();
        this.setChanged();
    }

    @Override
    public void clearContent() {
        this.input = ItemStack.EMPTY;
        this.catalyst = ItemStack.EMPTY;
        this.clearCache();
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    // ---- 配方匹配与缓存（RT-07） ----

    /**
     * 解析当前输入命中的配方：先正向，未命中再按 {@code result} 比对反向（TECH §4.8.2 步骤 2）。
     * 缓存失效有两条独立通道：资源重载自增 {@link #recipeGeneration}，以及 {@code byKey} 身份校验。
     */
    @Nullable
    private Match resolve(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (this.isCacheValid(level, stack)) {
            return this.cachedMatch;
        }
        this.clearCache();

        RecipeManager recipes = level.getRecipeManager();
        Optional<RecipeHolder<ConverterRecipe>> forward = recipes.getRecipeFor(
                FarmstormRecipes.CONVERTING_TYPE.get(), new SingleRecipeInput(stack), level);
        if (forward.isPresent()) {
            RecipeHolder<ConverterRecipe> holder = forward.get();
            return this.cache(holder, holder.value(), false);
        }

        for (RecipeHolder<ConverterRecipe> holder : recipes.getAllRecipesFor(FarmstormRecipes.CONVERTING_TYPE.get())) {
            ConverterRecipe candidate = holder.value();
            if (!candidate.bidirectional()) {
                continue;                                   // D-12：火把花 / 瓶子草只允许正向
            }
            if (!ItemStack.isSameItemSameComponents(candidate.result(), stack)) {
                continue;
            }
            ConverterRecipe view = candidate.reverse();
            if (!view.matches(new SingleRecipeInput(stack), level)) {
                continue;
            }
            return this.cache(holder, view, true);
        }
        return null;
    }

    private boolean isCacheValid(Level level, ItemStack stack) {
        if (this.cachedMatch == null || this.cachedHolder == null) {
            return false;
        }
        if (this.cachedGeneration != recipeGeneration) {
            return false;
        }
        if (!this.cachedMatch.view().matches(new SingleRecipeInput(stack), level)) {
            return false;
        }
        return level.getRecipeManager().byKey(this.cachedHolder.id()).orElse(null) == this.cachedHolder;
    }

    private Match cache(RecipeHolder<ConverterRecipe> holder, ConverterRecipe view, boolean reverse) {
        Match match = new Match(view, reverse, Math.max(0, view.xpCost()));
        this.cachedHolder = holder;
        this.cachedMatch = match;
        this.cachedGeneration = recipeGeneration;
        return match;
    }

    private void clearCache() {
        this.cachedHolder = null;
        this.cachedMatch = null;
    }

    /**
     * RT-07：服务端 {@code /reload} 或数据包更新后作废全部转换器缓存。
     * 监听 {@link AddReloadListenerEvent}（GAME 总线、仅在服务端触发）；客户端侧的
     * {@code RecipesUpdatedEvent} 只在逻辑客户端触发，无法清空服务端缓存，故不采用。
     * 必须走 {@link net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier#wait}：
     * 重载管线靠它把本监听器移出 preparingListeners，否则整次重载永远不会完成（服务端起不到 Done）。
     */
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener((preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler,
                           backgroundExecutor, gameExecutor) ->
                preparationBarrier.wait(Unit.INSTANCE).thenRun(ConverterBlockEntity::invalidateRecipeCaches));
    }

    private static void invalidateRecipeCaches() {
        recipeGeneration++;
        Farmstorm.LOGGER.info("Converter recipe cache invalidated (generation {})", recipeGeneration);
    }

    // ---- 事务化转换（TECH §4.8.2 的 6 步） ----

    /** 事务化转换：先全量校验（步骤 1–4），全部通过才扣材并产出（步骤 5–6）。 */
    public boolean tryConvert(ServerPlayer player) {
        Plan plan = this.plan(player);
        if (plan == null) {
            return false;
        }
        Level level = player.level();
        // 步骤 5：走到这里表示步骤 1–4 全部通过，开始动状态
        this.input.shrink(1);
        if (this.input.isEmpty()) {
            this.input = ItemStack.EMPTY;
        }
        this.clearCache();
        if (plan.xpCost() > 0) {
            player.giveExperiencePoints(-plan.xpCost());
        }
        int fromSlot = this.takeStoredCatalyst(plan.view().catalyst(), plan.catalystCost());
        reduceCatalyst(player, plan.view().catalyst(), plan.catalystCost() - fromSlot);
        player.getInventory().placeItemBackInInventory(plan.produced());
        FarmstormAdvancements.award(player, "first_conversion");
        if (!plan.reverse() && plan.catalystCost() > 0) {
            FarmstormAdvancements.award(player, "nether_star_toll");
        }
        // 步骤 6：存档 + 刷新 GUI
        this.setChanged();
        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        return true;
    }

    /** 步骤 1–4：任一条件不满足即返回 null，调用方因此拿不到可执行方案（D-12 的「绝不扣等级」）。 */
    @Nullable
    private Plan plan(Player player) {
        if (this.input.isEmpty()) {
            return null;                                    // 步骤 1：空槽
        }
        Level level = player.level();
        Match match = this.resolve(level, this.input);      // 步骤 2：正向 → 反向
        if (match == null) {
            return null;
        }
        int xpCost = match.xpCost();
        if (player.totalExperience < xpCost) {
            return null;                                    // 步骤 3：经验不足 → 拒绝，绝不扣等级
        }
        int catalystCost = requiredCatalyst(match.view(), match.reverse());
        Ingredient catalyst = match.view().catalyst();
        if (catalystCost > 0 && this.storedCatalyst(catalyst) + countCatalyst(player, catalyst) < catalystCost) {
            return null;                                    // 步骤 4：槽位 + 背包合计不够 → 拒绝
        }
        ItemStack produced = match.view().assemble(new SingleRecipeInput(this.input), level.registryAccess());
        return new Plan(match.view(), match.reverse(), xpCost, catalystCost, produced);
    }

    /** 催化剂需求量：正向读 {@code converter.netherStarForwardCost}，反向由 {@code netherStarOnReverse} 决定（D-12）。 */
    private static int requiredCatalyst(ConverterRecipe view, boolean reverse) {
        if (view.catalyst().isEmpty()) {
            return 0;
        }
        if (reverse) {
            return FarmstormConfig.converterNetherStarOnReverse.get() ? Math.max(0, view.catalystCount()) : 0;
        }
        return Math.max(0, FarmstormConfig.converterNetherStarForwardCost.get());
    }

    /** 统计背包 + 副手的催化剂总数；主手物品本身不算、也不消耗（TECH §4.8.2 步骤 4）。 */
    private static int countCatalyst(Player player, Ingredient catalyst) {
        Inventory inventory = player.getInventory();
        int found = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (slot == inventory.selected) {
                continue;
            }
            found += countIn(inventory.items.get(slot), catalyst);
        }
        return found + countIn(inventory.offhand.get(0), catalyst);
    }

    private static int countIn(ItemStack stack, Ingredient catalyst) {
        return !stack.isEmpty() && catalyst.test(stack) ? stack.getCount() : 0;
    }

    /** 催化剂槽里现存的催化剂数量：与背包合计后仍不够才拒绝转换（槽位优先，背包兜底）。 */
    private int storedCatalyst(Ingredient catalyst) {
        return countIn(this.catalyst, catalyst);
    }

    /** 先扣催化剂槽，返回实际扣掉的数量；不足的部分仍由 {@link #reduceCatalyst} 从背包 / 副手补齐。 */
    private int takeStoredCatalyst(Ingredient catalyst, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int taken = takeFrom(this.catalyst, catalyst, amount);
        if (taken <= 0) {
            return 0;
        }
        if (this.catalyst.isEmpty()) {
            this.catalyst = ItemStack.EMPTY;
        }
        this.setChanged();
        return taken;
    }

    private static void reduceCatalyst(Player player, Ingredient catalyst, int amount) {
        int remaining = amount;
        if (remaining <= 0) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE && remaining > 0; slot++) {
            if (slot == inventory.selected) {
                continue;
            }
            remaining -= takeFrom(inventory.items.get(slot), catalyst, remaining);
        }
        if (remaining > 0) {
            takeFrom(inventory.offhand.get(0), catalyst, remaining);
        }
    }

    private static int takeFrom(ItemStack stack, Ingredient catalyst, int limit) {
        if (stack.isEmpty() || !catalyst.test(stack)) {
            return 0;
        }
        int taken = Math.min(limit, stack.getCount());
        stack.shrink(taken);
        return taken;
    }

    // ---- 供 ConverterMenu 读取（服务端权威） ----

    /** 输入槽只接受能匹配配方的物品（TECH §8.3）。 */
    public boolean accepts(ItemStack stack) {
        Level level = this.level;
        return level != null && this.resolve(level, stack) != null;
    }

    /** 预览槽（只读）的内容：服务端现算，客户端由槽位同步拿到结果。 */
    public ItemStack previewResult() {
        Level level = this.level;
        if (level == null) {
            return ItemStack.EMPTY;
        }
        Match match = this.resolve(level, this.input);
        return match == null ? ItemStack.EMPTY : match.view().assemble(new SingleRecipeInput(this.input), level.registryAccess());
    }

    /** GUI 数据（TECH §4.8.3）：[xpCost, canConvert, reverse]，无配方时 xpCost 回落到全局配置值。 */
    public int containerData(int index, Player player) {
        Plan plan = this.plan(player);
        return switch (index) {
            case DATA_XP_COST -> plan == null ? FarmstormConfig.converterXpCost.get() : plan.xpCost();
            case DATA_CAN_CONVERT -> plan == null ? 0 : 1;
            case DATA_REVERSE -> plan != null && plan.reverse() ? 1 : 0;
            default -> 0;
        };
    }

    // ---- 存档 ----

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.input.isEmpty()) {
            tag.put(TAG_INPUT, this.input.save(registries));
        }
        if (!this.catalyst.isEmpty()) {
            tag.put(TAG_CATALYST, this.catalyst.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.input = tag.contains(TAG_INPUT)
                ? ItemStack.parse(registries, tag.get(TAG_INPUT)).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.catalyst = tag.contains(TAG_CATALYST)
                ? ItemStack.parse(registries, tag.get(TAG_CATALYST)).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.clearCache();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (!this.input.isEmpty()) {
            tag.put(TAG_INPUT, this.input.save(registries));
        }
        if (!this.catalyst.isEmpty()) {
            tag.put(TAG_CATALYST, this.catalyst.save(registries));
        }
        return tag;
    }

    /** 一次命中的方向视图与成本。 */
    private record Match(ConverterRecipe view, boolean reverse, int xpCost) {}

    /** 步骤 1–4 全部通过后的执行方案；拿不到方案就等于不可转换。 */
    private record Plan(ConverterRecipe view, boolean reverse, int xpCost, int catalystCost, ItemStack produced) {}
}
