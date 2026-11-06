package com.dashpig.farmstorm.blockentity;

import net.minecraft.core.BlockPos;

/**
 * 核绑定契约（D-228）：机器侧引用缓存的三条操作——读缓存 / 写缓存 / 清缓存。
 *
 * <p>只声明这三个方法，不声明第四个（D-228、D-234）：绑定关系的唯一真相仍在核侧（D-165），
 * 本接口只是机器侧缓存的读写面。它**不承担类型**——「这台机器是哪一个类型」由核侧的平行列表
 * 与 NBT 旁挂键记录，写进接口就会出现第二份真相。
 *
 * <p>可空语义留在实现类各自的 {@code @Nullable} 上（D-165：{@code null} = 未绑定），
 * 接口不引入新的注解来源；也不抽共同基类（D-228）——两个实现的状态机各自成体系。
 */
public interface CoreBound {
    /** 引用缓存只读视图；{@code null} = 未绑定。 */
    BlockPos corePos();

    /** 写入引用缓存：调用方先把核侧列表写成功，再调这里（两侧不一致时按未绑定处理）。 */
    void setCore(BlockPos pos);

    /** 清掉引用缓存（核被拆且没有接替核时的清理出口）；槽内内容与进度保留（D-168）。 */
    void clearCore();
}
