package com.dashpig.farmstorm.config;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 客户端配置（D-144 第 3 条 / 30 号 §4.2）：**全项目第一份 {@code ModConfig.Type.CLIENT}**。
 *
 * <p>为什么不并进 {@link FarmstormConfig}：两份 spec 的加载侧与落盘位置都不同（{@code config/farmstorm-server.toml}
 * 对 {@code config/farmstorm-client.toml}）。混在一个类里会让既有 166 键的 {@code bake()} 语义变复杂，
 * 而且 server toml 会从 166 变成 169（30 号 §5.5 第 4 条）。本类只依赖 {@code ForgeConfigSpec}（公共类），
 * 因此 {@code Farmstorm} 构造器里引用它不会让专用服务器碰到任何 client-only 类型。
 *
 * <p>3 键都带 {@code translation}：与既有 166 个 server 键一致，否则客户端配置页会露出裸键名。
 * 值域只挡明显坏值——真正的边界在绘制与拖拽时按屏幕尺寸二次钳制（{@code CoreHudRenderer#clampOrigin}）。
 */
public final class FarmstormClientConfig {
    /** 浮窗默认位置：左上角 (4, 4)（BRIEF §2 表下注 / D-144）。 */
    public static final int DEFAULT_HUD_X = 4;
    public static final int DEFAULT_HUD_Y = 4;

    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.BooleanValue hudEnabled;
    public static ForgeConfigSpec.IntValue hudX;
    public static ForgeConfigSpec.IntValue hudY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("hud");
        hudEnabled = builder.comment("浮窗开关（客户端本地设置；按键 H 与 /farmstorm hud 都会改它）")
                .translation("farmstorm.configuration.hud.enabled")
                .define("enabled", true);
        hudX = builder.comment("浮窗左上角的 GUI 像素 x（绘制时还会按屏幕宽二次钳制）")
                .translation("farmstorm.configuration.hud.x")
                .defineInRange("x", DEFAULT_HUD_X, 0, 3840);
        hudY = builder.comment("浮窗左上角的 GUI 像素 y（绘制时还会按屏幕高二次钳制）")
                .translation("farmstorm.configuration.hud.y")
                .defineInRange("y", DEFAULT_HUD_Y, 0, 2160);
        builder.pop();
        SPEC = builder.build();
    }

    private FarmstormClientConfig() {}

    /**
     * mod 总线：CLIENT spec 加载 / 重载后打一条读数（F-201 的验收面 #2 / #3 要一条「hud 位置读入 (x, y)」）。
     * 专用服务器不加载 CLIENT 类型，因此这条在服务端永远不会出现——那本身就是「服务器不生成这一份」的旁证。
     */
    public static void onLoading(ModConfigEvent event) {
        // Loading 阶段 spec 还没 bake，此时读 ConfigValue 会抛 IllegalStateException 并让客户端进入
        // 「broken mod state」（实测踩过）。因此先问 isLoaded——与 FarmstormConfig 的 #value() 同一手式。
        if (event.getConfig().getSpec() != SPEC || !SPEC.isLoaded()) {
            return;
        }
        Farmstorm.LOGGER.info("hud 位置读入 ({}, {})，开关 {}（config/farmstorm-client.toml）",
                hudX.get(), hudY.get(), hudEnabled.get());
    }

    /** 运行期写盘（30 号 §2.4.2）：{@code ConfigValue#set} + {@code SPEC#save}，不手写 {@code Files.write}。 */
    public static void saveHudState(boolean enabled, int x, int y) {
        hudEnabled.set(enabled);
        hudX.set(x);
        hudY.set(y);
        SPEC.save();
    }
}
