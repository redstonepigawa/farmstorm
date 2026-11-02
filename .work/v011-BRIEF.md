# v0.1.1 施工事实卡（唯一口径来源）

> 本文件是 v0.1.1 六份文档（28~33）的**共同事实底座**。每条事实后括注证据（`文件:行号`）。
> 文档作者**只能引用本卡与源码**，不得自创数值；本卡与源码冲突时以源码为准，并回报我。
> 本卡写于四轮访谈之后，**§2 的 25 条是作者当场拍板的**，下游不得改写、只能引用。

---

## 0. 版本与基线

| 项 | 值 | 证据 |
|---|---|---|
| Minecraft | 1.21.1 | `gradle.properties:2` |
| NeoForge | 21.1.213 | `gradle.properties:3` |
| Java | 21（本仓库自带 `.tools/jdk21` = 21.0.2） | `gradle.properties:16-17`、`.tools/jdk21` 实测 |
| 构建入口 | `.tools\gw.cmd`（**唯一入口**；本身**没有子命令分发**，`:41` 把参数原样转发 gradle） | `gw.cmd:41` |
| 基线版本 | **0.1.0 已实现并发版**（`gradle.properties:11` `mod_version=0.1.0`；`dist/` 与 `build/libs` 最新产物均为 0.1.0） | 实测 |
| 注册对象 | **93** = items 32 + blocks 26 + block_entity_type 7 + entity_type 13 + mob_effect 4 + sound_event 3 + recipe_type 1 + recipe_serializer 1 + creative_mode_tab 1 + damage_type 3 + data_component_type 1 + menu_type 1 | 逐条点名见附录 A；`verify_project.py:197-210` 的 `EXPECTED_COUNTS` 与 `:157-194` 的 ID 清单一致 |
| config 键 | **166**（59 组）= `defineInRange` 157 + `define` 6 + `defineInList` 3 | `FarmstormConfig.java` 实测逐组点名；`run/config/farmstorm-server.toml` 166 键行 / 59 组 |
| 语言键 | **315 × 2**（中英键集完全相同） | `zh_cn.json` / `en_us.json` 实测；`ModLangProvider.java:14-15` 口径 |
| 启动自检 | 7 条断言（`chebyshev==3` / `euclidean==5` / `weightSum==1000` / `ALL.size()==22 && throwable==12 && deployable==7 && edible==3 && demolition==2` / `poolSize==19` 且不含 3 个 ID / 注册面 93 逐类 / 客户端渲染器数） | `Farmstorm.java:82,83,84,103-104,110,138-141`、`client/FarmstormClient.java:127` |
| 验证工具实跑 | `FAIL 0 项，WARN 0 项`，exit 0 | `检查环境.cmd` → `verify_project.py` 实测 |
| Java 文件数 | **125**（`src/main/java` 递归实测） | 实测 `Get-ChildItem -Recurse -Filter *.java`（0.1.0 基线是 117，本版动工前已增至 125） |

### 0.1 两条红线（**本版口径变更**）

| 红线 | v0.0.1~v0.1.0 口径 | **v0.1.1 口径** | 依据 |
|---|---|---|---|
| 自定义网络包 | **0 个** | **废除该限制**（本版恰好新增 1 个，但不设上限） | 裁定 #22 |
| Mixin | 无 | **无（保留）** | 实测 `@Mixin`/`mixins.json` 全仓 0 命中 |

> ⚠️ 所有既有文档（`docs/zh/00`~`27`、`docs/en/*`、README 的红线段）写的是「0 自定义网络包」。
> 那六份**冻结不改**；新口径只能写进 28~33 与 README 增量。

### 0.2 编号接续（从 0.1.0 终点起算）

| 系列 | 上一版到 | **本版从** |
|---|---|---|
| 决策 D-xx | D-123 | **D-124** |
| 任务 F-xx | F-175 | **F-176** |
| Phase | 25 | **26** |
| 里程碑 M-xx | M21 | **M22** |
| 硬约束 HC-xx | HC-76 | **HC-77** |
| 禁止行为 N-xx | N-74 | **N-75** |
| 风险 RT-xx | RT-30 | **RT-31** |
| 裁决 A-xx | A-29 | **A-30** |
| 实现判定 V-xx | V-190（`.work/PLAN.md`） | **V-191** |
| 文档号 | 27 | **28** |

---

## 1. 源码已验证事实（每条带证据）

### 1.1 狂野苹果材质（R1）

- 现贴图 `textures/item/wild_apple.png`：**571 B / 16×16 / 88 个去重 RGBA / 107 个全透明 / 未半透明**（实测逐像素）。
- 其中 **33 px 是 `#3FA34D`**，位置 x∈[9,15] y∈[9,15] 的 7×7 十字 —— **现贴图本来就带着绿色「+」**（实测）。
- 模型 `generated/resources/assets/farmstorm/models/item/wild_apple.json`：`parent=minecraft:item/generated`、`layer0=farmstorm:item/wild_apple`、**无 overrides**（`:1-6`）。`src/main/resources` 下没有同名 json（实测）。
- 生成器 `tools/gen_textures.py` **会生成并覆盖** `wild_apple.png`：
  - `:154` `WILD_APPLE_SOURCE` 默认指向桌面 `2797B56FEF56AF13125F333A6989632A.png`（1294×1216，实测存在 1,113,442 B）
  - `:155` 可用环境变量 `FARMSTORM_WILD_APPLE_SOURCE` 覆盖
  - `:270` `ICONS` 表含 `wild_apple`；`:274-275` `NO_PLUS_ICONS = {"converter"}`（**26 条里只有 converter 免叠** ⇒ 25 张带「+」）
  - `:407` `add_plus = item_id not in NO_PLUS_ICONS`；`:974-975` `if spec.add_plus: return compose_icon(base, marker)`
  - `:546-560` `build_marker()` 画 7×7 十字（33 px）；`:612-623` 贴到右下角 `left=9 / top=9`
  - `:884-895` `draw_wild_apple()` 走 `wild_apple_grid()`（`:849-881`）**16 网格众数取色**；`:59` `TEXTURE_SIZE = 16` ⇒ **输出恒为 16×16**
