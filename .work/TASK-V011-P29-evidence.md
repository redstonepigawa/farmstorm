# Phase 29 证据 · F-187 ~ F-198（耕地-核心绑定制）

> 执行者：本 Phase 唯一执行者（子代理）。工作目录 `D:\project_farm`（Windows / pwsh）。
> 基线 commit `11329ca`（Phase 28 交付）。构建唯一入口 `cmd /c ""D:\project_farm\.tools\gw.cmd" build`。
> 红线：**无 Mixin**（verify_project §4 扫过）。「0 个自定义网络包」不作为任何判据；`GameRules.register` 仍 0。
> 未做任何 git 写操作（只有 `git status` / `git diff`）。

---

## 0. 结论行（逐条对应任务书 §7 的 12 条）

| # | 判据 | 结论 | 证据位置 |
|---|---|---|---|
| 1 | 绑定落盘形态：`dataVersion: 2` + `bindings` 1 条（4 键）+ 无 `byCore`；两次启动指纹相同 | **通过（实测）** 锄 1 格 → `/save-all flush` → `data{dataVersion:2, bindings:[1], orphans:[]}`，条目键 = `pos/owner/coreDimension/corePos`，`coreDimension="minecraft:overworld"`，无 `byCore`；runA/runB 两次启动指纹 `92499b2bfcf1382f（4 条）` 完全相同 | §4.1、§4.4 |
| 2 | 额度与第 17 格（含 `farmlandBonus=2`、拒绝不扣耐久） | **未验证** — 需要连续 17 次锄地 + 单核独占覆盖区，本轮 bot 编排未跑到；代码路径已实现（先过滤再取近 / `boundCount >= bonus` 跳过）。**不得读成已通过** | §8 第 1~4 条 |
| 3 | 三级键可复现 | **通过（实测）** 三级各自一次判别性读数 + 两次启动两行指纹完全相同（`b421749fb76ffe67` / `92499b2bfcf1382f`） | §4.2、§4.4 |
| 4 | 保护面（离线保护 / 无绑定对照 / 生物踩踏 / 换队） | **部分通过（实测）** 0 玩家在线（核主离线）时僵尸落到**有绑定**耕地 → 仍是 `minecraft:farmland`；落到**无绑定**对照 → 变 `minecraft:dirt`。「非同队玩家跳踩」「换队后两面并排」未验证 | §4.5、§8 第 5 条 |
| 5 | 拆核三支（① 改绑 ② 变泥土无掉落 ③ 只摘绑定） | **① ② 通过（实测）；③ 结构性不可达** ① (20,99,4) 由 B 改绑到 A；② (10,99,0) 变 `minecraft:dirt` 且 `@e[type=item]` 空；③ 见 §7 偏差 8 | §4.6、§7 |
| 6 | 补建可复现（N/M 两次相同 / 顺序打乱不变 / 20 田 1 核 → 16/4 / 0 个变泥土） | **通过（实测）** 三次独立重跑（同序 ×2、乱序 ×1）都是 `绑定 16 / 保持未绑定 4（共 20，按 BlockPos 全序）`，绑定集合逐条相同；20 块仍全是 farmland | §4.7 |
| 7 | 两处删除（`[farmland]` 无、58 段 / 166 键、`/farmstorm` 不注册、lang/grep = 0） | **通过（实测）** toml 段 58 / 键 166 / `[farmland]` 0；`farmlandlimit` 在三处（两份 lang + ModLangProvider）= 0；`FarmstormCommands` 全源码 0 命中；command 目录空 | §5、§6 |
| 8 | **A-40 关闭**：`结论：FAIL 0 项`、退出码 0、§1 段 166 | **通过（实测，全项目首次）** 退出码 0 + `结论：FAIL 0 项，WARN 0 项` + `生成 toml 键数: 166` | §3 |
| 9 | ticket 的 tick 面 + `chunks.dat` 不含核 | **tick 面通过（实测）**：0 玩家在线时两个核的轮次按 40 刻 × 30 的节拍持续增长（300 → 330 → …），`chunks.dat` 的 `Forced` 数组 `[]`（不含 -4294967294 / -4294967296），重启后补票 1~2 张。**三层区块状态未验证**（见偏差 6/7），且**不写「只有 1 个区块被加载」**（A-43） | §5.3、§7 |
| 10 | 语言键 314 × 2，键集相等 | **通过（实测）** zh 314 / en 314 / set 相等；`farmlandlimit` 键两份都不存在；verify_project §3 也报 `314 键` | §5.2 |
| 11 | `.tools\gw.cmd build` 退出码 0 | **通过** `EXIT=0`，`BUILD SUCCESSFUL` | §2 |
| 12 | 六处 `grep -c` | **通过** 六项全 0（另附 6 项附加判据） | §6 |

---

## 1. `git diff` 与文件清单（任务书 §8.1）

### 1.1 `git diff --stat -- src`

