# 06 · 分阶段任务清单（Task Prompt）

> **文档状态**：本文件是 v0.0.1 施工期的历史记录，其中的数值与结构描述可能已被实现修正。**实现口径以 00-DECISIONS.md 为准**（见 D-42~D-48），完整差异清单见 09-IMPL-VS-PLAN.md。

| 项 | 值 |
|---|---|
| 文档编号 | 06-PROMPT-TASKS |
| 版本 | v0.0.1 |
| 读者 | 投喂编码代理的人 + 被投喂的 CLI 编码代理（Claude Code / Codex 一类） |
| 上游 | `plan0.0.1.txt`、`docs/zh/00-DECISIONS.md`（唯一真相源）、`01-DESIGN.md`、`02-TECH-SPEC.md`、`03-TASKS.md` |
| 同级 | `05-PROMPT-MAIN.md`（常驻主提示词；本文件**不重复**其 HC-01~HC-14、N-01~N-14） |
| 装载方式 | 每个 Phase 投喂一次：`05-PROMPT-MAIN.md` 全文 + 本文件对应的那一节 |
| 内容 | 10 个 Phase / 59 个任务（F-01 ~ F-59），任务 ID 与标题逐字对齐 `03-TASKS.md` |

---

## 0. 使用说明（给人读）

### 0.1 本文件与 05-PROMPT-MAIN.md 的分工

| 文件 | 管什么 | 投喂频率 |
|---|---|---|
| `05-PROMPT-MAIN.md` | 全局纪律与硬约束（HC-01~HC-14、§4 代码规范、§5 执行纪律、§6 完成定义、§7 回报格式、§8 禁止行为、§9 已知陷阱） | 每个阶段都常驻 |
| `06-PROMPT-TASKS.md`（本文件） | 这一轮做哪些任务、做到什么算过、交付后怎么开下一阶段 | 一次只取一节 |

三条规矩：

1. 本文件不重写硬约束，只引用编号。看到「HC-xx」「N-xx」「RT-xx」就回 05 或 TECH 查原文，不要凭记忆理解。
2. 两者冲突时以 05 的硬约束为准（05 §0 的裁决顺序：DEC > TECH > DESIGN > plan0.0.1.txt > 本文件）。
3. 本文件的验收判据只在 05 §6 的完成定义之内收紧，不额外发明新的交付物。

### 0.2 怎么投喂

1. 打开本文件，找到当前 Phase 的小节（`## Phase N · …`），从标题开始整段复制。
2. 与 `05-PROMPT-MAIN.md` **全文**一起投给编码代理，**一次只投一个 Phase**。
3. 等它按 05 §7 的回报模板交回，再对照本节「本阶段出口判据」逐条验收。
4. 验收通过后，用 §12 的交接语开下一阶段。

两条禁令：

- 不要把 §1 总览表一起投——代理会试图一次做完十个阶段（同 N-01 的错误）。
- 不要跳过任何一节：Phase 编号即投喂顺序，`03-TASKS.md §1.1` 的依赖图已经排好了。

### 0.3 怎么判断一个阶段完成

三道闸全绿才算过，缺一条都按「未完成」处理：

1. **出口判据**：本节「本阶段出口判据」每一条都能指到命令输出、`/data get` 回读、文件内容或日志行。说「实现了但没验」不算过。
2. **任务覆盖**：任务清单里每个 F-xx 都有一份可指认的证据；代理漏做时按 05 §5.5 要求回补，不要自己补。
3. **仓库状态**：`./gradlew build` 绿；`git status` 只出现本节边界内的文件；`plan0.0.1.txt` 与上游文档未被触碰（HC-06）。

阶段失败时的处理顺序：先让它按 05 §5.4 自查 → 仍不过就把未通过的验收项抄回任务书重派一次 → 两次不过才换新会话重做，并在交接语里点名卡在哪一条。

### 0.4 四处最新裁决（覆盖 `03-TASKS.md` 的旧表述，写错即返工）

| # | 裁决 | 正确写法 | 常见错法 |
|---|---|---|---|
| C-9 | 部署方块**只存** `ownerUuid` + `ownerName` | 己方判定在触发瞬间**实时解析**归属者的当前队伍；归属者离线时按其最后已知队伍在 Scoreboard 成员表反查；归属者无队伍时按 D-31 的 `self` 口径 | 存「队伍名快照」或存 `PlayerTeam` 对象（`03-TASKS.md §0.4` 裁决 2 的旧表述已被 C-9 取代） |
| C-8 / D-37 | 固定硬挖 **10 秒只属于** `pumpkin_turret` | 火把花 / 瓶子草：己方按原版正常挖掘速度；非己方（含创造模式）进度恒 0；任何爆炸都破坏不了 | 把 10 秒套到 `awakened_torchflower` / `awakened_pitcher_plant` 上 |
| C-7 | 进度 **12 条**、资源 **257** 个、语言 **216** 键 | 257 = JSON 169 + TOML 1 + PNG 87（多出的 6 个是进度 JSON） | 沿用 TECH §6.1 的旧口径 251 / 204 |
| C-6 | 粒子与音效**用原版包** | `ClientboundLevelParticlesPacket` / `ClientboundSoundPacket` | 自己写 S2C 网络包（同时违反 HC-03） |

### 0.5 数字口径速查（每个阶段都用得上）

| 量 | 值 | 出处 |
|---|---|---|
| 作物 | 15 种（不是 16，C-1） | DEC §4 |
| registry 对象 | 64 个 | TECH §3 |
| config 键 | 117 个（Phase 1 只落 58 个，其余随阶段追加） | TECH §4.11 |
| 资源文件 | 257 个 | TECH §6.1 + C-7 |
| 语言键 | 216 个 | TECH §7 |
| 进度 | 12 条（含 1 条隐藏） | C-7；DESIGN §6 |
| 任务 | 59 个 / 10 个 Phase | `03-TASKS.md §2` |

---

## 1. 总览表（给人看，不要随阶段一起投）

| Phase | 标题 | 任务数 | 任务 ID | 前置 Phase | 出口判据（一句话） |
|---|---|---|---|---|---|
| Phase 1 | 工程基座 | 5 | F-01 ~ F-05 | — | `build` 产 jar、`runServer` 打印 `Done`、`config/farmstorm-server.toml` 生成 58 键、`Farmstorm.id()` 可用 |
| Phase 2 | 注册层全量 | 8 | F-06 ~ F-13 | Phase 1 | 创造栏 22 物品齐全、19 个方块可 `/setblock`、9 个实体可 `/summon`、6 个 BlockEntityType 中 5 个已注册 |
| Phase 3 | 爆炸与全局事件 | 5 | F-14 ~ F-18 | Phase 1（实操需 Phase 2 的 F-09/F-12/F-13） | 一次 `FarmstormExplosion` 能「按规则破坏 + 逐实体封顶 + 死因正确」，同 tick 12 次爆炸摊到 2 个 tick，火把花在 TNT 下存活 |
| Phase 4 | 部署行为 | 7 | F-19 ~ F-25 | Phase 2、Phase 3 | 胡罗贝自爆、仙人掌出两圈尖牙、man瓜开火、两种光环上 effect、单区块与单玩家上限生效 |
| Phase 5 | 投掷物 | 8 | F-26 ~ F-33 | Phase 2、Phase 3 | 8 种投掷物全部可投出并产生 `plan0.0.1.txt` 描述的落点效果，伤害不超上限，创造模式不消耗 |
| Phase 6 | 转换器 | 6 | F-34 ~ F-39 | Phase 2 | 小麦一次转换得膨胀麦并扣 3 点经验、反向可转回、经验不足被拒绝且不扣等级、5 个转换器对象全部入册 |
| Phase 7 | 食用型与幸运浆果 | 4 | F-40 ~ F-43 | Phase 3、Phase 6 | 甜菜！给 4 个增益；幸运浆果 8 档全部可被强制触发；创造模式分支崩溃后不残留 |
| Phase 8 | 数据驱动内容 | 5 | F-44 ~ F-48 | Phase 2、Phase 6 | 21 条配方可正反转换、20 张战利品表可 `/loot give`、4 个标签可判定、12 条进度可解锁、`/reload` 不命中过期配方 |
| Phase 9 | 资源与 DataGen | 6 | F-49 ~ F-54 | Phase 4、Phase 6、Phase 8 | `runData` 一次跑通、产物 257 个（JSON 169 + TOML 1 + PNG 87）、游戏内无紫黑方块且无原样 lang 键 |
| Phase 10 | 验收与发布 | 5 | F-55 ~ F-59 | Phase 4、Phase 5、Phase 7、Phase 9 | DoD 18 项全勾、RT-02/03/05/08/09 各有一次通过记录、`build/libs` 产出可发布 jar |
| **合计** | | **59** | F-01 ~ F-59 | | |

读表要点：

- Phase 3 的官方前置只有 Phase 1，但 F-14 依赖 F-13、F-17 依赖 F-09、F-18 依赖 F-12——三者都在 Phase 2。**实操上按 1 → 2 → 3 的顺序投，不要跳过 Phase 2。**
- Phase 4 / 5 / 6 在 Phase 3 之后彼此不阻塞，但本文件仍按编号顺序排；换序只在 F-44（Phase 8）之后才有意义。
- 全项目合计 96.0 人日（不含 25% 缓冲）；三人并行约 40 人日、单人串行约 120 人日（`03-TASKS.md §13.3`）。

