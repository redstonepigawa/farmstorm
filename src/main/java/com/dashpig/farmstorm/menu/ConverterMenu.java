package com.dashpig.farmstorm.menu;

import com.dashpig.farmstorm.blockentity.ConverterBlockEntity;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 转换器界面（TECH §4.8.3 + FIX-4 §2.2）：槽 0 = 输入，槽 1 = 催化剂，槽 2 = 只读预览。
 * ContainerData 为 [xpCost, canConvert, reverse]，服务端按当前输入现算；
 * 按钮只有 {@link #BUTTON_CONVERT}，不携带任何可被伪造的参数（TECH §8.4）。
 */
public class ConverterMenu extends AbstractContainerMenu {
    public static final int BUTTON_CONVERT = 0;

    /** 槽位 id（= slots 列表下标）；前两个同时也是方块实体的容器下标。 */
    public static final int SLOT_INPUT = ConverterBlockEntity.SLOT_INPUT;
    public static final int SLOT_CATALYST = ConverterBlockEntity.SLOT_CATALYST;
    public static final int SLOT_PREVIEW = 2;
    private static final int PLAYER_INVENTORY_START = 3;

    // 三个槽位排在 y=22 的一行（贴图同高）：催化剂 (44,22) → 输入 (80,22) → 预览 (116,22)，
    // 下方 42..59 让给转换按钮（ConverterScreen 的 BUTTON_Y），再往下是物品栏分隔线 y=66。
    private static final int INPUT_X = 80;
    private static final int INPUT_Y = 22;
    private static final int CATALYST_X = 44;
    private static final int CATALYST_Y = 22;
    private static final int PREVIEW_X = 116;
    private static final int PREVIEW_Y = 22;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int SLOT_SIZE = 18;

    private final Container container;
    private final Container preview;
    private final ContainerLevelAccess access;
    /** 仅服务端构造时有值：ContainerData 要读经验点与背包。 */
    @Nullable
    private final Player player;
    private final ContainerData data;

    /** 客户端构造（MenuType 的 MenuSupplier）：容器与预览都是占位容器，内容由服务端槽位同步覆盖。 */
    public ConverterMenu(int id, Inventory inventory) {
        this(id, inventory, null, new SimpleContainer(ConverterBlockEntity.SLOT_COUNT), ContainerLevelAccess.NULL);
    }

    /** 服务端构造：容器即方块实体本身（MenuType 的 IContainerFactory 形态未使用，见交付说明）。 */
    public ConverterMenu(int id, Inventory inventory, Player player, ContainerLevelAccess access) {
        this(id, inventory, player, resolveContainer(access), access);
    }

    private ConverterMenu(int id, Inventory inventory, @Nullable Player player, Container container,
                          ContainerLevelAccess access) {
        super(FarmstormMenus.CONVERTER.get(), id);
        this.container = container;
        this.preview = new SimpleContainer(1);
        this.access = access;
        this.player = player;
        this.data = new ConverterData();
        this.addSlot(new Slot(container, ConverterBlockEntity.SLOT_INPUT, INPUT_X, INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ConverterMenu.this.mayPlaceInput(stack);
            }
        });
        this.addSlot(new Slot(container, ConverterBlockEntity.SLOT_CATALYST, CATALYST_X, CATALYST_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.NETHER_STAR);     // FIX-4 §2.2：催化剂槽只收下界之星
            }
        });
        this.addSlot(new Slot(this.preview, 0, PREVIEW_X, PREVIEW_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });
        this.addDataSlots(this.data);
        this.addPlayerSlots(inventory);
        this.refreshPreview();
    }

    private static Container resolveContainer(ContainerLevelAccess access) {
        Container fallback = new SimpleContainer(ConverterBlockEntity.SLOT_COUNT);
        return access.evaluate((level, pos) ->
                level.getBlockEntity(pos) instanceof ConverterBlockEntity converter ? converter : fallback, fallback);
    }

    /** 3 行主背包 + 1 行快捷栏，标准 176x166 版式。 */
    private void addPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9,
                        INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, INVENTORY_X + column * SLOT_SIZE, HOTBAR_Y));
        }
    }

    /** 输入槽只接受能匹配配方的物品（TECH §8.3）；客户端占位容器一律放行，由服务端否决。 */
    private boolean mayPlaceInput(ItemStack stack) {
        return !(this.container instanceof ConverterBlockEntity converter) || converter.accepts(stack);
    }

    /** 预览槽随输入刷新；父类的 slotsChanged 也经由广播走到这里（TECH §4.8.3）。 */
    @Override
    public void broadcastChanges() {
        this.refreshPreview();
        super.broadcastChanges();
    }

    private void refreshPreview() {
        if (!(this.container instanceof ConverterBlockEntity converter)) {
            return;                                     // 客户端不本地算配方（HC-03），预览以槽位同步为准
        }
        ItemStack result = converter.previewResult();
        if (!ItemStack.matches(this.preview.getItem(0), result)) {
            this.preview.setItem(0, result);
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_CONVERT) {
            return false;
        }
        return player instanceof ServerPlayer serverPlayer
                && this.container instanceof ConverterBlockEntity converter
                && converter.stillValid(player)
                && converter.tryConvert(serverPlayer);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack current = slot.getItem();
        ItemStack result = current.copy();
        if (index == SLOT_PREVIEW) {
            return ItemStack.EMPTY;                     // 预览槽只读
        }
        if (index < PLAYER_INVENTORY_START) {
            if (!this.moveItemStackTo(current, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(current, SLOT_INPUT, SLOT_PREVIEW, false)) {
            return ItemStack.EMPTY;                     // 背包 → 输入槽，放不进去再退到催化剂槽
        }
        if (current.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, FarmstormBlocks.CONVERTER.get());
    }

    /** ContainerData[0]：本次转换的经验成本（客户端读同步值，服务端现算）。 */
    public int xpCost() {
        return this.data.get(ConverterBlockEntity.DATA_XP_COST);
    }

    /** ContainerData[1]：当前输入能否转换 —— 转换按钮的 active 状态直接跟它走。 */
    public boolean canConvert() {
        return this.data.get(ConverterBlockEntity.DATA_CAN_CONVERT) != 0;
    }

    /** [0]=xpCost [1]=canConvert(0/1) [2]=reverse(0/1)；服务端现算，客户端读同步值。 */
    private final class ConverterData implements ContainerData {
        private final int[] synced = new int[ConverterBlockEntity.DATA_COUNT];

        @Override
        public int get(int index) {
            if (ConverterMenu.this.container instanceof ConverterBlockEntity converter
                    && ConverterMenu.this.player != null) {
                return converter.containerData(index, ConverterMenu.this.player);
            }
            return this.synced[index];
        }

        @Override
        public void set(int index, int value) {
            this.synced[index] = value;
        }

        @Override
        public int getCount() {
            return ConverterBlockEntity.DATA_COUNT;
        }
    }
}
