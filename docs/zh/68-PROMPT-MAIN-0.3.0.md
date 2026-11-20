# 68 号 v0.3.0 主提示词（实现方开工前只读一次）

> 定位：实现方动第一行代码之前读一次本文，之后每个 Phase 只投 69 号的对应那一节。本文不复述 64 / 66 的论证，只给五样东西——现状读数、不许动的、不许做的、会炸的地方、交付与回报口径。
> 上游：64 号（`docs/zh/64-DECISIONS-0.3.0.md`，D-255 ~ D-289，唯一真相源）/ 65 号（B-74 ~ B-87）/ 66 号（RT-92 ~ RT-99、§8 的 29 条验收）/ 67 号（F-329 ~ F-349、Phase 73 ~ 83、M69 ~ M79、A-112 ~ A-118）/ `.work/v030-BRIEF.md`（241 行，下称 BRIEF，§2 的 15 条作者裁定）。
> 编号：本文只发两个系列——硬约束 **HC-189 ~ HC-204**（16 条）、禁止行为 **N-167 ~ N-178**（12 条）。其余系列一律写全前缀（D-255 起 / B-74 起 / F-329 起 / Phase 73 起 / M69 起 / RT-92 起 / A-112 起 / E-73-1 起），不认领下游编号。
> 底线：基线是 **v0.2.7 已实现态**（git `7211b06`，`mod_version = 0.2.7`）；本版只做 plan 的 4 条需求 + 裁定 #11 / #12 两条口径 + 使它们成立所必需的连带改动。判据只认五类读数——计数 / 日志行 / `/data get` 读数 / grep 命中 / 像素比对。

---

## 1. 事实卡（20 行，每行一个可观察读数）

| # | 事实（读数 / 现状） | 出处 |
|---|---|---|
| 1 | 注册面 **92** = 28 + 28 + 8 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 1；十二类每类写死成一个 `==` 常量（`total == 92`、`items == 28` …） | `Farmstorm.java:178-183` |
| 2 | 检查器把 12 类期望值写死在 `EXPECTED_COUNTS`，总数由 `sum(...)` 现算（92），上面还有一段算式注释 | `tools/verify_project.py:453-457`、`:458-471`、`:609-610` |
| 3 | 检查器**不数 Java 文件**：只把 `*.java` 拼成一个字符串做内容匹配 | `tools/verify_project.py:479-480` |
| 4 | 农作物培养器（cultivator）**不在** `mineable/pickaxe`（该标签 10 个成员里没有它），却已带 `requiresCorrectToolForDrops()` ⇒ 破坏时掉落 0 个 | `ModBlockTagsProvider.java:49-63`、`FarmstormBlocks.java:107` |
| 5 | `farmstorm:processing_block` 现有成员 **2**（cultivator + crop_converter） | `ModBlockTagsProvider.java:44-47`、`FarmstormTags.java:29-30` |
| 6 | `KIND_BY_BLOCK` 现有 **2** 条；取不到值就 `continue`（漏登记 = 只被发现、不绑定） | `OutpostCoreBlockEntity.java:117-125` |
| 7 | 连线现有两色：青 `0.30 / 0.85 / 0.90`、红 `0.92 / 0.22 / 0.22`，`ALPHA` 1.0，`switch (kind())` 无 `default` | `CultivatorLinkRenderer.java:48-50`、`:56-60`、`:94-103` |
| 8 | `Kind` 按 **BYTE** 上线（1 字节），未知码回落 `CULTIVATOR` | `CultivatorLinkPayload.java:46-50`、`:69-71`、`:84-92` |
| 9 | 协议串现为 `PROTOCOL_VERSION = "3"` | `FarmstormNetwork.java:29` |
| 10 | 项目内 `Button.builder` **0** 命中（`Button` 只有两处 `MouseButton`）⇒ 面板按钮是本版第一处 | `grep -c "Button.builder" src/main/java` = 0 |
| 11 | item 模型白名单现有**三类**（转换器 / 核心 / 培养器）；漏登记落 `basicItem`，去找不存在的 `textures/item/<id>.png` | `ModItemModelProvider.java:61-70`、`:55-56` |
| 12 | `ModBlockStateProvider` 是 else-if 链 + `simpleBlock` 兜底，兜底按同名贴图找图 | `ModBlockStateProvider.java:72-92`（兜底 `:88-91`） |
| 13 | `facingRotationY`（EAST 90 / SOUTH 180 / WEST 270 / 其余 0）与 `cubeSixFaces` 现成可复用 | `ModBlockStateProvider.java:277`、`:212` |
| 14 | mcmeta **不进 datagen**：`src/generated` 下 mcmeta 命中 0；现有 5 个 mcmeta 全在 `src/main/resources`（实点 84 PNG + 5 mcmeta = 89 文件） | `grep -r mcmeta src/generated` = 0；`Get-ChildItem src/main/resources/assets/farmstorm/textures/block -Filter *.mcmeta` = 5 |
| 15 | 语言键 **321 × 2**，按组 28 + 28 + 32 + 178 + 13 + 42 = 321 | `ModLangProvider.java:10`、`:13-16` |
| 16 | 0 槽菜单先例：`stillValid` 走 `stillValid(access, player, block)`；构造器两个重载 | `OutpostCoreMenu.java:132-135`、`:52-59` |
| 17 | 屏幕注册点只有 **1** 处 `register` | `FarmstormClient.java:63-66` |
| 18 | 事件类现有 **9** 个 `addListener`，类注释写「只做九件事」 | `FarmstormGameEvents.java:68-78`、`:30` |
| 19 | 膨胀麦（puffed_wheat）落点：`fillHay` 两条跳过分支在前、`setBlock` 在后；压缩膨胀麦（compressed_puffed_wheat）只覆写 `fillBlock()` | `PuffedWheatProjectile.java:104-123`（跳过 `:113-118`、写入 `:119`）、`CompressedPuffedWheatProjectile.java:34-37` |
| 20 | 培养器准入闸只有一处（含 `CropTier.EPIC` 比较）；核侧 54 格与名额 24 是两条不同列表 | `CultivatorBlockEntity.java:143-149`、`OutpostCoreBlockEntity.java:67`、`:139`、`:147`、`:154`、`:401-403` |

