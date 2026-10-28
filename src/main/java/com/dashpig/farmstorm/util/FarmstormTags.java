package com.dashpig.farmstorm.util;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 4 个 tag key（TECH §2 datagen 一节）。
 * 只在这里声明 key，内容由 DataGen Provider 生成，代码不写死成员。
 */
public final class FarmstormTags {
    private FarmstormTags() {}

    /** 任何爆炸都不得破坏的方块（D-14：火把花 / 瓶子草）。 */
    public static final TagKey<Block> EXPLOSION_IMMUNE = block("explosion_immune");

    /**
     * 整合包作者显式豁免的方块（RT-09）：无论阈值配成多少，爆炸都不破坏它们。
     * 与 {@link #EXPLOSION_IMMUNE} 的分工——后者是模组自己的「任何爆炸都不许碰」，前者是给整合包留的口子。
     */
    public static final TagKey<Block> BLAST_PROOF = block("blast_proof");

    /** 5 个部署方块，供 DataGen 与整合包作者批量处理。 */
    public static final TagKey<Block> DEPLOYED_CROPS = block("deployed_crops");

    /** 15 个改良作物物品（D-09：不具备原版用途）。 */
    public static final TagKey<Item> MODIFIED_CROPS = item("modified_crops");

    /** 6 个独立播种物 + 4 个自身即播种物的作物。 */
    public static final TagKey<Item> SEEDS = item("seeds");

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, Farmstorm.id(path));
    }

    private static TagKey<Item> item(String path) {
        ResourceLocation id = Farmstorm.id(path);
        return TagKey.create(Registries.ITEM, id);
    }
}
