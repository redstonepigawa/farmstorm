# F-285 · 红线、注册面与「19 项必改」签字（v0.2.2 / Phase 53 / W07）

> 签字人：W07 执行子代理。签字对象：HEAD `c382af5`。
> 依据：49 号 F-285 的六件事（红线 / 注册面 98 + 口径外 3 类 / 数值面 176·60 与 340×2 / 标签 7 个 key·1 个成员 / `BRIEF` §3.4 的 19 项必改逐条落点 / 不改清单）+ A-82 / A-83 / A-91。
> 取证纪律：D-41·D-47（config 用「行首 live」= 176，全量含注释 177 是含 `FarmstormConfig:940` 那行注释）、D-20（词边界）、D-27（构建产物读数放锁内）、D-43（`compileJava --rerun` 的 exit code）、D-18·A-85（不可满足的断言收敛成白名单）、D-37（`299` 只许出现在沿革段）、A-83（「不动」的是 11 个逐类 `==`）。
> **本 Phase 不改码**：签字只读，不改任何文件；`git status` 只多出本目录三份记录。


## ① 读数表（八项 + 口径外 3 类）

| # | 项 | 读数 | 判据来源 | 证据 |
|---|---|---|---|---|
| 1 | **注册面** | **98** = items **34** / blocks **27** / block_entities **8** / entity_types **13** / mob_effects **4** / sound_events **3** / recipe_types **1** / recipe_serializers **1** / creative_tabs **1** / damage_types **3** / data_components **1** / menu_types **2**（逐类点名如下表） | 48 号 §1.2；49 号表 B | 启动日志原文 `自检 注册对象 98 个：… → OK`；`verify_project.py` 的 `已核 registry 对象: 98 / 98` |
| 2 | **Mixin** | **0**（`src/` 口径） | 48 号 §1.5 | `src/` 全树 `@Mixin\b` 命中 **0** |
| 3 | `*.mixins.json` | **0**（`src/` 口径） | 49 号 F-285 易漏点② | `src/` 下 0 件。**全仓另有 1 件** `.scratch/p25a/src/neoforge.mixins.json`（实验残留，不属 `src/` 口径，不许拿它判红） |
| 4 | **网络包** | **2** 个：`farmstorm:core_list`、`farmstorm:cultivator_link` | 48 号 §1.5 | `FarmstormNetwork:38` `playToClient(CoreListPayload.TYPE…`、`:43` `playToClient(CultivatorLinkPayload.TYPE…`；启动日志两条 `自检 网络包注册 …→ OK` |
| 5 | **协议串** | `"2"` | 48 号 §1.5；§6.5 | `CultivatorLinkPayload` 所在的 `FarmstormNetwork:25 public static final String PROTOCOL_VERSION = "2";` |
| 6 | **config** | **176 键 / 60 组** | 48 号 §4.2；D-41·D-47 | 行首 live `.define*` = **176**（全量含注释 177，多的是 `FarmstormConfig:940` 被注释掉的 `farmlandBonus`）；`b.push(` = `b.pop()` = **60**。**段数 60 的实测构成**：顶层 `^[` 表 **36** + 缩进的点号子表 `^\s+[` **24**（`[blast_carrot.nausea]` / `[awakened_torchflower.strength]` …）= 60，与 `b.push(` 一一对上（注意是单括号点号子表，`^\s+\[\[` 实测 = **0**，不是 `[[数组表]]`）。工具侧 `文档口径键数: 176` / `生成 toml 键数: 176`；新生成 toml 实测 176 键 |
| 7 | **语言键** | **340 × 2**（zh 340 / en 340 / zh-only **0** / en-only **0**） | 48 号 §5；A-91 | 两份 `lang/*.json` 键集比对；`farmstorm.configuration.*` = **179**、`tooltip.farmstorm.*` = **20**；工具 `[OK] lang 键集一致，共 340 键` |
| 8 | **Java 文件数** | **154** | 48 号 §8 #1；D-24 | `Get-ChildItem -Recurse -File src\main\java -Filter *.java` = 154 |
| 9 | **`FarmstormTags`** | **7** 个 `TagKey`（4 Block + 3 Item） | 48 号 §2.6.1 | Block：`EXPLOSION_IMMUNE` / `BLAST_PROOF` / `DEPLOYED_CROPS` / `PROCESSING_BLOCK`；Item：`MODIFIED_CROPS` / `SEEDS` / `CONVERTER_FUEL`。成员面：`processing_block` 只 1 个成员 |
| 10 | **口径外累计** | **3 类** | 48 号 §1.3 | 见下方 ③ |

