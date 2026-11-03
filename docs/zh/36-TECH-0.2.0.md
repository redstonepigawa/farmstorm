# 36 号 v0.2.0 技术规格（Technical Spec）

> 项目：农场风云 / Farmstorm · 版本 **0.2.0**（MC 1.21.1 / NeoForge 21.1.213 / Java 21）。
> 唯一真相源：`docs/zh/34-DECISIONS-0.2.0.md`（D-148 ~ D-172，共 25 条）。本文**只引用**决策号，不新开决策、不改口径；与 34 号冲突时以 34 号为准，冲突本身登记在本文 §10。
> 事实底座：`.work/v020-BRIEF.md`（§0 基线 / §1 源码事实 / §2 **19 条**裁定 / §3 增量清单 / §4 风险 RT-42 ~ RT-50）。其中 §2 的 **#17~#19 是写作期新增的三条**（核心存储两道闸 / 交易粒度保持 3 项 / 转换器中文名不改），34 号尚未吸收、D 号待 37 号补登 ⇒ 本文按 19 条写，并把这三条落成 §2.6.4 / §2.4.2 / §2.9（§10 第 15 条）。
> 两路扫描 `.work/v020-scan-A.md` / `.work/v020-scan-B.md` 只作旁证，**下文所有计数都是本文自己从源码点出来的**。
> 设计口径：`docs/zh/35-DESIGN-0.2.0.md`（11 条需求全规格）。本文不复述设计数值，只写「怎么实现、怎么验」。
> 体例参照：`docs/zh/30-TECH-0.1.1.md`（只学 §1 基线核对 / §2 类设计 / §3 动作面表 / §4 config 全表 / §7 验收 / §8 待验证 / §9 差异登记的目录结构，**不引用它的任何数值**）。
> 红线：**无 Mixin（0 个，继续禁止）**；自定义网络包**不设上限**，本版新增 **1 个**（连线），累计 **2 个**（D-172，登记见 §2.8）。
> 写作纪律：注册面 97、server config 175 键 / 60 组、语言键 337 三个数**全部由本文自己逐条点名后相加**（方法写在 §1.2 / §1.3 / §4.1 / §4.3），不抄 34 号、不抄 `BRIEF`；凡本文自定的技术参数（payload 名、NBT 键名、`Property` 名、`RenderType`、tick 载体、等级位次）一律标「**本文确定**」并给理由（汇总见 §2 各条与附录 A 的清单）；**不写任务排期与工时**（属 37 号）。

---

## 0. 阅读约定

### 0.1 本文负责什么

| 项 | 内容 |
|---|---|
| 负责 | 注册面 **97** 的逐类独立点名与计数方法（§1.2）；语言键 **337** 与 server config **175 键 / 60 组**的逐键点名（§1.3、§4）；九个技术构件（品质分值、种植链两条路径、耕地注释清单、村民交易、培养器、核心容器、迁移三出口、连线渲染、转换器停用）的类设计（§2，含写作期新增裁定 #17~#19 的落点）；对 0.1.1 的增 / 改 / 删总表 **46 行**（§3）；资源清单（§5）；存档与兼容**四条分开写**（§6）；RT-42 ~ RT-50 的验证方式（§7）；**36 条**验收自检（§8）；待验证 API **13 条**（§9）；与上游的差异登记 **16 条**（§10） |
| 不负责 | 玩法叙述、时序表、玩家可见变化（35 号）；任务拆分、Phase、里程碑、工时（37 号）；主提示词与阶段提示词（38 / 39 号）；决策本身的取舍（34 号） |
| 不动的文件 | `src/` 下**既有**文件一律只读（本文只描述「要改成什么」，不落地代码）；`docs/zh/00` ~ `35`、`docs/en/*`、`.work/*` 一律只读；本文只新建 `docs/zh/36-TECH-0.2.0.md` 一个文件 |

### 0.2 计数纪律

1. **注册对象、config 键、语言键三个数，本文全部自己数**，并写出「数了哪个文件的哪几行」。不抄 `BRIEF` §0、不抄 34 号 §3.4、不抄 30 号 §1.2。
2. 三张表各自满足「行数 = 声明的计数」「小计 = 逐行相加」「跨表不重不漏」。
3. 数值只允许四个来源：34 号的决策号、`BRIEF` §1 / §2 / §3、两个已给数值的算术、以及本文标「本文确定」的技术参数（每个都给理由）。**不发明玩法数值**。
4. 读文件一律用 `[IO.File]::ReadAllLines($p,[Text.Encoding]::UTF8)`（本机 `Get-Content` 会把无 BOM 的 UTF-8 中文读成 GBK 乱码）。

### 0.3 「本文确定」的清单（13 项，逐项正文有理由）

| # | 参数 | 值 | 正文 |
|---|---|---|---|
| 1 | `CropTier` 的枚举值与 `translationKey()` 形式 | `COMMON`/`RARE`/`EPIC`；`tooltip.farmstorm.tier.<小写名>` | §2.1 |
| 2 | 品质名色用的 `ChatFormatting` | `WHITE` / `BLUE` / `GOLD` | §2.1 |
| 3 | tooltip 品质行的语言键与格式参数 | `tooltip.farmstorm.tier`，`%s` = 档名 | §2.1 |
| 4 | 培养器方块状态属性名 | `working`（`BooleanProperty`） | §2.5 |
| 5 | 培养器 BE 的三个 NBT 键 | `Held` / `Progress` / `Core`（缺 `Core` = 未绑定） | §2.5 |
| 6 | 培养器 BE 的 tick 载体 | 方块侧 `getTicker` → BE 静态 `serverTick`（服务端唯一） | §2.5 |
| 7 | 核心容器的 NBT 键与绑定列表键 | `Items`（`ContainerHelper.TAG_ITEMS`）/ `Cultivators`（Long 列表） | §2.6 |
| 8 | 连线的 payload 名、`MAX_LINKS`、广播常量 | `farmstorm:cultivator_link`；`1024`；`10` / `20` 刻 | §2.8 |
| 9 | 连线的 `RenderType` / 阶段 / 颜色 / 线宽 | `RenderType.lines()`；`Stage.AFTER_PARTICLES`；RGB `0.30/0.85/0.90`、alpha `1.0`；线宽取 `RenderType` 自带的 `1.0` | §2.8 |
| 10 | 交易的三个等级位次 | **1 / 3 / 5** | §2.4 |
| 11 | 交易的 `xp` 与 `priceMultiplier` | `2` / `0.0F` | §2.4 |
| 12 | 核心存储界面的标题键与屏幕贴图 | `container.farmstorm.outpost_core`；复用原版 `textures/gui/container/generic_54.png` | §2.6 |
| 13 | 静态首帧贴图的文件名 | `cultivator_a_side_still.png` / `cultivator_a_top_still.png` | §2.5 |

---

## 1. 基线与注册面核对

### 1.1 平台与构建

| 项 | 值 | 证据 |
|---|---|---|
| Minecraft / NeoForge / Java | **1.21.1 / 21.1.213 / 21** | `gradle.properties:2-3`；Java 21 由 toolchain 固定（`gradle.properties:21-23` 的 `org.gradle.java.installations.paths=D:/project_farm/.tools/jdk21`），本文不挂行号（34 号 §9 ②） |
| `mod_version` | **0.2.0**（现值 0.1.1，本版改） | `gradle.properties:11`（`:10` 是 `mod_license`，34 号 §9 ①） |
| 构建入口 | `.tools\gw.cmd`（唯一入口，无子命令分发） | `gw.cmd:41` |
| 运行目录 | `run/`（`-PfsRunDir=` 可隔离） | `build.gradle` 的 `gameDirectory` |
| Java 文件数（现状） | **134 个**（`src/main/java` 全树逐个 `ReadAllLines` 计数） | 本文自己数的；本版新增 **11 个类**（§2 逐个点名：`CropTier`、`VillagerTradeEvents`、`TierTrade`、`CultivatorBlock`、`CultivatorBlockEntity`、`OutpostCoreMenu`、`OutpostCoreScreen`、`CultivatorLinkPayload`、`CultivatorLinkBroadcaster`、`ClientCultivatorLinks`、`CultivatorLinkRenderer`），改后 **145** |
| 基线提交面 | `src/` 自 `v0.1.1` 标签后 0 改动 | `BRIEF` §1.2 末（`git diff --stat v0.1.1 -- src` 为空）；D-148 |

### 1.2 注册面逐类点名（**本文自己从源码数**，改后共 97）

**计数方法（可复现）**：逐个打开 `src/main/java/com/dashpig/farmstorm/registry/` 下 **11 个**注册类，按「注册调用 + 注册 id 字面量」逐条数，**不按 `EXPECTED_COUNTS` 反推、不看 `Farmstorm.java` 的断言值、不抄 `BRIEF` §0**。三条计数口径：

- `DeferredRegister#register(...)` / `registerItem` / `registerBlock` / `registerComponentType`：数**调用次数**；
- 走工厂方法注册的（`cropItem` / `seedItem` / `crop` / `deployed` / `projectile`）：数**工厂调用次数**，id 由 `CropProfile.path()` 或首个参数字面量给出；
- `damage_type` 是**数据驱动**（`FarmstormDamageTypes.java:14` 的注释：实例由数据包 JSON 提供，代码侧只有 `ResourceKey`），数**静态 `ResourceKey` 常量个数**。

| # | registry | 现在数了什么（文件:行号） | 逐条 ID | 现状条数 | v0.2.0 增量 | 改后 |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | `FarmstormItems.java`：**22** 次 `cropItem(...)`（`:36,38,40,42,44,47,49,51,53,55,57,59,61,63,65,70,72,75,79,85,91,95`）+ **7** 次 `seedItem(...)`（`:89,100,101,102,103,104,106`）+ **3** 次 `registerItem(...)`（`:110,113,116`） | 22 作物：`puffed_wheat` `blast_carrot` `ubi_server` `toxic_ubi_server` `power_beet` `melon_slice_bomb` `melon_grenade` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `lucky_berry` `molotov` `zangief_cane` `wild_apple` `compressed_puffed_wheat` `friend_chives` `point_defense_fungus` `vibration_bean`；7 种子：`chive_seeds` `puffed_wheat_seeds` `power_beet_seeds` `melon_grenade_seeds` `pumpkin_turret_seeds` `awakened_torchflower_seeds` `awakened_pitcher_plant_pod`；3 方块物品：`converter` `compressed_hay_block` `outpost_core` | **32** | **+1**（`cultivator` 的 BlockItem） | **33** |
| 2 | `Registries.BLOCK` | `FarmstormBlocks.java`：**15** 次 `crop(...)`（`:26,27,28,29,30,31,32,33,34,35,36,37,38,39,43`）+ **8** 次 `deployed(...)`（`:46,47,48,50,52,55,62,65`）+ **3** 次 `registerBlock(...)`（`:69,73,82`） | 15 作物方块：`puffed_wheat_crop` `blast_carrot_crop` `ubi_server_crop` `toxic_ubi_server_crop` `power_beet_crop` `melon_grenade_stem` `pumpkin_turret_stem` `awakened_torchflower_crop` `awakened_pitcher_plant_crop` `fang_trap_cactus_block` `bamboo_firecracker_stalk` `red_mushroom_puff_block` `brown_mushroom_puff_block` `lucky_berry_bush` `friend_chives_crop`；8 部署方块：`blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `zangief_cane` `point_defense_fungus` `vibration_bean`；3 其它：`converter` `compressed_hay_block` `outpost_core` | **26** | **+1**（`cultivator`） | **27** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | `FarmstormBlockEntities.java`：**7** 次 `REGISTER.register`（`:24,28,31,34,38,42,50`） | `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `converter` `outpost_core` | **7** | **+1**（`cultivator`） | **8** |
| 4 | `Registries.ENTITY_TYPE` | `FarmstormEntityTypes.java`：**12** 次工厂 `projectile(...)`（`:31,33,35,37,39,41,43,45,49,51,55,57`，工厂体 `:70-78`）+ **1** 次 `REGISTER.register("turret_bullet", …)`（`:61`） | `puffed_wheat` `ubi_server` `toxic_ubi_server` `melon_slice_bomb` `melon_grenade` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `molotov` `compressed_puffed_wheat` `friend_chives` `vibration_bean` `turret_bullet` | **13** | +0 | **13** |
| 5 | `Registries.MOB_EFFECT` | `FarmstormMobEffects.java:24,28,32,36` | `wild_hunter` `zangief_piledriver` `q_bounce` `confusion` | **4** | +0 | **4** |
| 6 | `Registries.SOUND_EVENT` | `FarmstormSounds.java:21,26,31` | `wild_hunter_loop` `zangief_piledriver` `friend_chives` | **3** | +0 | **3** |
| 7 | `Registries.RECIPE_TYPE` | `FarmstormRecipes.java:23` | `converting` | **1** | +0 | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | `FarmstormRecipes.java:25` | `converting` | **1** | +0 | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | `FarmstormCreativeTabs.java:16` | `main` | **1** | +0 | **1** |
| 10 | `Registries.DAMAGE_TYPE` | `FarmstormDamageTypes.java:20,23,26`（3 个 `ResourceKey` 常量） | `void_bolt` `crop_blast` `zangief_slam` | **3** | +0 | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `FarmstormDataComponents.java:16` | `temporary_enchantments` | **1** | +0（品质走静态字段，D-150） | **1** |
| 12 | `Registries.MENU` | `FarmstormMenus.java:17` | `converter` | **1** | **+1**（`outpost_core` 存储界面） | **2** |
| | **合计** | | | **93** | **+4** | **97** |

逐类小计（与上表逐行相等）：

```
items                 33   ← 32 + 1
blocks                27   ← 26 + 1
block_entity_type      8   ← 7 + 1
entity_type           13
mob_effect             4
sound_event            3
recipe_type            1
recipe_serializer      1
creative_mode_tab      1
damage_type            3
data_component_type    1
menu_type              2   ← 1 + 1
--------------------------------
相加 = 33+27+8+13+4+3+1+1+1+3+1+2 = 97
```

**三条独立算法互为校验**：本文逐条点名 = **97**；`Farmstorm#logRegistrySelfCheck`（`Farmstorm.java:132-156`）的 12 条断言相加（改后）= **97**；`tools/verify_project.py` 的 `EXPECTED_COUNTS` 改后 = **97**。本文的数**不是**从后两者抄来的——它是按上表「调用次数」口径重新数出来的，两条旁证只用来确认口径没跑偏。

**自检断言的逐项旧值 → 新值**（`Farmstorm.java:147-150`，本版必改；34 号 D-171 只说了「93 → 97、逐项 +」，没给逐项旧值）：

| 变量 | 旧值（`:148-150`） | 新值 |
|---|---|---|
| `total` | `93` | **97** |
| `items` | `32` | **33** |
| `blocks` | `26` | **27** |
| `blockEntities` | `7` | **8** |
| `menuTypes` | `1` | **2** |
| 其余 8 类 | `13 / 4 / 3 / 1 / 1 / 1 / 3 / 1` | **不变** |

`damageTypeCount()`（`Farmstorm.java:163-166`）本版**不加项**：品质、交易、培养器、连线都不是伤害类型。

### 1.3 语言键与 config 现状基线（本文逐条点名）

**SERVER config（现状 166 键 / 58 组）**：数法是读 `config/FarmstormConfig.java` 的静态块（`:267` 的 `static {` 到 `:927` 的 `SPEC = b.build();`），用「`b.push("…")` / `b.pop()` 维护组栈、`.define("…")` / `.defineInRange("…")` / `.defineInList("…")` 计一键」的规则逐键落地。本文用脚本跑了一遍并逐组人工复核：

```
键 166 / 组 58 = 34 个顶层组 + 24 个嵌套组
```

> 📌 与 30 号的**组数读法**：30 号 §1.4 的「现状 166 键 / 59 组（35 顶层 + 24 嵌套）」是**改前基准**（0.1.0，`farmland` 组尚在），同文件 §4.1 的**改后**值就是 **58 组**；本文实测 58 组（34 + 24）与之一致、也与 `BRIEF` §0 的 58 一致。**不是偏差**，见 §10 第 1 条。逐组明细见 §4.1。

**CLIENT config（现状 3 键）**：`config/FarmstormClientConfig.java:29-43` 的 `hud.enabled` / `hud.x` / `hud.y`，落 `config/farmstorm-client.toml`。本版**不动**（§4.2）。

**语言键（现状 323 × 2，中英键集完全相同）**：读 `src/generated/resources/assets/farmstorm/lang/zh_cn.json` 与 `en_us.json`，`ConvertFrom-Json` 后取键集：`zh=323`、`en=323`、zh-only = 0、en-only = 0。按前缀分布（本文自己分的组，相加 323）：

```
farmstorm.configuration   169   ← = 166 server + 3 client（不是 166！见 §10 第 12 条）
advancements.farmstorm     36
item.farmstorm             32
block.farmstorm            26
tooltip.farmstorm          16
entity.farmstorm           13
gui.farmstorm               9
death.attack                6
effect.farmstorm            4
subtitles.farmstorm         3
command.farmstorm           3
message.farmstorm           2
key.farmstorm               2
itemGroup.farmstorm         1
container.farmstorm         1
--------------------------------
相加 = 169+36+32+26+16+13+9+6+4+3+3+2+2+1+1 = 323
```

**资源现状（§5 的基数）**：`src/main/resources/assets/farmstorm` 下 **113 张 PNG** = `textures/block` 79 + `textures/item` 29 + `textures/mob_effect` 2 + `textures/misc` 2 + `textures/gui` 1；`*.png.mcmeta` **0 个**；datagen 产物：`blockstates` **26**、`models` **103**、`loot_table` **26**、`recipe` **29**。

### 1.4 两条红线现状

| 项 | 现状（本文复核） | v0.2.0 | 证据 |
|---|---|---|---|
| 自定义网络包 | **1 个**：`farmstorm:core_list`（S2C 只读，`MAX_CORES = 64`），注册点只有 `FarmstormNetwork.java:29-31` 的**一处** `.playToClient(...)` | **2 个**（新增连线 1 个，D-169 / D-172） | `CoreListPayload.java:23,25,28`；全仓 `playToClient\|playToServer` 命中 2 处（1 处是调用、1 处是 `RegisterPayloadHandlersEvent` 的 import 面），实际注册 **1** 个 |
| Mixin | **0 个** | **0 个**，继续禁止 | 全仓 `Mixin`/`mixin` 关键字**0 命中**（`src/`、`build.gradle`、`gradle.properties`、`settings.gradle` 全扫）；无 `*.mixins.json`；`neoforge.mods.toml` 无 mixin 段 |

