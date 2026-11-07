package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.network.CoreListPayload;
import java.util.List;
import net.minecraftforge.network.NetworkEvent;

/**
 * 客户端快照（F-199）：{@code volatile} 的两个引用，只被 handler 写、只被渲染读。
 *
 * <p>没有锁、没有竞态，因为两侧都只做一次引用替换（30 号 §2.1.2 第 3 条）。
 * 本类**只在客户端加载**：{@link #handle} 的唯一调用方是 {@code FarmstormNetwork} 里那个 lambda。
 */
public final class ClientCoreList {
    private static volatile List<CoreListPayload.Core> cores = List.of();
    private static volatile int total;

    private ClientCoreList() {}

    /**
     * S2C handler：方法体只有一句 {@code enqueueWork}。写在这里而不是依赖注册器的默认
     * {@code MainThreadPayloadHandler}，是把「必须在主线程」固定在调用点（30 号 §2.1.2 的三条理由）。
     */
    public static void handle(CoreListPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> set(payload.cores(), payload.total()));
    }

    public static void set(List<CoreListPayload.Core> newCores, int newTotal) {
        cores = List.copyOf(newCores);
        total = newTotal;
        Farmstorm.LOGGER.info("收到核心列表 {} 条（total={}）", cores.size(), total);
    }

    /**
     * 换服 / 断线清空（30 号 §2.1.4 的「客户端兜底」）：不清就会把上一个服务器的核带到下一个服务器上，
     * 这是本版唯一会跨会话残留的客户端状态（30 号 §7 #29）。
     */
    public static void clear() {
        cores = List.of();
        total = 0;
    }

    public static List<CoreListPayload.Core> cores() {
        return cores;
    }

    public static int total() {
        return total;
    }
}
