# 63 号 v0.2.4 分阶段投喂清单

> 投喂方式：**62 号（主提示词）先投一次、常驻**；此后每个 Phase 开工时**只投本文对应的那一节**，同一时间只投一节，**不跳节**。
> 依据：`docs/zh/61-TASKS-0.2.4.md`（下称 61 号，F-312 ~ F-327 + F-328 / Phase 66 ~ 72 / M62 ~ M68 / A-102 ~ A-111 的唯一来源）+ `docs/zh/60-TECH-0.2.4.md`（下称 60 号，§4 实现规格、§8 的 **25** 条验收、§9 的 RT-83 ~ RT-91）+ `docs/zh/59-DESIGN-0.2.4.md`（下称 59 号，R1 ~ R4、B-59 ~ B-73、§7 的已知影响 4 条）+ `docs/zh/58-DECISIONS-0.2.4.md`（下称 58 号，D-225 ~ D-252，唯一真相源）。
> 只引上游：本文不改 58 ~ 62 号、不改 `.work/`、不改 `src/`、不改 `tools/`；只新建本文件。

| 项 | 值 |
|---|---|
| 文档编号 | 63-PROMPT-TASKS |
| 版本 | v0.2.4（MC 1.21.1 / NeoForge 21.1.213 / Java 21 / modid `farmstorm`） |
| 读者 | 投喂编码代理的人 + 被投喂的 CLI 编码代理 |
| 上游分工 | 61 号给任务号、名称、复杂度、依赖与 Phase 归属；60 号给实现规格、25 条验收与 9 条风险；59 号给边界与已知影响；58 号给决策号 |
| 内容 | **7 个 Phase / 17 个任务（F-312 ~ F-327 共 16 条 + F-328 1 条）**；每节固定六小节：要做什么 / 前置条件 / 任务清单 / 出口判据 / 常见翻车点 / 必须回报 |
| 出口判据 | 7 节合计 **49** 条（`7 + 8 + 8 + 7 + 5 + 7 + 7 = 49`），编号 `E-66-1` ~ `E-72-7`，全局唯一、可 `grep` |
| 编号边界 | 本文件**只发** E 系列；D / B / F / Phase / M / HC / N / RT / A 一律写全前缀、只引用 |
| 中间态窗口 | **无**（61 号 §0.3 判定）：本版没有期望红的断言，任何一个中途 FAIL 都是真 bug |
| 基线 | v0.2.3 已实现态，git `52749b9`（该提交上 `git status --porcelain --untracked-files=no` 输出 **0** 行）；写作期已落地 F-328，工作区因此多出 **5** 个已跟踪路径（`D` 2 + `M` 3，见 §1.2 的 66 行） |

---

## 0. 投喂顺序与固定口径

### 0.1 投喂顺序

**先投 62 号一次**（管红线 HC-173 起、禁止行为 N-155 起、提交纪律与禁用词，全程常驻）；**此后每个 Phase 开工时只投本文对应的那一节**——投一节、做一节、按该节的「完成后必须回报什么」交一份回报，再投下一节。

投喂前先确认两支提示词都在仓库里：`Test-Path docs/zh/62-PROMPT-MAIN-0.2.4.md` 与 `Test-Path docs/zh/63-PROMPT-TASKS-0.2.4.md` 都为 `True`。62 号缺失时先补 62 号，本文不替代它。

四条读法：

1. **一次只投一节**。每节自带前置条件与出口判据，单节脱离其它节也能执行；同时投两节会让两节的「前置条件」互相覆盖，实现代理会按最后读到的那份干活。
2. **节序单调**：66 → 67 → 68 → 69 → 70 → 71 → 72，不得调换。节内还有 61 号 §5.3 的串行段（`F-313 → F-314 → F-317`、`F-318 → {F-319, F-320, F-321}`、`F-326 → F-327`）。
3. **不跳节**。每一节的「前置条件」都至少有一条读数来自上一节的出口判据；跳过任一节，那条读数就是「未发生」，且它不会自己变绿。
4. **并行不靠跳节**。并行落点按 61 号 §5.2 的四簇（A：`F-312` / `F-313` / `F-316`；B：`F-318` / `F-322` / `F-323` / `F-325`；C：`F-324`；D：`F-322` / `F-323`），并行度上限 **3**；并行时**一节仍只投一次**，两节分给两个人，且同一文件只被一个任务改。

### 0.2 60 号 §8 的 25 条验收 → Phase（无孤儿）

| 60 号 §8 条目 | 落点任务 | 归档 Phase | 哪条判据给读数 |
|---|---|---|---|
| `#4`、`#5` | F-313、F-314 | 67 | E-67-2、E-67-4 |
| `#3` | F-313（落点）+ F-326（复核） | 67 / 72 | E-67-1、E-72-3 |
| `#6`、`#7`、`#8`、`#9`、`#10`、`#11`、`#12` | F-317 | 68 | E-68-2 ~ E-68-8 |
| `#13`、`#14`、`#15`、`#16`、`#18` | F-318 ~ F-321 | 69 | E-69-1 ~ E-69-6 |
| `#17` | F-326（落点是 F-318 ~ F-321） | 69 / 72 | E-69-7、E-72-5 |
| `#19`、`#20` | F-322 | 70 | E-70-1、E-70-2 |
| `#21`、`#25` | F-324、F-325 | 71 | E-71-1 ~ E-71-3、E-71-6 |
| `#1`、`#2`、`#22`、`#23`、`#24` | F-326 | 72 | E-72-2 ~ E-72-4 |
| **合计** | | **7 节** | `2 + 1 + 7 + 5 + 1 + 2 + 2 + 5 = 25`，未覆盖 **0** |

- Phase 66 不承接任何 §8 条目：它的 7 条判据来自 61 号 §2 的 F-312 与 F-328 自身断言；`#1` / `#2` / `#22` 的读数都要等 jar 名成立之后才可能读。
- `#3` 与 `#17` 各给两节：一节落点、一节终态复核，计数只算一次，故合计仍是 **25**。
- 每条的「怎么做 / 期望看到什么」以 60 号 §8 表为准；本文只补「在哪个 Phase 读、哪条判据给读数」。

---

## 1. 窗口台账（本版：**无窗口**）

### 1.1 五条读数与判定

| # | 观察面 | 本版读数 | 出处（实读） |
|---|---|---|---|
| 1 | Java 文件数 | `152 − 2 + 1 = 151`（`152` = v0.2.3 记录基线；`−2` = F-328 删掉 `FarmlandGuard.java`（225 行）与 `FarmlandLedger.java`（555 行）；`+1` = 新建 `blockentity/CoreBound.java`，F-313），当前实点读数 **150** | 61 号 §0.3 第 1 行；`Test-Path src/main/java/com/dashpig/farmstorm/blockentity/CoreBound.java` 现为 `False` |
| 2 | 检查器是否读文件数 | **不读**：`verify_project.py:480` 只把源码目录下的 `*.java` 拼成一个字符串做内容匹配（`all_java = "\n".join(read(f) for f in java.rglob("*.java"))`），全文无计数断言；`:613-622` 数的是 `EXPECTED` 的 ID 清单（`total += len(ids)`）；`:637-653` 按文件扫六串禁令 | `tools/verify_project.py:479-480`、`:610`、`:613-622`、`:637-653` |
| 3 | 运行时注册自检 | 不读文件数：断言 `total == 92` 与十二类逐类相等，日志末段打 `OK` / `FAIL` | `Farmstorm.java:180-183`、`:184-188` |
| 4 | 十一项冻结面 | 注册面 **92**、server config **175**、client config **3**、语言键 **321 × 2**、`configuration` 组 **178**、网络包 **2**、进度 **16**、转换配方 **21**、培养器投入物 **17**、交易 listing **26**、Mixin **0** —— 全部 `− 0 + 0`，删除项 **0** | 58 号 D-227 全表；60 号 §1 ~ §7 |
| 5 | xp 分布 | 26 条 listing 会经历半改态（F-320 与 F-321 不是同一个提交），但**没有任何闸读 xp**：`verify_project.py` 里 `VILLAGER_XP` / `XP_LEVEL` / `MerchantOffer` 命中 **0**；`Farmstorm.java:180-183` 不碰交易 | 61 号 §0.3 第 5 行；60 号 §8 `#17` |