---

## 2. Phase 1 · 工程基座（F-01 ~ F-05）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 2.1 投喂前必须已就绪

- 仓库里只有 `plan0.0.1.txt` 与 `docs/zh/00~04` 文档，**没有任何 Java 源码**（05 §2.1 第 5 条）。
- 本机能跑 `./gradlew`，JDK 21 可用，能联网拉取 ModDevGradle 与 NeoForge 21.1.213。
- 已确认仓库内**不存在** `*.mixins.json` 与 `@Mixin`（HC-04）。

### 2.2 本阶段目标

让一个还没有任何注册内容的空模组在 ModDevGradle 上四端可跑，并把注册骨架、己方判定、静态档案、配置框架一次性立稳——后续所有阶段只往里追加条目，不再改这四处的形状。

### 2.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-01 | 构建基线落地 | 让空模组在 MDG 下完成 build / runClient / runServer / runData 四端首次成功运行 | ① `./gradlew build` 产出 `build/libs/farmstorm-0.0.1+mc1.21.1.jar`；② `./gradlew runServer` 日志出现 `Done (` 且无 `ERROR` 行；③ `build.gradle` 扩展块名为 `neoForge`（大小写敏感）、`neo_version=21.1.213` 不含通配符；④ `./gradlew runData` 退出码 0 |
| F-02 | 主类与注册骨架 | 建立模组入口与 8 个 `DeferredRegister` 骨架，使后续注册任务只需追加条目 | ① 构造器把 8 个 `REGISTER` 挂 mod 总线并调用 `container.registerConfig(ModConfig.Type.SERVER, …)`；② `Farmstorm.id("converter").toString()` 等于 `farmstorm:converter`；③ `runClient` 启动日志无 `Missing registry` 与注册异常 |
| F-03 | 己方判定与归属工具 | 把「是不是部署者的己方」抽成接口 + 唯一实现，并提供归属数据的读写工具 | ① `/team add red` 后同队玩家 `isAlly` 为 true、非同队为 false、部署者本人恒为 true；② 无队伍玩家在 `teams.untamedFallback=self` 下只对自身友好；③ 归属只落 `ownerUuid` + `ownerName` 两个字段，`grep -rn "PlayerTeam" team/` 仅命中实现类内部局部变量（C-9） |
| F-04 | 静态档案与距离工具 | 集中 15 个作物的静态档案，并提供切比雪夫 / 欧氏两种距离工具 | ① `CropProfile.ALL` 恰好 15 条，ID 与 `00-DECISIONS.md §4` 逐字一致；② `DistanceMetrics.chebyshev(3,1,3)==3`、`euclidean(3,4,0)==5.0`（启动日志断言）；③ `DeployedKind` 含 5 个枚举值 |
| F-05 | 配置通用项与效果模板键 | 落地 7 个通用分组与 22 组效果键，并搭好重载后重建缓存的钩子 | ① `config/farmstorm-server.toml` 出现 `[converter] [stack] [deploy] [teams] [explosion] [throwable] [food]` 七段；② 文件内键总数 = 58（14 通用 + 44 效果）；③ 改 `converter.xpCost` 为 7 后 `/reload`，`FarmstormConfig.converterXpCost` 读到 7 且日志打印一行 `bake()` |

### 2.4 本阶段出口判据

- [ ] `./gradlew build` 产出 `farmstorm-0.0.1+mc1.21.1.jar`，`runServer` 打印 `Done (` 且日志无 `ERROR`，`runData` 退出码 0。
- [ ] `config/farmstorm-server.toml` 生成且键数恰为 **58**（本阶段只要求 58；51 个作物专属键与 8 个概率键随 Phase 4/5/6/7 追加，最终 117 由 F-56 复核）。
- [ ] `Farmstorm.id("converter")` 的断言输出出现在日志里。
- [ ] 8 个 `DeferredRegister` 全部已注册到 mod 总线，且 `registry/` 包里没有任何逻辑代码。
- [ ] `git status` 只出现 `build.gradle`、`gradle.properties`、`settings.gradle`、`gradle/wrapper/`、`src/main/resources/META-INF/neoforge.mods.toml`、`src/main/resources/pack.mcmeta` 与 `src/main/java/com/dashpig/farmstorm/**`；上游文档与 `plan0.0.1.txt` 未变。

### 2.5 常见翻车点

1. **扩展块写成 `neoforge { }`**：报错后不要反复猜，先看插件 DSL 提示再按实情写，并把结论写进回报（05 §9 第一条）；同时禁止把 `neo_version` 改成 `21.1.+` 或「升级试试」（N-14）。
2. **在 F-02 里顺手写业务内容**：F-02 只交付骨架与 `Farmstorm.id()`，一旦开始注册具体 items/blocks，阶段边界就没了，后面 F-06/F-08 的验收无法区分是谁引入的缺陷。
3. **F-05 把 58 个键散落到多个类**：键必须在 `FarmstormConfig` 单点声明（05 §4.4：同一数值只允许一个来源）。

### 2.6 完成后应向用户回报

- jar 路径与 `./gradlew build` 的结论行；`runServer` 的 `Done (` 行；`runData` 退出码。
- `config/farmstorm-server.toml` 的键计数命令与结果（应为 58），以及七段分组的名称。
- `Farmstorm.id()` 的断言输出行。
- 8 个 `DeferredRegister` 的清单。
- 未决问题（尤其是 §9 第一条那类「扩展块到底叫什么」的结论）。

---

## 3. Phase 2 · 注册层全量（F-06 ~ F-13）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 3.1 投喂前必须已就绪

- Phase 1 的出口判据全部通过：`Farmstorm`、8 个 `DeferredRegister`、`CropProfile`、`DistanceMetrics`、`FarmstormConfig` 已可编译。
- `./gradlew build` 绿（本阶段每个任务结束都要再绿一次）。
- 已读过 `02-TECH-SPEC.md §3` 的对应 registry 表（items / blocks / entity_type / block_entity_type / damage_type / data_component_type）。

### 3.2 本阶段目标

把 64 个注册对象中的 **59 个**落进 registry（转换器相关的 5 个在 Phase 6 落地），并让它们能被放置、被召唤、被查询。

### 3.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-06 | 作物方块与 14 个注册 | 用唯一的 `FarmstormCropBlock` 实例化 14 个作物方块完成注册，**不**注册 BlockItem（D-34） | ① 14 条 `/setblock ~ ~ ~ farmstorm:<id>` 全部成功；② 每个方块含 `age` 属性且 0~3 四值合法；③ `melon_slice_bomb` 无作物方块（`grep` 命中 0）；④ `/gamerule randomTickSpeed 100` 下 5 分钟内 `age` 递增到 3 |
| F-07 | 投掷物基类与 9 个实体类型 | 实现 `ThrownCropEntity` 基类，并把 8 个投掷物 + 1 个炮台导弹登记为 EntityType | ① `FarmstormEntityTypes` 内恰好 9 个 `DeferredHolder`；② 9 条 `/summon farmstorm:<id> ~ ~ ~` 全部成功且不崩；③ 参数为 `sized(0.25,0.25)`（`turret_bullet` 0.3）、`clientTrackingRange(4)`、`updateInterval(10)`；④ `submunition` 经 `SynchedEntityData` 同步，客户端日志打印到值 |
| F-08 | 物品体系与 22 个物品注册 | 实现 5 个物品类并注册 15 个作物 + 6 个播种物 + 1 个转换器物品 | ① 创造栏 22 个物品齐全；② 服务端启动日志打印 22 个物品的 `stacksTo`：8 个投掷型为 16、其余 14 个为 64（D-16）；③ 6 个播种物对着泥土右键放置对应 `_crop` / `_stem` / `_stalk` / `_block` / `_bush` 方块；④ 5 个部署型物品在耕地上右键走播种分支（T-02） |
| F-09 | 部署方块与 5 个注册 | 实现部署方块的附着 / 悬空 / 破坏规则并注册 5 个方块 | ① 对玻璃 / 半砖 / 树叶右键被拒绝且物品不消耗；② `pumpkin_turret` 顶 / 底 / 侧三面均可放置，其余 4 个仅顶面；③ 依附方块被挖掉后 `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` 变 `floating=true` 不下落，`blast_carrot` / `fang_trap_cactus` 直接消失并掉落本体；④ `/setblock ~ ~ ~ farmstorm:pumpkin_turret[face=wall,facing=east,floating=false]` 成功 |
| F-10 | 部署物 BlockEntity 与 5 个类型 | 落地部署物的运行时状态、存档与同步，并用同一个类注册 5 个 BlockEntityType | ① 5 个 ID 独立但共用同一个类与同一个 factory；② `/data get block` 能读到 `ownerUuid` `ownerName` `placedGameTime` `detached` `pendingWaves` `fuseTicks` 这 6 个同步字段（TECH §5.3 白名单），且**读不到**任何队伍快照字段（C-9）；③ 退出重进世界后上述字段保持；④ `getUpdateTag` 不含 `behavior` 与 `cooldown`；⑤ `getTicker` 在客户端返回 null |
| F-11 | 创造模式标签页 | 注册自定义标签页并按「作物 → 播种物 → 工具方块」排序 | ① `itemGroup.farmstorm.main` 显示为「农场风云」；② 标签页内 22 个物品顺序为 15 作物 → 6 播种物 → converter；③ `BuildCreativeModeTabContentsEvent` 只往本标签页追加（其它标签页条目数不变） |
| F-12 | 数据组件 temporary_enchantments | 注册临时附魔账本组件并确定其序列化形态 | ① `DataComponentType` 注册成功且 ID 为 `farmstorm:temporary_enchantments`；② `stack.set(...)` 后 `stack.get(...)` 往返一致；③ 启动日志打印 `Codec` 编解码后的等价比较结果为 true；④ 物品放入箱子再取出后组件仍在 |
| F-13 | 伤害类型 void_bolt / crop_blast | 用数据驱动方式提供 2 个伤害类型并把它们并入原版标签 | ① `/damage @s 1 farmstorm:void_bolt` 与 `farmstorm:crop_blast` 均成功；② 死亡信息显示「被虚空撕碎」与「被作物炸飞了」；③ `/damage` 自动补全出现两条 ID；④ 穿爆炸保护 IV 皮甲受 `crop_blast` 伤害明显低于裸装 |

