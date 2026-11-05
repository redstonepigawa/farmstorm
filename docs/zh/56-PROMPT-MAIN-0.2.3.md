# 56 号 v0.2.3 实现方主提示词

> 投喂方式：本文件**只投一次**，在任何 Phase 之前。之后按 57 号逐 Phase 投喂。
> 阅读顺序：**52（决策，唯一真相源）→ 53（设计）→ 54（技术）→ 55（任务与裁决 A-92 ~ A-101）→ 本文**。冲突时按 §6 的顺序裁决，且一律报给作者。
> 本文的地位：**常驻约束**——每次开工先读这一份，再读 57 号给的当前 Phase。本文不回答「这一轮做哪件事」（那是 55 / 57 号的事），只回答「哪些做法一定错」。
> 一次只做任务书要求的事：做完就停、回报、等下一个 Phase。不提前实现下一阶段，不把「顺手改好」塞进本轮。

| 项 | 值 |
|---|---|
| 文档编号 | 56-PROMPT-MAIN-0.2.3 |
| 读者 | 实现代理（能读写本仓库、能跑 `.tools\gw.cmd`） |
| 上游 | `52-DECISIONS-0.2.3.md`（D-203 ~ D-224，22 条 + 18 条裁定）→ `53-DESIGN-0.2.3.md`（R1 ~ R5 / B-28 ~ B-58）→ `54-TECH-0.2.3.md`（92 注册 / 175 config / 321 语言键 / 32 验收 / RT-71 ~ RT-82）→ `55-TASKS-0.2.3.md`（F-286 ~ F-311 / Phase 54 ~ 65 / A-92 ~ A-101） |
| 本文件发什么编号 | **HC-156 ~ HC-172（17 条）**、**N-141 ~ N-154（14 条）**、事实卡 **20 行**、陷阱表 **44 条**、交付清单 **12 项** |
| 本文件不写什么 | 类设计与算法（54 号）、任务与排期（55 号）、分阶段内容（57 号）。本文只写「什么一定错」与「怎么判定」 |
| 编号接力 | 决策 **D-203** / 任务 **F-286** / Phase **54** / 里程碑 **M50** / 硬约束 **HC-156** / 禁止行为 **N-141** / 风险 **RT-71** / 裁决 **A-92** |

## 0. 这份提示词给谁看、怎么用

本文件写给**实现代理**：能读写 `D:\project_farm` 全树、能跑 `.tools\gw.cmd`、能起服看 `run/logs/latest.log`。它假设读者已经读过 52 号，知道 D-203 ~ D-224 各说什么；本文**不复述**那 22 条决策，只把它们压成「开工时不能违反的硬约束」「一定不能做的事」「踩了就静默出错的陷阱」。

用法只有一条：**开工前读一遍本文，收工时按 §5 的勾选项逐条对号**。本文与 55 号的分工是硬的——55 号说「这一轮做 F-293」，本文说「做 F-293 时不许把 17 个投入物抄进转换器」「投递失败不许清槽」。两者冲突时按 §6 裁决。

本文与 52 ~ 55 的关系，四个词说完：

| 上游 | 它定什么 | 本文怎么用它 | 本文不做什么 |
|---|---|---|---|
| 52 号（22 条决策） | **口径**：一件事「是什么」 | 每个 HC 的「出处」列都追到 D 号或裁定号；口径有疑义时回 52 号，不回 53 / 54 号 | **不新立口径**、不改 D 号结论、不解释「为什么这样定」（那是 52 号的活） |
| 53 号（R1 ~ R5、B-28 ~ B-58） | **玩家侧长什么样** | 边界口径（什么算 / 什么不算）直接引用 B 号，验收时成对跑 | 不重写行为叙述、不改 B 号、不补边界 |
| 54 号（类设计、32 验收、RT-71 ~ RT-82） | **技术落点**：改哪个方法、验哪一条 | 「判定」列的读数一律取 54 号 §8 的验收号；`文件:行号` 以 54 号实测为基线、本文再复核一遍 | 不写类结构、不排验收顺序、不改 `本文确定` 的技术参数 |
| 55 号（F-286 ~ F-311、A-92 ~ A-101） | **切分与顺序**：谁先谁后、窗口开合 | 「窗口」纪律（F-287 开 → F-308 关）与六条顺序死命令逐条落到 HC / N 里 | 不排 Phase、不估人日、不改任务边界 |

### 0.1 五条最贵的读法

1. **本版不是「加一台机器」，是「换一代形态」**：`ConverterBlockEntity`（435 行、`implements Container`、双槽、经验、催化剂、配方缓存代际）整条推倒（D-208）；`SeedItem`（54 行）整类删除（D-212）。凡照旧代码「改两个字段」的写法都会在第一次 `gw.cmd build` 时暴露成一片编译错误。
2. **92 不等于 152 不等于 175 不等于 321**：注册对象 92、Java 文件 152、config 键 175、语言键 321 × 2，四个数**互不推导**（D-205、D-223、D-224、BRIEF §3.2）。任意两个相加或相减都没有意义，混用一次就要重算整张表。
3. **两条「看起来能复用」的路都是断的**：`FarmstormCropBlock` 的构造器与 codec 强制要 `CropProfile`（`:43-46`、`:57-60`），`SeedItem.useOn` 被钉死成 `PASS`（`:38-41`）。平凡作物必须新开两个类，不许「凑一个假档案」也不许「改 `useOn`」（D-215、D-216）。
4. **本版唯一的协议变更是一个字节**：`CultivatorLinkPayload.Link` 从 2 字段变 3 字段（D-210）。包数仍是 2、包 id 不变、`MAX_LINKS = 1024` 不动——但 `PROTOCOL_VERSION` 必须 `"2"` → `"3"`，否则混装环境**必崩**（`FarmstormNetwork.java:25`）。
5. **窗口从 F-287 开到 F-308**：这 20 个任务里不许写 `92`，也不许写 `321`（55 号 §0.3）。窗口内的启动日志会打 `FAIL`，那是**期望红**，不是缺陷。

### 0.2 本版三处「不是照着抄一个类」能过的

| # | 是什么 | 为什么没有先例 | 落到哪 |
|---|---|---|---|
| 1 | **一台机器的功能重做，同时是一次有损的注册改名** | 0.2.0 起 `converter` 就是装饰方块（`ConverterBlock.java:85-90` 返回 `PASS`），`ConverterMenu` / `ConverterScreen` 是不可达代码；本版把「不可达」变成「删除 + 重写」，旧 id 下线不并存（D-206） | F-287、F-289、F-292、F-293 |
| 2 | **一批物品被删、一类作物被新开，而两者共用同一张标签** | 7 个种子删完 `#farmstorm:seeds` 会空，必须由 `plain_chives_seeds` 顶上；而平凡作物又**不许**进 `CropProfile.ALL`（红线 7） | F-295、F-297、F-298 |
| 3 | **协议加一个字节，落盘加一条平行数组** | `Kind` 不能靠渲染期反查（红线 3），只能绑定时写入、核侧落盘；而落盘是「旁挂 `int[]`」而不是改 `long[]` 的形状（54 号 §5.2） | F-302、F-303、F-304 |

### 0.3 本版的算术（全部是 52 号已有的加减法）

| # | 算式 | 结果 | 出处 |
|---|---|---|---|
| 1 | `34 − 7 + 1` | **28**（items） | D-205 |
| 2 | `27 + 1` | **28**（blocks） | D-205 |
| 3 | `2 − 1` | **1**（menu_type） | D-205 |
| 4 | `98 − 7 + 1 + 1 − 1` | **92**（注册面合计） | D-205 |
| 5 | `176 − 3 + 2` | **175**（config 键） | D-223 |
| 6 | `60 − 1 + 1` | **60**（`b.push` 次数） | D-223 |
| 7 | `340 − 23 + 4` | **321 × 2**（语言键） | D-224 |
| 8 | `26 − 6 + 1` | **21**（`converting` 配方） | D-209 |
| 9 | `21 − 4` | **17**（培养器投入物） | D-218 |
| 10 | `6 + 11` | **17**（投入物的两档分解） | D-219 |
| 11 | `18 − 2` | **16**（进度条数） | D-214 |
| 12 | `154 − 3 − 1 + 2` | **152**（Java 文件） | BRIEF §3.2；A-98 |
| 13 | `600 ÷ 32` | **18.75**（600 刻里动画播几个循环） | D-211 |
| 14 | `29 − 7` | **22**（item 贴图） | 54 号 §3.6 |

表下注两条：① **本文不自创数字**——需要新数值的地方（红色色值、配方解锁条件、两个新 config 键的值域）一律按 54 号 §0.3 的 16 项「本文确定」执行，不回 52 号编，也不在代码里偷偷取一个新值。② **四个数互不推导**——92 是注册对象、152 是文件数、175 是 config 键、321 是语言键，任意两个相加或相减都没有意义（D-205、D-223、D-224）。

