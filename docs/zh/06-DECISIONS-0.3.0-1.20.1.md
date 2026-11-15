# 农场风云 v0.3.0（1.20.1）决策记录

> 本文是 1.20.1 分支 v0.3.0 的**唯一真相源**。07 ~ 11 号五份文档一律以本文为准，只引不改；与源码冲突时报给作者，不自行取舍。
> 覆盖：**D-015 ~ D-052，共 38 条**，连续无跳号。增 / 改 / 删登记在 §3，红线在 §4，墓碑在 §5，已否决的方案在 §6，与 1.21.1 主线的口径差异在 §7，遗留登记在 §8。
> 基线：**1.20.1 v0.2.8 已实现态（git HEAD）**。依据三份事实取证（本工作区实读，共 978 行，全部带 `路径:行号`）：`.work/v030/facts-A-numbers.md`（数字面）、`.work/v030/facts-B-api.md`（1.20.1 API 等价面）、`.work/v030/facts-C-anchors.md`（现有代码锚点）。上游口径参考 1.21.1 的 `D:/project_farm/docs/zh/64 ~ 69` 号，只引不改。
> 编号约定：本文**续接本工作区**（`docs/zh/00 ~ 05` 从 001 起号），与 1.21.1 的 D-255~/B-74~/F-329~/Phase 73~ **不续接**；引用 1.21.1 编号时必须写全前缀（如「1.21.1 的 D-281」）。

---

## §0 阅读约定

### 0.1 编号（本工作区续接，从 D-015 起）

| 系列 | 上一份的末号 | 本文 / 下游的起点 | 谁写 |
|---|---|---|---|
| 文档号 | 05 | **06 ~ 11** | 全轮 |
| 决策 D-xx | D-014 | **D-015 ~ D-052**（38 条） | 本文（06 号） |
| 需求 R-x | R7 | **R8** 起 | 07 号 |
| 边界 B-xx | B-12 | **B-13** 起 | 07 号 |
| 任务 F-xx | F-021 | **F-022** 起 | 09 号 |
| Phase / 里程碑 | Phase 6 / M6 | **Phase 7 / M7** 起 | 09 号 |
| 硬约束 HC-xx | HC-010 | **HC-011** 起 | 10 号 |
| 禁止行为 N-xx | N-008 | **N-009** 起 | 10 号 |
| 风险 RT-xx | RT-010 | **RT-011** 起 | 08 号 |
| 裁决 A-xx | A-006 | **A-007** 起 | 09 号 |
| 出口判据 E-x-y | E-1-1 ~ | **E-7-1** 起（形状 `E-<Phase>-<序>`） | 11 号 |

四条纪律：

1. 本文只发 **D-xx（D-015 ~ D-052，共 38 条）**。引用其它系列时写全前缀，不自己编号、不预支下游编号。
2. 出口判据的形状是 `E-<Phase>-<序>`（沿用 04 / 05 号的 `E-1-1` 体例，不是全局流水号）：本版第一个条目由 11 号写成 **E-7-1**。
3. `00 ~ 05` 六份是**移植期**产物（基线 1.21.1 v0.2.4），已固化，本版不改它们一行；本版从 06 号接上。
4. 本工作区的 0.2.5 / 0.2.6 / 0.2.7 / 0.2.8 四个版本只有代码与 `docs/zh/REPORT-0.2.5.md` / `REPORT-0.2.8.md`，没有文档流水线产物，因此本版是 05 号的直接接续。

### 0.2 术语表（判定边界，不是同义词循环）

**控制终端**
：本版新增的方块 `farmstorm:control_terminal`，中文名「控制终端」。判定边界：它**不是**「据点核心」——核心是存储与归属的载体（`OutpostCoreBlockEntity`），终端不存物品、不存归属；它也**不是**「农作物培养器 / 农作物转换器」——那两台是执行机器，终端只负责把核里的东西搬给它们。

**控制面板**
：控制终端右键打开的界面（0 槽界面 + 1 个新 `MenuType`）。判定边界：它**不是**「据点核心的 54 格存储界面」（`OutpostCoreScreen`，只从核心右键打开）；面板里**没有任何槽位**，不显示、也不能搬动核里的物品。

**模式（Mode）**
：控制终端自己的三态 `OFF / CULTIVATOR / CONVERTER`，只存终端方块实体的 NBT。判定边界：它**不是**核的属性（裁定 #4 否决存核侧）；`OFF` 是一个真实状态，不是「两个都为假」的推导结果——枚举里就是三个值。

**投料**
：终端每一轮从核的 54 格里取出**恰好 1 个**物品放进某台空槽机器。判定边界：它**不是**「搬运整摞」、**不是**「把机器产出送回核」（那是机器自己的 `deliver`）、也**不是**「填充任意槽位」（机器只有 1 个投入槽）。

**核储存空间**
：`OutpostCoreBlockEntity.java:67` 的 `SLOT_COUNT = 54` 与 `:139` 的 54 格列表。判定边界：它**不是** 24 个绑定名额（`cultivators` 是名额，54 是格子数，两者不同义）；也**不是**玩家背包的延伸。

**名额**
：核侧 `cultivators` / `cultivatorKinds` 两条平行列表（`:147` / `:154`）的长度上限，读 `outpost_core.cultivatorCapacity`（默认 24，`FarmstormConfig.java:944-946`）。判定边界：控制终端**占一个名额**（裁定 #6），与培养器、转换器同一份名单；名额满时终端既不能绑定、也不投料。

**处理方块（processing_block）**
：`farmstorm:processing_block` 标签的成员，本版 2 → 3。判定边界：标签只承担**发现**（`OutpostCoreBlockEntity.java:891-892` 的 `state.is(...)`）；「这台机器是哪一个类型」仍由 `:123-125` 的 `KIND_BY_BLOCK` 单独决定——按标签发现**不等于**标签里存了类型。

**可挖门槛**
：本版起 `processing_block` 的三个成员都必须「石镐及以上」才能挖出掉落物，由 `#minecraft:mineable/pickaxe`（管挖速）+ `#minecraft:needs_stone_tool`（管是否正确工具）两条标签共同表达。判定边界：**金镐算不算「以上」由原版 `incorrect_for_gold_tool` 决定，本版不改那条标签**，因此本文不写金镐的断言（§8 第 1 条）。

**位置记录**
：`PuffedHayData` 里的 `BlockPos.asLong` 集合，记的是「膨胀麦写进去的那一格干草块」。判定边界：它**不是**「所有干草块」、**不是**「压缩干草块」（那是我方方块 `farmstorm:compressed_hay_block`，由 `CompressedPuffedWheatProjectile.java:34-37` 覆写 `fillBlock()` 写入），也**不是**方块实体（原版干草块没有我方方块实体）。

**全局战利品修饰器（global loot modifier，GLM）**
：1.20.1 的 Forge 在**没有方块掉落事件**的前提下提供的等价拦截面：`IGlobalLootModifier`（`net/minecraftforge/common/loot/IGlobalLootModifier.java:30`）由 `LootTable.java:118` → `ForgeHooks.java:1196-1198` 在开箱时调用。判定边界：它**不是**「取消破坏事件」——方块照旧被移除，被替换的只有掉落表结果；它也**不是**「改所有干草块」——本版只挂 `minecraft:blocks/hay_block` 一张表，且只在坐标命中记录时才把结果换成空表。

**绿色连线**
：控制终端与核心之间那条线，`Kind.TERMINAL` 的渲染后果。判定边界：线**只由 payload 的一个字节决定**，渲染层不按坐标反查方块（`client/CultivatorLinkRenderer.java:100-109` 的 `switch`）；绿色**不是**「培养器的青色」（那是 `0.30 / 0.85 / 0.90`，`:51-53`）。

### 0.3 四字段写法

每条决策固定四项，**缺一不可**，顺序固定：

| 字段 | 写什么 | 不许写什么 |
|---|---|---|
| **结论** | 一句话、祈使句、可直接执行 | 条件句、备选句、「可以考虑」 |
| **依据** | 事实报告的 §编号 + 源码 `文件:行号`；两者都给最好 | 「因为合理」「符合直觉」 |
| **落地位置** | 要改的 `路径` 或 `类#方法` | 代码片段、算法步骤 |
| **补充** | 连带影响 + 否决掉了哪些替代方案 | 实现细节（那是 08 号的事）、任务排期（那是 09 号的事） |

九条写作纪律：

