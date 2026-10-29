# 结论 · RT-13 输入锁到底能不能做（无 Mixin）

> 问题来源：`12-TECH-0.0.2.md` 的 D-60 写「输入锁 = 五个事件各管一段」，其中把 `InputEvent.Key` 当作快捷栏 1–9 的拦截点。
> 本文结论**全部来自源码**，不是推测。证据取自 `build/moddev/artifacts/neoforge-21.1.213-sources.jar`（已打过 NeoForge 补丁的 Minecraft 源码），行号为该 jar 内的行号。

## 总结论

**能做成，不用 Mixin —— 但文档里写给 `InputEvent.Key` 的角色是错的，必须重新指定。**

三条硬事实：

1. **`InputEvent.Key` 根本不可取消**：`InputEvent.java:214` 是 `public static class Key extends InputEvent`，**没有 `ICancellableEvent`**。对比 `MouseButton.Pre`（:99）与 `InteractionKeyMappingTriggered`（:296）都实现了它。
2. **就算能取消也太晚**：`ClientHooks.onKeyInput(...)` 是 `KeyboardHandler.keyPress` 的**最后一条语句**（`:485`），而在此之前（`:444`、`:483-484`）已经执行完 `KeyMapping.set(key, true)` 与 **`KeyMapping.click(key)`**。
3. **快捷栏是在 tick 里消费的**：`Minecraft.handleKeybinds()`（`:1926`）的第 `:1944` 行 `if (this.options.keyHotbarSlots[i].consumeClick())`。`handleKeybinds()` 在 `:1837` 被调用。

→ 所以对键盘动作**唯一可行的无 Mixin 手法是「抢消费」**：在 `InputEvent.Key`（时机正好在 click 之后、handleKeybinds 之前）里调用 `consumeClick()` 把点击**抽走**，让后面的 `handleKeybinds` 读不到。**不是"取消"，是"抽干"。**

## 逐路径核实表

| 要拦的动作 | 文档写的钩子 | 实际能否拦住 | 正确做法 | 证据 |
|---|---|---|---|---|
| 鼠标左/右键按下（攻击/使用） | `InteractionKeyMappingTriggered` | 能，但**晚**（已经在 startAttack 内部） | **`InputEvent.MouseButton.Pre`**：可取消且最先执行 | `MouseHandler.java:82` `if (onMouseButtonPre(...)) return;` |
| 按住左键持续挖掘 | 同上 | **能**：`continueAttack` 每 tick 也会 fire 该事件 | 同上，或保留此事件做第二道网 | `Minecraft.java:1630`（在 `continueAttack` 内）、`:2035` 由 `keyAttack.isDown()` 驱动 |
| 快捷栏 1–9 | `InputEvent.Key` + 取消 | **不能**（不可取消 + 太晚） | 在 `InputEvent.Key` 里 `keyHotbarSlots[i].consumeClick()` 抽干 | `Minecraft.java:1944` |
| 丢弃 Q / 背包 E / 副手 F / 视角 F5 | `InputEvent.Key` | **不能**（同上） | 同上，抽干对应的 `KeyMapping` | 均在 `handleKeybinds()` 内消费 |
| 滚轮切栏 | `InputEvent.MouseScrollingEvent` | **能**，可取消且早 | 保持 | `MouseHandler.java:172` `if (ClientHooks.onMouseScroll(...)) return;` |
| 移动 / 跳跃 / 潜行 | `MovementInputUpdateEvent` | **能**（清零 impulse） | 保持 | `LocalPlayer.aiStep` 阶段 |
| **鼠标转视角** | 文档未给 | 文档没写但**能做** | `CalculatePlayerTurnEvent.setMouseSensitivity(0)` | `ClientHooks.java:368`；事件有 `setMouseSensitivity(double)`（`CalculatePlayerTurnEvent.java:46`） |
| 打开 GUI | `ScreenEvent.Opening` | **能** | 保持 | 已在文档中 |
| **F11 全屏 / F2 截图 / F3 调试 / ESC 暂停** | 文档未提 | **全都拦不住**（无 Mixin 时） | 只能接受为已知限制 | 这些在 `keyPress` 里 `onKeyInput` **之前**就执行了（`:405-433` 一带）、ESC 在 `:460` 附近 |

## 三个文档没算到的坑

### 坑 1 · 锁定生效时鼠标已经按着，挖掘不会停
`continueAttack` 的入参是 `this.options.keyAttack.isDown()`（`Minecraft.java:2035`）。如果玩家在**按住左键挖掘的中途**被进入锁定状态，此后不会再有新的"按下"事件，`isDown()` 保持 true → 挖掘继续。
- **缓解**：进入锁定时主动 `options.keyAttack.setDown(false)`（或 `KeyMapping.set`）；或依赖 `continueAttack` 每 tick fire 的 `InteractionKeyMappingTriggered` 做逐 tick 取消（`:1630` 那条能拦住实际破坏）。

### 坑 2 · ESC 不需要"放行"，它本来就更早执行
文档写「放行 ESC」。实际上 ESC 走的是 `keyPress` 里 `p_90895_ == 256 → pauseGame(...)`，位置在所有 NeoForge 钩子**之前**——**无论你拦不拦，ESC 都会生效**。所以"放行"是个伪需求，但同时意味着"想拦 ESC"也做不到。

### 坑 3 · 需要 7 个机制，不是 5 个
把上表去重后：`MouseButton.Pre`、`MouseScrollingEvent`、`InteractionKeyMappingTriggered`、`InputEvent.Key`（抽干用途）、`MovementInputUpdateEvent`、`CalculatePlayerTurnEvent`、`ScreenEvent.Opening` —— **7 个**。文档写"五个事件各管一段"，少了两个（视角与 GUI 之外的 `InteractionKeyMappingTriggered` 独立成段）。

## 对 0.0.2 文档的处置建议（未擅自改）

`12-TECH-0.0.2.md` 里 D-60 那一节需要三处修订：
1. 把"`InputEvent.Key` 取消"改成"`InputEvent.Key` 里抽干 `consumeClick()`"，并说明为什么不能取消（不可取消 + 太晚）。
2. 补 `CalculatePlayerTurnEvent` 作为视角锁手段（文档没写，但这是唯一无 Mixin 的可行路径）。
3. 补"F11/F2/F3/ESC 拦不住"这条已知限制，以及"锁定中途按住左键"的状态复位。

## 复现方式

```powershell
# 从已打补丁的源码 jar 抽取并核对（本文所有行号来源）
$jar = "D:\project_farm\build\moddev\artifacts\neoforge-21.1.213-sources.jar"
# 需要的条目：
#   net/minecraft/client/KeyboardHandler.java   （:444 / :483-485）
#   net/minecraft/client/Minecraft.java          （:1837 / :1926 / :1944 / :2035 / :1630）
#   net/minecraft/client/MouseHandler.java       （:82 / :172 / :282）
#   net/neoforged/neoforge/client/event/InputEvent.java （:99 / :136 / :214 / :296）
#   net/neoforged/neoforge/client/ClientHooks.java（:368 / :667 / :675 / :680 / :684）
#   net/neoforged/neoforge/client/event/CalculatePlayerTurnEvent.java（:46）
```

抽出的副本在 `.scratch/mcsrc/`（不入库）。