**判定：本版无窗口**（61 号 §0.3、A-102）。窗口成立要有两个观察面同时被改；本版被改的数字只有文件数一项（`152 − 2 + 1 = 151`），而它没有任何断言在读；F-328 的删除不改变十二项冻结面的任何一个读数，其余十一项 `− 0 + 0`，xp 半改态不进任何闸。

**因此本版没有期望红的断言，任何一个中途 FAIL 都是真 bug**：按缺陷处理，不许当窗口内的期望红，也不许为「让它绿」提前落地后续任务。

### 1.2 台账（Phase → F 号范围 → 任务数 → 前置 Phase → 窗口段 → 可断言 / 禁写）

| Phase | F 号范围 | 任务数 | 前置 Phase | 窗口段 | 可断言 / 禁写 |
|---|---|---|---|---|---|
| 66 | F-312 + F-328 | 2 | —（首节） | 窗口外（本版无窗口） | 可断言 `0.2.4` 三文件命中 6、jar 名、**92** / **175 + 3** / **321 × 2**（全程相等）、`farmland` 命中 **2**（F-328 已落地）；禁写 **151**（F-313 未落地） |
| 67 | F-313 ~ F-315 | 3 | —（节内 F-313 先行） | 窗口外 | 可断言 **151**、`implements CoreBound` 2、`private void syncCore` 1；禁写 `KIND_BY_BLOCK`、`0x4D000000` |
| 68 | F-316 ~ F-317 | 2 | 67（E-67-1、E-67-3、E-67-5、E-67-7） | 窗口外 | 可断言 `AreaBlockEntities.collect` 1、`KIND_BY_BLOCK` 1、`clearCore()` 调用点 1；禁写 `0x4D000000`、`XP_LEVEL_5` |
| 69 | F-318 ~ F-321 | 4 | —（节内 F-318 先行；F-319 先于 F-321） | 窗口外 | 可断言 `XP_LEVEL_1 = 2` ~ `XP_LEVEL_5 = 30`、`new BotanistTrade(` 22、`xp:30` 2；禁写 alpha 77、三处客户端分支 |
| 70 | F-322 ~ F-323 | 2 | —（两任务无前驱） | 窗口外 | 可断言 `0x4D000000` 1 / `0x80000000` 0、内层 alpha 77、`裁定 #5` 命中 0；禁写三处客户端分支读数 |
| 71 | F-324 ~ F-325 | 2 | 67（F-324 依赖 E-67-5 的 `public convertible`） | 窗口外 | 可断言 `instanceof BlockItem` 1、`block.anvil.place` 两侧各 1；禁写 `#22` 的 **14** 个路径（要等全部落地）、README 三段 |
| 72 | F-326 ~ F-327 | 2 | 66 ~ 71（全部出口判据） | 窗口外 | 可断言 25 条全部读数、**14** 个 `.java` 路径、`[段]` 60 / 键 175；禁写 `src/main/resources` 与 `src/generated/resources` 下的任何路径 |
| **合计** | **F-312 ~ F-327 + F-328** | **17** | | | `2 + 3 + 2 + 4 + 2 + 2 + 2 = 17`（`16 = 327 − 312 + 1`，再加 F-328 一条） |

- **「禁写」不是禁令、是时序**：某一节的禁写项只是在那一节还读不到；到它归属的那一节，它立刻变成必写项。
- **窗口不存在，故没有「窗口内冻结」的行**：七行的「窗口段」全部写「窗口外（本版无窗口）」，依据是 §1.1 的五条读数。

---

## 2. 跨 Phase 台账

每一行是**一个动作横跨两个以上 Phase**：起点在哪、终点在哪、谁先谁后、违反后当场看到什么。

| # | 横跨的动作 | 起点（Phase / 任务） | 终点（Phase / 任务） | 谁先谁后 | 违反会怎样 |
|---|---|---|---|---|---|
| 1 | 需求 1 的六个面（发现 / 缓存判定 / 绑定写入 / 迁移改绑 / 清理出口 / `KIND_BY_BLOCK`） | 67 / F-313、F-314、F-315（接口与两个实现） | 68 / F-317（五处 + 映射，一个 L 任务） | 先契约、再收集面（F-316）、最后核侧五处 | 在 68 节内把六面拆成两个提交 ⇒ 中间态正好是被否决的半个修复：只改清理时转换器仍停在死坐标（`OutpostCoreBlockEntity.java:630-632` 对非空缓存一律跳过），只改发现时 D → A 那条迁移根本不发生（61 号 A-103） |
| 2 | 音效三处（两台机器 + 据点核心） | 71 / F-324 | 71 / F-325 | F-324 先（要等 67 节的 `public convertible`），F-325 可同批但不同任务 | 只落 F-324 ⇒ 两台机器听得见、核心听不见，同一版本内同类现象时有时无（58 号 D-249、61 号 A-106）；Phase 71 出口要求 E-71-1 与 E-71-3 同时成立 |
| 3 | 客户端谓词 `convertible` 提权 | 67 / F-314（`private` → `public`） | 71 / F-324（`block/` 包调用它） | 提权必须在 F-324 之前落地，且两者不在同一提交 | `convertible has private access`，编译不过（61 号 A-104） |
| 4 | 25 条验收的终态读数 | 66 / F-312（jar 名是 `#2`、`#22` 的前提） | 72 / F-326 | 全部落点任务（F-313 ~ F-325）先落地，F-326 才读终态 | `#3`、`#16`、`#17`、`#22` 四条读数失真（61 号 §5.3 读序列第 ④ 条） |
| 5 | 签字与上游只读 | 72 / F-326（25 条读数） | 72 / F-327（签字与 README） | 先验收、后签字 | E-72-7 的「`docs/zh/52` ~ `docs/zh/62` diff 命中 0 行」无从复核，红线 7 当场破（58 号 §4 第 7 条） |

三条读法：

1. 第 1、3 行是「编译不过」型：它们不产生假的绿，只产生红，因此最好排查。
2. 第 2、4 行是「静默型」：第 2 行留下一半音效（玩家侧时有时无），第 4 行让读数在未收口时失真。两条都必须在 Phase 出口处同时核对两个任务，不许「先交一半」。
3. 第 5 行是签字的唯一入口：`signoff.md` 里的「上游零改动」不是一句声明，是一条要跑命令的判据。

---

## Phase 66 · 发布面（M62）

### 66.1 本阶段要做什么

把 `mod_version` 推到 **0.2.4**，并把两支 `.cmd` 里写死的 jar 名与抬头行一次改齐 **6 处版本串**（`gradle.properties:11` 1 处 + `一键构建.cmd:5` / `:11` / `:12` 3 处 + `检查环境.cmd:5` / `:14` 2 处）。这一节只碰发布面：它是全部 jar 名与「检查环境」判据的唯一来源；`src/`、`run/`、`tools/` 本节一行不动（F-328 的删除已在写作期落地，见下），注册面与语言面一个字不提。本节另带一项**已落地**的删除（F-328）：`farmland/FarmlandGuard.java`（225 行）与 `farmland/FarmlandLedger.java`（555 行）两个类连同空目录于写作期整体删除，`FarmstormGameEvents`、`FarmstormConfig`、`OutpostCoreIndex` 三处连带清理同批完成；本节对它只做**复核**（跑 E-66-7 的三条读数），不再产生代码改动。

### 66.2 前置条件

