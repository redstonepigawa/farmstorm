# 58 号 v0.2.4 决策记录

> 项目 / 版本 0.2.4 / 基线 0.2.3 已实现态（git `52749b9`）/ 依据（`plan0.2.4.txt` 4 条 + `.work/v024-BRIEF.md` §2 的 7 条裁定 + 写作期追加的裁定 #8）/ 唯一真相源
> 依据补充：`plan0.2.4.txt` 4 条需求的原文位置——`:6-9` 转换器绑定、`:10-13` 村民交易经验、`:14-17` 核心浮窗、`:18-21` 放置音效；`.work/v024-BRIEF.md`（下称 `BRIEF`）§2 的 **7 条**作者裁定，加上写作期由作者追加的裁定 #8（**8 条**逐条转写在本文 §2）。
> 地位：**唯一真相源**。v0.2.4 的一切口径以本文为准；下游 **59（设计）/ 60（技术）/ 61（任务）/ 62（主提示词）/ 63（阶段提示词）只引本文，不自己发明**。本文与源码冲突时报给作者（一律写「作者」），不自行取舍。
> 覆盖：**D-225 ~ D-254，共 30 条**，连续无跳号。对 0.2.3 的增 / 改 / 删登记在 §3，红线在 §4，墓碑在 §5，已否决的方案在 §6，与 plan 的口径差异在 §7，遗留登记在 §8。

---

## 0. 阅读约定

### 0.1 编号（D-225 起；接续表）

本文的决策编号从 **D-225** 起（0.2.3 的 52 号到 D-224），发到 **D-254**；下一版的 D 系列从 **D-255** 起。其余编号系列本文**只引用、不新开**：

| 系列 | 0.2.3 到 | **本文从** | 由谁写 |
|---|---|---|---|
| 文档号 | 57 | **58** | 本文 |
| 决策 D-xx | D-224 | **D-225 ~ D-254** | 本文（58 号） |
| 边界 B-xx | B-58 | **B-59** | 59 号 |
| 任务 F-xx | F-311 | **F-312** | 61 号 |
| Phase / 里程碑 | 65 / M61 | **66 / M62** | 61 号 |
| 硬约束 HC-xx | HC-172 | **HC-173** | 62 号 |
| 禁止行为 N-xx | N-154 | **N-155** | 62 号 |
| 风险 RT-xx | RT-82 | **RT-83** | 60 号 |
| 裁决 A-xx | A-101 | **A-102** | 61 号 |
| 验收证据 E 系列 | E-61 段收尾 | **E-66-1 起** | 63 号 |

四条编号规则：

1. 本文只发 **D-xx（D-225 ~ D-254，共 30 条）**。引用其它系列时写全前缀，不自己编号、不预支下游编号。
2. 0.2.3 的 D-203 ~ D-224 仍以 52 号为准，**除非 §5 点名**；§5 没点到的一律继续有效。
3. 本文**不新立裁决**：`BRIEF` §2 的 7 条（#1 ~ #7）是作者拍板的，下游不得改写、只能引用；需要作者补充拍板的新条目由 61 号从 **A-102** 起登记；写作期由作者追加的裁定 #8 同样只在 §2 做映射，不新开裁决号。
4. **`BRIEF` §2 的 #3 与 #4 是两条独立裁定**：前者定「30% 读作不透明度」，后者定「硬编码、不新增 config 键」。下游引用必须分开写，不许合并成「浮窗背景做成可配」。

E 系列的形状是 `E-<Phase>-<序>`（不是全局流水号），所以它的起点跟随 Phase 号：下一段的第一个条目由 63 号写成 **E-66-1**。**本文补充，作者可否决**。

### 0.2 术语表

本节给每个术语**判定边界**，不是同义词循环。下游五份文档遇到这些词，一律按本节的边界用。

**核心浮窗**
：`com.dashpig.farmstorm.client.CoreHudRenderer` 负责的那块 HUD 叠加层——订阅 `RenderGuiEvent.Post`（`CoreHudRenderer.java:27`、`:52`），在任何 `Screen` 之外也会画（`:61-63`）。判定边界：它**不是** `OutpostCoreScreen`——后者 `extends AbstractContainerScreen`（`OutpostCoreScreen.java:21`），是据点核心的 **54 格容器界面**，只在玩家右键核心时出现（`OutpostCoreBlock.java:134-141`）；它也**不是** `CoreHudDragScreen`（只在拖拽态存在的编辑用 `Screen`）。本版的「背景」只指 `CoreHudRenderer.java:77-78` 那两条 `fill` 的**内层底色**。写错文件名会让下游改到容器界面，红线 5 兜这一条。

**绑定**
：核侧两条同长同下标的平行列表（`OutpostCoreBlockEntity.java:132` 的 `cultivators`、`:139` 的 `cultivatorKinds`）记录的「机器 ← 核」关系。判定边界：真相**只在核侧**（D-165 继续有效），机器侧的 `corePos` 是**引用缓存**；「绑定」**不是**「归属」（`ownerUuid` / `ownerName` 那一套），**不是**「红色连线」（连线由 `CultivatorLinkPayload` 下发、由渲染层画）。

**标签驱动**
：候选发现只问「该格是不是 `farmstorm:processing_block` 的成员」，不问「该方块实体是不是某个类」。依据链：标签常量 `FarmstormTags.java:30`，成员由 `ModBlockTagsProvider.java:45-47` 生成，产物 `processing_block.json:2-5` 恰 2 项。判定边界：标签只承担**发现**，`Link.Kind` 的取值仍要单独确定（D-234、D-235）——「按标签发现」**不等于**「标签里存了类型」。

**三条绑定路径 + 一个清理出口**
：放置绑定 `setPlacedBy`（`CultivatorBlock.java:95-102`、`CropConverterBlock.java:99-106`）、补绑 `rebindNearby`（`OutpostCoreBlockEntity.java:604-649`）、迁移改绑（`settleOnRemoval` 的出口①，`:845-857`）、清理出口②（`:866-873`）。判定边界：**前三条是「加绑定」，第四条是「解绑定」**——本 bug 的正面缺口只在第四条（出口②漏了转换器），前三条是「让同类逻辑看起来统一」的手段。

**补绑**
：`OutpostCoreBlockEntity#rebindNearby` 每隔 `REBIND_INTERVAL_TICKS` 扫一圈，把未绑定的机器挂到本核上。判定边界：**不是**放置那一刻的绑定；它只处理「机器侧缓存为空」的候选（`:630-632`），所以缓存里残留一个死核坐标时就永久拒绝——这是需求 1 的病因，不是另外一件事。

**逐项携带（xp）**
：把成交经验做成 record 的**分量**，每个 listing 实例自带自己的值（D-237）。判定边界：它**不是**「按 `CropTier` 查表」——等级与档位不同轴，见下一条；也**不是**「类常量 + 调用点覆盖」。

**交易等级**
：村民的 1 ~ 5 级，即 `event.getTrades().get(n)` 的下标（`VillagerTradeEvents.java:44-47`、`:55-57`），由**注册处**决定。判定边界：它**不是** `CropTier`（`COMMON` / `RARE` / `EPIC` 是作物档位）：植物学家 1 ~ 4 级全是 `NORMAL` 的切片、5 级挂史诗；农民 1 / 3 / 5 级各一项、2 / 4 级为空。两者的换算只写在 `VillagerTradeEvents`。

**输入物（客户端判据）**
：客户端能凭手上那一件物品直接判定「这次右键会被机器收走」的谓词——转换器是 `convertible(stack)`（`CropConverterBlockEntity.java:129-137`，查 `farmstorm:converting` 配方表），培养器是 `admissible(stack)`（`CultivatorBlockEntity.java:164`，经 `admissible` `:143-149`）。判定边界：它**不是** `canInsert`（后者含 `isUsable()`，而 `core()` 在客户端要求 `ServerLevel`，恒假）；也**不是**「槽里有货」那一半条件。

**客户端判据 vs 服务端裁决**
：同一动作在两侧的判定面不同——客户端只看物品品类（上一条），服务端还看范围、绑定与空槽（`CropConverterBlockEntity.java:118-120`、`CultivatorBlockEntity.java:152-154`）。判定边界：两侧不一致时的「无反应」是**已知影响**（D-251），**不是**缺陷、也不是回归。

**基线**
：v0.2.3 的**已实现态**（git `52749b9`，`BRIEF` 核对于 2026-11-06）——代码与 52 ~ 57 号都已经落地。判定边界：可引用面包含源码与文档两份；写「已发布」不成立（发布面另有 `07-PUBLISH.md` 的流程）。

### 0.3 四字段写法

每条决策固定四项，**缺一不可**，顺序固定：

