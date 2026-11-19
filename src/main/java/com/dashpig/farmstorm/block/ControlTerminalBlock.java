package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.menu.ControlTerminalMenu;
import com.dashpig.farmstorm.network.CultivatorLinkPayload.Link;
import com.dashpig.farmstorm.team.TeamResolver;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;

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

    /**
     * 放置绑定（F-029 / D-025 / D-026 / D-027）：照 {@code CultivatorBlock#setPlacedBy} 的三步，顺序不可换。
     *
     * <p>选核的四判据与平手键全在 {@link OutpostCoreBlockEntity#selectCore}，本方法只管两件事：
     * 先把核侧列表写成功（{@code addCultivator} 返回真），再写终端侧的 {@code Core} 引用缓存。
     * 反过来会在核侧返 {@code false}（已绑 / 名额满 24）时留下「核说没绑、终端说绑了」的假象。
     *
     * <p>没有可用核时方块照样放下、**不写 {@code Core} 键**（缺键就是未绑定），核侧
     * {@code rebindNearby} 会在 100 刻一轮的扫描里把没人要的终端补进名额（{@code KIND_BY_BLOCK} 已含
     * {@code TERMINAL}）。拆除时核侧那两条平行列表由既有惰性清理摘掉（标签成员含终端，F-028）。
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server) || !(placer instanceof Player player)) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof ControlTerminalBlockEntity terminal)) {
            return;
        }
        OutpostCoreBlockEntity core = OutpostCoreBlockEntity.selectCore(server, pos,
                new TeamResolver.Owner(player.getUUID(), player.getScoreboardName()), null);
        if (core == null) {
            // 未绑定：不写 Core 键（缺键就是未绑定）。日志是为了让「面板显示未绑定」可区分是
            // 「没选到核」还是「选到了但面板状态没同步」——这两种原因的修法完全不同。
            Farmstorm.LOGGER.info("控制终端 {} 未绑定：附近没有可用据点核心", pos.toShortString());
            return;
        }
        if (core.addCultivator(pos, Link.Kind.TERMINAL)) {
            terminal.setCore(core.getBlockPos());      // 先核侧、后终端侧
            Farmstorm.LOGGER.info("控制终端 {} 已绑定据点核心 {}", pos.toShortString(), core.getBlockPos().toShortString());
        } else {
            Farmstorm.LOGGER.info("控制终端 {} 绑定失败：据点核心 {} 名额已满或该格已登记",
                    pos.toShortString(), core.getBlockPos().toShortString());
        }
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

    /**
     * 右键开面板（F-030 / D-028 / 08 号 §4.2）：**三个出口一律不 PASS**——终端是「打开一个界面」，
     * 无反应等于让玩家拿着新方块学不到任何东西（机器那套「任一闸不过就 PASS」在这里不成立）。
     *
     * <p>客户端只返回预测结果：潜行右键 {@code PASS}（把手感交回原版，能贴着终端摆方块），其余
     * {@code SUCCESS}；服务端不判归属、不判绑定，直接 {@code player.openMenu}（未绑定时面板照开、
     * 两个按钮由屏幕侧置灰，D-028）。
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return player.isSecondaryUseActive() ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        // 面板一开就把「绑定到底卡在哪一步」打出来：corePos 缺 / 核方块实体在不在 / 核认不认这一格。
        // 玩家看到的永远是「未绑定」三个字，这三项才说明原因（选核没选中 vs 核没登记 vs 客户端没同步）。
        if (level.getBlockEntity(pos) instanceof ControlTerminalBlockEntity terminal) {
            BlockPos corePos = terminal.corePos();
            OutpostCoreBlockEntity core = corePos != null
                    && level.getBlockEntity(corePos) instanceof OutpostCoreBlockEntity found ? found : null;
            Farmstorm.LOGGER.info("控制终端 {} 面板状态：Mode={} corePos={} 核方块实体在={} 核认这一格={}",
                    pos.toShortString(), terminal.mode(), corePos, core != null,
                    core != null && core.hasCultivator(pos));
        }
        player.openMenu(this.getMenuProvider(state, level, pos));
        return InteractionResult.CONSUME;
    }

    /** 标题键 {@code container.farmstorm.control_terminal}；容器访问指向终端自己那一格（照 OutpostCoreBlock:106-110）。 */
    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new ControlTerminalMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                Component.translatable("container.farmstorm.control_terminal"));
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
