# 37 号 v0.2.0 任务拆分（Task Breakdown）

> 依据：34 号（25 决策 + 19 裁定）/ 35 号（11 需求 + 40 边界）/ 36 号（97 注册 / 46 动作面 / 175 config / 36 验收）、任务 F-213 起、Phase 32 起、M28 起、裁决 A-62 起
>
> 本文把 34 / 35 / 36 三份规格拆成 **34 个「一次可独立提交、可独立验证」的任务**，编号 **F-213 ~ F-246**，Phase **32 ~ 39** 与里程碑 **M28 ~ M35** 一一对应，裁决 **A-62 ~ A-69**（8 条，A-62 由 36 号 §10 第 7 条点名要求，A-63 ~ A-69 是本文写作期新立）。
> 覆盖：36 号 §3 的 **46 行动作面**（表 A，46/46）、注册面 **93 + 4 = 97**（表 B）、config **166 − 1 + 10 = 175 键 / 58 + 2 = 60 组**（表 C）、语言键 **323 − 3 + 17 = 337**（表 D）、风险 **RT-42 ~ RT-50 九条**（表 E）、待验证 API **13 条**（表 F）；未覆盖 0。
> 本文只读上游：不改 `docs/zh/34` ~ `docs/zh/36`、不改 `docs/en`、不改 `src/`、不改 `.work/` 的三份台账；只新建本文件。

---

## 0. 阅读约定

### 0.1 任务粒度（沿用 0.1.1 的五条）

一个任务 = 一次可独立提交、可独立验证的改动。五条判据沿用 31 号 §0.1（源头是 25 号 §0.1），本版不改写、只重述：

1. **一个任务 = 一次可独立提交、可独立验证的改动。**
2. **提交后 `.tools\gw.cmd build` 退出码 0**，且不依赖同批次里尚未提交的任务。
3. **每个任务自带至少一条可观察断言**，断言里必须出现具体数值、键名或路径。
4. **回滚该提交不会让其它已完成任务失效**（只影响依赖它的后续任务，依赖写在 §1 表格的「依赖」列与任务标题行）。
5. **不跨 Phase 边界**：一个任务的交付物全部落在它所属 Phase 的名称范围内。

三条**本版特有**的硬约定（写在这里，任务里不再重复）：

- **A-62**：`FarmstormConfig.java:227` 的字段声明**保留**，只注释 `:909-912` 的定义。任何任务都不许改 `FarmlandLedger` / `FarmlandGuard` 两个类（一行不改），也不许把该键改成硬编码常量。
- **A-63**：幸运浆果池的 11 条**由 `tier()` 推导**，不许写成第二份手写 id 清单。
- **A-69**：`farmstorm_farmland.dat` 的「不读不写」在 **F-233 之后**才成立——`.dat` 的最后一个运行期读者是 `settleOnRemoval`（`OutpostCoreBlockEntity.java:353` 的 `FarmlandLedger.of`），它到 F-233 才被整段改写。F-220 ~ F-233 之间拆核仍会读写该文件，任何任务都不许在 F-233 之前把「`.dat` mtime 不变」写成验收断言。

### 0.2 复杂度与人日换算（S/M/L → 0.5/1.5/3.0）

换算口径与 31 号 §0.2（源头是 25 号 §0.2）完全一致，本版不调整：

| 复杂度 | 含义 | 折算人日 |
|---|---|---|
| S | 单文件或纯数据；≤ 1 个类，或 ≤ 3 个文件里的字面量替换 | **0.5** |
| M | 2 ~ 4 个文件，跨 1 ~ 2 个包，或新增 1 个完整行为实现 | **1.5** |
| L | ≥ 5 个文件或跨 3 个包，含运行期状态机 / 序列化 / 容器契约 / 渲染管线 / 跨类接口改造 | **3.0** |

本版 34 个任务按此分布：**S 11 / M 9 / L 14**，折算 `11 × 0.5 + 9 × 1.5 + 14 × 3.0 = 5.5 + 13.5 + 42.0 = **61.0 人日**`（不含缓冲）。

四条**不按「改一行」估时**的说明：

1. **凡「全项目第一次」按上一档计**：F-223（第一段村民交易代码，全仓 `villager` / `trade` 0 命中）、F-225（第一张模组 `*.png.mcmeta`）、F-226（第一次「非容器 BE 升级成容器」）、F-238 / F-240（第一次在世界空间画线）都按 L 计，不按「加一个类」计 0.5。
2. **凡改 record 契约按 L 计**：F-214 的实际 diff 不大，但它是 22 条档案 + 3 个私有工厂（36 号 §2.1 记为「10 处构造点 + 15 处工厂实参」）的一次性契约变更，且**不能拆**（见 F-214 的易漏点①）。
3. **凡容器 / 菜单契约改造按 L 计**：F-226 的 7 个 `Container` 方法与 NBT 兼容、F-227 的 54 + 36 槽位版式与 `quickMoveStack`，都不是「照着抄 216 行」能完成的工作量。
4. **凡验收任务不按「跑一遍」估时**：F-244 覆盖 36 条、F-245 覆盖 40 条，每条至少一次构造 + 读数 + 记录，按 L 计。

### 0.3 **中间态窗口**（本版特有的第 6 条粒度规则）

**事实**：注册自检在 `Farmstorm.java:147-150` 里有 **5 个常量**——`total == 93`、`items == 32`、`blocks == 26`、`blockEntities == 7`、`menuTypes == 1`（其余 8 类也各有一个常量，本版不变）。本版注册面净增 4 项，而这 4 项**分两批**落地（培养器三件套一批、核心菜单一批），断言又只能在最后一次性改齐 ⇒ 必然存在一个「实际数已经变了、断言常量还是 93」的窗口。

**窗口 = F-224（开）→ F-228（关）**。四段状态（每段都可从 `run/logs/latest.log` 读到原文）：

| 段 | 任务边界 | 实际 `total` | 12 类分项 | 自检行后缀 | 说明 |
|---|---|---|---|---|---|
| 窗口前 | F-213 ~ F-223 | **93** | 与 0.1.1 相同 | `→ OK` | 这 11 个任务可以正常断言 OK |
| 状态 A | F-224 落地后 | **96** | `items 33 / blocks 27 / block_entities 8 / menu_types 1` | `→ FAIL` | 培养器三件套 +3 |
| 状态 B | F-227 落地后 | **97** | `… / menu_types 2` | `→ FAIL` | 菜单 +1；**数字全对，但断言常量仍是 93** |
| 关窗 | F-228 落地后 | **97** | 同上 | `→ OK` | 5 个常量一次改齐 |

**四条窗口纪律**（F-224 ~ F-227 的每个任务都适用；F-228 是关窗任务，第 1 条对它不适用）：

1. **不许**把「`自检 注册对象 … → OK`」写成验收断言。窗口内只能断言 **FAIL 行里的实际分项数字**（状态 A 写 96 与四个分项、状态 B 写 97 与 `menu_types 2`），并把「FAIL 是窗口期的正确值」写进任务正文。唯一允许写 OK 的是关窗任务 F-228——它写的就是「改完之后这一行变 OK」。
2. 窗口内**其它自检行必须绿**：`自检 作物 22 种…`、`自检 作物品质 普通 11 / 稀有 7 / 史诗 4`、`自检 幸运浆果池 11 种…`、`自检 网络包注册`（F-238 之后是两行）。这四条是窗口的**负向判据**——它们任何一条红，都是真故障，不是窗口。
3. **关窗任务 F-228 一次性把 5 个数改齐**（`93 → 97`、`32 → 33`、`26 → 27`、`7 → 8`、`1 → 2`），跑一次起服验日志，且**不夹带任何别的改动**（`git diff` 只动这 5 个数与那段方法注释）。
4. **窗口不止一个，且 config 的总数要拆成两件事**：语言键总数（`ModLangProvider.java:13-16` 注释里的 323）只在收口任务里改，窗口是 **F-215 → F-241**，中途任务**一律不动那段注释**（否则每个任务都要重算一遍注释，且必然有一个任务算错），判据只写「本条键存在 / 不存在」，总数只在 F-241 断言。config 侧的 **`run/config/farmstorm-server.toml` 区间读数（171 / 172 / 173 / 175 键）允许在中间任务断言**——它是跑一次就有的实测值，改了 config 就会变；而 **`FarmstormConfig.java` 里 `b.push(` 的组数与源码注释里写的总键数只在 F-242 收口时断言**——那是需要手工同步的文本。

**为什么窗口关在 F-228 而不是更晚**：F-228 是「最后一个注册增量（F-227）之后的第一件事」。把收口提前，F-229 起的 18 个任务就能自由引用 `→ OK`；压到 Phase 39，则 Phase 36 ~ 38 的每次起服都有一条常红噪声（RT-50 的更正口径：断言只写日志、不 throw，所以红≠崩，但会让真故障被噪声盖住）。

### 0.4 一个任务 = 一个提交

1. **一个任务一个提交**，提交信息以任务号开头（`F-224: …`）；提交内容与任务的「涉及文件」逐条对齐，多一个文件就是超范围。
2. **不夹带既存瑕疵的顺手修**：34 号 §8 登记的 8 条（`CropType.java:17,19,21` 的 8/5/2、`SeedItem.java:13` 的「6 个」、`FarmstormBlocks.java:25` 的「14 个」、`LuckyBerryRoller.java:170` 的「14 种池」等）本版**一条都不改**；它们出现在「易漏点」里是为了「别顺手改」，不是待办。
3. **回滚粒度 = 提交粒度**：每个任务的验收断言必须在**只回滚这一个提交**之后仍然描述得通（不依赖同批次的其它提交）。
4. **判据不写「调用点数 = N」**：一律换成文件数 / 键数 / 命中数 / 日志行 / 存档字段（A-37 的同一精神，31 号 §0.1）。

---

## 1. 任务总表

列：任务号 / 标题 / Phase / 复杂度 / 人日 / 依赖 / 交付物 / 对应决策 / 覆盖的 36 号动作面行号（`表外` = 不在 36 号 §3 的 46 行里，理由见 §5 的裁决）。

| 任务号 | 标题 | Phase | 复杂度 | 人日 | 依赖 | 交付物 | 对应决策 | 动作面行号 |
|---|---|---|---|---|---|---|---|---|
| F-213 | 版本号 0.2.0 与两支脚本的产物名 | 32 | S | 0.5 | — | `gradle.properties`、两支 `.cmd` | D-148、A-65 | 表外 |
| F-214 | 品质载体：`CropTier` + `CropProfile` 第 9 字段 + 22 条档案 | 32 | L | 3.0 | — | 新 `util/CropTier.java`、`util/CropProfile.java`、`Farmstorm.java` 品质自检行 | D-149、D-150 | #6、#7、#8 |
| F-215 | 品质视觉：名色 + tooltip 一行 + 4 条语言键 | 32 | M | 1.5 | F-214 | 3 个物品类 + `ModLangProvider` | D-151 | #10、#11、#12、#13 |
| F-216 | 幸运浆果池 11 与断言同提交 | 33 | S | 0.5 | F-214 | `util/CropProfile.java`、`Farmstorm.java` | D-156、A-63、RT-50 | #9、#46（池半条） |
| F-217 | 种植路径①：`isPlantableSoil` 恒 `false` | 33 | S | 0.5 | — | `item/ModifiedCropItem.java` | D-152 | #14、#16 |
| F-218 | 种植路径②：`SeedItem#useOn` 返回 `PASS` | 33 | S | 0.5 | — | `item/SeedItem.java` | D-152 | #15 |
| F-219 | 转换器 `useWithoutItem` 返回 `PASS` | 33 | S | 0.5 | — | `block/ConverterBlock.java` | D-170 | #44 |
| F-220 | 耕地体系停用：5 处注释 + 键定义 + 字段保留 | 34 | M | 1.5 | — | `event/FarmstormGameEvents.java`、`config/FarmstormConfig.java` | D-153、D-154、A-62、RT-42 | #17~#22、#25、#26 |
| F-221 | 3 条耕地语言键停用 | 34 | S | 0.5 | F-220 | `datagen/ModLangProvider.java` | D-153 | #23、#24 |
| F-222 | `trade` 组 6 键 + 6 条 config 语言键 | 34 | M | 1.5 | F-220 | `config/FarmstormConfig.java`、`ModLangProvider` | D-159 | #45（+6） |
| F-223 | 农民交易 3 项：`VillagerTradeEvents` + `TierTrade` | 34 | L | 3.0 | F-222 | 2 个新 `event/` 类、`Farmstorm.java` 挂载 | D-157、D-158、D-159、RT-46 | #27、#28、#29 |
| F-224 | 培养器注册三件套 + 创造栏 | 35 | L | 3.0 | — | 新 `block/`、`blockentity/` 2 类 + 3 个 registry + 创造栏 | D-160、D-171、A-64 | #1、#2、#3、#30、#31 |
| F-225 | 培养器资源、双模型与 datagen 三处 | 35 | L | 3.0 | F-224 | 7 个资源文件 + 3 个 datagen provider | D-160、D-161、RT-47 | 表外（36 §5） |
| F-226 | 核心改容器：54 格 + `Items` + 两个同步方法 | 35 | L | 3.0 | — | `blockentity/OutpostCoreBlockEntity.java`、config、lang | D-166、RT-44 | #33、#34、#35、#36、#45（+1） |
| F-227 | 核心菜单 + 屏幕 + `menu_types` +1 | 35 | L | 3.0 | F-226 | 新 `menu/`、`client/` 2 类 + 1 个 registry + 屏幕注册 | D-166、D-171 | #4、#40 |
| F-228 | 注册自检 97 收口（关窗） | 35 | S | 0.5 | F-224、F-227 | `Farmstorm.java:147-150` 与方法注释 | D-171 | #5、#46（注册半条） |
| F-229 | 核心存储打开：两道闸 | 36 | M | 1.5 | F-227 | `block/OutpostCoreBlock.java`、菜单 `stillValid` | D-166、裁定 #17 | #38 |
| F-230 | 核心破坏掉落 | 36 | S | 0.5 | F-226 | `block/OutpostCoreBlock.java:101-107` | D-167 | #39 |
| F-231 | 核侧 16 条绑定列表 + `cultivatorCapacity` | 36 | M | 1.5 | F-226 | `OutpostCoreBlockEntity`、config、lang | D-165 | #34（`Cultivators`）、#45（+1） |
| F-232 | 放置时绑定：四判据 + 两侧写入 | 36 | L | 3.0 | F-224、F-231 | `block/CultivatorBlock.java`、`CultivatorBlockEntity`、索引查询 | D-165 | #30、#31（缓存） |
| F-233 | 迁移三出口：`settleOnRemoval` 整段改写 | 36 | L | 3.0 | F-232 | `blockentity/OutpostCoreBlockEntity.java:341-394` | D-168、RT-43、A-69 | #37 |
| F-234 | `cultivator` 组 2 键 + 2 条语言键 | 37 | S | 0.5 | F-224 | `config/FarmstormConfig.java`、`ModLangProvider` | D-163 | #32（配置面）、#45（+2） |
| F-235 | 状态机、四道准入闸与放入 | 37 | L | 3.0 | F-232、F-234 | `CultivatorBlockEntity`、`CultivatorBlock` | D-160、D-162、D-164 | #30（准入）、#31（状态机） |
| F-236 | 产出与核满暂停 | 37 | L | 3.0 | F-226、F-235 | `CultivatorBlockEntity`、`OutpostCoreBlockEntity` 的整批插入 | D-163、D-164 | #32 |
| F-237 | 培养器破坏掉落 | 37 | S | 0.5 | F-224 | `block/CultivatorBlock.java` 的 `onRemove` | D-160 补充 | #31（掉落） |
| F-238 | 连线包 + 注册 + 协议版本 2 | 38 | M | 1.5 | — | 新 `network/CultivatorLinkPayload.java`、`FarmstormNetwork` | D-169、D-172、RT-45 | #41、#43 |
| F-239 | 连线广播器：范围与五个时机 | 38 | L | 3.0 | F-233、F-238 | 新 `network/CultivatorLinkBroadcaster.java` + 请求点 | D-169 | #42（broadcaster） |
| F-240 | 客户端只读与线框渲染 | 38 | L | 3.0 | F-238 | 新 `client/ClientCultivatorLinks.java`、`client/CultivatorLinkRenderer.java` | D-169、RT-45 | #42（client 2 类） |
| F-241 | 语言键总账 337 与类注释 | 39 | M | 1.5 | F-215、F-221、F-222、F-224、F-226、F-227、F-231、F-234 | 两份 lang + `ModLangProvider` 注释 | 36 §4.3 | 表外（36 §4.3） |
| F-242 | config 总账 175 键 / 60 组 | 39 | M | 1.5 | F-220、F-222、F-226、F-231、F-234 | `run/config/farmstorm-server.toml`（重生成）+ 逐组核对 | 36 §4.1 | #45（总账） |
| F-243 | 验证工具同步 97 / 175 / 337 | 39 | S | 0.5 | F-228、F-241、F-242 | `tools/verify_project.py` | A-66 | 表外 |
| F-244 | 36 号 §8 的 36 条验收 | 39 | L | 3.0 | F-215~F-240（见 §3） | 验收记录 `.work/v020-verify/acceptance.md` | 36 §8 | —（验收面） |
| F-245 | 35 号 §12 的 40 条边界复跑 | 39 | L | 3.0 | F-244 | 边界记录 `.work/v020-verify/boundaries.md` | 35 §12 | —（边界面） |
| F-246 | 红线、注册面与上游一致性签字 | 39 | M | 1.5 | F-243、F-244、F-245 | 签字记录 `.work/v020-verify/signoff.md` | D-172、A-62~A-69 | —（收口面） |

任务数：`246 − 213 + 1 = 34`，连续无跳号；Phase 8 个；里程碑 8 个。

---

## 2. Phase 32 ~ 39 的任务详情

按 Phase 分组。每个任务固定六节：**目标 / 涉及文件（带行号）/ 具体改动 / 实现要点 / 易漏点 / 验收断言**；依赖写在该任务的标题行里（`依赖 —` 表示无）。

### Phase 32 · 基线与品质载体（F-213 ~ F-215）

