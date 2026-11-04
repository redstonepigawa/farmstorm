package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 农作物培养器方块实体（D-160 ~ D-164）。F-224 只落骨架。
 *
 * <p><b>刻意不实现 {@code Container}</b>（36 号 §2.5.2）：D-160 没有菜单，
 * 一旦实现 Container，漏斗 / 投掷器就能绕过 {@code useWithoutItem} 塞入史诗作物，D-162 的准入面当场失守。
 *
 * <p>字段按 36 号 §2.5.2 先声明，NBT 三键（{@code Held} / {@code Progress} / {@code Core}）与状态机在 F-235，
 * 准入与产出在 F-235 / F-236，破坏掉落在 F-237。{@code corePos} 是<b>引用缓存</b>：
 * 绑定关系的唯一真相在核侧（D-165）。
 */
public class CultivatorBlockEntity extends BlockEntity {
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

    /**
     * 服务端每刻的入口（{@code CultivatorBlock#getTicker} 注册）。
     * 状态机在 F-235 落地：空槽 → 不可用 → 累计 → 结算四步，本任务留空实现。
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, CultivatorBlockEntity cultivator) {
    }
}
