package com.dashpig.farmstorm.farmland;

import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.outpost.OutpostCoreIndex;
import com.dashpig.farmstorm.team.TeamResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * 耕地三道闸（F-157 / F-158 / F-193 / F-195；D-116 / D-118 / D-120 / D-135 / D-138，30 号 §2.2.5 ~ §2.2.7）。
 *
 * <p>第一道是**锄地拦截**（{@link #check}）：范围闸 → 候选集（覆盖 ∩ 己方 ∩ 有空位）→ 取最近 → 返回选中的核。
 * 三态结构不变（{@code ALLOW} / {@code NO_CORE} / {@code QUOTA_FULL}），{@code QUOTA_FULL} 的语义从
 * 「队伍额度满」变成「最近的己方核满了」，语言键名不改（30 号 §9 第 4 条）。
 *
 * <p>第二道是**踩踏保护**（{@link #protectsFromTrample}）：**有绑定即受保护**（A-45）。
 * 理由：踩踏的动作方可能是一只没有队伍的 {@code Monster}，若按实时己方判，生物会把己方田踩坏。
 *
 * <p>第三道是 {@link #inOwnRange} 这个**实时己方**查询（换队后立刻变化）：额度闸与浮窗按它算，
 * 踩踏面不按它算（A-45 的三面差异，29 号 B-18 的收窄版）。
 *
 * <p>「己方」只有一条链路：{@code TeamResolver} 的 Owner × Owner 重载（{@link FarmlandLedger#isAlly}）。
 * 本类不遍历在线玩家，也不缓存队伍快照——归属者在离线时同样判得出来（D-138）。
 */
public final class FarmlandGuard {
    /** 被拒提示：目标位置不在任何己方核心的 3 × 3 内（F-157 第 2 步）。 */
    public static final String MESSAGE_NO_CORE = "message.farmstorm.farmland.no_core";

    /** 被拒提示：最近的己方核心额度已用满，带「已用 / 上限」两个参数（F-157 第 3 步）。 */
    public static final String MESSAGE_QUOTA_FULL = "message.farmstorm.farmland.quota_full";

    /** 判定结果：顺序固定「先范围、后空位」，被拒的两种各对一条提示。 */
    public enum Verdict {
        /** 范围内且选中的核还有空位：放行并在同一刻写绑定。 */
        ALLOW,
        /** 不在任何己方核心的 3 × 3 内。 */
        NO_CORE,
        /** 在范围内，但覆盖这一格的己方核都没有空位。 */
        QUOTA_FULL
    }

    /**
     * 判定 + 选中的核（F-193）：{@code ALLOW} 时 {@code core} 非空，两个拒绝态为 {@code null}。
     * 三态结构不变，只是多带了一格「绑给谁」。
     */
    public record Decision(Verdict verdict, @Nullable FarmlandLedger.CoreKey core) {}

    /**
     * 迁移 / 补建与锄地共用的候选集入口（F-196 / F-190：谓词与迁移**同一条**）。
     *
     * @param farmlandOwner 耕地主（不是核主：D-136 的判据是「与耕地主同盟」）
     * @param exclude       排除的核（迁移时排除正在被拆的那一个）；锄地传 {@code null}
     * @param pending       已经规划出去但尚未写进账本的名额（迁移的「先算完集合再动世界」），可为空 Map
     * @return 选中的核；没有候选（覆盖 / 同盟 / 空位三者任一不成立）返回 {@code null}
     */
    @Nullable
    public static FarmlandLedger.CoreKey selectCore(ServerLevel level, TeamResolver.Owner farmlandOwner, BlockPos pos,
            @Nullable FarmlandLedger.CoreKey exclude, Map<FarmlandLedger.CoreKey, Integer> pending) {
        List<OutpostCoreIndex.Entry> candidates = new ArrayList<>();
        FarmlandLedger ledger = FarmlandLedger.of(level);
        int bonus = FarmstormConfig.outpostCoreFarmlandBonus.get();
        for (OutpostCoreIndex.Entry entry : coveringCores(level, farmlandOwner, pos)) {
            FarmlandLedger.CoreKey key = new FarmlandLedger.CoreKey(entry.dimension(), entry.pos());
            if (exclude != null && exclude.equals(key)) {
                continue;
            }
            int used = ledger.boundCount(key) + pending.getOrDefault(key, 0);
            if (used >= bonus) {
                continue;                                  // 这个核满了：继续看下一个，不在这一步就拒
            }
            candidates.add(entry);
        }
        OutpostCoreIndex.Entry best = nearest(candidates, pos);
        return best == null ? null : new FarmlandLedger.CoreKey(best.dimension(), best.pos());
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
     * 锄地判定（F-193 / D-135 / 30 号 §2.2.7 的状态机）：
     * <ol>
     *   <li>范围闸：覆盖这一格的己方核一个都没有 → {@code NO_CORE}；</li>
     *   <li>候选集：这些核里**先**过滤掉已满的（{@code byCore} 基数 ≥ {@code farmlandBonus}）；</li>
     *   <li>候选为空 → {@code QUOTA_FULL}（「最近的己方核满了」）；</li>
     *   <li>取 {@code distSqr} 最近者，平手按 A-39 三级键 → {@code ALLOW} + 选中的核。</li>
     * </ol>
     *
     * <p>顺序「先过滤再取最近」是硬约束（30 号 §2.2.7 第 4 条）：先取最近再判空位，「最近的满、别处有空位」
     * 会被误拒，与 {@code 16 × 核数} 的总额度自相矛盾。
     */
    public static Decision check(ServerLevel level, Player player, BlockPos pos) {
        TeamResolver.Owner self = new TeamResolver.Owner(player.getUUID(), player.getScoreboardName());
        if (coveringCores(level, self, pos).isEmpty()) {
            return new Decision(Verdict.NO_CORE, null);
        }
        FarmlandLedger.CoreKey core = selectCore(level, self, pos, null, Map.of());
        return core == null ? new Decision(Verdict.QUOTA_FULL, null) : new Decision(Verdict.ALLOW, core);
    }

    /** 任务书的判据原样：{@code inOwnRange(...) && used < capacity} 的等价三态形态。 */
    public static boolean canTill(ServerLevel level, Player player, BlockPos pos) {
        return check(level, player, pos).verdict() == Verdict.ALLOW;
    }

    /**
     * 覆盖该位置的**己方核**条目（不含空位判定）。同维度过滤照旧：跨维度比较区块坐标会造出
     * 「下界 (0,0) 的核心让主世界 (0,0) 变成可耕地」这种假命中。
     *
     * <p>候选核的比较值全部来自 {@link OutpostCoreIndex}（{@code gameTime} / 维度 / 坐标），
     * 因此这条路径不读方块、不加载任何区块（A-39 的结构性保证）。
     */
    private static List<OutpostCoreIndex.Entry> coveringCores(ServerLevel level, TeamResolver.Owner self, BlockPos pos) {
        OutpostCoreIndex index = OutpostCoreIndex.of(level);
        ResourceLocation dimension = level.dimension().location();
        List<OutpostCoreIndex.Entry> result = new ArrayList<>();
        for (UUID owner : index.owners()) {
            if (!FarmlandLedger.isAlly(level, FarmlandLedger.ownerKey(level, owner), self)) {
                continue;
            }
            for (OutpostCoreIndex.Entry entry : index.entriesOf(level.getServer(), owner)) {
                if (!entry.dimension().equals(dimension)) {
                    continue;
                }
                if (!OutpostCoreBlockEntity.inRange(entry.pos(), pos)) {
                    continue;
                }
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * 在候选里取最近（距离口径 {@code BlockPos#distSqr} 的三维欧氏平方，30 号 §2.2.7），
     * 平手按 A-39 三级键：{@code placedGameTime} 升序 → 维度 id → 核坐标字典序（{@code BlockPos#compareTo}）。
     * **不依赖任何 Map / List 的迭代顺序。**
     */
    @Nullable
    private static OutpostCoreIndex.Entry nearest(List<OutpostCoreIndex.Entry> candidates, BlockPos pos) {
        OutpostCoreIndex.Entry best = null;
        for (OutpostCoreIndex.Entry candidate : candidates) {
            if (best == null || compareKeys(candidate, best, pos) < 0) {
                best = candidate;
            }
        }
        return best;
    }

    private static int compareKeys(OutpostCoreIndex.Entry left, OutpostCoreIndex.Entry right, BlockPos pos) {
        int byDistance = Double.compare(left.pos().distSqr(pos), right.pos().distSqr(pos));
        if (byDistance != 0) {
            return byDistance;
        }
        int byGameTime = Long.compare(left.gameTime(), right.gameTime());
        if (byGameTime != 0) {
            return byGameTime;
        }
        int byDimension = left.dimension().toString().compareTo(right.dimension().toString());
        return byDimension != 0 ? byDimension : left.pos().compareTo(right.pos());
    }

    /**
     * 实时己方查询（F-195 要点 3）：这一格**有绑定**，且它的核主对 {@code player} 是己方。
     * 与踩踏面的「有绑定即受保护」不同：这一条有明确的查询者，所以按判定那一刻的队伍算（C-9）。
     */
    public static boolean inOwnRange(ServerLevel level, Player player, BlockPos pos) {
        FarmlandLedger.Binding binding = FarmlandLedger.of(level).bindingAt(pos);
        if (binding == null) {
            return false;
        }
        UUID coreOwner = OutpostCoreIndex.of(level).ownerOf(binding.coreDimension(), binding.corePos());
        if (coreOwner == null) {
            return false;
        }
        return FarmlandLedger.isAlly(level, FarmlandLedger.ownerKey(level, coreOwner),
                new TeamResolver.Owner(player.getUUID(), player.getScoreboardName()));
    }

    /**
     * 踩踏保护（F-158 / F-195 / A-45）：坐标处仍是耕地、且**账本里这一格有绑定** ⇒ 受保护。
     *
     * <p>为什么不按实时己方判：踩踏事件本身不带查询者（{@code BlockEvent.FarmlandTrampleEvent} 只有坐标），
     * 动作方可能是一只没有队伍的 {@code Monster}；按实时己方遍历在线玩家会让生物把己方田踩坏，
     * 与 D-120 第 3 项（玩家与生物都要拦）冲突。绑定只在己方锄地时产生，所以「有绑定」就是
     * 「己方关系曾成立」的存储证据。
     *
     * <p>已知边界（A-49）：绑定只记坐标不记维度，下界同坐标的耕地会被主世界的绑定「借」到保护——本版不修。
     */
    public static boolean protectsFromTrample(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.FARMLAND)) {
            return false;
        }
        return FarmlandLedger.of(level).isBound(pos);
    }
}
