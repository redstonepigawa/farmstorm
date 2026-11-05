# v0.2.3 事实卡（BRIEF）

> 生成时间：2026-11-05。基线 = git `79b71f1`（v0.2.2 代码已全部落地，`plain_chives` / `BotanistTrade` 均在源码里）。
> 本文件是 52~57 号六份文档的**唯一事实来源**；文档里的每个数字都要能回到本文的某一行。

---

## §0 基线与口径

### 0.1 实测基线（`tools/verify_project.py` 跑出来的，不是抄文档）

```
注册面 98 / 98      items 34 / blocks 27 / block_entity_type 8 / entity_type 13
                    mob_effect 4 / sound_event 3 / recipe_type 1 / recipe_serializer 1
                    creative_mode_tab 1 / damage_type 3 / data_component_type 1 / menu_type 2
server config       176 键（toml 实数）/ 60 次 b.push（toml 顶层节 36 个）
语言键              340 × 2（zh_cn / en_us 一致）
检查器              FAIL 0 / WARN 1（trade.common.max 系列 6 条旧路径拼写漂移，历史遗留）
```

**口径警告**：「60 组」是 `b.push()` 调用次数（48 号定的数法）；`run/config/farmstorm-server.toml` 里的顶层节是 **36** 个。两个数都对，指的是不同的东西。本文与后续文档一律写「**176 键 / 60 push**」，不再写「60 组」。

### 0.2 plan 的范围

`plan0.2.3.txt` 共 36 行、5 条编号需求：

| # | 需求 | 行 |
|---|---|---|
| 1 | 农产品转换器重做（→ 农作物转换器） | `:7-17` |
| 2 | 改良农作物不再拥有种子 | `:18-21` |
| 3 | 农作物培养器修改（判据 + 新配方） | `:22-28` |
| 4 | man瓜炮弹 300 刻后删除 | `:29-32` |
| 5 | 进度「最扎人的一集」描述改字 | `:33-36` |

`:5` 写着「除非本文档明确要求，否则不能修改 0.2.2 原有内容」。**读法**：上述 5 条算「明确要求」，其余一律冻结；为落地这 5 条而发生的**必要连带改动**（注册改名、自检数字、检查器清单）不算越界，但必须在 52 号逐条登记。

---

## §1 五条需求的现状

### 1.1 转换器：现在是一台**装饰方块**

- `ConverterBlock.java:85-90` 的 `useWithoutItem` **返回 `PASS`**——注释写明「裁定 #19 / D-170：v0.2.0 起转换器暂时移除功能，只作为装饰方块」，右键无反应、不开界面。
- 也就是说：`ConverterMenu` / `ConverterScreen` / `ConverterBlockEntity` 的整套事务逻辑**目前是不可达代码**，GUI 已经打不开。
- 现存面：`converter` 方块（`FarmstormBlocks.java:71-72`，`.strength(3.5F)`，`MapColor.STONE`）+ 方块物品（`FarmstormItems.java:112-113`）+ 方块实体（`ConverterBlockEntity` 435 行，`implements Container`）+ 菜单（`ConverterMenu` 216 行）+ 界面（`ConverterScreen` 86 行）+ 配方书分类（`ConverterRecipeBookCategory` 24 行）+ `FACING` 水平四向 + 6 面贴图 + 1 条合成配方（8 个 `#farmstorm:converter_fuel` 围 1 个工作台）。
- 3 个 config 键：`converter.xpCost` / `converter.netherStarForwardCost` / `converter.netherStarOnReverse`（`FarmstormConfig.java:19-21`、`:295-305`）。
- 9 个语言键：`container.farmstorm.converter` + `gui.farmstorm.converter.{convert,xp_cost,no_recipe,need_catalyst,not_enough_xp,forward,reverse,tooltip}`（`ModLangProvider.java:177,180-187`）。
- 3 条进度：`converter`（放置，`ModAdvancementProvider.java:82-85`）、`first_conversion`（转换一次，`:88`）、`nether_star_toll`（消耗下界之星，`:88-89` 之后）。
- 配方面：**26 条** `farmstorm:converting`——24 条 `twoWay`（双向、无催化剂）+ 2 条 `oneWayWithNetherStar`（火把花 / 瓶子草，正向、收下界之星），`ModRecipeProvider.java:67-99`、helper `:187-209`。
- `ConverterRecipe`（112 行）字段：`input / result / xpCost / catalyst / catalystCount / bidirectional / reverseXpCost`，有 `reverse()` 运行时视图（`:93-112`）。

