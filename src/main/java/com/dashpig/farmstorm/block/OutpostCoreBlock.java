package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.menu.OutpostCoreMenu;
import com.dashpig.farmstorm.outpost.OutpostCoreIndex;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
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
import net.minecraft.world.phys.BlockHitResult;

/**
 * 据点核心方块（D-119 / D-122）。
 *
 * <p>注册属性（硬度 30 / 抗爆 1200 / 任意镐可挖 / 不可推动）在 {@code FarmstormBlocks} 的注册处声明；
 * 本类负责方块面上的五件事：放置上限、归属写入、开箱的第一道闸、归属释放、服务端 ticker。
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
     * 核心存储的菜单（F-229 / D-166 / 36 号 §2.6.4）：照 {@code CropConverterBlock#getMenuProvider} 的形态，
     * 容器由 {@link ContainerLevelAccess} 在服务端解析成 {@link OutpostCoreBlockEntity} 本身
     * （{@link OutpostCoreMenu#resolveContainer}），标题键 {@code container.farmstorm.outpost_core}。
     */
    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new OutpostCoreMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                Component.translatable("container.farmstorm.outpost_core"));
    }

    /**
     * 开箱的 A 闸（裁定 #17 / D-166 补充 / 36 号 §2.6.4）：**只判「己方」**。
     *
     * <p>非己方返回 {@link InteractionResult#PASS}——「我不处理」，把手感交回原版，既不吞右键也不挡住
     * 手上物品对该格的使用；返回 {@code SUCCESS} / {@code FAIL} 都会吞掉右键。
     *
     * <p><b>这里不判距离</b>：B 闸（触及范围）由菜单每次 tick 的 {@code stillValid} 承担，
     * 落到原版 {@code Container.stillValidBlockEntity} 的默认距离参数（{@code 4.0F}），与本版既有裁定一致
     * ——不新设距离常量、不新增 config 键、也不把 3 × 3 区块的 {@code RANGE_CHUNKS} 当开箱条件。
     * 客户端只返回预测结果、不做归属判定（HC-03）。
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core) || !core.isAlly(player)) {
            return InteractionResult.PASS;
        }
        MenuProvider provider = this.getMenuProvider(state, level, pos);
        if (provider == null) {
            return InteractionResult.PASS;
        }
        player.openMenu(provider);
        return InteractionResult.CONSUME;
    }

    /**
     * 拆方块的**掉落面**（F-230 / D-167 / 36 号 §2.6.4）：四个条件一起写，缺一个就是漏洞。
     *
     * <ol>
     *   <li><b>真移除</b>：{@code !state.is(newState.getBlock())} —— 区块卸载 / 停服不走这里，
     *       它们根本不换方块状态。</li>
     *   <li><b>非同方块替换</b>：同一个表达式，{@code /setblock} 成同一件方块时 {@code newState} 的方块相同，
     *       不掉落。</li>
     *   <li><b>非活塞推动</b>：{@code !movedByPiston}。漏这一条就是**物品复制漏洞** —— 活塞推动时方块实体
     *       连同内容物一起走到新坐标，这里再原地刷一份，等于凭空多一份。</li>
     *   <li><b>走 {@link Containers#dropContents}</b>：与原版箱子、{@code CropConverterBlock.java:88-96} 同一个入口，
     *       不自己遍历槽位。</li>
     * </ol>
     *
     * <p>掉落与「索引摘除 + 培养器迁移」是**两件事、两个钩子**（D-167 补充）：掉落在这里，
     * 索引与迁移在 {@code setRemoved} / {@code settleOnRemoval}。核心抗爆 1200、推不动（注册处
     * {@code PushReaction.BLOCK}），所以不另立爆炸 / 活塞分支。
     *
     * <p>{@code release()} 保持原样：从索引里摘掉这一条、名额当场释放（D-122 补充）；
     * {@code onRemove} 只在服务端、且在方块实体被移除之前调用（{@code LevelChunk.java:272-277}），
     * 因此这里拿得到方块实体。
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core) {
            if (!movedByPiston) {
                Containers.dropContents(level, pos, core);
            }
            core.release();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
