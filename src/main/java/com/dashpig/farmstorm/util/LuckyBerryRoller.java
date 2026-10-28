package com.dashpig.farmstorm.util;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.event.FarmstormGameEvents;
import com.dashpig.farmstorm.explosion.BlastRule;
import com.dashpig.farmstorm.explosion.ExplosionQueue;
import com.dashpig.farmstorm.explosion.FarmstormExplosion;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.registry.FarmstormItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 幸运浆果的 8 档概率表：数据结构、单次加权抽取、8 档执行（TECH §4.12 / D-17 / D-18）。
 *
 * <p>抽取只在服务端发生，且只在 {@code FoodCropItem#onEaten} 里调一次（§8.3：一个物品只能吃一次，
 * 原版食物流程负责 {@code consume}）；这里的每个方法都拿 {@link ServerLevel} 或 {@link ServerPlayer}，
 * 客户端根本没有入口（HC-03）。
 *
 * <p>1800 刻的创造模式硬还原需要每 tick 的驱动点：登录 / 登出两条兜底由 {@link FarmstormGameEvents} 负责，
 * 第①级「到点还原」的计时器由本类持有并在 {@link PlayerTickEvent.Post} 里结算。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class LuckyBerryRoller {
    /** 8 档结果（TECH §4.12），顺序即 config 概率键的顺序。 */
    public enum Outcome {
        LIGHTNING, SPAWN_CROPS, REGEN, LOOT_CHESTS, SMALL_EXPLOSION,
        CHARGED_CREEPER, POWER, CREATIVE
    }

    /** weight 为千分比：由百分比配置 ×10 取整而来，和恒为 1000。 */
    public record Roll(Outcome outcome, int permille) {}

    /** 抽取刻度（TECH §4.12）：一次 {@code nextInt(1000)} 定档，避免浮点累加漂移。 */
    private static final int PERMILLE_TOTAL = 1000;

    /**
     * D-33 的落点半径（格）。117 个 config 键已被 HC-01 锁死，而这是落点约束不是玩法数值，
     * 所以按 TECH §4.11.6 的口径做成内部常量。
     */
    private static final int LANDING_RADIUS = 2;

    /** 落点搜索的纵向范围（格）：水平半径 2 已定，纵向只做「脚下 / 同层 / 略高」三点修正。 */
    private static final int LANDING_VERTICAL_SCAN = 1;

    /** TECH §4.12：小爆炸不封顶，cap 取一个足够大的值等价于「不限制伤害」。 */
    private static final float SMALL_EXPLOSION_DAMAGE_CAP = 999.0F;

    /** config 千分比数组的索引 → 结果。不用 {@code Outcome#ordinal()} 直接对应，防止两边顺序漂移。 */
    private static final Outcome[] BY_CONFIG_INDEX = {
            Outcome.LIGHTNING, Outcome.SPAWN_CROPS, Outcome.REGEN, Outcome.LOOT_CHESTS,
            Outcome.SMALL_EXPLOSION, Outcome.CHARGED_CREEPER, Outcome.POWER, Outcome.CREATIVE
    };

    /**
     * 创造模式计时器（D-18 第①级）：UUID → 到期 {@code gameTime}。只在内存，绝不写存档；
     * 「本该还原成哪个模式」由 {@link FarmstormGameEvents} 的账本持有，这里不重复存。
     */
    private static final Map<UUID, Long> CREATIVE_EXPIRE_AT = new ConcurrentHashMap<>();

    private LuckyBerryRoller() {}

    /**
     * 当前生效的权重表（TECH §4.12）。
     *
     * <p>每次都从 {@link FarmstormConfig#luckyBerryPermille()} 取最新烘焙快照，而不是缓存一份：
     * {@code bake()} 重建表时没有任何回调能通知本类，缓存只会在 config 重载后陈旧。
     */
    public static List<Roll> table() {
        int[] permille = FarmstormConfig.luckyBerryPermille();
        List<Roll> rolls = new ArrayList<>(BY_CONFIG_INDEX.length);
        for (int index = 0; index < BY_CONFIG_INDEX.length; index++) {
            rolls.add(new Roll(BY_CONFIG_INDEX[index], permille[index]));
        }
        return List.copyOf(rolls);
    }

    /** 累计权重上界，长度与 {@link #table()} 相同，最后一个元素恒为 1000。 */
    private static int[] buildCumulative(List<Roll> table) {
        int[] cumulative = new int[table.size()];
        int running = 0;
        for (int index = 0; index < table.size(); index++) {
            running += table.get(index).permille();
            cumulative[index] = running;
        }
        if (running != PERMILLE_TOTAL) {
            // 配置层已经保证「百分比之和 = 100 → 千分比之和 = 1000」（§4.11.4），走到这里说明有人绕过了 bake。
            Farmstorm.LOGGER.warn("幸运浆果权重和为 {}（应为 1000），超出部分不会被抽到", running);
        }
        return cumulative;
    }

    /** 单次加权抽取：8 档互斥，每次食用恰好命中一个结果（TECH §4.12）。 */
    public static Outcome roll(RandomSource random) {
        return roll(random, table());
    }

    /** 抽取算法本体：表作参数传进来，「用哪张表」与「怎么抽」各管一件事。 */
    private static Outcome roll(RandomSource random, List<Roll> table) {
        int[] cumulative = buildCumulative(table);
        int roll = random.nextInt(PERMILLE_TOTAL);
        for (int index = 0; index < cumulative.length; index++) {
            if (roll < cumulative[index]) {
                return table.get(index).outcome();
            }
        }
        return Outcome.SPAWN_CROPS;                       // 权重和异常时的兜底，正常不到达
    }

    /** 执行抽中的分支（TECH §4.12 的 8 档执行细节），全部在服务端。 */
    public static void apply(ServerLevel level, ServerPlayer player, Outcome outcome) {
        RandomSource random = level.getRandom();
        switch (outcome) {
            case LIGHTNING -> applyLightning(level, player);
            case SPAWN_CROPS -> applySpawnCrops(level, player, random);
            case REGEN -> applyRegeneration(player);
            case LOOT_CHESTS -> applyLootChests(level, player, random);
            case SMALL_EXPLOSION -> applySmallExplosion(level, player);
            case CHARGED_CREEPER -> applyChargedCreeper(level, player, random);
            case POWER -> applyPower(player);
            case CREATIVE -> applyCreative(level, player);
        }
    }

    /** 15%：在食用者位置落一道闪电，不归属任何玩家击杀。 */
    private static void applyLightning(ServerLevel level, ServerPlayer player) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) {
            Farmstorm.LOGGER.warn("闪电实体创建失败，跳过 LIGHTNING 分支");
            return;
        }
        Vec3 at = player.position();
        bolt.moveTo(at.x, at.y, at.z);
        bolt.setCause(null);                              // TECH §4.12：无来源伤害
        level.addFreshEntity(bolt);
    }

    /** 25%：生成 {@code lucky_berry.randomCropCount} 个随机改良农作物（D-17 的 14 种池）。 */
    private static void applySpawnCrops(ServerLevel level, ServerPlayer player, RandomSource random) {
        List<CropProfile> pool = CropProfile.LUCKY_BERRY_POOL;
        int count = FarmstormConfig.luckyBerryRandomCropCount.get();
        for (int index = 0; index < count; index++) {
            CropProfile profile = pool.get(random.nextInt(pool.size()));
            Item item = FarmstormItems.forProfile(profile).get();
            BlockPos spot = findLandingSpot(level, player, random);
            level.addFreshEntity(new ItemEntity(level, spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 0.5,
                    new ItemStack(item)));
        }
    }

    /** 25%：生命恢复，等级与时长取 {@code lucky_berry.regeneration.*}。 */
    private static void applyRegeneration(ServerPlayer player) {
        applyEffect(player, MobEffects.REGENERATION,
                FarmstormConfig.luckyBerryRegenerationLevel.get(),
                FarmstormConfig.luckyBerryRegenerationDurationTicks.get());
    }

    /** 25%：生成 {@code lucky_berry.lootChestCount} 个末地城战利品宝箱（只挂战利品表，不预生成内容）。 */
    private static void applyLootChests(ServerLevel level, ServerPlayer player, RandomSource random) {
        int count = FarmstormConfig.luckyBerryLootChestCount.get();
        for (int index = 0; index < count; index++) {
            BlockPos spot = findLandingSpot(level, player, random);
            if (!level.setBlock(spot, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL)) {
                Farmstorm.LOGGER.debug("宝箱落点 {} 无法放置，跳过该箱", spot);
                continue;
            }
            if (!(level.getBlockEntity(spot) instanceof ChestBlockEntity chest)) {
                continue;
            }
            // 1.21.1 的 setLootTable 只接受表键，随机种子要单独设；TECH §4.12 的两参写法在这个版本不存在。
            chest.setLootTable(BuiltInLootTables.END_CITY_TREASURE);
            chest.setLootTableSeed(random.nextLong());
        }
    }

    /** 5%：威力取 {@code lucky_berry.smallExplosionPower} 的爆炸；幸运浆果没有 {@code DEMOLITION} 标签，故不破坏方块。 */
    private static void applySmallExplosion(ServerLevel level, ServerPlayer player) {
        Vec3 at = player.position();
        float power = FarmstormConfig.luckyBerrySmallExplosionPower.get();
        FarmstormExplosion explosion = new FarmstormExplosion(level, player,
                FarmstormDamageTypes.cropBlast(level, player),
                at.x, at.y, at.z, power,
                BlastRule.forCrop(CropProfile.LUCKY_BERRY, BlastRule.UNBREAKABLE_SKIP),
                SMALL_EXPLOSION_DAMAGE_CAP);
        ExplosionQueue.submit(level, explosion);
    }

    /** 3%：生成一只闪电苦力怕，不点燃。 */
    private static void applyChargedCreeper(ServerLevel level, ServerPlayer player, RandomSource random) {
        Creeper creeper = EntityType.CREEPER.create(level);
        if (creeper == null) {
            Farmstorm.LOGGER.warn("苦力怕实体创建失败，跳过 CHARGED_CREEPER 分支");
            return;
        }
        // 1.21.1 的 Creeper 没有 setPowered(boolean)（TECH §4.12 的写法已不存在），
        // 「被雷劈过」这个状态只由 readAdditionalSaveData / thunderHit 写入；
        // 这里用存档键直接落状态，避免为了一个布尔值凭空造一道闪电。
        CompoundTag powered = new CompoundTag();
        powered.putBoolean("powered", true);
        creeper.readAdditionalSaveData(powered);
        BlockPos spot = findLandingSpot(level, player, random);
        creeper.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5,
                random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(creeper);
    }

    /** 1.9%：四个效果一次性施加，等级与时长全部取 {@code lucky_berry.<effect>.*}。 */
    private static void applyPower(ServerPlayer player) {
        applyEffect(player, MobEffects.DAMAGE_BOOST,
                FarmstormConfig.luckyBerryStrengthLevel.get(),
                FarmstormConfig.luckyBerryStrengthDurationTicks.get());
        applyEffect(player, MobEffects.DAMAGE_RESISTANCE,
                FarmstormConfig.luckyBerryResistanceLevel.get(),
                FarmstormConfig.luckyBerryResistanceDurationTicks.get());
        applyEffect(player, MobEffects.JUMP,
                FarmstormConfig.luckyBerryJumpBoostLevel.get(),
                FarmstormConfig.luckyBerryJumpBoostDurationTicks.get());
        applyEffect(player, MobEffects.MOVEMENT_SPEED,
                FarmstormConfig.luckyBerrySpeedLevel.get(),
                FarmstormConfig.luckyBerrySpeedDurationTicks.get());
    }

    /**
     * 0.1%：D-18 三级兜底的第①级——记原模式、切创造、登记账本，{@code creativeDurationTicks} 后强制还原。
     *
     * <p>三种情况都要分清，否则会把人永久留在创造模式：
     * 本就是创造且不是本模组借出去的 → 只播音效，不登记（否则「还原成创造」等于没有还原）；
     * 账本里已有本模组的待还原记录 → 只把计时器重置为满时长（DESIGN §7.13「不叠加」）；
     * 其余情况才真正接管模式。
     */
    private static void applyCreative(ServerLevel level, ServerPlayer player) {
        FarmstormAdvancements.award(player, "lucky_jackpot");
        int duration = FarmstormConfig.luckyBerryCreativeDurationTicks.get();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        UUID playerId = player.getUUID();
        if (FarmstormGameEvents.hasPendingCreativeRestore(playerId)) {
            CREATIVE_EXPIRE_AT.put(playerId, level.getGameTime() + duration);
            return;
        }
        if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
            return;
        }
        GameType original = player.gameMode.getGameModeForPlayer();
        if (!player.setGameMode(GameType.CREATIVE)) {
            // 被其它模组的 PlayerChangeGameModeEvent 否决：不登记，避免「记录在但人没切过去」。
            Farmstorm.LOGGER.warn("创造模式切换被否决，跳过 CREATIVE 分支：{}", player.getGameProfile().getName());
            return;
        }
        FarmstormGameEvents.trackCreativeMode(player, original);
        CREATIVE_EXPIRE_AT.put(playerId, level.getGameTime() + duration);
        Farmstorm.LOGGER.info("幸运浆果创造模式分支：{} → CREATIVE，{} 刻后还原为 {}", player.getGameProfile().getName(),
                duration, original);
    }

    /**
     * D-18 第①级的结算点：到点强制还原。
     * 登录 / 登出已经还原过的（账本被销账）只需清掉本地计时器，不再重复动模式。
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID playerId = player.getUUID();
        Long expireAt = CREATIVE_EXPIRE_AT.get(playerId);
        if (expireAt == null) {
            return;
        }
        if (!FarmstormGameEvents.hasPendingCreativeRestore(playerId)) {
            CREATIVE_EXPIRE_AT.remove(playerId);
            return;
        }
        if (player.level().getGameTime() >= expireAt) {
            CREATIVE_EXPIRE_AT.remove(playerId);
            FarmstormGameEvents.restoreCreativeMode(player);
        }
    }

    /** 等级与时长同一条口径：{@code level} 以 1 起算（写入 amplifier 时减 1），{@code durationTicks = 0} 表示不施加（§4.11.3）。 */
    private static void applyEffect(ServerPlayer player, Holder<MobEffect> effect, int level, int durationTicks) {
        if (level <= 0 || durationTicks <= 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, durationTicks, level - 1));
    }

    /**
     * D-33 的落点：食用者半径 2 格内的随机可站立方块，放不下则就近。
     * 候选列随机洗牌后取第一个能站的地方，因此同一列不会因为遍历顺序固定而永远被优先。
     */
    private static BlockPos findLandingSpot(ServerLevel level, ServerPlayer player, RandomSource random) {
        BlockPos origin = player.blockPosition();
        List<BlockPos> columns = new ArrayList<>((LANDING_RADIUS * 2 + 1) * (LANDING_RADIUS * 2 + 1));
        for (int offsetX = -LANDING_RADIUS; offsetX <= LANDING_RADIUS; offsetX++) {
            for (int offsetZ = -LANDING_RADIUS; offsetZ <= LANDING_RADIUS; offsetZ++) {
                columns.add(origin.offset(offsetX, 0, offsetZ));
            }
        }
        Util.shuffle(columns, random);
        for (BlockPos column : columns) {
            for (int offsetY = LANDING_VERTICAL_SCAN; offsetY >= -LANDING_VERTICAL_SCAN; offsetY--) {
                BlockPos candidate = column.offset(0, offsetY, 0);
                if (isStandable(level, candidate)) {
                    return candidate;
                }
            }
        }
        return origin;                                    // D-33「放不下则就近」
    }

    /** 可站立：本格与头顶是空格，脚下有实心面（与原版「站得上去」的判定一致）。 */
    private static boolean isStandable(ServerLevel level, BlockPos pos) {
        return level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above())
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }
}
