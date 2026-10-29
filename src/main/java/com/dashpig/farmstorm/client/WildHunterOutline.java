package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/**
 * F-83 / F-83b 的红色剪影（D-62）：只画可见的、{@code wild_hunter.highlightRadius}（默认 64 格）内的生物。
 * <p>做法是**让渲染器自己再画一遍**（{@code renderer.render(...)}）：变换链由渲染器执行，
 * 几何与本体必然重合，不再需要在 Post 里手动重放 {@code scale(entity,…)} 这类 protected 变换。
 * 纹理一律换成 1×1 纯白 + {@code setShaderColor} 上红色，光照 {@code FULL_BRIGHT} 保证任意亮度可见。
 */
public final class WildHunterOutline {
    /**
     * 剪影贴图：1×1 RGBA **(255, 38, 38, 115)** —— D-62 的 (1, 0.15, 0.15) 与 0.45 透明度直接烘进贴图。
     * <p>为什么不 {@code setShaderColor}：{@code entityTranslucentEmissive} 的着色器**故意忽略 ColorModulator**
     * （emissive 的定义就是不受光照与色彩调制器影响），所以颜色必须来自贴图本身。
     */
    public static final ResourceLocation SILHOUETTE_TEXTURE =
            Farmstorm.id("textures/misc/wild_hunter_red.png");
    /** 重画时轻微放大，避免与本体 z-fighting。 */
    private static final float OUTLINE_SCALE = 1.008F;
    /** 重入守卫：renderer.render(...) 内部会再触发 RenderLivingEvent.Pre/Post，不守卫会无限递归。 */
    private static boolean rendering = false;

    private WildHunterOutline() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (rendering || !WildHunterClientState.active()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();
        Player self = minecraft.player;
        if (self == null || entity == self || !entity.isAlive()) {
            return;
        }
        if (entity instanceof Player other && self.isAlliedTo(other)) {
            return;
        }
        double radius = WildHunterClientState.highlightRadius();
        if (self.distanceToSqr(entity) > radius * radius) {
            return;
        }
        if (!self.hasLineOfSight(entity)) {
            return;
        }
        MultiBufferSource buffers = event.getMultiBufferSource();
        if (buffers == null) {
            return;
        }
        LivingEntityRenderer renderer = (LivingEntityRenderer) event.getRenderer();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource shell = type -> buffers.getBuffer(RenderType.entityTranslucentEmissive(SILHOUETTE_TEXTURE));
        float yaw = Mth.lerp(event.getPartialTick(), entity.yRotO, entity.getYRot());
        rendering = true;
        try {
            poseStack.pushPose();
            poseStack.scale(OUTLINE_SCALE, OUTLINE_SCALE, OUTLINE_SCALE);
            renderer.render(entity, yaw, event.getPartialTick(), poseStack, shell, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        } finally {
            rendering = false;
        }
    }
}
