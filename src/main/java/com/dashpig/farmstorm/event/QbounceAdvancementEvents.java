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
 * D-63 的扩充 / DESIGN §10.1 #16 与 §10.2 第 1 条：{@code first_q_bounce} = 玩家**首次获得**
 * {@code farmstorm:q_bounce}，**不限定来源**（朋友的韭命中、{@code /effect}、其它模组给药都算）。
 *
 * <p>挂法与 0.0.2 的 {@link WildHunterAdvancementEvents} 同口径：{@link MobEffectEvent.Added} 带
 * {@code getOldEffectInstance()}，它为空才表示「之前没有这个效果」；续时 / 刷新不重复推进。
 * 施加对象只认 {@link ServerPlayer}——§10.1 #16 的口径是「任意玩家」，生物身上的 Q弹 不推进本进度。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class QbounceAdvancementEvents {

    private QbounceAdvancementEvents() {}

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;                                   // 判定只在服务端（HC-03）
        }
        if (event.getOldEffectInstance() != null) {
            return;                                   // 之前就有这个效果 ⇒ 不是「首次获得」
        }
        MobEffectInstance added = event.getEffectInstance();
        if (added == null || added.getEffect().value() != FarmstormMobEffects.Q_BOUNCE.get()) {
            return;
        }
        FarmstormAdvancements.award(player, "first_q_bounce");
    }
}
