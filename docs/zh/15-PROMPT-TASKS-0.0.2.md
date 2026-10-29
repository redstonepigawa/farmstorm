# 15 · v0.0.2 分阶段任务清单（Task Prompt）

| 项 | 值 |
|---|---|
| 文档编号 | 15-PROMPT-TASKS |
| 版本 | v0.0.2 |
| 读者 | 投喂编码代理的人 + 被投喂的 CLI 编码代理（Claude Code / Codex 一类） |
| 上游 | `.work/v002-BRIEF.md`（唯一口径来源）、`docs/zh/10-DECISIONS-0.0.2.md`（D-49 ~ D-78）、`11-DESIGN-0.0.2.md`、`12-TECH-0.0.2.md`、`13-TASKS-0.0.2.md`（F-60 ~ F-99 的唯一来源） |
| 同级 | `14-PROMPT-MAIN-0.0.2.md`（常驻主提示词；本文件**不重复**它的硬约束与完成定义） |
| 装载方式 | 每个 Phase 投喂一次：`14-PROMPT-MAIN-0.0.2.md` 全文 + 本文件对应的那一节 |
| 内容 | 5 个 Phase / 40 个任务（F-60 ~ F-99）；任务编号、标题、复杂度、人日、依赖逐字对齐 `13-TASKS-0.0.2.md` |
| 边界 | 只读上游：不改 `06-PROMPT-TASKS.md`（v0.0.1 冻结件）、`10` / `11` / `12` / `13`、`.work/v002-BRIEF.md` 与 `src/` |

---

## 0. 使用说明（给人读）

### 0.1 本文件与 14-PROMPT-MAIN-0.0.2.md 的分工

| 文件 | 管什么 | 投喂频率 |
|---|---|---|
| `14-PROMPT-MAIN-0.0.2.md` | 全局纪律与硬约束（工程红线、代码规范、执行纪律、完成定义、回报格式、禁止行为、已知陷阱） | 每个阶段都常驻 |
| `15-PROMPT-TASKS-0.0.2.md`（本文件） | 这一轮做哪些任务、做到什么算过、交付后怎么开下一阶段 | 一次只取一节 |

三条规矩：

1. 本文件不重写硬约束，只引用编号。看到 `D-xx`（决策）回 `10`、`RT-xx`（风险）与 `§11 #n`（验收自检）回 `12`、`A-0x`（裁决）与 `F-xx` 回 `13`、`HC-15` ~ `HC-28` 与 `N-15` ~ `N-28` 回 `14`，不要凭记忆理解。
2. 冲突时的裁决顺序：`10-DECISIONS-0.0.2.md`（v0.0.2 唯一真相源）> `12-TECH-0.0.2.md` / `11-DESIGN-0.0.2.md` > `13-TASKS-0.0.2.md` > 本文件；`.work/v002-BRIEF.md` 是施工期的摊平稿，与 `10` 冲突时以 `10` 为准（§0.4 的 A-06 就是按这条裁的）。v0.0.1 的既有内容仍以 `00-DECISIONS.md` 为准。
3. 本文件的验收判据只在 `14-PROMPT-MAIN-0.0.2.md` 的完成定义之内收紧，不额外发明新的交付物。

### 0.2 怎么投喂

1. 打开本文件，找到当前 Phase 的小节（标题形如 `## 2. Phase 11 · 基座与注册增量（F-60 ~ F-66）`），从标题开始整段复制。
2. 与 `14-PROMPT-MAIN-0.0.2.md` **全文**一起投给编码代理，**一次只投一个 Phase**。
3. 等它按 `14` 的回报格式交回，再对照本节「本阶段出口判据」逐条验收。
4. 验收通过后，用 §7 的交接语开下一阶段。

两条禁令：

- 不要把 §1 总览表一起投——代理会试图一次做完五个阶段。
- 不要按编号顺序硬排 Phase 15：`F-97`（语言文件 39 条）依赖 `F-98`（三个进度），6 个进度文案键依赖 3 条进度路径，先做 `F-97` 会缺 6 个键。

### 0.3 怎么判断一个阶段完成

三道闸全绿才算过，缺一条都按「未完成」处理：

1. **出口判据**：本节「本阶段出口判据」每一条都能指到日志行、`/execute` / `/data get` / `/advancement` / `/playsound` 的返回值、`src/generated/resources` 或 `config/farmstorm-server.toml` 的文件内容、或 `build/libs` / `dist` 的产物。说「实现了但没验」不算过。
2. **任务覆盖**：任务清单里每个 F-xx 都有一份可指认的证据。代理漏做时按 `14` 的完成定义要求回补，不要自己补。
3. **仓库状态**：`.tools\gw.cmd build` 退出码 0；`git status` 只出现本节边界内的文件；`docs/zh/00`–`09`、`docs/en/00`–`09` 冻结件、`.work/v002-BRIEF.md` 与 15 条既有 `CropProfile` 的字段未被触碰（BRIEF §3.4）。

阶段失败时的处理顺序：先让它按 `14` 的自查节自查 → 仍不过就把未通过的验收项抄回任务书重派一次 → 两次不过才换新会话重做，并在交接语里点名卡在哪一条。

### 0.4 五条最新裁决（覆盖 `13-TASKS-0.0.2.md` §0.4 的旧表述，写错即返工）

| 编号 | 裁决 | 旧写法（会被判错） | 正确写法 |
|---|---|---|---|
| **A-01** | 莫洛托夫只要一个概率键 | `molotov.fireChance` + `molotov.magmaChance` 两个独立百分比；或给岩浆块再设第二个键、再定「和为 100」的归一规则 | 只落 `molotov.fireChance`（double，默认 85.0 %，范围 0.0–100.0）；**余下的百分比自动放岩浆块**，不设第二个键（新增键因此是 22 条） |
| **A-03** | 桑吉尔蔗相位 2 用 `fallTicks` 逐刻伺服，不定速 | `zangief_cane.dropVelocity`：进入相位 2 时设一次向下速度，之后不变 | `zangief_cane.fallTicks`（int，默认 40 刻，范围 1–200）；**每刻按「剩余高度 ÷ 剩余刻数」重设向下速度**，让玩家落在该时长内。理由是 plan 的约束是「1.7–2.0 秒内落地」这个**时间**，高度一变定速必然失效 |
| **A-05** | 水平转速默认值 | `zangief_cane.spinDegreesPerTick = 24.0`（BRIEF §8 的旧文），或写 24 却在同一句里说「1440 度每秒」 | `zangief_cane.spinDegreesPerTick = 72.0`（double，范围 0.0–360.0）：1440 ÷ 20 刻/秒 = 72°/刻；40 刻累计 2880° = 8 整圈。语言键 `farmstorm.configuration.zangief_cane.spinDegreesPerTick` 的默认值文案同步写 72.0 |
| **A-06** | 压缩干草块抗性 6.0 的后果矩阵 | 「瓜！炸不动、育碧服务器炸得动、**原版 TNT 也炸不动**」——把 6.0 当成能挡原版爆炸 | 瓜！与瓜片子（阈值 6.0）**破不动**（判定 `resistance < maxResistance`，`6.0 < 6.0` 为假）；育碧服务器与毒育碧服务器（阈值 3.6e6）**破得动**；**原版爆炸（含 TNT）也破得动**（原版公式逐格累积 `f -= (resistance + 0.3) * 0.3`、`f > 0` 即破，TNT 威力 4 时 f 起始区间 [2.8, 5.2]） |
| **A-07** | D-77「沿用膨胀麦」到底沿用什么 | 「爆炸威力与伤害上限沿用膨胀麦」——这句是无中生有：`PuffedWheatProjectile` 里没有任何 `Explosion` 调用 | 沿用**落点写入**两条：① 立方体边长干态 3 / 遇水 5，覆盖空气 / 流体 / 实体方块直接写入，原方块不产生掉落；② 目标格原方块抗性 ≥ 3.6e6 时跳过不写。只把写入的方块从 `minecraft:hay_block` 换成 `farmstorm:compressed_hay_block` |

### 0.5 数字口径速查

| # | 量 | 值 | 出处 |
|---|---|---|---|
| 1 | `CropProfile` 条数 | **19**（15 → 19） | BRIEF §3.1 / §4；`10-DECISIONS-0.0.2.md` §4；`12-TECH-0.0.2.md` §3.1 |
| 2 | 注册对象总数 | **77**（64 → 77；启动自检行带 12 类小计） | BRIEF §4；`12-TECH-0.0.2.md` §2.1 / §11 #2 |
| 3 | config 键 | **139**（117 + 22 新增） | BRIEF §8；`12-TECH-0.0.2.md` §8.1 / §11 #16 |
| 4 | 资源文件总数 | **298**（262 + 36 新增；JSON 193 / PNG 99 / OGG 2 / GLSL 2 / TOML 1 / `pack.mcmeta` 1，扣 `.cache` 的 11 个） | BRIEF §4.1；`12-TECH-0.0.2.md` §7.1 / §11 #17 |
| 5 | 语言文件键 | **261**（222 + 39 新增；中英键集必须相等） | BRIEF §8；`12-TECH-0.0.2.md` §7.3 / §11 #18 |
| 6 | 新增资源文件 | **36**（PNG 8 / OGG 2 / JSON 24 / GLSL 2） | `12-TECH-0.0.2.md` §7.2；`13-TASKS-0.0.2.md` §9.3 |
| 7 | 新增语言键 | **39**（config 22 + item 5 + block 2 + effect 2 + subtitles 2 + advancements 6） | `12-TECH-0.0.2.md` §7.3；`13-TASKS-0.0.2.md` F-97 |
| 8 | 新增 config 键 | **22**（molotov 3 / wild_apple 2 / wild_hunter 11 / zangief_cane 6） | BRIEF §8 唯一清单；`12-TECH-0.0.2.md` §8.1 |
| 9 | 进度 | **15**（12 + 3） | BRIEF §4；`10-DECISIONS-0.0.2.md` D-63；`12-TECH-0.0.2.md` §11 #19 |
| 10 | 转换配方 | **24**（21 + 3） | BRIEF §4；`10-DECISIONS-0.0.2.md` D-54；`13-TASKS-0.0.2.md` F-73 |
| 11 | 类型分布（投掷 / 部署 / 食用） | **10 / 6 / 3** | BRIEF §4；`10-DECISIONS-0.0.2.md` §4 的计数自洽表 |
| 12 | 破障类 | **4**（不变） | BRIEF §4；`10-DECISIONS-0.0.2.md` §5.3；D-42 |
| 13 | 任务 | **40** 个 / **5** 个 Phase（F-60 ~ F-99；7 / 9 / 10 / 8 / 6） | `13-TASKS-0.0.2.md` §2 / §12.2 |
| 14 | 人日 | **60.5**（S 10 / M 23 / L 7；= 5.0 + 34.5 + 21.0），加 25 % 缓冲 **75.625**（记 75.6） | `13-TASKS-0.0.2.md` §0.2 / §2 |

两条口径提醒：

- v0.0.1 的三个基数是 **117**（config）/ **262**（资源）/ **222**（语言键），不是 `06-PROMPT-TASKS.md` 里更早的 216 / 257——那两组数字已被实测取代，以 BRIEF §4.1 为准。
- `src/generated/resources` 下的 `.cache` 目录里有 **11 个无扩展名文件**，是 DataGen 缓存，**不进 jar、不计入 298**；资源核对时先扣掉再数。