两条读法（写错任一条，§8 的 #1 / #2 都会红）：

1. 网络包**不进注册面**——payload 走 `RegisterPayloadHandlersEvent`，不写 `BuiltInRegistries`，所以 §1.2 的 97 里没有它（34 号 §5.3 第 4 条）。
2. Mixin 的 0 是**禁项**，不是「本版没轮到」：0.2.0 的九个技术构件全部用继承 + 事件总线 + 原生扩展点实现，无一处需要字节码注入。

---

## 2. 新增 / 改写的技术构件

本节九个构件，每个给「类 / 文件 → 关键签名 → 数据形状 → 与决策的对应」。**凡未标「本文确定」的参数都来自 34 号或源码既有先例。**

### 2.1 品质分级（`CropTier` 枚举 + `CropProfile` 第 9 字段 + 名色 / tooltip 落地）

**决策**：D-149（11 / 7 / 4）、D-150（静态字段）、D-151（名色 + 一行 tooltip）。

**新文件 `util/CropTier.java`**（第 1 个新类）：

```
enum CropTier implements StringRepresentable
  COMMON / RARE / EPIC
  getSerializedName()  → name().toLowerCase(Locale.ROOT)
  translationKey()     → "tooltip.farmstorm.tier." + getSerializedName()
  color()              → ChatFormatting.WHITE / BLUE / GOLD
  nameStyle(Component) → base.copy().withStyle(color())
```

**本文确定（1）（2）（3）**，三条理由：

- 键名形式照既有 `CropCategory.translationKey()`（`CropCategory.java:38-40`：`"tooltip.farmstorm.category." + getSerializedName()`），不新开第二种键名体例；
- `color()` 取 `ChatFormatting.WHITE / BLUE / GOLD` 是 D-151 点名的白 / 蓝 / 金。⚠️ 这**不是**原版 `Rarity` 的配色（`Rarity.java:16-19` 是 白 / 黄 / 青 / 紫），所以更不能复用 `Rarity`——配色对不上（§10 第 9 条）；
- `nameStyle` 收在枚举里，是为了让 3 个物品类共用一个实现，不在三处各写一遍 `withStyle`。

**`CropProfile` 加第 9 字段**（`util/CropProfile.java:32-40`，record 头）：

```
record CropProfile(id, type, categories, cropBlock, seedItem, kind, allowedFaces, throwStack, CropTier tier)
```

第 9 位**放在 `throwStack` 之后**（本文确定，理由：前 8 个字段的行号与语义在 34 号、35 号、两份扫描里都被逐条引用过，追加在末尾不会让任何既有引用的「第 N 字段」错位）。

**加字段的连带改动面（本文逐处点名，共 10 处构造点 + 15 处工厂实参）**：

| 位置 | 现在 | 改后 |
|---|---|---|
| 三个私有工厂签名 | `crop(...)`（`:191-195`）、`throwable(...)`（`:197-201`）、`deployed(...)`（`:203-207`） | 各加一个 `CropTier tier` 形参，转发到 record 构造 |
| 走工厂的 15 条档案 | `:47,49,51,53,55,62,64,68,71,74,77,79,81,83,111` | 每条追加一个 `CropTier.X` 实参 |
| 直接 `new CropProfile(...)` 的 7 条 | `:58`（`melon_slice_bomb`）、`:87`（`molotov`）、`:92`（`zangief_cane`）、`:97`（`wild_apple`）、`:102`（`compressed_puffed_wheat`）、`:117`（`point_defense_fungus`）、`:125`（`vibration_bean`） | 每个构造实参表末尾追加 `CropTier.X` |

`15 + 7 = 22` 与 `CropProfile.ALL`（`:131-136`）的 22 条、`Farmstorm.java:112-115` 的既有断言三者同数——**这是 RT-49 的验收入口**：少补一条，编译期就会在 `new CropProfile(...)` 的参数个数上报错（record 的规范构造器不接受缺参），不会静默漂移。

**名色落地（3 个类，覆盖 29 个物品）**：

| 类 | 覆盖物品数 | 改法 |
|---|---|---|
| `item/ModifiedCropItem.java` | **15**（`ThrowableCropItem` 12 + `FoodCropItem` 3，两者都 `extends ModifiedCropItem`） | 覆写 `Item#getName(ItemStack)`：`return this.profile.tier().nameStyle(super.getName(stack));` |
| `item/DeployableCropItem.java` | **7**（`extends net.minecraft.world.item.BlockItem`，**不继承** `ModifiedCropItem`，必须单独覆写，`:18`） | 同上，用自己持有的 `profile` |
| `item/SeedItem.java` | **7**（`extends ItemNameBlockItem`，`:16`） | 同上 |

`15 + 7 + 7 = 29 = 22 作物 + 7 种子`（22 = 投掷 12 + 部署 7 + 食用 3，与 `Farmstorm.java:112-113` 的既有断言同数；注意**部署方块有 8 个、部署型物品只有 7 个**：`vibration_bean` 的方块是 `deployed(...)`，物品却是 `ThrowableCropItem`）。为什么走 `Item#getName` 而不是在 GUI 里画：`ItemStack#getHoverName()`（`ItemStack.java:731-739`）在没有 `CUSTOM_NAME` / `ITEM_NAME` 组件时**就是**调 `getItem().getName(this)`，而 tooltip 首行（`:755`）、容器里的物品名、聊天栏的物品链接、铁砧界面全部经由它 ⇒ 一次覆写、全界面同色，正是 D-151 要求的「走 Component 的样式」。

**tooltip 一行（一处实现覆盖 29 个物品）**：全部 29 个物品的 tooltip 都汇聚到 `ModifiedCropItem.appendCropTooltip(profile, tips)`（`ModifiedCropItem.java:113`），三个调用点在 `ModifiedCropItem.java:59`、`DeployableCropItem.java:33`、`SeedItem.java:30`。在该方法**末尾**追加一行：

```
tips.add(Component.translatable("tooltip.farmstorm.tier",
        Component.translatable(profile.tier().translationKey())).withStyle(ChatFormatting.GRAY));
```

档名子组件因此带自己的颜色，外壳是灰——与既有的类别行（`:126` 的 `withStyle(GRAY)`）同色，不抢视觉。**不与类别行合并**（D-151 补充：那是另一套语义）。

**品质明确的「四不」**（写错任一条就会把 D-150 做成数据组件）：不新增 `data_component_type`（`data_components` 仍 1）、不写物品 NBT、不新增注册对象、不改 22 张作物贴图。

### 2.2 种植链切断（两条路径各怎么堵）

**决策**：D-152（两条路径）、D-155（方块与物品保留注册）；风险 RT-48。

#### 路径 ①：`ModifiedCropItem#isPlantableSoil`（自定义播种，承载 15 条有作物方块的档案）

现状（`item/ModifiedCropItem.java:101-107`）：

```
private static boolean isPlantableSoil(UseOnContext ctx, BlockState soil) {
    if (!(soil.getBlock() instanceof FarmBlock)) { return false; }
    return true;                 // ← 唯一放行点
}
```

唯一调用点是 `plantCrop(...)` 的 `:72`。**改法：判据改为恒 `false`**（D-152 ①）。两种等价写法都行，本文取「保留方法、函数体 `return false;` 并把原判断留成注释」：

```
private static boolean isPlantableSoil(UseOnContext ctx, BlockState soil) {
    // 裁定 #4 / D-152：v0.2.0 起本模组作物不可种植。原判据「是 FarmBlock 才允许」保留在下一行注释里备查。
    // return soil.getBlock() instanceof FarmBlock;
    return false;
}
```

连带行为（必须一起想清楚，否则会误判成 bug）：

| 调用方 | 现状 | 改后 |
|---|---|---|
| `ModifiedCropItem#useOn`（`:47-50` → `plantCrop`） | 对着耕地播种 | `plantCrop` 在 `:72` 直接 `return PASS` ⇒ `useOn` 返回 PASS ⇒ **右键耕地无反应** |
| `DeployableCropItem#useOn`（`:36-43`） | 先试播种、`PASS` 则走 `super.useOn` 部署 | 播种那支恒 PASS ⇒ **永远走部署分支**，**7 个 `DeployableCropItem`** 的放置**不受影响**（8 个部署方块里的第 8 个 `vibration_bean` 的物品是投掷型，本来就不走这条路）。这是「作物不能种」而不是「部署方块不能放」的分界线 |

#### 路径 ②：7 个 `SeedItem` 的原版播种（`SeedItem extends ItemNameBlockItem`）

现状（`item/SeedItem.java:16-32`）：只覆写了 `appendHoverText`，播种**完全继承** `BlockItem#useOn`（`BlockItem.java:44-51` → `place(new BlockPlaceContext(ctx))`）。`FarmstormCropBlock` **未覆写** `mayPlaceOn` / `canSurvive`，方块侧本来就不拦（`BRIEF` §1.2）。

**改法：覆写 `useOn` 返回 `PASS`**（D-152 ②）：

```
@Override
public InteractionResult useOn(UseOnContext ctx) {
    // 裁定 #4 / D-152：v0.2.0 起 7 个独立种子不可播种（原版 BlockItem#useOn 这条路整条切断）。
    return InteractionResult.PASS;
}
```

三条必须写明的语义：

1. `PASS` 是「这里什么都不做」，**不是** `FAIL`（`FAIL` 会让后续逻辑误以为被拒绝）；返回 `PASS` 后原版会继续尝试方块自己的 `useWithoutItem`（`ServerPlayerGameMode.java:363` → `:370` 的既有顺序），例如手持种子右键箱子仍能开箱——这是原版行为，不该被本版改变。
2. **不消耗手持物**：`PASS` 早于 `BlockItem#place` 的 `itemstack.consume(1, player)`（`BlockItem.java:95`），所以种子不会凭空少一个。
3. **死代码 `canPlantAt`（`ModifiedCropItem.java:53-55`）不动**：它全仓 0 调用者，删它属既存清理，不在本版需求内（D-152 补充）。

#### 不动的部分（D-155，本条的「负向清单」）

| 对象 | 条数 | 为什么不动 |
|---|---|---|
| 作物方块 | 15 | 删方块会让老存档里的方块变空气（D-155 补充） |
| 作物物品 | 22 | 保留注册与创造成分页顺序 |
| 种子物品 | 7 | 同上；种子改由培养器（D-163）与交易（D-157）消耗 |
| 方块战利品表 | 26（其中 15 张是作物方块） | 与注册面同生共死 |
| `FarmstormCropBlock` | 1 个类 | 方块侧不加拒绝——「不能种」只由物品侧断 |

**RT-48 的验收入口**：§8 的 #6、#7 分别断言两条路径（各自单独验），#8 断言「部署方块仍能放」——只堵一条时，#7 或 #8 必有一条红。

### 2.3 耕地体系停用（只注释调用点）

**决策**：D-153（只注释调用点）、D-154（`.dat` 不读不写不删）、D-155（方块不删）；风险 RT-42。

#### 注释清单（7 处，逐处给「为什么不能删」）

| # | 文件:行号 | 注释什么 | 为什么不能删 / 不能整段删 |
|---|---|---|---|
| 1 | `event/FarmstormGameEvents.java:75` | `gameBus.addListener(FarmstormGameEvents::onRightClickBlock);` | 只注释注册行就能让监听器**不生效**；删除声明会连带把 #3 的方法签名也带走，失去对照 |
| 2 | `event/FarmstormGameEvents.java:76` | `gameBus.addListener(FarmstormGameEvents::onFarmlandTrample);` | 同上；这一行是「踩踏保护」的唯一开关 |
| 3 | `event/FarmstormGameEvents.java:241-265` | `onRightClickBlock` 的**整段实现**（含 `:254` 的 `ledger.bind(...)`、`:260-264` 的两条动作栏提示） | 实现体引用 `FarmlandGuard` / `FarmlandLedger` 与 2 条语言键；删掉就没有「原来这里拦过锄地」的可追溯证据（34 号 §8 第 3 条） |
| 4 | `event/FarmstormGameEvents.java:277-281` | `onFarmlandTrample` 的整段实现 | 同上；`FarmlandGuard.protectsFromTrample`（`:219`）的唯一调用点 |
| 5 | `event/FarmstormGameEvents.java:171` | `FarmlandLedger.get(server).backfill(server);`（在 `onServerTickPost` `:166-176` 内） | 这是**唯一的启动期 `get()`**，注释它 = `.dat` 不再被读（D-154） |
| 6 | `config/FarmstormConfig.java:909-912` | `farmlandBonus` 的 `.defineInRange(...)` 定义（键从 `farmstorm-server.toml` 消失） | 键留在 toml 里就等于「本版仍在维护一份已宣布停用的数据」 |
| 7 | `datagen/ModLangProvider.java:403`、`:412`、`:413` | 3 条语言键：`farmstorm.configuration.outpost_core.farmlandBonus`、`message.farmstorm.farmland.no_core`、`message.farmstorm.farmland.quota_full` | 键的定义没了、提示的发出点（#3）也没了；留下语言键只会让配置页露出裸键名 |

#### 三处**必须保留**（整段注释掉就出事）

| 对象 | 行号 | 保留的理由 |
|---|---|---|
| `onServerTickPost` 本身 | `FarmstormGameEvents.java:166-176` | 它还要跑 `CoreListBroadcaster.tick(server)`（`:175`），是浮窗包的**唯一每刻入口**。整段注释掉 ⇒ 浮窗永远收不到包（34 号 §9 ⑦） |
| `CoreChunkTickets.restoreAll(server)` | `FarmstormGameEvents.java:170` | 服务的是**核心的区块票**，与耕地无关 |
| `FarmlandLedger`（555 行）/ `FarmlandGuard`（225 行） | 两个整类 | 裁定 #3：本体一行不改、仍编译、仍在源码树里。删了就没有回头路（34 号 §7 第 1 条） |

#### ⚠️ 一处**必须改口径**：`FarmstormConfig.java:227` 的字段声明**不注释**

D-153 的结论写「`outpost_core.farmlandBonus` 的**读取**」也要注释，但**那两处读取就在被要求「一行不改」的两个类里**：

| 读取点 | 代码 |
|---|---|
| `farmland/FarmlandGuard.java:75` | `int bonus = FarmstormConfig.outpostCoreFarmlandBonus.get();` |
| `farmland/FarmlandLedger.java:305` | `return FarmstormConfig.outpostCoreFarmlandBonus.get() * allyCoreCount(level, player);` |

所以「注释掉字段声明 `:227`」与「两个类一行不改」互斥。**唯一自洽的口径 = A-62**（裁决全文见 §10 末）：

- 字段声明 `FarmstormConfig.java:227` **原样保留**（否则两个保留类编译不过）；
- 只注释**定义** `:909-912`（键从 toml 消失，`166 − 1 = 165` 的算式成立）；
- 两个类里的两处 `.get()` **原样保留**，运行期不可达（`onRightClickBlock`、`backfill` 的调用点已注释）；
- 代价必须写进注释：**字段此时为 `null`**，若将来有人只恢复监听器、不恢复定义，会得到 `NullPointerException` 而不是默认值——这正是 RT-42「误恢复」的具体形态。**注释处必须写这一行**：

```
// 裁定 #3 / D-153：v0.2.0 撤销耕地，勿恢复。
// 注意：本键的 .defineInRange 在 :909-912 已注释，字段现为 null；
// 恢复监听器（:75/:76）前必须先恢复定义，否则 FarmlandGuard:75 抛 NPE。
```

#### `.dat` 的三不（D-154）

`farmstorm_farmland.dat`（落盘名 `FarmlandLedger.java:53` 的 `"farmstorm_farmland"`，格式 `dataVersion: 2` 见 `:55`、`:34`）：**不读、不写、不删**。注释点 #5 之后，`FarmlandLedger.get(...)` 在运行期 0 调用；`of(level)` 的最后两个调用点（`onRightClickBlock:249`、`settleOnRemoval`）随 #3 注释与 §2.7 的整段改写一起消失。文件原样留在存档目录里。

### 2.4 农民交易（`VillagerTradesEvent` + 3 个交易项）

**决策**：D-157（3 项、按等级依次解锁）、D-158（`maxUses = 1` + 断货）、D-159（价格区间 2–5 / 10–32 / 40–64）；风险 RT-46。

#### 事件签名（本文已核，不再是「待验证」）

`.tools/gradle-home/caches/modules-2/…/neoforge-21.1.213-sources.jar!/net/neoforged/neoforge/event/village/VillagerTradesEvent.java`：

| 项 | 值 |
|---|---|
| 类型 | `class VillagerTradesEvent extends Event`（**不可取消**，无 result） |
| 总线 | **`NeoForge.EVENT_BUS`**（game 总线，类注释原文 "It is fired on the `NeoForge#EVENT_BUS`"） |
| 时机 | 每次 `TagsUpdatedEvent` 重载时，**每个** `VillagerProfession` 各触发一次 |
| `getTrades()` | `Int2ObjectMap<List<ItemListing>>`，**键 1~5 恒非 null**（类注释原文 "populated for levels 1-5 (inclusive), so Map#get will never return null"） |
| `getType()` | `VillagerProfession` |
| `getRegistryAccess()` | `RegistryAccess` |
| 交易项接口 | `VillagerTrades.ItemListing#getOffer(Entity, RandomSource)`（`VillagerTrades.java:1593-1595`） |

⇒ **新增 1 个类 `event/VillagerTradeEvents.java`**（第 2 个新类），在 `Farmstorm` 构造器里挂到 `NeoForge.EVENT_BUS`（与 `FarmstormGameEvents.register` 同一处，`:72` 旁）。handler 体：

```
if (event.getType() != VillagerProfession.FARMER) { return; }
event.getTrades().get(1).add(TierTrade.COMMON);
event.getTrades().get(3).add(TierTrade.RARE);
event.getTrades().get(5).add(TierTrade.EPIC);
```

