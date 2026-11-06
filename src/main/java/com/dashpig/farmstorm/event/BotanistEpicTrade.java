package com.dashpig.farmstorm.event;

import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.CropTier;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * 一个植物学家的史诗交易项（F-279 / D-199 / 裁定 #18）：只收绿宝石、只给 4 个史诗作物里的 1 个、成交一次即断货。
 *
 * <p><b>抽签只在报价首次生成时做一次</b>（机制与 {@link TierTrade} 同形，48 号 §2.4.4）：
 * {@code getOffer} 产出的 {@link MerchantOffer} 会被 {@code AbstractVillager} 存进村民的 {@code Offers}，
 * 此后的补货只由原版 {@code Villager#restock()} 调 {@link MerchantOffer#resetUses()} 把 {@code uses} 归零，
 * <b>不会</b>重新调用本方法 ⇒ 同一村民的史诗项在整个存档内只卖一个作物 id、一个基准价。
 *
 * <p><b>「固定 1 项」不是「4 个都卖」</b>：一个植物学家只有这 1 项，凑齐 4 个史诗作物同样要多找几个植物学家。
 * 这是作者选定的机制（裁定 #18），不是原版限制。
 *
 * <p>数值：{@code maxUses = 1}（断货由它承担）、{@code xp}（5 级档，取 {@link VillagerTradeEvents} 的五级常量，D-238）、
 * {@code priceMultiplier = 0.0F}（把 {@code MerchantOffer#getModifiedCostCount} 里的
 * {@code floor(i × demand × multiplier)} 归零 ⇒ 成交后不涨价；声望折扣 {@code specialPriceDiff}
 * 是独立加法项，照常作用）。价格取<b>类常量</b>，不读 {@code trade.*} 的 6 个键、不新增 config 键（176 不变）。
 *
 * @param pool 史诗档全集的档案（4 条），由 {@code CropProfile.ALL} 按 {@code tier()} 过滤得来（不写第二份 id 清单）
 */
public record BotanistEpicTrade(List<CropProfile> pool) implements VillagerTrades.ItemListing {

    /** 价格下界（裁定 #13：40 ~ 64，闭区间）。 */
    public static final int MIN_PRICE = 40;
    /** 价格上界：64 − 40 + 1 = 25 个取值。 */
    public static final int MAX_PRICE = 64;
    /** 成交一次即售罄（裁定 #13）。 */
    public static final int MAX_USES = 1;
    /** 单次给出的作物数（48 号 §2.4.4：每次 1 个）。 */
    public static final int COUNT = 1;
    /** 成交经验：5 级档，引用 {@link VillagerTradeEvents} 的五级常量（D-238）。 */
    public static final int VILLAGER_XP = VillagerTradeEvents.XP_LEVEL_5;
    /** 锁死基准价：关掉供需浮动通道（照 {@code TierTrade:47}）。 */
    public static final float PRICE_MULTIPLIER = 0.0F;

    /** 5 级列表里的唯一一项；池由 {@code tier()} 推导，恰好是史诗档的 4 条。 */
    public static final BotanistEpicTrade EPIC = new BotanistEpicTrade(
            CropProfile.ALL.stream().filter(profile -> profile.tier() == CropTier.EPIC).toList());

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        // 步 1：本档 4 条档案等概率抽 1 条——整个存档内只此一次（补货只归零 uses，不重调本方法）。
        CropProfile picked = this.pool.get(random.nextInt(this.pool.size()));
        Item item = FarmstormItems.forProfile(picked).get();
        // 步 2：闭区间取随机价（nextInt(bound) 上界开区间，所以 +1），与 TierTrade:75 同形。
        int price = MIN_PRICE + random.nextInt(MAX_PRICE - MIN_PRICE + 1);
        // 步 3：只收绿宝石，给 1 个被抽中的史诗作物。
        ItemCost cost = new ItemCost(Items.EMERALD, price);
        return new MerchantOffer(cost, Optional.empty(), new ItemStack(item, COUNT),
                MAX_USES, VILLAGER_XP, PRICE_MULTIPLIER);
    }
}