- 源图 `C:\Users\DASHpig\Downloads\wild_apple_64x64.png`（实测）：**存在 / 6,946 B / 64×64 / 1814 去重 RGBA / 1471 全透明 / 2625 不透明 / 0 半透明 / alpha bbox x∈[2,60] y∈[0,63]**。
- 该源图**不是**任何 16×16 图的 4× 最近邻放大（256 个 4×4 块里 204 块不均匀）⇒ 是真细节图。
- 「绿色 +」规则原文：`plan0.0.1.txt:8`；作废声明：`plan0.1.1.txt:10`。代码侧只有 `tools/gen_textures.py` 一处承载，Java 侧 **0 处**（无 `ItemDecorator`/`overrides`/贴图叠加）。
- ⚠️ **文档与源码不符**：`22-DECISIONS-0.1.0.md:581`、`26-PROMPT-MAIN-0.1.0.md:199`、`27-PROMPT-TASKS-0.1.0.md:405` 写的是「11 色 / 透明 126」，实测 **88 色 / 透明 107**。本版**不改那三份**，但新文档不得引用旧数值。

### 1.2 据点核心方块（R2/R3/R4）

- 注册链 `registry/FarmstormBlocks.java:82-89`：`MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / **`SoundType.STONE`** / `pushReaction(BLOCK)`；**没有 `.lightLevel(...)`**（实测 grep，取值 0）。
- 方块实体 `blockentity/OutpostCoreBlockEntity.java`：字段 `ownerUuid`(:76) / `ownerName`(:78) / `placedGameTime`(:79) / `cleared`(:85，**不写 NBT**)；`saveAdditional`(:401-406) / `loadAdditional`(:408-414) 有；**`getUpdateTag` 与 `getUpdatePacket` 都没有**；`setChanged` 仅 `:102` 一处。
- 破坏回调只有 `OutpostCoreBlock.java:101-107` 的 `onRemove → core.release()`；无 `playerWillDestroy` / `affectNeighborsAfterRemoval` / 自定义 shape。
- 边界粒子：`OutpostCoreBlockEntity.serverTick`(:179-192) → `emitBoundaryRing`(:201-233) → 每圈 48 个 `ClientboundLevelParticlesPacket`(:252-267)；间隔 config `outpost_core.boundaryParticleIntervalTicks`=40；**只发给 `isAlly` 的在线玩家**(:210-211)。
- 索引 `outpost/OutpostCoreIndex.java`：`SavedData`，落盘 `world/data/farmstorm_outpost_core.dat`(:41)，结构 `owners → {uuid → {name, entries → [{dimension, pos}]}}`(:209-238)；`owned(Player)`(:85-90) **只吃在线 `Player`**；`isOwnerOrAlly`(:99-113) **全仓 0 调用点**。
- 放置上限：`OutpostCoreBlock.getStateForPlacement`(:77-85) 用 `OutpostCoreIndex.countOwned(player) >= outpost_core.maxPerPlayer`(默认 2) 拒绝。

### 1.3 耕地账本与两个 bug 现场（R2/R3）

- 账本 `farmland/FarmlandLedger.java`：`SavedData`，落盘 `world/data/farmstorm_farmland.dat`(:44)；`byOwner: Map<UUID, LinkedHashSet<BlockPos>>`(:51)；`baseOverride`(:57-58，**不落盘**)；`save`/`load`(:228-244 / :246-264)；`remove(BlockPos)`(:141-158) **全仓只有 1 个调用点**（`OutpostCoreBlockEntity.java:332`）。
- 额度算式 `:82-108`：`capacity = base() + bonus × 核数`，`base()`(:72-74) 读 `farmlandLimitPerPlayer` 或 `baseOverride`，bonus 读 `outpost_core.farmlandBonus`(:107)；成员集合 `members(...)`(:185-196) **只取在线玩家**。
- **bug 1 现场**（多核重叠误铲）：`OutpostCoreBlockEntity.setRemoved`(:391-399) → `settleOnRemoval`(:315-335) → `doomedFarmland`(:353-366) → `collectFarmland`(:369-379) → `isFarmlandLoaded`(:382-385)。世界写入只有 `:331` 一行 `setBlock(DIRT)`；判定 14 条中的关键三条：`ownedBy(server, ownerUuid)`(:355) + 「队友一路」遍历 `server.players()`(:356-364) + `inRange(corePos, pos)` 3×3 区块切比雪夫(:371-373)。**没有任何「这块地是否还被别的核覆盖」的检查**。
- **bug 2 现场**（所有者下线即失效）：`farmland/FarmlandGuard.protectsFromTrample`(:120-134) 的 `:125` 遍历 `getPlayerList().getPlayers()`（**只取在线**）；`inOwnRange`(:92-107) 同样从在线玩家出发；`OutpostCoreIndex.owned(Player)`(:85-90) 也吃在线实体。类注释 `:114-115` 自己写明「账本归属者全员离线时不受保护」。
- 调用点：`event/FarmstormGameEvents.java:229-233`（`FarmlandTrampleEvent` 取消）；锄地闸 `:194-217`，`:206` `ledger.add(...)`。

### 1.4 服务端→客户端数据面（R5）

- **自定义网络包 0 个**（实测 17 个关键字全 0 命中）—— ⚠️ **这是 0.1.1 动工前的基线状态，不是本版最终口径**；本版将新增 1 个（§3.2 #21 / 裁定 #22）。**`@Mixin` 0 个**（实测 + `build.gradle` 无 mixin 插件）。
- **BE 同步先例 2 个**：`blockentity/DeployedCropBlockEntity.java:455-460` `getUpdateTag` + `:462-466` `getUpdatePacket`（`ClientboundBlockEntityDataPacket.create(this)`）+ `:468+` `writeSynced` 6 字段；`blockentity/ConverterBlockEntity.java:418-428` 只有 `getUpdateTag`。
- 原版包用法共 5 类 8 处：`ClientboundLevelParticlesPacket`（边界粒子）、`ClientboundBlockEntityDataPacket`、`ClientboundSoundEntityPacket`(`combat/WildHunterAura.java:211`)、`ClientboundSetEntityMotionPacket`(`behavior/ZangiefPiledriverController.java:362`)、`ClientboundUpdateMobEffectPacket`/`ClientboundRemoveMobEffectPacket`(`behavior/QbounceObserverSync.java:209/217/227`)。
- 结论：**「服务端→客户端推一份自定义列表」在本仓没有任何先例**，本版是第一次。

### 1.5 客户端 HUD 与输入先例（R5）

- `client/WildHunterHud.java`（77 行）：`:19` `@EventBusSubscriber(Dist.CLIENT)`、`:40-41` `RenderGuiEvent.Post`、`:46` 前置条件 `player==null || options.hideGui || screen!=null` 则不画、绘制全走 `GuiGraphics#fill`(`:60-67`)、**无遮挡求交、无开关、无 config**。
- `client` 包 21 个类（逐个点名见扫描报告 A §4.2）；**`KeyMapping` 注册 0 个**（`RegisterKeyMappingsEvent` 0 命中）；**CLIENT 类型 config 0 个**（唯一 `registerConfig` 在 `Farmstorm.java:56`，`ModConfig.Type.SERVER`）。
- 客户端入口 `client/FarmstormClient.java`：`:46-49` `RegisterMenuScreensEvent`、`:57-60` `RegisterRecipeBookCategoriesEvent`、`:103-119` 实体渲染器、`:67-72` `InteractionKeyMappingTriggered`、`:78-81` `ClientTickEvent.Post`、`:88-91` `RenderLevelStageEvent`、`:93-96` `RenderGuiEvent.Pre`。
- 服务器权威约定仍在：`TurretBullet.java:23-25` 类注释「追踪只在服务端做（HC-03）」。

