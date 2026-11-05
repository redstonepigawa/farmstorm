package com.dashpig.farmstorm.network;

import com.dashpig.farmstorm.Farmstorm;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 全项目第 **2** 个自定义网络包（D-169 / D-172 / 36 号 §2.8.1）：服务端 → 客户端单向推送
 * 「**当前维度内、玩家 3 × 3 区块范围覆盖到的**核 ↔ 培养器绑定关系」。
 *
 * <p>与 {@link CoreListPayload} 是**两类数据**（D-169 明确否决合并）：{@code core_list} 是己方核的
 * **全量快照**（{@code MAX_CORES = 64}、三级排序、指纹去抖、带 {@code total}）；本包是**逐玩家、按范围过滤、
 * 随绑定变化**的增量视图。塞进同一个 codec 会让两种语义混在一起，去抖与指纹都要重写。
 *
 * <p><b>为什么不带 {@code total}</b>：{@code core_list} 的 {@code total} 是「截断前的真实总数」；
 * 本包的列表是**按玩家过滤后的完整集合**，永不截断 —— {@link #MAX_LINKS} 只是**解码上限**，
 * 不当截断值用（36 号 §2.8.1）。
 *
 * <p><b>为什么不带维度</b>：培养器与核必同维度（D-165 补充，跨维度的绑定不成立），
 * 所以「所在维度」由收件人自己当前的维度承载，不上线。
 *
 * <p>{@code type()} 走 {@link Farmstorm#id(String)} 而不是 {@code createType(String)}
 * ——后者会落进 {@code minecraft:} 命名空间（30 号 §5.5 第 1 条）。
 */
public record CultivatorLinkPayload(List<Link> links) implements CustomPacketPayload {
    /**
     * 单包条目的**解码上限**（36 号 §2.8.1 的算术）：{@code maxPerPlayer} 上界 64 ×
     * {@code cultivatorCapacity} 上界 64 = 4096 是理论最坏值；本常量取 1024 是**解码保护**，
     * 不按默认值推导。新装默认下每玩家 4 核 × 24 = 96，远小于本上限。
     */
    public static final int MAX_LINKS = 1024;

    /** 注册名必须是 {@code farmstorm:cultivator_link}（36 号 §8 #2 的注册行判据）。 */
    public static final Type<CultivatorLinkPayload> TYPE = new Type<>(Farmstorm.id("cultivator_link"));

    /**
     * 单条连线 **3 字段** codec（F-302 / 54 号 §5.1）：两个 {@code BlockPos} + 一个 {@code Kind} 字节，
     * 三元 {@code composite}。第三段 {@code ByteBufCodecs.BYTE} 只占 **1 字节**，列表 codec 与
     * {@link #MAX_LINKS} 一个字符不动（D-210 第 1 条）。
     */
    public static final StreamCodec<FriendlyByteBuf, Link> LINK_STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, Link::core,
            BlockPos.STREAM_CODEC, Link::cultivator,
            ByteBufCodecs.BYTE.map(Link.Kind::byId, Link.Kind::id), Link::kind,
            Link::new);

    /** 一元 {@code composite} + 列表 codec（1 元重载，与 {@link CoreListPayload} 同一体例）。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, CultivatorLinkPayload> STREAM_CODEC = StreamCodec.composite(
            LINK_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINKS)), CultivatorLinkPayload::links,
            CultivatorLinkPayload::new);

    /**
     * 一条连线：核的坐标 + 机器的坐标 + **连着哪一类机器**。三个都是线上字段，画线时两侧各取格心（+0.5）。
     *
     * <p>{@link Kind} 之所以叫「kind」而不是「color」：语义是「这条线连着哪一类机器」，颜色只是它的后果；
     * 将来第三种机器只加一个枚举成员（D-210 补充）。
     */
    public record Link(BlockPos core, BlockPos cultivator, Kind kind) {
        /**
         * 连线类型（F-302 / D-210 / 54 号 §5.1）：线上各占 **1 字节**，{@code CULTIVATOR = 0}、
         * {@code CROP_CONVERTER = 1}。未知码按 {@link #CULTIVATOR} 解——第三类机器的服务端连到旧客户端时，
         * 画出来是既有的青色线，而不是崩或画错色（前向兼容）。
         */
        public enum Kind {
            CULTIVATOR(0),
            CROP_CONVERTER(1);

            private final int id;

            Kind(int id) {
                this.id = id;
            }

            /** 线上码：{@code BYTE} 只占 1 字节，与 {@link #byId(int)} 成对，不留第二条编码规则。 */
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

    @Override
    public Type<CultivatorLinkPayload> type() {
        return TYPE;
    }
}