- `gradle.properties:11` 现为 `mod_version=0.2.3`；`git log -1 --format=%h` 输出 `52749b9`（0.2.3 已实现态，58 号 D-225 的基线）。
- `git status --porcelain --untracked-files=no` 里，F-328 的 **5** 个路径（`D` 2：`farmland/FarmlandGuard.java`、`farmland/FarmlandLedger.java`；`M` 3：`config/FarmstormConfig.java`、`event/FarmstormGameEvents.java`、`outpost/OutpostCoreIndex.java`）不计入本节的改动面；本节**不新增**任何已跟踪路径；未跟踪文件集中在 `.work/`，同样不计。
- 版本串按**行**是 **6** 处：`gradle.properties:11` 1 处 + `一键构建.cmd:5` / `:11` / `:12` 3 处 + `检查环境.cmd:5` / `:14` 2 处。不是 4 处，也不是 5 处。
- 本节在 F-313 之前、F-328 之后：`(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` 此刻输出 **150**（`152 − 2 = 150`，`+1` 归 F-313），本节判据里 `151` 出现 **0** 次。
- 上一节：无（本节是首节）；本节 E-66-2 的 jar 名是 Phase 72 的 `#2` 与 `#22` 读数的前提。

### 66.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-312 | 版本号 0.2.4 与 6 处版本串 | S | `0.2.4` 三文件合计命中 6、`0.2.3` 命中 0；`farmstorm-0.2.4+mc1.21.1.jar` 与 `-sources.jar` 各 1 |
| F-328 | 删除 farmland 死代码（**本版已落地**，本节只复核） | L | `Farmland` 与 `farmland` 两条 grep 合计命中 **2**（都在 `datagen/ModLangProvider.java:39-40`）、`outpostCoreFarmlandBonus` 命中 **0**、`.java` 计数 **150**（新建 `CoreBound` 后 **151**） |

### 66.4 出口判据

1. **E-66-1 版本串一次改齐**：怎么验：三个文件逐个数。通过的样子：`0.2.4` 合计命中 **6**（`1 + 3 + 2`），其中 `一键构建.cmd:12` 的 `-sources.jar` 与两支脚本各 `:5` 的抬头行都在内；`0.2.3` 在这三个文件里命中 **0**。
2. **E-66-2 产物名与 toml 版本**：怎么验：跑 `gw.cmd build`，再 `jar tf build/libs/farmstorm-0.2.4+mc1.21.1.jar`。通过的样子：退出码 **0**；条目里有 `META-INF/neoforge.mods.toml`，其 `version` 解析为 `0.2.4`；`farmstorm-0.2.4+mc1.21.1-sources.jar` 同时存在；`build/libs` 里 `farmstorm-0.2.3` 残留 **0** 个。
3. **E-66-3 平台三行未动（负向）**：怎么验：`git diff gradle.properties`。通过的样子：只有第 **11** 行变化；`minecraft_version=1.21.1`、`neo_version=21.1.213`、Java toolchain `21` 三行逐字未变。
4. **E-66-4 源码面零新增（负向）**：怎么验：`git diff --stat src/ run/ tools/` 与计数命令。通过的样子：`git diff` 里除 F-328 的 5 个路径外无输出（本节新增行 **0**）；`(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` 仍输出 **150**（`152 − 2 = 150`，不是 151）。
5. **E-66-5 检查器仍绿**：怎么验：`python -X utf8 tools/verify_project.py`。通过的样子：末段 `FAIL 0 / WARN 1`；`git diff tools/verify_project.py` 里 `EXPECTED_COUNTS` 命中 **0** 行（`:458-471` 不改，61 号 A-109）。
6. **E-66-6 版本串单一来源（负向）**：怎么验：`grep -rn "0.2.4" src/main/java`。通过的样子：命中 **0**；`0.2.3` 在 `src/main/java` 同样命中 **0**——版本号只在 `gradle.properties:11` 一处来源。
7. **E-66-7 死代码删除已落地（F-328，已满足）**：怎么验：`grep -rn "Farmland|farmland" src/main/java`（GNU grep 的 BRE 下竖线是字面量，同一条模式用 `grep -rnE` 跑等价）、`grep -rn "outpostCoreFarmlandBonus" src/main/java`、`.java` 计数命令与 `Test-Path src/main/java/com/dashpig/farmstorm/farmland`。通过的样子（**已满足**，不是期望达成）：第一条命中恰 **2**，且都在 `datagen/ModLangProvider.java:39-40`（`farmstorm.configuration.farmland.limitPerPlayer` 的历史键名注释，随 321 键保留）；第二条命中 **0**；`.java` 计数输出 **150**（`152 − 2 = 150`），`CoreBound.java` 尚未新建、故不是 151；`Test-Path` 输出 **False**（空目录已删）。

### 66.5 常见翻车点

- **只改主 jar、漏 `-sources.jar`**：`一键构建.cmd:12` 仍按旧名复制，sources 静默不产出；预防：E-66-1 的合计必须 = 6，`:12` 单独点名。
- **把 151 提前写进本节**：`CoreBound.java` 尚未新建，`.java` 只有 **150** 个（`152 − 2 = 150`）；预防：E-66-4 的计数命令原样跑一次。
- **把 `ModLangProvider.java:39-40` 两行历史键名注释顺手删掉**：那两行是 E-66-7 的读数基准（`farmland` 命中恰 **2**），删掉后读数变 **0**、判据失去可核对面；预防：E-66-7 第一条 grep 原样跑一次，读数 2 不动。
- **顺手改 `tools/verify_project.py` 的 `EXPECTED` / `EXPECTED_COUNTS`**：本版变化的数字只有文件数一项（`152 − 2 + 1 = 151`），而它不进检查器（58 号 D-227、61 号 A-109）；预防：E-66-5 的 diff 命中 0 行。

### 66.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；6 处版本串的「文件:行号」逐处点名。
- **跑了什么命令、看到什么**：构建退出码、`jar tf` 里的 toml 条目、三个文件的两轮计数、`build/libs` 完整清单（含 `0.2.3` 残留 0 个）、一次真跑 `检查环境.cmd` 的输出。
- **没动的面**：`git diff --stat src/ run/ tools/` 原文（除 F-328 的 5 个路径外应为空）。
- **F-328 的三条读数**：`grep -rn "Farmland|farmland" src/main/java` 命中 **2**（附 `ModLangProvider.java:39-40` 两行原文）、`grep -rn "outpostCoreFarmlandBonus" src/main/java` 命中 **0**、`.java` 计数 **150**；两个整删文件的 `git diff --numstat` 读数为 `0 225` 与 `0 555`。
- **未验证的一项**：jar 能否进游戏不在本节验证，由 Phase 72 的 `#2` 承担。

---

## Phase 67 · 绑定契约：接口与两个实现（M63）

### 67.1 本阶段要做什么

新建 `blockentity/CoreBound.java`（只声明 `corePos()` / `setCore(BlockPos)` / `clearCore()` 三个方法），让 `CultivatorBlockEntity` 与 `CropConverterBlockEntity` 都实现它，并把转换器缺的 `clearCore()` 与私有 `syncCore()` 补齐、`convertible` 提为 `public`。这一节只做「契约 + 两个实现」，核侧一处不动（核侧在 Phase 68）。

### 67.2 前置条件

- Phase 66 已交付：`mod_version=0.2.4`（E-66-1）、F-328 三条读数在手（E-66-7）；上一节结束时 `git diff --stat src/` 里只剩 F-328 的 5 个路径。
- 培养器三方法已在位（`blockentity/CultivatorBlockEntity.java`）：类声明 `:44`、`corePos()` `:66`、`setCore` `:84-88`、`clearCore` `:94-101`、私有 `syncCore` `:104-110`。
- 转换器只有 `corePos()`（`CropConverterBlockEntity.java:71`）与 `setCore`（`:89-92`），**没有** `clearCore` / `syncCore`；`convertible` 在 `:129` 且是 `private`。
- 两条死命令要同提交：`implements CoreBound` 与它的 `clearCore()` 同提交（61 号 §0.5 死命令 1）；`this.syncCore()` 的调用与私有方法本身同提交（死命令 2），只写调用会得到 `cannot find symbol`（60 号 RT-87）。
- 本节结束前 `(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` 必须是 **151**（`152 − 2 + 1 = 151`）。

