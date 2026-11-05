# 54 号 v0.2.3 技术规格

> 项目：农场风云 / Farmstorm · 版本 **0.2.3**（MC 1.21.1 / NeoForge 21.1.213 / Java 21）· 基线 **0.2.2 已实现态**（git `79b71f1`，`gradle.properties:11` 现为 `mod_version=0.2.2`）。
> 唯一真相源：`docs/zh/52-DECISIONS-0.2.3.md`（D-203 ~ D-224，共 **22 条**）+ `.work/v023-BRIEF.md` §2 的 **18 条**作者裁定。本文**只引用**决策号与裁定号，不新开决策、不改口径；与 52 号冲突时报给作者，冲突登记在 §10。
> 事实底座：`.work/v023-BRIEF.md` §1（源码事实）、§3（目标数字）、§5（19 项必改）、§6（7 条红线）。**本卡与源码冲突时以源码为准**——本文所有 `文件:行号` 都是写稿时用 UTF-8 重读源码后自己核过的，与 BRIEF / 52 号不一致的地方逐条登记在 §10。
> 体例参照：`docs/zh/48-TECH-0.2.2.md`（948 行，只学 §1 基线核对 / §2 类设计 / §3 动作面表 / §4 config 全表 / §5 语言键 / §7 风险 / §8 验收 / §10 差异登记的**目录结构**，不引用它的任何数值）。
> 红线（两条，均不变）：**无 Mixin（0 个，继续禁止）**；自定义网络包**仍是 2 个**（不加包、不改包 id、`MAX_LINKS = 1024` 不动，D-210）。
> 写作纪律：注册面 **92**、server config **175 键 / 60 push**、语言键 **321 × 2**、Java 文件 **152** 四个数**全部由本文自己逐条点名**（方法写在 §1.2 / §1.3 / §1.4 / §1.5），不抄 `BRIEF` §3；凡本文自定的技术参数一律标「**本文确定**」并给理由（汇总见 §0.3）；**不写任务拆分与排期**（属 55 号）、**不写未定事项与占位标记**、不用第二人称敬语。

---

## 0. 阅读约定

### 0.1 本文负责什么

| 项 | 内容 |
|---|---|
| 负责 | 注册面 **92** 的逐类独立点名（§1.2）+ **口径外 3 类**的显式登记；config **175 键 / 60 push** 与语言键 **340 → 321 × 2** 的逐键点名（§1.3、§1.4、§7）；三个技术构件（农作物转换器 / 平凡作物轻路径 / 连线类型字节）的类设计与调用面（§2 ~ §5）；资源面 7 张新贴图与 3 个 mcmeta 的落位（§3）；行为规格（§4）；**32 条**验收自检（§8）；风险 **RT-71 ~ RT-82**（§9）；与上游的差异登记 **15 条**（§10） |
| 不负责 | 为什么定成 X（52 号）；玩家可见表现、数值表与边界叙述（53 号）；任务、Phase、裁决（55 号）；主提示词与阶段提示词（56 / 57 号） |
| 不动的文件 | `src/` 下**既有**文件一律只读（本文只描述「要改成什么」，不落地代码）；`docs/zh/00` ~ `53`、`docs/en/*`、`.work/*`、`plan0.2.3.txt` 一律只读；本文只新建 `docs/zh/54-TECH-0.2.3.md` 一个文件 |
| 本版新增的文件面 | 2 个新 Java 类（`PlainCropBlock` / `PlainSeedItem`；`CropConverterBlock` / `CropConverterBlockEntity` 是改名，不计增）+ 7 张方块贴图 + 3 个 mcmeta + 1 张重命名的物品贴图；**不新增**任何 `data/` 手写 JSON（全部由 datagen 出） |

### 0.2 计数纪律

1. **四个数全部自己数**：注册对象按「12 个注册类的注册调用次数」数（§1.2）；config 按「行首 `.define*` 计一键、`b.push` 计一组」数（§1.3）；语言键按「两份 `lang/*.json` 的行首 `"键":`」数（§1.4）；Java 文件按 `src/main/java` 全树 `*.java` 计数（§1.5）。**不抄** `BRIEF` §3、**不抄** `Farmstorm.java:180-183` 的断言值、**不抄** `verify_project.py` 的 `EXPECTED_COUNTS`。
2. 每张表满足「行数 = 声明的计数」「小计 = 逐行相加」「跨表不重不漏」。
3. 数值只允许三个来源：52 号的决策号、`BRIEF` §2 / §3 的已给数值、两个已给数值的算术（`98 − 7 + 1 + 1 − 1 = 92`、`176 − 3 + 2 = 175`、`340 − 23 + 4 = 321`、`34 − 7 + 1 = 28`、`27 + 1 = 28`、`21 − 4 = 17`、`26 − 6 + 1 = 21`、`18 − 2 = 16`、`29 − 6 + 2 = 25`、`154 − 3 − 1 + 2 = 152`、`6 − 6 + 1 = 1`），以及本文标「本文确定」的技术参数（每个都给理由）。**不发明玩法数值**。
4. **读文件一律用 UTF-8**：`[IO.File]::ReadAllLines($p, UTF8)` 或 `-Encoding UTF8`。默认 `Get-Content` 会把无 BOM 的 UTF-8 按本机代码页读，行号与字符都不可信（48 号 §0.2 第 4 条已记过一次实测）。
5. **「原版」与「本仓」分两棵树**：本仓类在 `src/main/java/com/dashpig/farmstorm/`；本文引用原版行为一律给 `类名#方法`，不假造行号。
6. **本版所有「删」都要能被 grep 或目录计数证伪**：删文件看目录、删键看 `verify_project.py` 的键集、删贴图看目录计数、删配方看 `data/farmstorm/recipe/` 的文件数。

### 0.3 「本文确定」的清单（16 项，逐项正文有理由）

| # | 参数 | 值 | 正文 |
|---|---|---|---|
| 1 | 转换器结构属性的「七项」口径 | `mapColor(MapColor.METAL)` / `strength(5.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `sound(SoundType.ANVIL)` / `pushReaction(PushReaction.BLOCK)` 五项 + `MapColor.METAL` / `SoundType.ANVIL` 两个取值 = 七项，**不新增第六、第七个成员** | §1.2、§2.1 |
| 2 | 新机器的方块模型父模型 | `minecraft:block/cube`（六面显式），**不用** `cube_bottom_top`（它只有 top/side/bottom 三个槽，装不下 front） | §3.2 |
| 3 | 新机器的模型名 | 空闲 `crop_converter`、工作 `crop_converter_working`（与培养器 `cultivator` / `cultivator_working` 同形） | §3.2 |
| 4 | 贴图粒子面 | `particle` 取 `block/crop_converter_a_front_still`（与现有转换器取 `north` 面同一口径，`ModBlockStateProvider:175`） | §3.2 |
| 5 | 平凡作物模型名 | `plain_chives_stage0` ~ `plain_chives_stage3`（沿用 `cropBase()` 去 `_crop` 后缀的既有规则，`ModBlockStateProvider:34`） | §3.5 |
| 6 | 平凡作物贴图指向 | 显式写 `farmstorm:block/friend_chives_stageN`，**不**走「同名贴图」的自动拼法 | §3.5 |
| 7 | `Kind` 的线上编码 | 一个字节：`CULTIVATOR = 0`、`CROP_CONVERTER = 1`；未知值按 `CULTIVATOR` 解 | §5.1 |
| 8 | 转换器连线颜色 | `(0.92F, 0.22F, 0.22F, 1.0F)`；不用 `0xFF0000` | §5.4 |
| 9 | 绑定条目的落盘 | 核侧 `Cultivators`（`long[]`）**旁挂**一条 `CultivatorKinds`（`int[]`）；缺键 ⇒ 全部 `CULTIVATOR` | §5.2 |
| 10 | `PROTOCOL_VERSION` | `"2"` → **`"3"`**（codec 变了，`FarmstormNetwork:22-24` 的既有纪律） | §5.5 |
| 11 | `crop_converter.processTicks` 范围 | **20 ~ 72000**（与 `cultivator.cycleTicks` 逐字同范围，`FarmstormConfig:964`） | §6.2 |
| 12 | `pumpkin_turret.bulletLifetimeTicks` 范围 | **1 ~ 72000**（下界 1 = 至少活一刻；上界抄同组 `fireIntervalTicks` 的 72000，`FarmstormConfig:456`） | §6.2 |
| 13 | 培养器新配方的 `unlockedBy` | `has_glass`（`Items.GLASS`）——原版材料，**不用**本模组独有标签 | §2.6 |
| 14 | `first_conversion` 的授予点 | 新机器投递成功那一支，授予**绑定核的归属者**（在线时）；离线不授予 | §4.1.6 |
| 15 | 转换器配方的数量算法 | 21 条产出 1 个（`result` 的 count 恒为 1） | §4.2 |
| 16 | `PlainCropBlock` 的碰撞箱 | 逐字照抄 `FarmstormCropBlock` 的 `SHAPE_BY_AGE` 四段（`FarmstormCropBlock:48-53`） | §4.3 |

---

## 1. 基线与口径总表

### 1.1 五个数一句话

| 口径 | 0.2.2 实测 | v0.2.3 | 算式 | 依据 |
|---|---|---|---|---|
| 注册对象（12 类相加） | **98** | **92** | `98 − 7（种子）+ 1（平凡种子）+ 1（平凡作物方块）− 1（MenuType）= 92` | D-205、裁定 2 / 7 / 14 |
| server config | **176 键 / 60 push** | **175 键 / 60 push** | `176 − 3（[converter] 整组）+ 2（processTicks / bulletLifetimeTicks）= 175` | D-223 |
| 语言键 | **340 × 2** | **321 × 2** | `340 − 23（7 种子 + 9 GUI + 3 config + 4 进度）+ 4（2 平凡作物 + 2 config）= 321` | D-224 |
| Java 文件 | **154** | **152** | `154 − 3（ConverterMenu / ConverterScreen / ConverterRecipeBookCategory）− 1（SeedItem：7 个种子删完后无任何引用）+ 2（PlainCropBlock / PlainSeedItem）= 152` | BRIEF §3.2、D-208 |
| 网络包 / Mixin | **2 / 0** | **2 / 0** | 包数不变；`Link` 多一个字节；Mixin 仍是禁项 | D-210、红线 3 |

> `BRIEF` §3.2 与 52 号 D-224 把语言键算式写成「340 − 21 + 2 = 321」。**分项清单本身是对的**（7 + 9 + 3 + 4 = 23，2 + 2 = 4），两条算式都得 321（因为 `−23 + 4` 与 `−21 + 2` 同为 `−19`），但「−21 + 2」与它自己的分项表不符。本文按**逐键清单**写 `−23 + 4`，差异登记在 §10 第 1 条。

### 1.2 注册面 92 逐类点名（**本文自己从源码数**）

**计数方法（可复现）**：逐个打开 `src/main/java/com/dashpig/farmstorm/registry/` 下的 12 个注册类，按「注册调用次数 + 注册 id 字面量」逐条数。三条口径与 48 号 §1.2 相同：`DeferredRegister#registerItem` / `registerBlock` / `register` 数**调用次数**；走工厂方法的（`cropItem` / `seedItem` / `crop` / `deployed` / `projectile`）数**工厂调用次数**；`damage_type` 是数据驱动，数 `FarmstormDamageTypes` 里的静态 `ResourceKey` 常量个数。

| # | registry | 现在数了什么（文件:行号） | 增 | 减 | 改后 |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | `FarmstormItems.java`：22 次 `cropItem(...)` + 7 次 `seedItem(...)`（`:91`、`:102-109`）+ 4 次 `registerItem(...)`（`:112`、`:115`、`:118`、`:122`）+ 1 次平凡物品（`:131`） | +1（`plain_chives_seeds`） | −7（7 个种子） | **28** |
| 2 | `Registries.BLOCK` | `FarmstormBlocks.java`：15 次 `crop(...)`（`:28-45`）+ 8 次 `deployed(...)`（`:48-68`）+ 4 次 `registerBlock(...)`（`:71`、`:75`、`:84`、`:95`） | +1（`plain_chives_crop`） | 0 | **28** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | `FarmstormBlockEntities.java`：8 次 `REGISTER.register(`（`:26,30,33,36,40,44,52,57`） | 0 | 0 | **8** |
| 4 | `Registries.ENTITY_TYPE` | `FarmstormEntityTypes.java`：12 次 `projectile(...)` + 1 次 `register("turret_bullet", …)`（`:61`） | 0 | 0 | **13** |
| 5 | `Registries.MOB_EFFECT` | `FarmstormMobEffects.java:23,27,31,35` | 0 | 0 | **4** |
| 6 | `Registries.SOUND_EVENT` | `FarmstormSounds.java:20,25,30` | 0 | 0 | **3** |
| 7 | `Registries.RECIPE_TYPE` | `FarmstormRecipes.java:22` | 0 | 0 | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | `FarmstormRecipes.java:24` | 0 | 0 | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | `FarmstormCreativeTabs.java:16` | 0 | 0 | **1** |
| 10 | `Registries.DAMAGE_TYPE` | `FarmstormDamageTypes.java:20,23,26`（3 个 `ResourceKey`） | 0 | 0 | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `FarmstormDataComponents.java:16` | 0 | 0 | **1** |
| 12 | `Registries.MENU` | `FarmstormMenus.java:17`（converter）、`:21`（outpost_core） | 0 | −1（`converter`） | **1** |
| | **合计** | | **+2** | **−8** | **92** |

**逐类小计（与上表逐行相等）**：

```
items                 28
blocks                28
block_entity_type      8
entity_type           13
mob_effect             4
sound_event            3
recipe_type            1
recipe_serializer      1
creative_mode_tab      1
damage_type            3
data_component_type    1
menu_type              1
--------------------------------
相加 = 28+28+8+13+4+3+1+1+1+3+1+1 = 92
```

**改后 28 个物品逐条点名**（`items` 是本版唯一一个「既有增又有减」的类，必须点全）：

- **22 个作物物品**：`puffed_wheat` `blast_carrot` `ubi_server` `toxic_ubi_server` `power_beet` `melon_slice_bomb` `melon_grenade` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `lucky_berry` `molotov` `zangief_cane` `wild_apple` `friend_chives` `point_defense_fungus` `vibration_bean` `compressed_puffed_wheat`（`CropProfile.ALL` 的 22 条，`CropProfile.java:138-140` 起）
- **4 个方块物品**：`crop_converter`（改名）、`compressed_hay_block`、`outpost_core`、`cultivator`
- **2 个平凡作物物品**：`plain_chives`（0.2.2 既有）、`plain_chives_seeds`（本版新增）
- 相加 `22 + 4 + 2 = 28`。

**改后 28 个方块逐条点名**：

