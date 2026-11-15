# 09 号 v0.3.0（1.20.1）任务拆分

> 上游：`06-DECISIONS-0.3.0-1.20.1.md`（D-015 ~ D-052）、07 号（R8 起 / B-13 ~ B-28）、08 号（RT-011 ~ RT-020 / 验收 #1 ~ #32）。只引不改 06 号；行号取自 `facts-B` / `facts-C`，另有七处本工作区实读并逐处标「实读」。
> 本文把四条需求拆成 **24 个任务 F-022 ~ F-045**、**8 个 Phase（7 ~ 14）**、**8 个里程碑（M7 ~ M14）**、**6 条裁决（A-007 ~ A-012）**，人日合计 **27.0**；基线是 1.20.1 v0.2.8 已实现态（06 号 §0.4）。

## §0 阅读约定
### 0.1 任务粒度五条判据
1. 一个任务 = 一次可独立提交、可独立验证的改动。
2. 提交后构建退出码 0，且不依赖同批次里尚未提交的任务。
3. 每条任务至少一条可观察断言，断言里出现具体数值 / 键名 / 路径 / 日志片段 / 像素值。
4. 回滚该提交不让其它已完成任务失效。
5. 不跨 Phase 边界；D-024 那种「两处必须同一任务改完」的成对改动，按第 3 条自带双断言。

### 0.2 复杂度换算表
| 档 | 人日 | 判据 |
|---|---|---|
| **S** | 0.5 | 单文件，或纯数据 / 纯资源 / 纯字面量替换；含「一条判据必须成对同改的 ≤ 2 文件」 |
| **M** | 1.5 | 2 ~ 4 个源码文件，或新增一个完整行为，或跨 ≥ 3 个产物目录的核对与重跑 |
| **L** | 3.0 | ≥ 5 个文件，或含状态机 / 序列化 / 协议 / 渲染管线 |

档位已由任务书钉死，本表只做反向校验。三处按判据的哪一条计：① F-028 的两文件是「一张标签 + 一张类型表」的成对同改（D-024 明写必须同一任务改完），无新行为，按 S 计；② F-022 是纯字面量、F-023 是纯资源，各按 S 计，与文件数无关；③ F-045 无源码改动但跨 5 个产物目录核对与重跑，按 M 计。A-007 的「序列化」边界另算。

### 0.3 中间态窗口与既有红基线
本版有 **两个** 互不重叠的窗口，窗口内某些断言**期望红**——那不是缺陷。

| 窗口 | 开窗任务 | 关窗任务 | 跨几个任务 | 窗口内期望红的断言 | 窗口内一行不许动的文件 |
|---|---|---|---|---|---|
| **W1 注册面** | F-024（第一次让 blocks / items 从 28 抬到 29） | F-042（把 `EXPECTED_COUNTS` 收到 96） | 19 个（F-024 ~ F-042） | 检查器 §2 的「92 / 96」计数断言，与 4 项新 ID 的存在性断言 | `tools/verify_project.py` |
| **W2 语言键表头** | F-024（第一次让语言键从 321 变 323） | F-032（收到 328 并同步表头） | 9 个（F-024 ~ F-032） | `ModLangProvider` 表头注释里 321 那组数与实际键数的人读一致性（检查器不校验这一条） | `datagen/ModLangProvider.java:10` 与 `:14-16` 的表头口径段 |

**既有红基线单独记账（不许记成新 bug）**：检查器当前必然打 **1 条 FAIL**，成因在基线里就有——`EXPECTED_COUNTS` 是 12 类 92，含一个 1.20.1 根本不存在的 `data_component_type: 1`，且其 ID 清单要求字面量 `temporary_enchantments`，而该字面量在 1.20.1 全 src **0 命中**（`facts-A` §0 第 1 / 第 2 条、`:448` / `:469`；D-052 补充）。它由 **F-042 与 W1 同批清掉**：把该类槽位换成 `global_loot_modifier_serializers`（D-047 引入的 1 项），两边重新对齐为 12 类 96。

### 0.4 六条读数纪律
1. **数字只从 D-018 的冻结表取**：注册面 96（12 类）、server config 175、client config 3、语言键 328、Java 文件 159、网络包 2、协议串版本 2、Mixin 0、方块战利品表 29、转换配方 21、合成配方 5、配方 JSON 26、进度 16、方块模型 79、blockstate 29、item 模型 29、贴图 88 / 8 / 96、事件监听 9、loot modifier 数据文件 2。
2. **每个数给算式**：`91 + 5 = 96`、`321 + 7 = 328`、`151 + 8 = 159`、`84 + 4 = 88`、`5 + 3 = 8`、`89 + 7 = 96`、`4 + 1 = 5`、`25 + 1 = 26`、`78 + 1 = 79`、`28 + 1 = 29`。
3. **行号只从事实报告取**：`facts-B` 与 `facts-C`；两份都没有的，本工作区实读后标「实读」，只用于 `verify_project.py` / `ModItemModelProvider` / `FarmstormDataGen` / `build-1.20.1.cmd` / `FarmstormItems` 这几处。
4. **验收断言只写可观察项**：计数 / 日志行 / `data get` 读数 / grep 命中 / 像素值 / 产物文件名；验收编号沿用 08 号的 #1 ~ #32，不重排、不拆分。
5. **构建退出码 0 是每个任务的入场券**，不是某个 Phase 的收尾项。
6. **中间态读数必须标窗口**：W1 / W2 内的红是账面状态，既有那 1 条红是基线遗留，两者都不进「缺陷」一栏。

