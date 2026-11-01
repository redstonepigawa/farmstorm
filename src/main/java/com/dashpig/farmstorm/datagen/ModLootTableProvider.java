package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.block.FarmstormCropBlock;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormItems;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * 26 张方块战利品表（TECH §6.7 + F-74 + F-168）：15 张作物 + 8 张部署方块 + 1 张压缩干草块
 * + 1 张转换器 + 1 张据点核心。
 *
 * <p>作物表复用原版作物表的结构（成熟判定 + 数量区间 + 时运修正 + 爆炸衰减），只把 item 换成
 * 改良版的作物与播种物（DESIGN §3.2.4）；数量区间逐条照抄 TECH §6.7。
 * 「同名（本身可种）」的 8 个作物不另开播种物池——作物的播种形态就是它自己。
 *
 * <p>部署方块按原版口径加 {@code survives_explosion}：被爆炸破坏时掉 0 个。
 * 转换器只掉方块本体，内容物由 {@code ConverterBlockEntity} 的 {@code dropContents} 负责。
 */
public final class ModLootTableProvider extends BlockLootSubProvider {
    /** 成熟等级：age 等于该值时同时掉作物与播种物（FarmstormCropBlock.MAX_CUSTOM_AGE）。 */
    private static final int MATURE_AGE = FarmstormCropBlock.MAX_CUSTOM_AGE;

    /** 原版作物种子的二项分布概率（小麦/甜菜的表都用这个数）。 */
    private static final float SEED_BINOMIAL_PROBABILITY = 0.5714286F;

