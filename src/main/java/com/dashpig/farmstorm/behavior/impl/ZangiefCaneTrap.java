package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TrapBehavior;
import com.dashpig.farmstorm.behavior.ZangiefPiledriverController;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import com.dashpig.farmstorm.util.Hostility;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

/**
 * 桑吉尔蔗（TECH §3.10）：一次性陷阱，非己方玩家或敌对生物进入球形半径 1.5 格即「被打桩机」。
 * <p>本类只负责触发判定与触发瞬间的动作；相位推进归 {@code ZangiefPiledriverController}
 * （相位要按玩家 tick 走，而陷阱方块一触发就没了，D-72）。
 * <p>对玩家跑既有相位链（相位 1 **伺服抬升固定高度** → 相位 2 伺服下坠 → 落地固定 7.5 点）；
 * 对生物只结算通用段（固定 7.5 点 + 击退），**不进相位链**——相位链服务的是玩家的相机与输入，生物没有接收端（D-114 补充）。
 */
public final class ZangiefCaneTrap implements TrapBehavior {
    /** 扫描日志的观察半径：只有附近有玩家时才打，野外空转不刷屏（F-86 验收 3）。 */
    private static final double SCAN_LOG_RADIUS = 16.0;

    /** 生物分支的击退强度：与 {@code combat/WildHunterCombat} 的 KNOCKBACK_STRENGTH 同值 0.4，不新造数值（D-114）。 */
    private static final double KNOCKBACK_STRENGTH = 0.4;

    @Override
    public DeployedKind kind() {
        return DeployedKind.ZANGIEF_CANE;
    }

    /** 扫描间隔的可观测读数（F-86 验收 3）：相邻两行 {@code t} 差 = 扫描间隔 10 刻。 */
    @Override
    public void tick(DeployedCropBlockEntity be) {
        logScan(be);
        TrapBehavior.super.tick(be);
    }

    /**
     * 球形欧氏判定（D-73）：方块中心到目标位置的距离 <= {@code zangief_cane.triggerRadius}（1.5 格）。
     * 不用切比雪夫——那会把「对角 1.4 / 1.4 格」（欧氏 1.98）误判成命中；也不判 {@code blockPosition()}，
     * 因为 1.5 格的口径是「从方块中心算起」，玩家站在相邻方块中心（1.0 或 1.41）也算命中。
     * <p>D-114：候选集合是「非己方玩家 + 敌对生物」，两边共用同一个半径键与同一种球形距离。
     */
    @Override
    public Optional<LivingEntity> findTrigger(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        return Optional.ofNullable(nearestCandidate(be, level, FarmstormConfig.zangiefCaneTriggerRadius.get()));
    }

