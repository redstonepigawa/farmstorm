# F-309 · 0.2.3 验收记录（54 号 §8 的 32 条）

> 执行者：验收子代理（F-309）。窗口已关：F-286 ~ F-308 全部落地。
> 记录形态照 57 号 §64.4 的出口判据：每条给「动作 / 读数 / 结论 / 证据」。
> **不改码**：本轮只新增 `.work/v023-verify/` 下的文件；`src/`、`tools/`、`docs/` 零改动（见 §5 的 mtime 取证）。

## 0. 结论分布

| 统计项 | 条数 | 编号 |
|---|---|---|
| 通过 | **28** | #1 #2 #3 #4 #5 #6 #7 #8 #9 #10 #11 #12 #13 #14 #15 #16 #17 #18 #19 #20 #21 #22 #23 #26 #27 #28 #29 #30 |
| 通过（含未验证子项，见各行「未验证子项」栏） | **4** | #24 #25 #31 #32 |
| 不通过 | **0** | — |
| 未验证（整条） | **0** | — |

**可复核的分组**（按结论栏字面即可复现）：结论栏写「**通过**」的 24 条（#1~#6、#9~#16、#18、#20~#23、#26~#30）；带括注的 8 条（#7 绿、#8 期望中的红、#17 与 #19 反转判据的两半、#24 #25 #31 #32 含未验证子项）。24 + 8 = 32。

**口径**：本轮用 RCON + mineflayer 真客户端玩家（用户名 `FSEval`）在专用服务端上把「需要玩家动手」的条目真实跑掉，因此没有整条未验证的项。
剩下的不可达只落在四类纯观感/外部件上，逐条写在「未验证子项」栏里：像素观感（#24）、粒子与音效（#25）、图形客户端日志（#31）、0.2.2 实体存档文件（#32）。

**表内顺序说明**：E-64-2 要求四条组合**相邻**，因此表的行序不是严格 1→32；四对按 #7+#8、#13+#16、#17+#19、#24+#23 相邻排列，其余条目在组间保持编号顺序。

## 1. 环境与命令

| 项 | 值 |
|---|---|
| 运行目录 | `run-v023`（`-PfsRunDir=run-v023`，`FS_RUN_LOCK=run-v023`），日志 `run-v023/logs/latest.log` |
| 版本 | NeoForge 21.1.213 / MC 1.21.1 / Java 21（`.tools/jdk21`） |
| 起服次数 | **3** 次：①新 config（删 toml 重生成）②老 toml 回填（`[converter]` + 2/16）③#20 复测 |
| RCON | `127.0.0.1:25585`，`rcon.password=fsverify`；每次 `stop` 优雅退出（exit code 0） |
| 玩家 | mineflayer 4.39 离线客户端 `FSEval`（`auth=offline`、`version=1.21.1`），右键用 `block_place` 包真发 |

跑过的命令（原文）：

```text
1) 构建          cmd /c ""D:\project_farm\.tools\gw.cmd" build"                       -> BUILD SUCCESSFUL in 9s（5 actionable tasks: 5 up-to-date）
2) 检查器        python -X utf8 tools/verify_project.py                                 -> 见 #2
3) 起服①        $env:FS_RUN_LOCK=run-v023; gw.cmd -PfsRunDir=run-v023 runServer          -> Done (0.402s)!，日志 = logs/f309-session1.log
4) 探针 v3       node .work/v023-verify/f309-probe.js                                    -> logs/f309-probe3.log（#13 #14 #15 #24 #16 #29 #32）
5) 起服②        （把 2/16 老 toml 放回 run-v023/config 后同上）                           -> 日志 = logs/f309-session2.log
6) 探针 v4       node .work/v023-verify/f309-probe4.js                                    -> logs/f309-probe4.log（#16 腾格投递 #25 #12）
7) 起服③+探针v5  node .work/v023-verify/f309-probe5.js                                    -> logs/f309-session3.log / logs/f309-probe5.log（#20）
8) 读档探针      java -cp <serverLegacyClasspath;neoforge-21.1.213-merged.jar;build/classes/java/main;probe> NbtProbe -> logs/f309-nbtprobe.log（#32）
```

