package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TurretBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 近防菌的炮台类行为（TECH §3.4）：以自身为中心、欧氏 {@code interceptRadius} 格球内拦非己方的本模组投掷物。
 *
 * <p>F-118 只做到「找到目标」：{@link #findTarget} 的 AABB 粗筛 + 四筛 + 取最近。开火仍是骨架，
 * 因此拦截计数（F-119）与图效 / {@code discard()}（F-120）都还没有发生。
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
        // 立方粗筛的角点到中心是 8√3 ≈ 13.86 格，所以半径只在这里当盒半边长，判定靠 ③ 的欧氏复核。
        AABB box = new AABB(center, center).inflate(radius);
        List<ThrownCropEntity> candidates = level.getEntitiesOfClass(ThrownCropEntity.class, box,
                target -> passesFilters(be, center, radius, target));
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
     * 四筛，顺序固定（D-98：先判 instanceof、再判同盟、最后才轮得到扣次数）：
     * ① 未移除 → ② 非子母弹 → ③ 欧氏半径内 → ④ 非己方。
     *
     * <p>④ 只调用既有的 {@link DeployedCropBlockEntity#isAlly(Player)} 一条链路，不新写队伍比较；
     * 归属者不是玩家（发射器 / 指令生成 / {@code getOwner() == null}）时按非己方处理。
     */
    private static boolean passesFilters(DeployedCropBlockEntity be, Vec3 center, double radius, ThrownCropEntity target) {
        if (target.isRemoved()) {
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

    @Override
    public void fire(DeployedCropBlockEntity be, Entity target) {
        // 骨架：拦截结算归 F-119（计数 / 同刻去重 / 第 6 次移除）与 F-120（图效 + discard）。
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }
}
