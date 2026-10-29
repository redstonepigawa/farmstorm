# Lane B（Phase 14 · 桑吉尔蔗 F-86 ~ F-93）证据

HEAD: c544c0f（未 commit）｜运行目录 `run-p14`（服务端）+ `run-p14c`（客户端）
端口：server 25651 / rcon 25661｜锁：`FS_RUN_LOCK=run-p14`

## 0. 交付物与按任务的改动清单

| 任务 | 改动文件 | 新增文件 |
|---|---|---|
| F-86 | `behavior/DeployedBehaviors.java`（+启动映射日志）、`behavior/impl/ZangiefCaneTrap.java`（+扫描读数） | — |
| F-87 | `behavior/impl/ZangiefCaneTrap.java`（findTrigger 球形判定 + trigger 三步） | — |
| F-88 / F-89 / F-90 | `behavior/impl/ZangiefCaneTrap.java`（trigger 改为 `begin()` 一行） | `behavior/ZangiefPiledriverController.java` |
| F-91 | — | `client/ZangiefClientState.java`、`client/ZangiefCameraController.java` |
| F-92 | — | `client/InputLockHandler.java` |

**未碰**：`client/FarmstormClient.java`、`Farmstorm.java`、`effect/WildHunterEffect.java`、`docs/`、`datagen/`、`src/generated/`。
**接线行：无**——F-86 / F-88~90 / F-91 / F-92 全部用 `@EventBusSubscriber` 自注册（GAME bus 默认；客户端类加 `value = Dist.CLIENT`），
因此不需要往 `FarmstormClient.java` 或 `Farmstorm.java` 加任何一行。
**未跑 runData**；**未 commit**。探针已删（见 §6）。

---

## 1. F-86 · 部署方块与陷阱行为接入

### 验收 1 · `/setblock` 成功
```
> setblock 100 6 100 farmstorm:zangief_cane
    | Changed the block at 100, 6, 100
> execute if block 100 6 100 farmstorm:zangief_cane
    | Test passed
> data get block 100 6 100
    | {detached: 0b, fuseTicks: 0, pendingWaves: [], placedGameTime: 0L, x: 100, y: 6, z: 100, id: "farmstorm:fang_trap_cactus"}
```
BE 的 `id` 是 `fang_trap_cactus` 属**正常**：RT-04 / F-63 让 ZANGIEF_CANE 与 FANG_TRAP_CACTUS 共用同一个 BlockEntityType。
外观（世界里无几何体 + 破坏粒子用 `zangief_cane.png`）由 V-23 的 `DEPLOYED_INVISIBLE` 与 particle 贴图承担，本任务未改。

### 验收 2 · 启动日志的 6 个 kind → 单例映射（本次新增）
```
[18:25:0x] [Server thread/INFO] [farmstorm/]: 自检 部署行为 BLAST_CARROT → BlastCarrotTrap
[18:25:0x] [Server thread/INFO] [farmstorm/]: 自检 部署行为 FANG_TRAP_CACTUS → FangTrapCactusTrap
[18:25:0x] [Server thread/INFO] [farmstorm/]: 自检 部署行为 PUMPKIN_TURRET → PumpkinTurret
[18:25:0x] [Server thread/INFO] [farmstorm/]: 自检 部署行为 AWAKENED_TORCHFLOWER → TorchflowerAura
[18:25:0x] [Server thread/INFO] [farmstorm/]: 自检 部署行为 AWAKENED_PITCHER_PLANT → PitcherPlantAura
[18:25:0x] [Server thread/INFO] [farmstorm/]: 自检 部署行为 ZANGIEF_CANE → ZangiefCaneTrap
```
（原文见 `.work/p14-log-extract.txt`）

