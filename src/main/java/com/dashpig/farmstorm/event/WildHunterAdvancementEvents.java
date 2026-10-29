package com.dashpig.farmstorm.event;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * D-63 / DESIGN §9.2 第 3 条：{@code first_wild_hunter} = 玩家**首次获得** {@code farmstorm:wild_hunter}
 * （**不限定来源**：吃狂野苹果、{@code /effect}、其它模组给药都算）。
 *
 * <p>所以不能挂在「狂野苹果被吃」那一处——那只能覆盖一个来源。这里挂 NeoForge 的
 * {@link MobEffectEvent.Added}：它带 {@code getOldEffectInstance()}，为空才表示「之前没有这个效果」，
 * 正好对应「首次获得」；续时 / 刷新（old 非空）不会重复推进，进度本身也会去重。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class WildHunterAdvancementEvents {

    private WildHunterAdvancementEvents() {}

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;                                   // 判定只在服务端（HC-03）
        }
        if (event.getOldEffectInstance() != null) {
            return;                                   // 之前就有这个效果 ⇒ 不是「首次获得」
        }
        MobEffectInstance added = event.getEffectInstance();
        if (added == null || added.getEffect().value() != FarmstormMobEffects.WILD_HUNTER.get()) {
            return;
        }
        FarmstormAdvancements.award(player, "first_wild_hunter");
    }
}
