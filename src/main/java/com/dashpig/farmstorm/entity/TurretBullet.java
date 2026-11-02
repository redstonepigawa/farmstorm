package com.dashpig.farmstorm.entity;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * man瓜 炮台发射的导弹（TECH §4.7）。自研实体而不是潜影贝子弹，因为伤害类型与击退都要按本模组口径结算。
 * 追踪只在服务端做（HC-03），客户端靠实体同步位置。
 */
public class TurretBullet extends ThrowableItemProjectile {
    /** D-129 / TECH §2.6.2：命中判定的最小步长。小于它就认为「已到目标中心」，直接结算，不再指望 move() 的扫掠。 */
    private static final double MIN_STEP = 0.05;

    private static final String TAG_TARGET = "Target";

    @Nullable
    private Entity target;

    /**
     * 出膛点（TECH §4.7 的「炮台位置」替身）。炮台方块坐标不在本实体手上——炮台行为只把生成点写进
     * {@code moveTo}，而生成点就落在「炮台 → 目标」射线上——所以用首个 tick 的自身位置代替炮台位置，
     * 击退方向因此与「南瓜 → 目标」一致，与部署者站在哪里无关。读档后由首个 tick 重新取样。
     */
    @Nullable
    private Vec3 launchOrigin;

    /** D-129：结算体可能被「贴脸直判」与 move() 的碰撞回调各走一次，用这个闸挡住第二次。 */
    private boolean hitResolved;

    public TurretBullet(EntityType<? extends TurretBullet> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public TurretBullet(EntityType<? extends TurretBullet> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
        this.setNoGravity(true);
    }

    /**
     * 导弹没有对应物品（TECH §3.4），但 {@link ThrowableItemProjectile#addAdditionalSaveData} 会无条件把物品堆栈
     * 写进 NBT，而 {@link ItemStack#save} 对空堆栈直接抛 {@code IllegalStateException}——返回 {@code Items.AIR}
     * 会让每次存档刷 ERROR 且导弹无法持久化。这里给一个真实物品作为占位：渲染走 {@code ShulkerBulletModel} 的
     * 模型与贴图（与潜影贝同源）不读物品栈，导弹也不可拾取，因此该堆栈不会出现在任何玩家手里。
     */
    @Override
    protected Item getDefaultItem() {
        return Items.SHULKER_SHELL;
    }

    public void setTarget(@Nullable Entity target) {
        this.target = target;
    }

    @Nullable
    public Entity target() {
        return this.target;
    }

    @Override
    public void tick() {
        if (this.launchOrigin == null) {
            this.launchOrigin = this.position();
        }
        if (this.level() instanceof ServerLevel) {
            if (this.target != null && !this.target.isAlive()) {
                this.target = null;
            }
            if (this.target != null) {
                this.steerTowards(this.target);
            }
        }
        super.tick();
    }

    /**
     * 每 tick 朝目标重算速度（D-129 / TECH §2.6.2）：正常距离仍交给原版 move 的扫掠做命中判定，本类不做射线检测；
     * 只有「一个步长以内」这一档特殊处理，用来消掉贴脸时每刻掉头造成的绕圈 / 抖动。
     */
    private void steerTowards(Entity aim) {
        double step = FarmstormConfig.pumpkinTurretBulletSpeed.get();
        Vec3 delta = aim.getBoundingBox().getCenter().subtract(this.position());
        double dist = delta.length();
        if (dist <= MIN_STEP) {
            // 位移已接近 0：move() 的扫掠体退化成一点，命中与否不可依赖，直接判中。
            this.resolveHit(aim);
            return;
        }
        if (dist <= step) {
            // 一步走完剩余距离：位移正好等于 dist，不越过、不掉头。
            this.setDeltaMovement(delta);
            return;
        }
        this.setDeltaMovement(delta.normalize().scale(step));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }
        // 撞到实体与贴脸直判共用同一条结算体；discard() 之后 move() 仍可能再回调一次，由 hitResolved 挡住。
        this.resolveHit(result.getEntity());
    }

    /** 结算入口（D-129）：同一发只走一次 applyHit，然后消失。 */
    private void resolveHit(Entity hit) {
        if (this.hitResolved) {
            return;
        }
        this.hitResolved = true;
        this.applyHit(hit);
        this.discard();
    }

    /**
     * 命中结算体（TECH §2.6.2：从原 onHitEntity 原样搬家——伤害类型 {@code farmstorm:void_bolt}、
     * 进度 {@code turret_hit}、击退算式一个字符不改；{@code discard()} 移到 {@link #resolveHit(Entity)}）。
     */
    private void applyHit(Entity hit) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        Entity owner = this.getOwner();
        hit.hurt(FarmstormDamageTypes.voidBolt(server, owner), FarmstormConfig.pumpkinTurretProjectileDamage.get());
        if (hit instanceof ServerPlayer serverPlayer) {
            FarmstormAdvancements.award(serverPlayer, "turret_hit");
        }
        if (hit instanceof LivingEntity living) {
            Vec3 origin = this.launchOrigin != null ? this.launchOrigin : this.position();
            Vec3 knockback = living.position().subtract(origin).multiply(1.0, 0.0, 1.0);
            if (knockback.lengthSqr() > 1.0E-4) {
                Vec3 direction = knockback.normalize();
                living.knockback(FarmstormConfig.pumpkinTurretKnockbackLevel.get(), -direction.x, -direction.z);
            }
        }
    }

    /** 撞方块即消失，不炸方块（TECH §4.7）。 */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel) {
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.target != null) {
            tag.putInt(TAG_TARGET, this.target.getId());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // 目标实体不跨存档保留：读档后由炮台行为重新索敌，避免持有失效引用。
        this.target = null;
    }
}
