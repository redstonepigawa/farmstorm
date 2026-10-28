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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 客户端入口（TECH §2 client 分包的唯一汇合点）。
 * 用 @EventBusSubscriber(Dist.CLIENT) 做边界隔离：专用服务器不会加载本类，因此不会因为缺客户端类而崩。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class FarmstormClient {
    /** 9 个实体类型（8 投掷物 + 1 炮台导弹），顺序即日志顺序；自检用（FIX-1）。 */
    private static final List<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> ALL_ENTITY_TYPES = List.of(
            FarmstormEntityTypes.PUFFED_WHEAT,
            FarmstormEntityTypes.UBI_SERVER,
            FarmstormEntityTypes.TOXIC_UBI_SERVER,
            FarmstormEntityTypes.MELON_SLICE_BOMB,
            FarmstormEntityTypes.MELON_GRENADE,
            FarmstormEntityTypes.BAMBOO_FIRECRACKER,
            FarmstormEntityTypes.RED_MUSHROOM_PUFF,
            FarmstormEntityTypes.BROWN_MUSHROOM_PUFF,
            FarmstormEntityTypes.TURRET_BULLET);

    private FarmstormClient() {}

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(FarmstormMenus.CONVERTER.get(), ConverterScreen::new);
    }

    /**
     * 9 个实体类型缺一个，{@code EntityRenderDispatcher#shouldRender} 就会拿到 null 并在渲染期 NPE 崩客户端（FIX-1）。
     * 8 个投掷物沿用原版雪球 / 鸡蛋的做法：{@link ThrownItemRenderer} 直接渲染 {@code ThrowableItemProjectile}
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
