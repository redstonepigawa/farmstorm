# 28 号 v0.1.1 决策记录（Decision Record）

> 项目：农场风云 / Farmstorm · v0.1.1
> 依据：`plan0.1.1.txt`（5 条原文）+ `.work/v011-BRIEF.md` §2 的 25 条作者裁定
> 地位：**唯一真相源**。与本文冲突时以本文为准；本文与源码冲突时报给作者，不得自行取舍。
> 覆盖：**D-124 ~ D-147，共 24 条**，连续无跳号。对 0.1.0 的增 / 改 / 删登记在 §3，红线口径变更在 §4，与被取代的 0.1.0 条款的对照在 §5。

---

## 0. 阅读约定

### 0.1 编号

本文的决策编号从 **D-124** 起（0.1.0 的 `22-DECISIONS-0.1.0.md` 到 D-123）。其余编号系列本文**只引用、不新开**，接续表照抄 `BRIEF` §0.2：

| 系列 | 0.1.0 到 | **本文从** | 由谁写 |
|---|---|---|---|
| 决策 D-xx | D-123 | **D-124** | 本文（28 号） |
| 任务 F-xx | F-175 | **F-176** | 31 号 |
| Phase | 25 | **26** | 31 号 |
| 里程碑 M-xx | M21 | **M22** | 31 号 |
| 硬约束 HC-xx | HC-76 | **HC-77** | 32 号 |
| 禁止行为 N-xx | N-74 | **N-75** | 32 号 |
| 风险 RT-xx | RT-30 | **RT-31** | 30 号 |
| 裁决 A-xx | A-29 | **A-30** | 31 号 |
| 实现判定 V-xx | V-190（`.work/PLAN.md`） | **V-191** | `.work/PLAN.md` |
| 文档号 | 27 | **28** | 本文 |

三条编号规则：

1. 本文只发 **D-xx**。引用其它系列时写全前缀（「RT-33」「HC-77」「F-176」），不自己编号、不预支下游编号。
2. 接续表来自 `BRIEF` §0.2，本文不重算、不另立。
3. 本文覆盖 **D-124 ~ D-147**。0.1.0 的 D-103 ~ D-123 仍以 `22-DECISIONS-0.1.0.md` 为准，**除非 §5 点名取代**；§5 没点到的条款一律继续有效。

### 0.2 术语表

本节给每个术语**判定边界**，不是同义词循环。下游六份文档遇到这些词，一律按本节的边界用。

**man瓜**
：等于 **`farmstorm:pumpkin_turret`**（物品 / 方块 / 方块实体同 id）。判定边界：只指这一个注册项。含「瓜」的另外三个对象——`melon_grenade`（瓜！）、`melon_slice_bomb`（瓜片子）、`melon_grenade_stem`（瓜！茎）——**都不是** man瓜。文档里一律写「man瓜（`farmstorm:pumpkin_turret`）」，不许用「南瓜炮台」之类别名替代 id（`BRIEF` §1.7、§6 第 5 条；裁定 #4）。

**据点核心**
：等于 **`farmstorm:outpost_core`** 的**已放置方块实例**及其方块实体。判定边界：说「核心」时指世界里的那一格方块，不指物品形态（物品形态一律写「核心物品」）；「核主」指放置者 UUID，与「当前在线的玩家」不是一回事（核主可以离线）。依据 `BRIEF` §1.2。

**己方**
：等于「**核主本人 + 同队队友**」，队伍口径沿用原版计分板队伍。判定边界：按 **UUID / 名字**判，**不看目标是否在线**；生物恒按「非己方」处理（生物没有队伍归属）；创造 / 旁观在索敌与触发面上另有既有排除，但那些排除**不属于**「己方」的定义。依据裁定 #17、`BRIEF` §1.3 的 bug 2 现场。

**核心容量**
：一个核最多能管理的耕地块数，取值 = `outpost_core.farmlandBonus`（默认 **16**）。判定边界：容量是**绑定名额**，单位是「块地」；它不等于「额度」——额度是「己方全体能开垦的总块数」，本版由容量求和得出（D-139）。两者同源（同一个 config 键）但不同义，写文档时必须分开说。依据裁定 #12、`BRIEF` §3.4、RT-33。

**耕地绑定**
：一块耕地与**唯一一个**核之间的持久关系。判定边界：绑定的对象是「核的坐标 + 维度」，不是核主；一块地同一时刻只有 **0 个或 1 个**绑定，**不存在 2 个**——这是与 0.1.0「范围覆盖」的根本差别（0.1.0 下一块地可以同时落在多个核的 3×3 里）。没有绑定的耕地仍然存在、仍然能种，只是**不受保护**。依据裁定 #12、#15。

**迁移**
：核被毁时，把它名下的耕地**改绑**到另一个核。判定边界：迁移的是**绑定关系**，不是方块本身；只有「接替核的影响范围覆盖该耕地 + 与耕地主同盟 + 接替核还有空位」三条同时成立才迁移，一条不成立就**变泥土**（这是唯一会动世界的分支）。依据裁定 #14。

**Q弹**
：等于状态效果 **`farmstorm:q_bounce`**（`HARMFUL`、`0xFF6EC7`）。判定边界：只指这个 MobEffect；不指任何视觉表现、不指弹跳高度、不指「七段输入锁」；本版不改它的 amplifier（仍为 0）、不改它不重写 `tick`、不读 config 的既有事实。依据 `BRIEF` §1.8。

**浮窗**
：等于「**屏幕左上角的自绘核心列表**」这一个元素。判定边界：它是本模组自己画的（`RenderGuiEvent.Post`），可开关、可拖拽；**不是**原版 Boss 条、**不是**记分板侧边栏、**不是**聊天栏、**不是** `WildHunterHud` 那条蓄力条。它显示自己 + 队友的**全部**核——**含未加载区块、含其它维度**（D-143），不显示敌对核、不显示核心物品。依据裁定 #19 ~ #21。

### 0.3 本文件的四字段写法

每条决策固定四项，**缺一不可**，顺序固定：

| 字段 | 写什么 | 不许写什么 |
|---|---|---|
| **结论** | 一句话、祈使句、可直接执行 | 条件句、备选句、「可以考虑」 |
| **依据** | `BRIEF` §2 的裁定编号，或 §1 的 `文件:行号`；两者都给最好 | 「因为合理」「符合直觉」「业界惯例」 |
| **落地位置** | 要改的 `路径` 或 `类#方法` | 代码片段、算法步骤 |
| **补充** | 这一条**连带会影响什么**、**否决掉了哪些替代方案** | 实现细节（那是 30 号的事）、任务排期（那是 31 号的事） |

四条写作纪律：

1. **「本文补充」标记**：凡由本文推导、而非 `BRIEF` 直接给出的口径，在该句前标 **本文补充**。带这个标记的条目是可以被作者否决的；不带标记的条目是 `BRIEF` 的转写，否决它等于否决作者裁定。
2. **数字纪律**：本文所有数值只能追到 `BRIEF` §1 / §2 / §3 的某一条，或是两个已给数值的算术（如 25 − 1 = 24、0.45 × 20 = 9）。**需要新数值的**，写成「由 29 号在本文件 D-xxx 的框架内定，受 config 的 int / double 值域约束」，本文不自己编。
3. **禁用词**：不出现未定事项、待办标记、条件含糊的结论；不写第二人称敬语，一律用「作者」。
4. **与源码的关系**：本文与源码冲突时报给作者，不自行取舍。写作期间发现的冲突与 `BRIEF` 未覆盖处，逐条列在文末的「写作期回报」（见 §7 末）。

---

## 1. 决策清单（D-124 ~ D-147）

本版实际编号 **D-124 ~ D-147，共 24 条**，连续无跳号。分组：§1.1 版本与材质 3 条、§1.2 man瓜 3 条、§1.3 压缩干草块 1 条、§1.4 朋友的韭 3 条、§1.5 耕地与据点核心 8 条、§1.6 浮窗与运行期 4 条、§1.7 红线与发布面 2 条。

### 1.1 版本与材质（D-124 ~ D-126）

#### D-124 · 版本号 0.1.1 与交付形态：六份中文 + 六份英文 + README 增量

- **编号**：D-124
- **结论**：把 `mod_version` 改为 **0.1.1**；本版交付**六份中文（28 ~ 33）+ 六份英文（对应 28 ~ 33）+ README 增量**，不新开第七份文档，也不改动 22 ~ 27 号。
- **依据**：`BRIEF` §2 第 1 条（0.1.1 / 文档号 28~33 / 编号接续见 §0.2）；`gradle.properties:11` 现为 `mod_version=0.1.0`（`BRIEF` §1.10）；`BRIEF` §5 的六份分工表与「中文全冻结 → 统一翻英文」的顺序。
- **落地位置**：`gradle.properties:11`；`docs/zh/28-DECISIONS-0.1.1.md` ~ `docs/zh/33-PROMPT-TASKS-0.1.1.md`（6 份新增）；`docs/en/28-DECISIONS-0.1.1.md` ~ `docs/en/33-PROMPT-TASKS-0.1.1.md`（6 份新增）；`README.md`（增量段，含红线段，见 D-146）。
- **补充**：英文六份是中文的译本，**不是第二真相源**——与中文冲突时以中文 28 号为准。版本号只有 `gradle.properties:11` 一处来源，其余文档只引用、不各自声明（否则 D-147 的 jar 名会与版本号脱钩）。0.1.0 的 22 ~ 27 号继续冻结（`BRIEF` §6 第 3 条），本版不给它们打补丁。否决「本版把六份合成一份大文档」：`BRIEF` §5 已经把 28 ~ 33 的分工切开（真相源 / 设计 / 技术 / 任务 / 主提示词 / 阶段提示词），合并会让「只引 28、不自己发明」这条约束失效。

#### D-125 · 「绿色 +」规则全局作废，但本版只重生成 1 张

- **编号**：D-125
- **结论**：把「物品贴图右下角叠绿色 `+`」这条规则**全局作废**，并从生成器里删掉叠图机制；**本版只重生成 `wild_apple` 一张**，其余 24 张保持现图不动。
- **依据**：`BRIEF` §2 第 2 条；规则原文 `plan0.0.1.txt:8`、作废声明 `plan0.1.1.txt:10`；承载点 `tools/gen_textures.py:274-275`（`NO_PLUS_ICONS = {"converter"}`，26 条里只有它免叠）、`:407`（`add_plus`）、`:546-560`（`build_marker()` 画 7×7 十字共 33 px）、`:612-623`（贴到右下角 `left=9 / top=9`）、`:974-975`（合成分支）；Java 侧 **0 处**（无 `ItemDecorator` / `overrides` / 贴图叠加，`BRIEF` §1.1）；现图实测本身已带 33 px 的 `#3FA34D` 十字（`BRIEF` §1.1）。
- **落地位置**：`tools/gen_textures.py`（`NO_PLUS_ICONS` / `add_plus` / `build_marker` 与合成分支）；`assets/farmstorm/textures/item/wild_apple.png`。
- **补充**：作废是**规则层面**的，不是「本版所有图标都不带 +」。26 条里 25 张带 `+`（只有 `converter` 免叠），本版处理后**仍有 24 张是旧样**（25 − 1 = 24），登记在 §7 第 1 条。生成器是唯一承载点，所以删机制之后重跑不会把 `+` 加回来——这也是本条必须动代码、不能只换 PNG 的原因。不选「本版一次重生成 25 张」：裁定 #2 只承诺 1 张，批量重生成属下一版；一次改 25 张图会让「本版视觉变化」这件事无法逐张验收。

#### D-126 · 狂野苹果改 64×64 原图直用

