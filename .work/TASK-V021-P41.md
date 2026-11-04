# 任务书 · farmstorm v0.2.1 · Phase 41（F-248 ~ F-250）

> 派发者：调度 Agent。工作目录 `D:\project_farm`（Windows / pwsh）。**3 个任务、4.5 人日。**

## 0. 目标：让核每 100 刻把 3×3 已加载区块里「没人要」的培养器补进自己的 16 个名额

| 任务 | 标题 | 复杂度 | 交付物 |
|---|---|---|---|
| **F-248** | `util/AreaBlockEntities#collect` | M / 1.5 | 新 `util/AreaBlockEntities.java`（唯一方法 `collect`，只走 `getChunkNow`，返回新 `ArrayList`） |
| **F-249** | 相位门与写入边界：`rebindDue` + `rebindNearby` | M / 1.5 | `OutpostCoreBlockEntity` 常量区加 `REBIND_INTERVAL_TICKS = 100`；方法区加两个方法（**本任务不接线**） |
| **F-250** | `serverTick` 接线 + `CultivatorBlock:73-84` 注释同改 | M / 1.5 | `:507-525`（`now` 上移一行 + 独立门 + 一条补绑 INFO）、`CultivatorBlock:73-84` 的类注释 |

**执行顺序** F-248 → F-249 → F-250，回报**按三段**（我据此分三次提交）。

## 1. 边界

**允许改/增**：`util/AreaBlockEntities.java`（新）、`blockentity/OutpostCoreBlockEntity.java`、`block/CultivatorBlock.java`（**只 :73-84 的注释**）。

**绝对不可动**：
- `FarmstormConfig.java` 一行不动（`b.push(` **60**、`b.pop()` **60**、行首 `.define*` **175**）——本 Phase **不新增任何 config 键与语言键**
- `CultivatorBlock:85-102` 的 `setPlacedBy` 一行不动
- `removeCultivator(` 的调用点仍是 **0**（只有 `:366` 声明与 `CultivatorLinkBroadcaster:35` 的 javadoc 提及）
- `pruneCultivators(` 的调用点仍 **1**（`OutpostCoreBlockEntity:329`，在 `cultivators()` 里）
- `registry/`、`network/`、`client/`、`menu/`、`docs/`、`build.gradle`、`.tools/`、`run/`

## 2. 必读

