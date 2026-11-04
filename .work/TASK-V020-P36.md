# 任务书 · farmstorm v0.2.0 · Phase 36（F-229 ~ F-233）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。

## 0. 目标（5 个任务）

| 任务 | 标题 | 复杂度 | 依赖 | 交付物 |
|---|---|---|---|---|
| **F-229** | 核心存储打开：两道闸 | M / 1.5 | F-227 | `block/OutpostCoreBlock.java` 新增 `useWithoutItem` 与 `getMenuProvider`；非己方 `PASS` |
| **F-230** | 核心破坏掉落 | S / 0.5 | F-226 | `OutpostCoreBlock.java:101-107` 的 `onRemove` 追加**四条件**掉落（`Containers.dropContents`） |
| **F-231** | 核侧 16 条绑定列表 + `cultivatorCapacity` | M / 1.5 | F-226 | `OutpostCoreBlockEntity` 增 `Cultivators`（Long 列表，≤ 16）与绑定访问方法；新增 `outpost_core.cultivatorCapacity`（默认 **16**） |
| **F-232** | 放置时绑定：四判据 + 两侧写入 | L / 3.0 | F-224、F-231 | `CultivatorBlock#setPlacedBy` 选核（`distSqr` → `placedGameTime` → 坐标序）；核侧列表与培养器侧 `Core` 都写 |
| **F-233** | 迁移三出口：`settleOnRemoval` 整段改写 | L / 3.0 | F-232 | `OutpostCoreBlockEntity.java:341-394` 整段改写：真移除判定 → 复制列表 → 逐条选接替核 → 三出口 → 只清 `cultivators` → `release()` |

**执行顺序**：F-229 → F-230 → F-231 → F-232 → F-233。回报**按五个任务分五段**。

## 1. 边界

**允许改**：`block/OutpostCoreBlock.java`、`block/OutpostCoreBlockEntity.java`、`block/CultivatorBlock.java`、`blockentity/CultivatorBlockEntity.java`、`config/FarmstormConfig.java`（**只加 `cultivatorCapacity`**）、`datagen/ModLangProvider.java`（**只加对应的 1~2 条键**）+ 生成的两份 lang。

**绝对不可动**：
- `farmland/FarmlandLedger.java` / `farmland/FarmlandGuard.java` —— 一行不改（555 / 225 LF）
- `registry/`、`client/`、`network/`、`team/`、`item/`、`docs/`、`build.gradle`、`.tools/`、`run/`
- `Container.stillValidBlockEntity` 的原版距离（`4.0F`）——**不新设常量、不新增 config 键**（裁定 #17）
- `RANGE_CHUNKS = 1`（3×3 区块）**只用于绑定与连线，不是开箱条件**
- `getContainerSize()` 必须读 `storageSlots`（**54**）；**16 只属于 `Cultivators`**
- config 的 166 / 58 两处总数注释（收口在 F-242）；`ModLangProvider:13-16`（收口在 F-241）
- 注册面仍 **97 / menu_types 2**（本节 5 个任务都不进注册面）

## 2. 必读

