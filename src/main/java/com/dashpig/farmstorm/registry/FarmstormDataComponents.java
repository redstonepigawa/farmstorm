package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.util.TempEnchantRecord;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 1 个数据组件（TECH §3.8）：临时附魔账本，落在物品上（D-15 / D-41）。 */
public final class FarmstormDataComponents {
    public static final DeferredRegister.DataComponents REGISTER =
            DeferredRegister.createDataComponents(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, Farmstorm.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<TempEnchantRecord>>> TEMPORARY_ENCHANTMENTS =
            REGISTER.registerComponentType("temporary_enchantments", builder -> builder
                    .persistent(TempEnchantRecord.LIST_CODEC)
                    .networkSynchronized(TempEnchantRecord.LIST_STREAM_CODEC));

    private FarmstormDataComponents() {}
}
