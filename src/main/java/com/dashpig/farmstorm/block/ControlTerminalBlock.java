package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * 控制终端方块（v0.3.0 按 D-259 新增）：注册面与方块实体创建面。
 *
 * <p>注册属性（{@code strength(5.0F, 1200.0F)} / {@code MapColor.METAL} / 需正确工具 /
 * {@code SoundType.ANVIL} / {@code PushReaction.BLOCK}）在 {@code FarmstormBlocks} 的注册处声明，
 * 逐项照农作物培养器与原版铁砧。本类负责三件事：{@code facing} 状态属性、普通方块模型渲染、
 * {@code newBlockEntity}（{@code CODEC} 与 {@code codec()} 照其余带方块实体的方块同形）。
 *
 * <p>模式三态在方块实体侧（F-333 已落）；{@code getTicker}（投料循环）归 F-340，
 * {@code setPlacedBy} / {@code onRemove} 归 F-336，{@code getMenuProvider} / {@code useWithoutItem} 归 F-337。
 */
public class ControlTerminalBlock extends BaseEntityBlock {
    /** 水平四向朝向（blockstate 的 y 旋转靠它）；默认值 north 由属性自身给出。 */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<ControlTerminalBlock> CODEC = simpleCodec(ControlTerminalBlock::new);

    public ControlTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** BaseEntityBlock 默认 INVISIBLE：本终端是普通方块模型，必须显式声明。 */
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControlTerminalBlockEntity(pos, state);
    }
}