## §1 任务总表
| 任务号 | 名称 | Phase | 里程碑 | 复杂度 | 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|---|---|---|
| F-022 | 版本串 0.3.0 | 7 | M7 | S | 0.5 | — | 2 处版本串改到 0.3.0，jar 名跟上 |
| F-023 | 终端贴图 7 文件手放 | 7 | M7 | S | 0.5 | — | 4 张 PNG + 3 个 mcmeta 落进资源目录 |
| F-024 | 方块与物品注册 | 8 | M8 | M | 1.5 | — | control_terminal 进 blocks / items / 创造栏 / 2 语言键 |
| F-025 | 模型与 blockstate | 8 | M8 | M | 1.5 | F-023、F-024 | 4 个 facing variant + 方块模型 + item 模型父级 |
| F-026 | 方块实体与三态 NBT | 9 | M9 | M | 1.5 | F-024 | BE 类型进注册面，模式三态可存读 |
| F-027 | Kind.TERMINAL 与协议版本 2 | 9 | M9 | M | 1.5 | F-024 | 第三个线上码画绿线，协议串升一位 |
| F-028 | 标签与 KIND_BY_BLOCK 同改 | 9 | M9 | S | 0.5 | F-024、F-026、F-027 | 标签 3 成员 + 类型表 3 条 |
| F-029 | 放置绑定与三出口 | 10 | M10 | M | 1.5 | F-026、F-028 | 终端能绑、能补绑、能解绑 |
| F-030 | MenuType 与 0 槽菜单 | 10 | M10 | M | 1.5 | F-026 | 第 2 个菜单类型，0 槽面板可开 |
| F-031 | 面板屏幕与注册 | 10 | M10 | M | 1.5 | F-030 | 两个按钮 + 一行状态可点可读 |
| F-032 | 面板 5 语言键 | 10 | M10 | S | 0.5 | F-024、F-030、F-031 | 语言键 323 → 328 并同步表头 |
| F-033 | 每 20 刻投料循环 | 11 | M11 | L | 3.0 | F-026、F-029 | 两种模式各投 1 个，四条边界全兜 |
| F-034 | 两条挖掘标签 | 12 | M12 | S | 0.5 | F-024 | 三成员进 pickaxe + 进 needs_stone_tool |
| F-035 | 史诗准入放行 | 12 | M12 | S | 0.5 | — | 删掉档位比较，4 条史诗输入可培养 |
| F-036 | PuffedHayData | 13 | M13 | M | 1.5 | — | 位置集合可存读、跨重启条数不变 |
| F-037 | 落点登记 | 13 | M13 | S | 0.5 | F-036 | 每次 setBlock 成功后记一条 |
| F-038 | 战利品修饰器类与注册 | 13 | M13 | L | 3.0 | F-036 | 开箱命中即返回空表并消费该条记录 |
| F-039 | 数据文件 provider | 13 | M13 | M | 1.5 | F-038 | 两份 JSON 由 datagen 产出，清单登记 forge |
| F-040 | 合成配方 | 14 | M14 | S | 0.5 | F-024 | 一条 GIG / IRI / GIG 的 3×3 |
| F-041 | 战利品表 | 14 | M14 | S | 0.5 | F-024 | 方块战利品表 29 张 |
| F-042 | 检查器收到 96 / 12 类 | 14 | M14 | S | 0.5 | F-024、F-026、F-030、F-038 | EXPECTED_COUNTS 收到 96 并清掉既有红 |
| F-043 | 启动自检收到 96 / 12 类 | 14 | M14 | S | 0.5 | F-024、F-026、F-030、F-038 | logRegistrySelfCheck 收到 96 / 12 类 |
| F-044 | README 与交付清单 | 14 | M14 | S | 0.5 | F-042、F-043 | 版本行改 0.3.0 + 六份文档清单 |
| F-045 | 数据面复核与 datagen 重跑 | 14 | M14 | M | 1.5 | F-023 ~ F-041 | 12 个产物面逐项对账，重跑后 git status 干净 |

人日合计：**27.0**。算式：12 个 S × 0.5 + 10 个 M × 1.5 + 2 个 L × 3.0 = 6.0 + 15.0 + 6.0 = **27.0**。

