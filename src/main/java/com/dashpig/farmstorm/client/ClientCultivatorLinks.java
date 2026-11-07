package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.network.CultivatorLinkPayload;
import java.util.List;
import net.minecraftforge.network.NetworkEvent;

/**
 * 客户端连线快照（F-240 / D-169 补充的三条「只读」）：{@code volatile} 的一个引用，
 * 只被 handler 写、只被渲染读。
 *
 * <p><b>只读三条</b>：
 * <ol>
 *   <li><b>收到什么存什么</b>——不推导绑定、不按坐标反查方块实体、不算「谁该连谁」；
 *       绑定关系的唯一真相在服务端（D-165）。</li>
 *   <li><b>不跨维度缓存</b>——{@link #clear()} 在换服 / 断线 / 换维度三处调用。
 *       包里不带维度（36 号 §2.8.1：同维度的绑定才成立），所以清空只能靠这三个时点。</li>
 *   <li><b>空列表真的清空</b>——收到的空列表是「你已不在任何核的 3 × 3 内」的权威回答，
 *       不是「没事发生」，因此**不忽略空包**（35 号 §12 B-38 的原始症状是远处的线一直挂着）。</li>
 * </ol>
 *
 * <p>没有锁、没有竞态，因为两侧都只做一次引用替换（30 号 §2.1.2 第 3 条）。
 * 本类**只在客户端加载**：{@link #handle} 的唯一调用方是 {@code FarmstormNetwork} 里那个 lambda。
 */
public final class ClientCultivatorLinks {
    private static volatile List<CultivatorLinkPayload.Link> links = List.of();

    private ClientCultivatorLinks() {}

    /**
     * S2C handler：方法体**只有一句 {@code enqueueWork}**（照 {@code ClientCoreList#handle}）。
     * 这里刻意不出现任何渲染类引用——专用服务器永不执行 CLIENTBOUND 的 handler，
     * 而「服务端侧引用了渲染类」正是 {@code NoClassDefFoundError} 的来源（36 号 §7 翻车点 8）。
     */
    public static void handle(CultivatorLinkPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> set(payload.links()));
    }

    public static void set(List<CultivatorLinkPayload.Link> newLinks) {
        links = List.copyOf(newLinks);
        Farmstorm.LOGGER.info("收到连线 {} 条", links.size());
    }

    /**
     * 换服 / 断线 / 换维度清空（D-169 补充）。不清就会把上一局的线画到这一局的世界坐标上。
     * 挂载点：{@code FarmstormClient} 的 {@code ClientPlayerNetworkEvent.LoggingOut}、
     * 逐刻兜底（{@code level == null}）与维度变化检测三处。
     */
    public static void clear() {
        links = List.of();
    }

    public static List<CultivatorLinkPayload.Link> links() {
        return links;
    }
}
