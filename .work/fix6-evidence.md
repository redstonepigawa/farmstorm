# FIX-6 证据（四条玩法修改 + 作者追加的「爆炸不伤使用者」）

任务书：`.work/TASK-FIX-6.md`（§2 与「追加第 5 条」两条更正已由派发者转达，按最新口径实施）。

## 0. 结论速览

四条改动 + 追加的第 5 条，全部有运行时证据（真服务端 + 真客户端玩家，非读码自证）：

| # | 需求 | 结果 |
|---|---|---|
| 1 | 只有破障类能破坏方块 | 6 个爆炸调用点全部改到位；运行时逐点验证（瓜片子/胡罗贝/浆果小爆炸破块数 = 0，育碧/毒育碧/瓜！破块数 = 44/18/28） |
| 2 | 胡罗贝触发后**不掉落**、直接消失（作者澄清版） | 触发后方块变空气、3x3x3 区域与放置前**逐块一致**、原地 ItemEntity = 0、背包胡罗贝 = 0 |
| 3 | 爆炸不分敌我 | 同一次爆炸同时伤到敌对生物（僵尸 5.90）、友善 NPC（村民 6.00）、以及站在爆点旁的玩家本人 |
| 4 | man瓜 射程 5→15 + 攻击敌对生物 + 归属放置者 | 僵尸在切比雪夫 10 格被锁定开火（旧默认 5 打不到）、20 格不锁定、鸡不锁定、命中伤害 `causing=player:Dev#380d…` = 放置者 |
| 5 | 爆炸不伤使用者（追加） | 改前：投掷者站在自己爆点旁 hp 20.0→20.0（**不掉血**）；改后：20.0→14.0（掉 6 点） |

最终探针结果：**PASS=28 FAIL=0**（`.work/fix6-probe-after.txt`，全文另见 §5）。

## 1. 改了什么（逐条改前 / 改后）

### 1.1 只有破障类能破坏方块

`explosion/BlastRule.java` 新增预置规则（第 43 行）：

```java
/** 非破障类专用（FIX-6 §1）：谓词恒 false，任何方块都不破坏，因此也不产生任何掉落。 */
public static final BlastRule NEVER_BREAK = new BlastRule(state -> false, 0.0F, false);
```

同时删掉已成死代码的 `BlastRule.sliceBomb()`（瓜片子是它唯一调用者，改完后无人引用；留着会让
`explosion.sliceResistThreshold` 看上去仍在生效）。

逐调用点（`grep 'BlastRule\.'` 全部命中点）：

| 调用点 | 文件:行 | 改前 | 改后 | 能否破方块 | 运行时证据 |
|---|---|---|---|---|---|
| 瓜片子 | `entity/thrown/MelonSliceProjectile:42` | `sliceBomb()`（阈值 6.0） | **`NEVER_BREAK`** | 不能 | 3x3x3 变化 0，ItemEntity 0 |
| 瓜！（含 4 枚子母弹） | `entity/thrown/MelonGrenadeProjectile:47` | `grenade()` | 不变 | 能 | 3x3x3 变化 28 |
| 育碧服务器 | `entity/thrown/UbiServerProjectile:42` | `UNBREAKABLE_SKIP` | 不变 | 能 | 3x3x3 变化 44，掉落 3 |
| 毒育碧服务器 | `entity/thrown/ToxicUbiServerProjectile:45` | `VANILLA` | 不变 | 能 | 3x3x3 变化 18 |
| 胡罗贝 | `behavior/impl/BlastCarrotTrap:66` | `VANILLA` | **`NEVER_BREAK`** | 不能 | 3x3x3 与放置前逐块一致 |
| 幸运浆果小爆炸 | `util/LuckyBerryRoller:214` | `UNBREAKABLE_SKIP` | **`NEVER_BREAK`** | 不能 | 3x3x3 变化 0 |

瓜！的 4 枚子母弹是 `MelonSliceProjectile`，自动跟着变成「不破坏」——符合「只有破障类能破坏」。
运行时也观测到子母弹在场（`子母弹残留=4`）且没有额外方块被破坏。

### 1.2 胡罗贝：不掉落、直接消失（作者澄清后**未加任何掉落逻辑**）

