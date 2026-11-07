package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.DeployedBehavior;
import com.dashpig.farmstorm.behavior.DeployedBehaviors;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.impl.FangTrapCactusTrap;
import com.dashpig.farmstorm.block.DeployedCropBlock;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import com.dashpig.farmstorm.trap.TrapFifoIndex;
import com.dashpig.farmstorm.util.DeployedOwnership;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * 部署物的全部运行时状态与逻辑入口（TECH §4.5）。
 * C-9：只存 ownerUuid + ownerName，不存任何队伍快照；己方判定每次触发时实时解析。
 */
public class DeployedCropBlockEntity extends BlockEntity {
    public static final int HARD_MINE_TICKS = 200;

    /** 安全阀（HC-13 / TECH §4.11.6）：服务器保护底线，不得调整、不得挪进 config。 */
    public static final int DEPLOY_COOLDOWN_TICKS = 5;
    public static final int MAX_DEPLOYED_PER_CHUNK = 8;
    public static final int MAX_DEPLOYED_PER_PLAYER = 32;
    public static final int MAX_EFFECTS_PER_TICK = 64;

    private static final String TAG_PLACED_GAME_TIME = "placedGameTime";
    private static final String TAG_PENDING_WAVES = "pendingWaves";
    private static final String TAG_FUSE_TICKS = "fuseTicks";
    private static final String TAG_DETACHED = "detached";
    private static final String TAG_INTERCEPT_COUNT = "interceptCount";
    private static final String TAG_WAVE_AT_TICK = "atTick";
    private static final String TAG_WAVE_COUNT = "count";
    private static final String TAG_WAVE_RADIUS = "radius";

    /** 仙人掌陷阱的两圈尖牙延迟队列（§4.5）。 */
    public record FangWave(long atTick, int count, double radius) {}

    /** 单玩家的部署计数与最近一次放置 / 拆除的 gameTime（§8.3）。 */
    private record DeployCounter(int count, long lastActionTick) {}

    /** 本 tick 已经施加过某种光环的玩家（RT-06 去重键）。 */
    private record AppliedAura(DeployedKind kind, UUID playerId) {}

    /** 放置限流账本：只在服务端主线程读写，因此用普通 HashMap；不写存档（D-18）。 */
    private static final Map<UUID, DeployCounter> DEPLOY_LEDGER = new HashMap<>();

    /** 光环施加配额（RT-06），每个 gameTime 清零一次。 */
    private static final Set<AppliedAura> APPLIED_AURAS = new HashSet<>();

    /** 账本归属的服务器实例：单人切换存档 / 重启都会换实例，旧账本随即作废。 */
    @Nullable
    private static MinecraftServer ledgerOwner;
    private static long auraTick = Long.MIN_VALUE;
    private static int auraSpent;

    private final DeployedKind kind;

    /** 行为对象：由 kind 推导的静态单例，不参与序列化（§4.5）。 */
    @Nullable
    private DeployedBehavior behavior;

    /** 错峰偏移，首次 tick 时按当前扫描间隔算一次（§8.1）。 */
    private int stagger = Integer.MIN_VALUE;

    /** setRemoved 可能在区块卸载时也被调用，用这个标志保证一份配额只释放一次。 */
    private boolean ledgerReleased;

    @Nullable
    private UUID ownerUuid;
    @Nullable
    private String ownerName;
    private long placedGameTime;
    private int cooldown;
    /** 近防菌的拦截计数（§3.10）：每成功拦下一次 +1，到上限那一次先完成拦截再静默移除自己。 */
    private int interceptCount;
    private List<FangWave> pendingWaves = List.of();
    private int fuseTicks;
    private boolean detached;

