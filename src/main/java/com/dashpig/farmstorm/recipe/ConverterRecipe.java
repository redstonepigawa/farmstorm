package com.dashpig.farmstorm.recipe;

import com.dashpig.farmstorm.registry.FarmstormRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * 农产品转换配方（TECH §4.8.5，JSON schema 见同节）。
 * 单向 1:1、无成本：JSON 只有 {@code input} / {@code result} 两个字段（D-209）。
 *
 * <p><b>1.20.1 的 {@link Recipe} 是 {@code Recipe<Container>}</b>（docs/zh/02-TECH-1.20.1.md §5 第 3 条）：
 * 入参从 1.21.1 的 {@code SingleRecipeInput} 换成 {@link Container}（只读第 0 格），
 * {@code assemble / getResultItem} 多一个 {@link RegistryAccess}，且 {@code getId()} 在 1.20.1 是抽象方法。
 */
public class ConverterRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack result;

    public ConverterRecipe(ResourceLocation id, Ingredient input, ItemStack result) {
        this.id = id;
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
    public boolean matches(Container container, Level level) {
        return this.input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.input);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
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
