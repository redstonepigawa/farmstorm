package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.combat.WildHunterCombat;
import com.dashpig.farmstorm.combat.WildHunterDash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * F-79 的客户端冲刺预测（D-65 / BRIEF §7.6）：本地按下攻击键 + 自己带着 {@code wild_hunter} +
 * 本地用与服务端同一套 {@link WildHunterCombat#resolveTarget} 算出目标时，按
 * {@link WildHunterDash#velocity} 在 {@code wild_hunter.dashTicks}（默认 3）刻内跨到落点。
 * <p>预测只是手感，权威仍在服务端（{@link WildHunterCombat#settleSwing}）。不发送任何自定义包（D-50）。
 * <p>接线在 {@code FarmstormClient}（调度者独占文件），本类不自己挂事件。
 */
public final class WildHunterDashClient {
    private static Vec3 velocity = Vec3.ZERO;
    private static int remainingTicks = 0;

    private WildHunterDashClient() {}

    /** 本地攻击键按下时调用（{@code InputEvent.InteractionKeyMappingTriggered#isAttack()}）。 */
    public static void onAttackKey() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !player.hasEffect(com.dashpig.farmstorm.registry.FarmstormMobEffects.WILD_HUNTER)) {
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
        int dashTicks = WildHunterDash.dashTicks();
        velocity = WildHunterDash.velocity(player.position(), plan.landing(), dashTicks);
        remainingTicks = dashTicks;
    }

    /** 每个客户端刻把预测速度写到本地玩家身上；跨到位就停。 */
    public static void onClientTick() {
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

    /** 死亡 / 退出世界 / 效果消失时清干净。 */
    public static void reset() {
        velocity = Vec3.ZERO;
        remainingTicks = 0;
    }
}
