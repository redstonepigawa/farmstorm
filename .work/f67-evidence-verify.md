# F-67 复核 + 「真实挖掘」补测（本会话第二段 · 只做父级点名的三件事）

> 定位：**不改任何源码**（`git diff` 为空），只补证据。上一段的探针由父级删除；本次新探针 `debug/F67MiningProbe.java` 跑完已删净（见 §5）。

## 0. 结论

| 项 | 结论 |
|---|---|
| 方块属性核对 | **零改动**，与 `12-TECH §3.9` 逐字一致 |
| A-06 矩阵 | 五行全过（四行 + 原版爆炸）；**真实投掷物实体**与**规则面**两条链路结论一致 |
| 空手左键 30 秒不掉落 | **成立**：真实挖掘路径跑满 600 tick，进度只 20%，方块未破、掉落 0、`BLOCK_MINED` 不变 |
| 钻石镐挖出本体 | **仍不成立，但归属 F-74**（两条方块标签 + 战利品表）。本轮未动 datagen |
| 一处读数修正 | 原证据的「钻石镐 750 秒」是 **FakePlayer 未落地**的产物；正确值是 **3000 tick / 150 秒** |

## 1. 父级转述逐条确认

| 父级说法 | 是/否 | 我的实测原文（`.work/f67-server-c.log`，15:08:00） |
|---|---|---|
| `defaultDestroyTime=30.0` | **是** | `defaultDestroyTime=30.0 getDestroySpeed=30.0 explosionResistance=6.0 requiresCorrectToolForDrops=true` |
| `class=Block`（无自定义子类） | **是** | `class=net.minecraft.world.level.block.Block` |
| `mapColor=COLOR_BROWN` | **是** | `mapColor==COLOR_BROWN=true soundType==GRASS=true isAir=false hasBlockEntity=false` |
| 瓜！ `canBreak=false` 存活 | **是** | `瓜！BlastRule.grenade() power=3 抗性=6.0 阈值=6.0 rule.canBreak=false detonate=true → 方块存活` |
| 瓜片子 `canBreak=false` 存活 | **是** | `瓜片子BlastRule.sliceBomb() power=2 … 阈值=6.0 rule.canBreak=false … → 方块存活` |
| 育碧 `canBreak=true` 被破坏 | **是** | `育碧BlastRule.UNBREAKABLE_SKIP power=4 … 阈值=3600000.0 rule.canBreak=true … → 方块被破坏` |
| 毒育碧 `canBreak=true` 被破坏 | **是** | `毒育碧BlastRule.VANILLA power=2 … 阈值=3600000.0 rule.canBreak=true … → 方块被破坏`（power 只 2，仍破：原版射线 f 起点 [1.4,2.6] 减 1.89 后仍有正数分支） |
| 原版 TNT 口径爆炸 → 被破坏 | **是** | `原版爆炸（Level#explode, power=4, TNT 口径）抗性=6.0 → 方块被破坏` |

**补充（父级口径里没有的一条）**：矩阵不只是探针规则面——上一段还用**真实投掷物实体**在活服上跑过第二遍（`/summon farmstorm:melon_grenade|melon_slice_bomb|ubi_server|toxic_ubi_server` + 原版 `minecraft:tnt`），并在每个测试块四邻摆了**泥土见证块**，证明「爆炸确实发生了」而不是投掷物没落地：

```
[14:58:16] [Rcon] f67b-100-hay-still     ← 瓜！炸完：方块还在
[14:58:16] [Rcon] f67b-100-witness-air   ← 相邻泥土被炸掉：爆炸确实发生
[14:58:16] [Rcon] f67b-120-hay-still     ← 瓜片子：方块还在
[14:58:16] [Rcon] f67b-120-witness-air
[14:58:16] [Rcon] f67b-140-hay-air       ← 育碧：方块没了
[14:58:16] [Rcon] f67b-160-hay-air       ← 毒育碧：方块没了
[14:58:16] [Rcon] f67b-180-hay-air       ← 原版 TNT 实体：方块没了
```

原文在已入库的 `.work/f67-server-a.log`（`f67b-` 标记 25 行、`f67-` 标记 12 行）。互补性成立：`hay-still` 只出现在 100/120，`hay-air` 只出现在 140/160/180，没有任何一行两边都命中。

## 2. 一处修正：750 秒 → 150 秒

原证据写「钻石镐 750 秒」，那是 FakePlayer 口径：`Player#getDigSpeed` 末尾有 `if (!this.onGround()) f /= 5.0F;`，FakePlayer 默认不落地，白吃 5 倍减速（0.2 而不是 1.0）。落地后（`setOnGround(true)`）实测：

```
[15:17:32] 真实玩家口径（BLOCK_BREAK_SPEED/SUBMERGED_MINING_SPEED 归一到 1.0）：空手 进度/tick=0.000333 ⇒ 30 秒(600 tick) 只挖到 200‰、挖穿需 3000 tick（150.0 秒）
```

`0.000333 = 1.0 / 30.0(硬度) / 100(不是正确工具)` —— 与硬度 30、无正确工具两条属性对得上。**结论**：真实玩家空手/带钻石镐都是 150 秒才挖穿（钻石镐不给加速，因为标签缺失，见 §4）。

