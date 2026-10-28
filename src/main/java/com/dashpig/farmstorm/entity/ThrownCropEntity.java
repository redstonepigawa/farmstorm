package com.dashpig.farmstorm.entity;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.util.CropProfile;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 8 个投掷物的共同基类（TECH §4.7）。
 * 生命周期与命中分流全部锁在基类：子类只重写 {@link #onImpact}，需要命中后续命时再重写
 * {@link #keepAliveAfterImpact()}；{@link #onHitBlock} / {@link #onHitEntity} 不得重写（F-32 ②）。
 * 物品与显示名从作物档案取，因此 8 个子类不需要各自写一份注册 ID。
 */
public abstract class ThrownCropEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> DATA_SUBMUNITION =
            SynchedEntityData.defineId(ThrownCropEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean settled;

    protected ThrownCropEntity(EntityType<? extends ThrownCropEntity> type, Level level) {
        super(type, level);
    }

    protected ThrownCropEntity(EntityType<? extends ThrownCropEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    /**
     * 子类的作物档案。必须是静态常量：Entity 构造期间（defineSynchedData）就会调用到它，
     * 那时子类的实例字段还没初始化。
     */
    protected abstract CropProfile profile();

    @Override
    protected Item getDefaultItem() {
        Item item = BuiltInRegistries.ITEM.get(this.profile().id());
        return item == null ? Items.AIR : item;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SUBMUNITION, false);
    }

    /** F-32 ①：寿命上限取 G-6 的 throwable.lifetimeTicks；已进入续命的实体由子类自行收尾。 */
    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel && !this.settled
                && this.tickCount > FarmstormConfig.throwableLifetimeTicks.get()) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel server) {
            Vec3 impact = result.getLocation().add(Vec3.atLowerCornerOf(result.getDirection().getNormal()).scale(0.05));
            this.settle(server, impact, null);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel server) {
            this.settle(server, result.getLocation(), result.getEntity());
        }
    }

    /**
     * 命中的唯一结算点（DESIGN §3.4：每次生成恰好一次）。
     * settled 同时防重入：续命型实体（如爆竹）命中后仍会被 tick 的碰撞检测反复命中，不能重复结算。
     */
    private void settle(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        if (this.settled) {
            return;
        }
        this.settled = true;
        this.onImpact(level, pos, directHit);
        if (!this.keepAliveAfterImpact()) {
            this.discard();
        }
    }

    /**
     * 命中后的唯一行为入口，仅服务端调用。
     * 基类默认不追加效果：投掷物「命中即消失」的生命周期由 onHitBlock / onHitEntity 保证，
     * 8 个子类的爆炸 / 药水云 / 方块替换在 M3 各自重写本方法。
     */
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        // 无额外效果：本方法的重写者是 M3 的 8 个投掷物。
    }

    /** 爆竹重写为 true：命中后原地继续计时撒风弹。 */
    protected boolean keepAliveAfterImpact() {
        return false;
    }

    public void setSubmunition(boolean value) {
        this.getEntityData().set(DATA_SUBMUNITION, value);
    }

    public boolean isSubmunition() {
        return this.getEntityData().get(DATA_SUBMUNITION);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSubmunition(tag.getBoolean("Submunition"));
        this.settled = tag.getBoolean("Settled");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Submunition", this.isSubmunition());
        tag.putBoolean("Settled", this.settled);
    }
}
