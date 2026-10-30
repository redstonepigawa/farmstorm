package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.effect.WildHunterEffect;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

/**
 * 狂野猎人的「持有者在场」表现与武器规则（作者 9 条里的 #2 / #5 / #6 / #7）。
 * <p>每刻由 {@link WildHunterCombatEvents#onPlayerTick} 驱动，全部是**可还原**的临时改动：
 * 阶梯高度与攻击速度都用 transient 修饰器（每刻重算、失效即摘），不碰玩家存档。
 */
public final class WildHunterAura {
    /** #7：跨越高度目标（1.21.1 的 {@code Attributes.STEP_HEIGHT} 基础值 0.6）。 */
    private static final double STEP_HEIGHT_TARGET = 1.5;
    /** #5：手持剑时**剑自身攻速值**的乘数（1.6 → 4.8）。 */
    private static final double SWORD_ATTACK_SPEED_MULTIPLIER = 3.0;
    /**
     * #6：手持斧时**在斧自身攻速值上加 0.8**（作者口径：0.8 是游戏内 attack_speed 属性单位，
     * 不是倍率）。原版铁斧自身约 0.9 → 加完 1.7；钻石斧 1.0 → 1.8。
     */
    private static final double AXE_ATTACK_SPEED_BONUS = 0.8;
    /** #5：单次攻击伤害上限。 */
    public static final float SWORD_DAMAGE_CAP = 5.0F;
    /** #5：非玩家生物失去 AI 的刻数（0.1 秒 = 2 刻）。 */
    private static final long AI_LOST_TICKS = 2L;
    /** #2：红烟粒子每 N 刻一小撮（落在 2~4 刻区间里）。 */
    private static final int PARTICLE_INTERVAL = 3;
    private static final Vector3f SMOKE_RED = new Vector3f(1.0F, 0.15F, 0.15F);

    private static final ResourceLocation STEP_ID = Farmstorm.id("wild_hunter_step_height");
    private static final ResourceLocation WEAPON_SPEED_ID = Farmstorm.id("wild_hunter_weapon_attack_speed");
    /** 被剥夺 AI 的生物 → 应恢复的 gameTime。 */
    private static final Map<UUID, Long> AI_RESTORE_AT = new ConcurrentHashMap<>();

    /**
     * #2 循环音的**服务端广播**重发间隔（刻）。曲长 47.14 s ≈ 942.8 刻，取 900 刻：
     * 下一次在上一次还没播完时就发出（尾巴重叠约 42 刻 ≈ 2.1 s）。宁可轻微重叠，也不要出现空档——
     * 空档会让听者以为「效果结束了」。
     */
    private static final int LOOP_BROADCAST_INTERVAL_TICKS = 900;
    // 广播半径不在这里写常量：读 config 的 wild_hunter.loopSoundRange（默认 32），与 sounds.json 的
    // attenuation_distance: 32 对齐。曾被写死成 32、而 config 默认值停在 16 ⇒ 那个键没有代码读，改了也无效。
    /**
     * 「这一对（持有者, 收件人）上次发包的 gameTime」。
     * <p>为什么按**收件人**计时而不是按持有者：按持有者统一发的话，刚走进 32 格的人、以及刚重连的人，
     * 最坏要等满 900 刻（45 秒）才会听到第一声——那会被当成「还是听不见」。按收件人计时，新人/重连者立刻收到。
     */
    private static final Map<LoopPair, Long> LOOP_LAST_SENT = new ConcurrentHashMap<>();

    /** 广播计时的键。 */
    private record LoopPair(UUID holder, UUID listener) {}

    private WildHunterAura() {}

    /** 每刻：阶梯高度、武器攻速、红烟粒子、AI 恢复扫描。 */
    public static void tick(ServerPlayer player) {
        if (!player.hasEffect(FarmstormMobEffects.WILD_HUNTER)) {
            clear(player);
            return;
        }
        applyStepHeight(player);
        applyWeaponAttackSpeed(player);
        spawnSmoke(player);
        broadcastLoop(player);
    }

