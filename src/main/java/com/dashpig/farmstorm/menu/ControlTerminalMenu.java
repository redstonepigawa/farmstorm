package com.dashpig.farmstorm.menu;

import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity;
import com.dashpig.farmstorm.blockentity.ControlTerminalBlockEntity.Mode;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 控制终端的面板菜单（D-273 / F-337）：<b>0 槽</b>，只有两个模式按钮与两格同步数据。
 *
 * <p>同步走原版 {@link SimpleContainerData} + {@link AbstractContainerMenu#broadcastChanges()} 链路
 * （裁定 Q1=A，覆盖 66 号 §4.6 / 68 号陷阱 14 的旧写法）：菜单自己持两格数据，服务端每次广播前
 * 先把值读出来写进去，再由原版链路发包——<b>不新增网络包，也不覆写 {@code ContainerData#get/set}</b>。
 * 客户端那份 {@code access} 是 {@link ContainerLevelAccess#NULL}，{@code evaluate} 直接返回兜底值，
 * 所以客户端一次都不会去碰方块实体；{@code mode()} / {@code bound()} 只读这两格数据，客户端安全。
 *
 * <p>两格语义：格 0 = 模式序号（{@link Mode#ordinal()}：OFF 0 / CULTIVATOR 1 / CONVERTER 2），
 * 格 1 = 绑定状态（1 = 已认定绑定，0 = 未绑定）。「绑定」不是读终端自己的 {@code Core} 缓存键，
 * 而是四判据一起成立：{@code corePos()} 非空 ∧ 该位置是 {@link OutpostCoreBlockEntity} ∧
 * 核对 {@code hasCultivator(终端坐标)} 认这份关系 ∧ {@link OutpostCoreBlockEntity#inRange} 在范围内
 * ——引用缓存只是缓存（D-264），唯一真相在核侧列表。
 */
public class ControlTerminalMenu extends AbstractContainerMenu {
    /** 格 0：模式序号。 */
    public static final int DATA_MODE = 0;
    /** 格 1：绑定状态（1 = 已绑定）。 */
    public static final int DATA_BOUND = 1;
    /** 按钮 id 0：培养模式（再点一次回 OFF）。 */
    public static final int BUTTON_CULTIVATOR = 0;
    /** 按钮 id 1：转换模式（再点一次回 OFF）。 */
    public static final int BUTTON_CONVERTER = 1;

    private final ContainerLevelAccess access;

    /** 两格同步数据：本菜单自己持（裁定 Q1=A），值由 {@link #broadcastChanges()} 在服务端写入。 */
    private final SimpleContainerData data = new SimpleContainerData(2);

    /** 客户端构造（MenuType 的 MenuSupplier）：没有方块可查，{@link ContainerLevelAccess#NULL} 兜底。 */
    public ControlTerminalMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    /** 服务端构造（方块的 {@code getMenuProvider} 用）：{@code access} 指向终端自己那一格。 */
    public ControlTerminalMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(FarmstormMenus.CONTROL_TERMINAL.get(), id);
        this.access = access;
        this.addDataSlots(this.data);
    }

    /** 模式序号（{@link Mode#ordinal()}）；客户端可读。 */
    public int mode() {
        return this.data.get(DATA_MODE);
    }

    /** 是否已认定绑定；客户端可读。 */
    public boolean bound() {
        return this.data.get(DATA_BOUND) != 0;
    }

    /**
     * 0 槽菜单没有可搬运的槽位：整段搬运恒为空。
     *
     * <p>{@code Shift} + 点击由原版 {@code AbstractContainerMenu#moveItemStackTo} 兜底，此处不索引任何槽位，
     * 因此越界也不再是问题（本菜单的槽位表是空表）。
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    /** 触及范围走原版语义（4.0 距离缓冲 + 「该格仍是这件方块」）：面板随人走远而关。 */
    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, FarmstormBlocks.CONTROL_TERMINAL.get());
    }

    /**
     * 两格数据的刷新点（裁定 Q1=A：<b>不在客户端算</b>，两个按钮的置灰全靠这两格）。
     *
     * <p>服务端在这里读终端方块实体、判绑定、写进 {@code data}，然后交回
     * {@code super.broadcastChanges()}：值的变化由原版链路检测并发包，不新开网络包。
     *
     * <p>{@code evaluate} 的 lambda <b>必须返回非 null</b>：{@link ContainerLevelAccess#create}
     * 的实现内部是 {@code Optional.of(...)}，返回 null 会 NPE 并被网络层吞掉，表现成面板永远「未绑定」。
     * 客户端那份 {@code access} 是 {@link ContainerLevelAccess#NULL}，这里只剩兜底返回值，不会碰方块实体。
     */
    @Override
    public void broadcastChanges() {
        this.access.evaluate((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof ControlTerminalBlockEntity terminal) {
                this.data.set(DATA_MODE, terminal.mode().ordinal());
                this.data.set(DATA_BOUND, isBound(level, terminal) ? 1 : 0);
            }
            return Boolean.TRUE;
        }, Boolean.TRUE);
        super.broadcastChanges();
    }

    /**
     * 按钮（服务端）四步：id 不在 0~1 ⇒ 否；取不到终端方块实体 ⇒ 否；未绑定 ⇒ 否；否则按迁移表改模式。
     *
     * <p>迁移表（D-273）：id 0 当前是 {@code CULTIVATOR} 就回 {@code OFF}，否则切 {@code CULTIVATOR}；
     * id 1 当前是 {@code CONVERTER} 就回 {@code OFF}，否则切 {@code CONVERTER}。{@code setMode} 内部会
     * {@code setChanged}，两格数据不用在这里写——下一次 {@link #broadcastChanges()} 会重新读出来。
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < BUTTON_CULTIVATOR || id > BUTTON_CONVERTER) {
            return false;
        }
        return this.access.evaluate((level, pos) -> toggle(level, pos, id), Boolean.FALSE);
    }

    /** 绑定四判据（服务端）：见类注释；引用缓存不算数，核侧列表说了算。 */
    private static boolean isBound(Level level, ControlTerminalBlockEntity terminal) {
        BlockPos corePos = terminal.corePos();
        if (corePos == null) {
            return false;
        }
        if (!(level.getBlockEntity(corePos) instanceof OutpostCoreBlockEntity core)) {
            return false;
        }
        return core.hasCultivator(terminal.getBlockPos()) && OutpostCoreBlockEntity.inRange(corePos, terminal.getBlockPos());
    }

    /** 改模式的落点（服务端）：前三步任一不成立就原样退出，一个字都不改。 */
    private static boolean toggle(Level level, BlockPos pos, int id) {
        if (!(level.getBlockEntity(pos) instanceof ControlTerminalBlockEntity terminal)) {
            return false;
        }
        if (!isBound(level, terminal)) {
            return false;
        }
        Mode current = terminal.mode();
        if (id == BUTTON_CULTIVATOR) {
            terminal.setMode(current == Mode.CULTIVATOR ? Mode.OFF : Mode.CULTIVATOR);
        } else {
            terminal.setMode(current == Mode.CONVERTER ? Mode.OFF : Mode.CONVERTER);
        }
        return true;
    }
}
