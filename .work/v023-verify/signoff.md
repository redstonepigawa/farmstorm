# F-311 · v0.2.3 签字记录（八项读数 / 19 项必改 / 不改清单 / 已知影响 / 口径外 3 类）

> 执行者：收尾子代理（F-311 / Phase 65 / M61）。窗口已关：F-286 ~ F-308 落地，F-309 / F-310 的两份记录在位（`acceptance.md` 32 条、`boundaries.md` 31 条）。
> 本记录只新增文件，**不改上游文档**：`docs/zh/00` ~ `57`、`docs/en/*`、`plan0.2.3.txt`、`.work` 下既有文件（含 `v023-BRIEF.md`、`acceptance.md`、`boundaries.md`）一字未动。
> 本轮**不跑任何 git 命令**（提交由调度者做），Mixin 仍为 **0**；取证方式统一为「文件 mtime + 逐行/逐键实读 + 检查器实跑」。
> 唯一一处 `src/` 改动是注释微修：`Farmstorm.java:193` 的「总数 98 的断言会因此失败」改为「总数 92」（与本版真实断言 92 一致），其余 `src/` 零改动。

## §0 口径与时间轴（读本节再看读数）

| 项 | 值 | 出处 |
|---|---|---|
| 本版窗口起点 | **2026-11-05 23:13:25**（`plan0.2.3.txt`，本版最早的 0.2.3 产物） | `Get-Item plan0.2.3.txt`；其后 `.work/v023-BRIEF.md` 23:48:16、`docs/zh/55` 23:54、`52` ~ `54` 00:06 ~ 00:07 |
| 关窗读数 | `Farmstorm.java` **2026-11-06 03:02:29**、`tools/verify_project.py` **03:02:25**（F-308 关窗） | `Get-Item`，两处都早于 F-309（03:07 开工） |
| 读数工具 | 行数与命中一律 `[IO.File]::ReadAllLines($p,[Text.Encoding]::UTF8)` + `Select-String`；键集一律 UTF-8 解析 JSON / toml | 本文件各节「出处」栏 |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build"` → `BUILD SUCCESSFUL`，**exit 0** | 见 §1 末行 |
| 检查器 | `python -X utf8 tools/verify_project.py` → `结论：FAIL 0 项，WARN 1 项`，**exit 0** | 见 §1 末行 |
| 发布件 | `build/libs/farmstorm-0.2.3+mc1.21.1.jar` **1,142,338 B**；`-sources.jar` **1,082,655 B** | `Get-ChildItem build/libs`（本条不属八项读数，仅供核对 jar 名） |

---

## §1 八项读数（E-65-1）

每项给「读数 / 出处 / 一行原文」。原文里的数字就是判据，不写「通过」两个字。

### ① Mixin **0**（`src/` 口径，`@Mixin` 与 `*.mixins.json` 都数）

- **读数**：`@Mixin` 命中 **0**（扫 `src/` 下 **152** 个 `.java`）；`*.mixins.json` 文件 **0** 个。
- **出处**：`Get-ChildItem -Recurse -File src -Include *.java | Select-String '@Mixin'` → 0；`Get-ChildItem -Recurse -File src -Filter *.mixins.json` → 0；旁证 `tools/verify_project.py` §4。
- **原文**：`[OK]   无 Mixin / TODO / 调试残留 / 空 catch`

### ② `playToClient(` **2**

- **读数**：恰 **2** 处，都在 `network/FarmstormNetwork.java`。
- **出处**：`Select-String -SimpleMatch 'playToClient('`。
- **原文**：
  - `FarmstormNetwork.java:42: .playToClient(CoreListPayload.TYPE, CoreListPayload.STREAM_CODEC,`
  - `FarmstormNetwork.java:47: .playToClient(CultivatorLinkPayload.TYPE, CultivatorLinkPayload.STREAM_CODEC,`

### ③ `PROTOCOL_VERSION` = `"3"`

- **读数**：全仓 **1** 处声明，值为字符串 `"3"`；`"2"` 命中 0。
- **出处**：`Select-String 'PROTOCOL_VERSION\s*='`。
- **原文**：`FarmstormNetwork.java:29: public static final String PROTOCOL_VERSION = "3";`
- **旁证**（启动日志，`acceptance.md` §4.4）：`自检 网络包注册 farmstorm:core_list（版本 3，S2C 只读，MAX_CORES 64）→ OK` / `自检 网络包注册 farmstorm:cultivator_link（版本 3，S2C 只读，MAX_LINKS 1024）→ OK`

