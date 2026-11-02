package com.dashpig.farmstorm.farmland;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.outpost.OutpostCoreIndex;
import com.dashpig.farmstorm.team.TeamResolver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 耕地-核心绑定的服务端账本（F-188 / D-134 ~ D-139 / 30 号 §2.2）：{@code 耕地坐标 → 唯一一个核}，
 * 落盘到 {@code world/data/farmstorm_farmland.dat}，格式 {@code dataVersion: 2}。
 *
 * <p>三份本体（30 号 §2.2.2）：{@link #bindings} 是唯一真相、落盘；{@link #byCore} 是由 bindings 派生的
 * 内存反查（「这个核还剩几个空位」）、**不落盘**；{@link #orphans} 是老档（v1）补不上绑定的遗留条目、
 * **落盘**（不落盘则补建不可重试，D-137 的「保持不动」会退化成「永久遗忘」）。
 *
 * <p>为什么 {@code binding.owner} 与核主可以不同：D-136 的迁移判据是「与**耕地主**同盟」，
 * 而耕地主是开垦者、核主是放置者，两者在同一队伍里可能不是同一个人 ⇒ 必须逐条存 owner。
 *
 * <p>为什么耕地坐标仍然不带维度（沿用 v1）：老档的 {@code pos} 只有一个 long，突然加维度字段会让迁移分叉；
 * 「耕地与它的核同维度」在绑定时已由候选集过滤保证，维度由 {@code coreDimension} 承载即可。
 * 已知边界（A-49，本版不修）：同一 {@code (x,y,z)} 在主世界与下界各有一块耕地时两边会撞。
 *
 * <p><b>版本兼容</b>：v1 的 {@code owners → {uuid → {positions}}} 全部读成 {@link #orphans}——
 * owners 段里没有核信息，直接当绑定是发明数据（30 号 §2.2.3 第 2 条）。{@code save} 永远写 v2、
 * 不再写 owners（只升不降：0.1.0 读 v2 档会得到空账本，等价于「所有耕地变未绑定」，不铲世界）。
 */
public final class FarmlandLedger extends SavedData {
    /** 落盘文件名（{@code world/data/farmstorm_farmland.dat}）。 */
    private static final String DATA_NAME = "farmstorm_farmland";

    private static final String TAG_DATA_VERSION = "dataVersion";
    private static final String TAG_BINDINGS = "bindings";
    private static final String TAG_ORPHANS = "orphans";
    private static final String TAG_OWNERS = "owners";
    private static final String TAG_POSITIONS = "positions";
    private static final String TAG_POS = "pos";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_CORE_DIMENSION = "coreDimension";
    private static final String TAG_CORE_POS = "corePos";

    /** 本版写的存档版本；缺失视为 1（= 0.1.0 格式）。 */
    private static final int DATA_VERSION = 2;

    /**
     * 核的键（跨维度唯一）。{@code compareTo} 就是 A-39 的第二、三级比较键（维度 id → 核坐标字典序）；
     * 第一级 {@code placedGameTime} 不在键里，由 {@link OutpostCoreIndex.Entry#gameTime()} 提供。
     */
    public record CoreKey(ResourceLocation dimension, BlockPos pos) implements Comparable<CoreKey> {
        @Override
        public int compareTo(CoreKey other) {
            int byDimension = this.dimension.toString().compareTo(other.dimension.toString());
            return byDimension != 0 ? byDimension : this.pos.compareTo(other.pos);
        }
    }

    /** 一条绑定：耕地 → 唯一一个核。{@code owner} 是**耕地主**（开垦者），不是核主。 */
    public record Binding(BlockPos pos, UUID owner, ResourceLocation coreDimension, BlockPos corePos) {}

    /** 绑定指纹的排序（30 号 §2.2.10）：{@code (pos, coreDimension, corePos)} 升序，与插入顺序无关。 */
    private static final Comparator<Binding> BINDING_ORDER = Comparator.comparing(Binding::pos)
            .thenComparing(binding -> binding.coreDimension().toString())
            .thenComparing(Binding::corePos);

    /** 唯一真相：耕地坐标 → 绑定。插入顺序只为落盘可读，**不参与任何判定**（A-39 / A-47）。 */
    private final LinkedHashMap<BlockPos, Binding> bindings = new LinkedHashMap<>();

    /** 内存反查：核 → 该核名下的耕地。由 bindings 派生，不落盘；rebuildByCore 是唯一的重建口。 */
    private final Map<CoreKey, LinkedHashSet<BlockPos>> byCore = new HashMap<>();

    /** 老档补不上的遗留：坐标 → 耕地主。落盘顺序只作诊断，**不参与判定**（补建顺序按 A-47 全序排序）。 */
    private final LinkedHashMap<BlockPos, UUID> orphans = new LinkedHashMap<>();

    /** 全服唯一账本（与 TrapFifoIndex#get 同一形态）。 */
    public static FarmlandLedger get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<FarmlandLedger>(FarmlandLedger::new, FarmlandLedger::load), DATA_NAME);
    }

    /** 服务端调用点手里只有 {@link ServerLevel}，这个入口省掉一次 {@code getServer()}。 */
    public static FarmlandLedger of(ServerLevel level) {
        return get(level.getServer());
    }

    // ------------------------------------------------------------------ 写入口（F-188）

    /**
     * 绑一条：耕地 pos 归 core 管，耕地主是 owner（F-194 在锄地成功后调用）。
     * 同一坐标重复绑定时旧的反查项一起摘掉，不留幽灵（30 号 §9 第 3 条）。
     */
    public void bind(UUID owner, CoreKey core, BlockPos pos) {
        Binding previous = this.bindings.put(pos, new Binding(pos, owner, core.dimension(), core.pos()));
        if (previous != null) {
            unlinkByCore(previous);
        }
        linkByCore(core, pos);
        this.orphans.remove(pos);
        this.setDirty();
    }

    /** 摘一条绑定（核被拆、方块变泥土时调用）。 */
    public boolean unbind(BlockPos pos) {
        Binding removed = this.bindings.remove(pos);
        if (removed == null) {
            return false;
        }
        unlinkByCore(removed);
        this.setDirty();
        return true;
    }

    /** 改绑：耕地主不变，只换核（F-196 的迁移出口）。 */
    public boolean migrate(BlockPos pos, CoreKey core) {
        Binding old = this.bindings.get(pos);
        if (old == null) {
            return false;
        }
        unlinkByCore(old);
        this.bindings.put(pos, new Binding(pos, old.owner(), core.dimension(), core.pos()));
        linkByCore(core, pos);
        this.setDirty();
        return true;
    }

    /** 补建成绑定：orphans 少一条、bindings 多一条（F-190 的补建出口）。 */
    public void adoptOrphan(UUID owner, CoreKey core, BlockPos pos) {
        this.orphans.remove(pos);
        bind(owner, core, pos);
    }

    /**
     * 摘一条（耕地被挖掉 / 炸掉 / 核被毁时的降级出口）：绑定与孤儿条目都摘，调用点不必先知道是哪种。
     *
     * <p>A-37：绑定制引入「摘绑定」后本类不再只有一个写入口，验收不得写「调用点数 = 1」。
     */
    public boolean remove(BlockPos pos) {
        boolean unbound = unbind(pos);
        boolean orphaned = this.orphans.remove(pos) != null;
        return unbound || orphaned;
    }

    /**
     * 兼容入口（F-188 到 F-194 之间的过渡）：没有选中的核时按 D-137 记成孤儿，不发明绑定。
     * F-194 落地后调用点改走 {@link #bind}，本方法在 F-194 的验收里读数归零。
     */
    public boolean add(ServerLevel level, Player player, BlockPos pos) {
        return add(pos, player.getUUID());
    }

    private boolean add(BlockPos pos, UUID owner) {
        if (this.bindings.containsKey(pos)) {
            return false;
        }
        if (this.orphans.put(pos, owner) != null) {
            return false;
        }
        this.setDirty();
        return true;
    }

    /** 从 bindings 重建 {@link #byCore}。纯内存派生，**不** {@code setDirty()}（30 号 §2.2.4）。 */
    public void rebuildByCore() {
        this.byCore.clear();
        for (Binding binding : this.bindings.values()) {
            linkByCore(new CoreKey(binding.coreDimension(), binding.corePos()), binding.pos());
        }
    }

    private void linkByCore(CoreKey core, BlockPos pos) {
        this.byCore.computeIfAbsent(core, key -> new LinkedHashSet<>()).add(pos);
    }

    private void unlinkByCore(Binding binding) {
        CoreKey core = new CoreKey(binding.coreDimension(), binding.corePos());
        Set<BlockPos> positions = this.byCore.get(core);
        if (positions == null) {
            return;
        }
        positions.remove(binding.pos());
        if (positions.isEmpty()) {
            this.byCore.remove(core);
        }
    }

    // ------------------------------------------------------------------ 读取

    /** 某核名下的全部绑定（F-196 的「只处理本核名下」）。顺序不参与判定，调用方自己排。 */
    public List<Binding> bindingsOf(CoreKey core) {
        Set<BlockPos> positions = this.byCore.get(core);
        if (positions == null || positions.isEmpty()) {
            return new ArrayList<>();                      // 可变副本：调用点会排序（迁移的确定性顺序）
        }
        List<Binding> result = new ArrayList<>(positions.size());
        for (BlockPos pos : positions) {
            Binding binding = this.bindings.get(pos);
            if (binding != null) {
                result.add(binding);
            }
        }
        return result;
    }

    /** 某核已占用的格数（「还剩几个空位」= farmlandBonus − 本数，30 号 §2.2.6）。 */
    public int boundCount(CoreKey core) {
        Set<BlockPos> positions = this.byCore.get(core);
        return positions == null ? 0 : positions.size();
    }

    /** 遗留条目数（F-190 的 total）。 */
    public int orphanCount() {
        return this.orphans.size();
    }

    /** 全部绑定（只读副本；指纹与验收从这取）。 */
    public List<Binding> bindings() {
        return List.copyOf(this.bindings.values());
    }

    /** 这一格有没有绑定（F-195 的「有绑定即受保护」）。 */
    public boolean isBound(BlockPos pos) {
        return this.bindings.containsKey(pos);
    }

    /** 这一格的绑定；没有返回 {@code null}。 */
    @Nullable
    public Binding bindingAt(BlockPos pos) {
        return this.bindings.get(pos);
    }

    /** 孤儿条目的坐标集合（F-190 的补建工作集，排序在补建里做）。 */
    public List<BlockPos> orphanPositions() {
        return List.copyOf(this.orphans.keySet());
    }

    /** 孤儿条目的耕地主；没有返回 {@code null}。 */
    @Nullable
    public UUID orphanOwner(BlockPos pos) {
        return this.orphans.get(pos);
    }

    /** 该玩家名下仍然有效的坐标（返回副本，调用点改不到账本）。 */
    public List<BlockPos> ownedBy(ServerLevel level, Player player) {
        return ownedBy(level, player.getUUID());
    }

    /**
     * 同上，但不吃在线实体：按 UUID 直接读绑定表（离线归属者也查得到）。
     *
     * <p>已知边界：本重载不做 isAlly，只覆盖归属者**本人**名下的那几块。
     */
    public List<BlockPos> ownedBy(ServerLevel level, UUID owner) {
        List<BlockPos> kept = new ArrayList<>();
        for (Binding binding : this.bindings.values()) {
            if (!binding.owner().equals(owner)) {
                continue;
            }
            if (isStillFarmland(level, binding)) {
                kept.add(binding.pos());
            }
        }
        return List.copyOf(kept);
    }

    // ------------------------------------------------------------------ 额度与已用（F-191 改口径）

    /** 己方核数：索引里 owner 与 player 互为己方的全部条目（含其它维度、离线也有）。 */
    public int allyCoreCount(ServerLevel level, Player player) {
        OutpostCoreIndex index = OutpostCoreIndex.of(level);
        TeamResolver.Owner self = new TeamResolver.Owner(player.getUUID(), player.getScoreboardName());
        int cores = 0;
        for (UUID owner : index.owners()) {
            if (!isAlly(level, ownerKey(level, owner), self)) {
                continue;
            }
            cores += index.count(level.getServer(), owner);
        }
        return cores;
    }

    /** 该玩家的可开垦上限 = {@code farmlandBonus × 己方核数}（D-139；不再有 base 项）。 */
    public int capacity(ServerLevel level, Player player) {
        return FarmstormConfig.outpostCoreFarmlandBonus.get() * allyCoreCount(level, player);
    }

    /** 该玩家口径下已绑定的块数：bindings 里 owner 与 player 互为己方的条数；orphans 不计入（D-139）。 */
    public int used(ServerLevel level, Player player) {
        validateAll(level.getServer());                    // 「与绑定同源」：脏条目先摘，再数
        TeamResolver.Owner self = new TeamResolver.Owner(player.getUUID(), player.getScoreboardName());
        int count = 0;
        for (Binding binding : this.bindings.values()) {
            if (isAlly(level, ownerKey(level, binding.owner()), self)) {
                count++;
            }
        }
        return count;
    }

    // ------------------------------------------------------------------ 惰性校验与指纹

    /**
     * 惰性实体性校验（D-117 补充）：坐标处方块必须仍是 {@code minecraft:farmland}，否则丢弃该条目。
     * 区块未加载时无法判定，按「仍然有效」处理——判定的时机交给下一次查询，不在这里强制加载区块。
     *
     * <p><b>必须同时剪 byCore</b>（30 号 §9 第 3 条）：只剪 bindings 会让幽灵耕地继续占住那 16 格容量。
     */
    private boolean isStillFarmland(ServerLevel level, Binding binding) {
        ServerLevel target = dimensionOf(level.getServer(), binding.coreDimension());
        if (target == null) {
            return false;
        }
        BlockPos pos = binding.pos();
        if (!target.isLoaded(pos)) {
            return true;
        }
        if (target.getBlockState(pos).is(Blocks.FARMLAND)) {
            return true;
        }
        this.bindings.remove(pos);
        unlinkByCore(binding);
        this.setDirty();
        Farmstorm.LOGGER.info("耕地绑定：{} 已不是耕地，摘除（剩 {} 条）", pos, this.bindings.size());
        return false;
    }

    /**
     * 逐条校验全部绑定（启动补建前跑一次；{@code used} 不调它——它是 O(n) 的方块读）。
     * 两个出口都剪：bindings 与 byCore（30 号 §9 第 3 条）。
     */
    public void validateAll(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        for (Binding binding : List.copyOf(this.bindings.values())) {
            isStillFarmland(overworld, binding);
        }
    }

    /**
     * 耕地绑定指纹（A-39 / 30 号 §2.2.10 的验收面）：按 {@code (pos, coreDimension, corePos)} 排序后
     * 逐行 {@code pos|coreDimension|corePos} 取 SHA-256 前 16 位。与插入顺序、加载时机都无关。
     *
     * <p>同时打「反查集合 N 条」：幽灵耕地被摘掉时两行必须一起变小（F-188 验收 6）。
     */
    public void logFingerprint(MinecraftServer server) {
        List<Binding> sorted = new ArrayList<>(this.bindings.values());
        sorted.sort(BINDING_ORDER);
        StringBuilder builder = new StringBuilder();
        for (Binding binding : sorted) {
            builder.append(binding.pos().asLong()).append('|')
                    .append(binding.coreDimension()).append('|')
                    .append(binding.corePos().asLong()).append('\n');
        }
        Farmstorm.LOGGER.info("耕地绑定指纹 {}（{} 条）", OutpostCoreIndex.fingerprint(builder.toString()), sorted.size());
        int reverse = 0;
        for (Set<BlockPos> positions : this.byCore.values()) {
            reverse += positions.size();
        }
        Farmstorm.LOGGER.info("反查集合 {} 条", reverse);
        OutpostCoreIndex.get(server).logFingerprint();
    }

    // ------------------------------------------------------------------ 启动补建（F-190 / D-137）

    /**
     * 老档补建（F-190 / D-137 / A-47 rev.2）：把 {@code orphans} 里的耕地绑到核上；补不上的保持耕地、
     * 保持无绑定（「只是不受保护」，D-137）。
     *
     * <p>三条硬约束：
     * <ol>
     *   <li><b>遍历顺序 = {@code BlockPos#compareTo}（y → z → x）单级全序</b>：耕地这一侧的落盘只有 {@code pos}、
     *       没有维度字段，单级即全序；{@code orphans} 的落盘顺序与任何 Map 的迭代顺序都**不参与判定**。</li>
     *   <li><b>整段不做任何方块写入</b>：本类从头到尾没有方块写入调用（六处 grep 判据之一）。</li>
     *   <li>容量 16 是硬顶：顺序决定老档哪些田能绑上，所以顺序必须是验收面。</li>
     * </ol>
     *
     * <p>与 30 号 §2.2.8 伪代码的**唯一差别**：伪代码首行有「区块已加载 && 方块已不是 farmland → 摘除孤儿」，
     * 本实现不做——v1 的 {@code orphans} 条目（以及 A-47 rev.2 确认的 v2 段）都没有维度字段，
     * 无法定位该 {@code pos} 属于哪个 {@link ServerLevel}；按维度逐个试会在 A-49 的跨维度同坐标上误删。
     * 该清理改由 {@link #validateAll} 在绑定条目上做（孤儿条目没有维度，本来也不占容量）。
     */
    public void backfill(MinecraftServer server) {
        ServerLevel level = server.overworld();
        validateAll(server);
        int total = this.orphans.size();
        int bound = 0;
        int kept = 0;
        if (total > 0) {
            List<BlockPos> work = new ArrayList<>(this.orphans.keySet());
            work.sort(BlockPos::compareTo);                // A-47 rev.2：单级全序，再逐条处理
            for (BlockPos pos : work) {
                UUID owner = this.orphans.get(pos);
                if (owner == null) {
                    continue;
                }
                FarmlandLedger.CoreKey core = FarmlandGuard.selectCore(level, ownerKey(level, owner), pos, null, Map.of());
                if (core == null) {
                    kept++;                                // 补不上：保持不动，也不动世界
                    continue;
                }
                adoptOrphan(owner, core, pos);
                bound++;
            }
        }
        Farmstorm.LOGGER.info("耕地补建：绑定 {} 块 / 保持未绑定 {} 块（老档共 {} 块，按 BlockPos 全序）", bound, kept, total);
        logFingerprint(server);
    }

    @Nullable
    private static ServerLevel dimensionOf(MinecraftServer server, ResourceLocation dimension) {
        if (server == null) {
            return null;
        }
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
    }

    // ------------------------------------------------------------------ 落盘 / 读盘

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(TAG_DATA_VERSION, DATA_VERSION);
        ListTag bindingList = new ListTag();
        for (Binding binding : this.bindings.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_POS, binding.pos().asLong());
            entry.putString(TAG_OWNER, binding.owner().toString());
            entry.putString(TAG_CORE_DIMENSION, binding.coreDimension().toString());
            entry.putLong(TAG_CORE_POS, binding.corePos().asLong());
            bindingList.add(entry);
        }
        tag.put(TAG_BINDINGS, bindingList);
        ListTag orphanList = new ListTag();
        this.orphans.forEach((pos, owner) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_POS, pos.asLong());
            entry.putString(TAG_OWNER, owner.toString());
            orphanList.add(entry);
        });
        tag.put(TAG_ORPHANS, orphanList);
        return tag;
    }

    private static FarmlandLedger load(CompoundTag tag, HolderLookup.Provider registries) {
        FarmlandLedger ledger = new FarmlandLedger();
        int version = tag.contains(TAG_DATA_VERSION) ? tag.getInt(TAG_DATA_VERSION) : 1;   // 缺失 = v1
        ledger.readOrphans(tag);
        if (version >= 2) {
            ledger.readBindings(tag);
        }
        // v1 的 owners 段一律进 orphans，不猜绑定（30 号 §2.2.3 第 2 条；
        // 人工拼过的「v2 + owners」档同此处理——双写两套格式就是双源）。
        int legacy = ledger.readLegacyOwners(tag);
        if (legacy > 0) {
            Farmstorm.LOGGER.info("耕地账本旧格式：{} 条读入 orphans（不猜绑定）", legacy);
        }
        ledger.rebuildByCore();
        return ledger;
    }

    private void readBindings(CompoundTag tag) {
        ListTag list = tag.getList(TAG_BINDINGS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID owner = parseUuid(entry.getString(TAG_OWNER));
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString(TAG_CORE_DIMENSION));
            if (owner == null || dimension == null) {
                continue;
            }
            BlockPos pos = BlockPos.of(entry.getLong(TAG_POS));
            this.bindings.put(pos, new Binding(pos, owner, dimension, BlockPos.of(entry.getLong(TAG_CORE_POS))));
        }
    }

    private void readOrphans(CompoundTag tag) {
        ListTag list = tag.getList(TAG_ORPHANS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID owner = parseUuid(entry.getString(TAG_OWNER));
            if (owner == null) {
                continue;
            }
            this.orphans.put(BlockPos.of(entry.getLong(TAG_POS)), owner);
        }
    }

    /** v1 的 {@code owners → {uuid → {positions → [{pos}]}}}。返回读入的条数（&gt; 0 时打一条 INFO）。 */
    private int readLegacyOwners(CompoundTag tag) {
        CompoundTag owners = tag.getCompound(TAG_OWNERS);
        int read = 0;
        for (String key : owners.getAllKeys()) {
            UUID owner = parseUuid(key);
            if (owner == null) {
                continue;
            }
            ListTag list = owners.getCompound(key).getList(TAG_POSITIONS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                BlockPos pos = BlockPos.of(list.getCompound(i).getLong(TAG_POS));
                if (this.bindings.containsKey(pos)) {
                    continue;                        // v2 段已占这一格：不覆盖真绑定
                }
                if (this.orphans.putIfAbsent(pos, owner) == null) {
                    read++;
                }
            }
        }
        return read;
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

    /** 己方判定（UUID 侧，离线有效）：走 {@code TeamResolver} 的 Owner × Owner 重载（F-187）。 */
    public static boolean isAlly(ServerLevel level, TeamResolver.Owner owner, TeamResolver.Owner target) {
        return Farmstorm.TEAMS.isAlly(level, owner, target);
    }

    /** 把一个 UUID 解析成可离线比较的 {@link TeamResolver.Owner}：名字先看索引、再看在线的同一玩家。 */
    public static TeamResolver.Owner ownerKey(ServerLevel level, UUID uuid) {
        String name = OutpostCoreIndex.of(level).ownerName(uuid);
        if (name == null) {
            ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
            if (online != null) {
                name = online.getScoreboardName();
            }
        }
        return new TeamResolver.Owner(uuid, name);
    }
}
