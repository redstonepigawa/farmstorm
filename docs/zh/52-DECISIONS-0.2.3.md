# 52 号 v0.2.3 决策记录

> 项目 / 版本 0.2.3 / 基线 0.2.2 已实现态（git `79b71f1`）/ 依据（plan0.2.3.txt 5 条 + BRIEF §2 的 18 条裁定）/ 唯一真相源
> 依据补充：`plan0.2.3.txt` 5 条需求的原文位置——`:7-17` 转换器重做、`:18-21` 去掉改良作物种子、`:22-28` 培养器修改、`:29-32` man瓜炮弹寿命、`:33-36` 进度文案；`.work/v023-BRIEF.md` §2 的 **18 条**作者裁定（逐字转写在本文 §2）
> 地位：**唯一真相源**。v0.2.3 的一切口径以本文为准；下游 **53（设计）/ 54（技术）/ 55（任务）/ 56（主提示词）/ 57（阶段提示词）只引本文，不自己发明**。本文与源码冲突时报给作者，不得自行取舍。
> 覆盖：**D-203 ~ D-224，共 22 条**，连续无跳号。对 0.2.2 的增 / 改 / 删登记在 §3，红线在 §4，已否决的方案在 §6，与 plan 的口径差异在 §7。

---

## 0. 阅读约定

### 0.1 编号（D-203 起；接续表）

本文的决策编号从 **D-203** 起（0.2.2 的 46 号到 D-202）。其余编号系列本文**只引用、不新开**：

| 系列 | 0.2.2 到 | **本文从** | 由谁写 |
|---|---|---|---|
| 文档号 | 51 | **52** | 本文 |
| 决策 D-xx | D-202 | **D-203** | 本文（52 号） |
| 任务 F-xx | F-285 | **F-286** | 55 号 |
| Phase / 里程碑 | 53 / M49 | **54 / M50** | 55 号 |
| 硬约束 HC-xx | HC-155 | **HC-156** | 56 号 |
| 禁止行为 N-xx | N-140 | **N-141** | 56 号 |
| 风险 RT-xx | RT-70 | **RT-71** | 54 号 |
| 裁决 A-xx | A-91 | **A-92** | 55 号 |

四条编号规则：

1. 本文只发 **D-xx（D-203 ~ D-224）**。引用其它系列时写全前缀，不自己编号、不预支下游编号。
2. 0.2.2 的 D-187 ~ D-202 仍以 46 号为准，**除非 §5 点名**；§5 没点到的一律继续有效。
3. 本文**不新立裁决**：`BRIEF` §2 的 18 条（#1 ~ #18）是作者拍板的，下游不得改写、只能引用；需要作者补充拍板的新条目由 55 号从 **A-92** 起登记。
4. **`BRIEF` §2 #2 与 #17 是两条独立裁定**：前者删下界之星门槛，后者因此删掉靠它成立的进度。下游引用时必须分开写，不许合并成「删下界之星及相关进度」。

### 0.2 术语表

本节给每个术语**判定边界**，不是同义词循环。下游五份文档遇到这些词，一律按本节的边界用。

**农作物转换器**
：本版新增的功能方块 `farmstorm:crop_converter`，继承 0.2.2 的 `farmstorm:converter` 的**位置**（同一个方块实体、同一条战利品表槽位），但**注册 ID、外观、行为、交互全部换掉**。判定边界：它**不是**「改造后的旧转换器」——旧的 `farmstorm:converter` 在本版**不存在**（裁定 #1 选「改为」，不是「并存」）；它**不是**「农产品转换器」的别名，中文名从「农产品转换器」改为「**农作物转换器**」；它与培养器是**两台不同的机器**，不是一个方块的两个模式。

**转换配方（单向）**
：`farmstorm:converting` 类型的数据，本版起**只有正向一个方向**：输入是原版物品或平凡作物，输出是对应的改良作物。判定边界：**不是**双向的（0.2.2 的 `twoWay` 在本版全部单向化）；**不收**经验、**不收**催化剂（下界之星整条删除）；**不由** GUI 触发——它由方块的右键放入 + 30 秒计时触发。

**改良作物**
：22 条 `CropProfile` 档案对应的物品（`CropProfile.ALL`）。判定边界：本版起它**不再有播种物**（裁定 #2），**不能**被培养器收（裁定 #9/#10），只能由**转换器产出**或**植物学家交易**或**创造栏**获得。它与「平凡作物」是两个不同的东西。

**平凡作物**
：本版起可种植的一类作物，当前唯一成员是**平凡韭菜**（`farmstorm:plain_chives`）。判定边界：它**没有 `CropProfile`**（裁定 #14），因此**不进** `CropProfile.ALL` / `LUCKY_BERRY_POOL` / `#farmstorm:modified_crops`；它有**自己的方块与种子**（`plain_chives_crop` / `plain_chives_seeds`），且**种子可以播种**；它能被培养器收（裁定 #9）。

**投入物**
：培养器能收的那批物品——21 条转换配方的**输入侧**减去 4 条史诗输入，共 **17** 个。判定边界：投入物是**物品**不是方块；判据来自**配方表**而不是手写白名单，因此改一条配方或改一条 tier 会自动跟着变（RT-49 的同一条纪律）。

**对应种子**
：一个投入物「用来种出它自己的那个物品」。判定边界：**自身即种子**的投入物（胡萝卜 / 马铃薯 / 仙人掌 / 竹子 / 蘑菇 / 可可豆 / 甘蔗 / 菌）的对应种子**就是它自己**（裁定 #16），因此产出是 2 + 2 = **4 个同一物品**；小麦 / 甜菜根 / 西瓜片 / 西瓜 / 南瓜 / 平凡韭菜的对应种子是**另一个物品**，产出是 2 个投入物 + 2 个种子。

**工作态**
：方块状态属性 `working` 为 `true` 的那一档。判定边界：工作态**只**表示「有货且能推进计时」，**不**表示「一定产得出来」（核满时 `working` 会落回 `false` 但槽与进度都保留）；动画的播放**由模型切换承担**，不靠代码暂停（陷阱：动态 PNG 一旦被引用就会自己播）。

**轻量路径**
：为平凡作物选定的实现方式——**不进 `CropProfile`**，改用独立的方块类与种子类。判定边界：轻量路径**不是**「临时方案」，本版就是最终形态；它**不是**「不写数据」，而是「数据写在方块类自己身上」；它的代价是**每一处需要认识平凡作物的地方都得显式写一条**，这个代价由 54 号的清单兜住。

### 0.3 四字段写法

每条决策固定四项，**缺一不可**，顺序固定：

| 字段 | 写什么 | 不许写什么 |
|---|---|---|
| **结论** | 一句话、祈使句、可直接执行 | 条件句、备选句、「可以考虑」 |
| **依据** | `BRIEF` §2 的裁定编号，或 §1 / §3 / 源码的 `文件:行号`；两者都给最好 | 「因为合理」「符合直觉」「业界惯例」 |
| **落地位置** | 要改的 `路径` 或 `类#方法` | 代码片段、算法步骤 |
| **补充** | 这一条**连带会影响什么**、**否决掉了哪些替代方案** | 实现细节（那是 54 号的事）、任务排期（那是 55 号的事） |

五条写作纪律：

1. **「本文补充」标记**：凡由本文推导、而非 `BRIEF` 直接给出的口径，在该句前标 **本文补充**。带这个标记的条目可以被作者否决。
2. **数字纪律**：所有数值只能追到 `BRIEF` §0 / §2 / §3 的某一条，或两个已给数值的算术（98 − 7 + 1 + 1 − 1 = 92、176 − 3 + 2 = 175、340 − 23 + 4 = 321、154 − 3 − 1 + 2 = 152）。需要新数值的写成「由 53 / 54 号定」，本文不自己编。
3. **禁用词**：不出现未定事项与待办标记；不用第二人称敬语，一律用「作者」。
4. **与源码的关系**：本文与源码冲突时报给作者，不自行取舍，也不改 `BRIEF`、不改任何既有文档。
5. **只写决策**：不写实现步骤、不写任务拆解、不写排期、不写验收细则。

---

## 1. 决策清单（D-203 ~ D-224）

本版编号 **D-203 ~ D-224，共 22 条**，连续无跳号。分组：§1.1 版本与范围 3 条（D-203 ~ D-205）、§1.2 农作物转换器 6 条（D-206 ~ D-211）、§1.3 种子与作物方块 3 条（D-212 ~ D-214）、§1.4 平凡作物 3 条（D-215 ~ D-217）、§1.5 培养器 3 条（D-218 ~ D-220）、§1.6 炮弹与文案 2 条（D-221 ~ D-222）、§1.7 口径同步 2 条（D-223 ~ D-224）。

### 1.1 版本与范围（D-203 ~ D-205）

#### D-203 · 版本号 0.2.3，基线 = 0.2.2 已实现态

