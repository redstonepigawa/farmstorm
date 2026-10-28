package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.recipe.ConverterRecipe;
import com.dashpig.farmstorm.registry.FarmstormItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 21 条农产品转换配方（TECH §6.7 / D-11 / D-12）。
 *
 * <p>全部配方共用自定义类型 {@code farmstorm:converting}，字段口径见 {@link ConverterRecipe} 的
 * 7 个分量。一条双向配方同时覆盖「原版 → 改良」与「改良 → 原版」（D-11）；
 * 火把花 / 瓶子草两条单向，且正向额外收 1 个下界之星（D-12）。
 *
 * <p>JSON 由 {@link ConverterRecipe} 实例经 serializer codec 落盘（R-03：不手写数据包 JSON）。
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
