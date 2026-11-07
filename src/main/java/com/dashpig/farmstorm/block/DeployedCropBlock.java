package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.explosion.BlastRule;
import com.dashpig.farmstorm.trap.TrapFifoIndex;
import com.dashpig.farmstorm.util.CropProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;

/**
 * 5 个部署方块的唯一实现（TECH §4.4）。
 * 附着面判定、悬空维护、归属校验、硬挖时长与 tick 分发都在这里；玩法效果在 BlockEntity 的行为层。
 */
public class DeployedCropBlock extends BaseEntityBlock {
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    /** 仅 WALL 有意义；FLOOR / CEILING 固定 NORTH（§4.4）。 */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 依附方块消失后置 true（只有 supportsFloating 的方块会读它）。 */
    public static final BooleanProperty FLOATING = BooleanProperty.create("floating");

    private final CropProfile profile;
    private final DeployedKind kind;
    private final Set<AttachFace> allowedFaces;
    private final boolean supportsFloating;
    private final boolean turretHardMine;
    private final boolean explosionImmune;
    /** D-91：与 {@link #supportsFloating} 正交的「从一开始就不依附」；只有 {@code vibration_bean} 传 true。 */
    private final boolean unattached;
    private final boolean allyOnlyBreak;

    public DeployedCropBlock(CropProfile profile, boolean supportsFloating, boolean turretHardMine, boolean explosionImmune,
                             boolean unattached, BlockBehaviour.Properties properties) {
        super(properties);
        this.profile = profile;
        this.kind = profile.kind() == null ? DeployedKind.BLAST_CARROT : profile.kind();
        this.allowedFaces = profile.allowedFaces();
        this.supportsFloating = supportsFloating;
        this.turretHardMine = turretHardMine;
        this.explosionImmune = explosionImmune;
        this.unattached = unattached;
        // D-14：只有火把花 / 瓶子草既爆炸免疫又「非己方不可挖」，两个标志恒同真同假。
        this.allyOnlyBreak = explosionImmune;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
                .setValue(FLOATING, false));
    }

    public CropProfile profile() {
        return this.profile;
    }

    public DeployedKind kind() {
        return this.kind;
    }

    public boolean supportsFloating() {
        return this.supportsFloating;
    }

    public boolean turretHardMine() {
        return this.turretHardMine;
    }

    public boolean explosionImmune() {
        return this.explosionImmune;
    }

    /** 「从一开始就不依附」（D-91 §7.1）：永不读写成名 FLOATING，也不参与依附面判定。 */
    public boolean unattached() {
        return this.unattached;
    }

    public boolean allyOnlyBreak() {
        return this.allyOnlyBreak;
    }

    /**
     * BaseEntityBlock 默认返回 INVISIBLE（指望你自己挂 BlockEntityRenderer），
     * 于是方块模型根本不会被渲染——炮台类放下后「透明」的真正原因。
     * 隐形陷阱不受影响：它们走 MODEL 但模型是空的（无 elements）。
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, FLOATING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeployedCropBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 客户端永不 tick（HC-03）。RT-04：5 个类型共用一个类，用 instanceof 分派而不是泛型强转。
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof DeployedCropBlockEntity deployed) {
                DeployedCropBlockEntity.serverTick(tickLevel, pos, tickState, deployed);
            }
        };
    }

    /** 放置规则（TECH §4.4）：附着面白名单 → 依附方块完整不透明 → 目标位置可替换 → FLOATING 初值 false。 */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clicked = ctx.getClickedFace();
        AttachFace face = attachFaceOf(clicked);
        if (!this.allowedFaces.contains(face)) {
            return null;
        }
        BlockPos pos = ctx.getClickedPos();
        BlockPos supportPos = pos.relative(clicked.getOpposite());
        Level level = ctx.getLevel();
        if (!isSupportValid(level.getBlockState(supportPos), level, supportPos, attachDirection(face, clicked))) {
            return null;
        }
        if (!level.getBlockState(pos).canBeReplaced(ctx)) {
            return null;
        }
        // F-25 放置限流：拒绝发生在这里，因此物品既不消耗也不产生掉落。
        if (!DeployedCropBlockEntity.canDeploy(level, pos, ctx.getPlayer())) {
            return null;
        }
        // F-149（D-115）：陷阱名额满时先挤掉队首，再放行——挤出必须排在写入新方块之前，
        // 而 getStateForPlacement 是唯一一处「判定早于扣物品」的位置（F-25 的注释同上）。
        if (level instanceof ServerLevel server) {
            TrapFifoIndex.makeRoom(server, ctx.getPlayer(), this.kind);
        }
        Direction facing = face == AttachFace.WALL ? clicked : Direction.NORTH;
        return this.defaultBlockState().setValue(FACE, face).setValue(FACING, facing).setValue(FLOATING, false);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // D-91：不依附模式没有依附面可查，恒真（BlockItem#canPlace / updateOrDestroy / FallingBlock 都拿到 true）。
        if (this.unattached) {
            return true;
        }
        BlockPos supportPos = supportPos(state, pos);
        return isSupportValid(level.getBlockState(supportPos), level, supportPos, supportDirection(state));
    }

    /**
     * 依附方块消失：可悬空的转 FLOATING，其余的直接变空气并按 G-9 掉落本体。
     * 不依附模式（D-91）不看方向、也不看支撑，任何更新都返回自身实例。
     */
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                    BlockPos pos, BlockPos neighborPos) {
        // D-91：返回同一实例 ⇒ Block#updateOrDestroy 的 newState != state 为 false ⇒ 既不 destroyBlock 也不 setBlock。
        // 必须早于下面 direction != supportDirection(state) 的判断：不依附方块的 6 个方向都要走这条。
        if (this.unattached) {
            return state;
        }
        if (direction != supportDirection(state)) {
            return state;
        }
        if (isSupportValid(neighbor, level, neighborPos, direction)) {
            return state;
        }
        if (this.supportsFloating) {
            if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof DeployedCropBlockEntity deployed) {
                deployed.markDetached();
            }
            return state.setValue(FLOATING, true);
        }
        // 「先自己摘、再自己掉一次」是唯一确定性的做法：
        // 若只返回 AIR 让别人处理，掉落会看调用方的脸色——实测同一次破坏里
        // NeighborUpdater#executeShapeUpdate → Block#updateOrDestroy → Level#destroyBlock(pos, true) 会掉一次，
        // 但在批量/同 tick 多次形变时可能根本不走 destroyBlock（方块凭空消失、0 掉落，实测到过）。
        // 所以：先 removeBlock(pos, false) 把方块无掉落摘掉（随后的 AIR 施加变成空操作），
        // 再由这里按配置决定是否 dropResources 一次 —— 恰好一次，与调用方无关。
        if (level instanceof ServerLevel server) {
            boolean drop = FarmstormConfig.deployDropOnSupportLost.get();
            if (drop) {
                // 战利品表优先；万一它返回空（实测在「同 tick 批量形变」这条路径上出现过 0 掉落），
                // 兜底直接掉方块本体，保证「依附丢失 = 恰好 1 个本体」这条语义不落空。
                java.util.List<net.minecraft.world.item.ItemStack> drops =
                        Block.getDrops(state, server, pos, level.getBlockEntity(pos));
                if (drops.isEmpty()) {
                    drops = java.util.List.of(new net.minecraft.world.item.ItemStack(state.getBlock()));
                }
                for (net.minecraft.world.item.ItemStack stack : drops) {
                    Block.popResource(server, pos, stack);
                }
            }
            server.removeBlock(pos, false);
        }
        return Blocks.AIR.defaultBlockState();
    }

    /**
     * 硬挖规则（D-13 / D-37 / C-8）：
     * pumpkin_turret 固定 10 秒（无视工具与附魔）；火把花 / 瓶子草非己方返回 0；其余走原版公式。
     * 必须是纯函数（RT-02）：只读方块字段与已同步的 BlockEntity，不读 gameTime、不写世界。
     */
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (this.turretHardMine) {
            return 1.0F / Math.max(1, FarmstormConfig.deployTurretMiningTicks.get());
        }
        if (!this.allyOnlyBreak) {
            return super.getDestroyProgress(state, player, level, pos);
        }
        if (level.getBlockEntity(pos) instanceof DeployedCropBlockEntity deployed && !deployed.isAlly(player)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    /**
     * D-14「任何爆炸都无法破坏」：火把花 / 瓶子草的抗性取 T-05 底线值 3.6e6，
     * 其它模组的爆炸走原版 Explosion 时读的就是这里；陷阱类保持可被爆炸摧毁（DESIGN §4.2）。
     */
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        if (this.explosionImmune) {
            return BlastRule.UNBREAKABLE_RESISTANCE;
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    /**
     * D-36 的二次校验：1.21.1 的 playerWillDestroy 无法取消破坏，真正「拦住」的点是
     * IBlockExtension#onDestroyedByPlayer 返回 false —— 创造模式与生存模式都会经过这里。
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
                                       FluidState fluid) {
        if (this.allyOnlyBreak && level.getBlockEntity(pos) instanceof DeployedCropBlockEntity deployed
                && !deployed.isAlly(player)) {
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    /** 归属只在放置瞬间写入，且只写 ownerUuid + ownerName（C-9）。 */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof DeployedCropBlockEntity deployed) {
            deployed.setOwner(player);
        }
    }

    /** 依附方块必须「完整不透明」并按附着方向托得住部署物（plan 第 11 行 / §4.4 规则 2）。 */
    private static boolean isSupportValid(BlockState support, BlockGetter level, BlockPos supportPos, Direction face) {
        if (FarmstormConfig.deploySupportMustBeFullOpaque.get()
                && !(support.isCollisionShapeFullBlock(level, supportPos) && support.canOcclude())) {
            return false;
        }
        return support.isFaceSturdy(level, supportPos, face);
    }

    private static AttachFace attachFaceOf(Direction clicked) {
        if (clicked == Direction.UP) {
            return AttachFace.FLOOR;
        }
        if (clicked == Direction.DOWN) {
            return AttachFace.CEILING;
        }
        return AttachFace.WALL;
    }

    /** 依附方块托住部署物的那一面：地板朝上、天花板朝下、墙朝部署物。 */
    private static Direction attachDirection(AttachFace face, Direction clicked) {
        return switch (face) {
            case FLOOR -> Direction.UP;
            case CEILING -> Direction.DOWN;
            case WALL -> clicked;
        };
    }

    /** 部署物指向依附方块的方向（updateShape / canSurvive 用它找依附方块）。 */
    private static Direction supportDirection(BlockState state) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            case WALL -> state.getValue(FACING).getOpposite();
        };
    }

    private static BlockPos supportPos(BlockState state, BlockPos pos) {
        return pos.relative(supportDirection(state));
    }
}