## 2. 32 条逐条记录

| # | 动作（谁 / 在哪 / 做什么） | 读数（原文或数值） | 结论 | 证据 |
|---|---|---|---|---|
| **#1** | 起服（run-v023，专用服务端），读启动日志的自检行 | `[03:12:55] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 92 个：items 28 / blocks 28 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK` | **通过** | `logs/f309-session1.log`（原文粘贴）；格式串 `Farmstorm.java:184-188` |
| **#2** | 跑 `tools/verify_project.py` | 第 1 节：`文档口径键数: 175` / `生成 toml 键数: 175` / `[OK]   175 个配置键与文档逐字一致（toml 175 键）`；第 2 节末行原文 `已核 registry 对象: 92 / 92`；末行原文 `结论：FAIL 0 项，WARN 1 项`；第 3 节 `[OK]   lang 键集一致，共 321 键` | **通过** | `logs/f309-verify.log`（全文），退出码 0 |
| **#3** | 读 `tools/verify_project.py` 的期望表 | `EXPECTED["items"]` 含 `plain_chives_seeds`（:416）、含 `crop_converter`（:412），7 个旧种子无一出现；`blocks` 含 `crop_converter`（:425）与 `plain_chives_crop`（:429）；`menu_type` 只剩 `outpost_core`（:450）；`EXPECTED_COUNTS` = items 28 / blocks 28 / … / menu_type 1（:458-471） | **通过** | 源码行号如上；检查器实跑亦报 28 / 28 / 1 全 OK |
| **#4** | **反向**：查 `expected_config_keys*` 五个老函数体 | 五个 v0.1.x 解析器仍在原位（`def expected_config_keys`:67、`_v002`:85、`_v003`:93、`_v010`:101、`_v011`:314），函数体内**不出现**本版删/增的 5 个键；本版的 3 删 / 2 增只由新函数 `expected_config_keys_v023`:239 现算（检查器实跑：`54-TECH §6.1 / §6.2：5 行（增 2 / 删 3…）`、`剔除已删键 5 个：converter.netherStarForwardCost、converter.netherStarOnReverse、converter.xpCost、…`） | **通过** | `tools/verify_project.py:67,85,93,101,314,239`。注：不跑 git 无法证明「一字不动」，本轮证的是**可观察形态**：老函数不解析这 5 个键、175/175 仍成立 |
| **#5** | 全树 grep `@Mixin` / `playToClient(`，读启动日志两行网络自检，读 `CultivatorLinkPayload:36` | `@Mixin` 命中 **0**；`playToClient(` 恰 **2** 处；日志原文 `自检 网络包注册 farmstorm:core_list（版本 3，S2C 只读，MAX_CORES 64）→ OK` 与 `自检 网络包注册 farmstorm:cultivator_link（版本 3，S2C 只读，MAX_LINKS 1024）→ OK`；`CultivatorLinkPayload.java:36: public static final int MAX_LINKS = 1024;` | **通过** | 脚本扫 `src/main/java` 的 152 个 `.java`；`logs/f309-session1.log` |
| **#6** | 数两份 `lang/*.json`（UTF-8 解析） | `zh_cn.json = 321`、`en_us.json = 321`、`zh-only = 0`、`en-only = 0`；§7.1 的 23 个删键**全部不存在**；§7.2 的 4 个加键**全部存在** | **通过** | `src/generated/resources/assets/farmstorm/lang/{zh_cn,en_us}.json`；逐键比对输出 |
| **#7** | 先删 `run-v023/config/farmstorm-server.toml`，起服后数新文件 | 键数 **175**；顶层节 **60**（`b.push` 口径）；`^[converter]` 命中 **0**、`^[crop_converter]` 命中 **1**；`maxPerPlayer = 4`、`cultivatorCapacity = 24`、`processTicks = 600`；首行 `[crop_converter]`；文件 mtime 03:12:57（本次起服生成） | **通过（绿）** | `run-v023/config/farmstorm-server.toml`（804 行）；删前备份 `logs/pre-f309-new-format.toml` |
| **#8** | **反向 · 期望中的红**：把升级前的旧 toml（`[converter]` 三段 + `maxPerPlayer = 2` / `cultivatorCapacity = 16`）放回 `run-v023/config/` 再起服 | 重写后：`maxPerPlayer = 2`、`cultivatorCapacity = 16` **原样保留**（未回写 4 / 24）；运行期旁证 `补绑：核 … 本轮新增 1 条（累计 4/16，周期 100 刻）`（容量 16 真的生效）；`[crop_converter]` 以默认值 600 出现；`[converter]` 段在重写后被删掉（首行变成 `[stack]`，803 行） | **通过（期望中的红，对应 #7 的绿）** | `logs/f309-session2.log`；toml mtime 03:24:52；构造件 `logs/old-format-2-16.toml`、原样旧档 `logs/old-format-4-24.toml`。**差异登记见 §3 第 1 条** |
| **#9** | 数 `data/farmstorm/recipe/` 里 `"type": "farmstorm:converting"` 的 JSON | **21** 个；6 条种子配方（`puffed_wheat_seeds` 等）**不存在**（目录里无任何 `*_seeds.json`）；`friend_chives.json` **存在** | **通过** | `src/generated/resources/data/farmstorm/recipe/*.json`（逐文件解析 `type`） |
| **#10** | 数整个 recipe 目录 | **25** 个文件 = 21 `converting` + `crop_converter` / `outpost_core` / `compressed_puffed_wheat` / `cultivator` 四条合成 | **通过** | 同上目录 |
| **#11** | 读两条合成配方 JSON | 转换器 `pattern: ["III","GRG","III"]` = 6 铁锭 + 2 玻璃 + 1 红石粉；培养器 `pattern: ["IGI","GRG","IGI"]` = 4 铁锭 + 4 玻璃 + 1 红石粉；两者的 JSON **没有 `unlockedBy` 字段**（因此不可能是 `#farmstorm:` 标签） | **通过** | `recipe/crop_converter.json`、`recipe/cultivator.json`。**差异登记见 §3 第 3 条** |
| **#12** | 起服看 recipe 载入；再 `/reload` 一次 | 启动：`[03:24:51] [main/INFO] [minecraft/RecipeManager]: Loaded 1315 recipes`；`/reload` 后 `[03:27:33] … [Rcon: Reloading!]` 紧跟 `Loaded 1315 recipes`；全日志 `Couldn't parse` / `Failed to parse` 命中 **0**（仅 3 条与本模组无关的 WMI COM WARN） | **通过** | `logs/f309-session2.log`；`/reload` 由 RCON 发出并读到 `Reloading!` |
| **#13** | 放核（8,-60,8）→ 放转换器（12,-60,8）→ 玩家手持小麦真右键 → 记时 | 右键后 `Held = {count: 1, id: "minecraft:wheat"}`、`Progress` 从 10 起涨（74→201→329→455→583）、`working=true` 断言 `Test passed`；**599 刻**后核里出现 `[{count: 1, Slot: 0b, id: "farmstorm:puffed_wheat"}]`，槽 `Held` 消失、`Progress = 0`、`working=false` 断言 `Test passed` | **通过** | `logs/f309-probe2.log`（插入与投递）、`logs/f309-probe3.log`（同流程复跑）；600 刻 = 30 秒，实测 599 刻差来自 0.5 s 轮询粒度 |
| **#16** | **核满暂停不丢**：把核 54 格塞满 64 个小麦 → 右键放 1 个小麦 → 等 700 刻 | 核 `slotCount=54`；插入后 `Held = wheat`、`Progress` 9→245→459→**600**；此后连续 60 s 采样恒为 `600`，核内**无** `puffed_wheat`（`PRODUCT-IN-CORE false`）、`working=false`、槽里小麦仍在；`item replace … container.53 with minecraft:air` 腾出 1 格后 **0.3 s 内**（≤1 刻）`Held` 消失、`Progress = 0`，核第 53 格变成 `{count: 1, id: "farmstorm:puffed_wheat"}` | **通过** | `logs/f309-probe3.log`（600 停住）+ `logs/f309-probe4.log`（腾格即刻投递） |
| **#14** | 转换器槽里有货时：空手右键；再手持**非方块**物品（木棍）右键 | 空手：`Held` 仍是 wheat、`Progress` 继续涨（40）；木棍：`SelectedItem` 前后都是 `{count: 1, id: "minecraft:stick"}`（未消耗）、`Held` 仍是 wheat、`Progress` 继续涨（28） | **通过** | `logs/f309-probe3.log`（14b 行）。注：v2 用泥土试过一次，泥土是方块物品、被原版放到上方空格，属测试构造问题，改用木棍 |
| **#15** | 转换器槽里有货（`Progress: 301`）时拆掉方块 | 掉落物只有 **1 个** `minecraft:wheat`（`Item: {count: 1, id: "minecraft:wheat"}` @ (12.29,-60,8.11)），不是产物、不是方块本体；重放后重新计时（`Progress` 15→40，从 0 起）；随后 `Progress: 0`、槽空 | **通过** | `logs/f309-probe3.log`。注：方块本体由战利品表另算（`/setblock air` 不产方块掉落），本读数只判「输入物那一件」 |
| **#17** | 培养器（10,-60,8）17 条投入物抽验 6 条，逐条真右键 | 收得进：`wheat` / `carrot` / `poisonous_potato` / `plain_chives`（四条都读到 `Held = {count: 1, id: <该物>}`、手持被消耗 1）；收不进：`torchflower` / `golden_apple`（`Held` 无键、手持 count 不变） | **通过（= 反转判据的一半）** | `logs/f309-probe2.log`（19/17-REJECT/17-ACCEPT 行） |
| **#19** | **反向**：拿 `farmstorm:puffed_wheat` 右键培养器 | 无反应：`Held` 读到 `Found no elements matching Held`（槽空）、`SelectedItem` 仍是 `{count: 1, id: "farmstorm:puffed_wheat"}`（未消耗） | **通过（= 反转判据的另一半）** | `logs/f309-probe2.log`（`19 puffed_wheat …` 行） |
| **#18** | 同一台培养器依次喂小麦、胡萝卜、毒马铃薯 | 小麦 ⇒ `{count: 2, id: "minecraft:wheat"}` + `{count: 2, id: "minecraft:wheat_seeds"}`；胡萝卜 ⇒ `{count: 4, id: "minecraft:carrot"}`；毒马铃薯 ⇒ `{count: 4, id: "minecraft:poisonous_potato"}`（不是 `potato`） | **通过** | `logs/f309-probe2.log`（18-PRODUCT 三行）。投料是真右键，600 刻等待用 `data merge Progress:598` 跳过 |
| **#20** | `plain_chives_seeds` 真右键耕地 → 骨粉催熟 → 拆成熟作物 | 种子被消耗、耕地变 `farmstorm:plain_chives_crop[age=0]`（服务端断言 `Test passed`）；骨粉第 1 次 → `age=2`、第 2 次 → `age=3`；拆掉后掉落 `{count: 1, id: "farmstorm:plain_chives"}` + `{count: 2, id: "farmstorm:plain_chives_seeds"}`（0~3 支内），方块变 air、耕地仍在 | **通过** | `logs/f309-probe5.log`。randomTickSpeed 200 的「等待」路径 60 s 内未到 age=3（见 §3 第 8 条），故用骨粉路径取证 |
| **#21** | 数 `data/farmstorm/tags/item/seeds.json` | 恰 **1** 项：`["farmstorm:plain_chives_seeds"]` | **通过** | 该文件原文 |
| **#22** | 目录计数 + 读模型文件 | **`textures/block/*.png` = 84 / `*.mcmeta` = 5 / `textures/item/*.png` = 22 / `textures/gui/` 内文件 = 0**（四数一次给全）；`converter_*.png` 命中 **0**（现存 9 张都以 `crop_converter_a_` 开头）；`models/block/` 下 `crop_converter.json`、`crop_converter_working.json`、`plain_chives_stage0..3.json` 六个文件全部存在 | **通过** | `src/main/resources/assets/farmstorm/textures/**`、`src/generated/resources/assets/farmstorm/models/block/**`。**差异登记见 §3 第 4 条** |
| **#24** | 同屏放培养器 + 转换器（同一个核）→ 拆培养器、原位置换放转换器，抓客户端收到的 `farmstorm:cultivator_link` 包 | 拆前包：`02 | (8,-60,8) | (10,-60,8) | 00 | (8,-60,8) | (12,-60,8) | 01`（kind 0=培养器 / 1=转换器）；换放后包：`02 | (8,-60,8) | (10,-60,8) | 01 | (8,-60,8) | (12,-60,8) | 01` —— **只差 1 字节 00→01**；服务端日志给出刻级时延：放置读 `gPlace=32256`，`连线下发：FSEval 2 条（…，刻 32258）` ⇒ **2 刻内重发**（去抖上限 `MIN_INTERVAL_TICKS = 10`）；核 NBT `CultivatorKinds` 从 `[0, 1]` 变 `[1, 1]` | **通过（像素观感未验证）** | `logs/f309-probe3.log`（FS-PAYLOAD#9 / #11）、`logs/f309-session1.log`（连线下发刻号）。**未验证子项**：屏幕上「青→红」的像素观感需图形客户端；替代读数是取色常量 `CultivatorLinkRenderer.java:48-58`（培养器 `0.30/0.85/0.90` = 青、转换器 `0.92/0.22/0.22` = 红）与包内 kind 字节 |
| **#23** | 读 `blockstates/crop_converter.json` | 恰 **8** 个 variant（4 朝向 × 2 工作态）；`working=false` → `farmstorm:block/crop_converter`、`working=true` → `farmstorm:block/crop_converter_working`；`y` 依次 north=0 / east=90 / south=180 / west=270；**全部 `uvlock: true`** | **通过** | 该文件原文（8 行 variant 表见 §4.2） |
| **#25** | 召唤 `farmstorm:turret_bullet` 并数其存活刻数 | `25 SPAWN gt=38534 alive=Test passed, count: 1` → `25 GONE {g:38835} ticks=301`，即 **第 300 刻消失**（301 含采样边界）；此后 `execute if entity @e[type=farmstorm:turret_bullet]` 恒 `Test failed` | **通过（粒子/音效未验证）** | `logs/f309-probe4.log`；代码 `TurretBullet.java:77-86`（`tickCount >= pumpkinTurretBulletLifetimeTicks` ⇒ `discard()`，不加粒子、不加音效、不写进度）。**未验证子项**：粒子与音效需客户端；F3 实体列表不可读（mineflayer 不认识模组实体 id，客户端计数恒 0），以服务端 `execute if entity` 为准 |
| **#26** | 看 `MelonGrenadeProjectile` / `MelonSliceProjectile` 两个类 | 文件 mtime 分别为 `2026-11-02 00:46:47`（本地）与 `2026-10-28 19:47:05`，**都早于本版窗口（11-05 21:00 起）**；行数 70 / 46；两个类都没有寿命判定，结算仍在 `onImpact(...)` 里经 `ExplosionQueue.submit` 落地才炸 | **通过** | `entity/thrown/MelonGrenadeProjectile.java:39`（`onImpact`）、`MelonSliceProjectile.java:40`。不跑 git，故以 mtime + 行数 + 结构取证 |
| **#27** | 数 `data/farmstorm/advancement/` | **16** 个 json；`seed_cycle.json` 与 `nether_star_toll.json` **不存在**；`converter.json` 仍在且判据是 `minecraft:placed_block` + `block: "farmstorm:crop_converter"` | **通过** | 该目录文件清单 + `advancement/converter.json` 原文 |
| **#28** | 读「最扎人的一集」的中英条目 | 中文 `advancements.farmstorm.fang_dance.description = 触发一次「刺球」`；英文 `= Trigger a "Fang Ball" once`；标题 `最扎人的一集` / `The Itchiest Episode`（标题两语均未变） | **通过** | `lang/zh_cn.json`、`lang/en_us.json` 原文（游戏内界面渲染的就是这两条，未另开客户端截图） |
| **#29** | 转换器硬度与抗爆：空手挖 8 s / 石镐挖 / TNT 炸 | 空手挖 8 s：方块**仍在**（`29 AFTER-HAND true`）；石镐：方块变 air 且掉落 `{count: 1, id: "farmstorm:crop_converter"}`；TNT（`fuse:30`，落点贴边）：转换器**仍在**，而旁边的地面格被炸成 air（对照组，证明爆炸真的发生）；注册属性 `strength(5.0F, 1200.0F)` + `requiresCorrectToolForDrops()` | **通过** | `logs/f309-probe3.log`；`registry/FarmstormBlocks.java:76-77` |
| **#30** | 读两条方块标签产物 | `data/farmstorm/tags/block/processing_block.json` = 恰 **2** 个成员（`farmstorm:cultivator` + `farmstorm:crop_converter`）；镐标签含 `farmstorm:crop_converter`（列表第 1 项） | **通过** | `data/minecraft/tags/block/mineable/pickaxe.json`（1.21.1 的实际路径）。**差异登记见 §3 第 2 条** |
| **#31** | 数 `src/main/java` 的 `*.java` + 查四个类是否存在 + 客户端启动日志 | `.java` = **152**；`ConverterMenu.java` / `ConverterScreen.java` / `ConverterRecipeBookCategory.java` / `SeedItem.java` **四个都不存在**；客户端 WARN 未取（未起图形客户端） | **通过（第 3 段未验证）** | 全树 glob + 文件名核对；**未验证子项**：`Unknown recipe category` WARN 需图形客户端；替代读数 = `FarmstormClient.java:75-76` 已内联 `registerRecipeCategoryFinder(farmstorm:converting, … → RecipeBookCategories.UNKNOWN)`，即 21 条配方都有归属分类 |
| **#32** | 老存档读档：用真实 `loadAdditional` 喂 0.2.2 形态的 NBT（有 `Cultivators`、无 `CultivatorKinds`）；再用 `/setblock` 试旧 id | 探针原文 `case1(0.2.2, 缺键) positions=3 kinds=[CULTIVATOR, CULTIVATOR, CULTIVATOR]`（3 条绑定全在、全按培养器解、**无 WARN**）⇒ 线仍是青色；`/setblock 20 -60 8 farmstorm:converter` ⇒ `Unknown block type 'farmstorm:converter'`（旧方块位置解析成 air）；现网 link 包里培养器那条 kind 字节 = `00` | **通过（端到端存档回放未做 = 子项未验证）** | `logs/f309-nbtprobe.log`（本轮 03:26:28 重跑；`probe/NbtProbe.java` 反射调真实 `loadAdditional`）；`OutpostCoreBlockEntity:964-988`（缺键补 `CULTIVATOR`、键存在才告警）。**未验证子项**：仓库内没有 0.2.2 的实体存档文件，未做「拷一份老存档 → 起服读档」的端到端回放 |

