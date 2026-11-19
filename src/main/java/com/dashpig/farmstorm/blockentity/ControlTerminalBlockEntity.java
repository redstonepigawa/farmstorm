package com.dashpig.farmstorm.blockentity;

import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
 * <p>刻意<b>不 tick</b>（投料循环整段归 F-340），面板归 F-337。
 */
public class ControlTerminalBlockEntity extends BlockEntity implements CoreBound {
    /** 模式的 NBT 键（D-269）：{@code String}，存可读名而不是枚举序号。 */
    private static final String TAG_MODE = "Mode";
    /** 绑定核坐标的 NBT 键（D-264）：{@code long}（{@link BlockPos#asLong()}）；**缺键 = 未绑定**。 */
    private static final String TAG_CORE = "Core";

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