### ④ 注册面 **92**（12 类逐类）

- **读数**：`28 + 28 + 8 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 1 = 92`。
- **出处**：`tools/verify_project.py:458-471` 的 `EXPECTED_COUNTS` 表（12 项，`sum()` 由 :610 现算）；运行期旁证是启动自检行。

| # | 类 | 读数 |
|---|---|---|
| 1 | `Registries.ITEM` | **28** |
| 2 | `Registries.BLOCK` | **28** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | **8** |
| 4 | `Registries.ENTITY_TYPE` | **13** |
| 5 | `Registries.MOB_EFFECT` | **4** |
| 6 | `Registries.SOUND_EVENT` | **3** |
| 7 | `Registries.RECIPE_TYPE` | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | **1** |
| 10 | `Registries.DAMAGE_TYPE` | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | **1** |
| 12 | `Registries.MENU` | **1** |
| — | **合计** | **92** |

- **原文**（检查器）：`已核 registry 对象: 92 / 92`（第 2 节末行，12 行逐类 `[OK] x: N 个 ID 齐全` 全绿）。
- **原文**（启动自检，`acceptance.md` §4.4）：
  `自检 注册对象 92 个：items 28 / blocks 28 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK`
- **同提交断言**：`Farmstorm.java:180-183` 的 `boolean ok` 把 `total == 92` 与 12 个逐类数一起断言（不是只断总数）。

### ⑤ 口径外 **3 类**（不计入 92，见 §5 的人工登记）

- **读数**：`ATTACHMENT_TYPES` **1** / `VillagerProfession` **1** / `PoiType` **1**，三类各 1 个注册项。
- **出处**：三个注册器各一处 `DeferredRegister.create(...)` + 各一处 `REGISTER.register(...)`。
- **原文**：
  - `FarmstormAttachments.java:33: DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Farmstorm.MOD_ID);` → `:37 REGISTER.register("ai_suspend", ...)`
  - `FarmstormVillagerProfessions.java:20: DeferredRegister.create(Registries.VILLAGER_PROFESSION, Farmstorm.MOD_ID);` → `:37 REGISTER.register("botanist", ...)`
  - `FarmstormPoiTypes.java:19: DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Farmstorm.MOD_ID);` → `:33 REGISTER.register("botanist",`
- **不计入 92 的证明**：`EXPECTED_COUNTS` 只有 12 个键（`items` ~ `menu_type`），合计 92；上面三个类名一个都不在表里。详见 §5。

### ⑥ config **175 键 / 60 push**

- **读数**：server toml **175 键**、文件 **803 行**、顶层节 **36** 个、首节 `[converter]` 已不存在（首行 `[crop_converter]`）；`FarmstormConfig.java` 里 `.push(` **60** / `.pop(` **60**（1133 行）。client 侧仍独立一份 3 键。
- **出处**：`[IO.File]::ReadAllLines('run/config/farmstorm-server.toml')` 按 `^\s*[A-Za-z0-9_.]+\s*=` 计键、按 `^\[` 计顶层节；`FarmstormConfig.java` 计 `\.push\(` / `\.pop\(`。
- **原文**（检查器）：
  - `文档口径键数: 175（… 按并集去重后 180，再扣掉 5 个已删键 = 175）`
  - `生成 toml 键数: 175`
  - `[OK]   175 个配置键与文档逐字一致（toml 175 键）`
- **口径注**：`push` 指 `ModConfigSpec.Builder` 的 `.push(` 调用数（`[converter]` 的 `b.push`/`b.pop` 被 `[crop_converter]` 的同一对替换 ⇒ 仍是 60），**不是**顶层节数 36；两个数一起给，避免与 55 号表 C 的「顶层节 36」串味。

### ⑦ 语言键 **321 × 2**

