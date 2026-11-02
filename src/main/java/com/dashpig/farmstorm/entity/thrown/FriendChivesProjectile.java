package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import com.dashpig.farmstorm.util.CropProfile;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
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
 * 朋友的韭的投掷物（TECH §2.7.2 / D-131 ~ D-133）：落地后不是「闪一下」就消失，而是变成
 * {@code friend_chives.effectTicks} 刻的场——每刻重扫落点球体，对球内每个生物施加
 * {@code fieldLength − x} 刻 Q弹（x = 落地后经过的刻数），第 {@code fieldLength} 刻自毁；
 * 命中音仍只在落地那一刻判一次（球内 ≥1 播一次、空投不响、后续进场不补播，D-132）。
 *
 * <p>A-33：场长只有 {@link #fieldLength} 一个来源（落地时读一次 config，默认值见
 * {@code FarmstormConfig}），施加量一律由它派生，本类不再出现任何时长字面量——服主把键改成 100，
 * 场就只跑 100 刻。
 *
 * <p>F-186 / RT-35：场不跨区块卸载。{@code fieldTick} 不写 NBT；读档时若发现基类的 {@code Settled}
 * 已经是 {@code true}，就在第一个 tick 自杀——否则它既不再 tick 场、也不会被基类的寿命检查回收
 * （{@code ThrownCropEntity} 的寿命检查只在 {@code !settled} 时生效），会变成一个永不消失的实体。
 */
public class FriendChivesProjectile extends ThrownCropEntity {
    /**
     * 原版 {@code playSound} 的广播半径 = 16 格 × 音量，因此 32.0 格的 {@code friend_chives.soundRange}
     * 对应音量 2.0（DESIGN §4.6 的「32 格怎么来」）。改 config 就改半径，代码里不出现第二个 32。
     */
    private static final double SOUND_RANGE_PER_VOLUME = 16.0;

    /** 播音音高：文档没有给出可变口径，取原版的 1.0。 */
    private static final float SOUND_PITCH = 1.0F;

    /** 落地后的刻数；-1 = 还没落地。不写 NBT（D-133：区块卸载即结束）。 */
    private int fieldTick = -1;

    /** 读档时基类的 {@code Settled} 已经是 {@code true}（F-186）：下一个 tick 自杀，不跨卸载复活。 */
    private boolean loadedSettled;

    /** 场长（A-33：唯一来源），落地那一刻读一次；此时 {@code fieldTick} 仍是 -1。 */
    private int fieldLength;

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

    /** D-131：落地后不消失，改为持续场。基类的默认仍是 {@code false}，其它投掷物不受影响。 */
    @Override
    protected boolean keepAliveAfterImpact() {
        return true;
    }

    /**
     * 落地那一刻（基类 {@code settle} 保证每次生成恰好调用一次）：读一次场长 → 施加 x = 0 那一批 →
     * 按既有判据播一次音（D-132）→ 把实体钉在落点（D-133 的三件套：置零速度 / 无重力 / 免碰撞）。
     *
     * <p>谓词里只有几何与存活条件：效果端不筛敌我，投掷者本人站进球内同样中招（DESIGN §4.5 / §12.12）。
     */
    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        this.fieldLength = FarmstormConfig.friendChivesEffectTicks.get();
        FieldScan scan = applyField(level, pos, 0);
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "朋友的韭 落点 (%.2f, %.2f, %.2f) 球内 %d 个生物，新施加 %d 个（半径 %.2f 格，第 0 刻施加 %d 刻，amplifier 0）",
                pos.x, pos.y, pos.z, scan.inSphere(), scan.applied(),
                FarmstormConfig.friendChivesHitRadius.get(), this.fieldLength));
        // 判据只有一条：球内 ≥ 1 个生物（D-132），不另做一次「附近有没有生物」的查询（DESIGN §4.6）；
        // 这里数球内生物而不是数新施加数，所以即使命中的生物身上已有 Q弹也照样只响这一声。
        // D-132：这条判据只在落地这一刻跑，所以整场最多播一次，后续进场不补播。
        if (scan.inSphere() >= 1) {
            float volume = (float) (FarmstormConfig.friendChivesSoundRange.get() / SOUND_RANGE_PER_VOLUME);
            level.playSound(null, pos.x, pos.y, pos.z, FarmstormSounds.FRIEND_CHIVES.get(), SoundSource.PLAYERS,
                    volume, SOUND_PITCH);
            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                    "朋友的韭 命中音 farmstorm:friend_chives 播放一次：落点 (%.2f, %.2f, %.2f) 音源 PLAYERS 音量 %.2f（可闻 %.1f 格）",
                    pos.x, pos.y, pos.z, volume, volume * SOUND_RANGE_PER_VOLUME));
        }
        // 三件套分工（TECH §2.7.2 要点 2）：置零速度防被上一刻的动量带走、无重力防缓慢下坠、
        // 免碰撞（Entity 的既有字段）防水流 / 活塞 / 实体推走。
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.fieldTick = 0;
    }

    /**
     * 场由本方法驱动。基类 {@code tick()} 的寿命检查在 settled 之后失效（{@code ThrownCropEntity} 只在
     * {@code !settled} 时 discard），落地后的回收只能靠这里的自毁（RT-35）。
     *
     * <p>顺序即验收点（TECH §2.7.2 / §7 #30）：{@code super.tick()} 之后第一件事是 F-186 的读档自杀，
     * 然后是「还没落地就不管」，再自增刻数；第 {@code fieldLength} 刻自毁（不再施加），否则重扫球体。
     */
    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.loadedSettled) {
            // F-186 / RT-35：这条路径只会在「场被区块卸载写进 NBT、再加载回来」时走到。
            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                    "朋友的韭 场结束：读档 Settled=true，(%s) 的第一个 tick 自毁（场不跨区块卸载，F-186）",
                    this.position()));
            this.discard();
            return;
        }
        if (this.fieldTick < 0) {
            return;
        }
        this.fieldTick++;
        if (this.fieldTick >= this.fieldLength) {
            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                    "朋友的韭 场结束：落点 (%.2f, %.2f, %.2f) 第 %d 刻自毁（场长 %d 刻）",
                    this.getX(), this.getY(), this.getZ(), this.fieldTick, this.fieldLength));
            this.discard();
            return;
        }
        FieldScan scan = applyField(level, this.position(), this.fieldTick);
        // 只在两端的验收点留读数：第 1 刻（首个重扫批）与最后一次施加（施加 1 刻）。每刻都打会让每场的行数等于场长。
        if (this.fieldTick == 1 || this.fieldTick == this.fieldLength - 1) {
            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                    "朋友的韭 第 %d 刻重扫：球内 %d 个生物，新施加 %d 个，施加 %d 刻（amplifier 0）",
                    this.fieldTick, scan.inSphere(), scan.applied(), this.fieldLength - this.fieldTick));
        }
    }

    /**
     * 每刻重扫一次落点球体（TECH §2.7.2 要点 1 + A-33）：施加 {@code fieldLength − elapsed} 刻 Q弹，
     * amplifier 仍是既有字面量 0。施加量比上一刻少 1 刻，于是「第 {@code fieldLength} 刻之后效果自然结束」
     * 与「场结束时效果刚好走完」同时成立，既不续期也不提前结束。
     *
     * <p>返回值只给日志与落地那一刻的音效判据（D-132）用，{@code tick()} 不看它。
     */
    private FieldScan applyField(ServerLevel level, Vec3 center, int elapsed) {
        int ticks = this.fieldLength - elapsed;
        if (ticks <= 0) {
            return new FieldScan(0, 0);
        }
        double radius = FarmstormConfig.friendChivesHitRadius.get();
        int inSphere = 0;
        int applied = 0;
        // AABB 只是粗筛（2r × 2r × 2r 的立方），命中判定必须再用 distanceTo 做欧氏复核（TECH §3.3 的「二次筛」）。
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius),
                candidate -> candidate.isAlive() && center.distanceTo(candidate.position()) <= radius)) {
            inSphere++;
            if (target.addEffect(new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true))) {
                applied++;
            }
        }
        return new FieldScan(inSphere, applied);
    }

    /**
     * 一次球扫的两个读数。{@code inSphere} 是粗筛 + 欧氏复核后的球内生物数（落地那一刻的音效判据用它，D-132）；
     * {@code applied} 是 {@code addEffect} 真的改动了效果状态的个数——已在场者逐刻被刷新时这里是 0，因为原版只在
     * 新时长更长时才更新，于是「在场者身上正好剩 {@code fieldLength − x}」这条幂等性由原版规则保证，不必自己比时长。
     */
    private record FieldScan(int inSphere, int applied) {
    }

    /**
     * F-186：只读基类既有的 {@code Settled} 键（{@code ThrownCropEntity} 的存取范围），不放宽、不自造键。
     * {@code fieldTick} 故意不持久化（D-133：区块卸载即结束），因此本类不重写 {@code addAdditionalSaveData}。
     */
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadedSettled = tag.getBoolean("Settled");
    }
}