- **编号**：D-126
- **结论**：`textures/item/wild_apple.png` 直接换成那张 **64×64** 源图的原样输出（**不降采样、不再走 16 网格众数取色**）；生成器的默认源图路径改为该文件。
- **依据**：`BRIEF` §2 第 3 条；`plan0.1.1.txt:9-10`；§1.1 实测——源图 `wild_apple_64x64.png` **6,946 B / 64×64 / 1814 个去重 RGBA / 1471 全透明 / 2625 不透明 / 0 半透明 / alpha bbox x∈[2,60] y∈[0,63]**，且**不是**任何 16×16 图的 4× 最近邻放大（256 个 4×4 块里 204 块不均匀）；现图 **571 B / 16×16 / 88 个去重 RGBA / 107 全透明**；生成器 `tools/gen_textures.py:59`（`TEXTURE_SIZE = 16`）、`:154`（`WILD_APPLE_SOURCE` 默认指向桌面旧图）、`:849-881`（`wild_apple_grid()` 16 网格众数取色）、`:884-895`（`draw_wild_apple()`）；模型 `models/item/wild_apple.json:1-6`（`parent=minecraft:item/generated`、`layer0=farmstorm:item/wild_apple`、无 overrides）。
- **落地位置**：`assets/farmstorm/textures/item/wild_apple.png`（就地替换，§3.2 第 4 行）；`tools/gen_textures.py:154`（默认源图）+ `:884-895` / `:849-881`（`wild_apple` 改直通分支，不再进 16 网格取色）。
- **补充**：模型的 `layer0` 引用、物品 id、语言键、注册面一律不动，也不新增第二张贴图。必须写进 29 / 30 的渲染事实：**物品栏里显示面积仍是 16×16**，64×64 只是像素密度，高出 4 倍的细节会被显示尺寸平均掉（RT-37）；重跑生成器不得把这张图换回 16×16，所以生成器与图必须同版改。否决「继续 16 网格取色 + 手工补细节」：现图那 88 色正是这套取色的产物，作者要的是原图；也否决「先缩到 32×32 折中」：`plan0.1.1.txt:10` 的原话是「直接将原图作为材质图」。

### 1.2 man瓜（D-127 ~ D-129）

#### D-127 · man瓜 身份固定为 `farmstorm:pumpkin_turret`，攻击间隔 60 → 40

- **编号**：D-127
- **结论**：文档里 `man瓜` 一律指 **`farmstorm:pumpkin_turret`**；`pumpkin_turret.fireIntervalTicks` 默认值 **60 → 40 刻**（2 秒）。
- **依据**：`BRIEF` §2 第 4、5 条；`plan0.1.1.txt:26`（「默认攻击间隔缩短为2秒」）；§1.7——`zh_cn.json:57/58/287/288` 的中文字面量就是「man瓜」，含「瓜」的对象共 4 个，`plan0.0.1.txt:91-97` 的原始需求也写作「man瓜 / 对应原版作物：南瓜」；`config/FarmstormConfig.java:431-433`（默认 60，范围 1–72000）；读取 `PumpkinTurret.java:80-82`；生效 `TurretBehavior.java:26-32` 与 `DeployedCropBlockEntity.java:146-149`；扫描间隔 `TurretBehavior.java:13-16` 默认 10 刻。
- **落地位置**：`config/FarmstormConfig.java:431-433`（默认值）；`behavior/impl/PumpkinTurret.java:80-82`（读 config，不动）；文档面：28 ~ 33 全文的身份写法。
- **补充**：间隔是**冷却**语义，冷却期内连索敌都不做（`DeployedCropBlockEntity.java:146-149`），所以 40 刻会连带提高索敌频率与炮弹总量——这正是 D-128、D-129 必须一起调的原因（间隔缩短后伤害与弹速不改，等于单方面加强）。40 是**默认值**，不是硬性：已落盘的 `farmstorm-server.toml` 仍是 60，升级须知见 D-147。否决「把 man瓜 写成『南瓜炮台』而不注 id」：`BRIEF` §6 第 5 条明令禁止，且同名的「南瓜」对象在模组里不止一个。

#### D-128 · man瓜 伤害 1 → 3

- **编号**：D-128
- **结论**：`pumpkin_turret.projectileDamage` 默认值 **1 → 3**；伤害类型仍是 `farmstorm:void_bolt`，击退等级 `knockbackLevel` 默认 1 不动。
- **依据**：`BRIEF` §2 第 6 条；`plan0.1.1.txt:26`（「伤害为3点」）；§1.7——`config/FarmstormConfig.java:425-427`（默认 1，范围 0–100）、`FarmstormDamageTypes.java:20`（`void_bolt`）、施加点 `TurretBullet.java:106`、击退 `:110-117`（`knockbackLevel` 默认 1，`FarmstormConfig.java:428-430`）。
- **落地位置**：`config/FarmstormConfig.java:425-427`（默认值）。
- **补充**：3 是**每一发**的伤害，不是 DPS。与 D-127 的间隔一起看：改前 1 点 / 60 刻、改后 3 点 / 40 刻，按刻折算的理论输出是 **4.5 倍**（(3 ÷ 40) ÷ (1 ÷ 60) = 4.5，**本文补充**：算术，两个数都出自裁定 #5 与 #6）。所以 29 号的数值表必须把「间隔 + 伤害 + 弹速」三行写在一起，不能只写伤害，否则读者会以为只加强了 3 倍。否决「伤害改成 3 但保留 60 刻间隔」：与裁定 #5 的 2 秒直接冲突。

#### D-129 · 炮弹速度 0.6 → 0.45 提成 config，并修近距离绕圈（**不引入软追踪**）

- **编号**：D-129
- **结论**：
  1. 删掉 `TurretBullet.SPEED = 0.6` 这个硬编码常量，改为读新 config 键 `pumpkin_turret.bulletSpeed`（double，默认 **0.45**，即每秒 9 格）；
  2. 追踪**只做近距离收敛**修复：目标距离小于一个步长（`dist < SPEED`）时按剩余距离收敛或直接判中，消除贴脸时每刻掉头造成的绕圈 / 抖动；
  3. **不引入软追踪**：不加转向角上限、不加最大追击时长、不加制导参数、不换实体父类。
- **依据**：`BRIEF` §2 第 7、8 条；`plan0.1.1.txt:27`；§1.7——`TurretBullet.java:28`（`SPEED = 0.6` 硬编码、无 config）、`:73-87`（服务端且目标存活时每刻 `steerTowards`）、`:90-96`（归一化 × `SPEED`，**每刻直接重设**、无转向角上限、无最大追击时长）、`:43-51`（两个构造器都 `setNoGravity(true)`）、`FarmstormEntityTypes.java:60-66`（注册处 `clientTrackingRange(4)` / `updateInterval(10)`）；`BRIEF` §3.4（新键 `bulletSpeed` 默认 0.45）；同仓 `homing` 0 命中、无实体继承 `ShulkerBullet` / `AbstractHurtingProjectile`（§1.7 末段）。
- **落地位置**：`entity/TurretBullet.java:28`（删常量）、`:90-96`（近距收敛）；**`behavior/impl/PumpkinTurret.java:75`**（发射点：`bullet.setDeltaMovement(direction.scale(TurretBullet.SPEED))` 改读同一个 config 键）；`config/FarmstormConfig.java` 的 `pumpkin_turret.bulletSpeed`（`BRIEF` §3.4，新键）；语言键 `farmstorm.configuration.pumpkin_turret.bulletSpeed`（`BRIEF` §3.5）。
- **补充**：**这是与 plan 原文的一处正面冲突**——plan 要「参考原版潜影贝导弹的追踪效果，最好可以直接代码复用」（`plan0.1.1.txt:27`），裁定 #8 把它改判成「不引入软追踪，只修近距离绕圈 / 抖动」。所以「复用 `ShulkerBullet`」被否决（§6 第 8 条）。两个改动必须同时做：只降速会把「打不中」变成「更慢地打不中」；只修收敛不改速，则在正常距离上仍是最初那套硬编码步长。**删常量不是单点改动**：这个常量有两处读者——`TurretBullet.java:90-96` 的每刻重设与 `behavior/impl/PumpkinTurret.java:75` 的出膛初速，只改前者会直接编译失败；两处必须同读 `pumpkin_turret.bulletSpeed`（A-30）。速度提成 config 不改变服务端权威——追踪之路仍只在服务端（`TurretBullet.java:23-25` 的类注释，HC-03）。速度的值域由 30 号在 `config` 的 double 值域内定死，本文只给默认 0.45。

### 1.3 压缩干草块（D-130）

#### D-130 · 压缩干草块音效 GRASS → STONE

- **编号**：D-130
- **结论**：`compressed_hay_block` 的 `SoundType` 由 `GRASS` 改为 **`STONE`**；硬度 30 / 抗爆 6 / `requiresCorrectToolForDrops()` / `MapColor.COLOR_BROWN` 一律不动。
- **依据**：`plan0.1.1.txt:20-23`（第 3 条原文「把压缩干草块的音效改为石头类的音效」）；`BRIEF` §3.2 第 3 行（`FarmstormBlocks.java:79` `SoundType.GRASS` → `STONE`）；§1.6——全仓 `SoundType` 只出现 3 个值（`GRASS` `:79` / `STONE` `:88`、`CROP` `:100,:110`），`STONE` 的唯一先例就是 `outpost_core`。
- **落地位置**：`registry/FarmstormBlocks.java:79`。
- **补充**：音效是**方块属性**，影响**破坏 / 放置 / 踩踏 / 挖掘 / 落地**五类声音（`SoundType` 的五个 getter：`getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound`），不影响**挖掘时长**、掉落与硬度；**因此验收项比原估多 2 条**（29 号已把验收项从 3 条加到 5 条，两边要对得上）（A-31）。连带：`22` 号 §7.2 的 D-57 行写着「`sound(GRASS)` 不动」，本版作废这半句（§3.2 第 11 行），但 22 号原文冻结不改。⚠️ **25 条裁定里没有这一条**——依据只有 plan 原文与 `BRIEF` §1.6 / §3.2，本文把它补成独立决策，否则六份下游文档会整体漏掉 plan 的第 3 条需求（见 §7 末的写作期回报第 1 条）。否决「改成 `CROP`」：`CROP` 是作物方块的音效，与「石头类」正相反。

### 1.4 朋友的韭（D-131 ~ D-133）

#### D-131 · 朋友的韭从「落地一次性」改为 effectTicks 刻持续场（默认 200）

- **编号**：D-131
- **结论**：`FriendChivesProjectile` 落地后改为**持续 `friend_chives.effectTicks` 刻的场**（默认 **200**，值域 20–1200）：重写 `keepAliveAfterImpact()` 返回 `true`；新增 `tick()`，**每刻**扫描落点半径 6 格球体（欧氏距离、含高低差）内的 `LivingEntity`，对每个目标施加 **`effectTicks − x`** 刻 Q弹（x = 落地后经过的刻数）；第 `effectTicks` 刻结束时由该实体自己 `discard()`。
- **依据**：`BRIEF` §2 第 9 条；`plan0.1.1.txt:31`；§1.8——`ThrownCropEntity.java:95-104`（`settle()` → `onImpact()` → **`!keepAliveAfterImpact()` 就 `discard()`**）、`:116-118`（默认 `false`，唯一重写者是爆竹）、`:65-72`（**一旦 settled，寿命上限不再生效**）；`FriendChivesProjectile.java:56-80` 现为落地一次性、全类**没有 `tick()` 重写**；`friend_chives.hitRadius` 默认 **6.0**（0–16，`FarmstormConfig.java:851-853`）、`effectTicks` 默认 **200**（20–1200，`:854-856`）；Q弹 `effect/QbounceEffect.java`（`farmstorm:q_bounce`，amplifier 字面量 0，`FriendChivesProjectile.java:63`）。
- **落地位置**：`entity/thrown/FriendChivesProjectile.java`（`keepAliveAfterImpact` + 新增 `tick()`）；`entity/ThrownCropEntity.java`（只在既有钩子上配合，不新开第二种生命周期）。
- **补充**：**必须自己结束**——`:65-72` 的寿命检查带 `!settled` 条件，落地之后没有任何机制会回收这个实体，漏了就是实体泄漏（RT-35）；第 `effectTicks` 刻的 `discard()` 是本条的硬性一部分，不是可选项。场不筛敌我：投掷者本人站进球内同样中招（`FriendChivesProjectile.java:51-53` 的既有注释，本版不改），29 号要把它写进边界清单。每刻施加不会无限续期：每次写的是 `effectTicks − x`，比上一刻少 1 刻，效果随场走完自然终止——这也是这个写法的意义（若每刻都写满 `effectTicks`，第 2 刻起就会永久续期）。**200 只是默认值，不是常量**：场长与施加基准都取同一个 `friend_chives.effectTicks`（值域 20–1200，`FarmstormConfig.java:854-856`），否则把配置改成 100 就会出现「场跑 200 刻、每人只拿 `100 − x`」的自相矛盾（A-33）。一键两义，30 号要在 config 注释里写明。同时存在的场数量受投掷物寿命限制（RT-34）。否决「用一个方块实体承载场」：那要给 `friend_chives` 新增 `block_entity_type`，注册面就不再是 +0（§3.4）。

