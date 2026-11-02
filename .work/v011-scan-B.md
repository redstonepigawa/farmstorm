# v0.1.1 事实扫描报告 · B（材质 / man瓜 / 朋友的韭 / 音频链）

扫描时间：2026-11-02T19:56+08:00
扫描范围：`D:\project_farm`（modid `farmstorm`，Java 包 `com.dashpig.farmstorm`，MC 1.21.1 / NeoForge 21.1.213）
读取方式：全部文本用 `[IO.File]::ReadAllLines($p,[Text.Encoding]::UTF8)` 或 read 工具；PNG 统计用 System.Drawing（本机无 python、无 Pillow）。

---

## 0. 结论摘要（只讲事实）

1. 「man瓜」= **`farmstorm:pumpkin_turret`**，中文字面量就是带英文的「man瓜」（`zh_cn.json:57/58/287/288`）；「瓜！」= `melon_grenade`，「瓜片子」= `melon_slice_bomb`。
2. man瓜的炮弹 `TurretBullet` **已有追踪实现**：`tick()` 在服务端每刻调 `steerTowards(target)` 重算 `setDeltaMovement(归一化方向 × 0.6)`（`TurretBullet.java:74-96`）。
3. 当前 `textures/item/wild_apple.png` 是 **16×16、571 B、88 个去重 RGBA、107 个全透明像素**，其中 **33 个像素是 `#3FA34D` 绿色「+」**，位于 (9,9)–(15,15) 的 7×7 十字 —— 即现贴图**已经带着**绿色角标。
4. `tools/gen_textures.py` **会**生成 `wild_apple.png`（`ICONS:270` + `ICON_DRAWERS:916` + `draw_wild_apple:884-895`），重跑时**会覆盖**该文件，且因为 `add_plus=True`（`build_specs:407`）**会重新叠上绿色「+」**（`compose:974-975`）。
5. `wild_apple_64x64.png` 存在于 Downloads，**64×64、6,946 B、1814 个去重 RGBA、1471 个全透明像素**；它**不是**任何 16×16 图的 4× 最近邻放大（256 个 4×4 块里 204 块不均匀）。
6. 朋友的韭投掷物落地即 `discard()`：`ThrownCropEntity.settle()` 在 `onImpact` 之后按 `keepAliveAfterImpact()` 决定，`FriendChivesProjectile` 未重写该方法（默认 `false`）。
7. 「Q弹」= `farmstorm:q_bounce`（`QbounceEffect`），amplifier 是字面量 `0`（`FriendChivesProjectile.java:63`），时长读 config `friend_chives.effectTicks` 默认 200 刻。
8. 现有 `sound_event` **共 3 个**：`wild_hunter_loop` / `zangief_piledriver` / `friend_chives`，ogg 分别 352,061 / 33,568 / 83,932 B。
9. 本机 **ffmpeg 不在 PATH**，但存在于 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`（7.1-essentials，带 libvorbis）；**ffprobe 未找到**。
10. `pengyoudejiu.mp3` 存在，**7,450,588 B、158.26 s、48 kHz 立体声 320 kb/s**；`tools/convert_media.ps1:74` 已配置「只取开头 10 秒」并产出当前 10.00 s 的 `friend_chives.ogg`。

---

## 1. 狂野苹果材质

### 1.1 现贴图实测

**实测**（System.Drawing 逐像素）：

`src/main/resources/assets/farmstorm/textures/item/wild_apple.png`

| 项 | 值 |
|---|---|
| 字节数 | **571 B** |
| 像素尺寸 | **16 × 16**（256 px） |
| 去重 RGBA 数 | **88** |
| 去重 RGB 数（仅 A>0） | **87** |
| 全透明（A=0）像素 | **107** |
| 半透明（0<A<255）像素 | **0** |
| 不透明（A=255）像素 | **149** |

逐像素 alpha 图（`.` = A0，`#` = 不透明非 `#3FA34D`，`+` = `#3FA34D`）：

```
 0: ................
 1: .........###....
 2: .......#.##.....
 3: ................
 4: ...#########....
 5: ..###########...
 6: .#############..
 7: ##############..
 8: ##############..
 9: .##########+++..
10: .##########+++..
11: .########+++++++
12: ..#######+++++++
13: ...######+++++++
14: ....#######+++..
15: .........#.+++..
```

- `#3FA34D` 像素共 **33 个**，全部落在 x∈[9,15]、y∈[9,15] 的 7×7 区域 —— 与 `tools/gen_textures.py` 的 `build_marker(size=7, arm=3)` 十字形状像素数完全一致（3×7 + 3×7 − 3×3 = 33），颜色常量见 `tools/gen_textures.py:63-66`（`PLUS_COLOR = (0x3F, 0xA3, 0x4D, 0xFF)`）。
- 仓库文档里对这张图的记载是「11 色 / 透明 126 / 256」（`docs/zh/22-DECISIONS-0.1.0.md:581`、`docs/zh/27-PROMPT-TASKS-0.1.0.md:405`、`docs/zh/26-PROMPT-MAIN-0.1.0.md:199`）。**实测值与文档值不一致**（实测 88 色 / 107 透明 / 其中 33 px 是绿色「+」）。

### 1.2 物品模型 json

`src/generated/resources/assets/farmstorm/models/item/wild_apple.json`（105 B，全文 6 行）：

```
1: {
2:   "parent": "minecraft:item/generated",
3:   "textures": {
4:     "layer0": "farmstorm:item/wild_apple"
5:   }
6: }
```

- `parent` = `minecraft:item/generated`（第 2 行）
- `layer0` = `farmstorm:item/wild_apple`（第 4 行）
- **没有** `overrides`，**没有**其它纹理层，**没有**其它键。

`src/main/resources/assets/farmstorm/models/item/wild_apple.json` **不存在**（`Test-Path` = False）；模型只在 `src/generated/resources` 下有一份。

### 1.3 `wild_apple` 物品注册处

| 位置 | 内容 |
|---|---|
| `src/main/java/com/dashpig/farmstorm/registry/FarmstormItems.java:74-78` | `WILD_APPLE = cropItem(CropProfile.WILD_APPLE, ... new FoodCropItem(...), foodProperties(CropProfile.WILD_APPLE, FarmstormConfig.wildAppleNutrition, FarmstormConfig.wildAppleSaturationModifier))` |
| `src/main/java/com/dashpig/farmstorm/util/CropProfile.java:96-100` | `CropProfile WILD_APPLE = new CropProfile(Farmstorm.id("wild_apple"), CropType.EDIBLE, categorySet(CropCategory.BUFF), null, null, null, Set.of(), OTHER_STACK_TIER)` |
| `src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java:762-767` | config 分组 `wild_apple`（`nutrition`、`saturationModifier` 两键） |
| `src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java:89` | `item(entries, "wild_apple", "狂野苹果", "Wild Apple")` |
| `src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java:339-340` | 两条 config 语言键 |
| `src/main/java/com/dashpig/farmstorm/datagen/ModRecipeProvider.java:96` | `twoWay(recipeOutput, "wild_apple", Items.GOLDEN_APPLE, FarmstormItems.WILD_APPLE)` |
| `src/main/java/com/dashpig/farmstorm/datagen/ModItemTagsProvider.java:52, 94` | 注释 + 燃料标签写入 |
| `src/main/java/com/dashpig/farmstorm/datagen/ModAdvancementProvider.java:157` | 首个进度图标 |
| `src/main/java/com/dashpig/farmstorm/item/FoodCropItem.java:61` | 食用逻辑所在类中的 wild_apple 相关行 |
| `src/main/java/com/dashpig/farmstorm/Farmstorm.java:111, 113` | 启动自检文案里的排除项 |
| `src/generated/resources/assets/farmstorm/lang/zh_cn.json:234, 235, 293` | 三条语言键 |
| `src/generated/resources/assets/farmstorm/lang/en_us.json:234, 235, 293` | 同上（英文） |
| `src/generated/resources/data/farmstorm/recipe/wild_apple.json:8` | 配方 id |
| `src/generated/resources/data/farmstorm/advancement/first_wild_hunter.json:15` | 进度图标引用 |
| `src/generated/resources/data/farmstorm/tags/item/converter_fuel.json:38` | `"farmstorm:wild_apple"` |
| `tools/verify_project.py:159` | 校验脚本的物品名清单 |
| `README.md:499` | 素材来源表 |

