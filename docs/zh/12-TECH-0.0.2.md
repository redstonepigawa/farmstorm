# 12 · v0.0.2 技术规格（Technical Specification）

> 对象：农场风云 / Farmstorm v0.0.2（NeoForge 21.1.213 / Minecraft 1.21.1 / Java 21）
> 唯一口径来源：`.work/v002-BRIEF.md`（下称 BRIEF）。BRIEF 里有的数字、术语、决策编号一律照抄，不改写、不重算、不换说法。
> 与 `docs/zh/00-DECISIONS.md`（下称 DEC）的关系：v0.0.2 新增内容以 BRIEF §2 为准；v0.0.1 既有内容以 DEC 为准，BRIEF 不覆盖它（D-49）。
> 本文只描述**架构与契约**，不交付实现；允许出现类名、方法签名与骨架片段。
> **本文补充** = BRIEF 未写、由本文按项目既有惯例推导并在此显式标注的内容。没有标注的结论都来自 BRIEF、DEC 或 `src/` 下的实际源码。
> 全文使用简体中文的陈述句，不用敬称、不用营销语；数值一律带单位与范围。

---

## 0. 本文范围与与 02-TECH-SPEC 的关系

### 0.1 本文负责什么

| 项 | 结论 |
|---|---|
| 复杂度档位 | L2（跨注册层 / 行为层 / 客户端渲染 / 媒体管线，多交付物，含 7 条编号风险） |
| 本文负责 | 构建基线变更、77 个注册对象的逐类核对、13 个新增对象的类设计、三个客户端子系统的技术方案、桑吉尔蔗相位同步、音频与贴图管线、资源与数据文件清单、config 新增键全量清单、0 包约束的维持证明、风险登记册 RT-10…RT-16、验收自检表 |
| 本文不负责 | 玩法数值的策划表述（`11-DESIGN-0.0.2.md`）、D-49…D-78 的逐条成行（`10-DECISIONS-0.0.2.md`）、任务拆分与依赖图（`13-TASKS-0.0.2.md`）、像素稿与音频素材本体（`06-TECH-SPEC.md` 后的 `tools/` 脚本） |
| 读者 | 实现阶段直接照本文写 `src/` 的人；以及验收阶段按 §11 逐条核对的人 |

### 0.2 与 `docs/zh/02-TECH-SPEC.md` 的关系

`02-TECH-SPEC.md` 描述 v0.0.1 的 64 个注册对象与全部既有契约，**按 D-49 冻结，v0.0.2 一个字都不改**。本文是它的增量续篇，不重抄、不重写、不覆盖：

| 主题 | v0.0.1 在哪 | 本文怎么处理 |
|---|---|---|
| 64 个既有注册对象 | `02-TECH-SPEC.md` §3.1–§3.11 | 只在 §2 里作为「存量」出现，不重列逐项表 |
| 既有类契约（物品、方块、BE、行为、投掷物、转换器） | `02-TECH-SPEC.md` §4.1–§4.13 | 引用为主；只有 v0.0.2 需要改动或继承的地方重写 |
| 服务端权威与 0 包（C-6） | `02-TECH-SPEC.md` §5 | §9 用它做**增量证明**，不重述原则 |
| 资源与语言键清单 | `02-TECH-SPEC.md` §6 / §7 | 只列新增文件与新增键；存量数字取自 BRIEF §4.1 |
| 风险 RT-01…RT-09 | `02-TECH-SPEC.md` §9 | 不复用编号；v0.0.2 的风险从 RT-10 起编，本文 §10 |
| 验收自检 | `02-TECH-SPEC.md` §11 | 本文 §11 只列 v0.0.2 的增量自检项 |

**一处失败的口径要提前说明**：`02-TECH-SPEC.md` §4.9 与 `09-IMPL-VS-PLAN.md` §1 都写「破障类一律按圆石 6.0 不破」，实测源码不是这样（只有瓜！与瓜片子带 6.0 阈值；育碧服务器 / 毒育碧服务器是 3.6e6）。勘误本身登记在 `10-DECISIONS-0.0.2.md`（D-78）；本文凡涉及破障阈值的地方一律按 D-78 的口径写，不沿用 `02-TECH-SPEC.md` §4.9 的错句。

### 0.3 三张清单的自洽承诺

BRIEF §4.2 要求由本文件产出三个自洽的数。本文的处理是**先把每一项列全再数**，清单行数与写出的计数必须完全相等：

| 清单 | 所在小节 | 清单行数 | BRIEF §4.1 存量 | 新总数 |
|---|---|---|---|---|
| 新增资源文件 | §7.2 | **36** | 262 | **298** |
| 新增语言文件键 | §7.3 | **39** | 222 | **261** |
| 新增 config 键 | §8.1 | **22** | 117 | **139** |

两条硬校验（实现阶段由 `runData` 与启动日志双向卡住）：

- 新增 `farmstorm.configuration.*` 语言键数 **22** == §8.1 的表行数 **22**；`zh_cn` 与 `en_us` 的键集必须逐字相等（`222 + 39 = 261`，两份都是 261）。
- 资源文件总数 = 迁移前 262 + 36 = **298**；按类别拆 = JSON 169+24=193、PNG 91+8=99、OGG 0+2=2、GLSL 0+2=2、TOML 1、`pack.mcmeta` 1 → 193+99+2+2+1+1 = 298。

### 0.4 数字口径（照抄 BRIEF §4）

| 指标 | v0.0.1 | v0.0.2 |
|---|---|---|
| `CropProfile` 条数 | 15 | **19** |
| 投掷型 / 部署型 / 食用型 | 8 / 5 / 2 | **10 / 6 / 3** |
| 破障类 | 4 | **4**（不变） |
| 陷阱类 | 2 | **3** |
| 炮台类 | 3 | **3** |
| buff 类 | 7 | **8** |
| 建造类 | 1 | **3** |
| 伤害类 | 8 | **9** |
| 注册对象总数 | 64 | **77** |
| items | 22 | **27** |
| blocks | 20 | **22** |
| block_entities | 6 | **6** |
| entity_types | 9 | **11** |
| mob_effects | 0 | **2** |
| sound_events | 0 | **2** |
| 转换配方 | 21 | **24** |
| 合成配方 | 0 | **2** |
| 进度 | 12 | **15** |
| 幸运浆果池 | 14 | **16** |

启动自检的目标文案（`Farmstorm#logSelfCheck` 会打印；改数字必须同步改它）：

```
自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK
```

### 0.5 「待验证」的登记规则

BRIEF §7 的每条首选方案都标了「待验证」。本文的处理：

1. **首选方案不换**（BRIEF §7 的前置条件），待验证项原样保留标记，并追加「实现阶段怎么验」——具体到一条命令、一行日志或一个手测动作。
2. 待验证分两类：**行为待验证**挂 RT-10…RT-16 中对应的那一条；**API 存在性 / 签名待验证**一律挂 RT-01（`02-TECH-SPEC.md` §9 的「补丁级 API 漂移」），不新开编号。
3. 待验证不等于「先不做」：每条都给出**当前默认实现**与**退化路径**，实现阶段按顺序试。

### 0.6 三个客户端子系统与两个效果的绑定（本文补充）

D-50 只规定「借道隐藏 MobEffect」，没有点名哪个效果承载哪一项。本文按 BRIEF §3.2 的两条备注（`wild_hunter` = 可见、带图标；`zangief_piledriver` = 隐藏、纯同步通道）与 `plan0.0.2.txt` 的原文（第 40 行「视野变为黑白色，红色高亮所有视野内的生物」写在狂野猎人条目内；第 29 行「视角上下倒转…所有的键盘与鼠标输入无效」写在桑吉尔蔗条目内）落定如下：

| 侵入性效果 | 承载它的 MobEffect | 判据 | 依据 |
|---|---|---|---|
| 整屏灰度（含 GUI） | `farmstorm:wild_hunter` | 效果存在即为开 | D-61 + `plan0.0.2.txt` 第 40 行 |
| 红色剪影 | `farmstorm:wild_hunter` | 效果存在即为开 | D-62 + 同上 |
| 相机倒转 / 自转 / 输入锁 | `farmstorm:zangief_piledriver` | amplifier 0/1/2 三相位都在锁定期内 | D-59 / D-60 / D-70 + `plan0.0.2.txt` 第 29 行 |

这条绑定决定了 §4 与 §5 的**状态来源**：客户端永远不去问服务端「你要我灰度吗」，只读自己身上的效果。这样「服务端告诉客户端状态」这件事就完全落在原版效果同步上，不需要任何新包（§9）。

---

## 1. 构建基线变更

### 1.1 `gradle.properties`：只改一行

```diff
-mod_version=0.0.1
+mod_version=0.0.2
```

| 项 | v0.0.1 | v0.0.2 | 说明 |
|---|---|---|---|
| `minecraft_version` | 1.21.1 | 1.21.1 | 不变 |
| `neo_version` | 21.1.213 | 21.1.213 | 不变（不追新，见 RT-01） |
| `mod_version` | 0.0.1 | **0.0.2** | 唯一变更 |
| `mod_license` | All Rights Reserved | 不变 | D-66 的非商业自用声明写在 README 与 Release，不改许可证 |
| Java `toolchain` | 21 | 21 | 不变 |
| ModDevGradle | `net.neoforged.moddev` 2.0.148 | 不变 | 不变 |
| `build.gradle` | — | **不动** | 没有任何新依赖：音频与 GLSL 都是资源文件，不引入运行时库 |

### 1.2 新增资源目录

v0.0.1 的 `assets/farmstorm/` 下只有 `textures/{block,item,gui}`。v0.0.2 需要新增 5 个目录（`src/main/resources/` 下）：

| 目录 | 用途 | 依据 |
|---|---|---|
| `assets/farmstorm/sounds/` | 2 个 Ogg Vorbis 音频 | D-53 / D-66 |
| `assets/farmstorm/textures/misc/` | 1×1 白色贴图（剪影用） | BRIEF §7.2 |
| `assets/farmstorm/textures/mob_effect/` | 狂野猎人效果图标 18×18 | D-53 / D-66 |
| `assets/farmstorm/shaders/core/` | 灰度着色器程序（`.json` + `.vsh` + `.fsh`） | BRIEF §7.1 |
| `assets/farmstorm/post_effect/` | 灰度后处理声明（RT-10 退化分支用） | BRIEF §3.2 |

`sounds.json` 落 `assets/farmstorm/sounds.json`（不在 `sounds/` 目录内，与原版同构）。

### 1.3 构建与验收门槛

| 门槛 | 命令 | 通过判据 |
|---|---|---|
| 编译 + 数据生成 | `.tools\gw.cmd build` | 退出码 0；`src/generated/resources` 下新增文件与 §7.2 逐项对得上 |
| 资源校验 | `.tools\gw.cmd build` 的 `processResources` 阶段 | 缺 `sounds.json` 引用的 `.ogg`、缺模型引用的 `.png` 时该阶段直接失败 |
| 启动自检 | 客户端启动日志 | §11 的 6 行「自检 …」全部 OK |
| 产物 | `dist/farmstorm-0.0.2+mc1.21.1.jar` | 存在，且大小 > v0.0.1（两个 ogg 打包进去） |

**本文不执行任何构建命令**：本文件是规格书，不是施工记录；上面三条是实现阶段的验收动作。

---

## 2. 增量注册对象总表（77 对象核对）

### 2.1 逐 registry 核对表

统计口径：**registry 对象 77 个**（跨 12 个 registry），另有 1 个 `ModConfigSpec`（非 registry，配置文件 `config/farmstorm-server.toml`）。`recipe` / `tag` / `advancement` / `post_effect` 是数据文件，不是 registry 对象，不计入本表（见 §7）。

| # | registry | v0.0.1 | 增量 | v0.0.2 | 增量明细 |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 22 | +5 | **27** | `molotov`、`zangief_cane`、`wild_apple`、`compressed_puffed_wheat`、`compressed_hay_block`(BlockItem) |
| 2 | `Registries.BLOCK` | 20 | +2 | **22** | `compressed_hay_block`（普通 `Block`）、`zangief_cane`（`DeployedCropBlock`） |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 6 | +0 | **6** | 复用 `DeployedCropBlockEntity`：把 `zangief_cane` 挂进既有 type 的合法方块集合（§3.14） |
| 4 | `Registries.ENTITY_TYPE` | 9 | +2 | **11** | `molotov`、`compressed_puffed_wheat` |
| 5 | `Registries.MOB_EFFECT` | 0 | +2 | **2** | `wild_hunter`、`zangief_piledriver` |
| 6 | `Registries.SOUND_EVENT` | 0 | +2 | **2** | `wild_hunter_loop`、`zangief_piledriver` |
| 7 | `Registries.RECIPE_TYPE` | 1 | +0 | **1** | 复用 `farmstorm:converting` |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | +0 | **1** | 复用 `ConverterRecipeSerializer` |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | +0 | **1** | 5 个新物品追加进 `farmstorm:main` |
| 10 | `Registries.DAMAGE_TYPE`（数据驱动，代码侧 key） | 2 | +0 | **2** | 莫洛托夫无伤害（§2.4） |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | +0 | **1** | — |
| 12 | `Registries.MENU` | 1 | +0 | **1** | — |
| | **合计** | **64** | **+13** | **77** | |

逐类小计（用于核对，必须与上表逐行相等）：

```
items           27   （22 + 5）
blocks          22   （20 + 2）
block_entities   6   （ 6 + 0）
entity_types    11   （ 9 + 2）
mob_effects      2   （ 0 + 2）
sound_events     2   （ 0 + 2）
recipe types     1   （ 1 + 0）
recipe serializers 1 （ 1 + 0）
creative tabs    1   （ 1 + 0）
damage_types     2   （ 2 + 0）
data_components  1   （ 1 + 0）
menu types       1   （ 1 + 0）
--------------------------------
相加 = 27+22+6+11+2+2+1+1+1+2+1+1 = 77
```

**核对结论**：`64 + 13 = 77`，12 类小计相加也是 `77`，两条算法互为校验。

### 2.2 新增的 13 个对象逐个点名

| # | registry | 完整 ID | 类 / 形态 | 依据 |
|---|---|---|---|---|
| 1 | items | `farmstorm:molotov` | `ThrowableCropItem` | BRIEF §3.2 |
| 2 | items | `farmstorm:zangief_cane` | `DeployableCropItem`（部署方块的 BlockItem） | BRIEF §3.2 |
| 3 | items | `farmstorm:wild_apple` | `FoodCropItem` | BRIEF §3.2 |
| 4 | items | `farmstorm:compressed_puffed_wheat` | `ThrowableCropItem` | BRIEF §3.1 #19 |
| 5 | items | `farmstorm:compressed_hay_block` | `BlockItem` | BRIEF §3.2 |
| 6 | blocks | `farmstorm:compressed_hay_block` | `Block`（无自定义子类，见 §3.9） | D-57 |
| 7 | blocks | `farmstorm:zangief_cane` | `DeployedCropBlock` | D-68 |
| 8 | entity_types | `farmstorm:molotov` | `MolotovProjectile`（继承 `ThrownCropEntity`） | D-58 |
| 9 | entity_types | `farmstorm:compressed_puffed_wheat` | `CompressedPuffedWheatProjectile`（继承 `ThrownCropEntity`） | D-77 |
| 10 | mob_effects | `farmstorm:wild_hunter` | `WildHunterEffect`（可见，带图标） | BRIEF §3.2 |
| 11 | mob_effects | `farmstorm:zangief_piledriver` | `ZangiefPiledriverEffect`（隐藏，纯同步通道） | D-50 / BRIEF §7.5 |
| 12 | sound_events | `farmstorm:wild_hunter_loop` | `SoundEvent`（可变距离事件） | D-76 |
| 13 | sound_events | `farmstorm:zangief_piledriver` | `SoundEvent`（可变距离事件） | D-66 |

`entity_types` 两条都沿用 v0.0.1 的 `projectile(...)` 工厂：`MobCategory.MISC`、`sized(0.25F, 0.25F)`、`clientTrackingRange(4)`、`updateInterval(10)`，与既有 8 个投掷物逐字一致（源码：`registry/FarmstormEntityTypes.java`）。

### 2.3 为什么是 13 而不是更多

三个新作物都没有种植形态（D-51）：`molotov` / `zangief_cane` / `wild_apple` 的 `cropBlock` 与 `seedItem` 全为 `null`，因此**不注册作物方块、不注册种子物品**——这一条吃掉了 3 个 blocks + 3 个 items 的增量。先例是 `melon_slice_bomb`（同样 `cropBlock = null`、`seedItem = null`，源码 `util/CropProfile.java`），所以这条不需要新机制。

### 2.4 不属于 registry 的增量（不进 77）

BRIEF §3.2 把 registry 对象与数据文件混排在一张表里。本文按性质拆开，数据文件全部落到 §7.2 的逐项清单：

| 增量 | 数量 | 性质 | 本文位置 |
|---|---|---|---|
| 转换配方 | +3（`molotov` / `zangief_cane` / `wild_apple`），总 21+3=24 | 数据文件 | §7.2 #27–#29 |
| 合成配方 | +2（转换器 1 + 压缩膨胀麦 1） | 数据文件 | §7.2 #25–#26 |
| item tag（新建） | +1 `farmstorm:converter_fuel` | 数据文件 | §7.2 #35 |
| vanilla tag（追加 / 新建） | 2 处：`minecraft:mineable/pickaxe` 追加、`minecraft:needs_diamond_tool` 新建 | 数据文件 | §7.2 #36（新建）+ 既有 `data/minecraft/tags/block/mineable/pickaxe.json` 追加（非新增文件，故不进 36 行） |
| advancements | +3 | 数据文件（datapack 注册表，无 `DeferredRegister`） | §7.2 #30–#32 |
| post_effect | +1 | 数据文件 | §7.2 #15 |
| damage_types | +0 | 莫洛托夫**不造成伤害**：落点爆炸威力 0，且 `BlastRule.NEVER_BREAK` 不破方块（D-58），没有可归属的伤害来源 | 本节 |
| data components / recipe type / recipe serializer / creative tabs / menu types | +0 | 全部复用 | 本节 |

**damage_types 为什么可以是 +0**：`FarmstormExplosion` 的构造要一个 `DamageSource`，但 `damageCap <= 0` 时它跳过整个伤害段（源码 `explosion/FarmstormExplosion.java` 的构造器注释：「<= 0 表示纯破坏型，跳过伤害段」）。莫洛托夫用 `damageCap = 0` + `BlastRule.NEVER_BREAK`，两个通道都是空操作，因此连伤害来源都用不上，不必新增 `farmstorm:molotov` 伤害类型。

---

## 3. 新增类的设计

每个类给五项：**职责 / 关键方法 / 读哪些 config 键 / 写哪些注册对象 / 依据**。「读哪些 config 键」里出现的键名都能在 §8.1 的 22 行清单里查到；「写哪些注册对象」只列 registry 对象，数据文件见 §7。

### 3.0 新增与改动总览

| 类 | 新建 / 改动 | 一句话职责 | 写哪些注册对象 |
|---|---|---|---|
| `CropProfile` | 改动 | 4 条新档案常量 + `ALL` 19 条 + `LUCKY_BERRY_POOL` 16 条 | 无（供注册层读取） |
| `DeployedKind` | 改动 | 枚举 +1 值 `ZANGIEF_CANE(10)` | 无 |
| `FarmstormMobEffects` | **新建** | `DeferredRegister` 承载 2 个 `MobEffect` | mob_effects ×2 |
| `FarmstormSounds` | **新建** | `DeferredRegister` 承载 2 个 `SoundEvent` | sound_events ×2 |
| `WildHunterEffect` | **新建** | 可见效果：属性加成 + 客户端读它决定灰度与剪影 | 经 `FarmstormMobEffects` |
| `ZangiefPiledriverEffect` | **新建** | 隐藏效果：只承载 amplifier 相位号 | 经 `FarmstormMobEffects` |
| `MolotovProjectile` | **新建** | 落点触发 0 威力爆炸，球内逐格掷火 / 岩浆块 | entity_types ×1 |
| `CompressedPuffedWheatProjectile` | **新建** | 膨胀麦的替换块换成压缩干草块 | entity_types ×1 |
| `ZangiefCaneTrap` | **新建** | `TrapBehavior` 实现：球形 1.5 格触发 | 无 |
| `ZangiefPiledriverController` | **新建** | 服务端三相位状态机 | 无 |
| `WildHunterCombat` | **新建** | 服务端目标复核、伤害与击退、击杀续时 | 无 |
| `GrayscalePass` | **新建** | 客户端整屏灰度 pass | 无 |
| `WildHunterOutline` | **新建** | 客户端红色剪影 | 无 |
| `InputLockHandler` | **新建** | 客户端输入锁（5 个事件） | 无 |
| `ZangiefCameraController` | **新建** | 客户端渲染层角度（roll / pitch / yaw） | 无 |
| `WildHunterLoopSound` | **新建** | 客户端循环 `SoundInstance` | 无 |
| `WildHunterDashClient` | **新建** | 客户端冲刺预测 | 无 |
| `compressed_hay_block` | **不新增类** | 纯属性方块，用原版 `Block` | blocks ×1 + items ×1 |

