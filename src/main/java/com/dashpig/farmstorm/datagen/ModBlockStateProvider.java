package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.ConverterBlock;
import com.dashpig.farmstorm.block.DeployedCropBlock;
import com.dashpig.farmstorm.block.FarmstormCropBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 20 个 blockstate + 62 个方块模型（TECH §6.2 / §6.3）。
 * 贴图名一律取 assets/farmstorm/textures/block 下真实存在的 PNG（先列目录再写，不按文档猜）。
 * 5 个部署方块按作者口径分两类：陷阱类给空模型（世界里隐形），炮台 / 光环类借用原版方块模型。
 */
public final class ModBlockStateProvider extends BlockStateProvider {
    /** §6.3：<base> = 作物方块 ID 去掉末尾后缀。 */
    private static final String[] CROP_BLOCK_SUFFIXES = {"_crop", "_stem", "_stalk", "_block", "_bush"};
    /** 作物 age 0~3 共四变体。 */
    private static final int CROP_AGES = 4;
    /** §6.3：悬空态复用同一模型，只改 y 旋转与 uvlock；这里再加 90° 让悬空态与贴地态可区分（rotationY 必须是 90 的整数倍）。 */
    private static final int FLOATING_ROTATION_Y = 90;
    /** 十字 / 平面几何（作物、火把花、瓶子草）都是带透明边的 PNG，必须自带 cutout：原版 render type 在 Java 里注册，不随 parent 继承。 */
    private static final String CUTOUT = "minecraft:cutout";
    /** 陷阱类部署方块：空模型（只有 particle，与原版 barrier.json 同构），世界里不渲染任何几何。 */
    private static final Set<String> DEPLOYED_INVISIBLE = Set.of("blast_carrot", "fang_trap_cactus");
    /**
     * 炮台 / 光环类部署方块：直接借用原版方块模型。
     * 瓶子草在原版是双格高植物，pitcher_plant_bottom/top 的几何都是 y -5~11（下沉 5 像素，单独占一格会插进下方方块），
     * 故取同为原版、几何为整格 0~16 的成熟作物上半 pitcher_crop_top_stage_4。
     */
    private static final Map<String, String> DEPLOYED_VANILLA_PARENT = Map.of(
            "pumpkin_turret", "block/pumpkin",
            "awakened_torchflower", "block/torchflower",
            "awakened_pitcher_plant", "block/pitcher_crop_top_stage_4");
    /** 上述父模型里十字 / 平面的两个必须补 render_type；南瓜是原版 SOLID 满方块，保持默认。 */
    private static final Set<String> DEPLOYED_CUTOUT = Set.of("awakened_torchflower", "awakened_pitcher_plant");

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Farmstorm.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Block block : farmstormBlocks()) {
            if (block instanceof FarmstormCropBlock) {
                cropStates(block);
            } else if (block instanceof DeployedCropBlock) {
                deployedStates(block);
            } else if (block instanceof ConverterBlock) {
                converterStates(block);
            }
        }
    }

    /** 14 个作物：age 0~3 → 模型 <base>_stage0..3，父模型 minecraft:block/crop。 */
    private void cropStates(Block block) {
        String base = cropBase(registryPath(block));
        ModelFile[] stages = new ModelFile[CROP_AGES];
        for (int age = 0; age < CROP_AGES; age++) {
            String stageName = base + "_stage" + age;
            stages[age] = models().withExistingParent(stageName, mcLoc("block/crop"))
                    .texture("crop", modLoc("block/" + stageName))
                    .renderType(CUTOUT);
        }
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (int age = 0; age < CROP_AGES; age++) {
            builder.partialState().with(BlockStateProperties.AGE_3, age)
                    .modelForState().modelFile(stages[age]).addModel();
        }
    }

    /** 5 个部署方块：face × facing × floating 全覆盖，全部指向同一个 models/block/<id>.json。 */
    private void deployedStates(Block block) {
        String name = registryPath(block);
        ModelFile model = deployedModel(name);
        getVariantBuilder(block).forAllStates(state -> {
            int rotationY = facingRotationY(state.getValue(DeployedCropBlock.FACING));
            if (state.getValue(DeployedCropBlock.FLOATING)) {
                rotationY += FLOATING_ROTATION_Y;
            }
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(faceRotationX(state.getValue(DeployedCropBlock.FACE)))
                    .rotationY(rotationY % 360)
                    .uvLock(true)
                    .build();
        });
    }

    /** 陷阱类留空，炮台 / 光环类指向原版父模型；两张表都必须命中，避免新增部署方块时静默拿到错误外观。 */
    private ModelFile deployedModel(String name) {
        if (DEPLOYED_INVISIBLE.contains(name)) {
            return models().getBuilder(name).texture("particle", modLoc("block/" + name));
        }
        String vanillaParent = DEPLOYED_VANILLA_PARENT.get(name);
        if (vanillaParent == null) {
            throw new IllegalStateException("部署方块缺少外观定义: " + name);
        }
        BlockModelBuilder model = models().withExistingParent(name, mcLoc(vanillaParent));
        if (DEPLOYED_CUTOUT.contains(name)) {
            model.renderType(CUTOUT);
        }
        return model;
    }

    /**
     * 仅 WALL 有意义；FLOOR / CEILING 固定 NORTH（§4.4）。
     * 没有独立的贴墙几何，故 WALL 与 FLOOR 同角度。
     */
    private static int faceRotationX(AttachFace face) {
        return switch (face) {
            case FLOOR, WALL -> 0;
            case CEILING -> 180;
        };
    }

    /**
     * 转换器六面贴图（FIX-5）：四个侧面各叠一种作物，顶面与底面都是原版工作台顶面 ——
     * 原版 crafting_table 模型的 up / down 也用同一张 crafting_table_top，底面跟着照抄。
     * 键用 Direction#getSerializedName()，正好是 minecraft:block/cube 的 up/down/north/south/east/west。
     */
    private static final Map<Direction, String> CONVERTER_FACE_TEXTURES = Map.of(
            Direction.NORTH, "block/converter_side_wheat",
            Direction.EAST, "block/converter_side_carrot",
            Direction.SOUTH, "block/converter_side_potato",
            Direction.WEST, "block/converter_side_beetroot",
            Direction.UP, "block/converter_top",
            Direction.DOWN, "block/converter_top");

    /**
     * 转换器：整格 cube，四面各贴一种作物；朝向只靠 blockstate 的 y 旋转（四个 facing 共用一个模型文件），
     * 因此作物顺序会跟着朝向转 —— 已确认 4 个 variant 都指向同一个模型、无空变体。
     */
    private void converterStates(Block block) {
        String name = registryPath(block);
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/cube"))
                .texture("particle", modLoc(CONVERTER_FACE_TEXTURES.get(Direction.NORTH)));
        for (Direction direction : Direction.values()) {
            model.texture(direction.getSerializedName(), modLoc(CONVERTER_FACE_TEXTURES.get(direction)));
        }
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(model)
                .rotationY(facingRotationY(state.getValue(ConverterBlock.FACING)))
                .uvLock(true)
                .build());
    }

    /**
     * 朝向 → blockstate 的 y 旋转，与 vanilla furnace.json 的写法一致（north=0 / east=90 / south=180 / west=270）。
     * 不能用 Direction#toYRot()：它按 data2d 算，对这个约定整整差 180°，会让转换器正面背对玩家。
     */
    private static int facingRotationY(Direction facing) {
        return switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private static String cropBase(String registryPath) {
        for (String suffix : CROP_BLOCK_SUFFIXES) {
            if (registryPath.endsWith(suffix)) {
                return registryPath.substring(0, registryPath.length() - suffix.length());
            }
        }
        return registryPath;
    }

    private static String registryPath(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null) {
            throw new IllegalStateException("Unregistered block " + block);
        }
        return id.getPath();
    }

    /** 本模组注册的 20 个方块：14 作物 + 5 部署 + 1 转换器。 */
    private static List<Block> farmstormBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id != null && Farmstorm.MOD_ID.equals(id.getNamespace())) {
                blocks.add(block);
            }
        }
        return blocks;
    }
}
