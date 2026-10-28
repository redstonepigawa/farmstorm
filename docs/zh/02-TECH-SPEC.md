# 02 · 技术规格（Technical Specification）

> 对象：农场风云 / Farmstorm v0.0.1（NeoForge 21.1.x / Minecraft 1.21.1 / Java 21）
> 唯一真相源：`docs/zh/00-DECISIONS.md`（下称 DEC）。本文件如与 DEC 冲突，以 DEC 为准。
> 本文件只描述**架构与契约**，不交付完整实现；允许出现骨架片段与签名。
> 侧写说明：`01-DESIGN.md` §3.8（配置项全量清单）已落盘，本文件 §4.11 的键名 / 默认值 / 取值范围与之逐项对齐（共 117 项）；两份文档冲突时以 DEC 为裁决，配置键名以 §3.8 为准。

---

## 0. 分诊与本文范围

| 项 | 结论 |
|---|---|
| 复杂度档位 | L2（跨模块、多交付物、存在隐含假设） |
| 本文件负责 | 构建基线、目录结构、注册总表、类契约、服务端权威模型、资源清单、性能与安全、风险 |
| 本文件不负责 | 玩法数值的策划表述（见 01-DESIGN.md）、任务拆分（见 03-TASKS.md）、材质像素稿（见 tools/gen_textures.py） |

### 0.1 本文件新引入的技术决策（DEC 未覆盖的部分）

DEC 已把玩法层面收敛完毕，但工程层面仍有若干空白，本文用 `T-xx` 编号补齐。**这些决策不改动玩法，只决定实现形态**，若作者否决，改动面集中在单点。

| ID | 技术决策 | 理由 |
|---|---|---|
| T-01 | 「种植形态」实现为 14 个独立的 `FarmstormCropBlock` 方块（`<id>_crop`），不额外注册对应 BlockItem；播种由物品的 `useOn` 直接 `level.setBlock` 完成 | DEC D-08 要求「收获时按原版比例同时掉落作物与播种物品，闭环自给」，这必须有可收获的自定义作物方块；单一方块 + BlockState 枚举会让 15 种原版种植规则（耕地/沙/菌丝、光照、生长条件）挤在一个类里，得不偿失 |
| T-02 | 部署型作物（`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus`）的物品是**部署方块的 BlockItem**；同一个物品在「对着耕地使用」时改为放置自己的 `_crop` 方块 | DEC §4 规定部署型方块 ID 与物品 ID 同名；一个 ItemStack 只能是一种 BlockItem，因此「部署」与「播种」必须在 `useOn` 内分流 |
| T-03 | 火把花 / 瓶子草的「不能被非己方玩家破坏」由 `Block#getDestroyProgress` 返回 `0.0F` + `playerWillDestroy` 二次校验实现；对创造模式玩家同样生效 | 客户端也要算出 0 进度才不会出现「挖了半天突然回弹」的体感；服务端仍需二次校验防改包 |
| T-04 | DEC D-13「炮台类固定硬挖 10 秒」只作用于 `pumpkin_turret`；火把花 / 瓶子草按 D-14「己方可正常挖取」执行 | D-14 比 D-13 更具体且点名了这两者；两条同时生效会导致己方也要挖 10 秒，与 D-14「正常挖取」矛盾。裁决顺序：具体条款优先于泛化条款。**已由 DEC C-8 背书** |
| T-05 | 膨胀麦的方块替换跳过「爆炸抗性 ≥ 3600000」的方块（基岩 / 屏障 / 命令方块 / 末地传送门框架等） | `plan.txt` 只说「3×3×3 包括空气方块」，字面执行会删掉基岩与屏障，破坏服务器保护结构。这是防破坏底线，不构成平衡调整（D-21 只锁玩法数值） |
| T-06 | 自定义伤害类型 2 个：`farmstorm:void_bolt`（炮台导弹）、`farmstorm:crop_blast`（作物爆炸） | `plan.txt` 的「虚空伤害」无法用原版 `explosion` / `generic` 表达；爆炸用独立类型才能给出「被 X 的作物炸飞」的死亡信息并正确归属击杀 |
| T-07 | 不引入 Mixin | 全部需求都能通过继承 + NeoForge 事件总线 + 原生扩展点完成；无 Mixin 意味着崩溃日志可读、与其他模组冲突面最小、无需 `farmstorm.mixins.json` |
| T-08 | 临时附魔用 **DataComponent** `farmstorm:temporary_enchantments` 记录（附魔 / 授予等级 / 原等级 / 到期 tick），不用玩家持久数据 | DEC D-15 要求「离开范围或到期自动移除，保留原有附魔不被覆盖」；记录在物品上可随物品移动、掉落、进入箱子，避免玩家把剑塞箱子里绕过回收 |

---

## 1. 构建基线

### 1.1 gradle.properties（关键项）

```properties
# --- 平台 ---
minecraft_version=1.21.1
neo_version=21.1.213
neo_version_range=[21.1.0,)
loader_version_range=[4,)

# --- 模组 ---
mod_id=farmstorm
mod_name=Farmstorm
mod_license=All Rights Reserved
mod_version=0.0.1
mod_group_id=com.dashpig.farmstorm
mod_authors=dashpig
mod_description=Vanilla crops turned into PvP gadgets.

# --- 构建 ---
org.gradle.jvmargs=-Xmx3G
org.gradle.parallel=true
org.gradle.caching=true
```

`neo_version` 取值口径：写在本文件的是**可核实到的最新 21.1.x 构件**，正式构建时以 `https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml` 中当前的 21.1.x 最大稳定版为准（理由与出处见 §10 版本核查）。**不要**在 `gradle.properties` 里写 `21.1.+` 之类动态版本——构建不可复现。

### 1.2 build.gradle 骨架

```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.148'   // 见 §10：2.0.x 线的可核实构件
}

version = mod_version + "+mc" + minecraft_version
group = mod_group_id

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)   // DEC：Java 21
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
}

neoForge {
    // 目标 NeoForge 版本；下载与映射由插件负责
    version = neo_version

    // MDG 默认使用官方（Mojang）映射，DEC 要求 official，因此此处不指定 parchment
    // parchment { }  // 需要参数名时再加

    runs {
        client { client() }
        server { server() }
        data   { data() }   // DataGen：GatherDataEvent
    }

    mods {
        farmstorm {
            sourceSet sourceSets.main
        }
    }
}
```

**DSL 名注意**：任务书写的是 `neoforge { }`，ModDevGradle 的实际扩展块为 `neoForge { }`（大小写敏感）。若构建时报 `Could not find method neoForge()` / `neoforge()`，以插件自带的 DSL 提示为准，并回写本文件——这属于「以构建时实际拉取为准」的范围。

### 1.3 neoforge.mods.toml 要点

```toml
modLoader = "javafml"
loaderVersion = "[4,)"
license = "All Rights Reserved"

[[mods]]
modId = "farmstorm"
version = "${file.jarVersion}"
displayName = "Farmstorm"
authors = "dashpig"
description = '''Vanilla crops, upgraded into PvP gadgets.'''

[[dependencies.farmstorm]]
modId = "neoforge"
type = "required"
versionRange = "[21.1.0,)"
ordering = "NONE"
side = "BOTH"

[[dependencies.farmstorm]]
modId = "minecraft"
type = "required"
versionRange = "[1.21.1]"
ordering = "NONE"
side = "BOTH"
```

### 1.4 构建产物与验收门槛

| 命令 | 期望 |
|---|---|
| `./gradlew build` | 产出 `build/libs/farmstorm-0.0.1+mc1.21.1.jar`，无警告级以上的 deprecation |
| `./gradlew runData` | 生成 `src/generated/resources` 下的模型 / 方块状态 / 配方 / 战利品表 / 语言文件 |
| `./gradlew runServer` | 专用服务器可独立启动（DEC D-23：客户端只装资源包） |
| `./gradlew runClient` | 创造标签页出现 22 个物品、转换器 GUI 可打开 |

---

## 2. 项目目录结构

```
src/main/java/com/dashpig/farmstorm/
├── Farmstorm.java                     主类：MOD_ID、mod 总线事件注册、注册表汇总入口
├── registry/                          注册层：只放 DeferredRegister 与 Supplier，不含逻辑
│   ├── FarmstormItems.java            22 个 Item（15 作物 + 6 播种物 + 1 转换器）
│   ├── FarmstormBlocks.java           20 个 Block（14 作物 + 5 部署 + 1 转换器）
│   ├── FarmstormBlockEntities.java    6 个 BlockEntityType
│   ├── FarmstormEntityTypes.java      9 个 EntityType（8 投掷物 + 1 炮台导弹）
│   ├── FarmstormMenus.java            1 个 MenuType
│   ├── FarmstormRecipes.java          1 RecipeType + 1 RecipeSerializer
│   ├── FarmstormDataComponents.java   1 个 DataComponentType
│   ├── FarmstormCreativeTabs.java     1 个 CreativeModeTab
│   └── FarmstormDamageTypes.java      2 个 DamageType 的 ResourceKey + 取用工具
├── config/
│   └── FarmstormConfig.java           服务端 ModConfigSpec（全部数值 + 缓存烘焙）
├── item/                              物品行为
│   ├── ModifiedCropItem.java          基类：公共 tooltip、CropProfile 持有、播种分流
│   ├── ThrowableCropItem.java         投掷型：use() 生成对应投掷物实体
│   ├── DeployableCropItem.java        部署型：BlockItem 子类，useOn 分流「部署 / 播种」
│   ├── SeedItem.java                  独立播种物：ItemNameBlockItem 子类
│   └── FoodCropItem.java              食用型：FoodProperties + 进食完成回调
├── block/
│   ├── FarmstormCropBlock.java        14 个作物方块的唯一实现（CropBlock 子类）
│   ├── DeployedCropBlock.java         5 个部署方块的唯一实现（附着 / 悬空 / 硬挖）
│   └── ConverterBlock.java            农产品转换器方块（含 MenuProvider 契约）
├── blockentity/
│   ├── DeployedCropBlockEntity.java   部署物状态机 + 行为分发 + 同步
│   └── ConverterBlockEntity.java      单槽容器 + 转换事务
├── behavior/                          部署行为（策略模式）
│   ├── DeployedBehavior.java          行为总接口：生命周期 + tick
│   ├── TrapBehavior.java              陷阱：触发条件
│   ├── TurretBehavior.java            炮台：索敌 + 开火
│   ├── AuraProvider.java              光环：半径 / 频率 / 施加（DEC C-4）
│   └── impl/
│       ├── BlastCarrotTrap.java       胡罗贝：切比雪夫 ≤1 自爆
│       ├── FangTrapCactusTrap.java    赢过输过没死过：欧氏 ≤3 两圈尖牙
│       ├── PumpkinTurret.java         man瓜：切比雪夫 ≤5 导弹
│       ├── TorchflowerAura.java       火把花：半径 20 增益 + 临时火焰附加
│       └── PitcherPlantAura.java      瓶子草：半径 10 增益 + 净化负面
├── entity/
│   ├── ThrownCropEntity.java          投掷物基类（ThrowableItemProjectile 子类）
│   ├── TurretBullet.java              炮台导弹（自定义，非原版潜影贝子弹）
│   └── thrown/
│       ├── PuffedWheatProjectile.java        膨胀麦：干草块填充
│       ├── UbiServerProjectile.java          育碧服务器：威力 4 爆炸
│       ├── ToxicUbiServerProjectile.java     毒育碧：爆炸 + 药水云
│       ├── MelonSliceProjectile.java         瓜片子：威力 2 且封顶 6 伤害
│       ├── MelonGrenadeProjectile.java       瓜！：4 片西瓜片 + 威力 3
│       ├── BambooFirecrackerProjectile.java  爆竹：140 tick 风弹雨
│       ├── RedMushroomPuffProjectile.java    红菇：反胃 / 失明
│       └── BrownMushroomPuffProjectile.java  棕菇：虚弱 / 缓慢
├── explosion/
│   ├── FarmstormExplosion.java        自定义爆炸：三段式（破坏 / 伤害 / 表现）
│   └── BlastRule.java                 破坏性谓词 + 抗性阈值 + 掉落策略
├── team/
│   ├── TeamResolver.java              己方判定抽象（DEC D-06）
│   └── ScoreboardTeamResolver.java    v0.0.1 实现：原版计分板队伍
├── recipe/
│   ├── ConverterRecipe.java           Recipe<SingleRecipeInput>
│   └── ConverterRecipeSerializer.java StreamCodec + MapCodec
├── menu/
│   └── ConverterMenu.java             1 输入 + 1 只读输出 + 转换按钮
├── client/                            仅客户端：模型 / 屏 / 渲染
│   ├── FarmstormClient.java           @EventBusSubscriber(Client)：菜单屏注册
│   ├── ConverterScreen.java           转换器 GUI
│   └── render/TurretBulletRenderer.java 复用潜影贝导弹模型
├── util/
│   ├── TempEnchantHandler.java        火焰附加的给予 / 撤销（DEC D-15）
│   ├── LuckyBerryRoller.java          幸运浆果 8 档概率表（DEC D-17/D-18）
│   ├── DistanceMetrics.java           切比雪夫 / 欧氏工具（DEC D-20）
│   ├── DeployedOwnership.java         归属读写（UUID + 名字；队伍实时解析）
│   └── CropProfile.java               单作物的静态档案（ID / 播种物 / 行为 / 部署面）
├── event/
│   ├── FarmstormModEvents.java        mod 总线：GatherData、BuildCreativeTabContents
│   └── FarmstormGameEvents.java       game 总线：PlayerTick、PlayerLoggedIn/Out、爆炸拦截
└── datagen/                           DataGen Provider（仅 runData 时加载）
    ├── FarmstormDataGen.java          GatherDataEvent 汇总
    ├── ModBlockStateProvider.java     20 个 blockstate + 62 个方块模型
    ├── ModItemModelProvider.java      22 个物品模型
    ├── ModRecipeProvider.java         20 条双向转换配方
    ├── ModLootTableProvider.java      20 张战利品表（含原版比例掉落）
    ├── ModBlockTagsProvider.java      deployed_crops / explosion_immune
    ├── ModItemTagsProvider.java       modified_crops / seeds
    ├── ModDamageTypeTagsProvider.java 把 2 个伤害类型并入 minecraft 标签
    ├── ModLangProvider.java           zh_cn / en_us
    └── ModAdvancementProvider.java    12 个进度
```

**分包原则**：`registry` 只做注册，`item/block/entity` 只做行为，`util` 不做注册。

---

## 3. 注册体系总表

统计口径：**registry 对象 64 个**（跨 10 个 registry），另有 1 个 `ModConfigSpec`（非 registry，配置文件 `config/farmstorm-server.toml`）。每个条目均给出完整 ID。

### 3.1 items — 22 个

命名空间统一 `farmstorm`。堆叠数遵循 DEC D-16（投掷型 16，其余 64）。

**A. 15 个作物物品（DEC §4 原样）**

| # | ID | 中文 | 类 | 堆叠 | 播种物 |
|---|---|---|---|---|---|
| 1 | `farmstorm:puffed_wheat` | 膨胀麦 | `ThrowableCropItem` | 16 | `puffed_wheat_seeds` |
| 2 | `farmstorm:blast_carrot` | 胡罗贝 | `DeployableCropItem`（= 部署方块 BlockItem） | 64 | 同名（自身） |
| 3 | `farmstorm:ubi_server` | 育碧服务器 | `ThrowableCropItem` | 16 | 同名（自身） |
| 4 | `farmstorm:toxic_ubi_server` | 毒育碧服务器 | `ThrowableCropItem` | 16 | 同名（自身，D-32：独立作物方块 + 自身即播种物） |
| 5 | `farmstorm:power_beet` | 甜菜！ | `FoodCropItem` | 64 | `power_beet_seeds` |
| 6 | `farmstorm:melon_slice_bomb` | 瓜片子 | `ThrowableCropItem` | 16 | 无（不可种） |
| 7 | `farmstorm:melon_grenade` | 瓜！ | `ThrowableCropItem` | 16 | `melon_grenade_seeds` |
| 8 | `farmstorm:pumpkin_turret` | man瓜 | `DeployableCropItem`（= 部署方块 BlockItem） | 64 | `pumpkin_turret_seeds` |
| 9 | `farmstorm:awakened_torchflower` | 激活的火把花 | `DeployableCropItem`（= 部署方块 BlockItem） | 64 | `awakened_torchflower_seeds` |
| 10 | `farmstorm:awakened_pitcher_plant` | 激活的瓶子草 | `DeployableCropItem`（= 部署方块 BlockItem） | 64 | `awakened_pitcher_plant_pod` |
| 11 | `farmstorm:fang_trap_cactus` | 赢过输过没死过 | `DeployableCropItem`（= 部署方块 BlockItem） | 64 | 同名（自身） |
| 12 | `farmstorm:bamboo_firecracker` | 爆竹 | `ThrowableCropItem` | 16 | 同名（自身） |
| 13 | `farmstorm:red_mushroom_puff` | ？！菇菇！？ | `ThrowableCropItem` | 16 | 同名（自身） |
| 14 | `farmstorm:brown_mushroom_puff` | ？！棕色菇菇色棕！？ | `ThrowableCropItem` | 16 | 同名（自身） |
| 15 | `farmstorm:lucky_berry` | 幸运浆果 | `FoodCropItem` | 64 | 同名（自身） |

