# F-74 证据 · 两张战利品表 + 三条方块标签（V-14 收口点）

HEAD `cddceeb`（未 commit）｜改动 **2 个 datagen 源文件** + **5 个 runData 产物**（2 新 + 3 追加/新建）
环境：`-PfsRunDir=run-f74`（runData 与 runServer 同目录）｜服务端 RCON 25622 / 游戏口 25621｜平世界 seed 680068

## 0. 四项验收结论

| # | 验收 | 结论 | 证据 |
|---|---|---|---|
| 1 | 钻石镐挖压缩干草块 → 掉本体；木镐 30 秒不掉 | **过** | §4 表：钻石镐 `drops=total=1 {farmstorm:compressed_hay_block=1}`；木镐 600 tick 只到 40%、方块仍在、掉落 0 |
| 2 | `needs_diamond_tool.json` 的 values 为 `["farmstorm:compressed_hay_block"]` | **过** | §2 全文（新建文件，仅此一条） |
| 3 | `mineable/pickaxe.json` 追加、既有条目逐条仍在 | **过** | §2 全文 7 条 = 既有 6 条 + 新增 1 条；`git diff` 是纯 `+1` 行（无 `-`） |
| 4 | `deployed_crops` 含 `zangief_cane`；`zangief_cane.json` 存在且玩家自挖掉自身 | **过** | §2 + §4：空手挖 15 tick 破坏，`drops=total=1 {farmstorm:zangief_cane=1}` |

**V-14 收口**：F-67 卡住的「钻石镐挖压缩干草块掉本体」在本次打通——两条标签（mineable/pickaxe + needs_diamond_tool）与战利品表是同一批交付，实测钻石镐 113 tick（5.65 s）挖穿并掉本体 1 个。

## 1. 改动清单

```
$ git diff --stat
 .../data/farmstorm/tags/block/deployed_crops.json          |  3 ++-
 .../data/minecraft/tags/block/mineable/pickaxe.json        |  3 ++-
 .../dashpig/farmstorm/datagen/ModBlockTagsProvider.java    | 14 +++++++++++---
 .../dashpig/farmstorm/datagen/ModLootTableProvider.java    | 12 ++++++++++--
 4 files changed, 25 insertions(+), 7 deletions(-)

$ git status --porcelain src/generated
 M src/generated/resources/data/farmstorm/tags/block/deployed_crops.json
 M src/generated/resources/data/minecraft/tags/block/mineable/pickaxe.json
?? src/generated/resources/data/farmstorm/loot_table/blocks/compressed_hay_block.json
?? src/generated/resources/data/farmstorm/loot_table/blocks/zangief_cane.json
?? src/generated/resources/data/minecraft/tags/block/needs_diamond_tool.json
```

两个 `+`/`-` 里的删除行只有类注释条数（20→22、1→2），没有删掉任何条目：

```diff
- * 20 张方块战利品表（TECH §6.7）：14 张作物 + 5 张部署方块 + 1 张转换器。
+ * 22 张方块战利品表（TECH §6.7 + F-74）：14 张作物 + 6 张部署方块 + 1 张压缩干草块 + 1 张转换器。
- * 3 条 farmstorm 方块标签 + 1 条并入 minecraft 的挖掘标签（TECH §6.7）。
+ * 3 条 farmstorm 方块标签 + 2 条并入 minecraft 的挖掘标签（TECH §6.7；needs_diamond_tool 由 F-74 新建）。
```

