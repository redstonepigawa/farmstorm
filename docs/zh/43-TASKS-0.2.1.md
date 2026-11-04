# 43 号 v0.2.1 任务拆分

> 依据：40 号（14 决策 + 17 裁定）/ 41 号（5 需求 + 36 边界）/ 42 号（97 注册 / 31 动作面 / 176 config / 31 验收）、任务 F-247 起、Phase 40 起、M36 起、裁决 A-70 起
>
> 本文把 40 / 41 / 42 三份规格拆成 **20 个「一次可独立提交、可独立验证」的任务**，编号 **F-247 ~ F-266**，Phase **40 ~ 45** 与里程碑 **M36 ~ M41** 一一对应，裁决 **A-70 ~ A-81**（12 条，均为本文写作期新立）。
> 覆盖：42 号 §3 的 **31 行动作面**（表 A，31/31）、注册面 **97（+0）+ 口径外第 13 类 1 行**（表 B）、config **176 键 / 60 组**（表 C，+1）、语言键 **338 × 2**（表 D，+1）、风险 **11 条**（表 E：RT-51 ~ RT-60 + RT-60b）、待验证 API **10 条**（表 F）；未覆盖 0。
> 本文只读上游：**不改** `docs/zh/40` ~ `docs/zh/42`、不改 `docs/en`、不改 `src/`、不改 `tools/`、不改 `.work/`；只新建本文件。

---

## 0. 阅读约定

### 0.1 任务粒度（五条）+ 三条读数纪律

一个任务 = 一次可独立提交、可独立验证的改动。五条判据沿用 37 号 §0.1（源头是 31 号 / 25 号），本版不改写、只重述：

1. **一个任务 = 一次可独立提交、可独立验证的改动。**
2. **提交后 `.tools\gw.cmd build` 退出码 0**，且不依赖同批次里尚未提交的任务。
3. **每个任务自带至少一条可观察断言**，断言里必须出现具体数值、键名、路径或日志片段。
4. **回滚该提交不会让其它已完成任务失效**（只影响依赖它的后续任务，依赖写在 §1 的「依赖」列与任务标题行）。
5. **不跨 Phase 边界**：一个任务的交付物全部落在它所属 Phase 的名称范围内。

三条**本版特有**的读数纪律（写在这里，任务里不再重复）：

- **行号一律 UTF-8 口径**：本机默认 `Get-Content` 会把无 BOM 的 UTF-8 中文读成 GBK 并**吞掉换行**（同一份 `OutpostCoreBlockEntity.java` 从 782 行读成 668 行、`FarmstormConfig.java` 从 1131 行读成 1095 行），行号随之全错（42 号 §0.2 第 4 条 / §9 第 12 条）。本文所有行号按 `[IO.File]::ReadAllLines($p, UTF8)` 复核过。
- **数 toml 一律指向 `run/config/farmstorm-server.toml`**：仓库里还有几十份 `run-*/config/` 旧副本（`run-verify` 等是 12,238 字节的 121 键旧文件），拿错一份就会得出「只有 121 键」的结论（A-70 之外的坑，42 号 §9 第 2 条即踩过）。
- **不写「手感」「正常」**：判据只能是计数（文件数 / 键数 / 命中数）、日志行、`/data get` 读数、键集比对四类。

### 0.2 复杂度与人日换算（S/M/L → 0.5/1.5/3.0）

换算口径与 37 号 §0.2 完全一致，本版不调整：

| 复杂度 | 含义 | 折算人日 |
|---|---|---|
| S | 单文件或纯数据；≤ 1 个类，或 ≤ 3 个文件里的字面量替换 | **0.5** |
| M | 2 ~ 4 个文件，跨 1 ~ 2 个包，或新增 1 个完整行为实现 | **1.5** |
| L | ≥ 5 个文件或跨 3 个包，含运行期状态机 / 序列化 / 容器契约 / 渲染管线 / 跨类接口改造 | **3.0** |

本版 20 个任务按此分布：**S 7 / M 10 / L 3**，折算 `7 × 0.5 + 10 × 1.5 + 3 × 3.0 = 3.5 + 15.0 + 9.0 = **27.5 人日**`（不含缓冲）。

**计时的三条口径**：凡「全仓 0 先例」（F-248 / F-257）按比「加一个类」高一档计；凡「契约一次性变更」（F-259 的 7 处调用里 6 处必须零改动）按 M 计；凡验收任务（F-264 / F-265）不按「跑一遍」估时，按 L 计。

### 0.3 **中间态窗口**（本版特有的第 6 条粒度规则）

**事实**：本版两个数值面各只有 **+1**，且增量都落在**同一个提交**（F-258：config 键 `trap.damageMultiplier` 与它的中英翻译一次写完）。但「键加上了」不等于「总数可以断言」——四处总数文本要等到各自的任务才同步：

| 总数文本 | 现值 | 由谁同步 | 证据 |
|---|---|---|---|
| `FarmstormConfig.java` 行首 `.define*` 的**计数** | 175 | F-262 | 42 号 §4.2 的三条独立校验 |
| `run/config/farmstorm-server.toml` 的键 / 段 | 175 / 60 | F-262（先跑一次 `runServer` 重生成） | 实测 801 行 / 60 段 / 175 键 |
| `ModLangProvider.java:13-16` 的类注释（`**337**` / `**178** config`） | 337 / 178 | F-263 | 42 号 §4.3 的两条纪律 |
| `verify_project.py` 的期望数（175）与并集 | 175 | F-261 | 42 号 §4.1.1 的四条契约 |

**窗口 = F-258（开）→ F-263（关）**，其中 config 侧关于 **F-262**、工具期望面关于 **F-261**。四段状态：

| 段 | 任务边界 | config 键 | 语言键 | 段内能断言什么 |
|---|---|---|---|---|
| 窗口前 | F-247 ~ F-257 | **175** | **337** | 可断言总数（175 / 337），因为此间没有任何键增删 |
| 窗口内 A | F-258 ~ F-260 | 实际 **176** | 实际 **338** | **只能**写「本条键存在」：`damageMultiplier` 在 `[trap]` 段出现 1 次、`farmstorm.configuration.trap.damageMultiplier` 在中英各 1 次；以及 `[trap]` **段内 4 键**、全文 **175 或 176（区间读数，旧 toml 未重生成时是 175）** |
| 窗口内 B | F-261 ~ F-262 | 实际 **176** | 实际 **338** | 工具段可写 176（F-261 关的是工具期望面）；语言键总数**仍不许**写 |
| 关窗 | F-263 之后 | **176** | **338** | F-262 写 176 / 60、F-263 写 338 × 2；此后任何任务再增删键都是超范围 |

**三条窗口纪律**（F-258 ~ F-262 的每个任务都适用；F-263 是关窗任务，第 1 条对它不适用）：

1. **不许**把「176 / 60」或「338 × 2」写成验收断言；窗口内只写**本条键的存在性**与**段内 / 区间读数**（见上表第三行）。例外只有两处：F-261 可写工具打印的 **176**（它关的是工具期望面），F-262 可写 **176 / 60**（它关的是源码与 toml）；**338** 只在 F-263 出现一次。
2. **`ModLangProvider.java:13-16` 的类注释一律不动**，直到 F-263 一次改齐（`337 → 338`、`178 → 179`）。中途改注释意味着每个任务都要重算一遍总数，且必然有一个算错。
3. **`run/config/farmstorm-server.toml` 的读数必须在 `runServer` 重生成之后取**（42 号 §4.2 的校验②），窗口内允许「175 或 176」的区间写法，但不允许把未重生成的文件当真值；**语言键没有自动闸**（`verify_project.py` 第 3 节只比 zh / en 键集相等，不校验 338 这个数，42 号 §9 第 4 条）⇒ 关窗只能靠 F-263 的人工 `ConvertFrom-Json` 计数。

**为什么窗口同开于 F-258、分三处关**：一个 config 键与它的翻译是**一次原子改动**（只加配置不加翻译，玩家看到的是一串裸键），所以同开；而「源码计数 / 工具期望 / 文档注释」三种总数文本的**观察面互不相同**（`.define*` 行、python 并集、java 注释），同步动作必须各自落在能验证它的任务上，否则关窗任务会变成一个「改三个数、没有一个数有读数」的假收口。

### 0.4 一个任务 = 一个提交

1. **一个任务一个提交**，提交信息以任务号开头（`F-248: …`）；提交内容与任务的「涉及文件」逐条对齐，多一个文件就是超范围。
2. **不夹带既存瑕疵的顺手修**：42 号 §9 登记的 16 条差异（`ModLangProvider.java:237` 的「139 个配置项」注释、`FarmstormBlocks.java:43` 的「3 个方块」注释、`.scratch/` 里的 `neoforge.mixins.json`、`removeCultivator` 的 0 调用点等）本版**一条都不改**；它们出现在「易漏点」里是为了「别顺手改」，不是待办。
3. **回滚粒度 = 提交粒度**：每个任务的验收断言必须在**只回滚这一个提交**之后仍然描述得通（不依赖同批次的其它提交）。
4. **上游一字不改**：写作期发现的 40 / 41 / 42 差异全部登记在 §5 的 A-70 ~ A-81，**只登记、不修改**上游文件；任务里引用上游时写全节号（`42 号 §2.3.3`），不转述细节。

---

## 1. 任务总表

列：任务号 / 标题 / Phase / 复杂度 / 人日 / 依赖 / 交付物 / 对应决策 / 覆盖的 42 号 §3 动作面行号（`表外` = 不在 42 号 §3 的 31 行里，理由见 §5）。

| 任务号 | 标题 | Phase | 复杂度 | 人日 | 依赖 | 交付物 | 对应决策 | 动作面行号 |
|---|---|---|---|---|---|---|---|---|
| F-247 | 版本号 0.2.1 与两支 `.cmd` 的 6 处版本串 | 40 | S | 0.5 | — | `gradle.properties`、两支 `.cmd` | D-173 | #1、#2、#3、#4、#5 |
| F-248 | `util/AreaBlockEntities#collect`（区域枚举方块实体，0 先例） | 41 | M | 1.5 | — | 新 `util/AreaBlockEntities.java` | D-177、RT-52 | #10 |
| F-249 | 补绑的相位门与写入边界：`rebindDue` + `rebindNearby` | 41 | M | 1.5 | F-248 | `OutpostCoreBlockEntity` 常量区与方法区 | D-176、D-177、裁定 #13、#14 | #7、#8、#9、#30 |
| F-250 | `serverTick` 接线 + `CultivatorBlock:73-84` 注释同改 | 41 | M | 1.5 | F-249 | `OutpostCoreBlockEntity:507-525`、`CultivatorBlock:73-84` | D-176、D-177 | #6、#11 |
| F-251 | `isMountMode` 可见性（`private` → `public`） | 42 | S | 0.5 | — | `QbounceMountController:199` | D-181 | #17 |
| F-252 | AI 原值附件载体：`Suspend` + `farmstorm:ai_suspend`（第 13 类） | 42 | M | 1.5 | — | 新 `registry/FarmstormAttachments.java`、`Farmstorm.java` +1 行 | D-179、裁定 #15、#17 | #19、#29 |
| F-253 | `MobAiSuspension`：进记原值 / 出按原值恢复 / 五条清理路径 | 42 | L | 3.0 | F-251、F-252 | `combat/MobAiSuspension.java` | D-179、裁定 #15、#16 | #18、#31 |
| F-254 | 震动豆：范围内所有非玩家生物删 AI 80 刻 | 42 | M | 1.5 | F-253 | `VibrationBeanTrap:74-105` | D-180、裁定 #4、#5 | #14、#15 |
| F-255 | Q弹：效果存续期删 AI、mount 跳过 | 42 | M | 1.5 | F-251、F-253 | `effect/QbounceEffect.java` | D-181、裁定 #6、#7 | #16 |
| F-256 | 核心储物 tooltip：保证默认链路 + 3 条验收 | 42 | M | 1.5 | — | `OutpostCoreScreen`（预期零改动）+ 验收记录 | D-178、RT-59 | #12、#13 |
| F-257 | 敌对谓词 `util/Hostility#isHostile`（0 先例） | 43 | S | 0.5 | — | 新 `util/Hostility.java` | D-182、裁定 #9、RT-57 | #20 |
| F-258 | `trap.damageMultiplier` 键与翻译（**开窗**） | 43 | S | 0.5 | — | `FarmstormConfig`、`ModLangProvider` | D-183、D-185 | #24、#25 |
| F-259 | `FarmstormExplosion` 第 9 参 `trapSourced` + 乘点① | 43 | M | 1.5 | F-257、F-258 | `FarmstormExplosion:69-78,178-179`、`BlastCarrotTrap:84-86` | D-184、RT-58 | #21、#22 |
| F-260 | `ZangiefCaneTrap#monsterBranch` 乘点② + 日志打乘后值 | 43 | S | 0.5 | F-257、F-258 | `ZangiefCaneTrap:111-118`、`:126-128` | D-184 | #23 |
| F-261 | `verify_project.py` 新解析器 `expected_config_keys_v021` | 44 | M | 1.5 | F-258 | `tools/verify_project.py` | D-185、RT-60b | #26 |
| F-262 | config 总账 176 键 / 60 组（**关窗**） | 44 | S | 0.5 | F-258、F-261 | 三处读数记录 | D-185、RT-60 | 表外（42 号 §4.2） |
| F-263 | 语言键总账 338 × 2（**关窗**） | 44 | S | 0.5 | F-258 | 两份 lang + `ModLangProvider` 注释 | D-185 | 表外（42 号 §4.3） |
| F-264 | 42 号 §7 的 31 条验收全量执行 | 45 | L | 3.0 | F-247 ~ F-263 | `.work/v021-verify/acceptance.md` | 42 号 §7 全 31 条 | —（验收面） |
| F-265 | 41 号 §9 的 36 条边界复跑 | 45 | L | 3.0 | F-264 | `.work/v021-verify/boundaries.md` | 41 号 §9 全 36 条 | —（边界面） |
| F-266 | 红线、注册面与上游一致性签字 | 45 | M | 1.5 | F-261、F-262、F-263、F-264、F-265 | `.work/v021-verify/signoff.md` | D-175、D-186、A-73 | #27、#28（负向核对） |

任务数：`266 − 247 + 1 = 20`，连续无跳号；Phase **6** 个；里程碑 **6** 个。

---

## 2. Phase 40 ~ 45 的任务详情

按 Phase 分组。每个任务固定六节：**目标 / 涉及文件（带行号）/ 具体改动 / 实现要点 / 易漏点 / 验收断言**；依赖写在该任务的标题行里（`依赖 —` 表示无前驱）。

### Phase 40 · 版本与基线（F-247）

阶段目标：把版本号与两支构建脚本的产物面钉死——它是全部 jar 名与「检查环境」判据的唯一来源。出口判据：`farmstorm-0.2.1+mc1.21.1.jar` 产出、`0.2.0` 在 6 处字面量里归 0。

#### F-247 · 版本号 0.2.1 与两支 `.cmd` 的 6 处版本串（S · 0.5 人日｜依赖 —）

