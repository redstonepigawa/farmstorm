# FIX-8 证据（胡罗贝爆炸伤害 + 两种蘑菇落地孢子雾）

> 执行者：实现工程师（FIX-8）。全部日志在 `.work/`，可逐行复核。

## 0. 结论速览

| 项 | 结论 |
|---|---|
| 作者判断的根因 | **成立，且已实测复现**：爆炸中心埋在陷阱方块内部 → `Explosion#getSeenPercent` = **0.0** → 公式只剩保底 `+1.0` → 恰好 1 点伤害 |
| 修法（根因） | `BlastCarrotTrap#trigger` 把 `be.consume()` 提到 `ExplosionQueue.submit()` **之前**（方块先消失，爆炸中心所在格变成空气） |
| 修法（防御） | `FarmstormExplosion#hurtCapped`：`seen <= 0` 时把**取样中心**沿 +Y 抬到最近的空格再算一次；距离衰减、威力、reach、封顶全部不动，无任何常数加权 |
| 改前 / 改后伤害（贴脸，dist=1.118） | **1.0 → 7.0**（= `blast_carrot.maxDamage` 默认 7） |
| 育碧服务器 / 瓜！ 贴脸回归 | **2.0 / 8.0**，等于各自 maxDamage，改前改后一致（它们不受同一 bug 影响） |
| 两种蘑菇特效 | 落地播 32（红）/ 28（棕）个染色的 `ENTITY_EFFECT` + 12 / 10 个 `SMOKE` + 1 个音效；水平扩散 = 各自 `radius` config（4.0） |
| 构建 | `compileJava` / `build` 绿，零 javac warning；`verify_project.py` **FAIL 0 / WARN 0**，117 键不变 |
| 探针 | 已删净：`build` 无 `*Probe*`、jar 内无 `Fix8`/`Probe` 条目、`src` grep `FIX8|Fix8|Probe` = 0 |

## 1. 根因复核（先测后改）

### 1.1 探针怎么搭的
临时类 `com.dashpig.farmstorm.entity.thrown.Fix8Probe`（`@EventBusSubscriber` + `ServerTickEvent.Post`，第 60 tick 执行后 `server.halt`）：
在出生点旁的空中 `y=120` 铺 5x5 石头地面，每列间隔 12 格，逐列做一次测量；伤害目标用 `Villager`
（`LivingEntity`，`hurt` 走原版路径、白天不燃烧，包围盒 0.6×1.95 最接近玩家 0.6×1.8），每次测量前调用
`setHealth(getMaxHealth())` 复位；站位固定为「陷阱 +X 方向一格、脚底贴着地面」，即 `dist = 1.118033988749895`。
伤害一律「触发前血量 − 触发后血量」，不读内部变量。

### 1.2 改前实测（`.work/fix8-before-server.log`）

```
[FIX8] config blast_carrot.power=2 maxDamage=7 / ubi_server.maxDamage=2 / melon_grenade.maxDamage=8
[FIX8] A seen%: 方块中心=0.0 顶面=0.4 顶面+1e-3=0.4
[FIX8] A 陷阱方块有碰撞箱=true
[FIX8] A seen%: 方块中心（陷阱方块已移除）=1.0
[FIX8] B 胡罗贝 端到端：center=(-35.5, 121.5, -15.5) dist=1.118033988749895 seen=0.0 reach=4.0 cap=7 health 20.0 -> 19.0 实际伤害=1.0
[FIX8] C 育碧服务器：center=(-22.5, 121.0, -15.5) dist=0.0 seen=1.0 cap=2.0 health 20.0 -> 18.0 实际伤害=2.0
[FIX8] D 瓜！：center=(-10.5, 121.0, -15.5) dist=0.0 seen=1.0 cap=8.0 health 20.0 -> 12.0 实际伤害=8.0
[FIX8] E 中心埋在石头里：center=(0.5, 121.5, -15.5) dist=1.118033988749895 seen=0.0 cap=7.0 health 20.0 -> 19.0 实际伤害=1.0
```

三条独立证据链闭合：
1. **遮挡的是陷阱自己**：同一实体、同一取样点，方块在 → `seen=0.0`；只把方块 `removeBlock` → `seen=1.0`。
2. **端到端退化**：走真实 `BlastCarrotTrap#trigger`（不放水、不直接 new 爆炸），伤害 = 1.0，与作者观察一致。
3. **退化签名可复现**：E 组把中心刻意埋进石头，同样 `seen=0.0` → 伤害 1.0，说明「1 点」= 公式保底 `+1.0`，不是别的路径。

### 1.3 为什么没选「把中心挪到方块顶面」