- **读数**：`zh_cn.json` **321** 键、`en_us.json` **321** 键，`zh-only = 0` / `en-only = 0`。
- **出处**：两份 `src/generated/resources/assets/farmstorm/lang/*.json` 的 UTF-8 解析（`ConvertFrom-Json` 数顶层属性）。
- **原文**（检查器）：`[OK]   lang 键集一致，共 321 键`
- **口径注**：检查器只比两份键集相等，**不校验 321**；321 这个绝对值由 F-307 的人工计数（55 号表 D：`340 − 23 + 4 = 321`）与本节读数兜住。

### ⑧ Java **152** 个 `.java`

- **读数**：`src/main/java` **152** 个 `.java`；`src/` 全树同为 **152**（没有测试源目录的 `.java`）。
- **出处**：`Get-ChildItem -Recurse -File src/main/java -Filter *.java | Measure-Object`。
- **旁证**：四个被删的类确实不在树里——`ConverterMenu.java` / `ConverterScreen.java` / `ConverterRecipeBookCategory.java` / `SeedItem.java` 各 0 命中（`acceptance.md` #31 同读数）。

### 本节两条命令的结论行

- `BUILD SUCCESSFUL in 13s` /`5 actionable tasks: 2 executed, 3 up-to-date`，**exit 0**。
- `已核 registry 对象: 92 / 92` · `[OK]   175 个配置键与文档逐字一致（toml 175 键）` · `[OK]   lang 键集一致，共 321 键` · `结论：FAIL 0 项，WARN 1 项`，**exit 0**。

---

## §2 19 项必改逐条落点（E-65-2）：**19 / 19**，无一项悬空

清单来源：`.work/v023-BRIEF.md` §5 的 19 行；落点任务号来源 `docs/zh/55` §3 表 A 表下注第 1 条。每行给「落点任务 / 落点位置 / 终态佐证」。

