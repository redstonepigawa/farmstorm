package com.probe.probe1201;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** 1.20.1 的网络面：SimpleChannel + 手写 encode/decode（没有 StreamCodec / CustomPacketPayload）。 */
public final class ProbeNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ProbeMod.MOD_ID, "main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals);

    private ProbeNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, ProbeMessage.class, ProbeMessage::encode, ProbeMessage::decode, ProbeMessage::handle);
    }

    public static void sendTo(ServerPlayer player, BlockPos pos, String text) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ProbeMessage(pos, text));
    }

    public record ProbeMessage(BlockPos pos, String text) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(this.pos);
            buf.writeUtf(this.text);
        }

        public static ProbeMessage decode(FriendlyByteBuf buf) {
            return new ProbeMessage(buf.readBlockPos(), buf.readUtf());
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ProbeClientState.lastPos = this.pos;
                ProbeClientState.lastText = this.text;
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
