package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.explosion.BlastRule;
import com.dashpig.farmstorm.explosion.ExplosionQueue;
import com.dashpig.farmstorm.explosion.FarmstormExplosion;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.registry.FarmstormEntityTypes;
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
 * 瓜！（DESIGN §4.7 / TECH §4.7 + DESIGN §7.12）：落点上方撒出一批西瓜片子母弹，随即本体爆炸。
 * 顺序固定为「先生成、后爆炸」；子母弹不立即结算，按各自的地面碰撞重新走一次投掷型落点流程。
 */
public class MelonGrenadeProjectile extends ThrownCropEntity {
    /** 子母弹的水平初速（TECH §4.7），实现细节常量，不进 config。 */
    private static final double SUBMUNITION_SPEED = 0.3;

    public MelonGrenadeProjectile(EntityType<? extends MelonGrenadeProjectile> type, Level level) {
        super(type, level);
    }

    public MelonGrenadeProjectile(EntityType<? extends MelonGrenadeProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.MELON_GRENADE;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        Entity owner = this.getOwner();
        spawnSubmunitions(level, pos, owner);
        ExplosionQueue.submit(level, new FarmstormExplosion(level, owner,
                FarmstormDamageTypes.cropBlast(level, owner), pos.x, pos.y, pos.z,
                FarmstormConfig.melonGrenadeExplosionPower.get(),
                BlastRule.forCrop(profile(), BlastRule.grenade()),
                FarmstormConfig.melonGrenadeMaxDamage.get()));
    }

    /** DESIGN §7.12：归属者 = 瓜！的投掷者；生成位置与水平偏移全部走 config。 */
    private void spawnSubmunitions(ServerLevel level, Vec3 pos, @Nullable Entity owner) {
        int count = FarmstormConfig.melonGrenadeSpawnCount.get();
        double offsetY = FarmstormConfig.melonGrenadeSpawnOffsetY.get();
        double jitter = FarmstormConfig.melonGrenadeHorizontalJitter.get();
        for (int index = 0; index < count; index++) {
            double x = pos.x + Mth.nextDouble(this.random, -jitter, jitter);
            double z = pos.z + Mth.nextDouble(this.random, -jitter, jitter);
            MelonSliceProjectile slice = owner instanceof LivingEntity living
                    ? new MelonSliceProjectile(FarmstormEntityTypes.MELON_SLICE_BOMB.get(), living, level)
                    : new MelonSliceProjectile(FarmstormEntityTypes.MELON_SLICE_BOMB.get(), level);
            slice.moveTo(x, pos.y + offsetY, z, 0.0F, 0.0F);
            slice.setSubmunition(true);
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            slice.setDeltaMovement(Math.cos(angle) * SUBMUNITION_SPEED, 0.0, Math.sin(angle) * SUBMUNITION_SPEED);
            level.addFreshEntity(slice);
        }
    }
}
