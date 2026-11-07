package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.combat.MobAiSuspension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;

/**
 * 口径外的第 13 类：<b>实体附件类型 1 个</b>（{@code farmstorm:ai_suspend}，42 号 §1.2.1 / 裁定 #17 / A-73）。
 *
 * <p><b>1.20.1 没有数据附件 API</b>（{@code AttachmentType} 是 1.20.5+ 的东西）。
 * 按 docs/zh/02-TECH-1.20.1.md §4.2 的等价类型口径，这里落到<b>实体持久 NBT</b>，键名
 * {@value #AI_SUSPEND_KEY}。存取语义与 1.21.1 的附件逐条对齐：
 * <ul>
 *   <li>只活在服务端与存档（{@code Entity#getPersistentData()} 随实体 NBT 落盘），客户端不读；</li>
 *   <li>死亡不复制（原版不会把持久 NBT 拷给新实体，与原 {@code copyOnDeath = false} 一致）；</li>
 *   <li>{@code sources == 0}（{@link MobAiSuspension.Suspend#NONE}）时键整个移除，
 *       存档里不出现该键——对应原 {@code serialize(codec, predicate)} 的 predicate 为 false。</li>
 * </ul>
 *
 * <p>序列化仍走 {@link MobAiSuspension.Suspend#CODEC}，字段形状与原附件一字不差。
 *
 * @see <a href="file:///D:/project_farm1.20.1/docs/zh/02-TECH-1.20.1.md">02 号 §4.2</a>
 */
// TODO(F-010): 若后续需要「多宿主并发写 + 版本协商」，把这里升级成 Capability（RegisterCapabilitiesEvent
// + ICapabilitySerializable）；本轮只要求 compileJava 变绿，按 02 号 §4.2 的 NBT 等价口径落地。
public final class FarmstormAttachments {

    /** 实体持久 NBT 键（02 号 §4.2 约定的 {@code FarmstormAiSuspend}）。 */
    public static final String AI_SUSPEND_KEY = "FarmstormAiSuspend";

    /** 读 AI 挂起原值；没有记录时返回 {@link MobAiSuspension.Suspend#NONE}。 */
    public static MobAiSuspension.Suspend getAiSuspend(Mob mob) {
        CompoundTag tag = mob.getPersistentData().getCompound(AI_SUSPEND_KEY);
        if (tag.isEmpty()) {
            return MobAiSuspension.Suspend.NONE;
        }
        return MobAiSuspension.Suspend.CODEC.parse(NbtOps.INSTANCE, tag)
                .result()
                .orElse(MobAiSuspension.Suspend.NONE);
    }

    /** 写 AI 挂起原值；{@code sources == 0} 等同于删除（键不落盘）。 */
    public static void setAiSuspend(Mob mob, MobAiSuspension.Suspend value) {
        if (value.sources() == 0) {
            removeAiSuspend(mob);
            return;
        }
        Tag encoded = MobAiSuspension.Suspend.CODEC.encodeStart(NbtOps.INSTANCE, value)
                .result()
                .orElseThrow(() -> new IllegalStateException("ai_suspend 编码失败: " + value));
        mob.getPersistentData().put(AI_SUSPEND_KEY, encoded);
    }

    /** 删除 AI 挂起记录（原 {@code removeData}）。 */
    public static void removeAiSuspend(Mob mob) {
        mob.getPersistentData().remove(AI_SUSPEND_KEY);
    }

    private FarmstormAttachments() {}
}
