# 32 号 v0.1.1 主提示词（Master Prompt）

> 投喂方式：本文件**只投一次**，在任何 Phase 之前。之后按 33 号逐 Phase 投喂。
> 适用版本：v0.1.1（基线 v0.1.0 已实现并发版）。冲突时以 28 号决策记录为准。

| 项 | 值 |
|---|---|
| 文档编号 | 32-PROMPT-MAIN-0.1.1 |
| 读者 | 实现代理（能读写本仓库、能跑 `.tools\gw.cmd`） |
| 上游 | `28-DECISIONS-0.1.1.md`（唯一真相源，D-124 ~ D-147）→ `30-TECH-0.1.1.md` → `29-DESIGN-0.1.1.md` → `31-TASKS-0.1.1.md` |
| 本文件发什么编号 | **HC-77 ~ HC-96（20 条）**、**N-75 ~ N-92（18 条）**、事实卡 19 行、陷阱 20 条（含 1 条判据自检元规则）；风险沿用 **RT-31 ~ RT-41**，裁决引用 **A-30 ~ A-49** |
| 本文件不写什么 | 分阶段内容（33 号）、任务与排期（31 号）、类设计与算法（30 号） |

## 0. 你是谁、你要做什么

你是这个仓库在 v0.1.1 的**实现代理**。基线是**已经发版的 0.1.0**（`gradle.properties:11` 现为 `mod_version=0.1.0`，`dist/` 里是 0.1.0 的产物），本版只是增量：24 条决策（D-124 ~ D-147）落在 0.1.0 已有的注册面、config、账本、客户端、生成器与构建脚本上，**不重写 0.1.0 已经实现的东西**，也不新开第二套框架。**动笔前先读 28 号**——它是唯一真相源，与本文、与 30 号冲突时以它为准；本文只在两件事上比 28 号更硬：**红线新口径**（§1）与**执行纪律**（§2 起）。你一次只看到本文 + 33 号给的当前 Phase 任务书，所以**只做任务书要求的事**：做完就停、回报、等下一个 Phase，不提前实现下一阶段，不把「顺手改好」塞进本轮。数值一律不发明：能追到 D 号 / RT 号 / 源码 `文件:行号` 的照抄，追不到的写进回报的「未决问题」，不许自己定。0.1.0 已实现的追踪、陷阱、生长、部署、音效、粒子、索引、菜单等行为，本版**一行都不改**，除非 28 号某条决策点名要改。回报里每一句「完成」都要落到一条可观察的证据（日志行、命令读数、`git diff`、`jar tf`、存档 NBT、游戏内读数），不接受「表现正常」。另外记一条**既有缺陷**：`检查环境.cmd:14` 与 `一键构建.cmd:11`/`:12` 写死的仍是 `0.0.1` 的 jar 名，今天跑到 `检查环境.cmd:14` 就会报 jar 缺失——这是 0.1.0 时就遗留的，不是本版新增要求，本版顺带修正（A-51）。

## 1. 红线（**本版口径已变，必须按本节**）

### 1.1 保留：无 Mixin

唯一保留的项目级红线。**不许**出现 `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor`，也不许给 `build.gradle` 加 mixin 插件。

- 判定：`grep -rn "@Mixin\|mixins.json" src/main/java` **0 命中**；`git diff build.gradle` **为空**；`src/main/resources` 下没有 `*.mixins.json`。
- 依据：D-146；`BRIEF` §1.4（`@Mixin` 实测 0 个、`build.gradle` 无 mixin 插件）。

### 1.2 **已废除：0 自定义网络包**

v0.0.1 ~ v0.1.0 的红线是「自定义网络包 **0 个**」。**从 v0.1.1 起，这条红线整体废除**。本版恰好新增 **1 个** S2C 只读包（D-142），但**不设数量上限**。

四条读法，写错任何一条，六份下游文档全错：

1. **是整体废除，不是「放宽为 1 个」**。没有上限，也没有「破例申请」这种动作空间。
2. **不是「本版例外」**。原本的结构是「默认禁止 + 本次豁免」，现在那个「默认禁止」不存在了；「例外」这个词暗示禁令仍在，禁用。
3. **废除不等于必须加包**。本版只加 1 个，因为浮窗确实需要（客户端拿不到服务端的 `SavedData`）；今后每新增一个包仍须**单独立决策登记**，不得顺手加——本版登记的就是 D-142 那一个。
4. **「无 Mixin」不受影响**：仍是零，仍禁止（§1.1）。

**旧红线说法清单**（下面每一处都写着「0 个」，它们**已被 28 号 §5 整体废除**）：

| 文档 | 位置 | 旧说法 | 现在的状态 |
|---|---|---|---|
| `00-DECISIONS.md` | C-6 | 「0 个自定义 S2C 包」 | **整体废除**；C-6 的其余部分（音效 / 粒子走原版包）仍是实现事实，但不再作为禁令 |
| `10-DECISIONS-0.0.2.md` | `:73-76`（D-50） | 「三项侵入性效果不新增 `CustomPacketPayload`」 | **项目级禁令废除**；那三项效果的实现事实不变，本版不动它们 |
| `22-DECISIONS-0.1.0.md` | `:44`、`:68`、`:480` | 「0 自定义网络包」写作两条红线之一 | **整体废除** |
| `26-PROMPT-MAIN-0.1.0.md` | `:183-185`（HC-73）、`:402` | 同上，作为硬约束与对照表行 | **HC-73 中关于网络包的那半句**作废（不是「半个禁令仍有效」）；Mixin 与 `GameRules.register` 两句仍有效（§1.3） |
| `27-PROMPT-TASKS-0.1.0.md` | 验收判据 | 以「`grep CustomPacketPayload` 为 0」作为通过判据 | **判据作废**，改为「恰 1 个文件，且客户端不回包」 |
| `README.md` | `:71`、`:148`、`:191`、`:419`、`:423` | 红线段写「0 自定义网络包」 | 属 README 增量范围，随 D-146 改 |

**这 1 个包为什么非它不可**（四条，任一条单独成立就足以否掉「0 包」这条路，也是你**不许**为了省事把它删掉的理由）：

1. 核心列表是**服务端的 `SavedData`**（`world/data/farmstorm_outpost_core.dat`），客户端**没有副本**；
2. 「我有哪些核」必须是**全量**的：客户端只加载视距内的区块，视距外的核它根本不知道存在；
3. 归属者**可能离线**，而「队友关系」只有服务端能解析；
4. 列表需要**稳定顺序**与「8 行 + `+N`」的溢出，只有拿到一份完整快照才画得出来。

**实现时以本文件为准**：不得因为某份旧文档写着「0 个」就把这个包删掉，也不得反过来把「废除」理解成「本版可以随意加包」。这个包的三条边界（只读、不回包、服务端权威）写在 HC-87。

### 1.3 工程约束（不是红线，但同样不许破）

28 号 §4 只列「无 Mixin」一条红线；下面两条不在红线清单里，但破法与红线同级——破一条，本轮判未完成。