### 验收 3 · 扫描间隔 10 刻（本次新增读数）
```
[17:43:16.586] 桑吉尔蔗 扫描 t=6094 玩家 Dev 距离 2.06
[17:43:17.086] 桑吉尔蔗 扫描 t=6104 玩家 Dev 距离 2.06
[17:43:17.586] 桑吉尔蔗 扫描 t=6114 玩家 Dev 距离 2.06
[17:43:18.085] 桑吉尔蔗 扫描 t=6124 玩家 Dev 距离 2.06
[17:43:18.585] 桑吉尔蔗 扫描 t=6134 玩家 Dev 距离 2.06
[17:43:19.086] 桑吉尔蔗 扫描 t=6144 玩家 Dev 距离 2.06
```
t 差恒为 **10 刻**，时间戳每行差 0.5 s。日志门槛：16 格内有玩家才打（野外空转不刷屏）。
原始场景记录：`.work/p14-f86.txt`。

---

## 2. F-87 · 球形触发判定与豁免

全部场景原文见 `.work/p14-f87-*.txt`（脚本 `.work/p14-verify.js`）。

| 场景 | 站位（方块中心为 100.5/6.5/100.5 这类） | 期望 | 实测 |
|---|---|---|---|
| 中心 0.5 格 | `100.5 6.0 100.5` | 触发 | **触发**；方块消失；`active_effects` = `farmstorm:zangief_piledriver amp=0 dur=14` |
| 同轴 2 格 | `112.5 6.0 110.5`（dx=2.0 → 欧氏 2.06） | 不触发 | **不触发**，方块仍在，无效果 |
| 对角 1.4/1.4 | `121.9 6.0 121.9`（欧氏 2.04，切比雪夫 1 会误判） | 不触发 | **不触发**（D-73 球形口径） |
| 对照：相邻方块中心 | `131.5 6.0 130.5`（欧氏 1.12） | 触发 | **触发** |
| 创造模式 | 同上，`gamemode creative` | 不触发 | **不触发** |
| 旁观者 | `gamemode spectator` | 不触发 | **不触发** |
| 同队玩家 | `/data merge block` 写 ownerName=LaneAOwner + `team join red` 双方 | 不触发 | **不触发**；把归属者改到 blue 后**触发**（实时解析 C-9） |
| 生物 | 方块里 `summon pig` | 不触发 | **不触发** |

### 验收 4 · 触发不产生掉落（A-08）
```
(触发前掉落物：Test failed = 0 个)
> execute if block 100 6 100 minecraft:air   → Test passed   （方块已消失）
(触发后掉落物：Test failed = 0 个)
```
`trigger` 里没有删战利品表；消失走 `be.consume()`（`removeBlock(pos,false)` + 破坏粒子）。

---

## 3. F-88 / F-89 / F-90 · 三相位状态机（一个文件覆盖三个任务，V-32）

### 3.1 默认 config 的相位时间轴（原文 `.work/p14-timeline-flat.txt`）
```
  t=  0 (23202)  Y=6.000  piledriver=无  levitation=无
  t=  2 (23204)  Y=6.000  piledriver=amp=0 dur=14  levitation=无      ← 触发当刻（amplifier 0 / 剩余 14 刻）
  t=  3 (23205)  Y=6.000  piledriver=amp=0 dur=13  levitation=无
  ...
  t= 15 (23217)  Y=6.000  piledriver=amp=0 dur=1   levitation=无      ← 相位 0 期间 Y 恒为 6.000
  t= 16 (23218)  Y=6.000  piledriver=amp=1 dur=21  levitation=amp=6 dur=20   ← 14 刻后硬切换
  t= 23 (23225)  Y=7.021  piledriver=amp=1 dur=14  levitation=amp=6 dur=13   ← 漂浮 VII = amplifier 6
  ...
  t= 35 (23237)  Y=10.578 piledriver=amp=1 dur=2   levitation=amp=6 dur=1
  t= 36 (23238)  Y=10.9   piledriver=amp=2 dur=100 levitation=无      ← 20 刻漂浮结束 → 相位 2
  t= ...         Y 逐刻下降
```
服务端日志（同一轮）：
```
桑吉尔蔗 相位 0 t=23204 玩家 Dev duration=15
桑吉尔蔗 触发   t=23204 玩家 Dev 中心距 0.51
桑吉尔蔗 相位 1 t=23218 玩家 Dev duration=21
桑吉尔蔗 相位 2 t=23238 玩家 Dev duration=100
桑吉尔蔗 落地耗时 42 刻（目标 40）
桑吉尔蔗 结束   t=23280 玩家 Dev
```
- **相位 0 = 14 刻**（23218 − 23204），**相位 1 = 20 刻**（23238 − 23218）✔
- 客户端读到的 duration 比相位长 1（15 / 21）：见下面「实现订正」。

