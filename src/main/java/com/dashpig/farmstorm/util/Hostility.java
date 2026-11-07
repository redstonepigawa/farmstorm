package com.dashpig.farmstorm.util;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/**
 * 敌对判据（裁定 #9 / D-182 / 42 号 §2.4）：{@code Enemy} 实例 ∪ 类别为 {@code MONSTER} 的并集。
 *
 * <p>全仓此前的敌对口径只有 {@code Monster} 一个类（5 处 {@code getEntitiesOfClass(Monster.class, …)}），
 * 那 5 处是陷阱的**触发面**，本版不动（RT-57）；本类只服务陷阱倍率的**伤害面**，使用点只有 2 处
 * （{@code FarmstormExplosion#hurtCapped} 与 {@code ZangiefCaneTrap#monsterBranch}）。
 *
 * <p>为什么是并集而不是 {@code instanceof Monster}：{@link Enemy} 是纯标记接口（11 行、0 方法），
 * 原版 {@code Monster} 已 {@code implements Enemy}，所以并集的实际增量只来自两类模组生物——
 * 类别是 {@code MONSTER} 却没实现 {@code Enemy} 的、实现了 {@code Enemy} 却不在 {@code monster} 包里的。
 * 只写其中一支都会漏掉另一半。
 */
public final class Hostility {
    private Hostility() {}

    /**
     * 非生物与玩家一律 false；其余按「实现了 {@link Enemy}」或「类别是 {@link MobCategory#MONSTER}」判。
     *
     * <p>玩家那一支是给读者看的**保险**（玩家既不是 {@code Enemy}、类别也不是 {@code MONSTER}），
     * 但它让「玩家一定不乘倍率」这条验收读得出来，不依赖读者去推理两个集合的交集。
     */
    public static boolean isHostile(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        if (entity instanceof Player) {
            return false;
        }
        return entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER;
    }
}
