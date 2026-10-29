package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.util.FarmstormTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

/**
 * 3 条 farmstorm 方块标签 + 2 条并入 minecraft 的挖掘标签（TECH §6.7；needs_diamond_tool 由 F-74 新建）。
 * 条目全部取自注册对象，不写字符串 ID。
 */
public final class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        // 标签引用的是注册表条目而不是资源文件，故不传 ExistingFileHelper
        super(output, lookupProvider, Farmstorm.MOD_ID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(FarmstormTags.DEPLOYED_CROPS).add(
                FarmstormBlocks.BLAST_CARROT.get(),
                FarmstormBlocks.PUMPKIN_TURRET.get(),
                FarmstormBlocks.AWAKENED_TORCHFLOWER.get(),
                FarmstormBlocks.AWAKENED_PITCHER_PLANT.get(),
                FarmstormBlocks.FANG_TRAP_CACTUS.get(),
                // F-74：桑吉尔蔗也是部署方块（A-08 的「玩家自挖掉自身」与其它五个同一条路径）
                FarmstormBlocks.ZANGIEF_CANE.get());

        // D-14：任何爆炸都不得破坏
        tag(FarmstormTags.EXPLOSION_IMMUNE).add(
                FarmstormBlocks.AWAKENED_TORCHFLOWER.get(),
                FarmstormBlocks.AWAKENED_PITCHER_PLANT.get());

        // RT-09：给整合包作者的豁免口子，模组自己不填，空列表起步
        tag(FarmstormTags.BLAST_PROOF);

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                FarmstormBlocks.CONVERTER.get(),
                FarmstormBlocks.BLAST_CARROT.get(),
                FarmstormBlocks.PUMPKIN_TURRET.get(),
                FarmstormBlocks.AWAKENED_TORCHFLOWER.get(),
                FarmstormBlocks.AWAKENED_PITCHER_PLANT.get(),
                FarmstormBlocks.FANG_TRAP_CACTUS.get(),
                // F-74：压缩干草块硬度 30，只有镐可挖
                FarmstormBlocks.COMPRESSED_HAY_BLOCK.get());

        // F-74 / D-57：钻石以下（木/石/铁镐）一律不生效——它进了 incorrect_for_* 的父标签，
        // 于是「有正确工具才掉」与「工具够不够」由 requiresCorrectToolForDrops + 这条标签共同决定。
        tag(BlockTags.NEEDS_DIAMOND_TOOL).add(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get());
    }
}
