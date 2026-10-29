# 农场风云 v0.0.2 · 事实卡（所有子代理共用）

> 这份文件是 v0.0.2 文档施工的**唯一口径来源**。写任何一份 10–15 号文档时：
> 本文件里有的，照抄，不许改写数字、不许换术语、不许自己算一遍再报另一个数；
> 本文件里没有的，你可以按项目既有惯例补充，但必须在文档里显式标注「本文补充」。
> 与 `docs/zh/00-DECISIONS.md`（v0.0.1 真相源）冲突时：**v0.0.2 新增内容以本文件为准**；v0.0.1 既有内容以 00-DECISIONS 为准，本文件不覆盖它。

---

## 1. 项目事实（已核实，不要再查再猜）

| 项 | 值 |
|---|---|
| 工作目录 | `D:\project_farm` |
| 模组名 | 农场风云 / Farmstorm |
| modid | `farmstorm`（全小写，无下划线） |
| Java 包根 | `com.dashpig.farmstorm` |
| 作者 | `dashpig` |
| MC | 1.21.1 |
| 加载器 | NeoForge `21.1.x`（当前工程用 21.1.213） |
| Java | 21 |
| 构建插件 | ModDevGradle `net.neoforged.moddev` 2.0.148 |
| 构建入口 | `.tools\gw.cmd`（**不要**直接调 gradle） |
| v0.0.1 状态 | 已完工并发版，`dist\farmstorm-0.0.1+mc1.21.1.jar` 存在 |
| 源码规模 | `src/main/java/com/dashpig/farmstorm/` 下 **74** 个 `.java` |
| 资源产出方式 | DataGen，落在 `src/generated/resources/` |

**v0.0.1 文档地图**（本批一律不改，只读）：

```
docs/zh/00-DECISIONS.md      决策记录，D-01…D-48、C-1…C-10，15 条作物注册名表
docs/zh/01-DESIGN.md         设计文档（1195 行）
docs/zh/02-TECH-SPEC.md      技术规格（2044 行）
docs/zh/03-TASKS.md          任务拆分，F-01…F-59 / Phase 1–10
docs/zh/04-ROADMAP.md        开发大纲，M1–M6
docs/zh/05-PROMPT-MAIN.md    主提示词
docs/zh/06-PROMPT-TASKS.md   分阶段任务清单
docs/zh/07-PUBLISH.md        发布到 GitHub
docs/zh/08-DEV-GUIDE.md      开发者手册
docs/zh/09-IMPL-VS-PLAN.md   实现 vs 计划差异对照
docs/en/00–06,09            上述对应英文版（07/08 无英文版）
```

**0.0.1 的关键工程口径**（0.0.2 必须继续遵守，不许推翻）：

- **服务端权威**：所有扣物品、生成实体、伤害、方块写入一律在服务端。
- **0 个自定义网络包**（C-6）：只用原版包。这是硬约束。
- **无 Mixin**（D-40）。
- **数值只有一个来源**：玩法数值一律进 `FarmstormConfig`，代码里不许出现魔法数字。
- **型与类是正交二维**：`CropType`（投掷/部署/食用，单值）与 `CropCategory`（陷阱/炮台/破障/buff/建造/伤害，多值）互不推导。
- **方块破坏权限只由 `CropCategory.DEMOLITION` 决定**，全仓没有第二处判据。
- **注册都用 `DeferredRegister`**；物品/方块/实体的 ID 与 `CropProfile.id()` 同名。

---

## 2. 决策总表 D-49 ~ D-78

编号接 `00-DECISIONS.md` 的 D-48。每条都要在 10-DECISIONS 里成行，格式与 v0.0.1 一致（编号 / 结论 / 依据 / 落地位置）。

### 交付与文档

**D-49 · v0.0.1 文档冻结，v0.0.2 另起 6 份**
`docs/zh/00`–`09` 与 `docs/en/00`–`09` 一律不动。v0.0.2 在 `docs/zh/` 与 `docs/en/` 各新增 `10`–`15` 六份，编号延续。README 加索引节。

**D-50 · 仍保持 0 个自定义网络包**
三项侵入性效果（灰度、红色高亮、输入锁）所需的「服务端告诉客户端状态」一律借道**隐藏 MobEffect**：效果存在与否 + amplifier + 剩余时长即完整状态，原版 `ClientboundUpdateMobEffectPacket` 免费同步。禁止为此新增 `CustomPacketPayload`。

### 增量内容

**D-51 · 三个新作物都没有种植形态**
`molotov` / `zangief_cane` / `wild_apple` 的 `CropProfile.cropBlock`、`seedItem` 均为 `null`，不注册作物方块、不注册种子。先例：`melon_slice_bomb`（同样无作物方块）。三者只从转换器产出。

