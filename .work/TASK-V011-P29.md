# 任务书 · farmstorm v0.1.1 · Phase 29（F-187 ~ F-198）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> **这是 v0.1.1 最大的一节：12 个任务、21.5 人日、两处存档格式变更、一条删除链、一处新增。** 一次全做完，但严格按下面的顺序。

---

## 0. 目标

把「范围内即受管」换成「显式绑定」——每个耕地只归 1 个核管理，一个核最多管 16 块（容量复用 `outpost_core.farmlandBonus`）。

| 任务 | 标题 | 复杂度 | 依赖 |
|---|---|---|---|
| **F-187** | `TeamResolver` 的 `Owner × Owner` 重载 | M / 1.5 | — |
| **F-188** | `FarmlandLedger` v2：绑定表、`orphans`、兼容读与写入口 | L / 3.0 | — |
| **F-189** | `OutpostCoreIndex.Entry` 增加 `gameTime` 与三个只读访问器 | M / 1.5 | — |
| **F-190** | 启动补建 `backfill` | M / 1.5 | F-188、F-189 |
| **F-191** | 额度 / 已用改口径，删 `base` 三件套 | M / 1.5 | F-187、F-188、F-189 |
| **F-192** | 删 config 的 `farmland` 整组与 1 条语言键 | S / 0.5 | **F-191** |
| **F-193** | `FarmlandGuard.check` 选核 + 单核空位 | L / 3.0 | F-187、F-188、F-189 |
| **F-194** | 锄地成功时写入绑定 | M / 1.5 | F-193 |
| **F-195** | 耕地保护的离线判定与踩踏面 | M / 1.5 | F-187、F-188 |
| **F-196** | 核被毁：先迁移、三支降级 | L / 3.0 | F-188、F-189、F-195 |
| **F-197** | 删 `/farmstorm farmlandlimit` 整棵服务端指令树 | M / 1.5 | **F-191** |
| **F-198** | 核心所在区块的运行期临时 ticket | M / 1.5 | F-189 |

### 两条串行链，**不许打散**（A-56 是编译顺序，不是风格）

1. **`F-191 → F-192 → F-197`**：`base()` 是 `farmland.limitPerPlayer` 的唯一读者 → `base()` 先无读者 → config 组才可删 → 指令树才可整棵删。
2. **`F-193 → F-194 → F-196`**：选核 → 写绑定 → 迁移都读同一个 `Decision.core`。

两条链之间可自由排序。`F-187 / F-188 / F-189` 三个无前置的**先落地**，`F-190 / F-191 / F-193 / F-195 / F-198` 才可开。

## 1. 边界

**允许改/增/删**：
- `team/TeamResolver.java`、`team/ScoreboardTeamResolver.java`
- `farmland/FarmlandLedger.java`
- `outpost/OutpostCoreIndex.java`
- `farmland/FarmlandGuard.java`
- `event/FarmstormGameEvents.java`
- `blockentity/OutpostCoreBlockEntity.java`
- `config/FarmstormConfig.java`（**只删 `farmland` 组**）
- `datagen/ModLangProvider.java`（**只删 2 条**：`farmstorm.configuration.farmland.limitPerPlayer`、`command.farmstorm.farmlandlimit.set`）
- `command/FarmstormCommands.java` —— **整文件删除**
- **新建** `outpost/CoreChunkTickets.java`
- datagen 重新生成的 `src/generated/resources/assets/farmstorm/lang/*.json`

**绝对不可动**：
- `util/DeployedOwnership.java` 的签名与调用点
- `isOwnerOrAlly` 保持「只有定义、无调用者」，**不复活它**
- `Verdict` 的三态结构；`message.farmstorm.farmland.quota_full` 与 `.no_core` 的**键名**（只许改文案内容）
- `Farmstorm.java` 的 12 类自检数字（注册面仍 93）
- `docs/` 任何文件；`build.gradle`、`settings.gradle`、`.tools/`；`.work/v011-*.md` 四份台账
- 除 `farmland` 组以外的任何 config 键与默认值
- 客户端侧任何东西（浮窗是 Phase 30 的活）

## 2. 必读（按顺序，长但必须）

