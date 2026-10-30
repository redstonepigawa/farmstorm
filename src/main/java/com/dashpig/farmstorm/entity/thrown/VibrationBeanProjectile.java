package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.util.CropProfile;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 震动豆的投掷物（TECH §3.5）：落点放置一个不依附方块的震动豆陷阱（{@code DeployedKind.VIBRATION_BEAN}）。
 *
 * <p>F-103 只立编译期骨架：{@code onImpact} 为空，因此落地不写方块；落点放置由 F-122 填入。
 */
public class VibrationBeanProjectile extends ThrownCropEntity {

    public VibrationBeanProjectile(EntityType<? extends VibrationBeanProjectile> type, Level level) {
        super(type, level);
    }

    public VibrationBeanProjectile(EntityType<? extends VibrationBeanProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.VIBRATION_BEAN;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        // 骨架：F-122 填落点放置（震动豆方块）
    }
}
