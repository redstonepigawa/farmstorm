package com.dashpig.farmstorm.item;

import com.dashpig.farmstorm.util.CropCategory;
import com.dashpig.farmstorm.util.CropProfile;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 15 个作物物品的共同父类（TECH §4.3.1）。
 * 作物方块本身不注册 BlockItem（D-34），所以「对着耕地就播种」这件事必须由物品自己完成——这就是本类的核心职责。
 */
public class ModifiedCropItem extends Item {
    /**
     * 类别分隔符用一个语言键表示：中英各自的分隔符（「、」/「, 」）只能由语言文件给出，
     * 在公共代码里读客户端语言会在专用服务端加载客户端类。
     */
    private static final String CATEGORY_SEPARATOR_KEY = "tooltip.farmstorm.category_separator";
    /** 品质行的行格式键；三档的档名由 {@code CropTier#translationKey()} 给出（3 + 1 = 4 条新键）。 */
    private static final String TIER_KEY = "tooltip.farmstorm.tier";

    private final CropProfile profile;

    public ModifiedCropItem(CropProfile profile, Item.Properties properties) {
        super(properties);
        this.profile = profile;
    }

    public CropProfile profile() {
        return this.profile;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        return plantCrop(this.profile, ctx);
    }

    /** 是否允许在该位置播种：原版作物语义只认耕地（§2.2.3）。 */
    protected boolean canPlantAt(UseOnContext ctx, BlockState soil) {
        return soil.getBlock() instanceof FarmBlock;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tips, TooltipFlag flag) {
        appendCropTooltip(this.profile, tips);
    }

    /**
     * 名色（D-151）：档色套在**物品名这个 {@link Component}** 上，所以铁砧改名、告示牌、聊天栏、
     * 容器预览里的同名物品全部同色——不需要在任何 GUI 里二次绘制。
     * 走 {@link Item#getName} 而不是 tooltip，因为 {@code ItemStack#getHoverName()} 在没有
     * {@code CUSTOM_NAME} / {@code ITEM_NAME} 组件时就是调它。覆盖 15 个物品（投掷 12 + 食用 3）。
     */
    @Override
    public Component getName(ItemStack stack) {
        return this.profile.tier().nameStyle(super.getName(stack));
    }

    /**
     * 播种分流的唯一实现：ModifiedCropItem 自己用，DeployableCropItem 的「播种 / 部署」二选一也用它。
     * 返回 PASS 表示「这里不适合播种」，交回调用方继续走默认行为。
     */
    public static InteractionResult plantCrop(CropProfile profile, UseOnContext ctx) {
        ResourceKey<Block> cropKey = profile.cropBlock();
        if (cropKey == null) {
            return InteractionResult.PASS;                     // 瓜片子不可种植（§3.2.3）
        }
        Level level = ctx.getLevel();
        if (!isPlantableSoil(ctx, level.getBlockState(ctx.getClickedPos()))) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;                  // HC-03：放置由服务端完成，客户端只回手感
        }
        Block crop = BuiltInRegistries.BLOCK.get(cropKey);
        if (crop == null) {
            return InteractionResult.PASS;
        }
        BlockPlaceContext placeContext = new BlockPlaceContext(ctx);
        if (!placeContext.canPlace()) {
            return InteractionResult.PASS;
        }
        BlockState placement = crop.getStateForPlacement(placeContext);
        if (placement == null || !placement.canSurvive(level, placeContext.getClickedPos())) {
            return InteractionResult.PASS;
        }
        BlockPos target = placeContext.getClickedPos();
        if (!level.setBlock(target, placement, Block.UPDATE_ALL)) {
            return InteractionResult.PASS;
        }
        Player player = ctx.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    /**
     * 裁定 #4 / D-152 ①：v0.2.0 起本模组作物不可种植——判据恒 {@code false}，{@code plantCrop} 一律回 PASS。
     * 注意这只堵「自定义播种」这一条；7 个独立种子那条链已在 v0.2.3 随种子一起删除（D-212），另一条（陷阱 23 / RT-48）。
     */
    private static boolean isPlantableSoil(UseOnContext ctx, BlockState soil) {
        // 原判据（「是 FarmBlock 才允许」）留成注释备查，不删（36 号 §2.2 路径①）：
        // if (!(soil.getBlock() instanceof FarmBlock)) {
        //     return false;
        // }
        // FarmBlock 的湿润状态只影响生长速度，不影响能否播种，因此这里不再额外判断。
        return false;
    }

    /**
     * 类别提示：按 DEC §4 的书写顺序把**全部**类别拼成一行——类别是多值集合，只显示第一个就是错的。
     * 作物物品、播种物与部署型物品共用这一份文案，避免同一个分类写两遍。
     */
    public static void appendCropTooltip(@Nullable CropProfile profile, List<Component> tips) {
        if (profile == null || profile.categories().isEmpty()) {
            return;
        }
        MutableComponent line = Component.empty();
        boolean first = true;
        for (CropCategory category : profile.categories()) {
            if (!first) {
                line.append(Component.translatable(CATEGORY_SEPARATOR_KEY));
            }
            line.append(Component.translatable(category.translationKey()));
            first = false;
        }
        tips.add(line.withStyle(ChatFormatting.GRAY));
        // D-151：品质行**另起一行**，不与上面的类别行合并（类别 ≠ 品质，是两套语义）。
        // 外壳 GRAY 与类别行同色、档名子组件带自己的颜色，不抢视觉。29 个物品共用这一处实现。
        tips.add(Component.translatable(TIER_KEY,
                Component.translatable(profile.tier().translationKey())).withStyle(ChatFormatting.GRAY));
    }
}
