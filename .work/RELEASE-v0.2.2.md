# Farmstorm 农场风云 v0.2.2

> Minecraft **1.21.1** · NeoForge **21.1.x** · Java **21** · modid `farmstorm`

四条需求 + 两个 UI 修复：**植物学家（新村民职业）**、**平凡农作物「平凡韭菜」**、**`farmstorm:processing_block` 标签**、**核心储物界面的 tooltip 与 1 像素错位**，外加放置上限 **2 → 4**、绑定名额 **16 → 24**。
注册面 **97 → 98（+1）**，网络包仍 **2 个**，红线仍**无 Mixin**。

## 本版改了什么

**R1 · 植物学家（新村民职业）**
- 职业方块就是**据点核心** —— 拿一个核就招一个，要第二个得再放一个核；**有主的核一样能当工作台**（作者口径：不额外加「必须无主 / 必须己方」的判据）。
- 普通交易 **22 项**：价格 **3-6** 绿宝石、每项换 **8** 次、每次给 **1** 个。
- **摊在 1~4 级 = 6 / 6 / 6 / 4**（每级都由原版抽 2 项）—— 第一版曾把 22 项全堆在新手级，结果 2/3/4 级是空表、什么都刷不出来，本版修掉了。
- 史诗交易 **1 项**：价格 **40-64**、只能换 **1** 次、在 5 级（大师级）；首次生成时在 4 个史诗作物里抽定 1 个，之后整个存档不变。
- 农民原有的 3 档交易**照旧并存**。

**R2 · 平凡农作物：平凡韭菜（`farmstorm:plain_chives`）**
- 可食，回 **2** 点饱食度；不做合成、不参与转化。
- 贴图直接复用「朋友的韭」那张作者原画，不额外存一份。
- 「平凡农作物」这一类给没有对应原版农作物的改良作物做对应物；本版只补 1 个（22 个作物里只有「朋友的韭」缺对应物）。

**R3 · 方块标签 `farmstorm:processing_block` 与名额**
- 带这个标签的方块才能被核心绑定；本版成员只有农作物培养器。以后想让别的方块能被绑，**改数据包就行，不用改代码**。
- 一个核能绑定的名额 **16 → 24**（所有带该标签的方块**共享**一个上限）。
- 每个玩家可放置的核心 **2 → 4**。

**R4 · 核心储物界面两处修复（都是「缺一行」，不是调参）**
- **悬停终于有 tooltip**：1.21.1 的 `AbstractContainerScreen#render` 体内不画 tooltip，每个具体界面必须自己调 `renderTooltip`；上一版只覆写了 `renderBg`，所以 54 格全程没有详情。
- **背包那段对齐了**：原版 `renderBg` 是两次 blit（下段把贴图 `v=126` 画到 `y=125`，自身错开 1 px），本模组此前一次 blit 整张 ⇒ 背景比物品低 1 px。

## 从 v0.2.1 升级必看

| 项 | 会发生什么 | 怎么办 |
|---|---|---|
| 两个上限的新默认值**对老存档不生效** | 升级后 `maxPerPlayer` 仍是 **2**、`cultivatorCapacity` 仍是 **16**（NeoForge 只补键/删键，不改已存在键的值） | 删掉 `config/farmstorm-server.toml` 让它重生成，或手改那两行成 **4 / 24** |
| config 键数**不变** | 仍 **176 键 / 60 组**（本版没有新增或删除任何键） | 无需操作 |
| 语言键 338 → **340** | 新增 `item.farmstorm.plain_chives` 与 `entity.minecraft.villager.farmstorm.botanist` 各一条（中英） | 无需操作 |
| 新增两张注册表 | POI 类型 `botanist` 与村民职业 `botanist`；与实体附件类型一样**不计入** 12 类的注册面计数，注册面仍是 **98** | 无需操作 |
| 老存档已超 4 个核 | **不会被删**：上限只在「放置时」判，已有的不动 | 无需操作 |
| 物品 / 配方 / 进度 / 方块 | 全部向后兼容，注册 ID 一个没变 | 无需迁移存档 |

## 已知限制

- **每个植物学家每级只卖 2 项**：原版村民每级只从该级交易表随机抽 2 项 ⇒ 升到满级最多也只看到 **8 项**（4 级 × 2），剩下 14 项要看别的植物学家。**多找几个植物学家才能凑齐**，这不是交易表坏了。
- **一个植物学家只卖 1 个史诗作物**：4 个史诗作物里抽定 1 个，之后整个存档不变（补货只重置次数、不换物品）。凑齐 4 个同样要多找几个。
- **悬停 tooltip 与像素对齐的观感未经人眼验收**：本版验收跑在专用服务端 + 协议机器人上，没有渲染面；代码层证据是「补了 `render` 覆写调 `renderTooltip`」与「背景从一次 blit 改两次 blit」，实际显示效果需要玩家确认。
- 平凡韭菜**没有自己的贴图**：直接复用「朋友的韭」那张作者原画（`farmstorm:item/friend_chives`），所以你换了朋友的韭的图，它也跟着变。
- 培养器必须在核的 3×3 区块内才可用；史诗作物无法进入培养器（只能靠交易）；培养器没有槽位界面。
- 连线与浮窗需要装本模组的客户端；连线的视觉观感同样未经人眼验收。
- 震动豆的「不触发」反向断言只对生存模式、非己方的玩家成立：创造 / 旁观不触发，但仍会被晕到（0.2.0 起的既存口径）。
- **转换器重做不在本版**（作者已把该条从本版需求里整块删除，不是搁置）。
- 历史遗留（0.0.2 起）：灰度只作用于方块/实体/粒子（HUD 保持原色）、桑吉尔蔗相位 2 只是降速约 15.6 倍、F11/F2/F3/ESC 拦不住、1×1 石柱落地约 23 刻、狂野猎人期间设置界面亮度滑条显示为满、火把花光环离场后火焰附加最多再留 15 秒、干耕地会毁苗、据点核心边界粒子每圈 48 个包。

## 数字（v0.2.2 发布件）

| 项 | 值 |
|---|---|
| 注册对象 | **98**（items **34** / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2） |
| 口径外 | **3 类**：实体附件 `farmstorm:ai_suspend`、POI 类型 `botanist`、村民职业 `botanist` —— **都不计入 98**，静态工具既不核存在也不核计数 |
| config | server **176 键 / 60 组**（默认 `maxPerPlayer` 4 / `cultivatorCapacity` 24）；client **3 键** |
| 语言键 | **340 × 2**（中英键集逐字相等） |
| 方块标签 / Java 文件 | **7** 个 / **154** 个 |
| 网络包 / 红线 | **2**（都 S2C 只读，协议串 `2`）/ **无 Mixin** |
| 验收 | **57 条**（30 技术 + 27 边界）= 通过 **46** + 观感类【未验证】 **11**；**失败 0**。19 个任务（F-267~F-285）全部落地 |

## 文件校验

```
farmstorm-0.2.2+mc1.21.1.jar           1,141,176 B
  SHA256 99309B4AB84B4267D004EA389F8E00390BB9D3329579A77F7151C89FB08389E3

farmstorm-0.2.2+mc1.21.1-sources.jar   1,067,719 B
  SHA256 0712EE1F538019A08B9B56F778908C76BE577257D86C8E65F0718232598AC94F
```

`*-sources.jar` 是源码包，给 IDE 看实现用，**不要**放进 `mods`。

---

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