---

## 1. 总览表（给人看，不要随阶段一起投）

| Phase | 标题 | 任务数 | 任务 ID | 人日 | 前置 Phase | 出口判据（一句话） |
|---|---|---|---|---|---|---|
| Phase 11 | 基座与注册增量 | 7 | F-60 ~ F-66 | 11.0 | — | `gw.cmd build` 产出 0.0.2 jar；四条自检行全 OK（19 种作物 / 77 个注册对象 / 16 条浆果池 / 11-11 渲染器）；`farmstorm-server.toml` 叶节点 139 |
| Phase 12 | 压缩物与莫洛托夫！ | 9 | F-67 ~ F-75 | 10.5 | Phase 11 | 5 条新配方可用；投莫洛托夫只追加火 / 岩浆块、原方块一个不少；压缩干草块对瓜！与瓜片子不破、对育碧服务器与 TNT 破 |
| Phase 13 | 狂 野 猎 人 | 10 | F-76 ~ F-85 | 16.0 | Phase 11 | 吃一颗狂野苹果后七项能力可观察（属性 / 吸附 / 伤害击退 / 续时 / 整屏灰度 / 红剪影 / 循环音），`/effect clear` 后全部复位 |
| Phase 14 | 桑吉尔蔗 | 8 | F-86 ~ F-93 | 11.5 | Phase 11 | 三相位按时序走完；三个高度落地耗时都落在 40 ± 2 刻；21 项键位手测只有 ESC 例外；`Rotation` 不写回 |
| Phase 15 | 资源、音频、进度与验收 | 6 | F-94 ~ F-99 | 11.5 | Phase 11 + 12 + 13 + 14 | 298 / 261 / 139 / 77 四项对齐；`12-TECH` §11 的 22 条自检全过；`dist/farmstorm-0.0.2+mc1.21.1.jar` 产出 |
| **合计** | **5 个 Phase** | **40** | **F-60 ~ F-99** | **60.5** | | |

读表要点：

- 官方依赖图里 Phase 12 / 13 / 14 **互不依赖**，三条内容线都只等 Phase 11，可以分别派给三个人（`13-TASKS-0.0.2.md` §1.1 与 §8.1 的簇 B / C / D）。本文件仍按编号顺序编排，只在这三条线并行时才有换序意义。
- 关键路径走的是 Phase 14 那条：`F-60 → F-62 → F-63 → F-86 → F-87 → F-88 → F-92 → F-93 → F-99` = 14.5 人日 / 9 个任务，只占 60.5 的 24 %。工期不是被最长的链卡住的，想缩短工期先加并行，别压关键路径。
- Phase 15 是唯一汇合点：`F-99` 依赖 F-75 / F-85 / F-93 / F-94 / F-96 / F-97，前面四个阶段任何一条没过，F-99 都不许开。
- `F-97` 在编号上排在 `F-98` 之后，依赖列也写明它等 `F-98`——**投喂顺序是 F-98 → F-97**，这是全文件唯一一处「编号顺序 ≠ 执行顺序」。
- 复杂度分布：S 10 / M 23 / L 7。7 个 L 是 `F-64`（两个新 register）、`F-79`（冲刺）、`F-82`（灰度）、`F-92`（输入锁）、`F-94`（媒体管线）、`F-95`（贴图）、`F-99`（验收与打包）——这七个都是「一错就要整段重来」的任务，投喂时优先给足上下文。

---

## 2. Phase 11 · 基座与注册增量（F-60 ~ F-66）

> 投喂方式：`14-PROMPT-MAIN-0.0.2.md` 全文 + 本节，一次只喂一个 Phase。

### 2.1 投喂前必须已就绪

- v0.0.1 已发版：`dist/farmstorm-0.0.1+mc1.21.1.jar` 在位，`src/main/java/com/dashpig/farmstorm/` 下 74 个 `.java` 可编译，`.tools\gw.cmd build` 退出码 0（BRIEF §1）。
- `12-TECH-0.0.2.md` §2.1（77 对象核对表）、§3.1（`CropProfile` 4 条新常量）、§3.3 / §3.4（两个新 `DeferredRegister`）、§8.1（22 键清单）、§11 #1–#6 已读；BRIEF §3.3 的 19 个既有文件白名单已抄成本阶段的改动清单。
- 本阶段每个任务只追加自己那一段：`registry/FarmstormItems.java` 与 `registry/FarmstormBlocks.java` 由 F-65 写，`registry/FarmstormEntityTypes.java` 与 `client/FarmstormClient.java` 由 F-66 写，两个任务不同时改同一个文件。
- 冻结件已确认不动：`docs/zh/00`–`09`、`docs/en/00`–`09`、15 条既有 `CropProfile` 的任何字段、既有 117 个 config 键的默认值、既有 21 条转换配方、既有 12 个进度（BRIEF §3.4）。
- 构建入口只认 `.tools\gw.cmd`，不要直接调 gradle（BRIEF §1）。
- 本阶段不产出音频、贴图与模型（那三类在 Phase 15 的 `F-94` / `F-95` / `F-96`）；`wild_hunter` 的 HUD 图标此时会是紫黑方块，`F-64` 的验收只认「效果在、`/data get` 读得到 `amplifier` 与 `show_icon`」，不认图标好不好看。

### 2.2 本阶段目标

把 v0.0.2 的注册层一次立稳：版本号切 0.0.2、22 个新 config 键、19 条作物档案、`DeployedKind` 第 6 个枚举值、`mob_effects` / `sound_events` 两个新 `DeferredRegister`、5 物品 / 2 方块 / 2 实体类型与 11/11 渲染器。四条启动自检行（19 / 77 / 16 / 11-11）全 OK、`farmstorm-server.toml` 叶节点 139，就是本阶段完成的唯一形态——后面三个阶段只往里填行为，不再改这四处的形状。

### 2.3 任务清单

| 编号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付 |
|---|---|---|---|---|
| F-60 | 构建基线切换与 5 个新资源目录 | S / 0.5 | — | `gradle.properties` 的 `mod_version` 切到 0.0.2；`assets/farmstorm/` 下建 `sounds/`、`textures/misc/`、`textures/mob_effect/`、`shaders/core/`、`post_effect/` 五个新目录；`build.gradle` 零改动 |
| F-61 | config 22 键四分组 | M / 1.5 | F-60 | `FarmstormConfig` 落 molotov 3 / wild_apple 2 / wild_hunter 11 / zangief_cane 6 共 22 键 + `bake()` 烘焙快照 |
| F-62 | 4 条新作物档案、`ALL` 19 条与幸运浆果池 16 条 | M / 1.5 | F-60 | `CropProfile` 加 4 个常量、`ALL` 19 条、`LUCKY_BERRY_POOL` 改显式排除 3 件 → 16 条，并同步启动自检文案 |
| F-63 | `DeployedKind` 新枚举值与两处穷尽 switch | M / 1.5 | F-62 | `ZANGIEF_CANE(10)` + `DeployedBehaviors` 的静态单例与 `case` + `FarmstormBlockEntities` 的 `case` 与方块列表 |
| F-64 | 两个新 `DeferredRegister`：`mob_effects` 与 `sound_events` | L / 3.0 | F-60 | `WildHunterEffect`（可见、带图标、三项属性修饰器挂载与卸下）/ `ZangiefPiledriverEffect`（隐藏，amplifier 当相位号）+ 两个注册类挂 mod 总线 |
| F-65 | 5 个物品、2 个方块与燃料标签键 | M / 1.5 | F-62 | `items` 加 5（含 `compressed_hay_block` 的 BlockItem）/ `blocks` 加 2（含 `deployed(ZANGIEF_CANE, …)`）/ `TagKey<Item> CONVERTER_FUEL` |
| F-66 | 2 个投掷物类骨架、2 个 entity_types 与渲染器 11/11 | M / 1.5 | F-64、F-65 | `MolotovProjectile` / `CompressedPuffedWheatProjectile` 骨架（`onImpact` 只留「每次生成只结算一次」的钩子）+ 2 个 `projectile(...)` + 2 个 `ThrownItemRenderer` + 补「77 个 / 11-11」两行自检 |

### 2.4 本阶段出口判据

- [ ] **F-60 构建**：跑 `.tools\gw.cmd build`。怎么验：看退出码与 `build/libs` 目录。通过的样子：退出码 0，`build/libs/farmstorm-0.0.2+mc1.21.1.jar` 存在（文件名版本号来自 `gradle.properties`），`build.gradle` 的 `git diff` 为空。
- [ ] **F-60 五个新目录**：`Get-ChildItem -Directory src\main\resources\assets\farmstorm`。通过的样子：`sounds`、`textures\misc`、`textures\mob_effect`、`shaders\core`、`post_effect` 五个都在（`12-TECH` §1.2 的五目录表）。
- [ ] **F-61 键总数**：起一次服务器后数 `run\config\farmstorm-server.toml` 的叶节点。通过的样子：**139** 个；四个新分组按字母序（`molotov` → `wild_apple` → `wild_hunter` → `zangief_cane`）出现在文件末尾；抽样三个默认值 `molotov.fireChance = 85.0`、`wild_hunter.durationTicks = 700`、`zangief_cane.spinDegreesPerTick = 72.0`（A-01 / A-05）。
- [ ] **F-61 热重载**：把 `molotov.fireChance` 改成 `50.0` 后 `/reload`。通过的样子：日志出现一行 `bake()`，读回值 50.0；改回 85.0 后同样生效。
- [ ] **F-62 两条自检行**：看 `logs/latest.log` 前 100 行。通过的样子：逐字出现 `自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK` 与 `自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK`（BRIEF §4 原文 + `12-TECH` §11 #1 / #3）。
- [ ] **F-62 冻结件**：`git diff util/CropProfile.java`。通过的样子：只有新增行、没有 `-` 行；既有 15 条的字段一个字符没改，顺序与 `10-DECISIONS-0.0.2.md` §4 的行号 1–15 一致。
- [ ] **F-63 枚举与开关**：看启动日志的 kind → 单例映射行，并跑一次编译。通过的样子：6 个枚举值各一行、`ZANGIEF_CANE` 的扫描间隔写 10 刻；编译日志无 `the switch expression does not cover all possible input values`；注册自检行里 `block_entities` 仍是 6（`12-TECH` §3.14 的编译期强制）。
- [ ] **F-64 两个效果**：`/effect give @s farmstorm:wild_hunter 10` 与 `/effect give @s farmstorm:zangief_piledriver 10 2`。通过的样子：前者 HUD 出图标槽，后者 HUD 无图标、物品栏无描述；`/data get entity @s ActiveEffects` 里 `zangief_piledriver` 的 `amplifier` 为 2、`show_icon` 为 false（D-50 的相位通道成立）。
- [ ] **F-64 两个声音事件**：`/playsound farmstorm:wild_hunter_loop master @s ~ ~ ~ 1 1` 与 `/playsound farmstorm:zangief_piledriver master @s ~ ~ ~ 1 1`。通过的样子：两条命令都成功（音频文件在 `F-94`，此时静音通过），两个事件出现在 `/playsound` 的补全列表里（`12-TECH` §11 #6）。
- [ ] **F-65 五物品两方块**：五次 `/give` + 两次 `/setblock`。通过的样子：7 条命令全部返回成功，创造栏 `farmstorm:main` 里能看到 5 条新物品；`/setblock ~ ~1 ~ farmstorm:zangief_cane` 后世界里无可见几何体（空模型，D-68）；`/data get block <坐标>` 读到 `owner` 字段（复用 `DeployedCropBlockEntity`）。
- [ ] **F-64 / F-66 另外两条自检行**：同一次启动的日志。通过的样子：逐字出现 `自检 注册对象 77 个：items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1 → OK`、`自检 实体渲染器 11/11 → OK`，且紧随一行列出 11 个实体 ID（含 `farmstorm:molotov` 与 `farmstorm:compressed_puffed_wheat`）。
- [ ] **F-66 两个新实体**：`/summon farmstorm:molotov ~ ~2 ~` 与 `/summon farmstorm:compressed_puffed_wheat ~ ~2 ~`。通过的样子：两条都成功、不崩；实体落地时周围方块 `/execute if block` 全部不变（骨架版 `onImpact` 未接行为）。
- [ ] **专用服务器**：`runServer`（不是只跑 `runClient`）。通过的样子：启动日志无 `NoClassDefFoundError`（两个效果类与两个注册类都在公共侧，不依赖 `Dist.CLIENT`）。
- [ ] **仓库状态**：`git status` 与 `git diff --stat`。通过的样子：只出现白名单文件与新增文件；`docs/`、`.work/` 与 15 条既有档案无改动。

