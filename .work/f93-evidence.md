# F-93 · Phase 14 出口回归记录（桑吉尔蔗）

HEAD：**e374bb0**（F-91+F-92）；服务端一组 **e1239e3**（F-86~F-90）
环境：`run-p14`（服务端 25651 / rcon 25661，`FS_RUN_LOCK=run-p14`）+ `run-p14c`（quickplay 客户端）
config：**默认值**（`stageDelayTicks=14 / levitationLevel=7 / fallTicks=40`），测量前后各核对一次
证据索引：主文件 `.work/p14-evidence.md`（按 F-86…F-92 分组）；日志摘录 `.work/p14-log-extract.txt`；
逐场景原文 `.work/p14-*.txt`；游戏内截图 `.work/p14-cam-*.png`

---

## 1. 三条判据（12-TECH §11）

### #10 · 三相位时序与落地时长 —— **过**
| 量 | 判据 | 实测 |
|---|---|---|
| 相位 0 时长 | = `stageDelayTicks`（14 刻） | 相位 0 `t=60944` → 相位 1 `t=60958`，**14 刻** |
| 相位 1 时长 | = `levitationDurationTicks`（20 刻） | `t=60958` → `t=60978`，**20 刻** |
| 相位 1 的漂浮等级 | `MobEffects.LEVITATION` amplifier = 6（VII 级） | `levitation=amp=6 dur=20` |
| 相位 2 落地耗时 | `fallTicks` 40 ± 2 | **平地 42 / 两格 42 / 四格 42**（3×3 台面，见 §7） |
| config 驱动 | 三键都得生效 | `stageDelayTicks=30 → 相位 0 变 30 刻`；`levitationLevel=3 → amp=2`；`fallTicks=80 → 落地 82 刻`（§8 单独标注轮次） |

### #11 · 只对非己方玩家生效 —— **过**
| 对象 | 结果 |
|---|---|
| 非己方玩家（生存，中心距 0.5） | 触发 |
| 同队玩家（归属者与目标同 `red` 队） | 不触发；**把归属者换到 `blue` 后立刻触发**（实时解析 C-9） |
| 创造模式玩家 | 不触发 |
| 旁观者 | 不触发 |
| 生物（pig） | 不触发 |
| 触发后方块 | 消失，**掉落物 0**（`/kill @e[type=item]` 前后计数相同，A-08） |
原文：`.work/p14-f87-*.txt` 八个场景。

### #12 · 输入锁只漏 ESC —— **过（机制级，见 §6）**
23 行逐项读数见 §4。**只漏** ESC（暂停菜单能开）与三条无 Mixin 够不着的已知限制（F11 / F2 / F3）。

---

## 2. 相位日志四行齐全 + `落地耗时`（逐 cycle，全部标了地形与配置轮）

本轮（默认 config，修复后的构建）`run-p14/logs/latest.log` 原文，按 cycle 归组：

| cycle | 地形 / 构型 | 相位 0 | 相位 1 | 相位 2 | 结束 | `落地耗时` |
|---|---|---|---|---|---|---|
| 1 | 平地（y=6，台面） | `t=60944 duration=15` | `t=60958 duration=21` | `t=60978 duration=100` | `t=61020` | **42 刻（目标 40）** |
| 2 | 两格高台（y=8，3×3） | `t=61494` | `t=61508` | `t=61528` | `t=61570` | **42 刻** |
| 3 | 四格高台（y=10，3×3） | `t=61622` | `t=61636` | `t=61656` | `t=61698` | **42 刻** |
| 4 | **1×1 石柱**（边缘构型，§7） | `t=61740` | `t=61754` | `t=61774` | `t=61797` | **23 刻**（预期，见 §7） |
| 5 | 两格高台（3×3，复跑） | `t=62222` | `t=62236` | `t=62256` | `t=62298` | **42 刻** |
| 6 | 四格高台（3×3，复跑） | `t=62340` | `t=62354` | `t=62374` | `t=62416` | **42 刻** |
| 7 | 平地 + 相位 2 中 `/kill` | `t=62554` | `t=62568` | `t=62588` | `结束（死亡）` | —（死亡收尾，无落地行） |
| 8 | 两格高台 + `/kill` | `t=62632` | `t=62646` | `t=62666` | `结束（死亡）` | — |
| 9 | 四格高台 + `/kill` | `t=62710` | `t=62724` | `t=62744` | `结束（死亡）` | — |

