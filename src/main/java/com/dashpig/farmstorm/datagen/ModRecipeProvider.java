package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.recipe.ConverterRecipe;
import com.dashpig.farmstorm.registry.FarmstormItems;
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
 * 21 条农产品转换配方（TECH §6.7 / 54 号 §4.2.3；F-73 补 3 条，D-54；D-217 补平凡韭菜一条）
 * + 1 条转换器合成配方（D-206 / F-71）
 * + 1 条压缩膨胀麦无序合成配方（D-67 / F-72）。
 *
 * <p>转换配方全部共用自定义类型 {@code farmstorm:converting}，字段口径见 {@link ConverterRecipe} 的
 * 两个分量（{@code input} / {@code result}）：单向 1:1、不收经验、不收催化剂（D-209）。
 *
 * <p>合成配方（F-71）属于 {@code minecraft:crafting_shaped}，与上面 21 条互不相干：
 * 合成只是给转换器一条获得途径（DESIGN §2.6 #4/#5）。
 *
 * <p>F-168 再加 1 条 3 × 3 合成配方：据点核心（D-122 第 3 条）。
 *
 * <p>F-301 补上培养器的 3 × 3 合成配方（D-220）：这结束了 0.2.0 起「培养器只能靠创造栏 / {@code /give}」的状态（A-64）。
 *
 * <p>JSON 由 serializer codec 落盘（R-03：不手写数据包 JSON），落盘只剩 {@code input} / {@code result}
 * 两个字段。
 */
