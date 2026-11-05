package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.menu.OutpostCoreMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 据点核心的 54 格存储界面（D-166 / 36 号 §2.6.3）。
 *
 * <p><b>复用原版贴图</b> {@code minecraft:textures/gui/container/generic_54.png}（256 × 256），
 * 因此新增 GUI 贴图 <b>0 张</b>：54 格 = 原版双箱，槽位坐标与 176 × 222 版式都是原版常量。
 *
 * <p>{@code renderBg} 必须显式给出<b>贴图尺寸</b>：{@code blit} 的缺省重载按 256 × 256 算 UV 是对的，
 * 但不能把 {@code IMAGE_WIDTH / IMAGE_HEIGHT} 当成贴图尺寸传进去——那样会把 256 × 256 的图按 176 × 222
 * 重新采样，槽位框与物品格整体错开（{@code ConverterScreen} 头上那条注释记过同一件事的反向错误）。
 * 这里传的是 {@code 256, 256}。
 */
public class OutpostCoreScreen extends AbstractContainerScreen<OutpostCoreMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 222;
    /** 原版 {@code generic_54.png} 的实际尺寸：UV 必须按它算，不能按 176 × 222。 */
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    public OutpostCoreScreen(OutpostCoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
        this.inventoryLabelY = IMAGE_HEIGHT - 94;    // 128：原版 6 行箱子的物品栏标题位置
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int upper = OutpostCoreMenu.ROWS * 18 + 17;               // 6 × 18 + 17 = 125
        // 上段：54 格区，贴图 v=0
        graphics.blit(BACKGROUND, x, y, 0.0F, 0.0F, IMAGE_WIDTH, upper, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        // 下段：玩家背包 + 快捷栏，贴图 v=126 画到 y=125（1 px 错开是原版自身的行为）
        graphics.blit(BACKGROUND, x, y + upper, 0.0F, 126.0F, IMAGE_WIDTH, 96, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