**D-52 · 转换器合成配方**
有序 3×3：中心 `minecraft:crafting_table`，周围八格收物品标签 `farmstorm:converter_fuel`，产出 1 个 `farmstorm:converter`。该标签内容 = 原版农作物 + 本模组全部改良作物（19 条档案里凡是能被当作「农作物」的物品）。v0.0.1 的转换器此前没有任何配方，本配方是**首条**。

**D-53 · 媒体管线口径**
Minecraft 声音引擎只解 Ogg Vorbis。三个源文件一律先转换再进 `src/main/resources`：
- 音频 → **mono, 64 kbps Ogg Vorbis**
- 效果图标 JPG → **18×18 PNG**，无透明通道要求
转换脚本随文档交付，缺 `ffmpeg` 时**直接报错退出并打印安装指引**，不许静默跳过、不许回退成原扩展名。
（**已更正**：本机有可用的 ffmpeg——7.1 essentials 构建，带 `libvorbis`，位于 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`；但它**不在 PATH 上**，脚本要用 `-Ffmpeg` 传绝对路径。Trae CN 自带的 6.1.1 是裁剪构建、无 `libvorbis`，不可用。npm/maven/镜像三个源仍全部不可达，所以脚本不下载任何东西。）

**D-54 · 狂野苹果转换配方双向**
`minecraft:golden_apple` ⇄ `farmstorm:wild_apple`，经验成本 3，无催化剂。与 v0.0.1 那 13 条双向配方同形（`ConverterRecipe`，`bidirectional=true`）。

**D-55 · 幸运浆果随机池定死 16 种**
`LUCKY_BERRY_POOL` = 19 条档案中排除 `lucky_berry` 与 `wild_apple`。排除狂野苹果的理由：它是本版最强道具，不该由 1 颗浆果随机产出。该池从「`ALL` 减去幸运浆果」的自动推导改为**显式排除两人**。

**D-56 · 压缩膨胀麦进档案表**
`compressed_puffed_wheat` 是第 19 条 `CropProfile`，类型 `THROWABLE`、类别 `CONSTRUCTION`，但 `cropBlock` / `seedItem` / `kind` 全空，且**不进转换器配方、不进幸运浆果池**。它属于「二级合成物」，不是「改良农作物」。

**D-57 · 压缩干草块数值**
`BlockBehaviour.Properties.of().strength(30.0F, 6.0F).requiresCorrectToolForDrops()`。六面方块、不可旋转（不用 pillar/axis）。材质 = 原版干草块侧面的灰度版。
依据实测：`ANCIENT_DEBRIS` 是 `strength(30.0F, 1200.0F)`，`HAY_BLOCK` 是 `strength(0.5F)`（同一对数字同时管硬度与抗性）。
取 6.0 的后果（**必须写进设计文档**）：瓜！与瓜片子（阈值 6.0）**破不动**它（判定是 `resistance < maxResistance`，6.0 不小于 6.0）；育碧服务器与毒育碧服务器（阈值 3.6e6）**破得动**；**原版爆炸（含 TNT）也破得动**——原版公式逐格累积 `f -= (resistance + 0.3) * 0.3`、`f > 0` 即破，TNT 威力 4 时 f 起始区间 [2.8, 5.2]，6.0 抗性挡不住圆石级别的方块。

**D-58 · 莫洛托夫落点语义**
落点触发一次**威力 0** 的爆炸，该爆炸**只当落点触发器**：0 实体伤害、0 方块破坏（原版 `Explosion` 半径 0 时伤害公式得 Infinity，天然无伤）。真正的效果是：以落点为球心、半径 4 格内**每个上表面接触空气的方块**独立掷一次，85% 在其**上方那个空气位**放火，15% 在其**上方那个空气位**放岩浆块。都不替换、不破坏原方块。
依据：破坏权限只由破障类决定（**D-48** 铁律；注意不是 D-19，D-19 管的是抗性阈值），而莫洛托夫的类别是「伤害类、建造类」，没有破障标签。

**D-59 · 桑吉尔蔗的「上下倒转」= roll + pitch 叠加**
画面绕视线轴滚 180°（roll = 180）**且** pitch 取反，两者同时生效。只做其中一个是错的。

**D-60 · 键鼠锁定范围**
锁：移动键、跳跃、攻击、使用/放置、快捷栏切换（1–9 与滚轮）、打开背包（E）、聊天（T / 斜杠）。
**只留 ESC**。依据：锁死 ESC 会让玩家以为游戏卡死。

**D-61 · 黑白含 GUI**
灰度后处理作用于整个世界渲染**与** GUI 层，是整屏灰度，不是只褪世界。

**D-62 · 红色高亮只画可见的**
只给**视线内可见**（无方块遮挡）且距离 **≤ 64 格**的生物画红色剪影。排除自己与同队伍玩家。不穿墙。

**D-63 · 新增 3 个进度**
`first_molotov`（初次投出莫洛托夫）、`zangief_hit`（桑吉尔蔗首次命中）、`first_wild_hunter`（首次获得狂野猎人效果）。既有 12 个不动。

### 三个侵入性效果

**D-64 · 狂野猎人索敌口径**
候选 = 以玩家为球心 **5 格**内、与玩家视线方向夹角 **≤ 30°** 的 `LivingEntity`，排除自己、同队伍玩家、旁观者。
排序：夹角最小者优先；夹角相同取距离近者。
「画面中最靠近准星」即「与视线夹角最小」，服务端算得出来，不需要读客户端鼠标位置。

**D-65 · 吸附用冲刺，不用瞬移**
服务端按当前距离算出一个初速，让玩家在 **3 刻**内跨到位；速度以 `setDeltaMovement` 下发，**客户端照常模拟移动与碰撞**。撞墙即停、不补位、不穿透。落点被方块占据时该次冲刺直接失败（本次不吸附）。

**D-66 · 音频打包与出处**
三个源文件转换后打包进 jar。README 与 Release 说明里**必须**注明来源与非商业自用：
- `Animals.mp3` → `wild_hunter` 循环音
- SF6 片段 `BV1qu411W7ha_846-850.mp3` → 桑吉尔蔗触发音
- 桌面 JPG → 狂野猎人效果图标

**D-67 · 压缩膨胀麦配方无序**
无序合成：任意位置放 **4 个** `farmstorm:puffed_wheat` → **1 个** `farmstorm:compressed_puffed_wheat`。

**D-68 · 桑吉尔蔗世界隐形**
部署方块走 v0.0.1 的陷阱类口径（D-48）：世界里**完全隐形**（空模型、无 elements），只保留 `textures.particle`；物品图标独立走 `item/generated`。

**D-69 · 桑吉尔蔗的漂浮等级**
「漂浮 7 效果 1 秒」= `MobEffects.LEVITATION`，amplifier **6**（= VII 级），持续 **20 刻**。在触发后第 **14 刻**施加。

**D-70 · 水平转速**
落地前持续 yaw **每刻 +72°**（1440°/秒 ÷ 20 刻/秒 = 72°/刻；40 刻累计 2880° = 8 整圈），方向为逆时针（yaw 递增）。

**D-71 · 桑吉尔蔗只对玩家生效**
触发者只认**非己方玩家**，不含生物。创造模式玩家与旁观者不触发（与 v0.0.1 三处陷阱的 `isCreative()` / `isSpectator()` 豁免一致）。

**D-72 · 触发后静默消失**
一次性陷阱，触发后方块**静默移除、不产生掉落物**（先例：胡罗贝 D-46）。

**D-73 · 触发半径是球形**
1.5 格按**球形欧氏距离**判定（不是切比雪夫）。

**D-74 · 击杀续时无上限**
狂野猎人期间每击杀一个生物：剩余时长 **+8 秒**、回复 **10 点生命**。没有次数上限（原文未写上限，不擅自加）。

**D-75 · 击退用原版默认强度**
「若成功造成伤害则击退目标」= 调原版 `LivingEntity#knockback` 的默认强度，不自订数值。