public final class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 转换器没有配方就拿不到，21 条转换配方也无从用起（D-52：本条是首条合成配方）
        converter(recipeOutput);
        // 培养器同理，0.2.2 之前只能创造栏 /give，补配方后第一次有生存途径（D-220 / A-64）
        cultivator(recipeOutput);
        // 压缩膨胀麦是二级合成物，原料就是膨胀麦（D-67）：先给出获得途径，转换配方表照旧不动
        compressedPuffedWheat(recipeOutput);
        // 据点核心是工作台 3×3 配方（D-122 第 3 条）：没有获得途径就没有核心，第 2 个上限也就无从生效
        outpostCore(recipeOutput);
        // 1-8：作物本体（单向：原版 → 改良）
        oneWay(recipeOutput, "puffed_wheat", Items.WHEAT, FarmstormItems.PUFFED_WHEAT);
        oneWay(recipeOutput, "blast_carrot", Items.CARROT, FarmstormItems.BLAST_CARROT);
        oneWay(recipeOutput, "ubi_server", Items.POTATO, FarmstormItems.UBI_SERVER);
        oneWay(recipeOutput, "toxic_ubi_server", Items.POISONOUS_POTATO, FarmstormItems.TOXIC_UBI_SERVER);
        oneWay(recipeOutput, "power_beet", Items.BEETROOT, FarmstormItems.POWER_BEET);
        oneWay(recipeOutput, "melon_slice_bomb", Items.MELON_SLICE, FarmstormItems.MELON_SLICE_BOMB);
        oneWay(recipeOutput, "melon_grenade", Items.MELON, FarmstormItems.MELON_GRENADE);
        oneWay(recipeOutput, "pumpkin_turret", Items.PUMPKIN, FarmstormItems.PUMPKIN_TURRET);
        // 9-10：史诗档，只允许正向（D-209：不再收下界之星）
        oneWay(recipeOutput, "awakened_torchflower", Items.TORCHFLOWER, FarmstormItems.AWAKENED_TORCHFLOWER);
        oneWay(recipeOutput, "awakened_pitcher_plant", Items.PITCHER_PLANT,
                FarmstormItems.AWAKENED_PITCHER_PLANT);
        // 11-15：其余作物本体
        oneWay(recipeOutput, "fang_trap_cactus", Items.CACTUS, FarmstormItems.FANG_TRAP_CACTUS);
        oneWay(recipeOutput, "bamboo_firecracker", Items.BAMBOO, FarmstormItems.BAMBOO_FIRECRACKER);
        oneWay(recipeOutput, "red_mushroom_puff", Items.RED_MUSHROOM, FarmstormItems.RED_MUSHROOM_PUFF);
        oneWay(recipeOutput, "brown_mushroom_puff", Items.BROWN_MUSHROOM, FarmstormItems.BROWN_MUSHROOM_PUFF);
        oneWay(recipeOutput, "lucky_berry", Items.SWEET_BERRIES, FarmstormItems.LUCKY_BERRY);
        // 16-18：F-73 新增三条作物本体（D-54），与上面同形：单向、无成本
        oneWay(recipeOutput, "molotov", Items.CRIMSON_FUNGUS, FarmstormItems.MOLOTOV);
        oneWay(recipeOutput, "zangief_cane", Items.SUGAR_CANE, FarmstormItems.ZANGIEF_CANE);
        oneWay(recipeOutput, "wild_apple", Items.GOLDEN_APPLE, FarmstormItems.WILD_APPLE);
        // 19-20：F-132 新增两条部署物（D-86 诡异菌 / D-90 可豆）
        oneWay(recipeOutput, "point_defense_fungus", Items.WARPED_FUNGUS, FarmstormItems.POINT_DEFENSE_FUNGUS);
        oneWay(recipeOutput, "vibration_bean", Items.COCOA_BEANS, FarmstormItems.VIBRATION_BEAN);
        // 21：D-217 补上「朋友的韭」唯一缺失的转换配方（平凡韭菜 → 朋友的韭，不改植物学家交易清单）
        oneWay(recipeOutput, "friend_chives", FarmstormItems.PLAIN_CHIVES.get(), FarmstormItems.FRIEND_CHIVES);
    }

    /**
     * D-206 / 54 号 §4.2.2：有序 3×3，六格 {@code minecraft:iron_ingot}、两格 {@code minecraft:glass}、
     * 正中一格 {@code minecraft:redstone}，产出 1 个转换器。
     *
     * <p>材料写死原版物品（不再用 {@code #farmstorm:converter_fuel} 标签）：本版转换器是机器，
     * 不是「拿农作物换机器」的入口（D-206 / D-209）。配方 id 随方块改名 farmstorm:crop_converter。
     *
     * <p>分类取 {@code DECORATIONS}：原版的工作站方块（工作台 / 熔炉 / 锻造台）都在这一类。
     */
    private static void converter(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FarmstormItems.CROP_CONVERTER.get())
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS)
                .define('R', Items.REDSTONE)
                .pattern("III")
                .pattern("GRG")
                .pattern("III")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeWithoutAdvancement(recipeOutput), Farmstorm.id("crop_converter"));
    }

    /**
     * D-220 / 54 号 §0.3 第 13 项：有序 3×3，四角 {@code minecraft:iron_ingot}、四边 {@code minecraft:glass}、
     * 正中一格 {@code minecraft:redstone}，产出 1 个培养器 —— 即 4 铁锭 + 4 玻璃 + 1 红石粉。
     *
     * <p>与 {@link #converter} 只差排布（那台是六铁两玻璃）：这是 plan 的原文，照抄，不自行「优化」以提高辨识度（D-220 补充）。
     * 解锁条件用**原版**物品 {@code minecraft:glass}，不用 {@code #farmstorm:} 前缀的本模组标签 ——
     * 否则配方在首次进入世界时不可见（D-220 补充）。
     *
     * <p>同样剥掉配方解锁进度（{@link #recipeWithoutAdvancement}）：本 Provider 只产配方 JSON。
     */
    private static void cultivator(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FarmstormItems.CULTIVATOR.get())
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS)
                .define('R', Items.REDSTONE)
                .pattern("IGI")
                .pattern("GRG")
                .pattern("IGI")
                .unlockedBy("has_glass", has(Items.GLASS))
                .save(recipeWithoutAdvancement(recipeOutput), Farmstorm.id("cultivator"));
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
     * D-122 第 3 条 / F-168：有序 3 × 3，四角 {@code minecraft:stone}、正中 {@code minecraft:diamond}、
     * 其余四格 {@code minecraft:redstone}，产出 1 个据点核心。
     *
     * <p>分类取 {@code DECORATIONS}（与转换器同形）：落进 JSON 的 {@code "category"} 不是这里的
     * {@link RecipeCategory}，而是 {@code determineBookCategory} 折算出的 {@code CraftingBookCategory}
     * （{@code DECORATIONS} ⇒ {@code misc}）。
     *
     * <p>同样剥掉配方解锁进度（{@link #recipeWithoutAdvancement}）：本 Provider 只产配方 JSON。
     */
    private static void outpostCore(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FarmstormItems.OUTPOST_CORE.get())
                .define('S', Items.STONE)
                .define('R', Items.REDSTONE)
                .define('D', Items.DIAMOND)
                .pattern("SRS")
                .pattern("RDR")
                .pattern("SRS")
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeWithoutAdvancement(recipeOutput), Farmstorm.id("outpost_core"));
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

    /** 单向 1:1 配方：只留「原版 / 平凡 → 改良」一个方向，不收经验、不收催化剂（D-209）。 */
    private static void oneWay(RecipeOutput recipeOutput, String path, Item vanilla, DeferredItem<? extends Item> improved) {
        recipeOutput.accept(Farmstorm.id(path), new ConverterRecipe(
                Ingredient.of(vanilla),
                new ItemStack(improved.get())), null);
    }
}