阶段目标：把「版本号」与「品质」两件事钉死——前者是全部产物的名字来源，后者是交易 / 培养器准入 / 幸运浆果池三处的**唯一数据源**（RT-49）。出口判据：`farmstorm-0.2.0+mc1.21.1.jar` 产出、启动日志出现 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`、29 个物品名按档着色且贴图 0 改动。

#### F-213 · 版本号 0.2.0 与两支脚本的产物名（S · 0.5 人日｜依赖 —）

- **目标**：把 `mod_version` 改成 0.2.0，并让两支 `.cmd` 里写死的 jar 名跟着改，使「一键构建」的复制步骤与「检查环境」的存在性检查仍指向真实产物（D-148；脚本行不在 36 号 §3 的 46 行里，见 A-65）。
- **涉及文件**：`gradle.properties:11`；`一键构建.cmd:11-12`；`检查环境.cmd:14`。
- **具体改动**：`mod_version=0.1.1` → `mod_version=0.2.0`；`一键构建.cmd:11` 与 `:12` 的源名（含 `-sources.jar` 那一行）里的 `0.1.1` → `0.2.0`；`检查环境.cmd:14` 的 `if exist` 路径同样替换。`minecraft_version` / `neo_version` / toolchain 三行一字不动（36 号 §1.1）。
- **实现要点**：jar 名由 `mod_version` 拼出，改完必须真跑一次构建才能确认新名；两支脚本是**产物面的载体**，不是版本号来源（版本号只有 `gradle.properties:11` 一处，D-148 补充）。
- **易漏点**：① `:12` 的 `-sources.jar` 那一行最容易只改主 jar；② `检查环境.cmd:14` 的提示文案「先跑 一键构建.cmd」与同一个路径绑在一起，漏改会在环境检查里报「jar: 缺失」；③ 两支脚本的 banner 不再写版本串（0.1.1 期已统一），本任务不要顺手加。
- **验收断言**：
  1. `.tools\gw.cmd build` 退出码 0，`build/libs/` 下有 `farmstorm-0.2.0+mc1.21.1.jar` 与 `farmstorm-0.2.0+mc1.21.1-sources.jar`，没有 `farmstorm-0.1.1*` 残留。
  2. `farmstorm-0.1.1` 在两支脚本里的命中数 = **0 / 0**；`farmstorm-0.2.0+mc1.21.1` 的命中数 = **2**（`一键构建.cmd`）/ **1**（`检查环境.cmd`）。
  3. `jar tf build/libs/farmstorm-0.2.0+mc1.21.1.jar` 含 `META-INF/neoforge.mods.toml`，其 `version` 行解析为 `0.2.0`。

#### F-214 · 品质载体：`CropTier` + `CropProfile` 第 9 字段 + 22 条档案（L · 3.0 人日｜依赖 —）

- **目标**：22 条作物档案各获得**唯一一档**（普通 11 / 稀有 7 / 史诗 4，34 号 §4.1），载体是 `CropProfile` 的第 9 个字段与一个新枚举（D-150）；**7 处直接 `new CropProfile(...)` 与 15 处工厂实参必须一次改完**，否则编译不过。
- **涉及文件**：新建 `util/CropTier.java`；`util/CropProfile.java:32-40`（record 头）、`:47-128`（22 条档案，其中直接构造的 7 处在 `:58`、`:87`、`:92`、`:97`、`:102`、`:117`、`:125`）、`:191-207`（三个私有工厂 `crop` / `throwable` / `deployed`）；`Farmstorm.java:112-123` 之间的既有自检段（新增一行品质自检，36 号 §8 #4）。
- **具体改动**：record 末尾追加 `CropTier tier`（位置在 `throwStack` 之后，36 号 §2.1 的理由）；`CropTier` 含 `COMMON` / `RARE` / `EPIC`、`translationKey()`、`color()`、`nameStyle()`（36 号 §2.1 的「本文确定」1~3）；22 条档案按 34 号 §4.1 逐条补值；三个工厂各加一个 `CropTier` 形参并转发；启动自检新增一行 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`。
- **实现要点**：品质是**档案级静态属性**，不新增数据组件、不写物品 NBT、不注册任何对象（`data_components` 仍是 1，D-150）；22 条档案只在这一处定义档位，交易 / 准入 / 池三处都读同一个 `tier()`（36 号 §7 RT-49 的缓释 (a)）。
- **易漏点**：① 7 处直接构造必须**同一次提交**全改（漏一处就是 record 参数个数不符，编译失败）——这是本任务不能拆的唯一理由；② 8 条「种子 = 自身」的档案（`blast_carrot`、`ubi_server`、`toxic_ubi_server`、`fang_trap_cactus`、`bamboo_firecracker`、`red_mushroom_puff`、`brown_mushroom_puff`、`lucky_berry`）补档位时**不要顺手改 `seedItem`**，它仍指向自身；③ `Farmstorm.java:112-113` 的 `22 / 12 / 7 / 3 / 2` 五个既有数字一个都不动；④ `compressed_puffed_wheat` 归稀有而它的原料 `puffed_wheat` 归普通是作者口径（34 号 D-149 补充），不是笔误。
- **验收断言**：
  1. `new CropProfile(` 在 `CropProfile.java` 里恰 **7** 处，且每处实参表末尾都有 `CropTier.` 实参。
  2. 启动日志出现 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`（36 号 §8 #4）。
  3. 档案区按档点数：`CropTier.COMMON` **11** 次、`CropTier.RARE` **7** 次、`CropTier.EPIC` **4** 次（枚举声明与工厂形参不计）。
  4. `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK` 一行不变（既有断言不被误改）。

#### F-215 · 品质视觉：名色 + tooltip 一行 + 4 条语言键（M · 1.5 人日｜依赖 F-214）

- **目标**：29 个物品（22 作物 + 7 种子）的名称按档着色（普通白 / 稀有蓝 / 史诗金），tooltip 在既有类别行之后**多一行**品质说明（D-151）；贴图 / 模型 / 物品栏尺寸一律不动。
- **涉及文件**：`item/ModifiedCropItem.java:57-60`（现有 `appendHoverText`，名色覆写的落点）、`:113-127`（`appendCropTooltip`，品质行的唯一实现）；`item/DeployableCropItem.java:18`（**不继承** `ModifiedCropItem`，必须独立覆写）、`:33`（tooltip 调用点）；`item/SeedItem.java:16`（`extends ItemNameBlockItem`）、`:28-31`；`datagen/ModLangProvider.java` 的 tooltip 段（新增 4 条，位置与 `tooltip.farmstorm.category.*` 同段）。
- **具体改动**：三个类各覆写 `getName(ItemStack)` 走 `tier().nameStyle(...)`（36 号 §2.1 的名色表：`WHITE` / `BLUE` / `GOLD`）；`appendCropTooltip` **末尾**追加一行 `tooltip.farmstorm.tier`（外壳灰，档名子组件带自己的颜色，36 号 §2.1）；新增 4 条键：`tooltip.farmstorm.tier.common` / `.rare` / `.epic` + `tooltip.farmstorm.tier`（36 号 §4.3.2 第 4~7 行）。
- **实现要点**：名色必须挂在**名称这个 `Component`** 上（一次覆写、铁砧 / 告示牌 / 聊天栏 / 容器预览全同色），不在 GUI 里二次绘制（D-151 补充）；tooltip **只加一行**，不与类别行合并（那是另一套语义）；22 张作物贴图与 7 张种子贴图不改（36 号 §5 的负向清单）。
- **易漏点**：① `DeployableCropItem` 不继承 `ModifiedCropItem`，只改父类会漏 **7 个**部署型物品；② `SeedItem` 也要单独覆写（7 个）；③ 4 条键是「3 条档名 + 1 条行格式」，不是 1 条也不是 3 条；④ 铁砧改名会走 `CUSTOM_NAME`，名色可能丢——这是 36 号 §9 ⑥ 的待验证项，验证与退路在 F-244（不在本任务里改实现）。
- **验收断言**：
  1. 两份语言文件各新增 4 条 `tooltip.farmstorm.tier*`，`tooltip.farmstorm` 前缀从 16 条变 **20** 条。
  2. 游戏内：普通作物名白、稀有蓝、史诗金；tooltip 在类别行之后多一行「品质：稀有」。
  3. `textures/item` 仍是 **29** 张 PNG，逐张 sha256 与提交前相同（36 号 §5 的负向清单）。

### Phase 33 · 规则面收窄（F-216 ~ F-219）

阶段目标：把 4 条「玩家可感知的规则变化」各自独立落地——浆果池、两条播种路径、转换器。出口判据：吃浆果只出普通、作物与种子都种不下去、部署型物品仍能摆、右键转换器无反应；四条验收互不依赖。

#### F-216 · 幸运浆果池 11 与断言同提交（S · 0.5 人日｜依赖 F-214）

- **目标**：池从 19 条改成 **11 条**且恰好等于普通档全集，**由 `tier()` 推导**（A-63）；启动断言与日志文案**同一次提交**改（D-156、RT-50）。
- **涉及文件**：`util/CropProfile.java:138-143`（池的构造与它头上的注释）；`Farmstorm.java:117-123`（断言 + 日志文案）。
- **具体改动**：池的构造从「排除 3 条」改为「只留 `tier() == COMMON`」，注释同步改成「只普通档 = 11 条」；断言 `poolSize == 19` → `== 11`；日志文案里「（排除 lucky_berry / wild_apple / compressed_puffed_wheat）」改成与推导一致的措辞；断言的后半条（三个 `!=` 判断）**保留**（D-156 原文要求保留「不含三条」的另半条）。
- **实现要点**：算式 `19 − 8 = 11`（被移出的是池内 6 稀有 + 2 史诗）；抽取算法 `LuckyBerryRoller.java:172-180` 一字不改；池的 11 条与 `CropTier.COMMON` 的 11 条必须同源（A-63 的理由：手写 11 条 id 会形成第二份真相，与 36 号 §7 RT-49 的缓释冲突）。
- **易漏点**：① 只改池不改断言 → 每次启动一条常红 FAIL（不 throw、不 exit，服务端照常起，但日志失真，RT-50 的更正口径）；② 只改断言不改池 → 断言红；③ 日志文案是 RT-50 点名的「两处」之一（另一处是断言值）；④ 不要把 `LuckyBerryRoller.java:170` 的 javadoc「14 种池」顺手改——那是 34 号 §8 第 8 条的既存瑕疵。
- **验收断言**：
  1. 启动日志：`自检 幸运浆果池 11 种…→ OK`。
  2. 池的条数 = **11**，且 11 条的 `tier()` 全是 `COMMON`（与 F-214 的第 3 条点数互相校验）。
  3. 吃 `lucky_berry` 命中「生成随机作物」分支时只滚出 11 条普通作物（36 号 §8 #35）。
  4. `== 19` 在 `Farmstorm.java` 里命中 **0** 处。

#### F-217 · 种植路径①：`isPlantableSoil` 恒 `false`（S · 0.5 人日｜依赖 —）

- **目标**：`ModifiedCropItem` 承载的 15 条有作物方块的档案不再能种到地上（D-152 ①）；部署型物品的「摆出」行为保留。
- **涉及文件**：`item/ModifiedCropItem.java:101-107`（判据本体）；`:72`（唯一调用点，**不动**）；`:53-55`（死代码 `canPlantAt`，**不动**）。
- **具体改动**：判据函数体改为返回 `false`，原「是 `FarmBlock` 才允许」的判断留成注释备查（36 号 §2.2 路径①的写法）；`ModifiedCropItem#useOn` 与 `DeployableCropItem#useOn` 都不改。
- **实现要点**：`plantCrop` 在 `:72` 拿到 `false` 后直接回 `PASS`，早于 `:96` 的 `consume(1)` ⇒ 手持物不减；`plantCrop` 的其余分支（无作物方块、`canPlace`、`canSurvive`）一字不动；`FarmstormCropBlock` 不动——`mayPlaceOn` / `canSurvive` 本来就没有覆写（36 号 §2.2 的负向清单）。
- **易漏点**：① 只堵这一条会漏 **7 个种子**（RT-48）——本任务只负责路径①，路径②在 F-218，两条验收**分开写、不合并**；② `canPlantAt`（`:53-55`）全仓 0 调用者，**不要删**（D-152 补充：删死代码属既存清理，不在本版）；③ 7 个部署型物品对耕地右键会变成「摆出部署形态」，这是切断后的**可见后果**，不是漏堵（D-152 补充、35 号 §3.2 的 ①'）。
- **验收断言**：
  1. 手持 15 个有作物方块的作物物品逐一右键耕地：方块不出现、手持物不减、无提示（36 号 §8 #6）。
  2. 7 个部署型物品（`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` / `zangief_cane` / `point_defense_fungus`）右键完整方块顶面：照常摆出（36 号 §8 #8）。
  3. `isPlantableSoil` 的可执行体里没有 `instanceof FarmBlock`（只剩注释行）；方法仍被 `:72` 调用（命中 1 处）。

#### F-218 · 种植路径②：`SeedItem#useOn` 返回 `PASS`（S · 0.5 人日｜依赖 —）

- **目标**：7 个独立种子物品不再能在耕地上种出作物，也不消耗手持物（D-152 ②）。
- **涉及文件**：`item/SeedItem.java:16`（类声明与父类）、`:28-31`（现有唯一覆写，新覆写加在它之后）。
- **具体改动**：新增 `useOn` 覆写返回 `PASS`（36 号 §2.2 路径②），切断 `ItemNameBlockItem` → `BlockItem#useOn` → `place()` 这条原版播种链；播种之外的一切继承不动。
- **实现要点**：`PASS` 是「这里什么都不做」，不是 `FAIL`（`FAIL` 会让调用方误以为被拒绝）；`PASS` 早于 `BlockItem#place` 的 `consume` ⇒ 种子不减；手持种子右键箱子 / 告示牌等其它交互不受影响（36 号 §2.2 的三条语义）。
- **易漏点**：① 7 个种子只有**一份实现**（都走 `SeedItem`），改一处覆盖 7 个——不要去逐个类找；② 8 条「种子 = 自身」的档案（种子物品就是作物本体）**不走这条路**，它们由 F-217 覆盖；③ `SeedItem.java:13` 的类注释写「6 个独立播种物」，实测 7 个——既存笔误，本版不改（34 号 §8 第 4 条）。
- **验收断言**：
  1. 7 个种子逐一右键耕地：方块不出现、种子不减、无提示（36 号 §8 #7）。
  2. 手持种子右键泥土（非耕地）仍无反应。
  3. `SeedItem.java` 里 `InteractionResult.PASS` 命中 **1** 处、`useOn` 命中 **1** 处；`appendHoverText` 仍在（tooltip 不回归）。

#### F-219 · 转换器 `useWithoutItem` 返回 `PASS`（S · 0.5 人日｜依赖 —）

- **目标**：右键转换器无反应、不开界面（D-170）；方块、方块实体、菜单链、29 条配方、创造栏条目**全部保留**。
- **涉及文件**：`block/ConverterBlock.java:78-86`（改）；`:70-76`（`getMenuProvider`，保留）、`:88-96`（掉落，保留）。
- **具体改动**：`useWithoutItem` 的方法体整体替换为返回 `PASS`，客户端与服务端同一句、不再分支（36 号 §2.9）；`getMenuProvider` 留着但没人调用。
- **实现要点**：`PASS` 而非 `SUCCESS` / `CONSUME`（后两者会吞右键）；`ConverterMenu` / `ConverterScreen` / `FarmstormMenus.CONVERTER` 一律不动。
- **易漏点**：① 顺手把屏幕注册或菜单注册删掉 ⇒ `menu_types` 从 1 变 0，注册面变 96，与 97 冲突；② 「暂时移除功能」不包含删 26 条 `converting` 配方（删了将来恢复要重造，D-170 补充）；③ 中文名保持现状「农产品转换器」，**不改语言键**（裁定 #19）。
- **验收断言**：
  1. 空手右键转换器：无界面、无提示、手持物不减（36 号 §8 #36）。
  2. 转换器仍在创造栏、仍能合成；`data/farmstorm/recipe/` 的 json 计数 = **29**（26 `converting` + 2 shaped + 1 shapeless）。
  3. `FarmstormMenus.java` 的 `REGISTER.register` 命中仍为 **1** 处（F-227 之后变 2）。

### Phase 34 · 耕地停用与交易（F-220 ~ F-223）

阶段目标：把耕地体系在运行期清零（不删代码、不删存档、不删方块），同时把「史诗档的唯一入口」——农民交易——建起来。出口判据：锄地不拦、踩踏不保护、`farmstorm-server.toml` 的 `[outpost_core]` 段没有 `farmlandBonus`、农民交易列表出现 3 个本模组项。

#### F-220 · 耕地体系停用：5 处注释 + 键定义 + 字段保留（M · 1.5 人日｜依赖 —）

- **目标**：把耕地体系的 5 个运行期入口注释掉（D-153）：两个注册行、两段实现、一行启动补建；`onServerTickPost` 本身与它里面的另外两行**保留**（否则浮窗包停发）；`farmlandBonus` **只注释定义、保留字段**（A-62）。
- **涉及文件**：`event/FarmstormGameEvents.java:75`（`onRightClickBlock` 注册）、`:76`（`onFarmlandTrample` 注册）、`:241-265`（锄地拦截实现）、`:277-281`（踩踏保护实现）、`:171`（`FarmlandLedger.get(server).backfill(server)`）；`config/FarmstormConfig.java:227`（字段，保留）、`:909-912`（定义，注释）。
- **具体改动**：五处注释各带一行理由「裁定 #3：v0.2.0 撤销耕地，勿恢复」；`:227` 的字段声明原样保留，并在旁边写两行注释——定义已注释 ⇒ 字段运行期为 `null` ⇒ 恢复 `:75` / `:76` 之前**必须先恢复定义**，否则 `FarmlandGuard.java:75` 抛 NPE（36 号 §2.3 的注释原文）；`:170` 的 `CoreChunkTickets.restoreAll`、`:173-175` 的 `CoreListBroadcaster.tick` 一字不动。
- **实现要点**：**三处注释（两个注册行 + backfill）与保留 `onServerTickPost` 必须同一个提交**——只注释注册行不注释实现会留下引用两个保留类的死代码，只注释实现不注释注册行则编译不过（`addListener` 的方法引用会断）；注释 `:171` 之后 `FarmlandLedger.get()` 在运行期的调用点只剩 `settleOnRemoval`（A-69），本任务**不要动** `OutpostCoreBlockEntity`。
- **易漏点**：① `FarmlandLedger`（555 行）/ `FarmlandGuard`（225 行）**一行不改**（D-153、34 号 §7 第 1 条）；② `.dat` **不读、不写、不删**（D-154），但它的「不读不写」要到 F-233 才完全成立（A-69）；③ `settleOnRemoval` 现在还在调 `FarmlandLedger.of`（`:353`）与 `ledger.migrate/remove`（`:384`/`:388`/`:391`），这些行由 F-233 整段改写，本任务不碰；④ 别把 `FarmlandGuard` / `FarmlandLedger` 的 import 从 `FarmstormGameEvents` 删掉——两个方法体还引用着它们（在注释里也要能读）。
- **验收断言**：
  1. 五个注释点全部处于注释态（`onRightClickBlock` / `onFarmlandTrample` 的注册行与实现体、`backfill` 单行），每处上方各有一行「裁定 #3」理由。
  2. `onServerTickPost` **未被注释**，方法体里 `CoreChunkTickets.restoreAll(server)`（`:170`）与 `CoreListBroadcaster.tick(server)`（`:175`）都在，且浮窗每 100 刻的对账日志仍在打（36 号 §8 #10）。
  3. `FarmstormConfig.java:227` 的字段声明仍在、`:909-912` 的定义已注释，且字段旁的两行注释写着「恢复监听器前必须先恢复定义，否则 NPE」（36 号 §8 #12）。
  4. 删旧 `farmstorm-server.toml` 后起服：`[outpost_core]` 段 **2 键**（`maxPerPlayer` / `boundaryParticleIntervalTicks`）、全文 **165 键 / 58 段**（`storageSlots` 与 `cultivatorCapacity` 分别在 F-226 / F-231 加回）。
  5. 游戏内：在非己方核范围内锄地照常成田、踩自家田会踩成泥土、两条动作栏提示不再出现（35 号 §7.4）。

