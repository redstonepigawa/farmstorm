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
 * <p><b>已知限制</b>（无 Mixin 够不着，验收时不算缺陷）：F11 全屏 / F2 截图 / F3 调试。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class InputLockHandler {
    private static boolean wasLocked;

    private InputLockHandler() {}

    /** 锁定判据只看「效果在不在」：三个相位都锁，少一个可能写错的数字（§4.3）。 */
    private static boolean locked() {
        return ZangiefClientState.active() && Minecraft.getInstance().player != null;
    }

    /**
     * 进入锁定的那一 tick 主动复位按键状态（D-79 新增）：若玩家在按住左键挖掘的中途被锁定，
     * 之后不会再有新的「按下」事件，{@code keyAttack.isDown()} 会保持 true，挖掘就停不下来。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        boolean nowLocked = locked();
        if (nowLocked && !wasLocked) {
            Options options = Minecraft.getInstance().options;
            options.keyAttack.setDown(false);
            options.keyUse.setDown(false);
            Farmstorm.LOGGER.info("桑吉尔蔗 输入锁 生效");
        } else if (!nowLocked && wasLocked) {
            Farmstorm.LOGGER.info("桑吉尔蔗 输入锁 解除");
        }
        wasLocked = nowLocked;
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!locked()) {
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
        if (locked()) {
            event.setCanceled(true);
        }
    }

    /** 第二道网：{@code continueAttack} 每 tick 也会 fire 本事件，因此「按住左键持续挖掘」也能拦住。 */
    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        if (locked()) {
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
        if (!locked()) {
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
        if (locked()) {
            event.setCanceled(true);
        }
    }

    /**
     * 视角：无 Mixin 的唯一手法（{@code ClientHooks#getTurnPlayerValues} 就在 {@code MouseHandler#turnPlayer} 里）。
     * 灵敏度压到 0 后仍剩 {@code (0 × 0.6 + 0.2)} 的系数，即「极慢但仍可微动」，这是文档既定口径的固有余量。
     */
    @SubscribeEvent
    public static void onTurn(CalculatePlayerTurnEvent event) {
        if (locked()) {
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
        if (!locked()) {
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
