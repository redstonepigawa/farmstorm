package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.util.FarmstormTags;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/**
 * 12 条进度（DEC C-7 / DESIGN §6 / TECH §6.7）。
 *
 * <p>全部 {@code frame=task}、{@code show_toast=true}、{@code announce_to_chat=false}；
 * 根进度背景用 {@code minecraft:textures/block/hay_block_side.png}，{@code lucky_jackpot} 隐藏。
 *
 * <p>触发器分两类：能用原版 criteria 表达的直接写（{@code inventory_changed} / {@code placed_block}），
 * 其余一律 {@code minecraft:impossible}，由服务端 {@code award} 授予——判定仍在服务端（DESIGN §6 约定），
 * 不新增指令（D-24）。{@link #AWARD_REQUIRED} 是后者的清单，M6 需要在这些判定点补
 * {@code ServerPlayer#getAdvancements().award(...)}。
 */
public final class ModAdvancementProvider {
    /**
     * 需要服务端 {@code award} 的进度（{@code minecraft:impossible} 触发器）。
     * 原版 criteria 无法表达这些判定，判定点全在服务端；清单的作用是让「进度」与「授予点」保持同步
     * ——改这里就必须同时改 {@code util/FarmstormAdvancements} 的调用点。v0.0.2 收口时 11 条已全部接线，v0.0.3 收到 14 条（+3）。
     */
    public static final List<String> AWARD_REQUIRED = List.of(
            "farmstorm:first_conversion",
            "farmstorm:nether_star_toll",
            "farmstorm:first_throw",
            "farmstorm:demolition",
            "farmstorm:turret_hit",
            "farmstorm:aura_bath",
            "farmstorm:fang_dance",
            "farmstorm:lucky_jackpot",
            // v0.0.2 的三条（D-63 / DESIGN §9.1）
            "farmstorm:first_molotov",
            "farmstorm:zangief_hit",
            "farmstorm:first_wild_hunter",
            // v0.0.3 的三条（D-63 的扩充 / DESIGN §10.1）
            "farmstorm:first_q_bounce",
            "farmstorm:point_defense_intercept",
            "farmstorm:vibration_bean_trigger");

    /** 根进度的背景贴图（DESIGN §6）。 */
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/block/hay_block_side.png");

    private ModAdvancementProvider() {}