- **16 个作物方块**：15 个 `crop(profile)`（`puffed_wheat_crop` … `friend_chives_crop`）+ 本版新增的 `plain_chives_crop`
- **8 个部署方块**：`blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `zangief_cane` `point_defense_fungus` `vibration_bean`
- **4 个功能方块**：`crop_converter`（改名）、`compressed_hay_block`、`outpost_core`、`cultivator`
- 相加 `16 + 8 + 4 = 28`。

**改名为什么不计数**：`converter → crop_converter` 是三处注册对象（`blocks` / `items` / `block_entity_type`）**同一个对象换了 id**，条目数不变（D-206 补充）。按「删一个加一个」算会得到 `blocks = 29`，与实际差 1——这是本版最容易数错的一处。

**口径外 3 类逐条登记（不进 92，但不许隐形）**：

| # | 注册表 | 本版数量 | 对象 | 注册点 | 为什么不在 12 类口径内 |
|---|---|---|---|---|---|
| 1 | `ATTACHMENT_TYPES` | 1（不变） | `farmstorm:ai_suspend` | `registry/FarmstormAttachments`；`Farmstorm.java:65` | `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` 不是 `BuiltInRegistries`（0.2.1 的裁定 #17 已定性） |
| 2 | `VillagerProfession` | 1（不变） | `farmstorm:botanist` | `registry/FarmstormVillagerProfessions`；`Farmstorm.java:67` | 走原版 `Registries.VILLAGER_PROFESSION`，不是本项目的 12 类 |
| 3 | `PoiType` | 1（不变） | `farmstorm:botanist` | `registry/FarmstormPoiTypes`；`Farmstorm.java:66` | 走原版 `Registries.POINT_OF_INTEREST_TYPE` |

**本版不新增任何口径外类型**：平凡作物的方块走 `BLOCK`、种子走 `ITEM`，两条都在 12 类里；转换器是既有对象的改名，不新增表项。

### 1.3 config 175 键 / 60 push

数法与前几版一致：读 `config/FarmstormConfig.java`，按「`b.push("…")` 建组、行首 `.define*` 计一键」逐键落地。本文实测（UTF-8 读）：

```
行首 .define* 的匹配行数        = 177   ← 含 1 行被注释掉的
被注释的定义                    =   1   ← FarmstormConfig.java:940 的 // .defineInRange("farmlandBonus", 16, 0, 1024);
                                  ⇒ 177 − 1 = 176 键
b.push(...) 次数                =  60
                                  ⇒ 176 键 / 60 组
run/config/farmstorm-server.toml 的键 = 176（与源码键集一致）
```

本版变化：**删 3 键**（`[converter]` 整组，`FarmstormConfig.java:295-305`：`xpCost` / `netherStarForwardCost` / `netherStarOnReverse`）+ **加 2 键**（`crop_converter.processTicks`、`pumpkin_turret.bulletLifetimeTicks`）⇒ `176 − 3 + 2 = 175`。

`push` 数**不变**：`[converter]` 的 `b.push`/`b.pop` 被 `[crop_converter]` 的同一对替换（`−1 + 1 = 0`）；`pumpkin_turret.bulletLifetimeTicks` 落在**既有**的 `[pumpkin_turret]` 组内（`FarmstormConfig.java:444-461`），不新增组。⇒ **175 键 / 60 push**。

**两个数别混**：`b.push()` 调用次数是 60，`run/config/farmstorm-server.toml` 的顶层节是 36 个（点号子表另算）。本文与下游一律写「175 键 / 60 push」。

### 1.4 语言键 321 × 2

数法：读 `src/generated/resources/assets/farmstorm/lang/zh_cn.json` 与 `en_us.json`，按行首 `"键":` 计。本文实测 `zh = 340`、`en = 340`、`zh-only = 0`、`en-only = 0`。

按 `ModLangProvider` 的条目构成分组（`ModLangProvider:14-15` 的注释口径，本文复核成立）：

| 前缀组 | 0.2.2 | 本版 | 说明 |
|---|---|---|---|
| `item.` | 34 | **28** | −7 种子 +1 `plain_chives_seeds`（`converter` 是改名） |
| `block.` | 27 | **28** | +1 `plain_chives_crop`（`converter` 是改名） |
| `advancements.` | 36 | **32** | −4（`seed_cycle` / `nether_star_toll` 各 title + description） |
| `farmstorm.configuration.` | 179 | **178** | −3（converter 组）+2（新键）；179 = 176 server + 3 client |
| `entity.` | 13 | **13** | 本版不新增实体 |
| `plain` 组（itemGroup 1 + container 2 + gui 9 + tooltip 20 + effect 4 + subtitles 3 + death 6 + key 2 + command 3 + villager 1） | 51 | **42** | −9（`container.farmstorm.converter` 1 + `gui.farmstorm.converter.*` 8） |
| **合计** | **340** | **321** | `28 + 28 + 32 + 178 + 13 + 42 = 321` |

核对：`28+28=56`，`+32=88`，`+178=266`，`+13=279`，`+42=321`。逐键清单见 §7。

### 1.5 Java 文件 152 / 网络包 2 / Mixin 0

| 项 | 0.2.2 实测（本文自己数） | v0.2.3 | 证据 |
|---|---|---|---|
| `src/main/java` 的 `*.java` | **154** | **152** | `Get-ChildItem src\main\java -Recurse -Filter *.java` = 154；删 3（`menu/ConverterMenu` 216 行、`client/ConverterScreen` 86 行、`client/ConverterRecipeBookCategory` 24 行）− 删 1（`item/SeedItem` 54 行，7 个种子删完后无任何引用）+ 新增 2（`block/PlainCropBlock`、`item/PlainSeedItem`）= 152。两个改名类（`ConverterBlock`→`CropConverterBlock`、`ConverterBlockEntity`→`CropConverterBlockEntity`）**不计数** |
| 自定义网络包 | **2**（`farmstorm:core_list`、`farmstorm:cultivator_link`） | **2** | `FarmstormNetwork:38-44` 两条 `playToClient`；本版只改 `CultivatorLinkPayload` 的 `Link` codec（§5） |
| `MAX_LINKS` | **1024** | **1024** | `CultivatorLinkPayload:36`，一个字符都不动 |
| `PROTOCOL_VERSION` | `"2"` | **`"3"`** | `FarmstormNetwork:25`；codec 变了必须跟着变（`:22-24` 的既有纪律） |
| Mixin | **0** | **0** | `src/` 全树 `@Mixin` 命中 0、`*.mixins.json` 0；`verify_project.py:543-559` 的禁令扫描含这条 |

> **网络包不进注册面**：payload 走 `RegisterPayloadHandlersEvent`，不写 `BuiltInRegistries`，所以 §1.2 的 92 里没有它。**Mixin 的 0 是禁项**，不是「本版没轮到」：本版三个构件全部用注册表 + 事件总线 + 数据驱动实现（尤其没有用 Mixin 去改渲染或配方书）。

---

## 2. 注册面逐项

### 2.1 改名：`converter` → `crop_converter`（三处，不计数）

| # | 位置 | 现值 | 改成 | 依据 |
|---|---|---|---|---|
| 1 | `FarmstormBlocks.java:71-72` | `REGISTER.registerBlock("converter", ConverterBlock::new, …MapColor.STONE.strength(3.5F)…)` | `REGISTER.registerBlock("crop_converter", CropConverterBlock::new, …)`，属性整段换成铁砧七项（§2.1.1） | D-206、D-207 |
| 2 | `FarmstormItems.java:112-113` | `REGISTER.registerItem("converter", … BlockItem(FarmstormBlocks.CONVERTER.get(), …))` | id 改 `crop_converter`、引用改 `FarmstormBlocks.CROP_CONVERTER.get()`、常量名改 `CROP_CONVERTER` | D-206 |
| 3 | `FarmstormBlockEntities.java:44` | `REGISTER.register("converter", … ConverterBlockEntity::new …)` | id 改 `crop_converter`、构造器引用改 `CropConverterBlockEntity::new`、常量名改 `CROP_CONVERTER` | D-206 |

#### 2.1.1 方块属性：铁砧七项逐项落

| # | 项 | 取值 | 出处 |
|---|---|---|---|
| 1 | `mapColor` | `MapColor.METAL` | 原版 `ANVIL` 的第一个成员；与培养器 `FarmstormBlocks.java:98` 逐字相同 |
| 2 | `strength(hardness, resistance)` | `strength(5.0F, 1200.0F)` | 原版 `ANVIL`；培养器 `:99` 同值 |
| 3 | `requiresCorrectToolForDrops()` | 无参 | 原版 `ANVIL`；培养器 `:100` |
| 4 | `sound(...)` | `SoundType.ANVIL` | 原版 `ANVIL` 的取值；培养器 `:101` |
| 5 | `pushReaction(...)` | `PushReaction.BLOCK` | 原版 `ANVIL`；培养器 `:102` |
| 6 | （取值）`MapColor.METAL` | 见第 1 项 | D-207 的「七项」把两个取值单列 |
| 7 | （取值）`SoundType.ANVIL` | 见第 4 项 | 同上 |

**不设** `noOcclusion` / `lightLevel` / `randomTicks` / `explosionResistance`：与培养器同形（D-207 补充）。现状的转换器是 `.strength(3.5F)` + `MapColor.STONE` 且**没有** `sound` / `pushReaction`，本版是一次**属性面整体替换**，不是调两个数。

### 2.2 新增：`plain_chives_crop`（方块）与 `plain_chives_seeds`（物品）

| # | 位置 | 插入点（精确到行） | 内容 | 依据 |
|---|---|---|---|---|
| 1 | `FarmstormBlocks.java` | `:102`（`CULTIVATOR` 注册块的收尾 `);`）与 `:104`（`private FarmstormBlocks() {}`）**之间**，插入 6 行 | `public static final DeferredBlock<PlainCropBlock> PLAIN_CHIVES_CROP = REGISTER.registerBlock("plain_chives_crop", PlainCropBlock::new, <平凡作物的 5 项属性>)；` 属性取 `MapColor.PLANT` / `noCollission()` / `instabreak()` / `sound(SoundType.CROP)`（与 `crop(...)` 私有工厂内的四项逐字相同，`FarmstormBlocks.java:109-113`），**不写** `.randomTicks()`（生长走计划 tick，`FarmstormCropBlock:119-120` 显式关掉随机刻） | D-215、裁定 7 |
| 2 | `FarmstormItems.java` | `:109`（`AWAKENED_PITCHER_PLANT_POD` 声明结束）之后、`:111`（`// ---- 3 个方块物品 ----`）**之前**插入 3 行；并在删掉 `:91` 的 `CHIVE_SEEDS` 后把 `PLAIN_CHIVES_SEEDS` 放在原 `:91` 的位置 | `public static final DeferredItem<PlainSeedItem> PLAIN_CHIVES_SEEDS = REGISTER.registerItem("plain_chives_seeds", properties -> new PlainSeedItem(FarmstormBlocks.PLAIN_CHIVES_CROP.get(), properties), otherProperties());` | D-216 |

**为什么插入点写在 `:102` 而不是 `BRIEF` 的 `:107-112`**：`BRIEF` §5 第 2 项写的 `FarmstormBlocks.java:107-112` 落在私有工厂 `deployed(...)` 的方法体内（`:116-134` 的方法，`:107` 是其签名行附近），不是注册区。注册语句只能写在类体里、私有构造器之前。差异登记在 §10 第 6 条。

**两个新类的形态**（细节在 §4.3，这里只钉注册面）：

| 类 | 父类 | 构造参数 | 注册点 | 依据 |
|---|---|---|---|---|
| `block/PlainCropBlock` | `net.minecraft.world.level.block.CropBlock` | 只有 `BlockBehaviour.Properties`（**不接 `CropProfile`**） | `FarmstormBlocks.java` 的新增行 | D-215；`FarmstormCropBlock.java:43-46,57-60` 的构造器与 codec 强制要档案，故**不复用** |
| `item/PlainSeedItem` | `net.minecraft.world.item.ItemNameBlockItem` | `(Block block, Item.Properties properties)` | `FarmstormItems.java` 的新增行 | D-216；`SeedItem.java:42-44` 的 `useOn` 无条件 `PASS`，故**不复用** |

### 2.3 删除：7 个种子 / 1 个 `MenuType`

| # | 删什么 | 位置（精确到行） | 连带 | 依据 |
|---|---|---|---|---|
| 1 | `puffed_wheat_seeds` | `FarmstormItems.java:102` | 语言键 1、战利品表 1 条、配方 1 条、`#farmstorm:seeds` 成员 1 | D-212 |
| 2 | `power_beet_seeds` | `:103` | 同上 | D-212 |
| 3 | `melon_grenade_seeds` | `:104` | 同上 | D-212 |
| 4 | `pumpkin_turret_seeds` | `:105` | 同上 | D-212 |
| 5 | `awakened_torchflower_seeds` | `:106-107`（跨行声明） | 同上 | D-212 |
| 6 | `awakened_pitcher_plant_pod` | `:108-109`（跨行声明） | 同上 | D-212 |
| 7 | `chive_seeds` | `:91` | 语言键 1、战利品表 1 条（`ModLootTableProvider:104-105`）、`#farmstorm:seeds` 成员 1、`textures/item/chive_seeds.png` | D-212 |
| 8 | `MenuType CONVERTER` | `FarmstormMenus.java:17-18` | 类注释 `:12` 的「2 个 MenuType」改 1；`FarmstormClient.java:62` 那行删；`ConverterMenu` / `ConverterScreen` / `ConverterRecipeBookCategory` 三个文件整删 | D-208、裁定 2 |

**7 个种子删完之后的四个连带面**（照 D-212 补充的五条，逐条给落点）：

| 面 | 现状 | 改后 | 落点 |
|---|---|---|---|
| 物品面 | `FarmstormItems.java:35` 的 `List<DeferredItem<SeedItem>> SEED_ITEMS` 收 7 条 | **整个清单删除**（收 0 条）：7 个种子没了，`plain_chives_seeds` 是 `PlainSeedItem`、走 `registerItem` 不经过 `seedItem(...)` 工厂 ⇒ `:175-180` 的工厂与 `:35`、`:178` 三处一起去掉；`seedItem(...)` 唯一的调用面消失后，`SeedItem.java`（55 行）本身也删（§1.5 的 −1） | `FarmstormItems.java:35,175-180` |
| 战利品面 | 15 张作物表里 5 张带种子参数（`ModLootTableProvider.java:70-71,80-81,83-84,85-86,88-89,90-91,104-105`） | 种子参数一律传 `null` / 去掉那一段，**作物本体的掉落一条不动** | `ModLootTableProvider` |
| 标签面 | `#farmstorm:seeds` 成员 **6**（`ModItemTagsProvider.java:42-48`；产物 `data/farmstorm/tags/item/seeds.json` 也是 6 项） | **1**（`farmstorm:plain_chives_seeds`），算式 `6 − 6 + 1 = 1` | `ModItemTagsProvider.java:42-48`、`FarmstormTags.java:35` |
| 进页面 | `seed_cycle` 的 6 个判据全部引用种子（`ModAdvancementProvider.java:96-104`） | 整条进度删（判据素材没了，D-214） | `ModAdvancementProvider.java:95-106` |

**`FarmstormTags.java:35` 的类注释要顺手改对**：现文「6 个独立播种物 + 4 个自身即播种物的作物」，后半句与标签实际成员不符（标签里只有 6 个物品，那 4 个「自身即播种物」的作物从来没有成员资格）。改成「1 个播种物（平凡韭菜种子）」——只改注释，**不改标签语义**（标签的语义仍然是「能种下去的实物」，D-216 补充）。

### 2.4 改 ID 的连带面（改名不是只改三行）

