package com.dashpig.farmstorm.util;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/**
 * 01-DESIGN §2.2 的三个类型（Type）：投掷型 / 部署型 / 食用型。
 *
 * <p>类型是**单值**：「怎么用」。它与 {@code CropCategory}（类别：战斗里干什么）是两个正交维度
 * （DESIGN §2.4），两者不得互相推导，也不由 {@code DeployedKind} 代替——后者是部署行为的实现种类。
 *
 * <p>§2.2.3 的「种植型」刻意没有常量：它是横切能力，15 种作物全部有播种形态（D-10 不新增设计），
 * 给它一个值只会制造「哪种作物是种植型」的伪问题。
 */
public enum CropType implements StringRepresentable {
    /** 投掷型：右键生成投掷物实体（8 个）。 */
    THROWABLE,
    /** 部署型：放下后成为带 BlockEntity 的部署方块（5 个）。 */
    DEPLOYABLE,
    /** 食用型：走原版进食流程（2 个）。 */
    EDIBLE;

    public static final Codec<CropType> CODEC = StringRepresentable.fromEnum(CropType::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
