package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.CultivatorBlockEntity;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.team.TeamResolver;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * 农作物培养器方块（D-160 / D-161）。
 *
 * <p>注册属性（{@code strength(5.0F, 1200.0F)} / {@code MapColor.METAL} / 需正确工具 /
 * {@code SoundType.ANVIL} / {@code PushReaction.BLOCK}）在 {@code FarmstormBlocks} 的注册处声明，
 * 逐项照原版铁砧。本类负责四件事：{@code working} 状态属性、普通方块模型渲染、放置绑定、服务端 ticker。
 *
 * <p><b>无菜单</b>（D-160）：放入动作走 {@code useWithoutItem}（F-235），右键直接放进槽里，
 * 不注册 {@code MenuType}——{@code menu_types} 的 +1 只给据点核心存储。
 *
 * <p>{@code working} 只在值真变化时写一次 blockstate（{@code setWorking} 在方块实体侧，F-235），
 * 动画本身由「双贴图 + blockstate 属性」承担，不靠代码暂停（36 号 §2.5.5 / 陷阱 27）。
 */
public class CultivatorBlock extends BaseEntityBlock {
    /** 工作态属性（D-161）：它同时是 blockstate 的两个 variant 的判据与两张模型的选择器。 */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public static final MapCodec<CultivatorBlock> CODEC = simpleCodec(CultivatorBlock::new);

    public CultivatorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WORKING, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /** BaseEntityBlock 默认 INVISIBLE：模型是完整的 cube_bottom_top，必须显式声明走普通方块模型。 */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WORKING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CultivatorBlockEntity(pos, state);
    }

    /**
     * 放置绑定（F-232 / D-165）：选「**覆盖它 ∧ 己方 ∧ 未满**」的核里最近的那一个，两侧都写
     * ——核的 {@code Cultivators} 列表（唯一真相）+ 培养器的 {@code Core} 引用缓存。
     *
     * <p>选核的四判据与平手键全在 {@link OutpostCoreBlockEntity#selectCore}，本方法只管两件事：
     * 先把核侧写成功，再写培养器侧（两侧不一致时按未绑定处理，顺序反过来就会留下「核说没绑、
     * 培养器说绑了」的假象）。
     *
     * <p><b>没有可用核时方块照样放下</b>，但永久处于未绑定态、不可使用，且**不自动认领**
     * ——事后在旁边补一个核也不会绑上（D-165 补充 / 35 号 §12 B-12、B-13）。刻意的：
     * 补绑会让「我放的时候有没有核」这件事失去意义。
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server) || !(placer instanceof Player player)) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof CultivatorBlockEntity cultivator)) {
            return;
        }
        OutpostCoreBlockEntity core = OutpostCoreBlockEntity.selectCore(server, pos,
                new TeamResolver.Owner(player.getUUID(), player.getScoreboardName()), null);
        if (core == null) {
            return;                                   // 未绑定：不写 Core 键（缺键就是未绑定）
        }
        if (core.addCultivator(pos)) {
            cultivator.setCore(core.getBlockPos());
        }
    }

    /** 服务端 ticker（与 {@code OutpostCoreBlock} 逐字同形）；客户端永不 tick（HC-03）。 */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof CultivatorBlockEntity cultivator) {
                CultivatorBlockEntity.serverTick(tickLevel, pos, tickState, cultivator);
            }
        };
    }
}