- **目标**：把 `mod_version` 改成 **0.2.1**（D-173），并让两支 `.cmd` 里写死的 jar 名与抬头行跟着改，使「一键构建」的复制步骤与「检查环境」的存在性检查仍指向真实产物（42 号 §3 #1 ~ #5；A-70）。
- **涉及文件**：`gradle.properties:11`；`一键构建.cmd:5`（抬头）、`:11`、`:12`（jar / sources-jar）；`检查环境.cmd:5`（抬头）、`:14`（jar）。
- **具体改动**：6 处字面量 `0.2.0` → `0.2.1`（`gradle.properties` 1 处 + 两支 `.cmd` 5 处）。`minecraft_version` / `neo_version` / toolchain 三行一字不动（42 号 §1.1）。
- **实现要点**：版本号只有 `gradle.properties:11` 一处来源，其余文档只引用、不各自声明；jar 名由 `mod_version` 拼出，改完必须**真跑一次构建**确认新名（42 号 §3 #1~#5；37 号 F-213 的同一纪律）。
- **易漏点**：① `一键构建.cmd:12` 的 `-sources.jar` 最容易只改主 jar；② 两支脚本的**抬头行**（各 `:5`）写死 `Farmstorm v0.2.0`，不改就是「产物对了、横幅还写旧版」；③ `检查环境.cmd:14` 的存在性检查与提示文案绑在一起，漏改会在环境检查里报「jar: 缺失」（实为改名后找不到）。
- **验收断言**：
  1. `.tools\gw.cmd build` 退出码 0；`build/libs/` 下有 `farmstorm-0.2.1+mc1.21.1.jar` 与 `farmstorm-0.2.1+mc1.21.1-sources.jar`，`farmstorm-0.2.0*` 残留 **0** 个。
  2. `0.2.0` 在 `gradle.properties` + 两支 `.cmd` 里的命中数 = **0**；`0.2.1` 的命中数 = **1（gradle）+ 3（一键构建）+ 2（检查环境）= 6**。
  3. `jar tf build/libs/farmstorm-0.2.1+mc1.21.1.jar` 含 `META-INF/neoforge.mods.toml`，其 `version` 解析为 `0.2.1`。

### Phase 41 · R1 周期补绑（F-248 ~ F-250）

阶段目标：让核在运行期每 100 刻把 3×3 已加载区块里「没人要」的培养器补进自己的 16 个名额，并且不被既有的粒子相位门筛掉。出口判据：`boundaryParticleIntervalTicks` 取 40 与 1200 两个值，补绑都在 **100 刻内**发生；满 16 即停；无主核不补。

#### F-248 · `util/AreaBlockEntities#collect`（区域枚举方块实体，0 先例）（M · 1.5 人日｜依赖 —）

- **目标**：新增一个只在**已加载**区块里按区域枚举方块实体的工具，作为补绑唯一的扫描面（42 号 §2.1.2 的「本文确定（3）」；42 号 §3 #10）。全仓 0 先例：`ChunkMap` 0 处、`getBlockEntities()` 仅 `DeployedCropBlockEntity:230` 一处（单区块、只数自己）。
- **涉及文件**：新建 `util/AreaBlockEntities.java`；只读参照 `blockentity/OutpostCoreBlockEntity.java:75`（`RANGE_CHUNKS = 1`）、`:296-300`（`getChunkNow` 的现成形态）、`:384-387`、`blockentity/DeployedCropBlockEntity.java:227-236`（唯一的单区块先例，`:228` 用 `getChunk` **会加载**、只能抄 `:230-234` 的遍历）。
- **具体改动**：唯一方法 `collect(ServerLevel level, BlockPos center, int chunkRadius, Class<T> type) → List<T>`，四条实现纪律逐条落地（只走 `ServerChunkCache#getChunkNow`、半径复用 `RANGE_CHUNKS` 不新设第二个常量、返回**新 `ArrayList`**、按 `Class<T>` 过滤）——细节见 42 号 §2.1.2。
- **实现要点**：**两条禁令**——禁止 `Level#getChunk` / `getChunkAt` / `Level#getBlockEntity`（会触发区块加载，`OutpostCoreBlockEntity:264-267` 的注释已写明）；禁止把 `LevelChunk#getBlockEntities()` 的**活映射**（`LevelChunk.java:528-530`）直接交出去遍历（遍历中被增删会 `ConcurrentModificationException`）。
- **易漏点**：① 把「未加载」当成「该区块没有培养器」去记日志 / 记状态（42 号 §2.1.2 的 📌：核的自有票把 3×3 常态拉成「9 块全在内存」，跳过是**防御分支**，不是常态）；② 为「省一次过滤」把 `Class<T>` 换成 `BlockEntityType`；③ 顺手给工具加排序或缓存（排序是 F-249 的事；缓存会让「写入即占位」失效）。
- **验收断言**：
  1. `gw.cmd build` 退出码 0；`src/main/java` 的 Java 文件数 145 → **146**（UTF-8 读法）。
  2. 该文件里 `getChunkNow` 的调用点 **1** 处；`getChunk(` / `getChunkAt(` / `getBlockEntity(` 命中 **0**。
  3. 该文件里 `new ArrayList` 命中 ≥ 1，且 `getBlockEntities()` 的结果在遍历前已被复制（两阶段，42 号 §2.1.2 纪律 3）。
  4. 按 `Class<T>` 过滤（`type.isInstance`）命中 ≥ 1，`BlockEntityType` 命中 **0**。
  5. 运行时读数**不在本任务**：`collect` 的第一个调用者是 F-249，42 号 §7 #6 ~ #10 的运行时读数归 F-250（42 号 §8 ① 已把「活映射语义」的验证指给补绑任务）。本任务的可独立执行判据是「能编译 + 上列四条结构读数」。`run-*/config/` 的旧 toml 与本任务无关。

#### F-249 · 补绑的相位门与写入边界：`rebindDue` + `rebindNearby`（M · 1.5 人日｜依赖 F-248）

- **目标**：把「每 100 刻、按坐标 hash 错峰到 0~99 刻」的相位门与「只补未绑定、不夺已绑、<16 才补、满即停」的写入边界落成两个方法（42 号 §2.1.1、§2.1.3；42 号 §3 #7 ~ #9、#30）。本任务**不接线**，两个方法暂时没有调用者。
- **涉及文件**：`blockentity/OutpostCoreBlockEntity.java`（常量区紧邻 `:91` 的 `TICK_LOG_ROUNDS` 新增 `REBIND_INTERVAL_TICKS = 100`；方法区新增 `rebindDue(BlockPos, long)` 与 `rebindNearby(ServerLevel)`）。
- **具体改动**：相位公式的模数是**固定 100**（与粒子门的 `interval` 模数分开，42 号 §2.1.1 的「本文确定（1）续」）；常量**不是** config 键；写入顺序写死「先 `addCultivator` 成功、再 `setCore`」；第一行无主核闸（裁定 #13）；候选排序 `distSqr` 升序 → `x` → `y` → `z`（裁定 #14，与 `selectCore:310-315` 同构）；容量读 `cultivatorCount()` / `isCultivatorListFull()`。
- **实现要点**：`rebindNearby` 方法体内**不得**出现 `pruneCultivators` / `removeCultivator` / `cultivators()`（42 号 §2.1.3 的第 1、3 条：前者会 `setChanged` 并与补绑交错，后者是「先绑再摘」，会造出第二个主）；不额外调广播（`addCultivator` 里已有 `setChanged` + `syncBindings` + `requestAll`，42 号 §2.1.3 第 4 条）。
- **易漏点**：① 把 `REBIND_INTERVAL_TICKS` 写成 config 键（175 会变 **177**，与 D-185 的 176 直接冲突）；② 排序漏掉 `y` 一级（同一 x/z、不同 y 的两个培养器顺序不稳，同一存档两次运行结果不同）；③ 忘掉 `ownerUuid == null` 的第一行闸（无主核会把周围培养器全收了，裁定 #13）；④ 顺手调 `removeCultivator` 做「两侧不一致」的兜底（该方法的调用点必须仍是 0）。
- **验收断言**：
  1. `REBIND_INTERVAL_TICKS` 的声明值是 **100**；`FarmstormConfig` 的 `b.push(` 仍 **60**、`b.pop()` 仍 **60**、行首 `.define*` 仍 **175**（本任务一个 config 键都不加）。
  2. `rebindNearby` 方法体内：`pruneCultivators(` 命中 **0**、`removeCultivator(` 命中 **0**、`cultivators()` 命中 **0**；`addCultivator(` 与 `setCore(` 各 ≥ 1，且前者在文本上**先**出现。
  3. 排序键恰好四级：`comparingDouble` **1** 次（`distSqr`）+ `thenComparingInt` **3** 次（`x` → `y` → `z`）。
  4. 全仓 `pruneCultivators(` 的调用点仍是 **1**（`OutpostCoreBlockEntity:329`，在 `cultivators()` 里）；`removeCultivator(` 的调用点仍是 **0**（只有 `:366` 的声明与 `CultivatorLinkBroadcaster:35` 的 javadoc 提及）。
  5. `gw.cmd build` 退出码 0。

#### F-250 · `serverTick` 接线 + `CultivatorBlock:73-84` 注释同改（M · 1.5 人日｜依赖 F-249）

- **目标**：把补绑门接进 `serverTick`（独立 `if`、在粒子门**之前**、**不 return**），并在**同一个提交**里改掉 `CultivatorBlock:73-84` 那段与本版正好相反的类注释，另补一条补绑 INFO 行作为观测面（42 号 §2.1.1、§3 #6、#11；BRIEF §3.4 第 3、11 项；A-71）。
- **涉及文件**：`blockentity/OutpostCoreBlockEntity.java:507-525`（`:515` 的 `now` 上移一行 + 一个独立门 + 一行调用 + 一条 INFO）；`block/CultivatorBlock.java:73-84`（类注释块）。
- **具体改动**：`serverTick` 从三件事变四件事（42 号 §2.1.1 的改造后形态），既有 `:508-510`、`:511-513`、`:516-518`、`:519` 与 `:521-524` 一字不动；`CultivatorBlock:81-83` 的「**不自动认领** / 事后在旁边补一个核也**不会绑上**」两句改为与周期补绑一致的表述（运行期每 100 刻补绑未绑定的，D-176），`:74-79` 的三判据说明保留。
- **实现要点**：**唯一的结构性改动是 `:515` 的 `now` 上移一行**（补绑门的必要条件，不是顺手优化；`emitBoundaryRing` 与日志段仍用同一个 `now`）；两门互不阻塞、无 `return` 交叠；`CultivatorBlock` 只改注释，`setPlacedBy`（`:85-102`，含 `:99` 的 `addCultivator` 与 `:100` 的 `setCore`）一行不改。
- **易漏点**：① 把补绑门接在 `:519` 之后（实际周期被筛成 `LCM(40, 100) = 200` 刻，且随 `boundaryParticleIntervalTicks` 的 20 ~ 1200 漂移，42 号 §2.1.1）；② 借用粒子门的 `interval <= 0` 守卫（`floorMod(x, 100)` 恒不抛，两套守卫不能互借）；③ 注释与代码分两次提交（「注释说不行、代码照做」的窗口哪怕一天也不该有，A-71）；④ 只改 `:81-83` 不把 `*/` 与 `:84` 的边界对齐，留下半句旧注释。
- **验收断言**：
  1. `boundaryParticleIntervalTicks = 40`（默认）+ 核绑 0 个 + 3×3 内 1 个未绑定培养器 ⇒ **100 刻内**该坐标出现在核的 `Cultivators` 里（`/data get block <core> Cultivators`）；同时日志出现一条补绑 INFO（核坐标 + 本轮新增条数 + 累计/上限）（42 号 §7 #6）。
  2. **同一场景把 `boundaryParticleIntervalTicks` 改成 1200** ⇒ 仍然 **100 刻内**补上（42 号 §7 #7）。这是「补绑没被粒子门筛掉」的判决性读数：复用粒子门的实现在这里要等 1200 刻。
  3. 满则停：3×3 内 20 个未绑定培养器、`cultivatorCapacity = 16` ⇒ `Cultivators` 恰 **16** 条，再等 300 刻仍是 16，已有 16 条的坐标与顺序逐字不变（42 号 §7 #10）。
  4. 无主核：`/setblock` 放一个核（`ownerUuid` 为空）+ 3×3 内 1 个未绑定培养器 ⇒ 等 **300 刻**，该核 `Cultivators` 仍是**空**（42 号 §7 #30）。
  5. 相位错开：同 3×3 里放 **2** 个核、各配 1 个未绑定培养器 ⇒ 两条补绑 INFO 的 `gameTime` 不相等（42 号 §7 #8）。
  6. `CultivatorBlock.java` 里 `不自动认领` 与 `不会绑上` 命中 **0**；`setPlacedBy` 的 `:99` / `:100` 两行逐字未变；该类总行数仍 **162**。

### Phase 42 · R2 tooltip 与 R3 / R4 的 AI 干预（F-251 ~ F-256）

阶段目标：给「删 AI」造出可落盘、可恢复、能覆盖五条清理路径的机制，并把它接到震动豆与 Q弹 两处；同时把核心储物 tooltip 从「本来就有」变成「有验收」。出口判据：震动豆 80 刻后 `NoAI` 回原值、Q弹 三条移除路径 1 刻内恢复、mount 态全程不删、悬停 54 格可见 tooltip。

#### F-251 · `isMountMode` 可见性（`private` → `public`）（S · 0.5 人日｜依赖 —）

- **目标**：给 AI 干预提供 mount 状态的**唯一只读入口**（42 号 §2.3.5 的「本文确定（7）」；42 号 §3 #17；BRIEF §3.4 第 12 项）。
- **涉及文件**：`behavior/QbounceMountController.java:199`（只改 1 个修饰符，方法体 `:200` 一行不动）。
- **具体改动**：`private static boolean isMountMode()` → `public static boolean isMountMode()`；`POSE_MODE_MOUNT` 仍是唯一字面量（`:50`）、`:200` 仍是唯一读取点。
- **实现要点**：**不新增第二个方法**（同一件事不许有两份实现）；`MobAiSuspension` 与 `QbounceEffect` 都只调它，**不得**自己解析 `qbounce.poseMode`（40 号术语表「mount 的真相只有一处」）；不碰 `ACTIVE`、不 discard 坐骑、不改挂载逻辑（42 号 §2.3.5）。
- **易漏点**：① 顺手加 `isMountModeForEntity(...)` 之类的包装（等于第二个真相）；② 把 `POSE_MODE_MOUNT` 的比较搬进调用方；③ 改 `:173-196` 的 tick 顺序（本任务只改可见性）。
- **验收断言**：
  1. `QbounceMountController.java` 的总行数仍 **376**（diff 只有 1 个修饰符）。
  2. 全仓 `isMountMode` 的**声明**恰 **1** 处，修饰符为 `public static`；方法体仍是 `POSE_MODE_MOUNT.equalsIgnoreCase(...)` 一行。
  3. 全仓 `qbouncePoseMode` 的读取点仍只有 `:200` **1** 处（新调用方只能调方法）。
  4. `gw.cmd build` 退出码 0。

