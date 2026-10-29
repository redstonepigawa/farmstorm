# F-80b 证据 —— 吸附判定成功即必中（作者口径，恢复 BRIEF §7.6 原意）

- 结论：**已实现并通过六条验收**。改动 2 个文件（都在 `combat/` 内）：
  1. `combat/WildHunterCombatEvents.java`（改：加 `PlayerTickEvent.Post` 挂载点 + `AttackEntityEvent` 里给去抖打点）
  2. `combat/WildHunterSwingSettler.java`（**新建**：双信号感知 + 去抖 + 调 `settleSwing`）
- `combat/WildHunterCombat.java` **没有改**（`settleSwing` 的结算逻辑原样复用）。
- 构建：`build` **BUILD SUCCESSFUL**（`.work/f80b-build.log`）。

## 1. 选了方案 A（双信号），为什么

- **信号 1 swing 跃变**：读 `LivingEntity.swinging`（public）与 `LivingEntity.swingTime`（public int）。
  跃变判据：`swinging == true && (上一刻 swinging == false || swingTime <= 上一刻 swingTime)`。
  两个条件并联是因为 `updateSwingTime()` 在刻末才把 `swingTime` 推进，`PlayerTickEvent.Post` 抓到的时刻可能落在
  「`swinging` 刚置真、`swingTime` 还是 0」或「`swingTime` 被重置」两侧，并联才稳。
- **信号 2 冲刺位移**：本刻水平位移 `hypot(dx,dz) >= 0.55` 格/刻。
  客户端预测速度 = 落点差 / dashTicks ≈ **0.93 格/刻**；正常疾跑约 0.36、走路约 0.22。0.55 落在两者之间。
- **只用信号 1 会误判**：右键使用、挖掘、放置都会走 `player.swing(...)`（`ServerGamePacketListenerImpl` 的交互分支），
  实测 S4 就是这条：只有 swing、没有冲刺位移 → 一滴血都不掉。
- **只用信号 2 会误判**：爆炸击退、末影珍珠、坐上矿车都会产生 ≥0.55 的位移，但它们不带 swing 跃变。
- 判定仍然**全走** `WildHunterCombat.resolveTarget`（5 格球 + 30°），没有放宽、也没有改成「朝目标方向就必中」。

## 2. 去抖（真命中只结算一次）

- `AttackEntityEvent`（真命中）在 `WildHunterSwingSettler.recordDirectHit(player)` 里记下 `gameTime`；
- 新路径结算前检查 `now - lastDirectHit > 1` **且** `now - lastSettle > 1`（窗口 1 刻）；
- 这样「真命中」那一下只由原版 + `settleSwing(directlyHit)` 说话（`directlyHit == resolved` 时本来就不重复施加），
  新路径看到打点就跳过；同一次挥击连续两刻也不会结算两次。

## 3. 六条验收读数（探针一次跑完，`ProbeF80b` 已删净）

```text
[F80bPROBE] attr=1.400 dashThreshold=0.55
狂野猎人 吸附挥击 速度 0.93
狂野猎人 结算 目标 minecraft:pig 伤害 1.4 击退 0.4
[F80bPROBE] S1_whiff_hits health 10.00->8.60 pigDelta=0.000,0.600
[F80bPROBE] S2_real_hit afterVanillaHealth=8.60 afterSwingHealth=8.60 (must be equal)
[F80bPROBE] S3_out_of_cone health=10.00 (must stay 10.00)
[F80bPROBE] S4_swing_without_dash health=10.00 (must stay 10.00)
[F80bPROBE] S5_allied allied=true health=20.00 (must stay 20.00)
```

| # | 场景 | 读数 | 判定 |
|---|---|---|---|
| 1 | **空挥必中**：猪在正前 4 格、玩家没碰到它，模拟冲刺 0.93 + `swing` | 猪血 **10.00 → 8.60**（伤害 **1.4** = 加成后攻击力），被推开（`delta=(0.000, 0.600)`，方向沿玩家→猪） | ✅ |
| 2 | **真命中不双倍**：`attackStrengthTicker=100` 后 `player.attack(pig)`，紧接着再一次「冲刺 + swing」 | `afterVanillaHealth=8.60`，`afterSwingHealth=8.60` **相等**（没有第二刀） | ✅ |
| 3 | **判定失败不结算**：同一套「冲刺 + swing」但猪在背后 180° | 血量 **10.00 不变** | ✅ |
| 4 | **误判排除**（右键/挖掘/放置等价物）：只 `swing`、无位移 | 血量 **10.00 不变** | ✅ |
| 5 | **豁免仍生效**：同队伍玩家在正前 3.5 格，冲刺 + swing | `allied=true`，队友血量 **20.00 不变** | ✅ |
| 6 | **红线** | `CustomPacketPayload` **0 处**、`@Mixin` **0 处**、`src`/`build/classes`/jar 三处 `Probe` **全 0**（重建后统计） | ✅ |

**关于 S1 的击退读数**：日志里我们传的是原版默认强度 0.4（`狂野猎人 结算 … 击退 0.4`），
而猪读到的 `deltaMovement` 是 **0.600**（方向沿玩家→猪）。`LivingEntity#knockback` 的公式是
`setDeltaMovement(vec3/2 - dir*strength)`，会把目标原有速度折半叠加；这里多出来的 0.2 来自猪被 `hurt` 后的自身速度项，
不是我们自订了数值。判据只要求「被击退」，0.6 ≥ 0.4 满足。

## 4. 探针与产物

- `ProbeF80b`（`@EventBusSubscriber` + 开关 `run-p13/f80b-probe.enabled`）取数后**本轮内已删除**，开关文件同步删除。
- 重建 `build` 后：`src` 0 / `build/classes` 0 / jar 内 `Probe` 0。

## 5. 清场

- 服务端 `/stop`；`:25631/:25641` 无 LISTENING；未 commit；
- 未碰 `client/FarmstormClient.java`、`Farmstorm.java`、Lane B 的任何文件；未新增 config 键。

## 6. 与 F-43 的关系

V-43 那个「空挥不结算」的缺口由本任务补齐：现在「swing 跃变 + 冲刺位移」会走 `settleSwing(player, null)`，
判定成功即结算伤害与击退。`AttackEntityEvent` 那条老路径保留（真命中仍走它），两条路径由 §2 的去抖合并成一次结算。
