package com.dashpig.farmstorm.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 转换配方的序列化（TECH §4.8.5）。
 * 两个字段，MapCodec 与 streamCodec 都只有 {@code input} / {@code result} 两段（D-209）。
 */
public class ConverterRecipeSerializer implements RecipeSerializer<ConverterRecipe> {
    public static final MapCodec<ConverterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(ConverterRecipe::input),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ConverterRecipe::result)
    ).apply(instance, ConverterRecipe::new));

    @Override
    public MapCodec<ConverterRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ConverterRecipe> streamCodec() {
        return StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, ConverterRecipe::input,
                ItemStack.STREAM_CODEC, ConverterRecipe::result,
                ConverterRecipe::new);
    }
}
