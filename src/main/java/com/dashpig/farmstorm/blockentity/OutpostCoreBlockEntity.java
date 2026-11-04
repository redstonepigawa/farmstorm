package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.CultivatorBlock;
import com.dashpig.farmstorm.block.OutpostCoreBlock;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.network.CoreListBroadcaster;
import com.dashpig.farmstorm.network.CultivatorLinkBroadcaster;
import com.dashpig.farmstorm.outpost.CoreChunkTickets;
import com.dashpig.farmstorm.outpost.OutpostCoreIndex;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import com.dashpig.farmstorm.team.TeamResolver;
import com.dashpig.farmstorm.util.AreaBlockEntities;
import com.dashpig.farmstorm.util.DeployedOwnership;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector3f;

/**
 * 据点核心方块实体（D-119 / D-120 / D-121 / D-122）。
 *
 * <p>五件事：三个归属字段（{@code ownerUuid} / {@code ownerName} / {@code placedGameTime}）、归属解析、
 * 3 × 3 区块范围查询，以及两条服务端链路——边界粒子（F-155，挂在 {@link #serverTick}）与拆核清算
 * （F-159，挂在 {@link #setRemoved}）。
 *
 * <p>归属只存 UUID 与名字、**不留队伍快照**（C-9）：换队后己方判定立刻变化，因为
 * {@link DeployedOwnership#isAlly} 在判定的那一刻才去解析归属者的当前队伍。
 */
public class OutpostCoreBlockEntity extends BlockEntity implements Container {
    /**
     * 54 格 = 6 行 × 9 列 = 双箱 27 × 2（D-166）。
     * <b>54 是格子数、16 是培养器名额</b>（裁定 #8）：后者在 F-231 才建，本类的 {@code cultivators} 留到那时。
     */
    public static final int SLOT_COUNT = 54;

    /** NBT 键名（与部署方块同形，不加前缀）：{@code ownerUuid} / {@code ownerName} 走 {@link DeployedOwnership}。 */
    private static final String TAG_PLACED_GAME_TIME = "placedGameTime";

    /**
     * 核侧绑定列表的 NBT 键（F-231 / 36 号 §2.6.1）：{@code ListTag &lt; LongTag &gt;}，每条是
     * {@link BlockPos#asLong()}。**首字母大写**，与 {@code Items} 同一体例。
     */
    private static final String TAG_CULTIVATORS = "Cultivators";

    /** 3 × 3 区块 = 所在区块 + 周围 8 区块（D-120 第 2 项，与 D-118、D-121 同一组数字）。 */
    private static final int RANGE_CHUNKS = 1;

    /** 边界粒子的几何（23 号 §8.7 / D-121 第 5 条）：48 × 48 格 = 16 × 3，与 {@link #inRange} 同一组数字。 */
    private static final int RING_BLOCKS = 16 * (RANGE_CHUNKS * 2 + 1);
    /** 每 4 格一点（23 号 §8.7）：周长 192 格 ÷ 4 = 单圈 48 点。 */
    private static final int RING_STEP = 4;
    private static final int RING_POINTS_PER_EDGE = RING_BLOCKS / RING_STEP;
    private static final int RING_POINTS = RING_POINTS_PER_EDGE * 4;

    /** 23 号 §8.7 的唯一取值：{@code minecraft:dust}、青蓝 (0.30, 0.85, 0.90)、scale 1.0。 */
    private static final DustParticleOptions RING_DUST = new DustParticleOptions(new Vector3f(0.30F, 0.85F, 0.90F), 1.0F);

    /** 贴地而不画在核心高度：核心方块 y + 0.1（23 号 §8.7 的高度行）。 */
    private static final double RING_Y_OFFSET = 0.1;

    /** tick 证据的日志节流：每 30 轮一条（interval = 40 刻 ⇒ 约 60 秒一条）。 */
    private static final int TICK_LOG_ROUNDS = 30;

    /**
     * 补绑周期（F-249 / D-176）：100 刻一轮。**编译期常量、不是 config 键**（D-185：本版 config 只 +1），
     * 因此不存在「配成 0」的可能，补绑的相位门也就不需要边界粒子门那条非零守卫。
     */
    private static final int REBIND_INTERVAL_TICKS = 100;

    /** 两档衰减（23 号 §8.7）：矩形内的玩家每轮满点；矩形外扩 16 格的余量带每 4 轮 1 次；再远不发。 */
    private static final double RING_MARGIN = 16.0;
    private static final int RING_MARGIN_DECIMATION = 4;

    /**
     * 每轮用玩家 UUID 去重（25 号 F-155 验收 3 / 23 号 §8.8）：两个核心范围相邻时交界处只留**一圈**，
     * 同一个玩家在一个 {@code interval} 窗口里最多收到一轮。只在服务端主线程读写，因此用普通
     * {@link HashMap}（与 {@code WildHunterAura} 的 {@code LOOP_LAST_SENT} 同形）。
     */
    private static final Map<UUID, Long> LAST_RING_TICK = new HashMap<>();

    /** 54 格存储（D-166）：{@code NonNullList} 固定长度，缺 {@code Items} 键时保持 54 个空槽。 */
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    /**
     * 核侧 16 条绑定列表（F-231 / D-165）：<b>唯一真相</b>在这里，容量读
     * {@code outpost_core.cultivatorCapacity}（默认 16）。培养器侧 NBT 里的 {@code Core} 只是引用缓存。
     *
     * <p><b>16 是名额、54 是格子数</b>（裁定 #8）：两者不同义，不与 {@link #items} 混用。
     */
    private final List<BlockPos> cultivators = new ArrayList<>();