````
 .../resources/assets/farmstorm/lang/en_us.json     |   2 -
 .../resources/assets/farmstorm/lang/zh_cn.json    |   2 -
 .../blockentity/OutpostCoreBlockEntity.java        | 118 ++---
 .../farmstorm/command/FarmstormCommands.java       |  58 ---
 .../dashpig/farmstorm/config/FarmstormConfig.java  |   9 +-
 .../farmstorm/datagen/ModLangProvider.java         |  17 +-
 .../farmstorm/event/FarmstormGameEvents.java       |  57 ++-
 .../farmstorm/farmland/FarmlandGuard.java          | 208 +++++---
 .../farmstorm/farmland/FarmlandLedger.java         | 560 +++++++++++++++------
 .../farmstorm/outpost/OutpostCoreIndex.java        | 149 +++++-
 .../farmstorm/team/ScoreboardTeamResolver.java     |  18 +-
 .../com/dashpig/farmstorm/team/TeamResolver.java    |   9 +
 12 files changed, 828 insertions(+), 379 deletions(-)
````

### 1.2 `git status --porcelain -- src`

````
 M src/generated/resources/assets/farmstorm/lang/en_us.json
 M src/generated/resources/assets/farmstorm/lang/zh_cn.json
 M src/main/java/com/dashpig/farmstorm/blockentity/OutpostCoreBlockEntity.java
 D src/main/java/com/dashpig/farmstorm/command/FarmstormCommands.java
 M src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java
 M src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java
 M src/main/java/com/dashpig/farmstorm/event/FarmstormGameEvents.java
 M src/main/java/com/dashpig/farmstorm/farmland/FarmlandGuard.java
 M src/main/java/com/dashpig/farmstorm/farmland/FarmlandLedger.java
 M src/main/java/com/dashpig/farmstorm/outpost/OutpostCoreIndex.java
 M src/main/java/com/dashpig/farmstorm/team/ScoreboardTeamResolver.java
 M src/main/java/com/dashpig/farmstorm/team/TeamResolver.java
?? src/main/java/com/dashpig/farmstorm/outpost/CoreChunkTickets.java
````

「绝对不可动」清单逐条复核：`util/DeployedOwnership.java` 不在列表里（`git diff` 为空）；`isOwnerOrAlly` 仍**只有定义**
（全源码 1 命中）；`docs/`、`build.gradle`、`settings.gradle`、`.tools/`、`.work/v011-*.md` 四份台账均未改。

### 1.3 F-197 的三处注册删除（删完后的现状）

````
$ Select-String -SimpleMatch 'RegisterCommandsEvent','FarmstormCommands','onRegisterCommands' FarmstormGameEvents.java
（无输出 = 三处均已删除）
$ (Get-ChildItem src/main/java/com/dashpig/farmstorm/command).Count
0
````

被删的三处（原位置，30 号 §2.3 表第 2 行）：`:4` import、`:62` `gameBus.addListener(FarmstormGameEvents::onRegisterCommands)`、
`:71-73` `onRegisterCommands` 方法体。删后 `register(...)` 的监听列表（现 `:53-63`）为
`onPlayerTick / onPlayerLoggedIn / onPlayerLoggedOut / onExplosionDetonate / onLevelTickPost / onServerTickPost / onServerStopping / onRightClickBlock / onFarmlandTrample`——
服务端 `RegisterCommandsEvent` 上不再有本模组的监听。

---

## 2. 构建（任务书 §8.2）

````
$ cmd /c ""D:\project_farm\.tools\gw.cmd" build"
> Task :build
BUILD SUCCESSFUL in 11s
EXIT=0
build/libs/farmstorm-0.1.1+mc1.21.1.jar          1037109 B
build/libs/farmstorm-0.1.1+mc1.21.1-sources.jar   957755 B
````

逐步用的 `compileJava` 全程 exit 0；中途每次报错都当场修掉（唯一一次运行期崩溃见 §4.8）。

---

## 3. `verify_project.py` 的**完整输出**与退出码（A-40 收口）

````
$ python.exe -X utf8 tools/verify_project.py
== 1. config：文档键集 vs 生成的 toml ==
  30-TECH §4.1：58 组 / 166 键（v0.1.1 的 58 行数据表现算，其中 165 个键名已在前四份清单里）
  剔除已删键 1 个：farmland.limitPerPlayer
  文档口径键数: 166（01-DESIGN §3.8 117 + 12-TECH §8.1 22 + 18-TECH §8.1 19 行 + 24-TECH §8.1 12 行（新 11 + 改 1） + 30-TECH §4.1 166 键；其中 24 / 18 号另有 3 行是改既有默认值、§4.1 另有 165 个键名与前面的清单重名；按并集去重后 167，再扣掉 1 个已删键 = 166）
  生成 toml 键数: 166
  [OK]   166 个配置键与文档逐字一致（toml 166 键）
