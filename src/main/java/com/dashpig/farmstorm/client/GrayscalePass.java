package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/**
 * F-82 的整屏灰度 pass（D-61）：{@code RenderGuiEvent.Post} 里对主 {@code RenderTarget} 跑一次全屏 pass，
 * 一次覆盖世界 + GUI（TECH §4.1 首选方案，不许换）。
 * <p>效果不在身时第一行就 return——灰度是异常状态而不是常驻后处理，常态必须零开销。
 */
public final class GrayscalePass {
    private static ShaderInstance shader;

    private GrayscalePass() {}

    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Farmstorm.id("grayscale"),
                DefaultVertexFormat.POSITION_TEX), instance -> {
                    shader = instance;
                    Farmstorm.LOGGER.info("灰度 shader 注册完成：{}", Farmstorm.id("grayscale"));
                });
    }

    public static void onRenderGuiPost() {
        if (!WildHunterClientState.active() || shader == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        float width = window.getWidth();
        float height = window.getHeight();
        RenderSystem.assertOnRenderThread();

        shader.setSampler("Sampler0", minecraft.getMainRenderTarget().getColorTextureId());
        RenderSystem.setShader(() -> shader);

        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.identity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(
                new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 3000.0F),
                VertexSorting.ORTHOGRAPHIC_Z);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(0.0F, height, 0.0F).setUv(0.0F, 1.0F);
        buffer.addVertex(width, height, 0.0F).setUv(1.0F, 1.0F);
        buffer.addVertex(width, 0.0F, 0.0F).setUv(1.0F, 0.0F);
        buffer.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 0.0F);
        MeshData mesh = buffer.buildOrThrow();
        BufferUploader.drawWithShader(mesh);

        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
}
