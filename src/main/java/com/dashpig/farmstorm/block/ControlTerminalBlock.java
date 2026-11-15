package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * 控制终端方块（F-024 / D-020 / D-021 / D-022）。
 *
 * <p>注册属性（{@code strength(5.0F, 1200.0F)} / {@code MapColor.METAL} / 需正确工具 /
 * {@code SoundType.ANVIL} / {@code PushReaction.BLOCK}）在 {@code FarmstormBlocks} 的注册处声明，
 * 逐项照原版铁砧（D-021）。本类此刻只负责水平朝向一项（D-022）：四个 variant 的 y 旋转由
 * {@code ModBlockStateProvider} 的 {@code facingRotationY} 给出，放置时正面朝玩家。
 *
 * <p><b>没有工作态属性</b>（D-022 的补充）：三面贴图自带 {@code .mcmeta}，
 * 动画由资源自己播、代码侧 0 行；多一个属性会要求 8 个 variant（验收 #4 / #27）。
 *
 * <p><b>父类仍是 {@code HorizontalDirectionalBlock}</b>（D-022 的 {@code FACING} 与 {@code rotate} / {@code mirror}
 * 都来自它），方块实体面经 {@link EntityBlock} 接口接上（{@code newBlockEntity} / {@code getTicker}，F-026）——
 * 两个既有父类 {@code BaseEntityBlock} 与 {@code HorizontalDirectionalBlock} 互不继承，走接口是唯一同时成立的写法，
 * 与 1.21.1 那个并不存在于此的 {@code DirectionPropertyBlock} 无关。放置绑定与右键开面板仍是 F-029 / F-030。
 */
public class ControlTerminalBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public ControlTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code FACING} 是父类的常量，这里只把它加进状态定义（D-022 的四值）。 */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** 放置时正面朝玩家：取水平方向的反向，照 {@code CropConverterBlock} 的同名方法。 */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    /** {@link EntityBlock} 的默认值是 {@code INVISIBLE}：模型是完整 cube，必须显式声明走普通方块模型（照 {@code CultivatorBlock:55-57}）。 */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControlTerminalBlockEntity(pos, state);
    }

    /** 服务端 ticker（与 {@code CultivatorBlock:153} 同形）；客户端永不 tick，所以客户端返回 {@code null}。 */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof ControlTerminalBlockEntity terminal) {
                ControlTerminalBlockEntity.serverTick(tickLevel, pos, tickState, terminal);
            }
        };
    }
}