### 2.5 常见翻车点

1. **加了 `ZANGIEF_CANE` 却没补两处穷尽 switch，或用 `throw new UnsupportedOperationException()` 糊过去**：`DeployedBehaviors.of()` 与 `FarmstormBlockEntities.create()` 都是穷尽 `switch`，加值不改就编译不过；而用 `throw` 糊过去时编译是绿的，但 `/setblock` 一个桑吉尔蔗会在区块加载时崩服。提前发现：加完枚举值立刻 `.tools\gw.cmd build`，日志出现 `the switch expression does not cover all possible input values` 就是它；注册自检里 `block_entities` 从 6 变成 7 说明顺手新注册了一个 BE（那是改错，不是修好）。
2. **22 个键自己重数、键名自己造**：`11-DESIGN` 的旧稿里有 `magmaChance`、`wild_apple.hunterDurationTicks`、`zangief_cane.dropVelocity` 这类已被 A-01 / A-02 / A-03 否掉的键名；抄错一个键名，`farmstorm.configuration.*` 语言键就少一条，139 与「语言键数 = config 键数」的相等性同时破。提前发现：把 BRIEF §8 的 22 行抄成核对表逐行对 `farmstorm-server.toml`，再数带 `farmstorm.configuration.` 前缀的语言键，两个数都必须是 139。
3. **自检文案被「润色」成同义句**：四条自检行是逐字验收（`12-TECH` §11 #1–#4），把 `→ OK` 换成 `OK`、把「投掷型 10」写成「投掷 10 种」都判不过——而 F-62 / F-66 的出口判据就是这几行的原文比对。提前发现：把 BRIEF §4 的原文行贴进任务书，交回时用 `Select-String` 逐字比对，字符级差异也要报。
4. **把 15 条既有档案「整理」一遍**：`CropProfile.ALL` 从 15 加到 19 时重排数组顺序、或把 `LUCKY_BERRY_POOL` 的推导式整体重写，都会碰到既有 15 条的字段（BRIEF §3.4 红线）。提前发现：`git diff` 只允许纯新增行，出现 `-` 行即翻车；`ALL` 的既有 15 条顺序必须与 `10-DECISIONS` §4 的行号 1–15 一一对应。
5. **两个新 `DeferredRegister` 泄进客户端侧**：`WildHunterEffect` / `ZangiefPiledriverEffect` 一旦 import 了 `net.minecraft.client.*`，专用服务器起服就 `NoClassDefFoundError`（`12-TECH` §11 #22）。提前发现：本阶段必须跑一次 `runServer`；`grep -rn "net.minecraft.client" effect/` 命中 0。
6. **`compressed_hay_block` 的属性提前写进 F-65**：F-65 只负责注册这个条目，`strength(30.0F, 6.0F).requiresCorrectToolForDrops()` 是 F-67 的交付物；两处各写一份 `Properties` 会造成「同一数值两个来源」。提前发现：本阶段该条目只允许出现一次构造，且不出现 `30.0F` 字面量。

### 2.6 完成后应向用户回报

- `build/libs/farmstorm-0.0.2+mc1.21.1.jar` 的路径与 `gw.cmd build` 的结论行；`build.gradle` 的 `git diff` 为空。
- `farmstorm-server.toml` 的叶节点计数命令与结果（139）+ 四个新分组名 + 三个抽样默认值原文。
- 四条自检行的原文（贴日志，不转述）。
- 5 个 `/give` 与 2 个 `/setblock` 的返回行，以及一条 `/data get block` 的 `owner` 回读。
- `/data get entity @s ActiveEffects` 里两个效果的 `amplifier` 与 `show_icon` 读数。
- `DeployedKind` 的 6 行 kind → 单例映射。
- 未关闭的问题（尤其是 `AttributeModifier` 在 21.1.213 下的构造签名、两个 `DeferredRegister` 挂 mod 总线的时机、`bake()` 被调用的位置）。

---

## 3. Phase 12 · 压缩物与莫洛托夫！（F-67 ~ F-75）

> 投喂方式：`14-PROMPT-MAIN-0.0.2.md` 全文 + 本节，一次只喂一个 Phase。

### 3.1 投喂前必须已就绪

- Phase 11 全部出口判据通过，特别是 `F-65`（5 物品 / 2 方块）与 `F-66`（两个投掷物骨架 + 2 个 entity_types）——本阶段 9 个任务里 6 个直接改它们造出来的东西。
- `12-TECH-0.0.2.md` §3.7（`MolotovProjectile`）、§3.8（`CompressedPuffedWheatProjectile`）、§3.9（`compressed_hay_block` 不新增方块类）、§11 #7 / #8 / #9 / #20 已读。
- `10-DECISIONS-0.0.2.md` §5.3 的抗爆逐件表已抄进任务书：这份表与 BRIEF §2 的 D-57 一致，任何「TNT 炸不动」的写法都按 §0.4 的 A-06 判错。
- 已知 v0.0.1 事实：`entity/thrown/PuffedWheatProjectile.java` 的 `fillHay` 写死了 `Blocks.HAY_BLOCK`，且该文件在 BRIEF §3.3 的白名单内，允许小改动（抽 `protected BlockState fillBlock()` 由子类覆写）；改法按 `13-TASKS` §12.3 第 4 条的裁决。
- 本阶段不新增模组依赖、不加网络包、不改既有 21 条转换配方与既有作物爆炸行为（BRIEF §3.4）。

### 3.2 本阶段目标

把三件内容（D-57 压缩干草块、D-77 压缩膨胀麦、D-58 莫洛托夫）与 5 条新配方落地，并补齐压缩干草块的两条方块标签与两张战利品表。阶段结束时玩家能走通「工作台做转换器 → 转换器换出三件新道具 → 4 个膨胀麦合 1 个压缩膨胀麦 → 投出去火烧一片（原方块一个不少）→ 铺一片压缩干草块，瓜！炸不动但 TNT 炸得动」。

### 3.3 任务清单

| 编号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付 |
|---|---|---|---|---|
| F-67 | 压缩干草块方块属性与抗性矩阵 | M / 1.5 | F-65 | 普通 `Block` + `strength(30.0F, 6.0F).requiresCorrectToolForDrops()`，六面同贴图、无 axis 变体，不新增方块类 |
| F-68 | 压缩膨胀麦投掷物：沿用落点写入 | M / 1.5 | F-66、F-67 | 子类只换写入的方块：`fillBlock()` 抽到父类、子类覆写；干态 3 / 遇水 5 与 3.6e6 抗性底线原样继承 |
| F-69 | 莫洛托夫落点触发器 | M / 1.5 | F-66 | 落点触发一次威力 0 的爆炸（`NEVER_BREAK`、`damageCap = 0`）只当触发器，并打一行落点日志 |
| F-70 | 球内候选枚举与逐格掷火 / 岩浆块 | M / 1.5 | F-69、F-61 | 半径 4 球内「上表面接触空气」的方块逐格掷：85 % 在上方空气位放火、余下放岩浆块，都不替换原方块 |
| F-71 | 转换器合成配方与 `converter_fuel` 标签 | M / 1.5 | F-65 | 有序 3×3（中心工作台 + 八格 `#farmstorm:converter_fuel`）产出 1 个转换器 + 新建该物品标签 |
| F-72 | 压缩膨胀麦无序合成 | S / 0.5 | F-65 | 无序：4 × `farmstorm:puffed_wheat` → 1 × `farmstorm:compressed_puffed_wheat` |
| F-73 | 三条新转换配方 | S / 0.5 | F-65、F-71 | 绯红菌 ⇄ 莫洛托夫 / 甘蔗 ⇄ 桑吉尔蔗 / 金苹果 ⇄ 狂野苹果，双向、经验 3、无催化剂 |
| F-74 | 压缩干草块与桑吉尔蔗战利品表、两条方块标签 | M / 1.5 | F-67 | 两张战利品表 + `mineable/pickaxe` 追加 + 新建 `needs_diamond_tool` + `deployed_crops` 追加 |
| F-75 | Phase 12 出口回归 | S / 0.5 | F-68、F-70、F-71、F-72、F-73、F-74 | 五步闭环一次跑通 + `§11 #7 / #8 / #20` 三条自检 + jar 体积大于 v0.0.1 |

### 3.4 本阶段出口判据