**与「贴图叠加 / + 号 / 角标」直接相关的代码，只有 `tools/gen_textures.py` 一处**（详见 1.4）。Java 侧**没有任何** `wild_apple` 的贴图叠加、`ItemPropertyFunction`、`overrides`、`ItemDecorator`、模型层叠加代码（1.2 的模型 json 只有一层 `layer0`）。

### 1.4 `tools/gen_textures.py` 是否生成 `wild_apple.png`

**是。** 相关行号逐个点名：

| 行 | 内容 |
|---|---|
| `tools/gen_textures.py:154` | `WILD_APPLE_SOURCE = Path(r"C:\Users\DASHpig\Desktop\2797B56FEF56AF13125F333A6989632A.png")` —— 默认源图是**桌面那张 1294×1216**，不是 Downloads 的 64×64 |
| `tools/gen_textures.py:155` | `WILD_APPLE_SOURCE_ENV = "FARMSTORM_WILD_APPLE_SOURCE"` |
| `tools/gen_textures.py:158` | `WILD_APPLE_COVERAGE = 250` |
| `tools/gen_textures.py:270` | `ICONS` 表条目 `("wild_apple", "狂野苹果", (("item", "apple"),))` |
| `tools/gen_textures.py:274-275` | `NO_PLUS_ICONS = frozenset({"converter"})` —— 只有 converter 免叠「+」 |
| `tools/gen_textures.py:400-410` | `build_specs()`：`add_plus=item_id not in NO_PLUS_ICONS`（407 行），`drawer=item_id if item_id in ICON_DRAWERS else None`（408 行） |
| `tools/gen_textures.py:843-846` | `wild_apple_source()`：有环境变量就用环境变量，否则 `WILD_APPLE_SOURCE` |
| `tools/gen_textures.py:849-881` | `wild_apple_grid()`：按 alpha bbox 取景 → 切 16 网格 → 每格取众数色 → 覆盖率 < `WILD_APPLE_COVERAGE` 留透明 |
| `tools/gen_textures.py:884-895` | `draw_wild_apple()`：读源图 → `wild_apple_grid(raw.convert("RGBA"), TEXTURE_SIZE)`；源图缺失时打印警告并返回 `None`（退回候选链 → 原版 `item/apple`） |
| `tools/gen_textures.py:915-917` | `ICON_DRAWERS = {"wild_apple": draw_wild_apple}` |
| `tools/gen_textures.py:957-979` | `compose()`：962 行取 `DRAWERS[spec.drawer]()`；**974-975 行 `if spec.add_plus: return compose_icon(base, marker)`** |
| `tools/gen_textures.py:63-66` | `PLUS_SIZE=7 / PLUS_ARM=3 / PLUS_MARGIN=0 / PLUS_COLOR=(0x3F,0xA3,0x4D,0xFF)` |
| `tools/gen_textures.py:546-560` | `build_marker()` 画 7×7 实心十字（33 px） |
| `tools/gen_textures.py:612-623` | `compose_icon()` 把 marker 贴到右下角 `left = 16-7-0 = 9`、`top = 9` |
| `tools/gen_textures.py:2-7` | 文件头 docstring 明写「15 个作物与 6 个播种物图标右下角叠绿色「+」（plan0.0.1.txt 第 8 条，区分原版与改良）；转换器图标不加」 |
| `tools/gen_textures.py:1141-1142` | 报告输出里 `action += " + 绿色「+」"` |

**重跑会不会覆盖当前贴图**（只描述逻辑）：

1. 输出路径 = `<out>/assets/farmstorm/textures/item/wild_apple.png`（`output_path`，`tools/gen_textures.py:986-988`），当 `--out src/main/resources` 时**就是**当前这张文件的路径。
2. 图源走 `ICON_DRAWERS` 的 `draw_wild_apple()`，**不读** `wild_apple_64x64.png`；默认读 `C:\Users\DASHpig\Desktop\2797B56FEF56AF13125F333A6989632A.png`（**实测存在**，1,113,442 B），除非设置环境变量 `FARMSTORM_WILD_APPLE_SOURCE`。
3. `compose()` 在 974 行判断 `spec.add_plus`；`wild_apple` 不在 `NO_PLUS_ICONS` 里，所以 `add_plus=True`，会走 `compose_icon()` 把绿色「+」重新叠回右下角。
4. 尺寸常量 `TEXTURE_SIZE = 16`（`tools/gen_textures.py:59`），输出恒为 16×16。
5. 因此重跑会**整体覆盖**当前 `wild_apple.png`，且产物是「16×16 + 右下角绿色 +」。

### 1.5 「狂野苹果右下角绿色 + 号」的历史记录

全仓搜 `plus / badge / overlay / corner / +` 的命中（逐个点名）：

| 位置 | 命中内容 | 是否相关 |
|---|---|---|
| `plan0.0.1.txt:8` | 「为区分原版与修改的农作物，每种改良农作物的材质在右下角加一个绿色的‘+’」 | **是（规则原文）** |
| `plan0.1.1.txt:10` | 「注意先前要求的‘在模组物品材质右下角增加绿色的‘+’号这一规则已作废」 | **是（作废声明）** |
| `tools/gen_textures.py:7` | docstring：15 作物 + 6 播种物图标右下角叠绿色「+」 | **是** |
| `tools/gen_textures.py:63-66` | `PLUS_SIZE / PLUS_ARM / PLUS_MARGIN / PLUS_COLOR` | **是** |
| `tools/gen_textures.py:274-275` | `NO_PLUS_ICONS` | **是** |
| `tools/gen_textures.py:407` | `add_plus=item_id not in NO_PLUS_ICONS` | **是** |
| `tools/gen_textures.py:546-560` | `build_marker()` | **是** |
| `tools/gen_textures.py:612-623` | `compose_icon()` | **是** |
| `tools/gen_textures.py:974-975` | 叠「+」的调用点 | **是** |
| `tools/gen_textures.py:1141-1142` | 报告文案 | **是** |
| `README.md:493` | 「按需做灰度化 / 缩放到 16×16 / **右下角叠绿色「+」**」 | **是** |
| `docs/zh/12-TECH-0.0.2.md:1428` | 「作物图标右下角叠绿色「+」（`plan0.0.1.txt` 第 8 条区分原版与改良），四个新作物同样加「+」」 | **是** |
| `docs/en/12-TECH-0.0.2.md:1419` | 同上英文版 | **是** |
| `tools/gen_textures.py:343-393` | `CONVERTER_SIDE_OVERLAYS` / `compose_crop_side()` —— 转换器侧面叠**作物图标** | 否（是 workspace 侧面叠加，不是「+」） |
| `tools/gen_textures.py:180, 183, 195` | `Spec.add_plus` / `Spec.overlay` 字段 | 是（数据结构） |
| `docs/zh/*`、`docs/en/*` 其它 `overlay` 命中 | 均为转换器侧面叠加的描述 | 否 |
| `plan0.1.1.txt:9` | 狂野苹果材质改为 `C:\Users\DASHpig\Downloads\wild_apple_64x64.png` | 是（本版需求原文） |

`badge`：全仓 **0** 命中。`corner`：仅 `tools/gen_textures.py` 的 docstring 与上述条目，**0** 处独立命中。

### 1.6 源图 `wild_apple_64x64.png`

**实测**：`C:\Users\DASHpig\Downloads\wild_apple_64x64.png`

