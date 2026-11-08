package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.TickEvent;

/**
 * 狂野猎人的服务端挂载点（F-80 / F-80b / F-81）。
 * <ul>
 *   <li>F-80：{@link AttackEntityEvent}（原版攻击流程的服务端结算点）→ 复核目标、结算伤害与击退；</li>
 *   <li>F-80b：{@link TickEvent.PlayerTickEvent} → {@link WildHunterSwingSettler} 用「swing 跃变 + 冲刺位移」感知吸附挥击；</li>
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
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            WildHunterAura.tick(player);
            WildHunterSwingSettler.tick(player);
        }
    }

    /** #5：每刻扫一遍「被剥夺 AI」的到期项（与玩家脱钩，掉线也不会漏恢复）。 */
    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        // 全维度扫描：只传主世界会让下界 / 末地被打中的生物永远停在 NoAI（不能移动 + 悬空）。
        WildHunterAura.sweep(event.getServer());
    }

    /** 实体进入世界时补一次 AI 恢复（区块卸载导致的漏网，见 {@link WildHunterAura#restoreIfDue}）。 */
    @SubscribeEvent
    public static void onEntityJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.Mob mob) {
            WildHunterAura.restoreIfDue(mob);
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