| # | 连带位置 | 现状 | 改成 | 依据 |
|---|---|---|---|---|
| 1 | `ModLootTableProvider` 的 `crop_converter` 表 | 注册方块的战利品表按 id 命名 | id 随方块走：`data/farmstorm/loot_table/blocks/crop_converter.json` | D-206 |
| 2 | `ModBlockStateProvider.java:160-184` | `converterStates` 用 6 张面贴图 + 4 个 `facing` variant | 重写为 `cropConverterStates`：8 个 variant（§3.2） | D-211 |
| 3 | `ModItemModelProvider.java:68-70` | `usesBlockModel` 判 `ConverterBlock` | 判 `CropConverterBlock`（漏改 ⇒ 物品落 `basicItem` ⇒ 紫黑格） | BRIEF §5 第 16 项 |
| 4 | `ModBlockTagsProvider.java:45` | `PROCESSING_BLOCK` 只有 `CULTIVATOR` | 加 `CROP_CONVERTER`（两个成员） | D-208、plan `:11` 的口径差异（§10 第 9 条） |
| 5 | `ModBlockTagsProvider.java:48` | `MINEABLE_WITH_PICKAXE` 里有 `CONVERTER` | 改 `CROP_CONVERTER`（铁砧硬度 5.0 ⇒ 必须进这条，否则木镐挖不出掉落物） | D-207 |
| 6 | `ModAdvancementProvider.java:82-85` | `placedBlock(FarmstormBlocks.CONVERTER.get())`、图标 `FarmstormItems.CONVERTER.get()` | 换 `CROP_CONVERTER`；**进度 id 仍是 `farmstorm:converter`** | D-214 补充 |
| 7 | `Farmstorm.java:180-183` | `total == 98` / `items == 34` / `blocks == 27` / `menuTypes == 2` | `92` / `28` / `28` / `1`，其余 8 个 `==` 一字不动 | D-205 |
| 8 | `tools/verify_project.py:334-393` | `EXPECTED` 12 组 id 清单 + `EXPECTED_COUNTS` 12 个计数 | 删 7 个种子、删 `menu_type` 的 `converter`、加 `crop_converter` / `plain_chives_crop` / `plain_chives_seeds`；计数改 28 / 28 / 1 | D-205 |

### 2.5 必改清单逐文件落点（`BRIEF` §5 的 19 项 → 本节的哪一条）

| `BRIEF` §5 的项 | 本文落点 |
|---|---|
| 1 / 3 / 6（方块、物品、方块实体三处改名） | §2.1、§2.1.1 |
| 2 / 4 / 5（两个新增 + 创造栏同步） | §2.2、§2.3、§2.4 |
| 7 / 8（`MenuType` 与三个整删文件） | §2.3 第 8 行、§2.6 |
| 9 / 10 / 11（两个类改写 + 配方字段收缩） | §4.1、§4.2.1 |
| 12 / 13（配方与 config） | §4.2.2、§6 |
| 14 / 15（进度与战利品） | §2.3 的进页面与战利品面 |
| 16 / 17 / 18 / 19（资源、语言、标签、自检） | §3、§7、§2.3 的标签面、§2.4 第 7~8 行 |

### 2.6 三个整删文件与 `FarmstormClient` 的接线

| # | 文件 | 行数（实测） | 删法 | 连带 |
|---|---|---|---|---|
| 1 | `menu/ConverterMenu.java` | **216** | 整文件删 | `FarmstormMenus:17-18`、`ConverterBlock:70-76`、`FarmstormClient:62` 三处引用同批清掉 |
| 2 | `client/ConverterScreen.java` | **86** | 整文件删 | `FarmstormClient:62` 的 `event.register(...)` 与顶部 import |
| 3 | `client/ConverterRecipeBookCategory.java` | **24** | 整类删，**但把那一行注册内联进 `FarmstormClient`** | 见下 |

**为什么第 3 个要内联而不是全删**：那个类的唯一职责是消掉客户端 `Unknown recipe category: farmstorm:converting/<id>` 警告（类注释 `:8-11` 记着机制：vanilla 认不出的 `RecipeType` 走 `RecipeBookManager.findCategories`，没注册 finder 就 `LOGGER.warn` 一条）。`converting` 类型本版**保留**（D-209），21 条配方仍在 ⇒ 全删会在客户端刷 **21 条** WARN。做法：把 `FarmstormClient:73-76` 的 handler 体改成一行内联

```
event.registerRecipeCategoryFinder(FarmstormRecipes.CONVERTING_TYPE.get(), holder -> RecipeBookCategories.UNKNOWN);
```

语义与 `ConverterRecipeBookCategory:22` 逐字相同，**类删了、警告仍然不出现**。`FarmstormClient:62` 那行（屏幕注册）与顶部两个 import 直接删。BRIEF §5 第 8 项写「`:62,75` 两行同步删」——`:75` 那行改为内联而不是删，差异登记在 §10 第 7 条。

---
## 3. 资源面

### 3.1 新机器的贴图落位（7 个文件 + 3 个 mcmeta）

命名沿用培养器的 `_a_` 中缀与 `_still` 后缀（培养器是 `cultivator_a_{top,side,bottom}` + 两个 `_still`，`ModBlockStateProvider:210-217`）。本机器的贴图包是「白钢双舱」的四视图，比培养器多一个**正面**。**plan 说的「翻书动画」就是贴图包里的逐帧动画**：正面 / 顶面 / 侧面各 16 帧、`32×512` 纵向帧条、`frametime: 2`、`interpolate: false`、1.6 秒一循环；素材与措辞一致，`working=true` 引用帧条、`working=false` 引用 `_still` 静态副本（D-211）。

| # | 文件（`assets/farmstorm/textures/block/`） | 动 / 静 | 用在哪个面 | mcmeta |
|---|---|---|---|---|
| 1 | `crop_converter_a_front.png` | **动画 16 帧** | 正面（`north`） | **有**（`frametime: 2`） |
| 2 | `crop_converter_a_front_still.png` | 静态 | 空闲态的正面 | 无 |
| 3 | `crop_converter_a_side.png` | **动画 16 帧** | 另外三个侧面（`south` / `east` / `west`） | **有**（`frametime: 2`） |
| 4 | `crop_converter_a_side_still.png` | 静态 | 空闲态的侧面 | 无 |
| 5 | `crop_converter_a_top.png` | **动画 16 帧** | 顶面（`up`） | **有**（`frametime: 2`） |
| 6 | `crop_converter_a_top_still.png` | 静态 | 空闲态的顶面 | 无 |
| 7 | `crop_converter_a_bottom.png` | 静态 | 底面（`down`），工作与否都不变 | 无 |

三个 `.mcmeta` 的内容逐字相同（只放帧序参数，不写 `interpolate`）：

```
{ "animation": { "frametime": 2 } }
```

**节奏核对**：16 帧 × `frametime 2` = **32 刻**一循环（1.6 秒）；`processTicks = 600` 刻里播 `600 ÷ 32 = 18.75` 个循环。**不整除是正常的**——到点直接切模型，不需要对齐动画，也不需要为「不整除」加任何代码（D-211 补充）。

**底面为什么没有 mcmeta**：它在两张模型里指向同一个文件，工作与否都不动；给它加 mcmeta 只会让「空闲态也必须有一张静态副本」这条规则多一处例外。

### 3.2 blockstate：8 个 variant（`FACING` × `WORKING`）

| # | `facing` | `working` | 模型 | `y` | `uvlock` |
|---|---|---|---|---|---|
| 1 | `north` | `false` | `farmstorm:block/crop_converter` | 0 | `true` |
| 2 | `north` | `true` | `farmstorm:block/crop_converter_working` | 0 | `true` |
| 3 | `east` | `false` | `farmstorm:block/crop_converter` | 90 | `true` |
| 4 | `east` | `true` | `farmstorm:block/crop_converter_working` | 90 | `true` |
| 5 | `south` | `false` | `farmstorm:block/crop_converter` | 180 | `true` |
| 6 | `south` | `true` | `farmstorm:block/crop_converter_working` | 180 | `true` |
| 7 | `west` | `false` | `farmstorm:block/crop_converter` | 270 | `true` |
| 8 | `west` | `true` | `farmstorm:block/crop_converter_working` | 270 | `true` |

**数量核对**：`4 个朝向 × 2 个状态 = 8`。生成方式沿用 `ModBlockStateProvider:179-183` 的 `getVariantBuilder(...).forAllStates(...)` + `rotationY(facingRotationY(...))` + `uvLock(true)`，只多一个 `.with(CropConverterBlock.WORKING, …)` 的分派。**不能用 `simpleBlock(block, model)`**：它只生成单态，两态的方块会有一个 variant 找不到模型（培养器那次的同一条教训，`ModBlockStateProvider:204-206`）。

**两张模型的父模型取 `minecraft:block/cube`**（六面显式），理由：`cube_bottom_top` 只有 `top` / `side` / `bottom` 三个槽位，装不下「正面」，而现有转换器模型用的正是 `cube`（生成产物 `models/block/converter.json` 的 `parent` 就是 `minecraft:block/cube`）。

| 面 | 空闲模型 `crop_converter` | 工作模型 `crop_converter_working` |
|---|---|---|
| `north`（正面） | `farmstorm:block/crop_converter_a_front_still` | `farmstorm:block/crop_converter_a_front` |
| `south` / `east` / `west` | `farmstorm:block/crop_converter_a_side_still` | `farmstorm:block/crop_converter_a_side` |
| `up` | `farmstorm:block/crop_converter_a_top_still` | `farmstorm:block/crop_converter_a_top` |
| `down` | `farmstorm:block/crop_converter_a_bottom` | `farmstorm:block/crop_converter_a_bottom` |
| `particle` | `farmstorm:block/crop_converter_a_front_still` | `farmstorm:block/crop_converter_a_front` |

**「只有工作时才动」的实现边界（红线 4）**：静态副本**没有 mcmeta**，所以模型一引用它就是定格；动态 PNG 一旦被引用就会自己播，`frametime` 是贴图元数据，代码碰不到它。**不许**写任何「暂停 / 恢复纹理动画」的代码——两张模型就是唯一正解（D-211 补充，陷阱 27）。

### 3.3 item model

| 项 | 值 | 依据 |
|---|---|---|
| 文件 | `src/generated/resources/assets/farmstorm/models/item/crop_converter.json` | datagen 产物 |
| 内容 | `{ "parent": "farmstorm:block/crop_converter" }` | 与 `converter` 现有产物逐字同形 |
| 生成路径 | `ModItemModelProvider` 的 `usesBlockModel(...)` 分支（`:45-46`） | `ModItemModelProvider.java:68-70` 加 `CropConverterBlock` |
| 漏改的后果 | 落进 `basicItem(item)`（`:55-57`）⇒ 找一张**不存在**的 `textures/item/crop_converter.png` ⇒ 紫黑格 | `ModItemModelProvider:61-66` 的注释记过这条 |

### 3.4 旧贴图删除清单

`assets/farmstorm/textures/block/` 下现有 **7 张** `converter*.png`（目录实测），本版全删：

| # | 文件 | 0.2.2 时是否被模型引用 | 处置 |
|---|---|---|---|
| 1 | `converter_front.png` | **否**（孤儿文件） | 删 |
| 2 | `converter_side.png` | **否**（孤儿文件） | 删 |
| 3 | `converter_side_wheat.png` | 是（`north` + `particle`） | 删 |
| 4 | `converter_side_carrot.png` | 是（`east`） | 删 |
| 5 | `converter_side_potato.png` | 是（`south`） | 删 |
| 6 | `converter_side_beetroot.png` | 是（`west`） | 删 |
| 7 | `converter_top.png` | 是（`up` + `down`） | 删 |

`BRIEF` 与 52 号 §3.3 第 12 项都写「6 张旧转换器贴图」，**实测是 7 张**（5 张被现模型引用 + 2 张孤儿）。差异登记在 §10 第 3 条；§8 #22 的验收判据按 7 张写（`converter_*.png` 命中 0）。

**为什么不重命名复用**：贴图包是全新的四视图（`front` / `side` / `top` / `bottom`），与旧的三面 + 四种作物侧面没有任何一一对应关系；硬套会得到一张「正面是麦子、侧面是胡萝卜」的机器。

### 3.5 `plain_chives_crop` 的 4 阶段 blockstate（复用朋友的韭贴图）

| 项 | 值 | 依据 |
|---|---|---|
| blockstate | 4 个 variant：`age=0..3` | 与 15 个作物同形（`ModBlockStateProvider:101-105`） |
| 模型名 | `plain_chives_stage0` ~ `plain_chives_stage3` | `cropBase()` 去掉 `_crop` 后缀（`ModBlockStateProvider:34`） |
| 模型父 | `minecraft:block/crop` + `renderType = minecraft:cutout` | 与 `cropStates` 逐字同形（`:97-99`） |
| 贴图 | **显式** `farmstorm:block/friend_chives_stage0` ~ `_stage3` | 裁定 18 |
| 新增美术资源 | **0 张** | 同上 |

**必须单开一个 `plainCropStates(...)` 方法，不能走 `cropStates(...)`**：后者按「模型名 = 贴图名」自动拼（`models().withExistingParent(stageName, …).texture("crop", modLoc("block/" + stageName))`，`:97-98`），对 `plain_chives_crop` 会拼出 `block/plain_chives_stage0`——**那张图不存在**（有的是 `friend_chives_stage0`）。单开方法后贴图路径写成 `modLoc("block/friend_chives_stage" + age)`。

**必须加一条 `instanceof PlainCropBlock` 分支**（`ModBlockStateProvider:70-88` 的 if 链）：
- 漏加 ⇒ 落进兜底的 `simpleBlock(block)`（`:83-87`）⇒ 生成一个六面同贴图的立方体模型，**不报错**、游戏里是一个方块形状的韭菜（BRIEF §7 第 5 项记的正是这个陷阱）；
- 顺序上放在 `FarmstormCropBlock` 分支**之后**、`DeployedCropBlock` 分支之前即可（`PlainCropBlock` 继承 `CropBlock`，与 `FarmstormCropBlock` 无继承关系，不会互相命中）。

**掉落表**：新增 `plain_chives_crop` 一张，产 1 个 `plain_chives` + 0~3 个 `plain_chives_seeds`（参数照膨胀麦那条二项分布，`ModLootTableProvider:70-71`）。`plain_chives` 是食物、也是种子的产物，两者都在 §1.2 的 28 个物品里。

### 3.6 `textures/item/` 这一面（29 → 22）

`assets/farmstorm/textures/item/` 现有 **29** 张，本版动 **8 个文件**：

| # | 文件 | 动作 | 理由 |
|---|---|---|---|
| 1 | `chive_seeds.png` → `plain_chives_seeds.png` | **重命名**（不计增删） | 同一个播种物换了归属；`plain_chives_seeds` 走 `basicItem`，**必须有这张图**，否则紫黑格（`ModItemModelProvider:55-57`） |
| 2 | `puffed_wheat_seeds.png` | 删 | 物品已删 |
| 3 | `power_beet_seeds.png` | 删 | 同上 |
| 4 | `melon_grenade_seeds.png` | 删 | 同上 |
| 5 | `pumpkin_turret_seeds.png` | 删 | 同上 |
| 6 | `awakened_torchflower_seeds.png` | 删 | 同上 |
| 7 | `awakened_pitcher_plant_pod.png` | 删 | 同上 |
| 8 | `converter.png` | 删 | **本来就是死文件**：转换器的物品模型走 `modLoc("block/…")`（`ModItemModelProvider:45-46,68-70`），这张图从未被引用 |

计数：`29 − 7 删 + 0 增 = 22`（重命名不算增删）。**验收口径**：`Get-ChildItem src\main\resources\assets\farmstorm\textures\item -File` 计数 = **22**，且 `plain_chives_seeds.png` 存在、`chive_seeds.png` 与 `converter.png` 不存在。

