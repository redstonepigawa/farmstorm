# 证据 · farmstorm v0.2.1 · Phase 42（F-251 ~ F-256）

> 执行者：子代理（P42 唯一执行者）。工作目录 `D:\project_farm`（Windows / pwsh）。
> 读数口径：文件数与键数 / 日志行 / `/data get block|entity` / 源码 grep 计数。**禁止 git 写操作**（本文件不含任何 commit）。
> 行号一律 `[IO.File]::ReadAllLines(..., UTF8)` 或 `Select-String -Encoding utf8` 取得。

---

## 0. 全局读数（改前 / 改后）

| 读项 | 改前实测 | 改后实测 | 编号 |
|---|---|---|---|
| `src/main/java` Java 文件数 | **146** | **148** | A-78（146 → 148：MobAiSuspension + FarmstormAttachments） |
| `Farmstorm.java` `.register(modBus)` | **11** | **12** | HC-121 / F-252 验收 2 |
| 启动日志注册面 | — | `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK` | HC-121 / A-73 |
| `FarmstormConfig` 行首 `.define*` / `b.push(` / `b.pop()` | 175 / 60 / 60 | **175 / 60 / 60** | HC-122（本 Phase 不动） |
| 语言键 zh / en / zh-only | 337 / 337 / 0 | **337 / 337 / 0** | HC-123（本 Phase 不动） |
| `farmstorm.configuration.*` / `tooltip.farmstorm` | 178 / 20 | **178 / 20** | HC-123 |
| `@Mixin`（src 口径） | 0 | **0** | HC-137 |
| `.playToClient` / `PROTOCOL_VERSION` | 2 / `"2"` | **2 / `"2"`** | HC-137（本版不新增包） |
| `setNoAi` / `isNoAi` | 2 / 1 | 6 / 3（新增 2 处代码在 MobAiSuspension；其余命中为注释/既有） | A-81 |
| `removeData(`（全仓） | 0 | **1**（MobAiSuspension::clearData） | HC-127 |
| `renderTooltip`（全仓） | 0 | **0** | F-256 验收 4 |
| `new Slot(`（OutpostCoreMenu） | 3 | **3**（90 个槽位：54 + 27 + 9） | A-77 / F-256 验收 4 |

构建：`& 'D:\project_farm\.tools\gw.cmd' build` **三次全部 `BUILD_RC=0`**（`.work/p42_build1.log` / `p42_build2.log` / `p42_build_final.log`，最后一次 `BUILD SUCCESSFUL in 14s`）。

起服：**6 次**（run1 基线 / run2 首版新代码 / run3 最终代码主验收 / run4 `/stop` 后 / run5 强杀后 / run6 最终字节码冒烟）；全部用 `run-p42` + `FS_RUN_LOCK=run-p42` + `-PfsRunDir=run-p42`，**没有在 `run/` 里起过服**。

---

## 1. F-251 · `isMountMode` 可见性（private → public）

### 改了哪些文件
```
 src/main/java/com/dashpig/farmstorm/behavior/QbounceMountController.java | 2 +-
```
- `:199` `private static boolean isMountMode() {` → `:199` `public static boolean isMountMode() {`（`-U1` diff 只有这一行）
- 方法体 `:200` 一行未动：`return POSE_MODE_MOUNT.equalsIgnoreCase(FarmstormConfig.qbouncePoseMode.get());`

### 观察
| 判据 | 改前 | 改后 |
|---|---|---|
| 该类总行数 | 376 | **376** |
| `isMountMode` 声明处 | 1（`:199`，`private static`） | **1**（`:199`，`public static`） |
| `qbouncePoseMode` 读取点 | `:200` 1 处 | **`:200` 1 处**（新增调用方只调方法：`MobAiSuspension:98` / `:205`） |
| `gw.cmd build` | — | `BUILD_RC=0` |

