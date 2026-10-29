package com.dashpig.farmstorm.effect;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 狂野猎人的状态载体（D-61 / D-62、TECH §3.5）：可见、带图标。
 * <p>只做三件注册层的事——分类与图标、三项属性修饰器的挂载与卸下、给客户端一个「效果在不在」的判据。
 * 索敌 / 冲刺 / 击杀续时都在服务端类里，不在这里。
 */
public class WildHunterEffect extends MobEffect {
    /**
     * 三个修饰器 id 必须是固定常量：卸下时按 id 逐个匹配
     * （{@code LivingEntity#onEffectRemoved} → {@code #removeAttributeModifiers}），
     * 每次新建 id 就会卸不掉，效果过期后留下残余属性。
     */
    public static final ResourceLocation SPEED_ID = Farmstorm.id("wild_hunter_speed");
    public static final ResourceLocation ATTACK_SPEED_ID = Farmstorm.id("wild_hunter_attack_speed");
    public static final ResourceLocation ATTACK_DAMAGE_ID = Farmstorm.id("wild_hunter_attack_damage");

    public WildHunterEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B1A1A);
    }

    /**
     * 三项百分比的唯一来源是 config（F-61 / V-13）：{@code wild_hunter.speedBonusPercent}（30.0）、
     * {@code attackSpeedBonusPercent}（30.0）、{@code attackDamageBonusPercent}（40.0）。
     * 取的是 {@code FarmstormConfig.bake()} 的快照而不是 {@code .get()}——本方法在两侧都会被调用（TECH §3.5），
     * 而 SERVER config 在客户端读不到，快照在未 bake 时回落到 §8.1 的默认值。
     */
    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        apply(attributes, Attributes.MOVEMENT_SPEED, SPEED_ID,
                FarmstormConfig.wildHunterSpeedBonus(), amplifier);
        apply(attributes, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID,
                FarmstormConfig.wildHunterAttackSpeedBonus(), amplifier);
        apply(attributes, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID,
                FarmstormConfig.wildHunterAttackDamageBonus(), amplifier);
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {
        clear(attributes, Attributes.MOVEMENT_SPEED, SPEED_ID);
        clear(attributes, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID);
        clear(attributes, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID);
    }

    /**
     * 与原版 {@code MobEffect.AttributeTemplate#create} 同构：量值按 amplifier + 1 倍。
     * 先按 id 卸再挂，同一条效果被重复施加（相位重设 / 续时）时不会叠加。
     * {@code addPermanentModifier} 是原版对效果修饰器的选择：1.21.1 读档时不重挂修饰器，加成靠存档里的修饰器恢复。
     */
    private static void apply(AttributeMap attributes, Holder<Attribute> attribute, ResourceLocation id,
                              double percentPerLevel, int amplifier) {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        instance.addPermanentModifier(new AttributeModifier(id, percentPerLevel * (amplifier + 1) / 100.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void clear(AttributeMap attributes, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}