## §2 逐任务详情
每条五字段：**目标 / 交付物 / 验收断言 / 依赖 / 回滚影响**（附注写在该任务末行，不另立字段）。
### Phase 7 · 版本串与贴图（M7）
#### F-022 · 版本串 0.3.0（S · 0.5）
- **目标**：把构建版本串改到 `0.3.0`，产物名成为 `farmstorm-0.3.0+mc1.20.1.jar`（D-015）。
- **交付物**：`gradle.properties:11`（`mod_version=0.2.8` → `0.3.0`，实读）；`remap-1.20.1.cmd:11` 注释里的示例 jar 名（现写 `farmstorm-0.2.4+mc1.20.1.jar`，实读）；`META-INF/mods.toml:7` 读 gradle 的 `mod_version` 占位，随构建自动跟随。
- **验收断言**：`Select-String -Path gradle.properties -Pattern 0.2.8` 命中 **0** 行、`-Pattern 0.3.0` 命中 **1** 行；构建退出码 **0**；`build/libs/` 出现 `farmstorm-0.3.0+mc1.20.1.jar` **1** 个。
- **依赖**：无。
- **回滚影响**：无（纯字符串）。附注：`build-1.20.1.cmd` 实读不含版本字面量（`0.2.8` 0 命中），D-015 虽把它列进落地位置，实际不需改动。
#### F-023 · 终端贴图 7 文件手放（S · 0.5）
- **目标**：把动画三面与静态底面共 **7** 个文件放进资源目录；mcmeta 不由 datagen 产出，因此是手放（D-023）。
- **交付物**：`src/main/resources/assets/farmstorm/textures/block/` 下 `control_terminal_a_front.png` / `_side.png` / `_top.png`（各 32×768、24 帧）+ `_bottom.png`（32×32）+ 三个同名 `*.mcmeta`（`frametime` 2、`frames` 0 ~ 23 显式列出）；注册 id 不带 `_a` 后缀（D-020 补充）。
- **验收断言**：验收 #5（三面各 **24** 帧、PNG 高 **768**、一轮 **48** 刻 = **2.4** 秒）；验收 #29 前半（该目录 `*.png` **84 → 88**、`*.mcmeta` **5 → 8**、文件总数 **89 → 96**，三数不许互借，A-011）；三个动画 PNG 字节数 **4191 / 3851 / 3513**、底面 **1102**、三个 mcmeta 各 **322**。
- **依赖**：无。
- **回滚影响**：无（F-025 之前无人引用）。附注：缺贴图不让构建失败，只在游戏里变成紫黑格，见 §5.3 第 ② 条。
### Phase 8 · 方块与模型（M8）
#### F-024 · 方块与物品注册（M · 1.5）
- **目标**：注册控制终端（`farmstorm:control_terminal`）的方块与 BlockItem，属性照铁砧七项，并进创造栏与两份语言文件（D-020、D-021）。
- **交付物**：`registry/FarmstormBlocks.java`（照 `:75-82` / `:105-112` 两组铁砧七项，注册走 `:127-131` 的 `registerBlock`）；`registry/FarmstormItems.java`（BlockItem 照 `:98-99` / `:104-105`，创造栏清单在 `:141` 的 `creativeTabItems()`，实读）；`datagen/ModLangProvider.java`（`item.farmstorm.control_terminal` 与 `block.farmstorm.control_terminal` 各 1 键）。
- **验收断言**：注册面 `items` **28 → 29**、`blocks` **28 → 29**，总数 **91 → 92**（W1 开窗，检查器此刻红）；两份语言文件键数各 **321 → 323**（W2 开窗）；两侧键集相等（验收 #3 前半）。
- **依赖**：无。
- **回滚影响**：F-025 / F-026 / F-030 / F-034 / F-040 / F-041 的依赖链断掉，需一并回滚。
#### F-025 · 模型与 blockstate（M · 1.5）
- **目标**：给终端出 **4** 个 facing variant 与一张 cube 模型，item 模型走方块模型而不是找不存在的物品贴图（D-022、D-023）。
- **交付物**：`datagen/ModBlockStateProvider.java`（照 `:195-206` 的转换器段，去掉 `WORKING` 与两模型切换，只留 `rotationY` 与 `:277-284` 的 `facingRotationY`）；`datagen/ModItemModelProvider.java:68` 的 `usesBlockModel(...)` 命中清单加 `ControlTerminalBlock`（调用点 `:45-46`，实读）。
- **验收断言**：验收 #4（`blockstates/control_terminal.json` 恰好 **4** 个 `facing=` variant）；验收 #27（同一文件里 `working` **0** 命中）；`models/block` **78 → 79**、`blockstates` **28 → 29**、`models/item` **28 → 29**，且 `models/item/control_terminal.json` 的 `parent` 是 `farmstorm:block/control_terminal`。
- **依赖**：F-023（贴图）、F-024（方块）。
- **回滚影响**：无（只影响渲染）。
### Phase 9 · 实体、连线与标签（M9）
#### F-026 · 方块实体与三态 NBT（M · 1.5）
- **目标**：把终端做成方块实体，模式三态存自己的 NBT、存序列化名而非 `ordinal()`，并先落 `CoreBound` 三方法（D-025、D-030、B-15）。
- **交付物**：`blockentity/ControlTerminalBlockEntity.java`（新增：嵌套 `Mode` 枚举 `OFF` / `CULTIVATOR` / `CONVERTER` + `saveAdditional` / `loadAdditional` + `corePos()` / `setCore()` / `clearCore()`，NBT 读写形状照 `CultivatorBlockEntity:338-348` / `:351-358`）；`registry/FarmstormBlockEntities.java:51-58`（第 9 个 BE 类型，照 `:56-58` 的单方块形态）；`block/ControlTerminalBlock.java`（`newBlockEntity` 与 `getTicker`，`getRenderShape` 照 `CultivatorBlock:55-57`）。
- **验收断言**：`block_entity_type` **8 → 9**；验收 #7 前半（`data get block` 读 `Mode` 依次读到 `off` → `cultivator` → `off` → `converter`，四个读数逐字命中——存序列化名的直接后果，D-030）；退出重进后同一读数不变。
- **依赖**：F-024。
- **回滚影响**：F-028 / F-029 / F-030 / F-033 / F-041 的依赖链断掉。
#### F-027 · Kind.TERMINAL 与协议版本 2（M · 1.5）
- **目标**：线上码追加第三个值、渲染器加绿色分支、协议串升一位；既有两值的三个分量一个字节不动（D-035 ~ D-038、B-21）。
- **交付物**：`network/CultivatorLinkPayload.java:73-75`（追加 `TERMINAL(2)`、两值不重排；`id()` 在 `:84-86`、`byId` 在 `:89-96` 不改）；`network/FarmstormNetwork.java:32`（协议串 `1` → `2`，`:30` 的陈旧注释同步改成「v0.3.0 起为 `2`」）；`client/CultivatorLinkRenderer.java`（绿色常量 `0.30 / 0.85 / 0.30` + `alpha 1.0`，照 `:51-53` 与 `:59-61` 两组写法；`switch` 在 `:100-109` 追加 `TERMINAL` 分支）。
- **验收断言**：验收 #24（`Kind` **3** 个值、`byId(2)` 返回 `TERMINAL`、`CULTIVATOR` 是 0 / `CROP_CONVERTER` 是 1 不变）；验收 #12（绿线像素 **0x4D / 0xD9 / 0x4D**，同屏青线与红线的分量不变）；验收 #20（启动日志两行带「版本 **2**」）；该 `switch` 无 `default`，漏改分支编译不过（D-038）。
- **依赖**：F-024。
- **回滚影响**：F-028 断链（类型表引用枚举，编译不过）；协议回到 `1`。
#### F-028 · 标签与 KIND_BY_BLOCK 同改（S · 0.5）
- **目标**：把终端同时写进 `farmstorm:processing_block`（成员 **2 → 3**）与 `KIND_BY_BLOCK`（**2 → 3**），一个提交改完（D-024）。
- **交付物**：`datagen/ModBlockTagsProvider.java:45-47`（`PROCESSING_BLOCK` 加第三个成员）；`blockentity/OutpostCoreBlockEntity.java:123-125`（`Map.of` 加第 3 条 `Link.Kind.TERMINAL`）；候选过滤 `:621-622` / `:635-638` 与 `isProcessingBlock` `:891-892` 不动。
- **验收断言**：`data/farmstorm/tags/blocks/processing_block.json` 的 `values` **3** 项；`KIND_BY_BLOCK` **3** 条且含 `TERMINAL`；放一台终端后核侧两条列表各 **+1** 且同下标（验收 #25 前半）。反向断言：只改标签时 `kindOf` 走 `:388-394` 的兜底返回 `CULTIVATOR`，连线画成青色、投料按培养器处理。
- **依赖**：F-024、F-026、F-027。
- **回滚影响**：F-029 断链；终端落进「被发现但不绑定」或「扫不到」两种静默状态。
### Phase 10 · 绑定与面板（M10）
#### F-029 · 放置绑定与三出口（M · 1.5）
- **目标**：终端与农作物培养器（`farmstorm:cultivator`）、农作物转换器（`farmstorm:crop_converter`）同形地进入三条绑定出入口——放置绑、补绑、拆除解绑；范围只用 `inRange` 一条判据（D-026、D-027、B-18、B-19）。
- **交付物**：`block/ControlTerminalBlock.java`（`setPlacedBy` 照 `CultivatorBlock:82-98`：`:90-91` 调 `selectCore`、`:95-97` 调 `addCultivator` + `setCore`、无核走 `:92-94` 的「不写 Core 键」出口；拆除交出照 `block/CultivatorBlock.java:142-148`）；核侧只复用既有入口：`addCultivator` `:413-426`、名额闸 `:401-403`、`inRange` `:532-534` 与 `:537-543`、补绑 `:608-656`，不新增方法。
- **验收断言**：3×3 区块内先核后终端则引用缓存非空且两条列表 **+1**；拆核再放核则终端自动改绑（重新出现 `Kind.TERMINAL` 的连线广播）；验收 #11（超出 3×3 区块既不绑定也不投料）；验收 #10 的绑定段（名额满 **24** 名后第 25 台不绑定）；验收 #25（两条列表同长同下标、末位为 terminal）。
- **依赖**：F-026、F-028。
- **回滚影响**：F-033 断链。附注：「三出口」指三个绑定 / 解绑出入口，不是 `CultivatorBlock:111-129` 那三个 PASS 出口；终端在 `use(...)` 的三处一律不 PASS、改开面板，那段随 F-030 落地（菜单类型到 F-030 才存在，提前写会编译不过）。
#### F-030 · MenuType 与 0 槽菜单（M · 1.5）
- **目标**：右键开 0 槽面板：1 个新 `MenuType`、菜单里没有槽位、`quickMoveStack` 直接返回空、状态走 2 格 `ContainerData`（D-028、D-031、D-034、B-14）。
- **交付物**：`menu/ControlTerminalMenu.java`（新增：槽位 **0** 个、`quickMoveStack` 返回 `ItemStack.EMPTY`、`clickMenuButton` 改模式、`stillValid` 照 `menu/OutpostCoreMenu.java:133-135`、`new SimpleContainerData(2)` + `addDataSlots`）；`registry/FarmstormMenus.java:17-18`（第 2 个 `MenuType`，1.20.1 只有 `(MenuSupplier, FeatureFlagSet)` 一个构造，`facts-B` §3.3）；`block/ControlTerminalBlock.java`（`use(...)` 三个出口一律不 PASS，`player.openMenu` 照 `block/OutpostCoreBlock.java:139`）。
- **验收断言**：`menu_type` **1 → 2**；验收 #6（右键开面板、槽位 **0** 个、标题读作「控制终端」）；验收 #7 后半（按钮把模式从 `off` 推到 `cultivator`、再点同一个退回 `off`，`data get` 同步）；验收 #26（未绑定时右键仍开面板、两个按钮置灰、核库存 **0** 变化，B-20）。
- **依赖**：F-026。
- **回滚影响**：F-031 / F-032 断链。附注：菜单构造必须是 `super(FarmstormMenus.CONTROL_TERMINAL.get(), id)`——`getType()` 在 `menuType == null` 时抛异常（`facts-B` §3.4 `:74-79`，RT-015）；F-030 提交时标题显示原始键名，验收 #6 的标题段在 F-032 之后复核。
#### F-031 · 面板屏幕与注册（M · 1.5）
- **目标**：把面板画出来（两个模式按钮 + 一行状态），并在客户端事件里注册屏幕；1.20.1 没有 `RegisterMenuScreensEvent`（D-028、D-034、机制差异 2）。
- **交付物**：`client/ControlTerminalScreen.java`（新增：2 个按钮 + 1 行状态，未绑定时 `active` 为假，选中态按 `ContainerData` 第 0 格）；`client/FarmstormClientSetup.java:31-34`（在 `FMLClientSetupEvent` 的 `enqueueWork` 里追加第二个 `MenuScreens.register`，与 `:33` 同形）。
- **验收断言**：验收 #31（`MenuScreens.register` 里 `CONTROL_TERMINAL` 命中 **1** 处且在 `FMLClientSetupEvent` 的 `enqueueWork` 内；全文件 `RegisterMenuScreensEvent` 命中 **0** 行）；验收 #26 的按钮置灰段；`textures/gui` 目录仍 **0** 文件（不新增 GUI 贴图，贴图 96 里没有 GUI 项）。
- **依赖**：F-030。
- **回滚影响**：无下游（界面缺文案；F-032 仍可提交）。
#### F-032 · 面板 5 语言键（S · 0.5）
- **目标**：补齐面板 **5** 个键，把语言键收到 **328**，并同步表头口径（D-018、D-051）。
- **交付物**：`datagen/ModLangProvider.java`（`container.farmstorm.control_terminal` 1 条 + 两个模式标签 + 已绑定 / 未绑定 2 条，共 5 键，两文件各 1 组）；同文件 `:10` 与 `:14-16` 的表头口径段从 `321` /「其余 42」改到 `328` /「其余 47」。
- **验收断言**：验收 #3（两份语言文件各 **328** 键且键集相同：`321 + 7 = 328`）；分组算式 `29 + 29 + 13 + 32 + 178 + 47 = 328`，其中「其余」`42 + 5 = 47`（container **+1**、gui **+4**，A-009）；W2 关窗：此后该表头不再出现 `321`。
- **依赖**：F-024、F-030、F-031。
- **回滚影响**：无下游（界面缺 5 条文案，键数回 323）。
### Phase 11 · 投料（M11）
#### F-033 · 每 20 刻投料循环（L · 3.0）
- **目标**：按当前模式每 **20** 刻从据点核心（`farmstorm:outpost_core`）的 54 格里给每台空槽机器投 **1** 个；`OFF` 不投料、不消费库存（D-031 ~ D-033、B-13、B-16、B-17）。
- **交付物**：`blockentity/ControlTerminalBlockEntity.java`（`serverTick`：间隔常量 **20** 写死、不新增 config，先例 `OutpostCoreBlockEntity.java:111` 的 `REBIND_INTERVAL_TICKS`；取核走 `:317-358` 的 `selectCore`；准入判据复用 `CultivatorBlockEntity.canInsert` `:152-154` 与 `CropConverterBlockEntity.canInsert` `:141-143`，不建第二份白名单，D-033；从核 54 格（`:139`）经 `getItem` `:257` / `removeItem` `:262` 取 **1** 个写进机器槽）。
- **验收断言**：验收 #8（相邻两次投料的 `gameTime` 差恒 **20**）；验收 #9（一轮核 **64 → 63**、机器槽 **1**；槽非空后 **200** 刻核库存 **0** 变化）；验收 #10 的库存段（名额满后核库存 **0** 变化）；`OFF` 时 **200** 刻 **0** 变化；数据包新增一条转换配方即自动多收一个投入物（与转换器同步，D-033）。
- **依赖**：F-026、F-029。
- **回滚影响**：无下游（除 F-045 复核）。
### Phase 12 · 规则线（M12）
#### F-034 · 两条挖掘标签（S · 0.5）
- **目标**：培养器、转换器、控制终端三个处理方块都能被石镐及以上挖出掉落物；木镐能破坏但掉落 0（D-039、D-040、B-22、B-23）。
- **交付物**：`datagen/ModBlockTagsProvider.java:49-63`（`mineable/pickaxe` 补 `CULTIVATOR` 与 `CONTROL_TERMINAL`，成员 **10 → 12**）；同文件新增一行 `tag(BlockTags.NEEDS_STONE_TOOL).add(...)` 照 `:67` 的 `NEEDS_DIAMOND_TOOL` 写法，成员恰 3 个（`BlockTags.NEEDS_STONE_TOOL` 见 `facts-B` §5.1 `:142`）。
- **验收断言**：验收 #13（木镐挖培养器掉落 **0** 个、方块仍被移除）；验收 #14（石镐挖同一块掉落 **1** 个）；验收 #19（金镐只记录、不断言——1.20.1 走 `ForgeHooks` → `DiggerItem` → `TierSortingRegistry`，金镐真值本仓读不出，D-041、RT-020）；`needs_stone_tool` 成员 **3**、`mineable/pickaxe` 成员 **12**。
- **依赖**：F-024（终端方块须先存在）。
- **回滚影响**：无（纯标签）。附注：回滚即恢复 B-23 记的既有缺口——培养器有 `requiresCorrectToolForDrops()` 但不在任何 `mineable/*` 里，破坏时 0 掉落。
#### F-035 · 史诗准入放行（S · 0.5）
- **目标**：删掉培养器准入闸里那道档位比较，火把花（`minecraft:torchflower`）、瓶子草（`minecraft:pitcher_plant`）、甜浆果（`minecraft:sweet_berries`）、金苹果（`minecraft:golden_apple`）四件自此可培养（D-042、D-043、B-24）。
- **交付物**：`blockentity/CultivatorBlockEntity.java:148`（把 `return output != null && output.tier() != CropTier.EPIC;` 改成 `return output != null;`）；`util/CropTier.java:24` 描述旧口径的注释不动——D-042 的落地位置只列前者，改它等于改墓碑 §5.1 的原文。
- **验收断言**：验收 #15（四件各成功投入 **1** 次并结算出产物）；该文件 `CropTier` 命中 **0** 行；同文件 `GOLDEN_APPLE` 与 `TORCHFLOWER` 各命中 **0** 行（无白名单，D-043；现状实测为 0）。
- **依赖**：无。
- **回滚影响**：无（独立行为，回滚即恢复旧口径）。
### Phase 13 · 干草块掉落（M13）
#### F-036 · PuffedHayData（M · 1.5）
- **目标**：建一份可存读的位置集合，供膨胀麦（`farmstorm:puffed_wheat`）登记与开箱时消费（D-045）。
- **交付物**：`util/PuffedHayData.java`（新增：`extends SavedData`、`LongOpenHashSet` 存 `BlockPos.asLong`、落盘名 `farmstorm_puffed_hay`）；1.20.1 的两个签名是 `SavedData.save(CompoundTag)` 一参（`facts-B` §4 `SavedData.java:15`）与 `DimensionDataStorage.computeIfAbsent(Function, Supplier, String)` 三参（`:38`），写法照 `outpost/OutpostCoreIndex.java:76-79` 与 `trap/TrapFifoIndex.java:59-60`。
- **验收断言**：验收 #32（`world/data/farmstorm_puffed_hay.dat` 存在，重启服务器后集合条数不变）；验收 #18 前半（老存档里已有的干草块不在集合里，照旧掉落，B-26）。
- **依赖**：无。
- **回滚影响**：F-037、F-038 断链。附注：三参顺序误写成 1.21.1 的两参 `SavedData.Factory` 会得到编译错或运行期 `ClassCastException`（RT-013）。
#### F-037 · 落点登记（S · 0.5）
- **目标**：膨胀麦每写成功一格原版干草块就登记一格；跳过分支不登记（D-046、B-25）。
- **交付物**：`entity/thrown/PuffedWheatProjectile.java:104-123`（`setBlock` 在 `:119`，登记写在其后；抗性跳过 `:113-115`、白名单跳过 `:116-118` 都在前，因此不登记）；登记前再判一次 `hay.is(Blocks.HAY_BLOCK)`——`fillBlock`（`:88-90`）被 `CompressedPuffedWheatProjectile.java:34-37` 覆写复用（压缩膨胀麦，`farmstorm:compressed_puffed_wheat`）。
- **验收断言**：掷一次膨胀麦后记录条数等于实际写入的干草块格数；被爆炸抗性挡掉的格子不在集合里；验收 #17（压缩干草块（`farmstorm:compressed_hay_block`）照旧掉 **1** 个、记录条数不变，B-25）；验收 #18 后半。
- **依赖**：F-036。
- **回滚影响**：F-038 断链（修饰器无记录可查，恒返回原表）。附注：不加 `is(Blocks.HAY_BLOCK)` 这道守卫就会把压缩干草块也登记进去（RT-017）。
#### F-038 · 战利品修饰器类与注册（L · 3.0）
- **目标**：1.20.1 没有方块掉落事件，第 ④ 条需求改由只挂 `minecraft:blocks/hay_block` 一张表的全局战利品修饰器（global loot modifier，GLM）承接；命中记录时返回空表并移除该条记录（D-047、D-048、B-27、机制差异 1）。
- **交付物**：`loot/PuffedHayLootModifier.java`（新增：`extends LootModifier`，只实现 `:68-69` 的 `doApply` 与静态 `CODEC`——`apply` 在 `:54-59` 是 final、带 conditions 判定）；`registry/FarmstormLootModifiers.java`（新增：`DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ...)`、泛型 `Codec<? extends IGlobalLootModifier>`，见 `facts-B` §2.2 `:187` / `:112`）；`Farmstorm.java:66` / `:68` 旁挂 mod 总线。
- **验收断言**：验收 #16（膨胀麦落点石镐挖则掉落 **0** 个、记录条数 **-1**）；方块仍被移除、经验照旧结算（D-048）；事件监听数保持 **9**（拦截面换了，D-018）；`global_loot_modifier_serializers` **0 → 1**，注册面 **92 → 93**（W1 内继续红）。
- **依赖**：F-036。
- **回滚影响**：F-039 断链；干草块恢复掉落，位置记录只增不减（RT-018）。附注：`LootContextParams.ORIGIN` 是 `Vec3` 不是 `BlockPos`，取整前不要直接当坐标用（`facts-B` §2.4 `:18`，RT-012）。
#### F-039 · 数据文件 provider（M · 1.5）
- **目标**：两份数据文件都由 datagen 产出、不手写；清单必须登记在 forge 命名空间（D-049、B-28）。
- **交付物**：`datagen/ModGlobalLootModifierProvider.java`（新增：`extends GlobalLootModifierProvider`，`start()` 里 `add("puffed_hay", ...)`，`facts-B` §2.1 `:60` / `:95-99`）；`datagen/FarmstormDataGen.java:23`（`gatherData(GatherDataEvent)` 里追加 `generator.addProvider(event.includeServer(), ...)` 照 `:31` / `:37` 的写法与 `:44` 的派生类顺序纪律，实读）；产物 `data/farmstorm/loot_modifiers/puffed_hay.json` 与 `data/forge/loot_modifiers/global_loot_modifiers.json`。
- **验收断言**：验收 #30（清单 `entries` 里有 `farmstorm:puffed_hay`）；loot modifier 数据文件计数 **0 → 2**；两份 JSON 均在 `src/generated` 下由 provider 落盘（重跑 runData 后字节不变）；反向断言（RT-011 的代价）：把清单 `entries` 临时清空后，挖记录内干草块应掉 **1** 个——零日志、静默失效。
- **依赖**：F-038。
- **回滚影响**：无下游；修饰器本体在而清单不在等于静默不生效。
### Phase 14 · 收口（M14）
#### F-040 · 合成配方（S · 0.5）
- **目标**：给终端一条与培养器、转换器两两不同的 3×3 配方：四角玻璃 + 四边铁锭 + 正中红石（D-029、A-010）。
- **交付物**：`datagen/ModRecipeProvider.java`（照 `:117-127` 的培养器段结构，图样 `GIG` / `IRI` / `GIG`，`G` 取 `minecraft:glass`、`I` 取 `minecraft:iron_ingot`、`R` 取 `minecraft:redstone`，产出 1 个；解锁进度用 `:174-201` 的 `recipeWithoutAdvancement` 剥掉）。
- **验收断言**：合成配方 **4 → 5**、配方 JSON **25 → 26**、转换配方 **21** 不变；按 `GIG` / `IRI` / `GIG` 摆出 **1** 个控制终端，按 `IGI` / `GRG` / `IGI` 仍出 **1** 个培养器（两条不串）。
- **依赖**：F-024。
- **回滚影响**：无。
#### F-041 · 战利品表（S · 0.5）
- **目标**：让终端被正确工具挖掉时掉自己（D-018、D-021）。
- **交付物**：`datagen/ModLootTableProvider.java:131-138`（`this.add(...)` 第 5 处，照 `:131` / `:135` / `:138` 三行同形）；`getKnownBlocks` `:200-231` 补第 29 条。
- **验收断言**：方块战利品表 **28 → 29**（验收 #22 的一段）；石镐挖终端掉 **1** 个；漏登记 `getKnownBlocks` 会在 validate 阶段报缺表，构建失败。
- **依赖**：F-024。
- **回滚影响**：无。
#### F-042 · 检查器收到 96 / 12 类（S · 0.5）
- **目标**：把静态检查器的期望面收到 **96（12 类）**，同时清掉基线里那条 `data_component_type` FAIL；**W1 关窗**（D-052、A-008、RT-019）。
- **交付物**：`tools/verify_project.py:458-471`（`EXPECTED_COUNTS` 五类同改：`items` 29 / `blocks` 29 / `block_entity_type` 9 / `menu_type` 2 / `:469` 的 `data_component_type: 1` 槽位换成 `global_loot_modifier_serializers: 1`）；`:448` 的同一槽位 ID 换成 `puffed_hay`；`:450` 的 `menu_type` 加 `control_terminal`；items / blocks / block_entity_type 三组清单各加 `control_terminal`；`:456` 的算式注释（12 类相加 92 → 96）与 `:609` 的分母注释同步。**六处同一提交**。
- **验收断言**：验收 #1（§2 打印「已核 registry 对象 **96 / 96**」且 12 类逐项相等）；验收 #2（FAIL **0** 项、WARN ≤ **1** 项；那 1 条 WARN 是 `run/config` 在仓内不存在导致 §1 走 `facts-A` §5 记的 warn 分支）；`data_component_type` 与 `temporary_enchantments` 各 **0** 命中（既有红转绿）。
- **依赖**：F-024、F-026、F-030、F-038。
- **回滚影响**：W1 窗口重新打开，检查器回到「92 / 96」的红。
#### F-043 · 启动自检收到 96 / 12 类（S · 0.5）
- **目标**：把运行期那条注册面断言同批收到 **96（12 类）**，与 F-042 成对（D-052）。
- **交付物**：`Farmstorm.java:177-201`（`:192` 的 `total == 91` 收到 `96`；`:193-195` 逐类值改 29 / 29 / 9 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 2 并补第 12 类 `globalLootModifierSerializers == 1`；`:191` 注释）；`:172-175` 的 javadoc 旧口径（写 98 的那段）与 `:205` 的「总数 92」同步。
- **验收断言**：启动日志末段出现「自检 注册对象 **96** 个：…」并以 `OK` 收尾；同一日志里 `:111-169` 的 `logSelfCheck` 六行仍全 `OK`（本版不动它）。
- **依赖**：F-024、F-026、F-030、F-038。
- **回滚影响**：与 F-042 必须成对；只回滚一个就会出现「日志绿、脚本红」或反过来的既有错位（D-052 补充）。
#### F-044 · README 与交付清单（S · 0.5）
- **目标**：把 README 的版本叙述改到 `0.3.0`，并写出 06 ~ 11 六份文档的交付清单（D-015、D-017）。
- **交付物**：`README.md:5`（版本行 `v0.2.8` 改到 `v0.3.0（1.20.1）`，实读）；`:39` / `:42`（jar 名 `farmstorm-0.3.0+mc1.20.1.jar`，实读）；新增交付清单段（六份文件的行数 / 字节 / SHA256）。
- **验收断言**：`README.md` 里 `06-DECISIONS-0.3.0-1.20.1.md` ~ `11-PROMPT-TASKS-0.3.0-1.20.1.md` 六个文件名各命中 **1** 次；版本行含 `0.3.0`；检查器仍 FAIL **0**；反向断言：`datagen/ModLangProvider.java` 的 `:80` / `:114` / `:123` / `:199` / `:213` 五处 man瓜 一字不动（D-050）。
- **依赖**：F-042、F-043（清单条目另需 06 ~ 11 六份文档落地）。
- **回滚影响**：无。
#### F-045 · 数据面复核与 datagen 重跑（M · 1.5）
- **目标**：重跑一次 runData，把 12 个产物面逐项对账，确认没有手写件、没有多余文件（D-018）。
- **交付物**：`src/generated/resources/` 全量重跑后的产物——新增 `blockstates/control_terminal.json`、`models/block/control_terminal.json`、`models/item/control_terminal.json`、`loot_tables/blocks/control_terminal.json`、`recipes/control_terminal.json`、`data/farmstorm/loot_modifiers/puffed_hay.json`、`data/forge/loot_modifiers/global_loot_modifiers.json`、`data/minecraft/tags/blocks/needs_stone_tool.json`，改动 `data/farmstorm/tags/blocks/processing_block.json` 与两份 lang；外加一份复核记录。
- **验收断言**：验收 #21（Java 文件 **151 → 159**，`151 + 8 = 159`：新增 8 个类 = 控制终端方块 / 方块实体 / 菜单 / 屏幕 + `PuffedHayData` + `PuffedHayLootModifier` + `FarmstormLootModifiers` + `ModGlobalLootModifierProvider`）；验收 #22（`models/block` **79** + `loot_tables` **29** + `recipes` **26** + `blockstates` **29**）；验收 #23（`advancements` **16** 不变、`farmstorm:converting` 配方 **21** 张不变）；验收 #28（终端恰好 **5** 个产物文件）；验收 #29 后半（贴图 **88 / 8 / 96**）；`git status` 干净（重跑不改任何已提交产物）。
- **依赖**：F-023 ~ F-041 全部。
- **回滚影响**：无（只重跑与核对）。