### 1.2 种子：**7 个，且从 v0.2.0 起就不能播种**

- `puffed_wheat_seeds` / `power_beet_seeds` / `melon_grenade_seeds` / `pumpkin_turret_seeds` / `awakened_torchflower_seeds` / `awakened_pitcher_plant_pod` / `chive_seeds`（`FarmstormItems.java:91,102-109`）。
- **`SeedItem.useOn` 无条件返回 `PASS`**（`SeedItem.java:38-41`），注释：「裁定 #4 / D-152 ②：v0.2.0 起这 7 个独立种子不可播种——整条切断原版播种链」。
- ⇒ **作物方块在正常玩法里本来就拿不到**（没有 BlockItem、种子不可播种），只能靠创造 / `setblock`。**这是 0.2.2 之前的既定状态，不是 0.2.3 的新改动。**
- 引用面（7 个种子合计）：`ModAdvancementProvider.java:96-104`（`seed_cycle` 的全部 6 个判据）、`ModItemTagsProvider.java:43-48`（`#farmstorm:seeds`）、`ModLangProvider.java:88-93,103`、`ModLootTableProvider.java:71,81,84,86,89,91,105`、`ModRecipeProvider.java:86-92`、`FarmstormCropBlock.java:160`（`getBaseSeedId()`，即创造取块 / 剪刀掉落用）。
- `FarmstormCropBlock` 的构造器与 codec **都要求一个非空 `CropProfile`**（`:43-46`、`:57-60`），因此**不能**用它承载无档案的平凡作物。

### 1.3 培养器：判据要整个反过来

- `CultivatorBlockEntity.admissible`（`:138-144`）：`profile != null && profile.tier() != CropTier.EPIC`——**只收本模组带档案的物品**，原版小麦**进不去**。
- `batch()`（`:230-243`）：作物 × `cultivator.yield`，若 `hasOwnSeed`（`seedItem != null && seedItem != id`）再加速度 × yield。
- config：`cultivator.cycleTicks`（默认 600 = 30 秒）、`cultivator.yield`（默认 2）。
- **没有合成配方**（`src/generated/.../recipe/cultivator.json` 不存在），创造栏是唯一入口（`FarmstormItems.java:121-123` 注释 A-64）。
- 工作态用 `WORKING` 布尔属性 + 两个模型（`CultivatorBlock.java:47`；`ModBlockStateProvider.java:200-221`）：`working=false` 引用 `_a_top_still/_a_side_still/_a_bottom`，`working=true` 引用 16 帧帧条。**这就是新转换器要照抄的那套做法。**
- 绑定：`CultivatorBlock.setPlacedBy`（`:88-105`）选核 → `core.addCultivator(pos)` → `cultivator.setCore(...)`；核侧 `OutpostCoreBlockEntity.cultivators`（`:335-395`）是唯一真相，容量 `outpost_core.cultivatorCapacity`（默认 24），**列表只存 `BlockPos`，不认方块类型**，`isProcessingBlock` 只查 `farmstorm:processing_block` 标签（`:802-804`）。

### 1.4 man瓜炮弹

- man瓜 = `pumpkin_turret`（`ModLangProvider.java:91`「man瓜种子」）。炮弹 = `TurretBullet`（166 行），`ThrowableItemProjectile` + `setNoGravity(true)`。
- **没有寿命上限**：`tick()`（`:78-92`）只做索敌与转向；命中 / 撞方块即 `discard()`。
- 相关 config：`pumpkin_turret.{targetRadius, projectileDamage, knockbackLevel, fireIntervalTicks, bulletSpeed}`（`FarmstormConfig.java:445-458`）。
- `MelonGrenadeProjectile`（瓜！）与 `MelonSliceProjectile` 是**另一条线**，本版不动。

