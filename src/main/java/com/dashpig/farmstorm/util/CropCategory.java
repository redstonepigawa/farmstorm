package com.dashpig.farmstorm.util;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/**
 * 01-DESIGN §2.3 的六个类别（Category）。
 *
 * <p>类别是**多值**的：一个作物可以同时属于多个类别（DESIGN §2.3 首段、§2.4 说明 2），
 * 因此 {@link CropProfile} 用 {@code Set<CropCategory>} 持有，而不是单值枚举。
 * 它与 {@code CropType}（类型：投掷 / 部署 / 食用）是两个正交维度（DESIGN §2.4），
 * 也与 {@code DeployedKind}（部署行为的实现种类）无关——C-4 的「分类是炮台、实现是光环」正是这种情况。
 */
public enum CropCategory implements StringRepresentable {
    /** 陷阱类：静默待机，非己方玩家进入指定距离即触发的一次性部署物。 */
    TRAP,
    /** 炮台类：周期性主动索敌并作用于目标的持续部署物（含 C-4 的光环型炮台）。 */
    TURRET,
    /** 破障类：主要效果能破坏方块（DESIGN §2.3.3）。方块破坏权限由本标签驱动。 */
    DEMOLITION,
    /** buff类：主要效果是施加状态效果（DESIGN §2.3.4 的命名归一）。 */
    BUFF,
    /** 建造类：主要效果是在世界里写入有实质用途的原版方块。 */
    CONSTRUCTION,
    /** 伤害类：效果链中存在明确的伤害来源。 */
    DAMAGE;

    public static final Codec<CropCategory> CODEC = StringRepresentable.fromEnum(CropCategory::values);

    /** 序列化名即 lang 键后缀，两者共用一份小写名，避免各写一遍。 */
    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** 物品提示里的类别文案键；6 个键由 DataGen 写入中英两份语言文件。 */
    public String translationKey() {
        return "tooltip.farmstorm.category." + getSerializedName();
    }
}
