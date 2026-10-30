package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;

/**
 * F-82 的世界灰度，走**官方 PostChain 机制**：{@code GameRenderer#loadEffect} + {@code shutdownEffect}
 * —— 原版 creeper / spider 效果用的就是这一套，投影、属性绑定与 ping-pong target 全由框架处理。
 * <p>已知限制：{@code process()} 在 {@code renderLevel} 之后、GUI 绘制之前执行，所以**只灰世界，不灰 GUI**
 * （V-56 的设计选择）。
 * <p><b>水下换一张链</b>（V-81）：水下画面偏暗、低对比，普通灰度会把细节糊成均匀一片灰色
 * （作者实测「完全看不见」）。所以眼睛在水里时改用 {@code grayscale_underwater}：
 * 仍是黑白，但提亮暗部 + 拉开对比，保留轮廓。浮出水面立刻换回普通灰度。
 */
public final class GrayscalePass {
    /** 常态灰度。 */
    private static final ResourceLocation EFFECT = Farmstorm.id("shaders/post/grayscale.json");
    /** 水下灰度（提亮暗部 + 拉对比）。 */
    private static final ResourceLocation EFFECT_UNDERWATER =
            Farmstorm.id("shaders/post/grayscale_underwater.json");

    /** 当前由本类加载的链；null 表示没接管。只关自己加载过的那条，不碰原版 creeper/spider 的链。 */
    private static ResourceLocation loaded;

    private GrayscalePass() {}

    /** 每客户端刻：按「效果在身 / 是否在水下」选链，状态变化时才动 GL。 */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean underwater = minecraft.player != null && minecraft.player.isEyeInFluid(FluidTags.WATER);
        ResourceLocation desired = WildHunterClientState.active()
                ? (underwater ? EFFECT_UNDERWATER : EFFECT)
                : null;

        if (loaded != null && !loaded.equals(desired)) {
            minecraft.gameRenderer.shutdownEffect();
            Farmstorm.LOGGER.info("灰度 PostChain 卸载 {}", loaded.getPath());
            loaded = null;
        }
        if (desired != null && loaded == null && minecraft.gameRenderer.currentEffect() == null) {
            minecraft.gameRenderer.loadEffect(desired);
            loaded = desired;
            Farmstorm.LOGGER.info("灰度 PostChain 加载 {}", desired.getPath());
        }
    }
}
