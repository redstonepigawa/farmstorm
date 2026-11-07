package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * 狂野猎人的武器规则（作者 9 条里的 #5 / #6）。
 * <ul>
 *   <li>#5 手持剑：单次攻击伤害**上限 5**（超出部分截断）。</li>
 *   <li>#6 手持斧：**无视 50 % 盔甲** —— 把「全额盔甲减免」与「半额盔甲减免」的差值补回伤害。</li>
 * </ul>
 * 两条都只在「攻击者是带 {@code wild_hunter} 的玩家」时生效。
 */
@EventBusSubscriber(modid = "farmstorm")
public final class WildHunterDamage {
    private WildHunterDamage() {}

    @SubscribeEvent
    public static void onIncoming(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.hasEffect(FarmstormMobEffects.WILD_HUNTER.get())
               ) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        LivingEntity target = event.getEntity();
        float amount = event.getAmount();
        if (held.is(ItemTags.SWORDS)) {
            if (amount > WildHunterAura.SWORD_DAMAGE_CAP) {
                event.setAmount(WildHunterAura.SWORD_DAMAGE_CAP);
            }
        } else if (held.is(ItemTags.AXES)) {
            double pierced = armorReduction(target, amount, 1.0) - armorReduction(target, amount, 0.5);
            if (pierced > 0.0) {
                event.setAmount((float) (amount + pierced));
            }
        }
    }

    /** #5：命中非玩家生物 → 失去 AI 2 刻（玩家目标不受影响）。 */
    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.hasEffect(FarmstormMobEffects.WILD_HUNTER.get())
                || event.getEntity() instanceof Player) {
            return;
        }
        WildHunterAura.loseAi(event.getEntity(), event.getEntity().level().getGameTime());
    }

    /**
     * 原版 1.21.1 的盔甲减免量：{@code damage * min(20, max(armor/5, armor - damage/(2 + toughness/4))) / 25}。
     *
     * @param factor 1.0 = 全额盔甲；0.5 = 只算一半盔甲
     */
    private static double armorReduction(LivingEntity target, float damage, double factor) {
        double armor = target.getArmorValue() * factor;
        double toughness = target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * factor;
        double applied = Math.max(armor / 5.0, armor - damage / (2.0 + toughness / 4.0));
        return damage * Math.min(20.0, applied) / 25.0;
    }
}