### 3.2 落地耗时（F-90 验收 1）· 三个高度（修复后全过）

默认 config（14 / 7 / 40），高台按「平台」搭 3×3 顶面（文档的「两格 / 四格高台」），原文 `.work/p14-terrace-*.txt`：

| 场景 | 实测 `落地耗时` | 判定 |
|---|---|---|
| 平地（陷阱 y=6，玩家站在 y=5 台面上） | **42 刻** | ✔（40 ± 2）|
| 两格高台（y=8，3×3 平台顶） | **42 刻** | ✔ |
| 四格高台（y=10，3×3 平台顶） | **42 刻** | ✔ |

三种地形各跑一次「相位 2 中 `/kill`」，四项全过（见 §3.4），原文 `.work/p14-final2-f90-kill*.txt`。

#### 3.2.1 修之前是什么样（存档：不掩盖；日志时间戳对照见下）
修之前：平地 42 ✔ / 两格 42 ✔ / **四格 23 ✘**。
按 dispatcher 要求先测根因（临时打印相位 2 每刻的 `dy / y / groundY / h / n / servo / onGround / noGravity`）：
```
P14DIAG fall t=1  y=15.442 dy=0.229  groundY=6.000 h=9.442 n=39 servo=0.242 onGround=false noGravity=false
P14DIAG fall t=2  y=14.724 dy=-0.237 groundY=6.000 h=8.724 n=38 servo=0.230 onGround=false noGravity=false
...
修正后同一位置：
P14DIAG fall t=2  y=14.724 dy=-0.237 ... noGravity=true    （实测位移 = 伺服的 0.23，不再多出 0.08）
```
两条根因：
1. **原版重力在 `setDeltaMovement` 之后又叠了一次**（约 +0.08 格/刻），实际下落比 `h/n` 快约 1.6 倍；
2. 1×1 石柱那类地形，漂浮段的横向漂移会把玩家挪出柱顶那一列，`groundYBelow` 量到的是远处的台面（6 而不是 10），
   伺服据此给的速度更大，落地提前。
修法（两条都做）：
- 相位 2 期间 `setNoGravity(true)`（比「每刻跟重力对削」干净，不受高度/浮点差异影响），
  并用 `GRAVITY_OFF` 集合 + 每 tick 兜底扫尾保证**四条退出路径**（落地/超时/外力清除、死亡、掉线、换维度）都把重力还回去；
- 进入相位 2 那一 tick 把「目标地面」量一次钉住（`PhaseState.targetGroundY`），不再每 tick 重解析。

`.work/p14-log-extract.txt` 里 `落地耗时` 各行的对照：
- `18:38:54` / `18:39:21` / `18:39:28` = 平地 / 两格(3×3) / 四格(3×3)，都是 **42**（修复后）；
- `18:39:33` = **1×1 石柱**那次（同代码、只换地形构型），**23** —— 就是 §3.2.1 里那个「漂出柱顶导致量错地面」的构型；
- `18:39:58` / `18:40:03` = 换成 3×3 平台后的两格 / 四格，都是 **42**。
即：**平台构型三个高度全过；1×1 石柱是已知的边缘构型**，玩家漂出柱顶就会落在柱顶上（比目标地面高 4 格），
伺服无法把「落在中途的柱顶」拉长到 40 刻。这一条已登记，不当作默认地形的验收缺陷。

