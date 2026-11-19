# 农场风云 v0.3.0（1.20.1）技术规格

> **定位**：实现方照着就能写代码；凡是「下游必须自己猜」的地方都算没写完，本文没写死的行为一律回 06 号找 D 编号。
> **事实来源**：`docs/zh/06-DECISIONS-0.3.0-1.20.1.md`（D-015 ~ D-052，唯一真相源）+ `.work/v030/facts-A-numbers.md`（数字面）、`facts-B-api.md`（1.20.1 API 等价面）、`facts-C-anchors.md`（现有代码锚点）。**只引不改**，上游有错写进 06 号 §8。
> **基线与数字**：基线是 1.20.1 v0.2.8 已实现态（`mod_version=0.2.8`；Java 151 文件 / 20,116 行，facts-A §3.1）；数字全部取 D-018 冻结的那一组，逐处给算式，本文不新造数字。
> **行号与判据**：每一处 `文件:行号` 都取自上述三份事实报告或 06 号的实读值，不另造行号，报告里没有的锚点只点到「类#方法」；验收判据只认五类读数——**计数 / 日志行 / 指令读数 / grep 命中 / 像素比对**。
> **术语**：首次出现写「中文名（英文 id）」，之后只用中文名（D-051）。本文用到的 控制终端（`farmstorm:control_terminal`）、农作物培养器（`farmstorm:cultivator`）、农作物转换器（`farmstorm:crop_converter`）、据点核心（`farmstorm:outpost_core`）、膨胀麦（`farmstorm:puffed_wheat`）、压缩干草块（`farmstorm:compressed_hay_block`）、处理方块（`processing_block`）、位置记录（`PuffedHayData`）、全局战利品修饰器（global loot modifier，GLM）、绿色连线 十个词按 06 号 §0.2 的判定边界使用。

---

## §1 口径总表

### 1.1 注册面 12 类逐项（91 → 96）

| 类 | 基线 | 本版 | 增减 | 算式与落点 |
|---|---|---|---|---|
| items | 28 | **29** | +1 | 28 + 1 = 29；终端方块物品，照 `FarmstormItems.java:98-99` 的 BlockItem 形态 |
| blocks | 28 | **29** | +1 | 28 + 1 = 29；终端方块，照 `FarmstormBlocks.java:105-112` 的铁砧组 |
| block_entity_type | 8 | **9** | +1 | 8 + 1 = 9；照 `FarmstormBlockEntities.java:56-58` 的单方块例 |
| menu_type | 1 | **2** | +1 | 1 + 1 = 2；控制面板，照 `FarmstormMenus.java:17-18` |
| global_loot_modifier_serializers | 0 | **1** | +1 | 0 + 1 = 1；`puffed_hay` 的 codec，键类型见 `ForgeRegistries.java:187`（facts-B §2.2） |
| entity_type | 13 | 13 | 0 | 13 + 0 = 13 |
| mob_effect | 4 | 4 | 0 | 4 + 0 = 4 |
| sound_event | 3 | 3 | 0 | 3 + 0 = 3 |
| recipe_type | 1 | 1 | 0 | 1 + 0 = 1（`farmstorm:converting`，`TYPE_REGISTER`） |
| recipe_serializer | 1 | 1 | 0 | 1 + 0 = 1（同名 `SERIALIZER_REGISTER`） |
| creative_mode_tab | 1 | 1 | 0 | 1 + 0 = 1（`main`，facts-A §4） |
| damage_type | 3 | 3 | 0 | 3 + 0 = 3（`Farmstorm.java:208-211` 的三项字面） |
| **合计** | **91** | **96** | **+5** | 91 + 1 + 1 + 1 + 1 + 1 = 96 |

两列各自相加必须相等（§2.4 与验收 #1 的判据）：基线 28 + 28 + 8 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 0 + 1 = **91**；目标 29 + 29 + 9 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 2 = **96**。口径差一次说清（D-052）：检查器现状是 **12 类 92**，因为它把最后一类写成 1.20.1 根本不存在的 `data_component_type: 1`；运行期自检是 **11 类 91**。本版把那一格换成 `global_loot_modifier_serializers: 1`，两处同时对齐到 12 类 96，算式 **92 − 1 + 1 + 4 = 96**。

### 1.2 其余冻结项（D-018）

| 项 | 基线 | 本版 | 算式 | 说明 |
|---|---|---|---|---|
| server config / client config | 175 / 3 | **175 / 3** | 175 + 0 = 175；3 + 0 = 3 | 节拍写成常量（D-032），一个键不增 |
| 语言键（zh_cn / en_us） | 321 | **328** | 321 + 7 = 328；29 + 29 + 13 + 32 + 178 + 47 = 328 | 其余 42 → 47：container +1、gui +4（§7） |
| Java 文件 | 151 | **159** | 151 + 8 = 159 | §2.1 的八个新文件 |
| 网络包 / 协议串 | 2 / `"1"` | **2 / `"2"`** | 2 + 0 = 2；1 + 1 = 2 | 复用既有 payload，只多一枚举值（D-035、D-037） |
| Mixin | 0 | **0** | 0 + 0 = 0 | 不改 |
| 方块战利品表 | 28 | **29** | 28 + 1 = 29 | 终端 1 张 |
| 转换配方 / 合成配方 / 进度 | 21 / 4 / 16 | **21 / 5 / 16** | 21 + 0 = 21；4 + 1 = 5；16 + 0 = 16 | 需求 ② 只改准入闸不加配方；合成配方 +1 而进度不变（解锁进度被剥掉，`ModRecipeProvider.java:174-201`） |
| 方块模型 / blockstate / item 模型 | 78 / 28 / 28 | **79 / 29 / 29** | 78 + 1 = 79；28 + 1 = 29；28 + 1 = 29 | 终端 1 张 cube 模型 + 1 个 blockstate + 1 个 item 模型（走 §3.5 白名单） |
| 贴图 `.png` / `.mcmeta` / 文件总数 | 84 / 5 / 89 | **88 / 8 / 96** | 84 + 4 = 88；5 + 3 = 8；89 + 7 = 96 | 84 + 5 = 89 是文件总数而非 PNG 数（facts-A §3.5） |
| 事件监听 | 9 | **9** | 9 + 0 = 9 | **不变**：拦截面换成 GLM，`event/FarmstormGameEvents.java:67-79` 的九行 `addListener` 一行不动 |
| loot modifier 数据文件 | 0 | **2** | 0 + 2 = 2 | `data/farmstorm/loot_modifiers/puffed_hay.json` + 清单 `data/forge/loot_modifiers/global_loot_modifiers.json`（D-049） |

### 1.3 派生数字（供 §2 与 §3.7 引用）

| 派生项 | 基线 | 本版 | 算式 | 依据 |
|---|---|---|---|---|
| 配方 JSON 总数 | 25 | **26** | 21 + 5 = 26 | 转换配方 21 + 合成配方 5 |
| 语言键分组 | 321 | **328** | 28 + 28 + 13 + 32 + 178 + 42 = 321 → 29 + 29 + 13 + 32 + 178 + 47 = 328 | 06 号 §0.4 的分组算式 |

---

## §2 注册面逐项

### 2.1 新增五项 + 八个新 Java 文件

| # | 类 | 注册 id | 落点 | 同形的既有先例 |
|---|---|---|---|---|
| 1 | blocks | `control_terminal` | `registry/FarmstormBlocks.java`，紧接培养器注册块之后 | `:105-112`（`CULTIVATOR`）与 `:75-82`（`CROP_CONVERTER`）逐项相同 |
| 2 | items | `control_terminal` | `registry/FarmstormItems.java`，紧接三个 BlockItem 之后 | `:98-99` / `:104-105` / `:108-109` |
| 3 | block_entity_type | `control_terminal` | `registry/FarmstormBlockEntities.java`，紧接 `CULTIVATOR` 之后 | `:56-58`（单方块例） |
| 4 | menu_type | `control_terminal` | `registry/FarmstormMenus.java`，紧接 `OUTPOST_CORE` 之后 | `:17-18`（`MenuSupplier` + `FeatureFlags.VANILLA_SET`） |
| 5 | global_loot_modifier_serializers | `puffed_hay` | `registry/FarmstormLootModifiers.java`（新文件） | 键定义见 `ForgeRegistries.java:187`，泛型必须写 `Codec<? extends IGlobalLootModifier>` |

八个新 Java 文件（151 + 8 = 159），职责与禁令由本表钉死：

