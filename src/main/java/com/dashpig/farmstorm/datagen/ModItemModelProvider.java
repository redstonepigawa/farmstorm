package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.ConverterBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 31 个物品模型（TECH §6.4）：15 个作物 + 7 个独立播种物 + 2 个二级合成物 + 4 个 v0.0.3 新物品 + 1 个转换器。
 * 部署方块的世界模型分两类（隐形的陷阱 / 原版的炮台光环），图标因此不再统一引用方块模型：
 * 炮台 / 光环类与瓜！指向原版方块模型，两个隐形陷阱指向自己的 item 贴图，只有转换器仍引用自家方块模型。
 */
public final class ModItemModelProvider extends ItemModelProvider {
    /** 炮台 / 光环类部署方块与瓜！：手持与投掷物都直接复用原版方块模型（3D 外观，与原版一致）。 */
    private static final Map<String, String> VANILLA_BLOCK_PARENT = Map.of(
            "pumpkin_turret", "block/pumpkin",
            "awakened_torchflower", "block/torchflower",
            "awakened_pitcher_plant", "block/pitcher_crop_top_stage_4",
            "melon_grenade", "block/melon",
            // D-86：近防菌的图标与方块共用原版诡异菌模型（TECH §9.3 第 12 行）
            "point_defense_fungus", "block/warped_fungus");

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Farmstorm.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (Item item : farmstormItems()) {
            String path = registryPath(item);
            String vanillaParent = VANILLA_BLOCK_PARENT.get(path);
            if (vanillaParent != null) {
                withExistingParent(path, mcLoc(vanillaParent));
            } else if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ConverterBlock) {
                withExistingParent(path, modLoc("block/" + blockPath(blockItem.getBlock())));
            } else if ("compressed_hay_block".equals(path)) {
                // TECH §3.14：普通方块物品直接复用方块模型，不额外要一张 item 贴图。
                withExistingParent(path, modLoc("block/compressed_hay_block"));
            } else {
                basicItem(item);
            }
        }
    }

    private static String registryPath(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) {
            throw new IllegalStateException("Unregistered item " + item);
        }
        return id.getPath();
    }

    private static String blockPath(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null) {
            throw new IllegalStateException("Unregistered block " + block);
        }
        return id.getPath();
    }

    /** 本模组注册的 31 个物品。 */
    private static List<Item> farmstormItems() {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && Farmstorm.MOD_ID.equals(id.getNamespace())) {
                items.add(item);
            }
        }
        return items;
    }
}
