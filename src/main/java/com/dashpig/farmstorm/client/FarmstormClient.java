package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.client.render.TurretBulletRenderer;
import com.dashpig.farmstorm.config.FarmstormClientConfig;
import com.dashpig.farmstorm.registry.FarmstormEntityTypes;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import com.dashpig.farmstorm.registry.FarmstormRecipes;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * 客户端入口（TECH §2 client 分包的唯一汇合点）。
 * 用 @EventBusSubscriber(Dist.CLIENT) 做边界隔离：专用服务器不会加载本类，因此不会因为缺客户端类而崩。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class FarmstormClient {
    /** 13 个实体类型（12 投掷物 + 1 炮台导弹），顺序即日志顺序；自检用（FIX-1）。 */
    private static final List<RegistryObject<? extends EntityType<?>>> ALL_ENTITY_TYPES = List.of(
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

    /**
     * 上一次看到的世界维度（F-240）：连线包**不带维度**（同维度的绑定才成立，36 号 §2.8.1），
     * 因此「这张表属于哪个维度」只能由客户端自己记，变了就 {@code clear()}，
     * 否则换维度后旧线会画在新世界的同一组坐标上（D-169 补充 / 35 号 §12 B-38）。
     */
    private static ResourceKey<Level> lastDimension;

    private FarmstormClient() {}

    // mod 总线的三条注册（界面 / 配方书分类 / 实体渲染器）在 FarmstormClientSetup：
    // 1.20.1 的 @Mod.EventBusSubscriber#bus 是单值，一个类只能挂一条总线。

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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        WildHunterClientHooks.onClientTick();
        // F-202：两个 KeyMapping 的消费点，与狂野猎人的客户端逐刻驱动共用这一个 TickEvent.ClientTickEvent。
        // while 而不是 if：一 tick 内连按两次要两次都生效（consumeClick 是队列语义）。
        while (FarmstormKeys.TOGGLE.consumeClick()) {
            toggleCoreHud();
        }
        while (FarmstormKeys.DRAG.consumeClick()) {
            Minecraft.getInstance().setScreen(new CoreHudDragScreen());
        }
        // 30 号 §8 ⑧ 的退路：LoggingOut 在部分路径下可能不触发，这里用「不在世界里」再兜一次。
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            ClientCoreList.clear();
            ClientCultivatorLinks.clear();
            lastDimension = null;
        } else {
            // F-240 / D-169 补充：换维度清空（35 号 §12 B-38）。客户端不跨维度缓存。
            ResourceKey<Level> dimension = level.dimension();
            if (!dimension.equals(lastDimension)) {
                lastDimension = dimension;
                ClientCultivatorLinks.clear();
            }
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
        // F-240：换服 / 断线清连线表与维度记录（D-169 补充：不缓存上一次会话）。
        ClientCultivatorLinks.clear();
        lastDimension = null;
        // F-82：灰度链是自持资源，断线时释放，避免把上一个世界的 GL 资源带过去。
        GrayscalePass.reset();
    }

    /**
     * 剪影要在灰度**之后**画，否则会被后处理一起灰掉（V-57）。
     * 灰度由原版 PostChain 在世界渲染结束后执行（{@code GameRenderer:1027}），所以：
     * 世界阶段只负责**捕获**投影/相机/modelview（AFTER_ENTITIES），真正重画发生在 GUI 阶段。
     */
    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        WildHunterOutline.onRenderStage(event);
        // F-240：连线线框（AFTER_PARTICLES）。挂在这里而不是新建订阅点，是因为
        // RenderLevelStageEvent 每阶段都发一次，两个消费者各自在体内判 stage 即可。
        CultivatorLinkRenderer.onRenderStage(event);
        // F-82：灰度改为自持 PostChain，在 AFTER_LEVEL 直接 process（不再依赖 GameRenderer 的后处理状态）。
        GrayscalePass.onRenderStage(event);
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