== 2. 93 个注册 ID 逐字核对（全源码树） ==
  [OK]   items: 32 个 ID 齐全
  [OK]   blocks: 26 个 ID 齐全
  [OK]   block_entity_type: 7 个 ID 齐全
  [OK]   entity_type: 13 个 ID 齐全
  [OK]   mob_effect: 4 个 ID 齐全
  [OK]   sound_event: 3 个 ID 齐全
  [OK]   recipe_type: 1 个 ID 齐全
  [OK]   recipe_serializer: 1 个 ID 齐全
  [OK]   creative_mode_tab: 1 个 ID 齐全
  [OK]   damage_type: 3 个 ID 齐全
  [OK]   data_component_type: 1 个 ID 齐全
  [OK]   menu_type: 1 个 ID 齐全
  已核 registry 对象: 93 / 93
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 314 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch

结论：FAIL 0 项，WARN 0 项
[退出码 0]
````

**A-40 的中间态对照**（删旧 toml 之前，同一条命令）：
`[STALE] run/config/farmstorm-server.toml 过期（mtime 2026-11-02 02:39:02，早于 config/FarmstormConfig.java 的 2026-11-02 23:47:50）：缺 1 个键、多 1 个键，重新生成即可消除（缺 ['pumpkin_turret.bulletSpeed']，多 ['farmland.limitPerPlayer']）`
+ `[FAIL] toml 多 1 个键且源码里存在（文档漏记 MISMATCH）: ['farmland.limitPerPlayer']`、`结论：FAIL 2 项，WARN 0 项`、退出码 **1**。
删旧 toml 重新生成后两项同时消失，转绿。

---

## 4. 运行期读数（专用服务器 + 无头 bot）

测试车道：`run-p29`（`eula.txt` + `server.properties` 从 `run/` 复制，`enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），
驱动脚本 `.work/p28-rcon.py`（持久 RCON）+ `.work/p29-bot.js`（mineflayer 1.21.1 无头 bot，`.scratch/f124-bot/node_modules`）。
世界参数：`doDaylightCycle=false`、`doMobSpawning=false`、`randomTickSpeed=0`（避免耕地失水退化污染踩踏面读数）。
20 × 20 格测试平台（y=99 泥土 / y=98 石头 / 上面清空）。

### 4.1 绑定的落盘形态（#16 / F-188 验收 3 / F-194 验收 1）

放 1 个核 `(40,100,-4)`（`placedGameTime=3503L`）与 `(0,100,-4)`（`3734L`），锄 `(20,99,0)` 后 `/save-all flush`：

````
 top keys: ['DataVersion', 'data']
 data keys: ['bindings', 'dataVersion', 'orphans']
 dataVersion: 2
 bindings: 1
   pos=(20, 99, 0) owner=81b5a0ee-ef3e-349d-9a7e-1a5138d5583b coreDimension=minecraft:overworld corePos=(40, 100, -4)
 orphans: 0
 has byCore key: False
````

条目键逐字 = `['coreDimension', 'corePos', 'owner', 'pos']`（4 键），`coreDimension = "minecraft:overworld"`，
`owner` 是耕地主 UUID，`corePos` 是核的方块坐标。**没有 `byCore` 键**（派生索引不落盘）。

### 4.2 三级比较键（A-39 / #18）

两个核相距 40 格：A = `(40,100,-4)`（**先放**，`gameTime=3503`）、B = `(0,100,-4)`（**后放**，`gameTime=3734`）。
等距点到两者的 `distSqr` 都是 **417**。判别设计：A 的「先放」与 B 的「坐标更小」互相**反向**，
所以每一条读数只有一个键能解释它。

| 级 | 锄的坐标 | 到 A / 到 B 的 distSqr | 绑到 | 只有哪一级能解释 |
|---|---|---|---|---|
| ① 距离最近 | `(30,99,0)` | **117** / 917 | A `(40,100,-4)` | 距离（坐标更小的是 B，会判成 B） |
| ① 距离最近 | `(10,99,0)` | 917 / **117** | B `(0,100,-4)` | 距离（`gameTime` 更早的是 A，会判成 A） |
| ② `gameTime` 升序 | `(20,99,0)` | 417 / 417（等距） | A `(40,100,-4)` | `gameTime`（坐标更小的是 B） |
| ③ 坐标字典序 | `(20,99,4)` | 465 / 465（等距，两个 `gameTime` 离线改成同一个值 3503） | B `(0,100,-4)` | `BlockPos#compareTo`（先放的是 A） |

离线改 `gameTime`：停服 → 用自写 NBT 读写器把 `farmstorm_outpost_core.dat` 里两条 `entries[*].gameTime` 都写成 `3503L` → 起服。

### 4.3 F-189 的老档计数

把索引里的 `gameTime` 全部删掉后起服：`核心比较键缺失（老档）：1 条`；连起两次都是 **1 条**（不随时间漂移，
因为**不做**「趁加载时从 BE 补写」）。同一次启动还打了 `核心比较键指纹 a93835ef6add04f8（1 条）`，两次相同。

