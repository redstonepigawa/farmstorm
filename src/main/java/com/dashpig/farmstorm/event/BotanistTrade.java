package com.dashpig.farmstorm.event;

import com.dashpig.farmstorm.registry.FarmstormItems;
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
 * 一个植物学家普通交易项（F-278 / D-197）：只收绿宝石、只给清单里那 1 个物品、每次 1 个、成交 8 次即断货。
 *
 * <p><b>本类不抽签</b>（48 号 §2.4.3 第 3 条）：普通项是「固定卖这一件」，22 条 listing 全部由事件处理器
 * 挂进<b>新手级（1 级）</b>，抽样发生在原版 {@code Villager#updateTrades} 那一层
 * （{@code Villager.java:875} 写死每级抽 2 条，语义见 {@code AbstractVillager.java:233-244}：随机、不放回、最多 N 条）
 * ⇒ 每个植物学家只显示 22 项里的 <b>2 项</b>，换村民才换项。这是裁定 #17 / D-198 已接受的已知限制，
 * <b>不是 bug</b>；在 {@code getOffer} 里再抽一次签会把「22 项」变成「22 × 每次刷新」。
 *
 * <p><b>{@code getOffer} 不许返回 {@code null}</b>（48 号 §2.4.3 第 4 条）：原版对 {@code null} 的处理是
 * 「不 add、计数不 ++、继续循环」⇒ 那一条会<b>静默消失</b>，列表少于 22 条且无任何报错。
 *
 * <p>五个数值是<b>类常量</b>（成交经验改由每条 listing 自带，见 {@code xp} 分量），不读 {@code trade.*} 的 6 个 config 键、不新增键（176 不变；47 号 §2.5）。
 * 农民那三档为什么是可配的、这两档为什么是常量，见 47 号 §2.5 末尾的对比段。
 *
 * <p><b>类初始化时机</b>：{@link #NORMAL} 在类初始化时就把 2 个本模组物品解析成 {@code Item}
 * （{@code DeferredItem#get()}）⇒ 本类必须<b>只在物品注册完成之后</b>被首次触碰。当前唯一入口是
 * 交易事件（{@code SERVER_DATA_LOAD}，见 {@code VillagerTradingManager.java:37-42}），那时注册早已完成；
 * 若将来有人在静态初始化阶段引用本类，会得到 {@code NullPointerException} 而不是静默错值。
 *
 * @param item 本项出售的物品（22 项里 20 项是 {@code minecraft:} 物品、1 项是本模组非作物物品 {@code plain_chives}、
 *             1 项是二级合成物 {@code compressed_puffed_wheat}）
 * @param xp   本项的成交经验：等级由注册处的切片位置决定（1~4 级），取值一律引用 {@code VillagerTradeEvents} 的五级常量
 */
public record BotanistTrade(Item item, int xp) implements VillagerTrades.ItemListing {

    /** 价格下界（绿宝石），闭区间；裁定 #12 的死数。 */
    public static final int MIN_PRICE = 3;
    /** 价格上界（绿宝石），闭区间 ⇒ 6 − 3 + 1 = 4 个取值，各 1/4（47 号 §2.5）。 */
    public static final int MAX_PRICE = 6;
    /** 断货前的成交次数（裁定 #12）：指的是能成交 8 次，不是「补货 8 次」也不是「一次 8 个」。 */
    public static final int MAX_USES = 8;
    /** 每次给出的物品数（裁定 #12）。 */
    public static final int COUNT = 1;
    /** 锁死基准价：关掉供需浮动通道，声望折扣照常作用（沿用 {@code TierTrade:47}）。 */
    public static final float PRICE_MULTIPLIER = 0.0F;

    /**
     * 22 项普通交易，<b>顺序 = 47 号 §2.3 表的 1 ~ 22 行</b>（该表是全项目唯一清单，这里不另排）。
     *
     * <p>清单形状是<b>显式写死的 22 个实例</b>，不从 {@code CropProfile} 派生（48 号 §2.4.3 第 2 条）：
     * 20 个原版物品不是档案、{@code plain_chives} 没有档案、{@code compressed_puffed_wheat} 是二级合成物
     * ⇒ 没有任何一个共同字段能推出来。第 19 项是压缩膨胀麦、第 20 项是平凡韭菜（47 号 §2.3 的对应两行）。
     */
    public static final List<BotanistTrade> NORMAL = List.of(
            new BotanistTrade(Items.WHEAT, VillagerTradeEvents.XP_LEVEL_1),            // 1  puffed_wheat            → minecraft:wheat
            new BotanistTrade(Items.CARROT, VillagerTradeEvents.XP_LEVEL_1),           // 2  blast_carrot            → minecraft:carrot
            new BotanistTrade(Items.POTATO, VillagerTradeEvents.XP_LEVEL_1),           // 3  ubi_server              → minecraft:potato
            new BotanistTrade(Items.POISONOUS_POTATO, VillagerTradeEvents.XP_LEVEL_1), // 4  toxic_ubi_server        → minecraft:poisonous_potato
            new BotanistTrade(Items.BEETROOT, VillagerTradeEvents.XP_LEVEL_1),         // 5  power_beet              → minecraft:beetroot
            new BotanistTrade(Items.MELON_SLICE, VillagerTradeEvents.XP_LEVEL_1),      // 6  melon_slice_bomb        → minecraft:melon_slice
            new BotanistTrade(Items.MELON, VillagerTradeEvents.XP_LEVEL_2),            // 7  melon_grenade           → minecraft:melon
            new BotanistTrade(Items.PUMPKIN, VillagerTradeEvents.XP_LEVEL_2),          // 8  pumpkin_turret          → minecraft:pumpkin
            new BotanistTrade(Items.TORCHFLOWER, VillagerTradeEvents.XP_LEVEL_2),      // 9  awakened_torchflower    → minecraft:torchflower
            new BotanistTrade(Items.PITCHER_PLANT, VillagerTradeEvents.XP_LEVEL_2),    // 10 awakened_pitcher_plant  → minecraft:pitcher_plant
            new BotanistTrade(Items.CACTUS, VillagerTradeEvents.XP_LEVEL_2),           // 11 fang_trap_cactus        → minecraft:cactus
            new BotanistTrade(Items.BAMBOO, VillagerTradeEvents.XP_LEVEL_2),           // 12 bamboo_firecracker      → minecraft:bamboo
            new BotanistTrade(Items.RED_MUSHROOM, VillagerTradeEvents.XP_LEVEL_3),     // 13 red_mushroom_puff       → minecraft:red_mushroom
            new BotanistTrade(Items.BROWN_MUSHROOM, VillagerTradeEvents.XP_LEVEL_3),   // 14 brown_mushroom_puff     → minecraft:brown_mushroom
            new BotanistTrade(Items.SWEET_BERRIES, VillagerTradeEvents.XP_LEVEL_3),    // 15 lucky_berry             → minecraft:sweet_berries
            new BotanistTrade(Items.CRIMSON_FUNGUS, VillagerTradeEvents.XP_LEVEL_3),   // 16 molotov                 → minecraft:crimson_fungus
            new BotanistTrade(Items.SUGAR_CANE, VillagerTradeEvents.XP_LEVEL_3),       // 17 zangief_cane            → minecraft:sugar_cane
            new BotanistTrade(Items.GOLDEN_APPLE, VillagerTradeEvents.XP_LEVEL_3),     // 18 wild_apple              → minecraft:golden_apple
            new BotanistTrade(FarmstormItems.COMPRESSED_PUFFED_WHEAT.get(), VillagerTradeEvents.XP_LEVEL_4), // 19 二级合成物（D-56，无转换配方）
            new BotanistTrade(FarmstormItems.PLAIN_CHIVES.get(), VillagerTradeEvents.XP_LEVEL_4),            // 20 本版唯一新造的交易项（D-193）
            new BotanistTrade(Items.WARPED_FUNGUS, VillagerTradeEvents.XP_LEVEL_4),    // 21 point_defense_fungus    → minecraft:warped_fungus
            new BotanistTrade(Items.COCOA_BEANS, VillagerTradeEvents.XP_LEVEL_4));     // 22 vibration_bean          → minecraft:cocoa_beans

    /** 闭区间随机价 → 只收绿宝石、给 1 个本项物品；不抽签、不返回 {@code null}。 */
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        // nextInt(bound) 上界开区间，所以 +1；6 − 3 + 1 = 4 个取值（47 号 §2.5）。
        int price = MIN_PRICE + random.nextInt(MAX_PRICE - MIN_PRICE + 1);
        ItemCost cost = new ItemCost(Items.EMERALD, price);
        return new MerchantOffer(cost, Optional.empty(), new ItemStack(this.item, COUNT),
                MAX_USES, this.xp, PRICE_MULTIPLIER);
    }
}
