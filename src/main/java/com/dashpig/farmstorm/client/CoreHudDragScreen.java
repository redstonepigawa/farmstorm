package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.config.FarmstormClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 拖拽定位（D-144 第 4 条 / 30 号 §2.4.5）：按 J 打开，拖动改预览坐标，{@code ESC} 才写盘。
 *
 * <p>五条口径：
 * <ul>
 *   <li>{@link #isPauseScreen()} 返回 <b>true</b>（30 号 §5.5 第 9 条）：单人世界里拖拽时世界暂停；</li>
 *   <li>{@link #mouseReleased} **不写盘**（30 号 §5.5 第 8 条）——一次拖动会有几百次位置变化，逐帧写盘没有意义；</li>
 *   <li>写盘只在 {@link #onClose()}（ESC / 关闭）：{@code x} / {@code y} = 当前预览坐标；取消路径不存在；</li>
 *   <li>**不画面板本体**：面板仍由 {@link CoreHudRenderer} 的「编辑态例外」负责，保证拖的时候看到的就是最终效果；</li>
 *   <li>{@link #active()} 为真时渲染器的 {@code hideGui} / {@code screen != null} 两条早退失效（30 号 §5.5 第 7 条）。</li>
 * </ul>
 */
public final class CoreHudDragScreen extends Screen {
    /** 拖拽中生效的预览坐标（未拖拽时等于 config 值）。 */
    private static int previewX = FarmstormClientConfig.DEFAULT_HUD_X;
    private static int previewY = FarmstormClientConfig.DEFAULT_HUD_Y;
    /** 编辑态：渲染器的两条早退据此失效。 */
    private static boolean active;

    private boolean dragging;
    private double grabDx;
    private double grabDy;

    public CoreHudDragScreen() {
        super(Component.empty());                          // 标题不渲染，只满足 Screen 的构造形态
    }

    /** 编辑态（渲染器读）。 */
    public static boolean active() {
        return active;
    }

    /** 预览 x（渲染器读）。 */
    public static int previewX() {
        return previewX;
    }

    /** 预览 y（渲染器读）。 */
    public static int previewY() {
        return previewY;
    }

    @Override
    protected void init() {
        if (!active) {                                     // 窗口缩放会再进 init()，别把拖了一半的预览冲掉
            previewX = FarmstormClientConfig.hudX.get();
            previewY = FarmstormClientConfig.hudY.get();
        }
        active = true;
        this.dragging = false;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);   // 只画背景（模糊 / 暗化），面板由渲染器画
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] size = CoreHudRenderer.panelSize(this.font);
        int x = CoreHudRenderer.clampX(previewX, size[0], this.width);
        int y = CoreHudRenderer.clampY(previewY, size[1], this.height);
        if (mouseX >= x && mouseX < x + size[0] && mouseY >= y && mouseY < y + size[1]) {
            this.dragging = true;                          // 只有落在面板内才开始拖；落在外面忽略，也不关界面
            this.grabDx = mouseX - previewX;
            this.grabDy = mouseY - previewY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!this.dragging) {
            return false;
        }
        int[] size = CoreHudRenderer.panelSize(this.font);
        previewX = CoreHudRenderer.clampX((int) Math.round(mouseX - this.grabDx), size[0], this.width);
        previewY = CoreHudRenderer.clampY((int) Math.round(mouseY - this.grabDy), size[1], this.height);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;                             // 只结束拖动，**不写盘**
        return true;
    }

    @Override
    public void onClose() {
        active = false;
        // 唯一写盘点：位置生效，enabled 原样写回（本界面不碰开关）。
        FarmstormClientConfig.saveHudState(FarmstormClientConfig.hudEnabled.get(), previewX, previewY);
        super.onClose();
    }
}