### 3.7 `textures/gui/`

| 文件 | 现状 | 处置 | 依据 |
|---|---|---|---|
| `gui/converter.png` | 存在（`textures/gui/` 下唯一一张） | **删** | 图形界面整套删除（裁定 2、D-208） |

删完 `textures/gui/` 为空目录，NetBeans / Gradle 不会为空目录生成 jar 条目，**不需要**额外保留 `.gitkeep`。

### 3.8 资源面计数核对

| 项 | 0.2.2 实测 | 增 | 减 | v0.2.3 |
|---|---|---|---|---|
| `textures/block/*.png` | **84** | +7 | −7 | **84** |
| `textures/block/*.mcmeta` | **2** | +3 | 0 | **5** |
| `textures/item/*.png` | **29** | 0 | −7 | **22** |
| `textures/gui/*.png` | **1** | 0 | −1 | **0** |
| `blockstates/` 的 json | **27** | +2（`crop_converter` / `plain_chives_crop`） | −1（`converter`） | **28** |
| `models/block/` 的 json | **73** | +6（`crop_converter` / `_working` + `plain_chives_stage0..3`） | −1（`converter`） | **78** |
| `models/item/` 的 json | **34** | +1（`plain_chives_seeds`） | −7（种子；`crop_converter` 是改名） | **28** |

`ModBlockStateProvider:28` 的类注释写「20 个 blockstate + 62 个方块模型」，实测是 **27 / 73**——过期数字，本版顺手改对；验收只判 §8 #22 的文件存在性与 `converter.json` 不存在。

---

## 4. 行为规格

### 4.1 转换器状态机

#### 4.1.1 方块类 `CropConverterBlock`

| # | 成员 | 形态 | 与培养器的对应行 |
|---|---|---|---|
| 1 | `FACING` | `BlockStateProperties.HORIZONTAL_FACING`（水平四向） | 转换器既有（`ConverterBlock:35`） |
| 2 | `WORKING` | `BooleanProperty.create("working")` | `CultivatorBlock:44` |
| 3 | `codec()` | `simpleCodec(CropConverterBlock::new)` | `ConverterBlock:36,44-46` |
| 4 | `getRenderShape` | `RenderShape.MODEL`（`BaseEntityBlock` 默认 `INVISIBLE`） | `CultivatorBlock:59-61` |
| 5 | `createBlockStateDefinition` | `builder.add(FACING, WORKING)` | 两个属性的并集 |
| 6 | `getStateForPlacement` | `defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite())` | `ConverterBlock:61-63` |
| 7 | `newBlockEntity` | `new CropConverterBlockEntity(pos, state)` | — |
| 8 | `setPlacedBy` | 照 `CultivatorBlock:86-102`：`selectCore(...)` → `core.addCultivator(pos, Kind.CROP_CONVERTER)` → `converter.setCore(core.getBlockPos())` | `CultivatorBlock:86-102` |
| 9 | `useWithoutItem` | 客户端先 `SUCCESS`；服务端四闸合并成一次判定，不过则 `PASS`，过则 `split(1)` + `CONSUME` | `CultivatorBlock:113-128` |
| 10 | `onRemove` | 真移除 ∧ 非活塞 ⇒ `Containers.dropContents(level, pos, converter.releaseHeld())` | `CultivatorBlock:140-147` |
| 11 | `getTicker` | 客户端返回 `null`；服务端转 `CropConverterBlockEntity.serverTick` | `CultivatorBlock:150-161` |
| 12 | `getMenuProvider` | **删掉**（`ConverterBlock:70-76` 整段）。没有界面 = 没有「打开界面」这条路径，`MenuType` 也删了 | 裁定 2 |

**不实现的接口**：**不实现 `Container`**（D-208 补充）。一旦实现，漏斗 / 投掷器就能绕过 `useWithoutItem` 塞东西进来，四道闸当场失守——与培养器同一条理由（`CultivatorBlockEntity:34-35` 的类注释）。

#### 4.1.2 方块实体 `CropConverterBlockEntity`：四步状态机

字段只有三个（对应培养器的 `Held` / `Progress` / `Core`，`CultivatorBlockEntity:41-53`）：

| 字段 | NBT 键 | 类型 | 语义 |
|---|---|---|---|
| `held` | `Held` | `ItemStack#save` / `ItemStack#parse` | 槽里那**一个**待转换物品 |
| `progress` | `Progress` | `int` | 已累计刻数 |
| `corePos` | `Core` | `long`（`BlockPos#asLong`），**缺键 = 未绑定** | 绑定核的引用缓存（真相在核侧） |

**服务端每刻四步**（与 `CultivatorBlockEntity:176-213` 逐行同形，只把产出函数与阈值换掉）：

```
第 1 步：槽空 ⇒ progress 归零、working=false、return
第 2 步：!isUsable() ⇒ working=false、return（不清槽、不清进度）
第 3 步：progress < processTicks ⇒ progress++、setChanged、working=true、return
第 4 步：progress == processTicks ⇒ deliver()
        成功 ⇒ 清槽、progress=0、working=false
        失败 ⇒ 槽与 progress 都保留、working=false（下一 tick 再走第 4 步）
```

**与培养器的唯一差别**：`deliver()` 投递的是**一件**（转换配方的 `result`，数量 1），不是 `batch(profile)` 的两组（`CultivatorBlockEntity:216-239`）。契约仍走核侧的 `acceptAll(List<ItemStack>)`（`OutpostCoreBlockEntity:151-165`）——**一次一个**也走整批接口，理由是那条接口已经保证「先算后写、失败一个都不写」，自己再写一份单件判定就是第二份副本。

**阈值**：`FarmstormConfig.cropConverterProcessTicks.get()`（新键，默认 600 = 30 秒，§6）。

**`working` 的写法**：只在值真变化时写一次 blockstate，且带 `UPDATE_CLIENTS`（照 `CultivatorBlockEntity:252-261`）；**不许**每刻 `setBlock`。

#### 4.1.3 四道准入闸

| # | 闸 | 判据 | 与培养器的差异 |
|---|---|---|---|
| 1 | **范围闸** | `OutpostCoreBlockEntity.inRange(corePos, pos)`（3 × 3 区块，`RANGE_CHUNKS = 1`，`:76`、`:466-471`） | 完全相同 |
| 2 | **绑定闸** | `corePos != null` ∧ 核方块实体在 ∧ `core.hasCultivator(pos)`（核侧列表是唯一真相） | 完全相同 |
| 3 | **手持闸** | `stack` 是**某一条 `farmstorm:converting` 配方的输入**（21 条里任一）。**包含** 4 条史诗输入（火把花 / 瓶子草荚 / 甜浆果 / 金苹果） | **反转的那一条**：培养器的闸是「21 − 4 = 17」（§4.3），转换器是**全 21**。若两者共用一个判据，史诗作物将永远产不出来 |
| 4 | **空槽闸** | `held.isEmpty()` | 完全相同 |

四闸**合并成一次判定**（`canInsert(stack)` 一个方法，照 `CultivatorBlockEntity:147-149`），任一不过 ⇒ `PASS`（不消耗、不提示、不报错）。

**手持闸的实现形态**：不要在方块实体里手写 21 个 `ItemStack.is(...)`。用配方表的只读视图：`level.getRecipeManager().getRecipeFor(FarmstormRecipes.CONVERTING_TYPE.get(), new SingleRecipeInput(stack), level)` 命中即通过。**这样做的两个后果**（都是期望行为）：
1. 数据包新增一条 `converting` 配方，转换器自动多收一个输入物；
2. 22 条档案里没有转换配方的那一条（`friend_chives` 是**输出**侧，不是输入侧）不影响判据。

> 这条与 D-218 的「判据只有一个来源」是同一条纪律：**配方表说什么能转换，转换器就收什么**；培养器则是「配方表里输出不是史诗档的那 17 条」。

#### 4.1.4 三种边界（逐条照抄培养器先例）

| 边界 | 行为 | 落点 | 依据 |
|---|---|---|---|
| **拆方块** | 四条件（真移除 `!state.is(newState.getBlock())` ∧ 非活塞 `!movedByPiston`）成立 ⇒ 槽里那**一个**原样掉落，`progress` 归零，避免同一份内容掉两次；方块本体由战利品表掉，两处不重叠 | `CropConverterBlock#onRemove` → `converter.releaseHeld()` | 照 `CultivatorBlock:140-147` / `CultivatorBlockEntity:282-296` |
| **核满** | 产物**不丢**：槽与 `progress` 都保留，`progress` 停在 `processTicks` **不清零**，`working` 落回 `false`；核腾出空间的那一 tick 自动完成投递（不重新计时） | `serverTick` 第 4 步的失败分支 | 照 `CultivatorBlockEntity:208-212`、D-208 第 2 条 |
| **绑定失效**（核被拆 / 核侧列表里没有它） | **留原地、内容保留、不可使用**：不清槽、不清进度、`working = false`、不 tick；核侧迁移失败时由 `settleOnRemoval` 清 `Core` 键（出口②），转换器这一侧只读不写 | 第 2 步 + 核侧 `OutpostCoreBlockEntity:750-795` | D-208 第 4 条 |

**第四条边界（不算「三种」之列，但同批钉死）**：**工作中右键无反应**——空手右键不取出，取回只能拆方块（`CultivatorBlock:111` 的同一条）。四种情形的对外表现都是 `PASS`。

#### 4.1.5 转换器不接的三样东西

| 项 | 结论 | 依据 |
|---|---|---|
| 红石 / 比较器 | **不接**：不覆写 `hasAnalogOutputSignal` / `getAnalogOutputSignal`，不读红石信号 | BRIEF §4.1 |
| 漏斗 / 投掷器 | **塞不进**：不实现 `Container` | D-208 补充 |
| 经验 / 催化剂 / 双向 | **全删**：`ConverterRecipe` 只留 `input` / `result` | D-209 |

#### 4.1.6 进度 `first_conversion` 的授予点

现状在 `ConverterBlockEntity:255` 由「玩家点转换按钮」授予；新机器没有界面，**授予点必须搬**，否则这条进度不可达（它的判据是 `impossible` 触发器，`ModAdvancementProvider:88`）。

**本文确定**：在 `deliver()` 成功那一支授予**绑定核的归属者**（`core.owner()`，`OutpostCoreBlockEntity:435-442`，离线返回 `null` 时跳过）。理由：新机器没有「操作者」这个角色，唯一稳定的主体是核的归属者；`FarmstormAdvancements.award` 对已授予的重复调用由原版忽略（`FarmstormAdvancements:19` 的注释）。`nether_star_toll` 的授予点（`ConverterBlockEntity:257`）随进度一起删。

### 4.2 转换配方 21 条

#### 4.2.1 `ConverterRecipe` 的字段收缩

| 项 | 现状（`ConverterRecipe.java`） | 改后 |
|---|---|---|
| 字段 | `input` / `result` / `xpCost` / `catalyst` / `catalystCount` / `bidirectional` / `reverseXpCost`（`:21-25`） | **`input` / `result`**（2 个） |
| 方法 | 含 `reverse()`（`:106-110`） | **删** `reverse()` 与五个 getter |
| 序列化器 | `ConverterRecipeSerializer.java:17-25` 的 7 字段 `MapCodec`、`:33-52` 的手写 `StreamCodec` | 2 字段 `MapCodec`；`StreamCodec` 只剩两段，**可以**改用 `StreamCodec.composite`（两参重载） |

**兼容口径（D-209 补充，必须写成已知限制）**：`converting` 的 JSON schema 变了，**带 `xp_cost` / `catalyst` / `catalyst_count` / `bidirectional` / `reverse_xp_cost` 五个字段的旧数据包配方会读失败**。本版**不写迁移、不写向后兼容 codec**——本模组没有发布过带 `converting` 配方的外部数据包。验收侧只需确认「本仓生成的 21 个 JSON 能被自己读回」（§8 #12）。

#### 4.2.2 `ModRecipeProvider` 的改法

| # | 位置 | 现状 | 改成 |
|---|---|---|---|
| 1 | `:60-61` 的 `converter(recipeOutput)` 调用 | 生成 8 燃料 + 工作台的配方 | 保留调用、**换方法体**：6 铁锭 + 2 玻璃 + 1 红石粉的有序 3×3 |
| 2 | `:86-92` 的 6 条种子 `twoWay(...)` | 6 条 | **整段删** |
| 3 | `:187-196` 的 `twoWay(...)` 助手 | 7 参构造（`XP_COST` / `Ingredient.EMPTY` / `NO_CATALYST` / `true` / `XP_COST`） | 2 参构造（`input` / `result`），方法名建议改 `oneWay(...)`（名字与「只留正向」对齐） |
| 4 | `:199-209` 的 `oneWayWithNetherStar(...)` | 收 1 个下界之星 | 改 `oneWay(...)`，催化剂整段删（D-209 补充第 1 条：`Ingredient.of(NETHER_STAR)` → 无、`NETHER_STAR_COST` → 无） |
| 5 | 新增 | — | 加一条 `oneWay(recipeOutput, "friend_chives", FarmstormItems.PLAIN_CHIVES.get(), FarmstormItems.FRIEND_CHIVES)`，配方 id 用 `farmstorm:friend_chives`（与输出同名，照既有 26 条的 id 规则） |
| 6 | 新增 | — | 加培养器的有序 3×3：`铁锭 玻璃 铁锭 / 玻璃 红石粉 玻璃 / 铁锭 玻璃 铁锭` ⇒ 4 铁 + 4 玻璃 + 1 红石 → 1 个 `farmstorm:cultivator`；`unlockedBy("has_glass", has(Items.GLASS))`（本文确定，§0.3 第 13 项） |

**转换器的合成配方（6 铁 + 2 玻璃 + 1 红石）**逐格：`铁 铁 铁 / 玻璃 红石 玻璃 / 铁 铁 铁`（D-206 补充、BRIEF §5 第 12 项），分类 `RecipeCategory.DECORATIONS`（与既有转换器配方同类），id 建议随方块改 `farmstorm:crop_converter`。

**配方文件总数**：现状 **29**（26 `converting` + `converter` / `outpost_core` / `compressed_puffed_wheat` 三条合成）；改后 `29 − 6 + 2 = 25`（21 `converting` + `crop_converter` / `outpost_core` / `compressed_puffed_wheat` / `cultivator` 四条合成）。验收按 `data/farmstorm/recipe/` 的文件数 = **25** 判（§8 #10）。

#### 4.2.3 21 条逐条列全（输入 → 输出）

顺序按 `ModRecipeProvider` 的书写顺序（`:67-99` + 新增那条）；「档」列取输出档案的 `tier()`（`CropProfile.java`）；「培养器」列标该输入能否被培养器收（§4.3）。

