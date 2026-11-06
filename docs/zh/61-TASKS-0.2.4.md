# 61 号 v0.2.4 任务拆分

> 依据：58 号（**D-225 ~ D-252，28 条**，唯一真相源；写作期新增 **D-253 / D-254**（删 farmland 死代码及其连带改动、取代 A-62），见 A-111）/ 59 号（**4 条需求 R1 ~ R4 + 边界 B-59 ~ B-73（15 条）+ 已知影响 4 条 + 附录 A 14 行**）/ 60 号（§4.1 ~ §4.4 实现规格 / §8 验收 **25 条** / §9 风险 **RT-83 ~ RT-91**）/ `.work/v024-BRIEF.md`（§5 必改清单 **19 行**）。任务 **F-312 起**、Phase **66 起**、里程碑 **M62 起**、裁决 **A-102 起**。
>
> 本文把 58 / 59 / 60 三份规格拆成 **17 个「一次可独立提交、可独立验证」的任务**：任务 **F-312 ~ F-327**（16 条）+ **F-328**（删 farmland 死代码，写作期已落地）、Phase **66 ~ 72**（F-328 归 66，不开新 Phase）、里程碑 **M62 ~ M68**（F-328 归 M62）、裁决 **A-102 ~ A-111**。
>
> 覆盖：动作面 **25 处**（表 A.1，25/25 = 58 号 §3 的 23 处 + F-328 的 2 行删除动作）、60 号 §8 的验收 **25 条**（表 A.2，25/25）、注册面 **92**（表 B）、config **175 + 3**（表 C）、语言键 **321 × 2**（表 D）、风险 **RT-83 ~ RT-91（9 条）**（表 E）、待验证 API **12 条**（表 F）。只引上游：不改 `docs/zh/52` ~ `docs/zh/60`、不改 `.work/v024-BRIEF.md`、不改 `tools/`；只新建本文件。唯一的 `src/` 例外是裁定 #8 的 F-328——它在本文写作期已落地（5 文件、866 行删除、19 行插入），本文只登记、不推动（A-111）。

---
## 0. 阅读约定

### 0.1 任务粒度（五条判据）

一个任务 = 一次可独立提交、可独立验证的改动。五条判据沿用 55 号 §0.1，本版只重述、不改写：

1. **一个任务 = 一次可独立提交、可独立验证的改动。**
2. **提交后构建退出码 0**，且不依赖同批次里尚未提交的任务。
3. **每个任务自带至少一条可观察断言**，断言里必须出现具体数值、键名、路径或日志片段。
4. **回滚该提交不会让其它已完成任务失效**（只影响依赖它的后续任务，依赖写在 §1 的「依赖」列与任务标题行）。
5. **不跨 Phase 边界**：一个任务的交付物全部落在它所属 Phase 的名称范围内。

### 0.2 复杂度换算与本版分布

| 复杂度 | 含义 | 折算人日 |
|---|---|---|
| S | 单文件或纯数据；≤ 1 个类，或 ≤ 3 个文件里的字面量替换 | **0.5** |
| M | 2 ~ 4 个文件，或新增 1 个完整行为实现 | **1.5** |
| L | ≥ 5 个文件，或含状态机 / 序列化 / 渲染管线 | **3.0** |

本版 17 个任务：**S 9 / M 5 / L 3**，折算 `9 × 0.5 + 5 × 1.5 + 3 × 3.0 = 4.5 + 7.5 + 9.0 = 21.0 人日`（不含缓冲；含 25% 缓冲 `21.0 × 1.25 = 26.25`）。比 16 条时多出的 `9.0 − 6.0 = 3.0` 人日（L 组从 `2 × 3.0 = 6.0` 涨到 `3 × 3.0 = 9.0`）就是 F-328 这一条 L。

计时的四条口径：① 凡「编译强制」的改动按它实际牵动的面计、**不许拆**（§0.5 五条死命令）；② F-317 记 **L** 是因为它改的是绑定状态机（60 号 §4.1.8 的四态 + 九条迁移），不是因为文件数；③ 两个只改一行 / 只改注释的任务（F-322、F-323）记 **S**，判据 3 由 grep 命中数兜住（A-110）；④ F-328 记 **L** 走的是判据 1 的「≥ 5 个文件」（`2 + 3 = 5`），不是因为它含状态机——它不改任何注册项、config 键值与语言键。

### 0.3 中间态窗口（本版回答：**无窗口**）

窗口 = 一次改动同时落在**两个不同观察面**上，从「改完 A」到「改完 B」之间存在一段任何总数都不能断言的区间。本版判定**无窗口**，五条读数与出处如下：

| # | 观察面 | 本版读数 | 出处（本文实读） |
|---|---|---|---|
| 1 | Java 文件数 | `152 − 2 + 1 = 151`：152 = v0.2.3 记录基线，−2 = 删 `FarmlandGuard` / `FarmlandLedger`（D-253），+1 = 新建 `blockentity/CoreBound.java`（F-313，D-228） | 实点 `(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` = **150**（F-328 落地后；落地前 152，即 `152 − 2 = 150`）；D-227 |
| 2 | 检查器是否读文件数 | **不读**：`:479-480` 把 `src/main/java/com/dashpig/farmstorm` 下 `*.java` 拼成一个字符串做内容匹配（`all_java = "\n".join(read(f) for f in java.rglob("*.java"))`），无任何计数断言；`:613-622` 数的是 `EXPECTED` 的 ID 清单（`total += len(ids)`），不是文件；`:637-653` 按文件扫六个禁令模式，新文件不含这六个即 PASS | `tools/verify_project.py:479-480`、`:613-622`、`:637-653`（60 号 §1.2 已证同一条） |
| 3 | 运行时注册自检 | **不读文件数**：断言的是 `total == 92` 与十二类逐类相等 | `Farmstorm.java:180-183` |
| 4 | 十二个计数面 | 注册面 **92**、server config **175**、client config **3**、语言键 **321 × 2**、`configuration` 组 **178**、网络包 **2**、进度 **16**、转换配方 **21**、培养器投入物 **17**、交易 listing **26**、Mixin **0** —— 十二个计数面全部 `− 0 + 0`（58 号 §3.3）；代码面另删 2 个 `.java` 文件、`225 + 555 = 780` 行（F-328），两个类不挂注册、不读 config 键、不进语言文件，故十二个计数面的删除项仍是 **0** | 58 号 D-227 全表；60 号 §2 ~ §7 的逐面证明 |
| 5 | xp 分布 | 26 条 listing 会经历半改态（F-320 改植物学家、F-321 改农民，两者不同提交），但**没有任何自检读 xp**：`tools/verify_project.py` 全文 `VILLAGER_XP` 与 `XP_LEVEL` 命中 **0**、`MerchantOffer` / `Offers` 命中 **0**；`Farmstorm.java:180-183` 不碰交易；`ModLangProvider.java:14-16` 的六组语言键不含交易项数值；60 号 §8 第 17 条的读数是人工 `/data get entity <村民> Offers` | 三处实读 grep；60 号 §8 #17 |

**结论**：本版**没有两个观察面**——被改的数字只有文件数一项（`152 − 2 + 1 = 151`，只动这一个数），而它没有任何断言在读；F-328 落地后 `verify_project.py` 复跑仍是 **FAIL 0 / WARN 1**、`92 / 175 / 321` 三段读数一字未变；其余十一项零变化；xp 半改态不进任何闸。**本版没有期望红的断言，任何一个中途 FAIL 都是真 bug**：按缺陷处理，不许当窗口内的期望红，也不许为了「让它绿」提前落地后续任务。

**另立一条界线**：编译强制耦合**不是窗口**（不是两个观察面，是同一提交内必须同时落地的一组改动），登记在 §0.5，不进本节。

### 0.4 本版读数纪律（五条）

- **行号一律 UTF-8 实读**：本文引用的行号逐处按 `read` 工具复核过——`CultivatorBlockEntity:44/66/84/94/104-110`、`CropConverterBlockEntity:40/71/89-92/118-120/129`、`OutpostCoreBlockEntity:604-608/619-643/851-855/866-873/887-889`、`AreaBlockEntities:32-33/39/43-44`、`BotanistTrade:38/41-51/60-82/91`、`TierTrade:37-38/41-47/49-58/79`、`BotanistEpicTrade:28/38-46/64`、`VillagerTradeEvents:44-47/55-57`、`CoreHudRenderer:40-44`、`FarmstormTags:29-30`、`CropConverterBlock:121-122`、`CultivatorBlock:117-118`、`OutpostCoreBlock:67/131-132`、`Farmstorm.java:180-183`、`verify_project.py:458-471/479-480/613-622`、`outpost/OutpostCoreIndex.java:189`（F-328 删掉的那行 javadoc）。
- **判据只用五类**：计数（文件数 / 键数 / 命中数）、日志行、`/data get` 读数、源码 grep 字符串、像素比对；感受类措辞一律不进断言。
- **数字只做加减法**：每个数给算式（`152 − 2 + 1 = 151`、`92 − 0 + 0`、`9 × 0.5 + 5 × 1.5 + 3 × 3.0 = 21.0`）；文件数一律写「基线 − 本版删除 + 本版新增」，当前实点 150 只作读数值出现、不当算式基线；需要新数值的地方由上游给，本文不发明。
- **「原版」与「本仓」分两棵树**：`net/minecraft/**` 在 `.scratch/p25a/src/`，本仓在 `src/main/java/com/dashpig/farmstorm/`；凡写「命中 0」一律指明搜的是本仓。
- **只引上游不改上游**：与 58 / 59 / 60 不一致的地方只登记在 §4，不改任何既有文件。

### 0.5 一个任务 = 一个提交 + 五条顺序死命令

这五条是**提交边界**，不是建议；违反任一条各自有一个当场可见的坏结果。

| # | 死命令 | 落点任务 | 违反后的当场后果 |
|---|---|---|---|
| 1 | `CropConverterBlockEntity` 的 `implements CoreBound` 必须与它的 `clearCore()` 同一提交 | **F-314** | 接口少一个实现 ⇒ `does not override abstract method clearCore()`、编译不过 |
| 2 | 转换器 `setCore` 里调 `this.syncCore()` 必须与该私有方法本身同一提交 | **F-314** | 全仓 `syncCore` 唯一定义在 `CultivatorBlockEntity.java:104-110`，只写调用 ⇒ `cannot find symbol`（RT-87） |
| 3 | `BotanistTrade` 的 record 第二分量必须与 22 项实参同一提交 | **F-320** | 22 个单实参字面量喂 2 个形参 ⇒ 编译不过 |
| 4 | `TierTrade` 的 record 分量必须与 `of(...)` 与三档常量同一提交 | **F-321** | `:49-54` 走 `:56-58` 的 `of` 三参、而 record 要四参 ⇒ 编译不过 |
| 5 | 核侧 `KIND_BY_BLOCK` 的取用必须与「删掉两次按类收集」同一提交 | **F-317** | 发现面按标签、kind 无来源 ⇒ 新成员取到 `null` 即 `continue`，静默不绑定（RT-86） |

**F-328 不新增死命令**：两个死类与三处引用清零在同一个提交里完成（5 个文件、`866` 行删除、`19` 行插入），删完即编译通过，不存在「一半删一半留」的中间态。