- **不许试图注册真 gamerule**：1.21.1 的 `GameRules#register` 是 `private static`，在「无 Mixin」内没有注册入口。判定：`grep -rn "GameRules.register" src/main/java` **0 命中**；全仓 `GameRule` 的唯一读点是 `explosion/FarmstormExplosion.java:109` 读原版 `RULE_MOBGRIEFING`（这一处不许动）。
- **服务端权威**：命中判定、耕地绑定、清账、封包全部在服务端；客户端只渲染，不判定、不回包、不读世界。判定：客户端 handler 只有一次引用替换；`grep -rn "playToServer\|sendToServer" src/main/java` **0 命中**；专用服务器起服不出现 `NoClassDefFoundError`。

## 2. 硬约束（HC-77 ~ HC-96）

HC 只约束本版增量面。0.1.0 的 HC-51 ~ HC-76 继续有效，但其中 8 条已被 §6.2 点名取代（HC-61 / HC-62 合并成一行，故 8 条对应 7 行）——**遇到冲突以本文件与 28 号为准**。每条给三项：**约束**（写什么算对）、**判定**（读到什么算过）、**来源**（决策 / 技术 / 风险编号）。判定列一律可执行：一条命令、一处 `git diff`、一行日志、一个方块状态。

- **HC-77 · 注册对象仍是 93，且逐类点名核对**
  - 约束：12 类小计必须仍是 `items 32 / blocks 26 / block_entity_type 7 / entity_type 13 / mob_effect 4 / sound_event 3 / recipe_type 1 / recipe_serializer 1 / creative_mode_tab 1 / damage_type 3 / data_component_type 1 / menu_type 1`，合计 **93**。本版新增的东西**一个都不进 registry**：payload 走 `RegisterPayloadHandlersEvent`、按键进客户端 `Options#keyMappings`、CLIENT config 走 `ModConfigSpec`、客户端指令走 Brigadier、绑定表是 `SavedData`、ticket 是普通对象。`Farmstorm.java:138-141` 的 12 条断言**不改**。
  - 判定：启动日志含「自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK」；`Farmstorm.java:138-141` 在 `git diff` 里 0 行改动；12 类**逐类点名**核对过（不许只改总数不改清单，也不许只改清单不改总数）。**数字必须与日志相同，类名以日志串为准**：约束里的 `block_entity_type` 在日志里写作 `block_entities`，两者不可能逐字相同。
  - 来源：D-142 / D-144 / D-145；28 号 §3.4「注册面 +0」；30 号 §1.2、§1.3。

- **HC-78 · server config 是 166 键 / 58 组，`farmland.limitPerPlayer` 不存在**
  - 约束：改后 server config = **166 键 / 58 组**（删 `farmland.limitPerPlayer` 及其整组、增 `pumpkin_turret.bulletSpeed`）；该键在源码、生成源、语言键、默认 toml 里**都不存在**；`pumpkin_turret` 组为 **5 键**。
  - 判定：**先删掉** `run/config/farmstorm-server.toml` 再起服，重新生成的文件里 `^\s*\[` 行数 = **58**、键行 = **166**、无 `[farmland]` 段；`grep -rn "limitPerPlayer\|farmlandLimitPerPlayer" src/` **0 命中**。旧 toml 里残留的 `[farmland]` 段**不算失败**（配置系统只补不删），但不得有任何代码读它。
  - 来源：D-139；A-42；30 号 §4.1；RT-33。

- **HC-79 · CLIENT config 3 键，单独一份文件，不计入 166**
  - 约束：新增 `ModConfig.Type.CLIENT` 一份 spec，3 键 `hud.enabled`（bool，**true**）/ `hud.x`（int，**4**）/ `hud.y`（int，**4**），落 `config/farmstorm-client.toml`（开发环境下实际落在 `run/config/farmstorm-client.toml`，与 server 那份并列）；**不计入 166**，也不与 server 混写。
  - 判定：首次启动客户端后 `run/config/farmstorm-client.toml` 存在、恰 3 键 / 1 组；`farmstorm-server.toml` 里没有 `hud` 段；server 组数仍 58。
  - 来源：D-144；30 号 §4.2、§2.4.2。

- **HC-80 · 语言键 323 × 2，中英键集完全相同**
  - 约束：改后两份语言文件各 **323** 键（`315 − 2 + 10`），且 `set(zh) == set(en)`；`farmstorm.configuration.*` 恰 **169**（= 166 server + 3 client）；新增 `key.farmstorm.*` **2** 条、`command.farmstorm.hud.*` **3** 条、`gui.farmstorm.core_hud.self` **1** 条；删掉的 2 条里包含 `farmlandlimit`。（`ModLangProvider` 的类注释叙述同改动注释里的键数也是产物的一部分）。
  - 判定：`zh_cn.json` 与 `en_us.json` 各 **323** 键且键集相等；`grep -c farmlandlimit` 两份都 **0**；`grep -c '"farmstorm.configuration\.'` 恰 **169**。
  - 来源：D-129 / D-140 / D-144；A-48；30 号 §4.3；RT-40。

- **HC-81 · 一块耕地只归 1 个核，每核容量 = `outpost_core.farmlandBonus`**
  - 约束：每块耕地同一时刻只有 **0 或 1** 个绑定；每核容量 = `outpost_core.farmlandBonus`（默认 **16**，值域 0 ~ 1024），**不新增键**；队伍容量 = 该键 × 己方核数；「已用」= 绑定条数；补不上的老条目（`orphans`）**不计容量也不计已用**。
  - 判定：同一核名下锄第 **17** 格被拒，存档 `bindings` 里该核仍 **16** 条；把 `farmlandBonus` 改成 **2** 后第 **3** 格被拒、队伍容量同步为 `2 × 核数`；`bindings` 里同一个 `pos` 只出现 1 次；容量日志里不出现 `orphans` 的条数。
  - 来源：D-134 / D-139；RT-33；30 号 §2.2.6。

- **HC-82 · 比较键三级，且只看索引与坐标**
  - 约束：任何「在多个候选核里选一个」的判定（锄地绑核、迁移、补建）用**同一条**三级键：`placedGameTime` 升序 → **维度 id** 升序 → `BlockPos#compareTo`（y → z → x）。比较值**只从 `OutpostCoreIndex`（`SavedData`）读**，不读方块实体、不加载任何区块；**不得依赖任何 `Map` / `List` 的迭代顺序**。
  - 判定：离线把两核 `gameTime` 改成同值后重启，排序按维度 id 升序；同维度同 `gameTime` 时按 `BlockPos#compareTo`；同一份存档连跑两次，日志 `核心比较键指纹 <hex16>` 两行完全相同；比较路径里 `getBlockEntity` / `getChunk` / `hasChunkAt` **0 命中**。
  - 来源：D-135；A-39；30 号 §2.2.7、§2.2.10。