| 字段 | 写什么 | 不许写什么 |
|---|---|---|
| **结论** | 一句话、祈使句、可直接执行 | 条件句、备选句、「可以考虑」 |
| **依据** | `BRIEF` §2 的裁定编号，或 §0 / §1 / §3 / 源码的 `文件:行号`；两者都给最好 | 「因为合理」「符合直觉」「业界惯例」 |
| **落地位置** | 要改的 `路径` 或 `类#方法` | 代码片段、算法步骤 |
| **补充** | 这一条**连带会影响什么**、**否决掉了哪些替代方案** | 实现细节（那是 60 号的事）、任务排期（那是 61 号的事） |

五条写作纪律：

1. **「本文补充，作者可否决」标记**：凡由本文推导、而非 `BRIEF` 直接给出的口径，在该句前标出。带这个标记的条目可以被作者否决；不标的等于转写裁定，否决它等于否决作者。
2. **数字纪律**：所有数值只能追到 `BRIEF` §0 / §2 / §3 的某一条，或两个已给数值的算术（152 − 2 + 1 = 151、92 − 0 + 0 = 92、175 − 0 + 0 = 175、321 − 0 + 0 = 321、0.30 × 255 ≈ 77 = `0x4D`）。需要新数值的写成「由 60 号定」，本文不自己编。
3. **禁用词**：不出现未定事项与任务清单标记；不用第二人称敬语，一律用「作者」。
4. **与源码的关系**：本文与源码冲突时报给作者，不自行取舍，也不改 `BRIEF`、不改任何既有文档（只引上游，不改上游）。
5. **只写决策**：不写实现步骤、不写任务拆解、不写排期、不写验收细则。

本版追加四条：

6. **表格纪律**：每张表首尾有 `|`，逐行列数一致。
7. **行号纪律**：本文引用的行号一律按 UTF-8 实读源码核过，不抄上游结论。
8. **措辞纪律**：plan 的原文不得直接当行为描述用（差异逐条登记在 §7）；不写无法验收的感受类词；已知影响必须成文（D-251、D-252）。
9. **源码注释用词纪律**：源码注释里写历史沿革一律用中文「耕地」；ASCII 的 `farmland` 只允许留在 `datagen/ModLangProvider.java:39-40` 那两行历史记账里（v0.1.1 F-192 / F-197 的沿革，保留不动）。理由是 v0.2.4 第 3 条验收用**大小写敏感**的 ASCII `grep -n "Farmland\|farmland" src/main/java` 判「死代码清零」（现在只剩上述 2 行），中文词不触发该 grep，两种写法混用会让验收在无关处变红。原版标识符 `VILLAGER_FARMLANDS`（`registry/FarmstormVillagerProfessions.java:31`）不在这条约束内。

---

## 1. 决策清单（D-225 ~ D-254）

本版编号 **D-225 ~ D-254，共 30 条**，连续无跳号。分组：§1.1 版本与范围 3 条（D-225 ~ D-227）、§1.2 绑定统一 8 条（D-228 ~ D-235）、§1.3 交易经验 4 条（D-236 ~ D-239）、§1.4 核心浮窗 3 条（D-240 ~ D-242）、§1.5 放置音效 4 条（D-243 ~ D-246）、§1.6 附带修复与注释 3 条（D-247 ~ D-249）、§1.7 文档规模与已知影响 3 条（D-250 ~ D-252）、§1.8 死代码清理 2 条（D-253 ~ D-254）。

### 1.1 版本与范围（D-225 ~ D-227）

#### D-225 · 版本号 0.2.4，基线 = 0.2.3 已实现态

- **编号**：D-225
- **结论**：把 `mod_version` 改为 **0.2.4**，以 **0.2.3 已实现态**（git `52749b9`）为基线；本版交付 **58 ~ 63 六份中文**，不做英译。
- **依据**：`plan0.2.4.txt:2`「版本号：0.2.4」、`:3`「使用neoforge模组加载器」、`:4`「minecraft本体版本：1.21.1」；`gradle.properties:11` 现值为 `0.2.3`；`BRIEF` §0 的基线实测（注册面 92 / server config 175 / client config 3 / 语言键 321 × 2 / Java 文件 152 / 检查器 FAIL 0）。
- **落地位置**：`gradle.properties:11`；`一键构建.cmd:5,11,12` 与 `检查环境.cmd:5,14`（共 3 + 2 = **5 处**版本串，本文实点）；`docs/zh/58` ~ `docs/zh/63`（6 份新增）；`README.md`（增量段）。
- **补充**：基线口径必须写死成「**已实现**」，不能写「已发布」：0.2.3 的代码全部落地（`CoreHudRenderer`、`processing_block` 标签产物、三个交易类都在源码里）。`plan0.2.4.txt:5` 的「本版本以0.2.3版本为主」是**基线口径**（在 0.2.3 之上追加），不是「本版版本号 0.2.3」，两者不得混读。否决「只改 `gradle.properties` 一处」：两处工具脚本与 `-sources.jar` 的复制名会与主 jar 脱节，构建面看不出错而产物名对不上。

#### D-226 · 本版范围 = plan 四条 + 裁定 #6 三项附带修复

- **编号**：D-226
- **结论**：把本版范围钉死为 plan 的 **4 条需求** + 裁定 #6 的 **3 项附带修复** + 使它们成立的口径同步；其余 0.2.3 行为一行不改。
- **依据**：`plan0.2.4.txt:5`「除非本文档明确要求，否则不能修改0.2.3原有内容」；裁定 #1 ~ #7；`BRIEF` §5 的 19 行必改清单；`BRIEF` §6 的 7 条红线。
- **落地位置**：全文；`src/` 的改动面以 §3 三张表为准（表外文件一律不动）。
- **补充**：plan 的 4 个编号块与决策的对应：

| # | plan 的原文位置 | 需求 | 决策 |
|---|---|---|---|
| ① | `:6-9` | 标签驱动的绑定统一 | D-228 ~ D-235 |
| ② | `:10-13` | 村民交易经验分级 | D-236 ~ D-239 |
| ③ | `:14-17` | 核心浮窗背景 | D-240 ~ D-242 |
| ④ | `:18-21` | 放置方块音效 | D-243 ~ D-246 |
| 附 | `:2-5` | 版本号 / 加载器 / MC 版本 / 基线口径 | D-225 |
| 裁 | `BRIEF` §2 #6 | 三项附带修复（音效 / 同步 / 注释） | D-245、D-247 ~ D-249 |
| 裁 | `BRIEF` §2 #7 | 文档规模 | D-250 |

  两条须知：① 裁定 #6 的三项不在 plan 的 4 条里，它们由作者裁定追加，**同属本版范围**（D-249）。② 「不能修改 0.2.3 原有内容」在本版必然被突破：第 ① ③ ④ 条的对象就是 0.2.3 建立的代码（清理出口 / HUD 常量 / 三个 `useWithoutItem`）。本文的读法是「4 条需求 + 为落地它们所必需的连带改动」算「明确要求」，且逐条登记在 §3——**这条读法是本文补充，作者可否决**。

#### D-227 · 数字面冻结：唯一变化的是 Java 文件 152 → 151

- **编号**：D-227
- **结论**：把注册面 92、server config 175、client config 3、语言键 321 × 2、`configuration` 组 178、网络包 2、Mixin 0、进度 16、转换配方 21、培养器投入物 17、交易 listing 26 全部冻结在基线值；本版**只有** Java 文件数变：**152 − 2 + 1 = 151**（删除的两个类见 D-253，新增的 `CoreBound.java` 见 D-228）。
- **依据**：`BRIEF` §3 全表与 §0 的实测；`tools/verify_project.py:458-471` 的 `EXPECTED_COUNTS`；`BRIEF` §6 红线 1 ~ 3；`Farmstorm.java:180-183` 的注册自检现值（`total == 92` / `items == 28` / `blocks == 28`）。
- **落地位置**：`tools/verify_project.py`（`EXPECTED` / `EXPECTED_COUNTS` **不改**）；`Farmstorm.java:180-183`（数字不动）；散文口径（`signoff` / `README`）里的「Java 文件」一项。
- **补充**：逐项算式（本文只做加减法）：