#### F-221 · 3 条耕地语言键停用（S · 0.5 人日｜依赖 F-220）

- **目标**：3 条耕地语言键不再生成（D-153）；`message.farmstorm` 前缀桶清空。
- **涉及文件**：`datagen/ModLangProvider.java:403`（`farmstorm.configuration.outpost_core.farmlandBonus`）、`:412`（`message.farmstorm.farmland.no_core`）、`:413`（`message.farmstorm.farmland.quota_full`），以及它们头上的段注释 `:411`。
- **具体改动**：三条条目从 `ENTRIES` 表里移除（表现为不再写进两份 lang）；`:411` 那段「v0.1.0 的 2 条动作栏提示」一并处理，不留指向空气的注释。
- **实现要点**：「删」在 datagen 里就是「不再产出」；键的定义已在 F-220 注释、提示的发出点也在 F-220 注释，三条键此时没有任何读者（36 号 §4.3.1）。
- **易漏点**：① 只删两条 `message.*` 而漏掉 `farmstorm.configuration.outpost_core.farmlandBonus`（配置页会露裸键名）；② `:411` 的段注释漏删；③ 不要在这里改 `ModLangProvider` 的类注释（总数由 F-241 一次性收口，见 §0.3 纪律 4）。
- **验收断言**：
  1. 两份 lang 里这 3 个键都不存在；`message.farmstorm` 前缀 **0** 条（36 号 §8 #13）。
  2. `farmlandBonus` / `farmland.no_core` / `farmland.quota_full` 在 `ModLangProvider.java` 里的命中数 = **0**。
  3. `git diff` 只显示删除行与那一段注释的改动，没有其它键被顺手改值。

#### F-222 · `trade` 组 6 键 + 6 条 config 语言键（M · 1.5 人日｜依赖 F-220）

- **目标**：新增顶层组 `trade`，三档各 min / max 共 6 键（D-159），并为 6 个键配 6 条 config 语言键（36 号 §4.1.1 #6~#11、§4.3.2 #12~#17）。
- **涉及文件**：`config/FarmstormConfig.java`（字段区 `:227-231` 旁新增 6 个静态字段；静态块里 `outpost_core` 段 `:919` 的 `b.pop()` 之后新增 `trade` 段）；`datagen/ModLangProvider.java` 的 config 段（新增 6 条）。
- **具体改动**：`trade.common.min` = 2、`trade.common.max` = 5、`trade.rare.min` = 10、`trade.rare.max` = 32、`trade.epic.min` = 40、`trade.epic.max` = 64，值域全部 **1–64**（36 号 §4.1.1 五个值域理由的第 5 条）；6 条键名 `farmstorm.configuration.trade.{common,rare,epic}.{min,max}`。
- **实现要点**：`trade` 是**顶层组**（与 `outpost_core` 同级），组数 `58 + 1 = 59`；`min > max` 的交换、`min == max` 取单值这两条**运行期纪律只在 F-223 的读值处实现一次**（36 号 §4.1.1 的三条运行期纪律），本任务不写第二套。
- **易漏点**：① 6 条语言键与 6 个 config 键 1:1（36 号 §4.3.2 的口径 2），漏一条配置页就露裸键名；② 数量的「每次给几个」**不进 config**（35 号 §4.3 第 3 条理由），多一个键就把 175 顶到 176；③ 别把 `trade` 段写成 `outpost_core` 的嵌套组（嵌套组会把组数算成 61）。
- **验收断言**：
  1. 删旧 toml 起服后 `run/config/farmstorm-server.toml` 出现 `[trade]` 段且恰 **6 键**，值依次为 `2 / 5 / 10 / 32 / 40 / 64`。
  2. 全文 **171 键 / 59 段**（`^\s*\[` 计数；这是 toml 区间读数，不是源码总账——源码侧的总账在 F-242）。
  3. 两份 lang 各新增 6 条 `farmstorm.configuration.trade.*`。

#### F-223 · 农民交易 3 项：`VillagerTradeEvents` + `TierTrade`（L · 3.0 人日｜依赖 F-222）

- **目标**：给农民职业的村民加 3 个交易项，一档一项，按等级位次 **1 / 3 / 5** 依次解锁，每项 `maxUses = 1`、只收绿宝石、给出该档的 2 个作物（D-157、D-158、D-159）；不做老档迁移。
- **涉及文件**：新建 `event/VillagerTradeEvents.java`、`event/TierTrade.java`；`Farmstorm.java:71-73` 区（在 `FarmstormGameEvents.register(NeoForge.EVENT_BUS)` 旁挂同一个总线）；读 F-222 的 6 个 `trade` 键。
- **具体改动**：订阅 `VillagerTradesEvent`，只处理 `VillagerProfession.FARMER`，往 `getTrades().get(1)` / `get(3)` / `get(5)` 各添加一项（36 号 §2.4 的三行 handler）；`TierTrade` 实现 `VillagerTrades.ItemListing`，在 `getOffer` 里**一次**抽定作物 id 与价格（36 号 §2.4 的五步：等概率抽 1 条 → 读区间并交换 → 闭区间取随机 → `ItemCost(EMERALD, price)` → `MerchantOffer(..., maxUses = 1, xp = 2, priceMultiplier = 0.0F)`）。
- **实现要点**：抽签与定价都在**首次报价生成时**完成（D-159 的时序纪律），之后固定；不写第二套断货 / 补货（断货由 `maxUses = 1` 承担，补货由原版 `resetUses` 承担，D-158）；不改原版任何交易项、不给种子（35 号 §4.3 附注 1）。
- **易漏点**：① 等级位次写进 2 / 4 ⇒ 交易项一个都看不到（RT-46）；② 价格在成交时重掷 ⇒ 同一笔报价点一次一个价；③ 老档里已满级的农民**不会补发**新交易项（原版语义，本版**不做迁移**：不写补发逻辑、不改 `Offers` NBT，36 号 §2.4.2 的 ⚠️）；④ `getTrades().get(1)` 的 list 若不可变，走 36 号 §9 ① 的退路（`put` 一个新的 `ArrayList`）——这是待验证 ① 的现场。
- **验收断言**：
  1. 农民交易界面出现 **3** 个本模组交易项，等级位次分别是 **1 / 3 / 5**（36 号 §8 #15）。
  2. `/data get entity <villager> Offers.Recipes` 里三项的 `maxUses` = **1**，`buy.count` 分别落在 **[2,5] / [10,32] / [40,64]**（36 号 §8 #17）。
  3. 每项 `sell` 是 **2 个**同档作物；成交一次后项仍在列表、只是变灰（36 号 §8 #16）。
  4. 补货后价格与作物 id 不变（报价不重掷，D-158）。

### Phase 35 · 注册面、资源与容器骨架（F-224 ~ F-228）

阶段目标：把本版全部 4 个注册增量落地（培养器三件套 → 核心菜单），配套资源、容器骨架与菜单骨架，最后一次性关掉注册自检窗口。出口判据：`自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / … / menu_types 2 → OK`；`blockstates` 27、`models` 106、`loot_table` 27。

#### F-224 · 培养器注册三件套 + 创造栏（L · 3.0 人日｜依赖 —）

- **目标**：`farmstorm:cultivator` 的方块 / 方块实体 / 方块物品三件套落地（D-160、D-171），并进创造成分页（A-64）；**这是注册自检窗口的开口**（§0.3 的状态 A）。
- **涉及文件**：新建 `block/CultivatorBlock.java`（`WORKING` 属性、`codec`、`getRenderShape`、`getTicker` 声明）、新建 `blockentity/CultivatorBlockEntity.java`（骨架 + `newBlockEntity` 接线）；`registry/FarmstormBlocks.java`（`:82-90` 的 `outpost_core` 之后 +1）、`registry/FarmstormBlockEntities.java`（`:50` 之后 +1）、`registry/FarmstormItems.java`（`:116-117` 的 `outpost_core` BlockItem 之后 +1）；`registry/FarmstormItems.java:122-129`（`creativeTabItems()`，A-64）；`datagen/ModLangProvider.java`（+2 条方块名 / 物品名）。
- **具体改动**：方块属性照铁砧：`strength(5.0F, 1200.0F)` + `MapColor.METAL` + `requiresCorrectToolForDrops` + `SoundType.ANVIL` + `PushReaction.BLOCK`（36 号 §2.5.1 的逐项表）；`working` 是 `BooleanProperty`、默认 `false`；`getRenderShape` 返回 `MODEL`（`BaseEntityBlock` 默认是 `INVISIBLE`）；`getTicker` **只在服务端**返回；两条语言键 `block.farmstorm.cultivator` / `item.farmstorm.cultivator`（36 号 §4.3.2 #1~#2）。
- **实现要点**：方块实体**不实现 `Container`**（36 号 §2.5.2 的理由：漏斗 / 投掷器会绕过准入面）；方块实体先只建骨架，三个 NBT 键与状态机在 F-235；**本任务不跑 `runData`**（方块状态 / 模型 / 战利品表在 F-225）。
- **易漏点**：① 不进 `creativeTabItems()` 就**拿不到**——本版没有培养器配方（36 号 §5 的 29 条配方不变），创造栏是唯一入口（A-64）；② 千万别顺手注册菜单（培养器无界面，`menu_types` 的 +1 只给核心存储）；③ `outpost_core` 的 `registerItem` + `otherProperties()` 是唯一先例，照它写；④ 注册了方块却不进 `ModLootTableProvider.getKnownBlocks()` 的问题在 F-225 处理，本任务不要提前改那个文件。
- **验收断言**：
  1. 启动日志 `自检 注册对象 96 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → FAIL`——**FAIL 是窗口期的正确值**，不许把这一行写成 OK（§0.3）。
  2. `/give` 拿到 `farmstorm:cultivator`；`/setblock` 放得下；`/data get block <pos>` 能看到该方块实体。
  3. 创造栏「工具方块」段末尾多出一项培养器；`FarmstormItems.java` 里 `cultivator` 的命中 ≥ **2**（注册 + 创造栏）。
  4. 自检的其它行仍全绿（`自检 作物`、`自检 作物品质`、`自检 幸运浆果池`）。

#### F-225 · 培养器资源、双模型与 datagen 三处（L · 3.0 人日｜依赖 F-224）

- **目标**：材质 7 文件 + 双模型 + 两态 blockstate + item 模型 + 战利品表落地（36 号 §5 的 12 个文件），并让 datagen 不再索取不存在的 `item/cultivator.png`。
- **涉及文件**：`src/main/resources/assets/farmstorm/textures/block/` 新增 7 个文件（36 号 §5 表 #1~#7：3 张 PNG + 2 个 mcmeta + 2 张静态首帧）；`datagen/ModBlockStateProvider.java:69-85`（`registerStatesAndModels` 的分派，兜底分支在 `:80-84`）；`datagen/ModItemModelProvider.java:56-59`（`usesBlockModel` 与它头上的注释）；`datagen/ModLootTableProvider.java:124-131`（`add` 调用区）、`:189-216`（`getKnownBlocks` 的 26 条清单）、`:37`（类注释「26 张」）。
- **具体改动**：三面用 `cube_bottom_top`：空闲模型引用 `cultivator_a_top_still` / `cultivator_a_side_still` / `cultivator_a_bottom`，工作模型引用帧条两张 + `_bottom`（36 号 §2.5.5 的两张模型表）；`blockstates/cultivator.json` 两个 variants（`working=false` / `true`）；`usesBlockModel` 加 `CultivatorBlock`；blockstate 走 `partialState().with(WORKING, false/true)` 而不是 `simpleBlock`（36 号 §5 的连带改动 2）；战利品表掉自身。
- **实现要点**：`cultivator_a_top` / `_side` / `_bottom` 三个名字**不许改**（材质包 README 明确要求）；两张静态首帧从 `editable_frames/side_00.png` / `top_00.png` 拷入并命名 `*_still`（36 号 §2.5.5 的「本文确定」13）；两个 mcmeta 逐字节相同、`frametime 2`、`frames 0..15`（36 号 §8 #19）。
- **易漏点**：① **`usesBlockModel` 不加 `CultivatorBlock`** ⇒ `ModItemModelProvider` 落到 `basicItem`，去要一张不存在的 `textures/item/cultivator.png`，游戏里是紫黑格（36 号 §5、§10 #10）——这是本任务第 1 号坑，`usesBlockModel` 头上那句注释（「只认 ConverterBlock 与 OutpostCoreBlock」）也要一起改；② 兜底分支会给培养器生成 `cubeAll` + 一张不存在的 `block/cultivator.png`，所以分派必须加在兜底**之前**；③ `getKnownBlocks()` 是**手写 26 条**，只加 `add(...)` 不加进这里，战利品表不生成、方块挖掉什么都不掉；④ 两张 `_still` **不带** mcmeta。
- **验收断言**：
  1. `src/generated/resources/assets/farmstorm/blockstates/cultivator.json` 恰 **2** 个 variant（`working=false` / `working=true`），后者的模型是 `farmstorm:block/cultivator_working`。
  2. `models/block/cultivator.json` 与 `models/block/cultivator_working.json` 存在，父模型都是 `minecraft:block/cube_bottom_top`。
  3. `models/item/cultivator.json` 的 parent 是 `farmstorm:block/cultivator`（若出现 `item/generated` 或 `layer0`，就是 `usesBlockModel` 漏改）。
  4. `data/farmstorm/loot_table/blocks/cultivator.json` 存在，挖掉掉 **1** 个自身。
  5. 计数对账：`blockstates` 26 → **27**、`models` 103 → **106**、`loot_table` 26 → **27**、`*.png.mcmeta` 0 → **2**、`textures/block` 新增 **5** 张 PNG。
  6. 游戏内 F3 的 targeted block state 在 `working=false` 时显示空闲模型（静态帧）。

#### F-226 · 核心改容器：54 格 + `Items` + 两个同步方法（L · 3.0 人日｜依赖 —）

- **目标**：`OutpostCoreBlockEntity` 从「只存 owner」改为 `implements Container` 的 **54 格**容器，NBT 增 `Items`，补 `getUpdateTag` / `getUpdatePacket`，老档兼容读（D-166、RT-44）；同时新增 `outpost_core.storageSlots`（默认 54）。
- **涉及文件**：`blockentity/OutpostCoreBlockEntity.java:48`（类声明）、`:82-86`（字段区）、`:411-424`（`saveAdditional` / `loadAdditional`）；`config/FarmstormConfig.java:913-918` 区（`outpost_core` 段 +1 键）；`datagen/ModLangProvider.java`（+1 条 config 键名）。
- **具体改动**：54 格 `NonNullList` + `Container` 的七个方法（36 号 §2.6.1 的逐方法形态）；NBT 追加 `Items`（`ContainerHelper` 的 `TAG_ITEMS`）；`getUpdateTag` **只写 owner 三键**、不写 `Items`、不写绑定（36 号 §2.6.1 的体积方案）；`getUpdatePacket` 用 `ClientboundBlockEntityDataPacket.create(this)`；新增 `canAccept` / `acceptAll` 两个**整批**插入方法（先算后写，36 号 §2.6.1 的三条纪律）；`storageSlots` 默认 **54**、值域 **54–54**（36 号 §4.1.1 #3）。
- **实现要点**：老档缺 `Items` ⇒ `loadAllItems` 读到空 list ⇒ **54 个空槽**，不报错、不清档（36 号 §2.6.2）；`acceptAll` 成功后必须 `setChanged()` 并在同一 tick `sendBlockUpdated`；`storageSlots` 的值域若被 `ModConfigSpec` 拒绝，走 36 号 §9 ⑦ 的退路（`54..55` + 创建菜单时钳到 54），**格数不许变成别的值**。
- **易漏点**：① **54 是格子数、16 是培养器名额**（裁定 #8），写反一个整段验收就废；② 老档里多出来的旧键**不读、不删**、原样留在 `CompoundTag` 里（36 号 §2.6.2 第 4 行）；③ 本任务**不加菜单注册**（`menu_types` 在 F-227 才 +1，否则 F-224 的窗口状态 A 就对不上）；④ 别把 `Items` 塞进 `getUpdateTag`（每次区块下发多几 KB，RT-44 的正面回答就是「不塞」）。
- **验收断言**：
  1. `/data get block <core>` 出现 `Items` 键（0 条物品时为空表）。
  2. 老档（0.1.1 存的核心）升级后：核心照常加载、`/data get block` 读成 54 个空槽、无异常栈（界面打开在 F-229 才验，36 号 §8 #24 的前置在此）。
  3. 删旧 toml 起服：`[outpost_core]` 段 **3 键**（`maxPerPlayer` / `boundaryParticleIntervalTicks` / `storageSlots`）、全文 **172 键 / 59 段**（toml 区间读数，不是源码总账）。
  4. `saveAdditional` / `loadAdditional` 之外没有第三处写 `Items`；`getUpdateTag` 的返回体里只有 owner 三键（人工核对 1 次）。

#### F-227 · 核心菜单 + 屏幕 + `menu_types` +1（L · 3.0 人日｜依赖 F-226）