- `blockentity/DeployedCropBlockEntity#consume()` **一字未动**，仍是 `server.removeBlock(pos, false)`（不掉落）。
- 作者观察到的「触发时掉出胡罗贝」根因（读码链条，三条互相印证）：
  1. `data/farmstorm/tags/block/explosion_immune.json` 只含 `awakened_torchflower` / `awakened_pitcher_plant`，
     **不含 `blast_carrot`**；
  2. 旧规则 `BlastRule.VANILLA` 的阈值是 3.6e6，胡罗贝方块抗性远低于它 → 会被自己的爆炸炸掉；
  3. `src/generated/resources/data/farmstorm/loot_table/blocks/blast_carrot.json` 的掉落条件只有
     `minecraft:survives_explosion`，掉落物就是 `farmstorm:blast_carrot` 自身 → 于是「爆炸炸掉方块 → 走战利品表 → 掉出胡罗贝」。
- 改为 `NEVER_BREAK` 后，胡罗贝的爆炸对任何方块（**含它自己**）都无效，掉落链条从源头断掉；
  与「先 `consume()` 还是先爆炸」的顺序无关（`ExplosionQueue` 可能把爆炸推迟 ≤1 tick，两种顺序都被覆盖）。

### 1.3 爆炸不分敌我（验证 + 一处修正）

- **读码证据（全链路）**：`grep isAlly` 全仓 15 行命中（其中 2 行是「谓词里没有 isAlly」的说明性注释），
  其余全部落在**索敌 / 触发 / 能否被挖**三处，**没有一处在伤害路径**：
  - 索敌/触发：`PumpkinTurret:89`、`PumpkinTurret:149`（投掷物拦截）、`BlastCarrotTrap:43`、`FangTrapCactusTrap:40`、`AuraProvider:38`
  - 方块能否被挖：`DeployedCropBlock:218`、`DeployedCropBlock:244`
  - 归属判定本体：`DeployedCropBlockEntity:280`、`DeployedOwnership`、`ScoreboardTeamResolver`、`TeamResolver`
  - `explosion/FarmstormExplosion#hurtCapped` 里 0 处 `isAlly`。
- **运行时证据**：一次瓜片子爆炸（投掷者 = Dev）同时造成
  `zombie 5.90` / `villager 6.00` 两条 `farmstorm:crop_blast` 伤害；胡罗贝爆炸（归属者离线）同时造成
  `zombie 0.94` / `player 1.00`。
- **唯一改动**：`FarmstormExplosion#hurtCapped` 里原先有一句自我排除（任务书「追加第 5 条」）：

```java
// 改前
Entity directSource = this.getDirectSourceEntity();
if (entity == directSource || entity.ignoreExplosion(this)) { continue; }
// 改后
if (entity.ignoreExplosion(this)) { continue; }
```

  说明（避免误记）：原版 `Explosion#explode` 用 `this.level.getEntities(this.source, aabb)` 收集实体，
  **原版同样不伤「爆炸的来源实体」**，所以那句话是「与原版一致」而非「本模组的私货」。
  按作者口径（不分敌我、也不宽恕使用者）删掉后，行为**故意偏离原版**，并已写进方法注释。
  `TurretBullet` 走的是另一条路（直接 `hurt(damageSource)`），不受这行影响，未改动。
- **改前 / 改后对照**（同一套探针、同一场景、同为真客户端玩家）：

| 场景 | 改前（探针 v1） | 改后（探针 v3） |
|---|---|---|
| 玩家站在自己投出的瓜片子爆点旁 2 格 | `玩家 hp 20.0 → 20.0（来源=<null>）` **FAIL** | `玩家 hp 20.0 → 14.0（来源=player:Dev#380d…）` **PASS** |
| 玩家站在自己触发的浆果小爆炸点上 | `玩家 hp=20.0` **FAIL** | `玩家 hp=5.0（来源=player:Dev#380d…）` **PASS** |

  （改前那次运行另有 4 条 FAIL 是我探针自己的断言/测量问题：狼实体被移除、胡罗贝快照口径、
  反向炮台地形快照含炮台自身、白天僵尸自燃。已逐条修正，与代码无关。）

### 1.4 man瓜：射程 15 + 攻击敌对生物 + 归属放置者

- **射程**：`config/FarmstormConfig.java:341` `defineInRange("targetRadius", 5, 0, 64)` → **`15, 0, 64`**（上界不动）。运行时 `config pumpkin_turret.targetRadius=15`。
- **索敌**（`behavior/impl/PumpkinTurret.java`）：

