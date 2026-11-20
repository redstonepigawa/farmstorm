# 农场风云 v0.3.0 技术规格

> **定位**：实现方照着就能写代码——凡是「下游必须自己猜」的地方都算没写完。
> **事实来源**：`.work/v030-BRIEF.md`（241 行事实卡）与 `docs/zh/64-DECISIONS-0.3.0.md`（D-255 ~ D-289，唯一真相源）。**只引不改**：上游已定稿的更正记录登记在 §9.1，不改上游任何文件。
> **基线**：v0.2.7 已实现态（git `7211b06`，`mod_version=0.2.7`）。
> **数字**：全部取 64 号 D-258 冻结的那一组，逐处给算式；本文不自造新数，需要新数的位置写「由本节的加法算式给出」。
> **行号**：本文引用的每一处行号都按 UTF-8 实读核过，含 BRIEF §7 第 2、3 条点名「上游没复核」的三处（`ModBlockStateProvider` 的转换器段、`facingRotationY`、`Tool.isCorrectForDrops` 链路）。
> **术语**：首次出现写「中文名（英文 id）」，之后只用中文名（裁定 #12 / D-288）。本文涉及的控制终端（`farmstorm:control_terminal`）、农作物培养器（`farmstorm:cultivator`）、农作物转换器（`farmstorm:crop_converter`）、据点核心（`farmstorm:outpost_core`）、膨胀麦（`farmstorm:puffed_wheat`）、压缩干草块（`farmstorm:compressed_hay_block`）六个名字按此写法首次并出，其后一律用中文名。
> **判据纪律**：所有验收判据只能是计数 / 日志行 / 指令读数 / grep 命中 / 像素比对（交接 §6 第 9 条）。

---

## §1 口径总表

### 1.1 注册面 12 类逐项（92 → 96）

注册面的逐类基线读 `tools/verify_project.py:459-470` 的 `EXPECTED_COUNTS`；本版只动四类。

| 类 | 基线 | 本版 | 增减 | 算式与落点 |
|---|---|---|---|---|
| items | 28 | **29** | +1 | 28 + 1 = 29，终端方块物品 |
| blocks | 28 | **29** | +1 | 28 + 1 = 29，终端方块 |
| block_entity_type | 8 | **9** | +1 | 8 + 1 = 9，终端方块实体 |
| menu_type | 1 | **2** | +1 | 1 + 1 = 2，控制面板 |
| entity_type | 13 | 13 | 0 | 13 + 0 = 13 |
| mob_effect | 4 | 4 | 0 | 4 + 0 = 4 |
| sound_event | 3 | 3 | 0 | 3 + 0 = 3 |
| recipe_type | 1 | 1 | 0 | 1 + 0 = 1（`farmstorm:converting`） |
| recipe_serializer | 1 | 1 | 0 | 1 + 0 = 1 |
| creative_mode_tab | 1 | 1 | 0 | 1 + 0 = 1 |
| damage_type | 3 | 3 | 0 | 3 + 0 = 3 |
| data_component_type | 1 | 1 | 0 | 1 + 0 = 1 |
| **合计** | **92** | **96** | **+4** | 92 + 1 + 1 + 1 + 1 = 96 |

两列各自相加必须相等，这是检查器 §2 的判据：

- 基线：28 + 28 + 8 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 1 = **92**。
- 目标：29 + 29 + 9 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 2 = **96**。

### 1.2 其余 13 项（D-258 冻结组）

| 项 | 基线 | 本版 | 算式 | 说明 |
|---|---|---|---|---|
| server config | 175 | **175** | 175 + 0 = 175 | 一个键都不增；节拍写成常量（D-271） |
| client config | 3 | **3** | 3 + 0 = 3 | 不改 |
| 语言键 | 321 | **328** | 321 + 7 = 328；29 + 29 + 32 + 178 + 13 + 47 = 328 | 增 1 item + 1 block + 1 container + 2 模式 + 2 状态 |
| Java 文件 | 151 | **156** | 151 + 5 = 156 | §2.1 的五个新文件 |
| 网络包 | 2 | **2** | 2 + 0 = 2 | 复用既有 payload，只扩一枚举值（§5） |
| 协议版本 | `"3"` | `"4"` | 3 + 1 = 4 | `Kind` 多了一个线上码 |
| Mixin | 0 | **0** | 0 + 0 = 0 | 不改 |
| 方块战利品表 | 28 | **29** | 28 + 1 = 29 | 终端 1 张 |
| 转换配方 | 21 | **21** | 21 + 0 = 21 | 需求 2.2 只改准入闸，不加配方 |
| 合成配方 | 4 | **5** | 4 + 1 = 5 | 终端 3×3 |
| 进度 | 16 | **16** | 16 + 0 = 16 | 配方解锁进度被剥掉（`ModRecipeProvider.java:176-193`），故合成配方 +1 而进度不变 |
| 方块模型 | 78 | **79** | 78 + 1 = 79 | 终端 1 张 cube 模型，朝向靠 blockstate 旋转 |
| 方块贴图 PNG | 84 | **88** | 84 + 4 = 88 | front / side / top / bottom 各 1 张（读数口径见 §9 RT-96） |
| 贴图 mcmeta | 5 | **8** | 5 + 3 = 8 | front / side / top 各 1 个 |
| 贴图文件总数 | 89 | **96** | 89 + 7 = 96；88 + 8 = 96 | PNG 与 mcmeta 之和。「89」是 `textures/block` 的**文件总数**，不是 PNG 数（84 + 5 = 89） |

### 1.3 三个派生数字（同一组加法，供 §2 与 §8 引用）

| 派生项 | 基线 | 本版 | 算式 | 依据 |
|---|---|---|---|---|
| 配方 JSON 总数 | 25 | **26** | 21 + 5 = 26 | 转换配方 21 + 合成配方 5 |
| blockstate 文件数 | 28 | **29** | 28 + 1 = 29 | 与方块数同步 |
| 物品模型文件数 | 28 | **29** | 28 + 1 = 29 | 与 items 同步 |

---

## §2 注册面逐项

### 2.1 新增四项 + 五个新 Java 文件

| # | 类 | 注册 id | 落点（文件:行号） | 同形的既有先例 |
|---|---|---|---|---|
| 1 | blocks | `control_terminal` | `registry/FarmstormBlocks.java`，插在 `:120` 之后（`:121` 构造器之前） | `:102-109` 农作物培养器 |
| 2 | items | `control_terminal` | `registry/FarmstormItems.java`，插在 `:107` 之后 | `:106-107` 培养器方块物品 |
| 3 | block_entity_type | `control_terminal` | `registry/FarmstormBlockEntities.java`，插在 `:58` 之后（`:60` 构造器之前） | `:56-58` 培养器 |
| 4 | menu_type | `control_terminal` | `registry/FarmstormMenus.java`，插在 `:18` 之后（`:20` 构造器之前） | `:17-18` 据点核心存储 |

五个新 Java 文件（151 + 5 = 156），文件名与职责由本节钉死：

| # | 路径 | 职责 | 不许做的事 |
|---|---|---|---|
| 1 | `block/ControlTerminalBlock.java` | `FACING` 属性、普通方块模型渲染、放置绑定、右键开面板、服务端 ticker | 不写第二份准入判据（D-282） |
| 2 | `blockentity/ControlTerminalBlockEntity.java` | 嵌套 `Mode` 三态枚举、`CoreBound` 三方法、每 20 刻投料、`ContainerData` 两格 | 不自建绑定真相（D-264） |
| 3 | `menu/ControlTerminalMenu.java` | 0 槽菜单：`quickMoveStack` 返回 `ItemStack.EMPTY`、`clickMenuButton` 改模式、`stillValid` 照 `:132-135` | 不嵌核的 54 格（D-273） |
| 4 | `client/ControlTerminalScreen.java` | 两个 `Button.builder` + 一行状态 + 选中框 | 不新增 GUI 贴图 |
| 5 | `util/PuffedHayData.java` | `extends SavedData`：膨胀麦写入过的干草块坐标集合 | 不做成方块实体（D-284） |

### 2.2 五项逐条落点（四类注册面 + 合成配方）

**① 方块（`FarmstormBlocks.java`）**

- 注册体照 `:102-109` 的七项：`mapColor(MapColor.METAL)` + `strength(5.0F, 1200.0F)` + `requiresCorrectToolForDrops()` + `sound(SoundType.ANVIL)` + `pushReaction(PushReaction.BLOCK)`（D-260；铁砧的行为不照抄）。
- 类注释 `:21-23` 现在是「27 个方块 / 15 个作物 + 8 个部署 + 1 个转换器 + 1 个压缩干草块 + 1 个据点核心 + 1 个农作物培养器」，与实际 28 个差 1（`plain_chives_crop` 没进那行注释）。加终端时改成 **29 个方块**，并把清单补成「15 个作物 + 8 个部署 + 1 个转换器 + 1 个压缩干草块 + 1 个据点核心 + 1 个农作物培养器 + 1 个平凡作物 + 1 个控制终端」（15 + 8 + 1 + 1 + 1 + 1 + 1 + 1 = 29）。
- 同一处注释里的版本沿革另起一句「v0.3.0 按 D-259 加 1 个控制终端」，不动既有文字。

**② 方块物品（`FarmstormItems.java`）**