| # | 路径 | 职责 | 不许做的事 |
|---|---|---|---|
| 1 | `block/ControlTerminalBlock` | `FACING` 属性、`MODEL` 渲染形状、放置绑定、右键开面板、服务端 ticker | 不写第二份准入判据（D-043）；不加工作态属性（D-022） |
| 2 | `blockentity/ControlTerminalBlockEntity` | 嵌套 `Mode` 三态、`CoreBound` 三方法、每 20 刻投料、`createData()` 两格、NBT 读写 | 不自建绑定真相（D-025）；不存 `ordinal()`（D-030）；不给 `CoreBound` 加第四个方法 |
| 3 | `menu/ControlTerminalMenu` | 0 槽菜单：`quickMoveStack` 返回 `ItemStack.EMPTY`、`clickMenuButton` 改模式、`stillValid`、`mode()` / `bound()` | 不嵌据点核心的 54 格（D-034）；不新增第三个网络包 |
| 4 | `client/ControlTerminalScreen` | 两个模式按钮 + 一行状态 + 选中框 | 不新增 GUI 贴图；渲染层不按坐标反查方块（D-038） |
| 5 | `util/PuffedHayData` | `extends SavedData`：`BlockPos.asLong` 集合的取 / 判 / 加 / 删 / 存 / 读 | 不做成方块实体（D-045）；不对老存档回溯扫描 |
| 6 | `loot/PuffedHayLootModifier` | `extends LootModifier`、静态 `CODEC`、`doApply` 空表替换并删记录 | 不写成「取消破坏」语义（D-048）；不改非本表的掉落（D-047 否决项） |
| 7 | `registry/FarmstormLootModifiers` | `DeferredRegister<Codec<? extends IGlobalLootModifier>>`，`register("puffed_hay", …)`，挂 mod 总线 | 不用错注册表键；不在 `NewRegistryEvent` 之前 `get()`（`ForgeRegistries.java:113-116`） |
| 8 | `datagen/ModGlobalLootModifierProvider` | `extends GlobalLootModifierProvider`，`start()` 里 `add("puffed_hay", …)` | 不手写两份 JSON（D-049 否决第 12 项） |

### 2.2 五个落点逐条

**① 方块（`registry/FarmstormBlocks.java`）**：照 `:105-112` 那一组逐项抄（D-021；facts-C §11 记作「铁砧七项」）——`mapColor(MapColor.METAL)` + `strength(5.0F, 1200.0F)` + `requiresCorrectToolForDrops()` + `sound(SoundType.ANVIL)` + `pushReaction(PushReaction.BLOCK)`。铁砧的行为不照抄；`requiresCorrectToolForDrops()` 是需求 ② 的前提，没有它「石镐及以上才掉」无处表达。同时照 `CropConverterBlock.java:44` 与 `:67-69` 加 `DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING`、放置时取 `ctx.getHorizontalDirection().getOpposite()`（D-022）；基类是 `HorizontalDirectionalBlock` 体系（`HorizontalDirectionalBlock.java:8-9`），`DirectionPropertyBlock` 这个类名在 1.20.1 全树 0 命中（facts-B §6），照 1.21.1 写直接编译不过。

**② 方块物品与创造栏（`registry/FarmstormItems.java`）**：注册照 `:98-99` 的 `registerItem("control_terminal", properties -> new BlockItem(FarmstormBlocks.CONTROL_TERMINAL.get(), properties), otherProperties())`；创造栏清单（`creativeTabItems()`）末尾追加 `CONTROL_TERMINAL` 一项，漏这一行 = 创造栏里看不到且不报错。物品模型不走 `textures/item/`，走 §3.5 的白名单。

**③ 方块实体类型（`registry/FarmstormBlockEntities.java`）**：照 `:56-58` 的 `REGISTER.register("control_terminal", () -> BlockEntityType.Builder.of(ControlTerminalBlockEntity::new, FarmstormBlocks.CONTROL_TERMINAL.get()).build(null))`，单方块例，与培养器同形；挂总线那一行（`Farmstorm.java:66`）不动。**④ 菜单类型（`registry/FarmstormMenus.java`）**：照 `:17-18` 的 `REGISTER.register("control_terminal", () -> new MenuType<>(ControlTerminalMenu::new, FeatureFlags.VANILLA_SET))`。1.20.1 的 `MenuType` 只有 `(MenuSupplier, FeatureFlagSet)` 一个构造（`MenuType.java:49-52`，facts-B §3.3），没有 `IMenuTypeExtension`，因此不能通过构造参数附带 `BlockPos`，位置由 `ContainerLevelAccess` 在菜单里解析。

**⑤ 合成配方（`datagen/ModRecipeProvider.java`，D-029）**：新方法照培养器（`:117-127`）的形态紧接其后，`save(recipeWithoutAdvancement(recipeOutput), Farmstorm.id("control_terminal"))`（剥进度的助手在 `:174-201`，故合成配方 4 + 1 = 5 而进度仍是 16）。图样 `GIG` / `IRI` / `GIG`：四角 `minecraft:glass`、四边 `minecraft:iron_ingot`、正中 `minecraft:redstone`，材料总数 4 铁 + 4 玻璃 + 1 红石。**图样不能与培养器逐格相同**——培养器是 `IGI` / `GRG` / `IGI`（`:122-124`）、转换器是 `III` / `GRG` / `III`（`:100-102`）；两条 shaped 配方逐格同料同图样时合成台只稳定命中一条。

**⑥ 连带的两条标签与一处映射（D-024、D-039、D-040）**：`minecraft:mineable/pickaxe` 成员 10 → 12（补培养器与终端，`datagen/ModBlockTagsProvider.java:49-63` 现有 10 个成员且培养器不在其中）；新增 `minecraft:needs_stone_tool` 一条合并，成员恰为培养器 / 转换器 / 终端 3 个（照 `:67` 的 `needs_diamond_tool` 写法）；`farmstorm:processing_block` 成员 2 → 3（`:45-47`）。两条标签分工不同：前者管**挖速**，后者管**算不算正确工具**（facts-B §5.1）。`processing_block` 只承担发现（`OutpostCoreBlockEntity.java:891-892`），「是哪一台机器」仍由 `KIND_BY_BLOCK`（`:123-125`，2 → 3 条）单独决定，两处同任务改完（D-024 红线 1）。

### 2.3 改名与删除

改名 **0** 项、删除 **0** 项：四个既有注册类里已注册的 id 一个字不动，两个既有 `Link.Kind` 的线上码不许重排（§5.2）；培养器准入闸里那一句比较是**删除语句**而不是删注册项（D-042）。

### 2.4 检查器同步（`tools/verify_project.py:458-471`，D-052）

| 位置 | 改什么 | 改前 → 改后 |
|---|---|---|
| `:456` 的算式注释 | 末句合计 | 92 → 96，并补「v0.3.0 按 D-020 / D-025 / D-034 / D-047 / D-052 加四类各 +1，把 `data_component_type` 槽位换成 `global_loot_modifier_serializers` ⇒ 92 − 1 + 1 + 4 = 96」 |
| `:459` / `:460` / `:461` / `:470` | 四类值 | items 28 → **29**；blocks 28 → **29**；block_entity_type 8 → **9**；menu_type 1 → **2** |
| `:469` | 槽位替换 | `data_component_type: 1` → `global_loot_modifier_serializers: 1`（键名、值、注释一并改；`data_component_type` 在 1.20.1 全树 0 命中，facts-A §2.1） |
| `:409-417` / `:418-430` / `:431-436` / `:450` | `EXPECTED` 四组 ID 清单 | 各加一项 `control_terminal`（items / blocks / block_entity_type / menu_type） |
| `:448` | 由 `data_component_type` 改名的那组清单 | `temporary_enchantments` → `puffed_hay`（该字面量必须出现在 Java 侧，否则 §2 仍打 FAIL） |

五处**值**改动（上述四类 + 槽位替换）与两处**清单**改动（四组各 +1、GLM 组换成 `puffed_hay`）必须同一个提交改完：只改 Java 自检会让「日志绿、脚本红」的既有错位固化（D-052 否决第 20 项）。检查器**不数** Java 文件（`:479-480` 只把 `*.java` 拼成一个字符串做字面匹配），所以 151 → 159 只出现在文档与 `Get-ChildItem src\main\java -Recurse -Filter *.java` 的计数里；`expected_total = sum(...)`（`:610`）是现算，不动代码，靠 `:456` 与本表对齐到 96。

### 2.5 连带要同步的既有注释（漏了不报错，但会让下一轮读错数）

| 文件:行号 | 现文 | 改成 |
|---|---|---|
| `Farmstorm.java:172-173` / `:191` / `:205` | javadoc「64 → 77 → 90 → 98 的那 12 类」「相加不等于 98」「总数 92 的断言会因此失败」 | 与 `:192` 的 12 类 96 同口径；`:191` 保留「1.20.1 没有 DataComponentType」的事实并补「v0.3.0 加 GLM 后 12 类 96」 |
| `FarmstormItems.java:24` / `FarmstormBlocks.java:23` / `FarmstormClientConfig.java:11` | 「34 个物品」「27 个方块」「166 键」 | 29 个物品（28 + 1）、29 个方块（28 + 1）、3 键（facts-A §5；server 侧 175 另记） |
| `FarmstormNetwork` 的 F-302 注释 | 注释称协议串「应为 3」，字段实际是 `"1"`（facts-C §15 第 3 条） | 「v0.3.0 起为 `"2"`（D-037）」 |
| `ModLootTableProvider.java:35-36` / `ModBlockStateProvider.java:303` | 类注释「27 张方块战利品表 + 15 张作物」与 javadoc「26 个方块」 | 29 张 / 16 张（28 + 1 = 29）、29 个方块（与 `FarmstormBlocks.java:23` 同一组数） |
| `CultivatorBlockEntity.java:136-142` | 「且该配方输出的档案不是史诗档」 | 删掉这半句（D-042） |
| `CropTier.java:24-25` | 「史诗：4 条，唯一的入口是植物学家交易」 | 「史诗：4 条（激活的火把花 / 激活的瓶子草 / 幸运浆果 / 狂野苹果）；v0.3.0 起经 D-042 放行，培养器不再按档位拒绝」 |

