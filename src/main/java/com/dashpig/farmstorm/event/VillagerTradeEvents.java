package com.dashpig.farmstorm.event;

import com.dashpig.farmstorm.registry.FarmstormVillagerProfessions;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/**
 * 农民交易注册（F-223 / D-157）：全项目第一次与 {@code Villager} / 交易打交道。
 *
 * <p>事件由 {@code it.unimi.dsi.fastutil} 的 {@code VillagerTradingManager#postVillagerEvents} 在
 * {@code UpdateCause == SERVER_DATA_LOAD} 时<b>对每个职业各抛一次</b>（{@code VillagerTradingManager:37-42}），
 * 挂的是 game 总线（{@code NeoForge.EVENT_BUS}），不可取消。{@code /reload} 的 cause 是 {@code RELOAD}
 * ⇒ <b>不重发</b>，故 {@code /reload} 之后看到旧交易列表是正常现象（48 号 §2.4.1 第 3 条、§0.5 死命令 4）。
 * {@code getTrades()} 的键 1~5 恒非 null
 * （{@code VillagerTradesEvent} 的类注释原文），且值是 {@code NonNullList}（可变）
 * ⇒ 直接 {@code add} 即可，不需要退路里的 {@code put(new ArrayList<>(...))}。
 *
 * <p><b>农民三档</b>的位次照 36 号 §2.4 定为 <b>1 / 3 / 5</b>：三段铺开，2 / 4 留空以不与原版农民交易抢抽样池
 * （原版每级从该级列表里抽 2 项）。写错成 2 / 4 会让本模组的项一项都看不到（RT-46）。植物学家另按
 * 1 / 2 / 3 / 4 / 5 铺满（作者 2026-11-05 改判 46 号裁定 #11）。
 */
public final class VillagerTradeEvents {

    private VillagerTradeEvents() {}

    /** 由 {@code Farmstorm} 构造器与 {@link FarmstormGameEvents} 并列挂到同一条 game 总线。 */
    public static void register(IEventBus gameBus) {
        gameBus.addListener(VillagerTradeEvents::onVillagerTrades);
    }

    /**
     * 植物学家与农民各一段、各自 {@code return}；其它职业一律原样放行（不清空、不改动原版任何交易项）。
     *
     * <p>植物学家必须写在农民早退<b>之前</b>：两段的数值口径不同（植物学家普通项每次 1 个、农民每项 2 个），
     * 合并成一个 {@code if} 会改到农民的口径，等于推翻裁定 #7 / D-200。1~5 级列表恒非 null
     * （{@code VillagerTradingManager:64-66}）⇒ 直接 {@code add} / {@code addAll}，
     * 不需要 {@code put(new ArrayList<>(...))}。
     */
    private static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == FarmstormVillagerProfessions.BOTANIST.get()) {
            // 22 项摊到 1~4 级 = 6 / 6 / 6 / 4（作者 2026-11-05 改判 46 号裁定 #11）：原版每级是独立表，
            // 空表就抽不出任何项 ⇒ 只挂 1 级时 2 / 3 / 4 级整级空白。每级仍由原版抽 2 项（裁定 #17 不变）。
            event.getTrades().get(1).addAll(BotanistTrade.NORMAL.subList(0, 6));
            event.getTrades().get(2).addAll(BotanistTrade.NORMAL.subList(6, 12));
            event.getTrades().get(3).addAll(BotanistTrade.NORMAL.subList(12, 18));
            event.getTrades().get(4).addAll(BotanistTrade.NORMAL.subList(18, 22));
            // 史诗固定 1 项、进大师级（裁定 #13 / #18）；它与 TierTrade 的 EPIC 同名不同类，故必须带类名。
            event.getTrades().get(5).add(BotanistEpicTrade.EPIC);
            return;
        }
        if (event.getType() != VillagerProfession.FARMER) {
            return;
        }
        event.getTrades().get(1).add(TierTrade.COMMON);
        event.getTrades().get(3).add(TierTrade.RARE);
        event.getTrades().get(5).add(TierTrade.EPIC);
    }
}
