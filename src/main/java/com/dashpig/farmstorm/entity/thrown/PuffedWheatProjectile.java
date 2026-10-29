package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.explosion.BlastRule;
import com.dashpig.farmstorm.util.CropProfile;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/**
 * 膨胀麦（DESIGN §4.1 / TECH §4.7）：落点立方体整体写入 {@code minecraft:hay_block}，空气与流体一并覆盖。
 * 落点立方体内存在水源时边长由 {@code puffed_wheat.dryEdge} 扩到 {@code puffed_wheat.wetEdge}。
 * 替换是「写入」而不是爆炸（DESIGN §7.11 优先级 4），因此不查爆炸免疫；只保留 D-38 的防破坏底线。
 */
public class PuffedWheatProjectile extends ThrownCropEntity {
    /**
     * 落点方块的反推步长。基类把命中点沿命中面外法线推出 0.05 格，命中点因此落在命中方块外侧；
     * 沿飞行方向再走这么多格才回到命中方块内部。采样用的 epsilon，不是玩法数值。
     */
    private static final double LANDING_PROBE = 0.1;

    public PuffedWheatProjectile(EntityType<? extends PuffedWheatProjectile> type, Level level) {
        super(type, level);
    }

    public PuffedWheatProjectile(EntityType<? extends PuffedWheatProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.PUFFED_WHEAT;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        int dryEdge = FarmstormConfig.puffedWheatDryEdge.get();
        BlockPos center = landingBlock(pos, directHit == null);
        // DESIGN §4.1：水源只在干态立方体内判定一次，命中即改用湿态边长，不做二次判定。
        int edge = hasWaterSource(level, center, dryEdge) ? FarmstormConfig.puffedWheatWetEdge.get() : dryEdge;
        fillHay(level, center, edge);
    }

    /** DESIGN §3.4：命中方块时落点方块 = 命中面内侧方块；命中实体时退化为命中点所在方块。 */
    private BlockPos landingBlock(Vec3 pos, boolean blockHit) {
        if (!blockHit) {
            return BlockPos.containing(pos);
        }
        Vec3 into = pos.subtract(this.position());
        if (into.lengthSqr() < 1.0E-6) {
            return BlockPos.containing(pos);
        }
        return BlockPos.containing(pos.add(into.normalize().scale(LANDING_PROBE)));
    }

    /** DESIGN §4.1 的水源口径：流体标签 {@code #minecraft:water} 且为水源方块。 */
    private static boolean hasWaterSource(ServerLevel level, BlockPos center, int edge) {
        int radius = edge / 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    FluidState fluid = level.getBlockState(center.offset(dx, dy, dz)).getFluidState();
                    if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 写入的方块，本类的唯一可变点。D-77：压缩膨胀麦只覆写这个方法，
     * 落点反推 / 水源判定 / 写入循环 / 抗性底线全部沿用父类那一份，不存在第二份。
     */
    protected BlockState fillBlock() {
        return Blocks.HAY_BLOCK.defaultBlockState();
    }

    /**
     * 写满边长 edge 的立方体；D-38 / T-05 的防破坏底线（抗性 ≥ 3.6e6）之外的方块一律覆盖。
     *
     * <p>抗性读「方块自身」的爆炸抗性而不是 context 敏感版本：DESIGN §7.11 明确膨胀麦是「写入」而非爆炸，
     * 会覆盖火把花 / 瓶子草这类把 context 抗性报成 3.6e6 的部署方块，只有基岩级方块才该挡住。
     * NeoForge 给无参版本标了 {@code @Deprecated}（指向 context 版本），此处刻意留在内禀口径上。
     */
    @SuppressWarnings("deprecation")
    private void fillHay(ServerLevel level, BlockPos center, int edge) {
        int radius = edge / 2;
        BlockState hay = this.fillBlock();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockState(pos).getBlock().getExplosionResistance() >= BlastRule.UNBREAKABLE_RESISTANCE) {
                        continue;
                    }
                    level.setBlock(pos, hay, Block.UPDATE_ALL);
                }
            }
        }
    }
}
