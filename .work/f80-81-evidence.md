# F-80 / F-81 证据 —— 服务端复核与伤害/击退结算 + 击杀续时（Lane A / Phase 13）

- 结论：**两条任务均已实现并通过判据**。改动 **2 个文件**：
  1. `src/main/java/com/dashpig/farmstorm/combat/WildHunterCombat.java`（在 F-78 版本上新增 `settleSwing` / `extendOnKill` / `KNOCKBACK_STRENGTH` 与 5 个 import，72 → 122 行）
  2. `src/main/java/com/dashpig/farmstorm/combat/WildHunterCombatEvents.java`（**新建**，34 行；F-80 的攻击挂载点 + F-81 的击杀挂载点）
- 环境：`run-p13`（server-port 25631 / rcon 25641），`FS_RUN_LOCK=run-p13`。
- 构建：`build` **BUILD SUCCESSFUL**（`.work/f80-build2.log`）。

## 0. 实现要点

`WildHunterCombatEvents`（新）：
- `onAttack(AttackEntityEvent)` → 只认 `ServerPlayer` → `WildHunterCombat.settleSwing(player, 被打中的实体)`；
- `onLivingDeath(LivingDeathEvent)` → killer 必须是 `ServerPlayer` 且不是死者本人 → `WildHunterCombat.extendOnKill(player)`。

**为什么挂 `AttackEntityEvent`**：1.21.1 的 NeoForge **没有** `ServerboundSwingPacket` 的公开事件，
而 D-50 禁止自订客户端包；TECH §3.12 给的出口是「用 `PlayerEvent`/原版攻击流程的**服务端结算点**复核目标」。
`AttackEntityEvent` 正是 `Player#attack` 内部的服务端结算点（且可取消）。
**已知缺口（写进 F-85 回归清单）**：纯空挥（没打中任何实体）不会触发该事件，因此「吸附后挥空」这一路径当前不结算。
要补就得拦截 `handleAnimate`，那需要 Mixin（D-40 禁止）或自订包（D-50 禁止），留作已知限制。

`settleSwing` 的三条规则：
1. 复核目标 == 这次打中的实体 → **不重复施加**任何东西（原版已经按加成后的攻击力算了伤害与默认强度击退，D-75 禁止自订数值）；
2. 复核目标 != 打中的实体 → 由服务端对被吸附目标结算 `Attributes.ATTACK_DAMAGE` 点伤害 + `knockback(0.4, …)` 原版默认强度；
3. 效果不在身、或 `zangief_piledriver` 在身（相位互斥）→ 直接放手。
   相位判定读**效果存在性**而不是调 Lane B 的 `ZangiefPiledriverController`，避免跨 lane 的源码依赖。

## 1. F-80 判据（探针一次跑完）

| 判据 | 构造 | 实测 | 判定 |
|---|---|---|---|
| 1 伤害 = 加成后数值 | 带效果（+40%）的假玩家攻击 4 格外的猪，出手冷却拉满（`LivingEntity.attackStrengthTicker=100`） | `attackDamageAttr=1.400`、猪血量 `10.000 → 8.600`、`dealt=1.400` | ✅ 等于 F3/attribute 读数 1.4 |
| 2 结算后被推开 ≥ 0.2 格 | 同一次攻击，用**带 AI** 的猪（`NoAi=false`）量下一 tick 的坐标差 | 攻击当刻 `delta=(0.000, 0.400)`；下一 tick `moved=0.400` 格 | ✅ ≥ 0.2，且强度就是原版默认 0.4（D-75） |
| 3 30° 外正常攻击、玩家不动 | 背后（180°）的猪 | 猪 `10.000 → 9.701`（普通攻击正常掉血）、玩家位移 `0.000` | ✅ 不吸附 |
| 4 同队玩家不结算 | 同队假玩家站在正前方 2 格，调 `settleSwing(player, null)` | `allied=true`、队友血量 `20.000 → 20.000` | ✅ 不吸附也不掉血 |
| 服务端独立复核 | 每次攻击 | 日志每次出现 `狂野猎人 目标 minecraft:pig 夹角 0.0 度 距离 4.65`（`settleSwing` 内部自己算了一遍） | ✅ 与客户端共用同一份算法 |

附注：`NoAi=true` 的猪攻击当刻同样拿到 `delta=0.400` 的击退冲量，但它下一 tick 不动（`NoAi` 不跑移动），
所以位移量这一项必须用 `NoAi=false` 的猪量——两种都记在日志里。

## 2. F-81 判据（同一次探针）

```text
[F80PROBE] killChain start health=5.00 duration=700
狂野猎人 续时 860     → kill 1 dead=true duration=860  health=15.00
狂野猎人 续时 1020    → kill 2 dead=true duration=1020 health=20.00
狂野猎人 续时 1180    → kill 3 dead=true duration=1180 health=20.00
狂野猎人 续时 1340    → kill 4 dead=true duration=1340 health=20.00
狂野猎人 续时 1500    → kill 5 dead=true duration=1500 health=20.00
狂野猎人 续时 1660    → kill 6 dead=true duration=1660 health=20.00
[F80PROBE] done duration=1660 health=20.00 effectPresent=true
```

- 判据1：连杀 6 只，每次 **+160 刻**，日志 **6 行** `狂野猎人 续时`。✅
- 判据2：第 6 次之后 `effectPresent=true`（无次数上限）；生命 5 → 15 → 20（第二次起被原版上限 20 截断）。✅
- 判据3：`git grep -nE "killCount|maxKill" -- src` **无输出**（exit 1）。✅

## 3. 探针与产物自查（按 V-40：删源码 ≠ 产物干净）

- 探针：`ProbeF80`（`@EventBusSubscriber` + 一次性开关 `run-p13/f80-probe.enabled`），取数后源码与开关都已删除。
- **重跑 `build` 之后**三处计数：
  - `src` 下 `*Probe*`：**0**
  - `build/classes` 下 `*Probe*`：**0**
  - `build/libs/farmstorm-0.0.2+mc1.21.1.jar` 内 `Probe` 条目：**0**
- 说明：先试过 `clean build`，`:clean` 失败（`build/` 被另一个进程占用：`build/moddev/artifacts/neoforge-21.1.213.jar`），
  改用增量 `build` 后 jar 重建成功、三处为 0。**如果以后要 clean，先确认没有别的 lane 的 java 在跑。**

## 4. 清场

- 服务端 `/stop`；`:25631/:25641` 无 LISTENING；未 commit；
- 未碰 `client/FarmstormClient.java`、`Farmstorm.java`、`docs/`、`datagen/`、`src/generated/`、
  `behavior/impl/ZangiefCaneTrap.java`、`effect/ZangiefPiledriverEffect.java`、`effect/WildHunterEffect.java`、`client/*`（Lane B 的类）。