### 3.4 本阶段出口判据

- [ ] 创造标签页出现 **22** 个物品，顺序正确。
- [ ] 14 个作物方块与 5 个部署方块全部可 `/setblock`；`melon_slice_bomb` 没有作物方块。
- [ ] 9 个实体全部可 `/summon` 且不崩。
- [ ] 6 个 BlockEntityType 中 **5 个**已注册（第 6 个 `farmstorm:converter` 属 F-35）。
- [ ] 2 个伤害类型可 `/damage`，死亡信息为中文显示名而非键名。
- [ ] 按 `03-TASKS.md §14` 逐条点数：本阶段应覆盖 items 22 + blocks 20 + entity_type 9 + block_entity_type 5 + damage_type 2 + data_component_type 1 + creative_mode_tab 1 = **60 项中的 59**（recipe_type / recipe_serializer / menu_type 与 converter 的 block_entity_type 留给 Phase 6），点数结论写进回报。
- [ ] `./gradlew build` 绿；`git status` 只出现 `registry/`、`block/`、`entity/`、`item/`、`blockentity/`、`datagen/`(若涉及) 与 `data/` 下本次范围内的文件。

### 3.5 常见翻车点

1. **`registry/FarmstormBlocks.java` 被三次写入互相覆盖**：F-06 / F-09 / F-38 都改这个文件。约定「一个任务只追加自己那一段」，每次追加后立刻数条目数，并在回报里写清本次新增的条目。
2. **顺手给 14 个作物方块注册 BlockItem**（违反 D-34），或在两处各写一份 `Properties` 字面量（05 §4.3）。作物方块没有 BlockItem，它的播种形态是 F-08 里那 6 个种子物品。
3. **把队伍名写进 `getUpdateTag` 或 NBT**：违反 C-9 与服务端权威（HC-03）。同步白名单就是 TECH §5.3 那 6 个字段（`ownerUuid` / `ownerName` / `placedGameTime` / `detached` / `pendingWaves` / `fuseTicks`），队伍字段一个都不在里面。

### 3.6 完成后应向用户回报

- 每个 registry 的条目数与总点数（对照 `03-TASKS.md §14`），并指认哪几项留到 Phase 6。
- 22 个物品的 `stacksTo` 打印行（8×16 / 14×64）。
- 一条 `/data get block` 的回读样本，明确列出字段名（证明没有队伍快照）。
- 一条 `/damage` 的死亡信息原文。
- 未决问题（尤其是 F-07 里 `SynchedEntityData` 的实际签名、F-12 的 Codec 形态）。

## 4. Phase 3 · 爆炸与全局事件（F-14 ~ F-18）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 4.1 投喂前必须已就绪

- Phase 1 全部通过；**Phase 2 的 F-09 / F-12 / F-13 已完成**（本级三个任务的硬前置，缺一个就别开本阶段）。
- 已读过 `02-TECH-SPEC.md §5`（服务端权威）与 §8（性能上限），知道粒子音效走原版包（C-6）。
- `FarmstormGameEvents` 需要的 game 总线事件类尚未存在（本阶段 F-15 建它）。

### 4.2 本阶段目标

先把「破坏 / 伤害 / 表现」三段式爆炸、爆炸批处理、爆炸免疫、临时附魔账本与玩家事件总线立起来——后面所有含爆炸或光环的作物都复用它，避免先写作物再回头改爆炸。

### 4.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-14 | 三段式爆炸与伤害封顶 | 实现 `FarmstormExplosion` 与 `BlastRule`，把方块破坏、逐实体封顶伤害、粒子音效拆成三段 | ① 威力 2 / 封顶 7 的爆炸对裸装玩家造成 0 < 伤害 ≤ 7；② `COBBLESTONE_6` 规则下圆石不破、泥土破；③ 死因显示 `death.attack.farmstorm.crop_blast` 且击杀归属为部署者 / 投掷者；④ `power=4, cap=2` 时站在中心的玩家只掉 2 点血 |
| F-15 | 事件总线与玩家事件 | 建立 game 总线事件类，承载玩家 tick、登录 / 登出与爆炸事件的统一入口 | ① `PlayerTickEvent.Post` 每 20 tick 打印一次心跳日志（可开关）；② `PlayerLoggedInEvent` / `PlayerLoggedOutEvent` 各打印一行含玩家名与 UUID 的日志；③ 事件类不注册任何方块实体 tick |
| F-16 | 爆炸批处理队列 | 用内部常量 `MAX_EXPLOSIONS_PER_TICK = 8` 把同 tick 超量爆炸推迟到下一 tick | ① 同 tick 抛出 12 次爆炸：前 8 次立即执行、后 4 次下一 tick 执行，日志打印 `deferred=4`；② 破坏方块总数与逐个执行一致；③ `/debug` 观察单 tick 时间 < 5 ms |
| F-17 | 爆炸免疫与 Detonate 拦截 | 让火把花 / 瓶子草对任何爆炸免疫（含原版 TNT 与其它模组） | ① TNT / 苦力怕 / 末地水晶各炸一次，两个方块仍在；② 同一位置 `blast_carrot` 被炸掉；③ `ExplosionEvent.Detonate` 日志打印从 `getAffectedBlocks()` 移除的数量 > 0；④ `getExplosionResistance` 返回 3600000F 与 tag 形成双保险 |
| F-18 | 临时附魔账本 | 实现 `TempEnchantHandler` 的授予 / 撤销 / 过期，并在撤销时不覆盖玩家真实附魔 | ① 给木剑授予火焰附加 I，工具提示出现 `tooltip.farmstorm.temporary_enchant` 行；② 到期后附魔消失、组件被移除（`/data get entity @s SelectedItem` 无该键）；③ 原等级 0 恢复为无附魔、原等级 2 不被降级；④ 剑放进箱子取出后下一次 tick 被清理 |

### 4.4 本阶段出口判据

- [ ] 能构造一次 `FarmstormExplosion` 并观察到「按规则破坏 + 逐实体封顶伤害 + 死因正确」三件事同时成立（F-14 四条断言全过）。
- [ ] 同 tick 12 次爆炸被摊到 **2 个 tick**，日志出现 `deferred=4`。
- [ ] 火把花在 TNT / 苦力怕 / 末地水晶下均存活，且同一位置的 `blast_carrot` 被炸掉。
- [ ] 临时附魔能授予、能到期撤销、能把「剑进箱子」这类旁路场景清理干净，且从不降级玩家真实附魔。
- [ ] 全仓无自定义网络包（`ClientboundCustomPayloadPacket` 与自建 S2C 类 `grep` 命中 0，HC-03 / C-6）。
- [ ] `./gradlew build` 绿；`runServer` 日志无 `ERROR`；`git status` 仅限 `explosion/`、`event/`、`util/`、`block/`、`data/farmstorm/tags/block/` 与主类。

### 4.5 常见翻车点

1. **伤害封顶写成「总伤害封顶」**：必须是**逐实体**封顶（每名玩家独立算），否则一炸多人时第一个人吃掉全部额度、后面的人零伤害；同时别顺手写自定义 S2C 包发粒子音效（C-6 要求用原版包）。
2. **爆炸免疫只靠 `getExplosionResistance`**：其它模组与原版路径仍可能移除方块，必须在 `ExplosionEvent.Detonate` 里从 `getAffectedBlocks()` 移除，两道同时上（RT-08）。
3. **F-18 把记录写进 playerdata**：违反 D-41，且会让 RT-03 的残留与降级漏洞无法收口——记录只能挂在物品的 `farmstorm:temporary_enchantments` 组件上。

### 4.6 完成后应向用户回报