| # | 输入（原版 / 平凡） | 输出（改良作物） | 档 | 培养器可收 | 出处 |
|---|---|---|---|---|---|
| 1 | 小麦 `minecraft:wheat` | `farmstorm:puffed_wheat` 膨胀麦 | 普通 | ✅ | `ModRecipeProvider:67` |
| 2 | 胡萝卜 `minecraft:carrot` | `farmstorm:blast_carrot` 胡罗贝 | 普通 | ✅ | `:68` |
| 3 | 马铃薯 `minecraft:potato` | `farmstorm:ubi_server` 育碧服务器 | 稀有 | ✅ | `:69` |
| 4 | 毒马铃薯 `minecraft:poisonous_potato` | `farmstorm:toxic_ubi_server` 毒育碧服务器 | 稀有 | ✅ | `:70` |
| 5 | 甜菜根 `minecraft:beetroot` | `farmstorm:power_beet` 甜菜！ | 稀有 | ✅ | `:71` |
| 6 | 西瓜片 `minecraft:melon_slice` | `farmstorm:melon_slice_bomb` 瓜片子 | 普通 | ✅ | `:72` |
| 7 | 西瓜 `minecraft:melon` | `farmstorm:melon_grenade` 瓜！ | 普通 | ✅ | `:73` |
| 8 | 南瓜 `minecraft:pumpkin` | `farmstorm:pumpkin_turret` man瓜 | 普通 | ✅ | `:74` |
| 9 | 火把花 `minecraft:torchflower` | `farmstorm:awakened_torchflower` 激活的火把花 | **史诗** | ❌（史诗排除） | `:76` |
| 10 | 瓶子草荚 `minecraft:pitcher_plant` | `farmstorm:awakened_pitcher_plant` 激活的瓶子草 | **史诗** | ❌（史诗排除） | `:77-78` |
| 11 | 仙人掌 `minecraft:cactus` | `farmstorm:fang_trap_cactus` 刺球 | 普通 | ✅ | `:80` |
| 12 | 竹子 `minecraft:bamboo` | `farmstorm:bamboo_firecracker` 爆竹 | 普通 | ✅ | `:81` |
| 13 | 红蘑菇 `minecraft:red_mushroom` | `farmstorm:red_mushroom_puff` ？！菇菇！？ | 普通 | ✅ | `:82` |
| 14 | 棕蘑菇 `minecraft:brown_mushroom` | `farmstorm:brown_mushroom_puff` ？！棕色菇菇色棕！？ | 普通 | ✅ | `:83` |
| 15 | 甜浆果 `minecraft:sweet_berries` | `farmstorm:lucky_berry` 幸运浆果 | **史诗** | ❌（史诗排除） | `:84` |
| 16 | 绯红菌 `minecraft:crimson_fungus` | `farmstorm:molotov` 莫洛托夫！ | 普通 | ✅ | `:94` |
| 17 | 甘蔗 `minecraft:sugar_cane` | `farmstorm:zangief_cane` 桑吉尔蔗 | 稀有 | ✅ | `:95` |
| 18 | 金苹果 `minecraft:golden_apple` | `farmstorm:wild_apple` 狂野苹果 | **史诗** | ❌（史诗排除） | `:96` |
| 19 | 诡异菌 `minecraft:warped_fungus` | `farmstorm:point_defense_fungus` 近防菌 | 稀有 | ✅ | `:98` |
| 20 | 可可豆 `minecraft:cocoa_beans` | `farmstorm:vibration_bean` 震动豆 | 普通 | ✅ | `:99` |
| 21 | 平凡韭菜 `farmstorm:plain_chives` | `farmstorm:friend_chives` 朋友的韭 | 稀有 | ✅ | **新增**（D-217） |

**四个史诗档输出**（第 9 / 10 / 15 / 18 行）：`awakened_torchflower` / `awakened_pitcher_plant` / `lucky_berry` / `wild_apple`，与 D-218 补充的 4 条史诗输入一一对应。**删掉下界之星门槛后，这四条的代价只剩 30 秒**——这是裁定 2 的直接后果，53 号写成「已知影响」，验收侧不额外加门槛（§8 #13 的产出判据本身就是「无催化剂、无经验」的证明）。

**21 条的三个不变量**：

1. **全部单向**：`bidirectional` 字段删掉之后，`改良 → 原版` 的方向**不存在**（D-209）。`ConverterRecipe#matches` 只比输入。
2. **全部无成本**：不收经验、不收催化剂；`result` 的 count 恒为 **1**（`plan0.2.3.txt:12` 的「产出 1 个」）。
3. **id 规则不变**：配方 id 取输出物的 path（`Farmstorm.id(path)`，`:188`），因此 21 个 JSON 的文件名就是 21 个输出的名字 + 新增的 `friend_chives`。

### 4.3 培养器：17 个投入物

#### 4.3.1 准入判据的反转

| 项 | 现状（`CultivatorBlockEntity.java`） | 改后 |
|---|---|---|
| `admissible(ItemStack)`（`:138-144`） | `profileOf(stack) != null && profile.tier() != EPIC` ⇒ **只收带档案的物品**，原版小麦进不去 | **是 21 条转换配方的输入** ∧ **该配方的输出档案 `tier() != EPIC`** ⇒ 改良作物**一个都不收** |
| `profileOf(ItemStack)`（`:151-165`） | 物品 → 档案（三个物品类各给一个 `profile()`） | 职责改为「输入物品 → 找到它那条配方的输出 → 输出的档案」，**方法名必须改**（`outputProfileOf`），不能让一个叫 `profileOf` 的方法去查配方（D-218 补充） |
| `deliver()`（`:216-227`） | 读 `profileOf(held)` | 读 `outputProfileOf(held)`，失败时物品留槽（不可达分支保留） |
| `_comment`（`:131-137`） | 「普通与稀有档放得进，史诗档一律拒绝」 | 整段重写为「只收转换配方的输入，且输出非史诗」 |

**两个判据的差集就是「史诗排除」这 4 个**：`awakened_torchflower` / `awakened_pitcher_plant` / `lucky_berry` / `wild_apple` 对应的输入（火把花 / 瓶子草荚 / 甜浆果 / 金苹果）被排除，算式 `21 − 4 = 17`。

#### 4.3.2 17 个投入物逐条列表

「对应种子」按 D-219 + 52 号 §4.3 的口径分两档：**有独立种子物品的 6 个**给 2 + 2；**其余 11 个**的对应种子就是它自己，给 **4 个同一物品**（裁定 #16）。

| # | 投入物 | 对应种子 | 单次产出（`cultivator.yield = 2`） | 档 |
|---|---|---|---|---|
| 1 | 小麦 `minecraft:wheat` | 小麦种子 `minecraft:wheat_seeds` | 2 小麦 + 2 小麦种子 | 2 + 2 |
| 2 | 胡萝卜 `minecraft:carrot` | **它自己** | **4 胡萝卜** | 4 同一物品 |
| 3 | 马铃薯 `minecraft:potato` | **它自己** | **4 马铃薯** | 4 同一物品 |
| 4 | 毒马铃薯 `minecraft:poisonous_potato` | **它自己** | **4 毒马铃薯** | 4 同一物品 |
| 5 | 甜菜根 `minecraft:beetroot` | 甜菜种子 `minecraft:beetroot_seeds` | 2 甜菜根 + 2 甜菜种子 | 2 + 2 |
| 6 | 西瓜片 `minecraft:melon_slice` | 西瓜种子 `minecraft:melon_seeds` | 2 西瓜片 + 2 西瓜种子 | 2 + 2 |
| 7 | 西瓜 `minecraft:melon` | 西瓜种子 `minecraft:melon_seeds` | 2 西瓜 + 2 西瓜种子 | 2 + 2 |
| 8 | 南瓜 `minecraft:pumpkin` | 南瓜种子 `minecraft:pumpkin_seeds` | 2 南瓜 + 2 南瓜种子 | 2 + 2 |
| 9 | 仙人掌 `minecraft:cactus` | **它自己** | **4 仙人掌** | 4 同一物品 |
| 10 | 竹子 `minecraft:bamboo` | **它自己** | **4 竹子** | 4 同一物品 |
| 11 | 红蘑菇 `minecraft:red_mushroom` | **它自己** | **4 红蘑菇** | 4 同一物品 |
| 12 | 棕蘑菇 `minecraft:brown_mushroom` | **它自己** | **4 棕蘑菇** | 4 同一物品 |
| 13 | 甘蔗 `minecraft:sugar_cane` | **它自己** | **4 甘蔗** | 4 同一物品 |
| 14 | 绯红菌 `minecraft:crimson_fungus` | **它自己** | **4 绯红菌** | 4 同一物品 |
| 15 | 诡异菌 `minecraft:warped_fungus` | **它自己** | **4 诡异菌** | 4 同一物品 |
| 16 | 可可豆 `minecraft:cocoa_beans` | **它自己** | **4 可可豆** | 4 同一物品 |
| 17 | 平凡韭菜 `farmstorm:plain_chives` | 平凡韭菜种子 `farmstorm:plain_chives_seeds` | 2 平凡韭菜 + 2 平凡韭菜种子 | 2 + 2 |

**两档计数**：`6 + 11 = 17`；有独立种子的 6 条 = 第 1 / 5 / 6 / 7 / 8 / 17 行；自身即种子的 11 条 = 第 2 / 3 / 4 / 9 / 10 / 11 / 12 / 13 / 14 / 15 / 16 行。

**三条实现约束**：

1. **毒马铃薯的对应种子是它自己**，不是马铃薯。这一点在 52 号的早期草稿里出现过相反写法，本文按 D-219 的最终口径（自身即种子那一档）落。验收见 §8 #17。
2. **`cultivator.yield` 一个键管两组**（`FarmstormConfig:966`，默认 2），不拆成两个键——两组数量在本版永远相等（D-219 补充）。
3. **`hasOwnSeed` 的判据要改**：现状是 `seedItem != null && !seedItem.location().equals(profile.id())`（`:246-249`），对「自身即种子」的 11 条只给一组；本版改成「有独立种子物品就给两组、否则给两组同一物品」，也就是**永远两组**。产出面因此从「5 条给 2 + 2、13 条只给 2」变成「6 条 2 + 2、11 条 4」——**这是本版唯一一处产出翻倍**，53 / 55 号要写成平衡变更，不是实现细节。

#### 4.3.3 平凡作物为什么能被收（轻量路径与判据的接缝）

培养器的判据来自**配方表**，而 `plain_chives → friend_chives` 是本版新增的一条 `converting` 配方 ⇒ 平凡韭菜自动成为第 17 个投入物，`outputProfileOf(平凡韭菜)` = `friend_chives` 的档案（稀有档，非史诗）⇒ 通过。**不需要**在判据里为平凡作物写任何特例。

「对应种子」那一侧同样成立：`plain_chives_seeds` 是 `PlainSeedItem`，`ItemNameBlockItem` 的子类，与种子判定无关——培养器只按**物品**给产出，不看物品类。

#### 4.3.4 轻量路径的两个类（与 §3.5 的资源面配套）

| 类 | 必须做到 | 不许做 | 依据 |
|---|---|---|---|
| `PlainCropBlock extends CropBlock` | `age 0~3`（`BlockStateProperties.AGE_3`）；`getMaxAge() = 3`；四个碰撞箱照抄 `FarmstormCropBlock:48-53`；生长读**同一组** `growth.*` config；骨粉与随机刻行为与原版 `CropBlock` 一致 | 不许接 `CropProfile`；不许进 `CropProfile.ALL`；不许写 `seedItem` 字段 | D-215、红线 7 |
| `PlainSeedItem extends ItemNameBlockItem` | `useOn` 走**原版放置流程**（不覆写、或覆写后调 `super`）；无 tooltip | 不许复用 `SeedItem`（`useOn` 被 D-152 ② 钉死成 `PASS`，`SeedItem.java:42-44`）；不许改 `SeedItem` 的行为 | D-216、红线 2 |

**为什么不能复用 `FarmstormCropBlock`**：它的构造器与 codec **都要求一个非空 `CropProfile`**（`FarmstormCropBlock.java:43-46` 的 `RecordCodecBuilder` 用 `CropProfile.CODEC.fieldOf("profile")`、`:57-60` 的构造器签名），给平凡作物凑一个假档案就会污染 `ALL`（红线 7）。

**四种「漏写就静默」的地方**（轻量路径的代价，52 号 §0.2 已登记）：① `ModBlockStateProvider` 的 if 链漏 `instanceof PlainCropBlock` ⇒ 落 `simpleBlock`，世界里的韭菜变成方块（§3.5）；② `ModItemModelProvider` 漏模型分派 ⇒ `plain_chives_seeds` 落 `basicItem`，紫黑格（§3.6）；③ `ModLootTableProvider` 漏表 ⇒ 拆了不掉东西（§3.5 末段）；④ `ModLangProvider` 漏键 ⇒ 显示 raw key（§7.2）。

### 4.4 `TurretBullet` 的寿命

| 项 | 内容 | 依据 |
|---|---|---|
| 改哪个方法 | `TurretBullet#tick()`（`:76-90`） | D-221 |
| 插入点 | `tick()` 的**最开头**、`launchOrigin` 取样之后：`if (this.tickCount >= FarmstormConfig.pumpkinTurretBulletLifetimeTicks.get()) { this.discard(); return; }` | 本文确定（放在索敌之前：到点就不再转向，省一次 `getBoundingBox`） |
| 计时起点 | **出膛**（实体第 1 刻），不是「最后一次命中」 | D-221 补充 |
| 时点 | 第 **300** 刻（15 秒）无条件 `discard()`；`tickCount` 从 0 起，故实际存活 300 刻 | D-221 |
| 删除方式 | 静默 `discard()`：不加粒子、不加音效、不写进度 | 同上 |
| 读档 | **不持久化「已飞刻数」**：读档后重新计 300 刻（`readAdditionalSaveData` 里目标本来就置空，寿命同理） | 同上 |
| 不动的两条线 | `MelonGrenadeProjectile`（瓜！母弹）与 `MelonSliceProjectile`（瓜片子）**一行不动** | 红线 6 |

**为什么不用 `throwable.lifetimeTicks` 那个既有键**：它是**投掷物物品**的通用寿命（`FarmstormConfig:255` 的语言键「投掷物实体的寿命上限」），`TurretBullet` 不是玩家投出来的；把炮台导弹塞进那个键会让「调投掷物寿命」顺手改掉炮台平衡。本版单独开 `pumpkin_turret.bulletLifetimeTicks`（§6.2）。

### 4.5 `fang_dance` 的文案改动

| 项 | 现状（`ModLangProvider.java:229`） | 改后 |
|---|---|---|
| 键 | `advancements.farmstorm.fang_dance.description` | **键不变** |
| 中文 | 触发一次「赢过输过没死过」 | **触发一次「刺球」** |
| 英文 | `Trigger a Fang Trap Cactus once` | **`Trigger a "Fang Ball" once`** |
| 标题（中 / 英） | 最扎人的一集 / `The Itchiest Episode` | **不动** |

**英文措辞的理由**：D-222 的约束是「不能再用 `Fang Trap Cactus` 这个机制描述，要与中文的『一个叫刺球的东西』同构」。`Fang Ball` 是与「刺球」同构的直译（fang = 尖牙、ball = 球），且与物品名 `fang_trap_cactus` 的中文名「刺球」（`ModLangProvider:83`）指向同一件东西——玩家读到描述后能对上物品名，而不是读到触发机制。

**只改这一条**：其余 15 条进度的文案一条不动（哪怕也有中英不一致），D-222 补充已钉死。

---

## 5. 协议

### 5.1 `CultivatorLinkPayload.Link` 加一个 `Kind` 字段

