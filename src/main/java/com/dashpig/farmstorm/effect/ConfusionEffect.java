package com.dashpig.farmstorm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 混乱的状态载体（TECH §3.2）。隐藏、不注册图标（{@code textures/mob_effect/} 下没有它的 PNG）；
 * 可见性由施加端的六参 {@code MobEffectInstance(..., false, false)} 决定，不是类级钩子。
 * 「只对玩家有效」由两侧保证：施加端只遍历 {@code level.players()}，消费端跑在客户端输入链上。
 *
 * <p>F-103 只立编译期骨架；反向移动由 F-111 填入。
 */
public class ConfusionEffect extends MobEffect {

    public ConfusionEffect() {
        super(MobEffectCategory.HARMFUL, 0x6A0DAD);
    }
}