1. **「本文补充，作者可否决」标记**：凡由本文推导、而非作者裁定 / 事实报告直接给出的口径，在该句前标出。带标记的条目可被作者否决；不标的等于转写裁定，否决它等于否决作者。
2. **数字纪律**：所有数值只能追到 §0.4 的基线表、事实报告的实读，或两个已给数值的算术。需要新数值的写「由 08 号定」，本文不自己编。
3. **禁用词**：不出现「您」、TODO、待定、待办、TBD；不写「正常」「手感」「合理」。
4. **表格纪律**：每张表首尾有 `|`，逐行列数一致。
5. **行号纪律**：本文引用的 1.20.1 行号一律取事实报告 B / C 的实读值（那份报告用 `read` 与 `Select-String` 双口径交叉核对过）。
6. **只引上游，不改上游**：发现 `00 ~ 05` 或 1.21.1 主线文档有错，写进 §8 并报给作者，不去改别人的文件。
7. **等价替换必须成文**：1.20.1 缺 API 的地方（§7 表）逐条登记替换方案与代价，不许「实现时再说」。
8. **已知影响必须成文**：玩家能感知的变化写成「不是缺陷，是裁定的后果」（D-028、D-043、D-047、D-049、D-052）。
9. **中文名为基准**（裁定 #10）：首次出现写「中文名（英文 id）」，之后只用中文名。

### 0.4 基线读数（1.20.1 v0.2.8，全部实读）

| 项 | 读数 | 出处 |
|---|---|---|
| 注册面 | **91（11 类）** | `Farmstorm.java:192-195`；`facts-A §1` / `§4` |
| └ items / blocks / block_entity_type | 28 / 28 / 8 | 同上 |
| └ entity_type / mob_effect / sound_event | 13 / 4 / 3 | 同上 |
| └ recipe_type / recipe_serializer / creative_tab / damage_type | 1 / 1 / 1 / 3 | 同上 |
| └ menu_type | 1 | `registry/FarmstormMenus.java:17-18` |
| └ global_loot_modifier_serializer | **0（本版首次引入）** | `facts-B §2` |
| server config / client config | **175 / 3** | `FarmstormConfig` 的 167 + 5 + 3 个 define；`facts-A §5` |
| 语言键（zh_cn / en_us） | **321 / 321**，键集零差异 | `facts-A §3` |
| Java 文件 / 代码行 | **151 / 20,116** | `facts-A §3` |
| 网络包 / 协议串 | **2 / `"1"`** | `FarmstormNetwork.java:32`、`:51` / `:60` |
| 事件监听 | **9** | `event/FarmstormGameEvents.java:67-79` |
| 转换配方 / 合成配方 / 配方 JSON | 21 / 4 / 25 | `facts-A §3` |
| 方块战利品表 / 进度 | 28 / 16 | `facts-A §3` |
| 方块模型 / blockstates / item 模型 | 78 / 28 / 28 | `facts-A §3` |
| 贴图 `.png` / `.mcmeta` / 两者之和 | 84 / 5 / 89 | `facts-A §3` |
| Mixin | 0 | 本文继承 `00` 号红线 1 |

---

## §1 决策清单（D-015 ~ D-052）

本版编号 **D-015 ~ D-052，共 38 条**，连续无跳号。分组：§1.1 版本与范围 5 条、§1.2 控制终端本体 10 条、§1.3 模式与投料 5 条、§1.4 连线与协议 4 条、§1.5 挖掘与标签 3 条、§1.6 史诗准入 3 条、§1.7 干草块掉落 5 条、§1.8 附带与文档 3 条。

### 1.1 版本与范围（D-015 ~ D-019）

#### D-015 · 版本号 0.3.0，基线 = 1.20.1 v0.2.8 已实现态

- **编号**：D-015
- **结论**：把 `mod_version` 从 `0.2.8` 改为 **`0.3.0`**，产物名 `farmstorm-0.3.0+mc1.20.1.jar`；本版交付 **06 ~ 11 六份中文**，不做英译。
- **依据**：作者裁定 #3（与 1.21.1 的 v0.3.0 同号，靠 `+mc` 后缀区分，即 `00` 号 D-004 的同一口径）；`gradle.properties` 现值 `mod_version=0.2.8`；`facts-A §0` 的基线实测。
- **落地位置**：`gradle.properties`（`mod_version`）；`build-1.20.1.cmd` 与 `remap-1.20.1.cmd` 的 jar 名；`docs/zh/06` ~ `docs/zh/11`。
- **补充**：`00` 号 D-004 的「版本号与主线同序列、不改号」在 1.20.1 已经走到 `0.2.8`（四个补丁版），本版**恢复同号**：两边都是 `0.3.0`，安装说明继续按 `+mc1.20.1` / `+mc1.21.1` 各放一份。否决「1.20.1 自走 `0.2.9`」：那会让两个平台的版本号表征不了「功能集等价」这件事。

#### D-016 · 本版范围 = 1.21.1 v0.3.0 四条需求 × 1.20.1 等价实现

- **编号**：D-016
- **结论**：把本版范围钉死为 1.21.1 v0.3.0 的 **4 条需求**（控制终端 / 三个处理方块石镐起 / 史诗 4 条放行 / 膨胀麦干草块掉 0）+ 使它们成立的**等价替换**与连带改动；其余 1.20.1 行为（含 0.2.5 ~ 0.2.8 的四个补丁）一行不改。
- **依据**：作者裁定 #4；1.21.1 的 D-256；`facts-C §1 ~ §8` 的现状锚点。
- **落地位置**：全文；`src/` 的改动面以 §3 三张表为准（表外文件一律不动）。
- **补充**：与 1.21.1 的 64 号 D-256 逐条对应，但**落地机制不完全相同**：1.21.1 用 `BlockDropsEvent`，1.20.1 没有这个事件（`facts-B §1`），所以第 ④ 条改走全局战利品修饰器（D-047）。四条需求与 1.21.1 的对应关系：

| # | 1.21.1 的编号 | 需求 | 本文决策 | 机制是否等价 |
|---|---|---|---|---|
| ① | 1.21.1 D-259 ~ D-277 | 控制终端（新方块 + 面板 + 两模式 + 投料 + 绿色连线 + 材质） | D-020 ~ D-038 | 等价，界面注册与状态同步换机制 |
| ② | 1.21.1 D-278 ~ D-280 | 处理方块可被石镐及以上挖掘并掉落 | D-039 ~ D-041 | 等价，判定链换符号 |
| ③ | 1.21.1 D-281 ~ D-283 | 史诗类作物对应的原版农作物可培养 | D-042 ~ D-044 | 完全相同（同一行删除） |
| ④ | 1.21.1 D-284 ~ D-286 | 膨胀麦产生的干草块破坏后不掉落 | D-045 ~ D-049 | **不等价于实现**：拦截面从事件换成战利品修饰器，玩家可感知行为一致 |
| 附 | 1.21.1 D-255、D-258、D-287 ~ D-289 | 版本号 / 数字面 / 术语 / 检查器 | D-015、D-018、D-050 ~ D-052 | 数字面同值不同构成（§7 第 2 行） |

#### D-017 · 文档规模与回报口径

- **编号**：D-017
- **结论**：六份文档按下限交付——边界 **≥ 16 条**、验收 **≥ 30 条**、陷阱 **≥ 34 条**、差异表 **≥ 12 行**；每稿收尾必须过 §0.3 的九条纪律与「表格 / 围栏 / 禁用词 / 末行」四项自检。
- **依据**：1.21.1 的 D-257（边界 14 / 验收 24 / 陷阱 28 / 差异 9 的下限档）；本版比 1.21.1 多两类机制替换（战利品修饰器、界面注册），故各抬 2 ~ 6 条。**本文补充，作者可否决**。
- **落地位置**：06 ~ 11 号全文。
- **补充**：否决「照 1.21.1 的下限档照抄」：1.20.1 的等价替换本身会引出额外陷阱（GLM 清单漏登记、`SavedData.save` 签名、`MenuScreens` 时序、`ORIGIN` 是 `Vec3`），照抄会让真陷阱漏项。

#### D-018 · 数字面冻结：注册面 91 → 96，语言键 321 → 328

- **编号**：D-018
- **结论**：把六份文档的关键数字冻结为：注册面 **96**（11 类 → 12 类，+5）、server config **175**（不变）、client config **3**（不变）、语言键 **328**（+7）、Java 文件 **159**（+8）、网络包 **2**（不变）、协议串 **"2"**、Mixin **0**、方块战利品表 **29**（+1）、转换配方 **21**（不变）、合成配方 **5**（+1）、配方 JSON **26**（+1）、进度 **16**（不变）、方块模型 **79**（+1）、blockstate **29**（+1）、item 模型 **29**（+1）、贴图 `.png` **88**（+4）、`.mcmeta` **8**（+3）、贴图文件总数 **96**（+7）、事件监听 **9**（不变）、loot modifier 数据文件 **2**（新引入）。
- **依据**：`facts-A §2 ~ §11` 的基线实读 + §0.4 的加法；1.21.1 的 D-258（同一套冻结纪律）。
- **落地位置**：06 ~ 11 号的每一处数字；`tools/verify_project.py` 与 `Farmstorm.java` 的期望值（D-052）。
- **补充**：算式逐条在 08 号 §1 复算。三处与 1.21.1 **同值不同构成**，必须写清否则下游会照抄错口径：① 注册面都是 96，但 1.21.1 是 92 + 4（四类各 +1），本版是 91 + 5（四类各 +1，外加 `global_loot_modifier_serializers` +1）；② 协议串都是「升一位」，但 1.21.1 是 `"3" → "4"`，本版是 `"1" → "2"`；③ 事件监听 1.21.1 是 9 → 10，本版**保持 9**（拦截面换了）。

