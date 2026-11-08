package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.block.DeployedCropBlock;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.util.CropProfile;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

/**
 * 震动豆的投掷物（TECH §3.5）：落点放置一个不依附方块的震动豆陷阱（{@code DeployedKind.VIBRATION_BEAN}）。
 *
 * <p>投掷路径不经过 {@code BlockItem}，因此 {@code DeployedCropBlock#setPlacedBy}（唯一写 owner 的地方）
 * 不会被调用；归属必须在这里显式写入，否则陷阱会把自己人也判成「非己方」（D-92 的触发端会失真）。
 */
public class VibrationBeanProjectile extends ThrownCropEntity {

    /**
     * 写入用的 block update flag：{@code UPDATE_NEIGHBORS | UPDATE_CLIENTS}（= 3，TECH §3.5 ③ 的原值）。
     * 邻居通知不会把方块弄掉：不依附模式（F-123）的 {@code updateShape} 恒返回自身。
     */
    private static final int WRITE_FLAGS = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;

    public VibrationBeanProjectile(EntityType<? extends VibrationBeanProjectile> type, Level level) {
        super(type, level);
    }

    public VibrationBeanProjectile(EntityType<? extends VibrationBeanProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.VIBRATION_BEAN;
    }

    /**
     * TECH §3.5 的四步（同一刻完成）：落点格 → 不可替换则上移一格 → 放置闸门 → 写方块并登记归属。
     * 两格都不可替换、或被 {@link DeployedCropBlockEntity#canDeploy} 拒绝时不放置，投掷物照常消失
     * （放置闸门不能被投掷路径绕过，TECH §3.5 ②）。
     */
    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        Player shooter = this.getOwner() instanceof Player player ? player : null;
        BlockPos landing = BlockPos.containing(pos);
        if (!level.getBlockState(landing).canBeReplaced()) {
            landing = landing.above();
            if (!level.getBlockState(landing).canBeReplaced()) {
                Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                        "震动豆 落点 %s 与上方一格都不可替换，不放置", BlockPos.containing(pos).toShortString()));
                return;
            }
        }
        if (!DeployedCropBlockEntity.canDeploy(level, landing, shooter)) {
            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                    "震动豆 落点 %s 被部署闸门拒绝（5 刻冷却），不放置",
                    landing.toShortString()));
            return;
        }
        level.setBlock(landing, FarmstormBlocks.VIBRATION_BEAN.get().defaultBlockState()
                .setValue(DeployedCropBlock.FACE, AttachFace.FLOOR)
                .setValue(DeployedCropBlock.FACING, Direction.NORTH)
                .setValue(DeployedCropBlock.FLOATING, false), WRITE_FLAGS);
        if (shooter != null && level.getBlockEntity(landing) instanceof DeployedCropBlockEntity be) {
            be.setOwner(shooter);
        }
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "震动豆 落点 %s 放置 farmstorm:vibration_bean（owner=%s）",
                landing.toShortString(), shooter == null ? "无（发射器 / summon）" : shooter.getGameProfile().getName()));
    }
}
