package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.effect.QbounceEffect;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Q弹 的 {@code mount} 分支（TECH §4.3 / F-109）：服务端给每个受 Q弹 的生物挂一个隐形的
 * {@code minecraft:armor_stand}（D-102 / A-12），让「坐着」这件事由原版骑乘通道真实发生。
 *
 * <p><b>只在这条分支读 {@code qbounce.poseMode}</b>（服务端 config，客户端不镜像）：{@code mount} 时挂坐骑，
 * 其余值（{@code render}）时把表里剩下的坐骑全部 {@code discard()} 并清表（§4.1 的切换时机）。
 *
 * <p><b>记账表只存内存</b>（{@link #ACTIVE}，乘客 UUID → 坐骑 UUID + 失效 tick + 重挂次数），与
 * {@code ZangiefPiledriverController.ACTIVE} 同构：服务器重启即清空，不写存档。
 *
 * <p><b>清理的层次</b>：事件是快路径（死亡 / 掉线 / 换维度 / 停服各自立即 discard），{@link ServerTickEvent.Post}
 * 的三条校验是兜底（效果结束 / 实体被移除 / 坐骑被 kill 都不依赖任何事件命中）。RT-18 的「坐骑残留把玩家钉住」
 * 只能靠这两层同时成立来避免。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class QbounceMountController {

    /** {@code qbounce.poseMode} 里选中的那个值（TECH §4.1 的 enum 取值之一，写死在 {@code defineInList} 里）。 */
    private static final String POSE_MODE_MOUNT = "mount";

    /**
     * 重挂上限（RT-18 兜底②）：别的模组每 tick 调 {@code stopRiding} 时，重挂会变成抖动，超过这个次数就放弃
     * 并只打<b>一行</b> warn（不刷屏）。
     */
    public static final int MAX_REMOUNT = 20;

    /**
     * 重启后补扫的间隔（刻）＝ 20。一次补扫要把所有维度里<b>全部</b>已加载实体走一遍，逐刻扫没有意义；
     * 1 秒的粒度仍然让「重启后自动认领」在用户看来是即时的——存档实体在起服头几刻就回到世界，
     * 20 刻既不会跨过这个窗口，也把补扫成本压到每秒最多一次。
     */
    private static final int ADOPT_RESCAN_INTERVAL_TICKS = 20;

    /**
     * 补扫次数上限 ＝ 10（另加翻转那一 tick 的第一次扫描，每次翻转最多 11 次世界扫描）。
     * 10 次 × 20 刻 ≈ 起服后 10 秒：起服 10 秒还没把存档实体放回世界属于异常（日志里会有区块加载报错），
     * 再扫只是空转；用完即彻底停扫，长跑的服务器不会长期付这份开销，也不会对「永远够不着」的实体无限重试。
     */
    private static final int MAX_ADOPT_RESCANS = 10;

    /** 只存内存：乘客 UUID → 坐骑记录。服务器重启即清空，不写存档。 */
    private static final Map<UUID, MountRecord> ACTIVE = new HashMap<>();

    /**
     * 上一个 tick 是否处于 {@code mount} 模式：翻转那一 tick 扫一次世界，把已经在效果里的生物认领进来
     * （§4.1：切到 mount 时，已在效果中的生物从下一个 tick 起挂坐骑），平时每 tick 只看表里的记录。
     * <p>初值 {@code false} 让「起服后第一个 tick」也算一次翻转：存档里带着效果的生物在重启后同样会被认领。
     * <p>V-101：翻转那一 tick 是<b>唯一</b>机会时，重启后实测从未生效——存档实体要等区块被加载进世界
     * 才会出现在 {@code getAllEntities()} 里，那个 tick 往往还没到。所以认领改成「翻转 tick + 有界补扫」
     * （见 {@link #ADOPT_RESCAN_INTERVAL_TICKS} / {@link #MAX_ADOPT_RESCANS}）。
     */
    private static boolean mountModeArmed;

    /** 本次 {@code mount} 期还允许补扫几次（从 {@link #MAX_ADOPT_RESCANS} 起算；0 ＝ 不再扫）。 */
    private static int adoptRescansLeft;

    /** 距离下一次补扫还要等几刻。 */
    private static int adoptRescanCooldown;

    private QbounceMountController() {}

    /** 一个乘客当前的坐骑。{@code expiresAtTick} 只作读数（判据永远是「效果还在不在」）。 */
    private static final class MountRecord {
        private final ResourceKey<Level> dimension;
        private final UUID mountId;
        private final long expiresAtTick;
        private int reMountCount;

        private MountRecord(ResourceKey<Level> dimension, UUID mountId, long expiresAtTick) {
            this.dimension = dimension;
            this.mountId = mountId;
            this.expiresAtTick = expiresAtTick;
        }
    }

    /** 进入路径：效果刚被加上的那一瞬间（{@code /effect give}、投掷物、药水都走这里）。 */
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity living = event.getEntity();
        if (!(living.level() instanceof ServerLevel)) {
            return;                                   // 只在服务端挂坐骑（HC-03）
        }
        if (!(event.getEffectInstance().getEffect().value() instanceof QbounceEffect)) {
            return;
        }
        if (event.getOldEffectInstance() != null) {
            return;                                   // 旧实例还在 ⇒ 不是「新进入效果」，不重复挂
        }
        ensureMount(living);
    }

    /** 快路径②：生物死亡——立刻 discard，不等下一个 tick（§4.3「随生物死亡清理」）。 */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        release(level.getServer(), event.getEntity().getUUID(), "实体死亡");
    }

    /** 快路径：掉线——玩家实体从世界里消失，坐骑不能跟着留。 */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        release(level.getServer(), event.getEntity().getUUID(), "掉线");
    }

    /** 快路径：换维度——同一份玩家实体换了 level，旧 level 里查不到它，坐骑留在原地就是残留。 */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        release(level.getServer(), event.getEntity().getUUID(), "换维度");
    }

    /**
     * 快路径：停服<b>之前</b>——世界与实体都还在，此刻 discard 才真正生效。写在存档里的隐形坐骑会在下次起服时
     * 变成「把乘客钉住的孤儿实体」（RT-18 的主风险）。
     * <p>实测读数：{@code ServerStoppedEvent} 阶段 {@code getLevel(dimension).getEntity(...)} 已经返回 null
     * （世界已关闭），在那种时候只能清表、discard 是空操作——所以真正的清理放在这一阶段。
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        releaseAll(event.getServer(), "服务器即将停止");
    }

    /** 兜底：停服之后仍保证表是空的（万一 {@code ServerStoppingEvent} 那条路径没跑到）。 */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        releaseAll(event.getServer(), "服务器停止");
    }

    /**
     * 主循环 + 兜底：三条校验同时成立才保留坐骑（§4.3），否则 discard 并清表。
     * <p>{@code poseMode} 切回 {@code render} 的那个 tick 也在这里收口（§4.1）。
     * <p>翻转 tick 之后还有 {@link #MAX_ADOPT_RESCANS} 轮补扫（每 {@link #ADOPT_RESCAN_INTERVAL_TICKS} 刻一次），
     * 补完即停——这是 V-101 的修法，见 {@link #mountModeArmed}。
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!isMountMode()) {
            if (mountModeArmed || !ACTIVE.isEmpty()) {
                mountModeArmed = false;
                adoptRescansLeft = 0;
                adoptRescanCooldown = 0;
                releaseAll(server, "切回 render");
            }
            return;
        }
        if (!mountModeArmed) {
            mountModeArmed = true;
            adoptExisting(server, 0);                  // 翻转那一 tick 的即时认领（原有行为，不变）
            adoptRescansLeft = MAX_ADOPT_RESCANS;
            adoptRescanCooldown = ADOPT_RESCAN_INTERVAL_TICKS;
        } else if (adoptRescansLeft > 0 && --adoptRescanCooldown <= 0) {
            adoptRescanCooldown = ADOPT_RESCAN_INTERVAL_TICKS;
            adoptRescansLeft--;
            adoptExisting(server, MAX_ADOPT_RESCANS - adoptRescansLeft);
        }
        tickRecords(server);
    }

    /** {@code qbounce.poseMode} 是否为 {@code mount}：只有服务端路径会调它（客户端读不到 server config）。 */
    private static boolean isMountMode() {
        return POSE_MODE_MOUNT.equalsIgnoreCase(FarmstormConfig.qbouncePoseMode.get());
    }

    /**
     * §12.3：正拉着拴绳的生物不挂隐形坐骑。
     * <p>21.1.213 里 {@code isLeashed()} 是 {@link Leashable} 的 default 方法（只有 {@code Mob} 实现它），
     * {@link Entity} / {@link LivingEntity} 都没有这个方法，所以先 instanceof 再问。
     */
    private static boolean isLeashed(LivingEntity living) {
        return living instanceof Leashable leashable && leashable.isLeashed();
    }

    /**
     * 认领扫描（§4.1 的翻转 tick + §12.3 的补扫共用）：先收集再挂，避免边遍历实体列表边 addFreshEntity。
     * <p>{@code scan} 只作读数：0 ＝ 翻转那一 tick，1..{@link #MAX_ADOPT_RESCANS} ＝ 重启后的补扫轮次。
     * <p>行尾打一行「扫描#k 候选/新挂/表内/游戏刻」，补扫上限内最多 11 行，运维据此判断重启后是第几刻认领上的。
     */
    private static void adoptExisting(MinecraftServer server, int scan) {
        List<LivingEntity> candidates = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof LivingEntity living
                        && !living.isRemoved()
                        && !living.isPassenger()
                        && !isLeashed(living)
                        && !ACTIVE.containsKey(living.getUUID())
                        && living.hasEffect(FarmstormMobEffects.Q_BOUNCE)) {
                    candidates.add(living);
                }
            }
        }
        int before = ACTIVE.size();
        for (LivingEntity living : candidates) {
            ensureMount(living);
        }
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "q_bounce mount 认领扫描#%d 候选=%d 新挂=%d 表内=%d 游戏刻=%d",
                scan, candidates.size(), ACTIVE.size() - before, ACTIVE.size(),
                server.overworld().getGameTime()));
    }

    /** 挂一个隐形坐骑：五项属性覆写 → {@code addFreshEntity} → {@code startRiding(force)} → 记账。 */
    private static void ensureMount(LivingEntity living) {
        if (!isMountMode() || !(living.level() instanceof ServerLevel level)) {
            return;
        }
        UUID passengerId = living.getUUID();
        if (ACTIVE.containsKey(passengerId) || living.isPassenger() || isLeashed(living)) {
            return;                                   // 已在表里 / 已经有坐骑（别抢别人的马）/ 正拉着拴绳（§12.3）
        }
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        if (stand == null) {
            return;
        }
        configure(stand);
        stand.moveTo(living.getX(), living.getY(), living.getZ(), living.getYRot(), 0.0F);
        if (!level.addFreshEntity(stand)) {
            stand.discard();
            return;
        }
        if (!living.startRiding(stand, true)) {
            stand.discard();
            Farmstorm.LOGGER.warn(String.format(Locale.ROOT,
                    "q_bounce mount 挂载失败 乘客=%s（startRiding 返回 false）", passengerId));
            return;
        }
        MobEffectInstance instance = living.getEffect(FarmstormMobEffects.Q_BOUNCE);
        long expiresAtTick = level.getGameTime() + (instance == null ? 0 : instance.getDuration());
        ACTIVE.put(passengerId, new MountRecord(level.dimension(), stand.getUUID(), expiresAtTick));
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "q_bounce mount 挂上 乘客=%s 坐骑=%s 维度=%s 失效 tick=%d 表内=%d",
                passengerId, stand.getUUID(), level.dimension().location(), expiresAtTick, ACTIVE.size()));
    }

    /**
     * 五项属性覆写（§4.3 + A-12）：隐形、无重力、无敌、无声、marker。
     * <p>21.1.213 里 {@code ArmorStand#setMarker} 与 {@code setSmall} 都是 <b>private</b>，只能改同步数据位
     * {@link ArmorStand#DATA_CLIENT_FLAGS}，改完必须 {@code refreshDimensions()}（原版 private setMarker
     * 内部就是这么做的）。RT-18 兜底①的「marker 若连带取消坐姿，退回 setSmall(true)」也走同一个口子。
     */
    private static void configure(ArmorStand stand) {
        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        setClientFlag(stand, (byte) ArmorStand.CLIENT_FLAG_MARKER);
        stand.refreshDimensions();
    }

    private static void setClientFlag(ArmorStand stand, byte flag) {
        byte flags = stand.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS);
        stand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, (byte) (flags | flag));
    }

    /** 三条校验（§4.3）：效果还在 + 乘客还是它 + 坐骑还在。任一不成立就走退出路径。 */
    private static void tickRecords(MinecraftServer server) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        for (UUID passengerId : new ArrayList<>(ACTIVE.keySet())) {
            MountRecord record = ACTIVE.get(passengerId);
            if (record != null) {
                validate(server, passengerId, record);
            }
        }
    }

    private static void validate(MinecraftServer server, UUID passengerId, MountRecord record) {
        ServerLevel level = server.getLevel(record.dimension);
        Entity passenger = level == null ? null : level.getEntity(passengerId);
        if (!(passenger instanceof LivingEntity living)
                || living.isRemoved()
                || !living.isAlive()
                || !living.hasEffect(FarmstormMobEffects.Q_BOUNCE)) {
            release(server, passengerId, "效果结束或实体移除");     // 退出路径①/④
            return;
        }
        Entity vehicle = level.getEntity(record.mountId);
        if (!(vehicle instanceof ArmorStand stand) || stand.isRemoved()) {
            release(server, passengerId, "坐骑消失");               // 退出路径③
            return;
        }
        if (living.getVehicle() == stand) {
            return;                                                // 三条同时成立：保留
        }
        record.reMountCount++;                                     // ②不成立：被第三方强制下车
        if (record.reMountCount > MAX_REMOUNT) {
            release(server, passengerId, "重挂超过上限");
            Farmstorm.LOGGER.warn(String.format(Locale.ROOT,
                    "q_bounce mount 放弃 乘客=%s 已被反复下车 %d 次（上限 %d）",
                    passengerId, record.reMountCount, MAX_REMOUNT));
            return;
        }
        if (!living.startRiding(stand, true)) {
            release(server, passengerId, "重挂失败");
        }
    }

    /** 单条退出：清表 + discard 坐骑 + 一行读数（剩余条数就是「表已清」的证据）。 */
    private static void release(MinecraftServer server, UUID passengerId, String reason) {
        MountRecord record = ACTIVE.remove(passengerId);
        if (record == null) {
            return;
        }
        boolean discarded = discardMount(server, record);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "q_bounce mount 释放 reason=%s 乘客=%s 坐骑=%s discarded=%s 表内=%d",
                reason, passengerId, record.mountId, discarded, ACTIVE.size()));
    }

    /** 整表退出：切回 render 与停服共用。 */
    private static void releaseAll(MinecraftServer server, String reason) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        int total = ACTIVE.size();
        int discarded = 0;
        for (MountRecord record : ACTIVE.values()) {
            if (discardMount(server, record)) {
                discarded++;
            }
        }
        ACTIVE.clear();
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "q_bounce mount 清表 reason=%s 共 %d 个（discard %d）", reason, total, discarded));
    }

    private static boolean discardMount(MinecraftServer server, MountRecord record) {
        ServerLevel level = server.getLevel(record.dimension);
        Entity vehicle = level == null ? null : level.getEntity(record.mountId);
        if (vehicle == null) {
            return false;
        }
        vehicle.discard();
        return true;
    }
}
