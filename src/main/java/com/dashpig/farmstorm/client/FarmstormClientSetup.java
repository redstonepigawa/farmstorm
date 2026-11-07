package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import com.dashpig.farmstorm.registry.FarmstormRecipes;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端 <b>mod 总线</b>注册点。
 *
 * <p>1.20.1 的 {@code @Mod.EventBusSubscriber#bus} 是<b>单值</b>（1.21.1 的 {@code EventBusSubscriber} 默认挂两条总线），
 * 而 {@code FarmstormClient} 同时有 game 总线（渲染 / 逐刻 / 输入）与 mod 总线（注册面）的处理器，
 * 因此把 mod 总线那三条拆到本类，语义与 1.21.1 一致、不重复注册。
 *
 * <p>1.20.1 没有 {@code RegisterMenuScreensEvent}：界面注册改在 {@link FMLClientSetupEvent} 里调
 * {@link MenuScreens#register}（docs/zh/02-TECH-1.20.1.md §3 的第 6 条同类问题）。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FarmstormClientSetup {

    private FarmstormClientSetup() {}

    /** F-227 / D-166：54 格存储界面。漏掉这一行 ⇒ 打开界面时崩或黑屏（36 号 §2.6.3）。 */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(FarmstormMenus.OUTPOST_CORE.get(), OutpostCoreScreen::new));
    }

    /** 自定义配方类型 {@code farmstorm:converting} 的配方书分类（F-2xx）。 */
    @SubscribeEvent
    public static void onRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(FarmstormRecipes.CONVERTING_TYPE.get(), holder -> RecipeBookCategories.UNKNOWN);
    }

    /** 13 个实体类型的渲染器注册（FIX-1），实现在 {@link FarmstormClient}。 */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        FarmstormClient.onRegisterRenderers(event);
    }
}
