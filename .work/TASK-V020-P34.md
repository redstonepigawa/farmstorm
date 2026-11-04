# 任务书 · farmstorm v0.2.0 · Phase 34（F-220 ~ F-223）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> **本 Phase 两个第一：第一次动 config 的组结构（config 窗口在此打开）、第一次做村民交易（全仓此前 0 命中）。**

## 0. 目标

| 任务 | 标题 | 复杂度 | 依赖 | 涉及文件 |
|---|---|---|---|---|
| **F-220** | 耕地体系停用：5 处注释 + 键定义 + 字段保留 | M / 1.5 | — | `event/FarmstormGameEvents.java`、`config/FarmstormConfig.java` |
| **F-221** | 3 条耕地语言键停用 | S / 0.5 | F-220 | `datagen/ModLangProvider.java` |
| **F-222** | `trade` 组 6 键 + 6 条语言键 | M / 1.5 | F-220 | `config/FarmstormConfig.java`、`datagen/ModLangProvider.java` |
| **F-223** | 农民交易 3 项：`VillagerTradeEvents` + `TierTrade` | L / 3.0 | F-222 | 新建两个 `event/` 类 + `Farmstorm.java` 挂载行 |

**执行顺序**：F-220 → F-221 → F-222 → F-223（F-221 与 F-222 都依赖 F-220；F-223 依赖 F-222 的 6 个键）。回报**按四个任务分四段**，我据此分四次提交。

## 1. 边界

**允许改**：`event/FarmstormGameEvents.java`、`config/FarmstormConfig.java`、`datagen/ModLangProvider.java`、`Farmstorm.java`（只加挂载行）、新建 `event/VillagerTradeEvents.java` 与 `event/TierTrade.java`、datagen 重生成的 `src/generated/resources/assets/farmstorm/lang/*.json`。

**绝对不可动**：
- `farmland/FarmlandLedger.java`（555 行）与 `farmland/FarmlandGuard.java`（225 行）——**一行不改**，两处 `.get()`（`FarmlandGuard:75`、`FarmlandLedger:305`）原样保留
- `OutpostCoreBlockEntity.java`——`settleOnRemoval` 里的 `FarmlandLedger.of`（`:353`）仍在，**F-233 才改**
- `event/FarmstormGameEvents.java` 的 `onServerTickPost` **整段不能注释**：`:170` 的 `CoreChunkTickets.restoreAll(server)` 与 `:175` 的 `CoreListBroadcaster.tick(server)` 一字不动，**只注释 `:171` 一行**
- `registry/`（注册面仍 **93 / menu_types 1**）、`client/`、`textures/`、`models/`、`data/`
- `FarmstormConfig.java` 的 `166` / `58` 两处**总数叙述注释**（config 窗口 F-220 → F-242，总数在 F-242）；`ModLangProvider.java:13-16` 的类注释（语言键窗口 F-215 → F-241）
- `docs/`、`build.gradle`、`.tools/`、`run/`

## 2. 必读