---

## §3 资源面

### 3.1 七文件手放表（不经 datagen）

素材包在 `C:\Users\DASHpig\Downloads\control_terminal_a_32x32_pack\assets\farmstorm\textures\block\`，逐字节复制到 `src/main/resources/assets/farmstorm/textures/block/`，**不改名、不重采样**（D-023）：

| # | 目标文件名 | 尺寸 | 字节 | 动画 |
|---|---|---|---|---|
| 1 | `control_terminal_a_front.png` | 32 × 768 | 4191 | 24 帧 |
| 2 | `control_terminal_a_side.png` | 32 × 768 | 3851 | 24 帧 |
| 3 | `control_terminal_a_top.png` | 32 × 768 | 3513 | 24 帧 |
| 4 | `control_terminal_a_bottom.png` | 32 × 32 | 1102 | 静态 |
| 5 | `control_terminal_a_front.png.mcmeta` | —— | 322 | 24 帧 |
| 6 | `control_terminal_a_side.png.mcmeta` | —— | 322 | 24 帧 |
| 7 | `control_terminal_a_top.png.mcmeta` | —— | 322 | 24 帧 |

包内恰好这 7 个文件，没有 `static_32x32/` 之类的备用面；三个字节数按 D-023 的列举顺序对应 front / side / top，复制后逐个比字节数即可确认没有串位。贴图文件名保留 `_a_`（A 版设计号）、注册 id 不带 `_a`（D-020），与既有 `crop_converter_a_front.png` / `cultivator_a_side.png` 同体例。mcmeta **不由 datagen 产出**（`src/generated` 下 mcmeta 计数为 0），这 7 个必须手放。

### 3.2 动画参数（读数口径）

单帧 32 × 32；帧数 **24**（768 ÷ 32 = 24）；每帧停留 2 刻（mcmeta 的 `frametime: 2`）；一轮循环 **48 刻**（24 × 2 = 48）；一轮墙钟 **2.4 秒**（48 ÷ 20 = 2.4，20 TPS）。本仓现有 5 个 mcmeta（`crop_converter_a_{front,side,top}` + `cultivator_a_{side,top}`）对应的 PNG 全是 32×512 = **16 帧**（512 ÷ 32 = 16），其中 3 个是最小形（只写 `frametime`）、2 个写了显式 `frames` 0~15；**24 帧 / 32×768 在本仓无先例**，进游戏数帧是验收 #5 的判据（06 号 §8 第 3 条）。

### 3.3 方块模型 1 张（78 → 79）

产物 `src/generated/resources/assets/farmstorm/models/block/control_terminal.json`，模型 id `farmstorm:block/control_terminal`（**不带 `_a`**），父模型 `minecraft:block/cube`，六面映射为 `north` = `control_terminal_a_front`、`south` / `east` / `west` = `control_terminal_a_side`、`up` = `control_terminal_a_top`、`down` = `control_terminal_a_bottom`、`particle` = `control_terminal_a_front`。生成方式：复用 `datagen/ModBlockStateProvider.java:212-221` 的六面贴图助手（父模型 `mcLoc("block/cube")`、逐面 `texture(...)`、`particle` 取 front），只换模型名。

### 3.4 blockstate 4 variant（28 → 29）

| variant 键 | `y` 旋转 | `uvlock` | `model` |
|---|---|---|---|
| `facing=north` | 不写 `y` 键（等价 0） | `true` | `farmstorm:block/control_terminal` |
| `facing=east` | `90` | `true` | 同上 |
| `facing=south` | `180` | `true` | 同上 |
| `facing=west` | `270` | `true` | 同上 |

恰好 **4 个** variant：`HORIZONTAL_FACING` 只有四值，且不新增工作态属性（D-022）。落点是 `registerStatesAndModels()` 的 `:72-92` 按类分派链：在培养器分支之后插一支 `else if (block instanceof ControlTerminalBlock)`，新方法放在 `:245-258` 的培养器两态之后，方法体三件套为「建模型 → `getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model).rotationY(facingRotationY(state.getValue(FACING))).uvLock(true).build())`」。**不能用** `simpleBlock(block, model)`（只生成 `""` 单态，4 个朝向里 3 个找不到模型）；**不能用** `Direction#toYRot()`（`facingRotationY` 实读为 `:277-284`：`EAST → 90`、`SOUTH → 180`、`WEST → 270`、`default → 0`，`:275` 的注释逐字记着这条禁令）；漏了这一支会落进 `:72-92` 的兜底分支，去找四张不存在的同名贴图，游戏里是紫黑格。

### 3.5 item 模型：一条白名单

`datagen/ModItemModelProvider` 的 `usesBlockModel(Block)` 白名单当前是 `CropConverterBlock || OutpostCoreBlock || CultivatorBlock`，加 `|| block instanceof ControlTerminalBlock`。命中时父模型指向 `farmstorm:block/control_terminal`，物品栏里是缩小的方块模型（§1.2 的 28 + 1 = 29）；漏了会落到 `basicItem(item)`，去找一张不存在的 `textures/item/control_terminal.png`，游戏里是紫黑格。

### 3.6 本版不新增的资源

GUI 贴图 **0**（面板用纯色底 + 选框，不进 `textures/gui`，该目录当前 0 文件，facts-A §3.5）；item 贴图 PNG **0**（走 §3.5 白名单）；静态帧 / 待机面 / 第二个方块模型 **0**（没有工作态属性，不需要 `_still` 副本；三个动画面自己播，与方块状态无关，代码侧 0 行，D-022）。

### 3.7 datagen 与产物核对单

跑一次既有 datagen 入口（本版只加 `ModGlobalLootModifierProvider` 一个 Provider 的注册行），然后逐项读回：

| 数什么 | 目录（`src/generated/resources` 之下） | 应读到 |
|---|---|---|
| 方块模型 | `assets/farmstorm/models/block` | **79** |
| blockstate | `assets/farmstorm/blockstates` | **29** |
| item 模型 | `assets/farmstorm/models/item` | **29** |
| 方块战利品表 | `data/farmstorm/loot_tables/blocks` | **29** |
| 配方 JSON | `data/farmstorm/recipes` | **26**（21 转换 + 5 合成） |
| 进度 | `data/farmstorm/advancements` | **16** |
| 语言键 | `assets/farmstorm/lang/{zh_cn,en_us}.json` 顶层键 | 各 **328** |
| loot modifier | `data/farmstorm/loot_modifiers` 与 `data/forge/loot_modifiers` | **1 + 1 = 2** |

**目录名是复数形**（`recipes` / `loot_tables` / `advancements`，facts-A §3.4）；1.21.1 是单数形，照抄会数到 0。终端自己应新增**恰好 5 个**产物文件：`blockstates/control_terminal.json`、`models/block/control_terminal.json`、`models/item/control_terminal.json`、`loot_tables/blocks/control_terminal.json`、`recipes/control_terminal.json`；七个贴图与 mcmeta **不在** `src/generated` 下（§3.1）。

---

## §4 行为规格

### 4.1 模式三态状态机（含 `OFF`）

`Mode` 是 `ControlTerminalBlockEntity` 的嵌套枚举，恰好三个值（D-030）：

| 状态 | 枚举序 | 是否投料 | 面板表现 | 迁移 |
|---|---|---|---|---|
| `OFF` | 0 | 不投料、不消费核库存 | 两个按钮都不带选中框 | 点任一按钮 → 对应模式 |
| `CULTIVATOR` | 1 | 每 20 刻向 `kind = CULTIVATOR` 的机器投料 | 培养模式按钮带选中框 | 点培养模式 → `OFF`；点转换模式 → `CONVERTER` |
| `CONVERTER` | 2 | 每 20 刻向 `kind = CROP_CONVERTER` 的机器投料 | 转换模式按钮带选中框 | 点转换模式 → `OFF`；点培养模式 → `CULTIVATOR` |

三条不变量：初始态是 `OFF`（D-031）；互斥由枚举保证，不存在「两个都为真」的第四态；再点已选中的那个退回 `OFF`。**编号陷阱**：`Mode` 的序是 `OFF = 0 / CULTIVATOR = 1 / CONVERTER = 2`，而 `Link.Kind` 的线上码是 `CULTIVATOR = 0 / CROP_CONVERTER = 1 / TERMINAL = 2`（`CultivatorLinkPayload.java:73-75`）——两套编号不同轴，任何一处把 `Mode` 的序当 `Kind` 的码用，培养器的线会画成终端的绿线。

#### 4.1.1 终端的两个 NBT 键

| 键 | 类型 | 写点 | 读点 | 缺键语义 |
|---|---|---|---|---|
| `Mode` | `String`（`off` / `cultivator` / `converter`） | `setMode` 之后与 `saveAdditional` | `loadAdditional` | 缺键 ⇒ `OFF`（老存档与刚放下的终端都走这条） |
| `Core` | `long`（`BlockPos#asLong`） | `setCore` / `clearCore` | `loadAdditional` | 缺键 ⇒ 未绑定（`CoreBound` 的可空语义） |

