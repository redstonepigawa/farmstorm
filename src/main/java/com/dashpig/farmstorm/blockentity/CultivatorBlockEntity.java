package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.block.CultivatorBlock;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.item.DeployableCropItem;
import com.dashpig.farmstorm.item.ModifiedCropItem;
import com.dashpig.farmstorm.item.SeedItem;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import com.dashpig.farmstorm.network.CultivatorLinkBroadcaster;
import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.CropTier;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 农作物培养器方块实体（D-160 ~ D-164）。F-224 落骨架，F-232 补 {@code Core} 的写入面与落盘，
 * F-235 / F-236 补状态机、四道准入闸与产出。
 *
 * <p><b>刻意不实现 {@code Container}</b>（36 号 §2.5.2）：D-160 没有菜单，
 * 一旦实现 Container，漏斗 / 投掷器就能绕过 {@code useWithoutItem} 塞入史诗作物，D-162 的准入面当场失守。
 *
 * <p>{@code corePos} 是<b>引用缓存</b>：绑定关系的唯一真相在核侧（D-165），使用前要用核侧列表校验。
 */
public class CultivatorBlockEntity extends BlockEntity {
    /** 绑定核坐标的 NBT 键（36 号 §6 ④）：{@code long}（{@link BlockPos#asLong()}）；**缺键 = 未绑定**。 */
    private static final String TAG_CORE = "Core";
    /** 槽内培养物的 NBT 键（36 号 §2.5.2）：{@code ItemStack#save} / {@code ItemStack#parse}。 */
    private static final String TAG_HELD = "Held";
    /** 已累计刻数的 NBT 键（36 号 §2.5.2）：{@code int}。 */
    private static final String TAG_PROGRESS = "Progress";

    /** 槽内培养物；不叠加、不替换（第四道准入闸，F-235）。 */
    private ItemStack held = ItemStack.EMPTY;
    /** 已累计刻数；等于 {@code cultivator.cycleTicks} 时本刻结算一次（F-236）。 */
    private int progress;
    /** 绑定的核心坐标；{@code null} = 未绑定（D-165：引用缓存，真相在核侧）。 */
    @Nullable
    private BlockPos corePos;