| 项 | 现状 | 改后 | 依据 |
|---|---|---|---|
| 记录 | `record Link(BlockPos core, BlockPos cultivator)`（`CultivatorLinkPayload:53`） | `record Link(BlockPos core, BlockPos cultivator, Kind kind)`（3 字段） | D-210 |
| 枚举 | 无 | `enum Kind { CULTIVATOR, CROP_CONVERTER }`，线上码 `CULTIVATOR = 0` / `CROP_CONVERTER = 1` | 本文确定（§0.3 第 7 项） |
| 单条 codec | `StreamCodec.composite(BlockPos.STREAM_CODEC, Link::core, BlockPos.STREAM_CODEC, Link::cultivator, Link::new)`（`:42-45`） | 三字段 `composite`，第三段 `ByteBufCodecs.BYTE.map(Kind::byId, Kind::id)`（该重载最多 6 个组件，3 个没问题） | 同上 |
| 线上体积 | 2 × `BlockPos` | 2 × `BlockPos` + **1 字节** | D-210「加一个类型字节」 |
| 列表 codec / `MAX_LINKS` | `LINK_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINKS))`（`:48-50`）；1024（`:36`） | **都不动** | D-210 第 1 条 |
| 包数量 / 包 id | 2 个；`farmstorm:cultivator_link` | **2 个**；id 不变 | 同上 |
| 未知码 | — | 按 `CULTIVATOR` 解（前向兼容：将来的第三种机器连到旧客户端，画的是既有青色线而不是崩） | 本文确定 |

**命名 `Kind` 而不是 `Color`**：语义是「这条线连着哪一类机器」，颜色是后果；将来第三种机器只加一个枚举成员（D-210 补充）。

### 5.2 `Kind` 从哪来：核侧持久化，不做方块反查

现状的绑定列表只存坐标（`OutpostCoreBlockEntity.java:120` 的 `List<BlockPos>`，落盘 `long[]`，`:847-852`），列表里没有任何字段能区分「这一格是培养器还是转换器」。三条路只有一条能走：

| 方案 | 做法 | 结论 | 理由 |
|---|---|---|---|
| A | 广播时按坐标读 `getBlockState` 判类型 | **否决** | 要么触发区块加载（违反 `AreaBlockEntities:14-16` 的不加载纪律），要么未加载时猜——猜错就是服务端侧的闪色，与红线 3 同性质 |
| B | 渲染期按方块类型反查 | **否决** | 红线 3 明令禁止 |
| C | **绑定时写入、落盘保存** | **采纳** | 绑定关系新增的那一刻，正好是方块自己知道答案的那一刻；此后零反查 |

**本文确定的落盘形态（对 0.2.2 存档无损）**：

| 项 | 现状键 | 改后 | 读档规则 |
|---|---|---|---|
| 坐标 | `Cultivators`：`ListTag` of `LongTag`（`:848-852`、`:866-870`） | **保留原键原格式** | 0.2.2 存档照旧读得出 24 条坐标 |
| 类型 | 无 | 新键 `CultivatorKinds`：`ListTag` of `IntTag`（`0` 培养器、`1` 转换器），与 `Cultivators` **一一对应同长** | **缺键 ⇒ 全部按 `CULTIVATOR` 解**（0.2.2 只可能绑过培养器，这个默认值就是真值，不是猜测）；两键长度不一致 ⇒ 按较短者截断、缺的补 `0`，并打一条 WARN |
| 写入 | `addCultivator(BlockPos)`（`:358-371`） | `addCultivator(BlockPos pos, Link.Kind kind)`；三个写入点（放置绑定、周期补绑、迁移改绑）都要传对 | 放置方各自知道自己的类型：`CultivatorBlock:99` 传 `CULTIVATOR`、`CropConverterBlock` 传 `CROP_CONVERTER` |
| 删除 | `removeCultivator(BlockPos)` | 签名不变（按坐标删，两条列表同时删） | — |

**为什么不把一条记录改成 compound**：那会把 `Cultivators` 从 `long[]` 变成 `{pos, kind}` 列表，0.2.2 存档的旧键直接读不出来，还要写迁移分支。**旁挂一个新键**是零迁移成本的写法，代价只是两条列表必须同长——这条约束由「只在三类写入点动它」保证。

### 5.3 广播端三处必须同步（加字段最容易漏的三处）

| # | 位置 | 现状 | 不同步的后果 | 改法 |
|---|---|---|---|---|
| 1 | `CultivatorLinkBroadcaster.java:76-78` 的 `ORDER` | 只比 `core().asLong()` 与 `cultivator().asLong()` | 排序键不完整 ⇒ 列表顺序不确定 ⇒ 指纹抖动 ⇒ 每 20 刻白重发一包 | 补第三键 `kind().id()`，并注释「同坐标不可能有两种 kind，补它是为了让比较器全序」 |
| 2 | `CultivatorLinkBroadcaster.java:227-234` 的 `fingerprint(...)` | 只哈希两个 `long` | **kind 变了不触发重发**：把培养器换成转换器后客户端颜色永远不变 | 把 `kind().id()` 也卷进哈希 |
| 3 | `CultivatorLinkBroadcaster.java:217-219` 的组装 | `new Link(entry.pos(), cultivator)` | 少参数编译不过；随手填常量 ⇒ 所有线同色 | 从 `Cultivators` + `CultivatorKinds` 按同一下标配对读出 |

**验收判据**（§8 #24）：把一台已绑定的培养器拆掉、原位置换放一台转换器（或反之）⇒ 客户端那条线必须在**一个去抖窗口内**（`MIN_INTERVAL_TICKS = 10` 刻，`:54`）从青色变红色（或反之），而不是保持不变。

### 5.4 渲染：按 `Kind` 选颜色

| 项 | 值 | 依据 |
|---|---|---|
| `CULTIVATOR` | `(0.30F, 0.85F, 0.90F, 1.0F)` —— 现状的青色，**一个字节都不许变** | `CultivatorLinkRenderer:41-44`、D-210 第 4 条 |
| `CROP_CONVERTER` | **`(0.92F, 0.22F, 0.22F, 1.0F)`** | 本文确定（§0.3 第 8 项） |
| 端点小框 | 与主线同色（`:77-78` 现在共用同一组常量，改成按 kind 取即可） | 同上 |

**红色数值的三条理由**：① 与青色的三个分量逐一拉开 `0.62 / 0.63 / 0.68`，任何背景上都能分辨；② **不用纯 `0xFF0000`**：纯红在高饱和草地与岩浆上会被同类色淹没，且与生命值条的红同色，读起来像「报错」；③ 三分量都低于 `1.0`，与青色同属「略带灰的亮色」，是一套配色。

**渲染端改法只有一行级**：`:71-79` 的循环里把四个颜色常量换成 `switch (link.kind())` 的两组常量。**不许**在读渲染时碰 `level` / `BlockState` / 方块实体（红线 3）。

### 5.5 `PROTOCOL_VERSION` 与不变项

| 项 | 现状 | 改后 | 依据 |
|---|---|---|---|
| `PROTOCOL_VERSION` | `"2"` | **`"3"`** | `FarmstormNetwork:22-24` 的既有纪律「codec 一变必须跟着变」；`Link` 从 2 字段变 3 字段就是 codec 变了 |
| 两行启动自检 | 版本 `2`，`MAX_LINKS 1024` | 版本变 `3`，其余逐字不变 | `FarmstormNetwork:49-52` |
| 包数量 / 通道属性 / `MAX_LINKS` | 2 / `optional()` + `playToClient` / 1024 | **全部不动** | D-210 |

**为什么不保持 `"2"`**：版本串参与通道协商。0.2.2 客户端连 0.2.3 服务端而版本串仍为 `"2"` 时，协商通过、随后按旧 codec 解新字节流——那是**必崩**的解码错误。升到 `"3"` 之后，`optional()` 的语义让旧客户端拿不到这条通道（只是没有连线，不崩），这正是 0.2.2 已有的降级形态。BRIEF 与 52 号都没提这个串，本文补上，登记在 §10 第 8 条。

### 5.6 客户端的三条「只读」仍然成立

`ClientCultivatorLinks` 的四条纪律（`:12-20`）一条不改：收到什么存什么、不按坐标反查、不跨维度缓存、空列表真的清空。加了 `Kind` 之后「不按坐标反查」**更容易被违反**（手上有 kind 了，容易顺手查方块确认）——**不许查**：payload 里的字节就是唯一真相。

---

## 6. 配置

### 6.1 删 3 键（`[converter]` 整组）

| # | 完整键路径 | 类型 | 现值 | 值域 | 动作 | 依据 |
|---|---|---|---|---|---|---|
| 1 | `converter.xpCost` | int | 3 | 0 ~ 1000 | **删**（字段 `FarmstormConfig.java:19`、定义 `:296-298`） | D-223、裁定 2 |
| 2 | `converter.netherStarForwardCost` | int | 1 | 0 ~ 64 | **删**（字段 `:20`、定义 `:299-301`） | 同上 |
| 3 | `converter.netherStarOnReverse` | boolean | `false` | — | **删**（字段 `:21`、定义 `:302-304`） | 同上 |

**整组下线**：`b.push("converter")`（`:295`）与 `b.pop()`（`:305`）一并删；三个静态字段（`:19-21`）也要删，否则编译不过。**连带**：三个语言键（`farmstorm.configuration.converter.*`，`ModLangProvider:243-245`）一并删（§7.1 的 A-3），`ConverterBlockEntity` 里读这三项的四段逻辑随方块实体重写一起消失。

### 6.2 加 2 键（逐键表）

| # | 完整键路径 | 落盘节 / 键 | 类型 | 默认值 | 值域 | 描述文本（中 / 英） | 依据 |
|---|---|---|---|---|---|---|---|
| 1 | `crop_converter.processTicks` | 新节 `[crop_converter]` 的 `processTicks` | int | **600** | **20 ~ 72000** | 转换周期（刻，600 刻 = 30 秒） / `Conversion cycle in ticks (600 ticks = 30 s)` | D-223、裁定 4 |
| 2 | `pumpkin_turret.bulletLifetimeTicks` | **既有**节 `[pumpkin_turret]` 的新键 | int | **300** | **1 ~ 72000** | 炮弹寿命（刻，300 刻 = 15 秒） / `Missile lifetime in ticks (300 ticks = 15 s)` | D-223、裁定 11 |

**三处必须同批写**：

| 位置 | 内容 | 漏写的后果 |
|---|---|---|
| `FarmstormConfig.java` 字段区 | `cropConverterProcessTicks` / `pumpkinTurretBulletLifetimeTicks`（后者加在 `:36` 附近的既有字段组，按 `pumpkinTurret*` 体例） | 编译不过或运行期 NPE |
| `FarmstormConfig.java` 定义区 | `[crop_converter]` 组**放在 `[converter]` 原来的位置**（`:295` 起）；`bulletLifetimeTicks` 加在 `[pumpkin_turret]` 组内（`:444-461`，建议排在 `bulletSpeed` 之后、`b.pop()` 之前） | 键落进别的节，或 `push`/`pop` 不配对 |
| `ModLangProvider.java` | 两条 `config(entries, …)`（`:482-484` 的助手，前缀 `farmstorm.configuration.`） | 游戏里显示 raw key |

**两个范围为什么这么取**：`processTicks` 抄 `cultivator.cycleTicks` 的 `20 ~ 72000`（`:964`）——同为「周期」，下限 20 刻保证至少一秒，上限 72000 刻 = 1 小时；`bulletLifetimeTicks` 下界取 **1**（0 刻会让炮弹一生成就消失，等于关掉炮台，没有配置价值），上界抄同组 `fireIntervalTicks` 的 `72000`（`:456`）。两个都是「抄既有范围」而不是新造数字（§0.3 第 11、12 项）。

### 6.3 四个数的核对

| 项 | 0.2.2 | 增 | 减 | v0.2.3 | 算法 |
|---|---|---|---|---|---|
| server config 键 | **176** | +2 | −3 | **175** | `176 − 3 + 2 = 175` |
| `b.push` 次数（组数） | **60** | +1（`[crop_converter]`） | −1（`[converter]`） | **60** | `60 − 1 + 1 = 60` |
| `.define` 行（含 1 行注释） | 178 | +2 | −3 | **177** | 175 live + 1 注释；只用于「读源码数键」的复核 |
| toml 顶层节 | 36 | 0 | 0 | **36** | `[converter]` 换名 `[crop_converter]`，节数不变 |

### 6.4 验收纪律：盘上的老 toml **不会**回写默认值

| 事实 | 证据 |
|---|---|
| 盘上的 `run/config/farmstorm-server.toml` **是陈旧的** | `:753` 的 `maxPerPlayer = 2`、`:765` 的 `cultivatorCapacity = 16`，而 0.2.2 的代码默认值已是 **4 / 24**（`FarmstormConfig:943`、`:956`）——0.2.2 的默认值改动至今没落到盘上 |
| 当前 toml 的第一行就是 `[converter]` | `:1`（本版删掉这一节、换成 `[crop_converter]`） |
| 机制 | NeoForge 的 config 只在**键增删**时重写文件；**改默认值不回写已存在的键**（D-223 补充、48 号 §2.7.3） |
| 本版的特殊性 | 本版确实增删了键，所以下次起服文件会被重写；但重写只补 / 删键，`maxPerPlayer` 与 `cultivatorCapacity` 的**值**仍从旧文件原样保留 |

**三条纪律**：

1. **核对任何 config 断言之前，先删掉（或改名备份）`run/config/farmstorm-server.toml`**，让游戏重生一份；否则会拿 0.2.1 时代的 `2 / 16` 做判断，得到假红（0.2.2 已踩过一次，D-223 补充）。
2. **观察点直接用「节名换了」**：重生成的 toml 里 `^\[converter\]` 命中 **0**、`^\[crop_converter\]` 命中 **1**——这一对是「文件确实被重写」的直接证据，比只看键数稳。
3. **不写迁移代码**：自动改写玩家配置 = 覆盖玩家自己的设置，比「不生效」更糟（0.2.2 已否决）。老存档的 `[converter]` 三项在重写时被删掉、`[crop_converter]` 以默认值出现——**这是期望行为**。

### 6.5 不动的 config 面

| 项 | 结论 | 依据 |
|---|---|---|
| `FarmstormClientConfig` 的 3 键（`hud.enabled` / `hud.x` / `hud.y`） | **不动**，且**不计入** 175 | 48 号 §1.4 的既有口径 |
| `cultivator.cycleTicks` / `cultivator.yield` | **不动**（600 / 2） | D-219 补充第 2 条 |
| `outpost_core.cultivatorCapacity` | **不动**（默认 24）；转换器与培养器**共享这 24 个名额** | 裁定 5、D-208 |
| `throwable.lifetimeTicks` | **不动**；炮台导弹不读它 | §4.4 |
| 其余 170 个键 | 一行不动 | D-204 的范围闸 |

---

## 7. 语言键 321 × 2 的逐键清单

**总账**：`340 − 23 + 4 = 321`（zh 与 en 各 321，键集逐字相同）。四张子表逐键点名。

### 7.1 子表 A：删 23 键

**A-1 删 7 个种子物品键**：

| # | 键 | 中文值（删前） |
|---|---|---|
| 1 | `item.farmstorm.puffed_wheat_seeds` | 膨胀麦种子 |
| 2 | `item.farmstorm.power_beet_seeds` | 甜菜！种子 |
| 3 | `item.farmstorm.melon_grenade_seeds` | 瓜！种子 |
| 4 | `item.farmstorm.pumpkin_turret_seeds` | man瓜种子 |
| 5 | `item.farmstorm.awakened_torchflower_seeds` | 激活的火把花种子 |
| 6 | `item.farmstorm.awakened_pitcher_plant_pod` | 激活的瓶子草豆荚 |
| 7 | `item.farmstorm.chive_seeds` | 朋友的韭种子 |