- 注册照 `:106-107`：`REGISTER.registerItem("control_terminal", properties -> new BlockItem(FarmstormBlocks.CONTROL_TERMINAL.get(), properties), otherProperties())`。
- 创造栏清单 `creativeTabItems()`（`:133-145`）末尾 `:143` 之后加 `items.add(CONTROL_TERMINAL);`；漏这一行 = 创造栏里看不到（`:138-142` 的两条注释记的就是这个坑）。

**③ 方块实体类型（`FarmstormBlockEntities.java`）**

- 照 `:56-58`：`REGISTER.register("control_terminal", () -> BlockEntityType.Builder.of(ControlTerminalBlockEntity::new, FarmstormBlocks.CONTROL_TERMINAL.get()).build(null))`。单方块例，与 converter 同形。

**④ 菜单类型（`FarmstormMenus.java`）**

- 照 `:17-18`：`REGISTER.register("control_terminal", () -> new MenuType<>(ControlTerminalMenu::new, FeatureFlags.VANILLA_SET))`。
- 类注释 `:11` 现在是「1 个 MenuType（TECH §3.9）：v0.2.0 的据点核心存储（D-166）」——改成「2 个 MenuType：v0.2.0 的据点核心存储（D-166）+ v0.3.0 的控制面板（D-273）」。

**⑤ 合成配方（`ModRecipeProvider.java`，D-268 定稿）**

- 在 `buildRecipes` 的 `:57` 的 `outpostCore(recipeOutput);` 之后加一行 `controlTerminal(recipeOutput);`；新方法照 `cultivator`（`:119-129`）的形态放在 `:129` 之后。
- 有序 3×3，**四角 `minecraft:glass`、四边 `minecraft:iron_ingot`、正中 `minecraft:redstone`**，产出 1 个：

| 行 | 第 1 格 | 第 2 格 | 第 3 格 |
|---|---|---|---|
| 上 | `G`（玻璃） | `I`（铁锭） | `G`（玻璃） |
| 中 | `I`（铁锭） | `R`（红石） | `I`（铁锭） |
| 下 | `G`（玻璃） | `I`（铁锭） | `G`（玻璃） |

- 即 `.pattern("GIG").pattern("IRI").pattern("GIG")`。

> **〔v0.3.0 实施期变更 · 作者 2026-11-20 追改〕**
> ~~原文：`.pattern("GIG").pattern("IRI").pattern("GIG")`~~
> 现口径（C22）：`.pattern("IGI").pattern("IRI").pattern("IQI")`，并多一个 `.define('Q', Items.QUARTZ)`（下界石英）；材料 6 铁锭 + 1 玻璃 + 1 红石 + 1 石英，产出 1 个不变。
- 材料总数与培养器相同（4 铁 + 4 玻璃 + 1 红石），但**图样必须与培养器的 `IGI` / `GRG` / `IGI` 不同**：两条 shaped 配方逐格相同只会稳定命中一条，做出培养器之后就再也做不出终端（D-268 的定稿理由）。旧稿的「四角铁、四边玻璃」正是因为与培养器逐格相同而被否决。
- 解锁与保存：`.unlockedBy("has_glass", has(Items.GLASS))` + `.save(recipeWithoutAdvancement(recipeOutput), Farmstorm.id("control_terminal"))`——配方解锁进度被剥掉（`:176-193`），因此合成配方 4 + 1 = 5 而进度仍是 16（§1.2）。

### 2.3 改名 / 删除

改名 **0** 项、删除 **0** 项。既有两个 `Link.Kind` 的线上码不许重排（§5.2），既有四个注册类里已注册的 id 一个字都不动。

### 2.4 检查器同步（`tools/verify_project.py`，D-289）

| 位置 | 改什么 | 改前 → 改后 |
|---|---|---|
| `:453-457` 的算式注释 | 末句的合计 | 92 → 96，并补一行「v0.3.0 按 D-259 / D-264 / D-273 / D-289 再加四类 +1 ⇒ 92 + 1 + 1 + 1 + 1 = 96」 |
| `:459-470` `EXPECTED_COUNTS` | 四个值 | `items` 28 → 29；`blocks` 28 → 29；`block_entity_type` 8 → 9；`menu_type` 1 → 2 |
| `:409-417` `items` ID 清单 | 加 1 项 | `:416` 的 `plain_chives_seeds` 之后加 `"control_terminal"` |
| `:418-430` `blocks` ID 清单 | 加 1 项 | `:429` 的 `plain_chives_crop` 之后加 `"control_terminal"` |
| `:431-436` `block_entity_type` ID 清单 | 加 1 项 | `:435` 的 `cultivator` 之后加 `"control_terminal"` |
| `:450` `menu_type` ID 清单 | 加 1 项 | `["outpost_core"]` → `["outpost_core", "control_terminal"]` |

检查器**不数** Java 文件数（`:479-480` 只把 `*.java` 拼成一个字符串做内容匹配），所以 151 → 156 只出现在文档与 `Get-ChildItem src\main\java -Recurse -Filter *.java` 的计数里，不会让检查器变红。注册面 96 与 ID 清单必须在同一个提交里改完（D-289 的中间态窗口）。

### 2.5 连带要同步的既有注释（漏了不报错，但会让下一轮读错数）

| 文件:行号 | 现文 | 改成 |
|---|---|---|
| `FarmstormBlocks.java:21-23` | 27 个方块 | 29 个方块（算式见 §2.2 ①） |
| `FarmstormBlockEntities.java:55` | 「v0.2.0 的第 8 个 type」 | 保留原文，另起一句「v0.3.0 的第 9 个 type（D-264）」 |
| `FarmstormMenus.java:11` | 1 个 MenuType | 2 个 MenuType（§2.2 ④） |
| `OutpostCoreBlockEntity.java:114-122` | 「`KIND_BY_BLOCK` 的单一映射」 | 表里 2 条 → 3 条，注释补「控制终端 → `Link.Kind.TERMINAL`（D-263）」 |
| `ModBlockTagsProvider.java:44` | 「成员 2 = 培养器 + 农作物转换器」 | 成员 3，加控制终端 |
| `ModItemModelProvider.java:62-66` | 「转换器（FIX-5）、据点核心（D-123 第 2 项）与农作物培养器」 | 加控制终端 |
| `ModBlockStateProvider.java:303` | 「本模组注册的 26 个方块」 | 29 个方块（与 `FarmstormBlocks.java:21` 同一组数） |
| `ModLangProvider.java:10-16` | 321 键的按组口径 | 328 键（逐字见 §7.5） |
| `CultivatorBlockEntity.java:136-142` | 「且该配方输出的档案不是史诗档」 | 删掉这半句（D-281） |
| `CropTier.java:24` | 「史诗：4 条，本版唯一的入口是农民交易（D-162 把它们挡在培养器外）」 | 「史诗：4 条（激活的火把花 / 激活的瓶子草 / 幸运浆果 / 狂野苹果），v0.3.0 起经 D-281 放行，培养器不再按档位拒绝它们」 |

---

> **〔v0.3.0 实施期更正 · F-331 · 依据实测〕**
> 上表漏了一处既有注释（C11）：`FarmstormItems.java:22-26` 的类注释「34 个物品 / 4 个方块物品」，表内未列。
> 现口径：F-331 已同步为「**35 个物品**（22 作物 + 7 独立播种物 + **5 个方块物品**，末尾加控制终端 + 1 平凡农作物）」，并另起一句「v0.3.0 按 D-259 加 1 个控制终端」；算式 22 + 7 + 5 + 1 = 35。


## §3 资源面

### 3.1 四张 PNG + 三个 mcmeta（手放，不经 datagen）

