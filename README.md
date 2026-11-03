# 农场风云 / Farmstorm

> 把原版农作物改造成 PvP 道具：**种在地里、扔出去、架在墙上、或者吃掉。**

Minecraft **1.21.1** · NeoForge **21.1.213** · Java **21** · modid `farmstorm` · 当前版本 **v0.1.1**

[下载](https://github.com/redstonepigawa/farmstorm/releases) · [文档索引](#文档) · [构建方法](#构建与调试)

---

## 这是什么

一个把原版作物改造成战术道具的模组。

- **22 种改良作物档案**：投掷型 12 / 部署型 7 / 食用型 3。
- 两种**正交**分类：`CropType`（单值，决定怎么用）与 `CropCategory`（多值，决定属于哪类战术功能——陷阱 / 炮台 / 破障 / buff / 建造 / 伤害）。行为由标签驱动，不写死在调用点。
- 改造入口是**农产转换器**：三格加上一个显式按钮，两个觉醒花额外要下界之星作催化剂。
- 队伍归属走**原版记分板**（`TeamResolver` 抽象），不引入自定义阵营系统。

几条贯穿全项目的工程约定：服务端权威、**无 Mixin**、原生资源优先（不重新分发原版素材）。

---

## 给玩家

### 版本要求

**Minecraft 1.21.1 + NeoForge 21.1.x + Java 21**。用 PCL / HMCL 建对应的版本即可（实测 NeoForge 21.1.265 也能跑）。

### 安装

1. 用启动器建一个 1.21.1 + NeoForge 21.1.x 的实例。
2. 打开实例的 `mods` 文件夹（没有就新建），把 `farmstorm-0.1.1+mc1.21.1.jar` 丢进去。
3. 启动。创造模式物品栏最后会多一个「农场风云」标签页。

`farmstorm-0.1.1+mc1.21.1-sources.jar` 是源码包，给 IDE 看实现用，**不要**放进 `mods`。

### 这个版本会生成哪些配置文件

| 文件 | 内容 | 说明 |
|---|---|---|
| `config/farmstorm-server.toml` | 166 键 / 58 组 | 服务端行为；首次起服自动生成 |
| `config/farmstorm-client.toml` | 3 键（`hud.enabled` / `hud.x` / `hud.y`） | 客户端浮窗开关与位置；**首次进游戏**才生成，专用服务器不会生成它 |

浮窗的按键与指令：**H** 开关、**J** 进拖拽（单人世界会暂停），`/farmstorm hud on|off|reset` 也能改。

### 从 v0.1.0 升级必看

| 项 | 会发生什么 | 怎么办 |
|---|---|---|
| `pumpkin_turret` 的两个默认值改了（开火间隔 60→**40** 刻、伤害 1→**3**） | **旧 toml 里的值不会跟着变**——配置系统只补不删，代码不做迁移 | 删掉 `config/farmstorm-server.toml` 重新起服（会重生成 166 键 / 58 组），或手工改这两行 |
| 新增 `pumpkin_turret.bulletSpeed`（默认 **0.45**） | 自动补进旧文件 | 无需操作 |
| `/farmstorm farmlandlimit` **已删除** | 额度现在跟着核心走：`outpost_core.farmlandBonus`（默认 16）× 己方核数 | 用核心铺地盘；改这一个键会**同时**改每核容量与队伍总额度 |
| 老存档首次启动会**补建耕地绑定** | 控制台一条 INFO 带 N / M 两个数；补建只写账本、**不改世界**，补不上的田仍是耕地（只是不受保护） | 无需操作，日志里核对 N / M 即可 |

物品、配方、存档结构都向后兼容，不需要迁移存档。

### 已知限制（v0.1.1）

**这一版特有**

| 限制 | 现象 / 口径 |
|---|---|
| **浮窗需要装本模组的客户端** | 纯原版客户端仍能正常进服（服务端对未声明通道的连接跳过下发），但看不到浮窗——这是设计，不是报错 |
| 耕地坐标**不带维度** | 账本条目只存 `pos`。跨维度同坐标的两块耕地会互相「借」到踩踏保护、额度里被算成一块。0.1.0 起就有，本版不修（老档条目无法回溯维度） |
| **拆核只有两级**：改绑 / 变泥土 | 玩家不在场时拆核，本核名下的田**照样变泥土**。与 v0.1.0 的「拆核铲田」手感一致，这是本意，不是回归 |
| 64×64 物品贴图的显示面积仍是 **16 GUI px** | 原版 `item/generated` 的渲染事实，高出的细节会被平均；不是破图 |
| **不要重跑 `tools/gen_textures.py`** | 「图标右下角叠绿色 +」的规则自 v0.1.1 起已作废，重跑会把其余 24 张的绿十字一起剥掉。发布件以 jar 内提交时的图为准 |
| 核心所在的 3×3 区块会常驻内存 | 运行期临时加载、不写 `chunks.dat`、拆核即释放。准确说法是「**只有 1 个区块被 tick**，另 8 个被加载到 FULL」 |

**历史遗留（v0.0.2 起就有，本版未动）**

| 限制 | 现象 / 口径 |
|---|---|
| 灰度只作用于世界，HUD 保持原色 | 方块 / 实体 / 粒子变灰，物品栏、血条、聊天栏仍是彩色（原版 `PostChain` 在 GUI 绘制之前执行，无 Mixin 无法覆盖 HUD） |
| 桑吉尔蔗相位 2 的「视角锁定」只是降速 | `setMouseSensitivity(0)` 之后仍留有 0.2 的系数，相对默认约慢 15.6 倍，不是锁死 |
| F11 / F2 / F3 / ESC 拦不住 | 这四个键的原版处理早于任何 NeoForge 钩子 |
| 1×1 石柱构型的落地时长偏短 | 3×3 及以上台面恒为 40 ± 2 刻；1×1 石柱约 23 刻（玩家横向漂移出柱顶、被柱体截住），属真实几何结果 |
| 狂野猎人期间打开设置界面，亮度滑条显示为满 | 水下可视性需要临时抬高 `options.gamma()`，原版选项系统没有「临时覆盖」；关闭界面 / 效果结束即恢复 |
| 火把花光环离开范围后火焰附加最多再留 15 秒 | `TempEnchantHandler.revokeAll()` 目前没有调用者，只由 300 刻到期撤销 |
| 干耕地会毁苗 | 本模组作物不在原版 `maintains_farmland` 标签里，种在无水耕地会被退化顶掉（原版小麦不会） |
| 据点核心边界粒子每圈 48 个包 | 每人约 24 包/秒，是「四条边 + 每 4 格一点」精确轮廓的代价（原版粒子包只有一个原点） |

---

## 给开发者

### 前置

- **JDK 21** —— 仓库自带一份（`.tools/jdk21`），不需要另装。
- 本机网络到 `maven.neoforged.net` 的 TLS 被按 SNI 阻断，`.tools/gw.cmd` 会自己拉起一个本地镜像代理。**换一台正常机器**时按 `.tools/README.md` 删掉 java.base 补丁那两行即可。

### 四条命令

```cmd
cmd /c ""D:\project_farm\.tools\gw.cmd" build        :: 产出 build/libs/farmstorm-<版本>+mc1.21.1.jar
cmd /c ""D:\project_farm\.tools\gw.cmd" runClient    :: 起开发客户端
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer    :: 起专用服务器（首次需 run/eula.txt）
cmd /c ""D:\project_farm\.tools\gw.cmd" runData      :: 重新生成 src/generated/resources
```

`检查环境.cmd` / `一键构建.cmd` / `启动客户端.cmd` / `启动服务器.cmd` 是对上面四条命令的二次封装，双击即用。

### 为什么必须走 gw.cmd，不能直接调 gradle

`gw.cmd` 负责四件缺一不可的事：设 JDK 21 与 `GRADLE_USER_HOME` / `TEMP`、拉起 NeoForge 镜像代理、注入 java.base 补丁（本机 JDK 的 ACL 判定恒 false，会让 zipfs 变只读）、给 `runServer` 加 `run/` 目录锁。

直接调 `gradle` 在**本机**必然失败；并行跑两个 `runServer` 会撞 `world/session.lock`。并行取证时给每个服务端独立目录：`-PfsRunDir=run-xxx` + `FS_RUN_LOCK=run-xxx`（`run-*/` 已在 `.gitignore` 里）。

### 代码地图

```
src/main/java/com/dashpig/farmstorm/   （134 个 Java 文件；command/ 已在 v0.1.1 清空）
├── registry/     12 类注册面（items 32 / blocks 26 / block_entity 7 / entity 13 / mob_effect 4 /
│                 sound 3 / recipe 2 / creative_tab 1 / damage_type 3 / data_component 1 / menu 1 = 93）
├── behavior/     作物行为实现（投掷 / 部署 / 陷阱 / 炮台 / 食用 / 范围效果）
├── entity/       投掷物与实体（ThrownCropEntity 基类 + 12 个子类、TurretBullet 等）
├── blockentity/  部署方块与转换器、据点核心的方块实体
├── farmland/     耕地账本（绑定表）与保护判定
├── outpost/      据点核心索引与区块票
├── team/         TeamResolver（原版记分板）抽象
├── event/        Forge 事件挂载点（全局事件的总入口）
├── config/       FarmstormConfig（server）+ FarmstormClientConfig（client）
├── client/       客户端侧 25 个类：渲染、HUD、键位、拖拽、客户端指令
├── network/      唯一一个自定义包 farmstorm:core_list（S2C 只读）
├── datagen/      语言键 / 模型 / 战利品 / 配方生成
└── util/ recipe/ item/ menu/ trap/ combat/ explosion/ effect/ block/
```

更细的调用链、每个包的职责边界与 13 条已踩过的坑见 [`docs/zh/08-DEV-GUIDE.md`](docs/zh/08-DEV-GUIDE.md)。

### 静态验收工具

```bash
python -X utf8 tools/verify_project.py     # 四个段落，缺一段就是有问题
```

它从**文档**现算期望值再和产物比对，不手抄清单：

1. **config 键集** —— 从 `30-TECH §4.1` 的 58 组 / 166 键现算，扣掉已删键，再与 `run/config/farmstorm-server.toml` 逐键比；
2. **注册面** —— 93 个 ID 逐类核对全源码树；
3. **语言键** —— 两份 `lang/*.json` 的键集必须逐字相等（当前 323 × 2）；
4. **禁令扫描** —— Mixin / TODO / 调试残留 / 空 catch。

本版终态：`93 / 93` · `166 个配置键与文档逐字一致` · `lang 键集一致，共 323 键` · `结论：FAIL 0 项`，退出码 0。

> ⚠️ 这个工具有个**静默盲区**：往期望清单里加 ID 却不同步计数会报错，但两边都不动就一直全绿、静默漏检。新增注册对象时的固定顺序是「先加清单 → 再改计数 → 再加 config 来源」。

另外两个工具：`tools/gen_textures.py`（从原版客户端 jar 直读生成 113 张材质，**本版不要重跑**）、`tools/convert_media.ps1`（音频 / 图标转码，需要显式传 `-Ffmpeg`）。

### 蓝图与实现的实际差异

`.work/PLAN.md` 是唯一的实现台账：每一处「文档这么写、实际那样做」都登记了编号（R-xx 裁决、V-xx 实现判定）。动代码前先搜一下要改的地方有没有既存判定。

---

## 文档

中文版是**权威**，`docs/en/` 是它的翻译。两者冲突以中文为准。

本项目的规矩是**文档先行**：每个版本先出六份（决策 / 设计 / 技术 / 任务 / 主提示词 / 分阶段任务），代码按任务书实现。决策用 `D-xx`、任务用 `F-xx`、裁决用 `A-xx`、技术风险用 `RT-xx` 编号，改动必须回灌到编号所在的文档，不许只写在提交信息里。

### 现行文档（v0.1.1）

| 文件 | 内容 |
|---|---|
| [`docs/zh/28-DECISIONS-0.1.1.md`](docs/zh/28-DECISIONS-0.1.1.md) | **唯一真相源**：24 条决策 D-124~D-147、对 0.1.0 的增 / 改 / **删**三表、红线口径变更 |
| [`docs/zh/29-DESIGN-0.1.1.md`](docs/zh/29-DESIGN-0.1.1.md) | 设计增量：10 条需求 R1~R10、39 条边界 B-01~B-39、数值表与时序表 |
| [`docs/zh/30-TECH-0.1.1.md`](docs/zh/30-TECH-0.1.1.md) | 技术增量：注册面 93、动作面 47 行、config 全表、38 条验收自检、RT-31~RT-41 |
| [`docs/zh/31-TASKS-0.1.1.md`](docs/zh/31-TASKS-0.1.1.md) | 任务拆分：37 个任务 F-176~F-212、6 个 Phase、七张覆盖对照表 |
| [`docs/zh/32-PROMPT-MAIN-0.1.1.md`](docs/zh/32-PROMPT-MAIN-0.1.1.md) | 实现方主提示词：硬约束 HC-77~HC-96、禁止行为 N-75~N-92、陷阱章 |
| [`docs/zh/33-PROMPT-TASKS-0.1.1.md`](docs/zh/33-PROMPT-TASKS-0.1.1.md) | 分阶段任务清单：Phase 26~31 各一节，每节六小节 |

长期有效的基础文档：[`00-DECISIONS`](docs/zh/00-DECISIONS.md)（v0.0.1 的 41 条决策）、[`01-DESIGN`](docs/zh/01-DESIGN.md)、[`02-TECH-SPEC`](docs/zh/02-TECH-SPEC.md)、[`07-PUBLISH`](docs/zh/07-PUBLISH.md)（发布步骤与提交 / 排除清单）、[`08-DEV-GUIDE`](docs/zh/08-DEV-GUIDE.md)（开发者手册）、[`09-IMPL-VS-PLAN`](docs/zh/09-IMPL-VS-PLAN.md)（12 组实现与原计划的差异）。

### 历史文档（已冻结，只作对照）

`10`~`27` 号（v0.0.2 / v0.0.3 / v0.1.0 各六份）与 `03`~`06` 号（v0.0.1 期）**不再修改**。它们记的是当时的口径，与现行版本冲突时一律以 28 号为准。

> ⚠️ 其中关于「0 个自定义网络包」的说法**已全部作废**（见下面的红线一节），不要照旧文档实现。

### 实施台账

[`.work/PLAN.md`](.work/PLAN.md) —— 决策记录与文档 / 实现的差异登记。
`.work/TASK-V<版本>-P<Phase>.md` —— 各 Phase 的任务书；`.work/v011-verify/` —— v0.1.1 的 77 条验收记录。

---

## 版本历史

| 版本 | 日期 | 一句话 |
|---|---|---|
| v0.1.1 | 2026-11-03 | 浮窗（自己 + 队友全部核心，含跨维度）、耕地-核心绑定制、狂野苹果 64×64、man瓜 数值与贴脸收敛、朋友的韭持续场 |
| v0.1.0 | 2026-11-02 | 作物生长重做（20~45 秒）、陷阱四项、据点核心与耕地额度、蓄力条上移 |
| v0.0.4 | 2026-10-31 | Q弹 音效截断、效果对观察者同步、只锁移动、强制第三人称 |
| v0.0.3 | 2026-10-30 | Q弹、混乱、朋友的韭、近防菌、震动豆、刺球、桑吉尔蔗 |
| v0.0.2 | 2026-10-29 | 7 项新内容 + 三个客户端子系统（世界灰度、红色剪影、键鼠拦截） |
| v0.0.1 | 2026-10-28 | 15 种作物、转换器、64 个注册对象 |

## 数字快照（v0.1.1 发布件）

| 项 | 值 |
|---|---|
| 注册对象 | **93**（12 类逐类点名见 `verify_project.py`） |
| config 键 | server **166 键 / 58 组**；client **3 键**（独立文件，不计入 166） |
| 语言键 | **323 × 2**（中英键集逐字相等） |
| 改良作物档案 | 22（投掷 12 / 部署 7 / 食用 3） |
| 进度 / 材质 | 18 条进度 / 113 张 PNG（v0.1.1 就地替换 1 张，新增 0） |
| 发布件 | `farmstorm-0.1.1+mc1.21.1.jar` 1,063,181 B / SHA256 `B20E2EA305AE2F19…F3CF746F2A5` |

---

## 工程约束（红线）

**只剩一条红线：无 Mixin。**

`@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor` 全仓 0 命中。这一条从 v0.0.1 保留至今。

**v0.1.1 起，「0 个自定义网络包」这条红线整体废除**——本版恰好新增 1 个 S2C 只读包（`farmstorm:core_list`），但**不设数量上限**。这不是「放宽为 1 个」，也不是「本版例外」；`22`~`27` 号与 `00-DECISIONS` 的 C-6 里关于网络包的说法均已作废，以 `28-DECISIONS` §4 为准。

工程约束（不属于红线，但不许破）：`GameRules.register` **0** 命中；服务端侧不出现 `net.minecraft.client.*`；CLIENT 配置必须独立成 `farmstorm-client.toml`，不许混进 server 那份。

---

## 素材来源与非商业声明

本项目**不是全部原创**：下列素材取自第三方，**版权归各自权利人**，本项目不对其主张任何权利。

| 产物 | 来源 | 处理 |
|---|---|---|
| `sounds/wild_hunter_loop.ogg` | `Animals_from_3m04s.mp3`（作者指定取 3m04s 起的剪辑，47.14 s） | 转 mono / 64 kbps Ogg Vorbis（保留 48 kHz），作狂野猎人状态期间循环 |
| `sounds/zangief_piledriver.ogg` | SF6 视频片段 `BV1qu411W7ha`（846–850 段） | 同上，作桑吉尔蔗触发音 |
| `sounds/friend_chives.ogg` | `pengyoudejiu.mp3`（7.45 MB） | 转 mono / 64 kbps Ogg Vorbis，取前 10 秒，作朋友的韭命中音 |
| `textures/mob_effect/wild_hunter.png` | 作者提供的桌面 JPG | 居中裁切缩放为 18×18 |
| `textures/mob_effect/q_bounce.png` | Steam 创意工坊 `431960/3796093604/preview.jpg` | 居中裁切缩放为 18×18 |
| `textures/item/friend_chives.png`、`chive_seeds.png` | 作者提供的两张 QQ 图片 | 缩放为 16×16 |
| `textures/block/friend_chives_stage0..3.png` | **本项目自绘占位**（洋红 `#D81E8A`） | 不是第三方素材，出终稿时替换 |
| `textures/block/outpost_core_a_{top,side,bottom}.png` | 作者提供的 `outpost_core_all_textures/32x32/a_runic/` 三张 32×32 | 原样拷入，走 `cube_bottom_top` |
| `textures/item/wild_apple.png`（v0.1.1） | 作者提供的 `wild_apple_64x64.png`（64×64 / 6,946 B） | 原图直用，不缩放、不取色、不叠「+」 |

### 使用条件（务必照此分发）

1. **非商业**：不得将上述素材或其转换产物用于任何商业用途。
2. **署名**：转载、二次分发本模组时，必须连同本表一并保留。
3. **范围**：针对上表里所有非原版派生的第三方素材；原版贴图派生图另按 Minecraft 使用条款处理（见下）。
4. **可移除**：若权利人提出异议，维护者会移除对应素材并发布替代版本。

### 关于原版贴图派生图

`textures/item/*.png` 与 `textures/block/*.png` 是**从原版客户端 jar 读出来再变换**的派生图：受 **Minecraft EULA / Brand and Asset Guidelines** 约束，本项目不对其主张任何权利；仓库里只有变换后的小尺寸占位图，不含原版 jar 的原始文件，也不重新分发原版素材。若要替换，`tools/gen_textures.py --out` 指向任意目录重新生成即可，不影响代码。

### 已知风险（维护者注）

本仓库目前是**私有**的，上述素材属非商业自用范围。**若将来转为公开仓库**，即使仍然非商业，第三方音乐与游戏音效的再分发在这类项目的惯例中仍存在被投诉的可能。转公开前建议二选一：换成 CC0 / 原创音源（推荐），或保留本节的署名与非商业条款并接受随时可能需要撤下的风险。

> **发布 Release 时必须把这一节复制进 Release 说明**——jar 一旦分发就无法补正说明。

---

## 许可 / License

未定，由作者决定。

---

## English

A Minecraft 1.21.1 NeoForge mod that turns vanilla crops into PvP gadgets — plant them, throw them, mount them, or eat them.

**Current release: v0.1.1** (Minecraft 1.21.1 · NeoForge 21.1.213 · Java 21 · modid `farmstorm`).

- **Download**: [Releases](https://github.com/redstonepigawa/farmstorm/releases). Drop `farmstorm-0.1.1+mc1.21.1.jar` into `mods/`. The `-sources.jar` is for IDEs, not for `mods/`.
- **22 crop profiles** across three usage types (throwable 12 / deployable 7 / edible 3), converted through the Crop Converter block. Team allegiance resolves through the vanilla scoreboard.
- **Build** (never call `gradle` directly): `cmd /c ""D:\project_farm\.tools\gw.cmd" build`. Other tasks: `runClient`, `runServer`, `runData`.
- **Static check**: `python -X utf8 tools/verify_project.py` — expected output ends with `结论：FAIL 0 项` and exit code 0.
- **Hard constraint**: no Mixin (unchanged since v0.0.1). The former "zero custom network packets" rule is **abolished as of v0.1.1**; this version happens to add one read-only S2C packet (`farmstorm:core_list`) with no cap on the count.
- **Known limitations**: see the tables above (the Chinese section is authoritative). Highlights: the HUD requires this mod on the client; farmland ledger entries carry no dimension; core destruction always degrades farmland to dirt (rebind or till, never "leave alone"); 64×64 item textures still render at 16 GUI px.
- **Docs**: Chinese originals in `docs/zh/` are authoritative; `docs/en/` holds the translations. v0.1.1 spec lives in `docs/zh/28`–`33`.

Asset provenance and the non-commercial distribution terms are in the section above — **keep that section verbatim when redistributing or publishing a release**.