| 项 | 值 |
|---|---|
| 是否存在 | **存在** |
| 字节数 | **6,946 B** |
| 像素尺寸 | **64 × 64**（4096 px） |
| 去重 RGBA 数 | **1814** |
| 去重 RGB 数（仅 A>0） | **1813** |
| 全透明（A=0）像素 | **1471** |
| 不透明像素 | **2625** |
| 半透明（0<A<255） | **0** |
| alpha bbox | x ∈ [2,60]，y ∈ [0,63] |
| 4×4 分块均匀性 | 256 块中 **52 块**均匀、**204 块**不均匀（⇒ 不是任何 16×16 图的 4× 最近邻放大） |

---

## 2. man瓜 身份与现状

### 2.1 身份确认：「man瓜」= `farmstorm:pumpkin_turret`

`src/generated/resources/assets/farmstorm/lang/zh_cn.json` 中含「瓜」的**全部**键值（共 16 行，逐条列出）：

| 行 | 键 | 值 |
|---|---|---|
| 32 | `advancements.farmstorm.turret_hit.description` | 被 **man瓜** 的导弹命中一次 |
| 52 | `block.farmstorm.melon_grenade_stem` | 瓜！ |
| 57 | `block.farmstorm.pumpkin_turret` | **man瓜** |
| 58 | `block.farmstorm.pumpkin_turret_stem` | **man瓜** |
| 69 | `death.attack.farmstorm.void_bolt.player` | %1$s 被 %2$s 的 **man瓜** 击落 |
| 80 | `entity.farmstorm.melon_grenade` | 瓜！ |
| 81 | `entity.farmstorm.melon_slice_bomb` | 瓜片子 |
| 134 | `farmstorm.configuration.explosion.grenadeResistThreshold` | 瓜！爆炸无法摧毁的抗性阈值 |
| 135 | `farmstorm.configuration.explosion.sliceResistThreshold` | 瓜片子爆炸无法摧毁的抗性阈值 |
| 179 | `farmstorm.configuration.melon_grenade.spawnCount` | 落点上方生成的瓜片子个数 |
| 181 | `farmstorm.configuration.melon_grenade.submunitionSpeed` | 瓜片子的径向初速（格/刻） |
| 277 | `item.farmstorm.melon_grenade` | 瓜！ |
| 278 | `item.farmstorm.melon_grenade_seeds` | 瓜！种子 |
| 279 | `item.farmstorm.melon_slice_bomb` | 瓜片子 |
| 287 | `item.farmstorm.pumpkin_turret` | **man瓜** |
| 288 | `item.farmstorm.pumpkin_turret_seeds` | **man瓜种子** |

**结论**：中文名含「瓜」的对象共 4 个 —— `pumpkin_turret`（man瓜）、`melon_grenade`（瓜！）、`melon_slice_bomb`（瓜片子）、`melon_grenade_stem`（瓜！）；「man瓜」**唯一**对应 **`farmstorm:pumpkin_turret`**。`plan0.0.1.txt:91-97` 的原始需求条也写作「物品名称：man瓜 / 对应原版作物：南瓜 / 类型：部署型…」。

注册链逐个点名：

| 对象 | 注册位置 |
|---|---|
| 作物档案 | `src/main/java/com/dashpig/farmstorm/util/CropProfile.java:64-67` `PUMPKIN_TURRET = deployed("pumpkin_turret", "pumpkin_turret_stem", "pumpkin_turret_seeds", DeployedKind.PUMPKIN_TURRET, categorySet(TURRET, DAMAGE), AttachFace.FLOOR, WALL, CEILING)` |
| 部署方块 | `src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java:47` `PUMPKIN_TURRET = deployed(CropProfile.PUMPKIN_TURRET, true, true, false, false)` |
| 茎方块 | `src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java:32` `PUMPKIN_TURRET_STEM = crop(CropProfile.PUMPKIN_TURRET)` |
| 物品 | `src/main/java/com/dashpig/farmstorm/registry/FarmstormItems.java:51-52` `PUMPKIN_TURRET = cropItem(..., DeployableCropItem(...))` |
| 种子物品 | `src/main/java/com/dashpig/farmstorm/registry/FarmstormItems.java:103` `PUMPKIN_TURRET_SEEDS = seedItem("pumpkin_turret_seeds", CropProfile.PUMPKIN_TURRET)` |
| 炮弹实体 | `src/main/java/com/dashpig/farmstorm/registry/FarmstormEntityTypes.java:60-66` `TURRET_BULLET`，注册名 `turret_bullet` |
| 行为类 | `src/main/java/com/dashpig/farmstorm/behavior/impl/PumpkinTurret.java`（157 行） |

### 2.2 攻击间隔 / 冷却

| 项 | 值 | 证据 |
|---|---|---|
| config 键名 | `pumpkin_turret.fireIntervalTicks` | `FarmstormConfig.java:431-433` |
| 默认值 | **60 刻**（= 3 秒） | `FarmstormConfig.java:433` `.defineInRange("fireIntervalTicks", 60, 1, 72000)` |
| 单位 | 游戏刻 | 同上 |
| 读取点 | `return FarmstormConfig.pumpkinTurretFireIntervalTicks.get();` | `PumpkinTurret.java:80-82` |
| 生效路径 | `TurretBehavior.tick()`：`findTarget().ifPresent(target -> { fire(...); be.setCooldown(cooldownTicks()); })` | `TurretBehavior.java:26-32` |
| 冷却递减 | `if (be.cooldown > 0) { be.cooldown--; return; }`（冷却期连索敌都不做） | `DeployedCropBlockEntity.java:146-149` |
| 部署后冷却常量 | `DEPLOY_COOLDOWN_TICKS = 5` | `DeployedCropBlockEntity.java:49` |
| 扫描间隔 | `scanInterval()` 默认 **10 刻** | `behavior/TurretBehavior.java:13-16` |
| 错峰 | `stagger(interval) = floorMod(pos.hashCode(), interval)` | `DeployedCropBlockEntity.java:156-162` |

### 2.3 伤害

| 项 | 值 | 证据 |
|---|---|---|
| damage_type 注册名 | `farmstorm:void_bolt` | `FarmstormDamageTypes.java:20` `VOID_BOLT = key("void_bolt")` |
| damage_type JSON | `{"exhaustion":0.0,"message_id":"farmstorm.void_bolt","scaling":"when_caused_by_living_non_player"}` | `src/generated/resources/data/farmstorm/damage_type/void_bolt.json:1-5` |
| 伤害值 config 键 | `pumpkin_turret.projectileDamage` | `FarmstormConfig.java:425-427` |
| 默认值 / 范围 | **1** 点 / 0–100 | `FarmstormConfig.java:427` `.defineInRange("projectileDamage", 1, 0, 100)` |
| 施加点 | `hit.hurt(FarmstormDamageTypes.voidBolt(server, owner), FarmstormConfig.pumpkinTurretProjectileDamage.get());` | `TurretBullet.java:106` |
| DamageSource 工厂 | `level.damageSources().source(VOID_BOLT, owner)` | `FarmstormDamageTypes.java:31-33` |
| 击退等级 config 键 | `pumpkin_turret.knockbackLevel`，默认 **1**，范围 0–10 | `FarmstormConfig.java:428-430` |
| 击退施加点 | `living.knockback(FarmstormConfig.pumpkinTurretKnockbackLevel.get(), -direction.x, -direction.z);` | `TurretBullet.java:110-117` |
| 死亡文案 | `death.attack.farmstorm.void_bolt.player` = 「%1$s 被 %2$s 的 man瓜 击落」 | `zh_cn.json:69` |
| 进度 | `farmstorm:turret_hit`（被导弹命中的玩家获得） | `TurretBullet.java:107-109` |
| 漂浮效果 | 代码中**没有**任何 `LEVITATION` 施加 | `TurretBullet.java:98-119` 全文无 |

### 2.4 炮弹实体类 `TurretBullet`

