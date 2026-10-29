package com.dashpig.farmstorm.client;

import java.io.IOException;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/**
 * 客户端四个钩子的唯一汇合点：{@code FarmstormClient}（调度者独占）只往这里转一行，
 * TECH §3.13 的「客户端代码只能从 FarmstormClient 进来」因此仍然成立。
 */
public final class WildHunterClientHooks {
    private WildHunterClientHooks() {}

    /** 每客户端刻：状态快照 → 冲刺预测 → 循环音。 */
    public static void onClientTick() {
        WildHunterClientState.refresh();
        WildHunterDashClient.onClientTick();
        WildHunterLoopSound.onClientTick();
    }

    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        GrayscalePass.onRegisterShaders(event);
    }

    public static void onRenderGuiPost() {
        GrayscalePass.onRenderGuiPost();
    }

    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        WildHunterOutline.onRenderLivingPost(event);
    }
}
