package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.ConverterBlock;
import com.dashpig.farmstorm.block.DeployedCropBlock;
import com.dashpig.farmstorm.block.FarmstormCropBlock;
import com.dashpig.farmstorm.util.CropProfile;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 20 个方块（TECH §3.2）：14 个作物 + 5 个部署 + 1 个转换器。
 * 14 个作物方块共用一个类且不注册 BlockItem（D-34 / T-01）。
 */
public final class FarmstormBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(Farmstorm.MOD_ID);

    // ---- 14 个作物方块 ----
    public static final DeferredBlock<FarmstormCropBlock> PUFFED_WHEAT_CROP = crop(CropProfile.PUFFED_WHEAT);
    public static final DeferredBlock<FarmstormCropBlock> BLAST_CARROT_CROP = crop(CropProfile.BLAST_CARROT);
    public static final DeferredBlock<FarmstormCropBlock> UBI_SERVER_CROP = crop(CropProfile.UBI_SERVER);
    public static final DeferredBlock<FarmstormCropBlock> TOXIC_UBI_SERVER_CROP = crop(CropProfile.TOXIC_UBI_SERVER);
    public static final DeferredBlock<FarmstormCropBlock> POWER_BEET_CROP = crop(CropProfile.POWER_BEET);
    public static final DeferredBlock<FarmstormCropBlock> MELON_GRENADE_STEM = crop(CropProfile.MELON_GRENADE);
    public static final DeferredBlock<FarmstormCropBlock> PUMPKIN_TURRET_STEM = crop(CropProfile.PUMPKIN_TURRET);
    public static final DeferredBlock<FarmstormCropBlock> AWAKENED_TORCHFLOWER_CROP = crop(CropProfile.AWAKENED_TORCHFLOWER);
    public static final DeferredBlock<FarmstormCropBlock> AWAKENED_PITCHER_PLANT_CROP = crop(CropProfile.AWAKENED_PITCHER_PLANT);
    public static final DeferredBlock<FarmstormCropBlock> FANG_TRAP_CACTUS_BLOCK = crop(CropProfile.FANG_TRAP_CACTUS);
    public static final DeferredBlock<FarmstormCropBlock> BAMBOO_FIRECRACKER_STALK = crop(CropProfile.BAMBOO_FIRECRACKER);
    public static final DeferredBlock<FarmstormCropBlock> RED_MUSHROOM_PUFF_BLOCK = crop(CropProfile.RED_MUSHROOM_PUFF);
    public static final DeferredBlock<FarmstormCropBlock> BROWN_MUSHROOM_PUFF_BLOCK = crop(CropProfile.BROWN_MUSHROOM_PUFF);
    public static final DeferredBlock<FarmstormCropBlock> LUCKY_BERRY_BUSH = crop(CropProfile.LUCKY_BERRY);

    // ---- 5 个部署方块（kind / 悬空 / 固定硬挖 / 爆炸免疫）----
    public static final DeferredBlock<DeployedCropBlock> BLAST_CARROT = deployed(CropProfile.BLAST_CARROT, false, false, false);
    public static final DeferredBlock<DeployedCropBlock> PUMPKIN_TURRET = deployed(CropProfile.PUMPKIN_TURRET, true, true, false);
    public static final DeferredBlock<DeployedCropBlock> AWAKENED_TORCHFLOWER =
            deployed(CropProfile.AWAKENED_TORCHFLOWER, true, false, true);
    public static final DeferredBlock<DeployedCropBlock> AWAKENED_PITCHER_PLANT =
            deployed(CropProfile.AWAKENED_PITCHER_PLANT, true, false, true);
    public static final DeferredBlock<DeployedCropBlock> FANG_TRAP_CACTUS =
            deployed(CropProfile.FANG_TRAP_CACTUS, false, false, false);

    // ---- 1 个功能方块 ----
    public static final DeferredBlock<ConverterBlock> CONVERTER = REGISTER.registerBlock("converter", ConverterBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5F).requiresCorrectToolForDrops());

    private FarmstormBlocks() {}

    private static DeferredBlock<FarmstormCropBlock> crop(CropProfile profile) {
        return REGISTER.registerBlock(cropBlockPath(profile),
                properties -> new FarmstormCropBlock(profile, properties),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.CROP));
    }

    private static DeferredBlock<DeployedCropBlock> deployed(CropProfile profile, boolean supportsFloating,
                                                             boolean turretHardMine, boolean explosionImmune) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.CROP)
                // 部署方块不可被活塞推动或拉动：owner / 附着面 / 依附坐标必须同时成立（§4.4）
                .pushReaction(PushReaction.BLOCK);
        if (explosionImmune) {
            // D-14：任何爆炸都不能破坏火把花 / 瓶子草。3.6e6 是 T-05 的「不可破坏」阈值，
            // 真正的拦截在 farmstorm:explosion_immune 标签与爆炸批处理（M2）里。
            properties = properties.explosionResistance(3600000.0F);
        }
        return REGISTER.registerBlock(profile.path(),
                props -> new DeployedCropBlock(profile, supportsFloating, turretHardMine, explosionImmune, props),
                properties);
    }

    private static String cropBlockPath(CropProfile profile) {
        ResourceKey<Block> key = profile.cropBlock();
        if (key == null) {
            throw new IllegalStateException("Crop " + profile.path() + " has no crop block");
        }
        return key.location().getPath();
    }
}