- **编号**：D-203
- **结论**：把 `mod_version` 改为 **0.2.3**，以 **0.2.2 已实现态**（git `79b71f1`）为基线；本版交付 **52 ~ 57 六份中文**，不做英译。
- **依据**：裁定 #13（文档流程照旧：编号 52~57 / 只出中文 / README 增量）；`plan0.2.3.txt:2`「版本号：0.2.3」、`:3`「使用neoforge模组加载器」、`:4`「minecraft本体版本：1.21.1」；`BRIEF` §0.1（基线实测：注册面 98、config 176 键、语言键 340 × 2、检查器 FAIL 0）。
- **落地位置**：`gradle.properties:11`；`一键构建.cmd`、`检查环境.cmd` 的 6 处版本串；`docs/zh/52` ~ `docs/zh/57`（6 份新增）；`README.md`（增量段）。
- **补充**：基线口径必须写死成「**已实现**」，不能写「已发布」。0.2.2 的代码全部落地了（`plain_chives` / `BotanistTrade` 都在源码里），但 46 号写的是「基线 0.2.1 已发布」。两者的差别在**可引用面**：0.2.2 的决策与源码**都能引**，0.2.1 当时只能引决策。`plan0.2.3.txt:5` 的「本版本以0.2.2版本为主」是**基线口径**（在 0.2.2 之上追加），不是「本版版本号 0.2.2」，两者不得混读。

#### D-204 · 本版只做 plan 的 5 条，转换器从「搁置」转为「执行」

- **编号**：D-204
- **结论**：把本版范围钉死为 plan 的 **5 条需求**，外加使它们成立所必需的版本号、注册面、配置面与检查器同步；其余 0.2.2 行为一行不改。
- **依据**：`plan0.2.3.txt:5`「除非本文档明确要求，否则不能修改0.2.2原有内容」；裁定 #1 ~ #18；`BRIEF` §5 的 19 项必改清单；`BRIEF` §6 的 7 条红线。
- **落地位置**：全文；`src/` 的改动面以 §3 三张表为准（表外的文件一律不动）。
- **补充**：**这条与 0.2.2 的 D-188 是同一把闸，但清单换了**。0.2.2 把「转换器重做」整条挡在门外（裁定 #5：搁置到 0.2.3），本版把它**放进来**——46 号 §3.3 与 §6 第 1 条明确写着「不是删除、不是否决，留到 0.2.3」。因此本版不是推翻 0.2.2 的裁定，而是**兑现**它。
  plan 的 5 个编号块与决策的对应：

| # | plan 的原文位置 | 需求 | 决策 |
|---|---|---|---|
| ① | `:7-17` | 农产品转换器重做 | D-206 ~ D-211 |
| ② | `:18-21` | 改良农作物不再拥有种子 | D-212 ~ D-214 |
| ③ | `:22-28` | 农作物培养器修改 | D-218 ~ D-220 |
| ④ | `:29-32` | man瓜炮弹 300 刻后删除 | D-221 |
| ⑤ | `:33-36` | 进度「最扎人的一集」描述改字 | D-222 |
| 附 | `:2-5` | 版本号 / 加载器 / MC 版本 / 基线口径 | D-203 |

- **补充（续）**：`:5` 的「不能修改 0.2.2 原有内容」**在本版必然被突破**——第 ② 条与第 ③ 条的对象就是 0.2.2 自己建立的东西（7 个种子、培养器的准入判据）。本文的读法是：上述 5 条算「明确要求」，**为落地它们所必需的连带改动**（注册改名、自检数字、检查器清单、语言键增减）不算越界，但必须在 §3 逐条登记。**这条读法本身是本文补充**，作者可否决。

#### D-205 · 注册面 98 → 92，口径外不变

- **编号**：D-205
- **结论**：注册对象从 **98** 降到 **92**；`items` 34 → 28、`blocks` 27 → 28、`menu_type` 2 → 1，其余九类不变；**口径外仍是 3 类**，本版不新增也不删除。
- **依据**：裁定 #2（删菜单）、#7（加平凡作物方块与种子）、#14（平凡韭菜不进档案）；`BRIEF` §3.1 的逐类表；`Farmstorm.java:180-183` 的现值为 `total == 98` / `items == 34` / `blocks == 27` / `menuTypes == 2`；`tools/verify_project.py:334-393` 的 `EXPECTED` 与 `EXPECTED_COUNTS`。
- **落地位置**：`Farmstorm.java:180-183`；`tools/verify_project.py:334-393`；`FarmstormBlocks` / `FarmstormItems` / `FarmstormMenus` / `FarmstormBlockEntities` 四个注册类。
- **补充**：逐类算式（**这是本版唯一一处允许出现的新数字，全部是加减法**）：

| 类 | 现 | 增 | 减 | 后 |
|---|---|---|---|---|
| `items` | 34 | +1（`plain_chives_seeds`） | −7（`puffed_wheat_seeds` / `power_beet_seeds` / `melon_grenade_seeds` / `pumpkin_turret_seeds` / `awakened_torchflower_seeds` / `awakened_pitcher_plant_pod` / `chive_seeds`） | **28** |
| `blocks` | 27 | +1（`plain_chives_crop`） | 0 | **28** |
| `block_entity_type` | 8 | 0 | 0 | **8** |
| `entity_type` | 13 | 0 | 0 | **13** |
| `mob_effect` | 4 | 0 | 0 | **4** |
| `sound_event` | 3 | 0 | 0 | **3** |
| `recipe_type` | 1 | 0 | 0 | **1** |
| `recipe_serializer` | 1 | 0 | 0 | **1** |
| `creative_mode_tab` | 1 | 0 | 0 | **1** |
| `damage_type` | 3 | 0 | 0 | **3** |
| `data_component_type` | 1 | 0 | 0 | **1** |
| `menu_type` | 2 | 0 | −1（`converter`） | **1** |
| **合计** | **98** | | | **92** |

  **`converter` → `crop_converter` 是改名，不计数**（裁定 #1 选了「改为」而不是「并存」）——方块、物品、方块实体三处都是同一个注册对象换了个 id，条目数不变。这一条最容易数错：如果按「删一个加一个」算，`blocks` 会得出 29，与实际差 1。
  **口径外 3 类不变**（`ATTACHMENT_TYPES` 1 + `VillagerProfession` 1 + `PoiType` 1，46 号 §1.3 已登记）。本版不碰食物、不碰职业、不碰 POI。平凡作物**不新增**口径外类型：它的方块走 `BLOCK` 注册表、种子走 `ITEM` 注册表，两者都在 12 类里。

### 1.2 农作物转换器（D-206 ~ D-211）

#### D-206 · 方块改名为 `farmstorm:crop_converter`，旧 id 下线不并存

- **编号**：D-206
- **结论**：把方块、方块物品、方块实体三处的注册 ID 从 `converter` 改为 **`crop_converter`**；旧的 `farmstorm:converter` **不再注册**，不保留兼容别名。
- **依据**：裁定 #1（**改为 `crop_converter`**，不并存）；`BRIEF` §1.1（现 `FarmstormBlocks.java:71-72`、`FarmstormItems.java:112-113`）；贴图包的命名是 `crop_converter_a_*`（`BRIEF` §4.3）。
- **落地位置**：`FarmstormBlocks.java:71-72`；`FarmstormItems.java:112-113`；`FarmstormBlockEntities`（`CONVERTER` → `CROP_CONVERTER`）；`ModLootTableProvider` 的战利品表路径；`ModBlockStateProvider:160-185`；`ModItemModelProvider:69`。
- **补充**：**这是本版唯一一处有损的注册变更**，代价必须写进 README 的升级须知而不是悄悄带过：
  1. 老存档里已放置的 `farmstorm:converter` 方块读档后变成**缺失方块**（`blocks` 注册表里没有这个 id）；
  2. 老存档背包里的 `farmstorm:converter` 物品**失效**；
  3. 已经完成「装个转换器」进度的玩家**进度不丢**（进度 id 仍是 `farmstorm:converter`，见 D-214 的补充），但该进度从此与新方块无关联。
  **减免这条代价的因素**：`ConverterBlock.java:84-86` 记着 D-170 / 裁定 #19——转换器自 v0.2.0 起就是**装饰方块**（`useWithoutItem` 返回 `PASS`），GUI 已经打不开。因此「已经放下的机器失去功能」这件事在 0.2.0 就已经发生过一次，本版只是把它彻底化。
  否决「两个 id 并存」：会让注册面 +2 而不是 +1，且 `#farmstorm:processing_block` 里要同时放两个方块，`isProcessingBlock` 的语义变模糊。

#### D-207 · 方块属性逐项照铁砧七项