    /** 供 {@code FarmstormDataGen} 接线的入口。 */
    public static AdvancementProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        return new AdvancementProvider(output, lookupProvider, List.of(ModAdvancementProvider::generate));
    }

    private static void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        // 1. 根：获得任意一件改良作物（#farmstorm:modified_crops）
        AdvancementHolder root = advancement("root", null, FarmstormItems.PUFFED_WHEAT.get(), BACKGROUND, false)
                .addCriterion("has_modified_crop",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(FarmstormTags.MODIFIED_CROPS)))
                .save(output, "farmstorm:root");

        // 2. 放置一台农产品转换器
        AdvancementHolder converter = advancement("converter", root, FarmstormItems.CROP_CONVERTER.get(), null, false)
                .addCriterion("placed_converter",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(FarmstormBlocks.CROP_CONVERTER.get()))
                .save(output, "farmstorm:converter");

        // 3. 在转换器内完成一次转换（任意方向）——没有原版触发器，服务端 award
        AdvancementHolder firstConversion = impossible("first_conversion", converter, FarmstormItems.POWER_BEET.get(), false)
                .save(output, "farmstorm:first_conversion");

        // 4. 完成一次「原版 → 激活版」的火把花 / 瓶子草转换（消耗下界之星）
        impossible("nether_star_toll", firstConversion, FarmstormItems.AWAKENED_TORCHFLOWER.get(), false)
                .save(output, "farmstorm:nether_star_toll");

        // 5. 收获一次成熟改良作物，并同时拾取作物与播种物（6 组独立播种物，任一组成立即可）
        advancement("seed_cycle", firstConversion, FarmstormItems.PUFFED_WHEAT_SEEDS.get(), null, false)
                .addCriterion("puffed_wheat", bothItems(FarmstormItems.PUFFED_WHEAT.get(), FarmstormItems.PUFFED_WHEAT_SEEDS.get()))
                .addCriterion("power_beet", bothItems(FarmstormItems.POWER_BEET.get(), FarmstormItems.POWER_BEET_SEEDS.get()))
                .addCriterion("melon_grenade", bothItems(FarmstormItems.MELON_GRENADE.get(), FarmstormItems.MELON_GRENADE_SEEDS.get()))
                .addCriterion("pumpkin_turret", bothItems(FarmstormItems.PUMPKIN_TURRET.get(), FarmstormItems.PUMPKIN_TURRET_SEEDS.get()))
                .addCriterion("awakened_torchflower",
                        bothItems(FarmstormItems.AWAKENED_TORCHFLOWER.get(), FarmstormItems.AWAKENED_TORCHFLOWER_SEEDS.get()))
                .addCriterion("awakened_pitcher_plant",
                        bothItems(FarmstormItems.AWAKENED_PITCHER_PLANT.get(), FarmstormItems.AWAKENED_PITCHER_PLANT_POD.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(output, "farmstorm:seed_cycle");

        // 6. 投掷任意一件投掷型改良作物——没有原版触发器，服务端 award
        AdvancementHolder firstThrow = impossible("first_throw", root, FarmstormItems.UBI_SERVER.get(), false)
                .save(output, "farmstorm:first_throw");

        // 7. 部署任意一件部署型改良作物（5 个部署方块，任一放置即可）
        AdvancementHolder firstDeploy = advancement("first_deploy", root, FarmstormItems.BLAST_CARROT.get(), null, false)
                .addCriterion("placed_blast_carrot",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(FarmstormBlocks.BLAST_CARROT.get()))
                .addCriterion("placed_pumpkin_turret",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(FarmstormBlocks.PUMPKIN_TURRET.get()))
                .addCriterion("placed_awakened_torchflower",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(FarmstormBlocks.AWAKENED_TORCHFLOWER.get()))
                .addCriterion("placed_awakened_pitcher_plant",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(FarmstormBlocks.AWAKENED_PITCHER_PLANT.get()))
                .addCriterion("placed_fang_trap_cactus",
                        ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(FarmstormBlocks.FANG_TRAP_CACTUS.get()))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(output, "farmstorm:first_deploy");

        // 8. 用破障类作物的爆炸破坏至少 1 个方块
        impossible("demolition", firstThrow, FarmstormItems.BAMBOO_FIRECRACKER.get(), false)
                .save(output, "farmstorm:demolition");

        // 9. 被 man瓜 的导弹命中一次
        impossible("turret_hit", firstDeploy, FarmstormItems.PUMPKIN_TURRET.get(), false)
                .save(output, "farmstorm:turret_hit");

        // 10. 从激活的火把花 / 瓶子草获得至少一次光环效果
        impossible("aura_bath", firstDeploy, FarmstormItems.AWAKENED_PITCHER_PLANT.get(), false)
                .save(output, "farmstorm:aura_bath");

        // 11. 触发一次「赢过输过没死过」
        impossible("fang_dance", firstDeploy, FarmstormItems.FANG_TRAP_CACTUS.get(), false)
                .save(output, "farmstorm:fang_dance");

        // 12. 食用幸运浆果并抽中 0.1% 的创造模式分支（分支判定在 LuckyBerryRoller，服务端 award）
        impossible("lucky_jackpot", root, FarmstormItems.LUCKY_BERRY.get(), true)
                .save(output, "farmstorm:lucky_jackpot");

        // ---- v0.0.2 的三条（D-63 / DESIGN §9.1：父进度、图标、口径逐条照该表）----
        // 13. 初次投出莫洛托夫（投掷物实体生成即成立，不要求命中）
        impossible("first_molotov", firstThrow, FarmstormItems.MOLOTOV.get(), false)
                .save(output, "farmstorm:first_molotov");

        // 14. 桑吉尔蔗首次命中：某名玩家被施加 amplifier 0 的 farmstorm:zangief_piledriver
        impossible("zangief_hit", firstDeploy, FarmstormItems.ZANGIEF_CANE.get(), false)
                .save(output, "farmstorm:zangief_hit");

        // 15. 玩家首次获得 farmstorm:wild_hunter 效果（不限定来源）
        impossible("first_wild_hunter", root, FarmstormItems.WILD_APPLE.get(), false)
                .save(output, "farmstorm:first_wild_hunter");

        // ---- v0.0.3 的三条（D-63 的扩充 / DESIGN §10.1：父进度、图标、口径逐条照该表）----
        // 16. 玩家首次获得 farmstorm:q_bounce（不限定来源，§10.2 第 1 条）
        impossible("first_q_bounce", root, FarmstormItems.FRIEND_CHIVES.get(), false)
                .save(output, "farmstorm:first_q_bounce");

        // 17. 近防菌首次成功拦截一发非己方投掷物（第 1 次扣减生效即成立，§10.1 #17）
        impossible("point_defense_intercept", firstDeploy, FarmstormItems.POINT_DEFENSE_FUNGUS.get(), false)
                .save(output, "farmstorm:point_defense_intercept");

        // 18. 震动豆首次被玩家踩中并完成结算（施混乱 + 静默移除都执行完，§10.1 #18）
        impossible("vibration_bean_trigger", firstDeploy, FarmstormItems.VIBRATION_BEAN.get(), false)
                .save(output, "farmstorm:vibration_bean_trigger");
    }

    /** 单个 {@code minecraft:impossible} 判定的进度：只有服务端 award 能授予。 */
    private static Advancement.Builder impossible(String path, @Nullable AdvancementHolder parent, ItemLike icon,
                                                  boolean hidden) {
        return advancement(path, parent, icon, null, hidden)
                .addCriterion(path, CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                .requirements(AdvancementRequirements.Strategy.OR);
    }

    /**
     * 进度骨架：父进度按已生成的 {@link AdvancementHolder} 串联（父进度的 ResourceLocation 重载已废弃），
     * 标题 / 描述走 {@code advancements.farmstorm.<path>.title} / {@code .description}（TECH §7.1）。
     *
     * @param background 只有根进度需要，其余传 null
     */
    private static Advancement.Builder advancement(String path, @Nullable AdvancementHolder parent, ItemLike icon,
                                                  @Nullable ResourceLocation background, boolean hidden) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        if (parent != null) {
            builder.parent(parent);
        }
        return builder.display(icon,
                Component.translatable("advancements.farmstorm." + path + ".title"),
                Component.translatable("advancements.farmstorm." + path + ".description"),
                background, AdvancementType.TASK, true, false, hidden);
    }

    /** 「背包里同时有两种物品」判定，用于播种闭环。 */
    private static Criterion<InventoryChangeTrigger.TriggerInstance> bothItems(ItemLike crop, ItemLike seed) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(crop), ItemPredicate.Builder.item().of(seed));
    }
}