## 1. 事实卡

只收「不写就会错」的源码事实。**查不到 `文件:行号` 的名字一律不写**——凭印象编一个类名进代码就是编译错误。基线 = **0.2.2 已实现态**（git `79b71f1`）。

| # | 事实 | 值 / 现状 | 证据 |
|---|---|---|---|
| 1 | 版本与基线 | `mod_version = 0.2.2`；本版改 **0.2.3**；版本串 **6 处**（2 抬头 + 3 jar 名 + gradle 1 行） | `gradle.properties:11`；`一键构建.cmd:5,11,12`；`检查环境.cmd:5,14`；D-203 |
| 2 | 注册面 12 类 | `34 / 27 / 8 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 2 = 98`；本版 `items` −6 净、`blocks` +1、`menu_type` −1 ⇒ **92** | `Farmstorm.java:180-183`；54 号 §1.2；D-205 |
| 3 | **转换器自 v0.2.0 起是装饰方块** | `useWithoutItem` **无条件返回 `PASS`**——右键无反应、不开界面，整套 GUI 事务是不可达代码 | `ConverterBlock.java:85-90`（实读 `:83-87`，`return` 在 `:86`）；D-170；BRIEF §1.1 |
| 4 | **7 个种子本来就不可播种** | `useOn` 无条件 `PASS`，早于 `BlockItem#place` 的 `consume(1)` ⇒ 种子不减、也种不下去；15 个作物方块正常玩法拿不到 | `SeedItem.java:38-41`（实读 `:41-44`，`PASS` 在 `:43`）；裁定 #4；D-152 ② |
| 5 | **`FarmstormCropBlock` 强制要 `CropProfile`** | codec 用 `CropProfile.CODEC.fieldOf("profile")`，构造器签名第一个参数就是档案 ⇒ **不能**用它承载无档案的平凡作物 | `FarmstormCropBlock.java:43-46`、`:57-60`；D-215 |
| 6 | 培养器现有判据只收带档案的物品 | `profile != null && profile.tier() != CropTier.EPIC` ⇒ 原版小麦**进不去**；档案由三个物品类的 `profile()` 反查 | `CultivatorBlockEntity.java:138-144`、`:151-165`；D-218 |
| 7 | 培养器现有产出 | `batch()` 给「作物 × `yield`」，`hasOwnSeed`（`seedItem != null && seedItem != id`）时再给种子 × `yield` ⇒ 自身即种子的 11 条只给一组 | `CultivatorBlockEntity.java:229-249`；D-219 |
| 8 | **盘上的 toml 是陈旧的** | `maxPerPlayer = 2`、`cultivatorCapacity = 16`，而代码默认值已是 4 / 24——0.2.2 的默认值改动至今没落到盘上；本版 `:1` 那一节就是 `[converter]` | `run/config/farmstorm-server.toml:753`、`:765`；`FarmstormConfig.java:943`、`:956`；D-223；A-95 |
| 9 | server config 现状 | **176 键 / 60 push**；行首 `.define*` 177 行含 1 行被注释（`farmlandBonus`） | `FarmstormConfig.java:940`；54 号 §1.3；D-223 |
| 10 | 语言键现状 | **340 × 2**（zh = en、zh-only 0、en-only 0）；类注释另写「各 340 键」与条目构成两处 | `src/generated/resources/assets/farmstorm/lang/zh_cn.json`、`en_us.json`；`ModLangProvider.java:10`、`:13-16`；D-224 |
| 11 | `#farmstorm:seeds` 现状 **6** | `tag(FarmstormTags.SEEDS).add(...)` 只有 6 个物品成员；类注释在 **`:35`**（不是 `:33`），且注释里的「4 个自身即播种物的作物」与实际成员不符 | `ModItemTagsProvider.java:42-48`；`FarmstormTags.java:35`；D-212；BRIEF §7 第 12 条 |
| 12 | `converting` 配方现状 **26** | 20 条作物本体 + 6 条种子（`:86-92`）；两个助手 `twoWay` / `oneWayWithNetherStar` 在 `:187-209` | `ModRecipeProvider.java:67-99`、`:187-209`；D-209 |
| 13 | `ConverterRecipe` 现状 7 字段 | `input` / `result` / `xpCost` / `catalyst` / `catalystCount` / `bidirectional` / `reverseXpCost`，另有 `reverse()` 运行时视图 | `ConverterRecipe.java:19-25`、`:27-28`、`:106-111`；D-209 |
| 14 | 客户端两处连线 | `:62` 注册 `ConverterScreen`（随三件套一起删）；`:75` 把 finder 委托给 `ConverterRecipeBookCategory`（**要内联，不许直接删**） | `FarmstormClient.java:62`、`:75`；A-96 |
| 15 | 协议面现状 | `PROTOCOL_VERSION = "2"`；`MAX_LINKS = 1024`；单条 `Link` 是 **2 字段**二元 `composite`；`record Link(BlockPos core, BlockPos cultivator)` | `FarmstormNetwork.java:25`；`CultivatorLinkPayload.java:36`、`:42-45`、`:53`；D-210 |
| 16 | 广播端两个静默失效点 | `ORDER` 只比两个 `long`；`fingerprint(...)` 只哈希两个 `long` ⇒ `kind` 不进这两处就「变了不重发」 | `CultivatorLinkBroadcaster.java:76-78`、`:227-234`；BRIEF §7 第 6 条 |
| 17 | 渲染颜色是硬编码常量 | 四个 `private static final float`：`0.30 / 0.85 / 0.90 / 1.0`；主循环三处 `renderLineBox` 共用同一组 | `CultivatorLinkRenderer.java:41-44`、`:71-79`；D-210 |
| 18 | **补绑只认培养器** | `AreaBlockEntities.collect(..., CultivatorBlockEntity.class)` 只按类型收集，`:563` 调单参 `addCultivator(BlockPos)` | `OutpostCoreBlockEntity.java:537-572`、`:546-547`、`:563`；A-100 |
| 19 | 核侧绑定面只认旧形态 | `addCultivator(BlockPos)` 单参、只加进 `List<BlockPos>`；`isProcessingBlock` **只查 `farmstorm:processing_block` 标签，不查方块类型** | `OutpostCoreBlockEntity.java:358-371`、`:802-804`；D-208；BRIEF §1.3 |
| 20 | `basicItem` 是紫黑格的来源 | 兜底分支去找 `textures/item/<id>.png`；`usesBlockModel` 只认三个方块类，漏一个就落到兜底 | `ModItemModelProvider.java:55-57`、`:68-70`；BRIEF §5.1 |

> 两条附注：① 本表 3 处行号与 `BRIEF` / 52 号的记载差 1 ~ 3 行（`ConverterBlock` 的 `useWithoutItem`、`SeedItem.useOn`、`CultivatorLinkRenderer` 的颜色常量），一律按**实读**用，差异登记在 §6.2；另有 2 处属行号以外的差异（平凡作物方块的插入点、`FarmstormClient:75` 的处置），同表登记。② 读源码一律 `[IO.File]::ReadAllLines($p, UTF8)`；本机默认 `Get-Content` 会把无 BOM 的 UTF-8 中文读成 GBK 并吞掉换行，行号随之全错（陷阱 5）。

## 2. 硬约束 HC-156 ~ HC-172（17 条）

HC 只约束本版增量面。44 号的 HC-121 ~ HC-137 与 50 号的 HC-138 ~ HC-155 中，凡被本版改写过的以本文为准（清单在 §6.3），其余继续有效。每条给三项：**约束**（写什么算对）、**判定**（读到什么算过）、**出处**（决策 / 技术 / 任务编号）。判定列一律可执行：一条命令、一处 `git diff`、一行日志、一个读数。

- **HC-156 · 注册面 92，与自检、检查器同一提交收口**
  - 约束：12 类分项 `items 28 / blocks 28 / block_entity_type 8 / entity_type 13 / mob_effect 4 / sound_event 3 / recipe_type 1 / recipe_serializer 1 / creative_mode_tab 1 / damage_type 3 / data_component_type 1 / menu_type 1` = **92**；`Farmstorm.java:180-183` 的 `total == 98` → `92`、`items == 34` → `28`、`blocks == 27` → `28`、`menuTypes == 2` → `1`，**其余 8 个逐类 `==` 一字不动**；`tools/verify_project.py` 的 `EXPECTED` id 清单与 `EXPECTED_COUNTS` 同批改到 `28 / 28 / 1`；窗口（F-287 ~ F-307）内除 F-308 外不许写 92。
  - 判定：启动日志整行 `自检 注册对象 92 个：items 28 / … / menu_types 1 → OK`；脚本打印 `已核 registry 对象: 92 / 92`、`结论：FAIL 0 项`；`Farmstorm.java` 里 `== 98` 命中 0、`total == 92` 命中 1；`EXPECTED_COUNTS` 12 项相加 = 92。
  - 出处：D-205；54 号 §1.2 / §2.4 第 7~8 行；55 号 F-308；RT-72。

