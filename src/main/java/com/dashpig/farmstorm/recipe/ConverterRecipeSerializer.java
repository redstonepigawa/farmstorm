package com.dashpig.farmstorm.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 转换配方的序列化（TECH §4.8.5）。两个字段，JSON 与网络包都只有 {@code input} / {@code result} 两段（D-209）。
 *
 * <p><b>1.20.1 没有 {@code MapCodec} / {@code StreamCodec} 那套序列化接口</b>：
 * 改回 {@link RecipeSerializer} 的三件套 {@code fromJson / fromNetwork / toNetwork}。
 * JSON 形状一字不动（{@code input} 用原版 {@code Ingredient.fromJson}、{@code result} 用 {@code ItemStack} 的原版反序列化）。
 */
public class ConverterRecipeSerializer implements RecipeSerializer<ConverterRecipe> {

    @Override
    public ConverterRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input = Ingredient.fromJson(json.get("input"));
        ItemStack result = net.minecraft.world.item.crafting.ShapedRecipe
                .itemStackFromJson(json.getAsJsonObject("result"));
        return new ConverterRecipe(recipeId, input, result);
    }

    @Override
    public ConverterRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();
        return new ConverterRecipe(recipeId, input, result);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ConverterRecipe recipe) {
        recipe.input().toNetwork(buffer);
        buffer.writeItem(recipe.result());
    }
}
