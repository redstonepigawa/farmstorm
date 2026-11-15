package com.dashpig.farmstorm.menu;

import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity;
import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity.Mode;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * 控制终端的 0 槽面板（F-030 / D-028 / D-031 / D-034）。
 *
 * <p><b>槽位 0 个</b>：面板只有两个模式按钮与一行状态，所以这里一个槽位都不登记，
 * 两向搬运直接返回空表——0 槽菜单不会有 {@code QUICK_MOVE} 点击。不嵌核的 54 格，
 * 那等于第二个核存储界面（D-034 的否决项 7）。
 *
 * <p><b>状态走两格 {@code ContainerData}</b>（D-034）：格 0 是模式序号，格 1 是绑定状态。两格的
 * {@code get} 每次都经 {@link ContainerLevelAccess#evaluate} 现读终端方块实体（取不到返回 0 与 0），
 * {@code set} 是空实现——服务端是唯一真相，客户端不回写。这样不开第三个网络包。
 *
 * <p><b>构造必须带真实 {@code MenuType}</b>：{@code super(null, id)} 会让 {@link #getType()} 抛
 * {@code UnsupportedOperationException}（RT-015），而注册与开屏链路都要读它。
 */
public class ControlTerminalMenu extends AbstractContainerMenu {
    /** 按钮 id：0 = 培养模式、1 = 转换模式（D-031；与服务端的 {@link #clickMenuButton} 一一对应）。 */
    public static final int BUTTON_CULTIVATOR = 0;
    public static final int BUTTON_CONVERTER = 1;

    /** 两格 {@code ContainerData} 的下标（08 号 §4.6）：格 0 模式序号、格 1 绑定状态。 */
    private static final int DATA_MODE = 0;
    private static final int DATA_BOUND = 1;
    private static final int DATA_COUNT = 2;

    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** 客户端构造（{@code MenuType} 的 {@code MenuSupplier}）：没有可解析的方块实体，两格恒读 0。 */
    public ControlTerminalMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    /** 服务端构造（方块 {@code getMenuProvider} 用）：容器访问指向终端自己的那一格。 */
    public ControlTerminalMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(FarmstormMenus.CONTROL_TERMINAL.get(), id);
        this.access = access;
        this.data = new SimpleContainerData(DATA_COUNT) {
            @Override
            public int get(int index) {
                return ControlTerminalMenu.this.readData(index);
            }

            /** 只往客户端推，不回写：服务端每刻现读，客户端读到的也是它自己那一份。 */
            @Override
            public void set(int index, int value) {
            }
        };
        this.addDataSlots(this.data);
    }

    /** 0 槽菜单没有可搬运的槽位：{@code QUICK_MOVE} 不会发生，一律返回空表（D-034）。 */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    /**
     * 面板按钮（F-030 / 08 号 §4.5 的四步，顺序即短路顺序）：① id 不在 0~1 就假；② 取不到方块实体就假；
     * ③ 未绑定（核侧不认）就假；④ 否则按 D-031 迁移：点未选中的模式切过去，点已选中的那个退回 {@code OFF}。
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_CULTIVATOR && id != BUTTON_CONVERTER) {
            return false;
        }
        return this.access.evaluate((level, pos) -> {
            if (!(level.getBlockEntity(pos) instanceof ControlTerminalBlockEntity terminal)) {
                return false;
            }
            if (!isBound(terminal)) {
                return false;
            }
            Mode target = id == BUTTON_CULTIVATOR ? Mode.CULTIVATOR : Mode.CONVERTER;
            terminal.setMode(terminal.mode() == target ? Mode.OFF : target);
            return true;
        }, false);
    }

    /** 触及范围用原版语义 {@code stillValid(access, player, block)}，不新设距离常量（照 {@code OutpostCoreMenu:133-135}）。 */
    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, FarmstormBlocks.CONTROL_TERMINAL.get());
    }

    /** 当前模式（屏幕读这一格决定哪个按钮带选中框）。 */
    public Mode mode() {
        Mode[] modes = Mode.values();
        int index = this.data.get(DATA_MODE);
        return index >= 0 && index < modes.length ? modes[index] : Mode.OFF;
    }

    /** 绑定状态（屏幕读这一格决定两个按钮是否可点）。 */
    public boolean bound() {
        return this.data.get(DATA_BOUND) != 0;
    }

    /** 现读一格：取不到终端方块实体时两格都给 0（模式 {@code OFF}、未绑定）。 */
    private int readData(int index) {
        ControlTerminalBlockEntity terminal = this.terminal();
        if (terminal == null) {
            return 0;
        }
        return switch (index) {
            case DATA_MODE -> terminal.mode().ordinal();
            case DATA_BOUND -> isBound(terminal) ? 1 : 0;
            default -> 0;
        };
    }

    @Nullable
    private ControlTerminalBlockEntity terminal() {
        return this.access.evaluate((level, pos) ->
                level.getBlockEntity(pos) instanceof ControlTerminalBlockEntity terminal ? terminal : null, null);
    }

    /** 绑定判据只有一条：核侧那条列表里有这一格（D-025：核侧是唯一真相，终端只有引用缓存）。 */
    private static boolean isBound(ControlTerminalBlockEntity terminal) {
        BlockPos corePos = terminal.corePos();
        if (corePos == null || terminal.getLevel() == null) {
            return false;
        }
        return terminal.getLevel().getBlockEntity(corePos) instanceof OutpostCoreBlockEntity core
                && core.hasCultivator(terminal.getBlockPos());
    }
}
