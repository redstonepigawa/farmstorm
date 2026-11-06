package com.dashpig.farmstorm.event;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.explosion.ExplosionQueue;
import com.dashpig.farmstorm.network.CoreListBroadcaster;
import com.dashpig.farmstorm.network.CultivatorLinkBroadcaster;
import com.dashpig.farmstorm.outpost.CoreChunkTickets;
import com.dashpig.farmstorm.trap.TrapImmunity;
import com.dashpig.farmstorm.util.FarmstormTags;
import com.dashpig.farmstorm.util.TempEnchantHandler;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * game 总线事件（TECH §2 / §4.10 / F-15 ~ F-17）。
 * 只做九件事，一条对应 {@link #register} 里的一行监听器注册，顺序同 {@code register}：
 * ① {@code onPlayerTick}——每 20 tick 的临时附魔自愈与心跳日志；
 * ② {@code onPlayerLoggedIn}——登录时对账，账本里还留着记录就立刻还原创造模式；
 * ③ {@code onPlayerLoggedOut}——登出时还原创造模式，并清掉陷阱免疫与两路广播的登出记录；
 * ④ {@code onPlayerChangedDimension}——换维度时请两路广播各重发一次（F-200 第 7 条）；
 * ⑤ {@code onPlayerRespawn}——重生时同上（F-200 第 8 条）；
 * ⑥ {@code onExplosionDetonate}——爆炸免疫方块拦截：从 {@code getAffectedBlocks()} 里摘掉它们（D-14 / RT-08）；
 * ⑦ {@code onLevelTickPost}——爆炸队列的每 tick 结算点（{@code ExplosionQueue.drain}）；
 * ⑧ {@code onServerTickPost}——一次性启动闸（{@code CoreChunkTickets.restoreAll}）加两路广播的每刻 tick；
 * ⑨ {@code onServerStopping}——停服释放区块票并复位闸。
 * F-157 / F-158 / F-194 的耕地相关监听器已在 v0.2.4 随耕地包一并删除。
 * 服务端 {@code /farmstorm} 指令树已在 F-197 整棵删除（D-140）；客户端 {@code /farmstorm hud} 是另一条注册，不在这里。
 * 不注册任何方块实体 tick（F-15 ③），也不在客户端进入任何判定路径（HC-03）。
 */
public final class FarmstormGameEvents {
    /** 临时附魔自愈与心跳的间隔（刻）。 */
    private static final int TICK_INTERVAL = 20;

    /**
     * 心跳日志开关。刻意不进 config：117 个键由 HC-01 锁死，而这是调试输出而不是玩法数值。
     * 打开方式：JVM 参数 {@code -Dfarmstorm.debugHeartbeat=true}，或运行期调用 {@link #setHeartbeatLogging(boolean)}。
     */
    private static boolean heartbeatLogging = Boolean.getBoolean("farmstorm.debugHeartbeat");

    /**
     * 创造模式回滚账本（D-18 / RT-05 ①）：只放内存，服务器重启自然清零，绝不写存档。
     * 值与 {@link GameType} 是「该玩家本该处于的模式」，由 M4 的幸运浆果创造模式分支通过
     * {@link #trackCreativeMode(ServerPlayer, GameType)} 登记。
     */
    private static final Map<UUID, GameType> CREATIVE_RESTORE = new ConcurrentHashMap<>();

    /** F-190 / F-198 的一次性启动闸：每个服务端实例的首个 tick 跑一次；v0.2.4 起闸内只剩 {@code CoreChunkTickets.restoreAll} 一件事。 */
    private static boolean startupDone;

    private FarmstormGameEvents() {}

    /** 由 {@code Farmstorm} 构造器在 mod 总线上注册到 {@code NeoForge.EVENT_BUS}。 */
    public static void register(IEventBus gameBus) {
        gameBus.addListener(FarmstormGameEvents::onPlayerTick);
        gameBus.addListener(FarmstormGameEvents::onPlayerLoggedIn);
        gameBus.addListener(FarmstormGameEvents::onPlayerLoggedOut);
        // F-200 / 30 号 §2.1.4 的第 7、8 条：换维度与重生各请求一次（列表内容不变，发它是为了覆盖
        // 「客户端在世界切换时丢过一次」的极小窗口，成本 1 包）。
        gameBus.addListener(FarmstormGameEvents::onPlayerChangedDimension);
        gameBus.addListener(FarmstormGameEvents::onPlayerRespawn);
        gameBus.addListener(FarmstormGameEvents::onExplosionDetonate);
        gameBus.addListener(FarmstormGameEvents::onLevelTickPost);
        gameBus.addListener(FarmstormGameEvents::onServerTickPost);
        gameBus.addListener(FarmstormGameEvents::onServerStopping);
    }

    /** 每 20 tick：临时附魔自愈（光环到期撤销也走这条）+ 可开关的心跳日志。 */
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % TICK_INTERVAL != 0) {
            return;
        }
        TempEnchantHandler.tick(player);
        if (heartbeatLogging) {
            Farmstorm.LOGGER.info("心跳：{}（{}）gameTime={} 维度={}", player.getGameProfile().getName(),
                    player.getUUID(), player.level().getGameTime(), player.level().dimension().location());
        }
    }

    /** 登录对账（D-18 / RT-05 ③）：账本里还留着记录（同一次运行内重连）就立刻还原，避免创造模式永久化。 */
    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Farmstorm.LOGGER.info("玩家登录：{}（{}）", player.getGameProfile().getName(), player.getUUID());
        restoreCreativeMode(player);
        // F-200 / 30 号 §2.1.4 的第 1、2、3 条：登录 / 重连 / 首次加入共用这一条路径（服务端不区分三者）。
        // 列表为空也请求一次（total=0），让客户端把「没核」与「没收到包」区分开；客户端此时尚未就绪由第 10 条的对账兜底。
        CoreListBroadcaster.request(player);
        // F-239 / 36 号 §2.8.2 时机 #5（登录 / 重连 / 首次加入）：连线由范围重扫兜底，
        // 这里再请求一次是为了让「刚登录就站在核旁」的那一帧不用等最多 20 刻。
        CultivatorLinkBroadcaster.request(player);
    }

    /** 下线即还原（D-18 / RT-05 ②）：不给「强退保留创造模式」留窗口。 */
    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Farmstorm.LOGGER.info("玩家登出：{}（{}）", player.getGameProfile().getName(), player.getUUID());
        restoreCreativeMode(player);
        TrapImmunity.clear(player.getUUID());             // D-112 第 4 条：重连后陷阱免疫清零
        CoreListBroadcaster.onLogout(player.getUUID());   // F-200：清 lastSentTick / pending / fingerprint 三条记录
        CultivatorLinkBroadcaster.onLogout(player.getUUID());   // F-239 时机 #5：三条记录同口径清掉
    }

    /** F-200 / 30 号 §2.1.4 第 7 条：换维度请求一次。 */
    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CoreListBroadcaster.request(player);
            CultivatorLinkBroadcaster.request(player);    // F-239 时机 #5：换维度（范围与维度都变了）
        }
    }

    /** F-200 / 30 号 §2.1.4 第 8 条：重生请求一次。 */
    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CoreListBroadcaster.request(player);
            CultivatorLinkBroadcaster.request(player);    // F-239 时机 #5：重生（位置变了）
        }
    }

    /**
     * 爆炸免疫拦截（D-14 / RT-08）：任何爆炸（含原版 TNT 与其它模组）都不许破坏
     * {@code #farmstorm:explosion_immune} 里的方块。只拦自己的爆炸是不够的，所以在这里从
     * {@code getAffectedBlocks()} 里摘掉它们。
     */
    private static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        List<BlockPos> affected = event.getAffectedBlocks();
        if (affected.isEmpty()) {
            return;
        }
        int before = affected.size();
        affected.removeIf(pos -> event.getLevel().getBlockState(pos).is(FarmstormTags.EXPLOSION_IMMUNE));
        int removed = before - affected.size();
        if (removed > 0) {
            Farmstorm.LOGGER.info("爆炸拦截：从受影响方块中移除 {} 个 #farmstorm:explosion_immune 方块", removed);
        }
    }

    /** 爆炸批处理结算点（TECH §8.2）：本 tick 超量的爆炸在这里逐个执行。 */
    private static void onLevelTickPost(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ExplosionQueue.drain(level);
        }
    }

    /**
     * 一次性启动闸（F-190 / F-198）：放在首个 {@code ServerTickEvent.Post} 而不是构造器里，
     * 因为构造器阶段 {@link MinecraftServer} 还没就绪。
     *
     * <p>v0.2.4 起闸内只剩 {@code CoreChunkTickets.restoreAll} 一件事——原「先补票、后补绑定」的
     * 先后约束随耕地撤销一起失效（绑定与补建均已删），这里只保留补票本身。
     */
    private static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!startupDone) {
            startupDone = true;
            CoreChunkTickets.restoreAll(server);
        }
        // F-200 / 30 号 §2.1.4 第 10 条：去抖 flush + 每 100 刻的指纹对账。**每刻都要走**，
        // 不能再让它挂在启动闸后面（闸只跑一次，那样包永远发不出去）。
        CoreListBroadcaster.tick(server);
        // F-239 / 36 号 §2.8.2：20 刻范围重扫 + 10 刻去抖 flush 的唯一入口（**每刻都要走**，同一条理由）。
        CultivatorLinkBroadcaster.tick(server);
    }

    /** 停服：票不落盘，显式释放一遍；闸复位，让同一个 JVM 里换存档 / 重开服时启动闸照常再跑一次。 */
    private static void onServerStopping(ServerStoppingEvent event) {
        CoreChunkTickets.releaseAll(event.getServer());
        startupDone = false;
    }

    /**
     * 登记「该玩家应被还原为 original」（D-18）。M4 的幸运浆果创造模式分支在切换到创造模式前调用，
     * 之后的还原由登录 / 登出事件与它自己的计时器共同兜底。
     */
    public static void trackCreativeMode(ServerPlayer player, GameType original) {
        CREATIVE_RESTORE.put(player.getUUID(), original);
    }

    /**
     * 还原并销账（D-18 / RT-05 ②③）。登录、登出与计时到期三条路径共用同一份账本。
     *
     * @return 是否真的改回了模式。被其它模组的 {@code PlayerChangeGameModeEvent} 取消时返回 false 且**保留**记录，
     *         下一次登录 / 登出会重试，不留「记录丢了但人还在创造模式」的漏网
     */
    public static boolean restoreCreativeMode(ServerPlayer player) {
        GameType original = CREATIVE_RESTORE.get(player.getUUID());
        if (original == null || original == GameType.CREATIVE) {
            CREATIVE_RESTORE.remove(player.getUUID());
            return false;
        }
        if (player.gameMode.getGameModeForPlayer() == original) {
            CREATIVE_RESTORE.remove(player.getUUID());
            return false;
        }
        if (!player.setGameMode(original)) {
            return false;
        }
        CREATIVE_RESTORE.remove(player.getUUID());
        Farmstorm.LOGGER.info("创造模式已还原：{}（{}）→ {}", player.getGameProfile().getName(), player.getUUID(), original);
        return true;
    }

    /** 是否有待还原的创造模式记录（给 M4 的计时器与回归验证用）。 */
    public static boolean hasPendingCreativeRestore(UUID playerId) {
        return CREATIVE_RESTORE.containsKey(playerId);
    }

    /** 心跳日志开关（F-15 验收项 ①）。 */
    public static void setHeartbeatLogging(boolean enabled) {
        heartbeatLogging = enabled;
    }

    /** 当前心跳日志开关状态。 */
    public static boolean heartbeatLogging() {
        return heartbeatLogging;
    }

}