素材包在 `C:\Users\DASHpig\Downloads\control_terminal_a_32x32_pack\assets\farmstorm\textures\block\`，逐字节复制到 `src/main/resources/assets/farmstorm/textures/block/`，**不改名、不重采样**：

| # | 目标文件名 | 尺寸 | 像素 | 来源（包内同名文件） | 动画 |
|---|---|---|---|---|---|
| 1 | `control_terminal_a_front.png` | 32 × 768 | 32 × (24 × 32) | `control_terminal_a_front.png` | 24 帧 |
| 2 | `control_terminal_a_side.png` | 32 × 768 | 同上 | `control_terminal_a_side.png` | 24 帧 |
| 3 | `control_terminal_a_top.png` | 32 × 768 | 同上 | `control_terminal_a_top.png` | 24 帧 |
| 4 | `control_terminal_a_bottom.png` | 32 × 32 | 32 × 32 | `control_terminal_a_bottom.png` | 静态 |

| # | mcmeta 文件名 | 内容 | 帧数 |
|---|---|---|---|
| 1 | `control_terminal_a_front.png.mcmeta` | 包内原样：`{"animation":{"frametime":2,"interpolate":false,"frames":[0..23]}}` | 24 |
| 2 | `control_terminal_a_side.png.mcmeta` | 同上 | 24 |
| 3 | `control_terminal_a_top.png.mcmeta` | 同上 | 24 |

- 贴图文件名保留素材包的 `_a_`（A 版设计号）：注册 id 不带 `_a`（D-259），但文件名照既有 `crop_converter_a_front.png` / `cultivator_a_top.png` 的体例保留，这样包内文件与仓内文件可以逐字节比对。
- mcmeta 用包内那份（带显式 `frames` 0~23），**不**照抄项目内 5 个既有 mcmeta 的最小形 `{"animation":{"frametime":2}}`：两种写法都出 24 帧，但包内那份是可与素材包逐字节比对的口径。
- 三个动画面**自己播**，与方块状态无关，代码侧 0 行（D-261、D-262）。包内 README 明说这套动画是「循环装饰示意，不代表模组内真实库存、选择状态或操作进度」。
- 包内另有 `static_32x32/` 四张单张与 `alternate_idle/` 一张待机正面：**一律不落地**（0 张）。无 `working` 态即不需要 `_still` 副本，这与转换器的 8 variant / 两张模型不是同一种做法。

### 3.2 动画参数（读数口径）

| 参数 | 值 | 算式 / 来源 |
|---|---|---|
| 单帧尺寸 | 32 × 32 | 包内 README「每面、每帧都为实际 32×32」 |
| 帧数 | **24** | 768 ÷ 32 = 24 |
| 每帧停留 | 2 刻 | `frametime: 2` |
| 一轮循环 | 48 刻 | 24 × 2 = 48 |
| 一轮墙钟 | 2.4 秒 | 48 ÷ 20 = 2.4（20 TPS） |

项目现有 5 个 mcmeta 全是 16 帧 / 32×512（`crop_converter_a_{front,side,top}` 三个 + `cultivator_a_{side,top}` 两个），512 ÷ 32 = 16。24 帧 / 32×768 在本仓**无先例**，进游戏数帧是验收项（§8 第 5 条）。

### 3.3 方块模型 1 张（78 → 79）

| 项 | 值 |
|---|---|
| 产物 | `src/generated/resources/assets/farmstorm/models/block/control_terminal.json` |
| 模型 id | `farmstorm:block/control_terminal`（**不带 `_a`**，与 `crop_converter.json` 同体例） |
| 父模型 | `minecraft:block/cube` |
| 六面映射 | `north` = `control_terminal_a_front`；`south` / `east` / `west` = `control_terminal_a_side`；`up` = `control_terminal_a_top`；`down` = `control_terminal_a_bottom`；`particle` = `control_terminal_a_front` |

生成方式：复用 `datagen/ModBlockStateProvider.java:212-221` 的 `cubeSixFaces(modelName, front, side, top, bottom)`——它已经按上表逐面写 `texture(...)` 并把 `particle` 取 front，父模型是 `mcLoc("block/cube")`。素材包 `models/block/control_terminal_a.json` 的逐面映射与它逐字对应，因此只需要换模型名。

### 3.4 blockstate 4 variant（28 → 29）

| variant 键 | `y` 旋转 | `uvlock` | `model` |
|---|---|---|---|
| `facing=north` | 不写 `y` 键（等价 0） | `true` | `farmstorm:block/control_terminal` |
| `facing=east` | `90` | `true` | 同上 |
| `facing=south` | `180` | `true` | 同上 |
| `facing=west` | `270` | `true` | 同上 |

- 恰好 **4 个** variant：`HORIZONTAL_FACING` 只有四值，且不新增 `working` 之类的工作态属性（D-261）。
- 落点：`ModBlockStateProvider.java` 的 `registerStatesAndModels()` 是 `:72-92` 的 `else if` 链，在 `:84-85` 的 `CultivatorBlock` 分支之后插一支 `else if (block instanceof ControlTerminalBlock) { controlTerminalStates(block); }`；新方法放在 `cultivatorStates`（`:245-258`）之后。
- 方法体三件套：`models()` 建模型 → `getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model).rotationY(facingRotationY(state.getValue(FACING))).uvLock(true).build())`。
- **不能用** `simpleBlock(block, model)`：它只生成 `""` 单态，4 个朝向里 3 个会找不到模型（`:191-193` 记的就是这一条）。
- **不能用** `Direction#toYRot()`：它按 data2d 算，与 `facingRotationY` 差 180°，会让终端正面背对玩家（`:273-276`）。`facingRotationY` 实读为 `ModBlockStateProvider.java:277-284`（`EAST -> 90`、`SOUTH -> 180`、`WEST -> 270`、`default -> 0`），BRIEF §7 第 2 条报的行号与实读一致。
- 漏了这一支会落进 `:88-92` 的 `simpleBlock` 兜底，去找四张不存在的 `control_terminal.png` 这类同名贴图。

### 3.5 item 模型：一条白名单（`ModItemModelProvider.java:68-70`）

`usesBlockModel(Block)` 现在是 `block instanceof CropConverterBlock || block instanceof OutpostCoreBlock || block instanceof CultivatorBlock`。加 `|| block instanceof ControlTerminalBlock`。

- 命中时走 `:45-46` 的 `withExistingParent(path, modLoc("block/" + blockPath(block)))`，物品栏里是缩小的方块模型。
- 漏了会落到 `:55-56` 的 `basicItem(item)`，去找一张不存在的 `textures/item/control_terminal.png`，游戏里是紫黑格（`:65-66` 的注释逐字记着这条）。

### 3.6 本版不新增的资源（0 的清单）

| 项 | 数 | 理由 |
|---|---|---|
| GUI 贴图 | 0 | 面板用 `graphics.fill` 纯色底 + `renderOutline` 选框（§4.5），不进 `textures/gui` |
| item 贴图 PNG | 0 | 走 §3.5 的方块模型白名单 |
| 静态帧 / 待机正面 | 0 | 无 `working` 态即不需要 `_still`（§3.1） |
| 战利品表 JSON | 1（新增，不是 0） | 见 §2.4 的 `ModLootTableProvider` 那一侧 |

### 3.7 datagen 与产物核对单

跑一次 datagen（项目既有入口，本版不改它的配置、不加新的 Provider），然后按下表逐项读回：

| 数什么 | 目录（`src/generated/resources` 之下） | 应读到 |
|---|---|---|
| 方块模型 | `assets/farmstorm/models/block` | **79** |
| blockstate | `assets/farmstorm/blockstates` | **29** |
| 物品模型 | `assets/farmstorm/models/item` | **29** |
| 方块战利品表 | `data/farmstorm/loot_table/blocks` | **29** |
| 配方 JSON | `data/farmstorm/recipe` | **26**（21 转换 + 5 合成） |
| 进度 | `data/farmstorm/advancement` | **16** |
| 语言键 | `assets/farmstorm/lang/{zh_cn,en_us}.json` 的顶层键 | 各 **328** |

终端自己应新增**恰好 5 个**产物文件，路径写死在验收第 28 条：`blockstates/control_terminal.json`、`models/block/control_terminal.json`、`models/item/control_terminal.json`、`loot_table/blocks/control_terminal.json`、`recipe/control_terminal.json`。贴图与 mcmeta 那 7 个文件**不在** `src/generated` 下（`src/generated` 的 mcmeta 计数为 0，D-262 的补充）。

---

## §4 行为规格

### 4.1 模式三态状态机（含 `OFF`）

`Mode` 是 `ControlTerminalBlockEntity` 的嵌套枚举，恰好三个值（D-269）：

| 状态 | 枚举序 | 是否投料 | 面板表现 | 迁移 |
|---|---|---|---|---|
| `OFF` | 0 | 不投料、不消费核库存 | 两个按钮都不带选中框 | 点任一按钮 → 对应的模式 |
| `CULTIVATOR` | 1 | 每 20 刻向 `kind = CULTIVATOR` 的机器投料 | 培养模式按钮带选中框 | 点培养模式 → `OFF`；点转换模式 → `CONVERTER` |
| `CONVERTER` | 2 | 每 20 刻向 `kind = CROP_CONVERTER` 的机器投料 | 转换模式按钮带选中框 | 点转换模式 → `OFF`；点培养模式 → `CULTIVATOR` |

三条不变量：

1. 初始态是 `OFF`（裁定 #13）：`registerDefaultState` 不碰模式，模式只存在方块实体的 NBT 里。
2. 互斥由枚举本身保证：不存在「两个都为真」的第四种状态（D-269 否决了「两个 boolean」）。
3. 再点已选中的那个退回 `OFF`（D-270）：`OFF` 是真实状态，不是推导出来的。

**编号陷阱**：`Mode` 的序是 `OFF=0 / CULTIVATOR=1 / CONVERTER=2`，而 `Link.Kind` 的线上码是 `CULTIVATOR=0 / CROP_CONVERTER=1 / TERMINAL=2`。两套编号不同轴，任何一处把 `Mode` 的序当 `Kind` 的码用，培养器的线会变成终端的线。

#### 4.1.1 终端的两个 NBT 键

| 键 | 类型 | 写点 | 读点 | 缺键语义 |
|---|---|---|---|---|
| `"Mode"` | `String`（`off` / `cultivator` / `converter`） | `setMode` 之后与 `saveAdditional` | `loadAdditional` | 缺键 ⇒ `OFF`（老存档与刚放下的终端都走这一条） |
| `"Core"` | `long`（`BlockPos#asLong`） | `setCore` / `clearCore` | `loadAdditional` | 缺键 ⇒ 未绑定（`CoreBound` 的可空语义，`CoreBound.java:15-23`、D-165） |

- 模式存**序列化名**而不是 `ordinal()`：与 `CropTier` 的 `StringRepresentable` 同一体例，且 `/data get block` 读出来是 `off` 这种可读值，验收第 7 条直接照它做判据。
- 未列入上表的状态一律不存：终端没有进度、没有槽、没有第二份绑定真相（D-264）。
- `"Core"` 键名与培养器（`CultivatorBlockEntity.java:46` 的 `TAG_CORE`）逐字相同，因为它是同一个 `CoreBound` 契约的一部分。

