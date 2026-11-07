package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 光环类行为（TECH §4.6）：周期性对半径内的己方玩家施加效果。
 * 默认 tick 遍历 {@code ServerLevel#players()}（个位数）而不是 AABB 扫实体（§8.1），
 * 并在施加前做「同玩家去重 + 每 tick 配额」的熔断（RT-06，落点在 BlockEntity）。
 */
public interface AuraProvider extends DeployedBehavior {

    @Override
    default int scanInterval() {
        return 100;
    }

    /** 欧氏半径（D-20）。 */
    double radius();

    /** 名义效果时长（tick）；逐条时长仍以 {@code <crop>.<effect>.durationTicks} 为准。 */
    int effectDurationTicks();

    void apply(DeployedCropBlockEntity be, ServerPlayer player);

    @Override
    default void tick(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = be.getBlockPos().getCenter();
        double radius = radius();
        double radiusSqr = radius * radius;
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!be.isAlly(player)) {
                continue;                                                   // HC-09 / HC-08：每次施加前实时解析
            }
            if (center.distanceToSqr(player.position()) > radiusSqr) {
                continue;
            }
            if (!DeployedCropBlockEntity.claimAura(be.kind(), player.getUUID(), now)) {
                continue;                                                   // RT-06：去重或本 tick 配额已用尽
            }
            apply(be, player);
        }
    }
}
