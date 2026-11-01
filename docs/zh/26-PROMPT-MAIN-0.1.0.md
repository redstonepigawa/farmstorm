# 26 · v0.1.0 主提示词

> **文档状态**：本文件是「农场风云 / Farmstorm」v0.1.0 施工期的主提示词，与 `05-PROMPT-MAIN.md`（v0.0.1）、`14-PROMPT-MAIN-0.0.2.md`（v0.0.2）、`20-PROMPT-MAIN-0.0.3.md`（v0.0.3）并存，不替代它们。

| 项 | 值 |
|---|---|
| 文档编号 | 26-PROMPT-MAIN-0.1.0 |
| 版本 | v0.1.0 |
| 读者 | CLI 编码代理（能读写仓库、能跑 `.tools\gw.cmd`） |
| 上游 | `docs/zh/22-DECISIONS-0.1.0.md`（唯一真相源，D-103 ~ D-123）→ `24-TECH-0.1.0.md` → `23-DESIGN-0.1.0.md` → `25-TASKS-0.1.0.md`（F-136 ~ F-175） |
| 任务书 | `25-TASKS-0.1.0.md` 的五个 Phase（21 ~ 25），**一次只投一个 Phase** |
| 装载方式 | 本文件**只投一次**，随 Phase 21 的第一份任务书一起给，之后常驻 |
| 编号接力 | 硬约束接 HC-50 → **HC-51** 起；禁止行为接 N-50 → **N-51** 起 |

## 0. 这份提示词的边界

本文件只写 v0.1.0 的增量与落在增量上的执行纪律。六条边界：

1. **覆盖面**：v0.1.0 是 9 个条目——蓄力条上移（D-103）、朋友的韭四阶段（D-104）、狂野苹果换图（D-105）、膨胀麦落地点白名单（D-106）、压缩麦三面与 3D 物品（D-107）、两件瓜去破障与落点收拢（D-108 / D-109）、作物生长改确定性推进（D-110 / D-111）、陷阱系统四项调整（D-112 ~ D-115）、耕地额度与据点核心（D-116 ~ D-122），外加素材口径（D-123）。决策共 **21 条**（D-103 ~ D-123，连续无跳号，22 号 §2 的六个分组），任务共 **40 个**（F-136 ~ F-175，25 号 §2）。
2. **三份老提示词都仍然有效**：`05-PROMPT-MAIN.md`（0.0.1）的 HC-01 ~ HC-14 与 N-01 ~ N-14、`14-PROMPT-MAIN-0.0.2.md`（0.0.2）的 HC-15 ~ HC-28 与 N-15 ~ N-28、`20-PROMPT-MAIN-0.0.3.md`（0.0.3）的 HC-29 ~ HC-50 与 N-29 ~ N-50 全部继续有效。本文件只追加 HC-51 起与 N-51 起，不复述、不改写、不放松这四份的任何一条。改 0.0.1 既有代码时基线是三份里的哪一段，由 `22-DECISIONS-0.1.0.md` §4.2 的 19 行点名决定。
3. **冲突裁决**：四份提示词冲突时，一律以 `22-DECISIONS-0.1.0.md` 为准；22 号没写到的，按 `24-TECH-0.1.0.md` > `23-DESIGN-0.1.0.md` > `25-TASKS-0.1.0.md` 取用。0.0.1 / 0.0.2 / 0.0.3 既有内容的裁决顺序仍走 `00-DECISIONS.md` / `10-DECISIONS-0.0.2.md` / `16-DECISIONS-0.0.3.md`（05 号 §0、14 号 §0、20 号 §0）。三份上游之间仍有分歧的六处，一律按 25 号 §0.4 的 A-19 ~ A-28 执行，逐条以裁决列为准。
4. **不写「这轮做哪一件事」**：那是任务书的事。本文件只回答「这件事允许怎么做、做完怎么算完」。
5. **投喂方式**：本文件**只投一次**，不随每个任务重复投递。分阶段任务在 `25-TASKS-0.1.0.md` 里，**一次只投一个 Phase**（Phase 21 起，编号紧接 0.0.3 的 Phase 20）；上一个 Phase 的阶段出口判据（25 号各段末的「阶段出口判据」+ 24 号 §11 的自检项）全部成立之后才投下一个。重复投递会让本文件与 05 / 14 / 20 号的三套编号在同一上下文里互相稀释，所以只投一次。
6. **只读清单**：`docs/zh/00` ~ `21`、`docs/en/00` ~ `21` 一律只读（D-80 的冻结口径继续有效，22 号 §4.1 的授权范围不覆盖文档）；22 – 25 是本版的上游真相源，实现期不改，要改口径先改 22 号并登记；26（本文件）是本次交付物。实现期只改代码与资源。

## 1. 角色与任务

你是这个仓库在 v0.1.0 的**实现工程师**，在 v0.0.4 已发版的代码上做增量。

- **目标形态**：Minecraft 1.21.1 NeoForge 模组 `farmstorm` v0.1.0——**93** 个注册对象（90 → 93）、**166** 条 config 键（155 → 166）、**315** 条语言键（299 → 315）；作物档案仍 **22** 条、进度仍 **18** 条；资源面 PNG 新增 **5** + 就地替换 **6**、JSON 新增 **5** + 就地替换 **2**（24 号 §2.4、§8.3、§9.4、§9.1、§9.2）。九个条目挂在 0.0.4 的档案模型、部署框架与陷阱接口上，不新开第二套框架。
- **交付判据**：`.tools\gw.cmd build` 退出码 0 并产出 `build/libs/farmstorm-0.1.0+mc1.21.1.jar`，复制到 `dist/farmstorm-0.1.0+mc1.21.1.jar`；`python -X utf8 tools/verify_project.py` 输出 `已核 registry 对象: 93 / 93`、config 段 `166 个配置键与文档逐字一致`、`lang 键集一致，共 315 键`、`结论：FAIL 0 项`，退出码 0；`.tools\gw.cmd runServer` 能起服且 `logs/latest.log` 的两行自检都以 `OK` 结尾；24 号 §11 的 **18 条**（#1 – #18）与 23 号 §11 的 **13 条**（§11.1 – §11.13）全部通过、未通过 0 条（24 号 §11、23 号 §11、25 号 F-175）。
- **任务规模**：任务从 **F-136** 起、Phase 从 **Phase 21** 起，共 40 个任务 / 五个 Phase / 原始 **63.0** 人日 / 加 25% 缓冲 **78.75** 人日 / 关键路径 **22.0** 人日 11 个任务（25 号 §2、§11.1）。

三条自我定位（沿用 05 号 §1、14 号 §1、20 号 §1，按 v0.1.0 的增量收紧）：

1. **你是执行者，不是策划。** 文档没写的数值你不发明；22 号标了「本文补充」的取值（例如 4 个陷阱的第二遍遍历、桑吉尔蔗的生物分支、FIFO 的落盘格式）照抄并在回报里提一句。24 号 §0.5 的 **8 处**「待验证」（① ~ ⑧）由你实测决定写法，不由你决定口径。
2. **你是服务端工程师。** 指令树、额度账本、两份服务端索引、免疫表、粒子发包全部在服务端与公共侧；本版客户端只改 `client/WildHunterHud.java:26` 的一个常量（D-103），客户端新增代码为 **0** 行（24 号 §7.1「是否服务端 only」行）。专用服务器起服时不许出现 `NoClassDefFoundError`。
3. **你是按图施工的人。** 类名、注册名、config 键名、资源路径、NBT 键名都已在 24 号定好（§3 类设计、§8.1 键清单、§9 资源清单）。你的工作是让它们存在，不是重新设计它们。

你一次只会看到本文件 + 当前 Phase 的任务书。**只做任务书要求的事，做完就停，回报，等下一个 Phase。** 不要提前实现下一阶段的行为，不要把下一个 Phase 才验收的东西先写一半。

## 2. 项目事实卡

| 项 | 值 | 出处 |
|---|---|---|
| 平台 | MC 1.21.1 / NeoForge 21.1.213 / Java 21 / ModDevGradle（构建基线不变） | 22 号 §1.1；25 号 F-137 |
| 构建入口 | `.tools\gw.cmd`（`compileJava` / `build` / `runData` / `runServer` / `runClient`）；**不许直接调 gradle** | `08-DEV-GUIDE.md` §4.1；20 号 §2 |
| 基线版本 | **0.0.4**，`dist\farmstorm-0.0.4+mc1.21.1.jar`，**959,376 B** | 22 号 §1.1 与 §8 第 5 行；24 号 卷首 |
| Java 文件数 | **117** 个 `.java`（`src/main/java` 递归计数） | 22 号 §1.1 与 §8 第 6 行 |
| 注册对象 90 → 93 | 基线 **90**（12 类逐类实测）、增量 **+3**、目标 **93** | 24 号 §1.2、§2.1、§2.4 |
| config 键 155 → 166 | 基线 **155**、增量 **+11**（第 12 行是改既有键的默认值，键不增）、目标 **166** | 24 号 §0.4、§1.4、§8.1、§8.3 |
| 语言键 299 → 315 | 基线 **299**（中英各一份）、增量 **+16**、目标 **315** | 24 号 §0.4、§1.4、§9.4 |
| 资源增量 | PNG 新增 **5** + 就地替换 **6**；JSON 新增 **5** + 就地替换 **2**；既有文件追加 3 处（`mineable/pickaxe` + 两份 lang） | 24 号 §9.1、§9.2；25 号 §0.5 |

数字只照抄，不重数。上表每一格都指到 22 / 24 / 25 号的具体小节；同一张表里两个数字相等是巧合，不是校验；校验用 §6 的 DoD 与 24 号 §11 的自检项编号。

### 2.1 动手前必须确认的五件事

这五条取自 24 号 §10 的 RT-24 ~ RT-30 与 §0.5 的 8 处「待验证」（① ~ ⑧），是本版最可能把实现卡死、且一旦判错就会做成假通过的五处。结论写进回报，不要默认「应该是对的」。

