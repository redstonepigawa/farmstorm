# 18 · v0.0.3 技术规格（Technical Specification）

> 本文件是「农场风云 / Farmstorm」v0.0.3 的技术规格：注册增量、类设计、两条 Q弹 分支、三处机制方案、
> config 与资源清单、RT-17 ~ RT-23 风险册、验收自检表。
> 唯一真相源是 `docs/zh/16-DECISIONS-0.0.3.md`（D-80 ~ D-97 正文 + §3.6 的补充裁定 D-98 ~ D-102）；冲突时以该文件为准。
> 本文 §1.4 曾登记四处基线不一致，现已全部关闭：三处由 16 号 §3.6 的 D-98 与作者裁定订正，一处是本文对 D-87 家族计数的正确判读。
> 全部源码事实取自 `src/main/java/com/dashpig/farmstorm/`，行号以 v0.0.2 发版代码为准。

---

## 0. 本文范围与与 12-TECH-0.0.2.md 的关系

> 本节标题的「与与」是照 `12-TECH-0.0.2.md` §0 的原样转写（该文件也是这么写的），不改。

### 0.1 本文负责什么

| 项 | 内容 |
|---|---|
| 版本 | 0.0.3（增量版，构建基线不变：MC 1.21.1 / NeoForge / Java 21） |
| 负责 | 注册增量 +13 逐项点名；新增与改动类的设计；Q弹 坐姿两分支；混乱的反向输入；近防菌的拦截；震动豆的不依附方块；config 全量清单；资源与数据文件清单；RT-17 ~ RT-23；验收自检 |
| 不负责 | 数值平衡与状态机的完整叙述（`17-DESIGN-0.0.3.md`）；任务拆分与工作量（`19-TASKS-0.0.3.md`）；提示词（`20` / `21`） |
| 不动的文件 | `docs/zh` 的 00 – 15 与 `docs/en` 的 00 – 15（D-80 冻结；16 – 21 是本轮新增，不在冻结之列） |

### 0.2 与 `docs/zh/12-TECH-0.0.2.md` 的关系

`12-TECH-0.0.2.md` 建立的体例本文全部沿用，一处不新创：

| 体例 | 12 号的位置 | 本文的继承方式 |
|---|---|---|
| 注册对象逐项表 | §2.1 / §2.2 | §2 沿用「registry × v0.0.2 × 增量 × v0.0.3」的四列口径 |
| 逐类小计自洽 | §2.1 表下代码块 | §1.2 / §2.4 用同一写法，两条算法互为校验 |
| 类设计的多项 | §3 开头 | §3 改为四项（职责 / 关键方法 / 读哪些 config 键 / 改哪些既有文件），理由见 §3 开头 |
| config 逐项清单 | §8.1 | §8.1 沿用同一列集，行数先点数再用 |
| 资源清单 | §7.2 / §7.3 | §9 拆成 PNG / OGG / JSON 三张表，各自给分类小计 |
| 风险册 | §10 | §10 给 RT-17 ~ RT-23，字段改为五项（风险 / 触发 / 首要缓释 / 兜底 / 实现阶段怎么验） |
| 「待验证」登记 | §0.5 | §0.5 沿用，本文的待验证项逐条挂到某条 RT |
| 数值纪律 | §0.4 | §0.4 沿用，但本文**允许**写注册总数——因为它已经是实测值（§1） |

**与 12 号的关系一句话**：12 号是「在 0.0.1 之上加 13 个对象、把 64 变 77」的规格；本文是「在 0.0.2 之上再加 13 个对象、把 77 变 90」的规格，两者的加法方式逐字相同。区别只有一处：12 号能拿 64 当基线是因为 0.0.1 的档案齐；本文能拿 77 当基线是因为**源码里有一行启动自检断言了这 12 个数字**（`Farmstorm.java` L138–L141），本文的实测值与那行断言逐项相等（§1.3）。

### 0.3 三张清单的自洽承诺

本文的三张清单（§2 注册对象、§8 config 键、§9 资源文件）各自满足：

1. **行数 = 声明的计数**：每张表写「本表 N 行」，N 与表的数据行数逐行点数一致，不先写数再凑行。
2. **小计 = 逐行相加**：三张表都在表下给分类小计，小计之和等于表行数。
3. **跨表不重不漏**：一个对象只在一张表里出现一次。注册对象进 §2；config 键进 §8；贴图 / 音频 / JSON 进 §9。`DeployedKind` 的枚举常量不是注册对象，只在 §3 出现，不进任何计数。

### 0.4 数字口径

| 类别 | 口径 | 出处 |
|---|---|---|
| 已实测 | 注册对象 77 个（逐类见 §1.2） | 本文逐条读注册类源码数出，并与 `Farmstorm#logRegistrySelfCheck` 的 12 条断言逐项相等 |
| 已实测 | config 139 键 | `FarmstormConfig` 的 133 条 `defineInRange` + 4 条 `define` + 2 条 `defineEnum/defineList` = 139；`assets/farmstorm/lang/en_us.json` 的 `farmstorm.configuration.*` 也是 139 |
| 已实测 | 语言键 263 | `tools/verify_project.py` §3 的实际输出（中英各 263，键集一致） |
| 已实测 | 作物档案 19 条 / 幸运浆果池 16 条 | `util/CropProfile.java` 的 `ALL`（19 项）与 `LUCKY_BERRY_POOL`（19 − 3 项） |
| 增量 | +13（注册）/ +16（config 键）/ +34（语言键）/ +8 PNG / +1 OGG | 本文 §2 / §8 / §9 |
| 目标值 | 注册 90、config 155、语言键 297 | 实测值 + 增量，逐项列在 §2.4 / §8.3 / §9.4 |
| 禁止 | 任何未经本节两条证据支持的注册总数 | D-80 的数字纪律；本文 §1 已解除该禁令（工具缺口与实测值都已给出） |

### 0.5 「待验证」的登记规则

本文不发明任何 Minecraft / NeoForge API。凡是本文不能从既有源码里找到用法先例的调用，一律在该处标「**待验证**」，并在方括号里写明所挂的 RT 编号。本文共 9 处，全部集中在 §10 的四条 RT 上（RT-17 四、RT-18 二、RT-20 一、RT-22 二，相加 9）。标了待验证的调用在实现阶段必须先写一个最小用例跑通，再写正式逻辑；跑不通就换成该 RT 的「兜底」栏写明的替代路径。

---

## 1. 基线核对（现有注册对象实测 + verify_project.py 缺口）

### 1.1 计数方法

**不用 grep 计数。** `grep "register"` 会把工厂方法、注释、`DeferredRegister` 的字段声明一起数进去——本文明确避开这条路。采用的三条证据链互相独立：

1. **逐条读构造点**：打开每个 `registry/*.java`，只数「真正产生一个注册条目」的调用——`REGISTER.register(...)` / `REGISTER.registerItem(...)` / `REGISTER.registerBlock(...)` / `REGISTER.registerComponentType(...)` / `REGISTER.registerDataComponents(...)` 系列，以及 `cropItem(...)` / `seedItem(...)` / `crop(...)` / `deployed(...)` / `projectile(...)` 这些**工厂方法**的每一次调用点。工厂方法本身不是条目：`FarmstormEntityTypes.projectile(String, EntityTypeFactory)` 是一个方法，它被调用 10 次，所以是 10 个条目而不是 11 个。这一条是本计数方法的全部要点。
2. **对照启动自检**：`Farmstorm.java` 的 `logRegistrySelfCheck()`（L123–L147）在每次启动时把 12 个类分别数一遍——数的是 `DeferredRegister#getEntries().size()`，不是写死常量——并断言 `total == 77` 且逐类等于一组具体数字（L138–L141）。这是**运行期**的第二份独立账本。
3. **对照既有文档**：`12-TECH-0.0.2.md` §2.1 的 12 行表（v0.0.1 列 + 增量列 + v0.0.2 列）。

三条证据一致 ⇒ 基线成立。任一条不一致 ⇒ 记进 §1.4。

### 1.2 逐类实测（v0.0.2 基线，共 77）

本表 12 行，逐类给「数了什么」。第 4 列是本文实测值，第 5 列是 `12-TECH` §2.1 的 v0.0.2 列，第 6 列是 `Farmstorm#logRegistrySelfCheck` 的断言值——三列逐项相等。

| # | registry | 数了什么（逐条点名的依据） | 实测 | 12 号 | 启动自检 |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 19 个作物物品（15 个 0.0.1 + 4 个 0.0.2：`molotov` / `zangief_cane` / `wild_apple` / `compressed_puffed_wheat`）+ 6 个独立播种物 + 2 个方块物品（`converter` / `compressed_hay_block`） | **27** | 27 | 27 |
| 2 | `Registries.BLOCK` | 14 个作物方块（`crop(profile)` 调用 14 次）+ 6 个部署方块（`deployed(...)` 调用 6 次）+ `converter` + `compressed_hay_block` | **22** | 22 | 22 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 5 个部署类型（`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus`）+ `converter` | **6** | 6 | 6 |
| 4 | `Registries.ENTITY_TYPE` | 10 次 `projectile(id, factory)`（8 个 0.0.1 + `molotov` + `compressed_puffed_wheat`）+ `turret_bullet` | **11** | 11 | 11 |
| 5 | `Registries.MOB_EFFECT` | `wild_hunter` + `zangief_piledriver` | **2** | 2 | 2 |
| 6 | `Registries.SOUND_EVENT` | `wild_hunter_loop` + `zangief_piledriver` | **2** | 2 | 2 |
| 7 | `Registries.RECIPE_TYPE` | `converting` | **1** | 1 | 1 |
| 8 | `Registries.RECIPE_SERIALIZER` | `converting` | **1** | 1 | 1 |
| 9 | `Registries.CREATIVE_MODE_TAB` | `main` | **1** | 1 | 1 |
| 10 | `Registries.DAMAGE_TYPE` | 数据驱动，代码侧只有 `ResourceKey`：`void_bolt` + `crop_blast` | **2** | 2 | 2 |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `temporary_enchantments` | **1** | 1 | 1 |
| 12 | `Registries.MENU` | `converter` | **1** | 1 | 1 |
| | **合计** | | **77** | 77 | 77 |

逐类小计（用于核对，必须与上表逐行相等）：

`
items                 27
blocks                22
block_entities         6
entity_types          11
mob_effects            2
sound_events           2
recipe types           1
recipe serializers     1
creative tabs          1
damage_types           2
data_components        1
menu types             1
--------------------------------
相加 = 27+22+6+11+2+2+1+1+1+2+1+1 = 77
`

**核对结论**：逐类实测相加 = 77，`12-TECH` §2.1 的 v0.0.2 列也是 77，`Farmstorm#logRegistrySelfCheck` 的断言同样是 77。三条独立算法互为校验，**77 是本文的基线**。

### 1.3 交叉核对：19 / 16 / 22 与注册总数

| 被核对的数 | 文档声称 | 本文实测 | 结论 |
|---|---|---|---|
| `CropProfile` 档案条数 | `10-DECISIONS-0.0.2.md` §4 为 19 行；`16-DECISIONS-0.0.3.md` §4 说明「该表是 19 行」 | `CropProfile` 有 19 个 `public static final CropProfile` 常量，`ALL` 恰好 19 项 | 一致 |
| 幸运浆果随机池 | D-55 / D-97：19 − 3 = 16，v0.0.3 起 16 → 19 | `LUCKY_BERRY_POOL` = `ALL` 过滤三件 = 19 − 3 = 16 | 一致（v0.0.3 只增档案、不改推导式，故自动变 19） |
| 作物类型分布 | `Farmstorm` 自检断言 `throwable == 10 && deployable == 6 && edible == 3` | 19 = 10 + 6 + 3；破障类 `demolition == 4` | 一致 |
| 注册对象总数 | `12-TECH-0.0.2.md` §2 标题「77 对象核对」 | 77（§1.2） | 一致 |
| `sounds.json` 事件数 | `16-DECISIONS` §8：「2 → 3」 | `sounds.json` 里 2 个事件（`wild_hunter_loop` / `zangief_piledriver`） | 一致 |
| config 键数 | `16-DECISIONS` §1：139（已核实） | 133 + 4 + 2 = 139 条定义；`farmstorm.configuration.*` 语言键也是 139 | 一致 |
| 语言键数 | `16-DECISIONS` §1：263 | `verify_project.py` §3 输出 263（中英键集一致） | 一致 |

### 1.4 基线核对发现的四处不一致（**四条全部已关闭**）

**每一处都写明「哪边可能是错的」；同时给出实现期的判定，避免把不确定性留给后面。**

**状态表（2026-02 末次更新，四条都已定案，不留悬案）**：

| 条 | 事项 | 状态 | 定案依据 |
|---|---|---|---|
| (a) | `ThrownCropEntity` 家族 9 vs 10 | **已关闭** | 16 号 D-98 订正为 10 + 2 = **12**；「9」是 0.0.1 的 `entity_types` 总数（8 投掷物 + `turret_bullet`），与家族成员数不是一回事 |
| (b) | `DeployedKind` 新增值 +1 vs +2 | **已关闭，取 +2** | 作者裁定 +2；枚举 6 → **8**；枚举常量不是注册要素，注册总数不变 |
| (c) | `tools/verify_project.py` 全部按 0.0.1 枚举 | **已关闭，修** | 作者裁定：修到 0.0.2 基线 **77**、v0.0.3 之后 **90**；§11 #1 的判据不许降级为人工签字 |
| (d) | PNG 增量 +7 vs +8 | **已关闭，+8** | 作者确认事实卡已改为 +8，第 8 张是震动豆物品图标（0.0.2 三个隐形陷阱各带一张 item 贴图的惯例） |

#### (a) `ThrownCropEntity` 家族的成员数：文档 9，源码 10

- 文档：`16-DECISIONS-0.0.3.md` §3.3 D-87 原文写「只拦 ThrownCropEntity 家族（0.0.2 的 **9 种** + 0.0.3 新增的 2 种）」——该条原文至今一字未改（D-98 的体例是「保留原文、另立条目订正」），所以这里仍用现在时；订正见本块末行与 §3.6 的 D-98。
- 源码：`entity/ThrownCropEntity.java` 有 **9 个直接子类**（`BambooFirecrackerProjectile` / `BrownMushroomPuffProjectile` / `MelonGrenadeProjectile` / `MelonSliceProjectile` / `MolotovProjectile` / `RedMushroomPuffProjectile` / `PuffedWheatProjectile` / `ToxicUbiServerProjectile` / `UbiServerProjectile`），外加 **1 个间接子类** `CompressedPuffedWheatProjectile`（它继承 `PuffedWheatProjectile`，不是直接继承基类）。10 个 `entity_type` 全部由 `FarmstormEntityTypes.projectile(...)` 创建，而该工厂的泛型上界是 `<T extends ThrownCropEntity>`，因此 10 个都必然是家族成员。
- **哪边可能错**：文档的「9 种」按**直接子类**数的；D-87 的判据写的是 `instanceof`，`instanceof` 按**家族成员**算。两边各数各的对，但口径不同。
- **实现判定**：按 D-87 的 `instanceof` 口径取 **10**。v0.0.3 落地后家族成员 = 10 + 2 = **12**，§11 #8 / #13 都按 12 这条基数验。
- **关闭（D-98）**：`16-DECISIONS-0.0.3.md` §3.6 的 D-98 已把家族规模订正为 **12**，并逐件列出（清单见本文 §6.1 的 12 行表）；D-98 同时补回了 D-87 漏掉的「非己方」限定（§6.1 / §6.2）。

#### (b) `DeployedKind` 的新增值：文档 +1，源码约束要求 +2

