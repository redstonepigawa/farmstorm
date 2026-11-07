package com.dashpig.farmstorm.item;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.LuckyBerryRoller;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 食用型作物（甜菜！ / 幸运浆果 / 狂野苹果，TECH §4.3.4）。
 * 营养值与饱和度由注册时的 FoodProperties 决定（D-29），食用时长统一走 {@code food.eatTicks}；
 * 效果一律在 {@link #finishUsingItem} 之后施加，因为幸运浆果要服务端 RNG、甜菜！要一次性上 4 个效果。
 */
public class FoodCropItem extends ModifiedCropItem {

    public FoodCropItem(CropProfile profile, Item.Properties properties) {
        super(profile, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return FarmstormConfig.foodEatTicks.get();
    }

    /**
     * 原版食物流程（{@code LivingEntity#completeUsingItem} → {@code Item#finishUsingItem} → {@code Player#eat}）
     * 负责消耗 1 个物品、喂饱玩家与播放音效，这里只在它之后追加各自的食用效果。
     * HC-03：效果施加只在服务端路径内，客户端连随机数都不碰（TECH §8.3）。
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (level instanceof ServerLevel serverLevel && livingEntity instanceof ServerPlayer player) {
            onEaten(stack, serverLevel, player);
        }
        return result;
    }

    /**
     * 吃掉之后干什么（TECH §4.3.4）：三种食用型（甜菜！ / 幸运浆果 / 狂野苹果）按作物档案分流。
     * 只会在服务端被调用。
     */
    protected void onEaten(ItemStack stack, ServerLevel level, ServerPlayer player) {
        CropProfile profile = profile();
        if (profile == CropProfile.POWER_BEET) {
            applyPowerBeet(player);
            return;
        }
        if (profile == CropProfile.LUCKY_BERRY) {
            LuckyBerryRoller.apply(level, player, LuckyBerryRoller.roll(level.getRandom()));
        }
        if (profile == CropProfile.WILD_APPLE) {
            applyWildHunter(player);
        }
    }

    /** DESIGN §4.5：力量 / 速度 / 急迫 / 抗性提升一次性施加，等级与时长全部取 {@code power_beet.<effect>.*}。 */
    private static void applyPowerBeet(ServerPlayer player) {
        applyEffect(player, MobEffects.DAMAGE_BOOST,
                FarmstormConfig.powerBeetStrengthLevel.get(),
                FarmstormConfig.powerBeetStrengthDurationTicks.get());
        applyEffect(player, MobEffects.MOVEMENT_SPEED,
                FarmstormConfig.powerBeetSpeedLevel.get(),
                FarmstormConfig.powerBeetSpeedDurationTicks.get());
        applyEffect(player, MobEffects.DIG_SPEED,
                FarmstormConfig.powerBeetHasteLevel.get(),
                FarmstormConfig.powerBeetHasteDurationTicks.get());
        applyEffect(player, MobEffects.DAMAGE_RESISTANCE,
                FarmstormConfig.powerBeetResistanceLevel.get(),
                FarmstormConfig.powerBeetResistanceDurationTicks.get());
    }

    /**
     * A-02：狂野苹果只负责施加 {@code farmstorm:wild_hunter}，**时长不自己存键**，
     * 一律读 {@code wild_hunter.durationTicks}（默认 700 刻）；amplifier 0。
     */
    private static void applyWildHunter(ServerPlayer player) {
        int duration = FarmstormConfig.wildHunterDurationTicks.get();
        if (duration <= 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(FarmstormMobEffects.WILD_HUNTER.get(), duration, 0));
    }

    /** {@code level} 以 1 起算（写入 amplifier 时减 1），{@code durationTicks = 0} 表示不施加（§4.11.3）。 */
    private static void applyEffect(ServerPlayer player, MobEffect effect, int level, int durationTicks) {
        if (level <= 0 || durationTicks <= 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, durationTicks, level - 1));
    }
}