| # | BRIEF §5 的动作 | 落点任务 | 落点位置（实读） | 终态佐证（一行原文） |
|---|---|---|---|---|
| 1 | 方块 `CONVERTER`→`CROP_CONVERTER` + 铁砧七项 | **F-287 + F-288** | `registry/FarmstormBlocks.java:72,76` | `public static final DeferredBlock<CropConverterBlock> CROP_CONVERTER = REGISTER.registerBlock("crop_converter",` / `.strength(5.0F, 1200.0F)` |
| 2 | 新增 `PLAIN_CHIVES_CROP`（用 `PlainCropBlock`） | **F-297** | `FarmstormBlocks.java:113` | `public static final DeferredBlock<PlainCropBlock> PLAIN_CHIVES_CROP = REGISTER.registerBlock("plain_chives_crop",` |
| 3 | 方块物品 `CONVERTER`→`CROP_CONVERTER` | **F-287** | `registry/FarmstormItems.java:96-97` | `public static final DeferredItem<BlockItem> CROP_CONVERTER = REGISTER.registerItem("crop_converter",` |
| 4 | 删 7 个种子物品 + 新增 `PLAIN_CHIVES_SEEDS` | **F-295 + F-298** | `FarmstormItems.java:127` | `public static final DeferredItem<PlainSeedItem> PLAIN_CHIVES_SEEDS = REGISTER.registerItem("plain_chives_seeds",`（`SeedItem.java` 已整文件删，F-299） |
| 5 | 创造栏清单同步 | **F-295 + F-298** | `FarmstormItems.java:135,143` | `items.add(CROP_CONVERTER);` / `items.add(PLAIN_CHIVES_SEEDS);`（`SEED_ITEMS` 组只剩这 1 个播种物） |
| 6 | 方块实体 `CONVERTER`→`CROP_CONVERTER` | **F-287** | `registry/FarmstormBlockEntities.java:43-45` | `REGISTER.register("crop_converter", () -> BlockEntityType.Builder` … `.of(CropConverterBlockEntity::new, FarmstormBlocks.CROP_CONVERTER.get())` |
| 7 | 删 `FarmstormMenus` 的 `CONVERTER` 条目（2 → 1） | **F-289** | `registry/FarmstormMenus.java`（21 行） | 该文件 `converter` / `CONVERTER` 命中 **0**；检查器 `menu_type: 1` |
| 8 | 整文件删 `ConverterMenu` / `ConverterScreen` / `ConverterRecipeBookCategory` + `FarmstormClient` 两行 | **F-289** | 三个文件不存在；`client/FarmstormClient.java:75-76` | `registerRecipeCategoryFinder(farmstorm:converting, … → RecipeBookCategories.UNKNOWN)` 已内联（`acceptance.md` #31 同一读数） |
| 9 | `ConverterBlock`→`CropConverterBlock`（删 `getMenuProvider`、加 `WORKING`/`getTicker`/`setPlacedBy`/`onRemove`） | **F-287 + F-292** | `block/CropConverterBlock.java` | 8 个 variant 的 `blockstates/crop_converter.json` 在位（`acceptance.md` #23 原文表） |
| 10 | `ConverterBlockEntity`→`CropConverterBlockEntity`（单槽 + 进度 + 绑定核） | **F-293** | `blockentity/CropConverterBlockEntity.java` | `Held` / `Progress` / `Core` 三字段 + 599 刻投递实测（`acceptance.md` #13） |
| 11 | `ConverterRecipe` 删五字段与 `reverse()` | **F-290** | `recipe/ConverterRecipe.java:22` | `public ConverterRecipe(Ingredient input, ItemStack result) {`（两字段） |
| 12 | `ModRecipeProvider`（改配方 / 删 6 条种子 / 单向化 / 加 `plain_chives` / 加培养器配方） | **F-291 + F-301** | `datagen/ModRecipeProvider.java` | 21 条 `converting` 配方；`crop_converter` = 6 铁 2 玻璃 1 红石、`cultivator` = 4 铁 4 玻璃 1 红石（`acceptance.md` #11） |
| 13 | `FarmstormConfig` 删 `[converter]` 三键 + 加 `processTicks` / `bulletLifetimeTicks` | **F-305** | `config/FarmstormConfig.java` | 175 键 / `processTicks = 600`；`^\[converter\]` 命中 0、`^\[crop_converter\]` 命中 1（`acceptance.md` #7） |
| 14 | `ModAdvancementProvider` 删 `seed_cycle` / `nether_star_toll`，`converter` 换图标 | **F-296**（`converter` 判据在 F-287、`seed_cycle` 在 F-295、`nether_star_toll` 在本条） | `datagen/ModAdvancementProvider.java:81-84` | `advancement("converter", root, FarmstormItems.CROP_CONVERTER.get(), …)` + `.placedBlock(FarmstormBlocks.CROP_CONVERTER.get())`；产物 16 个 advancement，两个被删项 0 命中 |
| 15 | 作物战利品表去种子 + 新增两张表 | **F-295** | `datagen/ModLootTableProvider.java:112-113,139` | `crop(FarmstormBlocks.PLAIN_CHIVES_CROP.get(), FarmstormItems.PLAIN_CHIVES.get(), FarmstormItems.PLAIN_CHIVES_SEEDS.get(), ConstantValue.exactly(1), 3);` / `this.add(FarmstormBlocks.CROP_CONVERTER.get(), this.createSingleItemTable(...))` |
| 16 | `ModBlockStateProvider` 两态 × 四朝向 + `ModItemModelProvider` 加 `instanceof` + 平凡作物四阶段 | **F-288 + F-292 + F-297** | `datagen/ModItemModelProvider.java:69`；`blockstates/crop_converter.json` | `return block instanceof CropConverterBlock || block instanceof OutpostCoreBlock || block instanceof CultivatorBlock;`；`variants=8`（四朝向 × 两态，全 `uvlock: true`） |
| 17 | `ModLangProvider` 逐键清单（删 23 / 加 4 / 改名 2 / 改值 2） | **F-307** | `datagen/ModLangProvider.java` | `zh_cn = 321` / `en_us = 321` / `zh-only = 0` / `en-only = 0` |
| 18 | `ModItemTagsProvider` 的 `#farmstorm:seeds` 改写（+ `CONVERTER_FUEL` 段） | **F-295 + F-298** | `datagen/ModItemTagsProvider.java:44`（seeds 改写已执行）；`:50` 的 `CONVERTER_FUEL` 段**按上游更正保留** | `tag(FarmstormTags.SEEDS).add(FarmstormItems.PLAIN_CHIVES_SEEDS.get());`；`tag(FarmstormTags.CONVERTER_FUEL).add(` **仍在**——见下方「本行的差异登记」 |
| 19 | `Farmstorm.java:180-183` + `verify_project.py:334-393`（现 :380-471）改到 §3.1 | **F-308** | `Farmstorm.java:180-183`；`tools/verify_project.py:458-471` | `boolean ok = total == 92 && items == 28 && blocks == 28 …`；`已核 registry 对象: 92 / 92` |