**D-76 · 循环音用客户端 SoundInstance**
Animals 由客户端一个循环 `SoundInstance` 持续播放，音源跟随玩家，衰减距离 16 格，声音分类 `SoundSource.PLAYERS`（不占用 `MUSIC`，不打断原版背景音乐）。服务端不每刻重发播放指令。

**D-77 · 压缩膨胀麦沿用膨胀麦的落点写入，不是「沿用爆炸」**

先纠正一处事实：**膨胀麦没有爆炸段**。`src/main/java/com/dashpig/farmstorm/entity/thrown/PuffedWheatProjectile.java` 只做两件事——
① 以命中方块为中心把立方体范围替换成 `minecraft:hay_block`（干态边长 3；若范围内存在 `#minecraft:water` 则边长 5）；
② 跳过爆炸抗性 ≥ 3.6e6 的方块（`BlastRule.UNBREAKABLE_RESISTANCE`）。它没有触发任何 `Explosion`，因此不存在「爆炸威力」与「伤害上限」。

所以 `compressed_puffed_wheat` 沿用膨胀麦的是这两条，**只把写入的方块从 `minecraft:hay_block` 换成 `farmstorm:compressed_hay_block`**：
- 立方体写入范围：干态边长 3、遇水边长 5，覆盖空气/流体/实体方块一律直接写入，原方块不产生掉落；
- 抗性底线：目标位置原方块抗性 ≥ 3.6e6 时跳过不写。

**不许**在 v0.0.2 文档里写「爆炸威力与伤害上限沿用膨胀麦」——那是无中生有。若以后真要给它加爆炸，属于改玩法，先改本条。

依据：`plan0.0.2.txt` 第 5 条原文只说「其属性与膨胀麦相同，但是生成的方块从干草块变为压缩干草块」，没有提爆炸。

