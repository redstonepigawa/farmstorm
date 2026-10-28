package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

/**
 * 2 个数据驱动的伤害类型（D-39）。
 * 伤害类型的实例由数据包 JSON 提供，代码侧只持有 ResourceKey，因此不参与 DeferredRegister（R-02）。
 * 取用一律走这里的静态方法，保证 {@code #minecraft:is_projectile} / {@code #minecraft:is_explosion} 标签的语义
 * （爆炸保护与抗性附魔据此生效）在调用点不被绕开。
 */
public final class FarmstormDamageTypes {
    /** man瓜 导弹命中：直接伤害，无疲劳消耗。 */
    public static final ResourceKey<DamageType> VOID_BOLT = key("void_bolt");

    /** 所有作物爆炸的伤害来源。 */
    public static final ResourceKey<DamageType> CROP_BLAST = key("crop_blast");

    private FarmstormDamageTypes() {}

    /** 导弹伤害：击杀归属炮台所有者的部署者。 */
    public static DamageSource voidBolt(Level level, @Nullable Entity owner) {
        return level.damageSources().source(VOID_BOLT, owner);
    }

    /** 作物爆炸伤害：击杀归属投掷者 / 部署者。 */
    public static DamageSource cropBlast(Level level, @Nullable Entity owner) {
        return level.damageSources().source(CROP_BLAST, owner);
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Farmstorm.id(path));
    }
}
