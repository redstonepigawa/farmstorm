package com.dashpig.farmstorm.network;

import com.dashpig.farmstorm.Farmstorm;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 全项目**唯一**一个自定义网络包（D-142 / 30 号 §2.1.2）：服务端 → 客户端单向推送「己方核心列表」。
 *
 * <p>客户端只读、不回包、不做任何判定。{@code type()} 由 {@link #TYPE} 给出，
 * 而 {@code TYPE} 走 {@link Farmstorm#id(String)}——**不用** {@code createType(String)}，
 * 后者走 {@code ResourceLocation.withDefaultNamespace}，包会落进 {@code minecraft:} 命名空间（30 号 §5.5 第 1 条）。
 *
 * <p>{@code total} **单独发**，它是**截断前**的真实总数：{@code cores.size()} 在满员时恒为 {@link #MAX_CORES}，
 * 答不出「+N」要的那个 N（30 号 §5.5 第 2 条 / A-34）。
 */
public record CoreListPayload(List<Core> cores, int total) implements CustomPacketPayload {
    /** 单包封顶（30 号 §2.1.3）。列表 codec 同时拿它当解码上限，超出的条目由服务端截断后再发。 */
    public static final int MAX_CORES = 64;

    /** 注册名必须是 {@code farmstorm:core_list}（30 号 §7 #6 的注册行判据）。 */
    public static final Type<CoreListPayload> TYPE = new Type<>(Farmstorm.id("core_list"));

    /** 单核 4 字段 codec；两两组合成 4 元 {@code composite}（1.21.1 的 4 元重载，已 javap 核对）。 */
    public static final StreamCodec<FriendlyByteBuf, Core> CORE_STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, Core::dimension,
            BlockPos.STREAM_CODEC, Core::pos,
            ByteBufCodecs.STRING_UTF8, Core::ownerName,
            ByteBufCodecs.BOOL, Core::self,
            Core::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, CoreListPayload> STREAM_CODEC = StreamCodec.composite(
            CORE_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_CORES)), CoreListPayload::cores,
            ByteBufCodecs.VAR_INT, CoreListPayload::total,
            CoreListPayload::new);

    /**
     * 单条核心：维度 + 坐标 + 归属者显示名 + 是否本人（D-143 的「维度 + 坐标 + 归谁 + 是否自己」）。
     * 排序由列表顺序承载，因此本 record 不带 {@code gameTime}——它是服务端的比较键，不上线。
     */
    public record Core(ResourceLocation dimension, BlockPos pos, String ownerName, boolean self) {}

    @Override
    public Type<CoreListPayload> type() {
        return TYPE;
    }
}