### 注册面 98 逐类点名（12 类，相加 = 98）

| 类 | 数 | `verify_project.py` 逐类输出 | 本版变化 | 来源任务 |
|---|---|---|---|---|
| `Registries.ITEM` | **34** | `[OK]   items: 34 个 ID 齐全` | **+1**（`plain_chives`） | F-268 |
| `Registries.BLOCK` | **27** | `[OK]   blocks: 27 个 ID 齐全` | +0 | — |
| `Registries.BLOCK_ENTITY_TYPE` | **8** | `[OK]   block_entity_type: 8 个 ID 齐全` | +0 | — |
| `Registries.ENTITY_TYPE` | **13** | `[OK]   entity_type: 13 个 ID 齐全` | +0 | — |
| `Registries.MOB_EFFECT` | **4** | `[OK]   mob_effect: 4 个 ID 齐全` | +0 | — |
| `Registries.SOUND_EVENT` | **3** | `[OK]   sound_event: 3 个 ID 齐全` | +0 | — |
| `Registries.RECIPE_TYPE` | **1** | `[OK]   recipe_type: 1 个 ID 齐全` | +0 | — |
| `Registries.RECIPE_SERIALIZER` | **1** | `[OK]   recipe_serializer: 1 个 ID 齐全` | +0 | — |
| `Registries.CREATIVE_MODE_TAB` | **1** | `[OK]   creative_mode_tab: 1 个 ID 齐全` | +0 | — |
| `Registries.DAMAGE_TYPE` | **3** | `[OK]   damage_type: 3 个 ID 齐全` | +0 | — |
| `Registries.DATA_COMPONENT_TYPE` | **1** | `[OK]   data_component_type: 1 个 ID 齐全` | +0 | — |
| `Registries.MENU` | **2** | `[OK]   menu_type: 2 个 ID 齐全` | +0 | — |
| **合计** | **98** | `已核 registry 对象: 98 / 98` | **+1** | |

### 两条红线的读法（48 号 §1.5 的两条，写错任一条 #5 会红）

1. **网络包不进注册面** —— payload 走 `RegisterPayloadHandlersEvent`，不写 `BuiltInRegistries` ⇒ 98 里没有它。
2. **Mixin 的 0 是禁项**，不是「本版没轮到」：七个构件全部用注册表 + 事件总线 + 数据驱动实现，**没有**用 Mixin 去改 `Villager#restock` / `#updateTrades`（那同时踩红线与裁定 #18）。


## ② 16 行任务核对表（F-267 ~ F-282）

> 核对方式：`git log 214b2bf..HEAD` 得 18 个提交 = **16 个任务提交** + 2 个文档提交（`635d3c5` 是 v0.2.1 的 README 收尾、`86a3acc` 是 v0.2.2 的六份文档）。16 个任务**逐个有独立提交**（49 号 §0.4「一个任务 = 一个提交」）。

