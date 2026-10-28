# 任务书 M2-B · 5 个部署行为（Phase 4：F-19 ~ F-25）

> 先读 `.work/PLAN.md` §0 与 §4。前置：M1-A（注册层）与 M2-A（爆炸/事件地基）均已交付且 `build` 为绿。

## 1. 目标

把 5 个部署物从「能放下的方块」变成「会做事的装置」：行为接口家族 + 5 个实现 + 部署限流。

## 2. 必读
1. `docs/zh/02-TECH-SPEC.md` §4.6（行为家族与 5 个实现的行为表）、§4.4（部署方块破坏规则）、§4.5（BE 字段与 tick 分发）、§8.1（错峰）/§8.3（防刷）。
2. `docs/zh/01-DESIGN.md` §4.2（胡罗贝）、§4.8（man瓜）、§4.9（火把花）、§4.10（瓶子草）、§4.11（仙人掌）、§3.3（部署系统）、§3.7（距离度量）。
3. `docs/zh/00-DECISIONS.md`：D-07、D-13、D-14、D-20、D-27、D-28、D-30、D-37、C-3、C-4、C-8、C-9。

## 3. 交付物

### 3.1 接口与工厂
`behavior/DeployedBehavior.java`、`TrapBehavior.java`、`TurretBehavior.java`、`AuraProvider.java`、`DeployedBehaviors.java`——签名与默认方法照 TECH §4.6；`DeployedBehaviors.of(DeployedKind)` 返回静态单例。

### 3.2 五个实现（`behavior/impl/`）
| 文件 | 关键数值（全部从 config 读，不得硬编码） |
|---|---|
| `BlastCarrotTrap.java` | 切比雪夫 ≤ `blast_carrot.triggerRadius`；`FarmstormExplosion(power=explosionPower, cap=maxDamage)`；爆炸半径 4 内**所有玩家**（含队友，D-27）反胃 `nausea.level` / `nausea.durationTicks`；触发后 `be.consume()` |
| `FangTrapCactusTrap.java` | 欧氏 ≤ `triggerRadius`；外圈 `outerFangCount` 个延迟 `outerDelayTicks`、半径 `outerRadius`；内圈 `innerFangCount` 个延迟 `innerDelayTicks`、半径 `innerRadius`；尖牙用原版 `EntityType.EVOKER_FANGS`，owner = 部署者；两波出完 `consume()` |
| `PumpkinTurret.java` | 切比雪夫 ≤ `targetRadius`；开火间隔 `fireIntervalTicks`；生成 `TurretBullet`；命中 `farmstorm:void_bolt` `projectileDamage` 点 + 沿「南瓜→目标」击退 `knockbackLevel`、**不给漂浮**；己方不成为目标；`interceptProjectiles` 为真时对非己方投掷物也开火（C-3） |
| `TorchflowerAura.java` | 欧氏 `auraRadius`；每 `refreshTicks` 对己方玩家施加 6 个效果（TECH §4.11.3 的 6 条 `awakened_torchflower.*`）；主手或副手是 `#minecraft:swords` → `TempEnchantHandler.grant(..., FIRE_ASPECT, fireAspectLevel, refreshTicks*3)`；每次施加播 `particlesPerRefresh` 个 flame |
| `PitcherPlantAura.java` | 欧氏 `auraRadius`；每 `refreshTicks` 施加 3 个效果；`clearHarmful` 为真时移除全部 `MobEffectCategory.HARMFUL`；`particlesPerRefresh` 个 heart |

### 3.3 `blockentity/DeployedCropBlockEntity.java` 的 tick 分发与限流
- 补全 `serverTick`：`drainPendingWaves` → 冷却递减早退 → `(now + stagger) % behavior.scanInterval() == 0` 主扫描（TECH §4.5）。
- 部署限流（内部常量，HC-13 禁止进 config）：`DEPLOY_COOLDOWN_TICKS=5`、`MAX_DEPLOYED_PER_CHUNK=8`、`MAX_DEPLOYED_PER_PLAYER=32`；被拒绝时不消耗物品、不产生掉落。
- 光环叠加：同 tick 同玩家只施加一次（`Set<UUID>` 去重）+ `MAX_EFFECTS_PER_TICK=64` 熔断（RT-06）。

## 4. 不做
- 不改 `registry/**`（注册已定稿）。
- 不实现投掷物（M3）与转换器（M4）。
- 不在客户端做任何判定（HC-03）。

## 5. 约束
- HC-09：陷阱/炮台只针对**非己方玩家**；唯一例外是 man瓜 拦截投掷物。
- HC-08/C-9：己方判定在触发瞬间实时解析，**不缓存队伍快照**，已部署的装置在玩家入队/退队后立刻生效、无需重新部署。
- D-27：胡罗贝与红/棕菇的范围效果**不筛敌我**。
- 罗马数字 → amplifier 一律 `level - 1`。
- 不写 TODO / 不注释实现 / 不留空方法。

## 6. 验收
1. `gw.cmd compileJava`、`gw.cmd build` 绿；`gw.cmd runServer` 出现 `Done (`。
2. 5 种部署物各放 1 个，跑 600 tick 无异常（用 runServer 日志或命令验证，写清怎么验证的）。
3. 同区块 8 个部署物按 `stagger` 分摊到不同 tick；冷却窗口内不做扫描（日志计数 ≤ 2 / 每 tick）。
4. 容器/静态检查证据：`behavior/impl/` 里出现任何硬编码玩法数值即为失败（应全部走 config）。
5. 补充 `tools/verify_project.py` 的 `--phase m2` 通过（若该选项尚不存在，忽略本项）。

## 7. 回报
按 `docs/zh/05-PROMPT-MAIN.md §7` 模板；额外写清：每个行为读的 config 键清单、你无法在无客户端环境验证的项、以及给 M3/M4 的接口约定。