    private ModLootTableProvider(HolderLookup.Provider registries) {
        // 本模组没有抗爆方块进这条路径（火把花/瓶子草由 explosion_immune tag 拦截），故传空集
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    /** 供 {@code FarmstormDataGen} 接线的入口（BLOCK 参数集 + 本类作为唯一子 Provider）。 */
    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        return new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModLootTableProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider);
    }

    @Override
    protected void generate() {
        // ---- 15 张作物表（TECH §6.7 的逐条比例）----
        // 膨胀麦：1 作物 + 0~3 种子（时运加成）
        crop(FarmstormBlocks.PUFFED_WHEAT_CROP.get(), FarmstormItems.PUFFED_WHEAT.get(),
                FarmstormItems.PUFFED_WHEAT_SEEDS.get(), ConstantValue.exactly(1), 3);
        // 胡罗贝 / 育碧服务器 / 毒育碧：1~4 作物，本身可种
        crop(FarmstormBlocks.BLAST_CARROT_CROP.get(), FarmstormItems.BLAST_CARROT.get(),
                FarmstormItems.BLAST_CARROT.get(), UniformGenerator.between(1.0F, 4.0F), 0);
        crop(FarmstormBlocks.UBI_SERVER_CROP.get(), FarmstormItems.UBI_SERVER.get(),
                FarmstormItems.UBI_SERVER.get(), UniformGenerator.between(1.0F, 4.0F), 0);
        crop(FarmstormBlocks.TOXIC_UBI_SERVER_CROP.get(), FarmstormItems.TOXIC_UBI_SERVER.get(),
                FarmstormItems.TOXIC_UBI_SERVER.get(), UniformGenerator.between(1.0F, 4.0F), 0);
        // 甜菜！：1 作物 + 0~1 种子
        crop(FarmstormBlocks.POWER_BEET_CROP.get(), FarmstormItems.POWER_BEET.get(),
                FarmstormItems.POWER_BEET_SEEDS.get(), ConstantValue.exactly(1), 1);
        // 瓜！/ man瓜：1~4 与 1 作物 + 1 种子
        crop(FarmstormBlocks.MELON_GRENADE_STEM.get(), FarmstormItems.MELON_GRENADE.get(),
                FarmstormItems.MELON_GRENADE_SEEDS.get(), UniformGenerator.between(1.0F, 4.0F), 0);
        crop(FarmstormBlocks.PUMPKIN_TURRET_STEM.get(), FarmstormItems.PUMPKIN_TURRET.get(),
                FarmstormItems.PUMPKIN_TURRET_SEEDS.get(), ConstantValue.exactly(1), 0);
        // 火把花 / 瓶子草：1 作物 + 1 播种物
        crop(FarmstormBlocks.AWAKENED_TORCHFLOWER_CROP.get(), FarmstormItems.AWAKENED_TORCHFLOWER.get(),
                FarmstormItems.AWAKENED_TORCHFLOWER_SEEDS.get(), ConstantValue.exactly(1), 0);
        crop(FarmstormBlocks.AWAKENED_PITCHER_PLANT_CROP.get(), FarmstormItems.AWAKENED_PITCHER_PLANT.get(),
                FarmstormItems.AWAKENED_PITCHER_PLANT_POD.get(), ConstantValue.exactly(1), 0);
        // 仙人掌 / 爆竹 / 红菇 / 棕菇 / 幸运浆果：1~3、1、1~2、1~2、1~3，本身可种
        crop(FarmstormBlocks.FANG_TRAP_CACTUS_BLOCK.get(), FarmstormItems.FANG_TRAP_CACTUS.get(),
                FarmstormItems.FANG_TRAP_CACTUS.get(), UniformGenerator.between(1.0F, 3.0F), 0);
        crop(FarmstormBlocks.BAMBOO_FIRECRACKER_STALK.get(), FarmstormItems.BAMBOO_FIRECRACKER.get(),
                FarmstormItems.BAMBOO_FIRECRACKER.get(), ConstantValue.exactly(1), 0);
        crop(FarmstormBlocks.RED_MUSHROOM_PUFF_BLOCK.get(), FarmstormItems.RED_MUSHROOM_PUFF.get(),
                FarmstormItems.RED_MUSHROOM_PUFF.get(), UniformGenerator.between(1.0F, 2.0F), 0);
        crop(FarmstormBlocks.BROWN_MUSHROOM_PUFF_BLOCK.get(), FarmstormItems.BROWN_MUSHROOM_PUFF.get(),
                FarmstormItems.BROWN_MUSHROOM_PUFF.get(), UniformGenerator.between(1.0F, 2.0F), 0);
        crop(FarmstormBlocks.LUCKY_BERRY_BUSH.get(), FarmstormItems.LUCKY_BERRY.get(),
                FarmstormItems.LUCKY_BERRY.get(), UniformGenerator.between(1.0F, 3.0F), 0);
        // 朋友的韭（D-84）：1 作物 + 0~3 种子，参数照膨胀麦那条二项分布
        crop(FarmstormBlocks.FRIEND_CHIVES_CROP.get(), FarmstormItems.FRIEND_CHIVES.get(),
                FarmstormItems.CHIVE_SEEDS.get(), ConstantValue.exactly(1), 3);

        // ---- 8 张部署方块表：掉自身，被爆炸破坏时掉 0 个 ----
        deployed(FarmstormBlocks.BLAST_CARROT.get());
        deployed(FarmstormBlocks.PUMPKIN_TURRET.get());
        deployed(FarmstormBlocks.AWAKENED_TORCHFLOWER.get());
        deployed(FarmstormBlocks.AWAKENED_PITCHER_PLANT.get());
        deployed(FarmstormBlocks.FANG_TRAP_CACTUS.get());
        // F-74：桑吉尔蔗同形入表——玩家自挖掉自身；触发消失走 consume() 不经过战利品表（A-08）
        deployed(FarmstormBlocks.ZANGIEF_CANE.get());
        // F-132：近防菌有 BlockItem，走同一条掉自身路径（TECH §9.3 第 18 行）
        deployed(FarmstormBlocks.POINT_DEFENSE_FUNGUS.get());
        // F-132：震动豆的方块没有 BlockItem（它的物品是 ThrowableCropItem），必须显式指物品（§2.2 注 2）
        deployedAs(FarmstormBlocks.VIBRATION_BEAN.get(), FarmstormItems.VIBRATION_BEAN.get());

        // ---- 1 张压缩干草块表：掉自身（F-74；能否取出由 requiresCorrectToolForDrops + needs_diamond_tool 决定）----
        this.add(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get(),
                this.createSingleItemTable(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get()));

        // ---- 1 张据点核心表：掉自身（F-168 / D-119 第 5 项）----
        // 该方块**有** BlockItem（FarmstormItems.OUTPOST_CORE），因此 lootTableItem(block) 解析得回本体，
        // 不需要走 deployedAs 的显式物品分支（对比震动豆那个无 BlockItem 的方块）。
        this.add(FarmstormBlocks.OUTPOST_CORE.get(),
                this.createSingleItemTable(FarmstormBlocks.OUTPOST_CORE.get()));

        // ---- 1 张转换器表：只掉本体（内容物由 dropContents 负责）----
        this.add(FarmstormBlocks.CONVERTER.get(), this.createSingleItemTable(FarmstormBlocks.CONVERTER.get()));
    }

    /**
     * 作物表：成熟时掉作物（数量区间 + 时运）与播种物（二项分布 + 时运），未成熟只掉 1 个播种物。
     *
     * @param seedBinomialExtra 播种物的二项分布上界（0 表示固定 1 个）
     */
    private void crop(Block cropBlock, Item product, Item seed, NumberProvider productCount, int seedBinomialExtra) {
        Holder<Enchantment> fortune = this.registries.holderOrThrow(Enchantments.FORTUNE);
        LootItemCondition.Builder mature = matureCondition(cropBlock);

        LootPoolSingletonContainer.Builder<?> productEntry = LootItem.lootTableItem(product)
                .apply(SetItemCountFunction.setCount(productCount))
                .apply(ApplyBonusCount.addUniformBonusCount(fortune));
        LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool().when(mature).add(this.applyExplosionDecay(cropBlock, productEntry)));

        // 播种物与作物不是同一件物品时才另开池（本身可种的 8 个作物跳过这步）
        if (seed != product) {
            LootItemConditionalFunction.Builder<?> seedBonus = seedBinomialExtra > 0
                    ? ApplyBonusCount.addBonusBinomialDistributionCount(fortune, SEED_BINOMIAL_PROBABILITY, seedBinomialExtra)
                    : null;
            LootPoolSingletonContainer.Builder<?> seedEntry = LootItem.lootTableItem(seed);
            if (seedBonus != null) {
                seedEntry = seedEntry.apply(seedBonus);
            }
            table = table.withPool(LootPool.lootPool().when(mature).add(this.applyExplosionDecay(cropBlock, seedEntry)));
        }

        // 未成熟：只掉 1 个播种物（本身可种时就是作物本体）
        LootPoolSingletonContainer.Builder<?> immatureEntry = LootItem.lootTableItem(seed);
        this.add(cropBlock, table.withPool(
                LootPool.lootPool().when(mature.invert()).add(this.applyExplosionDecay(cropBlock, immatureEntry))));
    }

    /** 部署方块：掉自身；爆炸破坏时按原版 {@code survives_explosion} 掉 0 个。 */
    private void deployed(Block block) {
        deployedAs(block, block.asItem());
    }

    /**
     * 部署方块：掉指定的物品。震动豆的方块没有 BlockItem 映射，
     * {@code lootTableItem(block)} 会经 {@code block.asItem()} 解析成空气，故必须显式给物品（TECH §2.2 注 2）。
     */
    private void deployedAs(Block block, Item item) {
        this.add(block, LootTable.lootTable().withPool(
                LootPool.lootPool().when(ExplosionCondition.survivesExplosion()).add(LootItem.lootTableItem(item))));
    }

    /** 成熟判定：作物方块且 {@code age == MATURE_AGE}。 */
    private static LootItemCondition.Builder matureCondition(Block cropBlock) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(cropBlock)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(BlockStateProperties.AGE_3, MATURE_AGE));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
                FarmstormBlocks.PUFFED_WHEAT_CROP.get(),
                FarmstormBlocks.BLAST_CARROT_CROP.get(),
                FarmstormBlocks.UBI_SERVER_CROP.get(),
                FarmstormBlocks.TOXIC_UBI_SERVER_CROP.get(),
                FarmstormBlocks.POWER_BEET_CROP.get(),
                FarmstormBlocks.MELON_GRENADE_STEM.get(),
                FarmstormBlocks.PUMPKIN_TURRET_STEM.get(),
                FarmstormBlocks.AWAKENED_TORCHFLOWER_CROP.get(),
                FarmstormBlocks.AWAKENED_PITCHER_PLANT_CROP.get(),
                FarmstormBlocks.FANG_TRAP_CACTUS_BLOCK.get(),
                FarmstormBlocks.BAMBOO_FIRECRACKER_STALK.get(),
                FarmstormBlocks.RED_MUSHROOM_PUFF_BLOCK.get(),
                FarmstormBlocks.BROWN_MUSHROOM_PUFF_BLOCK.get(),
                FarmstormBlocks.LUCKY_BERRY_BUSH.get(),
                FarmstormBlocks.BLAST_CARROT.get(),
                FarmstormBlocks.PUMPKIN_TURRET.get(),
                FarmstormBlocks.AWAKENED_TORCHFLOWER.get(),
                FarmstormBlocks.AWAKENED_PITCHER_PLANT.get(),
                FarmstormBlocks.FANG_TRAP_CACTUS.get(),
                FarmstormBlocks.ZANGIEF_CANE.get(),
                FarmstormBlocks.FRIEND_CHIVES_CROP.get(),
                FarmstormBlocks.POINT_DEFENSE_FUNGUS.get(),
                FarmstormBlocks.VIBRATION_BEAN.get(),
                FarmstormBlocks.COMPRESSED_HAY_BLOCK.get(),
                FarmstormBlocks.OUTPOST_CORE.get(),
                FarmstormBlocks.CONVERTER.get());
    }
}
