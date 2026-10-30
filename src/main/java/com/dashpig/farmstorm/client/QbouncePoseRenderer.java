package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/**
 * Q弹 的 {@code render} 分支（TECH §4.2 / F-108）：只改这一帧的 {@link PoseStack} 与模型实例字段，不碰实体——
 * AI、碰撞箱、寻路、同步全不受影响，出错的影响面限于画面（RT-17）。
 *
 * <p><b>进入条件</b>只有 {@code hasEffect(farmstorm:q_bounce)}，外加一条排除：实体已经是乘客时不叠加。
 * 后者是 mount 分支（F-109）的防双写——乘客的原版渲染器已经按 {@code isPassenger()} 给出真实骑乘姿势，
 * 再压一次就变成「沉 0.35 格 + 又转一圈」的双重变换。
 *
 * <p><b>参数来源</b>：三个姿态参数一律取 {@link QbounceClientState} 的编译期镜像常量（§3.12），本类不读 config——
 * {@code farmstorm-server.toml} 在专用服务器上客户端读不到，且服务端不下发姿态参数（D-81）。
 * 相位只有一份（本机玩家的本地累加刻数），因此同屏多个受效果生物共用同一个旋转角与压缩相位。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class QbouncePoseRenderer {

    /** 压低量（格）：TECH §4.2 的补充值（可调范围 0–1 格），把模型整体下沉以贴合坐姿的臀部高度。 */
    private static final double SINK_Y = -0.35D;

    private QbouncePoseRenderer() {}

    /**
     * {@code pushPose} 与 {@code popPose} 的 {@code push} 端（RT-17 的验法③）。
     * <p>用 {@link EventPriority#LOWEST}：本订阅者最后拿到事件，因此「别人已取消」这件事能读到，
     * 而 {@code Pre} 被取消时原版不会发 {@code Post}（{@code LivingEntityRenderer#render} 直接返回），
     * 那样 push 了就永远漏一个 PoseStack 帧——所以取消时直接不 push。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPre(RenderLivingEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (!appliesTo(entity) || event.isCanceled()) {
            return;
        }
        // 坐姿：只改这一帧的模型实例字段（模型实例由渲染器持有、同种生物共用）。
        // 21.1.213 的实测字节码：本事件在 render 入口（偏移 3）触发，而 model.riding 在偏移 89 由
        // isPassenger() && getVehicle().shouldRiderSit() 重写，所以这一行对原版模型不生效——真实坐姿只在
        // mount 分支（乘客）出现。保留它是 TECH §4.2 的口径，两条分支的取舍留给 F-112，本任务不下结论。
        event.getRenderer().getModel().riding = true;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0.0D, SINK_Y, 0.0D);
        poseStack.scale(QbounceClientState.squashScaleX(),
                QbounceClientState.squashScaleY(),
                QbounceClientState.squashScaleZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(QbounceClientState.rotateDegrees()));
    }

    /**
     * {@code pushPose} 与 {@code popPose} 的 {@code pop} 端：判据与 {@link #onRenderPre} 同一个方法，
     * 成对关系是「同一实体、同一次渲染调用」，因此不受同屏实体数量影响（三者互不累积位移）。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPost(RenderLivingEvent.Post event) {
        if (appliesTo(event.getEntity())) {
            event.getPoseStack().popPose();
        }
    }

    /** 唯一的姿态判据：效果在不在 + 还没有被别人承载。 */
    private static boolean appliesTo(LivingEntity entity) {
        return entity != null
                && entity.hasEffect(FarmstormMobEffects.Q_BOUNCE)
                && !entity.isPassenger();
    }
}