**B. 6 个独立播种物品**

| ID | 中文 | 类 | 堆叠 | 种出的方块 |
|---|---|---|---|---|
| `farmstorm:puffed_wheat_seeds` | 膨胀麦种子 | `SeedItem` | 64 | `farmstorm:puffed_wheat_crop` |
| `farmstorm:power_beet_seeds` | 甜菜种子（甜菜！） | `SeedItem` | 64 | `farmstorm:power_beet_crop` |
| `farmstorm:melon_grenade_seeds` | 瓜！种子 | `SeedItem` | 64 | `farmstorm:melon_grenade_stem` |
| `farmstorm:pumpkin_turret_seeds` | man瓜种子 | `SeedItem` | 64 | `farmstorm:pumpkin_turret_stem` |
| `farmstorm:awakened_torchflower_seeds` | 激活火把花种子 | `SeedItem` | 64 | `farmstorm:awakened_torchflower_crop` |
| `farmstorm:awakened_pitcher_plant_pod` | 激活瓶子草豆荚 | `SeedItem` | 64 | `farmstorm:awakened_pitcher_plant_crop` |

**C. 1 个方块物品**

| ID | 中文 | 类 | 堆叠 |
|---|---|---|---|
| `farmstorm:converter` | 农产品转换器 | `BlockItem` | 64 |

**D. 素材物品**：v0.0.1 **不注册任何新素材物品**。转换成本用原版资源（经验值、下界之星），DEC D-09 已明确改良作物不参与原版配方，因此不需要中间产物。

> 交叉校验：15 个作物物品全部在上表出现（§11 验收项 1）。

### 3.2 blocks — 20 个

| 组 | ID | 类 | 备注 |
|---|---|---|---|
| 作物（14） | `farmstorm:puffed_wheat_crop` | `FarmstormCropBlock` | 无 BlockItem（T-01） |
| | `farmstorm:blast_carrot_crop` | `FarmstormCropBlock` | |
| | `farmstorm:ubi_server_crop` | `FarmstormCropBlock` | |
| | `farmstorm:toxic_ubi_server_crop` | `FarmstormCropBlock` | |
| | `farmstorm:power_beet_crop` | `FarmstormCropBlock` | |
| | `farmstorm:melon_grenade_stem` | `FarmstormCropBlock` | 茎方块（原版参照：西瓜茎） |
| | `farmstorm:pumpkin_turret_stem` | `FarmstormCropBlock` | 茎方块（原版参照：南瓜茎） |
| | `farmstorm:awakened_torchflower_crop` | `FarmstormCropBlock` | |
| | `farmstorm:awakened_pitcher_plant_crop` | `FarmstormCropBlock` | |
| | `farmstorm:fang_trap_cactus_block` | `FarmstormCropBlock` | 沙 / 红沙 |
| | `farmstorm:bamboo_firecracker_stalk` | `FarmstormCropBlock` | |
| | `farmstorm:red_mushroom_puff_block` | `FarmstormCropBlock` | 低光照 |
| | `farmstorm:brown_mushroom_puff_block` | `FarmstormCropBlock` | 低光照 |
| | `farmstorm:lucky_berry_bush` | `FarmstormCropBlock` | 草方块（甜浆果丛同款） |
| 部署（5） | `farmstorm:blast_carrot` | `DeployedCropBlock` | 仅顶面 |
| | `farmstorm:pumpkin_turret` | `DeployedCropBlock` | 顶 / 底 / 侧，可悬空 |
| | `farmstorm:awakened_torchflower` | `DeployedCropBlock` | 仅顶面，可悬空，爆炸免疫 |
| | `farmstorm:awakened_pitcher_plant` | `DeployedCropBlock` | 仅顶面，可悬空，爆炸免疫 |
| | `farmstorm:fang_trap_cactus` | `DeployedCropBlock` | 仅顶面 |
| 功能（1） | `farmstorm:converter` | `ConverterBlock` | 单槽 GUI，带朝向 |

`melon_slice_bomb` 无作物方块（DEC §4：不可种），故作物方块为 14 而非 15。

### 3.3 block_entities — 6 个

| ID | 类 | 说明 |
|---|---|---|
| `farmstorm:blast_carrot` | `DeployedCropBlockEntity` | 同一个类注册 5 次，靠 `BlockEntityType` 区分 |
| `farmstorm:pumpkin_turret` | `DeployedCropBlockEntity` | |
| `farmstorm:awakened_torchflower` | `DeployedCropBlockEntity` | |
| `farmstorm:awakened_pitcher_plant` | `DeployedCropBlockEntity` | |
| `farmstorm:fang_trap_cactus` | `DeployedCropBlockEntity` | |
| `farmstorm:converter` | `ConverterBlockEntity` | 单槽容器 |

> 作物方块（`FarmstormCropBlock`）**不使用** BlockEntity：生长状态全在 BlockState，节省内存与同步带宽。

### 3.4 entity_types — 9 个

全部 `MobCategory.MISC`，`sized(0.25F, 0.25F)`（导弹 0.3F），`clientTrackingRange(4)`，`updateInterval(10)`。

| ID | 类 | 对应物品 |
|---|---|---|
| `farmstorm:puffed_wheat` | `PuffedWheatProjectile` | 膨胀麦 |
| `farmstorm:ubi_server` | `UbiServerProjectile` | 育碧服务器 |
| `farmstorm:toxic_ubi_server` | `ToxicUbiServerProjectile` | 毒育碧服务器 |
| `farmstorm:melon_slice_bomb` | `MelonSliceProjectile` | 瓜片子（也被「瓜！」用作子弹） |
| `farmstorm:melon_grenade` | `MelonGrenadeProjectile` | 瓜！ |
| `farmstorm:bamboo_firecracker` | `BambooFirecrackerProjectile` | 爆竹 |
| `farmstorm:red_mushroom_puff` | `RedMushroomPuffProjectile` | ？！菇菇！？ |
| `farmstorm:brown_mushroom_puff` | `BrownMushroomPuffProjectile` | ？！棕色菇菇色棕！？ |
| `farmstorm:turret_bullet` | `TurretBullet` | 无（man瓜 炮台发射） |

「瓜！」的 4 发子弹复用 `farmstorm:melon_slice_bomb` 实体类型（携带 `submunition` 标记），不额外注册。

### 3.5 recipe types / serializers — 各 1 个

| registry | ID | 说明 |
|---|---|---|
| `Registries.RECIPE_TYPE` | `farmstorm:converting` | `RecipeType<ConverterRecipe>` |
| `Registries.RECIPE_SERIALIZER` | `farmstorm:converting` | `ConverterRecipeSerializer` |

### 3.6 creative tabs — 1 个

| ID | 标题 lang key | 内容 |
|---|---|---|
| `farmstorm:main` | `itemGroup.farmstorm.main` | 22 个物品按「作物 → 播种物 → 工具方块」排序 |

### 3.7 damage types — 2 个

数据驱动：注册表 `Registries.DAMAGE_TYPE` 的实例由数据包 JSON 提供，代码侧只持有 `ResourceKey<DamageType>` 常量（`DeferredRegister` 不参与实例注册）。

| ID | 用途 | 关键属性 |
|---|---|---|
| `farmstorm:void_bolt` | man瓜 导弹命中（「1 点虚空伤害」） | `exhaustion=0.0`、`scaling=when_caused_by_living_non_player`、`effects=hurt`；并入 `#minecraft:is_projectile` |
| `farmstorm:crop_blast` | 所有作物爆炸的伤害来源 | `exhaustion=0.1`、`effects=hurt`；并入 `#minecraft:is_explosion`（爆炸保护 / 抗性附魔据此生效） |

### 3.8 data components — 1 个

| ID | 类型 | 用途 |
|---|---|---|
| `farmstorm:temporary_enchantments` | `List<TempEnchantRecord>`（`Codec` + `StreamCodec`） | DEC D-15 临时附魔账本 |

`record TempEnchantRecord(Holder<Enchantment> enchantment, int granted, int previous, long expireAt)`。

### 3.9 menu types — 1 个

| ID | 类 | 说明 |
|---|---|---|
| `farmstorm:converter` | `ConverterMenu` | 客户端屏 `ConverterScreen`（不占 registry） |

### 3.10 config — 1 个

| 项 | 值 |
|---|---|
| 文件 | `config/farmstorm-server.toml` |
| 类型 | `ModConfig.Type.SERVER` |
| 规格对象 | `FarmstormConfig.SPEC` |
| 注册时机 | 模组构造器内 `modContainer.registerConfig(...)` |
| 条目数 | 见 §4.11（共 117 个键） |

### 3.11 计数汇总

| registry | 数量 |
|---|---|
| items | 22 |
| blocks | 20 |
| block_entity_type | 6 |
| entity_type | 9 |
| recipe_type | 1 |
| recipe_serializer | 1 |
| creative_mode_tab | 1 |
| damage_type（数据驱动，代码侧 2 个 key） | 2 |
| data_component_type | 1 |
| menu_type | 1 |
| **registry 合计** | **64** |
| config spec（非 registry） | 1 |
| **总计** | **65** |

---

## 4. 核心类设计

本节给出每个关键类的**职责 / 继承关系 / 关键方法签名 / 状态字段**。签名以 1.21.1 + NeoForge 21.1.x 为准；版本敏感的 API（`FoodProperties.Builder`、`PotionContents`、`EnchantmentHelper`）标注「以构建时为准」。

### 4.1 `Farmstorm`（主类）

- **职责**：模组入口；持有 `MOD_ID` / `LOGGER` / 全局 `TeamResolver` 单例；把 8 个 `DeferredRegister` 挂到 mod 事件总线；注册配置；挂载 DataGen 与创造标签页事件。
- **继承**：无（`@Mod(Farmstorm.MOD_ID)` 标注的 `final class`）。
- **状态字段**：全部 `static final`，无实例状态。

```java
@Mod(Farmstorm.MOD_ID)
public final class Farmstorm {
    public static final String MOD_ID = "farmstorm";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final TeamResolver TEAMS = new ScoreboardTeamResolver();

    public Farmstorm(IEventBus modBus, ModContainer container) {
        FarmstormItems.REGISTER.register(modBus);
        FarmstormBlocks.REGISTER.register(modBus);
        FarmstormBlockEntities.REGISTER.register(modBus);
        FarmstormEntityTypes.REGISTER.register(modBus);
        FarmstormMenus.REGISTER.register(modBus);
        FarmstormRecipes.REGISTER.register(modBus);
        FarmstormDataComponents.REGISTER.register(modBus);
        FarmstormCreativeTabs.REGISTER.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, FarmstormConfig.SPEC);

        modBus.addListener(FarmstormModEvents::onGatherData);
        modBus.addListener(FarmstormModEvents::onBuildCreativeTab);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
```

> 说明：1.21 起 `ResourceLocation` 构造器私有，必须用 `fromNamespaceAndPath`；NeoForge 1.20.5+ 支持 `@Mod` 构造器注入 `IEventBus` 与 `ModContainer`，因此不需要 `FMLJavaModLoadingContext`。

### 4.2 `TeamResolver` + `ScoreboardTeamResolver`（DEC D-06 / C-9）

- **职责**：把「这个玩家是不是部署者的己方」从玩法代码里抽离；v0.0.1 用原版计分板队伍实现，后续可换 FTB Teams 而不动调用点。
- **继承**：接口 + 单一实现。
- **判定时机（C-9）**：部署物只记身份、不记队伍；**每一次触发 / 索敌的瞬间**实时解析归属者的当前队伍。队伍变更因此**即时生效**——A 部署后加入队伍 X，队友 B 立刻免疫；A 退队，B 立刻恢复敌对。

```java
public interface TeamResolver {
    /** 归属身份：UUID 必填；name 仅用于离线时在 Scoreboard 成员表反查（可为 null）。不含任何队伍信息。 */
    record Owner(UUID uuid, @Nullable String name) {}

    /** 己方判定：在触发瞬间实时解析 owner 的当前队伍，再与 target 的当前队伍比对。 */
    boolean isAlly(ServerLevel level, Owner owner, Player target);

    /** 实时队伍名（在线 / 离线都可用）；无队伍返回 empty。 */
    Optional<String> teamName(ServerLevel level, Owner owner);
}
```

```java
public final class ScoreboardTeamResolver implements TeamResolver {
    @Override public Optional<String> teamName(ServerLevel level, Owner owner) {
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = owner.name() == null ? null : scoreboard.getPlayersTeam(owner.name());
        return team == null ? Optional.empty() : Optional.of(team.getName());
    }

    @Override public boolean isAlly(ServerLevel level, Owner owner, Player target) {
        if (owner.uuid().equals(target.getUUID())) return true;       // 部署者本人
        Optional<String> ownerTeamName = teamName(level, owner);      // 实时解析
        if (ownerTeamName.isEmpty()) return false;                    // D-31 / G-8：无队伍 → self 口径
        PlayerTeam targetTeam = target.getTeam();                     // 1.21：Entity#getTeam()
        return targetTeam != null && ownerTeamName.get().equals(targetTeam.getName());
    }
}
```

**关键约定**：

- 部署方块只存 `ownerUuid` + `ownerName`（C-9），**不存队伍**，也不存 `PlayerTeam` 引用（队伍对象在存档重载后失效）。
- `Scoreboard#getPlayersTeam(String)` 是「名字 → 队伍」的哈希查找，O(1)，且**对离线玩家同样有效**——成员表按名字索引，不依赖实体在线。这正是必须存 `ownerName` 的唯一理由。
- **离线 / 已删除的归属者**：离线时按其最后已知队伍照常判定；若名字不在任何成员表（被删档、改名、或从未入队）→ 视为无队伍 → 走 `self` 口径（D-31 / G-8）：只有部署者本人是己方，其余玩家全部算「非己方」，即对全部在线玩家生效（与 `01-DESIGN.md` §7.2 一致）。
- **改名**：只影响 `ownerName` 的展示与反查命中率；判定主键仍是 `ownerUuid`（部署者本人永远免疫自家陷阱）。
- **缓存**：同一 tick 内以 `ownerName` 为键缓存 `teamName` 结果，`LevelTickEvent.Post` 清空——避免 20 格光环在同一 tick 对同一归属者重复查表。

### 4.3 物品体系

继承树：

```
Item
├── ModifiedCropItem                 （作物物品基类：播种分流 + tooltip + CropProfile）
│   ├── ThrowableCropItem            （投掷型：8 个）
│   └── FoodCropItem                 （食用型：甜菜！ / 幸运浆果）
├── DeployableCropItem extends BlockItem   （部署型：5 个，同时是部署方块的物品）
└── SeedItem extends ItemNameBlockItem     （6 个独立播种物）
```

#### 4.3.1 `ModifiedCropItem`

- **职责**：15 个作物物品的共同父类。持有静态档案 `CropProfile`；统一 tooltip；提供「对着可种植面就播种」的分流；统一禁止原版用途（DEC D-09）。
- **关键方法**：

```java
public class ModifiedCropItem extends Item {
    private final CropProfile profile;

    public ModifiedCropItem(CropProfile profile, Item.Properties props) { super(props); this.profile = profile; }

    public CropProfile profile() { return this.profile; }

    /** 播种分流：命中可种植面则放置 profile.cropBlock()，否则交回默认行为。 */
    @Override public InteractionResult useOn(UseOnContext ctx);

    /** 是否允许在该位置播种（土壤 + 光照 + 空间）。 */
    protected boolean canPlantAt(UseOnContext ctx, BlockState soil);

    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext ctx,
                                          List<Component> tips, TooltipFlag flag);
}
```

- **状态字段**：`profile`（`CropProfile`：`ResourceLocation id`、`ResourceKey<Block> cropBlock`、`@Nullable ResourceKey<Item> seedItem`、`DeployedKind kind`、`Set<AttachFace> allowedFaces`、`int throwStack`）。`CropProfile` 是 `record`，15 个常量集中在 `util/CropProfile.java` 的 `CropProfile.ALL` 表里（不新增文件）。

#### 4.3.2 `ThrowableCropItem`

- **职责**：右键生成对应投掷物实体，消耗 1 个，进入短冷却；堆叠 16（D-16）。
- **关键方法**：

```java
public class ThrowableCropItem extends ModifiedCropItem {
    private final Supplier<EntityType<? extends ThrownCropEntity>> projectileType;

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand);

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }

    /** 允许子类改写初速与散布（瓜！ 的子母弹需要）。 */
    protected ThrownCropEntity createProjectile(ServerLevel level, Player player, ItemStack stack);

    protected float velocity() { return 1.5F; }
    protected float inaccuracy() { return 1.0F; }
}
```

`use()` 的实现顺序（服务端权威）：`if (level.isClientSide) return InteractionResultHolder.success(stack)` → 生成实体 → `entity.setItem(stack)` → `entity.shootFromRotation(player, xRot, yRot, 0.0F, velocity(), inaccuracy())` → `level.addFreshEntity(entity)` → `stack.consume(1, player)` → `player.getCooldowns().addCooldown(this, cfg.throwCooldown)` → `player.awardStat(Stats.ITEM_USED.get(this))`。

