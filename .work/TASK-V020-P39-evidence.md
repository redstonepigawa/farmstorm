# Phase 39（F-241 ~ F-246）过程证据

> 执行者：farmstorm v0.2.0 Phase 39 唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> 铁律：F-241 / F-242 / F-243 改文件；F-244 ~ F-246 一律不改码，只产出记录（`.work/v020-verify/` 三份）。

## 0. 机器读数（本 Phase 全部实测的入口）

| 项 | 值 |
|---|---|
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build` → **BUILD SUCCESSFUL in 14s**，退出码 **0**（`.work/p39-build.log`） |
| datagen | `runData` → **BUILD SUCCESSFUL in 31s**，退出码 **0**，`Caching: total files: 222, old count: 222, new count: 223, removed stale: 0, written: 0`（lang 已被前序 Phase 写到位，本轮不产生 diff） |
| 服务端 | `run-p39`（`-PfsRunDir=run-p39` + `FS_RUN_LOCK=run-p39`），`Done (29.008s)!`，RCON `0.0.0.0:25575`；老档副本 `run-p39old`（`server-port=25566` / `rcon.port=25576`） |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`，`PYTHONIOENCODING=utf-8` |

## 1. F-241 · 语言键总账 337 与类注释

### 1.1 改前实测（陷阱 20）

```
zh total 337 en total 337
zh_only 0 en_only 0
```

### 1.2 改后逐前缀读数（`ConvertFrom-Json` 等价的 python 键集，`.work/p39_lang.py`）

```
advancements 36 / block 27 / command 3 / container 2 / death 6 / effect 4 / entity 13
farmstorm.configuration 178 / gui 9 / item 33 / itemGroup 1 / key 2 / subtitles 3 / tooltip 20
message 0（桶不存在）  合计 337
```

与 36 号 §4.3.3 的 `178 + 36 + 33 + 27 + 20 + 13 + 9 + 6 + 4 + 3 + 3 + 0 + 2 + 1 + 2 = 337` 逐桶一致。

### 1.3 `ModLangProvider.java:13-16` 改前 → 改后（`git diff` 原文）

```diff
@@ -10,10 +10,10 @@ import net.neoforged.neoforge.common.data.LanguageProvider;
- * 不存在「两份键各自维护」的可能。**条目构成（v0.1.1 / P30 实测，改表时必须同步本段）**：
- * 32 item + 26 block + 4 effect + 3 subtitles + **169** config（= 166 server + 3 client）+ 36 advancement + 6 death
- * + 16 tooltip + 13 entity + 9 gui + 1 container + 1 itemGroup + 2 message + 2 key + 3 command
- * = **323**。（P29 时是 314：同表但 config 166 / gui 8、无 key / command 两组。）
+ * 不存在「两份键各自维护」的可能。**条目构成（v0.2.0 / P39 实测，改表时必须同步本段）**：
+ * 33 item + 27 block + 4 effect + 3 subtitles + **178** config（= 175 server + 3 client）+ 36 advancement + 6 death
+ * + 20 tooltip + 13 entity + 9 gui + 2 container + 1 itemGroup + 0 message + 2 key + 3 command
+ * = **337**。（P29 时是 314：同表但 config 166 / gui 8、无 key / command 两组。）
```

- `git diff --stat`：`ModLangProvider.java | 8 ++++----`（1 file changed, 4 insertions(+), 4 deletions(-)）—— **只动这 4 行**。
- `323` 在该文件命中 **0**；`337` 命中 **1**。
- 两份 `lang/*.json` 本轮 datagen `written: 0`：产物已由 F-215 / F-221 / F-222 / F-224 / F-227 / F-234 写到位，本轮无 diff。

## 2. F-242 · config 总账 175 键 / 60 组

### 2.1 改前实测（陷阱 20）

```
run/config/farmstorm-server.toml  段 58 / 键 166
FarmstormConfig.java 的 b.push( 计数 = 60
```

### 2.2 操作路径（本版唯一允许动 `run/config` 的一处）

1. `Remove-Item run/config/farmstorm-server.toml` —— 旧文件删除（`deleted run/config/farmstorm-server.toml`）。
2. 起服让 NeoForge 重生成：`New-Item run-p39` + 复制 `run/eula.txt` / `run/server.properties` → `gw.cmd runServer -PfsRunDir=run-p39`（**不在 `run/` 里起服**）。
3. 生成物落在 `run-p39/config/farmstorm-server.toml`（20113 B），核到 60 / 175 后复制回 `run/config/farmstorm-server.toml`。

### 2.3 改后读数