#### D-132 · 朋友的韭音效仍只在落地那一刻判定

- **编号**：D-132
- **结论**：音效判定留在**落地那一刻**：球内 ≥1 个生物就播**一次** `friend_chives`，否则**永不播**（空投不播）；200 刻内后走进球内的生物**不补播**。
- **依据**：`BRIEF` §2 第 10 条；`plan0.1.1.txt:31`（「只播一次、空投不播」）；§1.8——`FriendChivesProjectile.java:71-73`（`if (applied < 1) return;` ⇒ 空投不播、命中 ≥1 才播、只播一次）、`:74-76`（`volume = friend_chives.soundRange / 16.0` = 32.0 ÷ 16 = **2.0**、`SOUND_PITCH = 1.0F`、`playSound(null, x, y, z, …, PLAYERS, volume, pitch)`）；`friend_chives.soundRange` 默认 **32.0**（0–64，`FarmstormConfig.java:857-859`）。
- **落地位置**：`entity/thrown/FriendChivesProjectile.java`（落地回调里的播音分支保留，施加逻辑同时迁到 `tick()`，两者从此分离）。
- **补充**：音量 2.0 与「32 格可闻」的关系不变：32 ÷ 16 = 2.0（§1.8）。`sounds.json` 是否再补 `"attenuation_distance": 32` 属技术面（`sounds.json:16-22` 未设该键、走原版默认 16，而同文件 `:6` 的 `wild_hunter_loop` 已有 32 的先例），由 30 号核，**不改变本条结论**；这条移交登记在 §7 第 5 条。否决「场持续期间按间隔重复播」：与「只播一次」直接冲突，也会把 10 秒的场变成噪音源（200 刻 = 10 秒，**本文补充**：200 ÷ 20）。

#### D-133 · 朋友的韭落地实体存活 200 刻且保持可见

- **编号**：D-133
- **结论**：落地实体**存活满 200 刻且保持可见**：落地后置零速度、保持无重力、免碰撞；第 200 刻自毁；区块卸载即结束该场（不写 NBT 续命、不跨维度恢复）。
- **依据**：`BRIEF` §2 第 11 条；§1.8（`ThrownCropEntity` 的 `setNoGravity(true)` 在 `:43-51`）；RT-35（重力 / 碰撞 / 区块卸载三条风险与缓释）。
- **落地位置**：`entity/thrown/FriendChivesProjectile.java`（落定后的物理收敛与自毁时机）；渲染沿用既有投掷物渲染器，客户端不改。
- **补充**：可见性是「实体存在 + 既有渲染器」的副产品，不新增模型、贴图或渲染器。免碰撞 + 置零速度是为了让可见的落点不被水流 / 活塞推走——它是这个场的视觉锚点，动了就与 plan 原文「落点处」不符。区块卸载即结束该场（RT-35 的缓释），因此跨区块追人不是本版的语义，29 号要把它写成边界而不是缺陷。否决「落地即隐身、只留效果」：裁定 #11 明确要求「保持可见」，看不到就没有「场在持续」的反馈。

### 1.5 耕地与据点核心（D-134 ~ D-141）

#### D-134 · 耕地-核心改为绑定制，每核容量复用 `farmlandBonus`

- **编号**：D-134
- **结论**：耕地与核的关系从「范围覆盖」改为**绑定**——每块耕地同一时刻只归 **1 个**核管理；一个核最多管 **16 块**；这个 16 **直接复用** `outpost_core.farmlandBonus`（默认 16），**不新增 config 键**。
- **依据**：`BRIEF` §2 第 12 条；`plan0.1.1.txt:14`（多核重叠误铲的 bug）；§1.3 的 bug 1 现场——`OutpostCoreBlockEntity.setRemoved`(`:391-399`) → `settleOnRemoval`(`:315-335`) → `doomedFarmland`(`:353-366`) → `collectFarmland`(`:369-379`)，判定三条为「本人在账」(`:355`) + 「队友一路」遍历 `server.players()`(`:356-364`) + 3×3 切比雪夫(`:371-373`)，**没有任何「这块地是否还被别的核覆盖」的检查**，世界写入只有 `:331` 一行 `setBlock(DIRT)`；`outpost_core.farmlandBonus` 默认 16（§1.2 / §3.4）；RT-32（存档格式升版）、RT-33（一键两义）。
- **落地位置**：`farmland/FarmlandLedger.java`（新增「耕地 → 核」绑定表与反查；`byOwner` 改为由它派生）；`blockentity/OutpostCoreBlockEntity.java`（容量判定）；`farmland/FarmlandGuard.java`（判据改读绑定表）。
- **补充**：`byOwner` **不再单独落盘**——「谁的田」和「哪块地归哪个核」若各存一份就是双源，绑定表是唯一真相，`byOwner` 只在内存里派生（**本文补充**）。`farmlandBonus` 从此**一键两义**：既是每核容量、又是额度基数（D-139 的 16 × 核数），改它会同时改两处，30 号必须在 config 注释里写明（RT-33）。否决「新增 `outpost_core.farmlandCapacity` 另立一个键」：那会让「16 块」与「+16 额度」各有一个旋钮，两个值一旦不同就没人能回答「这块地算不算超额」——裁定 #12 明确要求复用。存档格式与老档兼容见 D-137（RT-32），核被毁的清算见 D-136。

#### D-135 · 绑核规则：最近优先，平手按先放置

- **编号**：D-135
- **结论**：锄地成功那一刻，把这块耕地绑给**距离最近的己方核**；距离相同按**先放置**；候选核必须**已有覆盖该耕地的 3×3 影响范围**，且**仍有容量**。
- **依据**：`BRIEF` §2 第 13 条；`plan0.1.1.txt:14`；§1.3——锄地闸 `event/FarmstormGameEvents.java:194-217`（`:206` `ledger.add(...)`）、`FarmlandGuard.check`(`:69-75`) 的三态 `Verdict`（`ALLOW` / `NO_CORE` / `QUOTA_FULL`，`NO_CORE` 的定义就是「不在己方核心的 3×3 内」）；§1.2（3×3 影响范围与 `inRange`）。
- **落地位置**：`farmland/FarmlandGuard.java:69-75`（`check` 增加「选核 + 容量」判定）；`event/FarmstormGameEvents.java:194-217`（放行后写绑定）；`blockentity/OutpostCoreBlockEntity.java`（3×3 与容量查询）。
- **补充**：**范围闸保留**——0.1.0 的 D-118「只在己方核 3×3 内可耕地」没有被任何裁定作废，裁定 #13 回答的是「绑给哪个核」，不是「能不能耕」；这条「两道闸叠加」是**本文补充**，两处原文的合并读法（§5 第 3 行已登记，冲突登记见「写作期回报」第 2 条）。**「平手按先放置」的实现口径**：一律以 **`placedGameTime` 升序**为准（该字段已落盘，`OutpostCoreBlockEntity.java:79`、`:405`），**同一刻再按核坐标字典序**（完整比较键：`placedGameTime` → 维度 id → 坐标（A-39）；最后一段只用于跨维度同坐标的极端平手）。`OutpostCoreIndex.byOwner` 是 **`HashMap`**（`:53`），跨 owner 的迭代顺序不稳定，因此**不得依赖任何 Map / 列表的插入顺序**（A-39）。`placedGameTime` 的取值载体（索引侧缓存或等价做法）由 30 号定死，前提是**不为比较而加载候选核所在区块**（与 `isFarmlandLoaded` 同一条口径，`OutpostCoreBlockEntity.java:382-385`）。`Verdict.QUOTA_FULL` 的含义从「队伍额度用满」变为「范围内最近的己方核容量已满」，三态结构不变；提示语言键是否改名由 30 号定（不改键代价更小）。否决「绑给最早放置的核」：裁定 #13 的第一顺位是距离，先放置只是平手时的次序。

#### D-136 · 核被毁时的迁移规则

- **编号**：D-136
- **结论**：核被毁时先把它名下的耕地**迁移**给接替者，**迁不动才变泥土**；接替者必须同时满足「影响范围覆盖该耕地 + 与耕地主同盟 + 尚有容量」，多个候选时**最近优先**；一个都没有才 `setBlock(DIRT)`。
- **依据**：`BRIEF` §2 第 14 条；§1.3 的 bug 1 现场（现实现直接清算，`OutpostCoreBlockEntity.java:315-335`）；§1.2（破坏回调只有 `OutpostCoreBlock.java:101-107` 的 `onRemove → core.release()`）；`plan0.1.1.txt:14`。
- **落地位置**：`blockentity/OutpostCoreBlockEntity.java:315-335`（`settleOnRemoval` 改为「先迁移、后泥土」）、`:353-366`（只处理绑定表里本核的耕地）；`farmland/FarmlandLedger.java`（改绑 + 摘除）。
- **补充**：判定顺序固定「先算完要迁移 / 变泥土的坐标集合，再动世界」，沿用 `:312-313` 的既有理由（避免边遍历边改账本）。只处理**本核名下**的耕地，不再遍历在线玩家——现实现 `:356-364` 那一趟遍历正是 bug 2（所有者下线即失效）的另一半，绑定制之后它没有存在的必要（D-137 一并解决）。「变泥土」仍是唯一的世界写入（`:331`），不新增掉落、不给补偿、不通知耕地主。**「找不到接替者 + 该耕地所在区块未加载」是单独的一支**：此时 `setBlock` 写不进去，也不能为了铲地而加载区块，因此降级为**只摘绑定、世界不动**（耕地还在，只是失去保护）；理由是「绝不因清算而加载区块，也绝不丢方块更新」（A-35，与 RT-32 的降级口径同向）。否决「核被毁时把名下耕地全部变泥土（保留 0.1.0 的语义）」：那会让绑定制只剩下计费功能，而作者要修的正是这个 bug。

#### D-137 · 老存档启动时补建绑定

- **编号**：D-137
- **结论**：服务端启动时对**未绑定**的耕地按新规则补建绑定；补不上的**保持耕地不动**，只是不受保护；**绝不铲老存档**。
- **依据**：`BRIEF` §2 第 15 条；`plan0.1.1.txt:14-15`；§1.3（`farmland/FarmlandLedger.java:44` 落盘名 `farmstorm_farmland`、`:228-244` `save`、`:246-264` `load`）；RT-32（`farmstorm_farmland.dat` 格式 v1 → v2 兼容读 + 启动补建）。
- **落地位置**：`farmland/FarmlandLedger.java`（`load` 的旧格式兼容分支 + 启动补建入口）；`blockentity/OutpostCoreBlockEntity`（候选核查询复用 `inRange`）；服务端启动钩子（与 `Farmstorm` 的既有初始化同层）。
- **补充**：补建的匹配谓词与 D-136 的迁移**是同一条**（覆盖 + 同盟 + 有空位，最近优先）——裁定 #15 只说「按新规则」，而新规则里给出完整谓词的只有 #14，复用它是唯一不发明第三套判据的做法（**本文补充**，见「写作期回报」第 3 条）。旧格式的 `byOwner` 记录在加载时转成「无绑定耕地」，于是老田原地变成「待补建」，这正是「补不上也不动它」的语义。补建只在启动跑一次；运行期新开垦的耕地走 D-135。**补建的遍历顺序写死**：**老档补建（含 0.1.0 老档的首次补建）一律先把候选耕地按 `BlockPos` 单级全序排序（`BlockPos#compareTo`：y → z → x），再逐条处理**；「按 `byOwner` 插入顺序」**作废**——`FarmlandLedger.byOwner` 是 `HashMap`（A-39 已确认），跨 owner 的派生顺序不稳定，而 0.1.0 老档**没有 `orphans` 键**可依，派生顺序只能来自账本，不排序就会出现「同一份存档两次启动结果不同」，与 30 号 §7 #19 的排序确定性直接冲突（A-47）。**不设维度级**：`FarmlandLedger` 的 NBT 只有 `pos`、没有维度字段（`FarmlandLedger.java:46-48`、`:228-244`），而同一 `pos` 在账本里本就只有一条，单级即全序；将来账本若引入维度，再在这一条上追加一级。容量 16 是硬顶，顺序直接决定老档里哪些田能绑上，写死才可复现。否决「启动时把补不上的耕地变泥土」：与裁定 #15 的「绝不铲老存档」直接冲突；也否决「补建时按当前所有在线玩家的队伍重算」：那会把老档的归属关系洗掉。

#### D-138 · 「己方」边界与离线有效

