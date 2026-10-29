package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 狂野猎人的服务端挂载点（F-80 / F-80b / F-81）。
 * <ul>
 *   <li>F-80：{@link AttackEntityEvent}（原版攻击流程的服务端结算点）→ 复核目标、结算伤害与击退；</li>
 *   <li>F-80b：{@link PlayerTickEvent.Post} → {@link WildHunterSwingSettler} 用「swing 跃变 + 冲刺位移」感知吸附挥击；</li>
 *   <li>F-81：{@link LivingDeathEvent} → 击杀续时。</li>
 * </ul>
 * <p>1.21.1 没有 {@code ServerboundSwingPacket} 的公开事件、D-40/D-50 又禁 Mixin 与自订包，
 * 所以 F-80b 用「服务端可观测的双信号」补齐 BRIEF §7.6 的「空挥也必中」。
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
        if (directlyHit != null) {
            WildHunterSwingSettler.recordDirectHit(player);
        }
        WildHunterCombat.settleSwing(player, directlyHit);
    }

    /** F-80b：吸附挥击（空挥也算）的服务端结算入口。 */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WildHunterSwingSettler.tick(player);
        }
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
