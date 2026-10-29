package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/**
 * F-83 的红色剪影（D-62）：只画可见的、{@code wild_hunter.highlightRadius}（默认 64 格）内的生物。
 * <p>用 {@code RenderLivingEvent.Post} 再画一遍模型，配纯白贴图 + {@code setShaderColor} 上红色（BRIEF §7.2），
 * 因此不需要新模型或新贴图。
 */
public final class WildHunterOutline {
    /** 剪影底色贴图：1×1 纯白（BRIEF §7.2）。 */
    public static final ResourceLocation WHITE_TEXTURE = Farmstorm.id("textures/misc/white.png");
    /** D-62 的红色（1, 0.15, 0.15）与 0.45 透明度，打包成 ARGB 交给模型。 */
    private static final int COLOR = 0x73FF2626;

    private WildHunterOutline() {}

    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!WildHunterClientState.active()) {
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
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(WHITE_TEXTURE));
        poseStack.pushPose();
        event.getRenderer().getModel().renderToBuffer(poseStack, consumer, event.getPackedLight(),
                OverlayTexture.NO_OVERLAY, COLOR);
        poseStack.popPose();
    }
}