```
$ grep isMountMode  →  QbounceMountController.java:176 / :199(decl) / :243 + MobAiSuspension.java:98 / :205
$ grep qbouncePoseMode → FarmstormConfig.java:197(声明) / :861(定义) / QbounceMountController.java:200(唯一读取)
```

### 未验证
- 「`qbounce.poseMode = mount` 时带 Q弹 的生物 `NoAI` 恒 `0b`」的行为读数见 §5（那里跑通了，作为交叉证据）。

---

## 2. F-252 · AI 原值附件载体 `Suspend` + `farmstorm:ai_suspend`（第 13 类）

### 改了哪些文件
```
?? src/main/java/com/dashpig/farmstorm/registry/FarmstormAttachments.java        (42 行，新建)
?? src/main/java/com/dashpig/farmstorm/combat/MobAiSuspension.java              (291 行，新建；本任务交付其数据类部分)
 M src/main/java/com/dashpig/farmstorm/Farmstorm.java                           (+7 −0：2 import + 2 注释 + 2 调用)
```
- `registry/FarmstormAttachments.java`
  - `:32` `DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Farmstorm.MOD_ID)`
  - `:36-39` `AI_SUSPEND = REGISTER.register("ai_suspend", () -> AttachmentType.builder(() -> MobAiSuspension.Suspend.NONE).serialize(MobAiSuspension.Suspend.CODEC, suspend -> suspend.sources() != 0).build())`
  - 无 `.sync(...)`、无 `copyOnDeath()`
- `combat/MobAiSuspension.java`
  - `:52` `public record Suspend(boolean originalNoAi, int sources)`（**2 个字段**）
  - `:55` `NONE = new Suspend(false, 0)`；`:58-62` `CODEC`（Codec.BOOL + Codec.INT，与 `TempEnchantRecord` 同风格）
  - `:66` `SRC_VIBRATION_BEAN = 1`；`:68` `SRC_Q_BOUNCE = 2`
- `Farmstorm.java`
  - `:3` import MobAiSuspension；`:9` import FarmstormAttachments
  - `:60-63`（新增）注释 + `FarmstormAttachments.REGISTER.register(modBus);`
  - `:176-178` 的 12 类断言**逐字未变**（`boolean ok = total == 97` 仍在 `:176`，diff 里 0 行改动）

### 观察
| 判据 | 实测 |
|---|---|
| `AttachmentType` 注册调用 | `FarmstormAttachments` 里 `REGISTER.register(` **1** 处 |
| id 字面量 | `"ai_suspend"` **1** 处；命名空间由 `Farmstorm.MOD_ID` = `farmstorm` |
| `Suspend` 字段 | **2**（`originalNoAi` / `sources`） |
| 该文件 `deadline` / `expire` / `timestamp` | **0 / 0 / 0** |
| `.register(modBus)` | **11 → 12** |
| `gw.cmd build` | `BUILD_RC=0` |
| 启动日志 | 见下（97 → OK，附件已注册但**不进** 97） |

```
[05Nov2026 02:31:25.230] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK
```

### ⚠️ 覆盖缺口（A-73，必须写进签字）
`tools/verify_project.py` 的 `EXPECTED` 12 组 id 里**没有** `ai_suspend`、`EXPECTED_COUNTS` 相加仍是 **97** ⇒ 工具**既不核它的存在、也不核它的计数**。
**本任务的任何判据都不写「工具通过」**；第 13 类由「42 号 §1.2.1 + F-252 验收 5 + F-266 签字」三处人工登记兜住。

---

## 3. F-253 · `MobAiSuspension`：进记原值 / 出按原值恢复 / 五条清理路径

