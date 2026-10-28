package com.dashpig.farmstorm.config;

import com.dashpig.farmstorm.Farmstorm;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端配置（DEC D-21 / D-22，TECH §4.11）。
 * 117 个键是封闭集合（HC-07）：键名与默认值逐字对齐 01-DESIGN.md §3.8，不接受「差不多」。
 * 字段不能是 final——值在静态块里赋值。
 */
public final class FarmstormConfig {
    public static final ModConfigSpec SPEC;

    // ---- 4.11.1 通用项（14）----
    public static ModConfigSpec.IntValue converterXpCost;
    public static ModConfigSpec.IntValue converterNetherStarForwardCost;
    public static ModConfigSpec.BooleanValue converterNetherStarOnReverse;

    public static ModConfigSpec.IntValue stackThrowable;
    public static ModConfigSpec.IntValue stackOther;

    public static ModConfigSpec.IntValue deployTurretMiningTicks;
    public static ModConfigSpec.BooleanValue deploySupportMustBeFullOpaque;
    public static ModConfigSpec.BooleanValue deployDropOnSupportLost;

    public static ModConfigSpec.ConfigValue<String> teamsResolver;
    public static ModConfigSpec.ConfigValue<String> teamsUntamedFallback;

    public static ModConfigSpec.DoubleValue explosionSliceResistThreshold;
    public static ModConfigSpec.DoubleValue explosionGrenadeResistThreshold;

    public static ModConfigSpec.IntValue throwableLifetimeTicks;

    public static ModConfigSpec.IntValue foodEatTicks;

    // ---- 4.11.2 各作物专属项（51）----
    public static ModConfigSpec.IntValue puffedWheatDryEdge;
    public static ModConfigSpec.IntValue puffedWheatWetEdge;

    public static ModConfigSpec.IntValue blastCarrotTriggerRadius;
    public static ModConfigSpec.IntValue blastCarrotExplosionPower;
    public static ModConfigSpec.IntValue blastCarrotMaxDamage;
    public static ModConfigSpec.IntValue blastCarrotNauseaLevel;
    public static ModConfigSpec.IntValue blastCarrotNauseaDurationTicks;

    public static ModConfigSpec.IntValue ubiServerExplosionPower;
    public static ModConfigSpec.IntValue ubiServerMaxDamage;

    public static ModConfigSpec.IntValue toxicUbiServerExplosionPower;
    public static ModConfigSpec.IntValue toxicUbiServerMaxDamage;
    public static ModConfigSpec.DoubleValue toxicUbiServerCloudRadius;
    public static ModConfigSpec.IntValue toxicUbiServerCloudDurationTicks;

    public static ModConfigSpec.IntValue melonSliceBombExplosionPower;
    public static ModConfigSpec.IntValue melonSliceBombMaxDamage;

    public static ModConfigSpec.IntValue melonGrenadeSpawnCount;
    public static ModConfigSpec.IntValue melonGrenadeSpawnOffsetY;
    public static ModConfigSpec.DoubleValue melonGrenadeHorizontalJitter;
    public static ModConfigSpec.IntValue melonGrenadeExplosionPower;
    public static ModConfigSpec.IntValue melonGrenadeMaxDamage;

    public static ModConfigSpec.IntValue pumpkinTurretTargetRadius;
    public static ModConfigSpec.IntValue pumpkinTurretProjectileDamage;
    public static ModConfigSpec.IntValue pumpkinTurretKnockbackLevel;
    public static ModConfigSpec.IntValue pumpkinTurretFireIntervalTicks;

    public static ModConfigSpec.DoubleValue torchflowerAuraRadius;
    public static ModConfigSpec.IntValue torchflowerRefreshTicks;
    public static ModConfigSpec.IntValue torchflowerFireAspectLevel;
    public static ModConfigSpec.IntValue torchflowerParticlesPerRefresh;

    public static ModConfigSpec.DoubleValue pitcherPlantAuraRadius;
    public static ModConfigSpec.IntValue pitcherPlantRefreshTicks;
    public static ModConfigSpec.BooleanValue pitcherPlantClearHarmful;
    public static ModConfigSpec.IntValue pitcherPlantParticlesPerRefresh;

    public static ModConfigSpec.DoubleValue fangTrapTriggerRadius;
    public static ModConfigSpec.IntValue fangTrapOuterFangCount;
    public static ModConfigSpec.IntValue fangTrapOuterDelayTicks;
    public static ModConfigSpec.DoubleValue fangTrapOuterRadius;
    public static ModConfigSpec.IntValue fangTrapInnerFangCount;
    public static ModConfigSpec.IntValue fangTrapInnerDelayTicks;
    public static ModConfigSpec.DoubleValue fangTrapInnerRadius;

    public static ModConfigSpec.IntValue firecrackerSpawnIntervalTicks;
    public static ModConfigSpec.IntValue firecrackerDurationTicks;

    public static ModConfigSpec.DoubleValue redMushroomRadius;
    public static ModConfigSpec.DoubleValue brownMushroomRadius;

    public static ModConfigSpec.IntValue luckyBerryNutrition;
    public static ModConfigSpec.DoubleValue luckyBerrySaturationModifier;
    public static ModConfigSpec.IntValue luckyBerryRandomCropCount;
    public static ModConfigSpec.IntValue luckyBerryLootChestCount;
    public static ModConfigSpec.IntValue luckyBerrySmallExplosionPower;
    public static ModConfigSpec.IntValue luckyBerryCreativeDurationTicks;

