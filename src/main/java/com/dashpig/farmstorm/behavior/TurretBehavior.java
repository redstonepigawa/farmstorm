package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import java.util.Optional;
import net.minecraft.world.entity.Entity;

/**
 * 炮台类行为（TECH §4.6）：索敌 → 开火 → 进冷却。
 * 冷却由 BlockEntity 的 tick 分发早退实现（§4.5），所以 60 刻开火间隔期间不会再有索敌开销（§8.1）。
 */
public interface TurretBehavior extends DeployedBehavior {

    @Override
    default int scanInterval() {
        return 10;
    }

    /** 玩家优先，可选投掷物（DEC C-3）。 */
    Optional<Entity> findTarget(DeployedCropBlockEntity be);

    void fire(DeployedCropBlockEntity be, Entity target);

    /** 开火后的冷却刻数。 */
    int cooldownTicks();

    @Override
    default void tick(DeployedCropBlockEntity be) {
        findTarget(be).ifPresent(target -> {
            fire(be, target);
            be.setCooldown(cooldownTicks());
        });
    }
}
