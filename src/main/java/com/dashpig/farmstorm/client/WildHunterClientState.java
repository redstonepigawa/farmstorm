package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * 客户端状态缓存（TECH §4.1 末段）：把 {@code wild_hunter} 的「在不在」算一次，渲染线程只读快照。
 * <p>两个距离量用镜像常量而不是读 config：{@code FarmstormConfig} 是 {@code Type.SERVER}，
 * 专用服务器上客户端读不到（TECH §4.1 / §4.4 的降级口径），改 config 默认值时要同步改这里（已知偏差，不修）。
 */
public final class WildHunterClientState {
    /** 镜像 {@code wild_hunter.highlightRadius} 默认 64.0（D-62）。 */
    public static final double DEFAULT_HIGHLIGHT_RADIUS = 64.0;
    /** 镜像 {@code wild_hunter.loopSoundRange} 默认 16.0（D-76）。 */
    public static final double DEFAULT_LOOP_SOUND_RANGE = 32.0;
    /** 镜像 {@code wild_hunter.dashDistance} 默认 5.0（作者 v0.0.2 #4「5 格左右」）。 */
    public static final double DEFAULT_DASH_DISTANCE = 5.0;
    /** 镜像 {@code wild_hunter.dashCharges} 默认 2（作者「可储存两次」）。 */
    public static final int DEFAULT_DASH_CHARGES = 2;
    /** 镜像 {@code wild_hunter.dashChargeTicks} 默认 100（5 秒回 1 个）。**客户端唯一的那个数字，改默认值只改这里**。 */
    public static final int DEFAULT_DASH_CHARGE_TICKS = 100;

    private static boolean active = false;

    private WildHunterClientState() {}

    /** 每个客户端刻算一次（{@code ClientTickEvent.Post}）。 */
    public static void refresh() {
        Player player = Minecraft.getInstance().player;
        active = player != null && player.hasEffect(FarmstormMobEffects.WILD_HUNTER);
    }

    /** 本地玩家此刻是否带着 {@code wild_hunter}；渲染线程只读这个快照。 */
    public static boolean active() {
        return active;
    }

    public static double highlightRadius() {
        return DEFAULT_HIGHLIGHT_RADIUS;
    }

}