    public CultivatorBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.CULTIVATOR.get(), pos, state);
    }

    /** 引用缓存只读视图；{@code null} = 未绑定。 */
    @Nullable
    public BlockPos corePos() {
        return this.corePos;
    }

    /** 槽内培养物（只读；写入面只有 {@link #insert} 与 {@link #releaseHeld} 两处）。 */
    public ItemStack held() {
        return this.held;
    }

    /** 已累计刻数（{@code /data get block} 读的就是它）。 */
    public int progress() {
        return this.progress;
    }

    /**
     * 写入引用缓存（放置绑定 / 迁移改绑的**唯一**入口，D-165 的两个写入时点）。
     * 核侧列表由调用方先写成功再调这里 —— 两侧不一致时按未绑定处理，所以顺序是「先核后培」。
     */
    public void setCore(BlockPos pos) {
        this.corePos = pos;
        this.setChanged();
        this.syncCore();
    }

    /**
     * 清掉引用缓存（F-233 出口②：核被拆且没有接替核 ⇒ 留原地、内容保留、{@code Core} 键消失、不可使用）。
     * 不清 {@code Held} / {@code Progress}（D-168 补充）。
     */
    public void clearCore() {
        if (this.corePos == null) {
            return;
        }
        this.corePos = null;
        this.setChanged();
        this.syncCore();
    }

    /** 绑定变化的同步时点（36 号 §2.6.1 的同一条纪律）：与核侧的 {@code sendBlockUpdated} 成对。 */
    private void syncCore() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
    }

    // ---- F-235 · 四道准入闸（36 号 §2.5.3：合并成一次判定）----

    /**
     * 范围闸 + 绑定闸（36 号 §2.5.3 的 {@code isUsable()}）：
     * 未绑定 / 核不在 / 核侧 24 条列表里没有它 / 不在 3 × 3 区块覆盖内 ⇒ {@code false}。
     *
     * <p>范围复用 {@link OutpostCoreBlockEntity#inRange} 的纯函数形态，不写第二份范围公式。
     * 每刻**不查区块**（36 号 §2.7 末段）：{@code getBlockEntity} 只读已加载的区块，不触发加载。
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

    /**
     * 手持闸（D-162）：**普通与稀有档放得进，史诗档一律拒绝**。
     *
     * <p>准入<b>只读 {@code tier()}</b>，不按 id 白名单 —— 22 条档案的 tier 一改，准入面自动跟着变（RT-49）。
     * 档案由物品类反查（{@code SeedItem} / {@code DeployableCropItem} / {@code ModifiedCropItem} 三者各有
     * {@code profile()}），因此本方法里不出现任何注册 id 的比较。
     */
    public static boolean admissible(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CropProfile profile = profileOf(stack);
        return profile != null && profile.tier() != CropTier.EPIC;
    }

    /** 四道闸合并成**一次**判定：范围 ∧ 绑定 ∧ 手持档 ∧ 空槽。任一不过 = 右键无反应（36 号 §2.5.3）。 */
    public boolean canInsert(ItemStack stack) {
        return this.isUsable() && this.held.isEmpty() && admissible(stack);
    }

    /** 物品 → 父作物档案：三个物品类的 {@code profile()} 返回同一条档案，所以放种子与放作物产出相同。 */
    @Nullable
    private static CropProfile profileOf(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ModifiedCropItem crop) {
            return crop.profile();
        }
        if (item instanceof DeployableCropItem deployed) {
            return deployed.profile();
        }
        if (item instanceof SeedItem seed) {
            return seed.profile();
        }
        return null;
    }

    /** 放入一个培养物（{@code CultivatorBlock#useWithoutItem} 判过四闸之后调，只接受恰好 1 个）。 */
    public void insert(ItemStack one) {
        this.held = one;
        this.progress = 0;
        this.setChanged();
    }

    // ---- F-235 · 服务端每刻状态机（36 号 §2.5.2 的四步）----

    public static void serverTick(Level level, BlockPos pos, BlockState state, CultivatorBlockEntity cultivator) {
        if (level.isClientSide) {
            return;
        }
        // 第 1 步：空槽 ⇒ 不可用，进度归零（槽被清空后才可能重新计时，半途中的产出不存在）。
        if (cultivator.held.isEmpty()) {
            if (cultivator.progress != 0) {
                cultivator.progress = 0;
                cultivator.setChanged();
            }
            cultivator.setWorking(false);
            return;
        }
        // 第 2 步：未绑定 / 核侧不认 / 核不在 ⇒ 不 tick，但**不清槽、不清进度**（D-168 出口②③）。
        if (!cultivator.isUsable()) {
            cultivator.setWorking(false);
            return;
        }
        int cycleTicks = FarmstormConfig.cultivatorCycleTicks.get();
        // 第 3 步：有货且可用 ⇒ 累计一刻。
        if (cultivator.progress < cycleTicks) {
            cultivator.progress++;
            cultivator.setChanged();
            cultivator.setWorking(true);
            return;
        }
        // 第 4 步：progress == cycleTicks ⇒ 本刻结算一次。
        if (cultivator.deliver()) {
            cultivator.held = ItemStack.EMPTY;
            cultivator.progress = 0;
            cultivator.setChanged();
            cultivator.setWorking(false);
        } else {
            // 核满暂停（D-164）：progress **停在 cycleTicks 不清零**，下一 tick 会再走到第 4 步，
            // 核腾出空间的那一 tick 自动完成投递 —— 「从暂停处继续、不重新计时」由此天然成立。
            cultivator.setWorking(false);
        }
    }

    /** 整批投递进绑定核（D-164 / 36 号 §2.6.1：先算后写、按整批判）。失败时物品仍在槽里，不落地、不丢。 */
    private boolean deliver() {
        OutpostCoreBlockEntity core = this.core();
        if (core == null) {
            return false;
        }
        CropProfile profile = profileOf(this.held);
        if (profile == null) {
            // 不可达：准入闸只放带档案的物品进来。真到了这里也不销毁物品 —— 留在槽里，拆方块可原样取回。
            return false;
        }
        return core.acceptAll(batch(profile));
    }

    /** 产出批次（36 号 §2.5.4）：有独立种子 ⇒ {@code [作物 × yield, 种子 × yield]}，否则只 {@code [作物 × yield]}。 */
    private static List<ItemStack> batch(CropProfile profile) {
        int amount = FarmstormConfig.cultivatorYield.get();
        List<ItemStack> batch = new ArrayList<>(2);
        batch.add(new ItemStack(FarmstormItems.forProfile(profile).get(), amount));
        if (hasOwnSeed(profile)) {
            ResourceKey<Item> seed = profile.seedItem();
            batch.add(new ItemStack(BuiltInRegistries.ITEM.get(seed.location()), amount));
        }
        return batch;
    }

    /**
     * 「有独立种子物品」的判据（36 号 §2.5.4）：{@code seedItem() != null} <b>且</b>它不等于 {@code id()}。
     * 22 条档案里 8 条的 {@code seedItem} 指向**自身**，只写 {@code != null} 会让这些档案的产出翻倍
     * （D-163 补充明确否决）。准入面内因此是 5 条给 2 + 2、13 条只给 2。
     */
    private static boolean hasOwnSeed(CropProfile profile) {
        ResourceKey<Item> seed = profile.seedItem();
        return seed != null && !seed.location().equals(profile.id());
    }

    /** {@code working} 只在值真的变化时写一次 blockstate（D-161 / 36 号 §2.5.2），且带 {@code UPDATE_CLIENTS}。 */
    private void setWorking(boolean working) {
        if (!(this.level instanceof ServerLevel server)) {
            return;
        }
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof CultivatorBlock) || state.getValue(CultivatorBlock.WORKING) == working) {
            return;
        }
        server.setBlock(this.getBlockPos(), state.setValue(CultivatorBlock.WORKING, working), Block.UPDATE_CLIENTS);
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

    // ---- F-237 · 破坏掉落的交出面 ----

    /**
     * 把槽里那一个培养物交出去（掉落面的**唯一**入口）。四条件由
     * {@code CultivatorBlock#onRemove} 判，交出后槽与进度归零，避免同一份内容掉两次。
     *
     * <p>返回 {@link NonNullList} 而不是 {@code Container}：本类**不实现** {@code Container}（36 号 §2.5.2），
     * 所以 {@code onRemove} 走 {@code Containers.dropContents(Level, BlockPos, NonNullList)} 那个重载。
     */
    public NonNullList<ItemStack> releaseHeld() {
        NonNullList<ItemStack> contents = NonNullList.withSize(1, ItemStack.EMPTY);
        contents.set(0, this.held);
        this.held = ItemStack.EMPTY;
        this.progress = 0;
        // F-239 / 36 号 §2.8.2 时机 #3（培养器被拆）：本方法是**拆培养器唯一会走到的**服务端钩子
        // （{@code CultivatorBlock#onRemove} 只调它）。核侧的 {@code Cultivators} 条目由
        // {@code OutpostCoreBlockEntity#cultivators()} 的惰性清理摘掉，所以这里请求一次重算，
        // 客户端在去抖窗口内（≤10 刻）就收到短一条的列表 —— 不必等 20 刻重扫（36 号 §8 #32）。
        // 只多一句广播请求：掉落清单、槽与进度的清空逻辑一字未改。
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
        // 未绑定时不写这个键（36 号 §6 ④：缺键 = 未绑定），验收靠「缺 Core 键」读出来。
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
        this.progress = tag.getInt(TAG_PROGRESS);
        this.corePos = tag.contains(TAG_CORE) ? BlockPos.of(tag.getLong(TAG_CORE)) : null;
    }
}