### 4.4 两次启动的两行指纹（#18 / F-190 验收 1）

同一份存档连起两次（runA / runB，中间无任何改动）：

````
runA: 耕地绑定指纹 92499b2bfcf1382f（4 条）    反查集合 4 条    核心比较键指纹 b421749fb76ffe67（2 条）
runB: 耕地绑定指纹 92499b2bfcf1382f（4 条）    反查集合 4 条    核心比较键指纹 b421749fb76ffe67（2 条）
````

两行**逐字相同**。指纹口径：耕地侧按 `(pos, coreDimension, corePos)` 排序后逐行 `pos|coreDimension|corePos` 取 SHA-256 前 16 位；
核心侧按 `uuid` 排序后逐行 `uuid gameTime dimension pos` 取 SHA-256 前 16 位。两者都不看任何 Map / List 的迭代顺序。

### 4.5 保护面（A-45 / F-195 验收 2、3）

0 玩家在线（核主 ALICE 与 BOB 全部离线）时，向两个位置各召唤一只僵尸并任其自由落体（下落 2 格 > 0.5）：

| 位置 | 绑定 | 落体前 | 落体后 | 判定 |
|---|---|---|---|---|
| `(16,99,0)` | **有绑定**（补建绑到核 A） | farmland | `execute if block 16 99 0 minecraft:farmland` → **Test passed** | 踩不坏（A-45：有绑定即受保护；与核主在不在线无关） |
| `(22,99,1)` | **无绑定**（补建没绑上的孤儿） | farmland | `… minecraft:farmland` → **Test failed**；`… minecraft:dirt` → **Test passed** | 照常变泥土（对照组） |

僵尸落点读数：`data get entity @e[type=minecraft:zombie,limit=1] Pos` → `[13.76, 100.0, 0.5]`。

### 4.6 拆核三支（A-35 / D-136 / F-196）

前置：A `(40,100,-4)` 名下 2 块（`(20,99,0)`、`(30,99,0)`），B `(0,100,-4)` 名下 2 块（`(10,99,0)`、`(20,99,4)`）。
执行 `execute in minecraft:overworld run setblock 0 100 -4 minecraft:air`（拆 B）：

| 耕地 | 拆前 corePos | 拆后 | 出口 |
|---|---|---|---|
| `(20,99,4)` | `(0,100,-4)` B | `(40,100,-4)` **A**（`bindings` 里该条 corePos 变成 A） | ① 迁移成功 |
| `(10,99,0)` | `(0,100,-4)` B | 方块 = `minecraft:dirt`，`bindings` 里该条消失 | ② 迁不动 + 区块在内存 → 变泥土 |

- 掉落物：`execute if entity @e[type=minecraft:item]` → **Test failed**（无掉落物，D-136「不掉落」）。
- `bindings` 条数 4 → **3**（一条改绑、一条摘除），`orphans` 仍 0，反查集合与绑定条数一致。
- 第三支（迁不动 + 区块未加载 → **只摘绑定**）：见 §7 偏差 8 —— **结构性不可达**，代码分支保留
  （`Farmstorm.LOGGER.info("迁移失败且区块未加载：只摘绑定 {}", pos)`），运行时无法触发。

### 4.7 启动补建（A-47 rev.2 / F-190 验收 1、2、6）

做法：把 `farmstorm_farmland.dat` 换成 **0.1.0 老格式**（`data{owners{uuid{positions[{pos}]}}}`，无 `dataVersion`、无维度）
装 20 个坐标，把核心索引的 `gameTime` 全删（老档），然后起服。20 块的真实方块用
`/setblock <x> 99 <z> minecraft:farmland` 摆好（`x=16..25`、`z=0,1`），核 A 在 `(40,100,-4)`（容量 16）。

三次独立重跑的日志原文：

````
[01:20:04] 核心比较键缺失（老档）：1 条
[01:20:04] 核心区块票补票：1 张（farmstorm_core，distance=1，来源 OutpostCoreIndex）
[01:20:04] 耕地账本旧格式：20 条读入 orphans（不猜绑定）
[01:20:04] 耕地补建：绑定 16 块 / 保持未绑定 4 块（老档共 20 块，按 BlockPos 全序）
[01:20:04] 耕地绑定指纹 9d800981f335fb6c（16 条）
[01:20:04] 反查集合 16 条

[01:21:10] 耕地补建：绑定 16 块 / 保持未绑定 4 块（老档共 20 块，按 BlockPos 全序）   ← 同一份 fixture 重跑
[01:21:10] 耕地绑定指纹 9d800981f335fb6c（16 条）

[01:22:11] 耕地补建：绑定 16 块 / 保持未绑定 4 块（老档共 20 块，按 BlockPos 全序）   ← orphans 顺序打乱后重跑
[01:22:11] 耕地绑定指纹 9d800981f335fb6c（16 条）
````