    @Nullable
    private UUID ownerUuid;
    @Nullable
    private String ownerName;
    private long placedGameTime;

    /**
     * F-159 的一次性闸：{@code setRemoved} 在区块卸载与停服时也会调（见 {@link #settleOnRemoval()}），
     * 同一个实例上重复到达时只清算一次。
     */
    private boolean cleared;

    public OutpostCoreBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.OUTPOST_CORE.get(), pos, state);
    }

    // ---- 整批插入（D-164 补充 / 36 号 §2.6.1 的三条纪律：先算后写、按整批判、成功后 setChanged + 同 tick 刷新）----

    /** 只读模拟：这一批能不能整批放下。不写 {@link #items}、不 {@code setChanged}。 */
    public boolean canAccept(List<ItemStack> batch) {
        return this.simulateAccept(batch) != null;
    }

    /**
     * 整批真插入：模拟不通过就一个都不写（否则会出现半批落地），通过才整体替换并刷新。
     *
     * <p>刷新用 {@code sendBlockUpdated} 而不是把物品塞进 {@link #getUpdateTag}：绑定面的变化要同步，
     * 54 格物品由原版菜单槽位逐格下发（36 号 §2.6.1）。
     */
    public boolean acceptAll(List<ItemStack> batch) {
        List<ItemStack> result = this.simulateAccept(batch);
        if (result == null) {
            return false;
        }
        for (int i = 0; i < result.size(); i++) {
            this.items.set(i, result.get(i));
        }
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
        return true;
    }

    /**
     * 在副本上模拟「先合并到同物品未满的槽、再占空槽」；任何一件放不完就返回 {@code null}（整批失败）。
     * 只在服务端调用（{@code CultivatorBlockEntity} 的结算，F-236）。
     */
    @Nullable
    private List<ItemStack> simulateAccept(List<ItemStack> batch) {
        List<ItemStack> simulated = new ArrayList<>(SLOT_COUNT);
        for (ItemStack stack : this.items) {
            simulated.add(stack.copy());
        }
        for (ItemStack incoming : batch) {
            if (incoming.isEmpty()) {
                continue;
            }
            int max = Math.min(this.getMaxStackSize(incoming), incoming.getMaxStackSize());
            int remaining = incoming.getCount();
            for (int i = 0; i < simulated.size() && remaining > 0; i++) {
                ItemStack slot = simulated.get(i);
                if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, incoming) && slot.getCount() < max) {
                    int moved = Math.min(max - slot.getCount(), remaining);
                    slot.grow(moved);
                    remaining -= moved;
                }
            }
            for (int i = 0; i < simulated.size() && remaining > 0; i++) {
                if (simulated.get(i).isEmpty()) {
                    ItemStack placed = incoming.copyWithCount(Math.min(max, remaining));
                    simulated.set(i, placed);
                    remaining -= placed.getCount();
                }
            }
            if (remaining > 0) {
                return null;
            }
        }
        return simulated;
    }

    // ---- Container 契约（D-166 / 36 号 §2.6.1 的逐方法形态，照 ConverterBlockEntity）----

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = ContainerHelper.removeItem(this.items, slot, amount);
        if (!taken.isEmpty()) {
            this.setChanged();
        }
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.items.set(slot, stack);
        this.setChanged();
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    /** 原版语义的触及范围（{@code Container.stillValidBlockEntity}，默认距离 4.0）；裁定 #17 的 B 闸。 */
    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    // ---- 选核（F-232 放置绑定 / F-233 迁移改绑共用同一份实现）----

    /**
     * 选核：<b>先过滤（覆盖 ∧ 己方 ∧ 未满）再取最近</b>，序为 {@code distSqr} → {@code placedGameTime} →
     * 核坐标字典序（D-165 的四判据 + 两级平手键）。
     *
     * <p><b>顺序不能反</b>：先取最近再判空位会把「最近的满了、别处还有空位」误判成「没有可绑的核」，
     * 与「一核 16 个」的容量语义自相矛盾（35 号 §8.2 算例 1 / 算例 3）。
     *
     * <p><b>不为找核加载区块</b>：候选来自 {@link OutpostCoreIndex} 的只读视图；要读容量就得拿到方块实体，
     * 因此先用 {@code ServerChunkCache#getChunkNow} 确认区块在内存，再从**这个 {@link LevelChunk}** 上取
     * 方块实体 —— {@code Level#getBlockEntity} 会走 {@code getChunkAt}、可能触发加载，这里不碰它。
     * 区块不在内存 ⇒ 该候选本轮跳过（它的容量读不到，绑上去就是猜）。
     *
     * <p>「己方」只有一条链路：{@code TeamResolver} 的 Owner × Owner 重载（红线第 4 条 / D-138）。
     * 放置时 {@code self} 是放下的玩家，迁移时是被拆核的归属者 —— 两种场景同一个判定，不写第二份队伍比较。
     *
     * @param self    己方判定的参照者
     * @param exclude 要排除的核坐标（迁移时是正在被拆的这个核；放置时传 {@code null}）
     */
    @Nullable
    public static OutpostCoreBlockEntity selectCore(ServerLevel level, BlockPos pos, TeamResolver.Owner self,
                                                    @Nullable BlockPos exclude) {
        OutpostCoreIndex index = OutpostCoreIndex.of(level);
        MinecraftServer server = level.getServer();
        ResourceLocation dimension = level.dimension().location();
        List<OutpostCoreBlockEntity> candidates = new ArrayList<>();
        for (UUID owner : index.owners()) {
            if (!Farmstorm.TEAMS.isAlly(level, new TeamResolver.Owner(owner, index.ownerName(owner)), self)) {
                continue;                             // 判据 2：己方
            }
            for (OutpostCoreIndex.Entry entry : index.entriesOf(server, owner)) {
                if (!entry.dimension().equals(dimension)) {
                    continue;                         // 跨维度不成立（候选集只含同维度的核）
                }
                if (entry.pos().equals(exclude)) {
                    continue;                         // 正在被拆 / 已被排除的核
                }
                if (!inRange(entry.pos(), pos)) {
                    continue;                         // 判据 1：覆盖（3 × 3 区块，RANGE_CHUNKS = 1）
                }
                LevelChunk chunk = level.getChunkSource()
                        .getChunkNow(entry.pos().getX() >> 4, entry.pos().getZ() >> 4);
                if (chunk == null) {
                    continue;                         // 不加载区块
                }
                if (!(chunk.getBlockEntity(entry.pos()) instanceof OutpostCoreBlockEntity core)) {
                    continue;                         // 索引里的条目已过期（方块没了）
                }
                if (core.isCultivatorListFull()) {
                    continue;                         // 判据 3：未满
                }
                candidates.add(core);
            }
        }
        candidates.sort(Comparator
                .comparingDouble((OutpostCoreBlockEntity core) -> core.getBlockPos().distSqr(pos))
                .thenComparingLong(OutpostCoreBlockEntity::placedGameTime)
                .thenComparingInt(core -> core.getBlockPos().getX())
                .thenComparingInt(core -> core.getBlockPos().getY())
                .thenComparingInt(core -> core.getBlockPos().getZ()));
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    // ---- 核侧 16 条绑定列表（F-231 / D-165 / 裁定 #8）：唯一真相，培养器侧的 Core 只是引用缓存 ----

    /**
     * 绑定列表（**返回副本**，调用方可以边遍历边改自己的集合）。
     *
     * <p>只在这条低频路径上做一次惰性清理（36 号 §2.7 末段）：把「该格已不是培养器」的条目摘掉。
     * 区块用 {@code ServerChunkCache#getChunkNow} 取（只查已加载、不触发加载）；**取不到就保留该条**
     * ——读不到不等于方块没了。运行期每刻的可用性判定不做这个校验（每刻 × 16 条 × 核数，代价不成比例）。
     */
    public List<BlockPos> cultivators() {
        this.pruneCultivators();
        return List.copyOf(this.cultivators);
    }

    /** 已绑条数（不触发惰性清理：给候选过滤用，选核时每刻要问很多次）。 */
    public int cultivatorCount() {
        return this.cultivators.size();
    }

    public boolean hasCultivator(BlockPos pos) {
        return this.cultivators.contains(pos);
    }

    /** 满员判定：容量读 {@code outpost_core.cultivatorCapacity}（默认 16）。<b>不是 54</b>（裁定 #8）。 */
    public boolean isCultivatorListFull() {
        return this.cultivators.size() >= FarmstormConfig.outpostCoreCultivatorCapacity.get();
    }

    /**
     * 加一条绑定（放置绑定 / 迁移改绑的唯一入口）：已绑或满员返回 {@code false}，**不挤掉已绑的**
     * （35 号 §8.2 算例 2）。成功时 {@code setChanged()} + 同 tick 刷新，与 {@link #acceptAll} 同一纪律。
     */
    public boolean addCultivator(BlockPos pos) {
        if (this.hasCultivator(pos) || this.isCultivatorListFull()) {
            return false;
        }
        this.cultivators.add(pos);
        this.setChanged();
        this.syncBindings();
        // F-239 / 36 号 §2.8.2 时机 #3（绑定变化）：连线是**观察面**，这里只多一句「重算一次」，
        // 本方法的返回值与落盘内容不受影响（服务端任何逻辑都不依赖连线是否发得出去）。
        if (this.level instanceof ServerLevel server) {
            CultivatorLinkBroadcaster.requestAll(server.getServer());
        }
        return true;
    }

    public boolean removeCultivator(BlockPos pos) {
        if (!this.cultivators.remove(pos)) {
            return false;
        }
        this.setChanged();
        this.syncBindings();
        // F-239 时机 #3（解绑）：同 addCultivator，只多一次广播请求。
        if (this.level instanceof ServerLevel server) {
            CultivatorLinkBroadcaster.requestAll(server.getServer());
        }
        return true;
    }

    /** 惰性清理（只在 {@link #cultivators()} 里调）：区块取不到 ⇒ 保留，读不到不等于方块没了。 */
    private void pruneCultivators() {
        if (this.cultivators.isEmpty() || !(this.level instanceof ServerLevel server)) {
            return;
        }
        boolean removed = this.cultivators.removeIf(pos -> {
            LevelChunk chunk = server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk != null && !(chunk.getBlockState(pos).getBlock() instanceof CultivatorBlock);
        });
        if (removed) {
            this.setChanged();
        }
    }

    /**
     * 绑定变化的同步时点（36 号 §2.6.1）：放置 / 改绑 / 增删各调一次。
     * {@link #getUpdateTag} 不写 {@code Cultivators}（连线由 F-239 的包下发），这里只负责让客户端知道
     * 「这一格变了」，所以走 {@code sendBlockUpdated} 而不是把列表塞进同步面。
     */
    private void syncBindings() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
    }

    /**
     * 归属只在放置瞬间写入，且只写 UUID 与名字（C-9）。同时登记「玩家 → 坐标」索引（F-153），
     * 放置上限、额度求和与范围判定三处共用这一份数据。
     */
    public void setOwner(Player placer) {
        this.ownerUuid = placer.getUUID();
        this.ownerName = placer.getGameProfile().getName();
        this.placedGameTime = placer.level().getGameTime();
        if (this.level instanceof ServerLevel server) {
            // F-189 / A-39：同一个变量、同一个方法、同一个 tick ⇒ 方块实体与索引里的比较键不可能漂移。
            OutpostCoreIndex.of(server).record(server, placer, this.getBlockPos(), this.placedGameTime);
            // F-198：核所在的那 1 个区块上一张自有票，区块卸载后方块实体照样 tick（A-43 的 tick 面）。
            CoreChunkTickets.request(server, this.getBlockPos());
            // F-200 / 30 号 §2.1.4 第 4 条（放置核）：收件人集合由每人自己的组装结果决定，所以统一请求。
            CoreListBroadcaster.requestAll(server.getServer());
        }
        this.setChanged();
    }

    /**
     * 归属者在线时解析为实体；离线返回 {@code null}（队伍判定不依赖它，见 {@link DeployedOwnership}）。
     */
    @Nullable
    public ServerPlayer owner() {
        MinecraftServer server = this.level == null ? null : this.level.getServer();
        if (this.ownerUuid == null || server == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(this.ownerUuid);
    }

    /**
     * 己方判定：核心放置者本人或其队友（D-120 第 5 项）。
     * 客户端不做归属判定（HC-03）：多人场景下客户端看不到完整队伍信息，统一按「己方」显示，
     * 真实与否决一律以服务端为准（与 {@code DeployedCropBlockEntity#isAlly} 同一取舍）。
     */
    public boolean isAlly(Player player) {
        if (!(this.level instanceof ServerLevel server)) {
            return true;
        }
        return DeployedOwnership.isAlly(server, this.ownerUuid, this.ownerName, player);
    }

    /**
     * 3 × 3 区块范围（24 号 §6.2 的三行伪码）：
     * 核心区块 {@code (cx, cz)} 与目标区块 {@code (tx, tz)} 的切比雪夫距离 ≤ 1。
     * 48 × 48 格 = 16 × 3，与边界粒子、耕地拦截共用同一组数字。
     */
    public boolean inRange(BlockPos pos) {
        return inRange(this.getBlockPos(), pos);
    }

    /** 纯函数的形态：{@code OutpostCoreIndex} 与范围判定都不依赖方块实体是否在内存里（23 号 §11.2）。 */
    public static boolean inRange(BlockPos corePos, BlockPos pos) {
        int coreChunkX = corePos.getX() >> 4;
        int coreChunkZ = corePos.getZ() >> 4;
        int targetChunkX = pos.getX() >> 4;
        int targetChunkZ = pos.getZ() >> 4;
        return Math.max(Math.abs(targetChunkX - coreChunkX), Math.abs(targetChunkZ - coreChunkZ)) <= RANGE_CHUNKS;
    }

    @Nullable
    public UUID ownerUuid() {
        return this.ownerUuid;
    }

    @Nullable
    public String ownerName() {
        return this.ownerName;
    }

    public long placedGameTime() {
        return this.placedGameTime;
    }

    /**
     * 归属释放（{@code OutpostCoreBlock#onRemove} 调用）：从索引里摘掉这一条，名额**当场**释放
     * （D-122 补充：核心被破坏或被己方挖走时立即释放，不做延迟释放）。
     * F-159 的「范围内己方耕地转泥土」也挂这条链路上。
     */
    public void release() {
        if (this.level instanceof ServerLevel server && this.ownerUuid != null) {
            OutpostCoreIndex.of(server).remove(server, this.ownerUuid, this.getBlockPos());
            // F-198：票在这里释放（onRemove 与 settleOnRemoval 末尾共用这一处，幂等）。
            CoreChunkTickets.release(server, this.getBlockPos());
            // F-200 / 30 号 §2.1.4 第 5、6 条（摧毁核 / 迁移完成）：迁移在 settleOnRemoval 里已改完绑定，
            // 且它末尾一定会走到本方法，所以一次请求覆盖两条时机；10 刻去抖把重复请求合并成一次发送。
            CoreListBroadcaster.requestAll(server.getServer());
            // F-239 / 36 号 §2.8.2 时机 #4（核被拆）：同一个请求点。索引条目已摘，
            // 所以组装结果自然不再含这个核 —— 客户端收到的新列表就是「线消失了」的权威答案。
            CultivatorLinkBroadcaster.requestAll(server.getServer());
        }
    }

    /**
     * 补绑的相位门（F-249 / D-176 / 42 号 §2.1.1）：<b>与边界粒子门各自独立</b>。
     *
     * <p>模数是编译期常量 {@link #REBIND_INTERVAL_TICKS}（100 刻），不读 config —— 正因为常量取不到 0，
     * 这里**不设**边界粒子门那条「非零才继续」的守卫：两套门的守卫不能互借。
     * {@code floorMod(pos.hashCode(), 100)} 落在 0 ~ 99，同一片区域里的多个核各占一个 tick，不会同刻一起扫。
     */
    private static boolean rebindDue(BlockPos pos, long now) {
        return Math.floorMod(now + Math.floorMod(pos.hashCode(), REBIND_INTERVAL_TICKS),
                REBIND_INTERVAL_TICKS) == 0;
    }

    /**
     * 周期补绑（F-249 / D-177 / 42 号 §2.1.3）：把 3 × 3 已加载区块里「没人要」的培养器补进自己的名额。
     *
     * <p>五条写入边界，缺一条就会造出第二个主、或白跑一遍枚举：
     * <ol>
     *   <li>无主核不补（裁定 #13）：{@code /setblock} 出来的核没有归属者，判据放在方法第一行；
     *   <li>容量只读 {@link #cultivatorCount()}：返回副本的那条读取路径会先做惰性清理并可能
     *       {@code setChanged}，与补绑交错会让「本轮已绑」重复或漏计；
     *   <li>满则停：整轮入口与绑定循环里各判一次，每绑一个重读，不白跑剩下的候选；
     *   <li>只加不摘：两侧不一致的候选一律**跳过**，不调核侧的解绑方法做兜底（那会造出第二个主）；
     *   <li>先写核侧 {@link #addCultivator}，成功后才写培养器的 {@code Core} 缓存。
     * </ol>
     *
     * <p>候选先排序再绑（裁定 #14）：距离升序 → x → y → z。区块映射的迭代序不稳定，不排序的话同一存档
     * 两次运行可能绑到不同的一批；培养器没有「放置时刻」字段，坐标三元组已足以定序。
     *
     * <p>不额外调广播：{@link #addCultivator} 里已经 {@code setChanged} + 同步 + 一次请求。
     */
    private void rebindNearby(ServerLevel server) {
        if (this.ownerUuid == null) {
            return;                                       // 裁定 #13：/setblock 出来的核没有归属者
        }
        int cap = FarmstormConfig.outpostCoreCultivatorCapacity.get();
        if (cultivatorCount() >= cap) {
            return;                                       // 满员：这一轮连枚举都不做
        }
        int added = 0;
        List<CultivatorBlockEntity> candidates =
                AreaBlockEntities.collect(server, this.getBlockPos(), RANGE_CHUNKS, CultivatorBlockEntity.class);
        candidates.sort(Comparator
                .comparingDouble((CultivatorBlockEntity candidate) -> candidate.getBlockPos().distSqr(this.getBlockPos()))
                .thenComparingInt(candidate -> candidate.getBlockPos().getX())
                .thenComparingInt(candidate -> candidate.getBlockPos().getY())
                .thenComparingInt(candidate -> candidate.getBlockPos().getZ()));
        for (CultivatorBlockEntity candidate : candidates) {
            if (cultivatorCount() >= cap) {
                break;                                    // 每绑一个重读：满即停
            }
            if (candidate.corePos() != null) {
                continue;                                 // 已绑（含指向别的核）⇒ 跳过，不夺
            }
            if (hasCultivator(candidate.getBlockPos())) {
                continue;                                 // 核侧已有这一条 ⇒ 跳过（两侧不一致只跳过，不摘）
            }
            if (addCultivator(candidate.getBlockPos())) {
                candidate.setCore(this.getBlockPos());    // 先核侧、后培养器侧
                added++;
            }
        }
        if (added > 0) {
            Farmstorm.LOGGER.info("补绑：核 {} 本轮新增 {} 条（累计 {}/{}，周期 {} 刻）",
                    this.getBlockPos(), added, cultivatorCount(), cap, REBIND_INTERVAL_TICKS);
        }
    }

    /**
     * 服务端每刻的入口（{@code OutpostCoreBlock#getTicker} 注册）。
     *
     * <p>相位算式与陷阱 dust 同一形态（{@code TrapBehavior.java:89}）：
     * {@code floorMod(now + floorMod(pos.hashCode(), interval), interval) == 0} ⇒ 同一区块里的多个核心
     * 按坐标自行错峰，不会每 40 刻在同一 tick 一起发包（F-155 / D-121 第 3 条）。
     *
     * <p>四件事的顺序（F-250 / 42 号 §2.1.1）：早退 → **补绑（自己的门）** → 边界粒子门 → 粒子 +
     * 每 {@link #TICK_LOG_ROUNDS} 轮一条日志。两道门各自独立，之间没有 {@code return} 交叠。
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, OutpostCoreBlockEntity core) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        long now = server.getGameTime();
        // 补绑自带一套相位门（F-250 / D-176）：独立 if、放在边界粒子门**之前**、不 return。
        // 接在粒子门之后会被筛成 LCM(40, 100) = 200 刻，并随 boundaryParticleIntervalTicks 漂移。
        if (rebindDue(pos, now)) {
            core.rebindNearby(server);
        }
        int interval = FarmstormConfig.outpostCoreBoundaryParticleIntervalTicks.get();
        if (interval <= 0) {
            return;                                       // 防 floorMod 除零；config 范围 20~1200 取不到 0
        }
        if (Math.floorMod(now + Math.floorMod(pos.hashCode(), interval), interval) != 0) {
            return;
        }
        core.emitBoundaryRing(server, pos, now, interval);
        // F-198 的可观测面：每 30 轮（interval = 40 刻 ⇒ 1200 刻 ≈ 60 秒）一条，证明「无玩家靠近时方块实体仍在 tick」。
        long round = Math.floorDiv(now + Math.floorMod(pos.hashCode(), interval), interval);
        if (Math.floorMod(round, TICK_LOG_ROUNDS) == 0) {
            Farmstorm.LOGGER.info("核心 tick 轮次 {}：{}（间隔 {} 刻，区块票 farmstorm_core）", round, pos, interval);
        }
    }

    /**
     * 一轮边界粒子（F-155 / 23 号 §8.7）：几何算一次、按收件人逐个发包。
     *
     * <p>收件人三个条件同时成立：己方（{@link #isAlly}，红线第 4 条的唯一条链路）、在矩形或 16 格余量带内
     * （{@link #ringGap}）、本轮还没收到过别的核心发的一圈（{@link #LAST_RING_TICK} 用玩家 UUID 去重）。
     * 不区分创造与生存（D-121 补充项）。
     */
    private void emitBoundaryRing(ServerLevel server, BlockPos corePos, long now, int interval) {
        if (this.ownerUuid == null) {
            return;                                       // /setblock 出来的核心没有归属者，没有「己方」可发
        }
        long round = Math.floorDiv(now + Math.floorMod(corePos.hashCode(), interval), interval);
        boolean fullTier = Math.floorMod(round, RING_MARGIN_DECIMATION) == 0;
        // 两个方向都清：过期的不再挡人；记录来自上一次开服 / 上一次存档（时间倒流）的更不能再挡。
        LAST_RING_TICK.entrySet().removeIf(entry -> entry.getValue() > now || now - entry.getValue() >= interval);
        List<ClientboundLevelParticlesPacket> ring = null;
        for (ServerPlayer player : server.players()) {
            if (!this.isAlly(player)) {
                continue;                                 // 非己方看不见任何边界粒子（24 号 §11 #14）
            }
            double gap = ringGap(corePos, player.getX(), player.getZ());
            if (gap > RING_MARGIN) {
                continue;                                 // 出了余量带：不发
            }
            if (gap > 0.0 && !fullTier) {
                continue;                                 // 余量带：每 4 轮 1 次
            }
            Long last = LAST_RING_TICK.get(player.getUUID());
            if (last != null && now - last < interval) {
                continue;                                 // 这位本轮已经收过一圈：交界处只有一圈
            }
            if (ring == null) {
                ring = ringPackets(corePos);
            }
            for (ClientboundLevelParticlesPacket packet : ring) {
                player.connection.send(packet);
            }
            LAST_RING_TICK.put(player.getUUID(), now);
        }
    }

    /**
     * 一圈的 48 个包：48 × 48 格地面矩形的四条边、每 4 格一点、单圈 48 点（23 号 §8.7 / D-121 第 5 条）。
     *
     * <p>为什么一个点一个包（而不是每条边一个包）：原版 {@link ClientboundLevelParticlesPacket} 只有一个
     * 原点，{@code count > 1} 时客户端把每个粒子按 {@code nextGaussian() * dist} 撒在**包围盒**里
     * （{@code ClientPacketListener.java:2152-2158}），撒不出「四条边 + 每 4 格一点」的轮廓；把
     * {@code count} 取 1、三个 {@code dist} 取 0 才是**精确落点**（{@code nextGaussian() * 0 == 0}），
     * 48 个点因此需要 48 个包 / 圈 / 收件人。
     *
     * <p><b>已知代价（裁决 1：几何优先，保留 48 包 / 圈）</b>：48 包 / 圈 / 收件人 = 24 包/秒/人，是
     * 23 号 §8.7 包量账（每轮 1 个包）的 48 倍。取几何不取包量，是因为 25 号 §8.7 的验收读数是「四条边、
     * 间距约 4 格」这种几何量而不是包数，且 RT-30 的缓释（只发给范围内的己方 + 两档衰减）已经落地。
     *
     * <p>{@code overrideLimiter = true}：客户端 {@code LevelRenderer.addParticleInternal} 对
     * 未置位的粒子做 32 格距离剔除（{@code LevelRenderer.java:2668-2671}），不置位时直径 48 格 +
     * 余量带的远半边根本画不出来（D-121 依据里的「longDistance 参数」就是这一位）。
     */
    private static List<ClientboundLevelParticlesPacket> ringPackets(BlockPos corePos) {
        List<ClientboundLevelParticlesPacket> packets = new ArrayList<>(RING_POINTS);
        int minX = ringMin(corePos.getX() >> 4);
        int minZ = ringMin(corePos.getZ() >> 4);
        int maxX = minX + RING_BLOCKS;
        int maxZ = minZ + RING_BLOCKS;
        double y = corePos.getY() + RING_Y_OFFSET;
        for (int i = 0; i < RING_POINTS_PER_EDGE; i++) {
            int offset = i * RING_STEP;
            packets.add(ringPoint(minX + offset, y, minZ));
            packets.add(ringPoint(maxX, y, minZ + offset));
            packets.add(ringPoint(maxX - offset, y, maxZ));
            packets.add(ringPoint(minX, y, maxZ - offset));
        }
        return packets;
    }

    /** 一个精确落点：整格中心、贴地高度、零散布、1 个粒子。 */
    private static ClientboundLevelParticlesPacket ringPoint(int x, double y, int z) {
        return new ClientboundLevelParticlesPacket(RING_DUST, true,
                x + 0.5, y, z + 0.5,
                0.0F, 0.0F, 0.0F, 0.0F, 1);
    }

    /** 矩形起点：核心区块西 / 北邻区块的起始格（3 × 3 区块的西、北边界）。 */
    private static int ringMin(int chunkCoordinate) {
        return (chunkCoordinate - RANGE_CHUNKS) * 16;
    }

    /** 玩家到 48 × 48 矩形的水平间隙（矩形内为 0），两档衰减的判据。 */
    private static double ringGap(BlockPos corePos, double x, double z) {
        double minX = ringMin(corePos.getX() >> 4);
        double minZ = ringMin(corePos.getZ() >> 4);
        double dx = Math.max(Math.max(minX - x, x - (minX + RING_BLOCKS)), 0.0);
        double dz = Math.max(Math.max(minZ - z, z - (minZ + RING_BLOCKS)), 0.0);
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * 拆核迁移（F-233 / D-168 / 36 号 §2.7 的七步）：把这个核绑定的培养器逐个交给接替核。
     *
     * <p><b>怎么区分「方块真被移除」与「区块卸载 / 停服」</b>（本轮最容易出事的一处）：
     * <ul>
     *   <li><b>真移除</b>：{@code LevelChunk#setBlockState} 先把新状态写进 section，再在
     *       {@code LevelChunk.java:274} 调 {@code onRemove}，然后走
     *       {@code removeBlockEntity}（{@code LevelChunk.java:276} / {@code :439-447}）→ {@code setRemoved}。
     *       所以迁移跑起来时，该坐标的方块状态**已经是新的**了。</li>
     *   <li><b>区块卸载 / 停服</b>：{@code ServerLevel#unload}（{@code ServerLevel.java:953-955}）→
     *       {@code LevelChunk#clearAllBlockEntities}（{@code LevelChunk.java:616-619}）→
     *       {@code onChunkUnloaded} → {@code setRemoved}，**方块状态一个字节都没动**。</li>
     * </ul>
     * 因此判据是「该坐标当前的方块还是不是 {@code outpost_core}」——它只在真移除那一路会变。
     * <b>不能</b>拿 {@code Level#hasChunkAt} / 「区块在不在内存」当「方块还在不在」的替身：停服时区块照样
     * 不在内存，那个判据会把整本账读成「方块都没了」。区块用 {@code ServerChunkCache#getChunkNow} 取
     * （{@code ServerChunkCache.java:171-173}：只查已加载区块、不触发加载），方块读
     * {@code LevelChunk#getBlockState}（{@code LevelChunk.java:178}：纯 section 读，不触发加载、
     * 也不读方块实体的旧状态缓存）；取不到区块就按「无法证明方块没了」处理，一字节都不动。
     *
     * <p><b>三出口</b>（D-168 / RT-43）：
     * <ol>
     *   <li><b>① 改绑</b>：找到「覆盖该培养器 ∧ 己方 ∧ 未满」的接替核（最近优先，平手键同 D-165）⇒
     *       写新核的 {@code Cultivators} + 重塑培养器的 {@code Core} 缓存。世界不动。</li>
     *   <li><b>② 留原地、标记未绑定</b>：一个都没有，且区块在内存、该格仍是培养器 ⇒ 清 {@code Core} 键
     *       （{@code Held} / {@code Progress} 原样保留）⇒ 它从此不可使用。<b>不变泥土、不掉落</b>。</li>
     *   <li><b>③ 防御性跳过</b>：一个都没有，且区块取不到 **或** 该格已不是培养器 ⇒ 什么都不写，
     *       培养器 NBT 原值保留（不猜测、不删绑定）。</li>
     * </ol>
     *
     * <p><b>三条负面清单</b>（36 号 §2.7，写在这里是因为它们各自对应一次真实事故）：
     * <ol>
     *   <li><b>方法体内没有 {@code setBlock}</b>：0.1.1 的「迁不动 ⇒ 变泥土」随整段改写消失。
     *       培养器是独立方块，「无法使用」不等于「被拆掉」（D-168 补充）。</li>
     *   <li><b>不为迁移加载区块</b>：全程 {@code getChunkNow}。被绑的培养器必在核的 3 × 3 内，而
     *       {@code CoreChunkTickets} 的票在 {@link #release()} 里才放，{@code release()} 是最后一行
     *       ⇒ 迁移期间这些区块在内存；万一取不到就走出口③。</li>
     *   <li><b>物品不在这里掉</b>：54 格走 {@code OutpostCoreBlock#onRemove} 的
     *       {@code Containers.dropContents}。这里只清 {@code Cultivators}，{@code items} 一个都不动
     *       ——两条钩子分工（D-167 补充）。</li>
     * </ol>
     *
     * <p>{@code addCultivator} 返回 {@code false}（理论上不可达：候选已过滤「未满」，且同线程内立即写入）
     * 时按「没有接替核」处理，走出口②/③，不留下指向死核的假绑定。
     */
    private void settleOnRemoval() {
        if (!(this.level instanceof ServerLevel server) || this.ownerUuid == null) {
            return;
        }
        BlockPos corePos = this.getBlockPos();
        LevelChunk chunk = server.getChunkSource().getChunkNow(corePos.getX() >> 4, corePos.getZ() >> 4);
        if (chunk == null) {
            return;                                       // 区块不在内存：无法证明方块没了 ⇒ 什么都不动
        }
        if (chunk.getBlockState(corePos).getBlock() instanceof OutpostCoreBlock) {
            return;                                       // 方块还在原地：这是区块卸载 / 停服，不是移除
        }
        // 步骤 4：**先复制一份再遍历** —— 本方法会改 this.cultivators，边遍历边改会跳过或重复处理同一项。
        List<BlockPos> bound = this.cultivators();
        TeamResolver.Owner self = new TeamResolver.Owner(this.ownerUuid, this.ownerName);
        for (BlockPos pos : bound) {
            // 出口①：接替核 = 覆盖 ∧ 己方（相对本核归属者）∧ 未满，最近优先；本次已写走的名额自然包含在内
            OutpostCoreBlockEntity next = selectCore(server, pos, self, corePos);
            if (next != null && next.addCultivator(pos)) {
                LevelChunk nextChunk = server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (nextChunk != null
                        && nextChunk.getBlockEntity(pos) instanceof CultivatorBlockEntity cultivator) {
                    cultivator.setCore(next.getBlockPos());        // 两侧都写：核侧已写，这里写培养器侧缓存
                }
                continue;
            }
            // 以下都是「没有可用的接替核」：先看培养器那一格读不读得到、还是不是培养器（都不动世界）
            LevelChunk cultivatorChunk = server.getChunkSource()
                    .getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (cultivatorChunk == null) {
                Farmstorm.LOGGER.info("迁移失败且区块未加载：保留 NBT 原值 {}", pos);   // 出口③
                continue;
            }
            if (!(cultivatorChunk.getBlockState(pos).getBlock() instanceof CultivatorBlock)) {
                Farmstorm.LOGGER.info("迁移失败且该格已不是培养器：保留 NBT 原值 {}", pos);  // 出口③
                continue;
            }
            if (cultivatorChunk.getBlockEntity(pos) instanceof CultivatorBlockEntity cultivator) {
                cultivator.clearCore();                    // 出口②：留原地、清 Core、内容保留、不可使用
                Farmstorm.LOGGER.info("迁移失败：培养器留原地并标为未绑定 {}", pos);
            }
        }
        this.cultivators.clear();       // 只清绑定列表；items 不动（掉落是 onRemove 的 Containers.dropContents）
        this.setChanged();
        this.release();                 // 最后一行：摘索引 + 释放区块票 + 请求刷新浮窗列表（既有方法）
    }


    /**
     * 方块实体被移除：先过一次性闸，再由 {@link #settleOnRemoval()} 自己判「真移除 / 卸载」。
     * 闸放在判据之前——同一个实例上重复到达时不必再读一遍区块。
     */
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.cleared) {
            return;
        }
        this.cleared = true;
        this.settleOnRemoval();
    }

    /**
     * 客户端同步面（RT-44 的正面回答，36 号 §2.6.1）：<b>只写 owner 三键</b>——
     * 不写 {@code Items}（54 个 ItemStack 全量进这里会让每次区块下发多出几 KB，收益是 0：
     * 界面打开时由原版菜单槽位逐格同步），也不写 {@code Cultivators}（由连线的包下发，F-239）。
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        DeployedOwnership.write(tag, this.ownerUuid, this.ownerName);
        tag.putLong(TAG_PLACED_GAME_TIME, this.placedGameTime);
        return tag;
    }

    /** 绑定变化时由 {@code sendBlockUpdated} 触发；体量与 0.1.1 持平（见 {@link #getUpdateTag}）。 */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        DeployedOwnership.write(tag, this.ownerUuid, this.ownerName);
        tag.putLong(TAG_PLACED_GAME_TIME, this.placedGameTime);
        // D-166 / RT-44：存档面只增 Items 一块；键名走 ContainerHelper.TAG_ITEMS（"Items"）。
        ContainerHelper.saveAllItems(tag, this.items, registries);
        // F-231：绑定列表落盘为 Cultivators（Long 列表，每条 BlockPos#asLong）。空列表也写，与 Items 同一体例。
        ListTag cultivatorsTag = new ListTag();
        for (BlockPos cultivatorPos : this.cultivators) {
            cultivatorsTag.add(LongTag.valueOf(cultivatorPos.asLong()));
        }
        tag.put(TAG_CULTIVATORS, cultivatorsTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownerUuid = DeployedOwnership.readUuid(tag);
        this.ownerName = DeployedOwnership.readName(tag);
        this.placedGameTime = tag.getLong(TAG_PLACED_GAME_TIME);
        // 老档兼容读（36 号 §2.6.2）：缺 Items 键时 loadAllItems 一次都不 set ⇒ this.items 保持 54 个空槽，
        // 不报错、不清档；老档里多出来的键（Bindings / ByCore 之类）不读、不删，原样留在 CompoundTag 里。
        ContainerHelper.loadAllItems(tag, this.items, registries);
        // 缺 Cultivators 键 ⇒ 空列表（老核绑的是耕地，不是培养器，36 号 §2.6.2）；不按「键缺失」反推别的含义。
        this.cultivators.clear();
        ListTag cultivatorsTag = tag.getList(TAG_CULTIVATORS, Tag.TAG_LONG);
        for (int i = 0; i < cultivatorsTag.size(); i++) {
            if (cultivatorsTag.get(i) instanceof LongTag entry) {
                this.cultivators.add(BlockPos.of(entry.getAsLong()));
            }
        }
    }
}