一行原文（cycle 1）：
```
[18:38:50.406] 桑吉尔蔗 相位 0 t=60944 玩家 Dev duration=15
[18:38:51.089] 桑吉尔蔗 相位 1 t=60958 玩家 Dev duration=21
[18:38:52.089] 桑吉尔蔗 相位 2 t=60978 玩家 Dev duration=100
[18:38:54.188] 桑吉尔蔗 落地耗时 42 刻（目标 40）
[18:38:54.188] 桑吉尔蔗 结束 t=61020 玩家 Dev
```
四行齐全（`相位 0` / `相位 1` / `相位 2` / `结束`），`落地耗时 N 刻` 每个正常落地 cycle 各出现一次。

---

## 3. 0 个自定义网络包 —— **过**
```
grep -r "CustomPacketPayload" src/main/java   →  0 处（在 e374bb0 的 HEAD 上重跑确认）
```
相位同步只用隐藏效果的 amplifier + 服务端 `setDeltaMovement`（走原版 `ClientboundSetEntityMotionPacket`）。

---

## 4. 输入锁逐项读数（23 项；= `12-TECH` RT-13 那 21 项按字面展开）

> 方法见 §6。**对照组** = 同一段脚本在**无效果**时跑；**锁定期** = 相位 0~2 期间跑。
> 原文：`.work/p14-client8.log`（对照）/ `.work/p14-client10.log`（锁定）。

| # | 项 | 锁定期实测 | 对照组实测 | 结论 |
|---|---|---|---|---|
| 1 | W 前进 | `impulse.forward=0.00` | `0.30`（位移 dx=0.17 dz=0.63） | 拦住 |
| 2 | A 左移 | `impulse.left=0.00` | 随组合位移（同上） | 拦住 |
| 3 | S 后退 | forward/left 均归零 | 可后退 | 拦住 |
| 4 | D 右移 | forward/left 均归零 | 可右移 | 拦住 |
| 5 | 空格跳跃 | `impulse.jump=false`，`dy=0.0000` | `jump=true`，`dy=0.4952` | 拦住 |
| 6 | Shift 潜行 | `impulse.shiftKeyDown=false` | `shift=true` | 拦住 |
| 7 | 左键（攻击 / 持续挖掘） | `MouseButton.Pre` 取消 `true`；`InteractionKeyMappingTriggered` 取消 `true` | 两者 `false` | 拦住 |
| 8 | 右键（使用） | 同上（同一对事件覆盖 use） | `false` | 拦住 |
| 9 | 快捷栏 1 | `keyHotbarSlots[0]` 点击被抽干 | 会切槽 | 拦住 |
| 10 | 快捷栏 2 | 同上 | 会切槽 | 拦住 |
| 11 | 快捷栏 3 | `抽干后仍有点击=false`，选中槽 `2→2`（未变） | 选中槽 **0→2**（真的切了） | 拦住 |
| 12 | 快捷栏 4 | 同上（循环覆盖 9 个槽） | 会切槽 | 拦住 |
| 13 | 快捷栏 5 | 同上 | 会切槽 | 拦住 |
| 14 | 快捷栏 6 | 同上 | 会切槽 | 拦住 |
| 15 | 快捷栏 7 | 同上 | 会切槽 | 拦住 |
| 16 | 快捷栏 8 | 同上 | 会切槽 | 拦住 |
| 17 | 快捷栏 9 | 同上 | 会切槽 | 拦住 |
| 18 | 滚轮上 | `MouseScrollingEvent` 取消 `true` | `false` | 拦住 |
| 19 | 滚轮下 | 同上（同一事件） | `false` | 拦住 |
| 20 | E 背包 | `背包打开=false`（`ScreenEvent.Opening` 拦下） | `true` | 拦住 |
| 21 | T 聊天 | `聊天打开=false` | `true` | 拦住 |
| 22 | `/` 命令 | 与 21 同一路径（`ChatScreen`） | `true` | 拦住 |
| 23 | **ESC 暂停菜单** | `暂停菜单打开=true` | `true` | **必须可用 ✔** |

（探针脚本对 1–9 是循环处理 9 个槽位、对滚轮上下是同一个事件，故 9–17 与 18–19 的行为由同一段代码覆盖；
表里按字面逐项列出，行为项不重复计数。）

