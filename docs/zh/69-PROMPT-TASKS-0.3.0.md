# 69 号 v0.3.0 阶段提示词（每个 Phase 开工时只投对应那一节）

> **定位**：实现方在每个 Phase 开工时只读对应一节——读一节、做一节、按该节的「完成后必须回报什么」交一份回报，再读下一节。本文不复述 64 号的论证，只给六样东西：做什么 / 前置条件 / 任务清单 / 出口判据 / 常见翻车点 / 回报口径。
> **上游**：64 号（`docs/zh/64-DECISIONS-0.3.0.md`，D-255 ~ D-289，唯一真相源）/ 65 号（B-74 ~ B-87）/ 66 号（RT-92 ~ RT-99、§8 的 29 条验收）/ 67 号（F-329 ~ F-349、Phase 73 ~ 83、A-112 ~ A-118）/ 68 号（`docs/zh/68-PROMPT-MAIN-0.3.0.md`，HC-189 起、N-167 起）/ `.work/v030-BRIEF.md`（241 行事实卡）。
> **编号**：本文只发一个系列——出口判据 **E-73-1 起**，形状 `E-<Phase>-<序>`，按 Phase 分段、段内从 1 连续编号、不跳号，末条是 **E-83-6**。其余系列一律写全前缀（D-255 起 / B-74 起 / F-329 起 / Phase 73 起 / RT-92 起 / A-112 起 / HC-189 起 / N-167 起 / E-73-1 起），不认领下游编号。
> **行号**：本文引用的每一处行号都按 UTF-8 实读核过；上游报过、本文未复核的行号一处都不引（§14 第 1 条）。
> **数字**：只取 64 号 D-258 冻结的那一组（D-258 的十二项 + `BRIEF` §3 的四个资源计数，共 16 项：注册面 96、server config 175、client config 3、语言键 328、Java 文件 156、网络包 2、协议版本 `"4"`、Mixin 0、方块战利品表 29、转换配方 21、合成配方 5、进度 16、方块模型 79、贴图 PNG 88、mcmeta 8、文件总数 96）；中间读数一律写成算式（321 + 2 = 323、92 + 1 + 1 = 94），不发明新数。
> **判据**：只用五类可观察读数——计数、日志行、`/data get` 读数、grep 命中、像素比对。感受类措辞不进任何出口判据。
> **冲突**：上游打架时按 **决策 > 技术 > 任务** 取顺序，一律报给作者、不自行取舍；本文已登记的四条见 §14。

---

## §0 投喂顺序

**先投 68 号一次**（`docs/zh/68-PROMPT-MAIN-0.3.0.md`：HC-189 起、N-167 起、陷阱清单、冲突处理与回报口径，全程常驻）；**此后每个 Phase 开工时只投本文对应的那一节**——投一节、做一节、按该节的「完成后必须回报什么」交一份回报，再投下一节。

投喂前先确认两支提示词都在仓库里：`Test-Path docs/zh/68-PROMPT-MAIN-0.3.0.md` 与 `Test-Path docs/zh/69-PROMPT-TASKS-0.3.0.md` 都为 `True`。68 号缺失时先补 68 号，本文不替代它。

四条读法：

1. **一次只投一节**。每节自带前置条件与出口判据，单节脱离其它节也能执行；同时投两节会让两节的「前置条件」互相覆盖，实现方会按最后读到的那份干活。
2. **节序单调**：§1（Phase 73）→ §2（Phase 74）→ … → §11（Phase 83），不得调换。节内还有 67 号 §5.3 的关键路径（`F-331 → F-333 → F-334 → F-335 → F-336 → F-340 → F-349`）与两条顺序纪律（§3 的 E-75-7、§5 的 E-77-7）。
3. **不跳节**。每一节的前置条件都至少有一条读数来自上一节的出口判据；跳过任一节，那条读数就是「未发生」，且它不会自己变绿。
4. **并行不靠跳节**。67 号 §5.1 的三条线可并行（A 终端主线 F-329 ~ F-340 / B 规则线 F-341、F-342 / C 干草块线 F-343 ~ F-345），并行时一节仍只投一次，两节分给两个人，同一文件只被一个任务改。

### 0.1 节序与出口判据区间

每一节的「出口判据」是一段独立的编号区间，段内从 1 起、不跳号；投完一节就核这一节的区间，核完再投下一节。

| § 号 | Phase | 里程碑 | F 号 | 任务数 | 出口判据区间 |
|---|---|---|---|---|---|
| §1 | 73 | M69 | F-329 | 1 | E-73-1 ~ E-73-6 |
| §2 | 74 | M70 | F-330 ~ F-332 | 3 | E-74-1 ~ E-74-7 |
| §3 | 75 | M71 | F-333 ~ F-335 | 3 | E-75-1 ~ E-75-7 |
| §4 | 76 | M72 | F-336 ~ F-338 | 3 | E-76-1 ~ E-76-7 |
| §5 | 77 | M73 | F-339 ~ F-340 | 2 | E-77-1 ~ E-77-7 |
| §6 | 78 | M74 | F-341 | 1 | E-78-1 ~ E-78-7 |
| §7 | 79 | M75 | F-342 | 1 | E-79-1 ~ E-79-6 |
| §8 | 80 | M76 | F-343 ~ F-345 | 3 | E-80-1 ~ E-80-7 |
| §9 | 81 | M77 | F-346 ~ F-347 | 2 | E-81-1 ~ E-81-6 |
| §10 | 82 | M78 | F-348 | 1 | E-82-1 ~ E-82-6 |
| §11 | 83 | M79 | F-349 | 1 | E-83-1 ~ E-83-6 |
| **合计** | **11** 个 | **M69 ~ M79** | **F-329 ~ F-349** | **21** | **72** 条 |

### 0.2 两个常驻表

两个常驻表在 **§12 窗口台账**（哪些 Phase 处在 W1 / W2 内、窗口内哪条断言期望红、哪个文件禁写）与 **§13 跨 Phase 台账**（哪些动作横跨两个以上 Phase、谁先谁后、违反会怎样）。这两节不随投喂进度更换，开工前先读一遍。

本节涉及四类名词，首次出现按裁定 #12 / D-288 并列写：控制终端（`farmstorm:control_terminal`）、农作物培养器（`farmstorm:cultivator`）、农作物转换器（`farmstorm:crop_converter`）、据点核心（`farmstorm:outpost_core`）、膨胀麦（`farmstorm:puffed_wheat`）、压缩干草块（`farmstorm:compressed_hay_block`）、干草块（`minecraft:hay_block`）；其后一律只用中文名。

---

## §1 Phase 73 · 版本串（M69）

### 73.1 本阶段要做什么

把 `mod_version` 推到 **0.3.0**，并把两支 `.cmd` 里写死的 jar 名与抬头行一次改齐，共 **6 处 / 3 个文件**（D-255）。这一节只碰发布面：它是全部 jar 名与「检查环境」判据的唯一来源；`src/`、`run/`、`tools/`、`docs/zh/64` ~ `docs/zh/67` 本节一行不动，注册面与语言面一个字不提——那两面在 Phase 74 才开窗。

### 73.2 前置条件

- `gradle.properties:11` 现为 `mod_version=0.2.7`（实读）；6 处的分布是 `gradle.properties:11` 1 处 + `一键构建.cmd:5` / `:11` / `:12` 3 处 + `检查环境.cmd:5` / `:14` 2 处（67 号 §2 的 F-329 交付物）。
- 基线读数（`BRIEF` §0）：检查器 **FAIL 0 / WARN 1**、注册面 **92**、语言键 **321** × 2、Java 文件 **151**、协议版本 `"3"`。
- 本节在上游零改动的前提下开工：`docs/zh/64` ~ `docs/zh/67` 只读，改它们等于改真相源。
- 上一节：无（本节是首节）。本节 E-73-2 的 jar 名是 Phase 83 交付清单与 E-83-4 数字面复核的前提。

### 73.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-329 | 版本串 0.3.0 | S | `0.2.7` 在三文件命中 **0**、`0.3.0` 命中 **6**（1 + 3 + 2）；`build/libs` 里 `farmstorm-0.3.0+mc1.21.1.jar` 与 `-sources.jar` 各 **1** |

### 73.4 出口判据

1. **E-73-1 版本串一次改齐**：怎么验：`Select-String -Path gradle.properties,一键构建.cmd,检查环境.cmd -Pattern 0.2.7` 与同一条命令换 `-Pattern 0.3.0` 各跑一次。通过的样子：前者命中 **0** 行，后者命中 **6** 行。
2. **E-73-2 产物名与残留**：怎么验：跑一次构建后 `Get-ChildItem build\libs`。通过的样子：构建退出码 **0**；`farmstorm-0.3.0+mc1.21.1.jar` 与 `farmstorm-0.3.0+mc1.21.1-sources.jar` 各 **1** 个，`farmstorm-0.2.7` 残留 **0** 个。
3. **E-73-3 版本号单一来源（负向）**：怎么验：`Select-String -Path src -Recurse -Pattern 0.3.0`。通过的样子：命中 **0** 行——版本号只在 `gradle.properties:11` 一处来源。

> **〔v0.3.0 实施期更正 · F-329 · 依据实测〕**
> ~~原文命令：`Select-String -Path src -Recurse -Pattern 0.3.0`~~
> 现口径（C8）：PowerShell 的 `Select-String` **没有 `-Recurse` 参数**，原文命令必报 `ArgumentException`。改用 `Get-ChildItem -Path src -Recurse -File | Select-String -Pattern 0.3.0 -SimpleMatch`（实测命中 0；不带 `-SimpleMatch` 时会被 `src/generated/resources/.cache` 里的 git 哈希误命中 1 条）。另：`-SimpleMatch` 只对**单个字面量**有效，带 `|` 的多分支必须去掉该开关（F-332 实测踩到）。
4. **E-73-4 平台三行未动（负向）**：怎么验：`git diff gradle.properties`。通过的样子：只有第 **11** 行变化；`minecraft_version=1.21.1`、`neo_version=21.1.213`、Java toolchain `21` 三行逐字未变。
5. **E-73-5 检查器基线未动**：怎么验：`python -X utf8 tools\verify_project.py`。通过的样子：末段 **FAIL 0 / WARN 1**；`git diff tools/verify_project.py` 命中 **0** 行；§2 打出的注册面仍是 **92**（本节不开 W1）。
6. **E-73-6 源码面零改动（负向）**：怎么验：`git status --porcelain --untracked-files=no` 与 `.java` 计数命令。通过的样子：`src/`、`run/`、`tools/` 下命中 **0** 个路径；`(Get-ChildItem src\main\java -Recurse -Filter *.java).Count` 输出 **151**。

