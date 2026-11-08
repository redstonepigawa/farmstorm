package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.client.event.RenderLevelStageEvent;

/**
 * F-82 的世界灰度。
 *
 * <p><b>v0.2.8 起改为自持 {@link PostChain}</b>，不再走 {@code GameRenderer#loadEffect}。
 * 原因是实机日志证明那条路在某些整合包里活不过一帧：
 * 光影模组（Oculus / Iris）会接管 {@code GameRenderer} 的后处理状态，
 * 我们装上去的链在约 50 ms 后就被清掉 —— 日志表现为「加载」与「被外部关闭」交替出现，
 * 一个会话刷出 700 余次装载，而画面从来没有灰过一帧。
 *
 * <p>现在的做法是把链的所有权握在自己手里：在 {@code AFTER_LEVEL} 阶段（世界画完、GUI 之前）
 * 直接 {@code process()}，谁的脸色都不用看。GUI 不在本阶段，所以仍然「只灰世界、不灰 GUI」（V-56 / V-57）。
 */
public final class GrayscalePass {
    /** 常态灰度。 */
    private static final ResourceLocation EFFECT = Farmstorm.id("shaders/post/grayscale.json");
    /** 水下灰度（提亮暗部 + 拉对比）。 */
    private static final ResourceLocation EFFECT_UNDERWATER =
            Farmstorm.id("shaders/post/grayscale_underwater.json");

    /** 自持的链；null = 当前没接管。 */
    private static PostChain chain;
    /** 当前链对应的资源路径，用于判断「要不要换链」。 */
    private static ResourceLocation chainEffect;
    /** 建链时的窗口尺寸，尺寸变了要 resize，否则采样错位。 */
    private static int chainWidth = -1;
    private static int chainHeight = -1;
    /** 连续失败次数：超过上限就彻底放弃本会话，避免每帧刷异常。 */
    private static int failures;
    private static final int MAX_FAILURES = 20;

    private GrayscalePass() {}

    /** 每客户端刻：只负责「效果没了 / 换了链」时释放资源，真正的装载发生在渲染阶段。 */
    public static void tick() {
        ResourceLocation desired = desired();
        if (desired == null || !desired.equals(chainEffect)) {
            closeChain();
        }
    }

    /** 需要哪条链；null 表示当前不该灰。 */
    private static ResourceLocation desired() {
        if (!WildHunterClientState.active()) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean underwater = minecraft.player != null && minecraft.player.isEyeInFluid(FluidTags.WATER);
        return underwater ? EFFECT_UNDERWATER : EFFECT;
    }

    /**
     * 世界渲染收尾时执行一次灰度。
     *
     * <p>选 {@code AFTER_LEVEL} 而不是更早的阶段：此时地形、实体、粒子、天气都已画进主目标，
     * 灰一遍才算「整个世界的画面」；而 GUI 在这个阶段之后才画，所以不会被一起灰掉。
     */
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        ResourceLocation desired = desired();
        if (desired == null || failures >= MAX_FAILURES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        try {
            if (chain == null || !desired.equals(chainEffect)) {
                closeChain();
                chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(),
                        minecraft.getMainRenderTarget(), desired);
                chainEffect = desired;
                chainWidth = minecraft.getWindow().getWidth();
                chainHeight = minecraft.getWindow().getHeight();
                chain.resize(chainWidth, chainHeight);
                Farmstorm.LOGGER.info("灰度 PostChain 自持装载 {}", desired.getPath());
            } else if (minecraft.getWindow().getWidth() != chainWidth
                    || minecraft.getWindow().getHeight() != chainHeight) {
                chainWidth = minecraft.getWindow().getWidth();
                chainHeight = minecraft.getWindow().getHeight();
                chain.resize(chainWidth, chainHeight);
            }
            chain.process(event.getPartialTick());
            failures = 0;
        } catch (Exception exception) {
            failures++;
            Farmstorm.LOGGER.warn("灰度 PostChain 处理失败（第 {} 次），本帧跳过", failures, exception);
            closeChain();
            if (failures >= MAX_FAILURES) {
                Farmstorm.LOGGER.error("灰度 PostChain 连续失败 {} 次，本会话不再尝试", failures);
            }
        }
    }

    /** 释放链；这是唯一的释放点。 */
    private static void closeChain() {
        if (chain != null) {
            try {
                chain.close();
            } catch (Exception exception) {
                Farmstorm.LOGGER.warn("灰度 PostChain 关闭时抛异常（忽略）", exception);
            }
            chain = null;
            chainEffect = null;
            chainWidth = -1;
            chainHeight = -1;
        }
    }

    /** 断开世界 / 退出时调用。 */
    public static void reset() {
        closeChain();
        failures = 0;
    }
}