**计数：19 / 19 有落点，悬空 0。**

**本行的差异登记（#18）**：BRIEF §5 #18 的后半句写「`:54` `CONVERTER_FUEL` 整段删、`FarmstormTags.java:38-40` 删该 TagKey」，但**上游更正覆盖了它**：`docs/zh/57` Phase 58 的「常见翻车点」（:316）原文写着「**顺手删 `converter_fuel` 标签**：Phase 65 的「不改清单」当场红，而 52 号 D-209 并未要求删它」，`docs/zh/55` F-295 的验收 2（:261）也写着「`data/farmstorm/tags/item/converter_fuel.json` **仍存在**」。⇒ 本行只执行了「`#farmstorm:seeds` 成员改写」一半，`CONVERTER_FUEL` 这一半按 `D-209`（不在本版删除面）**保留**，并由 §3 第 3 项反向证其在位。这不是悬空：落点任务（F-295 / F-298）真实存在，且终态由两处上游文本明文要求。

---

## §3 不改清单的负向取证（E-65-4，六项）

不跑 git，所以每项给「mtime 早于本版窗口起点 2026-11-05 23:13:25」或「逐行核对」两种可复核读数之一。**六项各自 0 行改动。**

### 1. `TierTrade`

- **mtime**：`src/main/java/com/dashpig/farmstorm/event/TierTrade.java` → **2026-11-04 07:49:41**，比窗口起点早 **1 天 15 小时**；**81** 行。
- **逐行核对**：仍只有三档常量，`:49 TierTrade.COMMON` / `:51 TierTrade.RARE` / `:53 TierTrade.EPIC`，`:56 private static TierTrade of(...)` 未变。
- **结论**：**0 行改动**。

### 2. 农民三行

- **mtime**：`event/VillagerTradeEvents.java` → **2026-11-05 21:51:21**。它早于窗口起点 **1 小时 22 分**，且晚于 v0.2.2 的修复任务书 `.work/TASK-V022-FIX.md`（**21:49:42**）仅 **1 分 39 秒** ⇒ 属于 **0.2.2 修复波次**，不属于本版窗口。
- **逐行核对**（`:55` / `:56` / `:57` 三行原文）：
  ```java
  event.getTrades().get(1).add(TierTrade.COMMON);
  event.getTrades().get(3).add(TierTrade.RARE);
  event.getTrades().get(5).add(TierTrade.EPIC);
  ```
- **结构核对**：`VillagerProfession.FARMER` 判断在 `:52`，**仍在**植物学家分支（`:41`，带 `:50 return;`）**之后**；文件总行数 **59** = `docs/zh/49` F-280 验收记录的「37 → **59**」，与 v0.2.2 签字时一致。
- **结论**：**0 行改动**（三行文本逐字未变，位置因上方植物学家分支而整体下移 22 行）。

### 3. `#farmstorm:converter_fuel` 标签（**它必须仍在**）

- **mtime**：`src/generated/resources/data/farmstorm/tags/item/converter_fuel.json` → **2026-11-02 00:40:41**，早于窗口起点 **3 天 22 小时**。
- **仍在的证明**：该文件在树里（`Get-ChildItem -Recurse -File src -Filter converter_fuel.json` → 1 个命中），内容 **37 项** `values`（18 项原版农作物 + 19 条改良作物，首项 `minecraft:wheat`、末项 `farmstorm:friend_chives`）；TagKey 仍在 `util/FarmstormTags.java:39` `public static final TagKey<Item> CONVERTER_FUEL = item("converter_fuel");`；provider `datagen/ModItemTagsProvider.java:50` 仍 `tag(FarmstormTags.CONVERTER_FUEL).add(`。
- **结论**：**未被删、0 行改动**（Phase 58 没删它，§2 #18 的差异登记由此闭环）。

### 4. `OutpostCoreBlockEntity` 的补绑算法