---
## 1. 任务总表（八列）

| 任务号 | 名称 | Phase | 里程碑 | 复杂度 | 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|---|---|---|
| F-312 | 版本号 0.2.4 与 6 处版本串 | 66 | M62 | S | 0.5 | — | `mod_version=0.2.4` + 两支 `.cmd` 的 jar 名同步 |
| F-313 | 新建 `CoreBound` 三方法接口 | 67 | M63 | S | 0.5 | — | Java 文件 `152 − 2 + 1 = 151`，接口三个方法 + 第四个 0 |
| F-314 | 转换器实现接口 + `clearCore()` + `syncCore()` | 67 | M63 | M | 1.5 | F-313 | 该类 `syncCore` 定义 1、`sendBlockUpdated` 1 |
| F-315 | 培养器 `implements CoreBound` | 67 | M63 | S | 0.5 | F-313 | 类声明 1 行改、四个方法体 diff 0 行 |
| F-316 | `AreaBlockEntities` 按谓词重载 | 68 | M64 | S | 0.5 | — | `collect` 定义 2 个，旧重载原样保留 |
| F-317 | 核侧五处 + `KIND_BY_BLOCK`（需求 1 正面修复） | 68 | M64 | L | 3.0 | F-313、F-314、F-316 | 一次按标签收集、`clearCore()` 覆盖两个方块、含出口③ 两行文案 |
| F-318 | 五个 `XP_LEVEL_n` 常量 | 69 | M65 | S | 0.5 | — | `2 / 5 / 10 / 15 / 30` 只在一处 |
| F-319 | `BotanistEpicTrade` 常量 2 → 30 | 69 | M65 | S | 0.5 | F-318 | `VILLAGER_XP = VillagerTradeEvents.XP_LEVEL_5` |
| F-320 | `BotanistTrade` record 加 `xp` + 22 项逐项带值 | 69 | M65 | M | 1.5 | F-318 | 四个等级常量引用 6 / 6 / 6 / 4 = 22 |
| F-321 | `TierTrade` record 加 `xp` + 三档带值 | 69 | M65 | M | 1.5 | F-318 | 三档常量引用 1 / 1 / 1 |
| F-322 | `CoreHudRenderer:40` 底色 `0x4D000000` | 70 | M66 | S | 0.5 | — | 内层 alpha 77、边框仍是 `0xFF` |
| F-323 | `FarmstormTags:29` 注释订正 | 70 | M66 | S | 0.5 | — | 注释写「2 个成员」、「裁定 #5」命中 0 |
| F-324 | 两台机器的客户端分支（需求 4 主体） | 71 | M67 | M | 1.5 | F-314 | 两个 `block/` 文件**客户端分支内** `canInsert` 命中 0（全文件各 1） |
| F-325 | 据点核心的客户端分支 | 71 | M67 | S | 0.5 | — | 手持 `BlockItem` 才让路，归属判定留在服务端 |
| F-326 | 60 号 §8 的 **25 条**验收全量执行 | 72 | M68 | L | 3.0 | F-312 ~ F-325 | `.work/v024-verify/acceptance.md`，25 / 25 |
| F-327 | 已知影响、三项未实测与签字 | 72 | M68 | M | 1.5 | F-326 | `.work/v024-verify/signoff.md` + README 增量段 |
| F-328 | 删除 farmland 死代码 | 66 | M62 | L | 3.0 | 无（已落地） | 两个死类 780 行 + 三处引用清零，Java 150 |

任务数：`328 − 312 + 1 = 17`，连续无跳号；Phase **7** 个（66 ~ 72，F-328 归 66、不开新 Phase）；里程碑 **7** 个（M62 ~ M68，F-328 归 M62），一一对应。任务号顺序与 Phase 顺序在此处不等价：F-328 排在表尾却属 Phase 66，理由是它在写作期已落地、不占任何 Phase 的未完成面（A-111）。

---
## 2. 逐任务详情

每个任务固定五节：**目标 / 交付物（文件:行号）/ 验收断言 / 依赖 / 回滚影响**。依赖写在标题行，`依赖 —` 表示无前驱。交付物只写位置与要改什么，不写代码。

### Phase 66 · 发布面与死代码清除（F-312、F-328）
阶段目标：把版本号与两支构建脚本的产物面钉死——它是全部 jar 名与「检查环境」判据的唯一来源；并清掉 0.2.0 撤销耕地后遗留的两个 `farmland` 死类（F-328，写作期已落地）。出口判据：`farmstorm-0.2.4+mc1.21.1.jar` 产出，`0.2.3` 在这三个文件里归 0；`grep -rn "Farmland\|farmland" src/main/java` 命中恰 **2**，Java 文件数 **151**。

#### F-312 · 版本号 0.2.4 与 6 处版本串（S · 0.5 人日｜依赖 —）
- **目标**：把 `mod_version` 改成 **0.2.4**，并让两支 `.cmd` 里写死的 jar 名与抬头行跟着改，使「一键构建」的复制步骤与「检查环境」的存在性检查仍指向真实产物（D-225）。
- **交付物**：`gradle.properties:11`（`mod_version`）；`一键构建.cmd:5`（抬头）、`:11`、`:12`（jar / sources-jar）；`检查环境.cmd:5`（抬头）、`:14`（jar）。共 **6 处**版本串（`1 + 3 + 2`）。
- **验收断言**：
  1. `0.2.4` 在三个文件里命中 **6**；`0.2.3` 在这三个文件里命中 **0**。
  2. 构建退出码 0；`build/libs/farmstorm-0.2.4+mc1.21.1.jar` 与 `-sources.jar` 各存在 1 个，`farmstorm-0.2.3*` 残留 **0** 个。
  3. `jar tf` 该 jar 含 `META-INF/neoforge.mods.toml`，其 `version` 解析为 `0.2.4`。
- **依赖**：—。
- **回滚影响**：只影响产物名与两支脚本的横幅；回滚后 F-327 签字里的 jar 名读数失效，其余 16 个任务不受影响。

#### F-328 · 删除 farmland 死代码（L · 3.0 人日｜依赖 无（已落地）｜**本版已落地（写作期完成）**）
- **目标**：把 0.2.0 撤销耕地后遗留、已无任何读取方的两个死类连同三处悬挂引用一次删净，使 `Farmland` / `farmland` 在本仓源码里只剩两行历史记账注释（裁定 #8、A-111）。
- **交付物**：
  1. 删 `src/main/java/com/dashpig/farmstorm/farmland/FarmlandGuard.java`（225 行）与 `farmland/FarmlandLedger.java`（555 行）**整文件**，空目录 `farmland/` 一并删除：`225 + 555 = 780` 行。
  2. `event/FarmstormGameEvents.java`（**D-253**）：2 个 import、17 行耕地注释（`// 裁定 #3…` 5 行 + 其余 12 行）、`onRightClickBlock` / `onFarmlandTrample` 两个死方法的整段 javadoc 与本体、4 个随删而变死的 import（`Component` / `TriState` / `PlayerInteractEvent` / `BlockEvent`），头注释改为按 `register()` 的 9 个 `addListener` 逐条穷举（新增的 19 行即此）。
  3. `config/FarmstormConfig.java`（**D-254**）：`outpostCoreFarmlandBonus` 字段声明 1 行、字段上方 4 行注释 + 定义处 1 行注释（合计 5 行）、4 行被注释的 `.defineInRange`。
  4. `outpost/OutpostCoreIndex.java`（**D-253**）：`ownerOf` 的 javadoc 删掉指向 `FarmlandGuard#inOwnRange` 的那一行（`:189`）。
  5. 保留不动：`datagen/ModLangProvider.java:39-40` 的两行历史记账注释（v0.1.1 F-192 / F-197 的沿革）。
  文件与行数口径：2 删 + 3 改 = **5** 个文件；`git diff --stat -- src/` = 5 files changed、`19 insertions(+)`、`866 deletions(-)`。删行 `780 + 86 = 866`（780 = 两个整文件 `225 + 555`；86 = 三个改动文件的删行，`11 + 72 + 3 = 86`：`config/FarmstormConfig` 11、`event/FarmstormGameEvents` 72、`outpost/OutpostCoreIndex` 3）。插行 `1 + 16 + 2 = 19`（同三个文件，全部是把类头注释按 `register()` 的 9 个 `addListener` 逐条穷举重写所加）。净减 `866 − 19 = 847` 行。
- **验收断言**：
  1. `grep -rn "Farmland\|farmland" src/main/java` 命中恰 **2**（`ModLangProvider.java:39`、`:40`）；`grep -rn "outpostCoreFarmlandBonus" src/main/java` 命中 **0**。
  2. `(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` 实点 **150**（`152 − 2 = 150`）；F-313 落地后终值 **151**（`152 − 2 + 1`）。
  3. `python -X utf8 tools\verify_project.py` 复跑 **FAIL 0 / WARN 1**，注册面 **92**、server config **175**、语言键 **321** 三段读数与删除前一字不差。
  4. `Test-Path src/main/java/com/dashpig/farmstorm/farmland` 为 `False`（空目录随两个类一并删除）。
- **依赖**：无（已落地，写作期完成；无前驱，也无后继——没有任务读它删掉的符号）。
- **回滚影响**：`git revert` 该笔即把两个死类与三处引用原样恢复，其余 16 个任务的读数一个不变；唯一要同步改的是 F-313 与 F-326 ~ F-327 的文件数读数——回滚后基线回到删除前的 152，终值随之整体后移 2（`152 − 2 + 1` 里的 −2 归零）。它单独占一个任务号的理由就是这一条：它是一笔可独立回滚的提交。

### Phase 67 · 绑定契约：接口与两个实现（F-313 ~ F-315）
阶段目标：把「读缓存 / 写缓存 / 清缓存」三件事收敛成一个接口，并让两个方块实体都实现它。出口判据：`implements CoreBound` 在两个 `blockentity` 类各 1 处，转换器侧三个方法齐备、编译通过。

#### F-313 · 新建 `CoreBound` 三方法接口（S · 0.5 人日｜依赖 —）
- **目标**：新建 `blockentity/CoreBound.java`，只声明 `corePos()` / `setCore(BlockPos)` / `clearCore()` 三个方法，不声明第四个（D-228、D-234）。
- **交付物**：新建 `src/main/java/com/dashpig/farmstorm/blockentity/CoreBound.java`（包声明 `com.dashpig.farmstorm.blockentity`；import `net.minecraft.core.BlockPos`；三个抽象方法；无第四个方法）。
- **验收断言**：
  1. `(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` = **151**（`152 − 2 + 1`；实点 150 已含 F-328 的 `152 − 2`）。
  2. `BlockPos corePos();` / `void setCore(BlockPos pos);` / `void clearCore();` 在该文件各命中 **1**；`kind(` 命中 **0**。
  3. 该文件不含六个禁令模式（Mixin 注解 / mixin 配置 / 占位标记 / `printStackTrace` / `[PROBE]` / 空 `catch`），故 `verify_project.py:637-653` 的扫描仍 PASS、FAIL 仍 **0**。
- **依赖**：—。
- **回滚影响**：回滚删掉该文件后，F-314 / F-315 / F-317 的 `implements` 与接口调用全部编译不过——这三个任务是它的下游，回滚顺序必须由后往前。

