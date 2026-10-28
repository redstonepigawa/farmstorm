package com.dashpig.farmstorm.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * 转换配方的序列化（TECH §4.8.5）。
 * streamCodec 手写而不是 composite：字段有 7 个，StreamCodec.composite 最多只到 6 个组件。
 */
public class ConverterRecipeSerializer implements RecipeSerializer<ConverterRecipe> {
    public static final MapCodec<ConverterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(ConverterRecipe::input),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ConverterRecipe::result),
            Codec.INT.optionalFieldOf("xp_cost", 3).forGetter(ConverterRecipe::xpCost),
            Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.EMPTY).forGetter(ConverterRecipe::catalyst),
            Codec.INT.optionalFieldOf("catalyst_count", 1).forGetter(ConverterRecipe::catalystCount),
            Codec.BOOL.optionalFieldOf("bidirectional", true).forGetter(ConverterRecipe::bidirectional),
            Codec.INT.optionalFieldOf("reverse_xp_cost", 0).forGetter(ConverterRecipe::reverseXpCost)
    ).apply(instance, ConverterRecipe::new));

    @Override
    public MapCodec<ConverterRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ConverterRecipe> streamCodec() {
        return StreamCodec.of(
                (buffer, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input());
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
                    buffer.writeVarInt(recipe.xpCost());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.catalyst());
                    buffer.writeVarInt(recipe.catalystCount());
                    buffer.writeBoolean(recipe.bidirectional());
                    buffer.writeVarInt(recipe.reverseXpCost());
                },
                buffer -> new ConverterRecipe(
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                        ItemStack.STREAM_CODEC.decode(buffer),
                        buffer.readVarInt(),
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                        buffer.readVarInt(),
                        buffer.readBoolean(),
                        buffer.readVarInt()));
    }
}
