package com.dashpig.farmstorm.explosion;

import com.dashpig.farmstorm.Farmstorm;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * 同 tick 爆炸限流（TECH §8.2 / §4.11.6）。
 * 超过 {@link #MAX_EXPLOSIONS_PER_TICK} 的爆炸进入队列，由 {@code TickEvent.LevelTickEvent} 逐个执行，
 * 延迟不超过 1 tick，观感与原版无差异。
 * 配额按维度独立记账：一个维度里的爆竹风暴不会吃掉另一个维度的预算。
 */
public final class ExplosionQueue {
    /** 安全阀（HC-13）：不得调整、不得挪进 config。 */
    public static final int MAX_EXPLOSIONS_PER_TICK = 8;

    /** 待执行队列，键是维度。只在服务端主线程读写，因此用普通 HashMap。 */
    private static final Map<ResourceKey<Level>, Deque<FarmstormExplosion>> DEFERRED = new HashMap<>();

    /** 本 tick 已立即执行的次数，键是维度。tick 末清零。 */
    private static final Map<ResourceKey<Level>, Integer> EXECUTED = new HashMap<>();

    private ExplosionQueue() {}

    /**
     * 行为层的唯一入口：预算内立即执行，超预算则推迟到本 tick 末。
     *
     * @param level     爆炸所在的服务端维度
     * @param explosion 已构造好的爆炸
     */
    public static void submit(ServerLevel level, FarmstormExplosion explosion) {
        ResourceKey<Level> dimension = level.dimension();
        int executed = EXECUTED.getOrDefault(dimension, 0);
        if (executed < MAX_EXPLOSIONS_PER_TICK) {
            EXECUTED.put(dimension, executed + 1);
            explosion.detonate();
            return;
        }
        DEFERRED.computeIfAbsent(dimension, key -> new ArrayDeque<>()).addLast(explosion);
    }

    /**
     * tick 末结算：执行本 tick 攒下的爆炸并清零配额。
     * 队列在配额清零前清空，因此被推迟的爆炸不会占用下一个 tick 的预算。
     */
    public static void drain(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        EXECUTED.remove(dimension);
        Deque<FarmstormExplosion> deferred = DEFERRED.remove(dimension);
        if (deferred == null || deferred.isEmpty()) {
            return;
        }
        int drained = 0;
        while (!deferred.isEmpty()) {
            deferred.pollFirst().detonate();
            drained++;
        }
        Farmstorm.LOGGER.info("爆炸批处理：本 tick 超量，deferred={} 已在 tick 末执行", drained);
    }
}
