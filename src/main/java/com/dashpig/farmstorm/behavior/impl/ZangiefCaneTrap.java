package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TrapBehavior;
import com.dashpig.farmstorm.behavior.ZangiefPiledriverController;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 桑吉尔蔗（TECH §3.10）：一次性陷阱，非己方玩家进入球形半径 1.5 格即「被打桩机」。
 * <p>本类只负责触发判定与触发瞬间的动作；相位推进归 {@code ZangiefPiledriverController}
 * （相位要按玩家 tick 走，而陷阱方块一触发就没了，D-72）。
 */
public final class ZangiefCaneTrap implements TrapBehavior {
    /** 扫描日志的观察半径：只有附近有玩家时才打，野外空转不刷屏（F-86 验收 3）。 */
    private static final double SCAN_LOG_RADIUS = 16.0;

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
     * 球形欧氏判定（D-73）：方块中心到玩家位置的距离 <= {@code zangief_cane.triggerRadius}（1.5 格）。
     * 不用切比雪夫——那会把「对角 1.4 / 1.4 格」（欧氏 1.98）误判成命中；也不判 {@code blockPosition()}，
     * 因为 1.5 格的口径是「从方块中心算起」，玩家站在相邻方块中心（1.0 或 1.41）也算命中。
     */
    @Override
    public Optional<Player> findTrigger(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        return Optional.ofNullable(nearestCandidate(be, level, FarmstormConfig.zangiefCaneTriggerRadius.get()));
    }

    /**
     * 触发瞬间的三步（TECH §3.10）：施加 {@code amplifier 0} 的 {@code zangief_piledriver} → 播音 → 自毁。
     * <p><b>顺序不能反</b>：{@link DeployedCropBlockEntity#consume()} 之后方块与 BlockEntity 已从世界里消失，
     * 再读坐标 / 放音就是在读一个失效对象；A-08 的「触发消失不掉落」也走这条自毁路径，而不是删战利品表。
     */
    @Override
    public void trigger(DeployedCropBlockEntity be, Player target) {
        if (!(be.getLevel() instanceof ServerLevel level) || !(target instanceof ServerPlayer player)) {
            return;                                                            // 服务端权威（HC-03）
        }
        BlockPos pos = be.getBlockPos();                                       // consume() 之前先取出坐标
        double distance = Math.sqrt(Vec3.atCenterOf(pos).distanceToSqr(player.position()));
        ZangiefPiledriverController.begin(player);                             // 相位 0 的施加与记账归状态机（§3.11）
        // D-63 / DESIGN §9.1–9.2：zangief_hit 授予**陷阱的部署者**（父进度是 first_deploy，属于部署者那棵树），
        // 不是被命中的玩家——「你布的东西打中了人」才是成就。归属者离线 / 无归属（/setblock）时跳过。
        ServerPlayer deployer = be.owner(level);
        if (deployer != null) {
            FarmstormAdvancements.award(deployer, "zangief_hit");
        }
        // 作者改版：音效**以被触发的玩家为声源**（原先是 pos 上的「无来源实体」按位置播）。
        // 这样声音跟着人走，抬升/坠落时不会被留在原方块位置。
        level.playSound(null, player, FarmstormSounds.ZANGIEF_PILEDRIVER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "桑吉尔蔗 触发 t=%d 玩家 %s 中心距 %.2f",
                level.getGameTime(), player.getGameProfile().getName(), distance));
        be.consume();
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
     * 最近的非豁免玩家，超出 {@code maxDistance} 返回 null（D-71 的三条豁免在这里统一处理）：
     * 旁观者与创造玩家不触发陷阱，己方玩家同理（{@code be.isAlly} 每次触发时实时解析，C-9）。
     * 只遍历 {@code level.players()}——陷阱只对玩家生效，生物不参与判定。
     */
    @Nullable
    private static Player nearestCandidate(DeployedCropBlockEntity be, ServerLevel level, double maxDistance) {
        Vec3 center = Vec3.atCenterOf(be.getBlockPos());
        double limit = maxDistance * maxDistance;
        Player best = null;
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
        return best;
    }
}