| # | 要确认的事 | 读哪里 | 不成立时怎么办 |
|---|---|---|---|
| 1 | **计划 tick 与 `.randomTicks()` 是不是只剩一个源**：15 个作物方块从随机刻换成计划 tick 链，漏删 `.randomTicks()` 或漏挡 `isRandomlyTicking` 就是双源推进，20.1 – 45.0 秒不可控（RT-24 第 ①、② 条） | 24 号 §4.1、§4.4、§10 RT-24；待验证 ① ②；`registry/FarmstormBlocks.java:88` | 三件套（删 `.randomTicks()`、不重写 `randomTick`、显式 `isRandomlyTicking → false`）一起做，排程入口只留一个私有 `scheduleNext`；若实测「同 `(pos,type)` 并存」且同一株 1 秒内推进两次，退到 RT-24 兜底 ①「排程前先看 `age` 推剩余段数、只用一次 `scheduleTick`」 |
| 2 | **自定义 `GameRule` 在 1.21.1 注册不了**：`GameRules.register` 是 `private static`，无 Mixin 红线内没有注册入口（RT-25） | 22 号 §2.6 D-116；24 号 §7.4、§10 RT-25；待验证 ④ | 载体只留 config `farmland.limitPerPlayer`（默认 **0**）+ `/farmstorm farmlandlimit <n>`；`RegisterCommandsEvent` 挂错总线时先在 game 总线挂上并打一行「指令树已注册」，以日志为准；仍注册不上退到「运维改 toml + `/reload`」并写进验收记录 |
| 3 | **FIFO 索引必须落盘**：现有账本只有 `Map<UUID, DeployCounter(int, long)>`，拿不到「最早的是哪一个」；不落盘则重启后 `oldest` 退化成「最后加载的」，FIFO 变 LIFO（RT-26 第 ①、② 条） | 24 号 §3.3、§5.4、§10 RT-26；待验证 ⑤ | 三份索引（陷阱 / 核心 / 耕地）统一为「`UUID` → 按 `placedGameTime` 升序的坐标列表」+ 加载后惰性校验；`SavedData` 形态不对时退到自管 `CompoundTag` 文件，换载体不换格式与校验；排序不稳时改存 `placedGameTime` 的绝对刻号 |
| 4 | **耕地干预零基础**：`FARMLAND` / `FarmlandBlock` / `turnToDirt` / `HoeItem` / `Tillable` / `BlockEvent` 在 `src/` 下 0 命中，锄地拦截、记账、踩坏保护三条链全部从零（RT-28） | 24 号 §6.3、§6.4、§10 RT-28；待验证 ⑥ ⑦ | ⑥ 做不到「取消即不产生耕地、不扣耐久」时退到兜底 ①「允许产生但不入账」，把「视觉上能耕、规则上无效」写进验收记录；⑦ 没有可取消的踩踏事件时退到 40 刻对账 + 恢复（代价是一帧泥土） |
| 5 | **边界粒子与陷阱 dust 的包量与配色**：48 × 48 格逐格发是每秒上千个包；「只发给己方」需要按玩家发包而本仓没有粒子包的先例（RT-30 第 ①、② 条） | 23 号 §8.7、§7.4；24 号 §6.6、§5.2、§10 RT-30；待验证 ⑧ | 按边抽点到「每 4 格一点、单圈 48 点」并按玩家发包，每轮 **1 个包 / 48 个粒子**（0.5 包/秒/可见玩家）；⑧ 不可行时退到 `ServerLevel#sendParticles`（广播给半径内所有人，代价是敌人也看得见边界）；包量仍偏高时把 `boundaryParticleIntervalTicks` 从 40 调到 60（只改 config，不改代码） |

## 3. 不可违反的硬约束（HC-51 ~ HC-76）

HC-01 ~ HC-14 见 `05-PROMPT-MAIN.md` §3、HC-15 ~ HC-28 见 `14-PROMPT-MAIN-0.0.2.md` §3、HC-29 ~ HC-50 见 `20-PROMPT-MAIN-0.0.3.md` §3，三套继续有效。下面 26 条只约束 v0.1.0 的增量面。

- **HC-51 · `verify_project.py` 开工时是绿的（属静默盲区），修的顺序固定为三步**
  - 约束：工具**是绿的**（`FAIL 0` / 退出码 0），不是红的；三处缺口是 G1（三张 `EXPECTED` 清单缺 `outpost_core`，`:120-146`）、G2（`EXPECTED_COUNTS` 的 12 类小计硬编码在 90 的基线，`:150-163`）、G3（config 期望集只读三个已冻结文档，`:174-189`）。动作顺序固定：① F-140 三张清单各加 `outpost_core` → ② F-141 把 `items` 31→**32**、`blocks` 25→**26**、`block_entity_type` 6→**7**、分母 90→**93** → ③ F-142 加第四个 config 来源（解析 24 号 §8.1 的 11 个新键并联集），期望 155→**166**。中间态红是**预期**。
  - 违反后果：F-140 提交后 §2 段报「清单 26 个 ID，但 … 的 v0.0.3 列是 25 个」，这是 G2 的预期红；把它当失败改回清单，等于 G1 白做。跳过 G1 只改 G2 ⇒ 3 个新对象永不被复核，比红更糟。为了让工具变绿去改 `01-DESIGN.md` ⇒ 同时破 D-80 与冻结区。
  - 来源：A-21；24 号 §1.4 的 G1 / G2 / G3、§11 #1；22 号 §4.2 表 A 第 5 行；RT-27 第 ④ 条。

- **HC-52 · 注册总数只有一个来源：90 + 3 = 93，且 +3 只在三类**
  - 约束：基线 **90**（24 号 §1.2 的 12 类逐类实测）、增量 **+3**（`items` 31→32、`blocks` 25→26、`block_entity_type` 6→7）、目标 **93**；其余 9 类 **+0**（`entity_type` 13 / `mob_effect` 4 / `sound_event` 3 / `recipe_type` 1 / `recipe_serializer` 1 / `creative_mode_tab` 1 / `damage_type` 3 / `data_component_type` 1 / `menu_type` 1）。`DeployedKind` 仍 **8**、`CropProfile.ALL` 仍 **22**、进度仍 **18**、`deployedOf` 的 `switch` 一字不改。
  - 违反后果：24 号 §2.4 的三条算式（`90 + 3`、逐类相加、分项相加）任一条对不上；启动自检第 1 行不是 `93 → OK`；给据点核心新建 `DeployedKind` 或 `CropProfile` 会同时破「档案 22」与「+3」两条边界。
  - 来源：D-119 / D-120 / D-121 / D-122；24 号 §2.1、§2.2 注 1 – 注 3、§2.3、§2.4；RT-27 第 ③ 条。

- **HC-53 · config 166 与语言键 315 是两条成对上升的链，310 只是中间态**
  - 约束：config 变更 **12 行 = 新增 11 + 改默认 1**（第 10 行 `melon_grenade.horizontalJitter` 0.3 → **0.05** 只改默认值，键不增）；config 键 155 → **166**，`farmstorm.configuration.*` 同为 166，顶层分组 30 → **35**；语言键 299 → **315**（`+16` = 1 `block` + 1 `item` + 11 `configuration` + 1 `command` + 2 `message`，其余 88 键 +0）。F-139 之后两份语言文件各 **310** 键（299 + 11），余下 5 键由 F-169 补齐。
  - 违反后果：12 行抄成 12 个新键 ⇒ config 变 167；把 **310** 当终值 ⇒ 终值差 5 键；`command.farmstorm.*` 与 `message.farmstorm.*` 两个新前缀漏写 ⇒ 中英键集不等；改 `horizontalJitter` 的 `min` / `max` 或 `translation` 键 ⇒ D-109 落点被越界改写。
  - 来源：D-106 / D-109 / D-110 / D-112 / D-113 / D-115 / D-116 / D-120 / D-121 / D-122；24 号 §8.1、§8.3、§9.4；22 号 §5.2、§5.3。

- **HC-54 · 资源增量是 5 + 6 张 PNG 与 5 + 2 个 JSON，另有两处「不新增 PNG」**
  - 约束：PNG 新增 **5**（`outpost_core_top.png` 2,745 B / `_side.png` 2,534 B / `_bottom.png` 2,513 B，均 32 × 32；`compressed_hay_block_top.png` / `_bottom.png` 各 16 × 16）+ 就地替换 **6**（`friend_chives_stage0..3.png` 4 张 + `wild_apple.png` + `compressed_hay_block.png`）；JSON 新增 **5**（`blockstates/outpost_core.json`、`models/block/outpost_core.json`、`models/item/outpost_core.json`、`recipe/outpost_core.json`、`loot_table/blocks/outpost_core.json`）+ 就地替换 **2**（`models/block/compressed_hay_block.json`、`models/item/compressed_puffed_wheat.json`）。`textures/item/` 下不出现 `outpost_core.png`，压缩麦不新增第二张物品贴图。
  - 违反后果：JSON 按 4 个记（23 号 §1.3 / §10.3 的口径）⇒ 战利品表漏建，核心挖不出掉落物；补一张核心物品图标 ⇒ PNG 新增变 6；`data/minecraft/tags/block/mineable/pickaxe.json` 与两份 lang 属「既有文件追加 3 处」，不算新增文件但漏了功能不全。
  - 来源：D-107 / D-123；A-20 / A-23 / A-27；24 号 §9.1、§9.2 的 A1 – A3、§2.3。

- **HC-55 · 生长必须放弃随机刻，三件套一起做**
  - 约束：① 删 `registry/FarmstormBlocks.java:88` 的 `.randomTicks()`；② **不重写** `randomTick`（不新增该方法）；③ 新增 `isRandomlyTicking(state)` 且 `return false`。排程入口只有一个私有 `scheduleNext(ServerLevel, BlockPos, BlockState)`，`onPlace` 与 `tick` 都只调它；`tick` 第一行判 `state.getBlock() != this`、第二行判 `age >= MAX_CUSTOM_AGE`；`onPlace` 只在 `oldState.getBlock() != this` 且 `level instanceof ServerLevel` 时排程。
  - 违反后果：漏任一条就是双源推进，20.1 – 45.0 秒不可控（把 `randomTickSpeed` 调到 1000 再测会当场露馅）；`grep -c "randomTick" block/FarmstormCropBlock.java` 非 0 即第 ② 条破；两处排程入口会排出两条链，同一株 1 秒内推进两次。
  - 来源：D-110 / D-111；RT-24、§4.4 的三件套；24 号 §4.1、§4.3、§4.4、§4.5。

