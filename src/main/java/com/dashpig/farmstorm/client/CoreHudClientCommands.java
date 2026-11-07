package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.config.FarmstormClientConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * 全项目**第一条客户端指令**（D-144 第 2 条 / 30 号 §2.4.3）：{@code /farmstorm hud <on|off|reset>}。
 *
 * <p>三条口径：
 * <ul>
 *   <li>三个子命令**不带** {@code requires(...)}（30 号 §5.5 第 11 条）——客户端指令只改本地显示，
 *       设权限反而会让单人非创造模式玩家用不了；</li>
 *   <li>三条反馈都用<b>本地回显</b>形态（30 号 §5.5 第 10 条）：消息的第二参数一律 false = 只在本地聊天栏
 *       显示，不进服务器广播；写 true 会让第二客户端的聊天栏跟着刷。</li>
 *   <li>{@code reset} 只复位<b>位置</b>（{@code x = y = 4}），**不动 {@code enabled}**（30 号 §2.4.3）。</li>
 * </ul>
 */
public final class CoreHudClientCommands {
    /** 反馈键：{@code /farmstorm hud on} 与按键 H 回显共用（不额外占语言键）。 */
    public static final String FEEDBACK_ON = "command.farmstorm.hud.on";
    /** 反馈键：{@code /farmstorm hud off} 与按键 H 回显共用。 */
    public static final String FEEDBACK_OFF = "command.farmstorm.hud.off";
    /** 反馈键：{@code /farmstorm hud reset}。 */
    public static final String FEEDBACK_RESET = "command.farmstorm.hud.reset";

    private CoreHudClientCommands() {}

    /** 由 {@code FarmstormClient#onRegisterClientCommands} 调用（{@code RegisterClientCommandsEvent}，game 总线、仅客户端）。 */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("farmstorm")
                .then(Commands.literal("hud")
                        .then(Commands.literal("on").executes(context -> enable(context.getSource())))
                        .then(Commands.literal("off").executes(context -> disable(context.getSource())))
                        .then(Commands.literal("reset").executes(context -> resetPosition(context.getSource())))));
    }

    private static int enable(CommandSourceStack source) {
        FarmstormClientConfig.saveHudState(true,
                FarmstormClientConfig.hudX.get(), FarmstormClientConfig.hudY.get());
        source.sendSuccess(() -> Component.translatable(FEEDBACK_ON), false);
        return 1;
    }

    private static int disable(CommandSourceStack source) {
        FarmstormClientConfig.saveHudState(false,
                FarmstormClientConfig.hudX.get(), FarmstormClientConfig.hudY.get());
        source.sendSuccess(() -> Component.translatable(FEEDBACK_OFF), false);
        return 1;
    }

    private static int resetPosition(CommandSourceStack source) {
        FarmstormClientConfig.saveHudState(FarmstormClientConfig.hudEnabled.get(),
                FarmstormClientConfig.DEFAULT_HUD_X, FarmstormClientConfig.DEFAULT_HUD_Y);
        source.sendSuccess(() -> Component.translatable(FEEDBACK_RESET), false);
        return 1;
    }
}
