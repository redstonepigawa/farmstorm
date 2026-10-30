package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.util.FarmstormTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;

/**
 * 3 条 farmstorm 物品标签（TECH §6.7）：15 个改良作物 + 6 个独立播种物 + 37 项转换器燃料。
 * 条目全部取自注册对象与原版常量的 {@code Item} 实例，不写字符串 ID。
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

        // F-71 / D-52 / DESIGN §2.5：37 项 = 18 项原版农作物 + 19 条改良作物（22 条档案减播种物 / 二级合成物 / 方块）。
        // 不入标签的三类：播种物（是种子不是农作物）、compressed_puffed_wheat（D-56 二级合成物）、
        // compressed_hay_block（方块不是农作物）。molotov / zangief_cane / wild_apple 虽无作物方块与种子，
        // 但它们是「改良作物」本身（D-51），按合并口径入标签——它们只从转换器产出，正是燃料集合的一员。
        tag(FarmstormTags.CONVERTER_FUEL).add(
                // 原版一侧：01-DESIGN.md §5 的 15 个原版参照
                Items.WHEAT,
                Items.CARROT,
                Items.POTATO,
                Items.POISONOUS_POTATO,
                Items.BEETROOT,
                Items.MELON_SLICE,
                Items.MELON,
                Items.PUMPKIN,
                Items.TORCHFLOWER,
                Items.PITCHER_PLANT,
                Items.CACTUS,
                Items.BAMBOO,
                Items.RED_MUSHROOM,
                Items.BROWN_MUSHROOM,
                Items.SWEET_BERRIES,
                // 原版一侧：新增三条的原版参照（【P-2】绯红菌 / 【P-3】甘蔗 / D-54 金苹果）
                Items.CRIMSON_FUNGUS,
                Items.SUGAR_CANE,
                Items.GOLDEN_APPLE,
                // 改良一侧：v0.0.1 的 15 条档案
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
                FarmstormItems.LUCKY_BERRY.get(),
                // 改良一侧：v0.0.2 新增的 3 条档案
                FarmstormItems.MOLOTOV.get(),
                FarmstormItems.ZANGIEF_CANE.get(),
                FarmstormItems.WILD_APPLE.get(),
                // 改良一侧：v0.0.3 的 1 条（D-84：朋友的韭能当合成材料，但它自己没有转换配方）
                FarmstormItems.FRIEND_CHIVES.get());
    }
}
