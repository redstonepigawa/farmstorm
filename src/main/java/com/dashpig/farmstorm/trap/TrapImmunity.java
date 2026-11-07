package com.dashpig.farmstorm.trap;

import com.dashpig.farmstorm.config.FarmstormConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 陷阱大类的 5 秒免疫（D-112 / TECH §5.1）。
 *
 * <p>只记玩家：值是该玩家免疫到期的 {@code gameTime}，窗口按半开区间 {@code [t0, t0 + trap.immunityTicks)}，
 * 因此第 {@code t0 + 100} 刻起可以再次触发。生物不记免疫（D-112 第 3 条）——生物生命周期短、记账收益为零，
 * 而一旦记了就会出现「一群怪里有一只刚触发过 ⇒ 一队怪都被免疫」的反直觉结果。
 *
 * <p>只在服务端主线程读写，因此用普通 {@code HashMap}；换服务器实例就整表清空，
 * 与 {@code DeployedCropBlockEntity} 的放置账本 {@code ledgerOf} 同形。表不落盘（D-112 第 4 条）：
 * 它只活 100 刻，重启后清零的最坏后果是「同一人 5 秒内能再踩一次」。
 */
public final class TrapImmunity {

    /** 玩家 → 免疫到期的 gameTime。 */
    private static final Map<UUID, Long> UNTIL = new HashMap<>();

    /** 免疫表归属的服务器实例：重启 / 换存档都会换实例，旧表随即作废。 */
    @Nullable
    private static MinecraftServer tableOwner;

    private TrapImmunity() {}

    /**
     * 免疫闸：非 {@link Player} 一律放行（生物不记免疫）；是玩家时先判窗口——仍在窗口内返回 {@code false}
     * （本次扫描直接跳过，**不 consume、不施效果、不写免疫**），否则**先写入**新窗口再返回 {@code true}。
     *
     * <p>因为写入发生在 {@code trigger} 之前，同一刻第二个够到同一玩家的陷阱必然被闸住 ⇒
     * 「同一刻只触发一次」自动成立，不需要额外的去重键（TECH §5.1 的「同刻多次」行）。
     */
    public static boolean passGate(ServerLevel level, LivingEntity target) {
        if (!(target instanceof Player player)) {
            return true;
        }
        Map<UUID, Long> until = tableOf(level);
        UUID id = player.getUUID();
        long now = level.getGameTime();
        if (now < until.getOrDefault(id, 0L)) {
            return false;
        }
        int ticks = FarmstormConfig.trapImmunityTicks.get();
        if (ticks > 0) {
            until.put(id, now + ticks);           // ticks = 0 表示关掉免疫：不记账，也就不会留下只会增长的条目
        }
        return true;
    }

    /**
     * 清掉某个玩家的免疫条目：玩家登出即调用，满足「重连后免疫清零」（D-112 第 4 条、25 号 F-146 验收 3）。
     * 只摘一条而不是整表清空——同一次运行里其它玩家的窗口不受影响。
     */
    public static void clear(UUID playerId) {
        UNTIL.remove(playerId);
    }

    private static Map<UUID, Long> tableOf(ServerLevel level) {
        MinecraftServer server = level.getServer();
        if (server != tableOwner) {
            UNTIL.clear();
            tableOwner = server;
        }
        return UNTIL;
    }
}
