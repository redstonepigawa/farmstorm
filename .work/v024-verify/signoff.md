# F-327 · v0.2.4 签字：四条影响面、三项待进游戏项、十项读数与九条风险

> 执行者：收尾子代理（调度者 `session-b777045e-99f1-48ef-93c9-a66b94ac60e6`）｜工作目录 `D:\project_farm`
> 依据：`docs/zh/61-TASKS-0.2.4.md` F-327；`docs/zh/63-PROMPT-TASKS-0.2.4.md` Phase 72（E-72-1 ~ E-72-7）；`docs/zh/59-DESIGN-0.2.4.md` §7.1 ~ §7.4；`docs/zh/58-DECISIONS-0.2.4.md` D-227 / D-251 / D-252；`docs/zh/60-TECH-0.2.4.md` §9（RT-83 ~ RT-91）
> 读数来源：`.work/v024-verify/acceptance.md`（25 条，只读引用）+ 本轮实跑（build / 检查器 / 计数命令 / 只读 `git diff` / `git status`）
> 边界自检：本轮只改 `README.md` 与新建本文件；`src/`、`tools/`、`docs/zh/*`、`docs/en/*`、`plan0.2.4.txt`、`.work` 既有文件一行未动；未跑任何 git 写操作。

---

## 0. 结论摘要（七行）

| 面 | 结论 | 落点 |
|---|---|---|
| 影响面 4 条（59 号 §7.1 ~ §7.4） | 逐条成文，每条标「不是缺陷」 | §2.1 ~ §2.4（第 5 条是一次视觉闪动，RT-85） |
| D-252 的三项 | 逐条按未验证登记，不写已验证 | §3.1 ~ §3.3 |
| 十项读数 | 全给（值 + 出处） | §1 |
| 九条风险 RT-83 ~ RT-91 | 各有读数 | §4 |
| 与文档不一致 | **10** 条 | §5 |
| #22 验收 | **字面不通过 / 等价判据通过**（不写「通过」） | §5.1、§6.3 |
| build 与检查器 | build 退出码 **0**；检查器 **92 / 92 · 175 · 321 · FAIL 0 / WARN 1** | §7 |

---

## 1. 十项读数（每项「值 + 出处」）

| # | 项 | 值 | 出处（命令原文 / 文件行） |
|---|---|---|---|
| 1 | Java 源文件 | **151** | `(Get-ChildItem -Recurse src\main\java -Filter *.java -File).Count` = 151（`152 − 2 + 1`，D-227 / D-253 / D-228）；旁证 `acceptance.md` §2 #3 |
| 2 | 注册面 | **92** | 检查器 §2 `已核 registry 对象: 92 / 92`（12 类逐项见 §7）；旁证服务器首启日志恰 1 行 `自检 注册对象 92 个 … → OK`（`acceptance.md` §2 #2） |
| 3 | server config | **175** | 检查器 §1 `生成 toml 键数: 175` 与 `[OK] 175 个配置键与文档逐字一致（toml 175 键）`；`run-v024/config/farmstorm-server.toml` 803 行、**60** 段（`^\s*\[`）、**175** 行 `键 = 值`（`^\s*[A-Za-z0-9_.-]+ = `） |
| 4 | client config | **3** | `config/FarmstormClientConfig.java` 三处定义：`.define("enabled", true)`、`.defineInRange("x", …)`、`.defineInRange("y", …)`（`hud.enabled` / `hud.x` / `hud.y`） |
| 5 | 语言键 | **321 × 2** | `src/generated/resources/assets/farmstorm/lang/en_us.json` keys=**321**、`zh_cn.json` keys=**321**（`ConvertFrom-Json` 数属性）；检查器 §3 `[OK] lang 键集一致，共 321 键` |
| 6 | `configuration` 组 | **178** | 同上两份 lang 里 `farmstorm.configuration.*` 前缀各 **178**（178 = 175 server + 3 client，D-227） |
| 7 | 自定义网络包 | **2** | `network/FarmstormNetwork.java` `.playToClient(` occ=**2**（`:42` `CoreListPayload`、`:47` `CultivatorLinkPayload`）；`PROTOCOL_VERSION = "3"` occ=1 |
| 8 | 进度 | **16** | `src/generated/resources/data/farmstorm/advancement/*.json` = **16** 个文件 |
| 9 | 转换配方 | **21** | `.../data/farmstorm/recipe/*.json` 共 **25** 个，其中带 `farmstorm:converting` 的 **21** 个（余 4 个是合成配方 `crop_converter` / `cultivator` / `outpost_core` / `compressed_puffed_wheat`） |
| 10 | 交易 listing | **26** | 22（`BotanistTrade.NORMAL` = 6 + 6 + 6 + 4）+ 1（`BotanistEpicTrade` 史诗）+ 3（`TierTrade` 农民三档）；`acceptance.md` §3 #17 的 `xp:` 分组 **7 / 6 / 7 / 4 / 2 = 26** |

