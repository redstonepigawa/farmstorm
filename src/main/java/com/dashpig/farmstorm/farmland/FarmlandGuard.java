package com.dashpig.farmstorm.farmland;

import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.outpost.OutpostCoreIndex;
import com.dashpig.farmstorm.util.DeployedOwnership;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * 耕地两道闸（F-157 / F-158；D-116 / D-118 / D-120，TECH §6.3 / §6.4）。
 *
 * <p>第一道是**锄地拦截**：{@link #canTill} = {@link #inOwnRange} && {@code used < capacity}，
 * 顺序固定「先范围、后额度」。先判范围是因为它不碰账本（更便宜），而且「附近没有己方核心」
 * 是玩家更容易理解的那条提示（24 号 §6.3 表里的第 2、3 步）。
 *
 * <p>第二道是**踩踏保护**：己方核心 3 × 3 内、且记在己方账上的耕地不被踩成泥土（D-120 第 3 项）。
 * 只作用于踩踏：失水退化走 {@code FarmBlock#randomTick} → {@code turnToDirt}，不经过踩踏事件，
 * 因此范围内的耕地照常退化（D-120 补充：不做永不退化的永久田）。
 *
 * <p>「己方」只有一条链路：{@link DeployedOwnership#isAlly} → {@code TeamResolver}（红线第 4 条）。
 * 本类不另写队伍比较，也不缓存队伍快照。
 */
public final class FarmlandGuard {
    /** 被拒提示：目标位置不在己方核心的 3 × 3 内（F-157 第 2 步）。 */
    public static final String MESSAGE_NO_CORE = "message.farmstorm.farmland.no_core";

    /** 被拒提示：额度已用满，带「已用 / 上限」两个参数（F-157 第 3 步）。 */
    public static final String MESSAGE_QUOTA_FULL = "message.farmstorm.farmland.quota_full";

    /** 判定结果：顺序固定「先范围、后额度」，被拒的两种各对一条提示。 */
    public enum Verdict {
        /** 范围内且额度有余：放行并在同一刻记账。 */
        ALLOW,
        /** 不在己方核心的 3 × 3 内。 */
        NO_CORE,
        /** 在范围内，但 {@code used >= capacity}。 */
        QUOTA_FULL
    }

    private FarmlandGuard() {}

    /**
     * 这个右键是不是「土 / 草方块 → 耕地」：逐条照抄 {@code IBlockExtension#getToolModifiedState} 的
     * {@code HOE_TILL} 分支（1.21.1）——草方块 / 土径 / 土 且上方是空气才变耕地。
     * 粗土与缠根土只变泥土、不产生耕地，不在本闸的拦截面上：它们不占额度，拦了等于发明需求。
     */
    public static boolean isTillAction(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.canPerformAction(ItemAbilities.HOE_TILL)) {
            return false;
        }
        Block block = level.getBlockState(pos).getBlock();
        if (block != Blocks.GRASS_BLOCK && block != Blocks.DIRT_PATH && block != Blocks.DIRT) {
            return false;
        }
        return level.getBlockState(pos.above()).isAir();
    }

    /**
     * 顺序固定：先范围、后额度（24 号 §6.3 的第 2、3 步）。
     * 额度取账本的队伍口径（自己 + 在线队友的额度之和，D-117 第 1 条），已用数取坐标集合的基数。
     */
    public static Verdict check(ServerLevel level, Player player, BlockPos pos) {
        if (!inOwnRange(level, player, pos)) {
            return Verdict.NO_CORE;
        }
        FarmlandLedger ledger = FarmlandLedger.of(level);
        return ledger.used(level, player) < ledger.capacity(level, player) ? Verdict.ALLOW : Verdict.QUOTA_FULL;
    }

    /** 任务书的判据原样：{@code inOwnRange(...) && used < capacity}（F-157 交付物第 1 条）。 */
    public static boolean canTill(ServerLevel level, Player player, BlockPos pos) {
        return check(level, player, pos) == Verdict.ALLOW;
    }

    /**
     * 目标位置是否落在**己方**核心的 3 × 3 区块内：己方 = 本人 + 在线队友（D-118 第 2 条），
     * 多个核心取并集（D-118 补充：两个核心范围相邻时，交界处不能出现耕不了地的空洞）。
     *
     * <p>范围判定不依赖方块实体是否在内存里（23 号 §11.2）：坐标由 {@link OutpostCoreIndex} 提供，
     * 几何走 {@link OutpostCoreBlockEntity#inRange(BlockPos, BlockPos)} 的纯函数形态。
     *
     * <p>只认同维度的候选者：{@code OutpostCoreIndex#owned} 返回的是候选者**自己所在维度**的坐标，
     * 跨维度比较区块坐标会造出「下界 (0,0) 的核心让主世界 (0,0) 变成可耕地」这种假命中。
     */
    public static boolean inOwnRange(ServerLevel level, Player player, BlockPos pos) {
        for (ServerPlayer candidate : level.getServer().getPlayerList().getPlayers()) {
            if (candidate.level() != level) {
                continue;
            }
            if (!DeployedOwnership.isAlly(level, player.getUUID(), player.getScoreboardName(), candidate)) {
                continue;
            }
            for (BlockPos core : OutpostCoreIndex.owned(candidate)) {
                if (OutpostCoreBlockEntity.inRange(core, pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 踩踏保护（F-158 / D-120 第 3 项）：坐标处是耕地、落在己方核心的 3 × 3 内、且记在**己方账上**。
     *
     * <p>踩踏事件本身不带「谁的地」这一信息（D-120 第 3 项要求玩家与生物踩踏都拦），因此按在线玩家逐个反查：
     * 谁的账上有这一格、且这一格在他（或其队友）的核心范围内，这一格就是己方耕地。
     * 已知边界：账本归属者全员离线时不受保护——{@link DeployedOwnership#isAlly} 的身份锚点是一个在线
     * {@link Player}，离线时无从解析队伍；这一点写进 F-23B1 证据的未验证项。
     *
     * <p>先判范围再查账本：账本读取会跑一遍实体性校验（逐条 {@code getBlockState}），
     * 范围判定只要几个整数比较，把便宜的放前面能让「范围内没有己方耕地」的常见情形不做账本活。
     */
    public static boolean protectsFromTrample(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.FARMLAND)) {
            return false;
        }
        FarmlandLedger ledger = FarmlandLedger.of(level);
        for (ServerPlayer candidate : level.getServer().getPlayerList().getPlayers()) {
            if (!inOwnRange(level, candidate, pos)) {
                continue;
            }
            if (ledger.ownedBy(level, candidate).contains(pos)) {
                return true;
            }
        }
        return false;
    }
}
