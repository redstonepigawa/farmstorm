package com.dashpig.farmstorm.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 「被膨胀麦砸出来、挖掉时应 0 掉落」的干草块坐标记录（D-284）。
 *
 * <p>为什么是 {@link SavedData} 而不是方块实体：这类格子写下去的是**原版** {@code minecraft:hay_block}，
 * 原版方块没有我方方块实体可用，挂了也会被原版逻辑丢掉；因此记录只能挂在服务端存档数据上，
 * 落盘成 {@code world/data/farmstorm_puffed_hay.dat}，跨重启与区块重载都还在。
 *
 * <p>键是坐标（{@link BlockPos#asLong()}），**不带方块状态**：所以消费侧判定时不能只看
 * {@link #contains}，必须同时校验 {@code state.is(Blocks.HAY_BLOCK)}（D-286）——同一个坐标后来被
 * 换成别的方块时，那条记录不该再被当成我方的干草块。
 *
 * <p>已知代价（RT-94）：记录只增不减，被活塞推走 / 被爆炸清掉 / 被 {@code /setblock} 覆盖都不会消费，
 * 于是这类坐标会永久留在集合里；它们只会让 {@link #contains} 多一次命中，正确性由上面的
 * 方块状态校验兜住，但存档里的条数会单调增长。
 */
public final class PuffedHayData extends SavedData {
    /** 落盘文件名（{@code world/data/farmstorm_puffed_hay.dat}）。 */
    private static final String DATA_NAME = "farmstorm_puffed_hay";

    private static final String TAG_POSITIONS = "positions";

    /** 干草块坐标（{@link BlockPos#asLong()}）；只增不减，见类注释 RT-94。 */
    private final LongOpenHashSet positions = new LongOpenHashSet();

    /** 全服唯一索引（与 {@code OutpostCoreIndex#get} / {@code TrapFifoIndex#get} 同一形态）。 */
    public static PuffedHayData get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<PuffedHayData>(PuffedHayData::new, PuffedHayData::load),
                        DATA_NAME);
    }

    /** 该坐标是否被记录过（还需配合 {@code state.is(Blocks.HAY_BLOCK)} 校验，D-286）。 */
    public boolean contains(BlockPos pos) {
        return this.positions.contains(pos.asLong());
    }

    /** 记一条；真的新增才标脏。 */
    public void add(BlockPos pos) {
        if (this.positions.add(pos.asLong())) {
            this.setDirty();
        }
    }

    /** 摘一条；真的删掉才标脏。 */
    public boolean remove(BlockPos pos) {
        if (!this.positions.remove(pos.asLong())) {
            return false;
        }
        this.setDirty();
        return true;
    }

    /** 落盘形态：{@code positions → long[]}。 */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLongArray(TAG_POSITIONS, this.positions.toLongArray());
        return tag;
    }

    public static PuffedHayData load(CompoundTag tag, HolderLookup.Provider registries) {
        PuffedHayData data = new PuffedHayData();
        for (long packed : tag.getLongArray(TAG_POSITIONS)) {
            data.positions.add(packed);
        }
        return data;
    }
}
