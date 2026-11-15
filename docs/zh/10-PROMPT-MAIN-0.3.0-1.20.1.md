# 10 号 v0.3.0（1.20.1）主提示词（实现方开工前只读一次）

> 定位：实现方动第一行代码之前读一次本文，之后每个 Phase 只投 11 号的对应那一节。本文不复述 06 / 08 的论证，只给五样东西——现状读数、不许动的、不许做的、会炸的地方、交付与回报口径。
> 上游：06 号（`docs/zh/06-DECISIONS-0.3.0-1.20.1.md`，D-015 ~ D-052，唯一真相源）/ 07 号（B-13 ~ B-28）/ 08 号（RT-011 ~ RT-020、验收 #1 ~ #32）/ 09 号（F-022 ~ F-045、Phase 7 ~ 14、M7 ~ M14、A-007 ~ A-012）/ `.work/v030/facts-A-numbers.md`（数字面）、`facts-B-api.md`（1.20.1 API 等价面）、`facts-C-anchors.md`（现有代码锚点）。行号一律取 facts-B / facts-C 的实读值，不自己量。
> 编号：本文只发两个系列——硬约束 **HC-011 ~ HC-028**（18 条）、禁止行为 **N-009 ~ N-022**（14 条）。其余系列一律写全前缀（D-015 起 / B-13 起 / F-022 起 / Phase 7 起 / M7 起 / RT-011 起 / A-007 起 / E-7-1 起），不认领下游编号。
> 底线：基线是 **1.20.1 v0.2.8 已实现态**（`gradle.properties:11` 的 `mod_version=0.2.8`，注册面 91 / 11 类）；本版只做 1.21.1 v0.3.0 的四条需求 + 使它们成立的等价替换与连带改动。判据只认五类读数——计数 / 日志行 / `/data get` 读数 / grep 命中 / 像素比对。

---

## 1. 事实卡（22 行，每行一个可观察读数）

| # | 事实（读数 / 现状） | 出处 |
|---|---|---|
| 1 | 注册面 **91（11 类）**，逐类写死成 `==`；同一时刻检查器写的是 **12 类 / 92**（含 1.20.1 不存在的 `data_component_type`）⇒ 两处断言从移植期起就不同口径 | `Farmstorm.java:192-195`；`tools/verify_project.py:458-471`（`:469` 与 `:448`）；facts-A §0 第 1 / 2 项、§2.1 |
| 2 | `KIND_BY_BLOCK` 只有 **2 条**（培养器（`farmstorm:cultivator`）/ 转换器（`farmstorm:crop_converter`））；取不到值就 `continue`（只被发现、不绑定），`kindOf` 兜底返回 `CULTIVATOR` | `OutpostCoreBlockEntity.java:123-125`、`:117-118`、`:388-394`；facts-C §1 |
| 3 | `farmstorm:processing_block` 现有 **2 成员**（同样那两台） | `datagen/ModBlockTagsProvider.java:45-47`；`util/FarmstormTags.java:30`；facts-C §10.1 |
| 4 | 培养器**不在** `mineable/pickaxe`（该标签 10 名成员里没有它），却已带 `requiresCorrectToolForDrops()` ⇒ 破坏时掉落 0 个 | `ModBlockTagsProvider.java:49-63`；`registry/FarmstormBlocks.java:105-112`；facts-C §10.1、§15 第 4 项 |
| 5 | `Kind` 现 **2 值** `CULTIVATOR(0)` / `CROP_CONVERTER(1)`，1 字节上线；`byId` 未知码回落 `CULTIVATOR` | `network/CultivatorLinkPayload.java:73-75`、`:84-86`、`:89-96`；facts-B §8 |
| 6 | 协议串现值 `"1"`；两个包 discriminator 是 0 / 1，均 `PLAY_TO_CLIENT` | `network/FarmstormNetwork.java:32`、`:51`、`:60`；facts-A §6 |
| 7 | 界面注册点**只有 1 处**，且在 `FMLClientSetupEvent` 的 `enqueueWork` 里 | `client/FarmstormClientSetup.java:31-34`（register 在 `:33`）；facts-C §12；本次 grep `MenuScreens\.register` = 1 |
| 8 | `ContainerData` / `addDataSlots` 在本仓 **0 命中**（接口本身存在于 `ContainerData.java:3`） | grep `ContainerData` = 0 且 `addDataSlots` = 0；facts-B §3.2、facts-C §9 |
| 9 | `SavedData` 的抽象方法是 `CompoundTag save(CompoundTag)`（无 `Provider` 形参）；`computeIfAbsent` 是**三参** | `SavedData.java:15`、`DimensionDataStorage.java:38`；先例 `outpost/OutpostCoreIndex.java:76-79`、`trap/TrapFifoIndex.java:59-60`；facts-B §4 |
| 10 | 全局战利品修饰器要**两份**数据文件：`data/<ns>/loot_modifiers/<name>.json` + 清单 `data/forge/loot_modifiers/global_loot_modifiers.json`（清单命名空间固定 `forge`） | `LootModifierManager.java:43`、`:53`、`:75`；`GlobalLootModifierProvider.java:95`；facts-B §2.3 |
| 11 | `LootContextParams.ORIGIN` 是 `LootContextParam<Vec3>`（不是 `BlockPos`） | `LootContextParams.java:18`；facts-B §2.4 |
| 12 | 挖掘判定链换符号：`ForgeHooks.isCorrectToolForDrops` → `DiggerItem` → `TierSortingRegistry`（1.20.1 无 `DataComponents`） | `ForgeHooks.java:226-232`、`DiggerItem.java:68-75`（`:74` 的 `needs_stone_tool` 分支）/ `:80-82`、`TierSortingRegistry.java:123-134`；facts-B §5.2 |
| 13 | 语言键 **321 × 2**、键集零差异；按组 28 + 28 + 13 + 32 + 178 + 42 | `datagen/ModLangProvider.java:10`、`:14-16`；`lang/zh_cn.json` 与 `en_us.json` 各 321；facts-A §3.3 |
| 14 | 事件监听 **9 行** `addListener`，类注释写「只做九件事」 | `event/FarmstormGameEvents.java:68-78`、`:30`；facts-C §8 |
| 15 | 膨胀麦（`farmstorm:puffed_wheat`）落点：两条跳过分支在前（抗性 `:113-115`、白名单 `:116-118`）、`setBlock` 在后（`:119`），半径 `edge / 2` | `entity/thrown/PuffedWheatProjectile.java:104-123`；facts-C §6 |
| 16 | 压缩膨胀麦（`farmstorm:compressed_puffed_wheat`）**只覆写 `fillBlock()`**，与膨胀麦共用同一份 `fillHay`；压缩干草块（`farmstorm:compressed_hay_block`）由它写入 | `entity/thrown/CompressedPuffedWheatProjectile.java:34-37`、`PuffedWheatProjectile.java:106`；facts-C §6 |
| 17 | 方块战利品表 **28 张**（类注释仍写 27 张），`getKnownBlocks` 也列 28 条 | `datagen/ModLootTableProvider.java:125-138`、`:200-231`、`:35-36`；facts-C §10.4 |
| 18 | 界面按钮**本仓 0 先例**：`net.minecraft.client.gui.components` 命中 0，`Button` 的 2 处命中都是 `InputEvent.MouseButton` | grep `gui\.components` = 0；grep `Button` = 2（`client/InputLockHandler.java:23`、`:113`） |
| 19 | blockstate 由 else-if 链 + `simpleBlock` 兜底分派；朝向助手 `facingRotationY`（EAST 90 / SOUTH 180 / WEST 270 / 其余 0）现成 | `datagen/ModBlockStateProvider.java:70-94`、`:277-284`（`:274-275` 注释：不能用 `Direction#toYRot()`）；facts-C §10.3 |
| 20 | item 模型白名单只有**三类**方块类（`CropConverterBlock` / `OutpostCoreBlock` / `CultivatorBlock`），漏登记落到 `basicItem` | `datagen/ModItemModelProvider.java:68-70`、`:45-46`、`:55-57` |
| 21 | `recipeWithoutAdvancement(Consumer<FinishedRecipe>)` 现成：进度 id 与序列化都返回 `null` | `datagen/ModRecipeProvider.java:174-201`（`:192-199`）；facts-C §10.2 |
| 22 | 名额 **24** 读 config；**54** 是核的格子数；两条平行列表同长同下标；3×3 区块来自 `RANGE_CHUNKS = 1` | `FarmstormConfig.java:944-946`；据点核心（`farmstorm:outpost_core`）`OutpostCoreBlockEntity.java:67`、`:139`、`:147`、`:154`、`:89`、`:537-543`；facts-C §1 |

