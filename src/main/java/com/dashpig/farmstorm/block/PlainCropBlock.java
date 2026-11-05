package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 平凡韭菜的作物方块（F-297 / D-215、裁定 #7 / #14）：age 0~3，`不接` CropProfile。
 * 轻量路径的第二半（54 号 §4.3.4）：`{@link FarmstormCropBlock}` 的构造器与 codec 都强制要一份非空档案
 * （`FarmstormCropBlock.java:43-46,57-60`），给平凡作物凑假档案会污染 `CropProfile.ALL`（红线 7），
 * 故作物侧的契约在这里按节照抄一份、档案那一支整体缺席。
 */
public class PlainCropBlock extends CropBlock {
    /** 本方块自己的成熟等级（0~3，四段），与 `FarmstormCropBlock.MAX_CUSTOM_AGE` 同值不同源。 */
    public static final int MAX_AGE = 3;

    /** 生长亮度门槛：与原版 `CropBlock` 的随机刻判据同一条（D-110 第 4 条）。 */
    private static final int GROWTH_LIGHT_LEVEL = 9;

    /** 亮度不足时的重试间隔：暗处不长这条语义保留，但计划链不能断（24 号 §4.3 第 5 步）。 */
    private static final int DARK_RETRY_TICKS = 20;

    public static final MapCodec<PlainCropBlock> CODEC = simpleCodec(PlainCropBlock::new);

    /** 取块用的播种物 id：按注册表反查（`FarmstormCropBlock.getBaseSeedId` 的同一写法），本类不持物品引用。 */
    private static final ResourceKey<Item> BASE_SEED =
            ResourceKey.create(Registries.ITEM, Farmstorm.id("plain_chives_seeds"));

    /** 四个碰撞箱逐字照 `FarmstormCropBlock.java:48-53`（等高的四段）。 */
    private static final VoxelShape[] SHAPE_BY_AGE = {
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };

    public PlainCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return CODEC;
    }

    /** 覆盖 CropBlock 的 0~7，本模组统一 0~3。 */
    @Override
    protected IntegerProperty getAgeProperty() {
        return BlockStateProperties.AGE_3;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    /** 同 `FarmstormCropBlock`：只在「方块从无到有」时排第一段（骨粉换状态的重排由 `tick` 末尾负责）。 */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (oldState.getBlock() == this || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        this.scheduleNext(serverLevel, pos, state);
    }

    /** 计划 tick 的推进（D-110 / D-111）：三道闸 + 推进一级 + 按新 age 排下一段。骨粉这条原版路径不重写。 */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getBlock() != this) {
            return;
        }
        int age = this.getAge(state);
        if (age >= MAX_AGE) {
            return;
        }
        if (level.getRawBrightness(pos, 0) < GROWTH_LIGHT_LEVEL) {
            level.scheduleTick(pos, this, DARK_RETRY_TICKS);
            return;
        }
        BlockState grown = state.setValue(BlockStateProperties.AGE_3, age + 1);
        level.setBlock(pos, grown, Block.UPDATE_ALL);
        this.scheduleNext(level, pos, grown);
    }

    /** 防双源推进：显式关掉随机刻，判据落在本方代码里（24 号 §4.4，与 `FarmstormCropBlock` 同一口径）。 */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    /** 排程入口只有这一个：不合条件就什么都不排，段长在排程当刻算定并写进计划 tick。 */
    private void scheduleNext(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() != this || this.getAge(state) >= MAX_AGE) {
            return;
        }
        level.scheduleTick(pos, this, this.segmentTicks(pos));
    }

    /** 单段刻数：与 `FarmstormCropBlock.segmentTicks` 读同一组 `growth.*` config，算式逐字相同。 */
    private int segmentTicks(BlockPos pos) {
        int min = FarmstormConfig.growthMinSeconds.get() * 20;
        int max = FarmstormConfig.growthMaxSeconds.get() * 20;
        int dMin = Math.ceilDiv(min, 3);
        int dMax = Math.max(dMin, max / 3);
        return dMin + Math.floorMod(pos.hashCode(), dMax - dMin + 1);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[this.getAge(state)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.AGE_3);
    }

    /** 创造模式取块走这里：原版 `CropBlock` 的默认值是小麦种子，平凡韭菜要取自己的播种物。 */
    @Override
    protected ItemLike getBaseSeedId() {
        Item seed = BuiltInRegistries.ITEM.get(BASE_SEED);
        return seed == null ? Items.AIR : seed;
    }
}