### 1.5 进度文案

- `fang_dance`（`ModAdvancementProvider.java:140-141`，`impossible(...)`，服务端 award）。
- `ModLangProvider.java:229`：`advancement(entries, "fang_dance", "最扎人的一集", "The Itchiest Episode", "触发一次「赢过输过没死过」", "Trigger a Fang Trap Cactus once")`。
- **中英本来就不一致**：英文描述里没有「赢过输过没死过」的对应词。

---

## §2 作者裁定（18 条，2026-11-05）

| # | 议题 | 裁定 |
|---|---|---|
| 1 | 方块注册 ID | **改为 `crop_converter`**（旧 `converter` 下线，不并存） |
| 2 | GUI / 经验 / 下界之星 | **全部删除**（界面、菜单、配方书分类、经验、下界之星、燃料标签） |
| 3 | 转换配方体系 | **保留 `farmstorm:converting` 类型，只留正向** |
| 4 | 30 秒 | **进 config**，新键默认 **600 刻** |
| 5 | 边界行为 | **全部照抄培养器先例**（拆掉掉输入物 / 核心满暂停不丢 / 工作中不可取回 / 水平四向 FACING / 不实现 Container / 与培养器共享 24 名额 / 连线红色） |
| 6 | 15 个改良作物方块 | **保留但不可获得** |
| 7 | 平凡作物种子 | **把 `plain_chives` 升级成可种植作物** |
| 8 | `seed_cycle` 进度 | **直接删掉** |
| 9 | 培养器产出 | **2 个投入物 + 2 个对应种子（如果有）** |
| 10 | 史诗排除范围 | **凡史诗档案的转换输入都排**（火把花 / 瓶子草荚 / 甜浆果 / 金苹果） |
| 11 | man瓜炮弹 | **`TurretBullet`，进 config 默认 300 刻** |
| 12 | 成就文案 | **中文改，英文也补对应表达** |
| 13 | 文档流程 | **照旧**（编号 52~57 / 只出中文 / README 增量） |
| 14 | 平凡韭菜档案 | **不进 `CropProfile` 的 22 条档案**，另走轻量路径 |
| 15 | 平凡韭菜转换配方 | **新增 `平凡韭菜 → 朋友的韭`** |
| 16 | 「对应种子」判据 | **自身即种子的也算，给 4 个**（小麦→2 麦+2 麦种；胡萝卜→4 胡萝卜） |
| 17 | `nether_star_toll` 进度 | **删掉** |
| 18 | 平凡韭菜作物贴图 | **复用朋友的韭的 4 张 stage 图** |

---

## §3 目标数字

### 3.1 注册面 98 → **92**

| 类 | 现 | 增 | 减 | 后 | 依据 |
|---|---|---|---|---|---|
| `items` | 34 | +1（`plain_chives_seeds`） | −7（7 个种子） | **28** | 裁定 7、14 |
| `blocks` | 27 | +1（`plain_chives_crop`） | 0 | **28** | 裁定 7；`converter`→`crop_converter` 是改名，不计数 |
| `block_entity_type` | 8 | 0 | 0 | **8** | 同上，改名 |
| `entity_type` | 13 | 0 | 0 | **13** | — |
| `mob_effect` | 4 | 0 | 0 | **4** | — |
| `sound_event` | 3 | 0 | 0 | **3** | — |
| `recipe_type` | 1 | 0 | 0 | **1** | 裁定 3 保留 `converting` |
| `recipe_serializer` | 1 | 0 | 0 | **1** | 同上 |
| `creative_mode_tab` | 1 | 0 | 0 | **1** | — |
| `damage_type` | 3 | 0 | 0 | **3** | — |
| `data_component_type` | 1 | 0 | 0 | **1** | — |
| `menu_type` | 2 | 0 | −1（`converter`） | **1** | 裁定 2 |
| **合计** | **98** | | | **92** | |

