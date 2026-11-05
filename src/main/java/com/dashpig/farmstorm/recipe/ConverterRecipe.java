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
 * 单向 1:1、无成本：JSON 只有 {@code input} / {@code result} 两个字段（D-209）。
 */
public class ConverterRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack result;

    public ConverterRecipe(Ingredient input, ItemStack result) {
        this.input = input;
        this.result = result;
    }

    public Ingredient input() {
        return this.input;
    }

    public ItemStack result() {
        return this.result;
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
}