#### F-252 · AI 原值附件载体：`Suspend` + `farmstorm:ai_suspend`（第 13 类）（M · 1.5 人日｜依赖 —）

- **目标**：为「记原值」建落盘载体，并把**口径外的第 13 类**显式登记出来（42 号 §2.3.1 的「本文确定（4）（5）」与 §1.2.1；42 号 §3 #19、#29；裁定 #15 / #17；A-73）。
- **涉及文件**：新建 `combat/MobAiSuspension.java`（本任务只放 `Suspend` record + `NONE` 哨兵 + 两个来源位 + Codec）与 `registry/FarmstormAttachments.java`；`Farmstorm.java:45-58`（构造器里 **11** 个 `.register(modBus)` 的区域，加 1 行）。
- **具体改动**：`Suspend(boolean originalNoAi, int sources)` 两个字段、`sources == 0` 的 `NONE` 不落盘、**不存任何时间戳**（裁定 #16）；`AttachmentType` 的注册名 `ai_suspend`（全名 `farmstorm:ai_suspend`）、`.serialize(Codec, s -> s.sources() != 0)`；不用 `.sync(...)`、不调 `copyOnDeath()`（42 号 §1.2.1 的两条「不会」）；`Farmstorm.java` 加一行 `FarmstormAttachments.REGISTER.register(modBus)`。
- **实现要点**：注册面 **97 不变**——`ATTACHMENT_TYPES` 是 NeoForge 自己的注册表，不在本项目 12 类口径内（裁定 #17）；`Farmstorm.java:167-172` 的 12 类断言**一字不动**（42 号 §3 #29）；附件**不是**数据组件（D-175 否决的是 `data_components`，加一个就 97 → 98）。
- **易漏点**：① 用 `DataComponentType` 承载（踩 D-175）；② 往 record 里加 `deadline` / `expireAt` 字段（裁定 #16 否决的「记时点」形态）；③ 忘了在 `Farmstorm.java` 挂 register（附件类型未注册 ⇒ 载入侧读不到，42 号 §8 ⑧）；④ 顺手加 `.sync(...)`（会让附件出现在客户端，本版不需要）。
- **验收断言**：
  1. `registry/FarmstormAttachments.java` 恰 **1** 个 `AttachmentType` 注册，id 字面量为 `ai_suspend`，命名空间 `farmstorm`。
  2. `Farmstorm.java` 构造器多 **1** 行 `.register(modBus)`（**11 → 12** 个 register 调用；`damage_type` 是数据驱动、本来就没有 register，见 A-78）；`:169-172` 的 12 类断言逐字未变。
  3. 起服日志仍是 `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`（42 号 §7 #1、#29）。
  4. `Suspend` 的字段恰 **2** 个；该文件 `deadline` / `expire` / `timestamp` 命中 **0**。
  5. **承认覆盖缺口**：`verify_project.py` 的 `EXPECTED` 12 组 id 里没有 `ai_suspend`、`EXPECTED_COUNTS` 相加仍是 97 ⇒ 工具**既不核它的存在、也不核它的计数**（42 号 §1.2.1 第 5 行）。这条缺口由 42 号 §1.2.1 + 本任务 + F-266 签字三处人工登记兜住（A-73）。

#### F-253 · `MobAiSuspension`：进记原值 / 出按原值恢复 / 五条清理路径（L · 3.0 人日｜依赖 F-251、F-252）

- **目标**：把「删 AI」做成有机理的：进入时**先读原值再** `setNoAi(true)`，退出时按原值恢复并**立刻删附件**；恢复判据是「效果是否还在身上」+ 震动豆的内存到期刻（42 号 §2.3.2 ~ §2.3.4；42 号 §3 #18、#31）。
- **涉及文件**：`combat/MobAiSuspension.java`（`suspend` / `release` / `adopt` / `sweep` + 两个内存容器）；`Farmstorm.java:74-77`（game 总线区挂 `ServerTickEvent.Post` 与 `EntityJoinLevelEvent`）。
- **具体改动**：三步顺序写死（读原值 → 写附件 → `setNoAi(true)`）；`release` 是**唯一**写出口、末尾必 `removeData`；`sources` 位掩码（同一来源重复进入不翻倍、`left != 0` 不恢复）；`VB_DEADLINE` 只存内存（裁定 #16：窗口不跨档）；`sweep` 每刻跑、遍历副本；对账挂 `ServerTickEvent.Post` + `EventPriority.HIGHEST`（42 号 §2.3.5 的「本文确定（11）」，必须先于 mount 挂载的 NORMAL）；载入结算挂 `EntityJoinLevelEvent`（42 号 §8 ⑥ 未核 → 退路见 §6 表 F）。
- **实现要点**：**只对 `Mob`**（玩家不是 `Mob`，谓词形态同 `WildHunterAura:88`）；「观测不到（区块卸载）」只丢内存两件、**保留附件**（42 号 §2.3.4：删了就丢原值）；死亡路径**不挂** `LivingDeathEvent`、三条效果移除路径**一个事件都不挂**（对账读 `hasEffect` 是权威，少一处「事件没送达」的失效面）。
- **易漏点**：① 先 `setNoAi` 后写附件（崩在中间就是「没 AI、没原值」，RT-54 的第一个入口）；② 把「观测不到」当成实体已死而删附件（丢原值，RT-54 的第二个入口）；③ 给 Q弹 也记一个到期刻（裁定 #16 反对的「记时点」形态）；④ 用 `hasData` 判「是否挂起」（默认值被取用后 `hasData` 也可能为真，判据只认 `sources != 0`）；⑤ 观测不到时「多少刻后放弃」的上限常量（42 号 §2.3.4 明确**不需要**，A-79）。
- **验收断言**：
  1. `/data merge entity <mob> {NoAI:0b}` 后触发一次挂起 ⇒ `NoAI:1b`；恢复后 `NoAI` 回到 **0b**，且 `/data get entity` 里**看不到** `neoforge:attachments.farmstorm:ai_suspend`（42 号 §7 #28）。
  2. 本来就 `noAi` 的怪（`/data merge … {NoAI:1b}`）走同一流程 ⇒ 恢复后**仍是** `NoAI:1b`（42 号 §7 #15，裁定 #8）。
  3. `/stop` 时 80 刻未满 ⇒ 重启后该生物 `NoAI` 是 **0b**（提前恢复），附件里没有 `farmstorm:ai_suspend`（42 号 §7 #23，裁定 #16）。
  4. 强杀进程（不 `/stop`）后重启 ⇒ 载入时按 `sources` 结算：震动豆那一路按已到期恢复（≤ **1** 刻）、附件被删（42 号 §7 #24）。
  5. 源码面：`removeData(` 恰 **1** 处（唯一出口）；`LivingDeathEvent` / `MobEffectEvent` 命中 **0**；「观测不到多少刻后放弃」这类上限常量命中 **0**。
  6. 本版网络包仍 **2** 个（`.playToClient` = 2，`PROTOCOL_VERSION = "2"`）；附件不带 `.sync(...)`、不进 `getUpdateTag`（42 号 §1.2.1、§5.1）。

#### F-254 · 震动豆：范围内所有非玩家生物删 AI 80 刻（M · 1.5 人日｜依赖 F-253）

- **目标**：震动豆**触发的那一刻**，给触发半径内**所有非玩家生物**删 AI **80 刻（4 秒）**，并按进入前的原值记录（42 号 §2.3.6；42 号 §3 #14、#15；裁定 #4、#5、#8）。
- **涉及文件**：`behavior/impl/VibrationBeanTrap.java:74-105`（在 `:91` 玩家混乱循环之后、`:92` 取 `pos` 之前**追加一段**；常量区加 `AI_SUSPEND_TICKS = 80`）。
- **具体改动**：球内 `Mob` 全量遍历 + 与触发端**同一个** `vibration_bean.triggerRadius` 与同一个严格小于（`:900-902`，默认 4.0）；`isAlive()` 过滤；逐只调 `MobAiSuspension.suspend(mob, SRC_VIBRATION_BEAN, now + 80)`（骨架见 42 号 §2.3.6）；一条 INFO 打出「本轮生物数 / 删 AI 数」（41 号 §4.2 允许进日志）；既有四步（玩家混乱 / 爆炸音 / `consume` / 授进度）**一字不改**。
- **实现要点**：`Mob` 已排除玩家，**不写** `!(mob instanceof Player)` 这类冗余分支（`WildHunterAura:88` 之所以写，是因为它的入参是 `LivingEntity`）；**80 刻是常量不是 config 键**（写成键会把 175 变 177）；80 刻 ≠ `vibration_bean.confusionTicks`（后者是同值不同义的**玩家混乱时长**，不得合并、不得把「改混乱时长」当「改 AI 时长」）。
- **易漏点**：① 只删**触发者**的 AI（裁定 #4 说的是范围内所有）；② 新设第二个半径键（触发端 / 混乱端 / 本段必须同一个键）；③ 顺手给生物也上混乱（`CONFUSION` 的循环只遍历 `level.players()`，本版不动）；④ 用创造 / 旁观号做「不触发」的反向断言（既存口径：创造 / 旁观**不触发**却会被别人触发的豆晕到，42 号 §2.3.6 末条 / §9 第 13 条 —— 那一测会红，但红的是既存口径）。
- **验收断言**：
  1. 半径内 1 只僵尸（`NoAI:0b`）+ 1 个生存模式非己方玩家 ⇒ 触发后 `NoAI:1b`；**80 刻后** `NoAI` 回到 **0b**；玩家照旧拿到 `CONFUSION`（42 号 §7 #14）。
  2. 半径内 **3** 只生物 ⇒ 3 只**都**被删 AI（不是只有触发者），日志里本轮计数是 **3**（42 号 §7 #14 的第 2、3 只）。
  3. 半径内只有玩家、没有生物 ⇒ 行为与 0.2.0 完全一致（日志本轮计数 **0**，四步照旧）（41 号 §9 B-21）。
  4. 80 刻内被第二颗豆覆盖 ⇒ 窗口刷新到「此刻 + 80」、不叠加成 160，原值仍是**第一次**进入前的读数（41 号 §9 B-19）。
  5. `AI_SUSPEND_TICKS = 80`；`FarmstormConfig` 的 `b.push(` 仍 **60**、行首 `.define*` 仍 **175**（本任务不碰 config）。

#### F-255 · Q弹：效果存续期删 AI、mount 跳过（M · 1.5 人日｜依赖 F-251、F-253）

- **目标**：带 Q弹 的**非玩家生物**在效果存续期间一直 `noAi`，效果一结束立刻按原值恢复；**mount 态跳过**，删了之后切到 mount 的**先恢复再挂坐骑**（42 号 §2.3.4、§2.3.5；42 号 §3 #16；裁定 #6、#7）。
- **涉及文件**：`effect/QbounceEffect.java:15-19`（现 20 行、0 覆写 ⇒ 新增**一个**覆写 `onEffectAdded(LivingEntity, int)`）；`combat/MobAiSuspension.java`（进入面转发 + 对账里的 mount 两层判定）。
- **具体改动**：只覆写 `onEffectAdded`（21.1.213 里它**只在「此前没有同一效果」的分支**被调：`MobEffect.java:86` + `LivingEntity.java:957-961`），**不覆写** `applyEffectTick` / `isDurationEffectTick`；退出面**不在效果类里做**（21.1.213 的 `MobEffect` 没有 `onEffectRemoved`），交给 F-253 的每刻对账读 `hasEffect`；mount 第一层（进入时是 mount ⇒ `return`，不写附件）与第二层（对账读到 mount ⇒ 先 `release(SRC_Q_BOUNCE)`）都在 `MobAiSuspension`。
- **实现要点**：恢复判据是「效果**是否还在身上**」，**不是**「进入后过了多少刻」（RT-56：`FriendChivesProjectile` 每刻重扫并写「场长 − 已过刻数」，一次性计时器会被重置或提前恢复）；进入点**幂等**（`sources` 位不重复置位）；两个来源位可共存（「震动豆到点了但 Q弹 还在」不提前恢复）。
- **易漏点**：① 在第一次施加时配一次性计时器（RT-56 的两种翻车）；② 在效果类里解析 `qbounce.poseMode`（mount 的真相只有一处，只调 F-251 的入口）；③ 给 Q弹 也记「到期刻」（裁定 #16）；④ 覆写 `applyEffectTick` 来做「每刻检查」（它现在走空实现，覆写会把效果变成有副作用的类，42 号 §2.3.4 明确不做）。
- **验收断言**：
  1. `friend_chives` 落在生物身上 ⇒ `NoAI:1b`；效果存续期反复刷新时附件里的 `sources` 恒为 `SRC_Q_BOUNCE`（不进位两次）、`originalNoAi` 不随后续刷新改变（42 号 §7 #16）。
  2. 三条路径各跑一次——等效果自然到期（`friend_chives.effectTicks = 200`）、`/effect clear <mob> farmstorm:q_bounce`、让生物喝牛奶 ⇒ 三条都在 **1 刻内**把 `NoAI` 恢复成原值（42 号 §7 #17）。
  3. `qbounce.poseMode = mount` ⇒ 带 Q弹 的生物 `NoAI` 恒 **0b**（全程不删）；切回 `render` 后**下一次施加**才删（42 号 §7 #18）。
  4. 生物已被删 AI、随后把配置切到 `mount` ⇒ 先按原值恢复（`NoAI` 变回原值）、再挂坐骑（42 号 §2.3.5 第二层；41 号 §9 B-26）。
  5. `QbounceEffect.java` 的覆写数 = **1**（`onEffectAdded`）；`applyEffectTick` / `isDurationEffectTick` 命中 **0**；该类总行数 ≥ 20（只增不减）。

#### F-256 · 核心储物 tooltip：保证默认链路 + 3 条验收（M · 1.5 人日｜依赖 —）

