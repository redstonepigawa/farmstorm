package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.util.CropProfile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 压缩膨胀麦（D-77 / TECH §3.8）：与膨胀麦**完全同一套**落点语义——落点反推、
 * 水源判定、干 {@code puffed_wheat.dryEdge} / 湿 {@code puffed_wheat.wetEdge}、3.6e6 抗性底线
 * 全部走父类那一份实现，本类只把写入的方块换成压缩干草块。
 *
 * <p>因此本类不重写 {@code onImpact} / {@code fillHay}：D-77 的「完全沿用」是继承字面成立，
 * 不是复制一份可能各自漂移的写入循环。
 */
public class CompressedPuffedWheatProjectile extends PuffedWheatProjectile {
    public CompressedPuffedWheatProjectile(EntityType<? extends CompressedPuffedWheatProjectile> type, Level level) {
        super(type, level);
    }

    public CompressedPuffedWheatProjectile(EntityType<? extends CompressedPuffedWheatProjectile> type,
                                           LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.COMPRESSED_PUFFED_WHEAT;
    }

    /** D-77：唯一差异——父类写入的方块从 {@code minecraft:hay_block} 换成压缩干草块。 */
    @Override
    protected BlockState fillBlock() {
        return FarmstormBlocks.COMPRESSED_HAY_BLOCK.get().defaultBlockState();
    }
}
