package com.dashpig.farmstorm.farmland;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.outpost.OutpostCoreIndex;
import com.dashpig.farmstorm.util.DeployedOwnership;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 己方耕地账本（D-117 / TECH §3.4 / §6.1）：{@code 玩家 UUID → 坐标集合}，落盘到服务端 {@link SavedData}。
 *
 * <p>为什么是坐标集合而不是纯计数：玩家自己挖掉一块耕地时纯计数无处减一（D-117 第 4 条）；
 * 集合天然带减项，同时是「核心被拆只铲己方那几块」的数据来源（D-120 第 4 项）。
 * 计数不另存一份 {@code int} 镜像：{@link LinkedHashSet#size()} 本身是 O(1)，第二份真相只会漂移。
 *
 * <p>三层额度（TECH §6.1）：{@code base}（指令值或 config 默认，运行期待定）＋ {@code outpost_core.farmlandBonus}
 * × 该玩家名下核心数；队伍口径是**成员个人额度之和**（D-117 第 1 条），不是每人各算各的。
 *
 * <p>读取时实体性校验：坐标处必须仍是 {@code minecraft:farmland}，否则当场丢弃该条目（D-117 补充）。
 * 与 {@code TrapFifoIndex} 同形——坏条目自己过期，不需要额外的对账任务。
 */
public final class FarmlandLedger extends SavedData {
    /** 落盘文件名（{@code world/data/farmstorm_farmland.dat}）。 */
    private static final String DATA_NAME = "farmstorm_farmland";

    private static final String TAG_OWNERS = "owners";
    private static final String TAG_POSITIONS = "positions";
    private static final String TAG_POS = "pos";

    /** 账本本体：插入顺序即记录顺序（落盘后顺序不变，便于人工核对）。 */
    private final Map<UUID, LinkedHashSet<BlockPos>> byOwner = new HashMap<>();

    /**
     * 运行期覆盖值；{@code null} = 没被指令设置过，取 config 默认（D-116 第 5 项）。
     * 刻意不进 {@link #save}：指令值只作用于当前服务端进程，config 是唯一的持久口径。
     */
    @Nullable
    private Integer baseOverride;

    /** 全服唯一账本（与 {@code TrapFifoIndex#get} 同一形态）。 */
    public static FarmlandLedger get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<FarmlandLedger>(FarmlandLedger::new, FarmlandLedger::load), DATA_NAME);
    }

    /** 服务端调用点手里只有 {@link ServerLevel}，这个入口省掉一次 {@code getServer()}。 */
    public static FarmlandLedger of(ServerLevel level) {
        return get(level.getServer());
    }

    /** 当前生效的个人基础额度：指令值优先，未设置时取 config 默认（TECH §7.5）。 */
    public int base() {
        return this.baseOverride != null ? this.baseOverride : FarmstormConfig.farmlandLimitPerPlayer.get();
    }

    /** 指令的唯一写入口：只改 {@code base}，不碰已开垦数（TECH §7.6 / D-116 第 3 项）。 */
    public void setBase(int value) {
        this.baseOverride = value;
    }

    /** 个人额度 = {@code base} + {@code bonus} × 该玩家名下核心数（TECH §6.1 第 2 行）。 */
    public static int personalCapacity(int base, int bonusPerCore, int ownedCores) {
        return base + bonusPerCore * ownedCores;
    }

    /**
     * 队伍口径 = Σ 每个成员的个人额度（D-117 第 1 条）。
     *
     * <p>3 人队、每人 {@code base} 5、全队 2 个核心 ⇒ {@code 3 × 5 + 16 × 2 = 62}；
     * 不是每人 62 块，也不是「队伍核心数 × 16」之外的第三套算式。
     */
    public static int sumOverMembers(int base, int bonusPerCore, int... ownedCoresPerMember) {
        int total = 0;
        for (int ownedCores : ownedCoresPerMember) {
            total += personalCapacity(base, bonusPerCore, ownedCores);
        }
        return total;
    }

    /** 该玩家的可开垦上限（队伍口径：自己 + 己方在线成员的额度之和）。 */
    public int capacity(ServerLevel level, Player player) {
        List<ServerPlayer> members = this.members(level, player);
        int[] ownedCores = new int[members.size()];
        for (int i = 0; i < members.size(); i++) {
            ownedCores[i] = OutpostCoreIndex.countOwned(members.get(i));
        }
        return sumOverMembers(base(), FarmstormConfig.outpostCoreFarmlandBonus.get(), ownedCores);
    }

    /** 该玩家口径下已开垦的块数：同一坐标被多个成员记过也只算一块（D-118 第 4 条）。 */
    public int used(ServerLevel level, Player player) {
        List<ServerPlayer> members = this.members(level, player);
        if (members.size() == 1) {
            return this.validated(level, members.get(0).getUUID()).size();
        }
        Set<BlockPos> union = new HashSet<>();
        for (ServerPlayer member : members) {
            union.addAll(this.validated(level, member.getUUID()));
        }
        return union.size();
    }

    /**
     * 记账（F-157 通过判定后在同一 tick 调用）。
     *
     * @return 是否真的新增；重复坐标不重复计数（{@code /setblock} 或同一格被两个核心覆盖时）
     */
    public boolean add(ServerLevel level, Player player, BlockPos pos) {
        LinkedHashSet<BlockPos> positions = this.byOwner.computeIfAbsent(player.getUUID(), key -> new LinkedHashSet<>());
        if (!positions.add(pos)) {
            return false;
        }
        this.setDirty();
        return true;
    }

    /**
     * 摘一条（耕地被挖掉 / 炸掉时**当场**减项，D-117 补充）。
     * 坐标是全局键，因此对每个归属者名下都摘一次，调用点不必先知道归属者。
     */
    public boolean remove(BlockPos pos) {
        boolean removed = false;
        Iterator<Map.Entry<UUID, LinkedHashSet<BlockPos>>> iterator = this.byOwner.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, LinkedHashSet<BlockPos>> entry = iterator.next();
            if (!entry.getValue().remove(pos)) {
                continue;
            }
            removed = true;
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }
        if (removed) {
            this.setDirty();
        }
        return removed;
    }

    /** 该玩家名下仍然有效的坐标（供 F-159 的转泥土用；返回副本，调用点改不到账本）。 */
    public List<BlockPos> ownedBy(ServerLevel level, Player player) {
        return ownedBy(level, player.getUUID());
    }

    /**
     * 同上，但不吃在线实体：按 UUID 直接读账本（F-159 的转泥土用）。
     *
     * <p>为什么需要这一层：PvP 的主场景是「核心被敌人拆掉时归属者不在线」，{@link Player} 重载那时
     * 根本拿不到实体，离线归属者自己那几块耕地就会被漏掉，与「账本按 UUID 记账」的语义不符。
     *
     * <p>已知边界：本重载不做 {@code isAlly}。**队友**仍然只能通过在线实体的
     * {@link DeployedOwnership#isAlly} 解析（离线解析不了队伍关系），因此离线时只覆盖归属者本人名下
     * 的那几块，不覆盖离线队友名下那几块。
     */
    public List<BlockPos> ownedBy(ServerLevel level, UUID owner) {
        return List.copyOf(this.validated(level, owner));
    }

    /**
     * 队伍口径的成员集合：只取在线玩家，逐个用 {@link DeployedOwnership#isAlly} 判「是否己方」
     * （C-9 的唯一链路，不另写队伍比较）。归属者本人恒为真，所以单人时这里就是只含自己的单元素列表。
     *
     * <p>离线成员不参与：{@link OutpostCoreIndex#countOwned} 需要在线实体，核心数拿不到。
     */
    private List<ServerPlayer> members(ServerLevel level, Player player) {
        List<ServerPlayer> online = level.getServer().getPlayerList().getPlayers();
        List<ServerPlayer> allies = new ArrayList<>(online.size());
        UUID ownerUuid = player.getUUID();
        String ownerName = player.getScoreboardName();
        for (ServerPlayer candidate : online) {
            if (DeployedOwnership.isAlly(level, ownerUuid, ownerName, candidate)) {
                allies.add(candidate);
            }
        }
        return allies;
    }

    /**
     * 惰性实体性校验（D-117 补充）：坐标处方块必须仍是 {@code minecraft:farmland}，否则丢弃该条目。
     * 区块未加载时无法判定，按「仍然有效」处理——判定的时机交给下一次查询，不在这里强制加载区块
     * （与 {@code TrapFifoIndex} 同形）。
     */
    private List<BlockPos> validated(ServerLevel level, UUID owner) {
        LinkedHashSet<BlockPos> positions = this.byOwner.get(owner);
        if (positions == null || positions.isEmpty()) {
            return List.of();
        }
        List<BlockPos> kept = new ArrayList<>(positions.size());
        for (BlockPos pos : positions) {
            if (isStillFarmland(level, pos)) {
                kept.add(pos);
            }
        }
        if (kept.size() != positions.size()) {
            this.byOwner.put(owner, new LinkedHashSet<>(kept));
            this.setDirty();
        }
        return kept;
    }

    private static boolean isStillFarmland(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return true;
        }
        return level.getBlockState(pos).is(Blocks.FARMLAND);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag owners = new CompoundTag();
        this.byOwner.forEach((owner, positions) -> {
            ListTag list = new ListTag();
            for (BlockPos pos : positions) {
                CompoundTag entry = new CompoundTag();
                entry.putLong(TAG_POS, pos.asLong());
                list.add(entry);
            }
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.put(TAG_POSITIONS, list);
            owners.put(owner.toString(), ownerTag);
        });
        tag.put(TAG_OWNERS, owners);
        return tag;
    }

    private static FarmlandLedger load(CompoundTag tag, HolderLookup.Provider registries) {
        FarmlandLedger ledger = new FarmlandLedger();
        CompoundTag owners = tag.getCompound(TAG_OWNERS);
        for (String key : owners.getAllKeys()) {
            UUID owner = parseUuid(key);
            if (owner == null) {
                continue;
            }
            LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
            ListTag list = owners.getCompound(key).getList(TAG_POSITIONS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                positions.add(BlockPos.of(list.getCompound(i).getLong(TAG_POS)));
            }
            if (!positions.isEmpty()) {
                ledger.byOwner.put(owner, positions);
            }
        }
        return ledger;
    }

    @Nullable
    private static UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            Farmstorm.LOGGER.warn("耕地账本里有一条读不出来的玩家 UUID，已丢弃：{}", raw);
            return null;
        }
    }
}