- 文档：D-86 写「`behavior/DeployedKind.java`（枚举 +1 值 `POINT_DEFENSE_FUNGUS`）」（该条原文未改）；`16-DECISIONS` §4 第 22 行（震动豆）的「DeployedKind」列**当时写的是**「—」（无值），现已订正为 `VIBRATION_BEAN`。
- 源码：`blockentity/DeployedCropBlockEntity.java` L107–L113 的 `kindOf(BlockState)` 对**任何不是 `DeployedCropBlock` 的方块直接抛 `IllegalStateException`**，而构造器第一行就用它取 `BlockEntityType`（L103）。也就是说：D-91 要的「悬空陷阱方块 + 复用 `DeployedCropBlockEntity`」这条路径，**只能**是 `DeployedCropBlock`；而 `DeployedCropBlock` 的 `kind` 字段在构造器里从 `profile.kind()` 取（L74），取不到就回落到 `BLAST_CARROT`——那会让震动豆跑成胡罗贝的爆炸行为。
- **哪边可能错**：`16-DECISIONS` §4 第 22 行当时那格「DeployedKind —」。它很可能是在「震动豆是投掷型、没有种植形态」这个语境下填的「—」，但这一列在其它行表示的是**部署行为分派键**，不等于「有没有种植形态」；16 号现已按本节判定把该格改为有值（`VIBRATION_BEAN`）。
- **实现判定**：`DeployedKind` 实际 **+2 值**（`POINT_DEFENSE_FUNGUS(10)`、`VIBRATION_BEAN(10)`），`CropProfile.VIBRATION_BEAN` 的 `kind` 字段填 `VIBRATION_BEAN` 而不是 `null`。**这不影响任何注册计数**：枚举常量不是注册对象，`block_entities` 仍是 6（震动豆挂进既有 `BlockEntityType` 的合法方块集合，与 0.0.2 把 `zangief_cane` 挂进 `fang_trap_cactus` 那个 type 是同一手法）。**关闭（作者裁定，取 +2）**：`16-DECISIONS` §4 第 22 行**已订正为** `VIBRATION_BEAN`（§4 表下并补了「第 22 行的 DeployedKind 必填」的理由），事实卡已改；枚举 0.0.2 的 6 个 → **8** 个。枚举常量不是注册要素，§1.2 / §2 的任何数字都不受影响。RT-21 继续跟踪「方块与 BE 配错时抛异常」这条快速失败路径。

#### (c) `tools/verify_project.py`：分母 64 与它自己的枚举 62 不符，且全部按 0.0.1 枚举

运行结果（`python -X utf8 tools/verify_project.py`，退出码 1，FAIL 1 项）：

| 段 | 实际输出 | 判读 |
|---|---|---|
| §1 config | 文档口径 117 键、生成 toml 139 键、`[FAIL] toml 多 22 个键` | 错在**期望值来源**：工具读 `01-DESIGN.md` §3.8 的 117 键，而 0.0.2 新增的 22 键在 `12-TECH-0.0.2.md` §8.1（117 + 22 = 139，正好对上）。`01-DESIGN.md` 属 00–15 冻结区不能回改，所以修法只能是让工具多读一处 |
| §2 注册 | `items 22 / blocks 20 / block_entity_type 6 / entity_type 9`，`已核 registry 对象: 62 / 64` | 枚举是 0.0.1 的（22 + 20 + 6 + 9 = 57，加 5 个裸 ID 字面量 = 62）；分母 64 与它自己的 62 都不一致 |
| §3 语言 | `lang 键集一致，共 263 键` | 唯一一段仍然正确 |
| §4 禁令 | `无 Mixin / 调试残留 / 空 catch`（禁令扫描的四类一项未命中） | 通过 |

**哪边可能错**：工具的期望值与分母。实现侧没错——工具的 §3 段自己数出 263 且与实现一致。

**关闭（作者裁定，修）**：按下表的四段改法执行，目标是 0.0.2 基线 **77 / 77** 与 `结论：FAIL 0 项`；v0.0.3 落地后再收到 **90 / 90**、config **155**、lang **297**。§11 #1 的判据就是这条输出，**不许降级成人工签字**。

**修到 0.0.2 基线的目标**（作为 §11 #1 的验收项）：

| 段 | 现在 | 修成 |
|---|---|---|
| §1 期望键集 | 117（只读 `01-DESIGN.md` §3.8） | 139（117 + `12-TECH-0.0.2.md` §8.1 的 22 行） |
| §2 `EXPECTED` | items 22 / blocks 20 / entity 9 | items 27 / blocks 22 / entity 11（补 `compressed_hay_block`、`zangief_cane`、`molotov`、`compressed_puffed_wheat`） |
| §2 分母 | 64 | 77，且逐类小计与 §1.2 相等（12 类，见 §2.1） |
| §2 裸 ID 字面量 | 5 个（`converting` / `main` / `void_bolt` / `crop_blast` / `temporary_enchantments`） | 补齐剩余 5 个（含 recipe serializer 与 menu），使 12 类全部走「逐类小计」而不是裸字面量 |
| §3 语言 | 263 | 保持不变（v0.0.3 落地后改为 297，见 §9.4） |

#### (d) PNG 增量：8 与「分项相加」的缺口

`16-DECISIONS-0.0.3.md` §8 **曾记**「+7 / +1（PNG 与 OGG 的增量）」，已订正为 +8 / +1。逐项列全之后本文得出 **PNG 8 张**，比 7 多一张：第 8 张是**震动豆的物品图标**。理由是源码约束而不是偏好——`datagen/ModItemModelProvider.java` L46–L48 对没有原版父模型的物品一律走 `basicItem(item)` 的兜底分支，而 `basicItem` 生成的模型固定引用 `farmstorm:item/<id>`；0.0.2 的三个隐形陷阱（`blast_carrot` / `fang_trap_cactus` / `zangief_cane`）正是因此各自带一张 `textures/item/*.png`（仓库里实有这三个文件）。震动豆是第四个隐形陷阱，同一处置。近防菌不同：它是炮台类，方块与物品都借原版 `warped_fungus`（D-86 的对应物就是诡异菌），所以 0 张新贴图。逐项清单见 §9.1。

**关闭（作者确认，+8）**：事实卡已由 +7 改为 **+8**，并注明第 8 张是震动豆的物品图标。本文 §9.1 的 8 行就是这一口径的逐项展开（1 + 3 + 4 = 8）。

---

## 2. 增量注册对象总表

### 2.1 逐 registry 核对表

统计口径与 `12-TECH-0.0.2.md` §2.1 逐字相同：**registry 对象计数**；`recipe` / `advancement` / `tag` / `damage_type 的 JSON` 是数据文件，不进注册计数（§2.3 单列）。

本表 12 行。

| # | registry | v0.0.2 | 增量 | v0.0.3 | 增量明细 |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 27 | **+4** | **31** | `friend_chives`、`chive_seeds`、`point_defense_fungus`、`vibration_bean` |
| 2 | `Registries.BLOCK` | 22 | **+3** | **25** | `friend_chives_crop`、`point_defense_fungus`、`vibration_bean` |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 6 | +0 | **6** | 两个新方块挂进既有 type 的合法方块集合（§2.2 注 3） |
| 4 | `Registries.ENTITY_TYPE` | 11 | **+2** | **13** | `friend_chives`、`vibration_bean` |
| 5 | `Registries.MOB_EFFECT` | 2 | **+2** | **4** | `q_bounce`、`confusion` |
| 6 | `Registries.SOUND_EVENT` | 2 | **+1** | **3** | `friend_chives` |
| 7 | `Registries.RECIPE_TYPE` | 1 | +0 | **1** | 复用 `farmstorm:converting` |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | +0 | **1** | 复用 `ConverterRecipeSerializer` |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | +0 | **1** | 4 个新物品追加进 `farmstorm:main` |
| 10 | `Registries.DAMAGE_TYPE` | 2 | **+1** | **3** | `zangief_slam` |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | +0 | **1** | — |
| 12 | `Registries.MENU` | 1 | +0 | **1** | — |
| | **合计** | **77** | **+13** | **90** | |

### 2.2 新增的 13 个对象逐个点名

每条给完整注册 ID 与承载类。**13 行**。

| # | registry | 完整 ID | 承载类 / 形态 | 依据 |
|---|---|---|---|---|
| 1 | items | `farmstorm:friend_chives` | `ThrowableCropItem`（`CropProfile.FRIEND_CHIVES`） | D-84、D-85 |
| 2 | items | `farmstorm:chive_seeds` | `SeedItem`（`CropProfile.FRIEND_CHIVES` 的播种物） | D-84 |
| 3 | items | `farmstorm:point_defense_fungus` | `DeployableCropItem`（`extends BlockItem`） | D-86 |
| 4 | items | `farmstorm:vibration_bean` | `ThrowableCropItem`（**不是** BlockItem，见注 2） | D-90 |
| 5 | blocks | `farmstorm:friend_chives_crop` | `FarmstormCropBlock`（4 个 age 的种植形态） | D-84 |
| 6 | blocks | `farmstorm:point_defense_fungus` | `DeployedCropBlock`（炮台类外观，不隐形） | D-86 |
| 7 | blocks | `farmstorm:vibration_bean` | `DeployedCropBlock`（隐形空模型 + 不依附模式） | D-91 |
| 8 | entity_types | `farmstorm:friend_chives` | `FriendChivesProjectile`（`extends ThrownCropEntity`） | D-85 |
| 9 | entity_types | `farmstorm:vibration_bean` | `VibrationBeanProjectile`（`extends ThrownCropEntity`） | D-92 |
| 10 | mob_effects | `farmstorm:q_bounce` | `QbounceEffect`（可见、带 18×18 图标） | D-81 |
| 11 | mob_effects | `farmstorm:confusion` | `ConfusionEffect`（隐藏、无图标） | D-83 |
| 12 | sound_events | `farmstorm:friend_chives` | `SoundEvent.createVariableRangeEvent`（与既有 2 个同写法） | D-85 |
| 13 | damage_types | `farmstorm:zangief_slam` | 数据驱动：`FarmstormDamageTypes.ZANGIEF_SLAM` 的 `ResourceKey` + `data/farmstorm/damage_type/zangief_slam.json` | D-96 |

**注 1（第 3、6 行的同名关系）**：`point_defense_fungus` 的物品与方块同 ID，物品由 `DeployableCropItem` 承载且它是 `BlockItem` 子类（`item/DeployableCropItem.java` L18），因此 `Item.BY_BLOCK` 有映射、战利品表 `lootTableItem(block)` 能落到物品上。这与 0.0.2 的 6 个部署方块同一形态。

**注 2（第 4、7 行的非同型关系）**：`vibration_bean` 的物品是 `ThrowableCropItem`，**不继承 `BlockItem`**（它要能被投出去），所以同名方块没有 BlockItem 映射。后果两条，写进 §3.5 与 §9.3：① 方块战利品表必须显式 `LootItem.lootTableItem(FarmstormItems.VIBRATION_BEAN)`——写 `lootTableItem(block)` 会解析成 `Items.AIR`；② 物品模型走 `basicItem`，因此需要第 8 张 PNG（§1.4(d)）。

**注 3（第 3、6、7 行的 block_entities +0）**：`FarmstormBlockEntities.deployedOf(DeployedKind)` 的 switch 要加两个分支，但**不能**为它们新建 `BlockEntityType`（那会变成 `block_entities +1`）。正确处置是把两个新方块分别追加进两个既有 type 的方块集合：`fang_trap_cactus` 那个 type 收 `vibration_bean`，`blast_carrot` 那个 type 收 `point_defense_fungus`，`getEntries().size()` 仍是 6。

### 2.3 不属于 registry 的增量（不进 90）

按 `12-TECH-0.0.2.md` §2.4 的既有口径，转换配方与进度是数据文件，不进注册计数。**5 行。**

| 增量 | 数量 | 性质 | 位置 |
|---|---|---|---|
| 转换配方 | **+2** | 数据文件（`data/farmstorm/recipe/*.json`） | `point_defense_fungus`（`minecraft:warped_fungus` ⇄ 本品，双向、3 点经验、无催化剂，D-86）、`vibration_bean`（`minecraft:cocoa_beans` ⇄ 本品，同参数，D-90） |
| advancements | **+3**（本轮新增 3 条，硬计数 15 → 18） | 数据文件（datapack 注册表，无 `DeferredRegister`） | `first_q_bounce`（首次施加 Q弹）、`point_defense_intercept`（首次被近防菌拦截）、`vibration_bean_trigger`（首次触发震动豆），D-63 的扩充 |
| item tag（追加） | +1 项 | 数据文件 | `farmstorm:converter_fuel` 追加 `friend_chives`（D-84：没有转换配方，但仍是转换器的合成材料） |
| 既有进度不动 | +0 | — | 0.0.2 的 15 条进度一条不改（D-63） |
| 其余 6 类 | +0 | — | `block_entities` / `recipe types` / `recipe serializers` / `creative tabs` / `data_components` / `menu types` 全部复用 |

**朋友的韭没有转换配方**（D-84）：它没有原版对端，而 `ConverterRecipe` 的双向形态要求有一对可逆映射。

### 2.4 加总核对

| 算法 | 算式 | 结果 |
|---|---|---|
| 基线 + 增量 | 77 + 13 | **90** |
| 逐类相加 | 31 + 25 + 6 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 1 | **90** |
| 分项相加 | items 4 + blocks 3 + entity_types 2 + mob_effects 2 + sound_events 1 + damage_types 1 | **13** |

两条算法互为校验，**§2 的加总等于基线 77 + 13 = 90**。另有 5 个数据文件条目（转换配方 2 + 进度 3）不进这个和，单列在 §2.3。

**D-98 ~ D-102 对本章的影响为零**：五条补充裁定全部收在「既有实现内部的判据与语义」上——D-98 加己方筛选、D-99 定投掷物去处、D-100 复用原版音效、D-101 统一比较符、D-102 坐骑用原版类型。它们一条也不新增、不删除注册对象，因此 **77 / +13 / 90 三个数一字不动**（D-100 与 D-102 尤其如此：前者把 sound_events 钉在 +1，后者把 entity_types 钉在 +2）。

---

## 3. 新增与改动的类设计

每类给四项：**职责 / 关键方法 / 读哪些 config 键 / 改哪些既有文件**。比 `12-TECH` §3 的五项少了「写哪些注册对象」——因为 §2.2 已经逐条点名了 13 个对象与承载类，再写一遍就是两份会漂移的账。config 键名一律能在 §8.1 的 19 行里查到。

### 3.0 总览

| 类别 | 文件 | 新 / 改 |
|---|---|---|
| 效果 2 个 | `effect/QbounceEffect.java`、`effect/ConfusionEffect.java` | 新 |
| 客户端 3 个 | `client/QbounceClientState.java`、`client/QbouncePoseRenderer.java`、`client/ConfusionInputHandler.java` | 新 |
| 服务端 1 个 | `behavior/QbounceMountController.java`（mount 分支） | 新 |
| 投掷物 2 个 | `entity/thrown/FriendChivesProjectile.java`、`entity/thrown/VibrationBeanProjectile.java` | 新 |
| 行为 2 个 | `behavior/impl/PointDefenseFungusBehavior.java`、`behavior/impl/VibrationBeanTrap.java` | 新 |
| 刺球命中 1 个 | `behavior/impl/FangTrapFangs.java`（尖牙归属表 + 伤害事件订阅） | 新 |
| 既有 6 个文件 | `util/CropProfile.java`、`behavior/DeployedKind.java`、`block/DeployedCropBlock.java`、`blockentity/DeployedCropBlockEntity.java`、`behavior/impl/FangTrapCactusTrap.java`、`behavior/ZangiefPiledriverController.java` | 改 |
| 接线 | `registry/` 的 7 个注册类、`config/FarmstormConfig.java`、`Farmstorm.java`、`client/InputLockHandler.java`、datagen 的 7 个 Provider、`behavior/DeployedBehaviors.java`、`behavior/impl/ZangiefCaneTrap.java` | 改 |