旁证（不计入十项）：`build/libs` 里 `farmstorm-0.2.4+mc1.21.1.jar` **1,125,593 B** / SHA256 `0D6B68EFFF745D84F78A51B21129C72A7D3EB0A536CA759AB09E416AEA3A021A`；`farmstorm-0.2.4+mc1.21.1-sources.jar` **1,071,152 B** / SHA256 `971B0692438AF9EDC744E76ED73B73515F5B1EFEB63283022758D0B7436923F6`；`gradle.properties` 的 `mod_version=0.2.4`。本轮**不发布 Release**。

---

## 2. 四条影响面（59 号 §7.1 ~ §7.4）

本节四条都标「**不是缺陷**」：它们是本版改完以后玩家会感觉到的变化，不是缺陷、不是回归（D-251）。

### 2.1 已知影响 ① · 客户端只看品类，服务端还看范围 / 绑定 / 手持 / 空槽（59 号 §7.1）

- **玩家会看到什么**：手上拿的**不是**机器认的输入物（或者机器槽里已有货、机器未绑定）时，右键**无反应**——不消耗、不报错、界面也不开。机器认的那一档照旧能放进去。
- **为什么会这样**：客户端判的是「这次右键会不会被收走」（只看物品品类），服务端判的是「现在能不能收」（范围 ∧ 绑定 ∧ 手持档 ∧ 空槽）。两侧判据不同，落差的这一侧就是「没反应」（D-244、D-251、RT-84）。
- **不是缺陷**：服务端四道闸**一行未改**——`CropConverterBlockEntity.canInsert`（`:141`）与 `CultivatorBlockEntity.canInsert`（`:152`）仍是「范围 ∧ 绑定 ∧ 手持 ∧ 空槽」一次判定；客户端只多了「这次右键会不会被接走」的品类预测。「行为面无反应」是 0.2.3 就有的取舍（D-251 补充）。
- **读数**：三处客户端分支 `CropConverterBlock:122`（谓词 `convertible`）、`CultivatorBlock:118`（`admissible`）、`OutpostCoreBlock:132`（`BlockItem`）；服务端 `canInsert` 调用各 **1 / 1 / 0** 处，且都在客户端分支**之外**（`CropConverterBlock:133`、`CultivatorBlock:129`）。

### 2.2 已知影响 ② · 核心浮窗底色变透（59 号 §7.2）

- **玩家会看到什么**：浮窗面板后面的世界比以前清楚，面板内文字靠不透明边框与三种文字色维持可读性。
- **不是缺陷**：裁定 #3 把「30%」读作不透明度（D-241）；边框、文字三种颜色、几何与版式、三个 client config 键都不动（D-242、D-227）。
- **读数**：`client/CoreHudRenderer.java:40` `COLOR_BACKGROUND = 0x4D000000`（alpha `0x4D` = **77** ⇒ 77 ÷ 255 = **30.2%**）、`:41` `COLOR_BORDER = 0xFF000000`（alpha **255**）；v0.2.3 的 `0x80000000` 全文件 **0** 处；绘制顺序 `:77` 先铺边框色、`:78` 再内缩 1 px 铺底色。面板截图取色需要图形客户端，`acceptance.md` #20 记为「未验证」。

