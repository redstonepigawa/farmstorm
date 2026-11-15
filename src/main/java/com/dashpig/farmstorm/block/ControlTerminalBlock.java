package com.dashpig.farmstorm.block;

import javax.annotation.Nullable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
 * <p>方块实体（{@code newBlockEntity} / {@code getTicker}）、放置绑定与右键开面板分别是 Phase 9 ~ 10
 * 的 F-026 / F-029 / F-030；本类因此还不是 {@code BaseEntityBlock}，形状照
 * {@code HorizontalDirectionalBlock} 体系（{@code FACING} 常量）而不是 1.21.1 的
 * {@code DirectionPropertyBlock}——后者在 1.20.1 全树 0 命中。
 */
public class ControlTerminalBlock extends HorizontalDirectionalBlock {
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
}
