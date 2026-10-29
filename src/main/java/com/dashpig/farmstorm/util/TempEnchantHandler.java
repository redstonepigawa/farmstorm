package com.dashpig.farmstorm.util;

import com.dashpig.farmstorm.registry.FarmstormDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * 临时附魔账本（TECH §4.10 / D-15 / D-41）。
 *
 * <p>状态全部落在物品的 {@code farmstorm:temporary_enchantments} 组件上，绝不写玩家持久数据：
 * 物品被丢弃 / 进箱子 / 死亡掉落时记录随物品走，取到手的下一次 {@link #tick(ServerPlayer)} 自愈（RT-03）。
 * {@code expireAt} 用绝对 {@code gameTime}，因此不存在永不过期的挂起记录。
 */
public final class TempEnchantHandler {
    private TempEnchantHandler() {}

    /**
     * 授予临时附魔：记录原等级后按「取高不取低」写入，同一条附魔的临时记录只刷新到期时间，不压栈。
     *
     * @param player        受益玩家（只用来读 {@code gameTime}）
     * @param stack         被授予的物品（通常是手持剑）
     * @param enchantment   附魔
     * @param level         授予等级
     * @param durationTicks 持续刻数
     */
    public static void grant(ServerPlayer player, ItemStack stack, Holder<Enchantment> enchantment,
                             int level, int durationTicks) {
        if (stack.isEmpty() || level <= 0 || durationTicks <= 0) {
            return;
        }
        long now = player.level().getGameTime();
        // 关键：先把这条物品上**已过期但还没被 20 刻扫描器清掉**的记录还原掉，
        // 否则下面的 previous 会读到「临时等级」（例如 1），新记录的 previous 也随之变成 1，
        // 到期时还原成 1 → 附魔永久留下（作者报的「火焰附加不消失」）。
        revokeExpired(stack, now);
        long expireAt = now + durationTicks;
        List<TempEnchantRecord> records = read(stack);
        for (int index = 0; index < records.size(); index++) {
            TempEnchantRecord record = records.get(index);
            if (!sameEnchantment(record.enchantment(), enchantment) || record.expireAt() <= now) {
                continue;
            }
            records.set(index, new TempEnchantRecord(enchantment, record.granted(), record.previous(), expireAt));
            EnchantmentHelper.updateEnchantments(stack,
                    enchantments -> enchantments.set(enchantment, Math.max(record.previous(), record.granted())));
            write(stack, records);
            return;
        }
        // 1.21.1 起 NeoForge 把「原等级」的读取点挪到了 ItemStack 上（EnchantmentHelper 的同名方法已废弃）。
        int previous = stack.getEnchantmentLevel(enchantment);
        EnchantmentHelper.updateEnchantments(stack, enchantments -> enchantments.set(enchantment, Math.max(previous, level)));
        records.add(new TempEnchantRecord(enchantment, level, previous, expireAt));
        write(stack, records);
    }

    /**
     * 周期性自愈：手持 / 副手 / 护甲 / 背包全扫一遍，把过期的临时附魔还原掉。
     * 由 {@code PlayerTickEvent.Post} 每 20 tick 调一次，覆盖「物品进箱子后再取出」「切换手持」「下线重连」等所有路径。
     */
    public static void tick(ServerPlayer player) {
        long now = player.level().getGameTime();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            revokeExpired(inventory.getItem(slot), now);
        }
    }

    /**
     * 撤销一条物品上所有已到期的临时附魔；记录清空时整个组件被移除。
     *
     * @param stack 目标物品
     * @param now   当前绝对 {@code gameTime}
     */
    public static void revokeExpired(ItemStack stack, long now) {
        List<TempEnchantRecord> records = read(stack);
        if (records.isEmpty()) {
            return;
        }
        List<TempEnchantRecord> remaining = new ArrayList<>(records.size());
        boolean expired = false;
        for (TempEnchantRecord record : records) {
            if (record.expireAt() > now) {
                remaining.add(record);
                continue;
            }
            expired = true;
            restore(stack, record);
        }
        if (expired) {
            write(stack, remaining);
        }
    }

    /** 立刻撤销该物品上全部临时附魔（离开光环 / 换成别的手持物品时用）。 */
    public static void revokeAll(ItemStack stack) {
        List<TempEnchantRecord> records = read(stack);
        if (records.isEmpty()) {
            return;
        }
        for (TempEnchantRecord record : records) {
            restore(stack, record);
        }
        write(stack, List.of());
    }

    /** 该物品上是否存在这条附魔的临时记录（不区分是否已到期——到期清理走 {@link #tick(ServerPlayer)}）。 */
    public static boolean hasTemporary(ItemStack stack, Holder<Enchantment> enchantment) {
        for (TempEnchantRecord record : read(stack)) {
            if (sameEnchantment(record.enchantment(), enchantment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 还原一条记录：只在「当前等级 == 授予等级」时动手（RT-03 不覆盖检查）。
     * 玩家自己用铁砧升过级、或用砂轮洗掉了附魔，都只丢记录，不改玩家的真实附魔。
     */
    private static void restore(ItemStack stack, TempEnchantRecord record) {
        Holder<Enchantment> enchantment = record.enchantment();
        int current = stack.getEnchantmentLevel(enchantment);
        if (current != record.granted()) {
            return;
        }
        EnchantmentHelper.updateEnchantments(stack, enchantments -> enchantments.set(enchantment, record.previous()));
    }

    /** 写账本：空列表移除整个组件（F-18 ②：{@code /data get} 里不应再有该键）。 */
    private static void write(ItemStack stack, List<TempEnchantRecord> records) {
        if (records.isEmpty()) {
            stack.remove(FarmstormDataComponents.TEMPORARY_ENCHANTMENTS.get());
            return;
        }
        stack.set(FarmstormDataComponents.TEMPORARY_ENCHANTMENTS.get(), List.copyOf(records));
    }

    private static List<TempEnchantRecord> read(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ArrayList<>(0);
        }
        return new ArrayList<>(stack.getOrDefault(FarmstormDataComponents.TEMPORARY_ENCHANTMENTS.get(), List.of()));
    }

    /** 同一个附魔：优先比注册键，内联附魔（无注册键）退回实例比较。 */
    private static boolean sameEnchantment(Holder<Enchantment> first, Holder<Enchantment> second) {
        if (first.value() == second.value()) {
            return true;
        }
        Optional<ResourceKey<Enchantment>> firstKey = first.unwrapKey();
        Optional<ResourceKey<Enchantment>> secondKey = second.unwrapKey();
        return firstKey.isPresent() && firstKey.equals(secondKey);
    }
}