- **N = 16、M = 4**，三次逐字相同；绑定指纹三次相同（`9d800981f335fb6c`）。
- 顺序不参与判定：第一次与第三次的 `orphans` 落盘顺序**互为倒序**（`reverse` vs `sorted`），绑定集合与 N/M 完全不变。
- 绑上的 16 块 = `BlockPos#compareTo`（y → z → x）最小的 16 块：
  `z=0 的 x=16..25（10 块）+ z=1 的 x=16..21（6 块）`；保持未绑定的 4 块 = `(22,99,1) (23,99,1) (24,99,1) (25,99,1)`。
  **这条读数只有「y → z → x 单级全序」能解释**（插入顺序、按 x 主序、按 z 副序都会给出别的 16 块）。
- **0 个变泥土**：`execute if blocks 16 99 0 25 99 1 16 99 0 masked` → `Test passed, count: 20`。
- 补建只跑一次：同一次运行里只有一条「耕地补建」日志；重启才再跑。

### 4.8 运行期崩溃与修复（登记，不隐藏）

第一次起服在补建后立刻崩：`java.lang.UnsupportedOperationException`。原因 = `OutpostCoreIndex.owners()` 返回
`List.copyOf(...)`（不可变），`logFingerprint()` 对它 `sort(...)`。修法：`owners()` 与
`FarmlandLedger.bindingsOf(...)` 都改成返回**可变副本**（后者在迁移里也要排序）。修后连续 10+ 次起服无异常。

---

## 5. 配置 / 语言 / ticket 的读数

### 5.1 主 toml（F-192 / A-40）

````
删除前：lines=763 secs=59 keys=166 farmland=1  mtime 2026-11-02 02:39:02  sha256[:16]=03a68f6b445efa5f
删除后：lines=761 secs=58 keys=166 farmland=0  mtime 2026-11-03 00:49:48  sha256[:16]=a366852132b9e544
````

- 段行口径按 **A-52** 用 `^\s*\[`（不是 `^\[`）：**58**。键行口径 `^\s*[A-Za-z_][^=]*=`：**166**。
- `[farmland]` 段**不存在**；`[pumpkin_turret]` 仍 **5 键**（`targetRadius / projectileDamage / knockbackLevel / fireIntervalTicks / bulletSpeed`）。
- **166 = 166 − 1（farmland.limitPerPlayer）+ 1（pumpkin_turret.bulletSpeed，Phase 27 已进源码但旧 toml 未生成）**。
  旧 toml 的 STALE 正是「缺 `pumpkin_turret.bulletSpeed`、多 `farmland.limitPerPlayer`」这一对。
- 重新生成方式见 §7 偏差 10（在独立目录生成后复制回 `run/config/`）。`run/` 的其它文件一律未改。

### 5.2 语言键

````
zh 314 en 314 equal True；farmlandlimit in zh/en: False False
verify_project §3：[OK] lang 键集一致，共 314 键
````

### 5.3 ticket（F-198 / A-43 / A-38）

| 读数 | 值 |
|---|---|
| 申请日志 | `核心区块票申请：minecraft:overworld -4294967294（farmstorm_core，distance=1）` / `… -4294967296 …` |
| 起服补票 | `核心区块票补票：2 张（farmstorm_core，distance=1，来源 OutpostCoreIndex）`（另一个存档 `1 张`） |
| 0 玩家在线时的 tick 面 | `核心 tick 轮次 300 → 330`（间隔 60 秒 = 30 轮 × 40 刻），两个核**同时**推进，说明方块实体在 tick |
| 拆核释放 | `核心区块票释放：minecraft:overworld -4294967296（farmstorm_core，distance=1）` **两条**（`release()` 被 onRemove 与 settleOnRemoval 各调一次）⇒ 幂等成立 |
| `chunks.dat` | `run-p29/world/data/chunks.dat` 的 `Forced` 数组 = **`[]`**（本轮唯一的 forceload 是搭平台时用 `/forceload` 打的，已 `forceload remove all` 清空）。核坐标的 `ChunkPos.toLong()` = `-4294967294`（(2,-1)）与 `-4294967296`（(0,-1)），**都不在数组里** |
| DIM-1 / DIM1 | 该世界从未在下界 / 末地 forceload 或强制加载 ⇒ 这两个维度下**没有** `chunks.dat` 文件 |

---

## 6. 六处 `grep -c`（任务书 §7.12）+ 6 条附加判据