---|---|---|---|---|
| `UbiServerProjectile` | `ubi_server` | `UNBREAKABLE_SKIP` | 3.6e6 | **破** |
| `ToxicUbiServerProjectile` | `toxic_ubi_server` | `VANILLA` | 3.6e6 | **破** |
| `MelonGrenadeProjectile` | `melon_grenade` | `grenade()` | 6.0 | 不破（`6.0 < 6.0` 为假） |
| `MelonSliceProjectile` | `melon_slice_bomb` | `sliceBomb()` | 6.0 | 不破（同上） |
| 原版爆炸（TNT 等） | — | 原版 `Explosion` | 逐格累积公式 | **破**（见 D-57 依据） |

v0.0.1 文档按 D-49 冻结不改，本条只登记在 v0.0.2 文档内。

---

## 3. 注册对象增量全量表

### 3.1 `CropProfile` 新增 4 条（15 → 19）

| # | registry id | 类型 | 类别 | cropBlock | seed | DeployedKind | zh 名 | en 名 |
|---|---|---|---|---|---|---|---|---|
| 16 | `molotov` | THROWABLE | DAMAGE, CONSTRUCTION | null | null | null | 莫洛托夫！ | Molotov |
| 17 | `zangief_cane` | DEPLOYABLE | TRAP | null | null | `ZANGIEF_CANE` | 桑吉尔蔗 | Zangief Cane |
| 18 | `wild_apple` | EDIBLE | BUFF | null | null | null | 狂野苹果 | Wild Apple |
| 19 | `compressed_puffed_wheat` | THROWABLE | CONSTRUCTION | null | null | null | 压缩膨胀麦 | Compressed Puffed Wheat |

英文显示名沿用 C-10（= registry id 的 Title Case）。中文名以作者原文为准，**保留原文里的感叹号**。

### 3.2 其余增量

| 注册类型 | 增量 | 明细 |
|---|---|---|
| items | **+5** | `molotov`、`zangief_cane`、`wild_apple`、`compressed_puffed_wheat`、`compressed_hay_block`(BlockItem) |
| blocks | **+2** | `compressed_hay_block`(普通 Block)、`zangief_cane`(DeployedCropBlock，复用既有 BE) |
| block_entities | **0** | 复用 `DeployedCropBlockEntity` |
| entity_types | **+2** | `molotov`、`compressed_puffed_wheat`（都继承 `ThrownCropEntity`） |
| mob_effects | **+2** | `wild_hunter`（可见，带图标）、`zangief_piledriver`（隐藏，纯同步通道） |
| sound_events | **+2** | `wild_hunter_loop`、`zangief_piledriver` |
| recipes | **+5** | 转换器合成 1 + 压缩膨胀麦合成 1 + 转换器转换 3（`molotov`/`zangief_cane`/`wild_apple`） |
| item tags（新建） | **+1** | `farmstorm:converter_fuel` |
| vanilla tags（追加） | **2 处** | `minecraft:mineable/pickaxe` += `compressed_hay_block`；`minecraft:needs_diamond_tool` += `compressed_hay_block` |
| advancements | **+3** | `first_molotov`、`zangief_hit`、`first_wild_hunter` |
| post_effect | **+1** | `assets/farmstorm/post_effect/grayscale.json` + 2 个 GLSL |
| damage_types | **+0** | 莫洛托夫无伤害 |
| data components | **+0** | — |
| recipe type / serializer | **+0** | 复用 `ConverterRecipe` |
| creative tabs | **+0** | 追加 5 个物品进既有标签页 |
| menu types | **+0** | — |

`DeployedKind` 枚举 +1 值：`ZANGIEF_CANE(10)`（scanInterval 10，与另两个陷阱一致）。

### 3.3 允许修改的**既有**文件（除此之外一律不许动）

`CropProfile.java`、`DeployedKind.java`、`FarmstormItems.java`、`FarmstormBlocks.java`、`FarmstormEntityTypes.java`、`FarmstormCreativeTabs.java`、`Farmstorm.java`、`FarmstormClient.java`、`FarmstormConfig.java`、`ModRecipeProvider.java`、`ModItemModelProvider.java`、`ModBlockStateProvider.java`、`ModLootTableProvider.java`、`ModLangProvider.java`、`ModAdvancementProvider.java`、`ModBlockTagsProvider.java`、`ModItemTagsProvider.java`、`gradle.properties`、`FarmstormTags.java`。

`behavior/DeployedBehaviors.java`、`registry/FarmstormBlockEntities.java`、`entity/thrown/PuffedWheatProjectile.java` 也在白名单内：前两个是穷尽 `switch`（加枚举值不改就编译不过），第三个的 `fillHay` 写死了 `Blocks.HAY_BLOCK`。

### 3.4 禁止动的