### 改了哪些文件
```
?? src/main/java/com/dashpig/farmstorm/combat/MobAiSuspension.java   (行为部分)
 M src/main/java/com/dashpig/farmstorm/Farmstorm.java                (:83-84 注释 + MobAiSuspension.register(NeoForge.EVENT_BUS))
```
方法落点（`MobAiSuspension.java`）：`:84` `register(IEventBus)`（`addListener(EventPriority.HIGHEST, …onServerTickPost)` / `onEntityJoinLevel` / `onServerStopping`）、`:98` `suspend`、`:133` `release`、`:157` `clearData`（**全文件唯一 `removeData(`**）、`:167` `adopt`、`:189` `onServerTickPost`（每刻对账）、`:225` `onEntityJoinLevel`、`:238` `onServerStopping`（尽力结算）、`:272` `discard`（路径④，不写 noAi）、`:282` `resolve`。两个内存容器：`:71` `OBSERVED: Set<UUID>`、`:74` `VB_UNTIL: Map<UUID, Long>`。

### 观察（源码面）
| 判据 | 实测 |
|---|---|
| `removeData(`（该文件） | **1** |
| `LivingDeathEvent` / `MobEffectEvent`（该文件） | **0 / 0** |
| 「多少刻后放弃」类封顶常量（`MAX_` / `CAP_` / 上限） | **0** |
| `deadline` / `expire` / `timestamp` | **0** |
| `.sync(` / `copyOnDeath(`（注册类） | **0 / 0** |
| `.playToClient` / `PROTOCOL_VERSION` | 2 / `"2"`（未变） |

### 观察（运行期 / 逐条验收断言）
**① `NoAI` 的三种读数（动手前先各记一遍，基线 build）**
```
/data get entity @e[tag=t1,limit=1] NoAI          → Found no elements matching NoAI        （缺键 = 有 AI）
/data merge entity @e[tag=t1,limit=1] {NoAI:0b}
/data get entity @e[tag=t1,limit=1] NoAI          → Found no elements matching NoAI        （0b 不落键）
/data merge entity @e[tag=t1,limit=1] {NoAI:1b}
/data get entity @e[tag=t1,limit=1] NoAI          → Zombie has the following entity data: 1b
```

**断言 1（挂起 → 1b；恢复 → 0b 且看不到附件）**
```
/data get entity @e[tag=f1,limit=1] NoAI  → Zombie has the following entity data: 1b
/data get entity @e[tag=f1,limit=1]       → {Brain: {memories: {}}, "neoforge:attachments": {"farmstorm:ai_suspend": {sources: 1, originalNoAi: 0b}}, … NoAI: 1b …}
（80 刻后）
/data get entity @e[tag=f1,limit=1] NoAI  → Found no elements matching NoAI
/data get entity @e[tag=f1,limit=1]       → {Brain: {memories: {}}, HurtByTimestamp: 0, …}    ← 没有 neoforge:attachments
```
日志（最终字节码，run6）：
```
ai_suspend 挂起 实体=27695ea5-… 原值=false 来源=1 到期刻=12751 t=12671
震动豆 删 AI @ 4, 101, 4：本轮生物 1 删 AI 1（80 刻，来源位 1）
ai_suspend 恢复 实体=27695ea5-… 来源=1 写回原值=false t=12751        ← 恰好 80 刻
```

**断言 2（本来就 `NoAI:1b` 的怪 → 恢复后仍是 1b）**
```
ai_suspend 挂起 实体=4f0b1d66-… 原值=true 来源=1 到期刻=6131 t=6051
ai_suspend 恢复 实体=4f0b1d66-… 来源=1 写回原值=true  t=6131
/data get entity @e[tag=b2,limit=1] NoAI → Zombie has the following entity data: 1b   （释放后 3.5 s 仍然 1b）
```

**断言 3（`/stop` 时 80 刻未满 ⇒ 重启后 0b、无附件）**
```
ai_suspend 挂起 实体=e5176ed5-… 原值=false 来源=1 到期刻=9051 t=8971
[Rcon: Stopping the server]
ai_suspend 恢复 实体=e5176ed5-… 来源=1 写回原值=false t=8999            ← 停服结算（提前恢复）
ai_suspend 停服结算 处理=1
→ 重启后：/data get entity @e[tag=s1,limit=1] NoAI → Found no elements matching NoAI（0b）；NBT 无 neoforge:attachments
```