## §3 六张覆盖表
### 3.1 动作面（24 项 = 新增 10 + 修改 13 + 删除 1）
| # | 动作 | 项 | 任务 |
|---|---|---|---|
| 1 | 新增 | 方块 control_terminal | F-024 |
| 2 | 新增 | 物品 control_terminal（BlockItem） | F-024 |
| 3 | 新增 | 方块实体类型 control_terminal（8 → 9） | F-026 |
| 4 | 新增 | 菜单类型 control_terminal（1 → 2） | F-030 |
| 5 | 新增 | 控制面板屏幕 | F-031 |
| 6 | 新增 | util/PuffedHayData | F-036 |
| 7 | 新增 | loot/PuffedHayLootModifier + registry/FarmstormLootModifiers | F-038 |
| 8 | 新增 | datagen/ModGlobalLootModifierProvider + 2 份数据文件 | F-039 |
| 9 | 新增 | 合成配方 control_terminal（4 → 5） | F-040 |
| 10 | 新增 | minecraft:needs_stone_tool 合并（3 成员） | F-034 |
| 11 | 修改 | processing_block 成员 2 → 3 | F-028 |
| 12 | 修改 | KIND_BY_BLOCK 条目 2 → 3 | F-028 |
| 13 | 修改 | mineable/pickaxe 成员 10 → 12 | F-034 |
| 14 | 修改 | Link.Kind 值数 2 → 3 | F-027 |
| 15 | 修改 | 协议串 1 → 2 | F-027 |
| 16 | 修改 | 培养器准入闸（不再排除史诗输出） | F-035 |
| 17 | 修改 | 膨胀麦落点登记（无 → 每格一条） | F-037 |
| 18 | 修改 | 注册面 91（11 类）→ 96（12 类） | F-024、F-026、F-030、F-038，收口 F-042 / F-043 |
| 19 | 修改 | 检查器 EXPECTED_COUNTS 12 类 92 → 96 | F-042 |
| 20 | 修改 | 版本串 0.2.8 → 0.3.0 | F-022 |
| 21 | 修改 | 语言键 321 → 328 | F-024（2 键）、F-032（5 键） |
| 22 | 修改 | Java 文件 151 → 159 | F-026、F-030、F-031、F-036、F-038、F-039 |
| 23 | 修改 | 方块战利品表 28 → 29 | F-041 |
| 24 | 删除 | 培养器准入闸里的档位比较 | F-035 |

