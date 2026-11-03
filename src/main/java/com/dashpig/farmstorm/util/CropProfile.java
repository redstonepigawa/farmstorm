package com.dashpig.farmstorm.util;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * 单个改良作物的静态档案（DEC §4 的 22 条，一条一个常量）。
 * 物品 / 方块 / 实体都用这里的数据，避免同一个注册 ID 在多处各写一份字符串。
 *
 * <p>DEC §4 的两列是两个正交维度（DESIGN §2.4），这里是两个独立字段：
 * <ul>
 *   <li>{@link #type}：类型（§2.2），单值，来自 DEC §4 的「类型」列；</li>
 *   <li>{@link #categories}：类别（§2.3），多值集合，来自 DEC §4 的「类别」列。</li>
 * </ul>
 * 两者不得互相推导，也不由 {@link DeployedKind} 代替：C-4 的「分类是炮台、实现是光环」正是这种分离。
 * 方块破坏权限只由 {@link CropCategory#DEMOLITION} 标签决定（见 {@link #canBreakBlocks()}）。
 *
 * <p>品质（{@link #tier}）排在最后一位是有意为之：前 8 个字段的行号与语义在上游文档里被逐条引用过，
 * 追加在末尾不会让任何既有引用的「第 N 字段」错位（D-150、TECH §2.1）。
 */
public record CropProfile(
        ResourceLocation id,
        CropType type,
        Set<CropCategory> categories,
        @Nullable ResourceKey<Block> cropBlock,
        @Nullable ResourceKey<Item> seedItem,
        @Nullable DeployedKind kind,
        Set<AttachFace> allowedFaces,
        int throwStack,
        CropTier tier) {

    /** D-16：投掷型一档。 */
    public static final int THROWABLE_STACK_TIER = 16;
    /** D-16：部署型 / 食用型 / 播种物一档。 */
    public static final int OTHER_STACK_TIER = 64;

    public static final CropProfile PUFFED_WHEAT = throwable("puffed_wheat", "puffed_wheat_crop", "puffed_wheat_seeds",
            CropTier.COMMON, CropCategory.CONSTRUCTION);
    public static final CropProfile BLAST_CARROT = deployed("blast_carrot", "blast_carrot_crop", "blast_carrot",
            DeployedKind.BLAST_CARROT, CropTier.COMMON,
            categorySet(CropCategory.TRAP, CropCategory.DAMAGE), AttachFace.FLOOR);
    public static final CropProfile UBI_SERVER = throwable("ubi_server", "ubi_server_crop", "ubi_server",
            CropTier.RARE, CropCategory.DEMOLITION, CropCategory.DAMAGE);
    public static final CropProfile TOXIC_UBI_SERVER = throwable("toxic_ubi_server", "toxic_ubi_server_crop",
            "toxic_ubi_server", CropTier.RARE, CropCategory.BUFF, CropCategory.DEMOLITION, CropCategory.DAMAGE);
    public static final CropProfile POWER_BEET = crop("power_beet", "power_beet_crop", "power_beet_seeds",
            CropTier.RARE, CropCategory.BUFF);
    /** 瓜片子不可种植（§3.2.3），故作物方块与播种物为空。D-108：类别只剩伤害，破障类已删。 */
    public static final CropProfile MELON_SLICE_BOMB = new CropProfile(
            Farmstorm.id("melon_slice_bomb"), CropType.THROWABLE,
            categorySet(CropCategory.DAMAGE),
            null, null, null, Set.of(), THROWABLE_STACK_TIER, CropTier.COMMON);
    public static final CropProfile MELON_GRENADE = throwable("melon_grenade", "melon_grenade_stem", "melon_grenade_seeds",
            CropTier.COMMON, CropCategory.DAMAGE);
    public static final CropProfile PUMPKIN_TURRET = deployed("pumpkin_turret", "pumpkin_turret_stem",
            "pumpkin_turret_seeds", DeployedKind.PUMPKIN_TURRET, CropTier.COMMON,
            categorySet(CropCategory.TURRET, CropCategory.DAMAGE),
            AttachFace.FLOOR, AttachFace.WALL, AttachFace.CEILING);
    public static final CropProfile AWAKENED_TORCHFLOWER = deployed("awakened_torchflower", "awakened_torchflower_crop",
            "awakened_torchflower_seeds", DeployedKind.AWAKENED_TORCHFLOWER, CropTier.EPIC,
            categorySet(CropCategory.TURRET, CropCategory.BUFF), AttachFace.FLOOR);
    public static final CropProfile AWAKENED_PITCHER_PLANT = deployed("awakened_pitcher_plant",
            "awakened_pitcher_plant_crop", "awakened_pitcher_plant_pod", DeployedKind.AWAKENED_PITCHER_PLANT,
            CropTier.EPIC, categorySet(CropCategory.TURRET, CropCategory.BUFF), AttachFace.FLOOR);
    public static final CropProfile FANG_TRAP_CACTUS = deployed("fang_trap_cactus", "fang_trap_cactus_block",
            "fang_trap_cactus", DeployedKind.FANG_TRAP_CACTUS, CropTier.COMMON,
            categorySet(CropCategory.TRAP, CropCategory.DAMAGE), AttachFace.FLOOR);
    public static final CropProfile BAMBOO_FIRECRACKER = throwable("bamboo_firecracker", "bamboo_firecracker_stalk",
            "bamboo_firecracker", CropTier.COMMON, CropCategory.DAMAGE);
    public static final CropProfile RED_MUSHROOM_PUFF = throwable("red_mushroom_puff", "red_mushroom_puff_block",
            "red_mushroom_puff", CropTier.COMMON, CropCategory.BUFF);
    public static final CropProfile BROWN_MUSHROOM_PUFF = throwable("brown_mushroom_puff", "brown_mushroom_puff_block",
            "brown_mushroom_puff", CropTier.COMMON, CropCategory.BUFF);
    public static final CropProfile LUCKY_BERRY = crop("lucky_berry", "lucky_berry_bush", "lucky_berry",
            CropTier.EPIC, CropCategory.BUFF);

    /** D-51：莫洛托夫不可种植，作物方块与播种物为空（先例瓜片子）；撒火与撒岩浆块都是写世界，故 DAMAGE + CONSTRUCTION。 */
    public static final CropProfile MOLOTOV = new CropProfile(
            Farmstorm.id("molotov"), CropType.THROWABLE,
            categorySet(CropCategory.DAMAGE, CropCategory.CONSTRUCTION),
            null, null, null, Set.of(), THROWABLE_STACK_TIER, CropTier.COMMON);
    /** D-51：桑吉尔蔗不可种植，作物方块与播种物为空；纯 TRAP，没有 DAMAGE 标签。 */
    public static final CropProfile ZANGIEF_CANE = new CropProfile(
            Farmstorm.id("zangief_cane"), CropType.DEPLOYABLE,
            categorySet(CropCategory.TRAP),
            null, null, DeployedKind.ZANGIEF_CANE, Set.of(AttachFace.FLOOR), OTHER_STACK_TIER, CropTier.RARE);
    /** D-51：狂野苹果不可种植，作物方块与播种物为空；食用型、纯 BUFF。 */
    public static final CropProfile WILD_APPLE = new CropProfile(
            Farmstorm.id("wild_apple"), CropType.EDIBLE,
            categorySet(CropCategory.BUFF),
            null, null, null, Set.of(), OTHER_STACK_TIER, CropTier.EPIC);
    /** D-56：压缩膨胀麦是「二级合成物」而非改良农作物，作物方块与播种物为空；纯 CONSTRUCTION。 */
    public static final CropProfile COMPRESSED_PUFFED_WHEAT = new CropProfile(
            Farmstorm.id("compressed_puffed_wheat"), CropType.THROWABLE,
            categorySet(CropCategory.CONSTRUCTION),
            null, null, null, Set.of(), THROWABLE_STACK_TIER, CropTier.RARE);

    /**
     * D-84：朋友的韭，v0.0.3 的第 20 条档案（DEC §4）。投掷型 + 纯 BUFF，可种植（作物方块与播种物都非空）。
     * 它是三条新档案里唯一有作物方块与种子的一条。
     */
    public static final CropProfile FRIEND_CHIVES = throwable("friend_chives", "friend_chives_crop", "chive_seeds",
            CropTier.RARE, CropCategory.BUFF);
    /**
     * D-86：近防菌，v0.0.3 的第 21 条档案。部署型 + 炮台类；不可种植（方块与播种物为空）。
     * kind 必须是 {@link DeployedKind#POINT_DEFENSE_FUNGUS}：置 null 会回落到 BLAST_CARROT（A-11）。
     */
    public static final CropProfile POINT_DEFENSE_FUNGUS = new CropProfile(
            Farmstorm.id("point_defense_fungus"), CropType.DEPLOYABLE,
            categorySet(CropCategory.TURRET),
            null, null, DeployedKind.POINT_DEFENSE_FUNGUS, Set.of(AttachFace.FLOOR), OTHER_STACK_TIER,
            CropTier.RARE);
    /**
     * D-90：震动豆，v0.0.3 的第 22 条档案。投掷型 + 陷阱类；不可种植（方块与播种物为空）。
     * kind 必须是 {@link DeployedKind#VIBRATION_BEAN}：置 null 会回落到 BLAST_CARROT（A-11）。
     */
    public static final CropProfile VIBRATION_BEAN = new CropProfile(
            Farmstorm.id("vibration_bean"), CropType.THROWABLE,
            categorySet(CropCategory.TRAP),
            null, null, DeployedKind.VIBRATION_BEAN, Set.of(AttachFace.FLOOR), THROWABLE_STACK_TIER,
            CropTier.COMMON);

    /** DEC §4 的 22 条，顺序即 00-DECISIONS §4 的行号（第 20~22 行是 v0.0.3 的三条新档案）。 */
    public static final List<CropProfile> ALL = List.of(
            PUFFED_WHEAT, BLAST_CARROT, UBI_SERVER, TOXIC_UBI_SERVER, POWER_BEET,
            MELON_SLICE_BOMB, MELON_GRENADE, PUMPKIN_TURRET, AWAKENED_TORCHFLOWER, AWAKENED_PITCHER_PLANT,
            FANG_TRAP_CACTUS, BAMBOO_FIRECRACKER, RED_MUSHROOM_PUFF, BROWN_MUSHROOM_PUFF, LUCKY_BERRY,
            MOLOTOV, ZANGIEF_CANE, WILD_APPLE, COMPRESSED_PUFFED_WHEAT,
            FRIEND_CHIVES, POINT_DEFENSE_FUNGUS, VIBRATION_BEAN);

    /** D-55 + D-56 + D-97：22 条里排除 lucky_berry、wild_apple、compressed_puffed_wheat → 19 条。 */
    public static final List<CropProfile> LUCKY_BERRY_POOL = ALL.stream()
            .filter(profile -> profile != LUCKY_BERRY)
            .filter(profile -> profile != WILD_APPLE)
            .filter(profile -> profile != COMPRESSED_PUFFED_WHEAT)
            .toList();

    private static final Map<ResourceLocation, CropProfile> BY_ID = ALL.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(CropProfile::id, profile -> profile));

    public static final Codec<CropProfile> CODEC = Codec.STRING.xmap(CropProfile::byPath, profile -> profile.id().getPath());

    /** 供方块 codec 反解：路径不含命名空间，数据包里只有一个命名空间。 */
    public static CropProfile byPath(String path) {
        return byId(Farmstorm.id(path));
    }

    public static CropProfile byId(ResourceLocation id) {
        CropProfile profile = BY_ID.get(id);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown crop profile: " + id);
        }
        return profile;
    }

    /** 部署方块按 kind 反查档案：8 个 kind 的行为参数（附着面）只有这一处来源。 */
    public static CropProfile byKind(DeployedKind kind) {
        return ALL.stream()
                .filter(profile -> profile.kind() == kind)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No crop profile for kind " + kind));
    }

    /** 是否为部署型（7 个）。判据是类型列（§2.2.2），不是类别。 */
    public boolean isDeployed() {
        return this.type == CropType.DEPLOYABLE;
    }

    /** 是否带某个类别标签（DEC §4 的「类别」列）。 */
    public boolean has(CropCategory category) {
        return this.categories.contains(category);
    }

    /** 能否破坏方块：只由 {@link CropCategory#DEMOLITION} 决定，全仓没有第二处判据。 */
    public boolean canBreakBlocks() {
        return has(CropCategory.DEMOLITION);
    }

    /** 物品的注册 ID 与作物档案 ID 同名，注册层直接用它。 */
    public String path() {
        return this.id.getPath();
    }

    private static CropProfile crop(String itemPath, String cropBlockPath, String seedItemPath, CropTier tier,
                                    CropCategory... categories) {
        return new CropProfile(Farmstorm.id(itemPath), CropType.EDIBLE, categorySet(categories),
                blockKey(cropBlockPath), itemKey(seedItemPath), null, Set.of(), OTHER_STACK_TIER, tier);
    }

    private static CropProfile throwable(String itemPath, String cropBlockPath, String seedItemPath, CropTier tier,
                                         CropCategory... categories) {
        return new CropProfile(Farmstorm.id(itemPath), CropType.THROWABLE, categorySet(categories),
                blockKey(cropBlockPath), itemKey(seedItemPath), null, Set.of(), THROWABLE_STACK_TIER, tier);
    }

    private static CropProfile deployed(String itemPath, String cropBlockPath, String seedItemPath, DeployedKind kind,
                                        CropTier tier, Set<CropCategory> categories, AttachFace... faces) {
        return new CropProfile(Farmstorm.id(itemPath), CropType.DEPLOYABLE, categories,
                blockKey(cropBlockPath), itemKey(seedItemPath), kind, Set.of(faces), OTHER_STACK_TIER, tier);
    }

    /**
     * 类别集合：用 {@link LinkedHashSet} 保序，让 tooltip 与启动自检按 DEC §4 的书写顺序输出。
     * {@code Set.of} 的迭代顺序由 JVM 哈希盐决定，逐次运行可能不同，不能用来展示。
     */
    private static Set<CropCategory> categorySet(CropCategory... categories) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(categories)));
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, Farmstorm.id(path));
    }

    private static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(Registries.ITEM, Farmstorm.id(path));
    }
}