- **HC-83 · 老档补建按 `BlockPos` 单级全序，整段不写世界**
  - 约束：启动补建（含 0.1.0 老档首次补建）先把候选耕地按 `BlockPos#compareTo`（y → z → x）**单级全序**排序，再逐条处理；不许用 `byOwner` / `orphans` 的插入顺序；补不上就**保持耕地不动**；整段没有 `setBlock`。
  - 判定：同一份 0.1.0 老档连跑两次，日志 `耕地补建：绑定 N 块 / 保持未绑定 M 块` 的 N、M 完全相同；补建段里 `setBlock` 0 命中；补不上的坐标逐个 `/execute if block … minecraft:farmland` 为真。
  - 来源：D-137；A-47（rev.2，取代 A-36 / A-44）；30 号 §2.2.8；RT-32。

- **HC-84 · 锄地三态不变，顺序固定**
  - 约束：`Verdict` 三态 `ALLOW` / `NO_CORE` / `QUOTA_FULL` 不改；顺序固定：① 范围闸（己方核 3×3）→ ② 候选核空位（`farmlandBonus − 该核已绑 > 0`）→ ③ 最近（`BlockPos#distSqr`）+ 三级平手键；两次拒绝都**不产生耕地、不扣耐久**。
  - 判定：范围外锄地返回 `NO_CORE`，方块不变、耐久不变；最近的核满时返回 `QUOTA_FULL`（即使更远处有空位）；`bindings.corePos` 等于较近那个核的坐标。
  - 来源：D-135；30 号 §2.2.7。

- **HC-85 · 三面判据分家：踩踏面「有绑定即保护」，额度与浮窗「实时己方」**
  - 约束：**踩踏面**按「**有绑定即受保护**」判定（`protectsFromTrample` 没有查询者参数，答不了「谁踩的」）；**额度闸（锄地）与浮窗**按**实时**己方判定（核主本人 + 同队队友，走 UUID / 名字，不看是否在线）。
  - 判定：队友全员离线时，己方绑定耕地仍不被踩坏；同一块地换队后踩踏**仍**受保护，但下一格锄地的容量按新队伍算、浮窗下一包按新队伍显示；`protectsFromTrample` 的签名里**没有** `Player` 参数。
  - 来源：D-138；A-45；30 号 §2.2.5 与附录 A #30。

- **HC-86 · 拆核：先算后写 + 三支**
  - 约束：核被毁**只处理本核名下**的耕地；先算完三份集合再动世界；三支 = ① 找到接替者（覆盖 + 与耕地主同盟 + 有空位，最近 + 三级键）→ 改绑；② 找不到 + 该区块在内存 + 方块仍是耕地 → `setBlock(DIRT)`；③ 找不到 + 该区块**未加载** → **只摘绑定、世界不动**。不许为清算而加载区块。
  - 判定：无接替者且人站远处（区块已卸载）拆核后，该田仍是 `minecraft:farmland`，日志含「迁移失败且区块未加载」；站在田边拆核则变 `minecraft:dirt` 且无掉落；清算路径里 `getChunk` / `hasChunkAt` 0 命中。
  - 来源：D-136；A-35；30 号 §2.2.9；RT-32。

- **HC-87 · 自定义包恰 1 个、单向只读**
  - 约束：全仓自定义 payload **恰 1 个**，方向 S2C；客户端 handler 只做一次引用替换，**不回包、不判定、不读世界**；`playToServer` / `sendToServer` **0 命中**；包不进注册面；客户端登出 / 换服必须清空快照。
  - 判定：`grep -rln "CustomPacketPayload" src/main/java` 恰 **1** 个文件；`grep -rn "playToServer\|sendToServer" src/main/java` **0**；专用服务器能起服、无 `NoClassDefFoundError`；从服务器 X 退到没有核的服务器 Y 后浮窗**立即消失**。
  - 来源：D-142 / D-146；RT-31；30 号 §2.1。

- **HC-88 · 浮窗范围与排序**
  - 约束：列自己 + 队友的**全部**核（**含未加载区块、含其它维度**），不列敌对核、不列核心物品；每核一行 `#n [维度 path] (x, y, z) 归谁`（自己用标识、队友用名字；维度取 id 的 **path**，不新增语言键）；按 **`placedGameTime` 升序**、跨维度统一排序。
  - 判定：主世界 1 核 + 下界 1 核，站在任一维度都是 **2 行**且行首分别为 `overworld` / `the_nether`，传送后不变；先放 A（t=100）后放 B（t=200）⇒ A 在上；把两核 `gameTime` 改成同值、跨维度同坐标 ⇒ 按维度 id 定序。
  - 来源：D-143；A-39；30 号 §2.1.5、§2.4.4。

- **HC-89 · 浮窗版式：8 行核心 + 1 行 `+N`，面板高 `10n + 3`**
  - 约束：最多 **8 行核心**；超出时**另加 1 行** `+N`（`N = total − 8`）⇒ 最高 **9 行**；面板高 = `10n + 3`（8 行 **83 px** / 9 行 **93 px**）；`+N` 不新增语言键；`#n` 按**截断前**的全量顺序编号。
  - 判定：9 个己方核时画 8 行核心 + 1 行 `+1` = **9 行 / 93 px**；8 个核时 8 行 / 83 px / 无 `+N`；版式分解**只认** 行高 9 / 行距 1 / 内边距 2，两套分解不许并存。
  - 来源：D-143 第 4 项；A-34 / A-41；30 号 §2.4.4。

- **HC-90 · 朋友的韭：场长读 `friend_chives.effectTicks`，不是常量**
  - 约束：落地后持续 **`friend_chives.effectTicks`** 刻（默认 **200**，值域 20 ~ 1200）；**每刻**扫落点球内（半径取 `friend_chives.hitRadius`，默认 6 格、欧氏距离）的 `LivingEntity`，施加 **`effectTicks − x`** 刻 Q弹（x = 落地后经过刻数）；第 `effectTicks` 刻自毁。代码里场长只有 `fieldLength` 一个来源。音效**只在落地那一刻判一次**：球内 ≥1 个生物播一次，空投不播，场内后走进来的不补播。
  - 判定：把 `effectTicks` 改成 **100** 后重测，递减止于 100、第 **100** 刻实体消失；`grep -n "200" src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java` 的命中只允许出现在注释里（代码行为 0）；空投无 `friend_chives` 音、命中恰 1 条。
  - 来源：D-131 / D-132 / D-133；A-33；RT-34 / RT-35；30 号 §2.7。

- **HC-91 · man瓜（`farmstorm:pumpkin_turret`）三个数值 + 两个读者**
  - 约束：`pumpkin_turret.fireIntervalTicks` 默认 **40**、`projectileDamage` 默认 **3**、`bulletSpeed` 默认 **0.45**（新键，值域 0.0 ~ 4.0）；`TurretBullet.SPEED` 常量删除，**两处读者同读该键**（`entity/TurretBullet.java:95` 与 `behavior/impl/PumpkinTurret.java:75`）；不引入软追踪，只做近距离收敛。
  - 判定：删旧 toml 后 `[pumpkin_turret]` 恰 **5** 键，三值为 `40` / `3` / `0.45`；`grep -rn "TurretBullet.SPEED" src/` **0 命中**；两次开火时间差 ≈ **2000 ms**；目标贴到 1 格内连续 5 发全中。
  - 来源：D-127 / D-128 / D-129；A-30；30 号 §2.6；RT-38。

