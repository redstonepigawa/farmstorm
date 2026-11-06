package com.dashpig.farmstorm.outpost;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.OutpostCoreBlock;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import com.dashpig.farmstorm.util.DeployedOwnership;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
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
 *
 * <p><b>F-189 / A-39</b>：每条账目多带一个 {@code gameTime}（放置时刻），它是「多个候选核里选一个」的
 * 第一级比较键。**比较路径只读本索引**：本类不调用任何区块加载入口，也不读方块实体，
 * 因此「不为比较而加载候选核所在区块」是结构性的，不靠约定（六处 grep 判据里本类计数为 0）。
 * 惰性校验也用 {@code ServerLevel#isLoaded} + {@code getBlockState}——前者不触发加载，后者只在已加载区块上读。
 */
public final class OutpostCoreIndex extends SavedData {
    /** 落盘文件名（{@code world/data/farmstorm_outpost_core.dat}）。 */
    private static final String DATA_NAME = "farmstorm_outpost_core";

    private static final String TAG_OWNERS = "owners";
    private static final String TAG_OWNER_NAME = "name";
    private static final String TAG_ENTRIES = "entries";
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_POS = "pos";
    private static final String TAG_GAME_TIME = "gameTime";

    /** 老档（0.1.0）条目没有 {@code gameTime}：视作「早于一切已知时间」，不伪造时间（A-39 / 30 号 §2.2.10）。 */
    private static final long LEGACY_GAME_TIME = Long.MIN_VALUE;

    /**
     * 单条账目：维度 + 坐标 + 放置时刻（F-189）。
     *
     * <p>{@code gameTime} 是 A-39 三级比较键的第一级；{@code LEGACY_GAME_TIME} 表示这条来自 0.1.0 老档。
     */
    public record Entry(ResourceLocation dimension, BlockPos pos, long gameTime) {}

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
     * <p>静态入口的形态由 F-152 的放置上限决定（F-156 的额度求和已随耕地撤销一并删除）——调用方手里只有一个 {@link Player}。
     * 客户端没有索引（{@code getServer()} 为 null）返回 0：按 F-25 的既有口径「客户端只做预测，判定在服务端」。
     */
    public static int countOwned(Player player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        return get(server).count(server, player.getUUID());
    }

    /** 该玩家在**当前维度**名下仍然有效的核心坐标（供范围判定用，返回副本）。 */
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
     *
     * <p><b>A-42 / 31 号 F-189</b>：本方法**只有定义、没有调用者**，且必须保持这样——它吃 {@code Player}，
     * 离线判不了，绑定制里的己方判定一律走 {@link #entriesOf} + {@code TeamResolver} 的 Owner × Owner 重载。
     */
    public static boolean isOwnerOrAlly(MinecraftServer server, BlockPos core, Player player) {
        OutpostCoreIndex index = get(server);
        for (UUID owner : index.owners()) {
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
     *
     * <p>F-189：{@code gameTime} 与方块实体里的 {@code placedGameTime} 是**同一个变量**（调用点在同一个方法、
     * 同一个 tick），因此两个副本不可能漂移。
     */
    public void record(ServerLevel level, Player owner, BlockPos pos, long gameTime) {
        ResourceLocation dimension = level.dimension().location();
        // 存 scoreboardName 而不是 displayName：离线队伍反查走 Scoreboard#getPlayersTeam(name)（TeamResolver 的唯一入口）。
        // 对玩家而言它与 getGameProfile().getName() 是同一个串（Player.java:1939-1942）。
        this.ownerNames.put(owner.getUUID(), owner.getScoreboardName());
        List<Entry> entries = this.byOwner.computeIfAbsent(owner.getUUID(), key -> new ArrayList<>());
        entries.removeIf(entry -> entry.pos().equals(pos) && entry.dimension().equals(dimension));
        entries.add(new Entry(dimension, pos, gameTime));
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

    /**
     * 只读访问器（F-189 / A-42）：按 owner 枚举**仍然有效**的条目（含其它维度，返回副本）。
     * 己方判定的候选集从这里取，因此不需要「按坐标反查归属者」那种吃 {@code Player} 的形态。
     */
    public List<Entry> entriesOf(MinecraftServer server, UUID owner) {
        return List.copyOf(this.validated(server, owner));
    }

    /** 只读访问器（F-189 / A-42）：索引里出现过的 owner（顺序不参与任何判定，A-39；返回可变副本供排序）。 */
    public List<UUID> owners() {
        return new ArrayList<>(this.byOwner.keySet());
    }

    /** 该玩家名下仍然有效的核心数（F-189 把 {@code private} 改成公开只读重载；v0.2.4 起无调用者，保留为公开只读查询 API）。 */
    public int count(MinecraftServer server, UUID owner) {
        return this.validated(server, owner).size();
    }

    /**
     * 某坐标的核主（按维度 + 坐标反查，不看区块在不在内存）；没有返回 {@code null}。
     */
    @Nullable
    public UUID ownerOf(ResourceLocation dimension, BlockPos pos) {
        for (Map.Entry<UUID, List<Entry>> bucket : this.byOwner.entrySet()) {
            for (Entry entry : bucket.getValue()) {
                if (entry.dimension().equals(dimension) && entry.pos().equals(pos)) {
                    return bucket.getKey();
                }
            }
        }
        return null;
    }

    /** 归属者名字；没有记录返回 {@code null}（离线反查队伍用）。 */
    @Nullable
    public String ownerName(UUID owner) {
        return this.ownerNames.get(owner);
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

    /**
     * 惰性校验（D-122 补充 / 23 号 §11.2）：坐标处方块必须仍是 {@code outpost_core}，
     * 否则丢弃该条目。区块未加载时无法判定，按「仍然有效」处理——判定的时机交给下一次查询，
     * 不在这里强制加载区块（否则停服 / 区块边走一圈就变成全图加载）。
     *
     * <p>F-189：只用 {@code isLoaded}（不触发加载）与 {@code getBlockState}（只在已加载区块上读），
     * 不再触碰方块实体——那会让比较路径依赖「区块当时在不在内存」。
     */
    private List<Entry> validated(MinecraftServer server, UUID owner) {
        List<Entry> entries = this.byOwner.get(owner);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Entry> kept = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            if (isStillOwnedCore(server, entry)) {
                kept.add(entry);
            }
        }
        if (kept.size() != entries.size()) {
            this.byOwner.put(owner, kept);
            this.setDirty();
        }
        return kept;
    }

    private static boolean isStillOwnedCore(MinecraftServer server, Entry entry) {
        ServerLevel level = levelOf(server, entry.dimension());
        if (level == null) {
            return false;
        }
        if (!level.isLoaded(entry.pos())) {
            return true;
        }
        return level.getBlockState(entry.pos()).getBlock() instanceof OutpostCoreBlock;
    }

    @Nullable
    private static ServerLevel levelOf(MinecraftServer server, ResourceLocation dimension) {
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
    }

    /**
     * 核心比较键指纹（A-39 的验收面 / 30 号 §2.2.10）：按 {@code uuid} 排序后逐行
     * {@code uuid gameTime dimension pos} 取 SHA-256 前 16 位。
     *
     * <p>只依赖存档内容与比较键，不依赖遍历顺序或加载时机，因此「同一份存档两次启动给出同一套绑定」
     * 可以只比这一行日志。老档条目的 {@code gameTime} 恒为 {@link #LEGACY_GAME_TIME}，同样可复现。
     */
    public void logFingerprint() {
        List<UUID> keys = this.owners();
        keys.sort(Comparator.comparing(UUID::toString));
        StringBuilder builder = new StringBuilder();
        int entries = 0;
        for (UUID owner : keys) {
            List<Entry> list = this.byOwner.getOrDefault(owner, List.of());
            for (Entry entry : list) {
                builder.append(owner).append(' ').append(entry.gameTime()).append(' ')
                        .append(entry.dimension()).append(' ').append(entry.pos().asLong()).append('\n');
                entries++;
            }
        }
        Farmstorm.LOGGER.info("核心比较键指纹 {}（{} 条）", fingerprint(builder.toString()), entries);
    }

    /** SHA-256 前 16 位（8 字节的 hex）。两处指纹（核心比较键 / 耕地绑定）共用这一份实现。 */
    public static String fingerprint(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    /**
     * 落盘形态（与 {@code TrapFifoIndex} 同一层键名，三份索引格式统一）：
     * {@code owners → {<uuid> → {name, entries → [{dimension, pos, gameTime}]}}}。
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
                entryTag.putLong(TAG_GAME_TIME, entry.gameTime());
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
        int legacy = 0;
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
                long gameTime = LEGACY_GAME_TIME;
                if (entryTag.contains(TAG_GAME_TIME)) {
                    gameTime = entryTag.getLong(TAG_GAME_TIME);
                } else {
                    legacy++;                       // 0.1.0 老档：不伪造时间，也不趁加载时从 BE 补写（A-39）
                }
                entries.add(new Entry(dimension, BlockPos.of(entryTag.getLong(TAG_POS)), gameTime));
            }
            index.byOwner.put(owner, entries);
        }
        if (legacy > 0) {
            Farmstorm.LOGGER.info("核心比较键缺失（老档）：{} 条", legacy);
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
