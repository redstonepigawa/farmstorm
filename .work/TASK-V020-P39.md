# 任务书 · farmstorm v0.2.0 · Phase 39（F-241 ~ F-246）· 收口与全量验收

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> **铁律：F-241 / F-242 / F-243 改文件，F-244 ~ F-246 一律不改码——它们只产出记录。** 验收不通过就如实报「不通过」+ 证据，**不许就地改判据、不许改实现让记录变绿**。

## 0. 目标（6 个任务）

| 任务 | 标题 | 复杂度 | 交付物 |
|---|---|---|---|
| **F-241** | 语言键总账 337 与类注释 | M / 1.5 | 重跑 datagen；两份 lang 各 **337** 键；`ModLangProvider.java:13-16` 的逐项构成注释改成与键集一致 |
| **F-242** | config 总账 175 键 / 60 组 | M / 1.5 | 删旧 `farmstorm-server.toml` 重生成；逐组对 36 号 §4.1；`[outpost_core]` 4 键无 `farmlandBonus` |
| **F-243** | 验证工具同步 97 / 175 / 337 | S / 0.5 | `tools/verify_project.py` 的 `EXPECTED` / `EXPECTED_COUNTS` / config 期望来源 / 打印串**四处**同步 |
| **F-244** | 36 号 §8 的 36 条验收 | L / 3.0 | 新建 `.work/v020-verify/acceptance.md`（36 行 + 表头） |
| **F-245** | 35 号 §12 的 40 条边界复跑 | L / 3.0 | 新建 `.work/v020-verify/boundaries.md`（40 行 + 表头） |
| **F-246** | 红线、注册面与上游一致性签字 | M / 1.5 | 新建 `.work/v020-verify/signoff.md`：8 行签字表 + 上游四份文档 diff 为空 |

**执行顺序**：F-241 → F-242 → F-243 → F-244 → F-245 → F-246。回报**按六个任务分六段**。

## 1. 边界

**允许改**：两份生成的 `lang/*.json`、`datagen/ModLangProvider.java`（**只改 `:13-16` 的构成注释**）、`run/config/farmstorm-server.toml`（**只有 F-242 的删除重生成这一处**）、`tools/verify_project.py`（**只有 F-243**）、新建 `.work/v020-verify/` 三份记录与截图。

**绝对不可动**：
- `src/main/java/` 的**任何 Java**（F-244 ~ F-246 不改码；F-241 只动注释、F-242 只删 toml、F-243 只动工具）
- `docs/` 全树（**34~39 与 00~33 都只读**；`git diff --stat docs/` 必须为空）
- `farmland/` 两个类（555 / 225 LF）
- `build.gradle`、`.tools/`、`run/` 的其它文件
- 注册面仍 **97**、`playToClient` **2**、`PROTOCOL_VERSION = "2"`、`@Mixin` **0**

## 2. 必读

