package com.dashpig.farmstorm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 混乱的状态载体（TECH §3.2）。隐藏、不注册图标（{@code textures/mob_effect/} 下没有它的 PNG）；
 * 可见性由施加端的六参 {@code MobEffectInstance(..., false, false)} 决定，不是类级钩子。
 * 「只对玩家有效」由两侧保证：施加端只遍历 {@code level.players()}，消费端跑在客户端输入链上。
 *
 * <p>F-106 定稿：同样只有构造器——不重写 {@code applyEffectTick} / {@code isDurationEffectTick}，不设时长键，
 * 不读任何 config 键（D-83「混乱组连键都不设」）。时长由 {@code VibrationBeanTrap} 用
 * {@code vibration_bean.confusionTicks}（80 刻）写入；反向移动由客户端 {@code ConfusionInputHandler}（F-111）消费，
 * 因此本类不需要、也不应该知道效果有多长。
 */
public class ConfusionEffect extends MobEffect {

    public ConfusionEffect() {
        super(MobEffectCategory.HARMFUL, 0x6A0DAD);
    }
}