```
run/config/farmstorm-server.toml  段 60（顶层 36 + 嵌套 24）/ 键 175
[outpost_core] -> 4 keys: maxPerPlayer = 2 / boundaryParticleIntervalTicks = 40 / storageSlots = 54 / cultivatorCapacity = 16
[cultivator]   -> 2 keys: cycleTicks = 600 / yield = 2
[trade]        -> 6 keys: commonMin = 2 / commonMax = 5 / rareMin = 10 / rareMax = 32 / epicMin = 40 / epicMax = 64
farmlandBonus hits: 0     hud hits: 0
doc groups missing in toml: []    mismatches: []    sum toml keys: 175
FarmstormConfig.java b.push( = 60
run-p38c/config/farmstorm-client.toml  keys = 3  segs = 1（hud.enabled / hud.x / hud.y，未动）
```

36 个组逐组对 36 号 §4.1 全对（脚本 `.work/p39_groups.py`，输出 `mismatches: []`）。

## 3. F-243 · 验证工具四处同步

### 3.1 改前（A-66 的假绿，作为「改前实测值」登记，**不作为判据**）

```
  30-TECH §4.1：58 组 / 166 键
  生成 toml 键数: 166
  [OK]   166 个配置键与文档逐字一致（toml 166 键）
  已核 registry 对象: 93 / 93
  [OK]   lang 键集一致，共 337 键
结论：FAIL 0 项，WARN 0 项        退出码 0
```

`93 / 93` 与 `FAIL 0` 同时出现 ⇒ 4 个新对象它根本不看。

### 3.2 四处同步（`git diff --stat`：`tools/verify_project.py | 130 +++++++++++++++++----`，113 插入 / 25 删除）

| 处 | 改法 |
|---|---|
| ① `EXPECTED` | `items` +`cultivator`、`blocks` +`cultivator`、`block_entity_type` +`cultivator`、`menu_type` +`outpost_core`（4 个新 id） |
| ② `EXPECTED_COUNTS` | `items 32→33` / `blocks 26→27` / `block_entity_type 7→8` / `menu_type 1→2`，其余 8 类不动（合计 93→97） |
| ③ config 期望来源 | 新增 `expected_config_keys_v012()`：从 **36-TECH §4.1.1 的 11 行表现算**（第 2 列键路径 + 第 6 列「增/删」），并入 main() 的并集；删键由文档现算（`removed6`），**没有第二份手写清单** |
| ④ 打印串 | `== 2. 97 个注册 ID 逐字核对`、`已核 registry 对象: {total} / 97`、组数不符的报错改指 36-TECH §1.2 |

### 3.3 改后四段完整原文（`python -X utf8 tools/verify_project.py`，退出码 **0**）

```text
== 1. config：文档键集 vs 生成的 toml ==
  30-TECH §4.1：58 组 / 166 键（v0.1.1 的 58 行数据表现算，其中 165 个键名已在前四份清单里）
  36-TECH §4.1.1：11 行（增 10 / 删 1；v0.2.0 的 11 行数据表现算，其中 0 个键名与前面的清单重名）
  剔除已删键 2 个：farmland.limitPerPlayer、outpost_core.farmlandBonus
  文档口径键数: 175（01-DESIGN §3.8 117 + 12-TECH §8.1 22 + 18-TECH §8.1 19 行 + 24-TECH §8.1 12 行（新 11 + 改 1） + 30-TECH §4.1 166 键 + 36-TECH §4.1.1 10 键；其中 24 / 18 号另有 3 行是改既有默认值、§4.1 另有 165 个键名与前面的清单重名；按并集去重后 177，再扣掉 2 个已删键 = 175）
  生成 toml 键数: 175
  [WARN] 6 个键在文档里是逻辑路径、落盘是组内拼写（形状归一后才对上，已登记为文档-源码偏差）: ['trade.common.max -> trade.commonMax', 'trade.common.min -> trade.commonMin', 'trade.epic.max -> trade.epicMax', 'trade.epic.min -> trade.epicMin', 'trade.rare.max -> trade.rareMax', 'trade.rare.min -> trade.rareMin']
  [OK]   175 个配置键与文档逐字一致（toml 175 键）
== 2. 97 个注册 ID 逐字核对（全源码树） ==
  [OK]   items: 33 个 ID 齐全
  [OK]   blocks: 27 个 ID 齐全
  [OK]   block_entity_type: 8 个 ID 齐全
  [OK]   entity_type: 13 个 ID 齐全
  [OK]   mob_effect: 4 个 ID 齐全
  [OK]   sound_event: 3 个 ID 齐全
  [OK]   recipe_type: 1 个 ID 齐全
  [OK]   recipe_serializer: 1 个 ID 齐全
  [OK]   creative_mode_tab: 1 个 ID 齐全
  [OK]   damage_type: 3 个 ID 齐全
  [OK]   data_component_type: 1 个 ID 齐全
  [OK]   menu_type: 2 个 ID 齐全
  已核 registry 对象: 97 / 97
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 337 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch

结论：FAIL 0 项，WARN 1 项
```

