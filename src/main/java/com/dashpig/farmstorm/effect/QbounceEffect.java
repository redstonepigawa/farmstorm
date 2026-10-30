package com.dashpig.farmstorm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Q弹 的状态载体（TECH §3.1）。唯一职责是「效果在不在 + 剩余时长」：不设自己的时长键、不产生位移、
 * 不改属性、不发包（D-50 / D-81）。{@code applyEffectTick} 沿用 {@link MobEffect} 的空实现，因此本类无副作用。
 *
 * <p>F-106 定稿：本类到「构造器 + 一个 {@code MobEffectCategory} + 一个颜色」为止，**故意不再长**——不重写
 * {@code applyEffectTick} / {@code isDurationEffectTick}，不设时长键，不读任何 config 键。时长由施加方
 * {@code FriendChivesProjectile} 用 {@code friend_chives.effectTicks}（200 刻、amplifier 0）写入（§3.1 的分工）。
 * 感知侧是 {@code client/QbounceClientState}（F-107），姿态侧是 F-108 / F-109；两者都不回写本类。
 */
public class QbounceEffect extends MobEffect {

    public QbounceEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF6EC7);
    }
}
