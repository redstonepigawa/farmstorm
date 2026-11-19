package com.dashpig.farmstorm.block;

import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.network.CultivatorLinkPayload.Link;
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
 * 逐项照农作物培养器与原版铁砧。本类负责四件事：{@code facing} 状态属性、普通方块模型渲染、
 * {@code newBlockEntity}（{@code CODEC} 与 {@code codec()} 照其余带方块实体的方块同形）、放置绑定。
 *
 * <p>模式三态在方块实体侧（F-333 已落）；放置绑定已落（F-336），终端**不写 {@code onRemove}**：
 * 它没有内部槽位、无需掉落，核侧名单由 {@link OutpostCoreBlockEntity#pruneCultivators} 惰性清理；
 * 三个解绑出口（接替核改绑 / 清 {@code Core} 留原地 / 区块未加载保留）全在核的
 * {@code settleOnRemoval} 里，与方块类无关。{@code getTicker}（投料循环）归 F-340，
 * {@code getMenuProvider} / {@code useWithoutItem} 归 F-337。
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

    /**
     * 放置绑定（F-336）：选「**覆盖它 ∧ 己方 ∧ 未满**」的核里最近的那一个，两侧都写
     * ——核的 {@code Cultivators} 列表（唯一真相）+ 终端的 {@code Core} 引用缓存。
     *
     * <p>选核的四判据与平手键全在 {@link OutpostCoreBlockEntity#selectCore}，本方法只管两件事：
     * 先把核侧写成功，再写终端侧（两侧不一致时按未绑定处理，顺序反过来就会留下「核说没绑、
     * 终端说绑了」的假象）。
     *
     * <p><b>没有可用核时方块照样放下</b>，此刻处于未绑定态（{@code Core} 缺键）、不可使用；但核每
     * 100 刻会扫一遍 3 × 3 已加载区块，把「没人要」的终端补进自己的名额（{@code rebindNearby}，
     * 终端已在 {@code isProcessingBlock} 标签与 kind 表内，自动可用）。补绑只补未绑定的、不夺已绑的。
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
            return;                                        // 未绑定：不写 Core 键（缺键就是未绑定）
        }
        if (core.addCultivator(pos, Link.Kind.TERMINAL)) {  // 核侧先写成功
            terminal.setCore(core.getBlockPos());          // 再写终端侧缓存（顺序不可换）
        }
    }
}
