package com.dashpig.farmstorm.combat;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.effect.WildHunterEffect;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
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

    /** 失效即摘除两个临时修饰器（效果到期 / clear / 死亡 / 退出世界都由这里兜住）。 */
    public static void clear(ServerPlayer player) {
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