- **编号**：D-138
- **结论**：「己方」= **核主本人 + 同队队友**；判定一律走 **UUID / 名字**，**不看目标是否在线**；耕地保护、额度、浮窗显示**共用这一套判定**。
- **依据**：`BRIEF` §2 第 17 条（离线也有效，改从 UUID 索引查）；`plan0.1.1.txt:15`；§1.3 的 bug 2 现场——`FarmlandGuard.protectsFromTrample`(`:120-134`) 的 `:125` 遍历 `getPlayerList().getPlayers()`（只取在线）、`inOwnRange`(`:92-107`) 同样从在线玩家出发、类注释 `:114-115` 自认「账本归属者全员离线时不受保护」；`OutpostCoreIndex.owned(Player)`(`:85-90`) 也吃在线实体；`util/DeployedOwnership.isAlly(level, ownerUuid, ownerName, target)`(`:48`) —— owner 侧本来就是 UUID + 名字。
- **落地位置**：`farmland/FarmlandGuard.java:92-107`、`:120-134`（改查绑定表 → 核主 UUID → `DeployedOwnership.isAlly`）；`farmland/FarmlandLedger.java:101-108`、`:111-121`（`capacity` / `used` 的成员集合）；`blockentity/OutpostCoreBlockEntity.java:353-366`（随 D-136 一并去掉在线遍历）。
- **补充**：缺口不在「怎么判同盟」——`DeployedOwnership.isAlly` 的 owner 侧已经是 UUID + `scoreboardName`（`OutpostCoreIndex.java:121-123` 的注释写明这是为了离线队伍反查），缺的是「从这块耕地反查到核主」，而这正是 D-134 的绑定表提供的。额度也必须一起改：`capacity()` 现在遍历在线成员（`:102` 的 `members(...)`，`FarmlandLedger.java:185-196`），队友下线会当场变小——不与本条同改，就会出现「队友下线后已耕地超额」的新双源。生物恒非己方（无队伍归属）；创造 / 旁观在索敌与触发面上的既有排除不属于「己方」定义。**踩踏保护面按「有绑定即受保护」**：`FarmlandGuard.protectsFromTrample` 没有查询者参数，只能回答「这块地有没有绑定」，回答不了「谁踩的」，所以踩踏面**与换队无关**；**额度闸与浮窗仍按实时己方判定**——29 号 B-18 的「换队后立即失去保护」因此收窄为只对额度 / 浮窗成立（A-45）。否决「缓存队伍快照以便离线解析」：那会把换队变成需要刷新的事件，与 C-9 的实时解析相反。

#### D-139 · 额度来源改为核容量，废弃 `farmland.limitPerPlayer`

- **编号**：D-139
- **结论**：额度改为 **`capacity = 16 × 己方核数`**（16 即 `outpost_core.farmlandBonus`）；`farmland.limitPerPlayer` 与「个人基础额度（base）」概念**整体删除**。
- **依据**：`BRIEF` §2 第 16 条；§1.3——`FarmlandLedger.base()`(`:72-74`) 读 `farmlandLimitPerPlayer` 或 `baseOverride`、`:82-108` 的 `capacity = base + bonus × 核数`、`:185-196` `members(...)`；§1.9（`farmland.limitPerPlayer` 的 push `:904` / define `:907`，默认 0，范围 0–4096，**唯一读者**是 `base()`）；`BRIEF` §3.4（删 1 增 1 ⇒ config 仍 166，组 59 → 58）。
- **落地位置**：`farmland/FarmlandLedger.java:72-74`（删 `base()`）、`:76-79`（删 `setBase`）、`:82-84`（`personalCapacity` 去 base 参数）、`:92-98`（`sumOverMembers` 去 base）、`:101-108`（`capacity`）；`config/FarmstormConfig.java:904,907`（删键）。
- **补充**：0.1.0 的「默认 0 ⇒ 开局谁都耕不了地，必须先放核」（22 号 D-116 第 2 条）在算式上仍然成立，但成立的原因变了：不再是 base = 0，而是「没有核就没有容量」。`used()` 的「已用」改为「绑定表里己方名下的块数」，与容量同源。config 总数持平（166 = 166 − 1 + 1），但组数从 59 降到 58（§3.4）。否决「保留 `limitPerPlayer` 作为全队基础额度」：那会回到「base + 容量」的双源，与裁定 #16 的 `16 × 核数` 直接冲突，也会让「一个键两重语义」变成「两个键三重语义」。

#### D-140 · 删除 `/farmstorm farmlandlimit` 指令树

- **编号**：D-140
- **结论**：删除 `/farmstorm farmlandlimit` 子命令与整条服务端指令注册链；`/farmstorm` 这个根节点**不保留空壳**。
- **依据**：`BRIEF` §2 第 16 条；`plan0.1.1.txt:14-15`；§1.9——`command/FarmstormCommands.java` 全文件 58 行、**只有** `:37-41` 一个子命令（权限 2、范围 0..4096，`:27-31`），唯一动作 `ledger.setBase(value)`(`:50-57`)；注册点 `event/FarmstormGameEvents.java:62`、`:71-72`、`:4`；相关语言键 2 条（`zh:145`、`zh:64`）；RT-40（连带清单）。
- **落地位置**：`command/FarmstormCommands.java`（整文件删除）；`event/FarmstormGameEvents.java:4`、`:62`、`:71-72`；语言键 `command.farmstorm.farmlandlimit.set`。
- **补充**：指令是 `base` 的唯一写入口，`base` 死了它就没有语义（D-139）。连带四处：`baseOverride` 字段、两条语言键、0.1.0 文档里的指令说明（§3.3 第 1 ~ 5 行）、`README` 里「耕地规则走 `/farmstorm farmlandlimit`，不是 gamerule」那一句（`README.md:191`，属 README 增量范围）。删除后**服务端**不再有 `/farmstorm`；客户端的 `/farmstorm hud …`（D-144）是另一条注册，两者不共用同一个注册点。否决「保留指令但改成只读查询」：作者要的是删掉额度载体，留一个只能看不能改的壳只会让人以为额度还在。

#### D-141 · 据点核心亮度 15

- **编号**：D-141
- **结论**：`outpost_core` 注册时加 `.lightLevel(state -> 15)`，其余方块属性一律不动。
- **依据**：`BRIEF` §2 第 18 条；`plan0.1.1.txt:16`；§1.2——`registry/FarmstormBlocks.java:82-89` 有 `MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `SoundType.STONE` / `pushReaction(BLOCK)`，**没有 `.lightLevel(...)`**（实测 grep，取值 0）。
- **落地位置**：`registry/FarmstormBlocks.java:82-89`。
- **补充**：亮度是**方块光**，会强光传播 15 格、并让该位置永不刷怪（RT-36）——29 号要把它写进「已知影响」，30 号要写进方块属性表。亮度不影响方块实体、不影响边界粒子、不影响耕地判定。25 条裁定里只有这一条是纯方块属性改动，因此它不连带任何数值表。否决「把亮度做成 config 键」：作者给的是一条固定属性，加一个键等于发明需求。

### 1.6 浮窗与运行期（D-142 ~ D-145）

#### D-142 · 浮窗的数据面：新增 1 个自定义网络包（S2C 只读）

- **编号**：D-142
- **结论**：为浮窗新增**唯一一个**自定义网络包：**服务端 → 客户端单向**推送「己方核心列表」；客户端只读、**不回包**、不做任何判定。
- **依据**：`BRIEF` §2 第 22 条（废除 0 包红线，新增自定义包推送核心列表）；§1.4——自定义包现为 **0 个**、BE 同步先例 2 个（`DeployedCropBlockEntity.java:455-460` / `:462-466`、`ConverterBlockEntity.java:418-428`）、原版包用法 5 类 8 处，结论是「服务端 → 客户端推一份自定义列表**在本仓没有任何先例**，本版是第一次」；§1.2（`OutpostCoreIndex` 是 `SavedData`，客户端没有）；§1.5（`WildHunterHud` 无数据面、`FarmstormClient.java:46-96` 六个监听里没有收包）；RT-31。
- **落地位置**：新增 `network/` 包（S2C payload + `RegisterPayloadHandlersEvent` 注册 + 客户端 handler）；数据源 `outpost/OutpostCoreIndex`；推送时机挂在 `blockentity/OutpostCoreBlockEntity` 的放置 / 摧毁 / 迁移，以及玩家登录 / 换维度处。
- **补充**：payload 的名字、`StreamCodec` 形态、推送时机集合与去抖**由 30 号定**（RT-31 要求把时机列成清单逐条验收）。内容边界：每个核给「维度 + 坐标 + 归属者显示名 + 是否自己」，排序由列表顺序承载（D-143）；归属名取服务端已有的 `ownerNames`（`OutpostCoreIndex.java:55-56`，离线也有）。**为什么非它不可**：客户端手里只有视距内区块与已被追踪的实体，回答不了「我有哪些核」；归属者可能离线（`owned(Player)` 只在在线时可用，§1.2），队友关系只有服务端能解析；`OutpostCoreIndex` 本身是服务端 `SavedData`，客户端没有副本。这个包**不进注册面**（+0，§3.4：payload 走 `RegisterPayloadHandlersEvent`，不进 `BuiltInRegistries`）。被否决的五个替代方案见 §6 第 2 ~ 6 条与第 9 条，红线层面的说明见 §4。

#### D-143 · 浮窗的显示范围与内容

- **编号**：D-143
- **结论**：
  1. 列**自己 + 队友**的**全部**核心——**含未加载区块、含其它维度**（与耕地保护**同一套**同盟判定，即 D-138）；
  2. 每核一行：`#n [维度路径] (x, y, z) 归谁`（自己用「自己」标识，队友显示真实名字）；
  3. 按 **`placedGameTime` 升序**排列，**跨维度统一排序**；
  4. 最多显示 **8 行核心**，超出时**另加 1 行**「+N」——即最高 **9 行**。
- **依据**：`BRIEF` §2 第 19、20 条；`BRIEF` §2 表下注（8 行上限、默认位置左上角 `(4,4)`、自己绿色 / 队友白色、维度标识取维度 id 的 path 且不新增语言键，标注「本卡补充，可否决」）；§1.5（`WildHunterHud` 的注册与绘制形态 `:19`、`:40-41`、`:46`、`:60-67`）；§1.2 与 `blockentity/OutpostCoreBlockEntity.java:79`、`:405`（`placedGameTime` 落盘，是本条排序的依据）。
- **落地位置**：新增 `client/` 下的浮窗渲染类（`RenderGuiEvent.Post`，注册形态照 `client/WildHunterHud.java`）；数据来自 D-142 的包；排序信息来自列表顺序。
- **补充**：**正因为要跨维度显示，行首必须有维度标识**——取维度 id 的 **path**（`overworld` / `the_nether` / `the_end`），**不新增语言键**（`BRIEF` §2 表下注的口径）；同一组坐标在不同维度各有一个核时靠它区分，不带维度标识必然撞车（**本文补充**，见「写作期回报」第 4 条）。「归谁」自己一行用一条语言键，队友显示 `ownerNames` 里的名字（离线也有）。行距 / 字号 / 内边距 / 「+N」的字形由 **29 号在本条框架内定**，受 `hud.x` / `hud.y` 的 int 值域与屏幕尺寸约束。不显示敌对核、不显示核心物品、不做遮挡检测（遮挡检测是自动避让的前提，随 §6 第 7 条一起否决）。`options.hideGui` 与 `screen != null` 的前置沿用 `WildHunterHud.java:46`，唯一例外是拖拽模式（D-144 补充）。

#### D-144 · 浮窗的开关与位置载体：客户端按键 + 客户端指令 + CLIENT config + 拖拽

- **编号**：D-144
- **结论**：
  1. **2 个 `KeyMapping`**：开关浮窗、进入拖拽模式；
  2. **客户端指令** `/farmstorm hud <on|off|reset>`；
  3. **CLIENT 类型 config** 3 键持久化：`hud.enabled`（bool，默认 **true**）、`hud.x`（int，默认 **4**）、`hud.y`（int，默认 **4**）；
  4. 位置**只能**靠**拖拽模式**调：打开一个 Screen、拖动定位、`ESC` 保存（单人会自动暂停）；
  5. **不做自动避让**。
