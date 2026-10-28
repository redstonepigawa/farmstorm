package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.util.CropProfile;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 爆竹（TECH §4.7 / DESIGN §4.12）：落地后 {@code durationTicks} 内每 {@code spawnIntervalTicks}
 * 在落点附近撒一枚原版风弹并立刻引爆（默认 140 / 5 = 28 次）。
 */
public class BambooFirecrackerProjectile extends ThrownCropEntity {
    /** TECH §4.7：风弹生成点在落点水平方向上的散落半径，内部常量（不是 config 键）。 */
    private static final double FIRECRACKER_SCATTER = 1.5;

    /** 风弹雨剩余刻数；-1 表示尚未命中，命中瞬间由 onImpact 置为 {@code durationTicks}。 */
    private int fuseTicks = -1;

    public BambooFirecrackerProjectile(EntityType<? extends BambooFirecrackerProjectile> type, Level level) {
        super(type, level);
    }

    public BambooFirecrackerProjectile(EntityType<? extends BambooFirecrackerProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.BAMBOO_FIRECRACKER;
    }

    /** TECH §4.7：命中后不消失，原地进入风弹雨倒计时。 */
    @Override
    protected boolean keepAliveAfterImpact() {
        return true;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        this.fuseTicks = FarmstormConfig.firecrackerDurationTicks.get();
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * 必要例外（见回报说明）：基类只在 onImpact 里给一次机会，续命后的「每 spawnIntervalTicks 撒风弹 /
     * 到期 discard」没有任何每 tick 钩子可挂，只能在子类 tick 内驱动。
     */
    @Override
    public void tick() {
        if (this.fuseTicks >= 0) {
            // ThrowableProjectile#tick 每 tick 都会重新累加重力，先归零才能让爆竹钉在落点不动。
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
        if (this.fuseTicks < 0 || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.fuseTicks % FarmstormConfig.firecrackerSpawnIntervalTicks.get() == 0) {
            this.spawnWindCharge(level);
        }
        if (--this.fuseTicks <= 0) {
            this.discard();
        }
    }

    /** 在自身水平 ±FIRECRACKER_SCATTER 内生成风弹并立即引爆：破坏与伤害范围完全由原版风弹决定。 */
    private void spawnWindCharge(ServerLevel level) {
        Vec3 origin = this.position();
        DetonatingWindCharge windCharge = new DetonatingWindCharge(level,
                origin.x + Mth.nextDouble(this.random, -FIRECRACKER_SCATTER, FIRECRACKER_SCATTER),
                origin.y,
                origin.z + Mth.nextDouble(this.random, -FIRECRACKER_SCATTER, FIRECRACKER_SCATTER));
        level.addFreshEntity(windCharge);
        windCharge.detonateImmediately();
        windCharge.discard();
    }

    /**
     * 原版 {@code WindCharge#explode} 是跨包不可见的 protected 方法，子类里才允许调用自己身上的它。
     * 用这层最小转发类让「生成后立刻引爆」走的仍是原版逻辑（威力 1.2、风弹伤害计算器、粒子与音效都不复制数值）；
     * 这类实体不入注册表，只作为一次性的引爆载具，生成后同一刻即 discard。
     */
    private static final class DetonatingWindCharge extends WindCharge {
        private DetonatingWindCharge(Level level, double x, double y, double z) {
            super(level, x, y, z, Vec3.ZERO);
        }

        private void detonateImmediately() {
            this.explode(this.position());
        }
    }
}
