package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 部署物行为家族（TECH §4.6 / DEC C-4）。
 * 实现一律是无状态静态单例（见 {@link DeployedBehaviors}）：状态全部挂在 BlockEntity 上，
 * 因此同一区块里 8 个炮台共用一份对象，不会因为「一个方块一个行为实例」多出堆开销。
 */
public interface DeployedBehavior {

    DeployedKind kind();

    /** 主扫描间隔（tick）。陷阱 / 炮台 10，光环 100。 */
    int scanInterval();

    void tick(DeployedCropBlockEntity be);

    /** 部署成功后的首次钩子（M3 / M4 的投掷物与转换器需要初始化自身状态时用）。 */
    default void onDeployed(DeployedCropBlockEntity be) {}

    /** 破坏权限的扩展点；破坏判定本身在方块层（§4.6 末段：getDestroyProgress 拿不到行为对象）。 */
    default boolean canBeBrokenBy(Player player, DeployedCropBlockEntity be) {
        return true;
    }
}