- [ ] **F-67 硬度与掉落**：`/setblock ~ ~ ~ farmstorm:compressed_hay_block` 后看 F3 目标方块面板，并空手左键 30 秒。通过的样子：硬度读数 **30**；空手 30 秒不掉任何物品；换钻石镐一次破坏并取出本体（战利品表在 F-74 落地）。
- [ ] **F-67 抗爆四连**：依次用瓜！、瓜片子、育碧服务器各炸一次，再 `/summon tnt` 引爆一次，每次炸完回读 `/execute if block ~ ~ ~ farmstorm:compressed_hay_block`。通过的样子：四次返回值依次 **1 / 1 / 0 / 0**（A-06：两件瓜的 6.0 阈值破不动，3.6e6 阈值与原版爆炸破得动）。
- [ ] **F-68 逐格一致**：同一坐标先投膨胀麦、再投压缩膨胀麦，各记一次。通过的样子：两次写入的立方体边长一致（干态 27 格 / 水面附近 125 格），位置集合完全相同，唯一差异是方块 ID（`minecraft:hay_block` vs `farmstorm:compressed_hay_block`）；原方块全部被替换且掉落物为 0（`/kill @e[type=item]` 前后物品实体计数相同）。
- [ ] **F-68 抗性底线**：在 `minecraft:bedrock`（抗性 3.6e6）旁边投一颗压缩膨胀麦。通过的样子：逐格 `/execute if block` 读基岩全为真（跳过不写）。
- [ ] **F-68 无新增键**：查 `config/farmstorm-server.toml`。通过的样子：没有新的 `compressed_puffed_wheat.*` 或 `puffed_wheat.*` 键，读的仍是既有的 `puffed_wheat.dryEdge` / `wetEdge`。
- [ ] **F-69 无伤无破坏**：平地投一颗莫洛托夫后回读血量与 3×3×3 的 `/execute if block`。通过的样子：`/data get entity @p Health` 投前投后相同；落点 3×3×3 逐格与投前对齐，一个方块都没被破坏（`NEVER_BREAK` + `damageCap = 0`）。
- [ ] **F-69 键驱动**：看日志行。通过的样子：出现 `莫洛托夫 落点 (x, y, z) 威力 0`，与 `molotov.triggerExplosionPower` 默认值 0 一致；把该键改成 4 后重投出现方块破坏，改回 0 后不再破坏。
- [ ] **F-70 追加不是破坏**：平地投一颗后逐格对比投掷前后。通过的样子：半径 4 球内只有「实心且上方是空气」的格子上方新增火或岩浆块，原方块逐格与投前一致；台阶 / 雪层这类非 `isSolidRender` 的方块上方不放置任何东西。
- [ ] **F-70 比例与耗时**：连投 20 颗，读三次日志数值。通过的样子：`岩浆块 / (火 + 岩浆块)` 落在 **10 %–20 %** 区间（85 : 15 的粗核）；日志三次数值可直接相加核对；服务端无 `Can't keep up` 行。
- [ ] **F-70 半径读 config**：把 `molotov.effectRadius` 改成 `1.0` 后重投。通过的样子：日志里的 `候选 {N}` 明显变小。
- [ ] **F-71 配方形状**：看两个生成物。通过的样子：`data/farmstorm/recipe/converter.json` 的 `type` 为 `minecraft:crafting_shaped`、八个 `key` 位置指向同一个 tag、`result` 为 `farmstorm:converter` 且 `count` 为 1；`data/farmstorm/tags/item/converter_fuel.json` 的 `values` 覆盖 4 个原版农作物（`minecraft:wheat` / `carrot` / `potato` / `beetroot`）与 19 条档案里能当农作物的物品。
- [ ] **F-71 摆一次**：工作台按配方摆一次，再把任一格换成 `minecraft:diamond`。通过的样子：前者得 1 个 `farmstorm:converter`，后者无产物；既有 21 条转换配方 JSON 的 `git diff` 为空。
- [ ] **F-72 无序合成**：看 JSON 与三次摆放。通过的样子：`type` 为 `minecraft:crafting_shapeless`、`ingredients` 为 4 组同一物品、`result.count` 为 1；放 4 个得 1 个，放 3 个与 5 个均无产物。
- [ ] **F-73 三条转换**：看三个 JSON 与两次转换。通过的样子：三个 JSON 的 `type` 为 `farmstorm:converting`、`bidirectional` 为 true、`experience` 为 3；金苹果 ⇄ 狂野苹果正反各一次都成功，`/xp query @s levels` 两次各少 3 点；`farmstorm:converting` 配方在 `src/generated/resources/data/farmstorm/recipe/` 下共 **24** 条。
- [ ] **F-74 两张表两条标签**：看 `src/generated/resources` 与两次挖掘。通过的样子：钻石镐挖掉本体、木镐挖 30 秒不掉；`data/minecraft/tags/block/needs_diamond_tool.json` 的 `values` 为 `["farmstorm:compressed_hay_block"]`；`mineable/pickaxe.json` 是追加（既有条目逐条仍在）不是覆盖；`data/farmstorm/tags/block/deployed_crops.json` 含 `farmstorm:zangief_cane`，`data/farmstorm/loot_table/blocks/zangief_cane.json` 存在且掉自身。
- [ ] **F-75 五步闭环**：单人存档里一次跑通做转换器 → 换出三件 → 4 合 1 → 投莫洛托夫 → 投压缩膨胀麦。通过的样子：每步各留一条 `/give` 或 `/execute if block` 读数；`12-TECH` §11 #7 / #8 / #20 三条全过。
- [ ] **F-75 产物**：比较两个 jar 的体积。通过的样子：`build/libs/farmstorm-0.0.2+mc1.21.1.jar` 存在且体积大于 `dist/farmstorm-0.0.1+mc1.21.1.jar`。
- [ ] **全阶段红线**：`grep -r "CustomPacketPayload" src/main/java`。通过的样子：0 处匹配。

### 3.5 常见翻车点

1. **把 A-07 读成「沿用爆炸」**：`12-TECH` §3.8 的旧表述里有「威力 / 伤害上限沿用膨胀麦」这类话，照抄就会给压缩膨胀麦挂一个 `Explosion`——而膨胀麦根本没有爆炸段，做出来的结果是「铺完方块还多炸掉一圈」。提前发现：`grep -n "Explosion" entity/thrown/CompressedPuffedWheatProjectile.java` 必须 0 命中；两次投掷的 `/execute if block` 逐格结果只在方块 ID 上不同。
2. **照抄「TNT 炸不动压缩干草块」**：那是被 A-06 否掉的写法，照抄的后果是把抗性改成 `strength(30.0F, 1200.0F)` 之类，于是瓜！与 TNT 都炸不动，F-67 的第 ③④ 条同时失败。提前发现：`/setblock` 之后立刻 `/summon tnt` 引爆验一次；抗性值只认 `6.0F`。
3. **莫洛托夫顺手破坏原方块**：D-58 要的是在「上方那个空气位」**追加**火 / 岩浆块，判定条件是「上表面接触空气」；写成「把方块替换成火」或「先在球内破坏再放火」都会让 F-70 的「原方块一个都没少」直接红。提前发现：投掷前后各存一份 3×3×3 的 `/execute if block` 结果做 diff；除 `fireChance` 之外不存在第二个概率键（A-01）。
4. **`converter_fuel` 标签写成固定物品清单**：D-52 要的是 tag，八格全指向 `#farmstorm:converter_fuel`；写成八个 `item` 字段、或把标签内容只写原版 4 种，都会让「19 条档案里的农作物也能当燃料」失效，JSON 形状也对不上 F-71 的第 ① 条。提前发现：`converter.json` 里八个 `key` 的 `tag` 字段逐字相同；`converter_fuel.json` 的 `values` 行数与核对表一致。
5. **三条配方挤在同一次改 `ModRecipeProvider` 时改坏既有 21 条**：`F-71` / `F-72` / `F-73` 共用这个文件，重排 `accept` 顺序或改共享常量会让既有 21 条转换配方的 JSON 变动。提前发现：每次 `runData` 之后 `git status src/generated/resources/data/farmstorm/recipe/`，只允许出现 5 个新增文件，既有 21 个不许出现 `M`。
6. **`compressed_hay_block` 做成可旋转的 pillar**：D-57 明确六面同贴图、不用 `pillar` / `axis`。加了 axis 会让 `blockstates/compressed_hay_block.json` 出现多个变体，`F-96` 的第 ① 条（只有 1 个 `cube_all` 变体）到时返工。提前发现：写下 `strength(...)` 那一行的同时确认没有 `.rotate(...)` 调用。

### 3.6 完成后应向用户回报

- 五步闭环的每步读数（做转换器 / 换三件 / 4 合 1 / 投莫洛托夫 / 投压缩膨胀麦）。
- 抗爆四连的 `/execute if block` 返回值（应为 1 / 1 / 0 / 0）与两次 `/summon tnt` 的结论。
- 莫洛托夫的三类日志：`落点 … 威力 0`、`候选 {N} 火 {F} 岩浆 {M}`，以及连投 20 颗的粗核区间。
- 两张战利品表的 `/loot give` 原文；两条方块标签的 `values` 行。
- 5 条新配方 JSON 的路径清单，与「既有 21 条 `git diff` 为空」的结论。
- 两个 jar 的体积数字。
- 未关闭的问题（尤其是 `ThrownItemRenderer` 对自定义投掷物贴图的取法、`ConverterRecipe` 反查双向配方的键、`FarmstormExplosion` 在 `damageCap = 0` 下的行为）。

---

## 4. Phase 13 · 狂 野 猎 人（F-76 ~ F-85）

> 投喂方式：`14-PROMPT-MAIN-0.0.2.md` 全文 + 本节，一次只喂一个 Phase。

### 4.1 投喂前必须已就绪

- Phase 11 全部通过，重点是 `F-64`（两个效果 + 两个声音事件）与 `F-61`（11 个 `wild_hunter.*` 键）；`F-65` 的 `wild_apple` 物品也在 Phase 11 落地。
- 已读过 BRIEF §7.1 / §7.2 / §7.4 / §7.6 / §7.7、`12-TECH-0.0.2.md` §4.1 / §4.2 / §4.5 / §4.6，以及风险 `RT-10` / `RT-11` / `RT-12` / `RT-14` 四条。
- 客户端类只允许出现在 `client/` 包（`12-TECH` §3.13 的 6 个类）；服务端路径不许 import `net.minecraft.client.*`（`12-TECH` §11 #22）。
- 两条红线已确认：不加 `CustomPacketPayload`（D-50）、不引入 Mixin（D-40）。冲刺必须按 `12-TECH` §4.5 的「客户端预测位移 + 服务端判定」——`ServerPlayer#setDeltaMovement` 不会让客户端玩家移动（RT-12）。
- 本阶段自己产出的唯一贴图是 `assets/farmstorm/textures/misc/white.png`（1×1，F-83 的交付物）；其余 5 张物品 / 方块贴图与 2 个 OGG 在 Phase 15。

### 4.2 本阶段目标

落地两项侵入性效果（整屏灰度 D-61、红色剪影 D-62）与狂野猎人的全套玩法：三项属性加成、5 格 30° 索敌、3 刻吸附冲刺、伤害与击退结算、击杀续时无上限、循环音跟随。阶段出口是吃一颗狂野苹果后的 700 刻内七项能力全部可观察，且 `/effect clear @s` 后七项全部复位。

### 4.3 任务清单

| 编号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付 |
|---|---|---|---|---|
| F-76 | 狂野猎人三项属性加成 | M / 1.5 | F-61、F-64 | 速度 +30 % / 攻速 +30 % / 伤害 +40 %，三个固定 `ResourceLocation` + `ADD_MULTIPLIED_TOTAL`，效果清掉能卸干净 |
| F-77 | 狂野苹果食用与授予效果 | M / 1.5 | F-65、F-76 | `FoodProperties`（营养 4 / 饱和度 1.2）+ 食用后施加 `farmstorm:wild_hunter`（时长读 `wild_hunter.durationTicks` 700，A-02） |
| F-78 | 索敌算法：5 格球与 30° 夹角 | M / 1.5 | F-76 | `resolveTarget()`：半径 5 球内、夹角 ≤ 30°，排除自己 / 同队 / 旁观；夹角最小优先、同夹角取近 + 一行目标日志 |
| F-79 | 冲刺的客户端预测位移 | L / 3.0 | F-78 | 本地按下攻击键 + 自己持有效果 + 本地算出同一目标时给速度，3 刻跨到位；落点被方块占据则本次不吸附 |
| F-80 | 服务端复核与伤害 / 击退结算 | M / 1.5 | F-78 | 收到 `ServerboundSwingPacket` 时用同一套算法独立算目标，结算伤害，并在「成功造成伤害」后调原版 `LivingEntity#knockback` 默认强度 |
| F-81 | 击杀续时无次数上限 | S / 0.5 | F-80 | 每杀一个生物剩余时长 +160 刻、回复 10 点生命，不写任何计数器（D-74） |
| F-82 | 整屏灰度 pass（含 GUI） | L / 3.0 | F-60、F-76 | `RegisterShadersEvent` 注册 `ShaderInstance`、`RenderGuiEvent.Post` 对主 render target 跑一次全屏 pass；含 `shaders/core/grayscale.json` + `.vsh` + `.fsh` 与 `post_effect/grayscale.json` |
| F-83 | 红色剪影：可见 + 不超过 64 格 | M / 1.5 | F-76 | `RenderLivingEvent.Post` 用 `getModel().renderToBuffer(...)` 重绘 + `hasLineOfSight` + 距离 ≤ 64，另产出 `textures/misc/white.png` |
| F-84 | 循环音 `SoundInstance` | M / 1.5 | F-76 | 客户端循环音实例，`isLooping() = true`、每刻跟随玩家位置、可听距离 16 格、`SoundSource.PLAYERS` |
| F-85 | Phase 13 出口回归 | S / 0.5 | F-77、F-79、F-80、F-81、F-82、F-83、F-84 | 七项能力一次跑通 + `§11 #13 / #14 / #15` 三条自检 + 0 包 0 Mixin |