### 4.2 右键打开流程

1. `ControlTerminalBlock#getMenuProvider` 返回 `SimpleMenuProvider`，标题键 `container.farmstorm.control_terminal`，容器访问用 `ContainerLevelAccess.create(level, pos)`——照 `OutpostCoreBlock.java:110-116` 逐字同形。
2. 覆写 `useWithoutItem`（照 `OutpostCoreBlock.java:129-146`；空手与「不产生动作的持物」都走这里）。
3. 客户端分支照 `:132-135`：手持物是 `BlockItem` 返回 `PASS`（不掐断本地放置音），否则返回 `SUCCESS`。客户端不做归属判定（HC-03）。
4. 服务端分支：`player.openMenu(provider)` + 返回 `CONSUME`。**不判归属、不判绑定**——右键始终打开（D-267 / 裁定 #14）。这一点明确偏离机器那套「任一闸不过 = 右键无反应」（`CultivatorBlock.java:151-154` 的注释），偏离理由见 D-267 的补充。
5. 未绑定时不写聊天栏、不发动作栏提示：状态行由面板自己写「未绑定」，两个模式按钮置灰（`active = false`）。
6. 屏幕注册：`client/FarmstormClient.java:62-66` 的 `onRegisterMenuScreens` 里加一行 `event.register(FarmstormMenus.CONTROL_TERMINAL.get(), ControlTerminalScreen::new);`。漏了这一行客户端只有一条 warn、没有界面（D-273）。

### 4.3 放置绑定的三步顺序

照 `block/CultivatorBlock.java:86-103` 逐字同形，顺序不可换：

1. `super.setPlacedBy(...)`；`level instanceof ServerLevel` 与 `placer instanceof Player` 任一不成立就 return。
2. `OutpostCoreBlockEntity.selectCore(server, pos, new TeamResolver.Owner(player.getUUID(), player.getScoreboardName()), null)`；返回 `null` ⇒ 未绑定，**不写 `Core` 键**（缺键即未绑定，`:97-99`）。
3. `if (core.addCultivator(pos, Link.Kind.TERMINAL)) { terminal.setCore(core.getBlockPos()); }`——核侧先写成功，再写终端侧那份引用缓存；反过来会留下「核说没绑、终端说绑了」的假象（`:78-80` 的注释）。`addCultivator` 在已绑或满员时返回 `false`（`OutpostCoreBlockEntity.java:413-427`，满员判据 `:401-403`）。

两条补充：

- 没有可用核时方块照样放下、处于未绑定态；核侧 `rebindNearby`（`OutpostCoreBlockEntity.java:608`，周期 `:111` 的 `REBIND_INTERVAL_TICKS = 100`）会在 100 刻一轮的扫描里把「没人要」的终端补进名额——**前提是 `KIND_BY_BLOCK`（`:123-125`）里有终端**，否则候选过滤扫不到它（D-263）。
- 终端**占一个名额**（D-265）：与培养器、转换器共用 `cultivators` / `cultivatorKinds` 两条平行列表（`:147` / `:154`），上限读 `outpost_core.cultivatorCapacity`（默认 24）。不新建方法，复用 `addCultivator`。

### 4.4 每 20 刻投料循环（服务端）

`ControlTerminalBlock#getTicker` 照 `CultivatorBlock.java:155-167`：客户端返回 `null`，服务端返回一个 lambda 转调 `ControlTerminalBlockEntity.serverTick(...)`。循环九步：

| 步 | 动作 | 判据 / 依据 |
|---|---|---|
| 1 | `level.isClientSide` ⇒ return | HC-03：客户端永不 tick |
| 2 | `level.getGameTime() % FEED_INTERVAL_TICKS != 0` ⇒ return | `FEED_INTERVAL_TICKS = 20` 是 `private static final` 常量（D-271，不是 config 键） |
| 3 | `mode == OFF` ⇒ return | 裁定 #13、D-270 |
| 4 | `corePos() == null` ⇒ return | `ControlTerminalBlockEntity#corePos`（`CoreBound` 三方法之一） |
| 5 | `level.getBlockEntity(corePos())` 不是 `OutpostCoreBlockEntity` ⇒ return | 区块未加载时 `getBlockEntity` 只读已加载区块，不触发加载（`OutpostCoreBlockEntity.java:123` 的注释） |
| 6 | `!core.hasCultivator(this.getBlockPos())` ⇒ return | 绑定闸：核侧列表是唯一真相（`:396-398`、D-165） |
| 7 | `!OutpostCoreBlockEntity.inRange(corePos(), getBlockPos())` ⇒ return | 超范围不投料（D-266；`:537-543` 的纯函数形态） |
| 8 | 目标机器集 = `core.cultivatorKinds()`（`:383-386`）里等于本模式 kind 的那些下标，按同下标取 `core.cultivators()`（`:369-373`）的坐标 | 两条列表同长同下标（`:149-154` 的三条不变量） |
| 9 | 对每台目标机器跑一次「投 1 个」（见下） | 一轮每台最多 1 个（D-271） |

「投 1 个」的五步：

1. `level.getBlockEntity(pos)`；类型必须与模式匹配（`CULTIVATOR` → `CultivatorBlockEntity`，`CROP_CONVERTER` → `CropConverterBlockEntity`），否则跳过这台。
2. 按槽序扫核的 54 格（`getContainerSize()` 恒 54，`OutpostCoreBlockEntity.java:242-244` 与 `:67` 的 `SLOT_COUNT`），逐格取 `core.getItem(slot)`（`:257-259`）。
3. 对每格调**机器自己的**准入闸：培养器 `cultivator.canInsert(stack)`（`CultivatorBlockEntity.java:152-154`，内含 `isUsable()` ∧ 空槽 ∧ `admissible(stack)`）；转换器 `converter.canInsert(stack)`（`CropConverterBlockEntity.java:141-143`，内含 `isUsable()` ∧ 空槽 ∧ `convertible(stack)`）。
4. 第一个为真的格子：`ItemStack one = core.removeItem(slot, 1)`（`:262-268`，内部 `setChanged`），数量恰好 1；然后 `machine.insert(one)`（培养器 `:188-192`、转换器 `:163-167`，两者都只接受恰好 1 个并清零进度）。
5. 投成功后 **break**，这台本轮结束；扫满 54 格无一格通过就换下一台。

四条边界（每条一个判据，缺一条就是一个漏洞）：

| 边界 | 表现 | 判据落点 |
|---|---|---|
| 只填空槽 | 机器槽非空时本轮一个都不投 | `canInsert` 的「空槽」分量（`:153` 的 `this.held.isEmpty()`、`:142` 的同名分量） |
| 每台 1 个 | 一轮里同一台机器最多被投 1 个 | 第 5 步的 `break`；不存在「继续给同一台塞第二个」的路径 |
| 名额满 | 终端没绑上（`addCultivator` 返回 `false`）⇒ 第 6 步直接 return，一个都不投 | D-265；`OutpostCoreBlockEntity.java:401-403` |
| 超范围 | 终端的第 7 步、机器侧 `isUsable()` 的 `inRange`（`CultivatorBlockEntity.java:133`、`CropConverterBlockEntity.java:137`）两层都拦 | D-266 |

**准入判据只借不改**（D-272 / D-282）：终端不新增任何物品白名单、不按 `CropProfile` 判作物、不抄一份「史诗例外」。由此产生一个必须成文的后果：数据包新增一条转换配方，终端的培养模式自动多收一个投入物，与培养器同步。

### 4.5 面板按钮语义

| 动作 | 发出什么 | 服务端结果 |
|---|---|---|
| 点未选中的模式 | `clickMenuButton(player, id)`，`id = 0` 是培养模式、`id = 1` 是转换模式 | 模式切到该 id 对应的值，写 NBT + `setChanged` |
| 点已选中的模式 | 同一个 id | 模式退回 `OFF` |
| 未绑定时点任一按钮 | 客户端按钮 `active = false`，点不动；服务端 `clickMenuButton` 仍返回 `false` | 模式不变 |

实现要点：

- 屏幕 `init()` 里建两个 `Button.builder(Component.translatable(<模式键>), b -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id)).bounds(leftPos + 8, topPos + 30, 76, 20)` 与 `.bounds(leftPos + 92, topPos + 30, 76, 20)`；`bound == false` 时两个都 `active = false`。
- 选中态用 `GuiGraphics#renderOutline(bx - 1, by - 1, 78, 22, 0xFF4DD94D)` 画 1 像素绿框（`0.30 × 255 ≈ 77 = 0x4D`、`0.85 × 255 ≈ 217 = 0xD9`，与 §5.2 的绿线同一组分量）。
- 状态行：`font.drawString` 写在 `leftPos + 8, topPos + 56`；文本 = 绑定状态（已绑定 / 未绑定），`OFF` 之外再拼 `" · "` 与当前模式名。
- `clickMenuButton` 的服务端四步：`id` 不在 0~1 ⇒ `false`；`access.evaluate` 取不到方块实体 ⇒ `false`；未绑定 ⇒ `false`；否则按 §4.1 的迁移表改模式并返回 `true`。

### 4.6 `ContainerData` 两格的语义

| 格 | 语义 | 服务端读 | 客户端读 | 写（`set`） |
|---|---|---|---|---|
| 0 | 模式序号 | `mode().ordinal()`（0 / 1 / 2） | `menu.mode()` | 空实现（只往客户端推，不回写） |
| 1 | 绑定状态 | `core 已认定绑定 ? 1 : 0` | `menu.bound()` | 空实现 |

