package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.menu.ConverterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 转换器 GUI（TECH §4.8.4）。背景图由 M5 的纹理生成脚本产出，本类负责贴图、文案与转换按钮。
 * 按钮走原版 ServerboundContainerButtonClickPacket（{@link ConverterMenu#BUTTON_CONVERT}），不新增网络包。
 */
public class ConverterScreen extends AbstractContainerScreen<ConverterMenu> {
    private static final ResourceLocation BACKGROUND = Farmstorm.id("textures/gui/converter.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;
    private static final int LABEL_COLOR = 0x404040;
    /** 文案左右留白：与 titleLabelX / inventoryLabelX 的 8 像素对齐。 */
    private static final int CONTENT_MARGIN = 8;
    /** 转换按钮：贴图里画着同位置同尺寸的矩形底，两边常量必须一致。 */
    private static final int BUTTON_X = 8;
    private static final int BUTTON_Y = 42;
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 18;

    private Button convertButton;

    public ConverterScreen(ConverterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
        this.inventoryLabelY = IMAGE_HEIGHT - 94;       // 72：贴在背包区（84）上方的分隔线之下
    }

    @Override
    protected void init() {
        super.init();
        this.convertButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.farmstorm.converter.convert"),
                        button -> this.convert())
                .bounds(this.leftPos + BUTTON_X, this.topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("gui.farmstorm.converter.tooltip")))
                .build());
        this.convertButton.active = this.menu.canConvert();
    }

    /** 发原版容器按钮包让服务端做转换；客户端不判定成败（HC-03 / C-6）。 */
    private void convert() {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ConverterMenu.BUTTON_CONVERT);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.convertButton != null) {
            // ContainerData 每 tick 由服务端覆盖，按钮可用状态必须跟着刷新，否则会停在开界面那一刻的值。
            this.convertButton.active = this.menu.canConvert();
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 必须显式给出纹理尺寸：6 参重载内部按 256x256 算 UV，会把 176x166 的贴图放大 1.46/1.54 倍，
        // 只采样左上 121x108 的区域 —— 槽位框就会和上层渲染的真正物品格整体错开，看着「完全是乱的」。
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, IMAGE_WIDTH, IMAGE_HEIGHT,
                IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        // 经验成本右对齐到内容右边界、与标题同行；数值读 ContainerData[0]（服务端现算后同步）。
        Component cost = Component.translatable("gui.farmstorm.converter.xp_cost", this.menu.xpCost());
        graphics.drawString(this.font, cost, IMAGE_WIDTH - CONTENT_MARGIN - this.font.width(cost),
                this.titleLabelY, LABEL_COLOR, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                LABEL_COLOR, false);
    }
}