    public DeployedCropBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.deployedOf(kindOf(state)), pos, state);
        this.kind = kindOf(state);
    }

    private static DeployedKind kindOf(BlockState state) {
        if (state.getBlock() instanceof DeployedCropBlock block) {
            return block.kind();
        }
        // 注册表保证只有 DeployedCropBlock 会创建本 BE；真出现别的方块说明方块与 BE 配错了，直接报出配置错误。
        throw new IllegalStateException("DeployedCropBlockEntity attached to a non-deployed block: " + state.getBlock());
    }

    public DeployedKind kind() {
        return this.kind;
    }

    /** 行为单例（§4.5）：第一次取用时按 kind 解析，之后一直复用。 */
    public DeployedBehavior behavior() {
        if (this.behavior == null) {
            this.behavior = DeployedBehaviors.of(this.kind);
        }
        return this.behavior;
    }

    /**
     * tick 分发（§4.5）：延迟尖牙波 → 冷却递减早退 → 错峰主扫描。
     * 主扫描把本 tick 交给行为对象（陷阱触发 / 炮台索敌 / 光环施加）。
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, DeployedCropBlockEntity be) {
        if (!(level instanceof ServerLevel server)) {
            return;                                                       // 双保险：客户端永不 tick
        }
        long now = server.getGameTime();
        be.drainPendingWaves(server, now);
        if (be.isRemoved()) {
            return;                                                       // 延迟波已让陷阱自毁：本 tick 不再扫描
        }
        if (be.cooldown > 0) {
            be.cooldown--;                                                // 冷却期连索敌都不做（§8.1）
            return;
        }
        int interval = Math.max(1, be.behavior().scanInterval());
        if ((now + be.stagger(interval)) % interval == 0) {
            be.onScan(server, pos, state);
        }
    }

    /** 错峰：floorMod(pos.hashCode(), interval)，把同区块的多个部署物分摊到不同 tick（§8.1）。 */
    private int stagger(int interval) {
        if (this.stagger == Integer.MIN_VALUE) {
            this.stagger = Math.floorMod(this.getBlockPos().hashCode(), interval);
        }
        return this.stagger;
    }

    /** 主扫描入口：先推进 §4.5 的延迟引信槽，再执行行为。 */
    private void onScan(ServerLevel level, BlockPos pos, BlockState state) {
        if (this.fuseTicks > 0 && --this.fuseTicks == 0) {
            this.consume();
            return;
        }
        this.behavior().tick(this);
    }

    /**
     * 延迟尖牙波（§4.5）：到点的波交给仙人掌陷阱出牙，队列出空后自毁。
     * 每 tick 都跑，因此 0.2 s / 0.5 s 的延迟不受 10 tick 主扫描间隔限制。
     */
    public void drainPendingWaves(ServerLevel level, long now) {
        if (this.pendingWaves.isEmpty()) {
            return;
        }
        List<FangWave> remaining = new ArrayList<>(this.pendingWaves.size());
        boolean spawnedAny = false;
        for (FangWave wave : this.pendingWaves) {
            if (wave.atTick() > now) {
                remaining.add(wave);
                continue;
            }
            FangTrapCactusTrap.spawnWave(this, level, wave);
            spawnedAny = true;
        }
        if (!spawnedAny) {
            return;
        }
        if (remaining.isEmpty()) {
            this.consume();
            return;
        }
        this.pendingWaves = List.copyOf(remaining);
        this.setChanged();
    }

    /**
     * 放置闸门（TECH §8.3 / F-25）：冷却 → 单玩家上限 → 单区块上限。
     * 判定发生在 BlockItem 扣物品之前，因此被拒绝时物品不消耗、不产生掉落。
     *
     * @param player 放置者；为 null（发射器 / 结构生成）时只校验单区块上限
     */
    public static boolean canDeploy(Level level, BlockPos pos, @Nullable Player player) {
        if (!(level instanceof ServerLevel server)) {
            return true;                                                  // 客户端只做预测，判定一律在服务端（HC-03）
        }
        if (player != null) {
            DeployCounter counter = ledgerOf(server).get(player.getUUID());
            if (counter != null) {
                if (server.getGameTime() - counter.lastActionTick() < DEPLOY_COOLDOWN_TICKS) {
                    return false;                                         // 「放 → 立刻拆 → 再放」被冷却挡住
                }
                if (counter.count() >= MAX_DEPLOYED_PER_PLAYER) {
                    return false;
                }
            }
        }
        return countDeployedInChunk(server, pos) < MAX_DEPLOYED_PER_CHUNK;
    }

    /** 单区块上限按实际方块数计算，不另记账：区块卸载 / 存档重读后依然准确。 */
    private static int countDeployedInChunk(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        int count = 0;
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof DeployedCropBlockEntity) {
                count++;
            }
        }
        return count;
    }

    private static Map<UUID, DeployCounter> ledgerOf(ServerLevel level) {
        MinecraftServer server = level.getServer();
        if (server != ledgerOwner) {
            DEPLOY_LEDGER.clear();
            ledgerOwner = server;
        }
        return DEPLOY_LEDGER;
    }

    /**
     * 光环施加配额（RT-06）：同 tick 同玩家同种光环只施加一次，整 tick 最多
     * {@link #MAX_EFFECTS_PER_TICK} 次施加；超额只丢本次，下一次扫描会重新尝试。
     */
    public static boolean claimAura(DeployedKind kind, UUID playerId, long gameTime) {
        if (gameTime != auraTick) {
            auraTick = gameTime;
            APPLIED_AURAS.clear();
            auraSpent = 0;
        }
        if (auraSpent >= MAX_EFFECTS_PER_TICK) {
            return false;
        }
        if (!APPLIED_AURAS.add(new AppliedAura(kind, playerId))) {
            return false;
        }
        auraSpent++;
        return true;
    }

    /** 只写入 ownerUuid + ownerName（C-9），并记录部署时刻供硬挖与冷却推进使用。 */
    public void setOwner(Player placer) {
        this.ownerUuid = placer.getUUID();
        this.ownerName = placer.getGameProfile().getName();
        this.placedGameTime = placer.level().getGameTime();
        if (this.level instanceof ServerLevel server) {
            long now = server.getGameTime();
            Map<UUID, DeployCounter> ledger = ledgerOf(server);
            DeployCounter counter = ledger.get(placer.getUUID());
            ledger.put(placer.getUUID(), new DeployCounter(counter == null ? 1 : counter.count() + 1, now));
            // F-149（D-115）：陷阱名额的反向索引在这里加项，与限流账本的上加同进同出（§5.4 第 4 步）。
            TrapFifoIndex.of(server).record(server, placer.getUUID(), this.getBlockPos(), this.placedGameTime,
                    this.kind);
            this.behavior().onDeployed(this);                             // 行为层的部署钩子只在服务端触发（HC-03）
        }
        this.setChanged();
    }

    /**
     * 己方判定。客户端不做归属判定（HC-03）：多人场景下客户端看不到完整队伍信息，
     * 统一按「己方」显示挖掘进度，真实与否决一律以服务端为准（RT-02 的取舍）。
     */
    public boolean isAlly(Player player) {
        if (!(this.level instanceof ServerLevel server)) {
            return true;
        }
        return DeployedOwnership.isAlly(server, this.ownerUuid, this.ownerName, player);
    }

    /** 归属者在线时解析为实体；离线返回 null（队伍判定不依赖它，见 DeployedOwnership）。 */
    @Nullable
    public ServerPlayer owner(ServerLevel level) {
        if (this.ownerUuid == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(this.ownerUuid);
    }

    /** 近防菌的拦截计数（§3.10）。 */
    public int interceptCount() {
        return this.interceptCount;
    }

    /**
     * 记一次成功拦截（TECH §3.10 / §6.2 第 6 步第四步）：计数 +1 → 到 {@code maxIntercepts} 就走
     * {@link #consume()}（{@code removeBlock} + 破坏粒子，不走战利品表，D-72）。
     * 上限读 config 而不是写死常量：6 次是 D-88 明写的可调数值。
     */
    public void recordIntercept(long now) {
        this.interceptCount++;
        this.setChanged();
        int max = FarmstormConfig.pointDefenseFungusMaxIntercepts.get();
        Farmstorm.LOGGER.info("近防菌 拦截 t={} 计数 {}/{} @ {}", now, this.interceptCount, max, this.getBlockPos());
        if (this.interceptCount >= max) {
            this.consume();
        }
    }

    /** 一次性部署物（陷阱）自毁：移除方块并播破坏粒子（§4.5）。 */
    public void consume() {
        if (!(this.level instanceof ServerLevel server)) {
            return;
        }
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();
        server.removeBlock(pos, false);
        server.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
    }

    /** 拆除 / 卸载时释放一个玩家配额（§8.3）；账本只在内存，不产生任何存档写入（D-18）。 */
    @Override
    public void setRemoved() {
        super.setRemoved();
        this.releaseTrapSlot();
        if (this.ledgerReleased || this.ownerUuid == null || !(this.level instanceof ServerLevel server)) {
            return;
        }
        this.ledgerReleased = true;
        Map<UUID, DeployCounter> ledger = ledgerOf(server);
        DeployCounter counter = ledger.get(this.ownerUuid);
        if (counter == null) {
            return;
        }
        ledger.put(this.ownerUuid, new DeployCounter(Math.max(0, counter.count() - 1), server.getGameTime()));
    }

    /**
     * F-149（D-115）：方块的移除是账目的减项，与限流账本的下减同进同出（§5.4 第 4 步）。
     *
     * <p>{@code setRemoved} 不只在方块被移除时调：区块卸载与停服（{@code ServerLevel#unload →
     * LevelChunk#clearAllBlockEntities}）也会调，那时方块还在原地、只是区块不 tick 了。所以这里**只在一个前提下**
     * 摘账目——能拿到本区块、且能读到「该坐标已经不再是本地块」；拿不到区块就当「无法证明方块没了」，一律不动。
     * 错判的方向决定代价：条目留错了有 {@code TrapFifoIndex} 的惰性校验兜底，删错了就是数据丢失
     * （停服时那一轮落盘会把索引写成空表，重启后 FIFO 直接失效）。
     *
     * <p>区块用 {@code ServerChunkCache#getChunkNow} 取：它只查已加载的区块，不触发加载。
     * 方块读 {@code LevelChunk#getBlockState}，不用 {@code BlockEntity#getBlockState()}（后者会缓存旧状态），
     * 也不用 {@code Level#getBlockState}（区块不在时会触发加载）。
     */
    private void releaseTrapSlot() {
        if (this.ownerUuid == null || !(this.level instanceof ServerLevel server)) {
            return;
        }
        BlockPos pos = this.getBlockPos();
        LevelChunk chunk = server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) {
            return;                                                       // 区块不在内存：无法判定，账目留着
        }
        if (chunk.getBlockState(pos).getBlock() instanceof DeployedCropBlock block && block.kind() == this.kind) {
            return;                                                       // 方块还在：这是卸载 / 停服，不是移除
        }
        TrapFifoIndex.of(server).remove(server, this.ownerUuid, pos);
    }

    public void markDetached() {
        this.detached = true;
        this.setChanged();
    }

    public boolean isDetached() {
        return this.detached;
    }

    public long placedGameTime() {
        return this.placedGameTime;
    }

    public int cooldown() {
        return this.cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
        this.setChanged();
    }

    public int fuseTicks() {
        return this.fuseTicks;
    }

    public void setFuseTicks(int fuseTicks) {
        this.fuseTicks = fuseTicks;
        this.setChanged();
    }

    public List<FangWave> pendingWaves() {
        return this.pendingWaves;
    }

    public void setPendingWaves(List<FangWave> pendingWaves) {
        this.pendingWaves = List.copyOf(pendingWaves);
        this.setChanged();
    }

    @Nullable
    public UUID ownerUuid() {
        return this.ownerUuid;
    }

    @Nullable
    public String ownerName() {
        return this.ownerName;
    }

    /** 附着面：行为层只看这里，不直接解析 BlockState。 */
    public AttachFace attachFace() {
        return this.getBlockState().getValue(DeployedCropBlock.FACE);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.writeSynced(tag);
        tag.putInt(TAG_INTERCEPT_COUNT, this.interceptCount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.ownerUuid = DeployedOwnership.readUuid(tag);
        this.ownerName = DeployedOwnership.readName(tag);
        this.placedGameTime = tag.getLong(TAG_PLACED_GAME_TIME);
        this.fuseTicks = tag.getInt(TAG_FUSE_TICKS);
        this.detached = tag.getBoolean(TAG_DETACHED);
        this.interceptCount = tag.getInt(TAG_INTERCEPT_COUNT);
        this.pendingWaves = readWaves(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.writeSynced(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** 6 个同步字段（§4.5）：ownerUuid / ownerName / placedGameTime / pendingWaves / fuseTicks / detached。 */
    private void writeSynced(CompoundTag tag) {
        DeployedOwnership.write(tag, this.ownerUuid, this.ownerName);
        tag.putLong(TAG_PLACED_GAME_TIME, this.placedGameTime);
        tag.putInt(TAG_FUSE_TICKS, this.fuseTicks);
        tag.putBoolean(TAG_DETACHED, this.detached);
        ListTag waves = new ListTag();
        for (FangWave wave : this.pendingWaves) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_WAVE_AT_TICK, wave.atTick());
            entry.putInt(TAG_WAVE_COUNT, wave.count());
            entry.putDouble(TAG_WAVE_RADIUS, wave.radius());
            waves.add(entry);
        }
        tag.put(TAG_PENDING_WAVES, waves);
    }

    private static List<FangWave> readWaves(CompoundTag tag) {
        if (!tag.contains(TAG_PENDING_WAVES, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag waves = tag.getList(TAG_PENDING_WAVES, Tag.TAG_COMPOUND);
        List<FangWave> result = new ArrayList<>(waves.size());
        for (int i = 0; i < waves.size(); i++) {
            CompoundTag entry = waves.getCompound(i);
            result.add(new FangWave(entry.getLong(TAG_WAVE_AT_TICK), entry.getInt(TAG_WAVE_COUNT),
                    entry.getDouble(TAG_WAVE_RADIUS)));
        }
        return List.copyOf(result);
    }
}
