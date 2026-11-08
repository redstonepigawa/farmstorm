package com.dashpig.farmstorm.entity.thrown;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.entity.ThrownCropEntity;
import com.dashpig.farmstorm.explosion.BlastRule;
import com.dashpig.farmstorm.explosion.FarmstormExplosion;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.FarmstormAdvancements;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 莫洛托夫（D-58 / TECH §2.2 #8）：落地时在落点结算一次作物爆炸，并在落点球内逐格掷火 / 岩浆块（F-70）。
 *
 * <p>这次爆炸只当「落点触发器」，三个口径都是固定的：威力读 {@code molotov.triggerExplosionPower}
 * （默认 0）、方块破坏恒 {@link BlastRule#NEVER_BREAK}（裁决 A-10：任何威力都不破坏方块）、
 * 伤害上限 0（D-58：0 伤害）。命中结算仍走基类 {@code settle} 的「每次生成恰好一次」。
 *
 * <p>洒火是「追加」不是「破坏」（D-58 / DESIGN §5.10）：只往「候选方块正上方那一格空气」写方块，
 * 候选方块自身一个字节都不动、不掉落，因此不触碰「破坏权限只由 {@code DEMOLITION} 决定」这条铁律。
 */
public class MolotovProjectile extends ThrownCropEntity {
    /**
     * 落点方块的反推步长，与 {@link PuffedWheatProjectile} 同一口径（TECH §3.8 已说明这是有意的重复）：
     * 基类把命中点沿命中面外法线推出 0.05 格，命中点因此落在命中方块外侧；沿飞行方向再走这么多格才回到命中方块内部。
     * 采样用的 epsilon，不是玩法数值。
     */
    private static final double LANDING_PROBE = 0.1;

    /**
     * 写入用的 block update flag：{@link Block#UPDATE_CLIENTS}（只同步客户端、不通知邻居）。
     *
     * <p>本方法只往空气位写、不动任何原方块，没有任何相邻方块需要因这次写入重算存活 / 流动 / 掉落；
     * 而一次投掷是「几百格各写一次」的批量写入，逐格 {@link Block#UPDATE_ALL} 会把这些写入放大成
     * 数倍于自身的邻居更新，岩浆块还是实心方块，通知邻居就会拉起红石 / 流体 / 重力方块的连锁。
     * 客户端同步仍然要，否则火与岩浆块要等到下一次区块刷新才可见。
     */
    private static final int WRITE_FLAGS = Block.UPDATE_CLIENTS;

    public MolotovProjectile(EntityType<? extends MolotovProjectile> type, Level level) {
        super(type, level);
    }

    public MolotovProjectile(EntityType<? extends MolotovProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    /**
     * D-63 / DESIGN §9.1：**投出即判定**（不要求命中）——实体真正进入世界时判一次，授予投掷者。
     * 用 {@code onAddedToWorld}（1.20.1 Forge 的「实体进入世界」钩子，对应 1.21.1 的 {@code onAddedToLevel}）
     * 而不是构造器：{@code /summon} 走两参构造、没有归属者，天然不会推进；
     * 而投掷路径在 {@code addFreshEntity} 之前就 {@code setOwner} 过了（{@code ThrowableCropItem#createProjectile}）。
     */
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide() && this.getOwner() instanceof ServerPlayer thrower) {
            FarmstormAdvancements.award(thrower, "first_molotov");
        }
    }

    @Override
    protected CropProfile profile() {
        return CropProfile.MOLOTOV;
    }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        this.trigger(level, pos);
        this.scatter(level, this.landingBlock(pos, directHit == null));
    }

    /**
     * D-58 的落点爆炸：威力是唯一可变项，破坏与伤害两个入口按设计关死（A-10 明确「破坏权限与威力无关」，
     * 所以这里没有 {@code BlastRule.forCrop} 分支，只有 {@link BlastRule#NEVER_BREAK} 一个常量）。
     */
    private void trigger(ServerLevel level, Vec3 pos) {
        int power = FarmstormConfig.molotovTriggerExplosionPower.get();
        new FarmstormExplosion(level, this.getOwner(), FarmstormDamageTypes.cropBlast(level, this.getOwner()),
                pos.x, pos.y, pos.z, power, BlastRule.NEVER_BREAK, 0.0F).detonate();
        Farmstorm.LOGGER.info(String.format(Locale.ROOT, "莫洛托夫 落点 (%.2f, %.2f, %.2f) 威力 %d",
                pos.x, pos.y, pos.z, power));
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

    /**
     * DESIGN §5.6–5.9 / TECH §3.7：球内枚举候选、逐格独立掷一次，火写 85 %、岩浆块写余下的 15 %
     * （A-01：只读 {@code molotov.fireChance} 一个键，岩浆块是它的余量，没有第二个概率键）。
     *
     * <p>两条容易写错的判据：候选格必须**自身实心**（{@code isSolidRender}）**且紧邻上方是空气**——
     * 台阶 / 雪层这类非实心方块上方不放置，上方是水 / 雪层也不算空气（§5.7）；写入位置是**候选格正上方那一格**。
     *
     * <p>候选先整批裁完再写，不边裁边写：写入会把空气位变成实心方块（岩浆块），
     * 边裁边写会让同一发里的后几格读到被自己改过的世界，凭空多出一圈「叠上去」的候选。
     *
     * <p><b>生物身边的格子只放火、不放岩浆块</b>（{@link #blockedByEntity}）。岩浆块是**实心**方块，写进生物
     * 所占的格子、或紧贴它身体的那几格，等于把它**卡死在方块里**：碰撞解算拒绝一切位移，但它自己的 AI 照常跑
     * —— 目标选择、转视角、攻击都在动，就是位置不变，空中命中时还看着像悬空（真机实测就是这个症状）。
     * 火没有碰撞箱，烧得到它但卡不住它，所以这类格子一律降级成火。
     */
    private void scatter(ServerLevel level, BlockPos center) {
        double radius = FarmstormConfig.molotovEffectRadius.get();
        double radiusSqr = radius * radius;
        int reach = (int) Math.ceil(radius);
        // 球外再留 1 格余量：只取一次实体列表，逐候选做 AABB 相交判断，避免每格都查一遍世界。
        List<Entity> occupants = level.getEntities((Entity) null,
                new AABB(center).inflate(radius + 1.0), Entity::isAlive);
        List<BlockPos> candidates = new ArrayList<>();
        int skippedOccupied = 0;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSqr) {
                        continue;
                    }
                    BlockPos cell = center.offset(dx, dy, dz);
                    if (!level.getBlockState(cell).isSolidRender(level, cell)) {
                        continue;
                    }
                    if (!level.getBlockState(cell.above()).isAir()) {
                        continue;
                    }
                    candidates.add(cell);
                }
            }
        }
        double fireChance = FarmstormConfig.fireChance();
        RandomSource random = level.getRandom();
        int fire = 0;
        int magma = 0;
        int downgraded = 0;
        for (BlockPos cell : candidates) {
            boolean isFire = random.nextFloat() * 100.0F < fireChance;
            if (!isFire && blockedByEntity(occupants, cell.above())) {
                isFire = true;              // 实心块会卡住生物 → 降级成火
                downgraded++;
            }
            BlockState state = isFire ? Blocks.FIRE.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState();
            level.setBlock(cell.above(), state, WRITE_FLAGS);
            if (isFire) {
                fire++;
            } else {
                magma++;
            }
        }
        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
                "莫洛托夫 候选 %d 火 %d 岩浆 %d 因生物降级 %d 场内实体 %d",
                candidates.size(), fire, magma, downgraded, occupants.size()));
    }

    /**
     * 该格是否贴着某个存活生物（会把实心方块写进它身体里、或紧贴它围住它）。
     *
     * <p>碰撞箱按 0.5 格横向、0.25 格纵向外扩再判相交：严格相交只能挡住「整格塞进身体」，
     * 挡不住「紧贴身体围一圈」，而后者同样会让生物一步都走不了。
     */
    private static boolean blockedByEntity(List<Entity> entities, BlockPos pos) {
        if (entities.isEmpty()) {
            return false;
        }
        AABB box = new AABB(pos);
        for (Entity entity : entities) {
            if (entity.getBoundingBox().inflate(0.5D, 0.25D, 0.5D).intersects(box)) {
                return true;
            }
        }
        return false;
    }
}
