package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TurretBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 近防菌的炮台类行为（TECH §3.4）：以自身为中心、欧氏 {@code interceptRadius} 格球内拦非己方的本模组投掷物。
 *
 * <p>{@link #findTarget} 负责 AABB 粗筛 + 四筛 + 同刻去重 + 取最近（F-118 / F-119）；{@link #fire} 负责
 * 计数（F-119）与图效 / {@code discard()}（F-120）：拦下的投掷物当场消失，**永不结算落点**（D-99）。
 */
public class PointDefenseFungusBehavior implements TurretBehavior {

    @Override
    public DeployedKind kind() {
        return DeployedKind.POINT_DEFENSE_FUNGUS;
    }

    /**
     * 索敌（TECH §6.2 第 3~5 步）：立方 AABB 只做粗筛，真正的判定在 predicate 的四筛里做完，
     * 最后按欧氏距离取最近的一个——{@code getEntitiesOfClass} 不保证顺序，不能拿首元素当最近。
     */
    @Override
    public Optional<Entity> findTarget(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        Vec3 center = be.getBlockPos().getCenter();
        double radius = FarmstormConfig.pointDefenseFungusInterceptRadius.get();
        long now = level.getGameTime();
        // 立方粗筛的角点到中心是 8√3 ≈ 13.86 格，所以半径只在这里当盒半边长，判定靠欧氏复核。
        AABB box = new AABB(center, center).inflate(radius);
        List<ThrownCropEntity> candidates = level.getEntitiesOfClass(ThrownCropEntity.class, box,
                target -> passesFilters(be, center, radius, now, target));
        ThrownCropEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (ThrownCropEntity candidate : candidates) {
            double distance = center.distanceTo(candidate.position());
            if (distance < best) {
                best = distance;
                nearest = candidate;
            }
        }
        return Optional.ofNullable(nearest);
    }

    /**
     * D-98 的四筛，顺序固定（先判 instanceof、再判同盟、最后才轮得到扣次数）：
     * ① 未移除 →（同刻去重）→ ② 非子母弹 → ③ 欧氏半径内 → ④ 非己方。
     *
     * <p>④ 只调用既有的 {@link DeployedCropBlockEntity#isAlly(Player)} 一条链路，不新写队伍比较；
     * 归属者不是玩家（发射器 / 指令生成 / {@code getOwner() == null}）时按非己方处理。
     */
    private static boolean passesFilters(DeployedCropBlockEntity be, Vec3 center, double radius, long now,
            ThrownCropEntity target) {
        if (target.isRemoved()) {
            return false;
        }
        // 同刻去重（§6.3）的**索敌端口**：挡的是「本刻已被另一台菌扣过、但还没从实体段摘除」这一个窗口，
        // 于是去重由「本刻已拦过」这条语义显式成立，不再依赖上一台菌 discard() 的时序。
        if (target.wasInterceptedAt(now)) {
            return false;
        }
        if (target.isSubmunition()) {
            return false;
        }
        if (center.distanceTo(target.position()) > radius) {
            return false;
        }
        return !(target.getOwner() instanceof Player thrower && be.isAlly(thrower));
    }

    /**
     * 开火 = 拦截（TECH §6.2 第 6 步 / F-120）。四步顺序固定，且**没有任何走 {@code onImpact} 的分支**：
     * 被拦下的投掷物在第四步 {@code discard()}，落点永不结算——不爆炸、不写方块、不施加效果、不掉落（D-99）。
     */
    @Override
    public void fire(DeployedCropBlockEntity be, Entity target) {
        if (!(be.getLevel() instanceof ServerLevel level) || !(target instanceof ThrownCropEntity projectile)) {
            return;
        }
        long now = level.getGameTime();
        // ① 同刻去重（§6.3）：本刻已被别的菌扣过就整段跳过——不扣次数、不发粒子。
        if (projectile.markIntercepted(now)) {
            Farmstorm.LOGGER.info("近防菌 本刻已拦过 t={} 投掷物 id={} @ {} 跳过", now, projectile.getId(), be.getBlockPos());
            return;
        }
        // ② 拦截图效之一：类型固定 CRIT，数量取 config（§6.5「只留粒子与音效」）。
        // 粒子取投掷物位置、声音取方块位置，两者刻意不同源（视觉爆点在被拦下的弹上，听觉来源是这台菌），不是笔误。
        Vec3 at = projectile.position();
        level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z,
                FarmstormConfig.pointDefenseFungusInterceptParticles.get(), 0.3, 0.3, 0.3, 0.05);
        // ③ 之二：复用原版事件，不新增 sound_event（D-100）。
        level.playSound(null, be.getBlockPos(), SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.8F, 1.2F);
        // ④ 先记账（第 6 次会在此静默移除方块），再让投掷物消失：顺序固定，不回滚（§6.3）。
        be.recordIntercept(now);
        projectile.discard();
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }
}