**`97 / 97` 与 `FAIL 0` 同一次输出、同时出现**（A-66 的口径）；`175 个配置键与文档逐字一致`、`共 337 键` 也在这四段里。

### 3.4 活判据（判据自己会红）

```
EXPECTED_COUNTS["blocks"] = 28  ->  [FAIL] blocks: 清单 27 个 ID，但 36-TECH §1.2 的 v0.2.0 列是 28 个；结论：FAIL 1 项，WARN 1 项；EXIT=1
改回 27                        ->  [OK]   blocks: 27 个 ID 齐全；结论：FAIL 0 项，WARN 1 项；EXIT=0
```

`git diff --stat -- docs/` → 空（工具只读上游，不回改）。

## 4. F-244 · 36 条验收

产物：`.work/v020-verify/acceptance.md`（36 行 + 表头）。

| 项 | 读数 |
|---|---|
| 行数 | 36 |
| 通过（实测，含聚合） | 18 |
| 未验证（无客户端 / 需第二客户端 / 需停服读 region / 需改码） | 13 |
| 部分未验证（有实测面 + 未验证面） | 5 |
| 未通过（判红） | **0** |
| 手动场景截图 | **0 / 13 —— 未达成**（本环境只有专用服务端 + RCON，无 `runClient`） |

#1 的日志原文（与 36 号 §8 #1 逐字一致）：

```
[04Nov2026 11:29:26.250] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK
```

## 5. F-245 · 40 条边界

产物：`.work/v020-verify/boundaries.md`（40 行 + 表头）。

| 项 | 读数 |
|---|---|
| 行数 | 40（B-01 ~ B-40） |
| 符合（实测） | 10（B-10 B-12 B-13 B-15 B-16 B-18 B-20 B-27 B-28 B-37） |
| 符合（按预期·不判失败） | 2（B-33 B-34） |
| 符合（按设计不处理） | 1（B-07） |
| 未验证（写清原因 + 代码层证据） | 27 |
| 不符（判红） | **0** |

关键实测串（RCON 原始回包，见 `.work/p39-scenario.log` / `p39-scenario2.log` / `p39-scenario3.log`）：

```
B-12  data get block 200 200 200 -> {Progress: 0, x: 200, y: 200, z: 200, id: "farmstorm:cultivator"}（无 Core）
B-13  事后放核后再读 -> 同上（仍无 Core）
B-15  核 54 格全填 stone；Progress 595 -> 600，4 秒后仍 600；Held 仍在；@e[type=item] 无实体
B-16  清 container.50/51 后 -> Progress 0、Held 消失、Items[50]={count:2,puffed_wheat}、Items[51]={count:2,puffed_wheat_seeds}
B-20  拆核前 {Progress:163, Held:{power_beet}, Core:61022896160968L} -> 拆核后 {Progress:203, Held:{power_beet}}（Core 键消失）
B-27  空核 setblock destroy -> 掉落清单 = Outpost Core x1
#21   Held=puffed_wheat + Progress=590 -> 核 Items=[{count:2,puffed_wheat},{count:2,puffed_wheat_seeds}]
#26   3 格核 setblock destroy -> Outpost Core x1 / Stone x62 / Stone x2 / Diamond x3 / Puffed Wheat x5
```

## 6. F-246 · 签字

产物：`.work/v020-verify/signoff.md`（8 行签字表 + 34 行任务核对表）。

```
@Mixin（src/）        0        *.mixins.json 0
playToClient          2        playToServer|sendToServer 0
PROTOCOL_VERSION      "2"      CustomPacketPayload 实现 2 个
FarmlandLedger.java   555 行   FarmlandGuard.java 225 行（LF 计数，两文件 git status 无改动）
Farmstorm.java        169-172  total == 97 / items == 33 / blocks == 27 / blockEntities == 8 / menuTypes == 2
git diff --stat docs/ 空
```

## 7. 负向面（硬判据）

```
$ git status --porcelain -- src tools docs
 M src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java   <- F-241，仅 4 行注释
 M tools/verify_project.py                                            <- F-243
$ git diff --stat
 .../farmstorm/datagen/ModLangProvider.java |   8 +-
 tools/verify_project.py                    | 130 +++++++++++++++++----
 2 files changed, 113 insertions(+), 25 deletions(-)
$ git diff --stat -- docs/
（空）
$ git status --porcelain | Select-String '.work/v020-verify'
?? .work/v020-verify/
```

`src/` 零改动（F-244 ~ F-246 期间未碰任何 Java）；`docs/` 全树 diff 为空；`run/` 被 gitignore（toml 不进版本库）。