**断言 4（强杀进程 ⇒ 载入时按 sources 结算，≤1 刻，附件被删）**
前置：`save-all flush` 在挂起窗口内落盘（磁盘上是 `NoAI:1b` + 附件 sources=1）→ 再 `Stop-Process -Force` 杀掉全部 java 进程（`remaining java: 0`，端口 25643 `False`）→ 重启：
```
ai_suspend 载入结算 实体=ece27941-… 来源=1 原值=false t=9919
ai_suspend 恢复     实体=ece27941-… 来源=1 写回原值=false t=9919      ← 同一刻（≤1 刻）
→ /data get entity @e[tag=s3,limit=1] NoAI → Found no elements matching NoAI；NBT 无 neoforge:attachments
```

**断言 5（源码面）**：见上表（1 / 0 / 0 / 0）。

**断言 6（网络包仍 2、附件不带 `.sync`）**：`.playToClient` = 2、`PROTOCOL_VERSION = "2"`、`FarmstormAttachments` 里 `.sync(` = 0。

**路径④（死亡 / 被移除，只删附件不写 noAi）实测**
```
ai_suspend 实体移除 实体=cb3ba1ad-… 来源=2（不写 noAi）
```

---

## 4. F-254 · 震动豆：范围内所有非玩家生物删 AI 80 刻

### 改了哪些文件
```
 M src/main/java/com/dashpig/farmstorm/behavior/impl/VibrationBeanTrap.java | 28 ++++++++++
```
- `:41` `private static final int AI_SUSPEND_TICKS = 80;`（**常量，非 config 键**）
- `:100-115` 追加段（在玩家混乱循环之后、`BlockPos pos = be.getBlockPos();` 之前）：`Mob` 全量遍历 + `isAlive()` + 与触发端同一个 `vibration_bean.triggerRadius` 与同一个严格小于
- `:121-124` 一条 INFO：`震动豆 删 AI @ %s：本轮生物 %d 删 AI %d（%d 刻，来源位 %d）`
- 既有四步（玩家混乱 / 爆炸音 / `consume` / 授进度）**一行未改**（diff 里只看得到插入）

### 观察
| 判据 | 实测 |
|---|---|
| 基线行为（改前，0.2.0） | `震动豆 触发 @ 4, 101, 4 由 Zombie：球内玩家 0 施加成功 0（半径 4.00 格，80 刻，amplifier 0），爆炸音一次`；僵尸 `NoAI` **不变（缺键）** |
| 半径内 1 只僵尸 | 触发后 `NoAI:1b`；**80 刻后回到缺键（0b）**（t=12671 → t=12751） |
| 半径内 3 只生物 | `ai_suspend 挂起` 三行（`t=7601`）+ `震动豆 删 AI @ 4, 101, 4：本轮生物 3 删 AI 3（80 刻，来源位 1）`；三只的 `/data get` 全是 `1b` |
| 半径内没有可删 AI 的生物 | `震动豆 删 AI @ 4, 101, 4：本轮生物 0 删 AI 0`（触发者是刚被 `/kill` 的尸体，被 `isAlive()` 过滤 ⇒ 计数 0；**这不是「只有玩家」场景**，见未验证） |
| 80 刻内第二颗豆 | `震动豆 删 AI @ 6, 101, 6：本轮生物 1 删 AI 1`；释放 `t=7797` = 第二颗豆触发刻 `7717` + 80（**不是**第一颗的 `t=7661+160=7821`）⇒ 窗口刷新、原值仍是第一次的 |
| `AI_SUSPEND_TICKS` | 80（常量） |
| `b.push(` / `.define*` | **60 / 175**（本任务不碰 config） |

