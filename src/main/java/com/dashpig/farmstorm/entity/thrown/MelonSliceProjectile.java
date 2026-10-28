package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.explosion.BlastRule;
import com.dashpig.farmstorm.explosion.ExplosionQueue;
import com.dashpig.farmstorm.explosion.FarmstormExplosion;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.util.CropProfile;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 瓜片子（DESIGN §4.6 / TECH §4.7）：落点一次小威力爆炸，伤害封顶。
 * 能否破坏方块由 {@link CropProfile#canBreakBlocks()} 决定，本类不写死；也被「瓜！」当子母弹复用，子母弹因此跟随同一标签。
 */
public class MelonSliceProjectile extends ThrownCropEntity {

    public MelonSliceProjectile(EntityType<? extends MelonSliceProjectile> type, Level level) {
        super(type, level);
    }

    public MelonSliceProjectile(EntityType<? extends MelonSliceProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.MELON_SLICE_BOMB;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        Entity owner = this.getOwner();
        ExplosionQueue.submit(level, new FarmstormExplosion(level, owner,
                FarmstormDamageTypes.cropBlast(level, owner), pos.x, pos.y, pos.z,
                FarmstormConfig.melonSliceBombExplosionPower.get(), BlastRule.forCrop(profile(), BlastRule.sliceBomb()),
                FarmstormConfig.melonSliceBombMaxDamage.get()));
    }
}