读法：第 1 行与第 2 行是**两套闸**——运行时断言读 `DeferredRegister#getEntries().size()`，检查器读源码里的注册 id 清单；改数时两处都动，只动一处就出现「日志绿而脚本红」（陷阱 26）。第 3 行说明 Java 文件数 151 → 156 只活在散文里，检查器不会因此变红。第 4 ~ 5 行是既有缺口（培养器挖不出掉落物、标签 2 成员）。第 6 ~ 9 行是绑定与连线的四处单点。第 10 ~ 14 行是本版第一次遇到的四类形状（按钮 / 白名单 / blockstate 兜底与朝向助手 / 手放 mcmeta）。第 15 ~ 18 行是四处「加了东西忘同步」的口径面。第 19 ~ 20 行的两条链各有两条进入路径，登记与准入都只能落在一处。

---

## 2. 硬约束 HC-189 ~ HC-204（16 条，逐条可判定）

- **HC-189** · 注册面四个类同一提交各加 1 项（`FarmstormBlocks` / `FarmstormItems` / `FarmstormBlockEntities` / `FarmstormMenus`），并把运行时断言改到 `total == 96`、`items == 29`、`blocks == 29`、`blockEntities == 9`、`menuTypes == 2`（其余 8 个 `==` 一字不动）；判据：构建退出码 0、启动日志整行末段 `OK`、`== 92` 在 `Farmstorm.java` 命中 0；依据：D-258、`Farmstorm.java:178-183`、本文 §6.2 第 1 条。

> **〔v0.3.0 实施期更正 · Phase 74 · 依据实测与作者裁定〕**
> ~~原文：「注册面四个类同一提交各加 1 项」~~
> 现口径（C5）：四处注册分在 F-331（blocks / items）、F-333（block_entity_type）、F-337（menu_type）三次提交，且有编译依赖顺序（方块实体类 / 菜单类必须先存在），**不可能同一提交**。本条改按**最终态判据**执行：`total == 96`、`items == 29`、`blocks == 29`、`blockEntities == 9`、`menuTypes == 2`，其余 8 个 `==` 一字不动，启动日志末段 `OK`；两处断言在 F-348 一次改完（见 C4 / Q2=A）。
- **HC-190** · `KIND_BY_BLOCK` 与 `processing_block` 同一提交各加同一条（标签 2 → 3、映射 2 → 3）；判据：两个文件各命中 `control_terminal` 1 处、`processing_block.json` 读到 3 项；依据：D-263、64 号 §4 红线 1。
- **HC-191** · 既有两值的线上码不重排：`CULTIVATOR(0)` / `CROP_CONVERTER(1)` 逐字保留，`TERMINAL` 只能追加为 `(2)`；判据：枚举三行逐字比对、`byId(2)` 返回 `TERMINAL`；依据：D-274、红线 2。
- **HC-192** · 两处穷尽 `switch` 必须补分支：`switch (link.kind())`（`CultivatorLinkRenderer.java:94-103`）加 `TERMINAL`，模式到 kind 的换算不得借用 `Mode.ordinal()`；判据：`TERMINAL` 在渲染类命中 ≥ 1、编译退出码 0；依据：D-277、D-269、66 号 §4.1 的编号陷阱。
- **HC-193** · 准入判据只有一个来源：培养模式只调 `CultivatorBlockEntity#admissible`，转换模式只调转换器的 `canInsert`；判据：终端类里 `Items.` 与 `CropTier` 命中 0；依据：D-272、D-282、红线 3。
- **HC-194** · `man瓜` 五处一字不动（`ModLangProvider.java:80` / `:114` / `:123` / `:199` / `:213`）；判据：这五行 `git diff` 为 0、`Select-String -Pattern man瓜` 仍命中 5 行；依据：D-287、裁定 #11。
- **HC-195** · config 一个键不增：server 175 / client 3 保持；判据：生成的 toml 键数 175、`FarmstormClientConfig.java` 三行 diff 0；依据：D-271、红线 5。
- **HC-196** · 干草块登记必须先过 `hay.is(Blocks.HAY_BLOCK)`、且只在 `setBlock` 成功之后；判据：登记语句位于 `PuffedWheatProjectile.java:113-118` 两条 `continue` 之后、压缩膨胀麦落点坐标不进集合；依据：D-285、红线 6、RT-93。
- **HC-197** · 投料只填空槽、每台每轮最多 1 个、取走恰好 1 个；判据：`removeItem(` 的第二实参字面量为 1、投成功后 `break` 命中 1；依据：D-271、66 号 §4.4。
- **HC-198** · 20 刻是常量：`FEED_INTERVAL_TICKS = 20` 写成 `private static final`，不读任何 config；判据：`control_terminal` 在 `config/FarmstormConfig.java` 命中 0、常量定义命中 1；依据：D-271、红线 5。
- **HC-199** · 名额 24 与 54 格不同义：终端只走 `addCultivator` 与核侧两条平行列表，不新建列表；判据：新类里 `List<BlockPos>` 命中 0、`SLOT_COUNT` 只读不改；依据：D-265、`OutpostCoreBlockEntity.java:141-154`、`:401-403`。
- **HC-200** · 协议版本 `"4"`，payload 字段 0 改；判据：`PROTOCOL_VERSION = "4"` 命中 1、`LINK_STREAM_CODEC` 的两 `BlockPos` + 1 `Byte` 三元逐字未变；依据：D-274、D-276。
- **HC-201** · 语言键 328 与表头同批：7 个键名逐字取 66 号 §7.2，`ModLangProvider.java:10-16` 的 `321` 与按组算式同一次改到 328（29 + 29 + 32 + 178 + 13 + 47 = 328）；判据：两份 lang 各 328、表头算式可复算；依据：D-258、66 号 §7.5。
- **HC-202** · `EXPECTED_COUNTS` 的四类值 + `:408-451` 的 ID 清单 + `:453-457` 的算式注释三处同批改到 96；判据：检查器打印 `96 / 96`、`EXPECTED_COUNTS` 合计 96；依据：D-289、67 号 §0.3 的 W1。
- **HC-203** · 7 个贴图手放、逐字节复制、不改名不重采样（4 PNG = 三张 32 × 768 + 一张 32 × 32，3 mcmeta 各 24 帧）；判据：`.png` 88、`.mcmeta` 8、两者相加 96，PNG 高度 768 = 24 × 32；依据：D-262、66 号 §3.1 / §3.2。
- **HC-204** · 每个任务收尾构建退出码 0（入场券，不是收尾项）；判据：`gradlew build` 的 `$LASTEXITCODE` = 0，失败即停、不带病进下一个任务；依据：67 号 §5.2、§0.4 第 5 条。