- 15 条既有 `CropProfile` 的**任何字段**
- 任何既有 config 键的**默认值**
- 既有 21 条转换配方
- 既有 12 个进度
- `BlastRule.canBreak()` 的比较逻辑（`resistance < maxResistance`）
- v0.0.1 的 10 份中文档（00–09）与 8 份英文档（00–06、09）

---

## 4. 数字口径表（唯一允许引用的数字）

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

**启动自检文案**（`Farmstorm#logSelfCheck` 会打印，改了数字要同步改）：

```
自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK
```

### 4.1 已实测的 v0.0.1 存量数字（照抄，不许自己再测一遍报别的数）

| 指标 | 实测值 | 测法 |
|---|---|---|
| `FarmstormConfig` 配置键 | **117** | `defineInRange` 111 + `define` 4 + `defineInList` 2 |
| 语言文件 `farmstorm.configuration.*` 键 | **117** | 与上一行一一对应；两数不等就说明有 key 漏生成 |
| 资源文件真实总数 | **262** | 169 JSON + 91 PNG + 1 TOML + 1 `pack.mcmeta` |
| ├─ `.cache` 目录 | **11 个无扩展名文件，不计入** | DataGen 缓存，不进 jar |
| 语言文件键总数 | **222**（zh_cn 与 en_us 完全一致） | 中英键集必须始终相等 |
| ├─ 其中 `tooltip.farmstorm.*` | **16** | |

### 4.2 由 12-TECH-SPEC 产出、且必须自洽的三个数

这三项**不是去测**（v0.0.2 还没实现），而是**逐项列全再数**：

- v0.0.2 新增 config 键：先把每个键名 / 默认值 / 范围 / 所属分类列成表，表行数就是新增键数，再加 117 得新总数；且「新增的 `farmstorm.configuration.*` 语言键数」必须等于这张表行数
- v0.0.2 新增资源文件：先把每个新文件的类目 / 路径 / 用途列成表，表行数就是新增数，再加 262 得新总数
- v0.0.2 语言文件键：新增键（物品名、方块名、tooltip、进度、config 说明）逐条列出，行数加到 222 上；中英两份必须始终相等

**自洽性硬要求**：每张清单的行数必须等于文中写出的计数。数不对就是错。

---

### 4.3 工作量口径（唯一允许引用的排期数字）

出处 `13-TASKS-0.0.2.md` §2 / §11；**三条算法互为校验**，改任一值必须同步三处：

| Phase | 归属 | 任务数 | 任务区间 | 裸工作量（人日） | 里程碑 |
|---|---|---|---|---|---|
| 11 | 基座与注册增量 | 7 | F-60 ~ F-66 | 11.0 | M7 |
| 12 | 压缩物与莫洛托夫！ | 9 | F-67 ~ F-75 | 10.5 | M8 |
| 13 | 狂 野 猎 人 | 10 | F-76 ~ F-85 | 16.0 | M9 |
| 14 | 桑吉尔蔗 | 8 | F-86 ~ F-93 | 11.5 | M10 |
| 15 | 资源、音频、进度与验收 | 6 | F-94 ~ F-99 | 11.5 | M11 |
| **合计** | | **40** | **F-60 ~ F-99** | **60.5** | **M7 ~ M11** |

- 按档位折算：**S 10 个 / M 23 个 / L 7 个** → `10×0.5 + 23×1.5 + 7×3.0 = 60.5`
- 含 25% 缓冲：`60.5 × 1.25 = 75.625` → 记 **75.6 人日**
- 关键路径：**14.5 人日 / 9 个任务**（`F-60 → F-62 → F-63 → F-86 → F-87 → F-88 → F-92 → F-93 → F-99`），占总量 **24%**
- 3 路并行约 **20.2 人日**（缓冲后 25.2）；单人串行约 15 周

> 反过来读这条：**工期不是被最长链卡住的，而是被总工作量与并行度卡住的**。想缩短工期优先加并行，不要压关键路径。

## 5. 文档分工

| 文件 | 负责内容 | 主要读什么 |
|---|---|---|
| `docs/zh/10-DECISIONS-0.0.2.md` | D-49…D-78 逐条成行；对 v0.0.1 文档的勘误节；19 条档案总表（增量）；明确不做的事 | 本文 §2 §3 `00-DECISIONS.md` |
| `docs/zh/11-DESIGN-0.0.2.md` | 7 个新功能的完整设计规格：数值表、状态机、边界与异常、config 引用 | `01-DESIGN.md` 文风 + 本文 |
| `docs/zh/12-TECH-0.0.2.md` | 新增注册对象逐项表、类设计、资源清单、三个客户端子系统的技术方案、风险登记册、config 全量新增键、验收自检 | `02-TECH-SPEC.md` 文风 + 实际源码 |
| `docs/zh/13-TASKS-0.0.2.md` | 任务拆分 F-60 起 + 依赖图 + 大纲（Milestone）+ 工作量 + 关键路径 | `03-TASKS.md` `04-ROADMAP.md` 文风 |
| `docs/zh/14-PROMPT-MAIN-0.0.2.md` | 一份主提示词 | `05-PROMPT-MAIN.md` 文风 |
| `docs/zh/15-PROMPT-TASKS-0.0.2.md` | 一份分阶段任务清单 | `06-PROMPT-TASKS.md` 文风 |

