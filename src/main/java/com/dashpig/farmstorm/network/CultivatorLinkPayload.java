package com.dashpig.farmstorm.network;

import com.dashpig.farmstorm.Farmstorm;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * 全项目第 **2** 个自定义网络包（D-169 / D-172 / 36 号 §2.8.1）：服务端 → 客户端单向推送
 * 「**当前维度内、玩家 3 × 3 区块范围覆盖到的**核 ↔ 培养器绑定关系」。
 *
 * <p><b>1.20.1 没有 {@code CustomPacketPayload} / {@code StreamCodec}</b>：按 docs/zh/02-TECH-1.20.1.md §4.3
 * 改手写 {@link FriendlyByteBuf} 编解码，字段顺序与原三元 {@code composite} 一字不差
 * （core → cultivator → kind 字节）。
 *
 * <p>与 {@link CoreListPayload} 是**两类数据**（D-169 明确否决合并）：{@code core_list} 是己方核的
 * **全量快照**；本包是**逐玩家、按范围过滤、随绑定变化**的增量视图。
 *
 * <p><b>为什么不带 {@code total}</b>：本包的列表是**按玩家过滤后的完整集合**，永不截断 ——
 * {@link #MAX_LINKS} 只是**解码上限**，不当截断值用（36 号 §2.8.1）。
 *
 * <p><b>为什么不带维度</b>：培养器与核必同维度（D-165 补充），
 * 所以「所在维度」由收件人自己当前的维度承载，不上线。
 */
public record CultivatorLinkPayload(List<Link> links) {
    /**
     * 单包条目的**解码上限**（36 号 §2.8.1 的算术）：理论最坏值 4096；本常量取 1024 是**解码保护**，
     * 不按默认值推导。
     */
    public static final int MAX_LINKS = 1024;

    /** 注册名必须是 {@code farmstorm:cultivator_link}（36 号 §8 #2 的注册行判据）。 */
    public static final ResourceLocation ID = Farmstorm.id("cultivator_link");

    /** 写包体。 */
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(links.size());
        for (Link link : links) {
            link.encode(buffer);
        }
    }

    /** 读包体；条数按 {@link #MAX_LINKS} 封顶。 */
    public static CultivatorLinkPayload decode(FriendlyByteBuf buffer) {
        int size = Math.min(buffer.readVarInt(), MAX_LINKS);
        List<Link> links = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            links.add(Link.decode(buffer));
        }
        return new CultivatorLinkPayload(List.copyOf(links));
    }

    /**
     * 一条连线：核的坐标 + 机器的坐标 + **连着哪一类机器**。三个都是线上字段，画线时两侧各取格心（+0.5）。
     */
    public record Link(BlockPos core, BlockPos cultivator, Kind kind) {
        void encode(FriendlyByteBuf buffer) {
            buffer.writeBlockPos(core);
            buffer.writeBlockPos(cultivator);
            buffer.writeByte(kind.id());
        }

        static Link decode(FriendlyByteBuf buffer) {
            return new Link(buffer.readBlockPos(), buffer.readBlockPos(), Kind.byId(buffer.readByte()));
        }

        /**
         * 连线类型（F-302 / D-210 / 54 号 §5.1）：线上各占 **1 字节**，{@code CULTIVATOR = 0}、
         * {@code CROP_CONVERTER = 1}。未知码按 {@link #CULTIVATOR} 解（前向兼容）。
         */
        public enum Kind {
            CULTIVATOR(0),
            CROP_CONVERTER(1);

            private final int id;

            Kind(int id) {
                this.id = id;
            }

            /** 线上码：1 字节，与 {@link #byId(int)} 成对，不留第二条编码规则。 */
            public byte id() {
                return (byte) this.id;
            }

            /** 线上码 → 枚举；未知码回落 {@link #CULTIVATOR}（54 号 §5.1 的「未知码」判据）。 */
            public static Kind byId(int id) {
                for (Kind kind : values()) {
                    if (kind.id == id) {
                        return kind;
                    }
                }
                return CULTIVATOR;
            }
        }
    }
}
