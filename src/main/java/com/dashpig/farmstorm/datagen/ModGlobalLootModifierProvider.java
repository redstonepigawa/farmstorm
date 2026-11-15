package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.loot.PuffedHayLootModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

/**
 * 全局战利品修饰器的两份数据文件（F-039 / D-049）。
 *
 * <p>Forge 的 Provider 一次写出两份：修饰器本体
 * {@code data/farmstorm/loot_modifiers/puffed_hay.json} 与清单
 * {@code data/forge/loot_modifiers/global_loot_modifiers.json}。清单命名空间固定 {@code forge}
 * （{@code LootModifierManager} 只读那一份），**不在清单里 = 修饰器静默不生效**、没有日志也没有编译错（RT-011）。
 * 两份都不许手写：手写件下次 {@code runData} 会被冲掉，也不进 HashCache。
 */
public final class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output) {
        super(output, Farmstorm.MOD_ID);
    }

    @Override
    protected void start() {
        // 只挂 minecraft:blocks/hay_block 一张表（D-047 / B-27）；条件类型是 Forge 自带的 forge:loot_table_id。
        LootItemCondition tableCondition = LootTableIdCondition
                .builder(new ResourceLocation("minecraft", "blocks/hay_block"))
                .build();
        add("puffed_hay", new PuffedHayLootModifier(new LootItemCondition[] {tableCondition}));
    }
}
