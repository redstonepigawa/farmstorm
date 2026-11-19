package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.menu.ControlTerminalMenu;
import com.dashpig.farmstorm.menu.OutpostCoreMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 2 个 MenuType（TECH §3.9）：v0.2.0 的据点核心存储（D-166）与 v0.3.0 的控制面板（D-273）。
 * 转换器与培养器**无菜单**（D-160 / D-208）。
 */
public final class FarmstormMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER =
            DeferredRegister.create(Registries.MENU, Farmstorm.MOD_ID);

    /** v0.2.0 的第 2 个 MenuType（F-227 / D-166）：54 格存储界面；菜单槽数只在创建时读一次 storageSlots。 */
    public static final DeferredHolder<MenuType<?>, MenuType<OutpostCoreMenu>> OUTPOST_CORE =
            REGISTER.register("outpost_core", () -> new MenuType<>(OutpostCoreMenu::new, FeatureFlags.VANILLA_SET));

    /** v0.3.0 的 MenuType（F-337 / D-273）：控制终端的 0 槽面板（两个模式按钮 + 两格同步数据）。 */
    public static final DeferredHolder<MenuType<?>, MenuType<ControlTerminalMenu>> CONTROL_TERMINAL =
            REGISTER.register("control_terminal", () -> new MenuType<>(ControlTerminalMenu::new, FeatureFlags.VANILLA_SET));

    private FarmstormMenus() {}
}