- **目标**：把「悬停物品即显示详情」写成**显式需求 + 显式验收**，实现面**优先一行不改**（42 号 §2.2；42 号 §3 #12、#13；D-178；裁定 #3；RT-59）。
- **涉及文件**：`client/OutpostCoreScreen.java`（**42 行**，只覆写 `renderBg:37-41` —— 预期**零改动**）；`menu/OutpostCoreMenu.java`（**140 行**，54 存储格 + 36 玩家格，三处 `addSlot` 在 `:68` / `:79` / `:84`）；验收记录条目交给 F-264。
- **具体改动**：先按 42 号 §2.2 的四道证据确认链路未被绕过（`render` / `renderTooltip` / `renderLabels` / `init` 覆写 **0**；全仓 `renderTooltip` **0**；三处 `new Slot(` 全是原版类）；**只在验收失败时**启用退路（给 `render` 覆写补一次 `super.render(...)`，改动量 ≤ 2 行，仍不新增渲染代码）。
- **实现要点**：「保证」是**可验收的行为承诺**，不是「大概没问题」；**不新增** Screen / Slot / 渲染代码；不接管 tooltip 的定位 / 缩放 / 背景 / 延迟显示（接管一处就要接管全部，42 行的类会变成自绘界面）；本模组有 `tooltip.farmstorm` **20 条**语言键，它们在「详情」范围内，不是只显示名字。
- **易漏点**：① 重写 `render` 自绘 tooltip（D-178 已否决）；② 加自定义 `Slot` 子类「提供 tooltip」（原版 `Slot` 不负责 tooltip，加子类解决不了任何问题）；③ 把「代码里有没有一行 `renderTooltip`」当判据（原版调用点行号本机不可核，验收**只能**写「悬停可见」）；④ 顺手改 `ConverterScreen:77` 的 `renderLabels`（与本条无关）。
- **验收断言**：
  1. 己方空手右键核心打开 54 格界面，悬停**第 1 / 28 / 54 格**里的物品 ⇒ 出现原版 tooltip：第一行为物品名、详情行 ≥ 2 行，数量为 1 时不显示「1」（42 号 §7 #11）。
  2. 悬停**空槽** ⇒ **无** tooltip（不是空框）；悬停**玩家背包格** ⇒ 与 54 格一致（42 号 §7 #12）。
  3. 悬停带附魔 / 带 `temporary_enchantments` 组件的物品 ⇒ 附魔行 / 效果行都显示（41 号 §3.4 步 3）。
  4. 源码面：`OutpostCoreScreen` 的 `render` / `renderTooltip` / `renderLabels` / `init` 覆写数 = **0**；全仓 `renderTooltip` 命中 = **0**；`OutpostCoreMenu` 里 `new Slot(` = **3 处调用 / 90 个槽位**（54 + 27 + 9），无 `Slot` 子类（42 号 §7 #13；调用数与槽位数之分见 A-77）。
  5. 若第 1 条失败并启用了退路 ⇒ 记录里写明改动行数与「仍不新增渲染代码」，并把第 4 条的覆写数从 0 更新为实际值。

### Phase 43 · R5 陷阱 ×3 与 config 开窗（F-257 ~ F-260）

阶段目标：给 ×3 造出**只作用于敌对生物**的一道闸，并让倍率只落在**陷阱自己结算的那一次伤害**上——4 个陷阱作物里只有 2 个参与。出口判据：胡罗贝对僵尸 21.0、对玩家 7.0；桑吉尔蔗对僵尸 22.5；瓜！/ 莫洛托夫等 6 处非陷阱爆炸在倍率 1.0 与 10.0 下读数不变。

#### F-257 · 敌对谓词 `util/Hostility#isHostile`（0 先例）（S · 0.5 人日｜依赖 —）

- **目标**：新增 **`net.minecraft.world.entity.monster.Enemy` 实例 ∪ `MobCategory.MONSTER`** 的并集判定，**只**服务 ×3 的那一道闸（42 号 §2.4；42 号 §3 #20；裁定 #9；RT-57）。
- **涉及文件**：新建 `util/Hostility.java`（无状态纯函数，与 `util/DeployedOwnership`、`util/DistanceMetrics` 同类）；只读参照 `combat/WildHunterAura:88`（`Mob` 谓词的现成形态）。
- **具体改动**：`isHostile(@Nullable Entity)` 四个分支（非 `LivingEntity` → false；玩家 → false；`instanceof Enemy` → true；`getType().getCategory() == MobCategory.MONSTER` → true），顺序照 42 号 §2.4 的骨架。
- **实现要点**：`Enemy` 是**纯标记接口**（`Enemy.java:1-11`，11 行、0 方法）⇒ `instanceof` 就是全部判定；`Monster implements Enemy` ⇒ 并集的实际增量只有两类（类别是 `MONSTER` 但没实现 `Enemy` 的模组生物、实现 `Enemy` 但不在 `monster` 包的生物）；**使用点只有 2 处**（F-259、F-260）。
- **易漏点**：① 顺手把 5 处 `getEntitiesOfClass(Monster.class, …)` 换成新谓词（改的是陷阱的**触发面**，RT-57 明令禁止，D-174 也挡在门外）；② 用 `instanceof Monster` 代替 `Enemy`（裁定 #9 要覆盖其它模组的敌对生物）；③ 只看 `MobCategory`（比 `Enemy` 窄，且与并集写法不符）；④ 顺手加白名单（40 号 §7 ③ 定为「不扩判据」）。
- **验收断言**：
  1. 全仓 `Enemy` 的命中从 **0** 变成 **1 个文件**（`util/Hostility.java`）；`isHostile` 的定义 **1** 处（使用点 2 处在 F-259 / F-260 落地后才出现）。
  2. 5 处既有 `Monster` 过滤的行号**逐字未变**：`BlastCarrotTrap:59`、`FangTrapCactusTrap:54`、`VibrationBeanTrap:59`、`ZangiefCaneTrap:174`、`PumpkinTurret:109`（42 号 §7 #27）。
  3. `MobCategory` 的命中从 **2**（两处 `MISC`）变成 **3**（新增的一处是 `MONSTER`）。
  4. `gw.cmd build` 退出码 0；Java 文件数 **148 → 149**（F-248 已到 146、F-252 两个新类到 148）。运行时读数归 F-259（42 号 §8 ④ 已把「模组生物覆盖缺口」的确认指给倍率任务）。

#### F-258 · `trap.damageMultiplier` 键与翻译（**开窗**）（S · 0.5 人日｜依赖 —）

- **目标**：`trap` 组 +1 键（double、默认 **3.0**、值域 **1.0 ~ 10.0**）+ 1 条中英翻译；本提交**同时开两个窗口**（42 号 §2.5.1、§4.1.2、§4.3；42 号 §3 #24、#25；D-183、D-185；A-74）。
- **涉及文件**：`config/FarmstormConfig.java`（`:917-927` 的 `trap` 段内、`trapMaxTrapsPerPlayer`（`:924-926`）之后、`:927` 的 `b.pop()` 之前；字段区 `:223-225` 之后）；`datagen/ModLangProvider.java:412-413` 之后（+1 条 `config(entries, "trap.damageMultiplier", 中文, 英文)`）。
- **具体改动**：`defineInRange("damageMultiplier", 3.0, 1.0, 10.0)` + comment + `translation(tr("trap.damageMultiplier"))`；字段声明 `public static ModConfigSpec.DoubleValue trapDamageMultiplier;`；语言键全名 `farmstorm.configuration.trap.damageMultiplier`（`tr()` 前缀 `farmstorm.configuration.`，`FarmstormConfig:1001`）。文案照 42 号 §4.3 的两行。
- **实现要点**：**不改任何既有默认值**（`blast_carrot.maxDamage` 仍 7、`zangief_cane.slamDamage` 仍 7.5）；`trap` 组 3 → 4 键、组数仍 **60**（`b.push` 次数不变）；下界取 **1.0**（= 不改伤害），不取 0.0（会造出「命中但不掉血」的第三态，42 号 §4.1.2）；上界 10.0（`cap 7 × 10 = 70` 在 float 精确整数区内）。
- **易漏点**：① 只加 config 不加翻译（338 × 2 会变成 338 / 337）；② 下界写成 0.0；③ 落盘键名写成 `trapDamageMultiplier`（落盘键必须是 `[trap]` 段的 `damageMultiplier`）；④ 顺手改 `ModLangProvider.java:13-16` 的类注释总数（那是 F-263 的事，窗口纪律第 2 条）。
- **验收断言**：
  1. **本条键存在**：`run/config/farmstorm-server.toml`（先跑一次 `runServer` 重生成）里 `[trap]` 段恰 **4** 键、`damageMultiplier = 3.0` 出现 **1** 次。
  2. **本条键存在**：`FarmstormConfig.java` 行首 `.define*` 里 `"damageMultiplier"` 命中 **1**；`b.push(` 仍 **60**、`b.pop()` 仍 **60**。
  3. **本条键存在**：`zh_cn.json` 与 `en_us.json` 各含 `farmstorm.configuration.trap.damageMultiplier` **1** 次；`ConvertFrom-Json` 后 zh-only = **0**、en-only = **0**。
  4. 本任务**不写** `176` 与 `338` 两个总数（§0.3 窗口纪律第 1 条）；窗口内允许的写法是「本条键存在 / 不存在」+「`[trap]` 段内 4 键」+「全文 175 或 176（区间读数）」。

#### F-259 · `FarmstormExplosion` 第 9 参 `trapSourced` + 乘点①（M · 1.5 人日｜依赖 F-257、F-258）

- **目标**：给爆炸一个**最小限度的来源标记**，使倍率只落在 `blast_carrot` 的那一次爆炸上；**保留 8 参重载委托 `false`**，让 **6 处非陷阱调用零改动**（42 号 §2.5.2、§2.5.3 乘点①；42 号 §3 #21、#22；A-72）。
- **涉及文件**：`explosion/FarmstormExplosion.java:69-78`（8 参构造器 → 9 参 + 8 参重载）、`:178-179`（`hurtCapped` 的乘点）；`behavior/impl/BlastCarrotTrap.java:84-86`（**唯一**改成传 `true` 的调用点）。
- **具体改动**：新 9 参构造器吃 `boolean trapSourced`（`damageCap` 之后）；原 8 参签名保留为重载、唯一语句是委托 `false`；乘点：`this.trapSourced && Hostility.isHostile(entity)` 时 `dealt *= FarmstormConfig.trapDamageMultiplier.get()`，乘的是**封顶后的最终伤害**（`min(raw, cap)` 之后）。
- **实现要点**：**不新增伤害源**（新开 `farmstorm:trap_blast` 会把 `damage_type` 从 3 变 4、97 变 98，还改 `#minecraft:is_explosion` 的标签语义）；**不按 `BlastRule` 反查作物**（`BlastRule.forCrop` 只返回规则、把档案丢掉）；**不按 `damageSource.getEntity()` 判**（那是「谁放的」，不是「哪条链路」）；`:160` 的 `ignoreExplosion` 与「不宽恕使用者」的旧口径（`:144-147`）都不动。
- **易漏点**：① 两个构造器都保留但把乘法写在 8 参路径上（等于对所有爆炸 ×3，正是 RT-58 / RT-57 要挡的错）；② 乘 `damageCap` 而不是乘最终伤害（`raw < cap` 时完全无效，`raw = 4` 该得 12 却仍是 4）；③ 忘了让 `BlastCarrotTrap:84-86` 传 `true`（陷阱反而不参与）；④ 顺手改 6 处非陷阱调用（它们**必须零改动**，这正是本条验收的核心）。
- **验收断言**：
  1. 全仓 `new FarmstormExplosion` 仍 **7** 处；**只有** `BlastCarrotTrap:84-86` 传了第 9 参，其余 **6** 处实参表仍停在 `damageCap`：`MelonGrenadeProjectile:42`、`MelonSliceProjectile:40`、`MolotovProjectile:94`、`ToxicUbiServerProjectile:43`、`UbiServerProjectile:41`、`LuckyBerryRoller:212`。
  2. 构造器声明恰 **2** 个（8 参与 9 参）；8 参那一个的方法体只有一行 `this(…, false)`。
  3. 胡罗贝炸僵尸：`min(raw, 7) × 3`，封顶读数 **21.0**；同一爆炸炸玩家：**7.0**（42 号 §7 #19 的前半）。
  4. **6 处非陷阱爆炸逐点不变**：`melon_grenade` / `molotov` 对僵尸的伤害，在 `damageMultiplier` 取 **1.0** 与 **10.0** 两次时读数**相同**（42 号 §7 #20）。这是「6 处没被 ×3」的判决性读数。
  5. `FarmstormDamageTypes` 的 `damage_type` 仍 **3** 个 `ResourceKey`（`:20,23,26`）；`farmstorm:crop_blast` 仍是 7 处共用的那一个。

#### F-260 · `ZangiefCaneTrap#monsterBranch` 乘点② + 日志打乘后值（S · 0.5 人日｜依赖 F-257、F-258）

- **目标**：生物分支的 `hurt` 对敌对生物乘倍率，**日志打乘后值**；玩家分支与击退 / 音效不动（42 号 §2.5.3 乘点②；42 号 §3 #23；RT-58）。
- **涉及文件**：`behavior/impl/ZangiefCaneTrap.java:111-118`（`amount` 读出后、两个判定之前的乘点）、`:126-128`（INFO 的伤害字段）。
- **具体改动**：`amount` 读出后、`source == null` 与 `amount > 0.0` 两个判定**之前**乘（`slamDamage` 配 0 时乘完仍是 0，行为不变）；日志**格式串不改**，只把值换成乘后值（否则日志与 `hurt` 的实际值对不上，验收读数失效）。
- **实现要点**：玩家分支（`ZangiefPiledriverController:303` / `:311`）**不在**倍率面内；击退（`:124`）与音效（`:125`）不动；敌对判据只调 `Hostility.isHostile(target)`，不回接触发面的 5 处 `Monster` 过滤（含本类的 `:174`）。
- **易漏点**：① 把乘法放在 `amount > 0` 之后（日志与 `hurt` 会不同步）；② 顺手把 `:174` 的 `getEntitiesOfClass(Monster.class, …)` 也换掉（触发面，RT-57）；③ 忘了日志（本条验收的一半读数靠它）。
- **验收断言**：
  1. 桑吉尔蔗对僵尸：伤害读数 **22.5**（`7.5 × 3`），INFO 行的「伤害」字段也是 **22.5**（42 号 §7 #19 的后半；42 号那里写 11.25，按 A-76 以 22.5 为准）。
  2. 对牛 / 村民（非敌对）：**7.5**，日志同值（§6 表 A 的参与面逐条点名）。
  3. 玩家踩到：走相位链（`ZangiefPiledriverController`），不进倍率面，读数与 0.2.0 一致。
  4. `slamDamage` 配 **0** ⇒ 无 `hurt`、日志伤害仍是 **0.0**（乘完仍是 0）。
  5. `ZangiefCaneTrap:174` 的 `getEntitiesOfClass(Monster.class, …)` 行号逐字未变；该类总行数仍 **183**。

### Phase 44 · 数值面与工具收口（F-261 ~ F-263）

阶段目标：把「文档写了键」变成「工具真的期待它」，并把两个总数收口到 176 / 60 与 338 × 2。出口判据：`verify_project.py` 的 config 段两行都是 176；`ModLangProvider.java:13-16` 的注释是 338 / 179；关窗后任何任务再增删键都是超范围。

#### F-261 · `verify_project.py` 新解析器 `expected_config_keys_v021`（M · 1.5 人日｜依赖 F-258）

