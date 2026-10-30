package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.util.CropProfile;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 朋友的韭的投掷物（TECH §3.3）：落点对欧氏 6 格球内的生物施加 200 刻 Q弹，至少命中 1 个生物时在落点播一次命中音。
 *
 * <p>F-103 只立编译期骨架：{@code onImpact} 为空，因此落地不施加效果也不播音；落点结算由 F-115 填入。
 */
public class FriendChivesProjectile extends ThrownCropEntity {

    public FriendChivesProjectile(EntityType<? extends FriendChivesProjectile> type, Level level) {
        super(type, level);
    }

    public FriendChivesProjectile(EntityType<? extends FriendChivesProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.FRIEND_CHIVES;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        // 骨架：F-115 填落点结算（球内生物施加 Q弹 + 命中音）
    }
}