    /** #5：让非玩家生物「失去 AI」若干刻，到期自动恢复。 */
    public static void loseAi(LivingEntity target, long now) {
        if (!(target instanceof Mob mob) || target instanceof net.minecraft.world.entity.player.Player) {
            return;
        }
        if (!mob.isNoAi()) {
            mob.setNoAi(true);
        }
        AI_RESTORE_AT.put(mob.getUUID(), now + AI_LOST_TICKS);
    }

    /** 服务端每刻扫描到期项（不挂在某个玩家身上，掉线/换维度也不会漏恢复）。 */
    public static void sweep(ServerLevel level) {
        // #2：掉线的持有者 / 收件人都不该留在计时表里——收件人掉线后重连要能**立刻**再收到（而不是等满 900 刻）
        if (!LOOP_LAST_SENT.isEmpty()) {
            var playerList = level.getServer().getPlayerList();
            LOOP_LAST_SENT.keySet().removeIf(pair -> {
                ServerPlayer holder = playerList.getPlayer(pair.holder());
                ServerPlayer listener = playerList.getPlayer(pair.listener());
                return holder == null || listener == null
                        || !holder.hasEffect(FarmstormMobEffects.WILD_HUNTER);
            });
        }
        if (AI_RESTORE_AT.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        AI_RESTORE_AT.entrySet().removeIf(entry -> {
            if (entry.getValue() > now) {
                return false;
            }
            net.minecraft.world.entity.Entity entity = level.getEntity(entry.getKey());
            if (entity instanceof Mob mob) {
                mob.setNoAi(false);
            }
            return true;
        });
    }

    private static void applyStepHeight(ServerPlayer player) {
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step == null) {
            return;
        }
        step.removeModifier(STEP_ID);
        double delta = STEP_HEIGHT_TARGET - step.getBaseValue();
        if (delta > 0.0) {
            step.addTransientModifier(new AttributeModifier(STEP_ID, delta, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void applyWeaponAttackSpeed(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed == null) {
            return;
        }
        // 三个 id 各自负责一件事，全部按 id 精确增删（绝不 removeAll，会清掉玩家自己/其它模组的修饰符）：
        //  · WEAPON_SPEED_ID —— 手持武器时的专用修饰符（剑 ×3 / 斧 +0.8）
        //  · WildHunterEffect.ATTACK_SPEED_ID —— 效果自带的通用 +30%（持武器时**让位**，否则会连乘）
        speed.removeModifier(WEAPON_SPEED_ID);
        speed.removeModifier(WildHunterEffect.ATTACK_SPEED_ID);
        ItemStack held = player.getMainHandItem();
        if (held.is(ItemTags.SWORDS)) {
            // #5：剑自身攻速 ×3 —— ADD_MULTIPLIED_TOTAL +2.0 即 (1+2.0)=×3，且在所有修饰符之后结算
            speed.addTransientModifier(new AttributeModifier(WEAPON_SPEED_ID, SWORD_ATTACK_SPEED_MULTIPLIER - 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (held.is(ItemTags.AXES)) {
            // #6：斧自身攻速 +0.8（属性单位，不是倍率）—— ADD_VALUE
            speed.addTransientModifier(new AttributeModifier(WEAPON_SPEED_ID, AXE_ATTACK_SPEED_BONUS,
                    AttributeModifier.Operation.ADD_VALUE));
        } else {
            // 空手/其它物品：恢复效果自带的 +30%
            speed.addTransientModifier(new AttributeModifier(WildHunterEffect.ATTACK_SPEED_ID,
                    FarmstormConfig.wildHunterAttackSpeedBonus() / 100.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** #2：以持有者为圆心、半径 1 格内冒红烟；只发服务端（客户端渲染收到的粒子包，不重复发）。 */
    private static void spawnSmoke(ServerPlayer player) {
        if (player.tickCount % PARTICLE_INTERVAL != 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        level.sendParticles(new DustParticleOptions(SMOKE_RED, 1.0F),
                player.getX(), player.getY() + 0.9, player.getZ(),
                4, 0.5, 0.6, 0.5, 0.01);
    }

    /**
     * #2：把循环音**广播给附近的其它玩家**（作者报的 bug：原实现只有本地客户端那份 SoundInstance，
     * 别人根本没收到任何东西）。
     *
     * <p>做法是原版 {@link ClientboundSoundEntityPacket}（不是自定义 payload，不违反 D-50）。
     * <b>必须用「绑定实体」的那个包</b>：它的客户端实例 {@code EntityBoundSoundInstance} 每刻从实体读坐标，
     * 声音跟着持有者走；换成按坐标的 {@code ClientboundSoundPacket}，位置会被烘进包里，玩家一走开声音留在原地。
     * <p>其余口径：
     * <ul>
     *   <li><b>排除持有者本人</b>——他自己的那份由客户端 {@code WildHunterLoopSound}（{@code relative=true}）播放，
     *       这里再发一份他就会听到两层，表现为回声/叠加；</li>
     *   <li>原版包只播**一次**（本曲 47.14 s），所以按 {@link #LOOP_BROADCAST_INTERVAL_TICKS} 周期重发；</li>
     *   <li>只发给 {@code wild_hunter.loopSoundRange}（默认 32 格）内的玩家，与 {@code attenuation_distance: 32} 对齐。</li>
     * </ul>
     * 效果结束时不需要发「停止」：{@link #clear(ServerPlayer)} 摘掉重发记录，最后一次播放会在曲末自然结束。
     */
    private static void broadcastLoop(ServerPlayer holder) {
        if (!(holder.level() instanceof ServerLevel level)) {
            return;
        }
        // 用**绑定实体**的声音包：ClientboundSoundPacket 会把坐标烘进包里，声音就钉在发包那一刻的位置上，
        // 玩家一走开声音留在原地（作者实测报的正是这个）。EntityBoundSoundInstance 每刻从实体读坐标，声音跟着人走。
        // 持有者本人就是那个实体（ServerPlayer 是 Entity）。
        long now = level.getGameTime();
        double radius = FarmstormConfig.wildHunterLoopSoundRange.get();
        double radiusSqr = radius * radius;
        int sent = 0;
        for (ServerPlayer listener : level.players()) {
            if (listener == holder || listener.distanceToSqr(holder) > radiusSqr) {
                continue;                              // 排除持有者本人：否则他会同时听到客户端那份 + 这份
            }
            LoopPair pair = new LoopPair(holder.getUUID(), listener.getUUID());
            Long last = LOOP_LAST_SENT.get(pair);
            if (last != null && now - last < LOOP_BROADCAST_INTERVAL_TICKS) {
                continue;                              // 这位收件人还在本轮的播放窗口里
            }
            listener.connection.send(new ClientboundSoundEntityPacket(
                    FarmstormSounds.WILD_HUNTER_LOOP, SoundSource.PLAYERS,
                    holder, 1.0F, 1.0F, level.getRandom().nextLong()));
            LOOP_LAST_SENT.put(pair, now);
            sent++;
        }
        if (sent > 0) {
            Farmstorm.LOGGER.info("狂野猎人 循环音 广播 t={} 持有者 {} → {} 人（半径 {} 格，排除持有者）",
                    now, holder.getGameProfile().getName(), sent, (int) radius);
        }
    }

    /** 失效即摘除两个临时修饰器（效果到期 / clear / 死亡 / 退出世界都由这里兜住）。 */
    public static void clear(ServerPlayer player) {
        LOOP_LAST_SENT.keySet().removeIf(pair -> pair.holder().equals(player.getUUID()));  // 效果结束就不再重发
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step != null) {
            step.removeModifier(STEP_ID);
        }
        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed != null) {
            speed.removeModifier(WEAPON_SPEED_ID);
            // 效果结束：让效果自己的 +30% 恢复（效果的 removeAttributeModifiers 也会按同一 id 清掉）
            speed.removeModifier(WildHunterEffect.ATTACK_SPEED_ID);
        }
    }
}
