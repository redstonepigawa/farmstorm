package com.dashpig.farmstorm.effect;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
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
     *
     * <p>1.20.1 的 {@code AttributeModifier} 用 {@link UUID} 认身份（1.21.1 换成了字符串 id），
     * 所以这里是「字符串 id → 稳定 UUID」的映射结果，值本身不变。
     */
    public static final UUID SPEED_ID = modifierId("wild_hunter_speed");
    public static final UUID ATTACK_SPEED_ID = modifierId("wild_hunter_attack_speed");
    public static final UUID ATTACK_DAMAGE_ID = modifierId("wild_hunter_attack_damage");

    /** 修饰器显示名（{@code /attribute} 与存档里的 {@code Name} 字段用它，判定永远只看 UUID）。 */
    private static final String MODIFIER_NAME = "farmstorm wild_hunter";

    public WildHunterEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B1A1A);
    }

    /**
     * 1.21.1 的 id 字符串 → 1.20.1 的 UUID：{@link UUID#nameUUIDFromBytes} 是纯函数，
     * 跨进程 / 跨存档稳定，且与 {@code Farmstorm.id(path)} 一一对应。
     */
    public static UUID modifierId(String path) {
        return UUID.nameUUIDFromBytes(Farmstorm.id(path).toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 三项百分比的唯一来源是 config（F-61 / V-13）：{@code wild_hunter.speedBonusPercent}（30.0）、
     * {@code attackSpeedBonusPercent}（30.0）、{@code attackDamageBonusPercent}（40.0）。
     * 取的是 {@code FarmstormConfig.bake()} 的快照而不是 {@code .get()}——本方法在两侧都会被调用（TECH §3.5），
     * 而 SERVER config 在客户端读不到，快照在未 bake 时回落到 §8.1 的默认值。
     *
     * <p>1.20.1 的钩子签名多两个参数（{@code LivingEntity} 与 {@code amplifier}）；本类只用
     * {@code attributes} 与 {@code amplifier}，前两个位置的实参不参与计算。
     */
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        apply(attributes, Attributes.MOVEMENT_SPEED, SPEED_ID,
                FarmstormConfig.wildHunterSpeedBonus(), amplifier);
        apply(attributes, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID,
                FarmstormConfig.wildHunterAttackSpeedBonus(), amplifier);
        apply(attributes, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID,
                FarmstormConfig.wildHunterAttackDamageBonus(), amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        clear(attributes, Attributes.MOVEMENT_SPEED, SPEED_ID);
        clear(attributes, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID);
        clear(attributes, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID);
    }

    /**
     * 与原版 {@code MobEffect.AttributeTemplate#create} 同构：量值按 amplifier + 1 倍。
     * 先按 id 卸再挂，同一条效果被重复施加（相位重设 / 续时）时不会叠加。
     * {@code addPermanentModifier} 是原版对效果修饰器的选择：1.20.1 读档时不重挂修饰器，加成靠存档里的修饰器恢复。
     */
    private static void apply(AttributeMap attributes, Attribute attribute, UUID id,
                              double percentPerLevel, int amplifier) {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        instance.addPermanentModifier(new AttributeModifier(id, MODIFIER_NAME,
                percentPerLevel * (amplifier + 1) / 100.0,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void clear(AttributeMap attributes, Attribute attribute, UUID id) {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}