- 一条三段式爆炸的日志片段（破坏数 / 每名玩家伤害 / 死因行）。
- `deferred=4` 那一行日志原文。
- `Detonate` 的移除数量与三种爆炸源的存活结论。
- 临时附魔「授予 → 到期撤销 → 原等级 2 不被降级 → 箱子旁路清理」四步的证据。
- 未决问题（尤其是 `Explosion#finalizeExplosion` 在 21.1.213 下的实际签名）。

---

## 5. Phase 4 · 部署行为（F-19 ~ F-25）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 5.1 投喂前必须已就绪

- Phase 2 的 F-09（部署方块）与 F-10（部署物 BlockEntity）已完成：5 个方块能放下、5 个 BlockEntityType 能读档。
- Phase 3 的 F-14（爆炸）与 F-18（临时附魔）已完成：胡罗贝要用爆炸，火把花要用附魔账本。
- F-19 是本阶段其余 6 个任务的唯一前置——先交框架，再交 5 个行为，最后交限流。

### 5.2 本阶段目标

把 5 个部署物从「能放下的方块」变成「会做事的装置」：行为接口 + 5 个实现 + 限流。

### 5.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-19 | 行为家族与 tick 分发 | 定义 `DeployedBehavior` / `TrapBehavior` / `TurretBehavior` / `AuraProvider` 与工厂，并把 tick 分发到行为对象 | ① 5 种部署物各放 1 个，跑 600 tick 无异常；② 同区块 8 个部署物因 `stagger` 分摊到不同 tick（日志打印每 tick 触发数 ≤ 2）；③ 冷却窗口内不做扫描（炮台 60 tick 只出现 1 次索敌日志）；④ 客户端不 tick |
| F-20 | 胡罗贝陷阱 | 部署后对切比雪夫 ≤ 1 的非己方玩家自爆，威力 2 / 封顶 7 / 反胃 III | ① 敌方玩家进入切比雪夫 1 触发；② 每名玩家伤害 ≤ 7；③ 爆炸半径 4 内**所有**玩家（含队友）获反胃 III 60 tick（D-27）；④ 触发后方块消失（`/execute if block` 返回 0） |
| F-21 | 仙人掌尖牙陷阱 | 欧氏 ≤ 3 触发后分两波生成 8 + 5 个尖牙并延迟出现 | ① 欧氏距离 ≤ 3 触发（4 格不触发）；② 外圈 8 个 `minecraft:evoker_fangs` 在 10 tick 后出现、内圈 5 个在 4 tick 后出现（F3+B 数 hitbox 与顺序）；③ 外圈半径 2.0、内圈半径 1.0；④ 两波出完方块消失 |
| F-22 | man瓜炮台与导弹 | 实现切比雪夫 5 格索敌、60 tick 开火与自定义导弹的性能 | ① 非己方玩家进入范围后按 60 tick 间隔开火；② 命中造成 1 点 `farmstorm:void_bolt` 伤害；③ 击退方向为「南瓜 → 目标」连线且不施加漂浮；④ 撞方块即消失、不破坏方块；⑤ 己方玩家不成为目标；⑥ 配置开启拦截时对非己方投掷物也开火（C-3） |
| F-23 | 火把花光环 | 半径 20 内己方玩家每 100 tick 获得 6 个增益，并对持剑者授予临时火焰附加 | ① 6 个效果数值与 300 tick 时长正确（力量 III / 抗性 II / 急迫 II / 速度 I / 水下呼吸 I / 夜视 I）；② 手持任意 `#minecraft:swords` 获得临时火焰附加 I；③ 每次施加出现 8 个 flame 粒子；④ 非己方玩家与创造模式玩家都挖不动（进度恒 0）；⑤ 离开半径后 100 tick 内撤销临时附魔 |
| F-24 | 瓶子草光环 | 半径 10 内己方玩家每 100 tick 获得 3 个增益并净化全部负面效果 | ① 生命回复 II / 饱和 II / 伤害吸收 I 各 300 tick；② 身上 `MobEffectCategory.HARMFUL` 的效果全部被移除（用 `/effect give @s poison` 验证）；③ 8 个 heart 粒子；④ 非己方玩家挖不动 |
| F-25 | 部署限流与防刷 | 用 3 个内部常量封住放置刷取与单区块堆积 | ① 同一玩家第 33 个部署物被拒绝且不消耗物品；② 同区块第 9 个被拒绝；③ 「放 → 立刻拆 → 再放」在 5 tick 冷却内被拒绝；④ 被拒绝时不产生任何掉落 |

### 5.4 本阶段出口判据

- [ ] 胡罗贝能自爆：切比雪夫 1 触发、伤害封顶 7、反胃 III 覆盖半径 4 内所有玩家、触发后原地消失。
- [ ] 仙人掌能出两圈尖牙：8 + 5 两个波次、延迟 10 / 4 tick、半径 2.0 / 1.0。
- [ ] man瓜能开火：切比雪夫 5 索敌、60 tick 间隔、1 点 `void_bolt`、击退按连线方向、己方不被打。
- [ ] 两种光环能上 effect：6 个 / 3 个效果数值与 300 tick 时长正确，粒子数 8，离场撤销临时附魔。
- [ ] 单区块与单玩家上限生效，被拒绝的放置不消耗物品也不留掉落（HC-13：这 6 个内部常量不得为了测试调大）。
- [ ] 火把花 / 瓶子草的挖掘口径正确：己方原版速度、非己方与创造模式恒 0、任何爆炸都破坏不了（C-8 / D-37）。
- [ ] `./gradlew build` 绿；`git status` 仅限 `behavior/`、`blockentity/`、`entity/`、`block/` 与 `config/`（本阶段新增的专属键）。

### 5.5 常见翻车点

1. **己方判定读的是放置时的队伍快照**：C-9 要求触发瞬间**实时解析**归属者当前队伍；写成放置时快照会让「组队前部署、组队后触发」全错。归属者离线时按最后已知队伍在 Scoreboard 成员表反查。
2. **把 10 秒硬挖套到火把花 / 瓶子草上**：只有 `pumpkin_turret` 固定 10 秒；火把花 / 瓶子草己方按原版速度、非己方进度恒 0（C-8 / D-37）。另外光环别每个 tick 各自 `getEntitiesOfClass` 拉全量——同一 tick 对同一玩家只接受一次施加（RT-06）。

### 5.6 完成后应向用户回报

- 5 个行为各自的触发日志（谁触发的、触发了几次、施加了什么效果）。
- 限流的三个计数证据（第 33 个 / 第 9 个 / 5 tick 内重放）。
- 一次「组队前部署 → 组队后触发」的实测结论（证明是实时解析）。
- 挖掘口径三连：己方速度、非己方恒 0、TNT 炸不掉。
- 未决问题（尤其是 `evoker_fangs` 的生成时序与 `AuraProvider` 的去重口径）。

---

## 6. Phase 5 · 投掷物（F-26 ~ F-33）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 6.1 投喂前必须已就绪

- Phase 2 的 F-07（`ThrownCropEntity` + 9 个实体类型）与 F-08（8 个投掷型物品）已完成。
- Phase 3 的 F-14（三段式爆炸与封顶）已完成：本阶段 5 个投掷物都要用它。
- F-27 必须先于 F-29（瓜！复用 `MelonSliceProjectile`）。

### 6.2 本阶段目标

把 8 种投掷型作物的命中效果逐个实现，并统一生命周期、命中分流与创造模式口径。

### 6.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-26 | 膨胀麦 | 落点 3×3×3 替换为干草块，遇水扩到 5×5×5 | ① 干燥落点替换 27 格为 `minecraft:hay_block`；② 范围内放一格水后替换 125 格；③ 基岩 / 屏障 / 命令方块不被替换（T-05 / D-38）；④ 空气方块会被替换 |
| F-27 | 育碧服务器与瓜片子 | 实现两个破障 / 伤害型投掷物及其不同的抗性阈值 | ① 育碧服务器威力 4、每名玩家伤害 ≤ 2、可炸掉石头；② 瓜片子威力 2、伤害 ≤ 6；③ 圆石（6.0）不破、泥土破（D-19 / C-2） |
| F-28 | 毒育碧服务器 | 爆炸后原地生成一片剧毒药水云 | ① 先爆炸（威力 2 / 伤害 ≤ 2）；② 再生成半径 3.0、600 tick 的 `AreaEffectCloud`；③ 云内玩家获得中毒；④ 60 秒后云消散 |
| F-29 | 瓜！子母弹 | 落点上方一格生成 4 片西瓜片，随后立刻产生威力 3 的爆炸 | ① 在 `pos + (Δx, +1.0, Δz)`、Δ ∈ [-0.3, 0.3] 内生成 4 个 `melon_slice_bomb`；② `/data get entity` 读到 `submunition=true`；③ 立刻产生威力 3 / 伤害 ≤ 8 的爆炸；④ 4 片子母弹落地各自再炸一次，总爆炸次数 = 5 |
| F-30 | 爆竹 | 命中后存活 140 tick，每 5 tick 生成并引爆一个风弹 | ① 命中后不消失，`noPhysics=true`、速度为 0；② 每 5 tick 生成 `minecraft:wind_charge` 并立即引爆，共 28 次；③ 140 tick 后消失；④ 风弹不破坏方块 |
| F-31 | 红菇 / 棕菇投掷物 | 实现两个 AoE buff 类投掷物，范围效果不筛敌我 | ① 红菇：半径 4 内所有玩家（含队友）获反胃 V 60 tick + 失明 I 60 tick；② 棕菇：虚弱 II 200 tick + 缓慢 II 100 tick；③ 两者都不破坏方块、不造成伤害 |
| F-32 | 生命周期与命中分流 | 统一投掷物的存活上限与命中后行为，把公共部分锁进基类 | ① 存活超过 `throwable.lifetimeTicks`（默认 100）自动 `discard`；② `onHitBlock` / `onHitEntity` 只在基类实现（`grep -c "onHitBlock" entity/thrown/` 结果为 0）；③ 命中后立即消失（爆竹经 `keepAliveAfterImpact()` 例外） |
| F-33 | 创造模式消耗口径 | 统一「创造模式不消耗物品、不被陷阱锁定」的口径 | ① 创造模式投掷后物品数量不变；② 创造模式部署后物品数量不变；③ 创造模式玩家站在陷阱旁不触发；④ 创造模式挖掘 `pumpkin_turret` 仍需 10 秒 |