| 项 | 基线 | 目标 | 算式 |
|---|---|---|---|
| 注册面 | 92 | **92** | 92 − 0 + 0 |
| server config | 175 | **175** | 175 − 0 + 0（裁定 #4 硬编码，不新增键） |
| client config | 3 | **3** | 3 − 0 + 0（`FarmstormClientConfig.java:25-27` 三键不动） |
| 语言键 | 321 | **321** | 321 − 0 + 0（无新增、无删除、无改名） |
| └ `configuration` 组 | 178 | **178** | 178 − 0 + 0 = 175 + 3 |
| **Java 文件** | 152 | **151** | 152 − 2（`farmland` 两类，D-253） + 1（`CoreBound.java`，D-228） |
| 网络包 | 2 | **2** | 2 − 0 + 0（`PROTOCOL_VERSION` 与两个 payload 字段不动） |
| Mixin | 0 | **0** | 0 + 0 |
| 进度 | 16 | **16** | 16 − 0 + 0 |
| 转换配方 | 21 | **21** | 21 − 0 + 0 |
| 培养器投入物 | 17 | **17** | 6 + 11（`CultivatorBlockEntity.java:269-283`） |
| 交易 listing | 26 | **26** | 22 + 1 + 3（只改 xp 值，不增减项） |
| 检查器 FAIL | 0 | **0** | `EXPECTED` 不改，故不增不减 |

  **152 是 0.2.3 的基线实数**（`src/main/java/**/*.java` 实点）；删除 `farmland` 两个类后全树 **150**（2 个文件、225 + 555 = 780 行），加 `CoreBound.java` 得 **151**（D-253、D-228）。`verify_project.py:479-480` 只把源码拼成一个大字符串做内容校验、**不校验文件数**，所以 151 只出现在散文口径里，检查器不需要改。否决「顺手把 151 加进检查器」：那会让检查器多一条与本版行为无关的硬编码。

### 1.2 绑定统一（D-228 ~ D-235）

#### D-228 · 新增 `CoreBound` 接口，只声明三个方法

- **编号**：D-228
- **结论**：新建 `blockentity/CoreBound.java`，只声明 `corePos()` / `setCore(BlockPos)` / `clearCore()` 三个方法，不声明第四个。
- **依据**：裁定 #1；`BRIEF` §4；培养器三方法的现值是 `CultivatorBlockEntity.java:66`、`:84`、`:94`；转换器只有 `corePos()`（`CropConverterBlockEntity.java:71`）与 `setCore`（`:89-92`）。
- **落地位置**：`src/main/java/com/dashpig/farmstorm/blockentity/CoreBound.java`（新建）。
- **补充**：Java 文件 150 + 1 = 151（150 是删掉 `farmland` 两类之后的全树实数，D-253），本版唯一变化的数字（D-227）。否决「抽共同基类 `AbstractBoundBlockEntity`」与「接口再加 `kind()`」：前者要两个状态机共享字段（转换器的 `processTicks()` 与培养器的 `cycleTicks` 各自成体系，`CropConverterBlockEntity.java:282`、`CultivatorBlockEntity.java:54`），后者超出 `BRIEF` §4 的三方法，两条都在 §6 登记。

#### D-229 · 两个方块实体都实现 `CoreBound`

- **编号**：D-229
- **结论**：让 `CultivatorBlockEntity` 与 `CropConverterBlockEntity` 都 `implements CoreBound`；培养器已有三个方法只加声明，转换器补 `clearCore()`。
- **依据**：裁定 #1；`CultivatorBlockEntity.java:44`（类声明）、`:66` / `:84` / `:94`（三方法已齐）；`CropConverterBlockEntity.java:40`（类声明）、`:71`（`corePos`）、`:89-92`（`setCore`）；全仓 `clearCore` 只有 **1 处定义**（`CultivatorBlockEntity.java:94`）+ **1 处调用**（`OutpostCoreBlockEntity.java:871`）。
- **落地位置**：两个类声明行；`CropConverterBlockEntity#clearCore`。
- **补充**：转换器的 `clearCore` 与培养器同形（清缓存 + 同步，不清 `Held` / `Progress`，D-168 继续有效）。否决「让转换器用 `setCore(null)` 表达解绑」：那会把接口第二个方法变成读写两用，且与「NBT 缺 `Core` 键 = 未绑定」的既有约定（`CropConverterBlockEntity.java:269-272`）读不出区别。

#### D-230 · 候选发现改为按标签

- **编号**：D-230
- **结论**：把 `rebindNearby` 的候选收集改为「按 `farmstorm:processing_block` 标签发现方块实体」，删掉两次按 `Class` 的枚举。
- **依据**：裁定 #1；`OutpostCoreBlockEntity.java:604-608`（现状两次 `AreaBlockEntities.collect(..., Class)`：`CultivatorBlockEntity.class` 与 `CropConverterBlockEntity.class`）；`FarmstormTags.java:30`（`TagKey<Block>`）；`ModBlockTagsProvider.java:45-47`（成员 2）；`processing_block.json:2-5`（2 个值）。
- **落地位置**：`OutpostCoreBlockEntity#rebindNearby`。
- **补充**：连带效应——任何未来加入该标签的方块自动获得绑定能力，改成员必须同步 `ModBlockTagsProvider.java:45-47`（`BRIEF` §7 #1）。否决「保留两次按类枚举、只补一个转换器分支」：那是把一个 bug 的第三份副本留给下一个标签成员（§6 第 5 条）。

#### D-231 · `AreaBlockEntities` 增按谓词收集的重载，旧重载保留

- **编号**：D-231
- **结论**：给 `AreaBlockEntities` 增一个按谓词（标签）收集的重载；现有按 `Class` 的 `collect` 原样保留。
- **依据**：裁定 #1；`AreaBlockEntities.java:32-33`（现唯一重载的形参）、`:44`（`type.isInstance` 过滤）、`:13-15`（不为枚举而加载区块的纪律）。
- **落地位置**：`util/AreaBlockEntities#collect`。
- **补充**：旧重载还有别的调用者，删它等于把本版改动面扩到无关功能。否决「在 `collect` 上加第四个 `Class` 列表参数」：调用点会写出「一次转两次」的形状，正好是 D-230 要删掉的那种。

#### D-232 · 三处成对 `instanceof` 改为接口调用

- **编号**：D-232
- **结论**：把缓存判定、绑定写入、迁移改绑三处的成对 `instanceof` 全部改为经由 `CoreBound` 调用。
- **依据**：裁定 #1；`BRIEF` §4；`OutpostCoreBlockEntity.java:621-629`（kind 与缓存的成对判定）、`:639-643`（绑定写入）、`:851-855`（迁移改绑）。
- **落地位置**：`OutpostCoreBlockEntity#rebindNearby`、`OutpostCoreBlockEntity#settleOnRemoval`。
- **补充**：三处都保留「核侧先写、机器侧后写」的顺序（`OutpostCoreBlockEntity.java:636-643`），接口化不改时序。否决「只在缓存判定处接口化、绑定处保留 `instanceof`」：两套判定并列，下一处新增时就得二选一，等于没统一。

#### D-233 · 清理出口覆盖两个方块（bug 的正面修复）

- **编号**：D-233
- **结论**：把「无接替核」出口从「只判培养器」改为「该格仍是 `processing_block` 方块实体 ⇒ 调 `clearCore()`」，让转换器与培养器走同一条。
- **依据**：裁定 #1；`OutpostCoreBlockEntity.java:866-873`（现状 `:870-873` 的 `instanceof CultivatorBlockEntity` 之后无 else、无转换器分支）；病因链：残留 `corePos` 写进 NBT（`CropConverterBlockEntity.java:270-272`）⇒ 补绑对非空缓存一律跳过（`OutpostCoreBlockEntity.java:630-632`）⇒ 永久拒绝。
- **落地位置**：`OutpostCoreBlockEntity#settleOnRemoval`。
- **补充**：`clearCore` 的调用点仍只有 1 处（原 `:871` 被合并进新分支），不新增第二条解绑路径。这一条才是需求 1 的正面修复，D-230 ~ D-232 是让它不再复发的手段。否决「在补绑里判缓存坐标还有没有核」：那要在每轮补绑时按候选坐标查区块，且没修「拆核时没清缓存」这个事实本身。

#### D-234 · `Link.Kind` 不由接口推断

- **编号**：D-234
- **结论**：`CoreBound` 不暴露 `Link.Kind`；kind 继续由方块自己在放置时写入，接口不承担类型判定。
- **依据**：`CultivatorBlock.java:100`、`CropConverterBlock.java:104`（两处 `addCultivator(pos, Link.Kind.…)`）；`OutpostCoreBlockEntity.java:619-629`（现状 kind 由 `instanceof` 分支给出）、`:847`（`kindOf(pos)` 迁移取 kind）、`:398`（`addCultivator` 形参带 kind）；`CultivatorLinkPayload.java:69`（`enum Kind` 是协议字段）。
- **落地位置**：`blockentity/CoreBound`（不声明 `kind()`）。
- **补充**：接口保持 `BRIEF` §4 的三方法形状（D-228），kind 的来源另立一条（D-235）。否决「接口加 `kind()`」：`Link.Kind` 是**协议字段**，把它做成方块实体的契约会让「这台机器的 kind 是谁」多出第二份真相。

