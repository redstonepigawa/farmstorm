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
 * 4 条 farmstorm 方块标签 + 2 条并入 minecraft 的挖掘标签（TECH §6.7；needs_diamond_tool 由 F-74 新建）。
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
                FarmstormBlocks.ZANGIEF_CANE.get(),
                // F-132（A5）：v0.0.3 的两个新部署物
                FarmstormBlocks.POINT_DEFENSE_FUNGUS.get(),
                FarmstormBlocks.VIBRATION_BEAN.get());

        // D-14：任何爆炸都不得破坏
        tag(FarmstormTags.EXPLOSION_IMMUNE).add(
                FarmstormBlocks.AWAKENED_TORCHFLOWER.get(),
                FarmstormBlocks.AWAKENED_PITCHER_PLANT.get());

        // RT-09：给整合包作者的豁免口子，模组自己不填，空列表起步
        tag(FarmstormTags.BLAST_PROOF);

        // D-208：可被据点核心绑定的方块，成员 2 = 培养器 + 农作物转换器（crop_converter）
        tag(FarmstormTags.PROCESSING_BLOCK).add(
                FarmstormBlocks.CULTIVATOR.get(),
                FarmstormBlocks.CROP_CONVERTER.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                FarmstormBlocks.CROP_CONVERTER.get(),
                FarmstormBlocks.BLAST_CARROT.get(),
                FarmstormBlocks.PUMPKIN_TURRET.get(),
                FarmstormBlocks.AWAKENED_TORCHFLOWER.get(),
                FarmstormBlocks.AWAKENED_PITCHER_PLANT.get(),
                FarmstormBlocks.FANG_TRAP_CACTUS.get(),
                // F-132（A5）：两个新部署物与既有部署方块一致（§9.3 A5 的另一半：zangief_cane 逐旧不补）
                FarmstormBlocks.POINT_DEFENSE_FUNGUS.get(),
                FarmstormBlocks.VIBRATION_BEAN.get(),
                // F-74：压缩干草块硬度 30，只有镐可挖
                FarmstormBlocks.COMPRESSED_HAY_BLOCK.get(),
                // F-168 / D-119 第 2 项：据点核心「任意等级的镐都能挖出掉落物」——只进这一条，
                // 下面三个 needs_*_tool 一个都不加；漏了这一条，木镐挖它要按 220 秒以上且什么都不掉。
                FarmstormBlocks.OUTPOST_CORE.get());

        // F-74 / D-57：钻石以下（木/石/铁镐）一律不生效——它进了 incorrect_for_* 的父标签，
        // 于是「有正确工具才掉」与「工具够不够」由 requiresCorrectToolForDrops + 这条标签共同决定。
        tag(BlockTags.NEEDS_DIAMOND_TOOL).add(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get());
    }
}
