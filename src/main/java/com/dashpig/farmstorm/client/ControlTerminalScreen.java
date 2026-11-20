package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity.Mode;
import com.dashpig.farmstorm.menu.ControlTerminalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 控制终端的面板屏（F-338 / 66 号 §4.5）：<b>两个模式按钮 + 一行状态</b>，0 槽、0 贴图。
 *
 * <p>本版 GUI 贴图新增 <b>0 张</b>：底色与 1 像素描边都用 {@link GuiGraphics#fill} 画，
 * 选中框同法（{@link #drawFrame}），因此 {@link #renderBg} 里没有一次贴图采样。
 *
 * <p>按钮不新增网络包：点击走原版既有的「容器按钮点击」包（{@code containerId} + 按钮 id），
 * 服务端落到 {@link ControlTerminalMenu#clickMenuButton}，两格数据的刷新仍由
 * {@link ControlTerminalMenu#broadcastChanges} 负责。
 *
 * <p>「已绑定 / 未绑定」不在客户端算（裁定 #14）：两个按钮的 {@code active} 直接取
 * {@link ControlTerminalMenu#bound()}，那是服务端下发的两格数据的客户端副本。
 * <b>重算点必须每帧一次</b>（每帧在 {@link #render} 里重算）：{@code init()}
 * 跑在服务端 {@code ContainerData} 到达之前，那一刻 {@code bound()} 还是 0；只在 {@code init()}
 * 里赋一次会让两个按钮永久置灰，而状态行每帧读 {@code bound()}——表现成「显示已绑定、按钮却点不动」。
 */
public class ControlTerminalScreen extends AbstractContainerScreen<ControlTerminalMenu> {
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 88;

    /** 原版浅灰底。 */
    private static final int BACKGROUND_COLOR = 0xFFC6C6C6;
    /** 1 像素黑描边。 */
    private static final int BORDER_COLOR = 0xFF000000;
    /** 标题与状态行文字色（原版标签色）。 */
    private static final int LABEL_COLOR = 0x404040;
    /** 选中态的 1 像素绿框：与终端绿线同一组分量（{@code 0.30 / 0.85 / 0.30}）。 */
    private static final int SELECTED_COLOR = 0xFF4DD94D;

    private static final int BUTTON_WIDTH = 78;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_Y = 20;
    private static final int BUTTON_LEFT_X = 9;
    private static final int BUTTON_RIGHT_X = 89;
    private static final int STATUS_X = 9;
    private static final int STATUS_Y = 52;

    private static final String KEY_MODE_CULTIVATOR = "gui.farmstorm.control_terminal.mode.cultivator";
    private static final String KEY_MODE_CONVERTER = "gui.farmstorm.control_terminal.mode.converter";
    private static final String KEY_STATUS_BOUND = "gui.farmstorm.control_terminal.status.bound";
    private static final String KEY_STATUS_UNBOUND = "gui.farmstorm.control_terminal.status.unbound";

    private Button cultivatorButton;
    private Button converterButton;

    public ControlTerminalScreen(ControlTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    /**
     * 两个按钮的版式与初始可点态（未绑定时都是 {@code active = false}）。
     *
     * <p>先 {@code super.init()} 再建控件：{@code leftPos} / {@code topPos} 由
     * {@code AbstractContainerScreen#init} 算出，本方法全部坐标都基于它们。
     */
    @Override
    protected void init() {
        super.init();
        this.cultivatorButton = this.addRenderableWidget(Button.builder(
                        Component.translatable(KEY_MODE_CULTIVATOR),
                        button -> this.sendButton(ControlTerminalMenu.BUTTON_CULTIVATOR))
                .bounds(this.leftPos + BUTTON_LEFT_X, this.topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        this.converterButton = this.addRenderableWidget(Button.builder(
                        Component.translatable(KEY_MODE_CONVERTER),
                        button -> this.sendButton(ControlTerminalMenu.BUTTON_CONVERTER))
                .bounds(this.leftPos + BUTTON_RIGHT_X, this.topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        this.refreshButtons();
    }

    /** 纯色底 + 1 像素描边：没有贴图可 blit（本版 GUI 贴图 0 张）。 */
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + IMAGE_WIDTH, y + IMAGE_HEIGHT, BACKGROUND_COLOR);
        graphics.fill(x, y, x + IMAGE_WIDTH, y + 1, BORDER_COLOR);
        graphics.fill(x, y + IMAGE_HEIGHT - 1, x + IMAGE_WIDTH, y + IMAGE_HEIGHT, BORDER_COLOR);
        graphics.fill(x, y, x + 1, y + IMAGE_HEIGHT, BORDER_COLOR);
        graphics.fill(x + IMAGE_WIDTH - 1, y, x + IMAGE_WIDTH, y + IMAGE_HEIGHT, BORDER_COLOR);
    }

    /** 0 槽面板没有物品栏：只画标题，不画原版那行「物品栏」标签。 */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.refreshButtons();
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderSelectionFrames(graphics);
        this.renderStatusLine(graphics);
    }

    /** 每帧按两格 {@code ContainerData} 重算：绑定状态决定可点态（不可点时点下去毫无反应）。 */
    private void refreshButtons() {
        boolean bound = this.getMenu().bound();
        if (this.cultivatorButton != null) {
            this.cultivatorButton.active = bound;
        }
        if (this.converterButton != null) {
            this.converterButton.active = bound;
        }
    }

    /** 选中态的画法（08 号 §4.5）：模式格指向哪个按钮，就给哪个按钮描一圈 1 像素绿框。 */
    private void renderSelectionFrames(GuiGraphics graphics) {
        Mode mode = this.getMenu().mode();
        if (mode == Mode.CULTIVATOR && this.cultivatorButton != null) {
            drawFrame(graphics, this.cultivatorButton);
        }
        if (mode == Mode.CONVERTER && this.converterButton != null) {
            drawFrame(graphics, this.converterButton);
        }
    }

    /** 状态行 = 绑定状态；{@code OFF} 之外再拼「 · 」与当前模式名。 */
    private void renderStatusLine(GuiGraphics graphics) {
        Mode mode = this.getMenu().mode();
        Component status = Component.translatable(this.getMenu().bound() ? KEY_STATUS_BOUND : KEY_STATUS_UNBOUND);
        if (mode != Mode.OFF) {
            status = status.copy().append(" · ").append(modeLabel(mode));
        }
        graphics.drawString(this.font, status, this.leftPos + STATUS_X, this.topPos + STATUS_Y, LABEL_COLOR, false);
    }

    private static Component modeLabel(Mode mode) {
        return Component.translatable(mode == Mode.CULTIVATOR ? KEY_MODE_CULTIVATOR : KEY_MODE_CONVERTER);
    }

    /** 1 像素绿框包住控件矩形（{@code getX/Y/Width/Height}）。 */
    private static void drawFrame(GuiGraphics graphics, Button button) {
        int x = button.getX() - 1;
        int y = button.getY() - 1;
        int width = button.getWidth() + 2;
        int height = button.getHeight() + 2;
        graphics.fill(x, y, x + width, y + 1, SELECTED_COLOR);
        graphics.fill(x, y + height - 1, x + width, y + height, SELECTED_COLOR);
        graphics.fill(x, y, x + 1, y + height, SELECTED_COLOR);
        graphics.fill(x + width - 1, y, x + width, y + height, SELECTED_COLOR);
    }

    /** 按钮 → 服务端的 {@code ServerboundContainerButtonClickPacket}；未绑定那两格会让按钮根本收不到点击。 */
    private void sendButton(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.getMenu().containerId, id);
        }
    }
}
