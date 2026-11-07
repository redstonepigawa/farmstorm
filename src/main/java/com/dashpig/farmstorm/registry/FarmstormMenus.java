package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.menu.OutpostCoreMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/** 1 个 MenuType（TECH §3.9）：v0.2.0 的据点核心存储（D-166）。转换器与培养器**无菜单**（D-160 / D-208）。 */
public final class FarmstormMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER =
            DeferredRegister.create(Registries.MENU, Farmstorm.MOD_ID);

    /** v0.2.0 的第 2 个 MenuType（F-227 / D-166）：54 格存储界面；菜单槽数只在创建时读一次 storageSlots。 */
    public static final RegistryObject<MenuType<OutpostCoreMenu>> OUTPOST_CORE =
            REGISTER.register("outpost_core", () -> new MenuType<>(OutpostCoreMenu::new, FeatureFlags.VANILLA_SET));

    private FarmstormMenus() {}
}
