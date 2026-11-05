# 48 号 v0.2.2 技术规格

> 项目：农场风云 / Farmstorm · 版本 **0.2.2**（MC 1.21.1 / NeoForge 21.1.213 / Java 21）· 基线 **0.2.1 已发布态**（`gradle.properties:11` 现为 `mod_version=0.2.1`）。
> 唯一真相源：`docs/zh/46-DECISIONS-0.2.2.md`（D-187 ~ D-202，共 **16 条**）+ `.work/v022-BRIEF.md` §2 的 **18 条**作者裁定。本文**只引用**决策号与裁定号，不新开决策、不改口径；与 46 号冲突时报给作者，冲突本身登记在 §10。
> 事实底座：`.work/v022-BRIEF.md` §1（源码事实，带行号）、§3（增量与 16 项必改）、§4（RT-62 ~ RT-70）。**本卡与源码冲突时以源码为准**——本文所有行号都是写稿时用 `grep`（UTF-8）与 `[IO.File]::ReadAllLines($p, UTF8)` 重读源码后**自己核过的**，与 `BRIEF` 不一致的地方逐条登记在 §10。
> 反编译原版源码：`.scratch/p25a/src/net/minecraft/`（`BRIEF` §1.1 首段已推翻 0.2.1「本机无原版源码」的判断；本文所有 `*.java:行号` 的**原版**引用都指这一棵树）。该树同时含 `net/neoforged/neoforge/...`，NeoForge 侧引用指同一棵树。
> 体例参照：`docs/zh/42-TECH-0.2.1.md`（**872 行**，只学 §1 基线核对 / §2 类设计 / §3 动作面表 / §4 config 全表 / §6 存档 / §7 风险 / §8 验收 / §9 待验证 / §10 差异登记的**目录结构**，不引用它的任何数值）。
> 红线（两条，均不变）：**无 Mixin（0 个，继续禁止）**；自定义网络包**不设上限**，本版**不新增**，累计仍 **2 个**（D-189、46 号 §4）。
> 写作纪律：注册面 **98**、server config **176 键 / 60 组**、语言键 **340 × 2** 三个数**全部由本文自己逐条点名**（方法写在 §1.2 / §1.4 / §5），不抄 `BRIEF` §0、不抄 46 号 §4.4；凡本文自定的技术参数一律标「**本文确定**」并给理由（汇总见 §0.3）；**不写任务拆分与排期**（属 49 号）、**不写未定事项与占位标记**、不用第二人称敬语。

---

## 0. 阅读约定

### 0.1 本文负责什么

| 项 | 内容 |
|---|---|
| 负责 | 注册面 **98** 的逐类独立点名（§1.2）+ **口径外累计 3 类**的显式登记（§1.3）；config **176 键 / 60 组**与语言键 **338 → 340 × 2** 的逐键点名（§1.4、§5）；七个技术构件（tooltip / 两次 blit / POI 与职业 / 植物学家交易 / 平凡韭菜 / 方块标签与名额 / 两个默认值）的类设计与调用面（§2）；对 0.2.1 的增 / 改 / 不改总表 **33 行**（§3）；存档与兼容（§6）；RT-62 ~ RT-70 的验证方式（§7）；**30 条**验收自检（§8）；待验证 API **10 条**（§9）；与上游的差异登记 **13 条**（§10） |
| 不负责 | 为什么定成 X（46 号）；玩家可见表现、数值表与 27 条边界（47 号）；任务、Phase、里程碑、裁决（49 号）；主提示词与阶段提示词（50 / 51 号） |
| 不动的文件 | `src/` 下**既有**文件一律只读（本文只描述「要改成什么」，不落地代码）；`docs/zh/00` ~ `47`、`docs/en/*`、`.work/*`、`plan0.2.2.txt` 一律只读；本文只新建 `docs/zh/48-TECH-0.2.2.md` 一个文件 |

### 0.2 计数纪律

1. **注册对象、config 键、语言键三个数，本文全部自己数**，并写出「数了哪个文件的哪几行」。不抄 `BRIEF` §0、不抄 46 号 §4.4、不抄 `verify_project.py` 的 `EXPECTED_COUNTS`。
2. 三张表各自满足「行数 = 声明的计数」「小计 = 逐行相加」「跨表不重不漏」。
3. 数值只允许四个来源：46 号的决策号、`BRIEF` §1 / §2 / §3、两个已给数值的算术（`33 + 1 = 34`、`97 + 1 = 98`、`338 + 2 = 340`、`6 × 18 + 17 = 125`、`125 + 96 = 221`、`4 × 24 = 96`、`6 − 3 + 1 = 4`、`64 − 40 + 1 = 25`）、以及本文标「本文确定」的技术参数（每个都给理由）。**不发明玩法数值**。
4. **读文件一律用 UTF-8**。本机实测：`(Get-Content docs\zh\42-TECH-0.2.1.md).Count` = **647**，而 `[IO.File]::ReadAllLines($p, UTF8).Count` = **872** —— 同一份文件、同一条命令、只差 `-Encoding`，行数相差 **225**。⇒ 任何用默认 `Get-Content` 数出来的行号都不可信。本文的行号来自 `grep`（UTF-8）与 `[IO.File]::ReadAllLines($p, UTF8)`；**读原版源码时同样如此**。
5. **「原版」与「本仓」分两棵树**：`net/minecraft/...` 的原版类在 `.scratch/p25a/src/`，本仓类在 `src/main/java/com/dashpig/farmstorm/`。同一个简单类名（例如 `PoiTypes`）只出现在前者，写引用时必须带树名。

### 0.3 「本文确定」的清单（17 项，逐项正文有理由）

| # | 参数 | 值 | 正文 |
|---|---|---|---|
| 1 | `PoiType` 的注册 id | `farmstorm:botanist`（与职业同名，照原版「POI 与职业同名」惯例） | §2.3.2 |
| 2 | `PoiType` 的匹配集 | `Set.of(FarmstormBlocks.OUTPOST_CORE.get().defaultBlockState())`（**1 个** state） | §2.3.2 |
| 3 | `PoiType.maxTickets` | **1**（它就是「一核一村民」的机制本体） | §2.3.3 |
| 4 | `PoiType.validRange` | **1**（与全部原版工作站一致） | §2.3.3 |
| 5 | 职业 id | `farmstorm:botanist` | §2.3.4 |
| 6 | `VillagerProfession` 的六字段取值 | `("botanist", h -> h.is(POI_KEY), h -> h.is(POI_KEY), ImmutableSet.of(), ImmutableSet.of(), null)` | §2.3.4 |
| 7 | POI 标签的补法 | **新增** `datagen/ModPoiTypeTagsProvider extends TagsProvider<PoiType>`，把 `farmstorm:botanist` 加进 `minecraft:acquirable_job_site` | §2.3.5 |
| 8 | 两张新表的注册顺序 | `FarmstormPoiTypes` 的 `.register(modBus)` **先于** `FarmstormVillagerProfessions`，且都在 `FarmstormAttachments` 之后 | §2.3.6 |
| 9 | 绑定判据的落点 | 改 `OutpostCoreBlockEntity:393` 与 `:783` 两处 `instanceof CultivatorBlock` 为标签判定（新增私有 `isProcessingBlock(BlockState)`）；**候选发现保持** `CultivatorBlockEntity.class` | §2.6.3 |
| 10 | 普通交易的类形态 | `record BotanistTrade(Item item)`，22 个静态实例进 `BotanistTrade.NORMAL`；3-6 / 8 / 1 / 0.0F / 2 是**类常量** | §2.4.3 |
| 11 | 史诗交易的类形态 | `record BotanistEpicTrade(List<CropProfile> pool)`，单实例 `EPIC`；池 = 4 个 `CropTier.EPIC` 档案 | §2.4.4 |
| 12 | 两档交易的 `villagerXp` | **2**（与 `TierTrade:45` 一致） | §2.4.5 |
| 13 | `plain_chives` 的物品类 | 直接用 `Item`（**不新建** `PlainCropItem`），`FoodProperties` 固定为 `nutrition 2 / saturationModifier 0.1 / 不 alwaysEdible` | §2.5.2 |
| 14 | `plain_chives` 的堆叠 | 走 `otherProperties()`（`stack.other` 默认 64），**不新开键** | §2.5.2 |
| 15 | `PROCESSING_BLOCK` 的落点 | `FarmstormTags` 第 7 个 `TagKey<Block>`；产物 `data/farmstorm/tags/block/processing_block.json` | §2.6.1 |
| 16 | 语言键的最终值 | **340 × 2**（338 + 2，两条已逐字点名） | §5 |
| 17 | 新增 Java 文件数 | **5**（149 → 154） | §1.1 |

---

## 1. 基线与注册面核对

### 1.1 平台与构建

| 项 | 值 | 证据 |
|---|---|---|
| Minecraft / NeoForge / Java | **1.21.1 / 21.1.213 / 21** | `gradle.properties:2,3`（`minecraft_version` / `neo_version`）；toolchain 21 |
| `mod_version` | **0.2.1**（本版改 **0.2.2**） | `gradle.properties:11` |
| 版本串总数 | **6 处**（2 抬头 + 3 jar 名 + gradle 1 行） | `gradle.properties:11`；`一键构建.cmd:5`（抬头）、`:11`、`:12`（jar / sources-jar）；`检查环境.cmd:5`（抬头）、`:14`（jar） |
| 构建入口 | `.tools\gw.cmd`（唯一入口） | `一键构建.cmd` 只 `call` 它 |
| 运行目录 | `run/` | `run/config/farmstorm-server.toml` 与 `run/config/farmstorm-client.toml` |
| `src/main/java` Java 文件数（现状） | **149** | 本文自己数（`Get-ChildItem -Recurse -Filter *.java` 全树）；本版**新增 5 个类**（§2 逐个点名：`registry/FarmstormPoiTypes`、`registry/FarmstormVillagerProfessions`、`datagen/ModPoiTypeTagsProvider`、`event/BotanistTrade`、`event/BotanistEpicTrade`）⇒ 改后 **154** |
| 注册自检入口 | `Farmstorm#logRegistrySelfCheck`（`Farmstorm.java:161-185`），构造器 `:87` 调用 | `:176-179` 的 12 类断言（`total == 97` 与 `items == 33` 两处要改） |
| server config 生成位置 | `config/FarmstormConfig.java` 的静态块（`outpost_core` 段在 `:936-957`，`SPEC = b.build()` 在文件末段） | §1.4 的计数范围 |
| 语言生成位置 | `datagen/ModLangProvider`，`ENTRIES = buildEntries()`（`:51`）、条目表 `:70` 起 | §5 的计数范围 |

### 1.2 注册面逐类点名（**本文自己从源码数**，确认 **98**）

**计数方法（可复现）**：逐个打开 `src/main/java/com/dashpig/farmstorm/registry/` 下 **12 个**注册类（`FarmstormAttachments` 不在 12 类内，见 §1.3），按「注册调用次数 + 注册 id 字面量」逐条数，**不按 `EXPECTED_COUNTS` 反推、不看 `Farmstorm.java` 的断言值、不抄 `BRIEF` §0**。三条口径：

- `DeferredRegister#register` / `registerItem` / `registerBlock` / `registerComponentType`：数**调用次数**（工厂方法体里的那一次只算一次，`:155` / `:161` 那两行**不重复计**）；
- 走工厂方法注册的（`cropItem` / `seedItem` / `crop` / `deployed` / `projectile`）：数**工厂调用次数**，id 由 `CropProfile.path()` 或首个参数字面量给出；
- `damage_type` 是**数据驱动**（`FarmstormDamageTypes.java:12-17` 类注释：实例由数据包 JSON 提供，代码侧只持 `ResourceKey`），数**静态 `ResourceKey` 常量个数**。