- 两格由 `AbstractContainerMenu#addDataSlots` 挂上，同步靠 `broadcastChanges` 每刻自动完成，**不新增第三个网络包**（D-273、红线 7）。
- 两格的 `get` 必须每次现读：菜单持 `ContainerLevelAccess`（照 `OutpostCoreMenu.java:57-59`），用 `access.evaluate` 取终端方块实体；取不到时返回 `0` 与 `0`（等价 `OFF` + 未绑定）。

> **〔v0.3.0 实施期更正 · F-337 · 依据作者裁定 Q1=A 与实机 API 语义〕**
> ~~原文：上表「写（`set`）」列两格都写「空实现（只往客户端推，不回写）」，且上面这条要求「`get` 每次 `access.evaluate` 现读」~~
> 现口径（C1）：**两格用普通 `SimpleContainerData(2)`，不覆写 `get`/`set`**。服务端在 `broadcastChanges()` 里用 `this.access.evaluate(...)` 读方块实体写值（lambda 必须返回非 null，否则 `ContainerLevelAccess.create` 内部的 `Optional.of` 抛 NPE 并被网络层吞掉），随后 `super.broadcastChanges()` 由原版链路发包；客户端那份 `access` 是 `ContainerLevelAccess.NULL`，`evaluate` 自动跳过，`mode()` / `bound()` 只读两格。理由：客户端菜单由 `MenuSupplier` 构造、手里没有坐标，`get` 里现读在客户端恒为 0；`set` 空实现会把服务端推来的值当场丢掉（两者都会表现成「面板永远未绑定」）。实现见 `menu/ControlTerminalMenu.java`（F-337）。
- 0 槽菜单的两个硬性形状：`quickMoveStack(Player, int)` 直接返回 `ItemStack.EMPTY`；`stillValid(Player)` 走 `stillValid(this.access, player, FarmstormBlocks.CONTROL_TERMINAL.get())`（照 `OutpostCoreMenu.java:132-135`）。
- 构造器两个重载照 `OutpostCoreMenu.java:52-59`：`(int id, Inventory inventory)` 给 `MenuType` 的 `MenuSupplier`，转调 `(int id, Inventory, ContainerLevelAccess)`，后者用 `ContainerLevelAccess.NULL` 兜底。

### 4.7 干草块记录的生命周期

**记录本体**（`util/PuffedHayData.java`，D-284）：

| 项 | 值 |
|---|---|
| 基类 | `extends SavedData`（照 `outpost/OutpostCoreIndex.java:48`、`trap/TrapFifoIndex.java:42`） |
| 落盘名 | `farmstorm_puffed_hay`（落 `world/data/farmstorm_puffed_hay.dat`） |
| 内部结构 | `LongOpenHashSet`，元素 = `BlockPos.asLong()` |
| NBT 键 | `"positions"`，类型 `long[]`（`CompoundTag#putLongArray` / `getLongArray`） |
| 取实例 | `server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<PuffedHayData>(PuffedHayData::new, PuffedHayData::load), "farmstorm_puffed_hay")`（照 `OutpostCoreIndex.java:76-80`、`TrapFifoIndex.java:58-61`） |
| 写脏 | 每次 `add` / `remove` 之后 `setDirty()` |

**写入**（`entity/thrown/PuffedWheatProjectile.java:104-123`，D-285）：登记点在第 `:119` 的 `level.setBlock(pos, hay, Block.UPDATE_ALL)` **之后**，且必须再过一道 `hay.is(Blocks.HAY_BLOCK)`：

- `:113-115` 是防破坏底线（抗性 ≥ 3.6e6 跳过），`:116-118` 是白名单（非空气非流体跳过）；被这两条挡掉的格子不记。
- `fillHay` 是父类的私有方法，压缩膨胀麦通过覆写 `fillBlock()`（`CompressedPuffedWheatProjectile.java:34-37`）复用它、写入的是压缩干草块。不加 `hay.is(Blocks.HAY_BLOCK)` 这道判断，压缩干草块也会被登记成「该不掉落」，直接违反裁定 #6（§9 RT-93）。

**消费**（`event/FarmstormGameEvents.java` 的第 10 个监听，D-286）：

1. 监听 `net.neoforged.neoforge.event.level.BlockDropsEvent`（`:30` 类声明、`:68-70` `getDrops()`、`:101-103` `setCanceled`），只在服务端触发。
2. 判据两条同时成立才动手：`event.getState().is(Blocks.HAY_BLOCK)`、`event.getPos().asLong()` 在集合里。
3. 动手内容：`event.getDrops().clear()` 并移除该条记录。**用 `clear()` 不 `setCanceled(true)`**：后者会连经验与 `spawnAfterBreak` 一起掐掉（D-286 的补充）。
4. 类注释 `:30` 的「只做九件事」改「十件事」，并在 `:31-39` 的 ① ~ ⑨ 之后补 ⑩ `onBlockDrops` 一行；第 10 个 `addListener` 接在 `:78` 的 `onServerStopping` 那一行之后。

**移除**：只有消费点这一条路径会删记录。方块被活塞推走、被爆炸炸掉、被 `/setblock` 覆盖时记录留着——判据 2 的 `getState().is(Blocks.HAY_BLOCK)` 保证它不会误伤别的方块，代价是记录集合只会变长不会变短（§9 RT-94）。老存档里已有的干草块不在集合里，照旧掉落，本版不回溯扫描（D-285 的已知影响）。

### 4.8 五个新类的最小公开面

写代码时按这张表对齐签名，多出来的成员一律不加（D-264 的「接口不加第四个方法」是同一个纪律）。

| 类 | 最小公开面 | 先例 |
|---|---|---|
| `ControlTerminalBlock` | `public static final DirectionProperty FACING`；`getRenderShape` 返回 `MODEL`；`createBlockStateDefinition` 只加 `FACING`；`newBlockEntity`；`setPlacedBy`；`useWithoutItem`；`getMenuProvider`；`getTicker` | `CultivatorBlock.java:59-72` / `:86-103` / `:155-167`，外加 `OutpostCoreBlock.java:110-116` / `:129-146` |
| `ControlTerminalBlockEntity` | 嵌套 `enum Mode`（`OFF` / `CULTIVATOR` / `CONVERTER` + 序列化名读写）；`corePos` / `setCore` / `clearCore`（`CoreBound` 三方法）；`mode()` / `setMode(Mode)`；`private static final int FEED_INTERVAL_TICKS = 20`；`static void serverTick(...)`；`createData()` 返回 2 格 `ContainerData`；`saveAdditional` / `loadAdditional` | `CultivatorBlockEntity.java:44` / `:84-101` / `:188-196` |

> **〔v0.3.0 实施期更正 · F-337 · 依据作者裁定 Q1=A〕**
> ~~原文：上表把 `createData()` 列为 `ControlTerminalBlockEntity` 的最小公开面之一~~
> 现口径（C2）：**方块实体不提供 `createData()`**。两格数据由菜单自己持有（`ControlTerminalMenu` 里的 `SimpleContainerData(2)`），方块实体只暴露 `mode()` / `setMode(Mode)` 与 `corePos` / `setCore` / `clearCore`；`FEED_INTERVAL_TICKS` 与 `serverTick(...)` 由 F-340 落地。保留原句只会造出一处没人调用的死代码（LESSONS §3.2 的同类形状）。
| `ControlTerminalMenu` | 两个构造器（`(int, Inventory)` 与 `(int, Inventory, ContainerLevelAccess)`）；`quickMoveStack`；`clickMenuButton`；`stillValid`；`mode()` / `bound()` | `OutpostCoreMenu.java:52-59` / `:132-135` |
| `ControlTerminalScreen` | `init()`（建两个按钮、按绑定量置 `active`）；`renderBg`（`fill` 纯色底）；`render`（选中框 + 状态行） | `OutpostCoreScreen.java:30-52` 的屏注册与渲染时序 |
| `PuffedHayData` | `static PuffedHayData get(MinecraftServer)`；`boolean contains(BlockPos)`；`void add(BlockPos)`；`boolean remove(BlockPos)`；`save(CompoundTag, HolderLookup.Provider)`；`static PuffedHayData load(CompoundTag, HolderLookup.Provider)` | `OutpostCoreIndex.java:48` / `:76-80`、`TrapFifoIndex.java:42` / `:58-61` |

---

## §5 协议

### 5.1 为什么不新增第三个 payload

三个理由，缺一条都不成立：

1. 面板只有两格状态，`ContainerData` 由菜单的 `broadcastChanges` 自动同步，不需要自定义包（D-273 否决了「新增第三个 S2C 包同步模式」）。
2. 绿色连线不是新数据：既有 `CultivatorLinkPayload` 的第三段就是一个 `BYTE`，多一个枚举值即可表达（D-274）。
3. 红线 7 明写「不得新增第三个网络包」。新增一个包还要再动一次协议版本串，代价与收益不成比例。

因此 网络包 **2 + 0 = 2**（`network/FarmstormNetwork.java:41-48` 的两条 `playToClient` 一行不动）。

### 5.2 `Link.Kind` 追加油上码 `TERMINAL(2)`