- **HC-56 · 生长的时间常数只许这一个算式，四阶段只推三次**
  - 约束：`d = 134 + floorMod(pos.hashCode(), 167)`（`dMin` 134、`dMax` 300、`spread` 167），`T = 3d ∈ [402, 900]` 刻 = **20.1 – 45.0 秒**，三段各 `d` 刻、`d3 = T − 2×d1` 的余数恒为 0；**三次推进**到 `AGE_3`；亮度闸 `getRawBrightness(pos, 0) >= 9` 不过时排 **20** 刻重试、不推进、不打日志；`AGE_3`、`MAX_CUSTOM_AGE = 3`、四个 `VoxelShape`、骨粉走原版 `performBonemeal`（不重写）一律不动。
  - 违反后果：写成 [400, 900] 或「每株均匀取 T」⇒ 与 A-26 的裁决冲突，第三段拿不到 `d`；用 `level.random` 抽 `d` ⇒ 需要一个存东西的载体，撞破「不加方块实体」；日志里出现 6 次或 30 次推进 ⇒ 双源或重复排程；改成按条件重试推进 ⇒ 亮度闸的语义被换成计时器。
  - 来源：A-26；D-110 第 3 条 / D-111 第 4 条；24 号 §4.2、§4.3 第 5 步、§4.5、§11 #3 – #5；23 号 §6.3。

- **HC-57 · 破障类从 4 变 2，`demolition` 断言必须改**
  - 约束：`util/CropProfile.java:58-63` 两条档案（`MELON_SLICE_BOMB`、`MELON_GRENADE`）的 `categorySet` 去掉 `DEMOLITION`，各自只剩 `DAMAGE`；`Farmstorm.java:103-104` 的 `demolition == 4` → **2**；爆炸本体、`explosionPower` 3 / 2、`maxDamage` 8 / 6、两个抗性阈值 6.0、`spawnCount` 4、`spawnOffsetY` 1 一律不动。
  - 违反后果：不改断言 ⇒ 启动即 `破障类 2 → FAIL`（`taxonomyOk` 为 `false`）；把两件削弱成「不爆炸」⇒ 越过 D-108 第 3、4 条；顺手改同一行旁边的 22 / 12 / 7 / 3 四项 ⇒ 造出第二条错（RT-27 第 ② 条）。
  - 来源：D-108；22 号 §4.2 表 A 第 1 行与表 B 第 6 行、§5.1 第 4 行；RT-27 第 ①② 条；24 号 §3.7 第 4、6 行。

- **HC-58 · 瓜片子落点收拢是两个数一起压，常量改读 config**
  - 约束：`melon_grenade.horizontalJitter` 默认 0.3 → **0.05**；删 `entity/thrown/MelonGrenadeProjectile.java:26` 的 `SUBMUNITION_SPEED`，`:66` 改读 `melon_grenade.submunitionSpeed`（默认 **0.1**、范围 0.0 – 4.0）；`:25` 那句「实现细节常量，不进 config」的注释一并删掉；`:56` 的抖动走既有键；数量仍 **4** 片。
  - 违反后果：只改抖动 ⇒ 散开速度仍是起始半径的 6 倍，落点收不拢；留着常量 ⇒ 同一数值两个来源，改 toml 行为不变；留着那句旧注释 ⇒ 下一个读者照它把键删回去。
  - 来源：D-109；22 号 §4.2 表 A 第 2、3 行与表 B 第 8 行、§5.2 第 10、11 行；24 号 §3.7 第 7 行、§8.1 第 10、11 行。

- **HC-59 · 膨胀麦的白名单是叠加不是替换，压缩麦一行不改**
  - 约束：`entity/thrown/PuffedWheatProjectile.java:99-113` 的 `fillHay` 在既有「抗性 ≥ 3.6e6 跳过」之上**再叠**一道白名单——目标必须是空气或处于流体状态的方块才写，普通方块 / 方块实体 / 不可替换方块一律跳过；开关 `compressed.replaceOnlyAirAndFluid`（boolean，默认 **true**），`false` 时回到 0.0.4 行为；`CompressedPuffedWheatProjectile.java:35-37` **不改**（只重写 `fillBlock()`，判定继承）。
  - 违反后果：删掉抗性底线 ⇒ 破 D-38 / T-05 的防破坏底线，膨胀麦又能覆盖基岩级方块；白名单写成替换 ⇒ 关掉开关后与 0.0.4 不一致，对照实验失效；动压缩麦那一行 ⇒ `git diff` 出现名单外条目，HC-59 与 24 号 §3.7 第 9 行同时破。
  - 来源：D-106；22 号 §4.2 表 B 第 7 行；23 号 §4.2、§4.3、§8、§11.13；24 号 §3.7 第 8、9 行、§8.1 第 12 行。

- **HC-60 · 耕地规则的载体是 config + 子指令，不是 gamerule**
  - 约束：载体只有两件——服务端 config `farmland.limitPerPlayer`（默认 **0**）+ `/farmstorm farmlandlimit <n>` 子指令（`IntegerArgumentType.integer(0, 4096)`、`.requires(src -> src.hasPermission(2))`、成功后 `sendSuccess` 回一条带新值的反馈、返回 `Command.SINGLE_SUCCESS`）；**不注册任何 `GameRule`**；指令值只作用于当前服务端进程、重启回 config 默认、`/reload` 不重置（`bake()` 不重置 `base`）；全仓对 `GameRule` 的唯一读点是 `explosion/FarmstormExplosion.java:109` 读原版 `RULE_MOBGRIEFING`。
  - 违反后果：写成 `GameRules.register` ⇒ 编译不过，或为它引入 Mixin 而撞破两条红线（`grep -rn "GameRules.register" src/main/java` 非 0）；指令值落盘 ⇒ 出现「两处都能定义额度、且有先后」的双源；权限等级写成 0 ⇒ 任何玩家自助改额度，规则等于不存在。
  - 来源：A-19；D-116 第 1、5 项；RT-25；24 号 §7.1 – §7.6、§11 #18；22 号 §0 的「耕地规则的叫法」行。

- **HC-61 · 锄地拦截的顺序是「范围 → 额度」，两次拒绝都不扣耐久**
  - 约束：`canTill(level, player, pos) = inOwnRange(...) && used < capacity`，顺序固定；拦截点在 `PlayerInteractEvent.RightClickBlock`，取消时 `setUseBlock(Result.DENY)`；两种拒绝都**不产生耕地、不扣耐久**，各发一条动作栏提示（`message.farmstorm.farmland.no_core` / `message.farmstorm.farmland.quota_full` 带 `%s/%s`）；通过后在**同一 tick** 把坐标写进账本。
  - 违反后果：顺序反过来 ⇒ 范围外的玩家收到「额度已满」这种错误归因；扣了耐久 ⇒ 规则变成失败惩罚，D-116 第 4 项与 D-118 第 3 项同时破；记账晚一 tick ⇒ 连续锄地可以超额度。
  - 来源：D-116 第 4 项 / D-118 第 3 项；RT-28；24 号 §3.4、§6.3、§7.3。

- **HC-62 · 额度 = base + 16 × 核心数，记账用坐标集合并落盘**
  - 约束：`capacity = base + outpost_core.farmlandBonus(16) × 该玩家名下核心数`；有队伍时取**队伍内所有成员的个人额度之和**；`used` 是坐标集合的基数；记账容器 `Map<UUID, LinkedHashSet<BlockPos>>` + 计数缓存，落盘 `SavedData`；一块耕地被挖掉或炸掉时**当场**减项；同一块耕地在两个核心的并集范围里只算一块；读取时做实体性校验（坐标处必须仍是 `minecraft:farmland`）。
  - 违反后果：用纯计数 ⇒ 玩家自己挖掉一块就无处减一（D-117 第 4 项明确否决）；不落盘 ⇒ 重启后 `used` 归零，额度被「重启一次」绕过（RT-28 第 ④ 条）；上限按队伍均摊 ⇒ 与 D-117 第 1 项冲突。
  - 来源：D-117、D-118 第 4 项；RT-26 / RT-28；24 号 §3.4、§6.1、§6.2；23 号 §8.1。

- **HC-63 · 据点核心的四个属性逐项落位**
  - 约束：`strength(30.0F, 1200.0F)`（硬度 **30** / 抗爆 **1200**）、`requiresCorrectToolForDrops()` 且只进 `minecraft:mineable/pickaxe`（**不进** `needs_stone_tool` / `needs_iron_tool` / `needs_diamond_tool`，因此木镐即对工具）、`pushReaction(PushReaction.BLOCK)`（用注册属性，不监听活塞事件）、`mapColor(MapColor.STONE)` / `sound(SoundType.STONE)`；可放任意位置（不要求依附或支撑）；不做己方秒拆；掉落走方块自身战利品表产出自身。
  - 违反后果：省略 `pushReaction`（缺省是 `NORMAL`）⇒ 活塞能推能拉，事件拦不住已经发生的推动；进了任一 `needs_*_tool` ⇒ 「任意镐」破；用活塞事件替代属性 ⇒ 与 `registry/FarmstormBlocks.java:101-102` 的既有先例相反。
  - 来源：D-119 第 1 – 5 项；24 号 §3.1；23 号 §11.1；25 号 F-152 验收第 2 条。

- **HC-64 · 每人 2 个核心，超限拒绝放置而不是挤掉**
  - 约束：`outpost_core.maxPerPlayer` 默认 **2**，按**玩家**计、不按队伍计；超限时 `getStateForPlacement` 返回 `null`（方块不出现、物品**不消耗**、无掉落、**不做 FIFO**）；挖掉核心立即释放名额、不做延迟释放；效果（`+16`、范围、保护）才按队伍共享。
  - 违反后果：照陷阱的 FIFO 抄 ⇒ 玩家放下第 3 个时随机失去一个**已经生效**的据点（D-122 第 2 项明确否决）；消耗了物品 ⇒ `/clear` 前后计数会变，24 号 §11 #16 不过。
  - 来源：D-122 第 1、2、4 项与补充项；24 号 §3.1；25 号 F-152 验收第 1、3、5 条。