    public static ModConfigSpec.IntValue powerBeetNutrition;
    public static ModConfigSpec.DoubleValue powerBeetSaturationModifier;

    // ---- 4.11.3 效果类键（44 = 22 组 × 2）----
    public static ModConfigSpec.IntValue powerBeetStrengthLevel;
    public static ModConfigSpec.IntValue powerBeetStrengthDurationTicks;
    public static ModConfigSpec.IntValue powerBeetSpeedLevel;
    public static ModConfigSpec.IntValue powerBeetSpeedDurationTicks;
    public static ModConfigSpec.IntValue powerBeetHasteLevel;
    public static ModConfigSpec.IntValue powerBeetHasteDurationTicks;
    public static ModConfigSpec.IntValue powerBeetResistanceLevel;
    public static ModConfigSpec.IntValue powerBeetResistanceDurationTicks;

    public static ModConfigSpec.IntValue torchflowerStrengthLevel;
    public static ModConfigSpec.IntValue torchflowerStrengthDurationTicks;
    public static ModConfigSpec.IntValue torchflowerResistanceLevel;
    public static ModConfigSpec.IntValue torchflowerResistanceDurationTicks;
    public static ModConfigSpec.IntValue torchflowerHasteLevel;
    public static ModConfigSpec.IntValue torchflowerHasteDurationTicks;
    public static ModConfigSpec.IntValue torchflowerSpeedLevel;
    public static ModConfigSpec.IntValue torchflowerSpeedDurationTicks;
    public static ModConfigSpec.IntValue torchflowerWaterBreathingLevel;
    public static ModConfigSpec.IntValue torchflowerWaterBreathingDurationTicks;
    public static ModConfigSpec.IntValue torchflowerNightVisionLevel;
    public static ModConfigSpec.IntValue torchflowerNightVisionDurationTicks;

    public static ModConfigSpec.IntValue pitcherPlantRegenerationLevel;
    public static ModConfigSpec.IntValue pitcherPlantRegenerationDurationTicks;
    public static ModConfigSpec.IntValue pitcherPlantSaturationLevel;
    public static ModConfigSpec.IntValue pitcherPlantSaturationDurationTicks;
    public static ModConfigSpec.IntValue pitcherPlantAbsorptionLevel;
    public static ModConfigSpec.IntValue pitcherPlantAbsorptionDurationTicks;

    public static ModConfigSpec.IntValue redMushroomNauseaLevel;
    public static ModConfigSpec.IntValue redMushroomNauseaDurationTicks;
    public static ModConfigSpec.IntValue redMushroomBlindnessLevel;
    public static ModConfigSpec.IntValue redMushroomBlindnessDurationTicks;

    public static ModConfigSpec.IntValue brownMushroomWeaknessLevel;
    public static ModConfigSpec.IntValue brownMushroomWeaknessDurationTicks;
    public static ModConfigSpec.IntValue brownMushroomSlownessLevel;
    public static ModConfigSpec.IntValue brownMushroomSlownessDurationTicks;

    public static ModConfigSpec.IntValue luckyBerryRegenerationLevel;
    public static ModConfigSpec.IntValue luckyBerryRegenerationDurationTicks;
    public static ModConfigSpec.IntValue luckyBerryStrengthLevel;
    public static ModConfigSpec.IntValue luckyBerryStrengthDurationTicks;
    public static ModConfigSpec.IntValue luckyBerryResistanceLevel;
    public static ModConfigSpec.IntValue luckyBerryResistanceDurationTicks;
    public static ModConfigSpec.IntValue luckyBerryJumpBoostLevel;
    public static ModConfigSpec.IntValue luckyBerryJumpBoostDurationTicks;
    public static ModConfigSpec.IntValue luckyBerrySpeedLevel;
    public static ModConfigSpec.IntValue luckyBerrySpeedDurationTicks;

    // ---- 4.11.4 幸运浆果概率表（8）----
    public static ModConfigSpec.DoubleValue luckyBerryChanceLightning;
    public static ModConfigSpec.DoubleValue luckyBerryChanceRandomCrops;
    public static ModConfigSpec.DoubleValue luckyBerryChanceRegeneration;
    public static ModConfigSpec.DoubleValue luckyBerryChanceLootChest;
    public static ModConfigSpec.DoubleValue luckyBerryChanceSmallExplosion;
    public static ModConfigSpec.DoubleValue luckyBerryChanceChargedCreeper;
    public static ModConfigSpec.DoubleValue luckyBerryChanceGodlikeBuff;
    public static ModConfigSpec.DoubleValue luckyBerryChanceCreativeMode;

    // ---- 烘焙快照 ----
    /** 概率表条目数（§4.11.4 / §4.12）。 */
    public static final int LUCKY_OUTCOME_COUNT = 8;
    public static final int LIGHTNING = 0;
    public static final int SPAWN_CROPS = 1;
    public static final int REGEN = 2;
    public static final int LOOT_CHESTS = 3;
    public static final int SMALL_EXPLOSION = 4;
    public static final int CHARGED_CREEPER = 5;
    public static final int POWER = 6;
    public static final int CREATIVE = 7;