1. `docs/zh/44-PROMPT-MAIN-0.2.1.md` —— 全文（**HC-121~HC-137**、N-115~N-126、§4 的 41 条陷阱、§6.1 的 17 行已知冲突、§7 回报格式）
2. `docs/zh/45-PROMPT-TASKS-0.2.1.md` —— §0.2 五条重申、**§2（Phase 41 全节：7 条出口判据 / 8 条翻车点）**
3. `docs/zh/43-TASKS-0.2.1.md` —— F-248 ~ F-250 详情（约 131~177 行）、**§5.2 A-71**
4. `docs/zh/42-TECH-0.2.1.md` —— **§2.1.1 ~ §2.1.4**
5. `docs/zh/41-DESIGN-0.2.1.md` —— §2.2 ~ §2.7、§9 的 B-01 ~ B-12

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | Phase 40 已提交；`mod_version=0.2.1`；build exit 0；`build/libs` 是 0.2.1 两支 |
| `OutpostCoreBlockEntity` | **782 行（UTF-8 读法）** |
| ⚠️ 行号陷阱 | **默认 `Get-Content` 会把同一份文件读成 668 行，行号随之后移** —— 一律用 `[IO.File]::ReadAllLines($p, [Text.Encoding]::UTF8)` 取行号 |
| 关键锚点 | `RANGE_CHUNKS = 1`（`:75`）、`TICK_LOG_ROUNDS = 30`（`:91`）、`serverTick` 的 `:507-525` |
| Java 文件数 | **145 → 146**（F-248 后） |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build` 基线 exit 0。**唯一入口** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH） |
| 起服 | `run-p41`（复制 `run/eula.txt` + `run/server.properties`），`FS_RUN_LOCK=run-p41`；**不要在 `run/` 里起服** |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 40 号 / 已有裁决为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。提交由调度者做。
3. 每条判据**改前先跑一遍记下实测值**，改后再跑一次。
4. 读数只用四类：**文件数与键数、日志行、`/data get block`、源码 grep 计数**；不写「手感」「正常」「差不多」。

## 5. 实现要点（关键禁令）

### F-248 · `AreaBlockEntities.collect`
- **只走 `getChunkNow`**：`getChunk(` / `getChunkAt(` / `getBlockEntity(` 命中必须 **0**（那三条会把区块拉进内存，RT-52）。
- **先 `new ArrayList` 复制活映射再过滤**（两阶段写法），否则遍历中增删 → `ConcurrentModificationException`。
- `type.isInstance` ≥ 1；`BlockEntityType` 命中 **0**（泛型取类型，不要硬编码枚举）。

### F-249 · `rebindDue` + `rebindNearby`
- `REBIND_INTERVAL_TICKS = 100` 是**编译期常量、不是 config 键**（本版 config 只 +1，见 D-185）。
- `rebindDue` 的模数是 **100**、方法体里**不出现 `interval`**，**不借粒子门的 `interval <= 0` 守卫**。
- `rebindNearby` 方法体：**第一行** `ownerUuid == null` 的返回；排序链 `comparingDouble` **1** 次 + `thenComparingInt` **3** 次（x / z / **y** 一级不能漏）；`addCultivator(` 与 `setCore(` 各 ≥1 且**前者在文本上先出现**；`pruneCultivators(` / `removeCultivator(` / `cultivators()` 各 **0**。
- 两侧不一致一律**跳过**，不许调 `removeCultivator` 做兜底（那会造出第二个主，与 D-177 冲突）。

### F-250 · 接线 + 注释 + INFO 同一提交（A-71）
- **独立 `if`、放在粒子门之前、不 `return`**：接在粒子门之后会被筛成 `LCM(40,100)=200` 刻并随 `boundaryParticleIntervalTicks` 漂移。
- `now` 上移一行；一条补绑 INFO 带**核坐标 + 本轮新增条数 + 累计/上限**。
- `CultivatorBlock:73-84` 的注释同改：删掉「不自动认领 / 不会绑上」这类过时说法；`setPlacedBy` 一行不动。

## 6. 验收标准（= 45 号 §2.4 的 7 条）

1. **区域枚举只走已加载区块**：`getChunkNow` 调用点 1；`getChunk(`/`getChunkAt(`/`getBlockEntity(` 0；`new ArrayList` ≥1；`type.isInstance` ≥1、`BlockEntityType` 0。
2. **能编译、Java 文件数 +1**：build exit 0；`src/main/java` 的 `.java` 从 **145 → 146**。
3. **相位门独立、模数固定**：`REBIND_INTERVAL_TICKS` = **100**；`rebindDue` 模数 100、体内无 `interval`、无 `interval <= 0` 守卫。
4. **写入边界三禁令**：`rebindNearby` 首行 `ownerUuid == null` 返回；`pruneCultivators`/`removeCultivator`/`cultivators()` 各 0；`addCultivator(` 与 `setCore(` 各 ≥1 且前者先出现；`comparingDouble` 1 + `thenComparingInt` 3。
5. **全仓两个调用点没被顺手启用**：`pruneCultivators(` 调用点仍 **1**（`:329`）；`removeCultivator(` 调用点仍 **0**。
6. **粒子间隔 40 下 100 刻内补上** + **粒子间隔 1200 下仍 100 刻内补上**（**成对**，缺一条不算通过）：这两条是判决性的——复用粒子门会在 1200 那测红。
7. **满则停 / 无主核不补 / 注释同改**：3×3 内 20 个未绑定 + `cultivatorCapacity=16` ⇒ `Cultivators` 恰 **16** 条、再等 300 刻仍 16；`/setblock` 的无主核 + 1 个未绑定 ⇒ 等 300 刻仍为空；`CultivatorBlock` 里旧说法命中 **0**、`setPlacedBy` 的 diff 为空。

## 7. 必须回报的证据（按三段）

1. `git diff --stat` 原文 + build 退出码 + Java 文件数（145 → 146）。
2. **F-248 段**：新文件全文要点 + 四类 grep 计数（`getChunkNow` / 三条会加载的 API / `new ArrayList` / `type.isInstance`）。
3. **F-249 段**：常量声明；两个方法全文；排序链逐级点名；`pruneCultivators(` / `removeCultivator(` 的全仓调用点计数。
4. **F-250 段**：`:507-525` 改前 → 改后；补绑 INFO 的**完整原文**；`CultivatorBlock` 注释 diff。
5. **三组读数**：间隔 40 / 间隔 1200 / 满 16 场景 + 无主核场景的 `/data get block <core> Cultivators`。
6. **负向面**：`FarmstormConfig` diff 空（`b.push`/`b.pop`/`.define*` 三个数不变）；`registry/`、`network/`、`client/`、`docs/` diff 空；`setPlacedBy` diff 空。
7. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 8. 证据落盘

写 `.work/TASK-V021-P41-evidence.md`（新建）。回报正文只贴结论行与第一条错误。
