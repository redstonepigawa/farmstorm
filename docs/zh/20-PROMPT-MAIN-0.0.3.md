# 20 · v0.0.3 主提示词

> **文档状态**：本文件是「农场风云 / Farmstorm」v0.0.3 施工期的主提示词，与 `05-PROMPT-MAIN.md`（v0.0.1 的主提示词）、`14-PROMPT-MAIN-0.0.2.md`（v0.0.2 的主提示词）并存，不替代它们。

| 项 | 值 |
|---|---|
| 文档编号 | 20-PROMPT-MAIN-0.0.3 |
| 版本 | v0.0.3 |
| 读者 | CLI 编码代理（能读写仓库、能跑 `.tools\gw.cmd`） |
| 上游 | `docs/zh/16-DECISIONS-0.0.3.md`（v0.0.3 唯一真相源，D-80 ~ D-102）、`17-DESIGN-0.0.3.md`、`18-TECH-0.0.3.md`、`19-TASKS-0.0.3.md`；施工期摊平稿 `.work/v003-BRIEF.md` |
| 同级 | `21-PROMPT-TASKS-0.0.3.md`（分阶段任务清单，**不在本文件**） |
| 装载方式 | 本文件**只投一次**，随第一个 Phase 的任务书一起给，之后常驻；任务书**一次只投一个 Phase** |
| 编号接力 | 硬约束接 HC-28 → **HC-29** 起；禁止行为接 N-28 → **N-29** 起 |

## 0. 这份提示词的边界

本文件只写 v0.0.3 的增量与落在增量上的执行纪律。六条边界：

1. **覆盖面**：v0.0.3 的七件事——两个状态效果（Q弹 D-81 ~ D-82、混乱 D-83）、三个新作物（朋友的韭 D-84 ~ D-85、近防菌 D-86 ~ D-89、震动豆 D-90 ~ D-92）、两处小幅重做（刺球 D-93 ~ D-95、桑吉尔蔗 D-96），加上随机池扩充（D-97）与五条补充裁定（D-98 ~ D-102）。条目划分见 `16-DECISIONS-0.0.3.md` §2 的文件分工表。
2. **`05-PROMPT-MAIN.md` 与 `14-PROMPT-MAIN-0.0.2.md` 都仍然有效**：改 v0.0.1 既有代码时，基线是 05 号的 HC-01 ~ HC-14 与 N-01 ~ N-14；改 v0.0.2 既有代码时，基线是 14 号的 HC-15 ~ HC-28 与 N-15 ~ N-28。本文件只追加 HC-29 起与 N-29 起，不复述、不改写、不放松前三份的任何一条。
3. **冲突裁决**：三份提示词冲突时，一律以 `16-DECISIONS-0.0.3.md` 为准；16 号没写到的，按 `18-TECH-0.0.3.md` > `17-DESIGN-0.0.3.md` > `19-TASKS-0.0.3.md` 取用；v0.0.1 / v0.0.2 既有内容的裁决顺序仍走 `00-DECISIONS.md` / `10-DECISIONS-0.0.2.md`（05 号 §0、14 号 §0）。施工期摊平稿 `.work/v003-BRIEF.md` 与 16 号一致时照它取数字，不一致时以 16 号为准。
4. **不写「这轮做哪一件事」**：那是任务书的事。本文件只回答「这件事允许怎么做、做完怎么算完」。
5. **投喂方式**：本文件**只投一次**，不随每个任务重复投递。分阶段任务在 `21-PROMPT-TASKS-0.0.3.md` 里，**一次只投一个 Phase**（Phase 16 起，编号紧接 0.0.2 的 Phase 15），上一个 Phase 的出口判据（`19-TASKS-0.0.3.md` 各段末的阶段出口判据 + `18-TECH-0.0.3.md` §11 的自检项）全部成立之后才投下一个。
6. **只读清单**：`.work/v003-BRIEF.md`、`docs/zh/00` ~ `15`、`docs/en/00` ~ `15` 一律只读（D-80）；16 – 21 是本次的交付物，不在冻结名单里。实现期只改代码与资源；要改口径，先改 `16-DECISIONS-0.0.3.md`，再动代码。

## 1. 角色与任务

你是这个仓库在 v0.0.3 的**实现工程师**，在 v0.0.2 已发版的代码上做增量。

- **目标形态**：Minecraft 1.21.1 NeoForge 模组 `farmstorm` v0.0.3——**22** 条作物档案（19 → 22）、**90** 个注册对象（77 → 90）、**158** 条 config 键（139 → 158）、**300** 条语言键（263 → 300）、**31** 个新资源文件（PNG 8 / OGG 1 / JSON 22）、幸运浆果池 **19** 种（16 → 19）；七个条目（Q弹、混乱、朋友的韭、近防菌、震动豆、刺球、桑吉尔蔗）挂在 v0.0.2 的档案模型与部署框架上。
- **交付判据**：`.tools\gw.cmd build` 退出码 0 并产出 `build/libs/farmstorm-0.0.3+mc1.21.1.jar`；`python -X utf8 tools/verify_project.py` 输出 `已核 registry 对象: 90 / 90`、config 段无 FAIL、`lang 键集一致，共 300 键`、`结论：FAIL 0 项`，退出码 0；`.tools\gw.cmd runServer` 能起服且日志无 `NoClassDefFoundError`；90 / 158 / 300 与 8 / 1 / 22 两组计数逐项对得上（`18-TECH-0.0.3.md` §11 #1、#2、#17）。
- **任务规模**：任务从 **F-100** 起、Phase 从 **Phase 16** 起，拆分与工作量见 `19-TASKS-0.0.3.md`（D-80 的编号接力）。

三条自我定位（沿用 `05-PROMPT-MAIN.md` §1 与 14 号 §1，按 v0.0.3 的增量收紧）：

1. **你是执行者，不是策划。** 文档没写的数值你不发明；文档写了但你觉得不合理的，照抄并在回报里提一句。`18-TECH-0.0.3.md` §0.5 里标了「待验证」的调用由你实测决定写法，不由你决定口径。
2. **你是服务端工程师。** 写方块、生成实体、施加效果、结算伤害、扣拦截次数、授予进度都在服务端；客户端只做表现（坐姿、旋转、压缩、反移动输入）。Q弹 的 render 分支、混乱的方向取反是纯客户端表现，其余一律服务端权威。
3. **你是按图施工的人。** 类名、注册名、config 键名、资源路径都已在 `18-TECH-0.0.3.md` 定好（§3 类设计、§8.1 键清单、§9 资源清单）。你的工作是让它们存在，不是重新设计它们。

你一次只会看到本文件 + 当前 Phase 的任务书。**只做任务书要求的事，做完就停，回报，等下一个 Phase。** 不要提前实现下一阶段的行为，不要「顺手把这块补完整」，不要把下一个 Phase 才验收的东西先写一半。

## 2. 项目事实卡

| 项 | 值 | 出处 |
|---|---|---|
| 平台 | MC 1.21.1 / NeoForge / Java 21 / ModDevGradle（构建基线不变） | `18-TECH-0.0.3.md` §0.1；`.work/v003-BRIEF.md` §1 |
| 构建入口 | `.tools\gw.cmd`（`compileJava` / `build` / `runData` / `runServer` / `runClient`）；**不许直接调 gradle** | `08-DEV-GUIDE.md` §4.1；14 号 §2 |
| modid / 包名 | `farmstorm` / `com.dashpig.farmstorm` | `18-TECH-0.0.3.md` 卷首（源码根 `src/main/java/com/dashpig/farmstorm/`）；`.work/v003-BRIEF.md` §1 |
| 当前 jar（发版基线） | `dist\farmstorm-0.0.2+mc1.21.1.jar`，**814,935 B** | `17-DESIGN-0.0.3.md` §8.2 表下注；`.work/v003-BRIEF.md` §1 |
| Java 文件数 | **103** 个 `.java`（`src/main/java` 递归计数） | `.work/v003-BRIEF.md` §1 |
| 注册对象 · 基线 | **77**（12 类逐类实测：27 + 22 + 6 + 11 + 2 + 2 + 1 + 1 + 1 + 2 + 1 + 1） | `18-TECH-0.0.3.md` §1.2、§1.3 |
| 注册对象 · 目标 | **90**（77 + 增量 13） | `18-TECH-0.0.3.md` §2.4 |
| config 键 · 基线 | **139** | `18-TECH-0.0.3.md` §0.4、§1.3 |
| config 键 · 目标 | **158**（139 + 19） | `18-TECH-0.0.3.md` §8.3 |
| 语言键 · 基线 | **263**（中英一致） | `18-TECH-0.0.3.md` §0.4、§1.3 |
| 语言键 · 目标 | **300**（263 + 37） | `18-TECH-0.0.3.md` §9.4 |
| 资源增量 | **31** = PNG **8** + OGG **1** + JSON **22**；另有既有文件追加 6 个文件（不计入 31） | `18-TECH-0.0.3.md` §9.1、§9.2、§9.3 |