英文版一一对应落在 `docs/en/`，术语以 C-10 与 §3.1 的英文名为准。

---

## 6. 风格约定

- 全中文文档用简体中文，**不用**「您」，不用营销腔，直接陈述。
- 每条结论后面给依据（DEC 编号 / 源码路径 / 实测命令），不给依据的判断要标「本文补充」。
- 数值一律给单位与范围（例如「4.0 格，config 范围 0.0–16.0」）。
- 表格优于段落；超过 5 行的枚举一律用表格或代码块。
- 标题层级：`#` 一级唯一，`##` 章节，`###` 小节。与 v0.0.1 文档一致。
- 引用既有文档写 `01-DESIGN.md §3.4` 这种带文件名的形式，不要只写 `§3.4`。
- 禁止在文档里写「待定」「TODO」「视情况而定」。拿不准就按 §2 的决策推导，并在该处标注「本文补充：…」。

---

## 7. 三个客户端子系统的技术方向

> 这一节给的是**首选方案**与**风险编号**。12-TECH-SPEC 必须照此展开成类设计，可以补充实现细节，但
> ① 首选方案不许换；② 风险编号 RT-10…RT-16 不许重排、不许合并。
> 每条都标了「待验证」——那是实现阶段要实测的，不是文档阶段要回避的。

### 7.1 灰度（D-61，整屏含 GUI）→ RT-10

- **首选**：`RegisterShadersEvent` 里注册自定义 `ShaderInstance`（`assets/farmstorm/shaders/core/grayscale.fsh` + `grayscale.json`），
  在 `RenderGuiEvent.Post` 里对 `Minecraft#getMainRenderTarget()` 跑一次全屏 pass。
  选 `RenderGuiEvent.Post` 的理由：它在 GUI 之后、上屏之前，一次 pass 同时覆盖世界与 GUI，正好满足 D-61。
- **待验证**：`RenderGuiEvent.Post` 期间切换 render target 是否与其它模组的 GUI 后处理打架；如不可行，
  退化为「`RenderLevelStageEvent.AFTER_`（世界）跑一次 + `RenderGuiEvent.Post` 只跑 GUI 区」。
- **禁止**：用雾色 / 贴图替换冒充灰度。D-61 要的是真灰度。

### 7.2 红色剪影（D-62，可见 + ≤64 格）→ RT-11

- **首选**：订阅 `RenderLivingEvent.Post`，取 `LivingEntityRenderer#getModel()`，用 `Model#renderToBuffer`
  以 `RenderType.entityTranslucentEmissive(whiteTexture)` 再画一遍，配 `RenderSystem.setShaderColor(1, 0.15, 0.15, 0.45)`。
  白色 1×1 贴图放 `assets/farmstorm/textures/misc/white.png`。
- **禁止**：在 `RenderLivingEvent` 里调 `EntityRenderDispatcher#render` 重绘整只实体——会递归触发自身事件。
  也禁止用原版发光效果替代（那样是白色描边且全服可见，与 D-62 不符）。
- **可见性**：`player.hasLineOfSight(entity)` + 距离 ≤ 64。

### 7.3 输入锁（D-60，只留 ESC）→ RT-13

五个事件各管一段，缺一个就会漏：

| 要锁的 | 用哪个事件 |
|---|---|
| 移动 / 跳跃 / 潜行 | `MovementInputUpdateEvent`，把 `forwardImpulse` / `leftImpulse` / `jumping` / `shiftKeyDown` 清零 |
| 攻击 / 使用 | `InputEvent.InteractionKeyMappingTriggered`，`setCanceled(true)` |
| 快捷栏 1–9 | `InputEvent.Key`，放行 `GLFW_KEY_ESCAPE`，其余取消 |
| 滚轮切栏 | `InputEvent.MouseScrollingEvent`，取消 |
| 背包 / 聊天 | `ScreenEvent.Opening`，只放行 ESC 路径（`PauseScreen`） |

- **待验证**：`InputEvent.Key` 与 `KeyMapping` 的先后顺序决定了 1–9 是否真能拦住，需实测。

### 7.4 相机（D-59 / D-70）→ 无风险编号

- `ViewportEvent.ComputeCameraAngles`：`setRoll(180f)` + `setPitch(-pitch)` + `setYaw(yaw + accumulated)`。
- **铁律**：只改**渲染层**角度，**不写回** `player.getYRot()/getXRot()`。玩家真实朝向不变，服务端权威不破。
- yaw 自转在客户端按 tick 累加（每刻 +72°，D-70），不依赖服务端逐刻同步。