- **HC-157 · `converter` → `crop_converter` 是改名，不是「删一个加一个」**
  - 约束：方块、方块物品、方块实体**三处同一对象换 id**，条目数不变；`blocks` 仍是 28（不是 29）、`items` 仍是 28；连带的 id 面六处同步改（`FarmstormBlocks.java:71-72`、`FarmstormItems.java:112-113`、`FarmstormBlockEntities`、`ModBlockTagsProvider.java:48`、`ModBlockStateProvider` 的 `cropConverterStates`、战利品表路径）；进度 id 仍是 `farmstorm:converter`（不许改名）。
  - 判定：`src/main/java` 下 `FarmstormBlocks.CONVERTER` 命中 0、`"converter"` 只剩进度 id 与注释 ≤ 3 行；`grep -rn '"converter"' src/main/java` 不出现注册 id；启动日志的 98 保持（F-287 唯一允许引用 98 的任务）。
  - 出处：D-206；54 号 §2.1 / §2.4；55 号 F-287；RT-71。

- **HC-158 · `PROTOCOL_VERSION` 必须 `"2"` → `"3"`，与 `Link` 三字段同提交**
  - 约束：`Link` 从 2 字段变 3 字段就是 codec 变了（`FarmstormNetwork.java:22-24` 的既有纪律），版本串必须同一次改到 `"3"`；**不新增包、不改包 id、`MAX_LINKS = 1024` 一个字符不动**；启动两行自检里的版本号随之显示 3。
  - 判定：`FarmstormNetwork.java` 里 `PROTOCOL_VERSION = "3"` 命中 1、`"2"` 命中 0；`playToClient(` 仍 2 处；`CultivatorLinkPayload.java:36` 仍是 `1024`；日志两行自检版本 3。
  - 出处：D-210；54 号 §5.5；55 号 F-302；RT-76；A-99。

- **HC-159 · `#farmstorm:seeds` 从 6 降到 1，且不许空集**
  - 约束：`tag(FarmstormTags.SEEDS)` 从 6 个成员改成只留 `plain_chives_seeds` 一个；`#farmstorm:converter_fuel` 整段删（`ModItemTagsProvider` 与 `FarmstormTags.java:38-40` 两处）；`FarmstormTags.java:35` 的类注释改成「1 个播种物（平凡韭菜种子）」——只改注释、不改标签语义。
  - 判定：`data/farmstorm/tags/item/seeds.json` 的 `values` 恰 **1** 项且是 `farmstorm:plain_chives_seeds`；算式 `6 − 6 + 1 = 1`；`converter_fuel` 在 `data/farmstorm/tags/item/` 下命中 0。
  - 出处：D-212、D-216；54 号 §2.3 标签面；55 号 F-295 / F-298。

- **HC-160 · 语言键 321 × 2，中英同键同数**
  - 约束：删 **23**（7 种子 + 9 GUI + 3 config + 4 进度）、加 **4**（2 平凡作物 + 2 新 config）、**改名 2**（`item.` / `block.` 的 `converter` → `crop_converter`，键数不变）、**改值 2**（`fang_dance` 描述中英同改、`converter` 描述的中文换「农作物」）；两处总数注释（`ModLangProvider.java:10`、`:13-16`）同批改到 `28 + 28 + 32 + 178 + 13 + 42 = 321`；321 是**最终值不是下限**。
  - 判定：`zh = 321`、`en = 321`、`zh-only = 0`、`en-only = 0`；`ModLangProvider.java` 里 `340` 命中 0、`321` 命中 2；按「删 1 加 1」数成 323 即失败。
  - 出处：D-222、D-224；54 号 §7.1 ~ §7.5；55 号 F-307；RT-79。

- **HC-161 · config 175 键 / 60 push，`push` 数不变**
  - 约束：删 `[converter]` 整组 3 键（`xpCost` / `netherStarForwardCost` / `netherStarOnReverse`），加 `crop_converter.processTicks`（默认 600，范围 20 ~ 72000）与 `pumpkin_turret.bulletLifetimeTicks`（默认 300，范围 1 ~ 72000，落在**既有** `[pumpkin_turret]` 组内）；`[converter]` 的 `b.push` / `b.pop` 被 `[crop_converter]` 的同一对替换；**不写迁移代码、不手改 toml**。
  - 判定：删掉 `run/config/farmstorm-server.toml` 重生成后，键数 **175**、`b.push` **60**、顶层节 **36**、`^\[converter\]` 命中 0、`^\[crop_converter\]` 命中 1、`processTicks = 600`、`bulletLifetimeTicks = 300`。
  - 出处：D-223；54 号 §6.1 ~ §6.4；55 号 F-305；A-95；RT-80。

- **HC-162 · 转换配方 21 条，全单向、无成本、`result` 恒 1 个**
  - 约束：`farmstorm:converting` 类型与序列化器保留（`recipe_type` / `recipe_serializer` 各 1 不变）；条数 `26 − 6 + 1 = 21`；`ConverterRecipe` 只留 `input` / `result`，`reverse()` 与五个字段与对应 getter 全删；序列化器的 `MapCodec` / `StreamCodec` 同步收成 2 段。
  - 判定：`data/farmstorm/recipe/` 里 `"type": "farmstorm:converting"` 的 JSON = **21**，每份只有 `input` / `result`；六个种子配方文件名全部不存在；`friend_chives.json` 存在且输入是 `farmstorm:plain_chives`；无 `Couldn't parse recipe`。
  - 出处：D-209、D-217；54 号 §4.2；55 号 F-290 / F-291。

- **HC-163 · 培养器投入物 17 个，判据只有一个来源**
  - 约束：`admissible` 改成「是 21 条转换配方的输入 ∧ 该配方的输出档案 `tier() != EPIC`」，算式 `21 − 4 = 17`；排除的 4 个是火把花 / 瓶子草荚 / 甜浆果 / 金苹果；`profileOf` 改名 `outputProfileOf`；**不写 17 个物品的白名单**，也不把 17 当常量。
  - 判定：`grep -c 'minecraft:wheat'` 在 `CultivatorBlockEntity` = 0；行为抽验 6 条（小麦 / 胡萝卜 / 毒马铃薯 / 平凡韭菜收得进，火把花 / 金苹果收不进）；临时加一条 `converting` 配方 ⇒ 自动多收一个。
  - 出处：D-218；54 号 §4.3.1；55 号 F-299；B-38 ~ B-41、B-46。

- **HC-164 · `SeedItem` 类删除，且删在引用面清空之后**
  - 约束：7 个种子删完、22 条档案的 `seedItem` 一律置 `null`、`CultivatorBlockEntity` 的 `instanceof SeedItem` 分支随判据反转消失**之后**，才删 `SeedItem.java`（54 行）；`CropProfile.seedItem` **字段保留**（类型是 `ResourceKey<Item>`），值全 `null`；`SeedItem.useOn → PASS` 的行为**不许翻案**（D-152 ② 继续有效）。
  - 判定：`src/main/java` 下 `SeedItem` 命中 0、文件不存在；`CropProfile.ALL` 仍 22 条、`seedItem` 全 `null`；`FarmstormCropBlock.getBaseSeedId()` 的 `null` 分支返回 `Items.AIR` 照旧。
  - 出处：D-212；54 号 §1.5 / §2.3；55 号 F-295 → F-299（A-98）。

- **HC-165 · `plain_chives` 不进 `CropProfile.ALL`，走轻量路径**
  - 约束：`CropProfile` 仍 **22** 条；`plain_chives` 不进 `ALL` / `LUCKY_BERRY_POOL` / `#farmstorm:modified_crops`；新方块用 `PlainCropBlock extends CropBlock`（`age 0~3`、四个碰撞箱照 `FarmstormCropBlock.java:48-53`、生长读同一组 `growth.*` config），**不接 `CropProfile`**。
  - 判定：`CropProfile` 的档案数 22；两个标签 JSON 里都没有 `plain_chives`；`Farmstorm#logSelfCheck` 的「作物 22 种」一字不变；`PlainCropBlock` 的构造器只有 `BlockBehaviour.Properties` 一个参数。
  - 出处：D-215、裁定 #14；54 号 §4.3.4；55 号 F-297；红线 7。

- **HC-166 · 15 个改良作物方块保留但不可获得**
  - 约束：`blocks` 里 15 个 `crop(profile)` 注册**一个不删**、**一个都不补 `BlockItem`**；战利品表删掉种子那一条（引用已删物品会让 datagen 拿到 `null` 并崩），**作物本体的掉落保留不动**。
  - 判定：`blocks` = 28（不是 12 / 13）；创造栏里 15 个作物方块全都不存在；`ModLootTableProvider` 的 15 张作物表里种子参数全为 `null`，作物本体条目一条不少；老存档里已放置的照旧可拆。
  - 出处：D-213、裁定 #6；54 号 §2.3 战利品面；B-52 ~ B-54；红线 1。