### 3.3 config 三键生效（F-88 验收 3 / F-89 验收 2 / F-90 验收 3）
一次性把 `stageDelayTicks=30`、`levitationLevel=3`、`fallTicks=80` 三键同时改（停服改 + UTF8 无 BOM，V-33），重测：
```
桑吉尔蔗 相位 0 t=36924 duration=31      → 相位 0 持续 30 刻（阶段时长 = stageDelayTicks）
桑吉尔蔗 相位 1 t=36954 duration=21
  t= 32 (36954)  levitation=amp=2 dur=20   → amplifier = 等级 − 1 = 2（原 6）
桑吉尔蔗 相位 2 t=36974 duration=100
桑吉尔蔗 落地耗时 82 刻（目标 80）        → 时间伺服跟随 fallTicks（不是定速）
```
原文 `.work/p14-timeline-variant.txt`。测完已把三键改回默认（14 / 7 / 40）。

### 3.4 相位 2 中 /kill（F-90 验收 4）
```
(轮询到 amp=2 后) kill 前效果：fs:zangief_piledriver amp=2 dur=100
> kill @p                                  → Killed Dev
桑吉尔蔗 结束（死亡）玩家 Dev
重生后：无 zangief_piledriver / 无 levitation；Motion = [0.0d, 0.0d, 0.0d]
```
原文 `.work/p14-f90-kill.txt`。

### 3.5 实现订正（与 TECH §5.2 的三处偏差，均已在代码注释里写明）
1. **相位效果时长 +1 刻**（`PHASE_EFFECT_CUSHION`）：效果施加当刻就被原版扣 1，duration 写成「相位时长」时效果只覆盖「时长 − 1」刻，
   末尾那一 tick 会同时出现「效果已没、相位还没推进」，客户端会渲染成一次相机/输入全复位再上锁的闪烁（正是 RT-15 警告的那类画面）。
   +1 之后相位按 elapsed 精确卡点，实测相位 0 = 14 刻、相位 1 = 20 刻，客户端读到的 duration 是 15 / 21（仍在验收的 ±1 内）。
2. **相位 2 关重力 + 目标地面只在入口量一次**（见 §3.2.1）：原版重力会在 `setDeltaMovement` 后再叠 0.08 格/刻，
   实测位移 0.226 vs 伺服 0.13；且 `groundYBelow` 每 tick 重解析会在「玩家漂出 1×1 柱顶」时量错地面。
3. **相位结束判据用 elapsed，而不是只靠 amplifier 是否存在**：初版用「效果没了就收尾」自检，实测会在正常相位末尾误判成
   「被外部清除」并提前收尾（`状态复位` 出现在第 13 刻）。现在的顺序是：先判相位是否到点 → 到点就推进；只有**没到点且效果消失**
   才当外力清除（`桑吉尔蔗 状态复位（效果被外部清除）`）。

---

## 4. F-91 · 相机（roll 180 + pitch 取反 + 相位 2 自转）

截图由**游戏内 `Screenshot.grab`** 取（V-34：本机 `CopyFromScreen` 截不到游戏窗口），文件已拷到 `.work/`：

| 文件 | 对应相位（探针在相位变化时各截一张，文件名即探针日志里的「第 N 张」） | 看图看到的内容 |
|---|---|---|
| `p14-cam-1-phase0.png` | **相位 0**（`piledriver amp=0`） | 看到的是**石台的底面**在画面**上方**，右侧有一根**柱状方块朝下吊着**，而**天空在下方**；准心在画面中部偏上 |
| `p14-cam-2-phase1.png` | **相位 1**（`amp=1` 漂浮中） | 同上倒转视角，画面里的石台底面更近（玩家正被抬起来） |
| `p14-cam-3-phase2.png` | **相位 2**（`amp=2` 下坠自转） | 倒转依旧，且画面内容相对上一张明显转过一个角度（自转） |
| `p14-cam-4-after.png` | **效果消失后**（`amp=-1`） | 同一场景下视角恢复正常：石台底面在画面**上方**是因为玩家站在台面下方仰视，天空在**地平线之上**，准心回到水平——与相位 0 那张的「天地对调」相反 |