### 已知限制（照实写，不算缺陷）
- **F11 全屏 / F2 截图 / F3 调试**：这三类在 `KeyboardHandler#keyPress` 内部、NeoForge 钩子**之前**就执行完，无 Mixin 够不着（.work/FINDING-RT13-INPUT-LOCK.md）。
- **视角（V-31）**：`CalculatePlayerTurnEvent.setMouseSensitivity(0)` 之后，`MouseHandler#turnPlayer` 里
  `d2 = sens × 0.6 + 0.2` **系数仍是 0.2**（默认 sens=0.5 时 `d2=0.5`），即**转速约降到 1/15.6，不是锁死**。
  这是文档既定口径的固有余量，未加 Mixin。
- **ESC 的「放行」是伪需求**：它走 `keyPress` 里的 `pauseGame`，在任何钩子之前；`ScreenEvent.Opening` 放行
  `PauseScreen` 才是必须的那一步。

---

## 5. 配置轮标注（哪些读数来自哪一轮，别混读）

| 轮次 | config | 用它的读数 |
|---|---|---|
| 默认轮（本轮，修复后） | 14 / 7 / 40 | §1 #10 的 14/20/42；§2 全部 cycle 1–9；§4 输入锁读数 |
| 变体轮（V-33 停服改 key，UTF8 无 BOM） | **30 / 3 / 80** | 「相位 0 持续 30 刻」「`levitation amp=2`」「`落地耗时 82 刻（目标 80）`」；**`自转累计 5904.0 度（82 刻 × 72.0）` 也来自这一轮**（默认轮同式应为 42 × 72 = 3024 度） |

变体轮的 §1 三键读数原文见 `.work/p14-evidence.md` §3.3 与 `.work/p14-timeline-variant.txt`；
该轮的 server log 已随重启轮转，引用的是当时抓下来的行。

---

## 6. 验证方式声明（V-34，不含糊）

**F-92 的「真人逐键手测」在本机做不了**：V-34 确认本机无法把前台窗口切给游戏，
`CopyFromScreen` 只会截到无关窗口，真实键鼠事件送不进客户端。

因此 §4 是**机制级验证**，不是人手按下去的手感验证：
临时探针在客户端内**驱动真实输入路径**——`KeyMapping.setDown` / `KeyMapping.click` 后 fire `InputEvent.Key`、
fire `MouseButton.Pre` / `MouseScrollingEvent` / `InteractionKeyMappingTriggered`、`setScreen`（走 `ScreenEvent.Opening`），
再读回 impulse / 取消位 / 点击计数 / 屏幕状态。它证明每个钩子确实按设计改掉了对应输入，
但**不覆盖**「真人按下去的手感与意外路径」。要补齐的话，表结构可直接沿用 §4。

相机（F-91）的视觉证据用**游戏内 `Screenshot.grab`**（读 framebuffer，与焦点无关）：
`p14-cam-1-phase0.png`（相位 0，看图上石台底面在画面上方、柱状方块朝下吊着、天空在下方）、
`p14-cam-2-phase1.png`（相位 1）、`p14-cam-3-phase2.png`（相位 2，画面相对上一张转过一个角度）、
`p14-cam-4-after.png`（效果消失，同一场景恢复水平朝向）——相位 0 与「效果消失」两张构成开/关对照。

---

## 7. 「高台」几何口径（V-46）

1. 验收用的「两格 / 四格高台」指 **≥3×3 的台面**。本轮 cycle 2/3/5/6 就是 3×3 顶面，落地耗时都是 42 刻。
2. **1×1 柱面是边缘构型**（cycle 4，23 刻）：玩家在漂浮段的横向漂移会把自己挪出柱顶那一列，
   下坠中途**被自己站的柱子截住**——这是真实几何结果，不是伺服错误；
   落在比目标地面高 4 格的柱顶上，落地时刻**明显短于 `fallTicks`**，属预期。
   用 1×1 柱子复测看到 23 刻，不是回归。

---

## 8. 收尾清单

- 未做 `clean`（V-41）；只做增量 `compileJava` / `build`。
- 收尾按 V-37：rcon `stop` + **只杀自己客户端的 PID**（`MainWindowTitle` 匹配 `Minecraft NeoForge*` 的那一个），没有一刀切杀 java。
- `.tools/runserver-run-p14.lock` 与 `.tools/runserver.lock` 都不存在（没留锁）。
- `run-p14/config/farmstorm-server.toml` 已改回默认（14 / 7 / 40），UTF8 无 BOM（首字节 91,99,111）。
- 本 lane 探针已删净：`src` / `build\classes` / jar 内 **0 / 0 / 0**（jar 里剩下的 `ProbeF80b.class` 属 Lane A，全仓 gate 由你做）。
