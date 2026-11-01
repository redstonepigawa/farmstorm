# 24 · v0.1.0 技术规格（Technical Specification）

> 本文件是「农场风云 / Farmstorm」v0.1.0 的技术规格：注册增量实测、类设计、生长时间的确定性推进、
> 陷阱系统四项改造、耕地额度与据点核心、指令注册、config 与资源清单、RT-24 ~ RT-30 风险册、验收自检表。
> 唯一真相源是 `docs/zh/22-DECISIONS-0.1.0.md`（D-103 ~ D-123，共 21 条）；冲突时以该文件为准。
> 基线是**已实现并发版的 0.0.4**（`dist/farmstorm-0.0.4+mc1.21.1.jar`，959,376 B）。
> 全部源码事实取自 `src/main/java/com/dashpig/farmstorm/`，行号以 0.0.4 发版代码为准；数字一律由本文自己从源码点名数出（§1）。

---

## 0. 本文范围与 18-TECH-0.0.3.md 的关系

### 0.1 本文负责什么

| 项 | 内容 |
|---|---|
| 版本 | 0.1.0（增量版，构建基线不变：MC 1.21.1 / NeoForge 21.1.213 / Java 21） |
| 负责 | 注册增量 +3 的**实测核对与逐个点名**；八个新增类与十九个条目的改动面（18 改 + 1 处核对后不改）的设计；生长时间的计划 tick 链；陷阱四项改造的挂载点；耕地额度与据点核心的三条链路；`/farmstorm farmlandlimit` 的注册与权限；config 全量清单（12 行）；资源与语言键清单；RT-24 ~ RT-30；验收自检 18 条 |
| 不负责 | 9 条需求的玩法叙述与时序表（`23-DESIGN-0.1.0.md`）；任务拆分与工作量（`25-TASKS-0.1.0.md`）；提示词（`26` / `27`） |
| 不动的文件 | `docs/zh` 的 00 – 23 与 `docs/en` 全量（已冻结）；`src/` 在本文写作期间一行不动 |

### 0.2 与 `docs/zh/18-TECH-0.0.3.md` 的关系

18 号建立的体例本文全部沿用，一处不新创：

| 体例 | 18 号的位置 | 本文的继承方式 |
|---|---|---|
| 基线核对的三条证据链 | §1.1 | §1.1 沿用「逐条读构造点 → 对照启动自检 → 对照既有文档」，并把本文用到的每一个工厂方法的调用次数逐个列出 |
| 逐类实测表 | §1.2 | §1.2 同为 12 行表 + 表下逐类小计代码块 + 表下核对结论 |
| 基线不一致的登记 | §1.4 | §1.4 登记 **verify_project.py 的实跑输出与三处缺口**，并写明「哪边可能是错的」 |
| 注册对象逐项表 | §2.1 / §2.2 | §2 沿用「registry × 基线 × 增量 × 目标」四列口径 + 逐个点名表 |
| 类设计四项 | §3 开头 | §3 沿用「职责 / 关键方法 / 读哪些 config 键 / 改哪些既有文件」 |
| config 逐项清单 | §8.1 | §8.1 沿用同一列集（组.键 / 类型 / 默认 / 范围 / 用途 / D），行数先点数再用 |
| 资源清单 | §9.1 / §9.3 / §9.4 | §9 拆成 PNG / JSON / 语言键三张表，各自给分类小计 |
| 风险册 | §10 | §10 给 RT-24 ~ RT-30，字段同为五项（风险 / 触发 / 首要缓释 / 兜底 / 实现阶段怎么验） |
| 「待验证」登记 | §0.5 | §0.5 沿用，本文 8 处逐条挂到某条 RT |
| 数值纪律 | §0.4 | §0.4 沿用；本文**可以**写注册总数，因为它已经是实测值（§1.2） |

**与 18 号的关系一句话**：18 号是「在 0.0.2 之上再加 13 个对象、把 77 变 90」的规格；本文是「在 0.0.4 之上再加 3 个对象、把 90 变 93」的规格。加法方式逐字相同，区别只有一处：18 号能拿 77 当基线，是因为源码里有一行启动自检断言了那 12 个数字；本文能拿 90 当基线，是因为同一行断言在 0.0.4 里断言的就是 90，且本文 §1.2 的逐类实测与它逐项相等。

### 0.3 三张清单的自洽承诺

本文的三张清单（§2 注册对象、§8 config 键、§9 资源文件）各自满足：

1. **行数 = 声明的计数**：每张表写「本表 N 行」，N 与表的数据行数逐行点数一致，不先写数再凑行。
2. **小计 = 逐行相加**：三张表都在表下给分类小计，小计之和等于表行数。
3. **跨表不重不漏**：注册对象只进 §2；config 键只进 §8；PNG / JSON / 语言键只进 §9。`DeployedKind` 的 8 个枚举常量、`CropProfile` 的 22 条档案都不是注册对象，只出现在 §3 与 §5，不进任何计数。

### 0.4 数字口径

| 类别 | 口径 | 出处 |
|---|---|---|
| 已实测 | 注册对象 **90**（12 类逐类见 §1.2） | 逐条读 11 个 `registry/*.java` 数出，与 `Farmstorm#logRegistrySelfCheck` 的 13 条断言逐项相等（`Farmstorm.java:138-141`） |
| 已实测 | config **155** 键 | `FarmstormConfig` 里 `define*` 调用 155 次；`farmstorm.configuration.*` 语言键也是 155（实测） |
| 已实测 | 语言键 **299** | 中英各 299、键集一致（本文实测，与 `tools/verify_project.py` §3 输出同值） |
| 已实测 | 作物档案 **22** / 幸运浆果池 **19** | `util/CropProfile.java:131-143`；启动自检断言 22 / 19（`Farmstorm.java:103-113`） |
| 已实测 | 资源面：blockstates **25**、`models/block` **70**、`models/item` **31**、`loot_table/blocks` **25**、`recipe` **28**、`advancement` **18** | `src/generated/resources` 逐目录计数 |
| 增量 | +3（注册）/ +11（config 键）/ +16（语言键）/ +5 新增 PNG + 6 就地替换 / +5 新增 JSON + 2 就地替换 | 本文 §2 / §8 / §9 |
| 目标值 | 注册 **93**、config **166**、语言键 **315** | 实测值 + 增量，逐项列在 §2.4 / §8.3 / §9.4 |
| 禁止 | 任何未经 §1.2 实测支持的注册总数；任何凭空出现的数值（无出处又未标「本文补充」的，一律视为缺陷） | 本文 §0.3 + 施工事实卡 §6 第 2 条 |

### 0.5 「待验证」的登记规则

本文不发明任何 Minecraft / NeoForge API。凡是不能从本仓既有源码里找到用法先例的调用，一律在该处标「**待验证**」，并在方括号里写明所挂的 RT 编号。本文共 **8 处**（① – ⑧），分布为 RT-24 三、RT-25 一、RT-26 一、RT-28 二、RT-30 一（相加 8）。正文里带方括号的引用共 **9 次**——① 在 §4.3 与 §4.4 各引一次、⑤ 在 §3.3 与 §3.4 各引一次，同一个待验证项在两处被引用。标了待验证的调用在实现阶段必须先写一个最小用例跑通，再写正式逻辑；跑不通就换成该 RT「兜底」栏写明的替代路径，并把结论写进验收记录。

| # | 待验证的调用 | 挂在哪 | 本仓为什么没有先例 |
|---|---|---|---|
| ① | `Level#scheduleTick(BlockPos, Block, int)`、`Block#tick(BlockState, ServerLevel, BlockPos, RandomSource)`、`Block#onPlace(BlockState, Level, BlockPos, BlockState, boolean)`、`isRandomlyTicking(BlockState)` 的 1.21.1 签名与调用时机 | RT-24 | 全仓 `scheduleTick` / `onPlace` / `randomTick` **0 命中** |
| ② | 同一 `(BlockPos, Block)` 重复 `scheduleTick` 是覆盖还是并存（`SavedTick` 的相等口径） | RT-24 | 同上 |
| ③ | 计划 tick 是否随区块写进 `block_ticks` 并在服务器重启后继续推进 | RT-24 | 同上；D-111 只给语义、未给实测记录 |
| ④ | `RegisterCommandsEvent` 的订阅方式，以及 `Commands.literal` / `IntegerArgumentType.integer` / `requires` 的注册写法 | RT-25 | 全仓 `RegisterCommandsEvent` / `CommandSourceStack` / `LiteralArgumentBuilder` **0 命中** |
| ⑤ | `SavedData` / `DimensionDataStorage#computeIfAbsent` 在 1.21.1 的工厂签名与保存形态 | RT-26 | 全仓 `SavedData` / `DimensionDataStorage` / `getDataStorage` **0 命中** |
| ⑥ | 土 → 耕地的拦截点：`HoeItem` 在 1.21.1 的形态（静态映射表还是数据驱动），以及 `PlayerInteractEvent.RightClickBlock` 的取消语义是否足以「不产生耕地、不扣耐久」 | RT-28 | 全仓 `HoeItem` / `Tillable` / `FarmlandBlock` **0 命中** |
| ⑦ | 耕地踩坏的拦截点：NeoForge 21.1.213 是否提供可取消的耕地踩踏事件（`FarmlandTrampleEvent` 一族） | RT-28 | 全仓 `FarmlandBlock` / `turnToDirt` **0 命中** |
| ⑧ | `ClientboundLevelParticlesPacket` 的构造参数，以及「只发给指定玩家」的写法（`ServerPlayer#connection.send`） | RT-30 | 本仓只有 `ClientboundSoundEntityPacket` 的同类先例（`combat/WildHunterAura.java:211`），粒子包没有 |

---

## 1. 基线核对（现有注册对象实测 + verify_project.py 缺口）

### 1.1 计数方法

**不用 grep 计数。** `grep "register"` 会把工厂方法体、注释、`DeferredRegister` 的字段声明一起数进去。本文用三条互相独立的证据链：

1. **逐条读构造点**：打开 11 个 `registry/*.java`，只数「真正产生一个注册条目」的调用——`REGISTER.register(...)` / `registerItem(...)` / `registerBlock(...)` / `registerComponentType(...)`，以及**工厂方法的每一次调用点**。工厂方法本身不是条目。本文用到的工厂方法与调用次数逐个列明：

| 工厂方法 | 定义处 | 调用次数 | 产出条目 |
|---|---|---|---|
| `FarmstormBlocks.crop(CropProfile)` | `registry/FarmstormBlocks.java:82-91` | **15**（`:25-38` 14 行 + `:42` 1 行） | 15 个作物方块 |
| `FarmstormBlocks.deployed(...)` | `:93-111` | **8**（`:45`、`:46`、`:47-48`、`:49-50`、`:51-52`、`:54-55`、`:61-62`、`:64-65`） | 8 个部署方块 |
| `REGISTER.registerBlock(...)` 直调 | `:68`、`:72` | **2** | `converter`、`compressed_hay_block` |
| `FarmstormItems.cropItem(...)` | `registry/FarmstormItems.java:136-147` | **22**（`:36`–`:95`，逐行点） | 22 个作物物品 |
| `FarmstormItems.seedItem(...)` | `:149-154` | **7**（`:89`、`:100`–`:107`） | 7 个播种物 |
| `REGISTER.registerItem(...)` 直调 | `:110`、`:113` | **2** | 两个方块物品 |
| `FarmstormEntityTypes.projectile(...)` | `registry/FarmstormEntityTypes.java:70-78` | **12**（`:31`–`:58`，逐行点） | 12 个投掷物类型 |
| `REGISTER.register(...)` 直调 | `:60-66` | **1** | `turret_bullet` |
| `FarmstormBlockEntities.REGISTER.register(...)` | `registry/FarmstormBlockEntities.java:22-42` | **6** | 6 个 BlockEntityType |
| `FarmstormDamageTypes.key(String)` | `registry/FarmstormDamageTypes.java:53-55` | **3** | 3 个伤害类型 `ResourceKey` |

  `projectile(...)` 被调 **12** 次 ⇒ 12 个条目，不是 13 个——这一句是本计数方法的全部要点，也是 0.0.2 / 0.0.3 两次翻车的地方。
2. **对照启动自检**：`Farmstorm#logRegistrySelfCheck()`（`Farmstorm.java:123-147`）在每次启动时把 12 类各数一遍——数的是 `DeferredRegister#getEntries().size()` 而不是写死常量——并断言 `total == 90` 且逐类等于一组具体数字（`:138-141`）。这是**运行期**的第二份独立账本。
3. **对照既有文档与静态验收器**：`22-DECISIONS-0.1.0.md` §3.2 的 +3 分项，以及 `tools/verify_project.py:150-163` 的 12 类硬编码小计（本文实测与它逐项相等，见 §1.4）。

三条证据一致 ⇒ 基线成立。任一条不一致 ⇒ 记进 §1.4。

### 1.2 逐类实测（v0.0.4 基线，共 90）

本表 12 行。第 3 列写「数了什么」；第 4 列本文实测；第 5 列 `Farmstorm#logRegistrySelfCheck` 的断言值。**两列逐项相等**。

| # | registry | 数了什么（逐条点名的依据） | 实测 | 启动自检 |
|---|---|---|---|---|
| 1 | `Registries.ITEM` | 22 个 `cropItem(...)` + 7 个 `seedItem(...)` + 2 个 `registerItem(...)` | **31** | 31 |
| 2 | `Registries.BLOCK` | 15 个 `crop(...)`（`FarmstormCropBlock`）+ 8 个 `deployed(...)`（`DeployedCropBlock`）+ `converter` + `compressed_hay_block` | **25** | 25 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 5 个部署类型（`blast_carrot` 收 2 个方块 / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` 收 3 个方块）+ `converter` | **6** | 6 |
| 4 | `Registries.ENTITY_TYPE` | 12 次 `projectile(id, factory)` + `turret_bullet` | **13** | 13 |
| 5 | `Registries.MOB_EFFECT` | `wild_hunter` / `zangief_piledriver` / `q_bounce` / `confusion` | **4** | 4 |
| 6 | `Registries.SOUND_EVENT` | `wild_hunter_loop` / `zangief_piledriver` / `friend_chives` | **3** | 3 |
| 7 | `Registries.RECIPE_TYPE` | `converting` | **1** | 1 |
| 8 | `Registries.RECIPE_SERIALIZER` | `converting` | **1** | 1 |
| 9 | `Registries.CREATIVE_MODE_TAB` | `main` | **1** | 1 |
| 10 | `Registries.DAMAGE_TYPE` | 数据驱动，代码侧只有 `ResourceKey`：`void_bolt` / `crop_blast` / `zangief_slam` | **3** | 3 |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `temporary_enchantments` | **1** | 1 |
| 12 | `Registries.MENU` | `converter` | **1** | 1 |
| | **合计** | | **90** | 90 |

逐类小计（用于核对，必须与上表逐行相等）：

```
items                 31
blocks                25
block_entities         6
entity_types          13
mob_effects            4
sound_events           3
recipe types           1
recipe serializers     1
creative tabs          1
damage_types           3
data_components        1
menu types             1
--------------------------------
相加 = 31+25+6+13+4+3+1+1+1+3+1+1 = 90
```

**核对结论**：逐类实测相加 = **90**，`Farmstorm#logRegistrySelfCheck` 的断言是 **90**，`tools/verify_project.py:150-163` 的 12 类小计相加也是 **90**。三条独立算法互为校验 ⇒ **90 是本文的基线**，施工事实卡的 90 站得住，无需上报。