- **编号**：D-207
- **结论**：`crop_converter` 的 `BlockBehaviour.Properties` **逐项照抄原版铁砧**：`MapColor.METAL` / `strength(5.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `SoundType.ANVIL` / `pushReaction(PushReaction.BLOCK)`。
- **依据**：`plan0.2.3.txt:10`「方块硬度和爆炸抗性参考铁砧」；原版 `Blocks.java` 的 `ANVIL` = `.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F)`；**培养器已经照抄过同样的七行**（`FarmstormBlocks.java:95-103`），本版直接对齐它。
- **落地位置**：`FarmstormBlocks.java:71-72`。
- **补充**：现状的转换器是 `.strength(3.5F)` + `MapColor.STONE`、**没有** `sound` / `pushReaction`，本版是一次**属性面的整体替换**，不是调两个数。
  **七项的具体出处**（54 号写技术规格时逐项列）：`mapColor` / `strength(hardness, resistance)` / `requiresCorrectToolForDrops` / `sound` / `pushReaction` 五项来自铁砧；`MapColor.METAL` 与 `SoundType.ANVIL` 是它的两个取值。**本版不新增第六、第七项**——`BlockBehaviour` 的其它成员（`noOcclusion` / `lightLevel` / `randomTicks`）一律不设，保持与培养器同形。

#### D-208 · 机器的形态：单槽、无界面、无成本、一次一个、三十秒、自动进核

- **编号**：D-208
- **结论**：`crop_converter` 是一台**没有菜单**的机器：右键把手上**一个**原版物品或平凡作物放进唯一的槽；方块在满足「已绑定 ∧ 核侧认 ∧ 3 × 3 区块内」时开始计 30 秒；到点产出 **1 个**对应改良作物并**自动投递进绑定核心**；同一时刻只放得进一个。
- **依据**：裁定 #2（删 GUI / 经验 / 下界之星）、#5（边界行为照培养器先例）、#9（培养器产出形态另见 D-219）；`plan0.2.3.txt:12` 原文「手持有对应改良农作物的原版农作物或平凡农作物右键可将其放入该方块中转换，一次只能放入一个，三十秒后产出1个对应改良农作物并自动存放在其绑定的据点核心的存储空间中」。
- **落地位置**：`CropConverterBlock`（`ConverterBlock` 改名）；`CropConverterBlockEntity`（`ConverterBlockEntity` 重写）；`ConverterRecipe`（字段收缩，见 D-209）。
- **补充**：**这是本版改动量最大的一条**——`ConverterBlockEntity` 现有 435 行、`implements Container`、双槽（输入 + 催化剂）、`ContainerData`、配方缓存代际、经验与催化剂校验，**全部推倒**。新的状态机与培养器**同形**（`CultivatorBlockEntity.serverTick` 的四步），因此 54 号的技术规格应当写成「培养器的状态机换一个产出函数」，而不是重写一份。
  四条边界口径**直接沿用培养器**，不重新讨论：
  1. **拆方块** → 槽里的输入物掉落（`releaseHeld()` 同款四条件）；
  2. **核满** → 产物不丢，槽与进度都保留，`working` 落回 `false`，核腾出空间的那一 tick 自动完成投递；
  3. **工作中右键** → 无反应（`PASS`），不能取回；
  4. **绑定失效**（核被拆 / 核侧列表里没有它）→ 留原地、内容保留、不可使用。
  **不实现 `Container`**：与培养器同一条理由（36 号 §2.5.2），一旦实现，漏斗 / 投掷器就能绕过 `useWithoutItem` 塞东西进去。
  否决「保留经验消耗当门槛」：裁定 #2 明确删，且新机器没有界面显示「本次要扣多少经验」——没有显示的成本等于隐藏惩罚。

#### D-209 · 转换配方单向化：保留类型、26 → 21、全部无成本

- **编号**：D-209
- **结论**：保留 `farmstorm:converting` 配方类型与序列化器（`recipe_type` / `recipe_serializer` 各 1 不变），配方从 **26 条降到 21 条**，全部**只保留正向**（原版/平凡 → 改良），**不收经验、不收催化剂**；`ConverterRecipe` 删掉 `xpCost` / `catalyst` / `catalystCount` / `bidirectional` / `reverseXpCost` 五个字段与 `reverse()` 方法。
- **依据**：裁定 #3（保留类型、只留正向）、#2（删经验与下界之星）、#15（新增平凡韭菜 → 朋友的韭）；`ModRecipeProvider.java:67-99` 的 26 条（20 作物本体 + 6 种子）；`:199-209` 的 `oneWayWithNetherStar`；`ConverterRecipe.java:17-23,93-107`。
- **落地位置**：`ConverterRecipe.java`；`ConverterRecipeSerializer.java`；`ModRecipeProvider.java:67-99,187-209`。
- **补充**：21 条的算式是 **26 − 6 + 1 = 21**：减掉 6 条种子双向（D-212 删了种子），加上 1 条平凡韭菜。**逐条清单由 54 号列**，本文只钉三个口径：
  1. **两条史诗配方保持单向但去掉催化剂**——火把花 / 瓶子草原本就是 `oneWayWithNetherStar`，本版把 `Ingredient.of(Items.NETHER_STAR)` 换成 `Ingredient.EMPTY`、`NETHER_STAR_COST` 换成 `0`，方向不变；
  2. **`twoWay` 一律改成单向**，方法是把 `bidirectional` 传 `false`（字段最终会被删，中间态要先能编译）；
  3. **平凡的韭没有平凡对应物，也没有原版对应物**——它在本版**不新增**转换配方，仍然只能靠植物学家交易（46 号 D-195 的既有口径，本版不动）。
  **为什么保留配方类型而不是改成代码里的静态表**：`BRIEF` §4.3 的映射表（原版物品 → 改良作物）**已经用配方表达过一遍**，再写第二份静态表就是同一事实的两个副本，改一条 tier 或加一个作物必然漏一边（0.2.2 的 RT-49 踩过这个坑）。保留类型的代价是 `recipe_type` 与 `recipe_serializer` 各占 1 个注册额——本版不差这 2 个。
  ⚠️ **兼容口径**：`ConverterRecipe` 删字段会改变 JSON schema，**旧数据包里带 `xp_cost` / `catalyst` 字段的 `converting` 配方会读失败**。本版按「不写迁移、不写向后兼容 codec」处理，理由是本模组没有发布过带 `converting` 配方的外部数据包；这一条要在 54 号的 §4.8.5 明确写成已知限制。

#### D-210 · 红色连线：给 `Link` 加类型字节，不在渲染期反查方块

- **编号**：D-210
- **结论**：给 `CultivatorLinkPayload.Link` 增加一个 `Kind` 字段（`CULTIVATOR` / `CROP_CONVERTER`），渲染时按 `Kind` 选颜色：培养器保持现在的青色，转换器用**红色**；网络包数量不变（仍是 2 个）。
- **依据**：裁定 #5（连线红色）；`plan0.2.3.txt:12`「与据点核心的连线为红色（与农作物培养器和据点核心之间的蓝色连线相对应）」；`CultivatorLinkRenderer.java:41-44`（颜色硬编码 `0.30 / 0.85 / 0.90`）；`CultivatorLinkPayload.java:53`（`record Link(BlockPos core, BlockPos cultivator)`）。
- **落地位置**：`CultivatorLinkPayload.java`；`CultivatorLinkBroadcaster.java`；`ClientCultivatorLinks.java`；`CultivatorLinkRenderer.java`；`OutpostCoreBlockEntity` 的广播入口。
- **补充**：**这是本版唯一的协议变更**。四条约束：
  1. **不加包、不改包 id**——`Link` 的 stream codec 多一个 `ByteBufCodecs.BYTE`（或等价的枚举 codec），`MAX_LINKS = 1024` 不动；
  2. **不按方块类型在渲染期反查**：`CultivatorLinkRenderer` 手上只有 `BlockPos`，反查要读 `level.getBlockState(...)`，而连线恰恰常在**远端未加载区块**上——那时会随机闪回青色（否决理由写进 §6）；
  3. **红色数值由 54 号定**，本文只钉「红」这个语义与「与青形成对比、不用纯 `0xFF0000`」这个约束（纯红在草地与岩浆上都看不清）；
  4. **培养器的青色一个字节都不许变**——它是 0.2.0 起的既有视觉，改色等于无谓的回归面。
  ⚠️ **`PROTOCOL_VERSION` 必须从 `"2"` 升到 `"3"`**：`Link` 从 2 字段变 3 字段就是 codec 变了，而 `FarmstormNetwork:22-24` 的既有纪律是「codec 一变必须跟着变版本」。不升会「协商通过后用旧 codec 解新流」——混装环境必崩，且崩在客户端而不是握手阶段，极难定位。这条属于 D-210 的**强制连带项**，不是可选项。
  **命名为 `Kind` 而不是 `Color`**：语义是「这条线连着哪一类机器」，颜色是它的后果；将来第三种机器只需要加一个枚举成员，不必再加一个颜色字段。

#### D-211 · 工作时才动：`WORKING` 属性 + 双模型，不写代码暂停

- **编号**：D-211
- **结论**：`crop_converter` 增加布尔属性 `WORKING`，blockstate 按 `FACING`（水平四向）× `WORKING`（两档）生成 **8 个 variant**；`working=false` 引用**静态**贴图，`working=true` 引用**16 帧帧条**。**不得**用任何代码手段去暂停 / 恢复纹理动画。
- **依据**：裁定 #5（照培养器先例）；`plan0.2.3.txt:12`「当其处于工作状态时播放预设好的翻书动画」；`CultivatorBlock.java:47`（`WORKING` 属性）、`ModBlockStateProvider.java:200-221`（两态两模型，`_still` 静态 + 帧条动态）；`BRIEF` §4.3（贴图包 16 帧 / `frametime: 2` / 32 刻一循环）。
- **落地位置**：`CropConverterBlock`（属性与 `getTicker`）；`CropConverterBlockEntity.setWorking`；`ModBlockStateProvider`（新方法 `cropConverterStates`）；贴图落位。
- **补充**：**这条的陷阱与 0.2.0 培养器踩过的完全一样**（D-161 / 陷阱 27）：动态 PNG 一旦被模型引用就会自己播，`frametime` 是贴图元数据，代码碰不到它。「只有工作时才动」的唯一正解是**两张模型**——空闲那张引用一个**没有 mcmeta 的静态副本**。
  贴图落位沿用培养器的命名惯例（`_still` 后缀）：动画 `crop_converter_a_{front,side,top}.png` 各带一个 `.mcmeta`；静态副本 `crop_converter_a_{front,side,top}_still.png`；底面 `crop_converter_a_bottom.png` 只有静态、**不带** mcmeta。
  600 刻里这个动画播 **18.75 个循环**（600 ÷ 32），不整除——这是正常的，到点直接切模型，不需要对齐。
  **「翻书动画」就是贴图包里那套逐帧动画，不是比喻**：`翻书动画`（flipbook animation）指逐帧播放的序列帧，贴图包给的正是这个——正面 / 顶面 / 侧面**各 16 帧**、`32×512` 纵向帧条、`frametime: 2`、`interpolate: false`、1.6 秒一循环（`README.txt` 与三个 `.mcmeta`）。因此 plan 的措辞与素材**完全一致**，不需要另配动画、也不构成口径差异。

### 1.3 种子与作物方块（D-212 ~ D-214）

#### D-212 · 删掉 7 个种子，改良作物不再有播种物

- **编号**：D-212
- **结论**：删除 `puffed_wheat_seeds` / `power_beet_seeds` / `melon_grenade_seeds` / `pumpkin_turret_seeds` / `awakened_torchflower_seeds` / `awakened_pitcher_plant_pod` / `chive_seeds` **全部 7 个**种子物品；22 条档案的 `seedItem` 字段一律置 `null`；`SeedItem` 类**一并删除**（删除后它没有任何使用者，见补充）。
- **依据**：裁定 #2 之外的第 ② 条需求（`plan0.2.3.txt:18-21`）；`BRIEF` §1.2（7 个种子的引用面）；`FarmstormItems.java:91,102-109`。
- **落地位置**：`FarmstormItems.java:91,102-109`；`CropProfile.java`（22 条档案的 `seedItem` 实参）；`ModLangProvider.java:88-93,103`；`ModItemTagsProvider.java:43-48`；`ModLootTableProvider.java:71,81,84,86,89,91,105`；`ModRecipeProvider.java:86-92`；`ModAdvancementProvider.java:96-104`。
- **补充**：**必须先纠正一个容易形成的错判**：0.2.2 的种子**本来就不可播种**（`SeedItem.java:38-41` 的 `useOn` 无条件返回 `PASS`，D-152 ②），15 个改良作物方块**在正常玩法里本来也拿不到**。因此本版删种子**不改变种植能力**，它改变的是四件事：
  1. 物品面少 7 个条目（注册面 34 → 27，再 +1 见 D-215）；
  2. 战利品表少 7 条掉落；
  3. 6 条种子转换配方消失（26 → 20，再 +1 见 D-217）；
  4. `#farmstorm:seeds` 标签成员从 **6 条降到 1 条**（原来 6 条全是这 7 个种子里的 6 条；删完后由 `plain_chives_seeds` 顶上，标签不会空）。
  ⚠️ `FarmstormTags.java` 的类注释写着「6 个独立播种物 + 4 个自身即播种物的作物」，**这半句是错的**：`ModItemTagsProvider.java:42-48` 实际只 add 了 6 个物品，生成的 `data/farmstorm/tags/item/seeds.json` 也只有 6 项。本版顺手把这条注释改对（54 号列进改法清单），不改标签语义。
  **`SeedItem` 类要删，不是留**：7 个种子删掉、22 条档案的 `seedItem` 置 `null` 之后，这个类**一个使用者都没有了**——`CultivatorBlockEntity.profileOf` 的 `instanceof SeedItem` 分支随 D-218 的判据反转一起消失，`FarmstormItems.seedItem(...)` 工厂也随 7 个种子的删除而消失。留着它就是一个 47 行、无引用的孤儿类。
  ⚠️ `CropProfile.seedItem` **字段本身保留**（类型是 `ResourceKey<Item>`，不是 `SeedItem`）：`FarmstormCropBlock.getBaseSeedId()`（`:157-166`）仍然按它反查，且它的 `null` 分支本来就写着 `return Items.AIR`。字段保留、值全 `null`，是这个字段的既有语义，不是新写法。
  **`#farmstorm:seeds` 标签不会空**：`plain_chives_seeds` 顶上去（见 D-216）。
  否决「保留 `SeedItem` 以备将来」：它被 D-152 ② 钉死成不可播种，将来的种子也不会用它。

