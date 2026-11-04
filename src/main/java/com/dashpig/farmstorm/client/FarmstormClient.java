package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.client.render.TurretBulletRenderer;
import com.dashpig.farmstorm.config.FarmstormClientConfig;
import com.dashpig.farmstorm.registry.FarmstormEntityTypes;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 客户端入口（TECH §2 client 分包的唯一汇合点）。
 * 用 @EventBusSubscriber(Dist.CLIENT) 做边界隔离：专用服务器不会加载本类，因此不会因为缺客户端类而崩。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class FarmstormClient {
    /** 13 个实体类型（12 投掷物 + 1 炮台导弹），顺序即日志顺序；自检用（FIX-1）。 */
    private static final List<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> ALL_ENTITY_TYPES = List.of(
            FarmstormEntityTypes.PUFFED_WHEAT,
            FarmstormEntityTypes.UBI_SERVER,
            FarmstormEntityTypes.TOXIC_UBI_SERVER,
            FarmstormEntityTypes.MELON_SLICE_BOMB,
            FarmstormEntityTypes.MELON_GRENADE,
            FarmstormEntityTypes.BAMBOO_FIRECRACKER,
            FarmstormEntityTypes.RED_MUSHROOM_PUFF,
            FarmstormEntityTypes.BROWN_MUSHROOM_PUFF,
            FarmstormEntityTypes.MOLOTOV,
            FarmstormEntityTypes.COMPRESSED_PUFFED_WHEAT,
            FarmstormEntityTypes.FRIEND_CHIVES,
            FarmstormEntityTypes.VIBRATION_BEAN,
            FarmstormEntityTypes.TURRET_BULLET);

    private FarmstormClient() {}

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(FarmstormMenus.CONVERTER.get(), ConverterScreen::new);
        // F-227 / D-166：54 格存储界面。漏掉这一行 ⇒ 打开界面时崩或黑屏（36 号 §2.6.3）。
        event.register(FarmstormMenus.OUTPOST_CORE.get(), OutpostCoreScreen::new);
    }

    /**
     * 自定义配方类型 {@code farmstorm:converting} 不在 vanilla 的配方书 switch 里，
     * 不注册 finder 就会每个配方刷一条 {@code Unknown recipe category} WARN。
     * 返回 {@code UNKNOWN}（不进任何 tab）而不是 {@code CRAFTING_MISC}：
     * 转换配方只能在转换器方块里做，塞进工作台 misc tab 会让用户看着能合成却摆不进合成格。
     */
    @SubscribeEvent
    public static void onRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        ConverterRecipeBookCategory.onRegisterRecipeBookCategories(event);
    }

    /**
     * F-79：狂野猎人的冲刺是**客户端预测**（BRIEF §7.6）——按下攻击键时本地立刻算位移并冲出去；
     * 服务端在 {@code AttackEntityEvent} 上用同一套 {@code resolveTarget} 独立复核（F-80）。
     * 这里只做按键接线，位移数学在 {@code combat/WildHunterDash}（纯函数，可单测）。
     */
    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            WildHunterDashClient.onAttackKey();
        }
    }

    /**
     * 狂野猎人的客户端逐刻驱动：冲刺推进（F-79）+ 循环音跟随（F-84）+ 状态快照。
     * 三个功能的钩子都汇合在 {@link WildHunterClientHooks}，本类只做事件接线。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        WildHunterClientHooks.onClientTick();
        // F-202：两个 KeyMapping 的消费点，与狂野猎人的客户端逐刻驱动共用这一个 ClientTickEvent.Post。
        // while 而不是 if：一 tick 内连按两次要两次都生效（consumeClick 是队列语义）。
        while (FarmstormKeys.TOGGLE.consumeClick()) {
            toggleCoreHud();
        }
        while (FarmstormKeys.DRAG.consumeClick()) {
            Minecraft.getInstance().setScreen(new CoreHudDragScreen());
        }
        // 30 号 §8 ⑧ 的退路：LoggingOut 在部分路径下可能不触发，这里用「不在世界里」再兜一次。
        if (Minecraft.getInstance().level == null) {
            ClientCoreList.clear();
        }
    }

    /**
     * 按 H 开关浮窗（F-202）：写 {@code hud.enabled} 并存盘，动作栏回显。
     * 回显复用 {@code /farmstorm hud on|off} 的两条反馈键，**不额外占语言键**（本版语言键正好 323）。
     */
    private static void toggleCoreHud() {
        boolean enabled = !FarmstormClientConfig.hudEnabled.get();
        FarmstormClientConfig.saveHudState(enabled,
                FarmstormClientConfig.hudX.get(), FarmstormClientConfig.hudY.get());
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable(enabled
                    ? CoreHudClientCommands.FEEDBACK_ON
                    : CoreHudClientCommands.FEEDBACK_OFF), true);
        }
    }

    /** F-205：客户端指令 {@code /farmstorm hud <on|off|reset>} 的注册（game 总线、仅客户端）。 */
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CoreHudClientCommands.register(event.getDispatcher());
    }

    /**
     * F-200 / 30 号 §7 #29：退出服务器时清空快照，否则换服后会看到上一个服务器的核
     * （本版唯一会跨会话残留的客户端状态）。
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientCoreList.clear();
    }

    /**
     * 剪影要在灰度**之后**画，否则会被后处理一起灰掉（V-57）。
     * 灰度由原版 PostChain 在世界渲染结束后执行（{@code GameRenderer:1027}），所以：
     * 世界阶段只负责**捕获**投影/相机/modelview（AFTER_ENTITIES），真正重画发生在 GUI 阶段。
     */
    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        WildHunterOutline.onRenderStage(event);
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        WildHunterOutline.onRenderGuiPre();
    }

    /**
     * 13 个实体类型缺一个，{@code EntityRenderDispatcher#shouldRender} 就会拿到 null 并在渲染期 NPE 崩客户端（FIX-1）。
     * 12 个投掷物沿用原版雪球 / 鸡蛋的做法：{@link ThrownItemRenderer} 直接渲染 {@code ThrowableItemProjectile}
     * 同步过来的物品模型，因此不需要为本模组新增任何模型 JSON 或 PNG。
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FarmstormEntityTypes.PUFFED_WHEAT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.UBI_SERVER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.TOXIC_UBI_SERVER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.MELON_SLICE_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.MELON_GRENADE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.BAMBOO_FIRECRACKER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.RED_MUSHROOM_PUFF.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.BROWN_MUSHROOM_PUFF.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.MOLOTOV.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.COMPRESSED_PUFFED_WHEAT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.FRIEND_CHIVES.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.VIBRATION_BEAN.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(FarmstormEntityTypes.TURRET_BULLET.get(), TurretBulletRenderer::new);
        logRendererSelfCheck();
    }

    /**
     * 渲染器自检（FIX-1 要求 4）：以后往 registry 里加实体而忘了加渲染器时，客户端启动日志会直接报出来，
     * 不必等投掷物出现才崩。与 {@link Farmstorm#logSelfCheck()} 同规：只打日志，不抛异常。
     */
    private static void logRendererSelfCheck() {
        int expected = FarmstormEntityTypes.REGISTER.getEntries().size();
        boolean ok = ALL_ENTITY_TYPES.size() == expected;
        Farmstorm.LOGGER.info("自检 实体渲染器 {}/{} → {}", ALL_ENTITY_TYPES.size(), expected, ok ? "OK" : "FAIL");
        Farmstorm.LOGGER.info("自检 已注册渲染器的实体 ID：{}",
                ALL_ENTITY_TYPES.stream().map(holder -> holder.getId().toString()).toList());
        if (!ok) {
            Farmstorm.LOGGER.error("自检 实体类型有 {} 个，渲染器只注册了 {} 个；漏掉的类型一旦出现在世界里，"
                    + "EntityRenderDispatcher 会拿到 null 渲染器并 NPE 崩客户端", expected, ALL_ENTITY_TYPES.size());
        }
    }
}
