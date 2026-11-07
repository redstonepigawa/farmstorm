package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/** 1 个创造模式标签页（TECH §3.6）：22 个物品按「作物 → 播种物 → 工具方块」排序。 */
public final class FarmstormCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Farmstorm.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = REGISTER.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.farmstorm.main"))
                    .icon(() -> new ItemStack(FarmstormItems.PUFFED_WHEAT.get()))
                    .displayItems((parameters, output) ->
                            FarmstormItems.creativeTabItems().forEach(item -> output.accept(item.get())))
                    .build());

    private FarmstormCreativeTabs() {}
}