读法：第 1 / 2 行是**两套闸**——运行期断言读 `getEntries().size()`，检查器读源码里的注册 id 清单，改数时两处都动（陷阱 9、31）。第 3 / 4 行是既有缺口（标签 2 成员、培养器挖不出掉落物）。第 5 / 6 行是线上编码的两处单点。第 7 ~ 9 行是界面与状态通道的三处「本版第一次」。第 10 / 11 行是战利品修饰器的两处接口错位。第 12 行说明挖掘判定一行代码都不用写。第 13 / 14 行是两处「加了东西忘同步」的口径面。第 15 / 16 行的登记只能落在一条循环里。第 17 / 18 行是两处产物面。第 19 ~ 21 行是 datagen 的三处现成助手。第 22 行钉住名额与格子数不同义。

---

## 2. 硬约束 HC-011 ~ HC-028（18 条，每条「结论 + 判据 + 依据」）

- **HC-011** · 注册面两处断言与五处注册改动同批收到 96（12 类）：`tools/verify_project.py:458-471` 的 `EXPECTED_COUNTS` 把 `data_component_type` 槽位换成 `global_loot_modifier_serializers = 1`（`items 29` / `blocks 29` / `block_entity_type 9` / `menu_type 2`），`Farmstorm.java:192-195` 改到 `total == 96` 与同名五类新值，其余 7 类 `==` 一字不动；判据：检查器打印 `已核 registry 对象: 96 / 96` 且 12 类小计逐项相等、启动日志该行末段 `OK`；依据：D-052、D-018、facts-A §1.3、§2.1。
- **HC-012** · 标签与 `KIND_BY_BLOCK` 同一任务改完：`farmstorm:processing_block` 2 → 3、`KIND_BY_BLOCK` 2 → 3（追加 `CONTROL_TERMINAL → Link.Kind.TERMINAL`），控制终端（`farmstorm:control_terminal`）两处都写；判据：`ModBlockTagsProvider.java:45-47` 与 `OutpostCoreBlockEntity.java:123-125` 各命中 `CONTROL_TERMINAL` 1 处、生成的 `processing_block.json` 读到 3 项；依据：D-024、06 号 §4 红线 1、facts-C §1 / §10.1。
- **HC-013** · `Kind` 既有两值的线上码不重排：`CULTIVATOR(0)` / `CROP_CONVERTER(1)` 逐字保留，`TERMINAL` 只追加为 `(2)`；判据：`CultivatorLinkPayload.java:73-75` 三行逐字比对通过、`byId(2)` 返回 `TERMINAL`；依据：D-035、06 号 §4 红线 2、facts-B §8。
- **HC-014** · 穷尽式 `switch` 补 `TERMINAL` 分支：`CultivatorLinkRenderer.java:100-109` 那段没有 `default`，漏改编译不过；模式到 `Kind` 的换算写成显式映射，不借用任一枚举的 `ordinal()`；判据：该渲染类 `TERMINAL` 命中 ≥ 1、构建退出码 0；依据：D-038、D-030、facts-C §7。
- **HC-015** · 准入判据只有一个来源：培养模式只调培养器自己的 `admissible` / `canInsert`，转换模式只调转换器的 `convertible` / `canInsert`；判据：终端两个新类里 `Items.`、`CropTier`、`Ingredient` 命中 0；依据：D-033、D-043、06 号 §4 红线 3、facts-C §2 / §5。
- **HC-016** · `man瓜` 五处一字不动（`ModLangProvider.java:80` / `:114` / `:123` / `:199` / `:213`）；判据：`Select-String -Pattern man瓜` 在该文件仍命中 5 行、五行 `git diff` 为 0；依据：D-050、06 号 §4 红线 4、facts-C §10.5。
- **HC-017** · config 一个键不增：server 175 / client 3 保持，终端节拍是常量；判据：`FarmstormConfig.java` 的 define 家族调用仍 175、`control_terminal` 在 `FarmstormConfig.java` 与 `FarmstormClientConfig.java` 命中 0；依据：D-032、06 号 §4 红线 5、facts-A §5。
- **HC-018** · 战利品修饰器只挂干草块（`minecraft:hay_block`）一张表，且在 `data/forge/loot_modifiers/global_loot_modifiers.json` 的 `entries` 里登记；判据：`data/farmstorm/loot_modifiers/puffed_hay.json` 里 `minecraft:blocks/hay_block` 命中 1（无第二张表名）、清单 `entries` 含 `farmstorm:puffed_hay` 1 处；依据：D-047、D-049、facts-B §2.3（`LootModifierManager.java:53`、`:75`）。
- **HC-019** · `ORIGIN` 必须转坐标：`context.getParam(LootContextParams.ORIGIN)` 拿到的是 `Vec3`，用 `BlockPos.containing(...)` 或等价取整后再查集合，不许直接当 `BlockPos` 用；判据：修饰器类里 `ORIGIN` 命中 1、`BlockPos` 命中 ≥ 1 且与 `Vec3` 同一方法体；依据：D-047、facts-B §2.4（`LootContextParams.java:18`）、06 号 §7 第 7 行。
- **HC-020** · 位置记录用 `SavedData` 的三参形态：`computeIfAbsent(PuffedHayData::load, PuffedHayData::new, DATA_NAME)`，`save(CompoundTag)` 只有 1 个形参（没有 `HolderLookup.Provider`），`add` / `remove` 之后各调一次 `setDirty()`；判据：`computeIfAbsent` 调用 3 个实参、`save` 声明 1 个形参、`setDirty()` 命中 ≥ 2；依据：D-045、facts-B §4（`SavedData.java:15`、`DimensionDataStorage.java:38`）、先例 `OutpostCoreIndex.java:76-79`。
- **HC-021** · 登记点在 `fillHay` 的循环内、`setBlock` 返回 true 之后，且登记前再判一次 `hay.is(Blocks.HAY_BLOCK)`；判据：登记语句位于 `PuffedWheatProjectile.java:113-115` / `:116-118` 两条 `continue` 与 `:119` 之后、压缩膨胀麦的落点坐标不进集合；依据：D-046、facts-C §6。
- **HC-022** · 投料：只填空槽、每台机器每轮最多 1 个、节拍写死 `20` 刻；判据：`removeItem(` 第二实参字面量 1、投成功后 `break` 命中 1、常量声明为 `private static final ... = 20`；依据：D-032、D-033、facts-C §1（`REBIND_INTERVAL_TICKS = 100` 的同类写死先例）。
- **HC-023** · 名额 24 与 54 格不同义：终端只走 `addCultivator` 与核侧两条既有平行列表，不新建列表；判据：终端两个新类里 `List<BlockPos>` 命中 0、`SLOT_COUNT` 只读不改、`addCultivator` 命中 ≥ 1；依据：D-026、facts-C §1（`:147` / `:154` / `:401-403`）、`FarmstormConfig.java:944-946`。
- **HC-024** · 协议串 `"2"` 且 payload 字段 0 改：仍是两个 `BlockPos` + 一个 `Byte`；判据：`FarmstormNetwork.java:32` 读到 `"2"` 1 处、`CultivatorLinkPayload` 里 `writeBlockPos` × 2 与 `writeByte` × 1 逐字未变；依据：D-037、facts-B §8。
- **HC-025** · 语言键 328 与表头同批：`ModLangProvider.java:10-16` 的 `321` 与按组算式改到 328（29 + 29 + 13 + 32 + 178 + 47，其余 +5 是 container +1 / gui +4）；判据：两份 lang 顶层键各 328、键集相同、表头算式可复算；依据：D-018、facts-A §3.3、facts-C §10.5。
- **HC-026** · 检查器三处同批收到 96 / 12 类：`EXPECTED_COUNTS`（`:458-471`）、ID 清单（`:408-451`）、算式注释（`:453-457`）；判据：`sum(EXPECTED_COUNTS.values())` = 96、`data_component_type` 与 `temporary_enchantments` 在该文件命中 0；依据：D-052、facts-A §2.1 ~ §2.3、06 号 §8 第 5 项。
- **HC-027** · 7 个贴图手放、逐字节复制、不改名不重采样（4 PNG = 三张 32 × 768 + 一张 32 × 32；3 个 mcmeta 各 24 帧、`frametime` 2）；判据：`textures/block` 的 `.png` 88 / `.mcmeta` 8 / 合计 96，PNG 高 768 = 24 × 32；依据：D-023、facts-A §3.2 / §3.5、06 号 §8 第 2 项。
- **HC-028** · 每个任务收尾 `build-1.20.1.cmd` 退出码 0 是入场券、不是收尾项，失败即停、不带病进下一个任务；判据：该命令退出码 0（非 0 就地停下报读）；依据：D-019（不留静默失败）、本文 §5.3。