- **目标**：让工具**真的期待** `trap.damageMultiplier`，并把期望数从 175 同步到 176；四条契约照 42 号 §4.1.1 逐条落地（42 号 §3 #26；RT-60、RT-60b；A-75）。
- **涉及文件**：`tools/verify_project.py`（新增 `expected_config_keys_v021`，形照 `:137-179` 的 `expected_config_keys_v012`；`main()` 的并集区 `:382-391`；期望数 175 → 176）。
- **具体改动**：四条契约——① 用 `^#### 4\.1\.1\b` 与随后的 `^#### 4\.1\.2\b` **界定范围**（不许全文扫 `^\|\s*\d+\s*\|`：42 号 §3 与 §7 的表也以 `| 1 |` 开头）；② 逐行恰好 **7** 列，第 2 列匹配 `` ^`([A-Za-z0-9_]+\.[A-Za-z0-9_.]+)`$ ``、第 6 列以「增」/「删」开头；③ 行数断言 **== 1**（与 `:172-178` 的 11 行断言同形）；④ 结果并进 `main()` 的 `union`（`:382-391`），期望数 175 → 176。
- **实现要点**：现行工具只读 **6 份固定文档**（`main():349-355`），**今天不读 42 号** ⇒「把键写进文档」是**必要但不充分**条件；解析器与 42 号 §4.1.1 的表格格式**绑定**，格式被改坏时按 `:172-178` 的形态**带文件名 + 行号抛出**（宁可红，不要静默少核）。
- **易漏点**：① 只改期望数不加解析器（工具照绿、键没人核，A-66 的假绿照旧）；② 用全文正则扫表格（会多抓 §3 / §7 的行）；③ 忘了把 42 号加进 `main()` 的文档列表（那样解析器永远读不到输入）；④ 改 `EXPECTED_COUNTS`（注册面仍是 97，本任务不动它）。
- **验收断言**：
  1. `python tools/verify_project.py` 的 config 段打印「文档口径键数: **176**」与「生成 toml 键数: 176」（42 号 §7 #2）。
  2. 新解析函数对 `docs/zh/42-TECH-0.2.1.md` §4.1.1 返回**恰 1 个**键：`["trap.damageMultiplier"]`。
  3. `grep -c 'trap\.damageMultiplier' docs/zh/42-TECH-0.2.1.md` ≥ **2**，且 §4.1.1 的表行被契约正则命中 **1** 行（42 号 §7 #22）。
  4. 反例：对一份**临时副本**（放 `$env:TEMP`，42 号本体一字不改）把 §4.1.1 改成 2 行 ⇒ 工具**报红并带文件名 + 行号**，不是静默通过。

#### F-262 · config 总账 176 键 / 60 组（**关窗**）（S · 0.5 人日｜依赖 F-258、F-261）

- **目标**：把 config 的总数收口：**176 键 / 60 组**，并给出三个互不相同的读数（42 号 §4.2 的三条独立校验；D-185；A-74）。
- **涉及文件**：`config/FarmstormConfig.java`（静态块 `:289-995` 的逐键点数）；`run/config/farmstorm-server.toml`（重生成后读数）；`tools/verify_project.py`（并集 = 176）。
- **具体改动**：只做**核对与记录**，不改源码；三条校验——① 行首 `.define*` = **176**；② `[trap]` 段 **4** 键、段数 **60**、总键 **176**（先跑一次 `runServer` 重生成）；③ 工具并集 = **176**。
- **实现要点**：**按键路径只认 `run/config/farmstorm-server.toml`**（实测 801 行 / 60 段 / 175 键）；仓库里几十份 `run-*/config/` 是旧副本（`run-verify` 等 12,238 字节、约 121 键），拿错一份就会得出「只有 121 键」的错误结论（42 号 §9 第 2 条即踩过，A-70 之外的读法坑）。本任务**关窗**，之后任何任务再增删 config 键都是超范围。
- **易漏点**：① 拿 `run-*/config/` 的旧副本当基线；② 把 client 的 3 键（`hud.*`）加进 176（不计入，42 号 §4.2）；③ 把组数算成 61（`trap` 是既有组，只加键不建组）；④ 忘了「行首 `.define*` 的 175 live + 1 注释行（`:935`）」这个口径（注释行不计）。
- **验收断言**：
  1. `FarmstormConfig.java` 行首 `.define*` = **176**（live 176；`:935` 的注释行不计）；`b.push(` = `b.pop()` = **60**。
  2. `run/config/farmstorm-server.toml`：段（`^\s*\[`）= **60**、键（`^\s*[A-Za-z0-9_]+\s*=`）= **176**、`[trap]` 段 **4** 键（含 `damageMultiplier = 3.0`）。
  3. `verify_project.py` 的 config 段两行都是 **176**（42 号 §7 #2）。
  4. 关窗记录里写明：F-258 ~ F-261 的验收里**没有**「176 / 60」这两个数（§0.3 纪律第 1 条的复核）。

#### F-263 · 语言键总账 338 × 2（**关窗**）（S · 0.5 人日｜依赖 F-258）

- **目标**：把语言键收口到 **338 × 2**（中英同键集），并同步 `ModLangProvider` 的类注释（42 号 §4.3；D-185；A-74）。
- **涉及文件**：`src/generated/resources/assets/farmstorm/lang/zh_cn.json` / `en_us.json`（各 **338**）；`datagen/ModLangProvider.java:13-16`（类注释的 `**337**` → `338`、`**178** config` → `179`）。
- **具体改动**：只做核对 + 注释同步：`:14` 的 `**178** config（= 175 server + 3 client）` → `**179** config（= 176 server + 3 client）`；`:16` 的 `= **337**` → `= **338**`。
- **实现要点**：**语言键没有自动闸**——`verify_project.py` 第 3 节只比 zh / en **键集相等**，**不校验 338 这个数**（42 号 §9 第 4 条）⇒ 只能人工 `ConvertFrom-Json` 数键集；中英必须**同键同数**（只加中文会让 338 × 2 变成 338 / 337）。
- **易漏点**：① 只改 json 忘了类注释（注释说 337、实际 338，下一个作者会按注释核）；② 只加中文不加英文；③ 把 `tooltip.farmstorm` 的 20 条算进 `farmstorm.configuration.*` 的 179；④ 顺手把 `:237` 的「139 个配置项」注释也改了（那是 42 号 §9 第 11 条登记的既存漂移，**不在本版范围**）。
- **验收断言**：
  1. `ConvertFrom-Json` 取键集：`zh = 338`、`en = 338`、zh-only = **0**、en-only = **0**（42 号 §7 #5）。
  2. `farmstorm.configuration.*` = **179**（= 176 server + 3 client）；`tooltip.farmstorm.*` 仍 **20**。
  3. `ModLangProvider` 的 `config(entries, …)` 次数 = **179**（改前 178）。
  4. `ModLangProvider.java:13-16` 里 `337` 命中 **0**、`338` 命中 ≥ 1；`:237` 的「139 个配置项」逐字未变。

### Phase 45 · 全量验收与签字（F-264 ~ F-266）

阶段目标：把 42 号 §7 的 31 条与 41 号 §9 的 36 条逐条跑掉，并把红线、注册面、数值面与上游一致性签成一份可复核的记录。出口判据：两份记录一条不漏；签字含七项读数；上游文件一字未改。

#### F-264 · 42 号 §7 的 31 条验收全量执行（L · 3.0 人日｜依赖 F-247 ~ F-263）

- **目标**：逐条执行 42 号 §7 的 **31** 条验收，记录到 `.work/v021-verify/acceptance.md`（42 号 §7 与表下的「四条必测组合」）。
- **涉及文件**：验收记录 `.work/v021-verify/acceptance.md`（新建）；只读 `run/logs/latest.log`、`run/config/farmstorm-server.toml`、`src/generated/.../lang/*.json`、`/data get`。
- **具体改动**：31 条逐条「构造 → 读数 → 记录」，每条给：编号、执行动作、读数、通过 / 失败、证据（日志行原文或 `/data get` 输出）。
- **实现要点**：四条**必测组合**不能拆——`#6 + #7`（补绑没被粒子门筛）、`#14 + #15`（恢复原值 vs 恢复成有 AI）、`#19 + #20`（陷阱 vs 非陷阱）、`#23 + #24`（停服 vs 崩溃）；`#1` 的 Java 文件数按 **149** 写（145 + 4 个新类，A-78）。
- **易漏点**：① 只跑 `#6` 不跑 `#7`（默认 40 刻下 200 刻也「在 300 刻内」，看不出被筛）；② 用创造 / 旁观号做震动豆的「不触发」反向断言（既存口径会红，42 号 §2.3.6 末条）；③ 把 `#11` 的判据写成「代码里有 `renderTooltip`」（只能写「悬停可见」）；④ 把 `#19` 的桑吉尔蔗读数照 42 号抄成 11.25（A-76）。
- **验收断言**：
  1. 记录里 `#1` ~ `#31` **31** 条一条不漏，每条有读数（不是「已验」两个字）；`#25`（`world/data/` 的 `.dat` 文件数不变）与 `#28`（恢复即删附件）通过。
  2. `#7`（粒子间隔 1200 仍 100 刻内补上）与 `#20`（6 处非陷阱爆炸读数在倍率 1.0 / 10.0 下不变）通过 —— 这两条分别是 RT-51 与 RT-57 的判决性读数。
  3. `#21`（尖牙伤害与 0.2.0 逐点相同）通过，且记录里写明「4 个陷阱作物里 **2** 个参与」（RT-58）。
  4. `#22` 的 `grep -c 'trap\.damageMultiplier'` ≥ 2 且解析器返回 1 键；`#29` 的启动日志仍是 `自检 注册对象 97 个 … → OK`。

#### F-265 · 41 号 §9 的 36 条边界复跑（L · 3.0 人日｜依赖 F-264）

- **目标**：逐条跑 41 号 §9 的 **36** 条边界（B-01 ~ B-36），记录到 `.work/v021-verify/boundaries.md`（41 号 §9；与 42 号 §7 分开记，因为 42 号验收表不含 B 面）。
- **涉及文件**：边界记录 `.work/v021-verify/boundaries.md`（新建）；只读源码与游戏内。
- **具体改动**：B-01 ~ B-36 逐条执行，按 41 号 §9 的归属分五组（B-01 ~ B-12 属 R1、B-13 ~ B-17 属 R2、B-18 ~ B-23 属 R3、B-24 ~ B-30 属 R4、B-31 ~ B-36 属 R5），每组一条汇总。
- **实现要点**：其中 **6 条是负向判据**（B-04 / B-06 / B-16 / B-25 / B-26 / B-33），判据是「没发生」而不是「发生了什么」；B-19（二次覆盖刷新不叠加）与 B-27（五条清理路径）要单独构造；B-30（读档前后）与 F-264 的 `#24`（崩溃残留）**不是同一条**，前者验「落盘原值」，后者验「载入结算」。
- **易漏点**：① 把 B-30 当成 `#24` 重跑一遍；② 漏 B-11（两个核覆盖同一批未绑定培养器）——它是 RT-51 的边界面；③ 漏 B-03 / B-12（未加载区块 / 重启后票恢复前）——两条都要证明「没有新区块被加载」。
- **验收断言**：
  1. 记录里 `B-01` ~ `B-36` **36** 条一条不漏，分五组标注。
  2. 六条负向判据（B-04 / B-06 / B-16 / B-25 / B-26 / B-33）全部通过。
  3. B-19（80 刻内二次覆盖 ⇒ 刷新不叠加、原值仍是第一次）与 B-27（五条清理路径全走「按原值恢复 + 删记录」）通过。
  4. B-11 通过：两个己方核覆盖同一批未绑定培养器时，同一格只落到一个核（先写者占缓存）。

#### F-266 · 红线、注册面与上游一致性签字（M · 1.5 人日｜依赖 F-261 ~ F-265）

- **目标**：签字确认五件事——红线（0 Mixin / 2 包）、注册面 **97（+0）** 与**口径外第 13 类**、数值面 **176 / 60** 与 **338 × 2**、**不改清单**（42 号 §3 #27、#28 与 5 处 `Monster` 过滤）、上游一致性（42 号 §9 的 16 条差异与本文 A-70 ~ A-81 的处置）（A-73）。
- **涉及文件**：签字记录 `.work/v021-verify/signoff.md`（新建）；只读源码与日志。
- **具体改动**：逐项给读数：`@Mixin` = 0、`src/` 下 `*.mixins.json` = 0、`.playToClient` = 2、`PROTOCOL_VERSION = "2"`、12 类相加 = 97、`data_components` = 1、**附件类型 = 1（口径外）**、config 176 / 60、lang 338 × 2。
- **实现要点**：**第 13 类必须显式写进签字**——「另注 1 个附件类型 `farmstorm:ai_suspend`，不计入 97，`verify_project.py` 既不核存在也不核计数」（裁定 #17；A-73）；「不改」的两行（42 号 §3 #27 / #28）要有**逐字未变**的证据（行号 + 片段），不是「应该没动」。
- **易漏点**：① 把附件类型算进 97（会得出 98 的错误结论）；② 用「全仓 `*.mixins.json`」计数（`src/` 口径才是 0；`.scratch/p25a/src/neoforge.mixins.json` 是实验残留，42 号 §9 第 10 条）；③ 签字只写「通过」不给读数；④ 顺手改了 42 号 §9 的任何一句（上游一字不改，差异只登记）。
- **验收断言**：
  1. 签字记录含八项读数：Mixin **0**（`src/` 口径）、`*.mixins.json` **0**（`src/`）、`.playToClient` **2**、注册面 **97**、`data_components` **1**、附件类型 **1**（口径外）、config **176 / 60**、lang **338 × 2**。
  2. `Farmstorm.java:169-172` 的 12 类断言逐字未变；5 处 `Monster` 过滤的行号逐字未变（42 号 §7 #27）；`FangTrapCactusTrap:87-109` 的原版尖牙生成与 `vibration_bean` 的 0 伤害逐字未变（42 号 §3 #28）。
  3. 签字里点明覆盖缺口：「附件类型不在 `EXPECTED` 的任何一组、也不在 `EXPECTED_COUNTS` 的相加里」（42 号 §1.2.1；A-73）。
  4. 签字引用 42 号 §9 的 16 条差异登记，并说明本版按 A-70 ~ A-81 的处置；`.work/` 与 `docs/zh/40` ~ `42` 的 mtime 未变。

---

## 3. 依赖图与并行度

### 3.1 六条依赖链（示意）

下图只画**前驱 → 后继**的主干，不追求把每条边画全（F-264 实际等 17 个前驱）：

```
链 1（发布）：      F-247
链 2（补绑）：      F-248 → F-249 → F-250
链 3（AI 载体）：    F-252 ─┐
                    F-251 ─┴→ F-253 ─┬→ F-254
                                    └→ F-255
链 4（tooltip）：    F-256
链 5（敌对与倍率）： F-257 → F-259 ／ F-258 → F-260（另需 F-257）／ F-258 → F-261 → F-262 ／ F-258 → F-263
收口：              F-264（等 F-247 ~ F-263 全部）→ F-265 → F-266
```

**读图三条**：

1. **两条互不依赖的起点是对称的**：F-248（区域枚举）与 F-257（敌对谓词）都是「全仓 0 先例」的纯工具，各自单独可提交、单独可回滚；它们分别喂给补绑链与倍率链，**不要**为了「少一个任务」把它们并进调用方（并进去就没有独立可跑的验收了）。
2. **F-252 与 F-251 是 AI 链的两条并行前驱**：载体（附件）与 mount 入口互不依赖，F-253 同时等两者；把可见性改到 F-253 里会让「一个修饰符」的改动混进一个 L 任务，回滚粒度失真。
3. **F-264 是全图唯一的大汇合点**：它等 F-247 ~ F-263 全部落地（含两条关窗任务），因此关窗必须早于验收——否则验收会拿一份「注释说 337、实际 338」的文档去核。