- **mtime**：`blockentity/OutpostCoreBlockEntity.java` → **2026-11-06 02:35:22**。这一处**在窗口内**，但改动是**计划内的唯一一处**：候选收集由「只认培养器」扩到两种机器（`F-303` / `A-100`，`docs/zh/55` §3 表下注第 2 条 ③、§9 跨 Phase 台账第 9 行）。
- **逐行核对**（方法体 `rebindNearby`，`:593-650`）——五条写入边界与排序链一个没动：
  - 第一行无主核闸：`:594 if (this.ownerUuid == null) {` → `:595 return;`
  - `pruneCultivators(` **0** / `removeCultivator(` **0** / `cultivators()` **0**（「只加不摘」）
  - `addCultivator(` **1**（`:636`）、`setCore(` **2**（`:640` / `:642`，文本上都在 `addCultivator` 之后）
  - 排序链：`comparingDouble` **1** + `thenComparingInt` **3**（`:609-613`，距离 → x → y → z）
  - 容量：`:597-600` 整轮入口判一次、`:615-617` 每绑一个重读一次，`break` 在 cap 处
  - 相位门：`:110 REBIND_INTERVAL_TICKS = 100`、`:570-572 rebindDue` 未变
- **唯一新增的两行**（授权改动）：`:607-608 candidates.addAll(AreaBlockEntities.collect(server, this.getBlockPos(), RANGE_CHUNKS, CropConverterBlockEntity.class));`
- **结论**：**算法 0 行改动**；新增的只有候选收集那一类与对应的 `kind` 分支（F-303 明文要求）。

### 5. `verify_project.py` 的 config 期望面（现在是「从文档现算」，不是写死）

- **mtime**：`tools/verify_project.py` → **2026-11-06 03:02:25**（F-308 关窗，属本版授权改动；本项证的是「期望面仍从文档现算」这一形态，不是整文件 0 行）。
- **逐行核对**：
  - 期望面能现算：`:610 expected_total = sum(EXPECTED_COUNTS.values())`；实跑打出 `文档口径键数: 175（… 按并集去重后 180，再扣掉 5 个已删键 = 175）` 与 `生成 toml 键数: 175`。
  - **没有写死 175**：全文 `175` 命中 **4** 处（`:14` / `:19` 文档字符串的算式、`:198` / `:248` 解释「静默跳过会让期望集悄悄停在 175，比报错更糟」的注释），**没有一处是断言**。
  - 五个老解析器原位：`def expected_config_keys` `:67`、`_v002` `:85`、`_v003` `:93`、`_v010` `:101`、`_v011` `:314`；本版的 3 删 / 2 增只由新函数 `expected_config_keys_v023` `:239` 现算。
  - 五个键在全文的命中：`converter.xpCost` **0** / `converter.netherStarForwardCost` **0** / `converter.netherStarOnReverse` **0**（三删键在工具里已无痕迹）；`crop_converter.processTicks` 与 `pumpkin_turret.bulletLifetimeTicks` 各 **1** 处，都在 `:18` 的**文档字符串**里，不是清单项。
- **结论**：**期望面 0 行形态改动**（仍是「从文档现算」），没有退化成写死的 175。

### 6. `MelonGrenadeProjectile` / `MelonSliceProjectile` 两个类

- **mtime**：`entity/thrown/MelonGrenadeProjectile.java` → **2026-11-02 00:46:47**（早于窗口起点 3 天 22 小时）；`entity/thrown/MelonSliceProjectile.java` → **2026-10-28 19:47:05**（早于窗口起点 8 天 3 小时）。
- **逐行核对**：行数 **69** / **45**（`[IO.File]::ReadAllLines` 口径；`acceptance.md` #26 记的 70 / 46 是「按换行 split、含末尾空行」的口径，差 1 是计数口径而非改动）；两个类都**没有**寿命判定，结算仍在 `onImpact(...)` 里经 `ExplosionQueue.submit` 落地（300 刻只作用于 `TurretBullet`，BRIEF §6 红线 6 / 52 号红线 6）。
- **结论**：**0 行改动**。

---

## §4 已知影响三条（E-65-3 的正文口径，与 README 三段一一对应）

