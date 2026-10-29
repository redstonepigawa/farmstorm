package com.dashpig.farmstorm.client;

/**
 * 客户端每刻的汇合点：{@code FarmstormClient}（调度者独占）把 {@code ClientTickEvent.Post} 转到这里。
 * <p>剪影的两个渲染钩子不走本类——{@code onRenderStage}（{@code RenderLevelStageEvent}）与
 * {@code onRenderGuiPre}（{@code RenderGuiEvent.Pre}）由 {@code FarmstormClient} 直接转给
 * {@link WildHunterOutline}。
 */
public final class WildHunterClientHooks {
    private WildHunterClientHooks() {}

    /** 每客户端刻：状态快照 → 冲刺预测 → 循环音 → 灰度 PostChain 装载/卸载。 */
    public static void onClientTick() {
        WildHunterClientState.refresh();
        WildHunterDashClient.onClientTick();
        WildHunterLoopSound.onClientTick();
        GrayscalePass.tick();
        GammaBoost.tick();
    }
}
