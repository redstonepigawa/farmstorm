package com.dashpig.farmstorm.network;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.outpost.OutpostCoreIndex;
import com.dashpig.farmstorm.team.TeamResolver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 「己方核心列表」的服务端广播器（F-200 / 30 号 §2.1.3 ~ §2.1.5）。
 *
 * <p>四件事：**组装**（按 viewer 自己的同盟关系收集候选）、**三级排序**（A-39）、
 * **去抖**（{@link #MIN_INTERVAL_TICKS}）、**对账**（{@link #RECONCILE_INTERVAL_TICKS}）。
 *
 * <p>比较键**只从 {@link OutpostCoreIndex} 读**：本类不调用任何区块加载入口、也不读方块实体，
 * 因此「不为比较而加载候选核所在区块」是结构性的（30 号 §2.2.10 / A-39）。
 */
public final class CoreListBroadcaster {
    /** 同一次连锁事件可能在同刻触发多次请求；10 刻 = 0.5 秒，人眼仍在「立即」区间。 */
    public static final int MIN_INTERVAL_TICKS = 10;

    /** 兜「没有事件但列表变了」的路径（队友换队 / 离线 / 漏事件）：20 人服每 5 秒 20 次指纹比较。 */
    public static final int RECONCILE_INTERVAL_TICKS = 100;

    /** {@code UUID → 上一次真正发出的游戏刻}。 */
    private static final Map<UUID, Long> LAST_SENT_TICK = new HashMap<>();

    /** 待发集合：{@code request} 只往这里放，{@code ServerTickEvent.Post} 统一 flush。 */
    private static final Set<UUID> PENDING = new HashSet<>();

    /** {@code UUID → 上一次真正发出的列表指纹}，对账用。 */
    private static final Map<UUID, Fingerprint> FINGERPRINTS = new HashMap<>();

    /** 列表指纹：只由「已排序的 (维度, 坐标, 归属名, 是否自己)」与 total 决定，与遍历顺序无关。 */
    private record Fingerprint(int hash, int total) {}

    /** 组装期的中间形态：{@code gameTime} 是排序键，不上线，因此不放进 payload 的 {@code Core}。 */
    private record Candidate(CoreListPayload.Core core, long gameTime) {}

    /**
     * A-39 的三级比较键：{@code gameTime} 升序 → 维度 id → {@code BlockPos#compareTo}（y → z → x）。
     *
     * <p>第三级展开成三个 {@code thenComparingInt} 而不是直接写 {@code BlockPos::compareTo}：
     * {@code BlockPos} 继承的是 {@code Vec3i#compareTo(Vec3i)}，展开写既逐字对应 A-39 的次序，
     * 也不依赖泛型推断的边界。
     */
    private static final Comparator<Candidate> ORDER = Comparator
            .comparingLong(Candidate::gameTime)
            .thenComparing(candidate -> candidate.core().dimension().toString())
            .thenComparingInt(candidate -> candidate.core().pos().getY())
            .thenComparingInt(candidate -> candidate.core().pos().getZ())
            .thenComparingInt(candidate -> candidate.core().pos().getX());

    private CoreListBroadcaster() {}

    /** 单个玩家请求一次（登录 / 重连 / 首次加入 / 换维度 / 重生）。 */
    public static void request(ServerPlayer player) {
        if (player != null) {
            PENDING.add(player.getUUID());
        }
    }

    /** 全部在线玩家各请求一次（放置 / 摧毁 / 迁移：收件人集合由每人自己的组装结果决定）。 */
    public static void requestAll(MinecraftServer server) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PENDING.add(player.getUUID());
        }
    }

    /**
     * 登出清三条记录（30 号 §2.1.4 的「断线兜底」）。
     * 不 clean 会让重连后的第一次请求被判成「刚发过」而跳过。
     */
    public static void onLogout(UUID playerId) {
        LAST_SENT_TICK.remove(playerId);
        PENDING.remove(playerId);
        FINGERPRINTS.remove(playerId);
    }

    /** {@code ServerTickEvent.Post} 的唯一入口：先去抖 flush，每 100 刻再做一次指纹对账。 */
    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        flush(server, now);
        if (now % RECONCILE_INTERVAL_TICKS == 0) {
            reconcile(server);
        }
    }

    /** 同一刻的多次请求只发一次（去抖的单位是「玩家 × 包」，不是事件）。 */
    private static void flush(MinecraftServer server, long now) {
        if (PENDING.isEmpty()) {
            return;
        }
        List<UUID> players = new ArrayList<>(PENDING);
        players.sort(Comparator.comparing(UUID::toString));      // 日志顺序不依赖 HashSet 的迭代顺序
        for (UUID playerId : players) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                PENDING.remove(playerId);
                LAST_SENT_TICK.remove(playerId);
                continue;
            }
            Long last = LAST_SENT_TICK.get(playerId);
            if (last == null || now - last >= MIN_INTERVAL_TICKS) {
                PENDING.remove(playerId);
                send(server, player, now);
            }
        }
    }

    /** 队友换队 / 上线 / 漏事件：只对指纹变了的玩家请求，最长 5 秒延迟。 */
    private static void reconcile(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            List<CoreListPayload.Core> cores = new ArrayList<>();
            int total = build(server, player, cores);
            if (!fingerprint(cores, total).equals(FINGERPRINTS.get(player.getUUID()))) {
                PENDING.add(player.getUUID());
            }
        }
    }

    /** 唯一的发送点。 */
    private static void send(MinecraftServer server, ServerPlayer player, long now) {
        List<CoreListPayload.Core> cores = new ArrayList<>();
        int total = build(server, player, cores);
        // 原版客户端 / 没装本模组的客户端不声明这条通道；NeoForge 的 NetworkRegistry#checkPacket 会对
        // 「向不支持该 channel 的连接发 optional payload」抛 UnsupportedOperationException 并崩服
        // （本文档-源码偏差表 D-1 的实测栈）。所以先问连接一声，跳过时不发、只记组装结果。
        boolean deliverable = player.connection.hasChannel(CoreListPayload.TYPE);
        LAST_SENT_TICK.put(player.getUUID(), now);
        FINGERPRINTS.put(player.getUUID(), fingerprint(cores, total));
        if (deliverable) {
            PacketDistributor.sendToPlayer(player, new CoreListPayload(List.copyOf(cores), total));
            Farmstorm.LOGGER.info("核心列表下发：{} {} 条（total={}）",
                    player.getScoreboardName(), cores.size(), total);
        } else {
            Farmstorm.LOGGER.info("核心列表组装（客户端未声明 {}，跳过发送）：{} {} 条（total={}）",
                    CoreListPayload.TYPE.id(), player.getScoreboardName(), cores.size(), total);
        }
        if (!cores.isEmpty()) {
            // 顺序取证（30 号 §7 #9 的 ①②③）：日志里能直接看出三级比较键定序的结果。
            List<String> order = new ArrayList<>(cores.size());
            for (CoreListPayload.Core core : cores) {
                order.add(core.dimension().getPath() + "@" + core.pos().getX() + ","
                        + core.pos().getY() + "," + core.pos().getZ());
            }
            Farmstorm.LOGGER.info("核心列表顺序：{}", order);
        }
    }

    /**
     * 服务端权威组装（30 号 §2.1.5）：遍历索引全部 owner → 与 viewer 做 UUID 侧己方判定 →
     * 收候选 → 三级排序 → {@code total} = 候选总数、{@code cores} = 前 {@code MAX_CORES} 条。
     *
     * <p>敌对核整段跳过：不把敌对核发给客户端（D-143）。
     *
     * @return 截断**前**的总数（`+N` 要的是它）
     */
    private static int build(MinecraftServer server, ServerPlayer viewer, List<CoreListPayload.Core> out) {
        OutpostCoreIndex index = OutpostCoreIndex.get(server);
        TeamResolver.Owner viewerOwner = new TeamResolver.Owner(viewer.getUUID(), viewer.getScoreboardName());
        ServerLevel level = server.overworld();                 // 队伍表是全服一张，取哪个维度都一样
        List<Candidate> candidates = new ArrayList<>();
        for (UUID owner : index.owners()) {
            List<OutpostCoreIndex.Entry> entries = index.entriesOf(server, owner);
            if (entries.isEmpty()) {
                continue;
            }
            String ownerName = index.ownerName(owner);
            if (!Farmstorm.TEAMS.isAlly(level, new TeamResolver.Owner(owner, ownerName), viewerOwner)) {
                continue;
            }
            boolean self = owner.equals(viewer.getUUID());
            for (OutpostCoreIndex.Entry entry : entries) {
                candidates.add(new Candidate(
                        new CoreListPayload.Core(entry.dimension(), entry.pos(),
                                ownerName == null ? "" : ownerName, self),
                        entry.gameTime()));
            }
        }
        candidates.sort(ORDER);
        out.clear();
        int shown = Math.min(candidates.size(), CoreListPayload.MAX_CORES);
        for (int i = 0; i < shown; i++) {
            out.add(candidates.get(i).core());
        }
        return candidates.size();
    }

    private static Fingerprint fingerprint(List<CoreListPayload.Core> cores, int total) {
        long hash = 17L;
        for (CoreListPayload.Core core : cores) {
            ResourceLocation dimension = core.dimension();
            hash = hash * 31L + dimension.hashCode();
            hash = hash * 31L + core.pos().asLong();
            hash = hash * 31L + core.ownerName().hashCode();
            hash = hash * 31L + (core.self() ? 1 : 0);
        }
        return new Fingerprint(Long.hashCode(hash), total);
    }
}
