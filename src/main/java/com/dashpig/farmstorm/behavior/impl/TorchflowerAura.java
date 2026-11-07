package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.behavior.AuraProvider;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import com.dashpig.farmstorm.util.TempEnchantHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * 激活的火把花（DESIGN §4.9）：每 {@code refreshTicks} 对欧氏半径内的己方玩家刷新 6 个效果。
 * 手持任意剑（主手或副手）额外授予临时火焰附加（D-15），到期或离开范围由 {@link TempEnchantHandler} 自动撤销。
 */
public final class TorchflowerAura implements AuraProvider {

    @Override
    public DeployedKind kind() {
        return DeployedKind.AWAKENED_TORCHFLOWER;
    }

    @Override
    public double radius() {
        return FarmstormConfig.torchflowerAuraRadius.get();
    }

    @Override
    public int scanInterval() {
        return FarmstormConfig.torchflowerRefreshTicks.get();
    }

    /** 名义时长取 6 条效果里最长的一条；逐条施加仍以自己的 {@code durationTicks} 为准。 */
    @Override
    public int effectDurationTicks() {
        int[] durations = {
                FarmstormConfig.torchflowerStrengthDurationTicks.get(),
                FarmstormConfig.torchflowerResistanceDurationTicks.get(),
                FarmstormConfig.torchflowerHasteDurationTicks.get(),
                FarmstormConfig.torchflowerSpeedDurationTicks.get(),
                FarmstormConfig.torchflowerWaterBreathingDurationTicks.get(),
                FarmstormConfig.torchflowerNightVisionDurationTicks.get()
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
        applyEffect(player, MobEffects.DAMAGE_BOOST,
                FarmstormConfig.torchflowerStrengthLevel.get(), FarmstormConfig.torchflowerStrengthDurationTicks.get());
        applyEffect(player, MobEffects.DAMAGE_RESISTANCE,
                FarmstormConfig.torchflowerResistanceLevel.get(), FarmstormConfig.torchflowerResistanceDurationTicks.get());
        applyEffect(player, MobEffects.DIG_SPEED,
                FarmstormConfig.torchflowerHasteLevel.get(), FarmstormConfig.torchflowerHasteDurationTicks.get());
        applyEffect(player, MobEffects.MOVEMENT_SPEED,
                FarmstormConfig.torchflowerSpeedLevel.get(), FarmstormConfig.torchflowerSpeedDurationTicks.get());
        applyEffect(player, MobEffects.WATER_BREATHING,
                FarmstormConfig.torchflowerWaterBreathingLevel.get(), FarmstormConfig.torchflowerWaterBreathingDurationTicks.get());
        applyEffect(player, MobEffects.NIGHT_VISION,
                FarmstormConfig.torchflowerNightVisionLevel.get(), FarmstormConfig.torchflowerNightVisionDurationTicks.get());
        grantFireAspect(level, player);
        // G-7：每次刷新在被施加者周围播 particlesPerRefresh 个 flame。
        int particles = FarmstormConfig.torchflowerParticlesPerRefresh.get();
        if (particles > 0) {
            level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(),
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

    private static void grantFireAspect(ServerLevel level, ServerPlayer player) {
        int fireAspectLevel = FarmstormConfig.torchflowerFireAspectLevel.get();
        if (fireAspectLevel <= 0) {
            return;
        }
        // §4.6 / TECH §4.11.5：临时附魔时长 = refreshTicks × 3（默认 300 刻）。
        int durationTicks = FarmstormConfig.torchflowerRefreshTicks.get() * 3;
        Holder<Enchantment> fireAspect = BuiltInRegistries.ENCHANTMENT.wrapAsHolder(Enchantments.FIRE_ASPECT);
        ItemStack main = player.getMainHandItem();
        if (main.is(ItemTags.SWORDS)) {
            TempEnchantHandler.grant(player, main, fireAspect, fireAspectLevel, durationTicks);
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ItemTags.SWORDS)) {
            TempEnchantHandler.grant(player, offhand, fireAspect, fireAspectLevel, durationTicks);
        }
    }
}