#### D-019 · 等价替换总纲：缺 API 处逐条替换，玩家可感知行为不变

- **编号**：D-019
- **结论**：1.20.1 缺失的 API 一律**机制级替换**，且替换后**玩家可感知的行为与 1.21.1 等价**；每一处替换在 §7 差异表登记「原机制 / 替换机制 / 代价」，不许静默换语义。
- **依据**：作者裁定 #4；`facts-B` 全篇（8 类缺失面：掉落事件、界面注册事件、`CustomPacketPayload`、`SavedData.Factory`、`DataComponents`、`ContainerData` 先例、`MenuType` 构造、`DirectionPropertyBlock`）。
- **落地位置**：§7 差异表；08 号 §9 的等价实现专节。
- **补充**：三条判据：① 玩家看到的读数不变（掉落数、线的颜色、面板行为）；② 存档与协议的可观察形状可以有差（`SavedData` 方法签名、`SimpleChannel` 的编解码），但**版本串必须升位**；③ 不许「为了少改而留兼容层」（`00` 号红线 4 的同一条纪律）。

### 1.2 控制终端本体（D-020 ~ D-029）

#### D-020 · 方块 id 与中文名

- **编号**：D-020
- **结论**：注册 id 取 `farmstorm:control_terminal`，中文名「控制终端」，英文名 `Control Terminal`。
- **依据**：1.21.1 的 D-259（同一 id 两侧一致）；素材包 `README.txt` 与模型 `control_terminal_a.json` 把 id 钉成 `control_terminal`；裁定 #10。
- **落地位置**：`registry/FarmstormBlocks.java`、`registry/FarmstormItems.java`、`datagen/ModLangProvider.java`。
- **补充**：**注册 id 不带 `_a` 后缀**——素材包的 `_a` 是「A 版」的设计版本号，不是方块名的一部分；贴图文件名保留 `_a_`（与既有 `cultivator_a_top.png` 同体例）。

#### D-021 · 方块属性照铁砧七项

- **编号**：D-021
- **结论**：注册属性取 `strength(5.0F, 1200.0F)` + `mapColor(MapColor.METAL)` + `requiresCorrectToolForDrops()` + `sound(SoundType.ANVIL)` + `pushReaction(PushReaction.BLOCK)`，与培养器 / 转换器同一组。
- **依据**：1.21.1 的 D-260；`facts-B §5`（`BlockBehaviour` 的 `strength` / `sound` / `pushReaction` / `mapColor` / `requiresCorrectToolForDrops` 在 1.20.1 全部存在，行号见该节）。
- **落地位置**：`registry/FarmstormBlocks.java` 的注册处。
- **补充**：铁砧的**行为**不照抄（掉落物损坏、命名、重锤），只取两个数值。`requiresCorrectToolForDrops()` 是第 ② 条需求成立的前提：没有它，「石镐及以上才能挖出掉落物」无处表达。

#### D-022 · 水平朝向，正面朝玩家

- **编号**：D-022
- **结论**：方块带 `HORIZONTAL_FACING` 属性（`HorizontalDirectionalBlock.FACING`），放置时朝玩家，blockstate 恰好 **4 个 variant**，不新增 `working` 之类的工作态属性。
- **依据**：裁定 #5；**1.20.1 的等价类名是 `HorizontalDirectionalBlock`（`facts-B §6`：`DirectionPropertyBlock` 这个类名在 1.20.1 全树 0 命中）**；`CropConverterBlock.java:44` 与 `:67-69` 的既有 `FACING` 先例。
- **落地位置**：`block/ControlTerminalBlock.java`、`datagen/ModBlockStateProvider.java`。
- **补充**：三个动画面**自己播**，与方块状态无关，因此没有 `working` 属性的位置。这与培养器 / 转换器不是同一种做法：它们的动画是「帧条 + 按状态选模型」（`ModBlockStateProvider.java:195-206` 的转换器段、`CultivatorBlock` 的 `WORKING`），本版的动画是「贴图自带 mcmeta」，代码侧 0 行。

#### D-023 · 贴图取素材包的动画三面 + 静态底面，共 7 个手放文件

- **编号**：D-023
- **结论**：`front` / `side` / `top` 三面用素材包的 32×768 纵向帧条 + 同名 `.mcmeta`（24 帧、`frametime` 2），`bottom` 用 32×32 静态图；四张 PNG 与三个 mcmeta 手放进 `src/main/resources/assets/farmstorm/textures/block/`。
- **依据**：裁定 #8；素材包实读在 `C:\Users\DASHpig\Downloads\control_terminal_a_32x32_pack\assets\farmstorm\textures\block\`（三张动画 PNG 4191 / 3851 / 3513 字节、底面 1102 字节、三个 mcmeta 各 322 字节，`editable_frames` 下每面 24 帧）；1.21.1 的 D-262。
- **落地位置**：`src/main/resources/assets/farmstorm/textures/block/` 的 7 个文件。
- **补充**：mcmeta **不由 datagen 产出**（`src/generated` 下 mcmeta 计数为 0），所以是 7 个手放文件。否决「改用 `static_32x32/` 四张静态图 + 工作态切图」：素材包 README 明说这套动画是「循环装饰示意，不代表模组内真实库存、选择状态或操作进度」。

#### D-024 · 标签成员 2 → 3，与 `KIND_BY_BLOCK` 同改

- **编号**：D-024
- **结论**：把控制终端加进 `farmstorm:processing_block`（成员 2 → 3），同时在 `KIND_BY_BLOCK` 里登记 `Link.Kind.TERMINAL`；两处必须在同一个任务里改完。
- **依据**：`facts-C §1`（`OutpostCoreBlockEntity.java:123-125` 现 2 条）与 `§13`（`datagen/ModBlockTagsProvider.java:24-47` 的四条 farmstorm 标签、`:621-622` 的候选过滤）；`util/FarmstormTags.java:30`。
- **落地位置**：`datagen/ModBlockTagsProvider.java`、`blockentity/OutpostCoreBlockEntity.java`。
- **补充**：只改标签 → 终端成为候选，但 `KIND_BY_BLOCK` 取不到值就走到 `kindOf` 的兜底（`facts-C §2`：`:388-394`，兜底返回 `CULTIVATOR`），于是**连线画成青色、投料按培养器处理**——比 1.21.1 的「扫不到」更隐蔽；只改表 → `rebindNearby` 的候选过滤根本扫不到它。两种半边改动都不报错。

#### D-025 · 方块实体实现 `CoreBound` 三方法

- **编号**：D-025
- **结论**：终端的方块实体实现 `CoreBound`（`corePos()` / `setCore(BlockPos)` / `clearCore()`），只做引用缓存，不自建第二份绑定真相。
- **依据**：1.21.1 的 D-264；`CoreBound` 接口与 `CultivatorBlockEntity` / `CropConverterBlockEntity` 两个既有实现（`facts-C §2`、`§5`）。
- **落地位置**：`blockentity/ControlTerminalBlockEntity.java`。
- **补充**：接口**不加第四个方法**。模式状态不是绑定关系的一部分，不进 `CoreBound`——否则接口会从「引用缓存的读写面」变成混装容器。

#### D-026 · 终端占 24 名额之一

- **编号**：D-026
- **结论**：控制终端与培养器、转换器共用核侧那两条平行列表，占 `outpost_core.cultivatorCapacity`（默认 24）里的一个名额。
- **依据**：裁定 #6；`facts-C §1`（`:147` / `:154` 两条列表、`:401` 的 `isCultivatorListFull`）与 `§14`（`FarmstormConfig.java:944-946` 默认 24）。
- **落地位置**：`blockentity/OutpostCoreBlockEntity.java`（复用既有 `addCultivator`，不新增方法）。
- **补充**：名额满时终端**既不能绑定、也不投料**，与另外两台一致。否决「终端单独存一份列表」：容量口径会变成两处，`kindOf` / `hasCultivator` / `pruneCultivators` 三条既有不变量都要复制一遍。

#### D-027 · 绑定范围沿用 3×3 区块

- **编号**：D-027
- **结论**：终端只用 `OutpostCoreBlockEntity.inRange` 这一条范围判据，与培养器 / 转换器完全一致；超范围既不绑定也不投料。
- **依据**：裁定 #9；`facts-C §2`（`:532-534` 实例、`:537-543` 静态、`:89` 的 `RANGE_CHUNKS = 1`）。
- **落地位置**：`block/ControlTerminalBlock.java`（放置绑定的候选筛选）、`blockentity/ControlTerminalBlockEntity.java`（投料前判定）。
- **补充**：否决「不限范围、全世界唯一核即可绑」：那会把 24 名额与 3×3 区块两条既有承诺一起推翻，且连线要跨维度画。