模式存**序列化名**而不是 `ordinal()`：`/data get block` 读出来是 `off` 这种可读值，验收 #7 直接照它做判据。`Core` 键名与培养器逐字相同（`CultivatorBlockEntity.java:338-358` 的 `TAG_CORE` 读写），因为它是同一个 `CoreBound` 契约的一部分；未列进上表的状态一律不存（终端没有进度、没有槽、没有第二份绑定真相）。

### 4.2 右键打开流程（1.20.1 只有 `use(...)` 一个入口）

① `ControlTerminalBlock#getMenuProvider` 返回 `SimpleMenuProvider`，标题键 `container.farmstorm.control_terminal`，容器访问用 `ContainerLevelAccess.create(level, pos)`——照 `OutpostCoreBlock.java:106-110` 逐字同形（facts-B §3.3）。② 覆写 `use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)`：1.20.1 **没有** `useWithoutItem` 拆分（facts-C §4.1 的注、`BlockBehaviour.java:172`），本仓两台机器的 `use` 在 `CultivatorBlock.java:111-129` 与 `CropConverterBlock.java:114-132`。③ 客户端分支：潜行右键返回 `PASS`（把右键交回原版，玩家可以贴着终端摆方块），其余返回 `SUCCESS`，客户端不做归属判定（照 `OutpostCoreBlock.java:126-131` 的取舍）。④ 服务端分支：`player.openMenu(provider)`（`OutpostCoreBlock.java:139`）并返回 `CONSUME`，**不判归属、不判绑定**，右键始终打开（D-028）。⑤ 未绑定时不写聊天栏、不发动作栏提示：状态行由面板自己写「未绑定」，两个模式按钮 `active = false`。⑥ 屏幕注册在 `client/FarmstormClientSetup.java:31-34` 的 `FMLClientSetupEvent` 的 `enqueueWork` 里加一行 `MenuScreens.register(FarmstormMenus.CONTROL_TERMINAL.get(), ControlTerminalScreen::new)`；1.20.1 **没有** `RegisterMenuScreensEvent`（facts-B §3.1 全树 0 命中），漏了这一行客户端只有一条 warn、没有界面。

本条**明确偏离**机器那套「任一闸不过就返回 `PASS`」的口径（`CultivatorBlock.java:116` / `:121` / `:125` 三个出口）：机器是往槽里塞东西，无反应就是拒绝；终端是打开一个界面，无反应等于让玩家拿着新方块学不到任何东西（D-028 的补充）。

### 4.3 放置绑定的三步顺序

照 `block/CultivatorBlock.java:82-98` 逐字同形，顺序不可换：① `super.setPlacedBy(...)`；`level instanceof ServerLevel` 与 `placer instanceof Player` 任一不成立就 return。② `OutpostCoreBlockEntity.selectCore(server, pos, new TeamResolver.Owner(player.getUUID(), player.getScoreboardName()), null)`（`:90-91`），返回 `null` ⇒ 未绑定，**不写 `Core` 键**（`:92-94`）。③ `if (core.addCultivator(pos, Link.Kind.TERMINAL)) { terminal.setCore(core.getBlockPos()); }`（`:95-97`）——核侧先写成功、再写终端侧的引用缓存，反过来会留下「核说没绑、终端说绑了」的假象。没有可用核时方块照样放下、处于未绑定态；核侧 `rebindNearby`（`OutpostCoreBlockEntity.java:608-656`，周期 `:111` 的 `REBIND_INTERVAL_TICKS = 100`）会在 100 刻一轮的扫描里把没人要的终端补进名额，**前提是 `KIND_BY_BLOCK`（`:123-125`）里有终端**，否则候选过滤（`:621-622`）扫不到它。终端**占一个名额**（D-026）：与培养器、转换器共用 `cultivators`（`:147`）与 `cultivatorKinds`（`:154`）两条平行列表，上限读 `outpost_core.cultivatorCapacity`（默认 24，`FarmstormConfig.java:944-946`），复用 `addCultivator`（`:413-426`），不新增方法。

### 4.4 每 20 刻投料循环（服务端）

`ControlTerminalBlock#getTicker` 照 `CultivatorBlock.java:153-162`：客户端返回 `null`，服务端返回 lambda 转调 `ControlTerminalBlockEntity.serverTick(...)`。循环九步：

| 步 | 动作 | 判据 / 依据 |
|---|---|---|
| 1 | `level.isClientSide` ⇒ return | 客户端永不 tick（ticker 客户端返回 `null`） |
| 2 | `level.getGameTime() % FEED_INTERVAL_TICKS != 0` ⇒ return | `FEED_INTERVAL_TICKS = 20` 是 `private static final` 常量（D-032，不是 config 键） |
| 3 | `mode == OFF` ⇒ return | D-031 |
| 4 | `corePos() == null` ⇒ return | `CoreBound` 三方法之一（D-025） |
| 5 | `level.getBlockEntity(corePos())` 不是 `OutpostCoreBlockEntity` ⇒ return | `level.getBlockEntity` 只读已加载区块、不触发加载，取不到正好当闸用 |
| 6 | `!core.hasCultivator(this.getBlockPos())` ⇒ return | 绑定闸，核侧列表是唯一真相（`:396-398`） |
| 7 | `!OutpostCoreBlockEntity.inRange(corePos(), getBlockPos())` ⇒ return | 超 3×3 区块不投料（D-027；`:537-543`） |
| 8 | 目标机器集 = `core.cultivatorKinds()`（`:383-385`）里等于本模式 kind 的下标，按同下标取 `core.cultivators()`（`:369-372`）的坐标 | 两条列表同长同下标（`:147` / `:154`） |
| 9 | 对每台目标机器跑一次「投 1 个」 | 一轮每台最多 1 个（D-032） |

「投 1 个」四步：① `level.getBlockEntity(pos)`，类型必须与模式匹配（`CULTIVATOR` → `CultivatorBlockEntity`，`CONVERTER` → `CropConverterBlockEntity`），否则跳过这台；② 按槽序扫核的 54 格（`SLOT_COUNT = 54` 在 `:67`，列表在 `:139`），逐格取物品；③ 对每格调**机器自己的**准入闸——培养器 `canInsert`（`CultivatorBlockEntity.java:152-154`，内含 `isUsable()` ∧ 空槽 ∧ `admissible`）、转换器 `canInsert`（`CropConverterBlockEntity.java:141-143`，内含 `isUsable()` ∧ 空槽 ∧ `convertible`）；④ 第一个为真的格子：核侧移除恰好 1 个，再 `machine.insert(one)`（`CultivatorBlock.java:127-128` / `CropConverterBlock.java:130-131` 的同一调用）并 `break`，这台本轮结束，扫满 54 格无一格通过就换下一台。

四条边界：**只填空槽**——机器槽非空时本轮一个都不投（`canInsert` 的「空槽」分量，`:152-154` 与 `:141-143` 的 `held.isEmpty()`）；**每台 1 个**——靠上面第 ④ 步的 `break`，不存在继续给同一台塞第二个的路径；**名额满**——`addCultivator` 返回 `false` 时第 6 步直接 return（D-026、`:401-403` 的 `isCultivatorListFull`）；**超范围**——终端第 7 步与机器侧 `isUsable()` 两层都拦（D-027、`:532-534` / `:537-543`）。**准入判据只借不改**（D-033、D-043）：终端不新增物品白名单、不按 `CropProfile` 判作物、不抄一份「史诗例外」。后果必须成文：数据包新增一条转换配方，终端的培养模式自动多收一个投入物，与培养器同步。

### 4.5 面板按钮语义

点未选中的模式发出 `clickMenuButton(player, id)`（`id = 0` 是培养模式、`id = 1` 是转换模式），服务端把模式切到该 id 对应的值并写 NBT + `setChanged`；点已选中的同一个 id，模式退回 `OFF`；未绑定时客户端按钮 `active = false` 点不动，服务端 `clickMenuButton` 仍返回 `false`、模式不变。`clickMenuButton` 的服务端四步（顺序即短路顺序）：① `id` 不在 0~1 ⇒ `false`；② `access.evaluate` 取不到方块实体 ⇒ `false`；③ 未绑定（核侧不认）⇒ `false`；④ 否则按 §4.1 的迁移表改模式并返回 `true`。屏幕侧：两个 `Button.builder` 用模式语言键做标签，`bound == false` 时两个都 `active = false`；选中态画 1 像素绿框 `0xFF4DD94D`（`0.30 × 255 ≈ 77 = 0x4D`、`0.85 × 255 ≈ 217 = 0xD9`，与 §5.2 的绿线同一组分量）；状态行文本 = 绑定状态，`OFF` 之外再拼「 · 」与当前模式名。

### 4.6 `ContainerData` 两格的语义