- **目标**：`outpost_core` 的 54 格菜单与屏幕落地，`menu_types` 1 → 2（D-166、D-171）；**这是窗口内最后一次注册增量**（§0.3 的状态 B）。
- **涉及文件**：新建 `menu/OutpostCoreMenu.java`、新建 `client/OutpostCoreScreen.java`；`registry/FarmstormMenus.java:16-17`（+1 项）；`client/FarmstormClient.java:52-54`（+1 行屏幕注册）；`datagen/ModLangProvider.java`（+1 条 `container.farmstorm.outpost_core`）。
- **具体改动**：54 槽（6 行 × 9 列，x = 8 + 18·col、y = 18 + 18·row）+ 玩家背包 3 行（y = 140）+ 快捷栏（y = 198），`imageWidth = 176` / `imageHeight = 222`，复用原版 `generic_54.png`（36 号 §2.6.3 的「本文确定」12）；`quickMoveStack` 两向（0–53 ⇄ 54–89）；客户端兜底 `SimpleContainer(54)`；标题键 `container.farmstorm.outpost_core`。
- **实现要点**：屏幕的 `renderBg` 用**六参** `blit`（五参形式会按 256×256 算 UV、整体错位，36 号 §2.6.3）；菜单槽数只在创建时读一次 `storageSlots`；`stillValid` 用原版语义（F-229 落地时接上）。
- **易漏点**：① 忘了 `FarmstormClient.java:52-54` 的屏幕注册 ⇒ 开界面时崩或黑屏；② `quickMoveStack` 的 54 + 36 槽区间是全项目第一次写，照原版 `ChestMenu` 的两段（36 号 §9 ⑧）；③ `menu_types` 一旦 +1，自检行是 **97 + FAIL**（断言常量还是 93），**本任务不许写 OK**（§0.3）。
- **验收断言**：
  1. 启动日志 `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / … / menu_types 2 → FAIL`（窗口期正确值）。
  2. `FarmstormMenus.REGISTER.register` 命中 **2** 处；`FarmstormClient.java:52-54` 区有 2 行 `event.register(...Screen::new)`。
  3. 新增 `container.farmstorm.outpost_core` 1 条语言键，两份 lang 各一条。
  4. 此时游戏内**还开不了**界面（打开动作在 F-229）——这是预期的中间态，不是缺陷。

#### F-228 · 注册自检 97 收口（关窗）（S · 0.5 人日｜依赖 F-224、F-227）

- **目标**：把 `Farmstorm.java:147-150` 的 **5 个常量一次改齐**（`93 → 97`、`items 32 → 33`、`blocks 26 → 27`、`blockEntities 7 → 8`、`menuTypes 1 → 2`），跑一次起服验日志 `→ OK`，关闭 §0.3 的窗口。
- **涉及文件**：`Farmstorm.java:147-150`（5 个常量）；`:126-131`（方法注释里的「64 → 77 → 90 → **93**」与「相加不等于 **93**」两处）；`:151-155`（13 参数的格式串，**一个字都不改**）。
- **具体改动**：只改 5 个数值常量与那两处注释里的 93；其余 8 类的常量（`entityTypes 13` / `mobEffects 4` / `soundEvents 3` / `recipeTypes 1` / `recipeSerializers 1` / `creativeTabs 1` / `damageTypes 3` / `dataComponents 1`）**一个都不动**（36 号 §1.2 的旧 → 新表）。
- **实现要点**：13 个占位符的参数顺序不变（改值不改串，36 号 §9 ⑬）；改完必须**真起一次服**读日志，不能只看代码。
- **易漏点**：① 只改 `total` 不改四个分项 ⇒ 仍是 FAIL；② 漏掉方法注释里的两处 93（注释会撒谎）；③ 顺手把 `damageTypeCount()` 的 3 个 `ResourceKey` 加一项（本版没有新伤害类型，36 号 §1.2 末段明确「不加项」）；④ 本任务**不夹带**任何别的改动（`git diff` 只有这一段）。
- **验收断言**：
  1. `run/logs/latest.log` 出现 `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`（36 号 §8 #1）。
  2. `93` 在 `Farmstorm.java` 里的命中 = **0**；`97` ≥ **1**。
  3. `git diff` 只动 5 个常量与两处注释，格式串未被改动。

### Phase 36 · 核心交互、绑定与迁移（F-229 ~ F-233）

阶段目标：让核心成为「能开的箱子 + 能绑 16 个培养器的锚点 + 拆掉时会把培养器交出去」的完整载体。出口判据：己方在原版触及范围内右键开 54 格界面、挖掉整箱掉落、一个核绑 16 个培养器、拆核时培养器改绑或原地失效（**不变泥土**）。

#### F-229 · 核心存储打开：两道闸（M · 1.5 人日｜依赖 F-227）

- **目标**：右键核心打开 54 格存储，两道闸 = **己方** + **原版容器的触及范围**（D-166 补充、裁定 #17）；非己方 `PASS`。
- **涉及文件**：`block/OutpostCoreBlock.java`（在 `:101-107` 的 `onRemove` 之前新增 `useWithoutItem` 与 `getMenuProvider`）；`menu/OutpostCoreMenu.java`（F-227 建，本任务补 `stillValid`）。
- **具体改动**：服务端先判己方（`isAlly`，D-138 的既有判定），非己方 `return PASS`；通过则 `player.openMenu(state.getMenuProvider(...))` → `CONSUME`；客户端 `return SUCCESS`（HC-03 的既有取舍，照 `ConverterBlock.java:81-83`）；菜单的 `stillValid` 用 `Container.stillValidBlockEntity`（原版默认距离 4.0）。
- **实现要点**：**不新设距离常量、不新增 config 键**（裁定 #17）；3×3 区块是绑定 / 连线的范围，**不是**开箱条件（36 号 §2.6.4 的读法 2、35 号 §8.3 的 ⚠️ 已过期，36 号 §10 #13）；实际手感约 8.5 格内可一直开着界面，走远由原版关掉。
- **易漏点**：① 非己方必须 `PASS` 不能 `SUCCESS`（`SUCCESS` 会吞右键）；② 手持方块右键会先走放置、不开界面（36 号 §9 ⑨），验收一律写「空手」；③ 别在 `useWithoutItem` 里自己判距离（那会与原版 `stillValid` 双重判定，且多一个魔数）。
- **验收断言**：
  1. 己方空手右键核心：打开 **54 格**界面（6 行 × 9 列 + 玩家背包），标题「据点核心」；存入 3 个物品后关掉再开，物品仍在（36 号 §8 #24）。
  2. 非己方右键：**不开界面**（36 号 §8 #25 的①）。
  3. 己方在离核 **9 格**处右键：客户端发不出交互、界面不开；在 **8 格**内打开后走到 9 格：界面被原版 `stillValid` 关掉（36 号 §8 #25 的②）。

#### F-230 · 核心破坏掉落（S · 0.5 人日｜依赖 F-226）

- **目标**：核心被**真移除**时 54 格全掉；活塞推动 / 同方块替换 / 区块卸载都不掉（D-167）。
- **涉及文件**：`block/OutpostCoreBlock.java:101-107`（`onRemove` 里追加掉落分支）。
- **具体改动**：保留现有的 `core.release()`，追加四条件掉落：非同一方块 + 非活塞推动 + 该格 BE 是 `Container` ⇒ `Containers.dropContents`（照 `ConverterBlock.java:88-96`）。
- **实现要点**：掉落与「索引摘除 + 培养器迁移」是**两件事、两个钩子**（`onRemove` vs `setRemoved` / `settleOnRemoval`，D-167 补充）；`getStateForPlacement` / `setPlacedBy` / `getTicker` 不动。
- **易漏点**：① 漏 `!movedByPiston` ⇒ 活塞推动时原地刷一份物品（复制漏洞）；② 别把 54 格掉落写进 `settleOnRemoval`（F-233 只管绑定与索引）；③ 核心抗爆 1200，默认不会被炸掉，**不另立爆炸分支**（35 号 §8.4 附注①）。
- **验收断言**：
  1. 核里放 3 组不同物品后挖掉：地上出现这 3 组（36 号 §8 #26）。
  2. 活塞推动：**0** 掉落；同方块替换（`/setblock` 成同一个方块）：**0** 掉落；区块卸载（走远再回来）：物品仍在核里。
  3. 空核被挖掉：只掉方块本体 1 个（35 号 §12 B-27）。

#### F-231 · 核侧 16 条绑定列表 + `cultivatorCapacity`（M · 1.5 人日｜依赖 F-226）

- **目标**：核侧 16 条绑定列表成为**唯一真相**（容量载体 + 检索面），并新增 `outpost_core.cultivatorCapacity`（默认 16）（D-165、裁定 #8）。
- **涉及文件**：`blockentity/OutpostCoreBlockEntity.java`（字段区 `:82-86` 旁 + `:411-424` 的 NBT 增 `Cultivators` + 以下 5 个方法 `cultivatorCount` / `hasCultivator` / `addCultivator` / `removeCultivator` / `cultivators`）；`config/FarmstormConfig.java`（`outpost_core` 段 `:912` 之后 +1 键）；`datagen/ModLangProvider.java`（+1 条）。
- **具体改动**：`List<BlockPos>` 落盘为 `Cultivators`（Long 列表，≤ 16 条）；老档缺键 ⇒ 空列表（老核绑的是耕地，不是培养器，36 号 §2.6.2）；`cultivators()` 在**选核 / 放置**这两条低频路径上做一次 `getChunkNow` 惰性清理（36 号 §2.7 末段）；`cultivatorCapacity` 默认 **16**、值域 **1–64**（36 号 §4.1.1 #2）。
- **实现要点**：绑定的唯一真相在核侧，培养器 BE 里的 `Core` 只是**引用缓存**，使用前用核侧列表校验（D-165）；列表满 16 时第 17 个不挤掉已绑的（35 号 §8.2 的算例 2）；运行期每刻**不做** `getChunkNow` 校验（36 号 §2.7 明确否决：每刻 256 次区块查询）。
- **易漏点**：① 把 16 写进容器格数（54 才是格子数，裁定 #8）；② 把绑定列表塞进 `getUpdateTag`（36 号 §2.6.1 否决）；③ `Cultivators` 的键名大小写要与 36 号 §2.6.1 一致（`Items` / `Cultivators` 都是首字母大写）。
- **验收断言**：
  1. `/data get block <core>` 出现 `Cultivators`（空时为缺省或空表）；绑定 1 个培养器后是 **1** 条 long。
  2. 删旧 toml 起服：`[outpost_core]` 段 **4 键**（`cultivatorCapacity` / `storageSlots` / `maxPerPlayer` / `boundaryParticleIntervalTicks`）、全文 **173 键 / 59 段**（toml 区间读数，不是源码总账）。
  3. 改 `cultivatorCapacity = 2` 后（F-232 落地之后）第 3 个培养器放不放得上：**放得下但未绑定**（35 号 §8.2 算例 2）。

#### F-232 · 放置时绑定：四判据 + 两侧写入（L · 3.0 人日｜依赖 F-224、F-231）

- **目标**：培养器放下时自动绑「覆盖它 + 己方 + 有空位」中**最近**的一个；距离取 `BlockPos#distSqr`，平手按 `placedGameTime` 升序再按核坐标字典序；核侧与培养器侧**都写**（D-165）。
- **涉及文件**：`block/CultivatorBlock.java`（`setPlacedBy`）；`blockentity/CultivatorBlockEntity.java`（`Core` 缓存字段落在这里，NBT 键 `Core` 先建好，状态机在 F-235）；`blockentity/OutpostCoreBlockEntity.java`（`addCultivator`）；`outpost/OutpostCoreIndex.java`（候选核查询，口径同 0.1.1）。
- **具体改动**：候选集 = 覆盖（复用 `inRange`）+ 己方 + 列表未满；比较键照 D-165 的两级；写入 = 核侧列表 + 培养器侧 `Core`；写完后 `setChanged()` + `sendBlockUpdated`（36 号 §2.6.1 的同步时点）。
- **实现要点**：**不做运行期漂移 / 定期重绑**，写入只有「放置时」与「核被拆时」两个时点（D-165 补充、35 号 §12 B-13）；没有可绑的核时方块照样放下、处于未绑定态（35 号 §12 B-12）；跨维度不成立（候选集来自同维度的核）。
- **易漏点**：① 用切比雪夫或曼哈顿距离代替 `distSqr`（D-165 原文是三维欧氏平方）；② 平手漏掉第二级键 ⇒ 同一份存档两次运行给出不同答案（35 号 §8.2 算例 4）；③ 为「找核」加载区块（只查已加载，`getChunkNow`）；④ 两侧不一致时按**未绑定**处理（D-165），不要以培养器缓存为准。
- **验收断言**：
  1. 两个核都覆盖、A 已满 16、B 有空位：绑到 **B**（两侧 `/data get block` 一致）。
  2. 同一刻放两个核、距离相同：绑 `placedGameTime` 更小的那个；同一份存档重跑结果相同。
  3. 附近没有己方核：放下后培养器**缺 `Core` 键**（未绑定），右键无反应（35 号 §12 B-12）。
  4. 事后在它旁边放一个核：`Core` 键**仍缺失**（不自动认领，35 号 §12 B-13）。

#### F-233 · 迁移三出口：`settleOnRemoval` 整段改写（L · 3.0 人日｜依赖 F-232）

- **目标**：核被拆时把 16 个培养器逐个改绑；找不到接替核的**留在原地、内容保留、标记未绑定**（D-168、RT-43）；**没有「变泥土」这一支**；这也是 `.dat` 最后一个运行期读者消失的时点（A-69）。
- **涉及文件**：`blockentity/OutpostCoreBlockEntity.java:341-394`（整段改写，含 `:353` 的 `FarmlandLedger.of`、`:363` 的 `FarmlandGuard.selectCore`、`:387` 的 `setBlock(DIRT)`）；`blockentity/CultivatorBlockEntity.java`（接收改绑）。
- **具体改动**：照 36 号 §2.7 的七步——真移除判定（`getChunkNow` + 该格已不是核心方块）→ 复制一份绑定列表再遍历 → 逐条选「覆盖 + 己方 + 有空位」的其它核 → 三出口（改绑 / 清 `Core` 标记未绑定 / 防御性跳过）→ **只清 `cultivators`、不清 `items`** → `release()` 收尾；`setBlock` 在方法体内归 0。
- **实现要点**：不为迁移加载区块（全程 `getChunkNow`）；物品由 `onRemove` 负责（F-230），两条钩子分工；出口③ 保留 NBT 原值、不猜测、不删绑定（与 34 号 D-168 补充的 A-60 同向）；`release()` 仍在最后一行。
- **易漏点**：① 迁移把槽清空（35 号 §12 B-19 的反面）；② 迁不动时写「变泥土」或「打掉培养器掉物品」（RT-43、34 号 §7 第 1 条的同一逻辑）；③ 边遍历边改列表（36 号 §2.7 步骤 4 要求先复制）；④ 把 54 格掉落也搬进来（那是 F-230 的钩子）。
- **验收断言**：
  1. 拆核 + 有接替核：培养器 `Core` 指向新核，`Held` / `Progress` 不变（36 号 §8 #27）。
  2. 拆核 + 无接替核：培养器**留在原地**、`Core` 键消失、`Held` / `Progress` 保留、右键无反应（36 号 §8 #28）。
  3. 培养器所在区块不在内存：NBT **一个字节不变**（`Core` 仍是旧核坐标，36 号 §8 #29）。
  4. `setBlock` 在 `settleOnRemoval` 方法体内命中 **0**（36 号 §8 #30）；`FarmlandLedger` / `FarmlandGuard` 在本类的引用（含 import）归 **0**。

### Phase 37 · 培养器行为（F-234 ~ F-237）

阶段目标：让培养器从「一个能放下的方块」变成「按档准入、600 刻产 2 + 2、核满暂停、拆掉吐回」的完整机器。出口判据：普通 / 稀有可放入、史诗拒绝、600 刻产出进核、核满暂停且不丢、破坏掉内部作物。

#### F-234 · `cultivator` 组 2 键 + 2 条语言键（S · 0.5 人日｜依赖 F-224）

- **目标**：新增顶层组 `cultivator`，2 键 `cycleTicks`（600）/ `yield`（2）（D-163）+ 2 条 config 语言键（36 号 §4.1.1 #4~#5、§4.3.2 #8~#9）。
- **涉及文件**：`config/FarmstormConfig.java`（字段区 + 静态块，`outpost_core` 段之后新增 `cultivator` 段）；`datagen/ModLangProvider.java` 的 config 段（+2 条）。
- **具体改动**：`cultivator.cycleTicks` 默认 **600**、值域 **20–72000**；`cultivator.yield` 默认 **2**、值域 **1–64**（36 号 §4.1.1 的五条值域理由）；两条键名 `farmstorm.configuration.cultivator.cycleTicks` / `.yield`。
- **实现要点**：产出里的「2 个种子」这一半**不另设 config 键**（跟着产出规则走，D-163、35 号 §6.3 纪律 2）；`600 = 30 × 20`（30 秒 × 20 刻/秒）。
- **易漏点**：① 把「2 个种子」也做成键 ⇒ 键数变 176；② `cycleTicks` 的下界写成 1（下界是 20，36 号 §4.1.1）；③ 组必须是顶层组（嵌套会让组数变成 61）。
- **验收断言**：
  1. 删旧 toml 起服：`[cultivator]` 段恰 **2 键**，值 **600 / 2**。
  2. 全文 **175 键 / 60 段**（`^\s*\[` 计数；toml 区间读数，不是源码总账）——这是本版 config 的终值，后面的任务不许再增删键。
  3. 两份 lang 各新增 2 条 `farmstorm.configuration.cultivator.*`。
  4. 改 `cycleTicks = 20` 后（F-236 落地之后）20 刻即产出一次。

#### F-235 · 状态机、四道准入闸与放入（L · 3.0 人日｜依赖 F-232、F-234）

- **目标**：培养器的三个 NBT 键（`Held` / `Progress` / `Core`）、服务端每刻状态机、四道准入闸与放入动作（D-160、D-162、D-164）。
- **涉及文件**：`blockentity/CultivatorBlockEntity.java`（三个字段、`serverTick`、`isUsable`、`setWorking`）；`block/CultivatorBlock.java`（`getTicker` 的服务端分支、`useWithoutItem`）。
- **具体改动**：状态机四步（空槽 → 不可用 → 计时 → 完成 / 暂停，36 号 §2.5.2）；准入四闸合并成一次判定（绑定 + 范围 + 手持档 + 空槽，36 号 §2.5.3）；`useWithoutItem` 的顺序：客户端 `SUCCESS` → 任一闸不过 `PASS` → 通过则 `split(1)` + `CONSUME`；`working` 只在值真的变化时写 blockstate（`UPDATE_CLIENTS`）。
- **实现要点**：史诗 **4 个作物 + 2 个史诗种子**一律拒绝（D-162），准入**按 `tier()` 判、不按 id 白名单**；槽已占用时右键无反应（不叠加、不替换）；空手右键**不取出**（35 号 §12 B-11）；`isUsable` 只查核侧列表命中 + `inRange`，每刻**不查区块**（36 号 §2.7 末段）。
- **易漏点**：① 按 id 白名单判准入（`tier` 一改就漂，D-162 明确否决）；② 每刻写一次 blockstate（36 号 §2.5.2 要求只在变化时写）；③ `working` 只做 BE 同步、不走 blockstate（D-161 明确要求走属性）；④ 未绑定的培养器仍要能放下、能看见，只是不可用（D-165）。
- **验收断言**：
  1. 放普通作物：`Held` 有值、`Progress` 从 0 开始涨、F3 的 `working=true`；核满之外的正常路径在 F-236 验产出。
  2. 放史诗作物 / 2 个史诗种子：**无反应**、手持物不减（36 号 §8 #23）。
  3. 槽已占用再右键：无反应；未绑定的培养器右键：无反应。
  4. 区块卸载 / 停服重载：`Progress` 从原值继续、不重新计时（35 号 §12 B-17）。

