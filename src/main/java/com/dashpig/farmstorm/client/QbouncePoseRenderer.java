package com.dashpig.farmstorm.client;

/**
 * Q弹 的 {@code render} 分支渲染（TECH §4.2）：坐姿模型、压低 0.35 格、约 480 度/秒旋转、6 刻周期的压缩拉伸。
 * 三个待验证的渲染 API 与 PoseStack 的 push/pop 平衡是 RT-17 的缓释点。
 *
 * <p>F-103 只立编译期骨架；渲染逻辑由 F-108 填入。
 */
public final class QbouncePoseRenderer {

    private QbouncePoseRenderer() {}
}