- **HC-92 · 压缩干草块：五类声音全变石头**
  - 约束：`registry/FarmstormBlocks.java:79` 的 `SoundType.GRASS` → **`STONE`**；影响**破坏 / 放置 / 踩踏 / 挖掘 / 落地**五类声音（`getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound`）；硬度 30 / 抗爆 6 / 掉落工具要求 / `MapColor.COLOR_BROWN` 一律不动。
  - 判定：五个 `/playsound` 正反对照各一次（`block.stone.*` 一致、`block.grass.*` 不同）；`grep -c "SoundType.GRASS" src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java` 为 **0**；该行 `git diff` 只有一处改动。
  - 来源：D-130；A-31；30 号 §3 第 7 行、§7 #24 ~ #28。

- **HC-93 · 据点核心亮度 15**
  - 约束：`outpost_core` 注册链加 `.lightLevel(state -> 15)`，其余属性一字不动（`MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `SoundType.STONE` / `pushReaction(BLOCK)`）。
  - 判定：F3 的 `Light` 行 = **15**；`grep -c "lightLevel" src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java` = **1** 且只在核心那一链；该文件 `git diff` 不含其它属性行。
  - 来源：D-141；RT-36；30 号 §3 第 8 行。

- **HC-94 · 强制加载只用裸 `TicketType`**
  - 约束：核所在区块的运行期临时加载**只用** `TicketType` + `addRegionTicket` / `removeRegionTicket`，申请与释放**成对**、释放点与 `release()` 同处、停服兜底；**不许** `setChunkForced`、**不许** NeoForge `TicketController#forceChunk`；落盘的 `world/data/chunks.dat` 里不得出现核的 `ChunkPos`；验收**只写 tick 面**。
  - 判定：`grep -rn "setChunkForced\|forceChunk" src/main/java` **0 命中**；`chunks.dat` 的 `Forced` 数组不含核坐标；票等级日志显示中心 `newTicketLevel = 32`、紧邻一圈 `33`；拆核后同一位置不再有边界粒子。
  - 来源：D-145；A-38 / A-43；RT-39；30 号 §2.5。

- **HC-95 · 版本号只有一个来源**
  - 约束：`mod_version` 只在 `gradle.properties:11` 改一次（`0.1.0` → `0.1.1`）；`检查环境.cmd` 与 `一键构建.cmd` 里写死的 jar 名**同一次**改成 `farmstorm-0.1.1+mc1.21.1.jar`（含 `-sources`）；其余文档只引用、不各自声明版本。
  - 判定：`grep -c "0\.1\.0" gradle.properties` 为 **0**；两支 `.cmd` 里 `farmstorm-0.1.1+mc1.21.1` 命中 **3** 处（`检查环境.cmd:14`、`一键构建.cmd:11`、`:12`），`0.0.1` **0 命中**；`build/libs/` 与 `dist/` 的产物名一致。
  - 来源：D-124 / D-147；30 号 §3 第 1、2 行。

- **HC-96 · 代码不做 config 迁移**
  - 约束：不给已落盘的 `farmstorm-server.toml` 写迁移逻辑（不改、不删服主的键值）；「升级须知」只写进交付文档。
  - 判定：`grep -rn "farmstorm-server.toml" src/main/java` **0 命中**；`git diff` 里没有读写 toml 的代码；保留旧 toml 直接升级时 `fireIntervalTicks` 仍 **60**、`projectileDamage` 仍 **1**，而 `bulletSpeed` 被自动补成 **0.45**。
  - 来源：D-147；RT-38；30 号 §4.1 升级须知。

## 3. 禁止行为（N-75 ~ N-92）

每条都可用一条命令、一处 `git diff` 或一次读数判定，不是态度要求。「为什么」只给一句，够你判断边界即可。

- **N-75** 不得把「废除网络包红线」写成「放宽」「例外」「本版允许 1 个」。
  - 判定：文档、注释、回报里涉及网络包的句子中 `grep -rn "放宽\|例外\|允许 1 个"` 为 0。
  - 为什么：结构上已经没有那条禁令，写成「例外」会让下一个读者把包删掉（28 号 §4.2）。

- **N-76** 不得把耕地保护写成「范围内所有耕地」「核范围内自动保护」。
  - 判定：文档与注释里描述保护面时 `grep -rn "范围内所有耕地\|范围覆盖"` 为 0；代码判据必须从绑定表来。
  - 为什么：那是 0.1.0 的模型，也是 `plan0.1.1.txt:14` 要修的 bug（28 号 §6 第 1 条墓碑）。

- **N-77** 不得把「+」号作废写成「所有图标本版都去掉了」。
  - 判定：`git status` 里 `textures/item/` 只有 `wild_apple.png` 一张改动；`NO_PLUS_ICONS` 已从生成器删除。
  - 为什么：作废的是**规则**，本版只重生成 1 张，其余 24 张仍是旧样（D-125）。

- **N-78** 不得把 `man瓜` 写成别名而不注 id。
  - 判定：中文版里 `man瓜（farmstorm:pumpkin_turret）` 至少出现一次，且**首次**出现即为该形态；英文版里 `Pumpkin Turret (man瓜)` 至少出现一次，且**首次**出现即为该形态；两份文档 `grep -rn "南瓜炮台"` 均为 0。
  - 为什么：含「瓜」的对象有 4 个（`pumpkin_turret` / `melon_grenade` / `melon_slice_bomb` / `melon_grenade_stem`），别名会指错（28 号 §0.2）。

- **N-79** 不得把「时点性事实」写成验收断言。
  - 判定：验收与自检里 `grep -rn "调用点\|唯一调用\|只有 1 个调用"` 为 0；「全仓只有一处」一律换成可数的**文件数 / 键数 / 命中数**。
  - 为什么：A-37——绑定制引入「摘绑定」后写入口不再唯一，这类断言实现后必红，且分不清是缺陷还是预期。

- **N-80** 不得在 `verify_project.py` 按本版口径改完前引用它的「FAIL 0」。
  - 判定：验收记录里出现「FAIL 0」时必须同时给出该工具的 diff（已加 v0.1.1 的期望来源、已从并集剔除 `farmland.limitPerPlayer`）；改完前只准引用分段读数（`93 / 93`、`323 键`）。
  - 为什么：A-40——它的 config 期望集来自四份旧文档，删 1 增 1 后绿灯只能是漏检。

- **N-81** 不得为了清算 / 比较 / 显示而加载区块。
  - 判定：拆核清算、比较键路径、浮窗组装三处的 `getChunk` / `hasChunkAt` / `getBlockEntity` 全为 0。
  - 为什么：A-35 与 A-39——一次强制加载会连锁一批区块，并让结果随加载时机漂移。