### 67.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-313 | 新建 `CoreBound` 三方法接口 | S | 三签名各 1、`kind(` 命中 0；`.java` 计数 151 |
| F-314 | 转换器实现接口 + `clearCore()` + `syncCore()` + `convertible` 提权 | M | `private void syncCore` 1、`sendBlockUpdated` 1、`Block.UPDATE_CLIENTS` 2、`public boolean convertible(` 1 |
| F-315 | 培养器 `implements CoreBound` | S | `implements CoreBound` 1、`git diff --numstat` = `1 1`、四个方法体 diff 0 行 |

### 67.4 出口判据

1. **E-67-1 文件数与包声明**：怎么验：`.java` 计数命令。通过的样子：输出 **151**（`152 − 2 + 1 = 151`）；`blockentity/CoreBound.java` 存在，包声明为 `com.dashpig.farmstorm.blockentity`，import 行含 `net.minecraft.core.BlockPos`。
2. **E-67-2 三签名各 1、第四方法归零**：怎么验：`CoreBound.java` 里逐个 grep。通过的样子：`BlockPos corePos();` / `void setCore(BlockPos pos);` / `void clearCore();` 各命中 **1**；`kind(` 命中 **0**（58 号 D-234）。
3. **E-67-3 转换器清缓存（含负向）**：怎么验：`CropConverterBlockEntity.java`。通过的样子：`public void clearCore()` 命中 **1**；方法体内 `this.held` / `this.progress` 命中 **0**（58 号 D-168 继续有效）。
4. **E-67-4 转换器同步三计数**：怎么验：同文件三串。通过的样子：`private void syncCore` 命中 **1**、`sendBlockUpdated` 命中 **1**、`Block.UPDATE_CLIENTS` 命中 **2**（既有 1 + 新方法 1）。
5. **E-67-5 `convertible` 提权**：怎么验：同文件两串加一次 diff。通过的样子：`public boolean convertible(` 命中 **1**、`private boolean convertible(` 命中 **0**；`canInsert(ItemStack stack)` 的方法体在 `git diff -U0` 里命中 **0** 行（服务端准入面一行不改）。
6. **E-67-6 培养器只加声明**：怎么验：`git diff --numstat` 该文件。通过的样子：输出 `1 1`；`implements CoreBound` 命中 **1**；`public BlockPos corePos()` / `public void setCore(BlockPos pos)` / `public void clearCore()` / `private void syncCore()` 各命中 **1** 且都不在 diff 里。
7. **E-67-7 核侧本节不动（负向）**：怎么验：`OutpostCoreBlockEntity.java` 与两个类声明。通过的样子：`implements CoreBound` 在 `blockentity/` 下合计 **2**（转换器 1 + 培养器 1）；`OutpostCoreBlockEntity.java` 里 `CoreBound` 命中 **0**、`AreaBlockEntities.collect` 仍命中 **2**。
8. **E-67-8 构建与检查器**：怎么验：`gw.cmd build` 与 `python -X utf8 tools/verify_project.py`。通过的样子：构建退出码 **0**；检查器末段 `FAIL 0 / WARN 1`。

### 67.5 常见翻车点

- **只写 `this.syncCore()` 不写方法**：转换器现无 `syncCore()`（全仓唯一定义在 `CultivatorBlockEntity.java:104-110`）⇒ `cannot find symbol`（60 号 RT-87）；预防：E-67-4 的定义 1 与 `sendBlockUpdated` 1 两处同查。
- **只加 `implements` 不补 `clearCore()`**：接口少一个实现 ⇒ `does not override abstract method clearCore()`（61 号 §0.5 死命令 1）。
- **给接口加第四个方法（例如 `kind()`）**：`Link.Kind` 是协议字段，接口承担它会出现第二份真相（58 号 D-234、60 号 RT-86）；预防：E-67-2 的 `kind(` 命中 0。
- **把 `convertible` 提权挪到 Phase 71**：`block/` 包调用 `private` 方法编译不过（61 号 A-104）；提权归本节 F-314。

### 67.6 完成后必须回报什么

- **新建文件的完整路径与三个方法签名原文**：逐行贴；`git diff --stat` 原文。
- **两条 numstat 读数**：`CultivatorBlockEntity.java` 的 `1 1`、`CropConverterBlockEntity.java` 的改动行数。
- **跑了什么命令、看到什么**：构建退出码、检查器末段原文、`.java` 计数命令的原始输出（151）。
- **没做的**：`convertible` 在客户端是否可调不在本节验证（60 号 RT-83，落在 Phase 71 的 E-71-1 与 E-71-7）。

---

## Phase 68 · 收集面与核侧统一（M64）

### 68.1 本阶段要做什么

给 `AreaBlockEntities` 加一个按谓词收集的重载（现有按 `Class` 的重载原样保留），再把 `OutpostCoreBlockEntity` 的候选发现、缓存判定、绑定写入、迁移改绑、清理出口五处改成经 `CoreBound` 走，并落下「方块 → `Link.Kind`」的单一映射 `KIND_BY_BLOCK`。这是需求 1 的正面修复（58 号 D-233），五处与映射同一提交（61 号 A-103）。

### 68.2 前置条件

- Phase 67 已交付：`CoreBound` 存在（E-67-1）、两个方块实体都实现（E-67-7）、转换器的 `public convertible` 在位（E-67-5）。
- 现状读数：`OutpostCoreBlockEntity.java:604-608` 是两次 `AreaBlockEntities.collect(..., Class)`；`:630-632` 对非空缓存一律 `continue`；`:866-873` 的出口②只有 `instanceof CultivatorBlockEntity`，之后无 else、无转换器分支；`isProcessingBlock` 在 `:887-889`。
- 死命令 5：`KIND_BY_BLOCK` 的取用必须与「删掉两次按类收集」同一提交——否则发现面按标签、kind 无来源，新成员取到 `null` 即 `continue`、静默不绑定（60 号 RT-86）。
- 表外两行不动：`:863` 与 `:867` 的出口③文案本版不改（61 号 A-108）。

### 68.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-316 | `AreaBlockEntities` 按谓词重载 | S | `collect(` 定义 2、`Class<T> type` 1、`getChunkNow(` 2 |
| F-317 | 核侧五处 + `KIND_BY_BLOCK`（需求 1 正面修复） | L | 一次按标签收集、`CultivatorBlockEntity.class` 命中 0、`clearCore()` 调用点 1、拆核后 `Core` 键消失 |

### 68.4 出口判据

