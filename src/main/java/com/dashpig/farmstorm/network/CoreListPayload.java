package com.dashpig.farmstorm.network;

import com.dashpig.farmstorm.Farmstorm;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * 分项目**唯一**一个自定义网络包（D-142 / 30 号 §2.1.2）：服务端 → 客户端单向推送「己方核心列表」。
 *
 * <p>客户端只读、不回包、不做任何判定。注册名由 {@link #ID} 给出，走 {@link Farmstorm#id(String)}，
 * **不落进** {@code minecraft:} 命名空间（30 号 §5.5 第 1 条）。
 *
 * <p><b>1.20.1 没有 {@code CustomPacketPayload} / {@code StreamCodec}</b>：按 docs/zh/02-TECH-1.20.1.md §4.3
 * 改手写 {@link FriendlyByteBuf} 编解码，字段顺序与原 {@code composite} 一字不差
 * （dimension → pos → ownerName → self，然后 total）。
 *
 * <p>{@code total} **单独发**，它是**截断前**的真实总数：{@code cores.size()} 在满员时恒为 {@link #MAX_CORES}，
 * 答不出「+N」要的那个 N（30 号 §5.5 第 2 条 / A-34）。
 */
public record CoreListPayload(List<Core> cores, int total) {
    /** 单包封顶（30 号 §2.1.3）。列表解码同时拿它当上限，超出的条目由服务端截断后再发。 */
    public static final int MAX_CORES = 64;

    /** 注册名必须是 {@code farmstorm:core_list}（30 号 §7 #6 的注册行判据）。 */
    public static final ResourceLocation ID = Farmstorm.id("core_list");

    /** 写包体。 */
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(cores.size());
        for (Core core : cores) {
            core.encode(buffer);
        }
        buffer.writeVarInt(total);
    }

    /** 读包体；条数按 {@link #MAX_CORES} 封顶（解码保护，与 1.21.1 的 {@code ByteBufCodecs.list(max)} 同口径）。 */
    public static CoreListPayload decode(FriendlyByteBuf buffer) {
        int size = Math.min(buffer.readVarInt(), MAX_CORES);
        List<Core> cores = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            cores.add(Core.decode(buffer));
        }
        return new CoreListPayload(List.copyOf(cores), buffer.readVarInt());
    }

    /**
     * 单条核心：维度 + 坐标 + 归属者显示名 + 是否本人（D-143 的「维度 + 坐标 + 归谁 + 是否自己」）。
     * 排序由列表顺序承载，因此本 record 不带 {@code gameTime}——它是服务端的比较键，不上线。
     */
    public record Core(ResourceLocation dimension, BlockPos pos, String ownerName, boolean self) {
        void encode(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(dimension);
            buffer.writeBlockPos(pos);
            buffer.writeUtf(ownerName);
            buffer.writeBoolean(self);
        }

        static Core decode(FriendlyByteBuf buffer) {
            return new Core(buffer.readResourceLocation(), buffer.readBlockPos(), buffer.readUtf(), buffer.readBoolean());
        }
    }
}
