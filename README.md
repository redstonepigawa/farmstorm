# 农场风云 / Farmstorm

> 把原版农作物改造成 PvP 道具：**种在地里、扔出去、架在墙上、或者吃掉。**

Minecraft **1.21.1** · NeoForge **21.1.213** · Java **21** · modid `farmstorm` · **v0.2.4 已发布**：四条需求（绑定统一 / 交易经验分级 / 浮窗底色透明度 / 放置音效）与 `farmland` 死代码清理全部落地（当前可玩版本是 **v0.2.4**；本轮不发布 Release，Releases 页仍是 **v0.2.2**）

[下载](https://github.com/redstonepigawa/farmstorm/releases) · [文档索引](#文档) · [构建方法](#构建与调试)

---

## 这是什么

一个把原版作物改造成战术道具的模组。

- **22 种改良作物档案**：投掷型 12 / 部署型 7 / 食用型 3，按**品质三档**分：普通 **11** / 稀有 **7** / 史诗 **4**。
- 两种**正交**分类：`CropType`（单值，决定怎么用）与 `CropCategory`（多值，决定战术功能——陷阱 / 炮台 / 破障 / buff / 建造 / 伤害）。行为由标签驱动，不写死在调用点。
- **v0.2.0 起改良作物不能种植**：15 个作物方块与 22 个作物物品仍全部注册，但右键耕地不响应、不消耗；7 个部署型物品照常摆出。7 个种子物品在 **v0.2.3 已全部删除**，全模组只剩 `plain_chives_seeds` 一个能播种的种子。
- 改造入口是**农作物转换器**（`farmstorm:crop_converter`）：v0.2.3 把原来只作装饰的 `farmstorm:converter` 重做成了它——右键放入**一个**原版作物，**30 秒**后产出 **1 个**对应改良作物并自动送进绑定的据点核心；转换配方 21 条。
- **史诗档**：农民在第 1 / 3 / 5 级各多一项本模组交易，只收绿宝石、每项只能换一次；v0.2.3 起农作物转换器也能产出史诗作物（成本变化见下文「已知影响」一段）。
- 队伍归属走**原版记分板**（`TeamResolver` 抽象），不引入自定义阵营系统。

工程约定：服务端权威、**无 Mixin**、原生资源优先（不重新分发原版素材）。

---

## 给玩家

### 版本要求

**Minecraft 1.21.1 + NeoForge 21.1.x + Java 21**。用 PCL / HMCL 建对应的版本即可（实测 NeoForge 21.1.265 也能跑）。

**v0.2.4 / v0.2.3 / v0.2.2 的环境要求完全相同**，不用换启动器、也不用换 Java；网络协议串**仍是 `3`**（v0.2.3 从 `2` 升上来，v0.2.4 未动），所以客户端与服务端必须用同一版，混版会连不上。

### 安装

1. 用启动器建一个 1.21.1 + NeoForge 21.1.x 的实例。
2. 打开实例的 `mods` 文件夹（没有就新建），把 **`farmstorm-0.2.4+mc1.21.1.jar`** 丢进去（Releases 页上的 **Latest** 就是这一份；v0.2.3 是 `farmstorm-0.2.3+mc1.21.1.jar`，v0.2.2 是 `farmstorm-0.2.2+mc1.21.1.jar`）。**任两份不能同时放**。
3. 启动。创造模式物品栏最后会多一个「农场风云」标签页。

`farmstorm-<版本>+mc1.21.1-sources.jar`（v0.2.2 / v0.2.3 / v0.2.4 各一份）是源码包，给 IDE 看实现用，**不要**放进 `mods`。

### 这个版本会生成哪些配置文件

| 文件 | 内容 | 说明 |
|---|---|---|
| `config/farmstorm-server.toml` | **175 键 / 60 组**（v0.2.3 删 3 加 2；v0.2.2 是 176 键；**v0.2.4 无变化**） | 服务端行为；首次起服自动生成 |
| `config/farmstorm-client.toml` | **3 键**（`hud.enabled` / `hud.x` / `hud.y`） | 客户端浮窗开关与位置；**首次进游戏**才生成 |

### 本版（v0.2.4，**已实现**；本轮不发布 Release）

**四条需求 + 一次死代码清理：绑定走接口与标签、交易经验按级分档、浮窗更透、右键让路把放置音让回来。**

- **R1 绑定统一**：新增 `CoreBound` 接口（只声明 `corePos()` / `setCore(BlockPos)` / `clearCore()` 三个方法），农作物转换器与培养器都实现它；核侧四处「按类成对 `instanceof`」改按接口调用，「哪些方块能被核绑」改由方块标签 `farmstorm:processing_block` 发现；方块 → 类型的映射只留一张表（`KIND_BY_BLOCK`）；拆核迁移的清理出口同时覆盖两个方块（转换器以前漏掉）。
- **R2 交易经验分级**：五个常量只在 `VillagerTradeEvents` 定义一次（`XP_LEVEL_1` ~ `XP_LEVEL_5` = 2 / 5 / 10 / 15 / 30），三个交易类引用它；植物学家与农民的 1 ~ 5 级交易按级取值，**价格、数量、成交次数与 22 项顺序一项没动**。
- **R3 核心浮窗背景**：内层底色 `0x80000000` → `0x4D000000`，不透明度从 50.2%（128 ÷ 255）降到 **30.2%**（77 ÷ 255）；边框、三种文字颜色、几何与版式、三个 client config 键都不动（面板截图取色仍要玩家在有客户端的机器上看一眼）。
- **R4 放置音效**：三处右键（转换器 / 培养器 / 据点核心）的客户端判据从「无条件已消费」改成「只在手上是本机器认的物品时才接走」，其余情况返回 `PASS` 让原版放置流程继续——服务端四道闸一行未改，也不新增任何音效。
- **死代码清理**：`farmland/FarmlandGuard` + `farmland/FarmlandLedger`（225 + 555 行）与 `farmland/` 目录整体删除，`FarmstormConfig` 的 `outpostCoreFarmlandBonus` 字段、`FarmstormGameEvents` 的耕地监听、`OutpostCoreIndex.ownerOf` 一并清零；存档里的 `farmstorm_farmland.dat` 仍不读不写不删。

**本版的数字面**：v0.2.4 的 Java 源文件是 **151**（152 − 2 + 1：删掉两个死类、新增 `CoreBound.java`），这是本版唯一变化的数字；注册面 **92**、server config **175 键 / 60 组**、client config **3 键**、语言键 **321 × 2**、`configuration` 组 **178**、网络包 **2**、进度 **16**、转换配方 **21**、交易 listing **26** 全部停在 0.2.3 的值上，一项没动。

**右键「无反应」不是坏了**：客户端只看物品品类，服务端还看范围 / 绑定 / 手持 / 空槽——所以手上拿的不是机器认的输入物、机器槽里已经有货、或者机器还没绑定时，右键看上去**无反应**。这是**已知影响**，不是缺陷，也不是回归；服务端那四道闸一行未改，本版只是让「这次右键会不会被接走」的客户端预测收窄。

**跟大师级做生意，村民经验涨得明显快**：1 ~ 5 级分别给 2 / 5 / 10 / 15 / 30 点交易经验，大师级那一单一次 **30 点**，是 0.2.3 那 2 点的 **15 倍**（30 ÷ 2 = 15）。经验只影响村民的升级速度，价格与产出都不受影响。

### 上一版：v0.2.3（**已发布**）

**农业流水线闭环了：种原版 → 培养器增殖 → 转换器升级。**

- **农作物转换器重做**（`farmstorm:crop_converter`，取代旧的 `farmstorm:converter`）：硬度与爆炸抗性照**铁砧**；右键把手上**一个**原版作物或平凡作物放进去，**30 秒**后产出 **1 个**对应改良作物并**自动送进绑定的据点核心**。没有界面、不收经验、也不需要任何稀有催化剂。工作时方块播放动画、与核心的连线变**红色**（培养器是青色）。
- **改良作物不再有种子**：7 个种子物品全部删除。改良作物从此**只能靠转换器产出**（或植物学家交易 / 创造栏），不能种。
- **培养器改装原版作物了**：以前它只收改良作物，现在反过来——**只收原版农作物与平凡作物**，一个改良作物也收不进去。每次给 **2 个投入物 + 2 个对应种子**（自身即种子的作物，比如胡萝卜，直接给 **4 个**）。同时第一次有了**合成配方**（4 铁 + 4 玻璃 + 1 红石）。
- **平凡韭菜变成能种的作物**：新增 `plain_chives_crop` 方块与 `plain_chives_seeds` 种子，贴图复用朋友的韭。它也是唯一能靠转换器变成朋友的韭的平凡作物。
- **man瓜炮弹 300 刻后自毁**（15 秒），可配。
- 进度「最扎人的一集」的描述改为「触发一次「刺球」」。

数字：注册对象 **92**、server config **175 键 / 60 组**、语言键 **321 × 2**、转换配方 **21 条**（原 26，删 6 条种子配方、加 1 条平凡韭菜）。

### 从 v0.2.2 升级必看（老 toml 不会回写默认值）

盘上的 `run/config/farmstorm-server.toml` 是**老格式**：`[converter]` 那一段（`xpCost` / `netherStarForwardCost` / `netherStarOnReverse`）与旧默认值 `maxPerPlayer = 2` / `cultivatorCapacity = 16` 都还在文件里。NeoForge 只补键、删键，**不改**已存在键的值 ⇒ 升级后你会看到「我明明换了新版，怎么还是 2 / 16」。唯一干净的做法是把这份 `run/config/farmstorm-server.toml`（发布件里就是实例目录下的 `config/farmstorm-server.toml`）**删**掉，下次起服让它按 v0.2.3 的 175 键**重生成**——代价是你自己改过的其它值会一起丢；不想删就手改两行：`maxPerPlayer = 4`、`cultivatorCapacity = 24`。

### 已知影响（史诗作物变便宜了）

`[converter]` 的三个键（`xpCost` / `netherStarForwardCost` / `netherStarOnReverse`）在 v0.2.3 全部删除，**下界之星门槛彻底没有了**；新的农作物转换器又是 `crop_converter.processTicks = 600`，也就是**30 秒一台**。两件事叠加的直接后果：火把花与瓶子草这两个史诗作物，从「要下界之星 + 扣经验、基本拿不到」变成「一座转换器 **30 秒**产 1 个」。史诗档的另一个入口照旧——植物学家 5 级那一项 40-64 绿宝石、只能换一次。

### 平衡变更（培养器产出翻倍）

培养器的准入判据整个反过来之后（只收原版农作物与平凡作物，一个改良作物也收不进去），**自身即种子**的那 11 条投入物每次给的数量**翻倍**：胡萝卜 / 毒马铃薯 / 西瓜片这一类从「2 个」变成「**4 个**」；有独立种子的那一档仍是「2 个投入物 + 2 个对应种子」，没有变化。一句话记：**给得出种子的一档不变，给不出种子的一档翻倍。**

### 上一版：v0.2.2 有什么

**植物学家（新村民职业）**
- 职业方块是**据点核心** —— 拿一个核就招一个，要第二个得再放一个核；**有主的核一样能当工作台**（作者口径：不额外加「必须无主 / 必须己方」的判据）。
- 普通交易 **22 项**：价格 **3-6** 绿宝石、每项换 **8** 次、每次给 **1** 个。
- **摊在 1~4 级 = 6 / 6 / 6 / 4**（每级都由原版抽 2 项，所以升一级看到的是新的两项）。
- 史诗交易 **1 项**：价格 **40-64**，只能换 **1** 次，在 5 级（大师级）出现；首次生成时在 4 个史诗作物里抽定 1 个，之后整个存档不变。
- 农民原有的 3 档交易**照旧并存**，两者分工不同。

**平凡韭菜（`farmstorm:plain_chives`）与新的一类物品**
- 可食，回 **2** 点饱食度；不做合成、不参与转化、不占 `CROP_ITEMS`。
- 它是「**平凡农作物**」这一类里的第一个 —— 给没有对应原版农作物的改良作物做对应物。

**核心储物界面两处修复（都是「缺一行」，不是调参）**
- **悬停终于有 tooltip**：1.21.1 的 `AbstractContainerScreen#render` 体内**不画** tooltip，每个具体界面必须自己调 `renderTooltip` —— 上一版只覆写了 `renderBg`，所以 54 格全程没有详情。
- **背包那段对齐了**：原版 `renderBg` 是**两次 blit**（下段把贴图 `v=126` 画到 `y=125`，自身错开 1 px），本模组此前一次 blit 整张 ⇒ 背景比物品低 1 px。

**方块标签 `farmstorm:processing_block` 与名额**
- 带这个标签的方块才能被核心绑定；本版成员只有农作物培养器。以后想让别的方块能被绑，**改数据包就行，不用改代码**。
- 一个核能绑定的名额 **16 → 24**（所有带该标签的方块**共享**一个上限）。
- 每个玩家可放置的核心 **2 → 4**。

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
- 两个类（`FarmlandLedger` / `FarmlandGuard`）在 v0.2.3 时**保留在源码树里、仍编译**，只是没有运行期调用者；`farmstorm_farmland.dat` **不读不写不删**。**v0.2.4 起这两个类已整体删除**（连同 `farmland/` 目录，857 行），存档文件仍不读不写不删。

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

### 已知限制（v0.2.2）

**本版特有（v0.2.2）**

| 限制 | 现象 / 口径 |
|---|---|
| **每个植物学家每级只卖 2 项** | 普通交易共 **22 项**、摊在 1~4 级（6 / 6 / 6 / 4），而原版每级只从该级列表里抽 **2 项**（`Villager.java:875`）⇒ 一个植物学家**升到满级最多也只看到 8 项**，剩下 14 项要看别的植物学家。**多找几个植物学家才能凑齐**，这不是交易表坏了（裁定 #17 / D-198） |
| **一个植物学家只卖 1 个史诗作物** | 4 个史诗作物里，每个植物学家在**首次生成报价时**随机抽 **1 个**，之后整个存档不变（补货不换项）。凑齐 4 个同样要**多找几个植物学家**（裁定 #18 / D-199） |

**v0.2.1 遗留（本版未动）**

| 限制 | 现象 / 口径 |
|---|---|
| ~~**悬停 tooltip 的观感未经人眼验收**~~ **v0.2.2 已修** | 上一版这里是缺了一行 `renderTooltip` 调用（不是配置问题）。本版补上 `render` 覆写，**观感仍需玩家确认**（验收环境没有渲染面，只有源码与字节码证据） |
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
├── registry/     12 类注册面：items 28 / blocks 28 / block_entities 8 / entity 13 / mob_effect 4 /
│                 sound 3 / recipe 2 / creative_tab 1 / damage_type 3 / data_component 1 / menu 1 = 92
│                 （另 3 类口径外：ATTACHMENT_TYPES 1 / PoiType 1 / VillagerProfession 1，都不进这个 92）
├── blockentity/  部署方块、农作物转换器、据点核心（54 格容器）、培养器
├── block/        方块类与它们的交互 / 掉落 / 放置绑定
├── behavior/     作物行为（投掷 / 部署 / 陷阱 / 炮台 / 食用 / 范围效果）
├── entity/       投掷物与实体
├── network/      2 个 S2C 只读包：core_list（浮窗全量）+ cultivator_link（连线范围增量），协议串 3
├── client/       客户端：渲染、HUD、键位、拖拽、屏幕、连线线框
├── menu/         核心 54 格菜单
├── event/        事件挂载点（含村民交易）
├── config/       FarmstormConfig（server 175 键）+ FarmstormClientConfig（client 3 键）
├── datagen/      语言键 / 模型 / 战利品 / 配方生成
├── （farmland/ 已在 v0.2.4 整体删除）
└── util/ recipe/ item/ trap/ combat/ explosion/ effect/ outpost/ team/
```

更细的调用链与已踩过的坑见 [`docs/zh/08-DEV-GUIDE.md`](docs/zh/08-DEV-GUIDE.md)。

### 静态验收工具

```bash
python -X utf8 tools/verify_project.py
```

它从**文档**现算期望值再与产物比对。v0.2.3 终态四段同时成立（下面就是本轮实跑的原文）：

```
[OK]   175 个配置键与文档逐字一致（toml 175 键）
  已核 registry 对象: 92 / 92
[OK]   lang 键集一致，共 321 键
结论：FAIL 0 项，WARN 1 项
```

> v0.2.1 起 `42-TECH §4.1.1` 也进了读取面（新解析器 `expected_config_keys_v021`）：**加键不加解析器会让工具当场报红带文件名 + 行号**，不再有「键加了但没人核」的假绿。

> ⚠️ **只写「FAIL 0」是假绿**：这个工具曾经在只核 93 个对象的情况下也退出 0。引用结论时必须同时给出 `92 / 92`。新增注册对象时的固定顺序是「先加清单 → 再改计数 → 再加 config 来源 → 最后改打印串」。

---

## 文档

中文版是**权威**，`docs/en/` 是它的翻译。规矩是**文档先行**：每个版本先出六份（决策 / 设计 / 技术 / 任务 / 主提示词 / 分阶段任务），代码按任务书实现；决策 `D-xx`、任务 `F-xx`、裁决 `A-xx`、风险 `RT-xx`，改动必须回灌到编号所在文档。

长期有效的基础文档：[`00-DECISIONS`](docs/zh/00-DECISIONS.md)、[`01-DESIGN`](docs/zh/01-DESIGN.md)、[`02-TECH-SPEC`](docs/zh/02-TECH-SPEC.md)、[`07-PUBLISH`](docs/zh/07-PUBLISH.md)、[`08-DEV-GUIDE`](docs/zh/08-DEV-GUIDE.md)、[`09-IMPL-VS-PLAN`](docs/zh/09-IMPL-VS-PLAN.md)。

### 现行文档（v0.2.4，**已实现**）

六份中文文档已写完，**并已按文档实现**（16 个任务 F-312 ~ F-327 + F-328、7 个 Phase 66 ~ 72 全部落地；验收记录 `.work/v024-verify/acceptance.md`、签字 `.work/v024-verify/signoff.md`）。本轮仍未出英译，`docs/en/` 停在 v0.2.1。

| 文件 | 内容 |
|---|---|
| [`docs/zh/58-DECISIONS-0.2.4.md`](docs/zh/58-DECISIONS-0.2.4.md) | **唯一真相源**：28 条决策 D-225~D-252 + **7 条作者裁定** |
| [`docs/zh/59-DESIGN-0.2.4.md`](docs/zh/59-DESIGN-0.2.4.md) | 设计增量：4 条需求 R1~R4、**15 条边界 B-59~B-73**、附录 A 14 行差异、4 条已知影响 |
| [`docs/zh/60-TECH-0.2.4.md`](docs/zh/60-TECH-0.2.4.md) | 技术规格：注册面 **92**、config **175 键 + 3**、语言键 **321 × 2**、Java 文件 **150 → 151**、**25 条验收**、9 条风险 RT-83~RT-91 |
| [`docs/zh/61-TASKS-0.2.4.md`](docs/zh/61-TASKS-0.2.4.md) | 任务拆分：**16 个任务 F-312~F-327**、**7 个 Phase 66~72**、六张覆盖表、A-102~A-110、**18.0 人日** |
| [`docs/zh/62-PROMPT-MAIN-0.2.4.md`](docs/zh/62-PROMPT-MAIN-0.2.4.md) | 实现方主提示词：HC-173~HC-188、N-155~N-166、**26 条陷阱**、18 行事实卡 |
| [`docs/zh/63-PROMPT-TASKS-0.2.4.md`](docs/zh/63-PROMPT-TASKS-0.2.4.md) | 分阶段任务清单：Phase 66~72 各一节，**48 条出口判据 E-66-1 ~ E-72-7** |

投喂顺序：**先投 62 号一次**，之后每个 Phase 开工时只投 63 号对应的那一节。

### 上一版（v0.2.3，**已实现**）

六份中文文档已写完，**并已按文档实现**（26 个任务 F-286 ~ F-311、12 个 Phase 54 ~ 65 全部落地）。

| 文件 | 内容 |
|---|---|
| [`docs/zh/52-DECISIONS-0.2.3.md`](docs/zh/52-DECISIONS-0.2.3.md) | **唯一真相源**：22 条决策 D-203~D-224 + **18 条作者裁定** |
| [`docs/zh/53-DESIGN-0.2.3.md`](docs/zh/53-DESIGN-0.2.3.md) | 设计增量：5 条需求 R1~R5、**31 条边界 B-28~B-58**、附录 A 22 行差异、3 节已知影响 |
| [`docs/zh/54-TECH-0.2.3.md`](docs/zh/54-TECH-0.2.3.md) | 技术规格：注册面 **92**、config **175 键**、语言键 **321 × 2**、**32 条验收**、12 条风险 RT-71~RT-82 |
| [`docs/zh/55-TASKS-0.2.3.md`](docs/zh/55-TASKS-0.2.3.md) | 任务拆分：**26 个任务 F-286~F-311**、**12 个 Phase 54~65**、六张覆盖表、A-92~A-101、44 人日 |
| [`docs/zh/56-PROMPT-MAIN-0.2.3.md`](docs/zh/56-PROMPT-MAIN-0.2.3.md) | 实现方主提示词：HC-156~HC-172、N-141~N-154、**44 条陷阱**、20 行事实卡 |
| [`docs/zh/57-PROMPT-TASKS-0.2.3.md`](docs/zh/57-PROMPT-TASKS-0.2.3.md) | 分阶段任务清单：Phase 54~65 各一节，**76 条出口判据** |

投喂顺序：**先投 56 号一次**，之后每个 Phase 开工时只投 57 号对应的那一节。

**两个已经踩过的坑（实现方与验收方都要知道）：**

- **改 config 默认值不会更新已存在的 toml**。仓库里的 `run/config` 已在 v0.2.3 验收时重生成成新格式（175 键、`[crop_converter]`、4 / 24），但**玩家实例目录**里的那份很可能仍是 0.2.1 时代的 `maxPerPlayer = 2` / `cultivatorCapacity = 16`。验收 config 前**先删掉 `config/farmstorm-server.toml` 重新生成**，否则会拿到假红。
- **老存档升级有损**：`farmstorm:converter` 方块会变成缺失方块、7 种种子物品失效。这是「改名 + 删种子」两条裁定的既定代价（已在「升级须知」与「已知影响」两段对玩家点名）。

### 上一版文档（v0.2.2，已实现）

六份中文文档已全部实现（`docs/en/` 停留在 v0.2.1；那一轮没有出英译）。

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
  ⚠️ **已知限制**：原版每级只抽 2 项（`Villager.java:875`），22 项摊在 1~4 级，所以**一个植物学家升到满级最多只看到 8 项**——要多找几个才能凑齐，不是交易表坏了。
- **平凡农作物**：`plain_chives`（平凡韭菜），可食、不做合成。只补 **1 个**——22 个作物里只有「朋友的韭」没有对应的原版农作物。
- **方块标签** `farmstorm:processing_block`：可被核心绑定的处理方块，本版只有培养器带它；绑定名额 **16 → 24**（所有带该标签的方块**共享**）。
- **两个 bug**：核心储物界面**没有 tooltip**（1.21.1 的容器界面不自带，必须每个界面自己调 `renderTooltip`）；背包物品**比格子高 1 px**（原版用两次 blit、本模组用一次）。外加放置上限 **2 → 4**。

已实现（v0.2.2 发布件）：注册对象 **98**（+1）、**口径外累计 3 类**（`ATTACHMENT_TYPES` / `VillagerProfession` / `PoiType`，都不计入 98）、server config **176 键 / 60 组不变**（只改两个默认值）、语言键 **340 × 2**、网络包仍 **2 个**。

> ⚠️ **改 config 默认值不会更新已存在的 toml**：老存档升级后 `maxPerPlayer` 仍是 2、`cultivatorCapacity` 仍是 16，要删掉 `config/farmstorm-server.toml` 重生成才拿到 4 / 24。

### 上一版文档（v0.2.1，已归档）

口径以当时为准；与 v0.2.2 冲突时以 **46 号**为准。

| 文件 | 内容 |
|---|---|
| [`docs/zh/40-DECISIONS-0.2.1.md`](docs/zh/40-DECISIONS-0.2.1.md) | **唯一真相源**：14 条决策 D-173~D-186 + **17 条作者裁定** |
| [`docs/zh/41-DESIGN-0.2.1.md`](docs/zh/41-DESIGN-0.2.1.md) | 设计增量：5 条需求 R1~R5、42 行数值表、36 条边界 B-01~B-36 |
| [`docs/zh/42-TECH-0.2.1.md`](docs/zh/42-TECH-0.2.1.md) | 技术增量：注册面 **97（+0）**、动作面 31 行、**31 条验收**、10 条待验证 API、RT-51~RT-60b |
| [`docs/zh/43-TASKS-0.2.1.md`](docs/zh/43-TASKS-0.2.1.md) | 任务拆分：**20 个任务 F-247~F-266**、6 个 Phase 40~45、六张覆盖表、A-70~A-81 |
| [`docs/zh/44-PROMPT-MAIN-0.2.1.md`](docs/zh/44-PROMPT-MAIN-0.2.1.md) | 实现方主提示词：HC-121~HC-137、N-115~N-126、**41 条陷阱** |
| [`docs/zh/45-PROMPT-TASKS-0.2.1.md`](docs/zh/45-PROMPT-TASKS-0.2.1.md) | 分阶段任务清单：Phase 40~45 各一节，45 条出口判据 |

### 更早版本文档（v0.2.0，已归档）

口径以当时为准；与 v0.2.1 冲突时以 **40 号**为准。

| 文件 | 内容 |
|---|---|
| [`docs/zh/34-DECISIONS-0.2.0.md`](docs/zh/34-DECISIONS-0.2.0.md) | **唯一真相源**：25 条决策 D-148~D-172 + 19 条作者裁定 |
| [`docs/zh/35-DESIGN-0.2.0.md`](docs/zh/35-DESIGN-0.2.0.md) | 设计增量：11 条需求 R1~R11、**40 条边界 B-01~B-40** |
| [`docs/zh/36-TECH-0.2.0.md`](docs/zh/36-TECH-0.2.0.md) | 技术增量：注册面 97、动作面 46 行、config 全表、**36 条验收**、13 条待验证 API、RT-42~RT-50 |
| [`docs/zh/37-TASKS-0.2.0.md`](docs/zh/37-TASKS-0.2.0.md) | 任务拆分：**34 个任务 F-213~F-246**、8 个 Phase 32~39、六张覆盖表、A-62~A-69 |
| [`docs/zh/38-PROMPT-MAIN-0.2.0.md`](docs/zh/38-PROMPT-MAIN-0.2.0.md) | 实现方主提示词：HC-97~HC-120、N-93~N-114、31 条陷阱 |
| [`docs/zh/39-PROMPT-TASKS-0.2.0.md`](docs/zh/39-PROMPT-TASKS-0.2.0.md) | 分阶段任务清单：Phase 32~39 各一节 |

### 历史文档（已冻结）

`03`~`06`（v0.0.1 期）与 `10`~`33`（v0.0.2 / v0.0.3 / v0.1.0 / v0.1.1 各六份）**不再修改**，它们记的是当时的口径；与现行版本冲突时以 **34 号**为准，涉及 v0.2.1 的内容以 **40 号**为准，涉及 v0.2.2 的内容以 **46 号**为准。

> ⚠️ 关于「0 个自定义网络包」的说法**已全部作废**；`22`~`33` 里关于耕地绑定制的说法也随 v0.2.0 停用。

### 实施台账

[`.work/PLAN.md`](.work/PLAN.md) 与 [`.work/V022-DISPATCH.md`](.work/V022-DISPATCH.md)；各波次的任务书 `.work/TASK-V022-W0*.md`；v0.2.2 的 **57 条**验收记录（30 技术 + 27 边界）与签字在 [`.work/v022-verify/`](.work/v022-verify)。上一版的 67 条在 [`.work/v021-verify/`](.work/v021-verify)。

v0.2.3 这一版的手边件是 [`.work/v023-BRIEF.md`](.work/v023-BRIEF.md) 与 [`.work/v023-verify/`](.work/v023-verify)：**32 条**技术验收（`acceptance.md`）+ **31 条**边界（`boundaries.md`）+ 签字（`signoff.md`，八项读数 / 19 项必改 / 不改清单 / 已知影响 / 口径外 3 类）。

v0.2.4 的手边件是 [`.work/v024-BRIEF.md`](.work/v024-BRIEF.md) 与 [`.work/v024-verify/`](.work/v024-verify)：**25 条**技术验收（`acceptance.md`，通过 22 / 不通过 1 / 未验证 2）+ 签字（`signoff.md`：四条影响面 / D-252 三项 / 十项读数 / 九条风险 RT-83~RT-91 / 与文档不一致 10 条）。

---

## 版本历史

| 版本 | 日期 | 一句话 |
|---|---|---|
| v0.2.4 | 2026-11-07（**已发布**） | 四条需求 + 死代码清理：绑定统一（`CoreBound` 三方法接口 + `processing_block` 标签发现 + `KIND_BY_BLOCK` 单一映射 + 清理出口覆盖两个方块）、交易经验按级分 2 / 5 / 10 / 15 / 30（大师级单次 30 点 = 15 倍）、浮窗底色 alpha 128 → 77（30.2%）、三处右键让路恢复原版放置音；删 `farmland` 两个死类（881 行），Java 文件 152 → 151，其余十项读数一项未变 |
| v0.2.3 | 2026-11-07（**已发布**） | 五条需求全部落地：农作物转换器重做（铁砧硬度、右键放入、30 秒、产物进核心、红色连线、工作动画）、改良作物去种子（7 个种子物品全删）、培养器只收原版/平凡作物且「自身即种子」的一档给 4 个、平凡韭菜可种植、man瓜炮弹 300 刻寿命 + 进度文案；注册面 98 → 92 |
| v0.2.2 | 2026-11-05 | 植物学家职业（22 项普通 + 1 项史诗，职业方块就是据点核心）、平凡韭菜、`processing_block` 标签与 24 名额、核心界面的 tooltip 与 1 px 错位两个 bug、放置上限 4 |
| v0.2.1 | 2026-11-05 | 核心每 100 刻自动补绑培养器、核心储物 90 格悬停显示详情、震动豆与 Q弹 让生物停手（AI 原值落盘）、陷阱对敌对 ×3（`trap.damageMultiplier`） |
| v0.2.0 | 2026-11-04 | 核心变 54 格容器、培养器（16/核 · 600 刻 · 按档准入）、农民交易给史诗档、连线线框；撤销耕地绑定、作物不可种植、转换器停功能 |
| v0.1.1 | 2026-11-03 | 浮窗（自己 + 队友全部核心，含跨维度）、耕地-核心绑定制、狂野苹果 64×64、man瓜 数值与贴脸收敛、朋友的韭持续场 |
| v0.1.0 | 2026-11-02 | 作物生长重做（20~45 秒）、陷阱四项、据点核心与耕地额度、蓄力条上移 |
| v0.0.4 | 2026-10-31 | Q弹 音效截断、效果对观察者同步、只锁移动、强制第三人称 |
| v0.0.3 | 2026-10-30 | Q弹、混乱、朋友的韭、近防菌、震动豆、刺球、桑吉尔蔗 |
| v0.0.2 | 2026-10-29 | 7 项新内容 + 三个客户端子系统（世界灰度、红色剪影、键鼠拦截） |
| v0.0.1 | 2026-10-28 | 15 种作物、转换器、64 个注册对象 |

## 数字快照（v0.2.4 源码树；发布件仍停在 v0.2.2）

| 项 | 值 |
|---|---|
| 注册对象 | **92**（v0.2.2 的 98 **−6 净**：items −7 +1、blocks +1、menu −1）；另有 **3 类口径外**（`ATTACHMENT_TYPES` 1 / `PoiType` 1 / `VillagerProfession` 1），**都不计入 92** |
| config 键 | server **175 键 / 60 组**（176 −3 +2：删 `[converter]` 三键、加 `crop_converter.processTicks` 与 `pumpkin_turret.bulletLifetimeTicks`）；client **3 键** |
| 语言键 | **321 × 2**（340 −23 +4；中英键集逐字相等，`zh-only` / `en-only` 各 0） |
| 标签 TagKey | **7** 个（4 方块 + 3 物品）；`#farmstorm:converter_fuel` **仍在**，成员 37 项 |
| 自定义网络包 | **2**（`core_list` + `cultivator_link`，都 S2C 只读，协议串 **3**） |
| 改良作物档案 | 22（普通 11 / 稀有 7 / 史诗 4） |
| 进度 / 转换配方 | 进度 **16** 条（18 −2：删 `seed_cycle` / `nether_star_toll`）；`farmstorm:converting` 配方 **21 条**（26 −6 +1） |
| 方块贴图 / item 贴图 | `textures/block` **84 png + 5 mcmeta**（mcmeta **2 → 5**）；`textures/item` **22 png**（29 −7）；`textures/gui` **0** 文件 |
| Java 源码文件 | **151**（152 − 2 + 1：删 `farmland/FarmlandGuard` 与 `FarmlandLedger` 两个死类、新增 `blockentity/CoreBound.java`；本版唯一变化的数字） |
| 验收（v0.2.4） | **25 条**（60 号 §8）：**通过 22** + **不通过 1**（#22 判据假设未提交态，等价读数 **19 / 19** 通过，记为「字面不通过 / 等价判据通过」）+ **未验证 2**（#20 面板取色、#25 双客户端音频轨：没有图形客户端、也没有第二台机器）。逐条记录在 `.work/v024-verify/acceptance.md`，签字在 `.work/v024-verify/signoff.md`；16 个任务（F-312~F-327）+ F-328 全部落地。上一版是 32 条技术 + 31 条边界 = 63 条 |
| 发布件（v0.2.4，Releases 页的 Latest） | `farmstorm-0.2.4+mc1.21.1.jar` **1,125,593 B** / SHA256 `0D6B68EFFF745D84F78A51B21129C72A7D3EB0A536CA759AB09E416AEA3A021A`（上一版 v0.2.3 是 1,142,338 B） |
| 源码包 | `farmstorm-0.2.4+mc1.21.1-sources.jar` **1,071,152 B** / SHA256 `971B0692438AF9EDC744E76ED73B73515F5B1EFEB63283022758D0B7436923F6`（上一版 v0.2.3 是 1,082,655 B） |
| 上一版发布件（v0.2.3） | `farmstorm-0.2.2+mc1.21.1.jar` **1,141,176 B** / SHA256 `99309B4AB84B4267D004EA389F8E00390BB9D3329579A77F7151C89FB08389E3`；源码包 **1,067,719 B** / `0712EE1F538019A08B9B56F778908C76BE577257D86C8E65F0718232598AC94F` |

---

## 工程约束（红线）

**只剩一条红线：无 Mixin。** `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor` 全仓 **0**。

**自定义网络包不设数量上限**：v0.1.1 起「0 个自定义网络包」这条红线整体废除；v0.2.0 起累计 **2** 个（`farmstorm:core_list` 与 `farmstorm:cultivator_link`），协议串 **3**（v0.2.3 由 `2` 升上来，`playToClient(` 恰 **2** 处），**都是 S2C 只读、客户端都不回包**（`playToServer|sendToServer` 0 命中）。

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

**v0.2.4 is implemented but not released** — no GitHub Release this round; **the current release stays v0.2.2** (Minecraft 1.21.1 · NeoForge 21.1.213 · Java 21 · modid `farmstorm`).

**v0.2.2** ships a Botanist villager profession whose job site is the Outpost Core itself (22 normal trades at level 1 + 1 epic trade at level 5), a plain crop (`plain_chives`), a `farmstorm:processing_block` block tag with a shared 24-binding cap, and two Outpost Core UI fixes (missing tooltips, 1 px inventory offset) plus a placement cap of 4. Registry 97 → **98** (plus **3** registered types *outside* that count), config stays **176** keys (two defaults change), language keys 338 → **340**. The v0.2.2 spec is **Chinese-only** (no English translation for this round).

- **Download**: [Releases](https://github.com/redstonepigawa/farmstorm/releases). Drop `farmstorm-0.2.2+mc1.21.1.jar` into `mods/`; a local v0.2.4 build is `farmstorm-0.2.4+mc1.21.1.jar` (v0.2.3: `farmstorm-0.2.3+mc1.21.1.jar`; never ship two). The `-sources.jar` is for IDEs.
- **What's new in 0.2.4**: binding is unified behind the `CoreBound` interface and discovered via the `farmstorm:processing_block` block tag (one `KIND_BY_BLOCK` map, and the removal path now cleans up both blocks); villager trade XP is per level (2 / 5 / 10 / 15 / 30, so a master-level trade gives **30** XP = **15×** the old 2); the HUD panel background goes from alpha 128 to **77** (30.2%); and the three right-click handlers now return `PASS` unless the held item is an input, so vanilla's local place sound is no longer swallowed. The two dead `farmland` classes are gone (881 lines), so Java files drop 152 → **151** while the other ten baseline numbers stay put. Spec: `docs/zh/58`–`63`.
- **v0.2.3 is implemented** (build it locally: `farmstorm-0.2.3+mc1.21.1.jar`) and **Chinese-only**: `docs/zh/52`–`57`. It reworks the converter into a crop converter (anvil hardness, right-click insert, 30 s, output auto-delivered to the bound core, red link, working animation), removes all 7 improved-crop seed items, flips the cultivator to accept only vanilla/plain crops (the 11 "self-seeding" inputs now yield **4** instead of 2), makes `plain_chives` plantable, gives the man-melon bullet a 300-tick lifetime, and renames one advancement description. Registry 98 → **92**, config 176 → **175**, language keys 340 → **321**, Java files 154 → **152**, advancements 18 → **16**, converting recipes 26 → **21**. Two player-visible consequences: epic crops are cheap now (the nether-star gate is gone and one converter makes one item per 30 s), and old saves lose the `farmstorm:converter` block plus the 7 seed items.
- **What's new in 0.2.2**: the Botanist profession (job site = Outpost Core; 22 trades at level 1, one epic at level 5), the `plain_chives` plain crop, the `farmstorm:processing_block` tag with a shared 24-binding cap, the two Outpost Core UI fixes, and caps of 4 cores / 24 cultivators per core.
- **What's new in 0.2.1**: cores auto-rebind unbound cultivators every 100 ticks while under 16, core storage shows item tooltips, the vibration bean and Q-Bounce suspend AI of non-player mobs (the original AI flag is persisted on the entity), and trap crops deal ×3 to hostile mobs (`trap.damageMultiplier`).
- **What's new in 0.2.0**: the outpost core became a 54-slot container; the new **cultivator** binds to the nearest own core (max 16 per core) and delivers every 600 ticks (2 items, plus 2 seeds when the crop has its own seed item); **farmer trades** at levels 1/3/5 are the only source of EPIC crops; in-world **link lines** connect a core to its cultivators. Crops are **no longer plantable**, the 0.1.1 farmland binding is **disabled** (both classes remain in the tree, unused), and the converter block is decorative.
- **Build** (never call `gradle` directly): `cmd /c ""D:\project_farm\.tools\gw.cmd" build`.
- **Static check**: `python -X utf8 tools/verify_project.py` — must print `92 / 92`, `175 个配置键`, `共 321 键` **and** `FAIL 0` with exit code 0. `FAIL 0` alone is not enough (the tool once verified only 93 objects).
- **Red line**: no Mixin. Custom packets are uncapped (2 in this version, both S2C read-only, protocol string `3`).
- **Known limitations**: see the tables above (the Chinese section is authoritative). Link-line visuals and the HUD still need a human eye on a real client.
- **Docs**: Chinese originals in `docs/zh/` are authoritative; `docs/en/` holds translations. v0.2.0 spec: `docs/zh/34`–`39`; v0.2.1 spec: `docs/zh/40`–`45`; v0.2.2 spec: `docs/zh/46`–`51`; **v0.2.4 spec (current, implemented, Chinese only): `docs/zh/58`–`63`**; v0.2.3 spec: `docs/zh/52`–`57`.

Asset provenance and the non-commercial terms are above — **keep that section verbatim when redistributing**.
