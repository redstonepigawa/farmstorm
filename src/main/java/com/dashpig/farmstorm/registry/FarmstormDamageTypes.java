package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

/**
 * 3 个数据驱动的伤害类型（D-39 / D-96）。
 * 伤害类型的实例由数据包 JSON 提供，代码侧只持有 ResourceKey，因此不参与 DeferredRegister（R-02）。
 * 取用一律走这里的静态方法，保证 {@code #minecraft:is_projectile} / {@code #minecraft:is_explosion} 标签的语义
 * （爆炸保护与抗性附魔据此生效）在调用点不被绕开。
 */
public final class FarmstormDamageTypes {
    /** man瓜 导弹命中：直接伤害，无疲劳消耗。 */
    public static final ResourceKey<DamageType> VOID_BOLT = key("void_bolt");

    /** 所有作物爆炸的伤害来源。 */
    public static final ResourceKey<DamageType> CROP_BLAST = key("crop_blast");

    /** D-96：桑吉尔蔗的坠落伤害（数据驱动，{@code data/farmstorm/damage_type/zangief_slam.json} 由 F-132 落地）。 */
    public static final ResourceKey<DamageType> ZANGIEF_SLAM = key("zangief_slam");

    private FarmstormDamageTypes() {}

    /** 导弹伤害：击杀归属炮台所有者的部署者。 */
    public static DamageSource voidBolt(Level level, @Nullable Entity owner) {
        return build(level, VOID_BOLT, owner);
    }

    /** 作物爆炸伤害：击杀归属投掷者 / 部署者。 */
    public static DamageSource cropBlast(Level level, @Nullable Entity owner) {
        return build(level, CROP_BLAST, owner);
    }

    /**
     * 桑吉尔蔗落地固定伤害：击杀归属陷阱部署者（D-96）。
     * <p>{@code null} = 数据包里的 {@code damage_type/zangief_slam.json} 还没注册（F-132 的交付物），
     * 此时 {@code DamageSources#source} 会抛 {@code IllegalStateException}；调用点据此跳过结算并留 warn。
     */
    @Nullable
    public static DamageSource zangiefSlam(Level level, @Nullable Entity owner) {
        if (level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(ZANGIEF_SLAM).isEmpty()) {
            return null;
        }
        return build(level, ZANGIEF_SLAM, owner);
    }

    /**
     * 构造 DamageSource。
     *
     * <p><b>不要用 {@code level.damageSources().source(key, entity)}</b>：那个重载在**原版**里不是 public，
     * 只有 NeoForge 的 access transformer 会把它放宽成 public。用它在原版 Forge 上会抛
     * {@code IllegalAccessError: ... tried to access private method DamageSources.m_269298_}（真机实测，
     * 触发点是胡萝卜陷阱引爆）。这里改用 {@link DamageSource#DamageSource(Holder, Entity)} —— 该构造器
     * 在 vanilla 里本身就是 public，两个加载器都通得过，语义与原重载完全一致
     * （{@code DamageSources.source(key, entity)} 的实现就是同一行 {@code new DamageSource(holder, entity)}）。
     */
    private static DamageSource build(Level level, ResourceKey<DamageType> key, @Nullable Entity owner) {
        Holder<DamageType> type = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);
        return new DamageSource(type, owner);
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Farmstorm.id(path));
    }
}