---

## 3. 禁止行为 N-167 ~ N-178（12 条，每条三段）

- **N-167** · 新增第三个 S2C 网络包来同步模式
  - **不要做什么**：不要把模式与绑定状态做成新 payload。
  - **为什么**：两格状态用 `ContainerData` 由 `broadcastChanges` 自动同步（`AbstractContainerMenu#addDataSlots`），多一个包就要多一次协议变更与一条注册记录。
  - **会看到什么后果**：网络包由 2 变 3、注册面多 1 项（96 变 97），冻结组 2 与 96 同时破，且 `FarmstormNetwork` 的两行自检日志要改成三行。
- **N-168** · 在终端侧另写一份物品准入白名单
  - **不要做什么**：不要按 `CropProfile` 挑作物，也不要在终端抄一份「史诗例外」。
  - **为什么**：判据只有一个来源：转换器不收的作物搬出来会卡在机器里，数据包新增配方时两处必然不同步。
  - **会看到什么后果**：数据包加一条转换配方后培养器多收一个、终端不收；验收 #15 的四条史诗输入至少一条不进槽，且面板里没有任何报错。
- **N-169** · 用 `setCanceled(true)` 代替 `getDrops().clear()`
  - **不要做什么**：不要把整个破坏事件取消掉。
  - **为什么**：`setCanceled` 会连经验与 `spawnAfterBreak` 一起掐掉，而干草块本来没有经验，清掉落是更小的动作。
  - **会看到什么后果**：干草块挖掉后经验与后续钩子一起消失；验收 #16 的「掉落 0 个」能过，但事件语义被扩大，下一版任何依赖该事件的逻辑一起失效。
- **N-170** · 把干草块换成自家方块，或加全局战利品修饰器
  - **不要做什么**：不要动膨胀麦的落点外观，也不要用 `LootModifier` 改所有干草块。
  - **为什么**：裁定 #6 只限膨胀麦写进的原版干草块（`minecraft:hay_block`）；全局修饰器会连整合包里别人的干草块一起改。
  - **会看到什么后果**：所有干草块都不掉落（含原版自然生成的），验收 #17 / #18 的「压缩版照旧掉落」「老存档照旧掉落」双红，回滚面跨到数据包。
- **N-171** · 给终端加 `working` 属性或第二套模型
  - **不要做什么**：不要为了动画加状态属性，也不要按工作态切图。
  - **为什么**：三面动画自带 mcmeta 自己播，与方块状态无关；素材包 README 明说这套动画是循环装饰示意。
  - **会看到什么后果**：blockstate 变 8 个 variant、方块模型由 1 张变 2 张，模型 79 与 blockstate 29 两个冻结数同时破（验收 #4 的「恰好 4 个键」当场红）。
- **N-172** · 手动编辑 `src/generated` 下的产物 JSON
  - **不要做什么**：不要手写 `blockstates` / `models` / `loot_table` / `recipe` 的产物。
  - **为什么**：这四个目录是 datagen 的产物，手改会在下一次 `runData` 被覆盖，且 `getKnownBlocks` 与白名单的漏登记被手写掩盖。
  - **会看到什么后果**：下次跑 datagen 后改动消失、验收 #28 的「恰好 5 个文件」时有时无；最坏情况是手写的 model 引用了尚未生成的贴图名。
- **N-173** · 用 `simpleBlock(block)` 生成终端 blockstate
  - **不要做什么**：不要把 4 朝向的方块塞进单态兜底。
  - **为什么**：`simpleBlock` 只生成 `""` 单态，四个朝向里三个找不到模型；终端要 `forAllStates` + `rotationY(facingRotationY(...))` + `uvLock(true)`。
  - **会看到什么后果**：进游戏后三个朝向的终端是紫黑格或缺模型方块；验收 #4 的 `variants` 键数读到 1 而不是 4。
- **N-174** · 把 `Mode` 的枚举序当 `Kind` 的线上码
  - **不要做什么**：不要用 `mode().ordinal()` 去填 `Link.Kind`，也不要为了对齐而重排任一枚举。
  - **为什么**：`Mode` 是 `OFF=0 / CULTIVATOR=1 / CONVERTER=2`，`Kind` 是 `CULTIVATOR=0 / CROP_CONVERTER=1 / TERMINAL=2`，两套编号不同轴。
  - **会看到什么后果**：培养器的线画成绿色、终端线画成青色；验收 #12 的「同屏三色各就各位」红，而编译与运行都不报错。
- **N-175** · 先写终端引用缓存、后写核侧
  - **不要做什么**：不要把 `setCore` 放在 `addCultivator` 之前，也不要在返回 `false` 时照写。
  - **为什么**：核侧列表是唯一真相，`addCultivator` 在已绑或满员时返回 `false`；顺序反了会造出「终端说绑了、核说没绑」。
  - **会看到什么后果**：面板状态行显示「已绑定」但一个都不投料；验收 #10（名额满）与 #25（两条列表末位）同时红。
- **N-176** · 在终端另存一份 24 名额列表，或按 54 判满
  - **不要做什么**：不要新建 `List<BlockPos>`，不要用 54 当容量。
  - **为什么**：名额上限读 `outpost_core.cultivatorCapacity`（默认 24），54 是核的格子数；两份列表要复制 `kindOf` / `hasCultivator` / `pruneCultivators` 三条不变量。
  - **会看到什么后果**：名额口径变两处：拆核后终端仍占着旧名单、或第 25 台被放进名单；验收 #10 与 #25 读数与核侧不一致。
- **N-177** · 改 `FarmstormTags.java`，或把类型塞进标签
  - **不要做什么**：不要新增标签常量，也不要试图用标签表达「是哪一类机器」。
  - **为什么**：`PROCESSING_BLOCK` 已存在于 `FarmstormTags.java:30`，成员由 datagen 决定；类型只由 `KIND_BY_BLOCK` 决定。
  - **会看到什么后果**：标签常量与注册面无关地多出一个 `TagKey`，`FarmstormTags` 变成类型真相的第二份；两处类型取值不一致时编译器不报错。