### 1.6 压缩干草块音效（R6）

- `registry/FarmstormBlocks.java:73-79`：`.sound(` **`SoundType.GRASS`** `)`(:79)；同链 `mapColor(COLOR_BROWN)`(:76) / `strength(30.0F, 6.0F)`(:77) / `requiresCorrectToolForDrops()`(:78)。
- 全仓 `SoundType` 只出现 3 个值：`GRASS`(:79) / `STONE`(:88，**唯一先例是 `outpost_core`**) / `CROP`(:100,:110)。

### 1.7 man瓜 = `farmstorm:pumpkin_turret`（R7/R8）

- 身份：`zh_cn.json:57/58/287/288` 的中文字面量就是「man瓜」；含「瓜」的对象共 4 个（`pumpkin_turret` man瓜 / `melon_grenade` 瓜！/ `melon_slice_bomb` 瓜片子 / `melon_grenade_stem` 瓜！）。`plan0.0.1.txt:91-97` 原始需求也写作「物品名称：man瓜 / 对应原版作物：南瓜」。
- 开火间隔：config `pumpkin_turret.fireIntervalTicks` **默认 60**（1–72000）`FarmstormConfig.java:431-433`；读取 `PumpkinTurret.java:80-82`；生效 `TurretBehavior.java:26-32` + `DeployedCropBlockEntity.java:146-149`（冷却期连索敌都不做）；扫描间隔 `scanInterval()` 默认 10(`behavior/TurretBehavior.java:13-16`)。
- 伤害：`farmstorm:void_bolt`(`FarmstormDamageTypes.java:20`)；数值 config `pumpkin_turret.projectileDamage` **默认 1**（0–100）`FarmstormConfig.java:425-427`；施加点 `TurretBullet.java:106`；击退 `knockbackLevel` 默认 1(`:428-430` → `TurretBullet.java:110-117`)。
- 炮弹 `entity/TurretBullet.java`（`extends ThrowableItemProjectile`，注册名 `turret_bullet` `FarmstormEntityTypes.java:60-66`，`clientTrackingRange(4)` / **`updateInterval(10)`**）：
  - `:28` `SPEED = 0.6` **硬编码，无 config**
  - `:43-51` 两个构造器都 `setNoGravity(true)`
  - `:73-87` `tick()`：服务端且 `target != null && target.isAlive()` 时调 `steerTowards(target)`
  - `:90-96` `steerTowards`：`setDeltaMovement(归一化(目标碰撞箱中心 − 自身) × SPEED)`，**每刻直接重设、无转向角上限、无最大追击时长**
  - `:98-119` `onHitEntity`：`hurt(void_bolt, projectileDamage)` + 玩家发进度 `turret_hit` + 击退 + `discard()`
  - `:121-128` `onHitBlock`：直接 `discard()`，不炸方块
  - `:138-143` 读档强制 `target = null`（存档里虽写 `TAG_TARGET`）