## 3. 差异与不一致（只登记，不改上游）

1. **#8 的「`[converter]` 段仍在」不成立**：实测重写后该段被整段删掉（首行 `[stack]`、803 行），只有 `maxPerPlayer = 2` / `cultivatorCapacity = 16` 两个**值**被保留。这与 54 号 §6.4「三条纪律」第 3 条（老 `[converter]` 三项在重写时被删掉）一致，但与 #8 自己的期望句矛盾。判据主干（老文件不回写默认值）成立，故记「期望中的红 · 通过」。
2. **#30 的标签名是 1.20 时代的写法**：54 号写 `minecraft:mineable_with_pickaxe`，1.21.1 的实际产物是 `data/minecraft/tags/block/mineable/pickaxe.json`（`mineable/pickaxe`）。内容判据（含 `crop_converter`）成立。
3. **#11 的 `unlockedBy` 判据措辞偏差**：两个合成 JSON 里**没有 `unlockedBy` 字段**，不是「有但用了标签」。结论（不是 `#farmstorm:` 标签）仍然成立。
4. **#22 的 `textures/gui/` 是「目录存在但 0 文件」**：读数 0 指文件数，不是目录不存在（`src/main/resources/assets/farmstorm/textures/gui` 存在且为空）。
5. **54 号 §6.4 的「盘上的老 toml 是 2 / 16」已过期**：实测 `run/config/farmstorm-server.toml` 现在是新格式（803 行、`[crop_converter]`、4 / 24）。要拿 2 / 16 的旧档，只能从 `run-v023/config/farmstorm-server-1.toml.bak`（`[converter]` + 4 / 24）构造；本轮据该 .bak 生成 `logs/old-format-2-16.toml` 作为 #8 的输入，并在记录里标明是构造件。
6. **#2 的结论原文是 `结论：FAIL 0 项，WARN 1 项`**（不是只有 `FAIL 0 项`）。那条 WARN 是 `trade.*` 六个键的「文档逻辑路径 → 落盘拼写」形状归一，属已登记的文档-源码偏差，不影响 175 / 175。
7. **#25 的「F3 实体列表」不可读**：mineflayer 不认识模组实体 id（`e.name` 恒为 `unknown`），客户端实体计数恒 0，不能当判据；以服务端 `execute if entity` 为准（消失后恒 `Test failed`）。
8. **#20 的「等待」路径未复现出 age=3**：`randomTickSpeed 200` 跑 60 s，服务端逐次断言（age=0/1/2/3 + air）全部 `Test failed`，随后该格被挖空；骨粉路径（第 1 次 → age=2、第 2 次 → age=3）成立。判据本身（能种、能长到 age=3、成熟掉 1 + 0~3）由骨粉路径取证。**疑似观察面问题而非缺陷**：该次 `randomTickSpeed` 只在生长循环期间生效，且同区块另有 54 格核内物品与多台机器在 tick，未进一步定位。
9. **#15 的「不是方块本体两份」**：`/setblock … air` 走 `onRemove`，只交出战利品表之外的那一件输入物；方块本体掉落由战利品表另行判定，本轮读数里不含方块掉落，故这句话只能算「输入物只掉一份」成立。
10. **#13 / #16 的计时精度**：RCON 轮询粒度 0.1~0.5 s，实测 599 刻（#13）、腾格后 ≤1 刻投递（#16）。与「600 刻 = 30 秒」「下一 tick 完成投递」在刻级判据内一致，但不是逐刻采样。
11. **#4 / #26 的「一字不动 / 零改动」无法用 git 取证**（本轮纪律不许跑 git）：改用「函数体不解析该 5 键」「文件 mtime 早于本版窗口 + 行数 + 结构不变」三条可复核读数。

