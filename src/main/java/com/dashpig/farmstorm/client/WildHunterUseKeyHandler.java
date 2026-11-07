package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.InputEvent;

/**
 * 右键四向位移的**输入拦截**（作者 v0.0.2 #4）。
 *
 * <p>只在「带 {@code wild_hunter} + 有充能 + 有 WASD 输入」三条同时成立时吃掉这次右键，
 * 其余情况一律放行（吃东西 / 放方块 / 开箱子不受影响）——判定写在
 * {@link WildHunterDashClient#onUseKey()}，本类只负责把它的布尔结果翻译成「取消事件」。
 *
 * <p>自注册而不是写进 {@code FarmstormClient}（调度者独占文件）：与 {@code ZangiefCameraController}
 * 同一套写法，省掉一次跨人接线。{@code FarmstormClient} 里已有的 handler 只处理 {@code isAttack()}，
 * 与本类不重叠。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class WildHunterUseKeyHandler {

    private WildHunterUseKeyHandler() {}

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }
        if (WildHunterDashClient.onUseKey()) {
            event.setCanceled(true);                   // 吃掉这次右键：物品不会被打出去
        }
    }
}