```
[02:31:52.319] ai_suspend 挂起 实体=27695ea5-… 原值=false 来源=1 到期刻=12751 t=12671
[02:31:52.321] 震动豆 删 AI @ 4, 101, 4：本轮生物 1 删 AI 1（80 刻，来源位 1）
[02:31:56.319] ai_suspend 恢复 实体=27695ea5-… 来源=1 写回原值=false t=12751
```

---

## 5. F-255 · Q弹：效果存续期删 AI、mount 跳过

### 改了哪些文件
```
 M src/main/java/com/dashpig/farmstorm/effect/QbounceEffect.java | 18 +++++++++
 M src/main/java/com/dashpig/farmstorm/combat/MobAiSuspension.java（mount 两层判定：:98 第一层 / :205 第二层）
```
- `QbounceEffect.java:32-37`：`@Override public void onEffectAdded(LivingEntity entity, int amplifier)` → `MobAiSuspension.suspend(mob, SRC_Q_BOUNCE, 0L)`（唯一覆写；不解析 `qbounce.poseMode`）

### 观察
| 判据 | 实测 |
|---|---|
| 覆写数 | `@Override` **1**；`public void onEffectAdded` **1** |
| `applyEffectTick` / `isDurationEffectTick` 的**方法声明覆写** | **0**（文本命中 2，两处都在**既有**类注释里，见偏差 10） |
| 该类行数 | 20 → **38**（只增不减） |
| 施加 Q弹 后 | `NoAI:1b`；NBT `"neoforge:attachments": {"farmstorm:ai_suspend": {sources: 2, originalNoAi: 0b}}` |
| 反复刷新 | 第二次 `effect give` 后附件仍是 `{sources: 2, originalNoAi: 0b}`（**不进位两次、原值不变**） |
| 路径① 自然到期 | `effect give … 3`（3 s = 60 刻）：`挂起 t=8200` → `恢复 t=8260`（**同一刻收口**，0 刻延迟）；之后 `NoAI` = 缺键、NBT 无附件 |
| 路径② `/effect clear` | `挂起 t=12816` → `clear` → `恢复 t=12854`；`NoAI` 回缺键（读数被 rcon 往返 ≈ 10 刻夹住，见偏差 11） |
| 路径③ 牛奶 | **未验证**（见未验证清单） |
| mount 第一层 | `poseMode = mount` + 施加 Q弹 ⇒ `/data get entity` 里 **没有** `NoAI`、**没有** `neoforge:attachments`（全程不删）；日志只有 `q_bounce mount 挂上 …` |
| mount 第二层（删了之后切到 mount） | 切到 `mount` 后同一刻：`ai_suspend 恢复 … 来源=2 写回原值=false t=11204`（02:29:52.897）**先**、`q_bounce mount 挂上 乘客=d6196145-…`（02:29:52.898）**后** ⇒ 先恢复再挂坐骑 |
| 切回 `render` 后下一次施加才删 | `poseMode = render` + `effect give` ⇒ 立刻 `NoAI:1b` + 附件 `{sources: 2, originalNoAi: 0b}` |

```
[02:29:52.897] ai_suspend 恢复 实体=d6196145-… 来源=2 写回原值=false t=11204
[02:29:52.898] q_bounce mount 挂上 乘客=d6196145-… 坐骑=efbef564-… 维度=minecraft:overworld 失效 tick=15043 表内=2
```

---

## 6. F-256 · 核心储物 tooltip：保证默认链路 + 验收记录

### 改了哪些文件
**零改动。** `git diff --stat` 里没有 `client/OutpostCoreScreen.java` 与 `menu/OutpostCoreMenu.java`。

### 观察（四道证据 + 计数）
| 判据 | 实测 |
|---|---|
| `OutpostCoreScreen` 行数 / 覆写 | **42** / **1**（唯一覆写 `:37-38` `renderBg`） |
| `render` / `renderTooltip` / `renderLabels` / `init` 覆写 | **0 / 0 / 0 / 0** |
| 全仓 `renderTooltip` | **0** |
| `OutpostCoreMenu` `new Slot(` | **3 处调用**（`:68` 存储格循环 / `:79` 主背包循环 / `:84` 快捷栏循环）= **90 个槽位**（54 + 27 + 9），**无 `Slot` 子类**（A-77） |
| 版式常量 | `IMAGE 176×222` / `TEXTURE 256×256` / `generic_54.png`（`:22-28`，未动） |