- **HC-167 · 平凡韭菜种子必须可播种，不许复用 `SeedItem`**
  - 约束：新建 `PlainSeedItem extends ItemNameBlockItem`，`plain_chives_seeds` 用它注册；`useOn` 走原版放置流程（不覆写，或覆写后调 `super`）；**不许**用 `SeedItem` 承载它，也**不许**改 `SeedItem` 去复用；它进 `#farmstorm:seeds` 标签与创造栏。
  - 判定：`grep -n "PLAIN_CHIVES_SEEDS" src/main/java` 的构造器是 `PlainSeedItem`；游戏内手持右键耕地 ⇒ 生成 `farmstorm:plain_chives_crop[age=0]`、手上少 1 个；骨粉能长到 `age=3`。
  - 出处：D-216、裁定 #7 / #14；54 号 §4.3.4；55 号 F-298；B-48；红线 2；RT-82。

- **HC-168 · 动画只靠模型切换，8 个 variant、两张模型**
  - 约束：`FACING`（水平四向）× `WORKING`（布尔）= **8 个 variant**；`working=false` 引**静态**贴图（三张 `_still`，**都没有 mcmeta**），`working=true` 引 16 帧帧条（三张各带 `{"animation":{"frametime":2}}`）；底面 `_bottom` 静态且**不带** mcmeta；**不写任何「暂停 / 恢复纹理动画」的代码**。
  - 判定：`blockstates/crop_converter.json` 恰 8 个 variant、`y` 依次 `0 / 90 / 180 / 270`、`uvlock` 全 `true`；`textures/block/*.mcmeta` 恰 **5** 个（3 新 + 2 既有）；源码里没有任何以 `frametime` / 纹理帧为操作对象的调用。
  - 出处：D-211；54 号 §3.1 ~ §3.2；55 号 F-292 / F-294；RT-78；红线 4。

- **HC-169 · 红线 `Kind` 必须进指纹与排序，渲染只读 payload**
  - 约束：`Link` 加 `Kind`（`CULTIVATOR = 0` / `CROP_CONVERTER = 1`，未知值按 `CULTIVATOR` 解）；**排序列补第三键、指纹把它卷进哈希、组装按同一下标配对**三处同批；渲染按 `kind()` 取色，培养器青 `0.30 / 0.85 / 0.90` **一个字节都不许变**，转换器红 `0.92 / 0.22 / 0.22`；**不许**在渲染期读 `level` / `getBlockState` / 方块实体。
  - 判定：`ORDER` 的 `thenComparing*` 段 3 处、`fingerprint` 里 `kind()` 1 处；渲染文件里 `getBlockState` 命中 0；拆掉培养器原位置换放转换器 ⇒ 那条线在 10 刻（`MIN_INTERVAL_TICKS`）内由青变红。
  - 出处：D-210；54 号 §5.3 ~ §5.4；55 号 F-304；A-94；红线 3；RT-75。

- **HC-170 · `first_conversion` 的授予点搬家，进度 id 不改**
  - 约束：旧授予点在 `ConverterBlockEntity.java:255`，随界面与状态机重写一起消失；搬到 `deliver()` **成功那一支**，授予**绑定核的归属者**（`OutpostCoreBlockEntity#owner()`，离线返回 `null` 时跳过）；进度 id 仍是 `farmstorm:first_conversion`；`nether_star_toll` 的授予点（`:257`）随进度一起删。
  - 判定：`CropConverterBlockEntity` 的成功支里 `FarmstormAdvancements.award` 命中 1 处；连续产出两次不崩（重复授予幂等）；`data/farmstorm/advancement/` 的 JSON = **16**，`first_conversion.json` 仍在。
  - 出处：D-214；54 号 §4.1.6；55 号 F-293 / F-296；BRIEF §7 第 10 条。

- **HC-171 · `FarmstormClient:75` 内联 finder，不许直接删**
  - 约束：`ConverterMenu` / `ConverterScreen` / `ConverterRecipeBookCategory` 三个文件整删，`menu_type` 2 → 1；但 `:75` 那一行（finder 委托）必须改成**一行内联**：`event.registerRecipeCategoryFinder(FarmstormRecipes.CONVERTING_TYPE.get(), holder -> RecipeBookCategories.UNKNOWN);`；`:62` 的屏幕注册直接删。
  - 判定：`FarmstormClient.java` 里 `registerRecipeCategoryFinder(` = 1、`ConverterRecipeBookCategory` 命中 0；客户端启动日志里 `Unknown recipe category` 命中 **0**（不内联就是 21 条）；`FarmstormMenus` 里 `REGISTER.register(` 恰 1 行。
  - 出处：D-208、D-209；54 号 §2.6；55 号 F-289；A-96。

- **HC-172 · 转换器只收原版与平凡输入，但手持闸收全 21 条**
  - 约束：转换器的四道闸（范围 / 绑定 / 手持 / 空槽）**合并成一次判定**；手持闸用配方表的只读视图（`getRecipeFor` 命中即通过），**包含 4 条史诗输入**（火把花 / 瓶子草荚 / 甜浆果 / 金苹果）；**不许**把培养器的 17 条判据抄过来；**不实现 `Container`**，不接红石 / 比较器。
  - 判定：拿火把花右键转换器 ⇒ 放得进（`working=true`）；拿 `farmstorm:puffed_wheat` 右键培养器 ⇒ 收不进（反向判据，两者成对跑）；漏斗 / 投掷器塞不进；源码里 `implements Container` 命中 0。
  - 出处：D-208；54 号 §4.1.3；55 号 F-293；RT-73 / RT-74；B-30 ~ B-37。

## 3. 禁止行为 N-141 ~ N-154（14 条）

每条都能用一条命令、一处 `git diff` 或一次读数判定，不是态度要求。「为什么」只给一句，够判断边界即可。

- **N-141** 绝不给 15 个改良作物方块补 `BlockItem`，也绝不把它们删掉或让它们变得可获得。
  - 不要做什么：不加 `BlockItem`、不加合成 / 掉落 / 创造栏入口、不改 `FarmstormCropBlock` 的放置语义。
  - 为什么：裁定 #6 定的是「保留但不可获得」；能直接放置的作物方块会绕过 `age` 成长，等于给一条「一放就成熟」的捷径（D-213）。
  - 会看到什么后果：`blocks` 从 28 变成 29（或连方块一起删后掉到 12），与 HC-156 的逐类表当场冲突；签字里的注册面整表作废。

- **N-142** 绝不复用 `SeedItem` 承载平凡韭菜种子，也绝不为了让平凡作物能种去改 `SeedItem.useOn`。
  - 不要做什么：不 `new SeedItem(...)`、不改它的 `useOn` 返回值、不给它加「可播种」开关。
  - 为什么：`useOn` 被 D-152 ② 钉死成无条件 `PASS`（`SeedItem.java:38-41`），改它就是推翻 0.2.0 的裁定，本版无权翻案（D-216）。
  - 会看到什么后果：种子种不下去，而平凡韭菜是本版唯一的可种平凡作物 ⇒ 第 ② 条需求整条落空，且没有任何报错，只有「右键没反应」。

- **N-143** 绝不在渲染期按方块类型反查连线颜色，也绝不在广播期读 `getBlockState` 判机器类型。
  - 不要做什么：不在 `CultivatorLinkRenderer` 里碰 `level` / `BlockState` / 方块实体；不在 `CultivatorLinkBroadcaster` 里查区块补 kind。
  - 为什么：连线常画在**远端未加载区块**上，反查要么触发区块加载（违反不加载纪律），要么猜——猜错就是闪色（D-210、红线 3）。
  - 会看到什么后果：远处的线随机在青与红之间跳；更坏的是它「大部分时候是对的」，回归测试抓不到。

- **N-144** 绝不写任何「暂停 / 恢复纹理动画」的代码，也绝不给静态副本加 mcmeta。
  - 不要做什么：不操作 `frametime`、不给 `_still` 图配 `.mcmeta`、不试图用 tick 控制帧。
  - 为什么：`frametime` 是贴图元数据，代码碰不到；动态 PNG 一旦被模型引用就会自己播（D-211）。
  - 会看到什么后果：机器空闲时动画照播，玩家以为「它一直在干活」；代码里则多出一堆永远不生效的暂停逻辑。

- **N-145** 绝不把 `plain_chives` 塞进 `CropProfile.ALL`，也绝不给它编一条假档案。
  - 不要做什么：不加第 23 条档案、不写 `CropProfile` 常量、不让它进 `LUCKY_BERRY_POOL` 或 `#farmstorm:modified_crops`。
  - 为什么：`ALL` 是 22 条，`LUCKY_BERRY_POOL` 由它推导、标签也按档案生成，塞进去会同时污染三处（D-215、红线 7）。
  - 会看到什么后果：`Farmstorm#logSelfCheck` 的「作物 22 种」断言 FAIL；幸运浆果能抽出平凡韭菜；`modified_crops` 标签成员 22 → 23。