### 2.3 已知影响 ③ · 大师级单次交易经验 30 点（59 号 §7.3）

- **玩家会看到什么**：跟大师级（5 级）村民做生意，村民经验涨得明显快——单次 **30 点**，是 0.2.3 那 **2 点**的 **15 倍**（30 ÷ 2 = 15，D-238）；1 ~ 4 级是 **2 / 5 / 10 / 15**，只是「有差别」。
- **性质**：**按级取经验，不按档取**——同一档位在不同等级拿到的经验不同，只影响村民的升级速度，不影响任何产出与价格（价格区间、单次数量、成交次数、22 项顺序都不动，D-237）。
- **不是缺陷**：需求 2 的正面实现（D-236 ~ D-239）。
- **读数**：`event/VillagerTradeEvents.java:33-37` 五个 `public static final int` = **2 / 5 / 10 / 15 / 30**；`acceptance.md` §3 #17 的 `xp:` 分组里 `xp:30` 恰 **2** 条（植物学家史诗 1 + 农民 EPIC 1）。

### 2.4 已知影响 ④ · 绑定改由 `processing_block` 标签驱动（59 号 §7.4）

- **玩家会看到什么**：本版**没有**玩家可见变化；下一个版本里「能被核心绑定的方块」会跟着 `farmstorm:processing_block` 标签一起长出来，不用再改核侧代码。
- **必须先做什么**：同一批改动里登记「方块 → `Link.Kind`」映射，否则新成员只能被发现、绑不上，且**日志不增行**（静默，D-235 / RT-86）。
- **不是缺陷**：标签驱动是作者拍板的方向（BRIEF §7 #1、D-230 / D-235）。
- **读数**：`blockentity/OutpostCoreBlockEntity.java` 的 `KIND_BY_BLOCK` occ=**2**（`:123` **声明** `private static final Map<Block, Link.Kind> KIND_BY_BLOCK = Map.of(` + `:635` **取值** `KIND_BY_BLOCK.get(candidate.getBlockState().getBlock())`），`KIND_BY_BLOCK = Map.of(` occ=**1**（映射表唯一）；映射块内 `Link.Kind.CULTIVATOR` 1（`:124`）、`Link.Kind.CROP_CONVERTER` 1（`:125`）。发现面改按标签：`AreaBlockEntities.collect` occ=1（`:621`），`instanceof CultivatorBlockEntity` 全文件 **0** 处（`acceptance.md` §1 #6 / #7）。

### 2.5 附：己方手持方块右键据点核心会先走一次放置预测（RT-85，D-245）

- **玩家会看到什么**：界面照常打开，同时客户端预测出的方块被服务端的方块更新回滚——**一次视觉闪动**，方块不会真的放下。
- **为什么**：`block/OutpostCoreBlock.java:132` `if (level.isClientSide)` → `:133-135` 手持 `BlockItem` 返回 `PASS`（让路给原版放置预测），服务端仍在同一方法的 `:130` `useWithoutItem` 里 `openMenu`（`:144`）。
- **不是缺陷**：这是 D-245 的既定取舍（若改判「空手才 `SUCCESS`」，改的仍是同一处）。

---

## 3. D-252 的三项

三项都只能靠「进游戏跑一遍」定，本会话只有一个无头专用服务器、没有图形客户端、没有第二台机器、没有音频采集，因此**不写成已验证**。每项给「原因 + 替代读数」。

### 3.1 只有放置者听不见 —— 未实测

- **原因**：判据要 A / B **两个客户端各一条音频轨**（录屏数 `block.anvil.place` 命中次数），本会话无客户端、无显示设备、无音频采集（`acceptance.md` §3 #25 的结论是「未验证」）。
- **替代读数**：静态链 `ServerLevel.java:994-1004`（服务端广播排除放置者）与 `ClientLevel.java:527-529`（本地播放只给本人）⇒ 净效果「只有本人听不见」；`src/main/java/com/dashpig/farmstorm/block/` 下 `playLocalSound` / `playSound` / `SoundEvent` / `SoundEvents` 命中 **0**（本模组既不在服务端补播、也不在客户端自播）；`sound_event` 注册仍 **3** 个（D-246 禁令，未新增播放点）。

