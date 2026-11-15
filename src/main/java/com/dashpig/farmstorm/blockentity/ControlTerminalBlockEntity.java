package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.network.CultivatorLinkPayload.Link;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 控制终端方块实体（F-026 / D-025 / D-030）。
 *
 * <p><b>模式存自己</b>（D-030）：三态 {@code OFF} / {@code CULTIVATOR} / {@code CONVERTER} 写进终端自己的 NBT，
 * 不住核侧——两个终端绑同一核时会互相覆盖。NBT 里存的是<b>序列化名</b>
 * （{@code off} / {@code cultivator} / {@code converter}），不是枚举自带的那套序号：
 * 枚举顺序一改存档含义就变，且 {@code /data get block} 读出来会是一串不可读的数字（验收 #7）。
 *
 * <p>{@code OFF} 是<b>真实状态</b>，不是「两个都为假」的推导（06 号 §0.2 的术语表）；
 * 「再点已亮的那个退回 {@code OFF}」的轮转属于面板按钮（F-030），本类只提供状态与存读。
 *
 * <p><b>不自建绑定真相</b>（D-025）：{@code corePos} 只是引用缓存，三方法的语义与培养器同形
 * （核侧列表是唯一真相，写入顺序「先核后终端」）。绑定 / 解绑的写入点在 F-029，本类只落接口面。
 *
 * <p><b>投料循环</b>（F-033 / D-032 / D-033）：服务端每 20 刻（编译期常量，<b>不是 config 键</b>）按当前模式
 * 对每台目标机器投 <b>1</b> 个；准入判据只借机器自己的 {@code canInsert}，本类<b>不建物品白名单、
 * 也不判作物档</b>——数据包新增一条转换配方，培养模式自动多收一个投入物，与培养器同步。
 */
public class ControlTerminalBlockEntity extends BlockEntity implements CoreBound {
    /** 模式的 NBT 键（F-026 / D-030）：{@code /data get block <终端坐标> Mode} 读的就是它，值是序列化名。 */
    private static final String TAG_MODE = "Mode";
    /** 绑定核坐标的键：{@code long}（{@link BlockPos#asLong()}）；**缺键 = 未绑定**（与培养器同形）。 */
    private static final String TAG_CORE = "Core";

    /**
     * 终端的三种模式（D-030 / D-031）：互斥单选，{@code OFF} 是其中之一，不是缺省推导值。
     * 序列化名与常量名分开写，改常量名不动存档。
     */
    public enum Mode {
        OFF("off"),
        CULTIVATOR("cultivator"),
        CONVERTER("converter");

        private final String serializedName;

        Mode(String serializedName) {
            this.serializedName = serializedName;
        }

        /** NBT 与面板上的名字；**不暴露枚举序号**（D-030 的否决项 3：改枚举顺序不动存档）。 */
        public String serializedName() {
            return this.serializedName;
        }

        /** 序列化名 → 枚举；未知名字按 {@link #OFF} 解（缺键与坏档都从关闭起步）。 */
        public static Mode byName(String name) {
            for (Mode mode : values()) {
                if (mode.serializedName.equals(name)) {
                    return mode;
                }
            }
            return OFF;
        }
    }

    private Mode mode = Mode.OFF;
    /** 绑定的核心坐标；{@code null} = 未绑定（D-025：引用缓存，真相在核侧）。 */
    @Nullable
    private BlockPos corePos;