### 1.3 交叉核对：22 / 19 / 15 / 4 与注册总数

| 被核对的数 | 文档声称 | 本文实测 | 结论 |
|---|---|---|---|
| `CropProfile` 档案条数 | 22（22 号 §1.1） | `ALL`（`util/CropProfile.java:131-136`）恰好 22 项，且 `public static final CropProfile` 常量 22 个 | 一致 |
| 幸运浆果随机池 | 19（D-55 / D-97） | `LUCKY_BERRY_POOL` = `ALL` 滤掉 `LUCKY_BERRY` / `WILD_APPLE` / `COMPRESSED_PUFFED_WHEAT` = 19 | 一致（本版不改推导式，仍 19） |
| 作物类型分布 | 投掷型 12 / 部署型 7 / 食用型 3 | `Farmstorm.java:104` 的断言与之逐值相等；破障类 4 | 一致 |
| 作物方块数 | D-110 说「15 个 `FarmstormCropBlock` 注册项」 | `crop(...)` 调用 15 次；22 − 15 = 7 条档案没有种植形态 | 一致 |
| 具备 TRAP 类别的档案 | 22 号 §2.5 的四个陷阱 | `BLAST_CARROT` / `FANG_TRAP_CACTUS` / `ZANGIEF_CANE` / `VIBRATION_BEAN` 共 **4** 条，对应 4 个 `DeployedKind` | 一致（§5.4 的 16 名额只统计这 4 个 kind 的部署物） |
| 资源面与注册面的对应 | — | blockstates 25 = blocks 25；`models/item` 31 = items 31；`loot_table/blocks` 25 = blocks 25 | 一致（据点核心必须同步 +1，见 §9.2） |
| config 键数 | 155（施工事实卡 §0） | `define*` 调用 155 次；`farmstorm.configuration.*` 语言键 155 | 一致 |
| 语言键数 | 299 × 2 | 中英各 299、键集一致：`configuration` 155 + `advancements` 36 + `item` 31 + `block` 25 + `tooltip` 16 + `entity` 13 + `gui` 8 + `death.attack` 6 + `effect` 4 + `subtitles` 3 + `container` 1 + `itemGroup` 1 | 一致 |
| 进度 | 18 | `src/generated/resources/data/farmstorm/advancement` 18 个 JSON；本版 +0 | 一致 |

### 1.4 `tools/verify_project.py` 实跑输出与三处缺口

**先给结论**：本版开工时这个工具是**绿的**（退出码 0、FAIL 0、WARN 0），不是红的。任务书里「它现在是红的」这一前提与实测不符，22 号 §4.2 第 5 行「不变就永远红」的措辞也不成立——这两处按实测登记，本文不按前提倒推结论。

实测命令与输出（`python -X utf8 tools/verify_project.py`，工作目录 `D:\project_farm`）：

```
== 1. config：文档键集 vs 生成的 toml ==
  文档口径键数: 155（01-DESIGN §3.8 117 + 12-TECH §8.1 22 + 18-TECH §8.1 19 行；其中 3 行是改既有默认值、
  键名已在上两份清单里，按并集去重后 155 = 139 + 16）
  生成 toml 键数: 155
  [OK]   155 个配置键与文档逐字一致（toml 155 键）
== 2. 90 个注册 ID 逐字核对（全源码树） ==
  [OK]   items: 31 个 ID 齐全
  [OK]   blocks: 25 个 ID 齐全
  [OK]   block_entity_type: 6 个 ID 齐全
  [OK]   entity_type: 13 个 ID 齐全
  [OK]   mob_effect: 4 个 ID 齐全
  [OK]   sound_event: 3 个 ID 齐全
  [OK]   recipe_type: 1 个 ID 齐全
  [OK]   recipe_serializer: 1 个 ID 齐全
  [OK]   creative_mode_tab: 1 个 ID 齐全
  [OK]   damage_type: 3 个 ID 齐全
  [OK]   data_component_type: 1 个 ID 齐全
  [OK]   menu_type: 1 个 ID 齐全
  已核 registry 对象: 90 / 90
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 299 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch

结论：FAIL 0 项，WARN 0 项
```

退出码 **0**。三处缺口全部与「v0.1.0 落地之后」有关，逐条给出定位、后果与修法：

| # | 缺口 | 定位（源码） | 现状后果 | v0.1.0 落地后的后果 | 修法（写进 §11 #1 的动作顺序） |
|---|---|---|---|---|---|
| G1 | 三张 `EXPECTED` 清单里没有 `outpost_core` | `verify_project.py:120-146` | 工具**看不到**这 3 个新对象；加完照样 90 / 90 全绿 | **比红更糟**：注册面 +3 之后工具仍是绿的，等于这 3 个对象永远没人复核 | `EXPECTED["items"]` / `["blocks"]` / `["block_entity_type"]` 各追加 `"outpost_core"` |
| G2 | `EXPECTED_COUNTS` 的 12 类小计硬编码在 90 的基线 | `verify_project.py:150-163` | 与 §1.2 实测逐项相等，当前自洽 | 只加 G1 不改这里 ⇒ 立刻红（`清单 26 个 ID，但 §2.1 的 v0.0.3 列是 25 个`）。**这才是 22 号 §4.2 第 5 行想表达的事**：不是「不改就永远红」，而是「加了清单不改计数才会红」 | `items` 31→**32**、`blocks` 25→**26**、`block_entity_type` 6→**7**；表下注释的 `90` 改 **93** |
| G3 | config 期望集只读三个已冻结的文档 | `verify_project.py:174-189` | 155 与源码 155 逐字一致 | 新 toml 变 166 后，多出的 11 个键在源码里存在 ⇒ 命中 `real_extra` ⇒ 打 `toml 多 11 个键且源码里存在（文档漏记 MISMATCH）` 并红 | 增加第四个来源：解析 `docs/zh/24-TECH-0.1.0.md` §8.1 的 12 行，取「新」列的 11 个键名并联集；期望键数随之 155 → **166**。**不许回改 01 / 12 / 18 号**（那三个文件在冻结区） |

一处**不是缺口**但必须记账：语言文件那段（`verify_project.py:239-249`）只比对中英键集是否相等、不写死条数，因此 +16 键既不会让它变红也不会被它拦住——**315 这个数只能靠本文 §9.4 与 §11 #1 的复核兜住**。

**哪边可能是错的**：22 号 §4.2 第 5 行把「工具现在就是红的」写成了事实，实测是绿的；错在 22 号对工具现状的描述，不在它的处置动作（「按 +3 重算」仍然必须做）。本文按实测口径改写这一行的因果，不改它的动作。

### 1.5 与施工事实卡的一致性

`BRIEF §0` 的 90、`Farmstorm.java:138-141` 的 13 条断言、`verify_project.py:150-163` 的 12 类小计、22 号 §3.2 的「+3 ⇒ 93」四者一致，**无需上报**。BRIEF §3.2 未列、但 22 号 §4.3 已补登记的两处（`TrapBehavior` 签名放宽、`FarmstormBlocks.java:88` 的 `.randomTicks()`）本文在 §3.5 / §4.4 里各自给了完整处置。

---

## 2. 增量注册对象总表

### 2.1 逐 registry 核对表

统计口径与 18 号 §2.1 逐字相同：**registry 对象计数**；`recipe` / `loot_table` / `advancement` / `tag` 的 JSON 与方块状态文件是数据文件，不进注册计数（§2.3 单列）。本表 12 行。

| # | registry | 0.0.4 | 增量 | v0.1.0 | 增量明细 |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 31 | **+1** | **32** | `outpost_core`（`BlockItem`） |
| 2 | `Registries.BLOCK` | 25 | **+1** | **26** | `outpost_core`（`OutpostCoreBlock`） |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 6 | **+1** | **7** | `outpost_core`（`OutpostCoreBlockEntity`，**新建 type**，见注 1） |
| 4 | `Registries.ENTITY_TYPE` | 13 | +0 | **13** | 本版不新增投掷物（与「0 自定义网络包」同向） |
| 5 | `Registries.MOB_EFFECT` | 4 | +0 | **4** | 免疫不是效果；混乱已存在 |
| 6 | `Registries.SOUND_EVENT` | 3 | +0 | **3** | 本版全部复用原版音效（陷阱 dust 是粒子、无音） |
| 7 | `Registries.RECIPE_TYPE` | 1 | +0 | **1** | 据点核心用原版 `minecraft:crafting_shaped`，不进本模组的 `converting` |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | +0 | **1** | 同上 |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | +0 | **1** | `outpost_core` 追加进既有 `farmstorm:main` |
| 10 | `Registries.DAMAGE_TYPE` | 3 | +0 | **3** | 生物触发陷阱复用既有 `crop_blast`（胡罗贝）与 `zangief_slam`（桑吉尔蔗） |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | +0 | **1** | — |
| 12 | `Registries.MENU` | 1 | +0 | **1** | 据点核心没有界面 |
| | **合计** | **90** | **+3** | **93** | |

### 2.2 新增的 3 个对象逐个点名

**3 行**。每条给完整注册 ID、承载类与写法依据。

| # | registry | 完整 ID | 承载类 / 形态 | 依据 |
|---|---|---|---|---|
| 1 | blocks | `farmstorm:outpost_core` | `OutpostCoreBlock extends BaseEntityBlock`；注册写法与 `converter` 同形：`FarmstormBlocks.REGISTER.registerBlock("outpost_core", OutpostCoreBlock::new, properties)`（先例 `registry/FarmstormBlocks.java:68-69`） | D-119、D-122 |
| 2 | block_entities | `farmstorm:outpost_core` | `OutpostCoreBlockEntity extends BlockEntity`；`REGISTER.register("outpost_core", () -> BlockEntityType.Builder.of(OutpostCoreBlockEntity::new, FarmstormBlocks.OUTPOST_CORE.get()).build(null))`（先例 `:40-42` 的单方块例） | D-119 第 4 项（放置者归属）、D-120 第 4 项（破坏后转泥土）、D-121（tick 发粒子）、D-122 第 1 项（每人 2 个） |
| 3 | items | `farmstorm:outpost_core` | `BlockItem`；`REGISTER.registerItem("outpost_core", props -> new BlockItem(FarmstormBlocks.OUTPOST_CORE.get(), props), otherProperties())`（先例 `:113-114`） | D-119 第 5 项（掉落自身）、D-122 第 3 项（可合成 ⇒ 必须有物品） |

**注 1（第 3 行为什么必须新建 type，不能挂进既有 6 个）**：`DeployedCropBlockEntity` 的构造器用 `kindOf(state)` 反查 `DeployedKind`，非 `DeployedCropBlock` 的方块会被直接抛 `IllegalStateException`（`blockentity/DeployedCropBlockEntity.java:112-118`）；据点核心既不是部署方块、也没有 8 个 `DeployedKind` 之一，塞进既有 type 会在方块实体构造期就崩。`ConverterBlockEntity` 的 type 只收 `converter`（`registry/FarmstormBlockEntities.java:40-42`），且它的 BE 类没有归属字段。因此 **+1 个 type 是这 +1 个方块的必然结果**，不是可以省的选择。

**注 2（不新增 `DeployedKind`、不新增 `CropProfile`）**：`DeployedKind` 仍 8 个（`behavior/DeployedKind.java:10-20`）；`CropProfile.ALL` 仍 22（`util/CropProfile.java:131-136`）。据点核心不是作物、不是部署物，不参与 `deployedOf(DeployedKind)` 的 switch（`registry/FarmstormBlockEntities.java:47-58`）。

**注 3（`deployedOf` 不需要改动）**：8 个 `DeployedKind` 一个不增，switch 少一个分支编译器就报错，因此这处零改动即为正确。

### 2.3 不属于 registry 的增量（不进 93）

| 增量 | 数量 | 性质 | 位置 |
|---|---|---|---|
| 合成配方 | **+1** | 数据文件 | `data/farmstorm/recipe/outpost_core.json`（3×3：四角 `minecraft:stone`、正中 `minecraft:diamond`、其余四格 `minecraft:redstone`，产出 1 个，D-122 第 3 项） |
| 战利品表 | **+1** | 数据文件 | `data/farmstorm/loot_table/blocks/outpost_core.json`（掉自身，D-119 第 5 项） |
| 方块状态 / 模型 | **+3** | 资源文件 | `blockstates/outpost_core.json`、`models/block/outpost_core.json`、`models/item/outpost_core.json`（§9.2） |
| 方块标签追加 | **+1 项** | 数据文件 | `minecraft:mineable/pickaxe` 追加 `farmstorm:outpost_core`；**不进** `needs_stone_tool` / `needs_iron_tool` / `needs_diamond_tool`（D-119 第 2 项） |
| 指令树 | **+1 棵** | 运行期注册（Brigadier），**不是 registry 对象** | `/farmstorm` 根 + `/farmstorm farmlandlimit <int>`（§7） |
| 语言键 | **+16** | 数据文件 | §9.4 |
| 进度 | **+0** | — | 仍 18 条（22 号 §3.2 附注、§6 第 14 条） |
| 其余 9 类 | **+0** | — | `entity_type` / `mob_effect` / `sound_event` / `damage_type` / `recipe_type` / `recipe_serializer` / `creative_tab` / `data_component_type` / `menu_type` |

### 2.4 加总核对

| 算法 | 算式 | 结果 |
|---|---|---|
| 基线 + 增量 | 90 + 3 | **93** |
| 逐类相加 | 32 + 26 + 7 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 1 | **93** |
| 分项相加 | items 1 + blocks 1 + block_entities 1 | **3** |

三条算式互为校验，**§2 的加总等于 §1 实测的 90 + 3 = 93**。另有 6 个数据文件条目与 1 棵指令树不进这个和，单列在 §2.3。

---

## 3. 新增与改动的类设计

每类给四项：**职责 / 关键方法 / 读哪些 config 键 / 改哪些既有文件**。config 键名一律能在 §8.1 的 12 行里查到。

### 3.0 总览

| 类别 | 文件 | 新 / 改 |
|---|---|---|
| 方块 1 个 | `block/OutpostCoreBlock.java` | 新 |
| 方块实体 1 个 | `blockentity/OutpostCoreBlockEntity.java` | 新 |
| 指令 1 个 | `command/FarmstormCommands.java` | 新 |
| 耕地 2 个 | `farmland/FarmlandLedger.java`（额度与已开垦坐标集合）、`farmland/FarmlandGuard.java`（锄地拦截 + 3×3 + 踩坏 + 转泥土） | 新 |
| 据点核心索引 1 个 | `outpost/OutpostCoreIndex.java`（玩家 → 核心坐标，服务端 `SavedData`） | 新 |
| 陷阱 2 个 | `trap/TrapFifoIndex.java`（玩家 → 陷阱坐标，`SavedData`）、`trap/TrapImmunity.java`（5 秒免疫的内存表） | 新 |
| 陷阱接口 | `behavior/TrapBehavior.java`（签名放宽 + 免疫闸 + dust） | 改 |
| 生长 | `block/FarmstormCropBlock.java`（`onPlace` / `tick` / `isRandomlyTicking`） | 改 |
| 既有 9 个文件 | `registry/FarmstormBlocks.java`、`registry/FarmstormBlockEntities.java`、`registry/FarmstormItems.java`、`Farmstorm.java`、`config/FarmstormConfig.java`、`blockentity/DeployedCropBlockEntity.java`、`util/CropProfile.java`、`entity/thrown/MelonGrenadeProjectile.java`、`entity/thrown/PuffedWheatProjectile.java` | 改 |
| 四个陷阱实现 | `behavior/impl/{BlastCarrotTrap,FangTrapCactusTrap,ZangiefCaneTrap,VibrationBeanTrap}.java` | 改 |
| 接线 | `event/FarmstormGameEvents.java`、datagen 的 6 个 Provider、`tools/verify_project.py`、`tools/gen_textures.py` | 改 |