### 3.1 `CropProfile`：4 条新常量、`ALL` 19 条、`LUCKY_BERRY_POOL` 16 条

**职责**：单个改良作物的静态档案。物品 / 方块 / 实体 / tooltip / 自检都从这里取数据，避免同一个注册 ID 在多处各写一份字符串（源码 `util/CropProfile.java` 的类注释）。

**关键方法：不变**。`crop()` / `throwable()` / `deployed()` 三个私有工厂的签名一个都不动；新常量按「有什么参数就用哪个工厂」落位：

| # | 常量 | 构造方式 | type | categories | cropBlock | seedItem | kind | 依据 |
|---|---|---|---|---|---|---|---|---|
| 16 | `MOLOTOV` | `throwable("molotov", …)` 不可用 → **`new CropProfile(...)`** | `THROWABLE` | `DAMAGE, CONSTRUCTION` | `null` | `null` | `null` | D-51 |
| 17 | `ZANGIEF_CANE` | `new CropProfile(...)` | `DEPLOYABLE` | `TRAP` | `null` | `null` | `ZANGIEF_CANE` | D-51 |
| 18 | `WILD_APPLE` | `new CropProfile(...)` | `EDIBLE` | `BUFF` | `null` | `null` | `null` | D-51 |
| 19 | `COMPRESSED_PUFFED_WHEAT` | `new CropProfile(...)` | `THROWABLE` | `CONSTRUCTION` | `null` | `null` | `null` | D-56 |

四个常量都必须直接走 record 构造器，因为三个私有工厂都会对 `cropBlockPath` / `seedItemPath` 调 `blockKey()` / `itemKey()`（`ResourceKey.create`），传 `null` 会在类初始化时 NPE。先例就是 `MELON_SLICE_BOMB`：它同样没有作物方块，写法是裸 `new CropProfile(...)`。

堆叠档位：`MOLOTOV` 与 `COMPRESSED_PUFFED_WHEAT` 取 `THROWABLE_STACK_TIER`（16，D-16）；`ZANGIEF_CANE` 与 `WILD_APPLE` 取 `OTHER_STACK_TIER`（64，D-16）。
附着面：`ZANGIEF_CANE` 取 `Set.of(AttachFace.FLOOR)`（只贴地，与既有两个陷阱一致）。

**`ALL` 的顺序**（= BRIEF §3.1 的行号 1–19）：

```
1  PUFFED_WHEAT            11 FANG_TRAP_CACTUS
2  BLAST_CARROT            12 BAMBOO_FIRECRACKER
3  UBI_SERVER              13 RED_MUSHROOM_PUFF
4  TOXIC_UBI_SERVER        14 BROWN_MUSHROOM_PUFF
5  POWER_BEET              15 LUCKY_BERRY
6  MELON_SLICE_BOMB        16 MOLOTOV
7  MELON_GRENADE           17 ZANGIEF_CANE
8  PUMPKIN_TURRET          18 WILD_APPLE
9  AWAKENED_TORCHFLOWER    19 COMPRESSED_PUFFED_WHEAT
10 AWAKENED_PITCHER_PLANT
```

顺序不是装饰：`BY_ID` 用它建 `LinkedHashMap`，tooltip 与启动自检按这个顺序输出（源码注释：「`Set.of` 的迭代顺序由 JVM 哈希盐决定，逐次运行可能不同，不能用来展示」）。

**`LUCKY_BERRY_POOL`：从自动推导改为显式排除三人**。v0.0.1 是 `ALL.stream().filter(p -> p != LUCKY_BERRY)`；v0.0.2 必须写成三个显式排除：

```java
/** D-55 + D-56：19 条里排除 lucky_berry、wild_apple、compressed_puffed_wheat → 16 条。 */
public static final List<CropProfile> LUCKY_BERRY_POOL = ALL.stream()
        .filter(profile -> profile != LUCKY_BERRY)
        .filter(profile -> profile != WILD_APPLE)
        .filter(profile -> profile != COMPRESSED_PUFFED_WHEAT)
        .toList();
```

| 排除项 | 理由 | 依据 |
|---|---|---|
| `lucky_berry` | 自己不能产出自己（v0.0.1 既有口径） | D-17 |
| `wild_apple` | 本版最强道具，不该由 1 颗浆果随机产出 | D-55 |
| `compressed_puffed_wheat` | 二级合成物，不是「改良农作物」 | D-56 |

`19 - 3 = 16`，与 BRIEF §4 的「幸运浆果池 16」一致。**注意 D-55 的原文写「显式排除两人」**，按字面只有 17 条；16 条这个数字来自 D-56 的第三次排除与 BRIEF §4 的数字表，本文以数字表为准（已在 §0 的口径里对齐）。

**读哪些 config 键**：本类不读 config；`throwStack` 以外的堆叠数由 `FarmstormItems` 读 `stack.throwable` / `stack.other` 决定（v0.0.1 既有机制）。
**写哪些注册对象**：无。它是注册层的输入。

### 3.2 `DeployedKind`：+1 值 `ZANGIEF_CANE(10)`

**职责**：部署方块的**实现维度**枚举（不是 `CropType`，也不是 `CropCategory`）。`scanInterval` 放在枚举上，因为 `DeployedCropBlockEntity` 的错峰 tick 在行为对象就绪之前就要用它（源码注释，§4.5）。

```java
public enum DeployedKind {
    BLAST_CARROT(10),
    FANG_TRAP_CACTUS(10),
    PUMPKIN_TURRET(10),
    AWAKENED_TORCHFLOWER(100),
    AWAKENED_PITCHER_PLANT(100),
    ZANGIEF_CANE(10);        // 新增：与另两个陷阱一致
    …
}
```

**关键方法**：`scanInterval()` 不变。新值取 10 刻（0.5 秒），与 `BLAST_CARROT` / `FANG_TRAP_CACTUS` 一致——陷阱是「一次触发即结算」，10 刻的扫描延迟在 1.5 格半径下不会漏判：玩家最快 0.28 格/刻（疾跑 + 速度 II 上限附近），10 刻最多 2.8 格，靠 0.5 秒的判定期足以让「路过」被捕获（本文补充：口径沿用既有两个陷阱的实测手感，未新增数值）。

**加一个枚举值会打断两处穷尽 switch**（编译期就报错，这是设计上的哨兵，不是意外）：

| 文件 | 位置 | 必须补什么 | 在 BRIEF §3.3 白名单里吗 |
|---|---|---|---|
| `behavior/DeployedBehaviors.java` | `of(DeployedKind)` 的 `switch` 表达式 | `case ZANGIEF_CANE -> ZANGIEF_CANE_TRAP;` + 一个静态单例字段 | **不在**（见 §3.14 的矛盾登记） |
| `registry/FarmstormBlockEntities.java` | `deployedOf(DeployedKind)` 的 `switch` 表达式 | `case ZANGIEF_CANE -> FANG_TRAP_CACTUS.get();` | **不在**（同上） |

**读哪些 config 键**：不直接读；行为对象 `ZangiefCaneTrap` 读 `zangief_cane.triggerRadius`。
**写哪些注册对象**：无。

### 3.3 `FarmstormMobEffects`（新建 `DeferredRegister`）

**职责**：本模组第一个 `MobEffect` 注册器。v0.0.1 的 `mob_effects` 是 0（BRIEF §4），因此这个类是全新的，不继承任何既有注册器。

```java
public final class FarmstormMobEffects {
    public static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(Registries.MOB_EFFECT, Farmstorm.MOD_ID);

    /** 可见、带图标：灰度 + 红色剪影 + 三项属性加成的载体（D-61 / D-62）。 */
    public static final DeferredHolder<MobEffect, WildHunterEffect> WILD_HUNTER =
            REGISTER.register("wild_hunter", WildHunterEffect::new);

    /** 隐藏、无图标：只承载 amplifier 相位号（D-50 / BRIEF §7.5）。 */
    public static final DeferredHolder<MobEffect, ZangiefPiledriverEffect> ZANGIEF_PILEDRIVER =
            REGISTER.register("zangief_piledriver", ZangiefPiledriverEffect::new);

    private FarmstormMobEffects() {}
}
```

**关键方法**：无业务方法，只有两个 `Holder`。挂载点是 `Farmstorm` 构造器里加一行 `FarmstormMobEffects.REGISTER.register(modBus);`（与既有 9 个注册器并列）。

**读哪些 config 键**：不读。
**写哪些注册对象**：`farmstorm:wild_hunter`、`farmstorm:zangief_piledriver`（mob_effects ×2）。

**为什么不用别的办法**：D-50 已经判死——三项侵入性效果的状态不许走 `CustomPacketPayload`，只能借道隐藏 `MobEffect`。`MobEffect` 的「在 / 不在 + amplifier + 剩余时长」三个分量恰好等于全部需要同步的状态，且 `ClientboundUpdateMobEffectPacket` 免费给这三样（§9.2）。

### 3.4 `FarmstormSounds`（新建 `DeferredRegister`）

```java
public final class FarmstormSounds {
    public static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(Registries.SOUND_EVENT, Farmstorm.MOD_ID);

    /** D-76：循环音，可听距离 16 格由客户端 SoundInstance 与 sounds.json 共同决定。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> WILD_HUNTER_LOOP =
            REGISTER.register("wild_hunter_loop",
                    () -> SoundEvent.createVariableRangeEvent(Farmstorm.id("wild_hunter_loop")));

    /** D-66：桑吉尔蔗触发音，单次播放。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> ZANGIEF_PILEDRIVER =
            REGISTER.register("zangief_piledriver",
                    () -> SoundEvent.createVariableRangeEvent(Farmstorm.id("zangief_piledriver")));

    private FarmstormSounds() {}
}
```

**关键方法**：无。用 `createVariableRangeEvent` 而不是 `createFixedRangeEvent`：循环音的可听距离要由 §6.3 的 `sounds.json` 与 `SoundInstance` 一起控制（16 格），固定距离事件会把半径钉死在服务端。

**读哪些 config 键**：不读。
**写哪些注册对象**：`farmstorm:wild_hunter_loop`、`farmstorm:zangief_piledriver`（sound_events ×2）。

### 3.5 `WildHunterEffect`（可见效果）

**职责**：狂野猎人的状态载体。它本身**不做玩法**（索敌 / 冲刺 / 击杀续时都在 §3.12 的服务端类里），只负责三件注册层的事：分类与图标、三项属性加成的挂载与卸下、给客户端一个「效果在不在」的判据。

| 形状项 | 取值 | 依据 |
|---|---|---|
| 类 | `public class WildHunterEffect extends MobEffect` | 1.21.1 的 `MobEffect` 是类不是接口 |
| 分类 | `MobEffectCategory.BENEFICIAL` | 玩家视角是增益 |
| 颜色（粒子与图标底色） | `0x8B1A1A`（暗红）**本文补充** | 与红色剪影同色系 |
| 图标 | `assets/farmstorm/textures/mob_effect/wild_hunter.png`（18×18，D-53） | D-66 |
| 可见性 | `visible = true`、`showIcon = true`（施加时传） | BRIEF §3.2「可见，带图标」 |
| 瞬时 | `isInstantenous() = false` | 有时长 |

**关键方法**：

```java
@Override
public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
    attributes.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_ID,
            FarmstormConfig.wildHunterSpeedBonus() / 100.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    attributes.addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_ID,
            FarmstormConfig.wildHunterAttackSpeedBonus() / 100.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    attributes.addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID,
            FarmstormConfig.wildHunterAttackDamageBonus() / 100.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
}

@Override
public void removeAttributeModifiers(AttributeMap attributes, int amplifier) { …三个 removeAttributeModifier… }
```

三个 `AttributeModifier` 的 id 必须是固定的 `ResourceLocation`（`farmstorm:wild_hunter_speed` 等）并声明成静态常量——每次新建 id 会导致「卸不掉」。**待验证（挂 RT-01）**：1.21.1 的 `AttributeModifier` 构造器是否已经是 `(ResourceLocation, double, Operation)` 形态（1.21 改过 id 类型）；以编译期签名为准。

**读哪些 config 键**：`wild_hunter.speedBonusPercent`（30.0 %，0.0–1000.0）、`wild_hunter.attackSpeedBonusPercent`（30.0 %，0.0–1000.0）、`wild_hunter.attackDamageBonusPercent`（40.0 %，0.0–1000.0）。
**写哪些注册对象**：经 `FarmstormMobEffects.WILD_HUNTER` 写 mob_effects ×1。

**为什么属性加成放在 `MobEffect` 上而不是 tick**：原版对效果的属性修饰器在**两侧**都生效（`LivingEntity#onEffectAdded` 在客户端同样调用），所以移速/攻速/伤害在客户端预测里也能立刻感到，不需要额外同步；写成 `ServerPlayer` 每 tick 手动 `getAttribute().addTransientModifier` 反而会出现「客户端慢一帧」并且要自己管卸下。

### 3.6 `ZangiefPiledriverEffect`（隐藏效果，纯同步通道）

**职责**：**只有一个职责**——把「桑吉尔蔗打桩机的相位号」从服务端送到客户端。它不加任何属性、不造成任何伤害、不产生任何粒子。

| 形状项 | 取值 | 依据 |
|---|---|---|
| 类 | `public class ZangiefPiledriverEffect extends MobEffect` | **待验证（挂 RT-01）**：1.21.1 的 `MobEffect` 是否为可继承的类（构造签名为 `(MobEffectCategory, int color)`），以及 `MobEffectInstance(MobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)` 这个六参构造是否存在；以编译期为准 |
| 分类 | `MobEffectCategory.HARMFUL` | 被打桩机是负面体验 |
| 颜色 | `0x000000`（隐藏效果，颜色不会被看到） | — |
| 可见性 | `visible = false`、`showIcon = false`（施加时传） | BRIEF §3.2「隐藏，纯同步通道」 |
| amplifier 语义 | `0`=已倒转等待漂浮 / `1`=漂浮中 / `2`=下坠自转 | BRIEF §7.5 |
| 时长语义 | 当前相位的剩余刻数（每次切换重设） | §5.2 |

**关键方法**：

```java
@Override public boolean isDurationEffectTick(int duration, int amplifier) { return false; }  // 不靠 vanilla tick 做任何事
@Override public boolean shouldRenderHUD() { return false; }        // 隐藏：不画 HUD 图标
@Override public boolean shouldRenderInvText() { return false; }    // 隐藏：不写进物品栏描述
/** 不覆盖 addAttributeModifiers / removeAttributeModifiers：本效果没有任何属性影响。 */
```

**待验证（挂 RT-15）**：1.21.1 的 `MobEffect` 是否仍提供 `shouldRenderHUD()` / `shouldRenderInvText()` 两个可覆写方法；若只有 `shouldRenderIcon()`，则改为覆写它——隐藏效果的可见性最终由 `MobEffectInstance` 构造参数 `(visible, showIcon)` 保证，类上的覆写只是第二道保险。

**读哪些 config 键**：不读。时长由施加方（`ZangiefCaneTrap` / `ZangiefPiledriverController`）按 config 算好了传进来。
**写哪些注册对象**：经 `FarmstormMobEffects.ZANGIEF_PILEDRIVER` 写 mob_effects ×1。

### 3.7 `MolotovProjectile`（投掷物 #9）

**职责**：落点做两件事——(1) 触发一次**威力 0** 的爆炸作为落点触发器，(2) 以落点为球心按球半径遍历方块，对「上表面接触空气」的每一格**独立**掷一次，85 % 在其上方的空气位放火、15 % 放岩浆块。

```java
public class MolotovProjectile extends ThrownCropEntity {
    @Override protected CropProfile profile() { return CropProfile.MOLOTOV; }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        BlockPos center = landingBlock(pos, directHit == null);   // 与膨胀麦同一套落点反推
        trigger(pos);                                             // (1) 0 威力爆炸，只当触发器
        scatter(level, center);                                   // (2) 逐格独立掷
    }
}
```

**关键方法**：

| 方法 | 做什么 | 要点 |
|---|---|---|
| `profile()` | 返回 `CropProfile.MOLOTOV` | 必须是**静态常量**：`ThrownCropEntity` 的 `getDefaultItem()` 在 `defineSynchedData` 期间就会被调到，那时子类实例字段还没初始化（基类注释） |
| `onImpact(ServerLevel, Vec3, Entity)` | 唯一行为入口，仅服务端 | 基类保证每次生成只结算一次（`settled` 防重入） |
| `trigger(Vec3)` | `new FarmstormExplosion(level, owner, FarmstormDamageTypes.cropBlast(level, owner), x, y, z, power, BlastRule.NEVER_BREAK, 0F).detonate()` | `power = molotov.triggerExplosionPower`（默认 0）、`damageCap = 0`（跳过伤害段）、`NEVER_BREAK`（0 方块破坏） |
| `scatter(ServerLevel, BlockPos)` | 球内遍历 + 逐格 `nextFloat()` | 判据见下 |
| `landingBlock(Vec3, boolean)` | 命中面内侧方块 | 与 `PuffedWheatProjectile` 同算法（`LANDING_PROBE = 0.1` 的 epsilon），见 §3.8 的重复代码说明 |

**`scatter` 的判据（逐条固定，避免实现阶段各写一套）**：

1. 候选集合 = `BlockPos.betweenClosed(center.offset(-r,-r,-r), center.offset(r,r,r))` 里满足 `center.distSqr(pos) <= r*r` 的格（`r = molotov.effectRadius`，默认 4.0 格 → 球内约 268 格）。
2. 该格「上表面接触空气」= `level.getBlockState(pos).isSolidRender(...)` 为真 **且** `level.getBlockState(pos.above()).isAir()` 为真。**本文补充**：只认「实心 + 上面是空气」，不认可穿过方块（草、雪层），避免「草方块顶上放火」变成「方块的草上放火」这种视觉错误。
3. 每个候选格**独立**掷：`float roll = level.random.nextFloat() * 100.0F; if (roll < fireChance) 放火 else 放岩浆块`。`fireChance = molotov.fireChance`（默认 85.0 %）→ 余下的 15 % 自动是岩浆块，不需要第二个键。
4. 写入位置 = **上方那个空气位**（`pos.above()`），一律 `level.setBlock(target, state, Block.UPDATE_ALL)`。
5. 不替换、不破坏原方块：`pos` 自身一个字节都不动；原方块没有掉落、没有粒子。
6. 单次投掷的写入上限 = 球内候选格数（≈268），不另设熔断——268 次 `setBlock` 在同一 tick 内是可接受的量级（对照 v0.0.1 膨胀麦湿态 5×5×5 = 125 次 / 一次投掷）。

**为什么用 0 威力爆炸**：D-58 的原文是「落点触发一次威力 0 的爆炸，该爆炸只当落点触发器」。威力 0 时原版伤害公式得 `Infinity` 之外的退化值（半径 0 无实体进入），加上 `damageCap = 0` 直接跳过我们的伤害段，是**两道**空操作；同时保留 `ExplosionEvent.Start` 这个事件钩子——领地 / 保护类模组仍有机会否决它（与 v0.0.1 的 `FarmstormExplosion#detonate` 一致）。

**为什么不复用 `BlastRule.forCrop`**：`forCrop(MOLOTOV, …)` 会走到 `NEVER_BREAK`（`molotov` 没有 `DEMOLITION` 标签），结果相同但多绕一层；直接写 `NEVER_BREAK` 更清楚地表达「这次爆炸压根不该破方块」。**D-19 铁律的落点**：`scatter` 放的是火与岩浆块，是**追加**，不是破坏——原方块一个都没少，因此不违反「破坏权限只由 `DEMOLITION` 决定」。

**读哪些 config 键**：`molotov.triggerExplosionPower`（0，0–100）、`molotov.effectRadius`（4.0 格，0.0–16.0）、`molotov.fireChance`（85.0 %，0.0–100.0）。
**写哪些注册对象**：`farmstorm:molotov`（entity_types ×1，在 `FarmstormEntityTypes` 里用既有 `projectile(...)` 工厂注册）。

### 3.8 `CompressedPuffedWheatProjectile`（投掷物 #10）

**职责**：与膨胀麦**完全同一套**落点语义（水位判定、干/湿边长、写入式替换、3.6e6 防破坏底线），唯一差异是写入的方块从 `minecraft:hay_block` 换成 `farmstorm:compressed_hay_block`（D-77）。