| 任务 | 一句话交付（提交标题原文） | 提交 | 证据位置 | 状态 |
|---|---|---|---|---|
| **F-267** | 版本号 0.2.2 与 6 处版本串（gradle.properties + 两支 .cmd） | `265a49d` | `gradle.properties:11 mod_version=0.2.2`；`一键构建.cmd:5,11,12`；`检查环境.cmd:5,14` = 1 + 5 = **6** 处 | 已落地 |
| **F-268** | `plain_chives` 注册 + 注册面 98 的原子提交（开窗任务） | `8407d74` | `FarmstormItems:131`；`Farmstorm.java:180 total == 98` / `:181 items == 34`；`verify_project.py:342` / `:381`（两处同一提交，A-84 的 8 处 `97→98` 注释同步） | 已落地 |
| **F-269** | 平凡韭菜的可见面（贴图 + 模型），31 个 -> 数量由注册表决定 | `a705916` | `textures/item/plain_chives.png`（16×16，目录 30 张）；`models/item/plain_chives.json` | 已落地 |
| **F-270** | `OutpostCoreScreen` 补 `render` 覆写（核心储物没有 tooltip 的根因） | `2c7b7a9` | `OutpostCoreScreen:48-52`（`super.render` → `this.renderTooltip`） | 已落地 |
| **F-271** | `OutpostCoreScreen#renderBg` 一次 blit -> 两次 blit（背包物品高 1 px 的根因） | `2283f3c` | `OutpostCoreScreen:43`（v=0，h=`upper`=125）/ `:45`（v=126，y=`topPos+125`，h=96） | 已落地 |
| **F-272** | `FarmstormTags.PROCESSING_BLOCK` + 标签产物（`TagKey` 6 -> 7） | `014be0d` | `util/FarmstormTags:30`；`data/farmstorm/tags/block/processing_block.json` | 已落地 |
| **F-273** | 绑定判据从 `instanceof` 改到 `PROCESSING_BLOCK` 标签判定（两处落点） | `2556de6` | `OutpostCoreBlockEntity:393` / `:783` 调 `isProcessingBlock`，助手声明于 `:802`（该新增块 `:794-810`）；词边界判据见 D-18 / D-20 | 已落地 |
| **F-274** | 两个默认值 4 / 24 + 十六处注释同提交 | `de82a77` | `FarmstormConfig:943 .defineInRange("maxPerPlayer", 4, 1, 64)` / `:956 .defineInRange("cultivatorCapacity", 24, 1, 64)`；注释白名单收敛到 3 处（A-85 / D-29） | 已落地 |
| **F-275** | `registry/FarmstormPoiTypes` —— job site 注册（口径外第 3 类） | `4ea74f1` | `FarmstormPoiTypes:32-34`（单例匹配集 + `maxTickets=1`） | 已落地 |
| **F-276** | `ModPoiTypeTagsProvider` —— POI 进 `minecraft:acquirable_job_site`（RT-64 主形态） | `9548959` | `ModPoiTypeTagsProvider:31 tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(FarmstormPoiTypes.BOTANIST_KEY)`；产物 `data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` = `{"values":["farmstorm:botanist"]}` | 已落地 |
| **F-277** | `registry/FarmstormVillagerProfessions` —— 植物学家职业 | `45aec5e` | `FarmstormVillagerProfessions:37`；`Farmstorm.java:66-67`（构造器 +2 行 `.register(modBus)`） | 已落地 |
| **F-278** | `event/BotanistTrade` —— 22 项普通交易（3-6 / maxUses 8 / 每次 1 个） | `219e6a6` | `BotanistTrade:60-82`（`new BotanistTrade(` 恰 22 处）；`MIN_PRICE=3`/`MAX_PRICE=6`/`MAX_USES=8`/`COUNT=1` | 已落地 |
| **F-279** | `event/BotanistEpicTrade` —— 固定 1 项史诗交易（40-64 / maxUses 1） | `7510d62` | `BotanistEpicTrade:38-52`（`MIN_PRICE=40`/`MAX_PRICE=64`/`MAX_USES=1`；池由 `tier()==EPIC` 过滤得 4 条） | 已落地 |
| **F-280** | `VillagerTradeEvents` 植物学家分支 + 类注释漂移（农民三行逐字未动） | `6cfd6ea` | `VillagerTradeEvents:39-45` 植物学家分支；农民三行 `:49-51` **不在 diff 里** | 已落地 |
| **F-281** | 两条语言键 + 计数注释（语言侧关窗：338 -> 340） | `96a884e` | `ModLangProvider:111`（`plain_chives`）/ `:163`（`entity.minecraft.villager.farmstorm.botanist`）；lang **340 × 2** | 已落地 |
| **F-282** | README 已知限制与升级须知（v0.2.2 的玩家可见面） | `c382af5` | `git show --stat c382af5` → `README.md | 17 +++++++++++++++++`（只加 17 行、0 删除） | 已落地 |

**16 / 16 全部落地**，逐个独立提交，无一个任务缺提交、无一个提交跨两个任务。


## ③ 口径外累计 3 类（逐条写明「不计入 98、`verify_project.py` 既不核存在也不核计数」）