---

## 3. 禁止行为 N-009 ~ N-022（14 条，每条三段）

- **N-009** · 新增第三个 S2C 网络包来同步模式
  - **不要做什么**：不要为模式与绑定状态新开 payload，也不要给 `FarmstormNetwork` 加第 3 个 `messageBuilder`。**为什么**：两格状态用 `SimpleContainerData` + `addDataSlots` 由 `broadcastChanges` 自动同步，多一个包要多一次协议变更与一条注册记录。**会看到什么后果**：网络包 2 → 3、注册面多 1 项（96 → 97），验收 #1 的 96 / 96 与 §5.4 的签字读数一起破。
- **N-010** · 在终端侧另写一份物品准入白名单
  - **不要做什么**：不要按 `CropProfile` 或物品列表挑作物，也不要在终端抄一份「史诗例外」。**为什么**：判据只有一个来源；数据包新增一条转换配方时两处必然不同步。**会看到什么后果**：数据包加一条转换配方后培养器多收一个、终端不收，验收 #15 的四条史诗输入至少一条进不了槽，面板里没有任何报错。
- **N-011** · 用「取消破坏」代替整表替换
  - **不要做什么**：不要写取消语义，也不要覆写 `Block#playerDestroy` 去拦掉落。**为什么**：1.20.1 没有方块掉落事件，`apply` 的返回值就是**整表替换**（`ForgeHooks.java:1198`），空表等于零掉落。**会看到什么后果**：方块移除这一步也要自己接管，别家 mod 的 `BreakEvent` 处理器与掉落钩子一起失效，验收 #17 / #18 双红。
- **N-012** · 把修饰器挂到别的战利品表
  - **不要做什么**：不要挂 `minecraft:blocks/*` 之外的通配，也不要顺手登记到第二张表。**为什么**：范围只限膨胀麦写进去的那一格干草块，挂宽了会改别人的干草块。**会看到什么后果**：所有干草块都不掉落（含原版自然生成的），回滚面跨到数据包。
- **N-013** · 给终端加 `working` 属性或第二套模型
  - **不要做什么**：不要为了动画加方块状态属性，也不要按工作态切图。**为什么**：三面动画自带 mcmeta 自己播，与方块状态无关（D-022）。**会看到什么后果**：blockstate 由 4 个 variant 变 8 个、方块模型由 1 张变 2 张，验收 #4 当场红，模型 79 与 blockstate 29 两个冻结数一起破。