    /** §3.8.5 的默认百分比，和恒为 100。 */
    private static final double[] DEFAULT_PERCENT = {15.0, 25.0, 25.0, 25.0, 5.0, 3.0, 1.9, 0.1};
    /** 默认百分比 ×10 得到的千分比，和恒为 1000（§4.12）。 */
    private static final int[] DEFAULT_PERMILLE = {150, 250, 250, 250, 50, 30, 19, 1};
    /** 百分比之和的允许误差：配置写的是 0.1 这种小数，浮点累加不足以做精确相等比较。 */
    private static final double PERCENT_SUM_TOLERANCE = 1e-6;

    private static int[] luckyPermille = DEFAULT_PERMILLE.clone();
    private static double sliceResistThreshold = 6.0;
    private static double grenadeResistThreshold = 6.0;
    private static String untamedFallback = "self";

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        // ---- 通用项 ----
        b.push("converter");
        converterXpCost = b.comment("D-12：每次转换扣除的经验点数（不足则拒绝转换，绝不扣等级）")
                .translation(tr("converter.xpCost"))
                .defineInRange("xpCost", 3, 0, 1000);
        converterNetherStarForwardCost = b.comment("D-12：火把花 / 瓶子草正向转换额外消耗的下界之星个数")
                .translation(tr("converter.netherStarForwardCost"))
                .defineInRange("netherStarForwardCost", 1, 0, 64);
        converterNetherStarOnReverse = b.comment("D-12：反向转换是否也消耗下界之星")
                .translation(tr("converter.netherStarOnReverse"))
                .define("netherStarOnReverse", false);
        b.pop();

        b.push("stack");
        stackThrowable = b.comment("D-16：投掷型作物的堆叠上限")
                .translation(tr("stack.throwable"))
                .defineInRange("throwable", 16, 1, 64);
        stackOther = b.comment("D-16：部署型 / 食用型 / 播种物的堆叠上限")
                .translation(tr("stack.other"))
                .defineInRange("other", 64, 1, 64);
        b.pop();

        b.push("deploy");
        deployTurretMiningTicks = b.comment("D-13 / D-37：仅 pumpkin_turret 的固定硬挖刻数（200 刻 = 10 秒）")
                .translation(tr("deploy.turretMiningTicks"))
                .defineInRange("turretMiningTicks", 200, 0, 72000);
        deploySupportMustBeFullOpaque = b.comment("是否强制依附方块为完整不透明方块（半砖 / 玻璃 / 树叶不合格）")
                .translation(tr("deploy.supportMustBeFullOpaque"))
                .define("supportMustBeFullOpaque", true);
        deployDropOnSupportLost = b.comment("G-9：依附方块消失且不悬空时，是否掉落物品本体")
                .translation(tr("deploy.dropOnSupportLost"))
                .define("dropOnSupportLost", true);
        b.pop();

        b.push("teams");
        // 候选项用 Arrays.asList 而不是 List.of：文件首次生成时 correct() 会以 null 调 contains，
        // List.of 的不可变实现会因此抛 NPE（真实踩到过）。
        teamsResolver = b.comment("D-06：己方判定实现（scoreboard / ftbteams，v0.0.1 只实现 scoreboard）")
                .translation(tr("teams.resolver"))
                .defineInList("resolver", "scoreboard", java.util.Arrays.asList("scoreboard", "ftbteams"));
        teamsUntamedFallback = b.comment("G-8：无队伍玩家的降级口径（self = 只对自身友好）")
                .translation(tr("teams.untamedFallback"))
                .defineInList("untamedFallback", "self", java.util.Arrays.asList("self", "public"));
        b.pop();

        b.push("explosion");
        explosionSliceResistThreshold = b.comment("C-2 / D-19：瓜片子爆炸无法摧毁的抗性阈值（6.0 = 圆石）")
                .translation(tr("explosion.sliceResistThreshold"))
                .defineInRange("sliceResistThreshold", 6.0, 0.0, 100.0);
        explosionGrenadeResistThreshold = b.comment("D-19：瓜！爆炸无法摧毁的抗性阈值（6.0 = 圆石）")
                .translation(tr("explosion.grenadeResistThreshold"))
                .defineInRange("grenadeResistThreshold", 6.0, 0.0, 100.0);
        b.pop();

        b.push("throwable");
        throwableLifetimeTicks = b.comment("G-6：投掷物实体的寿命上限")
                .translation(tr("throwable.lifetimeTicks"))
                .defineInRange("lifetimeTicks", 100, 1, 1200);
        b.pop();

        b.push("food");
        foodEatTicks = b.comment("D-29：食用型作物的食用时长")
                .translation(tr("food.eatTicks"))
                .defineInRange("eatTicks", 32, 1, 200);
        b.pop();

        // ---- 各作物专属项 ----
        b.push("puffed_wheat");
        puffedWheatDryEdge = b.comment("干态替换立方体边长")
                .translation(tr("puffed_wheat.dryEdge"))
                .defineInRange("dryEdge", 3, 1, 15);
        puffedWheatWetEdge = b.comment("遇水扩容后的立方体边长")
                .translation(tr("puffed_wheat.wetEdge"))
                .defineInRange("wetEdge", 5, 1, 15);
        b.pop();

