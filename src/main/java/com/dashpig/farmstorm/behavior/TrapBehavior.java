package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;

/**
 * 陷阱类行为（TECH §4.6）：一次触发即结算。
 * 触发对象只可能是非己方玩家（HC-09 / D-07），己方判定在 findTrigger 里实时解析（HC-08 / C-9）。
 */
public interface TrapBehavior extends DeployedBehavior {

    @Override
    default int scanInterval() {
        return 10;
    }

    Optional<Player> findTrigger(DeployedCropBlockEntity be);

    void trigger(DeployedCropBlockEntity be, Player target);

    @Override
    default void tick(DeployedCropBlockEntity be) {
        findTrigger(be).ifPresent(target -> trigger(be, target));
    }
}