```java
public class CompressedPuffedWheatProjectile extends PuffedWheatProjectile {
    @Override protected CropProfile profile() { return CropProfile.COMPRESSED_PUFFED_WHEAT; }

    /** D-77：唯一差异——父类写入的方块从 minecraft:hay_block 换成压缩干草块。 */
    @Override protected BlockState fillBlock() {
        return FarmstormBlocks.COMPRESSED_HAY_BLOCK.get().defaultBlockState();
    }
}
```

**必须沿用的四个细节**（逐条对齐 `entity/thrown/PuffedWheatProjectile.java`，不许「大致相同」）：

| 细节 | 取值 / 算法 | 源码依据 |
|---|---|---|
| 落点反推步长 | `LANDING_PROBE = 0.1`，沿飞行方向从命中点往里探 0.1 格 | 该类注释：基类把命中点沿外法线推出 0.05 格，命中点落在命中方块外侧 |
| 水源判定 | 流体 `#minecraft:water` 且 `isSource()`，只在**干态**立方体内判一次 | 「水源只在干态立方体内判定一次，命中即改用湿态边长，不做二次判定」 |
| 边长 | 干 `puffed_wheat.dryEdge`（3，1–15）、湿 `puffed_wheat.wetEdge`（5，1–15） | 源码读这两个键 |
| 防破坏底线 | `getBlock().getExplosionResistance() >= BlastRule.UNBREAKABLE_RESISTANCE`（3.6e6）→ 跳过该格 | D-38 / T-05 |

**读哪些 config 键**：`puffed_wheat.dryEdge`、`puffed_wheat.wetEdge`（**不新增键**，D-77 的「完全沿用」在这里就是「读同一组键」）。
**写哪些注册对象**：`farmstorm:compressed_puffed_wheat`（entity_types ×1）。

**继承而不是复制**：`entity/thrown/PuffedWheatProjectile.java` **在 BRIEF §3.3 的可改文件白名单内**（v0.0.2 的裁决把它与 `DeployedBehaviors.java` / `FarmstormBlockEntities.java` 一并补入）。因此做法是给父类抽一个扩展点、子类只覆写它：

| 改动点 | 父类 `PuffedWheatProjectile` | 子类 `CompressedPuffedWheatProjectile` |
|---|---|---|
| 写入的方块 | 新增 `protected BlockState fillBlock()`，方法体返回 `Blocks.HAY_BLOCK.defaultBlockState()` | 覆写为 `FarmstormBlocks.COMPRESSED_HAY_BLOCK.get().defaultBlockState()` |
| 写入循环 | `fillHay(...)` 里把写死的 `BlockState hay = Blocks.HAY_BLOCK.defaultBlockState()` 换成 `BlockState hay = this.fillBlock()`（把该方法从 `private static` 改成实例方法） | 不重写循环 |
| 落点反推 `landingBlock` | 不动 | 不重写 |
| 水源判定 `hasWaterSource` | 不动 | 不重写 |
| `onImpact` | 不动 | 不重写 |

父类共 4 行改动（1 个新方法 + 1 行改用 + 方法签名由 `private static` 转实例）。这样 D-77 的「完全沿用」是**字面成立**的：子类走的仍是同一份落点反推、水源判定与写入循环，代码路径只有「方块」一个变量不同，不存在两份可能各自漂移的实现。13-TASKS 的 F-68 采用的也正是这个方案，两份文档口径一致。

### 3.9 `compressed_hay_block`：不新增方块类

**职责**：一个纯属性方块，六面同贴图、不可旋转、无 `BlockEntity`、无行为。**结论：不写新类**，用原版 `net.minecraft.world.level.block.Block`。

```java
// registry/FarmstormBlocks.java
public static final DeferredBlock<Block> COMPRESSED_HAY_BLOCK = REGISTER.registerBlock("compressed_hay_block",
        Block::new,
        BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(30.0F, 6.0F)                 // D-57：硬度 30.0F、抗性 6.0F
                .requiresCorrectToolForDrops()
                .sound(SoundType.GRASS));              // 与干草块同音
```

| D-57 的形状要求 | 落实方式 |
|---|---|
| `strength(30.0F, 6.0F)` | 一个方法两个参数同时管硬度与抗性（`ANCIENT_DEBRIS` 是 `strength(30.0F, 1200.0F)`，`HAY_BLOCK` 是 `strength(0.5F)`） |
| `requiresCorrectToolForDrops()` | 不加这个就不掉本体；配套 `minecraft:mineable/pickaxe` + `minecraft:needs_diamond_tool` 两条标签 |
| 六面方块、不可旋转 | 不引入 `RotatedPillarBlock`，blockstate 走 `cube_all`，只有 1 个变体 |
| 材质 = 干草块侧面的灰度版 | `assets/farmstorm/textures/block/compressed_hay_block.png`（§6.5） |

**取 6.0 抗性的后果，必须写进文档与 tooltip 说明**（D-57 明确要求）：

| 爆炸来源 | 阈值 | 对本方块 | 原因 |
|---|---|---|---|
| 瓜！`MelonGrenadeProjectile` | `explosion.grenadeResistThreshold` = 6.0 | **破不动** | 判定是 `resistance < maxResistance`，6.0 不小于 6.0 |
| 瓜片子 `MelonSliceProjectile` | `explosion.sliceResistThreshold` = 6.0 | **破不动** | 同上 |
| 育碧服务器 `UbiServerProjectile` | `UNBREAKABLE_SKIP`，3.6e6 | **破得动** | 6.0 < 3.6e6 |
| 毒育碧服务器 `ToxicUbiServerProjectile` | `VANILLA`，3.6e6 | **破得动** | 同上 |
| 原版爆炸（含 TNT） | 原版 `Explosion` 口径 | **破得动** | 原版 `Explosion` 逐格累积 `f -= (resistance + 0.3) * 0.3`、`f > 0` 即破：TNT 威力 4 时 `f` 从 `[2.8, 5.2]` 起，扣掉 6.0 抗性对应的 1.89 之后仍常为正，6.0 抗性挡不住圆石级别的方块 |

**读哪些 config 键**：**不读任何键**。D-57 把三个数字钉死了（30.0F / 6.0F / `requiresCorrectToolForDrops`），而且这三个是方块固有属性、不是可调玩法数值；把它们做成 config 会违反「既有 config 键默认值不许改」的精神（新增一个可调键等于给平衡性留后门），也与 v0.0.1 把 `CONVERTER` 的 `strength(3.5F)` 写死在同一处的做法一致。
**写哪些注册对象**：`farmstorm:compressed_hay_block`（blocks ×1）+ 同名 BlockItem（items ×1，在 `FarmstormItems` 用 `REGISTER.registerItem("compressed_hay_block", …)`）。

### 3.10 `ZangiefCaneTrap`（`TrapBehavior` 实现）

**职责**：桑吉尔蔗的**触发判定**与**触发瞬间**的全部动作。相位推进不在这里（§3.11），因为相位要按玩家 tick 走，而陷阱方块一触发就没了（D-72）。

```java
public final class ZangiefCaneTrap implements TrapBehavior {
    @Override public DeployedKind kind() { return DeployedKind.ZANGIEF_CANE; }

    @Override public Optional<Player> findTrigger(DeployedCropBlockEntity be) { … }
    @Override public void trigger(DeployedCropBlockEntity be, Player target) { … }
}
```

**关键方法**：

| 方法 | 规则 | 依据 |
|---|---|---|
| `scanInterval()` | 沿用 `TrapBehavior` 的默认 10 刻 | `DeployedKind.ZANGIEF_CANE(10)` |
| `findTrigger(be)` | 遍历 `level.players()`；跳过 `isSpectator()` / `isCreative()` / `be.isAlly(player)`；判据是 **球形欧氏距离** `pos.distSqr(player.position()) <= r*r`，`r = zangief_cane.triggerRadius`（1.5 格） | D-71（只对非己方玩家、创造与旁观豁免）、D-73（球形，不是切比雪夫） |
| `trigger(be, target)` | ① 给 trigger 施加 `zangief_piledriver` amplifier 0；② `level.playSound(null, pos, FarmstormSounds.ZANGIEF_PILEDRIVER.get(), SoundSource.PLAYERS, 1.0F, 1.0F)`；③ 记录 `(playerId → 触发 tick)`；④ `be.consume()`（静默移除 + 破坏粒子） | D-72（静默消失、无掉落）、D-66（播放触发音） |

**为什么距离判定用 `player.position()` 而不是 `player.blockPosition()`**：`BLAST_CARROT` 用 `blockPosition()` 是因为它是**切比雪夫**判定（格与格之间比整数）；`FANG_TRAP_CACTUS` 用欧氏距离，但比对的是方块的`Vec3`中心与实体的精确位置（源码 `FangTrapCactusTrap`）。桑吉尔蔗跟后者同族：`pos.getCenter().distanceTo(player.position()) <= 1.5`。**本文补充**：1.5 格是「从方块中心算起」而不是「从方块边界算起」，这样玩家站在陷阱正上方贴着站也算命中（中心距 0.5 左右），而站在相邻方块中心（距 1.0 或 1.41）也算——这正是 1.5 这个数想要的效果。

**触发后陷阱立刻消失顺序**：先施加效果与音效，再 `consume()`。`consume()` 内部是 `level.removeBlock(pos, false)` + `levelEvent(PARTICLES_DESTROY_BLOCK)`，方块一没了 `DeployedCropBlockEntity` 也随即被移除；反过来做的话 `be.getBlockPos()` 与 `be.getLevel()` 会先失效（v0.0.1 的 `BlastCarrotTrap` 注释里有同类教训：必须先摘方块再引爆，否则采样射线被自己挡住）。

**读哪些 config 键**：`zangief_cane.triggerRadius`（1.5 格，0.0–8.0）、`zangief_cane.stageDelayTicks`（14 刻，0–200）、`zangief_cane.levitationLevel`（7 级，1–255）、`zangief_cane.levitationDurationTicks`（20 刻，0–72000）、`zangief_cane.fallTicks`（40 刻 = 2 秒，1–200）、`zangief_cane.spinDegreesPerTick`（72.0 度/刻，0.0–360.0）。
**写哪些注册对象**：无（行为对象是静态单例，不注册）。

### 3.11 `ZangiefPiledriverController`（服务端三相位状态机）

**职责**：触发之后的一切**时序**都归它：14 刻后给漂浮、漂浮结束后给向下速度、落地或超时后摘效果。它挂在 `PlayerTickEvent.Post` 上，只在服务端跑。

```java
public final class ZangiefPiledriverController {
    /** 安全阀：相位 2 最长持续刻数，防止玩家卡在半空时效果永不结束。不进 config（HC-13 同类）。 */
    public static final int MAX_FALL_TICKS = 100;
    /** 只存内存：UUID → 相位状态。服务器重启即清空，不写存档（D-18 的先例）。 */
    private static final Map<UUID, PhaseState> ACTIVE = new HashMap<>();
    public static void onPlayerTick(ServerPlayer player) { … }
}
```

**相位表**（amplifier 与时间的唯一真相）：

| 相位 | amplifier | 进入时刻 | 持续 | 服务端动作 | 客户端读到的语义 |
|---|---|---|---|---|---|
| 0 已倒转、等待漂浮 | 0 | `trigger` 当刻（t=0） | `stageDelayTicks` = 14 刻 | 施加效果（`visible=false, showIcon=false`），播触发音 | 灰度关 / 倒转开 / 输入锁开 / 自转关 |
| 1 漂浮中 | 1 | t = 14 | `levitationDurationTicks` = 20 刻 | 硬切换效果；同时 `addEffect(LEVITATION, 20, 6)`（amplifier 6 = VII 级） | 灰度关 / 倒转开 / 输入锁开 / 自转关 |
| 2 下坠自转 | 2 | t = 34 | 到落地为止，目标 `zangief_cane.fallTicks` = 40 刻，安全上限 `MAX_FALL_TICKS` = 100 刻 | 硬切换效果；此后**每刻伺服**向下速度 = 剩余高度 ÷ 剩余刻数（§5.2） | 灰度关 / 倒转开 / 输入锁开 / 自转开 |
| （结束） | — | 落地或超时 | — | `removeEffect` + 从 `ACTIVE` 移除 | 全部复位 |

**「硬切换」的定义**（不能省）：相位推进时先 `player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER)`，下一行再 `addEffect(new MobEffectInstance(…, amplifier = 新相位, …))`。原因是原版 `MobEffectInstance#update` 有自己的合并规则（比较 amplifier 与 duration），直接叠加一个「同 id、不同 amplifier」的实例并不保证一定替换。
**待验证（挂 RT-15）**：1.21.1 的 `MobEffectInstance#update` 具体比较顺序；本设计**不依赖**它的行为，用「先移除再加」绕开。风险是硬切换会多一个 `ClientboundRemoveMobEffectPacket` + 一个 `ClientboundUpdateMobEffectPacket`，客户端有 1 tick 的「效果不在」窗口——§5.6 的客户端自检必须在「效果不在」时不立刻复位，而要等 2 tick。

**关键方法**：

| 方法 | 做什么 | 要点 |
|---|---|---|
| `begin(ServerPlayer, long tick)` | 相位 0 落地 | 由 `ZangiefCaneTrap#trigger` 调用 |
| `onPlayerTick(ServerPlayer)` | 读 `ACTIVE`，按相位推进；落地判 `player.onGround()`；超时判 `now - enteredAt >= MAX_FALL_TICKS` | 只在 `ServerLevel` 侧；每 tick 一次，常数开销 |
| `setPhase(ServerPlayer, int phase, int durationTicks)` | 硬切换 + 更新 `ACTIVE` | 唯一改 amplifier 的地方 |
| `finish(ServerPlayer)` | `removeEffect` + 移除记录 | 幂等 |
| `isActive(UUID)` | 给 `WildHunterCombat` 之类判断用（相位期间禁止冲刺吸附） | 只读 |

**读哪些 config 键**：`zangief_cane.stageDelayTicks`、`zangief_cane.levitationLevel`、`zangief_cane.levitationDurationTicks`、`zangief_cane.fallTicks`。
**写哪些注册对象**：无。

**落地时长怎么保证在 1.7–2 秒**（`plan0.0.2.txt` 第 29 行原文「确保其可以在 1.7-2 秒内落地」）：**不用定速，用逐刻伺服**（裁决 A-03）。相位 2 的每一刻先量出玩家脚下的剩余高度 `h` 与剩余刻数 `n = fallTicks - 已过刻数`，再把向下速度设成 `h / n`：

```java
double h = player.getY() - groundYBelow(player);          // 剩余下落高度（格）
int n = Math.max(1, fallTicks - elapsedInPhase2);         // 剩余刻数
player.setDeltaMovement(0.0, -Math.min(h / n, MAX_SERVO_SPEED), 0.0);
```

为什么定速不行：plan 的约束是**时间**（1.7–2 秒），而定速下落地时长由起始高度决定，玩家被漂浮抬多高每一局都不一样，`v` 一旦写死就必然在某些高度上超时或提前落地。伺服用「剩余高度 ÷ 剩余刻数」把时间钉住，高度自动被吸收。

| 量 | 取值 | 说明 |
|---|---|---|
| `fallTicks` | `zangief_cane.fallTicks`，默认 **40 刻 = 2.0 秒**，范围 1–200 | plan 的「1.7–2 秒」取上界 2.0 秒作为默认；要 1.7 秒设 34 |
| 伺服速度上限 `MAX_SERVO_SPEED` | `3.0` 格/刻（**本文补充**，安全阀） | 防止剩余刻数为 1、剩余高度 20 格时给出荒谬的瞬移速度；触顶后按自由落体收尾，日志记一行 `桑吉尔蔗 伺服触顶 h={} n={}` |
| 安全上限 `MAX_FALL_TICKS` | `100` 刻 | 伺服本身会让玩家在 `fallTicks` 内落地，这个上限只兜「落地判定失效」的底（卡在方块缝里） |

实现阶段怎么验：日志行 `桑吉尔蔗 落地耗时 {} 刻（目标 {fallTicks}）`，实测值与 `fallTicks` 的偏差应 ≤ 2 刻；连测 5 次（不同高度：平地、两格高台、四格高台）都要落在同一个时长附近——这正是定速方案做不到的。

### 3.12 `WildHunterCombat`（服务端判定：目标复核 / 伤害 / 击退 / 击杀续时）

**职责**：狂野猎人里**所有需要权威的计算**。客户端的预测（§3.13 的 `WildHunterDashClient`）只是手感，最终以这里为准。

**关键方法与规则**：

| 方法 | 规则 | 依据 |
|---|---|---|
| `selectTarget(ServerPlayer)` | 候选 = 以玩家为球心 `wild_hunter.searchRadius`（5.0 格）内的 `LivingEntity`，且与视线方向夹角 ≤ `wild_hunter.searchAngleDegrees`（30.0°）；排除自己、同队伍玩家（`player.isAlliedTo(other)`）、旁观者 | D-64 |
| 排序 | 夹角最小者优先；夹角相同取距离近者（`Comparator.comparingDouble(angle).thenComparingDouble(distSqr)`） | D-64 |
| `onSwing(ServerPlayer)` | 挂 `PlayerInteractEvent.LeftClickEmpty` 不够（那只覆盖空挥），真正的入口是 `ServerboundSwingPacket` 的服务端处理路径 ⇒ 用 `PlayerEvent`/原版攻击流程的**服务端结算点**复核目标；命中即按玩家自身攻击力结算伤害 | D-65 + BRIEF §7.6 |
| 击退 | 伤害成功后调 `target.knockback(0.4, dx, dz)` 的原版默认强度，不自订数值 | D-75 |
| `onKill(ServerPlayer)` | `LivingDeathEvent` 里判 killer 是持有效果的玩家 ⇒ 剩余时长 + `killExtensionTicks`（160 刻 = 8 秒）、回复 `killHealPoints`（10 点生命）；**没有次数上限** | D-74 |
| 与相位互斥 | `ZangiefPiledriverController.isActive(uuid)` 为真时不吸附 | 本文补充：被打桩机期间输入全锁，再吸附会自相矛盾 |

**「攻击伤害」这一项怎么落地**：D-75 只说击退用原版默认强度，没有给额外伤害数值。本文口径：**吸附本身不产生伤害**，伤害来自玩家自己那次普通攻击（走原版 `Player#attack`），因此不需要 `wild_hunter.attackDamage` 这种键。属性的 `attackDamageBonusPercent`（+40 %）由 §3.5 的 `MobEffect` 提供，是效果本身的加成，与吸附无关。

**关键：服务端独立算一遍，不听客户端的**。BRIEF §7.6 的原文是「客户端在『本地按下攻击键 + 自己持有效果 + 本地算出合法目标』时立刻朝目标方向给速度，服务端在收到 `ServerboundSwingPacket` 时用同一套确定性算法独立算一遍目标并结算伤害与击退」。「同一套」的意思就是：`selectTarget` 的**代码只有一份**，客户端与服务端都调它，只是客户端那份跑在 `ClientLevel` 上（要给 `Level` 泛型或传 `LivingEntity` 列表，不能写死 `ServerLevel`）。

**读哪些 config 键**：`wild_hunter.searchRadius`、`wild_hunter.searchAngleDegrees`、`wild_hunter.killExtensionTicks`、`wild_hunter.killHealPoints`、`wild_hunter.dashTicks`。
**写哪些注册对象**：无。

### 3.13 客户端 6 个类

客户端代码只放在 `client/` 包下，且**只能**从 `FarmstormClient`（`@EventBusSubscriber(modid, value = Dist.CLIENT)`）这条边界进来——专用服务器不加载本类，因此不会因为缺客户端类而崩（源码 `client/FarmstormClient.java` 的类注释）。

| 类 | 职责 | 关键方法 / 挂载点 | 读哪些 config 键 | 写哪些注册对象 |
|---|---|---|---|---|
| `GrayscalePass` | 整屏灰度（含 GUI） | `RegisterShadersEvent` 注册 `ShaderInstance`；`RenderGuiEvent.Post` 跑 pass | 无 | 无 |
| `WildHunterOutline` | 红色剪影 | `RenderLivingEvent.Post` → `getModel().renderToBuffer(...)` | 无 | 无 |
| `InputLockHandler` | 键鼠锁定（只留 ESC） | 5 个事件，见 §4.3 | 无 | 无 |
| `ZangiefCameraController` | 渲染层 roll / pitch / yaw | `ViewportEvent.ComputeCameraAngles` | `zangief_cane.spinDegreesPerTick`（只用于自转累加；**客户端读不到服务端 config，见 §4.4 的降级口径**） | 无 |
| `WildHunterLoopSound` | 循环音，跟随玩家 | 自写 `SoundInstance`，`isLooping() = true` | 无 | 无 |
| `WildHunterDashClient` | 冲刺预测 | 攻击键按下时本地给速度 | 与 `WildHunterCombat` 同名的 5 组键的**默认值镜像**（§4.5） | 无 |

