package com.dashpig.farmstorm.loot;

import com.dashpig.farmstorm.util.PuffedHayData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * 膨胀麦落点干草块「破坏后不掉落」的消费点（F-038 / D-047 / D-048）。
 *
 * <p>1.20.1 没有方块掉落事件（{@code BlockEvent} 的嵌套类里没有 {@code HarvestDropsEvent} /
 * {@code BlockDropsEvent}），等价拦截面只有全局战利品修饰器：作用面由数据文件里的
 * {@code forge:loot_table_id} 条件钉到 {@code minecraft:blocks/hay_block} 一张表，
 * 别的表一个字节不受影响。条件判定在 {@link LootModifier#apply} 里（那条是 final），
 * 本类只实现条件通过之后的 {@link #doApply}。
 *
 * <p>命中记录 ⇒ 摘掉该条并返回**空表**：{@code ForgeHooks} 拿返回值整表替换，
 * 于是本次零掉落，而方块照旧被移除、经验照旧结算（D-048：这不是「取消破坏」）。
 * 未命中 ⇒ 原样返回 {@code generatedLoot}。
 *
 * <p>{@code LootContextParams.ORIGIN} 在 1.20.1 是 {@link net.minecraft.world.phys.Vec3}，
 * 必须先经 {@link BlockPos#containing(net.minecraft.core.Position)} 转坐标再查集合（RT-012）。
 */
public final class PuffedHayLootModifier extends LootModifier {
    /** 数据文件的反序列化入口：字段只有基类那一条 {@code conditions}（{@code LootModifier#codecStart}）。 */
    public static final Codec<PuffedHayLootModifier> CODEC =
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, PuffedHayLootModifier::new));

    public PuffedHayLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockPos pos = BlockPos.containing(context.getParam(LootContextParams.ORIGIN));
        PuffedHayData data = PuffedHayData.get(context.getLevel().getServer());
        // remove 自己会 setDirty：命中才写脏，未命中不动存档。
        if (!data.remove(pos)) {
            return generatedLoot;
        }
        return new ObjectArrayList<>();
    }
}