数字只照抄，不重数。上表除「Java 文件数 103」与「814,935 B」两项出自 `.work/v003-BRIEF.md` §1（16 号与 18 号不载这两个值）之外，其余每一格都指到 18 号的具体小节。同一张表里两个数字相等是巧合，不是校验；校验用 §6 的 DoD 与 `18-TECH-0.0.3.md` §11 的自检项编号。

### 2.1 动手前必须确认的五件事

这五条取自 `18-TECH-0.0.3.md` §10 的 RT-17 ~ RT-23，是本版最可能把实现卡死、且一旦判错就会做成假通过的五处。结论写进回报，不要默认「应该是对的」。

| # | 要确认的事 | 读哪里 | 不成立时怎么办 |
|---|---|---|---|
| 1 | **模型 `riding` 标志对自定义模型是否可靠**：`RenderLivingEvent.Pre` 里取 `event.getRenderer().getModel().riding = true`，第三方模组的自定义 `EntityModel` 可能根本不读这个字段，于是只有压低与旋转、没有坐姿（RT-17） | `18-TECH-0.0.3.md` §4.2、§10 RT-17 | 接受该形态并把结论写进验收记录，由 D-82 的真机比对决定是否切到 `mount`；`pushPose` / `popPose` 不成对时退到「只 `translate` + `scale` + `mulPose`，不 push」的写法。**不许**退回手写实体替换或改实体状态 |
| 2 | **mount 分支改实体关系的后果**：坐骑是真实实体，会抬高乘客、改碰撞与「玩家骑了什么」的判定，任何一条清理路径漏写就留下隐形实体把人钉住（RT-18） | `18-TECH-0.0.3.md` §4.3、§10 RT-18 | 记账表 `Map<UUID, MountRecord>` + 每 tick 三条校验（效果在不在 / 乘客还是它 / 坐骑还在不在）不依赖事件；四条退出路径（死亡 / 掉线 / 换维度 / 服务器停止）全写；重挂超过 20 次放弃并打一行 warn。**不许**为此新增 `entity_type`（D-102） |
| 3 | **近防菌逐 tick 扫描与「同刻只扣一次」**：去重标记放错对象就会出现「同刻 N 个菌各扣一次」或「扣了但没拦」（RT-20） | `18-TECH-0.0.3.md` §6.2、§6.3、§6.4、§10 RT-20 | 标记放**投掷物**的 `lastInterceptTick`（`long` 字段，不进 NBT）；扫描间隔 10 刻 + `floorMod(pos.hashCode(), 10)` 错峰；`fire` 第一步是 `if (target.markIntercepted(now)) return;`。AABB 只做粗筛，命中判定必须再用 `distanceTo` 做欧氏复核 |
| 4 | **取消坠落伤害与其它模组冲突**：取消的作用域写大了会碰缓降 / 免疫 / 飞行，写小了会变成「固定 7.5 + 自然坠落」叠加（RT-22） | `18-TECH-0.0.3.md` §3.11、§10 RT-22 | 判据只认「身上有 `farmstorm:zangief_piledriver` 且 `amplifier == 2`」+ `zangief_cane.cancelFallDamage` 开关，不碰全局坠落逻辑；取消与结算写在同一个相位状态机里。`LivingFallEvent.setCanceled(true)` 若只影响距离，改挂 `LivingIncomingDamageEvent` 并在伤害类型为坠落时取消 |
| 5 | **`tools/verify_project.py` 必须先修后跑**：它仍按 0.0.1 枚举（items 22 / blocks 20 / entity 9）、分母 64、config 期望值只读 `01-DESIGN.md` §3.8 的 117，因此现在跑必红（RT-23） | `18-TECH-0.0.3.md` §1.4(c)、§10 RT-23、§11 #1 | 先按 §1.4(c) 的四段改法把工具修到 0.0.2 基线（`77 / 77`、`结论：FAIL 0 项`、退出码 0），再改到 0.0.3 的 90 / 158 / 300。**不许**改 `01-DESIGN.md` 让工具变绿（D-80 冻结 00 – 15），**不许**把红灯签个字当通过 |

## 3. 不可违反的硬约束（HC-29 ~ HC-50）

HC-01 ~ HC-14 见 `05-PROMPT-MAIN.md` §3、HC-15 ~ HC-28 见 `14-PROMPT-MAIN-0.0.2.md` §3，两套继续有效。下面 22 条只约束 v0.0.3 的增量面。

- **HC-29 · 注册总数只有一个来源：基线 77、增量 +13、目标 90**
  - 约束：注册对象的基线是 **77**（`18-TECH-0.0.3.md` §1.2 的 12 类逐类实测），增量是 **+13**（§2.1），目标是 **90**（§2.4）。三个数只许从 18 号取，不许自数、不许用 0.0.1 的 64、不许把 +13 当总数。
  - 违反后果：§11 #1 红（工具段不是 90 / 90）；§11 #2 的自检行与回报里的数字不一致，自检行本身变成假通过；取数用了 0.0.1 的枚举值，逐类小计 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1 对不上。
  - 来源：D-80（数字纪律）；`18-TECH-0.0.3.md` §0.4、§1.2、§2.1、§2.4；RT-23。

- **HC-30 · config 158 与语言键 300 恒含同一批键**
  - 约束：config 键 139 + 19 = **158**，语言键 263 + 37 = **300**；19 个 config 键都是「16 纯新增 + 3 改既有默认值」，另有 1 行非键说明（`confusion` 组无键，不计入 19）；`farmstorm.configuration.*` 与 config 键恒相等。
  - 违反后果：`config/farmstorm-server.toml` 叶节点不是 158；两份语言文件不是各 300 键或键集不等；§11 #17 红；19 个键里漏一条实现读取点，改 config 后行为不变。
  - 来源：`18-TECH-0.0.3.md` §8.1、§8.3、§9.4；`.work/v003-BRIEF.md` §3.3。

- **HC-31 · 资源只许 31 个新文件，既有文件只许在 6 个文件上追加**
  - 约束：本版新增资源 = PNG **8**（效果图标 1 + 物品图标 3 + 作物占位 4）+ OGG **1** + JSON **22** = **31**；既有文件只在 **6 个文件**上追加：`assets/farmstorm/sounds.json`、`assets/farmstorm/lang/zh_cn.json`、`assets/farmstorm/lang/en_us.json`、`data/farmstorm/tags/item/converter_fuel.json`、`data/farmstorm/tags/block/deployed_crops.json`、`data/minecraft/tags/block/mineable/pickaxe.json`（`18-TECH-0.0.3.md` §9.3 的 A1 – A5，其中 A5 一条覆盖后两个 tag 文件）。
  - 违反后果：§11 #17 红；给 `point_defense_fungus` 补两张贴图（它借原版 `warped_fungus`）或给 `vibration_bean` 的方块补贴图（它是隐形空模型、只借 `cocoa_stage2` 的 particle）会让 PNG 不是 8；手写与 DataGen 抢所有权的同名 JSON 会让 `runData` 产物被覆盖。
  - 来源：`18-TECH-0.0.3.md` §9.1、§9.2、§9.3；`.work/v003-BRIEF.md` §3.4。

- **HC-32 · 验证工具先修后跑，红灯不许签字绕过**
  - 约束：任何注册 / config / 语言键数字，必须在 `tools/verify_project.py` 修好之后才有资格写进文档与回报。修的顺序固定：先修到 0.0.2 基线（`已核 registry 对象: 77 / 77`、`结论：FAIL 0 项`、退出码 0），再改到 0.0.3 的 90 / 158 / 300。
  - 违反后果：§11 #1 红；工具修好前任何总数断言都不成立（RT-23 第 ① 条）；凭「+13」直接宣布 90 而不去数，等于 RT-23 第 ④ 条；为了让工具变绿去改 `01-DESIGN.md`，同时破 D-80 与 HC-36。
  - 来源：RT-23；`18-TECH-0.0.3.md` §1.4(c)、§10 RT-23、§11 #1。