### 3.2 副手放置 —— 未实测

- **原因**：同 3.1，还需要「副手放置」这一动作的双客户端录音；单人环境只验一侧会把「服务端广播有效」当成修复完成（RT-88）。
- **替代读数**：`ServerPlayerGameMode.java:369` 的 `MAIN_HAND` 判定 ⇒ 副手放置本来就不满足该分支，**推测本来就有音效**（D-252 ②）；本版没有给任何方块换 `SoundType`（`FarmstormBlocks.java:78` / `:108` 仍是 `SoundType.ANVIL`）、也没有新增音效资源。

### 3.3 客户端 `getRecipeManager()` 可用性 —— 未实测

- **原因**：需要图形客户端进服、在客户端配方表到手之后右键一次；本会话起不了客户端（RT-83）。
- **替代读数**：`blockentity/CropConverterBlockEntity.java:157` 的 `convertible()` 走 `level.getRecipeManager()`（`:224` 的匹配同源）；静态依据 `ClientLevel.java:609-610`；失败模式是客户端一律 `PASS`，退回 0.2.3 的表现（本地音效缺失），**不产生错误方块**，也不改任何服务端状态。

---

## 4. 九条风险 RT-83 ~ RT-91（各有读数）

| 风险 | 本轮读数 | 结论 |
|---|---|---|
| **RT-83** 客户端配方表可用性只有静态链 | `CropConverterBlockEntity.java:157` 与 `:224` 都走 `level.getRecipeManager()`；客户端无配方表 ⇒ 谓词恒假 ⇒ 一律 `PASS` | 静态链成立；进游戏一项见 §3.3，**不写已验证** |
| **RT-84** 客户端判据与服务端裁决的落差 | 客户端三处分支 `CropConverterBlock:122` / `CultivatorBlock:118` / `OutpostCoreBlock:132`；服务端 `canInsert` 各 1 / 1 / 0 处、都在客户端分支之外 | 落差已作为设计内行为登记（§2.1） |
| **RT-85** 手持方块右键核心的预测落差 | `OutpostCoreBlock:132` 客户端分支 → `:133` `getMainHandItem().getItem() instanceof BlockItem` ⇒ `PASS`；服务端 `:130` `useWithoutItem` 仍 `:144` `openMenu` | 一次视觉闪动，方块不落地（§2.5） |
| **RT-86** 新标签成员没登记 kind 映射 | `KIND_BY_BLOCK` occ=2（`:123` 声明 + `:635` 取值）、`KIND_BY_BLOCK = Map.of(` occ=1；`Link.Kind.CULTIVATOR` 全文件 3（`:80` 注释 / `:124` 映射 / `:968` 注释）、`Link.Kind.CROP_CONVERTER` 1（`:125`） | 映射表唯一；未登记的新成员会被发现但绑不上（静默） |
| **RT-87** `syncCore` 只写调用不写方法 | `CropConverterBlockEntity.java`：`private void syncCore` occ=1、`sendBlockUpdated` occ=1 | 「只写调用不写方法」未复现 |
| **RT-88** 「只有放置者听不见」与「副手放置」两条现象 | 两条现象的证据与替代读数见 §3.1 / §3.2（同一份：本会话无第二客户端音频轨） | 两条都按 §3 的口径登记，不写已验证 |
| **RT-89** xp 常量落点错位留下第二份真相 | `VillagerTradeEvents.java:33-37` 五个常量都是 `public static final int`（`private static final int` 全文件 **0**）；`VILLAGER_XP` 在 `BotanistTrade` **0** / `TierTrade` **0** / `BotanistEpicTrade` **2**（`:46` 声明 + `:64` 实参） | 无第二份真相 |
| **RT-90** `NORMAL` 的 22 项顺序被重排 | 四个切片逐字在 `VillagerTradeEvents.java:56-59`：`subList(0, 6)` / `(6, 12)` / `(12, 18)` / `(18, 22)` ⇒ 6 + 6 + 6 + 4 = **22** | 顺序未重排 |
| **RT-91** 把静态读链或 datagen 的 mtime 当结论 | 只读 `git diff --name-status 52749b9 HEAD -- src` = **19** 个路径；`git status --porcelain -- src` = **0** 行；`src/main/resources` + `src/generated/resources` 路径 **0**；标签产物按内容比对（`processing_block.json` 成员未变） | 未以 mtime 或静态链当结论 |