## 4. 附录：原文摘录

### 4.1 #2 检查器输出（`logs/f309-verify.log`）

```text
== 1. config：文档键集 vs 生成的 toml ==
  54-TECH §6.1 / §6.2：5 行（增 2 / 删 3；v0.2.3 的 §6.1 一张表 + §6.2 一张表分别现算，其中 0 个键名与前面的清单重名）
  剔除已删键 5 个：converter.netherStarForwardCost、converter.netherStarOnReverse、converter.xpCost、farmland.limitPerPlayer、outpost_core.farmlandBonus
  文档口径键数: 175（… 按并集去重后 180，再扣掉 5 个已删键 = 175）
  生成 toml 键数: 175
  [WARN] 6 个键在文档里是逻辑路径、落盘是组内拼写（形状归一后才对上，已登记为文档-源码偏差）: [trade.common.max -> trade.commonMax, …]
  [OK]   175 个配置键与文档逐字一致（toml 175 键）
== 2. 92 个注册 ID 逐字核对（全源码树） ==
  [OK]   items: 28 个 ID 齐全 …（12 类逐类 OK，此处省略中间 10 行，全文见日志）
  已核 registry 对象: 92 / 92
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 321 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch

结论：FAIL 0 项，WARN 1 项
```

### 4.2 #23 的 8 个 variant（`blockstates/crop_converter.json` 原文）