#### 三个等级位次 = **1 / 3 / 5**（本文确定（10））

理由：原版等级只有 5 个位次（`VillagerData.java:13-14`：`MIN_VILLAGER_LEVEL = 1`、`MAX_VILLAGER_LEVEL = 5`；`:15` 的经验阈值 `{0,10,70,150,250}`），34 号 §9 ⑪ 只给了「新手 / 老手 / 大师」三个示例称谓。取 **1 / 3 / 5** 的三个理由：

1. 三个位次**尽量铺开**（1 / 3 / 5），玩家在每一个可感知的阶段（刚入职 / 中期 / 满级）都碰到一次新交易，而不是挤在 1 / 2 / 3；
2. 中间位次 **2、4 留空**，不与原版已有的农民交易抢位置，也不改变原版「每级抽两项」的抽样池比例；
3. `getTrades().get(3)` / `get(5)` 在这个事件里**恒非 null**（类注释），不需要空判断——这一点让「留空位次」成为零风险写法。

#### 交易项的实现（`event/TierTrade.java`，第 3 个新类）

`record TierTrade(CropTier tier, List<Item> pool, int minKey, int maxKey) implements VillagerTrades.ItemListing`：

| 步 | 做什么 | 依据 |
|---|---|---|
| 1 | `Item item = pool.get(random.nextInt(pool.size()));` —— 该档全集等概率抽 1 条 | 35 号 §4.4（报价首次生成时抽一次，之后固定） |
| 2 | `int min = cfg(minKey), max = cfg(maxKey); if (max < min) { int t = min; min = max; max = t; }` —— **配置写反时自动交换**，不抛异常 | 本文确定（无 34 号口径；`trade` 组 6 键允许玩家写反，交换比崩服合理） |
| 3 | `int price = (max == min) ? min : min + random.nextInt(max - min + 1);` —— **闭区间**，`nextInt(bound)` 上界是开区间所以 `+1` | D-159（区间随机） |
| 4 | `ItemCost cost = new ItemCost(Items.EMERALD, price);` | `ItemCost.java:44` 的 `ItemCost(ItemLike, int)` |
| 5 | `return new MerchantOffer(cost, Optional.empty(), new ItemStack(item, 2), 1, 2, 0.0F);` | `MerchantOffer.java:70` 的 `(ItemCost, Optional<ItemCost>, ItemStack, maxUses, xp, priceMultiplier)` |

五个数值的出处，逐项对照（**没有一个新数值**）：

| 值 | 来源 |
|---|---|
| 单次 **2 个作物** | 35 号 §4.3（本文补充的 2，理由：与培养器单次产出同数、`2 ≤ 16` 最严堆叠档） |
| `maxUses = 1` | D-158；`MerchantOffer#isOutOfStock()`（`:181`）在 `uses >= maxUses` 时为真 ⇒ 成交一次即售罄 |
| `xp = 2` | **本文确定（11）**：取原版农民交易里最常见的 `2`（`VillagerTrades.java:90,93,94` 等），三档统一、不按档分 |
| `priceMultiplier = 0.0F` | **本文确定（11）**：把 `MerchantOffer#getModifiedCostCount`（`:107-111`）里的 `floor(i × demand × priceMultiplier)` 一项归零 ⇒ 成交后**不会涨价**（`demand` 在 `maxUses = 1` 下第一笔之后就是 1），报价**恒等于**区间内抽到的整数 |
| 三个区间 2–5 / 10–32 / 40–64 | D-159，进 `trade` 组 6 键（§4.1） |

三条边界写死（免实现期各写一套）：

1. **`priceMultiplier = 0` 不影响声望折扣**：`specialPriceDiff` 在 `:110` 是独立加法项，原版折扣照常作用，因此**实际支付可能低于区间下界**。验收断言的对象是**基准价**（`offer.getItemCostA().count()`，`:117-119`），它严格落在区间内（§8 #17）。
2. **不给种子**（35 号 §4.3 附注 1）；**不改原版任何交易项**（只 `add`，不清空列表）。
3. **不写第二套断货 / 补货逻辑**：断货由 `maxUses = 1` 承担，补货由原版 `resetUses()`（`:141`）承担。本版**不存 NBT、不写计时器、不做 GUI**（D-158）。

#### 2.4.1 价格浮动：本文的结论是「锁死基准价」

35 号 §4.6 把「原版供需 / 声望浮动会不会打穿区间」留给 36 号定。**本文选定：锁死基准价，接受声望折扣**，代码表达如下：

| 浮动通道 | 源码位置 | 本文的处理 | 怎么在代码里表达 |
|---|---|---|---|
| **供需**（demand） | `MerchantOffer#getModifiedCostCount` 的 `floor(i × demand × priceMultiplier)`（`:109`） | **关掉** | `priceMultiplier = 0.0F`（构造器的第 6 个参数）。`demand` 在 `maxUses = 1` 下第一笔之后为 1，乘 0 恒为 0 ⇒ 报价恒等于抽到的整数 |
| **声望折扣**（specialPriceDiff） | 同一个方法的 `+ this.specialPriceDiff`（`:110`） | **保留**（原版正向反馈，玩家可解释） | 不写任何代码：`Villager` 自己调 `setSpecialPriceDiff`（`:169-171`） |
| 「每次重生报价时重算基准价」 | 需要自定义补货钩子 | **否决** | 报价由 `ItemListing#getOffer` 在**首次生成时**产生一份 `MerchantOffer` 并存进村民的 `Offers` NBT；「每次补货重算」需要自己写补货监听 + 重写 NBT，与 D-158「不写第二套补货逻辑」正面冲突 |

⇒ 一句话：**首次报价的基准价严格 ∈ 区间，成交后不涨价，有折扣时只会更低**。§9 ⑪ 记录了这条对 `priceMultiplier` 语义的依赖。

#### 2.4.2 交易的粒度与代价（裁定 #18）

`BRIEF` §2 新增的裁定 #18 定：**保持 3 项、按档随机，不做 22 项、不做轮换**。本文照办，并把已知代价写在这里（不写进实现）：

- 集齐 4 条史诗的期望村民数 ≈ `4 × H₄ ≈ 8.3` 个大师级农民；集齐 7 条稀有 ≈ `7 × H₇ ≈ 18.2` 个老手级农民（35 号 §4.4 的算术，本文复核：`H₄ = 2.0833`、`H₇ = 2.5929`）。
- 这个代价**被接受**（裁定 #18）。要缩短它只能加交易项，而加项就是改 D-157。
- 实现上不引入任何「按档轮换 / 保底」机制：`TierTrade` 只有一次 `random.nextInt(pool.size())`，没有别的状态。

⚠️ 老存档里已满级（5 级）的农民**不会补发**新交易项——交易列表在村民升级时才重建，满级村民已无升级路径。这是原版语义，**本版不做迁移**（不写补发逻辑、不改存档里的 `Offers`）；玩家要么找未满级的农民升上去，要么用新村民（35 号 §4.6、§7 RT-46、§10 第 16 条）。

### 2.5 农作物培养器（方块 / BE / item / 无菜单）

**决策**：D-160（方块本体 + 铁砧属性 + 无菜单）、D-161（双贴图）、D-162（准入）、D-163（600 刻 + 产出）、D-164（核满暂停）；风险 RT-44 / RT-47。

#### 2.5.1 方块（`block/CultivatorBlock.java`，第 4 个新类）

| 项 | 值 / 写法 | 依据 |
|---|---|---|
| 父类 | `BaseEntityBlock` | 与 `ConverterBlock` / `OutpostCoreBlock` 同形 |
| `codec()` | `simpleCodec(CultivatorBlock::new)` | 照 `OutpostCoreBlock.java:33` |
| `getRenderShape` | `RenderShape.MODEL` | `BaseEntityBlock` 默认 `INVISIBLE`，必须显式声明（`ConverterBlock.java:48-52` 的注释记过这条坑） |
| 状态属性 | `static final BooleanProperty WORKING = BooleanProperty.create("working")`，默认 `false` | **本文确定（4）**：D-161 要求「`working` 必须走 blockstate 属性」 |
| `createBlockStateDefinition` | `builder.add(WORKING)` | — |
| `getTicker` | **服务端才返回**，客户端 `return null` | 与 `OutpostCoreBlock.java:56-67` 逐字同形；HC-03 |

方块属性逐项（注册处 `FarmstormBlocks`）：

| 属性 | 值 | 证据 |
|---|---|---|
| 硬度 / 抗爆 | **`strength(5.0F, 1200.0F)`** | D-160；铁砧 `Blocks.java:3053` |
| `mapColor` | `MapColor.METAL` | `Blocks.java:3051` |
| `requiresCorrectToolForDrops()` | 有 | `Blocks.java:3052` |
| `sound` | `SoundType.ANVIL` | `Blocks.java:3054` |
| `pushReaction` | `PushReaction.BLOCK` | `Blocks.java:3055`；同时防「被活塞推走时内部物品复制」 |
| `noOcclusion` | **不加** | 模型是完整立方体（`cube_bottom_top`），加了没有收益 |

#### 2.5.2 方块实体（`blockentity/CultivatorBlockEntity.java`，第 5 个新类）

| 项 | 内容 |
|---|---|
| 父类 | `BlockEntity`。**不 `implements Container`**（本文确定，理由：D-160 无菜单；一旦实现 `Container`，漏斗 / 投掷器就能绕过 `useWithoutItem` 塞入史诗作物，D-162 的准入面当场失守） |
| 字段 | `ItemStack held = ItemStack.EMPTY`、`int progress`、`BlockPos corePos`（`null` = 未绑定；**引用缓存**，唯一真相在核侧，D-165） |
| NBT 键 | `Held`（`ItemStack#save` / `ItemStack.parse`，照 `ConverterBlockEntity.java:396-416` 的写法）、`Progress`（int）、`Core`（long，`BlockPos#asLong`）。**缺 `Core` 键 = 未绑定**（本文确定（5）：不引入哨兵值，缺键即语义） |
| 同步 | **不需要 `getUpdateTag`**：客户端只认 blockstate 的 `working` 与两张模型，不读 BE 的任何字段；绑定关系走 §2.8 的包 |
| tick 载体 | **方块侧 `getTicker` → BE 静态 `serverTick(Level, BlockPos, BlockState, CultivatorBlockEntity)`**（本文确定（6）：与 `OutpostCoreBlockEntity.serverTick` 同形，不用第二套 tick 注册面） |

**状态机（每刻一次，`serverTick` 体）**：

```
1. if (held.isEmpty())        → setWorking(false); progress = 0; return;
2. if (!isUsable())           → setWorking(false); return;        // 未绑定 / 核侧不认 / 核不在
3. if (progress < cycleTicks) → progress++; setWorking(true); return;
4. // progress == cycleTicks：本刻结算一次
   if (acceptAll(batch()))    → held = EMPTY; progress = 0; setWorking(false);
   else                       → setWorking(false);                // 暂停：progress 停在 cycleTicks
```

第 4 步失败后**下一 tick 会再次走到第 4 步**（`progress` 仍等于 `cycleTicks`），核腾出空间的那一 tick 自动完成投递——「从暂停处继续、不重新计时」（D-164）由此天然成立，不需要额外状态位。

`setWorking(boolean)` 只在值真的变化时写 blockstate（`level.setBlock(pos, state.setValue(WORKING, v), Block.UPDATE_CLIENTS)`），不每刻发包。

#### 2.5.3 准入（D-162，四道闸合并成一次判定）

```
boolean isUsable():
    corePos != null
 && level.getBlockEntity(corePos) instanceof OutpostCoreBlockEntity core
 && core.hasCultivator(this.worldPosition)                       // 核侧 16 条列表命中
 && OutpostCoreBlockEntity.inRange(corePos, this.worldPosition)  // 3×3 区块覆盖
```

| 闸 | 35 号的分法 | 本文的落点 |
|---|---|---|
| 1 范围闸 | 培养器区块与某己方核切比雪夫距离 ≤ 1 | 并入 `isUsable()`：`inRange(corePos, worldPosition)` 为真（**复用** `OutpostCoreBlockEntity.inRange` 的纯函数形态 `:151-157`，不写第二份范围公式） |
| 2 绑定闸 | 核侧列表里有它 | `core.hasCultivator(pos)` |
| 3 手持闸 | 普通 / 稀有作物或其种子 | 按档判、**不按 id 白名单**（D-162 补充：22 条档案的 tier 一改，准入面自动跟着变） |
| 4 空槽闸 | 槽内无物 | `held.isEmpty()`；不叠加、不替换 |

可放入物品共 **23** 个 = 11 普通作物 + 7 稀有作物 + 5 准入面内的种子（`puffed_wheat_seeds` / `melon_grenade_seeds` / `pumpkin_turret_seeds` / `chive_seeds` / `power_beet_seeds`）。被挡在门外 **6** 个 = 4 个史诗作物 + 2 个史诗种子（`awakened_torchflower_seeds` / `awakened_pitcher_plant_pod`）。

`useWithoutItem` 的顺序（**任何一步不通过都 `return PASS`**：不消耗、不提示、不报错）：

```
if (level.isClientSide) return InteractionResult.SUCCESS;   // 照 ConverterBlock.java:81-83 的 HC-03 取舍
if (!isUsable() || !held.isEmpty() || !admissible(player 手持物)) return InteractionResult.PASS;
held = player 手持物.split(1); progress = 0; setChanged();
return InteractionResult.CONSUME;
```

⚠️ 非己方 / 未绑定的玩家在客户端会看到一次挥手动画（`SUCCESS` 是客户端的既有取舍，照 `ConverterBlock`），但**服务端不消费手持物、不入槽**——「右键无反应」的定义是行为面无反应，不是动画面无反应。

#### 2.5.4 产出（D-163，两处判据写死）

| 判据 | 写法 | 为什么这样写 |
|---|---|---|
| 「有独立种子物品」 | `profile.seedItem() != null && !profile.seedItem().location().equals(profile.id())` | 22 条档案分三类：7 条 `seedItem` 指向**别的**物品、8 条指向**自身**、7 条为 `null`。这个布尔表达式**恰好**只在第一类为真（8 条自身的 `seedItem().location()` 就是 `profile.id()`）⇒ 不需要第二张表，也不会把「种子 = 自身」算成有种子（D-163 补充否决的正是这种算法） |
| 数量 | `cultivator.yield`（默认 2） | D-163 |
| 周期 | `cultivator.cycleTicks`（默认 600） | D-163 |

产出批次（`List<ItemStack>`，投递前先算好）：

- 有独立种子：`[作物 × yield, 种子 × yield]`——准入面内 **5 条**（`puffed_wheat` / `melon_grenade` / `pumpkin_turret` / `friend_chives` / `power_beet`）；
- 无独立种子：`[作物 × yield]`——准入面内 **13 条**（`18 − 5`）。

投入的是种子时，产出仍由**父作物档案**决定（35 号 §6.4 补充）：`SeedItem#profile()` 与 `ModifiedCropItem#profile()` 返回同一条 `CropProfile`，所以「放 `chive_seeds` 与放 `friend_chives` 产出相同」不需要额外分支。

#### 2.5.5 双贴图切换与 `working` 属性（D-161）

**5 张 PNG + 2 个 mcmeta**（3 张 + 2 mcmeta 由材质包原样拷入，2 张静态首帧从 `editable_frames/` 取）：

| 文件 | 尺寸 | 用途 |
|---|---|---|
| `textures/block/cultivator_a_side.png` + `.png.mcmeta` | 32×512（16 帧） | 工作态侧面帧条（`frametime 2`） |
| `textures/block/cultivator_a_top.png` + `.png.mcmeta` | 32×512（16 帧） | 工作态顶面帧条（**16 帧里只有 9 帧不同**，1–7 与 9–15 回文重复，见 RT-47） |
| `textures/block/cultivator_a_bottom.png` | 32×32 | 底面（静态，两态共用） |
| `textures/block/cultivator_a_side_still.png` | 32×32 | 空闲态侧面首帧（拷自 `editable_frames/side_00.png`） |
| `textures/block/cultivator_a_top_still.png` | 32×32 | 空闲态顶面首帧（拷自 `editable_frames/top_00.png`） |

**`cultivator_a_*` 三个名字不许改**：材质包 README 明确要求模型引用 `farmstorm:block/cultivator_a_top` / `_side` / `_bottom`（D-160 补充）。两张静态副本是**新增**文件，命名由本文定（**本文确定（13）**：加 `_still` 后缀。理由：`_a_top` / `_a_side` 已被帧条占用，同一路径不能指向两张图）。

| 模型文件 | 父模型 | `top` / `side` / `bottom` |
|---|---|---|
| `models/block/cultivator.json`（空闲） | `minecraft:block/cube_bottom_top` | `cultivator_a_top_still` / `cultivator_a_side_still` / `cultivator_a_bottom` |
| `models/block/cultivator_working.json`（工作） | `minecraft:block/cube_bottom_top` | `cultivator_a_top` / `cultivator_a_side` / `cultivator_a_bottom` |

`blockstates/cultivator.json`（**两态**，走 datagen）：

```
{"variants":{
  "working=false":{"model":"farmstorm:block/cultivator"},
  "working=true" :{"model":"farmstorm:block/cultivator_working"}}}
```

#### 2.5.6 破坏掉落（D-160 补充）

`onRemove` 照 `ConverterBlock.java:88-96` 的**四条件**（`!state.is(newState.getBlock())` ∧ `!movedByPiston`）→ 掉落内部那一个培养物。因为 BE 不实现 `Container`，用 `Containers.dropContents(Level, BlockPos, NonNullList<ItemStack>)` 重载（`Containers.java:27`）而不是 `Container` 重载。方块本体由战利品表掉落（§5），两者不重叠。

### 2.6 核心改容器（54 格 + 16 条绑定 + 菜单 + 屏幕）

**决策**：D-165（16 条绑定）、D-166（54 格容器 + 菜单 + 老档兼容）、D-167（破坏掉落）；风险 RT-44。

#### 2.6.1 `OutpostCoreBlockEntity`（改写）