---

## 5. 与文档不一致 10 条（引用 `acceptance.md` §5；本轮只登记，未改上游）

1. **#22 的判据假设「未提交工作树」**：判据要 `git status --porcelain` 出现 19 个 `.java`；本版按「一 Phase 一提交」落地，工作树对 `src` 报 **0**，字面不通过。等价读数是 `git diff --name-status 52749b9 HEAD -- src` = **19** 个路径（构成 14 + 3 + 2，见 §6.3）。**处置：判据改写为「字面不通过 / 等价判据通过」**。
2. **#8 的 `KIND_BY_BLOCK` 字面命中 2 而非 1**：`:123` 声明 + `:635` 取值；判据写「命中 1」按**声明处**成立。建议判据改成 `KIND_BY_BLOCK = Map.of(` 命中 1（现读数 1）。**处置：声明 + 取值两个读数并列登记**。
3. **#15 的「`= 2`」裸子串 1**：全文件唯一那处是 `:39` 的价格注释 `/** 价格上界：64 − 40 + 1 = 25 个取值。 */`（被 `= 2` 误中，红线 6 不许改）；**常量声明里 `= 2` 命中 0**。建议判据写成 `VILLAGER_XP = 2` 命中 **0**（现读数 0）。
4. **#18 的 `get(1)` / `get(3)` / `get(5)`**：判据的「各 1」只在**农民段**（`:67-69`）成立；**全文件各 2**（植物学家段 `:56` `get(1)`、`:58` `get(3)`，史诗段 `:61` `get(5)`）。建议判据写「`:67-69` 三行各 1」或「全文件各 2」。
5. **#9 的行号整体平移**：判据写 `:859` / `:863` / `:867` / `:872`，现为 `:863` / `:867` / `:871` / `:876`（前面插入的 javadoc 与 `KIND_BY_BLOCK` 块把行号推后）；四处**文案**逐项成立。
6. **#10 / #11 缺一条前置条件**：判据没写「核必须有归属者」。`settleOnRemoval()` 与 `rebindNearby()` 的第一行 `ownerUuid == null` 就早退（裁定 #13），`/setblock` 出来的无主核两条路径都不触发（第一轮 `acceptance.md` 日志即此）。**处置：判据补「核有归属者」后重跑，两条都通过 ⇒ 登记为「判据缺前置，已补后通过」**。
7. **63 号 E-72-3 写 14，而 60 号 §8 #22 与 61 号 F-326 都是 19**：`docs/zh/63-PROMPT-TASKS-0.2.4.md:420` 写「恰 **14** 个 `.java`（`12 + 1 + 1`）」。本记录按 60 / 61 的 **19**（`14 + 3 + 2`）执行。
8. **#24 判据写 `run/config`，实际 `run-v024/config`**：两份读数逐项相同（803 行 / 60 段 / 175 键），已并列给出；不影响结论。
9. **`/echo` 不是服务端命令**：`/echo` 只在数据包函数里有效，RCON 里发 `echo ===X===` 一律回 `Unknown or incomplete command`（`acceptance.md` 各 `f326-cmds-*.txt` 有残留行）。
10. **RCON 分包会在字符串里插入空格**：b3 第一次聚合出现 `"minecraft: sugar_cane"`（冒号后多一个空格），当独立值会多算 1 个 distinct（该组一度显示 7）。已按去空格口径归一（6 个 distinct）。

---

## 6. 不改清单与负向取证

### 6.1 两条只读 git 读数（未跑任何写操作）

~~~text
git diff --name-status 52749b9 HEAD -- src
→ 19 行：M 16 / A 1 / D 2
git status --porcelain -- src
→ 0 行
~~~