### 3.2 其余口径

| 项 | 现 | 后 | 说明 |
|---|---|---|---|
| `farmstorm:converting` 配方 | 26 | **21** | −6（种子双向）+1（平凡韭菜→朋友的韭）；全部单向、无催化剂、无经验 |
| 培养器可收的投入物 | 0（原版进不去） | **17** | 21 条转换配方输入 − 4 条史诗输入 |
| server config | 176 键 / 60 push | **175 键 / 60 push** | −3（`[converter]` 整组）＋1（`crop_converter.processTicks`）＋1（`pumpkin_turret.bulletLifetimeTicks`）；`[converter]` 换成 `[crop_converter]`，push 数不变 |
| 语言键 | 340 × 2 | **321 × 2** | 逐键清单见 54 号。实测分解：`item` 34 / `block` 27 / `plain` 51 / `advancement` 18×2 / `config` 179 / `entity` 13 = 340 |
| Java 文件数 | 154 | **152** | −3（`ConverterMenu`/`ConverterScreen`/`ConverterRecipeBookCategory`）−1（`SeedItem`，删完 7 个种子后无任何引用）＋2（`PlainCropBlock`、`PlainSeedItem`）；`ConverterBlock`/`ConverterBlockEntity` 是改名，不计数 |
| 网络包 | 2 | **2** | 红线靠给 `Link` 加一个类型字节，不新增包 |
| Mixin | 0 | **0** | — |

> ⚠️ `Farmstorm.java:180-183` 的自检断言必须与注册面**同一次提交**改到位，否则启动即打印 `FAIL`。`tools/verify_project.py:334-393` 的 `EXPECTED` / `EXPECTED_COUNTS` 同理。

---

## §4 新机器的设计要点（供 53 / 54 号展开）

### 4.1 方块 `farmstorm:crop_converter`（农作物转换器）

- 属性：**逐项照铁砧**，直接抄 `FarmstormBlocks.java:95-103` 培养器那七行（`MapColor.METAL` / `strength(5.0F, 1200.0F)` / `requiresCorrectToolForDrops` / `SoundType.ANVIL` / `PushReaction.BLOCK`）。原版铁砧证据：`Blocks.java` 的 `ANVIL` = `.strength(5.0F, 1200.0F)`。
- 状态：`FACING`（水平四向）+ `WORKING`（布尔）⇒ **8 个 variant**。
- 红石 / 比较器：不接。
- 标签：`farmstorm:processing_block`（plan 原文写 `farmstorm_processing_block`，按既有标签 `farmstorm:processing_block` 落地）、`minecraft:mineable_with_pickaxe`。
- 渲染：`RenderShape.MODEL`（`BaseEntityBlock` 默认 INVISIBLE）。

### 4.2 工作循环

```
输入槽空 → 不工作
输入槽有货 ∧ 可用（绑定 ∧ 核侧认 ∧ 3×3 区块内） → progress++
progress == processTicks(600) → 投递 1 个产物进核
  投递成功 → 清槽、进度归零、WORKING=false
  投递失败（核满）→ 槽与进度都保留、WORKING=false，下一秒继续尝试
不可用 → 不 tick，不清槽不清进度
```

- **一次一个**：放入走 `useWithoutItem` + `heldStack.split(1)`，与培养器同形。
- **不能取回**：空手右键无反应；取回只能拆方块（`releaseHeld()` 同款）。
- 产物：转换配方 `result`，数量 1。
- **不实现 `Container`**（裁定 5）：堵漏斗 / 投掷器。

### 4.3 动画