- 索敌 `PumpkinTurret.java`：`targetRadius` 默认 15 切比雪夫(:422-424 / :46)、查询盒 `AABB.ofSize(2r+1)`(:120-124)、玩家优先且排除观察者/创造/己方(:85-100)、`Monster` 取近(:106-118)、拦截投掷物(:28-29,:144-150)。
- **没有弹药系统**（`弹药|ammo` 全仓 0 命中）。
- **既有追踪仅此一处**：全仓 `homing` 0 命中；无实体继承 `ShulkerBullet`/`AbstractHurtingProjectile`/`Projectile`；`client/render/TurretBulletRenderer.java:7,28,32` 只是**借 `ShulkerBulletModel` 模型**做渲染。

### 1.8 朋友的韭（R9/R10）

- 投掷物 `entity/thrown/FriendChivesProjectile.java`（81 行）`extends ThrownCropEntity`(:29) `extends ThrowableItemProjectile`。
- 基类 `ThrownCropEntity.java`：`:95-104` `settle()` → `onImpact()` → **`!keepAliveAfterImpact()` 就 `discard()`**；`:116-118` `protected boolean keepAliveAfterImpact()` 默认 `false`（**爆竹 `BambooFirecrackerProjectile` 是唯一已有重写者**）；`:65-72` `tick()` 只在 `!settled && tickCount > throwable.lifetimeTicks` 时 `discard()` ⇒ **一旦 settled，寿命上限不再生效**。
- **现有范围效果在落地那一瞬**（`FriendChivesProjectile.java:56-80`）：
  - `:57` `radius = friend_chives.hitRadius` 默认 **6.0**（0–16，`FarmstormConfig.java:851-853`）
  - `:58` `ticks = friend_chives.effectTicks` 默认 **200**（20–1200，`:854-856`）
  - `:61-62` AABB 粗筛 + `pos.distanceTo(candidate.position()) <= radius` 欧氏复核（含高低差）
  - `:63` `new MobEffectInstance(Q_BOUNCE, ticks, 0, false, true, true)` —— **amplifier 字面量 0，无 config**
  - `:71-73` `if (applied < 1) return;` ⇒ **空投不播、命中≥1 才播、只播一次**
  - `:74-76` `volume = friend_chives.soundRange / 16.0` = 32.0/16 = **2.0**（`:857-859` 默认 32.0，0–64）；`SOUND_PITCH = 1.0F`；`playSound(null, x,y,z, FriendChives, PLAYERS, volume, pitch)`
  - `:51-53` 注释明写「效果端不筛敌我，投掷者本人站进球内同样中招」
  - ⚠️ **全类没有 `tick()` 重写、没有任何「持续 N 刻」逻辑**
- Q弹 `effect/QbounceEffect.java`：`farmstorm:q_bounce`(`registry/FarmstormMobEffects.java:31-32`)，`super(HARMFUL, 0xFF6EC7)`，**不重写 tick、不读 config**。
- 音效 `friend_chives`：`registry/FarmstormSounds.java:30-32` `createVariableRangeEvent`；`sounds/friend_chives.ogg` **83,932 B / 48 kHz mono / 10.000 s**（实测逐字节解 OggS granule）；`sounds.json:16-22` `category=player`、有 subtitle、`stream=false`、**未设 `attenuation_distance`（走原版默认 16）**。同文件 `:6` 的 `wild_hunter_loop` 已用 `"attenuation_distance": 32`（**32 格有先例**）。
- 原素材 `D:\dsh\default\pengyoudejiu.mp3`：存在 7,450,588 B / **158.26 s** / 48 kHz stereo 320 kb/s / 带 1120×1120 封面。转换流水线 `tools/convert_media.ps1:61,74,190-196`（`-t 10` + libvorbis + `-fflags +bitexact`）⇒ 现 ogg 就是它的前 10 秒。

### 1.9 指令与负载体（R2/R3 连带）

- `command/FarmstormCommands.java`（58 行）：**全文件只有 `/farmstorm farmlandlimit <value>` 一个子命令**(`:37-41`)，权限 2、范围 0..4096(`:27-31`)；唯一动作 `ledger.setBase(value)`(:50-57)。
- 注册点：`event/FarmstormGameEvents.java:62` `gameBus.addListener(FarmstormGameEvents::onRegisterCommands)`、`:71-72` `FarmstormCommands.register(event.getDispatcher())`、`:4` import。
- config `farmland.limitPerPlayer`：`FarmstormConfig.java` push `:904` / define `:907`，默认 0，范围 0..4096；**唯一读者是 `FarmlandLedger.base()`**(:72-74)。
- 相关语言键 2 条：`farmstorm.configuration.farmland.limitPerPlayer`(`zh:145`)、`command.farmstorm.farmlandlimit.set`(`zh:64`)。
- **不注册任何真正的 GameRule**（`FarmstormCommands.java:18-20` 注释：1.21.1 `GameRules.register` 是 `private static`）。

### 1.10 版本号连带

- `gradle.properties:11` `mod_version=0.1.0`。
- `检查环境.cmd:14` 与 `一键构建.cmd:11-12` 里写死的 jar 名仍是 **`farmstorm-0.0.1+mc1.21.1.jar`**（0.1.0 时就没跟上，实测；`:10` 是 `if not exist "dist" mkdir "dist"`）。
- `build.gradle:28-47` 声明 `runClient` / `runServer` / `runData`，`gameDirectory` 可用 `-PfsRunDir=` 隔离（`gw.cmd` 只对含 `runServer` 的参数抢目录锁）。