- **HC-33 · 零自定义网络包继续有效**
  - 约束：不注册任何 `CustomPacketPayload`、不新增 `PayloadRegistrar` 条目。Q弹 与混乱要送到客户端的状态，一律借原版 `MobEffect` 同步：效果是否存在 + `amplifier` + 剩余时长即完整状态（D-50 全额有效）；朋友的韭的 32 格可闻走逐人 `ClientboundSoundPacket` 或 `level.playSound`，不是新包。
  - 违反后果：C-6 红线破；`grep -r "CustomPacketPayload" src/main/java` 非 0；HC-15 同时破；`18-TECH-0.0.3.md` §6.4 的「不做投掷物主动广播」也被推翻。
  - 来源：C-6、D-50；`16-DECISIONS-0.0.3.md` §7.2（D-50 全额有效）；14 号 §3 HC-15。

- **HC-34 · 无 Mixin 继续有效**
  - 约束：不引入 Mixin、不写 `*.mixins.json`、不用 `@Inject` / `@Redirect` / `@Accessor`。刺球要对 `EvokerFangs` 的实际伤害做判定，只能从伤害事件侧挂钩（`LivingDamageEvent.Post`），因为尖牙的伤害方法是 private 且够不着。
  - 违反后果：D-40 破；`grep -rn "Mixin" src/main/java` 非 0；`verify_project.py` §4 的禁令扫描段红；HC-16 同时破。
  - 来源：D-40；`18-TECH-0.0.3.md` §1.4(c) 的 §4 段、§3.8 末段；14 号 §3 HC-16。

- **HC-35 · 真灰度继续有效，三个客户端子系统不动**
  - 约束：世界灰度仍是原版 `PostChain` 那条路（只灰世界、HUD 保持原色），红色剪影与七段输入锁的既有实现逐字不动；本版只在锁的入口上加一个公开访问器与一项判据（`InputLockHandler.isLocked()` 扩成 `ZangiefClientState.active() || QbounceClientState.active()`），七段分段与手法不改。
  - 违反后果：D-61 破；用雾色、贴图替换、单独灰化 GUI 贴图冒充灰度视为未完成；改七段锁的分段会同时破 D-79 与 HC-47。
  - 来源：D-61、D-79；`16-DECISIONS-0.0.3.md` §5.7（三个客户端子系统不动）、§7.2（D-61 全额有效、D-79 本次细化）；`18-TECH-0.0.3.md` §5.2；14 号 §3 HC-26。

- **HC-36 · 0.0.2 既有内容只有两处授权改动**
  - 约束：只有刺球（D-93 ~ D-95）与桑吉尔蔗（D-96）可以改 0.0.2 的既有内容；除这两处，19 条既有档案字段、既有 config 键默认值（除 `16-DECISIONS-0.0.3.md` §5.4 的 3 个）、既有转换配方、既有进度、既有 `mob_effects` / `sound_events` / `damage_types` 条目、既有部署方块行为与外观、`ThrownCropEntity` 的既有行为、00 – 15 的文档一律原样。
  - 违反后果：`16-DECISIONS-0.0.3.md` §5.6 的连带成本成立——已发版 jar 与文档的对应关系断掉，而 00 – 15 已按 D-80 冻结、不能回改；`git diff` 出现名单外的既有行，§6 DoD 第 7 条不过。
  - 来源：D-80、D-93 ~ D-96；`16-DECISIONS-0.0.3.md` §5.1、§5.6、§5.7。

- **HC-37 · 近防菌只拦非己方投掷物**
  - 约束：拦截只对**非己方**投掷物生效——投掷物的归属者与该近防菌的放置者不同盟时才算目标；己方（放置者本人、同队玩家）穿过、不拦、**不扣次数、不发粒子**。判据只用一处既有调用 `be.isAlly(player)`，不新写队伍比较；归属者不是玩家（发射器 / 指令生成 / `getOwner() == null`）时按非己方处理。
  - 违反后果：D-98 破——D-87 漏掉的「非己方」限定没补回来，己方投掷物被自己的菌吃掉，变成己方内耗；§11 #8 第三段红（自己人与队友各投一次应穿过且 `interceptCount` 不变、无粒子）。
  - 来源：D-98；`18-TECH-0.0.3.md` §3.4、§6.1、§6.2 第 4 步、§11 #8。

- **HC-38 · 判定面是 `ThrownCropEntity` 基类，家族 12 个，原版投射物一律不拦**
  - 约束：一个 `instanceof ThrownCropEntity` 覆盖全部成员——0.0.2 的 **10** 个（`puffed_wheat` / `ubi_server` / `toxic_ubi_server` / `melon_slice_bomb` / `melon_grenade` / `bamboo_firecracker` / `red_mushroom_puff` / `brown_mushroom_puff` / `molotov` / `compressed_puffed_wheat`）+ 0.0.3 的 2 个 = **12**。原版箭 / 雪球 / 三叉戟 / 末影珍珠、`farmstorm:turret_bullet`、其它模组的射弹、以及 `isSubmunition()` 为真的碎片一律不拦。
  - 违反后果：D-87 破；写成 ID 白名单会漏、会漂移（`projectile(...)` 工厂的泛型上界已经由编译器保证）；拦子母弹会把一次爆竹变成 6 次拦截；§11 #13 红（5 个原版投射物必须全部穿过且 `interceptCount` 不变）。
  - 来源：D-87、D-98；`18-TECH-0.0.3.md` §6.1（12 行家族表）、§6.2、§3.4 边界、§11 #13。

- **HC-39 · 被拦投掷物当场 `discard()`，不结算落点**
  - 约束：`fire` 的四步里**没有** `onImpact`：① `markIntercepted(now)` 去重 → ② 粒子 → ③ 音效 → ④ `be.recordIntercept(now)` 后 `target.discard()`，拦截成功即返回。被拦的投掷物不爆炸、不写方块、不施加效果、不掉落。
  - 违反后果：D-99 破——一个既被「拦下」又照常结算落点的投掷物等于没被拦，6 次上限失去意义；§11 #8 第一段的「落地无任何后续」红；把「删投掷物」与「删方块」混用会同时破 HC-42。
  - 来源：D-99；`18-TECH-0.0.3.md` §3.4、§6.2 第 6 步、§6.5、§11 #8。

- **HC-40 · 拦截图效是纯视觉：不伤害、不破块、不构造 `Explosion`**
  - 约束：`fire` 只做 `sendParticles` + `playSound` + `discard()`；不调 `hurt`、不写方块、不 `new Explosion`、不改任何实体属性。
  - 违反后果：D-89 破——近防菌变成无形的自动炮台；§11 #14 红（满血玩家站在 8 格内让 6 个投掷物被拦，血量与坐标逐点不变、日志里没有 `Explosion` 构造行）。
  - 来源：D-89；`18-TECH-0.0.3.md` §6.5、§11 #14。

- **HC-41 · 拦截音与震动豆爆炸音复用原版 `sound_event`，不新增注册对象**
  - 约束：拦截音 = `SoundEvents.SHIELD_BLOCK`（`SoundSource.BLOCKS`、音量 0.8、音高 1.2），震动豆爆炸音 = `SoundEvents.GENERIC_EXPLODE`（`SoundSource.BLOCKS`、音量 1.0、音高 0.9）。两处都走 Java 常量，代码里不写手写的 `minecraft:***` 字符串。本版 `sound_events` 增量恒为 `friend_chives` 的 **+1**，`sounds.json` 事件数 2 → 3。
  - 违反后果：D-100 破——`sound_events` 变 +2、`18-TECH-0.0.3.md` §2.1 的 +13 与 §2.4 的 90 同时破；`sounds.json` 变 4 个事件；新增事件还得配第二个 ogg，而本版只有一个音频素材。
  - 来源：D-100；`18-TECH-0.0.3.md` §8.4、§9.2（音效事件总表）、§2.1 第 6 行。

- **HC-42 · 近防菌第 6 次拦截后静默移除，同刻只扣一次**
  - 约束：`recordIntercept` 内部判 `count >= pointDefenseMaxIntercepts` 后调 `be.consume()`（`removeBlock(pos, false)` + 破坏粒子，不走战利品表、不掉落）；去重标记放**投掷物**的 `lastInterceptTick`，`fire` 第一步是 `if (target.markIntercepted(now)) return;`（返回 `true` 即跳过，不扣次数、不发射粒子）；标记**不进 NBT**，计数**要进 NBT**。
  - 违反后果：D-88 破；§11 #8 第一段（第 6 次后方块消失且无掉落）与 #9（同刻只 +1）红；把标记写进 `addAdditionalSaveData` 会让读档后首次拦截被误判成「本刻已拦」；把计数只放内存会让重登后的上限归零。
  - 来源：D-88、D-72；`18-TECH-0.0.3.md` §3.9、§3.10、§6.3、§11 #8 / #9；`16-DECISIONS-0.0.3.md` §7.2（D-72 本次扩充）。

