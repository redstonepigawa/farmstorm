package com.dashpig.farmstorm.event;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.command.FarmstormCommands;
import com.dashpig.farmstorm.explosion.ExplosionQueue;
import com.dashpig.farmstorm.farmland.FarmlandGuard;
import com.dashpig.farmstorm.farmland.FarmlandLedger;
import com.dashpig.farmstorm.trap.TrapImmunity;
import com.dashpig.farmstorm.util.FarmstormTags;
import com.dashpig.farmstorm.util.TempEnchantHandler;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * game 总线事件（TECH §2 / §4.10 / F-15 ~ F-17）。
 * 只做七件事：玩家心跳与临时附魔自愈、登录 / 登出的创造模式对账、爆炸批处理、爆炸免疫拦截、
 * {@code /farmstorm} 指令树的注册（F-160）、锄地拦截（F-157）、耕地踩踏保护（F-158）。
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

    private FarmstormGameEvents() {}

    /** 由 {@code Farmstorm} 构造器在 mod 总线上注册到 {@code NeoForge.EVENT_BUS}。 */
    public static void register(IEventBus gameBus) {
        gameBus.addListener(FarmstormGameEvents::onPlayerTick);
        gameBus.addListener(FarmstormGameEvents::onPlayerLoggedIn);
        gameBus.addListener(FarmstormGameEvents::onPlayerLoggedOut);
        gameBus.addListener(FarmstormGameEvents::onExplosionDetonate);
        gameBus.addListener(FarmstormGameEvents::onLevelTickPost);
        gameBus.addListener(FarmstormGameEvents::onRegisterCommands);
        gameBus.addListener(FarmstormGameEvents::onRightClickBlock);
        gameBus.addListener(FarmstormGameEvents::onFarmlandTrample);
    }

    /**
     * {@code /farmstorm farmlandlimit <value>} 指令树（F-160 / TECH §7.1）。
     * Brigadier 指令树是运行期注册，不进任何 registry（TECH §2.3）；指令只在服务端注册。
     */
    private static void onRegisterCommands(RegisterCommandsEvent event) {
        FarmstormCommands.register(event.getDispatcher());
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
    }

    /** 下线即还原（D-18 / RT-05 ②）：不给「强退保留创造模式」留窗口。 */
    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Farmstorm.LOGGER.info("玩家登出：{}（{}）", player.getGameProfile().getName(), player.getUUID());
        restoreCreativeMode(player);
        TrapImmunity.clear(player.getUUID());             // D-112 第 4 条：重连后陷阱免疫清零
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

    /**
     * 锄地拦截（F-157 / 24 号 §6.3）：顺序固定「先范围、后额度」，两种拒绝各发一条动作栏提示。
     *
     * <p>取消点选在 {@code RightClickBlock}：服务端在 {@code ServerPlayerGameMode#useItemOn} 抛本事件，
     * 取消后该方法立刻返回，早于 {@code ItemStack#useOn}——改方块与 {@code hurtAndBreak} 都在那之后，
     * 所以「取消之后既不产生耕地也不扣耐久」由调用顺序保证，不靠猜（反编译路径见证据 F-23B1 第 3 条）。
     *
     * <p>只有服务端判定（红线第 3 条）。客户端保留原版预测：服务端 {@code Level#playSound(Player, ...)}
     * 把发声音的玩家本人排除在外，客户端若也不预测，成功锄地时锄头就没有音效。
     */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (!FarmlandGuard.isTillAction(level, pos, event.getItemStack())) {
            return;
        }
        FarmlandLedger ledger = FarmlandLedger.of(level);
        FarmlandGuard.Verdict verdict = FarmlandGuard.check(level, player, pos);
        if (verdict == FarmlandGuard.Verdict.ALLOW) {
            // 同一刻记账：紧接着的下一次锄地读到的 used 已含这一块（TECH §6.3 第 4 步）
            ledger.add(level, player, pos);
            return;
        }
        event.setCanceled(true);
        // 1.21 用 TriState 取代了原先的 Event.Result：DENY 对应 TriState.FALSE（24 号 §6.3 的待验证 ⑥）
        event.setUseBlock(TriState.FALSE);
        player.displayClientMessage(verdict == FarmlandGuard.Verdict.NO_CORE
                ? Component.translatable(FarmlandGuard.MESSAGE_NO_CORE)
                : Component.translatable(FarmlandGuard.MESSAGE_QUOTA_FULL,
                        ledger.used(level, player), ledger.capacity(level, player)),
                true);
    }

    /**
     * 耕地踩踏保护（F-158 / D-120 第 3 项）：取消即不产生泥土。
     *
     * <p>这条事件可取消：{@code BlockEvent.FarmlandTrampleEvent} 实现 {@code ICancellableEvent}，
     * 由 {@code CommonHooks#onFarmlandTrample} 在 {@code FarmBlock#fallOn} 的 {@code !level.isClientSide}
     * 分支里抛出；取消后该处返回 false，{@code turnToDirt} 不再执行。因此不必走 TECH §6.4 的
     * 「40 刻对账 + 恢复」兜底，也没有「踩坏的那一瞬闪一帧泥土」的代价。
     *
     * <p>失水退化不在这条链上（{@code FarmBlock#randomTick} 直接调 {@code turnToDirt}），范围内的耕地照常退化。
     */
    private static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof ServerLevel level && FarmlandGuard.protectsFromTrample(level, event.getPos())) {
            event.setCanceled(true);
        }
    }
}