- **N-82** 不得修 A-49 的跨维度同坐标撞车。
  - 判定：账本的耕地记录仍只有 `pos`（无维度字段）、`dataVersion` 仍为 **2**（不升 3）、没有「给老档补维度」的迁移代码。
  - 为什么：A-49 已裁定本版不修，理由是老档无法回溯维度、且耕地侧已叠过一次存档升版。

- **N-83** 不得跳过待验证 API 的确认、直接假设签名。
  - 判定：30 号 §8 的 8 条各有「最小用例的命令 + 结果」记录；未确认前不得用空实现、`UnsupportedOperationException` 或注释掉功能「让它编译过」。
  - 为什么：本版 6 个技术构件（包 / 按键 / CLIENT config / 客户端指令 / ticket / 绑定表）在本仓全是第一次。

- **N-84** 不得改 28 / 29 / 30 / 31 号已定的数值。
  - 判定：`git status` 里 `docs/` 为 0 改动；回报里出现的新数值必须能追到某个 D 号，或标注「实现实测值」。
  - 为什么：六份文档是冻结的上游，改口径要先改 28 号并登记。

- **N-85** 不得以任何形式复活 `farmland.limitPerPlayer`。
  - 判定：`grep -rn "limitPerPlayer\|baseOverride\|setBase" src/ tools/` 为 0；`farmstorm-server.toml` 的 `[farmland]` 段不再被任何代码读取；那 2 条语言键已删。
  - 为什么：D-139 / D-140——它会立刻造出「base + 容量」的双源。

- **N-86** 不得把本版新增构件塞进 registry。
  - 判定：93 与 12 类小计不变；`git diff src/main/java/com/dashpig/farmstorm/registry/` 只有 `FarmstormBlocks.java` 的两处（`:79`、`:82-89`）。
  - 为什么：新增件全都不进 `BuiltInRegistries`（30 号 §1.3），塞进去会同时破 93 与「+0」。

- **N-87** 不得给容量 / 额度单开第二个旋钮。
  - 判定：`FarmstormConfig` 的 diff 里 `pumpkin_turret.bulletSpeed` 是唯一新增键，`outpost_core.farmlandBonus` 的键名与默认值不变；没有 `farmlandCapacity` / `outpost_core.capacity` 之类的新键。
  - 为什么：RT-33——两个旋钮一旦不同，「这块地算不算超额」就没人答得上来。

- **N-88** 不得从方块实体读比较键。
  - 判定：`OutpostCoreIndex.Entry` 带 `gameTime` 且比较路径只读索引；`getBlockEntity` 在浮窗组装与 `FarmlandGuard.check` 里 0 命中。
  - 为什么：A-39 / 30 号 §2.2.10——方块实体是否存在取决于加载时机，取值会漂移。

- **N-89** 不得客户端回包或在客户端做己方判定。
  - 判定：`playToServer` / `sendToServer` 0 命中；客户端 handler 只有一次引用替换；客户端代码里没有 `isAlly` 调用。
  - 为什么：D-142 的三条边界——客户端能做判定就等于客户端能伪造「己方」。

- **N-90** 不得动 `sounds.json`。
  - 判定：`git diff src/main/resources/assets/farmstorm/sounds.json` 为空；`friend_chives` 段仍未设 `attenuation_distance`；`friend_chives.soundRange` 默认仍 32.0。
  - 为什么：可闻半径 = `max(音量, 1) × attenuationDistance` 是**相乘**，现状 `2.0 × 16 = 32` 已达标，补 32 会变 **64** 格（30 号 §5）。

- **N-91** 不得让「200」在代码里出现第二次。
  - 判定：`grep -n "200" src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java` 的命中只允许出现在注释里（代码行为 0）；场长只来自 `fieldLength`（读 `friend_chives.effectTicks`）。
  - 为什么：A-33——配置改成 100 时会出现「场跑 200 刻、每人只拿 `100 − x`」的自相矛盾。

- **N-92** 不得改 `ThrownCropEntity` 的既有生命周期钩子。
  - 判定：`git diff src/main/java/com/dashpig/farmstorm/entity/ThrownCropEntity.java` 为空（本版只重写子类 `keepAliveAfterImpact` 并新增子类 `tick`）；基类 `keepAliveAfterImpact` 仍默认 `false`、`settle` 仍只结算一次；**该文件在本版不得改动**（`keepAliveAfterImpact` 的默认值保持 `false`，由子类自己重写）。
  - 为什么：8 个投掷物共用这个基类，动它等于同时改全部投掷物（30 号 §3 第 18、42 行「不改」）。

## 4. 事实卡

只收「不写就会错」的源码事实。**查不到 `文件:行号` 的名字一律不写**——本仓没有 `Conprint` 这类类，凭印象编一个名字进代码就是编译错误。