**A-2 删转换器 GUI（9 条）**：

| # | 键 | 中文值（删前） |
|---|---|---|
| 8 | `container.farmstorm.converter` | 农产品转换器 |
| 9 | `gui.farmstorm.converter.convert` | 转换 |
| 10 | `gui.farmstorm.converter.xp_cost` | 消耗经验：%s |
| 11 | `gui.farmstorm.converter.no_recipe` | 无法转换 |
| 12 | `gui.farmstorm.converter.need_catalyst` | 缺少催化剂：%s |
| 13 | `gui.farmstorm.converter.not_enough_xp` | 经验不足 |
| 14 | `gui.farmstorm.converter.forward` | 原版 → 改良 |
| 15 | `gui.farmstorm.converter.reverse` | 改良 → 原版 |
| 16 | `gui.farmstorm.converter.tooltip` | 放入原版或改良作物进行 1:1 转换 |

**A-3 删转换器 config（3 条）**：

| # | 键 | 中文值（删前） |
|---|---|---|
| 17 | `farmstorm.configuration.converter.xpCost` | 每次转换扣除的经验点数 |
| 18 | `farmstorm.configuration.converter.netherStarForwardCost` | 正向转换额外消耗的下界之星个数 |
| 19 | `farmstorm.configuration.converter.netherStarOnReverse` | 反向转换是否也消耗下界之星 |

**A-4 删两条进度（4 条 = 2 条进度 × title + description）**：

| # | 键 | 中文值（删前） |
|---|---|---|
| 20 | `advancements.farmstorm.seed_cycle.title` | 闭环自给 |
| 21 | `advancements.farmstorm.seed_cycle.description` | 收获一次成熟改良作物，并同时拾取作物与播种物 |
| 22 | `advancements.farmstorm.nether_star_toll.title` | 星之代价 |
| 23 | `advancements.farmstorm.nether_star_toll.description` | 消耗下界之星完成一次激活的火把花或瓶子草转换 |

**小计**：`7 + 9 + 3 + 4 = 23`。四组分别对应 `ModLangProvider.java` 的 `:88-93,103`（种子）、`:177,180-187`（container / gui）、`:243-245`（config）、`:222-223`（两条进度）。

### 7.2 子表 B：加 4 键

| # | 键 | 中文值 | 英文值 | 落点 | 依据 |
|---|---|---|---|---|---|
| 1 | `block.farmstorm.plain_chives_crop` | 平凡韭菜 | `Plain Chives` | 方块段（`block(...)` 助手，`:462-464`） | D-215 |
| 2 | `item.farmstorm.plain_chives_seeds` | 平凡韭菜种子 | `Plain Chives Seeds` | 物品段（`item(...)` 助手，`:458-460`） | D-216 |
| 3 | `farmstorm.configuration.crop_converter.processTicks` | 转换周期（刻，600 刻 = 30 秒） | `Conversion cycle in ticks (600 ticks = 30 s)` | config 段（`:482-484`） | D-223 |
| 4 | `farmstorm.configuration.pumpkin_turret.bulletLifetimeTicks` | 炮弹寿命（刻，300 刻 = 15 秒） | `Missile lifetime in ticks (300 ticks = 15 s)` | 紧跟 `pumpkin_turret.bulletSpeed`（`:281`） | D-221、D-223 |

**小计 4**：两条平凡作物键补齐「一个新方块 + 一个新物品」，两条 config 键与 §6.2 逐键对应。

### 7.3 子表 C：改名 2 键（键数不变）

| # | 旧键 | 新键 | 中文值（新） | 英文值（新） | 说明 |
|---|---|---|---|---|---|
| 1 | `item.farmstorm.converter` | `item.farmstorm.crop_converter` | **农作物转换器** | `Crop Converter` | 键改名 + 值改术语（52 号 §0.2：中文名从「农产品转换器」改为「农作物转换器」） |
| 2 | `block.farmstorm.converter` | `block.farmstorm.crop_converter` | **农作物转换器** | `Crop Converter` | 同上 |

**为什么不算「删 1 加 1」**：那样会把总数数成 323（D-224 补充点过这个坑）。改名不改变键数。

### 7.4 子表 D：改值（键数不变）

| # | 键 | 旧值（中 / 英） | 新值（中 / 英） | 依据 |
|---|---|---|---|---|
| 1 | `advancements.farmstorm.fang_dance.description` | 触发一次「赢过输过没死过」 / `Trigger a Fang Trap Cactus once` | **触发一次「刺球」** / **`Trigger a "Fang Ball" once`** | D-222（§4.5） |
| 2 | `advancements.farmstorm.converter.description` | 放置一台**农产品**转换器 / `Place a Crop Converter`（英文已是新术语） | 放置一台**农作物**转换器 / `Place a Crop Converter`（英文不动） | 52 号 §0.2 的术语表；本文补，登记 §10 第 2 条 |

第 2 条是**纯术语统一**：英文那句在 0.2.2 就写的是 `Crop Converter`，中文还停在旧名——不改会让玩家在进度界面看到「农产品转换器」而方块叫「农作物转换器」。键不变、键数不变，`321` 不受影响；D-224 只列了 1 条改值，本文多这 1 条，登记在 §10。

### 7.5 四个子表的总核对

| 动作 | 键数变化 | 明细 |
|---|---|---|
| A 删 | **−23** | 7 种子 + 9 GUI + 3 config + 4 进度 |
| B 加 | **+4** | 2 平凡作物 + 2 config |
| C 改名 | **0** | 2 个键换 id，一删一增相抵 |
| D 改值 | **0** | 2 个键换值，键集不变 |
| **合计** | **−19** | `340 − 19 = 321` |

按组复核（§1.4 的表）：`items 34 → 28`（−7 + 1）、`blocks 27 → 28`（+1）、`advancements 36 → 32`（−4）、`config 179 → 178`（−3 + 2）、`entity 13 → 13`、`plain 51 → 42`（−9）⇒ `28 + 28 + 32 + 178 + 13 + 42 = 321`。

### 7.6 三条纪律

中英同键同数（两份文件由同一张 `ENTRIES` 表按列写出，`ModLangProvider:63-68`，`zh-only = 0` / `en-only = 0` 天然成立）；语言键**不会自动生成**（`config(...)` / `item(...)` / `block(...)` 都是逐条手写，所以「加一个 config 键」与「加一句翻译」是两件事）；**321 是最终值不是下限**——若实现期发现还需要第 5 条（例如某个界面标题），必须回头改本文与 53 / 55 号，不许在代码里偷偷加。

---

## 8. 验收标准（32 条）

每条给「怎么做 / 期望看到什么 / 依据」。**观察位置必须可操作**：一切判据都能用日志、文件计数、游戏内行为三者之一证伪。

| # | 怎么做 | 期望看到什么 | 依据 |
|---|---|---|---|
| 1 | 起服，看 `run/logs/latest.log` 的自检行 | `自检 注册对象 92 个：items 28 / blocks 28 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK`（`Farmstorm.java:184-188` 的格式串） | §1.2、D-205 |
| 2 | 跑 `tools/verify_project.py` | 末尾 `已核 registry 对象: 92 / 92`、`结论：FAIL 0 项`；第 1 节打印「文档口径键数 175」与「生成 toml 键数 175」 | §1.2、§1.3 |
| 3 | 读 `verify_project.py:334-393` | `EXPECTED["items"]` 含 `plain_chives_seeds`、不再含 7 个种子；`blocks` 含 `crop_converter` 与 `plain_chives_crop`；`menu_type` 只剩 `outpost_core`；`EXPECTED_COUNTS` 是 28 / 28 / 1 | §2.4 第 8 行 |
| 4 | **反向**：diff `verify_project.py` 的 config 期望面 | `expected_config_keys*` 五个函数体一字不动（本版删增的 3 / 2 个键不在它们的解析范围里） | §1.3、48 号 §4.4 |
| 5 | `grep` 全树 + 看启动日志 | `@Mixin` 命中 **0**；`playToClient` 恰 **2** 处；两行自检显示版本 **3**、`MAX_LINKS 1024`；`CultivatorLinkPayload:36` 仍是 `1024` | §1.5、§5.5 |
| 6 | 数两份 `lang/*.json`（`-Encoding UTF8`） | `zh = 321`、`en = 321`、`zh-only = 0`、`en-only = 0`；§7.1 的 23 个键全部不存在、§7.2 的 4 个键全部存在 | §7 |
| 7 | **先删 `run/config/farmstorm-server.toml`**，起服后数新文件 | 键数 **175**、`b.push` 口径 **60**；`^\[converter\]` 命中 **0**、`^\[crop_converter\]` 命中 **1**；`maxPerPlayer = 4`、`cultivatorCapacity = 24` | §6.3、§6.4 |
| 8 | **反向**：把升级前的旧 toml 放回去再起服 | 仍是 `maxPerPlayer = 2` / `cultivatorCapacity = 16`，且 `[converter]` 段仍在——这条「红」是**期望中的红**，证明「老文件不回写默认值」 | §6.4 |
| 9 | 数 `data/farmstorm/recipe/` 里 `"type": "farmstorm:converting"` 的 JSON | **21** 个；6 条种子配方（`puffed_wheat_seeds` 等）**不存在**；`friend_chives.json` 存在 | §4.2.2 |
| 10 | 数整个 `data/farmstorm/recipe/` 目录 | **25** 个文件 = 21 `converting` + `crop_converter` / `outpost_core` / `compressed_puffed_wheat` / `cultivator` 四条合成 | §4.2.2 |
| 11 | 读两个合成配方 JSON | 转换器 = 6 铁锭 + 2 玻璃 + 1 红石粉；培养器 = 4 铁锭 + 4 玻璃 + 1 红石粉（`铁 玻璃 铁 / 玻璃 红石 玻璃 / 铁 玻璃 铁`）；两者的 `unlockedBy` 都不是 `#farmstorm:` 标签 | §4.2.2、§0.3 第 13 项 |
| 12 | 起服后看日志有没有 recipe codec 报错；再 `/reload` 一次 | 无 `Couldn't parse recipe` / `Failed to parse` 之类；21 条配方全部加载成功（旧字段 JSON 读失败属已知限制，不作为失败） | §4.2.1 |
| 13 | 放核 → 放转换器 → 手持小麦右键 → 记时 | 右键后 `working=true`（模型切到帧条）；第 **600** 刻（30 秒）核里多 **1** 个 `farmstorm:puffed_wheat`；槽清空、`working=false` | §4.1.2、§4.2 |
| 14 | 转换器槽里有货时：空手右键、拿别的物品右键 | 两种情况都**无反应**（`PASS`）：不取出、不替换、不消耗手持物 | §4.1.4 |
| 15 | 转换器槽里有货时拆掉方块 | 掉出**那一个**输入物（不是产物、不是方块本体两份）；`progress` 归零，重放后重新计时 | §4.1.4 |
| 16 | **核满暂停不丢**：把核的 54 格塞满 → 放转换器 → 放一个小麦 → 等 700 刻 | 不投递、`working=false`，但槽里小麦还在、`progress` 停在 600；腾出 1 格后**下一 tick**完成投递（不重新计时） | §4.1.4、D-208 |
| 17 | 培养器的 17 投入物抽验 6 条：小麦 / 胡萝卜 / 毒马铃薯 / 火把花 / 金苹果 / 平凡韭菜 | 前三条与平凡韭菜**收得进**；火把花与金苹果**收不进**（`PASS`、槽不变） | §4.3.2 |
| 18 | 同一台培养器依次喂：小麦、胡萝卜、毒马铃薯 | 小麦 ⇒ 2 小麦 + 2 小麦种子；胡萝卜 ⇒ **4 胡萝卜**；毒马铃薯 ⇒ **4 毒马铃薯**（不是马铃薯） | §4.3.2 第 1 / 2 / 4 行 |
| 19 | **培养器收不进改良作物**：拿 `farmstorm:puffed_wheat` 右键培养器 | 无反应（`PASS`），槽保持空，手持物不消耗 | §4.3.1、D-218 |
| 20 | **平凡韭菜能种**：`plain_chives_seeds` 右键耕地 | 生成 `farmstorm:plain_chives_crop`（`age=0`）；骨粉 / 等待可长到 `age=3`；拆成熟作物掉 1 个 `plain_chives` + 0~3 个 `plain_chives_seeds` | §3.5、§4.3.4 |
| 21 | 数 `data/farmstorm/tags/item/seeds.json` 的成员 | 恰 **1** 项：`farmstorm:plain_chives_seeds`（`6 − 6 + 1 = 1`） | §2.3 标签面 |
| 22 | 目录计数 + 读模型文件 | `textures/block/*.png` = **84**、`*.mcmeta` = **5**、`textures/item/*.png` = **22**、`textures/gui/` = **0**；`converter_*.png` 命中 **0**；`models/block/crop_converter.json`、`crop_converter_working.json`、`plain_chives_stage0..3.json` 六个文件存在 | §3.6、§3.8 |
| 23 | 读 `blockstates/crop_converter.json` | 恰 **8** 个 variant（4 朝向 × 2 工作态）；`working=false` 指 `farmstorm:block/crop_converter`、`working=true` 指 `farmstorm:block/crop_converter_working`；`y` 依次 0 / 90 / 180 / 270；全部 `uvlock: true` | §3.2 |
| 24 | **红线颜色**：同屏放一台培养器与一台转换器（同一个核） | 培养器的线**青色**、转换器的线**红色**；拆掉培养器原位置放转换器 ⇒ 那条线在 **10 刻内**从青变红（kind 进了指纹） | §5.3、§5.4 |
| 25 | **炮弹 300 刻**：放 man瓜炮台，让它对天空/远处开火，数 `turret_bullet` 的存活 | 第 **300** 刻消失；无粒子、无音效、无进度；`F3` 实体列表里不再有它 | §4.4、D-221 |
| 26 | 看 `MelonGrenadeProjectile` / `MelonSliceProjectile` 两个类的 diff | **零改动**（行数、内容都不变）；瓜！仍然落地才结算 | 红线 6 |
| 27 | 数 `data/farmstorm/advancement/` | **16** 个 json（`18 − 2`）；`seed_cycle.json` 与 `nether_star_toll.json` 不存在；`converter.json` 仍在且判据是 `crop_converter` | §2.3、§2.4 第 6 行 |
| 28 | 游戏里看「最扎人的一集」的进度描述（中 / 英各一次） | 中文「触发一次「刺球」」、英文 `Trigger a "Fang Ball" once`；标题仍是「最扎人的一集」/ `The Itchiest Episode` | §4.5 |
| 29 | 转换器的硬度与抗爆：空手挖、石镐挖、TNT 炸 | 空手挖不动（`requiresCorrectToolForDrops` + 硬度 5.0）；石镐挖得出本体；TNT 炸不毁（抗性 1200.0） | §2.1.1 |
| 30 | 读两条方块标签产物 | `data/farmstorm/tags/block/processing_block.json` 恰 **2** 个成员（`cultivator` + `crop_converter`）；`minecraft:mineable_with_pickaxe` 含 `crop_converter` | §2.4 第 4、5 行 |
| 31 | 数 `src/main/java` 的 `*.java` + 查三个文件 | **152**；`ConverterMenu.java` / `ConverterScreen.java` / `ConverterRecipeBookCategory.java` / `SeedItem.java` 都不存在；客户端启动后日志里**没有** `Unknown recipe category` 的 WARN | §1.5、§2.6 |
| 32 | 老存档兼容：拿 0.2.2 的存档（含一个绑了培养器的核）读档 | 培养器绑定仍在、线仍是**青色**（`CultivatorKinds` 缺键 ⇒ 全按 `CULTIVATOR`）；`farmstorm:converter` 方块位置变成空气（裁定 1 的既定代价，写进 README 升级须知） | §5.2、D-206 |