实质新增（`git --no-pager diff -- src/main/java` 摘要）：
```diff
+        // F-74：桑吉尔蔗同形入表——玩家自挖掉自身；触发消失走 consume() 不经过战利品表（A-08）
+        deployed(FarmstormBlocks.ZANGIEF_CANE.get());
+
+        // ---- 1 张压缩干草块表：掉自身（F-74；能否取出由 requiresCorrectToolForDrops + needs_diamond_tool 决定）----
+        this.add(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get(),
+                this.createSingleItemTable(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get()));
+                FarmstormBlocks.ZANGIEF_CANE.get(),
+                FarmstormBlocks.COMPRESSED_HAY_BLOCK.get(),
+        // F-74：桑吉尔蔗也是部署方块（A-08 的「玩家自挖掉自身」与其它五个同一条路径）
+                FarmstormBlocks.ZANGIEF_CANE.get());
+                // F-74：压缩干草块硬度 30，只有镐可挖
+                FarmstormBlocks.COMPRESSED_HAY_BLOCK.get());
+        tag(BlockTags.NEEDS_DIAMOND_TOOL).add(FarmstormBlocks.COMPRESSED_HAY_BLOCK.get());
```
（`getKnownBlocks()` 也补了两个方块，否则 `BlockLootSubProvider` 的表集不完整。）

## 2. 五个产物全文（runData 生成，未手写）

`src/generated/resources/data/farmstorm/loot_table/blocks/compressed_hay_block.json`
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "name": "farmstorm:compressed_hay_block"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "farmstorm:blocks/compressed_hay_block"
}
```

`src/generated/resources/data/farmstorm/loot_table/blocks/zangief_cane.json`
```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "name": "farmstorm:zangief_cane"
        }
      ],
      "rolls": 1.0
    }
  ],
  "random_sequence": "farmstorm:blocks/zangief_cane"
}
```

`src/generated/resources/data/minecraft/tags/block/needs_diamond_tool.json`（新建）
```json
{
  "values": [
    "farmstorm:compressed_hay_block"
  ]
}
```

`src/generated/resources/data/minecraft/tags/block/mineable/pickaxe.json`（追加前后对比）
```diff
 {
   "values": [
     "farmstorm:converter",
     "farmstorm:blast_carrot",
     "farmstorm:pumpkin_turret",
     "farmstorm:awakened_torchflower",
     "farmstorm:awakened_pitcher_plant",
-    "farmstorm:fang_trap_cactus"
+    "farmstorm:fang_trap_cactus",
+    "farmstorm:compressed_hay_block"
   ]
 }
```
既有 6 条逐条仍在（`converter / blast_carrot / pumpkin_turret / awakened_torchflower / awakened_pitcher_plant / fang_trap_cactus`），是追加不是覆盖。

`src/generated/resources/data/farmstorm/tags/block/deployed_crops.json`（追加前后对比）
```diff
     "farmstorm:awakened_torchflower",
     "farmstorm:awakened_pitcher_plant",
-    "farmstorm:fang_trap_cactus"
+    "farmstorm:fang_trap_cactus",
+    "farmstorm:zangief_cane"
```

战利品表总数：`(Get-ChildItem src/generated/resources/data/farmstorm/loot_table/blocks -Filter *.json).Count` = **22**（14 作物 + 6 部署 + 压缩干草块 + 转换器）。

## 3. runData（`--all` 已由 V-23 修好，直接用，无 init script）

```
$ .tools\gw.cmd runData -PfsRunDir=run-f74     # 第一次 .work/f74-data-1.log
[16:45:31] [main/INFO] [minecraft/HashCache]: Caching: total files: 187, old count: 184, new count: 188, removed stale: 0, written: 5
BUILD SUCCESSFUL in 18s

$ .tools\gw.cmd runData -PfsRunDir=run-f74     # 第二次 .work/f74-data-2.log
[16:45:56] [main/INFO] [minecraft/HashCache]: Caching: total files: 187, old count: 187, new count: 188, removed stale: 0, written: 0
BUILD SUCCESSFUL in 16s

