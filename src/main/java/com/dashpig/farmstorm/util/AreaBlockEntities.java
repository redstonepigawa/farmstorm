package com.dashpig.farmstorm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * 区域方块实体枚举（F-248 / 42 号 §2.1.2）：以某个坐标为中心，扫 <b>已加载</b>区块里的方块实体。
 *
 * <p><b>不为枚举加载区块</b>：区块只从 {@code ServerChunkCache} 的「只查已加载」入口取，取不到
 * （返回 {@code null}）就跳过该区块。{@code Level} 上那几个按坐标取区块 / 取方块实体的方法一律不用
 * —— 它们会触发区块加载，每 100 刻把 3 × 3 个区块拉进内存（RT-52）。
 *
 * <p><b>两阶段</b>：{@code LevelChunk#getBlockEntities()} 返回的是该区块自己的活映射，直接过滤等于
 * 边遍历边让别处增删，会 {@code ConcurrentModificationException}。这里先把映射的值复制进新列表，
 * 再在副本上过滤（与 {@code QbounceMountController.adoptExisting} 同一种写法）。
 */
public final class AreaBlockEntities {
    private AreaBlockEntities() {}

    /**
     * 收集 {@code center} 所在区块及其外扩 {@code chunkRadius} 圈内的方块实体（切比雪夫距离，
     * 与调用方的范围常量同一组数字）。
     *
     * @param chunkRadius 区块半径；1 即 3 × 3（{@code OutpostCoreBlockEntity#RANGE_CHUNKS}）
     * @param type        目标类型：按 {@code Class} 过滤，不做注册类型分流
     * @return 新列表（不是区块映射的视图），枚举顺序即区块遍历顺序，调用方自行排序
     */
    public static <T extends BlockEntity> List<T> collect(ServerLevel level, BlockPos center, int chunkRadius,
                                                          Class<T> type) {
        List<T> found = new ArrayList<>();
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
                if (chunk == null) {
                    continue;                             // 不在内存：跳过，不写状态、不记日志
                }
                for (BlockEntity candidate : new ArrayList<>(chunk.getBlockEntities().values())) {
                    if (type.isInstance(candidate)) {
                        found.add(type.cast(candidate));
                    }
                }
            }
        }
        return found;
    }

    /**
     * 按谓词收集（F-316 / D-231）：范围、两阶段复制、{@code getChunkNow} 的纪律与按类重载逐字相同，
     * 差别只在判据由调用方给。
     *
     * <p>为什么加这一枚而不是给按类重载加一个 {@code Class} 列表参数（D-231 的否决面）：调用点会写出
     * 「一次转两次」的形状，正好是 D-230 要删掉的那种；谓词让调用方直接表达「该格是不是标签成员」。
     *
     * @param chunkRadius 区块半径；1 即 3 × 3
     * @param filter      判据：对每个候选调用一次，{@code true} 即收进结果
     * @return 新列表（不是区块映射的视图），枚举顺序即区块遍历顺序，调用方自行排序
     */
    public static List<BlockEntity> collect(ServerLevel level, BlockPos center, int chunkRadius,
                                            Predicate<BlockEntity> filter) {
        List<BlockEntity> found = new ArrayList<>();
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
                if (chunk == null) {
                    continue;                             // 不在内存：跳过，不写状态、不记日志
                }
                for (BlockEntity candidate : new ArrayList<>(chunk.getBlockEntities().values())) {
                    if (filter.test(candidate)) {
                        found.add(candidate);
                    }
                }
            }
        }
        return found;
    }
}
