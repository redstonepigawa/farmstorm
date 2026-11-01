package com.dashpig.farmstorm.trap;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.block.DeployedCropBlock;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.util.CropCategory;
import com.dashpig.farmstorm.util.CropProfile;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 「玩家 → 该玩家名下 TRAP 部署物坐标」的有序索引（D-115 / TECH §5.4）。
 *
 * <p>现有部署账本只有计数（{@code Map<UUID, DeployCounter>}，不落盘），回答不了「最早的是哪一个」；
 * 本类是那张反向索引：{@code UUID → 按 placedGameTime 升序的坐标列表}，落盘到服务端 {@link SavedData}，
 * 因此服务端重启后重建出的顺序与第一次部署一致（否则 FIFO 会退化成 LIFO）。
 *
 * <p>名额只数 TRAP 那 4 个 kind（§5.4 第 1 步）：炮台与建造类既不写入也不占名额。
 * 坐标带维度：挤出要能在另一个维度里执行移除，缺维度就会空转（RT-26 第 6 条）。
 */
public final class TrapFifoIndex extends SavedData {
    /** 落盘文件名（{@code world/data/farmstorm_trap_fifo.dat}）。 */
    private static final String DATA_NAME = "farmstorm_trap_fifo";

    private static final String TAG_OWNERS = "owners";
    private static final String TAG_ENTRIES = "entries";
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_POS = "pos";
    private static final String TAG_PLACED_GAME_TIME = "placedGameTime";

    /** 单条账目：维度 + 坐标 + 放置刻号（升序键）。 */
    private record Entry(ResourceLocation dimension, BlockPos pos, long placedGameTime) {}

    private final Map<UUID, List<Entry>> byOwner = new HashMap<>();