#### D-028 · 右键开面板；未绑定也能开，按钮置灰

- **编号**：D-028
- **结论**：右键始终打开控制面板；未绑定或核侧不认时，状态行显示「未绑定」，两个模式按钮置灰不可点。
- **依据**：裁定 #7；1.21.1 的 D-267；`facts-C §4`（`CultivatorBlock.java:111-129` 的三个 PASS 出口、`CropConverterBlock.java:114-132` 的同形）与 `§9`（`OutpostCoreBlock.java` 的打开写法）。
- **落地位置**：`block/ControlTerminalBlock.java`、`menu/ControlTerminalMenu.java`、`client/ControlTerminalScreen.java`。
- **补充**：这条**明确偏离**机器那套「任一闸不过 = 右键无反应」的口径（`CultivatorBlock.java:125` 的 `!canInsert` 出口）。偏离理由：机器是「往槽里塞东西」，无反应是唯一说得通的拒绝；终端是「打开一个界面」，无反应等于让玩家拿着新方块学不到任何东西。必须写进 07 号的已知影响。

#### D-029 · 合成配方 3×3：四角玻璃 + 四边铁锭 + 正中红石

- **编号**：D-029
- **结论**：给控制终端一条有序 3×3 配方——**四角 `minecraft:glass`、四边 `minecraft:iron_ingot`、正中 `minecraft:redstone`**（图样 `GIG` / `IRI` / `GIG`），产出 1 个，并剥掉配方解锁进度。
- **依据**：裁定 #8；`facts-C §13`（培养器图样 `IGI` / `GRG` / `IGI` 在 `ModRecipeProvider.java:122-124`、转换器 `III` / `GRG` / `III` 在 `:100-102`、剥进度的助手 `recipeWithoutAdvancement` 在 `:174-201`）。
- **落地位置**：`datagen/ModRecipeProvider.java`。
- **补充**：**不能与培养器同图样**——两条 `crafting_shaped` 逐格同料同图样时合成台只稳定命中其中一条。本条的 `GIG` / `IRI` / `GIG` 把四角与四边对调，材料总数不变（4 铁 + 4 玻璃 + 1 红石），与培养器、转换器三者两两不同。

### 1.3 模式与投料（D-030 ~ D-034）

#### D-030 · 模式状态存终端自己，三态枚举，存序列化名

- **编号**：D-030
- **结论**：模式是控制终端方块实体自己的状态，取值恰好三个：`OFF` / `CULTIVATOR` / `CONVERTER`，写进终端自己的 NBT；NBT 里存**序列化名**（`off` / `cultivator` / `converter`）而不是 `ordinal()`。
- **依据**：裁定 #4、裁定 #5；1.21.1 的 D-269；`facts-B §4`（`SavedData` 与 NBT 的 1.20.1 形态）。
- **落地位置**：`blockentity/ControlTerminalBlockEntity.java`（嵌套枚举 + `saveAdditional` / `loadAdditional`）。
- **补充**：否决「存核侧」——两个终端绑同一核时会互相覆盖；否决「用两个 boolean」——那会造出「两个都为真」的第四种状态，而互斥单选（裁定 #4）根本不需要它；否决「存 ordinal」——枚举顺序一改存档含义就变，且 `/data get` 读出来是不可读的数字。

#### D-031 · 互斥、初始关闭、再点已亮的那个退回关闭

- **编号**：D-031
- **结论**：两个按钮互斥；初始态 `OFF`；点未选中的模式切到它，点已选中的模式退回 `OFF`。
- **依据**：裁定 #4、裁定 #5。
- **落地位置**：`menu/ControlTerminalMenu.java`（`clickMenuButton`）、`client/ControlTerminalScreen.java`（两个按钮的亮度）。
- **补充**：`OFF` 时终端不投料、不消费核库存。否决「必须始终有一个模式」：玩家想停下只能拆方块，且刚放下的终端会立刻开始搬空核库存。

#### D-032 · 每 20 刻一轮，每台机器每轮最多投 1 个，节拍写死为常量

- **编号**：D-032
- **结论**：服务端每 20 刻跑一轮投料；一轮里对每台目标机器最多投 1 个；20 刻是代码常量，**不新增 config 键**。
- **依据**：裁定 #5；1.21.1 的 D-271；`facts-C §1`（`OutpostCoreBlockEntity.java:111` 的 `REBIND_INTERVAL_TICKS = 100` 是同类的写死先例）；`facts-A §5`（server config 175 不变）。
- **落地位置**：`blockentity/ControlTerminalBlockEntity.java`。
- **补充**：否决「新增 `control_terminal.intervalTicks` 键」：本版的冻结数字里 server config 是 175，加一个键就要同步检查器的键集口径（脚本从 08 号的表格现算）与四份文档的数字；1.21.1 的 plan 没要求可调。

#### D-033 · 投料准入复用机器自己的闸，终端不自建白名单

- **编号**：D-033
- **结论**：培养模式只投 `CultivatorBlockEntity.admissible` 为真的物品；转换模式只投 `CropConverterBlockEntity` 的 `convertible` 为真的物品；槽非空一律跳过。
- **依据**：裁定 #5；1.21.1 的 D-272；`facts-C §2`（`admissible` 在 `CultivatorBlockEntity.java:143-149`）、`§5`（转换器准入在 `CropConverterBlockEntity.java:152-160`）。
- **落地位置**：`blockentity/ControlTerminalBlockEntity.java`。
- **补充**：判据只有一个来源。1.20.1 的机器侧入口是 `canInsert`（`facts-C §4` 记的三个闸合并出口），终端调它而不是自己拼三个条件。数据包新增一条转换配方，终端的培养模式**自动多收一个投入物**，与培养器同步——这是「判据只有一个来源」的直接后果，写进 07 号的设计要点。

#### D-034 · 面板 = 两个模式按钮 + 一行状态；0 槽；状态走 `ContainerData`

- **编号**：D-034
- **结论**：面板里槽位 **0 个**，只有两个模式按钮与一行状态文字；模式与绑定状态用 **2 格 `ContainerData`**（1.20.1 的 `SimpleContainerData`）由菜单的 `addDataSlots` 自动同步，**不新增第三个网络包**。
- **依据**：裁定 #7；1.21.1 的 D-273；`facts-B §8`（`ContainerData` / `SimpleContainerData` / `addDataSlots` 在 1.20.1 全部存在，本仓**0 先例**）、`§9`（`MenuType` 只有 `(MenuSupplier, FeatureFlagSet)` 一个构造；本仓走 `player.openMenu`）、`facts-C §9`（`OutpostCoreMenu.java:133-135` 的 `stillValid`）。
- **落地位置**：`menu/ControlTerminalMenu.java`、`client/ControlTerminalScreen.java`、`client/FarmstormClientSetup.java`（屏幕注册）。
- **补充**：`quickMoveStack` 直接返回 `ItemStack.EMPTY`（0 槽菜单不会有 `QUICK_MOVE` 点击）；屏幕必须在 `FMLClientSetupEvent` 的 `enqueueWork` 里用 `MenuScreens.register` 注册（1.20.1 没有 `RegisterMenuScreensEvent`，`facts-B §7`；本仓唯一先例在 `FarmstormClientSetup.java:31-34`），否则客户端只有一条 warn、没有界面。否决「面板里嵌 54 格核库存」：那等于第二个核存储界面，点击搬运逻辑要写两遍。

### 1.4 连线与协议（D-035 ~ D-038）

#### D-035 · `Kind.TERMINAL` 的线上码固定为 2，既有两值不重排

- **编号**：D-035
- **结论**：在 `CultivatorLinkPayload.Kind` 里追加 `TERMINAL(2)`；`CULTIVATOR` 与 `CROP_CONVERTER` 的 id 保持 0 与 1 不动。
- **依据**：1.21.1 的 D-274；`facts-B §8`（`Kind` 两值在 `CultivatorLinkPayload.java:73-75`、`id()` 在 `:84-86`、`byId` 在 `:89-96` 且未知码回落 `CULTIVATOR`）。
- **落地位置**：`network/CultivatorLinkPayload.java`。
- **补充**：重排 id 会让旧客户端把「培养器的线」画成「终端的线」——`byId` 按数值反查，线上没有名字。追加是唯一安全的改法。1.20.1 的 `Kind` 是普通枚举 + 手写 `encode/decode`（不是 1.21.1 的 `StreamCodec`），因此「未知码回落」这条兜底照样成立。

#### D-036 · 终端连线取绿色 (0.30, 0.85, 0.30)

