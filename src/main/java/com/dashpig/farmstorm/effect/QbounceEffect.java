package com.dashpig.farmstorm.effect;

import com.dashpig.farmstorm.combat.MobAiSuspension;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

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

    /*
     * 进入面（F-255 / 42 号 §2.3.4）在 1.21.1 里是本类的 {@code onEffectAdded}；1.20.1 的 {@link MobEffect}
     * 没有这个钩子，改由 {@code QbounceMountController#onEffectAdded}（{@code MobEffectEvent.Added}）承载——
     * 触发条件与原来完全一样（新实例、{@code getOldEffectInstance() == null}、{@code instanceof Mob}），
     * 而 {@code MobAiSuspension.suspend} 本身只在服务端生效，所以搬过去不丢任何分支。
     * 本类因此回到「构造器 + 分类 + 颜色」为止（F-106 定稿），不再持有任何事件入口。
     */
}