#### 4.3.3 `SeedItem`

- **职责**：6 个独立播种物的唯一实现，等价于 `ItemNameBlockItem`，只是把「目标方块」换成对应的作物方块（后缀为 `_crop` / `_stem` / `_stalk` / `_block` / `_bush` 之一）并补一个 `CropProfile` 以复用 tooltip。
- **关键方法**：只重写 `appendHoverText`；播种逻辑继承 `ItemNameBlockItem`。

```java
public class SeedItem extends ItemNameBlockItem {
    private final CropProfile profile;
    public SeedItem(CropProfile profile, Block cropBlock, Item.Properties props) { super(cropBlock, props); this.profile = profile; }
}
```

#### 4.3.4 `FoodCropItem`

- **职责**：食用型（甜菜！、幸运浆果）。`FoodProperties` 只承载营养 / 饱和度；**效果在 `finishUsingItem` 里施加**，因为幸运浆果需要服务端 RNG、甜菜！需要一次性施加 4 个效果。
- **关键方法**：

```java
public class FoodCropItem extends ModifiedCropItem {
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living);

    /** 子类实现：吃掉之后干什么（只在服务端被调用）。 */
    protected void onEaten(ItemStack stack, ServerLevel level, ServerPlayer player);
}
```

- **状态字段**：无额外字段；`FoodProperties` 通过 `Item.Properties#food(...)` 在注册时给定。

```java
// 注册示例（D-29：甜菜！与幸运浆果统一取 4 / 0.3，食用时长 32 刻）
ITEMS.registerItem("power_beet", p -> new FoodCropItem(CropProfile.POWER_BEET,
        p.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible().build())),
        new Item.Properties());
```

> `FoodProperties.Builder` 的方法名在 1.21.x 期间有过变动（`saturationModifier` / `saturation`、`alwaysEdible()` 是否带参），以构建时实际 API 为准。

#### 4.3.5 `DeployableCropItem`

- **职责**：5 个部署型作物的物品，同时是部署方块的 `BlockItem`（T-02）。`useOn` 里做二选一分流。
- **关键方法**：

```java
public class DeployableCropItem extends BlockItem {
    private final CropProfile profile;

    @Override public InteractionResult useOn(UseOnContext ctx) {
        // 1) 命中面可种植（脚下是耕地 / 沙 / 草）→ 播种 profile.cropBlock()
        // 2) 否则 super.useOn(ctx) → BlockItem 放置流程 → DeployedCropBlock#getStateForPlacement
        return super.useOn(ctx);
    }

    @Override protected boolean placeBlock(BlockPlaceContext ctx, BlockState state);
}
```

### 4.4 `DeployedCropBlock`

- **职责**：5 个部署方块的唯一实现。负责：附着面判定、悬空状态维护、归属校验、硬挖时长、掉落策略、tick 分发（把 tick 交给 BlockEntity）。
- **继承**：`BaseEntityBlock`（需要 BlockEntity + 方块状态 + codec）。
- **状态字段（BlockState 属性）**：

| 属性 | 类型 | 说明 |
|---|---|---|
| `FACE` | `EnumProperty<AttachFace>`（`FLOOR` / `CEILING` / `WALL`） | 附着面；`WALL` 时配合 `FACING` 决定朝向 |
| `FACING` | `DirectionProperty` | 仅 `WALL` 使用；`FLOOR` / `CEILING` 固定 `NORTH` |
| `FLOATING` | `BooleanProperty` | 依附方块消失后置 true（man瓜 / 火把花 / 瓶子草，plan 第 93 / 101 / 109 行） |

- **实例字段（方块级不可变配置）**：`DeployedKind kind`、`EnumSet<AttachFace> allowedFaces`、`boolean supportsFloating`、`boolean turretHardMine`（仅 `pumpkin_turret`，T-04）、`boolean explosionImmune`（火把花 / 瓶子草）、`boolean allyOnlyBreak`（同上两者）。
- **关键方法签名**：

```java
public class DeployedCropBlock extends BaseEntityBlock {
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FLOATING = BooleanProperty.create("floating");

    // 放置
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx);
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos);

    // 悬空维护
    @Override protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                               LevelAccessor level, BlockPos pos, BlockPos neighborPos);

    // 破坏规则（T-03 / T-04 / D-13 / D-14）
    @Override public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos);
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player);
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston);

    // 交互
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                        Player player, BlockHitResult hit);

    // BlockEntity 接线
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state);
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type);

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder);
    @Override protected MapCodec<? extends BaseEntityBlock> codec();
}
```

**放置规则（`getStateForPlacement`）**：

1. `ctx.getClickedFace()` 必须属于 `allowedFaces`（胡罗贝 / 仙人掌 / 火把花 / 瓶子草 = 仅 `FLOOR`；man瓜 = 三面）。
2. 依附方块必须是**完整不透明方块**：`support.isCollisionShapeFullBlock(level, supportPos) && support.canOcclude()`。这是 plan 第 11 条「必须是完整不透明方块」的唯一落点；半砖 / 玻璃 / 树叶直接拒绝。
3. 目标位置必须是可替换的空气类方块；且不得与实体碰撞。
4. `FLOATING` 初始为 `false`；`supportsFloating == false` 的方块永远不读该属性（属性统一注册，语义按字段决定，避免 5 个方块各写一套 `createBlockStateDefinition`）。

**悬空规则（`updateShape`）**：依附方块消失时——

- `supportsFloating == true` → `state.setValue(FLOATING, true)`，并把 BE 标记为 `detached = true`（行为照常运行）；
- 否则 → 返回 `Blocks.AIR.defaultBlockState()`（掉落由 `onRemove` + 战利品表处理）。

**方块行为（对齐 `01-DESIGN.md` §7.4 / §7.5）**：全部部署方块设 `PistonBehavior.BLOCK`——不可被活塞推动或拉动，否则 owner / 依附面 / 依附坐标三者会同时失配；未加载区块内的部署方块不 tick（陷阱不触发、炮台不开火、光环不刷新），随区块存档正常保存。

**破坏规则**：

| 方块 | 己方 / 创造 | 非己方 |
|---|---|---|
| `pumpkin_turret` | `getDestroyProgress` 返回 `1.0F / 200.0F`（固定 10 秒，无视工具与附魔） | 同左（10 秒也拦不住，但挖掘时会挨炮） |
| `awakened_torchflower` / `awakened_pitcher_plant` | 正常挖掘（返回 `-1F` 表示「用默认公式」） | 返回 `0.0F`，且 `playerWillDestroy` 里 `event.setCanceled(true)` |
| `blast_carrot` / `fang_trap_cactus` | 正常挖掘 | 正常挖掘（一次性陷阱，被拆掉就等于拆弹，接受） |

`getDestroyProgress` 返回 `0.0F` 时客户端也会算出零进度（BE 已同步到客户端，见 §4.5），因此体感一致、无需客户端预测补丁。

> 反编译级别的细节：`getDestroyProgress` 的返回值是「每 tick 的进度增量」，`1.0F / 200` 正好 200 tick（10 秒 @20tps）。

**`codec()`**：1.21 起 `Block` 强制实现 `codec()`；本类需要 `DeployedKind` 等参数，用 `RecordCodecBuilder.mapCodec` 暴露 `kind`（枚举 codec）与三个布尔，便于 `/setblock` 与数据生成复用。

### 4.5 `DeployedCropBlockEntity`

- **职责**：部署物的全部运行时状态与逻辑入口——归属记录、行为实例、tick 调度、同步、掉落保护。
- **继承**：`BlockEntity`（不实现 `Container`）。
- **状态字段**：

| 字段 | 类型 | 同步 | 说明 |
|---|---|---|---|
| `ownerUuid` | `@Nullable UUID` | 是 | 部署者；己方判定的主键（C-9） |
| `ownerName` | `String` | 是 | 部署者名字；离线时用于在 Scoreboard 成员表反查其最后已知队伍（§4.2），兼作 tooltip 展示 |
| `placedGameTime` | `long` | 是 | 部署时刻；用于 10 秒硬挖与冷却推进 |
| `behavior` | `DeployedBehavior` | 否（由 `kind` 推导） | 策略对象，静态单例，不参与序列化 |
| `cooldown` | `int` | 否 | 炮台开火 / 光环下一次生效的倒计时 |
| `pendingWaves` | `List<FangWave>` | 是 | 仙人掌陷阱的两圈尖牙延迟队列 |
| `fuseTicks` | `int` | 是 | 延迟引爆类用；0 表示无 |
| `detached` | `boolean` | 是 | 是否已悬空；影响粒子与 tooltip |

`record FangWave(long atTick, int count, double radius)`。

- **关键方法签名**：

```java
public class DeployedCropBlockEntity extends BlockEntity {
    public static final int HARD_MINE_TICKS = 200;

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeployedCropBlockEntity be);

    public void setOwner(Player placer);   // 只写入 ownerUuid + ownerName（C-9），不写队伍
    public boolean isAlly(Player player);                        // 内部走 Farmstorm.TEAMS
    public @Nullable ServerPlayer owner(ServerLevel level);       // 解析 UUID 为在线玩家
    public DeployedBehavior behavior();

    /** 一次性部署物（陷阱）自毁：先跑行为后置，再移除方块并播粒子。 */
    public void consume();

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries);
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries);
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries);
    @Override public Packet<ClientGamePacketListener> getUpdatePacket();
}
```

**tick 分发（`serverTick`）**：

```java
public static void serverTick(Level level, BlockPos pos, BlockState state, DeployedCropBlockEntity be) {
    if (!(level instanceof ServerLevel server)) return;             // 双保险：客户端永不 tick
    if (be.behavior == null) be.behavior = DeployedBehaviors.of(be.kind);

    long now = server.getGameTime();
    be.drainPendingWaves(server, pos, state, now);                  // 1) 延迟尖牙波
    if (be.cooldown > 0) { be.cooldown--; return; }                 // 2) 冷却
    if ((now + be.stagger) % be.behavior.scanInterval() == 0) {     // 3) 错峰主扫描
        be.behavior.tick(be);
    }
}
```

- `stagger = Math.floorMod(be.getBlockPos().hashCode(), be.behavior.scanInterval())`：把同 chunk 内多个部署物错开到不同 tick，避免同 tick 尖峰（§8.1）。

**归属与防拆**：`onRemove` 里若 `supportsFloating` 为假则按战利品表掉落；若因 `/setblock` 或结构生成导致 `ownerUuid == null`，该部署物对所有玩家都「非己方」（可被任何人拆，陷阱对所有人触发），这是可接受的降级。

### 4.6 `DeployedBehavior` 家族（DEC C-4）

```java
public interface DeployedBehavior {
    DeployedKind kind();
    /** 主扫描间隔（tick）。陷阱 / 炮台 10，光环 100。 */
    int scanInterval();
    void tick(DeployedCropBlockEntity be);
    default void onDeployed(DeployedCropBlockEntity be) {}
    default boolean canBeBrokenBy(Player player, DeployedCropBlockEntity be) { return true; }
}

public interface TrapBehavior extends DeployedBehavior {
    default int scanInterval() { return 10; }
    Optional<Player> findTrigger(DeployedCropBlockEntity be);
    void trigger(DeployedCropBlockEntity be, Player target);
    @Override default void tick(DeployedCropBlockEntity be) {
        findTrigger(be).ifPresent(t -> trigger(be, t));
    }
}

public interface TurretBehavior extends DeployedBehavior {
    default int scanInterval() { return 10; }
    Optional<Entity> findTarget(DeployedCropBlockEntity be);    // 玩家优先，可选投掷物（C-3）
    void fire(DeployedCropBlockEntity be, Entity target);
    int cooldownTicks();
}

public interface AuraProvider extends DeployedBehavior {
    default int scanInterval() { return 100; }                 // 5 秒
    double radius();                                           // 欧氏（DEC D-20）
    int effectDurationTicks();                                 // 15 秒 = 300 tick
    void apply(DeployedCropBlockEntity be, ServerPlayer player);
}
```

`AuraProvider` 的默认 `tick` 遍历 `ServerLevel#players()`（而不是 AABB 扫描实体）——20 格半径下玩家列表通常只有个位数，比 `getEntitiesOfClass` 便宜一个量级（§8.1）。

**5 个实现的行为表**（数值全部来自 `plan.txt`，逐条可配见 §4.11）：

| 实现 | 接口 | 判定几何 | 扫描 | 行为要点 |
|---|---|---|---|---|
| `BlastCarrotTrap` | `TrapBehavior` | 切比雪夫 ≤ 1 | 10 tick | 触发即 `FarmstormExplosion(power=2, cap=7)` + 对爆炸范围内（半径 = power × 2 = 4 格）**所有玩家**施加反胃 III 60 tick（plan 第 46 行；不筛敌我，D-27）；随后 `be.consume()` |
| `FangTrapCactusTrap` | `TrapBehavior` | 欧氏 ≤ 3.0（DEC C-5） | 10 tick | 触发后压入两波：外圈 8 个、延迟 10 tick（0.5 s）；内圈 5 个、延迟 4 tick（0.2 s）。尖牙用原版 `EntityType.EVOKER_FANGS`，owner = 部署者；几何取外圈半径 2.0 均分 8 点、内圈半径 1.0 均分 5 点（plan 未给半径）；两波出完 `be.consume()` |
| `PumpkinTurret` | `TurretBehavior` | 切比雪夫 ≤ 5 | 10 tick 扫描，开火间隔 60 tick（G-2 / D-28） | 目标优先级：非己方玩家 →（`interceptProjectiles` 为真时）非己方投掷物（DEC C-3）。开火生成 `TurretBullet`，命中造成 `farmstorm:void_bolt` 1.0 伤害 + 沿「南瓜 → 目标」方向击退 1.0，**不给漂浮**（plan 第 95 行） |
| `TorchflowerAura` | `AuraProvider` | 欧氏 20 | 100 tick，效果 300 tick | 力量 III / 抗性 II / 急迫 II / 速度 I / 水下呼吸 I / 夜视 I；手持（主手或副手）任意 `#minecraft:swords` → `TempEnchantHandler.grant(..., FIRE_ASPECT, 1, 300)`；每次施加播 `ParticleTypes.FLAME` ×8（`particlesPerRefresh`） |
| `PitcherPlantAura` | `AuraProvider` | 欧氏 10 | 100 tick，效果 300 tick | 生命回复 II / 饱和 II / 伤害吸收 I；遍历 `player.getActiveEffects()`，对 `MobEffect#getCategory() == MobEffectCategory.HARMFUL` 的全 `removeEffect`；播 `ParticleTypes.HEART` ×8（`particlesPerRefresh`） |

**「buff 类部署物不能被非己方破坏」的实现落点**：`DeployedCropBlock.allyOnlyBreak = true`（火把花 / 瓶子草），行为接口只保留 `canBeBrokenBy` 作为扩展点。破坏判定必须在方块层——`getDestroyProgress` 拿不到行为对象。

**罗马数字 → amplifier 换算**（全文统一）：plan 里的「N 级」= `amplifier = N - 1`。力量 3 → 2，抗性提升 2 → 1，反胃 5 → 4，速度 1 → 0。

### 4.7 `ThrownCropEntity` + 8 个子类

- **职责**：投掷物基类，统一客户端渲染（用物品模型）、统一 `onHit` 分发、统一「命中后立即 discard」。
- **继承**：`ThrowableItemProjectile`（自带 `ItemStack` 同步与 `getDefaultItem`）。
- **关键方法**：

```java
public abstract class ThrownCropEntity extends ThrowableItemProjectile {
    private boolean submunition;            // 瓜！的子母弹标记

    protected ThrownCropEntity(EntityType<? extends ThrownCropEntity> type, Level level);
    protected ThrownCropEntity(EntityType<? extends ThrownCropEntity> type, LivingEntity shooter, Level level);

    @Override protected Item getDefaultItem();
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder);
    @Override protected void onHitEntity(EntityHitResult result);
    @Override protected void onHitBlock(BlockHitResult result);
    @Override protected void readAdditionalSaveData(CompoundTag tag);
    @Override protected void addAdditionalSaveData(CompoundTag tag);

    /** 命中后的唯一行为入口；仅服务端调用。 */
    protected abstract void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit);

    public void setSubmunition(boolean value) { this.submunition = value; }
    public boolean isSubmunition() { return this.submunition; }
}
```

`onHitBlock` / `onHitEntity` 的公共部分由基类实现，子类不得重写：

```java
@Override protected void onHitBlock(BlockHitResult r) {
    super.onHitBlock(r);
    if (level() instanceof ServerLevel sl) {
        onImpact(sl, r.getLocation().add(Vec3.atLowerCornerOf(r.getDirection().getNormal()).scale(0.05)), null);
        if (keepAliveAfterImpact()) return;   // 爆竹重写为 true
        discard();
    }
}

protected boolean keepAliveAfterImpact() { return false; }
```

**子类行为表**：