### 4.4 本阶段出口判据

- [ ] **F-76 三项读数**：吃一颗狂野苹果后看 F3。通过的样子：`Speed` +30 %、`Attack Speed` +30 %、`Attack Damage` +40 %，与 config 默认值逐项对；`/effect clear @s farmstorm:wild_hunter` 后三项回到基线。
- [ ] **F-76 键驱动**：把 `wild_hunter.attackDamageBonusPercent` 改成 `100.0` 后重进存档。通过的样子：F3 的伤害读数翻倍；改回 40.0 后恢复。
- [ ] **F-77 时长与数值**：吃一颗后读 `/effect` 与 `/data get entity @s ActiveEffects`。通过的样子：剩余时长 **700 刻 ± 2**、`amplifier` 为 0；hunger 行投前投后差 4 点、`Saturation` 差 4.8（4 × 1.2）。
- [ ] **F-77 双向配方**：用转换器把狂野苹果换回金苹果，再吃金苹果。通过的样子：产出 1 个 `minecraft:golden_apple`；吃金苹果不授予 `farmstorm:wild_hunter`。
- [ ] **F-78 目标日志与夹角**：正前方 5 格内放一只猪并攻击一次。通过的样子：日志出现 `狂野猎人 目标 minecraft:pig 夹角 {A} 度 距离 {D}` 且 `A <= 30`。
- [ ] **F-78 排序与排除**：左右同距离各放一只、再改成同夹角一侧更近；背后放一只、同队玩家与旁观者各一只。通过的样子：夹角更小的那只被选、同夹角取更近；夹角 > 30°、同队玩家、旁观者都不被选；把 `wild_hunter.searchRadius` 改成 `1.0` 后 5 格外的猪不再被选。
- [ ] **F-79 三刻到位**：开效果后对 5 格内 30° 内的猪按一次攻击。通过的样子：F3 坐标前后差 ≈ 初距，跨度发生在 3 刻内；`/data get entity @p Pos` 与目标位置对齐。
- [ ] **F-79 撞墙与占位**：玩家与目标之间放一堵石墙再攻击；再用 `/setblock` 把目标格占满后攻击。通过的样子：前者冲刺停在墙外侧、不穿墙不补位；后者本次不吸附（玩家坐标不变）；`logs/latest.log` 无 `moved too quickly!`。
- [ ] **F-80 伤害与击退**：攻击 30° 内的猪一次，攻击前记血量与 `Pos`。通过的样子：`/data get entity <target> Health` 的减少量等于 F3 显示的加成后伤害；`Pos` 前后差 ≥ 0.2 格（D-75 原版默认强度）。
- [ ] **F-80 对照两组**：攻击 30° 外的生物；攻击同队玩家。通过的样子：前者正常攻击、无吸附、玩家坐标不变；后者既不吸附也不掉血。
- [ ] **F-81 连杀 6 只**：每次击杀后读 `/effect`。通过的样子：每只剩余时长 **+160 刻**、日志出现 6 行 `狂野猎人 续时`；第 6 次之后效果仍在；生命每次 +10 点；`git grep -nE "killCount|maxKill"` 无输出。
- [ ] **F-82 整屏灰度**：开效果后截一张带物品栏与聊天栏的 F3 图。通过的样子：世界**与 GUI 同时**黑白（红心、绿色经验条、聊天栏彩色文字全部为灰度）；客户端日志无 `Shader … could not be loaded` 与 `GL_INVALID`；开与不开效果的 F3 `ms` 差 < 3 ms；`/effect clear` 后 1 刻内恢复彩色。
- [ ] **F-83 剪影三条件**：站 5 格内直视一只猪、65 格外、墙前 / 墙后各一次。通过的样子：只有「≤ 64 格 + 无遮挡 + 非己方」的生物被画红色剪影，自己与同队玩家不画；客户端日志无 `StackOverflowError`，F3 `ms` 差 < 3 ms；把 `highlightRadius` 改成 `8.0` 后 10 格外的生物不再被画。
- [ ] **F-84 循环音**：双人实测距离，再同时 `/playsound minecraft:music.game`。通过的样子：距 8 格能听到、20 格听不到（16 格衰减）；原版背景音乐不被切断（分类是 `PLAYERS` 不是 `MUSIC`）；`/effect clear` 后 1 刻内停；死亡重生不残留；整段效果期间 `latest.log` 里 `wild_hunter_loop` 不超过 2 行。
- [ ] **F-85 出口回归**：跑一遍七项 + 两条结构性断言。通过的样子：`12-TECH` §11 #13 / #14 / #15 三条全过；`/effect clear @s` 后七项全部复位；`grep -r "CustomPacketPayload" src/main/java` 与 `grep -rn "Mixin" src/main/java` 都是 0 处。

### 4.5 常见翻车点

1. **冲刺写成「服务端 `setDeltaMovement`」（RT-12）**：症状是「按了攻击键没反应、日志里目标算对了、伤害也结算了，但人没动」，再配上服务端的移动校验还会被拉回。玩家位移本来就是客户端模拟的，所以驱动必须落在客户端（`12-TECH` §4.5 的客户端预测 + 服务端判定），红线是不为此加包。提前发现：先打一条 `setDeltaMovement` 日志确认客户端有没有位移，再看 `latest.log` 有没有 `moved too quickly!`。
2. **灰度只灰世界（RT-10 / D-61）**：只在 `RenderLevelStageEvent` 跑 pass 而没在 `RenderGuiEvent.Post` 再跑一次，画面就是「世界黑白、物品栏红心还是红的」，F-82 第 ① 条直接红；反向翻车是在 `RenderGuiEvent.Post` 里切换 render target 与其它模组的 GUI 后处理打架（画面全黑 / 只灰半屏）。提前发现：截图必须带物品栏与聊天栏三处都灰；日志查 `[Render thread/ERROR]` 有无 GL 或 shader 编译错误。
3. **剪影用 `EntityRenderDispatcher#render` 重绘（RT-11）**：会递归触发 `RenderLivingEvent.Post` 自身，症状是日志刷 `StackOverflowError` 或帧时间暴涨。只能用 `getModel().renderToBuffer(...)`。提前发现：30 只生物同屏 + 效果开启，F3 `ms` 差 < 3 ms；墙前墙后各站一次确认 `hasLineOfSight` 生效；65 格外确认不画。
4. **狂野苹果自己存一个时长键（A-02）**：`wild_apple.hunterDurationTicks` 已被否掉，时长只有 `wild_hunter.durationTicks` 一个来源；写成两个键会在「吃苹果 700 刻」与「效果被其它来源刷成别的时长」之间打架，toml 叶节点也会变 140 而不是 139。提前发现：`grep -rn "hunterDurationTicks" src/` 命中 0；`wild_apple` 分组只有 `nutrition` 与 `saturationModifier` 两个键。
5. **循环音每刻重发播放指令（D-76）**：服务端在 `PlayerTickEvent` 里 `playSound` 会刷出大量原版包，且玩家移动时声音抖。要写成客户端一个 `SoundInstance` 子类、`isLooping() = true`、每刻更新位置；分类必须是 `SoundSource.PLAYERS`（用 `MUSIC` 会打断原版背景音乐，F-84 第 ② 条会红）。提前发现：整段效果期间 `latest.log` 里 `wild_hunter_loop` 不超过 2 行。
6. **索敌去读客户端鼠标位置（D-64）**：D-64 已把「最靠近准星」等价成「与视线夹角最小」，两端各算一遍同一套确定性算法即可；读鼠标位置等于要求新增同步包，直接撞 D-50。提前发现：`grep -rn "MouseHandler" combat/` 命中 0，且客户端与服务端两个入口调的是同一个静态方法。

### 4.6 完成后应向用户回报

- 三项属性的 F3 读数与 `/effect clear` 前后的对照。
- 一条 `狂野猎人 目标 … 夹角 … 距离 …` 日志原文，加「左右 / 同夹角 / 背后」三组对照结论。
- 冲刺四条：3 刻到位、墙前停住、目标格占满不吸附、无 `moved too quickly!`。
- 连杀 6 只的剩余时长序列与 `git grep killCount` 的空输出。
- 灰度 / 剪影 / 循环音三项证据（`ms` 差、无 `StackOverflowError`、`wild_hunter_loop` 行数、双人听距）。
- 未关闭的问题（尤其是 `RenderGuiEvent.Post` 在 21.1.213 下的 render target 行为、`SoundInstance#attributes` 的可听距离钩子、`entityTranslucentEmissive` 对 alpha 的处理）。

---

## 5. Phase 14 · 桑吉尔蔗（F-86 ~ F-93）

> 投喂方式：`14-PROMPT-MAIN-0.0.2.md` 全文 + 本节，一次只喂一个 Phase。

### 5.1 投喂前必须已就绪

- Phase 11 全部通过，特别是 `F-63`（`ZANGIEF_CANE(10)` 与两处穷尽 switch）、`F-64`（隐藏效果 `zangief_piledriver`）、`F-61`（6 个 `zangief_cane.*` 键）、`F-65`（方块与物品）——本阶段 8 个任务里 7 个依赖它们。
- 已读过 BRIEF §7.3 / §7.4 / §7.5、`12-TECH-0.0.2.md` §5.1–§5.7（三相位与五个客户端子系统的矩阵），以及 `RT-13` / `RT-15` 两条风险。
- `/setblock ~ ~ ~ farmstorm:zangief_cane` 在 Phase 11 已能放；世界里看不见是设计口径（空模型，D-68），模型文件 `models/block/zangief_cane.json` 在 `F-96` 才补——本阶段的可见性验收只认「无 `elements`」，不认模型文件存在，也不认破坏粒子贴图（`F-95`）。
- 同步方案已确认：三相位走隐藏效果的 `amplifier`（0 / 1 / 2），向下的速度走 `setDeltaMovement`（`ServerPlayer` 会经 `ClientboundSetEntityMotionPacket` 广播），全程不加包（D-50）。

### 5.2 本阶段目标