#### F-314 · 转换器实现接口 + `clearCore()` + `syncCore()`（M · 1.5 人日｜依赖 F-313）
- **目标**：让 `CropConverterBlockEntity` 实现 `CoreBound`，补 `clearCore()` 与私有 `syncCore()`，并让 `setCore` 写完即同步；同时把 `convertible` 提为 `public`（D-229、D-247，以及 D-244 的编译前提）。
- **交付物**：`blockentity/CropConverterBlockEntity.java:40`（类声明加 `implements CoreBound`）；`:89-92`（`setCore` 补一次 `syncCore()` 调用）；`:94` 之前新增 `clearCore()`（与 `CultivatorBlockEntity.java:94-101` 同形，**不清** `Held` / `Progress`）；新增私有 `syncCore()`（与 `CultivatorBlockEntity.java:104-110` 同形，走 `Block.UPDATE_CLIENTS`）；`:129`（`convertible` 由 `private` 改 `public`，服务端 `canInsert` 的行为一行不改）。
- **验收断言**：
  1. `private void syncCore` 命中 **1**；`sendBlockUpdated` 命中 **1**；`Block.UPDATE_CLIENTS` 命中 **2**（`:227` 既有 1 + 新方法 1）。
  2. `implements CoreBound` 命中 **1**；`public void clearCore()` 命中 **1**；`clearCore` 方法体内 `this.held` / `this.progress` 命中 **0**（D-168 继续有效）。
  3. `public boolean convertible(` 命中 **1**、`private boolean convertible(` 命中 **0**。
  4. 构建退出码 0（死命令 1 与 2 的当场判据）。
- **依赖**：F-313。
- **回滚影响**：回滚后 F-317 的 `bound.clearCore()` 在转换器上退化为「接口有声明、实现类缺方法」的编译错误，F-324 也失去 `public convertible`；两者必须一并回滚。

#### F-315 · 培养器 `implements CoreBound`（S · 0.5 人日｜依赖 F-313）
- **目标**：让 `CultivatorBlockEntity` 实现 `CoreBound`——它已有三个方法，只加声明（D-229）。
- **交付物**：`blockentity/CultivatorBlockEntity.java:44`（类声明加 `implements CoreBound`）；`:66` / `:84-88` / `:94-101` / `:104-110` **一行不动**。
- **验收断言**：
  1. `implements CoreBound` 在该文件命中 **1**；`git diff --numstat` 该文件为 `1 1`（一加一删）。
  2. 四个方法体 diff **0** 行：`public BlockPos corePos()` / `public void setCore(BlockPos pos)` / `public void clearCore()` / `private void syncCore()` 各命中 **1** 且不在 diff 里。
  3. 构建退出码 0。
- **依赖**：F-313。
- **回滚影响**：回滚只让培养器退出接口集合；若 F-317 已落地，培养器在核侧被 `instanceof CoreBound` 判为未实现 ⇒ 清绑定与补绑都不再覆盖它，必须与 F-317 一并回滚。

### Phase 68 · 收集面与核侧统一（F-316 ~ F-317）
阶段目标：把候选发现从「两次按类枚举」换成「一次按标签」，并让核侧的缓存判定、绑定写入、迁移改绑、清理出口四处经同一接口走（D-230 ~ D-233、D-235）。出口判据：`CultivatorBlockEntity.class` 在核侧命中 **0**，且拆核后转换器的 `Core` 键消失、再放新核能重新绑定。

#### F-316 · `AreaBlockEntities` 按谓词重载（S · 0.5 人日｜依赖 —）
- **目标**：给 `AreaBlockEntities` 增一个按谓词收集的重载，现有按 `Class` 的重载原样保留（D-231）。
- **交付物**：`util/AreaBlockEntities.java:32` 之后新增 `collect(ServerLevel, BlockPos, int, Predicate<BlockEntity>)`；`:3-8` 的 import 区新增 `java.util.function.Predicate`；`:32-33`、`:39-44` 旧重载一行不动。
- **验收断言**：
  1. `public static` + `collect(` 的定义命中 **2**（`Class<T> type` 1 + `Predicate<BlockEntity> filter` 1）；`Class<T> type` 命中 **1**。
  2. `getChunkNow(` 命中 **2**（新重载照抄 `:39-42` 的「不为枚举加载区块」写法）；`new ArrayList<>(chunk.getBlockEntities().values())` 命中 **2**（两阶段拷贝各一份）。
  3. 构建退出码 0，且本提交内该方法无调用点（`collect(` 的调用点仍只在 `OutpostCoreBlockEntity` 的 2 处，F-317 才减到 1 处）。
- **依赖**：—。
- **回滚影响**：回滚后 F-317 的标签收集失去落点；F-317 必须一并回滚，其余任务不受影响。

#### F-317 · 核侧五处 + `KIND_BY_BLOCK`（L · 3.0 人日｜依赖 F-313、F-314、F-316）
- **目标**：一次落地需求 1 的正面修复与其四道配套——候选按 `farmstorm:processing_block` 标签发现、kind 由单一映射给出、缓存判定与绑定写入与迁移改绑三处经接口调用、无接替核的出口覆盖两个方块（D-230 ~ D-233、D-235）。
- **交付物**：`blockentity/OutpostCoreBlockEntity.java:604-608`（两次按类收集 → 一次按标签，判据复用 `:887-889` 的 `isProcessingBlock`）；`:619-629`（新增静态映射 `KIND_BY_BLOCK`：`CULTIVATOR` / `CROP_CONVERTER` 两条；取到 `null` 即 `continue`；缓存判定改 `((CoreBound) candidate).corePos()`）；`:639-643`（绑定写入改 `setCore`，顺序仍是先核侧后机器侧）；`:851-855`（迁移改绑经接口）；`:866-873`（`:866` 的 `isProcessingBlock` 判据保留、`:870-873` 的出口② 改 `bound.clearCore()`，`:872` 日志去掉「培养器」字样）；`:859`（出口③ 的注释，去掉「培养器」字样、改按处理方块描述）与 `:867`（出口③ 第二条日志改「迁移失败且该格已不是处理方块：保留 NBT 原值 {}」）同批改（A-108）；`:863`（出口③ 第一条日志）**不动**。
- **验收断言**：
  1. `AreaBlockEntities.collect` 命中 **1**；`CultivatorBlockEntity.class` 命中 **0**；`isProcessingBlock(` 命中 **2**（定义 1 + 调用 1）。
  2. `rebindNearby` 与 `settleOnRemoval` 两方法体内 `instanceof CultivatorBlockEntity` / `instanceof CropConverterBlockEntity` 各命中 **0**。
  3. `KIND_BY_BLOCK` 命中 **1**，映射内 `Link.Kind.CULTIVATOR`、`Link.Kind.CROP_CONVERTER` 各 **1**。
  4. `clearCore()` 调用点命中 **1**；旧日志串「迁移失败：培养器留原地并标为未绑定」命中 **0**，新串「迁移失败：标签方块留原地并标为未绑定」命中 **1**。
  5. 拆掉转换器所绑的核后 `/data get block <转换器坐标> Core` 报告该键不存在，且 `Held` / `Progress` 仍在（60 号 §8 #10）。
  6. 3 × 3 区块内放新核、等一个补绑周期，日志出现 1 行 `补绑：核 … 本轮新增 1 条`；再 `/data get block` 读到新核坐标（60 号 §8 #11 / #12）。
  7. `:859` 行内「培养器」命中 **0**；旧日志串 `迁移失败且该格已不是培养器` 命中 **0**，新串 `迁移失败且该格已不是处理方块` 命中 **1**；`:863` 的 `迁移失败且区块未加载` 仍命中 **1**。
- **依赖**：F-313、F-314、F-316（三个前置各自提供接口、转换器的清缓存、按谓词收集）。
- **回滚影响**：回滚即退回 0.2.3 的僵死态（转换器保留死坐标、补绑永久跳过）；这是本版唯一一个「回滚就复发 bug」的任务，回滚必须与 F-326 的验收一并作废。

### Phase 69 · 交易经验分级（F-318 ~ F-321）
阶段目标：把 1 ~ 5 级的成交经验从统一 2 改为 2 / 5 / 10 / 15 / 30，映射只在一处定义，26 条 listing 逐项携带（D-236 ~ D-239）。出口判据：`VILLAGER_XP` 在三个交易类里合计命中 **2**（史诗类的声明 1 + 引用 1），`Offers` 读数按 xp 分组为 7 / 6 / 7 / 4 / 2。

#### F-318 · 五个 `XP_LEVEL_n` 常量（S · 0.5 人日｜依赖 —）
- **目标**：在 `VillagerTradeEvents` 类体里加五个 `public static final int` 常量，右值写字面量 `2 / 5 / 10 / 15 / 30`，其余类只引用不复制（D-236）。
- **交付物**：`event/VillagerTradeEvents.java`（类体新增五行，位置在 `onVillagerTrades` 方法之外）；`:44-47` 的四个 `subList` 与 `:55-57` 的三个 `get(n)` **一行不动**（D-239）。
- **验收断言**：
  1. `XP_LEVEL_1 = 2` / `XP_LEVEL_2 = 5` / `XP_LEVEL_3 = 10` / `XP_LEVEL_4 = 15` / `XP_LEVEL_5 = 30` 各命中 **1**。
  2. 可见性 `public static final int` 在该文件命中 **5**；`private static final int` 命中 **0**。
  3. `subList(0, 6)` / `(6, 12)` / `(12, 18)` / `(18, 22)` 各命中 **1**，且 `git diff -U0` 该文件里 `subList(` 命中 **0** 行（边界未动）。
- **依赖**：—。
- **回滚影响**：回滚使 F-319 / F-320 / F-321 的引用全部编译不过；四者构成一个编译闭环，回滚必须由后往前。

#### F-319 · `BotanistEpicTrade` 常量 2 → 30（S · 0.5 人日｜依赖 F-318）
- **目标**：把 `VILLAGER_XP` 由 2 改成引用 `VillagerTradeEvents.XP_LEVEL_5`（值 30），并同批改写 `:28` 的类 javadoc 与 `:45` 的注释（D-238）。
- **交付物**：`event/BotanistEpicTrade.java:28`（javadoc 里的 `xp = 2` 与 `{@link TierTrade#VILLAGER_XP}` 改写为指向 `VillagerTradeEvents.XP_LEVEL_5`）；`:45`（注释里「沿用 … 的 2」删掉数字）；`:46`（常量改引用）；`:38` / `:40` / `:42` / `:44` / `:64` **一行不动**。
- **验收断言**：
  1. `VILLAGER_XP = VillagerTradeEvents.XP_LEVEL_5` 命中 **1**；该文件 `= 2` 命中 **0**。
  2. `TierTrade#VILLAGER_XP` 命中 **0**（悬挂链接与旧口径同时清掉）；`XP_LEVEL_5` 命中 **1**。
  3. `MIN_PRICE = 40` / `MAX_PRICE = 64` / `MAX_USES = 1` / `COUNT = 1` 各命中 **1**（红线 6）。
- **依赖**：F-318。
- **回滚影响**：回滚只让 5 级经验退回 2；F-320 / F-321 的其他级不受影响。本条**先于** F-321 落地，理由是 A-105。