新增文件 **8 个**（方块 1 + 方块实体 1 + 指令 1 + 耕地 2 + 核心索引 1 + 陷阱 2）。改动文件 **18 个**（另有一个文件核对后确认不改，一并列在 §3.7 留痕），逐项列在 §3.7。

### 3.1 `OutpostCoreBlock`（新，`block/`）

| 项 | 内容 |
|---|---|
| 职责 | 据点核心的方块面：注册属性（硬度 30 / 抗爆 1200 / 任意镐可挖 / 不可推动）、放置者归属写入、每人 2 个的放置上限、破坏时把方块实体的归属交给释放流程。玩法效果（+16 额度、范围判定、边界粒子）在方块实体与索引里 |
| 关键方法 | `newBlockEntity(pos, state)` → `new OutpostCoreBlockEntity(pos, state)`；`getTicker(level, state, type)` 服务端返回 ticker（先例 `block/DeployedCropBlock.java:146-158`，含 `level.isClientSide` 早退）；`getStateForPlacement(ctx)` 里先判 `OutpostCoreIndex.countOwned(玩家) >= outpost_core.maxPerPlayer` ⇒ 返回 `null`（放置不成立、物品不消耗、无掉落，D-122 第 2 项）；`setPlacedBy(level, pos, state, placer, stack)` 写 `ownerUuid` 并登记索引（先例 `:292-297`）；`getDestroyProgress` 走原版公式（**不做**己方秒拆，D-119 补充）；`onRemove(...)` 只做一件事——把方块实体的归属交给 `OutpostCoreBlockEntity#release()` |
| 注册属性 | `BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(30.0F, 1200.0F).requiresCorrectToolForDrops().sound(SoundType.STONE).pushReaction(PushReaction.BLOCK)`。`requiresCorrectToolForDrops()` 只要求「工具对」，具体等级由 `needs_*_tool` 决定；本版**不加**任何一级标签 ⇒ 木镐即对工具（D-119 第 2 项），标签只进 `mineable/pickaxe`；不可推动用注册属性而不是监听活塞事件（先例 `registry/FarmstormBlocks.java:101-102`，注释里写的正是这条理由） |
| 读哪些 config 键 | `outpost_core.maxPerPlayer`（2）；`outpost_core.farmlandBonus`（16）由索引侧读，本类不读 |
| 改哪些既有文件 | `registry/FarmstormBlocks.java`（+1 个注册行）；`registry/FarmstormBlockEntities.java`（+1 个 type）；`registry/FarmstormItems.java`（+1 个 `BlockItem`）；`datagen/ModBlockStateProvider.java`、`ModItemModelProvider.java`、`ModLootTableProvider.java`、`ModRecipeProvider.java`、`ModBlockTagsProvider.java`、`ModLangProvider.java`（各 +1 项）；`Farmstorm.java`（自检断言） |

### 3.2 `OutpostCoreBlockEntity`（新，`blockentity/`）

| 项 | 内容 |
|---|---|
| 职责 | 三个字段加一条 tick 链：`ownerUuid` / `ownerName` / `placedGameTime`（**不加**队伍快照，C-9 的口径）；每 `boundaryParticleIntervalTicks` 刻向「己方且在看得到的范围内」的玩家发一轮边界粒子；`setRemoved` 时释放名额并把范围内己方耕地转泥土 |
| 关键方法 | `saveAdditional` / `loadAdditional` 读写三字段，**读写复用 `util/DeployedOwnership`** 的 `write(tag, uuid, name)` / `readUuid` / `readName`（`util/DeployedOwnership.java:24-45`，NBT 键名 `ownerUuid` / `ownerName` 不加前缀，与部署方块同形）；`owner()` 用 `level.getServer().getPlayerList().getPlayer(uuid)` 取在线实体（先例 `blockentity/DeployedCropBlockEntity.java:293-299`）；`isAlly(Player)` 走 `DeployedOwnership.isAlly(server, ownerUuid, ownerName, player)`（`:285-290` 同形）；`serverTick(...)` 静态 ticker：`floorMod(now, outpost_core.boundaryParticleIntervalTicks) == 0` 时跑一轮，同区块多核心用 `floorMod(pos.hashCode(), interval)` 错峰（先例 `:155-161`）；`setRemoved()` 里 `OutpostCoreIndex.remove(...)` + `FarmlandGuard.turnOwnedToDirt(server, this)` |
| 读哪些 config 键 | `outpost_core.boundaryParticleIntervalTicks`（40）、`outpost_core.farmlandBonus`（16）、`outpost_core.maxPerPlayer`（2，供索引计数） |
| 改哪些既有文件 | `block/OutpostCoreBlock.java`（同批新建）；`blockentity/DeployedCropBlockEntity.java` **不改**（两者是平行的 BE，不共用类）；`event/FarmstormGameEvents.java` **不改**（核心的 tick 走方块自己的 ticker，不进全局 tick） |

**为什么不用 `DeployedCropBlockEntity` 扩一个 kind**：那会迫使 `DeployedKind` +1、`CropProfile` +1 条档案，直接撞破本版「注册面 +3、档案仍 22」两条边界（§2.1 注 2）。

### 3.3 FIFO 索引 `trap/TrapFifoIndex.java`（新）

| 项 | 内容 |
|---|---|
| 职责 | 「玩家 → 该玩家名下 TRAP 部署物的坐标」，按 `placedGameTime` 升序；回答三件事：这名玩家现在有几个陷阱、最早的是哪一个、某个坐标是否还在账上。落盘到服务端 `SavedData` |
| 关键方法 | `static TrapFifoIndex get(MinecraftServer)`：`server.overworld().getDataStorage().computeIfAbsent(...)` [待验证 ⑤，RT-26]；`void record(UUID owner, BlockPos pos, long placedGameTime)`；`int count(UUID owner)`（**只数 TRAP**，判据是 `CropProfile.byKind(kind).has(CropCategory.TRAP)`）；`Optional<BlockPos> oldest(UUID owner)`；`void remove(UUID owner, BlockPos pos)`；`List<BlockPos> owned(UUID owner)`；加载后**惰性校验**——坐标处的方块必须仍是该玩家名下的 TRAP 部署物，否则丢弃该条目（D-115 补充） |
| 读哪些 config 键 | `trap.maxTrapsPerPlayer`（16） |
| 改哪些既有文件 | `blockentity/DeployedCropBlockEntity.java`（`setOwner` 里 `record`、`setRemoved` 里 `remove`）；`block/DeployedCropBlock.java`（`getStateForPlacement` 里先挤出、后放行，见 §5.4） |

### 3.4 耕地额度持有者 `farmland/FarmlandLedger.java` 与拦截器 `farmland/FarmlandGuard.java`（新）

| 项 | 内容 |
|---|---|
| 职责（Ledger） | 已开垦耕地的**坐标集合**加计数缓存（D-117 第 4 项）：`Map<UUID, LinkedHashSet<BlockPos>>`，落盘到 `SavedData`；`capacity(玩家)` 求额度（§6.1）；`used(玩家/队伍)` 求已开垦数；一块耕地被挖掉或炸掉时**当场**从集合里删 |
| 职责（Guard） | 三条拦截面：锄地（范围 → 额度）、踩坏保护、核心破坏后的转泥土。判定顺序固定为「范围 → 额度」，两者都是拒绝时**不扣耐久、不产生耕地**（D-116 第 4 项、D-118 第 3 项） |
| 关键方法（Ledger） | `static FarmlandLedger get(MinecraftServer)`（同 §3.3 的 `SavedData` 形态）[待验证 ⑤，RT-26]；`int capacity(ServerLevel, Player)`；`int used(ServerLevel, Player)`；`boolean add(ServerLevel, Player, BlockPos)`；`boolean remove(BlockPos)`；`List<BlockPos> ownedBy(ServerLevel, ServerPlayer)`（供 D-120 第 4 项的转泥土用） |
| 关键方法（Guard） | `static boolean canTill(ServerLevel, Player, BlockPos)` = `inOwnRange(...) && used < capacity`；`static boolean inOwnRange(ServerLevel, Player, BlockPos)` = 目标所在区块与「该玩家或其队友名下的任一核心」的区块切比雪夫距离 ≤ 1；`static boolean protectFromTrample(ServerLevel, BlockPos)`；`static void turnOwnedToDirt(ServerLevel, OutpostCoreBlockEntity)` |
| 读哪些 config 键 | `farmland.limitPerPlayer`（0，base 额度）、`outpost_core.farmlandBonus`（16）、`outpost_core.maxPerPlayer`（2，只用于放置上限） |
| 改哪些既有文件 | `command/FarmstormCommands.java`（写 base 额度）；`block/OutpostCoreBlock.java`、`blockentity/OutpostCoreBlockEntity.java`（归属与释放）；`event/FarmstormGameEvents.java`（踩坏事件订阅 +1 行，见 §6.4） |

### 3.5 `behavior/TrapBehavior.java` 的签名变更（改）——具体形态

**变更是类型放宽，不是新开分支。** 现在（`behavior/TrapBehavior.java:5`、`:18-20`）：

```java
import net.minecraft.world.entity.player.Player;
...
Optional<Player> findTrigger(DeployedCropBlockEntity be);

void trigger(DeployedCropBlockEntity be, Player target);
```

改后（D-114）：

```java
import net.minecraft.world.entity.LivingEntity;
import java.util.Optional;
...
Optional<LivingEntity> findTrigger(DeployedCropBlockEntity be);

void trigger(DeployedCropBlockEntity be, LivingEntity target);
```

五条连带修改，逐条给落点：

| # | 落点 | 改什么 |
|---|---|---|
| 1 | `behavior/TrapBehavior.java:22-25` | 默认 `tick` 加免疫闸与 dust（§5.1 / §5.2 的一处实现） |
| 2 | `BlastCarrotTrap`（`:38`、`:58`） | 两个方法签名换成 `LivingEntity`；方法体内不需要改（爆炸伤害本来就遍历 `LivingEntity` 结算，`explosion/FarmstormExplosion.java:159`） |
| 3 | `FangTrapCactusTrap`（`:33`、`:53`） | 签名换 `LivingEntity`；`trigger` 内既有的 `target instanceof ServerPlayer serverPlayer`（`:66`）保持不变——进度只授给玩家 |
| 4 | `ZangiefCaneTrap`（`:48`、`:61`、`:108`） | 签名换 `LivingEntity`；`nearestCandidate` 的返回类型 `Player` → `LivingEntity`（`:108`）；`trigger` 内既有的 `!(target instanceof ServerPlayer player)`（`:62`）改成两分支：`ServerPlayer` 走相位链，`Monster` 走生物分支（§5.3） |
| 5 | `VibrationBeanTrap`（`:43`、`:66`、`:88`） | 签名换 `LivingEntity`；**`:88` 的 `target.getGameProfile().getName()` 必须换掉**——`LivingEntity` 没有该方法，改用 `target.getName().getString()`（否则编译期就过不去，这是本条最容易漏的一处） |

### 3.6 `block/FarmstormCropBlock.java` 的生长改造（改）

| 项 | 内容 |
|---|---|
| 职责 | 15 个作物方块共用的唯一实现（`block/FarmstormCropBlock.java:29`）从「原版随机刻推进」改成「计划 tick 链推进」，其余（`AGE_3`、`MAX_CUSTOM_AGE = 3`、四个 `VoxelShape`、`getBaseSeedId` 兜底）一律不动 |
| 关键方法 | 新增 `onPlace(state, level, pos, oldState, movedByPiston)`（首次成型排第一段）、`tick(state, serverLevel, pos, random)`（亮度闸 → 推进一级 → 排下一段）、`isRandomlyTicking(state)` 返回 `false`；新增私有 `scheduleNext(ServerLevel, BlockPos, BlockState)` 与 `segmentTicks(BlockPos)`（§4.2 的算式）。**不重写** `randomTick`，**不重写** `performBonemeal`（骨粉走原版 `CropBlock` 的 `setBlock` 抬高 age，重排由 `tick` 末尾那一次 `scheduleNext` 负责，§4.3 第 4 步） |
| 读哪些 config 键 | `growth.minSeconds`（20）、`growth.maxSeconds`（45） |
| 改哪些既有文件 | `registry/FarmstormBlocks.java:88`（删 `.randomTicks()`）；`config/FarmstormConfig.java`（+2 键）；`Farmstorm.java` **不改**（生长不进注册自检） |

### 3.7 改动面的完整清单（19 个条目 = 18 改 + 1 核对不改）

| # | 文件 | 改什么 | 依据 |
|---|---|---|---|
| 1 | `registry/FarmstormBlocks.java` | +1 个 `outpost_core` 注册行；`:88` 删 `.randomTicks()` | D-119、D-111 |
| 2 | `registry/FarmstormBlockEntities.java` | +1 个 `outpost_core` type | D-119 |
| 3 | `registry/FarmstormItems.java` | +1 个 `outpost_core` 的 `BlockItem` | D-119 第 5 项 |
| 4 | `Farmstorm.java` | `logSelfCheck` 的 `demolition == 4` → **2**（`:104`）；`logRegistrySelfCheck` 的 `total == 90` → **93**、`items == 31` → **32**、`blocks == 25` → **26**、`blockEntities == 6` → **7**（`:138-141`） | D-108、§2.4 |
| 5 | `config/FarmstormConfig.java` | +11 键、改 1 个默认值（`horizontalJitter` 0.3 → 0.05） | §8.1 |
| 6 | `util/CropProfile.java` | `MELON_SLICE_BOMB`（`:58-61`）与 `MELON_GRENADE`（`:62-63`）的 `categorySet` 去掉 `DEMOLITION` | D-108 |
| 7 | `entity/thrown/MelonGrenadeProjectile.java` | 删 `SUBMUNITION_SPEED`（`:26`），`:66` 改读 `melon_grenade.submunitionSpeed`；`:25` 的注释删掉 | D-109 |
| 8 | `entity/thrown/PuffedWheatProjectile.java` | `fillHay`（`:99-113`）在抗性底线之上叠白名单 | D-106 |
| 9 | `entity/thrown/CompressedPuffedWheatProjectile.java` | **不改**（只重写 `fillBlock()`，判定继承，`:35-37`） | D-106 第 2 句 |
| 10 | `behavior/TrapBehavior.java` | 签名放宽 + 免疫闸 + dust | §3.5、§5.1、§5.2 |
| 11-14 | `behavior/impl/` 的四个陷阱 | 扫描放宽到 `Monster`；桑吉尔蔗加生物分支 | §5.3 |
| 15 | `block/FarmstormCropBlock.java` | 计划 tick 链 | §4 |
| 16 | `blockentity/DeployedCropBlockEntity.java` | 陷阱名额的「先挤出、后放行」 | §5.4 |
| 17 | `event/FarmstormGameEvents.java` | `register(...)` 里 +1 行 `RegisterCommandsEvent` 订阅、+1 行踩踏保护订阅 | §7.1、§6.4 |
| 18 | `tools/verify_project.py` | §1.4 的 G1 / G2 / G3 三处 | §1.4 |
| 19 | `tools/gen_textures.py` | 朋友的韭 4 张专用分支、压缩麦黄褐化与顶底两张 | §9.3 |

