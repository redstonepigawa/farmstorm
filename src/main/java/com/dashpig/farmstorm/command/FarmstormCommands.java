package com.dashpig.farmstorm.command;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.farmland.FarmlandLedger;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * {@code /farmstorm} 指令树（F-160 / D-116 / TECH §7.1 – §7.2）。
 *
 * <p>字面量逐字照抄：根 {@code farmstorm}（与模组 id 同名，避免与别家 {@code /farm} 撞名）、
 * 子指令 {@code farmlandlimit}（不是 {@code farmland_limit}、不是 {@code farmlandLimit}）。
 *
 * <p><b>不注册任何 {@code GameRule}</b>：1.21.1 的 {@code GameRules} 里 register 入口是 {@code private static}，
 * 在「无 Mixin」红线内注册不了真 gamerule，额度只由「服务端 config 默认值 + 本子指令」两件载体定义
 * （TECH §7.4 / A-19）。
 */
public final class FarmstormCommands {
    /** 参数名：{@code /farmstorm farmlandlimit <value>}。 */
    private static final String ARG_VALUE = "value";

    /** 下界与 config {@code farmland.limitPerPlayer} 的 0 一致，上界取 §8.1 的范围列（TECH §7.2）。 */
    private static final int MIN_LIMIT = 0;
    private static final int MAX_LIMIT = 4096;

    /** 权限 2 = 与 {@code /gamerule} 同档：额度是全局玩法规则，玩家自助改它等于没有规则（TECH §7.2）。 */
    private static final int PERMISSION_LEVEL = 2;

    private FarmstormCommands() {}

    /** 由 {@code FarmstormGameEvents#onRegisterCommands} 在 {@code RegisterCommandsEvent} 里调用。 */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Farmstorm.MOD_ID)
                .then(Commands.literal("farmlandlimit")
                        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                        .then(Commands.argument(ARG_VALUE, IntegerArgumentType.integer(MIN_LIMIT, MAX_LIMIT))
                                .executes(context -> setFarmlandLimit(context.getSource(),
                                        IntegerArgumentType.getInteger(context, ARG_VALUE))))));
    }

    /**
     * 只改 {@code base}，不改已开垦数（TECH §7.6）：把额度调小不会让既有耕地消失，只让下一次开垦被拒。
     *
     * <p>反馈回显新额度与「本进程内有效、重启回 config 默认」两件事，**不带玩家名**——这是全局值（TECH §7.3）。
     */
    private static int setFarmlandLimit(CommandSourceStack source, int value) {
        FarmlandLedger ledger = FarmlandLedger.get(source.getServer());
        ledger.setBase(value);
        source.sendSuccess(() -> Component.translatable("command.farmstorm.farmlandlimit.set", value), true);
        Farmstorm.LOGGER.info("指令 /farmstorm farmlandlimit {}：耕地基础额度 → {}（运行期覆盖，重启回 config 默认）",
                value, ledger.base());
        return Command.SINGLE_SUCCESS;
    }
}