### 3.1 `QbounceEffect`（新，`effect/`）

| 项 | 内容 |
|---|---|
| 职责 | Q弹 的状态载体。**唯一职责是「效果在不在 + 剩余时长」**，不设自己的时长键、不产生位移、不改属性、不发包（D-50 / D-81） |
| 关键方法 | 构造器 `super(MobEffectCategory.HARMFUL, 0xFF6EC7)`；不重写 `applyEffectTick`（空实现即无副作用）；不重写 `isDurationEffectTick` |
| 图标 | 可见、带图标：`assets/farmstorm/textures/mob_effect/q_bounce.png`（18×18）。隐藏与否不靠类级钩子，而是施加时的六参构造 `MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon)`——`q_bounce` 用 `(…, false, true)`（显示图标），`confusion` 用 `(…, false, false)` |
| 读哪些 config 键 | 不读。时长与等级由施加方 `FriendChivesProjectile` 写入：`friend_chives.effectTicks` 200 刻、amplifier 0 |
| 改哪些既有文件 | `registry/FarmstormMobEffects.java`（+1 个 `REGISTER.register("q_bounce", QbounceEffect::new)`）；`Farmstorm.java`（`logRegistrySelfCheck` 的 `mobEffects` 断言 2 → 4） |

**一条必须写下来的分工**：效果本体不知道自己是 10 秒还是 4 秒。谁施加谁定时长——`friend_chives.effectTicks` 是 Q弹 的唯一时长来源，`vibration_bean.confusionTicks` 是混乱的唯一时长来源（D-81 / D-83 / D-92）。

### 3.2 `ConfusionEffect`（新，`effect/`）

| 项 | 内容 |
|---|---|
| 职责 | 混乱的状态载体。**不注册图标**（因此 `textures/mob_effect/` 下没有它的 PNG，§9.1 的 8 张里也没有它）；只对玩家有效 |
| 关键方法 | 构造器 `super(MobEffectCategory.HARMFUL, 0x6A0DAD)`；无 tick 行为。「只对玩家有效」由两侧保证：施加端（`VibrationBeanTrap`）只遍历 `level.players()`；消费端（`ConfusionInputHandler`）跑在客户端输入链上，生物根本没有这条链 |
| 读哪些 config 键 | 不读（D-83 明确「混乱组连键都不设」）。时长由 `vibration_bean.confusionTicks` 80 刻写入 |
| 改哪些既有文件 | `registry/FarmstormMobEffects.java`（+1 个注册）；`Farmstorm.java`（同 3.1） |

### 3.3 `FriendChivesProjectile`（新，`entity/thrown/`）

| 项 | 内容 |
|---|---|
| 职责 | 朋友的韭的投掷物。落点对**欧氏 6 格**球内的生物施加 200 刻 Q弹；**至少命中 1 个生物**时在落点播一次命中音（32 格可闻），空投不响（D-85） |
| 关键方法 | `protected CropProfile profile()` 返回 `CropProfile.FRIEND_CHIVES`（必须是静态常量：基类在 `defineSynchedData` 期间就会调到它）；重写 `onImpact(ServerLevel, Vec3, @Nullable Entity)`——① `level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(radius), e -> e != shooter && e.isAlive() && pos.distanceTo(e.position()) <= radius)`；② 对每个命中者 `addEffect(new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true))`；③ 命中数 > 0 时在落点播放命中音 |
| 二次筛 | AABB 只是粗筛（`inflate(6.0)` 给的是 12×12×12 的立方），命中判定必须再用 `distanceTo` 做欧氏复核——否则就成了切比雪夫（D-20 的欧氏口径、D-85 的球体） |
| 读哪些 config 键 | `friend_chives.hitRadius`（6.0 格）、`friend_chives.effectTicks`（200 刻）、`friend_chives.soundRange`（32.0 格） |
| 改哪些既有文件 | `registry/FarmstormEntityTypes.java`（+1 次 `projectile("friend_chives", FriendChivesProjectile::new)`，参数与既有 10 个逐字一致：`MISC` / `0.25×0.25` / tracking 4 / interval 10）；`registry/FarmstormItems.java`（`FRIEND_CHIVES` 的 `cropItem` 指向它）；`registry/FarmstormSounds.java`（+1 个可变距离事件） |

**`soundRange` 怎么落到 32 格**：`SoundEvent.createVariableRangeEvent` 本身不定距离（既有 2 个事件都用它），可闻半径由播放端决定。本文的做法是**逐人播放**：遍历 `level.players()`，对 `distanceToSqr <= 32²` 的玩家单独发一个 `ClientboundSoundPacket`（**待验证** [RT-17]：1.21.1 的构造参数顺序），从而让 32 格成为硬上限而不是估计值。若实测构造不符，退化为 `level.playSound(null, pos, sound, SoundSource.PLAYERS, 1.0F, 1.0F)` 并接受服务端默认衰减，此时 §11 #12 的判据改为「32 格内可闻、33 格外不可闻」的实测记录。

### 3.4 `PointDefenseFungusBehavior`（新，`behavior/impl/`）——它是 `TurretBehavior`，不是 `TrapBehavior`

**判定依据（读接口，不猜）**：

| 接口 | 方法签名 | 能否拦住投掷物 |
|---|---|---|
| `behavior/TrapBehavior.java` | `Optional<Player> findTrigger(DeployedCropBlockEntity)`、`void trigger(DeployedCropBlockEntity, Player)` | **不能**。返回与形参都被钉成 `Player`，而投掷物是 `Entity` 且不是 `Player`。要用它就得改接口签名——那是改 0.0.2 既有内容，D-80 §5.7 不许 |
| `behavior/TurretBehavior.java` | `Optional<Entity> findTarget(DeployedCropBlockEntity)`、`void fire(DeployedCropBlockEntity, Entity)`、`int cooldownTicks()`，`default tick` = 索敌 → 开火 → `setCooldown(cooldownTicks())` | **能**。目标类型是 `Entity`，且 `default tick` 就是「找到就打」的形态 |

**结论**：`PointDefenseFungusBehavior implements TurretBehavior`。

| 项 | 内容 |
|---|---|
| 职责 | 以自身为圆心欧氏 8 格球内拦**非己方**的本模组投掷物；每次拦消耗 1 次，第 6 次后静默移除；拦截只有粒子与音效，不造成伤害（D-87 / D-88 / D-89，己方筛选由 **D-98** 补回） |
| `kind()` | `DeployedKind.POINT_DEFENSE_FUNGUS` |
| `cooldownTicks()` | **0**。返回 0 让 `default tick` 里的 `setCooldown(0)` 成为空操作，于是每次错峰扫描都能拦。这里不能用 `cooldown` 表达 6 次上限——上限是**方块生命**而不是节奏，二者语义不同 |
| `findTarget` | `level.getEntitiesOfClass(ThrownCropEntity.class, box, predicate)` 取最近的一个；predicate 的五道筛（未移除 / 非子母弹 / 欧氏 8 格内 / **非己方**）见 §6.2 第 4 步 |
| `fire` | 三条动作顺序固定：① 在投掷物位置 `server.sendParticles(ParticleTypes.CRIT, x, y, z, interceptParticles, 0.3, 0.3, 0.3, 0.05)`；② `level.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.8F, 1.2F)`（原版音，不新增 sound_event，否则 §2 的 +13 破）；③ `target.markIntercepted(now)` 打刻标记 → `be.recordIntercept(now)` → `target.discard()`（不调 `hurt`、不写方块、不 new `Explosion`）。**`discard()` 必须在 `onImpact` 之前返回**：被拦的投掷物当场从世界里消失，不结算落点——不爆炸、不写方块、不施加效果、不掉落（**D-99**） |
| 非己方筛选（D-98） | `final Entity thrower = target.getOwner()`；`thrower instanceof Player p && be.isAlly(p)` 为真 ⇒ **己方，直接跳过**（不扣次数、不发粒子、不 discard）；其余一律算非己方（含 `owner == null` 的发射器 / 指令生成，沿用 `DeployedOwnership#isAlly` L47–L52「无归属者时对所有人都是非己方」的既有口径）。判据只有 `be.isAlly(Player)` 这一处调用，不新写队伍比较 |
| 6 次上限 | `recordIntercept` 内部判 `count >= maxIntercepts` 后调 `be.consume()`（`removeBlock(pos, false)` + 破坏粒子，不走战利品表，D-72 的路径） |
| 读哪些 config 键 | `point_defense_fungus.interceptRadius`（8.0 格）、`point_defense_fungus.maxIntercepts`（6 次）、`point_defense_fungus.interceptParticles`（8 个） |
| 改哪些既有文件 | `behavior/DeployedKind.java`（+1 值 `POINT_DEFENSE_FUNGUS(10)`）；`behavior/DeployedBehaviors.java`（+1 个静态单例 + switch 的 `case`；switch 表达式漏分支编译期直接报错，不会静默漏登记）；`blockentity/DeployedCropBlockEntity.java`（`interceptCount` 字段与 `recordIntercept(long)`）；`entity/ThrownCropEntity.java`（tick 标记） |

**行为边界（接口给定的上限，必须写下来）**：`TurretBehavior#findTarget` 只回一个目标，所以**一个近防菌每次扫描最多拦 1 个投掷物**。扫描间隔 10 刻（0.5 秒）⇒ 单菌最多 **2 次/秒**；6 次上限意味着从第一次拦截算起，大约 **3 秒**打完 6 次。这是接口的形态，不是本文的取舍；要改就得改 `TurretBehavior`，本文不动它。

**`turret_bullet` 不在判定面内**：`entity/TurretBullet.java` 不是 `ThrownCropEntity` 的子类，所以 man瓜 的导弹不会被近防菌拦。这是「按基类判定」的必然结果，符合 D-87 的「只拦投掷物」，§11 #13 作为边界项验一次。

### 3.5 `VibrationBeanProjectile`（新，`entity/thrown/`）

| 项 | 内容 |
|---|---|
| 职责 | 震动豆的投掷物。落点放置一个不依附任何方块的隐形陷阱方块 + 方块实体，并把部署者写进它（D-91） |
| 关键方法 | `profile()` 返回 `CropProfile.VIBRATION_BEAN`；重写 `onImpact(ServerLevel, Vec3, @Nullable Entity)`——① 取落点 `BlockPos`（`BlockPos.containing(pos)`），若该位置不可替换则取上方一格，仍不可替换则不放置，投掷物照常消失；② `DeployedCropBlockEntity.canDeploy(level, target, shooter)` 为 false 时同样不放置（保留既有 8/区块、32/玩家两道闸门，投掷路径不能绕过它）；③ `level.setBlock(target, block.defaultBlockState().setValue(FACE, FLOOR).setValue(FACING, NORTH).setValue(FLOATING, false), 3)`；④ `if (level.getBlockEntity(target) instanceof DeployedCropBlockEntity be) be.setOwner(shooter)` |
| 为什么必须显式写 owner | 投掷路径不经过 `BlockItem`，因此 `DeployedCropBlock#setPlacedBy`（唯一写 owner 的地方，`block/DeployedCropBlock.java` L270–L275）不会被调用。不写 owner 的话 `be.isAlly()` 永远为 false，陷阱会把自己人也判成「非己方」——D-92 的触发端只认非己方玩家，这一条会失真 |
| 读哪些 config 键 | 不读（放置本身不参数化；触发与混乱在 `VibrationBeanTrap` 里） |
| 改哪些既有文件 | `registry/FarmstormEntityTypes.java`（+1 次 `projectile(...)`）；`registry/FarmstormItems.java`（`VIBRATION_BEAN` 的 `cropItem` 指向它） |

### 3.6 `VibrationBeanTrap`（新，`behavior/impl/`）——陷阱类的触发与结算

| 项 | 内容 |
|---|---|
| 职责 | 非己方玩家进入**严格小于 4 格**的欧氏距离即触发：对半径 4 内**所有玩家**施加 80 刻混乱 + 在陷阱处播一次爆炸音效，然后静默移除、不掉落（D-92） |
| 接口 | `implements TrapBehavior`（触发对象就是玩家，`Optional<Player>` 完全够用） |
| `findTrigger` | 遍历 `level.players()`，跳过旁观 / 创造 / `be.isAlly(player)`（沿用 HC-09 与 D-07 的豁免口径），判定 `center.distanceTo(player.position()) < triggerRadius`——**严格小于**，与 `FangTrapCactusTrap` 的 `<=` 不同，这两个不等号都是决策（D-92 / C-5） |
| `trigger` | ① 遍历 `level.players()`，对 `center.distanceTo(p.position()) < triggerRadius` 的**全部**玩家（严格小于，与触发端共用同一个键与同一个比较符，**D-101**）（不筛敌我，D-27 的一次性 AoE 口径）`addEffect(new MobEffectInstance(FarmstormMobEffects.CONFUSION, confusionTicks, 0, false, false, false))`；② `level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 0.9F)`（只用音效、不构造爆炸，D-58 的边界；该事件是**原版既有**的常量 `SoundEvents.GENERIC_EXPLODE`，不新增 `sound_event`，**D-100**）；③ `be.consume()` |
| 读哪些 config 键 | `vibration_bean.triggerRadius`（4.0 格）、`vibration_bean.confusionTicks`（80 刻） |
| 改哪些既有文件 | `behavior/DeployedKind.java`（+1 值 `VIBRATION_BEAN(10)`）；`behavior/DeployedBehaviors.java`（+1 单例 + switch case）；`registry/FarmstormBlocks.java`（+1 次 `deployed(...)` 且 `unattached = true`） |

**D-101 的落地：两端同一把尺子**

| 端 | 判据（同一常量、同一比较符） | 作用对象 | 依据 |
|---|---|---|---|
| 触发端 | `center.distanceTo(player.position()) < FarmstormConfig.vibrationBeanTriggerRadius.get()`（4.0 格） | 非己方玩家，带旁观 / 创造 / 己方三重豁免 | D-92、D-101 |
| 效果端 | `center.distanceTo(p.position()) < FarmstormConfig.vibrationBeanTriggerRadius.get()`（**同一个键、同一个 4.0、同一个 `<`**） | 半径内**所有**玩家，不筛敌我 | D-27、D-101 |

两端读同一个 config 键、写同一个比较符，因此不存在「没触发却被施加」的边界态。实现里一旦出现第二个 `4.0` 字面量或第二个比较符，即为实现错误——§11 #11 的边界用例正是在 3.9 格与 4.0 格各试一次。

### 3.7 震动豆的悬空方块模式（改 `block/DeployedCropBlock.java`）

新旗标 `unattached`（`private final boolean`），与 `supportsFloating` **并列但语义正交**，区别表见 §7.1。改动点四处，全部是「加分支」不是「改既有分支」：

| 位置 | 现在 | 改动 |
|---|---|---|
| 字段 + 构造器（L62–L85） | 4 个外部 boolean 入参（`supportsFloating` / `turretHardMine` / `explosionImmune`，`allyOnlyBreak` 派生） | 加第 5 个入参 `boolean unattached`，存字段并加 `public boolean unattached()` 访问器。既有 6 个调用点（`FarmstormBlocks` 的 6 次 `deployed`）补实参 `false` |
| `CODEC`（L53–L60） | 3 个字段（`kind` / `supports_floating` / `turret_hard_mine` / `explosion_immune`，共 4 个分量） | 加第 5 个 `Codec.BOOL.optionalFieldOf("unattached", false)`，放在 `explosion_immune` 之后、`propertiesCodec()` 之前 |
| `canSurvive`（L176–L180） | 恒查依附方块的 `isFaceSturdy` | 第一行 `if (this.unattached) return true;` |
| `updateShape`（L183–L221） | 依附方块消失 → 可悬空的转 `FLOATING`；其余掉落并自毁 | 第一行 `if (this.unattached) return state;`（**任何方向**都返回自身，早于 `direction != supportDirection(state)` 的判断） |

