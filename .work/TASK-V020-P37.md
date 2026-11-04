# 任务书 · farmstorm v0.2.0 · Phase 37（F-234 ~ F-237）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。

## 0. 目标（4 个任务）

| 任务 | 标题 | 复杂度 | 依赖 | 交付物 |
|---|---|---|---|---|
| **F-234** | `cultivator` 组 2 键 + 2 条语言键 | S / 0.5 | F-224 | 新建**顶层组** `cultivator`：`cycleTicks` **600**（20–72000）、`yield` **2**（1–64）；2 条 `farmstorm.configuration.cultivator.*` |
| **F-235** | 状态机、四道准入闸与放入 | L / 3.0 | F-232、F-234 | 三个 NBT 键（`Held` / `Progress` / `Core`）+ 服务端每刻状态机 + 四闸合并一次判定 + `useWithoutItem` 的放入顺序 |
| **F-236** | 产出与核满暂停 | L / 3.0 | F-226、F-235 | 600 刻结算一次，批次按档案决定（有独立种子 ⇒ 作物 × yield + 种子 × yield）；`acceptAll` 失败则停在 `cycleTicks` |
| **F-237** | 培养器破坏掉落 | S / 0.5 | F-224 | `CultivatorBlock.onRemove`：四条件后用 `NonNullList` 重载掉内部那一个培养物 |

**执行顺序**：F-234 → F-235 → F-236 → F-237。回报**按四个任务分四段**。

## 1. 边界

**允许改**：`blockentity/CultivatorBlockEntity.java`、`block/CultivatorBlock.java`、`config/FarmstormConfig.java`（**只加 `cultivator` 组 2 键**）、`datagen/ModLangProvider.java`（**只加 2 条**）+ 生成的两份 lang。

**绝对不可动**：
- `farmland/` 两个类、`registry/`、`network/`、`team/`、`item/`、`menu/`、`client/`、`docs/`、`build.gradle`、`.tools/`、`run/`
- `OutpostCoreBlockEntity.java`（F-236 只**调用**它的 `canAccept` / `acceptAll`，不修改它）
- config 的 166 / 58 两处总数注释（收口在 F-242）；`ModLangProvider:13-16`（收口在 F-241）
- 注册面仍 **97 / menu_types 2 / block_entities 8**（本节 4 个任务都不进注册面）
- **培养器 BE 不实现 `Container`**（36 号 §2.5.2）——F-237 必须用 `NonNullList` 重载，不是 `Containers.dropContents`
- `working` 必须走 **blockstate 属性**（D-161），不能只做 BE 同步

## 2. 必读