### 7.5 桑吉尔蔗的相位同步（D-50）→ RT-15

- 隐藏 `MobEffect` `farmstorm:zangief_piledriver`，**amplifier 当阶段号**：
  `0` = 已倒转、等待漂浮（14 刻）；`1` = 漂浮中；`2` = 下坠自转。
- 服务端在相位切换时**重新 apply 同 id、不同 amplifier** 的效果，`ClientboundUpdateMobEffectPacket` 免费同步。
- 「向下的速度」走 `setDeltaMovement`，`ServerPlayer` 会经 `ClientboundSetEntityMotionPacket` 广播，不需要自定义包。

### 7.6 狂野猎人冲刺（D-65）→ RT-12

- **首选**：**客户端预测位移 + 服务端判定**。MC 里玩家位置本来就是客户端权威（服务端只做速度校验），
  客户端在「本地按下攻击键 + 自己持有 `wild_hunter` + 本地算出合法目标」时立刻朝目标方向给速度，
  服务端在收到 `ServerboundSwingPacket` 时用同一套确定性算法独立算一遍目标并结算伤害与击退。
- **待验证**：`ServerPlayer#setDeltaMovement` 并不会让客户端玩家移动（玩家移动是客户端模拟的），
  所以「服务端给速度」这一句在实现上必须落到**客户端驱动**。红线：不许为了这个新增网络包（D-50）。
- **备选**：服务端逐刻小步 `teleportTo`（每刻 ≤ 1 格、带碰撞检测）。若首选实测手感不可接受再切换，
  但切换必须在 12-TECH 里留决策记录。

### 7.7 音频（D-53 / D-66 / D-76）→ RT-14

- `assets/farmstorm/sounds.json` 两个事件：`wild_hunter_loop`（循环）、`zangief_piledriver`（单次）。
- 循环音用自写 `SoundInstance` 子类：`isLooping() = true`，每 tick 把位置更新到玩家身上，
  可听距离 16 格，声音分类 `SoundSource.PLAYERS`。
- **待验证**：16 格衰减在 1.21.1 上到底该压 `SoundInstance#getVolume` 还是它的可听距离钩子，需实测确认。

### 7.8 新增风险登记册（编号固定）

| 编号 | 风险 | 首要缓释 |
|---|---|---|
| RT-10 | 整屏灰度后处理的挂载点不成立 | 拆成世界 + GUI 两次 pass；再不行只灰世界并在 Release 说明里降级登记 |
| RT-11 | 剪影重绘递归或掉帧 | 用 `getModel().renderToBuffer` 而非 dispatcher 重绘；≤64 格 + 视线才画 |
| RT-12 | 冲刺位移的驱动方判错，玩家不动或被拉回 | 走客户端预测 + 服务端判定；备选逐刻 teleport |
| RT-13 | 输入拦截有遗漏，某个键仍能穿透 | 五个事件全上；实现阶段逐键手测一遍并留记录 |
| RT-14 | ffmpeg 不在 PATH 上，或换机器 / 换构建后拿不到带 libvorbis 的 ffmpeg，音频产不出来 | 转换脚本缺 ffmpeg 或 ffmpeg 无 libvorbis 直接报错退出；工具已就位（`tools/convert_media.ps1` 实测通过）；音频任务排在资源阶段最前，早失败早发现 |
| RT-15 | 隐藏 MobEffect 被其它模组的效果清理逻辑干扰 | 客户端每 tick 自检「阶段效果还在不在」，不在就自己复位 |
| RT-16 | 整屏灰度与光影类模组（Oculus/Iris 等）不兼容 | 文档登记为已知不兼容，不做运行时检测 |


---

## 8. config 新增键唯一清单（22 条）与跨文档裁决

> 早期 11-DESIGN 与 12-TECH 各自数出 23 与 20 个新键，键名与机制也不一致；经载定后两份现均为 22 行。**下表是唯一清单**，
> 两份文档都必须照抄本表、不许各自再数一遍。裁定记录 A-01…A-05 见 §8.1。

| # | 键（`<group>.<key>`） | 类型 | 默认值 | 范围 | 用途 | 依据 |
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

**合计 22 条。** 以 v0.0.1 的 117 条为基数：`117 + 22 = 139`。

语言键：`222 + 17（5 物品 + 2 方块 + 2 效果 + 2 字幕 + 6 进度）+ 22（config 说明）= 261`，中英两份必须相等。

### 8.1 跨文档裁决