`getStateForPlacement` 不加分支：投掷路径根本不经过它（由 `VibrationBeanProjectile` 直接 `setBlock`），而悬空方块也不必支持手持放置（它的唯一来源是投掷物）。

### 3.8 刺球的重做（改 `behavior/impl/FangTrapCactusTrap.java` + 新增 `FangTrapFangs`）

D-94 的 8 行数值表里 4 行变、4 行不变。**其中 3 行的实现改动落在 config 默认值上，代码行一行不动**——这一点必须写清楚，否则实现者会去改代码里的字面量：

| D-94 的项 | 现状（源码） | 0.0.3 | 改在哪 |
|---|---|---|---|
| 触发距离 3.0 → **2.5** | L38 `FarmstormConfig.fangTrapTriggerRadius.get()` | 2.5 | `FarmstormConfig` 的 `defineInRange("triggerRadius", 3.0, 0.0, 32.0)` 默认值改 2.5（范围不动）。L38 **不改** |
| 外圈个数 8 → 8 | L61 `fangTrapOuterFangCount` | 8 | 不动 |
| 外圈延迟 10 → **4** | L61 `fangTrapOuterDelayTicks` | 4 | config 默认值 10 → 4。L61 **不改** |
| 外圈半径 2.0 → 2.0 | L61 `fangTrapOuterRadius` | 2.0 | 不动 |
| 内圈个数 5 → 5 | L59 `fangTrapInnerFangCount` | 5 | 不动 |
| 内圈延迟 4 → **2** | L59 `fangTrapInnerDelayTicks` | 2 | config 默认值 4 → 2。L59 **不改** |
| 内圈半径 1.0 → 1.0 | L59 `fangTrapInnerRadius` | 1.0 | 不动 |
| 命中效果 无 → **缓慢 V 60 刻** | 无 | 缓慢 amplifier 4、60 刻 | `spawnWave` 加 1 行登记 + 新增 `FangTrapFangs` 订阅 |

**代码层实际改 2 处 + 新增 1 类**：

1. `spawnWave`（L72–L93）在 `level.addFreshEntity(fangs)` **之前**加一行 `FangTrapFangs.mark(fangs, be.getBlockPos())`。归属行 L87–L89（`fangs.setOwner(owner)`）不动——D-95 明确「尖牙的归属者仍是部署者」。
2. `FangTrapFangs`（新类，`behavior/impl/`）：一张 `Map<Integer, Long>`（尖牙实体 id → 失效 gameTime），`mark` 写入 `gameTime + 40`（原版尖牙寿命 20 刻的两倍余量），`consume(Entity)` 读到即删并返回是否命中过。**不写 NBT、不注册任何对象**——它是一张有界内存表，条目上限 ≈ 单次触发的 13 个尖牙（8 外圈 + 5 内圈）。
3. `FangTrapFangs` 同时订阅 `LivingDamageEvent.Post`（**待验证** [RT-22]）：`event.getSource().getDirectEntity() instanceof EvokerFangs fangs` 且 `FangTrapFangs.consume(fangs)` 为真 → 对 `event.getEntity()` 施加 `new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDurationTicks, slownessLevel - 1)`。

**为什么不用包围盒判定**（D-95）：尖牙的伤害自带无敌帧、护甲与抗性结算；另造一套「在不在圈里」会造出「动画里被咬了、实际没掉血」却被减速的假命中。以实际伤害事件为准，等于把「击中」的定义交给唯一能改血的那一方。

**为什么不用 Mixin**：`@Mixin` 与 `mixins.json` 都在 `verify_project.py` §4 的禁令表里（D-40 / HC-04）。`EvokerFangs` 的伤害方法是 private，够不着，所以只能从伤害事件侧挂钩。

### 3.9 投掷物基类的一处新增（改 `entity/ThrownCropEntity.java`）

只加一个字段与两个方法，**不改任何既有行为**（既有 9 个直接子类 + 1 个间接子类的执行路径逐字不变）：

| 新增 | 签名 | 说明 |
|---|---|---|
| 字段 | `private long lastInterceptTick = Long.MIN_VALUE;` | 服务端内存态 |
| 方法 | `public boolean markIntercepted(long now)` | 若 `lastInterceptTick == now` 返回 `true`（本刻已被拦过，跳过）；否则写入 `now` 并返回 `false` |
| 方法 | `public boolean wasInterceptedAt(long now)` | 只读判定，供日志与自检用 |

**不进 NBT**：`addAdditionalSaveData` / `readAdditionalSaveData` 不动。理由：标记的有效期只有 1 刻，跨存档没有意义；写进去反而会让「读档后首次拦截」被误判成「本刻已拦」。

### 3.10 拦截计数（改 `blockentity/DeployedCropBlockEntity.java`）

| 项 | 内容 |
|---|---|
| 新增字段 | `private int interceptCount;`（默认 0） |
| 新增方法 | `public int interceptCount()`、`public void recordIntercept(long now)`——后者 `interceptCount++`、`setChanged()`，并在 `interceptCount >= FarmstormConfig.pointDefenseMaxIntercepts.get()` 时调 `consume()` |
| NBT | `saveAdditional` / `loadAdditional` 各加一行（键名 `interceptCount`），与既有 `placedGameTime` / `detached` 的写法一致。**这一条与 §3.9 相反**：计数要跨存档，因为「第 6 次后消失」是方块的生命周期，不是单刻语义 |
| 上限来源 | `point_defense_fungus.maxIntercepts`（6 次）。**不写死常量**：0.0.2 的 `HARD_MINE_TICKS` / `DEPLOY_COOLDOWN_TICKS` 是常量，因为它们没有「作者可能想调」的属性；6 次是明写在 D-88 里的可调数值 |
| 读哪些 config 键 | `point_defense_fungus.maxIntercepts` |
| 改哪些既有文件 | 本文件；`registry/FarmstormBlockEntities.java`（两个新方块挂进既有 type 的方块集合，见 §2.2 注 3） |

### 3.11 桑吉尔蔗的坠落伤害（改 `behavior/ZangiefPiledriverController.java` + 同文件新增订阅）

**挂哪个事件**：自然坠落伤害用 `net.neoforged.neoforge.event.entity.living.LivingFallEvent`（**待验证** [RT-22]），落地结算挂在既有的 `tickFalling`（L233–L246）里的 `player.onGround()` 分支——那里已经是「落地判定」的唯一入口，不再造第二个。

| 项 | 内容 |
|---|---|
| 取消自然坠落（相位 2 期间） | 新增一个 `@SubscribeEvent` 静态方法：`LivingFallEvent` → 判 `event.getEntity()` 身上有 `farmstorm:zangief_piledriver` 且 `getAmplifier() == ZangiefPiledriverEffect.PHASE_FALLING`（= 2）→ `event.setCanceled(true)`；另受 `zangief_cane.cancelFallDamage` 开关约束。判据是**效果存在性 + amplifier**，不是全局坠落逻辑，因此只作用于本陷阱的相位 2（RT-22） |
| 落地结算 | 在 `tickFalling` 的 `player.onGround()` 分支里、`finish(player)` **之前**：取 `ACTIVE.get(uuid).ownerId` 解析成 `Entity`（解析不到就传 `null`），`player.hurt(level.damageSources().source(FarmstormDamageTypes.ZANGIEF_SLAM, ownerEntity), (float) FarmstormConfig.zangiefCaneSlamDamage.get())`，然后 `finish(player)` |
| 顺序为什么固定 | **先结算、再收尾**——`finish` 会摘掉效果，摘掉之后 `cancelFallDamage` 的判据就不成立，反向的「先 finish 后 hurt」会造出「有伤害但相位已结束」的半截状态 |
| 归属传递 | `begin(ServerPlayer)` 加第二个参数 `@Nullable UUID ownerId`，`PhaseState` 加一个 `ownerId` 字段。调用点 `ZangiefCaneTrap#trigger`（L67）本来就有 `be.ownerUuid()` 可用，改成 `begin(player, be.ownerUuid())` |
| `cancelFallDamage` 的作用 | 布尔开关。为 `false` 时不取消自然坠落，于是「自然坠落 + 固定 7.5」叠加——这是给作者做对照实验留的口子，默认 `true` |
| 读哪些 config 键 | `zangief_cane.slamDamage`（7.5 点）、`zangief_cane.cancelFallDamage`（true） |
| 改哪些既有文件 | 本文件（`begin` 签名、`PhaseState`、`tickFalling`、新增订阅）；`behavior/impl/ZangiefCaneTrap.java`（L67 传 owner）；`registry/FarmstormDamageTypes.java`（+1 个 `ResourceKey` 与 `zangiefSlam(...)` 取用方法）；`datagen/ModDamageTypeProvider.java`（+1 个 `context.register`，`WHEN_CAUSED_BY_LIVING_NON_PLAYER`、`exhaustion 0.0F`、`DamageEffects.HURT`）；`Farmstorm.java`（`damageTypeCount()` 的列表 +1） |

### 3.12 两个客户端状态机（新，`client/`）

| 类 | 职责 | 关键方法 | config |
|---|---|---|---|
| `QbounceClientState` | 每 tick 读一次自己身上的 `farmstorm:q_bounce`，把「在不在 / 剩余刻数 / 进入刻」摊成静态标量。照 `ZangiefClientState` 的形态写（`ClientTickEvent.Post` + 三个 static 字段），不新增网络包 | `active()`、`remainingTicks()`、`phaseTicks()` | 不读 |
| `ConfusionInputHandler` | 混乱的反向输入（§5 全文） | `onMovementInput(MovementInputUpdateEvent)` | 不读 |

**`render` 分支的三个参数怎么到客户端**：`farmstorm-server.toml` 在专用服务器上客户端读不到，0.0.2 对这类参数用的是「编译期镜像常量 + 注释说明必须同步改」（`client/ZangiefClientState.java` L27–L34 的 `DEFAULT_SPIN_DEGREES_PER_TICK`）。本文沿用：`QbouncePoseRenderer` 里定义 `DEFAULT_ROTATE_DEGREES_PER_TICK = 24.0F` / `DEFAULT_SQUASH_AMPLITUDE = 0.15F` / `DEFAULT_SQUASH_PERIOD_TICKS = 6` 三个常量，值必须与 `FarmstormConfig` 的默认值逐字相等。**`qbounce.poseMode` 是例外**：它决定服务端要不要挂坐骑，因此只在服务端读，客户端不需要镜像。

---

## 4. Q弹的坐姿两条分支（技术方案）

两条都实现，`qbounce.poseMode` 切换，默认 `render`（D-82）。落选的那条靠配置关掉，**不删代码**。

### 4.1 开关方式

| 项 | 内容 |
|---|---|
| 键 | `qbounce.poseMode`，类型 `enum { render, mount }`，默认 `render`，范围就是这两个值（TOML 里写字符串） |
| 读取方 | `mount` 分支在服务端读（`QbounceMountController` 每 tick 判 `FarmstormConfig.qbouncePoseMode.get() == PoseMode.MOUNT`）；`render` 分支不读这个键——客户端只看自己身上有没有 `q_bounce` 效果 |
| 切换时机 | 运行期可改（`/reload` 后生效）。切到 `mount` 时，已在效果中的生物从下一个 tick 起挂坐骑；切回 `render` 时，`QbounceMountController` 在下一个 tick 把全部坐骑 `discard()` 并清表 |

### 4.2 render 分支

| 项 | 内容 |
|---|---|
| 挂哪个事件 | `net.neoforged.neoforge.client.event.RenderLivingEvent.Pre` 与 `Post`（**待验证** [RT-17]：1.21.1 的 `Pre` 是否提供 `getPoseStack()` / `getRenderer()` / `getPartialTick()`） |
| 进入条件 | `event.getEntity().hasEffect(FarmstormMobEffects.Q_BOUNCE)` |
| 姿势 | `event.getRenderer().getModel().riding = true`（**待验证** [RT-17]：`EntityModel#riding` 是否 public 且被对应渲染器读取）。这是「把它摆成乘坐姿势」的唯一非侵入手法——只改这一帧的模型实例，**不碰实体**，AI、碰撞箱、寻路全不受影响 |
| 压低与缩放 | `Pre` 里依次：① `poseStack.pushPose()`；② `poseStack.translate(0.0, -0.35, 0.0)` 压低模型（单位：格，本文补充值，可调范围 0–1）；③ 压缩因子 `s = 1.0 - amplitude * sin(2π * t / period)`，其中 `t = entity.tickCount + partialTick`、`amplitude` 取 `qbounce.squashAmplitude`（0.15 = 15 %）、`period` 取 `qbounce.squashPeriodTicks`（6 刻）；`poseStack.scale(1/s, s, 1/s)` 保持体积守恒；④ `poseStack.mulPose(Axis.YP.rotationDegrees(rotateDegreesPerTick * t % 360.0F))` 做持续旋转（**待验证** [RT-17]：1.21.1 用 `com.mojang.math.Axis` 还是 `org.joml` 的静态辅助） |
| 退出 | `Post` 里 `poseStack.popPose()`，与 `Pre` 的 `pushPose` 成对 |
| 读哪些 config 键 | `qbounce.rotateDegreesPerTick`（24.0 度/刻）、`qbounce.squashAmplitude`（0.15）、`qbounce.squashPeriodTicks`（6 刻）——三项都走 §3.12 的编译期镜像常量 |
| 失败模式 | ① **第三方模型的 `riding` 不生效**：别的模组的自定义 `EntityModel` 可能根本不读它，于是只有压低与旋转、没有坐姿。这是 RT-17 的主风险，缓解办法是接受它——render 分支的定位就是「不改实体状态」；② **`push` / `pop` 不平衡**：`Pre` push 了而 `Post` 因异常没跑到，会导致之后所有渲染累积位移（画面整体下沉）。缓解见 RT-17 的兜底栏；③ **压低量不符观感**：0.35 格是本文补充值，作者可在真机上调 |

### 4.3 mount 分支

| 项 | 内容 |
|---|---|
| 挂什么 | 服务端给每个受 Q弹 的生物挂一个**隐形坐骑**，类型按 **D-102** 定死为 `minecraft:armor_stand`（原版既有，不新增 `entity_type`）：`EntityType.ARMOR_STAND.create(level)` → `setInvisible(true)` / `setNoGravity(true)` / `setInvulnerable(true)` / `setSilent(true)` / `setMarker(true)`（**待验证** [RT-18]：`setMarker` 的语义是「取消碰撞箱与渲染」，若它连带取消乘客的坐姿，退回 `setMarker(false) + setSmall(true)`） |
| 挂载方式 | `stand.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), 0.0F)` → `level.addFreshEntity(stand)` → `entity.startRiding(stand, true)`（**待验证** [RT-18]：第二个 `boolean` 参数是「强制挂载」） |
| 记账 | `private static final Map<UUID, MountRecord> ACTIVE`（乘客 UUID → 坐骑实体 id + 失效 tick + 重挂次数），照 `ZangiefPiledriverController.ACTIVE` 的形态：只存内存、不写存档 |
| 每 tick 校验（`ServerTickEvent.Post`） | 三条同时成立才保留坐骑，否则 `discard()` 并移除记录：① 乘客还活着且 `getEffect(Q_BOUNCE) != null`；② `passenger.getVehicle() != null` 且 `getVehicle().getId() == record.mountId`；③ 坐骑还在（`level.getEntity(mountId) != null`） |
| 随生物死亡清理 | `LivingDeathEvent` 里立刻 `discard()`（不等下一个 tick）并从表里移除；另有三条退出路径与桑吉尔蔗同构：`PlayerEvent.PlayerLoggedOutEvent`、`PlayerEvent.PlayerChangedDimensionEvent`、`ServerStoppedEvent`（后者清空整张表） |
| 读哪些 config 键 | `qbounce.poseMode`（只在这条分支读） |
| 失败模式 | ① **改实体关系与碰撞**：坐骑是真实实体，会把乘客顶高（`armor_stand` 的乘客偏移），并改变别的模组对「玩家骑了什么」的判定——RT-18 的主风险；② **坐骑残留**：任何一条清理路径漏写都会留下隐形实体把玩家钉住，兜底是每 tick 的三条校验（不依赖事件）；③ **被第三方强制下车**：别的模组调 `stopRiding` 会让下一 tick 重新挂载产生抖动；缓解是 `MountRecord.reMountCount` 超过 20 次就放弃并打一行 warn（不刷屏） |