落地部署型陷阱桑吉尔蔗与它的三相位表现（倒转 → 漂浮 VII 共 20 刻 → 逐刻伺服下坠 + 72°/刻 自转），同时验收第三条侵入性效果（输入锁，只留 ESC）与相机（D-59 / D-70）。阶段出口是「踩进 1.5 格内立刻天旋地转、除 ESC 什么键都按不动、约 2 秒内落地、落地后 100 ms 内画面与输入全复位、方块静默消失且不掉落」。

### 5.3 任务清单

| 编号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付 |
|---|---|---|---|---|
| F-86 | 部署方块与陷阱行为接入 | M / 1.5 | F-63、F-65 | `deployed(ZANGIEF_CANE, …)` 走陷阱类隐形口径 + `ZangiefCaneTrap`（`TrapBehavior` 实现）静态单例注册进 `DeployedBehaviors` |
| F-87 | 球形触发判定与豁免 | M / 1.5 | F-61、F-86 | 方块中心到玩家的**球形欧氏距离** ≤ 1.5 触发，跳过旁观 / 创造 / 同队，只认玩家；触发后 `consume()` |
| F-88 | 相位 0：已倒转、等待漂浮 | M / 1.5 | F-64、F-87 | 服务端三相位状态机；相位 0 = `amplifier 0`、时长读 `stageDelayTicks` 14 刻，到期重发同 id 的 `amplifier 1` |
| F-89 | 相位 1：漂浮 VII 共 20 刻 | S / 0.5 | F-88 | `MobEffects.LEVITATION`，amplifier = 等级 − 1 = 6，持续 20 刻，在第 14 刻施加 |
| F-90 | 相位 2：逐刻伺服下坠与落地检测 | M / 1.5 | F-89 | 每刻重设向下速度 = 剩余高度 ÷ 剩余刻数；落地即清效果；目标时长读 `fallTicks` 40 刻（A-03） |
| F-91 | 相机：roll 180 + pitch 取反 + yaw 自转 | M / 1.5 | F-61、F-88 | `ViewportEvent.ComputeCameraAngles`：`setRoll(180f)` + `setPitch(-pitch)` 同时生效；相位 2 每刻 `yaw += 72.0` 累加 |
| F-92 | 输入锁：只留 ESC | L / 3.0 | F-88 | 五个事件各管一段（移动 / 攻击使用 / 1–9 / 滚轮 / 背包聊天），放行 `GLFW_KEY_ESCAPE`，按 21 项逐键手测表验收 |
| F-93 | Phase 14 出口回归 | S / 0.5 | F-90、F-91、F-92 | 三相位时序 + 三个高度落地耗时 + 21 项键位表 + 0 包 |

### 5.4 本阶段出口判据

- [ ] **F-86 方块与单例**：`/setblock ~ ~ ~ farmstorm:zangief_cane`，再看启动日志的 kind → 单例映射行。通过的样子：命令返回成功，世界里无可见几何体但破坏粒子用的是 `zangief_cane.png`；映射里 `ZANGIEF_CANE` 一行指向 `ZangiefCaneTrap`。
- [ ] **F-86 扫描间隔**：看两行 `桑吉尔蔗 扫描` 的日志时间戳。通过的样子：差 **10 刻**。
- [ ] **F-87 触发与不触发**：站到方块中心距 1.0 格内、同轴 2 格、以及「对角 1.5 格外」的位置各一次。通过的样子：只有第一处触发；同轴 2 格不触发；对角 1.5 格外（切比雪夫会误判命中的那类位置）也**不**触发（D-73 球形口径）。
- [ ] **F-87 豁免**：创造模式玩家、旁观者、同队玩家、生物各踩一次。通过的样子：四种全不触发（D-71）。
- [ ] **F-87 静默消失**：触发后看方块与物品实体计数。通过的样子：`/execute if block` 返回 0（方块没了）；`/kill @e[type=item]` 前后物品实体计数相同（掉落 0，D-72 触发路径）。
- [ ] **F-88 相位 0**：触发后立刻读 `/data get entity @s ActiveEffects`。通过的样子：`farmstorm:zangief_piledriver` 的 `amplifier` 为 **0**、剩余 **14 刻 ± 1**；日志出现 `桑吉尔蔗 相位 0`。
- [ ] **F-88 相位切换**：14 刻后再读一次。通过的样子：同一 id 的 `amplifier` 变 **1**，且不产生第二条效果记录；把 `stageDelayTicks` 改成 `30` 后相位 0 持续 30 刻。
- [ ] **F-89 漂浮**：相位 1 期间看 F3 的 Y 与效果回读。通过的样子：Y 逐刻上升；`minecraft:levitation` 的 `amplifier` 为 **6**、剩余 **20 刻**；相位 0 期间（前 14 刻）Y 不变；把 `levitationLevel` 改成 3 后 `amplifier` 变 2。
- [ ] **F-90 三个高度**：平地 / 两格高台 / 四格高台各踩一次。通过的样子：三次日志 `落地耗时 {N} 刻` 的 N 都落在 **40 ± 2**（`12-TECH` §11 #10 的分水岭）。
- [ ] **F-90 落地清理与时间伺服**：落地后读效果；把 `fallTicks` 改成 `80` 后重测三高度；相位 2 中 `/kill @s` 后重生。通过的样子：落地后读不到 `farmstorm:zangief_piledriver`；三个高度都变成约 80 刻；重生后无残留速度与效果。
- [ ] **F-91 倒转与自转**：相位 0 起看画面；相位 2 期间看 `/data get entity @s Rotation`。通过的样子：画面**上下颠倒且侧翻**（roll 与 pitch 同时生效，只做一个时肉眼可辨）；相位 2 每刻 yaw +72°、40 刻累计 2880° = 8 整圈；`Rotation` 读数**不变**（只改渲染层角度）。
- [ ] **F-91 复位**：效果消失后截图对比。通过的样子：1 刻内相机三轴回到效果前的水平线。
- [ ] **F-92 逐键手测**：按 `12-TECH` §10 `RT-13` 的 21 项逐键测并填表。通过的样子：W/A/S/D/空格/Shift 时 F3 坐标不动；左键右键无攻击无使用；1–9 与滚轮切不了快捷栏；E 打不开背包、T 与 `/` 打不开聊天；**ESC 能打开暂停菜单**；效果结束后所有键 1 刻内恢复。
- [ ] **F-93 出口回归**：跑 `12-TECH` §11 #10 / #11 / #12 三条 + 相位日志 + 结构断言。通过的样子：三条全过；四行相位日志（相位 0 / 相位 1 / 相位 2 / 结束）齐全且 `落地耗时 {N} 刻` 出现一次；`grep -r "CustomPacketPayload" src/main/java` 为 0 处。
- [ ] **RT-15 复位**：相位 2 中途 `/effect clear @s`。通过的样子：100 ms 内画面与输入全部恢复，日志出现「状态复位（效果被外部清除）」。

### 5.5 常见翻车点

1. **触发判定照抄切比雪夫**（D-73 是给桑吉尔蔗的显式例外）：陷阱类的既有实现用切比雪夫距离，照抄别人的判定会在「对角 1.5 格外」误触发，而 F-87 第 ② 条打的就是这个位置。提前发现：站到对角 1.0 / 1.5 格处各测一次——切比雪夫会把 1.0 也判成命中，球形判定只认欧氏距离；两个对照（同轴 2 格不触发、对角 1.5 格外不触发）必须一起判。
2. **下坠写成定速（A-03）**：`dropVelocity` 那种「进入相位 2 设一次向下速度」的写法，在四格高台上 40 刻内落不了地、在两格高台上又提前落地。症状是三个高度的 `落地耗时 {N} 刻` 分散在 20 ~ 90 刻。提前发现：三个高度一次测完再签字，只测平地不算过；`fallTicks` 改成 80 后三个数必须一起变大。
3. **相机角度写回玩家真实朝向**：`setPitch(-pitch)` 之后顺手 `player.setXRot(...)`，会让相位 2 期间 `/data get entity @s Rotation` 变化，服务端权威被破（D-59 铁律），落地后镜头朝向还会错乱。提前发现：相位 2 期间读一次 `Rotation`；`grep -rn "setXRot\|setYRot" client/ZangiefCameraController.java` 命中 0。
4. **输入锁只上三个事件（RT-13 / D-60）**：`MovementInputUpdateEvent` + `InteractionKeyMappingTriggered` + `InputEvent.Key` 看着够用，但滚轮切栏走 `InputEvent.MouseScrollingEvent`、背包与聊天走 `ScreenEvent.Opening`——漏这两路，F-92 的第 ③④ 条会红，而且「E 还能开背包」是特别显眼的穿帮。提前发现：按 21 项表逐键手测并留记录，不要抽测。
5. **隐藏效果被外部清掉后客户端不复位（RT-15）**：玩家在相位 2 里 `/effect clear @s` 或被第三方模组的「清负面」清掉，客户端如果只在「效果变化时」读一次状态，就会永远停在倒转 + 锁键。要做客户端每 tick 自检「阶段效果还在不在」，不在就自己复位并打日志。提前发现：进相位 2 后 `/effect clear @s`，100 ms 内画面与键盘必须全部恢复。
6. **相位切换时留下两条效果记录**：切换前忘记移除同 id 的旧效果，`/data get entity @s ActiveEffects` 里会出现两条 `zangief_piledriver`，F-88 第 ② 条「不产生第二条效果记录」直接红。提前发现：14 刻切换后立刻回读 `ActiveEffects` 的条数，再看客户端有没有读到 `null` 的 1 tick 空窗。

### 5.6 完成后应向用户回报

- 四行相位日志（相位 0 / 1 / 2 / 结束）+ 三个高度的 `落地耗时 {N} 刻`。
- 21 项键位手测表（每项「拦住 / 穿透」）与唯一例外 ESC。
- `/data get entity @s ActiveEffects` 在相位 0 / 1 / 2 的三次回读（`amplifier` 与剩余刻数）。
- 相位 2 期间 `/data get entity @s Rotation` 不变的回读。
- `/effect clear @s` 的复位日志行原文。
- 触发消失不掉落的计数前后值（方块与 `item` 实体各一组）。
- 未关闭的问题（尤其是 `ComputeCameraAngles` 的 roll 在 21.1.213 上会不会被后续事件覆盖、客户端读 `getEffect` 的时机、`ScreenEvent.Opening` 对 ESC 路径的判定方式）。

---

## 6. Phase 15 · 资源、音频、进度与验收（F-94 ~ F-99）

> 投喂方式：`14-PROMPT-MAIN-0.0.2.md` 全文 + 本节，一次只喂一个 Phase。本阶段内部顺序：F-94 → F-95 → F-96 → F-98 → F-97 → F-99（F-97 等 F-98）。

### 6.1 投喂前必须已就绪

