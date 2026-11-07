package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.config.FarmstormConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * F-79 的冲刺位移数学（D-65）：纯函数，不碰任何客户端类，服务端探针可以直接喂坐标断言。
 * <p>客户端的 {@code client/WildHunterDashClient} 与服务端复核共用这里的落点规则，位移数学只有一份。
 */
public final class WildHunterDash {
    /** 落点与目标保持的水平距离：够得着（攻击距离 3.0）又不与目标重叠。 */
    private static final double STOP_GAP = 1.2;
    /** 路径采样步长（格）：越小越贴墙。 */
    private static final double SAMPLE_STEP = 0.25;
    /** 脚 / 头两个采样高度（格，相对脚底）。 */
    private static final double FEET_OFFSET = 0.1;
    private static final double HEAD_OFFSET = 1.1;

    private WildHunterDash() {}

    /**
     * 计划一次吸附：从玩家当前位置到「贴着目标、留出 {@link #STOP_GAP}」的落点。
     * <ul>
     *   <li>路上有方块 → 停在墙外最后一个空格（不穿墙、不补位）；</li>
     *   <li>落点自己或目标的格子被占 → 本次不吸附（{@link Result#attached()} 为假，落点 = 起点）；</li>
     *   <li>跨到落点用几刻由 {@link #dashTicks()} 决定，每刻速度见 {@link #velocity(Vec3, Vec3, int)}。</li>
     * </ul>
     */
    public static Result plan(Player player, LivingEntity target) {
        Vec3 start = player.position();
        if (!free(player.level(), target.position())) {
            return new Result(start, false);
        }
        Vec3 flat = new Vec3(target.getX() - start.x, 0.0, target.getZ() - start.z);
        double distance = flat.length();
        if (distance <= STOP_GAP) {
            return new Result(start, true);
        }
        Vec3 dir = flat.normalize();
        double travel = distance - STOP_GAP;
        Vec3 reachable = walk(player.level(), start, dir, travel);
        if (reachable.distanceTo(start) < SAMPLE_STEP) {
            return new Result(start, false);
        }
        return new Result(reachable, true);
    }

    /** {@code dashTicks} 刻内从 {@code from} 跨到 {@code landing} 的每刻速度。 */
    public static Vec3 velocity(Vec3 from, Vec3 landing, int dashTicks) {
        return landing.subtract(from).scale(1.0 / Math.max(1, dashTicks));
    }

    /** {@code wild_hunter.dashTicks}（默认 3 刻）。 */
    public static int dashTicks() {
        return FarmstormConfig.wildHunterDashTicks.get();
    }

    /** 沿 {@code dir} 走 {@code travel} 格，返回最后一个脚/头都空的位置；一格都走不了就返回起点。 */
    private static Vec3 walk(Level level, Vec3 start, Vec3 dir, double travel) {
        Vec3 reachable = start;
        int steps = (int) Math.ceil(travel / SAMPLE_STEP);
        for (int step = 1; step <= steps; step++) {
            Vec3 probe = start.add(dir.scale(Math.min(travel, step * SAMPLE_STEP)));
            if (!free(level, probe)) {
                break;
            }
            reachable = probe;
        }
        return reachable;
    }

    private static boolean free(Level level, Vec3 position) {
        return !blocked(level, position, FEET_OFFSET) && !blocked(level, position, HEAD_OFFSET);
    }

    private static boolean blocked(Level level, Vec3 position, double offset) {
        BlockPos pos = BlockPos.containing(position.x, position.y + offset, position.z);
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    /**
     * 一次吸附的计划结果。
     *
     * @param landing  落点（不吸附时等于起点）
     * @param attached 是否吸附
     */
    public record Result(Vec3 landing, boolean attached) {}
}
