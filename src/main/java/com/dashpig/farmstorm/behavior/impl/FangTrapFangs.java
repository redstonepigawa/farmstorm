package com.dashpig.farmstorm.behavior.impl;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

/**
 * 刺球的尖牙归属表与伤害事件订阅（TECH §3.8、D-95）：两圈尖牙分两批出，只有**实际造成伤害**的那一次才施加缓慢 V。
 *
 * <p>「击中」的定义落在伤害结算链上，不做几何复算（D-95）——三条同时成立才算击中：
 * ① 伤害来源的直接实体是原版 {@link EvokerFangs}；② 该尖牙在本陷阱的归属表里（表只登记本陷阱生成的尖牙，
 * 归属者仍是部署者）；③ 受伤者是玩家。没被伤害结算到的玩家（动画扫到但没掉血、无敌帧吞掉、同刻被另一颗尖牙命中）
 * 因此不会拿到缓慢。
 *
 * <p>去重键是 **部署点 × 玩家**，不是玩家全局：同一个陷阱的一波尖牙只给同一玩家施加一次；
 * 相邻的两个刺球各自都要生效（它们是两个独立陷阱）。
 *
 * <p>两张表都是有界内存态：条目的有效期都是 {@link #WINDOW_TICKS}（原版尖牙寿命 20 刻的两倍余量），
 * 不写 NBT、不注册任何对象。条目上限约等于「同时在飞的 13 颗尖牙」（8 外圈 + 5 内圈）。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID)
public final class FangTrapFangs {

    /** 归属条目与「已施加过」窗口共用的有效期。 */
    private static final int WINDOW_TICKS = 40;

    /** 尖牙实体 id → 归属（部署点 + 失效 gameTime）。 */
    private static final Map<Integer, FangMark> FANGS = new HashMap<>();

    /** 玩家 UUID → 该玩家在本陷阱上已施加过缓慢的截止 gameTime。 */
    private static final Map<Slowed, Long> SLOWED_UNTIL = new HashMap<>();

    private FangTrapFangs() {}

    /** 一颗本陷阱的尖牙：{@code trap} 用来区分不同陷阱，{@code until} 之后条目自动失效。 */
    private record FangMark(BlockPos trap, long until) {}

    /** 去重键：同一个陷阱 × 同一个玩家。 */
    private record Slowed(BlockPos trap, UUID player) {}

    /** 生成前登记一颗本陷阱的尖牙：{@code WINDOW_TICKS} 之后条目自动失效。 */
    public static void mark(EvokerFangs fangs, BlockPos trap, long now) {
        prune(now);
        FANGS.put(fangs.getId(), new FangMark(trap, now + WINDOW_TICKS));
    }

    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof EvokerFangs fangs)
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;                                    // ① 不是尖牙 / ③ 受伤者不是玩家
        }
        long now = player.level().getGameTime();
        FangMark mark = FANGS.get(fangs.getId());
        if (mark == null || now > mark.until()) {
            return;                                    // ② 不是本陷阱的尖牙
        }
        Slowed key = new Slowed(mark.trap(), player.getUUID());
        Long slowedUntil = SLOWED_UNTIL.get(key);
        if (slowedUntil != null && now < slowedUntil) {
            return;                                    // 同一个陷阱对同一个玩家只施加一次
        }
        SLOWED_UNTIL.put(key, now + WINDOW_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                FarmstormConfig.fangTrapSlownessDurationTicks.get(),
                FarmstormConfig.fangTrapSlownessLevel.get() - 1));
    }

    /** 惰性清理：两张表都只活在 {@link #WINDOW_TICKS} 之内，不引入定时器。 */
    private static void prune(long now) {
        FANGS.values().removeIf(mark -> now > mark.until());
        SLOWED_UNTIL.values().removeIf(until -> now > until);
    }
}