- **HC-65 · 拆核转泥土只遍历账上坐标**
  - 约束：`OutpostCoreBlockEntity#setRemoved` 的清算**只遍历账本里该归属者名下的坐标**，三条同时成立才 `setBlock(DIRT, UPDATE_ALL)` 并从账本删除：① 仍在该核心的 3 × 3 内、② 方块仍是 `minecraft:farmland`、③ 账本归属者是核心归属者本人或其队友；顺序是**先算完要转哪些坐标、再动世界**；转完从 `OutpostCoreIndex` 删除，`+16` 当场失效；一次性标志位保证只跑一次。
  - 违反后果：做全范围扫描 ⇒ 铲掉别人开垦、不在账上的耕地（D-120 补充项禁止）；边遍历边改账本 ⇒ 同一条目被处理两次或被漏掉；不清索引 ⇒ 额度虚高 16；没有一次性标志位 ⇒ 区块卸载时重复清算。
  - 来源：D-120 第 4 项与补充项、D-122 补充项；24 号 §6.5；23 号 §11.10 / §11.11。

- **HC-66 · 陷阱免疫是 5 秒大类、只记玩家、不落盘，闸在调度层**
  - 约束：`trap.immunityTicks` 默认 **100**（5 秒）；按**陷阱大类**（TRAP 类别整体）判、不按具体种类；判据放在 `TrapBehavior#tick` 的默认实现里，调用顺序固定 `findTrigger → passGate → trigger`；被闸住时**不 `consume`、不施效果、不写免疫**，`findTrigger` 返回空时也不写免疫；只记 `Player`（非玩家一律放行）；时钟 `level.getGameTime()`、不写存档；端点是半开区间 `[t0, t0 + 100)`。
  - 违反后果：把免疫写进四个 `impl` ⇒ 四处重复判据，四种陷阱的免疫窗口起点不一致；写进存档 ⇒ `SavedData` 命中，与「不落盘」冲突且重连不再清零；免疫写在 `trigger` 之后 ⇒ 同一刻两个陷阱都触发。
  - 来源：D-112；24 号 §5.1、§5.5；23 号 §7.3；25 号 F-146 验收第 4 条。

- **HC-67 · 每玩家 16 个陷阱：索引先落盘，挤出排在写入之前**
  - 约束：`trap.maxTrapsPerPlayer` 默认 **16**，名额只数 `CropProfile.byKind(kind).has(CropCategory.TRAP)` 的 4 个 kind（炮台与建造类不占名额）；索引 `trap/TrapFifoIndex` 是服务端 `SavedData`，格式「`UUID` → 按 `placedGameTime` 升序的坐标列表」，加载后**惰性校验**；挤出发生在 `block/DeployedCropBlock.java:163-184` 的 `getStateForPlacement` 里、`canDeploy(...)` 之后与 `return defaultBlockState()` 之前（**先挤出、后放行**），一轮最多挤 1 个，走既有 `consume()`（`removeBlock(pos,false)` + 破坏粒子、**无掉落**、不退还物品）；新条目在 `setOwner` 里 `record`、`setRemoved` 里 `remove`。
  - 违反后果：索引不落盘 ⇒ 重启后 `oldest` 退化成「最后加载的」，FIFO 变 LIFO；挤出动作用在 `setOwner` ⇒ 第 17 个先占位、把刚放下的挤掉；名额口径写成「全部部署物」⇒ 16 陷阱 + 8 炮台时炮台被挤，24 号 §11 #17 不过；不校验坏条目 ⇒ `oldest` 指向空气，挤掉动作空转。
  - 来源：D-115；RT-26；24 号 §3.3、§5.4、§5.5；25 号 F-149 验收第 1 – 4 条。

- **HC-68 · 敌对生物走第二遍遍历，通用效果生效、玩家专属跳过**
  - 约束：四个 `findTrigger` 各加第二遍 `level.getEntitiesOfClass(Monster.class, box)`，`box` 用与玩家那一遍相同的半径包 `AABB`；距离判据复用**同一个** metric 与同一个 config 键（切比雪夫 ≤ 1 / 欧氏 ≤ 2.5 / 球形 ≤ 1.5 / 严格小于 4.0），**不写第二个数**；生物恒按非己方处理、生物那一遍不调 `isAlly`；混乱只对玩家施加、缓慢 V 只对 `ServerPlayer` 施加（两处现实现不改）；桑吉尔蔗的 `trigger` 分两支——`ServerPlayer` 走相位链，其余只结算 `zangiefSlam` 固定 **7.5** 点 + `knockback(0.4, -dx, -dz)`，**不进相位链**；本项不加 config 键。
  - 违反后果：给生物单开一个半径常量 ⇒ 同一半径出现两个来源；让生物吃混乱 ⇒ `/data get entity` 里出现 `farmstorm:confusion`，24 号 §11 #8 不过；让生物进相位链 ⇒ 相位控制器以 `ServerPlayer` 为目标，编译不过或目标为空。
  - 来源：D-114；A-22；24 号 §3.5、§5.3、§5.5、§11 #8；23 号 §7.5。

- **HC-69 · 边界粒子取 23 号的 dust 青蓝，每轮 1 个包**
  - 约束：粒子取 `minecraft:dust`（`DustParticleOptions`），颜色 **(0.30, 0.85, 0.90)**、scale **1.0**、高度 = 核心方块 y + 0.1；几何是 48 × 48 格地面矩形的**四条边**、每 **4** 格一点、单圈 **48** 点；刷新间隔 `outpost_core.boundaryParticleIntervalTicks` 默认 **40** 刻（2 秒），同区块多核心用 `floorMod(pos.hashCode(), interval)` 错峰；每轮 **1 个包 / 48 个粒子**（0.5 包/秒/可见玩家），两档衰减（范围内满 48 点 / 16 格余量带每 4 轮 1 次）；只发给「己方且在范围内」的玩家；0 自定义网络包。
  - 违反后果：改取 `ParticleTypes.END_ROD` 或按边发 **4 个包/圈** ⇒ 与 A-24 的裁决冲突，包量是 1 包/圈的 4 倍，与 RT-30 的方向相反；逐格发 ⇒ 单圈 192 个点、包量放大 4 倍；对非己方也发 ⇒ 敌人看得见领地边界，24 号 §11 #14 不过。
  - 来源：A-24；D-121 第 1 – 5 项与补充项；RT-30；23 号 §8.7；24 号 §6.6、§11 #14 / #15。

- **HC-70 · 陷阱 dust 取 23 号的土色，每 5 秒 2 个，首次在第 100 刻**
  - 约束：`trap.dustIntervalTicks` 默认 **100**；每次 **2** 个粒子；`minecraft:dust`、颜色 **(0.62, 0.56, 0.44)**、scale **1.0**、位置 `(x + 0.5, y + 0.15, z + 0.5)`、散布 `0.25 / 0.10 / 0.25`、速度 0、32 格广播；首次出现在部署后第 **100** 刻（不是第 0 刻）；出粒条件 `floorMod(now + floorMod(pos.hashCode(), dustInterval), dustInterval) == 0`，在免疫闸**之前**调用（dust 与有没有人踩无关）；只有 4 个 TRAP 类的部署物发。
  - 违反后果：取狂野猎人的红烟 `(1, 0.15, 0.15)` ⇒ 与 A-25 的裁决冲突，与既有的红色标记混淆；挂到 `trigger` 上 ⇒ 没人踩就不出粒；炮台与光环也发 ⇒ D-113 第 4 条破；相位与扫描错开 ⇒ `dustInterval` 100 是 `scanInterval` 10 的整数倍这一前提被打破，dust 会被跳过。
  - 来源：A-25；D-113；RT-30 的同源顾虑；23 号 §7.4；24 号 §5.2、§11 #7。

- **HC-71 · 桑吉尔蔗相位 1 是「伺服抬升固定高度」，不是漂浮**
  - 约束：相位 1 = 关重力 + 每刻「剩余高度 ÷ 剩余刻数」，`riseHeightBlocks` **16**（实测约 15 格）/ `riseTicks` **15** / 单刻上限 `MAX_RISE_SPEED = 4.0`；相位 2 每刻 yaw **+72°**、落地固定 **7.5** 点；`amplifier` 只是相位号（`PHASE_REVERSED` 0 / `PHASE_LEVITATING` 1 / `PHASE_FALLING` 2，常量名是历史漏名）；`MobEffects.LEVITATION` 只被移除，不是机制。文档、注释、提交信息、验收记录一律写「相位 1 伺服抬升固定高度」，不写「漂浮」「浮空」「施加 `LEVITATION`」。
  - 违反后果：按「漂浮」实现 ⇒ 变成一个不受控的持续上升；写成施加 `LEVITATION` ⇒ 与 `behavior/ZangiefPiledriverController.java:390`、`:421` 的「只移除」相反；模板与代码两份口径 ⇒ 验收无法判定，24 号 §11 #8 的桑吉尔蔗段不过。
  - 来源：A-22；D-114 第 3 条；24 号 §5.3 的术语约定；23 号 §7.5；22 号 §8 #91。

- **HC-72 · 压缩麦与核心的模型：物品 3D、方块 `cube_bottom_top`**
  - 约束：`models/item/compressed_puffed_wheat.json` 删 `item/generated` 与 `layer0`、父改 `farmstorm:block/compressed_hay_block`；`models/block/compressed_hay_block.json` 由 `cube_all` 改 `cube_bottom_top`，`textures` 三键 `side` / `top` / `bottom`；核心的 `blockstates/outpost_core.json` 单变体指向 `farmstorm:block/outpost_core`，`models/block/outpost_core.json` 父 `cube_bottom_top` 三键，`models/item/outpost_core.json` 父 `farmstorm:block/outpost_core` 且**不写 `layer0`**；压缩麦方块属性 `strength(30.0F, 6.0F)` / `requiresCorrectToolForDrops()` / `sound(SoundType.GRASS)` / `mapColor(MapColor.COLOR_BROWN)` 一字不动。
  - 违反后果：三键写错名 ⇒ 渲染成紫黑格；沿用 `item/generated` ⇒ 物品栏里是平面图；只改模型不改生成器 ⇒ 重跑一次回到灰阶单贴图（RT-29 第 ④ 条）；给核心补一张物品图标 ⇒ HC-54 破。
  - 来源：A-23；D-107 第 1 – 4 项 / D-123 第 2 项；24 号 §9.2 的 #2 / #3 / #6 / #7、§9.3；RT-29。