- **N-014** · 手改 `src/generated` 下的产物
  - **不要做什么**：不要手写 `blockstates` / `models` / `loot_tables` / `recipes` 的产物，也不要手写两份 loot modifier JSON。**为什么**：这些目录是 datagen 产物，手改会被下一次 `runData` 覆盖，且 `getKnownBlocks` 与白名单的漏登记被手写掩盖。**会看到什么后果**：重跑 datagen 后改动消失，验收 #22 的四个计数时对时错；最坏情况是模型引用了尚未生成的贴图名。
- **N-015** · 用 `simpleBlock(block)` 生成终端 blockstate
  - **不要做什么**：不要把带 `HORIZONTAL_FACING` 的方块塞进单态兜底。**为什么**：`simpleBlock` 只生成 `""` 单态，四个朝向里三个找不到模型。**会看到什么后果**：进游戏后三个朝向是紫黑格或缺模型方块，验收 #4 的 `variants` 键数读到 1 而不是 4。
- **N-016** · 把 `Mode` 的枚举序当 `Kind` 的线上码
  - **不要做什么**：不要用 `mode().ordinal()` 去填 `Link.Kind`，也不要为了对齐重排任一枚举。**为什么**：模式三态是 `OFF` / `CULTIVATOR` / `CONVERTER`，`Kind` 是 `CULTIVATOR(0)` / `CROP_CONVERTER(1)` / `TERMINAL(2)`，两套编号不同轴。**会看到什么后果**：培养器的线画成绿色、终端的线画成青色，验收 #12 红，而构建与运行都不报错。
- **N-017** · 先写终端引用缓存、后写核侧
  - **不要做什么**：不要把 `setCore` 放在 `addCultivator` 之前，也不要在它返回 false 时照写。**为什么**：核侧两条列表是唯一真相，`addCultivator` 在已绑或满员时返回 false（`OutpostCoreBlockEntity.java:414`）。**会看到什么后果**：面板状态行显示「已绑定」但一个都不投料，验收 #10 与 #25 同时红。
- **N-018** · 在终端另存一份名额列表，或按 54 判满
  - **不要做什么**：不要新建 `List<BlockPos>` 存绑定，也不要用 54 当容量。**为什么**：名额上限读 `outpost_core.cultivatorCapacity`（默认 24，`FarmstormConfig.java:944-946`），54 是核的格子数。**会看到什么后果**：名额口径变两处，拆核后终端仍占旧名单或第 25 台被放进去，验收 #10 与 #25 的读数与核侧不一致。
- **N-019** · 改 `FarmstormTags.java`，或把类型塞进标签
  - **不要做什么**：不要新增标签常量，也不要用标签表达「是哪一类机器」。**为什么**：`PROCESSING_BLOCK` 已存在（`FarmstormTags.java:30`），成员由 datagen 决定；类型只由 `KIND_BY_BLOCK` 决定。**会看到什么后果**：多出一个与注册面无关的 `TagKey`，`FarmstormTags` 成为类型真相的第二份，两处取值不一致时编译器不报错。
- **N-020** · 把金镐写成断言，或改原版 `incorrect_for_*`
  - **不要做什么**：不要改 `data/minecraft` 下的 `incorrect_for_*` 标签，也不要在验收里断言金镐掉不掉。**为什么**：本版只保证木镐掉落 0、石镐掉落 1，金镐的真值由 `TierSortingRegistry` 决定，本仓读不出。**会看到什么后果**：验收 #19 变成必红条目，或 5 个局部覆盖被改成非原版语义、影响整合包里所有方块。
- **N-021** · 绕过 `MenuType` 让 `getType()` 抛异常
  - **不要做什么**：不要用 `super(null, id)` 造菜单，也不要自造一个不进注册表的 `MenuType` 实例。**为什么**：`AbstractContainerMenu#getType()` 在 `menuType == null` 时抛 `UnsupportedOperationException`（`AbstractContainerMenu.java:74-79`），开屏路径要读它。**会看到什么后果**：右键后客户端抛异常、面板打不开，验收 #6 / #26 红，状态两格也读不到。
- **N-022** · 只改脚本、不改 Java 自检
  - **不要做什么**：不要只动 `tools/verify_project.py` 而把 `Farmstorm.java` 的断言留旧值。**为什么**：注册面断言有两处（脚本 + 启动自检），两处从移植期起就不同口径（D-052）。**会看到什么后果**：检查器 96 / 96 全绿而启动日志该行末段 `FAIL`，两套读数互相矛盾且没有编号验收能发现它，「日志绿、脚本红」的既有错位固化到下一版。

---

## 4. 陷阱清单（34 条，每条四段：症状 / 原因 / 正确做法 / 依据）

- **陷阱 1 · RT-011 · 清单漏登记，修饰器静默不生效**
  - 症状：两份数据文件都在，进游戏挖膨胀麦落点的干草块照旧掉 1 个，日志无一行提示。／原因：`LootModifierManager` 只读 `forge` 命名空间那一份清单，不在 `entries` 里的修饰器一律不生效（`LootModifierManager.java:53`、`:75` 的注释）。／正确做法：由 `ModGlobalLootModifierProvider` 用 `add(...)` 产出两份，验收 #30 直接读清单 `entries`。／依据：RT-011、D-049、facts-B §2.3。
- **陷阱 2 · RT-012 · 把 `ORIGIN` 当 `BlockPos`**
  - 症状：直接赋值编译不过；改成手写 `new BlockPos(v.x, v.y, v.z)` 时漏了取整，坐标偏一格、挖了不掉。／原因：`ORIGIN` 是 `LootContextParam<Vec3>`（`LootContextParams.java:18`）。／正确做法：`BlockPos.containing(context.getParam(LootContextParams.ORIGIN))`（或等价取整）再 `asLong()` 查集合。／依据：RT-012、D-047、facts-B §2.4。
- **陷阱 3 · RT-013 · `SavedData` 三参顺序与 `save` 签名**
  - 症状：`computeIfAbsent` 编译不过，或把工厂对象当第一参传出运行期 `ClassCastException`；`save` 写成两参覆写不成立。／原因：1.20.1 是 `(Function, Supplier, String)`（`DimensionDataStorage.java:38`）、`save(CompoundTag)` 只有 1 个形参（`SavedData.java:15`）。／正确做法：照 `OutpostCoreIndex.java:76-79`：`computeIfAbsent(PuffedHayData::load, PuffedHayData::new, DATA_NAME)` + `public CompoundTag save(CompoundTag tag)`。／依据：RT-013、D-045、facts-B §4。
- **陷阱 4 · RT-014 · 屏幕漏注册或注册在客户端之外**
  - 症状：服务端能开菜单，客户端只有一条 warn、界面不出现。／原因：1.20.1 没有 `RegisterMenuScreensEvent`，注册必须进 `FMLClientSetupEvent` 的 `enqueueWork`，且类上带 `value = Dist.CLIENT`。／正确做法：在 `FarmstormClientSetup.java:31-34` 那一段里追加 `MenuScreens.register(FarmstormMenus.CONTROL_TERMINAL.get(), ControlTerminalScreen::new)`，与菜单类型同一任务。／依据：RT-014、D-034、facts-B §3.1、facts-C §12、验收 #31。