### 3.2 注册面（5 项，91 → 96）
| 类 | 基线 | 目标 | 新增对象 | 任务 |
|---|---|---|---|---|
| items | 28 | 29 | control_terminal | F-024 |
| blocks | 28 | 29 | control_terminal | F-024 |
| block_entity_type | 8 | 9 | control_terminal | F-026 |
| menu_type | 1 | 2 | control_terminal | F-030 |
| global_loot_modifier_serializers | 0 | 1 | puffed_hay | F-038 |
| 合计 | 91 | 96 | 91 + 1 + 1 + 1 + 1 + 1 = 96 | 四处注册 + 一处收口 |

其余 7 类不动：entity_type 13 / mob_effect 4 / sound_event 3 / recipe_type 1 / recipe_serializer 1 / creative_tab 1 / damage_type 3（D-018）。

### 3.3 语言键（7 键，321 → 328）
| # | 键 | 分组 | 任务 |
|---|---|---|---|
| 1 | item.farmstorm.control_terminal | item | F-024 |
| 2 | block.farmstorm.control_terminal | block | F-024 |
| 3 | container.farmstorm.control_terminal | 其余（container） | F-032 |
| 4 | gui.farmstorm.control_terminal.mode.cultivator | 其余（gui） | F-032 |
| 5 | gui.farmstorm.control_terminal.mode.converter | 其余（gui） | F-032 |
| 6 | gui.farmstorm.control_terminal.status.bound | 其余（gui） | F-032 |
| 7 | gui.farmstorm.control_terminal.status.unbound | 其余（gui） | F-032 |