**32 条的分布（不重叠、逐段相加 = 32）**：计数与工具 **5**（#1~#5）/ 语言与 config **3**（#6~#8）/ 配方 **4**（#9~#12）/ 转换器行为 **4**（#13~#16）/ 培养器 **3**（#17~#19）/ 平凡作物 **2**（#20、#21）/ 资源 **2**（#22、#23）/ 连线与炮弹 **3**（#24~#26）/ 进度与文案 **2**（#27、#28）/ 属性与标签与文件面 **3**（#29~#31）/ 存档 **1**（#32）= `5+3+4+4+3+2+2+3+2+3+1 = 32`。**四条必测组合**：① #7 与 #8 必须成对跑（只跑 #7 会以为「删了就生效」，只跑 #8 会把期望红当缺陷）；② #13 与 #16 必须成对跑（#13 证明能产出，#16 证明核满不丢）；③ #17 与 #19 必须成对跑（一个证明收原版、一个证明不收改良，缺一无法证明判据**反转**了）；④ #24 的「换机器变色」与 #23 的 blockstate 必须各跑一次（一个是视觉、一个是数据，互不替代）。

---

## 9. 风险 RT-71 ~ RT-82

| 编号 | 风险 | 触发条件 | 后果 | 规避 |
|---|---|---|---|---|
| **RT-71** | **改名不彻底**：三处注册改了、某个字符串常量或数据路径没改 | 只改 `FarmstormBlocks` 忘了 `FarmstormItems` / `FarmstormBlockEntities`；或战利品表 / blockstate 的 id 仍写 `converter` | 世界里的转换器变成缺失方块（紫黑格 + 掉落物为空）；`#farmstorm:processing_block` 里有两个 id 的错觉 | (a) §2.1 / §2.4 逐处点名；(b) §8 #3 / #22 / #30 三条独立判据；(c) 验收跑 `grep -rn '"converter"' src/main/java`，命中只允许出现在 `farmstorm:converter` 的**进度 id** 与 `container`/`gui` 的删除说明里 |
| **RT-72** | **92 与两套闸不同步**：自检、检查器、注册面三处任一处少改 | 改了注册没改 `Farmstorm.java:180-183`，或没改 `EXPECTED_COUNTS` | 启动日志打 `FAIL` 而脚本全绿（或反之）——两套闸互相矛盾，验收失去意义 | (a) §2.4 第 7、8 行把**两处**列成必改；(b) §8 #1 / #2 成对验；(c) 两处必须在**同一个提交**里改完（0.2.2 的同一条教训） |
| **RT-73** | **手持闸写成「非史诗」**：把培养器的判据抄到转换器上 | 图省事共用一个 `admissible` | 4 条史诗配方**不可达**（火把花 / 瓶子草 / 幸运浆果 / 狂野苹果永远产不出来），且不报错——只是右键没反应 | (a) §4.1.3 第 3 行明写「转换器收全 21 条、含 4 条史诗输入」；(b) §8 #17 的反向条（火把花收不进**培养器**）与 #13 的正向条成对；(c) 单测式验收：拿火把花右键转换器必须成功 |
| **RT-74** | **核满时的投递失败被当成成功** | `deliver()` 返回 `true` 而 `acceptAll` 实际失败（例如先清槽再投递） | **产物凭空消失**（玩家的 30 秒白等），或复制（先投递后清槽且重复投） | (a) §4.1.2 的顺序写死：先 `acceptAll` 返回 `true` 才清槽；(b) §8 #16 专测「核满不丢」；(c) 复用 `OutpostCoreBlockEntity:151-165` 的整批接口，不自己写单件判定 |
| **RT-75** | **`Kind` 加了字段但没进指纹 / 排序** | 只改 `Link` 的 record 与 codec | 颜色不跟着变（去抖认为没变化）；或列表顺序不稳 ⇒ 每 20 刻重发一包 | (a) §5.3 的三行表逐处点名（`ORDER`、`fingerprint`、`assemble`）；(b) §8 #24 的「换机器变色」是唯一能抓它的判据 |
| **RT-76** | **协议串没升** | 只改 codec 忘了 `PROTOCOL_VERSION` | 旧客户端连新服时协商通过、随后按旧 codec 解新字节流 ⇒ **必崩**的解码错误 | (a) §5.5 明写 `"2"` → `"3"` 与理由；(b) §8 #5 读启动日志的两行自检；(c) `FarmstormNetwork:22-24` 的纪律原文已把这条钉死 |
| **RT-77** | **平凡作物走错资源分支** | 复用 `cropStates(...)`（贴图按模型名拼）或漏 `instanceof PlainCropBlock` 分支 | 前者：贴图指到不存在的 `plain_chives_stageN`（紫黑格）；后者：落 `simpleBlock`，世界里的韭菜变成六面方块（且**不报错**） | (a) §3.5 的两条「必须单开」写死；(b) §8 #22 / #23 的模型与 blockstate 判据；(c) datagen 后目视一眼 `models/block/plain_chives_stage0.json` 的 `textures.crop` |
| **RT-78** | **把动态 PNG 的暂停交给代码** | 试图用 `tick` 去控制纹理帧，或给空闲模型也引用带 mcmeta 的图 | 空闲时动画照播（看起来像还在工作）；或代码里出现一堆无效的「暂停」逻辑 | (a) §3.2 末段写死「两张模型是唯一正解」；(b) 贴图落位表里静态副本**没有** mcmeta（§3.1）；(c) §8 #23 读 blockstate 的模型指向 |
| **RT-79** | **语言键数目算错** | 把 2 个改名当「删 1 加 1」，或漏删 4 个进度键 | 总数变成 323 / 319，与 §1.4 的分组核对对不上 | (a) §7 的四张子表逐键点名（删 23 / 加 4 / 改名 2 / 改值 2）；(b) §8 #6 直接数两份 json；(c) §7.5 的按组复核表 |
| **RT-80** | **拿老 toml 做验收** | 没删 `run/config/farmstorm-server.toml` 就断言 4 / 24 / 175 | 假红（读到 2 / 16）或假绿（键数凑巧也是 176→175 却漏看节名） | (a) §6.4 的三条纪律；(b) §8 #7 与 #8 **成对跑**；(c) 用 `^\[converter\]` / `^\[crop_converter\]` 这一对做「文件真被重写」的旁证 |
| **RT-81** | **炮弹寿命写错位置或扩大范围** | 写进 `steerTowards` 而不是 `tick` 开头；或顺手给 `MelonGrenadeProjectile` 也加上 | 前者：追踪弹在索敌分支里漏计，寿命不准；后者：改变瓜！的战术用法（红线 6） | (a) §4.4 明写插入点与「只作用于 `TurretBullet`」；(b) §8 #25 / #26 成对验；(c) 验收看两个投射物类的 diff 为 0 |
| **RT-82** | **平凡韭菜种子复用了 `SeedItem`** | 觉得「改个构造参数就行」 | 种子**种不下去**（`useOn` 无条件 `PASS`），而平凡韭菜是唯一的可种植平凡作物——等于本版第 2 条需求完全落空 | (a) 红线 2 + §2.2 的类表写死「新类 `PlainSeedItem`」；(b) §8 #20 的种植判据；(c) 验收跑 `grep -n "PLAIN_CHIVES_SEEDS" src/main/java`，构造器必须是 `PlainSeedItem` 而不是 `SeedItem` |

**十二条的共同点**（写给 55 / 56 号）：每条缓释都是「**一处定义 + 一条可 grep 的字符串 + 一条行为验收**」，没有一条靠「记得小心」。RT-71 / RT-72 / RT-79 的落点是能数的文件与键；RT-73 / RT-74 / RT-80 / RT-82 的落点是必须真进游戏做一次的动作；RT-75 / RT-76 / RT-78 / RT-81 的落点是一行代码或一个字节。

---

## 10. 与 0.2.2 的差异表（15 条，**不自行改上游**）

按「与 52 号的矛盾 / 与 BRIEF 的矛盾 / 与工具实际行为的偏差 / 源码注释漂移」四类排。**本文只登记，不改任何上游文件。**

| # | 涉及 | 上游写的 | 本文的处理 | 性质 |
|---|---|---|---|---|
| 1 | `BRIEF` §3.2、52 号 D-224 | 语言键「`340 − 21 + 2 = 321`」 | **它的分项清单是对的**（7 + 9 + 3 + 4 = 23、2 + 2 = 4），只有概括式写错；本文按逐键清单写 `340 − 23 + 4 = 321`（§1.1、§7.5）。两条算式同为 `−19`，最终值 321 不受影响 | 上游算式与分项自相矛盾（本文按分项） |
| 2 | 52 号 D-224 的「改值」栏 | 只列 `fang_dance` **1 条** | 本文列 **2 条**：第 2 条是 `advancements.farmstorm.converter.description` 的中文把「农产品转换器」改成「农作物转换器」（英文早在 0.2.2 就是 `Crop Converter`）。键不变、键数不变，321 不受影响 | 上游漏项（本文补，属术语表 D-206 的连带） |
| 3 | `BRIEF` §1.1、52 号 §3.3 第 12 项 | 「6 张旧转换器贴图」 | **实测 7 张**：`converter_front` / `converter_side`（两张孤儿）+ `converter_side_{wheat,carrot,potato,beetroot}` + `converter_top`。只有 5 张被现模型引用，另 2 张从未被任何模型 / 代码引用 | 上游计数不足（不影响 92 / 175 / 321） |
| 4 | 52 号 D-214（早期稿） | 早期稿曾写「进度总数从 **26** 降到 **24**」 | **实测 18 → 16**：`data/farmstorm/advancement/` 下 18 个 json，删 2 条后 16 条；语言键那边一直是「18 × 2 = 36 → 32」，与 26 / 24 对不上。**52 号现已改为 18 → 16**，本文与该值一致 | 已回灌上游（52 号已修正） |
| 5 | 52 号 D-212 补充（早期稿） | 早期稿曾写「`#farmstorm:seeds` 成员从 **10** 条降到 **4** 条」 | **实测 6 → 1**：标签只有 6 个物品成员（`ModItemTagsProvider:42-48`、产物 `seeds.json` 6 项），那「4 条自身即播种物的作物」从来没有成员资格；改后只剩 `plain_chives_seeds`。**52 号现已改为 6 → 1**，本文与该值一致 | 已回灌上游（52 号已修正） |
| 6 | `BRIEF` §5 第 2 项 | 新增平凡作物方块「`FarmstormBlocks.java:107-112`」 | 那两行落在私有工厂 `deployed(...)` 的方法体内，不是注册区。**插入点应为 `:102`（`CULTIVATOR` 收尾）与 `:104`（私有构造器）之间** | 行号漂移（本文按可编译的位置） |
| 7 | `BRIEF` §5 第 8 项 | 「`FarmstormClient:62,75` 两行同步删」 | `:62`（屏幕注册）删；`:75` 那行**改为内联**（`registerRecipeCategoryFinder`），否则 21 条 `converting` 配方会让客户端刷 21 条 `Unknown recipe category` WARN。类文件照删；文件数按 `154 − 3 − 1（SeedItem） + 2 = 152` | 上游说法不完整（本文补代价与做法） |
| 8 | `BRIEF` §3.2、52 号 D-210 | 只说「包数不变、`MAX_LINKS` 不变」 | 本文补：`PROTOCOL_VERSION` 必须 `"2"` → **`"3"`**（codec 变了，`FarmstormNetwork:22-24` 的既有纪律）。不改会在混装环境下解出崩溃 | 上游漏项（本文补） |
| 9 | `BRIEF` §1.1 的行数、52 号 §3.3 的文件行数 | 「`ConverterMenu` 208 行 / `ConverterScreen` 80 行 / `ConverterRecipeBookCategory` 17 行 / `ConverterBlockEntity` 423 行 / `ConverterRecipe` 107 行」 | **实测 216 / 86 / 24 / 435 / 112**（`SeedItem` 为 54）。行数过期不改计数；但「删 3 加 2 = 153」在补入 `SeedItem` 的 −1 之后应为 **152**（§1.5 已同步） | 行数漂移（计数已修正） |
| 10 | `plan0.2.3.txt:11` 的标签名 | `farmstorm_processing_block`（下划线） | 按既有标签 `farmstorm:processing_block` 落地，`crop_converter` 加进该标签（成员 1 → 2）。52 号 §7 第 1 条已登记，本文在 §2.4 第 4 行给落点 | 口径差异（按既有事实） |
| 11 | `FarmstormTags.java:35` 的注释 | 「6 个独立播种物 + 4 个自身即播种物的作物」 | 后半句与实际成员不符（标签只有 6 个物品，改后 1 个）。本版**只改注释**，标签语义不动（D-216 补充） | 源码注释漂移（本版顺手改） |
| 12 | `CultivatorBlockEntity:243-244` 的注释 | 「22 条档案里 8 条的 `seedItem` 指向自身……准入面内因此是 5 条给 2 + 2、13 条只给 2」 | 本版产出规则改为「6 条 2 + 2、11 条给 4」（§4.3.2），这段注释要整段重写；`hasOwnSeed` 的语义从「有没有独立种子」变成「两组怎么给」 | 注释随行为失效（本文列为必改） |
| 13 | `ConverterBlock:79-81` 的注释 | 「29 条配方」 | 本版 21 条（`26 − 6 + 1`）。该注释随整个方法体重写消失，但**新类的类注释不许再写 29** | 注释漂移 |
| 14 | 52 号 §3.2 第 12 项 | 「`Farmstorm.java:180-183` 自检数字改 92 / 28 / 28 / 1」 | 成立，但**必须与注册改动同一个提交**：`:180-183` 的 12 个 `==` 里只有 4 个变，其余 8 个（8 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1）一个不动 | 上游正确，需强调时序 |
| 15 | 52 号留给本文的口子 | `Kind` 怎么落盘、红线取什么数值、协议串怎么处理、`first_conversion` 的授予点 | **本文各自给了结论**：核侧旁挂 `CultivatorKinds`（§5.2）、红 `(0.92, 0.22, 0.22)`（§5.4）、协议串 `"3"`（§5.5）、授予核归属者（§4.1.6）。四条都标「本文确定」，不进 §0.3 之外的编号体系 | 上游留的活，本文已办 |

**三条给 55 号的提示**：① 第 1、4、5 条是「数错」类，不影响最终交付值（321 / 16 / 1 都能从源码重新数出来），但会让下游照抄出第二个错数——验收任务里不要再抄 26 / 24 / 10 / 4 这组数；② 第 7、8 条是「漏项」类，只在**混装环境**（旧客户端 / 旧存档）现形（21 条 WARN、解码崩溃），都不在 `BRIEF` 的 19 项里，建议各列一条独立任务；③ 第 15 条的四条结论**不需要作者再裁**，依据分别是「不加载区块」「红线 3 的同一理由」「`FarmstormNetwork:22-24` 的既有纪律」「没有操作者时唯一稳定的主体」。
