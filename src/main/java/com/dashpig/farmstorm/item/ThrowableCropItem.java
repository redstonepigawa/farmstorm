package com.dashpig.farmstorm.item;

import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 投掷型作物（8 个，TECH §4.3.2）。右键即生成对应投掷物实体，消耗 1 个并进入短冷却。
 * 服务端权威：客户端只回手感，不生成实体、不扣物品（HC-03）。
 */
public class ThrowableCropItem extends ModifiedCropItem {
    /** HC-13：投掷冷却属安全阀，刻意不进 config。 */
    public static final int THROW_COOLDOWN_TICKS = 5;

    private final Supplier<EntityType<? extends ThrownCropEntity>> projectileType;

    public ThrowableCropItem(CropProfile profile, Supplier<EntityType<? extends ThrownCropEntity>> projectileType,
                             Item.Properties properties) {
        super(profile, properties);
        this.projectileType = projectileType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        ThrownCropEntity projectile = this.createProjectile((ServerLevel) level, player, stack);
        if (projectile == null) {
            return InteractionResultHolder.fail(stack);
        }
        projectile.setItem(stack);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, this.velocity(), this.inaccuracy());
        level.addFreshEntity(projectile);
        if (player instanceof ServerPlayer serverPlayer) {
            FarmstormAdvancements.award(serverPlayer, "first_throw");
        }
        // 1.21.1 的 ItemStack#consume(1, player) 会跳过「无限材料」（创造模式）；1.20.1 只有 shrink，
        // 创造性豁免要自己写，否则创造玩家扔一个少一个（v0.2.4 的可观察行为）。
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, THROW_COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    /** 允许子类改写初速与散布（瓜！ 的子母弹需要）。 */
    @Nullable
    protected ThrownCropEntity createProjectile(ServerLevel level, Player player, ItemStack stack) {
        ThrownCropEntity projectile = this.projectileType.get().create(level);
        if (projectile == null) {
            return null;
        }
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        return projectile;
    }

    protected float velocity() {
        return 1.5F;
    }

    protected float inaccuracy() {
        return 1.0F;
    }
}