### 73.5 常见翻车点

- **只改主 jar、漏 `-sources.jar`**：`一键构建.cmd:12` 仍按旧名复制，sources 静默不产出。预防：E-73-1 的合计必须 = 6，`:12` 单独点名。
- **把 `0.3.0` 写进 `src/main/java`**：版本号变成两处来源，下一版要改两遍。预防：E-73-3 的 grep 命中 0。
- **把 `0.2.7` 连同文档里的版本串一起替换**：D-255 只定 **3** 个文件的 **6** 处；`docs/zh` 与 `README.md` 里的版本串属于「只引不改」的上游内容，本节改它们即越过边界。预防：E-73-6 的 `git status` 判据（`docs/` 下命中 0）。
- **顺手把 `tools/verify_project.py` 的 `EXPECTED_COUNTS` 抬到 96**：那是 Phase 82 的 F-348；提前改会让 Phase 74 ~ 81 每一步都「已绿」，四个新 ID 缺项不再报（D-289 的中间态窗口）。预防：E-73-5 的 diff 命中 0 行。

### 73.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；6 处版本串的「文件:行号」逐处点名。
- **跑了什么命令、看到什么**：两轮 `Select-String` 的命中行数（0 / 6）、构建退出码、`build\libs` 的完整清单（含旧名残留 0 个）。
- **没动的面**：`git status --porcelain` 原文（`src/`、`run/`、`tools/` 下为空）、检查器末段原文、`.java` 计数读数（**151**）。

---

## §2 Phase 74 · 终端资源与方块（M70）

### 74.1 本阶段要做什么

落 7 个手放资源文件（4 张 PNG + 3 个 mcmeta，D-262），注册方块的方块本体与方块物品并让它进创造栏与语言文件（D-259、D-260），再出 4 个朝向 variant 的 blockstate、1 张 cube 模型与 item 模型白名单（D-261）。本节**同时打开两个窗口**：W1 注册面（F-331 是第一个把 `blocks` / `items` 从 28 抬到 29 的任务）与 W2 语言键表头（F-331 第一次让语言键从 321 变 323）——此后检查器 §2 的计数断言进入期望红区，那不是 bug。

### 74.2 前置条件

