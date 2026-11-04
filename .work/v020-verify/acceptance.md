# v0.2.0 Phase 39 · 36 号 §8 的 36 条验收记录

> 记录者：Phase 39 唯一执行者（F-244）。判据原文以 `docs/zh/36-TECH-0.2.0.md` §8 为准，本文不复述全文，只摘断言要点。
> 结论取值：`通过（实测）` / `通过（代码层证据）` / `未验证（原因）`。**未验证 ≠ 失败**，但也不当作通过。
> 服务端环境：`run-p39`（`gw.cmd runServer -PfsRunDir=run-p39`），启动日志 `run-p39/logs/latest.log`；RCON 场景见 `.work/p39-scenario.log` / `p39-scenario2.log` / `p39-scenario3.log`。

| # | 断言（要点） | 观察位置 | 实际值 | 结论 |
|---|---|---|---|---|
| 1 | 注册对象 97：12 类分项全行 `→ OK` | `run-p39/logs/latest.log`（Farmstorm.java:151-155 格式串） | `[04Nov2026 11:29:26.250] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK` | 通过（实测） |
| 2 | `@Mixin` 0 / `*.mixins.json` 0 / `playToClient` 2（`core_list`+`cultivator_link`） | 源码 grep + 启动日志两条网络包自检 | `@Mixin` src 命中 0；`mixins.json` 文件 0；`FarmstormNetwork.java` 的 `playToClient` 2 处、`playToServer|sendToServer` 0；`CustomPacketPayload` 实现 2 个文件（CoreListPayload / CultivatorLinkPayload）；日志两行 `自检 网络包注册 farmstorm:core_list（版本 2 …）→ OK`、`… farmstorm:cultivator_link（版本 2，MAX_LINKS 1024）→ OK` | 通过（实测） |
| 3 | 22 条档案 `tier()` 逐条 = 35 号 §2.2；普通 11 / 稀有 7 / 史诗 4 | `CropProfile.java` + 启动自检行 | 启动自检：`自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`；`CropProfile` 22 条档案、三档聚合读数与 35 号 §2.2 一致。逐条 id ↔ 档位对表本轮**未重做**（F-214 提交时做过，本轮只验聚合与派生面） | 通过（实测·聚合；逐条对表未重做） |
| 4 | 启动自检新增 `作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` | `run-p39/logs/latest.log` | `[04Nov2026 11:29:26.248] … 自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` | 通过（实测） |
| 5 | 准入面内「产 2+2」5 条、「只产 2」13 条（5+13=18） | 自检行 + 35 号 §6.4 + `CultivatorBlockEntity.hasOwnSeed` | `hasOwnSeed(CropProfile)` 按档案派生，不写第二份 id 清单；实测抽 `puffed_wheat`（5 条之一）产出 = 作物 ×2 + 种子 ×2（见 #21）。13 条「只产 2」本轮未逐条生成验证 | 通过（实测 1 条 + 代码层派生） |
| 6 | 手持 22 个作物之一右键耕地：方块不出现、手持物不减、无提示 | 手动 / `V-01`（需客户端） | 无客户端，未做。代码层：`ModifiedCropItem#isPlantableSoil` 恒 `false`（`FarmstormItems` 侧无第二条放置入口） | 未验证（无客户端） |
| 7 | 手持 7 个种子之一右键耕地：同上 | 手动 / `V-02`（需客户端） | 无客户端，未做。代码层：`SeedItem#useOn` 覆写返回 `PASS` | 未验证（无客户端） |
| 8 | 手持 7 个部署型之一右键**非耕地**顶面：正常放置 | 手动 / `V-03`（需客户端） | 无客户端，未做。代码层：`DeployableCropItem#useOn` 判据恒 `false` 后回落 `super.useOn`（陷阱 24：右键耕地会摆出部署形态） | 未验证（无客户端） |
| 9 | `onRightClickBlock`/`onFarmlandTrample` 注册行与方法体全注释；`:171 backfill` 单行注释 | 源码 grep | `FarmstormGameEvents.java`：注册行 `:74`（onServerTickPost）仍在、`:76` 理由行 + `:77 // gameBus.addListener(…::onRightClickBlock);` 注释态；`ledger.bind` 命中行 `:267`（在 `//` 注释块内）；`protectsFromTrample` 命中行 `:292`（注释态）；`backfill` 命中行 `:181`（注释态）。行号相对 36 号原文有漂移（+2~+10），行为判据成立 | 通过（实测） |
| 10 | `onServerTickPost` 仍在；`CoreChunkTickets.restoreAll` 与 `CoreListBroadcaster.tick` 都在 | 源码 + 运行日志 | `onServerTickPost` 出现在 `:74`（注册）与 `:175`（方法）；`restoreAll` 在 `:179`、`CoreListBroadcaster.tick` 在 `:185`；日志 `核心区块票补票：0 张（farmstorm_core，distance=1，来源 OutpostCoreIndex）`、老档 `核心区块票补票：2 张` | 通过（实测） |
| 11 | `[outpost_core]` 无 `farmlandBonus`，有 `cultivatorCapacity = 16` 与 `storageSlots = 54` | `run/config/farmstorm-server.toml` | `[outpost_core]` 4 键：`maxPerPlayer = 2` / `boundaryParticleIntervalTicks = 40` / `storageSlots = 54` / `cultivatorCapacity = 16`；全文件 `farmlandBonus` 命中 0 | 通过（实测） |
| 12 | `FarmstormConfig` 字段声明仍在、定义已注释、注释写明 NPE 前提 | 源码 | 声明在 `:231`（`outpostCoreFarmlandBonus`，第一处命中）；定义在 `:932`（第二处命中，`//` 注释态）；`:227-229` 三行理由注释含「定义已注释 ⇒ 字段运行期恒为 null；恢复监听器前必须先恢复定义」。36 号原文写 `:227`/`:909-912`，实为 `:231`/`:932`（行号漂移，已登记） | 通过（实测） |
| 13 | 3 条耕地语言键都不存在；`message.farmstorm` 归 0 | `zh_cn.json` / `en_us.json` | `outpost_core.farmlandBonus` zh 0 / en 0；`message.farmstorm.farmland.no_core` 0 / 0；`message.farmstorm.farmland.quota_full` 0 / 0；`message.farmstorm` 前缀 0 / 0 | 通过（实测） |
| 14 | `FarmlandLedger` 555 行 / `FarmlandGuard` 225 行；`farmstorm_farmland.dat` 升级后仍在且 mtime 不变 | 源码 + 老档副本 `run-p39old/world/data/` | `FarmlandLedger.java` LF 555 / `FarmlandGuard.java` LF 225；老档副本升级前 mtime `2026-11-02 02:55:28`、size 274、SHA256 `94A88F5F…B24572`；该副本起服一次（`Done (…)!`）并停服后：mtime/size/SHA256 **三项不变**，同目录 `neoforge_data_attachments.dat`、`raids.dat` 已被重写为 `2026-11-04 11:53:12`（证明服务端确实写过 world/data，只是没碰这个文件） | 通过（实测·F-233 之后） |
| 15 | 农民交易 3 项、位次 1/3/5、每项 `maxUses = 1` | 游戏内界面 / `/data get entity … Offers` | RCON `summon minecraft:villager … {VillagerData:{profession:"minecraft:farmer",level:1}}` 后 `data get … Offers` = `Found no elements matching Offers`（原版在生成报价前不写 Offers，summon 不触发报价生成）⇒ 本环境无法读出。代码层：`TierTrade` 三个位次 1/3/5、`maxUses = 1` | 未验证（无客户端交互）+ 代码层 |
| 16 | 成交一次后该项仍在、变灰；补货后价格与 id 不变 | 游戏内连续两次观察 | 无客户端，未做。代码层：报价在生成时一次性抽定（不在成交时重掷），`priceMultiplier = 0.0F` | 未验证（无客户端） |
| 17 | 首次报价基准价落 [2,5] / [10,32] / [40,64] | `Offers.Recipes[0].buy.count` | 同 #15：`Offers` 未生成，读不到。代码层：三档区间取自 `trade.*.min/max`（toml 实测 2/5、10/32、40/64） | 未验证（无客户端）+ 代码层 |
| 18 | `working=false` 用静态模型、`working=true` 用帧条模型 | `blockstates/cultivator.json` + 两个模型 json | `blockstates/cultivator.json` = `{"variants":{"working=false":{"model":"farmstorm:block/cultivator"},"working=true":{"model":"farmstorm:block/cultivator_working"}}}`（恰 2 个 variant）；`cultivator.json` 的 side/top 纹理是 `_still` 两张；`cultivator_working.json` 的 side/top 是帧条两张 | 通过（实测） |
| 19 | `*.png.mcmeta` 恰 2 个（side/top），`frametime 2` / `interpolate false` / `frames 0..15`；`_still` 无 mcmeta | 文件系统 | `textures/**/*.png.mcmeta` 计数 2：`cultivator_a_side.png.mcmeta`、`cultivator_a_top.png.mcmeta`；两份内容逐字相同：`{"animation":{"frametime":2,"interpolate":false,"frames":[0..15]}}`；`cultivator_a_side_still.png` / `cultivator_a_top_still.png` 无 mcmeta；`textures/block/cultivator_a_*` 共 5 张 PNG | 通过（实测） |
| 20 | 空槽不动；放入后 1.6 秒一轮；600 刻后停并清槽 | 游戏内观察 + `V-04` | 客户端观感未验证。实测到的时序面：`Progress` 从 595 走到 **600** 后停住（见 #22），投递后回 0 且 `Held` 清空（见 #21/#22）；「动画在空闲态是否播放」属渲染观感 | 未验证（渲染观感）+ 实测时序 |
| 21 | 放入普通作物：600 刻后核 54 格 +2 作物；5 条之一再加 +2 种子 | 游戏内 + `/data get block <core>` 的 `Items` | 构造绑定态（核侧 `Cultivators=[54975582208200L]` + 培养器 `Core=55800215929032L`）后设 `Held={farmstorm:puffed_wheat}, Progress=590`；9 秒后培养器 `{Progress:0, Core:…, 无 Held}`，核 `Items` = `[{count:2, Slot:0b, id:"farmstorm:puffed_wheat"}, {count:2, Slot:1b, id:"farmstorm:puffed_wheat_seeds"}]` | 通过（实测） |
| 22 | 核 54 格塞满后停在完成态：不落地、不丢；清出空间后自动投递 | `/data get block <cultivator>` 的 `Progress` | 核 54 格全填 `minecraft:stone`，培养器 `Held=puffed_wheat, Progress=595`；6 秒后 `Progress` = **600**、`Held` 仍在；再 4 秒 `Progress` 仍 **600**，`@e[type=minecraft:item]` 无实体（不落地、不丢） | 通过（实测） |
| 23 | 手持史诗作物或史诗种子右键培养器：无反应、手持物不减 | 手动 / `V-05` | 无客户端，未做。代码层：`CultivatorBlockEntity.admissible` = `profile != null && tier() != CropTier.EPIC` | 未验证（无客户端）+ 代码层 |
| 24 | 己方空手右键核打开 54 格界面，标题「据点核心」；存入后关掉再开仍在 | 游戏内 `V-06` | 无客户端，未做。代码层：`SLOT_COUNT = 54`、`menu_type` 注册数 2（converter + outpost_core）、`container.farmstorm.outpost_core` 语言键存在 | 未验证（无客户端）+ 代码层 |
| 25 | 两道闸：非己方不开；离核 9 格打不开、8 格内开界面后走到 9 格被 `stillValid` 关掉 | 双人客户端 `V-07` | 无第二客户端，未做。代码层：`OutpostCoreBlockEntity.isAlly` 走 `DeployedOwnership.isAlly`，开箱条件 = 己方 ∧ 原版 `stillValid` | 未验证（需第二客户端）+ 代码层 |
| 26 | 挖掉核心：54 格物品全掉；活塞推动 / 同方块替换 / 区块卸载都不掉 | 游戏内四种场景 `V-08` | 真移除（`setblock <pos> minecraft:air destroy`）掉落清单：`Outpost Core ×1`、`Stone ×62`、`Stone ×2`（合计 64）、`Diamond ×3`、`Puffed Wheat ×5` —— 核内容 3 格全部落地；活塞推动 / 同方块替换 / 区块卸载三种场景在 RCON 下不可构造（`/setblock` 同状态被拒：`Could not set the block`），未验 | 通过（实测·真移除）+ 其余三种 未验证 |
| 27 | 拆核时另有「覆盖 + 己方 + 有空位」的核：改绑成功，槽内容与进度保留 | 游戏内 + `/data get block <cultivator>` 的 `Core` | 构造两个同 owner 的核（230 / 233）后拆 230：培养器 231 的 `Core` 键**消失**、233 的 `Cultivators` 仍为 `[]` —— 候选核来自 `OutpostCoreIndex`，而 `/setblock` 放的核从不进索引，故本环境走不到「有接替核」分支；不是实现判红，是构造限制 | 未验证（构造态无索引项）+ 代码层 |
| 28 | 拆核且无接替核：留原地、`Core` 键消失、`Held`/`Progress` 保留、右键无反应 | 游戏内 + `/data get block` | 拆核前培养器 221 = `{Progress:163, Held:{count:1,id:"farmstorm:power_beet"}, Core:61022896160968L}`；拆核后 = `{Progress:203, Held:…, 无 Core}`；`data get block … Core` = `Found no elements matching Core`；之后 1.5 秒内进度不再增长（停摆）、地上无掉落 | 通过（实测） |
| 29 | 培养器所在区块不在内存：NBT 一个字节都不变（`Core` 仍是旧核坐标） | 停服后读 region `V-09` | 未做（需停服读 region 文件比对，本轮时间预算内未执行）。代码层：`settleOnRemoval` 体内 `getChunk(` 命中 **0**、`getChunkNow` 3 处、`minecraft:dirt` 0、`setBlock` 0 | 未验证（需停服读 region）+ 代码层 |
| 30 | `settleOnRemoval` 方法体内 `setBlock` 命中 = 0 | 源码 grep（方法体范围） | `settleOnRemoval` 行范围 `668-713`（括号配对切出）；`setBlock` 0 命中、`getChunk(` 0 命中、`getChunkNow` 3 命中、`minecraft:dirt` 0 命中 | 通过（实测） |
| 31 | 站在自家核旁：核与每个已绑定培养器之间有可见线框；出范围后线消失 | 游戏内 `V-10` | 无客户端，未做。代码层：`CultivatorLinkRenderer` 用 `LevelRenderer.renderLineBox`；`ClientCultivatorLinks` 只在客户端 | 未验证（渲染观感）+ 代码层 |
| 32 | 拆掉培养器：对应的线立刻消失（不等 20 刻重扫） | 游戏内 | 无客户端，未做。代码层：`CultivatorBlockEntity` 的移除路径调 `CultivatorLinkBroadcaster.requestAll(...)`（`:293`），`OutpostCoreBlockEntity` 三处（`:361`/`:374`/`:496`） | 未验证（渲染观感）+ 代码层 |
| 33 | 客户端连续 10 分钟在线，日志 0 条 `Missing elements in vertex` / `IllegalStateException` | 客户端日志 | 无客户端，未做（本轮只起过专用服务端） | 未验证（无客户端） |
| 34 | 把 `ClientCultivatorLinks` 的 handler 临时改成空实现后，绑定/产出/迁移/拆核掉落照常 | 断点式验证 `V-11` | 未做（F-244~F-246 不许改码，改 handler 属于改码）。代码层：服务端对连线的引用只有 `CultivatorLinkBroadcaster.requestAll(...)` 4 处**单向请求**（返回值未使用），`playToServer|sendToServer` 0 命中 ⇒ 服务端逻辑不读客户端回包 | 未验证（需改码，与本 Phase 铁律冲突）+ 代码层 |
| 35 | 池断言 `poolSize == 11` 且日志 `→ OK`；「不含 lucky_berry / wild_apple / compressed_puffed_wheat」另半条仍在 | `latest.log` | `自检 幸运浆果池 11 种（按 tier() 只留 COMMON，不含 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK`；代码 `Farmstorm.java:140-145`：`poolOk = poolSize == 11 && LUCKY_BERRY_POOL.stream().noneMatch(...)` | 通过（实测） |
| 36 | 手持种子右键转换器：无界面、无提示；转换器方块、创造栏、26 条 `converting` 配方仍在 | 游戏内 + 资源目录计数 | 右键面未验证（无客户端）。资源面实测：`data/farmstorm/recipe/*.json` 共 **29**，其中含 `farmstorm:converting` 的 **26** 条；`FarmstormBlocks` 里 converter 仍在注册面（97 的一部分） | 未验证（右键）+ 实测（资源计数） |

