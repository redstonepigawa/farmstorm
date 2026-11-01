package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.outpost.OutpostCoreIndex;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 据点核心方块（D-119 / D-122）。
 *
 * <p>注册属性（硬度 30 / 抗爆 1200 / 任意镐可挖 / 不可推动）在 {@code FarmstormBlocks} 的注册处声明；
 * 本类负责方块面上的四件事：放置上限、归属写入、归属释放、服务端 ticker。
 * 3 × 3 范围判定、边界粒子与拆核清算是方块实体与索引的事。
 *
 * <p>{@code getDestroyProgress} 刻意不覆写：走 {@code BlockBehaviour#getDestroyProgress} 的原版公式，
 * 不给己方开秒拆旁路（D-119 补充：任意镐挖得动但很久，硬度 30 × 1.5 = 45 秒基础挖掘时间）。
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

    /** 服务端 ticker（先例 {@code block/DeployedCropBlock.java:146-158}）；客户端永不 tick（HC-03）。 */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof OutpostCoreBlockEntity core) {
                OutpostCoreBlockEntity.serverTick(tickLevel, pos, tickState, core);
            }
        };
    }

    /**
     * 放置上限（D-122 第 1、2 项）：名额已满时**拒绝放置** —— 返回 {@code null}。
     * 返回 {@code null} 之后 {@code BlockItem#getPlacementState} 也返回 {@code null}，
     * {@code BlockItem#place} 在 {@code itemstack.consume(1, player)}（{@code BlockItem.java:95}）之前
     * 就 {@code return InteractionResult.FAIL}（{@code :64-66}），因此物品不消耗、无掉落、也不做 FIFO 挤掉。
     * 客户端预测同样走这里（{@code player.getServer()} 为 {@code null} ⇒ 计数为 0 ⇒ 放行），
     * 真正的拒绝与幽灵方块的回收都在服务端（{@code ServerGamePacketListenerImpl.java:1143-1144} 发回原方块）。
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player != null && OutpostCoreIndex.countOwned(player) >= FarmstormConfig.outpostCoreMaxPerPlayer.get()) {
            return null;
        }
        return this.defaultBlockState();
    }

    /** 归属只在放置瞬间写入（先例 {@code block/DeployedCropBlock.java:296-303}）；索引在这里登记（F-153）。 */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core) {
            core.setOwner(player);
        }
    }

    /**
     * 拆方块时把归属交给 {@link OutpostCoreBlockEntity#release()}：从索引里摘掉这一条、名额当场释放
     * （D-122 补充）。{@code onRemove} 只在服务端、且在方块实体被移除之前调用
     * （{@code LevelChunk.java:272-277}），因此这里拿得到方块实体。
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core) {
            core.release();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