- **N-178** · 为「金镐能挖」去改原版 `incorrect_for_*` 标签，或把金镐写成断言
  - **不要做什么**：不要改 `data/minecraft/tags/block/incorrect_for_*.json`，也不要在验收里断言金镐掉不掉。
  - **为什么**：本条只保证木镐挖不动、石镐能挖出掉落物；金镐的行为由原版 `incorrect_for_gold_tool` 决定，本仓读不出线上真值。
  - **会看到什么后果**：验收 #19 变成必红条目（与真值相反时永远不通过），或本仓的 5 个局部覆盖被改成非原版语义、影响整合包里所有方块。

---

## 4. 陷阱清单（31 条，每条四段：症状 / 原因 / 正确做法 / 依据）

- **陷阱 1 · RT-92 · 照抄项目内 16 帧 mcmeta**
  - **症状**：三面只播前 16 帧，第 17 ~ 24 帧永不出现；或帧条被压成 32 × 512（512 ÷ 32 = 16）。
  - **原因**：项目现有 5 个 mcmeta 全是 `{"animation":{"frametime":2}}` 的 16 帧 / 32 × 512 口径，照抄就少 8 帧。
  - **正确做法**：mcmeta 用素材包那份（显式 `frames` 0 ~ 23、`frametime` 2）；PNG 逐字节复制、不改名不重采样；进游戏对着一台终端数一轮 24 帧。
  - **依据**：RT-92、D-262、66 号 §3.1 / §3.2。
- **陷阱 2 · RT-93 · 压缩膨胀麦共用 `fillHay` 被误登记**
  - **症状**：压缩干草块被挖时也掉落 0 个。
  - **原因**：`fillHay` 是父类私有方法，压缩版只覆写 `fillBlock()`；登记点若只判「`setBlock` 成功」，压缩干草块坐标也进集合。
  - **正确做法**：登记前加 `hay.is(Blocks.HAY_BLOCK)`，再判成功。
  - **依据**：RT-93、D-285、`PuffedWheatProjectile.java:106`、`:119`、`CompressedPuffedWheatProjectile.java:34-37`。
- **陷阱 3 · RT-94 · 记录集合只增不减**
  - **症状**：`farmstorm_puffed_hay.dat` 里留下永不消费的坐标，文件随游戏时长单调变长。
  - **原因**：只有 `BlockDropsEvent` 一条消费路径；被活塞推走、被爆炸炸掉、被 `/setblock` 覆盖都不走它。
  - **正确做法**：消费点唯一且判据带 `getState().is(Blocks.HAY_BLOCK)`；记录量级与膨胀麦落点同阶，不额外做清理。
  - **依据**：RT-94、D-286、66 号 §4.7。
- **陷阱 4 · RT-95 · 投料一轮的配方查询成本**
  - **症状**：单轮最多 24 × 54 = 1296 次配方查询，20 刻一次。
  - **原因**：`canInsert` 内含配方查询（`convertible` 走 `recipeManager.getRecipeFor`）。
  - **正确做法**：先判槽空再查配方（`canInsert` 的短路顺序已保证），命中即 `break`。
  - **依据**：RT-95、66 号 §4.4。
- **陷阱 5 · RT-96 · 贴图三个数互借**
  - **症状**：`.png` / `.mcmeta` / 文件总数三个读数串位，写成旧口径 93 的断言稳定变红。
  - **原因**：`Get-ChildItem -File` 会把 mcmeta 一起数进去（读到 96），拿它当 PNG 数就错成 96。
  - **正确做法**：三个数各用一条命令：`.png` 88（84 + 4）、`.mcmeta` 8（5 + 3）、相加 96（89 + 7）。
  - **依据**：RT-96、A-116、66 号 §1.2。
- **陷阱 6 · RT-97 · 史诗四条写成南瓜**
  - **症状**：把南瓜（pumpkin）当成本版新放行的第 4 条，实测红。
  - **原因**：man瓜 的档位是 `CropTier.COMMON`，南瓜在基线里就能进培养器。
  - **正确做法**：四条按火把花 / 瓶子草 / 甜浆果 / 金苹果实测（验收 #15）；南瓜只作基线记录，不进断言。
  - **依据**：RT-97、D-281、裁定 #7、BRIEF §1 需求 2.2 的表下注。
- **陷阱 7 · RT-98 · 把金镐写成断言**
  - **症状**：验收 #19 断言「金镐挖不动」时可能与原版真值相反，变成必红条目。
  - **原因**：`needs_stone_tool` 之上还有原版 `incorrect_for_gold_tool`，本仓的 5 个局部覆盖读不出线上真值。
  - **正确做法**：只记录金镐的读数与工具本身，不参与 PASS / FAIL。
  - **依据**：RT-98、D-280、A-117、66 号 §8 第 19 条。
- **陷阱 8 · RT-99 · 半边改动与穷尽 switch 的静默失败**
  - **症状**：四种表现都不报错：终端永远绑不上 / 右键只有一条 warn 没有界面 / 物品栏紫黑格 / blockstate 指向不存在的贴图。
  - **原因**：标签与 `KIND_BY_BLOCK` 只改一处；屏幕漏注册；`ModItemModelProvider` 白名单漏登记；`ModBlockStateProvider` 漏 `else if` 落进兜底。
  - **正确做法**：四处同一提交改完；枚举 `TERMINAL` 与渲染 `switch` 同提交（`:94-103` 无 `default`，漏了编译不过）。
  - **依据**：RT-99、D-263 红线 1、`ModBlockStateProvider.java:88-91`、`FarmstormClient.java:65`。
- **陷阱 9 · 0 槽菜单却写了 `quickMoveStack` 的 `slots.get`**
  - **症状**：`IndexOutOfBoundsException`：`slots` 是空列表。
  - **原因**：面板槽位 0 个，`clicked` 的 `slotIndex` 没有对应槽可查。
  - **正确做法**：`quickMoveStack` 直接 `return ItemStack.EMPTY`，方法体内不出现 `slots.get`。
  - **依据**：D-273、66 号 §4.6、`OutpostCoreMenu.java:132-135`。
