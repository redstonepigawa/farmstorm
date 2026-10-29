package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * F-82 的整屏灰度，走**官方 PostChain 机制**：{@code GameRenderer#loadEffect} + {@code shutdownEffect}
 * —— 原版 creeper / spider 效果用的就是这一套，投影、属性绑定与 ping-pong target 全由框架处理。
 * <p>已知限制：{@code process()} 在 {@code renderLevel} 之后、GUI 绘制之前执行，所以**只灰世界，不灰 GUI**
 * （与 D-61「整屏含 GUI」冲突，待作者裁决；不为此回去手写全屏 pass）。
 */
public final class GrayscalePass {
    private static final ResourceLocation EFFECT = Farmstorm.id("shaders/post/grayscale.json");
    /** 只关掉自己加载过的那条链：原版 creeper/spider 的链不能被我们 shutdown。 */
    private static boolean loaded;

    private GrayscalePass() {}

    /** 每客户端刻：效果在身就加载，撤销就卸载。 */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean wanted = WildHunterClientState.active();
        if (wanted && !loaded) {
            if (minecraft.gameRenderer.currentEffect() == null) {
                minecraft.gameRenderer.loadEffect(EFFECT);
                loaded = true;
                Farmstorm.LOGGER.info("灰度 PostChain 加载 {}", EFFECT);
            }
        } else if (!wanted && loaded) {
            minecraft.gameRenderer.shutdownEffect();
            loaded = false;
            Farmstorm.LOGGER.info("灰度 PostChain 卸载");
        }
    }
}
