package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.client.render.TurretBulletRenderer;
import com.dashpig.farmstorm.registry.FarmstormEntityTypes;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import java.util.List;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.io.IOException;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 客户端入口（TECH §2 client 分包的唯一汇合点）。
 * 用 @EventBusSubscriber(Dist.CLIENT) 做边界隔离：专用服务器不会加载本类，因此不会因为缺客户端类而崩。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class FarmstormClient {
    /** 11 个实体类型（10 投掷物 + 1 炮台导弹），顺序即日志顺序；自检用（FIX-1）。 */
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
            FarmstormEntityTypes.TURRET_BULLET);

    private FarmstormClient() {}

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(FarmstormMenus.CONVERTER.get(), ConverterScreen::new);
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
    }

    /** 灰度后处理（F-82）的 shader 必须在资源重载期注册，否则 pass 无从创建。 */
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        WildHunterClientHooks.onRegisterShaders(event);
    }

    /** 整屏灰度要连 GUI 一起变灰（F-82 验收 2），所以挂在 RenderGuiEvent.Post。 */
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        WildHunterClientHooks.onRenderGuiPost();
    }

    /** 红色剪影（F-83）：只对可见且 64 格内的目标生效。 */
    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        WildHunterClientHooks.onRenderLivingPost(event);
    }

    /**
     * 11 个实体类型缺一个，{@code EntityRenderDispatcher#shouldRender} 就会拿到 null 并在渲染期 NPE 崩客户端（FIX-1）。
     * 10 个投掷物沿用原版雪球 / 鸡蛋的做法：{@link ThrownItemRenderer} 直接渲染 {@code ThrowableItemProjectile}
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