- **N-146** 绝不给 `MelonGrenadeProjectile` / `MelonSliceProjectile` 加寿命或改它们的结算方式。
  - 不要做什么：不动这两个类的 `tick` / 碰撞结算 / 命中处理；不把 `bulletLifetimeTicks` 读到它们身上。
  - 为什么：plan 只写「man瓜的炮弹」，`瓜！` 靠落地碰撞结算，加寿命会改变它的战术用法（D-221、红线 6）。
  - 会看到什么后果：两个类的 `git diff` 不再是 0 行，B-55 的边界判定当场失败；更坏的是改了玩法而没人发现。

- **N-147** 绝不用盘上的老 toml 做 config 断言，也绝不写迁移代码去改玩家的 toml。
  - 不要做什么：不 `grep run/config/farmstorm-server.toml:753` 就断言 4 / 24；不写自动改写配置的启动逻辑。
  - 为什么：NeoForge 只在**键增删**时重写文件、改默认值不回写，盘上仍是 2 / 16（`run/config/farmstorm-server.toml:753`、`:765`）；自动改写玩家配置比「不生效」更糟（D-223、A-95）。
  - 会看到什么后果：假红（读到 2 / 16 就以为代码没改）或假绿（键数凑巧对、节名没看）；两种都会让下一版再踩一次。

- **N-148** 绝不在窗口（F-287 ~ F-307）内断言 92 或 321 × 2。
  - 不要做什么：不在 F-308 之外的验收里写 `92`；不在 F-307 之外的验收里写 `321`；不提前把 `Farmstorm.java:180-183` 的 98 改成 92。
  - 为什么：三个观察面（id / 计数 / 语言）分在三个任务上关窗，中途写总数必然与实际读数打架（55 号 §0.3、A-92）。
  - 会看到什么后果：窗口内的 `FAIL` 被当成缺陷去「修」，越修越偏；或者写下一个当时为真、两天后为假的断言。

- **N-149** 绝不把 17 个投入物写成白名单，也绝不把转换器的手持闸收窄成 17 条。
  - 不要做什么：不在 `CultivatorBlockEntity` 里列出物品 id；不在 `CropConverterBlockEntity` 里复用 `admissible`。
  - 为什么：判据必须只有一个来源（配方表），否则加一条配方就会漂移（RT-49 的同一条纪律，D-218）；转换器收全 21 条、培养器收 17 条，抄错一侧就有一个方向不可达。
  - 会看到什么后果：四条史诗配方永远产不出来（转换器收窄），或加一条新配方后培养器不自动多收（写死白名单）——两种都不报错。

- **N-150** 绝不留下三件套的任何一处引用，也绝不整类删掉 finder 却不内联。
  - 不要做什么：不保留 `ConverterMenu` / `ConverterScreen` / `ConverterRecipeBookCategory` 的任何 import / 字段 / 注释引用；不删 `FarmstormClient:75` 而不写内联。
  - 为什么：`converting` 类型本版保留、21 条配方仍在，没有 finder 就为每条刷一条 `Unknown recipe category` WARN（A-96）。
  - 会看到什么后果：删了却内联，客户端日志一次刷 21 条 WARN，把真正的 recipe 解析错误淹没；留着类，Java 文件数算成 153 / 154 而不是 152。

- **N-151** 绝不新增网络包、改包 id、动 `MAX_LINKS`，也绝不引入 Mixin。
  - 不要做什么：不加 `playToClient`、不动 `1024`、不加 `@Mixin` / `mixins.json` / 构建插件。
  - 为什么：包数 2 与 Mixin 0 是本版仅剩的两条红线主项（54 号 §1.5）；`Kind` 只需要在既有 `Link` 里多一个字节。
  - 会看到什么后果：`playToClient(` 变成 3、`@Mixin` 命中非 0，两条一次性判据当场红；签字里的八项读数全废。

- **N-152** 绝不改上游文档：`docs/zh/52` ~ `55`、`docs/zh/00` ~ `51`、`docs/en/*`、`.work/*`、`plan0.2.3.txt`。
  - 不要做什么：不回改决策、不「顺手修正」上一版的数字、不生成英文文档。
  - 为什么：52 号是唯一真相源，55 号的 A-92 ~ A-101 已经把上游差异逐条登记；发现冲突按 §6 报，不动手（D-203、红线 5）。
  - 会看到什么后果：本轮 `git diff` 里出现上游文件，交付校验的第 5 条直接失败；下游会拿改过的文档当新口径再传一遍。

- **N-153** 绝不给 `crop_converter` 实现 `Container`，也不接红石 / 比较器 / 经验 / 催化剂。
  - 不要做什么：不 `implements Container`、不覆写 `hasAnalogOutputSignal` / `getAnalogOutputSignal`、不留 `xpCost` / `catalyst` 的任何读取。
  - 为什么：实现 `Container` 会让漏斗 / 投掷器绕过四道闸（D-208）；经验与催化剂在裁定 #2 里整条删除，没有界面就没有地方显示成本。
  - 会看到什么后果：漏斗能把东西塞进槽里，`canInsert` 的四道闸当场失守；或 `ConverterRecipe` 删了字段却还在某处被读，编译不过。

- **N-154** 绝不复用已删的进度 id，也绝不给 `farmstorm:converter` 改名。
  - 不要做什么：不在新进度里复用 `seed_cycle` / `nether_star_toll`；不把 `converter` 这个进度 id 改成 `crop_converter`。
  - 为什么：进度 id 存在玩家存档里，改名等于让所有已完成该进度的玩家退回未完成；复用旧 id 会让老玩家看到「已完成」而实际没做过新条件（D-214）。
  - 会看到什么后果：升级后玩家的进度树凭空少一格或多一格「已完成」；两种都无法回滚。

## 4. 陷阱清单（44 条）

每条四段：**症状**（会看到什么）、**原因**（为什么会这样）、**正确做法**、**依据**。第 **44** 条是**元规则**——它约束的不是代码，是判据本身，地位高于其余各条。