格 0 是模式序号，服务端读 `mode().ordinal()`（0 / 1 / 2）、客户端读 `menu.mode()`；格 1 是绑定状态，服务端按核侧是否已认定绑定给 1 或 0、客户端读 `menu.bound()`；~~两格的 `set` 都是空实现（只往客户端推，不回写）~~ —— **v0.3.0 收口修正（2026-11-19，实测缺陷）**：这句是错的，且与同段「同步靠 `broadcastChanges` 每刻自动完成」自相矛盾。原版同步链路是 `DataSlot#checkAndClearUpdateFlag` 在**服务端**比对 `get` 后发包、客户端收到后用 `set` **回填**；`set` 空实现等于把服务端发来的值当场丢掉。同时「`get` 必须每次现读」在客户端也不成立：客户端菜单由 `MenuSupplier` 构造、`ContainerLevelAccess` 是 `NULL`，读不到方块实体。**正确形状**：两格用普通 `SimpleContainerData(2)`（不覆写 `get`/`set`），服务端在 `broadcastChanges()` 里把终端真实状态 `set` 进两格，再由原版链路同步（见 `menu/ControlTerminalMenu#broadcastChanges`）。实现面：`new SimpleContainerData(2)`（`SimpleContainerData.java:3`）+ `addDataSlots(...)`（`AbstractContainerMenu.java:114`），两格数量必须与构造参数的 2 相等，不符抛 `IllegalArgumentException`（`checkContainerDataCount`，`:89-93`）；同步靠 `broadcastChanges` 每刻自动完成，**不新增第三个网络包**（D-034、06 号 §4 红线 7）。两格的 `get` 必须每次现读：菜单持 `ContainerLevelAccess`（照 `OutpostCoreMenu.java:57-59`），用 `access.evaluate` 取终端方块实体，取不到时返回 0 与 0。0 槽菜单的三个硬性形状：`quickMoveStack(Player, int)` 直接返回 `ItemStack.EMPTY`（0 槽菜单不会有 `QUICK_MOVE` 点击）、`stillValid(Player)` 走 `stillValid(this.access, player, FarmstormBlocks.CONTROL_TERMINAL.get())`（照 `OutpostCoreMenu.java:133-135`，不新设距离常量）、构造器两个重载照 `:52-59`（`(int id, Inventory)` 给 `MenuSupplier`，转调 `(int id, Inventory, ContainerLevelAccess)`，后者用 `ContainerLevelAccess.NULL` 兜底）。**`getType()` 陷阱**：菜单必须用真实 `MenuType` 走 `super(type, id)`，写成 `super(null, id)` 会让 `getType()` 抛 `UnsupportedOperationException`（`AbstractContainerMenu.java:74-79`），而注册与开屏链路都要读它（`MenuType.java:54-56`）。

### 4.7 位置记录的生命周期（记录 / 登记 / 消费）

**记录本体**（`util/PuffedHayData`，D-045）：

| 项 | 值 |
|---|---|
| 基类与先例 | `extends SavedData`，照 `outpost/OutpostCoreIndex.java:76-79` 与 `trap/TrapFifoIndex.java:59-60` 的同一形态 |
| 落盘名 | `farmstorm_puffed_hay` → `world/data/farmstorm_puffed_hay.dat`（`DimensionDataStorage.java:34-36`） |
| 取实例 | `server.overworld().getDataStorage().computeIfAbsent(PuffedHayData::load, PuffedHayData::new, "farmstorm_puffed_hay")`——**三参** `Function` / `Supplier` / `String`（`DimensionDataStorage.java:38`、`ServerLevel.java:1148-1149`） |
| 内部结构与 NBT | `LongOpenHashSet`，元素 = `BlockPos.asLong()`；NBT 键 `positions`，类型 `long[]` |
| 落盘签名与写脏 | `public CompoundTag save(CompoundTag tag)`——**只有一个参数**（`SavedData.java:15`）；每次加 / 删之后 `setDirty()`（`SavedData.java:17-19`） |

**登记**（`entity/thrown/PuffedWheatProjectile.java:104-123`，D-046）：登记点写在 `:119` 的 `level.setBlock(pos, hay, Block.UPDATE_ALL)` **之后**，且登记前必须再过一道 `hay.is(Blocks.HAY_BLOCK)`。`:113-115` 是抗性跳过、`:116-118` 是白名单跳过，被这两条挡掉的格子不记；不加那道守卫的话压缩膨胀麦会一起被登记——`CompressedPuffedWheatProjectile.java:34-37` 只覆写 `fillBlock()` 就复用了同一个 `fillHay`。**已知影响**：老存档里已有的膨胀麦干草块不在记录里，照旧掉落，本版不回溯扫描（验收 #18）。

**消费**（`loot/PuffedHayLootModifier`，D-047 / D-048）：① `extends LootModifier`，只实现 `doApply`（`LootModifier.java:68-69`；`apply` 是 `final`，先在 `:54-59` 判条件、通过才调 `doApply`）。② 限定表靠**条件**而不是代码分支：一条 `forge:loot_table_id`（`net.minecraftforge.common.loot.LootTableIdCondition`，条件类型注册在 `ForgeMod.java:663`、JSON 字段 `loot_table_id`）指向 `minecraft:blocks/hay_block`，条件不通过时 `apply` 原样返回，别的表一个字节不受影响。这类条件是 Forge 专为 GLM 提供的，因此「只挂一张表」在 1.20.1 可以字面成立。③ `doApply` 里 `BlockPos.containing(context.getParam(LootContextParams.ORIGIN))`——**1.20.1 的 `ORIGIN` 是 `Vec3`**（`LootContextParams.java:18`）；`ServerLevel` 走 `context.getLevel()`（`LootContext.java:75`）。④ 命中记录 ⇒ `remove(pos)` + `setDirty()` 并返回**空表** `new ObjectArrayList<>()`；未命中 ⇒ 原样返回 `generatedLoot`。返回值是整表替换（`ForgeHooks.java:1196-1198`），所以空表 = 本次零掉落，方块照旧被移除、经验照旧结算（D-048）。⑤ 两份数据文件由 `ModGlobalLootModifierProvider` 产出：`data/farmstorm/loot_modifiers/puffed_hay.json`（本体，含 `conditions` 字段——`LootModifier.java:39-42` 的 `fieldOf("conditions")`）与清单 `data/forge/loot_modifiers/global_loot_modifiers.json`（命名空间固定 `forge`，`LootModifierManager.java:53`）。**移除**：只有消费点这一条路径会删记录；方块被活塞推走、被爆炸炸掉、被 `/setblock` 覆盖时记录留着。表 id 条件保证不会误伤别的表，代价是记录集合只增不减（§10 的 RT-018）。

### 4.8 八个新类的最小公开面

写代码时按这张表对齐签名，多出来的成员一律不加。

| 类 | 最小公开面 | 先例 |
|---|---|---|
| `ControlTerminalBlock` | `public static final DirectionProperty FACING`；`getRenderShape` 返回 `MODEL`；`createBlockStateDefinition` 只加 `FACING`；`newBlockEntity`；`setPlacedBy`；`use`；`getMenuProvider`；`getTicker` | `CultivatorBlock.java:82-98` / `:111-129` / `:153-162`；`CropConverterBlock.java:44` / `:114-132` |
| `ControlTerminalBlockEntity` | 嵌套 `enum Mode`（三值 + 序列化名读写）；`corePos` / `setCore` / `clearCore`；`mode()` / `setMode(Mode)`；`FEED_INTERVAL_TICKS = 20`；`static void serverTick(...)`；`createData()` 返回 2 格；`saveAdditional` / `load` | `CultivatorBlockEntity.java:44` / `:152-154` / `:338-358` |
| `ControlTerminalMenu` | 两个构造器；`quickMoveStack`；`clickMenuButton`；`stillValid`；`mode()` / `bound()` | `OutpostCoreMenu.java:52-59` / `:133-135` |
| `ControlTerminalScreen` | `init()`（两个按钮 + 按绑定量置 `active`）；`renderBg`（纯色底）；`render`（选中框 + 状态行） | `OutpostCoreScreen` 的渲染时序 + `FarmstormClientSetup.java:31-34` 的注册 |
| `PuffedHayData` | `static PuffedHayData get(MinecraftServer)`；`boolean contains(BlockPos)`；`void add(BlockPos)`；`boolean remove(BlockPos)`；`CompoundTag save(CompoundTag)`；`static PuffedHayData load(CompoundTag)` | `OutpostCoreIndex.java:76-79`、`TrapFifoIndex.java:59-60` |
| `PuffedHayLootModifier` | 静态 `CODEC`；`protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack>, LootContext)`；构造器接 `LootItemCondition[]` | `IGlobalLootModifier.java:78-84`、`LootModifier.java:48` / `:68-69` |
| `FarmstormLootModifiers` | `public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTER`；`public static final RegistryObject<Codec<PuffedHayLootModifier>> PUFFED_HAY` | `FarmstormBlockEntities.java:21-22` 的注册器形态 |
| `ModGlobalLootModifierProvider` | 构造器 `(PackOutput, String)`；`protected void start()` | `GlobalLootModifierProvider.java:43` / `:60` / `:95-99` |

---

## §5 协议

### 5.1 为什么不新增第三个 payload

面板只有两格状态，`ContainerData` 由菜单的 `broadcastChanges` 自动同步，不需要自定义包（D-034 否决「新增第三个 S2C 包」）；绿色连线不是新数据，既有 `CultivatorLinkPayload` 的第三段就是一个 `BYTE`，多一个枚举值即可表达（D-035）；06 号 §4 红线 7 明写「不得新增第三个网络包」，新增一个包还要再动一次协议串，代价与收益不成比例。因此网络包 **2 + 0 = 2**，`FarmstormNetwork.java:51-58` 与 `:60-67` 两条登记一行不动。