六个类的状态来源都是**自己身上的效果**，不是服务端下发的消息：`GrayscalePass` / `WildHunterOutline` / `WildHunterLoopSound` 读 `wild_hunter` 在不在；`InputLockHandler` / `ZangiefCameraController` 读 `zangief_piledriver` 的 amplifier。这条约束是 §9 能成立的全部原因。

### 3.14 既有文件的改动点（逐文件）

```diff
  只改这些，别的一行都不碰（BRIEF §3.3 + 本节的三处白名单补充）
```

| 文件 | 改动 | 依据 |
|---|---|---|
| `util/CropProfile.java` | +4 常量；`ALL` 改 19 条；`LUCKY_BERRY_POOL` 改三排除 | §3.1 / D-55 / D-56 |
| `behavior/DeployedKind.java` | +1 枚举值 `ZANGIEF_CANE(10)` | §3.2 |
| `registry/FarmstormItems.java` | +5 个 `DeferredItem`；`creativeTabItems()` 里把 `compressed_hay_block` 追加进去（4 个作物走 `CROP_ITEMS` 自动进） | §2.2 |
| `registry/FarmstormBlocks.java` | +2 个方块：`deployed(ZANGIEF_CANE, false, false, false)` 与 `compressed_hay_block` | §3.9 / D-68 |
| `registry/FarmstormEntityTypes.java` | +2 个 `projectile(...)` | §2.2 |
| `registry/FarmstormCreativeTabs.java` | **不改**（内容来自 `FarmstormItems.creativeTabItems()`） | BRIEF §3.2「creative tabs +0」 |
| `Farmstorm.java` | 注册 `FarmstormMobEffects.REGISTER` 与 `FarmstormSounds.REGISTER`；自检文案改 19 / 10 / 6 / 3 / 4；新增 2 行自检 | §0.4 |
| `client/FarmstormClient.java` | 2 个新 `ThrownItemRenderer`；`ALL_ENTITY_TYPES` 从 9 条补到 11 条 | §2.2（`FIX-1` 的渲染器自检） |
| `config/FarmstormConfig.java` | +4 个分组共 22 个键（`molotov` / `wild_apple` / `wild_hunter` / `zangief_cane`）+ 烘焙快照 | §8 |
| `datagen/ModRecipeProvider.java` | +5 条配方：1 条有序合成、1 条无序合成、3 条转换 | §7.2 |
| `datagen/ModItemModelProvider.java` | 4 个 `basicItem` + 1 个 `withExistingParent(compressed_hay_block → block/compressed_hay_block)` | §7.2 |
| `datagen/ModBlockStateProvider.java` | `DEPLOYED_INVISIBLE` 加 `"zangief_cane"`；新增 `cubeAll` 的压缩干草块 | D-68 |
| `datagen/ModLootTableProvider.java` | +2 张方块战利品表（部署方块口径 + 普通方块口径） | §7.2 |
| `datagen/ModLangProvider.java` | +39 条（见 §7.3） | §7.3 |
| `datagen/ModAdvancementProvider.java` | +3 条进度；`AWARD_REQUIRED` 追加 `first_molotov` / `zangief_hit` / `first_wild_hunter` | D-63 |
| `datagen/ModBlockTagsProvider.java` | `deployed_crops` 追加 `zangief_cane`；`mineable/pickaxe` 追加 `compressed_hay_block`；新建 `needs_diamond_tool` | D-57 |
| `datagen/ModItemTagsProvider.java` | `modified_crops` 追加 4 条（三个新作物 + 压缩膨胀麦）；新建 `converter_fuel`（19 条档案对应物品 + 原版农作物） | D-52 / D-55 / D-56 |
| `util/FarmstormTags.java` | +1 个 `TagKey<Item> CONVERTER_FUEL` | D-52 |
| `gradle.properties` | `mod_version=0.0.2` | §1.1 |

**编译期强制的三处（必须改，不改编译不过）**

BRIEF §3.3 的可改文件清单已把下面三个文件纳入（`DeployedBehaviors.java` / `FarmstormBlockEntities.java` / `PuffedWheatProjectile.java`），因此上表之外不再有「白名单外」的文件。这三处对 `DeployedKind` / `PuffedWheatProjectile` 的改动是**编译期强制**的，绕不过去：

| 文件 | 为什么必须改 | 最小改动 | 风险 |
|---|---|---|---|
| `behavior/DeployedBehaviors.java` | `of(DeployedKind)` 是对枚举的**穷尽 `switch` 表达式**，无 `default`；加一个枚举值 ⇒ 编译报 "the switch expression does not cover all possible input values" | +1 个静态单例字段 + 1 个 `case` | 无 |
| `registry/FarmstormBlockEntities.java` | 同上：`deployedOf(DeployedKind)` 也是穷尽 `switch` | +1 个 `case ZANGIEF_CANE -> FANG_TRAP_CACTUS.get();` + 把 `FarmstormBlocks.ZANGIEF_CANE.get()` 加进 `FANG_TRAP_CACTUS` 那个 `BlockEntityType.Builder.of(…)` 的方块列表 | `block_entities` 数量保持 6（一个 type 收两个方块），**待验证（挂 RT-04）**：`BlockEntityType.Builder#of` 是否接受 `Block...` 可变参数 |
| `entity/thrown/PuffedWheatProjectile.java` | 见 §3.8：把写入的方块抽出成扩展点，D-77 的「完全沿用」才是同一份代码路径而不是两份复制 | +1 个 `protected BlockState fillBlock()` + 1 行改用（`fillHay` 由 `private static` 转实例方法） | 无 |

三处改动都不产生新的注册对象、不产生新的资源文件、不新增 config 键：§2 的 77、§7 的 36、§8 的 22 都不变。

---

## 4. 三个客户端子系统的技术方案

本节把 BRIEF §7.1–§7.3 展开成可实现方案。**首选方案一个字都不换**（BRIEF §7 的前置条件 ①），风险编号也一个都不重排（前置条件 ②）。三条待验证按 §0.5 的规则保留标记并补上「怎么验」。

三者的共同前提（BRIEF 未写，由 §0.6 推出）：**状态来源永远是自己身上的效果**。客户端不询问服务端、不缓存服务端消息、不新增包。

### 4.1 灰度（D-61，整屏含 GUI）→ RT-10

**触发条件**：本地玩家身上存在 `farmstorm:wild_hunter`（§0.6）。效果的「在 / 不在」由原版 `ClientboundUpdateMobEffectPacket` 送到的 `MobEffectInstance` 决定，客户端每 tick 读一次。

**首选方案（不许换）**：

1. `RegisterShadersEvent`（mod 总线）里注册一个自定义 `ShaderInstance`，程序落在 `assets/farmstorm/shaders/core/grayscale.json + grayscale.vsh + grayscale.fsh`。
2. `RenderGuiEvent.Post` 里对 `Minecraft.getInstance().getMainRenderTarget()` 跑一次全屏 pass。

选 `RenderGuiEvent.Post` 的理由（BRIEF §7.1 原文）：它在 GUI 之后、上屏之前，一次 pass 同时覆盖世界与 GUI，正好满足 D-61 的「整屏灰度，不是只褪世界」。

```java
// client/render/GrayscalePass.java（骨架，签名以 1.21.1 为准）
private static ShaderInstance shader;                       // RegisterShadersEvent 里赋值

@SubscribeEvent
public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
    event.registerShader(new ShaderInstance(event.getResourceProvider(),
            Farmstorm.id("grayscale"), DefaultVertexFormat.POSITION_TEX), s -> shader = s);
}

@SubscribeEvent
public static void onRenderGuiPost(RenderGuiEvent.Post event) {
    if (!WildHunterClientState.active() || shader == null) return;   // 不在效果里：0 开销，连 target 都不碰
    Minecraft mc = Minecraft.getInstance();
    RenderTarget main = mc.getMainRenderTarget();
    main.bindWrite(true);                                            // 切到主 target
    RenderSystem.setShader(() -> shader);
    RenderSystem.setShaderTexture(0, main.getColorTextureId());      // 采样自己上一帧的结果
    blitFullScreenQuad();                                            // 画一张铺满屏幕的四边形
    mc.getMainRenderTarget().bindWrite(true);                        // 复位
}
```

**为什么 pass 要在 `active()` 为假时第一行就 return**：灰度是「异常状态」而不是常驻后处理。常态下它必须是零成本，否则每个玩家的每一帧都白跑一次全屏 pass，等于给全服降帧。`active()` 是客户端快照（见下），不是现查效果。

**`WildHunterClientState` 是什么**：`client/WildHunterClientState.java`，客户端的状态缓存类，职责只有一件——把 `wild_hunter` 的几个「跨子系统共用、且服务端 config 读不到」的量算一次，供 §4.1 / §4.2 / §4.6 三个钩子直接读：

| 方法 | 返回 | 谁在用 |
|---|---|---|
| `active()` | `boolean`：本地玩家此刻是否带着 `wild_hunter` | §4.1 灰度、§4.2 剪影的第一行开关；在 `ClientTickEvent.Post` 里算一次，渲染线程只读快照 |
| `highlightRadius()` | `double`：红色剪影的最大距离（格），默认 `64.0` | §4.2 的距离过滤；值来自 §8.4 的镜像常量 `DEFAULT_HIGHLIGHT_RADIUS` |
| `loopRange()` | `double`：循环音可听距离（格），默认 `16.0` | §4.6 的衰减距离；值来自 §8.4 的镜像常量 `DEFAULT_LOOP_SOUND_RANGE` |

为什么要镜像而不是读 config：`FarmstormConfig` 是 `ModConfig.Type.SERVER`，单人游戏里客户端能读到，专用服务器上读不到。所以 `wild_hunter.highlightRadius`（§8.1 #15）与 `wild_hunter.loopSoundRange`（§8.1 #16）在客户端一律由本类返回**镜像常量**，与 §4.4 的降级口径同一套规则：改 config 默认值时必须同步改镜像，否则客户端表现会滞后（登记为已知偏差，不修——修它要新包，违反 D-50）。

**资源与两种挂载形态**：

| 文件 | 形态 | 谁用它 | 说明 |
|---|---|---|---|
| `shaders/core/grayscale.json` | `ShaderInstance` 的程序声明 | **首选**（本节） | BRIEF §7.1 点名的那个 json |
| `shaders/core/grayscale.vsh` / `grayscale.fsh` | GLSL 源码 | 两种形态共用 | BRIEF §3.2 的「2 个 GLSL」就是这两个文件 |
| `post_effect/grayscale.json` | `PostChain` 声明 | **RT-10 退化分支** | BRIEF §3.2 登记的 +1；用同一对 GLSL，只换挂载方式 |

**待验证（BRIEF §7.1 原文）**：`RenderGuiEvent.Post` 期间切换 render target 是否与其它模组的 GUI 后处理打架。

- **怎么验**：装一个做 GUI 后处理的模组（或临时在 `RenderGuiEvent.Post` 里再跑一次自己的 pass）→ 进入效果 → 观察是否出现「画面全黑 / 只灰了半个屏幕 / 其它模组的 HUD 消失」。同时看日志里 `GL_INVALID_OPERATION` 一类的 GL 报错（`-Dmixin.debug` 无关，直接看 `latest.log` 的 `[Render thread/ERROR]` 行）。
- **判定**：出现上述任一现象即视为「打架」，走下面的退化分支。
- **退化分支（BRIEF §7.1 给出）**：`RenderLevelStageEvent` 的 AFTER 阶段（世界段跑完、GUI 之前，具体常量名 **待验证，挂 RT-10**）跑一次世界灰度 + `RenderGuiEvent.Post` 只对 GUI 区跑一次。两次 pass 的 GLSL 完全相同，只换挂载点与视口。若世界段这一步也不成立，就只灰世界，并在 Release 说明里把「GUI 不灰」登记为降级。
- **RT 归属**：本条待验证挂 **RT-10**（整屏灰度后处理的挂载点不成立）。
- **另有一条 API 待验证（挂 RT-01）**：`RegisterShadersEvent#registerShader(ShaderInstance, Consumer<ShaderInstance>)` 的签名、以及 `ShaderInstance(ResourceManager, ResourceLocation, VertexFormat)` 这个三参构造在 1.21.1 上是否仍然成立（1.21 改过 shader 装载路径，`shaders/core/*.json` 与 `post_effect/*.json` 两套并存）。若三参构造不成立，改用 `Shaders` 的工具方法或改走 `PostChain`（§6 的 §7.2 #15 已经把那份资源列进清单，退化时不需要补文件）。

**禁止（BRIEF §7.1 原文）**：用雾色或贴图替换冒充灰度。D-61 要的是真灰度——「雾色」只能褪远处，「贴图替换」只能改方块表面，两者都改不了 GUI，也改不了实体与粒子。

### 4.2 红色剪影（D-62，只画可见的、≤64 格）→ RT-11

**触发条件**：同 §4.1（`wild_hunter` 存在）。

**首选方案（不许换）**：

1. 订阅 `RenderLivingEvent.Post`。
2. 取 `LivingEntityRenderer#getModel()`。
3. 用 `Model#renderToBuffer` 以 `RenderType.entityTranslucentEmissive(whiteTexture)` 再画一遍。
4. 配 `RenderSystem.setShaderColor(1.0F, 0.15F, 0.15F, 0.45F)`。
5. 白色 1×1 贴图放 `assets/farmstorm/textures/misc/white.png`。

```java
@SubscribeEvent
public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
    if (!WildHunterClientState.active()) return;
    LivingEntity entity = event.getEntity();
    LocalPlayer self = Minecraft.getInstance().player;
    if (entity == self || entity.isSpectator() || self.isAlliedTo(entity)) return;
    if (self.distanceToSqr(entity) > sq(WildHunterClientState.highlightRadius())) return;  // D-62：highlightRadius，默认 64.0 格
    if (!self.hasLineOfSight(entity)) return;                        // D-62：无方块遮挡
    var model = event.getRenderer().getModel();
    var buffer = event.getMultiBufferSource().getBuffer(
            RenderType.entityTranslucentEmissive(WHITE_TEXTURE));
    RenderSystem.setShaderColor(1.0F, 0.15F, 0.15F, 0.45F);
    model.renderToBuffer(event.getPoseStack(), buffer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, -1);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);            // 必须复位，否则后续实体被染红
}
```

**过滤规则逐条**（D-62 + D-64 的排除项）：

| 过滤 | 判据 | 依据 |
|---|---|---|
| 排除自己 | `entity == Minecraft.getInstance().player` | D-62 |
| 排除同队伍玩家 | `self.isAlliedTo(entity)`（客户端只有计分板队伍与好友信息，够用） | D-62 |
| 排除旁观者 | `entity.isSpectator()` | D-64 的同一豁免 |
| 距离 ≤ `wild_hunter.highlightRadius`（默认 64.0 格，范围 0.0–256.0） | `distanceToSqr <= r * r`（比较平方，不开方）；`r` 取 §4.4 的客户端镜像常量 | D-62 |
| 视线可见 | `self.hasLineOfSight(entity)`；**不穿墙** | D-62 |

**禁止（BRIEF §7.2 原文）**：

- 不许在 `RenderLivingEvent` 里调 `EntityRenderDispatcher#render` 重绘整只实体——会递归触发自身事件，直接栈溢出。
- 不许用原版发光效果（`Glowing`）替代：那样是**白色**描边且**全服可见**，与 D-62 的「红色、只给自己看、只给可见的」三条全不符。

**待验证（挂 RT-11）**：

| 待验证项 | 怎么验 | 判定 |
|---|---|---|
| `hasLineOfSight` 在客户端实体上的可用性与开销 | 在墙后 / 墙前各站一次，看是否只对墙前的生物画剪影；同时 `/debug profile` 看 tick 与帧时间 | 墙后有剪影 = 不可用，需退回「按距离 + 视线粗略采样」 |
| `setShaderColor` 的 alpha（0.45）是否对 `entityTranslucentEmissive` 生效 | 把 alpha 调 0.0 与 1.0 各看一次 | 两个极端都不变化 = alpha 不生效，改用 `RenderType` 自带透明度或顶点色 |
| 大量实体时的帧时间 | 30 只生物同屏 × 效果开启 | 帧时间增量 > 3 ms 视为不可接受，需要加「同帧最多 32 只」的熔断 |

**性能护栏（本文补充）**：每帧最多画 **32** 只（按距离近的优先），超出的不画。理由：`renderToBuffer` 是整模型重绘，不是便宜的描边 pass；默认 64 格半径里的一次大乱斗可能有上百只生物。

### 4.3 输入锁（D-60，只留 ESC）→ RT-13

**触发条件**：本地玩家身上存在 `farmstorm:zangief_piledriver`，**且 amplifier 是 0 / 1 / 2 的任意一个**（三个相位都在锁定期内，D-60 的范围是「从触发到落地前」）。

**五个事件各管一段，缺一个就会漏**（BRIEF §7.3 原表，逐字保留）：

| 要锁的 | 用哪个事件 | 具体做法 |
|---|---|---|
| 移动 / 跳跃 / 潜行 | `MovementInputUpdateEvent` | 把 `forwardImpulse` / `leftImpulse` / `jumping` / `shiftKeyDown` 全部清零 |
| 攻击 / 使用 | `InputEvent.InteractionKeyMappingTriggered` | `event.setCanceled(true)` |
| 快捷栏 1–9 | `InputEvent.Key` | 放行 `GLFW_KEY_ESCAPE`，其余取消 |
| 滚轮切栏 | `InputEvent.MouseScrollingEvent` | 取消 |
| 背包 / 聊天 | `ScreenEvent.Opening` | 只放行 ESC 路径（`PauseScreen`），其余 `setNewScreen(null)` 或取消 |

```java
// client/input/InputLockHandler.java（骨架）
private static boolean locked() {
    return Minecraft.getInstance().player != null
            && Minecraft.getInstance().player.hasEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
}

@SubscribeEvent
public static void onMovementInput(MovementInputUpdateEvent event) {
    if (!locked()) return;
    Input input = event.getInput();
    input.forwardImpulse = 0.0F;
    input.leftImpulse = 0.0F;
    input.jumping = false;
    input.shiftKeyDown = false;
}
```

**为什么锁定判据是「效果在不在」而不是「amplifier 等于几」**：三个相位都要锁，用「在不在」一个条件就够，少一个可能写错的数字。相位号只在 §4.1 / §4.4 需要区分的地方才读。

**只留 ESC 的完整含义**（D-60）：ESC 必须可用，因为「锁死 ESC 会让玩家以为游戏卡死」。具体到代码：`InputEvent.Key` 里 `keyCode == GLFW.GLFW_KEY_ESCAPE` 直接 return 不取消；`ScreenEvent.Opening` 里 `event.getNewScreen() instanceof PauseScreen` 放行。

**待验证（BRIEF §7.3 原文）**：`InputEvent.Key` 与 `KeyMapping` 的先后顺序决定了 1–9 是否真能拦住。

- **怎么验**：进效果后**逐键手测**并记录：W / A / S / D / 空格 / Shift / 左键 / 右键 / 1–9 九个数字键 / 滚轮上 / 滚轮下 / E / T / 斜杠 / T / ESC。每按一个记一行「键 → 是否被拦」。
- **判定**：任意一键仍生效（人物动了、物品用了、快捷栏换了、GUI 开了）即视为该事件挂错层级，需要把这一路改挂到更靠前的钩子（例如 `InputEvent.Key` 之前再加 `ScreenEvent.KeyPressed`）。
- **RT 归属**：挂 **RT-13**（输入拦截有遗漏，某个键仍能穿透）。

**不在锁定范围的按键（明确写出来，免得被当成漏测）**：F1 / F2 / F3 功能键、F5 视角、F11 全屏、Esc 菜单里的操作。D-60 的清单只有 5 组，这 5 组之外不管。

**解除条件**：`zangief_piledriver` 消失的**同一 tick** 就解除（`locked()` 是现查，没有缓存）。不许用「等下一次 tick 再解锁」的写法，否则落地后会多锁 1 刻，手感上是「卡了一下」。

### 4.4 相机（D-59 / D-70，BRIEF §7.4 未分配风险编号）

**触发条件**：`zangief_piledriver` 存在（三个相位都生效）。

**实现**：`ViewportEvent.ComputeCameraAngles`：

```java
@SubscribeEvent
public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
    var player = Minecraft.getInstance().player;
    if (player == null || !player.hasEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER)) return;
    event.setRoll(180.0F);                              // D-59：绕视线轴滚 180°
    event.setPitch(-event.getPitch());                  // D-59：pitch 取反，两者同时生效
    if (ZangiefClientState.spinning()) {                // 只有相位 2 自转（D-70）
        event.setYaw(event.getYaw() + ZangiefClientState.accumulatedDegrees());
    }
}
```