| 项 | 现在（`:48`） | 改后 |
|---|---|---|
| 类声明 | `class OutpostCoreBlockEntity extends BlockEntity` | `… extends BlockEntity implements Container` |
| 字段 | `ownerUuid` / `ownerName` / `placedGameTime`（`:82-86`）、`cleared`（`:92`） | 追加 `NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY)`、`List<BlockPos> cultivators` |
| 容器契约 | 无 | `getContainerSize()` → `54`；`isEmpty` / `getItem` / `removeItem` / `removeItemNoUpdate` / `setItem` / `clearContent` / `stillValid` 七个（照 `ConverterBlockEntity.java:72-146` 的逐方法形态；`stillValid` 用 `Container.stillValidBlockEntity`） |
| NBT | `saveAdditional` / `loadAdditional` 只写 3 键（`:411-424`） | 追加 `Items`（`ContainerHelper.saveAllItems` / `loadAllItems`，`ContainerHelper.java:24-28`，键名常量 `TAG_ITEMS = "Items"`）与 `Cultivators`（`ListTag<LongTag>`，`BlockPos#asLong`，≤ 16 条） |
| 同步 | 无 `getUpdateTag` / `getUpdatePacket` | 两个都补（见下） |
| 新增方法 | — | `canAccept(List<ItemStack>)`（只读模拟）、`acceptAll(List<ItemStack>)`（真插入）、`cultivatorCount()`、`hasCultivator(BlockPos)`、`addCultivator(BlockPos)`、`removeCultivator(BlockPos)`、`cultivators()` |

**`canAccept` / `acceptAll` 三条纪律**：① **先算后写**——`canAccept` 在副本上模拟合并与占位，`acceptAll` 只有模拟通过才真写（否则会出现半批落地）；② 「放得下」按**整批**判（D-164 补充）；③ `acceptAll` 成功后必须 `setChanged()` 并在同一 tick 内 `sendBlockUpdated`（否则关着界面的玩家看到旧内容）。

**`getUpdateTag` / `getUpdatePacket` 的体积方案（RT-44 的正面回答）**：

| 方法 | 返回什么 | 为什么 |
|---|---|---|
| `getUpdateTag(HolderLookup.Provider)` | **只写 `ownerUuid` / `ownerName` / `placedGameTime` 三个键**，**不写 `Items`、不写 `Cultivators`** | 客户端**不需要**这两份数据：54 格物品在界面打开时由原版菜单槽位同步（`addSlot` + `ClientboundContainerSetSlotPacket`）逐格下发；连线由 §2.8 的包下发。54 个 `ItemStack` 全量进 `getUpdateTag` 会让每次区块送到客户端都多出几 KB，收益是 0 |
| `getUpdatePacket()` | `ClientboundBlockEntityDataPacket.create(this)`（`BlockEntity.java:213` 的默认实现即此形态） | 只在**绑定变化**的三个时点由 `level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)` 触发（放置 / 迁移 / 培养器增删） |

结论：**同步面体积与 0.1.1 持平**，存档面增量只有 `Items` + `Cultivators` 两块。

#### 2.6.2 老档兼容读（只有三个键时怎么初始化）

| 老档里的键 | 读入结果 |
|---|---|
| `ownerUuid` / `ownerName` / `placedGameTime` 存在（`DeployedOwnership.readUuid/readName` + `tag.getLong`，`:421-423`） | 照旧赋值 |
| 缺 `Items` | `ContainerHelper.loadAllItems` 读到空 list ⇒ **54 个空槽**，不报错、不清档 |
| 缺 `Cultivators` | 绑定列表为空（老核绑的是耕地，不是培养器） |
| 老档里**多出**的 `Bindings` / `ByCore` 之类键 | 不读、不删、原样留在 `CompoundTag` 里（本版不认也不清理） |

⚠️ 不得把「老档没有 54 格」读成「54 格都被占用」，也不得把 owner 键当物品读——这是 RT-44 里最容易写错的一处。

#### 2.6.3 菜单 + 屏幕（照 `ConverterMenu` / `ConverterScreen` 范式）

| 构件 | 文件 | 关键点 |
|---|---|---|
| 菜单 | `menu/OutpostCoreMenu.java`（第 6 个新类，照 `ConverterMenu` 216 行的结构） | `super(FarmstormMenus.OUTPOST_CORE.get(), id)`；**54 槽**（6 行 × 9 列，x = 8 + 18×col，y = 18 + 18×row）；玩家背包 3 行（y = 140）+ 快捷栏（y = 198）；`quickMoveStack` 两向（0–53 ⇄ 54–89）；`stillValid` 用 `ContainerLevelAccess` |
| 客户端兜底容器 | 同上 | 客户端构造用 `new SimpleContainer(54)`（照 `ConverterMenu.resolveContainer` `:100-104`） |
| 屏幕 | `client/OutpostCoreScreen.java`（第 7 个新类，照 `ConverterScreen` 86 行） | `imageWidth = 176`、`imageHeight = 222`；`renderBg` 用**六参** `blit`（`ConverterScreen.java:69-74` 的注释写明五参形式会按 256×256 算 UV、整体错位） |
| 贴图 | **复用原版** `minecraft:textures/gui/container/generic_54.png`（**本文确定（12）**） | 54 格 = 原版双箱，槽位坐标（8 / 18 / 140 / 198）与 176×222 版式是原版常量 ⇒ 新增 GUI 贴图 **0 张** |
| 标题键 | `container.farmstorm.outpost_core` | 照 `container.farmstorm.converter`（`ConverterBlock.java:75`）的体例 |
| 注册 | `FarmstormMenus.java`（+1）、`client/FarmstormClient.java:52-54` 区（+1 行 `event.register(FarmstormMenus.OUTPOST_CORE.get(), OutpostCoreScreen::new)`） | `menu_types 1 → 2` |

`storageSlots` 与菜单槽数的关系（D-166 补充的技术落点）：**只在创建菜单时读一次**，且该键值域钉死 `54..54`（§4.1），所以「已打开的菜单」与「新开的菜单」不可能不一致。

#### 2.6.4 方块交互与掉落（`block/OutpostCoreBlock.java`）

**两道闸（`BRIEF` §2 新增的裁定 #17：己方 + 玩家触及范围内）**。触及范围**不新设自定义常量**，直接用原版容器菜单的 `stillValid` 语义：

| 闸 | 判据 | 写在哪 |
|---|---|---|
| A 己方 | `core.isAlly(player)`（核主 + 队友，D-138 的既有判定） | `useWithoutItem`（开界面之前） |
| B 触及范围 | **原版 `stillValid` 语义**：`AbstractContainerMenu.stillValid(access, player, block)`（`AbstractContainerMenu.java:70`）→ `Container.stillValidBlockEntity(be, player)`（`Container.java:81-82`，默认距离参数 `4.0F`）→ `player.canInteractWithBlock(pos, 4.0)`（`Player.java:2229-2232`：`AABB(pos).distanceToSqr(eye) < (blockInteractionRange + 4.0)²`） | 菜单的 `stillValid(Player)` 覆写里，**照抄** `ConverterMenu` 的 `Container.stillValidBlockEntity(this, player)` 形态 |

三条读法：

1. **`useWithoutItem` 只判 A**（己方）。B 由菜单每 tick 的 `stillValid` 承担——这与原版箱子完全一致（箱子也不在 `use` 里判距离，而是打开后由 `stillValid` 关掉菜单）。
2. **不许自己写距离常量**：既有的 `RANGE_CHUNKS = 1`（3×3 区块）是**绑定 / 连线**的范围，与「玩家手能不能碰到这一格」是两回事。用 `stillValid` 就自动继承原版的 `blockInteractionRange()`（默认 4.5）+ `4.0` 的眼到方块 AABB 距离，以及「该格方块仍是它自己」这一条。
3. 实际观感：约 8.5 格（`4.5 + 4.0`）之内可以一直开着界面；走远后菜单被原版关掉，再右键则重新判 A + B。

| 钩子 | 改法 |
|---|---|
| `useWithoutItem`（新增） | 服务端：`if (!core.isAlly(player)) return InteractionResult.PASS;` 否则 `player.openMenu(state.getMenuProvider(level, pos))` → `CONSUME`。客户端：`return InteractionResult.SUCCESS`（照 `ConverterBlock.java:81-83`，HC-03）⇒ **非己方右键不开界面**（D-166 补充 + 裁定 #17 的 A 闸） |
| `getMenuProvider`（新增） | `new SimpleMenuProvider((id, inv, player) -> new OutpostCoreMenu(id, inv, ContainerLevelAccess.create(level, pos)), Component.translatable("container.farmstorm.outpost_core"))` |
| 菜单的 `stillValid`（新增） | `return Container.stillValidBlockEntity(this.core, player);`（若菜单的容器是从 `ContainerLevelAccess` 解析出来的，则用 `AbstractContainerMenu.stillValid(this.access, player, FarmstormBlocks.OUTPOST_CORE.get())`——两条等价，取与 `ConverterMenu` 一致的那条）⇒ 裁定 #17 的 B 闸 |
| `onRemove`（改，`:101-107`） | 保留 `core.release()`；**追加**四条件掉落：`if (!state.is(newState.getBlock()) && !movedByPiston && level.getBlockEntity(pos) instanceof Container c) { Containers.dropContents(level, pos, c); }`（照 `ConverterBlock.java:88-96`） |
| `getStateForPlacement` / `setPlacedBy` / `getTicker` | **不动**（放置上限 2、归属写入、服务端 ticker 保持原样） |

`PASS` 而不是 `FAIL` / `SUCCESS`：退回 `PASS` 表示「我不处理」，把手感交回原版，不会吞掉别的交互（`ServerPlayerGameMode.java:363` → `:370` 的既有顺序：物品交互未消费时才回调 `useWithoutItem`）。D-170 对转换器用 `PASS` 是同一条理由。

⚠️ 与 35 号的一处口径变化：35 号 §8.3 写「存储访问判据只有**一道**（己方），范围闸只存在于绑定与连线」，并在 §12 B-24 留了「若作者要加范围闸」的接口。裁定 #17 已把范围闸加回来了（走原版 `stillValid`，**不是** 3×3 区块）⇒ **35 号 §8.3 那段 ⚠️ 已过期**，本文按两道闸写（§10 第 13 条）。

### 2.7 培养器迁移（核被拆时的三出口）

**决策**：D-168；风险 RT-43。位置：`OutpostCoreBlockEntity#settleOnRemoval`（现在 `:341-394`，整段改写）。保持既有结构：先判「真移除」、再算集合、最后动数据。

```
1. if (!(level instanceof ServerLevel) || ownerUuid == null) return;
2. chunk = getChunkNow(corePos 所在区块); if (chunk == null) return;                 // 无法证明方块没了 ⇒ 什么都不动
3. if (chunk.getBlockState(corePos).getBlock() instanceof OutpostCoreBlock) return;   // 方块还在 ⇒ 卸载 / 停服
4. List<BlockPos> bound = cultivators();          // 复制一份再遍历（不边遍历边改）
5. for (BlockPos p : bound):
     接替核 = 「覆盖 p ∧ 己方 ∧ 还有空位」的其它核（最近优先，平手键同 D-165）
     if (接替核 != null)                → 出口① 改绑：新核 addCultivator(p) + 培养器 BE 改写缓存
     else if (getChunkNow(p 所在区块) == null) → 出口③ 只摘本核侧绑定，培养器 NBT 原值保留
     else if (p 处已不是 CultivatorBlock)      → 出口③ 同形（方块已被别人挖走）
     else                               → 出口② 保留并标记未绑定（只有 BE 存在时清 `Core` 键）
6. 只清 cultivators 列表，**不清 items**（物品由 onRemove 的 dropContents 负责，两条钩子分工）
7. this.release();                                 // 摘索引 + 释放区块票 + 请求刷新浮窗（既有方法，末尾一行不动）
```

| 出口 | 条件 | 对培养器做什么 | 对世界做什么 |
|---|---|---|---|
| ① 改绑 | 找到接替核 | 写新核的 16 条列表 + 刷新培养器 BE 的 `Core` 缓存 | **不动** |
| ② 保留并标记未绑定 | 无接替核 ∧ 培养器区块在内存 ∧ 该格仍是培养器 | 清掉 `Core` 键（`Held` / `Progress` 原样保留）⇒ 它变「无法使用」 | **不动** |
| ③ 防御性跳过 | 无接替核 ∧（区块不在内存 ∨ 该格已不是培养器） | 什么都不写（NBT 原值保留） | **不动** |

**三处必须写进注释的负面清单**：

1. **不再有 `setBlock(DIRT)`**：0.1.1 的 `:387` 随整段改写消失。培养器是独立方块，「无法使用」不等于「被拆掉」（D-168 补充）。
2. **不为迁移加载区块**：全程 `getChunkNow`（`ServerChunkCache#getChunkNow`，只查已加载、不触发加载）。被绑的培养器必在核的 3×3 内，而 `CoreChunkTickets` 的票把这一圈摁在内存里，`release()` 在最后一行才放票 ⇒ 迁移期间这些区块在内存。
3. **物品不在这里掉**：54 格走 `onRemove` 的 `Containers.dropContents`（§2.6.4）；`settleOnRemoval` 只管绑定与索引。

**核侧列表的惰性清理**（本文确定）：`cultivators()` 在**选核 / 放置绑定**这两条低频路径上做一次 `getChunkNow` 校验，把「该格已不是培养器」的条目摘掉（不加载区块）。运行期每刻的 `isUsable()` **不**做这个校验（每刻 × 16 条 × 16 个培养器 = 256 次区块查询，代价不成比例）；它只查核侧列表命中 + `inRange`。

### 2.8 连线渲染（客户端线框）

**决策**：D-169（客户端线框 + 新包 + 仅范围内下发）、D-172（登记为第 2 个包）；风险 RT-45。

#### 2.8.1 数据形状（`network/CultivatorLinkPayload.java`，第 8 个新类）

```
record CultivatorLinkPayload(List<Link> links) implements CustomPacketPayload
record Link(BlockPos core, BlockPos cultivator) {}

TYPE      = new Type<>(Farmstorm.id("cultivator_link"))     // 本文确定（8）
MAX_LINKS = 1024                                            // 本文确定（8）
LINK_STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, Link::core,
        BlockPos.STREAM_CODEC, Link::cultivator, Link::new)
STREAM_CODEC = LINK_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINKS))
```

**为什么不带 `total`、不带维度**：`core_list` 的 `total` 是「截断前的真实总数」（`CoreListPayload.java:20-22`）；连线是**按玩家过滤后的完整集合**，不会截断（`MAX_LINKS` 只是解码上限）。维度也不需要：培养器与核必同维度（D-165 补充），跨维度的线不存在。

`MAX_LINKS = 1024` 的来历（算术，两个乘数都来自 config 值域）：`maxPerPlayer` 上限 **64** × `cultivatorCapacity` 上限 **16** = 1024。现实值是 `2 × 16 = 32`（默认每玩家 2 核），一个包 32 × 16 字节 ≈ 512 字节。

注册：`FarmstormNetwork.onRegisterPayloads` 追加第二条 `.optional().playToClient(...)`，**协议版本串必须从 `"1"` 改为 `"2"`**（`FarmstormNetwork.java:17` 的注释：codec 一变必须跟着变）。

#### 2.8.2 下发时机与范围（`network/CultivatorLinkBroadcaster.java`，第 9 个新类）

结构与 `CoreListBroadcaster`（215 行）同形，复用它的去抖口径（不发明第二套）：

| 项 | 值 | 依据 |
|---|---|---|
| 去抖 | `MIN_INTERVAL_TICKS = 10` | 沿用 `CoreListBroadcaster.java:31` |
| 范围重扫 | `RESCAN_INTERVAL_TICKS = 20`（**本文确定（8）**：1 秒一次，覆盖「走进 / 走出范围」这条无事件路径；`core_list` 的对账是 100 刻，连线比它密，因为线随位置变） | — |
| 收件人判定 | `player.connection.hasChannel(CultivatorLinkPayload.TYPE)`（照 `CoreListBroadcaster.java:143`） | optional 通道，未装模组的客户端不该被发 |

**下发 / 撤回时机（五条，逐条给触发点）**：

| # | 时机 | 谁触发 |
|---|---|---|
| 1 | 玩家进入某核的 3×3 区块范围 | `tick` 里的 20 刻重扫 |
| 2 | 玩家离开范围 | 同上（重扫后组装结果为空 ⇒ 发**空列表**清残留） |
| 3 | 绑定变化（培养器放置 / 被拆 / 迁移改绑） | `requestAll(server)`，与 `CoreListBroadcaster.requestAll` 同一处（`OutpostCoreBlockEntity.setOwner` / `release`） |
| 4 | 核被拆 | 同上（`release()` 末尾已有一次 `requestAll`，`:185`），连线广播器在同处补一次请求 |
| 5 | 登录 / 换维度 / 重生 / 登出 | 前三个照 `FarmstormGameEvents` 的既有请求点（`:103,120,128`）；登出清记录（照 `CoreListBroadcaster.onLogout`，`:114`） |

**范围判据**：`OutpostCoreBlockEntity.inRange(corePos, playerBlockPos)`——**直接复用这个静态方法，不写第二份范围公式**（D-169 的「玩家身处该核的 3×3 区块范围内」与 D-165 的绑定覆盖是同一个 `RANGE_CHUNKS = 1`）。

#### 2.8.3 客户端（第 10 / 11 个新类）

`client/ClientCultivatorLinks.java`：`volatile List<CultivatorLinkPayload.Link> links = List.of()`，handler 只做 `context.enqueueWork(...)`（照 `ClientCoreList.java:24-26`）；换服 / 断线清空（照 `:38-41`）。

`client/CultivatorLinkRenderer.java`（**全项目第一次在世界空间画线**，API 已核，见 §9 ②）：