| # | 事实 | 值 / 现状 | 证据 |
|---|---|---|---|
| 1 | 注册面 93 的 12 类断言写死在启动自检里 | `32 / 26 / 7 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1`，合计 93 | `Farmstorm.java:138-141` |
| 2 | server config 现状 166 键 / **59 组**，其中 24 组是**嵌套**段（toml 里带缩进） | 顶层 35 段 + 嵌套 24 段 | `FarmstormConfig.java:268-928`；`run/config/farmstorm-server.toml:85`（`\t[blast_carrot.nausea]`） |
| 3 | `outpost_core.farmlandBonus` 默认 **16**，值域 0 ~ 1024 | 一键两义：每核容量 + 额度基数 | `FarmstormConfig.java:911-913` |
| 4 | `friend_chives.hitRadius` 6.0（0 ~ 16）、`effectTicks` **200**（20 ~ 1200） | 场长与施加基准都取后者 | `FarmstormConfig.java:851-853`、`:854-856` |
| 5 | `pumpkin_turret` 组现状 4 键 | `targetRadius 15` / `projectileDamage 1` / `knockbackLevel 1` / `fireIntervalTicks 60` | `FarmstormConfig.java:422-433` |
| 6 | `TurretBullet.SPEED` 有**两个**读者 | 删常量必须两处同改 | `entity/TurretBullet.java:95`、`behavior/impl/PumpkinTurret.java:75` |
| 7 | 全仓 `SoundType` 只有 3 个值，`STONE` 的唯一先例是 `outpost_core` | `GRASS :79` / `STONE :88` / `CROP :100,:110` | `registry/FarmstormBlocks.java` |
| 8 | `SoundType` 有五类声音 getter | `getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound`；改类型即五类同变 | 原版 `SoundType`；改动点 `FarmstormBlocks.java:79` |
| 9 | `outpost_core` 注册链**没有** `.lightLevel` | 现值 0 | `FarmstormBlocks.java:82-89` |
| 10 | `FarmlandLedger.byOwner` 外层是 **`HashMap`**，内层是 `LinkedHashSet` | 「插入顺序」只在同一个 UUID 内部成立 | `FarmlandLedger.java:51` |
| 11 | `OutpostCoreIndex.byOwner` 是 **`HashMap`**；`Entry` 只有 `(dimension, pos)` | 本版要给 `Entry` 加 `gameTime` 并落盘 | `OutpostCoreIndex.java:50`、`:53` |
| 12 | `FarmlandLedger` 的 NBT **只有** `owners → positions → pos`（long），**无维度字段** | 老档无法回溯耕地维度 | `FarmlandLedger.java:46-48`、`:228-244` |
| 13 | `protectsFromTrample(ServerLevel, BlockPos)` **没有查询者参数**，且从在线玩家出发 | 只能回答「这块地有没有绑定」 | `farmland/FarmlandGuard.java:120-134`（`:125`） |
| 14 | `keepAliveAfterImpact()` 默认 `false`，**唯一既有重写者是爆竹** | 本版是第二个重写者 | `entity/ThrownCropEntity.java:116-118`；`entity/thrown/BambooFirecrackerProjectile.java:42` |
| 15 | `settle()` 是命中唯一结算点；`!keepAliveAfterImpact()` 才 `discard()`；`settled` 之后寿命检查不再生效 | `settled` 是 `private`、无访问器 | `entity/ThrownCropEntity.java:95-104`、`:65-72`、`:33` |
| 16 | `placedGameTime` 已落盘在方块实体 | 写入 `:405`、读取 `:413`、字段 `:79` | `blockentity/OutpostCoreBlockEntity.java` |
| 17 | 1.21.1 **没有 ticket 持久化**；强制加载唯一的落盘是 `world/data/chunks.dat` | `ForcedChunksSavedData.FILE_ID = "chunks"` | 30 号 §2.5.1（逐个枚举 `net/minecraft/server/level/`） |
| 18 | `verify_project.py` 的 config 期望集来自**四份旧文档**的并集 | `01-DESIGN` + `12-TECH-0.0.2` + `18-TECH-0.0.3` + `24-TECH-0.1.0` | `tools/verify_project.py:222-237` |
| 19 | 版本号只有一个来源 | `mod_version=0.1.0` 现况 | `gradle.properties:11` |

> 两条附注：① 基类文件是 **`entity/ThrownCropEntity.java`**，不在 `entity/thrown/` 下（那一层是 12 个投掷物子类）；② `farmstorm-server.toml` 的嵌套段是**缩进**的，数段必须用 `^\s*\[`，用 `^\[` 只会数到顶层 35 段。

## 5. 已知陷阱（**本版最重要的一章**）

把 A-30 ~ A-49 这 20 条裁决翻译成「你会怎么踩」。第 1 ~ 8 条是本版最贵的八个坑，先读这八条；第 19 条不是裁决，是本章写作期实测发现的计数坑（30 号的计数口径要按它修正）；**第 20 条是元规则**——它约束的不是代码，是判据本身。

| # | 陷阱（实现者会怎么踩） | 踩了会怎样 | 正确做法 | 依据 |
|---|---|---|---|---|
| 1 | 删 `TurretBullet.SPEED` 只改一处，以为常量只有一个读者 | `behavior/impl/PumpkinTurret.java:75` 还读它 ⇒ **编译失败**；改完只让炮弹飞行读 config、发射初速仍是旧值，则出膛与飞行两个速度 | 两处同读 `pumpkin_turret.bulletSpeed`；`grep -rn "TurretBullet.SPEED" src/` 为 0 | A-30 |
| 2 | 把 `wild_apple.png` 换成 64×64，却没让生成器同版跟上 | ① 生成器不变 ⇒ 下一次重跑静默把图换回 16×16；② 输出改成 64×64 而 `expected_size()`（`:982-983`）仍恒返回 16×16 ⇒ `:1153-1154` 抛 `SystemExit`，**整轮生成中止**（其余图标也一起停） | 直通分支、`expected_size()`、断言三处同版改；改完重跑一次仍是 64×64 | A-32 / D-126 |
| 3 | 用 `setChunkForced` 或 `TicketController#forceChunk` 图省事 | 两者都写 `world/data/chunks.dat`，停服后那个区块仍被强制加载，与「运行期临时」直接冲突 | 裸 `TicketType` + `addRegionTicket`，释放与 `release()` 同处、停服兜底 | A-38 |
| 4 | 拿 `byOwner` / `orphans` 的 Map / List 迭代顺序当次序 | 同一份存档两次启动给出不同的绑核与不同的补建结果（`byOwner` 是 `HashMap`） | 候选核用三级键；补建用 `BlockPos` 单级全序；两次启动比「指纹」两行 | A-39 / A-47 |
| 5 | 把 `effectTicks` 当常量 200 写进代码 | 配置改成 100 时出现「场跑 200 刻、每人只拿 `100 − x`」；写满 `effectTicks` 又会每刻续期 | 场长读 `friend_chives.effectTicks`，施加 `effectTicks − x`，代码里没有第二个 200 | A-33 |
| 6 | 只写 tick 面，却在验收里断言「只加载 1 个区块」 | 票的等级向外传播，紧邻一圈必然 border 加载（3×3）⇒ 断言必假，验收永远红 | 验收只写 tick 面（中心 `32` / 邻圈 `33`）；加载面不作为断言 | A-43 |
| 7 | 认为踩踏面还要判队伍，顺手给 `protectsFromTrample` 加个「查询者」参数 | 该函数没有查询者参数，踩踏事件本身也不带人；改签名会波及既有调用点 | 踩踏面「有绑定即受保护」；额度闸与浮窗仍按实时己方判 | A-45 |
| 8 | 改完直接信 `verify_project.py` 的绿灯 | 它的期望集来自四份旧文档，删 1 增 1 后必然对不上 ⇒ 绿灯只可能来自漏检 | 先按 A-40 改工具；改完前不引用 `FAIL 0`，只引用分段读数 | A-40 |
| 9 | 把压缩干草块音效当成「破坏 + 放置两类」 | 踩踏 / 挖掘 / 落地仍是草声，验收少 2 条，玩家走上去当场听出来 | 五类声音一起变；逐类 `/playsound` 正反对照 | A-31 |
| 10 | 把 `+N` 挤进 8 行里 | 第 9 个核把 `+N` 吃掉，面板 83 px，与 93 px 的验收对不上 | 8 行核心 + **另加** 1 行 `+N` = 9 行 / 93 px | A-34 |
| 11 | 照 A-36（按 `byOwner` 插入顺序）或 A-44（按 `orphans` 插入顺序）写补建 | A-36 已被 A-47 取代、A-44 已作废；0.1.0 老档没有 `orphans` 键，顺序仍不可复现 | 只认 A-47：`BlockPos` 单级全序（y → z → x） | A-36 / A-44 / A-47 |
| 12 | 把「某方法全仓只有一个调用点」写进验收 | 绑定制引入「摘绑定」后写入口不再唯一 ⇒ 断言实现后必红，且分不清是缺陷还是预期 | 这类时点性事实一律换成可数的文件数 / 键数 / 命中数 | A-37 |
| 13 | 为了把「迁不动的田变泥土」而对未加载区块 `getChunk` | 一次强制加载连锁一批区块；方向也错——该降级为「失去保护」却变成铲地 | 未加载一律**只摘绑定、世界不动**，不为清算加载区块 | A-35 |
| 14 | 让两套浮窗版式并存（行高 10 / 行高 9） | 两个人分别算出 83 px 与 93 px，验收时二选一 | 只认 行高 9 / 行距 1 / 内边距 2，面板高 `10n + 3` | A-41 |
| 15 | 按 30 号 §3 的 44 行收工 | 漏掉「删 `farmland` 整组 config」这一行（动作面应为 **47 = 44 + 3**：44 行 + A-42 一行 + A-55 两行），`[farmland]` 残留在源码 | 删整组，并把这一行补进改动面清单 | A-42 |
| 16 | 把 30 号 §7 的 38 条验收与 29 号 §11 的 39 条边界当一一对应 | 有 21 条边界没有对应断言，误以为「没断言 = 没边界」，边界清单被悄悄裁掉 | 边界以 29 号为准；38 条验收与 39 条边界**不要求一一对应**（A-46）；其中 **21 条**边界在 §7 里没有对应断言，由 **F-210 / F-211** 各自承担 14 / 7 条，必须逐条点名 | A-46 |
| 17 | 算语言键时漏掉 2 条 `key.farmstorm.*` | 算成 **321**，lang 段永远差 2 键、中英键集还会不等 | `315 − 2 + 10 = 323`；两份各 323 且 `set(zh) == set(en)` | A-48 |
| 18 | 顺手「修」耕地跨维度同坐标撞车（给账本加维度字段） | 触发本版第二次存档升版；老档无法回溯维度，只能给默认值或懒回填，引入新的不可复现面 | 本版**不修**，只登记为已知限制；账本仍只有 `pos` | A-49 |
| 19 | 用 `^\[` 数 toml 的段数 | 嵌套段是**缩进**的 ⇒ 只数到顶层 35 段（改后 34），与 58 对不上，误判成「组数错了」 | 用 `^\s*\[` 数（改前 59、改后 58）；键行单独用 `^\s*[A-Za-z_].*=` 数 | 30 号 §7 #2 的计数口径（本章实测修正） |
| 20 | **判据自身的失效（元规则）**：任何一条 grep / `git diff` / 计数判据上线前，**先在当前状态跑一遍**——它现在就该是 0 或某个数，先确认这个数算得出来 | `^\[` 数不到缩进段、缺 `src/main/java/` 前缀的 grep 恒 0、pathspec 写错的 `git diff` 恒空：三者都属于「判据永远通过或永远失败」，比数值写错更隐蔽 | 每条判据先在**改前**跑一次并报出实测值，改后再跑一次；两次都算得出来，判据才算立住 | A-52 / A-57 / A-58 与本表其余各条 |