| # | 判据 | 读数 |
|---|---|---|
| 1 | `setBlock` in `farmland/FarmlandLedger.java` | **0** |
| 2 | `getPlayerList().getPlayers()` in `farmland/FarmlandGuard.java` | **0** |
| 3 | `baseOverride|personalCapacity|sumOverMembers`（全源码） | **0** |
| 4 | `farmlandlimit`（全源码 + 两份 lang + ModLangProvider） | **0** |
| 5 | `FarmstormCommands`（全源码） | **0** |
| 6 | `setChunkForced|forceChunk`（全源码） | **0** |
| 附 1 | `getChunk|getBlockEntity|hasChunkAt` in `outpost/OutpostCoreIndex.java`（F-189 验收 3） | **0** |
| 附 2 | `isOwnerOrAlly`（全源码，仍只有定义无调用者） | **1** |
| 附 3 | `isAlly(ServerLevel` in `TeamResolver.java` / `ScoreboardTeamResolver.java`（F-187 验收 1） | **2 / 2** |
| 附 4 | `ledger.add(` in `event/FarmstormGameEvents.java`（F-194 验收 5） | **0** |
| 附 5 | `public void bind / public boolean unbind / public boolean migrate / bindingsOf / orphanCount`（F-188 验收 5，要求 ≥ 5） | **5** |
| 附 6 | `entriesOf / .owners() / .count(` in `OutpostCoreIndex.java`（F-189 验收 4，要求 ≥ 3） | **5** |

注：判据 1 / 6 / 附 1 的「= 0」包含**注释里也不许出现该字面量**——初版 javadoc 里写过
`{`code setBlock}` / `{`code getChunk}` / `{`code setChunkForced}`，已改成不含该字面量的表述再复测为 0。

---

## 7. 文档-源码偏差表（以源码为准，不改 `docs/`）

| # | 出处 | 文档写的 | 源码 / 实测 | 处置 |
|---|---|---|---|---|
| 1 | 31 号 F-192 验收 2 | `grep -c "farmland" config/FarmstormConfig.java` = **0** | **不可满足**：`outpost_core.farmlandBonus` 的键名、`translation(tr("outpost_core.farmlandBonus"))` 与字段 `outpostCoreFarmlandBonus` 都含 `farmland`，而「`outpost_core` 组一字不改」是同一任务第 3 条硬要求 | 改判为 `farmlandLimitPerPlayer` = 0、`farmland.limitPerPlayer` = 0、`b.push("farmland")` = 0（实测全 0）+ toml 无 `[farmland]` 段 |
| 2 | 30 号 §2.2.8 伪代码第 3 行 | 补建时「区块已加载 && 方块已不是 farmland → 摘除该孤儿」 | **无法实现**：`orphans` 条目只有 `pos`、没有维度（A-47 rev.2 已确认耕地侧无维度字段），无法定位它属于哪个 `ServerLevel`；按维度逐个试会在 A-49 的跨维度同坐标上误删 | 省略该分支；该判据改由绑定侧的 `validateAll`（补建第一步）执行，孤儿条目本来也不占容量 |
| 3 | 30 号 §2.2.3 的 `readBindings` 注释 | 读档时「逐条校验：坐标处已不是 farmland 的直接丢」 | `SavedData#load` 只拿得到 `HolderLookup.Provider`，**拿不到 `ServerLevel`**，读档期做不了方块读 | 校验搬到启动后第一 tick 的 `FarmlandLedger.validateAll(server)`（补建首步），判据与 `validated` 同一条 |
| 4 | 31 号 F-188 与 30 号 §2.2.2 | `orphans` 为 `LinkedHashMap<BlockPos, Orphan>`（第三个 record） | 31 号 F-188 写 `LinkedHashMap<BlockPos, UUID>` 且「**两个** record」；30 号写「本体三份、两个 record」 | 按 **31 号** 实现：两个 record = `Binding` / `CoreKey`，`orphans` 的值直接存 `UUID`（耕地主），不引入 `Orphan` record |
| 5 | 31 号 F-190 验收 1 | 「同一份存档连跑两次，N 与 M 完全相同」 | 补建成功即把孤儿转成绑定并 `setDirty`，第二次启动的输入只剩没绑上的那几条 ⇒ 日志自然是「绑定 0 / 保持 4（共 4）」，与第一次的「16 / 4（共 20）」**必然不同** | 判据按「**同一份老档 fixture 重跑两次**」取证（三次独立重跑都是 `16 / 4`，指纹相同）；顺序不参与判定另用「`orphans` 顺序打乱」取证 |
| 6 | 31 号 F-198 验收 5 / 30 号 §7 #36 | 三层区块状态「各截一次」（F3 的 Chunk 行） | 本机只有**无头专用服务器**，没有客户端 F3；替代工具 `execute if loaded` **与实测矛盾**（见下一行） | 只按 **tick 面**取证（A-43 明文要求「口径按 tick 面写」），**不写「只有 1 个区块被加载」** |
| 7 | A-43 / 31 号 F-198 验收 1、5 | 中心「方块实体 tick 中」、紧邻一圈「已加载」、两圈外「未加载」 | `execute if loaded` 的读数与 tick 行为**互相矛盾**：两个核都在按 40 刻节拍 tick，但 `positioned 40 100 -4 run execute if loaded ~ ~ ~` 报 **Test failed**，而 `positioned 0 100 -4 …` 报 Test passed | 该工具**判为不可用读数**并登记；tick 面改用「0 玩家在线时轮次按 40 刻推进」取证 |
| 8 | 31 号 F-196 验收 3 / A-35 | 走远 200 格让区块卸载后拆核 → 该田仍是 farmland、日志出现「迁移失败且区块未加载：只摘绑定」 | **F-198 之后结构性不可达**：`distance = 1` 的票让核所在区块处于 block/entity-ticking、邻圈为 FULL ⇒ **核的整个 3 × 3 都在内存里**；而耕地范围正好是这个 3 × 3 ⇒ 迁移时 `getChunkNow(耕地)` 恒非 null | 分支代码保留（A-35 的语义与日志都在），但**本 Phase 无法运行时触发**，记为「测不到」而非「未测」。**建议回灌 31 号** |
| 9 | 30 号 §2.2.10 的「观察性」三条 | 启动 + 补建之后「各打一条 INFO」 | 指纹与计数都实现了，但 `核心比较键缺失（老档）：N 条` 在**每次**启动都会打印（因为明文禁止「趁加载时从 BE 补写」），N 稳定不漂移 | 照实现取证（连起两次都是 1 条），并在 javadoc 里写明这是设计而非噪声 |
| 10 | 31 号 F-192 验收 1 | 「删旧 `run/config/farmstorm-server.toml` 起服」重新生成 | 直接在 `run/` 里起服会把 `run/world` 的 0.1.0 老档 fixture（`farmstorm_farmland.dat` 274 B / `farmstorm_outpost_core.dat` 201 B）改写成 v2 —— 而这两个文件是本 Phase 的取证样本 | 在独立目录 `run-p29-new` 用同一份代码 / 同一份默认值生成 toml，再复制回 `run/config/`；`run/` 内除这一个文件外零改动 |
| 11 | 31 号 F-190 验收 2 | 日志里「按 `BlockPos` 全序」字样仍在 | 实现里日志为 `…（老档共 {} 块，按 BlockPos 全序）` —— 与文档一致 | 无需处置；登记只为说明该字串是**代码里的硬编码常量**，不是凑判据 |
| 12 | A-37 | 不得写「调用点数 = 1」 | `FarmlandLedger` 的写入口有 4 个（`bind` / `unbind` / `migrate` / `adoptOrphan`），`remove(BlockPos)` = `unbind` + `orphans.remove` | 本证据**没有任何**「调用点数 = 1」的断言；`remove` 的调用点有 2 处（迁移的两条降级出口），照实登记 |