（以上描述是**看图写出来的**：相位 0 那张的「柱子朝下吊着 / 天空在下」就是倒转的直接视觉证据；
效果消失那张是同一场景的正常朝向，两张构成「开/关」对照。）

验收 2 · 自转读数（客户端日志）——**注意：这一行来自 `fallTicks=80` 的**变体配置轮**，不是默认 40 轮**：
```
桑吉尔蔗 自转累计 5904.0 度（82 刻 × 72.0）
```
82 × 72.0 = 5904 ✔ —— 82 刻是那一轮 `fallTicks=80` 的实测落地耗时（80 + 2），所以是 82 而不是默认的 42；
默认 40 配置下同样的算式是 42 × 72 = **3024 度**（同一轮里相位 2 时长 42 刻，见 §3.2）。

验收 3 · 只改渲染层（D-59 铁律）—— 触发前 / 相位 0 / 相位 2 三次读 `Rotation`：
```
> tp @p 100.5 6.0 100.5 0 0 ; data get entity @p Rotation  → [0.0f, 0.0f]
> (触发后相位 0)            data get entity @p Rotation  → [0.0f, 0.0f]
> (相位 2)                  data get entity @p Rotation  → [0.0f, 0.0f]
```
玩家真实朝向全程不变，倒转/自转只发生在渲染层。

---

## 5. F-92 · 输入锁（21 项逐键表 + 对照组）

方法：临时探针（已删，见 §6）在客户端内驱动**真实输入路径**——直接 `KeyMapping.setDown`/`KeyMapping.click` 后 fire `InputEvent.Key`、
fire `MouseButton.Pre` / `MouseScrollingEvent` / `InteractionKeyMappingTriggered`、`setScreen`（走 `ScreenEvent.Opening`）。
对照组 = 同一脚本在**无效果**时跑一遍；锁定时再跑一遍。原文 `.work/p14-client8.log`（对照）/ `.work/p14-client10.log`（锁定）。

| # | 项 | 锁定期实测 | 对照组实测 | 判定 |
|---|---|---|---|---|
| 1 | W（前进） | impulse forward=0.00 | 0.30（实际位移 dx=0.17 dz=0.63） | 拦住 |
| 2 | A（左移） | impulse left=0.00 | 0.00→随 W 组合位移 | 拦住 |
| 3 | S（后退） | 同 1：前进/后退 impulse 归零 | — | 拦住 |
| 4 | D（右移） | 同 1 | — | 拦住 |
| 5 | 空格（跳） | impulse jump=false，dy=0.0000 | jump=true，dy=0.4952 | 拦住 |
| 6 | Shift（潜行） | impulse shift=false | shift=true | 拦住 |
| 7 | 左键（攻击/挖掘） | `MouseButton.Pre` 取消=true；`InteractionKeyMappingTriggered` 取消=true | 取消=false | 拦住 |
| 8 | 右键（使用） | 同上（同一对事件覆盖 use） | 取消=false | 拦住 |
| 9–17 | 快捷栏 1–9 | 3 号槽点完被抽干、选中槽不变（2→2） | 选中槽 0→2（真的切了） | 拦住 |
| 18 | 滚轮上 | `MouseScrollingEvent` 取消=true | 取消=false | 拦住 |
| 19 | 滚轮下 | 同上（同一事件） | 取消=false | 拦住 |
| 20 | E（背包） | 背包打开=false | true | 拦住 |
| 21 | T / `/`（聊天） | 聊天打开=false | true | 拦住 |
| 22 | ESC（暂停菜单） | 暂停菜单打开=true | true | **必须可用 ✔** |