- **依据**：`BRIEF` §2 第 21 条；`BRIEF` §2 表下注（默认 `(4,4)`、拖拽开 Screen、`ESC` 保存）；§1.5——`client/` 21 个类里 `KeyMapping` 注册 **0 个**（`RegisterKeyMappingsEvent` 0 命中）、CLIENT 类型 config **0 个**（全仓唯一 `registerConfig` 在 `Farmstorm.java:56`，是 `ModConfig.Type.SERVER`）；`BRIEF` §3.4（CLIENT 3 键**不计入** 166，单独一份 `farmstorm-client.toml`）；`plan0.1.1.txt:17`。
- **落地位置**：`client/FarmstormClient.java`（首次 `RegisterKeyMappingsEvent` 注册）+ 新增客户端按键类；新增拖拽 `Screen`；`config/FarmstormConfig.java`（首次 `ModConfig.Type.CLIENT`，3 键）；客户端指令注册。
- **补充**：**拖拽时浮窗必须仍然可见**——`WildHunterHud.java:46` 的 `minecraft.screen != null` 前置会让 HUD 在 Screen 打开时整体消失，拖拽模式必须按「编辑态」例外处理，否则玩家看不到自己在拖什么（**本文补充**：技术必然，不是设计偏好）。位置钳制在屏幕内（`0 ≤ x ≤ guiWidth − 宽`，表达式由 29 号定）。**这是与 plan 原文的第二处正面冲突**：`plan0.1.1.txt:17` 要求「被遮挡时可以自动移动到最靠近左上角且不被遮挡的位置」，裁定 #21 改判为拖拽 + **不做自动避让**（§6 第 7 条）。「用服务端 config 驱动客户端 HUD 开关」在技术上跑不通（§6 第 2 条）。键位默认绑定、指令反馈文案与语言键由 30 号逐条点名（§3.4 的 `k`）。

#### D-145 · 核心所在区块的运行期临时强制加载

- **编号**：D-145
- **结论**：每个已放置的核在**运行期临时**强制加载它所在的**那 1 个区块**（**不是** 3×3）；**不写** `world/data/chunks.dat`；核被毁或停服即释放。
- **依据**：`BRIEF` §2 第 24 条；RT-39（ticket 泄漏：释放点与 `release()` 同处、停服兜底）；§1.2（`OutpostCoreBlockEntity.serverTick`(`:179-192`) 的边界粒子依赖方块实体在加载区块内 tick；`release()` 由 `OutpostCoreBlock.java:101-107` 触发）。
- **落地位置**：`blockentity/OutpostCoreBlockEntity`（放置时申请、`release()` 时释放）；`outpost/` 侧的区域 ticket 登记；停服钩子兜底。
- **补充**：ticket 的类型与等级**由 30 号按 21.1.213 的可用形态定**（`TicketType` 的形态属技术面）。只加载 1 个区块，不是 3×3——耕地判据读绑定表与坐标，不需要耕地所在区块在内存里（`isFarmlandLoaded` 只读内存区块，`OutpostCoreBlockEntity.java:382-385`）。它**不解决**「客户端看不见视距外的核」，那是 D-142 的包负责的，两者不可互换（§6 第 4 条）。**落盘口径（勘误）**：1.21.1 的强制加载区块数据**不在 `level.dat`**，而在 **`world/data/chunks.dat`**（`ForcedChunksSavedData.FILE_ID = "chunks"`，按维度各自落盘）；裁定 #24 早期版本里的 `level.dat` 是搬迁笔误（事实卡已按 A-38 修正），以实现口径为准。因此**两条实现路径一并否决**：① `ServerLevel#setChunkForced()`；② NeoForge 的 `TicketController#forceChunk`——两者都会把核坐标写进 `chunks.dat`，与「运行期临时」直接冲突，且会在停服后残留。**只用裸 `TicketType` + `addRegionTicket`**。

### 1.7 红线与发布面（D-146 ~ D-147）

#### D-146 · 红线口径变更：只保留「无 Mixin」

- **编号**：D-146
- **结论**：从 v0.1.1 起，项目级红线**只剩「无 Mixin」一条**；「**0 自定义网络包**」**整体废除**——不是放宽、不是「允许 1 个」、不是「例外」。
- **依据**：`BRIEF` §2 第 23 条；`BRIEF` §0.1 的两条红线对照表（v0.0.1 ~ v0.1.0 = 0 个；v0.1.1 = 废除该限制，本版恰好新增 1 个但**不设上限**；Mixin 始终无）；§1.4（`@Mixin` 实测 0 个、`build.gradle` 无 mixin 插件）。
- **落地位置**：28 ~ 33 六份（全部红线表述）；`README.md` 红线段（`:71`、`:148`、`:191`，英文段 `:419`、`:423`）；`32-PROMPT-MAIN-0.1.1.md` 的 HC-77 / N-75 新红线段。
- **补充**：22 ~ 27 号**冻结不改**（`BRIEF` §0.1 的 ⚠️、§6 第 3 条），与本文冲突时以本文为准（§4 的文档清单）。废除后**不设数量上限**，但每新增一个包仍须在决策记录里登记、不得顺手加——本版登记的就是 D-142 那一个。26 号 HC-73 里的第三项「`GameRules.register` 为 0」**不属于**两条红线（`BRIEF` §0.1 只列两条），它作为工程约束继续有效，32 号要在 HC-77 里原样保留。否决「写成『放宽为允许 1 个』或『本版例外』」（`BRIEF` §6 第 4 条明令禁止）。详细的变更说明见 §4。

#### D-147 · 发布面：config 默认值不迁移，脚本 jar 名顺带改 0.1.1

- **编号**：D-147
- **结论**：man瓜 的两个默认值改动（60 → 40、1 → 3）**只写文档、代码不做迁移**；`检查环境.cmd` 与 `一键构建.cmd` 里写死的 jar 名一并改成 `farmstorm-0.1.1+mc1.21.1.jar`。
- **依据**：`BRIEF` §2 第 25 条；§1.10——`检查环境.cmd:14` 与 `一键构建.cmd:11-12` 的 jar 名仍是 `farmstorm-0.0.1+mc1.21.1.jar`（0.1.0 时就没跟上，实测）；RT-38（已落盘 toml 不随默认值变化）。
- **落地位置**：`检查环境.cmd:14`、`一键构建.cmd:11-12`；交付文档的「升级须知」一节（`README.md` 增量 + `30-TECH-0.1.1.md`）。
- **补充**：升级须知必须写清两件事——① 已落盘的 `farmstorm-server.toml` 仍是旧值，要生效得删旧 toml 或手改 `pumpkin_turret.fireIntervalTicks` / `projectileDamage`；② 新增的 `pumpkin_turret.bulletSpeed` 会由配置系统自动补进旧 toml，不需要手加。代码不做迁移是有意的：为了「帮玩家把默认值改掉」去写别人的配置文件，等于擅自改服务器设置（RT-38 的同向）。jar 名与 `mod_version` 必须同一次改，否则构建产物名与实际版本脱钩（D-124）。这条与 D-124 的分工是：版本号在 `gradle.properties:11`，jar 名在两支 `.cmd`。

---

## 2. 作者裁定索引（25 条 → 决策号映射）

下表逐条转写 `BRIEF` §2 的 25 条作者裁定（**条目与「定为」两列与事实卡逐字一致**），最后一列是本文的决策号。

| 裁定 # | 条目 | 定为 | 对应决策号 |
|---|---|---|---|
| 1 | 版本号 | **0.1.1**；文档号 **28~33**；编号接续见 §0.2 | D-124 |
| 2 | 「绿色 +」规则 | **全局作废**（规则层面废掉），但**本版只重生成 `wild_apple` 一张**，其余 24 张留到下版批量重生成 | D-125 |
| 3 | 狂野苹果材质 | **直接用 64×64 原图**（不降采样、不再走 16 网格众数取色） | D-126 |
| 4 | man瓜 身份 | = `farmstorm:pumpkin_turret` | D-127 |
| 5 | man瓜 攻击间隔 | `pumpkin_turret.fireIntervalTicks` 默认 **60 → 40 刻**（2 秒） | D-127 |
| 6 | man瓜 伤害 | `pumpkin_turret.projectileDamage` 默认 **1 → 3** | D-128 |
| 7 | man瓜 炮弹速度 | `TurretBullet.SPEED` **0.6 → 0.45**（每秒 9 格），并**提成 config 键** | D-129 |
| 8 | man瓜 追踪 | **不引入软追踪**；只修「**近距离绕圈/抖动**」（目标贴脸时反复掉头打不中） | D-129 |
| 9 | 朋友的韭 窗口 | 落地后 **200 刻**内持续；**每刻**扫描球内生物；施加 **`200 − x`** 刻 Q弹（x=落地后经过刻数） | D-131 |
| 10 | 朋友的韭 音效 | **落地那一刻**判定：球内 ≥1 生物就播**一次**，否则永不播（空投不播） | D-132 |
| 11 | 朋友的韭 实体 | 落地后实体**存活 200 刻且保持可见** | D-133 |
| 12 | 耕地-核心关系 | **每块耕地只归 1 个核管理**；**一个核最多管 16 块**；容量**直接复用** `outpost_core.farmlandBonus`（默认 16），不新增键 | D-134 |
| 13 | 绑核规则 | 锄地成功时绑给**距离最近**的己方核（平手按先放置） | D-135 |
| 14 | 迁移规则 | 核被毁时，只在「**影响范围覆盖该耕地** + **与耕地主同盟** + **还有空位**」的核里找接替者，**最近优先**；找不到 → 变泥土 | D-136 |
| 15 | 老存档 | **启动时按新规则补建绑定**；补不上的**保持耕地不动**、只是不受保护（**绝不铲老存档**） | D-137 |
| 16 | 额度 | **废弃 `farmland.limitPerPlayer`**，`capacity = 16 × 核数`；`/farmstorm farmlandlimit` 指令**一并删除** | D-139、D-140 |
| 17 | 「己方」边界 | 核只管「**核主本人 + 同队队友**」的耕地；**离线也有效**（改从 UUID 索引查） | D-138 |
| 18 | 据点核心亮度 | `.lightLevel(state -> 15)` | D-141 |
| 19 | 浮窗显示谁 | **自己 + 队友**的**全部**核心 —— **含未加载区块、含其它维度**（不是「只显示当前维度」）；同盟判定与耕地保护同一套 | D-143 |
| 20 | 浮窗内容 | 每核一行：**维度标识 + 坐标 + 归谁（自己 / 队友名字）**，按放置顺序（`placedGameTime` 升序，跨维度统一排序） | D-143 |
| 21 | 浮窗开关/位置 | **客户端按键**（开关 + 进入拖拽）+ **客户端指令** + **CLIENT config 持久化**；位置用**拖拽模式**调，**不做自动避让** | D-144 |
| 22 | 浮窗数据面 | **废除「0 自定义网络包」红线**，新增**自定义网络包**推送核心列表 | D-142 |
| 23 | 红线新口径 | 网络包红线**整体废除**；**只保留「无 Mixin」** | D-146 |
| 24 | 强制加载 | 核心所在**那 1 个区块**，**运行期临时**，不落盘（**不写 `world/data/chunks.dat`** —— 裁定 #24 早期版本写的 `level.dat` 是笔误（事实卡已按 A-38 修正）） | D-145 |
| 25 | config 迁移 & 脚本 | man瓜 默认值改动**只写文档**、代码不做迁移；`检查环境.cmd` / `一键构建.cmd` 的 jar 名**顺带改 0.1.1** | D-147 |

三处映射备注（读表时注意）：

1. 25 条裁定对上 **24 条决策**：裁定 #16 与 #25 各自拆成两条（额度 / 指令、config / 脚本），因为它们的落地位置与验收方式完全不同；裁定 #4 + #5、#7 + #8、#19 + #20 各自合进一条（man瓜 身份与间隔、炮弹速度与近距收敛、浮窗范围与内容），合并理由写在那几条的「补充」里。
2. **25 条裁定里没有「压缩干草块音效」这一条**——plan 的第 3 条需求（`plan0.1.1.txt:20-23`）没有进访谈，D-130 的依据只有 plan 原文与 `BRIEF` §1.6 / §3.2 第 3 行。这是本文补成独立决策的，登记在「写作期回报」第 1 条。
3. 表里「定为」一列的加粗是事实卡原有的强调，不是本文新加的判断；引用时照抄，不许改写措辞。

---

## 3. 对 0.1.0 的改动登记

三张表分别登记**新增 / 修改 / 删除**。每行都给 0.1.0 的原文位置——它是「0.1.0 里这句话在哪」的证据，不是建议改它（22 ~ 27 号冻结）。表内不含任务与排期。

