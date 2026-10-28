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
 * 育碧服务器（DESIGN §4.3 / TECH §4.7）：落点一次破障爆炸，威力与伤害上限是两条独立约束。
 * 破障类不设抗性阈值，但永远跳过硬抗性方块（{@link BlastRule#UNBREAKABLE_SKIP}）；
 * 「是不是破障类」由 {@link CropProfile#canBreakBlocks()} 判定，本类不自己接线。
 */
public class UbiServerProjectile extends ThrownCropEntity {

    public UbiServerProjectile(EntityType<? extends UbiServerProjectile> type, Level level) {
        super(type, level);
    }

    public UbiServerProjectile(EntityType<? extends UbiServerProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.UBI_SERVER;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        Entity owner = this.getOwner();
        ExplosionQueue.submit(level, new FarmstormExplosion(level, owner,
                FarmstormDamageTypes.cropBlast(level, owner), pos.x, pos.y, pos.z,
                FarmstormConfig.ubiServerExplosionPower.get(),
                BlastRule.forCrop(profile(), BlastRule.UNBREAKABLE_SKIP),
                FarmstormConfig.ubiServerMaxDamage.get()));
    }
}
