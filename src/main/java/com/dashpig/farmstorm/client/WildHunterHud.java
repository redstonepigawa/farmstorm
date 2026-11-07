package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RenderGuiEvent;

/**
 * 狂野猎人的充能条 HUD（作者 v0.0.2 #4）：快捷栏上方两条，满 = 可用、空/半满 = 冷却中。
 *
 * <p>位置算法（避开三条行带）：快捷栏占 {@code y ∈ [h-22, h-1]}、经验条 {@code y ∈ [h-29, h-27]}、
 * 生命与饥饿 {@code y ∈ [h-39, h-30]}，所以两条充能条放在 {@code y = h-52 .. h-49}（距底 [48, 52) 带，
 * 与生命 / 饥饿行净空 9 px），两侧与快捷栏左右端对齐（快捷栏宽 182，居中）。
 * <p>只在带 {@code wild_hunter} 时画（{@link WildHunterClientState#active()}），别的效果/无效果都不画。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class WildHunterHud {
    /** 快捷栏宽（已缩放的 GUI 像素）：182。 */
    private static final int HOTBAR_WIDTH = 182;
    /** 每条充能条宽 20、高 4，两条间隔 4，整体居中 ⇒ 总宽 44。 */
    private static final int BAR_WIDTH = 20;
    private static final int BAR_HEIGHT = 4;
    private static final int BAR_GAP = 4;
    private static final int BAR_Y_FROM_BOTTOM = 52;

    /** 满充能：狂野猎人的红色主题。 */
    private static final int COLOR_FULL = 0xFFFF4444;
    /** 冷却中：同色但压暗。 */
    private static final int COLOR_CHARGING = 0xFF8A1A1A;
    /** 空槽底色。 */
    private static final int COLOR_EMPTY = 0x60101010;
    /** 边框。 */
    private static final int COLOR_BORDER = 0xFF000000;

    private WildHunterHud() {}

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!WildHunterClientState.active()) {
            return;                                    // 效果不在 ⇒ 不显示
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int totalWidth = WildHunterDashClient.MAX_CHARGES * BAR_WIDTH
                + (WildHunterDashClient.MAX_CHARGES - 1) * BAR_GAP;
        int startX = (screenWidth - totalWidth) / 2;
        int y = screenHeight - BAR_Y_FROM_BOTTOM;
        int charges = WildHunterDashClient.charges();
        float progress = WildHunterDashClient.rechargeProgress();
        for (int i = 0; i < WildHunterDashClient.MAX_CHARGES; i++) {
            int x = startX + i * (BAR_WIDTH + BAR_GAP);
            graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, COLOR_BORDER);
            graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_EMPTY);
            if (i < charges) {
                graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_FULL);     // 满格
            } else if (i == charges) {
                int width = Math.round(BAR_WIDTH * Math.max(0.0F, Math.min(1.0F, progress)));
                if (width > 0) {
                    graphics.fill(x, y, x + width, y + BAR_HEIGHT, COLOR_CHARGING); // 正在回充的那一格
                }
            }
        }
        if (Farmstorm.LOGGER.isDebugEnabled()) {
            Farmstorm.LOGGER.debug("狂野猎人 HUD 充能 {}/{} 进度 {} 位置 ({}, {})",
                    charges, WildHunterDashClient.MAX_CHARGES, String.format(java.util.Locale.ROOT, "%.2f", progress),
                    startX, y);
        }
    }
}