| 项 | 值 | 依据 |
|---|---|---|
| 事件 / 总线 | `RenderLevelStageEvent`，`NeoForge.EVENT_BUS`，客户端专属 | `RenderLevelStageEvent.java:34-35` 的类注释 |
| 阶段 | **`Stage.AFTER_PARTICLES`**（**本文确定（9）**） | `RenderType.lines()` 的 composite state 是 `TRANSLUCENT_TRANSPARENCY` + `VIEW_OFFSET_Z_LAYERING`（`RenderType.java:604-612`），而 `AFTER_TRANSLUCENT_BLOCKS` 的官方注释明说半透明在该阶段可能不正确、应改用 `AFTER_TRIPWIRE_BLOCKS` 或 `AFTER_PARTICLES` |
| 几何载体 | **新建**一个 `PoseStack`，`translate(-cam.x, -cam.y, -cam.z)`（**不**改事件给的 `getPoseStack()`，那会把后续阶段一起平移） | `RenderLevelStageEvent#getCamera()`（`:113`）、`#getPoseStack()`（`:78`） |
| 顶点源 | `Minecraft.getInstance().renderBuffers().bufferSource()` → `getBuffer(RenderType.lines())` | `WildHunterOutline.java:95` 是同一句的既有先例（它只取 buffers、不画线） |
| 画线 | **`LevelRenderer.renderLineBox(PoseStack, VertexConsumer, AABB, float, float, float, float)`**（`LevelRenderer.java:2252-2253`，public static）+ `buffers.endBatch(RenderType.lines())` | 原版自己画线框就用它（`LevelRenderer.java:2331-2354` 是它的顶点写法）；比手写顶点稳：`POSITION_COLOR_NORMAL` 三个元素（`RenderType.java:601`）缺一个就会在 `BufferBuilder.endLastVertex` 抛 `Missing elements in vertex`（`WildHunterOutline.java:54-57` 记过同类崩溃） |
| 线的两端 | 核中心 `(x+0.5, y+0.5, z+0.5)` → 培养器中心 `(x+0.5, y+0.5, z+0.5)`；两端各画一个 0.25 格 `AABB` 线框 | 本文确定（9）：端点框让「哪一头是核」一眼可辨，代价是每对多 24 个顶点 |
| 颜色 | RGB `0.30F / 0.85F / 0.90F`，alpha `1.0F` | **沿用核心边界粒子的同一组数值**（`OutpostCoreBlockEntity.java:63` 的 `DustParticleOptions(new Vector3f(0.30F, 0.85F, 0.90F), 1.0F)`）——同一视觉语义（核心范围）用同一颜色，不发明新数值 |
| 线宽 | `RenderType.lines()` 自带的 `1.0`（`RenderStateShard.LineStateShard(OptionalDouble.empty())`，`RenderType.java:606`） | 本文确定（9）：改线宽必须自建 `RenderType`，本版不做 |
| 遮挡 | 由 `RenderType.lines()` 的深度测试决定（不穿透地形） | `RenderType.java:610` 的 `COLOR_DEPTH_WRITE` |

**客户端只读三条**（D-169 补充）：① 收到什么画什么，不推导绑定；② 不跨维度缓存（换维度后由 `clear()` 清）；③ 离开范围后收到的**空列表必须真的清空**（不是「忽略空包」）。

### 2.9 转换器停用（右键无反应，配方保留）

**决策**：D-170。

| 对象 | 改法 | 保留什么 |
|---|---|---|
| `block/ConverterBlock.java:78-86` 的 `useWithoutItem` | 整体替换为 `return InteractionResult.PASS;`（客户端 / 服务端同一句，不再分支） | — |
| `getMenuProvider`（`:70-76`） | **保留原样**（没人调用它） | 将来恢复时不用重写（plan 写的是「暂时移除」） |
| `ConverterMenu` / `ConverterScreen` / `FarmstormMenus.CONVERTER` | **不动** | 菜单链与屏幕注册继续编译 |
| 26 条 `farmstorm:converting` 配方 + 2 shaped + 1 shapeless（共 29 个 json） | **不动** | 「原版作物 ⇄ 改良作物」映射的唯一载体 |
| 创造栏条目（`FarmstormItems.creativeTabItems()` `:125`） | **不动** | plan：只作为装饰方块，装饰方块必须拿得到 |
| 方块 / BE / 贴图 / 语言键 | **不动** | — |
| **中文名（裁定 #19）** | **保持「农产品转换器」**：`block.farmstorm.converter` 与 `item.farmstorm.converter` 两条语言键的**值一个字都不改** | `ModLangProvider.java:94`、`:128`。plan 标题写「农作物转换器」，但那是需求标题，不是方块名；改名会让老存档里的物品名与 wiki 截图全部对不上 |

**为什么是 `PASS` 不是 `FAIL` / `SUCCESS`**：`SUCCESS` / `CONSUME` 会吞掉右键事件（挡住手上物品对该格的使用）；`FAIL` 会让调用方以为「被明确拒绝」。`PASS` = 「这里什么都不做」（与 §2.2 的 `SeedItem#useOn` 同一条理由）。

**一处必须记住的连带后果**（35 号 §9.2 已登记）：转换器停用后「原版作物 → 本模组作物」这条老路断掉，玩家获取作物的入口只剩「农民交易（§2.4）+ 培养器（§2.5）+ 战利品 + 创造栏」。这不是缺陷，是 R2 + R10 叠加的结果。

---

## 3. 动作面总表（对 0.1.1 的增 / 改 / 删，逐行 46 条）

列说明：**类别** = 注册 / 类 / 数据 / 配置 / 语言 / 停用。**现值** = 0.1.1 已发布态的实际内容；**改后** = v0.2.0 的目标态。决策号一律引 34 号，风险号一律引 `BRIEF` §4。

| # | 类别 | 路径或 类#方法 | 现值 | 改后 | 决策号 | 风险号 |
|---|---|---|---|---|---|---|
| 1 | 注册 | `registry/FarmstormBlocks.java` | 26 | **27**（+`cultivator`） | D-171 | RT-44 |
| 2 | 注册 | `registry/FarmstormBlockEntities.java` | 7 | **8**（+`cultivator`） | D-171 | RT-44 |
| 3 | 注册 | `registry/FarmstormItems.java` | 32 | **33**（+`cultivator` 的 BlockItem） | D-171 | — |
| 4 | 注册 | `registry/FarmstormMenus.java` | 1（`converter`） | **2**（+`outpost_core`） | D-166、D-171 | RT-44 |
| 5 | 注册 | 其余 8 类（entity_types / mob_effects / sound_events / recipe_types / recipe_serializers / creative_tabs / damage_types / data_components） | 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 | **不变** | D-171 | — |
| 6 | 类 | `util/CropTier.java` | 不存在 | **新增枚举**（`COMMON`/`RARE`/`EPIC` + `translationKey()` + `color()` + `nameStyle`） | D-149、D-150 | RT-49 |
| 7 | 类 | `util/CropProfile.java:32-40` | 8 字段 record | **9 字段**（+`CropTier tier`，位置在 `throwStack` 后） | D-150 | RT-49 |
| 8 | 数据 | `util/CropProfile.java:47-128` | 22 条档案无档位 | **22 条逐条补 tier**（11 / 7 / 4） | D-149 | RT-49 |
| 9 | 数据 | `util/CropProfile.java:139-143` | 池 19 条 | **11 条**（仅普通档全集） | D-156 | RT-50 |
| 10 | 类 | `item/ModifiedCropItem#getName(ItemStack)` | 继承 `Item`（无覆写） | 覆写 → 按档着色（覆盖 15 个物品） | D-151 | RT-49 |
| 11 | 类 | `item/DeployableCropItem#getName(ItemStack)` | 继承 `BlockItem`（不继承 `ModifiedCropItem`） | 覆写 → 按档着色（7 个物品） | D-151 | RT-49 |
| 12 | 类 | `item/SeedItem#getName(ItemStack)` | 继承 `ItemNameBlockItem` | 覆写 → 跟随父作物档色（7 个） | D-151 | RT-49 |
| 13 | 类 | `item/ModifiedCropItem#appendCropTooltip`（`:113-127`） | 只加类别行 | 末尾**追加 1 行品质行**（3 个调用点全覆盖 29 个物品） | D-151 | — |
| 14 | 类 | `item/ModifiedCropItem#isPlantableSoil`（`:101-107`） | 是 `FarmBlock` 才允许 | **恒 `false`**（原判据留注释） | D-152 | RT-48 |
| 15 | 类 | `item/SeedItem#useOn` | 继承 `BlockItem#useOn` 的原版播种 | **覆写返回 `PASS`** | D-152 | RT-48 |
| 16 | 类 | `block/FarmstormCropBlock` | 未覆写 `mayPlaceOn` / `canSurvive` | **不动**（方块侧不拦） | D-152、D-155 | RT-48 |
| 17 | 停用 | `event/FarmstormGameEvents.java:75` | 注册 `onRightClickBlock` | **注释**（带理由行） | D-153 | RT-42 |
| 18 | 停用 | `event/FarmstormGameEvents.java:76` | 注册 `onFarmlandTrample` | **注释**（带理由行） | D-153 | RT-42 |
| 19 | 停用 | `event/FarmstormGameEvents.java:241-265` | 锄地拦截 + 写绑定 + 2 条动作栏提示 | **整段注释** | D-153 | RT-42 |
| 20 | 停用 | `event/FarmstormGameEvents.java:277-281` | 踩踏保护 | **整段注释** | D-153 | RT-42 |
| 21 | 停用 | `event/FarmstormGameEvents.java:171` | `FarmlandLedger.get(server).backfill(server)` | **只注释这一行**（`:166-176` 与 `:170`、`:175` 保留） | D-153、D-154 | RT-42 |
| 22 | 停用 | `config/FarmstormConfig.java:909-912` | `outpost_core.farmlandBonus` 定义 | **注释定义**；`:227` 的字段声明**保留**（§2.3 的例外） | D-153 | RT-42 |
| 23 | 停用 | `datagen/ModLangProvider.java:403` | `farmstorm.configuration.outpost_core.farmlandBonus` | **注释** | D-153 | RT-42 |
| 24 | 停用 | `datagen/ModLangProvider.java:412-413` | `message.farmstorm.farmland.no_core` / `quota_full` | **注释**（2 条） | D-153 | RT-42 |
| 25 | 数据 | `farmland/FarmlandLedger.java` / `FarmlandGuard.java` | 555 + 225 行，各有调用者 | **一行不改**，运行期 0 调用者 | D-153 | RT-42 |
| 26 | 数据 | `farmstorm_farmland.dat` | 启动期被 `get()` 读写 | **不读、不写、不删** | D-154 | RT-42 |
| 27 | 类 | `event/VillagerTradeEvents.java` | 不存在 | **新增**（`VillagerTradesEvent` 订阅 + 3 项） | D-157 | RT-46 |
| 28 | 类 | `event/TierTrade.java` | 不存在 | **新增**（`ItemListing` 实现，报价现抽） | D-157、D-159 | RT-46 |
| 29 | 数据 | 3 个交易项 | 无 | 等级 **1 / 3 / 5**；`maxUses = 1`；单次 2 个作物 | D-157、D-158 | RT-46 |
| 30 | 类 | `block/CultivatorBlock.java` | 不存在 | **新增**（`working` 属性 + `getTicker` + `useWithoutItem` + `onRemove` 掉落） | D-160、D-162 | RT-44、RT-47 |
| 31 | 类 | `blockentity/CultivatorBlockEntity.java` | 不存在 | **新增**（不实现 `Container`；`Held` / `Progress` / `Core`） | D-160、D-163 | RT-44 |
| 32 | 数据 | 培养器周期 / 产出 | 无 | **600 刻**；2 作物（5 条再 +2 种子）；核满暂停 | D-163、D-164 | RT-44 |
| 33 | 类 | `blockentity/OutpostCoreBlockEntity.java:48` | 只 `extends BlockEntity` | **`implements Container`（54 格）** | D-166 | RT-44 |
| 34 | 数据 | `OutpostCoreBlockEntity` NBT（`:411-424`） | 3 个键 | **+`Items`（54 格）+`Cultivators`（≤16 条）** | D-165、D-166 | RT-44 |
| 35 | 类 | `OutpostCoreBlockEntity#getUpdateTag` | 不存在 | **新增**（只写 owner 三键，不写物品） | D-166 | RT-44 |
| 36 | 类 | `OutpostCoreBlockEntity#getUpdatePacket` | 不存在 | **新增**（`ClientboundBlockEntityDataPacket.create(this)`） | D-166 | RT-44 |
| 37 | 类 | `OutpostCoreBlockEntity#settleOnRemoval`（`:341-394`） | 耕地三出口（含 `setBlock(DIRT)` `:387`） | **培养器三出口**（改绑 / 标记未绑定 / 跳过），**没有变泥土** | D-168 | RT-43 |
| 38 | 类 | `block/OutpostCoreBlock#useWithoutItem` | 不存在 | **新增**（己方开界面，非己方 `PASS`） | D-166 | RT-44 |
| 39 | 类 | `block/OutpostCoreBlock#onRemove`（`:101-107`） | 只 `release()` | **追加**四条件 `Containers.dropContents` | D-167 | RT-44 |
| 40 | 类 | `menu/OutpostCoreMenu.java` + `client/OutpostCoreScreen.java` | 不存在 | **新增 2 个类**（54 槽 + 176×222，复用原版 `generic_54.png`） | D-166 | RT-44 |
| 41 | 类 | `network/CultivatorLinkPayload.java` | 不存在 | **新增**（`farmstorm:cultivator_link`，`MAX_LINKS = 1024`） | D-169、D-172 | RT-45 |
| 42 | 类 | `network/CultivatorLinkBroadcaster.java` + `client/ClientCultivatorLinks.java` + `client/CultivatorLinkRenderer.java` | 不存在 | **新增 3 个类**（去抖 10 / 重扫 20 刻；`RenderType.lines()`） | D-169 | RT-45 |
| 43 | 类 | `network/FarmstormNetwork.java:17,29-31` | 1 个包，协议版本 `"1"` | **2 个包，协议版本 `"2"`** | D-169、D-172 | RT-45 |
| 44 | 停用 | `block/ConverterBlock.java:78-86` | 客户端 `SUCCESS` + 服务端 `openMenu` | **返回 `PASS`**（菜单 / 配方 / 创造栏保留） | D-170 | — |
| 45 | 配置 | `config/FarmstormConfig.java` | 166 键 / 58 组 | **175 键 / 60 组**（−1 +2 +2 +6） | D-159、D-163、D-165、D-166 | — |
| 46 | 数据 | `Farmstorm.java:119` 池断言 / `:147-150` 注册断言 | `19` / `93` + 12 类分项 | **`11`** / **`97`** + 分项（items 33 / blocks 27 / BE 8 / menu 2） | D-156、D-171 | RT-50 |

**46 行**的分类计数：**注册 5**（#1~5）+ **类 23**（#6,7,10~16,27,28,30,31,33,35~43）+ **数据 8**（#8,9,25,26,29,32,34,46）+ **停用 9**（#17~24,44）+ **配置 1**（#45）= **46**。**删 / 停用没有一处是「删文件」**：`FarmlandLedger` / `FarmlandGuard` / `ConverterMenu` / `ConverterScreen` / 26 条配方 / 15 个作物方块 / 22 个作物物品 / 7 个种子 / 26 张战利品表**全部保留**。

---

## 4. config 全表

### 4.1 server（逐组键数 + 合计；**本文自己点名**）

**数法**：读 `FarmstormConfig.java` 的静态块 `:267`（`static {`）~ `:927`（`SPEC = b.build();`），按组栈逐键落地。结果 **166 键 / 58 组 = 34 个顶层组 + 24 个嵌套组**。

| 组 | 键数 | 嵌套明细 | v0.2.0 |
|---|---|---|---|
| `converter` | 3 | — | 不动 |
| `stack` | 2 | — | 不动 |
| `deploy` | 3 | — | 不动 |
| `teams` | 2 | — | 不动 |
| `explosion` | 2 | — | 不动 |
| `throwable` | 1 | — | 不动 |
| `food` | 1 | — | 不动 |
| `puffed_wheat` | 2 | — | 不动 |
| `blast_carrot` | 5 | `nausea` 2 | 不动 |
| `ubi_server` | 2 | — | 不动 |
| `toxic_ubi_server` | 4 | — | 不动 |
| `melon_slice_bomb` | 2 | — | 不动 |
| `melon_grenade` | 6 | — | 不动 |
| `pumpkin_turret` | 5 | — | 不动 |
| `awakened_torchflower` | 16 | 6 个嵌套组 × 2（strength / resistance / haste / speed / water_breathing / night_vision） | 不动 |
| `awakened_pitcher_plant` | 10 | 3 个嵌套组 × 2（regeneration / saturation / absorption） | 不动 |
| `fang_trap_cactus` | 9 | — | 不动 |
| `bamboo_firecracker` | 2 | — | 不动 |
| `red_mushroom_puff` | 5 | `nausea` 2 + `blindness` 2 | 不动 |
| `brown_mushroom_puff` | 5 | `weakness` 2 + `slowness` 2 | 不动 |
| `lucky_berry` | 24 | 5 个效果组 × 2 + `chance` 8 | 不动 |
| `power_beet` | 10 | 4 个嵌套组 × 2（strength / speed / haste / resistance） | 不动 |
| `molotov` | 3 | — | 不动 |
| `wild_apple` | 2 | — | 不动 |
| `wild_hunter` | 11 | — | 不动 |
| `zangief_cane` | 8 | — | 不动 |
| `qbounce` | 4 | — | 不动 |
| `friend_chives` | 3 | — | 不动 |
| `point_defense_fungus` | 3 | — | 不动 |
| `vibration_bean` | 2 | — | 不动 |
| `growth` | 2 | — | 不动 |
| `trap` | 3 | — | 不动 |
| `outpost_core` | **3** | — | **改：−1 增 +2 ⇒ 4** |
| `compressed` | 1 | — | 不动 |
| `cultivator` | — | — | **新增 2** |
| `trade` | — | — | **新增 6** |
| **现状合计（34 组）** | **166** | **24 个嵌套组** | — |
| **改后合计（36 组）** | **175** | **24**（不变） | **净 +9** |

逐组相加校验（现状）：`3+2+3+2+2+1+1+2+5+2+4+2+6+5+16+10+9+2+5+5+24+10+3+2+11+8+4+3+3+2+2+3+3+1 = 166` ✓

#### 4.1.1 增 / 改 / 删的 11 行（逐键点名）

