package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.item.DeployableCropItem;
import com.dashpig.farmstorm.item.FoodCropItem;
import com.dashpig.farmstorm.item.SeedItem;
import com.dashpig.farmstorm.item.ThrowableCropItem;
import com.dashpig.farmstorm.util.CropProfile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 31 个物品（TECH §2.2）：22 个作物 + 7 个独立播种物 + 2 个方块物品（转换器 / 压缩干草块）。
 * 堆叠数按 D-16 分两档，实际数值来自 stack.*（§4.11.5）。
 */
public final class FarmstormItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(Farmstorm.MOD_ID);

    private static final Map<String, DeferredItem<? extends Item>> CROP_ITEMS = new LinkedHashMap<>();
    private static final List<DeferredItem<SeedItem>> SEED_ITEMS = new ArrayList<>();

    // ---- 15 个作物物品（顺序即 00-DECISIONS §4）----
    public static final DeferredItem<ThrowableCropItem> PUFFED_WHEAT = cropItem(CropProfile.PUFFED_WHEAT,
            properties -> new ThrowableCropItem(CropProfile.PUFFED_WHEAT, FarmstormEntityTypes.PUFFED_WHEAT::get, properties));
    public static final DeferredItem<DeployableCropItem> BLAST_CARROT = cropItem(CropProfile.BLAST_CARROT,
            properties -> new DeployableCropItem(CropProfile.BLAST_CARROT, FarmstormBlocks.BLAST_CARROT.get(), properties));
    public static final DeferredItem<ThrowableCropItem> UBI_SERVER = cropItem(CropProfile.UBI_SERVER,
            properties -> new ThrowableCropItem(CropProfile.UBI_SERVER, FarmstormEntityTypes.UBI_SERVER::get, properties));
    public static final DeferredItem<ThrowableCropItem> TOXIC_UBI_SERVER = cropItem(CropProfile.TOXIC_UBI_SERVER,
            properties -> new ThrowableCropItem(CropProfile.TOXIC_UBI_SERVER, FarmstormEntityTypes.TOXIC_UBI_SERVER::get, properties));
    public static final DeferredItem<FoodCropItem> POWER_BEET = cropItem(CropProfile.POWER_BEET,
            properties -> new FoodCropItem(CropProfile.POWER_BEET, properties),
            foodProperties(CropProfile.POWER_BEET, FarmstormConfig.powerBeetNutrition, FarmstormConfig.powerBeetSaturationModifier));
    public static final DeferredItem<ThrowableCropItem> MELON_SLICE_BOMB = cropItem(CropProfile.MELON_SLICE_BOMB,
            properties -> new ThrowableCropItem(CropProfile.MELON_SLICE_BOMB, FarmstormEntityTypes.MELON_SLICE_BOMB::get, properties));
    public static final DeferredItem<ThrowableCropItem> MELON_GRENADE = cropItem(CropProfile.MELON_GRENADE,
            properties -> new ThrowableCropItem(CropProfile.MELON_GRENADE, FarmstormEntityTypes.MELON_GRENADE::get, properties));
    public static final DeferredItem<DeployableCropItem> PUMPKIN_TURRET = cropItem(CropProfile.PUMPKIN_TURRET,
            properties -> new DeployableCropItem(CropProfile.PUMPKIN_TURRET, FarmstormBlocks.PUMPKIN_TURRET.get(), properties));
    public static final DeferredItem<DeployableCropItem> AWAKENED_TORCHFLOWER = cropItem(CropProfile.AWAKENED_TORCHFLOWER,
            properties -> new DeployableCropItem(CropProfile.AWAKENED_TORCHFLOWER, FarmstormBlocks.AWAKENED_TORCHFLOWER.get(), properties));
    public static final DeferredItem<DeployableCropItem> AWAKENED_PITCHER_PLANT = cropItem(CropProfile.AWAKENED_PITCHER_PLANT,
            properties -> new DeployableCropItem(CropProfile.AWAKENED_PITCHER_PLANT, FarmstormBlocks.AWAKENED_PITCHER_PLANT.get(), properties));
    public static final DeferredItem<DeployableCropItem> FANG_TRAP_CACTUS = cropItem(CropProfile.FANG_TRAP_CACTUS,
            properties -> new DeployableCropItem(CropProfile.FANG_TRAP_CACTUS, FarmstormBlocks.FANG_TRAP_CACTUS.get(), properties));
    public static final DeferredItem<ThrowableCropItem> BAMBOO_FIRECRACKER = cropItem(CropProfile.BAMBOO_FIRECRACKER,
            properties -> new ThrowableCropItem(CropProfile.BAMBOO_FIRECRACKER, FarmstormEntityTypes.BAMBOO_FIRECRACKER::get, properties));
    public static final DeferredItem<ThrowableCropItem> RED_MUSHROOM_PUFF = cropItem(CropProfile.RED_MUSHROOM_PUFF,
            properties -> new ThrowableCropItem(CropProfile.RED_MUSHROOM_PUFF, FarmstormEntityTypes.RED_MUSHROOM_PUFF::get, properties));
    public static final DeferredItem<ThrowableCropItem> BROWN_MUSHROOM_PUFF = cropItem(CropProfile.BROWN_MUSHROOM_PUFF,
            properties -> new ThrowableCropItem(CropProfile.BROWN_MUSHROOM_PUFF, FarmstormEntityTypes.BROWN_MUSHROOM_PUFF::get, properties));
    public static final DeferredItem<FoodCropItem> LUCKY_BERRY = cropItem(CropProfile.LUCKY_BERRY,
            properties -> new FoodCropItem(CropProfile.LUCKY_BERRY, properties),
            foodProperties(CropProfile.LUCKY_BERRY, FarmstormConfig.luckyBerryNutrition, FarmstormConfig.luckyBerrySaturationModifier));

    // ---- v0.0.2 新增的 4 条（TECH §2.2 #1–#4）；三条不可种植的作物因此都没有种子物品（D-51）----
    public static final DeferredItem<ThrowableCropItem> MOLOTOV = cropItem(CropProfile.MOLOTOV,
            properties -> new ThrowableCropItem(CropProfile.MOLOTOV, FarmstormEntityTypes.MOLOTOV::get, properties));
    public static final DeferredItem<DeployableCropItem> ZANGIEF_CANE = cropItem(CropProfile.ZANGIEF_CANE,
            properties -> new DeployableCropItem(CropProfile.ZANGIEF_CANE, FarmstormBlocks.ZANGIEF_CANE.get(), properties));
    /** D-54 / F-77：营养与饱和度取 {@code wild_apple.nutrition} 4 / {@code wild_apple.saturationModifier} 1.2（金苹果同值）。 */
    public static final DeferredItem<FoodCropItem> WILD_APPLE = cropItem(CropProfile.WILD_APPLE,
            properties -> new FoodCropItem(CropProfile.WILD_APPLE, properties),
            foodProperties(CropProfile.WILD_APPLE,
                    FarmstormConfig.wildAppleNutrition, FarmstormConfig.wildAppleSaturationModifier));
    public static final DeferredItem<ThrowableCropItem> COMPRESSED_PUFFED_WHEAT = cropItem(CropProfile.COMPRESSED_PUFFED_WHEAT,
            properties -> new ThrowableCropItem(CropProfile.COMPRESSED_PUFFED_WHEAT,
                    FarmstormEntityTypes.COMPRESSED_PUFFED_WHEAT::get, properties));

    // ---- v0.0.3 新增的 4 条（TECH §2.2 #1–#4）----
    /** D-84 / D-85：朋友的韭，投掷物是 {@code farmstorm:friend_chives}；落点结算由 F-115 填。 */
    public static final DeferredItem<ThrowableCropItem> FRIEND_CHIVES = cropItem(CropProfile.FRIEND_CHIVES,
            properties -> new ThrowableCropItem(CropProfile.FRIEND_CHIVES,
                    FarmstormEntityTypes.FRIEND_CHIVES::get, properties));
    /** D-84：朋友的韭的播种物；4 个 age 的种植形态由 F-114 填。 */
    public static final DeferredItem<SeedItem> CHIVE_SEEDS = seedItem("chive_seeds", CropProfile.FRIEND_CHIVES);
    /** D-86：近防菌。{@code DeployableCropItem} 是 BlockItem 子类，因此 {@code Item.BY_BLOCK} 有映射（§2.2 注 1）。 */
    public static final DeferredItem<DeployableCropItem> POINT_DEFENSE_FUNGUS = cropItem(CropProfile.POINT_DEFENSE_FUNGUS,
            properties -> new DeployableCropItem(CropProfile.POINT_DEFENSE_FUNGUS,
                    FarmstormBlocks.POINT_DEFENSE_FUNGUS.get(), properties));
    /** D-90 / D-92：震动豆是投掷型作物，**不是** BlockItem——同名方块因此没有 BlockItem 映射（§2.2 注 2）。 */
    public static final DeferredItem<ThrowableCropItem> VIBRATION_BEAN = cropItem(CropProfile.VIBRATION_BEAN,
            properties -> new ThrowableCropItem(CropProfile.VIBRATION_BEAN,
                    FarmstormEntityTypes.VIBRATION_BEAN::get, properties));

    // ---- 7 个独立播种物 ----
    public static final DeferredItem<SeedItem> PUFFED_WHEAT_SEEDS = seedItem("puffed_wheat_seeds", CropProfile.PUFFED_WHEAT);
    public static final DeferredItem<SeedItem> POWER_BEET_SEEDS = seedItem("power_beet_seeds", CropProfile.POWER_BEET);
    public static final DeferredItem<SeedItem> MELON_GRENADE_SEEDS = seedItem("melon_grenade_seeds", CropProfile.MELON_GRENADE);
    public static final DeferredItem<SeedItem> PUMPKIN_TURRET_SEEDS = seedItem("pumpkin_turret_seeds", CropProfile.PUMPKIN_TURRET);
    public static final DeferredItem<SeedItem> AWAKENED_TORCHFLOWER_SEEDS =
            seedItem("awakened_torchflower_seeds", CropProfile.AWAKENED_TORCHFLOWER);
    public static final DeferredItem<SeedItem> AWAKENED_PITCHER_PLANT_POD =
            seedItem("awakened_pitcher_plant_pod", CropProfile.AWAKENED_PITCHER_PLANT);

    // ---- 2 个方块物品 ----
    public static final DeferredItem<BlockItem> CONVERTER = REGISTER.registerItem("converter",
            properties -> new BlockItem(FarmstormBlocks.CONVERTER.get(), properties), otherProperties());
    /** DESIGN §3.4：方块物品沿用原版 64 上限，不纳入 stack.throwable。 */
    public static final DeferredItem<BlockItem> COMPRESSED_HAY_BLOCK = REGISTER.registerItem("compressed_hay_block",
            properties -> new BlockItem(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get(), properties), otherProperties());

    private FarmstormItems() {}

    /** 创造标签页内容：作物 → 播种物 → 工具方块（TECH §3.6）。 */
    public static List<DeferredItem<? extends Item>> creativeTabItems() {
        List<DeferredItem<? extends Item>> items = new ArrayList<>(CROP_ITEMS.values());
        items.addAll(SEED_ITEMS);
        items.add(CONVERTER);
        items.add(COMPRESSED_HAY_BLOCK);
        return items;
    }

    /** 作物档案 → 已注册物品，供 Behavioral 层按档案取物品（M3/M4 会用到）。 */
    public static DeferredItem<? extends Item> forProfile(CropProfile profile) {
        DeferredItem<? extends Item> item = CROP_ITEMS.get(profile.path());
        if (item == null) {
            throw new IllegalStateException("No item registered for crop " + profile.path());
        }
        return item;
    }

    private static <I extends Item> DeferredItem<I> cropItem(CropProfile profile,
                                                             Function<Item.Properties, ? extends I> factory) {
        return cropItem(profile, factory, stackProperties(profile));
    }

    private static <I extends Item> DeferredItem<I> cropItem(CropProfile profile,
                                                             Function<Item.Properties, ? extends I> factory,
                                                             Item.Properties properties) {
        DeferredItem<I> item = REGISTER.registerItem(profile.path(), factory, properties);
        CROP_ITEMS.put(profile.path(), item);
        return item;
    }

    private static DeferredItem<SeedItem> seedItem(String path, CropProfile profile) {
        DeferredItem<SeedItem> item = REGISTER.registerItem(path,
                properties -> new SeedItem(profile, cropBlock(profile), properties), otherProperties());
        SEED_ITEMS.add(item);
        return item;
    }

    private static Item.Properties stackProperties(CropProfile profile) {
        return profile.throwStack() == CropProfile.THROWABLE_STACK_TIER ? throwableProperties() : otherProperties();
    }

    private static Item.Properties throwableProperties() {
        return new Item.Properties().stacksTo(FarmstormConfig.registrationInt(FarmstormConfig.stackThrowable));
    }

    private static Item.Properties otherProperties() {
        return new Item.Properties().stacksTo(FarmstormConfig.registrationInt(FarmstormConfig.stackOther));
    }

    private static Item.Properties foodProperties(CropProfile profile, ModConfigSpec.IntValue nutrition,
                                                  ModConfigSpec.DoubleValue saturation) {
        FoodProperties food = new FoodProperties.Builder()
                .nutrition(FarmstormConfig.registrationInt(nutrition))
                .saturationModifier((float) FarmstormConfig.registrationDouble(saturation))
                .alwaysEdible()
                .build();
        return stackProperties(profile).food(food);
    }

    private static Block cropBlock(CropProfile profile) {
        ResourceKey<Block> key = profile.cropBlock();
        Block block = key == null ? null : BuiltInRegistries.BLOCK.get(key);
        if (block == null) {
            throw new IllegalStateException("No crop block registered for " + profile.path());
        }
        return block;
    }
}
