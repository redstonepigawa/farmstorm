package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;

/**
 * 狂野猎人期间**去掉迷雾**（作者反馈：水下视野仍不理想，抬伽马不够）。
 * <p>做法是取消原版的 {@link ViewportEvent.RenderFog}——它同时覆盖水下雾、岩浆雾、
 * 距离雾与失明/黑暗效果造成的浓雾，取消后视距由渲染距离决定。
 * <p>只在效果在身时生效，退出即恢复；不动玩家的任何本地设置。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class FogSuppressor {
    private FogSuppressor() {}

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!WildHunterClientState.active()) {
            return;
        }
        if (Minecraft.getInstance().player == null) {
            return;
        }
        event.setCanceled(true);
    }

    /** 水下那层蓝色蒙版也去掉：它和雾是两回事，只去雾仍会整屏偏蓝压暗。 */
    @SubscribeEvent
    public static void onBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.WATER) {
            return;
        }
        if (!WildHunterClientState.active()) {
            return;
        }
        event.setCanceled(true);
    }
}
