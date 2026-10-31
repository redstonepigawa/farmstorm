package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * 桑吉尔蔗的输入锁（TECH §4.3 / D-60 经 D-79 修订），锁定区间 = 效果存在的整个三相位。
 * <p><b>七段各管一块</b>，缺一段就漏一类输入：移动/跳跃/潜行（{@link MovementInputUpdateEvent} 清零）、
 * 鼠标按下（{@link InputEvent.MouseButton.Pre} 取消）、攻击/使用/持续挖掘（
 * {@link InputEvent.InteractionKeyMappingTriggered} 第二道网）、键盘动作（{@link InputEvent.Key} 里
 * <b>抽干</b> {@code consumeClick()}，因为该事件不可取消且触发在 {@code KeyMapping.click} 之后）、
 * 滚轮（{@link InputEvent.MouseScrollingEvent} 取消）、视角（{@link CalculatePlayerTurnEvent} 把灵敏度压到 0）、
 * GUI（{@link ScreenEvent.Opening} 只放行暂停菜单）。
 * <p><b>ESC 不需要「放行」</b>：它走 {@code KeyboardHandler#keyPress} 里的 {@code pauseGame}，
 * 位置在 NeoForge 所有钩子之前，拦不拦都会生效（见 {@code .work/FINDING-RT13-INPUT-LOCK.md}）。
 * <p><b>0.0.4 起锁定分成两个判据</b>（作者裁定）：桑吉尔蔗仍是<b>七段全锁</b>；Q弹 <b>只锁第①段</b>
 * （WASD 移动 + 跳跃 + 潜行），其余六段（鼠标按键 / 攻击与使用 / 键盘 / 滚轮 / 视角灵敏度 / GUI）全部放开。
 * 于是本类有两个判据：{@link #fullyLocked()} 只认桑吉尔蔗，供第②~⑦段；{@link #movementLocked()} 认
 * 桑吉尔蔗 或 Q弹，供第①段与「进锁松键」那条 tick 逻辑。
 * <p><b>分段判据对照</b>：①移动/跳跃/潜行 = 移动锁；②鼠标按下 / ③攻击与使用 / ④键盘 / ⑤滚轮 / ⑥视角 / ⑦GUI = 全锁。
 * <p><b>已知限制</b>（无 Mixin 够不着，验收时不算缺陷）：F11 全屏 / F2 截图 / F3 调试。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class InputLockHandler {
    /** 上一次 tick 的<b>移动锁</b>状态（Q弹 也算）：只服务「进入锁定那一 tick 松键 + 打一行日志」。 */
    private static boolean wasMovementLocked;

    private InputLockHandler() {}

    /**
     * <b>全锁</b>判据：只认桑吉尔蔗（七段全锁）。Q弹 不在这里——它只锁第①段（0.0.4 作者裁定）。
     * <p>与 0.0.3 逐字相同的部分是「只看效果在不在 + 得有本地玩家」：三个相位都锁，少一个可能写错的数字（§4.3）。
     */
    private static boolean fullyLocked() {
        return ZangiefClientState.active() && Minecraft.getInstance().player != null;
    }

    /**
     * <b>移动锁</b>判据：桑吉尔蔗 或 Q弹。只供第①段（{@link #onMovementInput}）与混乱的「锁优先」早退使用。
     * <p>为什么 Q弹 也算移动锁：混乱反转的正是移动分量，两者作用在同一处，所以 Q弹 期间混乱同样不生效。
     */
    private static boolean movementLocked() {
        return (ZangiefClientState.active() || QbounceClientState.active())
                && Minecraft.getInstance().player != null;
    }

    /**
     * 全锁的公开判据（TECH §5.2 #1）：第②~⑦段用它。锁的定义处仍只有本类的 {@link #fullyLocked()} 一处（D-79）。
     */
    public static boolean isLocked() {
        return fullyLocked();
    }

    /** 移动锁的公开判据：给混乱的「锁优先」早退用（{@link ConfusionInputHandler}）。 */
    public static boolean isMovementLocked() {
        return movementLocked();
    }

    /**
     * 进入锁定的那一 tick 主动复位按键状态（D-79 新增）：若玩家在按住左键挖掘的中途被锁定，
     * 之后不会再有新的「按下」事件，{@code keyAttack.isDown()} 会保持 true，挖掘就停不下来。
     * <p><b>0.0.4 起这里判的是移动锁</b>：Q弹 期间不拦攻击/使用，松键只在「进入锁」那一 tick 做一次，
     * 若跟着全锁走，Q弹（只锁移动）就永远不会松键、也就漏掉了这条保护。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        boolean nowLocked = movementLocked();
        if (nowLocked && !wasMovementLocked) {
            Options options = Minecraft.getInstance().options;
            options.keyAttack.setDown(false);
            options.keyUse.setDown(false);
            Farmstorm.LOGGER.info("输入锁 生效");
        } else if (!nowLocked && wasMovementLocked) {
            Farmstorm.LOGGER.info("输入锁 解除");
        }
        wasMovementLocked = nowLocked;
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!movementLocked()) {
            return;
        }
        Input input = event.getInput();
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    /** 可取消且最早（{@code MouseHandler#onPress} 入口直接 return），比交互事件更靠前。 */
    @SubscribeEvent
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (fullyLocked()) {
            event.setCanceled(true);
        }
    }

    /** 第二道网：{@code continueAttack} 每 tick 也会 fire 本事件，因此「按住左键持续挖掘」也能拦住。 */
    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        if (fullyLocked()) {
            event.setCanceled(true);
        }
    }

    /**
     * 键盘：本事件<b>不可取消</b>（{@code InputEvent.Key} 没实现 {@code ICancellableEvent}），
     * 且触发在 {@code KeyMapping.set/click} 之后；但快捷栏 / 丢弃 / 背包要等到 tick 里的
     * {@code Minecraft#handleKeybinds} 才消费点击，所以在这里把点击抽干等于拦截（D-79）。
     */
    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (!fullyLocked()) {
            return;
        }
        Options options = Minecraft.getInstance().options;
        for (KeyMapping hotbar : options.keyHotbarSlots) {
            drainOne(hotbar);
        }
        drain(options.keyDrop, options.keyInventory, options.keySwapOffhand, options.keyTogglePerspective);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (fullyLocked()) {
            event.setCanceled(true);
        }
    }

    /**
     * 视角：无 Mixin 的唯一手法（{@code ClientHooks#getTurnPlayerValues} 就在 {@code MouseHandler#turnPlayer} 里）。
     * 灵敏度压到 0 后仍剩 {@code (0 × 0.6 + 0.2)} 的系数，即「极慢但仍可微动」，这是文档既定口径的固有余量。
     */
    @SubscribeEvent
    public static void onTurn(CalculatePlayerTurnEvent event) {
        if (fullyLocked()) {
            event.setMouseSensitivity(0.0D);
            event.setCinematicCameraEnabled(false);
        }
    }

    /**
     * GUI：只放行暂停菜单（D-60 只留 ESC）。
     * <p>死亡屏是唯一例外，且是刻意的：{@code /kill} 与坠落伤害都发生在锁定期内（F-90 验收 4 就要在相位 2 里死一次），
     * 拦掉死亡屏会让玩家在没有 UI 的状态下卡住。
     */
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!fullyLocked()) {
            return;
        }
        Screen target = event.getNewScreen();
        if (target instanceof PauseScreen || target instanceof DeathScreen) {
            return;
        }
        event.setCanceled(true);
    }

    private static void drain(KeyMapping... mappings) {
        for (KeyMapping mapping : mappings) {
            drainOne(mapping);
        }
    }

    /** 抽干：把点击计数读到空，{@code handleKeybinds} 里的 {@code consumeClick()} 就再也拿不到点击。 */
    private static void drainOne(KeyMapping mapping) {
        while (mapping.consumeClick()) {
            // 空循环：每读一次就消耗掉一个点击
        }
    }
}