| # | 注册表 | 本版数量 | 对象 | 注册点 | 为什么不在 12 类口径内 | 决策 |
|---|---|---|---|---|---|---|
| 1 | `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` | 0（保留既有 **1**） | `farmstorm:ai_suspend` | `registry/FarmstormAttachments`；`Farmstorm.java:65`（48 号写 `:63`，实测 `:65`） | 0.2.1 的裁定 #17 已定性为「12 类口径外的第 13 类」；它不是 `BuiltInRegistries` | 0.2.1 的 D-181；本版**一行不动** |
| 2 | `Registries.VILLAGER_PROFESSION` | **+1** | `farmstorm:botanist` | **新增** `registry/FarmstormVillagerProfessions`（`:37 REGISTER.register("botanist", …)`）；`Farmstorm.java:67` | `BRIEF` §1.6 全仓命中 0；走原版 `Registries.java:198`，不是本项目的 12 类 | D-196 |
| 3 | `Registries.POINT_OF_INTEREST_TYPE` | **+1** | `farmstorm:botanist`（绑定 `outpost_core`） | **新增** `registry/FarmstormPoiTypes`（`:32-34`）；`Farmstorm.java:66` | 同上，挂 `Registries.java:177`，不是 12 类 | D-196 |

**三类的共同结论（48 号 §1.3 第 3 条原文口径）**：它们**都不计入 98**；`verify_project.py` 对注册面只做两件事——`EXPECTED`（`:334-375`）的 12 组 id 逐个 grep 字面量、`EXPECTED_COUNTS`（`:380-393`）的 12 个计数相加。两张新表**不在任何一组里** ⇒ **既不核存在、也不核计数**。这是已知的覆盖缺口，**本版不扩工具**（46 号 D-188 范围闸），靠 48 号 §1.3 + §3 #7/#8/#9 + §8 #8 三处人工登记兜住。

**两条容易算错的地方**（写下来防后人重犯）：

1. **POI 与职业不能算进 98** —— 算进去会得出 **100** 的错误结论。本版 98 的增量只有 `items +1`。
2. **职业与 POI 是同一个名字、两张不同的表** —— `farmstorm:botanist` 在两处都是同一个 id，这是原版惯例（`minecraft:farmer` 既是 POI 也是职业），不是重复登记。


## ④ 「19 项必改」逐项核对（`BRIEF` §3.4，A-82 口径：**19 项**，不是 16 项）