**D-102 的落地：坐骑定死为 `minecraft:armor_stand`**

| 项 | 内容 |
|---|---|
| 定死的类型 | `net.minecraft.world.entity.decoration.ArmorStand`（registry id `minecraft:armor_stand`）。**原版既有实体类型，不新增 `entity_type`**，因此 `entity_types` 增量保持 **+2**（两个投掷物，§2.1 第 4 行） |
| 为什么是它 | ① 它是唯一同时满足「能被完全隐形 + 能取消碰撞箱 + 无 AI + 可作 `Vehicle` 被 `startRiding` 挂上」四个条件的原版种类。② `setInvisible(true)` 后不渲染本体；`setMarker(true)` 取消碰撞箱与渲染（RT-18 的待验证项之一：`setMarker` 的语义），乘客的坐姿由 `LivingEntity` 的骑乘通道给出，所以姿势是**原版真实**的——这正是 mount 分支相对 render 分支的唯一收益（D-82）。③ 无 AI goal、不参与寻路，每 tick 只有 `tick()` 与乘客同步，开销与一个静态实体同量级。④ 它本身是 `LivingEntity`，`startRiding` 走原版常规通道；`setNoGravity(true)` 不被重力拉走、`setInvulnerable(true)` 不被误伤打死、`setSilent(true)` 不出声。⑤ 乘客偏移固定，坐标可预测 |
| 为什么不选其它原版类型 | `minecraft:boat` 与 `minecraft:minecart` 自带碰撞箱与交互（右键上船 / 上矿车）、且会改移动逻辑，与「隐形座位」语义冲突；`minecraft:item` / `minecraft:experience_orb` / `minecraft:area_effect_cloud` 不是可承载乘客的载体（`startRiding` 会失败）；`minecraft:bat` / `minecraft:silverfish` 有 AI、会自己动且会脱离原位。只有 `armor_stand` 落在「无 AI + 可隐形 + 可作载体」的交集里 |
| 不新增的理由（D-102 的口径） | 一个只为姿势服务的自定义实体类型，会成为一条谁都不会主动使用的注册项；原版类型已经够用。这与 D-100「复用原版音效」同向：两处都只用现成的东西，不扩大注册面 |
| 失败模式 | ① 若 `setMarker(true)` 连带取消乘客坐姿，退回 `setMarker(false) + setSmall(true) + setInvisible(true)`（此时碰撞箱回来，乘客被顶高的量变小）；② 若某个模组对 `armor_stand` 有特殊处理（例如禁止骑乘），该分支整体退化为 render——`qbounce.poseMode` 就是为此存在的 |
| 随生物死亡清理 | 见上表的「随生物死亡清理」行：`LivingDeathEvent` 立即 `discard()`，另有每 tick 三条校验兜底（不依赖事件）。僵尸 / 骷髅 / 玩家的死亡路径都经过 `LivingDeathEvent`，因此尸体不残留坐骑 |

### 4.4 两条分支的对照

| 维度 | render | mount |
|---|---|---|
| 改什么 | 渲染层的 `PoseStack` 与模型实例字段 | 服务端实体关系 + 碰撞箱 |
| 拿到的是不是「原版真实姿势」 | 不是（靠 `riding` 近似） | 是 |
| 影响 AI / 寻路 | 不影响 | 影响（乘客被抬高、被别的模组判为「在骑乘」） |
| 影响碰撞 | 不影响 | 影响 |
| 出错时的影响面 | 只有画面 | 实体状态，可导致卡死 |
| 默认 | **是** | 否 |
| 风险编号 | RT-17 | RT-18 |
| 注册增量 | 0 | 0（坐骑用原版 `minecraft:armor_stand`，D-102） |

---

## 5. 混乱的移动反转（技术方案）

### 5.1 只改方向分量

| 项 | 内容 |
|---|---|
| 挂哪个事件 | `net.neoforged.neoforge.client.event.MovementInputUpdateEvent`（0.0.2 的 `client/InputLockHandler.java` L61–L71 已经在用同一个事件，API 已证实存在，不需待验证） |
| 改什么 | `input.forwardImpulse = -input.forwardImpulse;` 与 `input.leftImpulse = -input.leftImpulse;`——`forwardImpulse` 的正向是前进，取反即 W↔S；`leftImpulse` 的正向是左，取反即 A↔D（D-83） |
| 不碰什么 | `input.jumping`、`input.shiftKeyDown`（跳跃与潜行不反转）；`forwardImpulse` / `leftImpulse` 的**数值大小**（只乘 −1，幅度逐字不变，因此「不碰 speed」）；玩家的 `Attributes.MOVEMENT_SPEED`（一个属性都不改）；视角（不订阅 `CalculatePlayerTurnEvent`、不碰灵敏度与相机）；攻击与使用（不碰 `InputEvent.MouseButton` / `InteractionKeyMappingTriggered`） |
| 进入条件 | `Minecraft.getInstance().player.hasEffect(FarmstormMobEffects.CONFUSION)` |

### 5.2 与 `InputLockHandler` 的优先级：锁优先

三段保证，从强到弱：

1. **显式判据（主）**：`ConfusionInputHandler` 的第一行是 `if (InputLockHandler.isLocked()) return;`。为此给 `client/InputLockHandler.java` 加一个 `public static boolean isLocked()`（把 L39–L41 的私有 `locked()` 包一层公开访问器，**既有七段逻辑一行不改**）。
2. **数值上的天然退化（副）**：即便两段代码的执行顺序被重排，结果也一致——锁跑在前则 `forwardImpulse` 先被清零，混乱再取反得到 `−0.0F`；而 `−0.0F == 0.0F` 为 `true`，移动消费者拿到的仍是零。混乱跑在前则取反后再被清零。两种顺序下玩家都不动。
3. **事件顺序不作为判据**：两个 `@EventBusSubscriber` 订阅同一个事件时**没有约定的执行顺序**，因此不能靠注册顺序保证「锁先跑」。这正是为什么第 1 段必须显式写，而不是只靠第 2 段。

**`locked()` 的判据要扩一项**：0.0.2 的 `locked()` 是 `ZangiefClientState.active() && player != null`；Q弹 也要上锁（D-81），所以改成 `(ZangiefClientState.active() || QbounceClientState.active()) && Minecraft.getInstance().player != null`。七段锁的分段与手法逐字不变（D-79 复用，不新写一套）。

### 5.3 边界与失败模式

| 边界 | 处置 |
|---|---|
| 效果剩余 1 刻时的抖动 | 客户端每 tick 读一次效果，效果消失后下一 tick 恢复正常（判据都是「当刻效果在不在」，不缓存） |
| 混乱 + Q弹 同时存在 | 锁优先，不反转（D-83 的显式结论）。玩家在锁定期内不受「反方向」影响 |
| 混乱作用在生物身上 | 无效但无害：生物没有客户端输入链，`MovementInputUpdateEvent` 不会为它触发。D-83 的「只对玩家有效」因此是结构性的，不是靠 `instanceof Player` 拦出来的 |
| 反转与冲刺 | `Input` 里没有冲刺字段；冲刺走 `player.isSprinting()`，本方案不碰它，因此冲刺方向跟着反转后的非零分量走——与「只反移动输入」一致 |

---

## 6. 近防菌的投掷物拦截（技术方案）

### 6.1 「什么算本模组的投掷物」——基类判据，不是 ID 白名单

| 项 | 内容 |
|---|---|
| 判据 | `target instanceof ThrownCropEntity`（`entity/ThrownCropEntity.java`）。一个 `instanceof` 覆盖全部成员 |
| 为什么能全覆盖 | `registry/FarmstormEntityTypes.java` 的 `projectile(String, EntityTypeFactory)` 工厂的泛型上界是 `<T extends ThrownCropEntity>`（L62），**编译期**保证 10 个既有 `entity_type` + 2 个新增的都在这条基类上。加白名单会漏、会漂移；基类判据由编译器维护 |
| 覆盖范围 | **12 个成员**（D-98 订正后的口径，逐件列在 §6.1 表下）：0.0.2 的 10 个 + `friend_chives` + `vibration_bean` |
| **己方排除（D-98）** | 命中面是**非己方**投掷物：投掷物的归属者与该近防菌的放置者同盟时**穿过、不拦、也不扣次数**。判据只用一处既有调用——`be.isAlly(throwerPlayer)`（`DeployedCropBlockEntity` L280–L285 → `DeployedOwnership#isAlly` → `Farmstorm.TEAMS` 的 `TeamResolver`，即 D-06 的原版计分板队伍口径），不新写队伍比较。归属者不是玩家（发射器 / 指令生成 / `getOwner() == null`）时按非己方处理，沿用 `DeployedOwnership` L47–L52「无归属者时对所有人都是非己方」的既有注释 |
| 明确不拦 | `minecraft:arrow` / `minecraft:snowball` / `minecraft:trident` / `minecraft:ender_pearl`（都不是 `ThrownCropEntity` 的子类）；`farmstorm:turret_bullet`（`entity/TurretBullet.java` 不在基类上）；其余模组的任何射弹 |
| 额外排除 | `isSubmunition()` 为 `true` 的实体（爆竹撒出的风弹，0.0.2 已有这个同步标记）：它是投掷物的**碎片**，拦它等于把一次爆竹变成 6 次拦截 |

投掷物家族表（**12 行**，D-98 的落地清单）：

| # | entity_type id | 承载类 | 归属者来源 | 是否本次新增 |
|---|---|---|---|---|
| 1 | `farmstorm:puffed_wheat` | `PuffedWheatProjectile` | `getOwner()`（投掷者 / 发射器） | 否 |
| 2 | `farmstorm:ubi_server` | `UbiServerProjectile` | 同上 | 否 |
| 3 | `farmstorm:toxic_ubi_server` | `ToxicUbiServerProjectile` | 同上 | 否 |
| 4 | `farmstorm:melon_slice_bomb` | `MelonSliceProjectile` | 同上 | 否 |
| 5 | `farmstorm:melon_grenade` | `MelonGrenadeProjectile` | 同上 | 否 |
| 6 | `farmstorm:bamboo_firecracker` | `BambooFirecrackerProjectile` | 同上 | 否 |
| 7 | `farmstorm:red_mushroom_puff` | `RedMushroomPuffProjectile` | 同上 | 否 |
| 8 | `farmstorm:brown_mushroom_puff` | `BrownMushroomPuffProjectile` | 同上 | 否 |
| 9 | `farmstorm:molotov` | `MolotovProjectile` | 同上 | 否 |
| 10 | `farmstorm:compressed_puffed_wheat` | `CompressedPuffedWheatProjectile`（**间接**：`extends PuffedWheatProjectile`） | 同上 | 否 |
| 11 | `farmstorm:friend_chives` | `FriendChivesProjectile`（`extends ThrownCropEntity`） | 同上 | **是** |
| 12 | `farmstorm:vibration_bean` | `VibrationBeanProjectile`（`extends ThrownCropEntity`） | 同上 | **是** |

分类小计：0.0.2 的成员 **10**（第 1–10 行）+ 本次新增 **2**（第 11–12 行）= **12**。第 10 行是「间接继承」那一件，也是 §1.4(a) 里「文档 9 / 源码 10」差额的来源。

### 6.2 每 tick 的判定流程

1. `DeployedCropBlockEntity.serverTick`（L131–L148）→ 冷却早退 → 错峰：`(now + stagger(interval)) % interval == 0` 才扫，`stagger = floorMod(pos.hashCode(), 10)`（L151–L156）。
2. `onScan` → `behavior().tick(be)` → `TurretBehavior` 的 default `tick` → `findTarget`。
3. `findTarget`：`AABB box = new AABB(center, center).inflate(8.0)`，`level.getEntitiesOfClass(ThrownCropEntity.class, box, predicate)`（**待验证** [RT-20]：这个重载在 1.21.1 的 `Level` 上是否存在；退化为不带 predicate 的重载再手工过滤）。
4. predicate **四筛**，顺序固定（D-98 的「先判 instanceof，再判同盟，最后才扣次数」）：① `!e.isRemoved()`；② `!e.isSubmunition()`；③ `center.distanceTo(e.position()) <= 8.0`（**欧氏复核**——AABB 是 16×16×16 的立方粗筛，不能当判定，否则就是切比雪夫：立方角点到中心的距离是 8√3 ≈ 13.86 格）；④ **非己方**——`!(e.getOwner() instanceof Player p && be.isAlly(p))`。第 ④ 条把己方投掷物在**索敌阶段**就排除掉，因此它既不会被 `fire`，也不会走到扣次数那一步。
5. 取最近的一个交给 `fire`。
6. `fire` 的四步：① `if (target.markIntercepted(now)) return;`（同刻去重，见 §6.3）→ ② `sendParticles` → ③ `playSound` → ④ `be.recordIntercept(now)` 后 `target.discard()`。顺序里**没有 `onImpact`**：拦截成功即返回，落点永不结算（D-99）。

### 6.3 「同刻只扣一次」的标记：加在哪、什么时候清

| 项 | 内容 |
|---|---|
| 加在哪 | **投掷物上**，不是方块上（D-88 的依据：方块实体只知道自己被扣了几次，无法知道同一刻还有谁在扣；投掷物是这条判定里唯一的共享对象）：`entity/ThrownCropEntity.java` 新增 `private long lastInterceptTick`（§3.9） |
| 写入时机 | `fire` 里第一件事：`if (target.markIntercepted(now)) return;`——返回 `true` 表示「本刻已被别人拦过」，本次直接跳过，**不消耗次数、不发射粒子** |
| 清除时机 | 三条：① **正常路径：下个 gameTime 自动失效**。判定是 `lastInterceptTick == now` 的相等比较，`now` 一变就不相等，无需任何清理代码；② **被拦之后**：投掷物随即 `discard()`，标记随实体一起消失；③ **绝不持久化**：`addAdditionalSaveData` 不加这个字段，因此读档后不存在「带着上一刻标记」的实体 |
| 为什么不用静态 `Set<Entity>` 表 | 表需要清理钩子（实体被移除时摘除），漏一处就是内存泄漏；一个 `long` 字段没有生命周期 |

### 6.4 扫描节奏与性能

| 项 | 数值与理由 |
|---|---|
| 扫描间隔 | 10 刻（0.5 秒）：`DeployedKind.POINT_DEFENSE_FUNGUS(10)` + `TurretBehavior#scanInterval()` 默认 10 |
| 错峰 | `floorMod(pos.hashCode(), 10)`：同区块最多 8 个近防菌（`MAX_DEPLOYED_PER_CHUNK = 8`）摊到不同 tick，最坏情况同一刻只有 1 个在扫 |
| 单次扫描代价 | 1 次 AABB 实体查询（按区块分段桶查，不遍历全服实体）+ n 次距离计算，n = 8 格立方内的投掷物数（实战通常 0–3） |
| 每秒总代价 | 单菌 2 次/秒；8 格内一簇 8 个菌 ⇒ 16 次/秒/区块，与既有炮台的扫描量同量级（`PumpkinTurret` 也是 10 刻间隔） |
| 上限 | `MAX_DEPLOYED_PER_PLAYER = 32`、`MAX_DEPLOYED_PER_CHUNK = 8`（既有两个常量，0.0.3 不动），因此最坏情况是「每玩家 32 个菌、每区块 8 个菌」，错峰后每区块每刻至多 1 次查询 |
| 不做的事 | 不做「投掷物主动广播自己位置」（要新包，D-50 禁止）；不做「菌之间互相通知」（要共享状态，RT-20 判断不划算） |

