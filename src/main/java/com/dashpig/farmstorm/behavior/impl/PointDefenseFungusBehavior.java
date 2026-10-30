package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TurretBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import java.util.Optional;
import net.minecraft.world.entity.Entity;

/**
 * 近防菌的炮台类行为（TECH §3.4）：每 tick 判「有没有本模组的投掷物进入拦截半径」，按 {@code interceptCount} 去重，
 * 己方投掷物不拦也不扣（D-98 / D-99）。
 *
 * <p>F-103 只立编译期骨架：索敌恒空、开火为空，因此拦截与计数都不发生；四筛与计数去重由 F-118 / F-119 填入。
 */
public class PointDefenseFungusBehavior implements TurretBehavior {

    @Override
    public DeployedKind kind() {
        return DeployedKind.POINT_DEFENSE_FUNGUS;
    }

    @Override
    public Optional<Entity> findTarget(DeployedCropBlockEntity be) {
        return Optional.empty();
    }

    @Override
    public void fire(DeployedCropBlockEntity be, Entity target) {
        // 骨架：F-119 填拦截结算
    }

    @Override
    public int cooldownTicks() {
        return 0;
    }
}
