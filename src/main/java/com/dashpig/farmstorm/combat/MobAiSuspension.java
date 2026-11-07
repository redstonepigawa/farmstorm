package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.QbounceMountController;
import com.dashpig.farmstorm.registry.FarmstormAttachments;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent;

/**
 * 「删 AI」的唯一入口（41 号 §4 ~ §5、42 号 §2.3.1 ~ §2.3.7、裁定 #4 / #5 / #6 / #7 / #8、A-79 / A-80 / A-81）。
 *
 * <p><b>原值落盘在实体附件</b>（{@link FarmstormAttachments#AI_SUSPEND}，
 * {@link #Suspend} 只有两个字段：原值与来源位掩码）。附件随实体写进 {@code neoforge:attachments}，
 * 所以停服 / 崩溃都不会丢原值；内存里只有两件可丢弃的索引——{@link #OBSERVED}（要每刻对账的实体）
 * 与 {@link #VB_UNTIL}（震动豆的内存到期刻，裁定 #16：窗口不跨档）。
 *
 * <p><b>进入顺序写死</b>：先读原值 → 再写附件 → 最后 {@code setNoAi(true)}。反过来的话，崩在中间就是
 * 「没 AI、也没原值」的永久脏状态（RT-54 的第一个入口）。
 *
 * <p><b>退出只有一个出口</b>：{@link #release} 末尾必 {@code removeData}；「是否处于挂起」只看
 * {@code sources != 0}，不看 {@code hasData}——{@code getData} 取默认值时会把它写进 map
 * （{@code AttachmentHolder.java:74-83}，实测），用 {@code hasData} 判会造出空记录。
 *
 * <p><b>五条清理路径</b>（42 号 §2.3.3）：① Q弹 自然到期 / ② {@code /effect clear} / ③ 牛奶 三条统一由
 * 每刻对账读 {@code hasEffect} 收敛（一个移除事件都不挂，少一处「事件没送达」的失效面）；④ 死亡只删附件、
 * 不写 {@code noAi}；⑤ 区块卸载 <b>保留附件、载入时结算</b>（A-80：卸载那一刻实体不在世界里，写不了
 * {@code setNoAi}）。「观测不到」只丢内存两件，附件一律留着；不设「多少刻后放弃」这类封顶常量（A-79）。
 */
public final class MobAiSuspension {

    /**
     * 原值载体（裁定 #15 / #16）：{@code originalNoAi} 是**第一次**进入挂起前读到的 {@code isNoAi()}，
     * {@code sources} 是还在挂起的来源位掩码。不存任何时间戳——落盘记录里没有时点，回档 / 复制世界都不需要特判。
     */
    public record Suspend(boolean originalNoAi, int sources) {

        /** 从没挂过时的值；{@code sources == 0} 是唯一的存在判据。 */

        public static final Suspend NONE = new Suspend(false, 0);

