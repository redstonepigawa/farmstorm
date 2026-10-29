package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.joml.Matrix4fStack;

/**
 * F-82 的整屏灰度 pass（D-61）：{@code RenderGuiEvent.Post} 里对主 {@code RenderTarget} 跑一次全屏 pass，
 * 一次覆盖世界 + GUI（TECH §4.1 首选方案，不许换）。
 * <p>效果不在身时第一行就 return——灰度是异常状态而不是常驻后处理，常态必须零开销。
 */
public final class GrayscalePass {
    private static ShaderInstance shader;
    /** ping-pong 的临时 target：先 main → temp，再用 temp 作采样写回 main，避免读写同一个 target。 */
    private static RenderTarget temp;
    private static int frames;

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

        RenderTarget main = minecraft.getMainRenderTarget();
        RenderTarget source = ensureTemp(main.width, main.height);
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, source.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, main.width, main.height, 0, 0, main.width, main.height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        main.bindWrite(true);

        shader.setSampler("Sampler0", source.getColorTextureId());
        RenderSystem.setShader(() -> shader);
        if (++frames % 240 == 0) {
            Farmstorm.LOGGER.info("灰度 pass 第 {} 帧执行（active={}）", frames, WildHunterClientState.active());
        }

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

    private static RenderTarget ensureTemp(int width, int height) {
        if (temp == null || temp.width != width || temp.height != height) {
            if (temp != null) {
                temp.destroyBuffers();
            }
            temp = new TextureTarget(width, height, false, Minecraft.ON_OSX);
        }
        return temp;
    }
}
