package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.item.DeployableCropItem;
import com.dashpig.farmstorm.item.FoodCropItem;
import com.dashpig.farmstorm.item.PlainSeedItem;
import com.dashpig.farmstorm.item.ThrowableCropItem;
import com.dashpig.farmstorm.util.CropProfile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 34 个物品（TECH §2.2；v0.1.0 按 D-119 加 1 个据点核心、v0.2.0 按 D-160 加 1 个培养器、v0.2.2 按 D-193 加 1 个平凡韭菜）：
 * 22 个作物 + 7 个独立播种物 + 4 个方块物品（转换器 / 压缩干草块 / 据点核心 / 农作物培养器）
 * + 1 个平凡农作物（平凡韭菜：不是作物、没有 CropProfile）。
 * 堆叠数按 D-16 分两档，实际数值来自 stack.*（§4.11.5）。
 */
public final class FarmstormItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(Farmstorm.MOD_ID);

    private static final Map<String, DeferredItem<? extends Item>> CROP_ITEMS = new LinkedHashMap<>();

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
    /** D-86：近防菌。{@code DeployableCropItem} 是 BlockItem 子类，因此 {@code Item.BY_BLOCK} 有映射（§2.2 注 1）。 */
    public static final DeferredItem<DeployableCropItem> POINT_DEFENSE_FUNGUS = cropItem(CropProfile.POINT_DEFENSE_FUNGUS,
            properties -> new DeployableCropItem(CropProfile.POINT_DEFENSE_FUNGUS,
                    FarmstormBlocks.POINT_DEFENSE_FUNGUS.get(), properties));
    /** D-90 / D-92：震动豆是投掷型作物，**不是** BlockItem——同名方块因此没有 BlockItem 映射（§2.2 注 2）。 */
    public static final DeferredItem<ThrowableCropItem> VIBRATION_BEAN = cropItem(CropProfile.VIBRATION_BEAN,
            properties -> new ThrowableCropItem(CropProfile.VIBRATION_BEAN,
                    FarmstormEntityTypes.VIBRATION_BEAN::get, properties));

    // ---- 3 个方块物品 ----
    public static final DeferredItem<BlockItem> CROP_CONVERTER = REGISTER.registerItem("crop_converter",
            properties -> new BlockItem(FarmstormBlocks.CROP_CONVERTER.get(), properties), otherProperties());
    /** DESIGN §3.4：方块物品沿用原版 64 上限，不纳入 stack.throwable。 */
    public static final DeferredItem<BlockItem> COMPRESSED_HAY_BLOCK = REGISTER.registerItem("compressed_hay_block",
            properties -> new BlockItem(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get(), properties), otherProperties());
    /** D-119 第 5 项：据点核心的方块物品（掉落自身；战利品表在 F-168，配方在 D-122 第 3 项）。 */
    public static final DeferredItem<BlockItem> OUTPOST_CORE = REGISTER.registerItem("outpost_core",
            properties -> new BlockItem(FarmstormBlocks.OUTPOST_CORE.get(), properties), otherProperties());

    /** v0.2.0 的第 4 个方块物品（F-224 / D-160）：本版没有培养器配方，创造栏与 /give 是仅有的两个入口（A-64）。 */
    public static final DeferredItem<BlockItem> CULTIVATOR = REGISTER.registerItem("cultivator",
            properties -> new BlockItem(FarmstormBlocks.CULTIVATOR.get(), properties), otherProperties());

    /**
     * v0.2.2 新增的 1 个平凡农作物（F-268 / D-193）：平凡韭菜，可食、饱食 2。
     * <p>它没有 {@link CropProfile} ⇒ 走不了 {@code cropItem}；食物属性也**不能**复用 {@code foodProperties}
     * （那个方法要档案读 config、且带 {@code alwaysEdible()}）——这里用固定常量，因此不新增 config 键（176 不变）。
     * 堆叠走 {@code otherProperties()}（{@code stack.other}），不进 {@code CROP_ITEMS} / 播种物清单。
     */
    public static final DeferredItem<Item> PLAIN_CHIVES = REGISTER.registerItem("plain_chives", Item::new,
            otherProperties().food(new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.1F)
                    .build()));

    /**
     * v0.2.3 新增的平凡韭菜种子（F-298 / D-216）：`PlainSeedItem` 拿原版放置流程，可播种。
     * <p>不是 D-152 ② 那个 `useOn` 无条件 `PASS` 的播种物类（它已随 7 个种子删除，D-212），也不进 `CROP_ITEMS`——
     * 平凡韭菜没有档案，这个种子只由 `#farmstorm:seeds` 标签与创造栏认领。
     * 堆叠走 `otherProperties()`（`stack.other`，与 D-16 的「播种物」一档）。
     */
    public static final DeferredItem<PlainSeedItem> PLAIN_CHIVES_SEEDS = REGISTER.registerItem("plain_chives_seeds",
            properties -> new PlainSeedItem(FarmstormBlocks.PLAIN_CHIVES_CROP.get(), properties), otherProperties());

    private FarmstormItems() {}

    /** 创造标签页内容：作物 → 播种物 → 工具方块（TECH §3.6）。 */
    public static List<DeferredItem<? extends Item>> creativeTabItems() {
        List<DeferredItem<? extends Item>> items = new ArrayList<>(CROP_ITEMS.values());
        items.add(CROP_CONVERTER);
        items.add(COMPRESSED_HAY_BLOCK);
        items.add(OUTPOST_CORE);
        // A-64：本版没有培养器配方，不进口袋就等于「谁都拿不到」。
        items.add(CULTIVATOR);
        // D-193：平凡韭菜不是作物（不在 CROP_ITEMS 里），必须手写进清单，否则创造栏看不到。
        items.add(PLAIN_CHIVES);
        // F-298：平凡韭菜种子同属「手写」这一档（不占 CROP_ITEMS），漏了它创造栏里就没有播种物。
        items.add(PLAIN_CHIVES_SEEDS);
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
}