1. **E-68-1 收集面两枚重载**：怎么验：`util/AreaBlockEntities.java`。通过的样子：`public static` 且 `collect(` 的定义命中 **2**（`Class<T> type` 1 + `Predicate<BlockEntity> filter` 1）；`getChunkNow(` 命中 **2**；`new ArrayList<>(chunk.getBlockEntities().values())` 命中 **2**。
2. **E-68-2 核侧只剩一次收集**：怎么验：`OutpostCoreBlockEntity.java` 三串。通过的样子：`AreaBlockEntities.collect` 命中 **1**（基线 2）；`CultivatorBlockEntity.class` 命中 **0**（基线 1）；定义行 `private static boolean isProcessingBlock(BlockState state)` 命中 **1**（判据原样复用，58 号 D-230）。
3. **E-68-3 两方法体无成对 `instanceof`（负向）**：怎么验：`rebindNearby` 与 `settleOnRemoval` 两方法体内逐个数。通过的样子：`instanceof CultivatorBlockEntity` 命中 **0**、`instanceof CropConverterBlockEntity` 命中 **0**。
4. **E-68-4 kind 的单一映射**：怎么验：同类三串。通过的样子：`KIND_BY_BLOCK` 命中 **1**；映射里 `Link.Kind.CULTIVATOR`、`Link.Kind.CROP_CONVERTER` 各 **1**；取到 `null` 即 `continue` 的分支留在源码里可读（60 号 RT-86）。
5. **E-68-5 清理出口覆盖两个方块**：怎么验：同类两串。通过的样子：`clearCore()` 的调用点命中 **1**；旧串「迁移失败：培养器留原地并标为未绑定」命中 **0**，新串「迁移失败：标签方块留原地并标为未绑定」命中 **1**；`:867` 的旧串「迁移失败且该格已不是培养器」命中 **1**（61 号 A-108 保持不变）。
6. **E-68-6 拆核后转换器的键消失（60 号 §8 `#10`）**：怎么验：拆掉转换器所绑的核，`/data get block <转换器坐标> Core`。通过的样子：读数报告该键不存在（未绑定）；`Held` / `Progress` 仍在。
7. **E-68-7 补绑日志一行（60 号 §8 `#11`）**：怎么验：3 × 3 区块内放一个新核、等一个补绑周期。通过的样子：日志出现 **1** 行 `补绑：核`，行内含 `本轮新增 1 条` 且 `累计` 增 **1**。
8. **E-68-8 再绑定成立（60 号 §8 `#12`）**：怎么验：串起 E-68-6 与 E-68-7 后再读一次。通过的样子：`/data get block <转换器坐标> Core` 等于新核坐标（一条 `Core` 长整数），三条读数按 D → A → B 的顺序可复查。

### 68.5 常见翻车点

- **只改清理不改发现（或反之）**：只改清理时转换器仍停在死坐标（`CropConverterBlockEntity.java:269-272` 写入 + `:630-632` 跳过），只改发现时 D → A 那条迁移根本不发生——两种都是被否决的半个修复（61 号 A-103）。
- **保留两次按类枚举、只给清理出口补一个转换器分支**：下一个标签成员会拿到同一个 bug 的第三份副本（58 号 §6 第 5 条）。
- **顺手把 `:867` 的出口③文案也改掉**：那是表外第二处文案改动，验收 5 从「一条旧串归零」变成「两条旧串归零」，回滚面变大（61 号 A-108）。
- **新重载里用 `level.getBlockEntity` 逐格取**：会为枚举加载区块，破坏 `AreaBlockEntities.java:39-42` 的既有纪律（60 号表 F 第 8 条）。

### 68.6 完成后必须回报什么

- **五处的行号与前后原文**：`OutpostCoreBlockEntity.java` 的 `:604-608`、`:621-629`、`:639-643`、`:851-855`、`:866-873` 各贴改动前后一行。
- **三条进游戏读数**：`#10` / `#11` / `#12` 的命令原文与返回值原文，按 D → A → B 顺序排。
- **三条 grep 计数**：`KIND_BY_BLOCK` 1、`AreaBlockEntities.collect` 1、`CultivatorBlockEntity.class` 0。
- **没动的两行**：`:863` 与 `:867` 在 `git diff` 里命中 **0** 行。

---

## Phase 69 · 交易经验分级（M65）

### 69.1 本阶段要做什么

把 1 ~ 5 级的成交经验从统一 **2** 改成 **2 / 5 / 10 / 15 / 30**：五个 `public static final int` 常量只在 `VillagerTradeEvents` 定义一次（58 号 D-236），`BotanistTrade` 与 `TierTrade` 的 record 各加 `xp` 分量逐项携带（D-237），`BotanistEpicTrade` 只改常量（D-238），`subList` 的分组边界一行不动（D-239）。

### 69.2 前置条件

- 现值三个类常量都是 **2**：`BotanistTrade.java:49`、`BotanistEpicTrade.java:46`、`TierTrade.java:45`。
- 22 项在 `BotanistTrade.java:60-82`（行内序号注释 `// 1` ~ `// 22`），三档在 `TierTrade.java:49-54`（走 `:56-58` 的三参 `of`）；切片在 `VillagerTradeEvents.java:44-47`、挂载在 `:55-57`。
- 两条死命令：record 的第二分量与 22 项实参同提交（死命令 3）、`TierTrade` 的分量与 `of(...)` 与三档常量同提交（死命令 4）；否则单实参字面量喂两形参、三参 `of` 喂四参，编译不过。
- 顺序：F-319（史诗类）排在 F-321（农民）**之前**（61 号 A-105），避免 `BotanistEpicTrade.java:28` 的 `{@link TierTrade#VILLAGER_XP}` 悬挂。
- 红线 4 与红线 6 本节点不动：`subList` 的 0 / 6 / 12 / 18 / 22、22 项的序号注释、`MIN_PRICE` / `MAX_PRICE` / `MAX_USES` / `COUNT` / `PRICE_MULTIPLIER`。

### 69.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-318 | 五个 `XP_LEVEL_n` 常量 | S | 五常量各 1、`public static final int` 5、diff 里 `subList(` 0 行 |
| F-319 | `BotanistEpicTrade` 常量 2 → 30 | S | `VILLAGER_XP = VillagerTradeEvents.XP_LEVEL_5` 1、`VILLAGER_XP = 2` 0、`TierTrade#VILLAGER_XP` 0 |
| F-320 | `BotanistTrade` record 加 `xp` + 22 项逐项带值 | M | `new BotanistTrade(` 22、引用 6 / 6 / 6 / 4、`VILLAGER_XP` 0、`this.xp` 1 |
| F-321 | `TierTrade` record 加 `xp` + 三档带值 | M | `XP_LEVEL_1` / `XP_LEVEL_3` / `XP_LEVEL_5` 各 1、`VILLAGER_XP` 0、`of(` 4 |

### 69.4 出口判据

1. **E-69-1 五常量只在一处**：怎么验：`VillagerTradeEvents.java`。通过的样子：`XP_LEVEL_1 = 2` / `XP_LEVEL_2 = 5` / `XP_LEVEL_3 = 10` / `XP_LEVEL_4 = 15` / `XP_LEVEL_5 = 30` 各命中 **1**；`public static final int` 命中 **5**、`private static final int` 命中 **0**（60 号 RT-89）。
2. **E-69-2 22 项逐项带值**：怎么验：`BotanistTrade.java`。通过的样子：`new BotanistTrade(` 命中 **22**；`XP_LEVEL_1` **6**、`XP_LEVEL_2` **6**、`XP_LEVEL_3` **6**、`XP_LEVEL_4` **4**，`6 + 6 + 6 + 4 = 22`。
3. **E-69-3 三档逐项带值**：怎么验：`TierTrade.java`。通过的样子：`XP_LEVEL_1` / `XP_LEVEL_3` / `XP_LEVEL_5` 各命中 **1**；`private static TierTrade of(` 命中 **1** 且形参含第四个 `int xp`；`of(` 命中 **4**（定义 1 + 调用 3）。
4. **E-69-4 三个类常量清干净（负向）**：怎么验：三类各数 `VILLAGER_XP`。通过的样子：`BotanistTrade` **0**、`TierTrade` **0**、`BotanistEpicTrade` **2**（声明 1 + 引用 1）；`this.xp` 在两个 record 类各 **1**。
5. **E-69-5 分组边界与顺序未动**：怎么验：四个切片、三个挂载点与 22 行序号注释。通过的样子：`subList(0, 6)` / `subList(6, 12)` / `subList(12, 18)` / `subList(18, 22)` 各命中 **1**；农民段 `:55-57` 三行逐行仍在，行内 `get(1)` / `get(3)` / `get(5)` 各命中 **1**（全文件各命中 **2**：植物学家段那三处一一对应不动）；`git diff -U0 VillagerTradeEvents.java` 里 `subList(` 命中 **0** 行；`BotanistTrade` 的 `// 1` ~ `// 22` 逐行仍在（红线 4）。
6. **E-69-6 价格与用量一行不动（红线 6）**：怎么验：三类常量逐个 grep。通过的样子：`MIN_PRICE = 3` / `MAX_PRICE = 6` / `MAX_USES = 8` / `COUNT = 1` / `PRICE_MULTIPLIER = 0.0F` 在 `BotanistTrade.java` 各 **1**；`CROP_COUNT = 2` / `MAX_USES = 1` 在 `TierTrade.java` 各 **1**；`MIN_PRICE = 40` / `MAX_PRICE = 64` / `MAX_USES = 1` / `COUNT = 1` 在 `BotanistEpicTrade.java` 各 **1**。
7. **E-69-7 分布读数（60 号 §8 `#17`）**：怎么验：`/data get entity <村民> Offers`，按 `xp:` 数值分组计数。通过的样子：`xp:2` **7**、`xp:5` **6**、`xp:10` **7**、`xp:15` **4**、`xp:30` **2**，`7 + 6 + 7 + 4 + 2 = 26`。