| 子类 | 实体 ID | `onImpact` 具体行为 | 防御 / 边界 |
|---|---|---|---|
| `PuffedWheatProjectile` | `farmstorm:puffed_wheat` | 以命中方块为心，把 3×3×3 替换为 `Blocks.HAY_BLOCK`；若该范围内存在任意 `#minecraft:water` 方块 → 改为 5×5×5 | 跳过爆炸抗性 ≥ 3.6e6 的方块（T-05）；替换包含空气（plan 第 38 行） |
| `UbiServerProjectile` | `farmstorm:ubi_server` | `FarmstormExplosion(power=4.0, cap=2, rule=UNBREAKABLE_SKIP)` | 破障类不设抗性阈值，但永远跳过不可破坏方块 |
| `ToxicUbiServerProjectile` | `farmstorm:toxic_ubi_server` | 先 `FarmstormExplosion(power=2.0, cap=2)`，再在命中点生成 `AreaEffectCloud`（剧毒，半径 3.0、600 tick，G-4），等价「滞留型剧毒药水」 | `PotionContents` 构造在 1.21 变化较大 → 以构建时为准；退化方案：直接给云内玩家施加 `MobEffects.POISON` |
| `MelonSliceProjectile` | `farmstorm:melon_slice_bomb` | `FarmstormExplosion(power=2.0, cap=6, rule=COBBLESTONE_6)` | 抗性阈值 6.0（DEC D-19 / C-2） |
| `MelonGrenadeProjectile` | `farmstorm:melon_grenade` | ① 在 `pos + (rand(-0.3,0.3), 1.0, rand(-0.3,0.3))` 生成 4 个 `MelonSliceProjectile`（`setSubmunition(true)`，随机水平初速 0.3）；② 立刻 `FarmstormExplosion(power=3.0, cap=8, rule=COBBLESTONE_6)` | 子母弹自身仍会再炸一次（威力 2），形成二次爆炸（plan 第 86 行） |
| `BambooFirecrackerProjectile` | `farmstorm:bamboo_firecracker` | 命中后 `keepAliveAfterImpact() = true`：设 `fuseTicks = 140`、`noPhysics = true`、`setDeltaMovement(ZERO)`；`tick()` 每 5 tick 在自身 ±1.5（内部常量 `FIRECRACKER_SCATTER`）内生成原版 `EntityType.WIND_CHARGE` 并立即 `windCharge.explode()` | 140 tick 后 `discard()`；每次风弹爆炸走原版逻辑（不破坏方块） |
| `RedMushroomPuffProjectile` | `farmstorm:red_mushroom_puff` | 对半径 4.0 内**所有玩家**施加反胃 V 60 tick + 失明 I 60 tick | 无爆炸，无方块伤害 |
| `BrownMushroomPuffProjectile` | `farmstorm:brown_mushroom_puff` | 对半径 4.0 内所有玩家施加虚弱 II 200 tick + 缓慢 II 100 tick | 同上 |

**为什么蘑菇类不排除己方**：`plan.txt` 第 135 / 144 行只写「落点半径 4 格内所有玩家」，DEC **D-27** 已明确「胡罗贝、红/棕菇菇类的范围效果**不筛敌我**，队友同样中招」，且 D-07 的「只针对非己方」只限定陷阱的**触发**与炮台的**索敌**，不覆盖一次性 AoE。实现上因此调用 `level.getEntitiesOfClass(Player.class, aabb, p -> true)`，**不带** `isAlly` 过滤。

**`TurretBullet`**（man瓜 炮台的导弹）：

```java
public class TurretBullet extends ThrowableItemProjectile {
    private @Nullable Entity target;
    public void setTarget(@Nullable Entity target);
    @Override protected void onHitEntity(EntityHitResult r);
    @Override protected void tick();                    // 追踪：每 tick 朝 target 重算速度
    @Override protected void onHitBlock(BlockHitResult r);   // 撞方块即消失，不炸方块
}
```

命中处理：`target.hurt(FarmstormDamageTypes.voidBolt(level, owner), 1.0F)` → 沿「炮台位置 → 目标位置」方向施加 `knockback(1.0, dx, dz)` → `discard()`。渲染复用原版 `ShulkerBulletRenderer` 的模型与贴图（`client/render/TurretBulletRenderer`）。

### 4.8 转换器四件套

#### 4.8.1 `ConverterBlock`

```java
public class ConverterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override protected MapCodec<? extends BaseEntityBlock> codec();
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state);
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type);
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                        Player player, BlockHitResult hit);
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston);
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
```

- `useWithoutItem`：服务端 `player.openMenu(state.getMenuProvider(level, pos))`；`getMenuProvider` 返回 `new SimpleMenuProvider((id, inv, p) -> new ConverterMenu(id, inv, ContainerLevelAccess.create(level, pos)), Component.translatable("block.farmstorm.converter"))`。
- `onRemove`：`Containers.dropContents(level, pos, be)`，防止拆方块吞物品。
- 方块属性：`.strength(3.5F)`（与制图台同档）、`.requiresCorrectToolForDrops()`、`#minecraft:mineable/pickaxe`。

#### 4.8.2 `ConverterBlockEntity`

```java
public class ConverterBlockEntity extends BlockEntity implements Container {
    private ItemStack input = ItemStack.EMPTY;
    private @Nullable ConverterRecipe cachedRecipe;      // RecipeManager 重载时清空
    private int cachedXpCost;

    @Override public int getContainerSize() { return 1; }
    @Override public ItemStack getItem(int slot);
    @Override public ItemStack removeItem(int slot, int amount);
    @Override public void setItem(int slot, ItemStack stack);
    @Override public boolean stillValid(Player player);
    @Override public void setChanged();

    /** 事务化转换：先全量校验，全部通过才扣材并产出。 */
    public boolean tryConvert(ServerPlayer player);

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries);
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries);
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries);
}
```

`tryConvert` 的校验顺序（**顺序即安全性**，DEC D-12）：

1. `input` 为空 → false。
2. 正向配方：`level.getRecipeManager().getRecipeFor(FarmstormRecipes.CONVERTING_TYPE.get(), new SingleRecipeInput(input), level)`；未命中且允许反向时，遍历 `getAllRecipesFor` 比对 `result` 与 `input`（缓存到 `cachedRecipe`）。
3. `player.totalExperience >= recipe.xpCost()`（不足直接拒绝，**绝不扣等级**）。
4. 催化剂：`recipe.catalyst()` 非空时，扫描玩家背包（含副手）凑够 `catalystCount`，不消耗主手物品本身。
5. 全部通过后才动状态：`input.shrink(1)` → `player.giveExperiencePoints(-xpCost)` → 扣除催化剂 → 产出交给 `player.getInventory().placeItemBackInInventory(...)`。
6. `setChanged()` + `level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)` 刷新 GUI。

> 步骤 2–4 必须在同一个服务端 tick 内完成，期间不接受客户端插入；按钮包只触发 `tryConvert`，不携带任何可被伪造的参数。

#### 4.8.3 `ConverterMenu`

```java
public class ConverterMenu extends AbstractContainerMenu {
    public static final int BUTTON_CONVERT = 0;

    private final Container container;
    private final ContainerLevelAccess access;
    private final ContainerData data;      // [0]=xpCost  [1]=canConvert(0/1)  [2]=reverse(0/1)

    public ConverterMenu(int id, Inventory inv, FriendlyByteBuf buf);                 // 客户端构造
    public ConverterMenu(int id, Inventory inv, ContainerLevelAccess access);         // 服务端构造

    @Override public void slotsChanged(Container c);
    @Override public boolean clickMenuButton(Player player, int id);
    @Override public ItemStack quickMoveStack(Player player, int index);
    @Override public boolean stillValid(Player player);
}
```

- 槽位：0 = 输入（`new Slot(container, 0, 80, 35)`），1 = 输出预览（`mayPlace = false`、`mayPickup = false`）。
- `clickMenuButton(player, 0)` → `container instanceof ConverterBlockEntity be && be.tryConvert((ServerPlayer) player)`。
- `quickMoveStack`：Shift 点击只处理 0 ↔ 玩家背包。

#### 4.8.4 `ConverterScreen`

```java
public class ConverterScreen extends AbstractContainerScreen<ConverterMenu> {
    private static final ResourceLocation BG = Farmstorm.id("textures/gui/converter.png");
    @Override protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY);
    @Override protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY);
}
```

注册：客户端 `@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)` 里监听 `RegisterMenuScreensEvent` → `e.register(FarmstormMenus.CONVERTER.get(), ConverterScreen::new)`。转换按钮用 `Button.builder(...)`，点击发出原生 `ServerboundContainerButtonClickPacket`——**不新增自定义网络包**（§5.4）。

#### 4.8.5 `ConverterRecipe` + `ConverterRecipeSerializer`

```java
public class ConverterRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack result;
    private final int xpCost;
    private final Ingredient catalyst;          // Ingredient.EMPTY 表示无催化
    private final int catalystCount;
    private final boolean bidirectional;
    private final int reverseXpCost;

    @Override public boolean matches(SingleRecipeInput in, Level level);
    @Override public ItemStack assemble(SingleRecipeInput in, HolderLookup.Provider registries);
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result; }
    @Override public RecipeSerializer<?> getSerializer() { return FarmstormRecipes.CONVERTING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return FarmstormRecipes.CONVERTING_TYPE.get(); }

    /** 反向视图：不落盘，运行时生成，供转换器做反向匹配。 */
    public ConverterRecipe reverse();
}
```

```java
public class ConverterRecipeSerializer implements RecipeSerializer<ConverterRecipe> {
    public static final MapCodec<ConverterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("input").forGetter(ConverterRecipe::input),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ConverterRecipe::result),
            Codec.INT.optionalFieldOf("xp_cost", 3).forGetter(ConverterRecipe::xpCost),
            Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.EMPTY).forGetter(ConverterRecipe::catalyst),
            Codec.INT.optionalFieldOf("catalyst_count", 1).forGetter(ConverterRecipe::catalystCount),
            Codec.BOOL.optionalFieldOf("bidirectional", true).forGetter(ConverterRecipe::bidirectional),
            Codec.INT.optionalFieldOf("reverse_xp_cost", 0).forGetter(ConverterRecipe::reverseXpCost)
    ).apply(inst, ConverterRecipe::new));

    @Override public MapCodec<ConverterRecipe> codec() { return CODEC; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ConverterRecipe> streamCodec();
}
```

**JSON schema**（`data/farmstorm/recipe/*.json`）：

```json
{
  "type": "farmstorm:converting",
  "input": { "item": "minecraft:wheat" },
  "result": { "id": "farmstorm:puffed_wheat", "count": 1 },
  "xp_cost": 3,
  "bidirectional": true,
  "reverse_xp_cost": 0
}
```

火把花 / 瓶子草（DEC D-12：只允许正向，且额外烧 1 个下界之星）：

```json
{
  "type": "farmstorm:converting",
  "input": { "item": "minecraft:torchflower" },
  "result": { "id": "farmstorm:awakened_torchflower", "count": 1 },
  "xp_cost": 3,
  "catalyst": { "item": "minecraft:nether_star" },
  "catalyst_count": 1,
  "bidirectional": false
}
```

字段语义：

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `type` | 字符串 | 是 | — | 固定 `farmstorm:converting` |
| `input` | Ingredient | 是 | — | 支持 `{"item":...}` / `{"tag":...}` / 数组（多选一） |
| `result` | ItemStack | 是 | — | `{"id":..., "count":1}` |
| `xp_cost` | int | 否 | 3 | 覆盖 `converter.xpCost` 全局值 |
| `catalyst` | Ingredient | 否 | 空 | 追加材料（下界之星） |
| `catalyst_count` | int | 否 | 1 | 材料数量 |
| `bidirectional` | bool | 否 | true | 是否允许「改良 → 原版」 |
| `reverse_xp_cost` | int | 否 | 0 | 反向的经验消耗（D-12：反向免费） |

### 4.9 `FarmstormExplosion` + `BlastRule`

- **职责**：一次性满足四个约束——① 破坏性谓词（哪些方块可炸）；② 抗性阈值（DEC D-19：≥ 圆石 6.0 不破）；③ **每实体伤害上限**（plan 的「最多造成 N 伤害」）；④ 正确的击杀归属与死亡信息（T-06）。
- **继承**：`Explosion`（复用构造、`Level` 注册流程与 `ExplosionInteraction` 语义）。
- **为什么必须重写 `finalizeExplosion`**：原版 `Explosion#finalizeExplosion(boolean)` 把「粒子 / 音效 / 实体伤害 / 方块破坏」写死在一个方法里，伤害上限无法在子类插入。子类重写后做三段式，是本需求的最小侵入实现（无 Mixin，T-07）。

```java
public record BlastRule(Predicate<BlockState> extraBreakable,
                        float maxResistance,
                        boolean dropsBlocks) {
    public static final BlastRule VANILLA           = new BlastRule(s -> true, 3.6e6F, true);
    public static final BlastRule UNBREAKABLE_SKIP  = new BlastRule(s -> true, 3.6e6F, true);
    /** DEC D-19：爆炸抗性 >= 圆石（6.0）不破。 */
    public static final BlastRule COBBLESTONE_6     = new BlastRule(s -> true, 6.0F, true);

    public boolean canBreak(BlockGetter level, BlockPos pos, BlockState state, Explosion exp) {
        if (!extraBreakable.test(state)) return false;
        return state.getExplosionResistance(level, pos, exp) < maxResistance;
    }
}
```

```java
public class FarmstormExplosion extends Explosion {
    private final BlastRule rule;
    private final float damageCap;
    private final DamageSource damageSource;

    public FarmstormExplosion(ServerLevel level, @Nullable Entity sourceEntity, DamageSource dmg,
                              double x, double y, double z, float radius,
                              BlastRule rule, float damageCap) {
        super(level, sourceEntity, dmg, null, x, y, z, radius, false, Explosion.BlockInteraction.DESTROY);
        this.rule = rule;
        this.damageCap = damageCap;
        this.damageSource = dmg;
    }

    @Override public void finalizeExplosion(boolean spawnParticles) {
        breakBlocks();            // 1) 方块破坏，受 BlastRule 约束
        hurtCapped();             // 2) 伤害，受 damageCap 约束
        spawnFx(spawnParticles);  // 3) 粒子与音效
    }

    private void breakBlocks();
    private void hurtCapped();
    private void spawnFx(boolean spawnParticles);

    /** 原版钩子：让其它模组与结构判定也走我们的规则。 */
    @Override public boolean shouldBlockExplode(Explosion e, BlockGetter level, BlockPos pos,
                                                BlockState state, float power) {
        return rule.canBreak(level, pos, state, e);
    }
}
```

**伤害上限算法**（`hurtCapped`，逐实体独立封顶）：

```text
中心 c = (x, y, z)；半径 r
for each LivingEntity e in level.getEntitiesOfClass(LivingEntity.class, aabb(r*2)) where !e.ignoredByExplosion():
    dist = e.position().distanceTo(c)
    if dist > r * 2: continue
    exposure = Explosion.getSeenPercent(c, e)          // 0..1，视线暴露率
    impact   = (1.0 - dist / (r * 2)) * exposure
    raw      = (impact * impact + impact) / 2.0 * 7.0 * r * 2.0 + 1.0      // 原版同款公式
    finalDmg = min(raw, damageCap)
    e.hurt(damageSource, finalDmg)
    e.push(...)                                        // 原版同款击退
```

- **归属**：`damageSource` 必须带 `owner`（投掷者 / 部署者），这样击杀记分与死亡信息（「X 被 Y 炸飞」）都正确。
- **`#farmstorm:explosion_immune`**：火把花 / 瓶子草「任何爆炸都无法破坏」（D-14）通过 `BlastRule.extraBreakable` 排除，实现为 `!state.is(FarmstormTags.EXPLOSION_IMMUNE)`，tag 文件见 §6。
- **纯破坏型**：`damageCap <= 0` 时跳过 `hurtCapped`（预留给未来的纯破坏作物）。

### 4.10 `TempEnchantHandler`（DEC D-15）

- **职责**：给火把花光环内的玩家武器挂「临时火焰附加」，到期或离开范围时撤销，且**不覆盖原有附魔等级**。
- **形态**：纯静态工具类（final + private ctor），状态全部落在物品的 DataComponent 上（T-08）。

```java
public final class TempEnchantHandler {
    public static void grant(ServerPlayer player, ItemStack stack, Holder<Enchantment> ench,
                             int level, int durationTicks);
    public static void tick(ServerPlayer player);        // PlayerTickEvent.Post，每 20 tick
    public static void revokeExpired(ItemStack stack, long now);
    public static void revokeAll(ItemStack stack);        // 离开光环 / 下线清理
    public static boolean hasTemporary(ItemStack stack, Holder<Enchantment> ench);
    private static void write(ItemStack stack, List<TempEnchantRecord> records);
    private static List<TempEnchantRecord> read(ItemStack stack);
}
```

**授予流程**：

1. `int previous = EnchantmentHelper.getItemEnchantmentLevel(ench, stack)`（记录原等级）。
2. 若已存在同一 `ench` 且 `expireAt` 未过的记录 → 只刷新 `expireAt`，不压栈。
3. `EnchantmentHelper.updateEnchantments(stack, m -> m.set(ench, Math.max(previous, level)))`。
4. 追加 `new TempEnchantRecord(ench, level, previous, now + durationTicks)` 到组件。