- Phase 73 已交付：`mod_version=0.3.0`（E-73-1）、jar 名成立（E-73-2）；上一节结束时 `.java` 计数仍是 **151**。
- 素材包在 `C:\Users\DASHpig\Downloads\control_terminal_a_32x32_pack\assets\farmstorm\textures\block\`，四张 PNG 与三个 mcmeta 逐字节复制、不改名、不重采样（66 号 §3.1）。
- 命名分工：**注册 id 不带 `_a`**（D-259），**贴图文件名带 `_a`**（66 号 §3.1 与既有 `cultivator_a_top.png` 同体例）。两件事各按各的规则走，不要互相统一。
- 现状读数：`textures/block` **84** 个 `.png` + **5** 个 `.mcmeta` = **89** 个文件；`models/block` **78**；`blockstates` **28**；语言键 **321** × 2；`mod_version` 已 **0.3.0**。
- 窗口纪律：本节起 W1 与 W2 同时打开，`tools/verify_project.py` 与 `ModLangProvider.java:10-16` 两处**禁写**（§12）。

### 74.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-330 | 终端贴图 7 文件手放 | S | `.png` 84 + 4 = **88**、`.mcmeta` 5 + 3 = **8**、文件总数 89 + 7 = **96**；三张动画 PNG 高 **768** = 24 × 32，底面 **32** |
| F-331 | 方块与物品注册 | M | 检查器 §2 的 `items` / `blocks` 各出现 `control_terminal`；注册面 92 + 1 + 1 = **94**；`zh_cn.json` 键数 321 + 2 = **323** |
| F-332 | 模型与 blockstate | M | `blockstates/control_terminal.json` 的 `facing=` 键 **4** 个；`models/block` 78 + 1 = **79**；`models/item/control_terminal.json` 的 `parent` = `farmstorm:block/control_terminal` |

### 74.4 出口判据

1. **E-74-1 贴图三个数分开读**：怎么验：`Get-ChildItem src\main\resources\assets\farmstorm\textures\block -Filter *.png` 与 `-Filter *.mcmeta` 各数一次，再把两数相加。通过的样子：`.png` **88**、`.mcmeta` **8**、两者相加 **96**；三个数任一串位都算不通过（RT-96）。
2. **E-74-2 帧条尺寸与 mcmeta 帧数**：怎么验：读四张 PNG 的像素高度与三个 mcmeta 的 `frames` 长度。通过的样子：front / side / top 三张高 **768**、底面 **32**；三个 mcmeta 的 `frametime` 为 `2`、`frames` 恰 **24** 项（0 ~ 23）。
3. **E-74-3 两个注册项 + 语言键开窗**：怎么验：`Select-String -Path tools\verify_project.py -Pattern control_terminal` 与两份语言文件的顶层键计数。通过的样子：检查器 §2 打出 `items` / `blocks` 各含 `control_terminal`（实际计数 **94**）；`zh_cn.json` 与 `en_us.json` 各 **323** 键且键集相等。
4. **E-74-4 窗口内期望红（W1）**：怎么验：`python -X utf8 tools\verify_project.py` 与 `git diff tools/verify_project.py`。通过的样子：§2 的注册面一行打出实际 **94** 而 `EXPECTED_COUNTS` 仍读 **92**（该行 FAIL 是窗口期望红，不算 bug）；`git diff` 命中 **0** 行——本节不碰检查器。
5. **E-74-5 blockstate 4 variant**：怎么验：`Get-Content src\generated\resources\assets\farmstorm\blockstates\control_terminal.json -Raw` 数 `variants` 的键。通过的样子：恰好 **4** 个键 `facing=north` / `east` / `south` / `west`，`y` 分别不写 / 90 / 180 / 270；文件里 `working` 命中 **0**。
6. **E-74-6 item 模型走方块模型**：怎么验：读 `models/item/control_terminal.json` 的 `parent`，并 `Select-String -Path src\main\java\com\dashpig\farmstorm\datagen\ModItemModelProvider.java -Pattern ControlTerminalBlock`。通过的样子：`parent` 为 `farmstorm:block/control_terminal`；白名单里 `ControlTerminalBlock` 命中 **1**（漏了会落 `basicItem`、游戏里是紫黑格）。
7. **E-74-7 构建与命名分工**：怎么验：跑一次构建，并 `Select-String -Path src\main\java\com\dashpig\farmstorm\registry -Pattern control_terminal_a`。通过的样子：退出码 **0**；注册面文件里 `_a` 后缀命中 **0**（`_a` 只许出现在贴图文件名里）。

> **〔v0.3.0 实施期更正 · F-331 / F-332 · 依据实测〕**
> ~~E-74-3 与 E-74-4 的「F-331 交付后语言键 323」「§2 计数断言期望红」~~
> 现口径一（C9）：F-331 窗口内 `runData` **必失败** —— `ModBlockStateProvider.java:91` 的 `simpleBlock` 兜底要 `textures/block/control_terminal.png`（不存在），实测抛 `IllegalArgumentException: Texture farmstorm:block/control_terminal does not exist in any known resource pack`；`ModItemModelProvider.java:68-70` 白名单同型。故 F-331 只交源码面（语言键仍 **321**、`src/generated` 零改动），**323 与 blockstate / 两模型顺延 F-332 的 `runData`**（实测 321 → 323）。
> 现口径二（C10）：W1 的「§2 计数断言期望红」**前提不成立** —— `tools/verify_project.py:609-623` 的 §2 只核对自带清单（`total += len(ids)`、`expected_total = sum(EXPECTED_COUNTS.values())`），**不数源码注册面**；新增注册项在关窗前完全不可见 ⇒ W1 全程 `92 / 92` 绿，F-348 改清单后才变 96。窗口内不存在期望红。

### 74.5 常见翻车点

- **mcmeta 照抄项目内 5 个既有的最小形（16 帧口径）**：只播前 16 帧、第 17 ~ 24 帧永不出现；PNG 被重采样成 32×512 也得到同样症状（RT-92）。预防：E-74-2 的两个读数同时读。
- **`ModItemModelProvider.java:68-70` 的白名单漏登记**：物品落到 `basicItem`，去找一张不存在的 `textures/item/control_terminal.png`，游戏里是紫黑格（RT-99）。预防：E-74-6 的 grep 命中 1。
- **blockstate 用 `simpleBlock` 或漏 `else if` 分支**：落进兜底 `simpleBlock`，4 个朝向里 3 个找不到模型（`ModBlockStateProvider.java:82-92` 的 `else if` 链与兜底）。预防：E-74-5 的 `facing=` 键数必须 = 4。
- **把注册 id 写成 `control_terminal_a`**：贴图名与注册 id 混成一个，下一版出 B 版时 id 要跟着改（D-259 的补充）。预防：E-74-7 的 `_a` 在 `registry` 下命中 0。

### 74.6 完成后必须回报什么

- **7 个资源文件的路径 + 字节数 + PNG 像素尺寸**：逐个点名；三个 mcmeta 的 `frames` 长度（各 24）。
- **注册面三读数**：`items` 29、`blocks` 29、合计 **94**；语言键 **323** × 2 的两份计数命令原文。
- **跑了什么命令、看到什么**：构建退出码、检查器 §2 那一行的原文（含期望红说明）、`git diff tools/verify_project.py` 的空输出。
- **没做的**：贴图在游戏里的帧数与观感不在本节验证，由 Phase 83 的数字面复核与 66 号 §8 第 5 条承担。

---

## §3 Phase 75 · 方块实体、连线协议与类型表（M71）

### 75.1 本阶段要做什么

让终端成为方块实体（模式三态存自己的 NBT，D-269），给线上码追加第三个值 `TERMINAL(2)` 并画绿线、把协议版本升到 **4**（D-274 ~ D-277），再把控制终端同时写进 `farmstorm:processing_block` 标签与 `KIND_BY_BLOCK`（D-263）——后两处必须同一个提交改完。本节是**顺序纪律①**的落点：`Link.Kind.TERMINAL` 必须先于 `KIND_BY_BLOCK` 的第 3 条存在，否则编译不过。

### 75.2 前置条件

- Phase 74 已交付：方块与物品注册在位（E-74-3）、模型与 blockstate 成立（E-74-5）。
- 现状读数（实读）：`KIND_BY_BLOCK` 现有 **2** 条（`OutpostCoreBlockEntity.java:123-125`）；`Kind` 现有 **2** 值 `CULTIVATOR(0)` / `CROP_CONVERTER(1)`（`CultivatorLinkPayload.java:69-71`），按 `BYTE` 上线（`:49`），`byId` 回落 `CULTIVATOR`（`:85-92`）；`PROTOCOL_VERSION = "3"`（`FarmstormNetwork.java:29`）；渲染器 `switch (link.kind())` 无 `default`（66 号 §5.2）。
- **顺序纪律①**：F-334 必须先于 F-335 落地——`KIND_BY_BLOCK` 的 `Map.of` 要引用 `Link.Kind.TERMINAL`，枚举没落就编译不过。
- 窗口纪律：本节仍在 W1 / W2 内，`tools/verify_project.py` 与 `ModLangProvider.java:10-16` 禁写（§12）。

### 75.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-333 | 方块实体与 NBT | M | `block_entity_type` 8 + 1 = **9**、注册面 92 + 3 = **95**；`/data get block <pos> Mode` 读到 `off`；退出重进读数不变；未绑定时 `Core` 键不存在 |
| F-334 | 绿色连线与协议 4 | M | `Kind` **3** 值、`byId(2)` 返回 `TERMINAL`；渲染 `switch` **3** 分支且无 `default`；`PROTOCOL_VERSION` 命中 `"4"`；启动日志两行带「版本 4」 |
| F-335 | 标签与 kind 表同改 | S | `processing_block.json` 的 `values` **3** 项；`KIND_BY_BLOCK` **3** 条且含 `TERMINAL`；放下终端后核侧名单 **+1** |

### 75.4 出口判据

1. **E-75-1 方块实体类型与注册面**：怎么验：检查器 §2 的 `block_entity_type` 一行与注册面合计。通过的样子：`control_terminal` 命中 **1**、该类计数 **9**、注册面合计 **95**（92 + 1 + 1 + 1）。
2. **E-75-2 模式键可读可存**：怎么验：放一个全新终端后 `/data get block <终端坐标> Mode`，退出重进再读一次。通过的样子：两次都读到 `off`（初始态 `OFF`，D-269 / 裁定 #13）；切换模式后再重进，读数与切换后一致。
3. **E-75-3 未绑定时 `Core` 键缺失（负向）**：怎么验：`/data get block <终端坐标> Core`。通过的样子：读不到该键（缺键即未绑定，`CoreBound` 的可空语义，D-264）；该读数取的是 3×3 区块**之外**放下的那台终端——区块之内放下的终端在 F-335 之后应读到 `Core` 有值，两侧一起看才说明「缺键 = 未绑定」没被破坏。
4. **E-75-4 线上码只追加不重排**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\network\CultivatorLinkPayload.java -Pattern "CULTIVATOR\(0\)|CROP_CONVERTER\(1\)|TERMINAL\(2\)"`。通过的样子：三个字面量各命中 **1**；枚举值总数 **3**；既有两个值的 id 数值一个都没变。
5. **E-75-5 渲染分支穷尽 + 绿色常量**：怎么验：读 `client/CultivatorLinkRenderer.java` 的 `switch` 与三组常量。通过的样子：`switch` 的 `case` **3** 个、`default` 命中 **0**（漏一个分支编译不过）；`0.30` / `0.85` / `0.30` 这一组各命中 **1**，既有青（`0.30` / `0.85` / `0.90`）与红（`0.92` / `0.22` / `0.22`）逐字未变。
6. **E-75-6 协议版本与启动日志**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\network\FarmstormNetwork.java -Pattern PROTOCOL_VERSION` 与一次真启动的日志。通过的样子：字面量为 `"4"`；日志里 `自检 网络包注册` 两行都带「版本 4」。
7. **E-75-7 类型表与标签同提交（顺序纪律①）**：怎么验：`git log --stat -1` 看 F-335 的提交内容，再 `Select-String` 两处。通过的样子：同一提交里既有 `ModBlockTagsProvider.java:45-47` 的成员 2 → 3，也有 `OutpostCoreBlockEntity.java:123-125` 的第 3 条；`processing_block.json` 的 `values` 读 **3**；构建退出码 **0**。

### 75.5 常见翻车点

- **先改 `KIND_BY_BLOCK` 再加枚举**：`Map.of` 引用不到 `Link.Kind.TERMINAL`，`cannot find symbol`、编译不过。预防：F-334 先提交，F-335 后提交（顺序纪律①，E-75-7 的 `git log` 判据）。
- **重排既有两个 `Kind` 的线上码**：旧客户端会把培养器的线画成终端的线（`byId` 按数值反查，线上没有名字）。预防：E-75-4 的三个字面量各 1。
- **把模式存成 `ordinal()`**：`/data get` 读出数字，判据失去可读面，且枚举顺序一改存档含义就变（D-269 与 66 号 §4.1.1 要求存序列化名）。预防：E-75-2 必须读到 `off` 这种字符串。
- **只改标签不改类型表（或反之）**：前者「被发现、不绑定」，后者「扫不到」，两种半边改动都静默不报错（D-263、RT-99）。预防：E-75-7 的两处同一提交判据。

### 75.6 完成后必须回报什么

- **三处枚举与常量的原文**：`Kind` 三个成员、`switch` 三个分支、`PROTOCOL_VERSION` 一行，逐行贴。
- **`/data get` 的三次读数**：`Mode` 初始值、切换后的值、重进后的值；`Core` 键缺失的那一次输出。
- **跑了什么命令、看到什么**：构建退出码、启动日志两行原文、检查器 §2 的注册面合计（**95**）、`git log --stat -1` 的提交内容。
- **没做的**：绿线的像素读数不在本节验证，由 Phase 83 的数字面复核与 66 号 §8 第 12 条承担。

---

## §4 Phase 76 · 绑定与面板（M72）

### 76.1 本阶段要做什么

让终端与两台机器同形地进入三条绑定路径与一个清理出口（D-263 ~ D-266：放置绑定、补绑扫描、迁移改绑、拆核清理），再给它一个 **0 槽菜单**并在 `RegisterMenuScreensEvent` 注册屏幕（D-273、D-267）。面板只有两个模式按钮与一行状态：未绑定时状态行写「未绑定」、两个按钮置灰。本节结束时注册面到 **96**（92 + 1 + 1 + 1 + 1）。

### 76.2 前置条件

- Phase 75 已交付：方块实体在位（E-75-1）、`Kind.TERMINAL` 与协议 **4** 在位（E-75-4、E-75-6）、标签 3 成员与类型表 3 条同提交（E-75-7）。
- 既有先例（实读）：`addCultivator` 在已绑或满员时返回 `false`（`OutpostCoreBlockEntity.java:413-427`），满员判据在 `:400-403`，补绑方法 `rebindNearby`（`:608`）的周期常量是 `REBIND_INTERVAL_TICKS = 100`（`:111`）；菜单类型现 **1** 个（`FarmstormMenus.java:17-18`）；`stillValid` 与两个构造器的写法照 `OutpostCoreMenu.java:132-135` 与 `:52-59`（66 号 §4.6）。
- **写入顺序不可换**：先调 `core.addCultivator(pos, Link.Kind.TERMINAL)`，成功之后才写终端自己那份引用缓存；反过来会留下「核说没绑、终端说绑了」的假象（66 号 §4.3 第 3 步）。
- 窗口纪律：本节仍在 W1 / W2 内，`tools/verify_project.py` 与 `ModLangProvider.java:10-16` 禁写；注册面在本节到 **96**，而检查器期望值仍是 **92**（期望红，§12）。

### 76.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-336 | 放置绑定与三出口 | M | 3×3 区块内先核后终端 → 引用缓存非空、核侧名单 **+1**；超 3×3 区块 → 缓存仍空；拆核再放核 → 100 刻内改绑并重新出现 `Kind.TERMINAL` 的连线广播 |
| F-337 | 菜单类型与菜单类 | M | `menu_type` 1 + 1 = **2**、注册面 92 + 4 = **96**；菜单 `slots` 计数 **0**；右键标题读到「控制终端」 |
| F-338 | 控制面板屏幕 | M | 界面 **2** 个按钮 + **1** 行状态；未绑定时两按钮 `active` 均 `false`；点未选中 → `ContainerData` 格 0 由 0 变 1，再点同一个 → 回 0 |

### 76.4 出口判据

1. **E-76-1 注册面到 96**：怎么验：`python -X utf8 tools\verify_project.py` 的 §2。通过的样子：`menu_type` 一行含 `control_terminal`、计数 **2**、注册面合计 **96**（92 + 1 + 1 + 1 + 1）；`EXPECTED_COUNTS` 仍读 **92**，故该行仍 FAIL（窗口期望红，不算 bug）。
2. **E-76-2 放置绑定的两侧读数**：怎么验：3×3 区块内先放核再放终端，读 `/data get block <核坐标> Cultivators` 与终端的 `Core` 键；再在 3×3 区块之外放一台。通过的样子：第一次名单长度 **+1** 且 `Core` 键有值；第二次 `Core` 键缺失（缺键即未绑定）。
3. **E-76-3 补绑与迁移改绑**：怎么验：拆掉核、在原位再放一个核，等满 **100** 刻的一轮补绑。通过的样子：终端 `Core` 键指向新核坐标；连线广播里重新出现该终端，且类型是 `Kind.TERMINAL`。
4. **E-76-4 0 槽菜单的形状**：怎么验：读 `menu/ControlTerminalMenu.java` 的 `addSlot` / `quickMoveStack` / `stillValid` 三处。通过的样子：`addSlot` 命中 **0**；`quickMoveStack` 命中 **1** 且返回 `ItemStack.EMPTY`；`stillValid` 走 `stillValid(this.access, player, ...)` 形态。
5. **E-76-5 屏幕注册与标题**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\client\FarmstormClient.java -Pattern ControlTerminalScreen`，再进游戏右键一台终端。通过的样子：`event.register` 那一行命中 **1**；界面标题读到「控制终端」（漏注册时客户端只有一条 warn、没有界面）。
6. **E-76-6 置灰与三态迁移读数**：怎么验：未绑定时看两个按钮的 `active`；绑定后连点三次，读 `ContainerData` 格 0 与 `/data get block <终端坐标> Mode`。通过的样子：未绑定两按钮 `active` 均为 `false`；绑定后 `Mode` 依次读到 `off` → `cultivator` → `off` → `converter`。
7. **E-76-7 不新增第三个网络包（负向）**：怎么验：`Select-String -Path src -Recurse -Pattern playToClient` 与 `createData` 的格数。通过的样子：`playToClient` 命中仍是 **2**（网络包 2 + 0 = **2**）；`createData` 返回 **2** 格 `ContainerData`，面板状态不走自定义包。