- 贴图包 16 帧、`frametime: 2` ⇒ 32 刻一循环（1.6 秒）；600 刻内播 18.75 个循环。
- **只有工作时动**：靠 `WORKING` 属性切两个模型——空闲引静态图（`_still`），工作引帧条。**不写代码去暂停动画**（陷阱：动态 PNG 一旦被引用就会自己播）。
- 贴图落位（沿用培养器命名惯例）：`crop_converter_a_front` (+`.png.mcmeta`) / `_front_still` / `_side` (+mcmeta) / `_side_still` / `_top` (+mcmeta) / `_top_still` / `_bottom`（静态，无 mcmeta）。

### 4.4 连线颜色

- 现状：`CultivatorLinkRenderer.java:47-50` 颜色**硬编码**青色 `(0.30, 0.85, 0.90)`；`CultivatorLinkPayload.Link` 是 `record Link(BlockPos core, BlockPos cultivator)`，`MAX_LINKS = 1024`。
- 方案：给 `Link` 加一个 `Kind`（`CULTIVATOR` / `CROP_CONVERTER`），渲染按 kind 选颜色；培养器保持青色不变，转换器用红色。
- **不**在渲染时按方块类型反查（区块未加载会闪色）。

### 4.5 平凡作物这一条独立路径

| 物件 | 实现 | 说明 |
|---|---|---|
| `plain_chives` | 不变（`Item` + 食物 2） | 0.2.2 已注册 |
| `plain_chives_crop` | **新类 `PlainCropBlock extends CropBlock`** | 4 阶段 `age 0~3`、`SHAPE_BY_AGE` 同 15 个作物、生长读同一组 `growth.*` config。**不复用 `FarmstormCropBlock`**：它的构造器与 codec 强制要 `CropProfile` |
| `plain_chives_seeds` | **新类 `PlainSeedItem extends ItemNameBlockItem`** | **必须可播种**。不能复用 `SeedItem`：它的 `useOn` 无条件 `PASS`（`:38-41`） |
| 贴图 | 复用 `friend_chives_stage0~3.png` | 裁定 18 |

---

## §5 必改清单（逐文件，19 项）

1. `FarmstormBlocks.java:71-72` — `CONVERTER`→`CROP_CONVERTER`、id `crop_converter`、属性换成铁砧七项。
2. `FarmstormBlocks.java:102` 与 `:104` 之间 — 新增 `PLAIN_CHIVES_CROP`（用 `PlainCropBlock`，不走 `crop(profile)` 私有工厂）。
3. `FarmstormItems.java:112-113` — `CONVERTER`→`CROP_CONVERTER`（BlockItem）。
4. `FarmstormItems.java:102-109` — 删 7 个种子里除 `chive_seeds` 外的 6 个；`chive_seeds` 一并删；新增 `PLAIN_CHIVES_SEEDS`。
5. `FarmstormItems.java:140-151` — 创造栏清单同步（`SEED_ITEMS` 只剩 1 个）。
6. `FarmstormBlockEntities.java` — `CONVERTER`→`CROP_CONVERTER`。
7. `FarmstormMenus.java` — 删 `CONVERTER` 条目（`menu_type` 2→1），类注释「2 个 MenuType」改 1。
8. `ConverterMenu.java` / `ConverterScreen.java` / `ConverterRecipeBookCategory.java` — **整文件删**；`FarmstormClient.java:62,75` 两行同步删。
9. `ConverterBlock.java` → `CropConverterBlock.java` — 删 `getMenuProvider`；`useWithoutItem` 改写；加 `WORKING` 属性、`getTicker`、`setPlacedBy`、`onRemove`（照 `CultivatorBlock`）。
10. `ConverterBlockEntity.java` → `CropConverterBlockEntity.java` — 重写：不再 `implements Container`，删双槽 / 催化剂 / 经验 / `ContainerData` / 配方缓存代际，改成「单槽 + 进度 + 绑定核」；`useWithoutItem` 的准入闸照 `CultivatorBlockEntity`。
11. `ConverterRecipe.java` — 删 `xpCost`/`catalyst`/`catalystCount`/`bidirectional`/`reverseXpCost` 与 `reverse()`，只留 `input`/`result`；`ConverterRecipeSerializer` 同步。
12. `ModRecipeProvider.java` — `converter()` 改配方（6 铁 + 2 玻璃 + 1 红石）；删 `:86-92` 六条种子配方；`twoWay` 改单向、`oneWayWithNetherStar` → `oneWay`；新增 `plain_chives → friend_chives`；**新增培养器配方**（4 铁 + 4 玻璃 + 1 红石）。
13. `FarmstormConfig.java:19-21,295-305` — 删 `[converter]` 三键；新增 `[crop_converter] processTicks`；`[pumpkin_turret]` 加 `bulletLifetimeTicks`。
14. `ModAdvancementProvider.java:95-106` — 删 `seed_cycle`；删 `nether_star_toll`；`converter` / `first_conversion` 保留（前者图标换新方块）。
15. `ModLootTableProvider.java:71-105` — 作物战利品表去掉种子；新增 `plain_chives_crop` 与 `crop_converter` 的表。
16. `ModBlockStateProvider.java:160-185` — `converterStates` 重写为「两态 × 四朝向」；`ModItemModelProvider.java:69` 的 `instanceof` 加 `CropConverterBlock`；新增 `plain_chives_crop` 的四阶段 blockstate。
17. `ModLangProvider.java` — 见 54 号的逐键清单（删 23 / 加 4 / 改名 2 / 改值 1，最终 321 × 2）。
18. `ModItemTagsProvider.java:42-48` — `#farmstorm:seeds` 成员改写；`:54` `CONVERTER_FUEL` 整段删；`FarmstormTags.java:38-40` 删该 TagKey。
19. `Farmstorm.java:180-183` + `tools/verify_project.py:334-393` — 自检数字与 ID 清单改到 §3.1。