#### D-213 · 15 个改良作物方块保留但不可获得

- **编号**：D-213
- **结论**：`puffed_wheat_crop` 等 **15 个**改良作物方块**全部保留注册**，不补 `BlockItem`、不改战利品表以外的行为；它们在本版仍然是「命令 / 创造才能放置」的方块。
- **依据**：裁定 #6（保留但不可获得）；`BRIEF` §1.2（这 15 个方块本来就**没有** `BlockItem`，种子的 `useOn` 也是 `PASS`）。
- **落地位置**：`FarmstormBlocks` 的 15 个 `crop(profile)` 注册；`FarmstormCropBlock`（不改）；`ModLootTableProvider:70-105`。
- **补充**：这条**不是新决定，是把现状写清楚**。0.2.2 结束时这 15 个方块就已经是「注册着、正常玩不到」的状态，本版只是**不再假装它们会被种出来**。
  **不补 `BlockItem` 的理由**：补了它们就变成「可获得」，与裁定 #6 直接冲突；而且一个能直接放置的作物方块会绕过 `age` 成长，等于给了一个「一放就是成熟」的捷径。
  **战利品表要改但要小心**：方块破坏时的掉落里，种子那一条（`ModLootTableProvider:71,81,84,86,89,91,105`）**必须删**——它引用的物品已经不存在，不删则 datagen 阶段就会崩（`BuiltInRegistries.ITEM.get(key)` 拿到 `null`）。作物本体的掉落**保留不动**。
  否决「连方块一起删」：`blocks` 会从 27 掉到 12，注册面变动 15 个条目，远超本版范围；且老存档里已放置的作物会变成空气。否决「改由作物物品直接放置」：`ModifiedCropItem` 是投掷/食用/部署三种行为的分支点，加放置语义会把 22 条档案的行为矩阵再乘一维。

#### D-214 · 删两条进度：`seed_cycle` 与 `nether_star_toll`

- **编号**：D-214
- **结论**：删除进度 `farmstorm:seed_cycle`（判据依赖种子）与 `farmstorm:nether_star_toll`（判据依赖下界之星）；进度总数从 **18** 降到 **16**；`farmstorm:converter` 与 `farmstorm:first_conversion` **保留**。
- **依据**：裁定 #8（`seed_cycle` 直接删）、#17（`nether_star_toll` 删）；`ModAdvancementProvider.java:95-106`（`seed_cycle` 的 6 个判据全部是「作物 + 种子」）、`:88-89` 之后的 `nether_star_toll`；`BRIEF` §1.1（下界之星门槛在 D-209 被删）。
- **落地位置**：`ModAdvancementProvider.java`；`ModLangProvider.java`（两条各 2 个语言键）。
- **补充**：两条删除的**理由不同，不许合并写**：
  - `seed_cycle` 是「**判据的素材没了**」——它的 6 个 `addCriterion` 每一条都要求同时持有作物与对应种子，种子删了以后 6 个判据**全部不可达**；
  - `nether_star_toll` 是「**门槛没了**」——它的达成条件是「完成一次消耗下界之星的转换」，而下界之星整条删除（裁定 #2），因此它同样**不可达**。
  **为什么不改成别的判据**：`seed_cycle` 改成「作物 + 对应原版物品」会与 `first_conversion` 的语义重叠（都是「转换过一次」的衍生）；`nether_star_toll` 改成「转换出史诗作物」则名实不符——那时候已经没有代价，「通行费」这个名字会变成谎。作者对 `seed_cycle` 的裁定是「直接删掉」，`nether_star_toll` 按同一处理（裁定 #17）。
  **两条进度 id 不要复用**：删掉的 id 若将来复用，老存档里已完成的玩家会看到「已完成」而实际上没做过新条件。
  **`converter` / `first_conversion` 保留但要点名**：前者的判据是 `placedBlock(FarmstormBlocks.CONVERTER.get())`（`ModAdvancementProvider.java:82-85`），方块改名后这里要跟着换成 `CROP_CONVERTER`；进度 **id 保持 `farmstorm:converter`** 不改——进度 id 存在玩家存档里，改名等于让所有已完成该进度的玩家「退回未完成」。

