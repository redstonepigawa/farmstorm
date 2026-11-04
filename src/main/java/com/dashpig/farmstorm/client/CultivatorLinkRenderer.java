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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

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
 * <p><b>颜色沿用核心边界粒子的同一组数值</b>（{@code OutpostCoreBlockEntity.java:84} 的
 * {@code DustParticleOptions(new Vector3f(0.30F, 0.85F, 0.90F), 1.0F)}）：同一视觉语义（核心范围）
 * 用同一个颜色，不发明新数值。
 */
public final class CultivatorLinkRenderer {
    /** 核边界粒子的那三个分量（36 号 §2.8.3）；alpha 取 1.0，遮挡交给深度测试。 */
    private static final float RED = 0.30F;
    private static final float GREEN = 0.85F;
    private static final float BLUE = 0.90F;
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
        // 新建 PoseStack，只平移到相机坐标系；**不用** event.getPoseStack()（会把后续阶段一起平移）。
        PoseStack poseStack = new PoseStack();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (CultivatorLinkPayload.Link link : links) {
            Vec3 core = center(link.core());
            Vec3 cultivator = center(link.cultivator());
            LevelRenderer.renderLineBox(poseStack, consumer,
                    new AABB(core.x, core.y, core.z, cultivator.x, cultivator.y, cultivator.z),
                    RED, GREEN, BLUE, ALPHA);
            LevelRenderer.renderLineBox(poseStack, consumer, endpoint(link.core()), RED, GREEN, BLUE, ALPHA);
            LevelRenderer.renderLineBox(poseStack, consumer, endpoint(link.cultivator()), RED, GREEN, BLUE, ALPHA);
        }
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