| # | 完整键路径 | 类型 | 默认 | 值域 | 动作 | 决策号 |
|---|---|---|---|---|---|---|
| 1 | `outpost_core.farmlandBonus` | int | ~~16~~ | ~~0–1024~~ | **删**（注释定义，字段保留） | D-153 |
| 2 | `outpost_core.cultivatorCapacity` | int | **16** | **1–64** | **增** | D-165 |
| 3 | `outpost_core.storageSlots` | int | **54** | **54–54** | **增** | D-166 |
| 4 | `cultivator.cycleTicks` | int | **600** | **20–72000** | **增**（新组） | D-163 |
| 5 | `cultivator.yield` | int | **2** | **1–64** | **增**（新组） | D-163 |
| 6 | `trade.common.min` | int | **2** | **1–64** | **增**（新组） | D-159 |
| 7 | `trade.common.max` | int | **5** | **1–64** | **增** | D-159 |
| 8 | `trade.rare.min` | int | **10** | **1–64** | **增** | D-159 |
| 9 | `trade.rare.max` | int | **32** | **1–64** | **增** | D-159 |
| 10 | `trade.epic.min` | int | **40** | **1–64** | **增** | D-159 |
| 11 | `trade.epic.max` | int | **64** | **1–64** | **增** | D-159 |

（11 行 = 1 删 + 2 改组 + 2 新组 8；`166 − 1 + 2 + 2 + 6 = 175`。）

**五个值域的理由**（34 号只给默认值，值域属技术面）：

| 键 | 值域来源 |
|---|---|
| `cultivatorCapacity` 1–64 | 下界 1 = 「至少能绑一个」；上界 64 = 「与堆叠上限同量级，且与 `maxPerPlayer` 的上界相乘就是 §2.8 的 `MAX_LINKS = 1024`」 |
| `storageSlots` **54–54** | **本文确定**：D-166 明确「不得让 54 变成别的值」，而 54 同时是界面版式（6×9）与 `getContainerSize()` 的返回值；钉死比「读一次再钳制」少一条不一致路径。`min == max` 的退化区间是否被 `ModConfigSpec` 接受，见 §9 ⑬（退路：改 `54..55` 并在创建菜单时钳到 54） |
| `cycleTicks` 20–72000 | 下界 20 = 1 秒（再短就不叫「培养」）；上界 72000 沿用同文件既有 `deploy.turretMiningTicks` 的上界（`FarmstormConfig.java:295`），72000 = 3600 × 20 |
| `yield` 1–64 | 上界 64 = 原版堆叠上限；下界 1 = 不许产出 0 个（否则进度白跑） |
| `trade.*` 1–64 | 绿宝石堆叠上限 64；下界 1 = 白送不被允许 |

`trade` 组的三条运行期纪律（写在读值处，不写第三套机制）：① `min > max` 时**交换**（§2.4 步 2）；② `min == max` 时取该值（区间退化成单值，不崩）；③ 三个区间**互不校验**（允许把稀有区间设得比普通低——那是玩家自己的世界）。

#### 4.1.2 组数与段数的口径

- **组** = `b.push(...)` 的次数（58 → 60），与落盘 toml 的 `[section]` 行同数。
- 新增的 `cultivator`、`trade` 是**顶层组**（与 `outpost_core` 同级），因此顶层组 34 → **36**，嵌套组仍 **24**。
- `outpost_core` 段仍在（3 键 → 4 键），**不是**「删组重建」——删组会让老 toml 里该段三个旧值全部失效。

### 4.2 client（3 键，不变）

| 组 | 键 | 类型 | 默认 | 值域 | 载体 |
|---|---|---|---|---|---|
| `hud` | `hud.enabled` | boolean | `true` | — | `config/farmstorm-client.toml` |
| `hud` | `hud.x` | int | `4` | 0–3840 | 同上 |
| `hud` | `hud.y` | int | `4` | 0–2160 | 同上 |

**本版不动**：§2 的九个构件没有一个是客户端设置项。这 3 键**不计入** §4.1 的 175（`BRIEF` §0 的既有口径），但**计入**语言键的 `farmstorm.configuration.*`（见 §4.3）。

### 4.3 语言键增删（逐条点名 + 本文自己数出的总数）

**现状 323**（方法见 §1.3）。本版 **−3 + 17 = +14 ⇒ 337**。

#### 4.3.1 删（3 条）

| # | 键 | 现值位置 | 删除理由 |
|---|---|---|---|
| 1 | `farmstorm.configuration.outpost_core.farmlandBonus` | `ModLangProvider.java:403` | 键的定义已注释（§4.1 第 1 行） |
| 2 | `message.farmstorm.farmland.no_core` | `:412` | 唯一发出点 `FarmstormGameEvents:260-264` 已注释 |
| 3 | `message.farmstorm.farmland.quota_full` | `:413` | 同上 |

删完后 `message.farmstorm` 整个前缀桶归 **0**——本版唯一被清空的前缀。

#### 4.3.2 增（17 条，逐条点名）

| # | 键 | 中文 | 英文 | 条数 | 决策号 |
|---|---|---|---|---|---|
| 1 | `item.farmstorm.cultivator` | 农作物培养器 | Cultivator | 1 | D-160 |
| 2 | `block.farmstorm.cultivator` | 农作物培养器 | Cultivator | 1 | D-160 |
| 3 | `container.farmstorm.outpost_core` | 据点核心 | Outpost Core | 1 | D-166 |
| 4–6 | `tooltip.farmstorm.tier.common` / `.rare` / `.epic` | 普通 / 稀有 / 史诗 | Common / Rare / Epic | 3 | D-150、D-151 |
| 7 | `tooltip.farmstorm.tier` | 品质：%s | Tier: %s | 1 | D-151 |
| 8–9 | `farmstorm.configuration.cultivator.cycleTicks` / `.yield` | 培养周期（刻） / 单次产出数量 | Cultivation cycle in ticks / Items per cycle | 2 | D-163 |
| 10–11 | `farmstorm.configuration.outpost_core.cultivatorCapacity` / `.storageSlots` | 每个核心可绑定的培养器数量 / 核心存储的格子数 | Cultivators bound per core / Storage slots per core | 2 | D-165、D-166 |
| 12–17 | `farmstorm.configuration.trade.{common,rare,epic}.{min,max}` | 普通 / 稀有 / 史诗档的绿宝石价格下界与上界 | Common / Rare / Epic trade price lower / upper bound | 6 | D-159 |

`1 + 1 + 1 + 3 + 1 + 2 + 2 + 6 = 17`。

两处口径写死：

1. **方块名与物品名各 1 条**（不是 1 条共用）：0.1.1 里 `block.farmstorm.outpost_core` 与 `item.farmstorm.outpost_core` 就是两条（`ModLangProvider.java:107` 与 `:37` 的注释），照此办理。
2. **config 语言键与 server config 键 1:1**：新增 10 个 config 键 ⇒ 新增 10 条 `farmstorm.configuration.*`（`169 − 1 + 10 = 178`）。

#### 4.3.3 改后总数（337）与逐前缀校验

```
item.farmstorm             32 → 33    (+1)
block.farmstorm            26 → 27    (+1)
container.farmstorm         1 →  2    (+1)
tooltip.farmstorm          16 → 20    (+4)
farmstorm.configuration   169 → 178   (−1 删 + 10 增)
message.farmstorm           2 →  0    (−2)
其余 9 个前缀桶                         (不变)
--------------------------------------------
323 − 3 + 17 = 337
```

逐前缀相加校验（改后）：`178 + 36 + 33 + 27 + 20 + 13 + 9 + 6 + 4 + 3 + 3 + 0 + 2 + 1 + 2 = 337` ✓

**中英同键**由 `ModLangProvider` 的构造方式保证（同一张 `ENTRIES` 表按列写两份，`:64-68`），所以 `zh = en = 337`、zh-only = 0、en-only = 0 是结构性结论。

**顺带必改的一处注释**：`ModLangProvider.java:13-16` 的类注释逐项写着「32 item + 26 block + … + 169 config + … = 323」，落完 datagen 后必须同步成「33 / 27 / 178 / 20 / 0 / 337」——那一段自己就写着「改表时必须同步本段」。

---

## 5. 资源清单

**新增 12 个文件**：PNG **5** + mcmeta **2** + blockstate 1 + 方块模型 2 + item 模型 1 + 战利品表 1。**不改任何既有贴图。**

| # | 文件（相对 `src/main/resources` 或 `src/generated/resources`） | 产自 | 说明 |
|---|---|---|---|
| 1 | `assets/farmstorm/textures/block/cultivator_a_side.png` | 手拷（材质包） | 32×512，16 帧帧条 |
| 2 | `assets/farmstorm/textures/block/cultivator_a_side.png.mcmeta` | 手拷 | `frametime 2` / `interpolate false` / `frames 0..15` |
| 3 | `assets/farmstorm/textures/block/cultivator_a_top.png` | 手拷 | 32×512，16 帧帧条（只有 9 帧不同，RT-47） |
| 4 | `assets/farmstorm/textures/block/cultivator_a_top.png.mcmeta` | 手拷 | 与 #2 **逐字节相同** |
| 5 | `assets/farmstorm/textures/block/cultivator_a_bottom.png` | 手拷 | 32×32，静态 |
| 6 | `assets/farmstorm/textures/block/cultivator_a_side_still.png` | 手拷（`editable_frames/side_00.png`） | 32×32 静态首帧（文件名 **本文确定**） |
| 7 | `assets/farmstorm/textures/block/cultivator_a_top_still.png` | 手拷（`editable_frames/top_00.png`） | 32×32 静态首帧 |
| 8 | `assets/farmstorm/blockstates/cultivator.json` | datagen（`ModBlockStateProvider`） | **两态**：`working=false` / `working=true` |
| 9 | `assets/farmstorm/models/block/cultivator.json` | datagen | 空闲：父 `cube_bottom_top`，三面 → `_still` / `_still` / `_bottom` |
| 10 | `assets/farmstorm/models/block/cultivator_working.json` | datagen | 工作：父 `cube_bottom_top`，三面 → `_top` / `_side` / `_bottom` |
| 11 | `assets/farmstorm/models/item/cultivator.json` | datagen | `withExistingParent("cultivator", "farmstorm:block/cultivator")` |
| 12 | `data/farmstorm/loot_table/blocks/cultivator.json` | datagen | 掉落自身（1 条 item 表，照 `outpost_core` 的 `createSingleItemTable`） |

**增量对账**：PNG `113 → 118`（+5）；`*.png.mcmeta` `0 → 2`；`blockstates` `26 → 27`；`models`（block + item 合计）`103 → 106`（+2 方块模型 +1 item 模型）；`loot_table` `26 → 27`；`recipe` 仍 **29**（不变）。

**新增 GUI 贴图 0 张**：核心存储界面复用原版 `minecraft:textures/gui/container/generic_54.png`（§2.6.3，本文确定（12））。

**三处「不改」写成负向清单**（否则验收会去改它们）：

| 不改什么 | 条数 | 为什么 |
|---|---|---|
| 22 个作物物品贴图 | 22 | D-151：品质只影响名色与 tooltip（`textures/item` 的 29 张 = 22 作物 + 7 种子） |
| 7 个种子物品贴图 | 7 | 同上 |
| 15 个作物方块贴图与既有方块贴图 | 79 张里的其余 | 本版不动任何既有方块外观 |

**两条 datagen 的连带改动（`BRIEF` 未覆盖，见 §10 第 10 条）**：

1. `ModItemModelProvider.usesBlockModel(Block)`（`:57-59`）现在只认 `ConverterBlock \|\| OutpostCoreBlock` ⇒ **必须加 `|| block instanceof CultivatorBlock`**。否则 `registerModels` 会落到 `basicItem(item)`（`:50-51`），要求一张**不存在**的 `textures/item/cultivator.png`，游戏内表现为紫黑格。
2. `ModBlockStateProvider` 新增 `cultivatorStates(Block)`：不能用现成的 `simpleBlock(block, model)`（那生成的是 `"": {...}` 单态），必须 `getVariantBuilder(block).partialState().with(CultivatorBlock.WORKING, false).modelFile(idle)` + `.partialState().with(WORKING, true).modelFile(working)`。

---

## 6. 存档与兼容

**四条，必须分开写**（34 号 §9 ⑨ 把这条列为最容易混的地方；本文按「不读不写不删 / 索引不变 / 区块 NBT 大变 / 新增 BE」逐条给判据）。

### ① `farmstorm_farmland.dat`：不读、不写、不删

| 项 | 内容 |
|---|---|
| 落盘名 | `farmstorm_farmland`（`FarmlandLedger.java:53`）⇒ `world/data/farmstorm_farmland.dat` |
| 现状格式 | `dataVersion: 2`（`:55`、`:34`），三张表 `bindings` / `byCore` / `orphans`（`:89,92,95`） |
| v0.2.0 | **既不读、也不写、也不删除**（D-154）：`FarmlandLedger.get(...)`（`:98-101`）的运行期调用点被 §2.3 的注释点清零后，这个 `SavedData` 不再被 `get()` ⇒ 保存时不会重写文件 |
| 验收判据 | 老档升级后：文件**仍在**、**mtime 不变**、内容**逐字节不变**；`latest.log` 里 0 条 `farmstorm_farmland` 读写相关记录 |
| 为什么不能删 | 删了就没有回头路；「本版不读」是暂时不用，将来若要恢复这套体系或写迁移工具，原始数据仍在（D-154 补充） |

### ② `farmstorm_outpost_core.dat`（索引）：条目不变

| 项 | 内容 |
|---|---|
| 类 | `outpost/OutpostCoreIndex.java`，`DATA_NAME = "farmstorm_outpost_core"`（`:50`） |
| 条目格式 | `record Entry(ResourceLocation dimension, BlockPos pos, long gameTime)`（`:67`），落盘 `owners → {uuid → {name, entries[]}}` |
| v0.2.0 | **格式与语义都不变**：本版没有给索引加任何字段（16 条培养器绑定长在核 BE 上，不在索引里） |
| 验收判据 | 升级后索引条目数与坐标集合不变；浮窗列表（`core_list`）显示的核数与升级前一致 |

### ③ 核心 BE 的 NBT：在**区块存档**里，结构大变

| 项 | 内容 |
|---|---|
| 位置 | **区块存档**（region 文件里的 BE `saveAdditional`），**不在** ② 那个 `.dat` 里 |
| 现状 | 3 个键：`ownerUuid` / `ownerName`（`DeployedOwnership.java:18-19`）+ `placedGameTime`（`OutpostCoreBlockEntity.java:50`） |
| v0.2.0 | **+`Items`（54 格，`ContainerHelper` 的 list）+`Cultivators`（≤16 条 Long）** |
| 兼容读 | 缺 `Items` ⇒ 54 个空槽；缺 `Cultivators` ⇒ 空列表；三键存在 ⇒ 照旧赋值（§2.6.2） |
| 同步面 | `getUpdateTag` **只写三键**（不写物品、不写绑定）⇒ 网络体积与 0.1.1 持平（§2.6.1） |
| 验收判据 | 老档放一个核心 → 打开是 54 个空槽、不报错；存入 3 个物品 → 重启后仍在；`/data get block <pos>` 能看到 `Items` 与 `Cultivators` 两个键 |

### ④ 新增 `cultivator` BE 的 NBT

| 键 | 类型 | 缺省含义 | 备注 |
|---|---|---|---|
| `Held` | `CompoundTag`（`ItemStack#save`） | 空槽 | 内部最多 1 个物品 |
| `Progress` | `int`（刻） | 0 | 取值 `0..cycleTicks`；暂停时停在 `cycleTicks` |
| `Core` | `long`（`BlockPos#asLong`） | **缺键 = 未绑定** | 引用缓存；唯一真相在核侧，使用前校验（D-165） |

**回滚方向**：v0.2.0 的存档退回 0.1.1 时，`cultivator` 方块与其 BE 会变成「未知方块 / 未知 BE」而被原版丢弃；`outpost_core` 的 `Items` / `Cultivators` 两个键会被 0.1.1 的 `loadAdditional` 忽略（它只读自己那 3 个键）。**不写降级代码**：未知方块的处理归原版，本版不接管。

---

## 7. 风险册 RT-42 ~ RT-50（逐条细化到「怎么验」）

