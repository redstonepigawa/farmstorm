package com.dashpig.farmstorm.outpost;

import com.dashpig.farmstorm.Farmstorm;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

/**
 * 核心所在区块的运行期临时票（F-198 / D-145 / A-38 / A-43 / RT-39）。
 *
 * <p>每个已放置的核在自己所在的那**1 个区块**上加一张自有 {@link TicketType}、{@code distance = 1}，
 * 于是即使没有玩家靠近，方块实体仍 tick（边界粒子照常刷新）；核被毁 / 停服即释放。
 *
 * <p><b>只用裸 {@code TicketType} + {@code addRegionTicket}</b>：原版的「强制加载区块」入口与 NeoForge
 * {@code TicketController} 上的对应入口都会写 {@code world/data/chunks.dat}
 * （{@code ForcedChunksSavedData.FILE_ID = "chunks"}），两条都否决（A-38）——所以本类全程不碰
 * 任何持久化的强制加载数据，{@code chunks.dat} 的 {@code Forced} 数组里永远不会出现核坐标。
 *
 * <p><b>口径按 A-43 写</b>：{@code distance = 1} 时中心区块的 ticket level 是
 * {@code ChunkLevel.byStatus(FULL) − 1 = 32}（entity-ticking，方块实体 tick），
 * 紧邻一圈是 33（FULL：已加载、不 tick），再外一圈不再加载。
 * 因此准确的说法是「**只有 1 个区块被 tick**，另外 8 个被加载到 FULL」——
 * 不能写成「只有 1 个区块被加载」（那一条必红）。
 *
 * <p>票是内存态、停服即失：重启后按 {@link OutpostCoreIndex} 的每条坐标重新申请（{@link #restoreAll}）。
 * 释放三处（{@code OutpostCoreBlockEntity#release()}、{@code settleOnRemoval} 末尾的同一处、
 * 停服 {@link #releaseAll}），且必须幂等——{@code SortedArraySet} 对同一张票去重，多删一次是无操作。
 */
public final class CoreChunkTickets {
    /**
     * 自有票类型（不进 registry）。{@code timeout} 取默认 0 = 永不过期：票的寿命由释放点决定，
     * 不由计时器决定（计时器会让「核还在、票没了」变成静默失败）。
     */
    public static final TicketType<ChunkPos> CORE =
            TicketType.create("farmstorm_core", Comparator.comparingLong(ChunkPos::toLong));

    /** A-43：{@code distance = 1} ⇒ 中心区块 32（entity-ticking），邻圈 33（FULL）。 */
    private static final int DISTANCE = 1;

    private CoreChunkTickets() {}

    /** 申请一张票（幂等：同一区块重复申请不会叠加）。 */
    public static void request(ServerLevel level, BlockPos corePos) {
        ChunkPos chunk = new ChunkPos(corePos);
        level.getChunkSource().addRegionTicket(CORE, chunk, DISTANCE, chunk);
        Farmstorm.LOGGER.info("核心区块票申请：{} {}（farmstorm_core，distance={}）",
                level.dimension().location(), chunk.toLong(), DISTANCE);
    }

    /** 释放一张票（幂等：多删一次是无操作，少删一次就是泄漏）。 */
    public static void release(ServerLevel level, BlockPos corePos) {
        ChunkPos chunk = new ChunkPos(corePos);
        level.getChunkSource().removeRegionTicket(CORE, chunk, DISTANCE, chunk);
        Farmstorm.LOGGER.info("核心区块票释放：{} {}（farmstorm_core，distance={}）",
                level.dimension().location(), chunk.toLong(), DISTANCE);
    }

    /** 起服补票：票是内存态，重启后按索引重新申请。**先补票、后补绑定**（F-198 要点 3）。 */
    public static void restoreAll(MinecraftServer server) {
        int count = forEachCore(server, true);
        Farmstorm.LOGGER.info("核心区块票补票：{} 张（farmstorm_core，distance={}，来源 OutpostCoreIndex）", count, DISTANCE);
    }

    /** 停服释放：票不落盘，停服即失，这里显式释放一遍让日志里的申请 / 释放次数对得上。 */
    public static void releaseAll(MinecraftServer server) {
        int count = forEachCore(server, false);
        Farmstorm.LOGGER.info("核心区块票停服释放：{} 张（farmstorm_core，distance={}）", count, DISTANCE);
    }

    private static int forEachCore(MinecraftServer server, boolean add) {
        OutpostCoreIndex index = OutpostCoreIndex.get(server);
        int count = 0;
        for (UUID owner : index.owners()) {
            for (OutpostCoreIndex.Entry entry : index.entriesOf(server, owner)) {
                ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, entry.dimension()));
                if (level == null) {
                    continue;
                }
                if (add) {
                    request(level, entry.pos());
                } else {
                    release(level, entry.pos());
                }
                count++;
            }
        }
        return count;
    }
}
