package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.behavior.TrapBehavior;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

/**
 * 震动豆的陷阱行为（TECH §3.6）：非己方玩家进入 4.0 格即触发，范围内所有玩家获得 {@code vibration_bean.confusionTicks} 刻混乱，
 * 播一次爆炸音后静默消失、无掉落（D-92 / D-101）。
 *
 * <p>两端（触发端与效果端）读同一个 config 键、写同一个 {@code <}，因此不存在「没触发却被混乱」的边界态。
 */
public final class VibrationBeanTrap implements TrapBehavior {

    /** 爆炸音的音源与音高：文档只给了「音源 BLOCKS、音高 0.9」，音量取原版默认的 1.0（TECH §3.6 ②）。 */
    private static final float EXPLODE_VOLUME = 1.0F;
    private static final float EXPLODE_PITCH = 0.9F;

    @Override
    public DeployedKind kind() {
        return DeployedKind.VIBRATION_BEAN;
    }

    /**
     * 触发端认**非己方玩家与敌对生物**（D-114），并沿用既有陷阱的创造 / 旁观豁免（HC-09 / D-92）；
     * 距离判据与效果端共用 {@code vibration_bean.triggerRadius} 与同一个严格小于（D-101）。
     * <p>两遍遍历用同一个 {@code <} 与同一个半径键（不写第二个数）；生物没有队伍归属，因此恒按非己方。
     */
    @Override
    public Optional<LivingEntity> findTrigger(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        Vec3 center = be.getBlockPos().getCenter();
        double radius = FarmstormConfig.vibrationBeanTriggerRadius.get();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative() || be.isAlly(player)) {
                continue;
            }
            if (center.distanceTo(player.position()) < radius) {
                return Optional.of(player);
            }
        }
        for (Monster monster : level.getEntitiesOfClass(Monster.class,
                TrapBehavior.searchBox(be.getBlockPos(), radius))) {
            if (center.distanceTo(monster.position()) < radius) {
                return Optional.of(monster);
            }
        }
        return Optional.empty();
    }

    /**
     * 同一刻的四步（TECH §3.6 / DESIGN §6.5）：半径内**所有**玩家写混乱（不筛敌我，D-27 的一次性 AoE 口径）
     * → 播一次爆炸音（只用原版既有事件，不构造 {@code Explosion}，D-58 边界 / D-100）
     * → {@code consume()} 静默移除，不触战利品表（D-72）。
     */
    @Override
    public void trigger(DeployedCropBlockEntity be, LivingEntity target) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = be.getBlockPos().getCenter();
        double radius = FarmstormConfig.vibrationBeanTriggerRadius.get();
        int ticks = FarmstormConfig.vibrationBeanConfusionTicks.get();
        int inRange = 0;
        int applied = 0;
        for (ServerPlayer player : level.players()) {
            if (center.distanceTo(player.position()) >= radius) {
                continue;
            }
            inRange++;
            if (player.addEffect(new MobEffectInstance(FarmstormMobEffects.CONFUSION, ticks, 0, false, false, false))) {
                applied++;
            }
        }
        BlockPos pos = be.getBlockPos();
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, EXPLODE_VOLUME, EXPLODE_PITCH);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "震动豆 触发 @ %s 由 %s：球内玩家 %d 施加成功 %d（半径 %.2f 格，%d 刻，amplifier 0），爆炸音一次",
                pos.toShortString(), target.getName().getString(), inRange, applied, radius, ticks));
        be.consume();
        // D-63 的扩充 / DESIGN §10.1 #18 + §10.2 第 3 条：只认「结算完成」（施混乱 + 静默移除都执行完），
        // 不认「谁踩的」——结算一完成就按效果端的口径给球内每个玩家各授一次（己方玩家同样推进，D-101）。
        for (ServerPlayer player : level.players()) {
            if (center.distanceTo(player.position()) < radius) {
                FarmstormAdvancements.award(player, "vibration_bean_trigger");
            }
        }
    }
}