| # | 症状 | 原因 | 正确做法 | 依据 |
|---|---|---|---|---|
| 1 | 机器空闲时也在播动画，玩家以为它一直在干活 | 动态 PNG 一旦被模型引用就会自己播，`frametime` 是贴图元数据，代码碰不到 | 两张模型：空闲那张引**没有 mcmeta** 的静态副本（`_still`），工作那张引帧条；不写暂停代码 | D-211；54 号 §3.2；HC-168；红线 4 |
| 2 | 物品在背包 / 创造栏里是紫黑格 | 落到 `basicItem` 兜底，去找不存在的 `textures/item/<id>.png` | `usesBlockModel` 必须认 `CropConverterBlock`；`plain_chives_seeds` 的图靠**重命名** `chive_seeds.png` 得到，不是删了再新建 | `ModItemModelProvider.java:55-57`、`:68-70`；BRIEF §5.1；A-93 |
| 3 | 改了 `defineInRange` 的默认值，游戏里还是旧值 | NeoForge 只在**键增删**时重写文件，改默认值不回写已有键 | 验收前删（或改名备份）`run/config/farmstorm-server.toml` 让它重生成；不写迁移 | `run/config/farmstorm-server.toml:753`、`:765`；D-223；A-95 |
| 4 | 拿老 toml 断言 175 键 / 4 / 24，读出来是 176 键 / 2 / 16 | 盘上那份是 0.2.1 时代的副本，`[converter]` 段还在第一行 | 把「老 toml 期望红」与「新 config 绿」**成对**记录，前者标「期望中的红」 | 54 号 §6.4、§8 #7~#8；RT-80 |
| 5 | `Get-Content` 读出的行号全错、中文乱码；命令行替换把反引号吃掉 | 默认编码按本机代码页解无 BOM 的 UTF-8 并吞掉换行；PowerShell 的双引号串会把反引号当转义 | 读文件一律 `[IO.File]::ReadAllLines($p, UTF8)`；改文件用编辑工具逐处改，**不用命令行做文本替换** | 48 号 §0.2 第 4 条；55 号 §0.4；BRIEF §7 第 1 条 |
| 6 | 拆掉一台培养器换放转换器后，那条线的颜色「串」到隔壁机器上 | `Cultivators`（`long[]`）与 `CultivatorKinds`（`int[]`）是两条**平行数组**，`removeCultivator` / 迁移改绑 / 读档截断三处任一不同步，下标就整体错位 | 增删只走同一个入口（两条列表同删）；读档按较短者截断、缺的补 `0` 并打 WARN；**不**改 `Cultivators` 的形状 | 54 号 §5.2；D-210；HC-169 |
| 7 | 客户端日志一次刷 21 条 `Unknown recipe category: farmstorm:converting/<id>` | 整类删了 finder，而 `converting` 类型本版保留、21 条配方仍在 | `FarmstormClient:75` 改成一行内联 `registerRecipeCategoryFinder(..., RecipeBookCategories.UNKNOWN)`；`ConverterRecipeBookCategory` 类文件照删 | A-96；54 号 §2.6；HC-171 |
| 8 | 0.2.2 客户端连 0.2.3 服务端，握手通过后当场解码崩 | `Link` 从 2 字段变 3 字段就是 codec 变了，协议串还停在 `"2"` | `PROTOCOL_VERSION` `"2"` → `"3"` 与 `Link` 加字段**同一提交** | `FarmstormNetwork.java:25`；D-210；A-99；RT-76 |
| 9 | 先放转换器、后放核，那台转换器永远绑不上，且服务端无日志 | `rebindNearby` 的候选收集只认 `CultivatorBlockEntity.class` | 收集扩到两种机器，`addCultivator(pos, kind)` 带 kind；这是独立验收项，不许含糊带过 | `OutpostCoreBlockEntity.java:537-572`、`:546-547`；A-100 |
| 10 | 转换器加了标签却仍被核侧「惰性清理」当成非机器摘掉；或两种机器共用一条判据却互相串 | `isProcessingBlock` **只查 `farmstorm:processing_block` 标签、不查方块类型** | 让 `crop_converter` 进该标签（成员 1 → 2），同时保证 kind 由绑定侧写入而不是由类型推 | 54 号 §2.4 第 4 行；`OutpostCoreBlockEntity.java:802-804`；D-208；BRIEF §1.3 |
| 11 | 工具打 `FAIL 0 项` 却与启动日志的 `FAIL` 相反 | 工具只看 12 类注册 id 与计数，口径外 3 类它既不核存在也不核计数 | 自检、`EXPECTED`、`EXPECTED_COUNTS` 三处**同一提交**；口径外 3 类人工登记进签字 | 55 号表 B.2；54 号 §1.2；RT-72 |
| 12 | 窗口内写下「92」，两天后同一条断言变假 | 三个观察面分三个任务关窗（id F-287 / 语言 F-307 / 计数 F-308） | 窗口内只写「本条对象存在 + 本条自己的计数」，总数只在 F-308 写 | 55 号 §0.3；A-92；N-148 |
| 13 | `blocks` 数出 29，与逐类表差 1 | 把 `converter` → `crop_converter` 当成「删一个加一个」 | 改名不计增删：三处同一对象换 id，`blocks` 仍是 28 | D-206；54 号 §1.2；HC-157 |
| 14 | 语言键数成 323 | 把两个改名键当「删 1 加 1」 | 改名 0 增减；总数只按 `340 − 23 + 4 = 321` 算 | 54 号 §7.3、§7.5；D-224；RT-79 |
| 15 | 按「新增类数」推出注册面 94 或 93 | 两个新类是 `BLOCK` / `ITEM` 各占 1 个额（已计入 +1），不是额外项 | 注册面只按 12 类的**注册调用次数**数，不按类数推 | 55 号表 B.3；54 号 §1.2 |
| 16 | 世界里的平凡韭菜是一个六面立方块，而且不报错 | `ModBlockStateProvider` 的 if 链漏 `instanceof PlainCropBlock`，落进兜底的 `simpleBlock` | 加分支（放在 `FarmstormCropBlock` 之后），并单开 `plainCropStates(...)` | `ModBlockStateProvider.java:70-88`、`:83-87`；54 号 §3.5；RT-77 |
| 17 | 平凡韭菜的模型贴图指向一张不存在的 `plain_chives_stageN` | `cropStates` 按「模型名 = 贴图名」自动拼，而实际素材叫 `friend_chives_stageN` | 单开方法，贴图路径显式写 `modLoc("block/friend_chives_stage" + age)` | `ModBlockStateProvider.java:92-106`；裁定 #18；D-215 |
| 18 | 升级后玩家已完成的「装个转换器」进度退回未完成 | 把进度 id `farmstorm:converter` 跟着方块改了名 | 方块判据与图标换新、**进度 id 保持不变** | D-214 补充；54 号 §2.4 第 6 行；N-154 |
| 19 | `first_conversion` 永远拿不到 | 旧授予点 `ConverterBlockEntity:255` 随界面消失，授予点没搬 | 搬到 `deliver()` 成功支，授予绑定核的归属者（离线跳过） | 54 号 §4.1.6；HC-170；BRIEF §7 第 10 条 |
| 20 | 核满时玩家的 30 秒白等：产物凭空消失 | 先清槽再投递，`deliver()` 返回了 `true` 而 `acceptAll` 实际失败 | 先 `acceptAll` 返回 `true` 才清槽；失败时槽与 `progress` 都保留 | 54 号 §4.1.2；RT-74；B-34 |
| 21 | 核腾出空间后又要重等 600 刻 | 投递失败时把 `progress` 清零了 | 失败分支只把 `working` 落回 `false`，`progress` 停在阈值不清零 | 54 号 §4.1.4；D-208；B-34 |
| 22 | 机器附近的客户端每刻抖一下，网络包暴涨 | 每刻无条件 `setBlock` 写 `working` | 只在值真变化时写一次 blockstate，且带 `UPDATE_CLIENTS` | 54 号 §4.1.2；D-211 |
| 23 | toml 里的键落进了别的节，或出现空的 `[crop_converter]` | `b.push("converter")` 换成 `[crop_converter]` 时 `push` / `pop` 不配对 | 删 3 键时把 `:295` / `:305` 那一对整体替换；新键落在既有 `[pumpkin_turret]` 组内 | `FarmstormConfig.java:295-305`、`:444-461`；D-223；HC-161 |
| 24 | 一键构建复制一个不存在的 jar，检查环境报 `jar: 缺失` | 两支 `.cmd` 里写死的 jar 名没跟着版本号改 | `0.2.2` → `0.2.3` 共 **6 处**：`gradle.properties:11` + `一键构建.cmd:5,11,12` + `检查环境.cmd:5,14` | D-203；55 号 F-286 |
| 25 | 启动日志 `FAIL`，但逐类数字看起来都对 | 只改了 `total`，没改 `items` / `blocks` / `menuTypes`；或只改逐类没改 `total` | 14 个 `==` 里改 4 个（`total` / `items` / `blocks` / `menuTypes`）、**其余 8 个一字不动** | `Farmstorm.java:180-183`；54 号 §2.4 第 7 行；HC-156 |
| 26 | 转换器放得进小麦，却放不进火把花 | 把培养器的 17 条判据抄给了转换器 | 转换器的 `canInsert` 收**全 21 条**（含 4 条史诗输入）；两条判据成对验收 | 54 号 §4.1.3；RT-73；HC-172 |
| 27 | 新加一条 `converting` 配方，培养器不认 | 判据写成了白名单，或把 17 写成了常量 | 判据读配方表：命中配方 ∧ 输出档案 `tier() != EPIC`；17 只是本版的值 | D-218；B-46；HC-163 |
| 28 | 「自身即种子」的 11 条仍然只给 2 个 | 沿用 `hasOwnSeed` 的旧语义（「有没有独立种子物品」），与「给几组」混为一谈 | 永远两组：有独立种子给 2 + 2，否则给 4 个同一物品 | `CultivatorBlockEntity.java:229-249`；D-219；B-42 / B-43 |
| 29 | 改 `cultivator.yield` 后两组产出不一样多 | 把产量拆成了两个键 | 一个键管两组，两组同乘 | D-219 补充；B-45；54 号 §4.3.2 |
| 30 | 毒马铃薯产出马铃薯 | 给它手写了一条「对应种子 → 马铃薯」的映射 | 按字面收口：没有独立种子物品的就是它自己，给 4 个毒马铃薯 | D-219；B-44；54 号 §4.3.2 第 1 条 |
| 31 | 编译不过：`SeedItem` 找不到 / `FarmstormItems` 引用不到 | 早删了 `SeedItem.java`，而此时 `instanceof SeedItem` 与 `seedItem(...)` 工厂还在 | 先删种子 + 档案置空 + 判据反转，**最后**删 `SeedItem.java` | 55 号 §0.5 死命令 4；HC-164；A-98 |
| 32 | 创造栏里 `plain_chives_seeds` 丢图 / 或 `chive_seeds` 先丢图 | 重命名的时序放错：早了旧物品丢图，晚了新物品丢图 | `chive_seeds.png` → `plain_chives_seeds.png` 与「删 7 个种子」**同一提交** | A-93；55 号 F-295；陷阱 2 |
| 33 | datagen 阶段崩在 `BuiltInRegistries.ITEM.get(key)` 拿到 `null` | 战利品表里还留着种子的掉落条目 | 15 张作物表的种子参数全删，作物本体保留；新增 `plain_chives_crop` 一张表 | D-213；54 号 §2.3 战利品面；B-53 |
| 34 | `#farmstorm:seeds` 变成空集 | 删了 6 个成员就收工，忘了让 `plain_chives_seeds` 顶上 | 改后成员**恰 1** 个；标签语义是「能种下去的实物」，不是「所有种子类」 | D-212、D-216；B-48；HC-159 |
| 35 | `#farmstorm:converter_fuel` 标签还在，但没有任何使用者 | 只删了 `FarmstormItems` 的引用，没删 `FarmstormTags` 的 `TagKey` | `ModItemTagsProvider` 的那一段与 `FarmstormTags.java:38-40` 一起删 | D-209；52 号 §3.3 第 8 项 |
| 36 | `FarmstormTags` 的注释改了，改的位置却不对 | 注释在 **`:35`**（BRIEF 记作 `:33`） | 只改注释、不改标签语义；改前先读那一行确认 | `FarmstormTags.java:35`；54 号 §2.3 末段；BRIEF §7 第 12 条 |
| 37 | 转换器的正面朝向差 180° | 用 `Direction#toYRot()` 算旋转（它按 data2d 算） | 照 `facingRotationY(...)` 的写法：`north 0 / east 90 / south 180 / west 270` | `ModBlockStateProvider.java:237-239`；54 号 §3.2 |
| 38 | 两态方块有一个 variant 找不到模型 | 用了 `simpleBlock(block, model)`，它只生成单态 | `getVariantBuilder(...).forAllStates(...)` 按 `FACING × WORKING` 分派 | `ModBlockStateProvider.java:204-206`、`:218-220`；D-211 |
| 39 | 转换器的红线在草地与岩浆上看不清 / 像报错 | 用了纯 `0xFF0000` | `0.92 / 0.22 / 0.22 / 1.0`，三分量与青色逐一拉开；青色的三个分量一个字节不动 | 54 号 §5.4；D-210；HC-169 |
| 40 | 把培养器换成转换器后，那条线永远是青色（服务端看起来一切正常） | `Kind` 加了字段，但没进 `fingerprint`（去抖认为「没变化」） | `ORDER` 补第三键、`fingerprint` 卷进 `kind().id()`、组装按同一下标配对，三处同批 | `CultivatorLinkBroadcaster.java:76-78`、`:227-234`；RT-75；A-94 |
| 41 | 旧客户端收到未知 kind 码后画错色或崩 | 未知码没有回落口径 | 未知值按 `CULTIVATOR` 解（前向兼容：画既有青色线而不是崩） | 54 号 §5.1；55 号表 F 第 3 条；D-210 |
| 42 | 调「投掷物寿命」顺手把炮台平衡改了 | 把 `TurretBullet` 接进既有的 `throwable.lifetimeTicks` | 单独开 `pumpkin_turret.bulletLifetimeTicks`；`throwable.lifetimeTicks` 一行不动 | 54 号 §4.4、§6.5；D-221；B-55 |
| 43 | 炮弹第 300 刻没消失，或提前消失 | 把寿命判定放在 `steerTowards` 之后（漏计索敌分支），或按「最后一次命中」计时 | 放在 `tick()` 最开头、`launchOrigin` 取样之后；计时起点是出膛；读档重新计 | `TurretBullet.java:76-90`；D-221；RT-81；B-56 |
| 44 | **判据自身的失效（元规则）**：`grep` 恒 0 或恒非 0、`^\[` 数不到缩进段、拿错 toml 副本恒 121 / 176 | 前缀写错、全仓 `grep` 把 `.scratch/p25a/src` 的反编译源算进来、`run-*/config/` 里有旧副本——都属于「判据永远通过或永远失败」 | 每条判据先在**改前**跑一次并报出实测值，改后再跑一次；两次都算得出来，判据才算立住。本版四个必踩实例：`grep -r PoiType .` 会把反编译源算进来（本仓 `src/` 才是 0）；`Get-Content` 默认编码把行号读错；数 toml 必须用 `^\s*\[` 且只认 `run/config/farmstorm-server.toml`；`grep -c 'BotanistTrade'` 那类「写类前恒 0」要先确认基线 | 48 号 §0.2 第 4 条；55 号 §0.4；50 号陷阱 20 的接力 |

