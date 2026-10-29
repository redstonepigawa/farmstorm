package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TrapBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;

/**
 * 骨架：真实球形判定与相位驱动由 F-86 / F-87 填充（TECH §3.10）。
 * 本轮只提供 {@link com.dashpig.farmstorm.behavior.DeployedBehaviors} 需要的单例与穷尽 switch 分支，
 * 判定恒为空、瞬时动作为空实现。
 */
public final class ZangiefCaneTrap implements TrapBehavior {

    @Override
    public DeployedKind kind() {
        return DeployedKind.ZANGIEF_CANE;
    }

    @Override
    public Optional<Player> findTrigger(DeployedCropBlockEntity be) {
        return Optional.empty();
    }

    @Override
    public void trigger(DeployedCropBlockEntity be, Player target) {}
}
