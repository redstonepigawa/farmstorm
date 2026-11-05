package com.dashpig.farmstorm.network;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 「核 ↔ 培养器」连线（F-239 / D-169 / 36 号 §2.8.2）的服务端广播器。
 *
 * <p>结构与 {@link CoreListBroadcaster} 同形（去抖口径、{@code hasChannel} 收件人判定、
 * {@code requestAll} / {@code onLogout}），**不发明第二套**去抖：
 * <b>去抖 {@link #MIN_INTERVAL_TICKS} 刻</b>、<b>范围重扫 {@link #RESCAN_INTERVAL_TICKS} 刻</b>。
 *
 * <p><b>五个时机</b>（36 号 §2.8.2 的表，逐条给出触发点）：
 * <ol>
 *   <li><b>进范围</b> —— {@link #tick} 的 20 刻重扫（无事件的路径只能靠轮询）。</li>
 *   <li><b>出范围</b> —— 同上；组装结果为空 ⇒ 发**空列表**清客户端残留（35 号 §12 B-38）。</li>
 *   <li><b>绑定变化</b>（培养器放置 / 被拆 / 迁移改绑）—— {@code OutpostCoreBlockEntity#addCultivator} /
 *       {@code #removeCultivator} 与 {@code CultivatorBlockEntity#releaseHeld} 里的 {@link #requestAll}。</li>
 *   <li><b>核被拆</b> —— {@code OutpostCoreBlockEntity#release()}（{@code OutpostCoreBlock#onRemove} 与
 *       {@code settleOnRemoval} 共用这一处）里的 {@link #requestAll}。</li>
 *   <li><b>登录 / 换维度 / 重生 / 登出</b> —— {@code FarmstormGameEvents} 的四个既有请求点；
 *       登出走 {@link #onLogout} 清三条记录。</li>
 * </ol>
 *
 * <p><b>范围判据只有一处</b>：{@link OutpostCoreBlockEntity#inRange(BlockPos, BlockPos)}（{@code RANGE_CHUNKS = 1}）。
 * 本类**不重复任何范围算式**——D-169 的「玩家身处该核的 3 × 3 区块范围内」与 D-165 的绑定覆盖是同一组数字，
 * 写第二份就会漂移成两个答案（36 号 §7 翻车点 5）。
 *
 * <p><b>不加载区块</b>：候选核的方块实体一律走 {@code ServerChunkCache#getChunkNow}（只查已加载），
 * 取不到就跳过；本类与 {@link CoreListBroadcaster} 一样不调用任何区块加载入口。
 *
 * <p><b>服务端不依赖连线</b>（D-169 补充 / 36 号 §8 #34）：本类只读索引与绑定列表、只发 S2C 包；
 * 绑定 / 产出 / 迁移 / 掉落四条线一个都不看它是否发出过包。
 */
public final class CultivatorLinkBroadcaster {
    /** 同一刻的连锁事件（放置 + 绑定变化）只发一包；10 刻 = 0.5 秒，人眼仍在「立即」区间。 */
    public static final int MIN_INTERVAL_TICKS = 10;

    /**
     * 范围重扫（36 号附录 A 第 8 项）：1 秒一次，覆盖「走进 / 走出范围」这条**无事件**的路径。
     * 比 {@code core_list} 的 100 刻对账密，因为线随玩家位置变。
     */
    public static final int RESCAN_INTERVAL_TICKS = 20;

    /** {@code UUID → 上一次真正发出的游戏刻}。 */
    private static final Map<UUID, Long> LAST_SENT_TICK = new HashMap<>();

    /** 待发集合：{@code request} / {@code rescan} 只往这里放，{@link #tick} 统一 flush。 */
    private static final Set<UUID> PENDING = new HashSet<>();

    /** {@code UUID → 上一次真正发出的列表指纹}，重扫对账用。 */
    private static final Map<UUID, Long> FINGERPRINTS = new HashMap<>();

    /**
     * 显式排序（陷阱 4）：{@code OutpostCoreIndex#owners()} 是 {@code HashMap} 的键集，
     * 绑定列表的顺序也不是可依赖的比较键。两次启动给出不同顺序 ⇒ 指纹抖动 ⇒ 每 20 刻重发一次包。
     * 排序键取两侧坐标的 {@code asLong()} + 类型码，与「线就是这两格之间的一段」同一语义。
     *
     * <p><b>F-304（54 号 §5.3 第 1 处）</b>：第三键 {@code kind().id()}。同一坐标不可能有两种 kind
     * （绑定列表里一条坐标只出现一次），补它是为了让比较器**全序**——不补就等于把「kind 不同、
     * 坐标相同」的两个元素交给排序算法自己决定先后，而它的稳定性是实现细节。
     */
    private static final Comparator<CultivatorLinkPayload.Link> ORDER = Comparator
            .comparingLong((CultivatorLinkPayload.Link link) -> link.core().asLong())
            .thenComparingLong(link -> link.cultivator().asLong())
            .thenComparingInt(link -> link.kind().id());

    private CultivatorLinkBroadcaster() {}

    /** 单个玩家请求一次（登录 / 换维度 / 重生）。 */
    public static void request(ServerPlayer player) {
        if (player != null) {
            PENDING.add(player.getUUID());
        }
    }

    /** 全部在线玩家各请求一次（绑定变化 / 核被拆：收件人集合由每人自己的组装结果决定）。 */
    public static void requestAll(MinecraftServer server) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PENDING.add(player.getUUID());
        }
    }

    /**
     * 登出清三条记录（照 {@code CoreListBroadcaster#onLogout}）。不清会让重连后的第一次请求
     * 被判成「刚发过」而跳过，客户端就停在上一局的线上（D-169 补充：不缓存上一次会话）。
     */
    public static void onLogout(UUID playerId) {
        LAST_SENT_TICK.remove(playerId);
        PENDING.remove(playerId);
        FINGERPRINTS.remove(playerId);
    }

    /** {@code ServerTickEvent.Post} 的唯一入口：每 20 刻重扫一次范围，每刻去抖 flush。 */
    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        if (now % RESCAN_INTERVAL_TICKS == 0) {
            rescan(server);
        }
        flush(server, now);
    }

    /** 「没有事件但结果变了」的路径（走进 / 走出范围）：只对指纹变了的玩家请求。 */
    private static void rescan(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            long fingerprint = fingerprint(assemble(server, player));
            Long previous = FINGERPRINTS.get(player.getUUID());
            if (previous == null || previous.longValue() != fingerprint) {
                PENDING.add(player.getUUID());
            }
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

    /** 唯一的发送点。 */
    private static void send(MinecraftServer server, ServerPlayer player, long now) {
        List<CultivatorLinkPayload.Link> links = assemble(server, player);
        long fingerprint = fingerprint(links);
        // 原版客户端 / 没装本模组的客户端不声明这条通道；NeoForge 的 NetworkRegistry#checkPacket 会对
        // 「向不支持该 channel 的连接发 optional payload」抛 UnsupportedOperationException 并崩服
        // （本文档-源码偏差表 D-1 的实测栈）。所以先问连接一声，跳过时不发、只记组装结果。
        boolean deliverable = player.connection.hasChannel(CultivatorLinkPayload.TYPE);
        LAST_SENT_TICK.put(player.getUUID(), now);
        FINGERPRINTS.put(player.getUUID(), fingerprint);
        if (deliverable) {
            PacketDistributor.sendToPlayer(player, new CultivatorLinkPayload(List.copyOf(links)));
            // 日志带**刻**：36 号 §8 #32 的「拆方块后立刻消失（不等 20 刻重扫）」与去抖判据（同刻只发 1 包）
            // 都只能靠刻级读数证明，秒级时间戳分不开 20 刻（1 秒）与下一刻。
            Farmstorm.LOGGER.info("连线下发：{} {} 条（维度 {}，刻 {}）",
                    player.getScoreboardName(), links.size(), player.level().dimension().location(), now);
        } else {
            Farmstorm.LOGGER.info("连线组装（客户端未声明 {}，跳过发送）：{} {} 条（刻 {}）",
                    CultivatorLinkPayload.TYPE.id(), player.getScoreboardName(), links.size(), now);
        }
    }

    /**
     * 服务端权威组装：遍历索引全部 owner → 己方判定 → 同维度 → **{@code inRange} 范围判定** →
     * 该核的绑定列表逐条展开 → 显式排序。
     *
     * <p>空结果**照样返回空列表**（不是「不发」）：调用方必须把它发出去，客户端才敢清残留
     * （35 号 §12 B-38 的原始症状是「远处的线一直挂着」）。
     *
     * <p>不截断：{@link CultivatorLinkPayload#MAX_LINKS} 只是解码上限（36 号 §2.8.1）。
     */
    private static List<CultivatorLinkPayload.Link> assemble(MinecraftServer server, ServerPlayer viewer) {
        List<CultivatorLinkPayload.Link> links = new ArrayList<>();
        if (!(viewer.level() instanceof ServerLevel level)) {
            return links;
        }
        OutpostCoreIndex index = OutpostCoreIndex.get(server);
        TeamResolver.Owner viewerOwner = new TeamResolver.Owner(viewer.getUUID(), viewer.getScoreboardName());
        ServerLevel teamLevel = server.overworld();               // 队伍表是全服一张，取哪个维度都一样
        ResourceLocation dimension = level.dimension().location();
        BlockPos viewerPos = viewer.blockPosition();
        for (UUID owner : index.owners()) {
            List<OutpostCoreIndex.Entry> entries = index.entriesOf(server, owner);
            if (entries.isEmpty()) {
                continue;
            }
            String ownerName = index.ownerName(owner);
            if (!Farmstorm.TEAMS.isAlly(teamLevel, new TeamResolver.Owner(owner, ownerName), viewerOwner)) {
                continue;                                         // 非己方看不见线（与 core_list / 边界粒子同一口径）
            }
            for (OutpostCoreIndex.Entry entry : entries) {
                if (!entry.dimension().equals(dimension)) {
                    continue;                                     // D-165 补充：跨维度的绑定不成立
                }
                // 范围判据**只准**是这一句（36 号 §2.8.2）——不写第二份 3 × 3 区块算式。
                if (!OutpostCoreBlockEntity.inRange(entry.pos(), viewerPos)) {
                    continue;
                }
                LevelChunk chunk = level.getChunkSource()
                        .getChunkNow(entry.pos().getX() >> 4, entry.pos().getZ() >> 4);
                if (chunk == null) {
                    continue;                                     // 区块不在内存：跳过，不触发加载
                }
                if (!(chunk.getBlockEntity(entry.pos()) instanceof OutpostCoreBlockEntity core)) {
                    continue;                                     // 索引里的条目已过期（方块没了）
                }
                // F-304（54 号 §5.3 第 3 处）：两条平行列表按**同一下标**配对读出。kind 的真相在核侧
                // 存档里（{@code CultivatorKinds}），绝不在这里按坐标反查方块类型（红线 3 / N-143）。
                List<BlockPos> machines = core.cultivators();
                List<CultivatorLinkPayload.Link.Kind> kinds = core.cultivatorKinds();
                int bound = Math.min(machines.size(), kinds.size());     // 同长由 F-303 保证；这里只是防御
                for (int slot = 0; slot < bound; slot++) {               // 同名 index 已被上面的核索引占用
                    links.add(new CultivatorLinkPayload.Link(entry.pos(), machines.get(slot), kinds.get(slot)));
                }
            }
        }
        links.sort(ORDER);
        return links;
    }

    /**
     * 列表指纹：由「已排序的两侧坐标 + 类型码」决定，与遍历顺序无关。
     *
     * <p><b>F-304（54 号 §5.3 第 2 处）</b>：{@code kind().id()} 必须进哈希 —— 不进的话「把已绑定的培养器
     * 拆掉、原位置换放一台转换器」在去抖看来完全没变化 ⇒ 那条线**永远青色**，而服务端看起来一切正常
     * （RT-75 / 陷阱 40）。
     */
    private static long fingerprint(List<CultivatorLinkPayload.Link> links) {
        long hash = 17L;
        for (CultivatorLinkPayload.Link link : links) {
            hash = hash * 31L + link.core().asLong();
            hash = hash * 31L + link.cultivator().asLong();
            hash = hash * 31L + link.kind().id();
        }
        return hash;
    }
}