| 编号 | 冲突 | 裁定 | 理由 |
|---|---|---|---|
| A-01 | 11-DESIGN 用 `fireChance` + `magmaChance` 两个键；12-TECH 只用 `fireChance`，余数自动归岩浆块 | **采 12-TECH：只留 `fireChance`** | 两个独立百分比会出现和 ≠ 100 的非法配置，还得再定义归一规则 |
| A-02 | 11-DESIGN 用 `wild_apple.hunterDurationTicks`；12-TECH 用 `wild_hunter.durationTicks` | **采 12-TECH：时长归效果，苹果不单设** | 狂野苹果只是授予该效果，时长只能有一个来源 |
| A-03 | 11-DESIGN 用 `fallTicks` 逐刻伺服速度；12-TECH 用 `dropVelocity` 定速 | **采 11-DESIGN：`fallTicks` + 逐刻伺服** | plan 的约束是「1.7–2 秒内落地」这个**时间**；定速无法保证，高度一变就不成立 |
| A-04 | 11-DESIGN 键名写 `farmstorm.<group>.<key>`；12-TECH 写 `<group>.<key>`，语言键再加前缀 | **采 12-TECH：正文写 `<group>.<key>`，语言键写 `farmstorm.configuration.<group>.<key>`** | 与 v0.0.1 的 117 条完全同构 |
| A-05 | `spinDegreesPerTick` 默认值：BRIEF 旧文写 24，同一句的算式给 72 | **72.0**（72 × 20 = 1440 度/秒） | plan 原文是「1440 度每秒」，这是硬需求；旧文的 24 是我算错 |
| A-06 | 压缩干草块取抗性 6.0 后，谁能破它 | **瓜！与瓜片子破不动；育碧服务器、毒育碧服务器、原版爆炸（含 TNT）都破得动** | 判定式是 `resistance < maxResistance`，6.0 不小于 6.0；而原版爆炸走逐格累积公式，6.0 挡不住 TNT |
| A-07 | D-77 的“沿用膨胀麦”到底沿用什么 | **沿用“立方体写入范围（干态 3 / 遇水 5）+ 抗性 ≥ 3.6e6 跳过”，不是沿用爆炸** | 膨胀麦根本没有爆炸段，没有威力与伤害上限可沿用 |
| A-08 | 桑吉尔蔗触发后不掉落，但玩家自挖呢 | **触发消失走 `consume()` 不过战利品表；玩家自挖正常走战利品表掉自身 1 个** | 与 v0.0.1 五个部署方块一致；D-72 的“不掉落”只约束触发路径 |
| A-09 | 进度显示名用物品简介还是短句 | **用短句**：火了！/ 手摇重 360 / 狂野猎人 | v0.0.1 的 12 个进度名都是短句；三句简介属于 tooltip 命名空间 |

**自洽要求**：11-DESIGN 第 10.2 节与 12-TECH 第 8.1 节的清单必须与本表逐行一致（键名、类型、默认值、范围）。
12-TECH 的「清单行数 = 写出的计数」、语言键相等性、`117 + 22 = 139`、`222 + 39 = 261` 都要跟着改。

## 9. 术语与两个补充裁决（A-10、A-11）

### 9.1 农产品转换器的英文名：统一用 `Produce Converter`

实测三种并存（0.0.1 的既成事实，D-49 冻结不改）：

| 出处 | 写法 |
|---|---|
| `en_us.json` 的 `block.farmstorm.converter` / `item.farmstorm.converter` | `Converter` |
| `en_us.json` 的 `advancements.farmstorm.converter.description` | `Crop Converter` |
| `docs/en/01-DESIGN.md` | `Produce Converter`（5 次） |
| `docs/en/02-TECH-SPEC.md` | `Crop Converter`（2 次） |

**v0.0.2 的英文文档一律写 `Produce Converter`**（跟设计文档走，它是玩家真正读到的那份）。
语言文件里新增的键沿用 `Converter`（与既有 `block.farmstorm.converter` 一致），不新增 `Produce Converter` 这个显示名。
已冻结的 `docs/en/01` 与 `02` 不改（D-49）。

### 9.2 A-10 · `molotov.triggerExplosionPower` 与「爆炸不许有副作用」的关系

| 编号 | 冲突 | 裁定 | 理由 |
|---|---|---|---|
| A-10 | 一处写「落点爆炸不许有任何副作用」（D-58），另一处把 `molotov.triggerExplosionPower` 开放成 0–100（config 清单 #3） | **默认 0 时零副作用；调大后只增加对实体的伤害，永远不破坏方块** | 莫洛托夫的类别是「伤害类、建造类」，没有破障标签，所以 `BlastRule.forCrop` 恒返回 `NEVER_BREAK`——破坏权限与威力无关。把「零副作用」改成「默认零副作用 + 不破坏方块永不成立」两句，矛盾即消。**config 清单的默认值与范围不动**（仍 0 / 0–100） |
| A-11 | `狂 野 猎 人` 的空格数：文档一处说「两个空格」，实际印出来是 4 个字 3 个空格 | **以`plan0.0.2.txt` 印出来的为准：4 个字、字与字之间各一个空格，共 3 个空格** | 直接数原文，不靠描述 |