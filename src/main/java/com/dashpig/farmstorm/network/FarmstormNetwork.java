package com.dashpig.farmstorm.network;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.client.ClientCoreList;
import com.dashpig.farmstorm.client.ClientCultivatorLinks;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 网络注册（30 号 §2.1.2）：1.20.1 走 {@link SimpleChannel}，在 **{@code FMLCommonSetupEvent}** 里建包注册。
 *
 * <p><b>1.21.1 的 {@code RegisterPayloadHandlersEvent} 在 1.20.1 不存在</b>（docs/zh/02-TECH-1.20.1.md §4.3）：
 * 通道由 {@link NetworkRegistry#ChannelBuilder} 建、包由 {@link SimpleChannel#messageBuilder} 加。
 * 只在 {@code play} 阶段、只 CLIENTBOUND——全仓没有服务端方向的注册（客户端不回包）。
 *
 * <p><b>两个包都是 S2C 只读</b>（D-172 / HC-115）：{@code farmstorm:core_list}（全量快照，F-200）与
 * {@code farmstorm:cultivator_link}（范围视图，F-238）。两者语义不同，各自独立注册、不合并 codec。
 *
 * <p><b>专用服务器为什么安全</b>：{@code ClientCoreList} / {@code ClientCultivatorLinks} 只出现在 lambda 体内，
 * 目标类在该 lambda 的合成方法被调用时才解析；而 CLIENTBOUND 的 handler 在服务端永不执行。
 *
 * <p><b>协议串</b>：仍沿用 {@value #PROTOCOL_VERSION}（1.21.1 的 F-302 值），本轮只做命名空间搬迁 + 能编译，
 * 版本串怎么对齐留给 F-011（docs/zh/04-PROMPT-MAIN-1.20.1.md 的 HC-003 要求改成 {@code "1"}）。
 */
public final class FarmstormNetwork {
    /**
     * 握手版本串：codec 一变必须跟着变（30 号 §2.1.2）。
     * F-238：新增第二个 S2C 包 ⇒ 版本 1 → 2；F-302：{@code Link} 从 2 字段变 3 字段 ⇒ 版本 2 → 3。
     */
    public static final String PROTOCOL_VERSION = "1";

    /** 通道名；与 {@link Farmstorm#MOD_ID} 一起构成线上通道 id。 */
    private static final String CHANNEL_NAME = "main";

    /** 唯一通道。{@code acceptedVersions} 两端都按 {@value #PROTOCOL_VERSION} 精确匹配（与 1.21.1 的 {@code optional()} 等价：不匹配只是没有通道，不崩）。 */
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(Farmstorm.id(CHANNEL_NAME))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    private FarmstormNetwork() {}

    /** 由 {@code Farmstorm} 构造器注册到 mod 总线。 */
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // 建包必须在主线程之外做（FMLCommonSetupEvent 的约定），enqueueWork 保证顺序。
        event.enqueueWork(() -> {
            CHANNEL.messageBuilder(CoreListPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(CoreListPayload::encode)
                    .decoder(CoreListPayload::decode)
                    .consumerMainThread((payload, context) -> {
                        ClientCoreList.handle(payload, context.get());
                        context.get().setPacketHandled(true);
                    })
                    .add();
            // 第 2 个包（F-238 / D-169 / D-172）：同样 S2C 只读，客户端不回包。
            CHANNEL.messageBuilder(CultivatorLinkPayload.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(CultivatorLinkPayload::encode)
                    .decoder(CultivatorLinkPayload::decode)
                    .consumerMainThread((payload, context) -> {
                        ClientCultivatorLinks.handle(payload, context.get());
                        context.get().setPacketHandled(true);
                    })
                    .add();
        });
        // 自检行（30 号 §7 #6 / 36 号 §8 #2 的「注册行含 farmstorm:xxx」判据）：
        // 包落进 minecraft: 命名空间是本 Phase 最隐蔽的翻车点（§5.5 第 1 条），所以把 id 原样打出来。
        Farmstorm.LOGGER.info("自检 网络包注册 {}（版本 {}，S2C 只读，MAX_CORES {}）→ OK",
                CoreListPayload.ID, PROTOCOL_VERSION, CoreListPayload.MAX_CORES);
        Farmstorm.LOGGER.info("自检 网络包注册 {}（版本 {}，S2C 只读，MAX_LINKS {}）→ OK",
                CultivatorLinkPayload.ID, PROTOCOL_VERSION, CultivatorLinkPayload.MAX_LINKS);
    }
}