- **陷阱 5 · RT-015 · 0 槽菜单的 `getType()`**
  - 症状：右键后客户端抛 `UnsupportedOperationException: Unable to construct this menu by type`。／原因：`getType()` 在 `menuType == null` 时抛（`AbstractContainerMenu.java:74-79`）。／正确做法：菜单构造传 `FarmstormMenus.CONTROL_TERMINAL.get()`，注册走 `new MenuType<>(ControlTerminalMenu::new, FeatureFlags.VANILLA_SET)`，开屏走 `player.openMenu`。／依据：RT-015、D-034、facts-B §3.3 / §3.4。
- **陷阱 6 · RT-016 · 照抄本仓 16 帧的 mcmeta**
  - 症状：三面只播前 16 帧、第 17 ~ 24 帧永不出现，或帧条被压成 32 × 512。／原因：本仓现有 5 个 mcmeta 全是 16 帧 / 32 × 512（facts-A §3.5），照抄少 8 帧。／正确做法：mcmeta 用素材包那份（24 帧、`frametime` 2），PNG 逐字节复制；进游戏对着一台终端数一轮 24 帧（48 刻 = 2.4 秒）。／依据：RT-016、D-023、06 号 §8 第 3 项、验收 #5。
- **陷阱 7 · RT-017 · 压缩膨胀麦共用 `fillHay` 被误登记**
  - 症状：压缩干草块被挖时也掉落 0 个。／原因：`fillHay` 只被一份实现共用，压缩版只覆写 `fillBlock()`（`CompressedPuffedWheatProjectile.java:34-37`）。／正确做法：登记前加 `hay.is(Blocks.HAY_BLOCK)`，再判 `setBlock` 成功。／依据：RT-017、D-046、facts-C §6、验收 #17。
- **陷阱 8 · RT-018 · 记录集合只增不减**
  - 症状：`farmstorm_puffed_hay.dat` 随时间单调变长。／原因：只有「开 hay_block 表且坐标命中」一条消费路径；活塞推走、爆炸、`/setblock` 覆盖都不走它。／正确做法：消费点唯一（`doApply` 命中即移除该条记录），记录量级与膨胀麦落点同阶，不额外做清理。／依据：RT-018、D-047、facts-B §2.5。
- **陷阱 9 · RT-019 · 检查器既有错位被当成新 bug**
  - 症状：F-042 之前跑检查器必打 1 条 FAIL（`data_component_type` / `temporary_enchantments`）。／原因：脚本是 12 类 92（含 1.20.1 不存在的类），运行期自检是 11 类 91，`temporary_enchantments` 在全 src 0 命中。／正确做法：按「期望红」记录、不提前改；W1 窗口内其余任务不动这两个文件，F-042 与 F-043 同批收口。／依据：RT-019、D-052、facts-A §0 第 1 / 2 项、§2.1、§2.2。
- **陷阱 10 · RT-020 · 金镐的真值就读不出**
  - 症状：验收 #19 断成 PASS / FAIL 时可能永远红。／原因：判定走 `TierSortingRegistry`，金镐在排序表里的位置本仓读不出（facts-B §5.2）。／正确做法：只记录工具与掉落数，不参与 PASS / FAIL，已知影响里写明「金镐未实测」。／依据：RT-020、D-041、06 号 §8 第 1 项、验收 #19。
- **陷阱 11 · 照抄 1.21.1 的 `Tool` 判定**
  - 症状：写 `DataComponents.TOOL` / `Tool.isCorrectForDrops` 时编译不过（全树 0 命中）。／原因：1.20.1 走 `Tier` 体系，判定链是 `ForgeHooks.isCorrectToolForDrops` → `DiggerItem` → `TierSortingRegistry`。／正确做法：一条判定代码都不写，只加 `requiresCorrectToolForDrops()` 与两条标签。／依据：facts-B §5.2 / §5.3、D-039、D-040。
- **陷阱 12 · 照抄 `DirectionPropertyBlock` 类名**
  - 症状：`extends DirectionPropertyBlock` 编译不过（该类名全树 0 命中）。／原因：1.20.1 的水平朝向基类叫 `HorizontalDirectionalBlock`（`:8-9` 的 `FACING`）。／正确做法：`extends BaseEntityBlock` + 自建 `DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING`，照 `CropConverterBlock.java:44`、`:67-69`。／依据：facts-B §6、D-022。
- **陷阱 13 · 照抄 `useWithoutItem`**
  - 症状：覆写的方法不生效、右键无反应，编译器还不报错。／原因：1.20.1 的方块右键入口只有 `use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)`（`BlockBehaviour.java:172`）。／正确做法：覆写 `use(...)`，照 `CultivatorBlock.java:111-129` / `CropConverterBlock.java:114-132`。／依据：facts-C §4.1、§13、D-028。
- **陷阱 14 · 照抄 `BlockDropsEvent` 做第 ④ 条**
  - 症状：写到一半发现类不存在；改用 `BlockEvent.BreakEvent` 后与别家 mod 互相干扰。／原因：1.20.1 的 `BlockEvent` 12 个嵌套类里没有 `HarvestDropsEvent` / `BlockDropsEvent`，全树 0 命中。／正确做法：走只挂干草块表的全局战利品修饰器 + 空表替换。／依据：facts-B §1、06 号 §5.2 墓碑、D-047。
- **陷阱 15 · 四条史诗写成南瓜**
  - 症状：把南瓜当成本版新放行的第 4 条，实测红。／原因：被那道比较挡住的恰好是 4 条转换配方（`ModRecipeProvider.java:66` / `:67-68` / `:74` / `:78`，输入为火把花（`minecraft:torchflower`）、瓶子草（`minecraft:pitcher_plant`）、甜浆果（`minecraft:sweet_berries`）、金苹果（`minecraft:golden_apple`）），南瓜不在其中。／正确做法：四条按 facts-C §5 的配方行实测（验收 #15），南瓜只作基线记录。／依据：D-042、facts-C §5。
- **陷阱 16 · 0 槽菜单却写了 `slots.get`**
  - 症状：`IndexOutOfBoundsException`：`slots` 是空列表。／原因：面板槽位 0 个，`clicked` 的 `slotIndex` 没有对应槽。／正确做法：`quickMoveStack` 直接 `return ItemStack.EMPTY`，方法体内不出现 `slots.get`。／依据：D-034、facts-B §3.4、`OutpostCoreMenu.java:104-125` 的对照。
