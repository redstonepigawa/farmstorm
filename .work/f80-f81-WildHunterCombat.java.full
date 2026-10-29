package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 狂野猎人的服务端判定（TECH §3.12 / D-64）：F-78 落地索敌。
 * <p>算法只有一份：客户端的预测（F-79）与服务端的复核（F-80）都调 {@link #resolveTarget(Player)}，
 * 参数因此是 {@link Player} 而不是 {@code ServerPlayer}——客户端那份跑在 {@code ClientLevel} 上（TECH §3.12 末段）。
 */
public final class WildHunterCombat {
    /** D-75：击退用原版默认强度，不自订数值。 */
    private static final double KNOCKBACK_STRENGTH = 0.4;

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


    /**
     * F-80 的服务端结算入口（D-65 / D-75）：用与客户端同一套 {@link #resolveTarget(Player)} 独立复核目标。
     * <ul>
     *   <li>复核目标 == 这次打中的实体：原版攻击已经结算了伤害与击退，这里<b>不重复施加</b>（D-75 禁止自订数值）。</li>
     *   <li>复核目标 != 打中的实体（吸附后准星偏了 / 挥空）：由服务端对被吸附的目标结算
     *       {@code Attributes.ATTACK_DAMAGE} 点伤害（含 {@code wild_hunter} 的 +40 %）与原版默认强度击退。</li>
     *   <li>相位期间（{@code zangief_piledriver} 在身）直接放手：输入全锁时再吸附会自相矛盾（TECH §3.12）。</li>
     * </ul>
     */
    public static void settleSwing(ServerPlayer player, @Nullable LivingEntity directlyHit) {
        if (!player.hasEffect(FarmstormMobEffects.WILD_HUNTER)
                || player.hasEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER)) {
            return;
        }
        LivingEntity resolved = resolveTarget(player);
        if (resolved == null || resolved == directlyHit) {
            return;
        }
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (!resolved.hurt(player.damageSources().playerAttack(player), damage)) {
            return;
        }
        Vec3 push = resolved.position().subtract(player.position());
        resolved.knockback(KNOCKBACK_STRENGTH, -push.x, -push.z);
        Farmstorm.LOGGER.info("狂野猎人 结算 目标 {} 伤害 {} 击退 {}", 
                BuiltInRegistries.ENTITY_TYPE.getKey(resolved.getType()), format(damage, 1),
                format(KNOCKBACK_STRENGTH, 1));
    }

    /**
     * F-81 的击杀续时（D-74）：剩余时长 += {@code wild_hunter.killExtensionTicks}（160 刻），
     * 回复 {@code wild_hunter.killHealPoints}（10 点）生命；**没有次数上限、也没有计数器**。
     */
    public static void extendOnKill(ServerPlayer player) {
        MobEffectInstance current = player.getEffect(FarmstormMobEffects.WILD_HUNTER);
        if (current == null) {
            return;
        }
        int extended = current.getDuration() + FarmstormConfig.wildHunterKillExtensionTicks.get();
        player.addEffect(new MobEffectInstance(FarmstormMobEffects.WILD_HUNTER, extended, current.getAmplifier(),
                current.isAmbient(), current.isVisible(), current.showIcon()));
        player.heal(FarmstormConfig.wildHunterKillHealPoints.get().floatValue());
        Farmstorm.LOGGER.info("狂野猎人 续时 {}", extended);
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
