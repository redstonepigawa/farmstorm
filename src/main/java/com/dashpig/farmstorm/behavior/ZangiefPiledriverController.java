package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.effect.ZangiefPiledriverEffect;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 桑吉尔蔗的三相位状态机（作者 v0.0.2 实机口径，取代 TECH §3.11 的漂浮版）。
 * <p>挂在 {@link PlayerTickEvent.Post} 上而不是挂在方块上——陷阱一触发就自毁（D-72），而相位要走
 * 「等待 + 升高 + 自由落体」共约 30~60 刻。
 * <p>相位的唯一载体是 {@code farmstorm:zangief_piledriver} 的 amplifier（D-50）：
 * 服务端改 amplifier = 客户端就知道现在是哪一段，不需要任何自定义包。
 *
 * <p><b>三相位（作者实测改版）</b>：
 * <ol>
 *   <li>相位 0 {@code PHASE_REVERSED}：已倒转、等待升高，时长 {@code stageDelayTicks}，不动位移；</li>
 *   <li>相位 1 {@code PHASE_LEVITATING}：**伺服向上抬 {@code riseHeightBlocks} 格**，用 {@code riseTicks} 刻抬完
 *       （默认 15 格 / 15 刻 ≈ 1 格/刻），期间关重力以免原版重力把上升吃掉；</li>
 *   <li>相位 2 {@code PHASE_FALLING}：**不再钉速度**，重力自然接管（作者要「痛感」，因此坠落伤害照常结算），
 *       只做落地检测，{@link #MAX_FALL_TICKS} 兜底。</li>
 * </ol>
 * <p>枚举常量名沿用 {@code PHASE_LEVITATING}：amplifier 的取值（0/1/2）是对客户端的协议，不能跟着语义改名。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class ZangiefPiledriverController {
    /** 安全阀：相位 2 最长持续刻数，兜「落地判定失效」的底（卡在方块缝里）。不进 config（HC-13 同类）。 */
    public static final int MAX_FALL_TICKS = 100;

    /**
     * 上升速度上限（格/刻）：默认 16 格 / 15 刻 ≈ 1.07，离上限很远，行为与不设限一致。
     * <p>从 1.5 放宽到 3.0 的原因：1.5 会把 {@code riseTicks} 夹死——实测 {@code riseTicks = 5}（需要 3 格/刻）
     * 只抬到 6.00 格，键名「抬完所用刻数」就名不副实了。3.0 仍能兜住「剩余刻数异常 ⇒ 荒谬速度」的退化情形。
     */
    private static final double MAX_RISE_SPEED = 4.0;

    /** 落地判定从相位 2 的第 3 刻起生效：进入相位 2 那一 tick 的 {@code onGround()} 还停留在升高前的旧值。 */
    private static final int LANDING_CHECK_AFTER = 2;

    /**
     * 相位 0 / 1 的效果时长比相位本身多 1 刻（实测订正）：效果施加当刻就会被 {@code LivingEntity#tickEffects}
     * 扣 1，所以 duration = 相位时长 时，效果只会覆盖「相位时长 − 1」刻，末尾那一 tick 出现「效果已没、
     * 相位还没推进」的空窗——客户端会把它渲染成一次相机/输入全复位再上锁的闪烁（RT-15 说的正是这个）。
     */
    private static final int PHASE_EFFECT_CUSHION = 1;

    /** 只存内存：UUID → 相位状态。服务器重启即清空，不写存档（D-18 的先例）。 */
    private static final Map<UUID, PhaseState> ACTIVE = new HashMap<>();

    /**
     * 被我们 {@code setNoGravity(true)} 过的玩家（相位 1 上升期间）。它比 {@link #ACTIVE} 多活一步：
     * 相位记录可能先被移除（死亡 / 外力清除 / 换维度），而 <b>noGravity 漏恢复 = 玩家永久悬空</b>，
     * 所以每 tick 拿它做兜底扫尾：只要集合里有人已经不在 {@code ACTIVE}，就立刻把重力还给他。
     */
    private static final Set<UUID> GRAVITY_OFF = new HashSet<>();

    private ZangiefPiledriverController() {}

    /** 一个玩家当前所在相位；{@code enteredAt} 是该相位的起始 gameTime。 */
    private static final class PhaseState {
        private int phase;
        private long enteredAt;
        /** 相位 1 进入时的 Y：抬升目标 = 它 + {@code riseHeightBlocks}。 */
        private double riseStartY;
        private boolean riseClampWarned;

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
                    enterRising(player);
                }
            }
            case ZangiefPiledriverEffect.PHASE_LEVITATING -> {
                if (phaseElapsed(player, elapsed, FarmstormConfig.zangiefCaneRiseTicks.get())) {
                    enterFalling(player);
                } else {
                    tickRising(player, elapsed);
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

    /** 相位 1 / 2 的对称自检：效果被外力清掉就收尾。 */
    private static void requireEffect(ServerPlayer player) {
        if (player.getEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER) == null) {
            Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 状态复位（效果被外部清除）玩家 %s",
                    player.getGameProfile().getName()));
            finish(player);
        }
    }

    /** 把重力还给玩家（幂等）：所有退出路径都调它，另有 {@link #sweepGravity} 兜底。 */
    private static void releaseGravity(Entity entity) {
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
        if (!GRAVITY_OFF.contains(id) || ACTIVE.containsKey(id)) {
            return;
        }
        Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 重力已恢复（兜底）玩家 %s",
                player.getGameProfile().getName()));
        releaseGravity(player);
    }

    /**
     * 进入相位 1：记下起点 Y、关重力（否则原版重力会把伺服抬升吃掉一半），之后每 tick 由 {@link #tickRising} 抬。
     */
    private static void enterRising(ServerPlayer player) {
        int riseTicks = Math.max(1, FarmstormConfig.zangiefCaneRiseTicks.get());
        setPhase(player, ZangiefPiledriverEffect.PHASE_LEVITATING, riseTicks + PHASE_EFFECT_CUSHION);
        PhaseState state = ACTIVE.get(player.getUUID());
        if (state != null) {
            state.riseStartY = player.getY();
            state.riseClampWarned = false;
        }
        player.setNoGravity(true);
        GRAVITY_OFF.add(player.getUUID());
    }

    /**
     * 相位 1：伺服抬升——每刻向上速度 = 剩余高度 ÷ 剩余刻数（与旧版下坠伺服同一套算法，只是方向反过来）。
     * 这样「抬多少格」由 {@code riseHeightBlocks} 决定、「多久抬完」由 {@code riseTicks} 决定，两个键各管一头。
     */
    private static void tickRising(ServerPlayer player, long elapsed) {
        PhaseState state = ACTIVE.get(player.getUUID());
        if (state == null) {
            return;
        }
        int riseTicks = Math.max(1, FarmstormConfig.zangiefCaneRiseTicks.get());
        double targetY = state.riseStartY + FarmstormConfig.zangiefCaneRiseHeightBlocks.get();
        int remaining = (int) Math.max(1, riseTicks - elapsed);
        double servo = (targetY - player.getY()) / remaining;
        if (servo > MAX_RISE_SPEED && !state.riseClampWarned) {
            state.riseClampWarned = true;
            Farmstorm.LOGGER.warn(String.format(Locale.ROOT, "桑吉尔蔗 上升伺服触顶 v=%.2f（目标 %.2f 格 / 剩 %d 刻）",
                    servo, targetY - player.getY(), remaining));
        }
        player.setDeltaMovement(0.0, Math.min(Math.max(servo, 0.0), MAX_RISE_SPEED), 0.0);
        syncMotion(player);
    }

    /**
     * 进入相位 2：把重力还给玩家并清掉残余速度——从这里开始**只由原版重力**决定下落，坠落伤害照常结算。
     * <p>「少 1 格」的问题在相位 1 一侧解决（多留 1 刻让最后的伺服速度结算，见 {@code onPlayerTick}）；
     * 这里清零是必要的：留着残余上冲速度实测会多滑到 **16.11 格**。
     */
    private static void enterFalling(ServerPlayer player) {
        setPhase(player, ZangiefPiledriverEffect.PHASE_FALLING, MAX_FALL_TICKS);
        releaseGravity(player);
        player.setDeltaMovement(Vec3.ZERO);
        syncMotion(player);
    }

    /** 相位 2：只做落地检测与超时兜底，**不写速度**（作者要「自然坠落 + 痛感」）。 */
    private static void tickFalling(ServerPlayer player, long elapsed) {
        if (elapsed >= LANDING_CHECK_AFTER && player.onGround()) {
            Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 落地耗时 %d 刻（自然坠落）", elapsed));
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
    }

    /** 把 {@code setDeltaMovement} 同步给客户端：{@code hurtMarked} 走 ServerEntity，显式包兜「单人没有旁观者」的情况。 */
    private static void syncMotion(ServerPlayer player) {
        player.hurtMarked = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
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

    /** 收尾（幂等）：还重力、摘效果、记账清空。 */
    public static void finish(ServerPlayer player) {
        releaseGravity(player);                          // 先还重力，再管相位表（记录已空时也要还）
        if (ACTIVE.remove(player.getUUID()) == null) {
            return;
        }
        player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
        player.removeEffect(MobEffects.LEVITATION);      // 兜底：旧存档里可能还挂着漂浮效果
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 结束 t=%d 玩家 %s",
                player.level().getGameTime(), player.getGameProfile().getName()));
    }

    /** 注销即清账：{@code ACTIVE} 是内存表，不清会残留到下一次登录（§5.3）。 */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
        releaseGravity(event.getEntity());               // 退出路径②：掉线（否则 noGravity 会跟着存档过去）
    }

    /** 退出路径③：换维度（同一份玩家实体，相位表按 UUID 记，不恢复就会带着 noGravity 走）。 */
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
        releaseGravity(event.getEntity());
    }

    /** 退出路径④：死亡——清账并顺手摘掉效果与速度，避免重生后带着半截状态。 */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (ACTIVE.remove(player.getUUID()) == null) {
            releaseGravity(player);
            return;
        }
        releaseGravity(player);
        player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
        player.removeEffect(MobEffects.LEVITATION);
        player.setDeltaMovement(Vec3.ZERO);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 结束（死亡）玩家 %s", player.getGameProfile().getName()));
    }
}