---

## 2. 已裁定口径（作者四轮访谈，共 25 条，**下游只能引用，不得改写**）

| # | 条目 | 定为 |
|---|---|---|
| 1 | 版本号 | **0.1.1**；文档号 **28~33**；编号接续见 §0.2 |
| 2 | 「绿色 +」规则 | **全局作废**（规则层面废掉），但**本版只重生成 `wild_apple` 一张**，其余 24 张留到下版批量重生成 |
| 3 | 狂野苹果材质 | **直接用 64×64 原图**（不降采样、不再走 16 网格众数取色） |
| 4 | man瓜 身份 | = `farmstorm:pumpkin_turret` |
| 5 | man瓜 攻击间隔 | `pumpkin_turret.fireIntervalTicks` 默认 **60 → 40 刻**（2 秒） |
| 6 | man瓜 伤害 | `pumpkin_turret.projectileDamage` 默认 **1 → 3** |
| 7 | man瓜 炮弹速度 | `TurretBullet.SPEED` **0.6 → 0.45**（每秒 9 格），并**提成 config 键** |
| 8 | man瓜 追踪 | **不引入软追踪**；只修「**近距离绕圈/抖动**」（目标贴脸时反复掉头打不中） |
| 9 | 朋友的韭 窗口 | 落地后 **200 刻**内持续；**每刻**扫描球内生物；施加 **`200 − x`** 刻 Q弹（x=落地后经过刻数） |
| 10 | 朋友的韭 音效 | **落地那一刻**判定：球内 ≥1 生物就播**一次**，否则永不播（空投不播） |
| 11 | 朋友的韭 实体 | 落地后实体**存活 200 刻且保持可见** |
| 12 | 耕地-核心关系 | **每块耕地只归 1 个核管理**；**一个核最多管 16 块**；容量**直接复用** `outpost_core.farmlandBonus`（默认 16），不新增键 |
| 13 | 绑核规则 | 锄地成功时绑给**距离最近**的己方核（平手按先放置） |
| 14 | 迁移规则 | 核被毁时，只在「**影响范围覆盖该耕地** + **与耕地主同盟** + **还有空位**」的核里找接替者，**最近优先**；找不到 → 变泥土 |
| 15 | 老存档 | **启动时按新规则补建绑定**；补不上的**保持耕地不动**、只是不受保护（**绝不铲老存档**） |
| 16 | 额度 | **废弃 `farmland.limitPerPlayer`**，`capacity = 16 × 核数`；`/farmstorm farmlandlimit` 指令**一并删除** |
| 17 | 「己方」边界 | 核只管「**核主本人 + 同队队友**」的耕地；**离线也有效**（改从 UUID 索引查） |
| 18 | 据点核心亮度 | `.lightLevel(state -> 15)` |
| 19 | 浮窗显示谁 | **自己 + 队友**的**全部**核心 —— **含未加载区块、含其它维度**（不是「只显示当前维度」）；同盟判定与耕地保护同一套 |
| 20 | 浮窗内容 | 每核一行：**维度标识 + 坐标 + 归谁（自己 / 队友名字）**，按放置顺序（`placedGameTime` 升序，跨维度统一排序） |
| 21 | 浮窗开关/位置 | **客户端按键**（开关 + 进入拖拽）+ **客户端指令** + **CLIENT config 持久化**；位置用**拖拽模式**调，**不做自动避让** |
| 22 | 浮窗数据面 | **废除「0 自定义网络包」红线**，新增**自定义网络包**推送核心列表 |
| 23 | 红线新口径 | 网络包红线**整体废除**；**只保留「无 Mixin」** |
| 24 | 强制加载 | 核心所在**那 1 个区块**，**运行期临时**，不落盘（**不写 `world/data/chunks.dat`** —— 裁定 #24 早期版本写的 `level.dat` 是笔误（事实卡已按 A-38 修正）） |
| 25 | config 迁移 & 脚本 | man瓜 默认值改动**只写文档**、代码不做迁移；`检查环境.cmd` / `一键构建.cmd` 的 jar 名**顺带改 0.1.1** |

> ⚠️ **本表只记「作者拍板」的口径，不是决策的唯一来源。** `plan0.1.1.txt` 原文本身也是来源——例如**压缩干草块音效 `GRASS → STONE`（plan 第 3 条）没有对应裁定**，因为它是无歧义的单点改动，依据就是 `plan0.1.1.txt:20-23` 与 §1.6。下游不得因为没有裁定行就漏掉这条需求。
>
> **本卡补充（非作者裁定，可否决）**：浮窗最多显示 **8 行**，超出显示「+N」；默认位置左上角 `(4,4)`；自己=绿色、队友=白色；拖拽模式打开一个 Screen（单人会自动暂停），`ESC` 保存。维度标识取维度 id 的 path（`overworld` / `the_nether` / `the_end`），不新增语言键。

---

## 3. 增量清单

### 3.1 注册面增量：**+0**

| registry | 增量 | 说明 |
|---|---|---|
| 全部 12 类 | **+0** | 网络包 payload 走 `RegisterPayloadHandlersEvent`，**不进 `BuiltInRegistries`**，不计入 93 |
| 隐形 Marker / 按键 / config | **+0** | 用原版 `EntityType`、`KeyMapping`、`ModConfigSpec`，都不进 registry |

⇒ **注册对象仍 93**。⚠️ 30 号仍须**自己从源码逐类点名核对**（0.0.2/0.0.3 两次我都在这一格翻过车），不得直接抄本行。

### 3.2 必改清单（含**删掉什么**）