---

## 8. 未验证清单（每条一行 + 原因）

| # | 项 | 原因 |
|---|---|---|
| 1 | 第 **17** 格被拒并提示「耕地额度已满 16/16」（31 号 F-193 验收 1 / 30 号 §7 #17） | 需要「一个**独占覆盖**的核 + 连续 17 次锄地」，本轮无头 bot 的编排窗口不够；代码路径已实现（`selectCore` 先过滤后取近、`boundCount >= bonus` 跳过），但**没有运行时读数** |
| 2 | `outpost_core.farmlandBonus = 2` 后第 **3** 格被拒、队伍容量同步变 2（F-193 验收 2 / RT-33） | 同上（改 config + 重启 + 连续锄地），未跑 |
| 3 | 「最近的己方核满了、另一个有空位 → 绑到有空位的那个」（F-193 验收 4 / 29 号 B-17） | 需要两个己方核 + 其中一个先填满 16 格，成本同上 |
| 4 | 拒绝时 `ItemDamage` 前后相同（F-193 验收 5 / 29 号 B-16） | 随 1~3 一起未测 |
| 5 | 保护面：①「核主下线后由**非同队玩家**跳踩」②「换队后踩踏面不变 + 额度按新队重算（两行并排）」（F-195 验收 1、4） | 玩家的「跳踩」需要 bot 精确落在耕地方块上（mineflayer 的落地判定与服务器差一点，本轮尝试未触发 `FarmlandTrampleEvent`）；换队后两面并排还需要 `/team` 编排。**生物踩踏与无绑定对照已测**（§4.5） |
| 6 | 拆核支 ③「迁不动 + 区块未加载 → 只摘绑定」（F-196 验收 3 / A-35） | **结构性不可达**（偏差 8），不是没测 |
| 7 | F-188 验收 4 的降级方向：把 v2 档交给 **0.1.0 的 jar** 读 | 仓库里没有 0.1.0 的构建产物（`build/libs` 只有 0.0.2 / 0.0.3 / 0.0.4 / 0.1.1），无法装载旧 jar |
| 8 | F-188 验收 6：把一条已绑坐标 `/setblock air` 后「`bindings` 与 `byCore` 条数**同时**减 1」 | 惰性校验的两条日志与「同时剪 `byCore`」的实现都在（`unlinkByCore`），但本轮没构造该用例；`used()` 触发校验的路径已接好 |
| 9 | F-198 验收 1 后半 / 验收 5：F3 的三层区块状态 | 无头服务器没有 F3；`execute if loaded` 判为不可用读数（偏差 7） |
| 10 | F-197 验收 2 后半：单人**客户端**执行 `/farmstorm farmlandlimit 5` 失败、客户端补全里不出现 `farmlandlimit` | 本机没有图形客户端；服务端侧已由「`/farmstorm` 根节点不注册 + 全源码 0 命中」覆盖。客户端 `/farmstorm hud` 子树是 Phase 30 的活，本 Phase 不碰 |
| 11 | 服务端控制台执行 `/farmstorm` 返回「未知的指令」的**原文** | RCON 通道下未截取该条回显；由「`FarmstormCommands` 0 命中 + command 目录空 + `RegisterCommandsEvent` 监听已删」三项结构证据覆盖 |

