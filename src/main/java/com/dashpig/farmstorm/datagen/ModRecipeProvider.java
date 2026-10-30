package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.recipe.ConverterRecipe;
import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.util.FarmstormTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 26 条农产品转换配方（TECH §6.7 / D-11 / D-12；F-73 补 3 条，D-54）+ 1 条转换器合成配方（D-52 / F-71）
 * + 1 条压缩膨胀麦无序合成配方（D-67 / F-72）。
 *
 * <p>转换配方全部共用自定义类型 {@code farmstorm:converting}，字段口径见 {@link ConverterRecipe} 的
 * 7 个分量。一条双向配方同时覆盖「原版 → 改良」与「改良 → 原版」（D-11）；
 * 火把花 / 瓶子草两条单向，且正向额外收 1 个下界之星（D-12）。
 *
 * <p>合成配方（F-71）属于 {@code minecraft:crafting_shaped}，与上面 21 条互不相干：
 * 合成不扣经验、不收下界之星，只是给转换器一条获得途径（DESIGN §2.6 #4/#5）。
 *
 * <p>JSON 由 serializer codec 落盘（R-03：不手写数据包 JSON）。
 * codec 对「等于默认值」的可选字段会省略（R-23 同类现象）：{@code xp_cost} 的默认值就是 3、
 * {@code catalyst_count} 默认 1、非单向配方的 {@code bidirectional} 默认 true，故这三处不会出现在
 * JSON 里，语义与显式书写完全等价。
 */
public final class ModRecipeProvider extends RecipeProvider {
    /** D-12：每次转换扣除的经验点（与 DESIGN §3.1.4 的伪代码一致，正反向同价）。 */
    private static final int XP_COST = 3;

    /** D-12：火把花 / 瓶子草正向转换额外消耗的下界之星个数。 */
    private static final int NETHER_STAR_COST = 1;

