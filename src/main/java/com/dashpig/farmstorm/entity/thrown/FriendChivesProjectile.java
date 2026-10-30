package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import com.dashpig.farmstorm.util.CropProfile;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 朋友的韭的投掷物（TECH §3.3）：落点对欧氏 6 格球内的生物施加 200 刻 Q弹，
 * 至少施加成功 1 个生物时在落点播一次命中音（音量 2.0，32 格可闻）；空投不响（D-85）。
 *
 * <p>F-115 落点结算 + F-116 播音都在 {@link #onImpact} 里：基类 {@code settle} 保证每次生成恰好调用一次，
 * 因此「一次投掷只播一次」由生命周期保证，本类不需要额外的播音标记。
 */
public class FriendChivesProjectile extends ThrownCropEntity {
    /**
     * 原版 {@code playSound} 的广播半径 = 16 格 × 音量，因此 32.0 格的 {@code friend_chives.soundRange}
     * 对应音量 2.0（DESIGN §4.6 的「32 格怎么来」）。改 config 就改半径，代码里不出现第二个 32。
     */
    private static final double SOUND_RANGE_PER_VOLUME = 16.0;

    /** 播音音高：文档没有给出可变口径，取原版的 1.0。 */
    private static final float SOUND_PITCH = 1.0F;

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

    /**
     * DESIGN §4.6 的判定顺序（同一刻完成，不排队）：欧氏球筛 → 逐个写 Q弹 → 累计施加成功数 n → n ≥ 1 播一次音。
     * 谓词里只有几何与存活条件：效果端不筛敌我，投掷者本人站进球内同样中招（DESIGN §4.5 / §12.12）。
     */
    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        double radius = FarmstormConfig.friendChivesHitRadius.get();
        int ticks = FarmstormConfig.friendChivesEffectTicks.get();
        int applied = 0;
        // AABB 只是粗筛（2r × 2r × 2r 的立方），命中判定必须再用 distanceTo 做欧氏复核（TECH §3.3 的「二次筛」）。
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(radius),
                candidate -> candidate.isAlive() && pos.distanceTo(candidate.position()) <= radius)) {
            if (target.addEffect(new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true))) {
                applied++;
            }
        }
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "朋友的韭 落点 (%.2f, %.2f, %.2f) 球内施加 %d 个生物（半径 %.2f 格，%d 刻，amplifier 0）",
                pos.x, pos.y, pos.z, applied, radius, ticks));
        // 判据只有一条：n 就是上面的施加成功计数，不另做一次「附近有没有生物」的查询（DESIGN §4.6）。
        if (applied < 1) {
            return;
        }
        float volume = (float) (FarmstormConfig.friendChivesSoundRange.get() / SOUND_RANGE_PER_VOLUME);
        level.playSound(null, pos.x, pos.y, pos.z, FarmstormSounds.FRIEND_CHIVES.get(), SoundSource.PLAYERS,
                volume, SOUND_PITCH);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "朋友的韭 命中音 farmstorm:friend_chives 播放一次：落点 (%.2f, %.2f, %.2f) 音源 PLAYERS 音量 %.2f（可闻 %.1f 格）",
                pos.x, pos.y, pos.z, volume, volume * SOUND_RANGE_PER_VOLUME));
    }
}