#### D-235 · kind 由「方块 → `Link.Kind`」单一映射承担

- **编号**：D-235
- **结论**：在 `OutpostCoreBlockEntity` 内保留一张「方块 → `Link.Kind`」的单一映射，按候选方块的注册类型取 kind；新增标签成员必须同时登记进这张映射。
- **依据**：裁定 #1 只定「按标签发现」，未定 kind 从哪来；`OutpostCoreBlockEntity.java:619-629`（现状 kind 由 `instanceof` 分支给出）；标签产物 `processing_block.json:2-5` 只有方块 id、不含类型；`FarmstormTags.java:30` 是 `TagKey<Block>`，结构上放不下类型。
- **落地位置**：`OutpostCoreBlockEntity#rebindNearby`（映射与取用）。
- **补充**：**本文补充，作者可否决**——接口化之后 kind 从哪来是本文定的落法；作者若选「接口加 `kind()`」（§6 第 1 条的否决面）或「标签只放一台机器的变体」，改的仍是 `OutpostCoreBlockEntity` 一处。连带：没登记进映射的新成员会被发现却不能绑定，这条约束写进 §8 第 4 条，由 60 号写成已知限制。

### 1.3 交易经验分级（D-236 ~ D-239）

#### D-236 · 五级经验取 2 / 5 / 10 / 15 / 30，映射只在一处

- **编号**：D-236
- **结论**：把 1 ~ 5 级的成交经验定为 **2 / 5 / 10 / 15 / 30**，映射只在 `event` 包内一处定义，其余类只引用不复制。
- **依据**：裁定 #2；原版同方向常量的声明在 `VillagerTrades.java:70-78`（`XP_LEVEL_1_SELL` = 1 … `XP_LEVEL_5_TRADE` = 30，全部 `private static final`；`BRIEF` §1 需求 2 已核「只声明、未被引用」）；现值三个类常量全部 = 2（`BotanistTrade.java:49`、`BotanistEpicTrade.java:46`、`TierTrade.java:45`）。
- **落地位置**：`event/VillagerTradeEvents`（五个具名常量）。
- **补充**：**本文补充，作者可否决**——「放在 `VillagerTradeEvents`」是本文选的落点（`BRIEF` §4 给了「`VillagerTradeEvents` 或各类的 `XP_LEVEL_n`」两个候选）；选前者是因为五级映射与等级切片在同一处，选后者会让三份常量各自记一套。1 级取 **2**（保留现值），不取原版的 `XP_LEVEL_1_SELL = 1`。否决「引用原版那五个符号」：它们是 `private`，原版自己的表也没用它们。

#### D-237 · 两个 record 逐项携带 xp

- **编号**：D-237
- **结论**：给 `BotanistTrade` 与 `TierTrade` 的 record 各加一个 `xp` 分量，22 项与 3 档逐项携带自己的值。
- **依据**：裁定 #2；等级由注册处切片决定、listing 不知道自己的等级（`VillagerTradeEvents.java:44-47`、`:55-57`）；现值两个 `getOffer` 都把类常量直接传进 `MerchantOffer`（`BotanistTrade.java:91`、`TierTrade.java:79`）。
- **落地位置**：`event/BotanistTrade`（record 分量 + `:60-82` 的 22 个实例）、`event/TierTrade`（record 分量 + `:49-54` 的三档常量）。
- **补充**：`NORMAL` 的 22 项**顺序**与分桶 6 / 6 / 6 / 4 不动（红线 4、D-239）；`MIN_PRICE` / `MAX_PRICE` / `MAX_USES` / `COUNT` / `PRICE_MULTIPLIER` 一行不改（红线 6）。否决「按 `CropTier` 查一张表」：档位与等级不同轴——植物学家 1 ~ 4 级全是 `NORMAL` 切片，用档位推等级会把四个等级算成同一个值，需求 2 当场落空（§6 第 2 条）。

#### D-238 · 史诗类只改常量

- **编号**：D-238
- **结论**：把 `BotanistEpicTrade.VILLAGER_XP` 从 **2** 改成 **30**，不给这个类加 record 分量。
- **依据**：裁定 #2；该类只有一项且固定挂 5 级（`VillagerTradeEvents.java:49`）；现值 `BotanistEpicTrade.java:46`（常量）、`:64`（引用点）、`:45`（注释里的「沿用 2」）。
- **落地位置**：`event/BotanistEpicTrade`（`:28` 的类 javadoc、`:45` 的注释、`:46` 的常量，三处同批改）。
- **补充**：一类只有一个取值时不携带，加分量会造出「字段有值但只有 1 个被读」的形状。`MAX_USES = 1`（`:42`）与价格 40 ~ 64（`:38`、`:40`）不动。

#### D-239 · 分组边界不动，分级只由 `VillagerTradeEvents` 决定

- **编号**：D-239
- **结论**：`subList` 的边界 0 / 6 / 12 / 18 / 22 与 `get(1)` / `get(3)` / `get(5)` 的挂载位置一律不动；等级 → 经验的对应只由 `VillagerTradeEvents` 的分组决定。
- **依据**：裁定 #2；`VillagerTradeEvents.java:44-47`（四个切片）、`:55-57`（农民三级）；`BRIEF` §4「`:44-47` 的 `subList` 分组边界（0/6/12/18/22）**不得改**」。
- **落地位置**：`event/VillagerTradeEvents#onVillagerTrades`。
- **补充**：改边界等于同时改「哪一项在几级」与「这一项拿多少经验」，是两条需求混在一处；本版只动后者。分布保持：植物学家 6 + 6 + 6 + 4 = 22 项 + 史诗 1 项 = 23，农民 1 + 0 + 1 + 0 + 1 = 3，合计 23 + 3 = 26 = 22 + 1 + 3。

### 1.4 核心浮窗（D-240 ~ D-242）

#### D-240 · 「核心浮窗」= `CoreHudRenderer`

- **编号**：D-240
- **结论**：把「核心浮窗」钉死为 `client/CoreHudRenderer`（HUD 叠加层）；`OutpostCoreScreen` **不在**本版范围。
- **依据**：裁定 #3；`CoreHudRenderer.java:27`（`@EventBusSubscriber(value = Dist.CLIENT)`）、`:52`（订阅 `RenderGuiEvent.Post`）、`:61-63`（有别的 `Screen` 时不画）；`OutpostCoreScreen.java:21`（`extends AbstractContainerScreen`，54 格容器）。
- **落地位置**：`docs/zh/59` ~ `docs/zh/63` 的术语表；`client/CoreHudRenderer`。
- **补充**：两个名字都能「浮在屏幕上」，但 `OutpostCoreScreen` 只在右键核心时出现（`OutpostCoreBlock.java:134-141`），`CoreHudRenderer` 在没有 `Screen` 时也画。写错会让下游改到容器界面（红线 5）。

#### D-241 · 底色 alpha 改 `0x4D`，硬编码，不加键

- **编号**：D-241
- **结论**：把浮窗底色常量从 `0x80000000` 改为 **`0x4D000000`**，硬编码在渲染类里，不新增任何 config 键。
- **依据**：裁定 #3（「30%」读作不透明度）与 #4（硬编码）；`CoreHudRenderer.java:40`（现值 `0x80000000`，alpha `0x80` = 128，128 ÷ 255 = 50.2%）；算术 0.30 × 255 = 76.5 ≈ 77 = `0x4D`，77 ÷ 255 = 30.2%。
- **落地位置**：`client/CoreHudRenderer#COLOR_BACKGROUND`（`:40`）。
- **补充**：client config 保持 3 键（`FarmstormClientConfig.java:25-27`），语言键 321 × 2 不变（无新增、删除、改名）——裁定 #4 的连带。否决「把 30% 读成透明度（alpha `0xB3`）」与「挂 `hud.opacity` 新键」：前者比现状更不透明，与「增加透明度」相反；后者破 config 冻结（§6 第 3、4 条）。

#### D-242 · 边框不动

- **编号**：D-242
- **结论**：保留边框 `COLOR_BORDER` = `0xFF000000` 不透明，本版只改内层底色。
- **依据**：裁定 #3 只针对背景；`CoreHudRenderer.java:41`（边框常量）、`:77-78`（两条 `fill`：`:77` 画外框、`:78` 画内底）、`:34-36`（`PADDING = 2` 与面板高 `10n + 3`）。
- **落地位置**：`client/CoreHudRenderer`（`:41` 不改）。
- **补充**：两条 `fill` 的几何（外框整块 + 内底内缩 1 px）与版式常数都不动，所以拖拽态的预览与吸附（`CoreHudDragScreen`）不受影响。否决「连边框一起半透明」：1 px 边框是面板的唯一硬边界，透明化后玩家看不出面板到哪结束，拖拽对位失去参照。