- Phase 12 / 13 / 14 全部出口判据通过：`F-99` 的依赖是 `F-75` / `F-85` / `F-93` / `F-94` / `F-96` / `F-97`，前面任何一条没过都不许开 F-99。
- `12-TECH-0.0.2.md` §6.1–§6.4（三个源文件与转换参数、转换脚本）、§7.1–§7.4（36 行资源清单 / 39 行语言键 / 核对方法）、§11 的 22 条自检表已读并抄成勾选表。
- **本机有 ffmpeg，但它不在 PATH 上**（BRIEF §2 的 D-53 实测：可用的是 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`，7.1 essentials 带 libvorbis；Trae CN 的 6.1.1 无 libvorbis 不可用；npm / maven / 镜像三个源全部不可达）。跑 `F-94` 时必须传 `-Ffmpeg` 绝对路径。`F-94` 仍排在本阶段最前，早失败早发现（RT-14）；代理不得自行寻找替代源或改转换规格。
- 三个素材的出处与用途已确认（D-66）：`Animals.mp3` → `wild_hunter` 循环音；SF6 片段 `BV1qu411W7ha_846-850.mp3` → 桑吉尔蔗触发音；桌面 JPG → 狂野猎人效果图标。来源声明写进 `README.md`（F-99 的交付物）。
- 本阶段只允许改白名单文件；`src/generated/` 里的一切只由 Provider 生成，不许手改。

### 6.2 本阶段目标

补齐 36 个新增资源文件里剩下的部分（6 张物品 / 方块贴图、2 个 OGG、1 个效果图标、9 个模型侧 JSON）与 39 条语言键、3 个进度，然后把 **298 / 261 / 139 / 77** 四项计数对齐、把 `12-TECH` §11 的 22 条自检逐条跑过，产出可发布的 `dist/farmstorm-0.0.2+mc1.21.1.jar`。

### 6.3 任务清单

| 编号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付 |
|---|---|---|---|---|
| F-94 | 音频与效果图标管线 | L / 3.0 | F-60、F-64、F-84 | `tools/convert_media.ps1`（缺 ffmpeg 报错退出）+ 2 个 mono 64 kbps OGG + `sounds.json`（2 事件各带 subtitle）+ 18×18 效果图标 |
| F-95 | 6 张物品 / 方块贴图 | L / 3.0 | F-60、F-65 | 4 张 16×16 物品图标 + 压缩干草块灰度贴图 + 桑吉尔蔗粒子贴图 |
| F-96 | 方块状态与模型：9 个 JSON | M / 1.5 | F-65、F-67、F-95 | 2 个 blockstate + 2 个方块模型 + 5 个物品模型（桑吉尔蔗模型只有 `textures.particle`、无 `elements`） |
| F-97 | 语言文件 39 条与中英相等 | S / 0.5 | F-61、F-64、F-65、F-98 | `ModLangProvider` 落 39 条 → 两份各 261 键且键集相等；`tooltip.farmstorm.*` 仍 16 条 |
| F-98 | 三个新进度 | S / 0.5 | F-69、F-76、F-87 | `first_molotov` / `zangief_hit` / `first_wild_hunter`，父节点分别为 `first_throw` / `first_deploy` / `root`，进度总数 15 |
| F-99 | 四项计数核对、三模式回归与打包 | L / 3.0 | F-75、F-85、F-93、F-94、F-96、F-97 | 298 / 261 / 139 / 77 四项对齐 + 22 条自检逐条留读数 + `dist` jar + `README.md` 的索引节与素材声明节 |

### 6.4 本阶段出口判据

- [ ] **F-94 两个 OGG**：对两个 `.ogg` 各跑一次 `ffprobe`。通过的样子：编码 `vorbis`、声道 1（单声道）、码率 ≈ 64 kbps、时长 > 0 秒。
- [ ] **F-94 缺 ffmpeg 的失败路径**：在没有 ffmpeg 的机器上跑脚本。通过的样子：退出码非 0，stdout 打印安装指引，**不回退扩展名、不静默跳过**（D-53）。
- [ ] **F-94 `sounds.json` 与图标**：读文件并进游戏。通过的样子：2 个事件各带 `subtitle`、引用的两个文件都存在（`processResources` 不报缺文件）；`wild_hunter.png` 尺寸 18×18，HUD 图标可见且不是紫黑。
- [ ] **F-94 声音可听与来源声明**：游戏内两条 `/playsound`，再读 `README.md`。通过的样子：两条都能听到；`README` 与 Release 说明里三个素材的来源与非商业自用声明齐全（D-66）。
- [ ] **F-95 六张 PNG**：逐张读尺寸。通过的样子：6 个文件都在且都是 16×16；创造栏 4 个新作物图标不是紫黑；压缩干草块六面贴图一致、色相 0（灰度）、与原版 `hay_block_side` 同形、亮度差 ≤ 8 %。
- [ ] **F-95 桑吉尔蔗粒子**：世界里放一个再挖掉。通过的样子：方块本身无可见几何体，破坏粒子用的是 `zangief_cane.png`（截图对比）。
- [ ] **F-96 九个 JSON**：读文件。通过的样子：`blockstates/compressed_hay_block.json` 只有 **1 个变体**（`cube_all`）且 `model` 指向 `farmstorm:block/compressed_hay_block`；`models/block/zangief_cane.json` 只有 `textures.particle`、**无 `elements`**；`models/item/zangief_cane.json` 的 `parent` 为 `minecraft:item/generated`；`models/item/compressed_hay_block.json` 的 `parent` 为 `farmstorm:block/compressed_hay_block`。
- [ ] **F-96 物品栏**：打开创造栏与背包。通过的样子：5 个物品都正常显示，无紫黑、无缺模型警告。
- [ ] **F-97 键数与相等性**：`runData` 后比两份 `.json`。通过的样子：两份各 **261** 键（222 + 39），`set(zh) == set(en)` 为 True；39 条的六类构成逐类对得上（config 22 + item 5 + block 2 + effect 2 + subtitles 2 + advancements 6）；`tooltip.farmstorm.*` 仍是 16 条。
- [ ] **F-98 三条进度**：分别真触发一次，再 `/advancement` 查询。通过的样子：投莫洛托夫弹 `first_molotov`、桑吉尔蔗命中弹 `zangief_hit`、获得效果弹 `first_wild_hunter`，三条各一次 toast；`/advancement` 能查到三条 ID；进度总数 **15**；既有 12 条的 `git diff` 为空。
- [ ] **F-98 JSON 形状**：读三个文件。通过的样子：`parent` 分别为 `farmstorm:first_throw` / `farmstorm:first_deploy` / `farmstorm:root`，`frame` 为 `task`、`show_toast` 为 true。
- [ ] **F-99 资源 298**：`Get-ChildItem -Recurse -File src\main\resources, src\generated\resources`，再扣掉 `.cache` 的 11 个无扩展名文件。通过的样子：总数 **298**；JSON **193**、PNG **99**、OGG **2**、GLSL **2**、TOML 1、`pack.mcmeta` 1。
- [ ] **F-99 其余三数**：语言键、toml、自检行。通过的样子：两份语言文件各 **261** 键且键集相等；`config/farmstorm-server.toml` **139** 键；日志里 `自检 注册对象 77 个 …` 与 `自检 实体渲染器 11/11 → OK` 两行都在。
- [ ] **F-99 22 条自检**：按 `12-TECH` §11 逐条跑。通过的样子：22 条全过，逐条留读数（一条一行：编号 / 结论 / 证据位置）。
- [ ] **F-99 结构与专用服务器**：跑两条 `grep` 与一次起服。通过的样子：`grep -r "CustomPacketPayload" src/main/java` 为 0 处、`grep -rn "Mixin" src/main/java` 为 0 处；专用服务器启动无 `NoClassDefFoundError`，客户端连入后效果相关功能正常。
- [ ] **F-99 jar 内容**：`jar tf dist/farmstorm-0.0.2+mc1.21.1.jar`。通过的样子：能看到两个 `.ogg`、`assets/farmstorm/sounds.json`、以及 4 个灰度相关资源（`shaders/core/grayscale.json` / `.vsh` / `.fsh`、`post_effect/grayscale.json`）。

### 6.5 常见翻车点

1. **缺 ffmpeg 时脚本静默跳过（RT-14 / D-53）**：把「没拿到可用 ffmpeg」当成「这一步跳过」，构建照样绿、jar 里没有 `.ogg`、两个效果无声——要等到 F-99 的 `jar tf` 才发现。D-53 要求缺 ffmpeg **直接报错退出并打印安装指引**、退出码非 0。提前发现：先跑 `pwsh -File tools/convert_media.ps1 -CheckOnly`——不传 `-Ffmpeg` 且 PATH 上没有时必须打印安装指引且退出码 1；传入可用路径时必须报环境可用且退出码 0。
2. **`.ogg` 用 mp3 改名冒充**：`jar tf` 里有名字、`processResources` 也不报错，但运行时不播放，`ffprobe` 一跑就露。提前发现：每个 `.ogg` 都跑一次 `ffprobe` 校验 `vorbis` / 单声道 / 64 kbps，不许只看文件名与体积。
3. **桑吉尔蔗模型带 `elements`（D-68）**：照普通方块写模型，世界里就会看见一个方块——桑吉尔蔗必须完全隐形，只保留 `textures.particle`。这是「一旦错了特别显眼、但极易在 DataGen 里被复制粘贴带进来」的错误。提前发现：`models/block/zangief_cane.json` 里 `elements` 命中数必须为 0；世界里 `/setblock` 后截图确认看不见。
4. **语言文件只加中文（A-04 / `12-TECH` §7.4）**：`farmstorm.configuration.*` 22 条的键名必须与 toml 的 22 个键逐字对应（正文写 `<group>.<key>`，语言键写 `farmstorm.configuration.<group>.<key>`）；只在 `zh_cn.json` 加而漏 `en_us.json`，`set(zh) == set(en)` 立刻变 False。提前发现：每次 `runData` 后跑一次键集 diff，同时数 261 与 `tooltip.farmstorm.*` 的 16。
5. **进度父节点写错导致「看得到、验不出」**：`first_molotov` 的父是 `farmstorm:first_throw`、`zangief_hit` 的父是 `first_deploy`、`first_wild_hunter` 的父是 `root`；父路径写错时 `/advancement` 里照样能看到 ID，但真触发时不弹 toast。提前发现：三条各真触发一次看 toast，别只用 `/advancement grant` 代替。
6. **F-97 排在 F-98 之前**：6 个进度文案键依赖 3 条进度路径，先做语言文件就会缺 6 个键，261 变 255。提前发现：`ModAdvancementProvider` 的 `AWARD_REQUIRED` 追加三条之后再动 `ModLangProvider`。

### 6.6 完成后应向用户回报

- 四项计数的命令与结果（298 / 261 / 139 / 77）与资源的六类分解（JSON 193 / PNG 99 / OGG 2 / GLSL 2 / TOML 1 / `pack.mcmeta` 1）。
- 两份语言文件的键数、键集 diff 的空结论、`tooltip.farmstorm.*` 的 16。
- 两个 `.ogg` 的 `ffprobe` 三行原文（编码 / 声道 / 码率），以及缺 ffmpeg 时脚本的退出码与提示原文。
- 22 条自检的逐条读数（一条一行：编号 / 结论 / 证据位置）。
- 三条进度的 toast 与 `/advancement` 查询结果；三条 JSON 的 `parent` 行。
- `jar tf dist/farmstorm-0.0.2+mc1.21.1.jar` 里两个 `.ogg`、`sounds.json`、4 个灰度资源的存在性。
- 两条 `grep` 的 0 命中原行。
- 未关闭的问题（尤其是 `sounds.json` 的 `attenuation_distance` 在 21.1.213 上的语义、`needs_diamond_tool` 与 `requiresCorrectToolForDrops` 叠加后木镐的表现）。

---

## 7. 阶段间的口头交接语（hand-off）

模板（把方括号换成实际内容，一次只发一条）：

~~~text
上一阶段（Phase N · [标题]）已按 15-PROMPT-TASKS-0.0.2.md 的出口判据验收通过：[一句话证据，例如「build 产出 0.0.2 jar、139 条 config 键、四条自检行全 OK」]。
本阶段开始：Phase N+1 · [标题]。
请严格按 15-PROMPT-TASKS-0.0.2.md「Phase N+1」一节的六项执行：先逐条确认「投喂前必须已就绪」并回报，再动第一行代码；只做该节任务清单里的 F-xx，不要提前实现下一阶段，完成后按 14-PROMPT-MAIN-0.0.2.md 的回报格式交回。
~~~

五条可以直接粘贴的实例：

| 交接 | 可直接粘贴的一句话 |
|---|---|
| Phase 11 → 12 | 上一阶段（Phase 11 · 基座与注册增量）已完成：`.tools\gw.cmd build` 退出码 0 并产出 `farmstorm-0.0.2+mc1.21.1.jar`，四条自检行全 OK（19 种作物 / 77 个注册对象 / 16 条浆果池 / 11-11 渲染器），`farmstorm-server.toml` 叶节点 139。本阶段开始 Phase 12 · 压缩物与莫洛托夫！，请按 15-PROMPT-TASKS-0.0.2.md「Phase 12」一节执行，先逐条确认「投喂前必须已就绪」再动第一行代码，只做 F-67 ~ F-75，完成后按 14-PROMPT-MAIN-0.0.2.md 的回报格式交回。 |
| Phase 12 → 13 | 上一阶段（Phase 12 · 压缩物与莫洛托夫！）已完成：5 条新配方可用、投莫洛托夫只追加火与岩浆块且原方块一个不少、压缩干草块对瓜！与瓜片子不破而对育碧服务器与 TNT 破、转换配方总数 24。本阶段开始 Phase 13 · 狂 野 猎 人，请按 15-PROMPT-TASKS-0.0.2.md「Phase 13」一节执行，只做 F-76 ~ F-85，完成后按 14 的回报格式交回。 |
| Phase 13 → 14 | 上一阶段（Phase 13 · 狂 野 猎 人）已完成：三项属性加成、5 格 30° 吸附冲刺、伤害与击退、击杀续时无上限、整屏灰度含 GUI、可见生物红剪影、16 格循环音七项均可观察，`/effect clear` 后全部复位，`CustomPacketPayload` 与 `Mixin` 匹配数都为 0。本阶段开始 Phase 14 · 桑吉尔蔗，请按 15-PROMPT-TASKS-0.0.2.md「Phase 14」一节执行，只做 F-86 ~ F-93，完成后按 14 的回报格式交回。 |
| Phase 14 → 15 | 上一阶段（Phase 14 · 桑吉尔蔗）已完成：三相位（倒转 → 漂浮 VII 20 刻 → 下坠自转）按时序走完，平地 / 两格 / 四格三个高度的落地耗时都落在 40 ± 2 刻，21 项键位手测只有 ESC 例外，`/data get entity @s Rotation` 全程不变。本阶段开始 Phase 15 · 资源、音频、进度与验收，请按 15-PROMPT-TASKS-0.0.2.md「Phase 15」一节执行，内部按 F-94 → F-95 → F-96 → F-98 → F-97 → F-99 的顺序做，完成后按 14 的回报格式交回。 |
| Phase 15 → 收尾 | 上一阶段（Phase 15 · 资源、音频、进度与验收）已完成：资源 298 / 语言 261（中英相等）/ config 139 / 注册对象 77 四项对齐，`12-TECH` §11 的 22 条自检逐条留读数，`dist/farmstorm-0.0.2+mc1.21.1.jar` 已产出。现在按 15-PROMPT-TASKS-0.0.2.md §8 的「全项目收尾提示词」做一次发布前自检，只核对与修复，不新增任何功能。 |

---

## 8. 全项目收尾提示词（Phase 15 通过后投喂）

~~~text
项目收尾。请对 farmstorm v0.0.2 做一次完整交付核对，只做核对与修复，不新增任何功能。

1. 构建
   跑 .tools\gw.cmd clean build（不要直接调 gradle），确认产物为 build/libs/farmstorm-0.0.2+mc1.21.1.jar；
   解包确认 META-INF/neoforge.mods.toml 里版本解析为 0.0.2、modId 为 farmstorm。

2. 资源清点（必须逐类点数，只报总数无效）
   重新跑 .tools\gw.cmd runData，按 12-TECH-0.0.2.md §7.1 / §7.4 的口径逐类计数，
   合计必须是 298 个：JSON 193 / PNG 99 / OGG 2 / GLSL 2 / TOML 1 / pack.mcmeta 1，
   并扣掉 src/generated/resources 下 .cache 的 11 个无扩展名文件（它们不进 jar，不计入 298）。
   再跑一次 runData，git diff --stat 必须为空（没有手改生成物的痕迹）。
   语言文件两份各 261 键、set(zh) == set(en) 为 True；其中 tooltip.farmstorm.* 仍是 16 条。
   config/farmstorm-server.toml 叶节点 139 个，带 farmstorm.configuration. 前缀的语言键也是 139 条。

3. 22 条验收自检
   打开 docs/zh/12-TECH-0.0.2.md §11，逐条跑 #1 ~ #22，输出一张 22 行的核对表，
   每行三列：编号 / 结论 / 证据位置（日志行、命令返回值或文件路径）。
   #1 ~ #4 是启动日志的逐字比对（19 种作物 / 77 个注册对象 / 16 条浆果池 / 11-11 渲染器），
   #21 与 #22 是结构性断言（grep 命中 0 与专用服务器不崩），其余是手测。

4. jar 里必须有的东西
   jar tf dist/farmstorm-0.0.2+mc1.21.1.jar，确认下面三组条目都在：
   ① assets/farmstorm/sounds/wild_hunter_loop.ogg 与 zangief_piledriver.ogg（两个 .ogg）；
   ② assets/farmstorm/sounds.json；
   ③ 4 个灰度相关资源：shaders/core/grayscale.json、grayscale.vsh、grayscale.fsh、post_effect/grayscale.json。
   缺任何一条就停下来汇报，不要宣布可以发布。
   .ogg 的编码不要只看文件名：每个都跑一次 ffprobe，确认 vorbis、单声道、约 64 kbps。

5. 三条红线
   grep -r "CustomPacketPayload" src/main/java 与 grep -rn "Mixin" src/main/java 都是 0 处；
   docs/zh/00~09 与 docs/en/00~09 的 git diff 为空（v0.0.1 冻结件，D-49）；
   15 条既有 CropProfile 字段、既有 117 个 config 键默认值、既有 21 条转换配方、既有 12 个进度
   各给一条「未改」的证据（git diff 只有新增行，或对应文件的 diff 为空）。

6. 40 个任务逐条核对完成定义
   打开 docs/zh/13-TASKS-0.0.2.md，按 F-60 ~ F-99 输出一张 40 行的核对表，
   每行四列：任务 ID / 标题 / 是否达成 / 证据位置。
   判定依据是 14-PROMPT-MAIN-0.0.2.md 的完成定义与 §0.3 的三道闸，
   再把该任务在 13-TASKS 里的验收断言逐条对上；缺一即「未达成」。
   有任何一行「未达成」就停下来汇报，不要宣布可以发布。

7. 回报格式
   按 14-PROMPT-MAIN-0.0.2.md 的回报格式输出，另外追加三张小表：资源清点表、22 条自检表、40 行任务核对表。
   不要贴完整日志（每条命令只贴结论行与第一条错误）；任何没跑过或没验证的项一律写「未验证」，
   不得写成「已通过」。
~~~

收尾时的三条判据（人眼复核，不看代理的自我陈述）：

1. 资源清点表的六类加总 = **298**，且每一行都能指到一个目录；`.cache` 的 11 个确实没算进去。
2. `12-TECH-0.0.2.md` §11 的 **22 条**全绿，且每条都有证据位置——#9（TNT 炸得动压缩干草块）与 #21（0 个网络包）这两条必须单独点名。
3. 任务核对表 **40 行**，「未达成」为 0；有则回补后重跑本节，不回补不发布。

---

## 9. 本文件与 13-TASKS-0.0.2.md 的一致性（自检记录）

| Phase | `13-TASKS` 的任务区间 | `13-TASKS` 的任务数 | 本文件 §x.3 搬运的任务数 | 是否相等 |
|---|---|---|---|---|
| Phase 11 | F-60 ~ F-66 | 7 | 7 | 相等 |
| Phase 12 | F-67 ~ F-75 | 9 | 9 | 相等 |
| Phase 13 | F-76 ~ F-85 | 10 | 10 | 相等 |
| Phase 14 | F-86 ~ F-93 | 8 | 8 | 相等 |
| Phase 15 | F-94 ~ F-99 | 6 | 6 | 相等 |
| **合计** | **F-60 ~ F-99** | **40** | **40** | **相等（差集为空）** |

其余四项自检：

| 检查项 | 方法 | 结论 |
|---|---|---|
| 任务编号与标题 | 从 `13-TASKS-0.0.2.md` 抽取 40 个 `#### F-xx · 标题`，与本文件 §2.3 ~ §6.3 的「编号 + 标题」逐行比对 | 40 / 40 逐字一致 |
| 复杂度与人日 | 逐 Phase 校对 S / M / L 与折算人日，再与 `13-TASKS` §2 的小计相加 | 11.0 + 10.5 + 16.0 + 11.5 + 11.5 = **60.5 人日**；S 10 / M 23 / L 7 三项计数一致 |
| 依赖列 | 与每个任务块的「依赖」字段逐行比对 | 40 / 40 一致，含 `F-97` 依赖 `F-98` 这一处编号倒序 |
| 裁决引用 | 本文件只引用裁决编号（`A-01` / `A-03` / `A-05` / `A-06` / `A-07`），不复制 `13-TASKS` §0.4 的原文 | 符合单一来源约定；§0.4 的五条按「旧写法 / 正确写法」重写，不改结论 |

维护约定：若 `13-TASKS-0.0.2.md` 修订了某个任务的标题、依赖或人日，本节与 §2.3 ~ §6.3 要重跑一次比对；本文件不自行改标题、不自行改编号、不自行加任务。

## 校订注

> 本节与英文版末尾的 Translator’s notes 对称：那边记录译名与翻译取舍，这边记录中文侧的撰写口径。

1. **权威版本**：本文件是中文原稿，也是 v0.0.2 的权威版本；英文版是同源翻译，两者逐节对应。中英数字必须始终相等——改任一数字时两边同改，**冲突以本文件为准**。
2. **只引用编号，不重写约束**：`D-xx` 回 `10-DECISIONS`、`RT-xx` 与 `§11 #n` 回 `12-TECH`、`A-0x` 与 `F-xx` 回 `13-TASKS`。发现编号指向不存在的条目时**先上报**，不要自行补号或改号。
3. **任务区间必须闭合**：本文件覆盖 F-60 ~ F-99 全部 40 个任务，与 `13-TASKS` 的任务集合应完全一致；增删任务时两边同时改。
