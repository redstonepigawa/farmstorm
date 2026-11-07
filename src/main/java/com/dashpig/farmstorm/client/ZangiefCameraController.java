package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/**
 * 桑吉尔蔗的相机（TECH §4.4 / D-59 / D-70，作者实测改版）：相位 2 起 roll 180° + pitch 取反并保持到结束，
 * yaw 自转在倒转后 {@code spinDelayTicks}（默认 4）刻开始、持续到结束。
 * <p><b>铁律</b>：只改渲染层角度，绝不写回 {@code player.getYRot()} / {@code getXRot()}——
 * 写回等于把被倒转的朝向发给服务端，玩家的真实面朝方向被篡改，服务端权威就破了。
 * <p>状态全部来自 {@link ZangiefClientState} 的快照，本类不读效果、不读 config。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class ZangiefCameraController {

    private ZangiefCameraController() {}

    /**
     * <b>必须是 {@code EventPriority.LOW}</b>：{@link InputLockHandler#onTurn} 在锁定期内会把 yaw/pitch
     * 钉回冻结值（NORMAL 优先级，先执行）。桑吉尔蔗的三个相位本来就在锁定期内，若本处理器也走默认优先级，
     * 它算出来的自转与 pitch 取反会被 InputLockHandler 整段覆盖 —— 现象是「自转累计在日志里正常、
     * 画面却完全不动」。LOW 让本处理器排在冻结之后，把效果叠加在冻结后的基准上。
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!ZangiefClientState.active()) {
            return;                                    // §5.6 断言 3：无效果时三个渲染钩子什么都不做
        }
        // 作者 v0.0.2 实测口径：上下倒转**从相位 2 起保持到结束**；yaw 自转在倒转后 spinDelayTicks 刻才加入。
        if (ZangiefClientState.inverted()) {
            event.setRoll(180.0F);                     // D-59：绕视线轴滚 180°（上下颠倒）
            event.setPitch(-event.getPitch());         // D-59：pitch 取反，与 roll 同时生效
        }
        if (ZangiefClientState.spinning()) {
            event.setYaw(event.getYaw() + ZangiefClientState.accumulatedDegrees());   // D-70：逆时针自转
        }
    }
}
