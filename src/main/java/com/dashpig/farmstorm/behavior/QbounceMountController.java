package com.dashpig.farmstorm.behavior;

/**
 * Q弹 的 {@code mount} 分支（TECH §4.3）：用隐形的 {@code minecraft:armor_stand} 承载坐姿，随生物死亡立即 discard。
 * 坐骑类型是原版既有实体，因此不新增 entity_type（D-102）。
 *
 * <p>F-103 只立编译期骨架；记账表与四条退出路径由 F-109 填入。
 */
public final class QbounceMountController {

    private QbounceMountController() {}
}
