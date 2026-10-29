package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.behavior.impl.BlastCarrotTrap;
import com.dashpig.farmstorm.behavior.impl.FangTrapCactusTrap;
import com.dashpig.farmstorm.behavior.impl.PitcherPlantAura;
import com.dashpig.farmstorm.behavior.impl.PumpkinTurret;
import com.dashpig.farmstorm.behavior.impl.TorchflowerAura;
import com.dashpig.farmstorm.behavior.impl.ZangiefCaneTrap;

/**
 * 6 个部署行为单例的工厂（TECH §4.6）。
 * 用 switch 表达式而不是 Map：枚举新增成员时编译器会直接报「不完整」，不会漏登记。
 */
public final class DeployedBehaviors {
    private static final BlastCarrotTrap BLAST_CARROT_TRAP = new BlastCarrotTrap();
    private static final FangTrapCactusTrap FANG_TRAP_CACTUS_TRAP = new FangTrapCactusTrap();
    private static final PumpkinTurret PUMPKIN_TURRET_BEHAVIOR = new PumpkinTurret();
    private static final TorchflowerAura TORCHFLOWER_AURA = new TorchflowerAura();
    private static final PitcherPlantAura PITCHER_PLANT_AURA = new PitcherPlantAura();
    private static final ZangiefCaneTrap ZANGIEF_CANE_TRAP = new ZangiefCaneTrap();

    private DeployedBehaviors() {}

    /** 静态单例：行为对象无状态，整服共用一份（§4.5「不参与序列化」）。 */
    public static DeployedBehavior of(DeployedKind kind) {
        return switch (kind) {
            case BLAST_CARROT -> BLAST_CARROT_TRAP;
            case FANG_TRAP_CACTUS -> FANG_TRAP_CACTUS_TRAP;
            case PUMPKIN_TURRET -> PUMPKIN_TURRET_BEHAVIOR;
            case AWAKENED_TORCHFLOWER -> TORCHFLOWER_AURA;
            case AWAKENED_PITCHER_PLANT -> PITCHER_PLANT_AURA;
            case ZANGIEF_CANE -> ZANGIEF_CANE_TRAP;
        };
    }
}
