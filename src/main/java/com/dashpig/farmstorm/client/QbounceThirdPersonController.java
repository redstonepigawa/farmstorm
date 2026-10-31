package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Q弹 期间的第三人称强制（0.0.4/D，作者裁定）：进效果时记下当时的视角 → 效果期间每刻强制第三人称 →
 * 结束 / 清除 / 死亡 / 退出世界时恢复记下的那个视角。
 *
 * <p><b>为什么必须每刻写一次</b>：F5 走的是原版 {@code keyTogglePerspective}，而 Q弹 只锁第①段
 * （{@code InputLockHandler} 的键盘抽干只在全锁时生效），所以 Q弹 期间玩家<b>能</b>改视角。强制语义只能靠
 * 「每刻把 {@code CameraType} 写回 {@link #FORCED}」实现：玩家按一次 F5，下一 tick 就被拉回。
 *
 * <p><b>用哪种第三人称</b>：固定 {@link CameraType#THIRD_PERSON_BACK}，不沿用玩家原来选的前/后。理由是
 * 可预测——F5 在三个档位里循环，无论从哪一档按下去，下一 tick 都回到同一处（不会出现「在 FRONT 按一下到 FIRST、
 * 下一 tick 再被拉去 BACK」的两跳）；且 BACK 是原版默认的第三人称，模型绕 Y 轴旋转、上下压缩的观感与旋转轴一致
 * （FRONT 会把左右与前后都反过来，看起来像在反着转）。
 *
 * <p><b>与 mount 分支的关系</b>：本类不读 {@code qbounce.poseMode}（服务端 config，客户端读不到，D-81），
 * 所以 {@code poseMode=mount}（玩家正骑在隐形盔甲架上）与 {@code render} 一样强制第三人称，语义不变。
 * 这不冲突：mount 分支要的正是「看得见自己坐在那」，第三人称是它的前提而不是反例。
 *
 * <p><b>不碰桑吉尔蔗的相机倒转</b>：那是 {@link ZangiefCameraController} 在
 * {@code ViewportEvent.ComputeCameraAngles} 里改渲染层的 roll/pitch/yaw，与本类改的 {@code CameraType}
 * 是两个不相干的量（前者是「镜头怎么转」，后者是「第几人称」），本类一个字节都没动它。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class QbounceThirdPersonController {

    /** 强制用的视角：固定第三人称背面（理由见类注释）。 */
    private static final CameraType FORCED = CameraType.THIRD_PERSON_BACK;

    /** 正在强制中：也标记着 {@link #saved} 有效，恢复只在它为 true 时发生一次。 */
    private static boolean forcing;

    /** 进效果那一刻玩家选的视角；恢复时逐字写回（中途玩家自己按 F5 改的不算数，强制语义）。 */
    private static CameraType saved = CameraType.FIRST_PERSON;

    private QbounceThirdPersonController() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Options options = minecraft.options;
        if (player == null || minecraft.level == null) {
            restore(options, "退出世界");
            return;
        }
        if (player.isDeadOrDying()) {
            restore(options, "死亡");
            return;
        }
        if (!player.hasEffect(FarmstormMobEffects.Q_BOUNCE)) {
            restore(options, "效果消失");
            return;
        }
        if (!forcing) {
            saved = options.getCameraType();
            forcing = true;
            Farmstorm.LOGGER.info("q_bounce 第三人称 强制 记下原视角={}", saved);
        }
        CameraType current = options.getCameraType();
        if (current != FORCED) {
            options.setCameraType(FORCED);         // F5 拉回：玩家每按一次，下一 tick 被这一行写回来
            Farmstorm.LOGGER.debug("q_bounce 第三人称 拉回 {} -> {}", current, FORCED);
        }
    }

    /** 恢复一次就收手：{@code forcing} 为 false 时什么都不做（无效果的那些 tick 不会反复写 options）。 */
    private static void restore(Options options, String reason) {
        if (!forcing) {
            return;
        }
        forcing = false;
        options.setCameraType(saved);
        Farmstorm.LOGGER.info("q_bounce 第三人称 恢复 reason={} 视角={}", reason, saved);
    }
}
