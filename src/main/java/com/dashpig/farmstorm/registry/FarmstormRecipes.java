package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.recipe.ConverterRecipe;
import com.dashpig.farmstorm.recipe.ConverterRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * 1 个 RecipeType + 1 个 RecipeSerializer（TECH §3.5）。
 * 两者分属不同的 registry，因此必须是两个 DeferredRegister——主类里也要分别 register 一次。
 */
public final class FarmstormRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPE_REGISTER =
            DeferredRegister.create(Registries.RECIPE_TYPE, Farmstorm.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Farmstorm.MOD_ID);

    public static final RegistryObject<RecipeType<ConverterRecipe>> CONVERTING_TYPE =
            TYPE_REGISTER.register("converting", () -> RecipeType.simple(Farmstorm.id("converting")));
    public static final RegistryObject<ConverterRecipeSerializer> CONVERTING_SERIALIZER =
            SERIALIZER_REGISTER.register("converting", ConverterRecipeSerializer::new);

    private FarmstormRecipes() {}
}