| 编号 | 风险 | 触发条件 | 影响 | 缓释 | 由哪个验收项覆盖 |
|---|---|---|---|---|---|
| **RT-42** | 撤销耕地是「注释调用点」而非删代码：两个类仍在，误恢复即复活旧规则 | ① 有人把 `:75`/`:76` 的注释当误注释恢复；② 有人「顺手清理」两个无调用者的类 | ① 锄地重新被拦、踩踏重新被保护、启动补建重新跑，玩家看到 0.1.1 的旧行为；② 删类后将来无法恢复，且 `FarmstormConfig.java:227` 的 `null` 字段会让恢复监听器的第一锄直接 NPE | 三处防线：(a) 每处注释上方一行理由「裁定 #3：v0.2.0 撤销耕地，勿恢复」；(b) `FarmstormConfig.java:227` 额外写「本键定义已注释 ⇒ 字段为 null，恢复监听器前必须先恢复定义」（§2.3）；(c) `FarmlandLedger` / `FarmlandGuard` 类注释顶部各加一行「v0.2.0 无运行期调用者，刻意保留（D-153 / RT-42）」 | §8 #9 ~ #14 |
| **RT-43** | `settleOnRemoval` 整段改写，三出口语义重定义 | 把「找不到接替核」写成「变泥土」或「打掉培养器」 | 培养器与内部作物凭空消失（数据丢失）；或 `setBlock(DIRT)` 把培养器换成泥土 | (a) 三出口写死成 §2.7 的表，**没有第四支**；(b) 「不动世界」写成注释里的硬约束；(c) 区块取不到时一律走出口③（不猜测） | §8 #27 ~ #30 |
| **RT-44** | 核心 BE 加 `Container` + 16 绑定 + 54 格，NBT 体积与同步要重算；老档兼容读 | ① 54 个 `ItemStack` 全量进 `getUpdateTag`；② 老档缺 `Items` 时读成「满格」或崩；③ 菜单槽数与 `getContainerSize()` 不一致 | ① 每次区块送到客户端多几 KB；② 老档进不去 / 报错；③ 打开界面立刻崩（`IndexOutOfBoundsException`） | (a) `getUpdateTag` 只写三键（§2.6.1）；(b) 老档三键兼容表（§2.6.2）；(c) `storageSlots` 钉死 54、菜单只在创建时读一次；(d) 槽位数与 `getContainerSize()` 共用常量 `SLOT_COUNT = 54` | §8 #21 ~ #26 |
| **RT-45** | 线框渲染全项目第一次（0 处 `RenderType.lines` 先例） | ① `POSITION_COLOR_NORMAL` 顶点缺元素 ⇒ `Missing elements in vertex` 崩溃；② 阶段选错 ⇒ 线看不见；③ 每 20 刻全量重扫 × 在线人数 | ① 客户端崩；② 线不可见或闪烁；③ 服务端 tick 抖动 | (a) 用 `LevelRenderer.renderLineBox` 而不是手写顶点（§2.8.3）；(b) 阶段选 `AFTER_PARTICLES`；(c) 去抖 10 刻 + 重扫 20 刻；(d) 客户端只读、不推导 | §8 #31 ~ #34 |
| **RT-46** | 村民交易全项目第一次（0 处 villager 代码） | ① 事件签名 / 总线搞错；② 等级位次写错（写进 2 / 4）；③ 价格随机在成交时重掷；④ **老档满级农民拿不到新项** | ① 三个交易项一个都不出现；② 项出现在错的等级，玩家看不到；③ 同一笔报价点一次一个价；④ 老存档玩家以为「更新了但没内容」 | (a) 签名已核（§2.4 表，`neoforge-21.1.213-sources.jar`）；(b) 位次 1 / 3 / 5 由 `VillagerData.java:13-15` 的 1~5 定；(c) 抽签与定价都在 `getOffer` 里一次完成；(d) ④ **不做迁移**（不写补发逻辑、不改 `Offers` NBT），写进玩家可见说明（§10 第 16 条） | §8 #15 ~ #17 |
| **RT-47** | 动画贴图全项目第一次（仓库 0 个模组 `.png.mcmeta`） | ① mcmeta 与 PNG 不在一起 / JSON 写错 ⇒ 贴图裂成 missing texture；② top 只有 9 帧不同 ⇒ 观感像卡顿；③ 空闲态忘了换静态图 ⇒ 空闲也在动 | ① 方块紫黑；② 观感瑕疵（非 bug）；③ 违反 D-161 | (a) mcmeta 与 PNG 同名同目录；(b) 两份 mcmeta 原样拷入、一字不改；(c) 双模型 + `working` 属性（§2.5.5）；(d) ② 登记为已知观感（34 号 §8 第 2 条），不修 | §8 #18 ~ #20 |
| **RT-48** | 切断种植有两条路径，只堵一条会漏 | 只改 `isPlantableSoil`，7 个独立种子仍能在耕地上种出作物 | 玩家仍能种出 7 种作物 ⇒ 入口控制失效 | 两条路径各一个改动点（§2.2 的 ① 与 ②），验收**分开两条**、不合并 | §8 #6 ~ #8 |
| **RT-49** | 品质分级是新概念，22 条 tier 必须与 35 号 §2.2 逐条一致 | 错 1 条（例如把 `power_beet` 写成普通） | 同时影响交易抽样池、培养器准入、幸运浆果池三处 | (a) 22 条只在 `CropProfile` 一处定义、三处都读同一个 `tier()`；(b) 编译期防线：record 加字段后 10 个构造点缺参即编译失败（§2.1）；(c) 启动自检加一条「普通 11 / 稀有 7 / 史诗 4」（照 `Farmstorm.java:112-115` 的既有形态） | §8 #3 ~ #5、#35 |
| **RT-50** | 池 19 → 11，`Farmstorm.java:119` 的断言会红 | 改池不改断言 | 每次启动打一条 `自检 幸运浆果池 … → FAIL`；服务端**照常启动**（`poolOk` 只影响日志文案，`:118-123`），真故障会被这条常红噪声盖住 | 池与断言**同一个提交**改；保留「不含 `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`」的另半条 | §8 #36 |

**九条的共同点**（写给 37 / 38 号）：每条的缓释都是「**一处定义 + 一处注释 + 一条独立验收**」，没有一条靠「记得小心」。RT-42 / RT-43 / RT-48 / RT-50 的红线都落在**具体行号**上，验收可以逐行 grep。

---

## 8. 验收自检清单（36 条）

每条给：编号 / 断言原文 / 观察位置 / 依据决策号。**「观察位置」是可操作的**：`V-xxx` 指本文自定的观察点，不引入第二个编号系列。

| # | 断言原文 | 观察位置 | 依据 |
|---|---|---|---|
| 1 | 服务端启动日志出现 `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK` | `run/logs/latest.log`（`Farmstorm.java:151-155` 的格式串） | D-171 |
| 2 | 全仓 `@Mixin` 命中 **0**、`*.mixins.json` 文件 **0** 个；`FarmstormNetwork.onRegisterPayloads` 里 `.playToClient` 恰 **2** 处，包 id 分别是 `farmstorm:core_list` 与 `farmstorm:cultivator_link` | 源码 grep + 启动日志的 `自检 网络包注册` 两行 | D-172、D-169 |
| 3 | 22 条作物档案的 `tier()` 逐条等于 35 号 §2.2 的表：普通 11（`puffed_wheat` … `vibration_bean`）/ 稀有 7 / 史诗 4 | `CropProfile.java:47-128`；`/data` 不可读，用启动自检行 | D-149 |
| 4 | 启动自检新增一行 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` | `latest.log` | D-149、RT-49 |
| 5 | 由分级派生的两个集合：准入面内「产 2 + 2」**5** 条、「只产 2」**13** 条（`5 + 13 = 18`） | 自检行 + 35 号 §6.4 的点名表 | D-163 |
| 6 | 手持 22 个作物之一右键**耕地**：方块不出现、手持物不减、无提示 | 手动（或 `V-01`） | D-152 ① |
| 7 | 手持 7 个种子之一右键**耕地**：同上（方块不出现、种子不减） | 手动（`V-02`） | D-152 ② |
| 8 | 手持 7 个部署型作物物品之一（`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` / `zangief_cane` / `point_defense_fungus`）右键**非耕地**的完整方块顶面：方块正常放置（证明只切断了种植路径） | 手动（`V-03`） | D-152、D-155 |
| 9 | `FarmstormGameEvents.java` 里 `onRightClickBlock` / `onFarmlandTrample` 的**注册行**（`:75`、`:76`）与两个方法体（`:241-265`、`:277-281`）全部处于注释态；且 `:171` 的 `FarmlandLedger.get(server).backfill(server)` 单行注释 | 源码 grep：`^\\s*//.*onRightClickBlock` 等 5 处命中 | D-153 |
| 10 | `onServerTickPost` **仍在**（未被注释），方法体里 `CoreChunkTickets.restoreAll(server)`（`:170`）与 `CoreListBroadcaster.tick(server)`（`:175`）都在，且 `CoreListBroadcaster.tick` 每刻被执行 | 源码 + 浮窗每 100 刻的对账日志仍在 | D-153、RT-42 |
| 11 | `farmstorm-server.toml` 里 `outpost_core` 段**没有** `farmlandBonus` 键，有 `cultivatorCapacity = 16` 与 `storageSlots = 54` | `run/config/farmstorm-server.toml` | D-153、D-165、D-166 |
| 12 | `FarmstormConfig.java:227` 的字段声明**仍在**、`:909-912` 的定义**已注释**，且注释里写着「恢复监听器前必须先恢复定义，否则 NPE」 | 源码 | D-153、§2.3 |
| 13 | 语言文件里 3 条耕地键**都不存在**：`farmstorm.configuration.outpost_core.farmlandBonus`、`message.farmstorm.farmland.no_core`、`message.farmstorm.farmland.quota_full` | `zh_cn.json` / `en_us.json`（`ConvertFrom-Json` 后查键集） | D-153 |
| 14 | `FarmlandLedger`（555 行）/ `FarmlandGuard`（225 行）两个类仍在源码树、行数不变、能编译；`farmstorm_farmland.dat` 在升级后的存档里**仍存在且 mtime 不变** | 源码 + `world/data/` 目录 | D-153、D-154 |
| 15 | 农民的交易列表里出现 **3** 个本模组交易项，等级位次分别是 **1 / 3 / 5**；每项的 `maxUses = 1` | 游戏内打开农民交易界面；`/data get entity <villager> Offers.Recipes` 看 `maxUses` | D-157、D-158 |
| 16 | 成交一次后该项**仍显示**、变灰不可点（不是消失、不是换 id）；价格与作物 id 在补货后**不变** | 游戏内连续两次观察 | D-158 |
| 17 | 首次报价的**基准价**落区间：普通 ∈ [2,5]、稀有 ∈ [10,32]、史诗 ∈ [40,64]（`offer.getItemCostA().count()`） | `/data get entity <villager> Offers.Recipes[0].buy` 的 `count` | D-159 |
| 18 | 培养器的方块状态有两态：`working=false` 用 `farmstorm:block/cultivator`（引用 `_still` 两张）、`working=true` 用 `farmstorm:block/cultivator_working`（引用帧条两张） | `blockstates/cultivator.json` + 两个模型 json + `F3` 的 targeted block state | D-161 |
| 19 | 资源里 `cultivator_a_*` 的 `*.png.mcmeta` 恰 **2** 个（side / top），内容为 `frametime 2` / `interpolate false` / `frames 0..15`；`_still` 两张**没有** mcmeta | 文件系统 | D-161、RT-47 |
| 20 | 空槽时方块**不动**（静态首帧）；放入普通作物后开始动（1.6 秒一轮）；600 刻后停止并清槽 | 游戏内观察 + `V-04`（时间戳） | D-161、D-163 |
| 21 | 培养器放入普通作物：600 刻后核的 54 格里多 **2** 个该作物；若该作物有独立种子（5 条之一）再多 **2** 个种子 | 游戏内 + `/data get block <core>` 的 `Items` | D-163 |
| 22 | 核的 54 格**塞满**后：培养器停在完成态（`progress == cycleTicks`）、物品**不落地、不丢**；清出 2 格后自动完成投递 | 游戏内 + `/data get block <cultivator>` 的 `Progress` | D-164 |
| 23 | 手持史诗作物（`lucky_berry` / `wild_apple` / `awakened_*` 2 条）或 2 个史诗种子右键培养器：**无反应**、手持物不减 | 手动（`V-05`） | D-162 |
| 24 | 己方空手右键核：打开 **54 格**界面（6 行 × 9 列 + 玩家背包），标题为「据点核心」；存入 3 个物品后关掉再开，物品仍在 | 游戏内（`V-06`） | D-166 |
| 25 | 核心存储的**两道闸**：① 非己方右键核 → **不开界面**（`useWithoutItem` 返回 `PASS`）；② 己方在离核 **9 格**处右键 → 客户端发不出交互、界面不开（原版交互距离），己方在 **8 格**内开界面后走到 9 格 → 界面被原版 `stillValid` 关掉 | 双人客户端（`V-07`） | D-166 补充、裁定 #17 |
| 26 | 挖掉核心：54 格里的**全部物品掉落**；活塞推动 / 同方块替换 / 区块卸载**都不掉** | 游戏内四种场景（`V-08`） | D-167 |
| 27 | 拆核时场上有另一个「覆盖该培养器 + 己方 + 有空位」的核：培养器改绑成功，槽内物品与进度**保留** | 游戏内 + `/data get block <cultivator>` 的 `Core` | D-168 出口① |
| 28 | 拆核时没有接替核：培养器**留在原地**、`Core` 键消失、`Held` / `Progress` 保留、右键无反应 | 游戏内 + `/data get block` | D-168 出口② |
| 29 | 拆核时培养器所在区块**不在内存**：培养器的 NBT **一个字节都不变**（`Core` 仍是旧核的坐标） | 停服后读 region 文件比对（`V-09`） | D-168 出口③ |
| 30 | `settleOnRemoval` 方法体内 `setBlock` 命中数 = **0**（0.1.1 的 `:387` 已随改写消失） | 源码 grep（方法体范围内） | D-168、RT-43 |
| 31 | 玩家站在自家核旁（3×3 区块内）：核与每个已绑定培养器之间有**可见线框**；走出范围后线**消失**（客户端列表被清空） | 游戏内（`V-10`） | D-169 |
| 32 | 拆掉培养器：对应的线立刻消失（不需要等 20 刻重扫） | 游戏内 | D-169 时机 #3 |
| 33 | 客户端画线不崩：连续 10 分钟在线、`Latest.log` 里 0 条 `Missing elements in vertex` / `IllegalStateException` | 客户端日志 | RT-45 |
| 34 | 服务端逻辑**不依赖**连线：把 `ClientCultivatorLinks` 的 handler 临时改成空实现后，绑定 / 产出 / 迁移 / 拆核掉落**全部照常** | 断点式验证（`V-11`） | D-169 补充 |
| 35 | 启动自检的池断言为 `poolSize == 11` 且日志 `→ OK`；同时「不含 `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`」的另半条仍在 | `latest.log` | D-156、RT-50 |
| 36 | 手 持种子右键**转换器**：无界面、无提示；但转换器的**方块仍在**、**创造栏里拿得到**、**26 条 `converting` 配方 json 仍在**（`data/farmstorm/recipe/` 计数 = 29） | 游戏内 + 资源目录计数 | D-170 |

**36 条的分布**：注册与红线 2 条 / 品质 3 条 / 种植 3 条 / 耕地 6 条 / 交易 3 条 / 培养器 5 条 / 核心 5 条 / 迁移 4 条 / 连线 4 条 / 收口 1 条 = 36。

---

## 9. 待验证 API 清单（13 条）

**判定口径**：一条 API 只有满足「类型 / 方法签名 / 调用点参数个数三者都在本机可读的源码或反编译源码里逐字看到」才算「已核」。本节列的是**本机可读源码里也没有直接先例**、或**只有签名没有用法先例**的 13 条。

| # | API 或语义 | 为什么不确定 | 由哪个任务确认 | 退路 |
|---|---|---|---|---|
| ① | `VillagerTradesEvent` 的签名与注册总线 | **已核**（`neoforge-21.1.213-sources.jar` 内 `net/neoforged/neoforge/event/village/VillagerTradesEvent.java`：`getTrades()` / `getType()` / `getRegistryAccess()`，`NeoForge#EVENT_BUS`，键 1~5 恒非 null）。**仍不确定的是**：本模组第一次在 game 总线上挂一个「重载期触发」的监听器，事件在 `TagsUpdatedEvent` 里触发 ⇒ 与 datapack 重载的先后顺序、以及 `Int2ObjectMap` 里的 `List` 是否可变，本机没有运行期证据 | 实现任务（交易注册） | 若 `getTrades().get(1)` 的 list 不可变 ⇒ 改为 `event.getTrades().put(1, new ArrayList<>(旧 + 新))` |
| ② | `RenderType.lines()` / `VertexConsumer` 画线的可用写法 | **已核签名**：`RenderType.lines()`（`RenderType.java:1063-1065`）、`LevelRenderer.renderLineBox(PoseStack, VertexConsumer, AABB, float×4)`（`:2252-2253`）、1.21.1 的顶点链是 `addVertex(...).setColor(...).setNormal(...)`（`VertexConsumer.java:16,18,26`）。**仍不确定的是运行期行为**：`AFTER_PARTICLES` 阶段能否拿到可用的 `bufferSource`、线宽固定 1.0 的实际观感、`VIEW_OFFSET_Z_LAYERING` 会不会与光影类模组冲突 | 渲染任务（连线客户端） | 若 `AFTER_PARTICLES` 不可用 ⇒ 退到 `AFTER_TRIPWIRE_BLOCKS`；若 `bufferSource` 已被 flush ⇒ 改为在 `AFTER_ENTITIES` 里捕获矩阵、在 `RenderGuiEvent` 阶段重画（`WildHunterOutline.java:66-74` 的既有手法） |
| ③ | `mcmeta` 动画与静态帧双贴图在 blockstate 层的表达 | 仓库内 **0 个**模组 `*.png.mcmeta`、**0 个**纵向帧条，全项目第一次：① 两张 PNG 同名同目录放 mcmeta 是否足够；② 同一张贴图被两个模型引用时动画是否各自计时；③ `working` 属性切换模型的那一帧会不会闪一帧默认图 | 资源任务（datagen + 材质拷入） | 若切换有闪帧 ⇒ 两个模型的 `textures` 全用静态图，工作态单独用一张「只含 16 帧中的第 0 帧」的过渡图；若动画只在 `working=true` 生效不成 ⇒ 改为两个**独立方块**（本版不做，需回 34 号） |
| ④ | 核心 BE 加 `Container` 后的 NBT 兼容与 `getUpdateTag` 体积 | NBT 兼容有先例（`ConverterBlockEntity.java:407-416` 的缺键当空），但**没有**「从非容器升级成容器」的先例；`getUpdateTag` 不带 `Items` 时客户端 BE 内容为空这一取舍，也第一次出现在本模组 | 核心容器任务 | 若客户端某处（例如比较器 / 渲染）确实需要物品 ⇒ 只把「非空槽数与首槽物品」放进 `getUpdateTag`（≤ 200 字节），仍不搬 54 格 |
| ⑤ | 培养器「暂停产出」的检测时机 | 34 号只定「核满则暂停」，**没定**在哪一刻判。**本文确定**为「完成时判一次」（§2.5.2 第 4 步）：每刻判要扫核的 54 格 × 16 个培养器 = 864 次/刻；每 20 刻判会让「暂停 → 恢复」最多晚 1 秒 | 培养器任务 | 若实测「恢复延迟 1 秒」不可接受 ⇒ 在核的 `setItem` / `removeItem` 里加一次「广播给绑定培养器」的回调（不动判定时机，只提前唤醒） |
| ⑥ | `CropTier` 名色的实现方式（`Rarity` / tooltip 事件 / 自定义） | **已定**：覆写 `Item#getName(ItemStack)`（`Item.java:299-300`）+ `withStyle`。**仍不确定的是**：① 铁砧改名后的 `CUSTOM_NAME` 是否压过我们的颜色（`getHoverName` `:731-739` 优先取 `CUSTOM_NAME`）⇒ 改名物品会**丢掉**档色；② `tooltip` 首行的 rarity 包装（`ItemStack.java:755`）不会覆盖子组件颜色这一点只有源码推断、无运行期截图 | 品质视觉任务 | ① 若要求「改名后仍保档色」⇒ 只能在 tooltip 事件里再包一层（`ItemTooltipEvent`），代价是多一个事件挂载；② 若 rarity 包装确实覆盖 ⇒ 在 `getName` 里返回 `Component.empty().append(带色文本)`（多一层空壳可保住子样式） |
| ⑦ | `ModConfigSpec` 接受 `min == max` 的退化区间（`storageSlots` 54–54） | 本机没有 `storageSlots` 这类先例（既有 166 键全是 `min < max`）；`defineInRange` 的边界校验只在运行期首次建 spec 时暴露 | 配置任务 | 改 `54..55`，并在创建菜单时 `Math.min(54, 读值)` 钳到 54（口径不变，只是多一行钳制） |
| ⑧ | `AbstractContainerMenu#quickMoveStack` 在 54 + 36 槽下的边界 | 先例 `ConverterMenu` 是 3 + 36 槽（`:121-216`），54 + 36 的区间划分第一次写 | 核心容器任务 | 照原版 `ChestMenu` 的两段区间（0–53 / 54–89）写，槽位合法性由 `Slot#mayPlace` 兜底 |
| ⑨ | 培养器 `useWithoutItem` 在**手持方块**时的可达性 | `ServerPlayerGameMode.java:363` → `:370` 的顺序说明「物品交互未消费时才回调」；「手持方块右键培养器」会先走 `BlockItem` 放置 ⇒ 培养器**不接**这一下（合理，但要在验收里写明） | 培养器任务 | 无（这是原版顺序，不改）；把「手持方块想放入」写进玩家可见说明（35 号 §6.7） |
| ⑩ | `Containers.dropContents(Level, BlockPos, NonNullList<ItemStack>)` 与培养器的配合 | `Containers` 的三个重载都在（`Containers.java:13,17,27`），但「BE 不是 Container、只掉一格」的用法第一次出现在本模组 | 培养器任务 | 若 `NonNullList` 重载的掉落位置不佳 ⇒ 改用 `Containers.dropItemStack(...)`（`:31`）逐件掉 |
| ⑪ | `MerchantOffer` 的 `priceMultiplier = 0.0F` 对原版折扣的影响 | 本文已选定「锁死基准价」（§2.4.1）：源码可读（`MerchantOffer.java:107-111`：`floor(i × demand × multiplier)` 与 `specialPriceDiff` 是两个独立加法项），但**没有运行期证据**证明 0.0F 不会被原版某处当异常值，也没有证据证明声望折扣的实际扣减幅度 | 交易任务 | 若 0.0F 有副作用 ⇒ 改用原版最常见的 `0.05F`（`VillagerTrades.java:132` 等处），代价是成交后基准价可能 +1（区间上界仍由 `clamp(...,1,maxStack)` 兜住） |
| ⑫ | 培养器与核的 `setBlockUpdated` 触发点是否足够让浮窗/连线刷新 | 现有 `ConverterBlockEntity` 用 `sendBlockUpdated` 只做「转换完成」一件事（`:261`），没有「绑定变化」这类先例 | 核心 + 连线任务 | 若客户端没刷新 ⇒ 在绑定变化的三个时点额外调一次 `CoreListBroadcaster.requestAll(server)`（已有方法） |
| ⑬ | `Farmstorm.java:151-155` 的自检格式串在 97 之后是否仍对齐 | 该行是 `{}` 占位符手工拼的 13 个参数，改值不改串即可；但**新增一行「品质自检」**会改变日志行数，验收若按行号取会错位 | 收口任务 | 验收按**内容 grep**（`自检 注册对象 97 个`）而不是按行号 |