任务书建议的第一方案是 `pos.getBottomCenter().add(0, 1.0, 0)`。**实测否决**：方块顶面正好是碰撞箱边界，
射线从边界出发仍被自己挡掉大半 → `seen = 0.4`（上表 A 组）。按公式推算顶面中心那一版贴脸伤害只有
`(1-1.414/4)*0.4 = 0.2586 → (0.2586²+0.2586)/2*7*4+1 ≈ 5.56`，**低于 cap 7，达不到验收要求**（此为公式推算，非实测）。

## 2. 改动（4 个文件，均在任务书允许范围内）

| 文件 | 改动 |
|---|---|
| `behavior/impl/BlastCarrotTrap.java` | `trigger` 中 `be.consume()` 从末尾提到 `submit()` 之前，并写明理由（`ExplosionQueue` 可能延后，所以不能依赖 `detonate` 的时机）。`applyNausea` 仍在引爆后执行，中心仍取方块的 `pos.getCenter()`——**爆炸几何、reach、反胃范围一处没动** |
| `explosion/FarmstormExplosion.java` | `hurtCapped` 里 `seen <= 0` 时调 `liftedSampleCenter(center)` 把**取样中心**沿 +Y 抬到最近空格再算一次 `seen`；新增 2 个内部常量（`MAX_SAMPLE_LIFT_BLOCKS=4`、`SAMPLE_LIFT_EPSILON=1e-3`）+ 2 个私有方法。距离衰减 `(1-d/reach)`、`raw` 公式、`Math.min(raw, damageCap)` 逐字未动 |
| `entity/thrown/RedMushroomPuffProjectile.java` | `onImpact` 末尾调 `spawnSporeCloud`：`ENTITY_EFFECT` 染 `0xC03030` × 32 + `SMOKE` × 12 + `SLIME_BLOCK_BREAK`（pitch 0.9）；水平扩散 = `red_mushroom_puff.radius` |
| `entity/thrown/BrownMushroomPuffProjectile.java` | 同上，`0x8B5A2B` × 28 + `SMOKE` × 10 + pitch 0.7；水平扩散 = `brown_mushroom_puff.radius` |

配置面：**未新增任何 config 键**（117 键不变，`verify_project.py` 第 1 项 PASS）；粒子数/颜色/纵向扩散/抬升步数全部是内部常量。

## 3. 改后实测（`.work/fix8-final-probe.log`，代码即交付版）

```
[FIX8] B 胡罗贝 端到端：center=(-35.5, 121.5, -15.5) dist=1.118033988749895 seen=0.0 reach=4.0 cap=7 health 20.0 -> 13.0 实际伤害=7.0
[FIX8] B 触发后陷阱方块是否已被移除=true（air=true）
[FIX8] C 育碧服务器：center=(-22.5, 121.0, -15.5) dist=0.0 seen=1.0 cap=2.0 health 20.0 -> 18.0 实际伤害=2.0
[FIX8] D 瓜！：center=(-10.5, 121.0, -15.5) dist=0.0 seen=1.0 cap=8.0 health 20.0 -> 12.0 实际伤害=8.0
[FIX8] E 中心埋在石头里：center=(0.5, 121.5, -15.5) dist=1.118033988749895 seen=0.0 cap=7.0 health 20.0 -> 13.802445 实际伤害=6.1975546
[FIX8] E 埋点 seen%（应为 0，说明中心被自己所在方块挡住）=0.0 → 抬到空格后 seen=0.4
```

- **B：1.0 → 7.0 = `blast_carrot.maxDamage`**，验收项达成；`seen` 打印仍是 0.0 是因为探针在**触发前**读的（那时方块还在），真实引爆时方块已被 `consume`。
- **C / D 回归无变化**：2.0 / 8.0，分别等于 `ubi_server.maxDamage` / `melon_grenade.maxDamage`。它们的中心在命中点（空气中），`seen = 1.0`，**本来就不受同一 bug 影响**。
- **E 防御层生效**：中心埋在石头里 → `seen=0` → 抬到空格后 `seen=0.4` → 伤害 6.1975546，不再是 1.0；与手算
  `0.7205×0.4=0.28822 → (0.28822²+0.28822)/2*28+1 = 6.198` 完全吻合，证明抬升**没有引入任何额外常数加权**。

## 4. 蘑菇孢子雾

### 4.1 参数
| 作物 | 主粒子 | 数量 | 染色 | 烟 | 烟数 | 水平扩散 / 纵向 | 速度 | 音效 |
|---|---|---|---|---|---|---|---|---|
| 红菇 | `ParticleTypes.ENTITY_EFFECT`（`ColorParticleOption`） | 32 | `0xC03030`（192,48,48） | `ParticleTypes.SMOKE` | 12 | `radius`=4.0 / 0.3 | 0.02 / 0.01 | `SLIME_BLOCK_BREAK` 0.8 音量 pitch 0.9 |
| 棕菇 | 同上 | 28 | `0x8B5A2B`（139,90,43） | 同上 | 10 | `radius`=4.0 / 0.3 | 0.02 / 0.01 | 同上，pitch 0.7 |