```text
facing=east,working=false   -> farmstorm:block/crop_converter          y=90   uvlock=true
facing=east,working=true    -> farmstorm:block/crop_converter_working  y=90   uvlock=true
facing=north,working=false  -> farmstorm:block/crop_converter          y=0    uvlock=true
facing=north,working=true   -> farmstorm:block/crop_converter_working  y=0    uvlock=true
facing=south,working=false  -> farmstorm:block/crop_converter          y=180  uvlock=true
facing=south,working=true   -> farmstorm:block/crop_converter_working  y=180  uvlock=true
facing=west,working=false   -> farmstorm:block/crop_converter          y=270  uvlock=true
facing=west,working=true    -> farmstorm:block/crop_converter_working  y=270  uvlock=true
```

### 4.3 #24 的 link 包原文（客户端实收，`farmstorm:cultivator_link`，8 字节大端 long = BlockPos.asLong）

```text
拆培养器之前（2 条）：02 | 0000020000008fc4 (8,-60,8)  | 0000028000008fc4 (10,-60,8) | 00
                          0000020000008fc4 (8,-60,8)  | 0000030000008fc4 (12,-60,8) | 01
换放转换器之后（2 条）：02 | 0000020000008fc4 (8,-60,8)  | 0000028000008fc4 (10,-60,8) | 01  <- 唯一变化的字节
                          0000020000008fc4 (8,-60,8)  | 0000030000008fc4 (12,-60,8) | 01
```