1. **史诗作物变便宜**：`[converter]` 的三个键（`xpCost` / `netherStarForwardCost` / `netherStarOnReverse`）在 F-305 全部删除，**下界之星门槛不存在了**；同时新机器 `crop_converter.processTicks = 600` ⇒ **30 秒一台**（`acceptance.md` #13 实测 599 刻投递）。结果：火把花 / 瓶子草这两个史诗作物从「要下界之星 + 经验」变成「**30 秒 1 个**」，与植物学家交易并行。出处：`docs/zh/52` §8 第 3 条「史诗作物变便宜（下界之星门槛删除）」、BRIEF §7 第 4 条。
2. **培养器产出翻倍**：判据反转（D-219）后，**自身即种子**的 11 条投入物从「2 个」变「**4 个**」；有独立种子的那一档仍是「2 个投入物 + 2 个种子」（`acceptance.md` #18 原文：`carrot` → `{count: 4, id: "minecraft:carrot"}`、`poisonous_potato` → `{count: 4, id: "minecraft:poisonous_potato"}`、`wheat` → 2 + 2）。出处：`docs/zh/52` §8 第 4 条、`docs/zh/55` §3 表下注。
3. **老存档有损**：`farmstorm:converter` 方块 → 缺失方块（`acceptance.md` #32：`/setblock … farmstorm:converter` ⇒ `Unknown block type 'farmstorm:converter'`，该位置解析成 air）；`farmstorm:converter` 物品失效；**7 个种子物品失效**（`SeedItem.java` 与 7 张 `*_seeds` 全部删除，F-295 / F-299）。出处：`docs/zh/52` §8 第 2 条、BRIEF §7 第 3 条。

---

## §5 口径外 3 类的人工登记（E-65-6）

**这 3 类不计入 92；`verify_project.py` 既不核它们的存在、也不核计数**——它的 `EXPECTED` 只有 12 组（`EXPECTED_COUNTS` `:458-471`，12 个键、合计 92），三个类名一个都不在表里。这条工具缺口由三处人工兜住：55 号表 B.2、F-303 的验收、本节的签字。

| # | registry | 基线 | 本版增量 | 改后 | 注册项原文 | 落点任务 |
|---|---|---|---|---|---|---|
| 13 | `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` | 1 | 0 | **1（口径外）** | `FarmstormAttachments.java:37 REGISTER.register("ai_suspend", () -> AttachmentType.builder(...))` | —（v0.2.1 引入，本版未动） |
| 14 | `Registries.VILLAGER_PROFESSION` | 1 | 0 | **1（口径外）** | `FarmstormVillagerProfessions.java:37 REGISTER.register("botanist", () -> new VillagerProfession(` | —（v0.2.2 引入，本版未动） |
| 15 | `Registries.POINT_OF_INTEREST_TYPE` | 1 | 0 | **1（口径外）** | `FarmstormPoiTypes.java:33 REGISTER.register("botanist",` | —（v0.2.2 引入，本版未动） |
| | **合计** | **3** | **0** | **3** | 三个 `DeferredRegister.create` 各 1 个 `register` | |

**三句必须写死的口径**：① 三类**都不计入 92**（92 只数 12 类）；② `verify_project.py` **既不核存在也不核计数**，绿了不代表它们在；③ 登记证据来自**两个文件**——`src/` 的三个注册器（上表「注册项原文」列）与本记录；运行时读数来自 `acceptance.md` 的启动自检 12 类行（该行同样不列这三类）。

---

## §6 本轮的自检（本记录自身的可复核读数）

