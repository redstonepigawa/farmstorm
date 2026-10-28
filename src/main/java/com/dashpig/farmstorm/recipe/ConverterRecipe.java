package com.dashpig.farmstorm.recipe;

import com.dashpig.farmstorm.registry.FarmstormRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * 农产品转换配方（TECH §4.8.5，JSON schema 见同节）。
 * 双向可逆、1:1；火把花 / 瓶子草只允许正向并额外消耗下界之星（D-12）。
 */
public class ConverterRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack result;
    private final int xpCost;
    private final Ingredient catalyst;
    private final int catalystCount;
    private final boolean bidirectional;
    private final int reverseXpCost;

    public ConverterRecipe(Ingredient input, ItemStack result, int xpCost, Ingredient catalyst, int catalystCount,
                          boolean bidirectional, int reverseXpCost) {
        this.input = input;
        this.result = result;
        this.xpCost = xpCost;
        this.catalyst = catalyst;
        this.catalystCount = catalystCount;
        this.bidirectional = bidirectional;
        this.reverseXpCost = reverseXpCost;
    }

    public Ingredient input() {
        return this.input;
    }

    public ItemStack result() {
        return this.result;
    }

    public int xpCost() {
        return this.xpCost;
    }

    public Ingredient catalyst() {
        return this.catalyst;
    }

    public int catalystCount() {
        return this.catalystCount;
    }

    public boolean bidirectional() {
        return this.bidirectional;
    }

    public int reverseXpCost() {
        return this.reverseXpCost;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.input);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FarmstormRecipes.CONVERTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FarmstormRecipes.CONVERTING_TYPE.get();
    }

    /**
     * 反向视图：input / result 互换的运行时快照，不落盘（TECH §4.8.5），供转换器做「改良 → 原版」匹配。
     * 成本随之互换（反向用 {@code reverse_xp_cost}）；催化剂声明保留，是否真的收费由
     * {@code converter.netherStarOnReverse} 在 ConverterBlockEntity 里裁决（D-12：反向默认免费）。
     */
    public ConverterRecipe reverse() {
        ItemStack[] sourceItems = this.input.getItems();
        ItemStack mirroredResult = sourceItems.length == 0 ? ItemStack.EMPTY : sourceItems[0].copyWithCount(1);
        return new ConverterRecipe(Ingredient.of(this.result.copyWithCount(1)), mirroredResult, this.reverseXpCost,
                this.catalyst, this.catalystCount, this.bidirectional, this.xpCost);
    }
}