#### F-320 · `BotanistTrade` record 加 `xp` + 22 项逐项带值（M · 1.5 人日｜依赖 F-318）
- **目标**：给 record 加第二分量 `int xp`，22 项按位置逐项带值，删掉类常量 `VILLAGER_XP`，`getOffer` 改读本项的值（D-237）。
- **交付物**：`event/BotanistTrade.java:38`（record 加第二分量）；`:48-49`（注释与常量一并删）；`:60-82`（22 个实例补第二实参：第 1 ~ 6 项 → `XP_LEVEL_1`、7 ~ 12 → `_2`、13 ~ 18 → `_3`、19 ~ 22 → `_4`）；`:91`（`MerchantOffer` 第二实参位置的 `VILLAGER_XP` 改 `this.xp`）；`:41` / `:43` / `:45` / `:47` / `:51` **一行不动**（红线 6）。
- **验收断言**：
  1. `new BotanistTrade(` 命中 **22**；`XP_LEVEL_1` **6**、`XP_LEVEL_2` **6**、`XP_LEVEL_3` **6**、`XP_LEVEL_4` **4**，相加 `6 + 6 + 6 + 4 = 22`。
  2. `VILLAGER_XP` 在该文件命中 **0**；`this.xp` 命中 **1**。
  3. `MIN_PRICE = 3` / `MAX_PRICE = 6` / `MAX_USES = 8` / `COUNT = 1` / `PRICE_MULTIPLIER = 0.0F` 各命中 **1**（红线 6）。
  4. 22 行的行内序号注释 `// 1` ~ `// 22` 逐行仍在（红线 4：顺序未动）。
- **依赖**：F-318。
- **回滚影响**：回滚后植物学家 1 ~ 4 级回到统一 2；F-326 的 `Offers` 读数（`xp:2` 与 `xp:5` / `xp:10` / `xp:15` 四条读法）失效，其余任务不受影响。

#### F-321 · `TierTrade` record 加 `xp` + 三档带值（M · 1.5 人日｜依赖 F-318）
- **目标**：给 record 尾部加 `int xp`，三档常量各带自己的值，`of` 加第四参并透传，删掉类常量 `VILLAGER_XP`（D-237）。
- **交付物**：`event/TierTrade.java:37-38`（record 尾加 `, int xp`）；`:45`（删 `VILLAGER_XP`）；`:49-54`（三档分别传 `XP_LEVEL_1`（`COMMON`）/ `XP_LEVEL_3`（`RARE`）/ `XP_LEVEL_5`（`EPIC`））；`:56-58`（`of` 加第四参并透传）；`:79`（`VILLAGER_XP` 改 `this.xp`）；`:41` / `:43` / `:47` **一行不动**（红线 6）。
- **验收断言**：
  1. `XP_LEVEL_1` / `XP_LEVEL_3` / `XP_LEVEL_5` 在该文件各命中 **1**；`VILLAGER_XP` 命中 **0**；`this.xp` 命中 **1**。
  2. `private static TierTrade of(` 命中 **1** 且形参含第四个 `int xp`；`of(` 命中 **4**（定义 1 + 三档调用 3）。
  3. `CROP_COUNT = 2` / `MAX_USES = 1` / `PRICE_MULTIPLIER = 0.0F` 各命中 **1**（红线 6）。
- **依赖**：F-318。
- **回滚影响**：回滚后农民 3 级与 5 级回到 2；`BotanistEpicTrade` 已在 F-319 改为引用 `XP_LEVEL_5`，不依赖本类常量，故本回滚不连带 F-319。

### Phase 70 · 浮窗底色与标签注释（F-322 ~ F-323）
阶段目标：把核心浮窗的内层底色改成不透明度 30.2%，并订正一处与代码不符的注释。出口判据：`0x80000000` 在 `client/` 命中 0，「裁定 #5」在 `util/FarmstormTags.java` 命中 0。

#### F-322 · `CoreHudRenderer:40` 底色 `0x4D000000`（S · 0.5 人日｜依赖 —）
- **目标**：把内层底色常量由 `0x80000000` 改为 `0x4D000000`，硬编码在渲染类里、不新增 config 键（D-241、D-242）。
- **交付物**：`client/CoreHudRenderer.java:40`（`COLOR_BACKGROUND`）；`:41`（`COLOR_BORDER = 0xFF000000`）、`:42-44`（三种文字色）、`:30-38`（版式常数）、`:77-78`（两条 `fill` 的几何）**一行不动**。
- **验收断言**：
  1. `0x4D000000` 在该文件命中 **1**；`0x80000000` 命中 **0**；`0xFF000000` 命中 **1**。
  2. 面板截图取色：内层像素 alpha = **77**（`0x4D`）、边框像素 alpha = **255**（`0xFF`），两处都要读（60 号 §8 #20）。
  3. `git status --porcelain` 不出现 `config/` 与 `lang/` 开头的路径；`FarmstormClientConfig.java` 三个键的 diff **0** 行。
- **依赖**：—。
- **回滚影响**：只回滚一个常量，不牵连任何任务；`OutpostCoreScreen` 全程不在改动面内（红线 5）。

#### F-323 · `FarmstormTags:29` 注释订正（S · 0.5 人日｜依赖 —）
- **目标**：把 `:29` 的注释订正为「**2 个成员**（培养器 + 农作物转换器）」，指向 D-194 与标签产物，**不得**写「裁定 #5」（D-248）。
- **交付物**：`util/FarmstormTags.java:29`（注释一行）；`:30` 的 `TagKey` 声明与 `datagen/ModBlockTagsProvider.java:45-47` **一行不动**。
- **验收断言**：
  1. `:29` 行含「2 个成员」与「D-194」；`裁定 #5` 在该文件命中 **0**。
  2. `src/generated/resources/data/farmstorm/tags/block/processing_block.json:2-5` 仍是 2 个值（`farmstorm:cultivator`、`farmstorm:crop_converter`），`git status --porcelain` 不出现 `src/generated/resources` 开头的路径。
  3. `TagKey<Block> PROCESSING_BLOCK` 命中 **1**（声明未改）。
- **依赖**：—。
- **回滚影响**：只回滚一行注释；F-317 的标签判据读的是常量不是注释，故本回滚不影响任何行为任务。

### Phase 71 · 放置音效客户端预测（F-324 ~ F-325）
阶段目标：三处 `useWithoutItem` 的客户端分支由「无条件已消费」改为「手持输入物才接走，否则让路」，服务端四道闸一行不改（D-243 ~ D-246）。出口判据：三个 `block/` 文件的客户端分支里 `canInsert` 命中 0，且双客户端音频轨两侧各 1 次（两个任务**都**落地才算出口达成，见 A-106）。

#### F-324 · 两台机器的客户端分支（M · 1.5 人日｜依赖 F-314）
- **目标**：把转换器与培养器的客户端分支改为「手持物是输入物才 `SUCCESS`，否则 `PASS`」，谓词取 `convertible` / `admissible`、**不得**调 `canInsert`（D-243、D-244）。
- **交付物**：`block/CropConverterBlock.java:121-122`（客户端分支改条件返回，谓词 `converter.convertible(player.getMainHandItem())`）；`block/CultivatorBlock.java:117-118`（同形，谓词 `cultivator.admissible(player.getMainHandItem())`）；`:124-132` 与 `:120-128` 的服务端四道闸**一行不动**；`FarmstormBlocks.java:78`、`:108` 的 `SoundType` 与 `registry/FarmstormSounds.java` **不动**（D-246）。
- **验收断言**：
  1. `CropConverterBlock.java` 里 `convertible(` 命中 **1**、`canInsert` 在客户端分支内命中 **0**（全文件仍是 **1**，`:128` 那道服务端闸不动）；`CultivatorBlock.java` 里 `admissible(` 命中 **1**、`canInsert` 在客户端分支内命中 **0**（全文件仍是 **1**，`:124` 不动）。
  2. 两文件的 `if (level.isClientSide)` 分支里 `InteractionResult.SUCCESS` 与 `InteractionResult.PASS` 各命中 **1**（条件返回的两支都在）。
  3. `SoundType.ANVIL` 在 `FarmstormBlocks.java` 仍命中 **2**；`FarmstormSounds.java` 的 diff **0** 行；`sound_event` 注册数仍 **3**（`Farmstorm.java:182` 的 `soundEvents == 3`）。
  4. 双客户端录屏：A 手持泥土右键培养器顶面，A 侧音频轨 `block.anvil.place` **1** 次、B 侧 **1** 次；转换器按同一读法复跑一遍，同样两侧各 **1** 次（60 号 §8 #25，转换器对读见 A-107）。
- **依赖**：F-314（`convertible` 提为 `public`）。
- **回滚影响**：回滚后两台机器的放置者本人重新听不见放置音；F-326 的第 25 条验收失效，F-325 不受影响。

#### F-325 · 据点核心的客户端分支（S · 0.5 人日｜依赖 —）
- **目标**：把据点核心的客户端分支改为「手持 `BlockItem` ⇒ `PASS`，否则 `SUCCESS`」，且客户端不做归属判定（D-245、HC-03）。
- **交付物**：`block/OutpostCoreBlock.java:131-132`（客户端分支改条件返回）；import 区新增 `net.minecraft.world.item.BlockItem`（该文件现只在注释里出现 `BlockItem`，见 `:79-80`）；`:134-142` 的服务端开界面分支**一行不动**。
- **验收断言**：
  1. `player.getMainHandItem().getItem() instanceof BlockItem` 命中 **1**；`InteractionResult.SUCCESS` 在客户端分支内命中 **1**（`PASS` 为另一支）。
  2. `isAlly(` 在客户端分支（`:131-132`）内命中 **0**、在服务端分支内命中 **1**（`:126` 的 HC-03 继续有效）。
  3. 该文件 `isClientSide` 命中 **2**（`:67` 既有 1 + `:131` 本任务 1）；`canInsert` 命中 **0**。
- **依赖**：—。
- **回滚影响**：回滚后据点核心的放置者本人重新听不见放置音；两台机器已在 F-324 修好，故回滚本条会留下「两台机器听得见、核心听不见」的发布态，只能整段退回（A-106）。

### Phase 72 · 验收与签字（F-326 ~ F-327）
阶段目标：把 60 号 §8 的 25 条逐条跑出读数，并把已知影响与三项未实测落成文字。出口判据：`.work/v024-verify/` 下两份记录齐全，25 / 25 与九条风险各有读数。