1. `docs/zh/38-PROMPT-MAIN-0.2.0.md` —— 全文（HC-109 / HC-110、陷阱 5 / 14 / 26 / 27 / 29、§7 回报格式）
2. `docs/zh/39-PROMPT-TASKS-0.2.0.md` —— §0.2 五条重申、**§6（Phase 37 全节：6 条出口判据 / 8 条翻车点）**
3. `docs/zh/37-TASKS-0.2.0.md` —— F-234 ~ F-237 详情（约 413~467 行）、§6 表 F ⑤ ⑦ ⑩
4. `docs/zh/36-TECH-0.2.0.md` —— **§2.5.2（状态机四步）**、**§2.5.3（四道闸）**、**§2.5.4（产出两处判据）**、§2.5.6（破坏掉落）、§9 ⑤
5. `docs/zh/35-DESIGN-0.2.0.md` —— §6.2 ~ §6.4、§6.8、B-08 ~ B-18
6. `docs/zh/34-DECISIONS-0.2.0.md` —— D-160 ~ D-164、裁定 #15

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | Phase 32~36 已提交；build exit 0；注册 **97 / menu_types 2** → OK |
| 核心容器 | `OutpostCoreBlockEntity` 已 `implements Container`、`SLOT_COUNT = 54`、有 `canAccept` / `acceptAll` / `simulateAccept`、`Cultivators` 列表（≤16）、`cultivatorCapacity` 16 |
| 培养器 | `CultivatorBlock` 有 `WORKING` 布尔属性与双态模型；`CultivatorBlockEntity` 已写好 `Core` 写入与 `setPlacedBy` 绑定；`serverTick` 仍是空实现 |
| toml | `run/config` 的 toml 是旧文件；本节读 toml 一律在独立 `run-p37` 目录**先删再生**，不要动 `run/config` |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`（可 `compileJava` / `runData`）。**唯一入口** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服 | `run-p37`（复制 `run/eula.txt` + `run/server.properties`），`FS_RUN_LOCK=run-p37`；**不要在 `run/` 里起服**；手工写文件 UTF-8 无 BOM |
| 工具 | `verify_project.py` 仍打 `93 / 93` —— **不许引用它的 `FAIL 0`**（A-66） |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 34 号 / 已有裁决为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。提交由调度者做。
3. 每条判据**改前先跑一遍记下实测值**（陷阱 20），改后再跑一次。
4. 客户端渲染类标「未验证」+ 代码层证据；能用 RCON / `/data get block` 取的一定要取。

## 5. 实现要点（照做）

### F-234
`b.push("cultivator")` 是**顶层组**（与 `outpost_core` 同级）；`cycleTicks` = `defineInRange("cycleTicks", 600, 20, 72000)`、`yield` = `defineInRange("yield", 2, 1, 64)`；配 2 条 `farmstorm.configuration.cultivator.*`。

### F-235 · 状态机与四道闸
- 三个 NBT 键：`Held`（内部那一个培养物）、`Progress`（已走刻数）、`Core`（**引用缓存**，唯一真相在核侧列表）。
- 服务端每刻状态机四步（36 §2.5.2）：**未绑定 / 空槽 → 不 tick；有货 → `Progress++`；到 `cycleTicks` → 交付；`working` 变化时才写 blockstate（带 `UPDATE_CLIENTS`）**。
- 四道闸合并**一次判定**：范围闸（3×3 区块）+ 绑定闸（核侧列表里有它）+ 手持闸（**按 `tier()` 判**：普通/稀有放得进，史诗拒绝）+ 空槽闸；任一不过 = **右键无反应**。
- **准入只读 `tier()`，绝不按 id 白名单**（D-162 明确否决）；`grep` 里不许出现作物 id 比较。

### F-236 · 产出与暂停
- 600 刻结算一次；批次按档案决定：**有独立种子（`seedItem() != null` 且 `!= id()`）⇒ 作物 × yield + 种子 × yield**，否则**只作物 × yield**。
- 准入面内 **5 条给 2 + 2、13 条只给 2**（合计 18 = 11 普通 + 7 稀有）。
- 用**整批**判 `canAccept` / `acceptAll`（F-226 已建）；失败则 `Progress` 停在 `cycleTicks` **不清零**，物品不落地、不丢；腾出空间后从暂停处继续、**不重新计时**。
- **产出只进核**：不许落 `ItemEntity`、不许塞玩家背包（D-164）。

### F-237
`CultivatorBlock.onRemove` 四条件（真移除 ∧ 非活塞推动 ∧ 非同方块替换 ∧ 掉落）后用 **`NonNullList` 重载**掉内部那一个培养物；方块本体由战利品表掉，两处不重叠。

## 6. 验收标准（= 39 号 §6.4 的 6 条，逐条给读数）

1. **新组 2 键（F-234）**：删旧 toml 起服读 `[cultivator]` 恰 **2** 键、值 **600 / 2**；全文 **175 键 / 60 段**（这是**区间读数**，`b.push(` 逐组核对与总数结论只在 F-242）。
2. **准入四闸**：普通作物 → `Held` 有值、`Progress` 从 0 涨、`working=true`；史诗 4 个作物与 2 个史诗种子 → 无反应、手持物不减；槽已占用再右键 → 无反应；未绑定培养器右键 → 无反应。
3. **进度持久**：计时中途走远 200 格再回来、再停服重载 → `Progress` 从原值继续、不重新计时、槽不清空。
4. **产出分类**：喂 `puffed_wheat` → 核里 **+2** 作物 **+2** 种子；喂 `ubi_server`（种子 = 自身）→ 只 **+2** 作物；喂 `chive_seeds` 与喂 `friend_chives` 产出**完全相同**。
5. **核满暂停**：核 54 格塞满再投料 → `Progress == cycleTicks` 且不再前进、物品不落地不丢；清出 **2** 格后自动完成投递、不重新计时。
6. **破坏掉落**：槽里有作物时挖掉 → **1** 方块 + **1** 该作物；空槽 → 只 **1** 方块；活塞推动 → **0** 掉落。
7. **注册与总数不动（负向）**：`自检 注册对象 97 个 … → OK` 不变；本节的 2 条语言键只写「键存在」，`ModLangProvider:13-16` 与 config 总数注释一字不动。

## 7. 必须回报的证据（按四个任务分段）

1. `git diff --stat` 原文 + build 退出码。
2. **F-234 段**：新组与两键的定义原文；`[cultivator]` 2 键读数与全文 175/60；2 条语言键的键名与落点。
3. **F-235 段**：三个 NBT 键的类型与落点；`serverTick` 状态机四步全文；四闸判定的代码（含 `tier()` 的读法）；四种右键场景的 `/data get block` 读数；进度持久的两轮读数。
4. **F-236 段**：批次决定的代码（`seedItem() != null && != id()`）；`canAccept`/`acceptAll` 调用点；三种投料的产出清单（作物/种子各几件）；核满时 `Progress` 与清空后变化。
5. **F-237 段**：`onRemove` 四条件 + `NonNullList` 重载的代码；三种挖法的掉落清单。
6. **负向面**：注册自检行原文；`OutpostCoreBlockEntity` diff 空；`farmland/`、`registry/`、`network/`、`docs/` diff 空。
7. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 8. 证据落盘

写 `.work/TASK-V020-P37-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 9. 完成定义

§6 的 7 条全部有读数或有「未验证」+ 原因；回报按四个任务分段。