> 落地方式：每条判据的「改前实测值」写进任务回报，改完再报一次；只写「通过」不写数值的，按未验证处理（第 44 条）。

三条附注：① 第 3、4、5、25 条与 50 号的同名陷阱同形（改默认值 / 拿老 toml 验收 / 编码与行号 / 断言只改一半），第 44 条承接 50 号陷阱 20 的元规则，跨版回查按编号找；② 第 44 条的地位高于其余各条——其余 43 条都是它的实例，判据本身站不住时，前面每一条的「通过」都不成立；③ 本表按「症状 → 原因」正查，反向查找（「我要改的这一处算不算陷阱」）用 §2 的判定列与 §5 的勾选项。

## 5. 交付清单

一套勾选项，**逐条勾、勾不上就不算交付**。前 11 条是代码面，第 12 条是记录面。

- [ ] **1** 版本面：`mod_version = 0.2.3`，`0.2.2` 在 6 处字面量里归 0，`build/libs/` 下是 `farmstorm-0.2.3+mc1.21.1.jar`（D-203、F-286）。
- [ ] **2** 方块面：`farmstorm:crop_converter` 三处注册改名完成、铁砧七项属性全中、`FACING × WORKING` = 8 个 variant、两张模型（一张引 `_still`、一张引帧条）（D-206、D-207、D-211、HC-157、HC-168）。
- [ ] **3** 界面面：`ConverterMenu` / `ConverterScreen` / `ConverterRecipeBookCategory` 三个文件不存在，`menu_type` = 1，`FarmstormClient:75` 已内联 finder，`Unknown recipe category` 命中 0（D-208、HC-171）。
- [ ] **4** 配方面：`ConverterRecipe` 只剩 `input` / `result`，`converting` = 21 条全单向无成本，转换器 6 铁 + 2 玻璃 + 1 红石、培养器 4 铁 + 4 玻璃 + 1 红石（D-209、D-217、D-220、HC-162）。
- [ ] **5** 状态机面：`CropConverterBlockEntity` 三字段（`Held` / `Progress` / `Core`）、四步 `serverTick`、`acceptAll` 成功才清槽、`implements Container` 命中 0（D-208、HC-172、陷阱 20 / 21）。
- [ ] **6** 种子面：7 个种子物品删除、22 条档案的 `seedItem` 全 `null`、战利品表去种子、`SeedItem.java` 不存在、`#farmstorm:seeds` 成员恰 1（D-212、D-216、HC-159、HC-164）。
- [ ] **7** 进度面：`seed_cycle` 与 `nether_star_toll` 删除、进度总数 16、`first_conversion` 授予点已搬到 `deliver()` 成功支（D-214、HC-170）。
- [ ] **8** 平凡作物面：`plain_chives_crop` + `plain_chives_seeds` 注册、4 阶段 blockstate 指向 `friend_chives_stageN`、`PlainSeedItem` 能种下去、`CropProfile` 仍 22 条（D-215、D-216、HC-165、HC-167）。
- [ ] **9** 培养器面：投入物 17 个（判据来自配方表）、产出永远两组（自身即种子给 4 个）、合成配方首次存在（D-218 ~ D-220、HC-163）。
- [ ] **10** 协议面：`Link` 三字段、`PROTOCOL_VERSION = "3"`、`MAX_LINKS = 1024`、包数 2、`CultivatorKinds` 与 `Cultivators` 同长、广播三处同步、培养器青 / 转换器红（D-210、HC-169、HC-158）。
- [ ] **11** 计数面：注册面 92、config 175 键 / 60 push、语言键 321 × 2、Java 文件 152，四处与自检、检查器、两份 `lang/*.json` 逐一对上（D-205、D-223、D-224、HC-156、HC-160、HC-161）。
- [ ] **12** 记录面：`README.md` 的三段（升级须知 / 已知影响 / 平衡变更）与 `.work/v023-verify/` 的三份记录（验收 32 条、边界 31 条、签字），且上游文档 `git diff` 为 0（52 号 §8；55 号 F-309 ~ F-311）。

表下注三条：① **勾选项不是验收条目**——54 号 §8 的 32 条与 53 号 §9 的 31 条才是验收，本清单只是「交付面是否齐」的粗筛，两者不一一对应，不许用本清单替代验收；② **每一勾都要能贴读数**——第 11 勾的四个数各要一组读数，第 12 勾要三份记录的文件名加 `git diff --stat`；③ **勾不上就停在原地**——任何一勾勾不上，先解决它再进下一个 Phase，不许带着半勾进 F-309。

## 6. 与上游的冲突处理

### 6.1 裁决顺序

**三条铁律**：① 文档之间冲突 ⇒ 按下面的顺位裁决，**不挑一个喜欢的版本实现**；② 源码与文档冲突 ⇒ 报上来，写进「未决问题」，附 `文件:行号` 与实测值；③ **不许回改上游**——52 ~ 55 已冻结，改它们等于给下游换一份真相源（D-203、N-152）。

