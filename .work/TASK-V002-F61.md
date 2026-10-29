# 任务书 V002-F-61 · config 22 键四分组（+ 关闭 V-13 死键）

## 目标（`13-TASKS-0.0.2.md` §3 F-61，经 V-13 追加）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 F-60（已完成）
- 这是 **Phase 11 的最后一件**：做完它，Phase 11 的出口判据才全部满足（77 注册对象已 OK、config 139 键）。

## 交付物

### 1. `config/FarmstormConfig.java`：4 个新分组共 **22** 个键

| 分组 | 键数 | 依据 |
|---|---|---|
| `molotov` | 3 | `12-TECH §8.1` |
| `wild_apple` | 2 | 同上 |
| `wild_hunter` | 11 | 同上 |
| `zangief_cane` | 6 | 同上 |

**键名与默认值以 `.work/v002-BRIEF.md` §8 的唯一清单为准，逐字照抄**（不要自己起名、不要按"常识"定默认值）。三条已裁决的默认值必须对：
- `molotov.fireChance = 85.0`（**A-01**：只留这一个概率键，其余百分比自动放岩浆块）
- `wild_hunter.durationTicks = 700`（**A-02**）
- `zangief_cane.spinDegreesPerTick = 72.0`（**A-05**：1440°/秒 ÷ 20）

书写形态按 **A-04**：正文写 `<group>.<key>`，语言键写 `farmstorm.configuration.<group>.<key>`（语言键属 datagen，本轮不写）。

### 2. `bake()` 派生快照

沿用 v0.0.1 的写法（`FarmstormConfig` 里既有的 `bake()`），把新键里需要派生的部分纳入快照，并保证 `/reload` 时重算。

### 3. 【V-13】把 `WildHunterEffect` 的三个百分比改为读 config

F-64 时因为 config 键还不存在，`WildHunterEffect` 里三项属性修饰器的百分比用了常量 **30/30/40**。本任务落地 `wild_hunter.*` 三键后，**必须同时把这三处改成读 config**——否则这三个键就是**死键**（改了不生效），比没键更糟。

只改这三个数值的来源，**挂载/卸载逻辑一行不动**（F-64 已验证三条路径都不留残余属性，别破坏它）。

## 验收

1. `farmstorm-server.toml` 的**叶节点 139 个**（117 + 22），4 个新分组按字母序出现在文件末尾。**在回报里写清你读的是哪个路径**（跑的是 `run-f61/`，而 `verify_project.py` 读的是 `run/config/`，两者可能不同）。
2. 22 个键名与 `BRIEF §8` 唯一清单逐字一致；抽三个默认值核对（上面那三条）。
3. 把 `molotov.fireChance` 改成 `50.0` → `/reload` → 日志出现一行 `bake()`，读回值 **50.0**；改回 `85.0` 同样生效。
4. 【V-13】把 `wild_hunter` 的某个百分比键改一个明显不同的值，重进游戏确认**属性修饰器的效果随之变化**（证明不再是死键）。

补充硬门禁：`build -PfsRunDir=run-f61` exit 0、零 warning；`自检 注册对象 77 个 … → OK`、`自检 作物 19 种 … → OK`、`自检 幸运浆果池 16 种 … → OK` 三条都在。

## ⚠️ `verify_project.py` 这次会**真的变红**（与 F-65/F-66 不同）

它的第 1 段是**精确比对**：文档口径 117 键 vs toml 实际键集，多出来的 22 个会被判「extra」。所以本轮之后它**会 FAIL**。
- **这是预期的**，不要改脚本、不要改文档去让它变绿。
- 在回报里贴出实际输出，并说明「失败项来自脚本尚未升级到 0.0.2 的 139 键口径」（V-07 已登记，脚本升级单列一件活）。

## 边界

- 只许动：`config/FarmstormConfig.java`、`effect/WildHunterEffect.java`（仅三个百分比的来源）。
- **不许**动 datagen（语言键）、`Farmstorm.java`、`FarmstormItems`、`docs/`。
- 不许新增第 23 个键、不许改既有 117 个键的任何默认值或名字。

## 回报

- toml 叶节点数的**数法**与结果，以及你读的确切路径。
- 22 个键的完整清单（group.key = 默认值 / 范围），与 BRIEF §8 一一对上。
- `/reload` 的 bake 日志原文 + 读回值。
- V-13 的验证方法（怎么看出属性效果随 config 变化）。
- `git diff --stat`、`build` 最后两行、`verify_project.py` 的**实际输出**。
- **不要 commit**。