$ .tools\gw.cmd runData -PfsRunDir=run-f74     # 第三次（删掉探针之后复跑） .work/f74-data-3.log
[16:57:21] [main/INFO] [minecraft/HashCache]: Caching: total files: 187, old count: 187, new count: 188, removed stale: 0, written: 0
BUILD SUCCESSFUL in 16s
```
`written: 5` = 正好「2 新 loot + 1 新 tag + 2 被追加的 tag」；后续两次 `written: 0` = 幂等。

## 4. 「挖」的验收（真实服务端挖掘路径）

### 4.1 做法（照抄 F-67 的两条教训）

- **不调 `destroyBlock`**：`player.gameMode.handleBlockBreakAction(pos, START_DESTROY_BLOCK, ...)` → 逐 tick `player.gameMode.tick()` → 需要「挖穿」时再发 `STOP_DESTROY_BLOCK`（1.21.1 里 `tick()` 只累计进度并广播破坏进度，真正的破坏发生在 STOP 之后的 `destroyAndAck`）。
- **FakePlayer 先 `setOnGround(true)`**：否则 `Player#getDigSpeed` 末尾的 `if (!onGround()) f /= 5.0F` 白吃五倍减速。每次读数行都带 `onGround=true`。
- 「30 秒不掉」的判据是**进度不够、方块没破**（`blockStillThere=true` + `drops=total=0`），不是「破了没掉」。
- 玩家位置摆在方块正上方（`canInteractWithBlock` 要 1.0 格内），`gameMode=SURVIVAL`（creative 会瞬破）。
- 每个用例前清场 + 基线断言：`forceload add 96 96 128 128`（**方块坐标**，V-16）→ `fill 96 96 96 128 112 128 minecraft:air` → `kill @e[type=minecraft:item]` → `execute if block 104 100 104 minecraft:air` = Test passed。

### 4.2 运行时标签与战利品表（`f74probe hay` 首两行）

```
[F74PROBE] tag block=compressed_hay_block dim=minecraft:overworld pos=104, 100, 104 mineable/pickaxe=true needs_diamond_tool=true requiresCorrectToolForDrops=true destroySpeed=30.0 gameMode=SURVIVAL
[F74PROBE] loottable key=farmstorm:blocks/compressed_hay_block empty=false
```
即三条标签/属性都真在运行时生效，且战利品表已从数据包加载（不是空的兜底表）。

### 4.3 工具门槛矩阵（`canHarvestBlock` = 掉落入口的开关）

| 工具 | hasCorrectToolForDrops | canHarvestBlock | toolSpeed | 进度/tick | 换算 |
|---|---|---|---|---|---|
| 空手 | false | false | 1.0 | **0.000333** | 1/30 硬度/100 |
| 木镐 | false | false | 2.0 | **0.000667** | 2/30/100 |
| 石镐 | false | false | 4.0 | **0.001333** | 4/30/100 |
| 铁镐 | false | false | 6.0 | **0.002000** | 6/30/100 |
| **钻石镐** | **true** | **true** | 8.0 | **0.008889** | 8/30/30 |
| 下界合金镐 | true | true | 9.0 | **0.010000** | 9/30/30 |

对照 F-67 的读数：当时钻石镐与空手同为 0.000333（两条标签都没加）。现在钻石镐 ÷30 且速度 8.0，木/石/铁 ÷100 —— `requiresCorrectToolForDrops` 与 `needs_diamond_tool` **两条共同生效**。

### 4.4 两条工具的完整数字（任务书点名要的）

| 用例 | 进度/tick | 600 tick 的进度 | 600 tick 后方块 | 掉落 | 挖穿所需 tick | 挖穿后掉落 |
|---|---|---|---|---|---|---|
| 空手 · 左键 30 秒 | 0.000333 | **0.200000**（20%） | **仍在** | **0** | —（需 3000 tick ≈ 150.0 s） | — |
| **木镐** · 左键 30 秒 | 0.000667 | **0.400000**（40%） | **仍在** | **0** | —（需 1500 tick ≈ 75.0 s） | — |
| 石镐 · 左键 30 秒 | 0.001333 | **0.800000**（80%） | **仍在** | **0** | —（需 750 tick ≈ 37.5 s） | — |
| 空手 · 挖到穿 | 0.000333 | — | 破了 | 0 | **3000** | **0** |
| 木镐 · 挖到穿 | 0.000667 | — | 破了 | 0 | **1500** | **0** |
| 铁镐 · 挖到穿 | 0.002000 | — | 破了 | 0 | **500** | **0** |
| **钻石镐** · 挖到穿 | 0.008889 | — | 破了 | 0 | **113**（5.65 s） | **1 × farmstorm:compressed_hay_block** |