实现：每个类各 2 次 `ServerLevel#sendParticles` + 1 次 `playSound`，服务端广播、不新增网络包（C-6）。
1.21.1 的 API 差异：`EntityEffectParticleOption` 已不存在，`ParticleTypes.ENTITY_EFFECT` 的类型是
`ParticleType<ColorParticleOption>`，正解是 `ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b)`（已按真实 API 编译通过）。

### 4.2 广播证据（动态，但**不是画面**）
在正式代码里临时插过一行 `[FIX8-TEMP]` 日志打印传给 `sendParticles` 的实参，跑一轮专用服务端后取到：

```
[FIX8-TEMP] 红菇 sendParticles: ENTITY_EFFECT count=32 spread=4.0/0.3/4.0 speed=0.02 color=#c03030 | SMOKE count=12 speed=0.01 | sound=SLIME_BLOCK_BREAK pos=(12.5, 121.0, -15.5)
[FIX8-TEMP] 棕菇 sendParticles: ENTITY_EFFECT count=28 spread=4.0/0.3/4.0 speed=0.02 color=#8b5a2b | SMOKE count=10 speed=0.01 | sound=SLIME_BLOCK_BREAK pos=(14.5, 121.0, -15.5)
```

即：服务端确实走到了这条代码路径、实参与上表一致（日志行随后**已删除**，`src` grep `FIX8-TEMP` = 0）。
**未验证项**：粒子在客户端的实际观感（颜色浓淡、4 格扩散是否偏稀）——本机只有专用服务端，没有真实的
`ClientboundLevelParticlesPacket` 接收端，无法给截图级证据。按任务书 §4.4 的口径：这属于「只能做到参数级论证」。

## 5. 构建与验收

| 项 | 命令 / 结果 |
|---|---|
| 编译 | `gw.cmd compileJava` → `BUILD SUCCESSFUL`（`.work/fix8-compile-final.log`，`--rerun-tasks` 强制真编译） |
| 警告 | javac 无 `warning`；仅一条**既有** `Note: ModAdvancementProvider.java uses or overrides a deprecated API`（FIX-7 的 `fix7-compile2.log` 同样有，非本次引入，文件本次未改） |
| 打包 | `gw.cmd clean build` → `BUILD SUCCESSFUL`，`build/libs/farmstorm-0.0.1+mc1.21.1.jar` 336,451 B（`.work/fix8-build.log`） |
| 静态验收 | `tools/verify_project.py` → **FAIL 0 / WARN 0**；117 键与文档逐字一致；64 注册 ID 齐全；无 Mixin/TODO/调试残留/空 catch |
| 服务端 | `gw.cmd runServer -PfsRunDir=run-fix8` → `Done (4.559s)`，全维度存档、RCON 停止，`BUILD SUCCESSFUL in 1m 6s`；日志内 `/ERROR`、`/FATAL` = 0（`.work/fix8-clean-server.log`） |
| 探针残留 | `build` 下 `*Probe*` = 0；jar 内 `Probe|Fix8` 条目 = 0；`src` 内 `FIX8|Fix8|Probe` = 0 |

## 6. 未验证 / 需调度者裁决

1. **客户端画面**：粒子实际观感没有截图证据（理由见 §4.2）。若要画面级证据，需按 R-46 的 `runClient` + quickPlay + 截图流程另跑一轮。
2. **真实 `ServerPlayer` 血量**：实测用的是 `Villager`（`hurtCapped` 对所有 `LivingEntity` 同口径，判定与实体类型无关）。
   玩家包围盒 0.6×1.8 与 Villager 0.6×1.95 的差异只影响 `getSeenPercent` 的采样点数；贴脸距离下 `raw = 18.35` 远高于 cap 7，结论不受影响。
3. **`ExplosionQueue` 延后分支**：未单独构造「同 tick 超 8 次」的实测。结构上 `consume()` 在 `submit()` 之前、
   `drain()` 在 tick 末执行，两条路径读 `getSeenPercent` 时方块都已是空气；属结构论证。
4. **`dist/` 里的发布 jar 是旧的**（334,710 B，19:58，早于本次修复）。我按边界只产出了 `build/libs`，是否刷新 `dist/` 由调度者决定。
5. `run-fix8/` 是我这次的临时运行目录（含 `eula.txt`，以及为优雅停服临时把 `server.properties` 的 `enable-rcon` 打开、一次性口令（已失效，略）、端口 25585）。
   `run/**` 不进交付物；新增的 `.work/fix8-rcon.js` 是这次写的 RCON 停服小脚本，可复用。
