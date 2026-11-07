package com.probe.probe1201;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 玩家进服时发一条探针消息，用来验证 S2C 往返真的通。 */
@Mod.EventBusSubscriber(modid = ProbeMod.MOD_ID)
public final class ProbeEvents {
    private ProbeEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ProbeNetwork.sendTo(player, player.blockPosition(), "probe-1201 hello");
        }
    }
}
