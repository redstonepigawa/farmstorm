package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TurretBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.TurretBullet;
import com.dashpig.farmstorm.registry.FarmstormEntityTypes;
import com.dashpig.farmstorm.util.DistanceMetrics;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * man瓜（DESIGN §4.8 / FIX-6 §4）：切比雪夫半径内索敌，每 {@code fireIntervalTicks} 刻开火一发导弹。
 * 目标优先级：非己方玩家或敌对生物里最近的一个 →（{@link #INTERCEPT_PROJECTILES}）非己方投掷物（DEC C-3 / D-07）。
 * 命中结算（{@code farmstorm:void_bolt} 伤害 + 击退 + 不给漂浮）在 {@link TurretBullet} 里，本类只负责瞄准与生成；
 * 导弹的 owner 取部署者（{@link com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity#owner}），击杀归属因此落在放置者身上。
 */
public final class PumpkinTurret implements TurretBehavior {
    /** C-3 / D-07：全模组唯一保留「拦截飞行中投掷物」的炮台。 */
    private static final boolean INTERCEPT_PROJECTILES = true;

    /** 导弹的半边长（EntityType 尺寸 0.3，TECH §3.4）；用于把出膛点顶到方块碰撞箱之外。 */
    private static final double BULLET_HALF_SIZE = 0.15;
    private static final double MUZZLE_MARGIN = 0.1;

    @Override
    public DeployedKind kind() {
        return DeployedKind.PUMPKIN_TURRET;
    }

    @Override
    public Optional<Entity> findTarget(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        BlockPos pos = be.getBlockPos();
        int radius = FarmstormConfig.pumpkinTurretTargetRadius.get();
        Entity target = nearestHostile(be, level, pos, radius);
        if (target == null && INTERCEPT_PROJECTILES) {
            target = nearestProjectile(be, level, pos, radius);
        }
        return Optional.ofNullable(target);
    }

    @Override
    public void fire(DeployedCropBlockEntity be, Entity target) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Vec3 from = be.getBlockPos().getCenter();
        Vec3 aim = target.getBoundingBox().getCenter().subtract(from);
        if (aim.lengthSqr() < 1.0E-4) {
            aim = new Vec3(0.0, 0.0, 1.0);
        }
        Vec3 direction = aim.normalize();
        // 出膛点必须顶到方块碰撞箱之外：炮台方块是整格碰撞箱，导弹若生成在格心会立刻撞上自己所在的方块。
        double largest = Math.max(Math.abs(direction.x), Math.max(Math.abs(direction.y), Math.abs(direction.z)));
        double muzzle = Math.min(0.5 / largest + BULLET_HALF_SIZE + MUZZLE_MARGIN, aim.length());
        Vec3 spawn = from.add(direction.scale(muzzle));
        ServerPlayer owner = be.owner(level);
        TurretBullet bullet = owner == null
                ? new TurretBullet(FarmstormEntityTypes.TURRET_BULLET.get(), level)
                : new TurretBullet(FarmstormEntityTypes.TURRET_BULLET.get(), owner, level);
        bullet.moveTo(spawn.x, spawn.y, spawn.z, 0.0F, 0.0F);
        bullet.setTarget(target);
        bullet.setDeltaMovement(direction.scale(TurretBullet.SPEED));
        level.addFreshEntity(bullet);
    }

    @Override
    public int cooldownTicks() {
        return FarmstormConfig.pumpkinTurretFireIntervalTicks.get();
    }

    /** 己方不成为目标（HC-09）：先判归属再算距离，省掉绝大多数距离计算（§8.1 早退）。 */
    private static ServerPlayer nearestPlayer(DeployedCropBlockEntity be, ServerLevel level, BlockPos pos, int radius) {
        ServerPlayer nearest = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative() || be.isAlly(player)) {
                continue;                    // DESIGN §7.6 L1053：创造玩家不被炮台锁定
            }
            double distance = blockDistance(pos, player.blockPosition());
            if (distance > radius || distance >= best) {
                continue;
            }
            nearest = player;
            best = distance;
        }
        return nearest;
    }

    /**
     * 玩家与敌对生物的最近者（FIX-6 §4.2）：两者同场竞技时按切比雪夫距离取近，平手仍判给玩家（保持既有优先级）。
     * 敌对生物的口径是 {@link Monster}（僵尸 / 骷髅 / 苦力怕…），动物与村民不在其中。
     */
    private static Entity nearestHostile(DeployedCropBlockEntity be, ServerLevel level, BlockPos pos, int radius) {
        Entity nearest = nearestPlayer(be, level, pos, radius);
        double best = nearest == null ? Double.MAX_VALUE : blockDistance(pos, nearest.blockPosition());
        for (Monster monster : level.getEntitiesOfClass(Monster.class, searchBox(pos, radius))) {
            double distance = blockDistance(pos, monster.blockPosition());
            if (distance > radius || distance >= best) {
                continue;
            }
            nearest = monster;
            best = distance;
        }
        return nearest;
    }

    /** 索敌用的查询盒：切比雪夫半径 r 对应边长 2r+1 的立方体（块坐标口径，D-20）。 */
    private static AABB searchBox(BlockPos pos, int radius) {
        double span = radius * 2.0 + 1.0;
        return AABB.ofSize(pos.getCenter(), span, span, span);
    }

    private static Entity nearestProjectile(DeployedCropBlockEntity be, ServerLevel level, BlockPos pos, int radius) {
        AABB box = searchBox(pos, radius);
        Entity nearest = null;
        double best = Double.MAX_VALUE;
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, box)) {
            if (!isInterceptable(be, projectile)) {
                continue;
            }
            double distance = blockDistance(pos, projectile.blockPosition());
            if (distance > radius || distance >= best) {
                continue;
            }
            nearest = projectile;
            best = distance;
        }
        return nearest;
    }

    /** 非己方投掷物才拦；己方导弹不拦（本模组炮台的弹不互相消耗）。 */
    private static boolean isInterceptable(DeployedCropBlockEntity be, Projectile projectile) {
        if (projectile instanceof TurretBullet) {
            return false;
        }
        return !(projectile.getOwner() instanceof Player owner) || !be.isAlly(owner);
    }

    /** 索敌距离用切比雪夫（D-20），原点与判定对象都取方块坐标。 */
    private static double blockDistance(BlockPos origin, BlockPos target) {
        return DistanceMetrics.chebyshev(target.getX() - origin.getX(), target.getY() - origin.getY(),
                target.getZ() - origin.getZ());
    }
}