        /** 两个字段手写 Codec（与 {@code TempEnchantRecord} 同风格）。 */
        public static final Codec<Suspend> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("originalNoAi").forGetter(Suspend::originalNoAi),
                Codec.INT.fieldOf("sources").forGetter(Suspend::sources)
        ).apply(instance, Suspend::new));
    }

    /** 来源位：震动豆（{@code VibrationBeanTrap}，80 刻）。 */
    public static final int SRC_VIBRATION_BEAN = 1;
    /** 来源位：Q弹（{@code QbounceEffect}，恢复判据是「效果还在不在」）。 */
    public static final int SRC_Q_BOUNCE = 2;

    /** 要每刻对账的实体（只存 UUID，**不含原值**）。区块卸载后会从这里掉出去，附件不受影响。 */
    private static final Set<UUID> OBSERVED = new HashSet<>();

    /** 震动豆的内存到期刻（gameTime）：只有这一个来源需要记到期，Q弹 靠效果自身。永不落盘。 */
    private static final Map<UUID, Long> VB_UNTIL = new HashMap<>();

    private MobAiSuspension() {}

    /**
     * 由 {@code Farmstorm} 构造器在 game 总线上注册（42 号 §2.3.5 的「本文确定（11）」）：
     * 对账挂 {@link EventPriority#HIGHEST}，必须先于 {@code QbounceMountController#onServerTick}
     * （NORMAL）——切到 mount 的那一 tick 要先把 AI 按原值放回去，再挂坐骑，否则坐骑会带着一个
     * {@code noAi} 的实体跑 0 移动。
     */
    public static void register(IEventBus gameBus) {
        gameBus.addListener(EventPriority.HIGHEST, MobAiSuspension::onServerTickPost);
        gameBus.addListener(MobAiSuspension::onEntityJoinLevel);
        gameBus.addListener(MobAiSuspension::onServerStopping);
    }

    /**
     * 进入挂起（幂等）：同一只实体被两处先后挂起时，原值只记第一次进入前的那一个（裁定 #8）。
     *
     * @param mob      目标（{@code Player} 不是 {@code Mob}，玩家恒不在表里）
     * @param sourceBit {@link #SRC_VIBRATION_BEAN} 或 {@link #SRC_Q_BOUNCE}
     * @param vbUntil  震动豆的到期刻；Q弹 传 0（它不记时点）
     * @return 是否真的处于挂起（mount 第一层跳过时返回 {@code false}）
     */
    public static boolean suspend(Mob mob, int sourceBit, long vbUntil) {
        if (sourceBit == SRC_Q_BOUNCE && QbounceMountController.isMountMode()) {
            return false;                             // 第一层：mount 态不删 AI，也不写附件
        }
        if (!(mob.level() instanceof ServerLevel level)) {
            return false;                             // 只在服务端写（客户端读不到附件注册表）
        }
        Suspend current = FarmstormAttachments.getAiSuspend(mob);
        boolean fresh = current.sources() == 0;
        if (fresh) {
            boolean original = mob.isNoAi();          // 先读原值
            FarmstormAttachments.setAiSuspend(mob, new Suspend(original, sourceBit));
            mob.setNoAi(true);                        // 再改状态
            if (sourceBit == SRC_VIBRATION_BEAN) {
                VB_UNTIL.put(mob.getUUID(), vbUntil);
            }
            OBSERVED.add(mob.getUUID());
            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                    "ai_suspend 挂起 实体=%s 原值=%s 来源=%d 到期刻=%d t=%d",
                    mob.getUUID(), original, sourceBit, vbUntil, level.getGameTime()));
        } else {
            FarmstormAttachments.setAiSuspend(mob,
                    new Suspend(current.originalNoAi(), current.sources() | sourceBit));
            if (sourceBit == SRC_VIBRATION_BEAN) {
                VB_UNTIL.merge(mob.getUUID(), vbUntil, Math::max);   // 「定身到最晚那次到期」，不叠加时长
            }
            OBSERVED.add(mob.getUUID());
        }
        return true;
    }

    /**
     * 退出挂起：按原值恢复 + 立刻删附件（裁定 #15 的硬要求）。
     * 还有别的来源没退出时（{@code left != 0}）只清这一位，不恢复、不删附件。
     */
    public static void release(Mob mob, int sourceBit) {
        UUID id = mob.getUUID();
        Suspend current = FarmstormAttachments.getAiSuspend(mob);
        if (current.sources() == 0) {
            OBSERVED.remove(id);                      // 没有记录：只清内存残留，绝不写 noAi
            VB_UNTIL.remove(id);
            return;
        }
        int left = current.sources() & ~sourceBit;
        if (left != 0) {
            FarmstormAttachments.setAiSuspend(mob, new Suspend(current.originalNoAi(), left));
            if (sourceBit == SRC_VIBRATION_BEAN) {
                VB_UNTIL.remove(id);
            }
            return;
        }
        mob.setNoAi(current.originalNoAi());          // 按原值恢复（原值本来就是 true 的仍是 true）
        clearData(mob);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "ai_suspend 恢复 实体=%s 来源=%d 写回原值=%s t=%d",
                id, sourceBit, current.originalNoAi(), mob.level().getGameTime()));
    }

    /** 全文件唯一的附件删除点：{@link #release}（恢复后）与 {@link #discard}（死亡后）都走这里。 */
    private static void clearData(Mob mob) {
        FarmstormAttachments.removeAiSuspend(mob);
        OBSERVED.remove(mob.getUUID());
        VB_UNTIL.remove(mob.getUUID());
    }

    /**
     * 载入时结算（路径⑤ / A-80）：读到 {@code sources} 后**先结算震动豆**（窗口不跨档 ⇒ 一律按已到期处理），
     * 再判 Q弹（效果还在身上就继续挂起，交给对账收敛）。宁可早恢复，不可永久锁死。
     */
    public static void adopt(Mob mob) {
        Suspend current = FarmstormAttachments.getAiSuspend(mob);
        if (current.sources() == 0) {
            return;                                   // 正常状态：存档里根本没有这条记录
        }
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "ai_suspend 载入结算 实体=%s 来源=%d 原值=%s t=%d",
                mob.getUUID(), current.sources(), current.originalNoAi(), mob.level().getGameTime()));
        if ((current.sources() & SRC_VIBRATION_BEAN) != 0) {
            release(mob, SRC_VIBRATION_BEAN);         // 判据 1：读不到「还剩几刻」，一律按已到期
        }
        Suspend afterBean = FarmstormAttachments.getAiSuspend(mob);
        if ((afterBean.sources() & SRC_Q_BOUNCE) != 0) {
            if (!mob.hasEffect(FarmstormMobEffects.Q_BOUNCE.get())) {
                release(mob, SRC_Q_BOUNCE);           // 判据 2：效果不在身上 ⇒ 恢复
            } else {
                OBSERVED.add(mob.getUUID());          // 效果还在 ⇒ 交给对账继续收敛
            }
        }
    }

    /** 每刻对账（{@link EventPriority#HIGHEST}）：先于 mount 挂载跑。 */
    private static void onServerTickPost(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (OBSERVED.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        for (UUID id : new ArrayList<>(OBSERVED)) {    // 遍历副本：release 会改索引
            Mob mob = resolve(server, id);
            if (mob == null) {
                OBSERVED.remove(id);                   // 观测不到（区块卸载）⇒ 只丢内存两件，附件留着
                VB_UNTIL.remove(id);
                continue;
            }
            if (mob.isRemoved() || !mob.isAlive()) {
                discard(mob);                          // 路径④：只删附件与索引，不写 noAi
                continue;
            }
            Suspend current = FarmstormAttachments.getAiSuspend(mob);
            if ((current.sources() & SRC_Q_BOUNCE) != 0 && !mob.hasEffect(FarmstormMobEffects.Q_BOUNCE.get())) {
                release(mob, SRC_Q_BOUNCE);            // 路径①②③：到期 / clear / 牛奶都收敛到「效果不在了」
            }
            current = FarmstormAttachments.getAiSuspend(mob);   // release 可能已删附件，重读
            if ((current.sources() & SRC_Q_BOUNCE) != 0 && QbounceMountController.isMountMode()) {
                release(mob, SRC_Q_BOUNCE);            // 第二层：切到 mount ⇒ 先按原值恢复，同刻再交给挂载
            }
            current = FarmstormAttachments.getAiSuspend(mob);
            if ((current.sources() & SRC_VIBRATION_BEAN) != 0) {
                Long until = VB_UNTIL.get(id);
                if (until == null || now >= until) {
                    release(mob, SRC_VIBRATION_BEAN);
                }
            }
        }
    }

    /** 载入结算的入口：只认从磁盘载入的实体（{@code /summon} 与首次加入走 {@code suspend} 的进入面）。 */
    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel)) {
            return;
        }
        if (event.getEntity() instanceof Mob mob) {
            adopt(mob);
        }
    }

    /**
     * 停服是路径⑤ 的特例（所有实体都「卸载」了）：做一次**尽力结算**，让存档干净、少一次载入时的修正。
     * 正确性不依赖它——漏掉的那一轮由下次载入的 {@link #adopt} 接住。
     */
    private static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        if (OBSERVED.isEmpty()) {
            return;
        }
        int settled = 0;
        for (UUID id : new ArrayList<>(OBSERVED)) {
            Mob mob = resolve(server, id);
            if (mob == null) {
                OBSERVED.remove(id);                   // 实体已经不在了：只丢内存，附件留给载入侧
                VB_UNTIL.remove(id);
                continue;
            }
            if (mob.isRemoved() || !mob.isAlive()) {
                discard(mob);
                settled++;
                continue;
            }
            Suspend current = FarmstormAttachments.getAiSuspend(mob);
            if ((current.sources() & SRC_VIBRATION_BEAN) != 0) {
                release(mob, SRC_VIBRATION_BEAN);      // 震动豆窗口不跨档 ⇒ 停服即按已到期
            }
            current = FarmstormAttachments.getAiSuspend(mob);
            if ((current.sources() & SRC_Q_BOUNCE) != 0 && !mob.hasEffect(FarmstormMobEffects.Q_BOUNCE.get())) {
                release(mob, SRC_Q_BOUNCE);
            }
            settled++;
        }
        OBSERVED.clear();
        VB_UNTIL.clear();
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "ai_suspend 停服结算 处理=%d", settled));
    }

    /** 路径④：实体已死 / 被移除。**不写 {@code noAi}**（它已经死了），只删附件与内存索引。 */
    private static void discard(Mob mob) {
        Suspend current = FarmstormAttachments.getAiSuspend(mob);
        if (current.sources() != 0) {
            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                    "ai_suspend 实体移除 实体=%s 来源=%d（不写 noAi）", mob.getUUID(), current.sources()));
        }
        clearData(mob);
    }

    /** 逐维度找实体：附件跟着实体走，索引只记 UUID，因此不存维度（找不到 ＝ 区块没加载）。 */
    private static Mob resolve(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity instanceof Mob mob) {
                return mob;
            }
        }
        return null;
    }
}
