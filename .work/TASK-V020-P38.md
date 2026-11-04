# 任务书 · farmstorm v0.2.0 · Phase 38（F-238 ~ F-240）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。

## 0. 目标（3 个任务，本版第 2 个自定义包 + 第一个世界空间线框）

| 任务 | 标题 | 复杂度 | 依赖 | 交付物 |
|---|---|---|---|---|
| **F-238** | 连线包 + 注册 + 协议版本 2 | M / 1.5 | — | 新建 `network/CultivatorLinkPayload.java`（`Link(core, cultivator)` + `MAX_LINKS = 1024` + 组合 codec）；`FarmstormNetwork` 第二条 `playToClient`；`PROTOCOL_VERSION` 改 `"2"`；自检行补到**两行** |
| **F-239** | 连线广播器：范围与五个时机 | L / 3.0 | F-233、F-238 | 新建 `network/CultivatorLinkBroadcaster.java`；五个时机（进范围 / 出范围 / 绑定变化 / 核被拆 / 登录换维度重生登出）；去抖 **10** 刻、重扫 **20** 刻；出范围发**空列表** |
| **F-240** | 客户端只读与线框渲染 | L / 3.0 | F-238 | 新建 `client/ClientCultivatorLinks.java` 与 `client/CultivatorLinkRenderer.java`；`AFTER_PARTICLES` 阶段画线；换服 / 断线 `clear()` |

**执行顺序**：F-238 → F-239 → F-240。回报**按三个任务分三段**。

## 1. 边界

**允许改/增**：`network/CultivatorLinkPayload.java`（新）、`network/CultivatorLinkBroadcaster.java`（新）、`client/ClientCultivatorLinks.java`（新）、`client/CultivatorLinkRenderer.java`（新）、`network/FarmstormNetwork.java`、`client/FarmstormClient.java`（挂载行）、`outpost/OutpostCoreBlockEntity.java` 或 `blockentity/CultivatorBlockEntity.java`（**只加广播触发调用**，不改既有逻辑）。

**绝对不可动**：
- `network/CoreListPayload.java` 与它的 handler —— **一字不动**（不扩 `core_list`）
- `FarmlandLedger` / `FarmlandGuard`、`registry/`（注册面仍 **97**）、`docs/`、`build.gradle`、`.tools/`、`run/`
- config 的 166 / 58 两处总数注释（收口在 F-242）；`ModLangProvider:13-16`（收口在 F-241）
- **范围公式只准复用 `OutpostCoreBlockEntity.inRange`**（`RANGE_CHUNKS = 1`），**不许写第二份**
- `MAX_LINKS = 1024` 只是**解码上限**，不当截断值用

## 2. 必读

