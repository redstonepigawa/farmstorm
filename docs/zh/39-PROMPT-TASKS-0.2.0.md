# 39 号 v0.2.0 分阶段任务清单（Phase Prompts）

> 投喂方式：**先投 38 号主提示词一次**，之后**每个 Phase 开工时只投该 Phase 的一节**；同一时间只投一节。每节从前到后独立可执行——把一节单独交给实现代理，不需要它读过上一节，也不需要它读过本文件的其它节。

| 项 | 值 |
|---|---|
| 文档编号 | 39-PROMPT-TASKS |
| 版本 | v0.2.0（MC 1.21.1 / NeoForge 21.1.213 / Java 21） |
| 读者 | 投喂编码代理的人 + 被投喂的 CLI 编码代理 |
| 上游 | `37-TASKS-0.2.0.md`（**F-213 ~ F-246 的唯一来源**：编号、标题、Phase 归属、复杂度、人日、依赖）、`36-TECH-0.2.0.md`（§1.2 注册面 97、§4.1 config 175 / 60、§4.3 语言键 337、§8 的 36 条验收、§9 的 13 条待验证）、`35-DESIGN-0.2.0.md`（11 条需求、B-01 ~ B-40）、`34-DECISIONS-0.2.0.md`（D-148 ~ D-172、19 条裁定） |
| 同级 | `38-PROMPT-MAIN-0.2.0.md`（常驻主提示词，只投一次）。本文件**不重复**它的硬约束、红线口径与执行纪律；每节只重述 §0.2 的五行，且每节都能脱离 38 号单独执行 |
| 内容 | **8 个 Phase / 34 个任务（F-213 ~ F-246）**；每节固定六小节：前置 / 本阶段目标 / 任务清单 / 出口判据 / 翻车点 / 汇报格式 |
| 出口判据 | 8 节合计 **66 条**（每节 7 ~ 10 条），全部以 `- [ ]` 开头。判据只做「本节任务自证」，不新增验收维度；全量口径在 §8：36 号 §8 的 **36** 条 + 35 号 §12 的 **40** 条 = **76** 条 |
| 边界 | 只读上游：不改 `docs/zh/34` ~ `docs/zh/37`、不改 `docs/en/`、不改 `src/`、不改 `.work/` 的三份台账；只新建本文件 |

---

## 0. 投喂顺序与约束

### 0.1 八节投喂顺序（Phase 32 → 39）与每节的前置

| 顺序 | 投哪一节 | Phase · 里程碑 | 任务 | 人日 | 本节前置（缺一条都不许开） |
|---|---|---|---|---|---|
| 1 | §1 | 32 · **M28** | F-213 ~ F-215 | 5.0 | 工作树干净（`git status` 只出现本节要改的 10 个文件）；`.tools\gw.cmd build` 退出码 0；基线是 **0.1.1 已发布态**（`src/` 自 `v0.1.1` 标签后 0 改动，D-148）。**本节是第一个 Phase，没有上一节** |
| 2 | §2 | 33 · **M29** | F-216 ~ F-219 | 2.0 | §1.4 的**第 1 / 3 / 4 条**已绿（产物名 0.2.0、7 处构造全带 `CropTier`、逐档 11 / 7 / 4）；日志里已有 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` |
| 3 | §3 | 34 · **M30** | F-220 ~ F-223 | 6.5 | §2.4 的**第 1 / 3 / 5 条**已绿（池 11 与断言同提交、两条播种路径各堵一次）；`tier()` 已可读（F-214 落地）——交易按档抽，没有 `tier()` 落不了地 |
| 4 | §4 | 35 · **M31** | F-224 ~ F-228 | 12.5 | §3.4 的**第 1 / 6 条**已绿（五处注释各带理由行、3 条耕地键在两份 lang 里 0 命中）；`[outpost_core]` 已无 `farmlandBonus`。**注册自检窗口在此打开**：开工时 `total` 仍是 93 / `→ OK` |
| 5 | §5 | 36 · **M32** | F-229 ~ F-233 | 9.5 | §4.4 的**第 1 / 5 / 6 / 7 条**已绿（状态 A 96 / 容器面 / 状态 B 97 / 关窗 `97 … → OK`）；`menu_types 2` 与 `block_entities 8` 在位 |
| 6 | §6 | 37 · **M33** | F-234 ~ F-237 | 7.0 | §5.4 的**第 3 / 4 条**已绿（`Cultivators` 落盘、放置绑定四判据）——F-232 是 F-235 的前置；`[outpost_core]` 4 键的 toml 读数已取到 |
| 7 | §7 | 38 · **M34** | F-238 ~ F-240 | 7.5 | §5.4 的**第 5 条**已绿（迁移三出口）——F-233 是 F-239 的前置；能同时起「单人客户端」与「专用服务器」；第二客户端可用于范围与「非己方」对照 |
| 8 | §8 | 39 · **M35** | F-241 ~ F-246 | 11.0 | §1 ~ §7 的**全部**出口判据已绿，其中 §6.4 第 4 / 5 条与 §7.4 第 5 / 6 条是 36 条验收的主体；三个滞后窗口同时到达关窗点（注册已关、语言键本节关、config 本节关） |
| 9 | 收尾 | 不在 Phase 内 | — | — | §8 的出口判据全过：**76 条**（36 + 40）与 **34 个任务**全部有结论 |

读表三条：

1. **顺序是投喂顺序，不是唯一可执行顺序。** `§4` 里有两条互不依赖的起跑线（F-224 培养器线 / F-226 核心线），可以分给两个人；但**一次只投一节**，投哪节只做那节的 F-xx，不跨节混做。
2. **`§6` 不能提到 `§5` 之前**：`isUsable` 要读核侧列表（F-231）、产出要调 `acceptAll`（F-226）、迁移要改绑（F-232 的写入形态）。三条少任何一条，培养器只有空转。
3. **`§8` 的 F-244 等 `§1` ~ `§7` 的 26 个任务（F-215 ~ F-240）全部落地。** F-241 / F-242 / F-243 三个总账可并行先做，但 F-246 一定要等 F-243、F-244、F-245 三份记录齐——签字是最后一步，不是第一步。

### 0.2 每节开头必须重申的五条（不依赖上下文）

38 号主提示词已投过一次之后，把下面五行**原样**贴在每一次投喂的开头（38 号之后、本节正文之前）。这五行是本节唯一被允许「不解释就直接执行」的口径，节内不再重复：

```text
① 红线只剩一条：无 Mixin（@Mixin / mixins.json 全仓 0 命中）。自定义网络包不设上限（D-146 / D-172）：
   本版累计 2 个，F-238 之前 1 个（farmstorm:core_list）、之后 2 个（+ farmstorm:cultivator_link）；客户端都不回包。
② 三个总数只在自己的收口点成立：注册对象 97 只在 F-228 之后、config 175 键 / 60 组 只在 F-242 之后、
   语言键 337 × 2 只在 F-241 之后。中途只写「本条键存在 / 不存在」与「本条 toml 区间读数」，
   不许把收口数写成本节的通过条件，也不许改 ModLangProvider:13-16 与 FarmstormConfig 的总数注释。
③ tools/verify_project.py 在 F-243 之前不是判据（A-66）：它对注册面打「已核 registry 对象: 93 / 93」并退出 0，
   对 config 段报「toml 多而文档没记」。F-243 之后引用它，必须同时给出「已核 registry 对象: 97 / 97」——
   只写「结论：FAIL 0 项」的是假绿，会静默少核 4 个对象。
④ 注册自检窗口 = F-224（开）→ F-228（关）：窗口内只写 FAIL 行的实际分项（状态 A 96 / 状态 B 97），
   唯一允许写 → OK 的是关窗任务 F-228（5 个常量一次改齐后真起一次服）。
⑤ A-62：outpost_core.farmlandBonus 只注释定义（FarmstormConfig.java:909-912），字段声明（:227）保留；
   A-69：farmstorm_farmland.dat 的「不读不写」判据只能在 F-233 之后执行——此前 settleOnRemoval 仍是它的最后读者。
