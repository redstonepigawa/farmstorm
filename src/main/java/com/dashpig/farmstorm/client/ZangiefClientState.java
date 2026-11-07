package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.effect.ZangiefPiledriverEffect;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent;

/**
 * 桑吉尔蔗的客户端状态快照（TECH §5.4）：每 tick 读一次自己身上的 {@code farmstorm:zangief_piledriver}，
 * 写进三个标量；相机与输入锁两个渲染/事件钩子只读这三个标量，不自己读效果。
 * <p>为什么不新增网络包：相位号就是 amplifier、相位时长就是 duration、「在不在」就是效果实例是否存在（D-50）。
 * <p>为什么在 tick 里读而不是渲染里读：渲染一帧可能跑多次（高刷屏），把逻辑状态与帧率耦合起来，
 * 而且渲染线程读 {@code MobEffectInstance} 没有同步保证。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class ZangiefClientState {
    /**
     * 自转速度的 config 默认值镜像（TECH §4.4 的降级口径）：{@code farmstorm-server.toml} 在专用服务器上
     * 客户端读不到，所以客户端只能用编译期常量。**改 config 默认值时必须同步改这里**。
     */
    public static final float DEFAULT_SPIN_DEGREES_PER_TICK = 72.0F;

    /**
     * 自转起始延迟的 config 默认值镜像（{@code zangief_cane.spinDelayTicks} 默认 4，§4.4 降级口径）。
     * <p>计时基准是**相位 2 的进入时刻**（作者口径「视角旋转从上下倒转 4 tick 后开始」——倒转本身就发生在相位 2），
     * 不是效果生效时刻：那样会在还没开始下坠时就先转起来。
     */
    public static final int DEFAULT_SPIN_DELAY_TICKS = 4;

    /** 硬切换（先摘再加）那一 tick 客户端可能读到 null，相位号容忍 2 刻（§5.4 / RT-15）。 */
    private static final int PHASE_MISSING_TOLERANCE_TICKS = 2;

    /** 效果实例此刻在不在：相机倒转与输入锁的唯一判据（D-60 / D-59 都要「三个相位全锁」）。 */
    private static boolean active;

    /** 相位号 = amplifier；-1 表示无效果或读数非法。 */
    private static int phase = -1;

    /** 连续读不到效果的刻数。 */
    private static int missingTicks;

    /** 相位 2 的自转累加角度（只增不减，相位切换即归零）。 */
    private static float accumulatedDegrees;

    /** 本段相位 2 已累加的刻数，只用于收尾时打一行可核对的读数（F-91 验收 2）。 */
    private static int spinTicks;

    /** 进入相位 2 以来的刻数：决定「倒转先走几刻、之后才开始自转」。 */
    private static int fallingTicks;

    private ZangiefClientState() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        MobEffectInstance instance = player == null ? null
                : player.getEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER.get());
        if (instance == null) {
            // 「在不在」当刻就复位：效果消失后 1 刻内相机与输入必须回到正常（F-91 验收 4）。
            active = false;
            if (++missingTicks >= PHASE_MISSING_TOLERANCE_TICKS) {
                logSpinFinished();
                phase = -1;
                accumulatedDegrees = 0.0F;
                fallingTicks = 0;
            }
            return;
        }
        active = true;
        missingTicks = 0;
        int next = instance.getAmplifier();
        if (next < ZangiefPiledriverEffect.PHASE_REVERSED || next > ZangiefPiledriverEffect.PHASE_FALLING) {
            // §5.6 断言 1：相位号必须在 {-1, 0, 1, 2} 内，其它值一律当无效并打 warn。
            Farmstorm.LOGGER.warn("桑吉尔蔗 客户端读到非法相位号 {}，按无效果处理", next);
            phase = -1;
            accumulatedDegrees = 0.0F;
            return;
        }
        if (next != phase) {
            // 只有「进入相位 2」才算自转的起点：倒转从这一刻开始并保持到结束，自转在 spinDelayTicks 刻后才加入。
            if (next == ZangiefPiledriverEffect.PHASE_FALLING) {
                accumulatedDegrees = 0.0F;
                fallingTicks = 0;
                spinTicks = 0;
            }
            phase = next;
        }
        if (phase == ZangiefPiledriverEffect.PHASE_FALLING) {
            fallingTicks++;
            if (fallingTicks > DEFAULT_SPIN_DELAY_TICKS) {
                accumulatedDegrees += DEFAULT_SPIN_DEGREES_PER_TICK;
                spinTicks++;
            }
        }
    }

    /** 离开相位 2 时打一行「累计自转」，用于核对 72 度/刻 × N 刻（F-91 验收 2）。 */
    private static void logSpinFinished() {
        if (spinTicks > 0) {
            Farmstorm.LOGGER.info(String.format(java.util.Locale.ROOT,
                    "桑吉尔蔗 自转累计 %.1f 度（%d 刻 × %.1f）", accumulatedDegrees, spinTicks,
                    DEFAULT_SPIN_DEGREES_PER_TICK));
            spinTicks = 0;
        }
    }

    /** 三个相位都在锁定期内：判据是「效果在不在」，不是 amplifier（D-60）。 */
    public static boolean active() {
        return active;
    }


    /**
     * 上下倒转：**从相位 2 开始一直保持到结束**（作者口径：倒转是打桩机的核心观感，不随自转取消）。
     */
    public static boolean inverted() {
        return active && phase == ZangiefPiledriverEffect.PHASE_FALLING;
    }

    /**
     * yaw 自转：进入相位 2 后第 {@link #DEFAULT_SPIN_DELAY_TICKS} 刻起开始，一直转到效果结束。
     * （作者口径「视角旋转从视角上下倒转 4 tick 后开始直到结束」；计时基准是相位 2 进入时刻。）
     */
    public static boolean spinning() {
        return active && phase == ZangiefPiledriverEffect.PHASE_FALLING
                && fallingTicks > DEFAULT_SPIN_DELAY_TICKS;
    }

    /** 相位 2 累计自转角（度），供相机 yaw 叠加；只读快照，不写回玩家真实朝向。 */
    public static float accumulatedDegrees() {
        return accumulatedDegrees;
    }
}