| # | 检查项 | 读数 | 结论 |
|---|---|---|---|
| 1 | 八项读数齐 | §1 的 ①~⑧ 各给读数 / 出处 / 原文；无一处只写「通过」 | 齐 |
| 2 | 19 项必改 | §2 表 **19** 行，逐行有落点任务号，计数 **19 / 19**；#18 的差异单独登记 | 无悬空 |
| 3 | 不改清单 | §3 六项各有「mtime 早于窗口」或「逐行核对」证据 | 各 0 行改动；`converter_fuel` 仍在 |
| 4 | 已知影响 | §4 三条（下界之星门槛 / 30 秒；翻倍 4 个；老存档有损） | 三条齐 |
| 5 | 口径外 3 类 | §5 表在，且写明「不计入 92、检查器既不核存在也不核计数」 | 独立成段 |
| 6 | 上游冻结面 | `docs/zh/00` ~ `57`、`docs/en/*`、`.work` 既有文件、`plan0.2.3.txt` 的 mtime 全部早于本记录开工时刻；本轮**写**过的位置只有 `README.md`、`.work/v023-verify/signoff.md` 与 `Farmstorm.java:193` 那一行注释 | 合规 |
| 7 | build / 检查器 | `BUILD SUCCESSFUL`（exit 0）；`已核 registry 对象: 92 / 92` · 175 键 · 321 键 · `结论：FAIL 0 项，WARN 1 项` | 绿 |
| 8 | 未验证项 | 见 §7 | 逐条一行 |

## §7 没做到的 / 未验证的（一条一行）

- **不跑 git**：E-65-4 的「`git diff` 0 行」改用 mtime + 逐行读数取证；「一字不动」这一级证明本轮没有拿到，只能证到「形态与文本未变」。
- **口径外 3 类没有工具闸**：`verify_project.py` 不核这三类，本记录的登记靠人工（§5 已写明证据来自哪两个文件）。
- **像素观感与图形客户端未验证**：`acceptance.md` #24（青→红的像素观感）、#25（粒子与音效）、#31（`Unknown recipe category` WARN）、#32（0.2.2 实体存档的端到端回放）四条各带未验证子项；`boundaries.md` B-54 / B-57 亦各带 1 条。本轮未起图形客户端，未新增读数。
- **`Farmstorm.java` 还剩两处 98**：`:160` 的「64 → 77 → 90 → 98 的那 12 类」与 `:161` 的「相加不等于 98」是沿革叙述；本任务只授权改 `:193` 那一句，这两处按「`src/` 除该注释外不许有任何改动」**保留未改**，一并登记。
- **行数口径差 1**：`MelonGrenadeProjectile` / `MelonSliceProjectile` 在本记录里是 69 / 45（`ReadAllLines`），`acceptance.md` #26 记 70 / 46（split 含末尾空行）；同一批文件、同一 mtime，差 1 只在计数口径。

## §8 README 三段的同段共现读数（E-65-3）

判据不是「文件里有这几个串」，而是「**同一个段落块**里同时有」。分段口径：`README.md` 全文按**空行**切块（PowerShell：`$text -split "\r?\n\r?\n"`），再逐块判包含关系；下表是实跑读数。

| 段 | 必须同段的串 | 命中块数 | 命中块首句（原文，节选） |
|---|---|---|---|
| 从 v0.2.2 升级必看 | `run/config/farmstorm-server.toml` + `删` + `重生成` | **2**（本新段 + v0.2.1 升级表那一块） | 「盘上的 `run/config/farmstorm-server.toml` 是**老格式**：…**删**掉，下次起服让它按 v0.2.3 的 175 键**重生成**…」 |
| 已知影响（史诗作物变便宜了） | `下界之星` + `30 秒` | **1** | 「`[converter]` 的三个键（…）在 v0.2.3 全部删除，**下界之星门槛彻底没有了**；新的农作物转换器是 `crop_converter.processTicks = 600`，也就是**30 秒一台**。」 |
| 平衡变更（培养器产出翻倍） | `翻倍` + `4 个` | **1** | 「…**自身即种子**的那 11 条投入物每次给的数量**翻倍**：胡萝卜 / 毒马铃薯 / 西瓜片这一类从「2 个」变成「**4 个**」…」 |

负向读数（同一遍扫描）：`只有文档` **0** / `代码未实现` **0** / `代码一行未写` **0** / `下一版（v0.2.3` **0** / 「协议串 2」**0** 命中；`协议串 3` 与 `92 / 92`、`175 个配置键`、`共 321 键` 各在位。

**素材来源与非商业声明一节逐字保留**：该段本轮**没有做过任何 `edit`**；特征串各命中 1 次（`非商业` 3 次，含「非商业自用」「非商业条款」两处正文）——`本项目**不是全部原创**` / `sounds/wild_hunter_loop.ogg` / `署名` / `可移除` / `发布 Release 时必须把这一节复制进 Release 说明`。