| # | 位置 | 现值 | 改后 | 原因 / 需求 |
|---|---|---|---|---|
| 1 | `gradle.properties:11` | `mod_version=0.1.0` | **0.1.1** | 版本号 |
| 2 | `检查环境.cmd:14`、`一键构建.cmd:10-11` | `farmstorm-0.0.1+mc1.21.1.jar` | `farmstorm-0.1.1+mc1.21.1.jar` | 裁定 #25 |
| 3 | `FarmstormBlocks.java:79` | `SoundType.GRASS` | **`SoundType.STONE`** | R6 |
| 4 | `FarmstormBlocks.java:82-89` | 无 `lightLevel` | **加 `.lightLevel(s -> 15)`** | 裁定 #18 |
| 5 | `FarmstormConfig.java:431-433` | `fireIntervalTicks` 默认 60 | **40** | 裁定 #5 |
| 6 | `FarmstormConfig.java:425-427` | `projectileDamage` 默认 1 | **3** | 裁定 #6 |
| 7 | `TurretBullet.java:28` | `SPEED = 0.6` 硬编码 | **删常量 → 读新 config `pumpkin_turret.bulletSpeed`（默认 0.45）** | 裁定 #7 |
| 8 | `TurretBullet.java:90-96` | 每刻直指目标、固定步长 | **改近距收敛**：`dist < SPEED` 时按 `dist` 收敛/直接判中，消除过冲抖动 | 裁定 #8 |
| 9 | `FriendChivesProjectile` | 未重写 `keepAliveAfterImpact()` | **重写返回 `true`**；新增 `tick()` 每刻扫球、施 `200 − elapsed` 刻 Q弹 | 裁定 #9 |
| 10 | `FriendChivesProjectile.java:56-80` | 落地一次性施加 + 同刻播音 | **音效逻辑保留在落地那一刻**（n≥1 播一次）；施加逻辑迁到 `tick()` | 裁定 #10 |
| 11 | `FarmlandLedger.java:51` | 只有 `byOwner` | **新增 耕地→核 绑定表**（含反查），存档格式升版并向下兼容 | 裁定 #12 |
| 12 | `FarmlandLedger.java:57-58,72-74` + `FarmstormCommands` | `baseOverride`/`base()`/`setBase` | **全删**（base 概念取消） | 裁定 #16 |
| 13 | `FarmlandGuard.java:120-134` | 遍历在线玩家 | **改查绑定表**（离线安全） | 裁定 #17 |
| 14 | `OutpostCoreBlockEntity.java:353-366` | 直接清算 | **先迁移、迁不动才 `setBlock(DIRT)`**；且**只处理本核管理的耕地** | 裁定 #12/#14 |
| 15 | `OutpostCoreBlockEntity` + `FarmlandLedger` | 无启动补建 | **新增启动补建**：未绑定耕地按 #13 规则补建，补不上保持不动 | 裁定 #15 |
| 16 | `command/FarmstormCommands.java`（整文件 58 行） | `/farmstorm farmlandlimit` | **删除整文件** | 裁定 #16 |
| 17 | `FarmstormGameEvents.java:62,71-72` + `:4` import | 注册指令 | **删除这 3 处** | 裁定 #16 |
| 18 | `FarmstormConfig.java:904,907` | `farmland.limitPerPlayer` | **删除整组**（`farmland` 组归零） | 裁定 #16 |
| 19 | `tools/gen_textures.py:274-275,407,974-975,546-560,612-623` | `add_plus` 机制 | **全局去掉叠「+」**（规则作废）；但**本版不重跑其余 24 张** | 裁定 #2 |
| 20 | `tools/gen_textures.py:154,270,884-895,849-881,59` | 16 网格取色 + 桌面源图 | `wild_apple` 改为**直读 Downloads 的 64×64 原图原样输出**；源图路径改默认 | 裁定 #3 |
| 21 | 新增 | — | **1 个自定义网络包**（服务端→客户端推己方核心列表）+ 注册 + 客户端 handler + 推送时机 | 裁定 #22 |
| 22 | 新增 | — | **2 个 `KeyMapping`**（开关 / 进拖拽）+ `RegisterKeyMappingsEvent` 首次注册 | 裁定 #21 |
| 23 | `FarmstormClient.java` + 新增 | — | **客户端指令**（`/farmstorm hud ...`）+ **CLIENT config**（首次 `ModConfig.Type.CLIENT`） | 裁定 #21 |
| 24 | 新增 | — | 浮窗渲染（`RenderGuiEvent.Post`，抄 `WildHunterHud`）+ 拖拽 Screen | 裁定 #21 |
| 25 | 新增 | — | 核心所在区块**临时强制加载 ticket**（释放时机 = 核被毁/停服） | 裁定 #24 |
| 26 | `ModLangProvider.java` | 315 键 | **删 2 加 N**（逐条点名，见 §3.5） | 连带 |
| 27 | `Farmstorm.java:103-104` 自检 | `demolition == 2` 等 | **不改**（本版不动作物档案）；注册面断言若 93 不变也不改 | — |

### 3.3 资源增量

| 类别 | 新增 | 就地替换 |
|---|---|---|
| PNG | **0** | **`textures/item/wild_apple.png`：16×16/571 B → 64×64**（源图 6,946 B） |
| JSON | **0** | `sounds.json` **不动**：可闻半径 = `max(volume, 1) × attenuationDistance`（**相乘**，非截断），现状 `2.0 × 16 = 32 格` 已达标；补 `attenuation_distance: 32` 会变成 64 格（仍进 30 号 §8 待验证 ④，但本版不碰该文件） |
| 音频 | **0**（素材不换，裁定见 1.8） | — |
| 语言键 | 见 §3.5 | 见 §3.5 |