> 落地方式：每条判据的「改前实测值」写进任务回报，改完再报一次；只写「通过」不写数值的，按未验证处理。

## 6. 与 0.1.0 文档的冲突处理

22 ~ 27 号已冻结，**一个字都不改**，它们里面仍有旧口径（28 号 §4.5 列了要写新口径的六份）。你遇到冲突时按下面两张表读，不许回改冻结文档，也不许把旧口径当成「已知事实」写进代码注释。

### 6.1 口径冲突

| 0.1.0 的文档说什么（位置） | 本版以什么为准 | 决策 |
|---|---|---|
| `22-DECISIONS-0.1.0.md:44`、`:68`、`:480` 与 `26-PROMPT-MAIN-0.1.0.md:183-185`（HC-73）：「0 自定义网络包」是红线 | 红线**整体废除**，只留「无 Mixin」；本版有 1 个 S2C 只读包，**不设上限**（§1.2） | D-142 / D-146 |
| `22` 号 D-118：「只在己方核 3×3 内可耕地、多核范围取并集」，保护面 = 范围内的己方耕地 | 范围闸保留；**归属**改为唯一绑定：一块地只归 1 个核，每核容量 16 | D-134 / D-135 |
| `22` 号 D-116 / D-117：额度 = `base + bonus × 核数`，载体是 `farmland.limitPerPlayer` + `/farmstorm farmlandlimit` | 额度 = **`16 × 己方核数`**；`base`、那个 config 键、整条子指令一起删 | D-139 / D-140 |
| `22` 号 D-116、`25` 号与 `README.md:191`：耕地规则走 `/farmstorm farmlandlimit` | 服务端 `/farmstorm` **不再存在**；只剩客户端 `/farmstorm hud <on\|off\|reset>`（另一条注册，不共用根节点语义） | D-140 / D-144 |
| `22-DECISIONS-0.1.0.md:581`、`26-PROMPT-MAIN-0.1.0.md:199`、`27-PROMPT-TASKS-0.1.0.md:405`：`wild_apple.png` 是「11 色 / 透明 126」 | 实测 **88 个去重 RGBA / 107 个全透明 / 571 B**；本版换成 **64×64** 原图（源图 6,946 B） | D-126；28 号 §7 第 2 条 |
| `22` 号 §6 第 4 条、`10-DECISIONS-0.0.2.md:73-76`（D-50）、`00-DECISIONS.md`（C-6）：「无自定义包 / `CustomPacketPayload` 仍为 0」 | 本版**恰 1 个** S2C 只读包（`farmstorm:core_list`）；旧禁令只作为历史口径，不再复查 | D-142 / D-146 |
| `22` 号 §7.2 的 D-57 行：压缩干草块 `sound(GRASS)` 不动 | 改 `SoundType.STONE`，**五类**声音全变 | D-130 |
| `22` 号 D-120 第 4 项：核被毁时范围内己方耕地**全部**变泥土 | 本核名下先迁移、迁不动才变泥土；未加载区块只摘绑定 | D-136 |
| `22` 号 D-121 第 2 条、`23-DESIGN-0.1.0.md:49`：客户端零新增代码 | 客户端 **+5 类**（按键 / 浮窗渲染 / 拖拽 Screen / 客户端指令 / 快照） | D-142 / D-143 / D-144 |

### 6.2 被取代的 0.1.0 硬约束（显式声明）

