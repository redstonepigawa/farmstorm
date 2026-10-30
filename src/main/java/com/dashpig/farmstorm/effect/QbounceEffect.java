package com.dashpig.farmstorm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Q弹 的状态载体（TECH §3.1）。唯一职责是「效果在不在 + 剩余时长」：不设自己的时长键、不产生位移、
 * 不改属性、不发包（D-50 / D-81）。{@code applyEffectTick} 沿用 {@link MobEffect} 的空实现，因此本类无副作用。
 *
 * <p>F-103 只立编译期骨架；客户端状态机与两条姿态分支由 F-107 ~ F-109 填入。
 */
public class QbounceEffect extends MobEffect {

    public QbounceEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF6EC7);
    }
}