## 8. 文档-源码偏差表（登记，不改上游）

| # | 文档写的 | 源码 / 实测 | 处置 |
|---|---|---|---|
| 1 | 36 号 §4.1.1 把交易键写作 `trade.common.min` …（6 条逻辑路径） | 落盘是 `[trade]` 段的 `commonMin` / `commonMax` / `rareMin` / `rareMax` / `epicMin` / `epicMax`（`FarmstormConfig.java:968` 的 `b.push("trade")` + `defineInRange("commonMin", 2, 1, 64)`） | 以源码为准；工具加**通用形状归一**（组名之后的点去掉），归一后才对上的 6 个键 `[WARN]` 登记，不静默吞掉 |
| 2 | 38 号 HC-117 / 36 号 §8 #12 写 `FarmstormConfig.java:227` 是字段声明、`:909-912` 是定义 | 实测声明在 **`:231`**、被注释的定义在 **`:932`**（`:227-229` 是三行理由注释） | 行号漂移，行为判据成立；按 A-68 口径登记，不改文档 |
| 3 | 36 号 §8 #9 / #10 写 `FarmstormGameEvents` 的 `:75` / `:76` / `:241-265` / `:277-281` / `:171` / 两处 `:170` `:175` | 实测：注册行 `:74`、`onRightClickBlock` 注释行 `:77`、`onServerTickPost` 方法 `:175`、`restoreAll` `:179`、`CoreListBroadcaster.tick` `:185`、`backfill` `:181`、`ledger.bind` `:267`、`protectsFromTrample` `:292` | 行号整体 +2 ~ +10，五处注释态与三处保留的**行为判据全部成立**；只登记，不改文档 |
| 4 | 36 号 §8 #38 / 41 号口径常把「服务端逻辑不依赖连线」理解为「服务端不 import 连线类」 | 实测服务端两个 BE 里各有 `CultivatorLinkBroadcaster.requestAll(...)`（共 4 处）—— 这是**单向下发请求**，返回值未被使用 | 判据按「不读客户端回包」成立（`playToServer|sendToServer` 0 命中）；已在 #34 / B-39 记录里写清这一点 |

## 9. 未验证清单（一条一行，都带原因）

| 类别 | 条目 | 原因 |
|---|---|---|
| 需真人右键 / 客户端 | #6 #7 #8 #16 #23 #24 #29(读 region) #31 #32 #33 #34；B-01 B-02 B-03 B-04 B-05 B-06 B-08 B-09 B-11 B-22 B-23 B-24 B-29 B-30 B-31 B-32 B-36 B-38 B-39 | 本环境只有专用服务端 + RCON，没有 `runClient` |
| 需第二客户端 | #25、B-22、B-24 | 同上，且「非己方」「两人同开」必须有第二个真实玩家 |
| 需停服读 region | #29、B-21 | 时间预算内未做「先卸载区块再读 region 字节比对」 |
| 需改码（与本 Phase 铁律冲突） | #34（把 handler 改空实现）、B-40（临时副本上恢复注释） | F-244 ~ F-246 一律不改码；B-40 还要求临时副本 + 验完还原 |
| 构造不出 | #27、B-19（候选核来自 `OutpostCoreIndex`，`/setblock` 的核不入索引）、B-26（活塞 / 同方块替换 / 区块卸载）、B-14（放置绑定要 `setPlacedBy` 的玩家）、B-25（区块未真正卸载重载）、#15~#17（`summon` 不生成 `Offers`） | RCON 下无放置者 / 无实体时序 |
| 截图 | 36 号 §8 要求的 ≥13 张手动场景截图 | 0 张，**未达成**（无图形客户端） |

## 10. 未关闭的问题

1. **手动场景截图 0 / 13**：这是 F-244 验收断言里唯一**明确未达成**的数值项，需要一台能跑 `runClient` 的机器补拍。
2. **迁移三出口只有出口②被实测**：出口①（有接替核改绑）与出口③（区块取不到保留原值）都要求「核进 `OutpostCoreIndex`」或「真停服读 region」，RCON 构造不出来。建议下一轮用 `p36-bot.js` 那套 bot 客户端做真实放置。
3. **交易三件（#15~#17）在本环境读不到 `Offers`**：`/summon` 不触发报价生成，必须真人右键。
4. **三个行号漂移**（偏差表 #2 / #3）：A-62 / A-69 的回灌仍未做（39 号 §8.6 已登记「属上游回灌，本节只登记不做」）。
5. **`trade` 键名的文档-源码形状差异**（偏差表 #1）：工具已用「通用形状归一 + WARN 登记」兜住，但 36 号 §4.1.1 的写法本身没改（不许改 `docs/`）。