#### F-326 · 60 号 §8 的 25 条验收全量执行（L · 3.0 人日｜依赖 F-312 ~ F-325）
- **目标**：逐条执行 60 号 §8 的 **25 条**验收，每条给「怎么做 / 期望看到什么 / 实际读数」，产出可复核记录。
- **交付物**：新建 `.work/v024-verify/acceptance.md`（25 行读数；编号与 60 号 §8 的 1 ~ 25 一一对应）。
- **验收断言**：
  1. 25 条逐条有读数：源码类 **14** 条（#4 ~ #9、#13 ~ #16、#18、#19、#21、#23）给 grep 命中数；构建与检查器类 **5** 条（#1、#2、#3、#22、#24）给命令输出；进游戏类 **6** 条（#10、#11、#12、#17、#20、#25）给 `/data get` 或像素或音频轨读数（`14 + 5 + 6 = 25`）。
  2. #3 输出 **151**（`152 − 2 + 1`）；#1 末段 `FAIL 0 / WARN 1` 且生成 toml 键数 **175**；#22 的 `git status --porcelain` 出现的路径恰 **19** 个 `.java`，三段构成 `14 + 3 + 2 = 19`：**14** = 本版计划面（12 改 + 1 新增 `blockentity/CoreBound.java` + 1 只加方法 `util/AreaBlockEntities.java`，58 号 D-228 ~ D-248，落点见 D-228 / D-247）；**3** = 删除批次带出的连带改动（`config/FarmstormConfig` 记 58 号 D-254；`event/FarmstormGameEvents` 与 `outpost/OutpostCoreIndex` 记 58 号 D-253）；**2** = 删除（`farmland/FarmlandGuard` / `farmland/FarmlandLedger`，58 号 D-253）；19 个路径全部以 `src/main/java/com/dashpig/farmstorm/` 开头，`src/main/resources/` 与 `src/generated/resources/` 命中 0。
  3. #17 的 `/data get entity <村民> Offers` 按 `xp:` 分组为 `xp:2` **7**、`xp:5` **6**、`xp:10` **7**、`xp:15` **4**、`xp:30` **2**，合计 `7 + 6 + 7 + 4 + 2 = 26`。
  4. #24 的 `run/config/farmstorm-server.toml` 为 `[段]` **60** 个、`键 = 值` **175** 行；两份 lang 各 **321** 键、`farmstorm.configuration.` **178** 键。
- **依赖**：F-312 ~ F-325（全部前置）。
- **回滚影响**：回滚只丢一份验收记录，不改任何源码；但「25 / 25」这一读数随之失效，F-327 的签字无法成立。

#### F-327 · 已知影响、三项未实测与签字（M · 1.5 人日｜依赖 F-326）
- **目标**：把四条已知影响（59 号 §7 的 7.1 ~ 7.4）、三项未实测（D-252）、九条风险（RT-83 ~ RT-91）与十项不动读数签成一份记录，并把玩家可见的三段话落进 README（D-225、D-251、D-252）。
- **交付物**：新建 `.work/v024-verify/signoff.md`；`README.md` 增量段（版本 0.2.4 +「客户端只看品类」的已知影响 +「大师级单次 30 点」+「底板不透明度 30.2%」三段）。
- **验收断言**：
  1. 已知影响 4 条逐条成文且标「不是缺陷」（59 号 §7 的 7.1 ~ 7.4）；`已知影响` 在 signoff 命中 **4**。
  2. 三项未实测逐条标「未实测」：只有放置者听不见 / 副手放置 / 客户端 `getRecipeManager()` 可用性（D-252）；`未实测` 命中 **3**。
  3. 十项读数齐：Java **151**、注册面 **92**、server config **175**、client config **3**、语言键 **321 × 2**、`configuration` 组 **178**、网络包 **2**、进度 **16**、转换配方 **21**、交易 listing **26**。
  4. `README.md` 三段同段共现：`0.2.4` 与 `151`；`无反应` 与 `已知影响`；`30 点` 与 `15 倍`。
  5. `docs/zh/52` ~ `docs/zh/60`、`.work/v024-BRIEF.md`、`plan0.2.4.txt` 的 diff **0** 行（只引上游）。
- **依赖**：F-326。
- **回滚影响**：回滚只丢签字与 README 增量段；F-326 的验收记录仍在，但交付面失去唯一一份「25 / 25 + 十项读数」的复核。
## 3. 覆盖对照表（六张）

表 A 有两半：**A.1** 对 58 号 §3 的动作面 **23 处**，另加裁定 #8 补的 2 行删除动作（合计 **25** 行）；**A.2** 对 60 号 §8 的验收 **25 条**。表 B ~ 表 F 分别对注册面、config、语言键、风险、待验证 API。每张表末行给「覆盖数 / 总数」。

### 表 A · 动作面与验收条目

**A.1 · 58 号 §3 的动作面 23 处 + 裁定 #8 的删除 2 处 → 任务**

| # | 58 号出处 | 动作 | 落点任务 |
|---|---|---|---|
| 1 | §3.1 第 1 项 | 新建 `blockentity/CoreBound.java`（Java 文件 `152 − 2 + 1 = 151`） | F-313 |
| 2 | §3.1 第 2 项 | `CropConverterBlockEntity#clearCore` | F-314 |
| 3 | §3.1 第 3 项 | `CropConverterBlockEntity#syncCore`（私有） | F-314 |
| 4 | §3.1 第 4 项 | `AreaBlockEntities#collect` 的按谓词重载 | F-316 |
| 5 | §3.1 第 5 项 | 五个 `XP_LEVEL_n` 具名常量（2 / 5 / 10 / 15 / 30） | F-318 |
| 6 | §3.1 第 6 项 | 「方块 → `Link.Kind`」的单一映射 | F-317 |
| 7 | §3.2 第 1 项 | `OutpostCoreBlockEntity.java:604-608` 两次按类枚举 → 一次按标签 | F-317 |
| 8 | §3.2 第 2 项 | `:621-629` 缓存判定改接口 | F-317 |
| 9 | §3.2 第 3 项 | `:639-643` 绑定写入改接口 | F-317 |
| 10 | §3.2 第 4 项 | `:851-855` 迁移改绑改接口 | F-317 |
| 11 | §3.2 第 5 项 | `:870-873` 清理出口覆盖两个方块 | F-317 |
| 12 | §3.2 第 6 项 | `CultivatorBlockEntity.java:44` 加 `implements CoreBound` | F-315 |
| 13 | §3.2 第 7 项 | `CropConverterBlockEntity.java:40` 加 `implements CoreBound` | F-314 |
| 14 | §3.2 第 8 项 | `CropConverterBlockEntity.java:89-92` 的 `setCore` 补同步 | F-314 |
| 15 | §3.2 第 9 项 | `BotanistTrade.java:38`、`:60-82`、`:91` | F-320 |
| 16 | §3.2 第 10 项 | `TierTrade.java:37-38`、`:49-54`、`:79` | F-321 |
| 17 | §3.2 第 11 项 | `BotanistEpicTrade.java:46`（连带 `:28`、`:45`） | F-319 |
| 18 | §3.2 第 12 项 | `VillagerTradeEvents.java:44-47`、`:55-57`（边界不动，传入本级取值） | F-318 |
| 19 | §3.2 第 13 项 | `CropConverterBlock.java:121-122` 客户端判据 | F-324 |
| 20 | §3.2 第 14 项 | `CultivatorBlock.java:117-118` 客户端判据 | F-324 |
| 21 | §3.2 第 15 项 | `OutpostCoreBlock.java:131-132` 客户端判据 | F-325 |
| 22 | §3.2 第 16 项 | `CoreHudRenderer.java:40` 的 `0x80000000` → `0x4D000000` | F-322 |
| 23 | §3.2 第 17 项 | `FarmstormTags.java:29` 注释订正 | F-323 |
| 24 | §3.3 删除项 1（58 号 D-253） | 删 `farmland/FarmlandGuard.java`（225 行，整文件） | F-328 |
| 25 | §3.3 删除项 2（58 号 D-253） | 删 `farmland/FarmlandLedger.java`（555 行，整文件） | F-328 |

**A.1 的行数核对**：**25 / 25** 有落点（新增 6 + 修改 17 + 删除 2 = 25），未覆盖 **0**；58 号 §3.3 原文的删除项仍是 **0**（只引上游、不改上游），表内第 24、25 两行是裁定 #8 补的删除动作，登记在 A-111。**表外动作 5 项**（不在 58 号 §3 的 23 处内，各有独立验收）：① `CropConverterBlockEntity.java:129` 的 `convertible` 由 `private` 提为 `public`（60 号 §4.4.3 的编译前提，归 F-314）；② `gradle.properties:11` 与两支 `.cmd` 的 5 处版本串（D-225，归 F-312）；③ `README.md` 增量段（D-225、D-251，归 F-327）；④ `OutpostCoreBlockEntity.java:859` 的注释与 `:867` 的日志串（同文件表外两行，A-108，归 F-317）；⑤ `event/FarmstormGameEvents.java` 与 `config/FarmstormConfig.java` 的 farmland 引用清零，加 `outpost/OutpostCoreIndex.java:189` 的一行 javadoc（裁定 #8，归 F-328，与表内第 24、25 行同一提交）。

**A.2 · 60 号 §8 的验收 25 条 → 任务**

| # | 验收条目（60 号 §8） | 由谁落地 / 谁给读数 | 一条多任务 |
|---|---|---|---|
| 1 | 检查器 FAIL 0 / WARN 1 / 生成 toml 175 键 | F-326 | — |
| 2 | 启动日志 `自检 注册对象` 恰 1 行、含 `92 个` 与 `OK` | F-326 | — |
| 3 | `*.java` 计数 **151** | F-313（落点）+ F-326（复核） | **是** |
| 4 | `CoreBound` 三签名各 1、第四个 0 | F-313 | — |
| 5 | `private void syncCore` 1、`sendBlockUpdated` 1、`UPDATE_CLIENTS` 2 | F-314 | — |
| 6 | `AreaBlockEntities.collect` 1、`CultivatorBlockEntity.class` 0 | F-317 | — |
| 7 | 两方法体内 `instanceof CultivatorBlockEntity` 0 | F-317 | — |
| 8 | `KIND_BY_BLOCK` 1、两个 `Link.Kind` 各 1 | F-317 | — |
| 9 | `clearCore()` 调用点 1、旧日志串 0 | F-317 | — |
| 10 | `/data get block` 的 `Core` 键不存在、`Held` / `Progress` 仍在 | F-317 | — |
| 11 | 补绑日志 1 行、`累计` 增 1 | F-317 | — |
| 12 | #10 与 #11 串起后 `Core` 等于新核坐标 | F-317 | — |
| 13 | 五个常量各 1 | F-318 | — |
| 14 | `BotanistTrade` 的四个常量引用 6 / 6 / 6 / 4 | F-320 | — |
| 15 | `VILLAGER_XP = VillagerTradeEvents.XP_LEVEL_5` 1、`= 2` 0 | F-319 | — |
| 16 | `VILLAGER_XP` 三类命中 0 / 0 / 2 | F-320 + F-321 + F-319 | **是** |
| 17 | `Offers` 按 `xp:` 分组 7 / 6 / 7 / 4 / 2 = 26 | F-326 | — |
| 18 | 四个切片与三个挂载点各 1 | F-318 | — |
| 19 | `0x4D000000` 1、`0x80000000` 0、`0xFF000000` 1 | F-322 | — |
| 20 | 内层 alpha 77、边框 alpha 255 | F-322 | — |
| 21 | 三处客户端分支各 1、`canInsert` 0 | F-324 + F-325 | **是** |
| 22 | `git status` 恰 19 个 `.java` 路径（`14 + 3 + 2`，含 F-328 的 2 删 + 3 改）、资源面 0 | F-326 | — |
| 23 | `PROTOCOL_VERSION = "3"` 1、`.playToClient(` 2 | F-326 | — |
| 24 | `[段]` 60 / 键 175 / lang 321 × 2 / 前缀 178 | F-326 | — |
| 25 | 双客户端音频轨两侧各 1 次 | F-324（转换器对读见 A-107） | — |