| 项 | 值 | 证据 |
|---|---|---|
| 类名 | `com.dashpig.farmstorm.entity.TurretBullet` | `TurretBullet.java:26` |
| 父类 | `net.minecraft.world.entity.projectile.ThrowableItemProjectile` | `TurretBullet.java:26` |
| 注册名 | `farmstorm:turret_bullet` | `FarmstormEntityTypes.java:60-66` |
| EntityType 参数 | `MobCategory.MISC` / `sized(0.3F, 0.3F)` / `clientTrackingRange(4)` / `updateInterval(10)` | `FarmstormEntityTypes.java:61-66` |
| 飞行速度 | `public static final double SPEED = 0.6;` —— **硬编码常量，无 config 键** | `TurretBullet.java:28` |
| 重力 | **无**：两个构造器都 `this.setNoGravity(true);` | `TurretBullet.java:43-51`（45、50 行） |
| 占位物品 | `getDefaultItem()` 返回 `Items.SHULKER_SHELL` | `TurretBullet.java:58-62` |

`tick()` 运动学（逐行抄关键行，`TurretBullet.java:73-96`）：

```java
73:     @Override
74:     public void tick() {
75:         if (this.launchOrigin == null) {
76:             this.launchOrigin = this.position();
77:         }
78:         if (this.level() instanceof ServerLevel) {
79:             if (this.target != null && !this.target.isAlive()) {
80:                 this.target = null;
81:             }
82:             if (this.target != null) {
83:                 this.steerTowards(this.target);
84:             }
85:         }
86:         super.tick();
87:     }
88:
89:     /** 每 tick 朝目标重算速度：命中判定交给原版 move 流程，本类不自己做射线检测。 */
90:     private void steerTowards(Entity aim) {
91:         Vec3 delta = aim.getBoundingBox().getCenter().subtract(this.position());
92:         if (delta.lengthSqr() < 1.0E-4) {
93:             return;
94:         }
95:         this.setDeltaMovement(delta.normalize().scale(SPEED));
96:     }
```

**既有追踪 / homing 逻辑：有。** `steerTowards()` 每刻把速度方向重置为「当前位置 → 目标碰撞箱中心」的单位向量 × `SPEED`，只在服务端执行（`this.level() instanceof ServerLevel` 判定，第 78 行）。目标由炮台在开火时 `bullet.setTarget(target)` 写入（`PumpkinTurret.java:74`）。

- 追踪只在服务端（类注释 `TurretBullet.java:23-25`：「追踪只在服务端做（HC-03），客户端靠实体同步位置」）。
- 读档后目标不保留：`readAdditionalSaveData` 强制 `this.target = null;`（`TurretBullet.java:138-143`），尽管 `addAdditionalSaveData` 会写 `TAG_TARGET`（`:130-136`）。

### 2.5 命中判定

```java
 98:     @Override
 99:     protected void onHitEntity(EntityHitResult result) {
100:         super.onHitEntity(result);
101:         if (!(this.level() instanceof ServerLevel server)) {
102:             return;
103:         }
104:         Entity hit = result.getEntity();
105:         Entity owner = this.getOwner();
106:         hit.hurt(FarmstormDamageTypes.voidBolt(server, owner), FarmstormConfig.pumpkinTurretProjectileDamage.get());
...
118:         this.discard();
119:     }
...
122:     @Override
123:     protected void onHitBlock(BlockHitResult result) {
124:         super.onHitBlock(result);
125:         if (this.level() instanceof ServerLevel) {
126:             this.discard();
127:         }
128:     }
```

- `onHitEntity`：`hurt(void_bolt, projectileDamage)` → 若命中的是 `ServerPlayer` 发 `turret_hit` 进度 → 若命中 `LivingEntity` 施加击退 → `discard()`（`TurretBullet.java:98-119`）。
- `onHitBlock`：直接 `discard()`，**不炸方块**（`TurretBullet.java:121-128`）。
- 射线检测、命中判定本身由原版 `ThrowableItemProjectile` / `Projectile` 的 `move` 流程负责（类注释 `:89`：「命中判定交给原版 move 流程，本类不自己做射线检测」）。

### 2.6 弹药消耗

**不存在。** 全仓搜 `弹药` / `ammo` / `Ammo`（`Select-String -Pattern '弹药|ammo|Ammo'` 对 `src\main\java\**\*.java`）→ **0 命中**。`PumpkinTurret.fire()` 只做 `new TurretBullet(...)` + `addFreshEntity`，不检查也不扣任何物品/能量。

### 2.7 发射者判定 / 索敌

| 项 | 实现 | 证据 |
|---|---|---|
| 索敌半径 config | `pumpkin_turret.targetRadius`，默认 **15**，范围 0–64（切比雪夫） | `FarmstormConfig.java:422-424`；读取 `PumpkinTurret.java:46` |
| 距离口径 | `DistanceMetrics.chebyshev(dx,dy,dz)`，取方块坐标 | `PumpkinTurret.java:152-156` |
| 查询盒 | `AABB.ofSize(center, 2r+1, 2r+1, 2r+1)` | `PumpkinTurret.java:120-124` |
| 玩家优先级 | 非观察者、非创造、非己方（`be.isAlly(player)` 排除） | `PumpkinTurret.java:85-100`（89-91 行） |
| 敌对生物 | `Monster` 类；与玩家按距离取近，平手判给玩家 | `PumpkinTurret.java:106-118` |
| 投掷物拦截 | `INTERCEPT_PROJECTILES = true`（全模组唯一保留拦截投掷物的炮台） | `PumpkinTurret.java:28-29` |
| 拦截排除 | 己方导弹不拦：`if (projectile instanceof TurretBullet) return false; ... !be.isAlly(owner)` | `PumpkinTurret.java:144-150` |
| 己方判定 | `DeployedCropBlockEntity.isAlly(Player)` → `DeployedOwnership.isAlly(server, ownerUuid, ownerName, player)` | `DeployedCropBlockEntity.java:289-293` |
| 归属者实体 | `owner(ServerLevel)`：离线返回 `null`；`ownerUuid == null` 也返回 `null` | `DeployedCropBlockEntity.java:296-302` |
| 导弹 owner | 有归属者 → `new TurretBullet(type, owner, level)`；否则无 owner | `PumpkinTurret.java:69-74` |
| 出膛点 | `BULLET_HALF_SIZE=0.15`、`MUZZLE_MARGIN=0.1`，沿射线顶到方块碰撞箱外 | `PumpkinTurret.java:31-33, 59-68` |

### 2.8 全模组既有 homing / 追踪 实现（搜 `homing`/`tracking`/`steer`/`ShulkerBullet`/`getTarget`）

| 命中 | 性质 |
|---|---|
| `TurretBullet.java:90-96` `steerTowards()` | **唯一的追踪实现**（带目标插值的 `setDeltaMovement`） |
| `TurretBullet.java:64-71` `setTarget()` / `target()` | 追踪目标存取 |
| `TurretBullet.java:82-84` | 每刻调 `steerTowards` 的调用点 |
| `PumpkinTurret.java:74` `bullet.setTarget(target)` | 目标注入点 |
| `client/render/TurretBulletRenderer.java:7, 28, 32` | **仅借用** `ShulkerBulletModel` 模型 / `ModelLayers.SHULKER_BULLET` 做渲染，不涉及 `ShulkerBullet` 实体或追踪 |
| `FarmstormEntityTypes.java:25, 54, 64, 75` | `clientTrackingRange(4)` —— 是**网络同步范围**，与 homing 无关 |
| `QbounceObserverSync.java:74` | 注释里的 `entity-tracking-range` —— 同上 |
| `WildHunterCombatEvents.java:31` `event.getTarget()` | 事件的目标实体，与追踪无关 |
| `homing` 字样 | 全仓 Java 侧 **0 命中** |

### 2.9 实体继承关系（可复用的原版追踪投射物）

全仓 Java 侧 `extends Projectile / AbstractHurtingProjectile / ShulkerBullet / ThrowableItemProjectile / ThrowableProjectile / Entity` 的命中**只有 2 处**：

| 类 | 父类 | 证据 |
|---|---|---|
| `entity/TurretBullet.java` | `ThrowableItemProjectile` | `TurretBullet.java:26` |
| `entity/ThrownCropEntity.java`（抽象基类） | `ThrowableItemProjectile` | `ThrownCropEntity.java:29` |