### 1.5 放置音效（D-243 ~ D-246）

#### D-243 · 两台机器的客户端分支改为条件返回

- **编号**：D-243
- **结论**：把转换器与培养器的客户端分支改为「手持物是输入物才 `SUCCESS`，否则 `PASS`」；服务端四道闸一行不改。
- **依据**：裁定 #5；`CropConverterBlock.java:121-122`、`CultivatorBlock.java:117-118`（现无条件 `SUCCESS`）；因果链：`SUCCESS` 属「已消费」（`InteractionResult.java:11-13`）⇒ 客户端在 `MultiPlayerGameMode.java:337-341` 提前返回 ⇒ `itemstack.useOn` / `BlockItem.place` 的本地音效一次都不跑（`BlockItem.java:86-93`）。
- **落地位置**：`block/CropConverterBlock#useWithoutItem`、`block/CultivatorBlock#useWithoutItem`。
- **补充**：净现象「方块放得下、物品被消耗、周围玩家听得见、只有放置者本人听不见」由两条路径决定：服务端广播把放置者当排除者（`Level.java:410-412` → `ServerLevel.java:994-1004`），客户端对称实现只给本人本地播（`ClientLevel.java:527-529`），而这一半正是被 `SUCCESS` 掐掉的那一半。否决「改服务端播放范围」：排除放置者是原版语义，改它会动全模组的放置音（§6 第 7 条）。

#### D-244 · 客户端谓词不得用 `canInsert`

- **编号**：D-244
- **结论**：客户端谓词取 `convertible(stack)`（转换器）与 `admissible(stack)`（培养器），**不得**调用 `canInsert`。
- **依据**：裁定 #5；`canInsert` 含 `isUsable()`（`CropConverterBlockEntity.java:118-120`、`CultivatorBlockEntity.java:152-154`），而 `core()` 在客户端要求 `ServerLevel`（`CropConverterBlockEntity.java:232-238`）⇒ 客户端恒假、右键整台机器没反应；`convertible` 在 `CropConverterBlockEntity.java:129-137`，`outputProfileOf` 在 `CultivatorBlockEntity.java:164`（经 `admissible` `:143-149`）。
- **落地位置**：`CropConverterBlockEntity#convertible`、`CultivatorBlockEntity#admissible`（已是 `public`，`:143`；不得改用 `private` 的 `outputProfileOf`，`:163`）。
- **补充**：客户端只看物品品类、服务端还看范围 / 绑定 / 空槽，两侧落差的「无反应」是**已知影响**（D-251），不是缺陷。否决「复用 `canInsert`」：那是把服务端裁决搬到客户端，客户端拿不到 `ServerLevel`，结果恒为「什么都没发生」（§6 第 6 条）。

#### D-245 · 据点核心并入同一处理

- **编号**：D-245
- **结论**：把据点核心的客户端分支从「无条件 `SUCCESS`」改为「手持 `BlockItem` ⇒ `PASS`，否则 `SUCCESS`」，且客户端不做归属判定。
- **依据**：裁定 #6（据点核心同类音效）；`OutpostCoreBlock.java:131-132`（现无条件 `SUCCESS`）、`:134-141`（服务端按 `isAlly` 开界面或 `PASS`）、`:120-121`（非己方 `PASS` 的语义）、`:126`（HC-03：客户端只返回预测结果、不做归属判定）；同根因见 D-243。
- **落地位置**：`block/OutpostCoreBlock#useWithoutItem`。
- **补充**：**本文补充，作者可否决**——「手持 `BlockItem`」这个谓词由本文推导：核心不收物品，能让这次右键「被手上物品接走」的只有放置。作者若改判「空手才 `SUCCESS`」，则任何手持物都走 `PASS`，改动仍是同一处。连带：己方玩家手持方块右键核时，客户端会先按放置走一遍（`MultiPlayerGameMode.java:348-358`），服务端仍在 `useWithoutItem` 里开界面——这次预测怎么收尾由 60 号写成已知影响（§8 第 3 条）。

#### D-246 · 不改音效配置

- **编号**：D-246
- **结论**：**不得**为修复本条改动 `SoundType`、`sound_event` 注册或任何音频资源；音效丢失与音效配置无关。
- **依据**：`BRIEF` §1 需求 4 第 6 段；`FarmstormBlocks.java:78` 与 `:108`（两处都是 `SoundType.ANVIL`）；`SoundType.java:58-60`（place 音 = `SoundEvents.ANVIL_PLACE`）；全仓 grep 无自定义 `getSoundType`，`sound_events` 仍是 3 个（`FarmstormSounds.java:10`）。
- **落地位置**：无改动面；`FarmstormBlocks`、`FarmstormSounds` 保持不动。
- **补充**：**本文补充，作者可否决**——写成显式禁令是因为「音效丢失」的字面读法会把下游引向换 `SoundType` 或补一条 place 音（§7 第 4 条）。否决「给两台机器补模组私有 place 音效」：那会改掉现有全部放置音，并新增 `sound_event` 注册项、破注册面 92 的冻结（D-227）。

### 1.6 附带修复与注释（D-247 ~ D-249）

#### D-247 · 转换器 `setCore` 补同步

- **编号**：D-247
- **结论**：让转换器的 `setCore` 与 `clearCore` 都触发一次方块更新同步，与培养器同形。
- **依据**：裁定 #6；`CropConverterBlockEntity.java:89-92`（现 `setCore` 只写字段 + `setChanged()`，无同步）；`CultivatorBlockEntity.java:84-88`（`setCore` 调 `syncCore()`）、`:94-101`（`clearCore` 同样）、`:104-110`（私有 `syncCore()`）、`:103`（同步时点的纪律）。
- **落地位置**：`CropConverterBlockEntity#setCore`、`CropConverterBlockEntity#clearCore`、`CropConverterBlockEntity#syncCore`。
- **补充**：转换器现在**没有** `syncCore()`（全仓 grep 实测：私有定义只有 `CultivatorBlockEntity.java:104` 一处）⇒ 裁定 #6 的「补 `syncCore()`」包含**方法本身**，不是只加一行调用。两个类各写一份同形私有方法，不抽公共基类（与 D-228 同一决定）。不同步的代价是机器侧缓存变化不即时下发到客户端。

#### D-248 · `FarmstormTags.java:29` 注释订正

- **编号**：D-248
- **结论**：把 `FarmstormTags.java:29` 的注释订正为「**2 个成员**（培养器 + 农作物转换器）」，并指向 D-194 与标签产物；**不得**写「裁定 #5」。
- **依据**：裁定 #6；`FarmstormTags.java:29` 现写「本版唯一成员是培养器，converter 不进（裁定 #5）」；`ModBlockTagsProvider.java:44-47`（成员 2）；`processing_block.json:2-5`（2 个值）；`BRIEF` §7 #4（这句「裁定 #5」指的是 52 号的第 5 条裁定「边界行为照抄培养器先例」，**不是**本版的裁定 #5 = 放置音效）。
- **落地位置**：`util/FarmstormTags`（`:29` 注释）。
- **补充**：注释与代码不符是**事实错误**，订正不改行为、不改标签成员（`ModBlockTagsProvider.java:45-47` 一行不动，产物仍是 2 项）。裁定号按版本重编，跨版本引用必须写全来源（52 号 §2 的 #5），否则下一版又对不上——这是本条不许照抄旧注释的原因。

#### D-249 · 三项附带修复与四条需求同批交付

- **编号**：D-249
- **结论**：把裁定 #6 的三项附带修复与 plan 的四条需求放在同一批交付，不拆成两段。
- **依据**：裁定 #6（三项全做）；`BRIEF` §5 的 19 行必改清单（三项分别占第 **3**、**13**、**19** 行）。
- **落地位置**：全文；`docs/zh/61` 的任务清单。
- **补充**：三处是同一类欠账的三份副本（客户端预测被 `SUCCESS` 掐断 / 引用缓存不同步 / 注释与代码不符）；只修两台机器会留下据点核心那一份。否决「附带修复留到 0.2.5」：那会让同一版本里「修了音效」与「没修音效」并存，玩家遇到同一类现象时有时无。

### 1.7 文档规模与已知影响（D-250 ~ D-252）

#### D-250 · 文档规模下限与归属

- **编号**：D-250
- **结论**：下游五份文档按 **边界 ≥ 12 / 验收 ≥ 20 / 陷阱 ≥ 24 / 差异 ≥ 8** 交付。
- **依据**：裁定 #7；`BRIEF` §2 的裁定 #7 行。
- **落地位置**：`docs/zh/59` ~ `docs/zh/63`。
- **补充**：**本文补充，作者可否决**——四项分别落在：边界 → B 系列（59 号）、验收 → E 系列（63 号）、陷阱 → 60 号的「陷阱」节、差异 → 59 号的「差异」节；这是本文的分配，裁定 #7 只给四个下限。下限不是配额，超出的部分按内容需要写。E 系列跟随 Phase 号（下一段从 **E-66-1** 起，见 §0.1）。