**撤销流程**：

1. `read(stack)` 过滤出 `expireAt <= now` 的记录。
2. 对每条：`updateEnchantments` 把等级设回 `previous`；`previous == 0` 则移除该附魔。
3. 剩余记录写回；列表为空则移除整个组件。
4. **不覆盖检查**：若当前等级 > `granted`（玩家用铁砧自己升过级）→ 跳过还原，只移除记录，避免「临时系统把玩家的附魔改小」。

**触发点**：

| 场景 | 触发 |
|---|---|
| 光环每 100 tick | `TorchflowerAura.apply` 内对剑 `grant` |
| 周期性过期 | `PlayerTickEvent.Post`，`player.tickCount % 20 == 0` |
| 离开范围 | 光环 tick 时对半径外的己方玩家 `revokeAll`（只撤光环给的那条） |
| 下线 | `PlayerEvent.PlayerLoggedOutEvent` 不做落盘清理，靠下次 tick 自愈 |
| 物品转入箱子 | 记录随物品走，取出后在下一次 `tick` 被 `revokeExpired` 清掉 |

> 边界：`gameTime` 单调不减，因此不存在永不过期的挂起记录；存档回滚可能让记录提前过期——可接受（撤销临时附魔不会伤害玩家）。

### 4.11 `FarmstormConfig`（服务端配置全量清单）

> **对齐声明**：本节键名、默认值、取值范围**逐项对齐 `01-DESIGN.md` §3.8**（共 **117** 项，含 §3.8.2 的效果键模板与 §3.8.5 的概率表）。文件内键路径写作 `<group>.<key>`（落在 `config/farmstorm-server.toml`），设计文档与 `translation` 里写作 `farmstorm.<group>.<key>` 与 `farmstorm.configuration.<group>.<key>`。本节早先草稿使用驼峰前缀（`general` / `blastCarrot` / `torchflower` …）的方案**作废**，一律以下表为准。

- **职责**：全部玩法数值的唯一入口（DEC D-21 / D-22）；加载与重载后重建派生缓存。
- **注册方式**：模组构造器内 `modContainer.registerConfig(ModConfig.Type.SERVER, FarmstormConfig.SPEC)`；`SPEC` 由 `ModConfigSpec.Builder` 在静态块里构建——字段**不能是 `final`**（值在静态块中赋值）。
- **烘焙**：监听 `ModConfigEvent.Loading` / `ModConfigEvent.Reloading` → `bake()`，重建 `LuckyBerryRoller` 的概率表与 `BlastRule` 阈值快照。§3.8.5 的「8 项之和必须等于 100」校验也在 `bake()` 里做：不满足则**拒绝该段并回退默认值 + `log.warn`**，不影响其他分组。

```java
public final class FarmstormConfig {
    public static final ModConfigSpec SPEC;
    public static ModConfigSpec.IntValue deployTurretMiningTicks;
    public static ModConfigSpec.DoubleValue blastCarrotExplosionPower;
    // ... 共 117 项

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("部署系统").push("deploy");
        deployTurretMiningTicks = b
                .comment("炮台类部署方块的固定硬挖刻数（200 刻 = 10 秒）")
                .translation("farmstorm.configuration.deploy.turretMiningTicks")
                .defineInRange("turretMiningTicks", 200, 0, 72000);
        b.pop();
        // ... 其余分组
        SPEC = b.build();
    }

    public static void bake();
}
```

#### 4.11.1 通用项（14）

```toml
[converter]
xpCost = 3                     # D-12：每次转换扣 3 点经验
netherStarForwardCost = 1      # D-12：火把花/瓶子草正向额外消耗
netherStarOnReverse = false    # D-12：反向不烧星

[stack]
throwable = 16                 # D-16
other = 64                     # D-16

[deploy]
turretMiningTicks = 200        # D-13：10 秒
supportMustBeFullOpaque = true  # plan 第 11 行：依附方块必须完整不透明
dropOnSupportLost = true       # G-9：失去依附且不悬空时掉落本体

[teams]
resolver = "scoreboard"        # D-06：scoreboard / ftbteams
untamedFallback = "self"       # G-8：无队伍玩家只对自身友好

[explosion]
sliceResistThreshold = 6.0     # C-2 / D-19：瓜片子
grenadeResistThreshold = 6.0   # D-19：瓜！

[throwable]
lifetimeTicks = 100            # G-6

[food]
eatTicks = 32                  # D-29
```

#### 4.11.2 各作物专属项（51）

```toml
[puffed_wheat]
dryEdge = 3                    # plan 第 38 行：3x3x3
wetEdge = 5                    # 遇水扩到 5x5x5

[blast_carrot]
triggerRadius = 1              # 切比雪夫
explosionPower = 2
maxDamage = 7
nausea.level = 3               # 反胃 III
nausea.durationTicks = 60

[ubi_server]
explosionPower = 4
maxDamage = 2

[toxic_ubi_server]
explosionPower = 2
maxDamage = 2
cloudRadius = 3.0
cloudDurationTicks = 600       # G-4：沿用原版滞留药水

[melon_slice_bomb]
explosionPower = 2
maxDamage = 6

[melon_grenade]
spawnCount = 4                 # 4 片西瓜片
spawnOffsetY = 1
horizontalJitter = 0.3         # x/z 误差 0~0.3
explosionPower = 3
maxDamage = 8

[pumpkin_turret]
targetRadius = 5               # 切比雪夫
projectileDamage = 1           # 虚空伤害
knockbackLevel = 1
fireIntervalTicks = 60         # G-2 / D-28：3 秒一发

[awakened_torchflower]
auraRadius = 20.0              # 欧氏
refreshTicks = 100
fireAspectLevel = 1
particlesPerRefresh = 8        # G-7

[awakened_pitcher_plant]
auraRadius = 10.0
refreshTicks = 100
clearHarmful = true
particlesPerRefresh = 8

[fang_trap_cactus]
triggerRadius = 3.0            # C-5：欧氏
outerFangCount = 8
outerDelayTicks = 10
outerRadius = 2.0              # G-3
innerFangCount = 5
innerDelayTicks = 4
innerRadius = 1.0

[bamboo_firecracker]
spawnIntervalTicks = 5
durationTicks = 140            # 140 / 5 = 28 次

[red_mushroom_puff]
radius = 4.0

[brown_mushroom_puff]
radius = 4.0

[lucky_berry]
nutrition = 4                  # D-29
saturationModifier = 0.3
randomCropCount = 16
lootChestCount = 2
smallExplosionPower = 1
creativeDurationTicks = 1800   # D-18：90 秒

[power_beet]
nutrition = 4                  # D-29
saturationModifier = 0.3
```

#### 4.11.3 效果类键（44 = 22 组 × 2）

模板（§3.8.2）：`<crop>.<effect>.level`（1 = I 级，写入时 `amplifier = level - 1`）+ `<crop>.<effect>.durationTicks`（0 = 不施加）。

| 作物段 | effect 段 | `level` | `durationTicks` | 折算 amplifier |
|---|---|---|---|---|
| `power_beet` | `strength` | 2 | 600 | 1 |
| `power_beet` | `speed` | 2 | 600 | 1 |
| `power_beet` | `haste` | 2 | 600 | 1 |
| `power_beet` | `resistance` | 1 | 600 | 0 |
| `awakened_torchflower` | `strength` | 3 | 300 | 2 |
| `awakened_torchflower` | `resistance` | 2 | 300 | 1 |
| `awakened_torchflower` | `haste` | 2 | 300 | 1 |
| `awakened_torchflower` | `speed` | 1 | 300 | 0 |
| `awakened_torchflower` | `water_breathing` | 1 | 300 | 0 |
| `awakened_torchflower` | `night_vision` | 1 | 300 | 0 |
| `awakened_pitcher_plant` | `regeneration` | 2 | 300 | 1 |
| `awakened_pitcher_plant` | `saturation` | 2 | 300 | 1 |
| `awakened_pitcher_plant` | `absorption` | 1 | 300 | 0 |
| `red_mushroom_puff` | `nausea` | 5 | 60 | 4 |
| `red_mushroom_puff` | `blindness` | 1 | 60 | 0 |
| `brown_mushroom_puff` | `weakness` | 2 | 200 | 1 |
| `brown_mushroom_puff` | `slowness` | 2 | 100 | 1 |
| `lucky_berry` | `regeneration`（25% 分支） | 5 | 100 | 4 |
| `lucky_berry` | `strength`（1.9% 分支） | 5 | 12000 | 4 |
| `lucky_berry` | `resistance`（1.9% 分支） | 3 | 12000 | 2 |
| `lucky_berry` | `jump_boost`（1.9% 分支） | 5 | 12000 | 4 |
| `lucky_berry` | `speed`（1.9% 分支） | 2 | 12000 | 1 |

#### 4.11.4 幸运浆果概率表（8）

```toml
[lucky_berry.chance]
lightning = 15.0          # 在食用者位置生成一道闪电
random_crops = 25.0       # 生成 randomCropCount 个随机改良农作物
regeneration = 25.0       # regeneration 分支
loot_chest = 25.0         # 生成 lootChestCount 个末地城战利品宝箱
small_explosion = 5.0     # 威力 smallExplosionPower
charged_creeper = 3.0     # 闪电苦力怕
godlike_buff = 1.9        # 600 秒的神级增益
creative_mode = 0.1       # 创造模式 creativeDurationTicks
```

**校验**：8 项之和必须 = 100；否则拒绝该段并回退默认值 + 警告（§3.8.5）。

#### 4.11.5 消费方对照（键 → 读它的类）

| 分组 | 消费方 |
|---|---|
| `converter.*` | `ConverterBlockEntity#tryConvert`（`netherStarForwardCost` / `netherStarOnReverse`）、`ConverterMenu`（显示 `xpCost`） |
| `stack.*` | `FarmstormItems` 的 `.stacksTo(...)` 注册 |
| `deploy.*` | `DeployedCropBlock#getDestroyProgress`（`turretMiningTicks`）、`#getStateForPlacement`（`supportMustBeFullOpaque`）、`#updateShape`/`#onRemove`（`dropOnSupportLost`） |
| `teams.*` | `Farmstorm` 构造时选择 `TeamResolver` 实现；`ScoreboardTeamResolver#isAlly` 读 `untamedFallback` |
| `explosion.*` | `MelonSliceProjectile` / `MelonGrenadeProjectile` 构造 `BlastRule` 时读阈值 |
| `throwable.lifetimeTicks` | `ThrownCropEntity#tick`（`tickCount > lifetime` → `discard()`） |
| `food.eatTicks` | `FoodCropItem#getUseDuration` |
| `<crop>.explosionPower` / `.maxDamage` | 各投掷物 / 陷阱构造 `FarmstormExplosion` 时读取 |
| `<crop>.<effect>.*` | 各 `AuraProvider` / `ThrownCropEntity` / `LuckyBerryRoller` 施加效果时读取 |
| `puffed_wheat.dryEdge` / `.wetEdge` | `PuffedWheatProjectile#onImpact` |
| `awakened_torchflower.fireAspectLevel` | `TempEnchantHandler#grant`（由 `TorchflowerAura` 传入） |
| `lucky_berry.*` | `LuckyBerryRoller#roll/apply` |

#### 4.11.6 不进 config 的内部常量（安全阀）

下列 6 个值**刻意不暴露为配置项**（`01-DESIGN.md` §3.8 未登记，且它们是安全阀而非玩法数值）：`DEPLOY_COOLDOWN_TICKS = 5`、`THROW_COOLDOWN_TICKS = 5`、`MAX_DEPLOYED_PER_CHUNK = 8`、`MAX_DEPLOYED_PER_PLAYER = 32`、`MAX_EXPLOSIONS_PER_TICK = 8`、`MAX_EFFECTS_PER_TICK = 64`。若要暴露，必须先回写 §3.8 再改代码。

**计数核对**：14（通用）+ 51（专属）+ 44（效果）+ 8（概率）= **117 项**。

### 4.12 `LuckyBerryRoller`（DEC D-17 / D-18）

- **职责**：幸运浆果的 8 档概率表——数据结构、抽取、执行。
- **形态**：final 工具类 + 不可变权重表；表在 `FarmstormConfig.bake()` 时按配置重建（默认值即 plan 的百分比）。
- **配置层用百分比、内部用千分比整数**：配置键按 `01-DESIGN.md` §3.8.5 暴露为百分比（15 / 25 / 25 / 25 / 5 / 3 / 1.9 / 0.1，和 = 100）；`bake()` 时乘 10 取整成 150 / 250 / 250 / 250 / 50 / 30 / 19 / 1 的内部权重，和恒为 1000，抽取一次 `nextInt(1000)` 即可，避免浮点累加漂移。配置层校验「和 = 100」，不满足则拒绝该段并回退默认值（§3.8.5）。

```java
public final class LuckyBerryRoller {
    public enum Outcome {
        LIGHTNING, SPAWN_CROPS, REGEN, LOOT_CHESTS, SMALL_EXPLOSION,
        CHARGED_CREEPER, POWER, CREATIVE
    }

    /** weight 为千分比：由百分比配置 ×10 取整而来，和恒为 1000。 */
    public record Roll(Outcome outcome, int permille) {}

    /** 由 FarmstormConfig 烘焙；运行时只读。 */
    private static final List<Roll> TABLE = List.copyOf(List.of(
            new Roll(Outcome.LIGHTNING,        150),
            new Roll(Outcome.SPAWN_CROPS,      250),
            new Roll(Outcome.REGEN,            250),
            new Roll(Outcome.LOOT_CHESTS,      250),
            new Roll(Outcome.SMALL_EXPLOSION,   50),
            new Roll(Outcome.CHARGED_CREEPER,   30),
            new Roll(Outcome.POWER,             19),
            new Roll(Outcome.CREATIVE,           1)));

    /** 累计权重上界，长度与 TABLE 相同，最后一个元素恒为 1000。 */
    private static final int[] CUMULATIVE = buildCumulative(TABLE);

    public static Outcome roll(RandomSource random) {
        int r = random.nextInt(1000);
        for (int i = 0; i < CUMULATIVE.length; i++) {
            if (r < CUMULATIVE[i]) return TABLE.get(i).outcome();
        }
        return Outcome.SPAWN_CROPS;   // 配置异常时的兜底，不应到达
    }

    public static void apply(ServerLevel level, ServerPlayer player, Outcome outcome);
    private static int[] buildCumulative(List<Roll> table);   // 校验和 == 1000；配置层已保证百分比之和 == 100
}
```

**8 档执行细节**：

| Outcome | 概率 | 执行（全部服务端） |
|---|---|---|
| `LIGHTNING` | 15% | `EntityType.LIGHTNING_BOLT` 在玩家位置生成，`setCause(null)`（无来源伤害） |
| `SPAWN_CROPS` | 25% | 16 次：从 **除幸运浆果外的 14 种作物**（D-17）均匀抽 1 个，生成 `ItemEntity`；位置 = 玩家半径 2 格内的随机可站立方块（D-33；放不下则就近） |
| `REGEN` | 25% | `MobEffects.REGENERATION`，amplifier 4（V 级），100 tick（5 秒） |
| `LOOT_CHESTS` | 25% | 生成 2 个原版箱子并把 `minecraft:chests/end_city_treasure` 战利品表灌入（`ChestBlockEntity#setLootTable` + `setLootTableSeed`）；箱子位置同样遵循 D-33（半径 2 格内可站立方块，放不下则就近） |
| `SMALL_EXPLOSION` | 5% | `FarmstormExplosion(power=1.0, cap=999, rule=UNBREAKABLE_SKIP)`——**注意**：plan 未给上限，取「不封顶」，避免误伤设计意图 |
| `CHARGED_CREEPER` | 3% | 生成 `EntityType.CREEPER` 并设置 `setPowered(true)`；不点燃，交给玩家处理 |
| `POWER` | 1.9% | 力量 V / 抗性 III / 跳跃提升 V / 速度 II，各 12000 tick（600 秒） |
| `CREATIVE` | 0.1% | **D-18 专门处理**：记录 `player.gameMode.getGameModeForPlayer()` → `player.setGameMode(GameType.CREATIVE)` → 记入 `Map<UUID, CreativeRestore>`（**只在内存，不写存档**）；1800 tick（90 秒）后强制还原。玩家中途下线 → `PlayerLoggedOutEvent` 里立刻还原并移除记录；服务器重启 → 记录丢失，但 `PlayerLoggedInEvent` 里做一次「非创造存档却是创造模式」的对账（以 `player.gameMode` 与存档默认模式比对，见 §9 风险 5） |

`LuckyBerryRoller.apply` 必须在 `FoodCropItem#onEaten` 里调用，且整个方法体包在 `if (level instanceof ServerLevel)` 内——客户端不得参与抽取（§5.3）。

---

## 5. 服务端权威架构

### 5.1 总原则

本模组的所有「会改变世界状态」的判定都在服务端完成，客户端只负责**表现**。这不只是安全要求，也是兼容要求：DEC D-23 规定「客户端只装资源包即可」，意味着专用服务器上可以没有 `farmstorm` 的客户端逻辑以外的任何东西（客户端仍需要模组本体以渲染方块/物品/实体，但**不需要**任何权威逻辑）。

