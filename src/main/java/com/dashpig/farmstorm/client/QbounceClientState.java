package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Q弹 的客户端状态机（TECH §3.12）：每 tick 读一次自己身上的 {@code farmstorm:q_bounce}，把「在不在 / 剩余刻数 /
 * 进入刻」摊成静态标量；{@code render} 分支（F-108）只读这些标量，不自己读效果。
 * <p>为什么不新增网络包：{@code qbounce} 的四个键里只有 {@code poseMode} 是服务端决策（要不要挂坐骑），
 * 剩下三个姿态参数在两侧取同一组默认值（D-81：不发包、不写回实体）。
 * <p>为什么在 tick 里读而不是渲染里读：渲染一帧可能跑多次（高刷屏），把相位与帧率耦合起来，
 * 且渲染线程读 {@code MobEffectInstance} 没有同步保证。
 * <p>相位的基准是**本地累加刻数** {@code t}，不是「总时长 − 剩余时长」：后者会被 {@code /effect} 改写时长
 * 拉得相位跳变（17 号 §2.5 的实现约束第 2 条）。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class QbounceClientState {
    /**
     * 旋转角速度的 config 默认值镜像（TECH §3.12 的降级口径）：{@code farmstorm-server.toml} 在专用服务器上
     * 客户端读不到，所以客户端只能用编译期常量。**与 config 同步义务**：改
     * {@code FarmstormConfig#qbounceRotateDegreesPerTick} 的默认值（现为 24.0，范围 0.0 ~ 360.0）时必须同步改这里。
     */
    public static final float DEFAULT_ROTATE_DEGREES_PER_TICK = 24.0F;

    /**
     * 压缩拉伸幅度的 config 默认值镜像（同上）。**与 config 同步义务**：改
     * {@code FarmstormConfig#qbounceSquashAmplitude} 的默认值（现为 0.15，范围 0.0 ~ 0.90）时必须同步改这里。
     */
    public static final float DEFAULT_SQUASH_AMPLITUDE = 0.15F;

    /**
     * 压缩拉伸周期的 config 默认值镜像（同上，单位刻）。**与 config 同步义务**：改
     * {@code FarmstormConfig#qbounceSquashPeriodTicks} 的默认值（现为 6，范围 2 ~ 100）时必须同步改这里。
     */
    public static final int DEFAULT_SQUASH_PERIOD_TICKS = 6;

    private static final double TWO_PI = Math.PI * 2.0;

    /** 效果实例此刻在不在：姿态分支的唯一判据（「时长还剩几刻」不参与判据）。 */
    private static boolean active;

    /** 本地累加的相位刻数 t；效果消失当刻归零。 */
    private static int phaseTicks;

    /** 服务端下发的剩余时长（只读快照，供 F-108 的观感核对；**不用它反推相位**）。 */
    private static int remainingTicks;

    private QbounceClientState() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        MobEffectInstance instance = player == null ? null
                : player.getEffect(FarmstormMobEffects.Q_BOUNCE);
        if (instance == null) {
            // 效果消失的下一 tick 就归零：模型当刻回到原尺寸 / 原朝向，且只打一行（不刷屏）。
            if (active || phaseTicks != 0) {
                active = false;
                phaseTicks = 0;
                remainingTicks = 0;
                Farmstorm.LOGGER.info("q_bounce t=0");
            }
            return;
        }
        active = true;
        phaseTicks++;
        remainingTicks = instance.getDuration();
    }

    /** 效果在不在（唯一的姿态判据）。 */
    public static boolean active() {
        return active;
    }

    /** 服务端下发的剩余刻数；无效果时为 0。仅作读数，不参与相位计算。 */
    public static int remainingTicks() {
        return remainingTicks;
    }

    /** 本地累加的相位刻数 t（进入效果起逐刻 +1，消失归零）。 */
    public static int phaseTicks() {
        return phaseTicks;
    }

    /** 当前旋转角 θ = rotateDegreesPerTick × t（mod 360）。 */
    public static float rotateDegrees() {
        return (DEFAULT_ROTATE_DEGREES_PER_TICK * phaseTicks) % 360.0F;
    }

    /** 当前压缩量 s = squashAmplitude × sin(2π t / squashPeriodTicks)，取值 [-0.15, 0.15]。 */
    public static float squash() {
        return (float) (DEFAULT_SQUASH_AMPLITUDE
                * Math.sin(TWO_PI * phaseTicks / DEFAULT_SQUASH_PERIOD_TICKS));
    }

    /** 压缩拉伸的 X 缩放 = 1 − s / 2（与 Y 反相：Y 拉长时 X/Z 收窄）。 */
    public static float squashScaleX() {
        return 1.0F - squash() / 2.0F;
    }

    /** 压缩拉伸的 Y 缩放 = 1 + s。 */
    public static float squashScaleY() {
        return 1.0F + squash();
    }

    /** 压缩拉伸的 Z 缩放 = 1 − s / 2（与 X 同向）。 */
    public static float squashScaleZ() {
        return 1.0F - squash() / 2.0F;
    }
}
