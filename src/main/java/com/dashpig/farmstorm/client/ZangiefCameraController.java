package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 桑吉尔蔗的相机（TECH §4.4 / D-59 / D-70）：roll 180° + pitch 取反（三个相位都开）+ yaw 自转（只在相位 2）。
 * <p><b>铁律</b>：只改渲染层角度，绝不写回 {@code player.getYRot()} / {@code getXRot()}——
 * 写回等于把被倒转的朝向发给服务端，玩家的真实面朝方向被篡改，服务端权威就破了。
 * <p>状态全部来自 {@link ZangiefClientState} 的快照，本类不读效果、不读 config。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class ZangiefCameraController {

    private ZangiefCameraController() {}

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!ZangiefClientState.active()) {
            return;                                    // §5.6 断言 3：无效果时三个渲染钩子什么都不做
        }
        event.setRoll(180.0F);                         // D-59：绕视线轴滚 180°（上下颠倒）
        event.setPitch(-event.getPitch());             // D-59：pitch 取反，与 roll 同时生效
        if (ZangiefClientState.spinning()) {
            event.setYaw(event.getYaw() + ZangiefClientState.accumulatedDegrees());   // D-70：逆时针自转
        }
        if (Farmstorm.LOGGER.isDebugEnabled()) {
            Farmstorm.LOGGER.debug("桑吉尔蔗 相机 roll={} pitch={} yaw={}", event.getRoll(), event.getPitch(), event.getYaw());
        }
    }
}
