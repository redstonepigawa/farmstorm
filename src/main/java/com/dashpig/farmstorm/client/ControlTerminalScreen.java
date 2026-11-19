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
 * <p>本版 GUI 贴图新增 <b>0 张</b>：底是 {@link GuiGraphics#fill} 的纯色，选中框是
 * {@link GuiGraphics#renderOutline}，因此 {@link #renderBg} 里没有一次贴图采样。
 *
 * <p>按钮不新增网络包：点击走原版既有的「容器按钮点击」包（{@code containerId} + 按钮 id），
 * 服务端落到 {@link ControlTerminalMenu#clickMenuButton}，两格数据的刷新仍由
 * {@link ControlTerminalMenu#broadcastChanges} 负责。
 *
 * <p>「已绑定 / 未绑定」不在客户端算（裁定 #14）：两个按钮的 {@code active} 直接取
 * {@link ControlTerminalMenu#bound()}，那是服务端下发的两格数据的客户端副本。
 */
public class ControlTerminalScreen extends AbstractContainerScreen<ControlTerminalMenu> {
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 96;
    /** 纯色底（例：深灰，不透明度 FF）。 */
    private static final int BACKGROUND_COLOR = 0xFF202020;
    /** 选中框：0.30 / 0.85 分量 → 0x4D / 0xD9，与绿线同一组。 */
    private static final int SELECTION_COLOR = 0xFF4DD94D;
    private static final int TEXT_COLOR = 0xFFFFFF;

    public ControlTerminalScreen(ControlTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    /**
     * 两个按钮的坐标是 66 号 §4.5 的定版值（各 76 × 20，两格之间留 8 px）。
     *
     * <p>先 {@code super.init()} 再建控件：{@code leftPos} / {@code topPos} 由
     * {@code AbstractContainerScreen#init} 算出，本方法全部坐标都基于它们。
     */
    @Override
    protected void init() {
        super.init();
        Button cultivator = Button.builder(Component.translatable("gui.farmstorm.control_terminal.mode.cultivator"),
                b -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0))
                .bounds(this.leftPos + 8, this.topPos + 30, 76, 20).build();
        Button converter = Button.builder(Component.translatable("gui.farmstorm.control_terminal.mode.converter"),
                b -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1))
                .bounds(this.leftPos + 92, this.topPos + 30, 76, 20).build();
        // 裁定 #14：未绑定 ⇒ 两个都置灰（不是只灰一个）。
        // 1.21.1 的 AbstractWidget 只有 public 的 active 字段、没有配套 setter，置灰就是写这个字段。
        cultivator.active = this.menu.bound();
        converter.active = this.menu.bound();
        this.addRenderableWidget(cultivator);
        this.addRenderableWidget(converter);
    }

    /** 纯色底，不加载任何贴图。 */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos,
                this.leftPos + this.imageWidth, this.topPos + this.imageHeight, BACKGROUND_COLOR);
    }

    /**
     * 标题只画自己这一行：本屏 0 槽，原版 {@code renderLabels} 的第二行玩家物品栏标题没有语义，
     * 且在深色底上看不清（原版颜色 0x404040）。
     */
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR);
    }

    /** {@code super.render} 已画底、标题与两个控件；这里再叠选中框（1 px 包住按钮）与状态行。 */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.menu.mode() == Mode.CULTIVATOR.ordinal()) {
            guiGraphics.renderOutline(this.leftPos + 7, this.topPos + 29, 78, 22, SELECTION_COLOR);
        } else if (this.menu.mode() == Mode.CONVERTER.ordinal()) {
            guiGraphics.renderOutline(this.leftPos + 91, this.topPos + 29, 78, 22, SELECTION_COLOR);
        }
        guiGraphics.drawString(this.font, this.statusText(), this.leftPos + 8, this.topPos + 56, TEXT_COLOR);
    }

    /**
     * 状态行 = 绑定状态（{@code status.bound} / {@code status.unbound}）；
     * 选了模式时再拼「 · 」与模式名。分隔符不进语言键，模式名复用两个按钮的键。
     */
    private Component statusText() {
        Component text = Component.translatable(this.menu.bound()
                ? "gui.farmstorm.control_terminal.status.bound"
                : "gui.farmstorm.control_terminal.status.unbound");
        int mode = this.menu.mode();
        if (mode == Mode.CULTIVATOR.ordinal()) {
            return text.copy()
                    .append(" · ")
                    .append(Component.translatable("gui.farmstorm.control_terminal.mode.cultivator"));
        }
        if (mode == Mode.CONVERTER.ordinal()) {
            return text.copy()
                    .append(" · ")
                    .append(Component.translatable("gui.farmstorm.control_terminal.mode.converter"));
        }
        return text;
    }
}