**铁律（BRIEF §7.4 原文）**：只改**渲染层**角度，**不写回** `player.getYRot()` / `player.getXRot()`。玩家真实朝向不变，服务端权威不破——如果写回，客户端就会把「被倒转的朝向」发给服务端，玩家的实际面朝方向被篡改，等于开了一个可以穿墙打人的洞。

**自转的累加方式**：客户端按 tick 累加，每刻 `accumulated += spinDegreesPerTick`（默认 72.0 度 = 1440 度/秒），**不依赖服务端逐刻同步**（BRIEF §7.4 原文）。相位 2 开始时归零，相位 2 结束时归零。

**客户端读不到服务端 config 的降级口径（本文补充）**：`FarmstormConfig` 是 `ModConfig.Type.SERVER`，单人游戏里客户端能读，专用服务器上读不到。因此 `ZangiefCameraController` 与 §3.13 的 `WildHunterDashClient` 一律使用**与 config 默认值相同的编译期常量**（`DEFAULT_SPIN_DEGREES_PER_TICK = 72.0`、`DEFAULT_SEARCH_RADIUS = 5.0`、`DEFAULT_HIGHLIGHT_RADIUS = 64.0`、`DEFAULT_LOOP_SOUND_RANGE = 16.0` 等），并在类注释里点名「这是 config 默认值的镜像，改 config 默认值时必须同步改这里」。后果与处理：

| 场景 | 现象 | 处理 |
|---|---|---|
| 服务端用默认值 | 完全一致 | — |
| 服务端改了 `spinDegreesPerTick` | 客户端自转速度与配置不符（纯视觉，不影响权威） | 登记为已知偏差，不修（修它要新包，违反 D-50） |
| 服务端改了 `searchRadius` / `searchAngleDegrees` | 客户端预测可能不触发 → 「这次没吸附」，伤害与击退仍由服务端按真实配置结算 | 同上 |

### 4.5 狂野猎人冲刺（D-65）→ RT-12

**首选方案（不许换）**：**客户端预测位移 + 服务端判定**。

MC 里玩家位置本来就是客户端权威（服务端只做速度校验）。完整流程：

| # | 在哪 | 发生什么 | 依据 |
|---|---|---|---|
| 1 | 客户端 | 本地按下攻击键 + 自己身上有 `wild_hunter` + 本地 `selectTarget` 算出合法目标 | BRIEF §7.6 |
| 2 | 客户端 | 立刻朝目标方向给速度：`setDeltaMovement(dir.normalize().scale(dist / dashTicks))`，`dashTicks = 3` 刻跨到位 | D-65 |
| 3 | 客户端 | 照常发 `ServerboundSwingPacket`（原版攻击流程本来就会发，不需要额外包） | §9 |
| 4 | 服务端 | 收到 swing 时用**同一份** `selectTarget` 独立算一遍目标；不合法就什么都不做 | BRIEF §7.6 |
| 5 | 服务端 | 合法则结算伤害（玩家自身攻击力）与击退（D-75 的原版默认强度） | D-75 |
| 6 | 服务端 | **不下发位移**（不下发 `ClientboundSetEntityMotionPacket` 去纠正玩家） | 避免「被拉回」 |

**为什么第 6 步必须是「不下发」**：BRIEF §7.6 的待验证说得很清楚——`ServerPlayer#setDeltaMovement` 并不会让客户端玩家移动（玩家移动是客户端模拟的），所以「服务端给速度」这句在实现上必须落到客户端驱动。若服务端硬发速度包，客户端会在下一 tick 被自己的位置校验覆盖，表现为「冲一半被拽回来」。**红线**：不许为了这个新增网络包（D-50）。

**待验证（BRIEF §7.6 原文）**：服务端给速度在玩家身上不生效。

- **怎么验**：在服务端调 `player.setDeltaMovement(0, 0, -2)` 后打一条日志，观察客户端是否位移（预期：不位移或仅轻微抖动）；同时看服务端是否打印 `moved too quickly!`。
- **RT 归属**：挂 **RT-12**（冲刺位移的驱动方判错，玩家不动或被拉回）。

**两条已知副作用与处理（本文补充）**：

| 副作用 | 原因 | 处理 |
|---|---|---|
| 客户端位移触发服务端的「移动过快」校验 | 3 刻跨 5 格 = 1.67 格/刻，超过原版 0.0625 格/刻的常规预期 | 观察 `latest.log` 是否出现 `moved too quickly!` / `moved wrongly!`；如出现，把 `dashTicks` 调到 4–5 刻，或改走备选方案 |
| 冲刺撞墙 | 客户端照常模拟碰撞（D-65：撞墙即停、不补位、不穿透） | 无需处理——这正是 D-65 想要的 |

**备选方案（BRIEF §7.6 给出，切换必须留决策记录）**：服务端逐刻小步 `teleportTo`（每刻 ≤ 1 格、带碰撞检测）。切换条件：首选方案实测手感不可接受（例如「冲过去但攻击没跟上」「位移与伤害不同步」）。切换时必须在本节追加一段「决策记录：YYYY-MM-DD，从 A 切到 B，理由是 …」，并把 RT-12 的首要缓释改成「逐刻 teleport」。

### 4.6 循环音（D-76 / D-53 / D-66）→ RT-14

**触发条件**：本地玩家身上存在 `wild_hunter`。

**实现**：客户端一个自写的 `SoundInstance` 子类，`isLooping() = true`，每 tick 把位置更新到玩家身上，可听距离取 `WildHunterClientState.loopRange()`（= `wild_hunter.loopSoundRange`，默认 16.0 格，范围 0.0–64.0），声音分类 `SoundSource.PLAYERS`。

```java
// client/audio/WildHunterLoopSound.java
public final class WildHunterLoopSound extends SoundInstance {
    @Override public boolean isLooping() { return true; }
    @Override public boolean canPlaySound() { return true; }
    @Override public SoundSource getSource() { return SoundSource.PLAYERS; }   // 不占 MUSIC，不打断背景音乐

    @Override
    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { stop(); return; }
        this.x = player.getX(); this.y = player.getY(); this.z = player.getZ();   // 音源跟随玩家
        if (!player.hasEffect(FarmstormMobEffects.WILD_HUNTER)) stop();          // 效果没了立刻停
    }
}
```

**为什么不用服务端每刻重发播放指令**（D-76 原文）：服务端每刻发 `ClientboundSoundPacket` 是 20 包/秒/玩家，而循环音的生命周期恰好等于效果的生命周期——效果的存在本身就已经告诉客户端「该播」。客户端自己起一个 `SoundInstance` 是 0 包。

**待验证（BRIEF §7.7 原文）**：16 格衰减在 1.21.1 上到底该压 `SoundInstance#getVolume` 还是它的可听距离钩子。

- **怎么验**：站到 8 / 16 / 17 / 32 格处各听一次，记录「能听到 / 听不到 / 音量是否随距离变小」。同时把 `getVolume()` 从 1.0 调到 4.0 看远处是否变响（体积放大会不会被当作「传得更远」）。
- **判定**：17 格仍清楚听到 = 衰减半径没生效，需要改压 `sounds.json` 里该事件的 `attenuation_distance`（若该字段存在，**待验证，挂 RT-14**）或改用 `getVolume()` 的手工距离衰减。
- **RT 归属**：挂 **RT-14**（音频产不出来 / 音频参数不对）。

**sounds.json 的两个事件**见 §6.3；两个 `.ogg` 的转换参数见 §6.2。

---

## 5. 桑吉尔蔗的相位同步方案

本节把 BRIEF §7.5 展开成可实现的时序。核心约束是 D-50：**不许新增 `CustomPacketPayload`**，因此「服务端告诉客户端现在是哪个相位」这件事只能靠 `farmstorm:zangief_piledriver` 的 amplifier。

### 5.1 状态定义

| 状态量 | 载体 | 取值范围 | 谁写 | 谁读 |
|---|---|---|---|---|
| 相位号 | `ZangiefPiledriverEffect` 的 amplifier | 0 / 1 / 2 | 服务端 `ZangiefPiledriverController#setPhase` | 客户端 `ZangiefClientState`（每 tick 一次） |
| 相位剩余时长 | 同一个 `MobEffectInstance` 的 duration | 14 / 20 / ≤100 刻 | 同上 | 客户端只用于日志与调试，不用于逻辑 |
| 「在不在」 | 效果实例是否存在 | true / false | 服务端的 `addEffect` / `removeEffect` | 全部 6 个客户端类（§4.3 的 `locked()` 就是它） |

三个量合起来就是完整状态，没有第四个量需要同步——这是 D-50 那句「效果存在与否 + amplifier + 剩余时长即完整状态」的字面落地。

### 5.2 服务端时序（唯一真相）

| 时刻 | 事件 | 服务端动作 | 发出的原版包 |
|---|---|---|---|
| t = 0 | 陷阱触发 | `addEffect(zangief_piledriver, 14 刻, amplifier 0, ambient=false, visible=false, showIcon=false)`；`playSound(zangief_piledriver)`；`be.consume()` | `ClientboundUpdateMobEffectPacket` ×1、`ClientboundSoundPacket` ×1、`ClientboundBlockUpdatePacket` ×1、`ClientboundLevelEventPacket` ×1 |
| t = 14 | 相位 0 → 1 | `removeEffect` 后立刻 `addEffect(…, 20 刻, amplifier 1)`；同时 `addEffect(LEVITATION, 20 刻, amplifier 6)` | `ClientboundRemoveMobEffectPacket` ×1、`ClientboundUpdateMobEffectPacket` ×2 |
| t = 34 | 相位 1 → 2 | `removeEffect` 后 `addEffect(…, 100 刻, amplifier 2)`；此后每 tick 伺服：`setDeltaMovement(0, -remainingHeight / remainingTicks, 0)` | 同上 ×1 + 每 tick 一个 `ClientboundSetEntityMotionPacket` |
| t = 落地 | 结束 | `removeEffect`；从 `ACTIVE` 移除 | `ClientboundRemoveMobEffectPacket` ×1 |
| t = 34+100 | 超时兜底 | 同「结束」，并打一行 `warn`：`桑吉尔蔗 相位 2 超时（玩家 {} 未落地）` | 同上 |

**t = 14 为什么不复用同一实例直接改 amplifier**：见 §3.11 的「硬切换」——`MobEffectInstance#update` 的合并规则不保证替换，且它不会重置 duration。用「移除 + 新增」把三个字段同时钉死（amplifier / duration / 可见性）。

**1 tick 的空窗**：硬切换会在同一 tick 内发出「移除」与「新增」两个包，客户端在处理这两包的间隙**可能**有一帧看不到效果。这就是 §5.6 的自检必须容忍 2 tick 的原因。**待验证（挂 RT-15）**：两个包在同一 tick 到达时，客户端 `getEffect` 是否会在两次读取之间返回 `null`；验法是打日志 `桑吉尔蔗 相位切换 t={} 客户端读到 amplifier={}`，看是否出现 `null`。

### 5.3 apply / remove 的完整调用序列

```java
// ZangiefPiledriverController#setPhase —— 唯一改 amplifier 的地方
private static void setPhase(ServerPlayer player, int phase, int durationTicks) {
    player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);                 // 硬切换第一步
    player.addEffect(new MobEffectInstance(FarmstormMobEffects.ZANGIEF_PILEDRIVER,
            durationTicks, phase, false /*ambient*/, false /*visible*/, false /*showIcon*/));
    ACTIVE.put(player.getUUID(), new PhaseState(phase, player.level().getGameTime()));
}
```

| 关注点 | 规则 |
|---|---|
| `visible` / `showIcon` | 两个都传 `false`。这是「隐藏」的第一道保险；类上覆写 `shouldRenderHUD()` 是第二道（§3.6） |
| `ambient` | `false`。粒子与图标都不会出现，与隐藏一致 |
| 玩家死亡 / 退出 | `PlayerLoggedOutEvent` 与 `LivingDeathEvent` 里清 `ACTIVE`（效果本身随死亡清除，但 `ACTIVE` 是内存表，不清会残留到下一世） |
| 服务器重启 | `ACTIVE` 是内存表，天然清空（D-18 的先例：创造模式计时器也只存内存） |
| 重复触发 | 已被打桩机的玩家再踩一个陷阱：`begin` 里先判 `ACTIVE.containsKey(uuid)`，为真则**直接返回**（不叠加、不重置） |

### 5.4 客户端读取时机

**唯一读取点**是 `ClientTickEvent.Post`（客户端，每 tick 一次），读完写进一份快照，渲染线程只读快照。

```java
// client/ZangiefClientState.java
private static int phase = -1;             // -1 = 无效果
private static int missingTicks;           // 连续读不到效果的 tick 数（RT-15 的容忍窗口）
private static float accumulated;

@SubscribeEvent
public static void onClientTick(ClientTickEvent.Post event) {
    LocalPlayer player = Minecraft.getInstance().player;
    MobEffectInstance inst = player == null ? null
            : player.getEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
    if (inst == null) {
        if (++missingTicks >= 2) { phase = -1; accumulated = 0.0F; }   // 容忍硬切换的 1 tick 空窗
        return;
    }
    missingTicks = 0;
    int next = inst.getAmplifier();
    if (next != phase) { phase = next; accumulated = 0.0F; }           // 换相位就重置自转
    if (phase == 2) accumulated += (float) DEFAULT_SPIN_DEGREES_PER_TICK;  // 只有相位 2 自转（D-70）
}
```

| 为什么在 tick 里读、不在渲染里读 | 渲染每帧可能跑多次（高刷屏），在渲染线程里读 `MobEffectInstance` 会把「逻辑状态」与「帧率」耦合；快照写法让 §4.1 / §4.2 / §4.4 三个渲染钩子都只读两个 int 与一个 float |
|---|---|
| 为什么容忍 2 tick | 硬切换那一 tick 客户端可能读到 `null`（§5.2 的待验证）；不忍的话画面会闪一帧「恢复正常」 |
| 有画面滞后吗 | 最多 1 tick（50 ms）。对「被打桩机」这种状态来说不可感知；换取的是零包与零渲染线程锁 |

### 5.5 三个相位 × 五个客户端子系统的矩阵

| 子系统 | 相位 0（0–14 刻） | 相位 1（14–34 刻） | 相位 2（34 刻–落地） | 效果消失后 |
|---|---|---|---|---|
| 相机 roll = 180°（D-59） | 开 | 开 | 开 | 关 |
| 相机 pitch 取反（D-59） | 开 | 开 | 开 | 关 |
| 相机 yaw 自转（D-70） | 关 | 关 | **开**（每刻 +72°，逆时针；72 × 20 = 1440 度/秒） | 关 |
| 输入锁（D-60） | 开 | 开 | 开 | 关 |
| 循环音（D-76） | 不适用（那是 `wild_hunter`） | — | — | — |
| 灰度（D-61） | 不适用（那是 `wild_hunter`） | — | — | — |
| 红色剪影（D-62） | 不适用（那是 `wild_hunter`） | — | — | — |

**表内的计数**：5 个子系统 = 相机（roll / pitch / yaw 三行合起来算一个）、输入锁、循环音、灰度、红色剪影；表共 **7 行**。「不适用」的三行（循环音、灰度、剪影）与 `zangief_piledriver` 的三个相位无关——它们的开关是 `wild_hunter` 在不在（§0.6 的绑定表），分别由 §4.6、§4.1、§4.2 负责。

**自转只在相位 2**：`plan0.0.2.txt` 第 29 行的语序是「漂浮效果结束后…并在其落地之前持续…转动水平视角」，即自转区间 = 从漂浮结束到落地。D-70 的「落地前」按这个语序取同一段。相位 0 / 1 只倒转不自转。

**倒转从相位 0 就开始**：同一条原文的第一句是「将其视角上下倒转」，紧接着才是「14 个游戏刻后给予漂浮」，所以倒转（roll + pitch）覆盖全部三个相位。

### 5.6 自检与复位（RT-15）

RT-15 的风险是「隐藏 MobEffect 被其它模组的效果清理逻辑干扰」——例如某个「清除全部负面效果」的模组把 `zangief_piledriver` 也清了，而客户端还停在 `phase = 2`，画面就永远倒着、键永远锁着。

**首要缓释：客户端每 tick 自检「阶段效果还在不在」，不在就自己复位**（BRIEF §7.8 原文）。落地成三条断言：

| 断言 | 位置 | 不成立时 |
|---|---|---|
| 相位号必须在 `{-1, 0, 1, 2}` 内 | `ZangiefClientState#onClientTick` | 打 `warn` 并强制置 `-1`（任何其它 amplifier 都当无效） |
| `phase >= 0` 时效果必须存在 | 同上 | 连续 2 tick 读不到 ⇒ 复位（§5.4 的 `missingTicks`） |
| `phase == -1` 时三个渲染钩子必须什么都不做 | `GrayscalePass` / `ZangiefCameraController` / `InputLockHandler` | 各钩子第一行判 `phase` / `hasEffect`，不复位就 return |

**服务端一侧的对称自检**：`ZangiefPiledriverController#onPlayerTick` 里，若 `ACTIVE` 有记录但 `player.getEffect(...)` 为空（被别的模组清了），就直接 `finish(player)` 并打 `warn`，不要试图重新施加——效果被外力清掉说明有第三方干预，重新施加会变成两边打架。

**怎么验（实现阶段）**：`/effect give @s farmstorm:zangief_piledriver 10 2 false`（直接给一个 amplifier 2 的效果）→ 看到画面倒转 + 输入锁 + 自转；`/effect clear @s` → 画面与输入必须在 **100 ms 内**恢复正常，日志里出现 `桑吉尔蔗 状态复位（效果被外部清除）`。

### 5.7 为什么这一整套不需要新包

因为需要同步的只有三个标量，而这三个标量恰好是 `MobEffectInstance` 的全部可变字段（id / amplifier / duration），原版 `ClientboundUpdateMobEffectPacket` 逐字发它们。详证见 §9.2 的第 1–3 行。特别地：

- **相位号**不需要包：它是 amplifier。
- **相位切换瞬间**不需要包：`removeEffect` + `addEffect` 各自触发一个原版包。
- **「被打桩机」这个布尔**不需要包：它是「效果实例在不在」。
- **向下的速度**不需要包：`setDeltaMovement` 在 `ServerPlayer` 上走 `ClientboundSetEntityMotionPacket`（服务端广播给所有追踪者，含本人）。

---

## 6. 音频与贴图管线

三个源文件都要先转换再进 `src/main/resources`（D-53）。Minecraft 的声音引擎只解 **Ogg Vorbis**，效果图标必须是 **PNG**——原扩展名直接拷进去等于打包一个永远不会被播放/显示的文件。

### 6.1 三个源文件（已在本机核实存在）

| # | 源路径（绝对） | 大小/形态 | 用途 | 目标路径 |
|---|---|---|---|---|
| 1 | `C:\Users\DASHpig\Downloads\Animals.mp3` | mp3 音频 | 狂野猎人循环音 | `assets/farmstorm/sounds/wild_hunter_loop.ogg` |
| 2 | `D:\dsh\default\sf6_audio\out\BV1qu411W7ha_846-850.mp3` | mp3 音频（SF6 片段） | 桑吉尔蔗触发音 | `assets/farmstorm/sounds/zangief_piledriver.ogg` |
| 3 | `C:\Users\DASHpig\Desktop\e91bb6997d8e24cda1f596267c41da97500924726.jpg` | 桌面 JPG | 狂野猎人效果图标 | `assets/farmstorm/textures/mob_effect/wild_hunter.png` |

路径来源：`plan0.0.2.txt` 第 29 / 39 / 40 行；三个文件的 `Test-Path` 在本文写作时全部为 `True`。**它们在仓库外**，因此转换脚本必须**先检查存在性再动手**，缺文件时同样报错退出（与缺 ffmpeg 同一口径）。

出处与非商业自用声明按 D-66 写进 README 与 Release 说明，不进 jar 内任何文件。

### 6.2 转换参数

| 目标 | ffmpeg 参数 | 判据 |
|---|---|---|
| 音频 → Ogg Vorbis | `-ac 1 -ar 44100 -c:a libvorbis -b:a 64k` | **mono**（1 声道）、**64 kbps**（D-53） |
| 图标 → PNG | `-vf scale=18:18:flags=lanczos -pix_fmt rgba` | **18×18**，无透明通道要求（D-53） |

```powershell
ffmpeg -y -i "<源 mp3>" -vn -ac 1 -ar 44100 -c:a libvorbis -b:a 64k "<目标>.ogg"
ffmpeg -y -i "<源 jpg>" -vf "scale=18:18:flags=lanczos" -pix_fmt rgba "<目标>.png"
```

