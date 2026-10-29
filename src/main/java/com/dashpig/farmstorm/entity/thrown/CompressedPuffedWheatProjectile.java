package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.util.CropProfile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 压缩膨胀麦（D-77 / TECH §2.2 #9）：F-66 只落骨架，落点写入沿用膨胀麦那一套在 F-68。
 * 命中结算仍走基类 {@code settle} 的「每次生成恰好一次」；本类不重写 {@code onImpact}。
 */
public class CompressedPuffedWheatProjectile extends ThrownCropEntity {
    public CompressedPuffedWheatProjectile(EntityType<? extends CompressedPuffedWheatProjectile> type, Level level) {
        super(type, level);
    }

    public CompressedPuffedWheatProjectile(EntityType<? extends CompressedPuffedWheatProjectile> type,
                                           LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.COMPRESSED_PUFFED_WHEAT;
    }
}
