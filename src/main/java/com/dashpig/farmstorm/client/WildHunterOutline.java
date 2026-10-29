package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

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
    /** AFTER_ENTITIES 时捕获的渲染状态：GUI 阶段没有 EntityRenderDispatcher 的相机偏移，必须自己补。 */
    private static Matrix4f capturedProjection;
    private static Matrix4f capturedModelView;
    private static Camera capturedCamera;
    private static float capturedPartialTick;

    private WildHunterOutline() {}

    /**
     * 只把**模型本体那一类**的 RenderType 换成红色单色类型，其余原样放行。
     * <p>必须按顶点格式过滤：拴绳（{@code RenderType.LEASH}）用
     * {@code DefaultVertexFormat.POSITION_COLOR_LIGHTMAP}，没有 UV0/UV1/Normal；
     * 把它塞进 entity 格式的缓冲区会在 {@code BufferBuilder.endLastVertex} 抛
     * {@code IllegalStateException: Missing elements in vertex}，客户端直接崩（v0.0.2 实测崩过两次）。
     */
    private static MultiBufferSource shellSource(MultiBufferSource buffers) {
        return type -> type.format() == DefaultVertexFormat.NEW_ENTITY
                ? buffers.getBuffer(RenderType.entityTranslucentEmissive(SILHOUETTE_TEXTURE))
                : buffers.getBuffer(type);
    }

    /** 世界阶段捕获投影 / modelview / 相机 / partialTick：{@code AFTER_ENTITIES} 时这些都还是对的。 */
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        capturedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        capturedModelView = new Matrix4f(RenderSystem.getModelViewMatrix());
        capturedCamera = event.getCamera();
        capturedPartialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
    }

    /** GUI 阶段（灰度之后）重画剪影：先试 {@code RenderGuiEvent.Pre}，不成再换 {@code Post}。 */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRenderGuiPre() {
        if (!WildHunterClientState.active() || capturedProjection == null || capturedCamera == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player self = minecraft.player;
        if (minecraft.level == null || self == null) {
            return;
        }
        double radius = WildHunterClientState.highlightRadius();
        AABB area = self.getBoundingBox().inflate(radius);
        var targets = minecraft.level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != self && entity.isAlive() && !(entity instanceof Player other && self.isAlliedTo(other))
                        && self.distanceToSqr(entity) <= radius * radius && self.hasLineOfSight(entity));
        if (targets.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Vec3 cameraPos = capturedCamera.getPosition();
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(capturedProjection, VertexSorting.DISTANCE_TO_ORIGIN);
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.set(capturedModelView);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        rendering = true;
        try {
            for (LivingEntity entity : targets) {
                // modelview 已经是相机旋转（capturedModelView），所以 PoseStack 只做世界平移。
                // 之前这里还 mulPose(cameraRotation)，等于把旋转应用了两次 → 剪影画到错误位置。
                PoseStack poseStack = new PoseStack();
                poseStack.translate(entity.getX() - cameraPos.x, entity.getY() - cameraPos.y, entity.getZ() - cameraPos.z);
                poseStack.scale(OUTLINE_SCALE, OUTLINE_SCALE, OUTLINE_SCALE);
                LivingEntityRenderer renderer =
                        (LivingEntityRenderer) minecraft.getEntityRenderDispatcher().getRenderer(entity);
                MultiBufferSource shell = shellSource(buffers);
                float yaw = Mth.lerp(capturedPartialTick, entity.yRotO, entity.getYRot());
                renderer.render(entity, yaw, capturedPartialTick, poseStack, shell, LightTexture.FULL_BRIGHT);
            }
            buffers.endBatch();
        } finally {
            stack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.enableBlend();
            rendering = false;
        }
    }
}