    /** 全服唯一索引：三份服务端索引里本类先落地，其余两份按同一形状复用（RT-26 待验证 ⑤）。 */
    public static TrapFifoIndex get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<TrapFifoIndex>(TrapFifoIndex::new, TrapFifoIndex::load), DATA_NAME);
    }

    /** 服务端调用点手里只有 {@link ServerLevel}，这个入口省掉一次 {@code getServer()}。 */
    public static TrapFifoIndex of(ServerLevel level) {
        return get(level.getServer());
    }

    /** 是否占陷阱名额：判据只有 {@link CropCategory#TRAP} 一个（§5.4 第 1 步），不另立清单。 */
    public static boolean isTrapKind(DeployedKind kind) {
        return CropProfile.byKind(kind).has(CropCategory.TRAP);
    }

    /**
     * 放置陷阱前的挤出闸（D-115 第 1、3 项）：名额已满就挤掉队首一个，一轮最多 1 个。
     * 必须在写入新方块之前调用——否则第 17 个会先占位、把某个刚放下的挤掉（§5.4 第 2 步）。
     * 放炮台 / 建造类不进这里，它们不占名额，也就不该挤掉别人的陷阱。
     */
    public static void makeRoom(ServerLevel level, @Nullable Player player, DeployedKind placing) {
        if (player == null || !isTrapKind(placing)) {
            return;
        }
        MinecraftServer server = level.getServer();
        TrapFifoIndex index = of(level);
        if (index.count(server, player.getUUID()) < FarmstormConfig.trapMaxTrapsPerPlayer.get()) {
            return;
        }
        index.evictOldest(server, player.getUUID());
    }

    /** 记一条部署（{@code DeployedCropBlockEntity#setOwner} 调用）；同一坐标重复记录按更新处理。 */
    public void record(ServerLevel level, UUID owner, BlockPos pos, long placedGameTime, DeployedKind kind) {
        if (!isTrapKind(kind)) {
            return;
        }
        ResourceLocation dimension = level.dimension().location();
        List<Entry> entries = this.byOwner.computeIfAbsent(owner, key -> new ArrayList<>());
        entries.removeIf(entry -> entry.pos().equals(pos) && entry.dimension().equals(dimension));
        entries.add(new Entry(dimension, pos, placedGameTime));
        entries.sort(Comparator.comparingLong(Entry::placedGameTime));
        this.setDirty();
    }

    /** 摘一条账目（{@code DeployedCropBlockEntity#setRemoved} 在方块真的没了时调用）。 */
    public void remove(ServerLevel level, UUID owner, BlockPos pos) {
        this.removeAt(owner, level.dimension().location(), pos);
    }

    /** 该玩家名下仍然有效的陷阱数（先跑一遍惰性校验，坏条目不算数）。 */
    public int count(MinecraftServer server, UUID owner) {
        return this.validated(server, owner).size();
    }

    /**
     * 挤出队首：取升序列表的第一个，走既有的 {@code consume()} 路径移除
     * （{@code removeBlock(pos,false)} + 破坏粒子，**无掉落**、不退还物品，D-115 第 3 项）。
     * 该坐标的方块实体在移除过程中会把账目自己摘掉，这里不再重复写。
     *
     * <p>这是本类唯一一处**允许**加载区块的读点：队首若在没加载的区块里，必须把它取出来才挤得掉；
     * 挤不掉的（坐标已空但区块没加载）按坏条目摘掉，不空转。
     */
    private boolean evictOldest(MinecraftServer server, UUID owner) {
        List<Entry> entries = this.validated(server, owner);
        if (entries.isEmpty()) {
            return false;
        }
        Entry oldest = entries.get(0);
        ServerLevel level = levelOf(server, oldest.dimension());
        if (level != null && level.getBlockEntity(oldest.pos()) instanceof DeployedCropBlockEntity deployed
                && isTrapKind(deployed.kind())) {
            deployed.consume();
            return true;
        }
        this.removeAt(owner, oldest.dimension(), oldest.pos());
        return false;
    }

    /**
     * 惰性校验（D-115 补充 / §5.4 第 6 步）：坐标处方块必须仍是该玩家名下的 TRAP 部署物，否则丢弃该条目。
     * 坏条目（{@code /setblock air}、爆炸、被别的路径清掉）因此会自己过期，不需要额外的对账任务。
     * 区块未加载时无法判定，按「仍然有效」处理——判定的时机交给下一次查询，不在这里强制加载区块。
     */
    private List<Entry> validated(MinecraftServer server, UUID owner) {
        List<Entry> entries = this.byOwner.get(owner);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Entry> kept = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            if (isStillOwnedTrap(server, owner, entry)) {
                kept.add(entry);
            }
        }
        if (kept.size() != entries.size()) {
            Farmstorm.LOGGER.info("陷阱 FIFO 索引惰性校验：{} 条 → {} 条（丢弃 {} 条坏条目，玩家 {}）",
                    entries.size(), kept.size(), entries.size() - kept.size(), owner);
            this.byOwner.put(owner, kept);
            this.setDirty();
        }
        return kept;
    }

    private static boolean isStillOwnedTrap(MinecraftServer server, UUID owner, Entry entry) {
        ServerLevel level = levelOf(server, entry.dimension());
        if (level == null) {
            return false;
        }
        // 与 DeployedCropBlockEntity#releaseTrapSlot 同一读法：getChunkNow 只查已加载的区块、不触发加载。
        LevelChunk chunk = level.getChunkSource().getChunkNow(entry.pos().getX() >> 4, entry.pos().getZ() >> 4);
        if (chunk == null) {
            return true;
        }
        if (!(chunk.getBlockState(entry.pos()).getBlock() instanceof DeployedCropBlock block)
                || !isTrapKind(block.kind())) {
            return false;
        }
        BlockEntity blockEntity = chunk.getBlockEntity(entry.pos());
        return blockEntity instanceof DeployedCropBlockEntity deployed && owner.equals(deployed.ownerUuid());
    }

    @Nullable
    private static ServerLevel levelOf(MinecraftServer server, ResourceLocation dimension) {
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
    }

    private void removeAt(UUID owner, ResourceLocation dimension, BlockPos pos) {
        List<Entry> entries = this.byOwner.get(owner);
        if (entries == null) {
            return;
        }
        if (!entries.removeIf(entry -> entry.pos().equals(pos) && entry.dimension().equals(dimension))) {
            return;
        }
        if (entries.isEmpty()) {
            this.byOwner.remove(owner);
        }
        this.setDirty();
    }

    /** 记账总条数（审计日志用）：内存里有多少条，落盘就该写成多少条。 */
    public int entryCount() {
        int total = 0;
        for (List<Entry> entries : this.byOwner.values()) {
            total += entries.size();
        }
        return total;
    }

    /**
     * 落盘。审计日志的两个数不是装饰：停服路径若把内存账目清空，这里会打出「记账 0 条」，
     * 与放了几枚陷阱的事实对不上，一眼能看出是账目丢了而不是世界没存。
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        int recorded = this.entryCount();
        int written = 0;
        CompoundTag owners = new CompoundTag();
        for (Map.Entry<UUID, List<Entry>> owned : this.byOwner.entrySet()) {
            ListTag list = new ListTag();
            for (Entry entry : owned.getValue()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString(TAG_DIMENSION, entry.dimension().toString());
                entryTag.putLong(TAG_POS, entry.pos().asLong());
                entryTag.putLong(TAG_PLACED_GAME_TIME, entry.placedGameTime());
                list.add(entryTag);
            }
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.put(TAG_ENTRIES, list);
            owners.put(owned.getKey().toString(), ownerTag);
            written += list.size();
        }
        tag.put(TAG_OWNERS, owners);
        Farmstorm.LOGGER.info("陷阱 FIFO 索引落盘：记账 {} 条 / 写成 {} 条 / {} 名玩家", recorded, written,
                this.byOwner.size());
        return tag;
    }

    private static TrapFifoIndex load(CompoundTag tag, HolderLookup.Provider registries) {
        TrapFifoIndex index = new TrapFifoIndex();
        CompoundTag owners = tag.getCompound(TAG_OWNERS);
        for (String key : owners.getAllKeys()) {
            UUID owner = parseUuid(key);
            if (owner == null) {
                continue;
            }
            List<Entry> entries = new ArrayList<>();
            ListTag list = owners.getCompound(key).getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                ResourceLocation dimension = ResourceLocation.tryParse(entryTag.getString(TAG_DIMENSION));
                if (dimension == null) {
                    continue;
                }
                entries.add(new Entry(dimension, BlockPos.of(entryTag.getLong(TAG_POS)),
                        entryTag.getLong(TAG_PLACED_GAME_TIME)));
            }
            entries.sort(Comparator.comparingLong(Entry::placedGameTime));
            index.byOwner.put(owner, entries);
        }
        Farmstorm.LOGGER.info("陷阱 FIFO 索引读入 {} 条 / {} 名玩家（惰性校验在首次查询时执行）",
                index.entryCount(), index.byOwner.size());
        return index;
    }

    @Nullable
    private static UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            Farmstorm.LOGGER.warn("陷阱 FIFO 索引里有一条读不出来的玩家 UUID，已丢弃：{}", raw);
            return null;
        }
    }
}