#### F-236 · 产出与核满暂停（L · 3.0 人日｜依赖 F-226、F-235）

- **目标**：600 刻结算一次，产出 **2 个作物**（该档案有独立种子物品时再 **+2 个种子**），整批投递进绑定核；核放不下就**暂停**，腾出空间后从暂停处继续（D-163、D-164）。
- **涉及文件**：`blockentity/CultivatorBlockEntity.java`（完成分支与批次组装）；`blockentity/OutpostCoreBlockEntity.java`（`canAccept` / `acceptAll`，F-226 已建）。
- **具体改动**：产出批次由档案决定（有独立种子物品 ⇒ `[作物 × yield, 种子 × yield]`，否则 `[作物 × yield]`）；`acceptAll` 成功 ⇒ 清槽 + `Progress = 0` + `working=false`；失败 ⇒ `Progress` 停在 `cycleTicks`、`working=false`、**不落地、不丢**（36 号 §2.5.2 第 4 步）。
- **实现要点**：「有独立种子物品」的判据是 `seedItem() != null` **且**它不等于 `id()`（36 号 §2.5.4 的布尔式）——8 条「种子 = 自身」的档案因此归到「无独立种子」；准入面内 **5 条给 2+2、13 条只给 2**（`18 = 5 + 13`，35 号 §6.4）；投入的是种子时产出仍由**父作物档案**决定（`chive_seeds` 与 `friend_chives` 结果相同）；「放得下」按**整批**判（D-164 补充）。
- **易漏点**：① 用 `seedItem != null` 单条件判「有种子」⇒ 8 条种子 = 自身的档案产出翻倍（36 号 §2.5.4 明确否决）；② 核满时重新计时（D-164 要求从暂停处继续，`Progress` 不清零即天然成立）；③ 产出落地成 `ItemEntity` 或塞进玩家背包（plan 原话是进核，D-164 明确否决）；④ 逐件判「放得下」（要求整批判）。
- **验收断言**：
  1. 喂 `puffed_wheat`：600 刻后核里 **+2** 作物 + **+2** 种子（36 号 §8 #21）。
  2. 喂 `ubi_server`（种子 = 自身）：只 **+2** 作物，没有种子。
  3. 喂 `chive_seeds`：与喂 `friend_chives` 产出**完全相同**（2 作物 + 2 种子）。
  4. 核塞满：`Progress == cycleTicks`、物品不落地、不丢；清出 2 格后自动完成投递（36 号 §8 #22）。

#### F-237 · 培养器破坏掉落（S · 0.5 人日｜依赖 F-224）

- **目标**：培养器被破坏时掉**自身 + 内部作物**；活塞推动不重复掉（D-160 补充）。
- **涉及文件**：`block/CultivatorBlock.java`（`onRemove`）；`datagen/ModLootTableProvider.java`（战利品表已在 F-225 建，掉自身）。
- **具体改动**：四条件（非同一方块 + 非活塞推动）后，用 `Containers.dropContents(Level, BlockPos, NonNullList)` 重载掉内部那一个培养物（36 号 §2.5.6：方块实体**不是** `Container`，所以用 `NonNullList` 重载）。
- **实现要点**：方块本体由战利品表掉，两处不重叠；活塞推动时内容物随方块实体走、不落地复制。
- **易漏点**：① 用 `Container` 重载（方块实体没实现 `Container`，编译不过）；② 漏 `!movedByPiston`（复制）；③ 把「掉自身」也写进 `onRemove`（与战利品表重复，掉 2 个）。
- **验收断言**：
  1. 槽里有作物时挖掉：掉 **1** 个方块 + **1** 个该作物（35 号 §12 B-10）。
  2. 空槽挖掉：只掉 **1** 个方块。
  3. 活塞推动：**0** 掉落。

### Phase 38 · 连线（F-238 ~ F-240）

阶段目标：新增第 2 个 S2C 只读包与客户端线框，把「核 ↔ 培养器」的绑定关系画出来。出口判据：站在自家核旁能看到线、走远线消失、拆方块线立刻消失、客户端连续在线不崩、服务端逻辑不依赖连线。

#### F-238 · 连线包 + 注册 + 协议版本 2（M · 1.5 人日｜依赖 —）

- **目标**：新增第 2 个自定义网络包 `farmstorm:cultivator_link`（S2C 只读），协议版本串 `"1"` → `"2"`（D-169、D-172）。
- **涉及文件**：新建 `network/CultivatorLinkPayload.java`；`network/FarmstormNetwork.java:17`（`PROTOCOL_VERSION`）、`:22-31`（第二条 `playToClient`）、`:35-36`（自检日志行）。
- **具体改动**：`Record Link(BlockPos core, BlockPos cultivator)` + `MAX_LINKS = 1024`；`StreamCodec` 照 36 号 §2.8.1（两个 `BlockPos.STREAM_CODEC` 组合 + `ByteBufCodecs.list(MAX_LINKS)`）；**不带 `total`、不带维度**；`.optional().playToClient(...)`；自检行补一行（或改成两行），把两个包 id 与版本串都打出来。
- **实现要点**：协议串必须从 `"1"` 变 `"2"`（codec 一变必须跟着变，36 号 §2.8.1 与 `FarmstormNetwork.java:16` 的注释）；`MAX_LINKS = 1024 = 64 × 16`（`maxPerPlayer` 上界 × `cultivatorCapacity` 上界）；`core_list` 的既有注册与 handler 一字不动（不扩它，D-169 补充）。
- **易漏点**：① 忘改协议串 ⇒ 装了新模组的客户端连旧服 / 反之握手不匹配；② 自检行仍是 1 行（36 号 §8 #2 的判据是**两行**）；③ 把连线塞进 `core_list` 的 codec（D-169 明确否决：全量快照与范围增量是两类数据）。
- **验收断言**：
  1. `latest.log` 出现 **2** 行 `自检 网络包注册`，id 分别是 `farmstorm:core_list` 与 `farmstorm:cultivator_link`，版本串都是 **2**。
  2. `playToClient` 在 `FarmstormNetwork.java` 里命中 **2** 处。
  3. 纯原版客户端能进服（optional 通道）；专用服务器起服无 `NoClassDefFoundError`（36 号 §8 #2）。
  4. `@Mixin` 全仓命中仍 **0**。

#### F-239 · 连线广播器：范围与五个时机（L · 3.0 人日｜依赖 F-233、F-238）

- **目标**：把范围内的绑定关系逐玩家下发，出范围发空列表清残留；去抖 10 刻、重扫 20 刻（D-169）。
- **涉及文件**：新建 `network/CultivatorLinkBroadcaster.java`；`event/FarmstormGameEvents.java:103`、`:120`、`:128`（登录 / 换维度 / 重生的既有请求点）、`:114`（登出清记录）；`blockentity/OutpostCoreBlockEntity.java`（绑定变化与 `release()` 处补一次请求）。
- **具体改动**：结构照 `CoreListBroadcaster`（去抖口径、`hasChannel` 收件人判定、`requestAll`）；范围判据复用 `OutpostCoreBlockEntity.inRange(corePos, playerBlockPos)`；五个时机照 36 号 §2.8.2 的表（进范围 / 出范围 / 绑定变化 / 核被拆 / 登录换维度重生登出）。
- **实现要点**：**不写第二份范围公式**（36 号 §2.8.2 明确要求复用 `inRange`，`RANGE_CHUNKS = 1`）；出范围发**空列表**而不是不发；`MAX_LINKS` 只是解码上限，不截断。
- **易漏点**：① 出范围不发空包 ⇒ 客户端的线一直挂着（35 号 §12 B-38）；② 核被拆时漏一次 `requestAll`（`release()` 末尾已有一次，连线广播器要在同处补一次）；③ 自己重算「玩家在不在 3×3 区块内」（会与 `inRange` 漂移）。
- **验收断言**：
  1. 站在自家核旁（3×3 区块内）：收到含 N 条 link 的包，N = 该核已绑定的培养器数（36 号 §8 #31）。
  2. 走出范围：收到空列表，客户端线消失（36 号 §8 #31 的后半）。
  3. 拆掉一个培养器：对应的线**立刻**消失，不等 20 刻（36 号 §8 #32）。
  4. 同一刻的连锁事件（放置 + 绑定变化）只发 **1** 包（去抖 10 刻）。

#### F-240 · 客户端只读与线框渲染（L · 3.0 人日｜依赖 F-238）

- **目标**：客户端保存最近一次收到的 link 列表并**只读**渲染；世界空间画线框（D-169、RT-45）。
- **涉及文件**：新建 `client/ClientCultivatorLinks.java`、新建 `client/CultivatorLinkRenderer.java`；`client/FarmstormClient.java`（渲染事件与 handler 的挂载处）。
- **具体改动**：handler 只做 `enqueueWork`（照 `ClientCoreList`），换服 / 断线清空；渲染挂 `RenderLevelStageEvent` 的 `Stage.AFTER_PARTICLES`；**新建**一个 `PoseStack` 并 `translate(-cam.x, -cam.y, -cam.z)`；用 `LevelRenderer.renderLineBox` + `RenderType.lines()`；颜色 RGB `0.30 / 0.85 / 0.90`、alpha `1.0`、线宽 1.0；两端各画一个 0.25 格的端点框（36 号 §2.8.3 的「本文确定」9）。
- **实现要点**：客户端**只读**三条（收到什么画什么、不跨维度缓存、空列表真的清空，D-169 补充）；线宽与 `RenderType` 不自建（36 号 §2.8.3）；颜色沿用核心边界粒子的同一组数值（`OutpostCoreBlockEntity.java:63`），不发明新色。
- **易漏点**：① 手写顶点漏 `NORMAL` 元素 ⇒ `Missing elements in vertex` 客户端崩（36 号 §7 RT-45）；② 改事件给的 `getPoseStack()` ⇒ 把后续阶段一起平移；③ 换维度后不清缓存（要 `clear()`）；④ 在服务端侧调用渲染类（专用服务器 `NoClassDefFoundError`）。
- **验收断言**：
  1. 站在自家核旁：核与每个已绑定培养器之间有**可见线框**，两端有 0.25 格的小框（36 号 §8 #31）。
  2. 走远：线消失；换维度：残留清空（35 号 §12 B-38）。
  3. 客户端连续在线 10 分钟：`latest.log` 里 **0** 条 `Missing elements in vertex` / `IllegalStateException`（36 号 §8 #33）。
  4. 把 handler 临时改成空实现后，绑定 / 产出 / 迁移 / 拆核掉落**全部照常**（36 号 §8 #34，服务端不依赖连线）。

### Phase 39 · 收口与全量验收（F-241 ~ F-246）

阶段目标：把三个总数（337 / 175 / 97）对平，跑完 36 条验收与 40 条边界，签红线与上游一致性。出口判据：两份 lang 各 337 键且键集相同、`farmstorm-server.toml` 175 键 / 60 段、注册面 97、`@Mixin` 0、2 个网络包、36 条与 40 条全部通过、上游四份文档 `git diff` 为空。

#### F-241 · 语言键总账 337 与类注释（M · 1.5 人日｜依赖 F-215、F-221、F-222、F-224、F-226、F-227、F-231、F-234）

- **目标**：两份语言文件各 **337** 键且键集完全相同，`ModLangProvider` 的条目构成注释同步（36 号 §4.3.3）。
- **涉及文件**：`datagen/ModLangProvider.java:13-16`（类注释的条目构成段）；`src/generated/resources/assets/farmstorm/lang/zh_cn.json`、`en_us.json`（重跑 `runData`）。
- **具体改动**：重跑 datagen；类注释改成 `33 item + 27 block + 4 effect + 3 subtitles + 178 config（= 175 server + 3 client）+ 36 advancement + 6 death + 20 tooltip + 13 entity + 9 gui + 2 container + 1 itemGroup + 0 message + 2 key + 3 command = 337`（36 号 §4.3.3 的逐前缀表）。
- **实现要点**：`178 = 169 − 1 + 10`（删 1 条耕地 config 键、增 10 条）；`tooltip` 从 16 → 20（+4 品质相关）；`container` 从 1 → 2；`message` 从 2 → 0。中英同键由构造方式保证（同一张 `ENTRIES` 表两列写盘），zh-only / en-only 必然为 0。
- **易漏点**：① 注释里的 169 → 178 最容易漏（config 含 3 条 CLIENT）；② `message.farmstorm` 归 0 但它的前缀行不能凭空消失（按前缀列举的注释要写 `+ 0 message`）；③ 只改 json 不改注释，下一个人会照着旧注释再算一遍；④ `ModLangProvider.java:10` 的「299 键（v0.0.3 口径）」是沿革句，不改。
- **验收断言**：
  1. 两份 json 各 **337** 键，键集完全相同（`Compare-Object` 结果为空）（36 号 §8 的口径）。
  2. 逐前缀：`farmstorm.configuration` **178** / `item` **33** / `block` **27** / `container` **2** / `tooltip` **20** / `message` **0**，其余 9 个桶不变。
  3. `323` 在 `ModLangProvider.java` 的命中 = **0**；`337` ≥ **1**。

#### F-242 · config 总账 175 键 / 60 组（M · 1.5 人日｜依赖 F-220、F-222、F-226、F-231、F-234）

- **目标**：`farmstorm-server.toml` 重生成后 **175 键 / 60 组**，`[outpost_core]` 4 键且**没有** `farmlandBonus`，`[cultivator]` 2 键、`[trade]` 6 键（36 号 §4.1）。
- **涉及文件**：`run/config/farmstorm-server.toml`（删旧重生成）；`config/FarmstormConfig.java`（逐组核对，不再改值）。
- **具体改动**：删掉旧的 server toml、起一次服让它重生成，然后逐组对 36 号 §4.1 的 36 个组表：`outpost_core 4`、`cultivator 2`、`trade 6`，其余 33 组键数与 0.1.1 相同。
- **实现要点**：`166 − 1 + 2 + 2 + 6 = 175`；`58 + 2 = 60`（新增 `cultivator`、`trade` 两个顶层组，`outpost_core` 段仍在、不是删组重建）。
- **易漏点**：① **不删旧 toml** 时 `farmlandBonus` 会作为孤立键留在文件里（NeoForge 不主动删未知键），36 号 §8 #11 的判据只在**删旧重生成**的口径下成立；② 组数按 `^\s*\[` 数（不是 `^\[`，A-52 的同一坑）；③ CLIENT 的 3 键**不计入** 175，别加进来；④ 别把 `outpost_core` 的 4 键数成 3（`maxPerPlayer` / `boundaryParticleIntervalTicks` / `cultivatorCapacity` / `storageSlots`）。
- **验收断言**：
  1. `farmstorm-server.toml` **175 键 / 60 段**，`[outpost_core]` **4 键**且无 `farmlandBonus`（36 号 §8 #11）。
  2. `[cultivator]` **2 键**（600 / 2）、`[trade]` **6 键**（2 / 5 / 10 / 32 / 40 / 64）。
  3. `farmstorm-client.toml` 仍 **3 键**（`hud.enabled` / `hud.x` / `hud.y`）。
  4. `FarmstormConfig.java` 的 `b.push(` 次数 = **60**。

#### F-243 · 验证工具同步 97 / 175 / 337（S · 0.5 人日｜依赖 F-228、F-241、F-242）

- **目标**：`tools/verify_project.py` 的期望面同步到 v0.2.0，否则它会**静默少核 4 个注册对象**（A-66）。
- **涉及文件**：`tools/verify_project.py:258-272`（`EXPECTED_COUNTS`）、它的 12 类 `EXPECTED` 清单（`:190-256`）、`:139-183`（`expected_config_keys_v011` 之后）、`:283-296`（五份旧文档的并集）、`:362-374`（`total / 93` 的打印）。
- **具体改动**：`EXPECTED` 的 4 个类各加新 id（`cultivator` ×3、`outpost_core` 菜单）；`EXPECTED_COUNTS` 改 5 个数（97 / 33 / 27 / 8 / 2）；`REMOVED_KEYS` 加 `outpost_core.farmlandBonus`；期望键集**只能从 34 号 / 36 号文档现算**（新增一段读 36 号 §4.1.1 的 11 行），**不许**把 10 个新键抄成第二份清单，文档解析失败就让工具报错退出、任务随即停下；打印串 `total / 93` → `total / 97`。
- **实现要点**：工具的判据是「清单长度 = 期望数」+「每个 id 在全源码树里出现」，所以**只改数量不改清单**会立刻红；config 期望来自五份**旧文档**，不补 36 号就会把 10 个新键报成「toml 多而文档没记」。
- **易漏点**：① 只改 `EXPECTED_COUNTS` 不改 `EXPECTED`；② 只加 id 不改打印串（仍是 `93`）；③ 把新键硬编码成第二份清单（该工具最忌讳的退化，31 号 §5 的 A-40 同一条纪律）；④ 工具的 §4 禁令扫描里有一条「源码占位标记」的正则，作用域是 `src/` 下的 `.java`——本文件不在其中，不受影响。
- **验收断言**：
  1. `python -X utf8 tools/verify_project.py` 退出码 0，输出 `已核 registry 对象: 97 / 97`。
  2. 同一输出里 config 段 `175 个配置键与文档逐字一致`、lang 段 `共 337 键`。
  3. 活判据：把 `EXPECTED_COUNTS["blocks"]` 临时改成 28 后运行，退出码非 0；恢复后回到 0。
  4. `git diff --stat docs/` 为空（工具只读上游，不回改）。

#### F-244 · 36 号 §8 的 36 条验收（L · 3.0 人日｜依赖 F-215 ~ F-240，见 §3）

- **目标**：逐条执行 36 号 §8 的 **36 条**验收并落记录，未通过 0 条。
- **涉及文件**：新建记录 `.work/v020-verify/acceptance.md`；截图 / 录屏放 `.work/v020-verify/`。
- **具体改动**：按 #1 ~ #36 的顺序，每条写「判据原文 / 观察位置 / 实际值 / 通过」，期望值**以 36 号 §8 原文为准**（本文不复述）；#6~#8、#18~#20、#23~#26、#31~#34 是手动场景，各留一张截图。
- **实现要点**：#1（`→ OK`）必须在 F-228 之后跑；#14（`.dat` mtime 不变）必须在 F-233 之后跑（A-69）；#21~#22 要构造「核接近满」；#35 的池断言与 F-216 是同一行日志。
- **易漏点**：① 把 #1 的期望写成窗口期的 FAIL（拿旧日志当结果）；② 用 F-233 之前的存档验 #14；③ 只记「通过」不记实际值（无法复核）；④ 把 36 条与 40 条边界混在一份记录里（边界是 F-245）。
- **验收断言**：
  1. 记录 **36** 行（+ 表头），`未通过 = 0`。
  2. 每行的「观察位置」与「实际值」两列都非空。
  3. #1 的日志原文与 36 号 §8 #1 的字符串**逐字一致**（含 `→ OK` 与 12 个分项）。
  4. 手动场景的截图数 ≥ **13**（#6~#8 三张、#18~#20 三张、#23~#26 四张、#31~#34 四张）。

