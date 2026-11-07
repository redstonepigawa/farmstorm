package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TrapBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.explosion.BlastRule;
import com.dashpig.farmstorm.explosion.ExplosionQueue;
import com.dashpig.farmstorm.explosion.FarmstormExplosion;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.DistanceMetrics;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

/**
 * 胡罗贝（DESIGN §4.2）：一次性陷阱，非己方玩家进入切比雪夫半径内即自爆。
 * 爆炸走 {@link ExplosionQueue}（同 tick 超量时延迟到 tick 末，§8.2），威力与伤害上限分别来自两个配置键。
 * 反胃**不筛敌我**（D-27）：队友与部署者本人只要在爆炸半径内同样中招。
 * 胡罗贝没有 {@code DEMOLITION} 标签，因此爆炸不破坏任何方块——包括它自己，否则方块被炸掉会走战利品表掉出自身。
 * 这条判定由 {@link BlastRule#forCrop} 按标签给出，本类不写死规则。
 */
public final class BlastCarrotTrap implements TrapBehavior {

    @Override
    public DeployedKind kind() {
        return DeployedKind.BLAST_CARROT;
    }

    /**
     * 切比雪夫 ≤ {@code blast_carrot.triggerRadius}，原点与判定对象都取方块坐标（DESIGN §4.2）。
     * <p>D-114 的两遍：第一遍仍是玩家（创造 / 旁观 / 己方三条豁免一字未改）；第二遍补敌对生物，
     * 复用同一个半径与同一个切比雪夫（不写第二个数），生物没有队伍归属因此恒按非己方。
     */
    @Override
    public Optional<LivingEntity> findTrigger(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        BlockPos pos = be.getBlockPos();
        int radius = FarmstormConfig.blastCarrotTriggerRadius.get();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative() || be.isAlly(player)) {
                continue;                    // HC-09 非己方；DESIGN §7.6 L1053：创造玩家不触发陷阱
            }
            BlockPos target = player.blockPosition();
            if (DistanceMetrics.chebyshev(target.getX() - pos.getX(), target.getY() - pos.getY(),
                    target.getZ() - pos.getZ()) <= radius) {
                return Optional.of(player);
            }
        }
        for (Monster monster : level.getEntitiesOfClass(Monster.class, TrapBehavior.searchBox(pos, radius))) {
            BlockPos target = monster.blockPosition();
            if (DistanceMetrics.chebyshev(target.getX() - pos.getX(), target.getY() - pos.getY(),
                    target.getZ() - pos.getZ()) <= radius) {
                return Optional.of(monster);
            }
        }
        return Optional.empty();
    }

    @Override
    public void trigger(DeployedCropBlockEntity be, LivingEntity target) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        Vec3 center = pos.getCenter();
        int power = FarmstormConfig.blastCarrotExplosionPower.get();
        float damageCap = FarmstormConfig.blastCarrotMaxDamage.get();
        ServerPlayer owner = be.owner(level);
        // FIX-8：必须先摘掉陷阱方块再引爆。爆炸中心就是本方块的中心，方块还在时
        // Explosion#getSeenPercent 的采样射线立刻被自己的碰撞箱挡住（实测 seen = 0.0），
        // 伤害退化成公式保底 +1.0。ExplosionQueue 会把超量的爆炸推迟到 tick 末执行，
        // 所以「移除方块」必须排在 submit 之前，而不是依赖 detonate 恰好立即发生。
        be.consume();
        FarmstormExplosion explosion = new FarmstormExplosion(level, owner, FarmstormDamageTypes.cropBlast(level, owner),
                center.x, center.y, center.z, power,
                BlastRule.forCrop(CropProfile.BLAST_CARROT, BlastRule.VANILLA), damageCap, true);
        ExplosionQueue.submit(level, explosion);
        // TECH §4.6：爆炸范围半径 = power × 2（默认 2 × 2 = 4 格），范围内的所有玩家都吃反胃。
        applyNausea(level, center, power * 2.0);
    }

    private static void applyNausea(ServerLevel level, Vec3 center, double reach) {
        int effectLevel = FarmstormConfig.blastCarrotNauseaLevel.get();
        int durationTicks = FarmstormConfig.blastCarrotNauseaDurationTicks.get();
        if (effectLevel <= 0 || durationTicks <= 0) {
            return;                                                     // §4.11.3：durationTicks = 0 表示不施加
        }
        for (ServerPlayer player : level.players()) {
            if (center.distanceTo(player.position()) > reach) {
                continue;
            }
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, durationTicks, effectLevel - 1));
        }
    }
}