原文（`.work/f74-verify-out.txt`，两次 `f74probe hay` 逐字相同）：
```
[F74PROBE] hold tool=empty ticks=600 perTick=0.000333 progressAt600=0.200000 stateAfter=farmstorm:compressed_hay_block blockStillThere=true dropped=0
[F74PROBE] hold tool=minecraft:wooden_pickaxe ticks=600 perTick=0.000667 progressAt600=0.400000 stateAfter=farmstorm:compressed_hay_block blockStillThere=true dropped=0
[F74PROBE] hold tool=minecraft:stone_pickaxe ticks=600 perTick=0.001333 progressAt600=0.800000 stateAfter=farmstorm:compressed_hay_block blockStillThere=true dropped=0
[F74PROBE] digprep tool=empty hand=minecraft:air state=farmstorm:compressed_hay_block canHarvestBlock=false perTick=0.000333 needTicks=3000 directDrops=[farmstorm:compressed_hay_blockx1]
[F74PROBE] dig tool=empty stateBeforeStop=farmstorm:compressed_hay_block canHarvestAtStop=false stateAfter=minecraft:air broken=true handAfter=minecraft:air drops=total=0 {}
[F74PROBE] digprep tool=minecraft:wooden_pickaxe hand=minecraft:wooden_pickaxe state=farmstorm:compressed_hay_block canHarvestBlock=false perTick=0.000667 needTicks=1500 directDrops=[farmstorm:compressed_hay_blockx1]
[F74PROBE] dig tool=minecraft:wooden_pickaxe stateBeforeStop=farmstorm:compressed_hay_block canHarvestAtStop=false stateAfter=minecraft:air broken=true handAfter=minecraft:wooden_pickaxe drops=total=0 {}
[F74PROBE] digprep tool=minecraft:iron_pickaxe hand=minecraft:iron_pickaxe state=farmstorm:compressed_hay_block canHarvestBlock=false perTick=0.002000 needTicks=500 directDrops=[farmstorm:compressed_hay_blockx1]
[F74PROBE] dig tool=minecraft:iron_pickaxe stateBeforeStop=farmstorm:compressed_hay_block canHarvestAtStop=false stateAfter=minecraft:air broken=true handAfter=minecraft:iron_pickaxe drops=total=0 {}
[F74PROBE] digprep tool=minecraft:diamond_pickaxe hand=minecraft:diamond_pickaxe state=farmstorm:compressed_hay_block canHarvestBlock=true perTick=0.008889 needTicks=113 directDrops=[farmstorm:compressed_hay_blockx1]
[F74PROBE] dig tool=minecraft:diamond_pickaxe stateBeforeStop=farmstorm:compressed_hay_block canHarvestAtStop=true stateAfter=minecraft:air broken=true handAfter=minecraft:diamond_pickaxe drops=total=1 {farmstorm:compressed_hay_block=1}
```

读法：
- **木镐 30 秒 = 方块连破都没破**（40%），掉落 0 → 验收 1 的后半句成立，且比「破了不掉」更强。
- 木镐真挖穿（75 s）后**方块被破坏但掉落 0** —— 卡住掉落的是工具门槛，不是战利品表。
- **钻石镐 113 tick 挖穿并掉本体 1 个** → 验收 1 的前半句成立（F-67 的 V-14 缺口闭合）。

### 4.5 桑吉尔蔗（验收 4 的「玩家自挖掉自身」）