- **陷阱 10 · 屏幕漏注册**
  - **症状**：服务端能开菜单，客户端只有一条 warn，界面不出现。
  - **原因**：`RegisterMenuScreensEvent` 全仓只有 1 处 register。
  - **正确做法**：`event.register(FarmstormMenus.CONTROL_TERMINAL.get(), ControlTerminalScreen::new)` 与菜单类型同一提交。
  - **依据**：D-273、66 号 §4.2 第 6 步、`FarmstormClient.java:63-66`。
- **陷阱 11 · item 模型落到 `basicItem`**
  - **症状**：物品栏与手上是一格紫黑。
  - **原因**：`usesBlockModel` 白名单只有三类，漏登记就去找不存在的 `textures/item/control_terminal.png`。
  - **正确做法**：白名单加 `|| block instanceof ControlTerminalBlock`。
  - **依据**：66 号 §3.5、`ModItemModelProvider.java:55-56`、`:68-70`。
- **陷阱 12 · blockstate 落进 `simpleBlock` 兜底**
  - **症状**：blockstate 只有 `""` 单态，或指向不存在的同名贴图，三个朝向缺模型。
  - **原因**：else-if 链（`:72-92`）没有终端分支，落到 `:88-91` 的兜底。
  - **正确做法**：插一支 `else if (block instanceof ControlTerminalBlock)` + `forAllStates` 的 4 variant 方法。
  - **依据**：D-261、66 号 §3.4、`ModBlockStateProvider.java:72-92`。
- **陷阱 13 · 模式枚举序与 `Kind` 序混用**
  - **症状**：培养模式的线画成绿色，或终端线画成青色。
  - **原因**：`Mode` 是 `OFF=0 / CULTIVATOR=1 / CONVERTER=2`，`Kind` 是 `CULTIVATOR=0 / CROP_CONVERTER=1 / TERMINAL=2`，两套编号不同轴。
  - **正确做法**：模式只存序列化名（`off` / `cultivator` / `converter`），`Kind` 只出现在核侧列表与 payload，两者之间写显式映射。
  - **依据**：D-269、D-274、66 号 §4.1 的编号陷阱。
- **陷阱 14 · `ContainerData` 两格写成缓存字段**
  - **症状**：面板状态不刷新，退出重进才变；或两格恒为 0。
  - **原因**：`get` 不现读方块实体、漏了 `addDataSlots`，或改模式后忘了 `setChanged()`。
  - **正确做法**：`get` 每次 `access.evaluate` 现读（取不到返回 0 / 0），两格 `set` 空实现，改模式后 `setChanged()` + `sendBlockUpdated`，同步交给 `broadcastChanges`。
  - **依据**：D-273、66 号 §4.6。

> **〔v0.3.0 实施期更正 · F-337 · 依据作者裁定 Q1=A 与实机 API 语义〕**
> ~~原文（本陷阱的「正确做法」）：`get` 每次 `access.evaluate` 现读（取不到返回 0 / 0），两格 `set` 空实现~~
> 现口径（C3）：症状与原因仍然成立，但正确做法改成 **普通 `SimpleContainerData(2)` + 服务端 `broadcastChanges()` 写值 + `super.broadcastChanges()`**，不覆写 `get`/`set`。原因：客户端菜单的 `ContainerLevelAccess` 是 `NULL`（`get` 现读恒为 0），而 `set` 空实现会把服务端推来的值丢掉。详见 66 号 §4.6 的更正块（C1）与 `menu/ControlTerminalMenu.java`。
- **陷阱 15 · 创造模式挖干草块不消费记录**
  - **症状**：记录集合里的坐标在创造模式下被挖掉后仍在。
  - **原因**：`BlockDropsEvent` 只在服务端触发，创造模式本来就不掉落。
  - **正确做法**：不当作缺陷修；把「创造模式不消费」写进签字已知影响。
  - **依据**：D-286 的补充、66 号 §4.7。
- **陷阱 16 · 木镐断言写成「挖不动」**
  - **症状**：验收 #13 变成「木镐破坏不了方块」，实测方块仍被移除。
  - **原因**：`needs_stone_tool` 管的是「算不算正确工具」，不是「能不能破坏」。
  - **正确做法**：判据写「掉落 0 个（方块仍被移除）」，石镐照旧掉落 1 个。
  - **依据**：D-278、D-279、66 号 §8 第 13 / 14 条。
- **陷阱 17 · 开窗期检查器红了当成 bug**
  - **症状**：W1 窗口内检查器 §2 的 92 / 96 计数断言红、4 项新 ID 不存在。
  - **原因**：检查器一行不许动到 F-348 关窗（W1 跨 18 个任务：F-331 ~ F-348）。
  - **正确做法**：按「期望红」记录、不修；关窗任务一次改完三类（四类值 + ID 清单 + 算式注释）。
  - **依据**：67 号 §0.3、A-113、D-289。
- **陷阱 18 · 先写终端引用缓存、后写核侧**
  - **症状**：面板显示已绑定，投料一个都不投。
  - **原因**：`addCultivator` 在已绑或满员时返回 `false`，缓存却已写上。
  - **正确做法**：`if (core.addCultivator(pos, Link.Kind.TERMINAL)) { terminal.setCore(core.getBlockPos()); }`，顺序不可换。
  - **依据**：D-264、`CultivatorBlock.java:95-102`、66 号 §4.3。
- **陷阱 19 · 用 `getItem(slot)` 或 `removeItem(slot, 64)` 搬整摞**
  - **症状**：核里一格被清空，机器槽塞满或直接崩。
  - **原因**：`removeItem` 的第二实参就是取走数量；`getItem` 拿到的是整摞只读视图。
  - **正确做法**：`ItemStack one = core.removeItem(slot, 1)`，再 `machine.insert(one)`。
  - **依据**：D-271、66 号 §4.4 的「投 1 个」五步。
- **陷阱 20 · 用 `Direction#toYRot()` 算朝向**
  - **症状**：终端正面背对玩家。
  - **原因**：`toYRot()` 按 data2d 算，与 `facingRotationY` 差 180°。
  - **正确做法**：复用 `ModBlockStateProvider.facingRotationY`（`:277`）。
  - **依据**：66 号 §3.4 第 3 条。
