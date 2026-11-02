package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 全项目**第一批 KeyMapping**（D-144 第 1 条 / 30 号 §2.4.1）：开关浮窗 H、进入拖拽 J。
 *
 * <p>分类复用 {@link KeyMapping#CATEGORY_GAMEPLAY}（30 号 §5.5 第 5 条）：自建分类要多一条
 * {@code key.categories.*} 语言键，与本版 323 的账对不上；复用的代价只是「按键出现在原版的『游戏操作』页」。
 *
 * <p>注册挂 **mod 总线**：按键注册事件是 {@code IModBusEvent}（已核对）。
 * 消费点在 {@code client/FarmstormClient} 既有的 {@code ClientTickEvent.Post} 里。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FarmstormKeys {
    /** 开关浮窗：默认 H。 */
    public static final KeyMapping TOGGLE = new KeyMapping("key.farmstorm.core_hud.toggle",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, KeyMapping.CATEGORY_GAMEPLAY);

    /** 进入拖拽定位：默认 J。 */
    public static final KeyMapping DRAG = new KeyMapping("key.farmstorm.core_hud.drag",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, KeyMapping.CATEGORY_GAMEPLAY);

    private FarmstormKeys() {}

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
        event.register(DRAG);
    }
}