**A.2 的读法**：**25 / 25** 有落点，未覆盖 **0**；一条挂多任务的只有 **3 条**（#3、#16、#21），其余 22 条一条一任务。给读数与落点不是同一件事：#3 的 **151** 由 F-313 落、F-326 复核；#16 的「三类命中 0 / 0 / 2」要三个交易任务都落地后才可读。

### 表 B · 注册面 **92**（12 类逐类）

| # | registry | 基线 | 本版增量 | 改后 | 落点任务 | 核对任务 |
|---|---|---|---|---|---|---|
| 1 | `items` | 28 | 0 | **28** | — | F-326 |
| 2 | `blocks` | 28 | 0 | **28** | — | F-326 |
| 3 | `block_entity_type` | 8 | 0 | **8** | — | F-326 |
| 4 | `entity_type` | 13 | 0 | **13** | — | F-326 |
| 5 | `mob_effect` | 4 | 0 | **4** | — | F-326 |
| 6 | `sound_event` | 3 | 0（D-246 禁令） | **3** | — | F-326 |
| 7 | `recipe_type` | 1 | 0 | **1** | — | F-326 |
| 8 | `recipe_serializer` | 1 | 0 | **1** | — | F-326 |
| 9 | `creative_mode_tab` | 1 | 0 | **1** | — | F-326 |
| 10 | `damage_type` | 3 | 0 | **3** | — | F-326 |
| 11 | `data_component_type` | 1 | 0 | **1** | — | F-326 |
| 12 | `menu_type` | 1 | 0 | **1** | — | F-326 |
| **合计** | | **92** | **0** | **92** | | `28+28+8+13+4+3+1+1+1+3+1+1 = 92` |

**三份冻结证明**：① 期望值与十二行逐行同值（`tools/verify_project.py:458-471` 的 `EXPECTED_COUNTS`，`:610` 用 `sum(EXPECTED_COUNTS.values())` 现算总数、不写第二份 92）；② 运行时自检 `Farmstorm.java:180-183` 的 `total == 92` 与十二个逐类 `==` 本版一行不改；③ 25 行动作面没有一处落在 `registry/` 下（F-328 删的两个类不是注册项，`92` 逐类也不减）——`FarmstormSounds` 另有 D-246 的显式禁改。**本表无落点任务**，全部由 F-326 的第 2 条与第 24 条读数兜住。

### 表 C · config **175 + 3**（全冻结）

| # | 面 | 基线 | 本版增量 | 改后 | 证明 | 核对任务 |
|---|---|---|---|---|---|---|
| 1 | server 键 / 组 | 175 / 60 | 0 | **175 / 60** | 58 号 §3 的 23 处无一处落在 `config/FarmstormConfig.java`；F-328 删的是一个无人读取的字段声明与 4 行注释，不减键（A-111） | F-326（#24） |
| 2 | `outpost_core` 组 | 4 键 | 0 | **4** | 那 4 行 `.define` 一直是注释、从未计入 175；F-328 连同字段声明一并删掉，键数仍 4（60 号 §6.1 第 1 条、A-111） | F-326 |
| 3 | `trade` 组 | 6 键 | 0 | **6** | 需求 2 只改 xp 值，价格区间一行不改（D-237、红线 6） | F-326 |
| 4 | client 键 | 3 | 0 | **3** | `FarmstormClientConfig.java:25-27`；F-322 硬编码、不加 `hud.opacity`（D-241） | F-322、F-326 |
| 5 | `configuration` 语言组 | 178 | 0 | **178** | `178 − 0 + 0 = 175 + 3` | F-326（#24） |
| 6 | 检查器 config 期望面 | FAIL 0 / WARN 1 | 0 | **同左** | `EXPECTED` 与期望键集不改（60 号 §6.3 第 3 条） | F-326（#1） |
| **合计** | | **178** | **0** | **178** | `175 + 3 = 178` | |

### 表 D · 语言键 **321 × 2**（全冻结）

| # | 组 | 键数 | 本版增量 | 落点任务 | 核对任务 |
|---|---|---|---|---|---|
| 1 | `item.` | 28 | 0 | — | F-326 |
| 2 | `block.` | 28 | 0 | — | F-326 |
| 3 | `advancements.` | 32 | 0 | — | F-326 |
| 4 | `farmstorm.configuration.` | 178 | 0 | — | F-326 |
| 5 | `entity.farmstorm.` | 13 | 0 | — | F-326 |
| 6 | 其余（effect 4 / subtitles 3 / death 6 / tooltip 20 / villager 1 / gui 1 / container 1 / itemGroup 1 / key 2 / command 3） | 42 | 0 | — | F-326 |
| **合计** | | **321 × 2** | **0** | | `28+28+32+178+13+42 = 321` |

**两条纪律**：① 本版不新增注册项、不新增 config 键、不改任何注册名 ⇒ 语言键无动因（58 号 §3.3 的删除项为 0）；② 唯一改了文字的地方是 `util/FarmstormTags.java:29` 的**代码注释**（F-323），注释不进语言文件（D-248）。两份语言的键集相等由 `ModLangProvider.java:12-13` 的同一张表按列写盘保证；检查器只比键集相等、**不校验 321**（`verify_project.py:629-633`），故 321 只能由 F-326 的人工计数兜住。

### 表 E · 风险 **RT-83 ~ RT-91（9 条）** → 任务

| 编号 | 风险（60 号 §9） | 负责验证的任务 | 该任务怎么验 |
|---|---|---|---|
| **RT-83** | 客户端配方表可用性只有静态链 | F-324、F-326 | F-324 的验收 4（双客户端音频轨）；F-326 的 #25 实跑一次，未通过按 RT-83 登记、不写成已验证 |
| **RT-84** | 客户端判据与服务端裁决的落差 | F-327 | signoff 的已知影响 4 条里第 1 条，标「不是缺陷」（D-251） |
| **RT-85** | 己方手持方块右键核心的预测与开界面落差 | F-325、F-327 | F-325 的验收 2（客户端不做归属判定）；F-327 把「一次视觉闪动」写成已知影响 |
| **RT-86** | 新标签成员没登记 kind 映射 | F-317 | 验收 3（`KIND_BY_BLOCK` 两个值各 1）；取到 `null` 即 `continue` 的分支留在源码里可读 |
| **RT-87** | 转换器 `syncCore` 只写调用不写方法 | F-314 | 验收 1（定义 1 + `sendBlockUpdated` 1）+ 死命令 2 |
| **RT-88** | 「只有放置者听不见」与「副手放置」未实测 | F-324、F-327 | F-324 的验收 4 双客户端双音频轨；F-327 的三项未实测里占 2 条 |
| **RT-89** | xp 常量落点错位留下第二份真相 | F-318 ~ F-321 | F-318 的验收 2（`public` 5 / `private` 0）、F-320 与 F-321 的 `VILLAGER_XP` 命中 0、F-319 的悬挂链接 0 |
| **RT-90** | `NORMAL` 的 22 项顺序被顺手重排 | F-320、F-326 | F-320 的验收 4（22 行序号注释逐行仍在）；F-326 的 #18 四个切片各 1 |
| **RT-91** | 把静态读链或 datagen 的 mtime 当结论 | F-326、F-327 | F-326 的 #22 只看路径不看时间；F-323 的验收 2 按内容比对标签产物 |
| **合计** | **9 条** | | **9 / 9，未覆盖 0** |

**F-328 已落地，不引入新风险**：它删的是无人读取的死类与三处悬挂引用，不新增注册项、不改 config 键值、不碰任何行为面；九条风险的责任任务一栏因此一行不改。

### 表 F · 待验证 API / 技术前提 **12 条** → 任务

| # | 待验证的语义（60 号出处） | 由哪个任务在哪一步确认 | 确认不了时的退路 |
|---|---|---|---|
| 1 | 客户端 `getRecipeManager()` 的配方表非空（§4.4.3、RT-83） | F-324 的验收 4 | 若为空 ⇒ 该窗口内退回 0.2.3 表现（本地音效缺失），按 RT-83 登记、不改谓词 |
| 2 | `convertible` / `admissible` 在客户端可调（§4.4.3） | F-324 的验收 1 | 若调不到 ⇒ 谓词只留 `admissible` 一条并报作者，不许搬 `canInsert`（D-244） |
| 3 | `SUCCESS` 属「已消费」、客户端提前 return（`MultiPlayerGameMode.java:337-341`） | F-324 的验收 4 | 若音频轨仍 0 ⇒ 查服务端是否也在 return，按缺陷报作者 |
| 4 | 服务端广播排除放置者（`ServerLevel.java:994-1004`） | F-326 的 #25 | 若 B 侧 0 ⇒ 把读数记成「两侧都 0」，按缺陷处理（不许改成服务端播放范围，D-246） |
| 5 | `public static final int` 字面量被 javac 内联（§4.2.1 ②） | F-320 / F-321 的编译与读数 | 若出现类初始化顺序问题 ⇒ 把五个常量改为各类自持，仍只一处定义 |
| 6 | `Map.of` 的两条 `Block → Link.Kind` 取用（§4.1.6） | F-317 的验收 3 | 若取不到 ⇒ 改用静态 `HashMap` 初始化块，语义不变 |
| 7 | `sendBlockUpdated(..., Block.UPDATE_CLIENTS)` 在转换器上可用（§4.1.3） | F-314 的验收 1 | 若编译不过 ⇒ 与培养器同形照抄（该类无额外约束），不改协议 |
| 8 | `Predicate<BlockEntity>` 重载不加载区块（§4.1.4） | F-316 的验收 2 | 若误用 `level.getBlockEntity` ⇒ 按 `:39-42` 改回 `getChunkNow`，不改调用方 |
| 9 | `/data get block … Core` 对缺失键的读法（§8 #10） | F-317 的验收 5 | 若命令不报「不存在」⇒ 改读 `Held` / `Progress` 作对照，并记录命令原文 |
| 10 | `MerchantOffer` 第二实参位置是 xp（`BotanistTrade.java:90-91`） | F-320 的验收 2、F-321 的验收 1 | 若位置错 ⇒ 以 `Offers` 的 `xp:` 读数为准（F-326 的 #17） |
| 11 | 据点核心的 `BlockItem` 谓词（D-245，本文补充、作者可否决） | F-325 的验收 1 | 若作者改判「空手才 `SUCCESS`」⇒ 只改 `:131-132` 一处，同提交 |
| 12 | `FarmstormTags:29` 的注释改动不触发 datagen 产物变化（§3.2 第 2 条） | F-323 的验收 2 | 若产物变了 ⇒ 说明改到了成员，退回只改注释 |
| **合计** | **12 条** | **12 / 12 落到具体任务的具体步骤** | **12 / 12 给出可执行退路** |

---
## 4. 裁决台账（A-102 起）

本节收 **10 条**（A-102 ~ A-111）：全部是本文写作期新立，上游一字不改（差异只登记在这里）；A-111 登记的是写作期已落地的一次代码删除。