- `-vn` 必须加：源 mp3 若带封面，不加会把封面当视频流写进 ogg，Minecraft 直接拒绝加载。
- `-ar 44100` 固定采样率：不同源的采样率混在一起会让音量听起来忽大忽小。
- 18×18 不是 16×16：效果图标在原版 HUD 里按 18×18 绘制（`MobEffectInstance` 的图标区），16×16 会被拉伸 1.125 倍，边缘发虚。

### 6.3 `assets/farmstorm/sounds.json`（1 个文件，2 个事件）

BRIEF §7.7 要求「`sounds.json` 两个事件：`wild_hunter_loop`（循环）、`zangief_piledriver`（单次）」：

```json
{
  "wild_hunter_loop": {
    "category": "player",
    "subtitle": "subtitles.farmstorm.wild_hunter_loop",
    "sounds": [
      { "name": "farmstorm:wild_hunter_loop", "stream": false, "attenuation_distance": 16 }
    ]
  },
  "zangief_piledriver": {
    "category": "player",
    "subtitle": "subtitles.farmstorm.zangief_piledriver",
    "sounds": [
      { "name": "farmstorm:zangief_piledriver", "attenuation_distance": 16 }
    ]
  }
}
```

| 字段 | 取值 | 理由 |
|---|---|---|
| `category` | `player` | 对应 `SoundSource.PLAYERS`（D-76：不占 `MUSIC`，不打断原版背景音乐） |
| `stream` | `false` | 循环音每次循环都要重读；`stream` 会在接缝处留缝。`Animals` 转换后是低码率 mono，整段进内存没有压力 |
| `attenuation_distance` | `16` | D-76 的「衰减距离 16 格」= `wild_hunter.loopSoundRange` 的**默认值**的静态镜像（json 读不到 config；config 改成别的值时以 §4.6 的程序侧衰减为准）；**待验证（挂 RT-14）**：该字段在 1.21.1 的 `SoundDefinition.Sound`（`sounds.json` 条目）里的键名与语义是否就是「可听距离」——验法见 §4.6 |
| `subtitle` | `subtitles.farmstorm.<事件名>` | 对应 §7.3 的两条字幕键（打开字幕后显示） |

### 6.4 转换脚本 `tools/convert_media.ps1`

**工具链要求**：