### 3.2 关键路径

按依赖取最长前驱（每个节点取所有前驱累计完成时间的最大值）：

```
F-252 附件载体            1.5   （累计 1.5）
 └─ F-253 MobAiSuspension 3.0   （累计 4.5，另需 F-251 的 0.5，取大者 4.5）
     └─ F-255 Q弹 接入    1.5   （累计 6.0）
         └─ F-264 验收    3.0   （累计 9.0）
             └─ F-265 边界 3.0  （累计 12.0）
                 └─ F-266 签字 1.5（累计 13.5）
```

**关键路径 = 13.5 人日 / 6 个任务**，占总量 27.5 的 **49.1%**。这条链是纯依赖推算的结果。

**并列的第二条 13.5**：`F-252 → F-253 → F-254 → F-264 → F-265 → F-266`（`1.5 + 3.0 + 1.5 + 3.0 + 3.0 + 1.5 = 13.5`）。它在 F-253 之后分叉走「震动豆」而不是「Q弹」，两条在 F-264 之前同时到达 6.0 —— 排期时把 F-254 与 F-255 视作同一层即可。

次长链（同样按依赖取最长前驱，不进关键路径）：

| 排名 | 链 | 人日 |
|---|---|---|
| 3 | F-248 → F-249 → F-250 → F-264 → F-265 → F-266 | 1.5 + 1.5 + 1.5 + 3.0 + 3.0 + 1.5 = **12.0** |
| 4 | F-257 → F-259 → F-264 → F-265 → F-266 | 0.5 + 1.5 + 3.0 + 3.0 + 1.5 = **9.5** |
| 5 | F-256 → F-264 → F-265 → F-266 | 1.5 + 3.0 + 3.0 + 1.5 = **9.0** |
| 6 | F-258 → F-260 → F-264 → F-265 → F-266 | 0.5 + 0.5 + 3.0 + 3.0 + 1.5 = **8.5**（F-258 → F-261 → F-262 → F-266 与它并列，`0.5 + 1.5 + 0.5 + 1.5 = 4.0`） |

> **读法**：除 `F-258 → F-261 → F-262 → F-266` 这一条（4.0）以外，每条链都汇到 F-264，所以 **F-264 之前的所有 M / L 都是并行的**；想缩短日历工期，优先压 F-253 那 3.0 人日（它是 13.5 的关键路径上唯一一个 L），而不是压验收（验收 6.0 人日已在链尾，压它等于把读数做薄）。

### 3.3 并行簇

| 簇 | Phase | 可并行任务 | 并行度 | 说明 |
|---|---|---|---|---|
| A | 40 ~ 41 | F-247、F-248 | 2 | 版本号与区域枚举互不共享文件；F-249 / F-250 与 F-248 串行 |
| B | 42 | F-251、F-252、F-256 | 3 | mount 入口、附件载体、tooltip 三件事互不共享文件 |
| C | 43 | F-257、F-258 | 2 | 谓词与配置键互不依赖；F-259 / F-260 各自等它们 |
| D | 43 | F-259 与 F-260 | 2 | 两处乘点分属两个类，可两人并行 |
| E | 44 ~ 45 | F-261、F-263 | 2 | 工具与语言总账互不共享文件；F-262 等 F-261，F-264 之后逐级串行 |

**并行度上限 6**（簇 A + B + C 同时开），实际受**同文件冲突**约束：F-249 / F-250 都改 `OutpostCoreBlockEntity.java`（串行）；F-252 / F-253 / F-255 都改 `MobAiSuspension.java`（按 252 → 253 → 255 串行）；F-257 / F-260 都改 `ZangiefCaneTrap` 的调用面（`Hostility` 先落地）；F-258 / F-261 / F-262 / F-263 共享「config 与语言」这条读数面（按 258 → 261 / 262 / 263 串行）。按「一个任务只动自己那一段」的约定后，**可稳定并行 3 路**。

---

## 4. 人日汇总

### 4.1 按 Phase 与里程碑

| Phase | 名称 | 任务 | 任务数 | S | M | L | 人日 | 里程碑 |
|---|---|---|---|---|---|---|---|---|
| 40 | 版本与基线 | F-247 | 1 | 1 | 0 | 0 | `0.5` = **0.5** | **M36** |
| 41 | R1 周期补绑 | F-248 ~ F-250 | 3 | 0 | 3 | 0 | `3 × 1.5` = **4.5** | **M37** |
| 42 | R2 tooltip 与 R3/R4 AI 干预 | F-251 ~ F-256 | 6 | 1 | 4 | 1 | `0.5 + 4 × 1.5 + 3.0` = **9.5** | **M38** |
| 43 | R5 陷阱 ×3 与 config 开窗 | F-257 ~ F-260 | 4 | 3 | 1 | 0 | `3 × 0.5 + 1.5` = **3.0** | **M39** |
| 44 | 数值面与工具收口 | F-261 ~ F-263 | 3 | 2 | 1 | 0 | `2 × 0.5 + 1.5` = **2.5** | **M40** |
| 45 | 全量验收与签字 | F-264 ~ F-266 | 3 | 0 | 1 | 2 | `1.5 + 2 × 3.0` = **7.5** | **M41** |
| **合计** | | **F-247 ~ F-266** | **20** | **7** | **10** | **3** | **27.5** | **M36 ~ M41** |

### 4.2 总计（两个数 + 算式）

- **裸人日**：`7 × 0.5 + 10 × 1.5 + 3 × 3.0 = 3.5 + 15.0 + 9.0 = **27.5 人日**`
- **含 25% 缓冲**：`27.5 × 1.25 = **34.375 人日**`

三条互相校验：

1. 复杂度汇总：`7 + 10 + 3 = 20` = 任务数；
2. 人日汇总：`3.5 + 15.0 + 9.0 = 27.5` = 六个 Phase 小计之和 `0.5 + 4.5 + 9.5 + 3.0 + 2.5 + 7.5 = 27.5`；
3. 缓冲：`27.5 × 1.25 = 34.375`。

> 缓冲只吸收返工与调试，不吸收范围变化。与 0.2.0（34 个任务 / 61.0 裸人日）相比，本版任务少 14 个、裸人日少 33.5：变化来自三处——0.2.0 有 14 个 L（容器、菜单、迁移、状态机、资源、渲染、两批验收），本版只有 3 个 L；本版有 7 个 S（版本号、可见性、谓词、配置键、乘点②、两个总账）；两个「从零写」的构件（F-248 / F-257）虽按上一档计，但体量只有 1 个类。

### 4.3 并行后的日历工期

- **单串行**：`34.375 人日`，按 5 天/周 ≈ **6.9 周**。
- **稳定 3 路并行**（§3.3 末段）：`27.5 ÷ 3 ≈ 9.2 人日` 裸，缓冲后 `9.2 × 1.25 ≈ **11.5 人日** ≈ **2.3 周**`（@ 5 天/周）。
- **关键路径下界**：`13.5 人日`（§3.2）——**它比 3 路并行的 11.5 还长**，所以真实下界是 **13.5 人日 ≈ 2.7 周**，即使无限加人也不会低于它；与 3 路并行的差是 `13.5 − 11.5 = 2.0 人日`，再想快只能压 F-253（关键路径上唯一的 L）或把 F-264 / F-265 拆成两批（**不建议**：31 条与 36 条各自有必测组合，拆开跑会把配对读数拆散）。

---

## 5. 裁决台账（A-70 起）

本节收 **12 条**：全部是本文写作期新立，状态均为**已落**；每条给「结论 / 依据 / 落地位置」，上游文件一字不改（差异只登记在这里与交稿说明里）。

### 5.1 A-70 · 版本串按**行**是 6 处，不是 4 处

- **结论**：`gradle.properties:11` 1 处 + 两支 `.cmd` **5 行**（`一键构建.cmd:5,11,12`、`检查环境.cmd:5,14`）= **6 处字面量**；验收按 6 写（`0.2.1` 命中 6、`0.2.0` 命中 0）。
- **依据**：40 号 §3.2 第 2 行写「jar 名 + 两条抬头行，共 **4 处**版本串」，§7 ⑤ 再写一次「版本串共 4 处，其中 2 处是抬头行……另 2 处是 jar 名（`一键构建.cmd:11,12` 与 `检查环境.cmd:14`）」——**列了 5 个行号却记作 4 处**（把「一键构建的两行 jar 名」记作一处）。实测两支 `.cmd` 共 5 行含 `0.2.0`，加 `gradle.properties` 共 6 处。
- **落地位置**：**F-247**（6 处字面量）、**F-264**（产物面验收）、**F-266**（签字里的 `.jar` 名）。

### 5.2 A-71 · 补绑接线、`CultivatorBlock:73-84` 注释与**一条补绑 INFO** 必须同一提交

- **结论**：`serverTick` 接线（F-250）与 `CultivatorBlock:73-84` 的类注释改在**同一个提交**里；同一个提交还要加**一条补绑 INFO**（核坐标 + 本轮新增条数 + 累计/上限）。
- **依据**：① BRIEF §3.4 第 11 项要求注释「必须与周期补绑同改」——注释现在写「不自动认领……事后补一个核也不会绑上」（`CultivatorBlock:81-83`，实测），与 D-176 正好相反，分两次提交就留下「注释说不行、代码照做」的窗口；② 42 号 §7 #8 的观察位置是「补绑 INFO 行（`V-03`）」，而 42 号 §2.1.3 的 `rebindNearby` 骨架**没有日志调用** ⇒ 观测面在 42 号的骨架里缺失，本文按 42 号 §7 #8 的需求补这一行（否则「相位错开」那条验收无法读数）。
- **落地位置**：**F-250**（接线 + 注释 + INFO）、**F-264**（`#6` ~ `#8`）。

### 5.3 A-72 · `FarmstormExplosion` 第 9 参与乘点① 必须同一提交

- **结论**：加 `boolean trapSourced` 与在 `hurtCapped` 里用它**同一次提交**；不许「先加参数（无人使用）再乘」。
- **依据**：只加参数不加乘点时，第 9 参是纯 dead 参数；先加乘点再加参数则要在中间态判断「这次爆炸是不是陷阱」——两条路都会造出「6 处非陷阱调用可能被 ×3」的窗口，而这 6 处**零改动**正是本条的验收核心（42 号 §2.5.2 的七处表、§2.5.3 乘点①）。
- **落地位置**：**F-259**（一次提交）、**F-264**（`#19` / `#20` 成对跑）。

### 5.4 A-73 · 第 13 类附件：显式登记 + 承认工具不核它

- **结论**：`farmstorm:ai_suspend` 按「口径外第 13 类」登记，**三处写死**——F-252 的验收第 5 条、F-266 的签字（「附件类型 1 个，不计入 97，工具既不核存在也不核计数」）、以及 42 号 §1.2.1 本身；任务里**不许**把附件说成「+1 个注册对象」。
- **依据**：裁定 #17（97 不变，但必须显式登记、不许隐形）；42 号 §1.2.1 第 5 行（`EXPECTED` 与 `EXPECTED_COUNTS` 都不含附件类型）；42 号 §9 第 14 条（附件 ≠ 数据组件，D-175 否决的是后者）。「工具不数的东西等于没登记」与 A-66 的假绿是同一性质。
- **落地位置**：**F-252**（注册 + 验收 5）、**F-266**（签字 3）、**F-261**（不动 `EXPECTED_COUNTS`；仍是 97）。

### 5.5 A-74 · 两个数值面窗口的开闭任务钉死

- **结论**：窗口 **F-258 开**（config 键 + 中英翻译一次写完）；config 侧**关于 F-262**、语言侧**关于 F-263**、工具期望面**关于 F-261**。窗口内只写「本条键存在 / 不存在」与「`[trap]` 段 4 键 / 全文 175 或 176（区间读数）」，**不写** `176` / `338`。
- **依据**：§0.3 的三条窗口纪律；42 号 §4.2 的三条独立校验（源码行 / toml / 工具并集）与 §4.3 的两条纪律（中英同键同数、语言键不自动生成）；42 号 §9 第 4 条（语言键**没有自动闸**，`verify_project.py` 只比键集相等）。
- **落地位置**：**F-258**（开窗）、**F-261 / F-262 / F-263**（三处关窗）、**F-264**（`#2` / `#4` / `#5` 复跑）。

### 5.6 A-75 · 新解析器四条契约 + 「文档 + 解析器同一提交」

- **结论**：`expected_config_keys_v021` 严格按 42 号 §4.1.1 的四条契约实现（范围界定 / 7 列 + 第 2 列正则 + 第 6 列「增删」/ 行数 == 1 / 并进 `union` 且期望 176）；它与「42 号 §4.1.1 真的有一行 `trap.damageMultiplier`」算**同一个提交**的交付物。
- **依据**：42 号 §4.1.1 的「解析契约」四条与「⚠️ 只写文档不改解析器 = 假绿照旧」（工具只读 6 份固定文档，今天不读 42 号）；RT-60 / RT-60b；42 号 §9 第 3 条与表下「四条给 43 号的提示」第 1 条。
- **落地位置**：**F-261**（解析器 + 期望数）、**F-264**（`#22`）、**F-266**（签字引用工具三段输出）。

### 5.7 A-76 · 桑吉尔蔗 ×3 的读数是 **22.5**（42 号 §7 #19 的 11.25 是笔误）

- **结论**：`zangief_cane` 对敌对生物的伤害 = `7.5 × 3 = **22.5**`；F-260 与 F-264 的 `#19` 一律按 22.5 写。
- **依据**：41 号 §6.5 的算例表与 §7 第 26 行都是 `7.5 × 3 = 22.5`；42 号 §2.5.3 乘点②的公式也是 `amount × multiplier`；42 号 §7 #19 写「`zangief_cane` 对僵尸 = **11.25**（7.5 × 3）」，括号里的算式与数值自相矛盾（11.25 = 7.5 × 1.5）。按 `7.5 × 3` 的算式取 **22.5**。
- **落地位置**：**F-260** 的验收 1、**F-264** 的 `#19`；回灌目标：42 号 §7 #19。

### 5.8 A-77 · `new Slot(` 的判据是 **3 处调用 / 90 个槽位**（不是「54 + 36 次」）

- **结论**：`OutpostCoreMenu` 里 `new Slot(` 是 **3 处调用**（`:68` 存储格循环、`:79` 主背包循环、`:84` 快捷栏循环），运行期产生 **90** 个槽位（54 + 27 + 9）；验收按「3 处调用 / 90 个槽位 / 无 `Slot` 子类」写。
- **依据**：42 号 §7 #13 写「`OutpostCoreMenu` 里 `new Slot(` = **54 + 36 次**」——54 + 36 = 90 是**槽位数**，不是调用数；实测 `new Slot(` 只有 3 处（都在循环里）。槽位总数 90 与 42 号一致，只有「次」这个量词错。
- **落地位置**：**F-256** 的验收 4、**F-264** 的 `#13`。

### 5.9 A-78 · 两处计数按实测校正：Java 文件数 **149**、构造器 register **11** 个

