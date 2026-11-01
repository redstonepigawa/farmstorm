package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 据点核心方块（D-119）。F-138 只落编译期骨架。
 * 注册属性（硬度 30 / 抗爆 1200 / 任意镐可挖 / 不可推动）在 FarmstormBlocks 的注册处声明；
 * 归属写入与每人 2 个上限在 F-152，3 x 3 范围查询在 F-154，边界粒子在 F-155。
 */
public class OutpostCoreBlock extends BaseEntityBlock {
    public static final MapCodec<OutpostCoreBlock> CODEC = simpleCodec(OutpostCoreBlock::new);

    public OutpostCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** BaseEntityBlock 默认 INVISIBLE（08-DEV-GUIDE §7 第 1 行）：方块模型在 F-166 落地，这里先声明走普通模型。 */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OutpostCoreBlockEntity(pos, state);
    }

    /** 骨架阶段没有每刻要推进的状态：每 40 刻一轮的边界粒子在 F-155 挂上来时才返回 ticker（D-121）。 */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
