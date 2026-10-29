package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.effect.WildHunterEffect;
import com.dashpig.farmstorm.effect.ZangiefPiledriverEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 2 个 mob_effects（TECH §2.2 #10~#11、§3.3）：本模组第一个 {@code MobEffect} 注册器。
 * v0.0.1 的 mob_effects 是 0，因此这里不继承任何既有注册器。
 * 两个效果都在公共侧，不依赖 {@code Dist.CLIENT}（专用服务器启动不得 NoClassDefFoundError）。
 */
public final class FarmstormMobEffects {
    public static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(Registries.MOB_EFFECT, Farmstorm.MOD_ID);

    /** 可见、带图标：灰度 + 红色剪影 + 三项属性加成的载体（D-61 / D-62）。 */
    public static final DeferredHolder<MobEffect, WildHunterEffect> WILD_HUNTER =
            REGISTER.register("wild_hunter", WildHunterEffect::new);

    /** 隐藏、无图标：只承载 amplifier 相位号（D-50 / BRIEF §7.5）。 */
    public static final DeferredHolder<MobEffect, ZangiefPiledriverEffect> ZANGIEF_PILEDRIVER =
            REGISTER.register("zangief_piledriver", ZangiefPiledriverEffect::new);

    private FarmstormMobEffects() {}
}
