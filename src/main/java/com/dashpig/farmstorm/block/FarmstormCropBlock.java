package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.util.CropProfile;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
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