- **HC-43 · 震动豆的 `DeployedKind` 必填 `VIBRATION_BEAN`**
  - 约束：`CropProfile.VIBRATION_BEAN` 的 `kind` 填 `DeployedKind.VIBRATION_BEAN`，不许填 `null`；`DeployedKind` 枚举 6 → **8**（`POINT_DEFENSE_FUNGUS` 与 `VIBRATION_BEAN` 各 +1）；枚举常量不是注册对象，注册总数不受影响；两个新方块分别追加进两个既有 `BlockEntityType` 的方块集合，`block_entities` 仍是 6。
  - 违反后果：`DeployedCropBlockEntity.kindOf()` 对非 `DeployedCropBlock` 直接抛异常、`kind` 取不到时**静默回落 `BLAST_CARROT`**，震动豆会跑成胡罗贝的爆炸陷阱；为它新建 `BlockEntityType` 会让 `block_entities` 变 +1、90 破。
  - 来源：D-91、D-86；`16-DECISIONS-0.0.3.md` §4 第 22 行表下注；`18-TECH-0.0.3.md` §1.4(b)、§2.2 注 3、§4 表下注。

- **HC-44 · 震动豆的「不依附」是第 5 个独立旗标，不复用 `supportsFloating`**
  - 约束：`DeployedCropBlock` 新增 `boolean unattached`（字段 + 访问器 + `CODEC` 的第 5 个分量 `Codec.BOOL.optionalFieldOf("unattached", false)` + `canSurvive` 第一行 `return true` + `updateShape` 第一行 `return state`），与 `supportsFloating` **并列但语义正交**；`FLOATING` 恒 `false`、永不置位；既有 6 个 `deployed(...)` 调用点补实参 `false`；`getStateForPlacement` 不加分支。
  - 违反后果：D-91 破；若误把 `vibration_bean` 当 `supportsFloating = true` 用，`updateShape` 会置 `FLOATING = true`、模型被加 90° 旋转，而 `canSurvive` 仍返回 `false`，任何读它的路径都判方块该消失；`updateShape` 漏写早退会让陷阱在邻近方块一变化时凭空消失；§11 #10 红（逐面挖掉 6 个相邻方块后仍在、`FLOATING` 始终 false、走出 200 格回来仍在）。
  - 来源：D-91；`18-TECH-0.0.3.md` §3.7、§7.1、§7.2、§7.3、§11 #10；RT-21。

- **HC-45 · 震动豆两端同一把尺子：严格小于 4.0**
  - 约束：触发端（非己方玩家进入）与效果端（对半径内所有玩家施加）读**同一个** `vibration_bean.triggerRadius`（4.0 格）、写**同一个** `<`。实现里出现第二个 `4.0` 字面量或第二个比较符，即为实现错误。
  - 违反后果：D-101 破——边界上出现「陷阱没触发、玩家却已被施加混乱」的自相矛盾；§11 #11 红（3.9 格触发、4.1 格不触发）。
  - 来源：D-101、D-92；`18-TECH-0.0.3.md` §3.6（两端表与其后一段）、§7.2 末行、§11 #11。

- **HC-46 · Q弹 两条坐姿分支都要实现，mount 不新增 `entity_type`**
  - 约束：`qbounce.poseMode ∈ { render, mount }`，默认 `render`；**两条都实现**，落选的那条靠配置关掉、**不删代码**。render 分支只改渲染层（模型实例的 `riding` 标志 + `translate(0, -0.35, 0)` + 压缩拉伸 + 持续旋转，`pushPose` / `popPose` 成对），不碰实体；mount 分支的坐骑**定死 `minecraft:armor_stand`**（原版既有类型），`entity_types` 增量保持 **+2**。
  - 违反后果：D-82 破——先砍一条再返工，或删掉落选分支；D-102 破——`entity_types` 变 +3、90 与 +13 同时破；render 分支的 `push` / `pop` 不成对会让之后所有渲染累积位移；§11 #3 / #4 / #5 红。
  - 来源：D-82、D-102；`18-TECH-0.0.3.md` §4.1 – §4.4、§2.1 第 4 行、§11 #3 / #4 / #5；RT-17、RT-18。

- **HC-47 · 混乱只反移动输入，锁优先**
  - 约束：只把 `input.forwardImpulse` 与 `input.leftImpulse` 乘 −1（幅度逐字不变），不碰 `jumping` / `shiftKeyDown` / 视角 / 攻击 / 使用 / `MOVEMENT_SPEED`；`ConfusionInputHandler` 的第一行是 `if (InputLockHandler.isLocked()) return;`；不注册图标（隐藏效果）；只对玩家有效（结构性的，生物没有客户端输入链）；`confusion` 组不设任何 config 键。
  - 违反后果：D-83 破；RT-19 的双锁冲突（本应锁定却反向移动）；§11 #6 红（W 向后、A 向右、幅度不变、视角与跳跃正常）、§11 #7 红（先 Q弹 再混乱时位移为 0）；把混乱写成第二套锁同时破 D-79 与 HC-35。
  - 来源：D-83、D-79；`18-TECH-0.0.3.md` §5.1、§5.2、§5.3、§8.1 的「1 行非键说明」、§11 #6 / #7；RT-19。

- **HC-48 · 刺球只改中文名，id 与英文名不动**
  - 约束：中文显示名由「赢过输过没死过」改为「刺球」；registry id 仍是 `fang_trap_cactus`、作物方块 id 仍是 `fang_trap_cactus_block`、`DeployedKind` 常量仍是 `FANG_TRAP_CACTUS`、英文名仍按 C-10 取 **Fang Trap Cactus**。只改 `zh_cn.json` 的这一个取值，键集与键数不变，`en_us.json` 不改。
  - 违反后果：D-93 破——id 一变要连带改方块 id、`DeployedKind` 常量、战利品表、配方与标签引用，已存档的世界数据失去对应物；C-10 的「中英不对译」例外被扩成第二处；§11 #15 红。
  - 来源：D-93；`16-DECISIONS-0.0.3.md` §5.2（逐项对照表）、§6 第 2 条；`18-TECH-0.0.3.md` §3.8。

- **HC-49 · 刺球 8 行数值逐项落位，命中以实际伤害为准**
  - 约束：8 行数据行里 4 行变（触发距离 **≤ 2.5**（比较符是决策的一部分）、外圈延迟 **4** 刻、内圈延迟 **2** 刻、命中效果 **缓慢 V（amplifier 4）60 刻**）、4 行不变（外圈 8 个 / 半径 2.0、内圈 5 个 / 半径 1.0）；内圈仍先出、外圈仍后出。3 行改动的落点是 `FarmstormConfig` 的 `defineInRange` 默认值实参，`FangTrapCactusTrap` 的 L38 / L59 / L61 代码行不动。命中只认 `LivingDamageEvent.Post` 上 `EvokerFangs` 实际造成的伤害，没掉血就不给缓慢；尖牙归属仍记部署者。
  - 违反后果：D-94 / D-95 破；改代码字面量会造成「config 一份、业务类再写一份」的两个来源（N-44 判据）；用包围盒判定会造出「动画里被咬了、实际没掉血」却被减速的假命中；§11 #15 红（3.0 格不触发、2.5 格触发、第 2 刻内圈先出、第 4 刻外圈后出、被扫到没掉血不给缓慢）。
  - 来源：D-94、D-95；`16-DECISIONS-0.0.3.md` §5.3、§5.4；`18-TECH-0.0.3.md` §3.8、§8.1 第 13 – 17 行、§11 #15。

- **HC-50 · 桑吉尔蔗只改坠落伤害一段，其余 12 项原样**
  - 约束：相位 2 下坠期间取消目标受到的自然坠落伤害（判据 = 身上有 `farmstorm:zangief_piledriver` 且 `amplifier == 2`，另受 `zangief_cane.cancelFallDamage` 开关约束），落地时在 `tickFalling` 的 `player.onGround()` 分支里施加**固定 7.5 点** `farmstorm:zangief_slam`（新增第 3 个 damage type，伤害归属记给陷阱所有者），顺序固定为**先结算、后 `finish`**。其余 12 项（部署形态、世界隐形、相位时序、抬升、漂浮、自转、触发判定与半径、静默移除、音效、输入锁、相机、扫描间隔）逐项原样。
  - 违反后果：D-96 破；漏写取消 = 「固定 7.5 + 自然坠落」叠加、伤害随高度漂移；漏写结算 = 0 伤害；反向顺序（先 `finish` 后 `hurt`）会造出「有伤害但相位已结束」的半截状态；取消范围写大就碰缓降 / 免疫 / 飞行；§11 #16 红（落地恰好减 7.5 点、日志里没有原版坠落伤害、关掉开关后伤害 > 7.5 且随高度变化）。
  - 来源：D-96；`16-DECISIONS-0.0.3.md` §5.5；`18-TECH-0.0.3.md` §3.11、§8.1 第 18 – 19 行、§11 #16；不动清单逐项见 `17-DESIGN-0.0.3.md` §8.3（12 行）；RT-22。