#### D-251 · 两侧落差写成已知影响

- **编号**：D-251
- **结论**：把「客户端判据与服务端裁决的落差」写进设计与技术文档的**已知影响**节，不得写成缺陷或回归。
- **依据**：`BRIEF` §7 #2；裁定 #5；D-244。
- **落地位置**：`docs/zh/59`、`docs/zh/60` 的「已知影响」节。
- **补充**：表现是「手持输入物但机器已满或未绑定 ⇒ 无反应」，与 0.2.3 的既有取舍同源（`CultivatorBlock.java:108-110` 记的 HC-03：右键无反应的定义是行为面无反应，不是动画面无反应）。落差的**范围**在服务端不变（四道闸一行不改），只是客户端多认了几种「这次右键会被接走」的物品。

#### D-252 · 未实测项按未实测登记

- **编号**：D-252
- **结论**：把「只有放置者听不见」「副手放置」「客户端 `getRecipeManager()` 可用」三项登记为未实测，**不得**写成已验证。
- **依据**：`BRIEF` §7 #3：① 由 `ServerLevel.java:994-1004` 与 `ClientLevel.java:527-529` 推出的只是静态读链；② 副手放置不满足 `MAIN_HAND` 判定（`ServerPlayerGameMode.java:369`），推测本来就有音效；③ `convertible()` 在客户端走 `level.getRecipeManager()`（`CropConverterBlockEntity.java:134-136`）是否可用。
- **落地位置**：`docs/zh/60` 的 RT 系列（RT-83 起）、`docs/zh/63` 的验收条目。
- **补充**：三项都指向「进游戏跑一遍就能定」，不需要新决策；写进文档是为了不让下游把静态读链当实测结果转述。

### 1.8 死代码清理（D-253 ~ D-254）

#### D-253 · 删除 `farmland` 包的两个类

- **编号**：D-253
- **结论**：删除 `farmland/FarmlandGuard.java`（225 行）与 `farmland/FarmlandLedger.java`（555 行）两个类，空的 `farmland/` 目录一并删除。
- **依据**：裁定 #8；这两个类是 v0.2.0 撤销耕地（D-153）时按「本体一行不改」刻意留下的死代码，运行期不可达、全树无读者——两者只互相引用，清理前 `event/FarmstormGameEvents` 里只剩被注释掉的监听器还提到它们（清理后该文件 0 处引用）；大小写敏感的 `grep -n "Farmland\|farmland" src/main/java` 复跑只剩 `datagen/ModLangProvider.java:39-40` 两行历史记账。
- **落地位置**：`src/main/java/com/dashpig/farmstorm/farmland/`（删 2 个文件 + 删目录）；连带清理 `event/FarmstormGameEvents`——2 个 import、17 行耕地注释（`git show HEAD` 实点：`// 裁定 #3…` 5 行 + 其余 12 行）、`onRightClickBlock` / `onFarmlandTrample` 两个死方法（整段 javadoc 与本体）、4 个随之变死的 import（`Component` / `TriState` / `PlayerInteractEvent` / `BlockEvent`），头注释从「七件事」改为按 `register()` 的 9 个 `addListener` 逐条穷举；`outpost/OutpostCoreIndex` 的 `ownerOf` 整个方法（方法体 13 行 + javadoc）——它是「坐标 → 业主」的反查原语，唯一读者 `FarmlandGuard#inOwnRange` 随包消失后一并删除。
- **补充**：数字面 152 − 2 = **150**，再加本版新增的 `CoreBound.java` 得 **151**（D-227、D-228）；其余冻结项一项不动（注册面 92、server config 175 键 / 60 组、client config 3 键、语言键 321 × 2、进度 16、转换配方 21、网络包 2、Mixin 0、`PROTOCOL_VERSION = "3"`）。否决「保留两个类以留回头路」（A-62 的否决项 ③）：那条否决的理由是「删了就没有回头路」，而本版连同字段声明一起删（D-254），恢复面已不由这两个文件承担；留 881 行不可达代码（881 = 225 + 555 的两个类 + 101 行连带清理）只让下一次 grep 把死代码当活代码。

#### D-254 · 删除 `outpostCoreFarmlandBonus` 字段声明（取代 A-62）

- **编号**：D-254
- **结论**：删除 `FarmstormConfig.outpostCoreFarmlandBonus` 的字段声明与它上方 4 行注释，并删掉定义处 1 行注释与 4 行被注释的 `.defineInRange`，直接撤销 A-62。
- **依据**：裁定 #8；A-62 的前提是「`FarmlandGuard.java:75` 与 `FarmlandLedger.java:305` 两处 `.get()` 仍在」（36 号 §10 末 A-62 的「理由」行），D-253 已删掉这两个类 ⇒ 字段恒为 `null` 且无读者；该键的 `.define` 在 v0.2.0 就已注释，server config 的 175 键 / 60 组与它无关。
- **落地位置**：`config/FarmstormConfig`（字段声明 1 行 + 字段上方 4 行注释 + 定义处 1 行注释 + 4 行被注释的 `.defineInRange`）。
- **补充**：删除依据是「两个类已不存在，字段恒为 `null` 且无读者」，不是「这个键没人配置」。**本条取代 A-62**（§5 墓碑）：A-62 的「保留声明是让两个类仍能编译的唯一做法」随 D-253 一起消失。连带：`FarmstormConfig` 里同一段历史注释的 ASCII `farmland` / `farmlandBonus` 改写为中文「耕地」/「每核上限」（§0.3 第 9 条）。否决「保留字段声明以免将来的误恢复编译不过」：要恢复监听器就得先恢复两个类、再重建字段与定义，A-62 那套「保声明换编译」的交换条件已经不成立；留一个恒 `null`、零读者、连定义都没了的 `IntValue` 只会让下一个人以为「只是没配置」。

---

## 2. 作者裁定索引（8 条 → 决策）

`BRIEF` §2 的裁定编号即下游引用号，本文只做映射，不改写内容。裁定 #8 不在 `BRIEF` §2 的 7 条里——它由作者在本文写作期追加（`farmland` 死代码清理），编号接在 #7 之后。

| # | 议题 | 裁定 | 决策 |
|---|---|---|---|
| 1 | 需求 1 · 绑定统一 | 按 `farmstorm:processing_block` **标签**发现方块实体，不按类 | D-228、D-229、D-230、D-231、D-232、D-233 |
| 2 | 需求 2 · 交易经验 | 1→2 / 2→5 / 3→10 / 4→15 / 5→30（1 级保留现值 2） | D-236、D-237、D-238、D-239 |
| 3 | 需求 3 · 「30%」的读法 | 读作**不透明度 30%** ⇒ alpha `0x4D` | D-240、D-241 |
| 4 | 需求 3 · 落地形态 | **硬编码** `0x4D000000`，**不新增** config 键 | D-241、D-242、D-227 |
| 5 | 需求 4 · 客户端判据 | 手里是输入物才 `SUCCESS`，否则 `PASS`；谓词不得用 `canInsert` | D-243、D-244、D-246 |
| 6 | 范围 · 三项附带修复 | 三项全做：据点核心同类音效 / 转换器补同步 / `FarmstormTags` 注释订正 | D-245、D-247、D-248、D-249 |
| 7 | 文档规模 | 边界 ≥ 12、验收 ≥ 20、陷阱 ≥ 24、差异 ≥ 8 | D-250 |
| 8 | 范围 · `farmland` 死代码清理 | 删除 v0.2.0 撤销耕地后遗留的 `FarmlandGuard` / `FarmlandLedger` 两个类，并改写 A-62 | D-253、D-254 |

不挂裁定的决策（由本文推导，作者可否决）：D-225 的版本串范围、D-227 的散文口径、D-234 / D-235 的 kind 来源、D-245 的 `BlockItem` 谓词、D-251 / D-252 的登记要求。D-226 的「明确要求」读法同属本文补充（该条 补充 已标）。

---

## 3. 对 0.2.3 的增 / 改 / 删

### 3.1 新增（6 项）

| # | 物件 | 类型 | 决策 |
|---|---|---|---|
| 1 | `blockentity/CoreBound.java` | 类（Java 文件 150 + 1 = 151） | D-228 |
| 2 | `CropConverterBlockEntity#clearCore` | 方法 | D-229 |
| 3 | `CropConverterBlockEntity#syncCore` | 方法（私有） | D-247 |
| 4 | `AreaBlockEntities#collect` 的按谓词重载 | 方法重载 | D-231 |
| 5 | 交易等级 → 经验的 5 个具名常量（2 / 5 / 10 / 15 / 30） | 常量 | D-236 |
| 6 | 「方块 → `Link.Kind`」的单一映射 | 映射（`OutpostCoreBlockEntity` 内） | D-235 |