### 76.5 常见翻车点

- **漏 `RegisterMenuScreensEvent` 注册**：客户端只有一条 warn、屏幕类不生效，右键终端看不到界面（RT-99）。预防：E-76-5 的 grep 命中 1 加一次真右键。
- **先写终端自己的缓存、后调 `addCultivator`**：核侧返回 `false` 时终端自称已绑定，`kindOf` / `hasCultivator` / `pruneCultivators` 三条既有不变量全部被绕过。预防：E-76-2 与 E-76-3 的两侧读数必须同时读。
- **面板里加槽或嵌核库存**：等于第二个核存储界面，点击搬运逻辑写两遍（D-273 的否决项）。预防：E-76-4 的 `addSlot` 命中 0。
- **用两个 boolean 表示模式**：会造出「两个都为真」的第四种状态（D-269 的否决项）。预防：E-76-6 的迁移读数只有三个值在轮转。

### 76.6 完成后必须回报什么

- **三处绑定的读数原文**：放置绑定、超 3×3 区块、补绑改绑三组 `/data get` 输出。
- **菜单与屏幕的关键行原文**：`addSlot` 计数、`quickMoveStack` 一行、`event.register` 一行、`createData` 的格数。
- **跑了什么命令、看到什么**：检查器 §2 的注册面合计（**96**）与那一行的期望红说明、`playToClient` 命中数（**2**）。
- **没做的**：面板的视觉排版与按钮位置不在本节验证（66 号 §4.5 给了坐标，验收只读 `active` 与读数）。

---

## §5 Phase 77 · 文案与投料（M73）

### 77.1 本阶段要做什么

补齐面板用的 5 个语言键、把语言键总数收到 **328** 并同步表头口径（W2 在此关窗），再落下服务端每 **20** 刻一轮的投料循环（D-271），准入判据一律借机器自己的闸（D-272）——四条边界各一条判据。本节是**顺序纪律②**的分界：投料循环对史诗四件的验收不在这里做。

### 77.2 前置条件

- Phase 76 已交付：菜单与屏幕在位（E-76-4、E-76-5）、注册面 **96**（E-76-1）。
- 现状读数：语言键 **321 + 2 = 323**（F-331 的两条已落）；`ModLangProvider.java:10-16` 的表头仍写 **321**（实读为 `28 + 28 + 32 + 178 + 13 + 42`），W2 期间这张表禁写，本节由 F-339 一次性改到 **328**（`29 + 29 + 32 + 178 + 13 + 47`）。
- 投料判据只借不改：农作物培养器 `canInsert`（内含 `isUsable()` ∧ 空槽 ∧ `admissible`，`CultivatorBlockEntity.java:152-154`）与农作物转换器 `canInsert`（`CropConverterBlockEntity.java:141-143`）；终端侧不得出现任何物品白名单（D-272、D-282）。
- 节拍是编译期常量 `FEED_INTERVAL_TICKS = 20`，不新增 config 键（D-271）——server config 仍 **175**、client config 仍 **3**。
- **顺序纪律②**：史诗四件（火把花、瓶子草、甜浆果、金苹果）的投料读数归 Phase 79（E-79-2）；本节只取 21 条转换配方里非史诗的 17 条中的投入物做读数。

### 77.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-339 | 面板 5 个语言键 | S | `zh_cn.json` / `en_us.json` 各 323 + 5 = **328** 且键集相等；表头算式 `29 + 29 + 32 + 178 + 13 + 47 = 328` 与实读一致 |
| F-340 | 每 20 刻投料循环 | L | 相邻两次投料的 `gameTime` 差恒 **20**；一轮核 64 → 63、机器槽 **1**；槽非空 / `OFF` / 名额满 / 超范围四种情形 **200** 刻内核库存变化 **0** |

### 77.4 出口判据