分组口径见 A-009：container 记在「其余」组，与 item / block 两组分开算，故表头算式是 29 + 29 + 13 + 32 + 178 + 47 = 328。

### 3.4 config（0 项）
| 项 | 基线 | 目标 | 任务 |
|---|---|---|---|
| server 键 | 175 | 175 | 无（20 刻写死为常量，D-032） |
| client 键 | 3 | 3 | 无 |
| 落盘 toml | 175 | 175 | 无（run/config 在仓内不存在，facts-A §5） |

### 3.5 风险（RT-011 ~ RT-020，谁兜）
| 风险 | 标题 | 兜底任务 |
|---|---|---|
| RT-011 | 清单漏登记静默失效 | F-039（反向断言） |
| RT-012 | ORIGIN 是 Vec3 | F-038 |
| RT-013 | SavedData 三参顺序 | F-036 |
| RT-014 | MenuScreens 时序与客户端专属 | F-031 |
| RT-015 | 0 槽菜单 getType() 陷阱 | F-030 |
| RT-016 | 24 帧动画无先例 | F-023、F-025 |
| RT-017 | 压缩膨胀麦共用 fillHay 被误登记 | F-037 |
| RT-018 | 记录集合只增不减 | F-036、F-038（消费即移除） |
| RT-019 | 检查器既有错位 | F-042 |
| RT-020 | 金镐在 TierSortingRegistry 的真值不可读 | F-034（只记录不断言） |

