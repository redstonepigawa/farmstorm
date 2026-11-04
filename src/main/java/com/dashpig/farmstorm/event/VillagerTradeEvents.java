package com.dashpig.farmstorm.event;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/**
 * 农民交易注册（F-223 / D-157）：全项目第一次与 {@code Villager} / 交易打交道。
 *
 * <p>事件由 {@code it.unimi.dsi.fastutil} 的 {@code VillagerTradingManager#postVillagerEvents} 在每次
 * {@code TagsUpdatedEvent}（数据包重载，含起服）时<b>对每个职业各抛一次</b>，挂的是 game 总线
 * （{@code NeoForge.EVENT_BUS}），不可取消。{@code getTrades()} 的键 1~5 恒非 null
 * （{@code VillagerTradesEvent} 的类注释原文），且值是 {@code NonNullList}（可变）
 * ⇒ 直接 {@code add} 即可，不需要退路里的 {@code put(new ArrayList<>(...))}。
 *
 * <p>位次照 36 号 §2.4 定为 <b>1 / 3 / 5</b>：三段铺开，2 / 4 留空以不与原版农民交易抢抽样池
 * （原版每级从该级列表里抽 2 项）。写错成 2 / 4 会让本模组的项一项都看不到（RT-46）。
 */
public final class VillagerTradeEvents {

    private VillagerTradeEvents() {}

    /** 由 {@code Farmstorm} 构造器与 {@link FarmstormGameEvents} 并列挂到同一条 game 总线。 */
    public static void register(IEventBus gameBus) {
        gameBus.addListener(VillagerTradeEvents::onVillagerTrades);
    }

    /** 只处理农民；其它职业一律原样放行（不清空、不改动原版任何交易项）。 */
    private static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.FARMER) {
            return;
        }
        event.getTrades().get(1).add(TierTrade.COMMON);
        event.getTrades().get(3).add(TierTrade.RARE);
        event.getTrades().get(5).add(TierTrade.EPIC);
    }
}