- **陷阱 17 · item 模型落到 `basicItem`**
  - 症状：物品栏与手上是一格紫黑。／原因：`usesBlockModel` 白名单只有三类方块（`ModItemModelProvider.java:68-70`），漏登记就去找不存在的 `textures/item/control_terminal.png`。／正确做法：白名单加 `|| block instanceof ControlTerminalBlock`；本版 `textures/item` 一个新文件都不加。／依据：`ModItemModelProvider.java:45-46`、`:55-57`、D-020、验收 #28。
- **陷阱 18 · blockstate 落进 `simpleBlock` 兜底**
  - 症状：blockstate 只有 `""` 单态，或指向不存在的同名贴图，三个朝向缺模型。／原因：`ModBlockStateProvider` 是 else-if 链（`:70-94`），没有终端分支就落到兜底。／正确做法：插一支 `else if (block instanceof ControlTerminalBlock)` + `forAllStates` 的 4 variant 方法。／依据：D-022、facts-C §10.3、验收 #4。
- **陷阱 19 · 模式枚举序与 `Kind` 序混用**
  - 症状：培养模式的线画成绿色，或终端线画成青色。／原因：模式三态与 `Kind` 三值两套编号不同轴。／正确做法：模式只存序列化名（`off` / `cultivator` / `converter`），`Kind` 只出现在核侧列表与 payload，两者之间写显式映射。／依据：D-030、D-035、facts-B §8。
- **陷阱 20 · 两格 `ContainerData` 写成缓存字段**
  - 症状：面板状态不刷新，退出重进才变；或两格恒为 0。／原因：`get` 不现读方块实体、漏了 `addDataSlots`，或改模式后忘了 `setChanged()`。／正确做法：`get` 每次现读（取不到返回 0 / 0），两格 `set` 空实现，改模式后 `setChanged()` + `sendBlockUpdated`，同步交给 `broadcastChanges`。／依据：D-034、facts-B §3.2、facts-C §9（本仓 0 先例）。
- **陷阱 21 · 创造模式挖掉不消费记录**
  - 症状：记录集合里的坐标在创造模式被挖掉后仍在。／原因：创造模式不结算战利品表，修饰器不被调用。／正确做法：不当作缺陷修，把「创造模式不消费」写进签字已知影响。／依据：D-048 的补充、facts-B §2.5。
- **陷阱 22 · 木镐断言写成「挖不动」**
  - 症状：验收 #13 变成「木镐破坏不了方块」，实测方块仍被移除。／原因：`needs_stone_tool` 管的是「算不算正确工具」，`removeBlock` 与 `playerDestroy` 是两个独立调用。／正确做法：判据写「掉落 0 个（方块仍被移除）」，石镐照旧掉落 1 个。／依据：D-040、facts-B §5.2（`ServerPlayerGameMode.java:249` / `:253` / `:255`）、验收 #13 / #14。
- **陷阱 23 · 先写终端缓存、后写核侧**
  - 症状：面板显示已绑定，投料一个都不投。／原因：`addCultivator` 在已绑或满员时返回 false（`OutpostCoreBlockEntity.java:414`）。／正确做法：`if (core.addCultivator(pos, Link.Kind.TERMINAL)) { terminal.setCore(core.getBlockPos()); }`，顺序不可换。／依据：D-025、`CultivatorBlock.java:95-97`、验收 #25。
- **陷阱 24 · 用 `removeItem(slot, 64)` 搬整摞**
  - 症状：核里一格被清空，机器槽塞满。／原因：`removeItem` 的第二实参就是取走数量，`getItem` 拿到的是整摞只读视图。／正确做法：`ItemStack one = core.removeItem(slot, 1)`，再进机器槽。／依据：D-032、D-033、facts-C §1（`acceptAll` 的先算后写）。
- **陷阱 25 · 用 `Direction#toYRot()` 算朝向**
  - 症状：终端正面背对玩家。／原因：`toYRot()` 与 `facingRotationY` 差 180°，`ModBlockStateProvider.java:274-275` 的注释明写不能用它。／正确做法：复用 `facingRotationY`（`:277-284`）。／依据：facts-C §10.3、facts-B §6。
- **陷阱 26 · blockstate 漏 `uvLock(true)`**
  - 症状：朝向旋转后侧面贴图上下错位。／原因：`ConfiguredModel.builder()` 默认不锁 uv。／正确做法：四个 variant 都写 `uvLock(true)`，照 `ModBlockStateProvider.java:201-205`。／依据：facts-C §10.3、D-022。
- **陷阱 27 · 注册 id 带上 `_a`**
  - 症状：`/give farmstorm:control_terminal_a` 取不到；模型引用的贴图名与仓内文件名对不上。／原因：素材包文件名带 `_a`（A 版设计号），注册 id 不带。／正确做法：注册 id 与模型 id 都是 `control_terminal`，只有贴图文件名保留 `_a_`（如 `control_terminal_a_front.png`）。／依据：D-020、D-023。
- **陷阱 28 · 战利品表漏登记 `getKnownBlocks`**
  - 症状：datagen 的 validate 报缺表，`runData` 失败。／原因：`getKnownBlocks` 是 `Iterable<Block>`，新方块不登记就缺表。／正确做法：`generate()` 加 1 张单表（照 `ModLootTableProvider.java:131`），`getKnownBlocks()`（`:200-231`）加 1 条，28 → 29。／依据：facts-C §10.4、D-018、验收 #22。
- **陷阱 29 · 终端配方与培养器同料同图样**
  - 症状：合成台只稳定命中其中一条，另一条时成时不成。／原因：同料同图样的两条 `crafting_shaped` 共存时命中顺序不保证。／正确做法：`GIG` / `IRI` / `GIG`（四角玻璃、四边铁锭、正中红石），与培养器 `IGI` / `GRG` / `IGI`、转换器 `III` / `GRG` / `III` 逐格不同。／依据：D-029、facts-C §10.2（`:100-102`、`:122-124`）、验收 #28。
- **陷阱 30 · 语言键表头留旧数**
  - 症状：两份 lang 各 328，类注释仍写 321 与 28 + 28 + 32 + 178 + 13 + 42。／原因：`ModLangProvider.java:10-16` 是注释口径段，检查器不校验它。／正确做法：5 个新键与表头同一任务改到 328（29 + 29 + 13 + 32 + 178 + 47）。／依据：D-018、facts-C §10.5、验收 #3。
- **陷阱 31 · `Farmstorm.java` 的断言漏改**
  - 症状：检查器全绿，启动日志整行末段 `FAIL`。／原因：运行期断言把 11 类写死成 `==`，检查器不读它。／正确做法：与四处注册同批改到 96 / 29 / 29 / 9 / 2（末位是 `menuTypes`），其余 7 个 `==` 不动。／依据：D-052、facts-A §1.3、`Farmstorm.java:192-195`。
