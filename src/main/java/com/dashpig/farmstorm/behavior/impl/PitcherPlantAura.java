package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.AuraProvider;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 激活的瓶子草（DESIGN §4.10）：每 {@code refreshTicks} 对欧氏半径内的己方玩家刷新 3 个正面效果，
 * 并把身上 {@code HARMFUL} 类效果全部清掉（只清半径内的己方玩家，DESIGN 备注 ②）。
 */
public final class PitcherPlantAura implements AuraProvider {

    @Override
    public DeployedKind kind() {
        return DeployedKind.AWAKENED_PITCHER_PLANT;
    }

    @Override
    public double radius() {
        return FarmstormConfig.pitcherPlantAuraRadius.get();
    }

    @Override
    public int scanInterval() {
        return FarmstormConfig.pitcherPlantRefreshTicks.get();
    }

    /** 名义时长取 3 条效果里最长的一条；逐条施加仍以自己的 {@code durationTicks} 为准。 */
    @Override
    public int effectDurationTicks() {
        int[] durations = {
                FarmstormConfig.pitcherPlantRegenerationDurationTicks.get(),
                FarmstormConfig.pitcherPlantSaturationDurationTicks.get(),
                FarmstormConfig.pitcherPlantAbsorptionDurationTicks.get()
        };
        int longest = 0;
        for (int duration : durations) {
            longest = Math.max(longest, duration);
        }
        return longest;
    }

    @Override
    public void apply(DeployedCropBlockEntity be, ServerPlayer player) {
        if (player.isSpectator()) {
            return;                                                      // DESIGN §7.6 L1060：旁观者不被光环覆盖
        }
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        applyEffect(player, MobEffects.REGENERATION,
                FarmstormConfig.pitcherPlantRegenerationLevel.get(), FarmstormConfig.pitcherPlantRegenerationDurationTicks.get());
        applyEffect(player, MobEffects.SATURATION,
                FarmstormConfig.pitcherPlantSaturationLevel.get(), FarmstormConfig.pitcherPlantSaturationDurationTicks.get());
        applyEffect(player, MobEffects.ABSORPTION,
                FarmstormConfig.pitcherPlantAbsorptionLevel.get(), FarmstormConfig.pitcherPlantAbsorptionDurationTicks.get());
        if (FarmstormConfig.pitcherPlantClearHarmful.get()) {
            clearHarmful(player);
        }
        int particles = FarmstormConfig.pitcherPlantParticlesPerRefresh.get();
        if (particles > 0) {
            level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(),
                    particles, 0.4, 0.6, 0.4, 0.01);
        }
        FarmstormAdvancements.award(player, "aura_bath");
    }

    /** 罗马数字 → amplifier 一律 {@code level - 1}；{@code durationTicks = 0} 表示不施加（§4.11.3）。 */
    private static void applyEffect(ServerPlayer player, MobEffect effect, int effectLevel, int durationTicks) {
        if (effectLevel <= 0 || durationTicks <= 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, durationTicks, effectLevel - 1));
    }

    /** 「移除所有负面效果」的口径是 category == HARMFUL，逐实例判断（DESIGN 备注 ②）。 */
    private static void clearHarmful(ServerPlayer player) {
        for (MobEffectInstance instance : List.copyOf(player.getActiveEffects())) {
            if (instance.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                player.removeEffect(instance.getEffect());
            }
        }
    }
}