- **编号**：D-036
- **结论**：`Kind.TERMINAL` 的三色分量取 `R = 0.30`、`G = 0.85`、`B = 0.30`，`alpha = 1.0`；主线与两端小框共用这一组。
- **依据**：1.21.1 的 D-275；**1.20.1 的既有两组常量在 `client/CultivatorLinkRenderer.java:51-53`（青 `0.30/0.85/0.90`）与 `:59-61`（红 `0.92/0.22/0.22`）**——注意渲染类**不在** `client/render/` 子包下（`facts-C §10`）。
- **落地位置**：`client/CultivatorLinkRenderer.java`。
- **补充**：R 与 G 直接取青的那两个分量，只把 B 从 0.90 压到 0.30——三组颜色的饱和度一致，玩家在草地上不会把绿线看成青线。**本文补充，作者可否决**：若嫌偏暗可整体乘 1.1，但那要到游戏里比对像素，不由本文拍。

#### D-037 · 协议串 `"1"` → `"2"`

- **编号**：D-037
- **结论**：把 `PROTOCOL_VERSION` 从 `"1"` 改为 `"2"`。
- **依据**：1.21.1 的 D-276（同一纪律，不同起点）；`facts-B §8`（`FarmstormNetwork.java:32` 现值 `"1"`、两个 discriminator 在 `:51` / `:60`）；D-035 让 `Kind` 多了一个线上码。
- **落地位置**：`network/FarmstormNetwork.java`。
- **补充**：payload 的字段结构一个字节没变（仍是两个 `BlockPos` + 一个 `Byte`），但旧客户端收到 id = 2 时 `byId` 会回落 `CULTIVATOR`（画成青线）——跨版本的真正屏障是信道版本串（`clientAcceptedVersions / serverAcceptedVersions` 精确匹配，`:40-41`），它会让握手阶段就拒掉跨版本联机。`facts-C §15` 还记着一个既有陈旧注释（说该是 3 而字段是 `"1"`），本版顺手改成「v0.3.0 起为 `"2"`」。

#### D-038 · 渲染器加一个分支，既有两色的三个分量一个字节不动

- **编号**：D-038
- **结论**：`switch (link.kind())` 里追加 `TERMINAL` 分支选绿色；`CULTIVATOR` / `CROP_CONVERTER` 两组常量的数值与配色注释一律不动。
- **依据**：1.21.1 的 D-277；`facts-C §10`（`switch` 在 `client/CultivatorLinkRenderer.java:100-109`，无 `default`）。
- **落地位置**：`client/CultivatorLinkRenderer.java`。
- **补充**：渲染类的四条硬约束（不自建 `RenderType`、不手写顶点、不碰事件的 `PoseStack`、`AFTER_PARTICLES` 阶段）继续有效，本版不重开讨论。无 `default` 的穷尽式 `switch` 是本版唯一一处「漏改立刻编译不过」的地方。

### 1.5 挖掘与标签（D-039 ~ D-041）

#### D-039 · 三个处理方块全部进 `mineable/pickaxe`

- **编号**：D-039
- **结论**：把培养器、转换器、控制终端三个成员全部写进 `#minecraft:mineable/pickaxe`（成员 10 → 12）。
- **依据**：1.21.1 的 D-278；`facts-C §13`（`datagen/ModBlockTagsProvider.java:49-63` 现有 10 个成员、**培养器不在其中**）；`facts-B §5`（`BlockTags.MINEABLE_WITH_PICKAXE` 常量行号）。
- **落地位置**：`datagen/ModBlockTagsProvider.java`。
- **补充**：这条同时修掉一个既有缺口——培养器既有 `requiresCorrectToolForDrops()` 又不在任何 `mineable/*` 里，于是没有任何工具算正确工具，破坏时**不掉落任何东西**。**这不是本版新增的缺陷，是第 ② 条需求要盖住的既有缺陷**，必须写进 07 号的已知影响。1.20.1 的判定链是 `ServerPlayerGameMode.java:249` `canHarvestBlock` → `ForgeHooks.java:226-232`（读 `requiresCorrectToolForDrops`）→ `DiggerItem.java:64-82`（`NEEDS_STONE_TOOL` 的分支在 `:74`）——**与 1.21.1 的 `Tool.isCorrectForDrops` 不是同一套符号**（`facts-B §5`）。

#### D-040 · 三个处理方块全部进 `needs_stone_tool`

- **编号**：D-040
- **结论**：新增一条 `#minecraft:needs_stone_tool` 的合并，成员恰为那三个；木镐因此挖不动（能破坏，但掉落 0）。
- **依据**：1.21.1 的 D-279；`facts-C §13`（`:67` 的 `needs_diamond_tool` 同类写法可作为模板）；`facts-B §5`（`BlockTags.NEEDS_STONE_TOOL` 存在）。
- **落地位置**：`datagen/ModBlockTagsProvider.java`。
- **补充**：两条标签分工不同——`mineable/pickaxe` 管**挖速**，`needs_stone_tool` 管**算不算正确工具**。只加前者，木镐挖得慢但仍然掉；只加后者，挖速还是徒手级。**已知影响**：转换器从「木镐可挖」变成「石镐起」，这是第 ② 条需求的直接后果，不是回归。**判据措辞纪律**：木镐的判据是「掉落 0 个（方块仍被移除）」，不是「破坏不了」——1.20.1 的 `removeBlock` 与 `playerDestroy` 是两个独立调用（`facts-B §5` 的 `:249` / `:253` / `:255`）。

#### D-041 · 金镐算不算「以上」不由本文断言

- **编号**：D-041
- **结论**：本文只保证木镐挖不动（掉落 0）、石镐能挖出掉落物；金镐的行为交给 08 号的验收实测记录，不写成断言。
- **依据**：1.21.1 的 D-280（同一纪律）；`facts-B §5`（1.20.1 走 `TierSortingRegistry`，`DiggerItem` 的 `NEEDS_STONE_TOOL` 分支在 `:74`，金镐的真值本仓读不出）。
- **落地位置**：08 号 §8 的验收条目（只记录）。
- **补充**：这是本文唯一一处「明明能做却故意不写死」的条目。理由：把一条自己没核实的原版行为写进验收，等于给实现方一个必然变红的断言。**本文补充，作者可否决**：若作者要求金镐必须能挖，则改 `needs_stone_tool` 为自定义标签并单独排除金镐，代价是多一条 `farmstorm` 标签。

### 1.6 史诗准入（D-042 ~ D-044）

#### D-042 · 删掉 `CropTier.EPIC` 那道比较，4 条全开

- **编号**：D-042
- **结论**：`admissible` 的判据从「命中转换配方且输出不是史诗」改为「命中转换配方」；火把花、瓶子草、甜浆果、金苹果四个原版物品自此可进培养器。
- **依据**：1.21.1 的 D-281；`facts-C §2`（`CultivatorBlockEntity.java:148` 逐字为 `return output != null && output.tier() != CropTier.EPIC;`）、`§8`（被挡住的正是那 4 条转换配方，21 − 4 = 17 一直可进）。
- **落地位置**：`blockentity/CultivatorBlockEntity.java`。
- **补充**：这条**推翻 1.20.1 沿用的旧口径**（「史诗档唯一的入口是植物学家交易」，1.21.1 侧编号为 D-162，本工作区登记为墓碑 §5.1）。否决「只开 3 条真作物、排除金苹果」：那要在培养器里写一份例外，「判据只有一个来源」当场破坏。

#### D-043 · 判据仍只有一处，禁止旁开白名单

- **编号**：D-043
- **结论**：史诗放行只允许删那道比较；`CultivatorBlockEntity` 里不得出现任何物品白名单，终端侧也不得出现第二份准入判据。
- **依据**：1.21.1 的 D-282；`CropTier.java:25` 的 `EPIC` 与 `CropProfile.java:73-105` 的四条档案（`facts-C §8`）。
- **落地位置**：`blockentity/CultivatorBlockEntity.java`、`blockentity/ControlTerminalBlockEntity.java`。
- **补充**：**已知影响（必须成文）**：4 条史诗作物从「只能靠植物学家交易」变成「可培养量产」。培养器产出是「投入物 × yield + 对应种子 × yield」（`facts-C §2`：`batch` 在 `:261-267`、`INDEPENDENT_SEEDS` 6 条在 `:270-276`、表外即自身在 `:285-288`），因此金苹果、火把花、瓶子草、甜浆果都会**自我复制**（金苹果尤其明显：一个金苹果进、至少两个金苹果当量出）。这不是缺陷，是裁定的后果。

#### D-044 · 产出面不动

- **编号**：D-044
- **结论**：史诗作物的产出批次仍按「投入物 × yield + 对应种子 × yield」两组，不因放行而改倍率、不因放行而加成本。
- **依据**：1.21.1 的 D-283；`facts-C §2`（`batch` 在 `CultivatorBlockEntity.java:261-267`）、`§14`（`cultivator.yield` 默认 2 在 `FarmstormConfig.java:955-957`）。
- **落地位置**：`blockentity/CultivatorBlockEntity.java`（本版不动这一段）。
- **补充**：否决「史诗投入物 yield 折半」之类的补丁：那会在准入闸旁边再造一处按档位分支的逻辑，与 D-043 冲突。平衡问题留给后续版本用配置或配方解决。

### 1.7 干草块掉落（1.20.1 等价实现，D-045 ~ D-049）