#### F-245 · 35 号 §12 的 40 条边界复跑（L · 3.0 人日｜依赖 F-244）

- **目标**：逐条复跑 35 号 §12 的 **B-01 ~ B-40**，不符 0 条。
- **涉及文件**：新建记录 `.work/v020-verify/boundaries.md`。
- **具体改动**：按 B 编号顺序，每条写「场景 / 期望行为 / 实测 / 结论」；B-05 ~ B-07 是指令与老档场景，B-26 ~ B-27 是掉落场景，B-33 ~ B-34 是交易抽样，B-40 是「恢复注释会立刻复活」。
- **实现要点**：B-33（老档满级农民不补发）与 B-34（交易是抽样）是**被接受的代价**，验证方式是「拿 0.1.1 老档的满级农民看它没有新项」，**不判失败**（36 号 §2.4.2）；B-40 的验证在**临时工作副本**上恢复注释、验完还原，不许入库。
- **易漏点**：① 把 B-33 判成缺陷；② 把 B-34 判成「这个村民坏了」；③ 用 F-233 之前的存档验 B-20 / B-21；④ B-26 的三个不掉落场景（活塞 / 同方块 / 卸载）只验一个。
- **验收断言**：
  1. 记录 **40** 行（+ 表头），`不符 = 0`。
  2. B-12 / B-13 / B-15 / B-16 / B-19 / B-20 各有 `/data get block` 的实际 NBT 值（`Core` 键的有无是关键）。
  3. B-26 / B-27 各记实际掉落物清单（空清单要写「空」，不能留白）。
  4. B-40 的记录里写明「在临时副本上恢复、验完还原，`git status` 无残留」。

#### F-246 · 红线、注册面与上游一致性签字（M · 1.5 人日｜依赖 F-243、F-244、F-245）

- **目标**：逐项签 8 条收口判据（D-172、A-62 ~ A-69 的落地核对），并确认上游四份文档一字未改。
- **涉及文件**：新建记录 `.work/v020-verify/signoff.md`；只读 `docs/zh/34` ~ `docs/zh/37`、`src/`。
- **具体改动**：8 行签字表——① 注册面 97（12 类分项）；② config 175 键 / 60 组；③ 语言键 337 × 2；④ 动作面 46/46（表 A）；⑤ `@Mixin` 0、`.playToClient` 2、协议串 `2`；⑥ `Farmstorm.java` 的 5 个自检数字；⑦ `farmstorm_farmland.dat` 仍在且 mtime 未变（F-233 之后）；⑧ `FarmlandLedger` 555 行 / `FarmlandGuard` 225 行逐字未变。
- **实现要点**：红线里「网络包不设上限」**不等于**「随便加」——本版只 1 个新包（D-172）；两个保留类**一行不改**是 A-62 / RT-42 的硬约束，行数是它的可核对形式。
- **易漏点**：① 把 `.dat` 的「mtime 未变」放在 F-233 之前验（A-69）；② 把「协议串 2」写成「版本 2.0」；③ 漏签「上游四份文档 `git diff` 为空」——本文件的写作前提就是只读上游；④ 把 46 行数成 47（表外的表外项不算行，见 §6 表 A 的表下注）。
- **验收断言**：
  1. `git diff --stat docs/zh/34-DECISIONS-0.2.0.md docs/zh/35-DESIGN-0.2.0.md docs/zh/36-TECH-0.2.0.md` **为空**。
  2. `@Mixin` 全仓命中 **0**；`playToClient` 在 `FarmstormNetwork.java` 命中 **2**；`PROTOCOL_VERSION = "2"`。
  3. 一个启动日志 + 三处计数同时成立：`97` / `175 键 60 段` / `337 键 × 2`，且与 F-243 的三行输出（`已核 registry 对象: 97 / 97`、`175 个配置键与文档逐字一致`、`共 337 键`）逐字一致。
  4. `FarmlandLedger.java` **555** 行、`FarmlandGuard.java` **225** 行（逐字未变）；`world/data/farmstorm_farmland.dat` 存在且 mtime 与升级前相同。
  5. 签字表 8 行全绿，记录里每行给出「判据 / 实测值 / 结论」。

---

## 3. 依赖图与并行度

### 3.1 七条依赖链（示意）

下图只画**前驱 → 后继**的主干，不追求把每条边都画全（F-241 实际等 8 个前驱、F-244 等 26 个）：

```
链 1（版本与规则）：  F-213      （无前驱，可第一天做）
链 2（品质）：        F-214 → F-215 ─┐
                        └→ F-216     ├→ F-241 → F-243
链 3（停用与交易）：  F-220 → F-221  │
                        └→ F-222 → F-223
链 4（核心）：        F-226 → F-227 → F-228 ─┐
                        ├→ F-229             ├→ F-242 → F-243
                        ├→ F-230             │
                        └→ F-231 → F-232 → F-233 → F-235 → F-236
链 5（培养器）：      F-224 → F-225          │
                        ├→ F-234 ────────────┘
                        ├→ F-237
                        └→ F-232（见链 4）
链 6（连线）：        F-238 → F-239（另需 F-233）
                        └→ F-240
收口：                F-244 → F-245 → F-246（F-244 等 F-215 ~ F-240 全部落地）
```

**读图三条**：

1. **唯一的硬串行段是 F-231 → F-232 → F-233 → F-235 → F-236**（绑定 → 放置绑定 → 迁移 → 状态机 → 产出）。顺序不能换：状态机的 `isUsable` 要读核侧列表（F-231），产出要调 `acceptAll`（F-226），迁移要改绑（F-232 的写入形态）。
2. **F-224 与 F-226 是两条互不依赖的起点**：培养器方块先落地（注册 + 资源），核心容器另起一支（容器 → 菜单 → 交互 → 掉落）。两者只在 F-232（放置绑定）汇合。
3. **F-239 要等 F-233**：连线的「绑定变化」时机包含迁移（改绑），迁移没落地时广播器少一个触发点，会留一个「拆核后线还挂着」的洞（35 号 §12 B-39）。

### 3.2 关键路径

按依赖取最长前驱（每个节点取所有前驱累计完成时间的最大值）：

```
F-226 核心改容器            3.0   （累计 3.0）
 └─ F-231 绑定列表          1.5   （累计 4.5）
     └─ F-232 放置时绑定    3.0   （累计 7.5，另需 F-224 的 3.0，取大者 7.5）
         └─ F-235 状态机    3.0   （累计 10.5，另需 F-234 的 3.5）
             └─ F-236 产出  3.0   （累计 13.5）
                 └─ F-244 验收 3.0 （累计 16.5）
                     └─ F-245 边界 3.0 （累计 19.5）
                         └─ F-246 签字 1.5 （累计 21.0）
```

**关键路径 = 21.0 人日 / 8 个任务**，占总量 61.0 的 **34.4%**。这条链是纯依赖推算的结果。

**并列的第二条 21.0**：`F-226 → F-231 → F-232 → F-233 → F-239 → F-244 → F-245 → F-246`（`3.0 + 1.5 + 3.0 + 3.0 + 3.0 + 3.0 + 3.0 + 1.5 = 21.0`）。它在 F-232 之后分叉走「迁移 → 连线」而不是「状态机 → 产出」，两条在 F-244 之前同时到达 13.5——排期时把 F-233 与 F-235 视作同一层即可。

次长链（同样按依赖取最长前驱，不进关键路径）：

| 排名 | 链 | 人日 |
|---|---|---|
| 3 | F-224 → F-234 → F-235 → F-236 → F-244 → F-245 → F-246 | 3.0 + 0.5 + 3.0 + 3.0 + 3.0 + 3.0 + 1.5 = **17.0** |
| 4 | F-226 → F-227 → F-228 → F-229 → F-244 → F-245 → F-246 | 3.0 + 3.0 + 0.5 + 1.5 + 3.0 + 3.0 + 1.5 = **15.5** |
| 5 | F-220 → F-222 → F-223 → F-244 → F-245 → F-246 | 1.5 + 1.5 + 3.0 + 3.0 + 3.0 + 1.5 = **13.5** |
| 6 | F-238 → F-239 → F-244 → F-245 → F-246 | 1.5 + 3.0 + 3.0 + 3.0 + 1.5 = **12.0**（F-239 实际被 F-233 的 10.5 门控） |

> **读法**：前四条链都汇到 F-244，所以 **F-244 之前的所有 L 都是并行的**；想缩短日历工期，优先压 `F-231 → F-232 → F-235 → F-236` 这一段（6 个任务里 4 个 L，合计 10.5 人日），而不是压验收（验收再压也压不过 6.0 人日的 F-244 + F-245）。

### 3.3 并行簇

| 簇 | Phase | 可并行任务 | 并行度 | 说明 |
|---|---|---|---|---|
| A | 32 ~ 34 | F-213、F-214、F-217、F-218、F-219、F-220 | 6 | 六条互不共享文件（F-214 与 F-216 串行；F-220 → F-221 / F-222 串行） |
| B | 35 | F-224 → F-225 与 F-226 → F-227 → F-228 | 2 | 培养器线、核心线互不依赖，只在 F-228 前汇合（窗口关） |
| C | 36 | F-229、F-230、F-231 → F-232 → F-233 | 3 | 菜单交互、掉落、绑定三条分支 |
| D | 37 | F-234、F-237 与 F-235 → F-236 | 2 | 配置与掉落可插空，状态机与产出天然串行 |
| E | 38 | F-238 → F-239 与 F-238 → F-240 | 2 | 数据面先行，广播器与渲染器可两人并行 |
| F | 39 | F-241、F-242、F-243 | 3 | 三个总账互不共享文件；F-244 / F-245 / F-246 逐级串行 |

**并行度上限 6**（簇 A），实际受**同文件冲突**约束：F-214 / F-216 都改 `CropProfile.java`（串行）；F-220 / F-222 / F-226 / F-231 / F-234 都改 `FarmstormConfig.java`（按 220 → 222 → 226 → 231 → 234 串行）；F-224 / F-232 / F-235 / F-237 都改 `CultivatorBlock.java` / `CultivatorBlockEntity.java`（串行）；F-215 / F-221 / F-222 / F-224 / F-226 / F-227 / F-231 / F-234 / F-241 都改 `ModLangProvider.java`（按 Phase 顺序各自追加自己那一段，最后 F-241 收口）。按「一个任务只追加自己那一段」的约定后，**可稳定并行 3 路**。

---

## 4. 人日汇总

### 4.1 按 Phase 与里程碑

| Phase | 名称 | 任务 | 任务数 | S | M | L | 人日 | 里程碑 |
|---|---|---|---|---|---|---|---|---|
| 32 | 基线与品质载体 | F-213 ~ F-215 | 3 | 1 | 1 | 1 | `0.5 + 1.5 + 3.0` = **5.0** | **M28** |
| 33 | 规则面收窄 | F-216 ~ F-219 | 4 | 4 | 0 | 0 | `4 × 0.5` = **2.0** | **M29** |
| 34 | 耕地停用与交易 | F-220 ~ F-223 | 4 | 1 | 2 | 1 | `0.5 + 2 × 1.5 + 3.0` = **6.5** | **M30** |
| 35 | 注册面、资源与容器骨架 | F-224 ~ F-228 | 5 | 1 | 0 | 4 | `0.5 + 4 × 3.0` = **12.5** | **M31** |
| 36 | 核心交互、绑定与迁移 | F-229 ~ F-233 | 5 | 1 | 2 | 2 | `0.5 + 2 × 1.5 + 2 × 3.0` = **9.5** | **M32** |
| 37 | 培养器行为 | F-234 ~ F-237 | 4 | 2 | 0 | 2 | `2 × 0.5 + 2 × 3.0` = **7.0** | **M33** |
| 38 | 连线 | F-238 ~ F-240 | 3 | 0 | 1 | 2 | `1.5 + 2 × 3.0` = **7.5** | **M34** |
| 39 | 收口与全量验收 | F-241 ~ F-246 | 6 | 1 | 3 | 2 | `0.5 + 3 × 1.5 + 2 × 3.0` = **11.0** | **M35** |
| **合计** | | **F-213 ~ F-246** | **34** | **11** | **9** | **14** | **61.0** | **M28 ~ M35** |

### 4.2 总计（两个数 + 算式）

- **裸人日**：`11 × 0.5 + 9 × 1.5 + 14 × 3.0 = 5.5 + 13.5 + 42.0 = **61.0 人日**`
- **含 25% 缓冲**：`61.0 × 1.25 = **76.25 人日**`

三条互相校验：

1. 复杂度汇总：`11 + 9 + 14 = 34` = 任务数；
2. 人日汇总：`5.5 + 13.5 + 42.0 = 61.0` = 八个 Phase 小计之和 `5.0 + 2.0 + 6.5 + 12.5 + 9.5 + 7.0 + 7.5 + 11.0 = 61.0`；
3. 缓冲：`61.0 × 1.25 = 76.25`。

> 缓冲只吸收返工与调试，不吸收范围变化。与 0.1.1（37 个任务 / 65.5 裸人日）相比，本版任务少 3 个、裸人日少 4.5：变化来自两处——本版有 4 个纯 S 任务（Phase 33）与 3 个 S 收尾项，而被砍掉的旧耕地「写绑定 + 补建 + 额度」三件套在 0.1.1 里是 3 个 M；代价是 L 的占比从 27% 升到 41%（容器、菜单、迁移、状态机四段都是 L）。

### 4.3 并行后的日历工期