### 5.1 资源面（实测，原先漏算）

**`textures/item/` 现有 29 张**，本版要动 8 个文件：

| 动作 | 文件 | 理由 |
|---|---|---|
| 重命名 | `chive_seeds.png` → `plain_chives_seeds.png` | 种子换了归属；与裁定 #18「复用朋友的韭美术」同一条纪律，不新增美术 |
| 删 | `converter.png` | **本来就未被引用**（转换器物品模型走 `modLoc("block/...")`，`ModItemModelProvider:45-46,68-70`） |
| 删 | 6 张种子图 | `puffed_wheat_seeds` / `power_beet_seeds` / `melon_grenade_seeds` / `pumpkin_turret_seeds` / `awakened_torchflower_seeds` / `awakened_pitcher_plant_pod` |

结果 **29 − 7 = 22 张**（重命名不计增删）。

⚠️ 陷阱：`ModItemModelProvider:55-57` 的兜底 `basicItem(item)` 会去找 `textures/item/<id>.png`，找不到就是**紫黑格**（`:61-66` 的注释记过同一件事）。`plain_chives_seeds` 必须有图，所以是「重命名」而不是「删掉再新建」。

**`textures/block/` 现有 84 张 png + 2 个 mcmeta**：删旧转换器 **7 张**（`converter_front` / `converter_side` / `converter_side_{wheat,carrot,potato,beetroot}` / `converter_top`），加新转换器 **7 张**（`crop_converter_a_{front,front_still,side,side_still,top,top_still,bottom}`）+ **3 个 mcmeta**。**png 总数仍是 84，mcmeta 从 2 变 5**。`plain_chives_crop` 复用 `friend_chives_stage0~3.png`，**不新增方块贴图**。

---

## §6 红线

1. **不许给 15 个改良作物方块补 BlockItem**：裁定 6 是「保留但不可获得」，补了 BlockItem 就变成可获得。
2. **不许复用 `SeedItem` 承载平凡韭菜种子**：它被 `useOn → PASS` 钉死，平凡作物必须能种。
3. **不许在渲染期按方块类型反查颜色**：区块未加载会闪色，必须走 payload 的 kind 字节。
4. **不许把动态 PNG 的暂停交给代码**：动画只能靠 blockstate 切模型。
5. **不许改 `docs/zh/00`~`45`**、不许改 `docs/en/*`、本轮**不生成英文文档**。
6. **不许动 `MelonGrenadeProjectile` / `MelonSliceProjectile`**：300 刻只作用于 `TurretBullet`。
7. **不许把 `plain_chives` 塞进 `CropProfile.ALL`**：那会让它进 `LUCKY_BERRY_POOL` 与 `MODIFIED_CROPS` 标签。

