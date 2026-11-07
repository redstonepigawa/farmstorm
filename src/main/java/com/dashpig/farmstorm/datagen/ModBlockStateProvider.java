package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.CropConverterBlock;
import com.dashpig.farmstorm.block.CultivatorBlock;
import com.dashpig.farmstorm.block.DeployedCropBlock;
import com.dashpig.farmstorm.block.FarmstormCropBlock;
import com.dashpig.farmstorm.block.OutpostCoreBlock;
import com.dashpig.farmstorm.block.PlainCropBlock;
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
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * 本模组的方块状态与方块模型（数量由注册表与状态机决定；TECH §6.2 / §6.3）。
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
    private static final Set<String> DEPLOYED_INVISIBLE =
            Set.of("blast_carrot", "fang_trap_cactus", "zangief_cane", "vibration_bean");
    /**
     * 炮台 / 光环类部署方块：直接借用原版方块模型。
     * 瓶子草在原版是双格高植物，pitcher_plant_bottom/top 的几何都是 y -5~11（下沉 5 像素，单独占一格会插进下方方块），
     * 故取同为原版、几何为整格 0~16 的成熟作物上半 pitcher_crop_top_stage_4。
     */
    private static final Map<String, String> DEPLOYED_VANILLA_PARENT = Map.of(
            "pumpkin_turret", "block/pumpkin",
            "awakened_torchflower", "block/torchflower",
            "awakened_pitcher_plant", "block/pitcher_crop_top_stage_4",
            "point_defense_fungus", "block/warped_fungus");
    /** 上述父模型里十字 / 平面的三个必须补 render_type；南瓜是原版 SOLID 满方块，保持默认。 */
    private static final Set<String> DEPLOYED_CUTOUT =
            Set.of("awakened_torchflower", "awakened_pitcher_plant", "point_defense_fungus");
    /**
     * 隐形方块的 particle 贴图：默认取自家 textures/block/&lt;id&gt;，震动豆没有自家方块贴图，
     * 借原版可豆的成熟态（D-90 的对应物是可豆；TECH §9.1 第 8 张 PNG 只是物品图标）。
     */
    private static final Map<String, String> DEPLOYED_INVISIBLE_PARTICLE = Map.of(
            "vibration_bean", "block/cocoa_stage2");

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Farmstorm.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Block block : farmstormBlocks()) {
            if (block instanceof FarmstormCropBlock) {
                cropStates(block);
            } else if (block instanceof PlainCropBlock) {
                // 平凡作物与 FarmstormCropBlock 没有继承关系，两支互不命中；漏了这一支会落进 simpleBlock 兜底，
                // 世界里的韭菜变成六面立方块且不报错（RT-77 / 陷阱 16）。
                plainCropStates(block);
            } else if (block instanceof DeployedCropBlock) {
                deployedStates(block);
            } else if (block instanceof CropConverterBlock) {
                cropConverterStates(block);
            } else if (block instanceof OutpostCoreBlock) {
                outpostCoreStates(block);
            } else if (block instanceof CultivatorBlock) {
                cultivatorStates(block);
            } else if ("compressed_hay_block".equals(registryPath(block))) {
                compressedHayStates(block);
            } else {
                // 兜底：其余普通方块走 cubeAll + 同名贴图。
                // 没有这个分支时普通方块会被静默跳过，不生成 blockstate，客户端表现为缺模型。
                simpleBlock(block);
            }
        }
    }

    /** 15 个作物：age 0~3 → 模型 <base>_stage0..3，父模型 minecraft:block/crop。 */
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

    /**
     * 平凡韭菜（F-297 / D-215、裁定 #18）：age 0~3 → 模型 <base>_stage0..3，父模型 minecraft:block/crop。
     *
     * <p><b>不能复用 {@link #cropStates}</b>：那个方法按「模型名 = 贴图名」拼路径，会拼出不存在的
     * {@code block/plain_chives_stageN}；素材是朋友的韭的 {@code friend_chives_stageN}（陷阱 17 / 54 号 §3.5），
     * 故这里把贴图路径显式写成 {@code block/friend_chives_stageN}，不新增任何 PNG。
     */
    private void plainCropStates(Block block) {
        String base = cropBase(registryPath(block));
        ModelFile[] stages = new ModelFile[CROP_AGES];
        for (int age = 0; age < CROP_AGES; age++) {
            stages[age] = models().withExistingParent(base + "_stage" + age, mcLoc("block/crop"))
                    .texture("crop", modLoc("block/friend_chives_stage" + age))
                    .renderType(CUTOUT);
        }
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (int age = 0; age < CROP_AGES; age++) {
            builder.partialState().with(BlockStateProperties.AGE_3, age)
                    .modelForState().modelFile(stages[age]).addModel();
        }
    }

    /** 8 个部署方块：face × facing × floating 全覆盖，全部指向同一个 models/block/<id>.json。 */
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
            String vanillaParticle = DEPLOYED_INVISIBLE_PARTICLE.get(name);
            return models().getBuilder(name)
                    .texture("particle", vanillaParticle != null ? mcLoc(vanillaParticle) : modLoc("block/" + name));
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
     * 农作物转换器（D-211 / 54 号 §3.2）：{@code FACING} × {@code WORKING} = **8 个 variant**、两张模型。
     *
     * <p>{@code working=false} 引三张 {@code _a_*_still} 静态副本（**不带 mcmeta**），{@code working=true}
     * 引三张 16 帧帧条（{@code _a_front} / {@code _a_side} / {@code _a_top}，各带
     * {@code {"animation":{"frametime":2}}}）；底面 {@code _a_bottom} 两态同值、静态且不带 mcmeta。
     * 「只有工作时才动」由此天然成立——动态 PNG 一旦被引用就会自己播，代码碰不到 {@code frametime}，
     * 所以这里不写、也不许写任何暂停 / 恢复动画的代码（红线 4 / 陷阱 1）。
     *
     * <p><b>不能用 {@code simpleBlock(block, model)}</b>：它只生成单态，8 个 variant 里有 4 个工作态会
     * 找不到模型（陷阱 38）。朝向走 {@link #facingRotationY}（north 0 / east 90 / south 180 / west 270），
     * 不用 {@code Direction#toYRot()}（陷阱 37）。
     */
    private void cropConverterStates(Block block) {
        String name = registryPath(block);
        BlockModelBuilder idle = cubeSixFaces(name, name + "_a_front_still", name + "_a_side_still",
                name + "_a_top_still", name + "_a_bottom");
        BlockModelBuilder working = cubeSixFaces(name + "_working", name + "_a_front", name + "_a_side",
                name + "_a_top", name + "_a_bottom");
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(CropConverterBlock.WORKING) ? working : idle)
                .rotationY(facingRotationY(state.getValue(CropConverterBlock.FACING)))
                .uvLock(true)
                .build());
    }

    /**
     * 六面显式（父模型 {@code minecraft:block/cube}）+ {@code particle}：正面 / 三个侧面 / 顶面 / 底面
     * 各一张，只有正面与侧面 / 顶面的图在两态间不同，故两张模型共用一个助手。
     */
    private BlockModelBuilder cubeSixFaces(String modelName, String front, String side, String top, String bottom) {
        return models().withExistingParent(modelName, mcLoc("block/cube"))
                .texture("north", modLoc("block/" + front))
                .texture("south", modLoc("block/" + side))
                .texture("east", modLoc("block/" + side))
                .texture("west", modLoc("block/" + side))
                .texture("up", modLoc("block/" + top))
                .texture("down", modLoc("block/" + bottom))
                .texture("particle", modLoc("block/" + front));
    }

    /**
     * 据点核心（F-166 / D-123）：顶 / 侧 / 底各一张 32 × 32，父模型 cube_bottom_top。
     * 取 cube_bottom_top 而非 cube_all —— 作者给的就是三张不同的面，cube_all 会让另外两张白给。
     */
    private void outpostCoreStates(Block block) {
        String name = registryPath(block);
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/cube_bottom_top"))
                .texture("top", modLoc("block/" + name + "_top"))
                .texture("side", modLoc("block/" + name + "_side"))
                .texture("bottom", modLoc("block/" + name + "_bottom"));
        simpleBlock(block, model);
    }

    /**
     * 农作物培养器（F-225 / D-161 / 36 号 §2.5.5）：**两态**——{@code working=false} 引用静态首帧
     * （{@code _a_top_still} / {@code _a_side_still} / {@code _a_bottom}），{@code working=true} 引用 16 帧帧条
     * （{@code _a_top} / {@code _a_side} / {@code _a_bottom}），两张块模型父模型都是 {@code cube_bottom_top}。
     *
     * <p><b>不能用现成的 {@code simpleBlock(block, model)}</b>：它生成的是 {@code "": {...}} 单态，
     * 两态的方块会有一个 variant 找不到模型（36 号 §5 的连带改动 2）。动画的「空闲静态」靠双贴图 + blockstate 属性，
     * 不靠代码暂停（D-161 / 陷阱 27）。
     */
    private void cultivatorStates(Block block) {
        String name = registryPath(block);
        BlockModelBuilder idle = models().withExistingParent(name, mcLoc("block/cube_bottom_top"))
                .texture("top", modLoc("block/" + name + "_a_top_still"))
                .texture("side", modLoc("block/" + name + "_a_side_still"))
                .texture("bottom", modLoc("block/" + name + "_a_bottom"));
        BlockModelBuilder working = models().withExistingParent(name + "_working", mcLoc("block/cube_bottom_top"))
                .texture("top", modLoc("block/" + name + "_a_top"))
                .texture("side", modLoc("block/" + name + "_a_side"))
                .texture("bottom", modLoc("block/" + name + "_a_bottom"));
        getVariantBuilder(block)
                .partialState().with(CultivatorBlock.WORKING, false).modelForState().modelFile(idle).addModel()
                .partialState().with(CultivatorBlock.WORKING, true).modelForState().modelFile(working).addModel();
    }

    /**
     * 压缩麦方块（F-165 / D-107 第 1、2 项）：顶 / 侧 / 底三张不同，父模型 cube_bottom_top。
     * 侧贴图沿用原文件名，只有顶 / 底带 _top / _bottom 后缀 —— 与据点核心的 _side 命名不同，故单独一个方法。
     */
    private void compressedHayStates(Block block) {
        String name = registryPath(block);
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/cube_bottom_top"))
                .texture("side", modLoc("block/" + name))
                .texture("top", modLoc("block/" + name + "_top"))
                .texture("bottom", modLoc("block/" + name + "_bottom"));
        simpleBlock(block, model);
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

    /** 本模组注册的 26 个方块：15 作物 + 8 部署 + 1 转换器 + 1 普通方块（compressed_hay_block）+ 1 据点核心（outpost_core）。 */
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