- **单串行**：`76.25 人日`，按 5 天/周 ≈ **15.3 周**。
- **稳定 3 路并行**（§3.3 末段）：`61.0 ÷ 3 ≈ 20.3 人日` 裸，缓冲后 `20.3 × 1.25 ≈ **25.4 人日** ≈ **5.1 周**（@ 5 天/周）。
- **关键路径下界**：`21.0 人日`（§3.2），即使无限加人也不会低于它——**这条链不能压**：容器 → 绑定 → 放置绑定 → 状态机 → 产出 → 验收 → 签字的七段是「先有数据结构、才有状态机、才有验收」的串行关系，压掉任何一环都会让后面三批验收失去判据。
- **与 3 路并行的差**：`25.4 − 21.0 = 4.4 人日`，即 3 路并行后只剩 4.4 人日的余量，再想快只能加人做**非**关键路径上的 L（F-225 资源、F-240 渲染、F-244 / F-245 验收）。

---

## 5. 裁决台账（A-62 起）

本节收 **8 条**：A-62 由 36 号 §10 第 7 条点名要求、并与 36 号 §10 的两条给 37 号的提示对齐；A-63 ~ A-69 是本文写作期新立。每条给：结论 / 依据 / 落地位置 / 状态。

### 5.1 A-62 · `farmlandBonus`：只注释定义，保留字段声明

- **结论**：`FarmstormConfig.java:227` 的字段声明**原样保留**；只注释 `:909-912` 的 `.defineInRange(...)` 定义；`FarmlandGuard.java:75` 与 `FarmlandLedger.java:305` 两处 `.get()` **原样保留**（运行期不可达）。
- **依据**：36 号 §10 第 7 条（唯一需要作者拍板的**实质**冲突，36 号 §10 的两条提示第 1 条），34 号 D-153 的「注释读取」与「两个类一行不改」互斥；两处读取点确实在这两个类里（`FarmlandGuard.java:75`、`FarmlandLedger.java:305`，源码已核）。34 号 §3.3 第 5 行把「字段与定义」一起登记为停用，35 号 §7.2 落点 6 也写「字段、定义 → 注释掉」——**这两处口径按本条回灌**。
- **理由（保留声明）**：保留声明让两个死类仍能编译——`FarmlandGuard` / `FarmlandLedger` 是「一行不改」的硬约束（裁定 #3、RT-42），删字段会让它们编译不过，等于变相改它们。
- **代价**：字段此时恒为 `null`；若将来有人只恢复监听器 `:75` / `:76`、不恢复定义，第一锄会得到 `NullPointerException` 而不是默认值——这正是 RT-42「误恢复」的具体形态。**缓释**：在 `:227` 旁边写两行注释（定义已注释 ⇒ 字段为 null ⇒ 恢复监听器前必须先恢复定义），并在 `:75` / `:76` 的注释里各写一行「裁定 #3：v0.2.0 撤销耕地，勿恢复」。
- **否决**：① 改那两个类各 1 行（把 `.get()` 删掉）——违反 D-153 与裁定 #3 的「本体一行不改」；② 改成硬编码常量 16——等于偷偷留着耕地额度，与 D-153「额度概念整体消失」（34 号 §6 D-139 行）正面冲突。
- **落地位置**：**F-220**（实现与注释）、**F-242**（`[outpost_core]` 段无该键的判据）、**F-244**（36 号 §8 #12 的验收）。回灌目标：34 号 §3.3 第 5 行、35 号 §7.2 落点 6。
- **状态**：**已落**（本版口径，A-62 为第一条）。

### 5.2 A-63 · 幸运浆果池的 11 条必须由 `tier()` 推导，不写第二份 id 清单

- **结论**：`LUCKY_BERRY_POOL` 从「排除 3 条」改成「只留 `tier() == COMMON`」，**不许**写成手写 11 条 `CropProfile` 引用的列表。
- **依据**：源码 `CropProfile.java:139-143` 现在就是 filter 构造（不是字面量列表），D-156 要求「11 条恰好等于普通档全集」，36 号 §7 RT-49 的缓释 (a) 要求「22 条只在一处定义、三处都读同一个 `tier()`」。手写清单会让「档位」与「池」形成两份真相：改一条 tier 之后池不会跟着变，RT-49 的防线当场失效。
- **落地位置**：**F-216**（池的构造与注释）、**F-244**（36 号 §8 #35 的验收按「11 条全部 `COMMON`」写，不按 id 枚举写）。
- **状态**：**已落**（本文新立）。

### 5.3 A-64 · 培养器的创造成分页条目必须补；36 号 §3 的 46 行未收录这一处

- **结论**：`FarmstormItems.creativeTabItems()`（`:122-129`）必须加 `CULTIVATOR`。
- **依据**：`:122-129` 是**显式列表**（作物 → 播种物 → 三个工具方块），注册了物品但不在这个列表里就不会出现在创造栏；而本版**没有**培养器配方（36 号 §5 的配方仍是 29 条、逐条点名里没有任何一条产培养器），生存模式也没有获取路径 ⇒ 不加就是「注册了但谁都拿不到」。36 号 §2.9 对转换器用过同一条理由（「装饰方块必须拿得到」），同一理由必须适用于新增方块。
- **落地位置**：**F-224**（创造栏那一行）、**F-244**（验收：创造栏末段多一项）。
- **状态**：**已落**（本文新立；36 号 §3 的 46 行不动，本条不新增行）。

### 5.4 A-65 · 两支 `.cmd` 的 jar 名必须随版本号同步改；D-148 的落地位置漏了它们

- **结论**：`一键构建.cmd:11-12` 与 `检查环境.cmd:14` 里写死的 `farmstorm-0.1.1+mc1.21.1[.jar|-sources.jar]` 要改成 `0.2.0`。
- **依据**：34 号 D-148 的落地位置只列了 `gradle.properties:11`、六份文档与 `README.md`，**没有**这两支脚本；36 号 §3 的 46 行也没有脚本行。而 0.1.1 期这两处已经写死成 0.1.1 的 jar 名（`一键构建.cmd:11-12`、`检查环境.cmd:14`，实测）⇒ 只改 `mod_version` 会让「一键构建」的 `copy` 复制一个不存在的文件、「检查环境」报「jar: 缺失」。
- **落地位置**：**F-213**（三处字面量）、**F-244**（产物面验收）。
- **状态**：**已落**（本文新立；该动作**不进**表 A 的 46 行，理由与 A-64 相同）。

### 5.5 A-66 · `tools/verify_project.py` 的期望面必须同步，否则它会静默少核 4 个对象

- **结论**：`EXPECTED`（12 类 id 白名单）、`EXPECTED_COUNTS`（93 基线）、五份旧文档的 config 并集、`total / 93` 的打印串，四处都要同步到 97 / 175 / 337。
- **依据**：36 号 §1.2 说「`tools/verify_project.py` 的 `EXPECTED_COUNTS` 改后 = 97」，但它的 §3（46 行）与 §8（36 条）都没有为工具改造留位置。工具的第 2 段是「清单里每个 id 在源码树出现 + 清单长度 = `EXPECTED_COUNTS`」，**多出来的注册对象它根本不看**——所以 v0.2.0 期间它会照常打印 `已核 registry 对象: 93 / 93` 并退出 0，比报红更危险（31 号 §5 的 A-40 是同一类问题，但那一版是「报红」，本版是「静默通过」）。config 段同理：期望集来自 `01 / 12 / 18 / 24 / 30` 五份旧文档，新 10 键会被报成「toml 多而文档没记」。
- **落地位置**：**F-243**（四处同步 + 活判据）、**F-246**（签字里引用工具的三段输出）。
- **状态**：**已落**（本文新立）。

### 5.6 A-67 · 裁定 #17 ~ #19 不补登 D 号：37 号只发 A 号

- **结论**：本文一律按「裁定 #17 / #18 / #19」引用，**不**给它们新开 D 号；对应关系写作「裁定 #17 + D-166」「裁定 #18 + D-157」「裁定 #19 + D-170」。D 号若要补登，由 34 号在回灌时发（本版 D 号仍到 D-172）。
- **依据**：34 号 §0.1 的编号规则 1（「本文只发 D-xx……引用其它系列时写全前缀，不自己编号、不预支下游编号」）与规则 4（「本文不新立裁决：写作期回报里需要作者拍板的几处，由 37 号从 A-62 起登记」）；34 号 §2 备注 2 也把这件事交给 37 号。而 36 号 §10 第 15 条要求「D 编号待 37 号从 A-62 之后补登」——这与 34 号的编号纪律冲突：37 号的号段是 **A-xx**，不是 D-xx。
- **落地位置**：本文全文的「对应决策」列（F-229 写「裁定 #17」、F-223 写「裁定 #18 + D-157」、F-219 写「裁定 #19 + D-170」）；回灌目标 34 号 §0.1 / §2 备注 2（补一句「裁定 #17~#19 不另发 D 号」）与 36 号 §10 第 15 条。
- **状态**：**已落**（本文新立；不改 34 号）。

### 5.7 A-68 · 34 号把 `FarmlandLedger` 的落盘名行号写成 `:44`，实为 `:53`

- **结论**：`farmstorm_farmland` 的落盘名常量在 `FarmlandLedger.java:53`（`DATA_NAME`），`:55` 是 `dataVersion` 的标签名；`:44` 是一段讲 `coreDimension` 的 javadoc。本文与后续验收一律按 `:53` 写。
- **依据**：34 号 D-154 的「依据」与 §3.3 第 7 行都写 `:44`；36 号 §6① 写 `:53`；源码实测为 `:53`（`:42-53` 的 javadoc 到 `DATA_NAME` 的定义）。34 号 §9 没有登记这一条。
- **落地位置**：**F-220** 的「涉及文件」（不引用 `:44`）、**F-246** 的签字里 `.dat` 相关的核对；回灌目标 34 号 D-154 与 §3.3 第 7 行。
- **状态**：**已落**（本文新立；不改 34 号）。

### 5.8 A-69 · D-154 的「不读不写」是 F-233 之后才成立的判据

- **结论**：`farmstorm_farmland.dat` 的最后一个运行期读者是 `OutpostCoreBlockEntity#settleOnRemoval`（`:353` 的 `FarmlandLedger.of(server)`，以及 `:384` / `:388` / `:391` 的 `migrate` / `remove` 会 `setDirty`）。F-220 注释掉两个监听器与一行 backfill 之后，**拆核仍然会读并重写这个文件**，直到 F-233 把整段改写。因此「文件仍在、mtime 不变」这条判据只能在 **F-233 之后**执行。
- **依据**：源码 `FarmlandLedger.java:104`（`of`）、`:114-121`（`bind` 后 `setDirty`）、`:131` / `:144` / `:180`（`migrate` / `remove` 后 `setDirty`）、`OutpostCoreBlockEntity.java:353`；D-154 的「不读不写不删」与 36 号 §8 #14 的判据。
- **落地位置**：**F-220** 的易漏点（不许在 F-233 前断言 mtime）、**F-233** 的目标（最后一个读者消失）、**F-244** 的 #14 执行时点、**F-246** 的签字第 7 项。
- **状态**：**已落**（本文新立；它不改变任何决策，只改变验收的**时点**）。

---

## 6. 覆盖对照表（六张）

表 A ~ 表 F 对应 36 号 §3 / §1.2 / §4 / §4.3 / §7 / §9。每张表末行给「覆盖数 / 总数」。

### 表 A · 36 号 §3 的 46 行动作面 → 任务

| 行 | 类别 | 路径或类#方法 | 增 / 改 / 删 | 负责任务 |
|---|---|---|---|---|
| 1 | 注册 | `registry/FarmstormBlocks.java` 26 → 27 | 增 | F-224 |
| 2 | 注册 | `registry/FarmstormBlockEntities.java` 7 → 8 | 增 | F-224 |
| 3 | 注册 | `registry/FarmstormItems.java` 32 → 33 | 增 | F-224 |
| 4 | 注册 | `registry/FarmstormMenus.java` 1 → 2 | 增 | F-227 |
| 5 | 注册 | 其余 8 类（`entity_types` / `mob_effects` / `sound_events` / `recipe_types` / `recipe_serializers` / `creative_tabs` / `damage_types` / `data_components`）不变 | 核 | F-228（逐类读数）、F-246（签字） |
| 6 | 类 | `util/CropTier.java` | 增 | F-214 |
| 7 | 类 | `util/CropProfile.java:32-40`（9 字段） | 改 | F-214 |
| 8 | 数据 | `util/CropProfile.java:47-128`（22 条 tier） | 改 | F-214 |
| 9 | 数据 | `util/CropProfile.java:139-143`（池 11 条） | 改 | F-216 |
| 10 | 类 | `item/ModifiedCropItem#getName(ItemStack)` | 改 | F-215 |
| 11 | 类 | `item/DeployableCropItem#getName(ItemStack)` | 改 | F-215 |
| 12 | 类 | `item/SeedItem#getName(ItemStack)` | 改 | F-215 |
| 13 | 类 | `item/ModifiedCropItem#appendCropTooltip`（`:113-127`） | 改 | F-215 |
| 14 | 类 | `item/ModifiedCropItem#isPlantableSoil`（`:101-107`） | 改 | F-217 |
| 15 | 类 | `item/SeedItem#useOn` | 改 | F-218 |
| 16 | 类 | `block/FarmstormCropBlock`（不动） | 核 | F-217（负向核对）、F-246 |
| 17 | 停用 | `event/FarmstormGameEvents.java:75` | 停用 | F-220 |
| 18 | 停用 | `event/FarmstormGameEvents.java:76` | 停用 | F-220 |
| 19 | 停用 | `event/FarmstormGameEvents.java:241-265` | 停用 | F-220 |
| 20 | 停用 | `event/FarmstormGameEvents.java:277-281` | 停用 | F-220 |
| 21 | 停用 | `event/FarmstormGameEvents.java:171` | 停用 | F-220 |
| 22 | 停用 | `config/FarmstormConfig.java:909-912`（`:227` 保留） | 停用 | F-220、F-242 |
| 23 | 停用 | `datagen/ModLangProvider.java:403` | 停用 | F-221 |
| 24 | 停用 | `datagen/ModLangProvider.java:412-413` | 停用 | F-221 |
| 25 | 数据 | `farmland/FarmlandLedger.java` / `FarmlandGuard.java`（一行不改） | 核 | F-220（负向登记）、F-246 |
| 26 | 数据 | `farmstorm_farmland.dat`（不读 / 不写 / 不删） | 停用 | F-220（代码面）、F-244（#14，时点见 A-69） |
| 27 | 类 | `event/VillagerTradeEvents.java` | 增 | F-223 |
| 28 | 类 | `event/TierTrade.java` | 增 | F-223 |
| 29 | 数据 | 3 个交易项（等级 1/3/5、`maxUses = 1`、单次 2 个） | 增 | F-223 |
| 30 | 类 | `block/CultivatorBlock.java` | 增 | F-224（骨架）、F-235（准入）、F-232（放置） |
| 31 | 类 | `blockentity/CultivatorBlockEntity.java` | 增 | F-224（骨架）、F-235（状态机）、F-237（掉落） |
| 32 | 数据 | 培养器周期 / 产出 / 核满暂停 | 增 | F-234（配置）、F-236（行为） |
| 33 | 类 | `blockentity/OutpostCoreBlockEntity.java:48`（`implements Container`） | 改 | F-226 |
| 34 | 数据 | `OutpostCoreBlockEntity` NBT（`:411-424`）`+Items` `+Cultivators` | 改 | F-226（`Items`）、F-231（`Cultivators`） |
| 35 | 类 | `OutpostCoreBlockEntity#getUpdateTag` | 增 | F-226 |
| 36 | 类 | `OutpostCoreBlockEntity#getUpdatePacket` | 增 | F-226 |
| 37 | 类 | `OutpostCoreBlockEntity#settleOnRemoval`（`:341-394`） | 改 | F-233 |
| 38 | 类 | `block/OutpostCoreBlock#useWithoutItem` | 增 | F-229 |
| 39 | 类 | `block/OutpostCoreBlock#onRemove`（`:101-107`） | 改 | F-230 |
| 40 | 类 | `menu/OutpostCoreMenu.java` + `client/OutpostCoreScreen.java` | 增 | F-227 |
| 41 | 类 | `network/CultivatorLinkPayload.java` | 增 | F-238 |
| 42 | 类 | `network/CultivatorLinkBroadcaster.java` + `client/ClientCultivatorLinks.java` + `client/CultivatorLinkRenderer.java` | 增 | F-239（broadcaster）、F-240（client 2 类） |
| 43 | 类 | `network/FarmstormNetwork.java:17,29-31`（协议 `"1"` → `"2"`） | 改 | F-238 |
| 44 | 停用 | `block/ConverterBlock.java:78-86` | 停用 | F-219 |
| 45 | 配置 | `config/FarmstormConfig.java` 166 键 / 58 组 → 175 / 60 | 改 | F-220（−1）、F-222（+6）、F-226（+1）、F-231（+1）、F-234（+2）、F-242（总账） |
| 46 | 数据 | `Farmstorm.java:119`（池断言）/ `:147-150`（注册断言） | 改 | F-216（池半条）、F-228（注册半条） |

**46/46 覆盖，未覆盖 0**。分类计数：注册 5 行（#1~#5）、类 23 行（#6、#7、#10~#16、#27、#28、#30、#31、#33、#35~#43）、数据 8 行（#8、#9、#25、#26、#29、#32、#34、#46）、停用 9 行（#17~#24、#44）、配置 1 行（#45）——与 36 号 §3 表下的分类计数逐项相同。

**表下注两条**：

1. **表外动作面 4 项**（**不计入 46 行**，因此不占行号）：① `gradle.properties:11` 的版本号（F-213，A-65）；② 两支 `.cmd` 的 jar 名（F-213，A-65）；③ `FarmstormItems.creativeTabItems()` 的创造栏条目（F-224，A-64）；④ `tools/verify_project.py` 的期望面（F-243，A-66）。四项都是「36 号 §3 未收录、但不做就会翻车」的动作，各自有裁决背书，**不**把表 A 撑到 50 行。
2. **一行多任务的行**（#26、#30、#31、#32、#34、#42、#45、#46）都按「谁动这一段」拆给多个任务；每一段都有独立验收，回滚其中任意一个任务不影响其余段。

### 表 B · 注册面 +4（97）→ 任务

**B.1 12 类逐类**（基线 93，36 号 §1.2 自己从源码点名）

| # | registry | 基线 | 本版增量 | 改后 | 落点任务 | 核对任务 |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 32 | **+1**（`cultivator` 的 BlockItem） | **33** | F-224 | F-228 |
| 2 | `Registries.BLOCK` | 26 | **+1**（`cultivator`） | **27** | F-224 | F-228 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 7 | **+1**（`cultivator`） | **8** | F-224 | F-228 |
| 4 | `Registries.MENU` | 1 | **+1**（`outpost_core` 存储界面） | **2** | F-227 | F-228 |
| 5 | `Registries.ENTITY_TYPE` | 13 | +0 | 13 | — | F-228 |
| 6 | `Registries.MOB_EFFECT` | 4 | +0 | 4 | — | F-228 |
| 7 | `Registries.SOUND_EVENT` | 3 | +0 | 3 | — | F-228 |
| 8 | `Registries.RECIPE_TYPE` | 1 | +0 | 1 | — | F-228 |
| 9 | `Registries.RECIPE_SERIALIZER` | 1 | +0 | 1 | — | F-228 |
| 10 | `Registries.CREATIVE_MODE_TAB` | 1 | +0 | 1 | — | F-228 |
| 11 | `Registries.DAMAGE_TYPE` | 3 | +0 | 3 | — | F-228 |
| 12 | `Registries.DATA_COMPONENT_TYPE` | 1 | +0（品质走静态字段，D-150） | 1 | — | F-228 |
| **合计** | | **93** | **+4** | **97** | | `33+27+8+2+13+4+3+1+1+1+3+1 = 97` |

**B.2 4 个新 id 逐条点名**（都进 `tools/verify_project.py` 的 `EXPECTED`，F-243）

| # | id | 类别 | 落点任务 |
|---|---|---|---|
| 1 | `farmstorm:cultivator`（block） | block | F-224 |
| 2 | `farmstorm:cultivator`（block entity type） | block_entity_type | F-224 |
| 3 | `farmstorm:cultivator`（BlockItem） | item | F-224 |
| 4 | `farmstorm:outpost_core`（MenuType） | menu_type | F-227 |

**B.3 五个「加了但不进注册面」的构件**（+4 之外的全部新增物）

| # | 构件 | 为什么不进 registry | 任务 |
|---|---|---|---|
| 1 | 第 2 个网络包（`farmstorm:cultivator_link`） | payload 走 `RegisterPayloadHandlersEvent`，不进 `BuiltInRegistries`（D-172、34 号 §5.3 第 4 条） | F-238 |
| 2 | `CropTier` 枚举与第 9 字段 | 静态字段，不注册（D-150） | F-214 |
| 3 | 3 个农民交易项 | 走 NeoForge 事件，不注册（D-157） | F-223 |
| 4 | 核侧 16 条绑定列表 | 方块实体 NBT，不注册（D-165） | F-231 |
| 5 | 54 格容器 | `Container` 接口实现，不注册（D-166） | F-226 |

### 表 C · config 175 键 / 60 组 → 任务（增 10 / 删 1）

| # | 键 | 类型 | 默认 | 值域 | 动作 | 落键任务 | 中文键任务 |
|---|---|---|---|---|---|---|---|
| 1 | `outpost_core.farmlandBonus` | int | ~~16~~ | ~~0–1024~~ | **删**（只注释定义，字段保留，A-62） | F-220 | F-221 |
| 2 | `outpost_core.cultivatorCapacity` | int | **16** | 1–64 | 增 | F-231 | F-231 |
| 3 | `outpost_core.storageSlots` | int | **54** | 54–54（退路见 36 号 §9 ⑦） | 增 | F-226 | F-226 |
| 4 | `cultivator.cycleTicks` | int | **600** | 20–72000 | 增（新组） | F-234 | F-234 |
| 5 | `cultivator.yield` | int | **2** | 1–64 | 增（新组） | F-234 | F-234 |
| 6 | `trade.common.min` | int | **2** | 1–64 | 增（新组） | F-222 | F-222 |
| 7 | `trade.common.max` | int | **5** | 1–64 | 增 | F-222 | F-222 |
| 8 | `trade.rare.min` | int | **10** | 1–64 | 增 | F-222 | F-222 |
| 9 | `trade.rare.max` | int | **32** | 1–64 | 增 | F-222 | F-222 |
| 10 | `trade.epic.min` | int | **40** | 1–64 | 增 | F-222 | F-222 |
| 11 | `trade.epic.max` | int | **64** | 1–64 | 增 | F-222 | F-222 |
| **合计** | **11 行 = 1 删 + 10 增** | | | | | **`166 − 1 + 10 = 175`** | 10 条语言键 |

**三处核对**：

1. **组数**：`58 + 1（trade）+ 1（cultivator）= 60`；`outpost_core` 是**加键不是删组重建**（3 → 4 键），36 号 §4.1.2。
2. **中间态键数**（每段都能从 `farmstorm-server.toml` 数出来）：F-220 后 **165 / 58** → F-222 后 **171 / 59** → F-226 后 **172 / 59** → F-231 后 **173 / 59** → F-234 后 **175 / 60**。
3. **CLIENT 3 键不动**（`hud.enabled` / `hud.x` / `hud.y`），**不计入** 175，但计入语言键的 `farmstorm.configuration`（36 号 §4.2 / §4.3）。