    /** 1.20.1 的构造是 {@code (BlockPos, BlockState)}，类型由注册表项注入。 */
    public ControlTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(FarmstormBlockEntities.CONTROL_TERMINAL.get(), pos, state);
    }

    /** 当前模式（面板与投料循环读它）。 */
    public Mode mode() {
        return this.mode;
    }

    /** 写模式（F-030 / D-031 的唯一写入点：面板的 {@code clickMenuButton}）；落盘由 {@link #saveAdditional} 承担。 */
    public void setMode(Mode mode) {
        if (this.mode == mode) {
            return;
        }
        this.mode = mode;
        this.setChanged();
    }

    /** 引用缓存只读视图；{@code null} = 未绑定。 */
    @Nullable
    @Override
    public BlockPos corePos() {
        return this.corePos;
    }

    /** 写入引用缓存：调用方先把核侧列表写成功（D-025 的两侧一致性顺序）。 */
    @Override
    public void setCore(BlockPos pos) {
        this.corePos = pos;
        this.setChanged();
    }

    /** 清掉引用缓存（核被拆且没有接替核时的清理出口）。 */
    @Override
    public void clearCore() {
        if (this.corePos == null) {
            return;
        }
        this.corePos = null;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_MODE, this.mode.serializedName());
        // 未绑定时不写这个键（缺键 = 未绑定），与培养器的 Core 键同形。
        if (this.corePos != null) {
            tag.putLong(TAG_CORE, this.corePos.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.mode = tag.contains(TAG_MODE) ? Mode.byName(tag.getString(TAG_MODE)) : Mode.OFF;
        this.corePos = tag.contains(TAG_CORE) ? BlockPos.of(tag.getLong(TAG_CORE)) : null;
    }

    /**
     * 每 20 刻一轮的间隔（F-033 / D-032）：编译期常量，<b>不是 config 键</b>——与
     * {@code OutpostCoreBlockEntity} 的 {@code REBIND_INTERVAL_TICKS} 同一体例，server config 仍是 175 个键。
     */
    private static final int FEED_INTERVAL_TICKS = 20;

    /**
     * 服务端每刻入口（{@code ControlTerminalBlock#getTicker} 唯一调用点）。
     *
     * <p>九步按 08 号 §4.4 的顺序短路：客户端 / 非节拍刻 / {@code OFF} / 未绑定 / 核取不到 / 核侧不认 /
     * 超 3 × 3 区块，任一不过就静默返回（不消费库存、不报错）；全过之后按 {@code cultivatorKinds} 挑出
     * 本模式的目标机器，每台跑一次「投 1 个」。
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ControlTerminalBlockEntity terminal) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % FEED_INTERVAL_TICKS != 0) {
            return;                                     // 每 20 刻才跑一轮
        }
        Mode mode = terminal.mode();
        if (mode == Mode.OFF) {
            return;                                     // D-031：关闭态不投料、不消费核库存
        }
        BlockPos corePos = terminal.corePos();
        if (corePos == null) {
            return;                                     // 未绑定：引用缓存为空（D-025）
        }
        if (!(level.getBlockEntity(corePos) instanceof OutpostCoreBlockEntity core)) {
            return;                                     // 区块不在内存 / 已不是核：取不到正好当闸用
        }
        if (!core.hasCultivator(pos)) {
            return;                                     // 核侧名单是唯一真相（名额满时也在这条上退出）
        }
        if (!OutpostCoreBlockEntity.inRange(corePos, pos)) {
            return;                                     // D-027：超 3 × 3 区块既不绑定也不投料
        }
        Link.Kind kind = mode == Mode.CULTIVATOR ? Link.Kind.CULTIVATOR : Link.Kind.CROP_CONVERTER;
        List<BlockPos> machines = core.cultivators();            // 先取这条：这一步会清理两条平行列表
        List<Link.Kind> kinds = core.cultivatorKinds();
        int bound = Math.min(machines.size(), kinds.size());     // 同长同下标由核侧保证，这里只是防御
        for (int index = 0; index < bound; index++) {
            if (kinds.get(index) == kind) {
                feedOne(level, core, machines.get(index), kind);
            }
        }
    }

    /**
     * 一轮里对一台机器的「投 1 个」：类型必须与模式匹配 ⇒ 按槽序扫核的 54 格 ⇒ 第一格通过<b>机器自己的</b>
     * 闸就投这一个，投完这一台本轮结束（{@code break}，不存在给同一台塞第二个的路径）。
     *
     * <p>准入判据只借不改（D-033）：培养器 {@code canInsert}（范围 ∧ 绑定 ∧ 手持档 ∧ 空槽）、
     * 转换器 {@code canInsert}（范围 ∧ 绑定 ∧ 手持 ∧ 空槽）；这里既没有物品白名单，也不判作物档。
     * 先移除恰好 1 个再放入，与右键那条路径同一顺序。
     */
    private static void feedOne(Level level, OutpostCoreBlockEntity core, BlockPos machinePos, Link.Kind kind) {
        BlockEntity machine = level.getBlockEntity(machinePos);
        CultivatorBlockEntity cultivator = machine instanceof CultivatorBlockEntity found ? found : null;
        CropConverterBlockEntity converter = machine instanceof CropConverterBlockEntity found ? found : null;
        // 类型必须与模式匹配：培养模式只认培养器、转换模式只认转换器，否则这台本轮跳过。
        if (kind == Link.Kind.CULTIVATOR ? cultivator == null : converter == null) {
            return;
        }
        for (int slot = 0; slot < OutpostCoreBlockEntity.SLOT_COUNT; slot++) {
            ItemStack candidate = core.getItem(slot);
            if (candidate.isEmpty()) {
                continue;
            }
            boolean accepted = cultivator != null
                    ? cultivator.canInsert(candidate)
                    : converter.canInsert(candidate);
            if (!accepted) {
                continue;
            }
            ItemStack one = core.removeItem(slot, 1);
            if (cultivator != null) {
                cultivator.insert(one);
            } else {
                converter.insert(one);
            }
            break;
        }
    }
}