1. `docs/zh/38-PROMPT-MAIN-0.2.0.md` —— 全文（HC-111 / HC-112 / HC-113、陷阱 4 / 13 / 14、§7 回报格式、§5 事实与推断的分界线）
2. `docs/zh/39-PROMPT-TASKS-0.2.0.md` —— §0.2 五条重申、**§5（Phase 36 全节：9 条出口判据 / 10 条翻车点）**
3. `docs/zh/37-TASKS-0.2.0.md` —— F-229 ~ F-233 详情（约 347~412 行）、§5.8 A-69
4. `docs/zh/36-TECH-0.2.0.md` —— **§2.6.1 ~ §2.6.4（开箱两道闸）**、**§2.7（迁移七步）**、§6 ①②③
5. `docs/zh/35-DESIGN-0.2.0.md` —— §8.2 ~ §8.5（含两个算例）、B-12 ~ B-29
6. `docs/zh/34-DECISIONS-0.2.0.md` —— D-165 ~ D-168、裁定 #8 / #17

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | Phase 32~35 已提交（`cd05e65`…`bd97094`）；build exit 0 |
| 注册 | **97 / menu_types 2 → OK**（关窗已确认；本节可以引用 `→ OK`） |
| 工具 | `tools/verify_project.py` 仍打 `已核 registry 对象: 93 / 93` 并退出 0 —— **A-66：本节任何判据都不许引用它的 `FAIL 0`**，若跑只记一条读数并标注「少核 4 个对象」 |
| toml（旧产物） | `run/config/farmstorm-server.toml` 仍是**旧文件**；本节读 toml 一律在独立 `run-p36` 目录**先删再生**，不要动 `run/config` |
| 可用的既有件 | `OutpostCoreBlockEntity` 已 `implements Container`、`SLOT_COUNT = 54`、`Items` NBT、`getUpdateTag` 只写 owner 三键；`OutpostCoreMenu` / `OutpostCoreScreen` 已注册但**界面还开不了**（F-229 接上） |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`（可 `compileJava` / `runData`）。**唯一入口** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服 | `run-p36`（复制 `run/eula.txt` + `run/server.properties`），`FS_RUN_LOCK=run-p36`；**不要在 `run/` 里起服**；手工写文件 UTF-8 无 BOM |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 34 号 / 已有裁决为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。提交由调度者做。
3. 每条判据**改前先跑一遍记下实测值**（陷阱 20），改后再跑一次。
4. 客户端渲染 / 需要真人操作的项标「未验证」+ 代码层证据；能用 RCON / `/data get block` 取得的一定要取。

## 5. 实现要点（照做）

### F-229 · 两道闸
- `OutpostCoreBlock.useWithoutItem`：**只判「己方」**，非己方返回 `PASS`（不开界面）。
- **距离交给菜单的 `stillValid`**（原版 `Container.stillValidBlockEntity` 语义，`4.0F`）——**不许在这里自己判距离**，也不许用 `RANGE_CHUNKS`。
- `getMenuProvider` 接上 `OutpostCoreMenu`。

### F-230 · 掉落四条件
`onRemove` 里追加掉落，**四个条件必须一起写**：真移除 ∧ 非活塞推动（`!state.hasBlockEntity()` 语义按源码核实，别漏 `movedByPiston`）∧ 非同方块替换 ∧ 掉落走 `Containers.dropContents`。漏 `movedByPiston` 就是**物品复制漏洞**。

### F-231 · 核侧 16 条
- `OutpostCoreBlockEntity` 增 `Cultivators`（Long 列表，≤ 16）+ 访问方法；**唯一真相在核侧**，培养器侧的 `Core` 只是引用缓存。
- 新增 `outpost_core.cultivatorCapacity`：默认 **16**、值域 **1–64**。
- `getContainerSize()` 仍读 `storageSlots`（**54**）——**16 与 54 不许混**。

### F-232 · 放置绑定四判据
- `CultivatorBlock#setPlacedBy` 选核：**先过滤**（覆盖 ∧ 己方 ∧ 未满）**再取最近**；序为 `distSqr` → `placedGameTime` → 坐标字典序。
- 两侧都写：核侧列表 + 培养器侧 `Core`。
- 无可用核时方块**照样放下**，但**永久未绑定、不可使用**；**不自动认领**（事后补核也不会绑上）。

### F-233 · 迁移三出口
- 整段改写 `settleOnRemoval`（`:341-394`）：真移除判定 → **先复制一份 `cultivators()` 再遍历** → 逐条选接替核 → 三出口 → 只清 `cultivators` → `release()`。
- 出口①：改绑到接替核（覆盖 ∧ 己方 ∧ 有空位，最近优先）；②：一个都没有 ⇒ **留原地、内容保留、`Core` 键消失、不可使用**（**不变泥土、不掉落**）；③：区块取不到 ⇒ **NBT 一个字节不动**。
- **方法体内 `setBlock` 必须 0**；**全程 `getChunkNow`**，不为清算加载区块。
- 改完后本类对 `FarmlandLedger` / `FarmlandGuard` 的引用（含 import）归 **0**。

