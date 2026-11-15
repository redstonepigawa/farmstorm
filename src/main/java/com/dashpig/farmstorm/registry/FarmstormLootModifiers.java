package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.loot.PuffedHayLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 全局战利品修饰器的**序列化器**注册（F-038 / D-047）。
 *
 * <p>注册表键是 Forge 的 {@code forge:global_loot_modifier_serializers}
 * （{@code ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS}），元素类型是
 * {@code Codec<? extends IGlobalLootModifier>}——不是修饰器实例本身：实例来自数据文件，
 * 注册的只是解析它的编解码器。这是本版注册面的第 12 类（{@code global_loot_modifier_serializers}）。
 */
public final class FarmstormLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTER =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Farmstorm.MOD_ID);

    public static final RegistryObject<Codec<PuffedHayLootModifier>> PUFFED_HAY =
            REGISTER.register("puffed_hay", () -> PuffedHayLootModifier.CODEC);

    private FarmstormLootModifiers() {}
}
