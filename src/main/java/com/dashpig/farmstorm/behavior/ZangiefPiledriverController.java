package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.effect.ZangiefPiledriverEffect;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 桑吉尔蔗的三相位状态机（TECH §3.11 / §5.2）：触发之后的一切时序都归它。
 * <p>挂在 {@link PlayerTickEvent.Post} 上而不是挂在方块上——陷阱一触发就自毁（D-72），
 * 而相位要走 14 + 20 + 落地共约 74 刻。
 * <p>相位的唯一载体是 {@code farmstorm:zangief_piledriver} 的 amplifier（D-50）：
 * 服务端改 amplifier = 客户端就知道现在是哪一段，不需要任何自定义包。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class ZangiefPiledriverController {
    /** 安全阀：相位 2 最长持续刻数，兜「落地判定失效」的底（卡在方块缝里）。不进 config（HC-13 同类）。 */
    public static final int MAX_FALL_TICKS = 100;

    /** 伺服速度上限（本文补充）：剩余刻数为 1、剩余高度 20 格时不给瞬移般的速度。 */
    private static final double MAX_SERVO_SPEED = 3.0;

    /** 落地判定从相位 2 的第 3 刻起生效：进入相位 2 那一 tick 的 {@code onGround()} 还停留在漂浮前的旧值。 */
    private static final int LANDING_CHECK_AFTER = 2;

    /**
     * 相位 0 / 1 的效果时长比相位本身多 1 刻（实测订正）：效果施加当刻就会被 {@code LivingEntity#tickEffects}
     * 扣 1，所以 duration = 相位时长 时，效果只会覆盖「相位时长 − 1」刻，末尾那一 tick 出现「效果已没、
     * 相位还没推进」的空窗——客户端会把它渲染成一次相机/输入全复位再上锁的闪烁（RT-15 说的正是这个）。
     * 多留 1 刻之后，相位推进按 elapsed 精确卡点，空窗消失；客户端读到的 duration 仍是「相位时长 ± 1」。
     */
    private static final int PHASE_EFFECT_CUSHION = 1;

    /** 只存内存：UUID → 相位状态。服务器重启即清空，不写存档（D-18 的先例）。 */
    private static final Map<UUID, PhaseState> ACTIVE = new HashMap<>();

    /**
     * 被我们 {@code setNoGravity(true)} 过的玩家（相位 2 期间）。它比 {@link #ACTIVE} 多活一步：
     * 相位记录可能先被移除（死亡 / 外力清除 / 换维度），而 <b>noGravity 漏恢复 = 玩家永久悬空</b>，
     * 所以每 tick 拿它做兜底扫尾：只要集合里有人已经不在 {@code ACTIVE}，就立刻把重力还给他。
     */
    private static final java.util.Set<UUID> GRAVITY_OFF = new java.util.HashSet<>();

    private ZangiefPiledriverController() {}

    /** 一个玩家当前所在相位；{@code enteredAt} 是该相位的起始 gameTime。 */
    private static final class PhaseState {
        private int phase;
        private long enteredAt;
        private boolean servoClampWarned;

        /** 相位 2 的目标地面高度：进入相位 2 那一 tick 量一次就钉住，之后不再随漂移重新解析。 */
        private double targetGroundY = Double.NaN;

        private PhaseState(int phase, long enteredAt) {
            this.phase = phase;
            this.enteredAt = enteredAt;
        }
    }

    /** 相位 0 落地：由 {@link com.dashpig.farmstorm.behavior.impl.ZangiefCaneTrap#trigger} 调用。 */
    public static void begin(ServerPlayer player) {
        if (ACTIVE.containsKey(player.getUUID())) {
            return;                                       // 重复触发不叠加、不重置（§5.3）
        }
        setPhase(player, ZangiefPiledriverEffect.PHASE_REVERSED,
                FarmstormConfig.zangiefCaneStageDelayTicks.get() + PHASE_EFFECT_CUSHION);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;                                       // 只在服务端跑（HC-03）
        }
        sweepGravity(player);
        PhaseState state = ACTIVE.get(player.getUUID());
        if (state == null) {
            return;
        }
        long elapsed = player.level().getGameTime() - state.enteredAt;
        switch (state.phase) {
            case ZangiefPiledriverEffect.PHASE_REVERSED -> {
                if (phaseElapsed(player, elapsed, FarmstormConfig.zangiefCaneStageDelayTicks.get())) {
                    enterLevitating(player);
                }
            }
            case ZangiefPiledriverEffect.PHASE_LEVITATING -> {
                if (phaseElapsed(player, elapsed, FarmstormConfig.zangiefCaneLevitationDurationTicks.get())) {
                    setPhase(player, ZangiefPiledriverEffect.PHASE_FALLING, MAX_FALL_TICKS);
                    // 目标地面只在这里量一次：玩家在漂浮段会横向漂移，若每 tick 重新解析「脚下地面」，
                    // 站在 1×1 柱顶时会一会儿量到柱顶（h≈5）、一会儿量到远处台面（h≈9），伺服速度忽快忽慢，
                    // 四格高台的落地耗时因此掉到 23 刻（这是实测抓到的偏差）。
                    PhaseState falling = ACTIVE.get(player.getUUID());
                    if (falling != null) {
                        falling.targetGroundY = groundYBelow(player);
                    }
                    // 相位 2 由我们全权钉速度：原版重力会在 setDeltaMovement 之后再叠一次 -0.08，
                    // 让实际下落比 h/n 快约 1.6 倍（四格高台实测 0.226 格/刻 vs 伺服 0.13）。
                    // 关掉重力比「每刻对削」干净，也不受高度与浮点差异影响。
                    player.setNoGravity(true);
                    GRAVITY_OFF.add(player.getUUID());
                }
            }
            case ZangiefPiledriverEffect.PHASE_FALLING -> tickFalling(player, elapsed);
            default -> {
                Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 相位号非法 %d，强制收尾", state.phase));
                finish(player);
            }
        }
    }

    /**
     * 相位 0 / 1 是不是该结束了：{@code elapsed} 到相位时长就推进（效果多留 1 刻的余量见 {@link #PHASE_EFFECT_CUSHION}）。
     * <p>效果提前消失要分两种：已在相位末尾附近 ⇒ 当正常到期、照常推进；否则就是被第三方「清除负面效果」清掉了 ⇒
     * §5.6 的自检：打 warn 直接收尾，不重新施加（重新施加会与外部清理逻辑对着干）。
     */
    private static boolean phaseElapsed(ServerPlayer player, long elapsed, int phaseLength) {
        if (elapsed >= phaseLength) {
            return true;
        }
        if (player.getEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER) != null) {
            return false;                                // 效果还在 ⇒ 相位没走完，继续等
        }
        if (elapsed >= Math.max(1, phaseLength - 2)) {
            return true;                                 // 已在相位末尾附近：当正常到期处理
        }
        Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 状态复位（效果被外部清除）玩家 %s",
                player.getGameProfile().getName()));
        finish(player);
        return false;
    }

    /** 相位 2 的对称自检：效果被外力清掉就收尾（相位 2 的时长是兜底值，正常落地走 {@code onGround}）。 */
    private static void requireEffect(ServerPlayer player) {
        if (player.getEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER) == null) {
            Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 状态复位（效果被外部清除）玩家 %s",
                    player.getGameProfile().getName()));
            finish(player);
        }
    }

    /** 把重力还给玩家（幂等）：四条退出路径都调它，另有 {@link #sweepGravity} 兜底。 */
    private static void releaseGravity(net.minecraft.world.entity.Entity entity) {
        GRAVITY_OFF.remove(entity.getUUID());
        if (entity.isNoGravity()) {
            entity.setNoGravity(false);
        }
    }

    /**
     * 兜底扫尾（每 tick，常数开销）：{@code GRAVITY_OFF} 里有人已经不在 {@link #ACTIVE}，说明某条退出路径
     * 把相位记录先摘掉了、重力却还没还——立刻还，并打一行 warn 留痕。
     */
    private static void sweepGravity(ServerPlayer player) {
        UUID id = player.getUUID();
        if (!GRAVITY_OFF.contains(id)) {
            return;
        }
        if (ACTIVE.containsKey(id)) {
            return;
        }
        Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 重力已恢复（兜底）玩家 %s",
                player.getGameProfile().getName()));
        releaseGravity(player);
    }

    /** 相位 1：漂浮 VII 共 20 刻（D-69）。amplifier = 等级 − 1。 */
    private static void enterLevitating(ServerPlayer player) {
        int duration = FarmstormConfig.zangiefCaneLevitationDurationTicks.get();
        int level = Math.max(1, FarmstormConfig.zangiefCaneLevitationLevel.get());
        setPhase(player, ZangiefPiledriverEffect.PHASE_LEVITATING, duration + PHASE_EFFECT_CUSHION);
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, level - 1));
    }

    /**
     * 相位 2：逐刻伺服（裁决 A-03）——向下速度 = 剩余高度 ÷ 剩余刻数。
     * 定速方案做不到「不同起始高度都落在 fallTicks ± 2 刻内」，因为漂浮把玩家抬多高每局都不一样。
     */
    private static void tickFalling(ServerPlayer player, long elapsed) {
        int fallTicks = Math.max(1, FarmstormConfig.zangiefCaneFallTicks.get());
        if (elapsed >= LANDING_CHECK_AFTER && player.onGround()) {
            Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 落地耗时 %d 刻（目标 %d）", elapsed, fallTicks));
            finish(player);
            return;
        }
        if (elapsed >= MAX_FALL_TICKS) {
            Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 相位 2 超时（玩家 %s 未落地）",
                    player.getGameProfile().getName()));
            finish(player);
            return;
        }
        requireEffect(player);
        if (!ACTIVE.containsKey(player.getUUID())) {
            return;                                   // requireEffect 已经收尾
        }
        PhaseState state = ACTIVE.get(player.getUUID());
        double groundY = state == null || Double.isNaN(state.targetGroundY) ? groundYBelow(player) : state.targetGroundY;
        double height = Math.max(0.0, player.getY() - groundY);
        int remaining = (int) Math.max(1, fallTicks - elapsed);
        double servo = height / remaining;
        if (servo > MAX_SERVO_SPEED && !ACTIVE.get(player.getUUID()).servoClampWarned) {
            ACTIVE.get(player.getUUID()).servoClampWarned = true;
            Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 伺服触顶 h=%.2f n=%d v=%.2f", height, remaining, servo));
        }
        player.setDeltaMovement(0.0, -Math.min(servo, MAX_SERVO_SPEED), 0.0);
        player.hurtMarked = true;                         // 让 ServerEntity 广播 ClientboundSetEntityMotionPacket
        player.connection.send(new ClientboundSetEntityMotionPacket(player));   // 单人时没有旁观者，显式发给本人
    }

    /** 玩家脚下最近的地面顶面高度；找不到则退到世界最低处。 */
    private static double groundYBelow(ServerPlayer player) {
        Level level = player.level();
        BlockPos.MutableBlockPos cursor = player.blockPosition().mutable();
        int minY = level.getMinBuildHeight();
        for (int y = cursor.getY(); y > minY; y--) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            VoxelShape shape = state.getCollisionShape(level, cursor);
            if (!shape.isEmpty()) {
                return y + shape.max(Direction.Axis.Y);
            }
        }
        return minY;
    }

    /** 相位推进的唯一写法：先摘再加的硬切换（§3.11），因为 {@code MobEffectInstance#update} 不保证替换 amplifier。 */
    private static void setPhase(ServerPlayer player, int phase, int durationTicks) {
        player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
        player.addEffect(new MobEffectInstance(FarmstormMobEffects.ZANGIEF_PILEDRIVER,
                durationTicks, phase, false, false, false));
        ACTIVE.put(player.getUUID(), new PhaseState(phase, player.level().getGameTime()));
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 相位 %d t=%d 玩家 %s duration=%d", phase,
                player.level().getGameTime(), player.getGameProfile().getName(), durationTicks));
    }

    /** 收尾（幂等）：摘效果、摘漂浮、记账清空。 */
    public static void finish(ServerPlayer player) {
        releaseGravity(player);                          // 退出路径①：落地 / 超时 / 外力清除（先还重力，再管相位表）
        if (ACTIVE.remove(player.getUUID()) == null) {
            return;
        }
        player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
        player.removeEffect(MobEffects.LEVITATION);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 结束 t=%d 玩家 %s",
                player.level().getGameTime(), player.getGameProfile().getName()));
    }

    /** 注销即清账：{@code ACTIVE} 是内存表，不清会残留到下一次登录（§5.3）。 */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
        releaseGravity(event.getEntity());                // 退出路径②：掉线（否则 noGravity 会跟着存档过去）
    }

    /** 退出路径③：换维度（同一份玩家实体，相位表按 UUID 记，不恢复就会带着 noGravity 走）。 */
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
        releaseGravity(event.getEntity());
    }

    /** 死亡即清账，并顺手摘掉效果与速度，避免重生后带着半截状态（F-90 验收 4）。 */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (ACTIVE.remove(player.getUUID()) == null) {
            return;
        }
        releaseGravity(player);                           // 退出路径④：死亡
        player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
        player.removeEffect(MobEffects.LEVITATION);
        player.setDeltaMovement(Vec3.ZERO);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 结束（死亡）玩家 %s", player.getGameProfile().getName()));
    }
}