### 6.4 本阶段出口判据

- [ ] 8 种投掷物全部可投出，落点效果与 `plan0.0.1.txt` 描述逐条对得上（`plan0.0.1.txt` 第 33~156 行是唯一原文）。
- [ ] 每条伤害上限都成立：育碧 ≤ 2、瓜片子 ≤ 6、瓜！≤ 8、胡罗贝 ≤ 7。
- [ ] 抗性阈值统一为「≥ 圆石 6.0 不破」（D-19 / C-2），且阈值从 `explosion.sliceResistThreshold` / `explosion.grenadeResistThreshold` 读，不硬编码 `6.0F`（RT-09）。
- [ ] 命中分流只在基类：`entity/thrown/` 下没有子类自己实现 `onHitBlock` / `onHitEntity`。
- [ ] 创造模式投掷 / 部署都不消耗物品，且创造模式玩家不被陷阱锁定。
- [ ] 投掷物不会因为「忘了 discard」而永久堆积：100 tick 上限生效。
- [ ] `./gradlew build` 绿；`git status` 仅限 `entity/thrown/`、`entity/ThrownCropEntity.java`、`item/`、`behavior/impl/BlastCarrotTrap.java` 与 `config/`。

### 6.5 常见翻车点

1. **瓜片子的阈值照抄原文的「≥ -1」**：那是笔误，正确口径是「≥ 圆石 6.0」（C-2 / D-19），并且要和瓜！共用同一档阈值。
2. **命中逻辑散落到各个子类**：F-32 的验收是反向证据——`grep` 结果为 0。子类只写「命中了要做什么」，不写「命中了要不要消失」。
3. **爆竹当成普通投掷物**：它命中后必须存活 140 tick、`noPhysics=true`、每 5 tick 生成一个原版 `wind_charge`，总计 28 次；用错实体或自己造爆炸都会让「不破坏方块」这条失效。

### 6.6 完成后应向用户回报

- 8 种投掷物各一条落点效果的证据（方块变化 / 伤害数字 / 效果列表）。
- 4 条伤害上限的实测数字。
- `grep -c "onHitBlock" entity/thrown/` 的结果行。
- 创造模式投掷与部署的物品数量前后对比。
- 未决问题（尤其是 `AreaEffectCloud` 与原版滞留药水的参数差异）。

---

## 7. Phase 6 · 转换器（F-34 ~ F-39）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 7.1 投喂前必须已就绪

- Phase 2 的 F-05（配置 `[converter]` 段）与 F-08（`farmstorm:converter` 物品）已完成。
- 15 个作物与 6 个播种物的物品 ID 已确定（DEC §4）——配方要按这些 ID 写。
- 转换器是整个项目唯一有 GUI 的方块；客户端类只允许出现在 `client/` 包（HC-03 / 05 §4.7、N-12）。

### 7.2 本阶段目标

做出「原版 ↔ 改良」的完整闭环方块：配方类型、容器、菜单、屏与事务化转换。本阶段结束时 64 个注册对象应全部入册。

### 7.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-34 | 配方类型与序列化器 | 定义 `ConverterRecipe` 与序列化器并注册 1 个 RecipeType + 1 个 Serializer | ① `farmstorm:converting` 的 RecipeType 与 Serializer 各 1 个；② 放入一条 `data/farmstorm/recipe/puffed_wheat.json` 后 `runServer` 无 `Parsing error`；③ `reverse()` 返回 input / result 互换的视图且不落盘 |
| F-35 | 转换器容器与存档 | 实现单槽位容器 BlockEntity 与内容物存档、破坏掉落 | ① 槽位放入 / 取出 / `stillValid` 正确；② 退出重进后槽内物品仍在；③ 破坏方块时内容物掉落、不吞物品 |
| F-36 | 转换器菜单 | 实现 1 输入 + 1 只读预览 + 3 个 `ContainerData` 的菜单并注册菜单类型 | ① `farmstorm:converter` 菜单类型注册成功；② `clickMenuButton(player, 0)` 只接受 0；③ `ContainerData` 的 `xpCost` / `canConvert` / `reverse` 随输入物品变化；④ `quickMoveStack` 只处理输入槽与玩家背包 |
| F-37 | 转换器界面与客户端注册 | 实现 GUI 背景 / 标题 / 按钮，并用客户端事件注册屏 | ① `runClient` 打开转换器显示 176×166 背景与「农产品转换器」标题；② 转换按钮点击发出 `ServerboundContainerButtonClickPacket`（日志可见，无自定义包）；③ 关闭重开 10 次不崩 |
| F-38 | 转换器方块与破坏掉落 | 注册转换器方块，接上菜单提供者与破坏掉物 | ① `farmstorm:converter` 方块 ID 注册成功（blocks 第 20 个）；② 含 4 向 `facing`、`getRenderShape` 为 `MODEL`；③ 挖掘后掉方块本体 + 内容物；④ `#minecraft:mineable/pickaxe` 生效（镐快于空手） |
| F-39 | 转换事务 | 按「先全量校验再动状态」实现 `tryConvert`，覆盖经验、催化剂与双向 | ① 正反双向 1:1 转换成功；② 经验不足被拒绝且**不扣等级**；③ 火把花 / 瓶子草仅正向、扣 1 个下界之星、反向免费；④ 校验失败时连点 10 次物品与经验均不变 |

### 7.4 本阶段出口判据

- [ ] 放入小麦点一次转换得到膨胀麦，经验扣 **3 点**（不是扣等级，D-12）。
- [ ] 反向可转回，1:1 双向可逆。
- [ ] 经验不足被拒绝且**绝不扣等级**；校验失败连点 10 次物品与经验均不变。
- [ ] 火把花 / 瓶子草仅「原版 → 激活」方向，每次额外消耗 1 个下界之星，反向免费。
- [ ] 转换器的 5 个注册对象（`recipe_type` / `recipe_serializer` / `menu_type` / `converter` 方块 / `converter` block_entity_type）全部入册——此时 64 / 64 齐。
- [ ] GUI 按钮走原版 `ServerboundContainerButtonClickPacket`，全仓仍 0 个自定义网络包。
- [ ] `./gradlew build` 绿；`runClient` 能开界面且不崩；`git status` 仅限 `recipe/`、`blockentity/`、`menu/`、`client/`、`block/`、`registry/`、`config/`。

### 7.5 常见翻车点

1. **事务顺序写成「先扣材料再校验」**：这是刷物品漏洞的唯一来源。`tryConvert` 必须「先全量校验（输入可转 / 经验够 / 催化剂够 / 输出可容纳）→ 再动状态」，任何一步提前扣减都算缺陷（`04-ROADMAP.md` 已点名）。
2. **把经验当等级扣**：D-12 是每次转换扣 **3 点经验值**，不足则拒绝，**绝不扣等级**——写成 `addExperienceLevels(-1)` 就直接违反裁决。
3. **忘了 `/reload` 后的缓存**：本阶段建缓存，Phase 8 的 F-48 才做完整回归；但缓存必须从第一天就带 `RecipeManager#byKey` 校验（RT-07），否则 Phase 8 要返工。

### 7.6 完成后应向用户回报

- 一次正向转换与一次反向转换的经验前后值、物品前后值。
- 经验不足时的拒绝日志（含「不扣等级」的证据：等级与经验前后一致）。
- 火把花正向的催化剂消耗证据（扣 1 个下界之星）、反向免费的证据。
- 连点 10 次的空转结论。
- 64 / 64 的点数结论。
- 未决问题（尤其是 `RecipeSerializer#streamCodec` 的实际签名）。

## 8. Phase 7 · 食用型与幸运浆果（F-40 ~ F-43）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 8.1 投喂前必须已就绪

- Phase 3 的 F-14（爆炸）与 F-15（玩家事件）已完成；Phase 6 的 F-05 配置段已能热重载。
- Phase 2 的 F-08 已交付 `power_beet` 与 `lucky_berry` 两个物品（本阶段只补行为，不补注册）。
- `config` 里的 8 个概率键尚未落——本阶段 F-41 负责把它们补进 117 键的总账里。