- **HC-73 · 两条红线继续：0 自定义网络包、无 Mixin；`GameRules.register` 也是 0**
  - 约束：不注册任何 `CustomPacketPayload` / `PayloadRegistrar` 条目；不引入 Mixin、不写 `*.mixins.json`、不用 `@Inject` / `@Redirect` / `@Accessor`；`GameRules.register` 全仓 0 命中。粒子、指令、效果状态一律走原版通道（`ClientboundLevelParticlesPacket` / Brigadier / 既有 `MobEffect`）。
  - 违反后果：`grep -r "CustomPacketPayload" src/main/java` 非 0；`grep -rn "Mixin" src/main/java` 非 0；24 号 §11 #18 的三个 `grep -c` 不都是 0；为一条耕地规则引入 Mixin ⇒ 同时破 HC-60 与本节。
  - 来源：C-6、D-40、D-50；24 号 §7.4、§11 #18；22 号 §6 第 1、4 条；20 号 §3 的 HC-33 / HC-34。

- **HC-74 · 三个安全阀 5 / 8 / 32 一个不动**
  - 约束：`DEPLOY_COOLDOWN_TICKS = 5` / `MAX_DEPLOYED_PER_CHUNK = 8` / `MAX_DEPLOYED_PER_PLAYER = 32`（`blockentity/DeployedCropBlockEntity.java:48-50`）是服务器保护底线，不进 config、不改值；**16** 是玩法名额，与三者不是同一层。
  - 违反后果：把 32 改成 16 ⇒ 保护线与玩法上限混成同一个旋钮（D-115 第 5 项）；把三者挪进 config ⇒ 24 号 §8.4 的「明确不进 config」被推翻；删任一阀 ⇒ 破 0.0.1 起就存在的 HC-13。
  - 来源：D-115 第 5 项；24 号 §5.4 第 5 步、§8.4；HC-13（`05-PROMPT-MAIN.md` §3）。

- **HC-75 · 蓄力条只改一个常量，落点是距底 [48, 52) 带**
  - 约束：`client/WildHunterHud.java:26` 的 `BAR_Y_FROM_BOTTOM` **39 → 52**（整体上移 13 个 GUI 像素）；`int y = screenHeight - BAR_Y_FROM_BOTTOM` 的写法不变，不引入按分辨率自适应的第二套公式；条宽 20 / 高 4 / 间隔 4 / 总宽 44 / 居中算法 / 四种颜色 / 逐格填充顺序一律不动；`HOTBAR_WIDTH = 182` 不删。
  - 违反后果：蓄力条仍压在生命 / 饥饿行带 `[30, 39)` 上，需求等于没做；加第二套公式 ⇒ 两种分辨率给出两个位置，23 号 §2.5 的实测口径失效；删 `HOTBAR_WIDTH` ⇒ 与需求无关的改动，22 号 §6 第 10 条破。
  - 来源：D-103 与补充项；22 号 §4.2 表 A 第 4 行、§5.1 第 1 行、§6 第 10 条；23 号 §2.3 / §2.5 / §2.6；24 号 §8.4。

- **HC-76 · 两张贴图的口径：韭四阶段与狂野苹果 11 色**
  - 约束：`friend_chives_stage0..3.png` 就地替换为 16 × 16 四阶段（贴图高度 4 / 8 / 12 / 16、叶片数 2 / 3 / 5 / 7、四色 `#266325` / `#2B702A` / `#4A8F28` / `#55AB2D`、不透明像素 6 / 14 / 40 / 90、alpha 只能是 0 或 255），并**同时**改 `tools/gen_textures.py` 的专用绘制分支（`STAGE_DRAWERS`，**不用随机数**），使「重跑等于入库」；`wild_apple.png` 就地替换为 16 × 16、**11** 色、透明像素 126 / 256，模型 `layer0`、物品名与语言键不动。
  - 违反后果：只换 PNG 不改生成器 ⇒ 下一次重跑被 `PLACEHOLDER_COLOR` 覆盖回洋红；色数按 12 记 ⇒ 与 A-28 的裁决冲突（BRIEF §1.11 已纠正为 11）；韭的 alpha 出现中间值 ⇒ `cutout` 下出黑边；用随机数 ⇒ 「重跑等于入库」这条判据失效。
  - 来源：A-28；D-104 与补充项、D-105；23 号 §3.4 / §3.5 / §3.7、§9.4 – §9.6；24 号 §9.1 的 #6 – #11、§9.3 第 1 项。

## 4. 代码规范

### 4.1 新增代码落在哪

包结构沿用 `05-PROMPT-MAIN.md` §4.1 与 `02-TECH-SPEC.md` §2 的分工，本轮只新增这些位置（类名与文件路径照 24 号 §3 的逐项表，不自造、不改名）：

| 位置 | 放什么 | 来源 |
|---|---|---|
| `block/` | `OutpostCoreBlock`（注册属性、归属写入、每人 2 个上限、释放归属） | 24 号 §3.1 |
| `blockentity/` | `OutpostCoreBlockEntity`（三字段、归属解析、3 × 3 范围查询、边界粒子 tick、`setRemoved` 清算） | 24 号 §3.2 |
| `trap/` | `TrapImmunity`（`Map<UUID, Long>` + 服务器实例守卫）、`TrapFifoIndex`（`SavedData`） | 24 号 §3.3、§5.1 |
| `farmland/` | `FarmlandLedger`（坐标集合 + 计数缓存 + `capacity`）、`FarmlandGuard`（三条拦截面） | 24 号 §3.4 |
| `outpost/` | `OutpostCoreIndex`（玩家 → 核心坐标的落盘索引） | 25 号 F-153；24 号 §3.2 |
| `command/` | `FarmstormCommands`（全仓 `command/` 目录当前不存在，本版新建） | 24 号 §7.1 |
| `behavior/` 与 `behavior/impl/` | `TrapBehavior` 的签名放宽 + 免疫闸 + dust；四个 `impl` 的第二遍遍历与桑吉尔蔗生物分支 | 24 号 §3.5、§5.3 |
| `config/` | `FarmstormConfig` 的 +11 键与 1 处默认值 | 24 号 §8.1 |
| `datagen/` | 6 个 Provider 各 +1 项（blockstate / item 模型 / 方块标签 / 战利品表 / 配方 / 语言键） | 24 号 §2.3、§9.2、§9.4 |
| `src/main/resources` 与 `src/generated/resources` | 5 张新 PNG、5 个新 JSON；6 张 PNG 与 2 个 JSON 就地替换 | 24 号 §9.1、§9.2 |

### 4.2 改既有文件的边界

本版要改的既有文件只有 24 号 §3.7 的 19 个条目（18 改 + 1 核对不改）与 22 号 §4.2 的 19 行点名的那些：`Farmstorm.java`、`registry/FarmstormBlocks.java`、`registry/FarmstormBlockEntities.java`、`registry/FarmstormItems.java`、`config/FarmstormConfig.java`、`util/CropProfile.java`、`entity/thrown/MelonGrenadeProjectile.java`、`entity/thrown/PuffedWheatProjectile.java`、`entity/thrown/CompressedPuffedWheatProjectile.java`（**核对后不改**）、`behavior/TrapBehavior.java`、`behavior/impl/` 的四个陷阱、`block/FarmstormCropBlock.java`、`block/DeployedCropBlock.java`、`blockentity/DeployedCropBlockEntity.java`、`client/WildHunterHud.java`、`event/FarmstormGameEvents.java`、`tools/verify_project.py`、`tools/gen_textures.py`，以及被追加项的 6 个 datagen Provider。名单外的既有文件不动，要动先改 22 号再改代码。

### 4.3 注册

一律 `DeferredRegister`，在 `Farmstorm` 构造器里 `register(modBus)`。本版 `blocks` +1、`block_entities` +1、`items` +1，合计 **+3**，其余 9 类 +0（24 号 §2.1）。据点核心的 `BlockEntityType` 必须新建（`DeployedCropBlockEntity` 的构造器用 `kindOf(state)` 反查 `DeployedKind`，非部署方块直接抛异常；`ConverterBlockEntity` 的 type 只收 `converter` 且没有归属字段），不挂进既有 6 个。`DeployedKind` 仍 8 个、`CropProfile.ALL` 仍 22 条、进度仍 18 条。

### 4.4 数值与常量

12 行 config 全部进 `FarmstormConfig`，组名、键名、类型、默认值、范围逐字照 24 号 §8.1（新增 11 + 改默认 1，行数不等于新键数）；5 个新分组追加在 `vibration_bean` 之后，组内键序照 §8.1 的行序；正文书写形态是 `<group>.<key>`，语言键是 `farmstorm.configuration.<group>.<key>`。代码里不许出现玩法数值的字面量，也不许为同一个值设第二个来源。明确不进 config 的常量逐条照 24 号 §8.4：3 × 3 范围 9 个区块 / 48 × 48 格、抽点每 4 格一点 / 单圈 48 点、可见余量 16 格、安全阀 5 / 8 / 32、权限等级 2、亮度重试 20 刻、段长取值池 `spread = 167`、`HOTBAR_WIDTH = 182`。

### 4.5 客户端边界

本版客户端只改 `client/WildHunterHud.java:26` 的一个常量（D-103），客户端新增代码 **0** 行。指令树、额度账本、两份索引、免疫表、粒子发包都在服务端与公共侧——专用服务器起服时不许出现 `NoClassDefFoundError`，也不许为了「只发给己方」而起自定义网络包（HC-69、HC-73）。

### 4.6 注释与错误处理

