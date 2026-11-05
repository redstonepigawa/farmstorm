package com.dashpig.farmstorm.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;

/**
 * 平凡韭菜种子（F-298 / D-216、裁定 #7 / #14）：轻量路径的种子侧，只做两件事——
 * 继承 {@link ItemNameBlockItem} 拿原版的放置流程（{@code useOn} → {@code BlockItem#place}，含 {@code consume(1)}），
 * 以及一个「无档案」的名字（名字来自物品 id，不走 {@code CropTier.nameStyle}）。
 *
 * <p><b>不复用 {@code SeedItem}</b>：它的 {@code useOn} 被 D-152 ② 钉死成无条件 {@code PASS}
 * （{@code SeedItem.java:41-44}），改它是推翻 0.2.0 的裁定，不改则平凡韭菜种不下去（红线 2 / N-142）。
 * 也<b>不写 tooltip</b>：{@code SeedItem.appendHoverText} 那套按档案取等级名的逻辑在这里没有输入。
 */
public class PlainSeedItem extends ItemNameBlockItem {
    public PlainSeedItem(Block block, Item.Properties properties) {
        super(block, properties);
    }
}