1. `docs/zh/38-PROMPT-MAIN-0.2.0.md` —— 全文（**HC-114 / HC-115**、陷阱 20、§7 回报格式）
2. `docs/zh/39-PROMPT-TASKS-0.2.0.md` —— §0.2 五条重申、**§7（Phase 38 全节：7 条出口判据 / 8 条翻车点）**
3. `docs/zh/37-TASKS-0.2.0.md` —— F-238 ~ F-240 详情（约 468~510 行）、§6 表 F ② ⑫
4. `docs/zh/36-TECH-0.2.0.md` —— **§2.8.1 ~ §2.8.3**、附录 A 第 8 / 9 项、§8 #31 #34、§9 ②
5. `docs/zh/35-DESIGN-0.2.0.md` —— §8.6、B-38 / B-39
6. `docs/zh/34-DECISIONS-0.2.0.md` —— D-169 / D-172

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | Phase 32~37 已提交；build exit 0；注册 **97 / menu_types 2** → OK；toml 175 键 / 60 段（`run-p37` 生成） |
| 现有包 | `farmstorm:core_list`（S2C 只读，`MAX_CORES = 64`，协议串 `"1"`），注册点只有 `FarmstormNetwork` |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`（可 `compileJava`）。**唯一入口** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服 | `run-p38`（复制 `run/eula.txt` + `run/server.properties`），`FS_RUN_LOCK=run-p38`；**不要在 `run/` 里起服** |
| 客户端 | 本机**可以做**：起 dev 客户端看日志（Phase 35 用过）——**但无法用人眼观察画面**。渲染类判据标「未验证」+ 代码层证据 |
| 工具 | `verify_project.py` 仍打 `93 / 93` —— **不许引用它的 `FAIL 0`**（A-66） |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 34 号 / 已有裁决为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。提交由调度者做。
3. 每条判据**改前先跑一遍记下实测值**（陷阱 20），改后再跑一次。
4. **渲染类一律标「未验证」+ 代码层证据**；服务端侧（包数、时机、范围）必须取到读数。

## 5. 实现要点（照做）

### F-238
- `CultivatorLinkPayload`：`Link(core, cultivator)` 记录 + `MAX_LINKS = 1024` + 组合 `StreamCodec`；payload 走 `RegisterPayloadHandlersEvent`（**不进注册面**）。
- `Type` 走 `Farmstorm.id("cultivator_link")`，id 必须是 **`farmstorm:cultivator_link`**。
- `FarmstormNetwork` 加**第二条** `playToClient`；`PROTOCOL_VERSION` 改 `"2"`（两行自检都用它）。
- 自检行补到**两行**（`farmstorm:core_list` 与 `farmstorm:cultivator_link`，各带 id 与版本串）。
- 客户端 handler **只做 `enqueueWork`**，方法体里不引用渲染类（否则专用服务器 `NoClassDefFoundError`）。

### F-239
- 五个时机：进范围 / 出范围 / 绑定变化 / 核被拆 / 登录·换维度·重生·登出。
- 去抖 **10** 刻、重扫 **20** 刻；**出范围发空列表**（不是不发）。
- 范围判定**只调用 `OutpostCoreBlockEntity.inRange`**。
- 服务端任何逻辑**都不得依赖连线**：绑定 / 产出 / 迁移 / 掉落与它无关。

### F-240
- `ClientCultivatorLinks` 只读：收到什么存什么、不自己算绑定、不跨维度缓存；换服 / 断线 `clear()`。
- `CultivatorLinkRenderer`：`RenderLevelStageEvent` 的 `AFTER_PARTICLES` 阶段，**用 `LevelRenderer.renderLineBox` + `RenderType.lines()`**，**不自建 `RenderType`**、不手写顶点（漏 `NORMAL` 会 `Missing elements in vertex` 崩客户端）。
- **新建一个 `PoseStack`** 并只 `translate(-cam.x, -cam.y, -cam.z)`；**不许改事件给的 `getPoseStack()`**。
- 两端各 **1** 个 **0.25** 格小框；颜色 RGB `0.30 / 0.85 / 0.90`、alpha `1.0`、线宽 1.0。
- 先画一条**固定坐标**的线确认可画（36 号 §9 ② 的待验证），再写正式逻辑；退路是 `AFTER_TRIPWIRE_BLOCKS`。

## 6. 验收标准（= 39 号 §7.4 的 7 条）

1. **两个包与协议串**：`latest.log` 出现 **2** 行 `自检 网络包注册`，id 分别是 `farmstorm:core_list` 与 `farmstorm:cultivator_link`，版本串都是 **2**；`playToClient` 在 `FarmstormNetwork` 命中 **2** 处；`PROTOCOL_VERSION = "2"`。
2. **纯原版客户端与专用服务器**：专用服务器起服**无 `NoClassDefFoundError`**；`@Mixin` 全仓 **0**。（原版客户端进服本机无法验，标未验证 + `optional()` 的代码层证据。）
3. **下发与清残留**：范围内收到含 **N** 条 link 的包（N = 该核已绑定的培养器数）；出范围收到**空列表**；拆培养器对应的线立刻消失（不等 20 刻）。
4. **去抖**：同一刻制造连锁事件只发 **1** 包（10 刻去抖）。
5. **渲染**：线框、两端 0.25 小框、颜色/alpha/线宽 —— **人眼类，标「未验证」** + 代码层证据（`renderLineBox` + `RenderType.lines()` + 颜色常量 + PoseStack 用法）。
6. **不崩**：客户端连续在线 10 分钟，`Missing elements in vertex` 与 `IllegalStateException` 各 **0** 条 —— 能起 dev 客户端就实测日志；起不了标未验证。
7. **服务端零依赖**：把 handler 临时改成空实现后重跑绑定 / 产出 / 迁移 / 拆核掉落四条线全部照常；验完**必须还原**且 `git status` 无残留。
8. **注册面不动（负向）**：`自检 注册对象 97 个 … → OK` 不变。

## 7. 必须回报的证据（按三个任务分段）

1. `git diff --stat` 原文 + build 退出码。
2. **F-238 段**：payload 的 shape 与 codec；`FarmstormNetwork` 三处（版本串 / 第二条注册 / 自检行）的原文；**两行自检日志原文**；`playToClient` 命中数。
3. **F-239 段**：五个时机的挂载点；去抖与重扫常量；范围判定**只调用 `inRange`** 的证据（`grep` 里没有第二份范围公式）；范围内 N / 出范围空包 / 拆培养器立刻消失的读数。
4. **F-240 段**：两个客户端类；`renderLineBox` + `RenderType.lines()` + 颜色常量 + `PoseStack` 用法的代码片段；`clear()` 的挂载点；空 handler 那轮的四条功能读数。
5. **负向面**：注册自检行原文；`CoreListPayload` diff 空；`farmland/`、`registry/`、`docs/` diff 空；`@Mixin` 0。
6. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 8. 证据落盘

写 `.work/TASK-V020-P38-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 9. 完成定义

§6 的 8 条全部有读数或有「未验证」+ 原因；回报按三个任务分段。