        b.push("blast_carrot");
        blastCarrotTriggerRadius = b.comment("陷阱触发的切比雪夫半径")
                .translation(tr("blast_carrot.triggerRadius"))
                .defineInRange("triggerRadius", 1, 0, 16);
        blastCarrotExplosionPower = b.comment("爆炸威力")
                .translation(tr("blast_carrot.explosionPower"))
                .defineInRange("explosionPower", 2, 0, 100);
        blastCarrotMaxDamage = b.comment("单次爆炸的伤害上限")
                .translation(tr("blast_carrot.maxDamage"))
                .defineInRange("maxDamage", 7, 0, 1000);
        b.push("nausea");
        blastCarrotNauseaLevel = b.comment("反胃等级（1 = I 级）")
                .translation(tr("blast_carrot.nausea.level"))
                .defineInRange("level", 3, 1, 255);
        blastCarrotNauseaDurationTicks = b.comment("反胃时长（0 = 不施加）")
                .translation(tr("blast_carrot.nausea.durationTicks"))
                .defineInRange("durationTicks", 60, 0, 72000);
        b.pop();
        b.pop();

        b.push("ubi_server");
        ubiServerExplosionPower = b.comment("爆炸威力")
                .translation(tr("ubi_server.explosionPower"))
                .defineInRange("explosionPower", 4, 0, 100);
        ubiServerMaxDamage = b.comment("伤害上限")
                .translation(tr("ubi_server.maxDamage"))
                .defineInRange("maxDamage", 2, 0, 1000);
        b.pop();

        b.push("toxic_ubi_server");
        toxicUbiServerExplosionPower = b.comment("爆炸威力")
                .translation(tr("toxic_ubi_server.explosionPower"))
                .defineInRange("explosionPower", 2, 0, 100);
        toxicUbiServerMaxDamage = b.comment("伤害上限")
                .translation(tr("toxic_ubi_server.maxDamage"))
                .defineInRange("maxDamage", 2, 0, 1000);
        toxicUbiServerCloudRadius = b.comment("G-4：剧毒药水云半径（沿用原版滞留药水）")
                .translation(tr("toxic_ubi_server.cloudRadius"))
                .defineInRange("cloudRadius", 3.0, 0.0, 16.0);
        toxicUbiServerCloudDurationTicks = b.comment("G-4：剧毒药水云时长（沿用原版滞留药水）")
                .translation(tr("toxic_ubi_server.cloudDurationTicks"))
                .defineInRange("cloudDurationTicks", 600, 0, 72000);
        b.pop();

        b.push("melon_slice_bomb");
        melonSliceBombExplosionPower = b.comment("爆炸威力")
                .translation(tr("melon_slice_bomb.explosionPower"))
                .defineInRange("explosionPower", 2, 0, 100);
        melonSliceBombMaxDamage = b.comment("伤害上限")
                .translation(tr("melon_slice_bomb.maxDamage"))
                .defineInRange("maxDamage", 6, 0, 1000);
        b.pop();

        b.push("melon_grenade");
        melonGrenadeSpawnCount = b.comment("落点上方生成的西瓜片投掷物个数")
                .translation(tr("melon_grenade.spawnCount"))
                .defineInRange("spawnCount", 4, 0, 64);
        melonGrenadeSpawnOffsetY = b.comment("生成位置相对落点的垂直偏移")
                .translation(tr("melon_grenade.spawnOffsetY"))
                .defineInRange("spawnOffsetY", 1, 0, 8);
        melonGrenadeHorizontalJitter = b.comment("x/z 轴随机偏移上界")
                .translation(tr("melon_grenade.horizontalJitter"))
                .defineInRange("horizontalJitter", 0.3, 0.0, 8.0);
        melonGrenadeExplosionPower = b.comment("爆炸威力")
                .translation(tr("melon_grenade.explosionPower"))
                .defineInRange("explosionPower", 3, 0, 100);
        melonGrenadeMaxDamage = b.comment("伤害上限")
                .translation(tr("melon_grenade.maxDamage"))
                .defineInRange("maxDamage", 8, 0, 1000);
        b.pop();

        b.push("pumpkin_turret");
        pumpkinTurretTargetRadius = b.comment("索敌切比雪夫半径")
                .translation(tr("pumpkin_turret.targetRadius"))
                .defineInRange("targetRadius", 15, 0, 64);
        pumpkinTurretProjectileDamage = b.comment("导弹命中造成的虚空伤害点数")
                .translation(tr("pumpkin_turret.projectileDamage"))
                .defineInRange("projectileDamage", 1, 0, 100);
        pumpkinTurretKnockbackLevel = b.comment("命中时施加的击退等级")
                .translation(tr("pumpkin_turret.knockbackLevel"))
                .defineInRange("knockbackLevel", 1, 0, 10);
        pumpkinTurretFireIntervalTicks = b.comment("D-28：开火间隔（60 刻 = 3 秒）")
                .translation(tr("pumpkin_turret.fireIntervalTicks"))
                .defineInRange("fireIntervalTicks", 60, 1, 72000);
        b.pop();