- **陷阱 21 · blockstate 漏 `uvLock(true)`**
  - **症状**：朝向旋转后侧面贴图上下错位。
  - **原因**：`ConfiguredModel.builder()` 默认不锁 uv。
  - **正确做法**：四个 variant 都写 `uvLock(true)`。
  - **依据**：66 号 §3.4 的 variant 表。
- **陷阱 22 · 注册 id 带上 `_a`**
  - **症状**：`/give farmstorm:control_terminal_a` 取不到；模型引用的贴图名与仓内文件名对不上。
  - **原因**：素材包文件名带 `_a`（A 版设计号），注册 id 不带。
  - **正确做法**：注册 id 与模型 id 都是 `control_terminal`，只有贴图文件名保留 `_a_`（如 `control_terminal_a_front.png`）。
  - **依据**：D-259、66 号 §3.1 / §3.3。
- **陷阱 23 · 战利品表漏登记 `getKnownBlocks`**
  - **症状**：datagen 的 `validate` 报缺表，`runData` 失败。
  - **原因**：`getKnownBlocks` 是 `Iterable<Block>`，新方块不登记就缺表。
  - **正确做法**：`generate()` 加 1 张单表（照 `:142`），`getKnownBlocks()`（`:204`）加 1 条。
  - **依据**：66 号 §2.4、`ModLootTableProvider.java:139-142`、`:204`。
- **陷阱 24 · 终端配方与培养器同料同图样**
  - **症状**：合成台只稳定命中其中一条，另一条时成时不成。
  - **原因**：同料同图样的两条 `crafting_shaped` 共存时命中顺序不保证。
  - **正确做法**：`GIG` / `IRI` / `GIG`（四角玻璃、四边铁锭、正中红石），与培养器 `IGI` / `GRG` / `IGI` 逐格不同。
  - **依据**：D-268、A-115、`ModRecipeProvider.java:119-128`。
- **陷阱 25 · 语言键表头留旧数**
  - **症状**：两份 lang 各 328，类注释仍写 321 与 28 + 28 + 32 + 178 + 13 + 42。
  - **原因**：`ModLangProvider.java:10-16` 是注释口径段，检查器不校验它。
  - **正确做法**：7 键与表头同一提交改到 328（29 + 29 + 32 + 178 + 13 + 47）。
  - **依据**：D-258、66 号 §7.5、67 号 W2。
- **陷阱 26 · `Farmstorm.java` 的断言漏改**
  - **症状**：检查器全绿，启动日志整行末段 `FAIL`。
  - **原因**：运行时断言把 12 类写死成 `==`，检查器不读它。
  - **正确做法**：与四处注册同批改到 96 / 29 / 29 / 9 / 2，其余 8 个 `==` 不动。
  - **依据**：`Farmstorm.java:178-183`、本文 §6.2 第 1 条。
- **陷阱 27 · 用 `pwsh` 的 `Get-Content` 数行号**
  - **症状**：报出的行号整体前移，指到的行不是要引的那一行。
  - **原因**：`Get-Content` 与 `read` 的行拆分不一致（0.2.4 轮实测前移 4 行）。
  - **正确做法**：行号一律 `read` 实读，`offset` + `limit` 分段（整读超 480 行会静默截断）。
  - **依据**：62 号陷阱 25、交接文档 §8.3 / §8.13。
- **陷阱 28 · `SavedData` 忘记 `setDirty()`**
  - **症状**：本次会话内掉落被清空，重进世界后记录消失、干草块又掉落。
  - **原因**：`SavedData` 只有在 `setDirty()` 之后才写盘。
  - **正确做法**：`add` 与 `remove` 之后各调一次 `setDirty()`。
  - **依据**：D-284、66 号 §4.7 的记录表。
- **陷阱 29 · 用 `level` 而不是 `server.overworld()` 取 `PuffedHayData`**
  - **症状**：登记写在一处、消费读不到；或 `level.getServer()` 为 null 时 NPE。
  - **原因**：数据挂在主世界的数据存储上，`computeIfAbsent` 只接受 `MinecraftServer`。
  - **正确做法**：写入点与消费点都从 `ServerLevel#getServer` 取 `MinecraftServer`，再 `getDataStorage()`。
  - **依据**：D-284、66 号 §4.7 的「取实例」行。
- **陷阱 30 · 只用坐标判「是不是该不掉落」**
  - **症状**：同一坐标换成别的方块再拆，掉落也被清空。
  - **原因**：记录键只有 `BlockPos.asLong`，不带方块类型。
  - **正确做法**：判据两条同时成立：`event.getState().is(Blocks.HAY_BLOCK)` 且坐标在集合里。
  - **依据**：D-286 的补充、66 号 §4.7 的消费三步。
- **陷阱 31 · 在客户端算绑定或准入**
  - **症状**：两个按钮永远置灰，状态行永远「未绑定」。
  - **原因**：`isUsable` / `canInsert` 依赖 `ServerLevel`，客户端取不到核 ⇒ 恒假。
  - **正确做法**：状态只走 `ContainerData` 两格；客户端一次都不调方块实体的判定方法。
  - **依据**：D-273、62 号陷阱 5 的同类形状、66 号 §4.6。

---

## 5. 交付清单（勾选项，逐项带路径或读数）

### 5.1 21 个任务（F-329 ~ F-349）