```java
// 改前：nearestPlayer(...) → nearestProjectile(...)
// 改后：
Entity target = nearestHostile(be, level, pos, radius);   // 非己方玩家 ∪ Monster，取切比雪夫最近
if (target == null && INTERCEPT_PROJECTILES) { target = nearestProjectile(be, level, pos, radius); }
```

  - `nearestHostile` = 先跑原有的 `nearestPlayer`（旁观/创造/己方排除规则一字未改），再扫
    `level.getEntitiesOfClass(Monster.class, searchBox(pos, radius))`，按 `DistanceMetrics.chebyshev` 取最近；
    平手判给玩家（保持既有优先级）。动物/村民不在 `Monster` 里，因此不会成为目标。
  - 抽取 `searchBox(pos, radius)`（边长 2r+1 的立方体）给 `nearestProjectile` 复用，去掉重复的 span 计算。
  - 冷却窗口内不索敌的既有行为未动（`TurretBehavior#tick` + `be.setCooldown`）。
- **归属**：核查结论是**原本已满足**——`fire()` 里 `be.owner(level)` 非空时用
  `new TurretBullet(type, owner, level)`，而 `ThrowableProjectile(EntityType, LivingEntity, Level)`
  构造器内部就调 `this.setOwner(shooter)`（1.21.1 源码 `ThrowableProjectile.java:20-23` 实测），
  `TurretBullet#onHitEntity` 用的正是 `FarmstormDamageTypes.voidBolt(server, this.getOwner())`。
  放置者离线时 owner 为 null（`be.owner` 只查 `PlayerList`），按任务书允许。
  **运行时证据**：`damage victim=zombie type=farmstorm:void_bolt amount=0.94 causing=player:Dev#380d… direct=player:Dev#380d…`
  → `damageSource.getEntity()` 就是放置者。

## 2. 与上游文档 / 原口径的冲突登记（一律按作者为准）

| 编号 | 冲突 | 处置 |
|---|---|---|
| F6-01 | D-07 / DESIGN §7.6 只写「炮台锁定非己方玩家」 | 按作者新增敌对生物目标；玩家优先级与排除规则不变 |
| F6-02 | D-21 的 **数值原样** 口径 vs 作者要求射程 5→15 | 改默认值，范围上界 64 不变 |
| F6-03 | C-2 / D-19 的 `explosion.sliceResistThreshold` 以瓜片子为作用对象 | 瓜片子改为不破坏方块后，**该键不再影响任何行为**（键位按 HC-01 保留，117 键不变，`verify_project` 仍 117/117）。若作者要「删键」，需同步改文档口径；我未删 |
| F6-04 | 原版 `Explosion#explode` 排除「爆炸来源实体」，本模组现在刻意不排除 | 作者口径，已在方法注释注明这是与原版的**故意差异** |
| F6-05 | `demolition` 进度口径（PLAN R-14：任何本模组爆炸破坏 ≥1 方块即授予） | 非破障类不再破坏方块 → 瓜片子/胡罗贝/浆果小爆炸**不再触发 `demolition`**；破障类仍触发。属预期副作用，登记 |

## 3. config 默认值变更对已有存档的影响（必须提醒作者）

- 改默认值**不会**改已有的 `run/config/farmstorm-server.toml` 或作者存档里的 config：
  - `D:\project_farm\run\config\farmstorm-server.toml`（作者既有配置）实测仍是 `targetRadius = 5`；
  - 全新目录 `run-fix6\config\farmstorm-server.toml`（本次生成）是 `targetRadius = 15`。
- 要让 15 生效：删掉 `config/farmstorm-server.toml` 重新生成，或手动把 `pumpkin_turret.targetRadius` 改成 15
  （服务端 config 在启动时读取，改完要重启）。

## 4. 验收命令与结果

```
cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava     → BUILD SUCCESSFUL，零 warning 零 note
cmd /c ""D:\project_farm\.tools\gw.cmd" build           → BUILD SUCCESSFUL，build/libs/farmstorm-0.0.1+mc1.21.1.jar 328,923 B
python -X utf8 tools/verify_project.py                    → 结论：FAIL 0 项，WARN 0 项（117 配置键逐字一致 / 62 注册对象 / 218 语言键 / 无 Mixin·TODO·调试残留·空 catch）
```

临时探针已删净：`src` 全树 grep `Fix6Probe|FIX6_PROBE` = 0 命中；`build/classes` 的 `*Probe*` = 0；
交付 jar 内 `Probe` 条目 = 0。

## 5. 运行时证据全文

运行方式（真服务端 + 真客户端，跑两次：改前 / 改后）：