### 4.4 #1 与 #5 的自检行（`logs/f309-session1.log` 原文）

```text
[03:12:55] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 92 个：items 28 / blocks 28 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK
[03:12:55] [modloading-sync-worker/INFO] [farmstorm/]: 自检 网络包注册 farmstorm:core_list（版本 3，S2C 只读，MAX_CORES 64）→ OK
[03:12:55] [modloading-sync-worker/INFO] [farmstorm/]: 自检 网络包注册 farmstorm:cultivator_link（版本 3，S2C 只读，MAX_LINKS 1024）→ OK
```

## 5. 不改码取证（不跑 git）

| 取证方式 | 读数 |
|---|---|
| `src/`、`tools/`、`docs/` 中 mtime 晚于 **2026-11-06 03:07:00**（本轮开工时刻）的文件 | **0 个** |
| 上述三棵树里 mtime 最新的文件 | `src/main/java/…/Farmstorm.java` 03:02:29、`tools/verify_project.py` 03:02:25 —— 都是 F-308 关窗时的改动，早于本轮 |
| 本轮**写**过的位置 | 只有 `.work/v023-verify/`（`acceptance.md`、`f309-probe*.js`、`logs/*`）与 `run-v023/`（config + world） |
| `.gitignore` | 已含 `run/`、`run-*/`、`build/`、`.tools/`、`.scratch/` ⇒ 起服产生的 config / world 变更不进版本库 |

