package com.dashpig.farmstorm.explosion;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.FarmstormTags;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 爆炸的方块破坏策略（TECH §4.9）：可破坏谓词 + 抗性阈值 + 掉落策略。
 * 伤害上限不在这里——那是 {@link FarmstormExplosion} 的 damageCap。
 *
 * @param extraBreakable 额外谓词：返回 false 表示无论抗性如何都不得破坏（默认排除 {@code #farmstorm:explosion_immune}）
 * @param maxResistance  抗性阈值：爆炸抗性 &gt;= 该值不破（D-19）
 * @param dropsBlocks    破坏时是否产生掉落（TECH §8.2：作物方块用 false，避免一次爆炸刷出成百个掉落实体）
 */
public record BlastRule(Predicate<BlockState> extraBreakable, float maxResistance, boolean dropsBlocks) {

    /**
     * D-38 / T-05 的防破坏底线：爆炸抗性 &gt;= 3.6e6 的方块（基岩 / 屏障 / 命令方块 / 结构方块 / 末地传送门框架）
     * 任何规则都不破。这是底线而不是平衡数值，因此不进 config（D-38），也不受阈值配置影响。
     */
    public static final float UNBREAKABLE_RESISTANCE = 3.6e6F;

    /** 原版口径：只受原版抗性累计约束，正常掉落（TECH §4.9 预置常量 1）。 */
    public static final BlastRule VANILLA = new BlastRule(BlastRule::breakable, UNBREAKABLE_RESISTANCE, true);

    /**
     * 与 {@link #VANILLA} 数值相同（TECH §4.9 预置常量 2）。
     * 语义是「显式声明跳过硬抗性方块」，留给行为层在与原版无关的场合自我说明，避免读代码的人以为抗性阈值没配。
     */
    public static final BlastRule UNBREAKABLE_SKIP = new BlastRule(BlastRule::breakable, UNBREAKABLE_RESISTANCE, true);

    /** C-2 / D-19：爆炸抗性 &gt;= 圆石（6.0）不破（TECH §4.9 预置常量 3）。 */
    public static final BlastRule COBBLESTONE_6 = new BlastRule(BlastRule::breakable, 6.0F, true);

    /**
     * 非破障类专用：谓词恒 false，任何方块都不破坏，因此也不产生任何掉落。
     * 哪些作物进这条规则由 {@link #forCrop} 按 {@link com.dashpig.farmstorm.util.CropCategory#DEMOLITION} 标签决定。
     */
    public static final BlastRule NEVER_BREAK = new BlastRule(state -> false, 0.0F, false);

    /**
     * 瓜片子（作者定为破障类）：阈值取 {@code explosion.sliceResistThreshold} 快照（C-2 / D-19）。
     * 它的子母弹（瓜！的 4 片）也是这个类，自动跟随。
     */
    public static BlastRule sliceBomb() {
        return new BlastRule(BlastRule::breakable, (float) FarmstormConfig.sliceResistThreshold(), true);
    }

    /** 瓜！：阈值取 {@code explosion.grenadeResistThreshold} 快照（D-19）。 */
    public static BlastRule grenade() {
        return new BlastRule(BlastRule::breakable, (float) FarmstormConfig.grenadeResistThreshold(), true);
    }

    /**
     * 通用工厂：行为层不需要自己拼谓词，否则很容易忘记排除爆炸免疫方块（D-14）。
     *
     * @param maxResistance 抗性阈值
     * @param dropsBlocks   是否掉落
     */
    public static BlastRule of(float maxResistance, boolean dropsBlocks) {
        return new BlastRule(BlastRule::breakable, maxResistance, dropsBlocks);
    }

    /**
     * 由作物档案的类别标签决定用哪条规则：没有 {@code DEMOLITION} 标签 → {@link #NEVER_BREAK}，有则用调用点给的规则。
     *
     * <p>这是「哪类作物能破方块」的唯一判据：DEC §4 的类别列改了，行为跟着变，爆炸调用点一个字都不用动。
     * 抗性阈值仍由调用点决定（瓜片子 / 瓜！各有自己的 config 阈值），本工厂只负责开关。
     *
     * @param profile       爆炸来源作物的档案
     * @param whenDemolition 破障类用的破坏规则
     */
    public static BlastRule forCrop(CropProfile profile, BlastRule whenDemolition) {
        return profile.canBreakBlocks() ? whenDemolition : NEVER_BREAK;
    }

    /** 同规则但破坏后不掉落（作物方块用，TECH §8.2）。 */
    public BlastRule withoutDrops() {
        return new BlastRule(this.extraBreakable, this.maxResistance, false);
    }

    /**
     * 方块是否可被这次爆炸破坏。
     * 判定顺序即优先级（DESIGN §7.11）：爆炸免疫 &gt; 整合包豁免 &gt; 防破坏底线 &gt; 抗性阈值。
     */
    public boolean canBreak(BlockGetter level, BlockPos pos, BlockState state, Explosion explosion) {
        if (!this.extraBreakable.test(state)) {
            return false;
        }
        if (state.is(FarmstormTags.BLAST_PROOF)) {
            return false;
        }
        float resistance = state.getExplosionResistance(level, pos, explosion);
        return resistance < UNBREAKABLE_RESISTANCE && resistance < this.maxResistance;
    }

    /** 默认谓词：排除 {@code #farmstorm:explosion_immune}（D-14「任何爆炸都无法破坏」）。 */
    private static boolean breakable(BlockState state) {
        return !state.is(FarmstormTags.EXPLOSION_IMMUNE);
    }
}
