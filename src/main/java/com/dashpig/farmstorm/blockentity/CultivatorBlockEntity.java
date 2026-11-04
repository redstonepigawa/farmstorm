package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 农作物培养器方块实体（D-160 ~ D-164）。F-224 落骨架，F-232 补 {@code Core} 的写入面与落盘。
 *
 * <p><b>刻意不实现 {@code Container}</b>（36 号 §2.5.2）：D-160 没有菜单，
 * 一旦实现 Container，漏斗 / 投掷器就能绕过 {@code useWithoutItem} 塞入史诗作物，D-162 的准入面当场失守。
 *
 * <p>字段按 36 号 §2.5.2 先声明，{@code Held} / {@code Progress} 与状态机在 F-235，
 * 准入与产出在 F-235 / F-236，破坏掉落在 F-237。{@code corePos} 是<b>引用缓存</b>：
 * 绑定关系的唯一真相在核侧（D-165），使用前要用核侧列表校验。
 */
public class CultivatorBlockEntity extends BlockEntity {
    /** 绑定核坐标的 NBT 键（36 号 §6 ④）：{@code long}（{@link BlockPos#asLong()}）；**缺键 = 未绑定**。 */
    private static final String TAG_CORE = "Core";

    /** 槽内培养物；不叠加、不替换（第四道准入闸，F-235）。 */
    private ItemStack held = ItemStack.EMPTY;
    /** 已累计刻数；等于 {@code cultivator.cycleTicks} 时本刻结算一次（F-236）。 */
    private int progress;
    /** 绑定的核心坐标；{@code null} = 未绑定（D-165：引用缓存，真相在核侧）。 */
    @Nullable
    private BlockPos corePos;

    public CultivatorBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.CULTIVATOR.get(), pos, state);
    }

    /** 引用缓存只读视图；{@code null} = 未绑定。 */
    @Nullable
    public BlockPos corePos() {
        return this.corePos;
    }

    /**
     * 写入引用缓存（放置绑定 / 迁移改绑的**唯一**入口，D-165 的两个写入时点）。
     * 核侧列表由调用方先写成功再调这里 —— 两侧不一致时按未绑定处理，所以顺序是「先核后培」。
     */
    public void setCore(BlockPos pos) {
        this.corePos = pos;
        this.setChanged();
        this.syncCore();
    }

    /**
     * 清掉引用缓存（F-233 出口②：核被拆且没有接替核 ⇒ 留原地、内容保留、{@code Core} 键消失、不可使用）。
     * 不清 {@code Held} / {@code Progress}（D-168 补充）。
     */
    public void clearCore() {
        if (this.corePos == null) {
            return;
        }
        this.corePos = null;
        this.setChanged();
        this.syncCore();
    }

    /** 绑定变化的同步时点（36 号 §2.6.1 的同一条纪律）：与核侧的 {@code sendBlockUpdated} 成对。 */
    private void syncCore() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // 未绑定时不写这个键（36 号 §6 ④：缺键 = 未绑定），验收靠「缺 Core 键」读出来。
        if (this.corePos != null) {
            tag.putLong(TAG_CORE, this.corePos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.corePos = tag.contains(TAG_CORE) ? BlockPos.of(tag.getLong(TAG_CORE)) : null;
    }

    /**
     * 服务端每刻的入口（{@code CultivatorBlock#getTicker} 注册）。
     * 状态机在 F-235 落地：空槽 → 不可用 → 累计 → 结算四步，本任务留空实现。
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, CultivatorBlockEntity cultivator) {
    }
}