- **没有任何实体继承 `ShulkerBullet`**、**没有任何实体继承 `AbstractHurtingProjectile`**、**没有实体直接继承 `Projectile`**。
- `ShulkerBullet` 只在客户端渲染器里被当作模型来源使用（`client/render/TurretBulletRenderer.java:7, 28, 32`）。

---

## 3. 朋友的韭

### 3.1 物品 / 种子 / 投掷物注册

| 对象 | 注册位置 |
|---|---|
| 物品 `friend_chives` | `registry/FarmstormItems.java:84-87` `FRIEND_CHIVES = cropItem(CropProfile.FRIEND_CHIVES, ... new ThrowableCropItem(CropProfile.FRIEND_CHIVES, FarmstormEntityTypes.FRIEND_CHIVES::get, properties))` |
| 种子 `chive_seeds` | `registry/FarmstormItems.java:88-89` `CHIVE_SEEDS = seedItem("chive_seeds", CropProfile.FRIEND_CHIVES)` |
| 作物方块 `friend_chives_crop` | `registry/FarmstormBlocks.java:42-43` `FRIEND_CHIVES_CROP = crop(CropProfile.FRIEND_CHIVES)` |
| 作物档案 | `util/CropProfile.java:107-112` `FRIEND_CHIVES = throwable("friend_chives", "friend_chives_crop", "chive_seeds", CropCategory.BUFF)` |
| 投掷物实体类型 | `registry/FarmstormEntityTypes.java:54-58` `FRIEND_CHIVES = projectile("friend_chives", FriendChivesProjectile::new)` |

`entity/thrown/` 下 12 个文件与父类（逐个点名）：

| 文件 | 父类 |
|---|---|
| `BambooFirecrackerProjectile.java` | `ThrownCropEntity` |
| `BrownMushroomPuffProjectile.java` | `ThrownCropEntity` |
| `CompressedPuffedWheatProjectile.java` | `PuffedWheatProjectile`（间接：→ `ThrownCropEntity`） |
| **`FriendChivesProjectile.java`** | **`ThrownCropEntity`** |
| `MelonGrenadeProjectile.java` | `ThrownCropEntity` |
| `MelonSliceProjectile.java` | `ThrownCropEntity` |
| `MolotovProjectile.java` | `ThrownCropEntity` |
| `PuffedWheatProjectile.java` | `ThrownCropEntity` |
| `RedMushroomPuffProjectile.java` | `ThrownCropEntity` |
| `ToxicUbiServerProjectile.java` | `ThrownCropEntity` |
| `UbiServerProjectile.java` | `ThrownCropEntity` |
| `VibrationBeanProjectile.java` | `ThrownCropEntity` |

基类 `ThrownCropEntity extends ThrowableItemProjectile`（`entity/ThrownCropEntity.java:29`）。

### 3.2 落地 / 命中处理与 `discard` 时机

`ThrownCropEntity.java`（158 行）逐个点名：

```java
 64:     /** F-32 ①：寿命上限取 G-6 的 throwable.lifetimeTicks；已进入续命的实体由子类自行收尾。 */
 65:     @Override
 66:     public void tick() {
 67:         super.tick();
 68:         if (this.level() instanceof ServerLevel && !this.settled
 69:                 && this.tickCount > FarmstormConfig.throwableLifetimeTicks.get()) {
 70:             this.discard();
 71:         }
 72:     }
 73:
 74:     @Override
 75:     protected void onHitBlock(BlockHitResult result) {
 76:         super.onHitBlock(result);
 77:         if (this.level() instanceof ServerLevel server) {
 78:             Vec3 impact = result.getLocation().add(Vec3.atLowerCornerOf(result.getDirection().getNormal()).scale(0.05));
 79:             this.settle(server, impact, null);
 80:         }
 81:     }
 82:
 83:     @Override
 84:     protected void onHitEntity(EntityHitResult result) {
 85:         super.onHitEntity(result);
 86:         if (this.level() instanceof ServerLevel server) {
 87:             this.settle(server, result.getLocation(), result.getEntity());
 88:         }
 89:     }
 90:
 ...
 95:     private void settle(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
 96:         if (this.settled) {
 97:             return;
 98:         }
 99:         this.settled = true;
100:         this.onImpact(level, pos, directHit);
101:         if (!this.keepAliveAfterImpact()) {
102:             this.discard();
103:         }
104:     }
 ...
111:     protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
112:         // 无额外效果：本方法的重写者是 M3 的 8 个投掷物。
113:     }
 ...
116:     protected boolean keepAliveAfterImpact() {
117:         return false;
118:     }
```

- `FriendChivesProjectile` **没有**重写 `keepAliveAfterImpact()`（全文 81 行，只重写 `profile()` 与 `onImpact()`）⇒ 返回 `false` ⇒ `onImpact` 后**立刻 `discard()`**（第 101-103 行）。
- 落地位置：`onHitBlock` 的 `result.getLocation()` 沿命中面法线外推 0.05 格（第 78 行）。
- 实体寿命上限：config `throwable.lifetimeTicks`（`FarmstormConfig.java:325-327`）；`keepAliveAfterImpact()` 为 true 的子类（爆竹）不受此限（`!this.settled` 条件）。
- 该实体**没有** `setNoGravity`，因此受重力（`ThrownCropEntity` 全文无 `setNoGravity`）。

### 3.3 既有范围效果代码（逐行抄）

`entity/thrown/FriendChivesProjectile.java:51-80`：

```java
 51:     /**
 52:      * DESIGN §4.6 的判定顺序（同一刻完成，不排队）：欧氏球筛 → 逐个写 Q弹 → 累计施加成功数 n → n ≥ 1 播一次音。
 53:      * 谓词里只有几何与存活条件：效果端不筛敌我，投掷者本人站进球内同样中招（DESIGN §4.5 / §12.12）。
 54:      */
 55:     @Override
 56:     protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
 57:         double radius = FarmstormConfig.friendChivesHitRadius.get();
 58:         int ticks = FarmstormConfig.friendChivesEffectTicks.get();
 59:         int applied = 0;
 60:         // AABB 只是粗筛（2r × 2r × 2r 的立方），命中判定必须再用 distanceTo 做欧氏复核（TECH §3.3 的「二次筛」）。
 61:         for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(radius),
 62:                 candidate -> candidate.isAlive() && pos.distanceTo(candidate.position()) <= radius)) {
 63:             if (target.addEffect(new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true))) {
 64:                 applied++;
 65:             }
 66:         }
 67:         Farmstorm.LOGGER.info(String.format(Locale.ROOT,
 68:                 "朋友的韭 落点 (%.2f, %.2f, %.2f) 球内施加 %d 个生物（半径 %.2f 格，%d 刻，amplifier 0）",
 69:                 pos.x, pos.y, pos.z, applied, radius, ticks));
 70:         // 判据只有一条：n 就是上面的施加成功计数，不另做一次「附近有没有生物」的查询（DESIGN §4.6）。
 71:         if (applied < 1) {
 72:             return;
 73:         }
 74:         float volume = (float) (FarmstormConfig.friendChivesSoundRange.get() / SOUND_RANGE_PER_VOLUME);
 75:         level.playSound(null, pos.x, pos.y, pos.z, FarmstormSounds.FRIEND_CHIVES.get(), SoundSource.PLAYERS,
 76:                 volume, SOUND_PITCH);
 77:         Farmstorm.LOGGER.info(String.format(Locale.ROOT,
 78:                 "朋友的韭 命中音 farmstorm:friend_chives 播放一次：落点 (%.2f, %.2f, %.2f) 音源 PLAYERS 音量 %.2f（可闻 %.1f 格）",
 79:                 pos.x, pos.y, pos.z, volume, volume * SOUND_RANGE_PER_VOLUME));
 80:     }
```