        b.push("awakened_torchflower");
        torchflowerAuraRadius = b.comment("光环欧氏半径")
                .translation(tr("awakened_torchflower.auraRadius"))
                .defineInRange("auraRadius", 20.0, 0.0, 128.0);
        torchflowerRefreshTicks = b.comment("光环刷新间隔（5 秒）")
                .translation(tr("awakened_torchflower.refreshTicks"))
                .defineInRange("refreshTicks", 100, 1, 72000);
        torchflowerFireAspectLevel = b.comment("手持剑时授予的火焰附加等级")
                .translation(tr("awakened_torchflower.fireAspectLevel"))
                .defineInRange("fireAspectLevel", 1, 0, 10);
        torchflowerParticlesPerRefresh = b.comment("G-7：每次刷新在玩家周围生成的 flame 粒子数")
                .translation(tr("awakened_torchflower.particlesPerRefresh"))
                .defineInRange("particlesPerRefresh", 8, 0, 64);
        b.push("strength");
        torchflowerStrengthLevel = b.comment("力量等级（1 = I 级）")
                .translation(tr("awakened_torchflower.strength.level"))
                .defineInRange("level", 3, 1, 255);
        torchflowerStrengthDurationTicks = b.comment("力量时长（0 = 不施加）")
                .translation(tr("awakened_torchflower.strength.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.push("resistance");
        torchflowerResistanceLevel = b.comment("抗性提升等级（1 = I 级）")
                .translation(tr("awakened_torchflower.resistance.level"))
                .defineInRange("level", 2, 1, 255);
        torchflowerResistanceDurationTicks = b.comment("抗性提升时长（0 = 不施加）")
                .translation(tr("awakened_torchflower.resistance.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.push("haste");
        torchflowerHasteLevel = b.comment("急迫等级（1 = I 级）")
                .translation(tr("awakened_torchflower.haste.level"))
                .defineInRange("level", 2, 1, 255);
        torchflowerHasteDurationTicks = b.comment("急迫时长（0 = 不施加）")
                .translation(tr("awakened_torchflower.haste.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.push("speed");
        torchflowerSpeedLevel = b.comment("速度等级（1 = I 级）")
                .translation(tr("awakened_torchflower.speed.level"))
                .defineInRange("level", 1, 1, 255);
        torchflowerSpeedDurationTicks = b.comment("速度时长（0 = 不施加）")
                .translation(tr("awakened_torchflower.speed.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.push("water_breathing");
        torchflowerWaterBreathingLevel = b.comment("水下呼吸等级（1 = I 级）")
                .translation(tr("awakened_torchflower.water_breathing.level"))
                .defineInRange("level", 1, 1, 255);
        torchflowerWaterBreathingDurationTicks = b.comment("水下呼吸时长（0 = 不施加）")
                .translation(tr("awakened_torchflower.water_breathing.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.push("night_vision");
        torchflowerNightVisionLevel = b.comment("夜视等级（1 = I 级）")
                .translation(tr("awakened_torchflower.night_vision.level"))
                .defineInRange("level", 1, 1, 255);
        torchflowerNightVisionDurationTicks = b.comment("夜视时长（0 = 不施加）")
                .translation(tr("awakened_torchflower.night_vision.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.pop();

        b.push("awakened_pitcher_plant");
        pitcherPlantAuraRadius = b.comment("光环欧氏半径")
                .translation(tr("awakened_pitcher_plant.auraRadius"))
                .defineInRange("auraRadius", 10.0, 0.0, 128.0);
        pitcherPlantRefreshTicks = b.comment("光环刷新间隔（5 秒）")
                .translation(tr("awakened_pitcher_plant.refreshTicks"))
                .defineInRange("refreshTicks", 100, 1, 72000);
        pitcherPlantClearHarmful = b.comment("是否移除范围内玩家的全部负面效果")
                .translation(tr("awakened_pitcher_plant.clearHarmful"))
                .define("clearHarmful", true);
        pitcherPlantParticlesPerRefresh = b.comment("G-7：每次刷新在玩家周围生成的 heart 粒子数")
                .translation(tr("awakened_pitcher_plant.particlesPerRefresh"))
                .defineInRange("particlesPerRefresh", 8, 0, 64);
        b.push("regeneration");
        pitcherPlantRegenerationLevel = b.comment("生命恢复等级（1 = I 级）")
                .translation(tr("awakened_pitcher_plant.regeneration.level"))
                .defineInRange("level", 2, 1, 255);
        pitcherPlantRegenerationDurationTicks = b.comment("生命恢复时长（0 = 不施加）")
                .translation(tr("awakened_pitcher_plant.regeneration.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.push("saturation");
        pitcherPlantSaturationLevel = b.comment("饱和等级（1 = I 级）")
                .translation(tr("awakened_pitcher_plant.saturation.level"))
                .defineInRange("level", 2, 1, 255);
        pitcherPlantSaturationDurationTicks = b.comment("饱和时长（0 = 不施加）")
                .translation(tr("awakened_pitcher_plant.saturation.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.push("absorption");
        pitcherPlantAbsorptionLevel = b.comment("伤害吸收等级（1 = I 级）")
                .translation(tr("awakened_pitcher_plant.absorption.level"))
                .defineInRange("level", 1, 1, 255);
        pitcherPlantAbsorptionDurationTicks = b.comment("伤害吸收时长（0 = 不施加）")
                .translation(tr("awakened_pitcher_plant.absorption.durationTicks"))
                .defineInRange("durationTicks", 300, 0, 72000);
        b.pop();
        b.pop();

        b.push("fang_trap_cactus");
        fangTrapTriggerRadius = b.comment("C-5 / D-20：触发用欧氏距离")
                .translation(tr("fang_trap_cactus.triggerRadius"))
                .defineInRange("triggerRadius", 3.0, 0.0, 32.0);
        fangTrapOuterFangCount = b.comment("外圈尖牙个数")
                .translation(tr("fang_trap_cactus.outerFangCount"))
                .defineInRange("outerFangCount", 8, 1, 64);
        fangTrapOuterDelayTicks = b.comment("外圈尖牙延迟（0.5 秒）")
                .translation(tr("fang_trap_cactus.outerDelayTicks"))
                .defineInRange("outerDelayTicks", 10, 0, 200);
        fangTrapOuterRadius = b.comment("G-3：外圈尖牙分布半径")
                .translation(tr("fang_trap_cactus.outerRadius"))
                .defineInRange("outerRadius", 2.0, 0.0, 16.0);
        fangTrapInnerFangCount = b.comment("内圈尖牙个数")
                .translation(tr("fang_trap_cactus.innerFangCount"))
                .defineInRange("innerFangCount", 5, 1, 64);
        fangTrapInnerDelayTicks = b.comment("内圈尖牙延迟（0.2 秒）")
                .translation(tr("fang_trap_cactus.innerDelayTicks"))
                .defineInRange("innerDelayTicks", 4, 0, 200);
        fangTrapInnerRadius = b.comment("G-3：内圈尖牙分布半径")
                .translation(tr("fang_trap_cactus.innerRadius"))
                .defineInRange("innerRadius", 1.0, 0.0, 16.0);
        b.pop();

        b.push("bamboo_firecracker");
        firecrackerSpawnIntervalTicks = b.comment("生成一个风弹的间隔")
                .translation(tr("bamboo_firecracker.spawnIntervalTicks"))
                .defineInRange("spawnIntervalTicks", 5, 1, 200);
        firecrackerDurationTicks = b.comment("生成持续总时长（140 / 5 = 28 次）")
                .translation(tr("bamboo_firecracker.durationTicks"))
                .defineInRange("durationTicks", 140, 1, 7200);
        b.pop();

        b.push("red_mushroom_puff");
        redMushroomRadius = b.comment("范围效果欧氏半径")
                .translation(tr("red_mushroom_puff.radius"))
                .defineInRange("radius", 4.0, 0.0, 32.0);
        b.push("nausea");
        redMushroomNauseaLevel = b.comment("反胃等级（1 = I 级）")
                .translation(tr("red_mushroom_puff.nausea.level"))
                .defineInRange("level", 5, 1, 255);
        redMushroomNauseaDurationTicks = b.comment("反胃时长（0 = 不施加）")
                .translation(tr("red_mushroom_puff.nausea.durationTicks"))
                .defineInRange("durationTicks", 60, 0, 72000);
        b.pop();
        b.push("blindness");
        redMushroomBlindnessLevel = b.comment("失明等级（1 = I 级）")
                .translation(tr("red_mushroom_puff.blindness.level"))
                .defineInRange("level", 1, 1, 255);
        redMushroomBlindnessDurationTicks = b.comment("失明时长（0 = 不施加）")
                .translation(tr("red_mushroom_puff.blindness.durationTicks"))
                .defineInRange("durationTicks", 60, 0, 72000);
        b.pop();
        b.pop();

        b.push("brown_mushroom_puff");
        brownMushroomRadius = b.comment("范围效果欧氏半径")
                .translation(tr("brown_mushroom_puff.radius"))
                .defineInRange("radius", 4.0, 0.0, 32.0);
        b.push("weakness");
        brownMushroomWeaknessLevel = b.comment("虚弱等级（1 = I 级）")
                .translation(tr("brown_mushroom_puff.weakness.level"))
                .defineInRange("level", 2, 1, 255);
        brownMushroomWeaknessDurationTicks = b.comment("虚弱时长（0 = 不施加）")
                .translation(tr("brown_mushroom_puff.weakness.durationTicks"))
                .defineInRange("durationTicks", 200, 0, 72000);
        b.pop();
        b.push("slowness");
        brownMushroomSlownessLevel = b.comment("缓慢等级（1 = I 级）")
                .translation(tr("brown_mushroom_puff.slowness.level"))
                .defineInRange("level", 2, 1, 255);
        brownMushroomSlownessDurationTicks = b.comment("缓慢时长（0 = 不施加）")
                .translation(tr("brown_mushroom_puff.slowness.durationTicks"))
                .defineInRange("durationTicks", 100, 0, 72000);
        b.pop();
        b.pop();

        b.push("lucky_berry");
        luckyBerryNutrition = b.comment("D-29：营养值")
                .translation(tr("lucky_berry.nutrition"))
                .defineInRange("nutrition", 4, 0, 20);
        luckyBerrySaturationModifier = b.comment("D-29：饱和度修正")
                .translation(tr("lucky_berry.saturationModifier"))
                .defineInRange("saturationModifier", 0.3, 0.0, 10.0);
        luckyBerryRandomCropCount = b.comment("随机生成改良作物的个数")
                .translation(tr("lucky_berry.randomCropCount"))
                .defineInRange("randomCropCount", 16, 0, 64);
        luckyBerryLootChestCount = b.comment("生成的末地城战利品宝箱个数")
                .translation(tr("lucky_berry.lootChestCount"))
                .defineInRange("lootChestCount", 2, 0, 16);
        luckyBerrySmallExplosionPower = b.comment("小爆炸的威力")
                .translation(tr("lucky_berry.smallExplosionPower"))
                .defineInRange("smallExplosionPower", 1, 0, 100);
        luckyBerryCreativeDurationTicks = b.comment("D-18：创造模式的持续时长（90 秒）")
                .translation(tr("lucky_berry.creativeDurationTicks"))
                .defineInRange("creativeDurationTicks", 1800, 1, 72000);
        b.push("regeneration");
        luckyBerryRegenerationLevel = b.comment("生命恢复等级（1 = I 级，25% 分支）")
                .translation(tr("lucky_berry.regeneration.level"))
                .defineInRange("level", 5, 1, 255);
        luckyBerryRegenerationDurationTicks = b.comment("生命恢复时长（0 = 不施加）")
                .translation(tr("lucky_berry.regeneration.durationTicks"))
                .defineInRange("durationTicks", 100, 0, 72000);
        b.pop();
        b.push("strength");
        luckyBerryStrengthLevel = b.comment("力量等级（1 = I 级，1.9% 分支）")
                .translation(tr("lucky_berry.strength.level"))
                .defineInRange("level", 5, 1, 255);
        luckyBerryStrengthDurationTicks = b.comment("力量时长（0 = 不施加）")
                .translation(tr("lucky_berry.strength.durationTicks"))
                .defineInRange("durationTicks", 12000, 0, 72000);
        b.pop();
        b.push("resistance");
        luckyBerryResistanceLevel = b.comment("抗性提升等级（1 = I 级，1.9% 分支）")
                .translation(tr("lucky_berry.resistance.level"))
                .defineInRange("level", 3, 1, 255);
        luckyBerryResistanceDurationTicks = b.comment("抗性提升时长（0 = 不施加）")
                .translation(tr("lucky_berry.resistance.durationTicks"))
                .defineInRange("durationTicks", 12000, 0, 72000);
        b.pop();
        b.push("jump_boost");
        luckyBerryJumpBoostLevel = b.comment("跳跃提升等级（1 = I 级，1.9% 分支）")
                .translation(tr("lucky_berry.jump_boost.level"))
                .defineInRange("level", 5, 1, 255);
        luckyBerryJumpBoostDurationTicks = b.comment("跳跃提升时长（0 = 不施加）")
                .translation(tr("lucky_berry.jump_boost.durationTicks"))
                .defineInRange("durationTicks", 12000, 0, 72000);
        b.pop();
        b.push("speed");
        luckyBerrySpeedLevel = b.comment("速度等级（1 = I 级，1.9% 分支）")
                .translation(tr("lucky_berry.speed.level"))
                .defineInRange("level", 2, 1, 255);
        luckyBerrySpeedDurationTicks = b.comment("速度时长（0 = 不施加）")
                .translation(tr("lucky_berry.speed.durationTicks"))
                .defineInRange("durationTicks", 12000, 0, 72000);
        b.pop();
        b.push("chance");
        luckyBerryChanceLightning = b.comment("在食用者位置生成一道闪电")
                .translation(tr("lucky_berry.chance.lightning"))
                .defineInRange("lightning", 15.0, 0.0, 100.0);
        luckyBerryChanceRandomCrops = b.comment("生成 randomCropCount 个随机改良农作物")
                .translation(tr("lucky_berry.chance.random_crops"))
                .defineInRange("random_crops", 25.0, 0.0, 100.0);
        luckyBerryChanceRegeneration = b.comment("生命恢复分支")
                .translation(tr("lucky_berry.chance.regeneration"))
                .defineInRange("regeneration", 25.0, 0.0, 100.0);
        luckyBerryChanceLootChest = b.comment("生成 lootChestCount 个末地城战利品宝箱")
                .translation(tr("lucky_berry.chance.loot_chest"))
                .defineInRange("loot_chest", 25.0, 0.0, 100.0);
        luckyBerryChanceSmallExplosion = b.comment("威力为 smallExplosionPower 的爆炸")
                .translation(tr("lucky_berry.chance.small_explosion"))
                .defineInRange("small_explosion", 5.0, 0.0, 100.0);
        luckyBerryChanceChargedCreeper = b.comment("闪电苦力怕")
                .translation(tr("lucky_berry.chance.charged_creeper"))
                .defineInRange("charged_creeper", 3.0, 0.0, 100.0);
        luckyBerryChanceGodlikeBuff = b.comment("600 秒的神级增益")
                .translation(tr("lucky_berry.chance.godlike_buff"))
                .defineInRange("godlike_buff", 1.9, 0.0, 100.0);
        luckyBerryChanceCreativeMode = b.comment("创造模式 creativeDurationTicks")
                .translation(tr("lucky_berry.chance.creative_mode"))
                .defineInRange("creative_mode", 0.1, 0.0, 100.0);
        b.pop();
        b.pop();

        b.push("power_beet");
        powerBeetNutrition = b.comment("D-29：营养值")
                .translation(tr("power_beet.nutrition"))
                .defineInRange("nutrition", 4, 0, 20);
        powerBeetSaturationModifier = b.comment("D-29：饱和度修正")
                .translation(tr("power_beet.saturationModifier"))
                .defineInRange("saturationModifier", 0.3, 0.0, 10.0);
        b.push("strength");
        powerBeetStrengthLevel = b.comment("力量等级（1 = I 级）")
                .translation(tr("power_beet.strength.level"))
                .defineInRange("level", 2, 1, 255);
        powerBeetStrengthDurationTicks = b.comment("力量时长（0 = 不施加）")
                .translation(tr("power_beet.strength.durationTicks"))
                .defineInRange("durationTicks", 600, 0, 72000);
        b.pop();
        b.push("speed");
        powerBeetSpeedLevel = b.comment("速度等级（1 = I 级）")
                .translation(tr("power_beet.speed.level"))
                .defineInRange("level", 2, 1, 255);
        powerBeetSpeedDurationTicks = b.comment("速度时长（0 = 不施加）")
                .translation(tr("power_beet.speed.durationTicks"))
                .defineInRange("durationTicks", 600, 0, 72000);
        b.pop();
        b.push("haste");
        powerBeetHasteLevel = b.comment("急迫等级（1 = I 级）")
                .translation(tr("power_beet.haste.level"))
                .defineInRange("level", 2, 1, 255);
        powerBeetHasteDurationTicks = b.comment("急迫时长（0 = 不施加）")
                .translation(tr("power_beet.haste.durationTicks"))
                .defineInRange("durationTicks", 600, 0, 72000);
        b.pop();
        b.push("resistance");
        powerBeetResistanceLevel = b.comment("抗性提升等级（1 = I 级）")
                .translation(tr("power_beet.resistance.level"))
                .defineInRange("level", 1, 1, 255);
        powerBeetResistanceDurationTicks = b.comment("抗性提升时长（0 = 不施加）")
                .translation(tr("power_beet.resistance.durationTicks"))
                .defineInRange("durationTicks", 600, 0, 72000);
        b.pop();
        b.pop();

        SPEC = b.build();
    }

    private FarmstormConfig() {}

    private static String tr(String path) {
        return "farmstorm.configuration." + path;
    }

    /**
     * 配置加载 / 重载后重建派生缓存（§4.11）。
     * 只能由 ModConfigEvent.Loading / Reloading 调用：ModConfigSpec 的取值在配置文件加载前会抛异常。
     */
    public static void bake() {
        sliceResistThreshold = explosionSliceResistThreshold.get();
        grenadeResistThreshold = explosionGrenadeResistThreshold.get();
        untamedFallback = teamsUntamedFallback.get();
        luckyPermille = bakeLuckyPermille();
    }

    public static void onLoading(ModConfigEvent.Loading event) {
        bake();
    }

    public static void onReloading(ModConfigEvent.Reloading event) {
        bake();
    }

    /**
     * 配置层百分比（8 项和 = 100）转千分比（和恒 = 1000，§4.12）。
     * 和不等于 100 时拒绝该段并回退默认值：一个整段被拒绝的浆果只会退化成初始手感，而不会让概率表溢出。
     */
    private static int[] bakeLuckyPermille() {
        double[] percent = {
                luckyBerryChanceLightning.get(), luckyBerryChanceRandomCrops.get(),
                luckyBerryChanceRegeneration.get(), luckyBerryChanceLootChest.get(),
                luckyBerryChanceSmallExplosion.get(), luckyBerryChanceChargedCreeper.get(),
                luckyBerryChanceGodlikeBuff.get(), luckyBerryChanceCreativeMode.get()
        };
        double sum = 0.0;
        for (double value : percent) {
            sum += value;
        }
        if (Math.abs(sum - 100.0) > PERCENT_SUM_TOLERANCE) {
            Farmstorm.LOGGER.warn("lucky_berry.chance 之和为 {}（应为 100），该段回退默认值", sum);
            return DEFAULT_PERMILLE.clone();
        }
        int[] permille = new int[LUCKY_OUTCOME_COUNT];
        int total = 0;
        int largestIndex = 0;
        for (int i = 0; i < LUCKY_OUTCOME_COUNT; i++) {
            permille[i] = (int) Math.round(percent[i] * 10.0);
            total += permille[i];
            if (permille[i] > permille[largestIndex]) {
                largestIndex = i;
            }
        }
        // 各自取整后的和可能差 1~2 个千分点；把残差补到权重最大的那一档，
        // 保证抽取一次 nextInt(1000) 就能命中，且不会把某一档压成负数。
        permille[largestIndex] += 1000 - total;
        return permille;
    }

    /** 幸运浆果千分比表（和恒为 1000），索引见 LIGHTNING / SPAWN_CROPS / REGEN / LOOT_CHESTS / SMALL_EXPLOSION / CHARGED_CREEPER / POWER / CREATIVE。 */
    public static int[] luckyBerryPermille() {
        return luckyPermille.clone();
    }

    /** 瓜片子爆炸的抗性阈值快照（C-2 / D-19）。 */
    public static double sliceResistThreshold() {
        return sliceResistThreshold;
    }

    /** 瓜！爆炸的抗性阈值快照（D-19）。 */
    public static double grenadeResistThreshold() {
        return grenadeResistThreshold;
    }

    /** G-8 口径快照，供 ScoreboardTeamResolver 在触发瞬间读取。 */
    public static String untamedFallback() {
        return untamedFallback;
    }

    /**
     * 注册期取值：SERVER 配置在服务器启动时才加载，而物品属性（堆叠数、食物数值）在注册时就固定下来。
     * 因此注册期只能取默认值——服主改 toml 也不会改变已注册物品的堆叠上限，这一点写进交付说明。
     */
    public static int registrationInt(ModConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    /** 同上，浮点版本。 */
    public static double registrationDouble(ModConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