三条硬规则：

1. **没有自定义 C2S 网络包**（0 个）。所有客户端→服务端的意图都走原版包：`ServerboundUseItemPacket`、`ServerboundUseItemOnPacket`、`ServerboundContainerButtonClickPacket`、`ServerboundPlayerActionPacket`。
2. **客户端不发送任何数值**。转换器的经验成本、炮台的伤害、爆炸的威力全部由服务端从配置/配方读取。
3. **所有权（owner）只能来自服务端上下文**：`BlockPlaceContext#getPlayer()` / `Projectile#getOwner()`，绝不从物品组件或包字段里读。

### 5.2 必须在服务端执行的逻辑清单

| 逻辑 | 位置 | 若放到客户端的后果 |
|---|---|---|
| 部署物归属写入 | `DeployedCropBlock#setPlacedBy` → `BE.setOwner` | 客户端可伪造 owner，把敌方部署物变成己方 |
| 陷阱触发判定 | `TrapBehavior#findTrigger` | 客户端决定谁踩雷，可无限刷爆炸 |
| 炮台索敌与开火 | `TurretBehavior#findTarget/fire` | 客户端可让炮台只打敌人、不打自己 |
| 光环施加效果 / 临时附魔 | `AuraProvider#apply` / `TempEnchantHandler` | 客户端可给全队上 buff |
| 爆炸的三段式 | `FarmstormExplosion#finalizeExplosion` | 客户端可绕过伤害上限与抗性阈值 |
| 投掷物命中效果 | `ThrownCropEntity#onImpact` | 客户端可决定爆炸位置与次数 |
| 转换事务 | `ConverterBlockEntity#tryConvert` | 客户端可免费转换、刷物品 |
| 幸运浆果抽取 | `LuckyBerryRoller#roll` | 客户端可自选结果（直接选创造模式） |
| 创造模式回滚计时 | `LuckyBerryRoller` 内存表 + 玩家 tick | 客户端可永久保持创造 |

### 5.3 数据同步

| 对象 | 机制 | 同步字段 | 频率 |
|---|---|---|---|
| `DeployedCropBlockEntity` | `getUpdateTag`（区块加载全量）+ `getUpdatePacket` → `ClientboundBlockEntityDataPacket.create(this)`（定向增量） | `ownerUuid` / `ownerName` / `placedGameTime` / `detached` / `pendingWaves` / `fuseTicks` | 放置时一次；状态变化时 `setChanged()` + `level.sendBlockUpdated` |
| `ConverterBlockEntity` | 容器槽位由 `AbstractContainerMenu` 自带的 `ContainerSynchronizer` 同步；额外状态用 `ContainerData`（`xpCost` / `canConvert` / `reverse`） | 槽位 0 的物品、3 个 int | 每次 `slotsChanged` |
| `ThrownCropEntity` | 实体 spawn 包由 `ServerLevel#addFreshEntity` 自动广播；`submunition` 用 `SynchedEntityData`（`defineSynchedData`） | 物品渲染、`submunition`、`fuseTicks`（如需） | 生成时 + 变化时 |
| `TurretBullet` | 同上；追踪目标用 `SynchedEntityData<Optional<Entity>>`？→ **不用**：目标只服务端需要，客户端按速度插值即可 | 仅物品/速度 | 生成时 |
| 临时附魔 | `DataComponent` 随 `ItemStack`（`ClientboundContainerSetSlotPacket` / `ClientboundSetEquipmentPacket`） | 整个 `List<TempEnchantRecord>` | 物品变化时 |
| 作物生长 | `BlockState`（`age`）→ 区块更新的常规路径 | `age` | 随机 tick |

**队伍信息不参与同步**：归属者的队伍**永远**由服务端在触发瞬间实时解析（C-9）——`getUpdateTag` 里没有、也不应有任何队伍字段。客户端即使知道方块属于谁，也判断不出谁能触发，这是越权保护的一环（§8.4）。

**同步白名单原则**：`getUpdateTag` 只写客户端真正需要的东西。`pendingWaves` 其实客户端不需要（尖牙是服务端 spawn 的实体），写进 `getUpdateTag` 的唯一理由是让客户端的 `getDestroyProgress` 与 tooltip 能显示「已触发」——如果实测带宽有压力，第一个裁掉它。

### 5.4 客户端预测的边界

| 行为 | 客户端可预测 | 说明 |
|---|---|---|
| 挖掘进度条 | 是 | `getDestroyProgress` 是纯函数（`1/200` 或 `0`），两侧一致，无回弹 |
| 方块放置预览（半透明方块影） | 是 | 由 `getStateForPlacement` 在客户端也可算出 |
| 技能冷却 UI | 是 | `ItemCooldowns` 由服务端下发冷却包 |
| 投掷物飞行轨迹 | 是（视觉） | 客户端按速度插值；命中点在服务端裁决 |
| 爆炸的方块破坏 | 否 | 服务端破坏后靠区块更新下发 |
| 爆炸的伤害数字/击退 | 否 | `ClientboundSetHealthPacket` / `ClientboundSetEntityMotionPacket` |
| 陷阱是否触发 | 否 | 客户端不知道 owner（除非 BE 已同步），且不该知道 |
| 转换是否成功 | 否 | 按钮点击后等服务端 `ContainerSetSlot` 回包 |

**已知可接受的不一致**：非己方玩家对火把花/瓶子草按下左键时，客户端因为拿不到 owner 而可能算出「有进度」（如果 BE 尚未同步），服务端拒绝后客户端会被 `ClientboundBlockUpdatePacket` 复位。为消除这 1 帧抖动，客户端在 `BE` 同步完成后调用同一套 `isAlly` 逻辑（`DeployedCropBlock#getDestroyProgress` 里直接读 `BlockGetter` 拿 BE），实际测试应无可见回弹。

### 5.5 为什么坚持 0 个自定义包

| 选项 | 结论 |
|---|---|
| 自定义 C2S「请求转换」包 | 不需要——`ServerboundContainerButtonClickPacket` 已在 `AbstractContainerMenu#clickMenuButton` 校验菜单打开 |
| 自定义 C2S「部署请求」包 | 不需要——`ServerboundUseItemOnPacket` 走原版 `BlockItem#useOn` |
| 自定义 S2C「炮台开火特效」包 | 不需要——`ServerLevel#sendParticles` + `level.playSound` 已够用 |
| 自定义 S2C「幸运浆果结果」包 | 不需要——结果本身通过世界变化与聊天消息体现 |

代价是「客户端无法显示炮台的倒计时环」这类纯装饰需求做不了；v0.0.1 不做，列入延后项。

---

## 6. 资源与数据文件清单

### 6.1 汇总

| 类别 | 数量 |
|---|---|
| `assets/farmstorm/blockstates/*.json` | 20 |
| `assets/farmstorm/models/block/*.json` | 62 |
| `assets/farmstorm/models/item/*.json` | 22 |
| `assets/farmstorm/lang/*.json` | 2 |
| `assets/farmstorm/textures/item/*.png` | 22 |
| `assets/farmstorm/textures/block/*.png` | 64 |
| `assets/farmstorm/textures/gui/*.png` | 1 |
| `data/farmstorm/damage_type/*.json` | 2 |
| `data/minecraft/tags/damage_type/*.json` | 2 |
| `data/farmstorm/recipe/*.json` | 21 |
| `data/farmstorm/loot_table/blocks/*.json` | 20 |
| `data/farmstorm/advancement/*.json` | 12 |
| `data/farmstorm/tags/block/*.json` | 2 |
| `data/farmstorm/tags/item/*.json` | 2 |
| `data/minecraft/tags/block/mineable/pickaxe.json` | 1 |
| 根：`pack.mcmeta` / `META-INF/neoforge.mods.toml` | 2 |
| **合计** | **257**（JSON 169 + TOML 1 + PNG 87） |

### 6.2 assets/farmstorm/blockstates（20）

```
blockstates/puffed_wheat_crop.json               作物（age 0-3 → 4 变体）
blockstates/blast_carrot_crop.json
blockstates/ubi_server_crop.json
blockstates/toxic_ubi_server_crop.json
blockstates/power_beet_crop.json
blockstates/melon_grenade_stem.json
blockstates/pumpkin_turret_stem.json
blockstates/awakened_torchflower_crop.json
blockstates/awakened_pitcher_plant_crop.json
blockstates/fang_trap_cactus_block.json
blockstates/bamboo_firecracker_stalk.json
blockstates/red_mushroom_puff_block.json
blockstates/brown_mushroom_puff_block.json
blockstates/lucky_berry_bush.json
blockstates/blast_carrot.json                     部署型（face × facing × floating）
blockstates/pumpkin_turret.json
blockstates/awakened_torchflower.json
blockstates/awakened_pitcher_plant.json
blockstates/fang_trap_cactus.json
blockstates/converter.json                        转换器（4 朝向）
```

### 6.3 assets/farmstorm/models/block（62）

- 作物阶段模型 56 个：`models/block/<base>_stage0.json` … `_stage3.json`，父模型 `minecraft:block/crop`。<base> 的取法 = 作物方块 ID 去掉末尾的 `_crop` / `_stem` / `_stalk` / `_block` / `_bush`（例：`puffed_wheat_crop` → `puffed_wheat`，`lucky_berry_bush` → `lucky_berry`，`melon_grenade_stem` → `melon_grenade`）。
- 部署型模型 5 个：`models/block/blast_carrot.json` / `pumpkin_turret.json` / `awakened_torchflower.json` / `awakened_pitcher_plant.json` / `fang_trap_cactus.json`。悬空状态**复用同一模型**，只在 blockstate 里改 `y` 旋转与 `uvlock`。
- 转换器 1 个：`models/block/converter.json`（`cube_bottom_top` + 正面独立贴图）。

### 6.4 assets/farmstorm/models/item（22）

```
models/item/puffed_wheat.json            } 15 个作物物品模型
models/item/blast_carrot.json            }
models/item/ubi_server.json              }
models/item/toxic_ubi_server.json        }
models/item/power_beet.json              }
models/item/melon_slice_bomb.json        }
models/item/melon_grenade.json           }
models/item/pumpkin_turret.json          }
models/item/awakened_torchflower.json    }
models/item/awakened_pitcher_plant.json  }
models/item/fang_trap_cactus.json        }
models/item/bamboo_firecracker.json      }
models/item/red_mushroom_puff.json       }
models/item/brown_mushroom_puff.json     }
models/item/lucky_berry.json             }
models/item/puffed_wheat_seeds.json      } 6 个播种物
models/item/power_beet_seeds.json        }
models/item/melon_grenade_seeds.json     }
models/item/pumpkin_turret_seeds.json    }
models/item/awakened_torchflower_seeds.json
models/item/awakened_pitcher_plant_pod.json
models/item/converter.json               转换器（父模型 = models/block/converter.json）
```

5 个部署型作物的物品模型**直接引用方块模型**（`{"parent": "farmstorm:block/blast_carrot"}`），不额外画图。

### 6.5 assets/farmstorm/textures（87 PNG）

| 路径 | 数量 | 用途 |
|---|---|---|
| `textures/item/<22 个 id>.png` | 22 | 物品图标；15 个作物图标必须在**右下角加绿色 +**（plan 第 8 条） |
| `textures/block/<base>_stage{0..3}.png` | 56 | 作物 4 阶段贴图（14 作物 × 4；`<base>` 规则见 §6.3） |
| `textures/block/blast_carrot.png` 等 5 个部署方块贴图 | 5 | 部署物外观 |
| `textures/block/converter_top.png` / `_side.png` / `_front.png` | 3 | 转换器三面 |
| `textures/gui/converter.png` | 1 | 176×166 GUI 背景 |

> 全部 PNG 由 `tools/gen_textures.py` 批量生成占位稿，作者审校后替换（DEC D-25）。

### 6.6 assets/farmstorm/lang（2）

`lang/zh_cn.json`、`lang/en_us.json`（键集完全一致，见 §7）。

### 6.7 data（62 个 JSON）

**伤害类型（2）**

| 路径 | 内容 |
|---|---|
| `data/farmstorm/damage_type/void_bolt.json` | `{"exhaustion":0.0,"message_id":"farmstorm.void_bolt","scaling":"when_caused_by_living_non_player","effects":"hurt"}` |
| `data/farmstorm/damage_type/crop_blast.json` | `{"exhaustion":0.1,"message_id":"farmstorm.crop_blast","scaling":"when_caused_by_living_non_player","effects":"hurt"}` |

**并入 minecraft 标签（2）**

| 路径 | 内容 |
|---|---|
| `data/minecraft/tags/damage_type/is_projectile.json` | `{"values":["farmstorm:void_bolt"]}` |
| `data/minecraft/tags/damage_type/is_explosion.json` | `{"values":["farmstorm:crop_blast"]}` |

**方块标签（3）**

| 路径 | 内容 |
|---|---|
| `data/farmstorm/tags/block/deployed_crops.json` | 5 个部署方块 |
| `data/farmstorm/tags/block/explosion_immune.json` | `awakened_torchflower` / `awakened_pitcher_plant` |
| `data/minecraft/tags/block/mineable/pickaxe.json` | `converter` + 5 个部署方块（部署物可被镐加速以外的工具挖，但挖速固定，见 §4.4） |

**物品标签（2）**

| 路径 | 内容 |
|---|---|
| `data/farmstorm/tags/item/modified_crops.json` | 15 个作物物品（供幸运浆果随机池与配方复用） |
| `data/farmstorm/tags/item/seeds.json` | 6 个播种物 |

**转换配方（21，每个 `bidirectional:true` 覆盖双向）**

| # | 文件 | input → result |
|---|---|---|
| 1 | `recipe/puffed_wheat.json` | `minecraft:wheat` → `farmstorm:puffed_wheat` |
| 2 | `recipe/blast_carrot.json` | `minecraft:carrot` → `farmstorm:blast_carrot` |
| 3 | `recipe/ubi_server.json` | `minecraft:potato` → `farmstorm:ubi_server` |
| 4 | `recipe/toxic_ubi_server.json` | `minecraft:poisonous_potato` → `farmstorm:toxic_ubi_server` |
| 5 | `recipe/power_beet.json` | `minecraft:beetroot` → `farmstorm:power_beet` |
| 6 | `recipe/melon_slice_bomb.json` | `minecraft:melon_slice` → `farmstorm:melon_slice_bomb` |
| 7 | `recipe/melon_grenade.json` | `minecraft:melon` → `farmstorm:melon_grenade` |
| 8 | `recipe/pumpkin_turret.json` | `minecraft:pumpkin` → `farmstorm:pumpkin_turret` |
| 9 | `recipe/awakened_torchflower.json` | `minecraft:torchflower` → `farmstorm:awakened_torchflower`（+ 下界之星，**单向**） |
| 10 | `recipe/awakened_pitcher_plant.json` | `minecraft:pitcher_plant` → `farmstorm:awakened_pitcher_plant`（+ 下界之星，**单向**） |
| 11 | `recipe/fang_trap_cactus.json` | `minecraft:cactus` → `farmstorm:fang_trap_cactus` |
| 12 | `recipe/bamboo_firecracker.json` | `minecraft:bamboo` → `farmstorm:bamboo_firecracker` |
| 13 | `recipe/red_mushroom_puff.json` | `minecraft:red_mushroom` → `farmstorm:red_mushroom_puff` |
| 14 | `recipe/brown_mushroom_puff.json` | `minecraft:brown_mushroom` → `farmstorm:brown_mushroom_puff` |
| 15 | `recipe/lucky_berry.json` | `minecraft:sweet_berries` → `farmstorm:lucky_berry` |
| 16 | `recipe/puffed_wheat_seeds.json` | `minecraft:wheat_seeds` → `farmstorm:puffed_wheat_seeds` |
| 17 | `recipe/power_beet_seeds.json` | `minecraft:beetroot_seeds` → `farmstorm:power_beet_seeds` |
| 18 | `recipe/melon_grenade_seeds.json` | `minecraft:melon_seeds` → `farmstorm:melon_grenade_seeds` |
| 19 | `recipe/pumpkin_turret_seeds.json` | `minecraft:pumpkin_seeds` → `farmstorm:pumpkin_turret_seeds` |
| 20 | `recipe/awakened_torchflower_seeds.json` | `minecraft:torchflower_seeds` → `farmstorm:awakened_torchflower_seeds` |
| 21 | `recipe/awakened_pitcher_plant_pod.json` | `minecraft:pitcher_pod` → `farmstorm:awakened_pitcher_plant_pod` |

**战利品表（20，`data/farmstorm/loot_table/blocks/*.json`）**

- 14 张作物表：成熟（`age=3`）时按**各自原版比例**掉落作物 + 播种物，未成熟只掉 1 个播种物。
  - 膨胀麦：1 作物 + 0–3 种子（时运加成）；胡罗贝/育碧服务器/毒育碧：1–4 作物；甜菜！：1 作物 + 0–1 种子；瓜！：1–4 作物；man瓜：1 作物；火把花/瓶子草：1 作物；仙人掌：1–3 作物；爆竹：1 作物；红/棕菇：1–2 作物；幸运浆果：1–3 作物。
