package com.probe.probe1201;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** NeoForge 1.20.1（= Forge 命名空间 net.minecraftforge.*）的垂直切片探针。 */
@Mod(ProbeMod.MOD_ID)
public final class ProbeMod {
    public static final String MOD_ID = "probe1201";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final RegistryObject<ProbeCropBlock> PROBE_CROP =
            BLOCKS.register("probe_crop", () -> new ProbeCropBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks()
                            .instabreak().sound(SoundType.CROP)));

    public static final RegistryObject<BlockItem> PROBE_SEED =
            ITEMS.register("probe_seed", () -> new BlockItem(PROBE_CROP.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<ProbeBlockEntity>> PROBE_BE =
            BLOCK_ENTITIES.register("probe_be",
                    () -> BlockEntityType.Builder.of(ProbeBlockEntity::new, PROBE_CROP.get()).build(null));

    public ProbeMod(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ProbeNetwork.register();
    }
}