- **陷阱 32 · 用 `Get-Content` 数行号**
  - 症状：报出的行号整体前移，指到的行不是要引的那一行。／原因：`Get-Content` 与 `read` 的行拆分不一致（facts-C §14 第 5 项：`Measure-Object -Line` 计数偏小）。／正确做法：行号一律用 `read` 实读，`offset` + `limit` 分段。／依据：facts-C §14 第 5 项、06 号 §0.3 第 5 条。
- **陷阱 33 · 忘记 `setDirty()`，或用 `level` 取数据存储**
  - 症状：本次会话内掉落被清空，重进世界后记录消失、干草块又掉落；或登记写一处、消费读不到。／原因：`SavedData` 只有 `setDirty()` 之后才写盘，而数据挂在主世界的数据存储上、`computeIfAbsent` 要的是 `MinecraftServer`。／正确做法：`add` / `remove` 后各调一次 `setDirty()`；两端都从 `ServerLevel#getServer` 取服务器再 `getDataStorage()`（`ServerLevel.java:1148`）。／依据：D-045、facts-B §4、`OutpostCoreIndex.java:76-79`、验收 #32。
- **陷阱 34 · 在客户端算绑定或准入**
  - 症状：两个按钮永远置灰，状态行永远「未绑定」。／原因：`isUsable` / `canInsert` 依赖 `ServerLevel`，客户端取不到核 ⇒ 恒假。／正确做法：状态只走两格 `ContainerData`，客户端一次都不调方块实体的判定方法。／依据：D-034、facts-C §2（`canInsert` 三闸）、验收 #26。

---

## 5. 交付清单（勾选项，逐项带路径或读数）

### 5.1 24 个任务（F-022 ~ F-045，7 个 Phase / M7 ~ M14）

| 勾选 | 任务 | 交付物（路径 / 读数） | 08 号验收 # |
|---|---|---|---|
| [ ] | F-022 | `gradle.properties:11` 的 `mod_version` → `0.3.0`；`remap-1.20.1.cmd:11` 的用法示例同步（现值 `farmstorm-0.2.4+mc1.20.1.jar`）；`0.2.8` 在 `gradle.properties` 命中 0 | 收口项（不占 #） |
| [ ] | F-023 | `textures/block` 加 4 PNG（三张 32 × 768 + 一张 32 × 32）+ 3 mcmeta = 7 个手放文件；`.png` 88 / `.mcmeta` 8 / 合计 96 | #5、#29 |
| [ ] | F-024 | `FarmstormBlocks` / `FarmstormItems` / `FarmstormBlockEntities` 各 +1；`blocks` 29、`items` 29、`block_entity_type` 9；两条 `block.` 与两条 `item.` 语言键 | #1（前置）、#3 |
| [ ] | F-025 | 4 个 facing variant + `models/block` 79 + item 模型白名单 +1 + `blockstates` 29；`working` 命中 0 | #4、#22、#27、#28 |
| [ ] | F-026 | `block_entity_type` 9 的实体类 + 三态枚举 + NBT 存序列化名；`/data get block <终端> Mode` 读到 `off` | #7 |
| [ ] | F-027 | `TERMINAL(2)`、既有两色不动、`PROTOCOL_VERSION = "2"`；启动日志两行带「版本 2」 | #12、#20、#24 |
| [ ] | F-028 | 标签 3 成员 + `KIND_BY_BLOCK` 3 条，同一提交 | #25 |
| [ ] | F-029 | 放置绑定三步顺序（先核后缓存）+ 两个 `PASS` 出口；核侧两条列表末位 `terminal` | #10、#25、#26 |
| [ ] | F-030 | `menu_type` 2 的 0 槽菜单（`quickMoveStack` 返回空，两格 `ContainerData`） | #6、#26 |
| [ ] | F-031 | 面板屏幕 + `MenuScreens.register` 命中 1（在 `FMLClientSetupEvent` 内）；未绑定置灰 | #6、#10、#26、#31 |
| [ ] | F-032 | 面板 5 个语言键（container +1 / gui +4）+ 表头 321 → 328；两份 lang 各 328 | #3、#6 |
| [ ] | F-033 | 每 20 刻一轮、每台 1 个、四类边界（只填空槽 / 名额满 / 超 3×3 / 槽非空） | #8、#9、#11 |
| [ ] | F-034 | `mineable/pickaxe` 12 成员 + `needs_stone_tool` 3 成员 | #13、#14、#19 |
| [ ] | F-035 | 删掉 `CropTier.EPIC` 那道比较，四条全收；其余 17 条不受影响 | #15 |
| [ ] | F-036 | `util/PuffedHayData`（`LongOpenHashSet` + `BlockPos.asLong` + `setDirty()`） | #32 |
| [ ] | F-037 | 登记点在 `setBlock` 成功后 + `hay.is(Blocks.HAY_BLOCK)` | #16、#17 |
| [ ] | F-038 | `loot/PuffedHayLootModifier` + `registry/FarmstormLootModifiers`（注册面第 12 类） | #16、#17、#18 |
| [ ] | F-039 | `datagen/ModGlobalLootModifierProvider` + 两份数据文件（清单命名空间 `forge`） | #30 |
| [ ] | F-040 | 终端 3 × 3 配方；`recipe` 26（21 条 converting + 5 条合成） | #22、#23、#28 |
| [ ] | F-041 | `loot_tables/blocks` 29（+1 张终端表）；`getKnownBlocks` 29 条 | #22、#28 |
| [ ] | F-042 | 检查器三处（`EXPECTED_COUNTS` / ID 清单 / 算式注释）同批 96 与 12 类 | #1、#2 |
| [ ] | F-043 | 启动自检 `Farmstorm.java:192-195` 收到 96（12 类），与 F-042 同批 | 不占 #（读数进 §5.4） |
| [ ] | F-044 | `README.md` 版本段与文档索引 + 签字清单 | 不占 #（§5.4 成文） |
| [ ] | F-045 | 数据面复核 + datagen 重跑：`models/block` 79 / `loot_tables` 29 / `recipe` 26 / `blockstates` 29 / Java 159 | #18、#21、#23、#29 |

### 5.2 32 条验收与任务的对照

