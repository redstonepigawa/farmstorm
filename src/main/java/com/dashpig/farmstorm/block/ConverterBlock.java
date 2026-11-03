package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.ConverterBlockEntity;
import com.dashpig.farmstorm.menu.ConverterMenu;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 农产品转换器方块（TECH §4.8.1）。单槽 GUI、带朝向；转换事务在 {@link ConverterBlockEntity} 上。
 * 方块属性（{@code strength(3.5F)} / {@code requiresCorrectToolForDrops()}）在 FarmstormBlocks 注册处声明。
 */
public class ConverterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<ConverterBlock> CODEC = simpleCodec(ConverterBlock::new);

    public ConverterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** 转换器是实体方块，但渲染走普通方块模型（BaseEntityBlock 默认 INVISIBLE）。 */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConverterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new ConverterMenu(id, inventory, player, ContainerLevelAccess.create(level, pos)),
                Component.translatable("container.farmstorm.converter"));
    }

    /**
     * 裁定 #19 / D-170：v0.2.0 起转换器「暂时移除功能，只作为装饰方块」——右键无反应、不开界面。
     * 用 PASS 而不是 FAIL / SUCCESS / CONSUME：后三者会吞掉右键、挡住手上物品对该格的使用（36 号 §2.9）。
     * 方块 / 方块实体 / 菜单链 / 29 条配方 / 创造栏条目全部保留，将来恢复时只需把方法体写回来。
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    /** 拆方块不吞物品（TECH §8.3）；被活塞推动时内容物随方块实体一起走，不落地复制。 */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !movedByPiston
                && level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