只写「为什么」，不写复述方法名的注释；涉及决策的注释带编号，例如 `// D-116：额度不是 gamerule，载体只有 config + 子指令`、`// D-115：挤出排在写入新方块之前`、`// D-111：删 .randomTicks() 是为了掐掉双源推进`。被注释掉的代码一律不留。错误处理沿用 `05-PROMPT-MAIN.md` §4.6：不可恢复的初始化失败才抛异常；玩家输入、缺失 NBT、跨区块实体的可恢复异常返回默认值并 `Farmstorm.LOGGER.debug` / `warn`。禁止 `printStackTrace`、禁止空 `catch`、禁止 `catch (Exception e) {}` 兜底。

## 5. 每个任务的执行纪律

### 5.1 Plan —— 动笔前先读

- **做什么**：按固定顺序读，最多四份，不通读全部文档。读完用三句话说清「要建哪些类 / 注册哪些 ID 或 config 键 / 用哪条硬约束兜底」，说不清就不写。
- **读哪份的哪一节**：① 当前任务书 + 22 号里被它点名的 D-xx（通常 2 ~ 4 条）；② 24 号里这次要碰的类那一节（§3.x / §4.x / §5.x / §6.x / §7.x）与要碰的 config 键（§8.1）或资源行（§9.x）；③ 23 号里该条目的数值表与边界；④ 25 号里那个任务的「交付物」与「依赖」两行。
- **留下什么证据**：回报里「动手前」三句话，含四项——改动类、注册 ID 或 config 键、读到的 D 编号、兜底用的 HC 编号。

### 5.2 Do —— 一次只做一件可独立验证的事

- **做什么**：一个任务的改动一次落完，不在同一批里塞两个任务的活。F-140 / F-141 / F-142 三步必须**串行**且顺序不可换（HC-51）。
- **读哪份的哪一节**：25 号 §0.1 的任务粒度定义与各 Phase 的任务块；24 号 §3 的类设计小节。
- **留下什么证据**：`git diff --stat` 一行 + `.tools\gw.cmd compileJava` 的退出码。

### 5.3 Check —— 每步验证

- **做什么**：写完 Java 立刻 `.tools\gw.cmd compileJava`，通过才继续，不积攒改动到最后一次编译；涉及 DataGen 跑 `.tools\gw.cmd runData` 并核对产物真的出现在 `src/generated/resources`；涉及方块、方块实体、指令、陷阱行为时按任务书的「验收」跑 `.tools\gw.cmd runServer` 或 `runClient`；任务收尾 `.tools\gw.cmd build`。
- **读哪份的哪一节**：该任务的「验收」列 + 24 号 §11 与 23 号 §11 里被引用的自检项编号（写编号，不重抄判据）。
- **留下什么证据**：命令原文 + 关键输出行。断言必须落在 `logs/latest.log` 的断言行、游戏内 `/execute` `/data get` `/setblock` `/clear` 的返回值、F3 / F3+B 读数、`config/farmstorm-server.toml` 的叶节点计数、`src/generated/resources` 或 `build/libs` / `dist` 的产物之一。「表现正常」「手感良好」不算证据。

### 5.4 Act —— 卡住时的正确姿势

- **做什么**：先在 24 号里 grep 类名 / 方法名；再在本地 Gradle 缓存的 NeoForge 源码 / 反编译产物里确认签名；仍确认不了就停下，在回报的「未决问题」里挂对应的 RT 编号并写清阻塞点。24 号 §0.5 标了「待验证」的 8 处调用必须先写一个最小用例跑通，再写正式逻辑；跑不通就换成该 RT「兜底」栏写明的替代路径，并把结论写进验收记录。**不许**为了「让它编译过」而伪造方法、注释掉功能、把实现换成空方法或 `UnsupportedOperationException`。
- **读哪份的哪一节**：24 号 §10 的 RT 归属——生长挂 RT-24、指令与 gamerule 挂 RT-25、三份索引挂 RT-26、三处计数断言挂 RT-27、耕地三条链挂 RT-28、32 × 32 与六面区分挂 RT-29、粒子包量挂 RT-30；API 存在性 / 签名一律挂 24 号 §0.5 的 ① – ⑧，不新开编号。
- **留下什么证据**：回报里的未决问题行（含 RT 编号或待验证编号、确认过的真实签名、阻塞位置）。

### 5.5 任务粒度的三判据

1. 提交后 `.tools\gw.cmd build` 退出码 0，且不依赖同一批次中尚未提交的任务。
2. 任务自带至少一条可在 §5.3 列出的位置观察到的验收断言。
3. 回滚该提交不会让其它已完成任务失效（回滚只影响依赖它的后续任务）。

另有两条边界同样硬：一个任务不跨 Phase 边界（交付物全部落在它所属 Phase 的名称范围内）；一个任务只动 24 号 §3.7 点名的既有文件 + 新增文件。跨 4 个及以上包的改动要拆开做（`05-PROMPT-MAIN.md` §5.2 口径）。

### 5.6 边界

只动任务书划定的文件。看到边界外的缺陷、死代码、命名不一致，写进回报的「未决问题」，不顺手修。每个任务结束时仓库必须可编译，不留半成品状态。前一个 Phase 的阶段出口判据没全过，不投下一个 Phase。

## 6. 完成定义（DoD）

一个任务算完成，以下**全部**成立，缺一即视为未完成：

1. 任务书列出的验收项逐条可指认到代码、资源或命令读数。
2. `.tools\gw.cmd compileJava` 通过；涉及 DataGen 时 `.tools\gw.cmd runData` 通过且产物落进 `src/generated/resources`；收尾 `.tools\gw.cmd build` 退出码 0 并产出 `build/libs/farmstorm-0.1.0+mc1.21.1.jar`。
3. `python -X utf8 tools/verify_project.py` 输出 `已核 registry 对象: 93 / 93`、config 段 `166 个配置键与文档逐字一致`、`lang 键集一致，共 315 键`、`结论：FAIL 0 项`，退出码 0（工具尚未按 HC-51 修好时先做 F-140 ~ F-142，不拿红灯交差，也不拿「全绿但漏检」交差）。
4. 三组计数成立：注册 **93** 且逐类为 `32 / 26 / 7 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1`；config **166** 且顶层分组 **35**；两份语言文件各 **315** 键且 `set(zh) == set(en)`；资源增量 5 + 6 张 PNG 与 5 + 2 个 JSON，外加既有文件的 3 处追加点逐条对得上。
5. 启动日志的两行自检都在且以 `OK` 结尾：注册对象 93（逐类同上）、作物 22 种（投掷型 12 / 部署型 7 / 食用型 3、**破障类 2**）。
6. 三条红线可判：`grep -r "CustomPacketPayload" src/main/java` 为 0；`grep -rn "Mixin" src/main/java` 为 0；`grep -rn "GameRules.register" src/main/java` 为 0。
7. 无越界改动：`git status` 只出现新增文件与 24 号 §3.7 点名的既有文件；`docs/zh/00` ~ `21` 与 `docs/en/00` ~ `21` 未被触碰；22 – 25 未被回改（回改口径必须先改 22 号并登记）。
8. 验收面完整：24 号 §11 的 18 条 + 23 号 §11 的 13 条共 **31 条**逐条有「动作 / 预期 / 实测 / 结论」四列的记录，未通过 **0** 条。
9. 无绕过：没有注释掉的实现、空方法占位、`@SuppressWarnings("unused")` 掩盖未使用字段、以占位标记顶替逻辑；没有「我跑过了，没问题」这类无证据结论。

## 7. 回报格式

任务结束后按下面的模板原样输出，不即兴发挥，不省略小节：

```
## 任务回报
- 任务：<F-编号 / 任务书标题>
- 状态：完成 / 部分完成 / 阻塞
- 动手前三句话：<改动类 / 注册 ID 或 config 键 / 读到的 D 编号 + 兜底 HC 编号>
- 改动文件：<新增与修改分开列；超过 8 个时只列路径 + 一句话作用；名单外的既有文件必须单独标出并说明依据>
- 执行的命令与结果：
  - .tools\gw.cmd compileJava → BUILD SUCCESSFUL（<耗时>）
  - .tools\gw.cmd runData → 生成 N 个文件到 src/generated/resources
  - .tools\gw.cmd build → 退出码 0，产物 build/libs/farmstorm-0.1.0+mc1.21.1.jar
  - python -X utf8 tools/verify_project.py → 已核 registry 对象: 93 / 93；config 166；lang 315；结论：FAIL 0 项（退出码 0）
  - （失败的命令照写，附第一条错误原文与总结行）
- 验收项对照：<任务书每个验收项 → 证据位置（文件:行 / 命令输出关键行 / 日志断言行 / 24 号 §11 或 23 号 §11 的自检项编号）>
- 计数与红线：<本条涉及的 93 / 166 / 315 / 5 + 6 / 5 + 2 现值；CustomPacketPayload、Mixin、GameRules.register 三个匹配数；config 叶节点数>
- 未决问题：<文档矛盾、缺失的 API、必须由作者裁决的事项；挂 RT 编号或待验证编号的在这里点名；无则写「无」>
- 下一步提示：<刻意没做但下一步会碰到的事，一句话>
```

硬规矩：不贴超过 20 行的日志，超过就只留第一条错误与总结行；不把「我计划」写成「我完成」；RT 编号一旦挂上必须在「未决问题」里点名；把 310 当终值、把 4 个 JSON 当新增数、把相位 1 写成漂浮，这三类口径错误一律算未完成。

## 8. 禁止行为清单（N-51 ~ N-74）

以下每条都可用一条命令、一处 `git diff` 或一次读数判定，不是态度要求。