### 4.1 A-102 · 本版**无**中间态窗口，「中途 FAIL 即真 bug」
- **结论**：按 §0.3 的五条读数判定本版无窗口；执行期内任何一个 FAIL 都按缺陷处理，不许当期望红，也不许为「让它绿」提前落地后续任务。
- **依据**：唯一变化的数字是 Java 文件数（`152 − 2 + 1 = 151`），而 `verify_project.py:479-480` 只把源码拼成字符串做内容匹配、`:613-622` 数的是注册 ID 清单、`Farmstorm.java:180-183` 断言的是十二类注册数——三处都不读文件数；其余十一项全部 `− 0 + 0`；xp 半改态不进任何闸（`VILLAGER_XP` / `XP_LEVEL` / `MerchantOffer` 在检查器里命中 0）。
- **落地位置**：§0.3 全节；F-313 的验收 1（151 的落点）；F-326 的验收 2（复核）。
- **否决**：「照 0.2.3 的习惯先假设本版有窗口、把一段自检冻结起来」——冻结的代价是让一行根本不用改的源码成为返工来源，而本版没有第二个观察面。

### 4.2 A-103 · 核侧五处**合成一个 L 任务**（F-317），不拆「发现」与「清理」
- **结论**：`OutpostCoreBlockEntity` 的候选发现、缓存判定、绑定写入、迁移改绑、清理出口五处与 `KIND_BY_BLOCK` 同一提交落地，记 L（状态机）。
- **依据**：59 号附录 A 表下注 1「第 1 ~ 6 行是同一台机器的六个面，不能拆开验收」；只改清理不改发现等于 58 号 §6 第 5 条的否决面（半个修复）；只改发现不改清理则 D → A 那条迁移根本不发生（转换器仍停在死坐标，`:630-632` 对非空缓存一律跳过）。
- **落地位置**：§2 的 F-317；§0.5 死命令 5。
- **否决**：「拆成 F-317a（清理出口）与 F-317b（按标签发现）」——两者改的是同一文件同一方法的两个半段（`:604-649` 与 `:843-874`），拆开后中间态正好是被否决的半个修复，且第二个提交的回滚会让第一个的验收读数失效（违反判据 4）。

### 4.3 A-104 · 转换器「实现接口」与两个新方法同一提交；`convertible` 提权也归 F-314
- **结论**：`implements CoreBound`、`clearCore()`、`syncCore()`、`setCore` 补同步、`convertible` 由 `private` 改 `public` 五处同属 **F-314** 一个提交。
- **依据**：接口三方法缺一即编译不过（§0.5 死命令 1）；`syncCore` 全仓唯一定义在 `CultivatorBlockEntity.java:104-110`，转换器没有它（死命令 2、RT-87）；`convertible` 现为 `private`（`:129`），F-324 在 `block` 包调用它必须同批提权，否则 `convertible has private access`。
- **落地位置**：§2 的 F-314；§0.5 死命令 1 与 2；表 A.1 的表外动作 ①。
- **否决**：「把提权放进 F-324」——那会让 F-324 的提交同时含一个可见性变更与两个客户端分支，回滚粒度从「两个 `block/` 文件」变成三个包。

### 4.4 A-105 · 两个 record 的分量与全部实参同一提交；史诗类**先行**
- **结论**：`BotanistTrade`（F-320）与 `TierTrade`（F-321）各自把 record 分量、全部实参、删类常量、`getOffer` 引用一并提交；`BotanistEpicTrade`（F-319）排在 F-321 **之前**。
- **依据**：record 加分量的那一刻，22 个单实参字面量（`BotanistTrade.java:60-82`）与 `of` 的三参调用（`TierTrade.java:56-58`）立刻编译不过（死命令 3、4）；`:28` 的 javadoc 引用 `{@link TierTrade#VILLAGER_XP}`，若 F-321 先删掉该符号，这个链接在 F-319 之前是悬挂的。
- **落地位置**：§2 的 F-319 ~ F-321；§5.3 的推荐提交序列；§5.1 串行段 4。
- **否决**：「先删三个类常量、再逐个加分量」——删常量的那一步会让三处 `getOffer` 引用不到符号，三个类同时编译不过。

### 4.5 A-106 · 音效三处拆 **F-324（两台机器）+ F-325（据点核心）**，Phase 出口要求两者都落地
- **结论**：同根因的三处不在一个提交里：两台机器（同判据族、跨 `block/` 两个文件）归 F-324，据点核心（判据不同、且是 58 号标「本文补充，作者可否决」的一处）归 F-325；Phase 71 的出口判据要求两个任务都完成。
- **依据**：59 号附录 A 表下注 3 与 D-249 要求三处**同批交付**（版本面），但 D-245 的谓词是唯一可被作者改判的一处（58 号 §8 第 2 条）——与两台机器绑在同一提交，改判会连带回滚已验收的 R4 主体；两台机器之间则必须同提交：同一根因、同一提交后一次验收，拆开会出现「培养器听得见、转换器听不见」的同类现象时有时无。
- **落地位置**：§2 的 F-324 与 F-325；Phase 71 的阶段出口判据；F-325 的回滚影响。
- **否决**：「三处一个提交（含三个 `block/` 文件）」——回滚粒度把可改判的一处与照裁定落地的两处绑死；「三处三个提交」——转换器与培养器拆开就会出现同一版本内同类现象时有时无。

### 4.6 A-107 · 验收补一条**转换器**的对读（#25 只点名培养器）
- **结论**：F-324 的验收 4 在 60 号 §8 #25 之外，按同一读法把转换器复跑一遍：A 侧音频轨 `block.anvil.place` 1 次、B 侧 1 次。
- **依据**：#25 的操作写的是「A 手持泥土右键培养器顶面」（`60-TECH-0.2.4.md:533` 所在的那一行），只覆盖两台机器中的一台；D-243 的对象是**两台**机器的客户端分支（`CropConverterBlock.java:121-122`、`CultivatorBlock.java:117-118`），只读培养器会让转换器那半条分支没有行为读数。**本文补充，作者可否决**。
- **落地位置**：§2 的 F-324 验收 4；表 A.2 的第 25 行。
- **否决**：「照 #25 只验培养器」——`convertible` 与 `admissible` 是两条不同的谓词路径（查 `converting` 配方类型 vs 查输出档案），一条通了推不出另一条。

### 4.7 A-108 · `OutpostCoreBlockEntity` 的出口③ 两行文案**并入 F-317 同批改**
- **结论**：`:859`（出口③ 的注释）与 `:867`（出口③ 的日志串）并入 **F-317** 同批改，措辞不再特指培养器；`:863`（区块未加载那条日志）**不动**。**本文补充，作者可否决**。
- **依据**：两处与出口② 的 `:870-873` 同在一个 if 链（`:860-873`）里，不是表外文件——F-317 本来就动这一段的判据；判据改按 `processing_block` 标签之后，`:867` 写「该格已不是培养器」在转换器那一格上是事实错误，与 D-248 订正 `FarmstormTags.java:29` 是同一性质（判据改了、文字没跟），成本 1 ~ 2 行、零行为影响、零数字变化。
- **落地位置**：§2 的 F-317 交付物（`:859`、`:867` 同批改，`:863` 不动）与验收 7（旧串 `迁移失败且该格已不是培养器` 命中 0、新串 `迁移失败且该格已不是处理方块` 命中 1、`:859` 行内「培养器」命中 0）。
- **否决**：「按 A-108 的初版保留原文」——那会让同一个 if 链里出现「判据按标签、日志按类」的自相矛盾，下次排障的人会按「这是培养器」的错误方向查；也否决「把 `:863` 一起改」——出口③ 的第一条日志与判据无关，改它只增加回滚面。

### 4.8 A-109 · **151** 只进散文与 F-313 的读数，不进检查器
- **结论**：`152 − 2 + 1 = 151` 出现在 F-313 的验收 1、F-326 的 #3 与 F-327 的签字三处；`tools/verify_project.py` 的 `EXPECTED` / `EXPECTED_COUNTS` 本版一行不改。
- **依据**：`:479-480` 只做内容拼接、`:613-622` 数的是 ID 清单、`:637-653` 是按文件的六串禁令扫描——加一条文件数断言等于给检查器增一条与本版行为无关的硬编码（58 号 D-227 补充的否决面）。
- **落地位置**：§2 的 F-313 验收 1；F-326 的验收 2；F-327 的验收 3。
- **否决**：「把 151 加进 `expected_total`」——那会把注册 ID 总数与文件数混成一个数，下一次动注册面时会得出错的结论。

### 4.9 A-110 · 三个「只改一行 / 只改注释」的任务用 **grep 命中数**当断言
- **结论**：F-322（`0x4D000000`）、F-323（`:29` 注释）、F-312（6 处版本串）三个任务按判据 3 各给「命中数 + 归零项」两类断言，复杂度仍记 S（0.5 人日）。
- **依据**：判据 3 要求断言里出现具体数值或路径；三者都改不出可运行的行为差别，能观察的面只有源码字符串与产物面（F-322 另有像素读数）。F-322 的像素读数（alpha 77 / 255）与 F-312 的产物名读数是这两条 S 之外唯一进游戏的证据。
- **落地位置**：§2 的 F-312 验收 1、F-322 验收 1 ~ 2、F-323 验收 1 ~ 2。
- **否决**：「这三个任务因为改动极小就不写断言」——不写就等于把三条改动交给「记得小心」，违反判据 3。

### 4.10 A-111 · farmland 死代码删除已落地；A-62 由 58 号 **D-253 / D-254** 取代
- **结论**：`farmland/FarmlandGuard.java`（225 行）与 `farmland/FarmlandLedger.java`（555 行）连同三处悬挂引用一次删净，记为 **F-328**（L · 3.0 人日，**已落地**）。A-62 的原话是「字段声明保留，因为 `FarmlandGuard` / `FarmlandLedger` 两处 `.get()` 仍在」；两个类已不存在，该前提由 **D-253** 取消，`FarmstormConfig` 里那个字段的处置由 **D-254** 给出（随三处连带改动一并删），A-62 因此被取代。**本文补充，作者可否决**。
- **依据**：裁定 #8（`.work/v024-BRIEF.md:141`：「删除 v0.2.0 撤销耕地后遗留的 `farmland` 死代码……**已落地**：5 文件、857 行删除、0 插入」）；该笔 `git diff --stat` 为 5 files changed、`857 deletions(-)`、`0 insertions`；`grep -rn "Farmland\|farmland" src/main/java` 命中 **2**（只剩 `ModLangProvider.java:39-40` 两行历史记账注释）、`grep -rn "outpostCoreFarmlandBonus" src/main/java` 命中 **0**；Java 文件数实点 **150**（`152 − 2 = 150`）；`python -X utf8 tools\verify_project.py` 复跑 **FAIL 0 / WARN 1**，`92 / 175 / 321` 三段读数一字未变。
- **编号出处**：按 60 号 §8 第 22 条的三段口径——**D-253** = 删两个类（`152 − 2 + 1` 里的 −2），**D-254** = 三处连带改动（`config/FarmstormConfig` / `event/FarmstormGameEvents` / `outpost/OutpostCoreIndex`）；59 号 `:52`、`:140`、`:386` 的「152 − 2 + 1」同记这条链。
- **落地位置**：§2 的 F-328；表 A.1 的第 24、25 行与表外动作 ⑤；§1 的 F-328 行；§0.3 第 1 与第 4 条；表 C 第 2 行；§6 第 2 条。
- **否决**：「照 A-62 保留字段声明」——两个读取方都不存在了，保留等于在 config 类里留一个无人读、也没有 `.define` 的孤儿字段；也否决「把删除摊到多个任务」——两个类互不引用，一次删完只有一个回滚点，摊开就多出两个半截状态。

