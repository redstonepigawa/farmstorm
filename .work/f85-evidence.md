# F-85 证据 —— Phase 13 出口回归（Lane A）· **完成**

环境：`run-p13` / server-port **25631** / rcon **25641**，`FS_RUN_LOCK=run-p13`；单个客户端（`.work/client-quickplay.gradle`）。
规则遵守：V-37（收尾只杀连到 25631 的那个客户端 PID）、V-40（探针本轮删净）、V-41（增量 build，未 clean）、V-58（异常退出先查 `run-p13/crash-reports/` —— 本轮**零新增**）。

## 判据 1 · TECH §11 #13 / #14 / #15

| 条目 | 读数 | 判定 |
|---|---|---|
| **#13 剪影三条件** | 作者真机确认「剪影贴合、位置正确」（`febad56`）；64 格过滤 + `hasLineOfSight` + 自己/同队排除已在 F-83 轮逐条验过；重入守卫 `rendering` 防递归 | ✅ |
| **#14 冲刺走服务端复核** | 真 `ServerboundSwingPacket` 之后，**服务端自己**算目标并结算：`狂野猎人 目标 minecraft:pig 夹角 26.0 度 距离 2.40` | ✅ |
| **#15 击杀续时无上限** | 连杀 6 只：`13899 → 14058 → 14218 → 14378 → 14538 → 14698`（每只 **+160 刻**），6 行 `狂野猎人 续时`；`grep -nE "killCount|maxKill"` 无输出 | ✅ |

## 判据 2 · 七项能力一次跑通 + `/effect clear` 复位

| # | 能力 | 读数（同一次会话 20:40–20:41） |
|---|---|---|
| 1 | 属性加成 | `Attack Damage 1.4` / `Speed 0.13000000193715097` / `Attack Speed 5.2`（基线 1.0 / 0.1 / 4.0） |
| 2 | 索敌吸附 | `狂野猎人 目标 minecraft:pig 夹角 26.0 度 距离 2.40` |
| 3 | 伤害击退 | 猪 `10.0f → 8.6f`（−1.4 = 加成后攻击力）；`结算 目标 minecraft:pig 伤害 1.4 击退 0.4` |
| 4 | 击杀续时 | 6 行 `续时`，每只 +160 |
| 5 | 世界灰度 | `灰度 PostChain 加载 farmstorm:shaders/post/grayscale.json`（20:38:28）；作者确认世界变灰 |
| 6 | 红色剪影 | 作者确认贴合（`febad56`） |
| 7 | 循环音 | `循环音 实例 Dev relative=true` + `循环音 起 Dev`（20:38:28） |

**复位（`/effect clear`，20:41:24）**：属性回 `Attack Damage 1.0` / `Speed 0.10000000149011612`；`active_effects` 空；客户端同时打出 `循环音 停 Dev` + `灰度 PostChain 卸载`。

**图**：`.work/f85-shots/seven-items-frame.png`（20:41:22）、`.work/f85-shots/after-clear-reset.png`（20:41:28）。

## 判据 3 · 红线（两种读数）

用法：`CustomPacketPayload` **0**、`@Mixin` 注解 **0**、`mixins.json` **0**、`org.spongepowered` **0**、`build.gradle` mixin **0**。
字面 `Mixin` = **5 处全是注释**（V-47，未洗）：`InputLockHandler:30/114`、`WildHunterCombatEvents:19`、`WildHunterSwingSettler:12`、`FarmstormExplosion:36`。

## 附加 A · F-79 真按键（真 `ServerboundSwingPacket`）—— 通过

```text
[20:40:03] [F85PROBE] fire active=true at=0.50,0.50
[20:40:03] [F85PROBE] sent real swing packet
[20:40:03.714] 狂野猎人 吸附挥击 速度 0.60
[20:40:03.718] 狂野猎人 目标 minecraft:pig 夹角 26.0 度 距离 2.40
[20:40:03.722] 狂野猎人 结算 目标 minecraft:pig 伤害 1.4 击退 0.4
```
玩家 `0.5 → 3.636754511501454`（冲刺跨到位），猪 `10.0f → 8.6f`（只掉一次血）。

## 附加 B · F-80b / V-43 —— 通过（同一次真包）

3.5 格外按攻击 → 服务端本 tick 无 `AttackEntityEvent`（没打中），只靠「swing 跃变 + 冲刺位移」双信号结算；
三行日志连发、只掉 1.4、无双倍 → 去抖生效。

## ⚠️ 本轮发现并修掉的真 bug（源码改动 1 处）

`combat/WildHunterSwingSettler.java`：原条件要求**同一 tick** 同时满足「swing 跃变」与「位移 ≥0.55」。
真包实测证明**位移包比挥击包晚 1–2 刻**到达 → 空挥在真链路上永远不结算（探针直接调 `tick()` 时才对，所以之前没暴露）。
修法：记录 `swingTick`，允许 **2 刻窗口**配对（`SWING_WINDOW = 2`），去抖窗口不变。
修复前：玩家 `0.5 → 3.82`（冲刺发生）但猪 `10.0f → 10.0f`、服务端无 `吸附挥击`/`结算`。
修复后：见 §附加 A。

## 产物自查

- 我的探针 `ProbeF85` + `f85-probe.enabled` / `f85-fire.enabled` 已删；`build` exit 0。
- ⚠️ `src` / `build/classes` / jar 三处各有 1 个 `Probe`，是 **Lane B 的 `client/F9XProbe.java`**（不是我的，我没动）；出货前须由 Lane B 清掉。
- `run-p13/crash-reports/` 仍是 19:38:28 / 19:45:22 两份旧报告，本轮零新增。

## 转给文档回写 lane 的不一致（我不改 docs/）

1. F-77 判据 2：文档 4.8 vs 实测 9.6（原版公式 ×2）。
2. F-78 方法名：13-TASKS `resolveTarget` vs TECH `selectTarget`。
3. F-82：D-61「整屏含 GUI」→ 作者裁决 V-56「世界灰度即可，HUD 原色」。
4. TECH §4.1：`RenderTarget` 是抽象类（应为 `TextureTarget`）；`post_effect` 的 `targets` 必须是**数组**。
5. TECH §4.2：Post 时变换已 `popPose()` → 必须「渲染器自己再画一遍」；`entityTranslucentEmissive` 忽略 ColorModulator → 红必须烘进贴图。
6. F-76 措辞：1.21.1 的 F3 不显示属性数值。
7. **新增**：F-80b 双信号配对必须留 **≥2 刻窗口**（挥击包与位移包不同 tick 到达）。