| 值 | 线上码 | 含义 | 颜色（R / G / B） | 十六进制 |
|---|---|---|---|---|
| `CULTIVATOR` | 0 | 农作物培养器 | `0.30 / 0.85 / 0.90` | 既有，一个字节不动 |
| `CROP_CONVERTER` | 1 | 农作物转换器 | `0.92 / 0.22 / 0.22` | 既有，一个字节不动 |
| `TERMINAL` | **2** | 控制终端 | `0.30 / 0.85 / 0.30` | `0x4D / 0xD9 / 0x4D` |

- 枚举声明在 `network/CultivatorLinkPayload.java:69-71`，追加 `TERMINAL(2)`；`:64-68` 的注释（「`CULTIVATOR = 0`、`CROP_CONVERTER = 1`」）补第三个值。
- 渲染侧：`client/CultivatorLinkRenderer.java` 在 `:56-58` 的红三常量之后加 `TERMINAL_RED = 0.30F` / `TERMINAL_GREEN = 0.85F` / `TERMINAL_BLUE = 0.30F`，并在 `:94-103` 的 `switch (link.kind())` 里加 `case TERMINAL -> { ... }` 分支；`ALPHA`（`:60`）与端点小框（`:107-108`）共用同一组分量，主线与两个小框不会一色一半。
- `:94-103` 的 `switch` 是**无 `default` 的穷尽式**：枚举多一个值而分支没补，代码**编译不过**。这是本版唯一一处「漏改立刻报错」的地方，其余漏改都是静默的。

### 5.3 payload 字段与字节

`CultivatorLinkPayload`（`network/CultivatorLinkPayload.java:30`）结构一个字节都不变：

| 段 | 字段 | codec | 字节 |
|---|---|---|---|
| 整包 | `links` | `ByteBufCodecs.list(MAX_LINKS)` 前缀 VarInt 条数 | 1 + (条目数 × 单条) |
| 单条 1 | `core` | `BlockPos.STREAM_CODEC`（`:47`） | 由原版 codec 决定，实现不手写字节 |
| 单条 2 | `cultivator` | `BlockPos.STREAM_CODEC`（`:48`） | 同上 |
| 单条 3 | `kind` | `ByteBufCodecs.BYTE.map(Kind::byId, Kind::id)`（`:49`） | **恒 1 字节** |

- `MAX_LINKS = 1024`（`:36`）是解码上限，不变；默认配置下每玩家 4 核 × 24 名额 = 96 条，远小于上限。
- 控制终端**占名额**（D-265），因此它也出现在这条 payload 里、也画线；线上的 `cultivator` 那一端就是终端的坐标，渲染层不知道也不问它是不是「机器」（`CultivatorLinkRenderer.java:39-41`：不按坐标反查方块）。

### 5.4 协议版本 `"3"` → `"4"`

- 改 `network/FarmstormNetwork.java:29` 的字面量；同一处的版本沿革注释（`:21-28`）补一行「v0.3.0：`Link.Kind` 多一枚举值（`TERMINAL = 2`）⇒ 3 → 4（D-274 / D-276）」。
- 自检日志两行（`:53-56`）会把 `PROTOCOL_VERSION` 打出来，改完启动一次就能在日志里读到 `（版本 4，S2C 只读…）` 两行。
- 跨版本行为：旧客户端收到 `id = 2` 时 `byId` 会回落到 `CULTIVATOR`（`:85-92`），画出的是青色线而不是崩——这是前向兼容的兜底，不是本版的验收对象；升版本的意义是握手阶段就拒掉跨版本联机。

### 5.5 广播时点（终端侧要写的代码为 0 行）

连线是绑定的**观察面**，三处时点全部由既有代码发起：

| 时点 | 发起位置 | 读哪份数据 |
|---|---|---|
| 绑定变化 | `OutpostCoreBlockEntity.addCultivator`（`:413-427`，成功后的 `:423-425` 调 `CultivatorLinkBroadcaster.requestAll`） | 核侧 `cultivators` / `cultivatorKinds` 两条列表 |
| 玩家登录 / 换维度 / 重生 | `FarmstormGameEvents` 的第 ②④⑤ 个监听（`:69` / `:73-74`） | 同上 |
| 核被拆 | `OutpostCoreBlockEntity.java:883` 的 `release()` | 同上 |

因此终端只要在 §4.3 第 3 步调一次 `core.addCultivator(pos, Link.Kind.TERMINAL)`，绿线自己就出现；终端侧不需要任何一处广播代码（`CultivatorLinkBroadcaster` 一行不改）。

---

## §6 配置

### 6.1 逐键表

本版新增 **0** 键、删除 **0** 键、改默认值 **0** 键（D-271）：

| 键名 | 默认值 | 范围 | 描述 | 本版动作 |
|---|---|---|---|---|
| ——（0 个） | —— | —— | —— | 不增、不删、不改 |

- server config 175 + 0 = **175**，client config 3 + 0 = **3**（D-258 的冻结值）。
- 终端节拍写成 `private static final int FEED_INTERVAL_TICKS = 20;`，与 `OutpostCoreBlockEntity.java:111` 的 `REBIND_INTERVAL_TICKS = 100` 同一体例：编译期常量、不存在「配成 0」的可能。
- 否决了 `control_terminal.intervalTicks`（D-271 的补充）：加一个键就要同步检查器的键集口径与四份文档的数字，而 plan 没要求可调。

### 6.2 老 toml 纪律

新增 0 键意味着老存档的 `run/config/farmstorm-server.toml`（175 键）与 `farmstorm-client.toml`（3 键）**逐键读回原值、不补写、不重排**：NeoForge 只在「文件缺键」时补写缺的那几个，键集不变就不会发生补写（交接 §8.10 记的正是「改默认值不会回写到已有 toml」）。本版连默认值都没改，因此老文件一个字节都不会变。

---

## §7 语言键

总数 321 + 7 = **328**，zh_cn 与 en_us 键集逐字一致（由 `ModLangProvider` 的构造方式保证：两种语言从同一张 `ENTRIES` 表按列写盘，`:63-68`）。

### 7.1 删（0 键）

| # | 键名 | 原 zh_cn | 原 en_us | 处置 |
|---|---|---|---|---|
| —— | —— | —— | —— | 本版删 0 键 |

### 7.2 加（7 键，逐键列全）

| # | 键名 | zh_cn | en_us | 出处 | 插入位置 |
|---|---|---|---|---|---|
| 1 | `item.farmstorm.control_terminal` | 控制终端 | Control Terminal | D-259 | `ModLangProvider.java:106` 之后（`plain_chives_seeds` 那条之后、`:107` 的方块段注释之前） |
| 2 | `block.farmstorm.control_terminal` | 控制终端 | Control Terminal | D-259 | `:140` 之后（`plain_chives_crop` 之后、`:141` 的实体段注释之前） |
| 3 | `container.farmstorm.control_terminal` | 控制终端 | Control Terminal | D-273 | `:175` 之后（`container.farmstorm.outpost_core` 之后） |
| 4 | `gui.farmstorm.control_terminal.mode.cultivator` | 培养模式 | Cultivate | D-270 | `:175` 之后、第 3 条之下 |
| 5 | `gui.farmstorm.control_terminal.mode.converter` | 转换模式 | Convert | D-270 | 第 4 条之下 |
| 6 | `gui.farmstorm.control_terminal.status.bound` | 已绑定 | Bound | D-267 | 第 5 条之下 |
| 7 | `gui.farmstorm.control_terminal.status.unbound` | 未绑定 | Not Bound | D-267 | 第 6 条之下 |

- 第 1、2 条走 `:443-449` 的 `item(...)` / `block(...)` 助手；第 3 ~ 7 条走 `:455-458` 的 `plain(...)`（键名本身就完整）。
- 第 3 条是容器标题（`getMenuProvider` 的 `Component.translatable`），必须与 §4.2 第 1 步的键名逐字一致。
- 第 4、5 条是两个按钮的标签，逐字取 BRIEF §3 的「培养模式 / 转换模式」；第 6、7 条是状态行的两半，`OFF` 之外由面板拼 `" · "` 与模式名（§4.5）。
- `man瓜` 五处（`:80` / `:114` / `:123` / `:199` / `:213`）一个字都不动（D-287）。

### 7.3 改名（0 键）

| # | 旧键名 | 新键名 | 处置 |
|---|---|---|---|
| —— | —— | —— | 本版改名 0 键 |

### 7.4 改值（0 键）

| # | 键名 | 原值 | 新值 | 处置 |
|---|---|---|---|---|
| —— | —— | —— | —— | 本版改值 0 键 |

### 7.5 表头口径同步（`ModLangProvider.java:10-16`）

`:10` 的「**321** 键」改成「**328** 键（v0.3.0 口径，64 号 D-258）」，`:13-16` 的按组拆解改成下面这段（逐字）：

```
 * **29** `item.` + **29** `block.` + **32** `advancements.` + **178** `farmstorm.configuration.`（= 175 server + 3 client）
 * + **13** `entity.farmstorm.` + **47** 其余（4 effect + 3 subtitles + 6 death + 20 tooltip + 1 villager + **5 gui**
 * + **2 container** + 1 itemGroup + 2 key + 3 command）= **328**（29 + 29 + 32 + 178 + 13 + 47）。
```

算式核对：4 + 3 + 6 + 20 + 1 + 5 + 2 + 1 + 2 + 3 = 47；29 + 29 + 32 + 178 + 13 + 47 = 328 = 321 + 7。

