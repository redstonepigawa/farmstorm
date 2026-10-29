package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/**
 * F-80b（作者口径，恢复 BRIEF §7.6 原意）：<b>吸附判定成功即必中</b>。
 * <p>1.21.1 没有 {@code ServerboundSwingPacket} 的公开事件，D-40 / D-50 又分别禁 Mixin 与自订包，
 * 所以这里用「服务端可观测的双信号」感知一次吸附挥击：
 * <ol>
 *   <li><b>swing 跃变</b>：{@code LivingEntity.swinging} 由假变真，或 {@code swingTime} 归零/回退；</li>
 *   <li><b>冲刺位移</b>：本刻水平位移 ≥ {@link #DASH_SPEED}（客户端冲刺约 0.93 格/刻，正常疾跑约 0.36）。</li>
 * </ol>
 * 两个信号同时成立才结算——只用信号 1 会把右键使用 / 挖掘 / 放置也算进来，只用信号 2 会把爆炸击退、末影珍珠算进来。
 * <p>判定仍然全走 {@link WildHunterCombat#resolveTarget}（5 格球 + 30° 锥），没有放宽。
 */
public final class WildHunterSwingSettler {
    /** 冲刺位移阈值（格/刻）：客户端预测每刻约 0.93，正常疾跑约 0.36。 */
    static final double DASH_SPEED = 0.55;
    /** 去抖窗口（刻）：同一次挥击在真命中后不再重复结算。 */
    private static final long DEDUPE_TICKS = 1L;
    /** swing 跃变与冲刺位移允许的最大间隔（刻）：实测位移常晚 1–2 刻到达。 */
    private static final long SWING_WINDOW = 2L;

    private static final Map<UUID, State> STATES = new Object2ObjectOpenHashMap<>();

    private WildHunterSwingSettler() {}

    /** 每次 {@code AttackEntityEvent}（真命中）都会打点，用于去重。 */
    public static void recordDirectHit(ServerPlayer player) {
        state(player).lastDirectHit = player.level().getGameTime();
    }

    /** 每刻一次（{@code PlayerTickEvent.Post}）。 */
    public static void tick(ServerPlayer player) {
        State state = state(player);
        boolean swinging = player.swinging;
        boolean newSwing = swinging && (!state.prevSwinging || player.swingTime <= state.prevSwingTime);
        double speed = Math.hypot(player.getX() - state.lastX, player.getZ() - state.lastZ);
        long now = player.level().getGameTime();
        if (newSwing) {
            state.swingTick = now;
        }
        // 客户端的挥击包与位移包不一定同 tick 到达（实测位移常晚 1–2 刻），
        // 所以「swing 跃变」与「冲刺位移」允许在 SWING_WINDOW 刻的窗口内配对。
        boolean swingWithinWindow = now - state.swingTick <= SWING_WINDOW;

        if (swingWithinWindow && speed >= DASH_SPEED
                && player.hasEffect(FarmstormMobEffects.WILD_HUNTER)
                && !player.hasEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER)
                && now - state.lastDirectHit > DEDUPE_TICKS
                && now - state.lastSettle > DEDUPE_TICKS) {
            state.lastSettle = now;
            Farmstorm.LOGGER.info("狂野猎人 吸附挥击 速度 {}", String.format(java.util.Locale.ROOT, "%.2f", speed));
            WildHunterCombat.settleSwing(player, null);
        }

        state.prevSwinging = swinging;
        state.prevSwingTime = player.swingTime;
        state.lastX = player.getX();
        state.lastZ = player.getZ();
    }

    private static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.getUUID(), uuid -> new State(player.getX(), player.getZ()));
    }

    private static final class State {
        private double lastX;
        private double lastZ;
        private boolean prevSwinging;
        private int prevSwingTime;
        private long lastDirectHit = Long.MIN_VALUE / 2;
        private long lastSettle = Long.MIN_VALUE / 2;
        private long swingTick = Long.MIN_VALUE / 2;

        private State(double x, double z) {
            this.lastX = x;
            this.lastZ = z;
        }
    }
}