---

## §7 待办与已知风险

1. **Java 文件数**：154 → **152**（−3 GUI 三件 −1 `SeedItem` +2 新类）。数行数只用 `Get-Content -Encoding UTF8`；按换行符 split 会因末尾换行**多算 1**。实测：`ConverterMenu` 216 / `ConverterScreen` 86 / `ConverterRecipeBookCategory` 24 / `ConverterBlockEntity` 435 / `ConverterRecipe` 112 / `SeedItem` 54。
2. **`converting` 配方单向化后 `ConverterRecipe.reverse()` 的删法**：`ConverterRecipeSerializer` 的 codec 字段也要同步删，否则旧数据包会读失败——54 号要在 §4.8.5 写清兼容口径。
3. **老存档**：`farmstorm:converter` 方块消失（裁定 1 的既定代价）；`farmstorm:converter` 物品失效；已获得的 7 种种子物品失效。要在 README 的升级须知里点名。
4. **史诗作物变便宜**：删掉下界之星门槛后，火把花 / 瓶子草只需 30 秒——这是裁定 2 的直接后果，53 号要写成「已知影响」而不是悄悄带过。
5. **`ModBlockStateProvider` 的兜底分支**：`plain_chives_crop` 若漏写分支会落到 `simpleBlock(block)`，生成错误模型而不报错（同类陷阱在 `:80` 的注释里记过）。
6. **红线加字段的两个静默失效点**：`CultivatorLinkBroadcaster.java:76-77` 的 `ORDER` 比较器只比两个 `long`；同文件的 `fingerprint(links)` 是去抖判据。`Kind` 必须同时进这两处，否则「kind 变了不重发」——客户端线条颜色永远不更新，而服务端看起来一切正常。
7. **`plain_chives_seeds` 的 item 贴图必须有**：`ModItemModelProvider:55-57` 的兜底 `basicItem` 会去找 `textures/item/plain_chives_seeds.png`，找不到就是紫黑格。做法是把 `chive_seeds.png` **重命名**过去（`BRIEF` §5.1）。
8. **`PROTOCOL_VERSION` 必须 `"2"` → `"3"`**：`Link` 从 2 字段变 3 字段就是 codec 变了，`FarmstormNetwork:22-24` 的纪律是「codec 一变必须跟着变」。不升会「协商通过后用旧 codec 解新流」，崩在客户端而非握手阶段。
9. **`FarmstormClient:75` 不能直接删**：删掉 `ConverterRecipeBookCategory` 的注册会让 21 条 `converting` 配方刷 21 条 `Unknown recipe category` WARN。做法是「类文件照删、把那一行 finder 内联进 `FarmstormClient`」。
10. **`first_conversion` 进度的授予点要搬家**：旧位置 `ConverterBlockEntity:255` 随界面一起消失。搬到「投递成功那一支、授予绑定核的归属者（在线时）」，不搬则该进度永远拿不到。
11. **红线 `Kind` 的落盘方案**：核侧旁挂新 NBT 键 `CultivatorKinds`（`int[]`，与 `Cultivators` 同长；缺键 ⇒ 全按 `CULTIVATOR`，0.2.2 存档无损）。代价：`addCultivator` 加 `Kind` 参数，**三个写入点**都要改；`rebindNearby`（`OutpostCoreBlockEntity:537-572`，`:547` 只收集 `CultivatorBlockEntity.class`）必须扩展到两种机器，否则「先放机器后放核」的转换器永远绑不上。
12. **`FarmstormTags.java` 的注释在 `:35`**（不是 `:33`）；`#farmstorm:seeds` 现状 6 条、改后 1 条。
