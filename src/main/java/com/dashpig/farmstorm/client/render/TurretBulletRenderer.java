package com.dashpig.farmstorm.client.render;

import com.dashpig.farmstorm.entity.TurretBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 炮台导弹渲染（TECH §4.7 / §4.8.4 的 client 分包）：直接复用潜影贝导弹的模型与贴图，
 * 因此不需要为本模组新增任何模型 JSON 或 PNG（§6 的资源清单里也没有它）。
 */
public class TurretBulletRenderer extends EntityRenderer<TurretBullet> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/shulker/spark.png");
    private static final RenderType GLOW_RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private static final int GLOW_ALPHA = 654311423;
    private static final float SPIN_SPEED = 0.1F;
    private static final float ROLL_SPEED = 0.15F;

    private final ShulkerBulletModel<TurretBullet> model;

    public TurretBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShulkerBulletModel<>(context.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    @Override
    public void render(TurretBullet entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float spin = (float) entity.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.15F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(spin * SPIN_SPEED) * 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(spin * SPIN_SPEED) * 180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(spin * ROLL_SPEED) * 360.0F));
        poseStack.scale(-0.5F, -0.5F, 0.5F);
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, yaw, pitch);
        VertexConsumer solid = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, solid, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        VertexConsumer glow = buffer.getBuffer(GLOW_RENDER_TYPE);
        this.model.renderToBuffer(poseStack, glow, packedLight, OverlayTexture.NO_OVERLAY, GLOW_ALPHA);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TurretBullet entity) {
        return TEXTURE;
    }
}