上表 **16 行覆盖 19 个条目**（第 11–14 行合并了四个陷阱实现，计 4 个文件），其中第 9 行是「不改（并写明理由）」——留在表里是因为它必须被逐项核对过，真正改动的是 **18 个**；datagen 的 6 个 Provider（`ModBlockStateProvider` / `ModItemModelProvider` / `ModBlockTagsProvider` / `ModLootTableProvider` / `ModRecipeProvider` / `ModLangProvider`）各 +1 项，算在第 1–3 行的接线里，不重复计数。

---

## 4. 生长时间的确定性推进（技术方案）

### 4.1 随机刻这条路在参数上不可达

D-110 的量化已定：现状最快约 **614 秒**（10 分 14 秒）、全干耕地约 24 分钟、密植干地约 44 分钟；目标上界 **45 秒** 比现状的**最快值**还快一个数量级。原版判据是「每随机刻 `nextInt(25/f + 1) == 0` 才推进一级，且 `getRawBrightness(pos,0) >= 9`」，随机刻频率是每区块 section 每 tick 3 次 / 4096 格；把 `randomTickSpeed` 调到能让 45 秒成熟，会让全服所有原版作物一起疯长（22 号 §6 第 2 条明令禁止）。因此本版改**计划 tick 链**，只作用于 `FarmstormCropBlock`。

### 4.2 三段等长的换算

D-110 第 3 条要求「先随机总刻数 T，再取 `d1 = d2 = floor(T/3)`、`d3 = T − 2×d1`」。这条要求在没有方块实体的前提下（D-111 排除方块实体）必须落成一个**可无状态重算**的形式，否则第三段拿不到 `d`。做法是让 T 取 3 的倍数，且下表的每一个量都**由 `growth.minSeconds` / `growth.maxSeconds` 现算**——134 与 167 是默认配置下的结果，不是写死的常量（写死就等于把两个 config 键降级成装饰品，改它们不改变任何行为）：

| 量 | 值 | 算式 |
|---|---|---|
| 下界折刻 `min` | **400** 刻（默认） | `growth.minSeconds × 20` = 20 × 20 |
| 上界折刻 `max` | **900** 刻（默认） | `growth.maxSeconds × 20` = 45 × 20 |
| 单段下界 `dMin` | **134**（默认） | `dMin = ceil(min ÷ 3)` = ceil(400 ÷ 3) = 134 |
| 单段上界 `dMax` | **300**（默认） | `dMax = max ÷ 3`（整除）= 900 ÷ 3 = 300；**约束 ①**：若算出 `dMax < dMin`，按 `dMax = dMin` 夹取（`span` 因此恒 ≥ 1） |
| 段长取值池 `span` | **167**（默认） | `span = dMax − dMin + 1` = 300 − 134 + 1 |
| 单段刻数 `d` | `dMin + floorMod(pos.hashCode(), span)` ∈ [dMin, dMax]（默认 [134, 300]） | 本文补充：无状态重算，不存盘 |
| 总时长 `T` | `3 × d` ∈ [3·dMin, 3·dMax]（默认 [402, 900] 刻 = **20.1 – 45.0 秒**） | 3 × 134 = 402、3 × 300 = 900 |
| 三段 | 各 `d` 刻，和恒等于 T | `d1 = d2 = d3 = floor(T/3)`，余数 `r = T − 3d = 0` |

两条理由，逐条写下来：

1. **为什么 T 必须是 3 的倍数**：若允许 `r ∈ {1,2}`，第三段就是 `d + r`，而「r 是多少」只有在排第一段的那一刻知道——除了方块实体，没有任何地方能把它带到第三段；D-111 已排除方块实体。取 `r = 0` 让三段恒等长，`d3 = T − 2d1 = d` 仍精确成立（D-110 第 3 条的算式不变），代价是 T 只取 {3·dMin, 3·dMin+3, …, 3·dMax} 这 `span` 个值（默认 167 个），落在 [min, max] 内不缺一格观感。**「T 为 3 的倍数」是本文补充**，它落实的是 D-110 第 3 条而不是改写它。
2. **为什么 `d` 由坐标决定而不是由 `level.random` 抽**：三个段各排一次，每次都要 `d`；用 `level.random` 抽就必须把第一次的结果存下来，又回到「需要一个能存东西的载体」。`BlockPos#hashCode()` 是纯算术（`(y + z*31)*31 + x`），跨存档、跨重启稳定，本仓已有同一写法用于错峰（`blockentity/DeployedCropBlockEntity.java:155-161` 的 `floorMod(pos.hashCode(), interval)`）。同一块地重新种下得到同一个 `d`，这是可复现的确定性，与「确定性推进」的口径同向。**「d 由坐标决定」是本文补充**。

### 4.3 计划 tick 链的完整方案

一次调度的生命周期（六步，顺序不能换）：

1. **排程入口只有一个**：私有方法 `scheduleNext(ServerLevel level, BlockPos pos, BlockState state)`。它先判 `state.getBlock() != this` 与 `state.getValue(AGE_3) >= MAX_CUSTOM_AGE`，两者任一成立就**直接返回**、不排任何 tick；否则 `level.scheduleTick(pos, this, segmentTicks(pos))`——`segmentTicks` 每次现算 `dMin` / `dMax` / `span`，**约束 ②**：`d` 在排程那一刻算定并作为 delay 写进计划 tick，因此改 config 只影响此后新排的段，已种下的作物不受影响（改 `maxSeconds` 不会让它们提前成熟）[待验证 ①，RT-24]。
2. **首次成型在 `onPlace`**：`onPlace(state, level, pos, oldState, movedByPiston)` 里，只有 `oldState.getBlock() != this`（方块从无到有）才调 `scheduleNext`。`level` 不是 `ServerLevel` 时直接返回（客户端不排程，HC-03）。
3. **推进在 `tick`**：`tick(state, serverLevel, pos, random)` 的第一行是 `if (state.getBlock() != this) return;`（方块已被挖掉、计划 tick 自然失效，D-111 第 4 条），第二行判 `age >= MAX_CUSTOM_AGE` 就返回；随后过亮度闸（`serverLevel.getRawBrightness(pos, 0) >= 9`，D-110 第 4 条「亮度条件不动」）；通过则 `serverLevel.setBlock(pos, state.setValue(AGE_3, age + 1), Block.UPDATE_ALL)`（先例 `entity/thrown/PuffedWheatProjectile.java:109` 的 `Block.UPDATE_ALL`），并在推进后调一次 `scheduleNext`（此时 `state` 已变、age 已 +1，`scheduleNext` 会读到新 age）。
4. **骨粉不写专门代码**：原版 `CropBlock` 的 `performBonemeal` 会把 age 抬高并 `setBlock`，`onPlace` 随之触发；由于 `onPlace` 只在 `oldState.getBlock() != this` 时排程，骨粉造成的「同方块换 age」**不会**被 `onPlace` 排一次，而是由第 3 步 `tick` 末尾那一次 `scheduleNext` 负责——即「提前推进后按新的 age 重排」（D-111 第 4 条）。这样排程入口仍然只有一个，不会出现两条链。
5. **亮度不足时不推进、1 秒后重试**：`tick` 里亮度闸不通过时，`level.scheduleTick(pos, this, 20)`，不打日志、不改 age。20 刻是本文补充：它让「暗处不长」这条既有语义原样保留，同时不会把计划链断掉；「20~45 秒」这条承诺的前提是光照 ≥ 9（与原版同一前提），验收按 §11 #3 在光照 15 的田里测。
6. **随区块保存**：计划 tick 由区块自己的待执行队列承载（`block_ticks`），因此区块卸载重载、服务器重启之后继续推进 [待验证 ③，RT-24]。D-111 第 2 条已要求实现阶段实测「存档 → 重启 → 继续推进」；若实测不成立，退回「载入时按方块状态重排」的补丁（`ChunkEvent.Load` 里对 age < 3 的作物调一次 `scheduleNext`），不改 D-111 的口径。

### 4.4 防双源推进的三件套

随机刻与计划 tick 同时推进，20~45 秒就不成立（D-111 第 3 条）。三处一起做，缺一条就有洞：

| # | 动作 | 落点 | 为什么这条不能省 |
|---|---|---|---|
| 1 | **删 `.randomTicks()`** | `registry/FarmstormBlocks.java:88`（`crop(...)` 工厂的 Properties） | 这是全模组与生长相关的唯一一处 Properties 声明；留着它就是给原版随机刻开门 |
| 2 | **不重写 `randomTick`** | `block/FarmstormCropBlock.java`（保持现状，不加方法） | 1.21.1 的 `CropBlock.randomTick` 自带「`nextInt(25/f+1)==0` 才推进」；一旦重写就等于接管随机刻，会与计划 tick 撞车 |
| 3 | **重写 `isRandomlyTicking(state)` 返回 `false`** | `block/FarmstormCropBlock.java` 新增一个方法 | 第 1 条删的是 Properties 上的开关，但原版 `CropBlock` 是否自己重写 `isRandomlyTicking` 取决于 1.21.1 的反编译结果 [待验证 ①，RT-24]；本方一处显式 `return false` 是**本地可控**的判据，不依赖反编译结论 |

第 3 条是本版比 D-111 多走的一步（D-111 只要求删除 `.randomTicks()` 且不重写 `randomTick`），理由写在右列：它把「双源推进是否已消除」从「依赖原版实现细节」变成「本方代码里有一行返回 false」。

### 4.5 与原版判据的关系（一条不改）

| 原版判据 | 本版处置 |
|---|---|
| 亮度门槛 `getRawBrightness(pos, 0) >= 9` | 保留（§4.3 第 5 步） |
| 骨粉行为 | 保留（§4.3 第 4 步，走原版 `performBonemeal`） |
| `AGE_3` 四阶段 / `MAX_CUSTOM_AGE = 3` | 保留（`block/FarmstormCropBlock.java:31`、`:63-70`） |
| 四个 `VoxelShape` | 保留（`:38-43`） |
| 原版作物（小麦 / 胡萝卜 / 马铃薯 / 甜菜 / 瓜茎 / 南瓜茎） | **一条不改**（22 号 §6 第 2 条） |
| 全局 `randomTickSpeed` | **不改**（同上） |

### 4.6 `behavior/TrapBehavior.java:18-24` 的签名变更

见 §3.5——那里给了改前 / 改后两段代码与五处连带修改，此处不重复。要点一句话：**类型从 `Player` 放宽到 `LivingEntity`**，四个实现里唯一会编译不过的是 `VibrationBeanTrap:88` 的 `getGameProfile()`，改成 `getName().getString()`。

---

## 5. 陷阱系统四项改造（技术方案）

### 5.1 触发后 5 秒大类免疫（D-112）——记在哪、闸在哪

| 项 | 方案 |
|---|---|
| 记账容器 | 新类 `trap/TrapImmunity.java`：`static Map<UUID, Long> UNTIL`（值 = 免疫到期的 `gameTime`），配一个 `MinecraftServer` 实例守卫——换服务器实例就整表清空，照 `blockentity/DeployedCropBlockEntity.java:237-244` 的 `ledgerOf` 同形 |
| 闸的位置 | `TrapBehavior#tick` 的**默认实现**（`behavior/TrapBehavior.java:22-25`）。四个陷阱共用它；`ZangiefCaneTrap` 虽然覆盖了 `tick`（`:37-40`），但它第一句是 `TrapBehavior.super.tick(be)`，闸自动生效——这就是「一处写、四处生效」的证据 |
| 判据 | `static boolean passGate(ServerLevel level, LivingEntity target)`：非 `Player` 一律返回 `true`（生物不记免疫，D-112 第 3 条）；是 `Player` 时 `now < UNTIL.getOrDefault(uuid, 0L)` ⇒ 返回 `false`（被闸住），否则**先写入** `now + trap.immunityTicks` 再返回 `true` |
| 调用顺序 | `findTrigger` → `passGate` → `trigger`。被闸住时**不 `consume`、不施效果**（D-112 第 2 条），`findTrigger` 返回空时也不写免疫（没有触发过就不该免疫） |
| 时钟 | `level.getGameTime()`，不写存档（D-112 第 4 条）；重连清零的最坏后果是「同一人 5 秒内能再踩一次」 |
| 同刻多次 | 因为 `passGate` 在 `trigger` 之前写入，同一刻第二个够到同一玩家的陷阱必然被闸住 ⇒「同一刻只触发一次」自动成立，不需要额外的去重键 |
| config | `trap.immunityTicks`（100） |

### 5.2 部署后每 5 秒 dust 粒子（D-113）——由谁发、相位怎么对齐

| 项 | 方案 |
|---|---|
| 谁发 | `TrapBehavior#tick` 的默认实现里，在免疫闸**之前**调用一个 `default void emitDust(DeployedCropBlockEntity be)`。理由：dust 是「这里有个东西」的提示，与有没有人踩无关，因此不能挂在 `trigger` 上 |
| 只为谁发 | 只有实现 `TrapBehavior` 的 4 个 kind（`BLAST_CARROT` / `FANG_TRAP_CACTUS` / `ZANGIEF_CANE` / `VIBRATION_BEAN`）。炮台走 `TurretBehavior`、光环走 `AuraProvider`，是**平行的另外两个接口**，天然不会被发到（D-113 第 4 条） |
| 出粒条件 | `Math.floorMod(now + Math.floorMod(pos.hashCode(), dustInterval), dustInterval) == 0`，`now = level.getGameTime()` |
| 相位为什么不会漏 | `dustInterval = 100` 是 `scanInterval = 10` 的整数倍，且两个相位同源（`phase100 = hash mod 100`、`stagger10 = hash mod 10`，因此 `phase100 mod 10 = stagger10`）。于是「`(now + phase100) % 100 == 0`」成立的那一刻，`serverTick` 的 10 刻门 `(now + stagger10) % 10 == 0` **必然同时成立**（`blockentity/DeployedCropBlockEntity.java:149-152` 的门）——dust 不会因为错峰而被跳过，也不需要第二个调度器 |
| 粒子形态 | 2 个 / 次；类型 `DustParticleOptions(new Vector3f(1.0F, 0.15F, 0.15F), 1.0F)`，与 `combat/WildHunterAura.java:47` 的 `SMOKE_RED` 逐字同值（本仓唯一的 dust 颜色取值，不新造 RGB）；位置 `Vec3.atCenterOf(pos)`，偏移 `(0.2, 0.1, 0.2)`、速度 `0.0`；调用 `level.sendParticles(...)`（先例 `:170-172` 的 8 参形态） |
| 「2 个 / 次」与偏移量的理由 | 22 号 D-113 补充把 2 定为「一眼能看见、又不足以标出精确位置」的最小可观测值；偏移量取 (0.2, 0.1, 0.2) 是**本文补充**，目的是让两点不重合又不离开方块中心 0.3 格内 |
| 包量 | 1 次 `sendParticles` = 1 个原版粒子包 / 陷阱 / 5 秒。10 枚陷阱 = 2 包/秒，不构成负担 |
| 0 自定义网络包 | 走原版 `ClientboundLevelParticlesPacket`（C-6 / D-50；22 号 §6 第 4 条） |

### 5.3 敌对生物可触发（D-114）——生物遍历怎么加