    /** 无催化剂配方的 {@code catalyst_count}：填 codec 默认值 1，从而不落进 JSON。 */
    private static final int NO_CATALYST = 1;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 转换器没有配方就拿不到，21 条转换配方也无从用起（D-52：本条是首条合成配方）
        converter(recipeOutput);
        // 压缩膨胀麦是二级合成物，原料就是膨胀麦（D-67）：先给出获得途径，转换配方表照旧不动
        compressedPuffedWheat(recipeOutput);
        // 1-8：作物本体双向
        twoWay(recipeOutput, "puffed_wheat", Items.WHEAT, FarmstormItems.PUFFED_WHEAT);
        twoWay(recipeOutput, "blast_carrot", Items.CARROT, FarmstormItems.BLAST_CARROT);
        twoWay(recipeOutput, "ubi_server", Items.POTATO, FarmstormItems.UBI_SERVER);
        twoWay(recipeOutput, "toxic_ubi_server", Items.POISONOUS_POTATO, FarmstormItems.TOXIC_UBI_SERVER);
        twoWay(recipeOutput, "power_beet", Items.BEETROOT, FarmstormItems.POWER_BEET);
        twoWay(recipeOutput, "melon_slice_bomb", Items.MELON_SLICE, FarmstormItems.MELON_SLICE_BOMB);
        twoWay(recipeOutput, "melon_grenade", Items.MELON, FarmstormItems.MELON_GRENADE);
        twoWay(recipeOutput, "pumpkin_turret", Items.PUMPKIN, FarmstormItems.PUMPKIN_TURRET);
        // 9-10：下界之星入场费，只允许正向（D-12 / D-14）
        oneWayWithNetherStar(recipeOutput, "awakened_torchflower", Items.TORCHFLOWER, FarmstormItems.AWAKENED_TORCHFLOWER);
        oneWayWithNetherStar(recipeOutput, "awakened_pitcher_plant", Items.PITCHER_PLANT,
                FarmstormItems.AWAKENED_PITCHER_PLANT);
        // 11-15：其余作物本体双向
        twoWay(recipeOutput, "fang_trap_cactus", Items.CACTUS, FarmstormItems.FANG_TRAP_CACTUS);
        twoWay(recipeOutput, "bamboo_firecracker", Items.BAMBOO, FarmstormItems.BAMBOO_FIRECRACKER);
        twoWay(recipeOutput, "red_mushroom_puff", Items.RED_MUSHROOM, FarmstormItems.RED_MUSHROOM_PUFF);
        twoWay(recipeOutput, "brown_mushroom_puff", Items.BROWN_MUSHROOM, FarmstormItems.BROWN_MUSHROOM_PUFF);
        twoWay(recipeOutput, "lucky_berry", Items.SWEET_BERRIES, FarmstormItems.LUCKY_BERRY);
        // 16-21：6 个独立播种物双向
        twoWay(recipeOutput, "puffed_wheat_seeds", Items.WHEAT_SEEDS, FarmstormItems.PUFFED_WHEAT_SEEDS);
        twoWay(recipeOutput, "power_beet_seeds", Items.BEETROOT_SEEDS, FarmstormItems.POWER_BEET_SEEDS);
        twoWay(recipeOutput, "melon_grenade_seeds", Items.MELON_SEEDS, FarmstormItems.MELON_GRENADE_SEEDS);
        twoWay(recipeOutput, "pumpkin_turret_seeds", Items.PUMPKIN_SEEDS, FarmstormItems.PUMPKIN_TURRET_SEEDS);
        twoWay(recipeOutput, "awakened_torchflower_seeds", Items.TORCHFLOWER_SEEDS,
                FarmstormItems.AWAKENED_TORCHFLOWER_SEEDS);
        twoWay(recipeOutput, "awakened_pitcher_plant_pod", Items.PITCHER_POD, FarmstormItems.AWAKENED_PITCHER_PLANT_POD);
        // 22-24：F-73 新增三条作物本体双向（D-54），与上面同形：无催化剂、正反向同价
        twoWay(recipeOutput, "molotov", Items.CRIMSON_FUNGUS, FarmstormItems.MOLOTOV);
        twoWay(recipeOutput, "zangief_cane", Items.SUGAR_CANE, FarmstormItems.ZANGIEF_CANE);
        twoWay(recipeOutput, "wild_apple", Items.GOLDEN_APPLE, FarmstormItems.WILD_APPLE);
        // 25-26：F-132 新增两条部署物双向（D-86 诡异菌 / D-90 可豆）；朋友的韭没有转换配方，只进 converter_fuel 标签（D-84）
        twoWay(recipeOutput, "point_defense_fungus", Items.WARPED_FUNGUS, FarmstormItems.POINT_DEFENSE_FUNGUS);
        twoWay(recipeOutput, "vibration_bean", Items.COCOA_BEANS, FarmstormItems.VIBRATION_BEAN);
    }

    /**
     * D-52 / DESIGN §2.4：有序 3×3，中心 {@code minecraft:crafting_table}，外围八格各 1 个
     * {@code #farmstorm:converter_fuel}，产出 1 个转换器。
     *
     * <p>八格写同一个 tag key（{@link FarmstormTags#CONVERTER_FUEL}）而不是八条物品清单：
     * 「什么算农作物」只维护在标签里（D-52 设计意图）。逐格独立判定、可混搭（DESIGN §2.6 #2）。
     *
     * <p>分类取 {@code DECORATIONS}：原版的工作站方块（工作台 / 熔炉 / 锻造台）都在这一类。
     */
    private static void converter(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FarmstormItems.CONVERTER.get())
                .define('T', Items.CRAFTING_TABLE)
                .define('F', FarmstormTags.CONVERTER_FUEL)
                .pattern("FFF")
                .pattern("FTF")
                .pattern("FFF")
                .unlockedBy("has_converter_fuel", has(FarmstormTags.CONVERTER_FUEL))
                .save(recipeWithoutAdvancement(recipeOutput), Farmstorm.id("converter"));
    }

    /**
     * D-67 / DESIGN §2.4：无序合成，任意位置 4 个 {@code farmstorm:puffed_wheat} → 1 个
     * {@code farmstorm:compressed_puffed_wheat}。
     *
     * <p>四份同物品原料逐个 {@code requires}，落盘就是 4 组同一物品的 {@code ingredients}；
     * 判定按「恰好 4 个」走，同一个格子堆 4 个或放 3 / 5 个都不匹配。
     *
     * <p>同样剥掉配方解锁进度（{@link #recipeWithoutAdvancement}）：本 Provider 只产配方 JSON。
     */
    private static void compressedPuffedWheat(RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, FarmstormItems.COMPRESSED_PUFFED_WHEAT.get())
                .requires(FarmstormItems.PUFFED_WHEAT.get(), 4)
                .unlockedBy("has_puffed_wheat", has(FarmstormItems.PUFFED_WHEAT.get()))
                .save(recipeWithoutAdvancement(recipeOutput), Farmstorm.id("compressed_puffed_wheat"));
    }

    /**
     * 只放配方、丢掉配方解锁进度。
     *
     * <p>{@code ShapedRecipeBuilder#save} 会顺带写一份 {@code advancement/recipes/<分类>/<id>.json}；
     * 而 F-71 的产物清单只要两个文件（BRIEF §7.2 #25 / #35），既有 21 条转换配方也都传 {@code null}
     * 进度，故这里统一剥掉，保持「本 Provider 只产配方 JSON」这一口径。
     */
    private static RecipeOutput recipeWithoutAdvancement(RecipeOutput recipeOutput) {
        return new RecipeOutput() {
            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement) {
                recipeOutput.accept(id, recipe, null);
            }

            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
                recipeOutput.accept(id, recipe, null, conditions);
            }

            @Override
            public Advancement.Builder advancement() {
                return recipeOutput.advancement();
            }
        };
    }

    /** 双向 1:1 配方：一条 JSON 覆盖两个方向，无催化剂（D-11）。 */
    private static void twoWay(RecipeOutput recipeOutput, String path, Item vanilla, DeferredItem<? extends Item> improved) {
        recipeOutput.accept(Farmstorm.id(path), new ConverterRecipe(
                Ingredient.of(vanilla),
                new ItemStack(improved.get()),
                XP_COST,
                Ingredient.EMPTY,
                NO_CATALYST,
                true,
                XP_COST), null);
    }

    /** 单向配方 + 下界之星催化剂（D-12：只在「原版 → 激活版」方向收）。 */
    private static void oneWayWithNetherStar(RecipeOutput recipeOutput, String path, Item vanilla,
                                             DeferredItem<? extends Item> improved) {
        recipeOutput.accept(Farmstorm.id(path), new ConverterRecipe(
                Ingredient.of(vanilla),
                new ItemStack(improved.get()),
                XP_COST,
                Ingredient.of(Items.NETHER_STAR),
                NETHER_STAR_COST,
                false,
                XP_COST), null);
    }
}
