package com.dashpig.farmstorm.explosion;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import com.dashpig.farmstorm.util.Hostility;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * 三段式爆炸（TECH §4.9）：破坏 / 封顶伤害 / 表现。
 *
 * <p>1.20.1 的 {@link Explosion} 把「射线收方块 + 逐实体伤害 + 击退」全写死在 {@link #explode()}，
 * 把「破坏方块 + 掉落 + 粒子 + 音效」写死在 {@link #finalizeExplosion(boolean)}，而且
 * {@link ExplosionDamageCalculator} 只有 {@code getBlockExplosionResistance} / {@code shouldBlockExplode}
 * 两个钩子（1.21.1 的 {@code shouldDamageEntity} 在这个版本不存在）。因此本版用两个重写点实现对等替换：
 * <ul>
 *   <li>{@link #explode()}：逐字复刻原版射线与击退，<b>去掉其中的 {@code entity.hurt}</b>——
 *       伤害改由 {@link #hurtCapped()} 逐实体封顶；</li>
 *   <li>{@link #finalizeExplosion(boolean)}：换成「我方破坏方块 → 封顶伤害 → 表现」的固定顺序。</li>
 * </ul>
 * 调用时序与 {@code Level#explode} 一致（{@code explode()} 之后紧跟 {@code finalizeExplosion(true)}，
 * 原版的 {@code explode()} 内部不会自己调 {@code finalizeExplosion}），所以 {@link #detonate()} 的两行不会重复执行。
 *
 * <p>与 1.21.1 版本的可观察行为逐项对齐：伤害半径 {@code radius * 2}、公式
 * {@code (impact² + impact) / 2 * 7 * reach + 1}、封顶、{@code ignoreExplosion} 过滤、方块破坏口径、
 * 击退（含爆炸保护附魔的衰减）、{@code ExplosionEvent.Detonate} 的拦截时机都保持不变。
 */
public class FarmstormExplosion extends Explosion {
    /** 掉落合并上限，与原版 {@code Explosion#addBlockDrops} 一致。 */
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
    /** 1.20.1 的 {@code Explosion} 没有 {@code radius()}；射线与伤害半径都由这个字段复现。 */
    private final float radius;
    /** 同一个计算器实例既进 {@code super} 的射线又供本方复判，规则只有一份。 */
    private final RuleDamageCalculator calculator;
    /** F-259：这条爆炸是否由陷阱自己结算（只有 {@code blast_carrot} 传 {@code true}）。 */
    private final boolean trapSourced;

    /**
     * 非陷阱链路（6 处投掷物 / 幸运浆果）走这条：等价于 {@code trapSourced = false}，
     * 实参表停在 {@code damageCap}（42 号 §2.5.2 的「6 处非陷阱零改动」）。
     */
    public FarmstormExplosion(ServerLevel level, @Nullable Entity sourceEntity, DamageSource damageSource,
                              double x, double y, double z, float radius, BlastRule rule, float damageCap) {
        this(level, sourceEntity, damageSource, x, y, z, radius, rule, damageCap, false);
    }

    /**
     * @param level        服务端维度（客户端没有构造入口，§8.4）
     * @param sourceEntity 直接来源实体（用于击杀归属与「不炸自己」）
     * @param damageSource 带 owner 的伤害来源（D-39：{@code farmstorm:crop_blast}）
     * @param rule         方块破坏策略
     * @param damageCap    每实体伤害上限；&lt;= 0 表示纯破坏型，跳过伤害段
     * @param trapSourced  是否陷阱自己的那一次爆炸（F-259 / A-72）：{@code true} 且目标敌对时才乘倍率
     */
    public FarmstormExplosion(ServerLevel level, @Nullable Entity sourceEntity, DamageSource damageSource,
                              double x, double y, double z, float radius, BlastRule rule, float damageCap,
                              boolean trapSourced) {
        this(level, sourceEntity, damageSource, x, y, z, radius, rule, damageCap, trapSourced,
                new RuleDamageCalculator(rule));
    }

    private FarmstormExplosion(ServerLevel level, @Nullable Entity sourceEntity, DamageSource damageSource,
                               double x, double y, double z, float radius, BlastRule rule, float damageCap,
                               boolean trapSourced, RuleDamageCalculator calculator) {
        super(level, sourceEntity, damageSource, calculator, x, y, z, radius, false, Explosion.BlockInteraction.DESTROY);
        this.serverLevel = level;
        this.rule = rule;
        this.damageCap = damageCap;
        this.damageSource = damageSource;
        this.radius = radius;
        this.calculator = calculator;
        this.trapSourced = trapSourced;
    }

    /**
     * 立即执行整次爆炸：先给 {@code ExplosionEvent.Start} 一次否决机会（领地 / 保护类模组的通用契约），
     * 再走本类 {@link #explode()} 与三段式 {@link #finalizeExplosion(boolean)}（与 {@code Level#explode} 同序）。
     *
     * @return 是否真的爆炸了；被其它模组取消时返回 false，世界与实体都不动
     */
    public boolean detonate() {
        if (ForgeEventFactory.onExplosionStart(this.serverLevel, this)) {
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

    /**
     * 原版 {@code Explosion#explode()} 的对等复刻，唯一差别是删掉 {@code entity.hurt(...)}：
     * 伤害由 {@link #hurtCapped()} 逐实体封顶执行（1.20.1 没有 {@code shouldDamageEntity} 钩子，
     * 想要「原版射线 + 原版击退 + 我方伤害」就只能把这一段拿过来）。
     *
     * <p>逐字保留的部分：16³ 边界射线的采样顺序与 {@code level.random} 消耗、每个方块一次
     * {@code getBlockExplosionResistance} / {@code shouldBlockExplode}（本类规则由此生效）、
     * {@code ExplosionEvent.Detonate} 的触发时机与实体表、以及击退（含爆炸保护附魔的
     * {@code getExplosionKnockbackAfterDampener} 与 {@code hitPlayers} 记账）。
     */
    @Override
    public void explode() {
        this.serverLevel.gameEvent(this.getDirectSourceEntity(), GameEvent.EXPLODE, this.getPosition());
        Vec3 center = this.getPosition();
        Set<BlockPos> set = Sets.newHashSet();
        for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
                for (int l = 0; l < 16; l++) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.radius * (0.7F + this.serverLevel.random.nextFloat() * 0.6F);
                        double d4 = center.x;
                        double d6 = center.y;
                        double d8 = center.z;
                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                            if (!this.serverLevel.isInWorldBounds(blockpos)) {
                                break;
                            }
                            BlockState blockstate = this.serverLevel.getBlockState(blockpos);
                            FluidState fluidstate = this.serverLevel.getFluidState(blockpos);
                            Optional<Float> optional = this.calculator.getBlockExplosionResistance(
                                    this, this.serverLevel, blockpos, blockstate, fluidstate);
                            if (optional.isPresent()) {
                                f -= (optional.get() + 0.3F) * 0.3F;
                            }
                            if (f > 0.0F && this.calculator.shouldBlockExplode(
                                    this, this.serverLevel, blockpos, blockstate, f)) {
                                set.add(blockpos);
                            }
                            d4 += d0 * 0.3F;
                            d6 += d1 * 0.3F;
                            d8 += d2 * 0.3F;
                        }
                    }
                }
            }
        }

        this.getToBlow().addAll(set);
        float reach = this.radius * 2.0F;
        int minX = Mth.floor(center.x - (double) reach - 1.0);
        int maxX = Mth.floor(center.x + (double) reach + 1.0);
        int minY = Mth.floor(center.y - (double) reach - 1.0);
        int maxY = Mth.floor(center.y + (double) reach + 1.0);
        int minZ = Mth.floor(center.z - (double) reach - 1.0);
        int maxZ = Mth.floor(center.z + (double) reach + 1.0);
        List<Entity> entities = this.serverLevel.getEntities(this.getDirectSourceEntity(),
                new AABB(minX, minY, minZ, maxX, maxY, maxZ));
        ForgeEventFactory.onExplosionDetonate(this.serverLevel, this, entities, (double) reach);
        // **半径为 0 时直接不进击退循环**：莫洛托夫的 triggerExplosionPower 默认就是 0（这个爆炸只当落点触发器），
        // 此时下面 distance = sqrt(dist) / reach 会退化成「0/0 = NaN」（中心正好落在生物身上）或「正数/0 = 无穷」。
        // NaN 不被 distance > 1.0 拦住，会一路传到 impact/dampened/knockback，最后
        // setDeltaMovement(原值 + NaN) 把速度写成 NaN —— 实体的碰撞与位置从此再也解算不出来：
        // AI 照常跑（会转视角、会攻击），但一步都动不了，空中命中时还看着像悬空（真机实测）。
        if (reach <= 0.0F) {
            return;
        }
        for (Entity entity : entities) {
            if (entity.ignoreExplosion()) {
                continue;
            }
            double distance = Math.sqrt(entity.distanceToSqr(center)) / (double) reach;
            if (distance > 1.0) {
                continue;
            }
            double dx = entity.getX() - center.x;
            double dy = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - center.y;
            double dz = entity.getZ() - center.z;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length == 0.0) {
                continue;
            }
            dx /= length;
            dy /= length;
            dz /= length;
            double seen = (double) getSeenPercent(center, entity);
            double impact = (1.0 - distance) * seen;
            // 原版在这里直接 entity.hurt(...)；本模组改成在 hurtCapped() 里按 damageCap 封顶后再施加。
            double dampened = entity instanceof LivingEntity livingEntity
                    ? ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, impact)
                    : impact;
            dx *= dampened;
            dy *= dampened;
            dz *= dampened;
            Vec3 knockback = new Vec3(dx, dy, dz);
            // 兜底：宁可这次不击退，也绝不把 NaN 写进实体速度（写进去就是永久卡死，无法自愈）。
            if (!isFinite(knockback) || !isFinite(entity.getDeltaMovement())) {
                Farmstorm.LOGGER.warn("爆炸击退计算出非有限值，已跳过 实体={} 击退={}", entity.getUUID(), knockback);
                continue;
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            if (entity instanceof Player player) {
                if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                    this.getHitPlayers().put(player, knockback);
                }
            }
        }
    }

    /**
     * 1) 方块破坏：逐块复判规则（射线里的判定以 {@link RuleDamageCalculator} 为准，这里是双保险）。
     */
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
        shuffle(toBlow, this.serverLevel.random);
        List<Pair<ItemStack, BlockPos>> drops = this.rule.dropsBlocks() ? new ArrayList<>() : null;
        // 原版 finalizeExplosion 的同一判据：击杀归属者是玩家时，掉落表按「玩家破坏」结算。
        boolean playerCause = this.getIndirectSourceEntity() instanceof Player;
        int broken = 0;
        for (BlockPos pos : toBlow) {
            BlockState state = this.serverLevel.getBlockState(pos);
            if (!this.rule.canBreak(this.serverLevel, pos, state, this)) {
                continue;
            }
            broken++;
            // 1.20.1 没有 BlockState#onExplosionHit：掉落自己取（口径照原版 finalizeExplosion），
            // 方块移除与 wasExploded 交给 onBlockExploded。
            if (drops != null && state.canDropFromExplosion(this.serverLevel, pos, this)) {
                BlockEntity blockEntity = state.hasBlockEntity() ? this.serverLevel.getBlockEntity(pos) : null;
                LootParams.Builder builder = new LootParams.Builder(this.serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, this.getDirectSourceEntity());
                BlockPos immutablePos = pos.immutable();
                state.spawnAfterBreak(this.serverLevel, pos, ItemStack.EMPTY, playerCause);
                for (ItemStack drop : state.getDrops(builder)) {
                    addOrAppendStack(drops, drop, immutablePos);
                }
            }
            state.onBlockExploded(this.serverLevel, pos, this);
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
     * <p>FIX-6 §5（作者口径）：**不分敌我，也不宽恕使用者**。命中范围里除 {@link Entity#ignoreExplosion()} 之外没有
     * 任何过滤——投掷者 / 部署者本人站在爆点旁一样掉血。原先这里有一句 {@code entity == getDirectSourceEntity()}
     * 的自我排除（与原版 {@code Explosion#explode} 里 {@code Level#getEntities(source, ...)} 排除源实体的做法一致），
     * 按作者要求已删除。
     */
    /** 击退向量三分量是否都是有限数（防 NaN / Infinity 污染实体速度）。 */
    private static boolean isFinite(Vec3 vec) {
        return Double.isFinite(vec.x) && Double.isFinite(vec.y) && Double.isFinite(vec.z);
    }

    private void hurtCapped() {
        if (this.damageCap <= 0.0F) {
            return;
        }
        Vec3 center = this.getPosition();
        double reach = this.radius * 2.0;
        if (reach <= 0.0) {
            return;
        }
        AABB box = new AABB(center, center).inflate(reach);
        for (LivingEntity entity : this.serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity.ignoreExplosion()) {
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
            float dealt = (float) Math.min(raw, this.damageCap);
            // F-259 乘点①（42 号 §2.5.3）：乘的是**封顶后的最终伤害**，不是 damageCap——raw < cap 时乘 cap 完全无效。
            // 只有陷阱链路（blast_carrot）且目标敌对时才乘；6 处非陷阱爆炸走 8 参重载 ⇒ trapSourced = false ⇒ 不进这一段。
            if (this.trapSourced && Hostility.isHostile(entity)) {
                dealt *= FarmstormConfig.trapDamageMultiplier.get().floatValue();
            }
            entity.hurt(this.damageSource, dealt);
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
        Vec3 center = this.getPosition();
        ParticleOptions particle = this.radius < 2.0F
                ? ParticleTypes.EXPLOSION
                : ParticleTypes.EXPLOSION_EMITTER;
        this.serverLevel.sendParticles(particle, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        float pitch = (1.0F + (this.serverLevel.random.nextFloat() - this.serverLevel.random.nextFloat()) * 0.2F) * 0.7F;
        this.serverLevel.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE,
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

    /**
     * 1.20.1 的 {@code Util#shuffle} 只收 {@code ObjectArrayList}；这里按同一算法（逆序 Fisher-Yates，
     * {@code random.nextInt(i)} 的调用次数与顺序都相同）洗 {@code List}，随机流与原版一致。
     */
    private static <T> void shuffle(List<T> list, RandomSource random) {
        for (int i = list.size(); i > 1; i--) {
            int j = random.nextInt(i);
            T swap = list.get(i - 1);
            list.set(i - 1, list.get(j));
            list.set(j, swap);
        }
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
     * 把 {@link BlastRule} 接进原版射线（TECH §4.9 的 {@code shouldBlockExplode} 钩子）。
     *
     * <p>1.20.1 的 {@link ExplosionDamageCalculator} 只有阻力与「该方块炸不炸」两个钩子，
     * 没有 1.21.1 的 {@code shouldDamageEntity}——「不给原版伤害」这件事因此由 {@link #explode()} 的
     * 复刻段承担（那一段里没有 {@code entity.hurt}），本类退回到只表达方块规则。
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
    }
}