### 6.5 图效与伤害边界（D-89）

`fire` 只做三件事：`sendParticles`、`playSound`、`discard()`。**不调 `hurt`、不写方块、不 `new Explosion`、不改任何实体属性**。因此 §11 #14 的判据是「满血玩家站在 8 格内让 6 个投掷物被拦，血量逐点不变」。

**D-99 的落地（被拦下的投掷物去哪儿）**：拦截成立即 `discard()`，且**在 `onImpact` 之前返回**——被拦的投掷物不爆炸、不写方块、不施加效果、不掉落。删的是**投掷物实体**（D-99），不是方块；方块侧的第 6 次静默移除仍走 D-72 的 `consume()` 口径（D-88），两条路径不混用。

**D-100 的落地（音效复用，不新增注册）**：拦截音用原版 `SoundEvents.SHIELD_BLOCK`（`SoundSource.BLOCKS`、音量 0.8、音高 1.2、服务端默认可闻半径），不新增 `sound_event`。因此本版 `sound_events` 增量只有 `friend_chives` 的 **+1**（§2.2 第 12 行），音效事件总表见 §9.2。

---

## 7. 震动豆的不依附方块（技术方案）

### 7.1 新旗标 `unattached` 与既有 `supportsFloating` 的区别

| 维度 | `supportsFloating`（0.0.2 有） | `unattached`（0.0.3 新增） |
|---|---|---|
| 语义 | **依附丢失后容忍**：方块先合法依附，之后支撑消失仍保留 | **从一开始就不依附**：方块从来没有支撑 |
| 初始状态 | 放置时要求依附面白名单 + 依附方块完整不透明 | 无放置规则（唯一来源是投掷物） |
| `canSurvive` | 查依附方块的 `isFaceSturdy`（L176–L180） | 恒 `true` |
| `updateShape` | 依附方块消失 → 可悬空的置 `FLOATING = true`；其余掉落并自毁 | 任何方向都返回自身 |
| `FLOATING` 属性 | 被读写（模型按它加 90° 旋转，`ModBlockStateProvider` L97–L99） | 恒 `false`，永不置位 |
| 使用方 | `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant`（3 个传 `true`） | 只有 `vibration_bean` |
| 能否合并 | **不能**：合并会让「一开始就没支撑」的方块走「先依附、再丢失」的状态机，`canSurvive` 与 `updateShape` 的语义完全不同（D-91 的依据） |

### 7.2 四个场景的处置

| 场景 | 处置 | 依据 |
|---|---|---|
| `canSurvive` | `if (this.unattached) return true;` 放在方法第一行。任何调用方（`BlockItem` 的 `canPlace`、`updateOrDestroy`、`FallingBlock` 的检查）都拿到 `true` | D-91「canSurvive 恒真」 |
| `updateShape` | `if (this.unattached) return state;` 放在方法第一行，**早于** `direction != supportDirection(state)` 的判断。返回同一个 `BlockState` 实例 ⇒ `Block.updateOrDestroy` 的 `newState != state` 判据为 false ⇒ 既不触发 `destroyBlock` 也不触发 `setBlock` | D-91「不被方块更新移除」 |
| 活塞推动 | **不需要新判断**：`FarmstormBlocks.deployed(...)` 给所有部署方块设了 `PushReaction.BLOCK`（L86–L87），`BLOCK` 的语义是「推不动，且挡住活塞」。震动豆走同一个 `deployed(...)` 工厂，天生不可推 | 既有源码 |
| 区块卸载 | **不需要新判断**：方块状态与方块实体都进存档（`DeployedCropBlockEntity` 的 `saveAdditional` / `loadAdditional`），重载后 `getTicker` 照常返回（L136–L148：客户端返回 null、服务端返回 ticker）。要保证的是「别在 `setRemoved` 里做多余的事」——既有实现只释放部署配额（L307–L321），不动方块 | 既有源码 |
| 破坏方式 | 玩家可以打掉（默认硬度 0.5、`PushReaction.BLOCK`、无 `requiresCorrectToolForDrops`）；掉什么由战利品表决定，见 §9.3 #19 的注 | §2.2 注 2 |
| 静默移除 | 触发后走 `be.consume()`：`removeBlock(pos, false)` + 破坏粒子，不走战利品表、不掉落（D-92 沿用 D-72） | `DeployedCropBlockEntity#consume` L296–L305 |
| 两端同尺（**D-101**） | 触发端与效果端读**同一个** `vibration_bean.triggerRadius`（4.0 格）、写**同一个** `<`。本节只负责方块与更新，判定见 §3.6 末尾的两端表；本表的四条世界维护处置与比较符无关 | D-101 |

### 7.3 失败模式

| 模式 | 现象 | 兜底 |
|---|---|---|
| `updateShape` 漏写分支 | 邻近方块一变化，陷阱就凭空消失（玩家看到「豆子放下去就没了」） | §11 #10 的判据：放置后逐面破坏周围 6 个方块，方块必须仍在 |
| 与 `supportsFloating` 混用 | 若误把 `vibration_bean` 传成 `supportsFloating = true`：`updateShape` 会置 `FLOATING = true`、模型被加 90° 旋转（隐形方块看不出问题），但 `canSurvive` 仍返回 `isSupportValid(...)` = false，任何读 `canSurvive` 的路径都会判它该消失 | 登记在 RT-21；§11 #10 的第二条判据是「`FLOATING` 必须始终为 false」 |

---

## 8. config 变更全量清单

### 8.1 逐项清单（**19 行**）

列集与 `12-TECH-0.0.2.md` §8.1 相同：组.键 / 类型 / 默认 / 范围 / 用途 / D 编号。**本表 19 行 = 19 个键**（纯新增 16 + 改既有默认值 3）。表后有 1 行非键说明（`confusion` 无键），**不计入 19**。

| # | 组.键 | 类型 | 默认 | 范围 | 用途 | D |
|---|---|---|---|---|---|---|
| 1 | `qbounce.poseMode` | enum | `render` | `render` / `mount` | 坐姿分支开关（§4.1） | D-82 |
| 2 | `qbounce.rotateDegreesPerTick` | double | 24.0 | 0.0 – 360.0 度/刻 | 持续旋转的角速度 | D-81（默认值照施工事实卡 §3.3） |
| 3 | `qbounce.squashAmplitude` | double | 0.15 | 0.0 – 0.90 | 压缩拉伸幅度（比例，0.15 = 15 %） | D-81（默认值照施工事实卡 §3.3） |
| 4 | `qbounce.squashPeriodTicks` | int | 6 | 2 – 100 刻 | 压缩拉伸一个完整周期的刻数 | D-81（默认值照施工事实卡 §3.3） |
| 5 | `friend_chives.hitRadius` | double | 6.0 | 0.0 – 16.0 格 | 落点作用球半径（欧氏） | D-85 |
| 6 | `friend_chives.effectTicks` | int | 200 | 20 – 1200 刻 | Q弹 时长（200 刻 = 10 秒） | D-85 |
| 7 | `friend_chives.soundRange` | double | 32.0 | 0.0 – 64.0 格 | 命中音可闻半径 | D-85 |
| 8 | `point_defense_fungus.interceptRadius` | double | 8.0 | 0.0 – 16.0 格 | 拦截作用球半径（欧氏） | D-87 |
| 9 | `point_defense_fungus.maxIntercepts` | int | 6 | 1 – 64 次 | 拦截次数上限（第 N 次后静默移除） | D-88 |
| 10 | `point_defense_fungus.interceptParticles` | int | 8 | 0 – 64 个 | 拦截图效的粒子数 | D-89 |
| 11 | `vibration_bean.triggerRadius` | double | 4.0 | 0.0 – 16.0 格 | 触发距离（**严格小于**） | D-92 |
| 12 | `vibration_bean.confusionTicks` | int | 80 | 20 – 600 刻 | 混乱时长（80 刻 = 4 秒） | D-92 |
| 13 | `fang_trap_cactus.triggerRadius` | double | **2.5**（原 3.0） | 0.0 – 32.0 格 | 触发距离（欧氏） | D-94（改既有默认值） |
| 14 | `fang_trap_cactus.outerDelayTicks` | int | **4**（原 10） | 0 – 200 刻 | 外圈尖牙延迟（0.2 秒） | D-94（改既有默认值） |
| 15 | `fang_trap_cactus.innerDelayTicks` | int | **2**（原 4） | 0 – 200 刻 | 内圈尖牙延迟（0.1 秒） | D-94（改既有默认值） |
| 16 | `fang_trap_cactus.slownessDurationTicks` | int | 60 | 0 – 600 刻 | 缓慢持续刻数 | D-94 |
| 17 | `fang_trap_cactus.slownessLevel` | int | 5 | 1 – 10 级 | 缓慢等级（写入 amplifier = level − 1 = 4） | D-94 |
| 18 | `zangief_cane.slamDamage` | double | 7.5 | 0.0 – 40.0 点 | 落地固定伤害（点 = 半颗心） | D-96 |
| 19 | `zangief_cane.cancelFallDamage` | boolean | `true` | `true` / `false` | 相位 2 期间取消自然坠落伤害 | D-96 |

**1 行非键说明**：`confusion` 组**不设任何键**——混乱没有「效果自身时长」这一档，时长由施加方 `vibration_bean.confusionTicks`（80 刻）写入（D-83、D-92）。`q_bounce` 同理（时长由 `friend_chives.effectTicks` 写入），但 `qbounce.*` 组存在，因为它承载姿势与控制参数，与「效果时长」无关。

**行数与分类小计**：本表 19 行 = 纯新增 16（#1–#12、#16–#19）+ 改既有默认值 3（#13–#15）。分类小计：`qbounce` 4 + `friend_chives` 3 + `point_defense_fungus` 3 + `vibration_bean` 2 + `fang_trap_cactus` 5 + `zangief_cane` 2 = 19。

**既有键的范围一个不动**：#13 / #14 / #15 只改 `defineInRange` 的默认值实参，`min` / `max` 与 `translation` 键逐字保留（现值 `0.0–32.0` / `0–200` / `0–200`，见 `FarmstormConfig` L494–L511）。

### 8.2 TOML 片段（新增的 4 个分组，按字母序追加）

`
[qbounce]
	# 坐姿分支：render = 只改渲染层（默认），mount = 服务端挂隐形坐骑
	poseMode = "render"
	# 持续旋转的角速度（度/刻），范围 0.0 ~ 360.0
	rotateDegreesPerTick = 24.0
	# 压缩拉伸幅度（比例），范围 0.0 ~ 0.90
	squashAmplitude = 0.15
	# 压缩拉伸一个完整周期的刻数，范围 2 ~ 100
	squashPeriodTicks = 6

[friend_chives]
	# 落点作用球半径（格，欧氏），范围 0.0 ~ 16.0
	hitRadius = 6.0
	# Q弹 时长（刻），200 刻 = 10 秒；范围 20 ~ 1200
	effectTicks = 200
	# 命中音可闻半径（格），范围 0.0 ~ 64.0
	soundRange = 32.0

[point_defense_fungus]
	# 拦截作用球半径（格，欧氏），范围 0.0 ~ 16.0
	interceptRadius = 8.0
	# 拦截次数上限，第 N 次拦截后静默移除；范围 1 ~ 64
	maxIntercepts = 6
	# 拦截图效的粒子数，范围 0 ~ 64
	interceptParticles = 8

[vibration_bean]
	# 触发距离（格，严格小于），范围 0.0 ~ 16.0
	triggerRadius = 4.0
	# 混乱时长（刻），80 刻 = 4 秒；范围 20 ~ 600
	confusionTicks = 80
`

`fang_trap_cactus` 既有 7 键里改 3 个默认值 + 追加 2 个新键，`zangief_cane` 既有 6 键里追加 2 个新键——两组都在文件中原位置追加，不新建分组。

### 8.3 键数与语言键的相等性

| 项 | 0.0.2 | 增量 | 0.0.3 |
|---|---|---|---|
| config 键 | 139 | +16 | **155** |
| `farmstorm.configuration.*` 语言键 | 139 | +16 | **155** |

> 注：§8.1 的 19 行里 #13/#14/#15 是既有键改默认值（键名不变），故键数为 139 + 16 = **155**；旧稿按 139 + 19 = 158 计算时把这三个键重复计入。

两者恒相等，因为每个键都由 `FarmstormConfig.tr(path)`（`"farmstorm.configuration." + path`）生成一个语言键，`ModLangProvider.config(path)` 一一对应地写一条。`verify_project.py` §1 的修法见 §1.4(c)：期望键集 = `01-DESIGN.md` §3.8 的 117 + `12-TECH-0.0.2.md` §8.1 的 22 = 139，v0.0.3 起再 + 本表新增 16 = 155。

### 8.4 明确不进 config 的常量（逐条给理由）

| 常量 | 值 | 理由 |
|---|---|---|
| Q弹 / 混乱的 amplifier | 0 | 这两个效果不叠层，等级没有含义（D-50 的「效果存在 + amplifier + 剩余时长即完整状态」里，amplifier 在此只用于相位类效果） |
| 拦截音效 | `SoundEvents.SHIELD_BLOCK` | 用原版音，可听距离由原版决定；不新增 `sound_event`（否则 §2 的 +13 破） |
| 拦截粒子类型 | `ParticleTypes.CRIT` | 只参数化数量（`interceptParticles`），类型不是可调项 |
| 桑吉尔蔗 `MAX_FALL_TICKS` | 100 刻 | 0.0.2 既有常量，安全阀性质，不进 config（HC-13 同类），本版不动 |
| `FangTrapFangs` 的 TTL | 40 刻 | 实现细节（尖牙寿命 20 刻的两倍余量），与玩法无关 |

---

## 9. 资源与数据文件清单

口径：只列 **0.0.3 新增或需要追加条目**的文件，不列 0.0.2 既有的 200 余个。三张表（PNG / OGG / JSON）各自给分类小计；「追加条目」的既有文件单列，不计入新增文件数。

### 9.1 PNG（**8 行**）

| # | 文件 | 尺寸 | 来源 / 处置 | 依据 |
|---|---|---|---|---|
| 1 | `assets/farmstorm/textures/mob_effect/q_bounce.png` | 18×18 | `preview.jpg`（1024×1024）转换后缩放；可见图标 | D-81、D-53 |
| 2 | `assets/farmstorm/textures/item/friend_chives.png` | 16×16 | 新绘物品图标 | D-84 |
| 3 | `assets/farmstorm/textures/item/chive_seeds.png` | 16×16 | 新绘物品图标 | D-84 |
| 4 | `assets/farmstorm/textures/item/vibration_bean.png` | 16×16 | 新绘物品图标（隐形陷阱没有方块外观可借，必须自备；同 0.0.2 的 `blast_carrot` / `fang_trap_cactus` / `zangief_cane`） | D-90、§1.4(d) |
| 5 | `assets/farmstorm/textures/block/friend_chives_stage0.png` | 16×16 | **洋红 #D81E8A 占位，非终稿** | D-84 |
| 6 | `assets/farmstorm/textures/block/friend_chives_stage1.png` | 16×16 | **洋红 #D81E8A 占位，非终稿** | D-84 |
| 7 | `assets/farmstorm/textures/block/friend_chives_stage2.png` | 16×16 | **洋红 #D81E8A 占位，非终稿** | D-84 |
| 8 | `assets/farmstorm/textures/block/friend_chives_stage3.png` | 16×16 | **洋红 #D81E8A 占位，非终稿** | D-84 |