1. `docs/zh/38-PROMPT-MAIN-0.2.0.md` —— 全文（HC-101 / HC-120 / HC-115、陷阱 8 / 17 / 20 / 21、§7 回报格式）
2. `docs/zh/39-PROMPT-TASKS-0.2.0.md` —— §0.2 五条重申、**§8（Phase 39 全节：10 条出口判据 / 9 条翻车点）**、§0.4（三个窗口台账）
3. `docs/zh/37-TASKS-0.2.0.md` —— F-241 ~ F-246 详情（约 511~594 行）、**§6 六张覆盖表**、§5 A-62~A-69
4. `docs/zh/36-TECH-0.2.0.md` —— **§8 的 36 条验收**、**§4.1（config 全表）**、§4.3.1~§4.3.3（语言键逐条）
5. `docs/zh/35-DESIGN-0.2.0.md` —— **§12 的 40 条边界**
6. `docs/zh/34-DECISIONS-0.2.0.md` —— §4.2 的 19 条改动面、§8 的 8 条既存瑕疵

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | Phase 32~38 已提交；build exit 0 |
| 注册 | **97 / menu_types 2 → OK**；`playToClient` **2**；`PROTOCOL_VERSION = "2"`；`@Mixin` **0** |
| lang 现状 | 两份各 **337**（区间读数已到位，本节 F-241 才写总账与类注释） |
| toml 现状 | `run/config/farmstorm-server.toml` 是**旧文件**（58 段）；F-242 删掉重生成 |
| 工具现状 | `verify_project.py` 仍打 `已核 registry 对象: 93 / 93` 并退出 0 —— **A-66 的假绿，F-243 之前不许引用它的 `FAIL 0`** |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`（可 `runData`）。**唯一入口** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服 | `run-p39`（复制 `run/eula.txt` + `run/server.properties`），`FS_RUN_LOCK=run-p39`；**不要在 `run/` 里起服** |
| 记录目录 | `.work/v020-verify/`（可写） |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 34 号 / 已有裁决为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。提交由调度者做。
3. 每条判据**改前先跑一遍记下实测值**（陷阱 20），改后再跑一次。
4. **渲染 / 听感 / 需第二客户端的项标「未验证」+ 代码层证据**，不许编读数。
5. **A-66**：F-243 之后引用工具，必须**同时**给 `已核 registry 对象: 97 / 97` 与 `FAIL 0`；只写 `FAIL 0` 的一律按假绿处理。

## 5. 实现要点

### F-241 · 语言键总账
两份 lang 各 **337**、键集相同；逐前缀 `farmstorm.configuration` **178** / `item` **33** / `block` **27** / `container` **2** / `tooltip` **20** / `message` **0**；`ModLangProvider.java:13-16` 的构成注释改成与键集一致（`323` 归一 **0**、`337` ≥ 1）。

### F-242 · config 总账
**删掉** `run/config/farmstorm-server.toml` 重生成（这是本版唯一允许动 `run/config` 的一处）；**175 键 / 60 段**（`^\s*\[` 计数）；`[outpost_core]` **4** 键无 `farmlandBonus`；`[cultivator]` **2** 键（600/2）、`[trade]` **6** 键（2/5/10/32/40/64）；`farmstorm-client.toml` 仍 **3** 键；`FarmstormConfig.java` 的 `b.push(` 次数对 60。

### F-243 · 工具四处同步
`EXPECTED`（加 4 个新 id）、`EXPECTED_COUNTS`（93→**97** 与各类分项）、config 期望来源（并集加 36 号 §4.1 的 **175** 键，**不写第二份手写清单**）、打印串（`/ 93` → `/ 97`）。
**活判据**：把 `EXPECTED_COUNTS["blocks"]` 临时改成 28 → 退出码非 0；改回 → 回 0。

### F-244 · 36 条验收
`.work/v020-verify/acceptance.md`：**36 行** + 表头；每行「观察位置」「实际值」两列都非空；未通过 **0**；#1 的日志原文与 36 号 §8 #1 逐字一致（含 `→ OK` 与 12 个分项）。

### F-245 · 40 条边界
`.work/v020-verify/boundaries.md`：**40 行** + 表头；B-12 / B-13 / B-15 / B-16 / B-19 / B-20 各有 `/data get block` 的实际 NBT 值（`Core` 键有无是关键）；B-26 / B-27 各记实际掉落清单（空清单写「空」）；**B-33 / B-34 按预期写「不判失败」**（老档满级农民不补发、交易是抽样，都是被接受的代价）。

### F-246 · 签字
`.work/v020-verify/signoff.md`：**8 行**签字表（注册面 97 逐类 / config 175 键 60 组 / 语言键 337×2 / 动作面 46/46 / 红线三项 / `Farmstorm.java` 的 5 个自检数字 / `.dat` / 两个死类行数），每行给「判据 / 实测值 / 结论」；另加 34 行任务核对表（未达成 **0**）；`git diff --stat docs/zh/34 35 36` 为空。

## 6. 验收标准（= 39 号 §8.4 的 10 条）

1. **语言键总账**：各 **337**、`Compare-Object` 为空；逐前缀 178 / 33 / 27 / 2 / 20 / 0；`323` 在 `ModLangProvider` 命中 0。
2. **config 总账**：**175 键 / 60 段**；`[outpost_core]` 4 键无 `farmlandBonus`；`[cultivator]` 2、`[trade]` 6；client toml 仍 3；`b.push(` = 60。
3. **工具四个读数一起给**：退出码 **0**，**同时**有 `已核 registry 对象: 97 / 97`、`175 个配置键与文档逐字一致`、`共 337 键`、`结论：FAIL 0 项`。
4. **工具的活判据**：`EXPECTED_COUNTS["blocks"]` 临时改 28 → 非 0；改回 → 0；`git diff --stat docs/` 为空。
5. **36 条验收**：36 行 + 表头、未通过 0、两列非空、#1 逐字一致。
6. **40 条边界**：40 行 + 表头、不符 0、指定编号有实际 NBT/掉落清单、B-40 写明「临时副本上恢复、验完还原、`git status` 无残留」。
7. **三份记录的时点**：`.dat` mtime 判据只出现在 F-244 的 #14 与 F-246 第 ⑦ 项（共 2 处），都在 F-233 之后。
8. **签字**：三份上游文档 diff 为空；`@Mixin` 0、`playToClient` 2、协议串 2；两个死类 555 / 225 行。
9. **8 行签字表** + **34 行任务核对表**（未达成 0）。
10. **76 条全有结论**：36 + 40 = 76 逐条有读数、未通过 0；两份记录各写各的，谁都不替谁签字。

## 7. 必须回报的证据（按六个任务分段）

1. `git diff --stat` 原文；build 退出码。
2. **F-241 段**：两份 lang 的键数与逐前缀读数；`ModLangProvider:13-16` 的改前→改后。
3. **F-242 段**：删 toml 重生成后的段数 / 键数 / 四个组逐键读数；`b.push(` 计数。
4. **F-243 段**：四处同步的 diff；工具**四段完整原文** + 退出码；活判据的两次退出码。
5. **F-244 段**：36 行的结论分布（实测 / 代码层证据 / 未验证）；未通过数；#1 的日志原文。
6. **F-245 段**：40 行的结论分布；指定编号的 NBT/掉落读数；B-33/B-34 的处置。
7. **F-246 段**：8 行签字表；34 行核对表；红线三项与两个死类行数的读数。
8. **负向面**：`src/` 零改动（F-244~F-246）；`docs/` diff 空；`git status` 只有 `.work/v020-verify/`。
9. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 8. 证据落盘

三份记录进 `.work/v020-verify/`，过程证据进 `.work/TASK-V020-P39-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 9. 完成定义

§6 的 10 条全部有读数或有「未验证」+ 原因；回报按六个任务分段。
