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

    /** 单条连线 2 字段 codec：两个 {@code BlockPos} 组合成二元 {@code composite}。 */
    public static final StreamCodec<FriendlyByteBuf, Link> LINK_STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, Link::core,
            BlockPos.STREAM_CODEC, Link::cultivator,
            Link::new);

    /** 一元 {@code composite} + 列表 codec（1 元重载，与 {@link CoreListPayload} 同一体例）。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, CultivatorLinkPayload> STREAM_CODEC = StreamCodec.composite(
            LINK_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINKS)), CultivatorLinkPayload::links,
            CultivatorLinkPayload::new);

    /** 一条连线：核的坐标 + 培养器的坐标。两个都是方块坐标，画线时各取格心（+0.5）。 */
    public record Link(BlockPos core, BlockPos cultivator) {}

    @Override
    public Type<CultivatorLinkPayload> type() {
        return TYPE;
    }
}