1. `docs/zh/38-PROMPT-MAIN-0.2.0.md` —— 全文（HC-116 / HC-117 / HC-118 / HC-119、陷阱 20 元规则、§7 回报格式）
2. `docs/zh/39-PROMPT-TASKS-0.2.0.md` —— §0.2 五条重申、**§3（Phase 34 全节：9 条出口判据 / 3.5 的 7 条翻车点）**
3. `docs/zh/37-TASKS-0.2.0.md` —— F-220 ~ F-223 详情（约 222~276 行）、**§5.1 A-62 / §5.7 A-68 / §5.8 A-69**
4. `docs/zh/36-TECH-0.2.0.md` —— **§2.3（注释清单与「三处必须保留」）**、**§2.4（交易五步）**、§9 ①
5. `docs/zh/35-DESIGN-0.2.0.md` —— §7.2 / §7.3 / §7.4、§4.2（交易表）、§4.5（断货与补货）
6. `docs/zh/34-DECISIONS-0.2.0.md` —— D-153 / D-154 / D-157 / D-158 / D-159、裁定 #3 / #18

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | Phase 32/33 已提交（`cd05e65`…`82c422b`）；`mod_version=0.2.0`；build exit 0 |
| 起服自检（当前） | 注册 **93**（`menu_types 1`）；作物品质 11/7/4 OK；幸运浆果池 **11** OK |
| toml 现状 | **166 键 / 58 段**、`[outpost_core]` **3** 键、无 `[trade]` |
| lang 现状 | 两份各 **327** 键；`tooltip.farmstorm` **20**；`farmstorm.configuration.` = 169 |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build` 基线 exit 0。**唯一入口，禁止直接调 gradle** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服 | `run-p34`（复制 `run/eula.txt` + `run/server.properties`；`enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），`FS_RUN_LOCK=run-p34`；**不要在 `run/` 里起服**；手工写文件 UTF-8 无 BOM |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 34 号 / 已有裁决为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。提交由调度者做。
3. 每条判据**改前先跑一遍记下实测值**（陷阱 20），改后再跑一次。
4. 交易类验收要么给 `/data get entity … Offers.Recipes` 读数，要么标「未验证」+ 代码层证据。

## 5. 本 Phase 的三个窗口

| 窗口 | 状态 |
|---|---|
| **config** | **本节开窗**（F-220）。只许写「本条键存在 / 不存在」与 toml 区间读数 **165/58 → 171/59**；**不许改 `FarmstormConfig` 的 166 / 58 两处总数叙述** |
| **语言键** | 仍开（F-215 → F-241）。本节只验「3 条耕地键消失」「6 条 trade 键存在」；**`ModLangProvider:13-16` 一字不动** |
| 注册自检 | 仍**未开**（F-224 才开）。注册必须仍是 **93 / menu_types 1** |

## 6. 实现要点（照做）

### F-220 · 五处注释 + 键定义 + 字段保留
- **5 处**（事件面）：`FarmstormGameEvents.java:75`、`:76` 两个注册行；`:241-265` 锄地拦截整段（含 `:254` 的 `ledger.bind`）；`:277-281` 踩踏保护整段（含 `:278` 的 `protectsFromTrample`）；`:171` 的 `FarmlandLedger.get(server).backfill(server)` 单行。
- 每处上方各写**一行**理由：「裁定 #3：v0.2.0 撤销耕地，勿恢复」。
- **A-62**：`FarmstormConfig.java:227` 的**字段声明保留**；**只注释 `:909-912` 的 `.defineInRange(...)` 定义**；`:227` 旁写两行注释说明「定义已注释 ⇒ 字段为 null ⇒ 恢复监听器前必须先恢复定义，否则 `FarmlandGuard.java:75` 抛 NPE」。
- **不注释**：`onServerTickPost` 整段、`:170` `restoreAll`、`:175` `tick`。

### F-221 · 3 条语言键停用
删 `ModLangProvider.java:403` 与 `:412-413` 三条：`farmstorm.configuration.outpost_core.farmlandBonus`、`message.farmstorm.farmland.no_core`、`message.farmstorm.farmland.quota_full`；`:411` 的段注释一并处理。结果：`message.farmstorm` 前缀归 **0**。

### F-222 · `trade` 组 6 键
新建**顶层组** `trade`（与 `outpost_core` 同级，**不是嵌套组**）：`common.min = 2` / `common.max = 5`、`rare.min = 10` / `rare.max = 32`、`epic.min = 40` / `epic.max = 64`，值域全 **1–64**；配 6 条 `farmstorm.configuration.trade.*` 语言键。

