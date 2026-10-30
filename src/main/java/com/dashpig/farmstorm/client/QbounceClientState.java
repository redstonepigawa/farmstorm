package com.dashpig.farmstorm.client;

/**
 * Q弹 的客户端状态机（TECH §3.12）：按「效果还在不在」驱动姿态分支与旋转 / 压缩相位。
 * 服务端权威（HC-03）：本类只读本客户端的实体效果，不回写任何服务端状态。
 *
 * <p>F-103 只立编译期骨架；三个镜像常量与状态迁移由 F-107 填入。
 */
public final class QbounceClientState {

    private QbounceClientState() {}
}
