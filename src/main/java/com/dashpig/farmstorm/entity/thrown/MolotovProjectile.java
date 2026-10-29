package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.explosion.BlastRule;
import com.dashpig.farmstorm.explosion.FarmstormExplosion;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.util.CropProfile;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 莫洛托夫（D-58 / TECH §2.2 #8）：落地时在落点结算一次作物爆炸，落点球内的洒火 / 岩浆块归 F-70。
 *
 * <p>这次爆炸只当「落点触发器」，三个口径都是固定的：威力读 {@code molotov.triggerExplosionPower}
 * （默认 0）、方块破坏恒 {@link BlastRule#NEVER_BREAK}（裁决 A-10：任何威力都不破坏方块）、
 * 伤害上限 0（D-58：0 伤害）。命中结算仍走基类 {@code settle} 的「每次生成恰好一次」。
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

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        this.trigger(level, pos);
    }

    /**
     * D-58 的落点爆炸：威力是唯一可变项，破坏与伤害两个入口按设计关死（A-10 明确「破坏权限与威力无关」，
     * 所以这里没有 {@code BlastRule.forCrop} 分支，只有 {@link BlastRule#NEVER_BREAK} 一个常量）。
     */
    private void trigger(ServerLevel level, Vec3 pos) {
        int power = FarmstormConfig.molotovTriggerExplosionPower.get();
        new FarmstormExplosion(level, this.getOwner(), FarmstormDamageTypes.cropBlast(level, this.getOwner()),
                pos.x, pos.y, pos.z, power, BlastRule.NEVER_BREAK, 0.0F).detonate();
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "莫洛托夫 落点 (%.2f, %.2f, %.2f) 威力 %d",
                pos.x, pos.y, pos.z, power));
    }
}