1. **E-77-1 语言键到 328 且两文件同键（W2 关窗）**：怎么验：用 `ConvertFrom-Json` 读两份 lang 的顶层键数，再逐条搜 7 个新键名。通过的样子：两份各 **328** 键、键集逐字相同；`item.` / `block.` / `container.` / 两条 `mode.` / 两条 `status.` 七个键名各命中 **1**。
2. **E-77-2 表头口径与实读一致（只改一次）**：怎么验：读 `ModLangProvider.java:10-16` 的六个分组数，再 `git log -p -- src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java` 看该段被改过几次。通过的样子：表头读到 `29 + 29 + 32 + 178 + 13 + 47 = 328`；该段注释只在 F-339 那一个提交里改过（命中 **1** 次）；`git diff` 里 `321` 命中 **0**。
3. **E-77-3 节拍恒 20 刻**：怎么验：切培养模式，核里放 3 组合法投入物，按世界刻记录相邻两次投料的 `gameTime`。通过的样子：差值恒为 **20**；`FEED_INTERVAL_TICKS` 的定义命中 **1** 且声明含 `private static final`；`run/config/farmstorm-server.toml` 键数仍 **175**。
4. **E-77-4 一轮每台 1 个 + 只填空槽**：怎么验：同一场景读核库存与机器槽。通过的样子：核里 64 → **63**、机器槽 **1**；槽非空之后 **200** 刻内核库存变化 **0**（不存在「继续给同一台塞第二个」的路径）。
5. **E-77-5 三条静默边界（`OFF` / 名额满 / 超范围）**：怎么验：三种情形各跑 **200** 刻并读核库存。通过的样子：三种情形核库存变化都是 **0**；`OFF` 情形下 `/data get block <终端坐标> Mode` 读到 `off`。
6. **E-77-6 判据只借不改（负向）**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\blockentity\ControlTerminalBlockEntity.java` 三串。通过的样子：`Items.` 命中 **0**、`CropTier` 命中 **0**、`canInsert(` 命中 **2**（培养器与转换器各一处调用，没有第三份判据）。
7. **E-77-7 史诗输入不进本节验收（顺序纪律②）**：怎么验：在本节的验收记录里搜四个原版物品 id。通过的样子：`minecraft:torchflower` / `minecraft:pitcher_plant` / `minecraft:sweet_berries` / `minecraft:golden_apple` 命中 **0**；这四件的投料读数归 **E-79-2**。

### 77.5 常见翻车点

- **中途改表头（W2）**：注释先说 323、下一任务又变 328，两头对不上，下一轮读不出口径（67 号 §0.3 第 2 条）。预防：E-77-2 的 `git log -p` 命中 1。
- **拿史诗四件验收投料**：Phase 79 之前农作物培养器仍然拒收这四件，读数表现为「投不进去」，会被误判成投料 bug（顺序纪律②，§13 第 6 行）。预防：E-77-7 的四串命中 0。
- **把 20 刻做成 config 键**：server config 由 175 变 176，五份文档的数字全部要跟着改，且检查器的键集口径也要动（D-271）。预防：E-77-3 的 toml 键数 **175**。
- **终端自建白名单**：判据变两处，数据包加一条转换配方时两边不同步（D-282）。预防：E-77-6 的 `CropTier` 与 `Items.` 命中 0。

### 77.6 完成后必须回报什么

- **两份 lang 的键数（各 328）与表头原文**：六个分组数逐项贴，附 `git log -p` 里该段被改的次数。
- **投料读数**：相邻两次 `gameTime` 差、核库存前后值、机器槽值；三条静默边界各自的 200 刻读数（各 0 变化）。
- **两处「没变的数」**：toml 键数 **175**、`playToClient` 命中 **2**（本节不新增网络包）。
- **检查器读数**：注册面 **96**、期望值仍 **92** ⇒ §2 一行期望红，`tools/verify_project.py` diff 命中 0 行。
- **没做的**：史诗四件的投料读数归 Phase 79（E-79-2），本节不写这四条。

---

## §6 Phase 78 · 可挖门槛（M74）

### 78.1 本阶段要做什么

把三个处理方块补进 `#minecraft:mineable/pickaxe`（管挖速），并新增一条 `#minecraft:needs_stone_tool`（管算不算正确工具），成员恰为农作物培养器、农作物转换器、控制终端三者（D-278、D-279）。金镐落在哪一档只记录不断言（D-280、A-117）。这一节顺手盖住一个既有缺口：v0.2.7 的农作物培养器既有「正确工具才掉」又不在任何镐类名单里，于是任何工具都挖不出掉落物（D-278 的补充）。

### 78.2 前置条件

- `farmstorm:processing_block` 的 **3** 个成员已在 Phase 75 落地（E-75-7），本节不改那三行；控制终端方块在 Phase 74 已注册（E-74-3）。
- 现状读数（实读）：`ModBlockTagsProvider.java:49-63` 的 `MINEABLE_WITH_PICKAXE` 现有 **10** 个成员、缺农作物培养器与控制终端；`:67` 是 `NEEDS_DIAMOND_TOOL` 的一行写法，新增一行照它。
- 两条标签分工不同：`mineable/pickaxe` 管**挖速**、`needs_stone_tool` 管**算不算正确工具**；只加前一条，木镐挖得慢但仍然掉（D-279 的补充）。
- 金镐不进断言：原版由 `incorrect_for_gold_tool` 决定，本仓读不出线上真值（D-280、RT-98）；本节只记录读数。
- 三个合成配方两两不同：本节取 `GIG` / `IRI` / `GIG`，农作物培养器是 `IGI` / `GRG` / `IGI`、农作物转换器是 `III` / `GRG` / `III`（A-115 的三者两两不同）。
- 窗口纪律：本节仍在 W1 内，`tools/verify_project.py` 禁写（§12）。

### 78.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-341 | 两条挖掘标签 | S | `mineable/pickaxe` 10 + 2 = **12** 成员；`needs_stone_tool` **3** 成员；木镐挖农作物培养器掉落 **0**、石镐挖掉落 **1** |

### 78.4 出口判据

1. **E-78-1 pickaxe 成员 12**：怎么验：`Get-Content src\generated\resources\data\minecraft\tags\block\mineable\pickaxe.json -Raw` 数 `values`。通过的样子：**12** 项（10 + 2），新增两项是 `farmstorm:cultivator` 与 `farmstorm:control_terminal`。
2. **E-78-2 needs_stone_tool 成员 3**：怎么验：`Get-Content src\generated\resources\data\minecraft\tags\block\needs_stone_tool.json -Raw`。通过的样子：文件存在、`values` **3** 项，恰为 `farmstorm:cultivator` / `farmstorm:crop_converter` / `farmstorm:control_terminal`。
3. **E-78-3 木镐 0 掉落**：怎么验：木镐挖一台农作物培养器、一台农作物转换器、一台控制终端，各数掉落物。通过的样子：三次掉落都是 **0**（方块仍被移除，判据是掉落物计数，不是「挖不动」）。
4. **E-78-4 石镐 1 个**：怎么验：石镐挖同样三块。通过的样子：三次掉落都是 **1** 个对应方块物品。
5. **E-78-5 金镐只记录（不断言）**：怎么验：金镐挖同样三块，把读数写进回报。通过的样子：读数表里金镐那一行**不含** PASS / FAIL 判定（A-117、RT-98）；三个读数只作记录。
6. **E-78-6 既有缺口已盖住（同读）**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\registry\FarmstormBlocks.java -Pattern requiresCorrectToolForDrops` 与 E-78-3 / E-78-4 的两组读数同读。通过的样子：培养器那条 `requiresCorrectToolForDrops` 命中 **1**（七项之一），而「任何工具都不掉」的旧表现不再可复现——这是 D-278 顺手盖住的既有缺口，方向是「从无到有」。
7. **E-78-7 config 与注册面不动（负向）**：怎么验：检查器末段与 toml 键数。通过的样子：server config **175**、client config **3**、注册面合计仍 **96**；`git diff --stat` 里 Java 侧只有 `ModBlockTagsProvider.java` 一个文件。

### 78.5 常见翻车点

- **只加 `mineable/pickaxe` 不加 `needs_stone_tool`**：木镐挖得慢但照样掉，「石镐及以上」没有落地（D-279）。预防：E-78-3 的三次 0。
- **只加 `needs_stone_tool` 不加 pickaxe**：挖速仍停在徒手级，玩家拿到的是「掉但挖很久」。预防：E-78-1 的 12。
- **把金镐写成断言**：断言可能与原版 `incorrect_for_gold_tool` 的真值相反，变成必红条目（RT-98）。预防：E-78-5 的读数行不含判定。
- **顺手再改一遍 `processing_block` 那三行**：它们已在 Phase 75 落地（E-75-7），本节 diff 里出现即说明重复改动，两处的成员集合容易走岔。预防：E-78-2 与 E-75-7 的交叉读数。

### 78.6 完成后必须回报什么

- **两份标签 JSON 的 `values` 条数与逐条内容**：12 与 3 各一份原文。
- **3 块 × 3 种镐的 9 个掉落读数**：木镐 / 石镐两组带判定，金镐那 3 个只记录。
- **跑了什么命令、看到什么**：检查器末段原文、toml 键数（**175** / **3**）、`git diff --stat` 原文。
- **没做的**：金镐算不算「石镐及以上」不由本节判定（D-280、64 号 §8 第 1 条）。

---

## §7 Phase 79 · 史诗准入（M75）

### 79.1 本阶段要做什么

把农作物培养器的准入闸从「命中转换配方且输出不是史诗档」改成「命中转换配方」，让火把花、瓶子草、甜浆果、金苹果四个原版物品自此可进培养器（D-281 的 4 条全开，裁定 #7），并改写 `CropTier` 里那句已作废的档位口径（54 号 D-162 进墓碑）。产出面不动：批次公式与倍率一个字不改（D-283）。这一节与终端线无依赖，可与 Phase 78 并行（67 号 §5.1 的 B 线）。

### 79.2 前置条件

- 判据只有一处（实读）：`CultivatorBlockEntity.java:143-149`，第 `:148` 行是 `return output != null && output.tier() != CropTier.EPIC;`——本节只删 `&&` 之后那半句，方法体其余行不动。
- 类注释同步：`util/CropTier.java:24` 那句「史诗：4 条，本版唯一的入口是农民交易（D-162 把它们挡在培养器外）」在本版作废，改写措辞照 66 号 §2.5。
- 四条的名与档位来源（实读）：激活的火把花（`farmstorm:awakened_torchflower`）在 `CropProfile.java:73-75`、激活的瓶子草（`farmstorm:awakened_pitcher_plant`）在 `:76-78`、幸运浆果（`farmstorm:lucky_berry`）在 `:88-89`、狂野苹果（`farmstorm:wild_apple`）在 `:102-105`；配方行是 `ModRecipeProvider.java:68` / `:69-70` / `:76` / `:80`（21 条 `oneWay` 在 `:59-85`）。
- 南瓜不在四条里：man瓜（`farmstorm:pumpkin_turret`）是普通档（`CropProfile.java:69-72`），基线就能进培养器；把它写进四条会得到一条必红读数（RT-97）。

### 79.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-342 | 史诗准入放行 | S | 火把花 / 瓶子草 / 甜浆果 / 金苹果四件各成功投入 **1** 次；`CropTier.EPIC` 在 `CultivatorBlockEntity.java` 命中 **0**；`Items.` 在该文件命中 **0** |

### 79.4 出口判据

1. **E-79-1 比较整条删除、白名单不出现（负向）**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\blockentity\CultivatorBlockEntity.java -Pattern CropTier.EPIC` 与 `-Pattern Items.`。通过的样子：前者命中 **0**、后者命中 **0**；`:143-149` 的方法体仍是一句返回，没有新增分支。
2. **E-79-2 四件各成功投入 1 次**：怎么验：手持 `minecraft:torchflower` / `minecraft:pitcher_plant` / `minecraft:sweet_berries` / `minecraft:golden_apple` 各右键一次农作物培养器，每次读 `/data get block`。通过的样子：四次都读到投入槽非空，一个都不被拒；这一组读数只在本节出现（E-77-7 已把本节之外的那份读数封到 0）。
3. **E-79-3 产出面不动（负向）**：怎么验：`git diff` 看 `batch` 段与产出倍率，再让一次投入结算并读核里产物的数量。通过的样子：`batch` 段 diff 命中 **0** 行；产物数量仍等于配置的产出数量（默认 2）与对应种子组的和，不因放行而改倍率。
4. **E-79-4 旧口径注释改写**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\util\CropTier.java -Pattern D-162`。通过的样子：命中 **0**（该句作废）；`CropTier.java:24` 现文提到 v0.3.0 与放行，`EPIC` 的枚举值个数仍是 **4** 条作物。
5. **E-79-5 配方面不动（负向）**：怎么验：数 `data/farmstorm/recipe` 与 `oneWay` 调用。通过的样子：转换配方仍 **21** 条、配方 JSON 在 Phase 81 之前仍 **25**（21 + 4）；本节不加任何配方。
6. **E-79-6 构建与检查器类别不扩**：怎么验：跑一次构建与 `python -X utf8 tools\verify_project.py`。通过的样子：构建退出码 **0**；检查器的 FAIL 只出现在 §2 的注册面计数（W1 期望红），配置面与语言面 FAIL 命中 **0**。

### 79.5 常见翻车点

- **顺手在培养器里加一份史诗白名单**：判据变两处，数据包加配方时两边不同步（D-282、64 号 §6 第 10 项）。预防：E-79-1 的 `Items.` 命中 0。
- **把南瓜算进第四条**：南瓜一直是普通档、基线就能进培养器，照它写的断言会与实测相反（RT-97）。预防：E-79-2 只取四个原版 id，`minecraft:pumpkin` 命中 0。
- **为了平衡给史诗投入物折半倍率**：等于在准入闸旁边再造一处按档位分支的逻辑，与 D-282 冲突（D-283 的否决项）。预防：E-79-3 的 `batch` 段 diff 0 行。
- **保留比较但改判据（例如改成 `!= EPIC || ...`）**：比较还在，只是从「整条删除」退化成「带例外的比较」，下一轮读不出判据只有一个来源。预防：E-79-1 的命中 0。

### 79.6 完成后必须回报什么

- **`admissible` 方法体的完整原文**（改后）与 `git diff` 的 `-1` / `+1` 行数。
- **四件各一次的实际读数**：投入槽读数、结算后核里产物数量。
- **旧口径改写后的 `CropTier.java:24` 一行原文**。
- **没做的**：产出倍率与平衡问题不由本节调整（D-283）；这四条作物的量产入口在 Phase 79 之后才成立。

---

## §8 Phase 80 · 干草块掉落（M76）

### 80.1 本阶段要做什么

新建一份服务端存档记录（`util/PuffedHayData`，`extends SavedData`，D-284），在膨胀麦把干草块真正写进那一格之后登记坐标（D-285），再在既有事件类里加第 **10** 个监听：位置命中记录时清空掉落并移除该条（D-286）。三件事都在既有类与既有 Provider 里落，**不新建事件处理类**（A-118）——那会把 Java 文件从 **156** 推到 157。

### 80.2 前置条件

- 写入点（实读）：`entity/thrown/PuffedWheatProjectile.java:104-123` 的 `fillHay`；`:113-115` 是爆炸抗性跳过、`:116-118` 是「非空气非流体」跳过、`:119` 是 `level.setBlock(pos, hay, Block.UPDATE_ALL)`。登记点在这三条之后。
- 压缩膨胀麦复用同一个 `fillHay`，只覆写 `fillBlock()`，写入的是压缩干草块；不加 `hay.is(Blocks.HAY_BLOCK)` 这道判断，压缩干草块也会被登记成「该不掉落」（RT-93）。
- 消费点：`event/FarmstormGameEvents.java:30` 的类注释现写「只做九件事」，第 10 个 `addListener` 接在既有 `register` 的最后一行之后（66 号 §4.7 第 4 步）。
- 现成先例：`outpost/OutpostCoreIndex.java:48`（`extends SavedData`）、`trap/TrapFifoIndex.java:42`（`BRIEF` §1 需求 2.3）。
- 窗口纪律：本节仍在 W1 内，`tools/verify_project.py` 禁写（§12）。

### 80.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-343 | 干草块位置记录 | M | `world/data/farmstorm_puffed_hay.dat` 存在；重启后 `positions` 里仍是同一批 `long`（条数不变）；Java 文件 151 + 5 = **156** |
| F-344 | 落点登记 | S | 掷一次膨胀麦：记录条数 = 实际写入格数；被抗性挡掉的格子不在集合；压缩膨胀麦掷出后条数变化 **0** |
| F-345 | 破坏时清掉落 | M | 监听数 9 + 1 = **10**；挖记录内干草块掉落 **0** 且条数 **−1**；挖玩家手放干草块掉落 **1** |

### 80.4 出口判据

1. **E-80-1 记录落盘与跨重启**：怎么验：首次写入后 `Test-Path run/world/data/farmstorm_puffed_hay.dat`，重启服务器再读 `positions` 的长度与内容。通过的样子：文件存在；两次读到的条数相等且 ≥ **1**，内容逐条相同。
2. **E-80-2 登记数 = 实写格数**：怎么验：在草地上掷一次膨胀麦，数落点实际写入的干草块格数与记录条数。通过的样子：两数相等；被爆炸抗性挡掉的格子不在集合里（跳过分支在前、登记在后）。
3. **E-80-3 压缩膨胀麦不登记（RT-93）**：怎么验：掷一次压缩膨胀麦，前后各读一次记录条数。通过的样子：条数变化 **0**；落点那一片方块是 `farmstorm:compressed_hay_block`。
4. **E-80-4 清掉落与移记录（两侧读数）**：怎么验：石镐挖记录内的一格，再挖一格玩家手放的干草块。通过的样子：第一次掉落 **0** 个且条数 **−1**；第二次掉落 **1** 个且条数不变。
5. **E-80-5 事件数 10 与类注释**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\event\FarmstormGameEvents.java` 五串各数一次：`十件事` / `九件事` / `BlockDropsEvent` / `getDrops().clear()` / `setCanceled`。通过的样子：依次读到 **1 / 0 / 1 / 1 / 0**。
6. **E-80-6 判据同时含 state 与坐标（负向）**：怎么验：把记录内的一格先用 `/setblock` 换成别的方块再拆，读掉落；再读监听里的两个条件。通过的样子：换过方块的那一格掉落 **1** 个（不是 0）；`getState().is(Blocks.HAY_BLOCK)` 与 `getPos().asLong()` 两个条件各命中 **1**。
7. **E-80-7 构建与 Java 计数**：怎么验：跑一次构建，再 `(Get-ChildItem src\main\java -Recurse -Filter *.java).Count`。通过的样子：退出码 **0**；计数 **156**（151 + 5，五个新类在本节收齐最后一个）。

### 80.5 常见翻车点

- **登记点放在两个跳过分支之前**：被抗性挡掉的格子与原本有方块的格子也会被记成「该不掉落」，拆了照旧不掉（D-285、红线 6）。预防：E-80-2 的两数相等。
- **用 `setCanceled(true)` 代替 `getDrops().clear()`**：连经验与 `spawnAfterBreak` 一起掐掉，改动面比需要的更大（D-286 的补充）。预防：E-80-5 的 `setCanceled` 命中 0。
- **只判坐标不判 state**：同一格换成别的方块再拆，掉落也被清空。预防：E-80-6 的换方块读数。
- **新建一个 `event/PuffedHayDropHandler.java`**：Java 从 **156** 变 157，与 D-258 冻结的数字打架（A-118）。预防：E-80-7 的计数 156。

### 80.6 完成后必须回报什么

- **记录文件的路径与两次读数**：`.dat` 的 `Test-Path` 结果、重启前后的条数。
- **三组行为读数**：一次膨胀麦的「实写格数 / 记录条数」、一次压缩膨胀麦的前后条数、挖两格的掉落数与条数。
- **五串 grep 的命中数**（1 / 0 / 1 / 1 / 0）与 `.java` 计数（**156**）。
- **没做的**：老存档里已经存在的落点干草块不在记录里、照旧掉落（D-285 的已知影响），本节不回溯扫描。

---

## §9 Phase 81 · 配方与战利品（M77）

### 81.1 本阶段要做什么

给控制终端一条与两台机器都不同的 3×3 合成配方（`GIG` / `IRI` / `GIG`：四角玻璃、四边铁锭、正中红石，D-268），再补一张单物品战利品表（战利品表 28 + 1 = **29**）。两件都在既有 Provider 里加，不新建 Provider，也不新增注册项。

### 81.2 前置条件

- 配方先例（实读）：农作物培养器是 `ModRecipeProvider.java:119-129` 的 `IGI` / `GRG` / `IGI`（四角铁、四边玻璃、正中红石）；剥掉解锁进度的助手在 `:176-193`。
- 战利品表先例（实读）：`ModLootTableProvider.java:138-142` 的转换器与农作物培养器两张单物品表；`getKnownBlocks()` 漏登记会在 `validate` 阶段报缺表、构建失败。
- 现状读数：配方 JSON **25**（21 转换 + 4 合成）、合成配方 **4**、方块战利品表 **28**、进度 **16**。
- 图样不能与培养器逐格相同：同料同图样的两条 `crafting_shaped` 共存时，合成台只稳定命中其中一条（D-268 的补充）。
- 窗口纪律：本节仍在 W1 内，`tools/verify_project.py` 禁写（§12）。

### 81.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-346 | 终端合成配方 | S | 配方 JSON 25 + 1 = **26**、合成配方 4 + 1 = **5**；`GIG` / `IRI` / `GIG` 三个 pattern 各命中 **1**；`IGI` / `GRG` / `IGI` 仍各命中 **1** |
| F-347 | 终端战利品表 | S | 方块战利品表 28 + 1 = **29**；石镐挖终端掉落 **1** 个；`getKnownBlocks()` 里 `CONTROL_TERMINAL` 命中 **1** |

### 81.4 出口判据

1. **E-81-1 配方 JSON 26 与图样**：怎么验：数 `src\generated\resources\data\farmstorm\recipe` 的文件数与 `Select-String -Path src\main\java\com\dashpig\farmstorm\datagen\ModRecipeProvider.java -Pattern pattern`。通过的样子：**26** 个 JSON（21 + 5）；`GIG` / `IRI` / `GIG` 三个 pattern 各命中 **1**，产物是控制终端、数量 **1**。

> **〔v0.3.0 实施期变更 · 作者 2026-11-20 追改〕**
> ~~原文：`GIG` / `IRI` / `GIG` 三个 pattern 各命中 1~~
> 现口径（C22）：`IGI` / `IRI` / `IQI` 三个 pattern 各命中 1（`IGI` 在文件里另有一处属培养器的 pattern 用法，故按「控制终端方法体内的三行」计）；配方 JSON 仍 **26**、产物仍控制终端 × 1。E-81-2 的对照也改成「按 `IGI/IRI/IQI` 摆出终端、按 `IGI/GRG/IGI` 摆出培养器」。
2. **E-81-2 两条配方不串**：怎么验：按两种图样各合成一次。通过的样子：`GIG` / `IRI` / `GIG` 摆出 **1** 个控制终端；`IGI` / `GRG` / `IGI` 摆出 **1** 个农作物培养器；两次产物互不串。
3. **E-81-3 进度仍 16（负向）**：怎么验：数 `src\generated\resources\data\farmstorm\advancement`。通过的样子：**16** 个 JSON（16 + 0 = **16**）——合成配方的解锁进度被 `recipeWithoutAdvancement` 剥掉。
4. **E-81-4 战利品表 29 与掉落**：怎么验：数 `src\generated\resources\data\farmstorm\loot_table\blocks`，再用石镐挖一台控制终端。通过的样子：**29** 张（28 + 1）；掉落 **1** 个控制终端物品。
5. **E-81-5 `getKnownBlocks()` 登记（负向）**：怎么验：`Select-String -Path src\main\java\com\dashpig\farmstorm\datagen\ModLootTableProvider.java -Pattern CONTROL_TERMINAL` 并跑一次构建。通过的样子：`generate()` 与 `getKnownBlocks()` 两处各命中 **1**；构建退出码 **0**（漏登记会在这里报缺表）。
6. **E-81-6 注册面不扩（负向）**：怎么验：检查器 §2 的合计与合成配方计数同读。通过的样子：注册面合计仍 **96**（这一节不加任何注册项）；合成配方 4 + 1 = **5**。

### 81.5 常见翻车点

- **图样照抄培养器**：两条同料同图样的配方共存，做出培养器之后就再也做不出终端（D-268 的定稿理由）。预防：E-81-1 的六个 pattern 读数与 E-81-2 的两次合成。
- **忘了 `recipeWithoutAdvancement`**：进度从 16 变 17，D-258 的冻结值当场破。预防：E-81-3 的计数 16。
- **`getKnownBlocks()` 漏登记**：方块有战利品表文件但校验报缺表，构建在 validate 阶段失败。预防：E-81-5 的两处各 1 加构建退出码 0。
- **把合成配方当成注册项去改检查器**：合成配方不在注册面十二类里，它只进配方 JSON 计数；本节动 `tools/verify_project.py` 会让 W1 提前关窗。预防：E-81-6 的注册面仍 96。

### 81.6 完成后必须回报什么

- **六个 pattern 的原文与产物行**，以及两次实际合成的产物名称与数量。
- **四个计数**：配方 JSON **26**、合成配方 **5**、战利品表 **29**、进度 **16**。
- **跑了什么命令、看到什么**：构建退出码、检查器 §2 的注册面合计（**96**）、`getKnownBlocks()` 的两处命中。
- **没做的**：合成配方的平衡与观感不在本节验证，本节只读产物与图样。

---

## §10 Phase 82 · 检查器口径（M78）

### 82.1 本阶段要做什么

把检查器的期望值抬到 **96**：`EXPECTED_COUNTS` 的四类同改、4 项新 ID 逐个补进清单、`:453-457` 的算式注释同步——三项**同一个提交**（D-289）。本节是 W1 的**关窗任务**：此前 Phase 74 ~ 81 的期望红在这一节结束，此后任何 FAIL 都按缺陷处理。

### 82.2 前置条件

- 四个新注册项全部落地：`blocks` / `items`（Phase 74，E-74-3）、`block_entity_type`（Phase 75，E-75-1）、`menu_type`（Phase 76，E-76-1）；注册面实读 **96**。
- 检查器三处位置（实读）：`:408-450` 的 `EXPECTED` ID 清单（`items` 在 `:409-417`、`blocks` 在 `:418-430`、`block_entity_type` 在 `:431-436`、`menu_type` 在 `:450`）、`:453-457` 的算式注释、`:458-471` 的 `EXPECTED_COUNTS`（四类现值 28 / 28 / 8 / 1）。
- 检查器**不数** Java 文件数：**156** 只出现在文档与 `Get-ChildItem` 计数里，改它不会让检查器变红（`BRIEF` §7 第 7 条）。
- 另外八类计数一个不动：`entity_type` 13、`mob_effect` 4、`sound_event` 3、`recipe_type` 1、`recipe_serializer` 1、`creative_mode_tab` 1、`damage_type` 3、`data_component_type` 1（`BRIEF` §0 的十二类逐项）。
- 窗口纪律：本节是 W1 关窗点，三项必须一次改完；改一半会留下「数对但 ID 缺」或「ID 齐但数错」的区间（D-289 的补充）。

### 82.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-348 | 检查器口径 96 | S | `EXPECTED_COUNTS` 四类读 29 / 29 / 9 / 2、合计 **96**；4 个新 ID 逐字各命中 **1**；检查器 **FAIL 0 / WARN 1**、§2 打出 **96 / 96** |

### 82.4 出口判据

1. **E-82-1 `EXPECTED_COUNTS` 四类同改**：怎么验：读 `tools/verify_project.py:458-471`。通过的样子：`items` **29**、`blocks` **29**、`block_entity_type` **9**、`menu_type` **2**，十二项相加 **96**（29 + 29 + 9 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 2）。
2. **E-82-2 四个新 ID 逐字命中**：怎么验：`Select-String -Path tools\verify_project.py -Pattern control_terminal`。通过的样子：命中 **4** 行，分别落在 `items` / `blocks` / `block_entity_type` / `menu_type` 四个清单里，各有 1 条。
3. **E-82-3 算式注释同步**：怎么验：读 `:453-457`。通过的样子：末句合计读 **96**；该段里把 `92` 当合计的写法命中 **0**。
4. **E-82-4 全绿（关窗读数）**：怎么验：`python -X utf8 tools\verify_project.py`。通过的样子：**FAIL 0 / WARN 1**；§2 打出「已核 registry 对象 **96 / 96**」。
5. **E-82-5 三项同一提交（负向）**：怎么验：`git show --stat` 与 `git show --name-only`。通过的样子：一个提交里同时含 `EXPECTED_COUNTS` 的四行、四个 ID 的四行、注释一段；该提交的路径里 `src/` 命中 **0**。

> **〔v0.3.0 实施期更正 · F-348 · 依据作者裁定 Q2=A〕**
> ~~原文：该提交的路径里 `src/` 命中 **0**~~
> 现口径（C4）：该提交的路径里 `src/` 命中 **1**，且必须是 `src/main/java/com/dashpig/farmstorm/Farmstorm.java`。理由：64 号 D-289 / 67 号 A-119 / 68 号 HC-189 都要求运行时启动自检与检查器**同批**收口；只改 `tools/` 会留下「日志 FAIL、检查器全绿」的区间。实际提交 `7ad45ae` 的路径恰为这两个文件（`git show --name-only` 两行）。
6. **E-82-6 关窗后的规矩**：怎么验：再跑一次检查器并把末段落进回报。通过的样子：从此不再有「期望红」的例外；`WARN` 只允许是基线那 **1** 条，多一条即按缺陷处理。

### 82.5 常见翻车点

- **只改计数不改 ID 清单**：检查器数对、ID 缺项不报，下一轮的「4 个新 ID」判据失去读数（D-289）。预防：E-82-2 的四行。
- **忘改 `:453-457` 的算式注释**：下一轮读者按 92 复算，与 96 的读数对不上（`BRIEF` §7 第 7 条）。预防：E-82-3 的两读数。
- **拆成两个提交**：中间态要么「数对 ID 缺」、要么「ID 齐数错」，两种都不报错（D-289 的中间态窗口）。预防：E-82-5 的单提交判据。
- **顺手去清那条 WARN**：基线那 **1** 条 WARN 不是本节的目标，动它要改另一段判据，与 D-289 的三项无关。预防：E-82-4 只要求 FAIL **0**。

### 82.6 完成后必须回报什么

- **`EXPECTED_COUNTS` 的完整原文**与四行 ID 清单的新增行原文。
- **检查器末段原文**：`FAIL 0 / WARN 1` 与 `96 / 96` 两行。
- **一个提交的 `git show --stat` 原文**（含 `src/` 命中 0 的判据）。
- **没做的**：Java 计数 **156** 不在本节改，也不在检查器里核（`BRIEF` §7 第 7 条）。

---

## §11 Phase 83 · 收尾（M79）

### 83.1 本阶段要做什么

更新 `README.md` 的版本行与文档索引（64 ~ 69 六份），写出交付清单（行数 / 字节 / SHA256），并对 D-258 的冻结数字做一次终态复核。这一节只改 `README.md` 与交付清单：`src/`、`tools/`、`docs/zh/64` ~ `docs/zh/67` 一行不动（只引不改）。

### 83.2 前置条件

- F-329 ~ F-348 全部落地；检查器 **FAIL 0 / WARN 1**（E-82-4），W1 与 W2 都已关窗。
- 六份文档在 `docs/zh/`：`64-DECISIONS-0.3.0.md` ~ `69-PROMPT-TASKS-0.3.0.md`，`Test-Path` 六个都为 `True`。
- 交付清单三列都要有值：行数 / 字节 / SHA256（67 号 §2 的 F-349 交付物）。
- 数字面终态：注册面 **96**、server config **175**、client config **3**、语言键 **328**、Java **156**、网络包 **2**、协议版本 `"4"`、Mixin **0**、战利品表 **29**、转换配方 **21**、合成配方 **5**、进度 **16**、方块模型 **79**、PNG **88**、mcmeta **8**、文件总数 **96**。

### 83.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-349 | README 与交付清单 | S | `README.md` 里 64 ~ 69 六个文件名各命中 **1**；版本行含 `0.3.0`；检查器仍 **FAIL 0**；交付清单六行（行数 / 字节 / SHA256 三列都有值） |

### 83.4 出口判据

1. **E-83-1 README 六个文件名**：怎么验：`Select-String -Path README.md -Pattern 64-DECISIONS-0.3.0.md`，再对另外五个文件名各跑一次。通过的样子：六个文件名各命中 **1**；版本行含 `0.3.0`，且不再把 `0.2.7` 写成现行版本。
2. **E-83-2 交付清单六行齐全**：怎么验：读交付清单。通过的样子：六份文档各一行，行数 / 字节 / SHA256 三列都有值，合计 **6** 行。
3. **E-83-3 数字面终态复核**：怎么验：`python -X utf8 tools\verify_project.py` 加五条计数命令（`.java` / `models/block` / `loot_table/blocks` / `recipe` / `advancement`）。通过的样子：FAIL **0** / WARN 1、**96 / 96**、Java **156**、模型 **79**、战利品表 **29**、配方 **26**、进度 **16**。
4. **E-83-4 贴图三个数终态**：怎么验：`.png` / `.mcmeta` / 两者相加三数各数一次。通过的样子：**88** / **8** / **96**，三个数互不借用（RT-96）。
5. **E-83-5 上游零改动（负向）**：怎么验：`git diff --stat` 四个上游文档路径（`docs/zh/64` ~ `docs/zh/67`）。通过的样子：命中 **0** 行。
6. **E-83-6 本文自检可复核**：怎么验：`Get-Content docs/zh/69-PROMPT-TASKS-0.3.0.md -Tail 1` 与逐节标题计数。通过的样子：末行非空；Phase 节 **11** 个、每节六个子节；E 编号从 **E-73-1** 到 **E-83-6**，段内连续无跳号。

### 83.5 常见翻车点

- **版本行只改一处、索引留旧版**：README 的版本行写 0.3.0 而索引仍指向 0.2.4 那一套（六份文档的入口）。预防：E-83-1 的六个文件名各 1。
- **交付清单缺 SHA256 列或漏一份**：六份文档三列都必须有值，缺一份就少一行。预防：E-83-2 的 6 行。
- **顺手改 `docs/zh/64` ~ `docs/zh/67`**：那四份是上游真相源，改它们等于改判据本身（只引不改）。预防：E-83-5 的 diff 命中 0。
- **在 README 里把 `0.2.7` 整个删掉**：v0.2.7 仍是本版的基线读数来源，删掉后文档里的基线句失去索引；只把「现行版本」换成 `0.3.0`，上一版索引下移保留。预防：E-83-1 的两条读数。

### 83.6 完成后必须回报什么

- **`README.md` 的 diff 原文**与六个文件名的命中行。
- **交付清单的六行原文**（行数 / 字节 / SHA256）。
- **终态数字面**：E-83-3 与 E-83-4 的八个读数逐项贴。
- **没做的**：本版不做英译、不改上游文档、不回写 `run/` 下的老 toml（66 号 §6.2）。

---

## §12 窗口台账

本版两个窗口互不重叠，都在 Phase 74 开：**W1 注册面**（F-331 开 → F-348 关，跨 **18** 个任务 = 348 − 331 + 1）与 **W2 语言键表头**（F-331 开 → F-339 关，跨 **9** 个任务 = 339 − 331 + 1）。窗口内某些断言**期望红**，那不是 bug；窗口内的禁写文件一旦被改，窗口就失去判据。

| Phase | F 号范围 | 任务数 | 前置 Phase | 窗口段 | 可断言 | 禁写 |
|---|---|---|---|---|---|---|
| 73 | F-329 | 1 | —（首节） | 窗口外（两窗均未开） | `0.3.0` 命中 **6**、jar 名成立、注册面 **92**、语言键 **321**、Java **151** | `tools/verify_project.py`、`ModLangProvider.java:10-16`、**94** / **95** / **96** / **323** 四个中间读数 |
| 74 | F-330 ~ F-332 | 3 | 73（E-73-1、E-73-2） | **W1 开（F-331）+ W2 开（F-331）** | PNG **88**、mcmeta **8**、文件总数 **96**、注册面 **94**、模型 **79**、blockstates **29**、语言键 **323** | 同上两文件；**95** / **96** / **328** |
| 75 | F-333 ~ F-335 | 3 | 74（E-74-3、E-74-5） | W1 + W2 内 | 注册面 **95**、`Kind` **3** 值、协议 `"4"`、`KIND_BY_BLOCK` **3** 条、标签 **3** 成员 | 同上两文件；**96** / **328** |
| 76 | F-336 ~ F-338 | 3 | 75（E-75-1、E-75-7） | W1 + W2 内 | 注册面 **96**、`menu_type` **2**、`slots` **0**、`event.register` 命中 **1** | 同上两文件；**328** |
| 77 | F-339 ~ F-340 | 2 | 76（E-76-1、E-76-4） | W1 内；**W2 关窗（F-339）** | 语言键 **328**、表头算式、`gameTime` 差 **20**、四条边界读数 | `tools/verify_project.py`；史诗四件的投料读数（归 Phase 79） |
| 78 | F-341 | 1 | 74（E-74-3） | W1 内 | pickaxe **12**、`needs_stone_tool` **3**、木镐 **0** / 石镐 **1** | `tools/verify_project.py`；金镐的断言 |
| 79 | F-342 | 1 | —（与 78 并行，B 线） | W1 内 | `CropTier.EPIC` 命中 **0**、四件各 **1** 次投入、转换配方 **21**、进度 **16** | `tools/verify_project.py`；`batch` 与产出倍率段 |
| 80 | F-343 ~ F-345 | 3 | —（C 线，与终端线无依赖） | W1 内 | Java **156**、监听 **10**、`.dat` 存在、掉落 **0** / **1** | `tools/verify_project.py`；老存档的回溯扫描 |
| 81 | F-346 ~ F-347 | 2 | 74（E-74-3）、75（E-75-1） | W1 内 | 配方 JSON **26**、合成配方 **5**、战利品表 **29**、进度 **16** | `tools/verify_project.py`；与培养器相同的图样 |
| 82 | F-348 | 1 | 74 ~ 81（四个新注册项 + 全部窗口内任务） | **W1 关窗（F-348）** | FAIL **0** / WARN 1、**96 / 96**、4 个新 ID 各 **1** | `src/` 下全部路径（本节只改 `tools/`） |

> **〔v0.3.0 实施期更正 · F-348 · 依据作者裁定 Q2=A〕**
> ~~上表 Phase 82 行的禁写列：`src/` 下全部路径（本节只改 `tools/`）~~
> 现口径（C4）：本节允许且必须改 `src/main/java/com/dashpig/farmstorm/Farmstorm.java`（运行时启动自检），与 `tools/verify_project.py` 同批提交；其余 `src/` 路径仍禁写。
| 83 | F-349 | 1 | 73 ~ 82（全部出口判据） | 窗口外（两窗均关） | README 六个文件名各 **1**、检查器仍 FAIL **0**、数字面 **16** 项终态 | `docs/zh/64` ~ `docs/zh/67`、`src/`、`tools/` |
| **合计** | **F-329 ~ F-349** | **21** | | W1 = Phase 74 ~ 82（**9** 个 Phase / **18** 个任务）；W2 = Phase 74 ~ 77（**4** 个 Phase / **9** 个任务） | | |

三条读法：

1. **两个窗口各跨哪几个 Phase**：W1 跨 Phase **74、75、76、77、78、79、80、81、82**（11 个 Phase 里的 9 个）；W2 跨 Phase **74、75、76、77**（4 个）。Phase 73 与 Phase 83 在两窗之外。
2. **「禁写」是时序不是禁令**：某一节的禁写项只是在那一节还读不准；到它归属的那一节，它立刻变成必写项（例如 **328** 在 Phase 77 由禁写变必读）。
3. **W1 关窗只有一个任务**：F-348 把 `EXPECTED_COUNTS` 四类同改 + 4 个新 ID + `:453-457` 的注释三项同一提交（E-82-5）；拆开就留下「数对 ID 缺」或「ID 齐数错」的区间，两种都不报错。

---

## §13 跨 Phase 台账

每一行是**一个动作横跨两个以上 Phase**：起点在哪、终点在哪、谁先谁后、违反后当场看到什么。

| 动作 | 起始 Phase | 收尾 Phase | 违反会怎样 |
|---|---|---|---|
| 注册面 92 → **96**（四个新注册项） | Phase 74（F-331，`blocks` / `items` 各 +1 ⇒ 94） | Phase 82（F-348，`EXPECTED_COUNTS` 改到 96） | 中途改检查器 ⇒ 96 提前绿，Phase 75 ~ 81 的每一步都「已绿」，四个新 ID 缺项不报；读者也分不清「数对」与「ID 齐」（D-289、§12 第 3 条） |
| 语言键 321 → **328**（7 键） | Phase 74（F-331，item / block 两条 ⇒ 323） | Phase 77（F-339，panel 五条 ⇒ 328 并同步表头） | 中途改表头 ⇒ 注释先说 323、下一任务又变 328，两头对不上；键数与表头算式两组读数互不相等（67 号 §0.3 第 2 条、A-114） |
| Java 文件 151 → **156**（五个新类） | Phase 74（F-331 建 `block/ControlTerminalBlock.java`，只带朝向属性） | Phase 80（F-343 建 `util/PuffedHayData.java`，第五个） | 多开一个类 ⇒ 157，与 D-258 冻结值打架（A-118）；少开一个 ⇒ 计数 155，`git log` 里查不出少的是哪一个 |
| 协议版本 3 → **"4"** | Phase 75（F-334，`Kind.TERMINAL(2)` 与版本串同提交） | Phase 75（同一提交内收尾） | 先升版本后加枚举 ⇒ 版本号与 `Kind` 值数对不上；先加枚举不升版本 ⇒ 旧客户端收到 id = 2 时 `byId` 回落成青线，握手阶段拦不住跨版本联机（D-274、D-276） |
| `KIND_BY_BLOCK` 与 `processing_block` 标签同改 | Phase 75（F-335 一个提交改两处） | Phase 75（同一提交内收尾） | 只改标签 ⇒ 终端成为候选但取不到 kind，「被发现、不绑定」；只改表 ⇒ `rebindNearby` 的候选过滤扫不到它。两种半边改动都不报错（D-263、红线 1） |
| **顺序纪律①**：`Kind.TERMINAL` 先于 `KIND_BY_BLOCK` | Phase 75（F-334） | Phase 75（F-335） | `Map.of` 引用不到枚举成员，`cannot find symbol`、编译不过——这是本版唯一「漏改必报错」处（E-75-7、66 号 §5.2） |
| **顺序纪律②**：投料循环的史诗输入判据来自 Phase 79 的史诗放行 | Phase 77（F-340，投料循环本身；史诗四件的读数推到 Phase 79） | Phase 79（F-342 放行后，E-79-2 的四条读数才可能成立） | 顺序反了（在 Phase 79 之前拿史诗四件验收投料）⇒ 农作物培养器仍拒收，读数表现为「投不进去」，被误判成投料 bug 并去改投料代码（E-77-7、E-79-2） |
| 战利品表 28 → **29** | Phase 74（F-331 注册方块） | Phase 81（F-347 补表） | 先补表后注册 ⇒ `validate` 报未知方块；先注册不补表 ⇒ 方块存在但 `getKnownBlocks()` 缺项，构建在 validate 阶段失败（F-347） |
| 语言键表头与检查器两条读数 | Phase 77（F-339，关 W2） | Phase 82（F-348，关 W1） | 两个窗口合并处理 ⇒ 被迫在「改检查器」与「改表头」二选一，选错一边的窗口永远读不准（A-113） |

四条读法：

1. **第 6、7 行是两条顺序纪律**，本节只登记先后；落点判据在 E-75-7 与 E-77-7。
2. **第 4、5 行是「同一 Phase 内收尾」型**：横跨的是两个任务而不是两个 Phase，登记在这里是因为它们的违反后果比跨 Phase 型更早暴露（编译不过、半边静默）。
3. **第 2 行的终点在 Phase 77 关窗**：F-339 之后 `ModLangProvider.java:10-16` 解禁，但**再改它**就是真缺陷。
4. **第 1 行的跨度在本文按 §2 的分组读**；按 67 号 §1 的 Phase 列会读作 74 → 83，两读的差异见 §14 第 2 条。

---

## §14 上游登记与本文自检

### 14.1 上游登记（四处，本文按「处置」列写，不改上游文件）

| # | 项 | 上游现状 | 本文处置 |
|---|---|---|---|
| 1 | 67 号 §1 任务总表的 Phase 列 | F-341 ~ F-349 记作 Phase **79 ~ 83**，整篇不出现 Phase **78**；§6 自检却写「Phase 73 ~ 83，**11** 个，连续」 | 以 §2 的分组为准（73 / 74 / 75 / 76 / 77 / 78 / 79 / 80 / 81 / 82 / 83 各一节），因为按 §1 的列只能凑出 10 个 Phase，与 §6 的 11 个冲突 |
| 2 | 67 号 §2 的分节标题 | 缺「Phase **75** · …（M**71**）」一行，F-333 ~ F-335 三个任务被并进「Phase 74 · 终端资源与方块」标题下 | 把这三个任务归回 Phase **75**（§1 的 Phase 列也记 75）；本文的 §3 就是这一节 |
| 3 | 67 号 §2 的 F-333 交付物 | 点名 `block/ControlTerminalBlock.java`（`newBlockEntity` 与 `getTicker`），而 F-331 要在 `FarmstormBlocks` 里注册方块必须先有该类 | 读作「F-331 建类（只带朝向属性）、F-333 加两个方法」；因此 Java 151 → 156 的起始 Phase 读作 **74**，终点读作 **80**（F-343）——派发单写的 74 → 81 是按 §1 的 Phase 列读出来的 |
| 4 | 贴图的三条数法（RT-96 / A-116） | `textures/block` 可读成 `.png` **84**、`.mcmeta` **5**、文件总数 **89** 三个数 | 本文每条 E 只读一个数并给算式：PNG 84 + 4 = **88**、mcmeta 5 + 3 = **8**、文件总数 89 + 7 = **96**；三个数互不借用（E-74-1、E-83-4） |

### 14.2 本文自检

| 项 | 读数 | 判据 |
|---|---|---|
| 总行数 | **636** | 620 ~ 700 区间内（交接文档 §4.6 的长度参考） |
| Phase 节 | **11** 个（§1 ~ §11，Phase 73 ~ 83） | 连续无跳号，每节六个子节顺序固定 |
| 任务覆盖 | **21** 个（F-329 ~ F-349） | 每个任务恰出现一次：1 + 3 + 3 + 3 + 2 + 1 + 1 + 3 + 2 + 1 + 1 = 21 |
| 出口判据 | **72** 条（E-73-1 ~ E-83-6） | 每段 **5 ~ 8** 条，段内从 1 连续编号：6 + 7 + 7 + 7 + 7 + 7 + 6 + 7 + 6 + 6 + 6 = 72 |
| 窗口 | **2** 个 | W1 跨 18 个任务（Phase 74 ~ 82）、W2 跨 9 个任务（Phase 74 ~ 77） |
| 跨 Phase 台账 | **9** 行 | 含两条顺序纪律（`Kind.TERMINAL` 先于 `KIND_BY_BLOCK`、史诗放行先于投料验收） |
| 数字来源 | **16** 项（D-258 十二项 + `BRIEF` §3 四个资源计数） | 中间读数一律带算式，无自造数值 |
| 禁用词 | **0** | 交接文档 §6 第 3 条与第 9 条点名的八个词逐词搜一遍，全部命中 **0**（本文不列词表） |
| 表格与围栏 | 每张表首尾有竖线、逐行列数一致；本文不启用围栏块 | 末行非空（本行即末行） |
