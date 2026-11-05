package com.dashpig.farmstorm.network;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.client.ClientCoreList;
import com.dashpig.farmstorm.client.ClientCultivatorLinks;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * 网络注册（30 号 §2.1.2）：挂在 **mod 总线**（{@code RegisterPayloadHandlersEvent} 是 {@code IModBusEvent}），
 * 只在 {@code play} 阶段、只 CLIENTBOUND——全仓没有服务端方向的注册（客户端不回包）。
 *
 * <p><b>两个包都是 S2C 只读</b>（D-172 / HC-115）：{@code farmstorm:core_list}（全量快照，F-200）与
 * {@code farmstorm:cultivator_link}（范围视图，F-238）。两者语义不同，各自独立注册、不合并 codec
 * （D-169 的「全量快照与范围增量是两类数据」）。
 *
 * <p><b>专用服务器为什么安全</b>：{@code ClientCoreList} / {@code ClientCultivatorLinks} 只出现在 lambda 体内，
 * 目标类在该 lambda 的合成方法被调用时才解析；而 CLIENTBOUND 的 handler 在服务端永不执行。
 * 这是 30 号 §2.1.2 的设计前提，由「专用服务器能起服、无 NoClassDefFoundError」兜底（30 号 §7 #6）。
 */
public final class FarmstormNetwork {
    /**
     * 握手版本串：codec 一变必须跟着变（30 号 §2.1.2）。
     * F-238：新增第二个 S2C 包 ⇒ 版本 1 → 2，与 codec 同一个提交（36 号 §7 翻车点 1）。
     * F-302：{@code Link} 从 2 字段变 3 字段（多一个 {@code Kind} 字节）⇒ 版本 2 → 3
     * （D-210 的强制连带项 / 54 号 §5.5 / A-99 / 死命令 5）。停在 2 会让 0.2.2 客户端**协商通过**
     * 之后按旧 codec 解新字节流，那是必崩的解码错误；升到 3 之后 {@code optional()} 让它拿不到这条通道，
     * 只是没有连线、不崩。
     */
    public static final String PROTOCOL_VERSION = "3";

    private FarmstormNetwork() {}

    /** 由 {@code Farmstorm} 构造器注册到 mod 总线。 */
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
                // optional()：**不**把这条通道当成「必须装 NeoForge + 必须装本模组」的门槛。
                // 实测（本文档-源码偏差表 D-1）：不加它，专用服务器会在 CONFIGURATION 阶段直接拒掉
                // 每一个原版客户端（"You are trying to connect to a server that is running NeoForge,
                // but you are not"），而本包是纯 S2C 只读、客户端从不回包，没有任何理由要求它必需
                // ——Phase 29 及以前原版客户端是可以进服的。
                .optional()
                .playToClient(CoreListPayload.TYPE, CoreListPayload.STREAM_CODEC,
                        (payload, context) -> ClientCoreList.handle(payload, context))
                // 第 2 个包（F-238 / D-169 / D-172）：同样 optional + S2C 只读，客户端不回包。
                // 协议串随 F-238 升到 2、随 F-302 升到 3 —— 只加这条而忘改版本串，会让「装了新模组的客户端连旧服」握手不匹配。
                .optional()
                .playToClient(CultivatorLinkPayload.TYPE, CultivatorLinkPayload.STREAM_CODEC,
                        (payload, context) -> ClientCultivatorLinks.handle(payload, context));
        // 自检行（30 号 §7 #6 / 36 号 §8 #2 的「注册行含 farmstorm:xxx」判据）：NeoForge 自己不在 INFO 级别
        // 打 payload 注册记录，而「包落进 minecraft: 命名空间」是本 Phase 最隐蔽的翻车点（§5.5 第 1 条），
        // 因此把注册后的 id 原样打出来，命名空间错了一眼就能看见。
        // F-238：**两行**，每行各带自己的 id 与同一个版本串（判据按行数数得出来，36 号 §7 翻车点 2）。
        Farmstorm.LOGGER.info("自检 网络包注册 {}（版本 {}，S2C 只读，MAX_CORES {}）→ OK",
                CoreListPayload.TYPE.id(), PROTOCOL_VERSION, CoreListPayload.MAX_CORES);
        Farmstorm.LOGGER.info("自检 网络包注册 {}（版本 {}，S2C 只读，MAX_LINKS {}）→ OK",
                CultivatorLinkPayload.TYPE.id(), PROTOCOL_VERSION, CultivatorLinkPayload.MAX_LINKS);
    }
}