#### D-045 · 位置记录用 `SavedData`，键为 `BlockPos.asLong`

- **编号**：D-045
- **结论**：新建 `util/PuffedHayData`，`extends SavedData`，内部用 `LongOpenHashSet` 存 `BlockPos.asLong`，挂 `server.overworld().getDataStorage()`，落盘名 `farmstorm_puffed_hay`。
- **依据**：裁定 #5；1.21.1 的 D-284；**1.20.1 的签名不同**：`SavedData.save(CompoundTag)` 只有一个参数（`facts-B §4`：`SavedData.java:15`），`DimensionDataStorage.computeIfAbsent(Function, Supplier, String)` 是三个参数（`:38`），`ServerLevel.getDataStorage()` 在 `ServerLevel.java:1148`；本仓两个先例 `outpost/OutpostCoreIndex.java:76-79`、`trap/TrapFifoIndex.java:59-60`。
- **落地位置**：`util/PuffedHayData.java`（新增）。
- **补充**：用 `SavedData` 而不是方块实体，因为原版干草块没有我们的方块实体可用。否决「改用自定义方块代替干草块」：那会改动膨胀麦的落点外观与掉落物，越过范围闸。**1.20.1 特有风险**：`computeIfAbsent` 的三个参数顺序容易与 1.21.1 的两参 `SavedData.Factory` 混用，误写成工厂对象会得到编译错或运行期 `ClassCastException`。

#### D-046 · 只在 `setBlock` 真正成功之后登记，且必须再判一次 `Blocks.HAY_BLOCK`

- **编号**：D-046
- **结论**：写入点在 `PuffedWheatProjectile.fillHay` 的循环内、`setBlock` 成功之后，并且登记前再判一次 `hay.is(Blocks.HAY_BLOCK)`；被跳过条件挡掉的格子不记。
- **依据**：1.21.1 的 D-285（并含该条补充的守卫）；`facts-C §6`（`fillHay` 在 `PuffedWheatProjectile.java:104-123`：半径 `edge/2` 在 `:105`、抗性跳过 `:113-115`、白名单跳过 `:116-118`、`setBlock` 在 `:119`）。
- **落地位置**：`entity/thrown/PuffedWheatProjectile.java`。
- **补充**：跳过分支在前、登记在后，因此「本该是空气的格子」「挖不动的格子」不会被记成「该不掉落」。`hay.is(Blocks.HAY_BLOCK)` 这道守卫在 1.20.1 比 1.21.1 **更硬性**：压缩膨胀麦通过覆写 `fillBlock()`（`CompressedPuffedWheatProjectile.java:34-37`）复用同一个 `fillHay`，不加守卫会把 `farmstorm:compressed_hay_block` 也登记进去。**已知影响**：老存档里已经存在的膨胀麦干草块**不在记录里**，照旧掉落——本版不回溯扫描。

#### D-047 · 消费点 = 限定 `minecraft:blocks/hay_block` 的全局战利品修饰器（1.20.1 无方块掉落事件）

- **编号**：D-047
- **结论**：新建 `loot/PuffedHayLootModifier`（`extends LootModifier`）与 `registry/FarmstormLootModifiers`（`DeferredRegister<Codec<? extends IGlobalLootModifier>>` 挂在 `ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS` 上），用 Forge 自带的条件类型 `forge:loot_table_id`（`net.minecraftforge.common.loot.LootTableIdCondition`，注册在 `ForgeMod.java:663`，JSON 字段 `loot_table_id`）把作用面钉到 `minecraft:blocks/hay_block` 一张表；开箱时用 `LootContextParams.ORIGIN`（**1.20.1 是 `Vec3`**）换成坐标查记录，命中就把整表结果替换为空表并移除该条记录。
- **依据**：裁定 #5；`facts-B §1`（`BlockEvent` 的 12 个嵌套类里**没有** `HarvestDropsEvent` / `BlockDropsEvent`，全树 0 命中）、`§2`（`IGlobalLootModifier` 抽象面在 `:30` / `:79` / `:84`；`LootModifier` 只留 `doApply` 在 `:24` / `:69`；`LootModifierManager` 在 `:38`；注册表键类型与 `DeferredRegister` 范本在 `ForgeRegistries.java:112` / `:187`；`LootContextParams.ORIGIN` 是 `LootContextParam<Vec3>` 在 `LootContextParams.java:18`；`apply` 的返回值整表替换在 `ForgeHooks.java:1196-1198` ← `LootTable.java:118`）。
- **落地位置**：`loot/PuffedHayLootModifier.java`（新增）、`registry/FarmstormLootModifiers.java`（新增）。
- **补充**：这是本版**唯一一处与 1.21.1 机制不同的替换**，代价明码标价：注册面多 1 项（`global_loot_modifier_serializers` 0 → 1），Java 文件多 2 个。否决 `BlockEvent.BreakEvent` + 手工 `destroyBlock`：那会与其它 mod 的 BreakEvent 处理器互相干扰，且方块移除路径要自己重写一遍。否决「改所有干草块」：那会连整合包里别人的干草块一起改，越过裁定 #5 的「只限膨胀麦落地的原版干草块」。

#### D-048 · 掉落清空 = 返回空表，不是取消事件

- **编号**：D-048
- **结论**：命中记录时 `apply` 返回**空的掉落表**（整表替换），不使用任何「取消破坏」的语义。
- **依据**：1.21.1 的 D-286（那边用 `getDrops().clear()` 而不是 `setCanceled(true)`）；`facts-B §6`（`ForgeHooks.java:1198` 的 `generatedLoot = mod.apply(...)` 是整表替换，唯一入口 `LootTable.java:118`）。
- **落地位置**：`loot/PuffedHayLootModifier.java`。
- **补充**：方块照旧被移除、经验照旧结算（干草块本来就是 0 经验），被替换的只有掉落物列表——这与 1.21.1 的 `getDrops().clear()` 语义对齐。创造模式挖掉不触发战利品表，于是记录不会被消费，这是可接受的：创造模式本来就不掉落（与 1.21.1 的后果一致）。

#### D-049 · 数据文件两份，且由 datagen 产出

- **编号**：D-049
- **结论**：新增 `datagen/ModGlobalLootModifierProvider`（`extends GlobalLootModifierProvider`），由它产出**两份**数据文件：`data/farmstorm/loot_modifiers/puffed_hay.json`（修饰器本体）与 `data/forge/loot_modifiers/global_loot_modifiers.json`（清单，命名空间固定 `forge`）；两份都不许手写。
- **依据**：裁定 #5；`facts-B §2`（数据文件是「两份」：`LootModifierManager.java:53` 的清单命名空间固定 `forge`、`:55` 的分层读取、`:60-68` 的 `replace` / `entries`、`:75` 的注释「不在清单里的修饰器不生效」；`GlobalLootModifierProvider` 在 `:35` 的抽象 `start`、`:95` 的 `add`）。
- **落地位置**：`datagen/ModGlobalLootModifierProvider.java`（新增）、`GatherDataEvent` 的 Provider 注册处。
- **补充**：**清单漏登记 = 修饰器静默不生效**，这是本版最隐蔽的一处失效（没有日志、没有编译错）。否决「手写这两个 JSON 到 `src/generated`」：那违反既有 datagen 纪律（`00` 号红线同源），下次 `runData` 会把改动冲掉。**本文补充，作者可否决**：若作者接受手放，可省一个 Java 文件（Java 159 → 158）。

### 1.8 附带与文档（D-050 ~ D-052）

#### D-050 · `man瓜` 一个字都不动

- **编号**：D-050
- **结论**：`pumpkin_turret` 的中文名保持 `man瓜`；本版不改 `datagen/ModLangProvider.java` 的 `:80` / `:114` / `:123` / `:199` / `:213` 五处中的任何一处。
- **依据**：`facts-C §13`（五处行号实读）；1.21.1 的 D-287（作者原话：这个农作物的名称就叫 man瓜）。
- **落地位置**：`datagen/ModLangProvider.java`（本版不动）。
- **补充**：六份文档一律不得把它写成缺陷、不得在验收里断言它是「南瓜炮台」。

#### D-051 · 术语：中文名（英文 id）首次并列，之后只用中文名

- **编号**：D-051
- **结论**：每个方块 / 物品 / 作物第一次出现时写「中文名（英文 id）」，同一份文档内之后只用中文名；原始名以语言文件为准。
- **依据**：裁定 #10；`src/generated/resources/assets/farmstorm/lang/zh_cn.json` 的逐键实测（如 `item.farmstorm.awakened_pitcher_plant` = 激活的瓶子草）。
- **落地位置**：06 ~ 11 号全文。
- **补充**：瓶子草不写成「猪笼草」、「激活的」不写成「觉醒的」——这两处 1.21.1 侧被作者当场纠正过，本版按语言文件逐字引用。

#### D-052 · 注册面断言有两处，必须同批收到 96 / 12 类

