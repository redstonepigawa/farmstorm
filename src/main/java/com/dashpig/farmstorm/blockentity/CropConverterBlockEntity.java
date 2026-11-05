package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.block.CropConverterBlock;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.network.CultivatorLinkBroadcaster;
import com.dashpig.farmstorm.recipe.ConverterRecipe;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import com.dashpig.farmstorm.registry.FarmstormRecipes;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 农作物转换器方块实体（D-208 / 54 号 §4.1.2）：单槽 + 进度 + 绑定核，服务端每刻走四步状态机。
 *
 * <p>状态机与 {@link CultivatorBlockEntity#serverTick} 逐行同形，只换产出函数与阈值：投递的是
 * 转换配方的那**一件** {@code result}，契约仍走核侧的 List 整批接口（那条接口已保证「先算后写、
 * 失败一个都不写」，自己再写一份单件判定就是第二份副本）。
 *
 * <p><b>刻意不实现 {@code Container}</b>（D-208 补充）：一旦实现，漏斗 / 投掷器就能绕过
 * {@code useWithoutItem} 塞东西进来，四道准入闸当场失守。也不接红石 / 比较器信号：原版那两个
 * 模拟输出方法一个都不覆写。
 *
 * <p>{@code corePos} 是<b>引用缓存</b>：绑定关系的唯一真相在核侧（D-165），使用前要用核侧列表校验。
 */
public class CropConverterBlockEntity extends BlockEntity {
    /**
     * 转换周期（刻）：读 {@code crop_converter.processTicks}（默认 600 刻 = 30 秒，D-208 / D-223）。
     *
     * <p>每次现读，不缓存到静态字段：改配置后下一次 tick 就按新阈值算。
     */
    private static int processTicks() {
        return FarmstormConfig.cropConverterProcessTicks.get();
    }

    /** 槽内待转换物品的 NBT 键（54 号 §4.1.2）：{@code ItemStack#save} / {@code ItemStack#parse}。 */
    private static final String TAG_HELD = "Held";
    /** 已累计刻数的 NBT 键：{@code int}。 */
    private static final String TAG_PROGRESS = "Progress";
    /** 绑定核坐标的 NBT 键：{@code long}（{@link BlockPos#asLong()}）；**缺键 = 未绑定**。 */
    private static final String TAG_CORE = "Core";

    /** 槽内那一个待转换物品；不叠加、不替换（空槽闸）。 */
    private ItemStack held = ItemStack.EMPTY;
    /** 已累计刻数；到达 {@link #processTicks()} 时本刻结算一次（{@code /data get block} 读的就是它）。 */
    private int progress;
    /** 绑定的核心坐标；{@code null} = 未绑定（引用缓存，真相在核侧）。 */
    @Nullable
    private BlockPos corePos;

    public CropConverterBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.CROP_CONVERTER.get(), pos, state);
    }

    /** 引用缓存只读视图；{@code null} = 未绑定。 */
    @Nullable
    public BlockPos corePos() {
        return this.corePos;
    }

    /** 槽内那一个待转换物品（只读；写入面只有 {@link #insert} 与 {@link #releaseHeld} 两处）。 */
    public ItemStack held() {
        return this.held;
    }

    /** 已累计刻数。 */
    public int progress() {
        return this.progress;
    }

    /**
     * 写入引用缓存（放置绑定的唯一入口，由 {@code CropConverterBlock#setPlacedBy} 在核侧写成功后调用）：
     * 两侧不一致时按未绑定处理，所以顺序是「先核后机器」。
     */
    public void setCore(BlockPos pos) {
        this.corePos = pos;
        this.setChanged();
    }

    // ---- 四道准入闸：范围 / 绑定 / 手持 / 空槽，合并成一次判定（54 号 §4.1.3）----

    /**
     * 范围闸 + 绑定闸：未绑定 / 核不在 / 核侧列表里没有它 / 不在 3 × 3 区块覆盖内 ⇒ {@code false}。
     *
     * <p>范围复用 {@link OutpostCoreBlockEntity#inRange} 的纯函数形态，不写第二份范围公式；
     * 每刻不查区块（{@code getBlockEntity} 只读已加载的区块，不触发加载）。
     */
    public boolean isUsable() {
        if (this.corePos == null) {
            return false;
        }
        OutpostCoreBlockEntity core = this.core();
        if (core == null) {
            return false;
        }
        // 绑定闸：核侧列表是唯一真相（D-165），引用缓存命中不算数。
        if (!core.hasCultivator(this.getBlockPos())) {
            return false;
        }
        return OutpostCoreBlockEntity.inRange(this.corePos, this.getBlockPos());
    }

    /** 四道闸合并成**一次**判定：范围 ∧ 绑定 ∧ 手持 ∧ 空槽。任一不过 = 右键无反应（{@code PASS}）。 */
    public boolean canInsert(ItemStack stack) {
        return this.isUsable() && this.held.isEmpty() && convertible(stack);
    }

    /**
     * 手持闸（54 号 §4.1.3 / HC-172）：手上那件物品是**某一条 {@code farmstorm:converting} 配方的输入**即通过。
     *
     * <p>判据只有一个来源——配方表的只读视图，因此收**全 21 条**（含火把花 / 瓶子草荚 / 甜浆果 / 金苹果
     * 四条史诗输入），不写物品白名单，也不抄培养器那套「输出档案非史诗」的 17 条判据（RT-73）：
     * 数据包新增一条 {@code converting} 配方，转换器自动多收一个输入物。
     */
    private boolean convertible(ItemStack stack) {
        Level level = this.level;
        if (level == null || stack.isEmpty()) {
            return false;
        }
        return level.getRecipeManager()
                .getRecipeFor(FarmstormRecipes.CONVERTING_TYPE.get(), new SingleRecipeInput(stack), level)
                .isPresent();
    }

    /** 放入一个待转换物品（{@code CropConverterBlock#useWithoutItem} 判过四闸之后调，只接受恰好 1 个）。 */
    public void insert(ItemStack one) {
        this.held = one;
        this.progress = 0;
        this.setChanged();
    }

    // ---- 服务端每刻四步状态机（54 号 §4.1.2）----

    public static void serverTick(Level level, BlockPos pos, BlockState state, CropConverterBlockEntity converter) {
        if (level.isClientSide) {
            return;
        }
        // 第 1 步：空槽 ⇒ 进度归零，不工作（半途中的产出不存在）。
        if (converter.held.isEmpty()) {
            if (converter.progress != 0) {
                converter.progress = 0;
                converter.setChanged();
            }
            converter.setWorking(false);
            return;
        }
        // 第 2 步：绑定失效（核被拆 / 核侧不认 / 超出 3 × 3 区块）⇒ 不 tick，但**不清槽、不清进度**。
        if (!converter.isUsable()) {
            converter.setWorking(false);
            return;
        }
        // 第 3 步：有货且可用 ⇒ 累计一刻。
        if (converter.progress < processTicks()) {
            converter.progress++;
            converter.setChanged();
            converter.setWorking(true);
            return;
        }
        // 第 4 步：进度到达阈值 ⇒ 本刻结算一次（进度只由本类的累计与 {@link #loadAdditional} 的夹取写入，
        // 因此「小于阈值」与「等于阈值」两支已穷尽，不存在第三态）。
        if (converter.progress == processTicks()) {
            if (converter.deliver()) {
                converter.held = ItemStack.EMPTY;
                converter.progress = 0;
                converter.setChanged();
                converter.setWorking(false);
            } else {
                // 核满暂停（D-208 第 2 条）：进度**停在阈值不清零**，槽里那件仍在，下一 tick 会再走到第 4 步，
                // 核腾出空间的那一 tick 自动完成投递 —— 「从暂停处继续、不重新计时」由此天然成立。
                converter.setWorking(false);
            }
        }
    }

    /**
     * 投递一件产物进绑定核（54 号 §4.1.2 / §4.1.6）。失败时物品仍在槽里，不落地、不丢。
     *
     * <p>成功支同时是 {@code first_conversion} 的授予点（HC-170）：新机器没有「操作者」这个角色，
     * 唯一稳定的主体是绑定核的归属者，离线返回 {@code null} 时跳过（重复授予由原版忽略）。
     */
    private boolean deliver() {
        OutpostCoreBlockEntity core = this.core();
        Level level = this.level;
        if (core == null || level == null) {
            return false;
        }
        Optional<RecipeHolder<ConverterRecipe>> match = level.getRecipeManager()
                .getRecipeFor(FarmstormRecipes.CONVERTING_TYPE.get(), new SingleRecipeInput(this.held), level);
        if (match.isEmpty()) {
            // 不可达：准入闸只放命中配方的物品进来。真到了这里也不销毁物品 —— 留在槽里，拆方块可原样取回。
            return false;
        }
        ItemStack product = match.get().value().getResultItem(level.registryAccess()).copy();
        if (!core.acceptAll(List.of(product))) {
            return false;
        }
        ServerPlayer owner = core.owner();
        if (owner != null) {
            FarmstormAdvancements.award(owner, "first_conversion");
        }
        return true;
    }

    /** {@code working} 只在值真的变化时写一次 blockstate（D-211），且带 {@code UPDATE_CLIENTS}。 */
    private void setWorking(boolean working) {
        if (!(this.level instanceof ServerLevel server)) {
            return;
        }
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof CropConverterBlock) || state.getValue(CropConverterBlock.WORKING) == working) {
            return;
        }
        server.setBlock(this.getBlockPos(), state.setValue(CropConverterBlock.WORKING, working), Block.UPDATE_CLIENTS);
    }

    /** 绑定核的方块实体；区块未加载时返回 {@code null}（不触发加载）。 */
    @Nullable
    private OutpostCoreBlockEntity core() {
        BlockPos pos = this.corePos;
        if (pos == null || !(this.level instanceof ServerLevel server)) {
            return null;
        }
        return server.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core ? core : null;
    }

    // ---- 破坏掉落的交出面（54 号 §4.1.4 第一种边界）----

    /**
     * 把槽里那**一件**原样交出去（掉落面的唯一入口）。四条件由 {@code CropConverterBlock#onRemove} 判，
     * 交出后槽与进度归零，避免同一份内容掉两次；方块本体由战利品表掉，两处不重叠。
     *
     * <p>返回 {@link NonNullList} 而不是 {@code Container}：本类不实现 {@code Container}，
     * 所以 {@code onRemove} 走 {@code Containers.dropContents(Level, BlockPos, NonNullList)} 那个重载。
     */
    public NonNullList<ItemStack> releaseHeld() {
        NonNullList<ItemStack> contents = NonNullList.withSize(1, ItemStack.EMPTY);
        contents.set(0, this.held);
        this.held = ItemStack.EMPTY;
        this.progress = 0;
        // 拆机器是绑定的解除时点之一（核侧列表由 OutpostCoreBlockEntity#cultivators() 惰性清理），
        // 这里请求一次连线重算，让观察面在去抖窗口内跟上；服务端逻辑不依赖它是否发得出去。
        if (this.level != null && !this.level.isClientSide) {
            CultivatorLinkBroadcaster.requestAll(this.level.getServer());
        }
        return contents;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.held.isEmpty()) {
            tag.put(TAG_HELD, this.held.save(registries));
        }
        tag.putInt(TAG_PROGRESS, this.progress);
        // 未绑定时不写这个键（缺键 = 未绑定），验收靠「缺 Core 键」读出来。
        if (this.corePos != null) {
            tag.putLong(TAG_CORE, this.corePos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.held = tag.contains(TAG_HELD)
                ? ItemStack.parse(registries, tag.get(TAG_HELD)).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        // 进度越过阈值时夹回阈值：状态机的第 3 / 4 步因此穷尽所有取值，不会出现「大于阈值」的第三态。
        this.progress = Math.min(tag.getInt(TAG_PROGRESS), processTicks());
        this.corePos = tag.contains(TAG_CORE) ? BlockPos.of(tag.getLong(TAG_CORE)) : null;
    }
}
