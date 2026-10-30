package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

/**
 * 混乱的客户端输入反转（TECH §5）：只反转方向分量，幅度不变；与 {@code InputLockHandler} 冲突时锁优先（§5.2）。
 *
 * <p><b>锁优先必须显式写，不靠事件顺序</b>：两个 {@code @EventBusSubscriber} 订阅同一个事件没有约定的执行顺序
 * （§5.2 第 3 段），所以本方法的第一个语句就是早退；锁的判据只有 {@link InputLockHandler#isLocked()} 一处，
 * 不另写第二套锁（D-79）。
 * <p><b>只碰两个方向分量</b>：{@code forwardImpulse}（正向前进，取反即 W↔S）与 {@code leftImpulse}
 * （正向向左，取反即 A↔D）各乘 −1，幅度逐字不变，因此不碰速度属性；跳跃 / 潜行 / 视角 / 攻击 / 使用一律不碰
 * （§5.1 的「不碰什么」一栏）。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class ConfusionInputHandler {

    private ConfusionInputHandler() {}

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (InputLockHandler.isLocked()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.hasEffect(FarmstormMobEffects.CONFUSION)) {
            return;
        }
        Input input = event.getInput();
        input.forwardImpulse = -input.forwardImpulse;
        input.leftImpulse = -input.leftImpulse;
    }
}
