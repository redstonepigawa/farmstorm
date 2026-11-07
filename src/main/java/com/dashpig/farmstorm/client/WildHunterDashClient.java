package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.combat.WildHunterCombat;
import com.dashpig.farmstorm.combat.WildHunterDash;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 狂野猎人的客户端冲刺预测（F-79 攻击锁定冲刺 + 作者 v0.0.2 的**右键四向位移**）。
 * <p>两条路径共用同一套位移写入（{@link #velocity} 每刻下发、{@link #remainingTicks} 计数），
 * 攻击冲刺的落点规则来自 {@link WildHunterDash}（与服务端共用一份数学）；
 * 右键位移的落点在 {@link #planDirectional} 里按同样的采样口径算（{@code combat/**} 归服务端那条线，本类不去改它）。
 * <p>预测只是手感，权威仍在服务端（{@link WildHunterCombat#settleSwing} / 服务端的右键复核）。
 * 不发送任何自定义包（D-50）。接线在 {@code FarmstormClient}（调度者独占文件）。
 */
public final class WildHunterDashClient {
    /** 三个数字全部来自 {@link WildHunterClientState} 的镜像常量：一处定义、处处引用。 */
    public static final double DASH_DISTANCE = WildHunterClientState.DEFAULT_DASH_DISTANCE;
    public static final int MAX_CHARGES = WildHunterClientState.DEFAULT_DASH_CHARGES;
    public static final int RECHARGE_TICKS = WildHunterClientState.DEFAULT_DASH_CHARGE_TICKS;
    /** 路径采样步长，与 {@code combat/WildHunterDash} 同口径。 */
    private static final double SAMPLE_STEP = 0.25;
    private static final double FEET_OFFSET = 0.1;
    private static final double HEAD_OFFSET = 1.1;

    private static Vec3 velocity = Vec3.ZERO;
    private static int remainingTicks = 0;

    /** 当前可用充能（0..{@link #MAX_CHARGES}）。 */
    private static int charges = MAX_CHARGES;
    /** 距离下一次回充还剩多少刻；充满时不计时。 */
    private static int rechargeTimer = 0;

    private WildHunterDashClient() {}

    /** 本地攻击键按下时调用（{@code InputEvent.InteractionKeyMappingTriggered#isAttack()}）。 */
    public static void onAttackKey() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !player.hasEffect(FarmstormMobEffects.WILD_HUNTER.get())) {
            return;
        }
        LivingEntity target = WildHunterCombat.resolveTarget(player);
        if (target == null) {
            return;
        }
        WildHunterDash.Result plan = WildHunterDash.plan(player, target);
        if (!plan.attached()) {
            return;
        }
        start(player, plan.landing());
    }

    /**
     * 右键按下时调用（{@code InputEvent.InteractionKeyMappingTriggered#isUseItem()}）。
     *
     * @return {@code true} 表示这次右键被位移吃掉（调用方要 {@code event.setCanceled(true)}）；
     *         {@code false} 表示放行原版右键（吃东西 / 放方块 / 开箱子都不受影响）
     */
    public static boolean onUseKey() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return false;
        }
        if (!player.hasEffect(FarmstormMobEffects.WILD_HUNTER.get()) || minecraft.screen != null) {
            return false;                                  // 效果不在 / 开着界面：不抢右键
        }
        if (charges <= 0) {
            return false;                                  // 没充能：交给原版
        }
        Vec3 direction = inputDirection(player);
        if (direction == null) {
            return false;                                  // 没有 WASD 输入：交给原版
        }
        Vec3 landing = walkDirection(minecraft.level, player.position(), direction, DASH_DISTANCE);
        if (landing.distanceTo(player.position()) < SAMPLE_STEP * 2) {
            return false;                                  // 正贴着墙，位移走不动：交给原版，别白发一次充能
        }
        start(player, landing);
        charges--;
        if (rechargeTimer <= 0) {
            rechargeTimer = RECHARGE_TICKS;
        }
        Farmstorm.LOGGER.info("狂野猎人 右键位移 方向 {} 落点 {} 剩余充能 {}",
                String.format(java.util.Locale.ROOT, "(%.2f, %.2f)", direction.x, direction.z),
                String.format(java.util.Locale.ROOT, "(%.2f, %.2f, %.2f)", landing.x, landing.y, landing.z), charges);
        return true;
    }

    /** 每个客户端刻：回充 + 把预测速度写到本地玩家身上；跨到位就停。 */
    public static void onClientTick() {
        tickRecharge();
        if (remainingTicks <= 0) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            reset();
            return;
        }
        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        if (--remainingTicks == 0) {
            velocity = Vec3.ZERO;
        }
    }

    /** 当前可用充能数（HUD 只读）。 */
    public static int charges() {
        return charges;
    }

    /** 正在回充的那一格的进度 0..1（充满时返回 1）。 */
    public static float rechargeProgress() {
        if (charges >= MAX_CHARGES || rechargeTimer <= 0) {
            return 1.0F;
        }
        return 1.0F - (float) rechargeTimer / RECHARGE_TICKS;
    }

    /** 死亡 / 退出世界 / 效果消失时清干净。 */
    public static void reset() {
        velocity = Vec3.ZERO;
        remainingTicks = 0;
        charges = MAX_CHARGES;
        rechargeTimer = 0;
    }

    private static void start(LocalPlayer player, Vec3 landing) {
        int dashTicks = WildHunterDash.dashTicks();
        velocity = WildHunterDash.velocity(player.position(), landing, dashTicks);
        remainingTicks = dashTicks;
    }

    private static void tickRecharge() {
        if (!WildHunterClientState.active()) {
            if (charges != MAX_CHARGES || rechargeTimer != 0) {
                charges = MAX_CHARGES;                     // 效果没了就重置（与服务端的下次施加对齐）
                rechargeTimer = 0;
            }
            return;
        }
        if (charges >= MAX_CHARGES) {
            rechargeTimer = 0;
            return;
        }
        if (rechargeTimer > 0 && --rechargeTimer == 0) {
            charges++;
            if (charges < MAX_CHARGES) {
                rechargeTimer = RECHARGE_TICKS;
            }
        }
    }

    /** 四向取整后的键盘输入方向；没有输入时返回 {@code null}。 */
    private static Vec3 inputDirection(LocalPlayer player) {
        float forward = player.input.forwardImpulse;
        float strafe = player.input.leftImpulse;
        if (Math.abs(forward) < 0.01F && Math.abs(strafe) < 0.01F) {
            return null;
        }
        double yaw = Math.toRadians(player.getYRot());
        Vec3 facing = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));   // yaw 0 = +Z
        Vec3 left = new Vec3(facing.z, 0.0, -facing.x);               // 朝 +Z 时左手朝 +X
        Vec3 raw = facing.scale(forward).add(left.scale(strafe));
        if (raw.lengthSqr() < 1.0E-6) {
            return null;
        }
        // 斜向按最主要的那个轴取整 ⇒ 只有四个方向
        return Math.abs(forward) >= Math.abs(strafe) ? facing.scale(Math.signum(forward)) : left.scale(Math.signum(strafe));
    }

    /**
     * 沿 {@code dir} 走 {@code travel} 格，返回最后一个「脚/头都空」的位置。
     *
     * <p><b>与 {@code combat/WildHunterDash#walk} 同口径，改动必须同步</b>（步长 0.25、脚 +0.1 / 头 +1.1）。
     * 之所以两份而不是一份：那边的 {@code walk} 是 {@code private}，而 {@code combat/**} 归服务端/战斗那条线，
     * 不能为了消重复去改它；等那条线收工后由调度者决定是否合并成 {@code WildHunterDash.planDirectional(...)}。
     */
    private static Vec3 walkDirection(Level level, Vec3 start, Vec3 dir, double travel) {
        Vec3 reachable = start;
        int steps = (int) Math.ceil(travel / SAMPLE_STEP);
        for (int step = 1; step <= steps; step++) {
            Vec3 probe = start.add(dir.scale(Math.min(travel, step * SAMPLE_STEP)));
            if (!free(level, probe)) {
                break;
            }
            reachable = probe;
        }
        return reachable;
    }

    private static boolean free(Level level, Vec3 position) {
        return !blocked(level, position, FEET_OFFSET) && !blocked(level, position, HEAD_OFFSET);
    }

    private static boolean blocked(Level level, Vec3 position, double offset) {
        BlockPos pos = BlockPos.containing(position.x, position.y + offset, position.z);
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}
