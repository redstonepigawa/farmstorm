package com.dashpig.farmstorm.explosion;

import com.dashpig.farmstorm.util.FarmstormAdvancements;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * 三段式爆炸（TECH §4.9）：破坏 / 封顶伤害 / 表现。
 *
 * <p>为什么必须重写 {@link #finalizeExplosion(boolean)}：原版把粒子、音效、方块破坏写死在一个方法里，
 * 伤害上限没有插入点。重写之后伤害段独立成 {@link #hurtCapped()}，是本需求的最小侵入实现（D-40：无 Mixin）。
 *
 * <p>原版 {@code Explosion#explode()} 仍然执行，但通过 {@link RuleDamageCalculator#shouldDamageEntity} 关掉了它的伤害入口：
 * 伤害改由 {@link #hurtCapped()} 逐实体封顶；击退、{@code hitPlayers} 记账与 {@code onExplosionHit} 仍走原版，因此手感和原版一致。
 */
public class FarmstormExplosion extends Explosion {
    /** {@code dropsBlocks = false} 的规则用：掉落物直接丢弃（TECH §8.2）。 */
    private static final BiConsumer<ItemStack, BlockPos> NO_DROPS = (stack, position) -> stack.setCount(0);

    /** 掉落合并上限，与原版 {@code Explosion#addOrAppendStack} 一致。 */
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;

    /**
     * 取样中心沿 +Y 抬升的最大格数（FIX-8 防御层）。
     * 只用来把埋在实心方块里的中心抬到空位，抬不出去就保持原值——不引入任何常数加权。
     */
    private static final int MAX_SAMPLE_LIFT_BLOCKS = 4;

    /** 抬升正好落在格子边界上时多抬这么一点，保证取样中心严格落在空气格内部。 */
    private static final double SAMPLE_LIFT_EPSILON = 1.0E-3;

    private final ServerLevel serverLevel;
    private final BlastRule rule;
    private final float damageCap;
    private final DamageSource damageSource;

    /**
     * @param level        服务端维度（客户端没有构造入口，§8.4）
     * @param sourceEntity 直接来源实体（用于击杀归属与「不炸自己」）
     * @param damageSource 带 owner 的伤害来源（D-39：{@code farmstorm:crop_blast}）
     * @param rule         方块破坏策略
     * @param damageCap    每实体伤害上限；&lt;= 0 表示纯破坏型，跳过伤害段
     */
    public FarmstormExplosion(ServerLevel level, @Nullable Entity sourceEntity, DamageSource damageSource,
                              double x, double y, double z, float radius, BlastRule rule, float damageCap) {
        super(level, sourceEntity, damageSource, new RuleDamageCalculator(rule),
                x, y, z, radius, false, Explosion.BlockInteraction.DESTROY,
                ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
        this.serverLevel = level;
        this.rule = rule;
        this.damageCap = damageCap;
        this.damageSource = damageSource;
    }

    /**
     * 立即执行整次爆炸：先给 {@code ExplosionEvent.Start} 一次否决机会（领地 / 保护类模组的通用契约），
     * 再走原版 {@code explode()} 与我们的三段式。
     *
     * @return 是否真的爆炸了；被其它模组取消时返回 false，世界与实体都不动
     */
    public boolean detonate() {
        if (EventHooks.onExplosionStart(this.serverLevel, this)) {
            return false;
        }
        this.explode();
        this.finalizeExplosion(true);
        return true;
    }

    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        this.breakBlocks();
        this.hurtCapped();
        this.spawnFx(spawnParticles);
    }

    /** 1) 方块破坏：逐块复判规则（射线里的判定以 {@link RuleDamageCalculator} 为准，这里是双保险）。 */
    private void breakBlocks() {
        if (!this.interactsWithBlocks()) {
            return;
        }
        // DESIGN §7.18：mobGriefing = false 时不破坏方块，但伤害照常。
        // 我们自己构造的爆炸固定是 DESTROY，原版那条「由 Level#explode 按游戏规则选 BlockInteraction」的路径走不到这里。
        if (!this.serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }
        List<BlockPos> toBlow = this.getToBlow();
        if (toBlow.isEmpty()) {
            return;
        }
        Util.shuffle(toBlow, this.serverLevel.random);
        List<Pair<ItemStack, BlockPos>> drops = this.rule.dropsBlocks() ? new ArrayList<>() : null;
        int broken = 0;
        for (BlockPos pos : toBlow) {
            BlockState state = this.serverLevel.getBlockState(pos);
            if (!this.rule.canBreak(this.serverLevel, pos, state, this)) {
                continue;
            }
            broken++;
            if (drops == null) {
                state.onExplosionHit(this.serverLevel, pos, this, NO_DROPS);
            } else {
                state.onExplosionHit(this.serverLevel, pos, this, (stack, at) -> addOrAppendStack(drops, stack, at));
            }
        }
        if (broken > 0 && this.damageSource.getEntity() instanceof ServerPlayer bombOwner) {
            FarmstormAdvancements.award(bombOwner, "demolition");
        }
        if (drops != null) {
            for (Pair<ItemStack, BlockPos> drop : drops) {
                Block.popResource(this.serverLevel, drop.getSecond(), drop.getFirst());
            }
        }
    }

    /**
     * 2) 伤害：原版同款公式逐实体计算后按 damageCap 封顶（TECH §4.9 伤害上限算法）。
     *
     * <p>FIX-6 §5（作者口径）：**不分敌我，也不宽恕使用者**。命中范围里除 {@link Entity#ignoreExplosion} 之外没有
     * 任何过滤——投掷者 / 部署者本人站在爆点旁一样掉血。原先这里有一句 {@code entity == getDirectSourceEntity()}
     * 的自我排除（与原版 {@code Explosion#explode} 里 {@code Level#getEntities(source, ...)} 排除源实体的做法一致），
     * 按作者要求已删除。
     */
    private void hurtCapped() {
        if (this.damageCap <= 0.0F) {
            return;
        }
        Vec3 center = this.center();
        double reach = this.radius() * 2.0;
        if (reach <= 0.0) {
            return;
        }
        AABB box = new AABB(center, center).inflate(reach);
        for (LivingEntity entity : this.serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.ignoreExplosion(this)) {
                continue;
            }
            double distance = entity.position().distanceTo(center);
            if (distance > reach) {
                continue;
            }
            double seen = Explosion.getSeenPercent(center, entity);
            if (seen <= 0.0) {
                // FIX-8 防御层：中心埋在实心方块里时视线采样全被挡掉（seen = 0），
                // 公式只剩保底 +1.0，伤害会退化成 1 点。把取样中心沿 +Y 抬到最近的空位再算一次。
                // 只换取样点，不改距离衰减与封顶。
                Vec3 sample = this.liftedSampleCenter(center);
                if (sample != null) {
                    seen = Explosion.getSeenPercent(sample, entity);
                }
            }
            double impact = (1.0 - distance / reach) * seen;
            double raw = (impact * impact + impact) / 2.0 * 7.0 * reach + 1.0;
            entity.hurt(this.damageSource, (float) Math.min(raw, this.damageCap));
        }
    }

    /**
     * 把视线取样中心从实心方块里抬出来（FIX-8）。
     *
     * @return 抬升到空格后的取样中心；中心本来就在可穿方块里、或头顶连续实心抬不出去时返回 {@code null}
     */
    @Nullable
    private Vec3 liftedSampleCenter(Vec3 center) {
        if (this.passable(BlockPos.containing(center))) {
            return null;
        }
        Vec3 lifted = center;
        for (int step = 0; step < MAX_SAMPLE_LIFT_BLOCKS; step++) {
            BlockPos at = BlockPos.containing(lifted);
            lifted = new Vec3(lifted.x, at.getY() + 1.0 + SAMPLE_LIFT_EPSILON, lifted.z);
            if (this.passable(BlockPos.containing(lifted))) {
                return lifted;
            }
        }
        return null;
    }

    /** 「可穿」＝碰撞箱为空；只看碰撞箱，不看流体与光照这类与视线无关的属性。 */
    private boolean passable(BlockPos pos) {
        return this.serverLevel.getBlockState(pos).getCollisionShape(this.serverLevel, pos).isEmpty();
    }

    /** 3) 表现：粒子与音效全部走原版包（C-6），服务端广播，客户端只播。 */
    private void spawnFx(boolean spawnParticles) {
        if (!spawnParticles) {
            return;
        }
        Vec3 center = this.center();
        ParticleOptions particle = this.radius() < 2.0F
                ? this.getSmallExplosionParticles()
                : this.getLargeExplosionParticles();
        this.serverLevel.sendParticles(particle, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        float pitch = (1.0F + (this.serverLevel.random.nextFloat() - this.serverLevel.random.nextFloat()) * 0.2F) * 0.7F;
        this.serverLevel.playSound(null, center.x, center.y, center.z, this.getExplosionSound().value(),
                SoundSource.BLOCKS, 4.0F, pitch);
    }

    /** 同物品掉落合并到上限，避免一次爆炸刷出过多掉落实体（TECH §8.2）。 */
    private static void addOrAppendStack(List<Pair<ItemStack, BlockPos>> drops, ItemStack stack, BlockPos pos) {
        for (int index = 0; index < drops.size(); index++) {
            Pair<ItemStack, BlockPos> existing = drops.get(index);
            ItemStack merged = existing.getFirst();
            if (ItemEntity.areMergable(merged, stack)) {
                drops.set(index, Pair.of(ItemEntity.merge(merged, stack, MAX_DROPS_PER_COMBINED_STACK), existing.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
        drops.add(Pair.of(stack, pos));
    }

    /** 本次爆炸的方块破坏策略。 */
    public BlastRule rule() {
        return this.rule;
    }

    /** 本次爆炸的每实体伤害上限（&lt;= 0 表示纯破坏型）。 */
    public float damageCap() {
        return this.damageCap;
    }

    /**
     * 把 {@link BlastRule} 接进原版射线（TECH §4.9 的 {@code shouldBlockExplode} 钩子；1.21.1 里这个方法不在
     * {@code Explosion} 上而在 {@code ExplosionDamageCalculator} 上，见回报中的 API 差异记录）。
     *
     * <p>同时关掉原版的伤害入口：伤害段由 {@link #hurtCapped()} 逐实体封顶执行。
     * 击退不受 {@code shouldDamageEntity} 影响，仍由原版 {@code explode()} 施加，因此不需要自己重算一遍。
     */
    private static final class RuleDamageCalculator extends ExplosionDamageCalculator {
        private final BlastRule rule;

        private RuleDamageCalculator(BlastRule rule) {
            this.rule = rule;
        }

        /** 保留方块自身的否决权（其它模组的 {@code shouldBlockExplode}），再叠加本规则。 */
        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
            return super.shouldBlockExplode(explosion, level, pos, state, power)
                    && this.rule.canBreak(level, pos, state, explosion);
        }

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }
    }
}
