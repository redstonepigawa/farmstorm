package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.client.Minecraft;

/**
 * 狂野猎人期间的伽马提升（作者反馈：水下图完全看不见）。
 * <p>只改**内存里的选项值**，不写 `options.txt`：进入效果时记下玩家原值并抬到 1.0，
 * 离开效果时**精确还原**。
 * <p>还原覆盖所有退出路径——效果到期 / `/effect clear` / 死亡（原版会清效果）/ 退出世界 / 掉线：
 * 这些情况下 {@link WildHunterClientState#active()} 会变假或 `level == null`，本类下一 tick 就还原。
 */
public final class GammaBoost {
    /** 提升到的伽马（原版滑条上限）。 */
    private static final double BOOSTED_GAMMA = 1.0;
    /** 玩家原值；null 表示当前没有接管。 */
    private static Double originalGamma;

    static {
        // 退出世界/掉线时立刻还原：tick 兜底依赖「下一 tick」，而退出游戏可能已经没有下一 tick，
        // 那时原版会把当前 gamma 写进 options.txt（玩家亮度被永久改成满）。
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(GammaBoost::onLoggingOut);
    }

    private GammaBoost() {}

    private static void onLoggingOut(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        restore();
    }

    /** 每客户端刻调用：按效果状态接管/还原。 */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean wanted = WildHunterClientState.active() && minecraft.player != null && minecraft.level != null
                && minecraft.getConnection() != null;
        if (wanted) {
            if (originalGamma == null) {
                originalGamma = minecraft.options.gamma().get();
                minecraft.options.gamma().set(BOOSTED_GAMMA);
                Farmstorm.LOGGER.info("狂野猎人 伽马提升 {} -> {}", originalGamma, BOOSTED_GAMMA);
            }
        } else {
            restore();
        }
    }

    /** 幂等还原：没有接管时什么也不做。 */
    public static void restore() {
        if (originalGamma == null) {
            return;
        }
        Minecraft.getInstance().options.gamma().set(originalGamma);
        Farmstorm.LOGGER.info("狂野猎人 伽马还原 -> {}", originalGamma);
        originalGamma = null;
    }
}