- 范围效果**一次性**发生在 `onImpact`（落地/命中那一刻），**没有**「落地后持续 N 刻、每刻/每隔若干刻结算一次」的代码（`FriendChivesProjectile` 无 `tick()` 重写；`ThrownCropEntity.tick()` 只在超寿命时 `discard`）。
- 相关常量：`SOUND_RANGE_PER_VOLUME = 16.0`（`:29-33`）、`SOUND_PITCH = 1.0F`（`:35-36`）。
- 范围半径 config：`friend_chives.hitRadius` 默认 **6.0**，范围 0–16（`FarmstormConfig.java:851-853`）。
- 可闻半径 config：`friend_chives.soundRange` 默认 **32.0**，范围 0–64（`FarmstormConfig.java:857-859`）。

### 3.4 现有「Q弹」效果

| 项 | 值 | 证据 |
|---|---|---|
| 注册名 | `farmstorm:q_bounce` | `registry/FarmstormMobEffects.java:31-32` |
| 类名 | `com.dashpig.farmstorm.effect.QbounceEffect` | `effect/QbounceEffect.java:15` |
| 注册行 | `REGISTER.register("q_bounce", QbounceEffect::new)` | `FarmstormMobEffects.java:31-32` |
| 构造 | `super(MobEffectCategory.HARMFUL, 0xFF6EC7)` | `QbounceEffect.java:17-19` |
| 是否重写 tick 逻辑 | **否**：类注释明写「不重写 `applyEffectTick` / `isDurationEffectTick`，不设时长键，不读任何 config 键」 | `QbounceEffect.java:10-13` |
| 时长来源 | 由施加方 `FriendChivesProjectile` 写入 `FarmstormConfig.friendChivesEffectTicks` | `FriendChivesProjectile.java:58, 63` |
| 时长 config | `friend_chives.effectTicks` 默认 **200** 刻，范围 **20–1200** | `FarmstormConfig.java:854-856` |
| amplifier | **字面量 `0`**，无 config 键 | `FriendChivesProjectile.java:63` `new MobEffectInstance(Q_BOUNCE, ticks, 0, false, true, true)` |
| 图标 | `assets/farmstorm/textures/mob_effect/q_bounce.png` | 文件清单 |
| 感知侧 | `client/QbounceClientState`、姿态侧 `QbouncePoseRenderer` 等（类注释点名） | `QbounceEffect.java:13` |

### 3.5 现有 sound_event（共 3 个，逐个点名）

| # | 注册名 | 注册行 | ogg 路径 | 字节数 | 实测时长/格式 | sounds.json 行 |
|---|---|---|---|---|---|---|
| 1 | `farmstorm:wild_hunter_loop` | `registry/FarmstormSounds.java:20-22`（`createVariableRangeEvent`） | `assets/farmstorm/sounds/wild_hunter_loop.ogg` | **352,061 B** | 47.14 s / 48 kHz mono vorbis 64 kb/s | `sounds.json:2-8` |
| 2 | `farmstorm:zangief_piledriver` | `registry/FarmstormSounds.java:25-27`（`createVariableRangeEvent`） | `assets/farmstorm/sounds/zangief_piledriver.ogg` | **33,568 B** | 4.00 s / 44.1 kHz mono vorbis 64 kb/s | `sounds.json:9-15` |
| 3 | `farmstorm:friend_chives` | `registry/FarmstormSounds.java:30-32`（`createVariableRangeEvent`） | `assets/farmstorm/sounds/friend_chives.ogg` | **83,932 B** | 10.00 s / 48 kHz mono vorbis 64 kb/s | `sounds.json:16-22` |

（时长/采样率/码率由 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe -i` **实测**。）

### 3.6 `sounds.json` 全文要点（23 行）

| 事件名 | category | subtitle | 文件 | stream | attenuation_distance |
|---|---|---|---|---|---|
| `wild_hunter_loop` | `player` | `subtitles.farmstorm.wild_hunter_loop` | `farmstorm:wild_hunter_loop` | **`false`**（显式） | **32**（显式） |
| `zangief_piledriver` | `player` | `subtitles.farmstorm.zangief_piledriver` | `farmstorm:zangief_piledriver` | 未设置 | **16**（显式） |
| `friend_chives` | `player` | `subtitles.farmstorm.friend_chives` | `farmstorm:friend_chives` | **`false`**（显式） | 未设置（走原版默认 16） |

- 三个事件都是 `category: "player"`，各只有 1 个 sound 条目，均无 `volume` / `pitch` / `weight` / `type` 字段。
- `wild_hunter_loop` 文件头注释（`FarmstormSounds.java:11-13`）说明为什么要 `createVariableRangeEvent`：「固定距离事件会把半径钉死在服务端」。

### 3.7 现有 `playSound(...)` 调用点（逐个点名）

| # | 位置 | 参数形态 | 走的 API 重载 |
|---|---|---|---|
| 1 | `util/LuckyBerryRoller.java:266-267` | `level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F)` | `Level#playSound(Player, double,double,double, SoundEvent, SoundSource, float, float)` |
| 2 | `explosion/FarmstormExplosion.java:220-221` | `this.serverLevel.playSound(null, center.x, center.y, center.z, this.getExplosionSound().value(), SoundSource.BLOCKS, 4.0F, pitch)` | 同上（double×3） |
| 3 | `behavior/impl/ZangiefCaneTrap.java:93` | `level.playSound(null, player, FarmstormSounds.ZANGIEF_PILEDRIVER.get(), SoundSource.PLAYERS, 1.0F, 1.0F)` | `Level#playSound(Player, Entity, SoundEvent, SoundSource, float, float)` |
| 4 | `behavior/impl/ZangiefCaneTrap.java:125` | `level.playSound(null, pos, FarmstormSounds.ZANGIEF_PILEDRIVER.get(), SoundSource.BLOCKS, 1.0F, 1.0F)` | `Level#playSound(Player, BlockPos, SoundEvent, SoundSource, float, float)` |
| 5 | `behavior/impl/VibrationBeanTrap.java:93` | `level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, EXPLODE_VOLUME, EXPLODE_PITCH)` | 同上（BlockPos） |
| 6 | `behavior/impl/PointDefenseFungusBehavior.java:111` | `level.playSound(null, be.getBlockPos(), SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.8F, 1.2F)` | 同上（BlockPos） |
| 7 | `entity/thrown/BrownMushroomPuffProjectile.java:78` | `level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.NEUTRAL, 0.8F, 0.7F)` | 同上（double×3） |
| 8 | `entity/thrown/RedMushroomPuffProjectile.java:78` | 同 7，pitch `0.9F` | 同上（double×3） |
| 9 | **`entity/thrown/FriendChivesProjectile.java:75-76`** | `level.playSound(null, pos.x, pos.y, pos.z, FarmstormSounds.FRIEND_CHIVES.get(), SoundSource.PLAYERS, volume, SOUND_PITCH)` | 同上（double×3），`volume = soundRange / 16.0` |

**非 `playSound` 的声音投递（2 处，需与上表区分）：**

- `combat/WildHunterAura.java:211-213`：直接发 `new ClientboundSoundEntityPacket(FarmstormSounds.WILD_HUNTER_LOOP, SoundSource.PLAYERS, holder, 1.0F, 1.0F, level.getRandom().nextLong())` —— **不走** `Level#playSound`。
- `client/WildHunterLoopSound.java:32`：客户端本地 `SoundInstance`（`super(FarmstormSounds.WILD_HUNTER_LOOP.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom())`）。

另有 2 处**注释**提到 `playSound`（不是调用）：`FriendChivesProjectile.java:30`、`event/FarmstormGameEvents.java:191`。

### 3.8 音频素材 `pengyoudejiu.mp3`

**实测**：`D:\dsh\default\pengyoudejiu.mp3`（`ffmpeg -hide_banner -i`）