**二分口径照实现走**，不另立判据：

| 步 | 做什么 | 落点 |
|---|---|---|
| 1 | 玩家那一遍原样保留（创造 / 旁观 / 己方的三条豁免一个字不改） | 四个 `findTrigger` 里既有的 `for (ServerPlayer player : level.players())` |
| 2 | **新增第二遍**：`level.getEntitiesOfClass(Monster.class, box)`，`box` 用与玩家那一遍相同的半径包一个 `AABB`。先例 `behavior/impl/PumpkinTurret.java:109`（man瓜 索敌同时锁玩家与 `Monster`） | 胡罗贝 / 刺球 / 桑吉尔蔗 / 震动豆各 +1 段 |
| 3 | 距离判据复用**同一个** metric 与同一个 config 键（切比雪夫 ≤ 1 / 欧氏 ≤ 2.5 / 球形 ≤ 1.5 / 严格小于 4.0），不写第二个数 | 同上 |
| 4 | 己方判定只对玩家：`Monster` 没有队伍归属，**恒按非己方**（D-114 第 4 条）；`be.isAlly(...)` 只接受 `Player`，因此生物那一遍不调它 | 同上 |
| 5 | 效果二分：**通用**（伤害 / 缓慢 / 击退）对所有触发者生效；**玩家专属**（混乱）对生物跳过 | 混乱已在效果端只遍历 `level.players()`（`behavior/impl/VibrationBeanTrap.java:75-83`），**不改**；缓慢已在伤害事件侧过滤 `ServerPlayer`（`behavior/impl/FangTrapFangs.java:57-77`），**不改**；胡罗贝的爆炸伤害本来就遍历 `LivingEntity`（`explosion/FarmstormExplosion.java:159`），**不改** |
| 6 | 桑吉尔蔗加**生物分支** | `behavior/impl/ZangiefCaneTrap.java:61-81`：`target instanceof ServerPlayer` 走既有的 `ZangiefPiledriverController.begin(...)` 相位链；否则只结算通用段——`target.hurt(FarmstormDamageTypes.zangiefSlam(level, deployer), (float) zangief_cane.slamDamage)`（默认 7.5）加一次击退 `target.knockback(0.4, -dx, -dz)`（强度 0.4 取自本仓 `combat/WildHunterCombat.java:27` 的 `KNOCKBACK_STRENGTH`，不新造数值），**不进相位链** |

**一条术语约定（作者订正，本文与 22 号统一到此口径）**：桑吉尔蔗的相位 1 是**伺服向上抬固定高度**（关重力 + 每刻「剩余高度 ÷ 剩余刻数」，上限 `MAX_RISE_SPEED = 4.0`；`riseHeightBlocks` 默认 16、实测约 15 格，`riseTicks` 默认 15 刻），相位 2 是伺服下坠 + 每刻 yaw +72° 自转 + 落地结算固定 7.5 点。效果类 `ZangiefPiledriverEffect` 只是相位通道（`amplifier` 当相位号），不加属性、不造伤害、不产粒子；`MobEffects.LEVITATION` 在 `behavior/ZangiefPiledriverController.java:390`、`:421` **只被移除**（旧存档兜底），不是机制。相位常量名叫 `PHASE_LEVITATING` 是历史漏名。**因此本节不写「漂浮」，也不把相位 1 与相位 2 混为一谈。**

### 5.4 每玩家 16 个陷阱与 FIFO 挤掉（D-115）——怎么挤

| 步 | 做什么 | 为什么必须这样 |
|---|---|---|
| 1 | 名额只数 **TRAP**：`CropProfile.byKind(kind).has(CropCategory.TRAP)` | 炮台（`PUMPKIN_TURRET` / `AWAKENED_*` / `POINT_DEFENSE_FUNGUS`）与建造类不占名额（D-115 第 2 项）；22 条档案里带 `TRAP` 的只有 4 条（§1.3） |
| 2 | 挤出发生在**写入新方块之前**：`block/DeployedCropBlock.java:163-184` 的 `getStateForPlacement` 里，`canDeploy(...)` 之后、`return defaultBlockState()` 之前调 `TrapFifoIndex.enforce(server, player, kind)` | 22 号 D-115 补充：挤掉动作若晚于写入，第 17 个会先占位、把刚放下的挤掉。`getStateForPlacement` 是唯一一处「判定在物品扣除之前」的位置（`:178-181` 的注释已写明这条性质） |
| 3 | `enforce` 的语义：`count(owner) >= trap.maxTrapsPerPlayer` 时，取 `oldest(owner)` 对该坐标调 `DeployedCropBlockEntity#consume()`（`removeBlock(pos,false)` + 破坏粒子，**无掉落**、不退还物品，D-115 第 3 项），直到 `count < 16`；一轮最多挤 1 个 | 复用既有的 `consume` 路径（`blockentity/DeployedCropBlockEntity.java:322-330`），不新写一套移除 |
| 4 | 新条目在 `setOwner` 里 `record(owner, pos, placedGameTime)`；`setRemoved` 里 `remove(owner, pos)` | 与既有限流账本的加减项位置一一对应（`:267-279`、`:333-346`），两本账同进同出 |
| 5 | 三个安全阀 **5 / 8 / 32 一个不动** | 它们是服务器保护底线（HC-13），16 是玩法名额（D-115 第 5 项） |
| 6 | 落盘格式：`UUID → 按 placedGameTime 升序的坐标列表`；加载时惰性校验（坐标处必须仍是该玩家名下的 TRAP 部署物） | D-115 补充的两条理由：能在重启后重建出与首次部署一致的顺序；坏条目会自己过期，不需要对账任务 |

### 5.5 四项的挂载点总表

| 项 | 挂载点 | 记账位置 | 落盘 | config 键 | 验收 |
|---|---|---|---|---|---|
| 5 秒大类免疫 | `TrapBehavior#tick` 默认实现（免疫闸） | `trap/TrapImmunity.UNTIL`（`Map<UUID, Long>`，内存 + 服务器实例守卫） | 不落盘 | `trap.immunityTicks` 100 | §11 #6 |
| dust 每 5 秒 | `TrapBehavior#emitDust`（默认实现，`tick` 里先调） | 无（相位由 `pos.hashCode()` 决定） | 不落盘 | `trap.dustIntervalTicks` 100 | §11 #7 |
| 生物可触发 | 四个 `findTrigger` 的第二遍 `getEntitiesOfClass(Monster.class, box)`；桑吉尔蔗的 `trigger` 两分支 | 无（一次性结算） | 不落盘 | 无新键（沿用四个既有半径键） | §11 #8 |
| FIFO 16 | `DeployedCropBlock#getStateForPlacement`（挤）+ `setOwner` / `setRemoved`（记账） | `trap/TrapFifoIndex`（`SavedData`） | **落盘** | `trap.maxTrapsPerPlayer` 16 | §11 #9 |

---

## 6. 耕地额度与据点核心（技术方案）

### 6.1 额度模型

```
base            = 运行期覆盖值（默认取 farmland.limitPerPlayer = 0）
capacity(玩家)  = base + 16 × （该玩家名下已放置的核心数）
capacity(队伍)  = Σ over 队伍成员 m of [ base + 16 × （m 名下已放置的核心数） ]
used(玩家/队伍) = 账本里该口径下的坐标集合大小（同一坐标只算一次）
```

| 情形 | 算式 | 额度 | 说明 |
|---|---|---|---|
| 单人、无队伍、无核心、config 默认 | 0 + 16×0 | **0** | 开局谁也耕不了地（D-116 第 2 项） |
| 单人、1 个核心 | 0 + 16×1 | **16** | D-120 第 1 项 |
| 队伍 2 人各 1 核心 | (0+16) + (0+16) | **32** | 队伍内求和（D-117 第 1 项），不是每人各算 16 |
| 执行 `/farmstorm farmlandlimit 8` 后，队伍 2 人各 1 核心 | (8+16) + (8+16) | **48** | 指令只改 `base`（§7.6） |
| 无队伍 | base + 16 × 自己的核心数 | — | D-117 第 3 项 |

三条硬口径：**只限制新开垦、不追溯**（D-116 第 3 项）；**记账用坐标集合**（同一块耕地在两个核心的并集范围里只算一块，D-118 第 4 项 / D-117 第 4 项）；**核心被破坏时立即释放 +16**（D-122 补充）。

### 6.2 3×3 判定

```
核心区块   cx = corePos.getX() >> 4,  cz = corePos.getZ() >> 4
目标区块   tx = pos.getX()     >> 4,  tz = pos.getZ()     >> 4
在范围内 ⇔ max(|tx − cx|, |tz − cz|) ≤ 1
```

- 3 × 3 区块 = 9 个区块 = 48 × 48 格（16 × 3 = 48），与 D-118、D-120 第 2 项、D-121 的边界粒子共用同一组数字。
- **多个核心取并集**：对「该玩家本人 + 其队友」名下的每个核心各判一次，任一命中即在范围内（D-118 补充）。
- 3 × 3 **不做成 config 键**（D-118 补充、22 号 §6 第 9 条）。

### 6.3 锄地拦截（顺序固定：范围 → 额度）

| 步 | 判定 | 被拒时的效果 |
|---|---|---|
| 1 | 动作是否「土 / 草方块 → 耕地」 | — |
| 2 | 目标位置是否在**己方**核心的 3×3 内（§6.2） | 不产生耕地、**不扣耐久**、发提示 `message.farmstorm.farmland.no_core` |
| 3 | `used < capacity`（§6.1） | 不产生耕地、**不扣耐久**、发提示 `message.farmstorm.farmland.quota_full`（带 `%s/%s`） |
| 4 | 通过 ⇒ 允许原版逻辑执行，并在同一 tick 把该坐标写进账本 | — |

拦截点选型：[待验证 ⑥，RT-28]。首要方案是 NeoForge 的 `PlayerInteractEvent.RightClickBlock`（在物品 `useOn` 之前触发、可取消），取消时同时 `setUseBlock(Result.DENY)`；本仓 0 处先例，因此先写最小用例确认「取消之后既不产生耕地也不扣耐久」。兜底见 RT-28。

账本写入口径：第 4 步通过后**立即**记录坐标，不等下一次读取；若原版逻辑因别的原因最终没产生耕地，惰性校验（坐标处必须仍是 `minecraft:farmland`）会在下一次读取时把这个条目丢掉。

### 6.4 踩坏保护（挂哪）

| 项 | 内容 |
|---|---|
| 保护对象 | 核心 3×3 内的**己方**耕地（D-120 第 3 项）；只拦「踩踏变泥土」，**不拦失水退化**（D-120 补充） |
| 挂载点 | `event/FarmstormGameEvents.register(...)` 里 +1 行订阅；处理器判断「被踩的坐标是否在己方核心范围内且是己方耕地」，是则取消 [待验证 ⑦，RT-28] |
| 为什么不做成方块属性 | 耕地方块是原版 `minecraft:farmland`，本模组没有它的类、也不能替换它（替换等于把全世界的耕地换成自研方块，撞破 §2 的注册面）；在无 Mixin 红线（D-40）下唯一可行的位置是事件层 |
| 兜底方案 | 若 21.1.213 没有可取消的踩踏事件，退化为「40 刻对账 + 恢复」：`OutpostCoreBlockEntity` 每 40 刻（与边界粒子同一轮）检查账本里位于范围内的坐标，发现方块已变成 `minecraft:dirt` 就 `setBlock` 回 `minecraft:farmland`（`moisture` 取默认 0）。代价写进验收记录：踩坏的那一瞬会闪一帧泥土 |

### 6.5 破坏核心 → 范围内己方耕地转泥土

| 项 | 内容 |
|---|---|
| 遍历范围 | **只遍历账本里该归属者名下的坐标**，不做全范围扫描（D-120 补充：扫描会把「范围内但不在账上的耕地」一起铲掉，与第 4 项的「己方」限定冲突） |
| 触发点 | `OutpostCoreBlockEntity#setRemoved()`（先例 `blockentity/DeployedCropBlockEntity.java:333-346`：`setRemoved` 是「方块被移除」的可靠钩子，区块卸载也会调，因此用一次性标志位保证只跑一次） |
| 判定 | 对每个账上坐标：① 仍在该核心的 3×3 内；② 方块仍是 `minecraft:farmland`；③ 该坐标的账本归属者是核心归属者本人或其队友——三条同时成立才 `server.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL)` 并从账本删除 |
| 顺序 | 先算完「要转哪些坐标」再动世界，避免边遍历边改账本 |
| 不留残余 | 转完之后该核心从 `OutpostCoreIndex` 删除，`+16` 当场失效（D-122 补充） |

### 6.6 边界粒子与包量估算（D-121）

| 项 | 值 / 方案 |
|---|---|
| 几何 | 48 × 48 格的地面矩形**边界**（不是填充面）。周长 = 4 × 48 = 192 格；每 4 格一点 ⇒ **48 点 / 圈** |
| 发包方式 | 每条边一次 `level.sendParticles(...)`：中心取该边中点、`count = 12`、沿边方向的 `dx`（或 `dz`）取边长一半、另一水平轴与 `dy` 取 0 ⇒ 12 点随机落在该边上，平均间距 48 ÷ 12 = **4 格**。四条边 = **4 个包 / 圈**（**本文补充**：一次 `sendParticles` 只出一个包，逐点发就是 48 个包；按边发把它压到 4 个而不改变「每 4 格一点」的观感） |
| 频率 | 每 40 刻（2 秒）一轮（`outpost_core.boundaryParticleIntervalTicks`） |
| 距离衰减 | 分三档（**本文补充**）：玩家到核心中心 ≤ 48 格 ⇒ 48 点（4 包）；48 < d ≤ 64 格（RT-30 的「余量」，取一个区块宽 16 格）⇒ 24 点（4 包，每边 `count = 6`）；d > 64 ⇒ 不发 |
| 可见性 | 只发给「己方玩家且在上表范围内」（D-121 第 1、4 项）；不区分创造 / 生存 |
| 粒子类型 | `ParticleTypes.END_ROD`——原版常量、无重力、不需要颜色参数，因此本版不产生任何 RGB 常量，也不与 `23-DESIGN-0.1.0.md` 的配色表冲突 |
| 包量（单玩家，范围内） | 4 包 / 2 秒 = **2 包/秒**，48 个粒子 / 2 秒 = 24 个/秒 |
| 包量（10 个己方玩家都在范围内） | **20 包/秒**、240 个粒子/秒 |
| 与「逐格发」的对照 | 48 × 48 = 2304 格 / 2 秒 = 1152 个粒子/秒/玩家 ⇒ 抽点后是它的 **1/48**（1152 ÷ 24 = 48） |
| 0 自定义网络包 | 若需要「只发给满足条件的玩家」，用原版 `ClientboundLevelParticlesPacket` + `ServerPlayer#connection.send`（先例 `combat/WildHunterAura.java:211` 的 `ClientboundSoundEntityPacket` 同形）[待验证 ⑧，RT-30]；退路是 `ServerLevel#sendParticles`（广播给半径内所有人，仍是原版包） |

---

## 7. 指令注册与权限

### 7.1 注册入口