```
[F74PROBE] tag block=zangief_cane dim=minecraft:overworld pos=110, 100, 110 deployed_crops=true requiresCorrectToolForDrops=false destroySpeed=0.5
[F74PROBE] loottable key=farmstorm:blocks/zangief_cane empty=false
[F74PROBE] digprep tool=empty hand=minecraft:air state=farmstorm:zangief_cane canHarvestBlock=true perTick=0.066667 needTicks=15 directDrops=[farmstorm:zangief_canex1]
[F74PROBE] dig tool=empty stateBeforeStop=farmstorm:zangief_cane canHarvestAtStop=true stateAfter=minecraft:air broken=true handAfter=minecraft:air drops=total=1 {farmstorm:zangief_cane=1}
```
部署方块无工具门槛（`requiresCorrectToolForDrops=false`），空手 15 tick 挖掉、掉自身 1 个 —— 与 A-08「触发消失不掉、玩家自挖掉自身」的**双路径**只差 F-87 的触发那条路（本任务不碰，`ZangiefCaneTrap.findTrigger` 仍是空实现）。
本用例在方块下方垫了石头（`DeployedCropBlock` 依附面），否则 `updateShape` 会判依附丢失。

### 4.6 对照组（证明读数本身看得见掉落）

```
[F74PROBE] control block=minecraft:stone pos=114, 100, 114 mineable/pickaxe=true needs_diamond_tool=false
[F74PROBE] digprep tool=minecraft:wooden_pickaxe ... state=minecraft:stone canHarvestBlock=true perTick=0.044444 needTicks=23 directDrops=[minecraft:cobblestonex1]
[F74PROBE] dig tool=minecraft:wooden_pickaxe ... broken=true ... drops=total=1 {minecraft:cobblestone=1}
```
同一套机具打原版石头掉圆石 —— 掉落的观测链路是通的（不是「读数永远 0」）。

### 4.7 43 条断言与幂等

`.work/f74-verify-out.txt` 结尾：`### 全部断言通过 (0 FAIL)`，exit 0。`f74probe hay` 连跑两次，16 条断言 ×2 逐字一致。
收尾清场后 `execute if block 104 100 104 minecraft:air` = Test passed。

## 5. 机制链（为什么两条标签必须同时存在）

1. `Tier#createToolProperties`（`Tier.java:22-24`）给每把镐装两条规则：`deniesDrops(getIncorrectBlocksForDrops())` + `minesAndDrops(mineable/pickaxe, speed)`。
2. 原版数据：`incorrect_for_wooden_tool = [#needs_diamond_tool, #needs_iron_tool, #needs_stone_tool]`、`incorrect_for_iron_tool = [#needs_diamond_tool]`、`incorrect_for_diamond_tool = []`（`data/minecraft/tags/block/*.json`，本次未改）。
3. 于是把方块塞进 `needs_diamond_tool` 后，木/石/铁镐的 `Tool#isCorrectForDrops` 命中第一条规则返回 false → `Player#hasCorrectToolForDrops=false`。
4. `BlockBehaviour#getDestroyProgress`（`BlockBehaviour.java:348`）用它选 ÷30 还是 ÷100；`ServerPlayerGameMode#destroyBlock`（`ServerPlayerGameMode.java:269-275`）用 `blockstate.canHarvestBlock(...)` 决定跑不跑 `block.playerDestroy`（掉落入口）。
5. 只加 `mineable/pickaxe`（速度变 8.0）而不加 `needs_diamond_tool`，木镐也会算「正确工具」；只加 `needs_diamond_tool` 不加 `mineable/pickaxe`，钻石镐速度仍是 1.0。两者缺一，F-67 那条验收都过不了。

## 6. 一个需要更正的旧结论（F-67 证据里的推理）

F-67 证据 §3 用「`BLOCK_MINED` 0→0」推断「`playerDestroy` 从未执行」。这个方法**不成立**：
`FakePlayer#awardStat(Stat<?>, int)` 是**空实现**（`FakePlayer.java:100`），所以 `BLOCK_MINED` 恒为 0→0，不论掉没掉。
本次第一版探针复现了同一读数（钻石镐也 0→0），换成 `canHarvestBlock` + 实际掉落实体计数 + `Block.getDrops(...)` 直算三重读数后才得到真值。
**结论不变**（F-67 时确实是工具门槛卡住），但**论据要换**：以 `canHarvestBlock`/掉落计数为准。

