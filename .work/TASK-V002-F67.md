# 任务书 V002-F-67 · 压缩干草块属性核对 + 抗性后果矩阵实测

## 目标（`13-TASKS-0.0.2.md` §3 F-67，经实际状态调整）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 F-65（已完成 `ab33fff`）
- **重要前置事实**：F-65 建这个方块时**已经**按规格写好了属性——`BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(30.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.GRASS)`、普通 `Block`、无自定义子类。
  → 所以本任务的**第一件事是核对**（不是重写）。属性若与上面逐字一致就**不要动**；不一致才改，并在回报里说明差异。
- 本任务真正的价值在**第二件事**：把 A-06 的抗性后果矩阵**跑一遍实测**，这是全项目最容易写反的一张表。

## A-06 矩阵（`13-TASKS` §0.4 裁决，`12-TECH §3.9` 与 §11 #9 已按此修正）

压缩干草块抗性 **6.0**。判定是 `BlastRule.canBreak()` 的 `resistance < maxResistance`：

| 来源 | 阈值 | 结果 | 为什么 |
|---|---|---|---|
| 瓜！ | 6.0 | **破不动** | `6.0 < 6.0` 为假 |
| 瓜片子 | 6.0 | **破不动** | 同上 |
| 育碧服务器 | 3.6e6 | **破得动** | `6.0 < 3.6e6` 为真 |
| 毒育碧服务器 | 3.6e6 | **破得动** | 同上 |
| 原版爆炸（含 TNT 威力 4） | 原版公式 | **破得动** | 逐格 `f -= (resistance + 0.3) * 0.3`，`f > 0` 即破；威力 4 时 `f` 起始区间 [2.8, 5.2]，减一次 1.89 后仍为正 |

## 验收（四条）

1. 方块硬度读数 = **30**（`strength(30.0F, 6.0F)` 的第一项），且 `/execute if block ~ ~ ~ farmstorm:compressed_hay_block run say ok` 输出 `ok`。
   - 原任务书写「F3 面板读数」，但 **F3 是客户端界面**，本机客户端进不去世界（F-66/F-61 两次都卡在 `Loaded 0 entity animations`）。**改用服务端等价读数并在回报里说明**：探针打印 `state.getDestroySpeed(level, pos)` 与 `block.defaultDestroyTime()`；两者应一致为 30.0。
2. **空手左键 30 秒不掉落任何物品**（`requiresCorrectToolForDrops`）；改用钻石镐能破坏。
   - 建议用 FakePlayer 走真实的破坏流程（`gameMode.destroyBlock`），比读属性更硬。注意 F-74 之前**没有战利品表**，所以「破坏后不掉本体」**属预期**，别当成缺陷。
3. 矩阵四行逐行实测：用瓜！、瓜片子各炸一次 → 方块**存活**（`/execute if block` 仍命中）；用育碧服务器炸一次 → 方块**消失**；毒育碧服务器可一并测。
4. 紧贴方块 `/summon tnt` 并引爆 → 方块**被破坏**（A-06 的这一行最反直觉：原版爆炸破得动，因为走的是原版公式而不是我们的 `BlastRule`）。

硬门禁：`build -PfsRunDir=run-f67` exit 0、零 warning；三条自检（注册对象 77 / 作物 19 种 / 幸运浆果池 16 种）仍在且 OK。

## 边界

- 只许动 `registry/FarmstormBlocks.java`（且**仅在属性与规格不一致时**才改）。
- **不许**动 datagen（战利品表 F-74、blockstate/模型 F-95）、`FarmstormConfig`、`docs/`。
- **不许**为了让矩阵"看起来对"去改 `BlastRule` 或阈值——矩阵是**验收对象**，不是可调参数。

## 环境提醒（上一轮我踩过的坑）

- **跑完服务端必须当场收干净**：`Get-Process java | Stop-Process -Force`，再删 `.tools/runserver.lock`。上一轮我漏了这一步，导致下一个代理在锁上等了 20 分钟。
- 用 `-PfsRunDir=run-f67`，端口自定（建议 25608 / RCON 25568）。
- 系统 `python` 是 Store 占位符，用 `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe -X utf8`。
- `verify_project.py` §1 会因 139 键 vs 文档 117 键判 FAIL（V-07，脚本未升级）；§2 是子集检查看不见新增对象。**两者都不是你的问题**。

## 回报

- 属性核对结论（逐字对比，一致就明说「未改动」）。
- 四条验收的**实际命令与返回原文**；矩阵四行各一行结论（破 / 不破）。
- F3 那一条你用什么服务端读数替代、为什么。
- `git diff --stat`（若为空请明确写「零改动」）、`build` 最后两行。
- **不要 commit**。
