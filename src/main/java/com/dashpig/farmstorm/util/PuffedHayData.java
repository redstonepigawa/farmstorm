package com.dashpig.farmstorm.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 「膨胀麦写出的原版干草块」的服务端坐标集合（F-036 / D-045）。
 *
 * <p>1.20.1 的干草块没有我们的方块实体可用（那是原版方块），因此记录挂在服务端 {@link SavedData} 上：
 * 元素是 {@link BlockPos#asLong()}，落盘名 {@value #DATA_NAME}（{@code world/data/farmstorm_puffed_hay.dat}）。
 *
 * <p>签名是 1.20.1 那一套，**不是** 1.21.1 的 {@code SavedData.Factory}：
 * {@code SavedData#save(CompoundTag)} 只有一个参数、{@code DimensionDataStorage#computeIfAbsent}
 * 是「读函数 / 构造器 / 落盘名」三个参数（与 {@code outpost/OutpostCoreIndex.java:76-79} 同一形态）。
 * 只有两种写操作，写入点由 F-037 保证「只登记真的写成功了、且真的是原版干草块」的那一格。
 */
public final class PuffedHayData extends SavedData {
    /** 落盘文件名（{@code world/data/farmstorm_puffed_hay.dat}）。 */
    private static final String DATA_NAME = "farmstorm_puffed_hay";

    private static final String TAG_POSITIONS = "positions";

    /** 坐标按 {@link BlockPos#asLong()} 压成 long，重复写入天然去重。 */
    private final LongOpenHashSet positions = new LongOpenHashSet();

    /** 全服唯一集合：主世界的数据存储（三份服务端索引同一入口）。 */
    public static PuffedHayData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(PuffedHayData::load, PuffedHayData::new, DATA_NAME);
    }

    /** 该格是否在记录里（消费点用）。 */
    public boolean contains(BlockPos pos) {
        return this.positions.contains(pos.asLong());
    }

    /** 记一格（D-046：调用点在 {@code setBlock} 成功之后）。 */
    public void add(BlockPos pos) {
        if (this.positions.add(pos.asLong())) {
            this.setDirty();
        }
    }

    /** 摘一格，返回是否真的摘掉（F-038 的消费点靠这个返回值决定要不要清掉落）。 */
    public boolean remove(BlockPos pos) {
        if (!this.positions.remove(pos.asLong())) {
            return false;
        }
        this.setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray(TAG_POSITIONS, this.positions.toLongArray());
        return tag;
    }

    private static PuffedHayData load(CompoundTag tag) {
        PuffedHayData data = new PuffedHayData();
        for (long packed : tag.getLongArray(TAG_POSITIONS)) {
            data.positions.add(packed);
        }
        return data;
    }
}