两个组的增减要写清，否则下一轮读不出差在哪：`gui` 1 → 5（+4：两条模式标签 + 两条状态行），`container` 1 → 2（+1：面板标题）。沿革段（`:23` 起）只**追加**一行「v0.3.0 的 F 系列再加 7 条（item 1 / block 1 / container 1 / gui 4），到 328 键」，既有各站的数字一个都不改。

### 7.6 键名体例对照

| 前缀族 | 键形状 | 既有例 | 本版新增 |
|---|---|---|---|
| item | `item.farmstorm.<id>` | `item.farmstorm.cultivator` | §7.2 第 1 条 |
| block | `block.farmstorm.<id>` | `block.farmstorm.cultivator` | §7.2 第 2 条 |
| container | `container.farmstorm.<id>` | `container.farmstorm.outpost_core`（`:175`） | §7.2 第 3 条 |
| gui | `gui.farmstorm.<屏>.<元素>` | `gui.farmstorm.core_hud.self`（`:436`） | §7.2 第 4 ~ 7 条 |
| effect / subtitles / tooltip / death / key / command | 各自前缀 | 见 `:161-203` / `:434-439` | 0 条 |

四条体例纪律：一个字都不含 `_a`（注册 id 的规则同样适用于键名）；模式与状态都放在 `gui.` 族而不是新开一族（否则 §7.5 的 42 → 47 拆不成「5 gui + 2 container」）；`container` 与 `gui` 两族的键名在拼装状态行时不重复出词（状态行 = 绑定状态 + `" · "` + 模式名）；`man瓜` 五处不动（D-287）。

---

## §8 验收标准

每条三列：怎么做 / 期望看到什么 / 依据（另加最左的编号列）。编号 1 ~ 29，纯阿拉伯数字，A 系列归 67 号。第 19 条是唯一一条「只记录不断言」的条目。

| # | 怎么做 | 期望看到什么 | 依据 |
|---|---|---|---|
| 1 | `python -X utf8 tools\verify_project.py` | 注册面一行读 **96**；12 类小计与 §1.1 的目标列逐项相等 | D-258、D-289、`tools/verify_project.py:459-470` |
| 2 | 同上一条命令的收尾统计 | **FAIL 0**（WARN 条数只允许 ≤1，即基线那一条） | BRIEF §0 |
| 3 | 用 `ConvertFrom-Json` 读两份语言文件，各取 `PSObject.Properties` 的元素个数，en_us 同法 | 两份都读到 **328**，且键集逐字相同 | D-258、§7.5 |
| 4 | `Get-Content src/generated/resources/assets/farmstorm/blockstates/control_terminal.json -Raw` 数 `variants` 的键 | 恰好 **4** 个键：`facing=north` / `east` / `south` / `west`，`y` 分别为不写 / 90 / 180 / 270 | D-261、§3.4 |
| 5 | 进游戏对着一台终端连看一轮；同时用 `Get-ChildItem ...\textures\block -Filter "control_terminal_*.png"` 核尺寸 | 三面各 **24 帧**（每帧 32×32），2 刻/帧，一轮 48 刻 = 2.4 秒；PNG 高度 768 = 24 × 32 | D-262、BRIEF §7 第 4 条 |
| 6 | 空手右键一台已绑定的终端 | 控制面板打开，标题显示「控制终端」，槽位 0 个 | D-267、D-273 |
| 7 | 打开面板后用 `/data get block <终端坐标> Mode` 读数，再分别点两个按钮三次 | 依次读到 `off` → `cultivator` → `off`（再点同一个）→ `converter` | D-269、D-270、§4.1 |
| 8 | 在两台机器中间放一台终端，切到培养模式，用 `/data get block <核坐标>` 盯核的 54 格，按世界刻记录每次投料的 `gameTime` | 相邻两次投料的 `gameTime` 差恒为 **20** | D-271、§4.4 第 2 步 |
| 9 | 同上场景，核里放 3 组各 64 个的合法投入物，盯培养器槽 | 一轮只少 1 个（核里 64 → 63），机器槽变成 1 个；下一轮机器槽非空 ⇒ 核里不再减少 | D-271、§4.4 的「只填空槽」与「每台 1 个」 |
| 10 | 造满 24 个名额（`outpost_core.cultivatorCapacity` 默认 24），再放第 25 台终端 | 终端放下但未绑定；面板开得起来、状态行「未绑定」、两个按钮置灰；核库存一格不减少 | D-265、D-267、§4.4 第 6 步 |
| 11 | 把一台已绑定的终端用活塞推到 3×3 区块之外（或跨区块边界放第 4 个区块外） | 该终端不再投料；核库存一格不减少 | D-266、§4.4 第 7 步 |
| 12 | 终端与核都绑上，用 F3 + 截图看连线 | 一条绿线：像素分量落在 `0x4D / 0xD9 / 0x4D` 附近（`0.30/0.85/0.30 × 255`）；同一屏里培养器仍是青线、转换器仍是红线 | D-275、D-277 |
| 13 | 用木镐破坏一台培养器，数掉落物 | 掉落 **0** 个（方块仍被移除）；判据是掉落物计数，不是「破坏不了」 | D-278、D-279、`ServerPlayerGameMode.java:269-275` |
| 14 | 用石镐破坏一台培养器 | 掉落 **1** 个培养器（本体） | D-278、D-279 |
| 15 | 依次把火把花 `minecraft:torchflower`、瓶子草 `minecraft:pitcher_plant`、甜浆果 `minecraft:sweet_berries`、金苹果 `minecraft:golden_apple` 右键放进培养器 | 4 个全部被收进槽（`/data get block` 的 `Held` 非空），一个都不被拒 | D-281、`CultivatorBlockEntity.java:143-149`、`CropProfile.java:74/:78/:89/:105` |
| 16 | 让膨胀麦落在草地上，记下落点；用石镐挖其中一格 | 掉落 **0** 个干草块；再 `/data get storage` 之外用日志或调试读法确认该坐标已从 `farmstorm_puffed_hay` 集合移除 | D-285、D-286、§4.7 |
| 17 | 让压缩膨胀麦落地，用石镐挖其中一格 | 照旧掉落 **1** 个压缩干草块（记录集合里不该有这些坐标） | 裁定 #6、§4.7 的 `hay.is(Blocks.HAY_BLOCK)` |
| 18 | 拿一份 v0.2.7 的老存档（里面已有膨胀麦落地形成的干草块），进游戏直接挖那一格 | 照旧掉落 1 个干草块（老坐标不在集合里，本版不回溯扫描） | D-285 的已知影响 |
| 19 | 用金镐破坏一台培养器与一台转换器，记录掉落物数量 | 只**记录**读数与工具（金镐）本身，不写成断言；本条不参与 PASS / FAIL 判定 | D-280、BRIEF §7 第 1 条 |
| 20 | 读启动日志里 `自检 网络包注册 …` 两行 | 两行都带 `（版本 4，S2C 只读…）` | D-276、`FarmstormNetwork.java:29` / `:53-56` |
| 21 | `(Get-ChildItem src\main\java -Recurse -Filter *.java).Count` | 读数 **156** | D-258、§2.1 |
| 22 | 数四类 JSON：`models/block` / `loot_table/blocks` / `recipe` / `blockstates` | 分别读到 **79** / **29** / 配方 JSON **26**（21 转换 + 5 合成）/ **29** | D-258、§1.2、§1.3 |
| 23 | 数 `data/farmstorm/advancement` 与 `ModRecipeProvider.java` 的 `oneWay` 调用 | 进度 **16** 个 JSON（不变）、转换配方 **21** 条（不变） | D-258、`ModRecipeProvider.java:59-85` |
| 24 | 读 `CultivatorLinkPayload.java:69-71` 的枚举与 `byId`（`:85-92`） | `CULTIVATOR(0)` / `CROP_CONVERTER(1)` 在线上的码不变，`TERMINAL(2)` 只能追加；`byId(2)` 返回 `TERMINAL` | D-274、红线 2 |
| 25 | 放一台终端，读 `/data get block <核坐标> Cultivators` 与 `CultivatorKinds` | 两条列表同长同下标，末位是终端坐标与 `terminal`；名额计数 +1 | D-265、`OutpostCoreBlockEntity.java:369-386` |
| 26 | 未绑定状态下右键终端，再对着核把终端绑上重开 | 第一次开得起来、两个按钮点不动、库存不变；绑定后状态行变「已绑定」、按钮可点 | D-267、§4.2、§4.5 |
| 27 | `Select-String -Path src\generated\resources\assets\farmstorm\blockstates\control_terminal.json -Pattern "working"`，再对 `ControlTerminalBlock.java` 搜 `WORKING` | 两处都 **0 命中**（本版没有工作态属性，4 个 variant 全是 `facing=*`） | D-261、§3.4 |
| 28 | 跑一次 datagen 后列 `control_terminal` 的全部产物：`Get-ChildItem src\generated -Recurse -Filter "*control_terminal*"` | 恰好 **5** 个文件：`blockstates/control_terminal.json`、`models/block/control_terminal.json`、`models/item/control_terminal.json`、`loot_table/blocks/control_terminal.json`、`recipe/control_terminal.json` | §1.3、§3.7 |
| 29 | 数贴图的三个数：`Get-ChildItem src\main\resources\assets\farmstorm\textures\block -Filter *.png` 与 `-Filter *.mcmeta` 各数一次，再把两数相加 | `.png` 读到 **88**（84 + 4）、`.mcmeta` 读到 **8**（5 + 3）、两者相加 **96** 个文件（89 + 7）；三个数任何一个串位都算不通过 | §1.2、§9 RT-96、§9.1 第 1 行 |