- **19 的构成**：**14** = 计划面（12 改 + 1 新增 `blockentity/CoreBound.java` + 1 只加方法 `util/AreaBlockEntities.java`）+ **3** = 删除批次带出的连带改动（`config/FarmstormConfig`、`event/FarmstormGameEvents`、`outpost/OutpostCoreIndex`）+ **2** = 删除（`farmland/FarmlandGuard`、`farmland/FarmlandLedger`）。`M 16 = 13（计划面 12 + 连带 3）+ 3` 与 `D 2` 合起来正好是 19。

### 6.2 上游与资源面

~~~text
git diff --stat 52749b9 HEAD -- docs plan0.2.4.txt
→ 只有 6 份新文档：58 / 59 / 60 / 61 / 62 / 63（合计 2958 insertions）
→ 排除这 6 份后：0 个文件、0 行
git diff --stat 52749b9 HEAD -- .work/v024-BRIEF.md plan0.2.4.txt  → 0 行
git diff --stat 52749b9 HEAD -- docs/en                            → 0 行
git diff --name-only 52749b9 HEAD -- src/main/resources src/generated/resources → 0 个路径
~~~

即 `docs/zh/52` ~ `docs/zh/57`、`docs/zh/01` ~ `51` 与更早文档、`.work/v024-BRIEF.md`、`plan0.2.4.txt`、`docs/en/` 全部 **0 行改动**（只引上游，不改上游）。

### 6.3 #22 的判据改写结论（本版口径）

- 60 号 §8 #22 的**字面判据**是「`git status --porcelain` 出现 **19** 个 `.java` 路径」，它假设的是**未提交工作树**。
- 本版按「一 Phase 一提交」落地（`.work/v024-commit-1.txt` ~ `v024-commit-6.txt` 是那几笔提交的说明稿），工作树对 `src` 报 **0** ⇒ **字面不通过**。
- **等价判据**：`git diff --name-status 52749b9 HEAD -- src` = **19** 个路径，构成 **14（计划面 M/A）+ 3（连带 M）+ 2（D）**；`src/main/resources` 与 `src/generated/resources` 各 **0** ⇒ **等价判据通过**。
- 本条的结论句只写「**字面不通过 / 等价判据通过**」，**不写「通过」**。

### 6.4 本任务的改动面

| 文件 | 动作 |
|---|---|
| `.work/v024-verify/signoff.md` | 新建（本文件） |
| `README.md` | 增量段：版本状态 0.2.4 已实现、三段玩家可见的话、数字快照表、版本历史 |
| 其它（`src/`、`tools/`、`docs/`、`plan0.2.4.txt`、`.work` 既有文件） | **0** |

---

## 7. 复核命令与结论行

| # | 命令（原样） | 结论 |
|---|---|---|
| 1 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build` | 退出码 **0**；`BUILD SUCCESSFUL in 9s` / `5 actionable tasks: 5 up-to-date` |
| 2 | `python.exe -X utf8 tools/verify_project.py` | 退出码 **0**；`已核 registry 对象: 92 / 92`、`生成 toml 键数: 175`、`[OK] 175 个配置键与文档逐字一致`、`[OK] lang 键集一致，共 321 键`、`结论：FAIL 0 项，WARN 1 项`（WARN 1 = 基线那条 `trade.*.min/max` 的拼写形状，非本轮引入） |
| 3 | 十项计数（§1 表内命令） | 151 / 92 / 175 / 3 / 321 × 2 / 178 / 2 / 16 / 21 / 26 全中 |
| 4 | `git diff --name-status 52749b9 HEAD -- src` | **19** 行（M 16 / A 1 / D 2） |
| 5 | `git diff --stat 52749b9 HEAD -- docs plan0.2.4.txt` | 只有 58 ~ 63 六份新文档；排除后 0 行 |

**JAR 名与协议串**：`mod_version=0.2.4` ⇒ 产物 `farmstorm-0.2.4+mc1.21.1.jar`；`PROTOCOL_VERSION = "3"` 未动，客户端与服务端必须同版。

**签字读取口径**：§2 的 4 条与 §3 的 3 项都在标题上各带一次标记（§2.1 ~ §2.4 / §3.1 ~ §3.3），计数按条目走；正文里引用同一条时不重复计数。
