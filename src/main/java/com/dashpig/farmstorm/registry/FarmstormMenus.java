package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.menu.ConverterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 1 个 MenuType（TECH §3.9）。 */
public final class FarmstormMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER =
            DeferredRegister.create(Registries.MENU, Farmstorm.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ConverterMenu>> CONVERTER =
            REGISTER.register("converter", () -> new MenuType<>(ConverterMenu::new, FeatureFlags.VANILLA_SET));

    private FarmstormMenus() {}
}