### 3.6 1.20.1 等价实现（8 处，谁先实现谁验）
| # | 1.21.1 的机制 | 1.20.1 的替换 | 任务 |
|---|---|---|---|
| 1 | BlockDropsEvent 清掉落 | IGlobalLootModifier / LootModifier 只挂 minecraft:blocks/hay_block | F-038、F-039 |
| 2 | RegisterMenuScreensEvent | FMLClientSetupEvent 的 enqueueWork 里 MenuScreens.register | F-031 |
| 3 | CustomPacketPayload / StreamCodec | SimpleChannel + 手写 FriendlyByteBuf 编解码（既有形态不动） | F-027 |
| 4 | SavedData.Factory 两参 | computeIfAbsent(Function, Supplier, String) 三参 | F-036 |
| 5 | DataComponents.TOOL | ForgeHooks → DiggerItem → TierSortingRegistry | F-034 |
| 6 | 状态同步（1.21.1 亦无先例） | SimpleContainerData(2) + addDataSlots | F-030 |
| 7 | MenuType 多构造 / IMenuTypeExtension | 唯一构造 (MenuSupplier, FeatureFlagSet) + player.openMenu | F-030 |
| 8 | DirectionPropertyBlock | HorizontalDirectionalBlock + BlockStateProperties.HORIZONTAL_FACING | F-024、F-025 |