### 3.2 修改（20 项）

| # | 位置 | 改法 | 决策 |
|---|---|---|---|
| 1 | `OutpostCoreBlockEntity.java:604-608` | 两次按类枚举 → 一次按标签发现 | D-230 |
| 2 | `OutpostCoreBlockEntity.java:621-629` | 缓存判定改 `CoreBound#corePos` | D-232 |
| 3 | `OutpostCoreBlockEntity.java:639-643` | 绑定写入改 `CoreBound#setCore` | D-232 |
| 4 | `OutpostCoreBlockEntity.java:851-855` | 迁移改绑改 `CoreBound#setCore` | D-232 |
| 5 | `OutpostCoreBlockEntity.java:870-873` | 清理出口覆盖两个方块 | D-233 |
| 6 | `CultivatorBlockEntity.java:44` | 类声明加 `implements CoreBound` | D-229 |
| 7 | `CropConverterBlockEntity.java:40` | 类声明加 `implements CoreBound` | D-229 |
| 8 | `CropConverterBlockEntity.java:89-92` | `setCore` 补同步 | D-247 |
| 9 | `BotanistTrade.java:38`、`:60-82`、`:91` | record 加 `xp` 分量；22 项逐项带值 | D-237 |
| 10 | `TierTrade.java:37-38`、`:49-54`、`:79` | record 加 `xp` 分量；三档各带值 | D-237 |
| 11 | `BotanistEpicTrade.java:46`（连带 `:28` 的 javadoc 与 `:45` 的注释） | `VILLAGER_XP` 2 → 30；`{@link TierTrade#VILLAGER_XP}` 改指 `XP_LEVEL_5` | D-238 |
| 12 | `VillagerTradeEvents.java:44-47`、`:55-57` | 边界不动，传入本级取值 | D-239 |
| 13 | `CropConverterBlock.java:121-122` | 客户端判据 | D-243、D-244 |
| 14 | `CultivatorBlock.java:117-118` | 客户端判据 | D-243、D-244 |
| 15 | `OutpostCoreBlock.java:131-132` | 客户端判据（手持方块才让路） | D-245 |
| 16 | `CoreHudRenderer.java:40` | `0x80000000` → `0x4D000000` | D-241 |
| 17 | `FarmstormTags.java:29` | 注释订正为「2 个成员」 | D-248 |
| 18 | `FarmstormGameEvents` 的头注释与 `onServerTickPost` / `onServerStopping` 的 javadoc | 头注释改为按 `register()` 的 9 个 `addListener` 逐条穷举；两处 javadoc 去掉「F-198 补建 / 先补票后补绑定」的引用（行为零变化） | D-253 |
| 19 | `FarmstormConfig` 的 `outpost_core` 段历史注释 | ASCII 的 `farmland` / `farmlandBonus` 改写为中文「耕地」/「每核上限」（行为零变化） | D-253、D-254 |
| 20 | `OutpostCoreIndex` 的静态入口与 `count` 两处 javadoc | 去掉「F-156 的额度求和」的引用；`count` 记活调用链（`OutpostCoreBlock` 的放置上限 → `countOwned` → 本方法），不记「无调用者」（行为零变化） | D-253、D-254 |

### 3.3 删除（4 项）

| # | 物件 | 类型 | 决策 |
|---|---|---|---|
| 1 | `farmland/FarmlandGuard.java`（225 行） | 类 | D-253 |
| 2 | `farmland/FarmlandLedger.java`（555 行） | 类（Java 文件 152 − 2 = 150） | D-253 |
| 3 | `FarmstormConfig.outpostCoreFarmlandBonus` 的字段声明 + 5 行注释 + 4 行被注释的 `.defineInRange` | 字段（server config 仍 175 键 / 60 组） | D-254 |
| 4 | `outpost/OutpostCoreIndex#ownerOf` 整个方法（方法体 13 行 + javadoc） | 方法（失去唯一读者后删除） | D-253 |

**为什么从 0 项变成 4 项**：v0.2.0 撤销耕地时这两个类被刻意留下（D-153、A-62），本版以裁定 #8 撤掉那个决定——`farmland` 包整包消失（225 + 555 = **780** 行），字段声明随两个读者一起删，`ownerOf` 随它唯一的读者一起删。合计删 **881** 行 = 780（两个整文件）+ 101（`FarmstormConfig` 11 + `FarmstormGameEvents` 72 + `OutpostCoreIndex` 18）。数字面仍是一增一减：152 − 2 + 1 = **151**（D-227、D-228、D-253）。注册面 92、语言键 321 × 2、转换配方 21、网络包 2、Mixin 0 一项不减（D-227）。

---

## 4. 红线（7 条）

1. **不许新增、删除任何注册 ID**：注册面 92 冻结（D-227、`BRIEF` §6 第 1 条）。
2. **不许新增任何 config 键**：server 175 / client 3 冻结，裁定 #4 硬编码（D-227、D-241）。
3. **不许改 `PROTOCOL_VERSION`、不许改两个 payload 的字段**：网络包 2 冻结（D-227、`BRIEF` §6 第 3 条）。
4. **不许改 `BotanistTrade.NORMAL` 的 22 项顺序**：等级由位置切片决定（`VillagerTradeEvents.java:44-47`），顺序即 47 号 §2.3 表（D-237、D-239）。
5. **不许动 `OutpostCoreScreen`**：它是 54 格容器界面，不是「核心浮窗」（§0.2、D-240、`BRIEF` §6 第 5 条）。
6. **不许改交易项的 `MIN_PRICE` / `MAX_PRICE` / `MAX_USES` / `COUNT`**：plan 只要求经验分级（D-237、`BRIEF` §6 第 6 条）。
7. **不许改写 `docs/zh/52` ~ `docs/zh/57` 与 `.work/v023-*`**：只引上游，不改上游（D-225、D-226、`BRIEF` §6 第 7 条）。

---

## 5. 墓碑（已撤回 / 已翻案的旧口径）

本版有 **1 条墓碑**：A-62（v0.2.0 的裁决，出处 36 号 §10 末）。

### 5.1 A-62 · `outpost_core.farmlandBonus` 只注释定义、保留字段声明 —— **已撤回**

| 项 | 内容 |
|---|---|
| 原文结论（原文照录） | 注释 `config/FarmstormConfig.java` 里 `outpost_core.farmlandBonus` 的**定义**（`:909-912`），**保留字段声明**（`:227`）。 |
| 当时的成立条件 | 「保留声明是让 `FarmlandGuard.java:75` 与 `FarmlandLedger.java:305` 两处 `.get()` 仍能编译的唯一做法」（36 号 §10 末 A-62 的「理由」行）。 |
| **撤回原因** | **裁定 #8 删除了它服务的两个类，前提消失**：`FarmlandGuard` / `FarmlandLedger` 已不存在（D-253）⇒ 字段恒为 `null`、无读者，「保声明换编译」换不到任何东西。 |
| 现在按谁 | **D-254**：字段声明、字段上方 4 行注释、定义处 1 行注释、4 行被注释的 `.defineInRange` 一并删除。 |
| 一并失效 | A-62 的否决项 ③「删掉两个类（删了就没有回头路）」——两个类的「回头路」本版明确不走（D-253）。 |
| 不追溯 | 35 号 `:589`、36 号 `:330-331`、38 号 `:182-183`、39 号 `:242` / `:569` 的论证记的是 v0.2.0 当时状态，按「只引上游、不改上游」一字不动（§8 第 13 条）。 |

### 5.2 三条不构成翻案的地方

1. **容易误读之一**：`FarmstormTags.PROCESSING_BLOCK` 的判据（D-194）**继续有效**，本版只是把「用它做候选发现」补上——判据没改，用法扩了（D-230、D-233）。
2. **容易误读之二**：培养器与转换器的四道准入闸（52 号 D-208、D-218）**一行不改**，客户端判据是**预测面**的新分支，不是准入面的改动（D-244）。
3. **唯一改了文字的地方**：`FarmstormTags.java:29` 的注释（1 个成员 → 2 个成员）。它订正的是**与代码不符的注释**，不是口径（D-248）。

---

## 6. 已否决的方案（8 条）

