# 任务书 M4-D · 食用型与幸运浆果（Phase 7：F-40 ~ F-43）

> 先读 `.work/PLAN.md` §0/§4/§6/§7/§8。前置：M1-A、M2-A 已交付（`FarmstormExplosion`、`ExplosionQueue`、`FarmstormGameEvents.trackCreativeMode/restoreCreativeMode/hasPendingCreativeRestore` 都可用）。

## 1. 必读
1. `docs/zh/02-TECH-SPEC.md` §4.12（`LuckyBerryRoller` 全节：8 档权重、`roll`、`apply` 与 8 档执行细节）、§4.3.4（`FoodCropItem`）、§8.3（RNG 只在服务端）。
2. `docs/zh/01-DESIGN.md` §3.5（食用系统）、§4.5（甜菜！）、§4.15（幸运浆果）、§7.13（创造模式分支的特殊处理）。
3. `docs/zh/00-DECISIONS.md`：D-17、D-18、D-27、D-29、D-33。

## 2. 交付物（只动这两个文件）
`item/FoodCropItem.java`、`util/LuckyBerryRoller.java`。

### 2.1 `FoodCropItem`
- `finishUsingItem` → 服务端分支调 `onEaten(stack, serverLevel, player)`；客户端不参与（HC-03）。
- 食用时长取 `food.eatTicks`（默认 32）；营养/饱和度在注册期给定（D-29，见 R-08）。
- 甜菜！：一次性施加 `power_beet` 的 4 个效果，全部从 config 读 `power_beet.<effect>.level/durationTicks`。
- 幸运浆果：调用 `LuckyBerryRoller.roll(...)` + `apply(...)`。

### 2.2 `LuckyBerryRoller`
按 TECH §4.12 逐条：
- `enum Outcome`（8 值）、`record Roll(Outcome, int permille)`、由 config 烘出的权重表、`CUMULATIVE`、`roll(RandomSource)`（单次 `nextInt(1000)`）、`apply(ServerLevel, ServerPlayer, Outcome)`、`buildCumulative`（校验和 = 1000）。
- 8 档执行细节照 TECH §4.12 的表；`SPAWN_CROPS` 用 **除幸运浆果外的 14 种**（D-17，`CropProfile.LUCKY_BERRY_POOL` 已存在）；落点按 D-33（半径 2 格内随机可站立方块，放不下则就近）。
- `LOOT_CHESTS`：2 个箱子 + `minecraft:chests/end_city_treasure` 战利品表（`setLootTable` + `setLootTableSeed`）。
- `SMALL_EXPLOSION`：`FarmstormExplosion(power=smallExplosionPower, cap=999, rule=UNBREAKABLE_SKIP)`（TECH §4.12：不封顶）。
- `CHARGED_CREEPER`：`setPowered(true)`，不点燃。
- `POWER`：力量 V / 抗性 III / 跳跃提升 V / 速度 II，各 12000 tick（全部走 `lucky_berry.<effect>.*`）。
- `CREATIVE`：**D-18 三级兜底**——记原模式 → `setGameMode(CREATIVE)` → `FarmstormGameEvents.trackCreativeMode(player, original)`；`creativeDurationTicks`（1800）后由既有事件路径还原。本就是创造的玩家只播音效、不改模式。**只在内存，不写存档**。

## 3. 约束
- 随机抽取只在服务端、只在 `finishUsingItem`；一个物品只能吃一次（原版食物流程负责 `shrink`）。
- 数值一律从 config 读，源码里出现玩法数值字面量即为失败。
- 不改 `config/**`（117 键已定稿）、`registry/**`、`event/**`、`explosion/**`。

## 4. 验收
1. `gw.cmd compileJava` / `build` 绿；`gw.cmd runServer` 出现 `Done (`。
2. 权重表断言：启动自检或临时探针打印 8 段千分比与累积上界，末位 = 1000。
3. 强制触发证据：把 8 项概率分别改成 100/0 各跑 100 次抽取，命中率 100%（可在临时探针里跑，跑完删）。
4. 创造模式分支：临时探针模拟抽中 → 切创造 → 到点还原；下线即还原。**写清哪些是实测、哪些只能静态论证**。
5. 配置和 ≠ 100 时 `bake()` 拒绝并回退默认 + `warn` 的行为可指认（改造 config 的临时验证要还原）。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外写清 8 档各自的 config 键清单与未验证项。
