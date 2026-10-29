package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * 狂野猎人的服务端挂载点（F-80 / F-81）。
 * <p>没有一个 {@code ServerboundSwingPacket} 的公开事件可挂（D-50 又禁止自订客户端包），
 * 所以按 TECH §3.12 的口径挂在「原版攻击流程的服务端结算点」——{@link AttackEntityEvent}，
 * 复核逻辑本体在 {@link WildHunterCombat#settleSwing}。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class WildHunterCombatEvents {
    private WildHunterCombatEvents() {}

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LivingEntity directlyHit = event.getTarget() instanceof LivingEntity living ? living : null;
        WildHunterCombat.settleSwing(player, directlyHit);
    }

    /** F-81 / D-74：killer 必须是持有效果的玩家本人；不设计数器。 */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getEntity() == player) {
            return;
        }
        WildHunterCombat.extendOnKill(player);
    }
}