- **编号**：D-052
- **结论**：注册面断言有**两处**——`tools/verify_project.py` 的 `EXPECTED_COUNTS` 与 `Farmstorm.java` 的 `logSelfCheck`；两处必须与五处注册改动同批收口到 **96（12 类）**，并同步 ID 清单与算式注释。
- **依据**：1.21.1 的 D-289 与 A-119；`facts-A §1`（`Farmstorm.java:192-195` 断言 `total == 91` 与 11 类逐类值）、`§2`（`verify_project.py:458-471` 的 `EXPECTED_COUNTS` 是 **12 类、相加 92**，且含一个 1.20.1 根本不存在的 `data_component_type`）。
- **落地位置**：`tools/verify_project.py`、`Farmstorm.java`。
- **补充**：**本版必须顺手修掉一个既有缺陷**：检查器脚本的 `EXPECTED_COUNTS` 有 12 类 92（含 `data_component_type: 1` 与 `EXPECTED["data_component_type"] = ["temporary_enchantments"]`），而运行期是 11 类 91，两处从移植期起就不同口径；`temporary_enchantments` 这个字面量在 1.20.1 全 src 0 命中（`facts-A §3 ~ §5`）⇒ 脚本当前必然打一条 FAIL。本版把该类的槽位换成 `global_loot_modifier_serializers`（D-047 引入的 1 项），于是两边重新对齐为 **12 类 96**。否决「只改 Java 不动脚本」：那会让「日志绿、脚本红」的既有错位固化。

---

## §2 作者裁定索引（裁定 # → 决策号）

本轮访谈取到 **10 条裁定**，逐条落到决策号。

| # | 一句话 | 决策号 |
|---|---|---|
| #1 | 新六份编号 06 ~ 11、带 `0.3.0` 与 `-1.20.1` 后缀 | D-015 |
| #2 | 各编号系列全部续接本工作区（D-015 / B-13 / F-022 / Phase 7 / HC-011 / N-009 / RT-011 / A-007 / E-7-1） | §0.1 全表 |
| #3 | 版本号 0.3.0 | D-015 |
| #4 | 四条需求全量对齐 + 允许机制级等价替换并逐条登记 | D-016、D-019、D-030、D-031 |
| #5 | 干草块那条改用「限定 `hay_block` 表的全局战利品修饰器」 | D-032、D-033、D-045 ~ D-049 |
| #6 | 冻结整张数字表并按 1.20.1 实读重算 | D-018、D-026 |
| #7 | 基线 = 1.20.1 v0.2.8，并登记与 1.21.1 v0.2.7 的差异 | D-016、§7 |
| #8 | 材质按规格冻结（24 帧 / 3 面 + 1 静态 / 正面朝北） | D-023、D-029 |
| #9 | 只写六份文档、不改源码与检查器；新代码只用 `net.minecraftforge.*` | D-052、§4 红线 6 |
| #10 | 术语以中文名为基准、`man瓜` 不动 | D-050、D-051 |

覆盖核对：10 条裁定全部有决策号，无一条悬空；38 条决策里 **11 条**由裁定直接给出（D-015、D-016、D-023、D-026、D-028、D-030、D-031、D-032、D-047、D-050、D-051），其余 27 条由 1.21.1 口径、事实报告或本文推导，两条线互不冲突。

---

## §3 对上一版（1.20.1 v0.2.8）的增 / 改 / 删

### 3.1 增（10 项）

| # | 项 | 本版 | 决策号 |
|---|---|---|---|
| 1 | 方块 `farmstorm:control_terminal` | 新增 | D-020、D-021、D-024 |
| 2 | 物品 `farmstorm:control_terminal`（BlockItem） | 新增 | D-020 |
| 3 | 方块实体类型 `control_terminal` | 新增（8 → 9） | D-025 |
| 4 | 菜单类型 `control_terminal` | 新增（1 → 2） | D-034 |
| 5 | 面板屏幕 | 新增 | D-034 |
| 6 | `util/PuffedHayData` | 新增 | D-045 |
| 7 | `loot/PuffedHayLootModifier` + `registry/FarmstormLootModifiers` | 新增 | D-047 |
| 8 | `datagen/ModGlobalLootModifierProvider` + 2 份数据文件 | 新增 | D-049 |
| 9 | 合成配方 `control_terminal` | 新增（4 → 5） | D-029 |
| 10 | `#minecraft:needs_stone_tool` 合并 | 新增（3 个成员） | D-040 |

### 3.2 改（13 项）

| # | 项 | 上一版 | 本版 | 决策号 |
|---|---|---|---|---|
| 1 | `processing_block` 成员数 | 2 | 3 | D-024 |
| 2 | `KIND_BY_BLOCK` 条目数 | 2 | 3 | D-024 |
| 3 | `mineable/pickaxe` 成员数 | 10 | 12 | D-039 |
| 4 | `Link.Kind` 值数 | 2 | 3 | D-035 |
| 5 | 协议串 | `"1"` | `"2"` | D-037 |
| 6 | 培养器准入闸 | 排除史诗输出 | 不再排除 | D-042 |
| 7 | 膨胀麦落点登记 | 无 | 每次 `setBlock` 成功后记一条 | D-046 |
| 8 | 注册面 | 91（11 类） | 96（12 类） | D-018 |
| 9 | 检查器 `EXPECTED_COUNTS` | 12 类 / 92（含已不存在的 `data_component_type`） | 12 类 / 96（含 `global_loot_modifier_serializers`） | D-052 |
| 10 | 版本串 | `0.2.8` | `0.3.0` | D-015 |
| 11 | 语言键 | 321 | 328 | D-018 |
| 12 | Java 文件 | 151 | 159 | D-018 |
| 13 | 方块战利品表 | 28 | 29 | D-018 |

### 3.3 删（1 项）

| # | 项 | 上一版 | 本版 | 决策号 |
|---|---|---|---|---|
| 1 | 培养器准入闸里的档位比较 | `output.tier() != CropTier.EPIC` | 整条比较删除 | D-042 |

除上表之外，v0.2.8 的行为一行不改。特别声明四条**不清算**：培养器与转换器仍然无菜单（`00` 号 D-160 / D-208 的 1.21.1 口径继续有效）；据点核心仍是 54 格存储且不改容量（`FarmstormConfig.java:939-941` 钉死 54 ~ 54）；`man瓜` 仍是 `man瓜`（D-050）；24 个绑定名额的上限值仍是配置项 `outpost_core.cultivatorCapacity`（`:944-946`）。

---

## §4 红线（7 条）

1. **`KIND_BY_BLOCK` 与标签必须同改**（`OutpostCoreBlockEntity.java:123-125` + `datagen/ModBlockTagsProvider.java`）。只改一处不会报错：前者漏 = 终端被当成培养器（`kindOf` 兜底），后者漏 = 终端能绑定却扫不到。
2. **既有一二两个 `Kind` 的线上码不许重排**：`CULTIVATOR` / `CROP_CONVERTER` 的 id 保持 0 / 1，`TERMINAL` 只能追加为 2。
3. **培养器的准入判据只有一处**：史诗放行只能删那道比较，不许在培养器、终端或方块侧另写白名单。
4. **`man瓜` 一个字都不许动**（D-050）。它不是缺陷、不进差异表、不进验收断言。
5. **175 个 config 键一个不增**：终端节拍是常量（`20`），不是配置键。
6. **不留兼容层、不做英译、不改 1.21.1 主线**：命名空间仍是 `net.minecraftforge.*`（本仓 `grep net.neoforged` = 0，`facts-A §1` 同一纪律），六份文档只写中文，`D:/project_farm` 只读。
7. **不得新增第三个网络包**：面板状态走 `ContainerData`（D-034），连线走既有 payload（D-035）。本版唯一的「新增拦截面」是战利品修饰器，它不占网络包。

---

## §5 墓碑（被本版推翻 / 不可用的旧口径）

### 5.1 旧口径「史诗档唯一的入口是植物学家交易」—— 已推翻

| 项 | 内容 |
|---|---|
| 原文 | 「史诗：4 条，唯一的入口是植物学家交易（把它们挡在培养器外）」 |
| 原文位置 | 1.21.1 的 52 号 D-162；本工作区对应的是 `util/CropTier.java:25` 的类注释与 `CropProfile.java:73-105` 的四条档案 |
| 推翻者 | D-042 |
| 推翻理由 | 1.21.1 的 plan 第 ③ 条明确要求「史诗类作物的对应原版农作物现在可以在培养器中培养」 |
| 处置 | 旧口径作废，**不删旧文档**（只引不改上游）。`CropTier.java` 那句类注释在本版实现时必须同步改写，改后的措辞由 08 号定 |

### 5.2 「用 `BlockDropsEvent` 清空干草块掉落」—— 本平台不可用