### 8.2 本阶段目标

完成两个食用型作物，重点是 **8 档概率表**与**创造模式分支的三级兜底**——这两处是全项目唯一会动玩家游戏模式的地方。

### 8.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-40 | 甜菜！食用效果 | 食用后一次性施加 4 个 600 tick 增益 | ① 食用 32 tick 后获得力量 II / 速度 II / 急迫 II / 抗性 I 各 600 tick；② 营养 4、饱和度 0.3（`/data get` 读到 `food` 组件）；③ 饱食度满时仍可食用 |
| F-41 | 概率表与抽取 | 实现千分比权重表与单次 `nextInt(1000)` 抽取，并在重载时重建 | ① 内部权重和恒为 1000（启动日志断言）；② 把 8 项配置分别调成 100 / 0 / … 各跑 100 次抽取，命中率 100%；③ 配置和 ≠ 100 时 `bake()` 拒绝该段、回退默认值并打印 warn |
| F-42 | 幸运浆果 6 个常规分支 | 实现闪电、随机作物、回复、宝箱、小爆炸、闪电苦力怕 6 个结果 | ① 闪电在玩家位置生成且无来源伤害；② 生成 16 个随机作物（除自身外 14 种，D-17），落点在半径 2 格可站立方块内（D-33）；③ 回复 V 100 tick；④ 2 个 `end_city_treasure` 宝箱；⑤ 威力 1 且不封顶的小爆炸；⑥ 苦力怕 `powered=true` 且未点燃 |
| F-43 | 创造模式分支与三级兜底 | 实现 0.1% 分支的 90 秒回滚、下线即还原与登录对账 | ① 抽中后切创造，1800 tick 后自动还原；② 中途下线立即还原且不写存档；③ `kill -9` 服务端后重连，非 OP 玩家被还原为生存、OP 玩家不被改动（RT-05）；④ 本就是创造模式的玩家抽中该分支只播音效、不改模式 |

### 8.4 本阶段出口判据

- [ ] 甜菜！食用后 4 个增益各 600 tick，营养 4 / 饱和度 0.3，饱食度满仍可食用。
- [ ] 幸运浆果 8 档**全部可被强制触发**（逐档把概率调成 100 抽 100 次，命中率 100%）。
- [ ] 内部权重和恒为 1000；配置和 ≠ 100 时 `bake()` 拒绝并回退默认 + 打 warn，不静默吞掉。
- [ ] 创造模式分支 1800 tick 还原、下线立即还原、`kill -9` 后重连能被对账救回；且**从不写存档**（RT-05）。
- [ ] 全项目仍 0 个自定义网络包；音效与粒子走原版包（C-6）。
- [ ] `./gradlew build` 绿；`git status` 仅限 `util/`、`item/`、`event/`、`config/`。

### 8.5 常见翻车点

1. **创造模式状态落进 playerdata**：RT-05 是最高严重度的玩法漏洞。记录只放内存 `Map<UUID, CreativeRestore>`，`PlayerLoggedOutEvent` 立刻还原、`PlayerLoggedInEvent` 对账；「误降级」保护只在非 OP 玩家上执行。任何「顺手存进存档更稳」的写法都会让三级兜底失效。
2. **概率用浮点累加或每档各自 `nextDouble()`**：必须是单次 `nextInt(1000)` 命中唯一一档，权重和恒 1000；否则「8 档命中率 100%」这条验收永远过不了。
3. **随机作物池子里算上幸运浆果自己**：D-17 明确池为除自身外的 **14 种**；数目错了会连带 F-42 的「生成 16 个」判据一起错。

### 8.6 完成后应向用户回报

- 甜菜！的 4 个效果与时长、`food` 组件回读行。
- 8 档强制触发的结果表（每档 100 次的命中率）。
- 一条 `bake()` 回退的 warn 日志原文。
- 创造模式分支三连证据：1800 tick 还原 / 下线还原 / `kill -9` 后对账，并注明「未写存档」的检查方式。
- 未决问题（尤其是 `FoodProperties.Builder` 在 21.1.213 下的签名）。

---

## 9. Phase 8 · 数据驱动内容（F-44 ~ F-48）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 9.1 投喂前必须已就绪

- Phase 6 的 F-34（`ConverterRecipe`）已完成：没有配方类型，21 条 JSON 无处安放。
- Phase 2 的 F-06 / F-09 已完成：战利品表要挂在 14 个作物方块与 5 个部署方块上。
- Phase 8 全部是**数据文件**任务：能用 DataGen 生成的走 Provider，不能生成的（配方、战利品表本体）手写 JSON 也算数据，但**不得**与 DataGen 抢同一个文件（05 §4.7）。

### 9.2 本阶段目标

把配方、战利品表、标签、进度这些纯数据内容补齐，让玩法闭环在数据层成立——本阶段结束时，游戏里已经「能玩」，只剩资源与验收。

### 9.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-44 | 21 条转换配方 | 为 15 个作物与 6 个播种物各写一条转换配方 | ① `runServer` 无配方解析错误；② 15 个原版作物 + 6 个种子在转换器内全部可转；③ 15 条可反向、火把花 / 瓶子草 2 条不可反向；④ `/reload` 后上述结论不变 |
| F-45 | 20 张战利品表 | 为 14 个作物方块、5 个部署方块与 1 个转换器方块写战利品表 | ① 成熟（`age=3`）按原版比例同时掉作物与播种物，未成熟只掉 1 个播种物；② 5 张部署表默认掉自身、被爆炸破坏掉 0 个；③ `/loot give @s loot farmstorm:blocks/puffed_wheat_crop` 逐表可执行无报错 |
| F-46 | 4 个方块 / 物品标签 | 提供部署物集合、改良作物集合、播种物集合与镐挖掘标签 | ① 四个标签条目数为 5 / 15 / 6 / 6；② `/execute if block` 对 5 个部署方块均返回 1（`#farmstorm:deployed_crops`）；③ `#farmstorm:modified_crops` 恰好 15 条、不含播种物 |
| F-47 | 12 条进度 | 按 `01-DESIGN.md §6` 交付 12 条进度，含 1 条隐藏 | ① `/advancement grant @s only farmstorm:root` 等 12 条全部成功；② 逐条触发条件可解锁；③ `lucky_jackpot` 为 `hidden: true`；④ 全部 `frame=task`、`announce_to_chat=false`；⑤ `root` 背景为 `minecraft:textures/block/hay_block_side.png` |
| F-48 | 配方缓存与 reload 回归 | 让 `/reload` 后转换器不再命中过期配方 | ① `/reload` 后连续转换 5 次成功、日志无 `Recipe not found`；② 删掉一条配方 JSON 再 `/reload`，对应物品立即不可转换；③ 恢复 JSON 后再次可转 |

### 9.4 本阶段出口判据

- [ ] 21 条配方可正反转换；火把花 / 瓶子草 2 条不可反向。
- [ ] 20 张战利品表可逐条 `/loot give`，成熟与未成熟掉落符合原版比例。
- [ ] 4 个标签可用，条目数 5 / 15 / 6 / 6。
- [ ] **12 条进度**全部可解锁（含 1 条 `hidden`），路径为 `data/farmstorm/advancement/*.json`（C-7：不是 6 条）。
- [ ] `/reload` 后转换器不命中过期配方，删表即失效、恢复即可用（RT-07 收口）。
- [ ] `./gradlew build` 绿；`git status` 仅限 `data/` 与 `blockentity/`、`event/`（F-48 的缓存校验）。

### 9.5 常见翻车点

1. **进度只写 6 条**：`02-TECH-SPEC.md §6.7` 的旧口径已被 C-7 否决，正确是 **12 条**（24 个 lang 键随之而来，F-52 的 216 键依赖它）。少写 6 条会连带 F-53 的 257 与 F-52 的 216 一起错。
2. **战利品表路径写错**：必须是 `data/farmstorm/loot_table/blocks/*.json`，表 ID 为 `farmstorm:blocks/<block_id>`；路径错了 `/loot give` 会静默失败，验收才发现。
3. **F-48 靠「重启服务器」收口**：缓存必须带 `RecipeManager#byKey` 校验并监听 `RecipesUpdatedEvent` 清空，`/reload` 当场就要正确。

### 9.6 完成后应向用户回报

- 21 / 20 / 4 / 12 四个计数的命令与结果。
- 一条 `/loot give` 与一条 `/advancement grant` 的原文返回。
- `/reload` 前后的转换结论（含「删表即失效」的对照实验）。
- 未决问题（尤其是进度 JSON 的 `requirements` 结构在 1.21.1 下的写法）。

---

## 10. Phase 9 · 资源与 DataGen（F-49 ~ F-54）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 10.1 投喂前必须已就绪

- Phase 4 的 F-22 已完成（F-54 要给导弹写渲染器）。
- Phase 6 的 F-38 已完成（F-49 要生成转换器的 blockstate 与模型）。
- Phase 8 的 F-44 / F-45 / F-46 / F-47 已完成（F-53 要把 7 个 provider 挂上 `GatherDataEvent`）。
- `tools/gen_textures.py` 尚不存在（F-51 建它）。

