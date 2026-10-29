package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.util.CropProfile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 莫洛托夫（D-58 / TECH §2.2 #8）：F-66 只落骨架，落点纵火与岩浆块在 F-69 / F-70。
 * 命中结算仍走基类 {@code settle} 的「每次生成恰好一次」；本类不重写 {@code onImpact}，
 * 因此落地时除实体消失外什么都不做。
 */
public class MolotovProjectile extends ThrownCropEntity {
    public MolotovProjectile(EntityType<? extends MolotovProjectile> type, Level level) {
        super(type, level);
    }

    public MolotovProjectile(EntityType<? extends MolotovProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.MOLOTOV;
    }
}
