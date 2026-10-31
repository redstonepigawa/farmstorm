package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.effect.QbounceEffect;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Q弹 的「告知观察者」链路（0.0.4/E）：用<b>原版包</b>把「谁带着 {@code farmstorm:q_bounce}」同步给附近的玩家。
 *
 * <p><b>为什么需要这条链路</b>：原版只把 {@code ClientboundUpdateMobEffectPacket} 发给实体自己那条连接
 * （{@code ServerPlayer#onEffectAdded} 发给自己，{@code LivingEntity#sendEffectToPassengers} 发给乘客），
 * 观察者的客户端根本不知道别人身上有这个效果，于是 {@code QbouncePoseRenderer.appliesTo()} 对被观察实体恒为
 * false、{@code QbounceClientState} 的相位表在实践中为空。上一批用双 mineflayer 账号实测确认了这一点
 * （Dev 自己的连接收到 {@code entity_effect}，Guest 一条没有；换成僵尸同样如此）。
 *
 * <p><b>为什么复用原版包</b>：D-50 的红线是「不新增自定义网络包」（原版 {@code custom_payload} 那一族类型）。这里用的两个都是原版
 * 效果同步包，与循环音复用 {@code ClientboundSoundEntityPacket} 是同一个口径。观察者客户端收到后走原版
 * {@code ClientPacketListener#handleUpdateMobEffect} → {@code LivingEntity#forceAddEffect}，与「效果本来就同步」
 * 没有任何区别；本类不发明语义，只是把原版漏发的那一份补上。
 *
 * <p><b>标志位逐字照抄实体身上那份</b>：构造器直接读 {@code MobEffectInstance} 的 amplifier / duration /
 * ambient / visible / showIcon，所以这里传的就是 {@code living.getEffect(Q_BOUNCE)} 本身，不自己拼参数——
 * 拼错一个标志位，观察者的客户端就会多画粒子或图标。第三个参数 {@code blend} 传 {@code false}：它只服务
 * 「自己那条 HUD 效果条的叠加」，观察者没有那条 HUD；原版给非持有者发包时也是 {@code false}。
 *
 * <p><b>节奏</b>：每 {@link #BROADCAST_INTERVAL_TICKS} 刻（20 刻 = 1 秒）重发一轮。重发让「中途进入半径的玩家
 * 最多等 1 秒就能看到」成立，同时把观察者那份的剩余时长刷新成最新值（每次重发都带当刻 duration）。
 *
 * <p><b>清除不能靠 duration 自然过期</b>：客户端 {@code LivingEntity#tickEffects} 里删表那段有
 * {@code !isClientSide} 守卫，所以时长归零后那条实例会留在观察者的 {@code activeEffects} 里
 * （{@code hasEffect()} 继续为 true，姿态会一直压在别人身上）。移除包因此是必需的，而且要在早退时立刻发：
 * 效果到期（{@link MobEffectEvent.Expired}）、被清除（{@link MobEffectEvent.Remove}）、实体死亡
 * （{@link LivingDeathEvent}）三条快路径各自直接发；每轮扫描再兜底处理「实体被移除 / 区块卸载 / 掉线 /
 * 换维度」这些没有事件可挂的情况。
 *
 * <p><b>代价</b>：每 20 刻把<b>所有维度</b>的已加载实体走一遍（与 {@code QbounceMountController} 的补扫同一个形状，
 * 但这里是常态化的）。一轮扫描只是内存遍历，发包数只跟「半径内的玩家数 × 带效果的实体数」有关。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class QbounceObserverSync {

    /**
     * 重发间隔（刻）＝ 20。取 1 秒的理由与循环音一致（{@code WildHunterAura} 的
     * {@code LOOP_BROADCAST_INTERVAL_TICKS}）：这是「中途进入半径的玩家多久能看到」的上界，
     * 再密只是白白多发，再疏就是肉眼可见的延迟。
     */
    private static final int BROADCAST_INTERVAL_TICKS = 20;

    /**
     * 发包半径（格）＝ 64。够住「同屏可见」的全部玩家：原版玩家实体的追踪距离默认就是 48 格，
     * 服务端 {@code entity-tracking-range} 调大后也不会超过 64 太多；超出这个距离的玩家客户端里
     * 根本没有这个实体（{@code level.getEntity(id)} 返回 null），包会被直接丢掉。
     */
    private static final double OBSERVER_RADIUS = 64.0D;

    /**
     * 移除包的半径（格）＝ 128（发包半径的两倍）。<b>比发包半径大是刻意的</b>：发包半径只是「这一轮谁在场」，
     * 不是成员名单；收到过更新包、之后走远到 64 格外的玩家，客户端里那份效果还在，必须也收到移除包。
     * 收不到更新包的客户端收到移除包是安全的空操作（{@code handleRemoveMobEffect} → {@code removeEffectNoUpdate}，
     * 表里没有这条 key 就什么也不做），所以宁可多发。
     */
    private static final double REMOVE_RADIUS = OBSERVER_RADIUS * 2.0D;

    /** 只存内存：实体 UUID → 它上一次被播报时的位置与实体 id。服务器重启即清空，不写存档。 */
    private static final Map<UUID, Tracked> TRACKED = new HashMap<>();

    private QbounceObserverSync() {}

    /**
     * 一条播报记录。{@code entityId} 只作移除包的寻址用；位置用于「已经查不到这个实体」时算半径。
     * <p>{@code dimension} 不是 final：玩家走过传送门时实体 id 不变、实体换了 level，记录必须跟着换维度，
     * 否则效果结束时移除包会发到<b>旧维度</b>，新维度的观察者会一直留着那份效果。
     */
    private static final class Tracked {
        private ResourceKey<Level> dimension;
        private final int entityId;
        private double x;
        private double y;
        private double z;

        private Tracked(ResourceKey<Level> dimension, Entity entity) {
            this.dimension = dimension;
            this.entityId = entity.getId();
            refresh(dimension, entity);
        }

        private void refresh(ResourceKey<Level> dimension, Entity entity) {
            this.dimension = dimension;
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }
    }

    /** 主循环：每 20 刻一轮（用全局游戏刻取模，与循环音的节奏口径一致）。 */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.overworld().getGameTime() % BROADCAST_INTERVAL_TICKS != 0) {
            return;
        }
        broadcast(server);
    }

    /** 快路径①：效果被清除 / 被治愈（{@code /effect clear}、牛奶、别的模组摘效果）。 */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        stop(event.getEntity(), event.getEffectInstance(), "效果被清除");
    }

    /** 快路径②：效果到时自然结束。 */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        stop(event.getEntity(), event.getEffectInstance(), "效果到期");
    }

    /**
     * 快路径③：实体死亡。
     * <p>死亡<b>不会</b>摘掉效果（21.1.213 里 {@code removeAllEffects()} 只有 {@code /effect clear} 一处调用），
     * 所以只靠扫描是不够的：尸体还在世界里、还带着效果，扫描会继续把「它带着 q_bounce」播报下去。
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        stop(event.getEntity(), event.getEntity().getEffect(FarmstormMobEffects.Q_BOUNCE), "实体死亡");
    }

    /** 停服清表：集成服务器的单进程里可能先后跑过多个存档，静态表不能跨世界残留。 */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TRACKED.clear();
    }

    /**
     * 一趟扫描做完三件事：新发现的实体登记并发包、已在表里的实体重发并刷新位置、这一轮没看到的一律摘除并发移除包。
     * <p>「没看到」这一条把四种退出路径收在同一个判据上：效果没了、实体被移除、区块卸载、掉线。
     */
    private static void broadcast(MinecraftServer server) {
        Set<UUID> seen = new HashSet<>();
        int dispatched = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity living) || living.isRemoved() || !living.isAlive()) {
                    continue;                              // 死亡/被移除的实体不再播报（移除包由快路径或下一段发）
                }
                MobEffectInstance instance = living.getEffect(FarmstormMobEffects.Q_BOUNCE);
                if (instance == null) {
                    continue;
                }
                UUID id = living.getUUID();
                seen.add(id);
                Tracked record = TRACKED.get(id);
                if (record == null || record.entityId != living.getId()) {
                    TRACKED.put(id, new Tracked(level.dimension(), living));
                    Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                            "q_bounce 观察者同步 开始 实体=%s id=%d 维度=%s",
                            living.getType(), living.getId(), level.dimension().location()));
                } else {
                    record.refresh(level.dimension(), living);
                }
                dispatched += sendUpdate(level, living, instance);
            }
        }
        for (Iterator<Map.Entry<UUID, Tracked>> it = TRACKED.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, Tracked> entry = it.next();
            if (!seen.contains(entry.getKey())) {
                it.remove();
                Tracked record = entry.getValue();
                ServerLevel level = server.getLevel(record.dimension);
                if (level == null) {
                    Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                            "q_bounce 观察者同步 结束 reason=效果结束或实体移除 实体 id=%d（维度已卸载，无包可发）",
                            record.entityId));
                } else {
                    sendRemove(level, entry.getKey(), record, "效果结束或实体移除");
                }
            }
        }
        if (dispatched > 0) {
            Farmstorm.LOGGER.debug("q_bounce 观察者同步 本轮更新包 {} 个（带效果实体 {} 个，半径 {} 格）",
                    dispatched, seen.size(), (int) OBSERVER_RADIUS);
        }
    }

    /** 向半径内的观察者发更新包，排除持有者本人。返回发包数（只作读数）。 */
    private static int sendUpdate(ServerLevel level, LivingEntity holder, MobEffectInstance instance) {
        ClientboundUpdateMobEffectPacket packet = null;
        double radiusSqr = OBSERVER_RADIUS * OBSERVER_RADIUS;
        int sent = 0;
        for (ServerPlayer observer : level.players()) {
            if (observer.getUUID().equals(holder.getUUID()) || observer.distanceToSqr(holder) > radiusSqr) {
                continue;                                  // 排除持有者本人：他自己那份由原版发（blend=true），重复发会叠加
            }
            if (packet == null) {
                packet = new ClientboundUpdateMobEffectPacket(holder.getId(), instance, false);
            }
            observer.connection.send(packet);
            sent++;
        }
        return sent;
    }

    /** 向半径内的观察者发移除包，同样排除持有者本人。 */
    private static void sendRemove(ServerLevel level, UUID holderId, Tracked record, String reason) {
        ClientboundRemoveMobEffectPacket packet =
                new ClientboundRemoveMobEffectPacket(record.entityId, FarmstormMobEffects.Q_BOUNCE);
        double radiusSqr = REMOVE_RADIUS * REMOVE_RADIUS;
        int sent = 0;
        for (ServerPlayer observer : level.players()) {
            if (observer.getUUID().equals(holderId)
                    || observer.distanceToSqr(record.x, record.y, record.z) > radiusSqr) {
                continue;
            }
            observer.connection.send(packet);
            sent++;
        }
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "q_bounce 观察者同步 结束 reason=%s 实体 id=%d 已通知 %d 人（半径 %d 格）",
                reason, record.entityId, sent, (int) REMOVE_RADIUS));
    }

    /** 三条快路径共用的出口：确认是本效果 + 在服务端 + 表里有它，才摘表并发移除包。 */
    private static void stop(LivingEntity living, MobEffectInstance instance, String reason) {
        if (instance == null || !(instance.getEffect().value() instanceof QbounceEffect)) {
            return;
        }
        if (!(living.level() instanceof ServerLevel level)) {
            return;
        }
        Tracked record = TRACKED.remove(living.getUUID());
        if (record == null) {
            return;                                        // 没播报过（半径内一直没人）：不用发
        }
        record.refresh(level.dimension(), living);
        sendRemove(level, living.getUUID(), record, reason);
    }
}