### 3.4 config 变更

**SERVER（计入 166）**：**删 1 增 1 ⇒ 166 − 1 + 1 = 166**（总数持平，但**组成变了**；59 组 → 58 组，`pumpkin_turret` 组 4 → 5 键）

| 组 | 键 | 类型 | 默认 | 动作 |
|---|---|---|---|---|
| `pumpkin_turret` | `fireIntervalTicks` | int | 60 → **40** | 改默认值 |
| `pumpkin_turret` | `projectileDamage` | int | 1 → **3** | 改默认值 |
| `pumpkin_turret` | `bulletSpeed` | double | **0.45** | **新增**（原硬编码常量） |
| `farmland` | `limitPerPlayer` | int | — | **删键**（整组消失） |

**CLIENT（不计入 166，单独一份 `farmstorm-client.toml`）**：新增 3 键 `hud.enabled`(bool, true) / `hud.x`(int, 4) / `hud.y`(int, 4)。

⇒ **30 号必须逐键点名后自己写死总数**；本卡只给算式骨架：`166 − 1 + 1 = 166`（server），`+3`（client，不计入）。

### 3.5 语言键变更（**必须逐条点名后数**）

| 动作 | 键 |
|---|---|
| 删 | `farmstorm.configuration.farmland.limitPerPlayer` |
| 删 | `command.farmstorm.farmlandlimit.set` |
| 增 | `farmstorm.configuration.pumpkin_turret.bulletSpeed` |
| 增 | CLIENT config 3 键的 translation（若加 `.translation()`） |
| 增 | 浮窗「自己」标识（队友显示真实名字，不需要键） |
| 增 | 客户端指令反馈（on / off / reset 各一条，候选） |

⇒ **30 号逐条点名后的结果 = 323 × 2**：`315 − 2 + 10`。
增项 10 条 = `pumpkin_turret.bulletSpeed`(1) + CLIENT config 3 键(3) + 浮窗「自己」标识(1) + 客户端指令反馈 on/off/reset(3) + **2 个 `KeyMapping` 的描述键 `key.farmstorm.*`(2)**。
> ⚠️ 最后那一项最容易漏：新增 `KeyMapping` 会连带两条描述键。30 号首版算式漏了它（算出 321），逐条点名时才发现。**任何"加个按键"的改动都要顺手数这两条语言键。**

---

## 4. 风险（RT-31 起）

| 编号 | 风险 | 缓释 |
|---|---|---|
| RT-31 | **首次引入自定义网络包**：codec、双向注册、断线重连、跨维度、推送时机（登录/放置/摧毁/换维度）任一处漏都会让浮窗显示错数据 | 推送时机列成清单逐条验收；payload 只读不写（客户端不回包） |
| RT-32 | **耕地-核绑定制重构**：`farmstorm_farmland.dat` 存档格式升版，老档要向下兼容 + 启动补建 | 格式 v1→v2 兼容读；补建失败只降级保护、绝不写方块 |
| RT-33 | 每核容量复用 `farmlandBonus`：改这个键会**同时**改额度与容量 | 文档写明「一个键两重语义」；`verify_project` 的 166 校验不受影响 |
| RT-34 | 朋友的韭**每刻**扫描球体（200 刻 × N 实体 × 每场） | 只扫 `LivingEntity`、用 AABB 粗筛 + 距离复核；同时存在的场数量受投掷物寿命限制 |
| RT-35 | 落地实体存活 200 刻且可见：会受重力/碰撞影响，跨区块卸载可能丢 | 落地后置零速度 + 无重力 + 免碰撞；区块卸载即结束该场（文档写明） |
| RT-36 | 亮度 15 ⇒ 该位置**永不刷怪**、强光传播 15 格 | 写进设计增量与已知影响清单 |
| RT-37 | 64×64 物品贴图：**显示面积仍是 16×16**，高出 4 倍的细节会被平均；且 `gen_textures.py` 重跑会回滚 | 文档写明渲染事实；生成器默认源图与 `add_plus` 同版改掉 |
| RT-38 | man瓜 两个默认值改动对**已落盘 toml 无效** | 交付文档单列「升级须知」：删旧 toml 或手改两键 |
| RT-39 | 强制加载 ticket 泄漏（核被毁未释放） | 释放点与 `release()` 同处；停服兜底 |
| RT-40 | 删 `/farmstorm` 指令树的连带：`baseOverride`、2 条语言键、既有文档里的指令说明 | 必改清单 16~18 已列全；28 号登记为「对 0.1.0 的删除项」 |
| RT-41 | 破红线后**既有文档写「0 网络包」**，与 26 号主提示词冲突 | 32 号写新口径并显式声明「取代 0.1.0 的 HC」；README 红线段同步改 |

---

## 5. 六份文档的分工与顺序

| 文档 | 负责内容 |
|---|---|
| `28-DECISIONS-0.1.1.md` | **唯一真相源**：D-124 起逐条决策 + 25 条裁定表 + 对 0.1.0 的**增/改/删**三栏登记。**最先写** |
| `29-DESIGN-0.1.1.md` | 10 条需求全规格：时序表（朋友的韭 200 刻时序、拖拽交互）、数值表带依据列、边界清单、附录 A 差异登记 |
| `30-TECH-0.1.1.md` | 自己逐类点名核 93、类设计（网络包/绑定表/CLIENT config）、资源清单、config 全表、RT-31~RT-41、待验证 API 清单、验收自检 |
| `31-TASKS-0.1.1.md` | 任务 F-176 起、Phase 26 起、M22 起、裁决 A-30 起、**六张覆盖对照表** |
| `32-PROMPT-MAIN-0.1.1.md` | HC-77 起、N-75 起、事实卡、已知陷阱、**新红线段（只留无 Mixin）**、对照表 |
| `33-PROMPT-TASKS-0.1.1.md` | Phase **26~31 共 6 个**（每节固定 6 小项：前置 / 目标 / 任务 / 出口判据 / 翻车点 / 回报）+ 收尾提示词 + 跨 Phase 注意事项 |