1. `docs/zh/32-PROMPT-MAIN-0.1.1.md` 全文
2. `docs/zh/33-PROMPT-TASKS-0.1.1.md` §0.2、**§4（Phase 29 全节，含 9 条出口判据与 11 条翻车点）**、§8
3. `docs/zh/31-TASKS-0.1.1.md` **F-187 ~ F-198 全部详情**（约 340~569 行，每个任务都给目标/涉及文件/实现要点/验收）
4. `docs/zh/30-TECH-0.1.1.md` **§2.2（耕地-核心绑定表全节，270~515 行：本体三份、两个 record、落盘、兼容读、`setDirty` 表、惰性清理）**、§2.3（删指令树）、§2.5（ticket，含 `distance = 1` 的加载面）、§7 的 #16 ~ #22 与 #36、§9 已知限制（A-49）
5. `docs/zh/29-DESIGN-0.1.1.md` §3.4 / §3.6 / §3.10、§11 的 B-15 ~ B-25
6. `docs/zh/28-DECISIONS-0.1.1.md` D-134 ~ D-140、D-145 与 A-39 / A-42 / A-43 / A-45 / A-47
7. `.work/v011-BRIEF.md` §1.2 / §1.3 / §1.9 + `.work/v011-verdicts.md`（A-35 / A-36 / A-37 / A-39 / A-43 / A-45 / A-47 / A-49 / A-56）

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`，基线 exit 0。**唯一入口，禁止直接调 gradle**；用 `compileJava` 逐步快验 |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 主 toml | `run/config/farmstorm-server.toml`：**59 段**（`^\s*\[` 数）、含 `[farmland]`、`[pumpkin_turret]` 5 键（Phase 27 之后）。**F-192 之前不许删它**；F-192 落地后才允许删掉重新生成 |
| 起服 | 复制 `run/eula.txt` + `run/server.properties` 到 `run-p29`（`enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），`FS_RUN_LOCK=run-p29 cmd /c ""<repo>\.tools\gw.cmd" runServer -PfsRunDir=run-p29" > .work\p29-server.log 2>&1`，RCON 发 `stop` |
| 老档 fixture | `run/world/data/farmstorm_farmland.dat`（274 B）与 `farmstorm_outpost_core.dat`（201 B）已存在，可作 0.1.0 老档样本；另需**自造**一份「20 块老田 + 1 个核」的存档验硬顶 |
| 注册面 | 93（本 Phase 不许变；`CoreChunkTickets` 不进 registry） |

## 4. 调度者已定的口径

1. 文档与源码冲突 → **以源码为准**，登记偏差表；不许改 `docs/`。
2. 已裁决未回灌条目按 `.work/v011-verdicts.md` 执行并注明 A-xx。本 Phase 必读的九条：
   - **A-35**：核被毁时找不到接替者且该区块未加载 → **只摘绑定、世界不动**（不写 `setBlock`，也不为铲地加载区块）。
   - **A-36 → A-47(rev.2)**：老档补建**一律先把候选耕地按 `BlockPos#compareTo` 全序排序（y → z → x）**再逐条处理；「按 `byOwner`/`orphans` 插入顺序」**作废**。
   - **A-37**：验收断言**不得写「调用点数 = 1」**。
   - **A-39**：平手比较键 = `placedGameTime` 升序 → 维度 id → `BlockPos#compareTo`；**不得依赖任何 Map/列表的插入顺序**；比较路径不 `getChunk` / 不 `getBlockEntity`。
   - **A-43**：ticket 的 **tick 面是 1 个区块**，**加载面必然 3×3**；口径按 tick 面写，**禁止**把「只有 1 个区块被加载」写成断言。
   - **A-45**：踩踏面按「**有绑定即受保护**」；额度与浮窗仍按实时己方判定。
   - **A-49**：耕地跨维度同坐标撞车 —— **已知限制，本版不修**，只登记，不设通过项。
   - **A-56**：`F-192` 的依赖是 **F-191**（编译顺序）。
   - **A-42 / A-45 相关**：`OutpostCoreIndex.ownerNames` / `count` 是 `private`，需加只读访问器；**不复活 `isOwnerOrAlly`**。
3. **禁止任何 git 写操作**。只许 `git status` / `git diff` 取证；提交由调度者做。
4. 客户端 GUI 类验收标「未验证」+ 原因；能起无头服务端取得的照做。
5. 遇到做不下去的：停下，写清阻塞点与已做部分。

## 5. 红线与三个数

- 红线只剩「无 Mixin」。网络包限制已整体废除（本 Phase 不涉及），不许写成通过条件；`GameRules.register` 仍 0。
- **A-40 在本 Phase 关闭**：F-192 落地 + 删旧 toml 重新生成之后，`verify_project.py` 必须**首次**输出 `结论：FAIL 0 项`、退出码 0、config 段 166。**这是全项目第一条可以引用工具结论的判据**。
- 本 Phase 结束时：注册 **93**（不变）、server config **166 键 / 58 组**（`166 − 1 + 1`，组成变了）、语言键 **314 × 2**（`316 − 2`），中英键集相等。

## 6. 实现要点（逐条）

> 30 号 §2.2 给了绑定表的完整骨架（本体三份 / 两个 record / 落盘 / 兼容读 / `setDirty` 表 / 惰性清理），逐段照做。

### F-187 `TeamResolver` 的 `Owner × Owner` 重载
新重载 `isAlly(ServerLevel, Owner, Owner)`；`Player` 版退化成一行委托；`DeployedOwnership.isAlly` 不动；`untamedFallback` 语义逐条等价。

### F-188 `FarmlandLedger` v2
- 三份本体：`bindings`（要落盘）、派生的 `byCore`（**不落盘**）、`orphans`（要落盘）；两个 record（耕地→核、核→耕地）
- `dataVersion: 2`；v1 老档**全读成 `orphans`**
- `bind` / `unbind` / `migrate` / `bindingsOf` / `orphanCount` / `rebuildByCore`
- 惰性清理**必须同时剪 `bindings` 与 `byCore`**（否则幽灵耕地占住 16 格容量）
- `grep -c "setBlock" farmland/FarmlandLedger.java` = **0**

### F-189 `OutpostCoreIndex.Entry`
- `record Entry(dimension, pos, long gameTime)` 落盘；老档缺值取 `Long.MIN_VALUE` 并打计数
- 加只读访问器 `entriesOf` / `owners` / `count(server, uuid)`
- 比较路径**不** `getChunk` / **不** `getBlockEntity`

### F-190 启动补建 `backfill`
一次性启动闸 + 按 `BlockPos#compareTo` **单级全序**排序后逐条处理（A-47 rev.2）；谓词与迁移同一条；**整段没有 `setBlock`**；收尾一条 INFO 带 N / M / T（形如「耕地补建：绑定 N 块 / 保持未绑定 M 块（老档共 T 块，按 `BlockPos` 全序）」）。

### F-191 额度 / 已用改口径
`capacity = farmlandBonus × 己方核数`、`used` = 绑定表里己方名下的条数、`orphans` 不计入；删 `baseOverride` / `base()` / `setBase()` / `personalCapacity` 与 `sumOverMembers` 的 base 参数；指令侧临时改只读回显保住编译（F-197 才删指令）。

### F-192 删 `farmland` 整组
删字段、整组、getter 与语言键生成段；**删旧 `run/config/farmstorm-server.toml` 重新生成**后段行 **58**、键 **166**、`[pumpkin_turret]` 仍 5 键。这是 A-40 的收口点。

### F-193 `FarmlandGuard.check` 选核
新增 `record Decision(Verdict, @Nullable CoreKey)`；候选集**先过滤（影响范围覆盖 + 己方 + 有空位）再取最近**；三级键 `placedGameTime` → 维度 id → `BlockPos#compareTo`；`QUOTA_FULL` 语义变为「最近的己方核满了」。

### F-194 锄地成功时写入绑定
`event/FarmstormGameEvents.java` 锄地闸的 `ALLOW` 分支把 `ledger.add(...)` 换成 `ledger.bind(owner, coreKey, pos)`；同一 tick 内完成写绑定 + 记账 + 放耕地；被拒时**一条绑定都不产生**。

### F-195 耕地保护的离线判定与踩踏面
`protectsFromTrample` 按「有绑定即受保护」（A-45）；`inOwnRange` 读绑定表 + `isAlly(核主, player)`；`getPlayerList().getPlayers()` 在 `FarmlandGuard` 里 **0 命中**。

### F-196 核被毁：先迁移、三支降级
只处理 `bindingsOf(dead)`；三个出口：① 改绑到接替核；② `setBlock(DIRT)` + 摘绑定；③ **只摘绑定**（找不到接替者且区块未加载，A-35）；**先算完集合再动世界**；不为清算加载区块；不通知、不掉落、不补偿。

### F-197 删指令树
整文件删除 + `FarmstormGameEvents` 的 3 处注册删除 + 1 条语言键；不保留空壳，`/farmstorm` 根节点不注册；客户端 `hud` 子树（Phase 30 才有）不碰。

### F-198 运行期临时 ticket
新建 `outpost/CoreChunkTickets.java`：自有 `TicketType`（`distance = 1`）+ 三处幂等释放 + 起服按索引补票；**只用裸 `TicketType` + `addRegionTicket`**，`setChunkForced` / `forceChunk` **两者都否决**（A-38：它们都会写 `world/data/chunks.dat`）。

## 7. 验收标准（= 33 号 §4.4 的 9 条出口判据，逐条给读数）

1. **绑定的落盘形态**：锄 1 格 → `/save-all` → 读 `farmstorm_farmland.dat`：含 `dataVersion: 2`、`bindings` 里 **1** 条（键 `pos` / `owner` / `coreDimension` / `corePos`，`coreDimension = "minecraft:overworld"`）、**没有** `byCore` 键；再启动一次「耕地绑定指纹（1 条）」非空且相同。
2. **额度与第 17 格**：一个核名下连开到第 17 格被拒并提示「耕地额度已满 16/16」；该核名下仍只有 **16** 条；`farmlandBonus` 改 **2** 重启后第 **3** 格即被拒、队伍容量同步变 2；被拒时 `ItemDamage` 前后相同。
3. **三级键可复现**：两核相距 40 格 → 等距 → 离线把两个 `gameTime` 改成同值各锄一格；连跑两次启动。依次绑「较近」→「`gameTime` 更早」→「坐标字典序在前」；两次启动的「核心比较键指纹」「耕地绑定指纹」两行**完全相同**。
4. **保护面**：核主下线后由非同队玩家跳踩 → 仍是 `minecraft:farmland`；同位置无绑定对照 → 变泥土；僵尸踩有绑定的耕地 → 踩不坏；换队后再踩 → 踩踏面不变、额度读数按新队重算（两行并排取证）。
5. **拆核三支**：① A 名下 3 块 / B 覆盖其中 2 块，拆 A → 那 2 块改绑 B、第 3 块变 `minecraft:dirt`、B ≤ 16；② 无接替核且人在田边 → 变泥土且**没有掉落物**；③ 走远 200 格让区块卸载后 `/execute in` 拆 A → 仍是 `minecraft:farmland`、世界一个方块没动、日志出现「迁移失败且区块未加载：只摘绑定」。
6. **补建可复现**：0.1.0 老档连跑两次 → `N` / `M` **完全相同**；把 `.dat` 里 `orphans` 两条记录对调再跑 → 绑定坐标集合与 N/M 不变；20 田 1 核 → **N = 16、M = 4**；补不上的田逐块 `/execute if block … minecraft:farmland` 全成功、**0** 个变泥土。
7. **两处删除**：`[farmland]` 段不存在、段行（`^\s*\[`）**58**、键 **166**、`[pumpkin_turret]` 仍 5 键；`/farmstorm` 与 `/farmstorm farmlandlimit 5` 都失败；`grep -c "farmlandlimit"` 在两份 lang 与 `ModLangProvider` 里都是 **0**；`grep -rn "FarmstormCommands"` **0 命中**。
8. **A-40 在这里关闭**：删旧 toml 重新生成后跑工具 → `结论：FAIL 0 项`、退出码 **0**、§1 段 **166**。
9. **ticket 的 tick 面**：走远 200 格让本地卸载 → 核的粒子按 **40** 刻持续增长；拆核后再走远 → 该区块 F3 显示未加载；`Forced` 数组里**不含**该核的 `ChunkPos.toLong()`；重启后票重建；`grep -c "setChunkForced|forceChunk"` = **0**；三层区块状态**各截一次**，**不许**写「只有 1 个区块被加载」。
10. 语言键区间读数：两份各 **314** 键（`316 − 2`），键集相等。
11. `.tools\gw.cmd build` 退出码 **0**。
12. 六处 `grep -c`：`setBlock`（FarmlandLedger）= 0；`getPlayerList().getPlayers()`（FarmlandGuard）= 0；`baseOverride|personalCapacity|sumOverMembers` = 0；`farmlandlimit` = 0；`FarmstormCommands` = 0；`setChunkForced|forceChunk` = 0。

## 8. 必须回报的证据

1. `git diff --stat` 原文；12 条各自的文件（含被删的 `command/FarmstormCommands.java`）与新增类 `outpost/CoreChunkTickets.java`；三处注册删除的行号。
2. `.tools\gw.cmd build` 退出码。
3. `verify_project.py` 的**完整输出**与退出码（本节第一次允许引用 `FAIL 0`，必须给原文）。
4. `bindings` / `orphans` 的 NBT 键与条数；两次启动的 N/M 与两行指纹；第 17 格与 `farmlandBonus = 2` 两次拒绝的文案原文；拆核三支的方块读数与日志行；补建日志**原文**。
5. 段行 58 / 键 166；ticket 的粒子计数与三层区块状态。
6. 六处 `grep -c` 的读数。
7. **文档-源码偏差表**。
8. **未验证清单**（每条一行 + 原因）。
9. **未关闭的问题**（含 A-49 只登记一行，不设通过项）。

## 9. 证据落盘

完整输出写进 `.work/TASK-V011-P29-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 10. 完成定义

§7 的 12 条全部有读数或有「未验证」+ 原因；`run/` 的改动必须是**计划内的**（只有 F-192 删旧 toml 那一处）；回报含 9 项。