| # | `BRIEF` 说的位置 | 要求的改动 | 改了没有 | 落在哪个文件:行号 |
|---|---|---|---|---|
| 1 | `gradle.properties:11` | `mod_version` 0.2.1 → 0.2.2 | **改了** | `gradle.properties:11 mod_version=0.2.2` |
| 2 | 两支 `.cmd` 的版本串 | 按行 **6** 处（`一键构建.cmd:5,11,12`、`检查环境.cmd:5,14`、gradle 1 行） | **改了 6 处** | `一键构建.cmd:5`（标题）/ `:11`（主 jar）/ `:12`（sources jar）；`检查环境.cmd:5`（标题）/ `:14`（jar 判定）；`gradle.properties:11` |
| 3 | `OutpostCoreScreen`（42 行） | **加 `render` 覆写**：`super.render(...)` 后调 `this.renderTooltip(...)` | **改了** | `OutpostCoreScreen:48-52`（类现 53 行，D-14 已登记 49 号写的 42/47 是估算） |
| 4 | `OutpostCoreScreen#renderBg` | **改两次 blit**（上段 `rows*18+17 = 125` 从 v=0；下段 96 从 v=126 画到 y=125） | **改了** | `OutpostCoreScreen:41`（`upper`）/ `:43` / `:45` |
| 5 | `FarmstormTags` | 加 `PROCESSING_BLOCK`（`TagKey<Block>`） | **改了** | `util/FarmstormTags:30 public static final TagKey<Block> PROCESSING_BLOCK = block("processing_block");`（`TagKey` 6 → **7**） |
| 6 | `ModBlockTagsProvider` | `tag(FarmstormTags.PROCESSING_BLOCK).add(FarmstormBlocks.CULTIVATOR.get())` | **改了** | `datagen/ModBlockTagsProvider:45`；产物 `data/farmstorm/tags/block/processing_block.json` |
| 7 | `FarmstormItems` | 注册 `plain_chives`（**不走 `cropItem`**）+ 进 `creativeTabItems()` | **改了** | `FarmstormItems:131-135`（`registerItem("plain_chives", Item::new, …)`）/ `:149 items.add(PLAIN_CHIVES);` |
| 8 | **新增** `registry/FarmstormPoiTypes` | `PoiType` 注册，绑定 `OUTPOST_CORE` | **新增了** | `registry/FarmstormPoiTypes:32-34`；注册挂载 `Farmstorm.java:66` |
| 9 | **新增** `registry/FarmstormVillagerProfessions` | 植物学家职业，workstation = 上面的 `PoiType` | **新增了** | `registry/FarmstormVillagerProfessions:37`；挂载 `Farmstorm.java:67` |
| 10 | **新增** `event/BotanistTrade`（或同类） | 22 项普通 + 1 项史诗的 `ItemListing` | **新增了 2 个类**（A-89：拆成两个类） | `event/BotanistTrade:60-82`（22 项）+ `event/BotanistEpicTrade:51-52`（史诗 1 项） |
| 11 | `VillagerTradeEvents` | 加一条分支处理植物学家（**不要动农民那三条**） | **改了，且农民三行未动** | `VillagerTradeEvents:39-45`；农民三行 `:49-51` 不在 `git diff 214b2bf..HEAD` 里 |
| 12 | `FarmstormConfig:941` / `:954` | 默认值 2 → 4、16 → 24 | **改了**（行号以实读为准） | `FarmstormConfig:943 .defineInRange("maxPerPlayer", 4, 1, 64)` / `:956 .defineInRange("cultivatorCapacity", 24, 1, 64)`。**登记**：`:941/:954` 是 `comment/translation` 行，`define` 行是 `:943/:956`（D-31） |
| 13 | `CultivatorLinkPayload:32-33` 的注释 | 算术随容量改 | **改了** | `CultivatorLinkPayload:31-35`（改后含「新装默认下每玩家 4 核 × 24 = 96，远小于本上限」）；常量在 `:36 MAX_LINKS = 1024`（A-90 说 `:31-34` 注释区间、`:35` 是常量行 ⇒ D-32 已漂到 `:36`） |
| 14 | `tools/verify_project.py` | `EXPECTED["items"]` + `EXPECTED_COUNTS["items"]` 33 → 34 | **改了 2 处** | `verify_project.py:342 "`plain_chives`"…`` / `:381 "items": 34,` |
| 15 | `datagen/ModLangProvider` | +`plain_chives` + 职业名 | **改了** | `ModLangProvider:111 item(entries, "plain_chives", "平凡韭菜", "Plain Chives");` / `:163 plain(entries, "entity.minecraft.villager.farmstorm.botanist", "植物学家", "Botanist");` |
| 16 | 创造栏 / 模型 / 战利品 | `plain_chives` 的 item 模型（datagen） | **改了** | `models/item/plain_chives.json` = `{"parent":"minecraft:item/generated","textures":{"layer0":"farmstorm:item/plain_chives"}}`；战利品面本版无新增（平凡韭菜不是方块、无掉落表） |
| 17 | **`Farmstorm.java:176-179` 的启动自检断言** | `total == 97` → **98**、`items == 33` → **34**（须与 `FarmstormItems` 同提交） | **改了，且同提交** | `Farmstorm.java:180 boolean ok = total == 98` / `:181 && items == 34 …`；与 `FarmstormItems` 同在 **`8407d74`**（F-268 原子提交）。实跑日志 `自检 注册对象 98 个：… → OK`（不是 `FAIL`）。**行号登记**：BRIEF 写 `:176-179`，D-36 已改为 `:180/`:181` |
| 18 | 注释里写死的默认值 | 「默认 16 / 默认 2」散在至少 5 处，**改默认值时必须同改** | **改了**（A-85 / D-29 白名单收敛） | 四个串（`默认 16`/`名额 16`/`上界 16`/`× 16`）在 `src/` 下：改前 **11** → 改后 **3**，且 3 处都**不是**名额语义（`ZangiefPiledriverController:52` 上升速度上限 / `WildHunterClientState:15` `loopSoundRange` 16.0 / `FarmstormConfig:841` 桑吉尔蔗抬升格数）。**登记**：`FarmlandLedger:394/:327` 的「16」是耕地额度基数 `farmlandBonus`，与 `cultivatorCapacity` 无关 ⇒ BRIEF 把它们归进这项是 BRIEF 的记账错误（D-30），**未改、正确** |
| 19 | **POI 必须进 `minecraft:acquirable_job_site` 标签** | 不加则村民永不认领 | **加了** | `datagen/ModPoiTypeTagsProvider:31`；产物 `data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` = `{"values":["farmstorm:botanist"]}`。**运行期佐证**：#13/#14 实测村民真的认领了。`FARMSTORM_TAGS` 只 +1（6→7），POI 标签用原版 `PoiTypeTags` |