**顺序**：28 串行 → 29 ‖ 30 并行 → 31 → 32 ‖ 33 → 中文全冻结 → 统一翻英文。

---

## 6. 给文档作者的硬禁令

1. **不许写死注册对象总数**，除非自己从源码逐类点名数出并把方法写进文档。
2. **不许凭空发明数值**。每个数字要么来自本卡 §1/§2/§3，要么标「本文补充」并给理由。
3. **不许改 `docs/zh/00`~`27` 与 `docs/en` 的既有文件**（已冻结）。本版只写 28~33。
4. **不许把「废除网络包红线」写成「放宽」或「例外」** —— 是**整体废除**，只留「无 Mixin」（裁定 #23）。
5. **不许把 `man瓜` 写成「南瓜炮台」以外的别名而不注明 id** —— 一律写 `man瓜（farmstorm:pumpkin_turret）`。
6. **不许把「耕地受保护」写成「范围内所有耕地」** —— 是**绑定到某个核且该核主与耕地主同盟**（裁定 #12/#17）。
7. **不许把「+」号作废写成「所有图标本版都去掉了」** —— 是**规则作废、本版只重生成 1 张**，其余 24 张仍是旧样（裁定 #2）。
8. 不写「待定」「TODO」「视情况而定」，不用「您」。
9. 与源码冲突时报给我，不许自行取舍。
10. **每个增量数字必须给算式**，并注明「由本文件自己逐类点名确认」。

---

## 7. 留给文档作者推导的（可标「本文补充」）

- 网络包的 codec 形态（`StreamCodec`）、payload 名字、推送时机集合与去抖
- 耕地绑定表的存档格式（NBT 结构、版本号、向下兼容读法）与补建算法
- 强制加载 ticket 的具体类型/等级/释放时机（`TicketType` 在 21.1.213 的可用形态）
- 浮窗默认坐标、行距、字号、配色、最大行数与溢出表现
- 拖拽 Screen 的交互（点击/释放、边界钳制、与 `options.hideGui` 的关系）
- 朋友的韭落地实体的物理收敛（置零速度、无重力、免碰撞、`settled` 后可见性）
- `RegisterClientCommandsEvent` 在 NeoForge 21.1.213 是否可用（**待验证 API**）
- `soundRange` 与 `sounds.json` 的 `attenuation_distance` 是否叠加（**待验证 API**）

---

## 附录 A　12 类注册对象逐条点名（基线 93，用于 30 号复核）

| 类 | 数 | 逐个 ID |
|---|---|---|
| items | 32 | 22 作物/物品：puffed_wheat / blast_carrot / ubi_server / toxic_ubi_server / power_beet / melon_slice_bomb / melon_grenade / pumpkin_turret / awakened_torchflower / awakened_pitcher_plant / fang_trap_cactus / bamboo_firecracker / red_mushroom_puff / brown_mushroom_puff / lucky_berry / molotov / zangief_cane / wild_apple / compressed_puffed_wheat / friend_chives / point_defense_fungus / vibration_bean；7 播种物：puffed_wheat_seeds / power_beet_seeds / melon_grenade_seeds / pumpkin_turret_seeds / awakened_torchflower_seeds / awakened_pitcher_plant_pod / chive_seeds；3 方块物品：converter / compressed_hay_block / outpost_core |
| blocks | 26 | 15 作物方块：puffed_wheat_crop / blast_carrot_crop / ubi_server_crop / toxic_ubi_server_crop / power_beet_crop / melon_grenade_stem / pumpkin_turret_stem / awakened_torchflower_crop / awakened_pitcher_plant_crop / fang_trap_cactus_block / bamboo_firecracker_stalk / red_mushroom_puff_block / brown_mushroom_puff_block / lucky_berry_bush / friend_chives_crop；8 部署方块：blast_carrot / pumpkin_turret / awakened_torchflower / awakened_pitcher_plant / fang_trap_cactus / zangief_cane / point_defense_fungus / vibration_bean；3 其它：converter / compressed_hay_block / outpost_core |
| block_entity_type | 7 | blast_carrot / pumpkin_turret / awakened_torchflower / awakened_pitcher_plant / fang_trap_cactus / converter / outpost_core |
| entity_type | 13 | puffed_wheat / ubi_server / toxic_ubi_server / melon_slice_bomb / melon_grenade / bamboo_firecracker / red_mushroom_puff / brown_mushroom_puff / molotov / compressed_puffed_wheat / friend_chives / vibration_bean / turret_bullet |
| mob_effect | 4 | wild_hunter / zangief_piledriver / q_bounce / confusion |
| sound_event | 3 | wild_hunter_loop / zangief_piledriver / friend_chives |
| recipe_type | 1 | converting |
| recipe_serializer | 1 | converting |
| creative_mode_tab | 1 | main |
| damage_type | 3 | void_bolt / crop_blast / zangief_slam |
| data_component_type | 1 | temporary_enchantments |
| menu_type | 1 | converter |
| **合计** | **93** | 32+26+7+13+4+3+1+1+1+3+1+1 |