## §4 裁决 A-007 ~ A-012（写作期新立，共 6 条）
#### A-007 · 「序列化」在复杂度换算里的边界
- **结论**：单一方块实体的两三个 NBT 键**不算** L 档的「序列化」；协议结构、跨版本存档格式、多字段 payload、注册表 Codec 才算。
- **理由与影响**：F-026 只存 1 个模式序列化名 + 1 个 BlockPos 引用，按「含序列化」判 L 会让本版多出 3 个 L 档、人日虚高 6.0。落点：F-026 定为 M、F-036 定为 M；F-033（三态 + 20 刻状态机）与 F-038（Codec + 注册表分派 + 数据文件）定为 L。
#### A-008 · 中间态窗口拆两个 + 既有红单独记账
- **结论**：拆 W1（注册面，F-024 开 → F-042 关）与 W2（语言键表头，F-024 开 → F-032 关）两个窗口；基线里那 1 条 data_component_type FAIL **单独记一行**，由 F-042 与 W1 同批清掉，不进「缺陷」栏。
- **理由与影响**：两个窗口的关窗任务与禁写文件都不同（W1 禁 tools/verify_project.py，W2 禁 ModLangProvider.java:10 与 :14-16）；既有红与两个窗口无关，混进窗口表会被误读成本版引入。落点：§0.3 的两行窗口表 + 一段记账；F-042 的验收里那条 FAIL 表现为「由红转绿」。
#### A-009 · 语言键分组（container 记「其余」组）
- **结论**：container.* 记在表头的「其余」组，与 item. / block. 两组分开算；目标算式 29 + 29 + 13 + 32 + 178 + 47 = 328。
- **理由与影响**：ModLangProvider.java:14-16 现有的「其余 42」枚举里已含 1 个 container，照既有分组走，表头其余组的写法不用改口径；改分法会让「其余」组从 6 个子组变 5 个，与 facts-C §10.5 的实读对不上。落点：F-032 的表头同步、§3.3 的分组列。
#### A-010 · 终端配方取四角玻璃，而不是换材料
- **结论**：配方定 GIG / IRI / GIG（四角玻璃、四边铁锭、正中红石），与培养器 IGI / GRG / IGI、转换器 III / GRG / III 三者两两不同。
- **理由与影响**：原定「四角铁、四边玻璃」与培养器逐格同料同图样，两条 crafting_shaped 只会稳定命中一条（D-029 补充）；对调四角与四边不换材料，材料总数仍是 4 铁 + 4 玻璃 + 1 红石。落点：F-040，D-029 已同步。
#### A-011 · 贴图三数不许互借
- **结论**：textures/block 下三个数分开数——.png **84 → 88**、.mcmeta **5 → 8**、文件总数 **89 → 96**，三者不许互借。
- **理由与影响**：Get-ChildItem -File 会把 mcmeta 一起数进去得到 96；拿 96 当 PNG 数会让冻结值从 88 错成 95，并让 88 + 8 = 96 这条本版冻结的自洽关系失效。落点：F-023 的验收断言、F-045 的复核项、§0.4 第 1 条。
#### A-012 · 战利品修饰器的代码与数据文件拆成 F-038 / F-039 两个任务
- **结论**：代码面（修饰器类 + 序列化器注册 + 总线挂载）落 F-038（**L**），数据面（provider + 2 份 JSON）落 F-039（**M**）。
- **理由与影响**：两者的验证面不同——F-038 以「挖一格掉 **0**、记录 **-1**」验，F-039 以「清单 entries 含 farmstorm:puffed_hay」验；合成一个任务会得到 4 个文件一个 L，并把 RT-011（清单漏登记、零日志）这种最隐蔽失效与代码缺陷压进同一条断言。落点：F-038 = 3.0、F-039 = 1.5；Java 151 → 159 里的 ModGlobalLootModifierProvider 记在 F-039 名下。

## §5 执行顺序
### 5.1 三条可并行线
| 线 | 任务 | 说明 |
|---|---|---|
| **A 终端主线** | F-022 ~ F-033 | 版本串 → 贴图 → 方块 → 模型 → 实体 → 连线协议 → 标签类型表 → 绑定 → 菜单 → 屏幕 → 文案 → 投料 |
| **B 规则线** | F-034、F-035 | 挖掘标签与史诗准入彼此独立；F-035 连终端都不等，可在任何时刻插队 |
| **C 干草块线** | F-036 ~ F-039 | 四任务内部串行（记录 → 登记 → 修饰器 → 数据文件），与 A、B 两线无关 |

收口五项（F-040 ~ F-045）在三条线全部结算后串行。W2 在 A 线的 F-032 关窗，W1 在收口的 F-042 关窗。

### 5.2 推荐提交序列（24 个提交）
F-022 → F-023 → F-024 → F-025 → F-026 → F-027 → F-028 → F-029 → F-030 → F-031 → F-032 → F-033 → F-034 → F-035 → F-036 → F-037 → F-038 → F-039 → F-040 → F-041 → F-042 → F-043 → F-044 → F-045

一个任务一个提交。每个提交前跑一次构建（退出码必须 0）；F-042 与 F-043 之后每次再跑检查器；F-045 之后 git status 必须干净。

### 5.3 关键路径与两处易错依赖
关键路径：F-024 → F-026 → F-028 → F-029 → F-033 → F-045。人日：1.5 + 1.5 + 0.5 + 1.5 + 3.0 + 1.5 = **9.5**，占 27.0 的 **35.2%**（9.5 / 27.0 = 0.3519）。

两处容易看错的依赖：

1. **F-027 必须先于 F-028**：KIND_BY_BLOCK（OutpostCoreBlockEntity.java:123-125）要引用 Link.Kind.TERMINAL，枚举没落就编译不过；反过来先改标签再补枚举，会得到「标签 3 成员 + 兜底类型」的静默错版（D-024 补充）。
2. **F-023 必须先于 F-025**：贴图缺失不会让构建失败、退出码仍是 0，只在游戏里变成紫黑格；把资源单列成 Phase 7 的第一个任务，是为了让「资源先于模型」有一次可复核的顺序，而不是塞进 F-025 的交付物里被一并验收。

## §6 自检表
| 项 | 读数 | 判据 |
|---|---|---|
| 任务编号 | F-022 ~ F-045，**24** 条 | 连续无跳号 |
| Phase | 7 ~ 14，**8** 个 | 连续 |
| 里程碑 | M7 ~ M14，**8** 个 | 连续，不带连字符 |
| 裁决 | A-007 ~ A-012，**6** 条 | 连续 |
| 人日 | **27.0** = 12 × 0.5 + 10 × 1.5 + 2 × 3.0 = 6.0 + 15.0 + 6.0 | 加法可复算 |
| 五字段 | 24 个任务各 5 项（目标 / 交付物 / 验收断言 / 依赖 / 回滚影响） | 缺一不可 |
| 覆盖表 | **6** 张 | 动作面 24 / 注册面 5 / config 3 行 / 语言键 7 / 风险 10 / 等价实现 8 |
| 中间态窗口 | **2** 个 + 既有红 **1** 条 | W1 跨 19 个任务、W2 跨 9 个任务；既有红单列 |
| 关键路径 | 9.5 / 27.0 = **35.2%** | 加法可复算 |
| 禁用词 | **0** | 八条一律未出现（清单见 06 号 §0.3 第 3 条） |

> 本文自检：24 个任务连续无跳号；每任务五段齐全；六张覆盖表分别 24 / 5 / 7 / 3 / 10 / 8 全命中；6 条裁决；窗口 2 个 + 既有红 1 条单列；人日 6.0 + 15.0 + 6.0 = 27.0 自洽；关键路径 9.5 / 27.0 = 35.2%。