### 3.1 新增（0.1.0 没有的东西）

| # | 新增项 | 0.1.0 的原文位置（证明它当时不存在） | 决策 |
|---|---|---|---|
| 1 | **1 个自定义网络包**（S2C 推己方核心列表 + 注册 + 客户端 handler） | `22-DECISIONS-0.1.0.md:44`（「0 自定义网络包」）、`:68`、`:480`（§6 第 4 条「`CustomPacketPayload` 仍是 0」）；`26-PROMPT-MAIN-0.1.0.md:183-185`（HC-73） | D-142 |
| 2 | **2 个 `KeyMapping`** + 首次 `RegisterKeyMappingsEvent` 注册 | `BRIEF` §1.5：`client/` 21 个类里 `KeyMapping` 注册 0 个、`RegisterKeyMappingsEvent` 0 命中；`client/FarmstormClient.java:46-96` 的六个监听里没有键位注册 | D-144 |
| 3 | **CLIENT 类型 config**（`farmstorm-client.toml`，3 键 `hud.enabled` / `hud.x` / `hud.y`） | `Farmstorm.java:56` 是全仓唯一 `registerConfig`，类型为 `ModConfig.Type.SERVER`（`BRIEF` §1.5） | D-144 |
| 4 | **客户端指令** `/farmstorm hud <on\|off\|reset>` | `command/FarmstormCommands.java:37-41` 只有 `/farmstorm farmlandlimit`，注册在服务端 `FarmstormGameEvents.java:71-72`（`BRIEF` §1.9） | D-144 |
| 5 | **浮窗渲染 + 拖拽 Screen** | `BRIEF` §1.5：客户端 HUD 只有 `WildHunterHud`（77 行、无遮挡求交、无开关、无 config） | D-143、D-144 |
| 6 | **耕地 → 核 绑定表**（含反查）与**启动补建** | `farmland/FarmlandLedger.java:51` 只有 `byOwner`；`save` / `load`(`:228-244` / `:246-264`) 无绑定字段（`BRIEF` §1.3） | D-134、D-137 |
| 7 | **核心所在区块的运行期临时 ticket** | `BRIEF` §3.2 第 25 行列为新增；0.1.0 无任何 chunk ticket | D-145 |
| 8 | **config 键 `pumpkin_turret.bulletSpeed`**（double，0.45） | `entity/TurretBullet.java:28` 是硬编码 `SPEED = 0.6`，无 config（`BRIEF` §1.7） | D-129 |
| 9 | **语言键**：`farmstorm.configuration.pumpkin_turret.bulletSpeed`、浮窗「自己」标识、客户端指令反馈（on / off / reset） | `BRIEF` §3.5 的增项清单；0.1.0 基数 315 键里没有这些键 | D-129、D-143、D-144 |

### 3.2 修改（0.1.0 有、语义或数值变了）

| # | 项 | 0.1.0 的值 / 语义 | v0.1.1 | 0.1.0 的原文位置 | 决策 |
|---|---|---|---|---|---|
| 1 | 版本号 | 0.1.0 | **0.1.1** | `gradle.properties:11` | D-124 |
| 2 | `pumpkin_turret.fireIntervalTicks` | 60 | **40** | `config/FarmstormConfig.java:431-433` | D-127 |
| 3 | `pumpkin_turret.projectileDamage` | 1 | **3** | `config/FarmstormConfig.java:425-427` | D-128 |
| 4 | `textures/item/wild_apple.png` | 16×16 / 571 B / 88 色 / 107 透明 | **64×64**（源图 6,946 B） | `22-DECISIONS-0.1.0.md:581`（D-105 的成品规格）、`assets/farmstorm/textures/item/wild_apple.png` | D-126 |
| 5 | 「绿色 +」规则 | 生效（25 张带 +） | **作废**（生成器删机制） | 规则原文 `plan0.0.1.txt:8`；承载点 `tools/gen_textures.py:274-275,407,974-975` | D-125 |
| 6 | 耕地保护判据 | 「范围内的己方耕地」 | **绑定到核的耕地** | `farmland/FarmlandGuard.java:120-134`；`22` 号 D-120 第 3 项 | D-134、D-138 |
| 7 | 额度算式 | `base + bonus × 核数` | **`16 × 核数`** | `farmland/FarmlandLedger.java:82-108`；`22` 号 D-116、D-117 | D-139 |
| 8 | 「己方」解析 | 从在线玩家列表出发 | **UUID / 名字侧**（离线有效） | `farmland/FarmlandGuard.java:92-107`、`:120-134`；`outpost/OutpostCoreIndex.java:85-90` | D-138 |
| 9 | 核被毁清算 | 范围内己方耕地**全部**变泥土 | **本核名下耕地先迁移、迁不动才泥土** | `blockentity/OutpostCoreBlockEntity.java:315-335`、`:353-366`；`22` 号 D-120 第 4 项 | D-136 |
| 10 | 据点核心亮度 | 0（无 `lightLevel`） | **15** | `registry/FarmstormBlocks.java:82-89`；`22` 号 D-119 | D-141 |
| 11 | 压缩干草块音效 | `SoundType.GRASS` | **`STONE`** | `registry/FarmstormBlocks.java:79`；`22` 号 §7.2 的 D-57 行（记「`sound(GRASS)` 不动」） | D-130 |
| 12 | 红线 | **0 自定义网络包** + 无 Mixin | **只留无 Mixin** | `22-DECISIONS-0.1.0.md:44`、`:68`、`:480`；`26-PROMPT-MAIN-0.1.0.md:183-185` | D-146 |
| 13 | 「客户端零新增代码」的口径 | 本版成立 | **不再成立** | `22` 号 D-121 第 2 条（`:302`）、`23-DESIGN-0.1.0.md:49` | D-142、D-143 |
| 14 | `tools/gen_textures.py` 的输出尺寸断言 | `:1152-1154` 断言 `image.size == expected_size(spec)`，而 `expected_size()`(`:982-983`) 对非 GUI 恒返回 `(TEXTURE_SIZE, TEXTURE_SIZE)` = 16×16 | **放宽为按 spec 的实际尺寸**（`wild_apple` 为 64×64），断言本身保留 | `tools/gen_textures.py:982-983`、`:1151-1154` | D-126 |
| 15 | `outpost/OutpostCoreIndex` 的条目结构（`Entry`） | `(dimension, pos)` 两字段，**无 `gameTime`** | **`(dimension, pos, gameTime)`**，`gameTime` 落盘；老档条目缺该字段时回落 `Long.MIN_VALUE` | `outpost/OutpostCoreIndex.java:50`（`record Entry`）、`:43-47`（NBT 键名）、`:209-238`（落盘结构） | D-135（A-39）；A-53；RT-32 |

### 3.3 删除（0.1.0 有、本版移除 —— **最容易漏的一节**）

| # | 删掉什么 | 0.1.0 的原文位置 | 决策 |
|---|---|---|---|
| 1 | config 键 **`farmland.limitPerPlayer`**（整组 `farmland` 归零） | `config/FarmstormConfig.java:904`（push）、`:907`（define）；`22` 号 §5.2 第 6 行 | D-139 |
| 2 | **`/farmstorm farmlandlimit` 子命令**——整个 `command/FarmstormCommands.java`（58 行） | `command/FarmstormCommands.java:37-41`；`22` 号 D-116、§4.2 第 18 行 | D-140 |
| 3 | 服务端指令注册 3 处 | `event/FarmstormGameEvents.java:4`（import）、`:62`（监听）、`:71-72`（注册调用） | D-140 |
| 4 | **`FarmlandLedger.baseOverride` / `base()` / `setBase()`** 与 `personalCapacity` 的 base 参数 | `farmland/FarmlandLedger.java:57-58`、`:72-74`、`:76-79`、`:82-84` | D-139 |
| 5 | 语言键 2 条：`farmstorm.configuration.farmland.limitPerPlayer`、`command.farmstorm.farmlandlimit.set` | `zh_cn.json:145`、`zh_cn.json:64`（`BRIEF` §1.9） | D-139、D-140 |
| 6 | 生成器的**叠「+」机制**（`NO_PLUS_ICONS` / `add_plus` / `build_marker` / 右下角贴附） | `tools/gen_textures.py:274-275`、`:407`、`:546-560`、`:612-623`、`:974-975` | D-125 |
| 7 | **`TurretBullet.SPEED` 常量** | `entity/TurretBullet.java:28` | D-129 |
| 8 | `expected_size()` 的「非 GUI 恒 16×16」硬编码（替换物：按 spec 实际尺寸，见 §3.2 第 14 行） | `tools/gen_textures.py:982-983` | D-126 |

删除项的四条附注：

- 第 1、2、4、5 行是**同一件事的四个落点**（额度载体消失）：config 键、指令、`base` 字段、两条语言键必须一起删，删一半会留下「读一个不存在的键」或「指令改一个不存在的字段」。
- 第 6 行删的是**机制**，不是 25 张图。其余 24 张旧图仍在（§7 第 1 条），但生成器重跑不会再把 `+` 加回去。
- 第 7 行删常量后必须同时加 config 键（§3.1 第 8 行），只删不加等于把速度清零。
- 第 8 行与 §3.2 第 14 行是**同一改动的两面**：删掉的是「非 GUI 恒 16×16」这条默认，替换物是「按 spec 实际尺寸 + 断言保留」（A-32）。

### 3.4 计数算式

- **config**：`166 − 1 + 1 = 166`（删 `farmland.limitPerPlayer`、增 `pumpkin_turret.bulletSpeed`），总数持平但**组成变了**，组数 `59 → 58`。CLIENT 3 键**不计入** 166，单独一份 `farmstorm-client.toml`。
- **语言键**：`315 − 2 + 1 + 3 + 1 + 2 + 3 = 323`，每一项点名如下——**− 2**＝删 `farmland.limitPerPlayer` 与 `farmlandlimit.set`；**+ 1**＝`pumpkin_turret.bulletSpeed`；**+ 3**＝CLIENT config 的 `hud.enabled` / `hud.x` / `hud.y`；**+ 1**＝浮窗「自己」标识；**+ 2**＝两个 `KeyMapping` 的描述键（`key.farmstorm.*`）；**+ 3**＝客户端指令反馈（on / off / reset）。净增 10 条（1 + 3 + 1 + 2 + 3 = 10），结果与 30 号逐条点名的 323 一致（A-48；A-48 里记作 `k` 的指令反馈项就是这里的 `+ 3`）。
- **注册面**：**+0**（网络包 payload 走 `RegisterPayloadHandlersEvent`、按键走 `KeyMapping`、CLIENT config 走 `ModConfigSpec`，三者都不进 `BuiltInRegistries`）。基线 93 出自 `BRIEF` §0 与附录 A 的逐类点名，**30 号仍须自己从源码逐类点名核对**，不得直接抄这一行（`BRIEF` §3.1 的 ⚠️）。
- **存档格式**：本版有**两处**变更，**不是一处**——① `farmland/FarmlandLedger` 的 `farmstorm_farmland.dat` 升到 v2（新增「耕地 → 核」绑定表，老档 v1 兼容读，RT-32）；② `outpost/OutpostCoreIndex` 的 `farmstorm_outpost_core.dat` 条目由 `(dimension, pos)` 改为 `(dimension, pos, gameTime)`（A-53，老档缺字段回落 `Long.MIN_VALUE`）。**只按一处做兼容读会漏掉另一处**，两处都要写进 30 号的存档兼容清单。

---

## 4. 红线口径变更（本版最重要的单项变更）

### 4.1 变了什么

| 项 | v0.0.1 ~ v0.1.0 | **v0.1.1** | 出处 |
|---|---|---|---|
| 自定义网络包 | **红线：0 个** | **该红线整体废除**；本版新增 1 个，**不设上限** | 裁定 #23；`BRIEF` §0.1 |
| Mixin | 无 | **无（唯一保留的红线）** | 裁定 #23；`BRIEF` §1.4（`@Mixin` 0 个、`build.gradle` 无 mixin 插件） |

0.1.0 的旧口径写在四处，它们是这次变更的对照面：`22-DECISIONS-0.1.0.md:44`（§1.1 的「两条红线」行）、`:68`（§1.3 边界）、`:480`（§6 第 4 条）；`26-PROMPT-MAIN-0.1.0.md:183-185`（HC-73）与 `:402`（HC 对照表）；`README.md:71`（中文红线段）、`:148`、`:191`；英文段 `:419`、`:423`。

