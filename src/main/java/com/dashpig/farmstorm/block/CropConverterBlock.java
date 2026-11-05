package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.CropConverterBlockEntity;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.team.TeamResolver;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 农作物转换器方块（D-206 ~ D-211）。带水平朝向；转换事务在 {@link CropConverterBlockEntity} 上。
 * 方块属性（铁砧七项，D-207）在 {@code FarmstormBlocks} 注册处声明；v0.2.3 起没有界面（D-208）。
 *
 * <p>本类负责五件事：{@code working} 状态属性、普通方块模型渲染、放置绑定、右键投料、服务端 ticker。
 * {@code working} 同时是 blockstate 两个 variant 的判据与两张模型的选择器——动画本身由「双贴图 +
 * blockstate 属性」承担，不靠代码暂停（D-211 / 红线 4）。
 */
public class CropConverterBlock extends BaseEntityBlock {
    /** 水平四向朝向（blockstate 的 y 旋转靠它）。 */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 工作态属性（D-211）：{@code false} 引静态图、{@code true} 引 16 帧帧条。 */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public static final MapCodec<CropConverterBlock> CODEC = simpleCodec(CropConverterBlock::new);

    public CropConverterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WORKING, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** BaseEntityBlock 默认 INVISIBLE：两张模型都是完整的 cube，必须显式声明走普通方块模型。 */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WORKING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CropConverterBlockEntity(pos, state);
    }

    /**
     * 放置绑定（D-208，形态照培养器的同名方法）：选「覆盖它 ∧ 己方 ∧ 未满」的核里
     * 最近的那一个，两侧都写——核的 {@code Cultivators} 列表（唯一真相）+ 转换器的 {@code Core} 引用缓存。
     *
     * <p>先核后机器：两侧不一致时按未绑定处理，顺序反过来会留下「核说没绑、机器说绑了」的假象。
     * <b>没有可用核时方块照样放下</b>，此刻未绑定、不可使用；补绑（核每 100 刻扫一遍 3 × 3 区块）是
     * F-303 的写入点，本阶段按现状只写单参 {@code addCultivator(pos)}。
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server) || !(placer instanceof Player player)) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof CropConverterBlockEntity converter)) {
            return;
        }
        OutpostCoreBlockEntity core = OutpostCoreBlockEntity.selectCore(server, pos,
                new TeamResolver.Owner(player.getUUID(), player.getScoreboardName()), null);
        if (core == null) {
            return;                                   // 未绑定：不写 Core 键（缺键就是未绑定）
        }
        if (core.addCultivator(pos)) {
            converter.setCore(core.getBlockPos());
        }
    }

    /**
     * 放入动作（D-208 / 54 号 §4.1.3）：没有菜单，右键把手上那**一个**放进唯一的槽。
     *
     * <p>顺序：客户端先 {@code SUCCESS}（「右键无反应」的定义是行为面无反应，不是动画面无反应）→
     * 服务端四道闸（范围 / 绑定 / 手持 / 空槽）**合并成一次判定**，任一不过就 {@code PASS}
     * （不消耗、不提示、不报错）→ 通过则 {@code split(1)} + {@code CONSUME}。
     *
     * <p>空手右键 / 槽里有货时右键**都不取出**（四种情形的对外表现都是 {@code PASS}）：要取回只能拆方块。
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof CropConverterBlockEntity converter)) {
            return InteractionResult.PASS;
        }
        ItemStack heldStack = player.getMainHandItem();
        if (!converter.canInsert(heldStack)) {
            return InteractionResult.PASS;
        }
        converter.insert(heldStack.split(1));
        return InteractionResult.CONSUME;
    }

    /**
     * 破坏掉落（54 号 §4.1.4 第一种边界）：真移除（{@code !state.is(newState.getBlock())}）∧ 非活塞推动
     * （{@code !movedByPiston}）⇒ 槽里那**一个**原样掉落，{@code progress} 归零。
     *
     * <p>第一条同时挡住「{@code working} 属性变化触发的那次移除回调」——属性变化时新旧 state 属于同一个
     * 方块，{@code state.is(newState.getBlock())} 为真。用 {@code Containers.dropContents} 的
     * {@link net.minecraft.core.NonNullList} 重载而不是 {@code Container} 那个：本方块实体**不实现**
     * {@code Container}（D-208 补充）。方块本体由战利品表掉，两处不重叠。
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !movedByPiston
                && level.getBlockEntity(pos) instanceof CropConverterBlockEntity converter) {
            Containers.dropContents(level, pos, converter.releaseHeld());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** 服务端 ticker（形态与 {@code CultivatorBlock} 逐字同形）；客户端永不 tick。 */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof CropConverterBlockEntity converter) {
                CropConverterBlockEntity.serverTick(tickLevel, pos, tickState, converter);
            }
        };
    }
}
