# Farmstorm

把原版作物改造成 PvP 道具的 Minecraft 模组 —— 种下去是庄稼，扔出去是杀器。

---

## 安装

**环境要求**

| 项 | 要求 |
|---|---|
| Minecraft | **1.20.1** |
| 加载器 | NeoForge **47.x**（测试于 47.1.106） |
| Java | **17** |

**步骤**

1. 安装对应版本的 NeoForge 47.x。
2. 把 `farmstorm-0.2.4+mc1.20.1.jar` 放进实例的 `mods/` 目录。
3. 启动游戏。

**注意：同一 modid 的同名版本不能混放**

本模组在 1.20.1 与 1.21.1 上共用同一个模组名与版本号（`farmstorm` / `0.2.4`），只靠产物名区分：

- 1.20.1 用 `+mc1.20.1`
- 1.21.1 用 `+mc1.21.1`

**请按 MC 版本各放一份**。两份放进同一个 `mods/` 时加载器只会读到其中一份，表现为「装了两份却只有一份生效」。

**联机**

本版网络协议串是 **1**（1.21.1 版是 3）。两端串不一致时连接会被明确拒绝，不会崩溃。

**存档**

只支持**新建世界**。1.21.1 的世界用本版打不开，这是预期行为，不提供存档迁移。

---

## 功能

### 22 种作物，三种用法

22 种作物分投掷型 12 / 部署型 7 / 食用型 3，另有 2 种破障类。按品质分为**普通 11 / 稀有 7 / 史诗 4**。

| 类型 | 用法 |
|---|---|
| **投掷型** | 手持右键投出，命中或落地后结算（爆炸、燃烧、烟雾、随机效果等） |
| **部署型** | 放置成方块，成为常驻的炮台或陷阱 |
| **食用型** | 吃下获得增益 |

### 可部署的炮台与陷阱

放置后会自行索敌或触发：

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
| **作物转换器**（Crop Converter） | 把原版作物按配方转换成对应的 Farmstorm 道具，共 21 条单向转换配方，无额外成本 |
| **培养器**（Cultivator） | 放入作物后持续产出 |
| **据点核心**（Outpost Core） | 占地范围（3 × 3 区块），带储物空间，可绑定周围的培养器与转换器，并有归属 / 队伍判定 |

### 战斗与状态

- **狂野猎人**（Wild Hunter）—— 增益期间为视野内的敌方生物描出红色轮廓，并强化多项属性；HUD 显示剩余时间
- **Q 弹**（Q-Bounce）—— 骑乘与位移强化
- 3 种自定义伤害类型、4 种自定义状态效果
- 16 条模组进度

### 其他

- 模组物品共 28 种、方块 28 种，统一挂在单独的创造模式标签页下
- 配置项：服务端 175 项、客户端 3 项（HUD 开关与位置）

---

## 第三方素材与许可

本项目**不是全部原创**。下列素材取自第三方，**版权归各自权利人**，本项目不对其主张任何权利。路径均相对 `assets/farmstorm/`。

| 产物 | 来源 | 处理 |
|---|---|---|
| `sounds/wild_hunter_loop.ogg` | `Animals_from_3m04s.mp3`（取 3m04s 起的剪辑，47.14 s） | 转 mono / 64 kbps Ogg Vorbis（保留 48 kHz） |
| `sounds/zangief_piledriver.ogg` | SF6 视频片段 `BV1qu411W7ha`（846–850 段） | 同上 |
| `sounds/friend_chives.ogg` | `pengyoudejiu.mp3`（7.45 MB） | 转 mono / 64 kbps Ogg Vorbis，取前 10 秒 |
| `textures/mob_effect/wild_hunter.png` | 作者提供的桌面 JPG | 居中裁切缩放为 18×18 |
| `textures/mob_effect/q_bounce.png` | Steam 创意工坊 `431960/3796093604/preview.jpg` | 居中裁切缩放为 18×18 |
| `textures/item/friend_chives.png`、`textures/item/plain_chives_seeds.png` | 作者提供的 QQ 图片 | 缩放为 16×16 |
| `textures/block/friend_chives_stage0..3.png` | **本项目自绘占位**（洋红 `#D81E8A`） | 不是第三方素材 |
| `textures/block/outpost_core_{top,side,bottom}.png` | 作者提供的 `outpost_core_all_textures/32x32/a_runic/` 三张 32×32 | 原样拷入 |
| `textures/item/wild_apple.png` | 作者提供的 `wild_apple_64x64.png`（64×64 / 6,946 B） | 原图直用，不缩放、不取色 |
| `textures/block/cultivator_a_{top,side,bottom}.png` + 两张静态首帧 + 2 个 mcmeta | 作者提供的 `cultivator_a_animation_pack`（37 PNG + 2 mcmeta） | 帧条原样拷入；静态首帧取 `editable_frames/{side,top}_00.png` |

### 使用条件（务必照此分发）

1. **非商业**：不得将上述素材或其转换产物用于任何商业用途。
2. **署名**：转载、二次分发本模组时，必须连同本表一并保留。
3. **可移除**：若权利人提出异议，维护者会移除对应素材并发布替代版本。

### 关于原版贴图派生图

`textures/item/*.png` 与 `textures/block/*.png` 里大部分是**从原版客户端 jar 读出来再变换**的派生图：受 **Minecraft EULA / Brand and Asset Guidelines** 约束，本项目不对其主张任何权利；仓库里只有变换后的小尺寸图，不含原版 jar 的原始文件。

### 已知风险

本仓库目前是**私有**的，上述素材属非商业自用范围。**若将来转为公开仓库**，第三方音乐与游戏音效的再分发仍存在被投诉的可能。转公开前建议换成 CC0 / 原创音源，或保留本节的署名与非商业条款并接受随时撤下。

> **发布 Release 时必须把这一节复制进 Release 说明**——jar 一旦分发就无法补正说明。

### 第三方代码

本模组**不内嵌任何第三方库**：唯一的编译与运行依赖是 NeoForge 提供的 Minecraft API（`net.neoforged:forge:1.20.1-47.1.106`），产物中不含 jar-in-jar 依赖。

### 随包素材清单（1.20.1 版）

| 类别 | 数量 | 说明 |
|---|---|---|
| 贴图 | 115 个 PNG | `block` 89 / `item` 22 / `misc` 2 / `mob_effect` 2 |
| 音效 | 3 个 Ogg Vorbis | 见上方来源表 |
| 语言 | 2 份 JSON | `en_us` / `zh_cn`，各 321 个键 |

### 许可

模组本体在 `META-INF/mods.toml` 中标注为 **All Rights Reserved**；第三方素材另受上述非商业条款约束。

---

*Farmstorm 0.2.4 · Minecraft 1.20.1 · NeoForge 47.x*