| 项 | 内容 |
|---|---|
| 原文 | 1.21.1 的 D-286：「在 `event/FarmstormGameEvents` 里加第 10 个监听，处理 `BlockDropsEvent`，`getDrops().clear()`」 |
| 为什么不可用 | 1.20.1 的 `BlockEvent` 只有 12 个嵌套类，没有 `HarvestDropsEvent` / `BlockDropsEvent`；全树 glob 与文本 grep 均 0 命中（`facts-B §1`） |
| 替代者 | D-047 ~ D-049（限定 `hay_block` 表的全局战利品修饰器 + 空表替换） |
| 处置 | 机制替换登记进 §7 第 1 行；玩家可感知行为不变（掉落 0、方块仍被移除、经验不受影响） |

---

## §6 已否决的方案

| # | 方案 | 否决理由 | 决策号 |
|---|---|---|---|
| 1 | 模式状态存据点核心 | 两个终端绑同一核时互相覆盖；核侧多一个 NBT 键 | D-030 |
| 2 | 模式用两个 boolean 表示 | 会造出「两个都为真」的第四种状态，而互斥单选不需要它 | D-030 |
| 3 | 模式存 `ordinal()` | 枚举顺序一改存档含义就变；`/data get` 读不出可读值 | D-030 |
| 4 | 默认进培养模式 / 没有 `OFF` 态 | 刚放下的终端会立刻搬空核库存；想停只能拆方块 | D-031 |
| 5 | 终端不占 24 名额，另存一份列表 | 容量口径变两处，`kindOf` / `hasCultivator` / `pruneCultivators` 三条不变量都要复制 | D-026 |
| 6 | 新增第三个 S2C 网络包同步模式 | 两格状态用 `ContainerData` 就够；多一个包要多一次协议升位 | D-034 |
| 7 | 面板里嵌核的 54 格 | 等于第二个核存储界面，点击搬运逻辑写两遍 | D-034 |
| 8 | 终端不限绑定范围 | 推翻 24 名额与 3×3 区块两条既有承诺，连线要跨维度画 | D-027 |
| 9 | 干草块改用自定义方块 | 改掉膨胀麦的落点外观与掉落物，越过范围闸 | D-045 |
| 10 | 所有干草块都不掉 | 会改原版行为，整合包里别的东西也受影响 | D-047 |
| 11 | `BlockEvent.BreakEvent` + 手工 `destroyBlock` | 与第三方 BreakEvent 处理器互相干扰，方块移除路径要自己重写 | D-047 |
| 12 | 手写两份 loot modifier JSON | 违反 datagen 纪律，下次 `runData` 冲掉 | D-049 |
| 13 | 在培养器里写「史诗白名单」按物品列 | 判据变成两处，数据包加配方时两边不同步 | D-043 |
| 14 | 史诗投入物的 yield 折半 | 在准入闸旁边再造一处按档位分支的逻辑 | D-044 |
| 15 | 改用 `static_32x32` 静态图 + 工作态切图 | 素材包 README 明说动画是循环装饰示意，与状态无关 | D-023 |
| 16 | 终端不画线 | 等于否决 1.21.1 的 plan 第 ① 条的「会有绿色连线」 | D-035 |
| 17 | 新增 `control_terminal.intervalTicks` 配置键 | 本版冻结 server config 175；plan 没要求可调 | D-032 |
| 18 | 把 `man瓜` 修成「南瓜炮台」 | 作者已裁定它是正名 | D-050 |
| 19 | 把金镐写成断言 | 本仓读不出金镐在 `TierSortingRegistry` 里的真值，写成断言必红 | D-041 |
| 20 | 只改 Java 自检、不改检查器脚本 | 「日志绿、脚本红」的既有错位会被固化 | D-052 |

---

## §7 与 1.21.1 v0.3.0（64 ~ 69 号）的口径差异

下列 13 处不是「措辞差异」，而是**实现面必须不同**。**一律按本表实现**，不改 1.21.1 的文档，也不照它的措辞写 1.20.1 的行为描述。

| # | 1.21.1 的做法 | 1.20.1 的做法 | 差异性质 / 代价 | 决策号 |
|---|---|---|---|---|
| 1 | 第 ④ 条用 `BlockDropsEvent` + `getDrops().clear()` | 限定 `minecraft:blocks/hay_block` 的全局战利品修饰器 + 空表替换 | **机制替换**；注册面 +1、Java +2、数据文件 +2 | D-047 ~ D-049 |
| 2 | 注册面 92 → 96（+4），12 类不变 | 注册面 91 → 96（+5），11 类 → 12 类 | 同值不同构成（多了 GLM 序列化器一类） | D-018 |
| 3 | 协议串 `"3"` → `"4"` | 协议串 `"1"` → `"2"` | 起点不同；跨版本互斥的语义一致 | D-037 |
| 4 | `RegisterMenuScreensEvent` 注册屏幕 | `FMLClientSetupEvent` 的 `enqueueWork` 里 `MenuScreens.register` | **机制替换**；时序纪律（客户端专属） | D-034 |
| 5 | `ContainerData` + `addDataSlots`（项目已有先例） | 同一套 API 存在，但**本仓 0 先例** | 无机制差异，多一条「首次使用」陷阱 | D-034 |
| 6 | `SavedData.Factory` + `save(CompoundTag, HolderLookup.Provider)` | `computeIfAbsent(Function, Supplier, String)` + `save(CompoundTag)` | **签名替换**；三参数顺序是新增陷阱 | D-045 |
| 7 | `LootContextParams.ORIGIN` 不涉及 | `ORIGIN` 是 `Vec3`，必须转 `BlockPos`（`BlockPos.containing`） | 新增类型转换点 | D-047 |
| 8 | 事件监听 9 → 10 | **保持 9**（拦截面换成战利品修饰器） | 数字不同，必须写清否则下游照抄 10 | D-047 |
| 9 | Java 文件 151 → 156（+5） | 151 → 159（+8） | 多 3 个：GLM 类 + 注册类 + datagen provider | D-018、D-049 |
| 10 | 数据文件：glm 无（0 新增） | loot modifier 2 份（修饰器 + `forge` 命名空间清单） | 新增冻结数字 2 | D-049 |
| 11 | 检查器 `EXPECTED_COUNTS` 12 类 92 → 96，与运行期一致 | 检查器 12 类 92（含已不存在的 `data_component_type`）vs 运行期 11 类 91 —— **既有错位**，本版一次修掉 | 顺带修缺陷 | D-052 |
| 12 | 挖掘判定走 `Tool.isCorrectForDrops`（组件体系） | 判定走 `ForgeHooks` + `DiggerItem` + `TierSortingRegistry`（`Tier` 体系，1.20.1 无 `DataComponents`） | 同一行为，不同符号链 | D-039、D-040 |
| 13 | 方块类可覆写 `getShape` | 本仓培养器与转换器**都没有**覆写 `getShape`；终端也不覆写，保持同形 | 形状随方块本体 | D-022 |

---

## §8 遗留登记（本版没定的事 + 归属）

| # | 事项 | 现状 | 归属 |
|---|---|---|---|
| 1 | 金镐算不算「石镐及以上」 | 本仓读不出 `TierSortingRegistry` 对金镐的真值 | 08 号验收实测（D-041） |
| 2 | 素材包的三张动画 PNG 与三个 mcmeta 的逐字节落地 | 素材在 `C:\Users\DASHpig\Downloads\control_terminal_a_32x32_pack\`，未被复制进仓 | 09 号任务、11 号的出口判据 |
| 3 | 24 帧 / 32×768 的动画贴图 | 项目现有 5 个 mcmeta 全是 16 帧 / 32×512，无先例 | 08 号的验收里加一条「进游戏数帧」 |
| 4 | 老存档里已存在的膨胀麦干草块 | 不在记录集合里，照旧掉落；本版不回溯扫描 | 07 号的已知影响 |
| 5 | `verify_project.py` 的 config 期望「从文档现算」 | 脚本读 `docs/zh` 的表格得出 175 键；本版不改 config，但改了文档结构 | 08 号落笔前先跑一次现状读数 |
| 6 | 培养器与转换器仍然无菜单 | `00` 号引用的 1.21.1 D-160 / D-208 继续有效，本版不清算 | 无（保持） |
| 7 | `ModLootTableProvider` 类注释写 27 张、实测 28 张 | 既有陈旧注释，本版加 1 张后变 29 | 08 号 §2.5 的连带注释清单（D-018） |
| 8 | `FarmstormItems` 注释写「34 个物品」、`FarmstormBlocks` 写「27 个方块」、`Farmstorm` 写「12 类 / 98 / 92」、`Botanist*` 写「176 不变」、`FarmstormClientConfig` 写「166 键」 | 全部与实读不符（`facts-A §0`、`facts-C §15`） | 08 号 §2.5 连带注释清单 |

以上 8 条全部有归属，无一条「由后定」而没有接手的文档。

---

> 本文自检：38 条决策连续无跳号（D-015 ~ D-052）；10 条裁定各有落点；增 10 / 改 13 / 删 1；红线 7 条；墓碑 2 条；否决 20 条；与 1.21.1 的差异 13 行；遗留 8 条且各有归属；基线读数 16 项全部可追到 `.work/v030/facts-*` 的实读。
