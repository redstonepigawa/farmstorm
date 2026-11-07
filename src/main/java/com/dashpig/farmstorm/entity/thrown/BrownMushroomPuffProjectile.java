package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.util.CropProfile;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * ？！棕色菇菇色棕！？（TECH §4.7 / DESIGN §4.14）：落点欧氏半径内<b>所有玩家</b>吃虚弱 + 缓慢。
 * 两个效果时长不同（200 / 100 刻），不可合并；不破坏方块、不造成伤害；不筛敌我（D-27）。
 */
public class BrownMushroomPuffProjectile extends ThrownCropEntity {
    /**
     * 孢子雾主色（土棕，贴近物品本身的棕菇）：{@link ParticleTypes#ENTITY_EFFECT} 用它染色。
     * FIX-8：粒子数、颜色、扩散都是表现细节，不进 config（HC-07 的 117 键是封闭集合）。
     */
    private static final int SPORE_COLOR = 0x8B5A2B;

    /** 孢子雾粒子数。 */
    private static final int SPORE_PARTICLES = 28;

    /** 混入的白烟数量，让雾有体积感。 */
    private static final int SMOKE_PARTICLES = 10;

    /** 粒子在落点的纵向扩散（格）；水平扩散直接取本作物的 radius config。 */
    private static final double SPORE_SPREAD_Y = 0.3;

    public BrownMushroomPuffProjectile(EntityType<? extends BrownMushroomPuffProjectile> type, Level level) {
        super(type, level);
    }

    public BrownMushroomPuffProjectile(EntityType<? extends BrownMushroomPuffProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.BROWN_MUSHROOM_PUFF;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        double radius = FarmstormConfig.brownMushroomRadius.get();
        // DESIGN §3.7：投掷型范围效果按欧氏半径判定，原点为落点精确坐标；谓词里只有几何条件，没有 isAlly。
        for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(radius),
                target -> target.position().distanceTo(pos) <= radius)) {
            applyEffect(player, MobEffects.WEAKNESS,
                    FarmstormConfig.brownMushroomWeaknessLevel.get(), FarmstormConfig.brownMushroomWeaknessDurationTicks.get());
            applyEffect(player, MobEffects.MOVEMENT_SLOWDOWN,
                    FarmstormConfig.brownMushroomSlownessLevel.get(), FarmstormConfig.brownMushroomSlownessDurationTicks.get());
        }
        spawnSporeCloud(level, pos, radius);
    }

    /**
     * 落地孢子雾（FIX-8）：土棕的 {@link ParticleTypes#ENTITY_EFFECT} 为主，混少量白烟。
     * 水平扩散直接用本作物的 {@code radius} config，改半径特效跟着改，不会和玩法范围脱节。
     * 只在服务端广播（C-6：不新增网络包，客户端只播）。
     */
    private static void spawnSporeCloud(ServerLevel level, Vec3 pos, double radius) {
        level.sendParticles(sporeParticle(), pos.x, pos.y, pos.z, SPORE_PARTICLES, radius, SPORE_SPREAD_Y, radius, 0.02);
        level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, SMOKE_PARTICLES, radius, SPORE_SPREAD_Y, radius, 0.01);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.NEUTRAL, 0.8F, 0.7F);
    }

    /** 土棕整数 → 归一化 RGB；ENTITY_EFFECT 的颜色是 0~1 的三个分量。 */
    private static DustParticleOptions sporeParticle() {
        return new DustParticleOptions(new org.joml.Vector3f(
                ((SPORE_COLOR >> 16) & 0xFF) / 255.0F, ((SPORE_COLOR >> 8) & 0xFF) / 255.0F, (SPORE_COLOR & 0xFF) / 255.0F), 1.0F);
    }

    /** 罗马数字 → amplifier 一律 {@code level - 1}；{@code durationTicks = 0} 表示不施加（§4.11.3）。 */
    private static void applyEffect(Player player, MobEffect effect, int effectLevel, int durationTicks) {
        if (effectLevel <= 0 || durationTicks <= 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, durationTicks, effectLevel - 1));
    }
}