### 69.5 常见翻车点

- **按 `CropTier` 查一张表**：植物学家 1 ~ 4 级全是 `NORMAL` 切片，用档位推等级会把四个等级算成同一个值（58 号 §6 第 2 条）。
- **加分量时按等级重排 22 行**：等级由 `subList` 位置决定，重排等于换商品（60 号 RT-90）；预防：E-69-5 的序号注释逐行核对。
- **五个常量写成 `private`**：三个交易类引用不到，只能各自抄一份，同一等级出现两个值（60 号 RT-89）；预防：E-69-1 的 `private static final int` 命中 0。
- **先删三个类常量、再逐个加分量**：三处 `getOffer` 引用不到符号，三个类同时编译不过（61 号 A-105 的否决面）。

### 69.6 完成后必须回报什么

- **四个文件的改动行数**：`git diff --stat` 原文，逐文件点名。
- **常量命中数一张表**：E-69-6 的十二个常量逐行给「文件 / 常量 / 命中数」。
- **`Offers` 读数**：命令原文与返回 JSON 截取，含 `xp:` 五组的计数过程。
- **一条已知影响**：大师级单次 **30** 点 = 原 **2** 点的 **15** 倍（`30 ÷ 2 = 15`，59 号 §7.3），成文后交给 Phase 72 的签字。

---

## Phase 70 · 浮窗底色与标签注释（M66）

### 70.1 本阶段要做什么

把 `CoreHudRenderer.java:40` 的内层底色常量由 `0x80000000` 改成 **`0x4D000000`**（硬编码在渲染类里、不新增任何 config 键），并订正 `FarmstormTags.java:29` 那行与代码不符的注释。两个 S 任务：各一条 grep 判据，F-322 另加一次像素读数。

### 70.2 前置条件

- 现值：`CoreHudRenderer.java:40` 是 `0x80000000`（alpha `0x80` = 128，`128 ÷ 255 = 50.2%`）；`:41` 是 `COLOR_BORDER = 0xFF000000`；`:77-78` 两条 `fill`（`:77` 外框、`:78` 内底）；`:34` 的 `PADDING = 2` 与 `:36` 的 `PANEL_EXTRA_HEIGHT = 3`。
- 算术：`0.30 × 255 = 76.5 ≈ 77 = 0x4D`（`77 ÷ 255 = 30.2%`）。
- 注释现状（实读）：`FarmstormTags.java:29` 写「本版唯一成员是培养器，converter 不进（裁定 #5）」。「裁定 #5」指的是 52 号 §2 的第 5 条，不是本版的裁定 #5（放置音效）；跨版本引用必须写全来源（58 号 D-248）。
- 冻结面：client config **3** 键（`FarmstormClientConfig.java:25-27` 的 `hud.enabled` / `hud.x` / `hud.y`）、语言键 **321 × 2**、`OutpostCoreScreen` 一行不动（红线 5）。

### 70.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-322 | `CoreHudRenderer:40` 底色 `0x4D000000` | S | `0x4D000000` 1、`0x80000000` 0、`0xFF000000` 1；内层 alpha 77 与边框 alpha 255 |
| F-323 | `FarmstormTags:29` 注释订正 | S | 注释含「2 个成员」与「D-194」、`裁定 #5` 0；标签产物 2 值 |

### 70.4 出口判据

1. **E-70-1 三个颜色常量**：怎么验：`client/CoreHudRenderer.java`。通过的样子：`0x4D000000` 命中 **1**、`0x80000000` 命中 **0**、`0xFF000000` 命中 **1**。
2. **E-70-2 像素读数（60 号 §8 `#20`）**：怎么验：面板截图取内层 1 px 与边框 1 px。通过的样子：内层 alpha = **77**（`0x4D`）、边框 alpha = **255**（`0xFF`）。
3. **E-70-3 几何与版式未动（负向）**：怎么验：`git diff -U0` 该文件。通过的样子：`COLOR_BORDER` 所在 `:41` 行命中 **0** 行；`PADDING = 2` 命中 **1** 且不在 diff 里；`:77-78` 两条 `fill` 的几何命中 **0** 行。
4. **E-70-4 config 与语言面零变化**：怎么验：`git status --porcelain` 与三键 diff。通过的样子：不出现 `config/` 与 `lang/` 开头的路径；`hud.enabled` / `hud.x` / `hud.y` 的 diff **0** 行；`hud.opacity` 全仓命中 **0**。
5. **E-70-5 注释订正与标签产物**：怎么验：`util/FarmstormTags.java:29` 与标签产物。通过的样子：该行含「2 个成员」与「D-194」；`裁定 #5` 在该文件命中 **0**；`src/generated/resources/data/farmstorm/tags/block/processing_block.json:2-5` 仍是 2 个值（`farmstorm:cultivator`、`farmstorm:crop_converter`），`git status --porcelain` 不出现 `src/generated/resources` 开头的路径；`TagKey<Block> PROCESSING_BLOCK` 命中 **1**。

### 70.5 常见翻车点

- **把「30%」读成透明度、alpha 取 `0xB3`**：`179 ÷ 255 = 70.2%`，比现状 50.2% 更不透明，与 plan 的「增加透明度」相反（58 号 §6 第 4 条）。
- **挂一个 `hud.opacity` 新 config 键**：client config 3 键冻结，新增键还要配语言键，321 × 2 当场破（58 号 §6 第 3 条、E-70-4）。
- **改到 `OutpostCoreScreen`**：那是 54 格容器界面（`extends AbstractContainerScreen`），不是浮窗（58 号 D-240、红线 5）；预防：本节 diff 只允许 `client/CoreHudRenderer.java` 与 `util/FarmstormTags.java` 两个文件。
- **重跑 datagen 后按 mtime 判「产物变了」**：标签成员未变，按内容比对而不看时间戳（60 号 RT-91）。

### 70.6 完成后必须回报什么

- **两处 diff 原文**：`CoreHudRenderer.java:40` 与 `FarmstormTags.java:29` 各一行改动的前后原文。
- **两个像素读数**：内层 1 px 与边框 1 px 的 `#AARRGGBB` 值，附截图文件名。
- **`git status --porcelain` 全量原文**：应只多出这 2 个路径；`.work/` 下的未跟踪文件单独标出、不计入。
- **标签产物**：`processing_block.json` 的 `git diff`（应为空）与内容哈希。

---

## Phase 71 · 放置音效客户端预测（M67）

### 71.1 本阶段要做什么

把三处 `useWithoutItem` 的客户端分支由「无条件 `SUCCESS`」改成「手持输入物才接走、否则让路」：两台机器归 F-324（`convertible` / `admissible` 两条谓词），据点核心归 F-325（手持 `BlockItem` 才 `PASS`）。**不得**调 `canInsert`，服务端四道闸一行不改，音效配置一行不改。Phase 出口要求两个任务**都**落地（61 号 A-106）。

### 71.2 前置条件

