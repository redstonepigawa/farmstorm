package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TrapBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity.FangWave;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.util.DistanceMetrics;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.EvokerFangs;

/**
 * 刺球（DESIGN §4.11 / §7.3 的 D-94）：一次性陷阱，非己方玩家进入欧氏 **≤ 2.5** 后吐出两圈尖牙。
 * 两圈的时间差**被刻意反过来**：内圈 2 刻先出、外圈 4 刻后出（照录原文，不做「修正」）。
 * 触发只负责压入两波队列；真正的出牙由 BlockEntity 的每 tick 队列驱动（§4.5 pendingWaves）。
 */
public final class FangTrapCactusTrap implements TrapBehavior {

    @Override
    public DeployedKind kind() {
        return DeployedKind.FANG_TRAP_CACTUS;
    }

    /**
     * 欧氏 ≤ {@code fang_trap_cactus.triggerRadius}（C-5 / D-20 的唯一例外）。
     * <p>D-114 的两遍：第一遍仍是玩家（三条豁免一字未改）；第二遍补敌对生物，复用同一个半径键与同一个欧氏
     * （不写第二个数），生物恒按非己方。生物的缓慢 V 由伤害事件侧跳过，本类不筛。
     */
    @Override
    public Optional<LivingEntity> findTrigger(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        BlockPos pos = be.getBlockPos();
        double radius = FarmstormConfig.fangTrapTriggerRadius.get();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative() || be.isAlly(player)) {
                continue;                    // HC-09；DESIGN §7.6 L1053：创造玩家不触发陷阱
            }
            BlockPos target = player.blockPosition();
            if (DistanceMetrics.euclidean(target.getX() - pos.getX(), target.getY() - pos.getY(),
                    target.getZ() - pos.getZ()) <= radius) {
                return Optional.of(player);
            }
        }
        for (Monster monster : level.getEntitiesOfClass(Monster.class, TrapBehavior.searchBox(pos, radius))) {
            BlockPos target = monster.blockPosition();
            if (DistanceMetrics.euclidean(target.getX() - pos.getX(), target.getY() - pos.getY(),
                    target.getZ() - pos.getZ()) <= radius) {
                return Optional.of(monster);
            }
        }
        return Optional.empty();
    }

    @Override
    public void trigger(DeployedCropBlockEntity be, LivingEntity target) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!be.pendingWaves().isEmpty()) {
            return;                          // 一次性：波已排出去就不再重排（重排会把外圈那波顶到更远，方块永不自毁）
        }
        long now = level.getGameTime();
        be.setPendingWaves(List.of(
                new FangWave(now + FarmstormConfig.fangTrapInnerDelayTicks.get(),
                        FarmstormConfig.fangTrapInnerFangCount.get(), FarmstormConfig.fangTrapInnerRadius.get()),
                new FangWave(now + FarmstormConfig.fangTrapOuterDelayTicks.get(),
                        FarmstormConfig.fangTrapOuterFangCount.get(), FarmstormConfig.fangTrapOuterRadius.get())));
        if (target instanceof ServerPlayer serverPlayer) {
            FarmstormAdvancements.award(serverPlayer, "fang_dance");
        }
    }

    /**
     * 出一圈尖牙：以部署点为中心按 {@code count} 等分圆周（G-3 / D-30），实体用原版 {@code EvokerFangs}，
     * 归属者记为部署者。由 {@link DeployedCropBlockEntity} 的延迟队列在到点时调用。
     */
    public static void spawnWave(DeployedCropBlockEntity be, ServerLevel level, FangWave wave) {
        if (wave.count() <= 0) {
            return;
        }
        BlockPos pos = be.getBlockPos();
        ServerPlayer owner = be.owner(level);
        double angleStep = Math.PI * 2.0 / wave.count();
        for (int index = 0; index < wave.count(); index++) {
            double angle = angleStep * index;
            double x = pos.getX() + 0.5 + Math.cos(angle) * wave.radius();
            double z = pos.getZ() + 0.5 + Math.sin(angle) * wave.radius();
            EvokerFangs fangs = EntityType.EVOKER_FANGS.create(level);
            if (fangs == null) {
                return;
            }
            if (owner != null) {
                fangs.setOwner(owner);                                  // 击杀归属部署者
            }
            fangs.moveTo(x, pos.getY(), z, (float) Math.toDegrees(-angle), 0.0F);
            FangTrapFangs.mark(fangs, pos, level.getGameTime());          // D-95：登记归属，伤害事件侧才认这颗尖牙
            level.addFreshEntity(fangs);
        }
    }
}