| 项 | 方案 |
|---|---|
| 事件 | `RegisterCommandsEvent`，在 `event/FarmstormGameEvents.register(IEventBus gameBus)`（`:46-52`）里 +1 行 `gameBus.addListener(FarmstormGameEvents::onRegisterCommands)` [待验证 ④，RT-25] |
| 处理体 | `onRegisterCommands(RegisterCommandsEvent event)` → `FarmstormCommands.register(event.getDispatcher())` |
| 承载类 | 新类 `command/FarmstormCommands.java`（全仓 `command/` 目录当前不存在，本版新建） |
| 注册面 | **+0 个 registry 对象**：Brigadier 指令树是运行期注册，不进 §2 的任何一类（§2.3 单列） |
| 是否服务端 only | 是。指令只在服务端注册，客户端不新增任何代码（C-6 / HC-03 同向） |

### 7.2 指令树

```
/farmstorm
    farmlandlimit <value>        权限等级 2（与 /gamerule 同档）
```

| 项 | 值 | 说明 |
|---|---|---|
| 根字面量 | `farmstorm` | 与模组 id 同名，避免与别家 `/farm` 撞名 |
| 子指令 | `farmlandlimit` | D-116 第 1 项给的字面名，逐字照抄（不是 `farmland_limit`、不是 `farmlandLimit`） |
| 参数 | `IntegerArgumentType.integer(0, 4096)` | 下界 0 与 config 的 `farmland.limitPerPlayer` 下界一致；上界与 §8.1 的范围列一致 |
| 权限 | `.requires(src -> src.hasPermission(2))` | 2 = 与 `/gamerule` 同档（本文补充：额度是全局玩法规则，玩家自助改它等于没有规则） |
| 执行 | 写 `FarmlandLedger.setBase(value)`，然后 `src.sendSuccess(...)` 回一条带新值的反馈 | 只改 base，**不改已开垦数**（D-117 末尾） |
| 返回 | 成功 1 | Brigadier 的 `Command.SINGLE_SUCCESS` |

### 7.3 反馈文案要点

| 键 | 触发 | 要点 |
|---|---|---|
| `command.farmstorm.farmlandlimit.set` | 指令执行成功 | 必须回显**新额度**与「本进程内有效、重启回 config 默认」两件事；不带玩家名（这是全局值） |
| `message.farmstorm.farmland.no_core` | 锄地被范围拒绝 | 一句话说清「附近没有己方据点核心」，不报坐标、不报距离（避免变成探针） |
| `message.farmstorm.farmland.quota_full` | 锄地被额度拒绝 | 必须带 `%s/%s`（已用/上限），让玩家自己判断该拆核心还是挖旧田 |

三条文案都用 `Component.translatable(...)`，语言键进 §9.4；**不给拒绝文案配音效**（原版锄地本身有音，再加一层会盖住）。

### 7.4 这不是 gamerule（明确写死）

**1.21.1 在没有 Mixin 的前提下无法注册 `GameRule`。** 三条依据：

1. 原版 `GameRules.register` 是 `private static`（施工事实卡 §1.8 的反编译行号 `GameRules.java:207`），NeoForge 21.1.213 源码里没有任何自定义 gamerule 的注册入口；
2. 本模组从 0.0.1 起维持「无 Mixin」红线（D-40），本版不为一个规则引入字节码注入（22 号 §6 第 1 条）；
3. 全仓对 `GameRule` 的唯一读点是 `explosion/FarmstormExplosion.java:109` 读 `RULE_MOBGRIEFING`——那是**读原版规则**，不是注册自己的规则，本版不动它。

因此耕地规则的载体只有两件：**服务端 config `farmland.limitPerPlayer`（默认 0）** + **`/farmstorm farmlandlimit <n>` 子指令**。本文件与任何交付物都不把这套叫「gamerule」（22 号 §0 的「耕地规则的叫法」一行）。

### 7.5 作用域与持久化

| 项 | 结论 |
|---|---|
| 指令值的作用域 | 当前服务端进程 |
| 重启后 | 回到 config 默认值（D-116 第 5 项） |
| 落盘 | **不写第二份持久数据**。config 已经是唯一持久口径，再存一份立刻出现「两处都能定义额度、且有先后」的双源 |
| `/reload` | `FarmstormConfig.onReloading` 会 `bake()` 派生快照；base 额度**不**在 `bake()` 里重置（指令值优先于 config，直到下一次重启） |

### 7.6 指令与额度的关系

指令只改 §6.1 的 `base`；`+16 × 核心数` 与 `used` 都不受它影响。因此「把额度调小」不会让已开垦的耕地消失或退化（D-116 第 3 项），只会让下一次开垦被拒。

---

## 8. config 变更全量清单

### 8.1 逐项清单（**12 行**）

列集与 18 号 §8.1 相同：组.键 / 类型 / 默认 / 范围 / 用途 / D 编号。**本表 12 行 = 新增 11 行 + 改默认值 1 行**；行数不等于新键数（第 10 行改的是既有键的默认值，键本身在 0.0.4 就存在）。**「范围」列全部是本文补充**——施工事实卡只给类型与默认值，范围的取法与同组既有键的量级对齐。

| # | 组.键 | 类型 | 默认 | 范围 | 用途 | 新 / 改 | D |
|---|---|---|---|---|---|---|---|
| 1 | `growth.minSeconds` | int | **20** | 1 – 600 秒 | 成熟总时长下界 | 新 | D-110 |
| 2 | `growth.maxSeconds` | int | **45** | 1 – 600 秒 | 成熟总时长上界 | 新 | D-110 |
| 3 | `trap.immunityTicks` | int | **100** | 0 – 1200 刻 | 触发后陷阱大类免疫的刻数（5 秒） | 新 | D-112 |
| 4 | `trap.dustIntervalTicks` | int | **100** | 20 – 1200 刻 | 陷阱 dust 粒子的间隔（5 秒） | 新 | D-113 |
| 5 | `trap.maxTrapsPerPlayer` | int | **16** | 1 – 128 个 | 每玩家陷阱名额（FIFO） | 新 | D-115 |
| 6 | `farmland.limitPerPlayer` | int | **0** | 0 – 4096 块 | 耕地额度的服务端默认值 | 新 | D-116 |
| 7 | `outpost_core.farmlandBonus` | int | **16** | 0 – 1024 块 | 每个核心增加的耕地上限 | 新 | D-120 |
| 8 | `outpost_core.maxPerPlayer` | int | **2** | 1 – 64 个 | 每玩家可放置的核心数 | 新 | D-122 |
| 9 | `outpost_core.boundaryParticleIntervalTicks` | int | **40** | 20 – 1200 刻 | 边界粒子的刷新间隔（2 秒） | 新 | D-121 |
| 10 | `melon_grenade.horizontalJitter` | double | **0.05**（原 0.3） | 0.0 – 8.0 格 | 瓜片子落点抖动上界 | **改** | D-109 |
| 11 | `melon_grenade.submunitionSpeed` | double | **0.1** | 0.0 – 4.0 格/刻 | 瓜片子径向初速（原硬编码 0.3） | 新 | D-109 |
| 12 | `compressed.replaceOnlyAirAndFluid` | boolean | **true** | `true` / `false` | 膨胀麦落地方块只替换空气与流体 | 新 | D-106 |

**分项小计**：`growth` **2** + `trap` **3** + `farmland` **1** + `outpost_core` **3** + `melon_grenade` **1**（第 11 行；第 10 行是改默认值，键不增）+ `compressed` **1** = 新增 **11**；改默认值 **1**（第 10 行）；表行数 **12**。

**逐行核对结论**（类型 / 范围 / 默认三列都对着施工事实卡 §3.4 与源码核对过）：

- 第 1–9、11、12 行的类型与默认值与施工事实卡 §3.4 逐字相同（int / int / int / int / int / int / int / int / int / double / boolean；20 / 45 / 100 / 100 / 16 / 0 / 16 / 2 / 40 / 0.1 / true）。
- 第 10 行的**现值**实测是 `defineInRange("horizontalJitter", 0.3, 0.0, 8.0)`（`config/FarmstormConfig.java:389-391`）：默认值从 0.3 改 0.05，`min` / `max` 与 `translation` 键逐字保留。
- 第 1 行与第 2 行必须互相约束：代码里取 `Math.min(minSeconds, maxSeconds)` 作下界、`Math.max(...)` 作上界，因此两个值填反不会让 `T` 算出负数或空区间（实现阶段的防御，不改口径）。
- 本版**没有**为 3 × 3 范围、48 × 48 边界、边界粒度、桑吉尔蔗的生物结算新增任何键（22 号 §6 第 9 条）。

### 8.2 TOML 片段（5 个新分组）

5 个新分组追加在 `vibration_bean` 之后（现文件末尾），组内键序按 §8.1 的行序：

```toml
[growth]
	# 成熟总时长下界（秒），范围 1 ~ 600
	minSeconds = 20
	# 成熟总时长上界（秒），范围 1 ~ 600
	maxSeconds = 45

[trap]
	# 触发后陷阱大类免疫的刻数，100 刻 = 5 秒；范围 0 ~ 1200
	immunityTicks = 100
	# 陷阱 dust 粒子的间隔（刻），100 刻 = 5 秒；范围 20 ~ 1200
	dustIntervalTicks = 100
	# 每玩家陷阱名额，第 17 个挤掉最早的一个；范围 1 ~ 128
	maxTrapsPerPlayer = 16

[farmland]
	# 耕地额度的服务端默认值，0 = 开局谁也耕不了地；范围 0 ~ 4096
	limitPerPlayer = 0

[outpost_core]
	# 每个据点核心增加的耕地上限；范围 0 ~ 1024
	farmlandBonus = 16
	# 每玩家可放置的核心数；范围 1 ~ 64
	maxPerPlayer = 2
	# 边界粒子刷新间隔（刻），40 刻 = 2 秒；范围 20 ~ 1200
	boundaryParticleIntervalTicks = 40

[compressed]
	# 落地方块只替换空气与流体；false = 回到 0.0.4 的「只跳过抗性 ≥ 3.6e6」
	replaceOnlyAirAndFluid = true
```

`melon_grenade` 那一组不新建分组：第 11 行在既有 5 键之后追加 1 个新键，第 10 行改既有键的默认值实参（`config/FarmstormConfig.java:382-398`）。

### 8.3 键数与语言键的相等性

| 项 | 0.0.4 | 增量 | v0.1.0 |
|---|---|---|---|
| config 键（`define*` 调用数） | 155 | +11 | **166** |
| `farmstorm.configuration.*` 语言键 | 155 | +11 | **166** |
| 顶层分组数（`b.push("...")` 的顶层调用） | 30 | +5 | **35** |

三者恒相等（后两项各自由前一项决定）：每个键都由 `FarmstormConfig.tr(path)`（`"farmstorm.configuration." + path`，`:867-869`）生成一个语言键，`ModLangProvider.config(path)` 一一对应写一条；每个新键都落在 5 个新分组里，因此分组数 30 → 35。

验算式：`155 + 11 = 166`；`30 + 5 = 35`；config 与 configuration 语言键两列同时成立。

### 8.4 明确不进 config 的常量（逐条给理由）

| 常量 | 值 | 理由 |
|---|---|---|
| 3 × 3 核心范围 | 9 个区块 / 48 × 48 格 | 作者没给这个旋钮，加一个键等于发明需求（D-118 补充、22 号 §6 第 9 条） |
| 边界粒子抽点间隔 | 每 4 格一点、单圈 48 点 | 观感参数，与 48 × 48 同一组数字；由 23 号对齐而非服主调（D-121 补充） |
| 边界粒子的可见余量 | 16 格（一个区块宽） | RT-30 只说「按距离衰减」，本文取一个区块宽作余量；它是性能参数，不是玩法参数 |
| 陷阱名额之外的安全阀 | `DEPLOY_COOLDOWN_TICKS = 5` / `MAX_DEPLOYED_PER_CHUNK = 8` / `MAX_DEPLOYED_PER_PLAYER = 32` | 服务器保护底线（HC-13），不得挪进 config（`blockentity/DeployedCropBlockEntity.java:47-50` 的注释已写死） |
| 耕地额度指令的权限等级 | 2 | 权限是运维口径，不是玩法数值 |
| 计划 tick 链的亮度重试间隔 | 20 刻 | 实现细节（「暗处不长」的重试节奏），与 D-113 的 5 秒无关 |
| 生长段长的取值池 | `span = dMax − dMin + 1`（默认 167） | 派生量：由 `growth.minSeconds` / `growth.maxSeconds` 每次现算（§4.2），**不单独进 config**；写死它就等于让那两个键失去作用 |
| `HOTBAR_WIDTH` | 182 | 声明后未使用的常量，本版不动（D-103 补充、22 号 §6 第 10 条） |

---

## 9. 资源与数据文件清单

口径：只列 v0.1.0 **新增或需要就地替换 / 追加条目**的文件，不列 0.0.4 既有的 200 余个。三张表（PNG / JSON / 语言键）各自给分类小计；「追加条目」的既有文件单列，不计入新增文件数。

### 9.1 PNG（**11 行**：新增 5 + 就地替换 6）

| # | 文件 | 尺寸 | 来源 / 处置 | 依据 |
|---|---|---|---|---|
| 1 | `assets/farmstorm/textures/block/outpost_core_top.png` | 32 × 32 | 新素材 `outpost_core_all_textures` 的 `32x32/a_runic` 里 `_top`（2,745 B），原样入库 | D-123 第 1 项 |
| 2 | `assets/farmstorm/textures/block/outpost_core_side.png` | 32 × 32 | 同目录 `_side`（2,534 B），原样入库 | D-123 第 1 项 |
| 3 | `assets/farmstorm/textures/block/outpost_core_bottom.png` | 32 × 32 | 同目录 `_bottom`（2,513 B），原样入库 | D-123 第 1 项 |
| 4 | `assets/farmstorm/textures/block/compressed_hay_block_top.png` | 16 × 16 | 新增顶面：由现有 `compressed_hay_block.png` 的黄褐底稿按「束口」重绘，色调与侧面同族 | D-107 第 3 项 |
| 5 | `assets/farmstorm/textures/block/compressed_hay_block_bottom.png` | 16 × 16 | 新增底面：与顶面同规格、去掉束口纹 | D-107 第 3 项 |
| 6 | `assets/farmstorm/textures/block/friend_chives_stage0.png` | 16 × 16 | **就地替换**：洋红 `#D81E8A` 占位 → 自绘第 0 阶段 | D-104 |
| 7 | `assets/farmstorm/textures/block/friend_chives_stage1.png` | 16 × 16 | **就地替换**：第 1 阶段（比第 0 阶段高一格叶片） | D-104 |
| 8 | `assets/farmstorm/textures/block/friend_chives_stage2.png` | 16 × 16 | **就地替换**：第 2 阶段 | D-104 |
| 9 | `assets/farmstorm/textures/block/friend_chives_stage3.png` | 16 × 16 | **就地替换**：第 3 阶段（成熟株高） | D-104 |
| 10 | `assets/farmstorm/textures/item/wild_apple.png` | 16 × 16 | **就地替换**：桌面素材 1,294 × 1,216 RGBA（1,113,442 B）重采样到 16 网格；模型 `layer0` 不动 | D-105 |
| 11 | `assets/farmstorm/textures/block/compressed_hay_block.png` | 16 × 16 | **就地替换**：全灰阶（9 色）→ 黄褐同规格 | D-107 第 3 项 |

**分类小计**：新增 **5**（据点核心 3 + 压缩麦顶底 2）+ 就地替换 **6**（朋友的韭 4 + 狂野苹果 1 + 压缩麦侧面 1）= **11**。