| 项 | 值 |
|---|---|
| 是否存在 | **存在** |
| 字节数 | **7,450,588 B** |
| 时长 | **00:02:38.26 = 158.26 s**（ffmpeg 提示「Estimating duration from bitrate, this may be inaccurate」） |
| 音频流 | mp3 (mp3float)，**48000 Hz，stereo，320 kb/s** |
| 附带流 | `Stream #0:1: Video: png, rgba, 1120x1120`（内嵌封面图） |
| 元数据 | title「「邦友的酒(DJ版)」梦的结唱halo」/ artist「活跃黑江乐」/ album「【夢ノ結唱 HALO 】」 |

- 当前 `friend_chives.ogg` 与它的关系：`tools/convert_media.ps1:61` 把 `$Chives` 默认指向该 mp3，`:74` 的 plan 条目带 `Seconds = 10`，`:190-196` 用 `-t 10` 单次转码 + `-fflags +bitexact`。产出 10.00 s / 83,932 B 的 ogg（**实测**一致）。
- 同目录另有 `Animals_from_3m04s.mp3`（2,153,318 B）。

---

## 4. 音频 / 材质处理工具链

### 4.1 ffmpeg / ffprobe

| 项 | 结果 |
|---|---|
| `Get-Command ffmpeg -ErrorAction SilentlyContinue` | **空**（不在 PATH） |
| `Get-Command ffprobe -ErrorAction SilentlyContinue` | **空**（不在 PATH） |
| 全盘（`D:\dsh` 递归）搜 `ffmpeg*.exe` | **命中 1 个**：`D:\dsh\default\sf6_audio\tools\ffmpeg.exe` |
| 该 ffmpeg 版本 | `ffmpeg version 7.1-essentials_build-www.gyan.dev` |
| libvorbis 编码器 | **有**：`A....D libvorbis  libvorbis (codec vorbis)` |
| `ffprobe` 全盘命中 | **0** |
| `D:\project_farm\.tools\ffmpeg`、`D:\project_farm\tools\ffmpeg` | 均**不存在** |

### 4.2 `.tools` 目录逐条点名

| 条目 | 类型 | 说明 |
|---|---|---|
| `.tools/jdk21/` | 目录 | OpenJDK **21.0.2** 2024-01-16（`jdk21\bin\java.exe -version` **实测**）；含 `bin conf include jmods legal lib release` |
| `.tools/jdk21/` 用途 | — | `gw.cmd:3` `set "JAVA_HOME=D:\project_farm\.tools\jdk21"` |
| `.tools/jdk-patch/` | 目录 | `classes` / `src` / `build-patch.cmd` / `CheckIsWritable.java`；`gw.cmd:12` 用 `--patch-module java.base=` 打补丁 |
| `.tools/gradle-8.10/` | 目录 | Gradle 8.10 解包（`bin/gradle`、`bin/gradle.bat`、`init.d/`、`lib/`） |
| `.tools/gradle-8.10-bin.zip` | 文件 136,713,202 B | Gradle 8.10 发行包 |
| `.tools/gradle-home/` | 目录 | `gw.cmd:4` `GRADLE_USER_HOME` |
| `.tools/openjdk21.zip` | 文件 201,328,186 B | JDK 发行包 |
| `.tools/gw.cmd` | 文件 2,978 B | Gradle wrapper 脚本（设置 JAVA_HOME / GRADLE_USER_HOME / TEMP / JAVA_TOOL_OPTIONS / 起 8899 代理 / 目录锁） |
| `.tools/gw.ps1` | 文件 389 B | 同上 PowerShell 版 |
| `.tools/init.gradle` | 文件 1,257 B | Gradle init 脚本 |
| `.tools/neoforge-proxy.mjs` | 文件 3,005 B | `gw.cmd:16` 起的 Node 代理（127.0.0.1:8899） |
| `.tools/npm-cache/` | 目录 | npm 缓存 |
| `.tools/tmp/` | 目录 | 临时目录（`gw.cmd:5-6`） |
| `.tools/_probe_src/` | 目录 | — |
| `.tools/README.md` | 文件 4,516 B | 说明 |
| `.tools/test.pom` | 文件 7,767 B | — |
| `.tools/m3b-server.log` / `m3b-server2.log` / `m3b-server3.log` / `m3b-server4.log` | 40,898 / 28,084 / 20,470 / 19,172 B | 历史服务器日志 |

**`.tools` 下没有任何音频工具**（无 ffmpeg、无 sox、无 lame、无 audacity 相关）。唯一可用的 ffmpeg 在仓库外：`D:\dsh\default\sf6_audio\tools\ffmpeg.exe`。

### 4.3 既有「外部素材 → 模组资源」的脚本 / 先例

| 脚本 | 处理对象 | 关键点 |
|---|---|---|
| **`tools/convert_media.ps1`**（233 行） | **音频 + 图标** | `$Plan` 数组共 **7 条**（`convert_media.ps1:68-87`）：3 条 audio（`wild_hunter_loop.ogg` / `zangief_piledriver.ogg` / `friend_chives.ogg`）+ 4 条 icon（`mob_effect/wild_hunter.png`、`mob_effect/q_bounce.png`、`item/friend_chives.png`、`item/chive_seeds.png`）。音频分支 `:189-196`：`-ac 1 -c:a libvorbis -b:a 64k` + 可选 `-t <Seconds>` + `-fflags +bitexact`。图标分支 `:197-215`：`-vf crop/scale:lanczos` + `-pix_fmt rgba`。契约：缺 ffmpeg / 缺 libvorbis / 缺源 → exit 1 不写文件（`:132-160`）；转换失败 → exit 2（`:217-221`）；输出比源新则 skip（`:177-186`）。 |
| `tools/gen_textures.py`（1183 行） | **PNG 材质** | 用 Pillow（`from PIL import Image, ImageDraw`，`:57`）+ `zipfile` 直读原版 jar（`:51`）。`--list` / `--vanilla-assets` / `--additions` 三种模式（`:22-29`）。 |
| `.tools/jdk-patch/build-patch.cmd` | JDK 模块补丁 | 非素材类。 |
| `.work/f130_crop_calc.py` | 图标裁切框计算 | `convert_media.ps1:78` 注释指出「recompute with the recipe in .work/f130_crop_calc.py」。 |
| `.scratch/p24a-sheet.py` / `p24b-*.py` / `p24c-*.py` | 图像比对 / 计数 | v0.1.0 期 F-163 的校验脚本（含 `import PIL` 相关用法）。 |

- 全仓搜 `ogg`：命中 `tools/convert_media.ps1`（多条）、`src/main/resources/assets/farmstorm/sounds.json`、`README.md`。
- 全仓搜 `resize` / `PIL` / `Image`：命中 `tools/gen_textures.py`（`Image.new/resize/Resampling.NEAREST/lanczos` 等）、`.scratch/p24*.py`。
- `convert_media.ps1` 文件头明写「本文件故意为 ASCII」（`:24-28`）：PowerShell 5.1 会按 ANSI 代码页解码无 BOM 的 UTF-8 脚本，中文注释会静默破坏解析（曾在 `$Plan` 数组里吃掉一个条目）。

### 4.4 `tools/gen_textures.py` 整体作用与产出数量

**一行说明**：程序化生成模组全部占位 / 成品 PNG 材质（从原版客户端 jar 读贴图后做灰度化、缩放、染色、叠「+」、程序绘制），产出 `textures/item`、`textures/block`、`textures/gui` 三类文件。

产出数量（文件头 `:2-3`、`:5-14`、`print_listing` `:996-1000`）：

| 组 | 数量 | 说明 |
|---|---|---|
| `textures/item/<id>.png` | **26** | 15 作物 + 6 播种物 + converter + V-23 的 4 个（`ICONS` 表 `:242-272`，共 26 条） |
| `textures/block/<base>_stage{0..3}.png` | **56** | 14 作物 × 4 阶段（`STAGE_CROPS` `:281-313`） |
| `textures/block/<id>.png` | **16** | 5 部署方块 + zangief_cane + compressed_hay_block + converter_{top,side,front} + 4 张 converter_side_* + compressed_hay_block_{top,bottom}（`BLOCK_TEXTURES` `:318-`） |
| `textures/gui/converter.png` | **1** | 176×166 |
| **主表合计** | **99** | 文件头 `:3`「共 99 张」 |
| `--additions` 额外 5 张 | **5** | `vibration_bean`（程序绘制）+ `friend_chives_stage0..3`（`build_additions_specs` `:926-954`） |

