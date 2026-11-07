package com.dashpig.farmstorm.util;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

/**
 * 作物品质三档（DEC §4.1 的 11 / 7 / 4 = 22，D-149 / D-150）。
 *
 * <p>品质是**档案级静态属性**：同一个注册 ID 的任何一摞、任何状态都是同一档，
 * 不随玩家 / 维度 / 时间变化。因此它既不进数据组件（{@code data_components} 仍 1）、
 * 也不写物品 NBT、更不新开注册对象——载体只有 {@link CropProfile} 的第 9 字段。
 *
 * <p>三档的配色借用原版 {@link ChatFormatting} 的白 / 蓝 / 金（D-151），
 * 但**不是**原版的 {@code Rarity}：{@code Rarity} 的档位是白 / 黄 / 青 / 紫，与本版三档对不上，
 * 复用它会连带接管名色与 tooltip 格式，之后再想加第四档就无路可走。
 */
public enum CropTier implements StringRepresentable {
    /** 普通：11 条，幸运浆果池的全体（D-156）。 */
    COMMON(ChatFormatting.WHITE),
    /** 稀有：7 条= plan 的 4 条 + 访谈补的 3 条（D-149 补充）。 */
    RARE(ChatFormatting.BLUE),
    /** 史诗：4 条，本版唯一的入口是农民交易（D-162 把它们挡在培养器外）。 */
    EPIC(ChatFormatting.GOLD);

    private final ChatFormatting color;

    CropTier(ChatFormatting color) {
        this.color = color;
    }

    /** 序列化名即语言键后缀，两者共用一份小写名，避免各写一遍（照 {@link CropCategory} 的体例）。 */
    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** 物品提示里的档名文案键；3 个键由 DataGen 写入中英两份语言文件。 */
    public String translationKey() {
        return "tooltip.farmstorm.tier." + getSerializedName();
    }

    /** 档色：普通白 / 稀有蓝 / 史诗金（D-151）。 */
    public ChatFormatting color() {
        return this.color;
    }

    /**
     * 把档色套到物品名这个 {@link Component} 上（D-151 补充）。
     * 收在枚举里是为了让 3 个物品类共用一个实现，不在三处各写一遍 {@code withStyle}。
     */
    public Component nameStyle(Component base) {
        return base.copy().withStyle(this.color);
    }
}
