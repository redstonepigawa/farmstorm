package com.dashpig.farmstorm.item;

import com.dashpig.farmstorm.util.CropProfile;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * 6 个独立播种物的唯一实现（TECH §4.3.3）。
 * 播种逻辑全部继承 ItemNameBlockItem，本类只补「作物档案」以复用统一的 tooltip。
 */
public class SeedItem extends ItemNameBlockItem {
    private final CropProfile profile;

    public SeedItem(CropProfile profile, Block cropBlock, Item.Properties properties) {
        super(cropBlock, properties);
        this.profile = profile;
    }

    public CropProfile profile() {
        return this.profile;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tips, TooltipFlag flag) {
        ModifiedCropItem.appendCropTooltip(this.profile, tips);
    }

    /**
     * 名色（D-151）：种子**没有自己的品质来源**，一律跟随父作物（DEC §4.2）。
     * 本类另立一支（{@code extends ItemNameBlockItem}），只改父类会漏掉这 7 个播种物。
     */
    @Override
    public Component getName(ItemStack stack) {
        return this.profile.tier().nameStyle(super.getName(stack));
    }
}