- Phase 67 已交付 `public boolean convertible(`（E-67-5）：F-324 在 `block/` 包调用它，`private` 会报 `convertible has private access`（61 号 A-104）。
- 现值：`CropConverterBlock.java:121-122`、`CultivatorBlock.java:117-118`、`OutpostCoreBlock.java:131-132` 三处客户端分支都是无条件 `SUCCESS`；三处服务端分支在 `CropConverterBlock.java:124-132`、`CultivatorBlock.java:120-128`、`OutpostCoreBlock.java:134-142`。
- `canInsert` 含 `isUsable()`，而 `core()` 在客户端要求 `ServerLevel` ⇒ 客户端恒假；调用它等于「右键整台机器没反应」（58 号 D-244、60 号 RT-84）。
- 音效配置禁改：`FarmstormBlocks.java:78`（`crop_converter`）与 `:108`（`cultivator`）都是 `SoundType.ANVIL`；`sound_event` 仍 **3** 个（`Farmstorm.java:182` 的 `soundEvents == 3`）。
- 两条已知影响先成文：手持输入物但机器已满或未绑定 ⇒「无反应」（60 号 RT-84）；己方手持方块右键核会看到一次视觉闪动（60 号 RT-85）。两条都不当缺陷。

### 71.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-324 | 两台机器的客户端分支（需求 4 主体） | M | 客户端分支内 `convertible(` / `admissible(` 各 1、`canInsert` 0；两侧音频轨各 1 |
| F-325 | 据点核心的客户端分支 | S | `instanceof BlockItem` 1、客户端分支内 `isAlly(` 0、`isClientSide` 2 |

### 71.4 出口判据

读法：60 号 §8 `#21` 与 61 号 F-324 验收 1 写「`canInsert` 命中 0」；按全文件读会与「服务端四道闸一行不改」冲突（`CropConverterBlock.java:128`、`CultivatorBlock.java:124` 各有一处服务端 `canInsert`）。本节把归零判据收窄到 `if (level.isClientSide)` 分支内，并写明全文件计数为 **1**。

1. **E-71-1 两个谓词各就位、分支内禁用谓词归零**：怎么验：两个 `block/` 文件的客户端分支。通过的样子：`CropConverterBlock.java` 的客户端分支内 `convertible(` 命中 **1**、`canInsert` 命中 **0**（全文件 **1**，即服务端那一处）；`CultivatorBlock.java` 的客户端分支内 `admissible(` 命中 **1**、`canInsert` 命中 **0**（全文件 **1**）。
2. **E-71-2 条件返回两支都在**：怎么验：两个文件的 `if (level.isClientSide)` 分支。通过的样子：`InteractionResult.SUCCESS` 与 `InteractionResult.PASS` 各命中 **1**。
3. **E-71-3 据点核心按手持方块让路、客户端不做归属判定**：怎么验：`block/OutpostCoreBlock.java`。通过的样子：`player.getMainHandItem().getItem() instanceof BlockItem` 命中 **1**；客户端分支内 `isAlly(` 命中 **0**、服务端分支内命中 **1**（HC-03 继续有效）；该文件 `isClientSide` 命中 **2**（`:67` 既有 1 + `:131` 本节 1）、`canInsert` 命中 **0**。
4. **E-71-4 服务端四道闸零改动（负向）**：怎么验：三个文件的 `git diff -U0`。通过的样子：`canInsert(heldStack)`、`isAlly(player)`、`openMenu(` 三类行命中 **0** 行。
5. **E-71-5 音效配置零改动（58 号 D-246、红线 1）**：怎么验：`FarmstormBlocks.java` 与 `registry/FarmstormSounds.java`。通过的样子：`SoundType.ANVIL` 命中 **2**；`FarmstormSounds.java` 的 diff **0** 行；启动日志 `自检 注册对象` 行内 `sound_events 3`。
6. **E-71-6 两侧音频轨各 1（60 号 §8 `#25`）**：怎么验：双客户端录屏，A 手持泥土右键培养器顶面。通过的样子：A 侧音频轨 `block.anvil.place` **1** 次、B 侧 **1** 次；两侧 `latest.log` 无播放失败行。
7. **E-71-7 转换器对读（61 号 A-107）**：怎么验：同一读法把转换器复跑一遍。通过的样子：A 侧 `block.anvil.place` **1** 次、B 侧 **1** 次，与 E-71-6 的读数成对记录。

### 71.5 常见翻车点

- **复用 `canInsert`**：客户端拿不到 `ServerLevel`，`isUsable()` 恒假 ⇒ 右键整台机器没反应（58 号 D-244）；预防：E-71-1 的分支内命中 0。
- **换 `SoundType` 或补一条模组私有 place 音**：根因是客户端预测被 `SUCCESS` 掐断，不是音效资源；新增 `sound_event` 还会破注册面 **92**（58 号 D-246、红线 1）。
- **只修两台机器、把核心留到下一版**：三处同根因，只修两处会让同类现象时有时无（58 号 D-249、61 号 A-106）；Phase 出口要求 E-71-1 与 E-71-3 同时成立。
- **只验培养器就收工**：`#25` 只点名培养器，`convertible` 与 `admissible` 是两条不同谓词路径，一条通了推不出另一条（61 号 A-107）；预防：E-71-7 单独给读数。

### 71.6 完成后必须回报什么

- **三个 `block/` 文件的 diff 原文**：客户端分支改动前后各一行；服务端分支那几行贴出「命中 0 行」的证据。
- **五条判据的命中数表**：`E-71-1` ~ `E-71-5` 逐行给数字。
- **四条音频轨读数**：培养器 A / B、转换器 A / B，附录屏文件名与两侧 `latest.log` 的相关行。
- **未实测三项的归档**：只有放置者听不见 / 副手放置 / 客户端 `getRecipeManager()` 可用性，按读数记「未实测」或「本次实测」，不得写成已验证（58 号 D-252）。

---

## Phase 72 · 验收与签字（M68）

### 72.1 本阶段要做什么

把 60 号 §8 的 **25** 条逐条跑出读数，产出 `.work/v024-verify/acceptance.md`（每条给「怎么做 / 期望看到什么 / 实际读数」）；再把 59 号 §7 的 **4** 条已知影响、58 号 D-252 的 **3** 项未实测、九条风险 `RT-83` ~ `RT-91` 与十项不动读数签成 `.work/v024-verify/signoff.md`，并把三段玩家可见的话落进 `README.md`。

### 72.2 前置条件

- Phase 66 ~ 71 全部出口判据通过：E-66-* / E-67-* / E-68-* / E-69-* / E-70-* / E-71-* 各有读数在手（合计 **42** 条，`7 + 8 + 8 + 7 + 5 + 7 = 42`）。
- 十项基线：注册面 **92**、server config **175**、client config **3**、语言键 **321 × 2**、`configuration` 组 **178**、网络包 **2**、进度 **16**、转换配方 **21**、交易 listing **26**、Java 文件 **151**（`152 − 2 + 1 = 151`）。
- 25 条的落点按 §0.2 的表反查；一条挂多任务的只有 `#3`、`#16`、`#21`（61 号 §3 表 A.2）。
- 红线 7：不改 `docs/zh/52` ~ `docs/zh/62`、不改 `.work/v024-BRIEF.md`、不改 `plan0.2.4.txt`。

### 72.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-326 | 60 号 §8 的 25 条验收全量执行 | L | 25 / 25 有读数；`#3` 输出 151；`#17` 分组 7 / 6 / 7 / 4 / 2；`#22` 恰 14 个 `.java` |
| F-327 | 已知影响、三项未实测与签字 | M | `已知影响` 4、`未实测` 3、十项读数齐；README 三段同段共现 |

### 72.4 出口判据

