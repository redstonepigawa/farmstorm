# 农场风云 / Farmstorm

> 把原版农作物改造成 PvP 道具：**种在地里、扔出去、架在墙上、或者吃掉。**

Minecraft **1.20.1** · **Forge 47.x / NeoForge 47.x 通用** · Java **17** · modid `farmstorm` · **v0.2.5（1.20.1 移植版）**

[仓库（1.20.1 分支）](https://github.com/redstonepigawa/farmstorm/tree/1.20.1)

---

## 这是什么

一个把原版作物改造成战术道具的模组。

- **22 种改良作物**：投掷型 12 / 部署型 7 / 食用型 3，按**品质三档**分：普通 **11** / 稀有 **7** / 史诗 **4**。
- 两种**正交**分类：`CropType`（单值，决定怎么用）与 `CropCategory`（多值，决定战术功能——陷阱 / 炮台 / 破障 / buff / 建造 / 伤害）。行为由标签驱动，不写死在调用点。
- **改良作物不能种植**：作物方块与物品仍全部注册，但右键耕地不响应、不消耗；全模组只剩 `plain_chives_seeds` 一个能播种的种子。
- 改造入口是**农作物转换器**（`farmstorm:crop_converter`）：右键放入**一个**原版作物，**30 秒**后产出 **1 个**对应改良作物并自动送进绑定的据点核心；转换配方 21 条。
- **据点核心**（`farmstorm:outpost_core`）：覆盖 3 × 3 区块，自带储物空间，可绑定周围的培养器与转换器。
- 队伍归属走**原版记分板**（`TeamResolver` 抽象），不引入自定义阵营系统。

工程约定：服务端权威、**无 Mixin**、原生资源优先（不重新分发原版素材）。

---

## 给玩家

### 版本要求

**Minecraft 1.20.1 + Forge 47.x 或 NeoForge 47.x + Java 17**。

**同一个 jar 两个加载器通用**，不需要装两份：产物里没有 `net.neoforged.*` 引用，mod 类用两边都认的无参构造器，步高属性用两边都存在的字段名。实测环境：Forge 47.4.28 与 NeoForge 47.1.106。

网络协议串是 **`1`**（1.21.1 版是 `3`），所以客户端与服务端必须用同一版，混版会连不上（会被明确拒绝，不会崩溃）。

### 安装

1. 用启动器建一个 1.20.1 + Forge 47.x（或 NeoForge 47.x）的实例。
2. 打开实例的 `mods` 文件夹（没有就新建），把 **`farmstorm-0.2.5+mc1.20.1.jar`** 丢进去。
3. 启动。创造模式物品栏最后会多一个「农场风云」标签页。

**请按 MC 版本各放一份**：本模组在 1.20.1 与 1.21.1 上共用同一个 modid `farmstorm`，产物名只差 `+mc1.20.1` / `+mc1.21.1`（版本号当前是 1.20.1 = **0.2.5**、1.21.1 = **0.2.4**，两边独立演进）。同一实例里放两份只会有一份生效。

源码直接看仓库的 `1.20.1` 分支即可（`src/main/java`），本版暂不产出 `-sources.jar`。

### 这个版本会生成哪些配置文件

| 文件 | 内容 | 说明 |
|---|---|---|
| `config/farmstorm-server.toml` | **175 键** | 服务端行为；首次起服自动生成 |
| `config/farmstorm-client.toml` | **3 键**（`hud.enabled` / `hud.x` / `hud.y`） | 客户端浮窗开关与位置；**首次进游戏**才生成 |

### 存档

只支持**新建世界**。1.21.1 的世界用本版打不开，这是预期行为，不提供存档迁移。

---

## 功能

### 22 种作物，三种用法

| 类型 | 用法 |
|---|---|
| **投掷型** | 手持右键投出，命中或落地后结算（爆炸、燃烧、烟雾、随机效果等） |
| **部署型** | 放置成方块，成为常驻的炮台或陷阱 |
| **食用型** | 吃下获得增益 |

### 可部署的炮台与陷阱

- **南瓜炮台**（Pumpkin Turret）—— 半径内索敌开火，并可拦截飞行中的投掷物
- **觉醒火把花 / 觉醒猪笼草**（Awakened Torchflower / Pitcher Plant）—— 范围光环
- **仙人掌捕兽夹**（Fang Trap Cactus）—— 触发式尖牙陷阱
- **爆破胡萝卜**（Blast Carrot）—— 落地爆炸
- **桑吉尔蔗**（Zangief Cane）—— 打桩机：把目标抬起、倒转视角、再砸下去
- **点防御菌**（Point Defense Fungus）—— 拦截来袭投掷物
- **振动豆**（Vibration Bean）—— 感应触发

### 三台机器

| 方块 | 作用 |
|---|---|
| **作物转换器**（Crop Converter） | 放入一个原版作物，30 秒后产出对应的改良作物，21 条单向转换配方，无额外成本 |
| **培养器**（Cultivator） | 放入作物后持续产出 |
| **据点核心**（Outpost Core） | 3 × 3 区块范围、储物空间、绑定周边机器、归属与队伍判定 |

### 战斗与状态

- **狂野猎人**（Wild Hunter）—— 增益期间为视野内的敌方生物描出红色轮廓，并强化多项属性；HUD 显示剩余时间
- **Q 弹**（Q-Bounce）—— 骑乘与位移强化
- 3 种自定义伤害类型、4 种自定义状态效果
- 16 条模组进度

### 其他

- 模组物品 28 种、方块 28 种，统一挂在单独的创造模式标签页下
- 自定义网络包 2 个（`farmstorm:core_list` / `farmstorm:cultivator_link`），均为 S2C 只读，客户端不回包

---

## 素材来源与非商业声明

本项目**不是全部原创**：下表里**只有三段音频**取自第三方（**版权归各自权利人**，本项目不对其主张任何权利）；**其余各行均为本项目自制**，适用 [MIT 许可证](./LICENSE)。

| 产物 | 处理 |
|---|---|
| `sounds/wild_hunter_loop.ogg` | 转 mono / 64 kbps Ogg Vorbis（保留 48 kHz），47.14 s（源音频取 3m04s 起的剪辑） |
| `sounds/zangief_piledriver.ogg` | 转 mono / 64 kbps Ogg Vorbis（保留 48 kHz），4 秒段 |
| `sounds/friend_chives.ogg` | 转 mono / 64 kbps Ogg Vorbis，取前 10 秒 |
| `textures/mob_effect/wild_hunter.png` | 本项目自制；居中裁切缩放为 18×18 |
| `textures/mob_effect/q_bounce.png` | 本项目自制；居中裁切缩放为 18×18 |
| `textures/item/friend_chives.png`、`plain_chives_seeds.png` | 本项目自制；缩放为 16×16 |
| `textures/block/friend_chives_stage0..3.png` | 本项目自绘占位（洋红 `#D81E8A`） |
| `textures/block/outpost_core_{top,side,bottom}.png` | 本项目自制；原样拷入（32×32） |
| `textures/item/wild_apple.png` | 本项目自制；原图直用，不缩放、不取色（64×64） |
| `textures/block/cultivator_a_{top,side,bottom}.png` + 两张静态首帧 | 本项目自制；帧条原样拷入，静态首帧取 `editable_frames/{side,top}_00.png` |
| `textures/entity/villager/profession/botanist.png` + 同名 `.mcmeta` | 本项目自制（v0.2.5 新增）；64×64 原版村民 UV 职业叠加层，UV 派生自原版 farmer 帽 + leatherworker 围裙；`.mcmeta` 为草帽渲染配置 `{"villager":{"hat":"full"}}` |

### 使用条件（务必照此分发）

1. **非商业**：不得将上述**三段音频**或其转换产物用于任何商业用途（表内其余自制贴图不受此限，按 MIT 分发）。
2. **署名**：转载、二次分发本模组时，必须连同本表一并保留。
3. **可移除**：若**上述三段音频**的权利人提出异议，维护者会移除对应音频并发布替代版本（替换为 CC0 / 自录音源）。

### 关于原版贴图派生图

`textures/item/*.png` 与 `textures/block/*.png` 里大部分是**从原版客户端 jar 读出来再变换**的派生图：受 **Minecraft EULA / Brand and Asset Guidelines** 约束，本项目不对其主张任何权利；仓库里只有变换后的小尺寸占位图，不含原版 jar 的原始文件。

### 已知风险（维护者注）

本仓库现在是**公开**仓库。三段音频属非商业自用，但**再分发**（尤其被第三方镜像/整合包收录）仍存在被投诉的可能：若权利人提出异议，维护者会按上面的「可移除」条款替换为 CC0 / 自录音源。想彻底规避风险的话，也欢迎优先换成 CC0 音源。

> **发布 Release 时必须把这一节复制进 Release 说明**——jar 一旦分发就无法补正说明。

---

## 许可 / License

本项目采用 **MIT 许可证** —— 全文见根目录 [`LICENSE`](LICENSE)（`Copyright (c) 2026 redstonepigawa`）。

一句话：可以自由使用、复制、修改、合并、发布、再许可、销售本软件的副本，**唯一条件是保留版权声明与本许可声明**；软件按「原样」提供，不附带任何明示或默示担保。

**范围**：MIT 覆盖本项目的**代码与自有美术 / 文本**；「素材来源与非商业声明」一节里的**三段第三方音频不适用 MIT**，版权归各自权利人，按该节的使用条件分发；从原版客户端 jar 派生出的贴图受 **Minecraft EULA / Brand and Asset Guidelines** 约束。

---

## English

A Minecraft 1.20.1 NeoForge mod that turns vanilla crops into PvP gadgets.

**v0.2.5 port for Minecraft 1.20.1** (Java 17 · modid `farmstorm`). **License: MIT** (see `LICENSE`) — code and original assets only; the three third-party audio clips listed in the asset-provenance section keep their own terms.

- **Install**: build a 1.20.1 instance on **either Forge 47.x or NeoForge 47.x** (one jar works on both), drop `farmstorm-0.2.5+mc1.20.1.jar` into `mods/`. The 1.20.1 and 1.21.1 builds share the same modid `farmstorm` (current versions: 1.20.1 = **0.2.5**, 1.21.1 = **0.2.4**, evolving independently), so **keep exactly one per instance** — pick the one matching your MC version.
- **Compatibility**: network protocol string is `1` (the 1.21.1 build uses `3`); mismatched client/server are rejected cleanly. **New worlds only** — no save migration from 1.21.1.
- **Content**: 22 crops (12 thrown / 7 deployed / 3 edible; tiers 11 / 7 / 4), 3 machines (crop converter / cultivator / outpost core), 21 converting recipes, 16 advancements, 3 damage types, 4 mob effects.
