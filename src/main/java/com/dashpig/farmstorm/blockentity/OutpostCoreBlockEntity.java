package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 据点核心方块实体（D-119 / D-121 / D-122）。F-138 只落编译期骨架。
 * 三个字段（ownerUuid / ownerName / placedGameTime）与归属解析在 F-154，玩家到核心坐标的索引在 F-153，
 * 每 40 刻一轮的边界粒子在 F-155，拆核时范围内己方耕地转泥土在 F-159。
 */
public class OutpostCoreBlockEntity extends BlockEntity {
    public OutpostCoreBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.OUTPOST_CORE.get(), pos, state);
    }
}
