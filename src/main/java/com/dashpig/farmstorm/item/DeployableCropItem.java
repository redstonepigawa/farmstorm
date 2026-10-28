package com.dashpig.farmstorm.item;

import com.dashpig.farmstorm.util.CropProfile;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/**
 * 5 个部署型作物的物品：同时是部署方块的 BlockItem（T-02 / D-35）。
 * useOn 里做二选一分流——命中耕地就播种作物方块，否则走 BlockItem 的部署流程。
 * 归属写入统一由 DeployedCropBlock#setPlacedBy 完成（C-9），本类不碰存档字段。
 */
public class DeployableCropItem extends net.minecraft.world.item.BlockItem {
    private final CropProfile profile;

    public DeployableCropItem(CropProfile profile, Block block, Item.Properties properties) {
        super(block, properties);
        this.profile = profile;
    }

    public CropProfile profile() {
        return this.profile;
    }

    /** 类别行与作物物品 / 播种物同一份实现（本类不继承 ModifiedCropItem，因此显式委托）。 */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tips, TooltipFlag flag) {
        ModifiedCropItem.appendCropTooltip(this.profile, tips);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        InteractionResult planted = ModifiedCropItem.plantCrop(this.profile, ctx);
        if (planted != InteractionResult.PASS) {
            return planted;
        }
        return super.useOn(ctx);
    }
}
