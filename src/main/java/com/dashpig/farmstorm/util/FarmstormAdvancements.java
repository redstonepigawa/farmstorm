package com.dashpig.farmstorm.util;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 服务端授予 14 条 {@code minecraft:impossible} 进度（TECH §6.7「实现补充」；v0.0.1 是 8 条，v0.0.2 +3、v0.0.3 +3）。
 * 判定只在服务端做（HC-03）：不发网络包、不加指令（D-24）。
 * 清单见 {@code datagen/ModAdvancementProvider#AWARD_REQUIRED}，准则键与进度路径同名。
 */
public final class FarmstormAdvancements {

    private FarmstormAdvancements() {}

    /**
     * 授予 {@code farmstorm:<path>}。进度未加载（数据包未启用）时静默跳过；
     * 已授予过的重复调用由 {@code PlayerAdvancements#award} 自行忽略。
     */
    public static void award(ServerPlayer player, String path) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        AdvancementHolder holder = server.getAdvancements().get(Farmstorm.id(path));
        if (holder != null) {
            player.getAdvancements().award(holder, path);
        }
    }
}
