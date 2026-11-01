package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.util.CropProfile;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
 * 14 个作物方块的唯一实现（D-34 / T-01）。
 * 生长状态全部落在 BlockState（age 0~3），不占 BlockEntity：作物是最多的方块族，省内存与同步带宽（TECH §3.3 注）。
 * 播种物通过 {@link CropProfile#seedItem()} 反查，因此 14 个方块共用一份实现而不需要 14 个类。
 */
public class FarmstormCropBlock extends CropBlock {
    /** 本模组所有作物的成熟等级（0~3，四段）。 */
    public static final int MAX_CUSTOM_AGE = 3;

    /** 生长亮度门槛：与原版 {@code CropBlock} 的随机刻判据同一条，不动（D-110 第 4 条）。 */
    private static final int GROWTH_LIGHT_LEVEL = 9;

    /** 亮度不足时的重试间隔：暗处不长这条语义保留，但计划链不能断（24 号 §4.3 第 5 步）。 */
    private static final int DARK_RETRY_TICKS = 20;

    public static final MapCodec<FarmstormCropBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CropProfile.CODEC.fieldOf("profile").forGetter(FarmstormCropBlock::profile),
            propertiesCodec()
    ).apply(instance, FarmstormCropBlock::new));

    private static final VoxelShape[] SHAPE_BY_AGE = {
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };

    private final CropProfile profile;

    public FarmstormCropBlock(CropProfile profile, BlockBehaviour.Properties properties) {
        super(properties);
        this.profile = profile;
    }

    public CropProfile profile() {
        return this.profile;
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
        return MAX_CUSTOM_AGE;
    }

    /**
     * 首次成型时排第一段计划 tick（D-111 第 1 条）。
     * 只在「方块从无到有」时排：骨粉抬高 age 走的是同方块换状态，重排由 {@link #tick} 末尾那一次负责，
     * 因此排程入口始终只有一个（24 号 §4.3 第 2 步与第 4 步）。
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (oldState.getBlock() == this || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        this.scheduleNext(serverLevel, pos, state);
    }

    /**
     * 计划 tick 的推进（D-110 / D-111）：过亮度闸、推进一级、按新 age 排下一段。
     * 前两行是「计划 tick 已过期」与「已成熟」两道闸，第三道是原版亮度条件（24 号 §4.3 第 3、5 步）。
     * 随机刻与骨粉这两条原版路径都不重写（24 号 §4.4 第 2 条、§4.5）。
     */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getBlock() != this) {
            return;
        }
        int age = this.getAge(state);
        if (age >= MAX_CUSTOM_AGE) {
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

    /** 防双源推进的第 3 条：显式关掉随机刻，判据落在本方代码里，不依赖原版实现细节（24 号 §4.4）。 */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    /** 排程入口只有这一个：不合条件就什么都不排，段长在排程当刻算定并写进计划 tick（24 号 §4.3 第 1 步）。 */
    private void scheduleNext(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() != this || this.getAge(state) >= MAX_CUSTOM_AGE) {
            return;
        }
        level.scheduleTick(pos, this, this.segmentTicks(pos));
    }

    /**
     * 单段刻数：三段等长、总时长 {@code T = 3d}，默认配置下 {@code d = 134 + floorMod(pos.hashCode(), 167)}、
     * {@code T ∈ [402, 900]} 刻（D-110 补充 / A-26、24 号 §4.2）。dMin 与 span 每次现算而**不是常量**——写死
     * 就等于把 growth.minSeconds / growth.maxSeconds 降级成装饰品（24 号 §4.2、§11 #3 后半句）。
     * 用坐标而不是 {@code level.random} 抽，是「无方块实体」下三段拿到同一个段长的前提（D-111 排除方块实体）。
     */
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

    /** 创造模式取块与剪刀掉落都走这里；瓜片子没有作物方块，兜底返回空物品。 */
    @Override
    protected ItemLike getBaseSeedId() {
        ResourceKey<Item> seed = this.profile.seedItem();
        if (seed == null) {
            return Items.AIR;
        }
        Item item = BuiltInRegistries.ITEM.get(seed);
        return item == null ? Items.AIR : item;
    }
}
