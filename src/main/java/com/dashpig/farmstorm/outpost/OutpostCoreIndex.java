package com.dashpig.farmstorm.outpost;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.OutpostCoreBlock;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.util.DeployedOwnership;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 「玩家 → 该玩家名下据点核心坐标」的服务端索引（D-122 补充 / TECH §3.2）。
 *
 * <p>与 {@link com.dashpig.farmstorm.trap.TrapFifoIndex} 各自独立的类、同一形态的数据：
 * {@code UUID → 坐标列表}，落盘到服务端 {@link SavedData}，读取时惰性校验。
 * 三份索引（陷阱 FIFO、核心、耕地账本）格式统一是本版对 RT-26 的首要缓释。
 *
 * <p>口径三条：只存 UUID 与名字、**不留队伍快照**（C-9：队伍在判定的那一刻由
 * {@link DeployedOwnership} 实时解析，所以换队立刻生效）；坐标带维度（同一坐标在不同维度是两条账）；
 * 坏条目（{@code /setblock air}、爆炸）由 {@link #validated} 自己过期，不需要额外的对账任务。
 *
 * <p>这份查询同时供三处使用：D-120 的耕地额度、D-121 的边界粒子、D-122 的放置上限。
 */
public final class OutpostCoreIndex extends SavedData {
    /** 落盘文件名（{@code world/data/farmstorm_outpost_core.dat}）。 */
    private static final String DATA_NAME = "farmstorm_outpost_core";

    private static final String TAG_OWNERS = "owners";
    private static final String TAG_OWNER_NAME = "name";
    private static final String TAG_ENTRIES = "entries";
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_POS = "pos";

    /** 单条账目：维度 + 坐标。 */
    private record Entry(ResourceLocation dimension, BlockPos pos) {}

    /** 账本本体：玩家 UUID → 该玩家名下的核心坐标（插入顺序即记录顺序）。 */
    private final Map<UUID, List<Entry>> byOwner = new HashMap<>();

    /** 归属者名字：与 {@link Entry} 分开存是因为它是玩家级而不是坐标级的字段，且离线时也要能反查队伍。 */
    private final Map<UUID, String> ownerNames = new HashMap<>();

    /** 全服唯一索引（与 {@code TrapFifoIndex#get} 同一形态，RT-26 待验证 ⑤）。 */
    public static OutpostCoreIndex get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<OutpostCoreIndex>(OutpostCoreIndex::new, OutpostCoreIndex::load),
                        DATA_NAME);
    }

    /** 服务端调用点手里只有 {@link ServerLevel}，这个入口省掉一次 {@code getServer()}。 */
    public static OutpostCoreIndex of(ServerLevel level) {
        return get(level.getServer());
    }

    /**
     * 该玩家名下**仍然有效**的核心数（D-122 第 1、4 项：按玩家计，不按队伍计）。
     *
     * <p>静态入口的形态由 F-152 的放置上限与 F-156 的额度求和共用——两处手里都只有一个 {@link Player}。
     * 客户端没有索引（{@code getServer()} 为 null）返回 0：按 F-25 的既有口径「客户端只做预测，判定在服务端」。
     */
    public static int countOwned(Player player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        return get(server).count(server, player.getUUID());
    }

    /** 该玩家在**当前维度**名下仍然有效的核心坐标（供 D-118 的范围判定用，返回副本）。 */
    public static List<BlockPos> owned(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return List.of();
        }
        return of(level).ownedIn(level, player.getUUID());
    }

    /**
     * 某个核心坐标的归属者是否「己方」（D-120 第 5 项：核心放置者本人或其队友）。
     *
     * <p>入参只有坐标没有维度，因此按条目里记的维度逐个解析——同一个坐标在两个维度各有一个核心时，
     * 任一归属者算己方即为真（调用方本来也只会问自己看得到的那一个）。
     * 队伍比较不在这里另写：走 {@link DeployedOwnership#isAlly} 这一条链路（红线第 4 条）。
     */
    public static boolean isOwnerOrAlly(MinecraftServer server, BlockPos core, Player player) {
        OutpostCoreIndex index = get(server);
        for (UUID owner : List.copyOf(index.byOwner.keySet())) {
            for (Entry entry : index.validated(server, owner)) {
                if (!entry.pos().equals(core)) {
                    continue;
                }
                ServerLevel level = levelOf(server, entry.dimension());
                if (level != null && DeployedOwnership.isAlly(level, owner, index.ownerNames.get(owner), player)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 记一条（{@code OutpostCoreBlockEntity#setOwner} 调用）；同一坐标重复记录按更新处理。
     * 名字随坐标一起更新：玩家改名后下一次放置会覆盖旧名字。
     */
    public void record(ServerLevel level, Player owner, BlockPos pos) {
        ResourceLocation dimension = level.dimension().location();
        // 存 scoreboardName 而不是 displayName：离线队伍反查走 Scoreboard#getPlayersTeam(name)（TeamResolver 的唯一入口）。
        // 对玩家而言它与 getGameProfile().getName() 是同一个串（Player.java:1939-1942）。
        this.ownerNames.put(owner.getUUID(), owner.getScoreboardName());
        List<Entry> entries = this.byOwner.computeIfAbsent(owner.getUUID(), key -> new ArrayList<>());
        entries.removeIf(entry -> entry.pos().equals(pos) && entry.dimension().equals(dimension));
        entries.add(new Entry(dimension, pos));
        this.setDirty();
    }

    /** 摘一条（{@code OutpostCoreBlockEntity#release} 在方块真的没了时调用）。 */
    public void remove(ServerLevel level, UUID owner, BlockPos pos) {
        List<Entry> entries = this.byOwner.get(owner);
        if (entries == null) {
            return;
        }
        if (!entries.removeIf(entry -> entry.pos().equals(pos) && entry.dimension().equals(level.dimension().location()))) {
            return;
        }
        if (entries.isEmpty()) {
            this.byOwner.remove(owner);
            this.ownerNames.remove(owner);
        }
        this.setDirty();
    }

    /** 该玩家在本维度名下仍然有效的核心坐标（惰性校验后，返回副本）。 */
    private List<BlockPos> ownedIn(ServerLevel level, UUID owner) {
        List<Entry> entries = this.validated(level.getServer(), owner);
        if (entries.isEmpty()) {
            return List.of();
        }
        List<BlockPos> positions = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            if (entry.dimension().equals(level.dimension().location())) {
                positions.add(entry.pos());
            }
        }
        return List.copyOf(positions);
    }

    /** 该玩家名下仍然有效的核心数（先跑一遍惰性校验，坏条目不算数）。 */
    private int count(MinecraftServer server, UUID owner) {
        return this.validated(server, owner).size();
    }

    /**
     * 惰性校验（D-122 补充 / 23 号 §11.2）：坐标处方块必须仍是该玩家名下的 {@code outpost_core}，
     * 否则丢弃该条目。区块未加载时无法判定，按「仍然有效」处理——判定的时机交给下一次查询，
     * 不在这里强制加载区块（否则停服 / 区块边走一圈就变成全图加载）。
     */
    private List<Entry> validated(MinecraftServer server, UUID owner) {
        List<Entry> entries = this.byOwner.get(owner);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Entry> kept = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            if (isStillOwnedCore(server, owner, entry)) {
                kept.add(entry);
            }
        }
        if (kept.size() != entries.size()) {
            this.byOwner.put(owner, kept);
            this.setDirty();
        }
        return kept;
    }

    private static boolean isStillOwnedCore(MinecraftServer server, UUID owner, Entry entry) {
        ServerLevel level = levelOf(server, entry.dimension());
        if (level == null) {
            return false;
        }
        if (!level.hasChunkAt(entry.pos())) {
            return true;
        }
        if (!(level.getBlockState(entry.pos()).getBlock() instanceof OutpostCoreBlock)) {
            return false;
        }
        return level.getBlockEntity(entry.pos()) instanceof OutpostCoreBlockEntity core
                && owner.equals(core.ownerUuid());
    }

    @Nullable
    private static ServerLevel levelOf(MinecraftServer server, ResourceLocation dimension) {
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
    }

    /**
     * 落盘形态（与 {@code TrapFifoIndex} 同一层键名，三份索引格式统一）：
     * {@code owners → {<uuid> → {name, entries → [{dimension, pos}]}}}。
     * 名字是玩家级字段，所以放在 UUID 那一层而不是每条坐标上；坐标带维度，同一坐标在两个维度是两条账。
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag owners = new CompoundTag();
        this.byOwner.forEach((owner, entries) -> {
            if (entries.isEmpty()) {
                return;
            }
            CompoundTag ownerTag = new CompoundTag();
            String name = this.ownerNames.get(owner);
            if (name != null && !name.isEmpty()) {
                ownerTag.putString(TAG_OWNER_NAME, name);
            }
            ListTag list = new ListTag();
            for (Entry entry : entries) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString(TAG_DIMENSION, entry.dimension().toString());
                entryTag.putLong(TAG_POS, entry.pos().asLong());
                list.add(entryTag);
            }
            ownerTag.put(TAG_ENTRIES, list);
            owners.put(owner.toString(), ownerTag);
        });
        tag.put(TAG_OWNERS, owners);
        return tag;
    }

    private static OutpostCoreIndex load(CompoundTag tag, HolderLookup.Provider registries) {
        OutpostCoreIndex index = new OutpostCoreIndex();
        CompoundTag owners = tag.getCompound(TAG_OWNERS);
        for (String key : owners.getAllKeys()) {
            UUID owner = parseUuid(key);
            if (owner == null) {
                continue;
            }
            CompoundTag ownerTag = owners.getCompound(key);
            String name = ownerTag.contains(TAG_OWNER_NAME) ? ownerTag.getString(TAG_OWNER_NAME) : null;
            if (name != null && !name.isEmpty()) {
                index.ownerNames.put(owner, name);
            }
            List<Entry> entries = new ArrayList<>();
            ListTag list = ownerTag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                ResourceLocation dimension = ResourceLocation.tryParse(entryTag.getString(TAG_DIMENSION));
                if (dimension == null) {
                    continue;
                }
                entries.add(new Entry(dimension, BlockPos.of(entryTag.getLong(TAG_POS))));
            }
            index.byOwner.put(owner, entries);
        }
        return index;
    }

    @Nullable
    private static UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            Farmstorm.LOGGER.warn("据点核心索引里有一条读不出来的玩家 UUID，已丢弃：{}", raw);
            return null;
        }
    }
}