### 未验证
- 悬停第 1 / 28 / 54 格是否出 tooltip、空槽是否无 tooltip、附魔行是否显示 —— **渲染面，需要客户端**，本会话无第二客户端 ⇒ 按任务书口径标「未验证」，只给代码层证据（覆写数 0 / 全仓 `renderTooltip` 0 / 三处 `new Slot(` 全是原版类 / 版式常量未动）。
- 退路未启用（第 1 条没有失败读数可说，故不写「失败」）。

---

## 7. 文档-源码偏差登记（只登记，不改 docs/）

| # | 文档写法 | 实测 | 处理 |
|---|---|---|---|
| 1 | 42 号 §8 ②/`Mob.java:437-439`：`NoAI` 只在 true 时写 | 源码属实（`if (this.isNoAi()) putBoolean("NoAI", …)`），且 `data merge {NoAI:0b}` 后 `/data get entity` 仍读不到键 | 「`0b`」与「缺键」在 NBT 读数上**同态**；本 Phase 的 `NoAI` 读数一律写成「缺键 = 有 AI / `1b` = 被删」 |
| 2 | 42 号 §8 ⑩① 「`getData` 会不会把默认值写进 map」 | **会**（`AttachmentHolder.java:74-83`：`attachments.put(type, ret)`）⇒ `hasData` 不能当存在判据 | 与文档结论一致：判据只用 `sources != 0` |
| 3 | 42 号 §8 ⑧③ 「predicate=false 是否完全不写键」 | **是**（`AttachmentType.java:196-219`：`write` 返回 `null`，`serializeAttachments` 因此不 `put`） | 「正常状态存档里零字节」成立；实测释放后 NBT 里没有 `neoforge:attachments` |
| 4 | 42 号 §8 ⑥ `EntityJoinLevelEvent` **未核** | 实测**覆盖**「区块加载把实体放回世界」（强杀后重启，`loadedFromDisk=true`，`载入结算` 与 `恢复` 同刻 `t=9919`） | 退路（有界补扫 10 轮）**未启用**；见未关闭问题 |
| 5 | 42 号 §8 ⑦ `EventPriority.HIGHEST` **未核** | 实测有效：mount 切换那一 tick，`恢复`（HIGHEST）与 `mount 挂上`（NORMAL）同一刻、恢复在前 | 不改挂 `ServerTickEvent.Pre` |
| 6 | 43 号 F-255 验收 2 「让生物喝牛奶」 | 服务端命令无法让 `Mob` 喝牛奶（牛奶是玩家使用行为；`NoAI` 只管 AI） | 标未验证，代码层证据：三条路径都收敛到 `hasEffect == false`，对账每刻读它 |
| 7 | 43 号 F-254 验收 3 「半径内只有玩家 ⇒ 计数 0」 | 需要真实生存玩家（本会话无第二客户端）；拿到的是「尸体触发 ⇒ 本轮生物 0 删 AI 0」 | 该条标未验证；「计数 0」的**日志格式**已实测成立 |
| 8 | 45 号 §3.6 要求「三次起服（正常 / `/stop` / 强杀）」 | 共起服 **6** 次（多出的：首版新代码冒烟、最终字节码冒烟；另一台用于 mount 两层判定） | 多跑不影响判据，读数照贴 |
| 9 | `qbounce.poseMode` 是否需重启生效 | **不需要**：改 `run-p42/config/farmstorm-server.toml` 后数秒内 `q_bounce mount 认领扫描` 自动开始（NeoForge 重载配置） | 层①/层②用配置热切换实测 |
| 10 | F-255 验收 5 「`applyEffectTick` / `isDurationEffectTick` 命中 **0**」 | 文本命中 **2**，两处都在**既有**类注释（`F-106 定稿` 那段）里；**方法声明覆写 0** | 判据按「声明覆写」计，登记文本命中 2 |
| 11 | F-255 验收 2 「三条都在 **1 刻内**恢复」 | 自然到期：`挂起 t → 到期 t`，**同一刻**收口（0 刻延迟）；`/effect clear`：rcon 往返 ≈ 7~10 刻，只能夹住（`t=12816` 施加 → clear → `t=12854` 已恢复） | 自然到期给精确刻；clear 给区间 + 代码层证据（对账每刻 `ServerTickEvent.Post`） |