## 4. 代码规范

### 4.1 新增代码落在哪

包结构沿用 `05-PROMPT-MAIN.md` §4.1 与 `02-TECH-SPEC.md` §2 的分工，本轮只新增这些位置（类名与文件路径照 `18-TECH-0.0.3.md` §3 的逐项表，不自造、不改名）：

| 位置 | 放什么 | 来源 |
|---|---|---|
| `effect/` | `QbounceEffect`（可见、带 18×18 图标）、`ConfusionEffect`（隐藏、不注册图标） | `18-TECH` §3.1、§3.2 |
| `entity/thrown/` | `FriendChivesProjectile`、`VibrationBeanProjectile`（都 `extends ThrownCropEntity`） | `18-TECH` §3.3、§3.5 |
| `behavior/impl/` | `PointDefenseFungusBehavior`（`implements TurretBehavior`）、`VibrationBeanTrap`（`implements TrapBehavior`）、`FangTrapFangs` | `18-TECH` §3.4、§3.6、§3.8 |
| `client/` | `QbounceClientState`、`ConfusionInputHandler`，以及 render 分支与 mount 分支的状态驱动 | `18-TECH` §3.12、§4、§5 |
| `registry/` | 既有 `DeferredRegister` 的追加项（物品 / 方块 / 实体 / 效果 / 音效 / 伤害类型）；只放 `DeferredRegister` 与 `Supplier`，不放逻辑 | `18-TECH` §2.1、§2.2 |

### 4.2 改既有文件的边界

本版要改的既有文件只有 `18-TECH-0.0.3.md` 各节「改哪些既有文件」行点名的那些：`block/DeployedCropBlock.java`、`blockentity/DeployedCropBlockEntity.java`、`entity/ThrownCropEntity.java`、`behavior/DeployedKind.java`、`behavior/DeployedBehaviors.java`、`behavior/ZangiefPiledriverController.java`、`behavior/impl/ZangiefCaneTrap.java`、`behavior/impl/FangTrapCactusTrap.java`、`client/InputLockHandler.java`、`config/FarmstormConfig.java`、`util/CropProfile.java`、`registry/` 下被追加项的注册类、`Farmstorm.java`（自检行与伤害类型列表）、`datagen/` 下被追加项的 Provider。名单外的既有文件不动，要动先改 16 号再改代码（HC-36）。

### 4.3 注册

一律 `DeferredRegister`，在 `Farmstorm` 构造器里 `register(modBus)`。本版 `entity_types` +2、`mob_effects` +2、`sound_events` +1、`damage_types` +1、`items` +4、`blocks` +3，合计 +13（`18-TECH` §2.1）。`block_entities` 仍是 6：两个新方块分别追加进两个既有 `BlockEntityType` 的方块集合，不建第二个 BE 类。4 个新物品追加进既有创造标签页，不新建标签页。

### 4.4 数值与常量

19 个 config 键全部进 `FarmstormConfig`，键名、类型、默认值、范围逐字照 `18-TECH-0.0.3.md` §8.1（默认值以施工事实卡 §3.3 为准，范围以 18 号 §8.1 为准，两者不一致时范围取 18 号）；正文书写形态是 `<group>.<key>`，语言键是 `farmstorm.configuration.<group>.<key>`。代码里不许出现玩法数值的字面量，也不许为同一个值设第二个来源。专用服务器上客户端读不到 toml 的三个 render 参数走编译期镜像常量（`DEFAULT_ROTATE_DEGREES_PER_TICK = 24.0F` / `DEFAULT_SQUASH_AMPLITUDE = 0.15F` / `DEFAULT_SQUASH_PERIOD_TICKS = 6`），值必须与 `FarmstormConfig` 的默认值逐字相等；`qbounce.poseMode` 是例外，只在服务端读。

### 4.5 客户端边界

客户端类只落在 `client/` 包内，用 `@EventBusSubscriber(value = Dist.CLIENT)` 或显式 `Dist` 判断隔离。两个效果类、两个投掷物、三种行为、相位状态机与拦截计数都在公共侧——专用服务器起服时不许出现 `NoClassDefFoundError`。

### 4.6 注释与错误处理

只写「为什么」，不写复述方法名的注释；涉及决策的注释带编号，例如 `// D-101：两端共用同一个键与同一个小于号`、`// D-100：拦截音复用原版事件，不新增 sound_event`。被注释掉的代码一律不留。错误处理沿用 `05-PROMPT-MAIN.md` §4.6：不可恢复的初始化失败才抛异常；玩家输入、缺失 NBT、跨区块实体的可恢复异常返回默认值并 `Farmstorm.LOGGER.debug` / `warn`。禁止 `printStackTrace`、禁止空 `catch`、禁止 `catch (Exception e) {}` 兜底。

## 5. 每个任务的执行纪律

### 5.1 Plan —— 动笔前先读

- **做什么**：按固定顺序读，最多四份，不通读全部文档。读完用三句话说清「要建哪些类 / 注册哪些 ID 或 config 键 / 用哪条硬约束兜底」，说不清就不写。
- **读哪份的哪一节**：① 当前任务书 + `16-DECISIONS-0.0.3.md` 里被它点名的 D-xx（通常 2 ~ 4 条）；② `18-TECH-0.0.3.md` 里这次要碰的类那一节（§3.x / §4.x / §5.x / §6.x / §7.x）与要碰的 config 键（§8.1）或资源行（§9.x）；③ `17-DESIGN-0.0.3.md` 里该条目的数值表与状态机；④ `19-TASKS-0.0.3.md` 里那个任务的「交付物」与「依赖」两行。
- **留下什么证据**：回报里「动手前」三句话，含四项——改动类、注册 ID 或 config 键、读到的 D 编号、兜底用的 HC 编号。

### 5.2 Do —— 一次只做一件可独立验证的事

- **做什么**：一个任务的改动一次落完，不在同一批里塞两个任务的活。
- **读哪份的哪一节**：`19-TASKS-0.0.3.md` 的任务粒度定义；`18-TECH-0.0.3.md` §3 的类设计小节。
- **留下什么证据**：`git diff --stat` 一行 + `.tools\gw.cmd compileJava` 的退出码。

### 5.3 Check —— 每步验证

- **做什么**：写完 Java 立刻 `.tools\gw.cmd compileJava`，通过才继续，不积攒改动到最后一次编译；涉及 DataGen 跑 `.tools\gw.cmd runData` 并核对产物真的出现在 `src/generated/resources`；涉及注册表、方块、实体行为时按任务书的「验收」跑 `.tools\gw.cmd runServer` 或 `runClient`；任务收尾 `.tools\gw.cmd build`。
- **读哪份的哪一节**：该任务的「验收」列 + `18-TECH-0.0.3.md` §11 里被引用的自检项编号（写编号，不重抄判据）；17 条自检项的覆盖关系写在 §11 末段的「覆盖声明」里。
- **留下什么证据**：命令原文 + 关键输出行。断言必须落在 `logs/latest.log` 的断言行、游戏内 `/execute` `/data get` `/damage` `/advancement` 的返回值、F3 / F3+B 读数、`src/generated/resources` 或 `config/farmstorm-server.toml` 的文件内容、`build/libs` 或 `dist` 的产物之一。「表现正常」「手感良好」不算证据。

### 5.4 Act —— 卡住时的正确姿势

- **做什么**：先在 `18-TECH-0.0.3.md` 里 grep 类名 / 方法名；再在本地 Gradle 缓存的 NeoForge 源码 / 反编译产物里确认签名；仍确认不了就停下，在回报的「未决问题」里挂对应的 RT 编号并写清阻塞点。18 号 §0.5 标了「待验证」的调用必须先写一个最小用例跑通，再写正式逻辑；跑不通就换成该 RT「兜底」栏写明的替代路径。**不许**为了「让它编译过」而伪造方法、注释掉功能、把实现换成空方法或 `UnsupportedOperationException`。
- **读哪份的哪一节**：`18-TECH-0.0.3.md` §10 的 RT 归属——Q弹 两分支挂 RT-17 / RT-18，混乱挂 RT-19，近防菌挂 RT-20，震动豆挂 RT-21，桑吉尔蔗与刺球挂 RT-22，数字与工具挂 RT-23；API 存在性 / 签名一律挂 RT-01，不新开编号。
- **留下什么证据**：回报里的未决问题行（含 RT 编号、确认过的真实签名、阻塞位置）。