| 验收 # | 判据（读数 / 命令） | 任务 |
|---|---|---|
| 1 | 检查器打印 `已核 registry 对象: 96 / 96`，12 类小计逐项相等 | F-042 |
| 2 | 同一条命令的收尾 `FAIL 0`（WARN 允许 ≤ 1 条，即 §1 因 `run/config` 未生成而走 warn 分支那条；基线的 `data_component_type` FAIL 在 F-042 一并清掉） | F-042 |
| 3 | 两份 lang 顶层键各 328，键集逐字相同 | F-032 |
| 4 | `blockstates/control_terminal.json` 的 `variants` 恰好 4 个键（north 不写 `y` / 90 / 180 / 270） | F-025 |
| 5 | 三面各 24 帧；PNG 高 768 = 24 × 32；一轮 48 刻 = 2.4 秒 | F-023 |
| 6 | 空手右键终端：面板打开、标题「控制终端」、槽位 0 | F-030、F-031、F-032 |
| 7 | `/data get block <终端> Mode`：`off` → `cultivator` → `off` → `converter` | F-026 |
| 8 | 相邻两次投料的 `gameTime` 差恒为 20 | F-033 |
| 9 | 一轮核库存 64 → 63、机器槽 1；槽非空后 200 刻核库存 0 变化 | F-033 |
| 10 | 名额满（默认 24）后第 25 台：不绑定、按钮置灰、库存 0 变化 | F-029、F-031 |
| 11 | 终端被推到 3×3 区块之外：不再投料 | F-033 |
| 12 | 绿线像素落在 `0x4D / 0xD9 / 0x4D` 附近；同屏青线、红线不变 | F-027 |
| 13 | 木镐破坏培养器：掉落 0 个（方块仍被移除） | F-034 |
| 14 | 石镐破坏同一块：掉落 1 个培养器 | F-034 |
| 15 | 火把花 / 瓶子草 / 甜浆果 / 金苹果四件各成功投入 1 次 | F-035 |
| 16 | 膨胀麦落点用石镐挖：掉落 0 个，记录条数 -1 | F-037、F-038 |
| 17 | 压缩干草块落点用石镐挖：照旧掉落 1 个 | F-037、F-038 |
| 18 | 老存档里已有的干草块：照旧掉落 1 个 | F-038、F-045 |
| 19 | 金镐：只记录工具与掉落数，不参与 PASS / FAIL | F-034 |
| 20 | 启动日志两行带「版本 2」 | F-027 |
| 21 | `(Get-ChildItem src/main/java -Recurse -Filter *.java -File).Count` = 159 | F-045 |
| 22 | `models/block` 79 / `loot_tables` 29 / `recipe` 26 / `blockstates` 29 | F-025、F-040、F-041 |
| 23 | `advancement` 16 个 JSON；`oneWay` 调用 21 条 | F-040、F-045 |
| 24 | `CULTIVATOR(0)` / `CROP_CONVERTER(1)` 不动；`byId(2)` 返回 `TERMINAL` | F-027 |
| 25 | 核的两条列表同长同下标，末位是终端坐标与 `terminal`；名额 +1 | F-028、F-029 |
| 26 | 未绑定右键：开得起来、按钮点不动、库存不变；绑定后状态行变「已绑定」 | F-030、F-031 |
| 27 | `working` 在 blockstate 命中 0、`WORKING` 在 `ControlTerminalBlock.java` 命中 0 | F-025 |
| 28 | 终端产物恰好 5 个文件：blockstate / 方块模型 / 物品模型 / 战利品表 / 配方 | F-025、F-040、F-041 |
| 29 | `.png` 88、`.mcmeta` 8、两者相加 96（三个数不许互借） | F-023、F-045 |
| 30 | GLM 清单 `entries` 里有 `farmstorm:puffed_hay` | F-039 |
| 31 | `MenuScreens.register` 命中 1，且该行在 `FMLClientSetupEvent` 方法体内 | F-031 |
| 32 | `farmstorm_puffed_hay.dat` 落盘，且跨重启条数不变 | F-036 |

### 5.3 复核命令（每个任务收尾跑一遍，读数进证据段）

```
build-1.20.1.cmd
python -X utf8 tools/verify_project.py
(Get-ChildItem src/main/java -Recurse -Filter *.java -File).Count
(Get-ChildItem src/main/resources/assets/farmstorm/textures/block -Filter *.png -File).Count
(Get-ChildItem src/main/resources/assets/farmstorm/textures/block -Filter *.mcmeta -File).Count
(Get-ChildItem src/main/resources/assets/farmstorm/textures/block -File).Count
```

读法：第一行退出码 0 才进下一个任务（HC-028）；第二行读 96 / 96 与 FAIL 0；第三行读 159；后三行读 88 / 8 / 96，三个数各用一条命令，不许互借（陷阱 6、验收 #29）。版本串只落 `gradle.properties:11` 与 `remap-1.20.1.cmd:11` 两处，`build-1.20.1.cmd` 是纯透传包装器、不含 jar 名（`0.2.8` 命中 0）。

### 5.4 收口三件（F-044 / F-045 的签字必须成文，缺一件视为未交付）

1. **已知影响 5 条**：右键始终开面板、未绑定置灰（D-028）；培养器从「挖不出」变「石镐能挖出」、转换器从「木镐可挖」变「石镐起」（D-039、D-040）；四条史诗作物可培养量产并自我复制（D-043）；老存档里已存在的膨胀麦干草块照旧掉落（D-046）；创造模式挖掉干草块不消费记录（D-048 的补充）。
2. **遗留 8 条各有归属**（06 号 §8 原样转写归属）：金镐 → 08 号验收实测；7 个素材逐字节落地 → 09 号任务 / 11 号出口判据；24 帧动画 → 08 号验收加一条；老存档干草块 → 07 号已知影响；检查器 config 期望现算 → 08 号落笔前跑一次现状读数；培养器与转换器仍无菜单 → 保持；战利品表类注释 27 → 08 号 §2.5；一批旧数字注释 → 08 号 §2.5。
3. **签字读数 21 项**：注册面 96（12 类）/ server config 175 / client config 3 / 语言键 328 / Java 文件 159 / 网络包 2 / 协议串 `"2"` / Mixin 0 / 方块战利品表 29 / 转换配方 21 / 合成配方 5 / 配方 JSON 26 / 进度 16 / 方块模型 79 / blockstate 29 / item 模型 29 / 贴图 `.png` 88 / `.mcmeta` 8 / 贴图文件总数 96 / 事件监听 9 / loot modifier 数据文件 2。

---

> 本文自检：事实卡 22 行（第 1 ~ 22 行各带 `文件:行号` 或 facts § 或一条命令读数）、HC-011 ~ HC-028 共 18 条连续无跳号、N-009 ~ N-022 共 14 条连续无跳号、陷阱 34 条（前 10 条逐条对应 RT-011 ~ RT-020，每条四段齐）、交付清单 4 小节（5.1 24 个任务 / 5.2 32 条验收 / 5.3 6 行复核命令 / 5.4 三件）、表格 3 张（事实卡 / 5.1 / 5.2）首尾有竖线且逐行列数一致、围栏 1 对、禁用词命中 0。
