package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.CropConverterBlock;
import com.dashpig.farmstorm.block.CultivatorBlock;
import com.dashpig.farmstorm.block.DeployedCropBlock;
import com.dashpig.farmstorm.block.FarmstormCropBlock;
import com.dashpig.farmstorm.block.OutpostCoreBlock;
import com.dashpig.farmstorm.block.PlainCropBlock;
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
 * 27 个方块（TECH §2.2；v0.1.0 按 D-119 加 1 个据点核心、v0.2.0 按 D-160 加 1 个培养器）：
 * 15 个作物 + 8 个部署 + 1 个转换器 + 1 个压缩干草块 + 1 个据点核心 + 1 个农作物培养器。
 * 作物方块共用一个类且不注册 BlockItem（D-34 / T-01）。
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

    // ---- v0.0.3 新增的 3 个方块（TECH §2.1 第 2 行 +3）----
    /** D-84：朋友的韭的种植形态，注册 ID 由档案的作物方块键给出（{@code friend_chives_crop}）；4 个 age 由 F-114 落。 */
    public static final DeferredBlock<FarmstormCropBlock> FRIEND_CHIVES_CROP = crop(CropProfile.FRIEND_CHIVES);

    // ---- 8 个部署方块（kind / 悬空 / 固定硬挖 / 爆炸免疫 / 不依附）----
    public static final DeferredBlock<DeployedCropBlock> BLAST_CARROT = deployed(CropProfile.BLAST_CARROT, false, false, false, false);
    public static final DeferredBlock<DeployedCropBlock> PUMPKIN_TURRET = deployed(CropProfile.PUMPKIN_TURRET, true, true, false, false);
    public static final DeferredBlock<DeployedCropBlock> AWAKENED_TORCHFLOWER =
            deployed(CropProfile.AWAKENED_TORCHFLOWER, true, false, true, false);
    public static final DeferredBlock<DeployedCropBlock> AWAKENED_PITCHER_PLANT =
            deployed(CropProfile.AWAKENED_PITCHER_PLANT, true, false, true, false);
    public static final DeferredBlock<DeployedCropBlock> FANG_TRAP_CACTUS =
            deployed(CropProfile.FANG_TRAP_CACTUS, false, false, false, false);
    /** D-68：陷阱口径与尖牙仙人掌逐项一致；与它共用同一个 BlockEntityType（RT-04）。 */
    public static final DeferredBlock<DeployedCropBlock> ZANGIEF_CANE =
            deployed(CropProfile.ZANGIEF_CANE, false, false, false, false);
    /**
     * D-86：近防菌，炮台类外观（不隐形）；索敌在 F-118，结算与图效在 F-119 / F-120。
     * 三个旗标文档没有逐项点名，按同类（炮台类只有 {@link #PUMPKIN_TURRET} 一个既有实例）比出来：
     * 悬空 = true（DESIGN §5.7 的「依附丢失后保留并标记」描述的就是这条）、固定硬挖 = true（炮台类口径）、爆炸免疫 = false（只有火把花 / 瓶子草免疫）。
     */
    public static final DeferredBlock<DeployedCropBlock> POINT_DEFENSE_FUNGUS =
            deployed(CropProfile.POINT_DEFENSE_FUNGUS, true, true, false, false);
    /** D-91：震动豆，隐形空模型；第 5 个旗标 {@code unattached = true} 是它唯一的专属模式（F-123，§7.1）。 */
    public static final DeferredBlock<DeployedCropBlock> VIBRATION_BEAN =
            deployed(CropProfile.VIBRATION_BEAN, false, false, false, true);

    // ---- 1 个功能方块 ----
    public static final DeferredBlock<CropConverterBlock> CROP_CONVERTER = REGISTER.registerBlock("crop_converter",
            CropConverterBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.ANVIL)
                    .pushReaction(PushReaction.BLOCK));

    // ---- 1 个建材方块（D-57：硬度照远古残骸 30.0，抗性照圆石 6.0，六面同材质不可旋转）----
    public static final DeferredBlock<Block> COMPRESSED_HAY_BLOCK = REGISTER.registerBlock("compressed_hay_block",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(30.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE));

    // ---- 1 个据点核心（D-119：硬度 30 / 抗爆 1200 / 任意镐可挖 / 不可推动；F-138 只落注册面，行为在 Phase 23）----
    public static final DeferredBlock<OutpostCoreBlock> OUTPOST_CORE = REGISTER.registerBlock("outpost_core",
            OutpostCoreBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(30.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.BLOCK)
                    .lightLevel(state -> 15));

    // ---- 1 个农作物培养器（F-224 / D-160）：属性逐项照原版铁砧七行，培养器无菜单 ----
    public static final DeferredBlock<CultivatorBlock> CULTIVATOR = REGISTER.registerBlock("cultivator",
            CultivatorBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.ANVIL)
                    .pushReaction(PushReaction.BLOCK));

    // ---- 1 个平凡作物（F-297 / D-215）：属性照上面 crop(...) 工厂的四项，不写 randomTicks（生长走计划 tick）----
    /** 平凡韭菜的种植形态：age 0~3，贴图复用朋友的韭的 4 张 stage 图（裁定 #18）；不进 {@code CropProfile}。 */
    public static final DeferredBlock<PlainCropBlock> PLAIN_CHIVES_CROP = REGISTER.registerBlock("plain_chives_crop",
            PlainCropBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.CROP));

    private FarmstormBlocks() {}

    private static DeferredBlock<FarmstormCropBlock> crop(CropProfile profile) {
        return REGISTER.registerBlock(cropBlockPath(profile),
                properties -> new FarmstormCropBlock(profile, properties),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.CROP));
    }

    private static DeferredBlock<DeployedCropBlock> deployed(CropProfile profile, boolean supportsFloating,
                                                             boolean turretHardMine, boolean explosionImmune,
                                                             boolean unattached) {
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
                props -> new DeployedCropBlock(profile, supportsFloating, turretHardMine, explosionImmune, unattached, props),
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