### 5.5 任务粒度的三判据

1. 提交后 `.tools\gw.cmd build` 退出码 0，且不依赖同一批次中尚未提交的任务。
2. 任务自带至少一条可在 §5.3 列出的位置观察到的验收断言。
3. 回滚该提交不会让其它已完成任务失效（回滚只影响依赖它的后续任务）。

另有两条边界同样硬：一个任务不跨 Phase 边界（交付物全部落在它所属 Phase 的名称范围内）；一个任务只动 `18-TECH-0.0.3.md` 「改哪些既有文件」行点名的既有文件 + 新增文件。跨 4 个及以上包的改动要拆开做（`05-PROMPT-MAIN.md` §5.2 口径）。

### 5.6 边界

只动任务书划定的文件。看到边界外的缺陷、死代码、命名不一致，写进回报的「未决问题」，不顺手修。每个任务结束时仓库必须可编译，不留半成品状态。

## 6. 完成定义（DoD）

一个任务算完成，以下**全部**成立，缺一即视为未完成：

1. 任务书列出的验收项逐条可指认到代码、资源或命令读数。
2. `.tools\gw.cmd compileJava` 通过；涉及 DataGen 时 `.tools\gw.cmd runData` 通过且产物落进 `src/generated/resources`；收尾 `.tools\gw.cmd build` 退出码 0 并产出 `build/libs/farmstorm-0.0.3+mc1.21.1.jar`。
3. `python -X utf8 tools/verify_project.py` 输出 `已核 registry 对象: 90 / 90`、config 段 158 键无 FAIL、`lang 键集一致，共 300 键`、`结论：FAIL 0 项`，退出码 0（工具尚未修好时先做 HC-32 的修复任务，不拿红灯交差）。
4. 三组计数成立：注册 90 且逐类为 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1；config 158；两份语言文件各 300 键且 `set(zh) == set(en)`；资源增量 8 / 1 / 22（合计 31）与既有文件的 6 个追加点逐条对得上。
5. 启动日志的三行自检都在且以 `OK` 结尾：注册对象 90、作物 22 种（投掷型 12 / 部署型 7 / 食用型 3、破障类 4）、幸运浆果池 19 种。
6. 红线可判：`grep -r "CustomPacketPayload" src/main/java` 为 0；`grep -rn "Mixin" src/main/java` 为 0。
7. 无越界改动：`git status` 只出现新增文件与 `18-TECH-0.0.3.md` 点名的既有文件；`.work/v003-BRIEF.md`、`docs/zh/00` ~ `15`、`docs/en/00` ~ `15` 未被触碰；16 – 21 是本次交付物，改动只落在 16 – 21 这六份上。
8. 无绕过：没有注释掉的实现、空方法占位、`@SuppressWarnings("unused")` 掩盖未使用字段、以占位标记顶替逻辑。
9. 有可复现证据：命令原文 + 关键输出行，而不是「我跑过了，没问题」。

## 7. 回报格式

任务结束后按下面的模板原样输出，不即兴发挥，不省略小节：

```
## 任务回报
- 任务：<F-编号 / 任务书标题>
- 状态：完成 / 部分完成 / 阻塞
- 动手前三句话：<改动类 / 注册 ID 或 config 键 / 读到的 D 编号 + 兜底 HC 编号>
- 改动文件：<新增与修改分开列；超过 8 个时只列路径 + 一句话作用；名单外的既有文件必须单独标出并说明依据>
- 执行的命令与结果：
  - .tools\gw.cmd compileJava → BUILD SUCCESSFUL（<耗时>）
  - .tools\gw.cmd runData → 生成 N 个文件到 src/generated/resources
  - .tools\gw.cmd build → 退出码 0，产物 build/libs/farmstorm-0.0.3+mc1.21.1.jar
  - python -X utf8 tools/verify_project.py → 已核 registry 对象: 90 / 90；config 158；lang 300；结论：FAIL 0 项（退出码 0）
  - （失败的命令照写，附第一条错误原文与总结行）
- 验收项对照：<任务书每个验收项 → 证据位置（文件:行 / 命令输出关键行 / 日志断言行 / 18 号 §11 的自检项编号）>
- 计数与红线：<本条涉及的 90 / 158 / 300 / 8 / 1 / 22 现值；CustomPacketPayload 与 Mixin 的匹配数；sounds.json 事件数>
- 未决问题：<文档矛盾、缺失的 API、必须由作者裁决的事项；挂 RT 编号的在这里点名；无则写「无」>
- 下一步提示：<刻意没做但下一步会碰到的事，一句话>
```

硬规矩：不贴超过 20 行的日志，超过就只留第一条错误与总结行；不把「我计划」写成「我完成」；RT 编号一旦挂上必须在「未决问题」里点名；隐瞒一处 RT-01 的签名不确定，比写十行未决问题危害大得多。

## 8. 禁止行为清单（N-29 ~ N-50）

以下每条都可用一条命令、一处 `git diff` 或一次读数判定，不是态度要求。

- **N-29** 不得为拦截音或震动豆爆炸音新增 `sound_event`，也不得在 `sounds.json` 里加第 4 个事件；判据是 `sounds.json` 事件数 = 3，且 `grep -rn "createVariableRangeEvent" src/main/java` 的命中数与 `sound_events` 的 3 个条目一致。
- **N-30** 不得给 Q弹 的 mount 分支新增 `entity_type`；判据是 `FarmstormEntityTypes` 里 `projectile(...)` 的调用点为 12 次 + `turret_bullet` = 13 个 `entity_type`，且 `grep -rn "EntityType.ARMOR_STAND" src/main/java` 有命中、`registry` 下没有任何新的坐骑注册项。
- **N-31** 不得把震动豆的 `DeployedKind` 置成 `null` 或省略；判据是 `CropProfile.VIBRATION_BEAN` 的 `kind` 实参为 `DeployedKind.VIBRATION_BEAN`，`DeployedKind` 枚举有 8 个常量，`DeployedCropBlockEntity.java` 里 `BLAST_CARROT` 的命中仍是回落分支那一处、没有新增第二处。
- **N-32** 不得在震动豆的判定里出现第二个 `4.0` 字面量或第二个比较符；判据是 `behavior/impl/VibrationBeanTrap.java` 里 `4.0` 命中为 0（只读 config 键），且两端判定行都用 `<`、该文件里 `<=` 命中为 0。
- **N-33** 不得把 `tools/verify_project.py` 的红灯当已知问题绕过，也不得为了让工具变绿去改 `01-DESIGN.md`；判据是修完后 `结论：FAIL 0 项` 且退出码 0，`git status` 不出现 `docs/zh/01-DESIGN.md`，回报里没有任何未经工具复核的总数。
- **N-34** 不得新增 `CustomPacketPayload`、`PayloadRegistrar` 条目或任何自定义网络注册；判据是 `grep -r "CustomPacketPayload" src/main/java` 为 0。
- **N-35** 不得引入 Mixin；判据是 `grep -rn "Mixin" src/main/java` 为 0，且 `verify_project.py` §4 的禁令扫描段无命中。
- **N-36** 不得让近防菌的拦截产生伤害、破坏方块或构造 `Explosion`；判据是拦截路径里 `hurt(` / `setBlock(` / `new Explosion` 命中 0，且 §11 #14 的血量、饥饿、坐标逐点不变。
- **N-37** 不得让被拦下的投掷物继续结算落点；判据是拦截分支在 `onImpact` 之前返回，被拦投掷物落地无爆炸、无方块写入、无掉落、无效果（§11 #8 第一段）。
- **N-38** 不得拦原版投射物、`farmstorm:turret_bullet` 或 `isSubmunition()` 为真的碎片；判据是 §11 #13 的 5 个投射物全部穿过且 `interceptCount` 不变，`findTarget` 的 predicate 里不出现 `Arrow` / `Snowball` / `ThrownTrident` / `Enderpearl`。
- **N-39** 不得为近防菌或震动豆新建 `BlockEntityType`；判据是注册自检里 `block_entities` 仍为 6，`FarmstormBlockEntities` 的 `getEntries().size()` 为 6。
- **N-40** 不得把混乱写成第二套锁，也不得反转视角、跳跃、攻击、使用；判据是 `ConfusionInputHandler` 只写 `forwardImpulse` 与 `leftImpulse`，第一行是 `if (InputLockHandler.isLocked()) return;`，该文件里 `setYRot` / `setXRot` / `MOVEMENT_SPEED` / `jumping` 命中为 0。
- **N-41** 不得删掉落选的 Q弹 分支；判据是 `PoseMode.MOUNT` 与 `PoseMode.RENDER` 在 `src/main/java` 各有命中，两条分支都能被 `qbounce.poseMode` 关掉，且 §11 #3 / #4 都能验。
- **N-42** 不得改刺球的 registry id、作物方块 id、`DeployedKind` 常量与英文名；判据是 `fang_trap_cactus` 在 `registry/` 与 `CropProfile` 里的写法不变、`en_us.json` 未被改动、`DeployedKind.FANG_TRAP_CACTUS` 仍在。
- **N-43** 不得动刺球的个数与两个分布半径；判据是 `outerFangCount` 8 / `outerRadius` 2.0 / `innerFangCount` 5 / `innerRadius` 1.0 的默认值与对应代码行在 `git diff` 里没有出现。
- **N-44** 不得改既有 config 键的默认值（`fang_trap_cactus` 的 `triggerRadius` / `outerDelayTicks` / `innerDelayTicks` 三个之外）；判据是 `FarmstormConfig` 的 diff 只有 16 个新键 + 该 3 行的默认值实参，且业务类里没有第二处同一数值的字面量。
- **N-45** 不得为朋友的韭生成转换配方；判据是 `runData` 后 `data/farmstorm/recipe/` 里没有 `friend_chives` 的配方文件，本版转换配方增量恒为 +2（近防菌与震动豆各 1）。它只以 `farmstorm:converter_fuel` 标签成员的身份参与合成。
- **N-46** 不得用 `supportsFloating` 冒充震动豆的「不依附」；判据是 `vibration_bean` 的 `deployed(...)` 实参为 `unattached = true` 且 `supportsFloating = false`，§11 #10 的 `FLOATING` 始终为 false。
- **N-47** 不得把 +13 当总数、不得写死任何未经工具复核的注册总数；判据是回报里的 90 必须同时带 `verify_project.py` 的 `90 / 90` 行与 `logs/latest.log` 的自检行。
- **N-48** 不得用 AABB 粗筛当命中判定（近防菌 8 格、朋友的韭 6 格、震动豆 4 格）；判据是每个 predicate 里都有 `distanceTo(...)` 的欧氏复核，且 §11 #8 的边界「对角 7.9 格拦、8.1 格不拦」成立。
- **N-49** 不得让朋友的韭空投也播音，也不得把 32 格写成估计值；判据是 §11 #12：命中播一次、32 格内可闻、33 格外不可闻，空投不播。
- **N-50** 不得改 `docs/zh/00` ~ `15` 与 `docs/en/00` ~ `15` 的任何一份，包括错别字；判据是 `git status` 不出现这些路径（D-80）。文档侧的改动只允许落在 16 – 21 这六份上。