### 1.4 平凡作物（D-215 ~ D-217）

#### D-215 · 平凡韭菜升级为可种植作物，但不进 `CropProfile`

- **编号**：D-215
- **结论**：`farmstorm:plain_chives`（平凡韭菜）从「普通食物物品」升级为**可种植作物**：新增方块 `farmstorm:plain_chives_crop`（4 个生长阶段）与物品 `farmstorm:plain_chives_seeds`；**`plain_chives` 本身不进 `CropProfile.ALL`**，22 条档案的口径不变。
- **依据**：裁定 #7（把 `plain_chives` 升级成可种植作物）、#14（不进档案，另走轻量路径）、#18（复用朋友的韭的 4 张 stage 图）；`FarmstormItems.java:125-135`（现状：`Item` + 食物 2、无档案）；46 号 D-193（0.2.2 定义它「不是作物、不能种、无种子」）。
- **落地位置**：`FarmstormBlocks`（新方块）；`FarmstormItems`（新种子）；新建 `PlainCropBlock`；`ModBlockStateProvider`（4 阶段 blockstate）；`ModLootTableProvider`（新表）；`ModLangProvider`（2 个新键）。
- **补充**：**这条推翻了 0.2.2 的 D-193 的一半**，必须显式登记（见 §5 墓碑第 1 条）：D-193 当时定「没有 `CropProfile`、不是作物、不能种、无种子」，本版把后三项翻掉，只留下「没有 `CropProfile`」。
  **为什么仍然不进档案**：`CropProfile.ALL` 是 22 条，`LUCKY_BERRY_POOL` 从它推导（`CropProfile.java` 的 `ALL.stream().filter(tier() == COMMON)`），`#farmstorm:modified_crops` 标签也按档案生成。把平凡韭菜塞进去会同时污染三处：它会变成「幸运浆果能抽到的奖品」、会进「改良作物」标签、会让 `Farmstorm.java` 的 22 条自检失效。
  **轻量路径的具体形态**（54 号展开）：`PlainCropBlock extends CropBlock` 自管 `age 0~3`、四个碰撞箱、生长计时（读同一组 `growth.*` config），**不复用** `FarmstormCropBlock`——后者的构造器与 codec 都强制要一个非空 `CropProfile`（`FarmstormCropBlock.java:43-46,57-60`）。
  **贴图复用**：`plain_chives_crop` 的 4 个阶段直接引用 `friend_chives_stage0~3.png`（裁定 #18），不新增美术资源。方块名与文件名不同，因此 blockstate 生成时要显式写贴图路径，不能走「同名贴图」的兜底分支。

#### D-216 · 平凡韭菜种子必须可播种，且不复用 `SeedItem`

- **编号**：D-216
- **结论**：新建 `PlainSeedItem extends ItemNameBlockItem`，`plain_chives_seeds` 用它注册；**`useOn` 走原版的放置流程**（不返回 `PASS`）；**不得**用 `SeedItem` 承载它。
- **依据**：裁定 #7（可种植）、#14（轻量路径）；`SeedItem.java:38-41`（`useOn` 无条件 `PASS`，D-152 ②）；`BRIEF` §6 红线第 2 条。
- **落地位置**：新建 `PlainSeedItem`；`FarmstormItems` 的注册处；`#farmstorm:seeds` 标签（`ModItemTagsProvider:42-48`）。
- **补充**：**这是「轻量路径」最容易翻车的一处**：`SeedItem` 就在手边、`ItemNameBlockItem` 的父类也已经选好，看起来改一个构造参数就能复用——但它的 `useOn` 被 D-152 ② 钉死成 `PASS`，改它等于推翻 0.2.0 的裁定，不改它则平凡韭菜种不下去。两条路都不能走，所以新开一个类。
  新类只做两件事：**继承 `ItemNameBlockItem`**（拿到原版的放置逻辑）、**给一个无档案的名字**（不走 `CropTier.nameStyle`）。**不写 tooltip**——`SeedItem.appendHoverText` 那套按档案取等级名的逻辑在这里没有输入。
  **它进 `#farmstorm:seeds` 标签**：那个标签的语义是「播种物」，平凡韭菜种子是唯一的真播种物，不进标签会让标签变成空集。

#### D-217 · 新增转换配方「平凡韭菜 → 朋友的韭」，贴图复用

- **编号**：D-217
- **结论**：新增一条正向 `farmstorm:converting` 配方，输入 `farmstorm:plain_chives`、输出 `farmstorm:friend_chives`；转换配方总数因此为 **21**。
- **依据**：裁定 #15（新增这条配方）；46 号 D-195 的补充（朋友的韭是 22 条里**唯一没有转换配方**的一条，这正是 0.2.2 补平凡韭菜的原始理由）；`ModRecipeProvider.java:97` 的注释「朋友的韭没有转换配方」。
- **落地位置**：`ModRecipeProvider.java`；`ModLangProvider`（无需新键，配方不走语言键）。
- **补充**：**这条把 0.2.2 留下的最后一个缺口补上了**：46 号 D-195 当时的原话是「朋友的韭是 22 个作物里唯一没有转换配方的」，当时只能靠植物学家交易取得；现在它有了「种平凡韭菜 → 转换 → 朋友的韭」这条闭环，与其它 20 条同形。**「唯一」这个词要加限定**：它指的是「22 条档案里唯一一个**有作物形态**却没有转换配方的」，`compressed_puffed_wheat` 也没有转换配方，但它是二级合成物（D-56）而不是作物，不在同一队列里。
  **它不影响「培养器能收什么」的算式**：培养器的 17 个投入物里包含平凡韭菜（`plan0.2.3.txt:24` 的「平凡农作物」），这条配方只是让平凡韭菜**有地方去**。
  ⚠️ **`friend_chives` 仍然不在植物学家的 22 项普通交易里**（46 号 D-197 的清单是 20 个原版物品 + 平凡韭菜 + 压缩膨胀麦），也不在 5 级史诗项里。本版**不改交易清单**。

### 1.5 培养器（D-218 ~ D-220）

#### D-218 · 准入判据反转：只收原版物品与平凡作物

- **编号**：D-218
- **结论**：把 `CultivatorBlockEntity.admissible` 的判据从「有 `CropProfile` 且非史诗档」反转为「是**转换配方的输入**且其对应改良作物不是史诗档」；培养器从此**一个改良作物也收不进**，投入物共 **17** 个。
- **依据**：`plan0.2.3.txt:24`「农作物培养器只能培养原版农作物和平凡农作物，不再能培养改良农作物，史诗品质的改良农作物对应的原版农作物或平凡农作物不能被培养」；裁定 #10（凡史诗档案的转换输入都排）；`CultivatorBlockEntity.java:138-144`（现判据：`profile != null && profile.tier() != CropTier.EPIC`）；`BRIEF` §1.3。
- **落地位置**：`CultivatorBlockEntity.java:138-144,161-170`（`admissible` 与 `profileOf`）；`CultivatorBlock.java:107-127`（`useWithoutItem` 的客户端分支与四道闸）。
- **补充**：**这是一次彻底的反转，不是放宽或收紧**：
  - 现状：**只收** 22 条档案对应的物品（改良作物、部署物、种子），原版小麦**根本放不进去**；
  - 本版：**只收** 21 条转换配方的输入侧，改良作物**一个都不收**。
  两句话没有交集，`admissible` 的方法体整个换掉。
  **17 的算式**：21 条转换配方 − 4 条史诗输入（火把花 / 瓶子草荚 / 甜浆果 / 金苹果）= **17**。被排除的 4 个与它们对应的史诗档案是 `awakened_torchflower` / `awakened_pitcher_plant` / `lucky_berry` / `wild_apple`。
  **为什么用「转换配方的输入」而不是手写白名单**：判据必须只有一个来源。配方表已经写着「什么能变成什么」，再手写一份「什么能培养」必然与它漂移（RT-49）。**副作用**：将来加一条转换配方，培养器会自动多收一个投入物——这是**期望行为**，要写进 54 号的维护须知。
  **「史诗对应的不能培养」在判定上等于「输入对应的 tier 是 EPIC」**：这条判定要读**配方**（输入 → 输出）再读**输出档案的 tier**，不能只看输入物品自己有没有档案——输入侧全都是原版物品，没有档案。
  **`profileOf` 的角色变了**：它原本负责「物品 → 档案」，现在用于「输入物品 → 找到配方的输出 → 输出档案」。方法名要跟着改（54 号定名），不能让一个叫 `profileOf` 的方法去查配方。

