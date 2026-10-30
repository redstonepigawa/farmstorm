package com.dashpig.farmstorm.client;

/**
 * 混乱的客户端输入反转（TECH §5）：只反转方向分量，幅度不变；与 {@code InputLockHandler} 冲突时锁优先（§5.2）。
 *
 * <p>F-103 只立编译期骨架；输入反转与优先级由 F-111 填入。
 */
public final class ConfusionInputHandler {

    private ConfusionInputHandler() {}
}
