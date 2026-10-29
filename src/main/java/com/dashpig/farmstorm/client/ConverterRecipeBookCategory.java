package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.registry.FarmstormRecipes;
import net.minecraft.client.RecipeBookCategories;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;

/**
 * V-63②：消除客户端 `Unknown recipe category: farmstorm:converting/<id>` 警告。
 * <p>来源（1.21.1 `ClientRecipeBook#getCategory` L123-136）：vanilla 认不出的 `RecipeType` 会走
 * `net.neoforged.neoforge.client.RecipeBookManager.findCategories(type, holder)`；
 * 没有注册过 finder 就 `LOGGER.warn("Unknown recipe category: {}/{}")` 并回落 `RecipeBookCategories.UNKNOWN`。
 * <p>NeoForge 给出的注册入口是 {@link RegisterRecipeBookCategoriesEvent#registerRecipeCategoryFinder}。
 * <p><b>为什么返回 {@link RecipeBookCategories#UNKNOWN} 而不是 CRAFTING_MISC</b>：
 * 转换配方只能在「转换器」方块里做，任何 vanilla 菜单都放不下它；
 * 挂进 CRAFTING_MISC 会让它们出现在工作台配方书里、却永远摆不进合成格（误导 + 有踩到 `PlaceRecipe` 的风险）。
 * UNKNOWN 与当前的实际表现一致（不进任何 tab），只是不再刷警告。
 */
public final class ConverterRecipeBookCategory {
    private ConverterRecipeBookCategory() {}

    public static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(FarmstormRecipes.CONVERTING_TYPE.get(), holder -> RecipeBookCategories.UNKNOWN);
    }
}