- **结论**：① 本版新增 **4 个类**（`util/AreaBlockEntities`、`combat/MobAiSuspension`、`util/Hostility`、`registry/FarmstormAttachments`），文件数 `145 + 4 = **149**`；② `Farmstorm.java` 构造器现有 **11** 个 `.register(modBus)`（`damage_type` 是数据驱动、只有 3 个 `ResourceKey`，本来就没有 register），加附件后是 **12** 个。
- **依据**：① 42 号 §1.1 写「现状 145……新增 4 个类……改后 **149**」，而 42 号 §7 #1 写「Java 文件数 = **148**」，两处差 1；② 42 号 §3 #29 写「12 个 register 的 `.register(modBus)` 区」，实测构造函数体内只有 **11** 行 `.register(modBus)`（`:46,47,48,49,50,52,53,54,55,57,58`），12 类里的第 10 类（`damage_type`）没有注册调用。
- **落地位置**：**F-248**（145 → **146**，`AreaBlockEntities`）、**F-252**（146 → **148**，`MobAiSuspension` + `FarmstormAttachments`）、**F-257**（148 → **149**，`Hostility`）；**F-252** 的验收 2（register 11 → 12）、**F-264** 的 `#1`（149）。

### 5.10 A-79 · 「观测不到」不设上限常量（以 42 号 §2.3.4 为准）

- **结论**：`sweep` 读不到实体时**只丢内存两件、保留附件**，**不设**「观测不到多少刻后放弃」的上限常量。
- **依据**：42 号 §2.3.4 明写「因此**不需要**『观测不到多少刻后放弃』这类上限常量——内存可以随便丢，附件才是真相」；而 42 号 §6 的 RT-54 缓释 (c) 写「只删索引、保留附件，**1200 刻上限**（§2.3.4）」——§2.3.4 里并没有这个上限，两处自相矛盾。按 §2.3.4 的正文执行（上限会造出「到期放弃」的第三个语义，且落盘的原值本就不需要它）。
- **落地位置**：**F-253** 的验收 5（上限常量命中 0）、**F-264** 的 `#23` / `#24`。

### 5.11 A-80 · 区块卸载的清理时点以 42 号 §2.3.3 路径⑤为准（不采用 41 号 §4.5 / §5.5）

- **结论**：实体所在区块卸载时**不**「按原值恢复 + 删记录」（那时实体不在世界里，`setNoAi` 没有对象可写），而是**保留附件、载入时结算**（震动豆那一路一律按已到期处理，Q弹 那一路按效果是否还在判）。
- **依据**：41 号 §4.5 与 §5.5 都把「区块卸载」写成「先按原值恢复、再删记录（第 5 条清理路径）」；42 号 §2.3.3 路径⑤ 明写「卸载时**不能**恢复……所以本版的处理是『**载入时结算**』」，RT-55 缓释 (c) 也写「卸载**不删附件**」。两处相反，按 42 号（它是技术面，且给出了「为什么不能在卸载时写」的理由）。41 号的意图（「不得把 `noAi` 留在存档里」）由「载入时结算」满足，不丢。
- **落地位置**：**F-253**（`adopt` 的三条判据）、**F-264** 的 `#23` / `#24`、**F-265** 的 B-27 / B-30。

### 5.12 A-81 · 40 号 D-179 的两处依据需要回改：`setNoAi` 实为 2 处、原值表已改为**落盘**

- **结论**：① 全仓 `setNoAi` 的调用是 **2 处**（`WildHunterAura:92` / `:119`），`BRIEF` §1.5 与 40 号 D-179 依据里写的「3 处（`:91` / `:92` / `:119`）」把 `isNoAi()`（`:91`）算了进去；② AI 原值**不是内存表**——裁定 #15 定为**落盘**（42 号 §2.3.1 的 `AttachmentType<Suspend>`），40 号 D-179 正文与 §0.2 术语表里的「表是**内存表**：不落盘、不进 `getUpdateTag`」已被裁定 #15 取代。
- **依据**：源码 `WildHunterAura:87-95` / `:98-123`（`:91` 是 `isNoAi()`、`:92` 与 `:119` 才是 `setNoAi`，实测 grep 命中 2）；`BRIEF` §2 裁定 #15（落盘 + 用后即删）；42 号 §2.3.1 的四候选比对（纯内存表被「崩溃不行」否决）。
- **落地位置**：**F-252 / F-253**（按落盘实现）、**F-264** 的 `#2` / `#23` / `#28`；回灌目标：40 号 D-179 与 §0.2 术语「原值」。

---

## 6. 覆盖对照表（六张）

表 A ~ 表 F 对应 42 号 §3 / §1.2 / §4 / §4.3 / §6 / §8。每张表末行给「覆盖数 / 总数」。

### 表 A · 42 号 §3 的 31 行动作面 → 任务

| 行 | 类别 | 路径或 类#方法 | 现 → 改后 | 负责任务 |
|---|---|---|---|---|
| 1 | 发布 | `gradle.properties:11` | `0.2.0` → **`0.2.1`** | F-247 |
| 2 | 发布 | `一键构建.cmd:5` | 抬头 `Farmstorm v0.2.0` → `v0.2.1` | F-247 |
| 3 | 发布 | `一键构建.cmd:11,12` | jar / sources-jar 名 → `0.2.1`（两行） | F-247 |
| 4 | 发布 | `检查环境.cmd:5` | 抬头 `Farmstorm v0.2.0` → `v0.2.1` | F-247 |
| 5 | 发布 | `检查环境.cmd:14` | `farmstorm-0.2.0+mc1.21.1.jar` → `0.2.1` | F-247 |
| 6 | 类 | `OutpostCoreBlockEntity#serverTick`（`:507-525`） | 三件事 → **四件事**（补绑门在粒子门之前、不 return） | F-250 |
| 7 | 类 | `OutpostCoreBlockEntity` 常量区（紧邻 `:91`） | 无 → **`REBIND_INTERVAL_TICKS = 100`** | F-249 |
| 8 | 类 | `OutpostCoreBlockEntity#rebindDue(BlockPos, long)` | 不存在 → **新增**（模数 100 的独立相位） | F-249 |
| 9 | 类 | `OutpostCoreBlockEntity#rebindNearby(ServerLevel)` | 不存在 → **新增**（两阶段：收集 → 写绑定；`cap` 处 `break`） | F-249 |
| 10 | 类 | `util/AreaBlockEntities.java` | 不存在 → **新增**（`collect(…, Class<T>)`，只走 `getChunkNow`） | F-248 |
| 11 | 注释 | `block/CultivatorBlock.java:73-84`（那句在 `:81-83`） | 「不自动认领……不会绑上」→ **与周期补绑一致** | F-250 |
| 12 | 类 | `client/OutpostCoreScreen.java`（42 行） | 只覆写 `renderBg` → **一行不改**（判据见 42 号 §2.2） | F-256 |
| 13 | 验收 | 42 号 §7 | tooltip / 悬停 **0** 条 → **+3 条**（`#11` ~ `#13`） | F-256（复跑在 F-264） |
| 14 | 类 | `VibrationBeanTrap#trigger`（`:74-105`） | 只给球内玩家 `CONFUSION` → **追加**球内所有非玩家生物删 AI 80 刻 | F-254 |
| 15 | 类 | `VibrationBeanTrap` 常量区 | 无 → **`AI_SUSPEND_TICKS = 80`**（不新增 config 键） | F-254 |
| 16 | 类 | `effect/QbounceEffect.java`（20 行、0 覆写） | 纯状态载体 → **覆写 `onEffectAdded` 一个方法**（不覆写 `applyEffectTick`） | F-255 |
| 17 | 类 | `behavior/QbounceMountController#isMountMode`（`:199`） | `private static` → **`public static`**（不新增第二个方法） | F-251 |
| 18 | 类 | `combat/MobAiSuspension.java` | 不存在 → **新增**（`sources` 位掩码 + 观测索引 + 内存到期刻 + 五条清理路径 + 每刻对账） | F-253（`Suspend` 在 F-252） |
| 19 | 类 | `registry/FarmstormAttachments.java` | 不存在 → **新增**（`AttachmentType<Suspend>`，`farmstorm:ai_suspend`，注在 `ATTACHMENT_TYPES`） | F-252 |
| 20 | 类 | `util/Hostility.java` | 不存在 → **新增**（`isHostile(Entity)`，`Enemy` ∪ `MONSTER`） | F-257 |
| 21 | 类 | `explosion/FarmstormExplosion` 构造器（`:69-78`） | 8 参 → **9 参**（+`boolean trapSourced`）+ **保留 8 参重载委托 `false`** | F-259 |
| 22 | 类 | `FarmstormExplosion#hurtCapped`（`:178-179`） | `hurt(src, min(raw, cap))` → 敌对 ∧ `trapSourced` 时 × 倍率 | F-259 |
| 23 | 类 | `ZangiefCaneTrap#monsterBranch`（`:111-118`、日志 `:126-128`） | `hurt(src, amount)`、日志打原值 → 敌对时 × 倍率、**日志打乘后值** | F-260 |
| 24 | 配置 | `config/FarmstormConfig.java`（`trap` 段 `:917-927`） | 3 键 / **175** → **4 键 / 176**（+`damageMultiplier` 3.0、1.0 ~ 10.0） | F-258（总账 F-262） |
| 25 | 语言 | `datagen/ModLangProvider.java`（`:412-413` 之后） | 178 条 / **337** → **+1 条 / 338** | F-258（总账 F-263） |
| 26 | 工具 | `tools/verify_project.py`（`main()` `:349-402`） | 无 42 号解析 → **新增 `expected_config_keys_v021`** + 期望 176 | F-261 |
| 27 | 不改 | `Farmstorm.java:167-172`（12 类断言）/ `FarmstormNetwork`（2 包、协议 `"2"`） | 原样 | F-266（负向核对） |
| 28 | 不改 | `FangTrapCactusTrap:87-109`（原版 `EvokerFangs`）/ `vibration_bean`（0 伤害）/ 5 处 `Monster` 过滤 | 原样 | F-266（负向核对） |
| 29 | 类 | `Farmstorm.java` 构造器（**11** 个 `.register(modBus)` 的区，`:45-58`） | 11 个 register → **+1 行**（`FarmstormAttachments`）；`:169-172` 一行不动 | F-252 |
| 30 | 类 | `#rebindNearby` 的前置闸与排序 | 不存在 → **无主核闸**（裁定 #13）+ **三级排序** `distSqr` → `x` → `y` → `z`（裁定 #14） | F-249 |
| 31 | 数据 | 震动豆 80 刻的到期刻 | 无 → **只存内存**（`VB_DEADLINE`）、**不落盘**（裁定 #16） | F-253 |

**31/31 覆盖，未覆盖 0**。分类计数：发布 **5** 行（#1 ~ #5）、类 **18** 行（#6 ~ #10、#12、#14 ~ #23、#29、#30）、注释 **1** 行（#11）、验收 **1** 行（#13）、数据 **1** 行（#31）、配置 **1** 行（#24）、语言 **1** 行（#25）、工具 **1** 行（#26）、不改 **2** 行（#27、#28）—— 与 42 号 §3 表下的分类计数（发布 5 / 类 18 / 注释 1 / 验收 1 / 数据 1 / 配置 1 / 语言 1 / 工具 1 / 不改 2 = 31）逐项相同。

**表下注**：**表外动作 2 项**（不计入 31 行）——① `tools/verify_project.py` 的**期望数**同步（F-261 的 `union` 与 175 → 176，42 号 §3 #26 只登记了「新增解析器」，期望数在同一行里）；② 两个总账（F-262 / F-263，42 号 §4.2 / §4.3）。两项都有独立验收，不把表 A 撑到 33 行。**一行多任务的行**只有 #18（`MobAiSuspension` 的数据类在 F-252、行为在 F-253）与 #24 / #25（键与翻译同提交、总账各自独立）——每一段都有独立验收，回滚其中任意一个任务不影响其余段。

### 表 B · 注册面 **97（+0）** + 口径外第 13 类 → 任务

**B.1 12 类逐类**（基线 97，42 号 §1.2 自己从源码逐类点名）

| # | registry | 基线 | 本版增量 | 改后 | 落点任务 | 核对任务 |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 33 | **+0** | **33** | — | F-266 |
| 2 | `Registries.BLOCK` | 27 | **+0** | **27** | — | F-266 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 8 | **+0** | **8** | — | F-266 |
| 4 | `Registries.ENTITY_TYPE` | 13 | **+0** | **13** | — | F-266 |
| 5 | `Registries.MOB_EFFECT` | 4 | **+0** | **4** | — | F-266 |
| 6 | `Registries.SOUND_EVENT` | 3 | **+0** | **3** | — | F-266 |
| 7 | `Registries.RECIPE_TYPE` | 1 | **+0** | **1** | — | F-266 |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | **+0** | **1** | — | F-266 |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | **+0** | **1** | — | F-266 |
| 10 | `Registries.DAMAGE_TYPE` | 3 | **+0**（不新开 `trap_blast`，42 号 §2.5.2 否决） | **3** | — | F-266 |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | **+0**（AI 原值走附件，不是数据组件，D-175 / 裁定 #17） | **1** | — | F-266 |
| 12 | `Registries.MENU` | 2 | **+0** | **2** | — | F-266 |
| **合计** | | **97** | **+0** | **97** | | `33+27+8+13+4+3+1+1+1+3+1+2 = 97` |

**B.2 口径外的第 13 类（1 行，不计入 97）**

| # | registry | 基线 | 本版增量 | 改后 | 落点任务 | 核对任务 |
|---|---|---|---|---|---|---|
| 13 | `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` | 0 | **+1**（`farmstorm:ai_suspend`） | **1**（**口径外**，不计入 97） | F-252 | F-266（人工登记；`verify_project.py` **既不核存在也不核计数**，42 号 §1.2.1） |

**B.3 四个「加了但不进注册面」的构件**

| # | 构件 | 为什么不进 registry | 任务 |
|---|---|---|---|
| 1 | `util/AreaBlockEntities` | 纯静态工具类，0 个注册调用 | F-248 |
| 2 | `util/Hostility` | 纯静态谓词，0 个注册调用 | F-257 |
| 3 | `combat/MobAiSuspension` + `Suspend` | 只持有两个内存容器 + 事件订阅；`Suspend` 是附件的值类型，都不注册 | F-252、F-253 |
| 4 | `trap.damageMultiplier` | config 键，不注册（D-183） | F-258 |

**覆盖：12 类 + 1 个口径外类 + 4 个不进注册面的构件 = 17 项，未覆盖 0**（合计行 97 不变）。

### 表 C · config **176**（+1）→ 任务

| # | 键 | 类型 | 默认 | 值域 | 动作 | 落键任务 | 中文键任务 |
|---|---|---|---|---|---|---|---|
| 1 | `trap.damageMultiplier` | double | **3.0** | **1.0–10.0** | **增** | F-258 | F-258 |
| **合计** | **1 行 = 增 1 / 删 0 / 改 0** | | | | | **`175 + 1 = 176`** | 1 条语言键 |