分类小计：效果图标 **1**（#1）+ 物品图标 **3**（#2 / #3 / #4）+ 方块贴图 **4**（#5–#8）= **8**。

不新增 PNG 的两个对象（必须写明，否则会有人去补两张）：

- `point_defense_fungus` 的方块与物品都借原版 `minecraft:block/warped_fungus` / `minecraft:item/warped_fungus`——D-86 的对应物就是诡异菌，与 0.0.2 的 `pumpkin_turret` 借 `block/pumpkin` 是同一手法（`ModBlockStateProvider` 的 `DEPLOYED_VANILLA_PARENT`、`ModItemModelProvider` 的 `VANILLA_BLOCK_PARENT` 各 +1 项）；
- `vibration_bean` 的方块是隐形空模型，`particle` 贴图借原版 `minecraft:block/cocoa_stage2`（D-90 的对应物是可豆），0 张。

### 9.2 OGG（**1 行**）

| # | 文件 | 规格 | 来源 |
|---|---|---|---|
| 1 | `assets/farmstorm/sounds/friend_chives.ogg` | mono 64 kbps OGG | 源文件 `pengyoudejiu.mp3`（7,450,588 B，文件名保留不改，D-66 的扩充） |

分类小计：**1**。

**§9.2 附：音效事件总表（D-100 的落地，5 行）**

本表是 D-100 要求的「逐处写明复用的原版事件 id」。**只有第 1 行是本版新增的注册对象**，后四行全部复用原版，因此 `sound_events` 增量恒为 **+1**（§2.1 第 6 行）。

| 处 | 播放点 | 事件 id | 注册归属 | 依据 |
|---|---|---|---|---|
| 朋友的韭命中 | `FriendChivesProjectile#onImpact`，落点，命中 ≥ 1 个生物才播 | `farmstorm:friend_chives` | **本模组新增**（+1） | D-85 |
| 近防菌拦截 | `PointDefenseFungusBehavior#fire`，被拦投掷物所在位置 | 常量 `SoundEvents.SHIELD_BLOCK` | 复用原版 | D-89、**D-100** |
| 震动豆爆炸 | `VibrationBeanTrap#trigger`，陷阱所在位置 | 常量 `SoundEvents.GENERIC_EXPLODE` | 复用原版 | D-92、**D-100** |
| 桑吉尔蔗触发 | `ZangiefCaneTrap#trigger`（0.0.2 既有，本版不动） | `farmstorm:zangief_piledriver` | 0.0.2 既有 | D-66 |
| 狂野猎人循环 | 客户端 `SoundInstance`（0.0.2 既有，本版不动） | `farmstorm:wild_hunter_loop` | 0.0.2 既有 | D-76 |

两处复用为什么可以复用到「不是本模组的命名空间」：音效事件不是本模组的注册面，`level.playSound` 接受任何已注册的 `Holder<SoundEvent>`；D-100 的动机正是「不扩大注册面」，因此这两处越靠原版越符合口径。

**表里不写 id 字符串**：两处复用一律通过 Java 常量 `SoundEvents.*` 取 `Holder<SoundEvent>`，代码里不出现手写的 `minecraft:***` 字符串。这样既不会因为记错 1.21 的事件 id 而埋一个静默无声的坑，也不需要在本文里固化一个本文无法从源码证实的字符串。

### 9.3 JSON（新增 **22 行** + 既有文件追加 **5 处**）

| # | 文件（`src/generated/resources/` 或 `src/main/resources/` 下） | 性质 |
|---|---|---|
| 1 | `assets/farmstorm/blockstates/friend_chives_crop.json` | 4 个 age 变体，指向 `friend_chives_stage0..3` |
| 2 | `assets/farmstorm/models/block/friend_chives_stage0.json` | 父 `minecraft:block/crop`，`render_type: cutout` |
| 3 | `assets/farmstorm/models/block/friend_chives_stage1.json` | 同上 |
| 4 | `assets/farmstorm/models/block/friend_chives_stage2.json` | 同上 |
| 5 | `assets/farmstorm/models/block/friend_chives_stage3.json` | 同上 |
| 6 | `assets/farmstorm/blockstates/point_defense_fungus.json` | face × facing × floating 全覆盖，指向同一个模型 |
| 7 | `assets/farmstorm/models/block/point_defense_fungus.json` | 父 `minecraft:block/warped_fungus` + `render_type: cutout` |
| 8 | `assets/farmstorm/blockstates/vibration_bean.json` | 同上（空模型 + particle） |
| 9 | `assets/farmstorm/models/block/vibration_bean.json` | 只有 `particle` 的空模型（与 `blast_carrot.json` 同构） |
| 10 | `assets/farmstorm/models/item/friend_chives.json` | `basicItem` |
| 11 | `assets/farmstorm/models/item/chive_seeds.json` | `basicItem` |
| 12 | `assets/farmstorm/models/item/point_defense_fungus.json` | 父 `minecraft:block/warped_fungus` |
| 13 | `assets/farmstorm/models/item/vibration_bean.json` | `basicItem`（引 `farmstorm:item/vibration_bean`，§9.1 #4） |
| 14 | `data/farmstorm/damage_type/zangief_slam.json` | `message_id: farmstorm.zangief_slam`、`WHEN_CAUSED_BY_LIVING_NON_PLAYER`、`exhaustion 0.0`、`HURT` |
| 15 | `data/farmstorm/recipe/point_defense_fungus.json` | `farmstorm:converting`，`minecraft:warped_fungus` ⇄ 本品，3 点经验，无催化剂，`bidirectional: true` |
| 16 | `data/farmstorm/recipe/vibration_bean.json` | 同上，`minecraft:cocoa_beans` ⇄ 本品 |
| 17 | `data/farmstorm/loot_table/blocks/friend_chives_crop.json` | 成熟 1 个作物 + 0–3 个种子（照既有作物的二项分布参数） |
| 18 | `data/farmstorm/loot_table/blocks/point_defense_fungus.json` | 掉自身（`deployed(block)` 路径，方块有 BlockItem） |
| 19 | `data/farmstorm/loot_table/blocks/vibration_bean.json` | **必须显式 `LootItem.lootTableItem(FarmstormItems.VIBRATION_BEAN)`**，不能写 `lootTableItem(block)`（该方块没有 BlockItem，会解析成 `Items.AIR`，§2.2 注 2） |
| 20 | `data/farmstorm/advancement/first_q_bounce.json` | `minecraft:impossible` + 服务端 `award`（进 `AWARD_REQUIRED`） |
| 21 | `data/farmstorm/advancement/point_defense_intercept.json` | 同上 |
| 22 | `data/farmstorm/advancement/vibration_bean_trigger.json` | 同上 |

分类小计：blockstate **3**（#1 / #6 / #8）+ block 模型 **6**（#2–#5 / #7 / #9）+ item 模型 **4**（#10–#13）+ damage_type **1**（#14）+ recipe **2**（#15 / #16）+ loot_table **3**（#17–#19）+ advancement **3**（#20–#22）= **22**。

**既有文件追加 5 处**（不算新增文件，但漏了就功能不全）：

| # | 文件 | 追加什么 |
|---|---|---|
| A1 | `assets/farmstorm/sounds.json` | +1 个事件 `friend_chives`（`category: player`、`subtitle: subtitles.farmstorm.friend_chives`、单条 sound、`stream: false`）；事件数 2 → 3 |
| A2 | `assets/farmstorm/lang/zh_cn.json` | +34 键（见 §9.4） |
| A3 | `assets/farmstorm/lang/en_us.json` | +34 键（键集与 A2 逐字相同） |
| A4 | `data/farmstorm/tags/item/converter_fuel.json` | +1 项 `farmstorm:friend_chives`（D-84） |
| A5 | `data/farmstorm/tags/block/deployed_crops.json` 与 `data/minecraft/tags/block/mineable/pickaxe.json` | 各 +2 项（`point_defense_fungus`、`vibration_bean`） |

A5 的另一半说明：0.0.2 的 `MINEABLE_WITH_PICKAXE` 里没有收 `zangief_cane`（既有状态），本版不补，只保证两个新方块与既有部署方块一致。

### 9.4 语言键增量（**34 键**）

| 前缀 | 0.0.2 | 增量 | 0.0.3 | 来源 |
|---|---|---|---|---|
| `item.farmstorm.*` | 27 | +4 | 31 | `friend_chives` / `chive_seeds` / `point_defense_fungus` / `vibration_bean` |
| `block.farmstorm.*` | 22 | +3 | 25 | `friend_chives_crop` / `point_defense_fungus` / `vibration_bean` |
| `effect.farmstorm.*` | 2 | +2 | 4 | `q_bounce` / `confusion` |
| `subtitles.farmstorm.*` | 2 | +1 | 3 | `friend_chives` |
| `farmstorm.configuration.*` | 139 | +16 | 155 | §8.1 中纯新增的 16 个键 |
| `advancements.farmstorm.*` | 30 | +6 | 36 | 3 条进度 × 2（title + description） |
| `death.attack.farmstorm.*` | 4 | +2 | 6 | `zangief_slam` + `zangief_slam.player`（照既有 2 个伤害类型各 2 键的形态） |
| 其余（`tooltip` 16 / `entity` 11 / `gui` 8 / `container` 1 / `itemGroup` 1） | 37 | +0 | 37 | 不改（16 + 11 + 8 + 1 + 1 = 37） |
| **合计** | **263** | **+34** | **297** | |

**三列合计的核对式**（逐行相加，两列都落在合计行上）：0.0.2 列：27+22+2+2+139+30+4+37 = **263**；0.0.3 列：31+25+4+3+155+36+6+37 = **297**；增量列：4+3+2+1+16+6+2+0 = **+34**。三处同时成立。

`+0` 的三类为什么不动：`tooltip.farmstorm.*` 的 16 键按类别（陷阱 / 炮台 / buff / 破障 / 伤害 / 建造）与「无原版对应物」等固定条目组织，两个新部署物复用既有类别行；`entity.farmstorm.*` 的 11 键与新 `entity_type` 的关系是「有独立显示名才给一条」，两个新投射物沿用物品名做显示名（`ThrownCropEntity#getDefaultItem` 从作物档案取物品，名称自然来自 `item.farmstorm.*`）。

---

## 10. 风险登记册（RT-17 ~ RT-23）

编号固定 7 条，不增删合并。每条五项：风险 / 触发条件 / 首要缓释 / 兜底 / 实现阶段怎么验。

### RT-17 · Q弹 render 分支：模型标志与 PoseStack 的生命周期

| 项 | 内容 |
|---|---|
| 风险 | ① 第三方模组的自定义 `EntityModel` 不读 `riding` 字段 ⇒ 只有压低与旋转、没有坐姿；② `RenderLivingEvent.Pre` 的 `pushPose` 与 `Post` 的 `popPose` 不成对 ⇒ 之后所有渲染累积位移；③ 本文在此挂了 4 个待验证 API（`RenderLivingEvent.Pre/Post` 的取法、`EntityModel#riding`、`com.mojang.math.Axis`、`ClientboundSoundPacket` 的构造参数） |
| 触发条件 | 装上任意替换生物模型的模组；或 `Post` 因异常提前返回；或编译期就报「找不到符号」 |
| 首要缓释 | 只改渲染层、不碰实体（AI / 碰撞箱 / 寻路全不受影响），因此出错的影响面限于画面；`riding` 的赋值放在 `Pre` 开头的第一个动作，任何一步失败都不影响实体状态 |
| 兜底 | ① 待验证 API 缺失时改用「只 `translate` + `scale` + `mulPose`，不 `push`」的退化写法（不 push 就不存在不平衡）；② `riding` 无效时接受该形态，把结论写进验收记录，由 D-82 的真机比对决定是否切到 `mount`；③ 坐姿完全拿不到时 `poseMode` 切 `mount` |
| 实现阶段怎么验 | ① 编译期：4 个待验证 API 各写一个最小用例先跑通；② 运行期：对同屏 3 个受 Q弹 的僵尸 / 玩家各看一帧，确认「压低 0.35 格 + 约 480 度/秒旋转（24 度/刻 × 20 刻/秒）+ 6 刻一个压缩周期」；③ 连续渲染 30 秒后看画面是否整体漂移（验证 push/pop 平衡） |

### RT-18 · Q弹 mount 分支：实体关系与坐骑残留

| 项 | 内容 |
|---|---|
| 风险 | ① 坐骑是真实实体，会抬高乘客、改变碰撞与「玩家骑了什么」的判定，可能与别的模组的骑乘逻辑打架；② 任何一条清理路径漏写 ⇒ 隐形实体残留，把玩家钉在原地；③ 两个待验证 API（`ArmorStand#setMarker`、`Entity#startRiding(Entity, boolean)`）语义不符 |
| 触发条件 | 同一生物被反复施加 Q弹；玩家在效果期间死亡 / 掉线 / 换维度；别的模组调 `stopRiding`；`setMarker(true)` 把乘客的坐姿也取消 |
| 首要缓释 | 记账表 `Map<UUID, MountRecord>` + 每 tick 三条校验（效果在不在 / 乘客还是它 / 坐骑还在不在），**不依赖事件**；四条退出路径（死亡 / 掉线 / 换维度 / 服务器停止）＋ 每 tick 兜底 |
| 兜底 | ① `setMarker(true)` 若连带取消坐姿，退回 `setMarker(false) + setSmall(true) + setInvisible(true)`；② 重新挂载超过 20 次就放弃并打一行 warn（不刷屏）；③ 该分支任何时候都能靠 `qbounce.poseMode = render` 整体关掉且不删代码 |
| 实现阶段怎么验 | ① 对一个僵尸施加 Q弹，验「坐姿 + 随生物死亡而坐骑消失」；② 效果期间 `kill` 该生物、断线重连、走传送门各一次，验世界里不残留 ArmorStand（用日志行或 `/data get entity`）；③ 切 `poseMode` 两次（render → mount → render），验每次切换后表被清空 |

### RT-19 · 混乱与输入锁共存：两套逻辑动同一个 `Input`

| 项 | 内容 |
|---|---|
| 风险 | 混乱与 Q弹 / 桑吉尔蔗的七段锁同时对 `Input` 动手：顺序不利时可能出现「本应锁定却反向移动」；若为混乱新写第二套锁，会与既有七段判定互相覆盖（D-79 明令不复用第二套） |
| 触发条件 | 玩家同时持有 `q_bounce` 与 `confusion`；或将来有人给混乱加「锁」的语义；或两个订阅者的执行顺序被运行时重排 |
| 首要缓释 | 三段保证（§5.2）：显式 `if (InputLockHandler.isLocked()) return;` + 数值上 `−0.0F == 0.0F` 的天然退化 + 明确「不拿事件顺序当判据」。混乱只订阅一个事件，不写第二套锁 |
| 兜底 | `locked()` 的判据扩成 `ZangiefClientState.active() 或 QbounceClientState.active()`；若将来出现第三个「要锁」的来源，只在 `locked()` 里加一项，不动七段 |
| 实现阶段怎么验 | ① 只施加混乱：按 W 向后走、按 A 向右走，幅度不变（§11 #6）；② 同时施加混乱 + Q弹：按 W 完全不动，且锁的日志行已打印（§11 #7）；③ 效果结束的下一 tick 方向恢复正常 |

### RT-20 · 近防菌：逐 tick 扫描、计数与「同刻只扣一次」