### 5.2 `Link.Kind` 追加上线码 `TERMINAL(2)`

| 值 | 线上码 | 含义 | 颜色（R / G / B） | 十六进制 |
|---|---|---|---|---|
| `CULTIVATOR` | 0 | 农作物培养器 | `0.30 / 0.85 / 0.90` | 既有，`CultivatorLinkRenderer.java:51-53`，一个字节不动 |
| `CROP_CONVERTER` | 1 | 农作物转换器 | `0.92 / 0.22 / 0.22` | 既有，`:59-61`，一个字节不动 |
| `TERMINAL` | **2** | 控制终端 | `0.30 / 0.85 / 0.30` | `0x4D / 0xD9 / 0x4D` |

枚举声明在 `network/CultivatorLinkPayload.java:73-75`，追加 `TERMINAL(2)`；`id()` 在 `:84-86`、`byId` 在 `:89-96`（未知码回落 `CULTIVATOR`，前向兼容兜底）。渲染侧在 `client/CultivatorLinkRenderer.java` 加三个绿分量常量，并在 `:100-109` 的 `switch (link.kind())` 里加 `TERMINAL` 分支；`ALPHA = 1.0`（`:63`）与端点小框（`:110-114`）共用同一组分量。该 `switch` **无 `default`**：枚举多一个值而分支没补就编译不过——这是本版唯一一处「漏改立刻报错」的地方，其余漏改都是静默的。

### 5.3 payload 字段与字节

`CultivatorLinkPayload` 结构一个字节都不变（`network/CultivatorLinkPayload.java:27` 的 `record CultivatorLinkPayload(List<Link> links)`）：

| 段 | 字段 | 编码 | 本版变化 |
|---|---|---|---|
| 整包 | `links` | `writeVarInt(links.size())`，逐条 `encode`（`:38-43`） | 不变 |
| 解码上限 | `MAX_LINKS = 1024` | 解码时 `Math.min(buffer.readVarInt(), MAX_LINKS)`（`:32` / `:46-53`） | 不变 |
| 单条 1 / 2 | `core` / `cultivator` | `FriendlyByteBuf` 的 `BlockPos` 读写，实现不手写字节 | 不变 |
| 单条 3 | `kind` | `writeByte(kind.id())`（`:62`）、读侧 `Kind.byId(readByte())` | **恒 1 字节**，加一个枚举值不改字段宽度 |

默认配置下每玩家 4 核 × 24 名额 = 96 条，远小于 1024。终端**占名额**（D-026），因此它也出现在这条 payload 里、也画线；线上的 `cultivator` 那一端就是终端坐标，渲染层不问它是不是机器（`CultivatorLinkRenderer.java:100-109` 只按 kind 分支）。

### 5.4 协议串 `"1"` → `"2"`

改 `network/FarmstormNetwork.java:32` 的字面量，并在 `:29-30` 的沿革注释补一行「v0.3.0：`Link.Kind` 多一枚举值（`TERMINAL = 2`）⇒ `"1"` → `"2"`（D-035 / D-037）」。`FarmstormNetwork` 的既有两行自检日志（格式串为 `自检 网络包注册 {}（版本 {}，S2C 只读，MAX_CORES / MAX_LINKS {}）→ OK`）会把版本串打出来，改完启动一次即可读到「版本 2」两行（验收 #20）。跨版本行为：旧客户端收到 `id = 2` 时 `byId` 回落 `CULTIVATOR`（`:89-96`），画成青线而不是崩——这是兜底、不是验收对象；真正的屏障是信道版本串两端精确匹配（`:38-43`），握手阶段就拒掉跨版本联机。

### 5.5 广播时点（终端侧代码 0 行）

连线是绑定的**观察面**，时点全部由既有代码发起：**绑定变化**走核侧 `addCultivator`（`OutpostCoreBlockEntity.java:413-426`，成功后 `:424` 的 `requestAll`）；**绑定摘除**走 `removeCultivator` / `pruneCultivators`（`:429-447` / `:450-469`）；**玩家登录 / 换维度 / 重生**走 `FarmstormGameEvents` 的第 ②④⑤ 个监听（`:100-112` / `:127-132` / `:135-140`）——三者读的都是核的 `cultivators` / `cultivatorKinds`。终端只要在 §4.3 第 3 步调一次 `core.addCultivator(pos, Link.Kind.TERMINAL)`，绿线自己就出现，终端侧不需要任何一处广播代码（`CultivatorLinkBroadcaster` 一行不改）。

---

## §6 配置

本版新增 **0** 键、删除 **0** 键、改默认值 **0** 键（D-032、06 号 §4 红线 5）：server config 175 + 0 = **175**，client config 3 + 0 = **3**（D-018），175 的构成是 167 + 5 + 3 个 define（facts-A §5）。

| 键名 | 默认值 | 范围 | 描述 | 本版动作 |
|---|---|---|---|---|
| ——（0 个） | —— | —— | —— | 不增、不删、不改 |

终端节拍写成 `private static final int FEED_INTERVAL_TICKS = 20;`，与 `OutpostCoreBlockEntity.java:111` 的 `REBIND_INTERVAL_TICKS = 100` 同一体例：编译期常量，不存在「配成 0」的可能。本版要读、但不新建的既有键（照旧现读、不缓存）：`outpost_core.cultivatorCapacity`（`FarmstormConfig.java:944-946`，默认 24）、`cultivator.yield`（`:955-957`，默认 2）、`puffed_wheat.dryEdge` / `puffed_wheat.wetEdge`（`:350-355`）。**老 toml 纪律**：新增 0 键意味着老存档的 `run/config/farmstorm-server.toml`（175 键）与 `farmstorm-client.toml`（3 键）逐键读回原值、**不补写、不重排**；Forge 只在「文件缺键」时补写缺的那几个，键集不变就不会发生补写，本版连默认值都没改，因此老文件一个字节都不会变（仓内 `run/config/` 目录当前不存在，落盘键数读不到，facts-A §7 有登记）。

---

## §7 语言键

总数 321 + 7 = **328**；zh_cn 与 en_us 键集逐字一致，因为两种语言从同一张表按列写盘（`ModLangProvider.java:63-68`）。

### 7.1 删（0 键）

本版删 0 键：没有任何既有键被移除或搬位置，`man瓜` 相关键也在内（D-050）。

### 7.2 加（7 键，逐键列全）

| # | 键名 | zh_cn | en_us | 出处 | 插入位置 |
|---|---|---|---|---|---|
| 1 | `item.farmstorm.control_terminal` | 控制终端 | Control Terminal | D-020 | item 段内，紧接 `:80` 那条 `item(entries, "pumpkin_turret", …)` 之后 |
| 2 | `block.farmstorm.control_terminal` | 控制终端 | Control Terminal | D-020 | block 段内，紧接 `:114` / `:123` 的 `block(entries, "pumpkin_turret", …)` 之后 |
| 3 | `container.farmstorm.control_terminal` | 控制终端 | Control Terminal | D-034 | 其余键段（`plain(...)` 助手）里，与既有 container 键相邻 |
| 4 | `gui.farmstorm.control_terminal.mode.cultivator` | 培养模式 | Cultivate | D-031 | 第 3 条之下 |
| 5 | `gui.farmstorm.control_terminal.mode.converter` | 转换模式 | Convert | D-031 | 第 4 条之下 |
| 6 | `gui.farmstorm.control_terminal.status.bound` | 已绑定 | Bound | D-028 | 第 5 条之下 |
| 7 | `gui.farmstorm.control_terminal.status.unbound` | 未绑定 | Not Bound | D-028 | 第 6 条之下 |

第 1、2 条走 `item(...)` / `block(...)` 助手，第 3 ~ 7 条走 `plain(...)`（键名本身完整）。第 3 条是容器标题，必须与 §4.2 第 1 步的 `Component.translatable` 键名逐字一致；第 4、5 条是两个按钮的标签；第 6、7 条是状态行的两半。`man瓜` 五处（`ModLangProvider.java:80` / `:114` / `:123` / `:199` / `:213`）一个字都不动（D-050）。

### 7.3 改名（0 键）

本版改名 0 键：键名体例照 §7.6，不出现新旧键并存或迁移。

### 7.4 改值（0 键）

本版改值 0 键：既有 321 键的中英文取值逐字不变。

### 7.5 表头口径同步（`ModLangProvider.java:10-16`）

`:10` 的「**321** 键」改成「**328** 键（v0.3.0 口径，D-018）」，`:14-16` 的按组拆解改成下面这段（逐字）：

```
 * **29** item. + **29** block. + **13** entity.farmstorm. + **32** advancements.
 * + **178** farmstorm.configuration.（= 175 server + 3 client）+ **47** 其余 = **328**（其余 42 → 47：container 1 → 2、gui 1 → 5）
```

算式核对：29 + 29 + 13 + 32 + 178 + 47 = **328** = 321 + 7。两组增减写清，否则下一轮读不出差在哪：`container` 1 → 2（+1：面板标题）、`gui` 1 → 5（+4：两条模式标签 + 两条状态行）。沿革段只**追加**一行「v0.3.0 再加 7 条（item 1 / block 1 / container 1 / gui 4），到 328 键」，既有各站的数字一个都不改。