| 0.1.0 主提示词的条目 | 它当时说什么 | 本版 | 依据 |
|---|---|---|---|
| HC-53 | config 166、语言键 **315** | config 仍 166（组成变），语言键 **323** | D-139 / D-144；A-48 |
| HC-60 | 耕地规则 = config 默认值 + `/farmstorm farmlandlimit` | 两者都删；额度由核容量求和 | D-139 / D-140 |
| HC-61 / HC-62 | 拦截顺序「范围 → 额度」；额度 = `base + 16 × 核数`，坐标集合记账 | 顺序保留，第二闸改为「单核空位」；额度 = `16 × 己方核数`，记账载体改为绑定表 | D-134 / D-135 / D-139 |
| HC-65 | 拆核时只遍历账上坐标、直接转泥土 | 先迁移、再三支；只处理本核名下 | D-136 |
| HC-69 | 边界粒子那条里写着「0 自定义网络包」 | 删去这一句；粒子仍走原版包 | D-146 |
| HC-73 | 0 自定义网络包、无 Mixin、`GameRules.register` 为 0 | **HC-73 中关于网络包的那半句**作废（本版 1 个 S2C）；无 Mixin 与 `GameRules.register` 两句保留（§1.1、§1.3） | D-146 |
| HC-76 | `wild_apple` 16×16 / 11 色 / 透明 126 | **64×64 原图**；模型 `layer0`、物品名、语言键不动 | D-126 |

## 7. 每轮结束的自检

一个任务或一个 Phase 收尾时照下表跑一遍，没跑的不算完成。命令都在仓库根目录执行，路径用本机形式。

| # | 检查项 | 命令 / 观察点 | 通过判据 |
|---|---|---|---|
| 1 | 编译与构建 | `.tools\gw.cmd compileJava`、`.tools\gw.cmd build` | 退出码 0；产物 `build/libs/farmstorm-0.1.1+mc1.21.1.jar`，`dist/` 同名同步 |
| 2 | DataGen | `.tools\gw.cmd runData` | 两份 lang 落到 `src/generated/resources`，各 **323** 键 |
| 3 | 注册面 | 启动日志第一行 | `自检 注册对象 93 个：… → OK`；12 类**数字**与 HC-77 相同，类名以日志串为准 |
| 4 | server config | 删旧 toml → 起服 → 读 `run/config/farmstorm-server.toml` | `^\s*\[` 行数 **58**、键行 **166**、无 `[farmland]`；`[pumpkin_turret]` **5** 键且 `40` / `3` / `0.45` |
| 5 | client config | 起客户端后读 `run/config/farmstorm-client.toml` | 恰 **3** 键：`true` / `4` / `4` |
| 6 | 语言键 | 两份 lang + grep | 各 **323**、键集相等、`farmlandlimit` 0、`"farmstorm.configuration.` 恰 **169** |
| 7 | 红线 grep | 在 `src/main/java` 下：`@Mixin\|mixins.json`；`CustomPacketPayload`；`playToServer\|sendToServer`；`GameRules.register` | 依次为 **0** / **恰 1 文件** / **0** / **0** |
| 8 | 禁用项 grep | 在 `src/` 与 `tools/` 下：`limitPerPlayer\|baseOverride\|setBase`；`setChunkForced\|forceChunk`；`TurretBullet.SPEED` | 全为 **0** |
| 9 | 日志断言 | 起服、拆核、老档各跑一次 | `耕地补建：绑定 N 块 / 保持未绑定 M 块`；`核心比较键指纹 <hex16>` 与 `耕地绑定指纹 <hex16>` **连跑两次完全相同** |
| 10 | jar 计数 | `jar tf build/libs/farmstorm-0.1.1+mc1.21.1.jar` | `network/CoreListPayload.class` **1**；`FarmstormKeys` / `CoreHudRenderer` / `CoreHudDragScreen` / `CoreHudClientCommands` / `ClientCoreList` 各 **1**；`command/FarmstormCommands.class` **0** |
| 11 | 落盘检查 | `world/data/chunks.dat`、`world/data/farmstorm_farmland.dat` | `Forced` 数组不含核坐标；账本含 `dataVersion: 2` |
| 12 | 越界检查 | `git status --short` | 只出现新增文件与 28 号点名的既有文件；`docs/` **0** 改动 |
| 13 | 工具适用性 | `python -X utf8 tools/verify_project.py` | 按 A-40 改完后：`93 / 93`、config **166**、lang **323**、`FAIL 0`；**改完前只引用分段读数** |

回报里三行证据固定：**命令原文 + 关键输出行 + 本表编号**（或 HC / N 编号）。日志超过 20 行只留第一条错误与总结行。本文件不回答「这轮做哪件事」——那是 33 号当前 Phase 的事。

## 8. 开工前的确认与编号接力

### 8.1 开工前必须确认的四件事

| # | 要确认的事 | 去哪里看 | 不成立时怎么办 |
|---|---|---|---|
| 1 | 这个任务落在哪条决策上、哪些数值已经定死 | 28 号 §1 的 D 条（四条字段：结论 / 依据 / 落地位置 / 补充） | 找不到对应 D 条 ⇒ 停下来问作者，不自己定口径 |
| 2 | 要用的 API 有没有被核对过 | 30 号 §8 的 8 条（每条给「不确定时的退路」） | 先写最小用例跑通，再把结论写进回报（N-83） |
| 3 | `verify_project.py` 是否已按本版口径改完 | `tools/verify_project.py:222-237` 与 30 号 §3 第 43 行 | 没改完就先做工具那一步；改完前不引用 `FAIL 0`（N-80） |
| 4 | 运行目录是否隔离 | `-PfsRunDir=` 与 `gw.cmd` 的 `runServer` 目录锁 | 本版会**同时**跑 `runServer`（验绑定 / ticket）与 `runClient`（验浮窗），撞目录会互相覆盖日志 |

四条贯穿全仓的旧坑仍然成立：`neoForge { }` 大小写敏感；`.tools\gw.cmd` 是唯一构建入口（不直接调 gradle）；两个 `runServer` 必须用 `-PfsRunDir=` 隔开；ffmpeg 不在 PATH 上（本版不改音频，别去动 `tools/convert_media.ps1`）。

### 8.2 编号接力（只引用，不新开）

| 系列 | 上一版到 | 本版从 | 本文件怎么用 |
|---|---|---|---|
| 决策 D-xx | D-123 | **D-124** | 只在引用里出现，不新开 |
| 任务 F-xx | F-175 | **F-176** | 由 31 号发，本文件不预支 |
| Phase | 25 | **26** | 由 31 / 33 号发 |
| 里程碑 M-xx | M21 | **M22** | 由 31 号发 |
| 硬约束 HC-xx | HC-76 | **HC-77** | 本文件发 HC-77 ~ HC-96 |
| 禁止行为 N-xx | N-74 | **N-75** | 本文件发 N-75 ~ N-92 |
| 风险 RT-xx | RT-30 | **RT-31** | 由 30 号 §6 发，本文件只引用 |
| 裁决 A-xx | A-29 | **A-30** | 由 31 号裁决章发；**A-44 已作废、A-47 是 rev.2、A-49 是已知限制** |
| 文档号 | 27 | **28** | 本版是 28 ~ 33 |

两条纪律：① 代码注释带编号（例如 `// D-131：场长读 config，不写死 200`），不带编号的猜测句不写；② 编号只引用、不新开——写出 `RT-42` 或 `A-50` 这类本版不存在的号，等于给下一个读者留一个查不到的引用。