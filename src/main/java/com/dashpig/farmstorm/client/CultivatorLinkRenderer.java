package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.network.CultivatorLinkPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 「核 ↔ 培养器」世界空间线框（F-240 / D-169 / 36 号 §2.8.3 / RT-45）。
 *
 * <p><b>全项目第一次在世界空间画线</b>，因此四条硬约束都写在这里：
 * <ol>
 *   <li><b>不自建 {@code RenderType}</b>：用 {@link RenderType#lines()} 自带的
 *       {@code POSITION_COLOR_NORMAL} + 线宽 1.0（{@code RenderType.java:599-613}）。</li>
 *   <li><b>不手写顶点</b>：用 {@link LevelRenderer#renderLineBox(PoseStack, VertexConsumer, AABB, float, float, float, float)}
 *       （{@code LevelRenderer.java:2252}）。手写顶点漏 {@code setNormal(...)} 会在
 *       {@code BufferBuilder.endLastVertex} 抛 {@code Missing elements in vertex} 崩客户端（RT-45）。</li>
 *   <li><b>不碰事件给的 {@code getPoseStack()}</b>：那会把后续阶段一起平移、整幅画面错位
 *       （36 号 §7 翻车点 7）。这里**新建**一个 {@code PoseStack}，只做世界 → 相机的平移。</li>
 *   <li><b>阶段</b>：{@code AFTER_PARTICLES}（36 号附录 A 第 9 项的「本文确定」）。{@code lines()} 的
 *       composite state 是 {@code TRANSLUCENT_TRANSPARENCY}，而 {@code AFTER_TRANSLUCENT_BLOCKS}
 *       的官方注释明说半透明在该阶段可能不正确、应改用 {@code AFTER_TRIPWIRE_BLOCKS} 或
 *       {@code AFTER_PARTICLES}（{@code RenderLevelStageEvent.java:179-185}）。</li>
 * </ol>
 *
 * <p><b>颜色只由 payload 的 {@code kind} 字节决定</b>（F-304 / D-210 / 54 号 §5.4）：培养器沿用核心边界
 * 粒子的同一组数值（{@code OutpostCoreBlockEntity.java:84} 的
 * {@code DustParticleOptions(new Vector3f(0.30F, 0.85F, 0.90F), 1.0F)}），转换器用红色。
 *
 * <p><b>红线 3 / N-143</b>：本类**不碰** {@code level} / {@code BlockState} / 方块实体。连线常画在远端
 * 未加载区块上，按坐标反查机器类型要么触发区块加载、要么猜——猜错就是服务端侧的闪色。
 * payload 里的那一个字节是唯一真相。
 */
public final class CultivatorLinkRenderer {
    /**
     * 培养器：核边界粒子的那三个分量（36 号 §2.8.3），**一个字节都不许变**（D-210 第 4 条）；
     * alpha 取 1.0，遮挡交给深度测试。
     */
    private static final float CULTIVATOR_RED = 0.30F;
    private static final float CULTIVATOR_GREEN = 0.85F;
    private static final float CULTIVATOR_BLUE = 0.90F;

    /**
     * 转换器：红（54 号 §5.4）。不用纯 {@code 0xFF0000} —— 纯红在高饱和草地与岩浆上会被同类色淹没，
     * 且与生命值条同色、读起来像「报错」；这三个分量与青逐一拉开 0.62 / 0.63 / 0.68，且都低于 1.0。
     */
    private static final float CONVERTER_RED = 0.92F;
    private static final float CONVERTER_GREEN = 0.22F;
    private static final float CONVERTER_BLUE = 0.22F;

    private static final float ALPHA = 1.0F;


    /** 端点小框边长 0.25 格（36 号附录 A 第 9 项）：以格心为中心，各方向 ±0.125。 */
    private static final double ENDPOINT_HALF = 0.125;

    private CultivatorLinkRenderer() {}

    /** 由 {@code FarmstormClient#onRenderStage} 挂载（与狂野猎人剪影共用同一个 {@code RenderLevelStageEvent}）。 */
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        List<CultivatorLinkPayload.Link> links = ClientCultivatorLinks.links();
        if (links.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());
        // 1.20.1 的相机变换只存在于 event 的 PoseStack 里（见 WildHunterOutline#onRenderStage 的注释：
        // shader 的 ModelViewMat 在世界阶段是 GUI 残留）。因此必须借用它，push/pop 包住，不会污染后续阶段。
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (CultivatorLinkPayload.Link link : links) {
            Vec3 core = center(link.core());
            Vec3 cultivator = center(link.cultivator());
            // 按 kind 选色（F-304 / 54 号 §5.4）：主线与两端的小框共用同一组，颜色不会一半青一半红。
            float red = CULTIVATOR_RED;
            float green = CULTIVATOR_GREEN;
            float blue = CULTIVATOR_BLUE;
            switch (link.kind()) {
                case CROP_CONVERTER -> {
                    red = CONVERTER_RED;
                    green = CONVERTER_GREEN;
                    blue = CONVERTER_BLUE;
                }
                case CULTIVATOR -> {
                    // 保持默认的青色（D-210 第 4 条：三个分量一个字节都不许变）
                }
            }
            LevelRenderer.renderLineBox(poseStack, consumer,
                    new AABB(core.x, core.y, core.z, cultivator.x, cultivator.y, cultivator.z),
                    red, green, blue, ALPHA);
            LevelRenderer.renderLineBox(poseStack, consumer, endpoint(link.core()), red, green, blue, ALPHA);
            LevelRenderer.renderLineBox(poseStack, consumer, endpoint(link.cultivator()), red, green, blue, ALPHA);
        }
        poseStack.popPose();
        buffers.endBatch(RenderType.lines());
    }

    /** 方块中心（36 号 §2.8.3：核中心 {@code (x+0.5, y+0.5, z+0.5)} → 培养器中心）。 */
    private static Vec3 center(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    /** 端点小框：0.25 格立方，让「哪一头是核」一眼可辨。 */
    private static AABB endpoint(BlockPos pos) {
        Vec3 center = center(pos);
        return new AABB(
                center.x - ENDPOINT_HALF, center.y - ENDPOINT_HALF, center.z - ENDPOINT_HALF,
                center.x + ENDPOINT_HALF, center.y + ENDPOINT_HALF, center.z + ENDPOINT_HALF);
    }
}