- **N-51** 不得保留 `.randomTicks()`；判据是 `grep -c "randomTicks" registry/FarmstormBlocks.java` 为 0，且该行在 `git diff` 里只有删除行。
- **N-52** 不得重写 `randomTick`，也不得漏写 `isRandomlyTicking` 的 `return false`；判据是 `grep -c "randomTick" block/FarmstormCropBlock.java` 为 0，且 `isRandomlyTicking` 的方法体里有 `return false`。
- **N-53** 不得把生长时长的下界写成 400 刻或「每株均匀取 T」；判据是 `FarmstormCropBlock` 里 `134` 与 `167` 各 1 处、四株实测全部落在 **402 – 900** 刻、同株推进日志 **3** 次。
- **N-54** 不得把耕地规则写成 `GameRule`；判据是 `grep -rn "GameRules.register" src/main/java` 为 0，且 `grep -rn "GameRule" src/main/java` 的唯一命中是 `explosion/FarmstormExplosion.java:109` 的 `RULE_MOBGRIEFING`。
- **N-55** 不得把子指令字面量写成 `farmland_limit` 或 `farmlandLimit`；判据是 `grep -rn "farmland_limit\|farmlandLimit" src/main/java` 为 0，`grep -c "farmlandlimit" command/FarmstormCommands.java` ≥ 1。
- **N-56** 不得给 `outpost_core` 省略 `pushReaction`，也不得用活塞事件替代它；判据是注册行的 `pushReaction(PushReaction.BLOCK)` 命中 1，且 `grep -rn "PistonEvent" src/main/java` 为 0。
- **N-57** 不得写 `END_ROD` 边界粒子；判据是 `grep -rn "END_ROD" src/main/java` 为 0，`OutpostCoreBlockEntity` 里 `DustParticleOptions` 命中 1、三个通道实参为 `0.30F` / `0.85F` / `0.90F`。
- **N-58** 不得把边界粒子按边发 4 个包；判据是粒子轮里每轮 `sendParticles` 调用 **1** 次、`count` 实参 **48**，1 个可见玩家的包量为 0.5 包/秒（23 号 §8.7 的包量账）。
- **N-59** 不得把陷阱 dust 写成红色 `(1, 0.15, 0.15)` 或复用 `SMOKE_RED`；判据是 `TrapBehavior` 的 dust 调用三通道实参为 `0.62F` / `0.56F` / `0.44F`，该文件里 `0.15F` 命中 0。
- **N-60** 不得让陷阱 dust 在第 0 刻出粒；判据是部署后 10 秒内恰好 **2** 次、每次 **2** 个，首次出现在第 **100** 刻。
- **N-61** 不得留着 `demolition == 4`，也不得顺手改同一行旁边的 22 / 12 / 7 / 3；判据是 `grep -c "demolition == 2" Farmstorm.java` 为 1、`demolition == 4` 为 0，且这四项断言在 `git diff` 里不出现。
- **N-62** 不得把语言键总数写成 310 或 300；判据是工具 §3 段输出 `lang 键集一致，共 315 键`，两份 lang 各 315 键。310 只在 F-139 之后、F-169 之前的中间态成立。
- **N-63** 不得把 config 期望键数写成 155，也不得把 12 行当 12 个新键；判据是工具 §1 段输出 `166 个配置键与文档逐字一致`，`config/farmstorm-server.toml` 叶节点 166、顶层分组 35。
- **N-64** 不得为了让工具变绿回改 `docs/` 冻结区，也不得给工具加第二份硬编码键清单；判据是 `git diff --stat docs/` 为空，第四个 config 来源是解析 24 号 §8.1 而不是新的硬编码表。
- **N-65** 不得把 FIFO 挤动作用在 `setOwner` 里；判据是 `enforce(...)` 的调用点在 `block/DeployedCropBlock.java` 的 `getStateForPlacement` 内、`return defaultBlockState()` 之前，`setOwner` 里只有 `record(...)`。
- **N-66** 不得把名额统计口径写成「全部部署物」；判据是 `TrapFifoIndex.count` 的判据里有 `CropCategory.TRAP`，且 16 陷阱 + 8 炮台时 8 个炮台的坐标不变（24 号 §11 #17）。
- **N-67** 不得改三个安全阀 5 / 8 / 32；判据是 `git diff blockentity/DeployedCropBlockEntity.java` 不含 `:48-50` 三行。
- **N-68** 不得把免疫写成落盘或按具体陷阱种类判；判据是 `grep -c "SavedData" trap/TrapImmunity.java` 为 0，且免疫判据里不出现 4 个具体 kind（只判 TRAP 大类）。
- **N-69** 不得让敌对生物吃玩家专属效果或进相位链；判据是僵尸触发后 `/data get entity @e[type=zombie,limit=1] active_effects` 里没有 `farmstorm:confusion`，僵尸推进桑吉尔蔗后身上没有 `farmstorm:zangief_piledriver` 且掉血 **7.5** 点。
- **N-70** 不得在代码、注释、提交信息或验收记录里把相位 1 写成「漂浮」「浮空」或「施加 `LEVITATION`」；判据是 `grep -rn "漂浮\|浮空" src/main/java` 为 0，`grep -c "LEVITATION" behavior/ZangiefPiledriverController.java` 的命中只在移除处。
- **N-71** 不得为桑吉尔蔗的生物结算或 3 × 3 范围新增 config 键；判据是 `FarmstormConfig` 的 diff 只有 §8.1 的 11 个新键 + 1 处改默认值，`zangief_cane` 段在 `git diff` 里为空。
- **N-72** 不得把 `outpost_core` 写进任一 `needs_*_tool` 等级标签；判据是三个 `needs_*_tool` 文件里 `grep -c outpost_core` 都是 0，`mineable/pickaxe.json` 里有 `farmstorm:outpost_core`。
- **N-73** 不得新增 `textures/item/outpost_core.png` 或第二张压缩麦物品图，也不得给两个 item 模型写 `layer0`；判据是 `jar tf dist/farmstorm-0.1.0+mc1.21.1.jar` 的条目按 HC-54 逐项对得上，两个 `models/item/*.json` 里 `layer0` 命中 0。
- **N-74** 不得用随机数画韭的四阶段，也不得把狂野苹果的色数写成 12；判据是 `tools/gen_textures.py` 的 `STAGE_DRAWERS` 段里 `random` 命中 0，`wild_apple.png` 的像素统计为 **11** 色、透明 126 / 256。

## 9. 已知陷阱

先点四件贯穿全仓的：**`neoForge { }` 大小写敏感**、**`.tools\gw.cmd` 是唯一构建入口**、**两个 `runServer` 必须隔开运行目录**、**ffmpeg 不在 PATH 上**。这四条与本版新增的十八条一起列在下表（全表 **22** 行）；「出处」列指到的节号就是那条陷阱的证据位置。