### 4.2 四条读法（写错任何一条，六份下游文档全错）

1. **这是整体废除，不是「允许 1 个」**。没有数量上限，也没有「破例申请」这种动作空间——`BRIEF` §0.1 写的是「废除该限制（本版恰好新增 1 个，但不设上限）」。任何写成「本版允许 1 个自定义包」的表述都是错的。
2. **这不是「例外」**。原本的结构是「默认禁止 + 本次豁免」，现在没有那个「默认禁止」了；「例外」这个词暗示禁令仍在，因此禁用。
3. **废除不等于「必须加包」**。本版只加 1 个，因为浮窗确实需要（§4.3）；今后每新增一个包仍要单独立决策登记，不得顺手加。
4. **「无 Mixin」不受影响**：仍然是零，仍然禁止 `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor`。26 号 HC-73 里的第三项 `GameRules.register` 为 0 也不属于这两条红线，作为工程约束继续有效（D-146 补充）。

### 4.3 本版实际新增的 1 个网络包是什么、为什么非它不可

**是什么**：一个**服务端 → 客户端单向**的 payload，内容是一份「己方核心列表」——每个核给「维度 + 坐标 + 归属者显示名 + 是否自己」，排序由列表顺序承载（D-142 / D-143）。客户端只读、不回包、不做判定。

**为什么非它不可**（四条，任一条单独成立都足以否掉「0 包」这条路）：

1. 核心索引是**服务端 `SavedData`**（`outpost/OutpostCoreIndex.java`，落盘 `world/data/farmstorm_outpost_core.dat`），客户端没有副本（`BRIEF` §1.2）。
2. 「我有哪些核」必须是**全量**的：客户端只加载视距内的区块，视距外的核它根本不知道存在。
3. 归属者**可能离线**：0.1.0 的 `OutpostCoreIndex.owned(Player)` 只在在线时可用（`:85-90`），而「队友关系」只有服务端能解析（`DeployedOwnership.isAlly` 的 owner 侧是 UUID + `scoreboardName`）。
4. 列表需要**稳定顺序**（按放置顺序）与**行数上限 / +N 溢出**，这是在客户端合成一份快照才能做的事，逐条推送做不了（§6 第 9 条）。

**三条边界**：① 只读不写——客户端不回包，服务端权威不变（HC-03 / F-25 的既有约定）；② 不带判定——客户端拿到列表只用于绘制，任何「是不是己方」的判断仍以服务端为准；③ **不进注册面**（+0），payload 走 `RegisterPayloadHandlersEvent`，不进 `BuiltInRegistries`。

### 4.4 被否决的替代方案

六条替代方案全部记进 §6 的墓碑（编号栏给墓碑号，方便后来者直接查）：

| 替代方案 | 一句话否决理由 | 墓碑 |
|---|---|---|
| 服务端 config 驱动客户端 HUD 开关 | 服务端 config 改不了客户端渲染 | §6 第 2 条 |
| 只显示「已加载区块内」的核心 | 视距外的核永远不显示，「我有哪些核」答不了 | §6 第 3 条 |
| 强制加载区块让客户端看见 | 客户端只收视距内区块，加载了也不发 | §6 第 4 条 |
| 原版文本 UI（Boss 条 / 记分板） | 位置固定、不能拖拽、不能按核分行 | §6 第 5 条 |
| 隐形标记实体 + `CustomName` 携带坐标 | 字符串不是数据通道，且同样受视距限制 | §6 第 6 条 |
| 方块实体同步（`getUpdateTag` / `ClientboundBlockEntityDataPacket`） | 只覆盖客户端已知的方块，答不了「有哪些核」 | §6 第 9 条 |

### 4.5 要写新口径的文档

| 文档 | 要写什么 | 它现在写的是旧口径 |
|---|---|---|
| **28（本文）** | D-146 即新口径；§4 是解释 | —（新写） |
| **29 设计增量** | 同步面按 D-142 写（1 个 S2C 只读包）；不许再出现「0 个自定义网络包」 | `23-DESIGN-0.1.0.md:49`、`:99`、`:108` 为旧口径（冻结，不改） |
| **30 技术规格** | 网络包章节按 D-142 写；红线章节写「只留无 Mixin」；RT-31 的推送时机清单 | `24-TECH-0.1.0.md:497`、`:614`、`:951` |
| **31 任务拆分** | 验收判据不许再用「`grep CustomPacketPayload` 为 0」；改为「全仓只有 D-142 登记的那 1 个 S2C 包，且客户端不回包」 | `25-TASKS-0.1.0.md:270`、`:326`、`:428`、`:585`、`:624` |
| **32 主提示词** | HC-77 / N-75 写新红线段，并**显式声明取代 0.1.0 的 HC** | `26-PROMPT-MAIN-0.1.0.md:183-185`（HC-73）、`:289`、`:402` |
| **33 阶段提示词** | 各阶段的「红线复跑」项同步改成新口径 | `27-PROMPT-TASKS-0.1.0.md:212`、`:251`、`:259`、`:299`、`:467`、`:500`、`:519`、`:549`、`:552` |
| **README** | 红线段改为「无 Mixin」，并说明网络包限制已整体废除 | `README.md:71`、`:148`、`:191`、`:419`、`:423` |

**22 ~ 27 号已冻结，一个字都不改**：它们里面仍有「0 自定义网络包」的表述，那是 0.1.0 的历史口径。读者与实现者遇到冲突时，**以本文为准**（`BRIEF` §0.1 的 ⚠️、§6 第 3 条、RT-41）。

---

## 5. 与 0.1.0 决策的取代关系

下表只登记「0.1.0 说 X、本版说 Y」的条款；表里同时保留了标注「**保留**」的对照行（D-40、D-122 / D-123），用来显式声明「这些没被改」。表中标注「保留」的条款继续有效；**未列入本表的** 0.1.0 条款同样一律继续有效（例如 D-06 / C-9 的队伍实时解析）。

| 0.1.0 的条款（位置） | 状态 | 本版 | 为什么 |
|---|---|---|---|
| D-116 额度载体 = `/farmstorm farmlandlimit` + config 默认 0（`22` 号 D-116） | **取代** | D-139、D-140 | 裁定 #16：额度改为 `16 × 核数`，指令一并删除 |
| D-117 队伍额度求和 + 坐标集合记账（`22` 号 D-117） | **取代**（求和算式） | D-139；记账载体改 D-134 | 求和的对象从「个人 base × 人数」变成「核容量求和」；`byOwner` 改为绑定表的派生视图 |
| D-118 只在己方核 3×3 内可耕地、多核范围取并集（`22` 号 D-118） | **部分取代** | D-134、D-135 | 范围闸保留（仍是可耕地的必要条件），**归属**从「取并集」改为「唯一绑定的那个核」。这条叠加读法是**本文补充**（见「写作期回报」第 2 条） |
| D-120 第 1 项：每核 +16 额度（`22` 号 D-120） | **取代** | D-134、D-139 | +16 现在是**每核容量**，不再是叠加到 base 上的额度 |
| D-120 第 3 项：范围内己方耕地不被踩坏（`22` 号 D-120） | **取代**（判据） | D-138 | 从「遍历在线玩家反查」改为「绑定表 + UUID 索引」，离线也有效 |
| D-120 第 4 项：核被毁时范围内己方耕地全变泥土（`22` 号 D-120） | **取代** | D-136 | 先迁移、迁不动才变泥土，且只处理本核名下的耕地 |
| D-121 第 2 条：客户端零新增代码、零自定义网络包（`22` 号 D-121，`:302`） | **取代**（前半句） | D-142、D-143 | 浮窗是客户端新增代码 + 新增 1 个包；边界粒子本身仍走原版粒子包、不变 |
| D-119 据点核心的方块属性（`22` 号 D-119） | **扩充** | D-141 | 加 `.lightLevel(15)`，其余属性一字不动 |
| **0 自定义网络包**红线（`22` 号 §1.1 `:44`、§1.3 `:68`、§6 第 4 条 `:480`） | **整体废除** | D-146 | 裁定 #23；不是放宽、不是例外（§4.2） |
| C-6「0 个自定义 S2C 包」（`00-DECISIONS.md`） | **整体废除** | D-146 | 同上；C-6 的其余部分（音效 / 粒子走原版包）仍是实现事实，只是不再作为禁令 |
| D-50「三项侵入性效果不新增 `CustomPacketPayload`」（`10-DECISIONS-0.0.2.md:73-76`） | **项目级禁令废除** | D-146 | 禁令不再；它描述的事实（那三项效果的状态仍借隐藏 `MobEffect` 同步）不变，本版不动那三项 |
| 26 号 HC-73 的网络包半条（`26-PROMPT-MAIN-0.1.0.md:183-185`） | **废除** | D-146、D-142 | 32 号以 HC-77 重写；Mixin 半条与 `GameRules.register` 半条保留 |
| D-40「不引入 Mixin」 | **保留（不被取代）** | D-146 | 裁定 #23 唯一保留的红线 |
| D-122 每人 2 个核、D-123 核心贴图 32×32 | **保留** | — | 本版不碰；列在此处是为了防止下游把「据点核心有改动」误读成「这两条也改了」 |

读表要点：**四件事必须点名**——① 耕地模型（范围覆盖 → 绑定）；② 额度载体（base + 指令 → 核容量，无 base）；③ 指令载体（`/farmstorm farmlandlimit` → 删除，只剩客户端 `/farmstorm hud`）；④ 红线（两条 → 一条）。任何一份下游文档只要写到这四件事中的一件，就必须按本表落到对应的 D 号。

---

## 6. 已否决的方案（登记在此，防止后来者重提）

| # | 被否决的方案 | 否决理由 | 谁在什么时候否的 |
|---|---|---|---|
| 1 | 用「**范围内所有耕地都受保护**」代替绑定制 | ① 一块地同时受多个核保护，拆任意一个核都会连带另一核的地——那正是 `plan0.1.1.txt:14` 要修的 bug；② 无法回答「这块地归谁」，容量与额度都无处计算；③ 与「一个核最多管 16 块」直接冲突 | 作者，v0.1.1 访谈（裁定 #12、#17） |
| 2 | 用**服务端 config** 驱动客户端浮窗开关 | 服务端 config 改不了客户端渲染：`ModConfig.Type.SERVER` 的值只在服务端进程生效，客户端渲染线程读不到；客户端要持久化自己的设置只能走 CLIENT config。本仓唯一 `registerConfig` 是 SERVER（`Farmstorm.java:56`），`client/` 21 类里 0 个 config 读取（`BRIEF` §1.5） | 作者，v0.1.1 访谈（裁定 #21 选了客户端按键 + 客户端指令 + CLIENT config）；技术面由 `BRIEF` §1.4 / §1.5 记录 |
| 3 | 用「**只显示已加载区块内**的核心」代替全部显示 | 视距外的核永远不显示，「我有哪些核」这个需求直接落空；玩家在远处时浮窗会静默丢行，且丢哪几行取决于加载状态，不可复现 | 作者，v0.1.1 访谈（裁定 #22 选了推送全量列表） |
| 4 | 用**强制加载区块**让客户端看见（代替网络包） | 技术上不成立：客户端只收视距内区块，区块在服务端被 ticket 加载不会把它发给客户端；它与 D-145 的 ticket 是两件事，不可互换 | 作者，v0.1.1 访谈（裁定 #24 的 ticket 只用于服务端运行期，裁定 #22 选了网络包） |
| 5 | 用**原版文本 UI**（Boss 条 / 记分板）代替左上角浮窗 | 两者都是原版的全局显示通道，位置由原版固定（Boss 条上方居中、记分板右侧），既落不到左上角指定坐标，也不能拖拽、不能按核分行；塞进去还会与既有 Boss 条 / 侧边栏的用途冲突 | 作者，v0.1.1 访谈（裁定 #21 要求可拖拽 + 可隐藏） |
| 6 | 用「**隐形标记实体 + `CustomName` 携带坐标**」的 0 包 hack | ① `CustomName` 是显示字符串、不是数据通道，要在一维字符串里编码「维度 + 坐标 + 归属 + 顺序」；② 实体同样受「客户端只认自己加载的区块」限制，墓碑 3 的问题原样回来；③ 每核一个实体 = 每核一份实体同步与卸载处理；④ 在 D-146 废除红线之后，这个 hack 换来的「0 包」收益等于 0 | 作者，v0.1.1 访谈（裁定 #22 选了更干净的方案） |
| 7 | 用「**遮挡时自动挪到最靠近左上角的不遮挡处**」代替拖拽定位 | ① 需要每帧对浮窗与所有原版 UI / 其它模组 HUD 做遮挡求交，成本与不确定性都高；② 「最靠近左上角且不遮挡」在浮窗自身占满左上角时可能无解；③ 拖拽是确定性的，玩家能自己摆。**`plan0.1.1.txt:17` 原文要求自动避让，裁定 #21 改判为拖拽 + 不做自动避让** | 作者，v0.1.1 访谈（裁定 #21 明写「不做自动避让」） |
| 8 | 给 man瓜 加**软追踪**（复用潜影贝 `ShulkerBullet` 的追踪） | ① 软追踪要引入转向角上限与最大追击时长两个新参数，作者没给；② 复用原版追踪要换实体父类（现 `extends ThrowableItemProjectile`），等于重写炮弹；③ 原版追踪在贴脸时同样绕圈，解决不了作者实际看到的现象；④ 追踪会让炮弹变成「必中」，与 `TurretBullet` 的直线 + 服务端权威设计相反。**`plan0.1.1.txt:27` 原文要求追踪，裁定 #8 改判为只修近距离绕圈 / 抖动** | 作者，v0.1.1 访谈（裁定 #8） |
| 9 | 用**方块实体同步**（`getUpdateTag` / `ClientboundBlockEntityDataPacket`）代替自定义包 | ① 只在客户端加载了该方块所在区块时才有数据，「我有哪些核」它答不了；② 只覆盖「客户端已经知道的方块」，与墓碑 3 同类；③ 8 行排序列表需要一次性完整快照，而 BE 同步是按方块逐条发的，客户端还得自己拼顺序 | 作者，v0.1.1 访谈（裁定 #22）；技术面由 `BRIEF` §1.4 的 BE 同步先例（2 个）核对 |