---

## §9 风险

| 风险 | 触发条件 | 后果 | 规避 |
|---|---|---|---|
| **RT-92** · 24 帧 / 32×768 动画无先例 | 直接照抄项目内 5 个既有 mcmeta 的 16 帧口径，或 PNG 被重采样 | 只播前 16 帧、第 17 ~ 24 帧永远不出现；或帧条被拉伸成 32×512 | mcmeta 用素材包那份（显式 `frames` 0~23）；PNG 逐字节复制不重编码；验收第 5 条进游戏数帧 |
| **RT-93** · 压缩膨胀麦共用 `fillHay` 被误登记 | 登记点只判「`setBlock` 成功」而不判写入的方块 | 压缩干草块被挖时也不掉落，直接违反裁定 #6 与验收第 17 条 | 登记前加 `hay.is(Blocks.HAY_BLOCK)`。依据 `CompressedPuffedWheatProjectile.java:34-37` 只覆写 `fillBlock()` |
| **RT-94** · 记录集合只增不减 | 干草块被活塞推走 / 被爆炸炸掉 / 被 `/setblock` 覆盖，都不走 `BlockDropsEvent` | `farmstorm_puffed_hay.dat` 里留下永不消费的坐标；存档缓慢变大 | 消费点唯一且判据带 `getState().is(Blocks.HAY_BLOCK)`，误伤不可能；记录量级与膨胀麦落点同阶，不额外做清理 |
| **RT-95** · 投料一轮的配方查询成本 | 24 台机器 × 54 格逐格调 `canInsert`（`convertible` 走 `recipeManager.getRecipeFor`） | 单轮最多 24 × 54 = 1296 次配方查询，20 刻一次；极端配置下抖动 | 命中即 `break`（§4.4 五步之第 5 步）；先判槽空再查配方（`canInsert` 的短路顺序已保证）；验收第 9 条盯读数 |
| **RT-96** · 贴图有三条数法 | 拿「89」当 PNG 数去核，或拿「文件总数」当 PNG 数去核 | 84 PNG / 5 mcmeta / 89 文件的三个数互相串位，照已作废的旧口径 93 写会稳定变红 | 三个数各用一条命令数：`-Filter *.png` ⇒ 本版 **88**（84 + 4）；`-Filter *.mcmeta` ⇒ **8**（5 + 3）；两者相加 ⇒ **96** 个文件（89 + 7）。口径见 §1.2 与 §9.1 第 1 行 |
| **RT-97** · 史诗四条写成南瓜 | 把南瓜 `minecraft:pumpkin` 当成「本版新放行」的第 4 条 | man瓜 的档位是 `CropTier.COMMON`（`CropProfile.java:69-72`），南瓜在基线里就能进培养器；照它写断言会红 | 四条按「火把花 `:73-75` / 瓶子草 `:76-78` / 甜浆果 → 幸运浆果 `:88-89` / 金苹果 `:102-105`」实测（验收第 15 条）；南瓜只作基线行为记录，不写进断言。权威自述见 `CropConverterBlockEntity.java:148-149` |
| **RT-98** · 金镐在 `needs_stone_tool` 上的行为 | 把「金镐挖不动」写成断言 | 断言可能与原版 `incorrect_for_gold_tool` 的真值相反，变成必红的验收 | 按 D-280 只记录不断言（验收第 19 条）。机理已可读：`Tiers.java:16` 的 GOLD 带 `INCORRECT_FOR_GOLD_TOOL`，`Tier.java:23` 把它做成 `deniesDrops` 规则，`VanillaBlockTagsProvider.java:1589` 让那条标签包含 `#needs_stone_tool`，`Tool.java:50-58` 首个命中即返回 `false` |
| **RT-99** · 半边改动与穷尽 switch 的静默失败 | 只改标签不改 `KIND_BY_BLOCK`（或反之）；`RegisterMenuScreensEvent` 漏注册屏幕；`ModItemModelProvider` 白名单漏登记；`ModBlockStateProvider` 漏了 `else if` 落进兜底 | 依次表现为：终端永远绑不上 / 右键只有一条 warn 没有界面 / 物品栏紫黑格 / blockstate 指向不存在的贴图。四种都不报错 | 四处同一任务改完（D-263 红线 1）；枚举 `TERMINAL` 与渲染 `switch` 必须同提交（`:94-103` 无 `default`，漏了编译不过） |

### 9.1 上游更正记录（均已定稿，本文按现口径写）

| # | 项 | 旧口径（已作废） | 现口径（定稿） | 出处与状态 |
|---|---|---|---|---|
| 1 | 方块贴图读数 | 「89 个 PNG + 5 个 mcmeta」，冻结值 PNG **93** | `textures/block` 实为 **84** 个 `.png` + **5** 个 `.mcmeta` = **89** 个文件（89 是文件总数，不是 PNG 数）；本版 +4 PNG +3 mcmeta ⇒ **88 PNG / 8 mcmeta / 96 文件** | BRIEF §0 已修正；本文 §1.2 与 §10 第 22 行按现口径写 |
| 2 | 史诗四条的成员 | 「南瓜 → man瓜」算作史诗档输入，档位来源记作 `CropProfile.java:78` | 四条史诗输入是 火把花 `:73-75` / 瓶子草 `:76-78` / 甜浆果 → 幸运浆果 `:88-89` / 金苹果 `:102-105`；man瓜 是 `CropTier.COMMON`（`:69-72`），南瓜一直在基线里就能进培养器 | 64 号 D-281 的补充已定稿；条数仍是 4（裁定 #7 不变），本文 §8 第 15 条按现名单写 |
| 3 | 事实卡行数 | 240 → 239 行 | **241** 行 | BRIEF 因补「南瓜是普通档」一条注而 +2 行；本文抬头与本行按最终实读 241 写 |
| 4 | 终端合成配方 | 四角铁锭、四边玻璃（与培养器同料同图样） | 四角玻璃、四边铁锭、正中红石（`GIG` / `IRI` / `GIG`），材料总数 4 铁 + 4 玻璃 + 1 红石不变 | 64 号 D-268 已定稿；本文 §2.2 ⑤ 按现图样写 |

---

## §10 与上一版（v0.2.7）的差异表

| # | 项 | v0.2.7 | v0.3.0 |
|---|---|---|---|
| 1 | 方块 `farmstorm:control_terminal` | 不存在（全仓 0 命中） | 新增，属性照铁砧七项（D-259、D-260） |
| 2 | 方块物品 `farmstorm:control_terminal` | 不存在 | 新增，进创造栏（§2.2 ②） |
| 3 | 方块实体类型 | 8 个 | 9 个（新增 `control_terminal`） |
| 4 | 菜单类型 | 1 个（`outpost_core`） | 2 个（新增 `control_terminal`，0 槽） |
| 5 | 控制面板 | 不存在 | 两个模式按钮 + 一行状态，状态走 `ContainerData`（D-273） |
| 6 | 模式状态 | 不存在 | `OFF / CULTIVATOR / CONVERTER` 三态，存终端自己的 NBT（D-269） |
| 7 | 投料节奏 | 不存在 | 每 20 刻一轮、每台机器每轮 1 个，常量不是配置键（D-271） |
| 8 | `Link.Kind` 值数 | 2（0 / 1） | 3（追加 `TERMINAL(2)`，既有两值不重排，D-274） |
| 9 | 终端连线颜色 | 只有青 / 红两色 | 绿 `0.30 / 0.85 / 0.30`，与终端同占 24 名额（D-275） |
| 10 | 协议版本 | `"3"` | `"4"`（D-276） |
| 11 | `processing_block` 成员 | 2 | 3（D-263） |
| 12 | `mineable/pickaxe` 成员 | 10 | 12（补培养器与终端，D-278） |
| 13 | `needs_stone_tool` 成员 | 无这一条标签 | 3 个成员，木镐起不到掉落（D-279、D-280） |
| 14 | 培养器准入闸 | 命中转换配方且输出不是史诗档 | 命中转换配方即可（删掉档位比较，D-281） |
| 15 | 膨胀麦落点登记 | 无 | 每次 `setBlock` 成功后记一条坐标（D-285） |
| 16 | 干草块掉落 | 照旧掉落 | 记录命中时 `getDrops().clear()` 并移除记录（D-286） |
| 17 | 事件监听数 | 9 | 10（`BlockDropsEvent`） |
| 18 | 注册面合计 | 92 | 96（D-258、D-289） |
| 19 | 语言键 | 321 | 328（+7） |
| 20 | Java 文件 | 151 | 156（+5） |
| 21 | 方块模型 / 战利品表 / 合成配方 | 78 / 28 / 4 | 79 / 29 / 5 |
| 22 | 方块贴图 PNG / mcmeta / 文件总数 | 84 / 5 / 89 | 88 / 8 / 96（口径见 §1.2 与 RT-96） |
| 23 | server config / client config | 175 / 3 | 175 / 3（0 增 0 删） |
| 24 | 转换配方 / 进度 | 21 / 16 | 21 / 16（不变） |
| 25 | Mixin / 网络包 | 0 / 2 | 0 / 2（不变） |
| 26 | 版本串 | `0.2.7` | `0.3.0`（D-255） |