| 现象 | 根因 | 怎么躲 | 出处 |
|---|---|---|---|
| 工具全绿，但新增的 3 个对象从没被复核过 | `EXPECTED` 三张清单里没有 `outpost_core`（G1），清单不加就永远不报 | 先做 F-140 加清单；「全绿」不等于「核过」，G1 修好前 `90 / 90` 是假通过 | A-21；24 号 §1.4 G1；RT-27 第 ④ 条 |
| 加了清单没改计数，工具突然报「清单 26 个 ID，但 … 是 25 个」 | `EXPECTED_COUNTS` 的 12 类小计硬编码在 90 的基线（G2） | 把这条红当**预期中间态**，紧接着做 F-141 改 32 / 26 / 7 与分母 93；不要改回清单 | 24 号 §1.4 G2；A-21 |
| config 段报 `toml 多 11 个键且源码里存在（文档漏记 MISMATCH）` | config 期望集只读三个已冻结文档（G3），新 toml 变 166 后多出的 11 键在源码里存在 | 加第四个来源（解析 24 号 §8.1 的 11 个新键并联集），期望改 166；**不许回改 01 / 12 / 18 号** | 24 号 §1.4 G3；RT-27 兜底 ① |
| 生长时间忽快忽慢、把 `randomTickSpeed` 调到 1000 后时长变短 | 计划 tick 与原版随机刻双源推进：漏删 `.randomTicks()` 或没挡 `isRandomlyTicking` | 三件套一起做；同株推进日志只应有 3 次 | RT-24 第 ① 条；24 号 §4.4 |
| 作物「看不到它长」，或区块重载后一次性全熟 | 计划 tick 不随区块落盘 / 重排时机错 | 实测「存档 → 停服 → 起服」；不成立时启用 D-111 授权的 `ChunkEvent.Load` 重排补丁，只改载体不改口径 | RT-24 第 ③ 条；24 号 §4.3 第 6 步 |
| 启动日志打 `破障类 2 → FAIL`，`taxonomyOk` 为 false | `Farmstorm.java:103-104` 仍断言 `demolition == 4`，而两条档案已去 `DEMOLITION` | 断言改 **2**；同一行旁边的 22 / 12 / 7 / 3 四项一字不动 | 22 号 §4.2 表 A 第 1 行；RT-27 第 ① 条 |
| 去翻 `GameRules` 想「注册一个试试」，一动手就要 Mixin | 1.21.1 的 `GameRules.register` 是 `private static`，无 Mixin 红线内没有注册入口 | 载体只留 config + `/farmstorm farmlandlimit`；`GameRules.register` 全仓 0 命中 | A-19；D-116；24 号 §7.4；RT-25 |
| 指令树写好了，起服后 `/farmstorm` 补全不出来 | `RegisterCommandsEvent` 订阅挂在 mod 总线而不是 game 总线，或字面量拼错 | 订阅挂在 `FarmstormGameEvents.register(IEventBus gameBus)`；字面量逐字 `farmlandlimit`；以「指令树已注册」那行日志为准 | RT-25 兜底 ①；24 号 §7.1 待验证 ④ |
| 第 17 个陷阱挤掉的是**刚放下**的那个 | 挤出动作用在 `setOwner` 里，晚于方块写入 | 挤出生在 `getStateForPlacement`、`return defaultBlockState()` 之前；一轮最多挤 1 个 | D-115 补充项；24 号 §5.4 第 2 步；RT-26 第 ③ 条 |
| 重启后再放第 17 个，被挤掉的是最后一个放下的（FIFO 变 LIFO） | 索引只在内存，`oldest` 的顺序退化成「最后加载的」 | 索引落 `SavedData`，存「`UUID` → 按 `placedGameTime` 升序的坐标列表」+ 惰性校验 | RT-26 第 ② 条；24 号 §3.3、§5.4 第 6 步 |
| 边界粒子看不见，或包量是预算的 4 倍 | 取了 `END_ROD` / 逐格发 / 按边发 4 个包 | 取 `dust` 青蓝 `(0.30, 0.85, 0.90)`，每 4 格一点、单圈 48 点、每轮 **1 个包** | A-24；RT-30；23 号 §8.7；24 号 §6.6 |
| 陷阱 dust 与狂野猎人的红烟混淆，敌人一眼找到陷阱 | 复用了 `SMOKE_RED` 的 `(1, 0.15, 0.15)` | 取中性土色 `(0.62, 0.56, 0.44)`，位置 y + 0.15、散布 `0.25 / 0.10 / 0.25` | A-25；23 号 §7.4；24 号 §5.2 |
| 文档写「漂浮」，代码写「伺服抬升」，验收两边对不上 | 相位 1 的机制被写成 `LEVITATION` 或「浮空」 | 一律写「相位 1 伺服抬升固定高度」；`LEVITATION` 只出现在移除处 | A-22；24 号 §5.3 的术语约定；22 号 §8 #91 |
| 物品栏里压缩麦仍是平面图，或落地后是紫黑格 | 只改了方块模型没改物品模型 / `textures` 三键写错名 | 物品模型父改为 `farmstorm:block/compressed_hay_block` 并删 `layer0`；方块模型改 `cube_bottom_top` 且三键 `top` / `side` / `bottom` | A-23；D-107；RT-29 第 ②③ 条 |
| 重跑 `tools/gen_textures.py` 后，黄褐变回灰阶、韭变回洋红 | 只替换了 PNG，生成器里还是 `monochrome=True` 与 `PLACEHOLDER_COLOR` 单色填充 | 三处生成器改动一起做（`STAGE_DRAWERS` 分支、`monochrome` 改 `False` + `tint`、`BLOCK_SPECS` 加顶 / 底两项），并用「删图重跑逐字节相同」验收 | D-104 / D-107 第 3 项；24 号 §9.3；23 号 §3.5 |
| 32 × 32 的核心贴图与图集里 16 × 16 混排，边缘出现细紫边 | 不同规格的像素密度差 + mipmap | 只认「原版按 16 × 16 逻辑网格采样」这条；渗色可见时把边缘 1 像素向内收缩并把结论写进验收记录 | RT-29 第 ① 条与兜底 ①；D-123 依据 |
| Ogg 文件与上次对不上，怀疑转换出错 | Ogg 每次重编码的**流序列号是随机的**，字节不可复现 | 判据是「时长 / 声道 / 码率」三项（`ffprobe` 读数），**不比 md5**；本版 `sound_events` +0、无新音频，口径照旧 | `12-TECH-0.0.2.md` §6.4；20 号 §9 |
| 音频产不出来时误以为是工具坏了 | ffmpeg 在 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe` 但**不在 PATH 上**；Trae CN 的 6.1.1 无 libvorbis | 传 `-Ffmpeg` 绝对路径；脚本缺 ffmpeg 时退出码 1 并打印安装指引 | `12-TECH-0.0.2.md` §6.4；RT-14；20 号 §9 |
| 构建报 `Could not find method neoForge()` | 扩展块名是 `neoForge { }`，**大小写敏感**；插件未应用时报同一句 | 确认插件已应用且块名大小写正确；本版 `build.gradle` 零改动（F-137），结论写进回报 | 22 号 §1.1；25 号 F-137；20 号 §9 |
| 直接调 `gradle` / `gradlew` 失败或行为异常 | `.tools\gw.cmd` 负责设 JDK 21 与 `GRADLE_USER_HOME`、拉 NeoForge 镜像代理、注入补丁、给 `runServer` 加目录锁 | 一律 `cmd /c ""D:\project_farm\.tools\gw.cmd" <任务>"`；不绕过、不另写替代脚本 | `08-DEV-GUIDE.md` §4.1、§4.2 |
| 语言键读数停在 **310** 就宣布验收通过 | 310 是 F-139 之后、F-169 之前的**中间态**（299 + 11 条 configuration），终值是 **315** | 终值只认工具 §3 段的 `共 315 键` 与两份 lang 各 315 键；报告、验收记录、文档里 310 一律标成中间态 | A-21；24 号 §9.4；25 号 F-139 验收第 3 条 / F-169 验收第 1 条 |
| 两个 `runServer` 撞 `session.lock`、日志互相覆盖 | 并行服务端共用一个运行目录 | 起第二个之前加 `-PfsRunDir=run-xxx`，各自独立目录；跨 Phase 验收留好各自的 `logs` | `08-DEV-GUIDE.md` §4.2；20 号 §9 |

## 10. 硬约束 → 来源 对照表

| 编号 | 约束摘要 | 来源 |
|---|---|---|
| HC-51 | verify_project.py 是绿的（静默盲区），修的顺序固定为三步 | A-21；24 号 §1.4 G1 / G2 / G3、§11 #1；22 号 §4.2 表 A 第 5 行；RT-27 |
| HC-52 | 注册总数只有一个来源：基线 90、增量 +3、目标 93，其余 9 类 +0 | D-119 ~ D-122；24 号 §2.1、§2.2 注 1 – 注 3、§2.4 |
| HC-53 | config 166 与语言键 315 成对上升，310 只是中间态 | D-106 / D-109 / D-110 / D-112 / D-113 / D-115 / D-116 / D-120 / D-121 / D-122；24 号 §8.1、§8.3、§9.4；22 号 §5.2 |
| HC-54 | 资源 5 + 6 张 PNG、5 + 2 个 JSON，两处不新增 PNG | D-107 / D-123；A-20 / A-23 / A-27；24 号 §9.1、§9.2 |
| HC-55 | 生长放弃随机刻，删 .randomTicks() + 不重写 randomTick + isRandomlyTicking false | D-110 / D-111；RT-24；24 号 §4.1、§4.4、§4.5 |
| HC-56 | d = 134 + floorMod(hash, 167)、T = 3d ∈ [402, 900] 刻，只推三次 | A-26；D-110 第 3 条 / D-111；24 号 §4.2、§4.3、§11 #3 – #5；23 号 §6.3 |
| HC-57 | 破障类 4 → 2，demolition 断言改成 2 | D-108；22 号 §4.2 表 A 第 1 行、表 B 第 6 行；RT-27 |
| HC-58 | 瓜片子抖动 0.05 与径向速度 0.1 一起改，常量改读 config | D-109；22 号 §4.2 表 A 第 2、3 行、§5.2 第 10、11 行；24 号 §8.1 |
| HC-59 | 膨胀麦白名单叠加不替换，压缩麦核对后不改 | D-106；22 号 §4.2 表 B 第 7 行；23 号 §4.2、§11.13；24 号 §3.7 第 8、9 行 |
| HC-60 | 耕地规则 = config 默认 0 + /farmstorm farmlandlimit，不是 gamerule | A-19；D-116；RT-25；24 号 §7.1 – §7.6、§11 #18 |
| HC-61 | 锄地拦截顺序「范围 → 额度」，两拒都不扣耐久不产耕地 | D-116 第 4 项 / D-118 第 3 项；RT-28；24 号 §6.3、§7.3 |
| HC-62 | 额度 = base + 16 × 核心数，队伍求和，坐标集合落盘 | D-117、D-118 第 4 项；RT-26 / RT-28；24 号 §6.1、§6.2；23 号 §8.1 |
| HC-63 | 核心硬度 30 / 抗爆 1200 / 任意镐 / pushReaction(BLOCK) | D-119 第 1 – 5 项；24 号 §3.1；23 号 §11.1 |
| HC-64 | 每人 2 个核心，超限拒绝放置、不 FIFO、不消耗物品 | D-122；24 号 §3.1；25 号 F-152 |
| HC-65 | 拆核转泥土只遍历账上坐标，三条同时成立才转 | D-120 第 4 项与补充项、D-122 补充项；24 号 §6.5；23 号 §11.10 / §11.11 |
| HC-66 | 陷阱免疫 5 秒大类、只记玩家、不落盘，闸在调度层 | D-112；24 号 §5.1、§5.5；23 号 §7.3 |
| HC-67 | 每玩家 16 个陷阱：索引先落盘，挤出排在写入之前 | D-115；RT-26；24 号 §3.3、§5.4、§5.5 |
| HC-68 | 敌对生物走第二遍遍历，通用效果生效、玩家专属跳过 | D-114；A-22；24 号 §3.5、§5.3、§5.5、§11 #8；23 号 §7.5 |
| HC-69 | 边界粒子 dust (0.30, 0.85, 0.90)，每轮 1 个包 / 48 点 | A-24；D-121；RT-30；23 号 §8.7；24 号 §6.6、§11 #14 / #15 |
| HC-70 | 陷阱 dust 土色 (0.62, 0.56, 0.44)，每 5 秒 2 个，首次第 100 刻 | A-25；D-113；23 号 §7.4；24 号 §5.2、§11 #7 |
| HC-71 | 相位 1 是伺服抬升固定高度，不是漂浮、不是 LEVITATION | A-22；D-114 第 3 条；24 号 §5.3；23 号 §7.5；22 号 §8 #91 |
| HC-72 | 物品模型 3D、方块 cube_bottom_top 六面区分、不写 layer0 | A-23；D-107 / D-123 第 2 项；24 号 §9.2、§9.3；RT-29 |
| HC-73 | 0 自定义网络包、无 Mixin、GameRules.register 为 0 | C-6、D-40、D-50；24 号 §7.4、§11 #18；22 号 §6 第 1、4 条 |
| HC-74 | 三个安全阀 5 / 8 / 32 一个不动，16 是玩法名额 | D-115 第 5 项；24 号 §5.4 第 5 步、§8.4；HC-13 |
| HC-75 | 蓄力条 39 → 52 只改一个常量，落点 [48, 52) | D-103；22 号 §4.2 表 A 第 4 行、§5.1 第 1 行；23 号 §2.3 / §2.5 / §2.6；24 号 §8.4 |
| HC-76 | 韭四阶段 + 生成器专用分支；狂野苹果 11 色 126 / 256 | A-28；D-104 / D-105；23 号 §3.4 / §3.5、§9.4 – §9.6；24 号 §9.1、§9.3 |

> 说明：本表的 26 行与 §3 的 26 条一一对应，行数必须相等。引用顺序按「决策编号优先、实现形态补节号」：能指到 D 编号的写 D 编号，需要落到具体写法时再补 24 号 / 23 号的节号。A 编号指 25 号 §0.4 的十条裁决（A-19 ~ A-28）；RT 编号指 24 号 §10 的七条风险（RT-24 ~ RT-30）。