### F-223 · 农民交易 3 项（全项目第一次）
- 新建 `event/VillagerTradeEvents.java`（订阅 `VillagerTradesEvent`，**只处理 `VillagerProfession.FARMER`**）与 `event/TierTrade.java`（一项报价的实现）。
- 往 `get(1)` / `get(3)` / `get(5)` **各加一项**；位次写错成 2/4 就一个都看不到（RT-46）。
- 每项：`maxUses = 1`、`xp = 2`、`priceMultiplier = 0.0F`（锁死基准价）、单次给 **2** 个同档作物、**按档在报价生成时抽一次**（不在成交时重掷）。
- 价格取 `trade.<档>.min ~ max`。**不写**第二套补货、**不写**老档补发。
- 若 `getTrades().get(n)` 的 list 不可变，退路是 `put` 一个新的 `ArrayList`（36 §9 ①）——回报里写明你走了哪条。

## 7. 验收标准（= 39 号 §3.4 的 9 条，逐条给读数）

1. **五处注释都带理由行**：`:75` `:76` `:241-265` `:277-281` `:171` 共 **5** 处处于注释态，每处上方各 **1** 行理由。
2. **三处必须保留**：`onServerTickPost` 未注释；`:170` `restoreAll` 与 `:175` `tick` 都在；浮窗每 **100** 刻的对账日志仍在打。
3. **A-62 的两面**：`:227` 字段仍在、`:909-912` 定义已注释、字段旁 **2** 行 NPE 说明；`FarmlandLedger.java` **555** 行 / `FarmlandGuard.java` **225** 行逐字未变（`git diff` 为空）。
4. **config 的两级读数**：删旧 toml 起服两次 —— F-220 后 `[outpost_core]` **2** 键、全文 **165 键 / 58 段**；F-222 后 `[trade]` **6** 键（2/5/10/32/40/64）、全文 **171 键 / 59 段**。
5. **总数注释不动**：`FarmstormConfig` 的 166 / 58 两处与 `ModLangProvider:13-16` 一字不动。
6. **3 条耕地键消失**：3 条键在两份 lang 与 `ModLangProvider` 里各 **0** 命中；`message.farmstorm` 前缀 **0** 条。
7. **交易三项**：农民界面出现 **3** 个本模组项、位次 **1 / 3 / 5**；三项 `maxUses = 1`；`buy.count` 落在 **[2,5] / [10,32] / [40,64]**；`sell` 是 **2** 个同档作物。**能起服就实测；无客户端无法开界面就标「未验证」+ 代码层证据 + 一次 `/data get entity … Offers.Recipes` 的服务端侧尝试。**
8. **断货与不重掷**：成交一次后该项仍在、变灰不可点；补货后价格与作物 id 不变；老档满级农民不补发（不判失败）。
9. **A-69 的时点**：记录里「`farmstorm_farmland.dat` mtime 不变」的断言 **0** 条；改为写明「本节之后拆核仍会重写该文件」并给出 `OutpostCoreBlockEntity.java:353` 仍在的证据。

## 8. 必须回报的证据（按四个任务分段）

1. `git diff --stat` 原文 + `.tools\gw.cmd build` 退出码。
2. **F-220 段**：五处注释的 grep 与上方理由行原文；`onServerTickPost` 的 `:170`/`:175` 原文；A-62 的两面读数；两个保留类的行数与 `git diff --stat`。
3. **F-221 段**：三条被删键的键名与落点；两份 lang 的键数与 `message.farmstorm` 前缀读数。
4. **F-222 段**：6 个字段与 6 条键的落点；两次 toml 全文读数（段数 / 键数 / `[outpost_core]` 键数 / `[trade]` 6 键逐值）。
5. **F-223 段**：两个新类的关键代码（订阅点、位次、`maxUses` / `xp` / `priceMultiplier` / 抽档与定价时机）；`Farmstorm.java` 的挂载行；`Offers.Recipes` 读数或「未验证」+ 代码层证据。
6. **负向面**：注册自检行原文（93 / menu_types 1）；`farmland/` 两个类 diff 为空；`OutpostCoreBlockEntity.java` diff 为空；`registry/`、`client/`、`textures/`、`data/`、`docs/` diff 全空。
7. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 9. 证据落盘

写 `.work/TASK-V020-P34-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 10. 完成定义

§7 的 9 条全部有读数或有「未验证」+ 原因；回报按四个任务分段。