- 5 张部署方块表：默认掉自身（1 个），**被爆炸破坏时掉 0 个**（用 `survives_explosion` 条件，与原版一致）；火把花/瓶子草永不进爆炸路径（`explosion_immune`）。
- 1 张转换器表：掉自身 + 内部物品（`copy_components` 或 `onRemove` 里 `dropContents`，二选一——本规格选后者，表里只掉方块本体）。

**进度（12，DEC C-7；ID / 父进度 / 触发条件逐条对齐 `01-DESIGN.md` §6）**

| # | 文件 | 显示名 | 父进度 | 触发条件 |
|---|---|---|---|---|
| 1 | `advancement/root.json` | 农场风云 | —（根） | 获得任意一件改良作物（`#farmstorm:modified_crops`） |
| 2 | `advancement/converter.json` | 装个转换器 | `farmstorm:root` | 放置一台农产品转换器 |
| 3 | `advancement/first_conversion.json` | 双向奔赴 | `farmstorm:converter` | 在转换器内完成一次转换（任意方向） |
| 4 | `advancement/nether_star_toll.json` | 星之代价 | `farmstorm:first_conversion` | 完成一次「原版 → 激活版」的火把花 / 瓶子草转换（消耗下界之星） |
| 5 | `advancement/seed_cycle.json` | 闭环自给 | `farmstorm:first_conversion` | 收获一次成熟改良作物，并同时拾取作物与播种物 |
| 6 | `advancement/first_throw.json` | 出手 | `farmstorm:root` | 投掷任意一件投掷型改良作物 |
| 7 | `advancement/first_deploy.json` | 落子 | `farmstorm:root` | 部署任意一件部署型改良作物 |
| 8 | `advancement/demolition.json` | 未授权拆墙 | `farmstorm:first_throw` | 用破障类作物的爆炸破坏至少 1 个方块 |
| 9 | `advancement/turret_hit.json` | 别站那儿 | `farmstorm:first_deploy` | 被 man瓜 的导弹命中一次 |
| 10 | `advancement/aura_bath.json` | 光环洗浴 | `farmstorm:first_deploy` | 从激活的火把花 / 瓶子草获得至少一次光环效果 |
| 11 | `advancement/fang_dance.json` | 最扎人的一集 | `farmstorm:first_deploy` | 触发一次「赢过输过没死过」 |
| 12 | `advancement/lucky_jackpot.json` | 老实发育，很枯燥吧 | `farmstorm:root` | 食用幸运浆果并抽中 0.1% 的创造模式分支 |

约定（照抄 `01-DESIGN.md` §6）：全部 `"frame": "task"`、`"show_toast": true`、`"announce_to_chat": false`；`root` 的 `background` 用 `minecraft:textures/block/hay_block_side.png`。

**实现补充（技术侧）**：进度树共两级（`root` → 6 个子节点；`converter → first_conversion → nether_star_toll / seed_cycle`、`first_throw → demolition`、`first_deploy → turret_hit / aura_bath / fang_dance`）。其中 #1 / #2 / #3 / #5 / #6 / #7 可用原版 `criteria`（`inventory_changed` / `placed_block` / `impossible`+`recipe` 组合）直接表达；#4 / #8 / #9 / #10 / #11 没有对应的原版统计项，统一用 `minecraft:impossible` 触发器 + 服务端 `ServerPlayer#getAdvancements().award(...)` 授予——**判定仍在服务端**（DESIGN §6 约定），不新增指令（D-24）。

---

## 7. 语言文件键命名规范

### 7.1 规则

| 前缀 | 格式 | 数量 | 备注 |
|---|---|---|---|
| `item.farmstorm.<id>` | 全小写蛇形，与注册 ID 一致 | 22 | 缺一个就是 `item.farmstorm.puffed_wheat.name` 式的原样显示 |
| `block.farmstorm.<id>` | 同上 | 20 | 作物方块的显示名取作物名（「膨胀麦」而非「膨胀麦作物」） |
| `entity.farmstorm.<id>` | 同上 | 9 | 投掷物的名字只出现在调试/潜影贝式陨落信息里 |
| `itemGroup.farmstorm.main` | 固定 | 1 | 创造标签页标题 |
| `container.farmstorm.converter` | 固定 | 1 | GUI 标题 |
| `gui.farmstorm.converter.*` | `.convert` / `.xp_cost` / `.no_recipe` / `.need_catalyst` / `.not_enough_xp` / `.reverse` / `.forward` / `.tooltip` | 8 | 按钮与状态文本 |
| `tooltip.farmstorm.*` | `.deployed_by` / `.attached_face` / `.floating` / `.team` / `.turret_range` / `.aura_radius` / `.temporary_enchant` / `.cannot_break` / `.trap_armed` / `.type.<kind>` | 10 | 部署物与物品的附加信息 |
| `death.attack.farmstorm.<message_id>` | 对应 `damage_type` 的 `message_id` | 4 | `farmstorm.void_bolt` 与 `farmstorm.crop_blast`，各含 `.player` 变体 |
| `advancements.farmstorm.<path>.title` / `.description` | 12 个进度 × 2 | 24 | DataGen 生成 |
| `farmstorm.configuration.<group>.<key>` | 与 TOML 键路径完全一致 | 117 | `ModConfigSpec.Builder#translation` 指定 |
| **合计** | | **216** | 中英各一份，键集必须完全一致 |

### 7.2 示例条目（zh_cn.json）

```json
{
  "item.farmstorm.puffed_wheat": "膨胀麦",
  "item.farmstorm.melon_grenade": "瓜！",
  "item.farmstorm.awakened_pitcher_plant_pod": "激活的瓶子草豆荚",
  "item.farmstorm.lucky_berry": "幸运浆果",

  "block.farmstorm.converter": "农产品转换器",
  "block.farmstorm.pumpkin_turret": "man瓜",
  "block.farmstorm.puffed_wheat_crop": "膨胀麦",

  "entity.farmstorm.turret_bullet": "炮台导弹",
  "entity.farmstorm.melon_grenade": "瓜！",

  "itemGroup.farmstorm.main": "农场风云",
  "container.farmstorm.converter": "农产品转换器",

  "gui.farmstorm.converter.convert": "转换",
  "gui.farmstorm.converter.xp_cost": "消耗经验：%s",
  "gui.farmstorm.converter.no_recipe": "无法转换",
  "gui.farmstorm.converter.need_catalyst": "缺少催化剂：%s",
  "gui.farmstorm.converter.not_enough_xp": "经验不足",
  "gui.farmstorm.converter.forward": "原版 → 改良",
  "gui.farmstorm.converter.reverse": "改良 → 原版",
  "gui.farmstorm.converter.tooltip": "放入原版或改良作物进行 1:1 转换",

  "tooltip.farmstorm.deployed_by": "部署者：%s",
  "tooltip.farmstorm.attached_face": "附着面：%s",
  "tooltip.farmstorm.floating": "悬空",
  "tooltip.farmstorm.team": "队伍：%s",
  "tooltip.farmstorm.turret_range": "索敌范围：%s 格（切比雪夫）",
  "tooltip.farmstorm.aura_radius": "光环半径：%s 格",
  "tooltip.farmstorm.temporary_enchant": "临时附魔（%s 秒后消失）",
  "tooltip.farmstorm.cannot_break": "非己方玩家无法破坏",
  "tooltip.farmstorm.trap_armed": "已布置",
  "tooltip.farmstorm.type.trap": "陷阱类",
  "tooltip.farmstorm.type.turret": "炮台类",
  "tooltip.farmstorm.type.aura": "光环提供者",

  "death.attack.farmstorm.void_bolt": "%1$s 被虚空撕碎",
  "death.attack.farmstorm.void_bolt.player": "%1$s 被 %2$s 的 man瓜 击落",
  "death.attack.farmstorm.crop_blast": "%1$s 被作物炸飞了",
  "death.attack.farmstorm.crop_blast.player": "%1$s 被 %2$s 的作物炸飞了",

  "farmstorm.configuration.deploy.turretMiningTicks": "炮台硬挖时长（tick）",
  "farmstorm.configuration.blast_carrot.maxDamage": "胡罗贝爆炸的伤害上限"
}
```

### 7.3 规范细则

1. **中文名照抄 `plan.txt`**，包括「？！菇菇！？」这类标点与全角字符；英文语义命名（DEC D-26）。
2. 作物方块与作物物品**同名**（block 与 item 分属不同 registry，lang 键也分属不同前缀，不冲突）。
3. 死亡信息用 `%1$s` / `%2$s` 位置占位符（`.player` 变体由原版伤害系统自动选用）。
4. 配置项一律走 `farmstorm.configuration.*`，与 TOML 路径逐段对应（`deploy.turretMiningTicks` → `farmstorm.configuration.deploy.turretMiningTicks`），这样 `ModConfigSpec` 的自动 GUI 不需要额外映射。
5. 语言文件由 `ModLangProvider` 生成，禁止手改 `src/generated`（会被 `runData` 覆盖）。

---

## 8. 性能与安全

### 8.1 每 tick 扫描的代价控制

| 手段 | 具体做法 | 量级 |
|---|---|---|
| 扫描间隔 | 陷阱/炮台 10 tick 一次，光环 100 tick 一次 | 常驻开销 ÷ 10 或 ÷ 100 |
| 错峰 | `stagger = floorMod(pos.hashCode(), interval)` | 同一区块的 N 个部署物分摊到 interval 个不同 tick |
| 玩家列表优先 | 光环用 `ServerLevel#players()`（个位数）而非 `getEntitiesOfClass`（可能几十个） | 20 格半径下便宜约一个数量级 |
| AABB 精确化 | 炮台索敌 AABB 取 `AABB.ofSize(center, 2r+1, 2r+1, 2r+1)`，r ≤ 5 → 11×11×11 | 原版 `getEntitiesOfClass` 的体素裁剪足够 |
| 早退 | 己方玩家不参与索敌 → 先判 `isAlly` 再算距离（`isAlly` 是 O(1) 字符串比较） | 省掉绝大多数距离计算 |
| 无 BlockEntity 的作物 | 14 个作物方块无 BE，靠 `randomTick` 生长 | 0 常驻开销 |
| 服务端限定 | `getTicker` 返回 `level.isClientSide ? null : DeployedCropBlockEntity::serverTick` | 客户端 0 开销 |
| 冷却内提前返回 | `if (cooldown > 0) { cooldown--; return; }` 放在一切扫描之前 | 炮台 60 tick 开火间隔 → 实际每 60 tick 只做 1 次索敌 |

**最坏情况估算**：单区块 8 个部署物上限 × 每 10 tick 一次 × 索敌 AABB 内平均 3 个候选实体 ≈ 每 tick 2.4 次距离计算——可忽略。真正需要盯的是**光环叠加**：20 个火把花覆盖同一片玩家区时，每个玩家每 100 tick 会被施加 20 次效果（20 次 `addEffect` + 20 次粒子广播）。这在 100+ 玩家服会出错峰效应，因此用内部常量 `MAX_EFFECTS_PER_TICK`（64）作为熔断：超限时本次 tick 丢弃剩余净化/施加，下个 tick 继续。

### 8.2 爆炸批处理

| 问题 | 方案 |
|---|---|
| 爆竹 28 次风弹爆炸集中爆发 | 每 5 tick 一次，天然摊平；不需要额外限流 |
| 多个投掷物同 tick 命中 | 内部常量 `MAX_EXPLOSIONS_PER_TICK = 8`：超出的 `FarmstormExplosion` 进入 `Deque`，在 `LevelTickEvent.Post` 逐个执行（延迟 ≤ 1 tick，观感无差异） |
| 膨胀麦替换 125 个方块 | 单次硬上限 5×5×5 = 125；堆叠 16 → 最多 2000 次 `setBlock`。用 `level.setBlock(pos, state, Block.UPDATE_CLIENTS)` 并跳过与当前状态相同的方块，避免无意义更新 |
| 爆炸掉落实体过多 | `BlastRule.dropsBlocks` 与 `survives_explosion` 联动；对作物方块（无掉落价值）用 `dropsBlocks = false` |
| 光束/粒子风暴 | 每次爆炸只播一次 `Level#explode` 风格粒子；光环粒子按 `particleCount` 上限 |

### 8.3 防刷

| 攻击面 | 防线 |
|---|---|
| 放置/拆卸循环刷部署物 | 内部常量 `DEPLOY_COOLDOWN_TICKS = 5` + `MAX_DEPLOYED_PER_CHUNK = 8` + `MAX_DEPLOYED_PER_PLAYER = 32`；拆卸后冷却才归零，避免「放→立刻拆→再放」循环触发掉落 |
| 靠炮台硬挖 10 秒刷时间 | 硬挖进度不可加速（固定 `1F/200F`），也不因工具附魔变化 |
| 转换器刷物品 | 单槽位 + 事务化校验（§4.8.2）；输入槽不接受容器（`Slot#mayPlace` 只接受能匹配配方的物品）；`quickMoveStack` 不做批量转换 |
| 转换器吞物品 | `onRemove` → `Containers.dropContents`；方块被活塞推动时不掉（`movedByPiston` 分支） |
| 幸运浆果 RNG 刷 | 抽取只在服务端 `finishUsingItem` 里发生；一个物品只能吃一次（`stack.shrink(1)` 由原版食物流程处理） |
| 创造模式永久化 | D-18 的 90 秒硬还原 + 下线即还原 + 登录对账（§4.12） |
| 投掷物刷实体 | `getUseDuration` + 冷却 5 tick；投掷物实体 `clientTrackingRange(4)`，不会全区广播 |
| 宝箱生成刷战利品 | `chestCount = 2` 固定；`setLootTable` 而非预生成内容，避免一次性刷满物品栏 |

### 8.4 越权保护

| 风险 | 防护 |
|---|---|
| 客户端决定伤害数值 | 所有 `hurt()` 调用在服务端路径内；客户端没有 `FarmstormExplosion` 的构造入口 |
| 客户端伪造 owner | owner 只从 `BlockPlaceContext#getPlayer()` / `Projectile#getOwner()` 读取；`getUpdateTag` 只下发不下发写入 |
| 客户端伪造转换成本 | `clickMenuButton(id)` 只有 0 一个合法值；成本由服务端按配方与配置计算 |
| 客户端跳过 10 秒硬挖 | 挖掘进度由 `ServerPlayerGameMode` 独立累计；`playerWillDestroy` 二次校验己方 |
| 客户端发假 `pendingWaves` | BE 数据只服务端写；`loadAdditional` 只在反序列化时调用 |
| 客户端触发陷阱 | 触发条件在 `TrapBehavior#findTrigger`（服务端 tick），无法被客户端调用 |
| OP 滥用 | `/setblock` 出来的部署物无 owner → 对所有人「非己方」，不能被用来当己方炮台；这是刻意的降级 |

### 8.5 世界保护与兼容

1. **T-05 底线**：任何方块替换/破坏都跳过爆炸抗性 ≥ 3.6e6 的方块（基岩、屏障、命令方块、末地传送门框架、结构方块）。
2. **`#farmstorm:explosion_immune`**：火把花/瓶子草进 tag，并且**不只拦自己的爆炸**——在 `ExplosionEvent.Detonate` 里把这些方块从 `getAffectedBlocks()` 移除，这样原版 TNT 与其它模组的爆炸也炸不掉（DEC D-14「任何爆炸都无法破坏」）。
3. **领地兼容**：v0.0.1 不接领地插件；部署物破坏受 `maxDeployedPerChunk` 约束，避免单区块堆满炮台当 lag machine。接入领地是延后项。
4. **方块实体 NBT 体积**：`getUpdateTag` 只写 5 个字段（UUID 两个 long + 2 个字符串 + 1 个 boolean），单包 < 200 字节。
5. **实体数量**：单个部署物同一时刻最多 1 个在飞子弹（冷却 > 飞行时间时为 1）；爆竹最多同时存在 1 个「引信实体」。

---

## 9. 已知技术风险与规避方案

编号 `RT-xx`（Risk Technical）。每条给出：触发条件 → 影响 → 规避 → 验证方式。

### RT-01 NeoForge 21.1.x 补丁级 API 漂移

- **触发**：`neo_version` 从 21.1.213 升到更高的 21.1.x，或降级。
- **影响**：`Explosion#finalizeExplosion`、`RecipeSerializer#streamCodec`、`FoodProperties.Builder`、`PotionContents` 这些「次常用」API 在补丁版本间偶有签名调整，编译期报错。
- **规避**：`gradle.properties` 钉死具体版本，不写动态版本；升级单独一个 PR，只改版本号后跑 `build`；对上述四处加单元级冒烟测试（编译通过即算过）。
- **验证**：`./gradlew build` 在钉死版本上连续两次成功。

### RT-02 硬挖 10 秒与客户端/服务端预测不一致

- **触发**：客户端与服务端对 `getDestroyProgress` 的输入（`BlockGetter`、`BlockPos`）解析不同；或 BE 尚未同步到客户端。
- **影响**：进度条来回跳、出现「挖到 99% 突然回弹」，玩家会报 bug。
- **规避**：`getDestroyProgress` 必须是**纯函数**——不读 `level.getGameTime()`、不读服务端才有的字段；owner 判定走已同步的 BE；对 `BE == null`（客户端区块刚加载）返回「默认挖掘速度」而不是 0。
- **验证**：创造/生存/旁观三种模式各挖一次并录屏；在 `/tp` 到远处再回来（触发区块重载）后重测。

