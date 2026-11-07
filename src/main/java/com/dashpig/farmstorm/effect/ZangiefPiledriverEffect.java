package com.dashpig.farmstorm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 桑吉尔蔗打桩机的相位通道（D-50、BRIEF §7.5）——只有一个职责：
 * 把「当前在哪个相位」从服务端送到客户端，靠 amplifier 当相位号，靠原版
 * {@code ClientboundUpdateMobEffectPacket} 免费同步，因此不必新增网络包。
 * <p>不加任何属性、不造成任何伤害、不产生任何粒子、不读 config。
 * <p><b>隐藏靠施加方</b>：1.21.1 的 {@code MobEffect} 没有 {@code shouldRenderHUD()} /
 * {@code shouldRenderInvText()} 之类的类级钩子（TECH §3.6 的 RT-15 待验证项在此关闭），
 * 「不进 HUD、不进物品栏」由 {@code MobEffectInstance} 的
 * {@code visible=false, showIcon=false} 两个构造参数保证——施加它的类（陷阱 / 三相位状态机）
 * 必须用六参构造 {@code new MobEffectInstance(…, false, false)}，见 TECH §5.2 的 t=0 行。
 */
public class ZangiefPiledriverEffect extends MobEffect {
    /** 已倒转、等待漂浮（{@code stageDelayTicks} = 14 刻）。 */
    public static final int PHASE_REVERSED = 0;

    /** 漂浮中。 */
    public static final int PHASE_LEVITATING = 1;

    /** 下坠自转。 */
    public static final int PHASE_FALLING = 2;

    public ZangiefPiledriverEffect() {
        super(MobEffectCategory.HARMFUL, 0x000000);
    }
}