| 项 | 要求 | 本机现状 |
|---|---|---|
| ffmpeg | 在 `PATH` 上，或由 `-Ffmpeg` 参数给绝对路径；需带 `libvorbis` 编码器（`ffmpeg -encoders \| Select-String libvorbis` 有输出） | **已安装但不在 PATH 上**：本机可用的是 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`（ffmpeg 7.1 essentials，带 `libvorbis`）；Trae CN 自带的 6.1.1 是裁剪构建、无 `libvorbis`，不可用 → RT-14 的现实依据 |
| PowerShell | 5.1 或 7.x | 满足 |
| 网络 | **不需要**。脚本不许自己下载 ffmpeg | 本机 npm / maven / 镜像三个源全部不可达（D-53 的实测记录） |

**参数**：三种调用形态——不带参数（ffmpeg 在 PATH 上时全量转换）、`-Ffmpeg <绝对路径>`（ffmpeg 不在 PATH 上）、`-CheckOnly`（只校验环境与三个源文件，一个字节都不写）。

```powershell
pwsh -File tools/convert_media.ps1
pwsh -File tools/convert_media.ps1 -Ffmpeg "D:\dsh\default\sf6_audio\tools\ffmpeg.exe"
pwsh -File tools/convert_media.ps1 -CheckOnly
```

**失败行为（D-53 的硬要求，逐条不可放宽）**：

| 情况 | 行为 | 退出码 |
|---|---|---|
| 找不到 `ffmpeg` | 打印安装指引（`winget install Gyan.FFmpeg` / 官网 zip 解压 + 加 PATH / 项目内 `tools/ffmpeg/bin` 放好），**直接退出**，一个文件都不写 | `1` |
| 源文件不存在 | 列出缺哪个绝对路径，**直接退出** | `1` |
| ffmpeg 返回非 0 | 打印它的 stderr 全文，**直接退出**，不继续下一个文件 | `1` |
| 转换成功 | 逐项打印 `源 → 目标 (大小, 声道, 码率/尺寸)`，结尾对每个目标做一次回读校验 | `0` |

**禁止（D-53 原文「不许静默跳过、不许回退成原扩展名」）**：

- 不许 `try { … } catch { Write-Warning }` 之后继续——失败必须终止整条管线。
- 不许「转不出来就把 `.mp3` 改个名塞进 `sounds/`」——Minecraft 只解 Ogg Vorbis，改名得到的是 0 播放。
- 不许在脚本里下载 ffmpeg（本机三个源都不可达，写了也是死代码）。

**幂等**：目标文件已存在且比源文件新时跳过该文件并打印 `skip`，让脚本可以反复跑。

**回读校验**（转换成功的判据，逐条打印）：

```powershell
ffprobe -v error -show_entries stream=codec_name,channels,sample_rate,bit_rate -of json "<目标>.ogg"
ffprobe -v error -show_entries stream=width,height,pix_fmt -of json "<目标>.png"
```

第一条期望 `codec_name=vorbis`、`channels=1`、`bit_rate` ≈ 64000；第二条期望 `width=18`、`height=18`。任一不符即退出码 1。

### 6.5 压缩干草块贴图：从原版干草块侧面生成灰度版

D-57 要求「材质 = 原版干草块侧面的灰度版」。这一步**不需要 ffmpeg**，用既有 `tools/gen_textures.py` 同一套工具链（Python 3.9+ / Pillow 9.1+，`zipfile` 直读客户端 jar，不解包）。

**源**：客户端 jar 里的 `assets/minecraft/textures/block/hay_block_side.png`。本机已核实该文件存在：

| 项 | 值 |
|---|---|
| 客户端 jar | `D:\project_farm\.tools\gradle-home\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar` |
| 条目 | `assets/minecraft/textures/block/hay_block_side.png`（202 字节，16×16） |
| 抽取方式 | `zipfile.ZipFile(jar).read(entry)` → `PIL.Image.open(BytesIO(...))`，不落地临时文件（与 `gen_textures.py` 的既有做法一致） |

**转换**（三步，写进 `tools/gen_textures_002.py`）：

```python
src = Image.open(BytesIO(zipfile.ZipFile(jar).read("assets/minecraft/textures/block/hay_block_side.png")))
rgba = src.convert("RGBA")
gray = rgba.convert("L").convert("RGBA")      # 只取亮度，保留 alpha（原版贴图是不透明）
gray.save(out / "assets/farmstorm/textures/block/compressed_hay_block.png")
```

| 要求 | 落实 |
|---|---|
| 必须是灰度 | `convert("L")` 后再 `convert("RGBA")`：R=G=B=L。不许用「降低饱和度」代替（那会留一点色偏） |
| 尺寸不变 | 16×16，与原版同尺寸；Noise/模糊一个都不加 |
| 可复跑 | 脚本每次从 jar 重读源图，不缓存中间产物 |

**同一个脚本还产 4 张 16×16 物品图标**（`molotov` / `zangief_cane` / `wild_apple` / `compressed_puffed_wheat`）与 1 张 `textures/block/zangief_cane.png`（只作陷阱方块的 `particle` 贴图，D-68）。占位稿规则沿用 `gen_textures.py`：作物图标右下角叠绿色「+」（`plan0.0.1.txt` 第 8 条区分原版与改良），四个新作物同样加「+」。

### 6.6 1×1 白色贴图

`assets/farmstorm/textures/misc/white.png`：1×1、全白、RGBA（255,255,255,255）。BRIEF §7.2 指定。剪影靠 `setShaderColor` 上红色，贴图本身必须是纯白——否则颜色会相乘变脏。

### 6.7 管线顺序与证据

| 序 | 步骤 | 命令 | 产物 | 失败即停 |
|---|---|---|---|---|
| 1 | 音频 + 图标 | `pwsh -File tools/convert_media.ps1` | 2 个 `.ogg` + 1 个 18×18 `.png` | 是（缺 ffmpeg 就退 1） |
| 2 | 7 张贴图（4 张物品图标 + 压缩干草块 + 桑吉尔蔗粒子 + 1×1 白图） | `python -X utf8 tools/gen_textures_002.py --vanilla-assets "<客户端 jar>" --out src/main/resources` | 7 个 `.png`（6 张 16×16 + 1 张 1×1） | 是 |
| 3 | 手写资源 | 编辑器写 `sounds.json` / `shaders/core/*` / `post_effect/grayscale.json` | 4 个文件 | — |
| 4 | DataGen | `.tools\gw.cmd build` | §7.2 里标「DataGen」的 24 个 JSON | 是 |

**PNG 累计（与 §7.2 逐行对齐）**：步骤 1 出 **1** 张（#8 效果图标，18×18）+ 步骤 2 出 **7** 张（#1–#4 物品图标、#5 压缩干草块、#6 桑吉尔蔗粒子、#7 白图）= **8** 张，正是 §7.2 的 #1–#8，一张不多一张不少。OGG：步骤 1 出 **2** 个（#9–#10）。

**证据要留下**（RT-14 的验收材料）：步骤 1 的完整 stdout（含每个目标的回读校验行）、步骤 2 的「原版命中 / 退回占位」清单、`dist/farmstorm-0.0.2+mc1.21.1.jar` 内 `assets/farmstorm/sounds/` 的 2 个 `.ogg` 条目（`jar tf` 或解压看）。

**两个脚本不进 jar、也不计入 §7.2 的 36**：它们落在仓库的 `tools/` 下，是构建期工具，不是模组资源。

---

## 7. 资源与数据文件清单

### 7.1 汇总

| 类别 | v0.0.1 | 增量 | v0.0.2 |
|---|---|---|---|
| `.json` | 169 | +24 | **193** |
| `.png` | 91 | +8 | **99** |
| `.ogg` | 0 | +2 | **2** |
| `.glsl`（`.vsh` + `.fsh`） | 0 | +2 | **2** |
| `.toml`（`neoforge.mods.toml`） | 1 | +0 | **1** |
| `pack.mcmeta` | 1 | +0 | **1** |
| **合计** | **262** | **+36** | **298** |

口径提醒（BRIEF §4.1）：`src/generated/resources/.cache` 下的 11 个无扩展名文件是 DataGen 缓存，**不进 jar、不计入**。语言文件的 2 个 `.json` 已经算在 169 里（内容变、文件不增）。

### 7.2 新增资源文件逐项清单（**36 行**）

「产线」列：`手写` = 用编辑器写死在 `src/main/resources`；`DataGen` = 由 Provider 生成到 `src/generated/resources`；`脚本` = 由 §6 的转换脚本产出。

| # | 路径（相对 `src/{main,generated}/resources`） | 类别 | 产线 | 用途 | 依据 |
|---|---|---|---|---|---|
| 1 | `assets/farmstorm/textures/item/molotov.png` | PNG 16×16 | 脚本 | 莫洛托夫物品图标 | §2.2 |
| 2 | `assets/farmstorm/textures/item/zangief_cane.png` | PNG 16×16 | 脚本 | 桑吉尔蔗物品图标（世界隐形，图标独立，D-68） | D-68 |
| 3 | `assets/farmstorm/textures/item/wild_apple.png` | PNG 16×16 | 脚本 | 狂野苹果物品图标 | §2.2 |
| 4 | `assets/farmstorm/textures/item/compressed_puffed_wheat.png` | PNG 16×16 | 脚本 | 压缩膨胀麦物品图标 | D-56 |
| 5 | `assets/farmstorm/textures/block/compressed_hay_block.png` | PNG 16×16 灰度 | 脚本 | 压缩干草块六面贴图（干草块侧面灰度版） | D-57 / §6.5 |
| 6 | `assets/farmstorm/textures/block/zangief_cane.png` | PNG 16×16 | 脚本 | 陷阱方块的 `textures.particle`（世界隐形，D-68） | D-68 |
| 7 | `assets/farmstorm/textures/misc/white.png` | PNG 1×1 | 脚本 | 红色剪影的底色贴图 | BRIEF §7.2 |
| 8 | `assets/farmstorm/textures/mob_effect/wild_hunter.png` | PNG 18×18 | 脚本 | 狂野猎人效果图标 | D-53 / D-66 |
| 9 | `assets/farmstorm/sounds/wild_hunter_loop.ogg` | OGG mono 64k | 脚本 | 循环音本体 | D-53 / D-76 |
| 10 | `assets/farmstorm/sounds/zangief_piledriver.ogg` | OGG mono 64k | 脚本 | 桑吉尔蔗触发音本体 | D-53 / D-66 |
| 11 | `assets/farmstorm/sounds.json` | JSON | 手写 | 2 个声音事件定义 | D-76 / §6.3 |
| 12 | `assets/farmstorm/shaders/core/grayscale.json` | JSON | 手写 | 灰度 `ShaderInstance` 的程序声明 | BRIEF §7.1 |
| 13 | `assets/farmstorm/shaders/core/grayscale.vsh` | GLSL | 手写 | 全屏四边形顶点着色器 | BRIEF §7.1 / §3.2 |
| 14 | `assets/farmstorm/shaders/core/grayscale.fsh` | GLSL | 手写 | 灰度片段着色器（`dot(rgb, 亮度系数)`） | BRIEF §7.1 / §3.2 |
| 15 | `assets/farmstorm/post_effect/grayscale.json` | JSON | 手写 | 灰度后处理声明（RT-10 退化分支的 `PostChain` 挂载） | BRIEF §3.2 |
| 16 | `assets/farmstorm/blockstates/compressed_hay_block.json` | JSON | DataGen | 单变体 `cube_all` | D-57 |
| 17 | `assets/farmstorm/blockstates/zangief_cane.json` | JSON | DataGen | `face`×`facing`×`floating` 全变体 → 空模型 | D-68 |
| 18 | `assets/farmstorm/models/block/compressed_hay_block.json` | JSON | DataGen | 六面同贴图模型 | D-57 |
| 19 | `assets/farmstorm/models/block/zangief_cane.json` | JSON | DataGen | 空模型（只有 `textures.particle`） | D-68 |
| 20 | `assets/farmstorm/models/item/molotov.json` | JSON | DataGen | `item/generated` + layer0 | §2.2 |
| 21 | `assets/farmstorm/models/item/zangief_cane.json` | JSON | DataGen | `item/generated`（不引用方块模型） | D-68 |
| 22 | `assets/farmstorm/models/item/wild_apple.json` | JSON | DataGen | `item/generated` | §2.2 |
| 23 | `assets/farmstorm/models/item/compressed_puffed_wheat.json` | JSON | DataGen | `item/generated` | D-56 |
| 24 | `assets/farmstorm/models/item/compressed_hay_block.json` | JSON | DataGen | `parent = farmstorm:block/compressed_hay_block` | D-57 |
| 25 | `data/farmstorm/recipe/converter.json` | JSON | DataGen | 转换器合成：中心 `minecraft:crafting_table` + 八格 `#farmstorm:converter_fuel` | D-52 |
| 26 | `data/farmstorm/recipe/compressed_puffed_wheat.json` | JSON | DataGen | 无序：4 个 `puffed_wheat` → 1 个 `compressed_puffed_wheat` | D-67 |
| 27 | `data/farmstorm/recipe/molotov.json` | JSON | DataGen | `ConverterRecipe`：`minecraft:crimson_fungus` ⇄ `molotov`，双向，经验 3，无催化剂 | D-54 同形 |
| 28 | `data/farmstorm/recipe/zangief_cane.json` | JSON | DataGen | `ConverterRecipe`：`minecraft:sugar_cane` ⇄ `zangief_cane`，双向，经验 3 | D-54 同形 |
| 29 | `data/farmstorm/recipe/wild_apple.json` | JSON | DataGen | `ConverterRecipe`：`minecraft:golden_apple` ⇄ `wild_apple`，双向，经验 3 | D-54 |
| 30 | `data/farmstorm/advancement/first_molotov.json` | JSON | DataGen | 进度：初次投出莫洛托夫 | D-63 |
| 31 | `data/farmstorm/advancement/zangief_hit.json` | JSON | DataGen | 进度：桑吉尔蔗首次命中 | D-63 |
| 32 | `data/farmstorm/advancement/first_wild_hunter.json` | JSON | DataGen | 进度：首次获得狂野猎人效果 | D-63 |
| 33 | `data/farmstorm/loot_table/blocks/compressed_hay_block.json` | JSON | DataGen | 掉自身（`requiresCorrectToolForDrops` 决定空手不掉） | D-57 |
| 34 | `data/farmstorm/loot_table/blocks/zangief_cane.json` | JSON | DataGen | 部署方块口径：`survives_explosion` + 掉自身 | 既有 5 张同形 |
| 35 | `data/farmstorm/tags/item/converter_fuel.json` | JSON | DataGen | 新建物品标签：原版农作物 + 19 条档案里能当农作物的物品 | D-52 |
| 36 | `data/minecraft/tags/block/needs_diamond_tool.json` | JSON | DataGen | 新建：`compressed_hay_block` 需要钻石镐 | D-57 |

列名对应关系（本节是文件清单，不是键表）：**文件路径** ↔ 键名；**类别** ↔ 默认值（含文件尺寸，如 `PNG 16×16`）；**产线** ↔ 范围（该文件由谁产出、可否复跑）；**用途** ↔ 用途；**依据** ↔ 依据 D 编号。

**行数核对**：`36` 行；分类计数 PNG 8（#1–#8）+ OGG 2（#9–#10）+ JSON 24（#11–#12、#15–#36）+ GLSL 2（#13–#14）→ 见下表。#11–#36 共 26 个槽位 = 24 个 JSON + 2 个 GLSL，与分类计数一致。

| 类别 | 行号 | 数量 |
|---|---|---|
| PNG | #1–#8 | 8 |
| OGG | #9–#10 | 2 |
| JSON | #11–#12、#15–#36 | 24 |
| GLSL | #13–#14 | 2 |
| **合计** | — | **36** |

**追加型改动（不产生新文件，不计入 36）**：`data/minecraft/tags/block/mineable/pickaxe.json` 追加 `compressed_hay_block`；`data/farmstorm/tags/block/deployed_crops.json` 追加 `zangief_cane`；`data/farmstorm/tags/item/modified_crops.json` 追加 4 条；`assets/farmstorm/lang/{zh_cn,en_us}.json` 追加 39 条（§7.3）；既有的 12 个进度文件不动。

### 7.3 新增语言文件键逐项清单（**39 行**）

键集由 `ModLangProvider` 的同一张 `ENTRIES` 表按列写出，因此 `zh_cn` 与 `en_us` 的键天然相等。`222 + 39 = 261`，两份都必须是 **261**。

| # | 键 | `zh_cn` | `en_us` | 依据 |
|---|---|---|---|---|
| 1 | `item.farmstorm.molotov` | 莫洛托夫！ | Molotov | §2.2 / 保留原文感叹号 |
| 2 | `item.farmstorm.zangief_cane` | 桑吉尔蔗 | Zangief Cane | §2.2 |
| 3 | `item.farmstorm.wild_apple` | 狂野苹果 | Wild Apple | §2.2 |
| 4 | `item.farmstorm.compressed_puffed_wheat` | 压缩膨胀麦 | Compressed Puffed Wheat | D-56 |
| 5 | `item.farmstorm.compressed_hay_block` | 压缩干草块 | Compressed Hay Block | D-57 |
| 6 | `block.farmstorm.zangief_cane` | 桑吉尔蔗 | Zangief Cane | §2.2 |
| 7 | `block.farmstorm.compressed_hay_block` | 压缩干草块 | Compressed Hay Block | D-57 |
| 8 | `effect.farmstorm.wild_hunter` | 狂 野 猎 人 | Wild Hunter | `plan0.0.2.txt` 第 37 行 |
| 9 | `effect.farmstorm.zangief_piledriver` | 桑吉尔蔗打桩 | Zangief Piledriver | §3.6（隐藏效果，名字只用于 `/effect` 补全） |
| 10 | `subtitles.farmstorm.wild_hunter_loop` | 狩猎主题响起 | Hunting theme plays | §6.3 |
| 11 | `subtitles.farmstorm.zangief_piledriver` | 打桩机启动 | Piledriver hits | §6.3 |
| 12 | `advancements.farmstorm.first_molotov.title` | 火了！ | It's Lit! | D-63 |
| 13 | `advancements.farmstorm.first_molotov.description` | 初次投出莫洛托夫！ | Throw a Molotov for the first time | D-63 |
| 14 | `advancements.farmstorm.zangief_hit.title` | 手摇重 360 | Hand-Cranked 360 | D-63 |
| 15 | `advancements.farmstorm.zangief_hit.description` | 桑吉尔蔗首次命中一名玩家 | Land a Zangief Cane on a player for the first time | D-63 |
| 16 | `advancements.farmstorm.first_wild_hunter.title` | 狂野猎人 | Wild Hunter | D-63 |
| 17 | `advancements.farmstorm.first_wild_hunter.description` | 首次获得「狂 野 猎 人」效果 | Obtain the Wild Hunter effect for the first time | D-63 |
| 18 | `farmstorm.configuration.molotov.effectRadius` | 落点球半径（格） | Impact sphere radius (blocks) | §8.1 #1 |
| 19 | `farmstorm.configuration.molotov.fireChance` | 每个候选方块被点火的概率（%） | Chance (%) to place fire on each candidate block | §8.1 #2 |
| 20 | `farmstorm.configuration.molotov.triggerExplosionPower` | 落点触发爆炸的威力（0 = 只当触发器） | Power of the landing trigger explosion (0 = trigger only) | §8.1 #3 |
| 21 | `farmstorm.configuration.wild_apple.nutrition` | 营养值 | Nutrition | §8.1 #4 |
| 22 | `farmstorm.configuration.wild_apple.saturationModifier` | 饱和度修正 | Saturation modifier | §8.1 #5 |
| 23 | `farmstorm.configuration.wild_hunter.durationTicks` | 效果基础时长（tick） | Base effect duration (ticks) | §8.1 #6 |
| 24 | `farmstorm.configuration.wild_hunter.killExtensionTicks` | 每次击杀延长的时长（tick） | Duration added per kill (ticks) | §8.1 #7 |
| 25 | `farmstorm.configuration.wild_hunter.killHealPoints` | 每次击杀回复的生命点数 | Health restored per kill | §8.1 #8 |
| 26 | `farmstorm.configuration.wild_hunter.searchRadius` | 索敌球半径（格） | Target search radius (blocks) | §8.1 #9 |
| 27 | `farmstorm.configuration.wild_hunter.searchAngleDegrees` | 索敌视线夹角上限（度） | Target search view angle limit (degrees) | §8.1 #10 |
| 28 | `farmstorm.configuration.wild_hunter.dashTicks` | 吸附冲刺跨越的刻数 | Ticks the dash takes | §8.1 #11 |
| 29 | `farmstorm.configuration.wild_hunter.speedBonusPercent` | 移动速度加成（%） | Movement speed bonus (%) | §8.1 #12 |
| 30 | `farmstorm.configuration.wild_hunter.attackSpeedBonusPercent` | 攻击速度加成（%） | Attack speed bonus (%) | §8.1 #13 |
| 31 | `farmstorm.configuration.wild_hunter.attackDamageBonusPercent` | 攻击伤害加成（%） | Attack damage bonus (%) | §8.1 #14 |
| 32 | `farmstorm.configuration.wild_hunter.highlightRadius` | 红色剪影的最大距离（格） | Maximum silhouette distance (blocks) | §8.1 #15 |
| 33 | `farmstorm.configuration.wild_hunter.loopSoundRange` | 循环音的可听距离（格） | Looping sound audible range (blocks) | §8.1 #16 |
| 34 | `farmstorm.configuration.zangief_cane.triggerRadius` | 触发球半径（格） | Trigger sphere radius (blocks) | §8.1 #17 |
| 35 | `farmstorm.configuration.zangief_cane.stageDelayTicks` | 相位 0 的等待刻数 | Ticks spent in phase 0 | §8.1 #18 |
| 36 | `farmstorm.configuration.zangief_cane.levitationLevel` | 漂浮等级（1 = I 级） | Levitation level (1 = level I) | §8.1 #19 |
| 37 | `farmstorm.configuration.zangief_cane.levitationDurationTicks` | 漂浮时长（tick） | Levitation duration (ticks) | §8.1 #20 |
| 38 | `farmstorm.configuration.zangief_cane.fallTicks` | 相位 2 的目标落地时长（tick） | Target landing duration of phase 2 (ticks) | §8.1 #21 |
| 39 | `farmstorm.configuration.zangief_cane.spinDegreesPerTick` | 水平自转速度（度/刻） | Horizontal spin speed (degrees/tick) | §8.1 #22 |

列名对应关系：**键** ↔ 键名；**`zh_cn` / `en_us` 两列** ↔ 值（语言文件没有「范围」，两份文本互为对照）；**类别**由键前缀给出（`item.` / `block.` / `effect.` / `subtitles.` / `advancements.` / `farmstorm.configuration.`）；**依据**见最后一列。

**行数核对**：`39` 行 = 物品 5（#1–#5）+ 方块 2（#6–#7）+ 效果 2（#8–#9）+ 字幕 2（#10–#11）+ 进度 6（#12–#17）+ config 22（#18–#39）。`222 + 39 = 261`。

**没有新增的两类，写明理由**：

| 没有新增 | 为什么 |
|---|---|
| `tooltip.farmstorm.*` | v0.0.1 的 16 条 tooltip 键里，类别键是**按类别**而不是按作物（`category.trap` / `category.damage` / `category.construction` / `category.buff` 都已存在），4 个新作物的类别集合是这四个的子集，所以 0 条新增。总数仍为 16 |
| `death.attack.*` | 莫洛托夫不造成伤害（§2.4：`damageCap = 0` + `NEVER_BREAK`），没有新伤害类型，所以没有新死亡信息 |

### 7.4 核对方法

| 数 | 怎么核 | 期望 |
|---|---|---|
| 资源 298 | `Get-ChildItem -Recurse -File src\main\resources, src\generated\resources \| Where-Object Name -ne '.cache' \| Measure-Object`，再减去 `.cache` 下的 11 个 | 298 |
| JSON 193 | 同上但只数 `.json` | 193 |
| PNG 99 | 只数 `.png` | 99 |
| 语言键 261 | `python -c "import json;print(len(json.load(open('…/zh_cn.json'))))"`，`en_us` 同跑 | 两个都 261 |
| 两份键集相等 | 同上但比 `set(a) == set(b)` | `True` |

---

## 8. config 新增键全量清单

### 8.1 逐项清单（**22 行**）

本表**逐列照抄 `.work/v002-BRIEF.md` §8「config 新增键唯一清单（22 条）」**，键名 / 类型 / 默认值 / 范围四列与那份表逐字一致——BRIEF §8 的裁定 A-01…A-05 已在 `11-DESIGN-0.0.2.md` 与本文之间定死口径，两份文档都不许各自再数一遍。全部进 `config/farmstorm-server.toml`（`ModConfig.Type.SERVER`）。键路径写作 `<group>.<key>`（裁定 A-04），语言键写作 `farmstorm.configuration.<group>.<key>`，与 v0.0.1 的 117 条完全同构。

| # | 键 | 类型 | 默认值 | 范围 | 用途 | 依据 |
|---|---|---|---|---|---|---|
| 1 | `molotov.effectRadius` | double | 4.0 格 | 0.0–16.0 | 落点球半径：球内每个「上表面接触空气」的方块独立掷一次 | D-58 |
| 2 | `molotov.fireChance` | double | 85.0 % | 0.0–100.0 | 每个候选方块放火的概率；**余下的百分比自动放岩浆块，不设第二个键** | D-58 |
| 3 | `molotov.triggerExplosionPower` | int | 0 | 0–100 | 落点触发爆炸的威力；0 = 只当落点触发器 | D-58 |
| 4 | `wild_apple.nutrition` | int | 4 点 | 0–20 | 狂野苹果营养值（取原版金苹果同值） | D-54 + 本文补充 |
| 5 | `wild_apple.saturationModifier` | double | 1.2 | 0.0–10.0 | 狂野苹果饱和度修正（取原版金苹果同值） | D-54 + 本文补充 |
| 6 | `wild_hunter.durationTicks` | int | 700 刻（35 秒） | 1–72000 | 效果基础时长；狂野苹果授予的就是这个时长 | plan0.0.2.txt 第 52 行 |
| 7 | `wild_hunter.killExtensionTicks` | int | 160 刻（8 秒） | 0–72000 | 效果期间每击杀一个生物的续时；无次数上限 | D-74 |
| 8 | `wild_hunter.killHealPoints` | double | 10.0 点 | 0.0–40.0 | 每次击杀回复的生命点数 | D-74 |
| 9 | `wild_hunter.searchRadius` | double | 5.0 格 | 0.0–32.0 | 索敌球半径 | D-64 |
| 10 | `wild_hunter.searchAngleDegrees` | double | 30.0 度 | 0.0–180.0 | 候选与视线方向的夹角上限 | D-64 |
| 11 | `wild_hunter.dashTicks` | int | 3 刻 | 1–20 | 吸附冲刺跨越到位所用的刻数 | D-65 |
| 12 | `wild_hunter.speedBonusPercent` | double | 30.0 % | 0.0–1000.0 | 移动速度加成 | plan0.0.2.txt 第 40 行 |
| 13 | `wild_hunter.attackSpeedBonusPercent` | double | 30.0 % | 0.0–1000.0 | 攻击速度加成 | plan0.0.2.txt 第 40 行 |
| 14 | `wild_hunter.attackDamageBonusPercent` | double | 40.0 % | 0.0–1000.0 | 攻击伤害加成 | plan0.0.2.txt 第 40 行 |
| 15 | `wild_hunter.highlightRadius` | double | 64.0 格 | 0.0–256.0 | 红色剪影的最大距离 | D-62 |
| 16 | `wild_hunter.loopSoundRange` | double | 16.0 格 | 0.0–64.0 | 循环音的可听距离 | D-76 |
| 17 | `zangief_cane.triggerRadius` | double | 1.5 格 | 0.0–8.0 | 触发球半径（球形欧氏距离） | D-73 |
| 18 | `zangief_cane.stageDelayTicks` | int | 14 刻 | 0–200 | 相位 0（已倒转、等待漂浮）的时长 | D-69 |
| 19 | `zangief_cane.levitationLevel` | int | 7 级 | 1–255 | 漂浮等级；amplifier = 等级 − 1 = 6 | D-69 |
| 20 | `zangief_cane.levitationDurationTicks` | int | 20 刻（1 秒） | 0–72000 | 漂浮时长 = 相位 1 时长 | D-69 |
| 21 | `zangief_cane.fallTicks` | int | 40 刻（2 秒） | 1–200 | 相位 2 目标落地时长；服务端逐刻伺服向下速度以落在该时长内 | plan0.0.2.txt 第 29 行 + 本文补充 |
| 22 | `zangief_cane.spinDegreesPerTick` | double | **72.0** 度/刻 | 0.0–360.0 | 相位 2 水平自转；72 × 20 = 1440 度/秒 | D-70 |

**行数核对**：`22` 行 = `molotov` 3（#1–#3）+ `wild_apple` 2（#4–#5）+ `wild_hunter` 11（#6–#16）+ `zangief_cane` 6（#17–#22）。`117 + 22 = 139`。

**相对 11-DESIGN 首版 23 条的四处裁决，本文的落地**（裁定记录见 BRIEF §8.1）：

| 裁定 | 结论 | 本文对应 |
|---|---|---|
| A-01 | 只留 `molotov.fireChance`，不设 `magmaChance` | #2 的用途栏写明「余下的百分比自动放岩浆块」 |
| A-02 | 时长归 `wild_hunter.durationTicks`，狂野苹果不单设 `hunterDurationTicks` | #6 的用途栏写明「狂野苹果授予的就是这个时长」 |
| A-03 | 用 `zangief_cane.fallTicks` + 逐刻伺服，删掉「定速」方案 | #21；机制见 §3.11 与 §5.2（那个定速键已删，现版本不含它） |
| A-04 | 正文写 `<group>.<key>`，语言键才加 `farmstorm.configuration.` 前缀 | 本表第一列与 §7.3 的 #18–#39 |
| A-05 | `spinDegreesPerTick` 默认 **72.0** 度/刻 | #22 |

> **#22 的默认值与 plan 原文一致**：`plan0.0.2.txt` 第 29 行要求「1440 度每秒」，1440 ÷ 20 刻/秒 = **72.0 度/刻**。旧版事实卡曾把该算式误写成 24，已由 BRIEF §8.1 的裁决 A-05 修正为 72.0。方向固定为逆时针，不做成配置项。

**六个默认值/口径的来源标注**：BRIEF 未给数字、由本文按惯例推导的是 #4、#5（取原版金苹果同值，因为 D-54 的对应关系就是金苹果）、#21 的默认值 40 刻（plan 只给「1.7–2 秒」区间，取上界 2.0 秒）；#6 有出处（plan 第 52 行「35 秒」）、#12–#14 有出处（plan 第 40 行的三个百分比）、#22 有出处（plan 第 29 行的 1440 度/秒）。

### 8.2 TOML 片段（新增的 4 个分组，按字母序追加在文件末尾）

```toml
[molotov]
effectRadius = 4.0                 # D-58：落点球半径（格）
fireChance = 85.0                  # D-58：每格放火概率（%），其余百分比放岩浆块
triggerExplosionPower = 0          # D-58：落点爆炸威力，0 = 只当触发器

[wild_apple]
nutrition = 4                      # D-54：与金苹果同值
saturationModifier = 1.2           # D-54：与金苹果同值

[wild_hunter]
durationTicks = 700                # 35 秒
killExtensionTicks = 160           # D-74：+8 秒，无次数上限
killHealPoints = 10.0              # D-74：+10 点生命
searchRadius = 5.0                 # D-64：5 格
searchAngleDegrees = 30.0          # D-64：30 度
dashTicks = 3                      # D-65：3 刻跨到位
speedBonusPercent = 30.0           # +30% 移速
attackSpeedBonusPercent = 30.0     # +30% 攻速
attackDamageBonusPercent = 40.0    # +40% 攻击伤害
highlightRadius = 64.0             # D-62：红色剪影最大距离（格）
loopSoundRange = 16.0              # D-76：循环音可听距离（格）

[zangief_cane]
triggerRadius = 1.5                # D-73：球形 1.5 格
stageDelayTicks = 14               # D-69：14 刻后漂浮
levitationLevel = 7                # D-69：VII 级 → amplifier 6
levitationDurationTicks = 20       # D-69：1 秒
fallTicks = 40                     # 相位 2 目标落地时长（刻）= 2 秒，逐刻伺服
spinDegreesPerTick = 72.0          # D-70：1440 度/秒 ÷ 20
```

**为什么新分组按字母序追加**：v0.0.1 的分组顺序是 DEC 的书写顺序，v0.0.2 的四个新组没有 DEC 行号可依，按字母序（`molotov` < `wild_apple` < `wild_hunter` < `zangief_cane`）追加可以让以后每一个版本的新组都落在末尾、不打乱既有 toml 的 diff（本文补充）。

### 8.3 键数与语言键的相等性

| 断言 | 值 | 怎么核 |
|---|---|---|
| `FarmstormConfig` 新键 | **22** | §8.1 的行数 |
| 新增 `farmstorm.configuration.*` 语言键 | **22** | §7.3 的 #18–#39，逐条对应 §8.1 的 #1–#22 |
| 两份相等 | `22 == 22` | `ModLangProvider` 用同一张 `ENTRIES` 表按列写两份文件，键集天然相等 |
| 新总数 | **139** | `117 + 22` |
| 语言键新总数 | **261** | `222 + 39`（其中 config 22 条） |

实现阶段的机械比对：`run/config/farmstorm-server.toml` 的叶节点数应为 **139**，`zh_cn.json` 里 `farmstorm.configuration.` 前缀的键数也应为 **139**。两数不等就说明有键漏生成。

### 8.4 明确不进 config 的常量（逐条给理由）

「数值只有一个来源」不等于「所有数字都进 config」。下面这些**刻意**留在代码里。**反向核对**：凡是出现在 §8.1 的 22 行里的键（含 `fallTicks`、`highlightRadius`、`loopSoundRange`、`spinDegreesPerTick`）都**不在**本表，本表里也不许再出现同名常量。

| 常量 | 值 | 留在代码里的理由 | 依据 |
|---|---|---|---|
| `MAX_FALL_TICKS` | `100` 刻 | 兜底上限，**不是**目标时长：目标时长是 config 的 `zangief_cane.fallTicks`（默认 40 刻），这个上限只兜「落地判定失效（卡在方块缝里）」的底。安全阀不进 config 是 v0.0.1 的既有口径 | HC-13 同类 |
| 白色贴图路径 / 18×18 尺寸 / 红色 `(1.0, 0.15, 0.15, 0.45)` / 同帧 32 只上限 | — | 纯渲染参数，不改变任何玩法结果，也不是「范围」类数值。**距离类的两个值已进 config**（`wild_hunter.highlightRadius` #15、`wild_hunter.loopSoundRange` #16），客户端通过 §4.4 的镜像常量读到同一组默认值 | 本文补充 |
| `DEFAULT_SPIN_DEGREES_PER_TICK` / `DEFAULT_SEARCH_RADIUS` / `DEFAULT_SEARCH_ANGLE` / `DEFAULT_HIGHLIGHT_RADIUS` / `DEFAULT_LOOP_SOUND_RANGE` 等客户端镜像常量 | 与 config 默认值同值：72.0 / 5.0 / 30.0 / 64.0 / 16.0 | 客户端读不到 SERVER config（§4.4 的降级口径）；这 5 个镜像常量对应的键都在 §8.1 里（#22 / #9 / #10 / #15 / #16），注释里点名必须同步 | 本文补充 |
| `compressed_hay_block` 的 `strength(30.0F, 6.0F)` 与 `requiresCorrectToolForDrops()` | — | D-57 已把三个数字钉死，属方块固有属性；做成可调键等于给平衡性开后门 | D-57 |
| `THROW_COOLDOWN_TICKS` / `DEPLOY_COOLDOWN_TICKS` / `MAX_DEPLOYED_PER_*` | 既有值 | v0.0.1 既有安全阀，不动 | HC-13 |
| `LANDING_PROBE`（0.1 格） | `0.1` | 采样 epsilon，不是玩法数值（源码注释已说明） | 既有 |
| 触发音与循环音的音量 / 音高 | `1.0F` / `1.0F` | 原版默认值；改它没有玩法后果。循环音的**可听距离**是 `wild_hunter.loopSoundRange`（#16），不在这里 | 本文补充 |

---

## 9. 服务端权威与 0 包约束的维持证明

### 9.1 判据

D-50 的判据只有一句话：**三项侵入性效果所需的「服务端告诉客户端状态」一律借道隐藏 `MobEffect`；效果存在与否 + amplifier + 剩余时长即完整状态；禁止为此新增 `CustomPacketPayload`。**

v0.0.1 的 C-6 是「0 个自定义网络包」；v0.0.2 的 13 个新注册对象里**没有任何一项属于网络层**（§2.2 的 13 行里没有 packet 类），这是第一层证据。第二层是本节：把 v0.0.2 需要的每一条「服务端 → 客户端」信息逐条映射到原版包。

### 9.2 逐条映射（19 条，没有一条需要新包）

| # | 需要告诉客户端的信息 | 载体 / 触发点 | 走哪个原版包 | 为什么够 |
|---|---|---|---|---|
| 1 | 「你被打了桩机」这个布尔 | 服务端 `addEffect(zangief_piledriver, …)` | `ClientboundUpdateMobEffectPacket` | 包体自带 effect holder + amplifier + duration + flags（ambient/visible/showIcon） |
| 2 | 现在处于哪个相位（0/1/2） | 同上的 amplifier 字段 | 同 #1 | amplifier 就是相位号，不需要额外字段 |
| 3 | 相位还剩多少刻 | 同上的 duration 字段 | 同 #1 | 原版效果计时器自己会往下减，客户端读到的是权威剩余时长 |
| 4 | 相位结束（落地 / 超时） | 服务端 `removeEffect` | `ClientboundRemoveMobEffectPacket` | 「效果没了」就是「该复位了」 |
| 5 | 相位 2 每刻的向下速度（逐刻伺服，`fallTicks` 目标时长） | 服务端每 tick `ServerPlayer#setDeltaMovement(0, -h / n, 0)` | `ClientboundSetEntityMotionPacket`（相位 2 内约每 tick 一个，40 刻共约 40 个；仍是原版包） | `ServerPlayer` 的速度变更由 `ServerEntity` 的同步广播给所有追踪者（含本人）；40 个速度包分摊在 2 秒内，带宽量级与一次爆炸（`ClientboundExplodePacket` 带方块列表）相当 |
| 6 | 「你进了狂野猎人状态」 | 服务端 `addEffect(wild_hunter, …)` | `ClientboundUpdateMobEffectPacket` | 同 #1；灰度（D-61）与红色剪影（D-62）都以它为开关 |
| 7 | 狂野猎人的剩余时长（击杀续时会变长） | 服务端重新 `addEffect` 一个更长的实例 | `ClientboundUpdateMobEffectPacket` | 时长变化 = 一次新的效果包 |
| 8 | 三项属性加成（+30 % 移速 / +30 % 攻速 / +40 % 伤害） | `MobEffect#addAttributeModifiers` | 不需要包 | 原版在客户端也会执行该方法（`LivingEntity#onEffectAdded` 两侧对称），客户端属性表与服务端一致 |
| 9 | 桑吉尔蔗触发音 | `level.playSound(null, pos, …)` | `ClientboundSoundPacket` | 位置 + 事件 + 音量 + 音高，一次性 |
| 10 | 狂野猎人循环音 | 客户端自写 `SoundInstance`（D-76） | **零包** | 音的生命周期 == 效果的生命周期，客户端从 #6 就能推出该不该播 |
| 11 | 莫洛托夫落点的火 / 岩浆块 | `level.setBlock(target, state, UPDATE_ALL)` | `ClientboundBlockUpdatePacket` | 每个被改的格一个区块级更新包（原版自动合并同区块的连续更新） |
| 12 | 莫洛托夫的爆炸表现 | `FarmstormExplosion#detonate` 的 `finalizeExplosion` | `ClientboundExplodePacket` + `ClientboundLevelParticlesPacket` | 原版爆炸的两件套；威力 0 时前者只有位置与音效 |
| 13 | 投掷物实体出现在世界里 | `level.addFreshEntity(projectile)` | `ClientboundAddEntityPacket` | 原版实体生成流程 |
| 14 | 陷阱方块静默消失 | `be.consume()` 里的 `server.removeBlock(pos, false)` + `levelEvent(PARTICLES_DESTROY_BLOCK)` | `ClientboundBlockUpdatePacket` + `ClientboundLevelEventPacket` | 后者就是原版的方块破坏粒子事件，不需要自己发粒子包 |
| 15 | 击杀续时后回血 10 点 | `player.heal(10.0F)` | `ClientboundSetHealthPacket` | 服务端改生命值走原版同步 |
| 16 | 三个新进度的达成 | `PlayerAdvancements#award` | `ClientboundUpdateAdvancementsPacket` | 原版进度系统 |
| 17 | 部署方块与其 owner | `DeployedCropBlockEntity#getUpdatePacket` | `ClientboundBlockEntityDataPacket` | 既有机制（6 个同步字段），v0.0.2 不加字段 |
| 18 | 相机倒转 / 自转 | 客户端读 #2 的 amplifier + 本地 tick 累加 | **零包** | 纯渲染层，D-70 明确「不依赖服务端逐刻同步」 |
| 19 | 输入锁 | 客户端读 #1 的「在不在」 | **零包** | 纯客户端行为 |

19 条里 4 条是零包（#8 的属性加成、#10 的循环音、#18 的相机、#19 的输入锁），其余 15 条全部落在 **12 种**原版包上：`ClientboundUpdateMobEffectPacket`、`ClientboundRemoveMobEffectPacket`、`ClientboundSetEntityMotionPacket`、`ClientboundSoundPacket`、`ClientboundBlockUpdatePacket`、`ClientboundLevelEventPacket`、`ClientboundExplodePacket` / `ClientboundLevelParticlesPacket`、`ClientboundAddEntityPacket`、`ClientboundSetHealthPacket`、`ClientboundUpdateAdvancementsPacket`、`ClientboundBlockEntityDataPacket`。

### 9.3 为什么不新增 `CustomPacketPayload`

反证法：要新增一个包，必须先有一个「原版包表达不了」的状态。逐项排除：

| 候选理由 | 为什么站不住 |
|---|---|
| 「灰度开关」 | 就是 `wild_hunter` 在不在，原版效果包已经送了 |
| 「红色剪影的目标列表」 | 不需要下发列表：客户端自己遍历视野内的 `LivingEntity`，判据（距离 / 视线 / 队伍）都在客户端本地可算，且 D-62 只要求「给可见的、给自己看」 |
| 「相位号」 | 就是 amplifier（#2） |
| 「冲刺目标」 | 客户端自己算（服务端用同一份算法复核），BRIEF §7.6 的流程里没有一步需要服务端告诉客户端目标是谁 |
| 「16 格可听距离」 | `sounds.json` 的 `attenuation_distance` + `SoundInstance` 在客户端本地 |
| 「相位的精确切换时刻」 | 每个相位一个新的效果包，包本身带 duration，客户端用 duration 倒计时就能推出切换 |

代价（对比 v0.0.1 的选择，`02-TECH-SPEC.md` §5.5 已列过一次）：新增包要写 `CustomPacketPayload` + `PayloadType` 注册 + 两端 handler + 版本兼容，还要为「客户端与服务端的相位号不一致」单独设计纠错——而全部收益只是省掉几个原版包的解析。在 D-50 已经判死的前提下，这条路的成本收益是负的。

**唯一被牺牲的东西**：客户端无法显示「相位还剩几刻」的精确倒计时环（纯装饰需求）。列入延后项，不做。

### 9.4 v0.0.2 新增的服务端职责（服务端权威清单的增量）

| 职责 | 在哪 | 为什么必须在服务端 |
|---|---|---|
| 莫洛托夫的火 / 岩浆块写入 | `MolotovProjectile#scatter` | 世界写入只能服务端做（HC-03） |
| 压缩膨胀麦的方块写入 | `CompressedPuffedWheatProjectile#fill` | 同上 |
| 桑吉尔蔗的触发判定 | `ZangiefCaneTrap#findTrigger` | 己方判定要读队伍（客户端看不到完整队伍信息，源码 `DeployedCropBlockEntity#isAlly` 注释） |
| 相位推进与漂浮施加 | `ZangiefPiledriverController` | 时间与状态必须单一权威 |
| 狂野猎人的目标复核 / 伤害 / 击退 | `WildHunterCombat` | 伤害只能服务端结算 |
| 击杀续时与回血 | `WildHunterCombat#onKill` | 生命值与效果时长只能服务端改 |
| 三个新进度的授予 | `FarmstormAdvancements#award` | 进度判定在服务端（`ModAdvancementProvider#AWARD_REQUIRED` 的既有约定） |

### 9.5 客户端预测的边界（v0.0.2 新增的两处）

| 预测 | 边界 | 越界的后果 | 兜底 |
|---|---|---|---|
| 冲刺位移（§4.5） | 只改**自己的**速度，不改别人的位置，不改伤害 | 预测错 ⇒ 玩家冲了但没伤害，或没冲但有伤害（后者不可能，伤害只由服务端算） | 服务端复核；不合法就什么都不做 |
| 相机角度（§4.4） | 只改**渲染层**角度，不写回 `getYRot()` / `getXRot()` | 写回 ⇒ 玩家的真实朝向被篡改，可以隔墙打人 | 铁律：只调 `event.setXxx`，一行都不碰 `player` |

---

## 10. 风险登记册（RT-10 ~ RT-16）

编号接 `02-TECH-SPEC.md` §9 的 RT-09。**七条，编号连续，不增不删不合并**（BRIEF §7.8 的前置条件 ②）。「风险」「首要缓释」两列照抄 BRIEF §7.8，其余三列由本文补齐。

| 编号 | 风险 | 触发条件 | 首要缓释 | 兜底 | 实现阶段怎么验 |
|---|---|---|---|---|---|
| **RT-10** | 整屏灰度后处理的挂载点不成立 | `RenderGuiEvent.Post` 里切换 render target 与其它模组的 GUI 后处理打架：画面全黑、只灰半个屏、其它模组 HUD 消失；或 `RegisterShadersEvent` 注册的 `ShaderInstance` 编译失败 | 拆成世界 + GUI 两次 pass（`RenderLevelStageEvent` 的 AFTER 阶段 + `RenderGuiEvent.Post`）；再不行只灰世界并在 Release 说明里降级登记 | 只灰世界 + 在 Release 说明写明「GUI 不灰」；若连世界 pass 都不成立，D-61 降级为「只褪世界」并登记为已知缺陷 | ① 进效果后看是否整屏灰度（标题栏与物品栏也必须灰）；② 同屏开另一个做 GUI 后处理的模组，看是否互相破坏；③ `latest.log` 里 `[Render thread/ERROR]` 是否出现 GL 或 shader 编译错误；④ 记录帧时间（F3）前后差 |
| **RT-11** | 剪影重绘递归或掉帧 | 在 `RenderLivingEvent` 里误调 `EntityRenderDispatcher#render` 导致递归；或同屏生物过多导致帧时间暴涨；或 `hasLineOfSight` 在客户端不可用 | 用 `getModel().renderToBuffer` 而非 dispatcher 重绘；≤64 格 + 视线才画 | 同帧最多 32 只（按距离近的优先）；仍不行则只画「距离 ≤ 16 格」的一个子集，并在 Release 说明写明 | ① 30 只生物同屏 + 效果开启，看是否卡死或掉帧 > 3 ms；② 墙前 / 墙后各站一次，确认墙后无剪影；③ 65 格外确认无剪影；④ 看日志有无 `StackOverflowError` |
| **RT-12** | 冲刺位移的驱动方判错，玩家不动或被拉回 | 服务端 `setDeltaMovement` 对 `ServerPlayer` 不产生客户端位移；或客户端预测位移被服务端的移动校验判成「移动过快」而拉回 | 走客户端预测 + 服务端判定；备选逐刻 teleport | 切到备选：服务端每刻 `teleportTo` ≤ 1 格、带碰撞检测；切换时在 §4.5 追加决策记录 | ① 打一条 `setDeltaMovement` 日志看客户端是否位移；② 冲刺时看 `latest.log` 有无 `moved too quickly!` / `moved wrongly!`；③ 对着墙冲刺确认停在墙前不穿墙；④ 目标被方块完全占据时确认本次不吸附 |
| **RT-13** | 输入拦截有遗漏，某个键仍能穿透 | `InputEvent.Key` 与 `KeyMapping` 的先后顺序导致 1–9 没拦住；或某个 GUI 打开路径没走 `ScreenEvent.Opening` | 五个事件全上；实现阶段逐键手测一遍并留记录 | 对漏掉的那一路改挂更靠前的钩子（如再加 `ScreenEvent.KeyPressed`）；仍漏则在 Release 说明登记「该键不可锁」 | 逐键手测并填表：W/A/S/D/空格/Shift/左键/右键/1–9/滚轮上/滚轮下/E/T/`/`/ESC 共 21 项，每项记「拦住 / 穿透」；ESC 必须可用 |
| **RT-14** | ffmpeg 不在 PATH 上，或换机器 / 换构建后拿不到带 libvorbis 的 ffmpeg，音频产不出来 | 脚本拿不到可用 ffmpeg——本机现状：7.1 essentials 在 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`，**可用**；Trae CN 的 6.1.1 **无 libvorbis** 不可用；或 `sounds.json` 的 `attenuation_distance` 在 1.21.1 上语义不是「可听距离」 | 转换脚本缺 ffmpeg 或 ffmpeg 无 libvorbis 一律直接报错退出；音频任务排在资源阶段最前，早失败早发现；工具已就位并实测通过（`tools/convert_media.ps1`） | 传对 `-Ffmpeg` 绝对路径即可；换机器时按脚本打印的安装指引装一个带 libvorbis 的构建 | ① 不传 `-Ffmpeg` 且 PATH 上没有时必须打印安装指引并退出码 1，传入可用路径时打印环境可用并退出码 0；② 真跑后 `ffprobe` 回读校验声道 = 1、码率 ≈ 64 kbps；③ 游戏内站 8 / 16 / 17 / 32 格各听一次；④ `jar tf` 确认两个 `.ogg` 在 jar 里 |
| **RT-15** | 隐藏 MobEffect 被其它模组的效果清理逻辑干扰 | 第三方模组「清除全部负面效果」（`/effect clear`、药水、命令）把 `zangief_piledriver` 提前清掉，客户端仍停在相位 2：画面永远倒着、键永远锁着 | 客户端每 tick 自检「阶段效果还在不在」，不在就自己复位 | 服务端一侧对称自检：`ACTIVE` 有记录但 `getEffect` 为空 ⇒ `finish` + `warn`，不重新施加 | ① `/effect give @s farmstorm:zangief_piledriver 10 2 false` 看画面倒转 + 输入锁 + 自转；② `/effect clear @s` 后 100 ms 内必须全部复位，日志出现「状态复位（效果被外部清除）」；③ 相位切换打日志看客户端是否读到 `null`（硬切换的 1 tick 空窗） |
| **RT-16** | 整屏灰度与光影类模组（Oculus/Iris 等）不兼容 | 装了光影类模组时，灰度 pass 与光影的 framebuffer 链冲突：画面全黑、光影失效、或灰度只作用于最终合成前的一层 | 文档登记为已知不兼容，不做运行时检测 | Release 说明与 README 的「已知不兼容」节列出 Oculus / Iris；不做版本探测、不做自动降级 | ① 装 Iris + 任一光影，进效果看画面是否全黑或光影失效；② 不装光影时回到 RT-10 的第 ① 步确认正常；③ 在 `12-TECH` 与本条登记一次，不再复查 |

**七条编号清单**：RT-10、RT-11、RT-12、RT-13、RT-14、RT-15、RT-16（连续，无缺号、无合并）。

### 10.1 风险与 RT-10…RT-16 的交叉引用（便于验收时按编号回查）

| RT | 本文引用它的地方 |
|---|---|
| RT-10 | §4.1、§6 的 `post_effect/grayscale.json`、§10 |
| RT-11 | §4.2、§6.6（白贴图）、§3.13（`WildHunterOutline`） |
| RT-12 | §4.5、§3.13（`WildHunterDashClient`）、§9.5 |
| RT-13 | §4.3、§3.13（`InputLockHandler`） |
| RT-14 | §4.6、§6.1–§6.4、§7.2（#9/#10） |
| RT-15 | §3.6、§3.11、§5.2、§5.6 |
| RT-16 | §4.1（灰度与光影）、§10 |

---

## 11. 验收自检表

每条都能用一个**具体动作**判定，不接受「看起来没问题」。前 6 条是启动日志（服务端与客户端各一次），后 8 条是手测或命令。

| # | 自检项 | 检查动作 | 通过判据 |
|---|---|---|---|
| 1 | 19 条作物档案的型与类没被打乱 | 启动任一实例，看服务端日志第一屏 | 出现 `自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK`（BRIEF §4 的原文，逐字） |
| 2 | 77 个注册对象齐全 | 同一次启动，看新增的注册对象自检行 | `自检 注册对象 77 个：items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1 → OK` |
| 3 | 幸运浆果池是 16 条而不是 17 或 18 | 同一次启动，看池自检行 | `自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK` |
| 4 | 11 个实体都有渲染器（FIX-1 的既有哨兵） | 客户端启动日志 | `自检 实体渲染器 11/11 → OK`，且紧随其后那行列出 11 个实体 ID（含 `farmstorm:molotov` 与 `farmstorm:compressed_puffed_wheat`） |
| 5 | 灰度着色器编译成功 | 客户端启动日志 | 无 `Shader … could not be loaded` / `GL_INVALID` 行；进效果后 F3 帧率不出现断崖（与不进效果相比掉帧 < 3 ms） |
| 6 | 两个声音事件被资源管理器读到 | 客户端启动后在聊天栏执行 `/playsound farmstorm:wild_hunter_loop master @s ~ ~ ~ 1 1` 与 `zangief_piledriver` 各一次 | 两条命令都成功、都能听到声音；缺任一则命令报 `Unknown sound event` |
| 7 | 莫洛托夫的落点语义 | 在平地上投一颗，清点火焰与岩浆块数量并对照 D-58 | 球半径 4 格内只有「上表面接触空气」的方块上方新增了火/岩浆块；原方块一个都没少；无爆炸伤害、无方块破坏；火 : 岩浆块 ≈ 85 : 15（投 20 颗做粗核） |
| 8 | 压缩膨胀麦完全沿用膨胀麦 | 同一位置分别投一颗膨胀麦、一颗压缩膨胀麦，各记一次 | 两次的立方体边长一致（干 3 / 遇水 5）、落点方块一致、被 3.6e6 方块挡住的格一致；唯一差异是写入的方块（干草块 vs 压缩干草块） |
| 9 | 压缩干草块的抗性边界（D-57） | 用 `/setblock` 放一块，旁边放一块圆石；分别用瓜！、育碧服务器、原版 TNT 各炸一次 | 瓜！炸不掉压缩干草块、炸得掉圆石；育碧服务器两块都炸得掉；**原版 TNT 也炸得掉压缩干草块**（原版 `Explosion` 逐格减 `(resistance + 0.3) * 0.3`，6.0 抗性挡不住圆石级别的方块——圆石本身就会被 TNT 炸掉） |
| 10 | 桑吉尔蔗的三相位时序与落地时长 | 站进 1.5 格内，掐表 + 看日志；在平地 / 两格高台 / 四格高台各测一次 | 立刻倒转；14 刻后开始漂浮（VII 级、20 刻）；34 刻后开始下坠 + 自转（72.0 度/刻 = 1440 度/秒）；**三个高度测出的落地耗时都落在 `fallTicks`（默认 40 刻）± 2 刻内**（这是逐刻伺服与定速方案的分水岭）；落地后 100 ms 内画面与输入全部复位；日志有 `桑吉尔蔗 相位 0→1→2→结束` 四行与 `落地耗时 {N} 刻` |
| 11 | 桑吉尔蔗只对非己方玩家生效 | 同队玩家 A、敌对玩家 B、创造模式玩家 C、旁观者 D 各踩一次 | 只有 B 被打桩机；A（同队）不触发；C、D 不触发；陷阱触发后**方块消失且不掉落任何物品** |
| 12 | 输入锁只漏 ESC | 按 §10 RT-13 的 21 项逐键手测并填表 | 除 ESC 外全部拦住；ESC 能打开暂停菜单；效果消失后所有键立刻恢复 |
| 13 | 红色剪影的三个条件 | 站 5 格内 / 65 格外；墙前 / 墙后；同队玩家 / 敌对玩家 | 只有「≤64 格 + 无遮挡 + 非己方」的生物被画红色剪影；自己与同队玩家不画；65 格外不画 |
| 14 | 狂野猎人冲刺走服务端复核 | 开效果后对 5 格内、30° 内的生物攻击一次；再对 30° 外的生物攻击一次 | 前者吸附并造成伤害与击退；后者正常攻击、无吸附；`latest.log` 无 `moved too quickly!`（若出现，按 RT-12 调 `dashTicks`） |
| 15 | 击杀续时无上限 | 开效果后连续击杀 6 个生物，每次看效果剩余时长 | 每次「到期时刻」都 +8 秒、生命 +10 点；6 次后仍然有效（无次数上限） |
| 16 | 配置键数 139 且两侧相等 | `run/config/farmstorm-server.toml` 数叶节点；`zh_cn.json` 数 `farmstorm.configuration.` 前缀的键 | 两个数都是 **139**（117 + 22）；新增的 22 个键名与 §8.1 逐字一致，且与 BRIEF §8 的唯一清单逐列相同 |
| 17 | 资源文件 298 且分类对得上 | `Get-ChildItem -Recurse -File src\main\resources, src\generated\resources` 减 `.cache` 的 11 个 | 总数 **298**；JSON **193**、PNG **99**、OGG **2**、GLSL **2**、TOML 1、`pack.mcmeta` 1 |
| 18 | 语言键 261 且中英相等 | `ModLangProvider` 跑完后比两份 `.json` 的键集 | 两份都 **261** 键（222 + 39）；`set(zh) == set(en)` 为 `True`；其中 `tooltip.farmstorm.*` 仍是 **16** 条（没有误加） |
| 19 | 三个新进度能解锁 | 分别：投出莫洛托夫 / 让桑吉尔蔗命中一名玩家 / 获得狂野猎人效果 | 三个进度各弹一次 toast；`/advancement` 里能看到 `farmstorm:first_molotov`、`farmstorm:zangief_hit`、`farmstorm:first_wild_hunter` |
| 20 | 5 条新配方的形状与数量 | 工作台里做转换器（中心工作台 + 八格农作物）；无序放 4 个膨胀麦；转换器里转换 molotov / zangief_cane / wild_apple 各一次 | 转换器得 1 个；4 个膨胀麦得 1 个压缩膨胀麦；三条转换各扣 3 点经验且双向可用（金苹果 ⇄ 狂野苹果 正反各一次） |
| 21 | 0 个自定义网络包 | `grep -r "CustomPacketPayload" src/main/java` | **0 处匹配**（v0.0.1 也是 0；v0.0.2 不许首次出现） |
| 22 | 客户端在专用服务器上不崩 | `启动服务器.cmd` 起服，再用客户端连 | 服务端启动无 `NoClassDefFoundError`（客户端 6 个类全部由 `Dist.CLIENT` 隔离）；连接后效果相关功能正常 |

**22 条，全部可用一次具体动作判定。** 其中 #1–#6 是启动日志（自动化程度最高），#21/#22 是结构性断言（一条命令就能证伪），其余是手测。