## 6. 交付物

```text
.work/v023-verify/acceptance.md            本记录
.work/v023-verify/f309-probe.js           探针 v3（转换器 / 培养器 / 连线 / 核满）
.work/v023-verify/f309-probe4.js          探针 v4（腾格投递 / 炮弹 / reload）
.work/v023-verify/f309-probe5.js          探针 v5（平凡作物种植与掉落）
.work/v023-verify/logs/f309-build.log     gw.cmd build 输出
.work/v023-verify/logs/f309-verify.log    verify_project.py 输出
.work/v023-verify/logs/f309-session1.log  起服①（新 config，含 #1/#5 自检行与 #24 的刻号）
.work/v023-verify/logs/f309-session2.log  起服②（老 toml，含 #8 与 /reload）
.work/v023-verify/logs/f309-session3.log  起服③（#20 复测）
.work/v023-verify/logs/f309-probe2.log    #13 #14 #17 #18 #19 首轮
.work/v023-verify/logs/f309-probe3.log    #13 #15 #14 #24 #16 #29 #32
.work/v023-verify/logs/f309-probe4.log    #16 腾格 / #25 / #12
.work/v023-verify/logs/f309-probe5.log    #20
.work/v023-verify/logs/f309-nbtprobe.log  #32 读档探针
.work/v023-verify/logs/old-format-2-16.toml, old-format-4-24.toml, pre-f309-new-format.toml   #8 的输入与对照
.work/v023-verify/logs/f309-rcon.py       临时 RCON 查询器
```
