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
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 毒育碧服务器（DESIGN §4.4 / TECH §4.7）：先落点爆炸，再在落点生成一片剧毒药水云，等价滞留型剧毒药水。
 * 云不筛敌我，对所有进入者按原版 {@link AreaEffectCloud} 规则施加效果。
 */
public class ToxicUbiServerProjectile extends ThrownCropEntity {

    public ToxicUbiServerProjectile(EntityType<? extends ToxicUbiServerProjectile> type, Level level) {
        super(type, level);
    }

    public ToxicUbiServerProjectile(EntityType<? extends ToxicUbiServerProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.TOXIC_UBI_SERVER;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        Entity owner = this.getOwner();
        ExplosionQueue.submit(level, new FarmstormExplosion(level, owner,
                FarmstormDamageTypes.cropBlast(level, owner), pos.x, pos.y, pos.z,
                FarmstormConfig.toxicUbiServerExplosionPower.get(),
                BlastRule.forCrop(profile(), BlastRule.VANILLA),
                FarmstormConfig.toxicUbiServerMaxDamage.get()));
        level.addFreshEntity(cloud(level, pos));
    }

    /** DESIGN §4.4 ③：粒子与颜色由药水内容派生，不单独配置。 */
    private static AreaEffectCloud cloud(ServerLevel level, Vec3 pos) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, pos.x, pos.y, pos.z);
        cloud.setRadius(FarmstormConfig.toxicUbiServerCloudRadius.get().floatValue());
        cloud.setDuration(FarmstormConfig.toxicUbiServerCloudDurationTicks.get());
        cloud.setPotionContents(new PotionContents(Potions.POISON));
        return cloud;
    }
}