**不新增 PNG 的两处（必须写明，否则会有人去补图）**：据点核心**不需要**单独的物品图标——物品模型直接以方块模型为父（D-123 第 2 项），因此 `textures/item/` 下**不出现** `outpost_core.png`；压缩膨胀麦的物品是 3D 模型，**不新增**第二张物品贴图（D-107 第 1 项）。

### 9.2 JSON（**7 行**：新增 5 + 就地替换 2）

| # | 文件（`src/generated/resources/` 下） | 性质 |
|---|---|---|
| 1 | `assets/farmstorm/blockstates/outpost_core.json` | 新增：单变体，指向 `farmstorm:block/outpost_core` |
| 2 | `assets/farmstorm/models/block/outpost_core.json` | 新增：父 `minecraft:block/cube_bottom_top`，`textures` 三键 `top` / `side` / `bottom` 指向 §9.1 的 #1–#3 |
| 3 | `assets/farmstorm/models/item/outpost_core.json` | 新增：父 `farmstorm:block/outpost_core`（3D 物品，不写 `layer0`） |
| 4 | `data/farmstorm/recipe/outpost_core.json` | 新增：原版 `minecraft:crafting_shaped`，3 × 3 四角 `stone` / 正中 `diamond` / 其余四格 `redstone`，产出 1 |
| 5 | `data/farmstorm/loot_table/blocks/outpost_core.json` | 新增：掉自身（`LootItem.lootTableItem(block)`，该方块**有** `BlockItem`，与 `vibration_bean` 那种无 BlockItem 的情形不同） |
| 6 | `assets/farmstorm/models/block/compressed_hay_block.json` | **就地替换**：`minecraft:block/cube_all` → `minecraft:block/cube_bottom_top`，`textures` 的 `side` 指 `compressed_hay_block`、`top` / `bottom` 指 §9.1 的 #4 / #5 |
| 7 | `assets/farmstorm/models/item/compressed_puffed_wheat.json` | **就地替换**：`item/generated` + `layer0` → 父 `farmstorm:block/compressed_hay_block`（3D 小方块） |

**分类小计**：blockstate **1** + block 模型 **2**（#2 新增、#6 替换）+ item 模型 **2**（#3 新增、#7 替换）+ recipe **1** + loot_table **1** = **7**；其中新增 **5**、就地替换 **2**。

**既有文件追加 3 处**（不算新增文件，但漏了就功能不全）：

| # | 文件 | 追加什么 |
|---|---|---|
| A1 | `data/minecraft/tags/block/mineable/pickaxe.json` | +1 项 `farmstorm:outpost_core`；**不进**三个 `needs_*_tool`（D-119 第 2 项） |
| A2 | `assets/farmstorm/lang/zh_cn.json` | +16 键（§9.4） |
| A3 | `assets/farmstorm/lang/en_us.json` | +16 键（键集与 A2 逐字相同） |

**与 22 号 §3.3 的差异（必须登记）**：22 号把 JSON 新增记为 4 个（blockstate / block 模型 / item 模型 / 配方），**少了第 5 行的战利品表**。D-119 第 5 项明写「挖掘掉落走方块自身的战利品表，产出该方块本身」，没有 `loot_table/blocks/outpost_core.json` 这条就落不了地（`ModLootTableProvider` 当前为 25 个方块各建一张，见 §1.3）。本文按 **5 个新增** 记，22 号那一格需要一个增量订正——它不是新需求，是实现 D-119 必需的文件。

### 9.3 生成器改动（`tools/gen_textures.py`，3 处）

| # | 位置 | 改什么 | 为什么必须改生成器 |
|---|---|---|---|
| 1 | `:659-666` 的 `for index in range(ADDITION_PLACEHOLDER_STAGES)` 分支 | 这 4 张不再走 `make_placeholder()`（`:512-514` 的纯色填充，色值 `PLACEHOLDER_COLOR = (0xD8,0x1E,0x8A,0xFF)`，`:64`），改走按阶段绘制的专用分支：给 `Spec.drawer` 填一个键（`drawer` 字段已存在，`:148-149`），在 `ITEM_DRAWERS`（`:632-634`）之外新增 `STAGE_DRAWERS`，按 `index` 决定苗高与叶片数 | 只替换 PNG 不动生成器，下一次重跑就被覆盖回洋红（D-104 的唯一理由） |
| 2 | `:284-286` 的 `("compressed_hay_block", ...)` 项 | 第 4 个元素（`monochrome`）由 `True` 改为 `False`，并加 `tint` 成黄褐 | 现在这一项是 `monochrome=True`（走 `convert("L")`），重跑会把黄褐覆盖回灰阶 |
| 3 | `BLOCK_SPECS`（`:275-291` 那段元组的同处） | 追加 `compressed_hay_block_top` / `compressed_hay_block_bottom` 两项（16 × 16，来源同 `hay_block_side`，同样 `tint`） | 顶 / 底两张同属生成物，不写进生成器就与 §9.1 #4 / #5 不一致 |

**专用分支不用随机数**（D-104 补充）：随机会让每次重跑输出不同，「重跑等于入库」这条判据就失效了。阶段形状按 `AGE_3` 的四级递进，画风参照同目录 `power_beet_stage0..3`（4 色、约 251/256 透明）与 `ubi_server_stage0..3`（9 色、175/256 透明）。

### 9.4 语言键增量（**16 键**）

| 前缀 | 0.0.4 | 增量 | v0.1.0 | 来源 |
|---|---|---|---|---|
| `block.farmstorm.*` | 25 | **+1** | 26 | `outpost_core` 的方块名 |
| `item.farmstorm.*` | 31 | **+1** | 32 | `outpost_core` 的物品名 |
| `farmstorm.configuration.*` | 155 | **+11** | 166 | §8.1 的 11 个新键（第 10 行是改默认值，键名不变） |
| `command.farmstorm.*` | 0（新前缀） | **+1** | 1 | `command.farmstorm.farmlandlimit.set`（§7.3） |
| `message.farmstorm.*` | 0（新前缀） | **+2** | 2 | `message.farmstorm.farmland.no_core`、`message.farmstorm.farmland.quota_full`（§7.3） |
| 其余（`advancements` 36 / `tooltip` 16 / `entity` 13 / `gui` 8 / `death.attack` 6 / `effect` 4 / `subtitles` 3 / `container` 1 / `itemGroup` 1） | 88 | **+0** | 88 | 本版不加进度、不加效果、不加实体、不加界面；`tooltip` 的 6 个类别行复用既有条目 |
| **合计** | **299** | **+16** | **315** |

**两列合计的核对式**：0.0.4 列 `25 + 31 + 155 + 0 + 0 + 88 = 299`；v0.1.0 列 `26 + 32 + 166 + 1 + 2 + 88 = 315`；增量列 `1 + 1 + 11 + 1 + 2 + 0 = 16`。三处同时成立。

**`+0` 的其余类为什么不动**：进度 36 键对应 18 条进度（每条 title + description），本版 +0 条进度；`tooltip` 的 16 键按类别与「无原版对应物」组织，据点核心复用既有类别行而不新增类别；`entity` / `effect` / `subtitles` / `gui` / `container` / `itemGroup` 对应的注册面本版全部 +0（§2.1）。

---

## 10. 风险登记册（RT-24 ~ RT-30）

编号固定 7 条，不增删合并。每条五项：风险 / 触发条件 / 首要缓释 / 兜底 / 实现阶段怎么验。

### RT-24 · 生长时间从 10~44 分钟压到 20~45 秒：随机刻 → 计划 tick 链

| 项 | 内容 |
|---|---|
| 风险 | ① 这是**全局机制替换**：15 个作物方块从「原版随机刻推进」改成「计划 tick 链」，一旦漏删 `.randomTicks()` 或漏挡 `isRandomlyTicking`，就与计划 tick 双源推进，20~45 秒不可控；② `scheduleTick` 对同一 `(pos, type)` 是覆盖还是并存未知（待验证 ②），并存会让同一株被多排几次、提前成熟；③ 计划 tick 若不随区块落盘（待验证 ③），区块卸载 / 重启后作物会停在原地，甚至被「重新排一次 d` 而拉长；④ 骨粉走原版 `setBlock` 抬高 age，若排程入口不止一处就会排出第二条链；⑤ `d` 由 `pos.hashCode()` 决定，同一块地重种得到同一个 `d`，观感上「这块地永远长得一样快` |
| 触发条件 | 有人为了让某处作物快点熟而把 `.randomTicks()` 加回去；`scheduleTick` 的重载签名与本文假设不同（待验证 ①）；区块卸载 200 格再回来；`performBonemeal` 被别的模组重写 |
| 首要缓释 | 三件套（§4.4）一起做：删 `.randomTicks()`、不重写 `randomTick`、显式 `isRandomlyTicking → false`；排程入口只有一个私有 `scheduleNext`，`onPlace` 与 `tick` 都只调它；`tick` 第一行判 `state.getBlock() != this`、第二行判 `age >= MAX`，任何失效状态都直接退出 |
| 兜底 | ① 若实测「同 `(pos,type)` 并存」且可观测（同一株 1 秒内推进两次），改为「排程前先看 `age` 推剩余段数、只用一次 `scheduleTick`」的形态，并把 `d` 的重算放进同一次调用；② 若计划 tick 不落盘，加 `ChunkEvent.Load` 里对 `age < 3` 的作物重排一次（D-111 已授权这条补丁）；③ 若 `d` 的坐标决定被作者判为「太规律」，把 `d` 改成 `level.getSeed() ^ pos.hashCode()` 参与（仍不引入方块实体与存储） |
| 实现阶段怎么验 | ① 4 株不同坐标的作物，光照 15，逐株记录「种下 → `AGE 3`」的刻数，全部落在 402 – 900 刻；② `randomTickSpeed` 临时调到 1000 再测一次，时间不变（§11 #4）；③ 同株日志里只有 3 次推进（不是 6 次、不是 30 次）；④ 种下后走出 200 格再回来，成熟时间仍落在 402 – 900 刻且没有瞬间成熟（§11 #5） |

### RT-25 · 自定义 `GameRule` 在 1.21.1 无法注册（无 Mixin）：改走 `/farmstorm farmlandlimit`

| 项 | 内容 |
|---|---|
| 风险 | ① 有人去翻 `GameRules` 想「试一下」，一引入 Mixin 就撞破两条红线；② 指令树是运行期注册，写错（字面量拼成 `farmland_limit`、参数类型用 `StringArgumentType`）不会有编译错误，只会在游戏里「指令不存在」；③ 权限等级写成 0 会让任何玩家自助改额度，规则等于不存在；④ 指令值被误当成持久值（重启后回到 config 默认，玩家会认为「设了没用」）；⑤ `RegisterCommandsEvent` 的订阅位置放错总线（mod 总线 vs game 总线）导致**永不触发** |
| 触发条件 | 有人在服务端启动日志里搜不到 `/farmstorm`；玩家在重启后再执行指令看不到旧值；有人拿 `RULE_MOBGRIEFING` 的读法去写自己的规则 |
| 首要缓释 | 载体只有 config + 子指令两件（§7.4 的三条依据写在文档里），实现里**不出现** `GameRules.register`；指令树的字面量与参数类型逐字照 §7.2；权限等级 2；反馈文案必须包含「本进程内有效」（§7.3） |
| 兜底 | ① 若 `RegisterCommandsEvent`（待验证 ④）的注册方式与本文不同，先在 `Farmstorm` 构造器里挂到 game 总线并打一行「指令树已注册」，以日志为准而不是以猜测为准；② 若指令最终无法注册，退化方案是「额度只由 config 决定，运维改 toml + `/reload`」——这会失去运行期调整能力，须写进验收记录 |
| 实现阶段怎么验 | ① 起服后 `/farmstorm` 能补全出 `farmlandlimit`；② 权限 1 的玩家执行被拒（原版权限提示）；③ 执行 `farmlandlimit 8` 后，无核心的玩家锄地被额度拒，核心 +16 后能锄到 24 块（§11 #11 的额度段）；④ 重启后额度回到 config 默认 0（§11 #10） |

### RT-26 · FIFO 与三份服务端索引：现有账本只有计数且不落盘

| 项 | 内容 |
|---|---|
| 风险 | ① 现有「玩家 → 部署物` 账本只有 `Map<UUID, DeployCounter(int, long)>`（`blockentity/DeployedCropBlockEntity.java:65-72`），拿不到「最早的是哪一个`；② 索引若不落盘，重启后 `oldest` 的顺序退化成「最后加载的`，FIFO 变成 LIFO；③ 挤出动作用错位置（写在 `setOwner` 里）会让第 17 个先占位、把刚放下的挤掉；④ 名额统计口径写成「全部部署物` 会把炮台与建造类算进去；⑤ `SavedData` 在 21.1.213 的工厂签名未知（待验证 ⑤），三份索引（陷阱 / 核心 / 耕地）都要用它；⑥ 坏条目（坐标已被 `/setblock` 改成空气）若不校验，`oldest` 会一直指向一个空位，挤掉动作变成空转 |
| 触发条件 | 同一玩家连放 17 个陷阱；服务端重启后再放第 17 个；放 16 个陷阱 + 8 个炮台；坐标被 `/setblock` 或爆炸清掉；区块卸载后重载 |
| 首要缓释 | 落盘的三份索引都是 `SavedData`，格式统一为「`UUID → 按 placedGameTime 升序的坐标列表`」；加载后**惰性校验**（坐标处的方块必须仍是该玩家名下的同类方块），坏条目自己过期；挤出动作用在 `getStateForPlacement`（唯一一处「判定在物品扣除之前」的位置，`block/DeployedCropBlock.java:178-181`）；名额只数 `CropProfile.byKind(kind).has(CropCategory.TRAP)` 的 4 个 kind |
| 兜底 | ① 若 `SavedData` 的 API 形态与本文假设不同，退化为「把三份索引写进一个自管的 `CompoundTag` 文件`——同一份格式、同一个惰性校验，只换载体；② 若重排顺序无法保证，索引里改存 `placedGameTime` 的**绝对刻号**并按它排序（不依赖加载顺序）；③ 若挤出仍有「挤错对象` 的报警，加一行日志打出被挤坐标与它的 `placedGameTime`，以日志取证后再改判据 |
| 实现阶段怎么验 | ① 同一玩家放 17 个陷阱：第 17 个放下后最早的消失、**无掉落**、总数仍 16（§11 #9）；② 服务端 `stop` 再 `start`，再放第 17 个，被挤掉的仍是最早那个（不是最后那个）；③ 16 个陷阱 + 8 个炮台混放，炮台一个都不被挤（§11 #17）；④ 把某个陷阱坐标 `/setblock air`，再放第 17 个：索引自动跳过这个空位 |

### RT-27 · 三处计数断言：`demolition == 4`、`total == 90`、`verify_project.py` 的硬编码小计

