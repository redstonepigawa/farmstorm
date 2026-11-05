package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.combat.MobAiSuspension;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 口径外的第 13 类：<b>实体附件类型 1 个</b>（{@code farmstorm:ai_suspend}，42 号 §1.2.1 / 裁定 #17 / A-73）。
 *
 * <p><b>不计入 98</b>：98 的口径是本项目 12 类（{@code Farmstorm#logRegistrySelfCheck} 的断言），全部落在
 * {@code BuiltInRegistries}（伤害类型数 {@code ResourceKey}）。附件注在 NeoForge 自己的
 * {@link NeoForgeRegistries.Keys#ATTACHMENT_TYPES}（{@code NeoForgeRegistries.java:58}），不在这 12 类里
 * ⇒ 注册面仍 <b>98</b>、{@code data_components} 仍是 <b>1</b>。
 *
 * <p><b>附件不是数据组件</b>：D-175 否决的是 {@code DataComponentType}（12 类的第 11 类，加一个就 98 → 99）；
 * 这里用的是 NeoForge 的实体附件注册表，两句话必须一起读，否则会被当成「换了个名字绕过自己的否决」。
 *
 * <p><b>工具既不核它的存在、也不核它的计数</b>（{@code verify_project.py} 的 {@code EXPECTED} 12 组 id 里没有
 * {@code ai_suspend}、{@code EXPECTED_COUNTS} 相加仍是 98）⇒ 这条覆盖缺口由「42 号 §1.2.1 + F-252 验收 5 +
 * F-266 签字」三处人工登记兜住，本类的任何验收都<b>不</b>写「工具通过」。
 *
 * <p>不挂同步（{@code AttachmentType.Builder#sync} 的三个重载一个都不用：附件只活在服务端与存档，
 * 客户端不需要），也不开随死亡复制（默认 {@code false}，这是「死亡不复制、不造第二个宿主」的前提）；
 * {@code sources == 0} 的 {@code NONE} 不落盘
 * （{@code serialize(codec, predicate)} 的 predicate 为 false 时写出 {@code null}，键根本不出现）。
 */
public final class FarmstormAttachments {

    public static final DeferredRegister<AttachmentType<?>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Farmstorm.MOD_ID);

    /** 全名 {@code farmstorm:ai_suspend}：AI 原值的落盘载体（{@link MobAiSuspension.Suspend}）。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobAiSuspension.Suspend>> AI_SUSPEND =
            REGISTER.register("ai_suspend", () -> AttachmentType.builder(() -> MobAiSuspension.Suspend.NONE)
                    .serialize(MobAiSuspension.Suspend.CODEC, suspend -> suspend.sources() != 0)
                    .build());

    private FarmstormAttachments() {}
}
