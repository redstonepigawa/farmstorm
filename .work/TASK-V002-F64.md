# 任务书 V002-F-64 · 两个新 `DeferredRegister`：`mob_effects` 与 `sound_events`

## 目标（`13-TASKS-0.0.2.md` §3 F-64）

- 复杂度 **L** ｜ 3.0 人日 ｜ 依赖 F-60（已完成，`cd2c230`）
- 这是 Phase 11 里唯一的 L 任务，也是「0 个自定义网络包」这条红线的支点：三项侵入性效果的状态同步全靠**隐藏 `MobEffect`**（D-50）。

## 交付物

| # | 文件 | 内容 |
|---|---|---|
| 1 | `effect/WildHunterEffect.java`（新建） | 可见、带图标；**三项属性修饰器的挂载与卸下**（属性与数值见 `12-TECH` 里 `WildHunterEffect` 的小节与 `BRIEF` 对应条目） |
| 2 | `effect/ZangiefPiledriverEffect.java`（新建） | 隐藏；**amplifier 当相位号**（D-50），不显示图标、不进 HUD |
| 3 | `registry/FarmstormMobEffects.java`（新建） | `DeferredRegister<MobEffect>`，注册 `wild_hunter` 与 `zangief_piledriver` |
| 4 | `registry/FarmstormSounds.java`（新建） | `DeferredRegister<SoundEvent>`，注册 `wild_hunter_loop` 与 `zangief_piledriver`，**都用可变距离事件**（`SoundEvent.createVariableRangeEvent`） |
| 5 | `Farmstorm.java` | 挂载两个 `REGISTER`（**只加挂载**：不要动自检文案，那是 F-66 的活） |
| 6 | `src/main/resources/assets/farmstorm/sounds.json`（新建） | 把两个 `SoundEvent` 映射到已存在的两个 ogg（`sounds/wild_hunter_loop.ogg`、`sounds/zangief_piledriver.ogg`，都已在仓库里） |

## 上游必读（不要凭印象写 API）

- `docs/zh/12-TECH-0.0.2.md`：搜 `WildHunterEffect`、`ZangiefPiledriverEffect`、`mob_effects`、`sound_events` 定位逐项规格；另看 **§1.2**（`sounds.json` 的位置约定）与 **§2.2** 第 10~13 行。
- `docs/zh/10-DECISIONS-0.0.2.md` 的 **D-50**（0 包与隐藏 `MobEffect` 通道）、**D-66 / D-76**（音频）。
- `.work/v002-BRIEF.md` §7.5（相位与效果的对应）。
- `.work/FINDING-RT13-INPUT-LOCK.md`（相位同步的已知副作用，与 F-64 相关但本轮不实现控制器）。

## 验收（四条，逐条给原文）

1. `/effect give @s farmstorm:wild_hunter 10` 成功且 HUD 出现效果图标；`/effect give @s farmstorm:zangief_piledriver 10 2` 成功且 **HUD 无图标、物品栏无描述**。
2. `/data get entity @s ActiveEffects` 里 `farmstorm:zangief_piledriver` 的 **`amplifier` 为 2**、**`show_icon` 为 false**（D-50 的相位通道成立）。
3. `/playsound farmstorm:zangief_piledriver master @s ~ ~ ~ 1 1` 与 `/playsound farmstorm:wild_hunter_loop master @s ~ ~ ~ 1 1` 都返回成功，且两个事件在 `/playsound` 的补全里可见。
4. **专用服务器**启动日志无 `NoClassDefFoundError`（两个效果类与两个注册类必须不依赖 `Dist.CLIENT`）。

补充硬门禁：`build -PfsRunDir=run-f64` 退出码 0、**零 warning**；`自检 作物 19 种 … → OK` 与 `自检 幸运浆果池 16 种 … → OK` 仍在。

## 边界

- **不许**改 `FarmstormItems` / `FarmstormBlocks` / `FarmstormConfig` / `datagen/*` / `docs/`。
- **不许**动 `Farmstorm.java` 的自检文案（F-66 会把注册对象自检改成 77，别抢）。
- **不许**加语言键（`subtitles.farmstorm.*`、效果名等属 datagen 的 +39 条，另算）。
- `/playsound` 在 RCON 下没有玩家实体时，可像 F-65 那样用 FakePlayer 作命令源（玩法上下文一致），但要在回报里说明。

## 已知坑（前几个任务踩过的，别再踩）

- `verify_project.py` 是**子集检查**，新增注册对象它看不见（V-07），所以"它绿了"**不能**当作你新增对象有效的证据。
- 系统 PATH 上的 `python` 是 Microsoft Store 占位符，要用 `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe -X utf8`。
- 跑服务端必须 `-PfsRunDir=run-f64`，否则会撞 `run/world/session.lock`。收尾如果 kill 掉 java，gradle 会报 `exit value -1`——这是人为终止，不是缺陷，但要在回报里讲清楚。
- 探针用完必须删净，并且在**源码 / `build\classes` / jar** 三处都查 `Probe` 计数为 0。

## 回报

- 四条验收的**实际命令与返回原文**。
- 两个效果类与两个注册类的关键代码片段（属性修饰器怎么挂/卸、隐藏怎么做、可变距离事件怎么建）。
- `sounds.json` 的完整内容。
- `git diff --stat`、`build` 最后两行、`verify_project.py` 输出。
- 你判断需要下游注意的点（尤其：`WildHunterEffect` 的属性修饰器在效果被**移除/过期**时能否可靠卸下——这决定 F-77 会不会留残余属性）。
- **不要 commit**。