### 10.2 本阶段目标

把 20 个 blockstate、62 个方块模型、22 个物品模型、87 个材质、216 个语言键与客户端渲染一次性补齐，并**全部由 DataGen / 脚本生成**。

### 10.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-49 | 方块状态与方块模型 | 用 DataGen 生成 20 个 blockstate 与 62 个方块模型 | ① `runData` 生成 `blockstates/*.json` 20 个、`models/block/*.json` 62 个；② 14 个作物各 4 阶段（`age` 0 ~ 3）变体齐全；③ 5 个部署方块的 blockstate 覆盖 `face × facing × floating` 且悬空复用同一模型 |
| F-50 | 物品模型 | 用 DataGen 生成 22 个物品模型 | ① `models/item/*.json` 22 个；② 5 个部署型物品模型 `parent` 指向 `farmstorm:block/<id>`；③ 创造栏 22 个图标均非紫黑方块 |
| F-51 | 材质生成脚本 | 用脚本批量产出 87 个占位 PNG，并给 15 个作物图标加右下角绿色加号 | ① 一次运行产出 `textures/item` 22 张、`textures/block` 64 张、`textures/gui` 1 张；② 15 个作物物品图标右下角有绿色加号（逐张目测 + 尺寸断言 16×16）；③ 重复运行结果幂等（两次运行 md5 一致） |
| F-52 | 语言文件 216 键 | 生成中英两套键集完全一致的语言文件 | ① `zh_cn.json` 与 `en_us.json` 键集合 diff 为空；② 键数 216（22 item + 20 block + 9 entity + 1 itemGroup + 1 container + 8 gui + 10 tooltip + 4 death + 24 advancement + 117 config）；③ 中文名与 `plan0.0.1.txt` 逐字一致（含全角标点）；④ 游戏内无 `item.farmstorm.*` 原样显示 |
| F-53 | DataGen 总装与产物核对 | 把 7 个 provider 挂到 `GatherDataEvent`，并核对产物总数与清单 | ① `./gradlew runData` 一次生成 `src/generated/resources` 全量产物；② 产物总数 257（JSON 169 + TOML 1 + PNG 87），与旧口径 251 相比 +6；③ 无手改 `src/generated` 的痕迹（再次运行 diff 为空） |
| F-54 | 炮台导弹渲染 | 让导弹在客户端可见并复用潜影贝导弹模型 | ① 客户端可见导弹且外形与潜影贝导弹一致；② 朝向随速度变化；③ `runServer` 不加载客户端渲染类（无 `NoClassDefFoundError`） |

### 10.4 本阶段出口判据

- [ ] `./gradlew runData` 一次跑通，产物总数 **257**（JSON 169 + TOML 1 + PNG 87），并且**再跑一次 diff 为空**。
- [ ] 逐类计数与 `02-TECH-SPEC.md §6.1` 一致：blockstates 20 / models-block 62 / models-item 22 / lang 2 / textures-item 22 / textures-block 64 / textures-gui 1 / damage_type 2 / minecraft tags-damage_type 2 / recipe 21 / loot_table-blocks 20 / advancement **12** / farmstorm tags-block 2 / farmstorm tags-item 2 / minecraft tags-block-mineable-pickaxe 1 / 根 2。
- [ ] 语言键 **216**，中英键集 diff 为空；游戏内无原样显示的键。
- [ ] 15 个作物图标右下角有绿色加号；创造栏与世界里都没有紫黑方块。
- [ ] 脚本幂等：连跑两次 md5 一致。
- [ ] `runServer` 不因客户端类崩（无 `NoClassDefFoundError`，HC-03 / 05 §4.7、N-12）。
- [ ] `./gradlew build` 绿；`git status` 仅限 `datagen/`、`client/`、`tools/`、`src/generated/`。

### 10.5 常见翻车点

1. **手改 `src/generated/resources` 里生成的 JSON**：下次 `runData` 会覆盖，F-53 的「再次运行 diff 为空」直接不成立。所有生成物只认 Provider，改就改 Provider。
2. **拿 251 / 204 的旧口径核对**：257 与 216 才是最新裁决（C-7），差额就是那 6 条进度的 JSON 与 12 个文案键。核对脚本要按本节的 15 类逐类点数，不要只认总数。
3. **客户端类泄进服务端路径**：`TurretBulletRenderer` 与 `ConverterScreen` 只能出现在 `client/` 包，并通过客户端事件注册；否则专用服务器起不来（05 §4.7、N-12）。

### 10.6 完成后应向用户回报

- `runData` 的输出行 + 15 类逐类计数 + 合计 257 的核对结论。
- 第二次 `runData` 的 `git diff --stat` 为空。
- 216 键的 diff 为空结论与键数。
- 15 张作物图标的目测结论（或一张拼图）。
- `runServer` 无客户端类加载错误的日志行。
- 未决问题（尤其是 `ParticleProvider` / BlockState 变体在 1.21.1 下的 provider 签名）。

---

## 11. Phase 10 · 验收与发布（F-55 ~ F-59）

> 投喂方式：`05-PROMPT-MAIN.md` 全文 + 本节，一次只喂一个 Phase。

### 11.1 投喂前必须已就绪

- Phase 4、Phase 5、Phase 7、Phase 9 全部通过（本阶段四条验收线各依赖其中之一）。
- 有一台可重启的专用服务器环境（`kill -9` 回归与多人场景需要，`04-ROADMAP.md` 已把它排在 M5 之前）。
- `04-ROADMAP.md §6` 的 DoD-01 ~ DoD-18 已准备好勾选表。

### 11.2 本阶段目标

把 9 条技术风险与 18 条 DoD 逐项跑一遍，产出可发布的 v0.0.1——**本阶段不新增功能代码**，只允许为验收缺陷回补。

### 11.3 任务清单

| ID | 标题 | 一句话做什么 | 可观察验收判据 |
|---|---|---|---|
| F-55 | 专用服务器与三模式回归 | 验证服务端权威与硬挖进度的稳定性 | ① `runServer` 独立启动 `Done` ≤ 120 s；② 两个客户端连入后陷阱 / 炮台 / 光环均生效；③ 创造 / 生存 / 旁观各挖一次 `pumpkin_turret`，进度条 10 秒不回弹（RT-02）；④ `/tp` 远处再回来触发区块重载后仍稳定 |
| F-56 | 数值对照验收 | 逐项核对 117 个配置键与规格一致 | ① 导出 toml 的 117 键与 `02-TECH-SPEC.md §4.11` 逐项比对，差异 0；② 随机抽 5 个键改值后行为随之变化；③ 8 项概率和恒为 100 的校验在改错时被拒绝并回退 |
| F-57 | 性能验收 | 验证光环叠加与部署密度的最坏情况 | ① 10 个光环 + 8 名玩家场景 `/debug profile` 单 tick < 1 ms（RT-06）；② 单区块 8 个部署物稳态无 tick 尖峰；③ 同 tick 12 次投掷命中无卡顿（批处理生效） |
| F-58 | 异常路径与崩溃回归 | 把 RT-03 / RT-05 / RT-08 / RT-09 的验证方式各跑一遍 | ① 岩浆 / 箱子 / 死亡掉落 / 下线四个场景后物品组件被清空；② `kill -9` 后重连模式正确；③ TNT / 苦力怕 / 末地水晶各炸一次火把花仍在；④ 抗性 6.0 与 5.99 的方块各一，前者不破后者破 |
| F-59 | 打包与 v0.0.1 发布 | 产出可发布 jar 并完成 DoD 签核 | ① `./gradlew build` 产物名为 `farmstorm-0.0.1+mc1.21.1.jar`；② jar 内 `META-INF/neoforge.mods.toml` 版本解析为 0.0.1；③ 全仓无 mixin 配置与占位标记（`grep` 命中 0）；④ DoD 18 项逐项勾选完毕 |

### 11.4 本阶段出口判据

- [ ] `04-ROADMAP.md` 的 **18 项 DoD** 全部勾选，每项附命令输出或截图位置。
- [ ] 风险登记册中 **RT-02 / RT-03 / RT-05 / RT-08 / RT-09** 各有一次通过记录。
- [ ] 四条验收线各有一份记录：服务器回归（F-55）、数值对照（F-56）、性能（F-57）、异常与崩溃（F-58）。
- [ ] 117 个配置键**逐项**比对差异 0（不是抽查）。
- [ ] `build/libs/farmstorm-0.0.1+mc1.21.1.jar` 产出，jar 内版本解析为 0.0.1。
- [ ] 全仓 `grep` 命中 0：无 mixin 配置、无 `TODO(api-unverified)` 以外的占位标记、无被注释的代码、无 `System.out.println`（05 §6 第 4 条、N-11）。
- [ ] `git status` 干净；`03-TASKS.md` 的 Phase 编号 / 任务数 / 工作量三处数字互相吻合（DoD-18）。

### 11.5 常见翻车点