| 勾选 | 任务 | 交付物（路径 / 读数） | 66 号验收 # |
|---|---|---|---|
| [ ] | F-329 | `gradle.properties:11` + `一键构建.cmd`（3 处）+ `检查环境.cmd`（2 处）= 6 处 / 3 个文件；`0.2.7` 命中 0、`0.3.0` 命中 6 | 收口项（不占 #） |
| [ ] | F-330 | `textures/block` 加 4 PNG（三张 32 × 768 + 一张 32 × 32）+ 3 mcmeta；`.png` 88、`.mcmeta` 8、合计 96 | #5、#29 |
| [ ] | F-331 | `FarmstormBlocks` / `FarmstormItems`（含创造栏）/ 2 条语言键；`items` 29、`blocks` 29 | #1、#3 |
| [ ] | F-332 | 4 个 variant + `models/block` 79 + item 模型白名单 +1 | #4、#22、#27、#28 |
| [ ] | F-333 | `block_entity_type` 9；`/data get block <终端> Mode` 读到 `off` | #7、#21 |
| [ ] | F-334 | `TERMINAL(2)`、既有两色不动、`PROTOCOL_VERSION = "4"`；启动日志两行带「版本 4」 | #12、#20、#24 |
| [ ] | F-335 | 标签 3 成员 + `KIND_BY_BLOCK` 3 条，同一提交 | #25 |
| [ ] | F-336 | 放置绑定三步顺序 + 核侧两条列表末位 `terminal` | #10、#11、#25 |
| [ ] | F-337 | `menu_type` 2、0 槽菜单（`quickMoveStack` 返回空） | #6、#28 |
| [ ] | F-338 | 两个按钮 + 一行状态；未绑定置灰 | #6、#26 |
| [ ] | F-339 | 5 个面板语言键 + 表头 321 → 328；两份 lang 各 328 | #3 |
| [ ] | F-340 | 每 20 刻一轮、每台 1 个、四条边界 | #8、#9、#11 |
| [ ] | F-341 | `mineable/pickaxe` 12 成员 + `needs_stone_tool` 3 成员 | #13、#14、#19 |
| [ ] | F-342 | 删掉 `CropTier.EPIC` 比较，四条全收 | #15 |
| [ ] | F-343 | `util/PuffedHayData`（`LongOpenHashSet` + `long[]`） | #16 |
| [ ] | F-344 | 登记点在 `setBlock` 成功后 + `hay.is(Blocks.HAY_BLOCK)` | #16、#17 |
| [ ] | F-345 | 第 10 个监听 + 类注释「九件事」→「十件事」 | #16、#17、#18 |
| [ ] | F-346 | 终端 3 × 3 配方；`recipe` 26（21 + 5） | #22 |
| [ ] | F-347 | `loot_table/blocks` 29 | #22 |
| [ ] | F-348 | `EXPECTED_COUNTS` 四类值 + ID 清单 + 算式注释三处同批 96 | #1、#2 |
| [ ] | F-349 | `README.md` 版本段与文档索引 + 签字清单 | #21、#23、#28 |

### 5.2 29 条验收与任务的对照

| 验收 # | 判据（读数 / 命令） | 任务 |
|---|---|---|
| 1 | `python -X utf8 tools/verify_project.py` 打印 `已核 registry 对象: 96 / 96`，12 类小计逐项相等 | F-348 |
| 2 | 同一条命令的收尾：`FAIL 0`（WARN ≤ 1，即基线那一条） | F-348 |
| 3 | 两份 lang 顶层键各 328，键集逐字相同 | F-339 |
| 4 | `blockstates/control_terminal.json` 的 `variants` 恰好 4 个键：north 不写 `y` / 90 / 180 / 270 | F-332 |
| 5 | 三面各 24 帧；PNG 高度 768 = 24 × 32；一轮 48 刻 | F-330 |
| 6 | 空手右键已绑定终端：面板打开、标题「控制终端」、槽位 0 | F-337、F-338 |
| 7 | `/data get block <终端> Mode`：`off` → `cultivator` → `off` → `converter` | F-337、F-340 |
| 8 | 相邻两次投料的 `gameTime` 差恒为 20 | F-340 |
| 9 | 一轮只少 1 个；机器槽非空 ⇒ 核库存不再减少 | F-340 |
| 10 | 名额满（默认 24）后第 25 台：不绑定、按钮置灰、库存一格不减 | F-336 |
| 11 | 终端被推到 3 × 3 区块之外：不再投料 | F-340 |
| 12 | 绿线像素落在 `0x4D / 0xD9 / 0x4D` 附近；同屏青线、红线不变 | F-334 |
| 13 | 木镐破坏培养器：掉落 0 个（方块仍被移除） | F-341 |
| 14 | 石镐破坏培养器：掉落 1 个培养器 | F-341 |
| 15 | 火把花 / 瓶子草 / 甜浆果 / 金苹果四个全部被收进槽 | F-342 |
| 16 | 膨胀麦落点用石镐挖：掉落 0 个，记录被移除 | F-344、F-345 |
| 17 | 压缩膨胀麦落点用石镐挖：照旧掉落 1 个压缩干草块 | F-344、F-345 |
| 18 | 老存档里已有的干草块：照旧掉落 1 个 | F-345 |
| 19 | 金镐：只记录工具与掉落数，不参与 PASS / FAIL | F-341 |
| 20 | 启动日志两行「自检 网络包注册 …（版本 4，S2C 只读…）」 | F-334 |
| 21 | `(Get-ChildItem src/main/java -Recurse -Filter *.java).Count` = 156 | F-349 |
| 22 | `models/block` 79 / `loot_table/blocks` 29 / `recipe` 26 / `blockstates` 29 | F-332、F-346、F-347 |
| 23 | `advancement` 16 个 JSON；`oneWay` 调用 21 条 | F-349 |
| 24 | `CULTIVATOR(0)` / `CROP_CONVERTER(1)` 不动，`byId(2)` 返回 `TERMINAL` | F-334 |
| 25 | 核的两条列表同长同下标，末位是终端坐标与 `terminal`；名额 +1 | F-335、F-336 |
| 26 | 未绑定右键：开得起来、按钮点不动、库存不变；绑定后状态行变「已绑定」 | F-338 |
| 27 | `working` 在 blockstate 命中 0、`WORKING` 在 `ControlTerminalBlock.java` 命中 0 | F-332 |
| 28 | 终端产物恰好 5 个文件：blockstate / 方块模型 / 物品模型 / 战利品表 / 配方 | F-332、F-346、F-347 |
| 29 | `.png` 88、`.mcmeta` 8、两者相加 96（三个数不许互借） | F-330、F-348 |

### 5.3 复核命令（每个任务收尾跑一遍，读数进证据段）

```
python -X utf8 tools/verify_project.py
(Get-ChildItem src/main/java -Recurse -Filter *.java).Count
```

读法：第一行读注册面 96 / 96 与 FAIL 0；第二行读 156。两者都不数贴图与 mcmeta——贴图的三个数见验收 #29 的两条 `-Filter` 命令。