遇到 52 / 53 / 54 / 55 之间不一致，按**固定顺序**裁决：

| 顺位 | 谁说了算 | 适用 | 边界 |
|---|---|---|---|
| 1 | **52 号**（D-203 ~ D-224）+ `BRIEF` §2 的 18 条裁定 | 一切口径问题：某件事「是什么」「删不删」「收不收」 | 52 号自述为唯一真相源；裁定是作者拍板的，下游不得改写、只能引用 |
| 2 | **54 号** | 52 号未定的**技术落点**：改哪个方法、序列化形状、色值、范围、落盘形态 | 只在其标「本文确定」的 16 项与逐处 `文件:行号` 范围内生效 |
| 3 | **55 号** | 52 / 54 都未覆盖的**切分与顺序**：窗口开合、提交边界、六条顺序死命令 | 不得改变 54 号的技术结论；A-92 ~ A-101 里属「对上游补写或改判」的（A-97、A-98、A-99、A-100）按 A 条执行，并在回报里登记与之不一致的上游文字 |
| — | **源码** | 一切 `文件:行号` 与运行期事实 | 源码与文档冲突 ⇒ **不自行取舍**，写进回报的「未决问题」，附行号与实测值 |

**一律报给作者**：任何一次裁决都要在回报的「未决问题」里留痕（冲突双方的原话 + 编号 + 暂时按哪边写），不许先改文档再实现，也不许两边都实现一遍。

### 6.2 本版已登记的口径差异与转述核对（10 条，读之前先看这张表）

| # | 谁与谁不一致 | 两边的原话 / 位置 | 本文怎么处理 |
|---|---|---|---|
| 1 | 52 号**自己**给了两个语言键算式 | 52 号 §0.3 的数字纪律写「340 − 21 + 2 = 321」；同一份的 D-224 补充写「340 − 23 + 4 = 321」 | 按**逐键清单**（删 23 / 加 4）写 `340 − 23 + 4 = 321`；两条算式同为 −19，最终值 321 不受影响 |
| 2 | 「改值」条数 | 54 号 §7.4 称 52 号 D-224「只列 1 条」并自称多补 1 条；实读 52 号 D-224 的结论写「2 个**改值**」，表格也列了 2 行（`fang_dance.description` 与 `converter.description`，后者在 `ModLangProvider.java:220`） | 按 **2 条**执行；54 号 §7.4 的「本文多这 1 条」是误记，不影响它的落点与键数 |
| 3 | 旧转换器贴图「6」还是「7」 | `BRIEF` §1.1 的「6 面贴图」说的是模型用到的**面数**；`BRIEF` §5.1、52 号 §3.3 第 12 项、54 号 §3.4 都写 **7 张**，目录实测也是 7 张（含两张孤儿） | 按 **7 张**删；判据是 `converter_*.png` 命中 **0**，不是「6」（A-97） |
| 4 | 54 号 §10 第 4、5 条对 52 号的转述 | 54 号称 52 号写了「进度 26 → 24」「seeds 10 → 4」；实读 52 号 D-214 是 **18 → 16**、D-212 补充是 **6 → 1** | 按 **52 号原文**；54 号的验收条目本身不受影响，只是它的「上游写的」一列不成立 |
| 5 | 54 号 §10 第 6 条对 `BRIEF` 的转述 | 54 号称 `BRIEF` §5 第 2 项写 `:107-112`；实读 `BRIEF` 写的是「`:102` 与 `:104` 之间」——那两行落在私有工厂 `deployed(...)` 里，只有 `:102`/`:104` 之间才编得过 | 按 **`:102` 与 `:104` 之间**；两边结论其实一致，54 号的引文有误 |
| 6 | `FarmstormClient:75` 是删还是改 | `BRIEF` §5 第 8 项写「`:62,75` 两行同步删」；`:75` 删掉会刷 21 条 `Unknown recipe category` WARN | 按 **`:62` 删、`:75` 内联**（A-96、54 号 §2.6）；类文件照删，Java 文件数仍 152 |
| 7 | `ConverterBlock` 的 `useWithoutItem` 行号 | `BRIEF` §1.1 记 `:85-90`；实读是 `:83-87`（`return` 在 `:86`） | 按**实读**引用，事实卡第 3 行括注；方法体内容三处一致 |
| 8 | `SeedItem.useOn` 的行号 | `BRIEF` §1.2 记 `:38-41`；54 号 §4.3.4 记 `:42-44`；实读是 `:41-44`（`PASS` 在 `:43`） | 按**实读**引用，事实卡第 4 行括注；行为结论（无条件 `PASS`）三处一致 |
| 9 | 颜色常量的行号 | `BRIEF` §4.4 记 `CultivatorLinkRenderer.java:47-50`；实读 `:41-44`（`:47` 是端点小框边长） | 按**实读**引用；数值 `0.30 / 0.85 / 0.90` 与 `BRIEF` 一致，改动面不变 |
| 10 | Java 文件终态 152 还是 153 | 55 号 A-98 称「54 号 §1.5 与 `BRIEF` §3.2 写 **153**」；实读两处都写 **152**（`154 − 3 − 1 + 2`） | 按 **152**；A-98 的结论正确，只有它「上游写 153」这一句是误记 |

**三条读法**：① 本表里第 1、2、4、5、10 条是**上游自己的账不平**，不影响任何一个交付值（321 / 16 / 1 / 152 都能从源码重新数出来）；② 第 3、6 条是**判据口径**的差异，必须按「本文怎么处理」那一列执行，否则会在验收时得出相反结论；③ 第 7 ~ 9 条只是行号漂移，按实读用——但**不许**在回报里引用「文档写的行号」而不复核。

### 6.3 已被本版取代或更正的旧口径（6 条，显式声明，免得照旧抄）

| 旧条目 | 它当时说什么 | 本版 |
|---|---|---|
| 50 号 HC-142 / HC-143 | config **176 键 / 60 组**，只改两个默认值 | **175 键 / 60 push**：删 3 加 2，默认值改动之外还有键集合变化（HC-161） |
| 50 号 HC-145 | 语言键 **340 × 2** | **321 × 2**；且 `:10` 与 `:13-16` 两处同改（HC-160） |
| 50 号 HC-153 | `plain_chives` 「不是作物、不能种、不能加配方」 | **翻案的正是前两项**：本版它是可种作物、有种子；只保留「没有 `CropProfile`」（D-215、D-216；52 号 §5 墓碑第 1 条） |
| 50 号 HC-155 | `converter` 的方块 / 配方 / 菜单一律不动 | 本版**整条重做**：改名、删界面、配方收缩、加 `WORKING`（D-206 ~ D-211） |
| D-170 的「转换器暂时移除功能」 | 装饰方块是**临时的**，将来恢复（这条注释就写在同一处：`ConverterBlock.java:85-90`） | 本版**兑现**：恢复的不是旧功能，是一台新机器（D-204、D-206） |
| 46 号 D-193 的「平凡韭菜无种子、不可种」 | 0.2.2 的定论 | 前两项翻案，只留「没有 `CropProfile`」；`plain_chives_seeds` 是本版唯一的真播种物（D-215） |

### 6.4 发现新冲突时的动作

写进回报的「未决问题」，四件齐：① 冲突双方各自的 `文件:行号` 或编号；② 两边的原话；③ 暂时按哪一边写（只允许选「52 号 + 18 条裁定」这一侧，或先不动）；④ 需要谁拍板。**不许**先改文档再实现，也不许把两边都实现一遍。

## 7. 回报口径

每份回报固定四段，**顺序不变、缺段即未完成**：

```
## 结论
一句话：做了什么、对应哪几个编号（F 号 / D 号 / 裁定 # / 54 号 §8 的验收号 / 53 号 §9 的 B 号）。
禁止写「完成」「正常」「没问题」——那不是结论。

## 证据
每条判据三行：命令原文 → 关键输出（原样一至三行）→ 编号（HC-xx / N-xx / 54 §8 #n / B-nn）。
计数类判据给「改前实测值 / 改后实测值」两个数（陷阱 44）。
改动面给 `git diff --stat` 的文件清单（一任务一提交，多一个文件就是超范围）。
config 类判据给「老 toml 的期望红」与「新 config 的绿」两组读数，缺一组按未跑处理。

## 未决问题
拿不准的、文档之间冲突的、需要作者拍板的。没有就写「无」——不许省略这一段。

## 下一步
按 55 号的依赖图，下一个该做的任务号；以及本轮没做但已经看见的问题（只登记，不动手）。
```

三条约束：

1. **证据必须是可观察的**：日志行、命令读数、`git diff`、目录计数、`/data get` 读数、游戏内截图。日志超过 20 行只留**第一条错误**与**总结行**。
2. **每条断言都要落编号**：没有编号的断言按「未验证」处理；写了编号但没给读数的，按「未跑」处理。
3. **不许只贴文件内容**：`git diff` 只贴改动的那几行；回报是给下一个人做判断用的，不是存档。