| 项 | 内容 |
|---|---|
| 风险 | ① AABB 是立方粗筛，若把它当判定就成了切比雪夫（8 格立方的角点距中心 13.86 格）；② 同一刻多个菌够到同一投掷物 ⇒ 重复扣次数；③ 计数与 6 次上限的状态分裂（方块记次数、投掷物记去重）导致「扣了但没拦」；⑤ **己方筛选漏写**（D-98）⇒ 自己人和队友的投掷物被自己的菌吃掉，变成「己方内耗」；⑥ **拦截后没真删**（D-99）⇒ 投掷物照常结算落点，6 次上限失去意义；④ 一个待验证 API（`Level#getEntitiesOfClass(Class, AABB, Predicate)` 的重载） |
| 触发条件 | 8 格内同时有 2 个以上近防菌与 1 个投掷物；投掷物从对角方向飞来；`interceptRadius` 被调到 16 格；两个菌的 `stagger` 恰好相同 |
| 首要缓释 | ① 命中判定必须 `distanceTo(...) <= radius` 复核，AABB 只做粗筛（§6.2 第 4 步）；② 去重标记放在投掷物上（`lastInterceptTick`），不是方块上；③ 错峰 `floorMod(pos.hashCode(), 10)` 让同区块 8 个菌摊到不同 tick；④ `TurretBehavior#findTarget` 只回一个目标 ⇒ 单菌每次扫描最多拦 1 个（§3.4 边界）；⑤ 己方筛选放在 predicate 的第 ④ 筛（索敌阶段），因此己方投掷物根本进不到 `fire`，不可能被扣次数（D-98）；⑥ `fire` 的第四步固定是 `recordIntercept` 后 `discard()`，没有任何走 `onImpact` 的分支（D-99） |
| 兜底 | ① 若待验证重载不存在，退化为不带 predicate 的重载 + 手工过滤；② 若实测同刻仍有双扣，把去重从「投掷物字段」升级为「服务端 tick 级的 `Set<Integer>`，在 `ServerTickEvent.Post` 清空」——但先按字段方案实现，因为字段没有清理钩子、不会泄漏；③ 6 次上限只要 `recordIntercept` 是唯一入口就不会分裂 |
| 实现阶段怎么验 | ① 摆 3 个近防菌朝同一投掷物，逐次读 `interceptCount`，验总共只 +1（§11 #8）；② 单菌连投 6 个，验第 6 次后方块消失且无掉落；③ 对角 7.9 格应被拦、8.1 格不应被拦，验欧氏而非切比雪夫；④ 原版箭 / 雪球 / 三叉戟 / 末影珍珠各投一次，验全部穿过（§11 #13）；⑤ **自己人和队友各投一次**，验穿过且 `interceptCount` 不变、无粒子（§11 #8 第三段，D-98）；⑥ 被拦的投掷物验「落地没有任何后续」（不爆炸 / 不写方块 / 不施加效果 / 无掉落，D-99） |

### RT-21 · 震动豆：不依附方块与世界维护

| 项 | 内容 |
|---|---|
| 风险 | ① `updateShape` 漏写 `unattached` 分支 ⇒ 邻近方块一变，陷阱凭空消失；② 与 `supportsFloating` 混用 ⇒ 置 `FLOATING = true`、模型被加 90° 旋转且 `canSurvive` 仍返回 false；③ 区块卸载 / 重载后方块或方块实体丢失；④ 活塞推动；⑤ `16-DECISIONS` §4 第 22 行的 DeployedKind 列与源码约束冲突（§1.4(b)，已关闭：取 +2）；⑥ 触发端与效果端写了两个比较符或两个 4.0（D-101） |
| 触发条件 | 在陷阱旁挖 / 放方块；区块被卸载后重载；活塞臂推向陷阱；实现者照 §4 第 22 行把 `kind` 填成 `null`（会回落到 `BLAST_CARROT`，陷阱跑成爆炸陷阱） |
| 首要缓释 | ① `unattached` 作为**第 5 个独立旗标**、与 `supportsFloating` 正交，四处各加一行早退（§3.7）；② 活塞由既有的 `PushReaction.BLOCK` 保证（`FarmstormBlocks` L86–L87），无需新代码；③ `kind` 必须填 `DeployedKind.VIBRATION_BEAN`，而 `DeployedCropBlockEntity#kindOf` 会在方块与 BE 配错时抛异常（快速失败而不是静默跑错行为）；④ 两端共用一个 config 键与一个 `<`（§3.6 末尾的两端表），实现里出现第二个字面量即为错误（D-101） |
| 兜底 | ① 若 `updateShape` 早退仍有路径漏网，把 `canSurvive` 与 `updateShape` 两道独立判据都写成恒真（已经这么写）；② 若区块卸载后确实丢失，把方块重建挂在 `ChunkEvent.Load` 上按 `placedGameTime` 兜底——但这条要先证明丢失真实发生，不预先写 |
| 实现阶段怎么验 | ① 放置后把周围 6 个面各挖一个方块，验方块仍在且 `FLOATING` 为 false（§11 #10）；② 走出 200 格再回来（强制区块卸载 / 重载），验方块与方块实体仍在；③ 用活塞臂推一次，验推不动；④ 触发一次，验「静默移除 + 无掉落」（§11 #11） |

### RT-22 · 桑吉尔蔗与刺球：伤害事件链上的挂钩

| 项 | 内容 |
|---|---|
| 风险 | ① 只取消自然坠落、忘了落地结算 ⇒ 0 伤害；② 只结算、没取消 ⇒「固定 7.5 + 自然坠落」，伤害随高度漂移，与「固定」冲突；③ 取消的作用域写大了（全局改坠落）⇒ 影响缓降 / 免疫 / 别的模组；④ 刺球的命中回调若拿不到尖牙实例，就退化成「在不在圈里」的假命中；⑤ 两个待验证 API（`LivingFallEvent` 的取消语义、`LivingDamageEvent.Post` 与 `Entity#getPersistentData()`） |
| 触发条件 | 相位 2 期间玩家从 16 格落下；玩家在相位 2 里死掉；第三方模组清了负面效果；`cancelFallDamage = false` 的对照实验；尖牙命中但伤害被无敌帧吞掉 |
| 首要缓释 | ① 取消与结算写在同一份相位状态机里（`tickFalling` 的 `onGround()` 分支），任何一处改动都同时看到两处；② 取消的判据是「效果存在 + `amplifier == 2`」，不碰全局坠落逻辑，也不碰缓降 / 免疫 / 飞行；③ 顺序固定：先结算、后 `finish`（`finish` 会摘效果，摘掉后判据失效）；④ 刺球侧以「实际伤害事件」为准（D-95） |
| 兜底 | ① 若 `LivingFallEvent.setCanceled(true)` 只影响距离而不阻止伤害，改挂 `LivingIncomingDamageEvent` 并在伤害类型为坠落时取消；② 若伤害事件侧也拿不到，退化为「取消不改、把 7.5 改成 7.5 减自然坠落伤害」——这会破坏「固定」语义，只在两条主路都走不通时使用，且必须写进验收记录；③ 刺球侧若拿不到尖牙实例，改用 `Set<Integer>` + `getPersistentData()` 标记（待验证） |
| 实现阶段怎么验 | ① 16 格高度做一次：落地瞬间血量恰好减 7.5 点（= 3.75 颗心），且日志里没有原版坠落伤害行（§11 #16）；② 关闭 `cancelFallDamage` 再落一次：伤害 > 7.5 且随高度变化；③ 刺球打中玩家一次：缓慢 V 60 刻且只施加一次，被动画扫到但没掉血的玩家不获得缓慢（§11 #15） |

### RT-23 · 数字纪律与 `tools/verify_project.py` 的缺口

| 项 | 内容 |
|---|---|
| 风险 | ① 工具仍按 0.0.1 枚举（items 22 / blocks 20 / entity 9），分母 64 与它自己数出的 62 都不等于 77 ⇒ 任何「总数」断言在修好工具之前都不成立；② config 期望值仍读 `01-DESIGN.md` 的 117 键，而该文件属 00–15 冻结区不能回改 ⇒ 工具会一直红；③ 有人为了让工具变绿去改 `01-DESIGN.md`，破坏 D-80；④ 有人凭「+13」直接宣布 90，而不去数 |
| 触发条件 | 任何人现在跑这个工具；任何人照 §4 第 22 行把 `DeployedKind` 当 +1；任何人复制粘贴「77」而不看 §1.2 |
| 首要缓释 | 本文 §1.2 已把 77 逐类数出并与 `Farmstorm#logRegistrySelfCheck` 的 12 条运行期断言对齐，因此 90 是**两条独立证据 + 一次加法**的结果，不是猜的；工具的修法逐段写在 §1.4(c) 的表里 |
| 兜底 | 若作者的意图是「工具永不改、只当红」，则 §11 #1 改为「工具保持红，但把它的输出与本表逐段对照并在验收记录里签字」——这会失去自动化价值，因此只在作者明确否决修工具时使用 |
| 实现阶段怎么验 | ① 修完后跑一次：出现 `已核 registry 对象: 77 / 77` 且 `结论：FAIL 0 项`、退出码 0；② v0.0.3 落地后再跑一次：`90 / 90`、config `139`（v0.0.3 后 `155`）、lang `263`（v0.0.3 后 `297`）；③ 每次改注册类后重跑，任一小计不符立即红 |

---

## 11. 验收自检表

每条一个具体动作 + 一个可判定的通过判据。**17 条**（#1 – #17）。

| # | 动作 | 通过判据 | 覆盖 |
|---|---|---|---|
| 1 | 修 `tools/verify_project.py` 到 0.0.2 基线（按 §1.4(c) 的四段改法），然后 `python -X utf8 tools/verify_project.py` | 输出 `已核 registry 对象: 77 / 77`、config 段 139 键无 FAIL、`lang 键集一致，共 263 键`、`结论：FAIL 0 项`，退出码 0 | D-80、RT-23 |
| 2 | 起服，读 `latest.log` 的注册对象自检行与作物自检行 | 注册对象 **90** 且逐类为 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1；作物 **22** 种、投掷型 12 / 部署型 7 / 食用型 3、破障类 4；幸运浆果池 **19** 种；三行都以 `OK` 结尾 | §2.4、D-97 |
| 3 | `qbounce.poseMode = render`，对一只僵尸投朋友的韭 | 僵尸呈乘坐姿势（腿部下压）、压低 0.35 格、持续旋转（约 480 度/秒，即 24 度/刻 × 20 刻/秒；一圈 15 刻）、按 6 刻周期压缩拉伸；AI 仍在走（位置继续变化）、碰撞箱不变 | D-81、D-82、RT-17 |
| 4 | `qbounce.poseMode = mount`，同一只僵尸 | 同上的观感；实体数据里能看到它骑在一个 ArmorStand 上；僵尸死亡后世界里不再有该 ArmorStand | D-82、RT-18 |
| 5 | 对玩家施加 Q弹，按 W / A / 空格 / 潜行 / 左键 / 右键 / 滚轮 / 数字键各一次 | 全部无响应；ESC 仍能开暂停菜单；锁定日志打印一次（Q弹 复用同一段七段锁，不新增第二套） | D-60、D-79、D-81 |
| 6 | 对玩家施加混乱，按 W 再按 A | W 向后走、A 向右平移，**速度幅度与正常一致**（用 F3 的坐标逐刻差算）；视角鼠标转向正常、跳跃与潜行正常、左键能打到人 | D-83 |
| 7 | 先施加 Q弹 再施加混乱（或反之），按 W | 玩家完全不动（位移为 0）；锁的日志行已打印；效果结束的下一 tick 恢复反向移动 | D-83、RT-19 |
| 8 | 三段一起做：① 摆 1 个近防菌连投 6 个朋友的韭；② 再摆 3 个近防菌围同一投掷物投 1 个；③ 让**放置者本人**与**同队队友**各投 1 个 | ① 第 6 次拦截结束瞬间方块消失且**无掉落物**，日志里 `interceptCount` 逐次为 1..6，被拦的投掷物落地无任何后续（不爆炸 / 不写方块 / 不施加效果，D-99）；② `interceptCount` 总共只 +1，另外 2 个菌没有粒子；③ 两次投掷都**穿过**（落点照常结算），`interceptCount` **不变**、无粒子——己方不拦也不扣（D-98） | D-87、D-88、D-98、D-99、RT-20 |
| 9 | 单菌在同一刻面对 2 个投掷物 | 只有 1 个被拦，`interceptCount` 单次只 +1；日志里「本刻已拦过」分支被走到至少一次 | D-88、RT-20 |
| 10 | 放置震动豆陷阱后，把东西南北上下 6 个相邻方块各挖掉一个 | 方块仍在原位；方块状态里的 `FLOATING` 始终为 false；走出 200 格再回来（强制区块重载）后仍在 | D-91、RT-21 |
| 11 | 玩家（非己方）走到距震动豆 3.9 格 / 4.1 格各一次 | 3.9 格触发（4 格内所有玩家获得 80 刻混乱 + 一次爆炸音效）；4.1 格不触发；触发后方块静默消失、无掉落 | D-92 |
| 12 | 朋友的韭：一次命中 1 只生物、一次空投 | 命中：6 格内全部生物获得 200 刻 Q弹，落点播一次音，32 格内可闻、33 格外不可闻；空投：范围外生物不获得效果、**不播音** | D-85 |
| 13 | 对近防菌投原版箭 / 雪球 / 三叉戟 / 末影珍珠 / man瓜导弹各一次，再投一支朋友的韭 | 5 个全部穿过且 `interceptCount` 不变；朋友的韭被拦且 `interceptCount` +1（证明判定面是基类而不是「什么都不拦」） | D-87、§6.1、§3.4 边界 |
| 14 | 满血玩家站在 8 格内，让 6 个投掷物被拦 | 血量、饥饿、坐标逐点不变；方块与方块状态无变化；日志里没有 `Explosion` 构造行 | D-89 |
| 15 | 刺球：按 D-94 的 8 行逐项验 | 3.0 格不触发、2.5 格触发；内圈 5 个先出（第 2 刻）、外圈 8 个后出（第 4 刻），两圈间隔 2 刻；被尖牙**实际造成伤害**的玩家获得缓慢 V（amplifier 4）60 刻且只施加一次；被动画扫到但没掉血的玩家**不**获得缓慢 | D-93 ~ D-95、RT-22 |
| 16 | 桑吉尔蔗：默认 16 格拉升后自然下落；再关掉 `cancelFallDamage` 落一次 | 默认：落地瞬间恰好减 7.5 点，日志里没有原版坠落伤害；关掉后：伤害 > 7.5 且随高度变化；死亡消息显示 `death.attack.farmstorm.zangief_slam`，归属陷阱所有者 | D-96、RT-22 |
| 17 | 资源与语言键计数自洽 | `textures/` 下新增 PNG 恰好 8 张且与 §9.1 逐条同名（其中 4 张是 `friend_chives_stage0..3`，**洋红 #D81E8A 占位，非终稿**）；`sounds/` 下新增 OGG 1 个；`sounds.json` 事件数 3；`zh_cn.json` 与 `en_us.json` 各 297 键且键集逐字相等；新增 JSON 文件 22 个且与 §9.3 逐条同名 | §9.1 – §9.4 |

**覆盖声明**：17 条里，注册与 config 数字 2 条（#1 / #2）、Q弹 两分支 3 条（#3 / #4 / #5）、混乱 2 条（#6 / #7）、近防菌 4 条（#8 / #9 / #13 / #14）、震动豆 2 条（#10 / #11）、朋友的韭 1 条（#12）、刺球 1 条（#15）、桑吉尔蔗 1 条（#16）、资源与语言键 1 条（#17）。七条 RT 全部被至少一条覆盖：RT-17 → #3；RT-18 → #4；RT-19 → #7；RT-20 → #8 / #9；RT-21 → #10；RT-22 → #15 / #16；RT-23 → #1 / #2。