**19 / 19 有落点**；其中 #17 可追到 **F-268**（`8407d74`）、#19 可追到 **F-276**（`9548959`）—— 与 A-82 的要求一致。


## ⑤ 不改清单（48 号 §3 表下的 5 行「不改」+ F-285 断言 3 的 5 项）—— 逐字未变的证据

| # | 不改的项 | 证据（行号 + 片段 / 零 diff） |
|---|---|---|
| 1 | **农民三行与 `TierTrade`** | `VillagerTradeEvents:49-51` 的 `getTrades().get(1).add(TierTrade.COMMON)` / `get(3)…RARE` / `get(5)…EPIC` **不在** `git diff 214b2bf..HEAD` 的 diff 里（diff 只有 import + javadoc + 植物学家分支 6 行）；`TierTrade.java` 零 diff |
| 2 | **`converter` 的方块 / 配方 / `CONVERTER_FUEL` / 菜单** | `git diff 214b2bf..HEAD -- '*onverter*'` = **0 行**（7 个 converter 类逐字未动）；`FarmstormTags:39 public static final TagKey<Item> CONVERTER_FUEL = item("converter_fuel");` 仍在；运行期佐证 B-16（转换器绑不上，但它作为方块/菜单本身未变） |
| 3 | **`Farmstorm#logSelfCheck` 的作物 / 品质 / 池断言** | `Farmstorm.java:128-129 boolean taxonomyOk = CropProfile.ALL.size() == 22 && throwable == 12 && deployable == 7 && edible == 3 && demolition == 2;` 与 `:145 boolean tierOk = common == 11 && rare == 7 && epic == 4;` **不在 diff 里**；实跑日志 `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK` + `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` |
| 4 | **`verify_project.py` 的 config 期望面** | 全 diff 只有 5 个 hunk，全在注册面 docstring / `EXPECTED` / `EXPECTED_COUNTS` / 两处 print 串；`expected_config_keys*` 七个函数（`:63/:81/:89/:97/:140/:185/:240`）**一行未进 diff**；工具实跑仍打印 `文档口径键数: 176` |
| 5 | **`OutpostCoreBlockEntity` 的补绑（`:537-570`）** | `git diff` 的 hunk 头为 `@@ -1,7 …` / `-12,6` / `-59,7` / `-112,10` / `-266,7` / `-323,14` / `-346,7` / `-390,7` / `-780,7` / `-794,6`` —— **`:537-570` 区间没有任何 hunk** ⇒ `rebindNearby` 逐字未动。运行期佐证：#25 的补绑读数（24/24、按距离排序、满则停）与源码一致 |

## ⑥ 复核方式与签字结论

| 检查 | 做法 | 结论 |
|---|---|---|
| 16 个任务都有提交 | `git log 214b2bf..HEAD` = 18 提交（16 任务 + 2 文档） | 16 / 16 |
| 「19 项必改」都有落点 | 逐项读源码行号（上表） | 19 / 19 |
| 5 项「不改」都真的没动 | `git diff` 零 hunk / 逐字比对 | 5 / 5 |
| 口径外 3 类都不进 98 | 98 逐类点名相加 = 98；两张新表不在 `EXPECTED`/`EXPECTED_COUNTS` | 成立（算进 98 会得 100，是错的） |
| 红线 | Mixin 0（`src/`）/ `*.mixins.json` 0（`src/`）/ 网络包 2 / 协议串 `"2"` | 全部成立 |
| 源码真的编译过 | `gw.cmd compileJava --rerun`（构建锁内） | `BUILD SUCCESSFUL in 12s` / `2 actionable tasks: 1 executed, 1 up-to-date` / **EXITCODE=0**（D-43：只用 `build exit 0` 不算证据） |
| 本 Phase 不改码 | `git status --porcelain` | 只有 `.work/v022-verify/` 下三份新文件（F-283 / F-284 / F-285 的记录）；**`src/` 与 `docs/` 的 diff 为空、本 Phase 无任何 git 写操作** |

**签字结论**：八项读数（98 / 176·60 / 340×2 / 2 包 / 协议串 `"2"` / Mixin 0 / Java 154 / `TagKey` 7）+ 口径外 **3 类** + 「19 项必改」**19 / 19** + 不改清单 **5 / 5** 全部核对完毕。
**未关闭项**（如实登记，不掩盖）：~~#16~~（**已由调度者授权补跑并闭环**：只移走 datagen 产物、不碰 Java 源码，反证成立 ⇒ 移走后 170 秒两个村民紧贴核仍不认领；已移回并重建，三条恢复证据全绿。完整链路见 `acceptance.md` 附录 A-5）、#20/#21/#22（需真开界面）、#24（需在线玩家）、#12（无命令可读 POI）、#27/#28 的行为面（需玩家）。清单见 `acceptance.md` 附录 A-4。

## ⑦ 交付物与偏离登记（本 Phase 实际动了什么）

| 项 | 内容 |
|---|---|
| 交付物（新增，仅此三份） | `.work/v022-verify/acceptance.md`（30 条验收）/ `boundaries.md`（27 条边界 + 四组汇总）/ `signoff.md`（本文件） |
| **tracked 文件改动** | **0**。`git status --porcelain` 里没有任何 ` M ` / ` D ` / ` A ` 行 ⇒ **`src/` 与 `docs/` 的 diff 为空、本 Phase 无任何 git 写操作**（只有 `git status` / `git diff` / `git log` / `git show` 四条只读取证命令） |
| **偏离登记：3 个 scratch 日志文件** | 为了留下可复核的原始日志，本 Phase 另在 `.work/`（**未跟踪区**）写了 3 个文件：`_w07_srv_a.log`（47 KB，首轮起服含 #1 的自检行）/ `_w07_srv_b.log`（7 KB，旧 toml 重启）/ `_w07_restock2.txt`（2 KB，补货二次读数）/ `_w07_srv_c.log`（#16 反证实验那一轮）。**性质与 W06 留下的 `_w06_build_f280.log` / `_w06_compile_rerun_f280.log` 相同**（调度者已确认：留着，不删），都在 `.work/` 这个历次波次的 scratch 区。调度者二审确认**留着**（未跟踪的取证日志，不进提交面）；**判据本身也不依赖它们**（所有读数已逐字抄进 `acceptance.md` 与 `boundaries.md`） |
| **偏离登记：#16 期间短暂移出过一个 tracked 产物** | 经调度者**专项授权**（只覆盖 #16），把 `src/generated/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` 用 `Move-Item`（**不是删除**）移到 `$env:TEMP`，重建 + 重启 + 观察后**立即移回**并重建。净结果为零：`git status --porcelain -- src/generated` 空、`git hash-object` 与移出前**相等**（`d12c464bca3fca7128d4e279ed7031019518fe0a`）、`git diff --stat -- src/generated` 空、全仓 tracked diff 空。**全程未改任何 Java 源码**（BRIEF #19 原本写的「注释掉 tag 行」被等价替换成「移走产物」，正是为了不碰源码）。完整链路见 `acceptance.md` 附录 A-5 |
| 服务端 | 全程只在 `run-w07`（`FS_RUN_LOCK=run-w07` + `-PfsRunDir=run-w07`，rcon 25647 / 密码 w07rcon）；**从未在 `run/` 里起服**；盘上的 `run/config/farmstorm-server.toml` 未被修改（只被读取与拷出） |
| 收尾 | 两轮服务端都已 `/stop` 正常关闭（`SERVER_A_EXIT=0`）；构建锁已释放 |
