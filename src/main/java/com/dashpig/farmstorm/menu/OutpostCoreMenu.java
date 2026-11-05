package com.dashpig.farmstorm.menu;

import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 据点核心的 54 格存储界面（D-166 / 36 号 §2.6.3）。
 *
 * <p>版式照原版双箱：{@code generic_54.png}（176 × 222），54 个存储格 6 行 × 9 列
 * （x = 8 + 18·col、y = 18 + 18·row），玩家背包 3 行 y = 139 / 157 / 175、快捷栏 y = 197 ——
 * 这四个 y 是原版 {@code ChestMenu} 在 6 行时的常量（{@code (rows - 4) * 18} 偏移 + 103 / 161），
 * 挪 1 像素就会与复用的原版贴图错格。<b>本节登记该偏差</b>：36 号 §2.6.3 写的 140 / 198 是按 1 像素错位记的，
 * 以原版源码为准（{@code ChestMenu.java:56-72}）。
 *
 * <p>槽数只在**创建时**读一次 {@code outpost_core.storageSlots}（值域钉死 54–54），
 * 并且再钳一次到 {@link OutpostCoreBlockEntity#SLOT_COUNT}，所以已打开的菜单与新开的菜单不可能不一致。
 * 客户端兜底容器是 {@code SimpleContainer(54)}，内容由服务端槽位同步覆盖。
 */
public class OutpostCoreMenu extends AbstractContainerMenu {
    /** 存储格 6 行 × 9 列 = 54（D-166）。<b>与培养器名额 24 不是一个数</b>（裁定 #8）。 */
    public static final int ROWS = 6;
    public static final int COLUMNS = 9;
    public static final int SLOT_COUNT = ROWS * COLUMNS;

    /** 玩家背包起点（= 54）与总槽数（54 + 27 + 9 = 90）。 */
    public static final int PLAYER_INVENTORY_START = SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int TOTAL_SLOTS = PLAYER_INVENTORY_START + 36;

    private static final int SLOT_SIZE = 18;
    private static final int CONTAINER_X = 8;
    private static final int CONTAINER_Y = 18;
    /** 原版 6 行箱子的玩家背包起始 y：{@code 103 + (6 - 4) * 18}。 */
    private static final int INVENTORY_Y = 139;
    /** 原版 6 行箱子的快捷栏 y：{@code 161 + (6 - 4) * 18}。 */
    private static final int HOTBAR_Y = 197;

    private final Container container;
    private final ContainerLevelAccess access;

    /** 客户端构造（MenuType 的 MenuSupplier）：兜底容器，内容由服务端槽位同步覆盖。 */
    public OutpostCoreMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(slotCount()), ContainerLevelAccess.NULL);
    }

    /** 服务端构造（方块的 {@code getMenuProvider} 用）：容器即方块实体本身。 */
    public OutpostCoreMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        this(id, inventory, resolveContainer(access), access);
    }

    private OutpostCoreMenu(int id, Inventory inventory, Container container, ContainerLevelAccess access) {
        super(FarmstormMenus.OUTPOST_CORE.get(), id);
        this.container = container;
        this.access = access;
        checkContainerSize(container, SLOT_COUNT);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                this.addSlot(new Slot(container, column + row * COLUMNS,
                        CONTAINER_X + column * SLOT_SIZE, CONTAINER_Y + row * SLOT_SIZE));
            }
        }
        addPlayerSlots(inventory);
    }

    /** 3 行主背包 + 1 行快捷栏（原版双箱版式）。 */
    private void addPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                this.addSlot(new Slot(inventory, column + row * COLUMNS + 9,
                        CONTAINER_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < COLUMNS; column++) {
            this.addSlot(new Slot(inventory, column, CONTAINER_X + column * SLOT_SIZE, HOTBAR_Y));
        }
    }

    /** 槽数：只在创建时读一次配置，并钳到 54（36 号 §2.6.3 的口径；格数在本版恒为 54）。 */
    private static int slotCount() {
        return Math.min(SLOT_COUNT, FarmstormConfig.outpostCoreStorageSlots.get());
    }

    private static Container resolveContainer(ContainerLevelAccess access) {
        Container fallback = new SimpleContainer(slotCount());
        return access.evaluate((level, pos) ->
                level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core ? core : fallback, fallback);
    }

    /**
     * 两向搬运（0–53 ⇄ 54–89），区间划分照原版 {@code ChestMenu} 的两段（36 号 §9 ⑧）：
     * 存储格 → 背包；背包（含快捷栏）→ 存储格。槽位合法性由 {@code Slot#mayPlace} 兜底。
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack current = slot.getItem();
        result = current.copy();
        if (index < PLAYER_INVENTORY_START) {
            if (!this.moveItemStackTo(current, PLAYER_INVENTORY_START, TOTAL_SLOTS, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(current, 0, PLAYER_INVENTORY_START, false)) {
            return ItemStack.EMPTY;
        }
        if (current.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    /**
     * 裁定 #17 的 B 闸（触及范围）：用原版语义 {@code AbstractContainerMenu#stillValid(access, player, block)}
     * —— 它落到 {@code Container.stillValidBlockEntity} 的 4.0 距离缓冲，并顺带判「该格仍是这件方块」。
     * 不新设自定义距离常量（3 × 3 区块的 {@code RANGE_CHUNKS} 是绑定 / 连线的范围，与手能不能碰到这一格是两回事）。
     */
    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, FarmstormBlocks.OUTPOST_CORE.get());
    }

    public Container container() {
        return this.container;
    }
}