#### D-219 · 产出 = 2 个投入物 + 2 个对应种子

- **编号**：D-219
- **结论**：培养器每次结算产出**两组**：投入物 × `cultivator.yield`（默认 2）+ 对应种子 × `cultivator.yield`（默认 2）；**自身即种子**的投入物两组都是它自己，因此实际得到 **4 个同一物品**。
- **依据**：裁定 #9（两个投入物 + 两个对应种子）、#16（自身即种子的也算，给 4 个）；`CultivatorBlockEntity.java:230-243`（现 `batch()`：作物 × yield + 有独立种子时种子 × yield）、`:246-249`（`hasOwnSeed` 判据）；`FarmstormConfig.java:961-966`（`cultivator.cycleTicks` / `cultivator.yield`）。
- **落地位置**：`CultivatorBlockEntity.java:230-249`。
- **补充**：**逐项对照表**（54 号据此写规格，不要把「对应种子」理解成「另一种物品」）：

| 投入物 | 对应种子 | 一组产出 |
|---|---|---|
| 小麦 | 小麦种子 | 2 小麦 + 2 小麦种子 |
| 甜菜根 | 甜菜种子 | 2 甜菜根 + 2 甜菜种子 |
| 西瓜片 | 西瓜种子 | 2 西瓜片 + 2 西瓜种子 |
| 西瓜 | 西瓜种子 | 2 西瓜 + 2 西瓜种子 |
| 南瓜 | 南瓜种子 | 2 南瓜 + 2 南瓜种子 |
| 平凡韭菜 | 平凡韭菜种子 | 2 平凡韭菜 + 2 平凡韭菜种子 |
| 胡萝卜 / 马铃薯 / 毒马铃薯 / 仙人掌 / 竹子 / 红蘑菇 / 棕蘑菇 / 可可豆 / 诡异菌 / 绯红菌 / 甘蔗 | **它自己** | **4 个同一物品** |

  **「自身即种子」这一档是本版的判定分叉**（裁定 #16）：0.2.2 的 `hasOwnSeed` 判据是「`seedItem != null` **且** `seedItem != id`」——自身即种子的那 11 条因此**只给一组**。本版把这一档改成给两组，理由是从玩家的角度看，胡萝卜没有「另一个种子物品」，能种出胡萝卜的就是胡萝卜本身；只给 2 个会让这 11 条比另外 6 条少一半产出，而它们本来是同一档投入物。**这是本版唯一一处「产出翻倍」**，49/54 号必须把它写成平衡变更而不是实现细节。
  **`cultivator.yield` 一个键管两组**：不拆成两个键（否则 config 多 1 个、语言键多 1 个，而两组数量在本版永远相等）。
  **「自身即种子」这一档按字面收口**：毒马铃薯、甘蔗、可可豆这些在原版里没有独立种子物品的投入物，一律按「它自己就是种子」处理，给 **4 个同一物品**。不按「原版能不能种」逐条考据——那会引入一张 17 行的手写映射表，而本条的立意恰恰是「判据只有一个来源」。**本文补充**，作者可否决。

#### D-220 · 培养器补合成配方

- **编号**：D-220
- **结论**：新增培养器的有序 3×3 合成配方：`铁锭 玻璃 铁锭 / 玻璃 红石粉 玻璃 / 铁锭 玻璃 铁锭`，即 **4 铁锭 + 4 玻璃 + 1 红石粉** → 1 个 `farmstorm:cultivator`。
- **依据**：`plan0.2.3.txt:25-27` 的配方原文；`BRIEF` §1.3（培养器现在**没有**合成配方，`recipe/cultivator.json` 不存在，创造栏是唯一入口，A-64）。
- **落地位置**：`ModRecipeProvider`；`ModLangProvider`（无需新键）。
- **补充**：**这条结束了一个长期状态**：0.2.0 起培养器只能靠创造栏或 `/give` 取得（`FarmstormItems.java:121-123` 的注释 A-64）。补配方后它第一次有了生存获取途径。
  **配方要与转换器的配方区分开**：转换器是 6 铁 + 2 玻璃 + 1 红石（D-206 那台机器的配方在 `BRIEF` §5 第 12 项），两者都是「铁 + 玻璃 + 红石」的三件套，只有排布不同——这是 plan 的原文，照抄，不自行「优化」以提高辨识度。
  **配方解锁条件**（`unlockedBy`）由 54 号定，本文只要求它**不要**用 `#farmstorm:processing_block` 之类本模组独有的标签当解锁条件——那会让配方在第一次进入世界时不可见。

### 1.6 炮弹与文案（D-221 ~ D-222）

#### D-221 · `TurretBullet` 出膛 300 刻后删除

- **编号**：D-221
- **结论**：给 `TurretBullet` 加寿命：**出膛后第 300 刻**（15 秒）无条件 `discard()`；寿命进 config 新键 `pumpkin_turret.bulletLifetimeTicks`，默认 **300**。
- **依据**：裁定 #11（`TurretBullet`，进 config 默认 300）；`plan0.2.3.txt:31`「man瓜的炮弹在飞行300刻后会被直接删除」；`BRIEF` §1.4（`TurretBullet` 现在**没有**寿命上限）；`ModLangProvider.java:91`（man瓜 = `pumpkin_turret`）。
- **落地位置**：`TurretBullet.java:77-90`（`tick()` 方法体）；`FarmstormConfig.java:445-458`（`[pumpkin_turret]` 组）。
- **补充**：**「man瓜」的指向必须写死**：`man瓜` 是 `pumpkin_turret` 的中文名（`ModLangProvider.java:91` 的「man瓜种子」），它的炮弹是 `TurretBullet`。**`MelonGrenadeProjectile`（瓜！）与 `MelonSliceProjectile` 是另一条线，本版一行不动**——`瓜！` 的母弹与子弹都靠落地碰撞结算，加寿命会改变它们的战术用法。
  **计时起点是出膛，不是「最后一次命中」**：`TurretBullet` 是追踪弹，若按「脱离目标后计时」算，玩家可以靠走位无限续命。
  **删除方式取静默 `discard()`**，不加粒子、不加音效、不写「消失」的进度——plan 的原文是「直接删除」。
  **不覆盖 `readAdditionalSaveData` 的读档场景**：`TurretBullet` 的目标不跨存档保留（`readAdditionalSaveData` 里把 `target` 置空），寿命同理——读档后重新计 300 刻即可，不做「已飞刻数」的持久化。

#### D-222 · 进度「最扎人的一集」描述改字，中英同改

- **编号**：D-222
- **结论**：把 `farmstorm:fang_dance` 的**中文描述**从「触发一次「赢过输过没死过」」改为「触发一次「**刺球**」」；**英文描述同步补一个对应表达**；标题「最扎人的一集」/ "The Itchiest Episode" **不动**。
- **依据**：裁定 #12（中文改，英文也补对应表达）；`plan0.2.3.txt:35`「将进度『最扎人的一集』的描述中的『赢过输过没死过』改为『刺球』」；`ModLangProvider.java:229`。
- **落地位置**：`ModLangProvider.java:229`。
- **补充**：**中英现在本来就不一致**：中文描述是「触发一次「赢过输过没死过」」，英文描述是 "Trigger a Fang Trap Cactus once"——英文里从来没有「赢过输过没死过」的对应词。本版按裁定 #12 让两者对齐：中文改成「刺球」，英文改成能表达「刺球」的说法（具体措辞由 54 号定，约束是**不能再用 "Fang Trap Cactus" 这个机制描述**，要与中文的「一个叫刺球的东西」同构）。
  **只改描述、不改标题**：标题是出现在进度树上的可见名，「最扎人的一集」与「刺球」不冲突；改标题会让已完成该进度的玩家看到不同的名字。
  **只改 `fang_dance` 一条**：22 条进度里其它任何一条的文案**不在本版范围**，哪怕它们也有中英不一致。

### 1.7 口径同步（D-223 ~ D-224）

#### D-223 · config 176 键 → 175 键，push 数不变