### 表 D · 语言键 337 → 任务（删 3 / 增 17）

| # | 键 | 动作 | 落点任务 |
|---|---|---|---|
| 1 | `farmstorm.configuration.outpost_core.farmlandBonus` | **删** | F-221 |
| 2 | `message.farmstorm.farmland.no_core` | **删** | F-221 |
| 3 | `message.farmstorm.farmland.quota_full` | **删** | F-221 |
| 4 | `item.farmstorm.cultivator` | 增 | F-224 |
| 5 | `block.farmstorm.cultivator` | 增 | F-224 |
| 6 | `container.farmstorm.outpost_core` | 增 | F-227 |
| 7 | `tooltip.farmstorm.tier.common` | 增 | F-215 |
| 8 | `tooltip.farmstorm.tier.rare` | 增 | F-215 |
| 9 | `tooltip.farmstorm.tier.epic` | 增 | F-215 |
| 10 | `tooltip.farmstorm.tier` | 增 | F-215 |
| 11 | `farmstorm.configuration.cultivator.cycleTicks` | 增 | F-234 |
| 12 | `farmstorm.configuration.cultivator.yield` | 增 | F-234 |
| 13 | `farmstorm.configuration.outpost_core.cultivatorCapacity` | 增 | F-231 |
| 14 | `farmstorm.configuration.outpost_core.storageSlots` | 增 | F-226 |
| 15 | `farmstorm.configuration.trade.common.min` | 增 | F-222 |
| 16 | `farmstorm.configuration.trade.common.max` | 增 | F-222 |
| 17 | `farmstorm.configuration.trade.rare.min` | 增 | F-222 |
| 18 | `farmstorm.configuration.trade.rare.max` | 增 | F-222 |
| 19 | `farmstorm.configuration.trade.epic.min` | 增 | F-222 |
| 20 | `farmstorm.configuration.trade.epic.max` | 增 | F-222 |
| **合计** | **20 行 = 3 删 + 17 增** | | **`323 − 3 + 17 = 337`**（总账在 F-241） |

**逐前缀校验**（改后，与 36 号 §4.3.3 同表）：`farmstorm.configuration 169 → 178`（−1 +10）、`item 32 → 33`、`block 26 → 27`、`container 1 → 2`、`tooltip 16 → 20`、`message 2 → 0`，其余 9 个桶不变；`178 + 36 + 33 + 27 + 20 + 13 + 9 + 6 + 4 + 3 + 3 + 0 + 2 + 1 + 2 = 337`。

### 表 E · 风险 RT-42 ~ RT-50 → 任务

| 编号 | 风险（36 号 §7） | 负责验证的任务 | 该任务怎么验 |
|---|---|---|---|
| **RT-42** | 撤销耕地是「注释调用点」而非删代码；误恢复即复活旧规则 | F-220、F-233、F-246 | F-220 验 5 处注释各带理由行 + `:227` 的两行 NPE 说明；F-233 验两个类在 `OutpostCoreBlockEntity` 的引用归 0；F-246 验两个类行数 555 / 225 逐字未变 |
| **RT-43** | `settleOnRemoval` 整段改写，三出口语义重定义 | F-233、F-245 | F-233 验三出口 + `setBlock` 命中 0；F-245 逐条跑 B-19 / B-20 / B-21（改绑 / 留原地标未绑定 / 区块读不到保留原值） |
| **RT-44** | 核心加 `Container` + 16 绑定 + 54 格，NBT 体积与同步要重算，老档兼容 | F-226、F-227、F-230、F-231 | F-226 验 `getUpdateTag` 只写三键 + 老档读成 54 空槽；F-227 验槽位与 `getContainerSize()` 同数；F-230 验四条件掉落；F-231 验绑定列表不进 `getUpdateTag` |
| **RT-45** | 线框渲染全项目第一次（0 处 `RenderType.lines` 先例） | F-238、F-239、F-240 | F-238 验协议串 2 与两行自检；F-239 验范围与空包清残留；F-240 验 `renderLineBox` + 10 分钟 0 条 `Missing elements in vertex` |
| **RT-46** | 村民交易全项目第一次（0 处 villager 代码） | F-222、F-223、F-245 | F-222 验 6 键值域；F-223 验等级位次 1/3/5 与 `maxUses = 1`、价格落区间；F-245 逐条跑 B-30 ~ B-34（断货 / 补货 / 抽样 / 老档不补发） |
| **RT-47** | 动画贴图全项目第一次（0 个模组 `.png.mcmeta`） | F-225、F-244 | F-225 验 2 个 mcmeta + 2 个 variant + `_still` 不带 mcmeta；F-244 验 36 号 §8 #18 ~ #20（两态 / mcmeta / 空槽不动） |
| **RT-48** | 切断种植有两条路径，只堵一条会漏 | F-217、F-218、F-245 | F-217 与 F-218 **各一条验收、不合并**；F-245 跑 B-01 ~ B-04（15 个作物 / 7 个部署型 / 7 个种子 / 创造模式） |
| **RT-49** | 品质是新概念，22 条 tier 必须逐条一致 | F-214、F-216、F-223、F-235 | F-214 验逐档点数 11/7/4；F-216 验池 = 普通全集；F-223 验三档抽样池；F-235 验准入面 18 = 11 + 7；F-244 的 #3 逐条比对 34 号 §4.1 |
| **RT-50** | 池 19 → 11，`Farmstorm.java:119` 的断言会红（只写日志、不崩） | F-216、F-244 | F-216 **池与断言同一提交**，验 `11 种…→ OK` 且保留「不含三条」的另半条；F-244 的 #35 逐字比对日志 |
| **合计** | **9 条** | | **9/9，未覆盖 0** |

### 表 F · 待验证 API 13 条 → 任务

| # | 待验证的 API / 语义（36 号 §9） | 由哪个任务在哪一步确认 | 确认不了时的退路 |
|---|---|---|---|
| ① | `VillagerTradesEvent` 的 list 可变性与重载期顺序 | **F-223 第一步**：先只挂一项，起服后在村民列表里看到它；再改成三项 | list 不可变 ⇒ `event.getTrades().put(level, new ArrayList<>(旧 + 新))`（36 号 §9 ① 的退路，口径不变） |
| ② | `RenderType.lines()` 在 `AFTER_PARTICLES` 阶段的可画性 | **F-240 第一步**：先画一条固定坐标的线（不依赖网络包），看是否可见 | 阶段不可用 ⇒ 退 `AFTER_TRIPWIRE_BLOCKS`；`bufferSource` 已被 flush ⇒ 在 `AFTER_ENTITIES` 捕获矩阵、`RenderGuiEvent` 重画（36 号 §9 ② 的退路） |
| ③ | mcmeta 动画 + 静态帧双贴图在 blockstate 层的表达 | **F-225 第一步**：`runData` 后看 `blockstates/cultivator.json` 两态，进游戏看空闲是否静止 | 切换闪帧 ⇒ 两个模型全用静态图、工作态单独一张过渡图；动画只在 `working=true` 不成立 ⇒ 回 34 号（本版不做） |
| ④ | 核心加 `Container` 后的 NBT 兼容与 `getUpdateTag` 体积 | **F-226 第一步**：老档放一个核心 → 读成 54 空槽；存 3 个物品 → 重启仍在 | 客户端某处确实需要物品 ⇒ 只把「非空槽数 + 首槽物品」放进 `getUpdateTag`（≤ 200 字节） |
| ⑤ | 「暂停产出」的检测时机（完成时判一次） | **F-236 第一步**：核塞满 → 看 `Progress == cycleTicks` 且不动 | 恢复延迟 1 秒不可接受 ⇒ 在核的 `setItem` / `removeItem` 里加一次「唤醒绑定培养器」的回调（判定时机不动） |
| ⑥ | `CropTier` 名色的实现（`getName` + `withStyle`） | **F-215 第一步**：普通 / 稀有 / 史诗各拿一个在背包里看色 | 铁砧改名丢档色 ⇒ 在 `ItemTooltipEvent` 再包一层；rarity 包装覆盖 ⇒ `getName` 返回 `Component.empty().append(带色文本)` |
| ⑦ | `ModConfigSpec` 是否接受 `min == max`（`storageSlots` 54–54） | **F-226 第一步**：起服看 config 加载是否报错 | 被拒 ⇒ 改 `54..55` + 创建菜单时 `Math.min(54, 读值)`（36 号 §9 ⑦ 的退路，格数不变） |
| ⑧ | `quickMoveStack` 在 54 + 36 槽下的边界 | **F-227 第一步**：开界面后 `Shift+左键` 两个方向的物品 | 边界不对 ⇒ 照原版 `ChestMenu` 的两段（0–53 / 54–89）重写，`Slot#mayPlace` 兜底 |
| ⑨ | 手持方块右键培养器的可达性 | **F-235 第一步**：手持方块右键培养器，确认走的是放置 | 无退路（原版 `ServerPlayerGameMode` 的顺序）；把「手持方块放不进」写进玩家可见说明 |
| ⑩ | `Containers.dropContents(Level, BlockPos, NonNullList)` 与培养器的配合 | **F-237 第一步**：槽里有作物时挖掉，看掉落位置 | 位置不佳 ⇒ 改用 `Containers.dropItemStack(...)` 逐件掉 |
| ⑪ | `MerchantOffer` 的 `priceMultiplier = 0.0F` 对原版折扣的影响 | **F-223 第二步**：成交后反复看基准价是否仍等于报价 | 有副作用 ⇒ 改 `0.05F`（基准价可能 +1，区间上界仍由 `clamp` 兜住） |
| ⑫ | 绑定变化的 `setBlockUpdated` 是否足够让浮窗 / 连线刷新 | **F-232 第一步**：放置 / 迁移后看浮窗与连线是否立刻变 | 没刷新 ⇒ 在三个变化时点额外调一次 `CoreListBroadcaster.requestAll(server)` |
| ⑬ | 自检格式串在 97 之后是否仍对齐（13 个参数） | **F-228 第一步**：起服读整行，逐项比对 36 号 §8 #1 的字符串 | 参数错位 ⇒ 只调参数顺序、不改格式串；验收一律按**内容 grep**（`自检 注册对象 97 个`）而不是按行号 |
| **合计** | **13 条** | **13/13 落到具体任务与具体步骤** | **13/13 给出可执行退路** |

---

## 7. 本版自检（这个文档自身的验收）

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 任务编号连续、无跳号 | `grep -c "^#### F-"` = **34**；区间 `F-213 ~ F-246` ⇒ `246 − 213 + 1 = 34` | 通过 |
| 2 | Phase 与里程碑连续 | Phase `32 ~ 39` 共 **8** 个；里程碑 `M28 ~ M35` 共 **8** 个，一一对应（§4.1 表） | 通过 |
| 3 | 裁决编号 | A-62（36 号点名）+ A-63 ~ A-69，共 **8** 条，连续无跳号；A-62 是第一条且排在 §5.1 | 通过 |
| 4 | 表 A 覆盖 | 36 号 §3 的 **46 行一行不漏**，未覆盖 **0**；分类计数（注册 5 / 类 23 / 数据 8 / 停用 9 / 配置 1）与 36 号 §3 表下计数逐项相同 | **46/46** |
| 5 | 表 B 覆盖 | 12 类逐类（合计 93 → 97）；4 个新 id 逐条点名；5 个不进注册面的构件各有任务 | **21 项**（12 类 + 4 id + 5 构件） |
| 6 | 表 C 覆盖 | 11 行（1 删 + 10 增）；`166 − 1 + 10 = 175`；组数 `58 + 2 = 60`；五段中间态键数自洽 | **11 行** |
| 7 | 表 D 覆盖 | 20 行（3 删 + 17 增）；`323 − 3 + 17 = 337`；逐前缀相加 = 337 | **20 行** |
| 8 | 表 E 覆盖 | RT-42 ~ RT-50 共 **9** 条，每条至少一个任务负责验证且给了「怎么验」 | **9/9** |
| 9 | 表 F 覆盖 | 36 号 §9 的 **13** 条，每条落到「具体任务的具体步骤」+「具体退路」，无「实现时注意」 | **13/13** |
| 10 | 每个任务 ≥ 1 条可观察验收 | 34 个任务逐条读过：全部有落在 `latest.log` / `*.toml` / `src/generated` / `/data get` / 游戏内 / `grep` 计数上的断言；无「表现正常」「手感良好」 | 通过 |
| 11 | 每个任务有依赖 | §1 表「依赖」列 + 任务标题行 `依赖`，34 / 34 一致；其中**无前驱（`—`）9 个**：F-213、F-214、F-217、F-218、F-219、F-220、F-224、F-226、F-238 | 通过 |
| 12 | 依赖无环、编号递增 | 每个任务的依赖编号都小于自身编号；按 Phase 排序即拓扑序，无回边 | 通过 |
| 13 | 工时自洽 | `11 × 0.5 + 9 × 1.5 + 14 × 3.0 = 61.0`；八个 Phase 小计相加 = 61.0；`61.0 × 1.25 = 76.25`；`S + M + L = 11 + 9 + 14 = 34` | 通过 |
| 14 | 中间态窗口自洽 | 窗口 = F-224 → F-228；三段数字 `93 / 96 / 97` 与分项（items 33、blocks 27、BE 8、menu 1 → 2）逐项对得上；F-224 ~ F-227 四个任务的验收**都不含**「自检 注册对象 … → OK」，唯一写 OK 的是关窗任务 F-228 | 通过 |
| 15 | 数字不发明 | 97 / 175 / 337 / 46 / 36 / 40 / 25 / 22 / 13 / 9 / 11 / 7 / 4 / 18 / 5 / 19 / 600 / 54 / 16 / 2 / 3 全部可追到 34 号、35 号、36 号或它们的算术；无新造玩法数值 | 通过 |
| 16 | 禁用词与语气 | 全文无未定事项标记、无待办占位；人称一律用「作者」「实现者」，不用第二人称敬语；结论句一律祈使 | 通过 |
| 17 | 表外项不撑大表 A | 表 A 恰 46 行；4 项表外动作（版本号 / `.cmd` / 创造栏 / 工具）写在表下注里并各有裁决（A-64 / A-65 / A-66） | 通过 |
| 18 | 只新建一个文件 | 本文之外不改任何文件：`docs/zh/34` ~ `36`、`docs/en`、`src/`、`.work/` 一字未改 | 通过 |

---

## 附录 A　任务号 ↔ 决策号 ↔ 验收号 三向索引

「36 号 §8」列给该任务直接负责的验收编号；「35 号 §12」列给该任务直接负责的边界编号；`—` 表示不直接对应（验证来自计数、产物或工具）。

| 任务 | 决策号 | 36 号 §8 | 35 号 §12 | 风险 |
|---|---|---|---|---|
| F-213 | D-148、A-65 | —（产物面） | — | — |
| F-214 | D-149、D-150 | #3、#4 | B-37 | RT-49 |
| F-215 | D-151 | —（名色 / tooltip 手测） | B-36 | RT-49 |
| F-216 | D-156、A-63 | #35 | B-28、B-29 | RT-50 |
| F-217 | D-152 | #6、#8 | B-01、B-02、B-05 | RT-48 |
| F-218 | D-152 | #7 | B-03 | RT-48 |
| F-219 | D-170、裁定 #19 | #36 | B-35 | — |
| F-220 | D-153、D-154、A-62、A-69 | #9、#10、#12、#14 | B-40 | RT-42 |
| F-221 | D-153 | #13 | — | — |
| F-222 | D-159 | #17（载体） | — | RT-46 |
| F-223 | D-157、D-158、D-159、裁定 #18 | #15、#16、#17 | B-30 ~ B-34 | RT-46 |
| F-224 | D-160、D-171、A-64 | #1（窗口开） | B-10、B-12 | RT-44、RT-47 |
| F-225 | D-160、D-161 | #18、#19 | — | RT-47 |
| F-226 | D-166 | #24（前置）、#26（容器面） | B-24、B-25 | RT-44 |
| F-227 | D-166、D-171 | #24（界面面） | B-24 | RT-44 |
| F-228 | D-171 | #1（关窗） | — | — |
| F-229 | D-166、裁定 #17 | #24、#25 | B-22、B-23 | RT-44 |
| F-230 | D-167 | #26 | B-26、B-27 | RT-44 |
| F-231 | D-165 | #21（前置） | B-14 | RT-44 |
| F-232 | D-165 | #27（前置） | B-12、B-13、B-14 | RT-44 |
| F-233 | D-168、A-69 | #27、#28、#29、#30 | B-19、B-20、B-21 | RT-43 |
| F-234 | D-163 | —（配置面，§8 无专条） | — | — |
| F-235 | D-160、D-162、D-164 | #20、#23 | B-08、B-09、B-11、B-17 | RT-44、RT-47 |
| F-236 | D-163、D-164 | #21、#22 | B-15、B-16、B-18 | RT-44 |
| F-237 | D-160 补充 | —（任务内断言） | B-10 | — |
| F-238 | D-169、D-172 | #2 | — | RT-45 |
| F-239 | D-169 | #31、#32 | B-38、B-39 | RT-45 |
| F-240 | D-169 | #31、#33、#34 | B-38、B-39 | RT-45 |
| F-241 | 36 号 §4.3 | #13（键集面） | — | — |
| F-242 | 36 号 §4.1、D-153 | #11 | — | — |
| F-243 | A-66、36 号 §1.2 | —（工具面） | — | — |
| F-244 | 36 号 §8 全 36 条 | #1 ~ #36 | （B 面由 F-245） | 全部 9 条 |
| F-245 | 35 号 §12 全 40 条、A-69 | — | B-01 ~ B-40（**40 条逐条对应**） | 全部 9 条 |
| F-246 | D-172、A-62 ~ A-69 | #1、#2、#14（签字面） | — | RT-42、RT-45 |

三条对账说明：

1. **决策号覆盖**：D-148 ~ D-172 共 25 条，逐条至少出现在一个任务的「决策号」列里；出现最多的是 D-166（F-226、F-227、F-229、F-244 四处），因为它同时管容器、菜单、访问条件与老档兼容。
2. **验收号覆盖**：36 号 §8 的 #1 ~ #36 由 F-244 全包（#3、#4 等实现任务另在自身验收里先行验一遍，属「提前验一层」，不替代 F-244）。
3. **边界号覆盖**：35 号 §12 的 B-01 ~ B-40 由 F-245 全包（40 条逐条对应）；实现任务的验收里已单点断言 23 条，剩 17 条（B-04、B-06、B-07、B-18、B-22、B-23、B-25、B-28、B-29、B-31、B-32、B-33、B-34、B-36、B-37、B-40 与 B-05 的半条）只在 F-245 复跑——它们要么依赖老档 / 指令 / 抽样这类构造场景，要么是「不该发生」的负向判据。