```
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-fix6
cmd /c ""D:\project_farm\.tools\gw.cmd" runClient -PfsRunDir=run-fix6c --init-script <把 --quickPlayMultiplayer 127.0.0.1:25566 追加进 build/moddev/clientRunProgramArgs.txt 的 init script>
```

为什么必须起真客户端：`blast_carrot` 的触发扫描只看 `ServerLevel#players()`，`PumpkinTurret#fire` 的 owner 只查
`PlayerList#getPlayer(uuid)`——假玩家（`FakePlayer`）两条都进不去，拿不到「真玩家触发 / 归属真放置者」的证据。
场景搭在 y=120 的空中平台（`mobGriefing=true`、`naturalRegeneration=false`、`doMobSpawning=false`、永夜防僵尸自燃）。

改后（`PASS=28 FAIL=0`，摘自 `run-fix6/logs/latest.log`，全量另存 `.work/fix6-probe-after.txt`）：

```
join name=Dev uuid=380df991-f603-344c-a090-369bad2a924a dim=minecraft:overworld
config pumpkin_turret.targetRadius=15（期望 15）
gamerule mobGriefing=true（破坏方块测试必需 true）
--- step 2/19 slice:place
damage victim=villager type=farmstorm:crop_blast amount=6.00 causing=player:Dev#380df991-... victimHpAfter=14.0
damage victim=zombie   type=farmstorm:crop_blast amount=5.90 causing=player:Dev#380df991-... victimHpAfter=14.1
--- step 3/19 slice:check
PASS 瓜片子·不破坏方块 | 3x3x3 区域变化方块数=0（期望 0；旧规则下这里会破）
PASS 瓜片子·无方块掉落 | 半径 8 内 ItemEntity=0
PASS 爆炸伤害敌对生物 | 僵尸 hp=14.096 < 20（来源=player:Dev#380d…）
PASS 爆炸伤害友善 NPC（不分敌我） | 村民 hp=14.0 < 20.0｜alive=true pos=(-1.5, 122.0, 0.5) 场景内村民数=1
PASS 爆炸伤害归属投掷者 | 僵尸受击来源=player:Dev#380d…
--- step 5/19 ubi:check
PASS 育碧服务器·破坏方块 | 3x3x3 区域变化方块数=44（期望 > 0）
PASS 育碧服务器·破块后有掉落物 | 半径 12 内 ItemEntity=3
--- step 7/19 toxic:check
PASS 毒育碧服务器·破坏方块 | 3x3x3 区域变化方块数=18（期望 > 0）
--- step 9/19 grenade:check
PASS 瓜！·破坏方块（破障类） | 3x3x3 区域变化方块数=28（期望 > 0）
grenade 收尾：子母弹残留=4（子母弹走 NEVER_BREAK，二次爆不破块）
--- step 10~11 幸运浆果小爆炸
damage victim=player type=farmstorm:crop_blast amount=15.00 causing=player:Dev#380d… victimHpAfter=5.0
PASS 幸运浆果小爆炸·不破坏方块 | 3x3x3 区域变化方块数=0（期望 0）
PASS 幸运浆果小爆炸·伤到站在原地的食用者 | 玩家 hp=5.0（来源=player:Dev#380d…）
--- step 13/19 carrot:check（胡罗贝）
damage victim=zombie type=farmstorm:crop_blast amount=0.94 causing=<null> victimHpAfter=19.1
damage victim=player type=farmstorm:crop_blast amount=1.00 causing=<null> victimHpAfter=19.0
PASS 胡罗贝·触发后方块消失 | 胡罗贝位置方块=Block{minecraft:air}
PASS 胡罗贝·爆炸不破坏任何别的方块（含自身被炸走） | 3x3x3 区域与「放置胡罗贝之前」逐块一致，变化=[]
PASS 胡罗贝·原地无掉落物 | 半径 8 内 ItemEntity=0，其中胡罗贝物品=0
PASS 胡罗贝·玩家背包里也没有胡罗贝 | 背包胡罗贝数=0
PASS 胡罗贝爆炸伤到旁边僵尸 | 僵尸 hp=19.06
PASS 胡罗贝爆炸也伤到站在旁边的玩家（不分敌我） | 玩家 hp 20.0 → 19.0
--- step 15/19 self:check（使用者自伤 = 追加第 5 条）
damage victim=player type=farmstorm:crop_blast amount=6.00 causing=player:Dev#380d… victimHpAfter=14.0
PASS 使用者自伤·投掷者站在爆点旁会掉血 | 玩家 hp 20.0 → 14.0（来源=player:Dev#380d…）
PASS 使用者自伤·同一次爆炸仍然不破块 | 3x3x3 变化方块数=0
--- step 16~17/19 炮台（正向）
turret 场景：man瓜 @BlockPos{x=48, y=121, z=64} 归属者=Dev（在线 → 导弹 owner 应等于放置者）
turret 场景：僵尸 @(48,121,74) 切比雪夫 10 格（旧默认 5 打不到，新默认 15 应锁定）；玩家(放置者)站 2 格内 hp=20.0
turret 锁定开火：TurretBullet 在场，owner=player:Dev#380d… target=zombie:Zombie#9fa59696-… 位置=BlockPos{x=48, y=121, z=65}
damage victim=zombie type=farmstorm:void_bolt amount=0.94 causing=player:Dev#380d… direct=player:Dev#380d… victimHpAfter=19.1
PASS man瓜·锁定切比雪夫 10 格外的敌对生物并开火 | 观测到 TurretBullet=true
PASS man瓜·命中后僵尸掉血 | 僵尸 hp 20.0 → 19.06
PASS man瓜·伤害归属放置者 | 僵尸受击来源=player:Dev#380d…（期望=player:Dev#380d…）
PASS man瓜·放置者(己方)不被锁定 | 曾锁定玩家=false，玩家 hp=20.0，玩家受击记录=null（应为 null）
--- step 19/19 炮台（反向）
PASS man瓜·鸡(动物)不成为目标 | 鸡 hp=4.0 受击记录=null
PASS man瓜·20 格外的僵尸不成为目标（半径 15 生效） | 僵尸 hp=20.0 受击记录=null（应为 null）
PASS man瓜·反向炮台 12 格内没有导弹 | TurretBullet 在场=0
PASS man瓜·炮台从不锁定放置者 | 曾锁定玩家=false
PASS man瓜·反向场景炮台没破坏地形 | 地形未变
SUMMARY PASS=28 FAIL=0
```