---
## 5. 执行顺序

### 5.1 必须串行的段（五段）

| 段 | 串行链 | 为什么不能并行 |
|---|---|---|
| 1 | F-313 → F-314 → F-317 | 接口 → 转换器实现 + 清缓存 → 核侧按接口调用，后一步读前一步的符号 |
| 2 | F-313 → F-315 → F-317 | 培养器的接口声明是核侧 `instanceof CoreBound` 判定的前提 |
| 3 | F-316 → F-317 | 收集面重载先存在，核侧才删得掉两次按类收集 |
| 4 | F-318 → {F-319, F-320, F-321} | 五个常量是三个交易类引用的唯一来源 |
| 5 | F-326 → F-327 | 25 条验收读数 → 签字与 README 增量段 |

**F-328 不进任何串行段**：它已落地，无前驱也没有后继（没有任务读它删掉的符号），既不占串行链，也不占 §5.2 的并行位。

### 5.2 可并行的簇（四簇）

| 簇 | Phase | 可并行任务 | 并行度 | 说明 |
|---|---|---|---|---|
| A | 66 ~ 68 | F-312、F-313、F-316 | 3 | 三者文件集互不相交（`gradle` + 两支 `.cmd` / 新建接口 / `util` 收集面） |
| B | 69 ~ 71 | F-318、F-322、F-323、F-325 | 4 | 分属 `event/`、`client/`、`util/`、`block/`，互不相交 |
| C | 71 | F-324 | 1 | 等 F-314 的 `public convertible`；与 F-325 可同批提交但不同任务 |
| D | 70 | F-322、F-323 | 2 | 两个 S 任务，各自一条 grep 判据 |

**并行度上限 3**。实际受**同文件冲突**约束：`OutpostCoreBlockEntity.java` 只被 F-317 改一次；`VillagerTradeEvents.java` 只被 F-318 改一次；三个交易类各被一个任务改一次；`CropConverterBlockEntity.java` 只被 F-314 改一次。按「一个任务只动自己那一段、不在别人的行上做格式修正」的约定，可稳定并行 2 ~ 3 路。

### 5.3 推荐提交序列

```
F-328（删两个死类，已落地，Java 150）→ F-313（接口，151 落地）→ {F-312, F-316}
      → {F-314 → F-315} → F-317（需求 1 收口）
      → F-318 → {F-319 → F-320, F-321}（需求 2 收口）
      → {F-322, F-323}（需求 3 与注释收口）
      → {F-324, F-325}（需求 4 收口）
      → F-326（25 条验收）→ F-327（签字）
```

**读序列四条**：① **F-313 在新增文件里最先**——F-328 只有删除、不产生新文件，151 这个数（`152 − 2 + 1`）从 F-313 之后成立，后面所有任务的读数都在它之后；② **F-317 必须等 F-314 与 F-315**，否则核侧按接口调用时两个实现类还没进接口集合；③ **F-319 排在 F-321 之前**（A-105），避免 `:28` 的 `{@link TierTrade#VILLAGER_XP}` 悬挂；④ **F-326 必须最后**：它要读的是 25 条终态读数，任何一个前置任务未落地都会让 #3、#16、#17、#22 四条读数失真。

### 5.4 关键路径

```
F-313 接口          0.5  （累计 0.5）
 └─ F-314 转换器     1.5  （累计 2.0）
     └─ F-317 核侧   3.0  （累计 5.0）
         └─ F-326 验收 3.0（累计 8.0）
             └─ F-327 签字 1.5（累计 9.5）
```

**关键路径 = 9.5 人日 / 5 个任务**，占总量 21.0 的 `9.5 ÷ 21.0 = 45.2%`（`9.5 ÷ 21.0 = 0.4523…`，留一位小数）。第二长的链走音效面（F-314 → F-324 → F-326 → F-327）= `1.5 + 1.5 + 3.0 + 1.5 = 7.5`；第三长走交易面（F-318 → F-321 → F-326 → F-327）= `0.5 + 1.5 + 3.0 + 1.5 = 6.5`。三条链共享链尾（F-326、F-327）。

> **读法**：想缩短日历工期，先压 F-317（`5.0` 累计里占 3.0，且它同时是需求 1 的唯一验收面）；不要压 F-326 ~ F-327（`4.5` 人日已在链尾，压它等于把 25 条读数做薄）。

### 5.5 人日汇总

| Phase | 名称 | 任务 | 任务数 | S | M | L | 人日 | 里程碑 |
|---|---|---|---|---|---|---|---|---|
| 66 | 发布面与死代码清除 | F-312、F-328 | 2 | 1 | 0 | 1 | `0.5 + 3.0` = **3.5** | **M62** |
| 67 | 绑定契约：接口与两个实现 | F-313 ~ F-315 | 3 | 2 | 1 | 0 | `0.5 + 1.5 + 0.5` = **2.5** | **M63** |
| 68 | 收集面与核侧统一 | F-316 ~ F-317 | 2 | 1 | 0 | 1 | `0.5 + 3.0` = **3.5** | **M64** |
| 69 | 交易经验分级 | F-318 ~ F-321 | 4 | 2 | 2 | 0 | `2 × 0.5 + 2 × 1.5` = **4.0** | **M65** |
| 70 | 浮窗底色与标签注释 | F-322 ~ F-323 | 2 | 2 | 0 | 0 | `2 × 0.5` = **1.0** | **M66** |
| 71 | 放置音效客户端预测 | F-324 ~ F-325 | 2 | 1 | 1 | 0 | `1.5 + 0.5` = **2.0** | **M67** |
| 72 | 验收与签字 | F-326 ~ F-327 | 2 | 0 | 1 | 1 | `3.0 + 1.5` = **4.5** | **M68** |
| **合计** | | **F-312 ~ F-328** | **17** | **9** | **5** | **3** | **21.0** | **M62 ~ M68** |

- **裸人日**：`9 × 0.5 + 5 × 1.5 + 3 × 3.0 = 4.5 + 7.5 + 9.0 = 21.0 人日`
- **含 25% 缓冲**：`21.0 × 1.25 = 26.25 人日`
- **三条互相校验**：`9 + 5 + 3 = 17` = 任务数；七个 Phase 小计之和 `3.5 + 2.5 + 3.5 + 4.0 + 1.0 + 2.0 + 4.5 = 21.0`；`21.0 ÷ 3 = 7.0`（稳定 3 路并行），而**关键路径下界 9.5 人日**才是真实下界（`9.5 > 7.0`，三路均分压不到链长）。

---
## 6. 本文自检

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 编号连续 | 任务 `grep -c "^#### F-"` = **17**、区间 `F-312 ~ F-328`；Phase `66 ~ 72` 共 **7** 个；里程碑 `M62 ~ M68` 共 **7** 个；裁决 `A-102 ~ A-111` 共 **10** 条 | 通过 |
| 2 | 表 A.1 覆盖 | 58 号 §3 的动作面 **23 / 23** 有落点，加裁定 #8 的删除 **2** 行（新增 6 + 修改 17 + 删除 2 = 25）；表外动作 **5** 项各自登记 | **25 / 25** |
| 3 | 表 A.2 覆盖 | 60 号 §8 的 **25 / 25** 有落点；一条挂多任务 **3** 条（#3、#16、#21）各自写明拆法 | **25 / 25** |
| 4 | 表 B / C / D 覆盖 | 注册面 12 类逐类合计 `28+28+8+13+4+3+1+1+1+3+1+1 = 92` 且增量全 0；config `175 + 3 = 178` 六行；语言键六组 `28+28+32+178+13+42 = 321`（× 2），增量全 0 | **12 + 6 + 6 行** |
| 5 | 表 E / F 覆盖 | 风险 **9 / 9**（RT-83 ~ RT-91 连续）；待验证 API **12 / 12**，每条给退路 | **9 + 12 条** |
| 6 | 每个任务 ≥ 1 条可观察断言 | 17 个任务逐条读过：断言全部落在源码 grep 命中数、命令输出、`/data get` 读数、日志行、像素、音频轨六类上；无感受类措辞 | 通过 |
| 7 | 五个字段齐全 | 17 个任务各含 **目标 / 交付物 / 验收断言 / 依赖 / 回滚影响**，无缺项；交付物全部给到 `文件:行号` | **17 / 17** |
| 8 | 总表与详情一一对应 | §1 的 17 行任务号、Phase、里程碑、复杂度、人日与 §2 的标题行逐条相等 | 通过 |
| 9 | 依赖无环、编号单调 | 每个依赖编号都小于自身编号；无前驱 **8 个**（F-312、F-313、F-316、F-318、F-322、F-323、F-325、F-328） | 通过 |
| 10 | 中间态窗口回答 | §0.3 判定**无窗口**并给五条读数与出处；明写「本版没有期望红的断言，任何一个中途 FAIL 都是真 bug」；A-102 把该判定落成裁决 | 通过 |
| 11 | 五条顺序死命令逐条有落点 | §0.5 的五条分别落在 F-314（#1、#2）、F-320（#3）、F-321（#4）、F-317（#5），且每条在 §2 的任务正文里被再点一次 | 通过 |
| 12 | 工时自洽 | `9 × 0.5 + 5 × 1.5 + 3 × 3.0 = 21.0`；七个 Phase 小计相加 = 21.0；`21.0 × 1.25 = 26.25`；`S + M + L = 9 + 5 + 3 = 17` | 通过 |
| 13 | 只发四类号 | 本文只发 **F-312 ~ F-328 / Phase 66 ~ 72 / M62 ~ M68 / A-102 ~ A-111**；其余系列写全前缀（D-225 起、D-253 / D-254、B-59 起、HC-173 起、N-155 起、RT-83 起、E-66-1 起），未认领任何下游编号 | 通过 |
| 14 | 行号实读 | 本文引用的每处行号已按 `read` 工具逐条复核（清单见 §0.4 第 1 条），未抄上游结论 | 通过 |
| 15 | 禁用词与语气 | 全文无未定事项标记、无占位符；人称只用「作者」「实现者」，无第二人称敬语；结论句一律祈使；无感受类措辞 | 通过 |
| 16 | 与上游无冲突 | 58 号的 28 条决策、59 号的 15 条边界、60 号的 25 条验收与 9 条风险全部只引用不改写；本文补充的 4 处（A-107 的转换器对读、A-108 的出口③ 两行文案、A-110 的断言口径、A-111 的死代码删除与 A-62 取代）标「本文补充，作者可否决」 | 通过 |

---
> **本文的六个可复核判据**（交稿时逐条给读数）：任务 **17** 个 / Phase **7** 个 / 里程碑 **M62 ~ M68** 共 **7** 个 / 裸 **21.0 人日**（缓冲后 **26.25**）/ 六张覆盖表（表 A 分两半）共 `25 + 25 + 12 + 6 + 6 + 9 + 12 = 95` 行 / 中间态窗口 **无**。