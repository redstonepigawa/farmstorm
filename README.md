# 农场风云 / Farmstorm

> 把原版农作物改造成 PvP 道具：**种在地里、扔出去、架在墙上、或者吃掉。**

Minecraft **1.21.1** · NeoForge **21.1.213** · Java **21** · modid `farmstorm` · 当前发布 **v0.2.1**（**v0.2.2 只有文档，代码未实现**）

[下载](https://github.com/redstonepigawa/farmstorm/releases) · [文档索引](#文档) · [构建方法](#构建与调试)

---

## 这是什么

一个把原版作物改造成战术道具的模组。

- **22 种改良作物档案**：投掷型 12 / 部署型 7 / 食用型 3，按**品质三档**分：普通 **11** / 稀有 **7** / 史诗 **4**。
- 两种**正交**分类：`CropType`（单值，决定怎么用）与 `CropCategory`（多值，决定战术功能——陷阱 / 炮台 / 破障 / buff / 建造 / 伤害）。行为由标签驱动，不写死在调用点。
- **v0.2.0 起不能种植**：15 个作物方块与 22 个作物物品、7 个种子仍全部注册，但右键耕地不响应、不消耗；7 个部署型物品照常摆出。
- 改造入口是**农产转换器**（本版只作装饰，右键无反应，29 条配方与原方块全部保留）。
- **史诗档的唯一入口是农民交易**：农民在第 1 / 3 / 5 级各多一项本模组交易，只收绿宝石、每项只能换一次。
- 队伍归属走**原版记分板**（`TeamResolver` 抽象），不引入自定义阵营系统。

工程约定：服务端权威、**无 Mixin**、原生资源优先（不重新分发原版素材）。

---

## 给玩家

### 版本要求

**Minecraft 1.21.1 + NeoForge 21.1.x + Java 21**。用 PCL / HMCL 建对应的版本即可（实测 NeoForge 21.1.265 也能跑）。

### 安装

1. 用启动器建一个 1.21.1 + NeoForge 21.1.x 的实例。
2. 打开实例的 `mods` 文件夹（没有就新建），把 `farmstorm-0.2.1+mc1.21.1.jar` 丢进去。
3. 启动。创造模式物品栏最后会多一个「农场风云」标签页。

`farmstorm-0.2.1+mc1.21.1-sources.jar` 是源码包，给 IDE 看实现用，**不要**放进 `mods`。

### 这个版本会生成哪些配置文件

| 文件 | 内容 | 说明 |
|---|---|---|
| `config/farmstorm-server.toml` | **176 键 / 60 组** | 服务端行为；首次起服自动生成 |
| `config/farmstorm-client.toml` | **3 键**（`hud.enabled` / `hud.x` / `hud.y`） | 客户端浮窗开关与位置；**首次进游戏**才生成 |

### v0.2.1 有什么

**据点核心自动收编培养器（R1）**
- 每 **100 刻**扫一次自己的 3×3 区块，把还没绑定的培养器补进「少于 16」的名额；满 16 就停。
- 补绑有**自己的相位门**，不复用边界粒子那道 —— 把 `boundaryParticleIntervalTicks` 拉到 1200，补绑仍然是 100 刻内补上。
- 没有主人（命令放下的）核不收编；读不到的区块跳过，**不会为了补绑去加载区块**。
- 这是「运行期不再定期重绑」的一次性例外；选核判据 / 16 上限 / 一对一 / 核侧唯一真相全部保留。

**核心储物：90 格都显示原版 tooltip（R2）**
- 54 格储物 + 玩家背包共 90 格，悬停即出物品名与详情行；数量为 1 时不显示「1」，空格子不弹框。
- 这是「保证原版链路没被绕过」的验收项 —— 界面代码**一行未改**（`OutpostCoreScreen` 仍只覆写 `renderBg`）。

**震动豆与 Q弹 让生物停手（R3 / R4）**
- 震动豆：球内**所有**非玩家生物停手 **80 刻**（4 秒），不是只停触发那一只。
- Q弹：带效果的生物在效果存续期间一直停手；效果自然到期或中途清除，都在 1 刻内恢复。
- 恢复一律按**原值**写回：本来就 `noAi` 的怪不会因为踩了陷阱被放出来。
- AI 原值落盘在实体上（**第 13 类附件** `farmstorm:ai_suspend`），用完即删；停服走提前恢复，崩溃走载入结算。

**陷阱对敌对生物 ×3（R5）**
- 新增 `trap.damageMultiplier`（默认 **3.0**，范围 1.0 ~ 10.0）。
- 只有 `blast_carrot` 与 `zangief_cane` 参与：对敌对生物 = `min(raw, 7) × 倍率`（胡罗贝 **21.0** / 桑吉尔蔗 **22.5**）。
- 玩家恒按原值（胡罗贝最高 **7.0**，不进倍率）；中立 / 被动生物同样走原值；非陷阱爆炸在倍率 1.0 / 3.0 / 10.0 下**逐点相同**。
- 尖牙仙人掌走原版 `EvokerFangs` 不参与，震动豆本来就是 0 伤害。

### v0.2.0 有什么

**核心变成 54 格容器**
- 己方空手右键打开（6 行 × 9 列 + 玩家背包），标题「据点核心」；非己方打不开。
- 挖掉时整箱内容一起掉（活塞推动 / 同方块替换 / 区块卸载这三种「非真移除」不掉）。
- 距离闸走原版 `stillValid`，界面开着走远会被自动关掉。

**培养器（`farmstorm:cultivator`）**
- 放下时自动绑给**最近**的己方核（覆盖范围 = 核所在 3×3 区块），一个核最多 **16** 个。
- 四道闸：范围 / 绑定 / 手持品质（**史诗拒绝**）/ 空槽，任一不过 = 右键无反应。
- 每 **600 刻**（30 秒）把产出投进绑定的那个核：**普通与稀有**投 2 个作物；有独立种子的再加 2 个种子（准入面内 5 条给 2+2、13 条只给 2）。
- 核满了就停在 `cycleTicks` 不动、物品不丢；腾出格子后自动投递，**不重新计时**。
- 挖掉时掉自身 + 内部那一个。

**连线**
- 站在自家核的 3×3 区块内，能看到核与每个已绑定培养器之间的青蓝色线（两端各一个 0.25 格小框）。
- 走远线消失、换维度不残留、拆方块线立刻消失。这是**纯观察面**：关掉它不影响绑定 / 产出 / 迁移 / 掉落。

**品质与交易**
- 29 个物品名按档着色（普通白 / 稀有蓝 / 史诗金），tooltip 多一行「品质：稀有」。贴图一张没改。
- 农民 3 项交易：普通 2–5 / 稀有 10–32 / 史诗 40–64 绿宝石，单次给 2 个同档作物，`maxUses = 1`。

**撤销的东西**
- 0.1.1 的**耕地-核心绑定制整体停用**：不再拦锄地、不再有踩踏保护、两条动作栏提示不再出现。
- `/farmstorm farmlandlimit`（0.1.1 已删）本版不恢复；服务端没有 `/farmstorm` 指令树。
- 两个类（`FarmlandLedger` / `FarmlandGuard`）**保留在源码树里、仍编译**，只是没有运行期调用者；`farmstorm_farmland.dat` **不读不写不删**。

### 从 v0.2.1 升级必看

| 项 | 会发生什么 | 怎么办 |
|---|---|---|
| 两个默认值改了，**老存档照旧** | 现象：「我升级了，怎么还是 2？」——盘上的 `run/config/farmstorm-server.toml` 里 `maxPerPlayer` 仍是 **2**、`cultivatorCapacity` 仍是 **16**。新默认值 **4 / 24** 只对**新生成**的 toml 生效：NeoForge 只补键 / 删键，不改已存在键的值 | 两条任选：① 删掉 `run/config/farmstorm-server.toml`（发布件里就是实例目录下的 `config/farmstorm-server.toml`），下次起服让它**重生成**——会一并丢掉你自己改过的其它值；② 手改那两行：`maxPerPlayer = 4`、`cultivatorCapacity = 24` |
| config 键数**不变** | 仍是 **176 键 / 60 组**：本版只改两个默认值，**不新增 config 键**，所以不会出现「缺键」 | 无需操作 |
| 语言键 338 → **340** | 只多两条：`item.farmstorm.plain_chives`（平凡韭菜）与 `entity.minecraft.villager.farmstorm.botanist`（植物学家），中英各 1 条 | 无需操作；老存档里已刷出的村民不受影响 |
| 老档里已超过 4 个据点核心 | 上限只在**放置**时判定，没有任何回收路径 ⇒ 已经放着的不**会**被删掉，只是新的放置会被拒绝 | 无需操作 |
| 物品 / 配方 / 进度 / 方块 | 注册 ID 一个没变，只多 `plain_chives` 一个物品；老存档向后兼容 | 无需迁移存档 |

### 从 v0.2.0 升级必看

| 项 | 会发生什么 | 怎么办 |
|---|---|---|
| config 只多 1 个键 | `[trap]` 段新增 `damageMultiplier = 3.0`；server config 从 175 键变 **176 键 / 60 组** | 建议**删掉 `config/farmstorm-server.toml` 重新起服**；不删也能跑，缺失的键会按默认值补上 |
| 实体上新挂一个附件 | 第 13 类附件 `farmstorm:ai_suspend`；它**不进** registry 的 12 类计数，注册面仍是 **97** | 无需操作；老存档里没有这个附件，载入后照常工作 |
| 不新增 SavedData | `world/data/` 下的 `.dat` 文件数与 0.2.0 **相同** | 无需迁移 |
| 语言键 338 × 2 | 只多了 `trap.damageMultiplier` 的中英各 1 条 | 无需操作 |
| 物品 / 配方 / 进度 / 方块 | 全部向后兼容，注册 ID 一个没变 | 无需迁移存档 |

### 从 v0.1.1 升级必看

| 项 | 会发生什么 | 怎么办 |
|---|---|---|
| config 组结构变了 | `farmland` 组消失、新增 `[trade]` 6 键与 `[cultivator]` 2 键、`[outpost_core]` 从 3 键变 4 键（删 `farmlandBonus`、加 `cultivatorCapacity` / `storageSlots`） | **删掉 `config/farmstorm-server.toml` 重新起服**（落到当前版本会重生成 **176 键 / 60 组**）；不删也能跑，但旧键会残留 |
| 作物不能种了 | 手里的作物 / 种子右键耕地没反应（不消耗） | 这是本版设计；作物改为走交易与培养器 |
| 老档的核心 | 读成 **54 个空槽**，主人与放置时间保留 | 无需操作 |
| 老档满级农民 | **不会补发**那 3 个交易项 | 本版不做迁移；新刷的村民才有 |
| 老档的耕地 | `farmstorm_farmland.dat` 原样留在存档里，**不再被读写** | 无需操作，也不会被删 |

物品、配方、进度都向后兼容，不需要迁移存档。

### 已知限制（v0.2.1）

**本版特有（v0.2.2）**

| 限制 | 现象 / 口径 |
|---|---|
| **每个植物学家只卖 22 项里的 2 项** | 植物学家的普通交易共 **22 项**、全部挂在新手级，但原版每级只从该级列表里抽 **2 项**（`Villager.java:875`）⇒ 一个植物学家只随机卖这 22 项里的 **2 项**，剩下 20 项要看别的植物学家。**多找几个植物学家才能凑齐**，这不是交易表坏了（裁定 #17 / D-198） |
| **一个植物学家只卖 1 个史诗作物** | 4 个史诗作物里，每个植物学家在**首次生成报价时**随机抽 **1 个**，之后整个存档不变（补货不换项）。凑齐 4 个同样要**多找几个植物学家**（裁定 #18 / D-199） |

**本版特有（v0.2.1）**

| 限制 | 现象 / 口径 |
|---|---|
| **悬停 tooltip 的观感未经人眼验收** | 本版验收跑在专用服务端 + 协议机器人上，没有渲染面。代码层证据是「界面没有覆写 `renderTooltip`、90 格全部交给原版 `Slot`」；实际显示效果需要玩家确认 |
| 补绑是**每核心 100 刻一次** | 满 16 的核不补；无主核不补；读不到的区块跳过、不为补绑加载区块。先放培养器后放核，要等下一轮才会被认领 |
| `trap.damageMultiplier` **只作用于 2 个陷阱作物** | `blast_carrot` / `zangief_cane` 参与；`fang_trap_cactus` 走原版 `EvokerFangs`、`vibration_bean` 本来就是 0 伤害 |
| 「3×3 里某区块读不到」这条边界**构造不出来** | 核自己的区块票 `distance = 1` 就把 3×3 的 9 个区块全加载了（A-43），该前提在核活着时不成立。详见 `.work/v021-verify/boundaries.md` 的 `B-03` / `B-12` |

**v0.2.0 遗留（本版未动）**

| 限制 | 现象 / 口径 |
|---|---|
| **培养器在 3×3 区块内才可用** | 范围闸复用 `OutpostCoreBlockEntity.inRange`（核所在区块 ±1 区块）。核被拆后会迁移；迁不动的培养器留原地但**永久未绑定、不可使用**（只能拆掉重放） |
| 培养器**不自动认领** | 先放培养器、后放核，那个培养器仍然未绑定 |
| **史诗作物无法进入培养器** | 4 个史诗作物与 2 个史诗种子一律拒绝；史诗档只能靠农民交易拿 |
| 培养器**没有界面** | 右键直接放入 / 取出，没有槽位 GUI |
| 连线的**视觉**未经人眼验收 | 服务端发包、客户端收包、10 分钟不崩都有实测；线框颜色与观感需要玩家确认 |
| **浮窗与连线需要装本模组的客户端** | 纯原版客户端仍能进服（服务端对未声明通道的连接跳过下发），但看不到它们 |
| 耕地坐标**不带维度** | `farmstorm_farmland.dat` 只有 `pos`；本版已停用该文件，因此不再影响玩法 |

**历史遗留（v0.0.2 起就有，本版未动）**

| 限制 | 现象 / 口径 |
|---|---|
| 灰度只作用于世界，HUD 保持原色 | 方块 / 实体 / 粒子变灰，物品栏、血条、聊天栏仍是彩色 |
| 桑吉尔蔗相位 2 的「视角锁定」只是降速 | 相对默认约慢 15.6 倍，不是锁死 |
| F11 / F2 / F3 / ESC 拦不住 | 这四个键的原版处理早于任何 NeoForge 钩子 |
| 1×1 石柱构型的落地时长偏短 | ≥3×3 台面恒为 40 ± 2 刻；1×1 石柱约 23 刻（几何结果） |
| 狂野猎人期间打开设置界面，亮度滑条显示为满 | 水下可视性需要临时抬高 `options.gamma()`，关闭界面即恢复 |
| 火把花光环离开范围后火焰附加最多再留 15 秒 | 只由 300 刻到期撤销 |
| 干耕地会毁苗 | 本模组作物不在原版 `maintains_farmland` 标签里 |
| 据点核心边界粒子每圈 48 个包 | 「四条边 + 每 4 格一点」精确轮廓的代价 |

---

## 给开发者

### 前置

- **JDK 21** —— 仓库自带一份（`.tools/jdk21`），不需要另装。
- 本机到 `maven.neoforged.net` 的 TLS 被按 SNI 阻断，`.tools/gw.cmd` 会自己拉起本地镜像代理。**换一台正常机器**时按 `.tools/README.md` 删掉 java.base 补丁那两行即可。

### 四条命令

```cmd
cmd /c ""D:\project_farm\.tools\gw.cmd" build        :: 产出 build/libs/farmstorm-<版本>+mc1.21.1.jar
cmd /c ""D:\project_farm\.tools\gw.cmd" runClient    :: 起开发客户端
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer    :: 起专用服务器（首次需 run/eula.txt）
cmd /c ""D:\project_farm\.tools\gw.cmd" runData      :: 重新生成 src/generated/resources
```

### 为什么必须走 gw.cmd

`gw.cmd` 负责四件缺一不可的事：设 JDK 21 与 `GRADLE_USER_HOME` / `TEMP`、拉起 NeoForge 镜像代理、注入 java.base 补丁（本机 JDK 的 ACL 判定恒 false，会让 zipfs 变只读）、给 `runServer` 加 `run/` 目录锁。直接调 `gradle` 在本机必然失败；并行起服务端要给独立目录：`-PfsRunDir=run-xxx` + `FS_RUN_LOCK=run-xxx`（`run-*/` 已在 `.gitignore`）。

### 代码地图

```
src/main/java/com/dashpig/farmstorm/
├── registry/     12 类注册面：items 33 / blocks 27 / block_entities 8 / entity 13 / mob_effect 4 /
│                 sound 3 / recipe 2 / creative_tab 1 / damage_type 3 / data_component 1 / menu 2 = 97
├── blockentity/  部署方块、转换器、据点核心（54 格容器）、培养器
├── block/        方块类与它们的交互 / 掉落 / 放置绑定
├── behavior/     作物行为（投掷 / 部署 / 陷阱 / 炮台 / 食用 / 范围效果）
├── entity/       投掷物与实体
├── network/      2 个 S2C 只读包：core_list（浮窗全量）+ cultivator_link（连线范围增量）
├── client/       客户端：渲染、HUD、键位、拖拽、屏幕、连线线框
├── menu/         核心 54 格菜单
├── event/        事件挂载点（含村民交易）
├── config/       FarmstormConfig（server 176 键）+ FarmstormClientConfig（client 3 键）
├── datagen/      语言键 / 模型 / 战利品 / 配方生成
├── farmland/     【0.2.0 已停用】两个类保留、仍编译、无运行期调用者
└── util/ recipe/ item/ trap/ combat/ explosion/ effect/ outpost/ team/
```

更细的调用链与已踩过的坑见 [`docs/zh/08-DEV-GUIDE.md`](docs/zh/08-DEV-GUIDE.md)。

### 静态验收工具

```bash
python -X utf8 tools/verify_project.py
```

它从**文档**现算期望值再与产物比对。v0.2.1 终态四段同时成立：

```
[OK]   176 个配置键与文档逐字一致（toml 176 键）
      已核 registry 对象: 97 / 97
[OK]   lang 键集一致，共 338 键
结论：FAIL 0 项，WARN 1 项
```

> v0.2.1 起 `42-TECH §4.1.1` 也进了读取面（新解析器 `expected_config_keys_v021`）：**加键不加解析器会让工具当场报红带文件名 + 行号**，不再有「键加了但没人核」的假绿。

> ⚠️ **只写「FAIL 0」是假绿**：这个工具曾经在只核 93 个对象的情况下也退出 0。引用结论时必须同时给出 `97 / 97`。新增注册对象时的固定顺序是「先加清单 → 再改计数 → 再加 config 来源 → 最后改打印串」。

---

## 文档

中文版是**权威**，`docs/en/` 是它的翻译。规矩是**文档先行**：每个版本先出六份（决策 / 设计 / 技术 / 任务 / 主提示词 / 分阶段任务），代码按任务书实现；决策 `D-xx`、任务 `F-xx`、裁决 `A-xx`、风险 `RT-xx`，改动必须回灌到编号所在文档。

### 现行文档（v0.2.1）

| 文件 | 内容 |
|---|---|
| [`docs/zh/40-DECISIONS-0.2.1.md`](docs/zh/40-DECISIONS-0.2.1.md) | **唯一真相源**：14 条决策 D-173~D-186 + **17 条作者裁定** |
| [`docs/zh/41-DESIGN-0.2.1.md`](docs/zh/41-DESIGN-0.2.1.md) | 设计增量：5 条需求 R1~R5、42 行数值表、36 条边界 B-01~B-36 |
| [`docs/zh/42-TECH-0.2.1.md`](docs/zh/42-TECH-0.2.1.md) | 技术增量：注册面 **97（+0）**、动作面 31 行、**31 条验收**、10 条待验证 API、RT-51~RT-60b |
| [`docs/zh/43-TASKS-0.2.1.md`](docs/zh/43-TASKS-0.2.1.md) | 任务拆分：**20 个任务 F-247~F-266**、6 个 Phase 40~45、六张覆盖表、A-70~A-81 |
| [`docs/zh/44-PROMPT-MAIN-0.2.1.md`](docs/zh/44-PROMPT-MAIN-0.2.1.md) | 实现方主提示词：HC-121~HC-137、N-115~N-126、**41 条陷阱** |
| [`docs/zh/45-PROMPT-TASKS-0.2.1.md`](docs/zh/45-PROMPT-TASKS-0.2.1.md) | 分阶段任务清单：Phase 40~45 各一节，45 条出口判据 |

长期有效的基础文档：[`00-DECISIONS`](docs/zh/00-DECISIONS.md)、[`01-DESIGN`](docs/zh/01-DESIGN.md)、[`02-TECH-SPEC`](docs/zh/02-TECH-SPEC.md)、[`07-PUBLISH`](docs/zh/07-PUBLISH.md)、[`08-DEV-GUIDE`](docs/zh/08-DEV-GUIDE.md)、[`09-IMPL-VS-PLAN`](docs/zh/09-IMPL-VS-PLAN.md)。

### 下一版文档（v0.2.2，代码未实现）

六份已写完并冻结，**代码尚未开始**，且**只出中文**（`docs/en/` 保持在 v0.2.1）。想实现它的人从这里进：

| 文件 | 内容 |
|---|---|
| [`docs/zh/46-DECISIONS-0.2.2.md`](docs/zh/46-DECISIONS-0.2.2.md) | **唯一真相源**：16 条决策 D-187~D-202 + **18 条作者裁定** |
| [`docs/zh/47-DESIGN-0.2.2.md`](docs/zh/47-DESIGN-0.2.2.md) | 设计增量：5 条需求 R1~R5、数值表、27 条边界 B-01~B-27、附录 A 六行差异 |
| [`docs/zh/48-TECH-0.2.2.md`](docs/zh/48-TECH-0.2.2.md) | 技术增量：注册面 **98**、口径外累计 **3 类**、动作面 33 行、**30 条验收**、10 条待验证 API、RT-62~RT-70 |
| [`docs/zh/49-TASKS-0.2.2.md`](docs/zh/49-TASKS-0.2.2.md) | 任务拆分：**19 个任务 F-267~F-285**、8 个 Phase 46~53、六张覆盖表、A-82~A-91 |
| [`docs/zh/50-PROMPT-MAIN-0.2.2.md`](docs/zh/50-PROMPT-MAIN-0.2.2.md) | 实现方主提示词：HC-138~HC-155、N-127~N-140、**55 条陷阱** |
| [`docs/zh/51-PROMPT-TASKS-0.2.2.md`](docs/zh/51-PROMPT-TASKS-0.2.2.md) | 分阶段任务清单：Phase 46~53 各一节，52 条出口判据 |

投喂顺序：**先投 50 号一次**，之后每个 Phase 开工时只投 51 号对应的那一节。

四条需求，两个 bug：

- **植物学家**（新村民职业）：职业方块就是**据点核心**（有主的也能当、一核一村民）。普通交易 **22 项**全在新手级、3-6 绿宝石、8 次封顶；另 1 项史诗 @大师级、40-64、**一次**。
  ⚠️ **已知限制**：原版每级只抽 2 项（`Villager.java:875`），所以**每个植物学家只会随机卖 22 项里的 2 项**——要多找几个才能凑齐，不是交易表坏了。
- **平凡农作物**：`plain_chives`（平凡韭菜），可食、不做合成。只补 **1 个**——22 个作物里只有「朋友的韭」没有对应的原版农作物。
- **方块标签** `farmstorm:processing_block`：可被核心绑定的处理方块，本版只有培养器带它；绑定名额 **16 → 24**（所有带该标签的方块**共享**）。
- **两个 bug**：核心储物界面**没有 tooltip**（1.21.1 的容器界面不自带，必须每个界面自己调 `renderTooltip`）；背包物品**比格子高 1 px**（原版用两次 blit、本模组用一次）。外加放置上限 **2 → 4**。

目标值（实现后 `数字快照` 才会有这些数）：注册对象 **98**（+1）、**口径外累计 3 类**（`ATTACHMENT_TYPES` / `VillagerProfession` / `PoiType`，都不计入 98）、server config **176 键 / 60 组不变**（只改两个默认值）、语言键 **340 × 2**、网络包仍 **2 个**（不新增）。

> ⚠️ **改 config 默认值不会更新已存在的 toml**：老存档升级后 `maxPerPlayer` 仍是 2、`cultivatorCapacity` 仍是 16，要删掉 `config/farmstorm-server.toml` 重生成才拿到 4 / 24。

### 上一版文档（v0.2.0，已归档）

口径以当时为准；与 v0.2.1 冲突时以 **40 号**为准。

| 文件 | 内容 |
|---|---|
| [`docs/zh/34-DECISIONS-0.2.0.md`](docs/zh/34-DECISIONS-0.2.0.md) | **唯一真相源**：25 条决策 D-148~D-172 + 19 条作者裁定 |
| [`docs/zh/35-DESIGN-0.2.0.md`](docs/zh/35-DESIGN-0.2.0.md) | 设计增量：11 条需求 R1~R11、**40 条边界 B-01~B-40** |
| [`docs/zh/36-TECH-0.2.0.md`](docs/zh/36-TECH-0.2.0.md) | 技术增量：注册面 97、动作面 46 行、config 全表、**36 条验收**、13 条待验证 API、RT-42~RT-50 |
| [`docs/zh/37-TASKS-0.2.0.md`](docs/zh/37-TASKS-0.2.0.md) | 任务拆分：**34 个任务 F-213~F-246**、8 个 Phase 32~39、六张覆盖表、A-62~A-69 |
| [`docs/zh/38-PROMPT-MAIN-0.2.0.md`](docs/zh/38-PROMPT-MAIN-0.2.0.md) | 实现方主提示词：HC-97~HC-120、N-93~N-114、31 条陷阱 |
| [`docs/zh/39-PROMPT-TASKS-0.2.0.md`](docs/zh/39-PROMPT-TASKS-0.2.0.md) | 分阶段任务清单：Phase 32~39 各一节 |

本版已实现：**67 条**验收（31 技术 + 36 边界）跑完，失败 0；落地记录见下面的「实施台账」。

已实现（v0.2.1 发布件）：注册对象 **97**（+0，另有 1 个**口径外**的附件类型 `farmstorm:ai_suspend`，不计入 12 类）、server config **176 键 / 60 组**、语言键 **338 × 2**、网络包 **2 个**。

### 历史文档（已冻结）

`03`~`06`（v0.0.1 期）与 `10`~`33`（v0.0.2 / v0.0.3 / v0.1.0 / v0.1.1 各六份）**不再修改**，它们记的是当时的口径；与现行版本冲突时以 **34 号**为准，涉及 v0.2.1 的内容以 **40 号**为准，涉及 v0.2.2 的内容以 **46 号**为准。

> ⚠️ 关于「0 个自定义网络包」的说法**已全部作废**；`22`~`33` 里关于耕地绑定制的说法也随 v0.2.0 停用。

### 实施台账

[`.work/PLAN.md`](.work/PLAN.md) 与 [`.work/V021-DISPATCH.md`](.work/V021-DISPATCH.md)；各 Phase 的任务书与证据 `.work/TASK-V021-P*.md`；v0.2.1 的 **67 条**验收记录（31 技术 + 36 边界）与签字在 [`.work/v021-verify/`](.work/v021-verify)。上一版的 76 条在 [`.work/v020-verify/`](.work/v020-verify)。

---

## 版本历史

| 版本 | 日期 | 一句话 |
|---|---|---|
| v0.2.2 | —（**文档阶段**，代码未实现） | 植物学家职业（22 项普通 + 1 项史诗，职业方块就是据点核心）、平凡韭菜、`processing_block` 标签与 24 名额、核心界面的 tooltip 与 1 px 错位两个 bug、放置上限 4 |
| v0.2.1 | 2026-11-05 | 核心每 100 刻自动补绑培养器、核心储物 90 格悬停显示详情、震动豆与 Q弹 让生物停手（AI 原值落盘）、陷阱对敌对 ×3（`trap.damageMultiplier`） |
| v0.2.0 | 2026-11-04 | 核心变 54 格容器、培养器（16/核 · 600 刻 · 按档准入）、农民交易给史诗档、连线线框；撤销耕地绑定、作物不可种植、转换器停功能 |
| v0.1.1 | 2026-11-03 | 浮窗（自己 + 队友全部核心，含跨维度）、耕地-核心绑定制、狂野苹果 64×64、man瓜 数值与贴脸收敛、朋友的韭持续场 |
| v0.1.0 | 2026-11-02 | 作物生长重做（20~45 秒）、陷阱四项、据点核心与耕地额度、蓄力条上移 |
| v0.0.4 | 2026-10-31 | Q弹 音效截断、效果对观察者同步、只锁移动、强制第三人称 |
| v0.0.3 | 2026-10-30 | Q弹、混乱、朋友的韭、近防菌、震动豆、刺球、桑吉尔蔗 |
| v0.0.2 | 2026-10-29 | 7 项新内容 + 三个客户端子系统（世界灰度、红色剪影、键鼠拦截） |
| v0.0.1 | 2026-10-28 | 15 种作物、转换器、64 个注册对象 |

## 数字快照（v0.2.1 发布件）

| 项 | 值 |
|---|---|
| 注册对象 | **97**（v0.2.1 **+0**；另有 1 个**口径外**的附件类型 `farmstorm:ai_suspend`，不计入 12 类） |
| config 键 | server **176 键 / 60 组**（v0.2.0 的 175 + `trap.damageMultiplier`）；client **3 键**（独立文件，不计入 176） |
| 语言键 | **338 × 2**（中英键集逐字相等） |
| 自定义网络包 | **2**（`core_list` + `cultivator_link`，都 S2C 只读，协议串 `2`） |
| 改良作物档案 | 22（普通 11 / 稀有 7 / 史诗 4） |
| 进度 / 材质 | 18 条进度；本版**未改任何贴图** |
| 验收 | **67 条** = 31 技术 + 36 边界；分布 = 通过 **59** + 构造不可达 **3**（`#9` / `B-03` / `B-12`）+ 观感【未验证】 **5**；**失败 0**。20 个任务（F-247~F-266）全部落地 |
| 发布件 | `farmstorm-0.2.1+mc1.21.1.jar` **1,131,260 B** / SHA256 `7DE77738866599510D252286209074317B98130876551D62DC0873D4AC686A5A` |
| 源码包 | `farmstorm-0.2.1+mc1.21.1-sources.jar` **1,054,637 B** / SHA256 `15F76E5883ED32DAC6DC1EB3F196E18213DAB259B75130546A1AAF257F41305B` |

---

## 工程约束（红线）

**只剩一条红线：无 Mixin。** `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor` 全仓 **0**。

**自定义网络包不设数量上限**：v0.1.1 起「0 个自定义网络包」这条红线整体废除；v0.2.0 累计 **2** 个（`farmstorm:core_list` 与 `farmstorm:cultivator_link`），**都是 S2C 只读、客户端都不回包**（`playToServer|sendToServer` 0 命中）。

工程约束（不属红线但不许破）：`GameRules.register` **0**；服务端侧不出现 `net.minecraft.client.*`；CLIENT 配置必须独立成 `farmstorm-client.toml`。

---

## 素材来源与非商业声明

本项目**不是全部原创**：下列素材取自第三方，**版权归各自权利人**，本项目不对其主张任何权利。

| 产物 | 来源 | 处理 |
|---|---|---|
| `sounds/wild_hunter_loop.ogg` | `Animals_from_3m04s.mp3`（作者指定取 3m04s 起的剪辑，47.14 s） | 转 mono / 64 kbps Ogg Vorbis（保留 48 kHz） |
| `sounds/zangief_piledriver.ogg` | SF6 视频片段 `BV1qu411W7ha`（846–850 段） | 同上 |
| `sounds/friend_chives.ogg` | `pengyoudejiu.mp3`（7.45 MB） | 转 mono / 64 kbps Ogg Vorbis，取前 10 秒 |
| `textures/mob_effect/wild_hunter.png` | 作者提供的桌面 JPG | 居中裁切缩放为 18×18 |
| `textures/mob_effect/q_bounce.png` | Steam 创意工坊 `431960/3796093604/preview.jpg` | 居中裁切缩放为 18×18 |
| `textures/item/friend_chives.png`、`chive_seeds.png` | 作者提供的两张 QQ 图片 | 缩放为 16×16 |
| `textures/block/friend_chives_stage0..3.png` | **本项目自绘占位**（洋红 `#D81E8A`） | 不是第三方素材 |
| `textures/block/outpost_core_a_{top,side,bottom}.png` | 作者提供的 `outpost_core_all_textures/32x32/a_runic/` 三张 32×32 | 原样拷入 |
| `textures/item/wild_apple.png`（v0.1.1） | 作者提供的 `wild_apple_64x64.png`（64×64 / 6,946 B） | 原图直用，不缩放、不取色 |
| `textures/block/cultivator_a_{top,side,bottom}.png` + 两张静态首帧（v0.2.0） | 作者提供的 `cultivator_a_animation_pack`（37 PNG + 2 mcmeta） | 帧条原样拷入；静态首帧取 `editable_frames/{side,top}_00.png` |

### 使用条件（务必照此分发）

1. **非商业**：不得将上述素材或其转换产物用于任何商业用途。
2. **署名**：转载、二次分发本模组时，必须连同本表一并保留。
3. **可移除**：若权利人提出异议，维护者会移除对应素材并发布替代版本。

### 关于原版贴图派生图

`textures/item/*.png` 与 `textures/block/*.png` 里大部分是**从原版客户端 jar 读出来再变换**的派生图：受 **Minecraft EULA / Brand and Asset Guidelines** 约束，本项目不对其主张任何权利；仓库里只有变换后的小尺寸占位图，不含原版 jar 的原始文件。

### 已知风险（维护者注）

本仓库目前是**私有**的，上述素材属非商业自用范围。**若将来转为公开仓库**，第三方音乐与游戏音效的再分发仍存在被投诉的可能。转公开前建议换成 CC0 / 原创音源，或保留本节的署名与非商业条款并接受随时撤下。

> **发布 Release 时必须把这一节复制进 Release 说明**——jar 一旦分发就无法补正说明。

---

## 许可 / License

未定，由作者决定。

---

## English

A Minecraft 1.21.1 NeoForge mod that turns vanilla crops into PvP gadgets.

**Current release: v0.2.1** (Minecraft 1.21.1 · NeoForge 21.1.213 · Java 21 · modid `farmstorm`).

A **v0.2.2 document set is written and frozen but not implemented**, and it is **Chinese-only** (no English translation for this round): a Botanist villager profession whose job site is the Outpost Core itself (22 normal trades at level 1 + 1 epic trade at level 5), a plain crop (`plain_chives`), a `farmstorm:processing_block` block tag with a shared 24-binding cap, and two Outpost Core UI bugs (missing tooltips, 1 px inventory offset) plus a placement cap of 4. Registry 97 → **98** (plus **3** registried types *outside* the count), config stays **176** (two defaults change), language keys 337 → **340**.

- **Download**: [Releases](https://github.com/redstonepigawa/farmstorm/releases). Drop `farmstorm-0.2.1+mc1.21.1.jar` into `mods/`; the `-sources.jar` is for IDEs.
- **What's new in 0.2.1**: cores auto-rebind unbound cultivators every 100 ticks while under 16, core storage shows item tooltips, the vibration bean and Q-Bounce suspend AI of non-player mobs (the original AI flag is persisted on the entity), and trap crops deal ×3 to hostile mobs (`trap.damageMultiplier`).
- **What's new in 0.2.0**: the outpost core became a 54-slot container; the new **cultivator** binds to the nearest own core (max 16 per core) and delivers every 600 ticks (2 items, plus 2 seeds when the crop has its own seed item); **farmer trades** at levels 1/3/5 are the only source of EPIC crops; in-world **link lines** connect a core to its cultivators. Crops are **no longer plantable**, the 0.1.1 farmland binding is **disabled** (both classes remain in the tree, unused), and the converter block is decorative.
- **Build** (never call `gradle` directly): `cmd /c ""D:\project_farm\.tools\gw.cmd" build`.
- **Static check**: `python -X utf8 tools/verify_project.py` — must print `97 / 97`, `176 个配置键`, `共 338 键` **and** `FAIL 0` with exit code 0. `FAIL 0` alone is not enough (the tool once verified only 93 of 97 objects).
- **Red line**: no Mixin. Custom packets are uncapped (2 in this version, both S2C read-only, protocol string `2`).
- **Known limitations**: see the tables above (the Chinese section is authoritative). Link-line visuals and the HUD still need a human eye on a real client.
- **Docs**: Chinese originals in `docs/zh/` are authoritative; `docs/en/` holds translations. v0.2.0 spec: `docs/zh/34`–`39`; v0.2.1 spec: `docs/zh/40`–`45`. The **v0.2.2 spec — documents only, not implemented, Chinese only — is `docs/zh/46`–`51`**.

Asset provenance and the non-commercial terms are above — **keep that section verbatim when redistributing**.
