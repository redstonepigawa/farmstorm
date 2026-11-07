package com.probe.probe1201;

import net.minecraft.core.BlockPos;

/** 探针读数用：客户端收到的那一条消息。 */
public final class ProbeClientState {
    public static volatile BlockPos lastPos;
    public static volatile String lastText;

    private ProbeClientState() {
    }
}
