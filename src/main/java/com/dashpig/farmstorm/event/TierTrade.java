package com.dashpig.farmstorm.event;

import com.dashpig.farmstorm.config.FarmstormConfig;
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
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 一个农民交易项（F-223 / D-157 ~ D-159）：只收绿宝石、只给该档的 2 个作物、成交一次即断货。
 *
 * <p><b>抽签与定价都在报价首次生成时完成一次</b>（35 号 §4.4 / D-159 的时序纪律）：
 * {@code getOffer} 产出的 {@link MerchantOffer} 会被 {@code AbstractVillager} 存进村民的 {@code Offers}，
 * 此后的补货只由原版 {@code resetUses()} 把 {@code uses} 归零，<b>不会</b>重新调用本方法
 * ⇒ 同一村民的这一项整个存档内只卖一个作物 id、一个基准价。
 *
 * <p>三项数值全部照 36 号 §2.4 的表格：{@code maxUses = 1}（断货由它承担，不写第二套计时器）、
 * {@code xp}（三档各自带值：1 / 3 / 5 级取 {@link VillagerTradeEvents} 里对应级的常量）、{@code priceMultiplier = 0.0F}
 * （把 {@code MerchantOffer#getModifiedCostCount} 里的 {@code floor(i × demand × multiplier)} 归零
 * ⇒ 成交后不涨价；声望折扣 {@code specialPriceDiff} 是独立加法项，照常作用）。
 *
 * @param tier   档位（三档各一项，同时决定池与语言键）
 * @param pool   该档全集的档案，由 {@code CropProfile.ALL} 按 {@code tier()} 过滤得来（不写第二份 id 清单）
 * @param minKey 价格下界的 config 键（{@code trade.<档>.min}）
 * @param maxKey 价格上界的 config 键（{@code trade.<档>.max}）
 * @param xp     本档的成交经验，由等级决定（1 / 3 / 5 级各取 {@code VillagerTradeEvents} 里对应级的常量）
 */
public record TierTrade(CropTier tier, List<CropProfile> pool, ModConfigSpec.IntValue minKey,
                        ModConfigSpec.IntValue maxKey, int xp) implements VillagerTrades.ItemListing {

    /** 单次给出的作物数（35 号 §4.3：三档一致，且不进 config）。 */
    public static final int CROP_COUNT = 2;
    /** 成交一次即售罄（D-158）。 */
    public static final int MAX_USES = 1;
    /** 锁死基准价：关掉供需浮动通道（36 号 §2.4.1）。 */
    public static final float PRICE_MULTIPLIER = 0.0F;

    public static final TierTrade COMMON =
            of(CropTier.COMMON, FarmstormConfig.tradeCommonMin, FarmstormConfig.tradeCommonMax,
                    VillagerTradeEvents.XP_LEVEL_1);
    public static final TierTrade RARE =
            of(CropTier.RARE, FarmstormConfig.tradeRareMin, FarmstormConfig.tradeRareMax,
                    VillagerTradeEvents.XP_LEVEL_3);
    public static final TierTrade EPIC =
            of(CropTier.EPIC, FarmstormConfig.tradeEpicMin, FarmstormConfig.tradeEpicMax,
                    VillagerTradeEvents.XP_LEVEL_5);

    private static TierTrade of(CropTier tier, ModConfigSpec.IntValue minKey, ModConfigSpec.IntValue maxKey,
                                int xp) {
        return new TierTrade(tier, CropProfile.ALL.stream().filter(profile -> profile.tier() == tier).toList(),
                minKey, maxKey, xp);
    }

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        // 步 1：该档全集等概率抽 1 条（D-157 补充 / 裁定 #18 的抽样代价）。
        CropProfile picked = this.pool.get(random.nextInt(this.pool.size()));
        Item item = FarmstormItems.forProfile(picked).get();
        // 步 2：读区间；玩家把 min 写成大于 max 时交换，不抛异常（36 号 §4.1.1 的运行期纪律 ①）。
        int min = this.minKey.get();
        int max = this.maxKey.get();
        if (max < min) {
            int swap = min;
            min = max;
            max = swap;
        }
        // 步 3：闭区间取随机（nextInt(bound) 上界开区间，所以 +1）；min == max 时取该值（纪律 ②）。
        int price = (max == min) ? min : min + random.nextInt(max - min + 1);
        // 步 4 / 5：只收绿宝石，给 2 个同档作物。
        ItemCost cost = new ItemCost(Items.EMERALD, price);
        return new MerchantOffer(cost, Optional.empty(), new ItemStack(item, CROP_COUNT),
                MAX_USES, this.xp, PRICE_MULTIPLIER);
    }
}