## 3. 真实挖掘补测（父级点名的第 2 件）

走**真实服务端挖掘路径**：`gameMode.handleBlockBreakAction(START_DESTROY_BLOCK)` + 逐 tick `gameMode.tick()`（1.21.1 里 tick 只累计进度并广播破坏进度，真正破坏发生在客户端发 `STOP_DESTROY_BLOCK` 之后，所以「30 秒」用例**只发 START、不发 STOP**，与真实客户端行为一致）。原文（`.work/f67-server-f-mining2.log`，15:17:32）：

```
空手 左键 30 秒（600 tick，不发 STOP） 手=minecraft:air hasCorrectToolForDrops=false 进度/tick=0.000333 tick 数=600 期望进度=0.200 方块被破坏=false 掉落实体=0 BLOCK_MINED 0→0
钻石镐 左键 30 秒（600 tick，不发 STOP） 手=minecraft:diamond_pickaxe hasCorrectToolForDrops=false 进度/tick=0.000333 tick 数=600 期望进度=0.200 方块被破坏=false 掉落实体=0 BLOCK_MINED 0→0
空手 按进度挖到穿 手=minecraft:air hasCorrectToolForDrops=false 进度/tick=0.000333 tick 数=3002 期望进度=1.00（need=3002） 方块被破坏=true 掉落实体=0 BLOCK_MINED 0→0
钻石镐 按进度挖到穿 手=minecraft:diamond_pickaxe hasCorrectToolForDrops=false 进度/tick=0.000333 tick 数=3002 期望进度=1.00（need=3002） 方块被破坏=true 掉落实体=0 BLOCK_MINED 0→0
```

读法（都不用瞬破、不改属性）：
- **30 秒 → 方块连破都没破（进度 20%）**，掉落 0，`BLOCK_MINED` 不动 → 验收第 2 条的前半句「空手左键 30 秒不掉落任何物品」**成立**，而且比要求更强：30 秒根本挖不穿。
- 真挖穿（3002 tick = 150 秒）后才走 `destroyBlock`：方块被破坏、**掉落仍是 0**、`BLOCK_MINED` 仍是 0→0 —— 说明 `playerDestroy`（掉落入口）**从未被执行**，卡住它的是 `requiresCorrectToolForDrops` 的工具门槛，不是战利品表缺失。

## 4. 缺口归属：两条标签 + 战利品表都是 F-74

```
[15:08:00] mineable/pickaxe=false needs_diamond_tool=false farmstorm:explosion_immune=false farmstorm:blast_proof=false
```

- `13-TASKS §9.3 #10/#11`（第 783–784 行）：`minecraft:mineable/pickaxe` 追加、`minecraft:needs_diamond_tool` 新建 → **F-74**；
- `§9.3 #33`（第 752 行）：`data/farmstorm/loot_table/blocks/compressed_hay_block.json` → **F-74**。
- 因为不在 `mineable/pickaxe` 里，`player.hasCorrectToolForDrops(state)` 对钻石镐也是 `false` —— 所以钻石镐与空手读数完全相同（都是 0.000333/tick）。这不是 F-67 的缺陷。

本轮**没有**动 `ModBlockTagsProvider` / `ModLootTableProvider` / 任何 datagen 文件。

## 5. 收尾自查（父级点名的第 3 件）

```
src Probe 文件数            = 0        （debug/ 目录已删）
build/classes 内 Probe 类   = 0        ；debug 类目录存在 = False
jar probeHits              = 0（主 jar）/ 0（sources jar，各 426 / 406 条目）
git status                 = 只有 .work/ 下的未跟踪 .log，无 debug/、无未跟踪 .java
git diff --stat            = 空（零源码改动）
build -PfsRunDir=run-f67   = BUILD SUCCESSFUL，exit 0，warning 行 0，error 行 0
服务端收尾                  = java 进程 0，.tools/runserver.lock 不存在，run-f67/f67-probe.enabled 已删
```

## 6. 本次留下的证据文件

| 文件 | 内容 |
|---|---|
| `.work/f67-server-c.log` | 属性 + 标签 + FakePlayer 读数 + `/execute if block`（return=1）+ 五行规则面矩阵 |
| `.work/f67-server-f-mining2.log` | **最终挖掘证据**（落地后 0.000333/tick、30 秒 20%、150 秒挖穿仍 0 掉落） |
| `.work/f67-server-e-mining1.log` | 中间那次（未落地，5 倍减速）——「750 秒」的来源，留作对照 |
| `.work/f67-build.log` | 最终 `build` 门禁（exit 0、零 warning） |
| `.work/f67-server-a.log` / `f67-server-b.log` | 上一段已被 76ea279 入库，未改动；`b` 是我那次启动失败的日志（见下） |

附注：上一段有**一次启动失败**（15:06:01，`Failed to load class com.dashpig.farmstorm.Farmstorm … cls is null`）。原因是我改探针的同时另一路把 `debug/` 删了、构建产物与源码瞬时不一致；重编后正常。它与产品代码无关，`f67-server-b.log` 就是那次失败的完整日志。