## 6. 验收标准（= 39 号 §5.4 的 9 条，逐条给读数）

1. **两道闸**：己方开 54 格（6×9 + 玩家背包，标题「据点核心」，存 3 个物品关掉再开仍在）；非己方**不开**；**9 格外**发不出交互、**8 格内**打开后走到 9 格被原版 `stillValid` 关掉。**无客户端就标未验证 + 代码层证据。**
2. **掉落四条件**：核里 3 组不同物品被挖掉后 3 组全掉；**活塞推动 0 掉落**；`/setblock` 同方块 **0 掉落**；区块卸载后物品仍在核里；空核只掉 **1** 个方块本体。
3. **绑定列表**：`/data get block <core>` 出现 `Cultivators`（绑 1 个后 **1** 条 long）；删旧 toml 起服后 `[outpost_core]` **4 键**、全文 **173 键 / 59 段**；`cultivatorCapacity = 2` 时第 **3** 个培养器放得下但未绑定。
4. **放置绑定**：A 满 16、B 有空位 → 绑 **B**（两侧 `/data get block` 一致）；同刻两核等距 → 绑 `placedGameTime` 更小者且重跑结果相同；附近无己方核 → **缺 `Core` 键**、右键无反应；事后补核 → `Core` 仍缺失。
5. **迁移三出口**：有接替核 → 改绑、`Held` / `Progress` 不变；无接替核 → 留原地、`Core` 键消失、内容保留、右键无反应、**不变泥土、不掉落**；区块不在内存 → NBT **一字节不变**（`Core` 仍是旧核坐标）。
6. **`setBlock` 与两个死类**：`settleOnRemoval` 方法体内 `setBlock` **0**；`FarmlandLedger` / `FarmlandGuard` 在本类的引用（含 import）归 **0**。
7. **A-69 的时点**：**开工先记一次** `run/world/data/farmstorm_farmland.dat` 的 mtime；**F-233 落地后再记一次**；文件存在且两次 mtime 相同。F-233 之前的记录里**不许**出现这条断言。
8. **注册与窗口（负向）**：`自检 注册对象 97 个 … → OK` 不变；`menu_types 2`、`block_entities 8` 不变。
9. **工具面（A-66）**：若跑 `verify_project.py`，只允许记一条读数并标注「待 F-243 同步，此读数少核 4 个对象」；**任何判据都不引用它的 `FAIL 0`**。

## 7. 必须回报的证据（按五个任务分段）

1. `git diff --stat` 原文 + build 退出码。
2. **F-229 段**：`useWithoutItem` / `getMenuProvider` 全文；`stillValid` 用的是原版语义的证据；两道闸的实测或未验证。
3. **F-230 段**：四个条件的代码原文；四个场景的实际掉落清单（空清单写「空」）。
4. **F-231 段**：`Cultivators` 字段与方法；`cultivatorCapacity` 定义；toml 173/59 与 `[outpost_core]` 4 键读数；容量 2 的算例。
5. **F-232 段**：选核四判据的代码；两侧写入的落点；四个对照的 `/data get block` 原文。
6. **F-233 段**：整段改写后的 `settleOnRemoval` 全文；三出口的实测读数；`setBlock` / `getChunkNow` / 两个死类引用的 grep 读数。
7. **A-69 段**：两次 mtime 读数（开工 / F-233 之后）。
8. **负向面**：注册自检行原文；`farmland/` 两个类 diff 空；`registry/`、`network/`、`docs/` diff 空。
9. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 8. 证据落盘

写 `.work/TASK-V020-P36-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 9. 完成定义

§6 的 9 条全部有读数或有「未验证」+ 原因；回报按五个任务分段。
