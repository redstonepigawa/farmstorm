package com.dashpig.farmstorm.behavior;

/**
 * 6 个部署方块的行为种类（实现维度）。
 * 用枚举而不是 Class 引用，是为了让方块状态（BlockEntity 的 kind）可序列化，也让注册层的枚举 codec 免费可用。
 *
 * <p>它既不是类型（{@code CropType}）也不是类别（{@code CropCategory}）：C-4 的「分类是炮台、实现是光环」
 * 就是本枚举与类别标签分离的实例，tooltip 的类别行也已改由 {@code CropProfile#categories()} 提供。
 */
public enum DeployedKind {
    BLAST_CARROT(10),
    FANG_TRAP_CACTUS(10),
    PUMPKIN_TURRET(10),
    AWAKENED_TORCHFLOWER(100),
    AWAKENED_PITCHER_PLANT(100),
    ZANGIEF_CANE(10);

    private final int scanInterval;

    DeployedKind(int scanInterval) {
        this.scanInterval = scanInterval;
    }

    /**
     * 主扫描间隔（tick）：陷阱 / 炮台 10，光环 100（TECH §4.6）。
     * 放在 kind 上而不是行为对象上，是因为 DeployedCropBlockEntity 的错峰 tick 在行为对象就绪前就要用（§4.5）。
     */
    public int scanInterval() {
        return this.scanInterval;
    }
}