- **编号**：D-223
- **结论**：server config 从 **176 键 / 60 push** 变为 **175 键 / 60 push**：删掉 `[converter]` 整组 3 键，新增 `crop_converter.processTicks`（默认 600）与 `pumpkin_turret.bulletLifetimeTicks`（默认 300）；`[converter]` 组名换成 `[crop_converter]`，因此 push 计数不变。
- **依据**：裁定 #2（删经验 / 下界之星）、#4（30 秒进 config 默认 600）、#11（炮弹寿命进 config 默认 300）；`FarmstormConfig.java:19-21,295-305`（现 3 键）；`BRIEF` §0.1（现 176 键 / 60 push）。
- **落地位置**：`FarmstormConfig.java`；`ModLangProvider.java:243-245`（config 语言键）；`run/config/farmstorm-server.toml`（**不手改**，见补充）。
- **补充**：**算式是 176 − 3 + 2 = 175**。三个删的是 `converter.xpCost` / `converter.netherStarForwardCost` / `converter.netherStarOnReverse`；两个加的是 `crop_converter.processTicks` 与 `pumpkin_turret.bulletLifetimeTicks`（后者进**既有**的 `[pumpkin_turret]` 组，不新增组）。
  **「60 组」是两个不同的数，别混**：`b.push()` 调用次数是 60，`run/config/farmstorm-server.toml` 的顶层节是 36 个。本文与下游一律写「**175 键 / 60 push**」。
  ⚠️ **盘上的 `run/config/farmstorm-server.toml` 不会自动更新**：Minecraft 的 config 只在**键增删**时重写文件，**改默认值不会回写已有文件**。因此「删 3 键 + 加 2 键」这一档**会**改到 toml（键集合变了），但**验收仍必须用新生成的配置目录**——0.2.2 已经踩过一次「老 toml 让验收假红」（47 号附录 A 第 4 行），本版沿用同一条纪律。

#### D-224 · 语言键 340 × 2 → 321 × 2

- **编号**：D-224
- **结论**：语言键从 **340 × 2** 变为 **321 × 2**（zh_cn 与 en_us 仍然逐键相等）：删 **23** 个、加 **4** 个，再把 2 个**改名**、2 个**改值**。
- **依据**：裁定 #2（删 9 个 GUI 键）、#8 / #17（删两条进度共 4 个键）、#14（加 2 个）、#12（改 1 条描述）、#4 / #11（加 1 个 config 键、删 3 个）、D-212（删 7 个种子）、D-214（删 `nether_star_toll`）；`BRIEF` §0.1 / §3.2。
- **落地位置**：`ModLangProvider.java`；`src/generated/resources/assets/farmstorm/lang/{zh_cn,en_us}.json`。
- **补充**：算式 **340 − 23 + 4 = 321**。逐项：

| 类别 | 键数 | 说明 |
|---|---|---|
| 删：7 个种子物品 | −7 | `item.farmstorm.{puffed_wheat_seeds,power_beet_seeds,melon_grenade_seeds,pumpkin_turret_seeds,awakened_torchflower_seeds,awakened_pitcher_plant_pod,chive_seeds}` |
| 删：转换器 GUI | −9 | `container.farmstorm.converter` + `gui.farmstorm.converter.{convert,xp_cost,no_recipe,need_catalyst,not_enough_xp,forward,reverse,tooltip}` |
| 删：转换器 config | −3 | `farmstorm.configuration.converter.{xpCost,netherStarForwardCost,netherStarOnReverse}` |
| 删：两条进度 | −4 | `advancements.farmstorm.{seed_cycle,nether_star_toll}.{title,description}` |
| 加：平凡作物 | +2 | `block.farmstorm.plain_chives_crop`、`item.farmstorm.plain_chives_seeds` |
| 加：新 config | +1 | `farmstorm.configuration.crop_converter.processTicks` |
| 加：新 config（炮弹） | +1 | `farmstorm.configuration.pumpkin_turret.bulletLifetimeTicks` |
| 改：`fang_dance` 描述 | 0 | 键不变，值变（D-222） |
| 改：`converter` 进度的描述 | 0 | `advancements.farmstorm.converter.description` 的中文还写着旧名「放置一台**农产品**转换器」，改成「**农作物**转换器」；键不变、值变。**这条最容易漏**：英文在 0.2.2 就已是 `Place a Crop Converter`，只有中文没跟上，漏改会出现「方块叫农作物转换器、进度写农产品转换器」 |
| 改：`converter` 两个键 | 0 | `item.farmstorm.converter` → `item.farmstorm.crop_converter`，`block.farmstorm.converter` → `block.farmstorm.crop_converter`，键数不变 |

  **改名与删除要分清**：`converter` 的两个键是**改名**（`item.` / `block.` 各一个），不计入删除也不计入新增；如果按「删一个加一个」数，会得出 323 而不是 321。
  **`farmstorm.configuration.` 前缀不要漏**：config 语言键的前缀与 item / block 不同（`ModLangProvider.java:482-484` 的 `config(...)` helper），逐键清点时容易把 3 个 `converter.*` 当成物品键去找。

---

## 2. 作者裁定索引（18 条 → 决策）

| # | 议题 | 裁定 | 决策 |
|---|---|---|---|
| 1 | 方块注册 ID | 改为 `crop_converter` | D-206 |
| 2 | GUI / 经验 / 下界之星 | 全部删除 | D-208、D-209、D-214 |
| 3 | 转换配方体系 | 保留类型，只留正向 | D-209 |
| 4 | 30 秒 | 进 config，默认 600 | D-208、D-223 |
| 5 | 边界行为 | 全部照抄培养器先例 | D-208、D-210、D-211 |
| 6 | 15 个改良作物方块 | 保留但不可获得 | D-213 |
| 7 | 平凡作物种子 | 升级成可种植作物 | D-215、D-216 |
| 8 | `seed_cycle` 进度 | 直接删掉 | D-214 |
| 9 | 培养器产出 | 2 投入物 + 2 对应种子 | D-219 |
| 10 | 史诗排除范围 | 凡史诗档案的转换输入都排 | D-218 |
| 11 | man瓜炮弹 | `TurretBullet`，config 默认 300 | D-221 |
| 12 | 成就文案 | 中文改，英文也补 | D-222 |
| 13 | 文档流程 | 照旧 | D-203 |
| 14 | 平凡韭菜档案 | 不进档案，轻量路径 | D-215 |
| 15 | 平凡韭菜转换配方 | 新增平凡韭菜 → 朋友的韭 | D-217 |
| 16 | 「对应种子」判据 | 自身即种子的也算，给 4 个 | D-219 |
| 17 | `nether_star_toll` | 删掉 | D-214 |
| 18 | 平凡韭菜贴图 | 复用朋友的韭 4 张 stage 图 | D-215 |

---

## 3. 对 0.2.2 的增 / 改 / 删

### 3.1 新增（11 项）

| # | 物件 | 类型 | 决策 |
|---|---|---|---|
| 1 | `farmstorm:plain_chives_crop` | 方块 | D-215 |
| 2 | `farmstorm:plain_chives_seeds` | 物品 | D-216 |
| 3 | `PlainCropBlock` | 类 | D-215 |
| 4 | `PlainSeedItem` | 类 | D-216 |
| 5 | `farmstorm:plain_chives` 转换配方 | 配方（`converting`） | D-217 |
| 6 | `farmstorm:cultivator` 合成配方 | 配方（`crafting_shaped`） | D-220 |
| 7 | `crop_converter.processTicks` | config 键 | D-223 |
| 8 | `pumpkin_turret.bulletLifetimeTicks` | config 键 | D-223 |
| 9 | `plain_chives_crop` 的 4 阶段 blockstate | 资源 | D-215 |
| 10 | 转换器 7 张贴图 + 3 个 mcmeta | 资源 | D-211 |
| 11 | `CultivatorLinkPayload.Link.Kind` | 协议字段 | D-210 |

### 3.2 修改（14 项）

| # | 位置 | 改法 | 决策 |
|---|---|---|---|
| 1 | `converter` → `crop_converter` | 方块 / 物品 / 方块实体三处改名 | D-206 |
| 2 | `FarmstormBlocks.java:71-72` | 属性 3.5F → 铁砧七项 | D-207 |
| 3 | `ConverterBlockEntity`（435 行） | 双槽容器 → 单槽状态机，不再 `implements Container` | D-208 |
| 4 | `ConverterRecipe`（112 行） | 删五字段 + `reverse()`，只留 `input` / `result` | D-209 |
| 5 | `twoWay` → 单向 | 24 条全部单向化 | D-209 |
| 6 | `oneWayWithNetherStar` → `oneWay` | 催化剂清空 | D-209 |
| 7 | 转换器合成配方 | 8 燃料 + 工作台 → 6 铁 + 2 玻璃 + 1 红石 | D-206 |
| 8 | `CultivatorBlockEntity.admissible` | 判据反转 | D-218 |
| 9 | `CultivatorBlockEntity.batch` | 产出改两组 | D-219 |
| 10 | `TurretBullet.tick` | 加寿命 | D-221 |
| 11 | `ModLangProvider.java:229` | `fang_dance` 描述中英同改 | D-222 |
| 12 | `Farmstorm.java:180-183` | 自检数字改 92 / 28 / 28 / 1 | D-205 |
| 13 | `tools/verify_project.py:334-393` | `EXPECTED` 与 `EXPECTED_COUNTS` | D-205 |
| 14 | `#farmstorm:seeds` 标签 | 成员 6 → 1 | D-212 |

### 3.3 删除（13 项）