---

## 7. 本版遗留（登记，不算缺陷）

以下五条是**本版有意不做的**，不是漏项。下游文档可以引用，但不得把它们写成待办或缺陷。

1. **「绿色 +」规则全局作废，但本版只重生成 `wild_apple` 一张，其余 24 张留到下版批量重生成**。25 张带 `+`（26 条里只有 `converter` 免叠），本版处理后剩 24 张仍是旧样（25 − 1 = 24）。规则已在代码层废掉（生成器删机制），所以下版重生成是纯执行、不需要再改决策（裁定 #2）。
2. **既有文档 22 / 26 / 27 号里 `wild_apple.png` 的「11 色 / 透明 126」是错的**，实测 **88 个去重 RGBA / 107 个全透明**（`22-DECISIONS-0.1.0.md:581`、`26-PROMPT-MAIN-0.1.0.md:199`、`27-PROMPT-TASKS-0.1.0.md:405`）。那三份已冻结，本版**不改**；新文档一律不得引用旧数值，要引用就引 D-126 的实测值（88 色 / 107 透明 / 571 B）。
3. **22 / 24 / 25 / 26 / 27 号里关于 `/farmstorm farmlandlimit` 的说明随指令删除而失效**（典型位置：`22` 号 D-116、§4.2 第 18 行；`README.md:191`）。那几份冻结不改，以 D-140 为准；README 属增量范围，其 `:191` 那句要随 D-147 的升级须知一起改（RT-40）。
4. **22 号 D-118 第 4 条「同一块耕地在两个核心范围里只算一块」在绑定制下仍成立**，但成立的理由从「计数去重」变成了「唯一绑定」（D-134）。22 号原文不改；这句话的**依据**变了，下游引用时要按 D-134 解释。
5. **朋友的韭「32 格可闻」的载体**：现由 `friend_chives.soundRange / 16 = 2.0` 的音量承担（`BRIEF` §1.8）；`sounds.json` 是否再补 `"attenuation_distance": 32` 属技术面（该键现未设、走原版默认 16，而同文件 `wild_hunter_loop` 已有 32 的先例）。移交 **30 号**在技术层核，**不改变 D-132 的结论**。

### 写作期回报（与 `BRIEF` 的冲突、以及 `BRIEF` 未覆盖的地方）

按 `BRIEF` §6 第 9 条与本文 §0.3 第 4 条，逐条列给作者，不自行取舍、不改事实卡：

1. **plan 的第 3 条需求没有对应的作者裁定**：`BRIEF` §2 的 25 条里没有「压缩干草块音效 GRASS → STONE」，它的依据只有 `plan0.1.1.txt:20-23` 与 `BRIEF` §1.6 / §3.2 第 3 行。本文把它补成 D-130；若作者认为它不该单独成条，删 D-130 后 D-131 ~ D-147 需整体重编号（会影响 §2 的映射表）。
2. **「锄地要不要先过 3×3 范围闸」裁定没说**：裁定 #13 只写「锄地成功时绑给距离最近的己方核」，没写距离条件；0.1.0 的 D-118 要求「只在己方核 3×3 内可耕地」。本文按「两道闸叠加」处理（范围闸仍在，D-135 补充），理由是 `plan0.1.1.txt:5` 的「除非本文明确要求，否则不能修改 0.1.0 原有内容」——plan 只要求修多核误铲，没有要求放开耕地范围；且 `FarmlandGuard` 现成的 `NO_CORE` 态就是为这条范围闸存在的。**若作者的本意是「范围闸取消、只要有核就能耕」，D-135 的结论与 §5 第 3 行要改。**
3. **老存档补建的匹配谓词裁定未给**：裁定 #15 只说「按新规则补建」，而新规则里唯一给出完整谓词的是 #14（覆盖 + 同盟 + 有空位，最近优先）。本文让 D-137 复用 #14 的谓词（标了「本文补充」）。若作者希望补建更宽松（例如只按同盟 + 就近、不看范围），D-137 要改。
4. **浮窗必须显示全部核心（含未加载区块与其它维度）——已按作者裁定修正**。本文初稿是在裁定 #20 只写「坐标 + 归谁」、坐标不含维度的情况下，默认推断成「按维度过滤、只列自己所在的那个维度」，**方向反了**。修订后的裁定 #19 / #20 明确要求**跨维度全量显示**；作者在访谈里选的就是「必须显示全部（含未加载、跳维度）」，而这正是促成裁定 #22（破红线加网络包）的那条需求。**这一条留痕，因为它是本轮「交叉核对抓出上游漏项」的实证**：浮窗的跨维度约束在事实卡搬迁时丢失，28 号的默认推断把错误方向固化了，靠 28 号与事实卡的逐条交叉核对才发现。D-143 已按修订后的 #19 / #20 重写（行首维度标识、`placedGameTime` 跨维度统一排序）。
5. **`BRIEF` 的一处小笔误**：§1.4 写「自定义网络包 0 个（实测 17 个关键字全 0 命中）」，而 §0.1 与 §2 第 23 条已把该红线废除——§1.4 那句是**变更前**的实测记录，读的时候要按「当时为 0」理解，不是「本版仍为 0」。本文所有「0 个」的引用都指变更前状态。
6. **`BRIEF` §2 表下注的 8 行上限、默认坐标 `(4,4)`、配色**明确标了「本卡补充（非作者裁定，可否决）」，本文把它们写进 D-143 / D-144 并标了来源。作者若要改，只改这三个值，不影响 D-142 的数据面与 D-144 的载体选择。

---

## 8. 裁决回灌记录（A-30 ~ A-53 中属于本文的 15 条，2026-11-02）

29 号（设计增量）、30 号（技术规格）、31 号（任务拆分）、32 号（主提示词）交稿后共立了 **15 条与本文有关**的上游瑕疵裁决（29 号 7 条 + 30 号 2 条 + 31 号 3 条 + 32 号 3 条），**全部回灌到本文**。裁决的正式登记在 31 号的 §裁决章（内部台账 `.work/v011-verdicts.md`），本节只记「改在哪条决策」，便于逐条对账。

| 裁决 | 一句话 | 改在哪条决策 | 日期 |
|---|---|---|---|
| A-30 | man瓜 炮弹速度提成 config 后，**发射点 `behavior/impl/PumpkinTurret.java:75` 也必须改读该键**——删常量不是单点改动 | D-129（落地位置 + 补充） | 2026-11-02 |
| A-31 | 压缩干草块改音效实际影响**五类**声音（破坏 / 放置 / 踩踏 / 挖掘 / 落地）；验收项因此比原估**多 2 条**（3 条 → 5 条） | D-130（补充） | 2026-11-02 |
| A-32 | 生成器的尺寸断言与 `expected_size()` 的「非 GUI 恒 16×16」必须**一起放开**，否则 64×64 直通输出会直接 `SystemExit` | §3.2 第 14 行、§3.3 第 8 行（依据 D-126） | 2026-11-02 |
| A-33 | 朋友的韭「200」的口径：**场长与施加基准都取 `friend_chives.effectTicks`**，施加量 = `effectTicks − x`；200 只是默认值 | D-131（结论 + 补充） | 2026-11-02 |
| A-34 | 浮窗「8 行」是**8 行核心**，`+N` **另加 1 行**——最高 **9 行** | D-143（结论第 4 项） | 2026-11-02 |
| A-35 | 核被毁时「找不到接替者 + 该耕地所在区块未加载」→ **只摘绑定、世界不动**（不为清算加载区块） | D-136（补充） | 2026-11-02 |
| A-36 | 老档补建的遍历顺序写死为 **`FarmlandLedger.byOwner` 的插入顺序（`LinkedHashSet`）** —— **已被 A-47 推翻**（改按 `BlockPos` 单级全序：y → z → x） | D-137（补充） | 2026-11-02 |
| A-38 | 强制加载的落盘文件不是 `level.dat` 而是 **`world/data/chunks.dat`**（`ForcedChunksSavedData.FILE_ID = "chunks"`）；`setChunkForced()` 与 `TicketController#forceChunk` **都会写它 ⇒ 两者都否决**，只用裸 `TicketType` + `addRegionTicket` | D-145（结论 + 补充）、§2 第 24 行 | 2026-11-02 |
| A-39 | 「平手按先放置」以 **`placedGameTime` 升序**为准、同刻按坐标字典序；`byOwner` 是 `HashMap`，**不得依赖任何 Map / 列表的插入顺序** | D-135（补充） | 2026-11-02 |
| A-45 | 踩踏保护面按「**有绑定即受保护**」（`protectsFromTrample` 没有查询者参数，答不了「谁踩的」）；额度闸与浮窗仍按实时己方判定 | D-138（补充） | 2026-11-02 |
| A-47 | 老档补建遍历顺序**推翻 A-36**：先按 **`BlockPos` 单级全序（`BlockPos#compareTo`：y → z → x）** 排序再逐条处理；「按 `byOwner` 插入顺序」作废（`byOwner` 是 `HashMap`，0.1.0 老档没有 `orphans` 可依；账本无维度字段 ⇒ 不设维度级） | D-137（补充） | 2026-11-02 |
| A-48 | 语言键算式补 `+2`（两个 `KeyMapping` 的描述键）；`k = 3` ⇒ **323** | §3.4 计数算式 | 2026-11-02 |
| A-50 | 投掷物基类在 **`entity/ThrownCropEntity.java`**，`entity/thrown/` 下没有它（`Test-Path` 实测） | D-131（落地位置） | 2026-11-02 |
| A-51 | `一键构建.cmd` 的 jar 名在 **`:11`（release）与 `:12`（sources）**，不是 `:10-11`（`:10` 是 `mkdir "dist"`） | D-147（依据 + 落地位置） | 2026-11-02 |
| A-53 | `OutpostCoreIndex.Entry` 加 `gameTime` 并落盘 = 本版**第二处**存档格式变更（第一处是耕地账本 v1 → v2） | §3.2 第 15 行、§3.4 存档格式 | 2026-11-02 |

三条口径说明：

1. 本节只收**回灌到本文**的裁决。落点在 31 号验收章或 30 号技术章的裁决不在本节范围——它们不改本文任何一个字的结论。
2. 每条裁决只动**对应决策的那几个字段**（表里已点名）与必要的交叉索引；其余决策、§2 的裁定行、§3 的既有行、§5、§6 一字不动（§2 只在表下加了一条勘误注，裁定行本身未改）。
3. 裁决与本文正文冲突时，以**回灌后的正文**为准，本节只是索引；日期统一记本轮写作日 2026-11-02。
