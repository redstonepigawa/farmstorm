package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
 *
 * <p><b>0.0.4 的改动：相位从「一份」变成「每实体一份」</b>。0.0.3 只有上面那一份相位（本机玩家的本地累加刻数），
 * 渲染器把它套给了同屏**所有**带 {@code q_bounce} 的实体，于是「本机玩家没带效果、别人带了」时
 * {@code t} 恒为 0（旋转 0°、压缩 1.0），别人的模型看起来纹丝不动。现在每 tick 额外维护一张客户端内存表
 * {@code ENTER_TICKS}（实体 id → 它第一次被看到带着该效果的刻号），{@link QbouncePoseRenderer} 按
 * 「当前刻 − 进入刻」给每个实体算自己的相位；三个姿态参数仍是本类的镜像常量（不发包、不读 config）。
 * 本机玩家**不进这张表**，它的相位仍走 {@link #phaseTicks} 的本地累加刻数——0.0.3 能动的行为逐字不变。
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

    /** 本机玩家的本地累加相位刻数 t；效果消失当刻归零。 */
    private static int phaseTicks;

    /** 服务端下发的剩余时长（只读快照，供 F-108 的观感核对；**不用它反推相位**）。 */
    private static int remainingTicks;

    /**
     * 客户端刻计数器：每 tick +1，是 {@code ENTER_TICKS} 里所有相位的唯一时钟。
     * 不用 {@code level.getGameTime()}：那张表的清理与相位只依赖「客户端走了几刻」，与存档时间无关，
     * 换维度 / 换世界时也不会因为时间跳变把相位拉飞。
     */
    private static long tickCounter;

    /**
     * 实体 id → 该实体**第一次**被客户端看到带着 {@code farmstorm:q_bounce} 的刻号（{@code tickCounter} 口径）。
     * 纯客户端内存表：不发包、不写回实体（D-81 的口径不变）。**只放非本机玩家**——本机玩家走
     * {@link #phaseTicks}。表项在 {@link #trackOtherEntities} 的每刻清理里删除：死亡、卸载、走出视野
     * （客户端实体被移除）、效果到点都会落到同一条「这一 tick 没看到」上，所以表不会随对局变长而泄漏。
     */
    private static final Map<Integer, Long> ENTER_TICKS = new HashMap<>();

    private QbounceClientState() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tickCounter++;
        Minecraft minecraft = Minecraft.getInstance();
        tickLocalPlayer(minecraft.player);
        trackOtherEntities(minecraft.level);
    }

    /** 本机玩家那一条（0.0.3 的逻辑逐字保留）：在不在 / 相位 / 剩余刻数。 */
    private static void tickLocalPlayer(LocalPlayer player) {
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

    /**
     * 每刻维护「实体 id → 进入刻」表：新看到的带效果实体登记一次，这一 tick 没看到的一律删掉。
     * <p>登记与删除各打一行日志（每次状态变化一行，不是每帧一行）：这既是缺陷修复的现场读数
     * 「实体 X 的相位从第 n 刻开始」，也是 0.0.4 在没有真实客户端可自动化的前提下唯一能给出的运行时证据。
     *
     * <p><b>这张表还有一个前提，0.0.4 的实测已经证伪了它</b>：表只在「客户端知道这个实体带着效果」时才填得上，
     * 而原版对**非本机**实体不下发效果实例（{@code ClientboundUpdateMobEffectPacket} 只发给实体自己那条连接）。
     * 实测（2026-10-31，Dev/Guest 两个 mineflayer 账号连同一个 NeoForge 21.1.213 服务端）：给 Dev 施加
     * {@code farmstorm:q_bounce} 后 Dev 自己的连接收到 {@code entity_effect}（entityId=7, effectId=41），
     * Guest 的连接一条都没有（只收到 {@code entity_metadata} 里的效果颜色）；给僵尸施加效果，Guest 同样收不到。
     * 所以 {@code hasEffect()} 在观察者侧对被观察实体恒为 false，{@link QbouncePoseRenderer} 的进入条件直接返回
     * false，本表在实践中为空——「看不见别人的 Q弹」因此还有第二层原因，需要一条把「谁带着 Q弹」告诉观察者的链路
     * （服务端广播，或换一个客户端可观测的触发信号）。那不在 0.0.4 本批的硬边界内，本类只负责相位。
     */
    private static void trackOtherEntities(ClientLevel level) {
        if (level == null) {
            if (!ENTER_TICKS.isEmpty()) {
                ENTER_TICKS.clear();
                Farmstorm.LOGGER.info("q_bounce phase table cleared (left the level)");
            }
            return;
        }
        LocalPlayer self = Minecraft.getInstance().player;
        Set<Integer> seen = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity == self || !(entity instanceof LivingEntity living)
                    || !living.hasEffect(FarmstormMobEffects.Q_BOUNCE)) {
                continue;
            }
            int id = entity.getId();
            seen.add(id);
            if (ENTER_TICKS.putIfAbsent(id, tickCounter) == null) {
                Farmstorm.LOGGER.info("q_bounce phase start: entity={} id={} t=0", living.getType(), id);
            }
        }
        for (Iterator<Map.Entry<Integer, Long>> it = ENTER_TICKS.entrySet().iterator(); it.hasNext();) {
            Integer id = it.next().getKey();
            if (!seen.contains(id)) {
                it.remove();
                Farmstorm.LOGGER.info("q_bounce phase drop: id={} (dead / unloaded / out of view / effect over)", id);
            }
        }
    }

    /** 效果在不在（唯一的姿态判据；输入锁也读它，0.0.4 不动这条语义）。 */
    public static boolean active() {
        return active;
    }

    /** 服务端下发的剩余刻数；无效果时为 0。仅作读数，不参与相位计算。 */
    public static int remainingTicks() {
        return remainingTicks;
    }

    /** 本机玩家的本地累加相位刻数 t（进入效果起逐刻 +1，消失归零）。 */
    public static int phaseTicks() {
        return phaseTicks;
    }

    /**
     * 该实体自己的相位刻数。
     * <p>本机玩家 → {@link #phaseTicks}（0.0.3 的唯一相位，行的还是同一行代码，所以行为不变）；
     * 其它实体 → 当前刻 − 它自己进表的那一 tick（表里没有就是 0：旋转 0°、压缩 1.0，与原尺寸一致）。
     * 负数与溢出都夹到 {@code [0, Integer.MAX_VALUE]}，避免表被外部改坏时算出倒退的相位。
     */
    public static int phaseTicksOf(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        if (entity == Minecraft.getInstance().player) {
            return phaseTicks;
        }
        Long enter = ENTER_TICKS.get(entity.getId());
        if (enter == null) {
            return 0;
        }
        long delta = tickCounter - enter;
        if (delta <= 0L) {
            return 0;
        }
        return (int) Math.min(delta, Integer.MAX_VALUE);
    }

    /** 当前旋转角 θ = rotateDegreesPerTick × t（mod 360）。 */
    public static float rotateDegrees(int t) {
        return (DEFAULT_ROTATE_DEGREES_PER_TICK * t) % 360.0F;
    }

    /** 当前压缩量 s = squashAmplitude × sin(2π t / squashPeriodTicks)，取值 [-0.15, 0.15]。 */
    public static float squash(int t) {
        return (float) (DEFAULT_SQUASH_AMPLITUDE
                * Math.sin(TWO_PI * t / DEFAULT_SQUASH_PERIOD_TICKS));
    }

    /** 压缩拉伸的 X 缩放 = 1 − s / 2（与 Y 反相：Y 拉长时 X/Z 收窄）。 */
    public static float squashScaleX(int t) {
        return 1.0F - squash(t) / 2.0F;
    }

    /** 压缩拉伸的 Y 缩放 = 1 + s。 */
    public static float squashScaleY(int t) {
        return 1.0F + squash(t);
    }

    /** 压缩拉伸的 Z 缩放 = 1 − s / 2（与 X 同向）。 */
    public static float squashScaleZ(int t) {
        return 1.0F - squash(t) / 2.0F;
    }
}