| # | 物件 | 类型 | 决策 |
|---|---|---|---|
| 1 | `ConverterMenu.java`（216 行） | 文件 | D-208 |
| 2 | `ConverterScreen.java`（86 行） | 文件 | D-208 |
| 3 | `ConverterRecipeBookCategory.java`（24 行） | 文件 | D-208 |
| 4 | 7 个种子物品 | 注册项 | D-212 |
| 5 | `converter` 的 `MenuType` | 注册项（`menu_type` 2 → 1） | D-208 |
| 6 | `converter.xpCost` 等 3 个 config 键 | config | D-223 |
| 7 | 9 个 GUI 语言键 | 语言键 | D-224 |
| 8 | `#farmstorm:converter_fuel` 标签 | 标签 | D-209 |
| 9 | `farmstorm:seed_cycle` | 进度 | D-214 |
| 10 | `farmstorm:nether_star_toll` | 进度 | D-214 |
| 11 | 6 条种子转换配方 | 配方 | D-212 |
| 12 | 7 张旧转换器方块贴图 | 资源 | D-206 |
| 13 | `gui/converter.png` | 资源 | D-208 |

---

## 4. 红线（7 条）

1. **不许给 15 个改良作物方块补 `BlockItem`**：裁定 #6 是「保留但不可获得」，补了就是可获得（D-213）。
2. **不许复用 `SeedItem` 承载平凡韭菜种子**：它的 `useOn` 被 D-152 ② 钉死成 `PASS`（D-216）。
3. **不许在渲染期按方块类型反查颜色**：区块未加载会闪色，必须走 payload 的 `Kind` 字节（D-210）。
4. **不许把动态 PNG 的暂停交给代码**：动画只能靠 blockstate 切模型（D-211）。
5. **不许改 `docs/zh/00`~`51`**、不许改 `docs/en/*`、本版**不生成英文文档**（D-203）。
6. **不许动 `MelonGrenadeProjectile` / `MelonSliceProjectile`**：300 刻只作用于 `TurretBullet`（D-221）。
7. **不许把 `plain_chives` 塞进 `CropProfile.ALL`**：那会让它进 `LUCKY_BERRY_POOL` 与 `#farmstorm:modified_crops` 标签（D-215）。

---

## 5. 墓碑（已撤回 / 已翻案的旧口径）

1. **46 号 D-193 的「平凡韭菜不能种」已翻案**：0.2.2 定「没有 `CropProfile`、不是作物、不能种、无种子」，本版把后三项翻掉，只保留「没有 `CropProfile`」（D-215 / D-216）。**这是本版唯一一处推翻 0.2.2 决策的地方**，翻案理由是作者裁定 #7。
2. **46 号 §3.3 第 1 条的「转换器重做整条搁置」已兑现**：0.2.2 把它挡在门外并写明「留到 0.2.3」，本版执行（D-204）。**这不是撤回，是履约**——搁置与墓碑的区别见 46 号 §0.2。
3. **`SeedItem.useOn → PASS`（D-152 ②）继续有效**，本版**不翻案**：平凡韭菜种子靠新类绕开它，不改它的行为（D-216）。
4. **`hasOwnSeed` 的「自身即种子只给一组」被本版改掉**：0.2.2 的判据是 `seedItem != null && seedItem != id`，本版改成「自身即种子也给两组」（D-219）。注意这两条**不是**同一个东西——前者是「有没有独立种子物品」，后者是「产出几组」。

---

## 6. 已否决的方案（8 条）

| # | 方案 | 否决理由 |
|---|---|---|
| 1 | **两个方块 id 并存**（旧的 `converter` 保留注册、下线） | 注册面 +2 而不是 +1；`#farmstorm:processing_block` 里要同时放两个方块，`isProcessingBlock` 的语义变模糊（D-206） |
| 2 | **保留经验消耗当下限** | 裁定 #2 明确删；且新机器没有界面显示「本次要扣多少经验」，没有显示的成本等于隐藏惩罚（D-208） |
| 3 | **删掉 `converting` 类型，改成代码里的静态映射表** | 映射表已经用配方表达过一遍，再写一份就是同一事实的两个副本，改一条 tier 或加一个作物必然漏一边（RT-49）（D-209） |
| 4 | **给 15 个作物方块补 `BlockItem`** | 补了就变成「可获得」，与裁定 #6 冲突；且能直接放置的作物方块会绕过 `age` 成长（D-213） |
| 5 | **让 `plain_chives` 进 `CropProfile.ALL` 当第 23 条** | 会同时污染 `LUCKY_BERRY_POOL`、`#farmstorm:modified_crops` 标签与 `Farmstorm.java` 的 22 条自检（D-215） |
| 6 | **渲染期按方块类型反查连线颜色** | 连线常在远端未加载区块上，反查会随机闪回青色（D-210） |
| 7 | **把 `SeedItem` 的 `useOn` 改回可播种以复用** | 那会推翻 0.2.0 的 D-152 ②（改良种子不可播种），本版无权翻案（D-216） |
| 8 | **300 刻也作用于瓜！的母弹与子弹** | plan 只写「man瓜的炮弹」；瓜！靠落地碰撞结算，加寿命会改变其战术用法（D-221） |

---

## 7. 与 plan 的口径差异（5 条）

**读法**：口径差异**不是笔误、不是「plan 写错了」**——它是 plan 的文字与作者裁定（或与源码事实）指向不同行为的那些点。处理口径差异只有两个动作：**按裁定实现** + **在本文登记**。三个禁止：不许照抄 plan 的措辞当行为描述、不许把差异当 bug 修、不许改 plan 原文。

> **一条已撤回的差异**（2026-11-05 作者指出后核销）：初稿曾把 `:12` 的「播放预设好的翻书动画」登记为口径差异，理由是「贴图包描述的是转换动画而不是翻书」。**这个判断是错的**：`翻书动画` 指的就是**逐帧动画（flipbook）**，而贴图包的正面 / 顶面 / 侧面**各 16 帧**、`32×512` 纵向帧条、`frametime: 2`、`interpolate: false`、1.6 秒一循环——素材与 plan 的措辞**完全一致**，没有任何差异。原文见 `README.txt`「正面、顶面、侧面均为16帧动态」与三个 `.mcmeta`。该条从下表撤除，不占编号。

| # | plan 原文 | 差异 | 处置 |
|---|---|---|---|
| 1 | `:11`「带有标签：`farmstorm_processing_block`」 | plan 写的是**下划线**形式，仓库里既有的标签是 `farmstorm:processing_block`（0.2.2 的 D-194 已建，成员只有培养器） | 按 `farmstorm:processing_block` 落地，不新建第二个标签；`crop_converter` 加入该标签（D-208） |
| 2 | `:18-20`「移除所有改良农作物的种子，若该改良农作物有对应的平凡作物，则将该种子转为对应平凡作物的种子」 | 全仓只有 `friend_chives` ↔ `plain_chives` **一对**；且 `plain_chives` 在 0.2.2 里被定义为「不是作物、不能种、无种子」 | 7 个种子**全删**；只对这一对补出「可种植的平凡作物 + 它的种子」；其余 6 个种子**不补任何替代品**（D-212、D-215） |
| 3 | `:24`「史诗品质的改良农作物对应的原版农作物或平凡农作物不能被培养」 | plan 用「对应的原版农作物或平凡农作物」描述，这个集合没有唯一读法 | 落成「**转换配方的输入**里，输出档案 tier 为 EPIC 的那 4 条被排除」，即火把花 / 瓶子草荚 / 甜浆果 / 金苹果（D-218） |
| 4 | `:5`「除非本文档明确要求，否则不能修改0.2.2原有内容」 | 第 ② 条与第 ③ 条的对象就是 0.2.2 自己建立的东西 | 5 条编号需求算「明确要求」；为落地它们所必需的连带改动（注册改名、自检数字、检查器清单、语言键增减）不算越界，但逐条登记在 §3（D-204） |
| 5 | `:12`「三十秒后产出1个对应改良农作物」 | plan 写死 30 秒；作者裁定进 config | 默认 **600 刻**，可配；「1 个」不变（D-208、D-223） |

---

## 8. 遗留登记

| # | 事项 | 现状 | 归属 |
|---|---|---|---|
| 1 | `converting` 配方的 JSON schema 变更会让旧数据包读失败 | 本版不写迁移、不写向后兼容 codec | D-209 补充；54 号写成已知限制 |
| 2 | 老存档的 `converter` 方块 / 物品失效、7 种种子物品失效 | 既定代价（裁定 #1、#2） | README 升级须知；56 号 |
| 3 | 史诗作物变便宜（下界之星门槛删除） | 裁定 #2 的直接后果 | 53 号写成「已知影响」 |
| 4 | 培养器产出翻倍（自身即种子那 11 条从 2 个变 4 个） | 裁定 #16 的直接后果 | 53 / 55 号写成平衡变更 |
| 5 | 红线的红色数值未定 | 约束是「与青形成对比、不用纯 `0xFF0000`」 | 54 号定 |
| 6 | 培养器新配方的 `unlockedBy` 未定 | 约束是「不能用本模组独有标签」 | 54 号定 |