---

## 8. 未验证清单

1. **悬停 tooltip 的任何一格**（第 1 / 28 / 54 / 空槽 / 附魔物品）——渲染面，需客户端；F-256 只给代码层证据。
2. **震动豆「半径内只有玩家、没有生物」**——需真实生存玩家（无第二客户端）；已给「计数 0」的日志格式读数（尸体触发那条）。
3. **牛奶移除路径**——无法让 `Mob` 喝牛奶；代码层证据：`MilkBucketItem → removeEffectsCuredBy(MILK)` 之后 `hasEffect` 变假，对账下一秒收口。
4. **强杀方式**：`Stop-Process -Force` 杀掉**全部** java 进程（含 gradle daemon），不是精确单杀服务端 PID——`Get-CimInstance Win32_Process` 在本机被拒（`拒绝访问`），`jps` 也拿不到列表。崩溃语义成立（无 `/stop`、无存档写入、端口立即关闭）。
5. 渲染 / 听感 / 第二客户端类读数：一律未验证（本 Phase 无渲染改动，除 tooltip 外无听感面）。

---

## 9. 未关闭的问题

1. **`EntityJoinLevelEvent` 的覆盖边界**：只实测了「起服时 `forceload` 区块里的实体」这一档。**起服 10 秒后**才被加载的区块里的实体是否会被 `adopt` 覆盖，本轮没有读数（42 号 §8 ⑥ 的退路「有界补扫 10 轮」因此仍未启用）。这是 RT-54 的残余，登记不处理。
2. **F-252 / F-253 共用 `MobAiSuspension.java`**：一个文件、两个任务（数据类 vs 行为），按 43 号 §0.4「一个任务 = 一个提交」需要 `git add -p` 拆 hunk，或把 F-252 与 F-253 合并成一个提交。**由调度者决定**（本执行者不做任何 git 写操作）。
3. `vibration_bean` 的既存不一致（`findTrigger` 筛创造/旁观/己方，`trigger` 的混乱循环不筛）本轮**一行未改**（42 号 §9 第 13 条、D-174）。

---

## 10. 复现命令清单（本节所有读数）

```powershell
# 构建
& 'D:\project_farm\.tools\gw.cmd' build                      # BUILD_RC=0（.work/p42_build*.log）

# 起服（隔离目录，6 次同一入口）
$env:FS_RUN_LOCK='run-p42'; & 'D:\project_farm\.tools\gw.cmd' runServer -PfsRunDir=run-p42

# rcon 驱动（密码 p42rcon / 端口 25643；命令文件在 .work/p42_t_*.txt）
& '<python>' .work\p42_rcon.py .work\p42_t_final.txt 0.5

# 源码计数
Select-String -Path (Get-ChildItem -Recurse src/main/java -Filter *.java) -Pattern 'isMountMode' -Encoding utf8
Select-String -Path src/main/java/com/dashpig/farmstorm/combat/MobAiSuspension.java -Pattern 'removeData\(' -Encoding utf8
Select-String -Path src/main/java/com/dashpig/farmstorm/menu/OutpostCoreMenu.java -Pattern 'new Slot\(' -Encoding utf8
```