### 5.4 收口三件（F-349 的签字必须成文，缺一件视为未交付）

1. **已知影响 4 条**：右键始终开面板、未绑定置灰（D-267）；培养器从「永远挖不出」变成「石镐能挖出」、转换器从「木镐可挖」变成「石镐起」（D-278、D-279）；四条史诗作物可培养量产并自我复制（D-282）；老存档里已存在的膨胀麦干草块照旧掉落（D-285）。
2. **未实测 1 项**：金镐在 `needs_stone_tool` 上的行为（D-280、A-117）。
3. **冻结读数 15 项**：注册面 96 / server config 175 / client config 3 / 语言键 328 / Java 156 / 网络包 2 / 协议版本 `"4"` / Mixin 0 / 战利品表 29 / 转换配方 21 / 合成配方 5 / 进度 16 / 模型 79 / 贴图 PNG 88 / mcmeta 8。

---

## 6. 冲突处理

### 6.1 裁决顺序与三条处置

裁决顺序固定：**决策 > 技术 > 任务**，即 64 号（D-255 ~ D-289）> 66 号 > 67 号。65 号的设计增量只在其与 64 号一致时作为叙述取材，不参与裁决。执行时只用下面三条，不自创第四条。

| # | 冲突形状 | 处置 | 报给作者的内容 |
|---|---|---|---|
| 1 | 64 号与 66 号不一致 | 按 64 号落地 | 列 66 号的原句与它引用的行号，问作者改哪一侧 |
| 2 | 66 号与 67 号不一致 | 按 66 号落地 | 列任务边界与 §4 规格的差，任务号照旧不改 |
| 3 | 源码与任何上游不一致 | 停下，不落地 | 并列 `文件:行号` 与上游编号，由作者裁 |

登记格式固定一行：冲突主题 / 上游 A 的说法（编号）/ 上游 B 的说法（编号）/ 源码读数（`文件:行号`）/ 需要作者裁定的问题。三条禁令：不自行取舍、不把冲突当笔误、不改任何上游文件（只引上游）。

> **〔v0.3.0 实施期更正 · Phase 74 · 依据作者裁定 Q5=C〕**
> ~~原文：三条禁令里的「不改任何上游文件（只引上游）」~~
> 现口径（C7）：作者裁定本轮**允许**改 `docs/zh/64~69`，但每处必须带可见更正标记块（原件保留 + 删除线 + 现口径）。前两条禁令（不自行取舍、不把冲突当笔误）仍然有效。全部更正清单见 `.work/v030-CORRECTIONS.md`。

### 6.2 上游登记（本文实测发现，报给作者，本文不改上游）

| # | 项 | 上游写法 | 本文实测与处置 |
|---|---|---|---|
| 1 | `Farmstorm.java` 的运行时 12 类断言 | 64 / 66 / 67 与 BRIEF §5 的必改清单都没有这一项（66 号 §2.4 只点名 `tools/verify_project.py`） | 实测 `Farmstorm.java:178-183` 把 `total == 92`、`items == 28`、`blocks == 28`、`blockEntities == 8`、`menuTypes == 1` 写死；不改则启动日志末段 `FAIL` 而检查器全绿。本文 HC-189 补上，建议作者在 67 号点名为 F-331 或 F-348 的一部分 |
| 2 | 67 号 §0.3 的 W2 关窗任务 | 表格 `:35` 写关窗 = F-339；正文 `:40` 写「只在 F-338 改一次」 | 本文按表格的 F-339（与 §1 任务总表、§3.4 的语言键分组一致）；两处差一个任务号，由作者裁 |
| 3 | 66 号 §9 的 RT-96 后果列 | 该行写「验收照旧冻结值 93 写会稳定变红」 | 93 是 §9.1 第 1 行已作废的旧口径；本文判据一律按 88 / 8 / 96（84 + 4、5 + 3、89 + 7），建议该句改成「照旧口径 93」 |

---

## 7. 回报口径

### 7.1 固定四段（顺序不变，缺一段视为未交付）

- **结论**：一句，给任务号 + 验收编号 + 达标与否；不写过程。例：`F-335 交付完成，验收 #25 达标`。
- **证据**：命令原文 + 关键输出 + 编号。四类证据各自要读数——grep 命中数、`/data get` 读数、日志行原文、像素分量；每条附 `文件:行号` 或验收 #。
- **未决问题**：只列需要作者裁定的项，每条给冲突形状与两个候选；没有就写「无」，不写推测。
- **下一步**：下一个任务号 + 它依赖的前置是否已落地（依赖见 67 号 §1 的「依赖」列）。

### 7.2 三条禁令

1. 不把静态读链写成实测：没进游戏跑出的读数一律标「静态推链」。
2. 不写「应该」「大概」「可能」一类推测词；判据只认五类读数。
3. 不只给结论不给读数：每条断言后面必须跟一个可复算的数或一条命令原文。

### 7.3 本文自检（四项读数的落点）

| # | 要求 | 读数 |
|---|---|---|
| 1 | 只发 HC 与 N 两个系列 | HC-189 ~ HC-204 共 16 条、N-167 ~ N-178 共 12 条，两系列连续无跳号 |
| 2 | 事实卡 15 ~ 20 行且每行带出处 | 20 行，逐行带 `文件:行号` 或一条命令 |
| 3 | 陷阱 ≥ 28 条且每条四段 | 31 条，症状 / 原因 / 正确做法 / 依据齐全，其中第 1 ~ 8 条逐条对应 RT-92 ~ RT-99 |
| 4 | 交付清单逐项可判 | 21 个任务（F-329 ~ F-349）+ 29 条验收（#1 ~ #29）全覆盖，每行带路径或读数 |
| 5 | 表格纪律 | 6 张表（事实卡 / 任务 / 验收对照 / 冲突处置 / 上游登记 / 本文自检），首尾有竖线、逐行列数一致（脚本逐行比对） |
| 6 | 围栏成对 | 1 对（§5.3 的复核命令块，2 行围栏标记），开闭成对 |
| 7 | 禁用词 | 交接文档 §6 第 3 条与 §0.3 第 3 条点的八个词，逐个 `Select-String` 命中 0（本句不再复写这八个词，避免自命中） |
| 8 | 末行非空 | 文件末行为本表最后一行 |