**已知不一致（事实陈述）**：`print_listing` 的 docstring（`:397`）写「91 条」，`--list` 帮助文本（`:1024-1027`）写「99 张」，文件头（`:2-3`）写「91 → 99」。三处数字不同源。

---

## 5. 资源清单现状

### 5.1 `src/main/resources/assets/farmstorm/textures/item/` —— 共 **29** 个文件

| # | 文件名 | 字节 |
|---|---|---|
| 1 | `awakened_pitcher_plant.png` | 367 |
| 2 | `awakened_pitcher_plant_pod.png` | 395 |
| 3 | `awakened_torchflower.png` | 363 |
| 4 | `awakened_torchflower_seeds.png` | 320 |
| 5 | `bamboo_firecracker.png` | 299 |
| 6 | `blast_carrot.png` | 391 |
| 7 | `brown_mushroom_puff.png` | 213 |
| 8 | `chive_seeds.png` | 735 |
| 9 | `compressed_puffed_wheat.png` | 469 |
| 10 | `converter.png` | 587 |
| 11 | `fang_trap_cactus.png` | 420 |
| 12 | `friend_chives.png` | 867 |
| 13 | `lucky_berry.png` | 347 |
| 14 | `melon_grenade.png` | 307 |
| 15 | `melon_grenade_seeds.png` | 235 |
| 16 | `melon_slice_bomb.png` | 328 |
| 17 | `molotov.png` | 324 |
| 18 | `power_beet.png` | 304 |
| 19 | `power_beet_seeds.png` | 252 |
| 20 | `puffed_wheat.png` | 398 |
| 21 | `puffed_wheat_seeds.png` | 192 |
| 22 | `pumpkin_turret.png` | 320 |
| 23 | `pumpkin_turret_seeds.png` | 197 |
| 24 | `red_mushroom_puff.png` | 194 |
| 25 | `toxic_ubi_server.png` | 455 |
| 26 | `ubi_server.png` | 319 |
| 27 | `vibration_bean.png` | 258 |
| 28 | **`wild_apple.png`** | **571** |
| 29 | `zangief_cane.png` | 355 |

（`outpost_core.png` **不在** `textures/item/` 下 —— 与 `docs/zh/26-PROMPT-MAIN-0.1.0.md:89` 的约束一致。）

### 5.2 本次相关的方块贴图（实测）

| 文件 | 字节 | 尺寸 | 去重 RGBA | 全透明像素 |
|---|---|---|---|---|
| `textures/block/outpost_core_top.png` | **2,745 B** | **32 × 32** | （未统计） | （未统计） |
| `textures/block/outpost_core_side.png` | **2,534 B** | **32 × 32** | （未统计） | （未统计） |
| `textures/block/outpost_core_bottom.png` | **2,513 B** | **32 × 32** | （未统计） | （未统计） |
| `textures/block/compressed_hay_block.png` | **420 B** | **16 × 16** | **9** | **0** |
| `textures/block/compressed_hay_block_top.png` | **125 B** | **16 × 16** | **5** | **0** |
| `textures/block/compressed_hay_block_bottom.png` | **164 B** | **16 × 16** | **5** | **0** |

`compressed_hay_block*.png` 三张全不透明（0 个 A=0 像素），色数 9 / 5 / 5。

### 5.3 `src/main/resources/assets/farmstorm/sounds/` —— 共 **3** 个文件

| 文件名 | 字节数 | 实测时长 |
|---|---|---|
| `friend_chives.ogg` | **83,932 B** | 10.00 s |
| `wild_hunter_loop.ogg` | **352,061 B** | 47.14 s |
| `zangief_piledriver.ogg` | **33,568 B** | 4.00 s |

### 5.4 `compressed_hay_block` 方块 Properties 注册

`src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java:72-79`：

```java
72:     // ---- 1 个建材方块（D-57：硬度照远古残骸 30.0，抗性照圆石 6.0，六面同材质不可旋转）----
73:     public static final DeferredBlock<Block> COMPRESSED_HAY_BLOCK = REGISTER.registerBlock("compressed_hay_block",
74:             Block::new,
75:             BlockBehaviour.Properties.of()
76:                     .mapColor(MapColor.COLOR_BROWN)
77:                     .strength(30.0F, 6.0F)
78:                     .requiresCorrectToolForDrops()
79:                     .sound(SoundType.GRASS));
```

| 属性 | 值 | 行号 |
|---|---|---|
| mapColor | `MapColor.COLOR_BROWN` | 76 |
| 硬度 / 抗爆 | **`strength(30.0F, 6.0F)`**（destroyTime 30.0，explosionResistance 6.0） | 77 |
| 工具要求 | `requiresCorrectToolForDrops()` | 78 |
| 音效 | **`SoundType.GRASS`** | 79 |
| 方块类 | `Block::new`（无自定义子类） | 74 |
| 挖掘工具标签 | `farmstorm:compressed_hay_block` 在 `#minecraft:mineable/pickaxe` 内 | `src/generated/resources/data/minecraft/tags/block/mineable/pickaxe.json:11` |
| 钻石工具需求 | `#minecraft:needs_diamond_tool` **只有** `farmstorm:compressed_hay_block` 一条 | `src/generated/resources/data/minecraft/tags/block/needs_diamond_tool.json:3` |

---

## 附：未能确认清单

1. **`ffprobe` 不存在**（PATH 与 `D:\dsh` 递归均无）。所有音频时长/码率/采样率是用 **ffmpeg 7.1 `-i`** 读到的；mp3 时长 ffmpeg 自己提示「Estimating duration from bitrate, this may be inaccurate」，未用逐帧解析复核。
2. **重跑 `tools/gen_textures.py` 的产物与当前 `wild_apple.png` 是否逐字节相等** —— 未验证。本次扫描禁止改文件，因此没有实际执行生成器。可确证的只有：路径相同（会覆盖）、`add_plus=True`（会重新叠「+」）、`TEXTURE_SIZE=16`（输出 16×16）、源图默认是桌面 1294×1216 那张。
3. **`outpost_core_{top,side,bottom}.png` 的颜色数 / 透明像素数** —— 未统计（本次只按要求测了尺寸与字节数）。
4. **`.scratch/*.py` 里 PIL 相关脚本的具体能力** —— 只按文件名与 grep 命中登记，未逐个通读全部 20+ 个文件。
5. **本机是否有其它可用的 `ffmpeg`（PATH 之外的 GUI 版 / winget 包 / 其他盘）** —— 只递归搜了 `D:\dsh` 与 `C:\` 深度 3，未做全盘扫描。
6. **`docs/en` 与 `docs/zh` 的 `wild_apple.png` 数值（11 色 / 126 透明）为何与实测（88 色 / 107 透明 / 含 33 px 绿色「+」）不一致** —— 只记录了不一致这一事实，未追溯成因。
7. **`tools/gen_textures.py` 的 `--list` 实际输出条数** —— 未执行（需 python + Pillow，本机 `python` 不存在）。数字取自源码常量与 docstring（99 / 91 两处不一致）。
8. **`farmstorm:friend_chives` 事件在 `sounds.json` 未设置 `attenuation_distance` 时的实际可闻半径** —— 代码侧按 `音量 = soundRange/16 = 2.0` 计算为 32 格（`FriendChivesProjectile.java:30-33, 74`），但未在游戏内实测。
9. **`man瓜` 的 `plan0.0.1.txt:95` 写的「切比雪夫距离为 5」与现行 config 默认 15 的差异** —— 只列出两处数值（`plan0.0.1.txt:95` = 5；`FarmstormConfig.java:424` = 15），未判断哪一个是现行口径来源。