1. **用「看着正常」当证据**：`03-TASKS.md §0.3` 禁止「表现正常」「手感良好」这类表述。每条验收必须落到日志行、`/execute` / `/data get` 返回、F3 读数或文件内容。
2. **配置对照做成抽查**：F-56 要求 117 键逐项比对，差异 0；抽查 5 个键是第 ② 条「改值行为变化」的做法，不能替代第 ① 条。
3. **`kill -9` 回归只做一半**：RT-05 的对账要同时覆盖「非 OP 玩家被还原为生存」与「OP 玩家不被改动」两侧，只做一侧就漏掉了误降级保护。

### 11.6 完成后应向用户回报

- 18 项 DoD 的勾选表（每项一行：DoD 编号 / 结论 / 证据位置）。
- 5 条风险（RT-02/03/05/08/09）各自的通过记录。
- 四条验收线的记录路径。
- jar 路径与 jar 内版本解析行。
- `grep` 命中 0 的四条清理结论。
- 仍未关闭的未决问题清单（这一项决定能不能署名发布）。

---

## 12. 阶段间的口头交接语（hand-off）

模板（把方括号换成实际内容，一次只发一条）：

~~~text
上一阶段（Phase N · [标题]）已按 06-PROMPT-TASKS.md 的出口判据验收通过：[一句话证据，例如「build 绿、22 个物品齐全、5 个 BlockEntityType 已注册」]。
本阶段开始：Phase N+1 · [标题]。
请严格按 06-PROMPT-TASKS.md「Phase N+1」一节的六项执行：先逐条确认「投喂前必须已就绪」并回报，再动第一行代码；只做该节任务清单里的 F-xx，不要提前实现下一阶段，完成后按 05-PROMPT-MAIN.md §7 回报。
~~~

九条可以直接粘贴的实例：

| 交接 | 可直接粘贴的一句话 |
|---|---|
| P1 → P2 | 上一阶段（Phase 1 · 工程基座）已完成：`build` 产 jar、`runServer` 打印 `Done`、`config/farmstorm-server.toml` 58 键齐全。本阶段开始 Phase 2 · 注册层全量，请按 06-PROMPT-TASKS.md「Phase 2」一节执行，先确认就绪再动代码，完成后按 05 §7 回报。 |
| P2 → P3 | 上一阶段（Phase 2 · 注册层全量）已完成：22 物品 / 19 方块 / 9 实体 / 5 个 BE 类型全部落地、伤害类型可用。本阶段开始 Phase 3 · 爆炸与全局事件，请按 06-PROMPT-TASKS.md「Phase 3」一节执行，完成后按 05 §7 回报。 |
| P3 → P4 | 上一阶段（Phase 3 · 爆炸与全局事件）已完成：三段式爆炸可用、`deferred=4` 生效、临时附魔可授予与撤销。本阶段开始 Phase 4 · 部署行为，请按 06-PROMPT-TASKS.md「Phase 4」一节执行，完成后按 05 §7 回报。 |
| P4 → P5 | 上一阶段（Phase 4 · 部署行为）已完成：5 个部署物行为全部生效、限流三条上限成立。本阶段开始 Phase 5 · 投掷物，请按 06-PROMPT-TASKS.md「Phase 5」一节执行，完成后按 05 §7 回报。 |
| P5 → P6 | 上一阶段（Phase 5 · 投掷物）已完成：8 种投掷物全部可投出、伤害上限与抗性阈值全部对上。本阶段开始 Phase 6 · 转换器，请按 06-PROMPT-TASKS.md「Phase 6」一节执行，完成后按 05 §7 回报。 |
| P6 → P7 | 上一阶段（Phase 6 · 转换器）已完成：双向转换、经验扣点、催化剂规则全部成立、64 / 64 注册对象入册。本阶段开始 Phase 7 · 食用型与幸运浆果，请按 06-PROMPT-TASKS.md「Phase 7」一节执行，完成后按 05 §7 回报。 |
| P7 → P8 | 上一阶段（Phase 7 · 食用型与幸运浆果）已完成：甜菜！4 增益、8 档概率全部可强制触发、创造模式三级兜底成立。本阶段开始 Phase 8 · 数据驱动内容，请按 06-PROMPT-TASKS.md「Phase 8」一节执行，完成后按 05 §7 回报。 |
| P8 → P9 | 上一阶段（Phase 8 · 数据驱动内容）已完成：21 配方 / 20 战利品表 / 4 标签 / 12 进度全部可验、`/reload` 回归通过。本阶段开始 Phase 9 · 资源与 DataGen，请按 06-PROMPT-TASKS.md「Phase 9」一节执行，完成后按 05 §7 回报。 |
| P9 → P10 | 上一阶段（Phase 9 · 资源与 DataGen）已完成：`runData` 一次产出 257 个文件、216 键中英一致、无紫黑方块。本阶段开始 Phase 10 · 验收与发布，请按 06-PROMPT-TASKS.md「Phase 10」一节执行，完成后按 05 §7 回报。 |

---

## 13. 全项目收尾提示词（Phase 10 通过后投喂）

~~~text
项目收尾。请对 farmstorm v0.0.1 做一次完整交付核对，只做核对与修复，不新增任何功能。

1. 构建
   跑 ./gradlew clean build，确认产物为 build/libs/farmstorm-0.0.1+mc1.21.1.jar；
   解包确认 META-INF/neoforge.mods.toml 里版本解析为 0.0.1，modId 为 farmstorm。

2. 资源清点（必须逐类点数，只报总数无效）
   重新跑 ./gradlew runData，然后按下面 16 类逐个计数，合计必须是 257 个（JSON 169 + TOML 1 + PNG 87）：
   blockstates 20 / models/block 62 / models/item 22 / lang 2 / textures/item 22 / textures/block 64 /
   textures/gui 1 / data/farmstorm/damage_type 2 / data/minecraft/tags/damage_type 2 /
   data/farmstorm/recipe 21 / data/farmstorm/loot_table/blocks 20 / data/farmstorm/advancement 12 /
   data/farmstorm/tags/block 2 / data/farmstorm/tags/item 2 /
   data/minecraft/tags/block/mineable/pickaxe 1 / 根目录 pack.mcmeta + META-INF/neoforge.mods.toml 2。
   再跑一次 runData，git diff --stat 必须为空（没有手改生成物的痕迹）。
   另核对语言文件 216 键、zh_cn 与 en_us 键集 diff 为空（不是 204，见 C-7）。

3. 进度验证（12 条，不是 6 条）
   逐条执行 /advancement grant @s only farmstorm:<id>，12 条全部成功；
   ./gradlew runData 产出的 data/farmstorm/advancement/*.json 恰好 12 个；
   其中 1 条 hidden（lucky_jackpot），lang 里 24 个进度文案键齐全；
   游戏内 /advancement 界面 12 条全部显示中文名而非键名。

4. 59 个任务逐条核对完成定义
   打开 docs/zh/03-TASKS.md，按 F-01 ~ F-59 顺序输出一张 59 行的核对表，
   每行四列：任务 ID / 标题 / 是否达成 / 证据位置（命令输出行、文件路径或 /data get 回读）。
   判定依据是 05-PROMPT-MAIN.md §6 的七条完成定义 + 该任务的验收标准，缺一即「未达成」。
   有任何一行「未达成」就停下来汇报，不要宣布可以发布。

5. DoD 签核
   按 docs/zh/04-ROADMAP.md §6 把 DoD-01 ~ DoD-18 逐项勾选，每项附一句证据。
   其中至少这四项单独取证：DoD-03（64 个注册对象全覆盖）、DoD-13（216 键）、
   DoD-14（257 资源 + 15 个绿色加号）、DoD-18（无 mixin、无占位标记，grep 命中 0）。

6. 回报格式
   按 05-PROMPT-MAIN.md §7 输出，另外追加三张小表：资源清点表、12 条进度表、59 行任务核对表。
   不要贴完整日志（每条命令只贴结论行与第一条错误）；任何没跑过或没验证的项一律写「未验证」，
   不得写成「已通过」（05 §8 的 N-13）。
~~~

收尾时的三条判据（人眼复核，不看代理的自我陈述）：

1. 资源清点表 16 行加总 = **257**，且每一行都能指到一个目录。
2. 进度表 **12 行**全绿，隐藏那条真的隐藏。
3. 任务核对表 **59 行**，其中「未达成」为 0；有则回补后重跑本节，不回补不发布。

---

## 14. 本文件与 03-TASKS.md 的一致性（自检记录）

| 检查项 | 方法 | 结论 |
|---|---|---|
| 任务 ID 与标题 | 从 `03-TASKS.md` 表格抽取 `F-xx + 标题`，与本文件逐一比对（脚本比对，非人眼） | 59 / 59 逐字一致，差集为空 |
| Phase 数量 | 数本文件的 `## Phase N · …` 小节 | 10 个（Phase 1 ~ Phase 10） |
| 任务数分布 | 逐 Phase 点数 | 5 / 8 / 5 / 7 / 8 / 6 / 4 / 5 / 6 / 5 = 59 |
| 硬约束引用 | `grep` HC-01 ~ HC-14 | 只引用编号，不复制条文（符合 05 §3 的单一来源） |

维护约定：若 `03-TASKS.md` 修订了某个任务标题，本节要重跑一次比对；本文件不自行改标题，只跟随上游。
