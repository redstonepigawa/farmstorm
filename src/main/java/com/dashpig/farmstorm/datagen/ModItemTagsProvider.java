package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.util.FarmstormTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;

/**
 * 2 条 farmstorm 物品标签（TECH §6.7）：15 个改良作物 + 6 个独立播种物。
 * 条目全部取自注册对象，不写字符串 ID。
 */
public final class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ModBlockTagsProvider blockTagsProvider) {
        // 没有 copy(block -> item) 的标签，但仍需方块标签的 lookup；EFH 同样不传（标签引用注册表条目）
        super(output, lookupProvider, blockTagsProvider.contentsGetter(), Farmstorm.MOD_ID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(FarmstormTags.MODIFIED_CROPS).add(
                FarmstormItems.PUFFED_WHEAT.get(),
                FarmstormItems.BLAST_CARROT.get(),
                FarmstormItems.UBI_SERVER.get(),
                FarmstormItems.TOXIC_UBI_SERVER.get(),
                FarmstormItems.POWER_BEET.get(),
                FarmstormItems.MELON_SLICE_BOMB.get(),
                FarmstormItems.MELON_GRENADE.get(),
                FarmstormItems.PUMPKIN_TURRET.get(),
                FarmstormItems.AWAKENED_TORCHFLOWER.get(),
                FarmstormItems.AWAKENED_PITCHER_PLANT.get(),
                FarmstormItems.FANG_TRAP_CACTUS.get(),
                FarmstormItems.BAMBOO_FIRECRACKER.get(),
                FarmstormItems.RED_MUSHROOM_PUFF.get(),
                FarmstormItems.BROWN_MUSHROOM_PUFF.get(),
                FarmstormItems.LUCKY_BERRY.get());

        tag(FarmstormTags.SEEDS).add(
                FarmstormItems.PUFFED_WHEAT_SEEDS.get(),
                FarmstormItems.POWER_BEET_SEEDS.get(),
                FarmstormItems.MELON_GRENADE_SEEDS.get(),
                FarmstormItems.PUMPKIN_TURRET_SEEDS.get(),
                FarmstormItems.AWAKENED_TORCHFLOWER_SEEDS.get(),
                FarmstormItems.AWAKENED_PITCHER_PLANT_POD.get());
    }
}