改前（同一探针 v1，只差 `FarmstormExplosion` 那一行，`PASS=21 FAIL=6`，全量另存 `.work/fix6-probe-before.txt`）：

```
PASS 瓜片子·不破坏方块 / 育碧·破坏方块 / 瓜！·破坏方块 / 胡罗贝·无掉落 …（§1 §2 改动当时已生效）
FAIL 幸运浆果小爆炸·伤到站在原地的食用者 | 玩家 hp=20.0
FAIL 使用者自伤·投掷者站在爆点旁会掉血 | 玩家 hp 20.0 → 20.0（来源=<null>）
（另 4 条 FAIL 为探针自身断言/测量缺陷：狼实体被移除、胡罗贝快照口径、反向炮台地形快照含炮台自身、白天僵尸自燃；已修正）
SUMMARY PASS=21 FAIL=6
```

## 6. 未决 / 需要作者拍板

1. `explosion.sliceResistThreshold`（F6-03）现在不影响任何行为。保留键（117 键不变）还是删键改文档，请作者定。
2. `demolition` 进度不再被非破障类授予（F6-05）。若希望「炸到方块以外的东西也算拆迁」，需要另立判据。
3. 炮台索敌口径：现在是「玩家 ∪ 敌对生物」取最近，投掷物仍然只在没有这两类目标时才拦。
   若作者希望「投掷物也参与最近者竞争」，需要改一行取最逻辑（当前按原 C-3 优先级保留）。
4. 本机无客户端截图交付：本次验收只要求日志级证据，未做截图。

## 7. 探针的实现方式（供复核；源码已按验收要求删除）

服务端 `@EventBusSubscriber` 探针：`PlayerLoggedInEvent` 起脚本，`ServerTickEvent.Post` 按固定刻数逐条推步骤，
每步只做「摆场景 / 断言」两件事，断言全部读**真实世界状态**（`level.getBlockState` 快照对比、
`getEntitiesOfClass(ItemEntity.class, ...)` 计数、`LivingEntity#getHealth`、`LivingEntity` 的
`LivingDamageEvent.Post` 里 `getSource().getEntity()`），最后 `server.halt(false)` 退出。
两处测量陷阱已在实现里规避：
- `getSeenPercent` 是视线判定：生物贴平台面时射线会被平台方块挡住 → 伤害接近 0。场景里把生物浮在平台上方 2 格。
- 自然回血/白天僵尸自燃会把「掉血了」读成「没掉血」→ 关 `naturalRegeneration`、世界设为永夜。