### RT-03 临时附魔状态泄漏

- **触发**：玩家在光环内被给附魔 → 把剑丢进岩浆/箱子/末影箱 → 临时记录随物品消失或滞留。
- **影响**：残留记录导致「莫名其妙的火焰附加」，或撤销时把玩家的真实附魔改小。
- **规避**：`grant` 记录 `previous` 等级；撤销时只在「当前等级 == granted」时才还原（§4.10 不覆盖检查）；`expireAt` 用绝对 `gameTime`，物品进箱子后取出即过期；容器场景由 `PlayerTickEvent` 兜底。
- **验证**：手测 4 个场景（丢岩浆 / 放箱子 / 死亡掉落 / 直接下线）；断言物品 NBT 里组件被清空。

### RT-04 共享 `DeployedCropBlockEntity` 类注册 5 次

- **触发**：5 个 `BlockEntityType` 用同一个类 + 同一个 `BlockEntityTicker`，泛型在 `getTicker` 处需要强制转换。
- **影响**：`ClassCastException` 在运行期才暴露（编译期擦除），且只在特定方块被 tick 时触发。
- **规避**：`getTicker` 里用 `type == FarmstormBlockEntities.PUMPKIN_TURRET.get() ? (BlockEntityTicker<T>) DeployedCropBlockEntity::serverTick : null` 的显式分支，而不是 `createTickerHelper` 的泛型魔法；5 个类型共用一个 factory lambda，但每个 `BlockEntityType.Builder#of` 单独给 `Block` 列表。
- **验证**：5 种部署物各放 1 个，跑 600 tick 观察无异常；`runData` 的 `validate` 阶段校验注册表无重复 ID。

### RT-05 创造模式回滚（D-18）在异常路径下失效

- **触发**：吃到 0.1% 后服务器崩溃 / 玩家强退 / `/stop`。
- **影响**：玩家可能长期停留在创造模式（**最高严重度的玩法漏洞**）。
- **规避**：三级兜底——① 记录只在内存（`Map<UUID, CreativeRestore>`），服务器重启后自然清零；② `PlayerLoggedOutEvent` 立刻还原；③ `PlayerLoggedInEvent` 对账：若玩家当前 `GameType` 为 `CREATIVE` 且存档 `playerdata` 中记录的游戏模式不是创造（用 `ServerPlayer#gameMode` 初值 + `ServerPlayerGameMode` 的存档字段比对），则还原为 `SURVIVAL`。第三级会带来「创造服玩家被误降级」的风险，因此**只在 `level.getServer().getPlayerList().isOp(...)` 为假的玩家上执行**。
- **验证**：模拟崩溃（`kill -9` 服务端）后重连，确认模式正确；对创造服做一次回归（OP 玩家不被改动）。

### RT-06 20 格光环的性能与叠加

- **触发**：多人团队在同一基地堆 10+ 个火把花。
- **影响**：每 100 tick 对每玩家施加 6 个效果 × N 个光环 = 效果刷新风暴；粒子广播放大带宽。
- **规避**：`MAX_EFFECTS_PER_TICK` 熔断；`AuraProvider` 对同一玩家同一 tick 只接受一次施加（用 `Set<UUID>` 去重）；粒子只在「本次确实施加成功」时播。
- **验证**：10 个光环 + 8 个玩家的场景下用 `/debug profile` 观察 tick 时间 < 1 ms。

### RT-07 转换器配方缓存与 `RecipeManager` 重载

- **触发**：`/reload` 或数据包更新后，`ConverterBlockEntity.cachedRecipe` 仍指向旧对象。
- **影响**：玩家能转换已被删除的配方，或新配方不生效。
- **规避**：监听 `RecipesUpdatedEvent`（或 `AddReloadListenerEvent` 之后）遍历已加载的 `ConverterBlockEntity` 清空缓存；同时在 `tryConvert` 里对缓存做一次 `level.getRecipeManager().byKey(cached.getId())` 校验，失效则重查。
- **验证**：`/reload` 后连续转换 5 次，日志无 `Recipe not found` 异常。

### RT-08 与其它模组爆炸的兼容

- **触发**：原版 TNT、其它模组的爆炸碰到火把花/瓶子草。
- **影响**：D-14「任何爆炸都无法破坏」失效。
- **规避**：`ExplosionEvent.Detonate` 里过滤 `#farmstorm:explosion_immune`；同时给这两个方块加 `blastResistance` 之外的逻辑保护（`getExplosionResistance` 返回 `3600000F`），双保险。
- **验证**：TNT / 苦力怕 / 末地水晶各炸一次，方块仍在。

### RT-09 「圆石 6.0」阈值的模组方块歧义

- **触发**：其它模组加入抗性介于 5.9–6.1 的方块。
- **影响**：瓜片子/瓜！的破坏边界与玩家直觉不符。
- **规避**：把阈值放进配置（`explosion.sliceResistThreshold` / `explosion.grenadeResistThreshold`），并提供 `#farmstorm:blast_proof` 方块标签让整合包作者显式豁免。
- **验证**：用 `/setblock` 放一个抗性 6.0 与 5.99 的方块各一，确认前者不破后者破。

---

## 10. 版本核查

> **核查方式说明（重要）**：本机到外网的实际 HTTP 抓取被阻断（`docs.neoforged.net`、`raw.githubusercontent.com`、`maven.neoforged.net` 直连均超时），因此以下结论来自**检索命中的官方文档 URL 与 Maven 构件路径**，而非页面正文。凡标「中」/「低」置信度的条目，**以构建时实际拉取为准**，不得凭本文写死。

### 10.1 逐项结论

| # | 待核实项 | 核实结论 | 出处 | 置信度 |
|---|---|---|---|---|
| 1 | NeoForge 对应 MC 1.21.1 的具体版本号 | 版本线为 **21.1.x**；可核实存在的构件有 **21.1.213**（Maven 仓库内已有该版本的 changelog 产物）与 **21.1.209**（第三方主机商售卖页标注 MC 1.21.1）。**无法确认 21.1.213 是否为当前绝对最新**，本文取 21.1.213 作为下限，构建时以 `maven-metadata.xml` 的最大 21.1.x 为准 | `https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.213/neoforge-21.1.213-changelog.txt`；`https://scalacube.com/store/minecraft/neoforge-21.1.209` | 中高 |
| 2 | ModDevGradle 的 plugin id 与版本 | plugin id 为 **`net.neoforged.moddev`**；Gradle Plugin Portal 上存在 **2.0.140 / 2.0.148** 等 2.0.x 构件，本文取 **2.0.148** | `https://plugins.gradle.org/plugin/net.neoforged.moddev/2.0.148`；`https://plugins.gradle.org/plugin/net.neoforged.moddev.repositories/2.0.140` | 高（id）/ 中高（版本号） |
| 3 | MDG 的 `neoforge { }` 配置块与 runs 写法 | 官方文档的扩展块名为 **`neoForge { }`（大小写敏感）**，内含 `version`、可选的 `parchment`、`runs { client() / server() / data() }`、`mods { <modid> { sourceSet sourceSets.main } }`。**任务书写作 `neoforge { }` 与实际 DSL 不一致**，实现时以插件提示为准 | `https://docs.neoforged.net/toolchain/docs/plugins/mdg/`；`https://raw.githubusercontent.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/main/build.gradle`；`https://github.com/neoforged/MDK/blob/archive/1.21-mdg/build.gradle` | 高（块名）/ 中（字段细节） |
| 4 | 1.21.1 的 `DeferredRegister` 用法 | 1.21.1 文档仍有 Registries 章节，`DeferredRegister` 是标准注册方式；除 `DeferredRegister.create(Registry, modid)` 外还提供 `DeferredRegister.Items`（`createItems` / `registerItem`）、`DeferredRegister.Blocks`（`registerBlock`）、`DeferredRegister.DataComponents` 等便捷子类；注册到 mod 事件总线（`register(IEventBus)`）后由 `RegisterEvent` 统一填充 | `https://docs.neoforged.net/docs/1.21.1/concepts/registries/`；`https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/registries/DeferredRegister.html`；同目录 `DeferredRegister.DataComponents.html` | 高 |
| 5 | `DataComponents`（1.20.5 起取代物品 NBT） | 自 **1.20.5** 起物品栈的自定义数据由 **data components** 承载，取代原先的 `ItemStack` NBT；自定义组件需注册 `DataComponentType`（`DataComponentType.Builder` + Codec/StreamCodec），通过 `stack.set/get/remove` 读写。1.21.1 沿用该模型 | `https://minecraft.wiki/w/Data_component_format`；`https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/registries/DeferredRegister.DataComponents.html` | 高 |
| 6 | 1.21.1 自定义伤害类型的数据驱动方式 | 1.21.1 有独立的服务端资源文档「Damage Types」，路径为 `data/<namespace>/damage_type/<id>.json`，字段为 `exhaustion` / `message_id` / `scaling` / `effects`；要并入原版行为需在 `data/minecraft/tags/damage_type/*.json` 里追加（例如 `is_explosion`、`is_projectile`） | `https://docs.neoforged.net/docs/1.21.1/resources/server/damagetypes/`；`https://minecraft.wiki/w/Damage_type` | 高 |
| 7 | NeoForge 自定义配置（`ModConfigSpec`）在 1.21.1 的注册方式 | 1.21.1 有「Configuration」文档；用 `ModConfigSpec.Builder` 构建 `SPEC`，在模组构造器里 `modContainer.registerConfig(ModConfig.Type.SERVER, SPEC)`；值变化通过 `ModConfigEvent.Loading` / `ModConfigEvent.Reloading` 感知（用于重建缓存） | `https://docs.neoforged.net/docs/1.21.1/misc/config/` | 高 |

### 10.2 明确标注「以构建时实际拉取为准」的条目

| 项 | 为什么不确定 | 落地做法 |
|---|---|---|
| 具体的 `neo_version` 补丁号 | 无法确认当前 Maven 上的最大 21.1.x；且最新的 ≠ 最稳的 | 首次 `./gradlew build` 后把实际解析到的版本回写 `gradle.properties` 与本表 |
| ModDevGradle 的具体 `2.0.x` | 只能确认 2.0.140/148 存在，无法确认是否更高 | 以 Plugin Portal 当前最新 2.0.x 为准 |
| `FoodProperties.Builder` 方法名 | 1.21.x 内有 `saturation` / `saturationModifier`、`alwaysEdible()` 带参与否的差异 | 以 IDE 补全为准，编译期即可发现 |
| `PotionContents` / `AreaEffectCloud` 的构造方式 | 1.21 起的组件化重构 | 编译期确认；退化方案已给（直接施加 `MobEffects.POISON`） |
| `Explosion#finalizeExplosion` 是否仍为 public | 该方法是本方案的关键钩子 | 用 `javap` 或源码附件确认；若被封闭，退化方案：不继承 `Explosion`，改为独立执行器（破坏 / 伤害 / 表现三段自写），接口不变 |
| `Ingredient.CODEC` 的 JSON 形态 | `{"item":...}` 与 `{"tag":...}` 的简写在 1.21.1 可用，但数组/嵌套形态的细节未逐字核对 | 用 `runData` 生成的配方做回归（能生成即能解析） |

### 10.3 反例清单（本次**没有**采信的说法）

- 未采信「NeoForge 21.1.x 的最新版本是 21.1.72 / 21.1.100」之类记忆性数字——它们出现在旧教程里，与本次检索到的 21.1.209/21.1.213 矛盾。
- 未采信「`neoforge { }`」作为扩展块名（任务书写法），理由见 10.1 #3。
- 未采信「1.20.5 之后 `ItemStack` 仍可用 NBT 存取自定义数据」——data components 已取代该路径。

---

## 11. 验收自检

| 验收项 | 结果 | 证据 |
|---|---|---|
| 15 个作物全部出现在注册总表 | 通过 | §3.1-A 表格 15 行，ID 与 DEC §4 逐字一致：`puffed_wheat` / `blast_carrot` / `ubi_server` / `toxic_ubi_server` / `power_beet` / `melon_slice_bomb` / `melon_grenade` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` / `bamboo_firecracker` / `red_mushroom_puff` / `brown_mushroom_puff` / `lucky_berry` |
| 每个注册对象都有明确 ID | 通过 | §3.1–§3.10 共 **64 个 registry 对象**，每个都有 `farmstorm:<id>`；§3.11 给出分 registry 计数 |
| 末尾有「版本核查」小节并逐项写明结论 | 通过 | §10（含 7 项核实结论 + 6 项「以构建时为准」+ 3 条反例） |
| 只创建一个文件 | 通过 | 仅写 `docs/zh/02-TECH-SPEC.md`；`plan.txt` 与 `00-DECISIONS.md` 未改动 |
| 不实现完整代码 | 通过 | 全文只有类骨架与签名，无完整业务实现 |
| 中文撰写、代码标识符英文 | 通过 | — |

### 11.1 与 DEC 的逐条对照

| DEC | 本文落点 |
|---|---|
| D-06 `TeamResolver` | §4.2 |
| D-07 只针对非己方（陷阱/炮台） | §4.6 行为表 + §4.7 蘑菇类说明 |
| D-08 播种闭环 | T-01 + §6.7 战利品表 |
| D-09 不参与原版用途 | §4.3.1（无原版配方，天然满足） |
| D-10 种植型只留接口 | T-01（14 个作物方块，无新玩法） |
| D-11 单槽 + 数据驱动 + 双向 | §4.8.2 / §4.8.5 |
| D-12 3 点经验、下界之星单向 | §4.11 `[converter]` + §4.8.5 JSON 示例 |
| D-13 炮台硬挖 10 秒 | §4.4 破坏规则表（并按 T-04 收窄到 `pumpkin_turret`） |
| D-14 火把花/瓶子草规则 | §4.4 + §8.5 + RT-08 |
| D-15 临时附魔 | §4.10 + T-08 |
| D-16 堆叠 16 / 64 | §3.1 堆叠列 |
| D-17 随机池 14 种 | §4.12 `SPAWN_CROPS` 行 |
| D-18 创造模式 90 秒 + 下线取消 | §4.12 + RT-05 |
| D-19 圆石 6.0 阈值 | §4.9 `BlastRule.COBBLESTONE_6` + §4.11 |
| D-20 距离度量 | §4.6 表（切比雪夫 / 欧氏）+ `DistanceMetrics` |
| D-21 数值原样 + 进配置 | §4.11（117 个键，默认值与 `01-DESIGN.md` §3.8 逐项一致，且等于 plan 原值） |
| D-22 配置服务端权威 | §3.10 + §4.11 |
| D-23 支持专用服务器 | §5 + §1.4（`runServer` 门槛） |
| D-24 创造标签页 + 进度 | §3.6 + §6.7 进度 12 个（DEC C-7） |
| D-25 材质只给需求 + 脚本 | §6.5 数量清单 |
| D-26 英文语义命名 | §7.3 |
| D-27 胡罗贝 / 红棕菇不筛敌我 | §4.6 行为表 + §4.7 说明 |
| D-28 man瓜 开火间隔 60 刻 | §4.11.2 `pumpkin_turret.fireIntervalTicks` |
| D-29 食用数值 4 / 0.3、32 刻 | §4.3.4 + §4.11.1 `food.eatTicks` |
| D-30 尖牙半径 2.0 / 1.0 | §4.11.2 `fang_trap_cactus.outerRadius` / `.innerRadius` |
| D-31 其余补全项（云 3.0/600、寿命 100、粒子 8、`self`、失去依附掉落本体） | §4.11.1 + §4.11.2 + §4.11.6 |
| D-32 毒育碧服务器独立作物方块与播种物 | §3.1-A #4 + §3.2 |
| D-33 幸运浆果落点半径 2 格 | §4.12 `SPAWN_CROPS` / `LOOT_CHESTS` |
| C-6 粒子 / 音效走原版包，0 个自定义 S2C 包 | §5.5（未新增任何自定义包） |
| C-7 进度 12 条 / 24 个 lang 键 | §6.1 + §6.7 + §7.1 |
| C-8 火把花 / 瓶子草己方按原版正常速度 | §0.1 T-04 + §4.4 破坏规则表 |
| C-9 归属只存 `ownerUuid` + `ownerName`，队伍实时解析 | §4.2 + §4.5 + §5.3 |

### 11.2 后续文档的接口约束

- `03-TASKS.md` 的每个任务必须能映射到本文的**一个类或一组注册项**；跨 4 个包的改动要拆成多个任务。
- `04-ROADMAP.md` 的阶段划分建议按「注册层 → 方块/实体 → 行为 → 爆炸/配置 → 数据与资源」推进，因为注册层是所有后续编译的前提。
- `01-DESIGN.md` §3.8 是配置的**唯一命名与数值来源**；本文件 §4.11 只作实现视角的复述与「键 → 消费方」映射。任一方修改配置键，必须同步另一方，否则视为文档缺陷。