| # | 方案 | 否决理由 |
|---|---|---|
| 1 | 在 `CoreBound` 接口之外搞两级方案：先立 `AbstractBoundBlockEntity` 基类，再让两个方块实体继承 | 两个状态机不同轴（转换器的 `processTicks()` 与培养器的 `cycleTicks` 各自成体系，`CropConverterBlockEntity.java:282` / `CultivatorBlockEntity.java:54`），共享基类会强迫它们共享字段；接口只要三个方法就够（D-228、D-234） |
| 2 | 经验取原版 SELL 全量：1 / 5 / 10 / 15 / 30（把 1 级也改成 1） | 裁定 #2 明确 1 级保留现值 **2**；改成 1 会让 1 级交易比现状更便宜，那不是 plan 要求的「分级」（D-236） |
| 3 | 给浮窗挂 client config 新键 `hud.opacity` | 裁定 #4 硬编码；client config 3 键冻结（`FarmstormClientConfig.java:25-27`），新增键还要配语言键，321 × 2 当场破（D-241、D-227） |
| 4 | 把「30%」按透明度读，alpha 取 `0xB3` | 0.70 × 255 = 178.5 ≈ 179 = `0xB3`（70.2%），比现状 50.2% 更不透明，与 plan 的「增加透明度」相反（D-241） |
| 5 | 保留两次按类枚举，只给清理出口补一个转换器分支 | 下一个标签成员会拿到同一个 bug 的第三份副本；裁定 #1 要的是按标签发现（D-230、D-233） |
| 6 | 客户端判据复用 `canInsert` | `canInsert` 含 `isUsable()`，`core()` 在客户端要求 `ServerLevel`（`CropConverterBlockEntity.java:232-238`）⇒ 客户端恒假 ⇒ 右键整台机器没反应（D-244） |
| 7 | 换 `SoundType` 或补一条模组私有 place 音来「修」音效 | 根因是客户端预测被掐断，不是音效资源；换音效会改掉现有全部放置音，新增事件还会加一个 `sound_event` 注册项、破注册面 92（D-246、D-227） |
| 8 | 把据点核心的音效修复推迟到下一版 | 裁定 #6 明确三项全做；三处同根因，只修两处会让同类现象时有时无（D-245、D-249） |

---

## 7. 与 plan 的口径差异（5 条）

**读法**：口径差异**不是笔误、不是「plan 写错了」**——它是 plan 的文字与作者裁定（或与源码事实）指向不同行为的那些点。处理口径差异只有两个动作：**按裁定实现** + **在本文登记**。三个禁止：不许照抄 plan 的措辞当行为描述、不许把差异当 bug 修、不许改 plan 原文。

| # | plan 原文 | 差异 | 处置 |
|---|---|---|---|
| 1 | `:8`「本版本将所有带有farmstorm:processing_block标签的的方块与据点核心绑定的逻辑统一」 | plan 只说「**绑定**逻辑统一」，而本 bug 的缺口在**清理出口**（解绑侧，`OutpostCoreBlockEntity.java:866-873`）；「统一」在代码里也不是一处：绑定分三条路径（放置 / 补绑 / 迁移改绑） | 按裁定 #1 落地「按标签发现 + `CoreBound` 接口 + 清理出口合并」，四条路径全改并登记为本版范围（D-230 ~ D-233）；原文的「的的」按笔误处理，不写进口径 |
| 2 | `:16`「为核心浮窗增加背景透明度」 | 「核心浮窗」没有定义，仓里两个候选：`CoreHudRenderer`（HUD 叠加层）与 `OutpostCoreScreen`（54 格容器界面，`OutpostCoreScreen.java:21`） | 钉死为 `CoreHudRenderer`（D-240），`OutpostCoreScreen` 不动（红线 5） |
| 3 | `:16`「默认为30%」 | 代码里没有 30% 对应的常量，现状不透明度已是 50.2%（`CoreHudRenderer.java:40`）；「30%」可读成不透明度或透明度，两个读法差 102/255（`0x4D` ↔ `0xB3`） | 按裁定 #3 读作**不透明度** ⇒ alpha `0x4D`；按裁定 #4 硬编码 `0x4D000000`，不加 config 键（D-241、§6 第 4 条） |
| 4 | `:20`「目前当对一个农作物转换器或农作物培养器右键放置方块时音效会丢失」 | 「音效丢失」的字面读法指向音效资源或配置，实际根因是客户端 `useWithoutItem` 无条件 `SUCCESS`（`CropConverterBlock.java:121-122`、`CultivatorBlock.java:117-118`）掐断了客户端的放置预测；音效资源一行都没错（`FarmstormBlocks.java:78`、`:108` 都是 `SoundType.ANVIL`） | 按裁定 #5 改客户端判据（D-243、D-244），并显式禁止改音效配置（D-246）；「只有放置者本人听不见」写进已知影响（D-251） |
| 5 | `:5`「除非本文档明确要求，否则不能修改0.2.3原有内容」 | 第 ① ③ ④ 条的对象就是 0.2.3 自己建立的东西（清理出口、HUD 常量、三个 `useWithoutItem`） | 4 条需求 + 裁定 #6 的三项算「明确要求」；为落地它们所必需的连带改动（Java 文件 152 − 2 + 1 = 151、注释订正）不算越界，逐条登记在 §3（D-226、D-227） |

---

## 8. 遗留登记

| # | 事项 | 现状 / 约束 | 归属 |
|---|---|---|---|
| 1 | 转换器的 `syncCore()` 要新写一份（该类当前无此方法） | 与培养器同形、各写一份私有方法，不抽基类 | 由 D-247 定 |
| 2 | 据点核心的客户端谓词若作者改判 | 本文补的是「手持 `BlockItem` ⇒ `PASS`」 | 由 D-245 定（本文补充，作者可否决） |
| 3 | 己方玩家手持方块右键核时的预测与开界面落差 | 两侧都按现有分支走，收尾方式本文不预判 | 由 D-245 定；60 号写成已知影响 |
| 4 | 新增标签成员时必须同表登记 kind | 否则只能被发现、不能写 kind | 由 D-235 定（本文补充，作者可否决） |
| 5 | 客户端 `convertible()` 的配方表可用性 | 静态读链成立，未进游戏验证 | 由 D-244 定；63 号写成必须实测 |
| 6 | 「只有放置者听不见」「副手放置」两条现象 | 由原版两条播放路径推出，未双客户端实测 | 由 D-243 定；63 号写成必须实测 |
| 7 | 检查器 WARN 1（6 个 config 键的文档-源码形状偏差） | v0.2.3 已登记的遗留，本版不触碰 | 由 D-227 定；不分配实现任务 |
| 8 | 四个文档下限的具体条目与编号 | 只给下限与归属文档 | 由 D-250 定 |
| 9 | `FarmstormTags.java:29` 订正后的措辞 | 约束：指向 D-194 与标签产物、不写「裁定 #5」 | 由 D-248 定 |
| 10 | `processing_block` 标签产物是否要重新 datagen | 成员未变，产物应不变 | 由 D-227 定；60 号确认 |
| 11 | `outpost/OutpostCoreIndex#ownerOf` **已随本版删除** | 它是「坐标 → 业主」的反查原语，唯一读者 `FarmlandGuard#inOwnRange` 随 `farmland` 包消失后全树 0 调用者 ⇒ 方法体与 javadoc 一并删除。性质 = **本版删除的连带产物**（删除事实挂在 D-253 的落地位置） | 已删除，不进下一版候选 |
| 12 | `outpost/OutpostCoreIndex#isOwnerOrAlly`（`:119`）有意留空 | 方法自己的 javadoc 写着「本方法**只有定义、没有调用者**，且必须保持这样」（A-42 / 31 号 F-189）。性质 = **按设计就应该没有调用者** | 一律不动；下一版评估孤儿 API 时只考虑「刚失去读者」的那一种（本版那一条已直接删掉，见第 11 条），「按设计就该没有调用者」的这一种不评估 |
| 13 | v0.2.0 的 A-62 及其论证不再反映现状 | 35 号 `:589`、36 号 `:330-331`、38 号 `:182-183`、39 号 `:242` / `:569` 记的是 v0.2.0 当时状态，**不追溯修改** | 读者以本文 **D-254** 与 §5.1 墓碑为准 |

---

**收尾自检（本文对 §0.3 九条纪律的逐条落点）**：不用第二人称敬语、一律用「作者」（全文）；无未定事项与任务清单标记（§8 每条都落在一个 D 号或明确的下版评估面上）；表格首尾有 `|` 且逐行列数一致（§0.1 / §0.3 / D-226 与 D-227 的补充表 / §2 / §3.1 ~ §3.3 / §6 / §7 / §8）；数字全部是加减法或两数算术（D-227 表、D-241 的 0.30 × 255 ≈ 77）；每条决策四字段齐全（D-225 ~ D-254 共 30 条）；§5 有墓碑 1 条（A-62，由 D-254 取代）；只引上游（`plan0.2.4.txt` / `BRIEF` / 52 号 / 源码），未改任何既有文件。