| 项 | 内容 |
|---|---|
| 风险 | ① `Farmstorm.java:104` 断言 `demolition == 4`，删掉两条档案的 `DEMOLITION` 后不改就**启动即 FAIL**；② 同一行旁边还断言 `CropProfile.ALL.size() == 22 && throwable == 12 && deployable == 7 && edible == 3`——这四项本版不动，误改反而会造出第二条错；③ `:138-141` 断言 `total == 90` 与 12 类逐类数，+3 之后不改也 FAIL；④ `verify_project.py` 的三处缺口（G1 / G2 / G3）里 G1 是「静默失效」——不加清单工具照旧绿，等于新对象永不复核 |
| 触发条件 | 任何人跑一次服务端；任何人跑一次 `verify_project.py`；任何人只改 G1 不改 G2；有人为了变绿去改 `01-DESIGN.md`（冻结区） |
| 首要缓释 | 三处改动写在同一批里（§3.7 第 4 行、第 18 行）：`demolition` 4 → 2、`total` 90 → 93 与三个逐类数 +1、工具的三段修；工具的动作顺序固定为「先加 3 个 ID → 再改 3 个计数 → 再加第四 config 来源`，顺序反了会先红一次（这正是 §1.4 说的「加了清单不改计数才会红`） |
| 兜底 | ① 若工具第四来源的解析难度过大，允许把期望 config 键数改成「从 `FarmstormConfig.java` 的 `define*` 调用数现数」（`grep` 计数在本仓已被证明会数错，因此这条只在解析文档失败时用，且必须写进验收记录）；② 若 `demolition` 的断言被误改成别的值，以 §11 #2 的日志行为唯一判据（必须是 `破障类 2 → OK`） |
| 实现阶段怎么验 | ① 起服读 `latest.log`：`自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / ... → OK` 与 `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK`（§11 #2）；② 跑 `python -X utf8 tools/verify_project.py`：`已核 registry 对象: 93 / 93`、config 段 166 键无 FAIL、`lang 键集一致，共 315 键`、`结论：FAIL 0 项`、退出码 0（§11 #1） |

### RT-28 · 耕地干预零基础：锄地拦截 + 记账 + 踩坏保护三条链

| 项 | 内容 |
|---|---|
| 风险 | ① 本模组对耕地 / 锄地**零干预**（`FARMLAND` / `FarmlandBlock` / `turnToDirt` / `HoeItem` / `Tillable` / `BlockEvent` 在 `src/` 下 0 命中），三条链全部从零开始；② 锄地的拦截点未知（待验证 ⑥）：`HoeItem` 在 1.21.1 是静态映射表还是数据驱动，决定「取消交互` 能否真的不产生耕地；③ 踩坏的拦截点未知（待验证 ⑦）：没有可取消事件就只能事后对账，会闪一帧泥土；④ 账本若只在内存，重启后 `used` 归零 ⇒ 额度被「重启一次` 绕过；⑤ 记账若用纯计数（D-117 已否），玩家自己挖掉一块就无处减一；⑥ 破坏核心转泥土时若做全范围扫描，会铲掉别人开垦的耕地（D-120 补充明确禁止） |
| 触发条件 | 玩家在无核心处锄地；队伍凑满额度后继续锄；核心范围内跳踩耕地；`/setblock` 把账上的耕地改成空气；核心被非己方炸掉 |
| 首要缓释 | 判定顺序固定「范围 → 额度」，两次拒绝都**不扣耐久**（§6.3）；账本是**坐标集合 + 计数缓存**并落盘（D-117 第 4 项），减项当场发生；转泥土只遍历账上坐标（§6.5 三条同时成立）；踩坏走可取消事件，不可用时退化为 40 刻对账 |
| 兜底 | ① 若锄地拦截无法做到「不扣耐久`，退化为「事件取消 + 允许产生耕地但不入账`：额度与范围的语义仍成立（不入账的耕地不计入 `used`、也不参与转泥土），并在验收记录里写明「视觉上能耕、规则上无效`；② 若踩坏事件不可用，用 §6.4 的 40 刻对账（代价：一帧泥土）；③ 若账本落盘失败，退化方案是把账本挂在「玩家下线时写一次 + 上线时读一次`，粒度粗但不会绕过额度 |
| 实现阶段怎么验 | ① 默认额度 0、无核心：锄草方块 → 不产生耕地、耐久不变、有提示（§11 #10）；② 放核心后同一位置锄 → 产生耕地，出 3×3 一格 → 不产生（§11 #11）；③ 范围内跳踩己方耕地 → 仍是耕地；范围外同操作 → 变泥土（§11 #12）；④ 破坏核心 → 范围内己方耕地全变泥土，范围外 / 他人耕地不动（§11 #13）；⑤ 重启后再锄：`used` 与重启前一致（不归零） |

### RT-29 · 据点核心 32 × 32 与其它方块 16 × 16 不同规格 + 六面区分

| 项 | 内容 |
|---|---|
| 风险 | ① 32 × 32 的贴图与同一图集里 16 × 16 的贴图混排，若开了 mipmap 可能出现边缘渗色；② `cube_bottom_top` 只有顶 / 侧 / 底三张，写错 `textures` 的键名（`top` / `side` / `bottom`）不会有编译错误，只会渲染成紫黑格；③ 物品模型若照抄 `item/generated`，物品栏里会显示成一张平面图而不是小方块；④ 压缩麦从 `cube_all` 改 `cube_bottom_top` 后，若只改模型没改生成器（§9.3 第 2 项），重跑一次就回到灰阶单贴图 |
| 触发条件 | 有人给 `outpost_core.json` 写了 `block/cube_all` 而不是 `cube_bottom_top`；有人在 `ModItemModelProvider` 里沿用了 `basicItem` 路径；有人重跑 `gen_textures.py` 之后没对比 PNG |
| 首要缓释 | 32 × 32 **不需要** uv 特殊处理：原版按 16 × 16 逻辑网格采样，贴图分辨率只是像素密度（BRIEF §2 第 20 条、D-123 依据）；模型父类固定 `cube_bottom_top`（素材本来就给了三张不同的面，用 `cube_all` 等于浪费两张）；物品模型固定「以方块模型为父」，不写 `layer0` |
| 兜底 | ① 若 mipmap 渗色可见，把 `outpost_core_*.png` 的边缘 1 像素向内收缩 1 格（不改尺寸、不改模型）；② 若 `cube_bottom_top` 的三键写不出预期观感，退化到 `cube_all` + 只用 `_side` 一张（会丢掉顶底两张的差异，写进验收记录后再决定） |
| 实现阶段怎么验 | ① 放下核心，四个侧面 / 顶 / 底各截一帧，三面贴图不同且都不是紫黑格；② 物品栏里它是一个小方块（能看出顶面与侧面不同），不是平面图；③ 压缩麦落地后顶 / 侧 / 底三面不同，且重跑 `gen_textures.py` 后三张 PNG 的字节数不变 |

### RT-30 · 边界粒子：48 × 48 格的量大

| 项 | 内容 |
|---|---|
| 风险 | ① 逐格发是每秒上百个包（2304 格 / 2 秒 = 1152 个粒子/秒/玩家）；② `只发给己方` 需要按玩家发包，而本仓只有 `ClientboundSoundEntityPacket` 的同类先例，粒子包没有（待验证 ⑧）；③ 距离衰减若只减点数不减频率，远端的玩家仍会收到每秒一轮的空包；④ 多个核心的范围重叠时，同一个玩家可能收到两圈粒子；⑤ 边界位置若按「核心中心 ± 24 格」硬算，核心放在区块边缘时边界会跨区块、观感断在区块缝上 |
| 触发条件 | 10 个己方玩家同时站在核心范围内；两个核心范围相邻或重叠；核心放在区块角落；`boundaryParticleIntervalTicks` 被调到 20 |
| 首要缓释 | 抽点到「每 4 格一点、单圈 48 点」并按边发包（4 包 / 圈）；距离三档衰减（≤ 48 格 48 点 / 48–64 格 24 点 / > 64 格不发）；几何按**区块网格**算（核心所在区块的 3 × 3 → 48 × 48 格），因此边界永远与区块线对齐，不会断在缝上；同一玩家被两个核心覆盖时每轮只取一次（用玩家 UUID 去重，与既有的 `AppliedAura` 同一形态，`blockentity/DeployedCropBlockEntity.java:68-75`） |
| 兜底 | ① 若按玩家发包（待验证 ⑧）不可行，退化到 `ServerLevel#sendParticles`（广播给半径内所有人，仍是原版包，代价是敌人也能看见边界）；② 若包量仍偏高，把间隔从 40 刻调到 60 刻（只改 config 值，不改代码）；③ 若重叠去重必须精确到「每个玩家每轮一圈」，用 `Set<UUID>` 在每轮开头清空（与 `claimAura` 同形） |
| 实现阶段怎么验 | ① 己方玩家进入范围：地面矩形四条边上每 2 秒出现一圈点，间距约 4 格，走出 48 格后仍有半密度，走出 64 格后消失（§11 #14）；② 第二个客户端（非己方）站在同一点：看不到任何边界粒子；③ 两个核心范围相邻时，交界处只有一圈而不是两圈重叠（§11 #15 的对照项）；④ 统计 10 秒内的粒子包数量 ≈ 20 个/玩家（4 包 × 5 轮） |

---

## 11. 验收自检表

每条一个具体动作 + 一个可判定的通过判据。**18 条**（#1 – #18）。

| # | 动作 | 通过判据 | 覆盖 |
|---|---|---|---|
| 1 | 按 §1.4 的三处修法改 `tools/verify_project.py`（G1 加 3 个 ID、G2 改 3 个计数、G3 加第四 config 来源），然后跑 `python -X utf8 tools/verify_project.py` | 输出 `已核 registry 对象: 93 / 93`、config 段 `166 个配置键与文档逐字一致`、`lang 键集一致，共 315 键`、`结论：FAIL 0 项`，退出码 0 | §1.4、§2.4、§9.4、RT-27 |
| 2 | 起服，读 `latest.log` 的两行自检 | 注册对象 **93** 且逐类为 `items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1`，行尾 `OK`；作物自检为 `22 种、投掷型 12 / 部署型 7 / 食用型 3；破障类 2`，行尾 `OK` | D-108、§2.4、RT-27 |
| 3 | 在光照 15 的田里种下 4 株坐标各不相同的作物，逐株记录「种下 → `AGE 3`」；再把 `growth.minSeconds` / `growth.maxSeconds` 改成 **10 / 20** 另种 4 株重测 | 默认配置：四株全部落在 **402 – 900 刻**（20.1 – 45.0 秒）且不全相同；改成 10 / 20 后：四株全部落在 **201 – 399 刻**（10.05 – 19.95 秒，`3 × ceil(200/3) = 201` 到 `3 × (400/3 整除) = 399`）——证明两个键真的参与算 `d`，不是装饰品 | D-110、§4.2、§8.1 |
| 4 | 把 `randomTickSpeed` 临时调到 1000，重复 #3 | 成熟时长与 #3 逐株相等（差 ≤ 20 刻）；同一株的推进日志只出现 **3** 次 | D-111、§4.4、RT-24 |
| 5 | 种下后立刻走出 200 格（强制区块卸载），再回来等到成熟 | 总时长仍落在 402 – 900 刻；回来的一瞬间没有「全部瞬间成熟」 | D-111 补充、§4.3 第 6 步、RT-24 |
| 6 | 同一玩家先踩胡罗贝，2 秒内再踩刺球；等 6 秒后在同一刺球再踩一次 | 第二次（2 秒内）**不触发**：无爆炸、无音效、刺球方块仍在原地；6 秒后的第三次正常触发 | D-112、§5.1 |
| 7 | 部署 1 个陷阱（不靠近它），记录 10 秒内的粒子 | 方块中心每 **5 秒**出现一次 dust（每次 2 个），10 秒内恰好 2 次；炮台与光环方块一次都不出现 | D-113、§5.2 |
| 8 | 把一只僵尸推进胡罗贝 / 刺球 / 震动豆的触发范围各一次；再把一只僵尸推进桑吉尔蔗 | 前三个陷阱都被触发（胡罗贝有爆炸、刺球出尖牙、震动豆有爆炸音并消失）；僵尸拿到的是通用效果——伤害 / 缓慢（若被打中）/ 击退，**混乱不施加**（`/data get entity` 里没有 `confusion`）；桑吉尔蔗：僵尸掉 **7.5 点**并被击退，且**不进相位链**（僵尸身上没有 `farmstorm:zangief_piledriver` 效果） | D-114、§5.3 |
| 9 | 同一玩家连放 17 个陷阱（按 5 刻间隔放，或对冷却期用不同放置点），逐个记录坐标 | 第 17 个放下后，**最早**那个的坐标变成空气、**没有掉落物**；该玩家的陷阱总数仍为 16 | D-115、§5.4、RT-26 |
| 10 | config 默认（额度 0）、无核心：拿锄头右键草方块 | 不产生耕地；锄头耐久 **不变**；出现「附近没有己方据点核心」的提示；重启后再试，行为不变 | D-116 第 2、4 项、§6.3、RT-28 |
| 11 | 放 1 个核心，在核心所在区块及其周围 8 个区块内各锄一格；再在 3 × 3 之外的第 9 个区块锄一格 | 前一批（9 个区块内的采样点）全部产生耕地；第 9 个区块那一格不产生耕地；核心给到的额度是 **16**（第 17 格被拒并提示已用/上限） | D-118、D-120 第 1 项、§6.1 / §6.2 |
| 12 | 在核心范围内跳踩己方耕地一次；在核心范围外对另一块己方耕地做同一动作 | 范围内那块仍是 `minecraft:farmland`；范围外那块变成 `minecraft:dirt` | D-120 第 3 项、§6.4 |
| 13 | 记下范围内、范围外、他人各 1 块耕地，然后挖掉核心 | 范围内己方那块变成泥土；范围外那块与他人那块都不变；该核心的 `+16` 当场失效（额度回到 `base`） | D-120 第 4 项、D-122 补充、§6.5 |
| 14 | 己方玩家站在核心 48 格内看地面边界；再让非己方玩家站在同一点 | 己方：矩形四条边上每 2 秒出现一圈点（约 48 点、间距约 4 格）；非己方：看不到任何粒子 | D-121、§6.6、RT-30 |
| 15 | 在相邻两个区块各放 1 个核心（两个 3 × 3 范围部分重叠），在两范围交界处锄地、并观察边界粒子 | 交界处那一格能正常开垦（并集成立、不出现空洞）；该处只出现一圈边界粒子，不是两圈重叠 | D-118 补充、§6.2、RT-30 |
| 16 | 同一玩家放第 3 个核心 | 放置不成立：方块没出现、物品**不消耗**、无掉落；已有 2 个核心不受影响 | D-122 第 1、2 项、§3.1 |
| 17 | 16 个陷阱 + 8 个炮台混放（同一玩家），再放第 17 个陷阱 | 被挤掉的是**最早的陷阱**；8 个炮台一个都没被挤、位置不变 | D-115 第 2 项、§5.4、RT-26 |
| 18 | 全仓扫描：`@Mixin`、`CustomPacketPayload`、`GameRules.register` | 三个计数都是 **0**；`GameRule` 只允许 `explosion/FarmstormExplosion.java:109` 那一处**读取** `RULE_MOBGRIEFING` | D-40、C-6、§7.4、22 号 §6 第 1、4 条 |

**编号与需求的对照**：#1 工具与三处计数（RT-27）、#2 启动自检（D-108）、#3–#5 生长（D-110 / D-111 / RT-24）、#6–#9 陷阱四项（D-112 ~ D-115 / RT-26）、#10–#13 耕地与核心（D-116 ~ D-120 / RT-28）、#14–#15 边界粒子（D-121 / RT-30）、#16 核心上限（D-122）、#17 名额口径（D-115）、#18 红线（D-40 / C-6）。

**四张表的行数复核**：§2 逐项点名 **3** 行、§8 逐项清单 **12** 行、§9（PNG 11 + JSON 7 + 语言键 6 行）、§11 自检 **18** 行——与 §0.4 的计数逐个相等。