**三条「已从待验证降级为已核」的记录**（写在这里，免得 37 / 38 号再当风险）：`VillagerTradesEvent` 签名（①的前半）、`RenderType.lines` / `LevelRenderer.renderLineBox` 签名（②的前半）、`ContainerHelper` 的 `Items` 键与 `saveAllItems` / `loadAllItems`（④的前半）——三者都在本机的 `neoforge-21.1.213-sources.jar` 与 `sourcesAndCompiledWithNeoForge_…_output.jar` 里逐字读到，并已在正文给出 `文件:行号`。

---

## 10. 与上游的差异登记（16 条，**不自行改上游**）

按「34 号的条款 / `BRIEF` 的条款 / 30 与 35 的条款 / 缺失项」四类排。**本文只登记，不改任何上游文件。**

| # | 涉及 | 上游写的 | 本文的处理 | 性质 |
|---|---|---|---|---|
| 1 | 30 号 §1.4 的「59 组」 vs 本文实测 | 30 号 §1.4 写「现状 166 键 / **59 组**（35 个顶层组 + 24 个嵌套组）」 | **读法澄清，不是偏差**：30 号的「现状」一律指**改前基准**（0.1.0，`farmland` 组尚在 ⇒ 59 组）；同文件 §4.1（`:968`）明写「本表是**改后**的 58 组：`farmland` 组整组消失（−1 组）」，合计行 `:1032` 与 `:1063` 也都是 58。本文实测 58 组（34 + 24）与 30 号**改后**值、`BRIEF` §0 的 58 三者一致 | 口径一致（30 号冻结不改） |
| 2 | 34 号 §9 ⑤ vs `BRIEF` §3.5 | 34 号说 `BRIEF` §3.5 只记了「3 PNG + 2 mcmeta」 | 现行 `BRIEF` §3.5（`:217`）**已经**写成「新增 PNG 5 张 + mcmeta 2 个」并给了双贴图理由 ⇒ 34 号该条已被上游改稿吸收，本文按 5 + 2 写 | 34 号回报已过期 |
| 3 | 34 号 §9 ⑥ vs `BRIEF` §3.3 第 13 行 | 34 号说 `BRIEF` 写「`cultivator` 方块 + BE + item + **菜单**」，与 97 冲突 | 现行 `BRIEF` §3.3 第 13 行**已经**写「（**无菜单**：右键直接放入，不开界面）」⇒ 冲突已不存在，但「培养器无菜单」这条口径仍是 97 的前提，本文保留 | 34 号回报已过期 |
| 4 | 34 号 §9 ⑨ vs `BRIEF` §3.6 | 34 号说 `BRIEF` 把核心 BE 的 NBT 位置写成了索引 `.dat` | 现行 `BRIEF` §3.6 已把「索引 `.dat` 不变」与「核心 BE 的 NBT 在区块存档、结构大变」**分成两条** ⇒ 已修正；本文 §6 仍按三条分开写 | 34 号回报已过期 |
| 5 | 34 号 §9 ⑧ vs `BRIEF` §2 裁定 #15 | 34 号说裁定 #15 只覆盖「有独立种子 / 无种子」两端，中间 8 条没写 | 现行 `BRIEF` §2 裁定 #15 已含「22 个作物实为三类（7 / 8 / 7），准入面内 5 给 2+2、13 只给 2」⇒ 口径一致 | 34 号回报已过期 |
| 6 | 34 号 §2 备注 3 vs `BRIEF` §2 裁定 #3 | 34 号说「裁定 #3 的『3 个监听器』是事实卡的表述，实际是 2 + 1」 | 现行 `BRIEF` §2 裁定 #3 已直接写「注释掉 **2 个监听器** 与 **1 行 backfill**；`onServerTickPost` 必须保留」⇒ 两份文档都指 2 + 1，34 号的措辞会让人以为事实卡还写着 3 | 表述失真（不影响结论） |
| 7 | **D-153 内部矛盾（实质）** | D-153 要求注释「`outpost_core.farmlandBonus` 的**读取**」，同时又要求 `FarmlandGuard` / `FarmlandLedger`「本体一行不改」 | 矛盾：两处读取就在这两个类里（`FarmlandGuard.java:75`、`FarmlandLedger.java:305`）。**已由作者裁定为 A-62**（见本节末）：只注释**定义**（`FarmstormConfig.java:909-912`）、**字段声明 `:227` 必须保留**、两个类的读取点原样保留（运行期不可达）。代价：字段为 `null`，误恢复监听器会 NPE，已写进注释理由与 RT-42 | **已裁定（A-62）** |
| 8 | 34 号 §9 ⑪ | 交易等级的**位次未定**，交 36 号按等级表核 | 本文已核：`VillagerData` 的等级是 **1~5**（`VillagerData.java:13-15`，含经验阈值 `{0,10,70,150,250}`）⇒ 位次定为 **1 / 3 / 5**（§2.4） | 34 号留下的活，本文已办 |
| 9 | D-151 的括注 | 「三个颜色**借用原版稀有度**的白 / 蓝 / 金」 | 原版 `Rarity` 是 白 / **黄** / **青** / **紫**（`Rarity.java:16-19`），**没有金**。本文按白 / 蓝 / 金取 `ChatFormatting.WHITE / BLUE / GOLD`；该失真反而加强了「不用 `Rarity`」的理由 | 表述失真（不影响结论） |
| 10 | `BRIEF` 未覆盖（缺失） | §3.3 第 19 行只说「新增 item 模型走 datagen」 | `ModItemModelProvider.usesBlockModel`（`:57-59`）只认 `ConverterBlock \|\| OutpostCoreBlock`；不加 `CultivatorBlock` 会落到 `basicItem` 分支、要求一张不存在的 `item/cultivator.png`。本文把它写成必改项（§5） | **缺失，会直接翻车** |
| 11 | `BRIEF` 未覆盖（缺失） | D-171 只说「`Farmstorm.java:147-150` 的断言 93 → 97、逐项 +」 | 没给逐项旧值。本文补了逐项旧 → 新表（`total 93→97`、`items 32→33`、`blocks 26→27`、`blockEntities 7→8`、`menuTypes 1→2`，其余 8 类不变）（§1.2） | 缺失（照抄会漏项） |
| 12 | **【已撤回】本文初稿的「30 号三处数字过期」** | 初稿曾在此登记三条：① 30 号 §1.4 的「59 组」多算 1；② 30 号 §1.4 的「315 语言键」过期；③ 30 号说「`farmstorm.configuration` 166 与 server config 1:1」过期 | **三条全部撤回**，逐行复核后 30 号自洽：① 见第 1 行，「59」是 0.1.0 基准、同文件 §4.1 的 58 是改后；② `:117` 的「315 × 2」同样是改前值，`:1092` 的算式 `315 − 2 + 10 = 323` 已落到 **323**，`:1318` 还专门解释了「算式漏了 2 条按键描述键，加上后 = 323」；③ `:1116` 明确写「`farmland.limitPerPlayer` 的删除让 `farmstorm.configuration` 从 166 → 165，再加 `bulletSpeed` 与 3 个 `hud.*` 回到 **169**——169 = 166 + 3」，`:1254` 的验收项也按 169 断言。结论：**30 号没有过期，是本文初稿把「改前基准」误读成了「现状值」** | **误读，已撤回**（30 号冻结不改；本文 §1.3 / §4.1 的实测与 30 号 §4.1、§4.3 一致） |
| 13 | 35 号 §8.3 vs 34 号 D-166 vs **裁定 #17** | 35 号写明「存储访问判据只有**一道**（己方），没有范围闸」，并在 §12 B-24 留了「若作者要加范围闸」的接口 | `BRIEF` §2 新增的**裁定 #17** 把范围闸加回来了（己方 + 玩家触及范围内），且规定「不新设自定义常量、直接用原版 `stillValid` 语义」⇒ 35 号 §8.3 那段 ⚠️ 与 B-24 **已过期**。本文按两道闸写（§2.6.4），范围取原版 `Container.stillValidBlockEntity` 的 `4.0F`（`Container.java:81-82`），**不是** 3×3 区块 | **上游已变，35 号需回改** |
| 14 | 34 号 §3.4 的「段数」措辞 | 34 号写「段数 `58 + 2 = 60`」 | 本文按「组 = `b.push` 次数」与「段 = toml 的 `[section]` 行」同数处理（58 → 60），并在 §4.1.2 明确「`outpost_core` 是加键、不是删组重建」 | 术语统一（口径不变） |
| 15 | `BRIEF` §2 新增裁定 vs 34 号（缺失） | `BRIEF` §2 现有 **19 条**裁定（#17 核心存储两道闸 / #18 交易粒度保持 3 项 / #19 转换器中文名不改），34 号仍只到 16 条、D 号也只到 D-172 | 本文按 19 条写：§2.6.4（#17）、§2.4.2（#18）、§2.9（#19）。**D 编号待 37 号从 A-62 之后补登**——本文不新开 D 号（34 号 §0.1 的编号纪律） | **上游已变，34 号需回改** |
| 16 | 35 号 §4.6 / `BRIEF` 未覆盖的老档迁移（缺失） | 「老存档满级农民不会补发交易项」只被 35 号登记为玩家可见后果，没人说清楚**本版做不做迁移** | 本文明确：**不做迁移**（不写补发逻辑、不改村民 `Offers` NBT、不做数据修复），与 D-158「不写第二套补货逻辑」同一条纪律；已写进 §7 RT-46 与 §2.4.2 的 ⚠️ | **缺失，需 35 / 38 号在玩家说明里落一条** |

**两条给 37 号的提示**：

1. 第 7 条**已由作者裁定为 A-62**（正文见本节末），37 号**只需登记、不需再裁**；`175` 个键与 `97` 个注册面都不受影响——A-62 只回答了「`FarmlandGuard.java:75` / `FarmlandLedger.java:305` 要不要动 1 行」这一个问题，答案是**不动**。
2. 第 15 条要补的是**编号**、第 16 条要补的是**玩家说明**：裁定 #17~#19 需要一个 D 号（本文按纪律不自己开），而「老档满级农民不补发」需要在升级说明里写一句「本版不迁移，请用新村民」。前者属 37 号（A-62 之后的裁决登记），后者属 35 号修订 / 38 号 HC 段。

---

### A-62 · 裁决：`outpost_core.farmlandBonus` 只注释定义、保留字段声明

| 项 | 内容 |
|---|---|
| 编号 | **A-62**（写作期当场裁定；接续 0.1.1 的 A-61；本文按 34 号 §0.1 的「不外发编号」纪律不新开 D 号） |
| 结论 | 注释 `config/FarmstormConfig.java` 里 `outpost_core.farmlandBonus` 的**定义**（`:909-912`），**保留字段声明**（`:227`）。 |
| 理由 | D-153 同时要求「注释该键的读取」与「`FarmlandGuard` / `FarmlandLedger` 本体一行不改」，而两处读取就在这两个类里（`FarmlandGuard.java:75`、`FarmlandLedger.java:305`）——两者不可兼得；保留声明是让两个类仍能编译的唯一做法。 |
| 代价 | 该字段恒为 `null`；若有人误恢复那两个监听器（`FarmstormGameEvents.java:75` / `:76`）会抛 **`NullPointerException`**。 |
| 缓释 | RT-42 的三处防线：`:227` 与 `:909` 两处注释都写明「恢复监听器前必须先恢复本定义，否则 `FarmlandGuard:75` 抛 NPE」；`FarmlandLedger` / `FarmlandGuard` 类注释顶部各加一行「v0.2.0 无运行期调用者，刻意保留（D-153 / RT-42）」。 |
| 否决 | ① **把两个类也改**（违反 D-153「本体一行不改」）；② **把字段改成硬编码常量 16**（等于偷偷保留耕地额度，与「撤销耕地」正面冲突）；③ **删掉两个类**（34 号 §7 墓碑 1：删了就没有回头路）。 |
| 影响面 | **零**：`175` 个 config 键与 `97` 个注册对象都不受影响；受影响的只有「那两个类里的两处 `.get()` 要不要动 1 行」，A-62 的答案是「不动」。 |
| 与本文 §2.3 的关系 | §2.3 的注释清单第 6 行与「三处必须保留」就是按 A-62 写的；两处文档口径一致，改一处必须同时改另一处。 |

---

## 附录 A　本文确定的 13 项参数（速查，正文逐项有理由）

| # | 参数 | 值 | 正文 |
|---|---|---|---|
| 1 | `CropTier` 值 / `translationKey()` | `COMMON`/`RARE`/`EPIC`；`tooltip.farmstorm.tier.<小写名>` | §2.1 |
| 2 | 品质名色 | `ChatFormatting.WHITE` / `BLUE` / `GOLD` | §2.1 |
| 3 | tooltip 品质行 | `tooltip.farmstorm.tier`，`%s` = 档名，外壳灰 | §2.1 |
| 4 | 培养器状态属性 | `working`（`BooleanProperty`，默认 false） | §2.5.1 |
| 5 | 培养器 BE 的 NBT | `Held` / `Progress` / `Core`（缺 `Core` = 未绑定） | §2.5.2 |
| 6 | 培养器 tick 载体 | 方块 `getTicker` → BE 静态 `serverTick`（服务端唯一） | §2.5.2 |
| 7 | 核心容器 NBT | `Items`（`ContainerHelper.TAG_ITEMS`）/ `Cultivators`（Long 列表） | §2.6.1 |
| 8 | 连线 payload / 广播常量 | `farmstorm:cultivator_link`；`MAX_LINKS = 1024`；去抖 10 刻 / 重扫 20 刻 | §2.8.1、§2.8.2 |
| 9 | 连线渲染 | `RenderType.lines()`；`AFTER_PARTICLES`；RGB 0.30/0.85/0.90、alpha 1.0；线宽 1.0；端点 0.25 格框 | §2.8.3 |
| 10 | 交易等级位次 | **1 / 3 / 5** | §2.4 |
| 11 | 交易 `xp` / `priceMultiplier` | `2` / `0.0F` | §2.4 |
| 12 | 核心界面标题键与屏幕贴图 | `container.farmstorm.outpost_core`；复用原版 `generic_54.png` | §2.6.3 |
| 13 | 静态首帧贴图名 | `cultivator_a_side_still.png` / `cultivator_a_top_still.png` | §2.5.5 |