### 7.6 键名体例对照

| 前缀族 | 键形状 | 既有例 | 本版新增 |
|---|---|---|---|
| item | `item.farmstorm.<id>` | `item.farmstorm.pumpkin_turret`（`:80`） | §7.2 第 1 条 |
| block | `block.farmstorm.<id>` | `block.farmstorm.pumpkin_turret`（`:114` / `:123`） | §7.2 第 2 条 |
| container | `container.farmstorm.<id>` | 既有 container 族（1 → 2） | §7.2 第 3 条 |
| gui | `gui.farmstorm.<屏>.<元素>` | 既有 gui 族（1 → 5） | §7.2 第 4 ~ 7 条 |

四条体例纪律：键名一个字都不含 `_a`（注册 id 的规则同样适用于键名）；模式与状态放进 `gui.` 族而不是新开一族（否则 42 → 47 拆不出「container +1、gui +4」）；状态行拼装时 `container` 与 `gui` 两族不重复出词；effect / subtitles / death / tooltip / key / command 六族本版 0 条（既有例 `death.attack.farmstorm.void_bolt.player` 在 `:199`、advancement 段在 `:213`）；`man瓜` 五处不动（D-050）。

---

## §8 验收标准 #1 ~ #32

每条三列：怎么做 / 期望看到什么 / 依据（另加最左的编号列）。#19 是唯一一条「只记录不断言」的条目。

| # | 怎么做 | 期望看到什么 | 依据 |
|---|---|---|---|
| 1 | `python -X utf8 tools\verify_project.py` | 注册面读 **96**，12 类小计与 §1.1 目标列逐项相等 | D-018、D-052；`tools/verify_project.py:458-471` |
| 2 | 同一条命令的收尾统计 | **FAIL 0**；WARN 不超过 1（基线那条 `data_component_type` FAIL 由 F-042 与 W1 同批清掉） | D-052；facts-A §0 第 1、2 条 |
| 3 | `ConvertFrom-Json` 读两份语言文件，各取 `PSObject.Properties` 元素个数 | 两份都读到 **328**，且键集逐字相同 | D-018；`ModLangProvider.java:14-16` / `:63-68` |
| 4 | 数 `blockstates/control_terminal.json` 里 `variants` 的键 | 恰好 **4** 个：`facing=north` / `east` / `south` / `west`，`y` 分别为不写 / 90 / 180 / 270 | D-022；§3.4 |
| 5 | 进游戏对着一台终端连看一轮，同时核三张动画 PNG 的尺寸 | 三面各 **24 帧**（每帧 32×32）、2 刻/帧、一轮 48 刻 = 2.4 秒；PNG 高 **768** = 24 × 32 | D-023；§3.2；06 号 §8 第 3 条 |
| 6 | 空手右键一台已绑定的终端 | 控制面板打开，标题「控制终端」，槽位 **0** 个 | D-028、D-034；§4.2 |
| 7 | 打开面板后 `/data get block <终端坐标> Mode`，再分别点两个按钮 | 依次读到 `off` → `cultivator` → `off`（再点同一个）→ `converter` | D-030、D-031；§4.1 |
| 8 | 两台机器中间放一台终端，切培养模式，按世界刻记录每次投料的 `gameTime` | 相邻两次投料的 `gameTime` 差恒为 **20** | D-032；§4.4 第 2 步 |
| 9 | 核里放合法投入物，盯培养器槽 | 一轮核 64 → **63**、机器槽变 1 个；槽非空后 200 刻内核库存 **0 变化** | D-032、D-033；`CultivatorBlockEntity.java:152-154` |
| 10 | 造满 24 名额（`outpost_core.cultivatorCapacity` 默认 24）后放第 25 台终端 | 不绑定；面板开得起来、状态行「未绑定」、两个按钮置灰；核库存 **0 变化** | D-026、D-028；`:401-403` |
| 11 | 把已绑定的终端移出 3×3 区块 | 该终端不再投料；核库存 **0 变化** | D-027；`:537-543` |
| 12 | 终端与核都绑上，截图看连线 | 一条绿线，像素分量落在 `0x4D / 0xD9 / 0x4D` 附近（`0.30 / 0.85 / 0.30 × 255`）；同屏里青线与红线不变 | D-036、D-038；`:51-53` / `:59-61` |
| 13 | 用木镐破坏一台培养器，数掉落物 | 掉落 **0** 个；**方块仍被移除**（判据是掉落计数，不是「破坏不了」） | D-039、D-040；`ServerPlayerGameMode.java:249` / `:253` / `:255-257` |
| 14 | 用石镐破坏同一块 | 掉落 **1** 个培养器 | D-039、D-040；`DiggerItem.java:68-75` |
| 15 | 依次把火把花 / 瓶子草 / 甜浆果 / 金苹果右键放进培养器 | 4 件各成功投入 **1 次**，一个都不被拒 | D-042、D-043；`:143-149`、`CropProfile.java:73-75` / `:76-78` / `:88-89` / `:102-105` |
| 16 | 让膨胀麦落在草地上记下落点，用石镐挖其中一格 | 掉落 **0** 个；该坐标已从 `farmstorm_puffed_hay` 集合移除（记录 **−1**） | D-046 ~ D-048；§4.7 |
| 17 | 让压缩膨胀麦落地，用石镐挖其中一格 | 照旧掉落 **1** 个压缩干草块（记录集合里没有这些坐标） | D-046；`CompressedPuffedWheatProjectile.java:34-37` |
| 18 | 拿一份 v0.2.8 老存档（已有膨胀麦落地的干草块）直接挖那一格 | 照旧掉落 **1** 个干草块（老坐标不在集合里，不回溯扫描） | D-046 的已知影响 |
| 19 | 用金镐破坏一台培养器与一台转换器，**记录**掉落物数量与工具 | 只记录读数，**不写成断言**；本条不参与 PASS / FAIL 判定 | D-041；`TierSortingRegistry.java:123-134` 的真值本仓读不出 |
| 20 | 读启动日志里两行 `自检 网络包注册 …` | 两行都带「版本 **2**」 | D-037；`FarmstormNetwork.java:32` |
| 21 | `(Get-ChildItem src\main\java -Recurse -Filter *.java).Count` | 读数 **159** | D-018；§2.1 |
| 22 | 数四类 JSON：`models/block` / `loot_tables` / `recipes` / `blockstates` | **79** / **29** / **26**（21 转换 + 5 合成）/ **29** | D-018；§1.2、§1.3、§3.7 |
| 23 | 数 `data/farmstorm/advancements` 与 `ModRecipeProvider` 的 `oneWay` 调用 | 进度 **16** 个 JSON（不变）、`converting` 配方 **21** 条（不变） | D-018；`ModRecipeProvider.java:57-83` |
| 24 | 读 `CultivatorLinkPayload.java:73-75` 的枚举与 `:89-96` 的 `byId` | `CULTIVATOR(0)` / `CROP_CONVERTER(1)` 不变，`TERMINAL(2)` 只能追加；`byId(2)` 返回 `TERMINAL` | D-035；06 号 §4 红线 2 |
| 25 | 放一台终端，读 `/data get block <核坐标> Cultivators` 与 `CultivatorKinds` | 两条列表同长同下标，末位是终端坐标与 `terminal` | D-024、D-026；`:147` / `:154` / `:369-385` |
| 26 | 未绑定状态右键终端，再绑上重开 | 第一次开得起来、两个按钮点不动、库存不变；绑定后状态行变「已绑定」、按钮可点 | D-028；§4.2、§4.5 |
| 27 | 对 blockstate JSON 搜 `working`，再对 `ControlTerminalBlock` 搜 `WORKING` | 两处都 **0 命中** | D-022；§3.4 |
| 28 | 跑一次 datagen 后列 `control_terminal` 的全部产物 | 恰好 **5** 个文件（§3.7 的五条路径） | §1.3、§3.7 |
| 29 | `-Filter *.png` 数一次、`-Filter *.mcmeta` 数一次，再相加 | `.png` **88**（84 + 4）、`.mcmeta` **8**（5 + 3）、两者相加 **96** 个文件（89 + 7）；三个数不许互借 | D-018；§1.2、§3.1 |
| 30 | 读 `data/forge/loot_modifiers/global_loot_modifiers.json` | `entries` 里有 `farmstorm:puffed_hay`（清单里没有 = 修饰器静默不生效） | D-049；`LootModifierManager.java:53` / `:75` |
| 31 | 在 `FarmstormClientSetup` 搜 `MenuScreens.register` | 命中 **1** 处，且写在 `FMLClientSetupEvent` 的 `enqueueWork` 里 | D-034；`FarmstormClientSetup.java:31-34` |
| 32 | 挖掉一格登记过的膨胀麦干草块后重启服务器，再读 `world/data/farmstorm_puffed_hay.dat` | 文件落盘，且跨重启的记录条数与关服前一致 | D-045；`DimensionDataStorage.java:34-38` |

---

## §9 1.20.1 等价实现专节

八处机制差异逐条给「1.21.1 用什么 / 1.20.1 有没有 / 换成什么 / 代价与陷阱」；本节每一条都是**实现面必须不同**，不是措辞差异（D-019、06 号 §7）。