```

### 0.3 速查表（一屏：Phase ↔ 里程碑 ↔ 任务号 ↔ 人日 ↔ 出口判据一句话）

| Phase | 里程碑 | 任务号 | 个数 | 人日 | 出口判据（一句话） |
|---|---|---|---|---|---|
| 32 基线与品质载体 | M28 | F-213 ~ F-215 | 3 | 5.0 | 产物 `farmstorm-0.2.0+mc1.21.1.jar`；7 处 `new CropProfile(` 全带 `CropTier`；逐档 11 / 7 / 4；`tooltip.farmstorm` 16 → 20 |
| 33 规则面收窄 | M29 | F-216 ~ F-219 | 4 | 2.0 | 池 `11 种… → OK` 且 `== 19` 0 命中；15 个作物与 7 个种子种不下去、7 个部署型照常摆；空手右键转换器无反应而 29 条配方仍在 |
| 34 耕地停用与交易 | M30 | F-220 ~ F-223 | 4 | 6.5 | 5 处注释各带理由行、`FarmstormConfig.java:227` 字段保留；toml 165 / 58 → 171 / 59；农民 3 项在 1 / 3 / 5、`maxUses = 1`、价格落 [2,5] / [10,32] / [40,64] |
| 35 注册面、资源与容器骨架 | M31 | F-224 ~ F-228 | 5 | 12.5 | 窗口期两次 FAIL（96 四个分项 / 97 且 `menu_types 2`），F-228 后 `97 … → OK`；`blockstates` 27 / `models` 106 / `loot_table` 27 / mcmeta 2 |
| 36 核心交互、绑定与迁移 | M32 | F-229 ~ F-233 | 5 | 9.5 | 54 格界面两道闸（9 格开不了）；四条件 0 掉落；`Cultivators` 1 条；改绑 / 留原地 / NBT 一字节不变；`setBlock` 0 |
| 37 培养器行为 | M33 | F-234 ~ F-237 | 4 | 7.0 | `[cultivator]` 600 / 2、全文 175 / 60；史诗拒绝；600 刻 +2 与 +2、核满 `Progress == cycleTicks` 不丢；挖掉掉 1 + 1 |
| 38 连线 | M34 | F-238 ~ F-240 | 3 | 7.5 | 2 行 `自检 网络包注册`、协议串 2；范围内 N 条、出范围空包；10 分钟 0 条 `Missing elements in vertex` |
| 39 收口与全量验收 | M35 | F-241 ~ F-246 | 6 | 11.0 | 337 × 2 键集相同、175 / 60、`97 / 97` 与 `FAIL 0` 同时成立；36 + 40 = 76 条全过；`@Mixin` 0 |
| **合计** | **M28 ~ M35** | **F-213 ~ F-246** | **34** | **61.0** | 裸人日 61.0（`11 × 0.5 + 9 × 1.5 + 14 × 3.0`），含 25 % 缓冲 **76.25**；关键路径 **21.0 人日 / 8 个任务**（核心线） |

### 0.4 三个滞后窗口的读数台账

| 窗口 | 开 | 关 | 中途只许写 | 关窗时一次改齐 |
|---|---|---|---|---|
| 注册自检 | **F-224** | **F-228** | FAIL 行的实际分项：状态 A = **96**（`items 33 / blocks 27 / block_entities 8 / menu_types 1`）；状态 B = **97** 且 `menu_types 2` | 5 个常量 `93 → 97`、`32 → 33`、`26 → 27`、`7 → 8`、`1 → 2` + 方法注释里两处 `93` |
| 语言键 | **F-215** | **F-241** | 「本条键存在 / 不存在」+ 前缀桶增减（`tooltip` 16 → 20、`container` 1 → 2、`message` 2 → 0） | 两份 lang 各 **337** 与 `ModLangProvider.java:13-16` 的逐项构成 |
| config | **F-220** | **F-242** | 本条任务的 toml 区间读数：**165 / 58** → **171 / 59** → **172 / 59** → **173 / 59** → **175 / 60** | **175 键 / 60 组** 与逐组核对（`b.push(` = **60**） |

三条读法：

1. **三个窗口各自只在关窗任务里改「总数」**，且关窗任务不夹带任何别的改动（F-228 的 `git diff` 只动那 5 个数与那段注释；F-241 / F-242 各只动自己那一份账）。
2. **窗口内每一次起服都能读到原文**：注册自检行在 `run/logs/latest.log`、两份 lang 的键集在 `src/generated/resources/assets/farmstorm/lang/`、config 全文在 `run/config/farmstorm-server.toml`。三者都可复查，不靠记忆。
3. **三个窗口的起止点不在同一个 Phase**：注册窗口整个落在 §4；语言键窗口从 §1 开到 §8；config 窗口从 §3 开到 §8。跨节引用一律写「本节的区间读数」，不写「已到位」。

---

## 1. Phase 32 · 基线与品质载体（M28）

### 1.1 前置

- [ ] 工作树干净：`git status` 只出现本节要改的 **10** 个文件（`gradle.properties`、`一键构建.cmd`、`检查环境.cmd`、新建的 `util/CropTier.java`、`util/CropProfile.java`、`Farmstorm.java`、`item/ModifiedCropItem.java`、`item/DeployableCropItem.java`、`item/SeedItem.java`、`datagen/ModLangProvider.java`）。
- [ ] `.tools\gw.cmd build` 当前退出码 **0**；构建入口只认 `.tools\gw.cmd build`，不要直接调 gradle。
- [ ] 基线是 **0.1.1 已发布态**：`git diff --stat v0.1.1 -- src` 为空（D-148）；`build/libs` 里此刻有 `farmstorm-0.1.1+mc1.21.1.jar`。
- [ ] 手边这四份：34 号 §4.1 的 22 条品质表与 §4.3 的派生集合；36 号 §2.1（第 9 字段位置、名色表、tooltip 行）与 §4.3.2 第 4 ~ 7 行；36 号 §1.3 的基线（`tooltip.farmstorm` **16** 条、`textures/item` **29** 张、`data_components` **1** 个）；35 号 §2.2 / §2.5。
- [ ] 版本号只有一处来源：`gradle.properties:11`。两支 `.cmd` 是**产物面的载体**（`一键构建.cmd:11-12`、`检查环境.cmd:14`，A-65），banner 不写版本串——0.1.1 期已统一，本节不要顺手加。
- [ ] 冻结面：`textures/item` 的 **29** 张 PNG、`models/item/*.json`、`FarmstormEntityTypes` 的 `clientTrackingRange(4)` / `updateInterval(10)`、注册面（仍 **93**）——四条 `git diff` 必须为空。
- [ ] 上一 Phase：**本节是第一节**，没有上一节；它的出口判据就是 §2 的前置。

### 1.2 本阶段目标

把「版本号」与「品质」两件事钉死：前者是全部产物的名字来源（F-213），后者是交易 / 培养器准入 / 幸运浆果池三处的**唯一数据源**（F-214）；再让 29 个物品名按档着色、tooltip 多一行（F-215）。

本节结束时世界多了两个能力：产物名与版本号一致；22 条作物从「一堆同色 id」变成「三档可读、可按档筛选的集合」。而贴图、堆叠、配方、注册面一个都没动。

### 1.3 任务清单（F-213 ~ F-215）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-213 | 版本号 0.2.0 与两支脚本的产物名 | S | 0.5 | — | `gradle.properties:11` 的 `mod_version` 改 `0.2.0`；`一键构建.cmd:11-12`（含 `-sources.jar` 那行）与 `检查环境.cmd:14` 的 jar 名同步 |
| F-214 | 品质载体：`CropTier` + `CropProfile` 第 9 字段 + 22 条档案 | L | 3.0 | — | 新建 `util/CropTier.java`；`util/CropProfile.java` 的 record 头 `:32-40`、22 条档案 `:47-128`、三个私有工厂 `:191-207` 一次改完；`Farmstorm.java` 新增一行品质自检 |
| F-215 | 品质视觉：名色 + tooltip 一行 + 4 条语言键 | M | 1.5 | F-214 | 三个物品类各覆写 `getName`；`appendCropTooltip` 末尾追加一行；两份 lang 各 +4 条 `tooltip.farmstorm.tier*` |

阶段小计：**3 个任务、5.0 人日**（S 1 / M 1 / L 1）。

### 1.4 出口判据

- [ ] **产物名与版本行（F-213）**：怎么验：`.tools\gw.cmd build` 后 `jar tf build/libs/farmstorm-0.2.0+mc1.21.1.jar` 读 `META-INF/neoforge.mods.toml`。通过的样子：退出码 **0**；主 jar 与 `farmstorm-0.2.0+mc1.21.1-sources.jar` 同时在位；`build/libs` 里 `farmstorm-0.1.1*` 与 `farmstorm-0.1.0*` 残留 **0** 个；`version` 行是 `0.2.0`。
- [ ] **三处字面量一次改齐（F-213 / A-65）**：怎么验：两个 `grep -c` + `git diff gradle.properties`。通过的样子：`farmstorm-0.1.1` 在 `一键构建.cmd` 与 `检查环境.cmd` 各 **0** 命中；`farmstorm-0.2.0+mc1.21.1` 分别 **2** / **1** 命中；`git diff gradle.properties` 只有第 **11** 行一行变化。
- [ ] **record 契约一次改完（F-214）**：怎么验：`grep -c "new CropProfile("` + 通读三个工厂。通过的样子：**7** 处直接构造，每处实参表末尾都有 `CropTier.` 实参；`crop` / `throwable` / `deployed` 三个私有工厂各多一个 `CropTier` 形参并原样转发；`Farmstorm.java:112-113` 的 `22 / 12 / 7 / 3 / 2` 五个数一个不动。
- [ ] **逐档点数（F-214 / D-149）**：怎么验：数档案区 `CropTier.` 的次数 + 读启动日志。通过的样子：`CropTier.COMMON` **11**、`CropTier.RARE` **7**、`CropTier.EPIC` **4**（枚举声明与工厂形参不计）；日志出现 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`。
- [ ] **名色与 tooltip（F-215）**：怎么验：三档各取一个放背包、开 tooltip、再用铁砧改一次名。通过的样子：普通白 / 稀有蓝 / 史诗金；tooltip 在既有类别行**之后**多一行「品质：稀有」；`DeployableCropItem`（**7** 个）与 `SeedItem`（**7** 个）也变色——不是只有 `ModifiedCropItem`；铁砧改名后的颜色按 36 号 §9 ⑥ 记录现象，本节不另写实现。
- [ ] **语言键只增 4 条（F-215，总数不在本节）**：怎么验：两份 lang 的键集。通过的样子：各新增 **4** 条 `tooltip.farmstorm.tier*`（`.common` / `.rare` / `.epic` 三条 + 行格式一条）；`tooltip.farmstorm` 前缀 **16 → 20** 条；**不写总数**——`ModLangProvider.java:13-16` 的类注释一字不动（总数在 F-241）。
- [ ] **注册面与贴图不动（负向）**：怎么验：`git diff --stat textures/` + 起服读自检行。通过的样子：`textures/item` 仍 **29** 张 PNG，逐张 sha256 与提交前相同；`自检 注册对象 93 个：… → OK`，12 类分项与 0.1.1 相同（`menu_types` 仍 **1**）。

### 1.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| record 分几次提交 | 7 处直接构造与 15 处工厂实参漏一处即编译失败，回滚面从 1 个提交变成 3 个 | F-214 一个提交：`new CropProfile(` 7 处 + 三个工厂 + 22 条 `tier` 全在同一个 `git diff` 里 |
| 顺手改 8 条「种子 = 自身」的 `seedItem` | 那 8 条（`blast_carrot` / `ubi_server` / `toxic_ubi_server` / `fang_trap_cactus` / `bamboo_firecracker` / `red_mushroom_puff` / `brown_mushroom_puff` / `lucky_berry`）指向自己；一改就被 F-236 的布尔式判成「有独立种子」，产出从 2 个变 2 + 2 个，`5 给 2+2 / 13 只给 2` 的账当场作废 | 本节只补第 9 字段的档位；`seedItem` 的 diff 必须为空 |
| 名色只改父类 | `DeployableCropItem`（7 个）不继承 `ModifiedCropItem`，`SeedItem`（7 个）另立一支 ⇒ **14** 个物品仍是白名 | 三个类各自覆写；`grep -c "getName"` 在三个文件各 ≥ **1**；背包里逐个过 |
| 档位写成第二份 id 清单 | 交易 / 准入 / 池三处各自维护列表，改一条 `tier()` 后三处不同步（RT-49 的原始症状） | 只允许 `CropProfile` 一处定义；下游一律读 `tier()`（A-63 对池的要求同一条） |
| 顺手「修好」既存笔误 | `CropType.java:17,19,21` 的 8 / 5 / 2 与 `SeedItem.java:13` 的「6 个」被改，diff 里出现与本任务无关的行 | 34 号 §8 第 4 条点名两条，**一条都不改**；本节 `git diff --stat` 只允许 **10** 个文件 |
| 提前开注册窗口 | 顺手注册点东西为 F-224 预备，`自检 注册对象` 从 93 变 94，§2 / §3 的 `→ OK` 全部作废 | 本节注册读数必须仍是 **93**；`registry/` 四个文件的 diff 为空 |
| 顺手改语言键总数注释 | `ModLangProvider.java:13-16` 被改成 327，到 F-241 已无基线可对 | 注释一字不动；本节只验「4 条键存在 + 前缀 16 → 20」 |

### 1.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；F-213 的三处字面量、F-214 的 record 头 + 7 处构造 + 三个工厂 + 22 条档案、F-215 三个类各几行 + 4 条键的位置（文件:行号）。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`jar tf` 的两组条目；两个 `grep -c` 的读数；`git diff gradle.properties` 的行数。
- **观察到什么**：`自检 作物品质` 与 `自检 作物` 两行原文；逐档 11 / 7 / 4 的点数；`tooltip.farmstorm` 16 → 20；29 张 PNG 的 sha256 清单与提交前的比对结果。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如铁砧改名后的档色只有单人现场可看）。
- **未关闭的问题**：例如 36 号 §9 ⑥ 的两条待验证（`CUSTOM_NAME` 是否压过档色、rarity 包装是否覆盖子组件颜色）——本节只取证、不结论。

---

## 2. Phase 33 · 规则面收窄（M29）

### 2.1 前置

- [ ] 上一节 **§1.4 的第 1 / 3 / 4 条**已绿：产物名与版本行（`0.2.0`）、record 契约一次改完、逐档 11 / 7 / 4。
- [ ] 日志里已有 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`；`自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK` 一行保持不变。
- [ ] 手边这四份：35 号 §3（两条路径逐条对照）、§5（池 19 → 11 与被移出的 8 条）、§9.2 / B-35；36 号 §2.2（路径① / ② 的写法与负向清单）、§2.9；34 号 D-152 / D-155 / D-156 / D-170、裁定 #19；37 号 A-63。
- [ ] 冻结面：22 条档案的档位与第 9 字段在 F-214 已冻结，本节不改；`FarmstormCropBlock` 不动（`mayPlaceOn` / `canSurvive` 本来就没覆写）；`ModifiedCropItem.java:53-55` 的 `canPlantAt` 全仓 0 调用者，**不删**。
- [ ] 换算基数就在手边：`19 − 8 = 11`（移出的是池内 **6** 稀有 + **2** 史诗）；准入面的 `18 = 11 + 7` 与本节无关，别拿来当判据。
- [ ] 注册窗口尚未打开：本节仍是 `total = 93 / → OK`（F-224 才开）。

### 2.2 本阶段目标

把四条「玩家可感知的规则变化」各自独立落地：浆果池收窄（F-216）、两条播种路径各堵一次（F-217 / F-218）、转换器停功能（F-219）。四条互不依赖，可以分四天做，但**验收必须分开写、不合并**。

本节结束时：吃幸运浆果只滚 11 条普通作物；15 个有作物方块的作物与 7 个种子在耕地上都没有反应且不消耗；7 个部署型物品照常摆出；右键转换器什么都不发生，而方块、29 条配方、创造栏条目全在。

### 2.3 任务清单（F-216 ~ F-219）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-216 | 幸运浆果池 11 与断言同提交 | S | 0.5 | F-214 | `util/CropProfile.java:139-143` 的池改「只留 `tier() == COMMON`」；`Farmstorm.java:117-123` 的断言 `19 → 11` 与日志文案同一次提交改 |
| F-217 | 种植路径①：`isPlantableSoil` 恒 `false` | S | 0.5 | — | `item/ModifiedCropItem.java:101-107` 的判据体改返回 `false`，原「是 `FarmBlock` 才允许」留成注释；调用点 `:72` 不动 |
| F-218 | 种植路径②：`SeedItem#useOn` 返回 `PASS` | S | 0.5 | — | `item/SeedItem.java` 新增 `useOn` 覆写返回 `InteractionResult.PASS`；切断 `ItemNameBlockItem` → `BlockItem#useOn` → `place()` |
| F-219 | 转换器 `useWithoutItem` 返回 `PASS` | S | 0.5 | — | `block/ConverterBlock.java:78-86` 的方法体整体替换为返回 `PASS`；`getMenuProvider`（`:70-76`）与掉落（`:88-96`）保留 |

阶段小计：**4 个任务、2.0 人日**（S 4 / M 0 / L 0）。

### 2.4 出口判据

- [ ] **池 11 与断言同提交（F-216）**：怎么验：读启动自检行 + `grep`。通过的样子：日志 `自检 幸运浆果池 11 种…→ OK`；池的 11 条 `tier()` 全是 `COMMON`；`== 19` 在 `Farmstorm.java` 命中 **0** 处；「不含 `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`」的另半条（三个 `!=`）仍在（36 号 §8 #35）。
- [ ] **只滚普通（F-216 / D-156）**：怎么验：吃 `lucky_berry` 直到命中「生成随机作物」分支，逐次记产出的 id（抽样次数写进记录）。通过的样子：全部产出落在 11 条普通集合内；`wild_apple` / `lucky_berry` / `compressed_puffed_wheat` 各 **0** 次。
- [ ] **路径①（F-217）**：怎么验：手持 **15** 个有作物方块的作物物品逐一右键耕地。通过的样子：方块不出现、手持物不减、无提示；`isPlantableSoil` 的可执行体里没有 `instanceof FarmBlock`（只剩注释），方法仍被 `ModifiedCropItem.java:72` 调用 **1** 处。
- [ ] **部署型不误伤（F-217 / 35 号 B-02）**：怎么验：**7** 个部署型物品（`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` / `zangief_cane` / `point_defense_fungus`）右键完整方块顶面。通过的样子：照常摆出部署形态——切断的是「种作物」，不是「摆方块」。
- [ ] **路径②（F-218）**：怎么验：**7** 个种子逐一右键耕地、再右键泥土。通过的样子：方块不出现、种子不减、无提示；`SeedItem.java` 里 `InteractionResult.PASS` 命中 **1** 处、`useOn` 命中 **1** 处；`appendHoverText` 仍在（tooltip 不回归）。
- [ ] **转换器（F-219）**：怎么验：空手右键一次；再数资源与注册。通过的样子：无界面、无提示、手持物不减；`data/farmstorm/recipe/` 的 json 计数 **29**（**26** `converting` + 2 shaped + 1 shapeless）；`FarmstormMenus.java` 的 `REGISTER.register` 仍 **1** 处。
- [ ] **注册面与 config 不动（负向）**：怎么验：起服读自检行 + 读 `run/config/farmstorm-server.toml`。通过的样子：`自检 注册对象 93 个 … → OK`（本节 4 个任务都不进注册面）；toml 仍 **166 键 / 58 段**、`[outpost_core]` 仍 **3** 键；`config/FarmstormConfig.java` 的 diff 为空。

### 2.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 只堵一条播种路径 | 7 个种子仍能在耕地上种出作物（RT-48 的原始症状） | F-217 / F-218 **各一条验收、不合并**；两条路径的类与调用点完全不同，各堵一次 |
| 顺手删 `canPlantAt` 死代码 | 与 D-152 补充冲突，diff 里多出与本任务无关的一段 | `ModifiedCropItem.java:53-55` 一字不动；「删死代码」不在本版 |
| 池写成手写 11 条 id | 改一条 `tier()` 后池不跟，档位与池形成两份真相（RT-49 的防线当场失效） | 只留 `tier() == COMMON` 的过滤式（A-63）；不写第二份 id 清单 |
| 只改池不改断言（或反之） | 每次启动一条常红 FAIL；服务端照常起，但真故障会被这条噪声盖住（RT-50 的更正口径） | 池与断言**同一提交**；断言值与日志文案两处都改 |
| `PASS` 写成 `FAIL` / `SUCCESS` | `FAIL` 让调用方以为被拒绝，`SUCCESS` 会吞掉右键 | 两处都是 `InteractionResult.PASS`；两个文件各 `grep -c` 一次 |
| 转换器顺手删屏幕或菜单注册 | `menu_types` 1 → 0，注册面 93 → 92，F-228 的 97 永远对不上 | `FarmstormMenus` / `ConverterScreen` 不动；29 条配方不动 |
| 把「26 条配方」写成整目录计数 | 35 号 B-35 说的是 26 条 `converting`，目录里其实有 **29** 个 json，数错就判红 | 两个数分开写：26 = `converting` 子集，29 = 全量 |

### 2.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；四条各自的文件与行号（`CropProfile.java:139-143`、`Farmstorm.java:117-123`、`ModifiedCropItem.java:101-107`、`SeedItem.java` 新增的覆写行、`ConverterBlock.java:78-86`）。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；两个 `grep -c`（`InteractionResult.PASS`、`== 19`）；`ls data/farmstorm/recipe/*.json` 的计数；起服一次读自检行。
- **观察到什么**：池的 11 条 id 与它们的 `tier()`；抽样记录（次数 + 产出 id 全表）；15 + 7 + 7 三组右键结果各一行；转换器的四场景（空手 / 手持种子 / 创造栏 / 配方计数）。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如没有第二客户端做「创造模式能否种」的对照，B-04 留给 F-245）。
- **未关闭的问题**：例如 `LuckyBerryRoller.java:170` 的 javadoc「14 种池」是既存笔误（34 号 §8 第 8 条），本节不改也不引用。

---

## 3. Phase 34 · 耕地停用与交易（M30）

### 3.1 前置

- [ ] 上一节 **§2.4 的第 1 / 3 / 5 条**已绿：池 11 与断言同提交、路径①、路径②。§2 的第 2 / 4 / 6 / 7 条不构成本节前置，但必须在 §8 之前补绿。
- [ ] §1.4 的**第 3 / 4 条**已绿：`tier()` 可读、逐档 11 / 7 / 4——交易按档抽，没有它落不了地。
- [ ] fixture 两份在手：① 0.1.1 老档（`world/data/farmstorm_farmland.dat` 在位，用于 A-69 的时点核对）；② 一个可升级的农民村民（旁边有床与堆肥桶，能自然升级到 1 / 3 / 5 位次）。
- [ ] 手边这四份：34 号 D-153 / D-154 / D-157 ~ D-159、裁定 #3 / #18；35 号 §7.2（七处落点）、§7.4（玩家侧观感）、§4.2（交易表）、§4.5（断货与补货）；36 号 §2.3（注释清单与「三处必须保留」）、§2.4（交易五步）；37 号 A-62 / A-68 / A-69。
- [ ] **A-62 的口径以本节为准**：`FarmstormConfig.java:227` 的字段**保留**，只注释 `:909-912` 的定义。34 号 §3.3 第 5 行与 35 号 §7.2 落点 6 写的是「字段与定义一起注释」——那两处上游未回灌，**按 A-62 执行**。
- [ ] **A-69 的时点**：本节**不许**把「`farmstorm_farmland.dat` mtime 不变」写成判据。F-220 注释掉两个监听器与一行 backfill 之后，拆核仍会读写它（`settleOnRemoval` 是最后读者，F-233 才整段改写）。
- [ ] 注册窗口的边界：本节仍在**窗口前**（`total = 93 / → OK`），F-224 才开窗。现状 toml 是 **166 键 / 58 段**、`[outpost_core]` **3** 键、`farmstorm-client.toml` **3** 键不动。

### 3.2 本阶段目标

把耕地体系在运行期清零（不删代码、不删存档、不删方块），同时建起「史诗档的唯一入口」——农民交易。

本节结束时：锄地不拦、踩踏不保护、两条动作栏提示不再出现；`/farmstorm` 仍不存在（0.1.1 已删，本版不恢复）；`farmstorm-server.toml` 里 `[outpost_core]` 少了 `farmlandBonus`、多出 `[trade]` 的 6 键；农民的交易列表出现 3 个本模组项，按等级位次 1 / 3 / 5 依次解锁，每项只收绿宝石、只卖一次。

### 3.3 任务清单（F-220 ~ F-223）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-220 | 耕地体系停用：5 处注释 + 键定义 + 字段保留 | M | 1.5 | — | `event/FarmstormGameEvents.java:75` / `:76` 的注册行、`:241-265` / `:277-281` 的实现、`:171` 的 backfill 单行共 5 处注释；`FarmstormConfig.java:227` 字段保留 + `:909-912` 定义注释 |
| F-221 | 3 条耕地语言键停用 | S | 0.5 | F-220 | `datagen/ModLangProvider.java:403`、`:412-413` 三条条目从 `ENTRIES` 移除；`:411` 的段注释一并处理 |
| F-222 | `trade` 组 6 键 + 6 条 config 语言键 | M | 1.5 | F-220 | 新建顶层组 `trade`：`common` 2 / 5、`rare` 10 / 32、`epic` 40 / 64，值域全 1–64；6 条 `farmstorm.configuration.trade.*` |
| F-223 | 农民交易 3 项：`VillagerTradeEvents` + `TierTrade` | L | 3.0 | F-222 | 新建两个 `event/` 类；订阅 `VillagerTradesEvent` 只处理 `FARMER`，往 `get(1)` / `get(3)` / `get(5)` 各加一项，`maxUses = 1`、`xp = 2`、`priceMultiplier = 0.0F` |

阶段小计：**4 个任务、6.5 人日**（S 1 / M 2 / L 1）。

### 3.4 出口判据

- [ ] **五处注释都带理由行（F-220 / D-153）**：怎么验：`grep` 五处 + 逐处读上方一行。通过的样子：`:75`、`:76`、`:241-265`、`:277-281`、`:171` 全处于注释态（共 **5** 处）；每处上方各 **1** 行「裁定 #3：v0.2.0 撤销耕地，勿恢复」。
- [ ] **三处必须保留（F-220 / 35 号 §7.3）**：怎么验：读 `onServerTickPost` 全段 + 看浮窗对账日志。通过的样子：`onServerTickPost` 未被注释；方法体里 `CoreChunkTickets.restoreAll(server)`（`:170`）与 `CoreListBroadcaster.tick(server)`（`:175`）都在；浮窗每 **100** 刻的对账日志仍在打（36 号 §8 #10）。
- [ ] **A-62 的两面（F-220）**：怎么验：读 `FarmstormConfig.java` 两处 + 数两个类的行数。通过的样子：`:227` 的字段声明仍在、`:909-912` 的定义已注释、字段旁 **2** 行注释写明「恢复监听器前必须先恢复定义，否则 `FarmlandGuard.java:75` 抛 NPE」；`FarmlandLedger.java` **555** 行、`FarmlandGuard.java` **225** 行逐字未变。
- [ ] **config 的两级读数（F-220 / F-222）**：怎么验：删旧 `farmstorm-server.toml` 起服两次，各读一次全文。通过的样子：F-220 后 `[outpost_core]` **2** 键（`maxPerPlayer` / `boundaryParticleIntervalTicks`）、全文 **165 键 / 58 段**；F-222 后多出 `[trade]` **6** 键（2 / 5 / 10 / 32 / 40 / 64）、全文 **171 键 / 59 段**。
- [ ] **三个总数注释不动（滞后窗口，总数在 F-242）**：怎么验：读 `FarmstormConfig.java` 的 `166` / `58` 两处叙述与 `ModLangProvider.java:13-16` 的类注释。通过的样子：两处都一字不动；本节只写「本条键存在 / 不存在」与上一条的 toml 区间读数。
- [ ] **3 条耕地键消失（F-221）**：怎么验：`ConvertFrom-Json` 后查两份 lang 的键集 + `grep` 生成器。通过的样子：`farmstorm.configuration.outpost_core.farmlandBonus`、`message.farmstorm.farmland.no_core`、`message.farmstorm.farmland.quota_full` 在两份 lang 与 `ModLangProvider.java` 里都 **0** 命中；`message.farmstorm` 前缀 **0** 条。
- [ ] **交易三项（F-223）**：怎么验：打开农民交易界面 + `/data get entity <villager> Offers.Recipes`。通过的样子：出现 **3** 个本模组项，等级位次 **1 / 3 / 5**；三项 `maxUses` = **1**，`buy.count` 分别落在 **[2,5] / [10,32] / [40,64]**；每项 `sell` 是 **2** 个同档作物。
- [ ] **断货与不重掷（F-223 / D-158 / D-159）**：怎么验：成交一次后连续观察两次（补货前后）。通过的样子：该项仍在列表、变灰不可点；补货后价格与作物 id 不变（`priceMultiplier = 0.0F` 锁死基准价）；老档满级农民**不补发**（35 号 B-33，本版不做迁移，不判失败）。
- [ ] **A-69 的时点（本节最容易越界的一条）**：怎么验：通读本节验收记录 + 查一个引用点。通过的样子：记录里「`farmstorm_farmland.dat` mtime 不变」的断言 **0** 条；改为一条可观察的替代——`OutpostCoreBlockEntity.java:353` 的 `FarmlandLedger.of` 仍在该方法里（F-233 才删），并在记录里写明「本节之后拆核仍会重写该文件」。

### 3.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 按 34 / 35 号把字段一起注释 | `FarmlandGuard.java:75` 与 `FarmlandLedger.java:305` 的 `.get()` 编译不过，两个「一行不改」的类被迫改 | 只注释 `:909-912` 的定义；`:227` 字段保留（A-62）；两个类的 `git diff` 必须为空 |
| 只注释注册行、不注释实现 | 留下引用两个保留类的死代码；反过来只注释实现则 `addListener` 的方法引用断掉、编译失败 | 两个注册行 + 两段实现 + 一行 backfill **同一个提交**；`onServerTickPost` 保留 |
| 顺手把 `restoreAll` 或 `tick` 一起注释 | 浮窗包停发、核心区块票不恢复——两条都是别的东西 | 只注释 `:171` 一行；`:170` / `:175` 一字不动；日志每 100 刻的对账是判据 |
| 在 F-233 之前断言 `.dat` 不变 | 拆核会重写该文件，判据必红，现场看起来像「文档写错了」 | 本节只登记「拆核仍读写」；mtime 判据留给 §5 第 7 条与 §8 第 7 条 |
| 交易等级位次写成 2 / 4 | 交易项一个都看不到（RT-46） | 位次是 **1 / 3 / 5**；先只挂一项起服确认能看到，再改成三项 |
| 价格在成交时重掷 | 同一笔报价点一次一个价 | 抽签与定价都在首次报价生成时完成；补货后价格与 id 不变 |
| `trade` 写成 `outpost_core` 的嵌套组 | 组数算成 61，F-242 的 `b.push(` = 60 永远对不上 | `trade` 是**顶层组**，与 `outpost_core` 同级；`[trade]` 恰 6 键 |

### 3.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；F-220 的 5 处注释 + `:227` 的两行注释、F-221 的三条删除、F-222 的 6 字段 + 6 条键、F-223 两个新类与 `Farmstorm.java` 的挂载行。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；两次删 toml 起服；`grep -c` 三处（`farmlandBonus`、`message.farmstorm`、`REGISTER.register`）；`git diff --stat` 对两个保留类。
- **观察到什么**：五处注释的 `grep` 命中；`onServerTickPost` 的 :170 / :175 原文；两次 toml 全文读数（165 / 58、171 / 59）；交易三项的 `maxUses` / `buy.count` / `sell` 各一行；成交与补货前后两帧。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如老档满级农民需要真实老档，只有构造档时写明构造方式）。
- **未关闭的问题**：例如 `getTrades().get(1)` 的 list 是否可变（36 号 §9 ① 的待验证，退路是 `put` 一个新的 `ArrayList`）。

---

## 4. Phase 35 · 注册面、资源与容器骨架（M31）

### 4.1 前置

- [ ] 上一节 **§3.4 的第 1 / 6 条**已绿：五处注释各带理由行、3 条耕地键在两份 lang 里 0 命中。
- [ ] `[outpost_core]` 已无 `farmlandBonus`（否则 F-226 的 toml 读数会多 1 键，172 变 173）。
- [ ] 材质源文件在手：三张帧条（`cultivator_a_top` / `cultivator_a_side` / `cultivator_a_bottom`，**名字不许改**）与两张静态首帧（`editable_frames/side_00.png` / `top_00.png`），共 **7** 个文件落 `textures/block/`。
- [ ] **注册自检窗口在本节打开**：开工时日志是 `自检 注册对象 93 个 … → OK`；F-224 落地后 **96 / FAIL**，F-227 落地后 **97 / FAIL**，F-228 才 **97 / OK**。
- [ ] 窗口纪律四条（37 号 §0.3）：① 不许把 `→ OK` 写成本节任何一条判据；② 其它四条自检行必须仍绿；③ 关窗任务不夹带别的改动；④ 语言键与 config 的总数注释不动。
- [ ] 本节有两件「全项目第一次」，都按上一档估工：① 全仓第一个 `*.png.mcmeta`（RT-47）；② 第一次把「非容器 BE」升级成容器（RT-44）。两件都要求「先写最小用例跑通、再写正式逻辑」。
- [ ] 上一 Phase 的 **§3.4 第 1 / 6 条**必须已绿（即本节第 1、2 条前置）；§3 的第 7 / 8 条（交易三项、断货与不重掷）不构成本节前置，但必须在 §8 之前补绿。

### 4.2 本阶段目标

把本版全部 **4** 个注册增量落地（培养器三件套 → 核心菜单），配套 12 个资源文件、容器骨架与菜单骨架，最后一次性关掉注册自检窗口。

本节结束时：`farmstorm:cultivator` 拿得到、放得下、有双态模型与战利品表；核心变成 54 格容器并能存物品；菜单与屏幕已注册（但界面还开不了，F-229 才接）；自检行从 `93 → OK` 走完 `96 / FAIL`、`97 / FAIL` 到 `97 → OK`。

### 4.3 任务清单（F-224 ~ F-228）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-224 | 培养器注册三件套 + 创造栏 | L | 3.0 | — | 新建 `block/CultivatorBlock.java` 与 `blockentity/CultivatorBlockEntity.java`（骨架）；三个 registry 各 +1；`creativeTabItems()` 加一项；**窗口开** |
| F-225 | 培养器资源、双模型与 datagen 三处 | L | 3.0 | F-224 | 7 个材质文件 + 双模型 + 两态 blockstate + item 模型 + 战利品表；`usesBlockModel` 加 `CultivatorBlock`；`getKnownBlocks()` 加一条 |
| F-226 | 核心改容器：54 格 + `Items` + 两个同步方法 | L | 3.0 | — | `OutpostCoreBlockEntity` 实现 `Container`（54 格）+ NBT `Items` + `getUpdateTag` / `getUpdatePacket` + 老档兼容读；新增 `outpost_core.storageSlots` |
| F-227 | 核心菜单 + 屏幕 + `menu_types` +1 | L | 3.0 | F-226 | 新建 `menu/OutpostCoreMenu.java` 与 `client/OutpostCoreScreen.java`；`FarmstormMenus` +1；屏幕注册 +1；**窗口最后一次注册增量** |
| F-228 | 注册自检 97 收口（关窗） | S | 0.5 | F-224、F-227 | `Farmstorm.java:147-150` 的 5 个常量一次改齐（93 → 97、32 → 33、26 → 27、7 → 8、1 → 2）+ 方法注释两处 93；**窗口关** |

阶段小计：**5 个任务、12.5 人日**（S 1 / M 0 / L 4）。

### 4.4 出口判据

- [ ] **窗口状态 A（F-224）**：怎么验：起服读 `run/logs/latest.log`。通过的样子：`自检 注册对象 96 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → FAIL`。**FAIL 是窗口期的正确值**，不许改成 OK，也不许改断言常量让它变绿。
- [ ] **培养器可获取（F-224 / A-64）**：怎么验：`/give`、`/setblock`、`/data get block`、开创造栏。通过的样子：`farmstorm:cultivator` 拿得到、放得下、有方块实体；创造栏「工具方块」段末尾多 **1** 项；`FarmstormItems.java` 里 `cultivator` 命中 ≥ **2**（注册 + 创造栏）。
- [ ] **资源计数（F-225）**：怎么验：数 `src/generated/resources` 与 `textures/block`。通过的样子：`blockstates` **26 → 27**、`models` **103 → 106**、`loot_table` **26 → 27**、`*.png.mcmeta` **0 → 2**、`textures/block` 新增 **5** 张 PNG（3 帧条 + 2 静态首帧）。
- [ ] **双态与模型（F-225 / RT-47）**：怎么验：读三个 json + 两个 mcmeta。通过的样子：`blockstates/cultivator.json` 恰 **2** 个 variant（`working=false` / `true`），后者模型是 `farmstorm:block/cultivator_working`；`models/item/cultivator.json` 的 parent 是 `farmstorm:block/cultivator`（出现 `item/generated` 或 `layer0` 就是 `usesBlockModel` 漏改）；两个 mcmeta 逐字节相同、`frametime 2`、`frames 0..15`，两张 `_still` **不带** mcmeta。
- [ ] **容器面（F-226 / RT-44）**：怎么验：`/data get block <core>` + 老档升级 + 人工核对一次 `getUpdateTag`。通过的样子：出现 `Items` 键（0 条时为空表）；老档（0.1.1 存的核心）升级后读成 **54** 个空槽、无异常栈；`getUpdateTag` 的返回体只有 owner 三键，不写 `Items`、不写绑定。
- [ ] **窗口状态 B（F-227）**：怎么验：起服读日志 + 数两个注册点。通过的样子：`自检 注册对象 97 个：… / menu_types 2 → FAIL`——数字全对、断言常量仍是 93；`FarmstormMenus.REGISTER.register` **2** 处；`FarmstormClient.java` 里 **2** 行 `event.register(...Screen::new)`；此时游戏内**还开不了**界面（F-229 才接，预期中间态）。
- [ ] **关窗（F-228）**：怎么验：真起一次服读整行 + 查 `93` 的残留 + 读 diff。通过的样子：`自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`；`93` 在 `Farmstorm.java` 命中 **0**、`97` ≥ **1**；`git diff` 只动 5 个常量与方法注释两处 93，**13** 参数格式串一个字不改。
- [ ] **窗口的负向四条（F-224 ~ F-227 任一次起服）**：怎么验：同一次日志里读另外四行。通过的样子：`自检 作物 22 种…`、`自检 作物品质 普通 11 / 稀有 7 / 史诗 4`、`自检 幸运浆果池 11 种…`、`自检 网络包注册` 四条仍 `→ OK`——它们任何一条红都是真故障，不是窗口。
- [ ] **另外两个数没被顺手改（滞后窗口）**：怎么验：读 toml 与两处总数注释。通过的样子：`run/config/farmstorm-server.toml` 在 F-226 之后 **172 键 / 59 段**、`[outpost_core]` **3** 键（`maxPerPlayer` / `boundaryParticleIntervalTicks` / `storageSlots`）；`ModLangProvider.java:13-16` 与 config 总数注释一字不动（总数在 F-241 / F-242）。

### 4.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 窗口内写了 `→ OK` | 后面每次起服都带着一条常红噪声，真故障被盖住（RT-50 的更正口径：只写日志、不 throw） | 本节只写 FAIL 行的实际分项（96 / 97）；唯一写 OK 的是第 7 条（关窗任务自己的判据） |
| 为了让自检变绿提前改 5 个常量 | F-224 之后日志变 OK、但 `menu_types` 还差 1，F-227 时又红一次，窗口形同虚设 | 5 个常量只在 F-228 改，且该提交不夹带别的改动 |
| `usesBlockModel` 不加 `CultivatorBlock` | item 模型落到 `basicItem`，去要一张不存在的 `textures/item/cultivator.png`，游戏里是紫黑格 | 三条一起查：`models/item/cultivator.json` 的 parent、`usesBlockModel` 的判定、`textures/item` 里没有新文件 |
| 只加 `add(...)` 不加进 `getKnownBlocks()` | 战利品表不生成，方块挖掉什么都不掉 | `ModLootTableProvider` 的手写清单 **26 → 27**；`loot_table` 目录计数同时看 |
| 把 `Items` 塞进 `getUpdateTag` | 每次区块下发多几 KB（RT-44 的正面回答就是「不塞」） | `getUpdateTag` 只写 owner 三键；人工核对 **1** 次 |
| 54 与 16 写反 | 54 是格子数、16 是培养器名额（裁定 #8）；写反一个整段验收就废 | 两个键名分开放：`storageSlots` 54、`cultivatorCapacity` 16（后者在 F-231，本节不建） |
| 把 `storageSlots` 的值域写成 1–64 | `ModConfigSpec` 接受了，但格数会变成别的值，界面版式与 `getContainerSize()` 两张皮 | 值域 **54–54**；若被拒，走 36 号 §9 ⑦ 的退路（`54..55` + 创建菜单时钳到 54），格数不许变 |
| 顺手注册培养器菜单 | `menu_types` 提前 +1，状态 A 变 97、状态 B 与关窗全对不上 | 培养器**无菜单**（右键直接放入）；`menu_types` 的 +1 只给核心存储 |

### 4.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；F-224 三个 registry 各一行 + 两个新类、F-225 的 12 个资源文件逐个点名、F-226 的类声明 / 字段区 / NBT / 两个同步方法、F-227 两个新类 + 两个注册点、F-228 的 5 个常量。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`runData` 的两次（F-225 一次、F-227 之后一次）；三次起服的日志（96 / 97 / 97）；五个目录计数（`blockstates` / `models` / `loot_table` / mcmeta / `textures/block`）。
- **观察到什么**：三次自检行的**完整原文**（含 FAIL 与 OK）；其它四条负向自检行；`/data get block <core>` 的 `Items` 读数；老档升级后的槽数；创造栏截图。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如动画在光影下的观感要客户端现场看，RT-47 的一部分留给 F-244）。
- **未关闭的问题**：例如 `getUpdateTag` 不带 `Items` 的取舍（36 号 §9 ④ 的待验证，退路是只放「非空槽数 + 首槽物品」，≤ 200 字节）。

---

## 5. Phase 36 · 核心交互、绑定与迁移（M32）

### 5.1 前置

- [ ] 上一节 **§4.4 的第 1 / 5 / 6 / 7 条**已绿：状态 A（96 / FAIL）、容器面、状态 B（97 / FAIL）、关窗（`97 … → OK`）。其中第 5 条（容器面）是 F-230 / F-231 的直接前置。
- [ ] **关窗已确认**：本节起可以引用 `→ OK`，但**不能**引用 `tools/verify_project.py` 的结论（A-66：它仍会打 `93 / 93` 并退出 0）。
- [ ] `menu_types 2`、`block_entities 8` 在位；§4 的「资源计数」（`blockstates` 27 / `models` 106 / `loot_table` 27）已绿。
- [ ] fixture：一个 0.1.1 老档核心（只有 owner 三字段）；两个核（用于「一个满 16、另一个有空位」的对照）；一个培养器（F-224 已落地）。
- [ ] 手边这四份：36 号 §2.6.1 ~ §2.6.4、§2.7（七步）、§6 ①②③；35 号 §8.2 ~ §8.5、B-12 ~ B-29；34 号 D-165 ~ D-168、裁定 #8 / #17；37 号 A-69。
- [ ] 冻结面：`Container.stillValidBlockEntity` 的原版距离（`4.0F`）不新设常量、不新增 config 键（裁定 #17）；`RANGE_CHUNKS = 1`（3×3 区块）只用于绑定与连线，**不是**开箱条件；`FarmlandLedger` / `FarmlandGuard` 一行不改。
- [ ] **A-69 的时点**：本节 **F-233 之后**才允许写「`.dat` mtime 不变」。第 7 条判据因此放在本节末尾执行——开工时先记一次 mtime，F-233 落地后再记一次。

### 5.2 本阶段目标

让核心成为「能开的箱子 + 能绑 16 个培养器的锚点 + 拆掉时会把培养器交出去」的完整载体。

本节结束：己方空手右键开 54 格、非己方开不了、9 格外开不了；挖掉整箱掉落，活塞 / 同方块 / 卸载都 0 掉落；核侧 `Cultivators` 记 16 条；培养器放下时绑最近的己方核、平手按 `placedGameTime`；拆核时改绑或留原地标未绑定（**不变泥土**），而 `.dat` 的最后一个运行期读者在这一节消失。

### 5.3 任务清单（F-229 ~ F-233）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-229 | 核心存储打开：两道闸 | M | 1.5 | F-227 | `block/OutpostCoreBlock.java` 新增 `useWithoutItem` 与 `getMenuProvider`；菜单 `stillValid` 用原版语义；非己方 `PASS` |
| F-230 | 核心破坏掉落 | S | 0.5 | F-226 | `block/OutpostCoreBlock.java:101-107` 的 `onRemove` 追加四条件掉落（`Containers.dropContents`） |
| F-231 | 核侧 16 条绑定列表 + `cultivatorCapacity` | M | 1.5 | F-226 | `OutpostCoreBlockEntity` 增 `Cultivators`（Long 列表，≤ 16）与绑定访问方法；新增 `outpost_core.cultivatorCapacity`（默认 16） |
| F-232 | 放置时绑定：四判据 + 两侧写入 | L | 3.0 | F-224、F-231 | `CultivatorBlock#setPlacedBy` 选核（`distSqr` + `placedGameTime` + 坐标序）；核侧列表与培养器侧 `Core` 都写 |
| F-233 | 迁移三出口：`settleOnRemoval` 整段改写 | L | 3.0 | F-232 | `OutpostCoreBlockEntity.java:341-394` 整段改写：真移除判定 → 复制列表 → 逐条选接替核 → 三出口 → 只清 `cultivators` → `release()` |

阶段小计：**5 个任务、9.5 人日**（S 1 / M 2 / L 2）。

### 5.4 出口判据

- [ ] **两道闸（F-229 / 裁定 #17）**：怎么验：己方与非己方各右键一次；再在 9 格与 8 格各试一次。通过的样子：己方开 54 格界面（6 行 × 9 列 + 玩家背包）、标题「据点核心」，存 3 个物品关掉再开仍在；非己方**不开界面**；**9** 格外发不出交互、**8** 格内打开后走到 9 格被原版 `stillValid` 关掉。
- [ ] **掉落四条件（F-230 / D-167）**：怎么验：四个场景各一次。通过的样子：核里 3 组不同物品被挖掉后 3 组全掉；活塞推动 **0** 掉落、`/setblock` 同方块 **0** 掉落、区块卸载后物品仍在核里；空核只掉 **1** 个方块本体。
- [ ] **绑定列表（F-231 / 裁定 #8）**：怎么验：`/data get block <core>` + 删旧 toml 起服 + 改容量。通过的样子：出现 `Cultivators`（绑 1 个后 **1** 条 long）；`[outpost_core]` **4** 键、全文 **173 键 / 59 段**；`cultivatorCapacity = 2` 时第 **3** 个培养器放得下但未绑定（35 号 §8.2 算例 2）。
- [ ] **放置绑定（F-232 / D-165）**：怎么验：四个对照。通过的样子：A 满 16、B 有空位 → 绑 **B**（两侧 `/data get block` 一致）；同刻两核等距 → 绑 `placedGameTime` 更小者，同一存档重跑结果相同；附近无己方核 → **缺 `Core` 键**、右键无反应；事后补核 → `Core` 仍缺失（不自动认领）。
- [ ] **迁移三出口（F-233 / D-168）**：怎么验：三种拆核场景。通过的样子：有接替核 → 改绑、`Held` / `Progress` 不变；无接替核 → 留原地、`Core` 键消失、内容保留、右键无反应、**不变泥土、不掉落**；区块不在内存 → NBT **一个字节不变**（`Core` 仍是旧核坐标）。
- [ ] **`setBlock` 与两个死类（F-233 / RT-43）**：怎么验：`grep` 方法体 + `grep` 引用。通过的样子：`settleOnRemoval` 方法体内 `setBlock` 命中 **0**；`FarmlandLedger` / `FarmlandGuard` 在本类的引用（含 import）归 **0**。
- [ ] **A-69 在这条判据之后才成立（F-233 之后执行）**：怎么验：开工记一次 `world/data/farmstorm_farmland.dat` 的 mtime，F-233 落地后再记一次。通过的样子：文件存在且两次 mtime 相同；本节之前的任何记录里没有这条断言（有就是越界）。
- [ ] **注册与窗口（负向）**：怎么验：起服读自检行。通过的样子：`自检 注册对象 97 个 … → OK` 不变（本节 5 个任务都不进注册面）；`menu_types 2`、`block_entities 8` 不变。
- [ ] **工具面（A-66）**：怎么验：本节若跑 `tools/verify_project.py`，只允许记录一条读数。通过的样子：`已核 registry 对象: 93 / 93` 被标注为「A-66：待 F-243 同步，此读数少核 4 个对象」；本节任何判据都不引用它的 `FAIL 0`。

### 5.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 在 `useWithoutItem` 里自己判距离 | 与原版 `stillValid` 双重判定，多一个魔数；8.5 格的观感被改掉 | 只判「己方」；距离交给菜单的 `stillValid`（裁定 #17） |
| 把 3×3 区块当成开箱条件 | 站在自家核旁边却打不开（35 号 B-23 的反面） | `RANGE_CHUNKS = 1` 只用于绑定与连线；开箱只走 `stillValid` |
| 漏 `!movedByPiston` | 活塞推动时原地刷一份物品，变成复制漏洞 | 四条件一起写；四个场景逐个验（3 组掉落 / 活塞 0 / 同方块 0 / 卸载保留） |
| 迁移时边遍历边改列表 | 改绑过程中跳过或重复处理同一个培养器 | 照 36 号 §2.7 步骤 4 先复制一份 `cultivators()`，再遍历 |
| 迁不动时写「变泥土」或「打掉培养器掉物品」 | 0.1.1 的耕地三出口被照搬进来（RT-43 的原始症状） | 出口②是「留原地 + 清 `Core` + 保留内容」；`setBlock` 在方法体内必须为 0 |
| 为清算加载区块 | 拆核时一次强制加载连锁一批区块（卡顿） | 全程 `getChunkNow`；读不到就当出口③，NBT 原值不动 |
| 先判「最近」再判「空位」 | 「最近的满、别处还有空位」被误拒，与「一核 16 个」的容量自相矛盾 | 候选集**先过滤**（覆盖 + 己方 + 未满）再取最近；35 号 §8.2 算例 1 是这条的对照 |
| 把 16 写进 `getContainerSize()` | 54 与 16 混用，界面只剩 3 行 | `getContainerSize()` 返回读 `storageSlots`（54）；16 只在 `Cultivators` |
| 拿窗口期的旧日志当 `→ OK` | §4 中途那两次 FAIL（96 / 97）或更早的 `93 / 93` 被当成通过证据；`menu_types` 其实已是 2，断言常量却停在 93 | 本节的 `→ OK` 必须是**关窗之后**新起的一次服；日志的行数与起服时间要对上 |

### 5.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；`OutpostCoreBlock.java` 的两处新增/改动、`OutpostCoreBlockEntity.java` 的字段区 / NBT / 绑定方法 / `:341-394` 整段改写、`CultivatorBlock.java` 与 `CultivatorBlockEntity.java` 的写入点。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；删旧 toml 起服一次；`grep -c`（`setBlock`、`FarmlandLedger`、`getChunkNow`）；两次 mtime 读数。
- **观察到什么**：54 格界面的槽数与标题；四个掉落场景的实际掉落清单（空清单写「空」）；`Cultivators` 的条数；改绑 / 留原地 / 不在内存三组的 `/data get block` 原文；`.dat` 的两次 mtime。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如「区块不在内存」需要停服后读 region 文件比对，写明工具与路径）。
- **未关闭的问题**：例如「绑定变化是否需要额外一次 `CoreListBroadcaster.requestAll`」（36 号 §9 ⑫ 的待验证，退路在 §7 的广播器里补）。

---

## 6. Phase 37 · 培养器行为（M33）

### 6.1 前置

- [ ] 上一节 **§5.4 的第 3 / 4 条**已绿：`Cultivators` 落盘（F-231）、放置绑定四判据（F-232）——后者是 F-235 的前置。
- [ ] §4 的「窗口状态 A」（`cultivator` 三件套）与「资源计数」已绿；`working` 属性与双态模型在位。
- [ ] `[outpost_core]` **4** 键的 toml 读数已取到（F-231 之后是 **173 键 / 59 段**）。
- [ ] 手边这四份：36 号 §2.5.2（状态机四步）、§2.5.3（四道闸）、§2.5.4（产出两处判据）、§2.5.6（破坏掉落）；35 号 §6.2 ~ §6.4、§6.8、B-08 ~ B-18；34 号 D-160 ~ D-164、裁定 #15；37 号 §6 表 F ⑤ ⑦ ⑩。
- [ ] 冻结面：准入按 `tier()` 判、**不按 id 白名单**（D-162）；`working` 走 blockstate 属性（D-161）；培养器 BE **不实现 `Container`**（36 号 §2.5.2）——所以 F-237 用 `NonNullList` 重载掉落。
- [ ] 滞后窗口：语言键（F-234 的 **2** 条）与 config（`[cultivator]` **2** 键）都只写「本条键存在」与区间读数，总数在 F-241 / F-242。

### 6.2 本阶段目标

让培养器从「一个能放下的方块」变成「按档准入、600 刻产 2 + 2、核满暂停、拆掉吐回」的完整机器。

本节结束：普通 / 稀有放得进，史诗（4 个作物 + 2 个史诗种子）拒绝；600 刻后核里多 2 个作物，5 条有独立种子的再多 2 个种子；核满时 `Progress == cycleTicks`、物品不落地不丢，腾出空间后从暂停处继续；挖掉掉自身 + 内部那一个。

### 6.3 任务清单（F-234 ~ F-237）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-234 | `cultivator` 组 2 键 + 2 条语言键 | S | 0.5 | F-224 | 新建顶层组 `cultivator`：`cycleTicks` 600（20–72000）、`yield` 2（1–64）；2 条 `farmstorm.configuration.cultivator.*` |
| F-235 | 状态机、四道准入闸与放入 | L | 3.0 | F-232、F-234 | 三个 NBT 键（`Held` / `Progress` / `Core`）+ 服务端每刻状态机 + 四闸合并一次判定 + `useWithoutItem` 的放入顺序 |
| F-236 | 产出与核满暂停 | L | 3.0 | F-226、F-235 | 600 刻结算一次，批次按档案决定（有独立种子 ⇒ 作物 × yield + 种子 × yield）；`acceptAll` 失败则停在 `cycleTicks` |
| F-237 | 培养器破坏掉落 | S | 0.5 | F-224 | `block/CultivatorBlock.java` 的 `onRemove`：四条件后用 `NonNullList` 重载掉内部那一个培养物 |

阶段小计：**4 个任务、7.0 人日**（S 2 / M 0 / L 2）。

### 6.4 出口判据

- [ ] **新组 2 键（F-234）**：怎么验：删旧 toml 起服读 `[cultivator]`。通过的样子：恰 **2** 键、值 **600 / 2**；全文 **175 键 / 60 段**——这两组是**区间读数**，`b.push(` 的逐组核对与「总数」结论只在 F-242 出。
- [ ] **准入四闸（F-235 / D-162）**：怎么验：普通、史诗、占槽、未绑定四种右键。通过的样子：放普通作物 → `Held` 有值、`Progress` 从 0 涨、F3 的 `working=true`；史诗 4 个作物与 2 个史诗种子 → 无反应、手持物不减；槽已占用再右键 → 无反应；未绑定的培养器右键 → 无反应。
- [ ] **进度持久（F-235 / B-17）**：怎么验：计时中途走远 200 格再回来；再停服重载。通过的样子：`Progress` 从原值继续、不重新计时、槽不清空。
- [ ] **产出分类（F-236 / 裁定 #15）**：怎么验：三种投料各跑一轮 600 刻。通过的样子：喂 `puffed_wheat` → 核里 **+2** 作物 **+2** 种子；喂 `ubi_server`（种子 = 自身）→ 只 **+2** 作物；喂 `chive_seeds` 与喂 `friend_chives` 产出**完全相同**。
- [ ] **核满暂停（F-236 / D-164）**：怎么验：把核的 54 格塞满再投料；然后清出 2 格。通过的样子：`Progress == cycleTicks` 且不再前进、物品不落地、不丢；清出 **2** 格后自动完成投递、不重新计时。
- [ ] **破坏掉落（F-237 / B-10）**：怎么验：三种挖法。通过的样子：槽里有作物时挖掉 → **1** 个方块 + **1** 个该作物；空槽 → 只 **1** 个方块；活塞推动 → **0** 掉落。
- [ ] **注册与两个总数不动（负向）**：怎么验：起服读自检行 + 读两处总数注释。通过的样子：`自检 注册对象 97 个 … → OK` 不变；本节新增的 2 条语言键只写「键存在」，`ModLangProvider.java:13-16` 与 config 总数注释一字不动。

### 6.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 按 id 白名单判准入 | `tier` 一改就漂，与「22 条只在一处定义」的防线冲突（D-162 明确否决） | 准入只读 `tier()`；`grep` 里不出现任何作物 id 的比较 |
| 每刻写一次 blockstate | 无谓的方块更新，客户端抖动 | 只在 `working` 真的变化时写，且带 `UPDATE_CLIENTS` |
| `working` 只做 BE 同步、不走属性 | 模型不切换，双贴图白做（D-161 要求走 blockstate） | 属性名 `working`；`blockstates/cultivator.json` 两个 variant 都验一次 |
| 用 `seedItem != null` 单条件判「有种子」 | 8 条「种子 = 自身」的档案产出翻倍（36 号 §2.5.4 明确否决） | 判据是 `seedItem() != null` **且**它不等于 `id()`；准入面内 **5** 条给 2 + 2、**13** 条只给 2 |
| 核满时重新计时 | 一次塞满就让 600 刻白跑，玩家以为机器坏了 | `Progress` 停在 `cycleTicks` 不清零；清出空间后从暂停处继续 |
| 逐件判「放得下」 | 只投进去一半，槽已清空 ⇒ 物品凭空少一半 | 按**整批**判（`canAccept` / `acceptAll`，F-226 已建） |
| 产出落地成 `ItemEntity` 或塞玩家背包 | 与「产出进核」的 plan 原话冲突（D-164 明确否决） | 只走 `acceptAll`；掉落面只有 F-237 那一条 |
| 用 `Container` 重载掉内部作物 | 培养器 BE 不是 `Container`，编译不过 | 用 `NonNullList` 重载；方块本体由战利品表掉，两处不重叠 |

### 6.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；`FarmstormConfig.java` 的新组与两键、`ModLangProvider.java` 的 2 条、`CultivatorBlockEntity.java` 的三个字段 / `serverTick` / `isUsable` / 完成分支、`CultivatorBlock.java` 的 `useWithoutItem` 与 `onRemove`。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；删旧 toml 起服两次；`/data get block <cultivator>` 与 `<core>` 各若干次；`grep -c` 三处（`tier()`、`seedItem()`、`NonNullList`）。
- **观察到什么**：`[cultivator]` 两键读数与全文 175 / 60；三种投料的产出清单（作物 / 种子各几件）；核满时的 `Progress` 读数与清空后的变化；三组挖法的掉落清单。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如 72000 刻的上界没有实测、核满暂停的恢复延迟只测了一次）。
- **未关闭的问题**：例如「暂停检测时机」的 1 秒恢复延迟是否可接受（36 号 §9 ⑤ 的待验证，退路是在核的 `setItem` / `removeItem` 里加一次唤醒回调）。

---

## 7. Phase 38 · 连线（M34）

### 7.1 前置

- [ ] 上一节 **§5.4 的第 5 条**已绿：迁移三出口（F-233）——它是 F-239 的前置，广播器的「核被拆」时机就挂在迁移与 `release()` 上。
- [ ] §6.4 的第 4 / 5 条已绿（产出与核满暂停）：广播器的「绑定变化」时机要能看到培养器侧状态真的在变。
- [ ] 环境：能同时起「单人客户端」与「专用服务器」；第二客户端（或第二玩家）用于「非己方」与范围对照。
- [ ] 手边这四份：36 号 §2.8.1 ~ §2.8.3、附录 A 第 8 / 9 项；35 号 §8.6、B-38 / B-39；34 号 D-169 / D-172；37 号 §6 表 F ② ⑫。
- [ ] **待验证 ② 的先后**：`RenderType.lines()` 在 `AFTER_PARTICLES` 的可画性——先画一条固定坐标的线（不依赖网络包）并确认可见，再写正式逻辑；退路是 `AFTER_TRIPWIRE_BLOCKS`。
- [ ] 红线口径：两个包都是 S2C 只读，客户端不回包；`core_list` 的既有注册与 handler 一字不动（不扩它）；`@Mixin` 全仓仍 **0**。
- [ ] 范围判据只有一处：复用 `OutpostCoreBlockEntity.inRange`（`RANGE_CHUNKS = 1`），**不写第二份范围公式**；`MAX_LINKS = 1024` 只是解码上限，不截断。

### 7.2 本阶段目标

新增第 2 个 S2C 只读包与客户端线框，把「核 ↔ 培养器」的绑定关系画出来。

本节结束：站在自家核旁（3×3 区块内）能看到核与每个已绑定培养器之间的线；走远线消失、换维度不残留、拆方块线立刻消失；客户端连续在线不崩；把客户端的 handler 改成空实现后，绑定 / 产出 / 迁移 / 拆核掉落全部照常——连线是纯观察面。

### 7.3 任务清单（F-238 ~ F-240）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-238 | 连线包 + 注册 + 协议版本 2 | M | 1.5 | — | 新建 `network/CultivatorLinkPayload.java`（`Link(core, cultivator)` + `MAX_LINKS = 1024` + 组合 codec）；`FarmstormNetwork` 第二条 `playToClient`；`PROTOCOL_VERSION` 改 `"2"`；自检行补到两行 |
| F-239 | 连线广播器：范围与五个时机 | L | 3.0 | F-233、F-238 | 新建 `network/CultivatorLinkBroadcaster.java`；五个时机（进范围 / 出范围 / 绑定变化 / 核被拆 / 登录换维度重生登出）；去抖 10 刻、重扫 20 刻；出范围发空列表 |
| F-240 | 客户端只读与线框渲染 | L | 3.0 | F-238 | 新建 `client/ClientCultivatorLinks.java` 与 `client/CultivatorLinkRenderer.java`；`AFTER_PARTICLES` 阶段画线；换服 / 断线 `clear()` |

阶段小计：**3 个任务、7.5 人日**（S 0 / M 1 / L 2）。

### 7.4 出口判据

- [ ] **两个包与协议串（F-238）**：怎么验：起服读日志 + `grep` 注册点。通过的样子：`latest.log` 出现 **2** 行 `自检 网络包注册`，id 分别是 `farmstorm:core_list` 与 `farmstorm:cultivator_link`，版本串都是 **2**；`playToClient` 在 `FarmstormNetwork.java` 命中 **2** 处；`PROTOCOL_VERSION = "2"`。
- [ ] **纯原版客户端与专用服务器（F-238）**：怎么验：用原版客户端进服；再起一次专用服务器。通过的样子：原版客户端能进服（optional 通道）；专用服务器起服无 `NoClassDefFoundError`；`@Mixin` 全仓 **0** 命中。
- [ ] **下发与清残留（F-239 / 36 号 §8 #31）**：怎么验：站在自家核旁、走出范围、拆一个培养器。通过的样子：收到含 **N** 条 link 的包，N = 该核已绑定的培养器数；出范围收到**空列表**、客户端线消失；拆培养器对应的线**立刻**消失（不等 **20** 刻重扫）。
- [ ] **去抖（F-239）**：怎么验：在同一刻制造连锁事件（放置 + 绑定变化）。通过的样子：只发 **1** 包（去抖 **10** 刻）；`MAX_LINKS = 1024` 不被当成截断值使用。
- [ ] **渲染（F-240）**：怎么验：站在核旁看线、换维度、看端点。通过的样子：核与每个已绑定培养器之间有可见线框、两端各 **1** 个 **0.25** 格小框；颜色 RGB `0.30 / 0.85 / 0.90`、alpha `1.0`、线宽 1.0；换维度后残留清空（35 号 B-38）。
- [ ] **不崩（F-240 / RT-45）**：怎么验：客户端连续在线 **10** 分钟，读客户端日志。通过的样子：`Missing elements in vertex` 与 `IllegalStateException` 各 **0** 条。
- [ ] **服务端零依赖（F-240 / 36 号 §8 #34）**：怎么验：把 handler 临时改成空实现后重跑四条线。通过的样子：绑定 / 产出 / 迁移 / 拆核掉落全部照常；验完还原，`git status` 无残留。
- [ ] **注册面不动（负向）**：怎么验：起服读自检行。通过的样子：`自检 注册对象 97 个 … → OK` 不变——两个网络包都不进 `BuiltInRegistries`，payload 走 `RegisterPayloadHandlersEvent`。

### 7.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 忘改协议串 | 装了新模组的客户端连旧服（或反之）握手不匹配，进服即断 | 协议串与 codec 同一个提交改；`PROTOCOL_VERSION = "2"` 有 `grep` 判据 |
| 自检行仍是一行 | 36 号 §8 #2 的判据是**两行**，验收时数不出来 | 自检行补到两行（或改成两行），两行都带 id 与版本串 |
| 把连线塞进 `core_list` 的 codec | 全量快照与范围增量混成一种数据，浮窗与连线互相拖累（D-169 明确否决） | 新包独立；`core_list` 的 codec 与 handler 一字不动 |
| 出范围不发空包 | 客户端的线一直挂着，远处残留（35 号 B-38 的原始症状） | 出范围发**空列表**；换维度与断线都要 `clear()` |
| 自己重算「玩家在不在 3×3 内」 | 与 `inRange` 漂移，两个范围两套答案 | 只复用 `OutpostCoreBlockEntity.inRange`；`RANGE_CHUNKS = 1` 不复制 |
| 手写顶点漏 `NORMAL` | `Missing elements in vertex` 客户端崩（RT-45） | 用 `LevelRenderer.renderLineBox` + `RenderType.lines()`，不自建 `RenderType` |
| 改事件给的 `getPoseStack()` | 把后续阶段一起平移，画面整体错位 | 新建一个 `PoseStack` 并只 `translate(-cam.x, -cam.y, -cam.z)` |
| 在服务端侧引用渲染类 | 专用服务器 `NoClassDefFoundError` | handler 只做 `enqueueWork`；渲染类只在客户端事件里出现 |

### 7.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；`CultivatorLinkPayload.java` 的 shape 与 codec、`FarmstormNetwork.java` 的三处（版本串 / 第二条注册 / 自检行）、`CultivatorLinkBroadcaster.java` 的五个时机、两个客户端类与 `FarmstormClient.java` 的挂载行。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；单人 + 专用服务器各一次；`grep -c`（`playToClient`、`@Mixin`）；客户端 10 分钟日志的完整 grep。
- **观察到什么**：两行自检原文；范围内的 N 值与线上的培养器数；出范围与拆方块前后两帧截图；换维度前后的残留对照；空 handler 那一轮的四条功能读数。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如第二客户端不可用时，「非己方看不到线」这一条要写明替代做法）。
- **未关闭的问题**：例如 `RenderType.lines()` 在光影类模组下的观感（36 号 §9 ② 的后半），本节只记录现场。

---

## 8. Phase 39 · 收口与全量验收（M35）

### 8.1 前置

- [ ] 上一节 **§7.4 的第 1 / 4 / 6 条**已绿：两行网络包自检与协议串 2、去抖、客户端 10 分钟不崩。
- [ ] §1 ~ §7 的**全部**出口判据已绿；其中 §6.4 第 4 / 5 条（产出与暂停）与 §7.4 第 5 / 6 条（渲染与不崩）是 36 条验收的主体。
- [ ] 三个滞后窗口同时到达关窗点：注册自检已于 F-228 关闭（`97 … → OK`）；语言键本节由 F-241 关；config 本节由 F-242 关。
- [ ] **A-69 已解锁**：F-233 已落地，`.dat` 无运行期读者——「mtime 不变」这条判据从本节起才可以执行。
- [ ] **A-66 的假绿在本节之前一直存在**：`tools/verify_project.py` 在 F-243 之前对注册面打 `已核 registry 对象: 93 / 93` 并退出 0；F-243 之后引用它，必须同时给 `97 / 97`。
- [ ] 记录目录：`.work/v020-verify/` 可写（`acceptance.md` / `boundaries.md` / `signoff.md` + 截图）。
- [ ] 本节 **6** 个任务里 F-241 / F-242 / F-243 改文件，F-244 / F-245 / F-246 **一律不改码**（只产出记录）；验收不通过就回对应任务重做，不许就地改判据。
- [ ] 上游只读：`docs/zh/34` ~ `docs/zh/36` 一字不改（`git diff --stat docs/` 为空）。

### 8.2 本阶段目标

把三个总数（337 / 175 / 97）对平，跑完 36 条验收与 40 条边界，签红线与上游一致性。

本节结束：两份 lang 各 337 键且键集相同、`farmstorm-server.toml` 175 键 / 60 段、注册面 97、`@Mixin` 0、`playToClient` 2、36 + 40 = 76 条全部有读数、上游三份文档（34 / 35 / 36）的 `git diff` 为空——可以发布。

### 8.3 任务清单（F-241 ~ F-246）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-241 | 语言键总账 337 与类注释 | M | 1.5 | F-215、F-221、F-222、F-224、F-226、F-227、F-231、F-234 | 重跑 datagen；两份 lang 各 **337** 键；`ModLangProvider.java:13-16` 的逐项构成注释改成与键集一致 |
| F-242 | config 总账 175 键 / 60 组 | M | 1.5 | F-220、F-222、F-226、F-231、F-234 | 删旧 `farmstorm-server.toml` 重生成；逐组对 36 号 §4.1 的 36 个组表；`outpost_core` 4 键无 `farmlandBonus` |
| F-243 | 验证工具同步 97 / 175 / 337 | S | 0.5 | F-228、F-241、F-242 | `tools/verify_project.py` 的 `EXPECTED` / `EXPECTED_COUNTS` / config 期望来源 / 打印串四处同步；活判据一条 |
| F-244 | 36 号 §8 的 36 条验收 | L | 3.0 | F-215~F-240（见 §3） | 新建 `.work/v020-verify/acceptance.md`（36 行 + 表头）；手动场景截图 ≥ 13 张 |
| F-245 | 35 号 §12 的 40 条边界复跑 | L | 3.0 | F-244 | 新建 `.work/v020-verify/boundaries.md`（40 行 + 表头）；B-01 ~ B-40 逐条 |
| F-246 | 红线、注册面与上游一致性签字 | M | 1.5 | F-243、F-244、F-245 | 新建 `.work/v020-verify/signoff.md`：8 行签字表 + 上游四份文档的 `git diff` 为空 |

阶段小计：**6 个任务、11.0 人日**（S 1 / M 3 / L 2）。

> F-244 的依赖写作「F-215~F-240（见 §3）」，与 37 号 §1 表逐字一致；其中的「§3」指 **37 号 §3 的依赖图**（本文件的 §3 是 Phase 34，不是同一处）。

### 8.4 出口判据

- [ ] **语言键总账（F-241）**：怎么验：`ConvertFrom-Json` 取两份键集 + 逐前缀数。通过的样子：各 **337** 键、键集完全相同（`Compare-Object` 为空）；`farmstorm.configuration` **178** / `item` **33** / `block` **27** / `container` **2** / `tooltip` **20** / `message` **0**；`323` 在 `ModLangProvider.java` 命中 **0**、`337` ≥ **1**。
- [ ] **config 总账（F-242）**：怎么验：删旧 toml 重生成后逐组读。通过的样子：**175 键 / 60 段**（`^\s*\[` 计数）；`[outpost_core]` **4** 键且**没有** `farmlandBonus`；`[cultivator]` **2** 键（600 / 2）、`[trade]` **6** 键（2 / 5 / 10 / 32 / 40 / 64）；`farmstorm-client.toml` 仍 **3** 键；`FarmstormConfig.java` 的 `b.push(` 次数 **60**。
- [ ] **工具的四个读数一起给（F-243 / A-66）**：怎么验：`python -X utf8 tools/verify_project.py`。通过的样子：退出码 **0**，且**同时**出现 `已核 registry 对象: 97 / 97`、`175 个配置键与文档逐字一致`、`共 337 键`、`结论：FAIL 0 项`。只写 `FAIL 0` 而不写 `97 / 97` 的判据一律按假绿处理。
- [ ] **工具的活判据（F-243）**：怎么验：把 `EXPECTED_COUNTS["blocks"]` 临时改成 **28** 后运行，再改回。通过的样子：改后退出码非 0、改回后回 **0**；`git diff --stat docs/` 为空（工具只读上游，不回改）。
- [ ] **36 条验收（F-244）**：怎么验：读 `.work/v020-verify/acceptance.md`。通过的样子：**36** 行（+ 表头）、未通过 **0**；每行「观察位置」与「实际值」两列都非空；#1 的日志原文与 36 号 §8 #1 逐字一致（含 `→ OK` 与 12 个分项）；手动场景截图 ≥ **13** 张（#6 ~ #8 三张、#18 ~ #20 三张、#23 ~ #26 四张、#31 ~ #34 四张）。
- [ ] **40 条边界（F-245）**：怎么验：读 `.work/v020-verify/boundaries.md`。通过的样子：**40** 行（+ 表头）、不符 **0**；B-12 / B-13 / B-15 / B-16 / B-19 / B-20 各有 `/data get block` 的实际 NBT 值（`Core` 键的有无是关键）；B-26 / B-27 各记实际掉落清单（空清单写「空」，不留白）；B-40 写明「在临时副本上恢复、验完还原、`git status` 无残留」。
- [ ] **三份记录的时点（A-69 / A-66）**：怎么验：通读三份记录里的时点相关行。通过的样子：`.dat` mtime 判据只出现在 F-244 的 #14 与 F-246 第 ⑦ 项（共 **2** 处），且都在 F-233 之后；F-241 / F-242 之前没有任何「总数已到位」的结论。
- [ ] **签字（F-246）**：怎么验：`grep` 三项 + 数两个类的行数 + 读 mtime。通过的样子：`git diff --stat docs/zh/34-DECISIONS-0.2.0.md docs/zh/35-DESIGN-0.2.0.md docs/zh/36-TECH-0.2.0.md` 为空；`@Mixin` **0**、`playToClient` **2**、`PROTOCOL_VERSION = "2"`；`FarmlandLedger.java` **555** 行、`FarmlandGuard.java` **225** 行；`world/data/farmstorm_farmland.dat` 存在且 mtime 与升级前相同。
- [ ] **8 行签字表（F-246）**：怎么验：读 `signoff.md`。通过的样子：注册面 **97**（12 类分项）/ config **175 键 60 组** / 语言键 **337 × 2** / 动作面 **46 / 46** / 红线三项（`@Mixin` 0、`playToClient` 2、协议串 `2`）/ `Farmstorm.java` 的 **5** 个自检数字 / `.dat` / 两个死类的行数——每行给出「判据 / 实测值 / 结论」。
- [ ] **76 条与 34 个任务全有结论**：怎么验：两份记录与 34 行任务核对表。通过的样子：36 + 40 = **76** 条逐条有读数、未通过 **0**；34 行任务核对表「未达成」**0**；两份记录各写各的「未通过 0」，谁都不替谁签字（36 条是技术面、40 条是边界面）。

### 8.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 只写「FAIL 0」 | 工具少核 4 个注册对象也退出 0，验收看起来全绿（A-66） | `97 / 97` 与 `FAIL 0` **必须同时出现**；缺一个就当假绿，判据无效 |
| 只改 `EXPECTED_COUNTS` 不改 `EXPECTED` 清单 | 数量对、清单旧，工具会立刻红（「清单长度 = 期望数」+「每个 id 出现」两条同时生效） | 四处一起改：清单、计数、config 期望来源、打印串 |
| 把 10 个新键硬编码成第二份清单 | 工具从「读文档现算」退化成「抄一份答案」，A-66 的原始缺陷原地复活 | 期望集仍从文档现算（36 号 §4.1.1 入并集）；不写第二份清单 |
| 只改 json 不改类注释 | `ModLangProvider.java:13-16` 仍写 323，下一个人照着旧注释再算一遍 | 注释与键集同一次提交；`323` 归一 0、`337` ≥ 1 |
| 不删旧 toml 就数键数 | `farmlandBonus` 作为孤立键留在文件里，175 变 176、`[outpost_core]` 变 5 键 | 删旧重生成后再数；组数按 `^\s*\[` 数，不是 `^\[` |
| 把 36 条与 40 条混在一份记录里 | 两份记录互相顶替，21 条「无对应断言」的边界一条都没验 | 两份文件分开：`acceptance.md` 36 行、`boundaries.md` 40 行 |
| 用 F-233 之前的存档验 #14 | `.dat` 被判红，现场看起来像「实现坏了」 | #14 与签字第 ⑦ 项都在 F-233 之后（A-69）；先确认 F-233 的时间戳 |
| 验收不通过就改判据 | 三轮验收作废，`git status` 里出现 `src/` 的改动 | 本节 F-244 ~ F-246 **不改码**；`git status` 只允许 `.work/v020-verify/` |
| 把 B-33 / B-34 判成缺陷 | 老档满级农民不补发、交易是抽样，这两条是**被接受的代价** | 按 35 号 §4.6 / 36 号 §2.4.2 写「按预期，不判失败」 |

### 8.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；F-241 只有两份 lang + 类注释、F-242 只有重生成的 toml、F-243 只有 `tools/verify_project.py`；F-244 ~ F-246 新增三份记录与截图。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`python -X utf8 tools/verify_project.py` 的**四段完整原文**与退出码；`Compare-Object` 的键集结果；三个 `grep`（`@Mixin`、`playToClient`、`PROTOCOL_VERSION`）；两个类的行数；`.dat` 的 mtime。
- **观察到什么**：337 / 178 / 33 / 27 / 20 / 0 的逐前缀读数；175 键 / 60 段与四个组的键数；97 / 97 那一行原文；76 条逐条结论（36 行 + 40 行）；8 行签字表。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如某条观感类没有录屏、第二客户端不可用）。
- **未关闭的问题**：A-62 要求回灌 34 号 §3.3 与 35 号 §7.2（字段保留）、A-69 要求 36 号 §8 #14 补时点、A-67 的 D 号补登——三件都属上游回灌，本节只登记不做。

---

## 9. 跨 Phase 纪律的落点台账

| # | 纪律 | 落在哪几节的「前置 / 翻车点 / 判据」 | 违反后现场看到什么 |
|---|---|---|---|
| 1 | 注册自检窗口 **F-224 → F-228** | 前置：§4（窗口在这里开）、§5（关窗已确认）；翻车点：§1（提前开窗）、§4（窗口内写 OK、提前改常量）；判据：§4 第 1 / 6 / 7 条（96 → 97 → OK） | 窗口内出现一条 `→ OK` 判据 ⇒ 之后每次起服都带一条常红噪声，真故障被盖住 |
| 2 | 语言键窗口 **F-215 → F-241** | 前置：§1（只增 4 条）、§8（本节关窗）；翻车点：§1（改总数注释）、§3（把总数写进判据）、§4、§6、§8；判据：§1 第 6 条、§3 第 5 条、§6 第 7 条、§8 第 1 条 | 中途写出 337 ⇒ 每个 Phase 都要重算一遍注释，必有一节算错 |
| 3 | config 窗口 **F-220 → F-242** | 前置：§3（区间读数）、§8（本节关窗）；翻车点：§3（把 175 当总数）、§4、§6、§8（不删旧 toml）；判据：§3 第 4 / 5 条、§4 第 9 条、§6 第 1 条、§8 第 2 条 | 中途把 175 当总数 ⇒ F-242 的逐组核对失去基线 |
| 4 | **A-69** 的时点（`.dat` 只在 F-233 之后） | 前置：§3、§5（第 7 条在 F-233 之后执行）、§8；翻车点：§3（提前断言 mtime）、§5（拿旧存档验）；判据：§3 第 9 条、§5 第 7 条、§8 第 7 条 | F-233 之前断言 mtime ⇒ 拆核会重写该文件，判据必红，看起来像文档写错 |
| 5 | **A-66** 的假绿（少核 4 个对象仍退出 0） | 前置：§5（只许记录 93 / 93 并标注）、§8；翻车点：§5、§8（只写 FAIL 0、硬编码第二份清单）；判据：§5 第 9 条、§8 第 3 / 4 条 | 只写 `FAIL 0` ⇒ 4 个新对象从未被核过，验收全绿但没人验过它们 |
| 6 | **A-62**（`farmlandBonus` 只注释定义、字段保留） | 前置：§3（本节口径以 A-62 为准）；翻车点：§3（按 34 / 35 号把字段一起注释）；判据：§3 第 3 条、§8 第 9 条 | 字段被一起注释 ⇒ `FarmlandGuard` / `FarmlandLedger` 编译不过，两个「一行不改」的类被迫改 |

---

## 10. 本文件的一致性自检

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 任务号连续、无跳号、无重号 | 8 张任务表合计 **34** 行；区间 `F-213 ~ F-246` ⇒ `246 − 213 + 1 = 34` | 通过 |
| 2 | Phase 与里程碑一一对应 | Phase **32 ~ 39** 共 **8** 个；里程碑 **M28 ~ M35** 共 **8** 个；每节标题带自己的里程碑号 | 通过 |
| 3 | 每节六个小节、顺序固定 | 每节都是「前置 / 本阶段目标 / 任务清单 / 出口判据 / 翻车点 / 汇报格式」；**6** 个小节 × **8** 个 Phase = **48** 个二级小标题，顺序无一处调换 | 通过 |
| 4 | 任务号 / 标题 / 复杂度 / 人日 / 依赖与 37 号一致 | 34 行逐行比对 37 号 §1 表与各任务标题行：编号 **34/34**、标题 **34/34**、S/M/L **34/34**、人日 **34/34**、依赖 **34/34** | 通过 |
| 5 | 人日自洽 | 八节小计 `5.0 + 2.0 + 6.5 + 12.5 + 9.5 + 7.0 + 7.5 + 11.0 = 61.0`；复杂度 `S 11 / M 9 / L 14`；`61.0 × 1.25 = 76.25` | 通过 |
| 6 | 出口判据总数与分布 | 合计 **66** 条：32 节 **7** / 33 节 **7** / 34 节 **9** / 35 节 **9** / 36 节 **9** / 37 节 **7** / 38 节 **8** / 39 节 **10**；每条以 `- [ ]` 开头，每条含数值、键名或路径 | 通过 |
| 7 | 中间态窗口写对 | 窗口只落在 §4（F-224 开、F-228 关）：§4 的四条相关判据只写 **96 / FAIL** 与 **97 / FAIL**，唯一写 OK 的是关窗那一条；§1 ~ §3 在**窗口前**，写 `93 … → OK` 是对的；§5 起写 `97 … → OK` | 通过 |
| 8 | 两个滞后窗口写对 | 语言键：§1 只写「4 条键 + 前缀 16 → 20」，总数只在 §8 第 1 条；config：§3 / §4 / §6 只写 toml 区间读数，总数只在 §8 第 2 条 | 通过 |
| 9 | A-62 / A-66 / A-69 三处口径 | A-62 在 §3 前置与第 3 条；A-66 在 §5 第 9 条与 §8 第 3 / 4 条；A-69 在 §3 第 9 条与 §5 第 7 条（F-233 之后） | 通过 |
| 10 | 数字不发明 | 97 / 96 / 93 / 175 / 173 / 172 / 171 / 165 / 337 / 178 / 323 / 76 / 46 / 40 / 36 / 34 / 22 / 18 / 16 / 13 / 11 / 11 / 7 / 5 / 4 / 2 / 600 / 555 / 225 / 54 —— 每个都能追到 34 / 35 / 36 / 37 号或它们的算术 | 通过 |
| 11 | 只读上游、只新建一个文件 | `git status` 只出现本文件；`docs/zh/34` ~ `docs/zh/37`、`docs/en`、`src`、`.work` 一字未改 | 通过 |
| 12 | 禁用词与语气 | 全文无未定事项标记、无待办占位；人称用「实现者 / 作者」，不用第二人称敬语；结论句一律祈使 | 通过 |