---

## 9. 未关闭的问题

1. **A-49（只登记，不设通过项）**：耕地坐标**不带维度**（`FarmlandLedger` 的 `pos` 只有一个 long，v1 起如此）。
   后果两条：① 下界 `(0,64,0)` 的耕地会被主世界同坐标的绑定「借」到保护（`protectsFromTrample(level, pos)` 只看坐标）；
   ② 额度计数把两维同坐标的两块地算成一块。**本版不修**（理由见 `.work/v011-verdicts.md` A-49），本 Phase 未设任何通过项。
2. **F-196 第三支 × F-198 的相互作用（新发现，建议回灌 31 号）**：ticket 让核的整个 3 × 3 常驻内存，
   于是「迁不动 + 区块未加载 → 只摘绑定」在正常运行中不可达（偏差 8）。语义保留、代码保留、**运行期无法验证**。
3. **F-190 × A-47 的「第二次启动」口径**：补建成功即落盘，所以「连跑两次 N/M 相同」只能在**同一份老档 fixture**
   上重跑取证（偏差 5）。若 30 号 / 31 号仍要求「同一份存档连跑两次」，建议改成「同一份**老档**跑两次」。
4. **`execute if loaded` 不可用**（偏差 7）：作为 A-43 三层读数的替代工具，它的读数与 tick 行为矛盾，
   已判为不可用；后续 Phase 若要验区块状态，需要另找工具。
5. **补建的作用域**：补建用 `server.overworld()` 作候选核的作用域（同维度过滤），因此**下界 / 末地的老档耕地**
   若索引里没有同维度的核，会一直保持未绑定（与 A-49 同族，本版不修）。
6. **A-37**：本证据**没有**任何「调用点数 = 1」的断言；`FarmlandLedger` 现有 4 个写入口 + `remove` 2 个调用点，照实登记。
7. **F-192 的 toml 重新生成走的是「独立目录生成 + 复制」**（偏差 10）：内容由同一份代码生成，
   但 `run/config/farmstorm-server.toml` 的 mtime 不是「在 run/ 里起服」那一刻，取证时以 sha256 与段/键数为准。

---

## 10. 附录：本 Phase 用到的取证脚本（全部在 `.work/`，不进源码树）

| 文件 | 作用 |
|---|---|
| `.work/p28-rcon.py` | 持久 RCON 会话（一条命令一行，`!sleep <ms>` 控节奏） |
| `.work/p29-bot.js` | mineflayer 无头 bot（文件轮询驱动：`pos/walk/hop/use/rawuse/place/block/slot/equip/inv/chat/cmd/sleep`） |
| `.work/p29_nbtlib.py` | 最小 NBT 读写器（1~12 号 tag 全覆盖，含 `position` 的 `xyz ↔ long`） |
| `.work/p29-dump.py` | 耕地 / 核心 `.dat` 的结构摘要（键、条数、坐标） |
| `.work/p29-mkold.py` | 造 0.1.0 老格式 `farmstorm_farmland.dat`（20 条，可指定落盘顺序）+ 剥掉索引的 `gameTime` |
| `.work/p29-setg.py` | 离线把索引里所有 `gameTime` 改成同一个值（三级键的第三级取证） |
| `.work/p29-fixtures/` | 存档样本：`farmland-v1.dat`（0.1.0 原档 274 B）、`outpost_core-v1.dat`（201 B）、`b-after-till1/3.dat`、`old20-reverse.dat`、`oldcore-nogametime.dat`、`toml-before.txt` |
| `.work/p29-server*.log` / `.work/p29-bf-*.log` | 各次起服日志（含补建、指纹、tick 轮次、票申请/释放） |
| `.work/p29-verify-before.txt` / `p29-verify-final.txt` | A-40 中间态与收口态的 `verify_project.py` 全文输出 |
| `run-p29` / `run-p29-new` / `run-p29-data` | 独立车道（`.gitignore` 的 `run-*/` 覆盖，不进版本库；`run/` 未被起服） |