| # | 1.21.1 用什么 | 1.20.1 有没有 | 换成什么 | 代价与陷阱 |
|---|---|---|---|---|
| 1 | `BlockDropsEvent` + `getDrops().clear()` | **没有**：`BlockEvent` 的 12 个嵌套类里没有掉落事件，全树 grep 0 命中（facts-B §1） | 限定 `minecraft:blocks/hay_block` 的 GLM：`extends LootModifier`，`doApply` 命中记录返回空表（D-047 / D-048） | 代价：注册面 +1 类、Java +2 文件、数据文件 +2 份。陷阱：清单漏登记即静默不生效（RT-011）；`apply` 是 final，条件不通过不过 `doApply`（`LootModifier.java:54-59`） |
| 2 | `RegisterMenuScreensEvent` | **没有**：全树 grep 0 命中（facts-B §3.1） | `FMLClientSetupEvent` 的 `enqueueWork` 里调 `MenuScreens.register`（`FarmstormClientSetup.java:31-34`；签名见 `MenuScreens.java:65`） | 代价：注册必须在 `enqueueWork` 里做（类加载时序），整类带 `Dist.CLIENT`；陷阱：漏了只有一条 warn、没有界面（RT-014） |
| 3 | `CustomPacketPayload` + `StreamCodec` | **没有**：改用 `SimpleChannel` + 手写 `FriendlyByteBuf`（`CultivatorLinkPayload.java:38-43` / `:46-53`） | 既有 payload 只多一枚举值，`kind` 恒 1 字节（`:62`）；面板状态走 `ContainerData` | 代价：编解码自己写、解码上限自己设（`MAX_LINKS = 1024`）；陷阱：漏 `Math.min` 时恶意条数撑爆缓冲区 |
| 4 | `SavedData.Factory` + `save(CompoundTag, HolderLookup.Provider)` | **有但是旧签名**：`save(CompoundTag)`（`SavedData.java:15`）；`computeIfAbsent(Function, Supplier, String)` 三参（`DimensionDataStorage.java:38`） | `computeIfAbsent(PuffedHayData::load, PuffedHayData::new, "farmstorm_puffed_hay")`，照 `OutpostCoreIndex.java:76-79` | 代价：两处签名都照 1.20.1 写；陷阱：三参顺序写反编译错，把工厂当第一参则运行期 `ClassCastException`（RT-013） |
| 5 | 不涉及（没有 `ORIGIN` 的类型问题） | **有**：`LootContextParams.ORIGIN` 是 `LootContextParam<Vec3>`（`LootContextParams.java:18`） | `BlockPos.containing(context.getParam(LootContextParams.ORIGIN))`；`ServerLevel` 走 `context.getLevel()`（`LootContext.java:75`） | 代价：新增一次类型转换；陷阱：把 `Vec3` 当 `BlockPos` 用编译不过，取记录前先判 `hasParam`（`LootContext.java:34` / `:47-48`）（RT-012） |
| 6 | 菜单构造可带附加数据（`IMenuTypeExtension` 一类） | **没有**：`MenuType` 只有 `(MenuSupplier, FeatureFlagSet)` 一个构造（`MenuType.java:49-52`），`IMenuTypeExtension` 全树 0 命中 | 走 `player.openMenu(provider)`（`OutpostCoreBlock.java:139`），位置由 `ContainerLevelAccess` 在菜单里解析 | 代价：不能通过 buf 传坐标；陷阱：菜单用 `super(null, id)` 时 `getType()` 直接抛（`AbstractContainerMenu.java:74-79`）（RT-015） |
| 7 | `ContainerData` + `addDataSlots`（项目已有先例） | **有 API、本仓 0 先例**：`ContainerData.java:3-8`、`SimpleContainerData.java:3`、`addDataSlots` 在 `AbstractContainerMenu.java:114` | `new SimpleContainerData(2)` + `addDataSlots(...)`，两格 = 模式序号 + 绑定状态 | 代价：无先例可抄，两格数量必须与构造参数一致；陷阱：数量不符抛 `IllegalArgumentException`（`:89-93`），`set` 侧留空实现 |
| 8 | 方块类可覆写 `getShape` | **可覆写但本仓不用**：培养器与转换器都没覆写（facts-C §4.3），全仓只有两个作物类覆写 | 终端也不覆写，形状随方块本体 | 代价：没有碰撞箱微调；陷阱：照 1.21.1 写 `getShape` 会让终端与两台机器判定形状不同形，等于自造第三条口径 |

两条配套口径（不占八处，但同一份差异表要写清）：**事件监听 9 + 0 = 9**——1.21.1 是 9 → 10（加 `BlockDropsEvent` 监听），本版拦截面换成 GLM，`event/FarmstormGameEvents.java:67-79` 的九行 `addListener` 一行不动（06 号 §7 第 8 行）；**挖掘判定链换符号**——1.21.1 走 `Tool.isCorrectForDrops`（组件体系），1.20.1 走 `ForgeHooks.isCorrectToolForDrops`（`ForgeHooks.java:226-232`）→ `DiggerItem`（`:80-82`）→ `TierSortingRegistry`（`:123-134`），无 `DataComponents`（facts-B §5.2）。

---

## §10 风险 RT-011 ~ RT-020

| 风险 | 影响 | 缓解 | 兜底任务 |
|---|---|---|---|
| **RT-011** · GLM 清单漏登记 | 修饰器静默不生效：干草块照旧掉 1 个，没有日志、没有编译错 | 清单由 `ModGlobalLootModifierProvider` 产出（不手写），验收 #30 直接读 `entries`；两份文件同任务交付 | F-039、F-045 |
| **RT-012** · `ORIGIN` 是 `Vec3` | 写成 `BlockPos` 编译不过；宽松写法会把坐标截断成错的格 | `BlockPos.containing(...)` 显式转换，取参前判 `hasParam`；验收 #16 用「记录 −1」当判据 | F-038 |
| **RT-013** · `computeIfAbsent` 三参顺序 | 顺序写反编译错；把工厂对象当第一参则运行期 `ClassCastException`，只在第一次写记录时炸 | 照 `OutpostCoreIndex.java:76-79` 的形状逐字抄，`load` / `new` 两个方法引用写在同行便于比对 | F-036 |
| **RT-014** · `MenuScreens` 时序与客户端专属 | 注册写在构造器或服务端事件里 ⇒ 客户端一条 warn、没有界面；类没带 `Dist.CLIENT` ⇒ 服务端崩 | 照 `FarmstormClientSetup.java:31-34`：`enqueueWork` + `Dist.CLIENT` 注解；验收 #31 核命中 1 处在事件内 | F-031 |
| **RT-015** · 0 槽菜单的 `getType()` 陷阱 | `super(null, id)` ⇒ 开屏链路上抛 `UnsupportedOperationException`；0 槽菜单最容易图省事省掉 `MenuType` | 菜单一律 `super(type, id)` 且经 `MenuType.create` 创建，`SimpleContainerData(2)` 与 `addDataSlots` 数量对齐 | F-030 |
| **RT-016** · 24 帧 / 32×768 动画无先例 | 照抄项目内 16 帧的 mcmeta 口径 ⇒ 只播前 16 帧；PNG 被重采样 ⇒ 帧条拉伸 | mcmeta 用素材包那份（显式 `frames` 0~23），PNG 逐字节复制不重编码；验收 #5 进游戏数帧 | F-023 |
| **RT-017** · 压缩膨胀麦共用 `fillHay` 被误登记 | 压缩干草块被挖时也不掉落，直接违反 D-046 与验收 #17 | 登记前加 `hay.is(Blocks.HAY_BLOCK)`；验收 #17 盯「照旧掉 1 个」 | F-037 |
| **RT-018** · 记录集合只增不减 | 活塞推走 / 爆炸 / `/setblock` 都不走消费点 ⇒ 永不消费的坐标堆在 `.dat` 里，存档缓慢变大 | 消费点唯一且带表 id 条件，误伤不可能；记录量级与膨胀麦落点同阶，不额外清理；验收 #32 只核条数跨重启一致 | F-037、F-045 |
| **RT-019** · 检查器既有错位（12 类 92 vs 11 类 91） | 只改 Java 自检 ⇒ 日志绿、脚本红；只改脚本 ⇒ 两条口径继续分叉 | `EXPECTED_COUNTS` 的槽位替换与四类 +1 同一提交改完；验收 #1 与 #2 同批复核 | F-042 |
| **RT-020** · 金镐在 `TierSortingRegistry` 的真值本仓读不出 | 把「金镐挖不动」写成断言 ⇒ 与 `incorrect_for_gold_tool` 的真值相反时必红 | 按 D-041 只记录不断言（验收 #19 不参与 PASS / FAIL），机理链留在 §9 的配套口径里 | 验收 #19（只记录） |

---

> 本文自检：注册面 12 类逐项相加 = 96（91 + 5）；验收 #1 ~ #32 共 32 条；风险 RT-011 ~ RT-020 共 10 条；1.20.1 等价实现 8 处；本轮禁用的八个词全篇 0 命中（敬语、占位标记、主观评价三类均无）；每张表首尾有竖线且逐行列数一致。
