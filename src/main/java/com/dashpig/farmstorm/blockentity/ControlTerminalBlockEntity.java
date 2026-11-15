package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
     * 服务端每刻入口（{@code ControlTerminalBlock#getTicker} 唯一调用点）。
     *
     * <p>F-026 只把 ticker 接上，每 20 刻的投料循环是 F-033（间隔常量 20 写死、不是 config 键）。
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ControlTerminalBlockEntity terminal) {
    }
}
