package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.network.CultivatorLinkPayload.Link;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 控制终端方块实体（D-259；F-333 落模式三态与绑定引用缓存）。
 *
 * <p>模式<b>只存在本方块实体自己的 NBT</b>（{@link #TAG_MODE}，D-269）：不写 blockstate，
 * 也不参与菜单同步（裁定 Q1=A —— 两个格子的同步数据由菜单自己持）。
 *
 * <p>{@code corePos} 是<b>引用缓存</b>，不构成第二份绑定真相（D-264 / D-165）：绑定关系的唯一真相
 * 在核侧列表，使用前要用核侧列表校验。
 *
 * <p><b>服务端每 20 刻投料一轮</b>（F-340）：节拍是编译期常量 {@link #FEED_INTERVAL_TICKS}，<b>不是</b>
 * 配置键（D-271）——本版 server config 仍是 175 键，投料间隔没有可调项。
 *
 * <p>三条纪律：① <b>准入只借机器自己的闸</b>（{@code CultivatorBlockEntity#canInsert} /
 * {@code CropConverterBlockEntity#canInsert}，D-272），本类里<b>一个物品白名单都没有</b>，
 * 数据包新增配方机器自动多收一个；② 模式 → {@link Link.Kind} 是<b>显式映射</b>
 * ——两个枚举的序号并不同（{@code Mode.CULTIVATOR} 是 1，而 {@code Kind} 的 1 是
 * {@code CROP_CONVERTER}），拿枚举序号当 kind 会把培养器写成转换器；③ <b>每台机器每轮最多 1 个</b>，
 * 第一次投进就 {@code break}，剩下的格子留给下一轮（20 刻之后）。
 *
 * <p>客户端永不 tick，面板归 F-337。
 */
public class ControlTerminalBlockEntity extends BlockEntity implements CoreBound {
    /** 模式的 NBT 键（D-269）：{@code String}，存可读名而不是枚举序号。 */
    private static final String TAG_MODE = "Mode";
    /** 绑定核坐标的 NBT 键（D-264）：{@code long}（{@link BlockPos#asLong()}）；**缺键 = 未绑定**。 */
    private static final String TAG_CORE = "Core";
    /** 投料节拍（D-271）：编译期常量，**不是**配置键 —— server config 仍是 175 键。 */
    private static final int FEED_INTERVAL_TICKS = 20;

    /**
     * 模式三态（D-269）：枚举序 0/1/2 与存盘名解耦——落盘写的是小写可读名而不是序号，
     * 所以 {@code /data get block <pos> Mode} 直接读到 {@code off} 这样的值。
     */
    public enum Mode {
        OFF("off"),
        CULTIVATOR("cultivator"),
        CONVERTER("converter");

        /** 存盘用的可读名（小写）。 */
        private final String serializedName;

        Mode(String serializedName) {
            this.serializedName = serializedName;
        }

        /** 名字 → 枚举；未知名字返回 {@code null}，由调用方回落 {@link #OFF}。 */
        @Nullable
        private static Mode byName(String name) {
            for (Mode mode : values()) {
                if (mode.serializedName.equals(name)) {
                    return mode;
                }
            }
            return null;
        }
    }

    /** 当前模式；刚放下的终端与老存档都从这里开始（{@link Mode#OFF}）。 */
    private Mode mode = Mode.OFF;
    /** 绑定的核心坐标；{@code null} = 未绑定（D-264：引用缓存，真相在核侧）。 */
    @Nullable
    private BlockPos corePos;

    public ControlTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.CONTROL_TERMINAL.get(), pos, state);
    }

    /** 当前模式（{@code /data get block} 读到的 {@code Mode} 就是这个的可读名）。 */
    public Mode mode() {
        return this.mode;
    }

    /** 写入模式（唯一写入面）；模式变化不需要广播、不发包。 */
    public void setMode(Mode mode) {
        this.mode = mode;
        this.setChanged();
    }

    /** 引用缓存只读视图；{@code null} = 未绑定。 */
    @Nullable
    @Override
    public BlockPos corePos() {
        return this.corePos;
    }

    /** 写入引用缓存：调用方先把核侧列表写成功，再调这里（两侧不一致时按未绑定处理）。 */
    @Override
    public void setCore(BlockPos pos) {
        this.corePos = pos;
        this.setChanged();
    }

    /** 清掉引用缓存（核被拆且没有接替核时的清理出口）。 */
    @Override
    public void clearCore() {
        this.corePos = null;
        this.setChanged();
    }

    // ---- F-340 · 服务端每 20 刻投料一轮（D-271 / D-272）----

    /**
     * 服务端投料循环：每 {@link #FEED_INTERVAL_TICKS} 刻，把核库存里**机器自己肯收**的那一格投给
     * 核侧名单上、kind 与本模式匹配的每一台机器（**每台每轮最多 1 个**）。
     *
     * <p>前七步全是**静默边界**：客户端、未到节拍、{@link Mode#OFF}、未绑定、核不在内存、核侧名单不认这台、
     * 超出 3 × 3 区块范围 —— 任一成立就本轮什么都不做，不报错、不广播、不动库存。
     *
     * <p>第 6 步用 {@code hasCultivator} 而不是数一遍名单：「名额满」这件事由核侧名单兜住，
     * 本类不认识 24（容量）也不认识 54（核库存），不去算满不满。
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ControlTerminalBlockEntity terminal) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % FEED_INTERVAL_TICKS != 0) {
            return;
        }
        if (terminal.mode() == Mode.OFF) {
            return;
        }
        BlockPos corePos = terminal.corePos();
        if (corePos == null) {
            return;
        }
        if (!(level.getBlockEntity(corePos) instanceof OutpostCoreBlockEntity core)) {
            return;
        }
        if (!core.hasCultivator(terminal.getBlockPos())) {
            return;
        }
        if (!OutpostCoreBlockEntity.inRange(corePos, terminal.getBlockPos())) {
            return;
        }
        // 显式映射：严禁用枚举序号当 Kind（两个枚举的序号不同）。
        Link.Kind targetKind = terminal.mode() == Mode.CULTIVATOR
                ? Link.Kind.CULTIVATOR
                : Link.Kind.CROP_CONVERTER;
        // 两条列表同长同下标：先取 cultivators()（会惰性清理），再取 kinds 快照，越界跳过而不是抛。
        var machines = core.cultivators();
        var kinds = core.cultivatorKinds();
        int pairs = Math.min(machines.size(), kinds.size());
        for (int index = 0; index < pairs; index++) {
            if (kinds.get(index) != targetKind) {
                continue;
            }
            feedOne(level, core, machines.get(index), terminal.mode());
        }
    }

    /**
     * 给<b>一台</b>机器投 1 个：类型对 → 按槽序扫核库存 → 机器自己的准入闸 → 取 1 个投进去 → 结束这一台。
     *
     * <p>准入判据只有机器的 {@code canInsert}（可用 ∧ 空槽 ∧ 准入，D-272）：本类不写物品白名单，
     * 也不自己判空槽 —— 闸里已经先判槽空再查配方，短路顺序由机器保证。
     *
     * <p>扫满核库存（54 格）无一格通过就这台本轮不投，接着换下一台；投进一个立刻 {@code break}。
     */
    private static void feedOne(Level level, OutpostCoreBlockEntity core, BlockPos machinePos, Mode mode) {
        BlockEntity blockEntity = level.getBlockEntity(machinePos);
        CultivatorBlockEntity cultivator = blockEntity instanceof CultivatorBlockEntity found ? found : null;
        CropConverterBlockEntity converter = blockEntity instanceof CropConverterBlockEntity found ? found : null;
        // 类型必须与本模式匹配：培养器模式不碰转换器，反之亦然。
        boolean matches = mode == Mode.CULTIVATOR ? cultivator != null : converter != null;
        if (!matches) {
            return;
        }
        for (int slot = 0; slot < core.getContainerSize(); slot++) {
            ItemStack stack = core.getItem(slot);
            boolean admitted = mode == Mode.CULTIVATOR
                    ? cultivator.canInsert(stack)
                    : converter.canInsert(stack);
            if (!admitted) {
                continue;
            }
            ItemStack one = core.removeItem(slot, 1);
            if (mode == Mode.CULTIVATOR) {
                cultivator.insert(one);
            } else {
                converter.insert(one);
            }
            break;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(TAG_MODE, this.mode.serializedName);
        // 未绑定时不写这个键（D-264：缺键 = 未绑定），验收靠「缺 Core 键」读出来。
        if (this.corePos != null) {
            tag.putLong(TAG_CORE, this.corePos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // 缺键（刚放下 / 老存档）与未知字符串一律回落 OFF。
        Mode loaded = Mode.byName(tag.getString(TAG_MODE));
        this.mode = loaded == null ? Mode.OFF : loaded;
        this.corePos = tag.contains(TAG_CORE) ? BlockPos.of(tag.getLong(TAG_CORE)) : null;
    }
}
