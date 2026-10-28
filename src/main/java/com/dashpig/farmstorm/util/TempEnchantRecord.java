package com.dashpig.farmstorm.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 临时附魔账本条目（D-15 / D-41），落在物品的 {@code farmstorm:temporary_enchantments} 组件上。
 *
 * @param granted  本次授予的等级
 * @param previous 授予前的原等级（0 = 原本没有），撤销时据此还原
 * @param expireAt 到期的绝对 gameTime（不是剩余刻数，避免存档停顿后漂移）
 */
public record TempEnchantRecord(Holder<Enchantment> enchantment, int granted, int previous, long expireAt) {

    public static final Codec<TempEnchantRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Enchantment.CODEC.fieldOf("enchantment").forGetter(TempEnchantRecord::enchantment),
            Codec.INT.fieldOf("granted").forGetter(TempEnchantRecord::granted),
            Codec.INT.fieldOf("previous").forGetter(TempEnchantRecord::previous),
            Codec.LONG.fieldOf("expireAt").forGetter(TempEnchantRecord::expireAt)
    ).apply(instance, TempEnchantRecord::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TempEnchantRecord> STREAM_CODEC = StreamCodec.composite(
            Enchantment.STREAM_CODEC, TempEnchantRecord::enchantment,
            ByteBufCodecs.VAR_INT, TempEnchantRecord::granted,
            ByteBufCodecs.VAR_INT, TempEnchantRecord::previous,
            ByteBufCodecs.VAR_LONG, TempEnchantRecord::expireAt,
            TempEnchantRecord::new
    );

    public static final Codec<java.util.List<TempEnchantRecord>> LIST_CODEC = CODEC.listOf();

    public static final StreamCodec<RegistryFriendlyByteBuf, java.util.List<TempEnchantRecord>> LIST_STREAM_CODEC =
            STREAM_CODEC.apply(ByteBufCodecs.list());
}