## 汇总

| 项 | 读数 |
|---|---|
| 行数 | **36** 行 + 表头 |
| 通过（实测，含聚合） | **18**（#1 #2 #3 #4 #5 #9 #10 #11 #12 #13 #14 #18 #19 #21 #22 #28 #30 #35，#26 的真移除部分计入） |
| 通过（代码层证据为主） | 见各行「未验证 + 代码层」，不单列 |
| 未验证 | **13**（#6 #7 #8 #16 #20 #23 #24 #25 #29 #31 #32 #33 #34） |
| 部分未验证 | **5**（#15 #17 #26 #27 #36 —— 有实测面也有未验证面） |
| **未通过（判红）** | **0** |
| 手动场景截图 | **0 / 13**（#6~#8 三张、#18~#20 三张、#23~#26 四张、#31~#34 四张）—— 本轮无图形客户端，**未达成**，如实登记 |

## 未做的与做不了的

- 全部「需人眼看画面 / 需听感 / 需第二客户端 / 需真人右键」的条目一律记「未验证 + 代码层证据」，没有编读数（任务书 §4.4）。
- 截图 0 张：本环境只有专用服务端 + RCON，无 `runClient`。36 号 §8 的截图要求（≥13 张）**未达成**。
- 交易三件（#15~#17）在 RCON 下读不到 `Offers`：`summon` 不触发生成报价，需要真人右键。
