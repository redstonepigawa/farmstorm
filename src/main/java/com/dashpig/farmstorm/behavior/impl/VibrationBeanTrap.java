package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TrapBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;

/**
 * 震动豆的陷阱行为（TECH §3.6）：非己方玩家进入 4.0 格即触发，范围内所有玩家获得 {@code vibration_bean.confusionTicks} 刻混乱，
 * 播一次爆炸音后静默消失、无掉落（D-92 / D-101）。
 *
 * <p>F-103 只立编译期骨架：触发条件恒空，因此不会触发也不会消失；结算由 F-124 填入。
 */
public class VibrationBeanTrap implements TrapBehavior {

    @Override
    public DeployedKind kind() {
        return DeployedKind.VIBRATION_BEAN;
    }

    @Override
    public Optional<Player> findTrigger(DeployedCropBlockEntity be) {
        return Optional.empty();
    }

    @Override
    public void trigger(DeployedCropBlockEntity be, Player target) {
        // 骨架：F-124 填触发结算
    }
}
