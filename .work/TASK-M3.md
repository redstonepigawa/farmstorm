# 任务书 M3 · 8 种投掷物（Phase 5：F-26 ~ F-33）

> 先读 `.work/PLAN.md` §0/§4/§6。前置：M1-A（注册层）、M2-A（`FarmstormExplosion` / `ExplosionQueue`）已交付。
> 本文件按文件所有权拆成 M3-A / M3-B / M3-C 三个并行子范围，**每个子代理只写自己那一组文件**。

## 0. 公共必读
1. `docs/zh/02-TECH-SPEC.md` §4.7（投掷物基类 + 8 个子类行为表，含 `keepAliveAfterImpact` 与 `submunition`）。
2. `docs/zh/01-DESIGN.md` §3.4（投掷系统）+ §4.1 / §4.3 / §4.4 / §4.6 / §4.7 / §4.12 / §4.13 / §4.14（对应作物规格）。
3. `docs/zh/00-DECISIONS.md`：D-07、D-16、D-17、D-19、D-20、D-27、D-38、C-2、C-6。

## 1. M3-A · 生命周期 / 命中分流 / 创造模式口径（F-32、F-33）
**可写文件**：`entity/ThrownCropEntity.java`、`item/ThrowableCropItem.java`、`item/DeployableCropItem.java`。
- `tick()`：存活超过 `throwable.lifetimeTicks`（默认 100）自动 `discard()`。
- `onHitBlock` / `onHitEntity` 的公共部分**只在基类**；命中后立即消失，`keepAliveAfterImpact()` 为 true 时例外。
- `submunition` 经 `SynchedEntityData` 同步；子母弹不消耗「命中即消失」以外的额外逻辑。
- 创造模式**不消耗**物品、不被陷阱锁定（D-07 / F-33）；投掷冷却走 `THROW_COOLDOWN_TICKS = 5`（HC-13，不得进 config）。

## 2. M3-B · 爆炸型投掷物（F-26、F-27、F-28、F-29）
**可写文件**：`entity/thrown/PuffedWheatProjectile.java`、`UbiServerProjectile.java`、`ToxicUbiServerProjectile.java`、`MelonSliceProjectile.java`、`MelonGrenadeProjectile.java`。
| 类 | 行为（数值全部从 config 读） |
|---|---|
| 膨胀麦 | 命中方块的 `dryEdge³` 替换为 `minecraft:hay_block`；范围内存在 `#minecraft:water` 则改用 `wetEdge³`；**跳过爆炸抗性 ≥ 3.6e6 的方块**（D-38/T-05）；含空气 |
| 育碧服务器 | `FarmstormExplosion(power=ubi_server.explosionPower, cap=maxDamage, rule=UNBREAKABLE_SKIP)` |
| 毒育碧服务器 | 先 `FarmstormExplosion(power, cap)`，再在命中点生成剧毒 `AreaEffectCloud`（`cloudRadius` / `cloudDurationTicks`）；`PotionContents` 构造以实际 API 为准，退化方案=直接对云内玩家施加 `MobEffects.POISON` |
| 瓜片子 | `FarmstormExplosion(power=melon_slice_bomb.explosionPower, cap=maxDamage, rule=抗性阈值 = explosion.sliceResistThreshold)` |
| 瓜！ | ① 在 `pos + (rand(-jit,jit), spawnOffsetY, rand(-jit,jit))` 生成 `spawnCount` 个 `MelonSliceProjectile`，`setSubmunition(true)`，随机水平初速；② 立刻 `FarmstormExplosion(power=melon_grenade.explosionPower, cap=maxDamage, rule=抗性阈值 = grenadeResistThreshold)` |

## 3. M3-C · 功能型投掷物（F-30、F-31）
**可写文件**：`entity/thrown/BambooFirecrackerProjectile.java`、`RedMushroomPuffProjectile.java`、`BrownMushroomPuffProjectile.java`。
| 类 | 行为 |
|---|---|
| 爆竹 | `keepAliveAfterImpact() = true`：命中后 `fuseTicks = durationTicks`、`noPhysics = true`、`setDeltaMovement(ZERO)`；每 `spawnIntervalTicks` 在自身 ±`FIRECRACKER_SCATTER`(1.5，内部常量) 内生成 `minecraft:wind_charge` 并立即引爆；到期 `discard()` |
| 红菇 | 半径 `red_mushroom_puff.radius` 内**所有玩家**（含队友，D-27）获得 `nausea` / `blindness` 两个效果（level/durationTicks 全部走 config） |
| 棕菇 | 半径 `brown_mushroom_puff.radius` 内所有玩家获得 `weakness` / `slowness`（同上） |
蘑菇类不破坏方块、不造成伤害。

## 4. 通用约束
- 伤害上限、威力、半径、时长一律从 `FarmstormConfig` 读；**源码里出现玩法数值字面量即为失败**。
- `onImpact` 只在服务端被调用（HC-03）；客户端不做任何判定。
- 不写 TODO / 不注释实现 / 不留空方法。
- 不改 `registry/**`、`config/**`、`explosion/**`、`blockentity/**`、`menu/**`、`recipe/**`（其他代理负责）。

## 5. 验收
1. `gw.cmd compileJava` 与 `gw.cmd build` 绿；`gw.cmd runServer` 出现 `Done (`。
2. `grep` 证据：`entity/thrown/` 下 `onHitBlock` 命中 0 次（F-32 验收项②）。
3. 每个子类给出「读了哪些 config 键」的清单。
4. 无法在无客户端环境验证的运行时行为，明确写出来，不要写成已验证。

## 6. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外写清你改了哪些文件、读了哪些 config 键、以及未验证项。