1. **E-72-1 25 / 25 有读数**：怎么验：`acceptance.md` 的编号 `1` ~ `25` 逐条三栏齐全。通过的样子：源码类 **14**（`#4` ~ `#9` 6 条、`#13` ~ `#16` 4 条、`#18` 与 `#19` 2 条、`#21` 与 `#23` 2 条）、构建与检查器类 **5**（`#1`、`#2`、`#3`、`#22`、`#24`）、进游戏类 **6**（`#10`、`#11`、`#12`、`#17`、`#20`、`#25`），`14 + 5 + 6 = 25`。
2. **E-72-2 检查器与启动日志（`#1`、`#2`）**：怎么验：`python -X utf8 tools/verify_project.py` 与专用服务器首启。通过的样子：末段 `FAIL 0 / WARN 1`，`== 1. config` 段打印的生成 toml 键数 **175**；日志 `自检 注册对象` 恰 **1** 行，含 `92 个` 与行尾 `OK`。
3. **E-72-3 文件数与越界面（`#3`、`#22`）**：怎么验：计数命令与 `git status --porcelain`。通过的样子：`.java` 计数输出 **151**；出现的路径恰 **14** 个 `.java`（`12 + 1 + 1`），全部以 `src/main/java/com/dashpig/farmstorm/` 开头；`src/main/resources/` 与 `src/generated/resources/` 命中 **0**。
4. **E-72-4 协议、config 与语言键（`#23`、`#24`）**：怎么验：`FarmstormNetwork.java`、`run/config/farmstorm-server.toml`、两份 lang。通过的样子：`PROTOCOL_VERSION = "3"` 命中 **1**、`.playToClient(` 命中 **2**；toml 的 `[段]` **60** 个、`键 = 值` **175** 行；两份 lang 各 **321** 键、`farmstorm.configuration.` **178** 键。
5. **E-72-5 经验分布终态复核（`#17`）**：怎么验：`/data get entity <村民> Offers` 再读一遍。通过的样子：`xp:2` **7**、`xp:5` **6**、`xp:10` **7**、`xp:15` **4**、`xp:30` **2**，`7 + 6 + 7 + 4 + 2 = 26`，与 E-69-7 的读数逐组相等。
6. **E-72-6 已知影响 4 条与未实测 3 项**：怎么验：`signoff.md` 与 59 号 §7 的 7.1 ~ 7.4 逐条对照。通过的样子：`已知影响` 命中 **4** 且每条标「不是缺陷」（客户端只看品类 / 浮窗底色变透 / 大师级单次 **30** 点是 **15** 倍 / 绑定改由 `processing_block` 标签驱动）；`未实测` 命中 **3**（只有放置者听不见 / 副手放置 / 客户端 `getRecipeManager()` 可用性）。
7. **E-72-7 十项读数与 README 三段**：怎么验：`signoff.md` 的读数表与 `README.md`。通过的样子：十项读数全给（**151** / **92** / **175** / **3** / **321 × 2** / **178** / **2** / **16** / **21** / **26**）；`README.md` 三段同段共现：`0.2.4` 与 `151`、`无反应` 与 `已知影响`、`30 点` 与 `15 倍`；`docs/zh/52` ~ `docs/zh/62`、`.work/v024-BRIEF.md`、`plan0.2.4.txt` 的 `git diff` 命中 **0** 行。

### 72.5 常见翻车点

- **把 `#17` 的 `Offers` 读数当「要开面板才能做」**：它只依赖 `/data get entity` 的返回，服务端单人即可复现（60 号 §8 表下注 ②）。
- **按 mtime 判「资源面变了」**：重跑 datagen 后 `src/generated/resources` 下文件时间戳全变，只看路径不看时间（60 号 RT-91）；预防：E-72-3 的路径清单。
- **把静态读链或半截实测写成已验证**：三项（只有放置者听不见 / 副手放置 / 客户端 `getRecipeManager()`）只能按实测结果归档（58 号 D-252）。
- **为把 `#22` 压回 14 个路径而删自己的改动**：出现第 **15** 个路径要停下来查是不是越界，不是删代码凑数（60 号 §8 表下注 ①）。

### 72.6 完成后必须回报什么

- **两份记录**：`acceptance.md` 与 `signoff.md` 的路径、行数，`25` 条逐条编号清单。
- **十项读数表**：每项给「命令原文 + 读数」两列。
- **四条已知影响与三项未实测的落点**：`signoff.md` 的行号逐条点名。
- **上游零改动的命令原文**：`git diff --stat docs/zh/ .work/v024-BRIEF.md plan0.2.4.txt`（应为空）。

---

## 3. 本文自检

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 节数与小节 | 7 个 Phase 节（66 ~ 72）；每节 **6** 个三级小标题，顺序为「要做什么 / 前置条件 / 任务清单 / 出口判据 / 常见翻车点 / 必须回报」，`6 × 7 = 42` 个标题零调换 | 通过 |
| 2 | 任务覆盖 | 17 个任务与 61 号 §1 逐条相等：`2 + 3 + 2 + 4 + 2 + 2 + 2 = 17`（`16 = 327 − 312 + 1`，再加 F-328 一条）；名称 / 复杂度 / Phase 归属一处不改 | 通过 |
| 3 | E 编号 | 合计 **49** 条（`7 + 8 + 8 + 7 + 5 + 7 + 7 = 49`）；从 `E-66-1` 起按 Phase 分段、全局唯一；每节 **5 ~ 8** 条 | 通过 |
| 4 | E 可 grep | 49 条逐条读过：每条至少含一个源码或日志字面量（键名 / 常量值 / 日志片段 / 文件名），无「功能可用」类表述 | 通过 |
| 5 | 25 条验收无孤儿 | §0.2 的表把 `#1` ~ `#25` 全部落到节与判据：`2 + 1 + 7 + 5 + 1 + 2 + 2 + 5 = 25`，未覆盖 **0** | 通过 |
| 6 | 窗口台账 | §1 明写「本版无窗口」并给五条读数与出处（`:479-480` 不数文件、十一项 `− 0 + 0`、xp 无闸），同时写明「本版没有期望红的断言，任何一个中途 FAIL 都是真 bug」 | 通过 |
| 7 | 跨 Phase 台账 | §2 五条：需求 1 的六个面、音效三处、`convertible` 提权、25 条终态、签字只读（≥ 要求的 3 条） | 通过 |
| 8 | 数字纪律 | 全部为上游值或加减法：`152 − 2 + 1 = 151`、`6 + 6 + 6 + 4 = 22`、`7 + 6 + 7 + 4 + 2 = 26`、`14 + 5 + 6 = 25`、`30 ÷ 2 = 15`、`0.30 × 255 ≈ 77`、`128 ÷ 255 = 50.2%` | 通过 |
| 9 | 禁发其它系列 | 只发 E 系列；D / B / F / Phase / M / HC / N / RT / A 全部写全前缀引用，未认领任何编号 | 通过 |
| 10 | 行号与只引上游 | 引用的行号逐处按 `read` 工具复核（`CoreBound.java` 不存在、`ModLangProvider.java:39-40` 的 `farmland` 两行、`FarmstormTags.java:29` 原文、`OutpostCoreBlockEntity.java:604-608` / `:630-632` / `:866-873`、`verify_project.py:479-480` / `:613-622` / `:637-653`）；58 ~ 61 号与 `.work/` 一字未改 | 通过 |
| 11 | 与上游的两处差异 | 就地登记、不改上游：① 60 号 §8 `#21` 与 61 号 F-324 验收 1 的「`canInsert` 命中 0」收窄到客户端分支内（71.4 的读法行）；② 61 号 F-326 验收 1 的「15 + 5 + 5」按点名编号实点为 `14 + 5 + 6 = 25`（E-72-1） | 通过 |

> **本文的六个可复核判据**（交稿时逐条给读数）：Phase **7** 个（66 ~ 72）/ 任务 **17** 个（F-312 ~ F-327 共 16 条 + F-328 1 条）/ 出口判据 **49** 条 / 25 条验收 25 / 25 有落点 / 中间态窗口 **无** / 跨 Phase 台账 **5** 行。