**两处核对**：① `trap` 组 **3 → 4** 键，组数仍 **60**（`b.push` / `b.pop` 各 60 次，没有新组）；② 中间态键数（每段都能从 `run/config/farmstorm-server.toml` 数出来）——F-257 之前 **175 / 60** → F-258 之后 **176 / 60**（区间读数允许写「175 或 176」，总数只在 F-262 断言）；CLIENT 3 键（`hud.enabled` / `hud.x` / `hud.y`）**不动、不计入** 176，但计入语言键的 `farmstorm.configuration.*`（42 号 §4.2 / §4.3）。

**不改的三件事**（42 号 §4.1.1）：`blast_carrot.maxDamage`（7）、`zangief_cane.slamDamage`（7.5）、`trap` 段既有 3 键的默认值——倍率是**新键**、在结算处乘（裁定 #10）。

### 表 D · 语言键 **338**（+1）→ 任务

| # | 键 | 语言 | 动作 | 落点任务 |
|---|---|---|---|---|
| 1 | `farmstorm.configuration.trap.damageMultiplier` | zh_cn | **增** | F-258 |
| 2 | `farmstorm.configuration.trap.damageMultiplier` | en_us | **增** | F-258 |
| **合计** | **1 个键 × 2 语言** | | **`337 + 1 = 338`（× 2）** | 总账在 F-263 |

**逐前缀校验**（改后，42 号 §4.3 的核对口径）：`farmstorm.configuration 178 → 179`（= 176 server + 3 client）、`tooltip.farmstorm` 仍 **20**、其余 13 个桶不变；`179 + 36 + 33 + 27 + 20 + 13 + 9 + 6 + 4 + 3 + 3 + 0 + 2 + 1 + 2 = 338`。**中英同键同数**（zh-only 0 / en-only 0）；**语言键没有自动闸**，只能人工 `ConvertFrom-Json` 数（42 号 §9 第 4 条）。

### 表 E · 风险 **11 条**（RT-51 ~ RT-60 + RT-60b）→ 任务

编号顺序照 42 号 §6：**RT-51 ~ RT-60 十条连续**，最后一行是 **RT-60 的子条 `RT-60b`**（同一机制的第二个入口，42 号 §9 第 5 条：不新开 RT-61）。

| 编号 | 风险（42 号 §6） | 负责验证的任务 | 该任务怎么验 |
|---|---|---|---|
| **RT-51** | 周期补绑与放置时绑定并存 ⇒ 同一培养器可能被两个核同时认领 | F-249、F-250、F-264 | F-249 验「先核后培」的写入顺序 + 不调 `removeCultivator`；F-250 验注释同改 + 100 刻读数；F-264 跑 `#6` ~ `#10` |
| **RT-52** | 区域枚举无先例：`getBlockEntities()` 的返回值语义、未加载区块必须跳过 | F-248、F-264 | F-248 验只走 `getChunkNow`（三条会加载的 API 命中 0）+ 返回新列表；F-264 的 `#9` 验未加载区块被跳过且日志无该区块的加载行 |
| **RT-53** | 一核 16 个上限：满之后必须停；`pruneCultivators` 与补绑同时跑会「刚绑上又被摘」 | F-249、F-264 | F-249 验 `cap` 处 `break` + 容量读数不走 `cultivators()`；F-264 的 `#10` 验 20 个候选只绑 16 且不再增长 |
| **RT-54** | AI 原值**落盘后的残留风险**：五条清理路径漏一条就留过期记录 | F-252、F-253、F-264 | F-252 验 `sources == 0` 不落盘 + 无时间戳字段；F-253 验 `release` 末尾必 `removeData`；F-264 的 `#23` / `#24` / `#28` |
| **RT-55** | Q弹 效果存续期：到期 / `/effect clear` / 牛奶 / 死亡 / 卸载 五条路径都必须清理 | F-253、F-255、F-264 | F-253 验对账是唯一权威（0 个效果事件订阅）；F-255 验三条移除路径 **1 刻内**恢复；F-264 的 `#17` / `#18` |
| **RT-56** | Q弹 的效果是「反复施加、时长递减」，持续期不能配一次性计时器 | F-255、F-264 | F-255 验进入面只挂 `onEffectAdded`（只在「此前无同一效果」分支被调）+ `sources` 位不重复置位；F-264 的 `#16` |
| **RT-57** | ×3 只作用于敌对：不能顺手改到别的路径（尤其 5 处触发面） | F-257、F-259、F-260、F-264 | F-257 验 5 处 `Monster` 过滤逐字未变；F-259 / F-260 验乘点只在 2 处；F-264 的 `#19` / `#20` / `#27` |
| **RT-58** | 尖牙不参与：验收照「所有陷阱」写必然误判 | F-259、F-260、F-264 | 两个乘点都不碰 `EvokerFangs`；F-264 的 `#21` 验尖牙逐点不变，且记录里写明「4 个里 2 个参与」 |
| **RT-59** | tooltip 可能本来就是好的：任务会变成「加验收」，容易写成「改了一堆」 | F-256、F-264 | F-256 验 4 项覆写 = 0 + 90 个槽位全原版 + 退路 ≤ 2 行；F-264 的 `#11` ~ `#13` |
| **RT-60** | `verify_project.py` 的期望面（A-66）：175 → 176 不同步会**静默少核** | F-261、F-262、F-266 | F-261 验解析器返回 1 键 + 期望 176；F-262 双向核（源码 176 / toml 176 / 工具并集 176）；F-266 签字引用工具三段输出 |
| **RT-60b** | `trap.damageMultiplier` 必须**真的写进文档**（期望键集从文档抽取） | F-261、F-264 | F-261 验 `grep -c` ≥ 2 且 §4.1.1 被正则命中 1 行；F-264 的 `#22` |
| **合计** | **11 条** | | **11/11，未覆盖 0** |

### 表 F · 待验证 API **10 条** → 任务

| # | 待验证的 API / 语义（42 号 §8） | 由哪个任务在哪一步确认 | 确认不了时的退路（42 号 §8 已给） |
|---|---|---|---|
| ① | `LevelChunk#getBlockEntities()` 的返回语义与未加载判定 | **F-248**：`collect` 落地后跑 F-250 的 `#6` ~ `#10`；CME 是否出现由 `#9`（未加载跳过）与 `#10`（20 个候选）两处读数覆盖 | 若真出 CME ⇒ 在 `collect` 里显式 `new ArrayList<>(chunk.getBlockEntities().values())` 再过滤，并加一层 `try/catch` |
| ② | `Mob#setNoAi` 与「原值」怎么读回（`NoAI:0b` 与「缺键」的区别） | **F-253** 的 `#1` / `#2`：`/data get entity <mob> NoAI` 的三种读数（缺键 / `0b` / `1b`） | 读数改成「有键且为 1b」= 被删、「无键」= 有 AI（与 `Mob.java:437` 只在 true 时写一致） |
| ③ | `MobEffect#onEffectAdded` / `onEffectRemoved` 在 21.1.213 的可用性 | **F-255** 的 `#1`：先只给一只怪施加一次效果，看附件是否只写一次；「无 `onEffectRemoved`」已在 42 号 §8 ③ 核过 | 若 `onEffectAdded` 在某条路径不触发 ⇒ 补挂 `MobEffectEvent.Added`（带 `getOldEffectInstance() == null` 过滤），`sources` 位幂等不会重复 |
| ④ | `Enemy` 接口与 `MobCategory.MONSTER` 的取值方式 | **F-257** 的源码 4 分支 + **F-259** 的 `#3` / `#4` 运行时读数 | 若实测漏判某个模组生物 ⇒ **只登记**（40 号 §7 ③ 已定「不扩判据」），不加白名单 |
| ⑤ | `verify_project.py` 从文档抽取键集的具体函数 | **F-261** 的 `#1` ~ `#3`：新解析器返回 1 键、两行 176 | 若表格格式被改坏 ⇒ 解析器按 `:172-178` 的形态**带文件名 + 行号抛出**（宁可红） |
| ⑥ | `EntityJoinLevelEvent` 的触发面（载入时结算） | **F-253** 的 `#4`：强杀进程后重启，看附件是否被结算并删除 | 起服**有界补扫**：照 `QbounceMountController:63-70` / `:190-194` 的 `ADOPT_RESCAN_INTERVAL_TICKS = 20` / `MAX_ADOPT_RESCANS = 10` 做 10 轮（10 秒后才加载的区块是已知残余，登记为 RT-54） |
| ⑦ | `EventPriority.HIGHEST` 在 `ServerTickEvent.Post` 上的排序 | **F-253** 的 `#5` 前 + **F-255** 的 `#3`：mount 态下恢复必须先于挂载 | 改挂 `ServerTickEvent.Pre`（事件阶段天然早于所有 `Post` 订阅者），代价是位移损失 1 刻 |
| ⑧ | `AttachmentType` 的落盘 / 读回时点与体积 | **F-252** 的 `#1` / `#4` + **F-253** 的 `#1` / `#3` | 若 `shouldSerialize` 仍写出空 compound ⇒「存档里零字节」改为「≤ 40 字节」，结论不变 |
| ⑨ | `ArmorStand` 坐骑与 `noAi` 的交互（震动豆 × mount 同刻） | **F-254** 的 `#1` 与 **F-255** 的 `#3` **分开测、不合并**（42 号 §2.3.7） | 若实测出现「坐骑被摘 / 实体被弹下车」⇒ 按 RT-55 登记，**不改**裁定 #4 的生效面 |
| ⑩ | `getData` / `setData` / `removeData` / `hasData` 的默认值语义 | **F-253** 的 `#1`（`removeData` 之后 `getData` 回落到 `NONE`）+ `#5`（存在判据用 `sources != 0`） | 若 `getData` 会把默认值写进 map ⇒ 不影响本版（判据是 `sources != 0`）；若 `removeData` 后不回落 ⇒ 给默认值提供者加一层 `sources == 0 ? NONE : 已存值` 的兜底 |
| **合计** | **10 条** | **10/10 落到具体任务与具体步骤** | **10/10 给出可执行退路** |

---

## 7. 本版自检（这个文档自身的验收）

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 任务编号连续、无跳号 | `grep -c "^#### F-"` = **20**；区间 `F-247 ~ F-266` ⇒ `266 − 247 + 1 = 20` | 通过 |
| 2 | Phase 与里程碑连续 | Phase `40 ~ 45` 共 **6** 个；里程碑 `M36 ~ M41` 共 **6** 个，一一对应（§4.1 表） | 通过 |
| 3 | 裁决编号 | A-70 ~ A-81 共 **12** 条，连续无跳号；A-70 是第一条且排在 §5.1 | 通过 |
| 4 | 表 A 覆盖 | 42 号 §3 的 **31 行一行不漏**，未覆盖 **0**；分类计数（发布 5 / 类 18 / 注释 1 / 验收 1 / 数据 1 / 配置 1 / 语言 1 / 工具 1 / 不改 2）与 42 号 §3 表下计数逐项相同 | **31/31** |
| 5 | 表 B 覆盖 | 12 类逐类（**97 = 97 + 0**，合计行算式 `33+27+8+13+4+3+1+1+1+3+1+2 = 97`）；**口径外第 13 类 1 行**（`ATTACHMENT_TYPES` +1，不计入 97，工具不核）；4 个不进注册面的构件各有任务 | **17 项**（12 类 + 1 口径外 + 4 构件） |
| 6 | 表 C 与表 D 覆盖 | 表 C **1 行**（增 1 / 删 0 / 改 0，`175 + 1 = 176`，组数 `60` 不变）；表 D **2 行**（zh_cn / en_us 各 1，`337 + 1 = 338` × 2，逐前缀相加 = 338）；两表的中间态与窗口纪律自洽（§0.3） | **1 行 + 2 行** |
| 7 | 表 E 覆盖 | **11 条**：RT-51 ~ RT-60 十条 + **RT-60b** 一条子条（最后一行）；每条至少一个任务负责验证且给了「怎么验」 | **11/11** |
| 8 | 表 F 覆盖 | 42 号 §8 的 **10** 条，每条落到「具体任务的具体步骤」+「具体退路」，无「实现时注意」 | **10/10** |
| 9 | 每个任务 ≥ 1 条可观察验收 | 20 个任务逐条读过：全部有落在 `latest.log` / `farmstorm-server.toml` / `lang/*.json` / `/data get` / 源码 grep 计数 / 游戏内悬停上的断言；无「表现正常」「手感良好」 | 通过 |
| 10 | 每个任务有依赖、依赖无环 | §1 表「依赖」列 + 任务标题行 `依赖`，20 / 20 一致；**无前驱（`—`）7 个**：F-247、F-248、F-251、F-252、F-256、F-257、F-258；每个依赖编号都小于自身编号，按 Phase 排序即拓扑序，无回边 | 通过 |
| 11 | 工时自洽 | `7 × 0.5 + 10 × 1.5 + 3 × 3.0 = 27.5`；六个 Phase 小计相加 = 27.5；`27.5 × 1.25 = 34.375`；`S + M + L = 7 + 10 + 3 = 20` | 通过 |
| 12 | 中间态窗口自洽 | 窗口 = **F-258（开）→ F-263（关）**，config 关于 **F-262**、语言关于 **F-263**、工具期望面关于 **F-261**；F-258 ~ F-262 的五个任务验收里**都不含**「176 / 60」与「338 × 2」；两个总数各只在一个关窗任务里断言一次 | 通过 |
| 13 | 两个「从零写」的构件各自独立成任务 | `util/AreaBlockEntities#collect`（F-248）与 `util/Hostility#isHostile`（F-257）都是独立任务、各有可单独执行的验收（前者 = 编译 + 四条结构读数；后者 = 4 分支 + 5 处过滤未变 + `MobCategory` 命中 2 → 3）；两者的运行时读数分别由 F-250 / F-259 承接 | 通过 |
| 14 | 第 9 参与 8 参重载的验收核心 | F-259 的验收 1（7 处调用、只有 1 处传第 9 参）与验收 4（倍率 1.0 / 10.0 下 6 处读数不变），以及 F-264 的 `#20`，三处共同证明「6 处非陷阱调用零改动、没被 ×3」 | 通过 |
| 15 | 第 13 类的三处登记 | F-252 的验收 5（工具不核它）+ F-266 的签字 3 + 42 号 §1.2.1；三处都写明「不计入 97」 | 通过 |
| 16 | 「不许发明」 | 97 / 176 / 338 / 31 行 / 31 条 / 10 条 / 11 条全部可追到 42 号 §1.2、§3、§4、§6、§7、§8 或它们的算术（`175 + 1`、`337 + 1`、`97 + 0`、`145 + 4`、`7.5 × 3`）；新增的只有 12 条裁决与任务编号，无新造玩法数值 | 通过 |
| 17 | 禁用词、语气与改动面 | 全文无未定事项标记与占位符；人称一律用「作者」「实现者」，不用第二人称敬语；结论句一律祈使；本文之外不改任何文件（`docs/zh/40` ~ `42`、`docs/en`、`src/`、`tools/`、`.work/` 一字未改） | 通过 |