    /**
     * 触发瞬间按触发者分两支（D-114 / TECH §5.3 第 6 步），两支都排在 {@code consume()} 之前。
     * <ul>
     *   <li>{@link ServerPlayer}：施加 {@code amplifier 0} 的 {@code zangief_piledriver}（相位号通道）→ 授进度 → 播音 → 自毁，
     *       即既有相位链；</li>
     *   <li>其余（敌对生物）：只结算通用段，见 {@link #monsterBranch}。</li>
     * </ul>
     * <p><b>顺序不能反</b>：{@link DeployedCropBlockEntity#consume()} 之后方块与 BlockEntity 已从世界里消失，
     * 再读坐标 / 放音就是在读一个失效对象；A-08 的「触发消失不掉落」也走这条自毁路径，而不是删战利品表。
     */
    @Override
    public void trigger(DeployedCropBlockEntity be, LivingEntity target) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;                                                            // 服务端权威（HC-03）
        }
        BlockPos pos = be.getBlockPos();                                       // consume() 之前先取出坐标
        Vec3 center = Vec3.atCenterOf(pos);
        double distance = Math.sqrt(center.distanceToSqr(target.position()));
        // D-96：同一个归属者随相位状态机带到落地结算（固定 7.5 点也记给部署者），所以要在 begin 之前取出来。
        ServerPlayer deployer = be.owner(level);
        if (target instanceof ServerPlayer player) {
            ZangiefPiledriverController.begin(player, deployer);               // 相位 0 的施加与记账归状态机（§3.11）
            // D-63 / DESIGN §9.1–9.2：zangief_hit 授予**陷阱的部署者**（父进度是 first_deploy，属于部署者那棵树），
            // 不是被命中的玩家——「你布的东西打中了人」才是成就。归属者离线 / 无归属（/setblock）时跳过。
            if (deployer != null) {
                FarmstormAdvancements.award(deployer, "zangief_hit");
            }
            // 作者改版：音效**以被触发的玩家为声源**（原先是 pos 上的「无来源实体」按位置播）。
            // 这样声音跟着人走，抬升/坠落时不会被留在原方块位置。
            level.playSound(null, player, FarmstormSounds.ZANGIEF_PILEDRIVER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 触发 t=%d 玩家 %s 中心距 %.2f",
                    level.getGameTime(), player.getGameProfile().getName(), distance));
        } else {
            monsterBranch(level, pos, center, target, deployer, distance);
        }
        be.consume();
    }

    /**
     * 生物分支（D-114 补充 / TECH §5.3 第 6 步）：只结算通用段——固定 {@code zangief_cane.slamDamage}
     * （默认 7.5 点）的 {@code farmstorm:zangief_slam} 伤害 + 一次击退 + 一次本陷阱音效，在陷阱位置结算，
     * **不进相位链**（相位链以 {@link ServerPlayer} 为目标，生物没有接收端）。
     * <p>伤害源查不到（数据侧的 {@code zangief_slam.json} 未注册）时跳过伤害并留 warn，击退照旧——
     * 与 {@code ZangiefPiledriverController#slam} 的 null 口径一致；{@code slamDamage} 配 0 同样只跳过伤害。
     */
    private static void monsterBranch(ServerLevel level, BlockPos pos, Vec3 center, LivingEntity target,
                                      @Nullable ServerPlayer deployer, double distance) {
        double amount = FarmstormConfig.zangiefCaneSlamDamage.get();
        // F-260 乘点②（42 号 §2.5.3）：在 source == null 与 amount > 0 两个判定**之前**乘，slamDamage 配 0 时乘完仍是 0；
        // 下面 INFO 打的就是这个乘后值（格式串不改）。玩家分支不进倍率面（相位链的落地结算在 ZangiefPiledriverController）。
        if (Hostility.isHostile(target)) {
            amount *= FarmstormConfig.trapDamageMultiplier.get();
        }
        DamageSource source = FarmstormDamageTypes.zangiefSlam(level, deployer);
        if (source == null) {
            Farmstorm.LOGGER.warn(String.format(Locale.ROOT,
                    "桑吉尔蔗 生物结算跳过伤害（damage_type farmstorm:zangief_slam 未注册）生物 %s",
                    target.getName().getString()));
        } else if (amount > 0.0) {
            target.hurt(source, (float) amount);
        }
        // 击退方向：原版 knockback 把实体沿传入向量的**反**方向推开，所以传「从生物指向陷阱」再取反，
        // 结果是把生物推离陷阱中心（与 combat/WildHunterCombat 的 knockback(0.4, -push.x, -push.z) 同一写法）。
        double dx = target.getX() - center.x;
        double dz = target.getZ() - center.z;
        target.knockback(KNOCKBACK_STRENGTH, -dx, -dz);
        level.playSound(null, pos, FarmstormSounds.ZANGIEF_PILEDRIVER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "桑吉尔蔗 生物结算 t=%d 生物 %s 中心距 %.2f 伤害 %.1f 击退 %.1f（不进相位链）",
                level.getGameTime(), target.getName().getString(), distance, amount, KNOCKBACK_STRENGTH));
    }

    /**
     * 扫描日志：{@code t} 是 gameTime，用它读扫描间隔；只在 16 格内有玩家时打。
     * 判定用的豁免（旁观 / 创造 / 己方）不参与这个门槛——这一行只回答「这颗陷阱这几刻扫过没有」。
     */
    private static void logScan(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(be.getBlockPos());
        for (ServerPlayer player : level.players()) {
            double distance = Math.sqrt(center.distanceToSqr(player.position()));
            if (distance <= SCAN_LOG_RADIUS) {
                Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 扫描 t=%d 玩家 %s 距离 %.2f",
                        level.getGameTime(), player.getGameProfile().getName(), distance));
                return;
            }
        }
    }

    /**
     * 最近的非豁免候选，超出 {@code maxDistance} 返回 null（D-71 的三条豁免在这里统一处理）：
     * 旁观者与创造玩家不触发陷阱，己方玩家同理（{@code be.isAlly} 每次触发时实时解析，C-9）。
     * <p>D-114 的第二遍：敌对生物恒按非己方（生物没有队伍归属，不调 {@code be.isAlly}），
     * 复用同一个 {@code maxDistance} 与同一种「方块中心到实体位置」的球形距离（不写第二个数）。
     * 玩家那一遍在前、比较用严格小于，因此同距时玩家优先。
     */
    @Nullable
    private static LivingEntity nearestCandidate(DeployedCropBlockEntity be, ServerLevel level, double maxDistance) {
        BlockPos pos = be.getBlockPos();
        Vec3 center = Vec3.atCenterOf(pos);
        double limit = maxDistance * maxDistance;
        LivingEntity best = null;
        double bestSqr = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative() || be.isAlly(player)) {
                continue;
            }
            double sqr = center.distanceToSqr(player.position());
            if (sqr <= limit && sqr < bestSqr) {
                best = player;
                bestSqr = sqr;
            }
        }
        for (Monster monster : level.getEntitiesOfClass(Monster.class, TrapBehavior.searchBox(pos, maxDistance))) {
            double sqr = center.distanceToSqr(monster.position());
            if (sqr <= limit && sqr < bestSqr) {
                best = monster;
                bestSqr = sqr;
            }
        }
        return best;
    }
}
