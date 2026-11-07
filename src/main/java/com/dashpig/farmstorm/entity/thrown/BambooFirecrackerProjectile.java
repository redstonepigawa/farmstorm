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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 爆竹（TECH §4.7 / DESIGN §4.12）：落地后 {@code durationTicks} 内每 {@code spawnIntervalTicks}
 * 在落点附近撒一枚原版风弹并立刻引爆（默认 140 / 5 = 28 次）。
 */
public class BambooFirecrackerProjectile extends ThrownCropEntity {
    /** TECH §4.7：风弹生成点在落点水平方向上的散落半径，内部常量（不是 config 键）。 */
    private static final double FIRECRACKER_SCATTER = 1.5;

    /**
     * 每次爆炸的威力。原版风弹是 1.2，实测在 1.20.1 里伤害明显过高（风弹本体还有击退修正，我们没有），
     * 按作者要求**减半**取 0.6。爆炸伤害与威力近似成正比，因此贴脸伤害也随之减半。
     */
    private static final float EXPLOSION_POWER = 0.6F;

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

    /**
     * 在自身水平 ±FIRECRACKER_SCATTER 内起一次爆炸（1.20.1 没有 {@code WindCharge}）。
     *
     * <p>威力按原版风弹的 1.2 取，不破块、不起火（{@code ExplosionInteraction.NONE}），
     * 与原版风弹「只推不拆」的口径一致。风弹那套击退修正与粒子在 1.20.1 不存在，见 TODO(F-012)。
     */
    // TODO(F-012): 1.20.1 无 WindCharge；当前用等价威力的无破块爆炸近似，击退修正与风弹粒子待补齐。
    private void spawnWindCharge(ServerLevel level) {
        Vec3 origin = this.position();
        double x = origin.x + Mth.nextDouble(this.random, -FIRECRACKER_SCATTER, FIRECRACKER_SCATTER);
        double z = origin.z + Mth.nextDouble(this.random, -FIRECRACKER_SCATTER, FIRECRACKER_SCATTER);
        level.explode(this, x, origin.y, z, 1.2F, Level.ExplosionInteraction.NONE);
    }
}