## 9. 已知陷阱

先记四条贯穿全仓的：**`neoForge { }` 大小写敏感**，写成 `neoforge { }` 会报 `Could not find method neoForge()`；**`.tools\gw.cmd` 是唯一构建入口**，直接调 `gradle` / `gradlew` 必然失败；**两个 `runServer` 必须隔开运行目录**，否则撞 `run/world/session.lock` 并互相覆盖日志；**ffmpeg 不在 PATH 上**，音频产线必须用 `-Ffmpeg` 传绝对路径。

| 现象 | 根因 | 怎么躲 | 出处 |
|---|---|---|---|
| 构建报 `Could not find method neoForge()` | 扩展块名是 `neoForge { }`，**大小写敏感**；插件 `net.neoforged.moddev` 未应用时也报同一句 | 不对着 `neoforge` 反复试；确认插件已应用且块名大小写正确，本轮 `build.gradle` 零改动，结论写进回报 | `18-TECH` §0.1；14 号 §9 |
| 直接调 `gradle` / `gradlew` 失败或行为异常 | `.tools\gw.cmd` 负责四件缺一不可的事：设 JDK 21 与 `GRADLE_USER_HOME`、拉起 NeoForge 镜像代理、注入 java.base 补丁、给 `runServer` 加目录锁 | 一律 `cmd /c ""D:\project_farm\.tools\gw.cmd" <任务>"`；不绕过、不另写替代脚本 | `08-DEV-GUIDE.md` §4.1 |
| 震动豆放下后行为像胡罗贝的爆炸陷阱 | `kindOf()` 对非 `DeployedCropBlock` 直接抛异常，而 `kind` 在构造器里取自 `profile.kind()`，取不到就**静默回落 `BLAST_CARROT`** | `CropProfile.VIBRATION_BEAN` 的 `kind` 填 `DeployedKind.VIBRATION_BEAN`；不照 `16-DECISIONS` §4 第 22 行旧的「—」填 `null` | `18-TECH` §1.4(b)、§3.7；RT-21 |
| 悬空陷阱凭空消失（玩家看到「豆子放下去就没了」） | `updateShape` 漏写 `unattached` 早退，邻近方块一变就被移除 | 早退放方法第一行、返回同一个 `BlockState` 实例；`FLOATING` 恒 false；§11 #10 逐面挖 6 个相邻方块验一次 | `18-TECH` §3.7、§7.2、§7.3 |
| 隐形方块被加了 90° 旋转、任何 `canSurvive` 读法都判它该消失 | 误用 `supportsFloating` 做「不依附」：`updateShape` 会置 `FLOATING = true`，而 `canSurvive` 仍返回 `isSupportValid(...)` = false | 用第 5 个独立旗标 `unattached`，与 `supportsFloating` 正交；既有 6 个 `deployed(...)` 补实参 `false` | `18-TECH` §7.1、§7.3；RT-21 |
| 投掷物明明被拦了却照常爆炸 / 写方块 | `fire` 里没在 `onImpact` 之前 `return`，或 `discard()` 写在 `recordIntercept` 之前而漏了返回 | `recordIntercept(now)` 后立刻 `target.discard()`，拦截成功即返回；§11 #8 第一段验「落地无任何后续」 | D-99；`18-TECH` §6.2 第 6 步、§6.5 |
| 自己人与队友的投掷物被自己的菌吃掉 | 己方筛选漏写，或把 `isAlly` 换成了新写的一套队伍比较 | predicate 的第 ④ 筛做己方排除，判据只用 `be.isAlly(player)`；§11 #8 第三段验「穿过且 `interceptCount` 不变、无粒子」 | D-98；`18-TECH` §3.4、§6.2 |
| 同一刻被 N 个近防菌各扣一次，或「扣了但没拦」 | 去重标记放错对象（放在方块上只知道自己被扣了几次） | 标记放投掷物的 `lastInterceptTick`，`fire` 第一步 `if (target.markIntercepted(now)) return;`；标记不进 NBT、计数进 NBT | D-88；`18-TECH` §3.9、§6.3 |
| 拦截音没声音，或凭空多出一个 `sound_event` | 新增事件却没配第二个 ogg；或手写 `minecraft:***` 字符串记错事件 id | 用 Java 常量 `SoundEvents.SHIELD_BLOCK` / `SoundEvents.GENERIC_EXPLODE`，代码里不出现手写字符串；`sounds.json` 事件数保持 3 | D-100；`18-TECH` §9.2、§8.4 |
| 震动豆「没触发却被施加混乱」（边界自相矛盾） | 触发端写 `<`、效果端写 `<=`，或两处各写各的 `4.0` 字面量 | 两端读同一个 `vibration_bean.triggerRadius`、写同一个 `<`；§11 #11 在 3.9 格与 4.1 格各验一次 | D-101；`18-TECH` §3.6、§7.2 |
| Q弹 只有压低与旋转、没有坐姿 | 第三方模组的自定义 `EntityModel` 不读 `riding` 字段（RT-17 的主风险） | 接受该形态并记进验收记录；`pushPose` / `popPose` 失配时退到「只 `translate` + `scale` + `mulPose`」；实在拿不到坐姿就切 `qbounce.poseMode = mount` | `18-TECH` §4.2；RT-17 |
| 效果结束后玩家仍被隐形实体钉在原地 | mount 分支的清理路径漏写，或只在事件里清、事件没覆盖换维度 / 停服 | `Map<UUID, MountRecord>` + 每 tick 三条校验（不依赖事件）+ 四条退出路径；重挂超过 20 次放弃并打一行 warn | `18-TECH` §4.3；RT-18 |
| 效果期间按 W 反而向后走，或锁与混乱互相覆盖 | 把事件订阅顺序当判据，或给混乱写了第二套锁 | `if (InputLockHandler.isLocked()) return;` 放第一行；靠 `−0.0F == 0.0F` 的天然退化兜底；七段锁一行不改 | D-83、D-79；`18-TECH` §5.2；RT-19 |
| 刺球改了数值但 config 里还是旧值，或同一数值出现两份 | 去改 `FangTrapCactusTrap` 的代码字面量（L38 / L59 / L61）而不是 `defineInRange` 的默认值实参 | 3 行改动全部落在 `FarmstormConfig` 的默认值实参上，代码行不动；§11 #15 用 3.0 格与 2.5 格各试一次 | D-94；`18-TECH` §3.8 |
| 桑吉尔蔗落地 0 伤害，或伤害随高度漂移 | 只取消没结算、只结算没取消，或顺序反成「先 `finish` 后 `hurt`」 | 取消与结算写在同一个相位状态机里，顺序固定为「先结算、后 `finish`」；关掉 `cancelFallDamage` 做对照实验时必须看到伤害大于 7.5 且随高度变化 | D-96；`18-TECH` §3.11；RT-22 |
| Ogg 文件与上次对不上，怀疑转换出错 | Ogg 每次重编码的**流序列号是随机的**，字节不可复现，md5 天然不同 | 判据是「时长 / 声道 / 码率」三项（`ffprobe` 读数），**不比 md5**；重编单文件一次，保留新旧两份读数 | `12-TECH-0.0.2.md` §6.4 |
| 音频产不出来 / 构建成功但没声音 | ffmpeg 在 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe` 但**不在 PATH 上**；Trae CN 的 6.1.1 无 libvorbis 不可用 | 传 `-Ffmpeg` 绝对路径；脚本缺 ffmpeg 时退出码 1 并打印安装指引；音频任务排在资源阶段最前 | `12-TECH-0.0.2.md` §6.4；RT-14；`tools/convert_media.ps1` 的 `-Ffmpeg` 参数 |
| `verify_project.py` 一直红，越修越红 | 工具仍按 0.0.1 枚举、分母 64、config 期望值只读 `01-DESIGN.md` §3.8 的 117，而该文件属冻结区不能回改 | 按 `18-TECH` §1.4(c) 的四段改法修：期望键集改 139、`EXPECTED` 改 27 / 22 / 11、分母改 77、补齐剩余 5 个裸 ID；修完再收到 90 / 158 / 300 | `18-TECH` §1.4(c)、§10 RT-23、§11 #1 |
| 改了 config 默认值但游戏里行为没变 | 已生成的 `config/farmstorm-server.toml` 不会因默认值变化而重写 | 删掉该文件重新生成，或手动改值；核对叶节点 158 | `08-DEV-GUIDE.md` §4.3；14 号 §9 |
| 两个 `runServer` 撞 `session.lock`、日志互相覆盖 | 并行服务端共用一个运行目录 | 起第二个之前加 `-PfsRunDir=run-xxx`，各自独立目录 | `08-DEV-GUIDE.md` §4.2 |
| 扔新投掷物客户端崩 `entityrenderer is null` | 新增的 `entity_type` 没注册客户端渲染器 | 13 个 `entity_type` 每个都 `registerEntityRenderer`，自检行必须是 13/13；两个新投掷物沿用 `ThrownItemRenderer` | `18-TECH` §2.1 第 4 行；14 号 §9 |
| 无玩家时震动豆 / 近防菌完全不响应 | 没人的区块不 tick | 测前 `forceload add`，轮询确认方块实体真进了世界再计时 | `08-DEV-GUIDE.md` §7 |

## 10. 硬约束 → 来源 对照表

| 编号 | 约束摘要 | 来源 |
|---|---|---|
| HC-29 | 注册总数只有一个来源：基线 77、增量 +13、目标 90 | D-80；`18-TECH` §0.4、§1.2、§2.1、§2.4；RT-23 |
| HC-30 | config 158 与语言键 300 恒含同一批键（19 行 = 16 新 + 3 改） | `18-TECH` §8.1、§8.3、§9.4；`.work/v003-BRIEF.md` §3.3 |
| HC-31 | 资源只许 31 个新文件（PNG 8 / OGG 1 / JSON 22），既有文件只在 6 个文件上追加 | `18-TECH` §9.1、§9.2、§9.3；`.work/v003-BRIEF.md` §3.4 |
| HC-32 | verify_project.py 先修后跑，红灯不许签字绕过 | RT-23；`18-TECH` §1.4(c)、§10、§11 #1 |
| HC-33 | 零自定义网络包，效果状态靠原版 MobEffect 同步 | C-6、D-50；`16-DECISIONS` §7.2；14 号 §3 HC-15 |
| HC-34 | 无 Mixin，尖牙判定从伤害事件侧挂钩 | D-40；`18-TECH` §1.4(c) 的 §4 段、§3.8；14 号 §3 HC-16 |
| HC-35 | 真灰度继续有效，三个客户端子系统不动 | D-61、D-79；`16-DECISIONS` §5.7、§7.2；`18-TECH` §5.2；14 号 §3 HC-26 |
| HC-36 | 0.0.2 既有内容只有刺球与桑吉尔蔗两处授权改动 | D-80、D-93 ~ D-96；`16-DECISIONS` §5.1、§5.6、§5.7 |
| HC-37 | 近防菌只拦非己方投掷物（己方穿过、不扣次数） | D-98；`18-TECH` §3.4、§6.1、§6.2、§11 #8 |
| HC-38 | 判定面是 ThrownCropEntity 基类，家族 12 个，原版投射物不拦 | D-87、D-98；`18-TECH` §6.1、§6.2、§11 #13 |
| HC-39 | 被拦投掷物当场 discard，不结算落点 | D-99；`18-TECH` §3.4、§6.2、§6.5、§11 #8 |
| HC-40 | 拦截图效纯视觉：不伤害、不破块、不构造 Explosion | D-89；`18-TECH` §6.5、§11 #14 |
| HC-41 | 拦截音与震动豆爆炸音复用原版 sound_event，不新增 | D-100；`18-TECH` §8.4、§9.2、§2.1 |
| HC-42 | 第 6 次拦截后静默移除，同刻只扣一次 | D-88、D-72；`18-TECH` §3.9、§3.10、§6.3、§11 #8 / #9 |
| HC-43 | 震动豆 DeployedKind 必填 VIBRATION_BEAN，枚举 6 → 8 | D-91、D-86；`16-DECISIONS` §4 表下注；`18-TECH` §1.4(b)、§2.2 注 3 |
| HC-44 | 不依附是第 5 个独立旗标，不复用 supportsFloating | D-91；`18-TECH` §3.7、§7.1、§7.2、§11 #10；RT-21 |
| HC-45 | 震动豆两端同一把尺子：严格小于 4.0 | D-101、D-92；`18-TECH` §3.6、§7.2、§11 #11 |
| HC-46 | Q弹 两条分支都实现，mount 不新增 entity_type | D-82、D-102；`18-TECH` §4.1 ~ §4.4、§11 #3 / #4 / #5；RT-17、RT-18 |
| HC-47 | 混乱只反移动输入，锁优先，不注册图标 | D-83、D-79；`18-TECH` §5.1 ~ §5.3、§11 #6 / #7；RT-19 |
| HC-48 | 刺球只改中文名，id 与英文名不动 | D-93；`16-DECISIONS` §5.2、§6 第 2 条；`18-TECH` §3.8 |
| HC-49 | 刺球 8 行数值逐项落位，命中以实际伤害为准 | D-94、D-95；`16-DECISIONS` §5.3、§5.4；`18-TECH` §3.8、§11 #15 |
| HC-50 | 桑吉尔蔗只改坠落伤害一段，其余 12 项原样 | D-96；`16-DECISIONS` §5.5；`18-TECH` §3.11、§11 #16；`17-DESIGN` §8.3；RT-22 |

> 说明：本表的 22 行与 §3 的 22 条一一对应，行数必须相等。引用顺序按「决策编号优先、实现形态补节号」：能指到 D 编号的写 D 编号，需要落到具体写法时再补 `18-TECH-0.0.3.md` 的节号。`16-DECISIONS` 指 `16-DECISIONS-0.0.3.md`，`18-TECH` 指 `18-TECH-0.0.3.md`，`17-DESIGN` 指 `17-DESIGN-0.0.3.md`。
