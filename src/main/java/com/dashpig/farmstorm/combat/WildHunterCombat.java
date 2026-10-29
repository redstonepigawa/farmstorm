package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 狂野猎人的服务端判定（TECH §3.12 / D-64）：F-78 落地索敌。
 * <p>算法只有一份：客户端的预测（F-79）与服务端的复核（F-80）都调 {@link #resolveTarget(Player)}，
 * 参数因此是 {@link Player} 而不是 {@code ServerPlayer}——客户端那份跑在 {@code ClientLevel} 上（TECH §3.12 末段）。
 */
public final class WildHunterCombat {
    private WildHunterCombat() {}

    /**
     * 选中 {@code wild_hunter.searchRadius}（默认 5.0 格）球内、与视线夹角不超过
     * {@code wild_hunter.searchAngleDegrees}（默认 30.0°）的生物：夹角最小者优先，夹角相同取距离近者（D-64）。
     * 排除自己、同队伍玩家（{@link Player#isAlliedTo}）、旁观者与已死实体。
     * <p>只在服务端打日志（{@code 狂野猎人 目标 {id} 夹角 {A} 度 距离 {D}}），客户端那一份静默。
     *
     * @return 没有候选时返回 {@code null}
     */
    public static LivingEntity resolveTarget(Player player) {
        double radius = FarmstormConfig.wildHunterSearchRadius.get();
        double maxAngle = FarmstormConfig.wildHunterSearchAngleDegrees.get();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class, box,
                candidate -> isCandidate(player, candidate, radius, maxAngle, eye, look));
        LivingEntity best = candidates.stream()
                .min(Comparator.comparingDouble((LivingEntity candidate) -> angleTo(eye, look, candidate))
                        .thenComparingDouble(player::distanceToSqr))
                .orElse(null);
        if (best != null && player.level() instanceof ServerLevel) {
            Farmstorm.LOGGER.info("狂野猎人 目标 {} 夹角 {} 度 距离 {}",
                    BuiltInRegistries.ENTITY_TYPE.getKey(best.getType()),
                    format(angleTo(eye, look, best), 1),
                    format(Math.sqrt(player.distanceToSqr(best)), 2));
        }
        return best;
    }

    private static boolean isCandidate(Player player, LivingEntity candidate, double radius, double maxAngle,
                                       Vec3 eye, Vec3 look) {
        if (candidate == player || !candidate.isAlive() || candidate.isSpectator()) {
            return false;
        }
        if (candidate instanceof Player other && player.isAlliedTo(other)) {
            return false;
        }
        if (player.distanceToSqr(candidate) > radius * radius) {
            return false;
        }
        return angleTo(eye, look, candidate) <= maxAngle;
    }

    /** 视线与「眼睛 → 目标包围盒中心」的夹角（度，0–180）。 */
    private static double angleTo(Vec3 eye, Vec3 look, LivingEntity target) {
        Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
        if (to.lengthSqr() < 1.0E-6) {
            return 0.0;
        }
        double dot = look.dot(to.normalize());
        return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));
    }

    private static String format(double value, int decimals) {
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }
}
