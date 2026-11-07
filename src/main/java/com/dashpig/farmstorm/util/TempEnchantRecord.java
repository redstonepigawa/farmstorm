package com.dashpig.farmstorm.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 临时附魔账本条目（D-15 / D-41），落在物品 NBT 的 {@code FarmstormTempEnchants} 列表上。
 *
 * <p><b>1.20.1 没有 {@code DataComponentType}</b>，按 docs/zh/02-TECH-1.20.1.md §4.1 改走物品 NBT；
 * 字段形状与原组件一字不差（{@code enchantment / granted / previous / expireAt}），
 * 因此 {@code /data get entity <玩家> Inventory} 能直接读到。
 *
 * @param granted  本次授予的等级
 * @param previous 授予前的原等级（0 = 原本没有），撤销时据此还原
 * @param expireAt 到期的绝对 gameTime（不是剩余刻数，避免存档停顿后漂移）
 */
public record TempEnchantRecord(Holder<Enchantment> enchantment, int granted, int previous, long expireAt) {

    /**
     * 1.20.1 的附魔还在 {@code BuiltInRegistries}（不是数据包注册表），没有 {@code Enchantment.CODEC}，
     * 因此用注册 ID 存、按 ID 还原；内联附魔（无注册键）无法落盘，编码时直接报错。
     */
    public static final Codec<Holder<Enchantment>> ENCHANTMENT_CODEC = ResourceLocation.CODEC.flatXmap(
            id -> BuiltInRegistries.ENCHANTMENT
                    .getHolder(ResourceKey.create(Registries.ENCHANTMENT, id))
                    .<DataResult<Holder<Enchantment>>>map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "未知附魔: " + id)),
            holder -> holder.unwrapKey()
                    .<DataResult<ResourceLocation>>map(key -> DataResult.success(key.location()))
                    .orElseGet(() -> DataResult.error(() -> "内联附魔没有注册键，无法落盘")));

    public static final Codec<TempEnchantRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ENCHANTMENT_CODEC.fieldOf("enchantment").forGetter(TempEnchantRecord::enchantment),
            Codec.INT.fieldOf("granted").forGetter(TempEnchantRecord::granted),
            Codec.INT.fieldOf("previous").forGetter(TempEnchantRecord::previous),
            Codec.LONG.fieldOf("expireAt").forGetter(TempEnchantRecord::expireAt)
    ).apply(instance, TempEnchantRecord::new));

    public static final Codec<java.util.List<TempEnchantRecord>> LIST_CODEC = CODEC.listOf();

    /** 同一个附魔：优先比 {@code Holder} 相等，再退回注册键比较（与原 {@code sameEnchantment} 同口径）。 */
    public boolean sameEnchantment(Holder<Enchantment> other) {
        if (enchantment.value() == other.value()) {
            return true;
        }
        Optional<ResourceKey<Enchantment>> mine = enchantment.unwrapKey();
        Optional<ResourceKey<Enchantment>> theirs = other.unwrapKey();
        return mine.isPresent() && mine.equals(theirs);
    }
}