| # | registry | 现在数了什么（文件:行号，**UTF-8 行号**） | 逐条 ID | 0.2.1 | 本版 | 改后 |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | `FarmstormItems.java`：**22** 次 `cropItem(...)`（`:37,39,41,43,45,48,50,52,54,56,58,60,62,64,66,71,73,76,80,86,92,96`）+ **7** 次 `seedItem(...)`（`:90,101,102,103,104,105,107`）+ **4** 次 `registerItem(...)`（`:111,114,117,121`） | 22 作物：`puffed_wheat` `blast_carrot` `ubi_server` `toxic_ubi_server` `power_beet` `melon_slice_bomb` `melon_grenade` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `lucky_berry` `molotov` `zangief_cane` `wild_apple` `compressed_puffed_wheat` `friend_chives` `point_defense_fungus` `vibration_bean`；7 播种物：`chive_seeds` `puffed_wheat_seeds` `power_beet_seeds` `melon_grenade_seeds` `pumpkin_turret_seeds` `awakened_torchflower_seeds` `awakened_pitcher_plant_pod`；4 方块物品：`converter` `compressed_hay_block` `outpost_core` `cultivator` | **33** | **+1** | **34** |
| 2 | `Registries.BLOCK` | `FarmstormBlocks.java`：**15** 次 `crop(...)`（`:28,29,30,31,32,33,34,35,36,37,38,39,40,41,45`）+ **8** 次 `deployed(...)`（`:48,49,50,52,54,57,64,67`）+ **4** 次 `registerBlock(...)`（`:71,75,84,95`） | 15 作物方块：`puffed_wheat_crop` `blast_carrot_crop` `ubi_server_crop` `toxic_ubi_server_crop` `power_beet_crop` `melon_grenade_stem` `pumpkin_turret_stem` `awakened_torchflower_crop` `awakened_pitcher_plant_crop` `fang_trap_cactus_block` `bamboo_firecracker_stalk` `red_mushroom_puff_block` `brown_mushroom_puff_block` `lucky_berry_bush` `friend_chives_crop`；8 部署方块：`blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `zangief_cane` `point_defense_fungus` `vibration_bean`；4 其它：`converter` `compressed_hay_block` `outpost_core` `cultivator` | **27** | +0 | **27** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | `FarmstormBlockEntities.java`：**8** 次 `REGISTER.register(`（`:26,30,33,36,40,44,52,57`） | `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `converter` `outpost_core` `cultivator` | **8** | +0 | **8** |
| 4 | `Registries.ENTITY_TYPE` | `FarmstormEntityTypes.java`：**12** 次工厂 `projectile(...)`（`:31,33,35,37,39,41,43,45,49,51,55,57`，工厂体 `:70-78`）+ **1** 次 `REGISTER.register("turret_bullet", …)`（`:61`） | `puffed_wheat` `ubi_server` `toxic_ubi_server` `melon_slice_bomb` `melon_grenade` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `molotov` `compressed_puffed_wheat` `friend_chives` `vibration_bean` `turret_bullet` | **13** | +0 | **13** |
| 5 | `Registries.MOB_EFFECT` | `FarmstormMobEffects.java:23,27,31,35` | `wild_hunter` `zangief_piledriver` `q_bounce` `confusion` | **4** | +0 | **4** |
| 6 | `Registries.SOUND_EVENT` | `FarmstormSounds.java:20,25,30` | `wild_hunter_loop` `zangief_piledriver` `friend_chives` | **3** | +0 | **3** |
| 7 | `Registries.RECIPE_TYPE` | `FarmstormRecipes.java:22` | `converting` | **1** | +0 | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | `FarmstormRecipes.java:24` | `converting` | **1** | +0 | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | `FarmstormCreativeTabs.java:16` | `main` | **1** | +0 | **1** |
| 10 | `Registries.DAMAGE_TYPE` | `FarmstormDamageTypes.java:20,23,26`（3 个 `ResourceKey` 常量） | `void_bolt` `crop_blast` `zangief_slam` | **3** | +0 | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `FarmstormDataComponents.java:16` | `temporary_enchantments` | **1** | +0 | **1** |
| 12 | `Registries.MENU` | `FarmstormMenus.java:18,22` | `converter` `outpost_core` | **2** | +0 | **2** |
| | **合计** | | | **97** | **+1** | **98** |

逐类小计（与上表逐行相等）：

```
items                 34
blocks                27
block_entity_type      8
entity_type           13
mob_effect             4
sound_event            3
recipe_type            1
recipe_serializer      1
creative_mode_tab      1
damage_type            3
data_component_type    1
menu_type              2
--------------------------------
相加 = 34+27+8+13+4+3+1+1+1+3+1+2 = 98
```

**三条独立算法互为校验**：本文逐条点名 = **98**；`Farmstorm#logRegistrySelfCheck`（`Farmstorm.java:161-185`）的 12 条运行期断言相加 = **98**（改后）；`tools/verify_project.py` 的 `EXPECTED_COUNTS`（`:377-393`）= **98**（改后）。本文的数**不是**从后两者抄的——它是按上表「调用次数」口径重新数出来的，两条旁证只用来确认口径没跑偏。

**本版只有 `items` 动了 1 个，另外 11 类 +0 的四条技术理由**：① tooltip 与两次 blit 都改在既有 `OutpostCoreScreen`（§2.1 / §2.2），**不新增屏幕类**，`menu_types` 仍 2；② 植物学家的两张表是**注册表对象**、不是物品 / 方块（§2.3），`blocks` / `block_entities` 不动；③ 方块标签不是注册对象（§2.6），`blocks` 仍 27；④ `plain_chives` 没有配方（裁定 #3），`recipe_types` / `recipe_serializers` 仍 1 / 1，它也不是投掷物（无 `ThrowableCropItem`），`entity_types` 仍 13。

> ⚠️ **一处会让「+1」误判的口径**：本版新增 5 个 Java 类（§1.1），其中 **3 个一个注册调用都没有**（`ModPoiTypeTagsProvider` 是 DataGen、`BotanistTrade` / `BotanistEpicTrade` 只实现 `VillagerTrades.ItemListing` 接口），另 2 个各 **1 次**注册调用但落在 §1.3 的口径外表。若验收按「新增类数」推断注册面，会得出 103 或 99 的错误结论；注册面只按上表 12 类的**注册调用**数。

### 1.3 口径外累计 3 类逐条登记

`BRIEF` §6 硬禁令第 1 条的例外面：12 类注册面**之外**还有一套内建注册表，它们**不计入 98**，但**必须逐类登记**，不许隐形——「工具不数的东西等于没登记」，那和 A-66 的假绿是同一性质。

| # | 注册表 | 本版数量 | 对象 | 注册点 | 为什么不在 12 类口径内 | 决策 |
|---|---|---|---|---|---|---|
| 1 | `ATTACHMENT_TYPES` | 0（保留既有 1） | `farmstorm:ai_suspend` | `registry/FarmstormAttachments`；`Farmstorm.java:63` | 0.2.1 的裁定 #17 已定性为「12 类口径外的第 13 类」；`NeoForgeRegistries.Keys.ATTACHMENT_TYPES` 不是 `BuiltInRegistries` | 0.2.1 的 D-181 |
| 2 | `VillagerProfession` | **+1** | `farmstorm:botanist` | **新增** `registry/FarmstormVillagerProfessions`；`Farmstorm.java` 构造器 +1 行 | `BRIEF` §1.6：全仓命中 **0**；走原版 `Registries.VILLAGER_PROFESSION`（`Registries.java:198`），不是本项目的 12 类 | D-196 |
| 3 | `PoiType` | **+1** | `farmstorm:botanist`（绑定 `outpost_core`） | **新增** `registry/FarmstormPoiTypes`；`Farmstorm.java` 构造器 +1 行 | 同上，全仓 0 处；走 `Registries.POINT_OF_INTEREST_TYPE`（`Registries.java:177`） | D-196 |

⇒ 本版累计口径外 **3 类**。**它们不进 98，也不许用「塞进数据组件 / 塞进 12 类里的某一类」的方式让数字好看**（46 号 D-189 补充已否决）。

三条逐条说明：

1. **`ATTACHMENT_TYPES` 本版 0 增量**：它只是把 0.2.1 的登记搬进本表凑齐「累计 3 类」。本版对 `FarmstormAttachments` **一行不动**，`Farmstorm.java:63` 那行注册也照旧。
2. **`VillagerProfession` 与 `PoiType` 是同一个 `farmstorm:botanist` 名字、两张不同的表**：两个注册表不同，id 相同不冲突，且这正是原版惯例（`minecraft:farmer` 既是 POI 类型也是职业）。写成两个名字（例如 POI 叫 `farmstorm:botanist_poi`）会让读者以为是两条数据，且与原版惯例不符——**本文确定取同名**（§0.3 第 1 项）。
3. **`verify_project.py` 两类都不核**：它对注册面只做两件事——`EXPECTED`（`:333-375`）的 12 组 id 逐个 grep 字面量、`EXPECTED_COUNTS`（`:377-393`）的 12 个计数相加。两张新表**不在任何一组里** ⇒ 既不核存在、也不核计数。这是已知的覆盖缺口，**本版不扩工具**（46 号 D-188 范围闸），靠 §1.3 + §3 #7/#8/#9 + §8 #8 三处人工登记兜住。

### 1.4 config 与语言键基线

**SERVER config（现状 176 键 / 60 组）**：数法是读 `config/FarmstormConfig.java`，按「`b.push("…")` / `b.pop()` 维护组栈、行首 `.define*(` 计一键」逐键落地。本文跑了两遍并逐组人工复核：

```
行首 .define* 的匹配行数        = 177   ← 含 1 行被注释掉的
被注释的定义                    =   1   ← :940 的 // .defineInRange("farmlandBonus", 16, 0, 1024);
                                  ⇒ 177 − 1 = 176 键
b.push(...) 次数                =  60
b.pop() 次数                    =  60
                                  ⇒ 176 键 / 60 组
```

三个可复现的旁证：① `validate` 之外没有第二处 `define`；② `outpost_core` 段落在 `:935` 的 `b.push("outpost_core")` ~ `:957` 的 `b.pop()`，其中 **4** 个 live 键在 `:943`（`maxPerPlayer`）/ `:946`（`boundaryParticleIntervalTicks`）/ `:951`（`storageSlots`）/ `:956`（`cultivatorCapacity`），`farmlandBonus` 在 `:940` 已注释；③ 盘上 `run/config/farmstorm-server.toml` 与源码键集一致（`maxPerPlayer` 在 `:753`、`cultivatorCapacity` 在 `:765`，见 §6.3）。

**CLIENT config（现状 3 键）**：`config/FarmstormClientConfig` 的 `hud.enabled` / `hud.x` / `hud.y`（`builder.push("hud")` 一次）。本版**不动**，且**不计入** 176（`BRIEF` §0 的既有口径），但**计入** §5 的语言键。

**语言键（现状 338 × 2，中英键集完全相同）**：读 `src/generated/resources/assets/farmstorm/lang/zh_cn.json` 与 `en_us.json`，按行首 `"键":` 计（`ConvertFrom-Json` 在中文那份上会因本机默认编码读出乱码而解析失败，**必须**用行匹配或 `-Encoding UTF8`）：`zh = 338`、`en = 338`、`zh-only = **0**`、`en-only = **0**`；两份文件的物理行数都是 **340**（338 键 + `{` + `}`）。

`datagen/ModLangProvider` 的类注释（`:14-19`）自己给了条目构成，本文复核与实测一致：

```
33 item + 27 block + 4 effect + 3 subtitles + 179 config（= 176 server + 3 client）
+ 36 advancement + 6 death + 20 tooltip + 13 entity + 9 gui + 2 container
+ 1 itemGroup + 0 message + 2 key + 3 command = 338
```

### 1.5 两条红线

| 项 | 现状（本文复核） | v0.2.2 | 证据 |
|---|---|---|---|
| 自定义网络包 | **2 个**：`farmstorm:core_list`、`farmstorm:cultivator_link` | **2 个，不新增**，协议串仍 `"2"` | `network/CultivatorLinkPayload.java:35`（`MAX_LINKS = 1024`）、`:38`（注册名）；本版对这两个包**只改一段注释**（§2.7.4） |
| Mixin | **0 个** | **0 个**，继续禁止 | `src/` 全树 `@Mixin` 命中 **0**、`*.mixins.json` **0**；`build.gradle` / `neoforge.mods.toml` 无 mixin 段 |

两条读法（写错任一条，§8 的 #5 会红）：

1. **网络包不进注册面**——payload 走 `RegisterPayloadHandlersEvent`，不写 `BuiltInRegistries`，所以 §1.2 的 98 里没有它。
2. **Mixin 的 0 是禁项，不是「本版没轮到」**：本版七个构件全部用注册表 + 事件总线 + 数据驱动实现——尤其**没有**用 Mixin 去改原版 `Villager#restock` 或 `Villager#updateTrades` 来实现「每次补货重抽」（那同时踩红线与裁定 #18）。POI 与职业是**原版留出来的注册表**，交易是**原版留出来的事件**（§2.3 / §2.4 逐条给证据）。

---

## 2. 技术构件

### 2.1 tooltip（Bug A）：补 `render` 覆写

#### 2.1.1 现状与根因（逐条复核）

| # | 事实 | 出处（本文重读） |
|---|---|---|
| 1 | 原版 `AbstractContainerScreen#render` 在 `:91`，函数体内**没有** `renderTooltip` 调用 | `.scratch/p25a/src/net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java:91`（全文 `renderTooltip` 只出现在 `:176`（定义）与 `:179`（它自己体内的一次调用），**`render` 里 0 次**） |
| 2 | `AbstractContainerScreen#renderTooltip` 存在、`protected`、全类**无人调用** | 同上 `:176-181` |
| 3 | **每个具体界面必须自己调**，原版箱子界面的指定写法是「`super.render(...)` 之后一句 `this.renderTooltip(...)`」 | 同上树 `ContainerScreen.java:25-29`（`:26` 起 `render` 覆写、`:27` `super.render`、`:28` `this.renderTooltip`） |
| 4 | 本模组 `OutpostCoreScreen`（**42 行**）**只覆写了 `renderBg`**（`:37-41`），`render` 覆写 **0 处** | `client/OutpostCoreScreen.java:21-42` |

⇒ **根因是「缺一行调用」**。1.21.1 的默认链路**不包含** tooltip，`OutpostCoreScreen` 没有补那句调用，就只有背景、没有 tooltip。修法 = 补 `render` 覆写，**不是调渲染参数、不是自绘、不是优化**（D-190、`BRIEF` §6 硬禁令第 2 条）。

#### 2.1.2 目标代码形态（本文确定）

```java
@Override
public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);
    this.renderTooltip(graphics, mouseX, mouseY);
}
```

四条形态约束（写错任一条，§8 #20 ~ #23 会红）：

1. **必须先 `super.render(...)`**：它画背景、槽位、`renderLabels` 与拖拽物；调反顺序会让 tooltip 被背景盖住。0.2.1 的四条否决里「覆写 `render` 后不调 `super`」一条**原样保留**（46 号 §5）。
2. **`renderTooltip` 是 `protected`**（`AbstractContainerScreen.java:176`），子类可以直接调；不要把它改成 `public`、不要新写一个同名方法。
3. **签名必须是 4 参**（`GuiGraphics, int, int, float`），与 `AbstractContainerScreen:91` 逐字一致；写成 3 参会变成**新的重载**而不覆盖，`@Override` 直接编译失败。
4. **不碰 `renderBg` / `renderLabels` / `init` / `Slot`**：tooltip 的定位、缩放、右侧越界翻转全由原版那次调用负责（`ContainerScreen` 自己也不接管）。

#### 2.1.3 这一条推翻了 0.2.1 的判断（不重述 46 号，只留技术结论）

0.2.1 的 41 号 §3.2 认为「原版 `render` 自己会调 `renderTooltip`，没有人拦它」，于是 F-251 只加验收、不动代码。**这句是错的**（D-192）：原版的 `render` 里根本没有这一步，「没人拦」不等于「有人调」。本版**不再把「验收通过而代码一行没改」当正确结果**——§2.1.2 的六行代码是必改项（§3 #6）。

`OutpostCoreScreen` 改后仍是**一个**类、**一个** `renderBg` 覆写 + **一个** `render` 覆写，行数 42 → 约 47，`menu_types` 不变（§1.2 第 12 行）。

### 2.2 两次 blit（Bug B）

#### 2.2.1 现状与根因（逐条复核）

| # | 事实 | 出处 |
|---|---|---|
| 1 | 原版 `renderBg` 是**两次 blit**：上段 `v=0`、高 `rows*18 + 17`；下段 `v=126`、高 **96**、画到 **`y = rows*18 + 17`** | `.scratch/.../ContainerScreen.java:32-37`（`:35` 上段、`:36` 下段；`rows = 6` 时 125 / 96） |
| 2 | **下段把贴图 `v=126` 画到 GUI `y=125`**——原版自身就错开 1 px，这是**必须照抄**的常数，不是笔误 | 同上 `:36` 逐字 |
| 3 | 本模组是**一次 blit 整张**（`v=0 → y=0`，176 × 222）⇒ 背包那一段落在 **`y=126`**，比原版**低 1 px** | `client/OutpostCoreScreen.java:37-41` |
| 4 | 槽位坐标仍是 `139 / 157 / 175 / 197`，与原版 `ChestMenu` 6 行常量一致 | `menu/OutpostCoreMenu.java:44`（`INVENTORY_Y = 139`）、`:46`（`HOTBAR_Y = 197`），3 行主背包由 `:80` 加 `row * 18` 得到 157 / 175 |
| 5 | ⇒ **背景格子比物品低 1 px**（等价地：物品比格子高 1 px） | 由 3 + 4 推出 |

⇒ **根因是「单次 blit vs 两次 blit」**，**不是槽位坐标写错**（坐标本就与原版一致），也**不是贴图尺寸传错**（`:39` 传的是 `256, 256`，类注释 `:16-19` 记过这件事）。

#### 2.2.2 目标代码形态（本文确定）

```java
@Override
protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    int x = this.leftPos;
    int y = this.topPos;
    int upper = ROWS * 18 + 17;                       // 6 × 18 + 17 = 125
    // 上段：54 格区，贴图 v=0
    graphics.blit(BACKGROUND, x, y, 0.0F, 0.0F, IMAGE_WIDTH, upper, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    // 下段：玩家背包 + 快捷栏，贴图 v=126 画到 y=125（1 px 错开是原版自身的行为）
    graphics.blit(BACKGROUND, x, y + upper, 0.0F, 126.0F, IMAGE_WIDTH, 96, TEXTURE_WIDTH, TEXTURE_HEIGHT);
}
```

五条形态约束：

1. **两段的 `v` 分别是 0 与 126**（不是 0 与 125）：125 是**目标 y**，126 是**贴图 v**，两个数在 `ContainerScreen:35-36` 里本来就不同，混用会让下段整条贴图错 1 px。
2. **下段高度固定 96**（不是 `IMAGE_HEIGHT − 125 = 97`）：原版就是 96，`125 + 96 = 221`，比 `imageHeight` 的 **222** 少 1 px，最后 1 px **不画**（照抄，不补第三次 blit）。
3. **9 参 `blit` 的贴图尺寸仍是 `TEXTURE_WIDTH / TEXTURE_HEIGHT`（256 × 256）**：改成 `IMAGE_WIDTH / IMAGE_HEIGHT` 会把 256 的图按 176 × 222 重采样，槽位框整体错开（类注释 `:16-19` 已记过这个反向错误）。
4. **槽位 `139 / 157 / 175 / 197` 一个都不动**（`OutpostCoreMenu:44-46`、`:80`）：错位是 blit 次数造成的，把槽位往下推 1 px 会让 54 个格子与物品一起错。
5. **`imageHeight`（222）、`inventoryLabelY`（128）、贴图尺寸都不动**：`OutpostCoreScreen:24-28` / `:32-34` 与原版 `ContainerScreen:20-22` 的算法同形。

`ROWS` 从 `OutpostCoreMenu.ROWS`（= 6，`OutpostCoreMenu:31`）取，**不写死 6**，也不新开常量——`OutpostCoreMenu` 已经是「6 行 × 9 列」的唯一来源。

### 2.3 POI 与职业：两张全新注册表

#### 2.3.1 为什么必须两张表，以及「加职业 ≠ 加交易项」

现有交易实现（`event/VillagerTradeEvents.java`，37 行）只在 `VillagerProfession.FARMER` 上挂 3 项（`:30-35`）。本版要加的是**职业**，而职业在原版里由**两个注册表**共同定义：`PoiType`（哪个方块位置算工作点）与 `VillagerProfession`（职业本身 + 它认哪类工作点）。全仓 `PoiType` / `VILLAGER_PROFESSION` / `POINT_OF_INTEREST` 命中 **0**（本文实测 `src/` 全树 0 命中）⇒ 本版是**第一次**引入这两张表，且**都不在 12 类口径内**（§1.3 第 2、3 行）。

#### 2.3.2 `PoiType`：id、匹配集、构造函数

| 项 | 本文确定 | 依据 |
|---|---|---|
| 类 | **新增** `registry/FarmstormPoiTypes`：`DeferredRegister<PoiType> REGISTER = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Farmstorm.MOD_ID)` | `Registries.java:177`；范式同 `FarmstormMobEffects` 等既有 12 类 |
| 注册 id | `farmstorm:botanist` | §0.3 第 1 项：与原版「POI 与职业同名」惯例一致 |
| 常量 | `DeferredHolder<PoiType, PoiType> BOTANIST`（供职业的谓词与标签 provider 用） | — |
| 匹配集 | `Set.of(FarmstormBlocks.OUTPOST_CORE.get().defaultBlockState())`（**恰好 1 个** state） | `OutpostCoreBlock:40-49`：类里**没有** `createBlockStateDefinition`、**没有**任何 `Property` ⇒ 该方块只有 1 个 state；46 号 D-196 补充已给同一结论 |
| `maxTickets` | **1** | §2.3.3 |
| `validRange` | **1** | §2.3.3 |
| 构造 | `new PoiType(states, maxTickets, validRange)` | `.scratch/.../PoiType.java:8`（record 三字段）、`:11-16`（NeoForge 把集合包进 `PoiStateSet`） |

**两处 API 事实（已核，不必再验）**：

1. **NeoForge 自动把匹配集登记进 `PoiTypes.TYPE_BY_STATE`**，不需要我们自己调 `PoiTypes.registerBlockStates`（那个方法是 `private`）。证据：`PoiTypes.java:121-122` 的注释原文「Neo: we do this automatically for modded PoiTypes in NeoForgeRegistryCallbacks」，实现在 `.scratch/.../NeoForgeRegistryCallbacks.java:106-126`（`PoiTypeCallbacks.onAdd` 把 `value.matchingStates()` 逐个 `put` 进 `BLOCKSTATE_TO_POI_TYPE_MAP`）。
2. **同一个 BlockState 不许出现在两个 POI 类型里**：`NeoForgeRegistryCallbacks.java:110-116` 在重复时抛 `IllegalStateException`（原文「Blockstates can only have one point of interest type each」）。`outpost_core` 现在没有任何 POI 类型 ⇒ 本版不会撞，但**不许**把 `outpost_core` 顺手塞进原版某个 POI 类型（例如借用 `minecraft:farmer`），那会同时撞这条和原版农民。

`PoiStateSet`（`PoiStateSet.java:18-23`）是 `ReferenceOpenHashSet` 的包装 ⇒ 匹配是**引用比较**，必须传 `Block#getStateDefinition()` 里的**单例** state，`defaultBlockState()` 就是它。不要 `BlockState` 反序列化或手搓实例。

#### 2.3.3 为什么 `maxTickets = 1` 就是「一核一村民」，`validRange = 1` 是什么

裁定 #9 的「一核一村民」不是要我们写判定，它**就是** `maxTickets` 的语义：

| 事实 | 出处 |
|---|---|
| `PoiRecord` 的 `freeTickets` 初值 = `poiType.maxTickets()` | `.scratch/.../poi/PoiRecord.java:38-40` |
| `acquireTicket()` 在 `freeTickets <= 0` 时**返回 false**（占票失败） | 同上 `:48-50` |
| `hasSpace()` = `freeTickets > 0` | 同上 `:68-70` |

⇒ `maxTickets = 1` ⇒ 一个方块位置**只有一张票** ⇒ 第二个村民来认领时占票失败，会去找下一个工作点。**这就是裁定 #9 的原版机制本体**，也是全部原版工作站的值（`PoiTypes.bootstrap` 里 13 个职业工作站全是 `maxTickets = 1, validRange = 1`，唯一例外是 `MEETING` 的 `32 / 6`）。

`validRange = 1` 与全部原版工作站一致（同上 `bootstrap`），取 1 的含义是「工作点就在这一格」，不额外放宽。

#### 2.3.4 `VillagerProfession`：六字段逐个定死

`VillagerProfession` 是 record，六字段（`.scratch/.../VillagerProfession.java:21-28`）：

```java
public record VillagerProfession(
    String name,
    Predicate<Holder<PoiType>> heldJobSite,
    Predicate<Holder<PoiType>> acquirableJobSite,
    ImmutableSet<Item> requestedItems,
    ImmutableSet<Block> secondaryPoi,
    @Nullable SoundEvent workSound
)
```

| 字段 | 本文确定 | 理由与依据 |
|---|---|---|
| `name` | `"botanist"` | 只用于 `toString()`（`:52-55`）；**玩家看到的职业名走语言键**，与 `name` 无关（`Villager.java:743-749` 用注册表 key 拼键） |
| `heldJobSite` | `holder -> holder.is(FarmstormPoiTypes.BOTANIST_KEY)` | 「已经是我的人，还认不认这个工作点」。原版同形（`:57-59`）。**不按方块、不按 `ownerUuid`、不按距离**（46 号 D-196 补充的纪律） |
| `acquirableJobSite` | 同上（同一个 key） | 「重新找工作点时认哪类」。原版同形（`:58` 两个谓词传同一个 key）。**不许**用 `VillagerProfession.ALL_ACQUIRABLE_JOBS`——那会把原版 13 种工作站全变成它的候选 |
| `requestedItems` | `ImmutableSet.of()` | 影响 `wantsToPickUp`（`Villager.java:839`）。空集 = 植物学家不捡地上的东西。给物品会让它去捡玩家丢的货，本版不需要 |
| `secondaryPoi` | `ImmutableSet.of()` | 只有农民用它（`Villager.java:838` 的 `VILLAGER_FARMLANDS` 判定）；植物学家没有副工作点 |
| `workSound` | `null` | ① 本版**不新增音效**（`sound_events` +0，§1.2 第 6 行）；② `null` 是安全的：`Villager#playWorkSound`（`:582-584`）把 `workSound()` 交给 `LivingEntity#makeSound(@Nullable SoundEvent)`（`LivingEntity.java:1325-1329`），后者 `if (p_320810_ != null)` 才响；③ 借原版农民音效会让玩家以为它是农民 |

**职业 id 与语言键**：id `farmstorm:botanist`；语言键 `entity.minecraft.villager.farmstorm.botanist`，键形由 `Villager#getTypeName`（`Villager.java:743-749`）决定：`getType().getDescriptionId()`（= `entity.minecraft.villager`）+ `.` + 非 `minecraft` 命名空间时的 `命名空间 + "."` + `path` ⇒ 必带 `farmstorm.` 段。**写成 `entity.minecraft.villager.botanist` 是错的**（少了命名空间段，键永远命中不到，玩家看到 raw key）。

#### 2.3.5 POI 标签：不加就永远不认领（本版最隐蔽的一处）

这是 46 号留给 48 号的口子里最硬的一条，也是 RT-64 的**主形态**。**代码路径已逐行核过**：

| 步 | 发生了什么 | 出处 |
|---|---|---|
| 1 | 村民把「候选工作点」记在 `POTENTIAL_JOB_SITE` 这个 memory 里，它的谓词**不是**职业的 `acquirableJobSite`，而是 `VillagerProfession.ALL_ACQUIRABLE_JOBS` | `Villager.java:176-185` 的 `POI_MEMORIES`：`MemoryModuleType.POTENTIAL_JOB_SITE` → `(v, holder) -> VillagerProfession.ALL_ACQUIRABLE_JOBS.test(holder)`（`:181-182`） |
| 2 | `ALL_ACQUIRABLE_JOBS` = `holder.is(PoiTypeTags.ACQUIRABLE_JOB_SITE)`，即**标签** `minecraft:acquirable_job_site` | `VillagerProfession.java:29`；`PoiTypeTags.java:8` |
| 3 | 无业村民的 `AcquirePoi` 用的正是 `profession.acquirableJobSite()`；无业时职业是 `NONE`，它的谓词是 `ALL_ACQUIRABLE_JOBS` ⇒ **又是那个标签** | `VillagerGoalPackages.java:38`（`ValidateNearbyPoi`）、`:43`（`AcquirePoi`）；`VillagerProfession.java:30`（`NONE` 用 `ALL_ACQUIRABLE_JOBS`） |
| 4 | 村民走到工作点 2 格内后，`AssignProfessionFromJobSite` 才按「**当前工作点的 POI 类型**」反查职业：遍历 `BuiltInRegistries.VILLAGER_PROFESSION`，取**第一个** `heldJobSite().test(poiType)` 成立的职业 | `.scratch/.../ai/behavior/AssignProfessionFromJobSite.java:37-41`（`findFirst()`）、`:43`（`setVillagerData`）、`:24`（2.0 格判据） |

⇒ **必须把 `farmstorm:botanist` 加进 `minecraft:acquirable_job_site`**。不加的后果不是报错，是**永远不认领**：无业村民的候选集里根本没有我们的 POI，第 4 步永远不会被走到。这与 RT-64 描述的表现（「村民不认领」而非报错）完全吻合。

**补法（本文确定）**：**新增** `datagen/ModPoiTypeTagsProvider`：

```java
public final class ModPoiTypeTagsProvider extends TagsProvider<PoiType> {
    public ModPoiTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Registries.POINT_OF_INTEREST_TYPE, lookup, Farmstorm.MOD_ID, null);
    }
    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(FarmstormPoiTypes.BOTANIST_KEY);
    }
}
```

四条形态约束：

1. **不要 `extends` 原版 `PoiTypeTagsProvider`**（`.scratch/.../data/tags/PoiTypeTagsProvider.java:11`）：它的 `addTags`（`:21-41`）会连带重写 `minecraft:village` 与 `minecraft:bee_home` 两条标签（`:39-40`），把原版内容复制进我们的数据包，属于「不该由我们写」的文件。直接继承抽象 `TagsProvider<PoiType>`，只写一条。
2. **构造器用 NeoForge 的 5 参变体**（`TagsProvider.java:47`：`(PackOutput, ResourceKey<? extends Registry<T>>, CompletableFuture<HolderLookup.Provider>, String modId, @Nullable ExistingFileHelper)`），与 `ModBlockTagsProvider:19` 同形（第四个参数传 `null`，因为标签引用注册表条目而不是资源文件）。
3. **产物在 `data/minecraft/tags/point_of_interest_type/acquirable_job_site.json`**（`minecraft:` 命名空间的标签由我们**补**一条），**不是** `data/farmstorm/...`。写错目录等于没加。
4. **不需要**把 POI 类型也加进 `minecraft:village`：那条只影响「村庄判定」（`PoiTypeTagsProvider:39`），与认领无关。

#### 2.3.6 注册顺序与三处接线

| # | 位置 | 改动 |
|---|---|---|
| 1 | **新增** `registry/FarmstormPoiTypes` | 1 个 `DeferredRegister<PoiType>` + 1 个 `BOTANIST` + 1 个 `BOTANIST_KEY`（`ResourceKey<PoiType>`，供职业谓词与标签 provider 引用） |
| 2 | **新增** `registry/FarmstormVillagerProfessions` | 1 个 `DeferredRegister<VillagerProfession>` + 1 个 `BOTANIST` |
| 3 | `Farmstorm.java` 构造器（`:47-63` 的 **12** 行 `.register(modBus)` 区） | **+2 行**，顺序：`FarmstormPoiTypes.REGISTER.register(modBus);` 在 `FarmstormVillagerProfessions.REGISTER.register(modBus);` **之前**，两行都放在 `FarmstormAttachments`（`:63`）之后 |

**顺序的理由（本文确定）**：`FarmstormVillagerProfessions` 的谓词要引用 `FarmstormPoiTypes.BOTANIST_KEY`。`ResourceKey` 是纯常量、本身与注册先后无关，但**保持「先 POI 后职业」**有两条实际收益：① 读代码的人按原版 `VillagerProfession:31-50` 的顺序（先 `PoiTypes.X` 后职业）理解，不会怀疑「职业引用了还没注册的 POI」；② 将来若把谓词改成 `FarmstormPoiTypes.BOTANIST::isBound` 之类的 `DeferredHolder` 调用，顺序已经是安全的。

**`Farmstorm.java:176-179` 的断言必须同步改**：`total == 97` → `98`、`items == 33` → `34`，其余 10 个 `==` 一个不动。**这一条 `BRIEF` §3.4 没列**（登记在 §10 第 1 条），不改的后果是启动日志直接打 `→ FAIL`（`Farmstorm.java:180-184` 的格式串），§8 #1 必红。

### 2.4 植物学家的交易：22 个 listing 与 1 个史诗

#### 2.4.1 事件面：模组职业**会**收到 `VillagerTradesEvent`

| 事实 | 出处 |
|---|---|
| 事件对**每一个已注册职业**各抛一次（遍历 `BuiltInRegistries.VILLAGER_PROFESSION`） | `.scratch/.../neoforge/common/VillagerTradingManager.java:60-75`（`:61` 的 `for`、`:70` 的 `post`） |
| 1 ~ 5 级**全部**会被填成非 null 的可变列表 | 同上 `:64-66`（`for (int i = 1; i < 6; i++) mutableTrades.put(i, NonNullList.create())`）；类注释 `VillagerTradesEvent.java:26` 原文「The map is populated for levels 1-5 (inclusive), so Map#get will never return null for those keys」 |
| 事件结束后写回 `VillagerTrades.TRADES` | 同上 `:71-73` |
| 触发的**唯一**时机是 `TagsUpdatedEvent` 且 `UpdateCause == SERVER_DATA_LOAD` | 同上 `:37-42` |
| 村民升级时从**该级**列表里抽 **2** 条 | `Villager.java:871-877`（`:875` `addOffersFromItemListings(merchantoffers, avillagertrades$itemlisting, 2)`） |
| 抽样语义 = **随机、不放回、最多 N 条** | `AbstractVillager.java:233-244`（`:237` `while (i < N && !list.isEmpty())`、`:238` `arraylist.remove(random.nextInt(size))`） |

⇒ 三条直接后果（**必须成对写**，否则玩家会以为交易表坏了）：

1. **22 个 listing ≠ 22 项可见**：22 条全部进 1 级 ⇒ `Villager.java:875` 写死只抽 2 条 ⇒ **每个植物学家只显示 2 项**（D-198、裁定 #17）。
2. **一个满级植物学家的报价总数 = 3 项**（1 级可见 2 + 5 级 1）。
3. **`/reload` 不会重发该事件**（`VillagerTradingManager:38` 只认 `SERVER_DATA_LOAD`）⇒ 验收改完交易必须**重启服务端**，`/reload` 之后看到旧列表是正常的，不是「没生效」。

#### 2.4.2 22 项普通交易：清单与数值

| 项 | 本文确定 | 依据 |
|---|---|---|
| 项数 | **22** 条 `ItemListing` | 裁定 #10；D-197 |
| 组成 | 20 个原版物品 + `plain_chives` + `compressed_puffed_wheat` | 裁定 #10；47 号 §2.3 的 22 行表就是**唯一清单**，本文不复制第 3 份 |
| 等级 | **全部进 1 级**（`event.getTrades().get(1)`） | 裁定 #11 |
| 价格 | **3 ~ 6** 绿宝石，闭区间（`6 − 3 + 1 = 4` 个取值） | 裁定 #12；取法与 `TierTrade:75` 同形 |
| `maxUses` | **8** | 裁定 #12 |
| 每次给几个 | **1** | 裁定 #12（性质见 47 号 §0.2 第 2 条） |
| `priceMultiplier` | **0.0F**（关掉供需涨价，声望折扣照常） | 47 号 §2.5；沿用 `TierTrade:47` |
| `villagerXp` | **2** | 本文确定（§0.3 第 12 项），沿用 `TierTrade:45` |
| 报价方向 | 玩家**付绿宝石**、**得到物品** | 沿用 `TierTrade:77-79` 的写法 |
| 价格来源 | **类常量**，不读 `trade.*` 的 6 个键、**不新增键** | 由 D-189 的「176 不变」推出（46 号 D-197 补充） |

#### 2.4.3 类形态（本文确定）

```java
public record BotanistTrade(Item item) implements VillagerTrades.ItemListing {
    public static final int MIN_PRICE = 3;
    public static final int MAX_PRICE = 6;
    public static final int MAX_USES = 8;
    public static final int COUNT = 1;
    public static final int VILLAGER_XP = 2;
    public static final float PRICE_MULTIPLIER = 0.0F;
    /** 22 项，顺序 = 47 号 §2.3 表的 1~22 行。 */
    public static final List<BotanistTrade> NORMAL = List.of(/* 22 个实例 */);
    // getOffer：闭区间随机价 → new MerchantOffer(new ItemCost(Items.EMERALD, price),
    //           Optional.empty(), new ItemStack(item, COUNT), MAX_USES, VILLAGER_XP, PRICE_MULTIPLIER)
}
```

四条约束：

1. **不复用 `TierTrade`**：它按 `CropTier` 取池、每项给 **2** 个（`TierTrade:41`）、`maxUses = 1`（`:43`），三个数与池的形状都不对；20 个原版物品根本不是 `CropProfile`，塞不进那个构造器（46 号 D-197、D-200 补充）。
2. **清单是显式 `List<Item>`，不是从 `CropProfile` 派生**：22 项里 20 项是 `minecraft:` 物品、1 项是本模组非作物物品、1 项是二级合成物，没有任何一个共同字段能推导出来。顺序照 47 号 §2.3，**写死 22 个实例**。
3. **`getOffer` 不做抽签**：普通项是「固定卖这一件」，抽签发生在原版抽样（`Villager.java:875`）那一层。自己再抽一次会让「22 项」变成「22 × 每次刷新」。
4. **`getOffer` 不许返回 `null`**：`AbstractVillager:238-242` 对 `null` 的处理是「不 add、`i` 不 ++、继续循环」⇒ 返回 null 只是让这一条静默消失，不是报错。

#### 2.4.4 史诗交易：固定 1 项、首次抽定

```java
public record BotanistEpicTrade(List<CropProfile> pool) implements VillagerTrades.ItemListing {
    public static final int MIN_PRICE = 40;
    public static final int MAX_PRICE = 64;   // 64 − 40 + 1 = 25 个取值
    public static final int MAX_USES = 1;
    public static final int COUNT = 1;
    public static final int VILLAGER_XP = 2;
    public static final float PRICE_MULTIPLIER = 0.0F;
    public static final BotanistEpicTrade EPIC = new BotanistEpicTrade(
            CropProfile.ALL.stream().filter(p -> p.tier() == CropTier.EPIC).toList());
    // getOffer：pool.get(random.nextInt(pool.size())) → FarmstormItems.forProfile(picked).get() → 报价
}
```

| 项 | 本文确定 | 依据 |
|---|---|---|
| 项数 | **1** 条 listing，进 **5 级**（`getTrades().get(5)`） | 裁定 #13 + 18；D-199；5 级是 46 号 D-199 的「本文补充」 |
| 池 | `CropProfile.ALL` 里 `tier() == EPIC` 的 4 条（`CropProfile:74,78,89,105`，本文复核 4 条） | 裁定 #18；`CropTier.java` 的 `EPIC` 注释「4 条」 |
| 抽取时机 | **`getOffer` 内一次**（首次生成报价时），之后整个存档不变 | 裁定 #18；机制同 `TierTrade:62-79` |
| 补货 | 只 `resetUses()`，**不重调 `getOffer`** | `Villager.java:398-408`（`:401-403` 只有 `merchantoffer.resetUses()`） |
| 价格 | **40 ~ 64**，闭区间 | 裁定 #13 |
| `maxUses` | **1** | 裁定 #13 |
| 每次给几个 | **1** | 47 号 §2.6（本文补充，可否决） |

**「固定 1 项」不是「4 个都卖」**：一个植物学家只有 1 项，凑齐 4 个史诗作物同样要多找几个植物学家。这是**作者选定的机制**（裁定 #18），不是原版限制。

**plan 的「额外刷新出」是口径差异**：`plan0.2.2.txt:11` 的措辞会被读成「每次补货换一个」，真实口径是首次生成时抽定、之后不变（RT-67）。**「每次补货重抽」这五个字不许在任何下游文档里单独出现。**

#### 2.4.5 分支接线：不动农民那三行

`event/VillagerTradeEvents#onVillagerTrades`（`:29-36`）改成两段，**农民的 3 行原样保留**：

```java
private static void onVillagerTrades(VillagerTradesEvent event) {
    if (event.getType() == FarmstormVillagerProfessions.BOTANIST.get()) {
        BotanistTrade.NORMAL.forEach(listing -> event.getTrades().get(1).add(listing));  // 22 条全进 1 级
        event.getTrades().get(5).add(BotanistEpicTrade.EPIC);                            // 史诗进 5 级
        return;
    }
    if (event.getType() != VillagerProfession.FARMER) {
        return;
    }
    event.getTrades().get(1).add(TierTrade.COMMON);   // ← 以下三行一行不动
    event.getTrades().get(3).add(TierTrade.RARE);
    event.getTrades().get(5).add(TierTrade.EPIC);
}
```

四条约束：

1. **植物学家分支写在农民早退之前**，且**新加** `return`；不许把两个职业的判断合并成一个 `if`（合并会改到农民的口径，等于推翻裁定 #7）。
2. **`event.getType() ==` 用引用比较**：两边都是注册表单例（`DeferredHolder#get()` 返回同一个对象），与原写法 `:30` 一致。
3. **`getTrades().get(1)` 与 `.get(5)` 都不会返回 null**（`VillagerTradingManager:64-66`），所以直接 `add`；不需要 0.2.1 注释里担心的 `put(new ArrayList<>(...))` 退路。
4. **`VillagerTrades.TRADES` 的写回由管理器负责**（`:73`），我们只往列表里 `add`。

**一处注释漂移（本版顺手改）**：`VillagerTradeEvents:10-11` 写「每次 `TagsUpdatedEvent`（数据包重载，含起服）」，实际只在 `SERVER_DATA_LOAD` 抛（`VillagerTradingManager:38`），`/reload` 不重发。登记在 §10 第 7 条。

### 2.5 `plain_chives`：唯一一个非作物物品

#### 2.5.1 身份与形态

| 项 | 值 | 依据 |
|---|---|---|
| 注册 id | `farmstorm:plain_chives` | 裁定 #4 |
| 中文名 / 语言键 | **平凡韭菜** / `item.farmstorm.plain_chives` | 裁定 #4；键形同既有 `item.farmstorm.puffed_wheat` |
| 物品类 | **直接用 `Item`**，不新建 `PlainCropItem` | §0.3 第 13 项：它没有任何专属行为（不能种、不能投、不能部署），自建类只多一个空壳 |
| 注册入口 | `REGISTER.registerItem("plain_chives", Item::new, props)`（**不走 `cropItem`**） | `BRIEF` §1.5：`cropItem` 要 `CropProfile`，它没有档案 |
| 堆叠 | **64**（走 `otherProperties()` → `stack.other`） | `FarmstormItems:175-177`；`FarmstormConfig:311-313` 默认 64；**不新开键** |
| 食物属性 | **固定常量**，不读 config | D-193：`foodProperties(profile, …)`（`FarmstormItems:179-187`）要 `CropProfile`，它没有 ⇒ 营养 / 饱和度只能是常量；开两个键会让 176 变 178 |
| 创造栏 | **进** `creativeTabItems()` | D-193；RT-69 |

#### 2.5.2 食物属性（本文确定）

```java
FoodProperties food = new FoodProperties.Builder()
        .nutrition(2)                 // 裁定 #4 的「饱食 2」→ nutrition
        .saturationModifier(0.1F)     // 47 号 §3.3 的配套值（原版甜浆果 2 / 0.1）
        .build();                     // 不调 alwaysEdible()
```

三条理由（与 47 号 §3.3 一致，此处只留技术面）：

1. **`nutrition` 是必填整数（0 ~ 20）**，裁定 #4 给的是整数 2；`saturationModifier` 是 double 乘法修正，把它设成 2 会得到一个「恢复 0 点饥饿值」的可食物品。
2. **不带 `alwaysEdible()`**：现有 `foodProperties(...)`（`FarmstormItems:184`）**带**它，那是食用型作物的口径；平凡韭菜要与原版食物一致（饱食度满时吃不下），所以**不能复用那个方法**——这也是它必须单独写 `Item.Properties` 的第二个理由。
3. **`saturationModifier = 0.1` 取原版甜浆果的配套值**：甜浆果正是 `lucky_berry` 的对应原版物品，同值不算新比例。

#### 2.5.3 三个「加了但拿不到」的坑（RT-69 的技术面）

| # | 坑 | 现状 | 本版要做 |
|---|---|---|---|
| 1 | **物品模型** | `ModItemModelProvider#farmstormItems()`（`:85-94`）遍历 `BuiltInRegistries.ITEM` 里所有 `farmstorm:` 物品 ⇒ `plain_chives` **自动**被建模，落进 `basicItem(item)`（`:52`） | **代码不用改**，但 `basicItem` 指向 `farmstorm:item/plain_chives` ⇒ **必须补一张 16×16 贴图** `assets/farmstorm/textures/item/plain_chives.png`（`textures/item/` 现有 **29** 张，无此文件） |
| 2 | **语言键** | `ModLangProvider#buildEntries`（`:70` 起，物品段 `:72-88`）逐条手写 | **+1 条** `item(entries, "plain_chives", "平凡韭菜", "Plain Chives")`（§5） |
| 3 | **创造栏** | `creativeTabItems()`（`FarmstormItems:127-136`）是**手写清单**，不遍历注册表 | **+1 行** `items.add(PLAIN_CHIVES);` |

**贴图怎么出**：`tools/gen_textures.py` 是现有的贴图生成器（`ICONS` 表 + drawer，输出 `assets/farmstorm/textures/item/<id>.png`），本版加一条 `plain_chives` 的 16×16 条目即可。**不要**指望 datagen 生成 PNG——`verify_project.py` 不校验贴图存在性（§8 #30 是唯一的人工闸）。

#### 2.5.4 不许做的事（逐条给后果）

| 不许 | 后果 |
|---|---|
| 加进 `CropProfile` / `CROP_ITEMS` | 22 个作物变 23，`Farmstorm.logSelfCheck` 的 `taxonomyOk`（`Farmstorm.java:124-125`）当场 FAIL |
| 加进 `MODIFIED_CROPS` / `SEEDS` 标签 | 它会变成「改良作物 / 播种物」，`converter_fuel` 的 37 项基数与 `FarmstormTags` 的注释全错 |
| 加合成配方 | 违反裁定 #3，且多出第四条获取路径（46 号 §6 第 4 条墓碑） |
| 建方块形态 / 投掷物 | `blocks` 27 → 28、`entity_types` 13 → 14，§1.2 整表崩 |

### 2.6 方块标签与名额

#### 2.6.1 标签本体

| 项 | 本文确定 | 依据 |
|---|---|---|
| 标签 | **`farmstorm:processing_block`**（方块标签） | 裁定 #14 |
| Java 常量 | `FarmstormTags.PROCESSING_BLOCK`，`TagKey<Block>` | D-194 |
| 落点 | `FarmstormTags`（46 行）第 **7** 个 key：现 6 个（3 block + 3 item，`:18-36`）⇒ 7 个（4 block + 3 item） | D-194 |
| 生成 | `ModBlockTagsProvider#addTags`（`:23-63`）加一行 `tag(FarmstormTags.PROCESSING_BLOCK).add(FarmstormBlocks.CULTIVATOR.get());` | `BRIEF` §1.4 的既有范式 |
| 产物 | `data/farmstorm/tags/block/processing_block.json` | 同上 |
| 成员 | **只有** `farmstorm:cultivator` | 裁定 #6；`converter` **不进**（裁定 #5） |

**`FarmstormTags` 只 +1，不是 +2**：POI 类型那条标签用的是**原版的** `PoiTypeTags.ACQUIRABLE_JOB_SITE`（§2.3.5），它不在 `FarmstormTags` 里、也不是我们定义的 `TagKey`。写成「`FarmstormTags` 6 → 8」是错的（登记在 §10 第 11 条）。

#### 2.6.2 名额的读取点（一处不改、一处只改默认值）

| 读取点 | 读什么 | 本版动不动 |
|---|---|---|
| `OutpostCoreBlockEntity:350-352` `isCultivatorListFull()` = `cultivators.size() >= cultivatorCapacity.get()` | 容量 | **代码不改**，阈值随默认值 16 → 24 自动变 |
| `OutpostCoreBlockEntity:541` `int cap = cultivatorCapacity.get();`（补绑） | 容量 | **代码不改**，同上 |
| `OutpostCoreBlockEntity:542` / `:553-555` 满即停 / 每绑一个重读 | 补绑纪律 | **一行不动**（D-176 / D-177 保留） |

⇒ 「24 个名额由所有带标签方块共享」在本版**没有可观测差异**（标签只有 1 个成员，46 号 §7 第 8 条）。本版定的是**判据**，不是行为差异。

#### 2.6.3 判据的落点（46 号 D-194 留给 48 号的口子，本文定为）

现状：「这一格还是不是可绑定的方块」由**类型**判定，出现在**两处**（都在 `OutpostCoreBlockEntity`）：

| 行 | 代码 | 语义 |
|---|---|---|
| `:393` | `return chunk != null && !(chunk.getBlockState(pos).getBlock() instanceof CultivatorBlock);` | 惰性清理：该格已不是培养器 ⇒ 从绑定表摘掉 |
| `:783` | `if (!(cultivatorChunk.getBlockState(pos).getBlock() instanceof CultivatorBlock)) { … continue; }` | 迁移失败路径：该格已不是培养器 ⇒ 保留 NBT 原值 |

**本文确定**：

1. 这两处改成**标签判定**：新增私有辅助 `private static boolean isProcessingBlock(BlockState state)`（实现 `state.is(FarmstormTags.PROCESSING_BLOCK)`），两处都调它。这就是 D-194 的「语义判据由『是不是培养器』改为『带不带这个标签』」的**唯一代码落点**。
2. **候选发现保持类型参数不变**：`OutpostCoreBlockEntity:546-547` 的 `AreaBlockEntities.collect(server, pos, RANGE_CHUNKS, CultivatorBlockEntity.class)` **不改**。三条理由：① 标签本版只有 1 个成员，改与不改行为相同；② 绑定成功后要写回培养器侧的 `Core` 缓存（`:564` `candidate.setCore(...)`），非 `CultivatorBlockEntity` 没有这个写入口；③ 把类型面放开属于「为 0.2.3 的转换器预留接口」，被裁定 #5 明令禁止。
3. **`addCultivator(BlockPos)` 的签名不动**（`:358`）：它本来就是坐标面，不接方块实体 ⇒ 「绑定入口还接不接受 `CultivatorBlockEntity` 之外的东西」的答案是**签名不变、本版不接受、也不需要改签名**。
4. **`convert` 相关一行不动**：`converter` 不进标签（§2.6.1），它的方块 / 配方 / 燃料标签 / 菜单全部照旧。

### 2.7 两个默认值（以及七处必须跟着改的注释）

#### 2.7.1 两个键

| 键 | 定义处 | 现值 | 改后 | 值域 |
|---|---|---|---|---|
| `outpost_core.maxPerPlayer` | `FarmstormConfig:941-943`（`:941` comment、`:942` translation、`:943` `defineInRange`） | **2** | **4** | 1 ~ 64 不动 |
| `outpost_core.cultivatorCapacity` | `FarmstormConfig:954-956`（`:954` comment、`:955` translation、`:956` `defineInRange`） | **16** | **24** | 1 ~ 64 不动 |

两条纪律：

1. **只改 `defineInRange` 的第二个实参**，不碰 comment、不碰 translation、不碰范围。
2. **语言键不用改**：两个键的描述不含默认值（`en_us.json:196` = `"Cultivators bound per core"`、`:197` = `"Outpost cores each player may place"`；`zh_cn.json` 同键同义）⇒ D-202 的「改默认值不需要改语言键」成立。

#### 2.7.2 七处必须跟着改的注释（**`BRIEF` 只列了 1 处**）

| # | 位置 | 现文 | 改成 | 强制? |
|---|---|---|---|---|
| 1 | `config/FarmstormConfig.java:236` | 「**与培养器名额 16 不是一个数**」 | 24 | **必须**（直接写死旧默认值） |
| 2 | `config/FarmstormConfig.java:238` | 「一个核心最多绑定的培养器数量（默认 **16**）」 | 24 | **必须** |
| 3 | `config/FarmstormConfig.java:952-953` | 「绑定名额默认 **16**、值域 1 ~ 64」「**16** 是『一个核能绑几个培养器』」 | 24 | **必须** |
| 4 | `blockentity/OutpostCoreBlockEntity.java:116` | 「`cultivatorCapacity`（默认 **16**）」 | 24 | **必须** |
| 5 | `blockentity/OutpostCoreBlockEntity.java:349` | 「容量读 `cultivatorCapacity`（默认 **16**）」 | 24 | **必须** |
| 6 | `menu/OutpostCoreMenu.java:30` | 「**与培养器名额 16 不是一个数**」 | 24 | **必须** |
| 7 | `network/CultivatorLinkPayload.java:32-33` | 「`maxPerPlayer` 上界 64 × `cultivatorCapacity` 上界 **16** = 1024」「现实值是 `2 × 16 = 32`」 | 见 §2.7.4 | **必须**（`BRIEF` §3.4 第 13 行已列） |

**不动**的四处（数字 16 与本键无关，误改会引入新错误）：`FarmstormConfig:841`（桑吉尔蔗抬升 16 格）、`behavior/ZangiefPiledriverController.java:52`（16 格 / 15 刻）、`client/WildHunterClientState.java:15`（`loopSoundRange` 默认 16.0）、`FarmstormConfig:236` 之外的 `6 × 9 = 54` 那一串。

**建议同步（非阻塞）**：`OutpostCoreBlockEntity:333` 的成本估算写「每刻 × **16** 条 × 核数」，条数随容量走，改成 24 更准确。

#### 2.7.3 为什么改默认值**不会**更新老存档的 toml

盘上的事实（本文实测）：`run/config/farmstorm-server.toml:753` = `maxPerPlayer = 2`、`:765` = `cultivatorCapacity = 16`。NeoForge 的 config 加载只**补键 / 删键**，**不改已存在键的值** ⇒ 老存档升级后仍是 2 / 16（`BRIEF` §1.8、D-202、RT-63）。

⇒ **验收必须用新生成的 config**：先删 `run/config/farmstorm-server.toml`（或改名备份）让游戏重生一份，再数 §4 的两个值。**读老存档的 toml 判定这两条，必然假红**（§8 #9 就是这条）。

#### 2.7.4 `MAX_LINKS` 的注释：算术随容量重写，常量不动

现注释（`CultivatorLinkPayload.java:31-34`，四行 javadoc）：

```
单包条目的解码上限（36 号 §2.8.1 的算术）：maxPerPlayer 上界 64 ×
cultivatorCapacity 上界 16 = 1024。现实值是 2 × 16 = 32（默认每玩家 2 核）。
```

两处错：① `cultivatorCapacity` 的范围上界是 **64**（`FarmstormConfig:956`），不是 16 ⇒ 按「上界 × 上界」应是 `64 × 64 = 4096`，这段算术**在 0.2.1 就已不严格**；② 默认值改成 24 后「× 16」更不知所指。

**本文确定**的改法（只改注释，`MAX_LINKS = 1024` 常量在 `:35` **一个字符都不动**）：

```java
/**
 * 单包条目的**解码上限**（36 号 §2.8.1 的算术）：{@code maxPerPlayer} 上界 64 ×
 * {@code cultivatorCapacity} 上界 64 = 4096 是理论最坏值；本常量取 1024 是**解码保护**，
 * 不按默认值推导。新装默认下每玩家 4 核 × 24 = 96，远小于本上限。
 */
```

**为什么常量不动**（D-201 已否决抬到 4096）：实际值 96 远小于 1024，没有任何人会撞上解码上限；把一个只影响解码上限的常量放大 4 倍不解决任何需求，还会让「协议串仍 `"2"`」的兼容承诺多一个不必要的变更点。

---

## 3. 动作面总表（对 0.2.1 的增 / 改 / 不改，逐行 33 条）

列说明：**类别** = 发布 / 类 / 注释 / 数据 / 配置 / 语言 / 工具 / 不改。**现值** = 0.2.1 已发布态的实际内容；**改后** = v0.2.2 的目标态。决策号一律引 46 号，风险号一律引 `BRIEF` §4。

| # | 类别 | 路径或 类#方法 | 现值 | 改后 | 决策号 | 风险号 |
|---|---|---|---|---|---|---|
| 1 | 发布 | `gradle.properties:11` | `mod_version=0.2.1` | **`0.2.2`** | D-187 | — |
| 2 | 发布 | `一键构建.cmd:5` | 抬头 `Farmstorm v0.2.1` | **`v0.2.2`** | D-187 | — |
| 3 | 发布 | `一键构建.cmd:11,12` | jar / sources-jar 名各含 `0.2.1` | **`0.2.2`**（两处） | D-187 | — |
| 4 | 发布 | `检查环境.cmd:5` | 抬头 `Farmstorm v0.2.1` | **`v0.2.2`** | D-187 | — |
| 5 | 发布 | `检查环境.cmd:14` | `farmstorm-0.2.1+mc1.21.1.jar` | **`0.2.2`** | D-187 | — |
| 6 | 类 | `client/OutpostCoreScreen` | 42 行，只覆写 `renderBg`（`:37-41`），**无** `render` 覆写 | **+`render` 覆写**：`super.render(...)` 后 `this.renderTooltip(...)` | D-190 | RT-62 |
| 7 | 类 | `client/OutpostCoreScreen#renderBg`（`:37-41`） | **一次 blit** 整张 176 × 222（`v=0 → y=0`） | **两次 blit**：上段 `v=0` 高 125、下段 `v=126` 高 96 画到 `y=125` | D-191 | RT-62 |
| 8 | 类 | `registry/FarmstormPoiTypes` | 不存在 | **新增**（`DeferredRegister<PoiType>`，`farmstorm:botanist` 绑 `outpost_core`，`maxTickets=1` / `validRange=1`） | D-196 | RT-64 |
| 9 | 类 | `registry/FarmstormVillagerProfessions` | 不存在 | **新增**（`DeferredRegister<VillagerProfession>`，六字段见 §2.3.4） | D-196 | RT-64 |
| 10 | 类 | `Farmstorm.java` 构造器（`:47-63`） | **12** 行 `.register(modBus)`（11 类 + 1 个口径外附件；`damage_type` 数据驱动、无 register） | **+2 行**：POI 在前、职业在后，都排在 `FarmstormAttachments`（`:63`）之后 | D-196 | RT-64 |
| 11 | 类 | `Farmstorm.java:176-179` | `total == 97` / `items == 33` | **`98` / `34`**，其余 10 个 `==` 不动 | D-189 | RT-70 |
| 12 | 类 | **新增** `event/BotanistTrade` | 不存在 | 22 条 listing 全进 1 级（3-6 / `maxUses 8` / 每次 1 个） | D-197 | RT-66 |
| 13 | 类 | **新增** `event/BotanistEpicTrade` | 不存在 | 1 条 listing 进 5 级（4 个史诗作物抽 1、40-64、`maxUses 1`） | D-199 | RT-67 |
| 14 | 类 | `event/VillagerTradeEvents#onVillagerTrades`（`:29-36`） | 只处理 `FARMER`，1 / 3 / 5 各 `add` 一项 | **+植物学家分支**（写在前、带 `return`）；**农民 3 行原样保留** | D-197、D-200 | RT-66 |
| 15 | 类 | `registry/FarmstormItems`（`:124` 之前） | 33 个物品 | **+1**：`registerItem("plain_chives", …)` + 固定 `FoodProperties`（`nutrition 2` / `saturationModifier 0.1` / **不** `alwaysEdible`） | D-193 | RT-69 |
| 16 | 类 | `FarmstormItems#creativeTabItems`（`:127-136`） | 手写清单，无平凡韭菜 | **+1 行** `items.add(PLAIN_CHIVES);` | D-193 | RT-69 |
| 17 | 注释 | `FarmstormItems:26` | 「**33 个物品**…… 22 作物 + 7 播种物 + 4 方块物品」 | 「**34 个物品**…… + 1 个平凡农作物」 | D-193 | RT-69 |
| 18 | 类 | `util/FarmstormTags`（`:18-36`） | 6 个 `TagKey`（3 block + 3 item） | **7 个**（+`PROCESSING_BLOCK`） | D-194 | — |
| 19 | 数据 | `datagen/ModBlockTagsProvider#addTags`（`:23-63`） | 5 条 `tag(...)` | **+1 条** `tag(PROCESSING_BLOCK).add(CULTIVATOR.get())` ⇒ 产物 `tags/block/processing_block.json` | D-194 | — |
| 20 | 数据 | **新增** `datagen/ModPoiTypeTagsProvider` | 不存在 | **新增**（把 `farmstorm:botanist` 加进 `minecraft:acquirable_job_site`） | D-196 | RT-64 |
| 21 | 数据 | `datagen/FarmstormDataGen#gatherData`（`:22-52`） | 11 个 `addProvider` | **+1**：把新 provider 挂到 `event.includeServer()`（与两个 tags provider 同组） | D-196 | RT-64 |
| 22 | 类 | `blockentity/OutpostCoreBlockEntity:393`（`pruneCultivators`） | `instanceof CultivatorBlock`（类型判据） | **标签判据**：`!isProcessingBlock(state)`（新增私有辅助） | D-194 | — |
| 23 | 类 | `blockentity/OutpostCoreBlockEntity:783`（迁移失败路径） | `instanceof CultivatorBlock` | **标签判据**（同一个辅助方法） | D-194 | — |
| 24 | 配置 | `config/FarmstormConfig:943` | `defineInRange("maxPerPlayer", 2, 1, 64)` | **`4`** | D-195 | RT-63 |
| 25 | 配置 | `config/FarmstormConfig:956` | `defineInRange("cultivatorCapacity", 16, 1, 64)` | **`24`** | D-195 | RT-63 |
| 26 | 注释 | `FarmstormConfig:236` / `:238` / `:952-953` | 三处写死「16」 | **24**（逐条见 §2.7.2） | D-195 | RT-68 |
| 27 | 注释 | `OutpostCoreBlockEntity:116` / `:349`、`OutpostCoreMenu:30` | 三处写死「16」 | **24** | D-195 | RT-68 |
| 28 | 注释 | `network/CultivatorLinkPayload:32-33` | 「上界 64 × 上界 16 = 1024」「`2 × 16 = 32`」 | 按 §2.7.4 重写；**`MAX_LINKS = 1024`（`:35`）不动** | D-201 | RT-68 |
| 29 | 注释 | `event/VillagerTradeEvents:10-11` | 「每次 `TagsUpdatedEvent`（数据包重载，含起服）」 | 改成「`SERVER_DATA_LOAD` 时；`/reload` 不重发」（§2.4.5） | D-197 | — |
| 30 | 数据 | `assets/farmstorm/textures/item/plain_chives.png` | **不存在**（现有 29 张 item 贴图） | **新增** 16×16（由 `tools/gen_textures.py` 出图） | D-193 | RT-69 |
| 31 | 语言 | `datagen/ModLangProvider#buildEntries` | 338 条 | **+2 条**（`item.farmstorm.plain_chives`、`entity.minecraft.villager.farmstorm.botanist`） | D-189、D-193 | RT-69 |
| 32 | 语言 | `ModLangProvider:14-19` 的类注释 | 「33 item …… = **338**」 | 「34 item + 1 villager …… = **340**」 | D-201 | — |
| 33 | 工具 | `tools/verify_project.py:334` / `:378` | `EXPECTED["items"]` 含 33 项、`EXPECTED_COUNTS["items"] = 33` | **34**（两处同改） | D-201 | RT-70 |
| — | 不改 | `TierTrade`（81 行）/ 农民 3 行（`VillagerTradeEvents:33-35`） | 3 项、每项 2 个、`maxUses 1` | **原样** | D-200 | — |
| — | 不改 | `converter` 的方块 / 配方 / `CONVERTER_FUEL` / 菜单 | 全部照旧工作 | **原样**（不在标签里） | D-188 | — |
| — | 不改 | `OutpostCoreBlockEntity` 的补绑（`:537-570`）与满即停 | 周期 100 刻、错峰、只补未绑、满则停 | **原样**（只改两处判据与注释） | D-176、D-177 | — |
| — | 不改 | `verify_project.py` 的 **config 期望面** | 176 键由 6 份旧文档现算 | **原样**（本版不加键、不加组，见 §4.4） | D-189 | — |
| — | 不改 | `Farmstorm#logSelfCheck`（`:95-153`）的作物 / 品质 / 池断言 | 22 作物、11/7/4、池 11 | **原样**（平凡韭菜不是作物） | D-193 | — |

**逐行分类计数**（带编号的 **33** 行，逐行相加必须等于 33）：发布 **5**（#1~5）+ 类 **15**（#6~18, #22, #23）+ 配置 **2**（#24, #25）+ 注释 **4**（#26~#29）+ 数据 **4**（#19~#21, #30）+ 语言 **2**（#31, #32）+ 工具 **1**（#33）= `5 + 15 + 2 + 4 + 4 + 2 + 1 = 33`。另有 **5** 行「不改」（`| — |` 开头、不占正式编号），表内共 **38** 行。

**`BRIEF` §3.4 的 16 项必改逐项落点**：① `gradle.properties` → #1；② 两支 `.cmd` 的 6 处版本串 → #2~#5（抬头 2 + jar 名 3 已拆成 4 行，jar 名两处在同一行）；③ `OutpostCoreScreen` 加 `render` → #6；④ `renderBg` 两次 blit → #7；⑤ `FarmstormTags` → #18；⑥ `ModBlockTagsProvider` → #19；⑦ `FarmstormItems` → #15、#16；⑧ `FarmstormPoiTypes` → #8；⑨ `FarmstormVillagerProfessions` → #9；⑩ `BotanistTrade` → #12、#13；⑪ `VillagerTradeEvents` → #14；⑫ `FarmstormConfig` 两处默认值 → #24、#25；⑬ `CultivatorLinkPayload` 注释 → #28；⑭ `verify_project.py` → #33；⑮ `ModLangProvider` → #31；⑯ 创造栏 / 模型 / 战利品 → #16、#30（模型自动出，贴图必须补；平凡韭菜没有战利品表，它不是方块）。**16 项全部有着落。**

**`BRIEF` §3.4 未列、但本版必须做的 5 项**：#11（`Farmstorm.java` 的 12 类断言，不改就 FAIL）、#10 / #20 / #21（两张新表的接线与 POI 标签 provider，缺一就「村民不认领」）、#22 / #23（判据落点，D-194 的语义变更唯一代码位置）。这 5 项不落在 `BRIEF` 的 16 行里，因为 `BRIEF` 把它们留在「留给文档作者推导」（§7）或根本没提（`Farmstorm.java` 的断言）。

---

## 4. config 全表（只列变化的两个键 + 总数核对）

### 4.1 变化逐键（**2 行**：改 2 / 增 0 / 删 0）

| # | 完整键路径 | 类型 | 现值 | 改后 | 值域 | 动作 | 决策号 |
|---|---|---|---|---|---|---|---|
| 1 | `outpost_core.maxPerPlayer` | int | **2** | **4** | 1 ~ 64（不变） | **改默认值** | D-195 |
| 2 | `outpost_core.cultivatorCapacity` | int | **16** | **24** | 1 ~ 64（不变） | **改默认值** | D-195 |

（`outpost_core` 组 **5 → 5** 键；组数不变、键数不变 ⇒ **176 键 / 60 组**。本版**没有**增键、**没有**删键、**没有**改值域，只改两个默认值。）

### 4.2 总数核对（176 / 60）

| 项 | 0.2.1 | 本版 | v0.2.2 | 算法 |
|---|---|---|---|---|
| server config 键 | **176** | **+0** | **176** | 行首 `.define*` 的匹配行 177 − 注释 1（`:940`）= 176（§1.4 的方法，逐键复核） |
| server config 组 | **60** | **+0** | **60** | `b.push` 60 次 / `b.pop` 60 次 |
| client config 键 | 3 | +0 | **3** | `hud.enabled/x/y`，不计入 176 |
| `.define` 行总数（含注释） | 177 | +0 | **177** | 176 live + 1 注释 |

**三条独立校验**：① 源码行首 `.define*` = 176；② `run/config/farmstorm-server.toml` **重生成后**取键 = 176；③ `verify_project.py` 的并集 = 176（现算自 6 份旧文档，本版不加键 ⇒ 不变）。

### 4.3 两个键为什么不是「增键」

46 号 D-195 定的是「改**默认值**」：两个键在 0.2.0 就存在（`FarmstormConfig:943` / `:956`），字段声明 `:234` / `:239` 也在。加到表里的只有**默认值的两个数字**，`defineInRange` 的键名字符串、comment、translation、值域四个参数都不动 ⇒ 键集不变、组集不变。

### 4.4 本文 §4 与 `verify_project.py` 的关系（**重要**）

`tools/verify_project.py` 的期望键集**只从 7 份固定文档的固定小节现算**（`main()` 读 `docs/zh/01-DESIGN.md` / `12-TECH-0.0.2.md` / `18-TECH-0.0.3.md` / `24-TECH-0.1.0.md` / `30-TECH-0.1.1.md` / `36-TECH-0.2.0.md` / `42-TECH-0.2.1.md`，见 `:402-408`），**它今天不读 `docs/zh/48-*`**。

⇒ 两条结论，写错任一条 §8 #4 会红：

1. **本文 §4 不进任何解析器**，不需要为了工具改表格格式（本文仍按 7 列写，是为了将来若扩解析器时能直接复用 42 号 §4.1.1 的契约）。
2. **本版要改的是 `EXPECTED["items"]` 与 `EXPECTED_COUNTS["items"]`（33 → 34），不是 config 期望面**。config 期望面**一个字节都不用改**——本版不加键。把「改默认值」误当成「改期望面」会去动 176 这个数，那是错的方向（§8 #4 的正反两问就是这条）。

---

## 5. 语言键增删

**现状 338 × 2**（方法见 §1.4）。本版 **+2** ⇒ **340 × 2**。

| # | 键 | 语言 | 落点 | 文案（本文确定） |
|---|---|---|---|---|
| 1 | `item.farmstorm.plain_chives` | zh_cn / en_us | `ModLangProvider#buildEntries` 的物品段（`:72-88` 之后） | 用 `item(entries, "plain_chives", "平凡韭菜", "Plain Chives")`（`item(...)` 助手的键前缀是 `item.farmstorm.`，`:453-456`） |
| 2 | `entity.minecraft.villager.farmstorm.botanist` | zh_cn / en_us | 同一张条目表，**新起一个「村民职业」段** | 用 `plain(entries, "entity.minecraft.villager.farmstorm.botanist", "植物学家", "Botanist")`（`plain(...)` 写全路径键，`:466-468`）——**不能用 `entity(...)` 助手**，它的前缀是 `entity.farmstorm.`，拼不出这个键 |

**改动前后逐组对照**（本文复核的现状值 + 本版增量）：

| 前缀组 | 0.2.1 | 本版 | 说明 |
|---|---|---|---|
| `item.` | 33 | **34** | +`plain_chives` |
| `block.` | 27 | 27 | 方块数不变 |
| `effect.` | 4 | 4 | — |
| `subtitles.` | 3 | 3 | 本版不加音效 |
| `farmstorm.configuration.` | 179 | 179 | 176 server + 3 client；改默认值不加键 |
| `advancements.` | 36 | 36 | — |
| `death.attack.` | 6 | 6 | — |
| `tooltip.` | 20 | 20 | — |
| `entity.` | 13 | 13 | 平凡韭菜不是实体 |
| `gui.` | 9 | 9 | — |
| `container.` | 2 | 2 | — |
| `itemGroup.` | 1 | 1 | no new tab |
| `message.` | 0 | 0 | — |
| `key.` | 2 | 2 | — |
| `command.` | 3 | 3 | — |
| **`entity.minecraft.villager.`（新组）** | 0 | **1** | 职业名（与 `entity.` 组不同前缀，单列） |
| **合计** | **338** | **340** | `338 + 2 = 340` |

**三条纪律**：

1. **中文与英文同键同数**：两份语言文件由同一张 `ENTRIES` 表按列写出（`ModLangProvider:63-68`），加一条就两边各一条，`zh-only = 0` / `en-only = 0` 天然成立。
2. **语言键不会自动生成**：`config(...)` / `item(...)` 都是逐条手写（现 338 条），所以「加一个 config 键」与「加一句翻译」是两件事——本版恰好**只加物品与职业**，不加 config 键。
3. **`340` 是最终值不是下限**：本版能确定的新增语言键只有这两条（一个物品名 + 一个职业名）；POI 类型、标签、交易项都没有语言键（交易项复用物品名）。若实现期发现还需要第三条第（例如某个界面标题），**必须回头改本文与 47 号 §5.6**，不许在代码里偷偷加。

**怎么数（可复现）**：

```
两列行匹配  ^\s*"[^"]+"\s*:   ⇒ zh = 340、en = 340
ConvertFrom-Json 取键集        ⇒ zh-only = 0、en-only = 0
ModLangProvider 的条目数        ⇒ 338 → 340
```

⚠️ 中文那份**不要**用默认 `Get-Content | ConvertFrom-Json` 直读：本机默认编码会把无 BOM 的 UTF-8 读成乱码并让解析器报 `ArgumentException`。用 `-Encoding UTF8`，或用行匹配计数（§1.4）。

---

## 6. 存档与兼容

六问逐条回答（每条给「落不落盘」与「新进程 / 老存档看到什么」）。

### 6.1 老存档里**已经存在**的据点核心，会不会自动变成工作站点？

**会，不需要拆了重放。** 这是本版最容易被误判成「需要迁移脚本」的一处，机制已逐行核过：

| 步 | 发生了什么 | 出处 |
|---|---|---|
| 1 | 每次区块**反序列化**（含老存档首次加载）都会调 `PoiManager#checkConsistencyWithBlocks` | `.scratch/.../ChunkSerializer.java:134` |
| 2 | 该方法对每个 section 先 `refresh(...)`，再在 `mayHavePoi(section)` 为真时 `updateFromSection(...)` 全量扫描 | `.scratch/.../poi/PoiManager.java:229-240` |
| 3 | `mayHavePoi(section)` = `section.maybeHas(PoiTypes::hasPoi)`，判据是**当前**的 `TYPE_BY_STATE` 映射 | 同上 `:242-244` |
| 4 | 扫描时 `PoiTypes.forState(blockstate)` 命中就 `accept`（登记） | 同上 `:246-257`（`:255` 的 `forState`） |

⇒ 老存档里的 `outpost_core` 在**区块下次加载时**被自动登记成 `farmstorm:botanist` POI。玩家**不需要**破坏再放回。前提只有一个：POI 类型已经注册（§2.3.2），因为第 3 步读的是当前映射。

**副作用**：老存档的 `world/poi/` 会在下次保存时多出 `farmstorm:botanist` 的记录（`PoiRecord.codec` 用注册表 id 存 type，`PoiRecord.java:19-29`）⇒ **格式不变、不新增文件类型、不动 `DataVersion`**。

### 6.2 村民报价（`Offers`）的落地与补货

| 项 | 结论 | 依据 |
|---|---|---|
| 报价写在哪 | 村民实体的 `Offers` NBT（`AbstractVillager` 的 `offers` 字段） | `TierTrade:22-25` 的类注释已记；`Villager.java:398-408` 的 `restock` 只对已存在的 offers 调 `resetUses()` |
| 补货会不会换内容 | **不会** | 同上 `:401-403`；这是裁定 #18 的源码依据 |
| 老存档里的无业村民会不会变成植物学家 | **会**（核被加载成 POI 后，村民的 `POTENTIAL_JOB_SITE` 能找到它，走到 §2.3.5 第 4 步） | `Villager.java:176-185`；`AssignProfessionFromJobSite.java:37-43` |
| 老存档里已满级的普通村民 | 不变（它已有职业，`AssignProfessionFromJobSite:30-31` 在「已有职业」时直接 `return true`，不改职业） | 同上 `:30-32` |

### 6.3 老存档的 config：仍是 2 / 16

| 项 | 结论 | 依据 |
|---|---|---|
| 盘上现状 | `run/config/farmstorm-server.toml:753` = `maxPerPlayer = 2`、`:765` = `cultivatorCapacity = 16` | 本文实测（UTF-8 读） |
| 升级后会不会变 | **不会**：NeoForge 只补键 / 删键，不改已存在键的值 | `BRIEF` §1.8；D-202 |
| 解法 ①（推荐） | 删 `run/config/farmstorm-server.toml` 让游戏重生成（玩家自己的改动会一起丢，先备份） | D-202 |
| 解法 ② | 手改那两行 | D-202 |
| **不写迁移代码** | 自动改写玩家配置 = 覆盖玩家自己的设置，比「不生效」更糟（已否决） | 46 号 §6 |
| 上限不追溯 | 上限只在**放置**时读一次（`OutpostCoreBlock:89` 的 `countOwned(player) >= maxPerPlayer`）⇒ 老存档里已有 5 个核的玩家**不删核**，只是放不下第 6 个 | 裁定 #16；D-202 |

### 6.4 新增的两张注册表会不会让老存档读不出来

**不会**，三条：

1. `PoiType` 与 `VillagerProfession` 都是**内建注册表**（`BuiltInRegistries`，不是数据包注册表），模组加载即注册，与存档无关。
2. 村民的职业存的是**注册名**（`VillagerData.CODEC:19-23` 用 `BuiltInRegistries.VILLAGER_PROFESSION.byNameCodec().fieldOf("profession").orElseGet(() -> NONE)`）⇒ 未知 / 缺失的职业会按 `orElseGet` 回落成 `none`，**不是硬错误**（严格行为见 §9 第 8 条）。
3. POI 记录存的是 `RegistryFixedCodec.create(Registries.POINT_OF_INTEREST_TYPE)`（`PoiRecord.java:23`），同样是注册名。

### 6.5 网络面：协议串不变

| 项 | 结论 |
|---|---|
| 包数 | **2**，本版不新增（§1.5） |
| 协议串 | 仍 **`"2"`** ⇒ 客户端与服务端的版本兼容面**没有变化** |
| 本版对两个包的改动 | 只有 `CultivatorLinkPayload:32-33` 的**注释**（§2.7.4）；字节流、字段、`MAX_LINKS` 全不动 |

### 6.6 卸载模组时会发生什么（登记，不改）

带 `farmstorm:botanist` 职业的村民在无模组环境下回落成 `none`（§6.4 第 2 条）；`world/poi/` 里的 `farmstorm:botanist` 记录读不出来（该注册表项缺失），POI 记录会被弃置。**两者都不是本版的兼容承诺范围**——本模组的既有口径是「不保证存档可在卸载后继续使用」，本节只把形状写清，免得被当成缺陷。

---

## 7. 风险册 RT-62 ~ RT-70

逐条：风险 / 触发条件 / 影响 / 缓释 / 由哪个验收覆盖。

| 编号 | 风险 | 触发条件 | 影响 | 缓释 | 由哪个验收覆盖 |
|---|---|---|---|---|---|
| **RT-62** | **两个 bug 都是「原版默认行为」类**：`renderTooltip` 与两次 blit 都不是可选的优化，缺一行就复现 | ① 只读代码判断「链路应该会调」；② 把修复写成「优化渲染 / 调整位置」；③ 只加验收不动代码（0.2.1 的 F-251 形态） | 交付一个「验收通过而 bug 还在」的版本——这是 0.2.1 已经发生过一次的事故 | (a) §2.1 / §2.2 各给逐行源码证据（`AbstractContainerScreen:91` 无调用、`ContainerScreen:35-36` 两次 blit）；(b) §3 #6 / #7 列成必改的两行；(c) §8 #20 ~ #23 **必须开界面看**，且给出「看什么」（tooltip 第一行是物品名、格子与物品像素级重合） | §8 #20 ~ #23 |
| **RT-63** | **改默认值对老存档无效** | ① 用老存档的 `run/config` 读 2 / 16 就判「没改成」；② 以为「改了 `defineInRange` 就等于玩家看到 4 / 24」 | 最可能收到的误报：「我升级了，怎么还是 2？」；反过来，开发者若拿老 toml 当验收基线，会得到一个**永远红**的验收 | (a) §2.7.3 写死「验收必须用新生成的 config」；(b) §8 #9 **专门有一条**用老 toml 验证「仍是 2 / 16」（这条的「红」是**期望中的红**）；(c) 不写迁移代码（46 号 §6） | §8 #8、#9 |
| **RT-64** | **POI 与职业是两张新注册表**：注册顺序、`PoiType` 的 blockstate 集合、`VillagerProfession` 的谓词、**以及 `minecraft:acquirable_job_site` 标签**都要对；写错的表现是「村民不认领」而非报错 | ① 只注册职业不注册 POI；② 注册了 POI 但**没加** `acquirable_job_site` 标签（§2.3.5 的主形态）；③ 匹配集写成空集 / 写成别的方块；④ 谓词用 `ALL_ACQUIRABLE_JOBS`；⑤ 把 `outpost_core` 塞进原版某个 POI 类型 | 村民永不认领，界面上无任何报错，只有「村民四处走」——最难定位的一类失败 | (a) §2.3.2 ~ §2.3.5 逐条给代码路径（`Villager.java:181-182` → `ALL_ACQUIRABLE_JOBS` → `PoiTypeTags`）；(b) §3 #20 / #21 把标签 provider 列成必改；(c) §8 #13 ~ #16 用**行为**验（放核 + 无业村民 ⇒ 变植物学家），并**专设一条反向验收**（§8 #16：临时去掉标签，确认**不认领**） | §8 #13 ~ #16 |
| **RT-65** | **有主核也能当工作台** ⇒ 村民会跑到玩家基地「上班」 | 有人把 `ownerUuid` 接进职业判据，或加「必须无主 / 必须己方」 | 加判据会让**把核放在基地的玩家永远招不到植物学家**（46 号 D-196 补充已否决） | (a) §2.3.4 的谓词只认 POI key，明写「不按方块、不按 owner、不按距离」；(b) §8 #15 用**有主核**验证认领成功（这条是防「好心加判据」的判决性验收） | §8 #15 |
| **RT-66** | ~~22 项只显示 2 项~~ → **已由裁定 #17 接受**。剩下的是**表述风险** | 文档写「植物学家卖 22 项」而不写「每个只显示 2 项」，或反过来写「每个卖 22 项」 | 玩家把「我这儿只有 2 项」读成 bug；或实现者为了「让 22 项都能看到」去拆级 / 自建池 | (a) §2.4.1 用源码把「22 个 listing ≠ 22 项可见」钉死（`Villager.java:875` 的 `2`、`AbstractVillager:237-238` 的抽样语义）；(b) §8 #17 / #18 成对验收（列表 22 条 / 界面 2 项）；(c) 表述纪律进 47 号与 README（46 号 §7 第 1 条） | §8 #17、#18 |
| **RT-67** | ~~史诗项补货重抽~~ → **已由裁定 #18 改为固定 1 项**。剩下的是**文档风险** | 照抄 plan `:11` 的「额外刷新出」 | 被读成「每次补货换一个史诗项」，玩家会去刷补货然后报 bug | (a) §2.4.4 用 `Villager.java:398-408` 证明补货不重调 `getOffer`；(b) §8 #19 用「补货两次后仍是同一项」验收；(c) 46 号 §7 第 2 条与 47 号附录 A 第 2 行已登记为口径差异，本文 §10 第 5 条再点一次 | §8 #19 |
| **RT-68** | `MAX_LINKS` 注释与 24 不符，且**同类注释共七处**（`BRIEF` 只列了一处） | 只改 `CultivatorLinkPayload` 那一段，其余六处仍写 16 | 注释与默认值不一致 ⇒ 下一位读者按注释推算，得出 32 / 1024 之类的错数 | (a) §2.7.2 的**七行表**（位置 + 现文 + 改成）逐条点名；(b) §8 #25 用 grep 验「`默认 16` / `名额 16` 在 `src/` 下命中 0」；(c) 常量 `MAX_LINKS = 1024` 明写不动 | §8 #24、#25 |
| **RT-69** | `plain_chives` 不走 `cropItem` ⇒ 容易漏掉**创造栏、模型、贴图、语言键**（四个「加了但拿不到」） | ① 只加注册不加 `creativeTabItems()`；② 忘了贴图（`basicItem` 指向不存在的 PNG）；③ 忘了语言键（raw key） | 物品存在于注册表但玩家看不到 / 看到紫黑格 / 看到 `item.farmstorm.plain_chives` 字样 | (a) §2.5.3 的四行表（模型 / 语言 / 创造栏 / 贴图）逐个点名；(b) §8 #26 ~ #29 四条独立验收（创造栏可见、紫黑格检查、名称正确、能吃不生病）；(c) 贴图由 `tools/gen_textures.py` 出，不手搓 | §8 #26 ~ #29 |
| **RT-70** | `verify_project.py` 的 `EXPECTED_COUNTS` 写死 33（且 `Farmstorm.java` 的断言写死 97 / 33） | ① 加了物品不改两处期望面；② 改了期望面但漏了 `Farmstorm.java:176-179`；③ 反过来把 config 期望面也动了 | 加了一个物品却全绿（A-66 那类假绿）；或启动日志 FAIL 而脚本通过（两套闸不一致） | (a) §3 #11 与 #33 把**两处**列成必改；(b) §4.4 明写「config 期望面一个字节都不改」；(c) §8 #2 / #3 / #4 三条分别验脚本、验日志、验「没多改」 | §8 #2 ~ #4 |

**九条的共同点**（写给 49 / 50 号）：每条的缓释都是「**一处定义 + 一行注释 + 一条独立验收**」，没有一条靠「记得小心」。RT-62 / RT-63 的落点是**两条必须真的看界面 / 真的重生成 config 的验收**；RT-64 的落点是**那条 `acquirable_job_site` 标签**；RT-68 / RT-70 的落点是**可以 grep 的字符串**。

---

## 8. 验收自检清单（30 条）

每条给：编号 / 断言原文 / 观察位置 / 依据。**「观察位置」必须可操作**：`V-nn` 指本文自定的观察点，不引入第二个编号系列。

| # | 断言原文 | 观察位置 | 依据 |
|---|---|---|---|
| 1 | 启动日志出现 `自检 注册对象 98 个：items 34 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`；且 `src/main/java` Java 文件数 = **154** | `run/logs/latest.log`（`Farmstorm.java:180-184` 的格式串）；文件计数 | §1.1、§1.2 |
| 2 | `tools/verify_project.py` 打印 `已核 registry 对象: 98 / 98`，且第 1 节打印「文档口径键数 **176**」与「生成 toml 键数 176」 | 跑 `tools/verify_project.py` | §4.2、§4.4 |
| 3 | `verify_project.py:334` 的 `EXPECTED["items"]` 含 `plain_chives`、`:378` 的 `EXPECTED_COUNTS["items"] == 34`；两处**同一个提交**改 | 源码 grep | §3 #33、RT-70 |
| 4 | **反向**：`verify_project.py` 的 **config** 期望面**没有**被改（`expected_config_keys*` 五个函数体一字不动，`added7` 的断言行数仍是 1） | 源码 diff | §4.4、RT-70 |
| 5 | 全仓 `@Mixin` = **0**；`src/` 下 `*.mixins.json` = **0**；网络包注册调用恰 **2** 处，协议串仍 `"2"` | 源码 grep | §1.5 |
| 6 | `FarmstormTags` 恰 **7** 个 `TagKey`（4 block + 3 item）；产物 `data/farmstorm/tags/block/processing_block.json` 存在且**只有** `farmstorm:cultivator` 一个成员 | `src/generated/resources/...`（datagen 产物）+ 源码计数 | §2.6.1 |
| 7 | `converter` **不在** `processing_block` 标签里；`converter` 的方块 / 配方 / `CONVERTER_FUEL` / 菜单全部照旧可用 | 标签 JSON + 进游戏试转换 | §2.6.1、§3「不改」行 |
| 8 | 两张新表都被显式登记且不进 98：`registry/FarmstormPoiTypes` 有 **1** 个 `PoiType` 注册、`registry/FarmstormVillagerProfessions` 有 **1** 个职业注册；`Farmstorm.java` 构造器多 **2** 行 `.register(modBus)`；§1.3 存在且写明「不计入 98 / 工具不核它」 | 源码 + 本文 §1.3 | §1.3 |
| 9 | **老 toml 的期望红**：把升级前的 `run/config/farmstorm-server.toml` 放回去启动 ⇒ 读到的是 `maxPerPlayer = 2`、`cultivatorCapacity = 16`（**这条「红」是正常的**，见下一条） | `run/config/farmstorm-server.toml:753` / `:765` | §2.7.3、§6.3、RT-63 |
| 10 | **新 config 的绿**：删掉该 toml（先备份）后重启 ⇒ 重生成的 toml 里 `maxPerPlayer = 4`、`cultivatorCapacity = 24`，键数 176、段数 60 | 重生成的 toml | §4.2、RT-63 |
| 11 | 语言文件 `zh = 340` / `en = 340` / zh-only **0** / en-only **0**；`item.farmstorm.plain_chives` 与 `entity.minecraft.villager.farmstorm.botanist` 两条都在；`farmstorm.configuration.*` 仍 **179** | 两份 `lang/*.json`（`-Encoding UTF8`） | §5 |
| 12 | POI 类型存在且只绑一个方块：把 `outpost_core` 放下后，`/data get block <core>` 之外无法直接读 POI —— **改用行为判据**（下一条）；反向证据：`PoiTypes.hasPoi(outpost_core.defaultBlockState())` 在调试命令下为 `true`（若不可用则只按 #13 判） | 游戏内（`V-01`） | §2.3.2 |
| 13 | **认领成功**：放 1 个据点核心 + 1 个**无业**村民 ⇒ 村民走到核旁 2 格内后职业变成植物学家（`/data get entity <villager> VillagerData.Profession` = `"farmstorm:botanist"`） | 游戏内（`V-02`） | §2.3.5、RT-64 |
| 14 | **一核一村民**：同一个核旁再拉 1 个无业村民 ⇒ 第二个**不会**变成植物学家（占票失败），且它不会反复原地打转；放第 2 个核后它才认领 | `/data get entity` × 2（`V-03`） | §2.3.3、裁定 #9 |
| 15 | **有主核也能上班**：玩家自己放的核（`ownerUuid` 非空）**照样**被认领（这条防「好心加判据」） | 游戏内（`V-02` 的同一场景） | §2.3.4、RT-65 |
| 16 | **反向（标签缺失必须失败）**：临时把 `acquirable_job_site` 那行注释掉、重新 datagen 并重启 ⇒ 同样场景下村民**永远不认领**（本条证明标签是必需的，不是可选优化） | 游戏内（`V-04`） | §2.3.5、RT-64 |
| 17 | **列表长度 22**：植物学家 1 级列表里 `BotanistTrade.NORMAL.size() == 22`（日志或断点；也可 `grep -c 'new BotanistTrade('` = 22） | 源码 / 日志（`V-05`） | §2.4.2、RT-66 |
| 18 | **可见 2 项**：与植物学家开界面 ⇒ 只显示 **2** 项普通交易（不是 22 项）；换一个**新**植物学家 ⇒ 至少有一项不同（各自抽签） | 游戏内（`V-06`） | §2.4.1、D-198、RT-66 |
| 19 | **史诗固定 1 项**：把植物学家练到 5 级 ⇒ 出现 **1** 项史诗报价；让它补货两次 ⇒ 仍是**同一个**物品、同一个价格区间；价格落在 40 ~ 64 | 游戏内（`V-07`） | §2.4.4、RT-67 |
| 20 | **tooltip 出现**（★必须开界面看）：进 54 格界面，悬停**有物品**的格子（取第 1 / 28 / 54 格做样本）⇒ tooltip 出现且**第一行是物品名**；悬停**空格子** ⇒ **没有** tooltip；悬停**玩家背包格** ⇒ 同样有 tooltip | 游戏内（`V-08`） | §2.1、RT-62 |
| 21 | **tooltip 的详情行**：悬停带附魔 / 带本模组效果组件的物品 ⇒ 多出附魔行 / 效果行，样式与普通箱子一致；悬停**最右侧一列** ⇒ tooltip 仍完整（原版会翻到左侧） | 游戏内（`V-08`） | §2.1.2、D-190 |
| 22 | **背景与格子像素级对齐**（★必须开界面看）：把同一种物品分别放进 54 格区与玩家背包区 ⇒ 两处的**物品与格子边框重合**，没有「物品略微上移」的观感；截图像素比对时，背包 3 行的背景格顶边与物品 16×16 的顶边相差 **0 px**（改前是 1 px） | 截图比对（`V-09`），与 0.2.1 的截图并排 | §2.2、D-191、RT-62 |
| 23 | **两次 blit 的形态**：`OutpostCoreScreen#renderBg` 里 `blit(` 恰 **2** 处，`v` 分别是 `0` 与 `126`，第二段的 y 是 `topPos + 125`、高是 `96`；`render` 覆写恰 **1** 处且第一句是 `super.render(` | 源码 grep | §2.2.2、D-191 |
| 24 | **上限 4**（新 config）：同一玩家放第 **4** 个核成功、第 **5** 个被拒绝；被拒绝时物品**不消耗、不掉落** | 游戏内（`V-10`） | §2.7.1、§6.3 |
| 25 | **绑定 24**（新 config）：一个核周围放 **25** 个培养器 ⇒ 绑定表恰 **24** 条且不再增长；第 25 个绑不上；已绑的 24 条**顺序与坐标一条不变** | `/data get block <core> Cultivators`（`V-11`） | §2.6.2、§2.7.1 |
| 26 | **注释已同步**：`src/` 下 grep `默认 16` / `名额 16` / `上界 16` / `× 16` 命中 **0**（`FarmstormConfig:841` 的桑吉尔蔗 16 与 `WildHunterClientState:15` 的 16.0 不在此列，它们不含上述四个串）；`CultivatorLinkPayload` 的 `MAX_LINKS` 仍 `= 1024` | 源码 grep | §2.7.2、§2.7.4、RT-68 |
| 27 | **平凡韭菜拿得到**：创造栏里能看到「平凡韭菜」（不是紫黑格、不是 raw key）；`/give farmstorm:plain_chives` 可用 | 游戏内（`V-12`） | §2.5.3、RT-69 |
| 28 | **平凡韭菜能吃**：吃一个恢复 **2** 点饥饿值（`nutrition = 2`），饱食度满时**吃不下**（不 `alwaysEdible`）；不能种、不能喂村民、没有配方（配方书里搜不到）、`MODIFIED_CROPS` / `SEEDS` 两个标签里**都没有**它 | 游戏内 + 标签 JSON（`V-13`） | §2.5.2、§2.5.4 |
| 29 | **平凡韭菜能被植物学家卖**：22 项清单里含它（第 20 行），且**它本身不是作物**：`Farmstorm#logSelfCheck` 的「作物 22 种 / 投掷 12 / 部署 7 / 食用 3」四个数**一字不变** | `latest.log` 的自检行（`Farmstorm.java:126-127`） | §2.5.4、D-193 |
| 30 | **贴图真的存在**：`assets/farmstorm/textures/item/plain_chives.png` 是 16×16 PNG；`textures/item/` 下文件数 = **30**（改前 29） | 目录计数 + 打开文件看尺寸 | §2.5.3、RT-69 |

**30 条的分布**（**不重叠、逐段相加 = 30**）：发布与计数 **5**（#1~#5）/ 方块标签 **2**（#6, #7）/ 口径外与 POI **6**（#8, #12, #13, #14, #15, #16）/ 交易 **3**（#17~#19）/ 界面 **4**（#20~#23）/ 两个默认值 **4**（#9, #10, #24, #25）/ 语言 **1**（#11）/ 注释 **1**（#26）/ 平凡韭菜 **4**（#27~#30）= `5 + 2 + 6 + 3 + 4 + 4 + 1 + 1 + 4 = 30`。

**四条必测组合（写给出验收的人）**：

1. **#9 与 #10 必须成对跑**：只跑 #10 会以为「改默认值立刻生效」，只跑 #9 会把它当缺陷。两条一起跑才证明「默认值只对新装生效」这句话。
2. **#13 与 #16 必须成对跑**：#13 证明「能认领」，#16 证明「靠的是那条标签」。只跑 #13 无法区分「标签生效」与「POI 自动生效」——#16 是唯一能把 §2.3.5 那条隐蔽依赖逼出来的验收。
3. **#17 与 #18 必须成对跑**：只跑 #17 会写成「植物学家卖 22 项」（RT-66 的表述风险），只跑 #18 无法确认清单长度没被偷偷削成 2。
4. **#20 与 #22 都必须**真的**开界面看**：这两条的失效形态（tooltip 不出现、1 px 错位）在源码里都「看起来是对的」，只有像素与悬停能证伪（RT-62）。

---

## 9. 待验证 API 清单（10 条）

**判定口径**：一条 API 只有满足「类型 / 方法签名 / 调用点参数个数三者都在本机可读的源码里逐字看到」才算「已核」。本机源码两棵树：原版 + NeoForge 在 `.scratch/p25a/src/`（含 `net/minecraft/**` 与 `net/neoforged/**`），本仓在 `src/main/java/com/dashpig/farmstorm/`。下文的行号**已逐处标注是哪棵树**。

| # | API 或语义 | 已核到什么程度 | 为什么仍待验证 | 由谁确认 | 退路 |
|---|---|---|---|---|---|
| ① | `PoiType` 的构造与「NeoForge 自动挂 blockstate」 | **已核**：`record PoiType(Set<BlockState>, int maxTickets, int validRange)`（`.scratch/.../poi/PoiType.java:8`），NeoForge 把集合换成 `PoiStateSet`（`:11-16`）；自动登记实现在 `.scratch/.../registries/NeoForgeRegistryCallbacks.java:106-126`（`AddCallback<PoiType>` 的 `onAdd` 遍历 `matchingStates()` 写映射，重复即抛）；`PoiTypes.java:121-122` 的注释原文声明「Neo: we do this automatically for modded PoiTypes」 | ① `DeferredRegister` 路径最终也走 `Registry#register` ⇒ 应触发 `onAdd`，但没有运行期证据；② `PoiStateSet` 是 `ReferenceOpenHashSet`（`PoiStateSet.java:19`）⇒ 匹配是**引用**比较，`defaultBlockState()` 返回单例这件事是常识但没有本文的实测记录 | POI 任务（§8 #12 / #13） | 若自动登记没发生 ⇒ 在 `FarmstormPoiTypes` 的 `register` 之后手动调一次原版 `PoiTypes.bootstrap` 之外的替代路径**不存在**（`registerBlockStates` 是 `private`）⇒ 只能退回「用一个已有的原版 POI 类型」这条路，但那样会撞 §2.3.2 的重复 state 异常 ⇒ 因此**这条一旦为假，整个方案要报给作者**（本文把它列第一条就是因为它没有便宜的退路） |
| ② | `maxTickets = 1` 是否确定等于「一核一村民」 | **已核**：`PoiRecord` 的 `freeTickets` 初值 = `maxTickets()`（`.scratch/.../PoiRecord.java:38-40`）、`acquireTicket()` 在 `<= 0` 时返回 false（`:48-50`）、`hasSpace()` = `> 0`（`:68-70`）；原版 13 个工作站全是 `1 / 1`（`PoiTypes.java` 的 `bootstrap`，唯一例外 `MEETING` 是 `32 / 6`） | ① **两个村民同一刻到达**时的胜负没有运行期证据（理论上后者 `acquireTicket` 失败 ⇒ 换目标）；② `validRange` 的确切作用面（`PoiSection`/`PoiManager` 的搜索半径）只读了签名层面的用法 | POI 任务（§8 #14） | 若实测出现「一核两村民」⇒ 说明有别处绕过了 ticket（例如 `PoiManager#reserveTicket` 的重入），按 RT-64 登记并把 §2.3.3 的结论改为「取决于占票时序」 |
| ③ | `minecraft:acquirable_job_site` 标签是**必需**的 | **已核（代码路径）**：`Villager.POI_MEMORIES` 的 `POTENTIAL_JOB_SITE` 谓词 = `VillagerProfession.ALL_ACQUIRABLE_JOBS`（`.scratch/.../npc/Villager.java:181-182`）；`ALL_ACQUIRABLE_JOBS` = `holder.is(PoiTypeTags.ACQUIRABLE_JOB_SITE)`（`VillagerProfession.java:29`、`PoiTypeTags.java:8`）；无业村民的职业是 `NONE`，其 `acquirableJobSite` 也是 `ALL_ACQUIRABLE_JOBS`（`VillagerProfession.java:30`） | 「不加标签 ⇒ 不认领」是**读代码推出来的**，没有实测（§8 #16 就是补这个） | POI 任务（§8 #16） | 若实测「不加标签也能认领」⇒ 说明另有通路（例如 `AssignProfessionFromJobSite` 的 `assignProfessionWhenSpawned` 分支，`AssignProfessionFromJobSite.java:24`），本文 §2.3.5 的结论降级为「强烈建议加」，但**仍然加**（加了无害、不加有风险） |
| ④ | `VillagerProfession` 会不会收到 `VillagerTradesEvent` | **已核**：事件按 `BuiltInRegistries.VILLAGER_PROFESSION` 逐个职业抛（`.scratch/.../common/VillagerTradingManager.java:60-75`，`:61` 的 `for`）；1~5 级全部非 null（`:64-66` + `VillagerTradesEvent.java:26` 的类注释原文）；写回 `VillagerTrades.TRADES`（`:73`） | ① 模组职业是否在 `TagsUpdatedEvent(SERVER_DATA_LOAD)` 之前完成注册（理论上「是」，模组构造期注册早于服务端数据加载）；② 若服主装了别的改动 `VillagerTradingManager` 的模组，顺序可能变 | 交易任务（§8 #17 / #18） | 若事件不触发 ⇒ 退路是 `VillagerTrades.TRADES` 的**静态表**在 `FMLCommonSetupEvent` 里手动放（比事件脏，但同样不碰 Mixin）；**不许**为此加 Mixin |
| ⑤ | `/reload` 不会重发交易事件 | **已核（读代码）**：`loadTrades` 只在 `UpdateCause.SERVER_DATA_LOAD` 时调 `postVillagerEvents`（`VillagerTradingManager.java:37-42`） | `/reload` 的 cause 到底是 `RELOAD` 还是 `SERVER_DATA_LOAD` 没有实测记录（本仓既有注释 `VillagerTradeEvents:10-11` 说「数据包重载，含起服」，与代码不符） | 交易任务（§8 #17） | 验收一律**重启服务端**，不用 `/reload` 判定交易是否生效（这条不依赖 ⑤ 的真值） |
| ⑥ | `AssignProfessionFromJobSite` 的 `findFirst()` 择业顺序 | **已核**：`BuiltInRegistries.VILLAGER_PROFESSION.stream().filter(...heldJobSite().test(poiType)).findFirst()`（`.scratch/.../ai/behavior/AssignProfessionFromJobSite.java:37-41`）、`setVillagerData`（`:43`）、2.0 格判据（`:24`） | ① 注册顺序决定「第一个」，但没有任何保证说我们的职业排在谁前面/后面（本版靠「没有第二个职业认 `outpost_core`」规避）；② 若两个职业都认同一个 POI，谁赢没有实测 | POI 任务（§8 #13） | 若将来出现第二个认 `outpost_core` 的职业 ⇒ 这是**模组冲突**，登记而非修（不许在自己这边加优先级 hack） |
| ⑦ | `getOffer` 返回 `null` 的语义 | **已核**：`addOffersFromItemListings` 里 `if (merchantoffer != null)` 才 `add` 且 `i++`；为 null 时**跳过、不计数、不报错**（`.scratch/.../npc/AbstractVillager.java:237-243`） | 本版两个 `ItemListing` 都不返回 null，所以这条只用于「万一实现写错」的兜底判读 | 交易任务（§8 #17） | 若某条 listing 常量配错（例如 `Item` 为 null）⇒ 该条**静默消失**，列表长度会少于 22；§8 #17 的 `size() == 22` 是唯一能抓到它的闸 |
| ⑧ | 未知职业 id 在 `VillagerData.CODEC` 上的解码行为 | **已核（读代码）**：`BuiltInRegistries.VILLAGER_PROFESSION.byNameCodec().fieldOf("profession").orElseGet(() -> VillagerProfession.NONE)`（`.scratch/.../npc/VillagerData.java:19-23`） | `orElseGet` 对「字段存在但 id 未知」是否也走默认值（而不只是「字段缺失」）取决于 DFU 的 `Codec#orElseGet` 实现——本机没有 DFU 源码可读 | 存档兼容任务（§6.4） | 若未知 id 会硬失败 ⇒ §6.6 改写为「卸载模组后带植物学家的存档会读档失败」，并把措辞从「回落」改成「须先清职业」 |
| ⑨ | `plain_chives` 的贴图是否存在 | **未核**：`ModItemModelProvider` 走 `basicItem(item)`（`ModItemModelProvider.java:52`）⇒ 模型指向 `farmstorm:item/plain_chives`，但**没有任何工具校验该 PNG 存在**（`verify_project.py` 全文无 `texture` / `.png` 命中）；现有 `textures/item/` 29 张、无此项 | 贴图由 `tools/gen_textures.py` 生成，能不能出图取决于 `ICONS` 表的新条目写得对不对（这是工具行为，不是 API） | 物品任务（§8 #30） | 若 `gen_textures.py` 出不了图 ⇒ 手绘 16×16 PNG 放进目录即可（**不影响**注册面与语言键） |
| ⑩ | `PoiType` 注册表在**客户端**是否需要同样的条目 | **已核一半**：`PoiType` 走内建注册表（`Registries.java:177`），不是数据包注册表 ⇒ 双端都会加载模组的注册；但 POI **记录**（`world/poi/`）是服务端数据，客户端不持有 | ① 客户端在什么路径上会碰 `PoiType`（例如 `VillagerProfession` 的 `heldJobSite` 谓词在客户端跑不跑）；② 若客户端缺这个注册项会不会崩（理论上不会，因为双端同 jar） | 双端联机验收（§8 #13 的联机版） | 若客户端报缺注册项 ⇒ 说明某个客户端路径真的用了 POI（本版没有客户端 POI 逻辑），按 RT-64 登记；退路是**不加**客户端专用代码（本来就没有） |

**三条「已从待验证降级为已核」的记录**（写在这里，免得 49 / 50 号再当风险）：① `VillagerProfession` 的六字段与 `workSound` 可为 null（`VillagerProfession.java:21-28` + `LivingEntity.java:1325-1329`）；② `Villager#getTypeName` 的语言键拼法（`Villager.java:743-749`，非 `minecraft` 命名空间必带命名空间段）；③ `ItemListing#getOffer` 的签名（`VillagerTrades.java` 的 `interface ItemListing`：`@Nullable MerchantOffer getOffer(Entity, RandomSource)`）。

---

## 10. 与上游的差异登记（13 条，**不自行改上游**）

按「与 46 号的矛盾 / 与 `BRIEF` 的矛盾 / 与工具实际行为的偏差 / 源码注释漂移」四类排。**本文只登记，不改任何上游文件。**

| # | 涉及 | 上游写的 | 本文的处理 | 性质 |
|---|---|---|---|---|
| 1 | `BRIEF` §3.4 的 16 项必改 vs `Farmstorm.java:176-179` | 16 项里**没有**「改 `Farmstorm.java` 的 12 类断言」这一项 | **本文补上**（§3 #11）：`total == 97` → `98`、`items == 33` → `34`。不改的后果是启动日志直接打 `→ FAIL`（`:180-184`），与 `verify_project.py` 全绿形成**两套闸互相矛盾**。这是本版**最容易漏**的一项，因为它不在 `BRIEF` 的清单里、也不在任何文档的「必改」表里 | 上游漏项（本文补，建议 49 号把它列成独立任务） |
| 2 | `BRIEF` §1.7 / §3.4 第 13 行 vs 实际注释数 | 两处都只说 `CultivatorLinkPayload:32-33` 这一处注释要改 | **实际有 6 处硬写「16」**（`FarmstormConfig:236` / `:238` / `:952-953`、`OutpostCoreBlockEntity:116` / `:349`、`OutpostCoreMenu:30`），加上 `CultivatorLinkPayload` 共 **7 处**（§2.7.2 逐条给位置与现文）。只改 1 处会让另外 6 处继续撒谎 | 上游计数不足（不影响 98 / 176 / 340，影响交付完整度） |
| 3 | `BRIEF` §1.6「全仓 0 处」 | 写「精确 grep `PoiType` / `VILLAGER_PROFESSION` / `POINT_OF_INTEREST` 命中 0」 | **按 `src/` 口径成立**（本文实测 `src/` 全树 0 命中）。⚠️ 但 `.scratch/p25a/src/` 里有原版的 `PoiTypes` / `PoiType` / `VillagerProfession`——那是**反编译源树**，不是本仓。验收若用「全仓 grep」会命中几十处 ⇒ 本文一律写「`src/` 下 0 命中」，并在 §0.2 第 5 条把两棵树分开点名 | 计数口径（不是缺陷） |
| 4 | `BRIEF` §3.3 vs 47 号 §5.6 | `BRIEF` 说语言键「至少 +2，最终值由 47/48 号逐条点名后写死」；47 号写「**下限** 340」 | **本文钉死 340 × 2**（§5 两条逐字点名）。理由：新增面只有「一个物品名 + 一个职业名」两条；POI 类型、标签、交易项都没有语言键（交易项复用物品名）。⇒ 340 是**最终值**，不是下限 | 上游留的活，本文已办 |
| 5 | 46 号 D-199 vs plan `:11` | plan 写「会**额外刷新出**一个史诗品质改良农作物交易」 | **本文按裁定 #18 写**（固定 1 项、首次抽定、之后不变），并在 §2.4.4 用 `Villager.java:398-408` 给源码依据。plan 的原文**只在 46 号 §7 ② 与 47 号附录 A 第 2 行出现**，本文不复述它的措辞（RT-67 的纪律：那五个字不许单独出现） | 口径差异（按裁定写） |
| 6 | 46 号 D-196 的「一核一村民＝原版 POI 行为」 | 只说「原版 POI 行为，第二个村民认领失败」，没给机制 | **本文补机制**：它就是 `PoiType.maxTickets = 1`（`PoiRecord:38-40` / `:48-50`，§2.3.3）。补的理由是：不知道这一条的人会去写一个「第二个村民拒绝认领」的判定，而原版已经给了；反过来，把 `maxTickets` 设成 2 就会得到「一核两村民」，且没人会立刻发现 | 上游留的活，本文已办 |
| 7 | `VillagerTradeEvents:10-11` 的注释 vs `VillagerTradingManager:38` | 注释写「每次 `TagsUpdatedEvent`（数据包重载，含起服）对每个职业各抛一次」 | **不准确**：只在 `UpdateCause == SERVER_DATA_LOAD` 时抛（`VillagerTradingManager.java:37-42`）；`/reload` 的 cause 是 `RELOAD` ⇒ **不重发**。⇒ 验收不能用 `/reload` 判交易是否生效（§2.4.1 第 3 条、§8 #17）。本文在 §3 #29 把它列成注释必改项 | 源码注释漂移（本版顺手改） |
| 8 | 42 号 §1.2 的 `block_entities` 行号 | 写 `:25,29,32,35,39,43,51,56` | **实测是 `:26,30,33,36,40,44,52,57`**（每个都大 1）：42 号取的是 `public static final DeferredHolder<…>` 那一行的行号，注册调用实际在下一行。计数（8）不受影响。本文按实测写（§1.2 第 3 行），并把这类偏差登记在此，提醒下一位作者**行号要落在调用本身** | 行号漂移（计数不变） |
| 9 | `ModItemModelProvider` 的物品数与注释 | `:20` 写「**31 个**物品模型」、`:84` 写「本模组的 **31 个**物品」 | **实测注册 33 个**（本版改后 34）：`farmstormItems()`（`:85-94`）遍历 `BuiltInRegistries.ITEM`，取到的是全部 `farmstorm:` 物品，与注释的 31 不符（31 是某个早期快照的数）。本版**不写死数字**：把两处注释改成「本模组注册的全部物品（数量由注册表决定）」，避免再一次漂移 | 源码注释漂移（本版顺手改） |
| 10 | `ModLangProvider:14-19` 的条目构成 | 「33 item + … = **338**」 | **本文复核成立**（逐组相加 = 338，§1.4）。本版 +2 ⇒ 这张表要改成「34 item + 1 villager + … = **340**」（§3 #32）。⚠️ 同一段还写「179 config = 176 server + 3 client」，本版**不变**（§4.2） | 上游正确，需按增量更新 |
| 11 | `FarmstormTags` 的 key 数与 POI 标签 | 46 号 D-194 只说 `FarmstormTags` 6 → 7 | **易错点**：POI 那条标签用的是**原版**的 `PoiTypeTags.ACQUIRABLE_JOB_SITE`，不新增我们自己的 `TagKey` ⇒ `FarmstormTags` **只 +1**（6 → 7），不是 +2。写成 8 会让 §1.2 之外的标签计数与 §2.6.1 自相矛盾 | 易混淆点（本文点明） |
| 12 | `OutpostCoreBlockEntity:333` 的成本注释 | 写「每刻 × **16** 条 × 核数，代价不成比例」 | 条数随 `cultivatorCapacity` 走 ⇒ 现在是 **24**。本文把它列为**建议同步、非阻塞**（§2.7.2 末），因为它不影响任何判据，只影响估算的准确性 | 注释漂移（非阻塞） |
| 13 | 46 号 D-194 补充「读取点与类型面由 48 号定」 | 46 号明确把「绑定入口还接不接受 `CultivatorBlockEntity` 之外的东西」留给本文 | **本文定为**：① 判据改两处 `instanceof CultivatorBlock`（`OutpostCoreBlockEntity:393` / `:783`）为标签判定；② **候选发现保持** `CultivatorBlockEntity.class`（`:546-547`）；③ `addCultivator(BlockPos)` 签名不动 ⇒ 类型面**不放开**。三条理由见 §2.6.3（唯一成员、写回 `Core` 缓存需要类型、放开类型等于给 0.2.3 预留接口，被裁定 #5 禁止） | 上游留的活，本文已办 |

**四条给 49 号的提示**：

1. **第 1 条是本版最容易漏的必改**：`Farmstorm.java:176-179` 的两处断言不在 `BRIEF` 的 16 项里，但它是**启动日志的唯一闸**。它必须与 `FarmstormItems` 的注册**同一个提交**，否则任何一个中间状态都会打 FAIL。
2. **第 2 条是「改默认值」的实际工作量**：不是改两个数字，是改 **2 个数字 + 7 处注释**。只改数字会让注释与新默认值矛盾，而注释是下一位读者唯一的口径来源。
3. **第 4、6、13 条都是 46 号明确留给 48 号的口子**，本文已各自给了结论（语言键 340 / `maxTickets = 1` / 判据落点与类型面）；**不需要作者再裁**。第 5 条是按裁定 #18 写的口径差异，也不是新决定。
4. **第 3 条会影响验收的第一条命令**：`grep PoiType src/` 应该 0 命中（改前），**不要**写成 `grep -r PoiType .`——那会把 `.scratch/p25a` 的反编译源算进来，得到几十个命中并误判「本仓早就注册过 POI」。