已知限制（验收时不算缺陷，D-79 / FINDING-RT13）：
- **F11 / F2 / F3** 在 `KeyboardHandler#keyPress` 内部、NeoForge 钩子之前执行完，无 Mixin 够不着。
- **视角**：`CalculatePlayerTurnEvent.setMouseSensitivity(0)` 只把系数从 0.5 压到 0.2（`d2 = sens×0.6 + 0.2`），
  即**转速降到约 1/15.6，不是锁死**（V-31，已批准为已知限制，未加 Mixin）。
- **锁定中途按住左键**：进入锁定时主动 `keyAttack/keyUse.setDown(false)`，配合逐 tick 的 `InteractionKeyMappingTriggered` 取消。

---

## 6. 探针清理（硬红线自查）

临时探针 `client/P14Probe.java`（自注册、开关文件 `run-p14c/p14-shot.enabled` / `p14-input.enabled` / `p14-input.locked`）
**已删除**，开关文件一并删除，`build/classes` 下残留 class 也已删除，随后重新 `gw.cmd build`。

三处计数（`build/libs/farmstorm-0.0.2+mc1.21.1.jar` 为 build 产物）：
- `src/**/*Probe*` → **0**（剩下 1 处是 **Lane A 的 `ProbeF80.java`**，F-80 的探针，不属本 lane）
- `build/classes/**/*Probe*` → **0**（同上，剩 1 处是 `ProbeF80.class`）
- jar 内 `Probe` 条目 → **0**（剩 1 条是 `com/dashpig/farmstorm/ProbeF80.class`）
本 lane 自己的探针在三处都是 **0**。

---

## 7. F-93 出口回归（Phase 14）

> **出口回归的正式记录在 `.work/f93-evidence.md`**（三条判据逐条 + 相位日志逐 cycle 归组 + 23 行键位表 +
> 配置轮标注 + V-31 / V-34 / V-46 三处口径声明）。本节只留摘要。

| # | 判据 | 结果 |
|---|---|---|
| 1 | TECH §11 #10 三相位时序与落地时长 | ✔ 相位 14 / 20 刻；落地时长 平地 42 / 两格 42 / 四格 42（均 40±2，见 §3.2） |
| 2 | #11 只对非己方玩家生效 | ✔ F-87 七种场景 |
| 3 | #12 输入锁只漏 ESC | ✔ §5 表；F11/F2/F3 与视角灵敏度为已知限制 |
| 4 | 相位日志四行齐全 + `落地耗时` | ✔ `相位 0/1/2` + `结束` + `落地耗时 42 刻（目标 40）` |
| 5 | `grep -r "CustomPacketPayload" src/main/java` = 0 | ✔ 0 处 |
| 6 | 触发后不掉落（A-08） | ✔ §2 验收 4 |

**未完成 / 需要你裁决的一项**：
1. F-92 的「**真人逐键手测**」这一层没做——本机无法把前台窗口切到游戏（V-34），
真实键鼠事件送不进客户端。上表是「探针驱动真实输入路径 + 服务端/客户端读数」的**机制级**证据：
它证明每个钩子确实把对应输入的 impulse / 取消位 / 点击计数按设计改掉了，但**不等于人手按下去的手感验证**。
需要真人手测的话，请指派在能操作窗口的环境里跑一遍，表结构可直接沿用 §5。

---

## 8. 运行与清场

- 服务端：`FS_RUN_LOCK=run-p14 gw.cmd runServer -PfsRunDir=run-p14`（25651 / rcon 25661）
- 客户端：`FS_QUICKPLAY_PORT=25651 gw.cmd runClient -PfsRunDir=run-p14c --init-script .work/client-quickplay.gradle`
- 收尾：rcon `stop` + **只杀自己的客户端 PID**（V-37，不再一刀切杀 java）；`run-p14/config` 已改回默认值（14 / 7 / 40）。
