package com.dashpig.farmstorm.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * 控制终端方块（v0.3.0 按 D-259 新增）：本版只落注册面，行为分在后续任务。
 *
 * <p>注册属性（{@code strength(5.0F, 1200.0F)} / {@code MapColor.METAL} / 需正确工具 /
 * {@code SoundType.ANVIL} / {@code PushReaction.BLOCK}）在 {@code FarmstormBlocks} 的注册处声明，
 * 逐项照农作物培养器与原版铁砧。本类只有两件事：{@code facing} 状态属性、普通方块模型渲染。
 *
 * <p>模式三态、投料、绑定在后续任务落地：{@code newBlockEntity} / {@code getTicker} 归 F-333，
 * {@code setPlacedBy} / {@code onRemove} 归 F-336，{@code getMenuProvider} / {@code useWithoutItem} 归 F-337。
 */
public class ControlTerminalBlock extends Block {
    /** 水平四向朝向（blockstate 的 y 旋转靠它）；默认值 north 由属性自身给出。 */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ControlTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** 控制终端的几何是普通方块模型（不是实体渲染）：必须显式声明，否则客户端什么都不画。 */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