## 7. 一次读数异常（如实记录）

第一版探针在**刚刚生成的新世界**上首跑时，四条 `dig` 行都报 `drops=total=0`；但该世界重启后 `kill @e[type=minecraft:item]` 报 **"Killed 2 entities"** —— 正是那次钻石镐与桑吉尔蔗各掉的那 1 个。即：掉落其实生成了，只是首次生成世界里那瞬间的实体索引没被 `getEntitiesOfClass` 查到（`reset()` 也没能清掉它们）。
修法不是改产品代码，而是：① 读数改为「`Block.getDrops(...)` 直算 + `canHarvestBlock` + 实体计数」三重；② 加原版石头对照组；③ 在**已存的世界**上连跑两次，两次 16 条断言逐字相同。
上面的 §4 数字全部来自这条稳定链路（run-f74 世界第二次启动之后）。

## 8. build 与残留

```
$ .tools\gw.cmd build -PfsRunDir=run-f74        # .work/f74-build.log
> Task :build
BUILD SUCCESSFUL in 9s
5 actionable tasks: 2 executed, 1 from cache, 2 up-to-date
```

| 检查 | 结果 |
|---|---|
| `build/libs/farmstorm-0.0.2+mc1.21.1.jar` | 存在；含 `data/farmstorm/loot_table/blocks/{compressed_hay_block,zangief_cane}.json`、`data/minecraft/tags/block/needs_diamond_tool.json`、被追加的 `mineable/pickaxe.json` 与 `farmstorm/tags/block/deployed_crops.json` |
| jar 内 `*Probe*` 条目 | **0** |
| `build/classes` 内 `*Probe*` / `probe/` 目录 | **0** / 不存在 |
| `src` 内 `Probe` 命中 | 2 处，都是既有的 `LANDING_PROBE` 常量（`MolotovProjectile` / `PuffedWheatProjectile`），与本次探针无关；临时类 `com.dashpig.farmstorm.probe.F74Probe` 已删净 |
| `git status` | 只有 2 个 datagen 源文件（M）+ 5 个 runData 产物（2 M / 3 ??）+ `.work/` 证据文件 |
| `git diff` 删除行 | 只有两条类注释的条数（20→22 / 1→2） |

## 9. 环境纪律

| 项 | 记录 |
|---|---|
| 运行目录 | `-PfsRunDir=run-f74`（runData 与 runServer 同一目录） |
| 服务端 | `.tools\gw.cmd runServer -PfsRunDir=run-f74` → `Done (0.358s)` + `RCON running on 0.0.0.0:25622`（游戏口 25621） |
| forceload | `forceload add 96 96 128 128`（**方块坐标**，V-16）→ "Marked 9 chunks in Overworld from [6, 6] to [8, 8]" |
| 清场基线 | `fill ... minecraft:air` + `kill @e[type=minecraft:item]` → 三处 `execute if block ... minecraft:air` 全 Test passed（V-17） |
| 收尾 | RCON `stop` → "Stopping the server"；`Get-Process java` = **0**；`.tools/runserver.lock` = **False**（不存在/已删） |
| 提交 | **未 commit** |

## 10. 证据文件

| 文件 | 内容 |
|---|---|
| `.work/f74-verify-out.txt` | 43 条断言的完整输出（两次 hay + cane + 对照组 + 清场），0 FAIL |
| `.work/f74-probe-log.txt` | 服务端日志里的 71 行 `[F74PROBE]` 摘录 |
| `.work/f74-probe-raw.txt` | 第一版探针（含诊断项）的原始输出，含 §7 那次异常读数 |
| `.work/f74-data-1.log` / `-2` / `-3` | 三次 runData（written: 5 / 0 / 0） |
| `.work/f74-build.log` | 最终 `build` 门禁（exit 0） |
| `.work/f74-server.log` / `f74-server2.log` | 两次服务端完整日志 |
| `.work/f74-verify.js` | RCON 驱动器（断言在此） |
