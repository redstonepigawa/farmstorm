# F-75 证据 · Phase 12 出口回归（五步闭环 + §11 #7/#8/#20 + jar）

HEAD `71c9fa6`（**未 commit**）｜**零源码改动**（`git diff --stat` 空）
环境：`-PfsRunDir=run-f75`（runData 与 runServer 同目录）｜游戏口 25631 / RCON 25632（`f75rcon`）｜flat / seed 680068
驱动器：`.work/f75-verify.js`（五步闭环，75 条断言）、`.work/f75-wet.js`（§11 #8 湿态补充，14 条断言）
性质：回归。**没有发现需要修的缺陷，因此没有产生补丁**；唯一的"改动"是验证期的临时探针，已删净。

## 0. 三条验收结论

| # | 验收 | 结论 | 证据 |
|---|---|---|---|
| 1 | 五步闭环在单人存档内**一次跑通** | **过** | §1（每步 `/give` + `/execute if block` 原文）；`### 断言合计：PASS 75 / FAIL 0` |
| 2 | `12-TECH §11` 的 #7 / #8 / #20 三条全过 | **过** | §2 / §3 / §4（逐条读数，不是"通过"两个字） |
| 3 | `build/libs/farmstorm-0.0.2+mc1.21.1.jar` 存在且体积大于 v0.0.1 | **过** | §5：**2,180,993 B** > **336,451 B**（+1,844,542 B，6.482 倍） |

一次性结论：Phase 12 的九个任务（F-67 ~ F-75）所交付的功能在**同一条闭环里串起来跑通**，无阻断问题。

## 1. 五步闭环（同一次服务端会话、同一个世界、按序执行）

闭环顺序：**做转换器 → 换出三件新物品 → 4 合 1 → 投莫洛托夫点火 → 投压缩膨胀麦铺压缩干草块**。
每步都留 `/give` 与 `/execute if block` 各一条（原文照抄，中文是 UTF-8 直读；PowerShell 控制台的乱码只是显示编码问题）。

### 前置：清场与基线（V-16 / V-17）

```
> /forceload add 96 96 128 128          （方块坐标）
    "Marked 9 chunks in Overworld from [6, 6] to [8, 8] to be force loaded"
> /kill @e[type=minecraft:item]            → "No entity was found"
> /fill 96 96 96 128 112 128 minecraft:air → "No blocks were filled"
> /fill 96 99 96 108 99 108 minecraft:stone   → "Successfully filled 169 block(s)"
> /fill 96 99 112 104 99 120 minecraft:stone  → "Successfully filled 81 block(s)"   （莫洛托夫台 9×9）
> /fill 110 99 96 118 99 104 minecraft:stone  → "Successfully filled 81 block(s)"   （膨胀麦台 9×9）
> /execute if block 96 96 96 minecraft:air     → "Test passed"
> /execute if block 100 100 116 minecraft:air  → "Test passed"
> /execute if block 114 99 100 minecraft:stone → "Test passed"
> /execute if block 100 99 100 minecraft:stone → "Test passed"
  [PASS] 基线：世界里没有掉落物
```

### 第 1 步 · 做转换器（中心工作台 + 八格农作物 → 1 个转换器）

无客户端，工作台的 3×3 用**crafter 方块**代替（9 格容器槽 0..8 = 3×3 行优先格位，红石上升沿触发一次合成；
F-71 已确认它与工作台走同一个 `RecipeManager` 判定）。八格刻意**混搭八种不同燃料**。

```
> /f75probe setup 102.5 100.0 102.5
    "[F75PROBE] setup name=F75Probe dim=minecraft:overworld pos=102.50,100.00,102.50 gameMode=SURVIVAL health=20.00 totalExperience=0"

/give 读数（9 条，每条都是真实 vanilla give 的反馈原文）：
> /f75probe give minecraft:crafting_table 1
    "[F75PROBE] give command="/give @s minecraft:crafting_table 1" reply=Gave 1 [Crafting Table] to F75Probe | inv=minecraft:crafting_tablex1"
> /f75probe give minecraft:wheat 1     → reply=Gave 1 [Wheat] to F75Probe        | inv=minecraft:wheatx1
> /f75probe give minecraft:carrot 1    → reply=Gave 1 [Carrot] to F75Probe        | inv=minecraft:carrotx1
> /f75probe give minecraft:potato 1    → reply=Gave 1 [Potato] to F75Probe        | inv=minecraft:potatox1
> /f75probe give minecraft:beetroot 1  → reply=Gave 1 [Beetroot] to F75Probe      | inv=minecraft:beetrootx1
> /f75probe give minecraft:melon_slice 1 → reply=Gave 1 [Melon Slice] to F75Probe | inv=minecraft:melon_slicex1
> /f75probe give minecraft:cactus 1    → reply=Gave 1 [Cactus] to F75Probe        | inv=minecraft:cactusx1
> /f75probe give minecraft:bamboo 1    → reply=Gave 1 [Bamboo] to F75Probe        | inv=minecraft:bamboox1
> /f75probe give minecraft:sweet_berries 1 → reply=Gave 1 [Sweet Berries] to F75Probe | inv=minecraft:sweet_berriesx1

摆料与合成：
> /setblock 100 100 100 minecraft:crafter                → "Changed the block at 100, 100, 100"
> /item replace block 100 100 100 container.4 with minecraft:crafting_table   → "Replaced a slot ... with [Crafting Table]"
> /item replace block 100 100 100 container.{0,1,2,3,5,6,7,8} with 上述八种农作物 → 8 条 "Replaced a slot ..."
> /setblock 101 100 100 minecraft:redstone_block          → "Changed the block at 101, 100, 100"
> /setblock 101 100 100 minecraft:air                     → "Changed the block at 101, 100, 100"

产出读数：
> /data get entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:converter"}},limit=1]
    "Converter has the following entity data: { ... Item: {count: 1, id: "farmstorm:converter"}, Pos: [100.23d, 100.0d, 96.64d], PickupDelay: 0s, ... }"
> /execute if entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:converter"}}]
    "Test passed, count: 1"          ← 既有的 /give 之外，第 1 条的 /execute 读数
> /execute if entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:converter",count:1}}]
    "Test passed, count: 1"

放置（headless 无手，用 /setblock 代替"拿在手里右键"）：
> /setblock 104 100 100 farmstorm:converter  → "Changed the block at 104, 100, 100"
> /execute if block 104 100 100 farmstorm:converter
    "Test passed"                            ← 第 1 步的 /execute if block 读数
```

### 第 2 步 · 换出三件新物品（真实 ConverterMenu + 真实按钮）

```
/give 读数：
> /f75probe give minecraft:nether_star 1      → reply=Gave 1 [Nether Star] to F75Probe      | inv=minecraft:nether_starx1
> /f75probe give minecraft:crimson_fungus 1    → reply=Gave 1 [Crimson Fungus] to F75Probe  | inv=minecraft:crimson_fungusx1
> /f75probe give minecraft:sugar_cane 1        → reply=Gave 1 [Sugar Cane] to F75Probe      | inv=minecraft:sugar_canex1
> /f75probe give minecraft:golden_apple 1      → reply=Gave 1 [Golden Apple] to F75Probe    | inv=minecraft:golden_applex1
> /f75probe give minecraft:torchflower 1       → reply=Gave 1 [Torchflower] to F75Probe     | inv=minecraft:torchflowerx1
> /f75probe xp 200 → "xp requested=200 total=200 points=F75Probe has 12 experience points levels=F75Probe has 11 experience levels"

四次转换（真实 ConverterMenu#clickMenuButton(BUTTON_CONVERT)，原文逐字）：
A) > /f75probe convert 104 100 100 minecraft:crimson_fungus minecraft:nether_star 1
   "[F75PROBE] convert input=minecraft:crimson_fungus catalyst=minecraft:nether_starx1 xpCost=3 canConvertBefore=true
    preview=farmstorm:molotovx1 clickMenuButton=true marginXp=200->197
    points=F75Probe has 12 experience points -> F75Probe has 9 experience points
    inputSlotAfter=empty catalystSlotAfter=minecraft:nether_starx1 inv={...,slot14=farmstorm:molotovx1}"
B) > /f75probe convert 104 100 100 minecraft:sugar_cane none 0
   "... xpCost=3 canConvertBefore=true preview=farmstorm:zangief_canex1 clickMenuButton=true marginXp=197->194
    catalystSlotAfter=minecraft:nether_starx1 inv={...,slot15=farmstorm:zangief_canex1}"
C) > /f75probe convert 104 100 100 minecraft:golden_apple none 0
   "... preview=farmstorm:wild_applex1 clickMenuButton=true marginXp=194->191
    catalystSlotAfter=minecraft:nether_starx1 inv={...,slot16=farmstorm:wild_applex1}"
D) > /f75probe convert 104 100 100 farmstorm:wild_apple none 0     ← 反向
   "... input=farmstorm:wild_apple preview=minecraft:golden_applex1 clickMenuButton=true marginXp=191->188
    catalystSlotAfter=minecraft:nether_starx1 inv={...,slot12=minecraft:golden_applex2}"

补充（下界之星催化剂槽的正面案例，见 §9 偏差 1）：
E) > /f75probe convert 104 100 100 minecraft:torchflower none 0
   "... preview=farmstorm:awakened_torchflowerx1 clickMenuButton=true marginXp=188->185
    points=F75Probe has 0 experience points -> F75Probe has 24 experience points
    catalystSlotAfter=empty inv={...,slot17=farmstorm:awakened_torchflowerx1}"

/execute if block 读数：
> /execute if block 104 100 100 farmstorm:converter
    "Test passed"                      ← 转换器仍在原位（四次转换都在同一个方块实体上完成）
```

读法：
- 三条新配方**各扣 3 点经验**（total 200→197→194→191→188），`levels` 全程 11 不变 —— 扣的是**点数**（V-24）。
- 正向 A 带 1 个下界之星进催化剂槽，三次转换后**星星原样还在**（`catalystSlotAfter=minecraft:nether_starx1`）——
  即**这三条配方根本不需要催化剂**；E 的 torchflower 正向才是真正吃星星的那条（1 → 0）。
- 反向 D 用同一份 JSON 的 `reverse()` 视图（`input=farmstorm:wild_apple`），双向可用。

### 第 3 步 · 4 合 1（4 × puffed_wheat → compressed_puffed_wheat）

```
/give 读数：
> /f75probe give farmstorm:puffed_wheat 4
    "[F75PROBE] give command="/give @s farmstorm:puffed_wheat 4" reply=Gave 4 [Puffed Wheat] to F75Probe | inv=farmstorm:puffed_wheatx4"

合成（crafter 槽 0..3 各 1 个膨胀麦，红石上升沿）：
> /setblock 100 100 104 minecraft:crafter → "Changed the block at 100, 100, 104"
> /item replace block 100 100 104 container.0..3 with farmstorm:puffed_wheat → 4 条 "Replaced a slot ... with [Puffed Wheat]"
> /setblock 101 100 104 minecraft:redstone_block → air
> /data get entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:compressed_puffed_wheat"}},limit=1]
    "item.farmstorm.compressed_puffed_wheat has the following entity data: { ... Item: {count: 1, id: "farmstorm:compressed_puffed_wheat"}, Pos: [101.13d, 100.0d, 102.33d], ... }"
> /execute if entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:compressed_puffed_wheat"}}]
    "Test passed, count: 1"
> /execute if entity @e[type=minecraft:item,nbt={Item:{id:"farmstorm:compressed_puffed_wheat",count:1}}]
    "Test passed, count: 1"
> /execute if block 100 100 104 minecraft:crafter
    "Test passed"                      ← 第 3 步的 /execute if block 读数

反例（同一台 crafter，清空后各触发一次）：
> 放 3 个膨胀麦 → /execute if entity @e[type=minecraft:item] → "Test failed"   [PASS] 无产物
> 放 5 个膨胀麦 → /execute if entity @e[type=minecraft:item] → "Test failed"   [PASS] 无产物
```

### 第 4 步 · 投莫洛托夫点火（默认 `fireChance=85.0`）

```
/give 读数：
> /f75probe give farmstorm:molotov 1
    "[F75PROBE] give command="/give @s farmstorm:molotov 1" reply=Gave 1 [item.farmstorm.molotov] to F75Probe | inv=farmstorm:molotovx2"

投掷（自由落体，无手；用 /summon + Motion 全 0，F-68/F-70 同一手法）：
> /summon minecraft:cow 96.5 100.0 112.5 {NoAI:1b,Silent:1b}   → "Summoned new Cow"    （距落点 5.66 格：半径 4 之外）
> /data get entity @e[type=minecraft:cow,limit=1] Health       → "Cow has the following entity data: 10.0f"
> /summon farmstorm:molotov 100.5 104.0 116.5 {Motion:[0.0d,0.0d,0.0d]}
    "Summoned new entity.farmstorm.molotov"
> /execute if block 100 100 116 minecraft:fire
    "Test passed"                      ← 第 4 步的 /execute if block 读数（落点正上方那格是火）

落点扫描（y=100 层、半径 4 圆盘 49 格，逐格 /execute if block）：
    [扫描] y=100 层半径 4 圆盘 49 格：火 38 岩浆 11 空气 0 其它 0
    [PASS] S4 候选格全部被写入火/岩浆（49 格无遗漏）  (fire=38 magma=11 other=[])
服务端日志原文（logs/latest.log 摘录）：
[17:10:35] [Server thread/INFO] [farmstorm/]: 莫洛托夫 落点 (100.50, 100.05, 116.50) 威力 0
[17:10:35] [Server thread/INFO] [farmstorm/]: 莫洛托夫 候选 49 火 38 岩浆 11
```

### 第 5 步 · 投压缩膨胀麦铺压缩干草块

```
/give 读数：
> /f75probe give farmstorm:compressed_puffed_wheat 1
    "[F75PROBE] give command="/give @s farmstorm:compressed_puffed_wheat 1" reply=Gave 1 [item.farmstorm.compressed_puffed_wheat] to F75Probe | inv=farmstorm:compressed_puffed_wheatx1"

> /summon farmstorm:compressed_puffed_wheat 114.5 104.0 100.5 {Motion:[0.0d,0.0d,0.0d]}
    "Summoned new entity.farmstorm.compressed_puffed_wheat"
> /execute if block 114 99 100 farmstorm:compressed_hay_block
    "Test passed"                      ← 第 5 步的 /execute if block 读数（中心格）
> /execute if block 114 100 100 farmstorm:compressed_hay_block
    "Test passed"                      （平台上方那格也被写满）
    投后 3×3×3 立方体 27 格：compressed_hay_block 27 / ["farmstorm:compressed_hay_block"]
    [PASS] S5 写入无掉落物
```

## 2. `§11 #7` 莫洛托夫的落点语义（追加不是破坏）

| 判据（12-TECH §11 #7） | 实测读数 | 结论 |
|---|---|---|
| 球半径 4 格内只有"上表面接触空气"的方块上方新增火/岩浆 | 49 个候选格 → 火 38 + 岩浆 11 = 49，其它 0 | 过 |
| 原方块一个都没少 | y=99 平台 **81/81 格仍是 `minecraft:stone`** | 过 |
| 半径外不写入 | 半径 4 之外的 **32 格全部仍是空气**（bad=[]） | 过 |
| 无爆炸伤害 | 对照牛 `Health` 投前投后**逐字相同**（10.0f）；日志 `威力 0` | 过 |
| 无方块破坏 | 同上 81 格逐格 `/execute if block`（`NEVER_BREAK`） | 过 |
| 火 : 岩浆块 ≈ 85 : 15（投 20 颗粗核） | **候选 980 / 火 822 / 岩浆 158 = 16.12 %**（每次候选恰 49，火+岩浆=候选，0 例外） | 过 |

20 投的逐投读数（脚本逐格扫描 + 服务端日志双向核对，两者合计一致：log=822/158 scan=822/158）：

```
第 1..21 次「莫洛托夫 候选 N 火 F 岩浆 M」日志行（f75-server.log）：
  候选 49 火 38 岩浆 11 ／ 49/34/15 ／ 49/38/11 ／ 49/42/7 ／ 49/41/8 ／ 49/43/6 ／ 49/43/6 ／ 49/43/6
  49/42/7 ／ 49/42/7 ／ 49/38/11 ／ 49/45/4 ／ 49/39/10 ／ 49/43/6 ／ 49/42/7 ／ 49/44/5 ／ 49/40/9
  49/40/9 ／ 49/42/7 ／ 49/42/7 ／ 49/39/10
20 投合计：候选 980 火 822 岩浆 158 → 岩浆占比 16.12 %（区间 10 %–20 %）
```

另外两条口径（V-26 / V-28 的同类陷阱）：
- **没有用 `BLOCK_MINED` 之类的统计量**做任何判据（`FakePlayer#awardStat` 是空实现，恒 0）；本步判据全部落在
  「`/execute if block` 逐格读数 + 实体 Health + 日志文案」上。
- 本步没有任何"挖方块"用例，因此不存在"破了没掉"的表述；如果将来有人把 §7 的结论写成"方块破了但没掉"，
  那是错的 —— 这里的事实是**方块一个都没动，火/岩浆只写在候选格正上方的空气里**。

## 3. `§11 #8` 压缩膨胀麦完全沿用膨胀麦

同一坐标（落点方块 `114,99,100`）先投一颗、复位、再投另一颗，逐格 `/execute if block` 记录 3×3×3 立方体。

| 用例 | 写入格数 | 写入的方块 | 位置集合 |
|---|---|---|---|
| 膨胀麦（干态） | **27** | `minecraft:hay_block` ×27 | A |
| 压缩膨胀麦（干态） | **27** | `farmstorm:compressed_hay_block` ×27 | A（与 A **完全相同**） |
| 膨胀麦（遇水，边长 5） | **125** | `minecraft:hay_block` ×125 | B |
| 压缩膨胀麦（遇水，边长 5） | **125** | `farmstorm:compressed_hay_block` ×125 | B（与 B **完全相同**） |
| 膨胀麦（基岩挡 1 格） | **26** | hay_block ×26，基岩仍在 | C |
| 压缩膨胀麦（基岩挡 1 格） | **26** | compressed_hay_block ×26，基岩仍在 | C（与 C **完全相同**） |

原文：
```
§11 #8 压缩膨胀麦完全沿用膨胀麦（同坐标两颗）
  [PASS] §8 基线（膨胀麦一投前）：立方体已复位（石头 9 / 空气 18）  (stone=9 air=18 bad=[])
    膨胀麦写入：hay_block 27 / ["minecraft:hay_block"]
  [PASS] §8 基线（压缩膨胀麦一投前）：立方体已复位（石头 9 / 空气 18）  (stone=9 air=18 bad=[])
    膨胀麦写入格集合 = 27 格；压缩膨胀麦写入格集合 = 27 格
  [PASS] §11 #8 两次立方体边长一致（干态 3 → 27 格）  (hay=27 compressed=27)
  [PASS] §11 #8 写入位置集合完全相同  (haySetN=27 compressedSetN=27)
  [PASS] §11 #8 唯一差异是方块 ID（hay_block vs compressed_hay_block）  (hay=27 compressed=27)

§11 #8 抗性 3.6e6 的格被挡住（基岩），两颗行为一致
    基岩位读数：膨胀麦前 true 后 true；压缩膨胀麦前 true 后 true
    绕开基岩的写入格：膨胀麦 26 格 / 压缩膨胀麦 26 格
  [PASS] §11 #8 两颗都被基岩挡住（基岩位置前后都是 bedrock）
  [PASS] §11 #8 被挡住的格一致：两次都写 26 格  (hay=26 compressed=26)
  [PASS] §11 #8 被挡住的格集合相同
```

湿态（边长 5）是补充脚本 `.work/f75-wet.js` 单独跑的（在主闭环之后、同一个世界、**已删掉探针的干净构建**上）：
```
湿态基线（膨胀麦前）：落点方块是石头、正上方是空气  (top=true above=true)
湿态基线：干态 27 格里只有 1 格是水  (["minecraft:air","minecraft:stone","minecraft:water"])
    膨胀麦写入（5³ 立方体 125 格）：hay_block 125 / ["minecraft:hay_block"]
  [PASS] 膨胀麦：遇水边长扩到 5（125 格 hay_block）  (hay=125)
  [PASS] 膨胀麦：水源格被 hay_block 覆盖（写入不是跳过）  (waterLeft=false)
    压缩膨胀麦写入（5³ 立方体 125 格）：compressed_hay_block 125 / ["farmstorm:compressed_hay_block"]
  [PASS] 压缩膨胀麦：遇水边长同样是 5（125 格）  (compressed=125)
  [PASS] §11 #8（湿态）：两次写入的位置集合完全相同  (haySetN=125 compressedSetN=125)
  [PASS] §11 #8（湿态）：唯一差异是方块 ID  (hay=125 compressed=125)
### 湿态补充断言：PASS 14 / FAIL 0
```
（水源按 F-68 的做法放在干态立方体内部 `115,98,100`，六面皆石、不会流走。）

## 4. `§11 #20` 5 条新配方的形状与数量

静态 JSON（`src/generated/resources/data/farmstorm/recipe/`，全部由 F-71/F-72/F-73 的 datagen 产出）：

| 文件 | type | 形状 / 内容 | result |
|---|---|---|---|
| `converter.json` | `minecraft:crafting_shaped` | `pattern=FFF/FTF/FFF`，`key.F.tag=farmstorm:converter_fuel`，`key.T.item=minecraft:crafting_table` | `farmstorm:converter` × **1** |
| `compressed_puffed_wheat.json` | `minecraft:crafting_shapeless` | `ingredients=[farmstorm:puffed_wheat ×4]`（count=4） | `farmstorm:compressed_puffed_wheat` × **1** |
| `molotov.json` | `farmstorm:converting` | `input=minecraft:crimson_fungus`（keys: type,input,result,reverse_xp_cost） | `farmstorm:molotov` ×1，`reverse_xp_cost=3` |
| `zangief_cane.json` | `farmstorm:converting` | `input=minecraft:sugar_cane` | `farmstorm:zangief_cane` ×1，`reverse_xp_cost=3` |
| `wild_apple.json` | `farmstorm:converting` | `input=minecraft:golden_apple` | `farmstorm:wild_apple` ×1，`reverse_xp_cost=3` |

```
farmstorm:converting 条数 = 24          （既有 21 + 三条新增，BRIEF §4 的口径）
recipe 目录文件数 = 26                  （24 转换 + converter.json + compressed_puffed_wheat.json）
```
`bidirectional` 与 `xp_cost` 不落盘 = 取 codec 默认值（`true` / `3`），与既有 21 条同形（F-73 已记录），
运行期以实测为准：上表三条转换**各扣 3 点经验**、`preview` 与产物都对、wild_apple 反向命中 `reverse` 视图。

运行时读数（与 §1 的第 1/2/3 步是同一批命令）：
- 工作台得 **1 个**转换器（`Test passed, count: 1`）。
- 4 个膨胀麦得 **1 个**压缩膨胀麦（`Test passed, count: 1`）；放 3 个 / 5 个**都无产物**（`Test failed`）。
- 转换器里 `molotov` / `zangief_cane` / `wild_apple` 各转一次，各扣 3 点经验；
  `golden_apple ⇄ wild_apple` **正反各一次**都成功（双向可用）。

## 5. 产物核对（两个 jar）

```
$ Get-Item build/libs/farmstorm-0.0.2+mc1.21.1.jar, dist/farmstorm-0.0.1+mc1.21.1.jar
farmstorm-0.0.2+mc1.21.1.jar = 2,180,993 B      （mtime 2026-10-29 17:17:01，删掉 jar 后重新构建）
farmstorm-0.0.1+mc1.21.1.jar =   336,451 B
delta = +1,844,542 B      ratio = 6.482 倍     → 验收 3 过
```
jar 内（`System.IO.Compression.ZipFile` 直读）：
```
jar 内 recipe 条目 = 27（26 个文件 + 目录）
  data/farmstorm/recipe/converter.json = True
  data/farmstorm/recipe/compressed_puffed_wheat.json = True
  data/farmstorm/recipe/molotov.json = True
  data/farmstorm/recipe/zangief_cane.json = True
  data/farmstorm/recipe/wild_apple.json = True
jar 内 loot_table/blocks 条目 = 22（F-74 的两张新表在内）
jar 内 data/minecraft/tags/block/needs_diamond_tool.json = True
jar 内 *Probe* 条目 = 0
```

## 6. build 与 runData 的输出行

```
$ .tools\gw.cmd build -PfsRunDir=run-f75          # .work/f75-build.log
> Task :jarJar NO-SOURCE
> Task :jar
> Task :build
BUILD SUCCESSFUL in 8s
5 actionable tasks: 2 executed, 3 up-to-date

$ .tools\gw.cmd runData -PfsRunDir=run-f75        # .work/f75-data-1.log（--all 已由 V-23 烧进 run 配置，不用显式传）
[17:16:41] [main/INFO] [minecraft/HashCache]: Caching: total files: 187, old count: 187, new count: 188, removed stale: 0, written: 0
BUILD SUCCESSFUL in 16s
```
`written: 0` = **datagen 零漂移**：本任务没有任何 datagen 改动，那条"第二次必须 `written: 0`"的门禁**第一次就成立**。
（如实记录：我第一次把 `--all` 当成 gradle 参数传进去，得到 `Unknown command-line option '--all'` —— 那是我调用方式错，
不是 datagen 的问题；去掉 `--all` 后一次通过。）

## 7. 探针与残留 / 改动清单

验证期临时探针 `com.dashpig.farmstorm.probe.F75Probe`（`@EventBusSubscriber` 自注册，命令
`/f75probe setup|give|xp|inv|whoami|raw|convert`；无 tick 钩子、无自动行为、只由显式命令触发）：

| 检查 | 结果 |
|---|---|
| `src` 里 `Probe` 命中 | **4 处，全是既有玩法常量 `LANDING_PROBE`**（`MolotovProjectile.java`:42/94、`PuffedWheatProjectile.java`:31/64），无探针类 |
| `build\classes` 里 `*Probe*` 文件 | **0**；`build\classes\...\farmstorm\probe` 目录 = **False** |
| jar 内 `*Probe*` 条目 | **0** |

```
$ git diff --stat
（空输出）      → **零源码改动**（本次没有发现需要修的缺陷）

$ git status --porcelain
?? .work/TASK-V002-F75.md
?? .work/f75-build.log  ?? .work/f75-data-1.log  ?? .work/f75-server.log
?? .work/f75-server-attempt2.log  ?? .work/f75-server-wet.log
?? .work/f75-smoke.js  ?? .work/f75-stop.js
?? .work/f75-verify.js  ?? .work/f75-verify-out.txt  ?? .work/f75-verify-out-attempt2.txt
?? .work/f75-wet.js  ?? .work/f75-wet-out.txt
（只有 `.work/` 证据文件；`src` 与 `build` 干净）
```
**未 commit**（按任务书要求）。

## 8. 收尾与环境纪律

| 项 | 记录 |
|---|---|
| 运行目录 | `-PfsRunDir=run-f75`（runData 与 runServer 同一目录） |
| 服务端 | `.tools\gw.cmd runServer -PfsRunDir=run-f75` → `Done (0.656s)!` + `RCON running on 0.0.0.0:25632` |
| 启动自检 | `bake() 派生快照：molotov.fireChance=85.0%（余下 15.0% 归岩浆块）；... 爆炸抗性阈值 6.0 / 6.0` |
| forceload | `forceload add 96 96 128 128`（**方块坐标**，V-16）→ `Marked 9 chunks ... from [6, 6] to [8, 8]` |
| 清场基线 | 每段投掷前逐段 `fill ... air` + 基线断言（§3 那些 `§8 基线（…）：立方体已复位（石头 9 / 空气 18）`） |
| 收尾 | `kill @e` 两轮 → `fill` 清场 → 三处 `execute if block ... minecraft:air` 全 `Test passed` → `forceload remove` |
| 进程 | RCON `stop` 后 `Get-Process java` = **0**；`.tools/runserver.lock` = **False** |
| 世界污染处置 | 第一轮（attempt2）跑 §8 时发现两处**我自己的脚手架 bug**：膨胀麦台复位 `fill` 的 z 坐标写成 113..115（应为 99..101），导致立方体没复位、读数变成"18 格"；另有上一轮烧死的牛留下的皮革掉落物把 `@e[...,limit=1]` 顶掉。按 V-17 删掉 `run-f75/world` 全新重跑，并把选择器改成带 NBT 过滤，最终一轮 0 FAIL |
| 提交 | **未 commit** |

## 9. 未验证的部分 / 与任务书表述的偏差（不粉饰）

**未验证**（明确不声称通过）：
1. **湿态以外的 §11 #8 细节**：只测了"干 3 / 遇水 5 / 基岩挡 1 格"三种，没有测"多个水源 / 流动水 / 多层水"。
2. **§11 #7 的逐格覆盖范围**：只逐格核对了 y=99 平台 81 格 + y=100 的 49 个候选格与半径外 32 格，
   **没有**对整个区块做全量快照比对（等价性靠"只往候选格正上方写"的代码结构 + 这三层的逐格读数）。
3. **F-74 的"木镐 30 秒不掉"** 不在本任务五步闭环内，本回归**没有重测**（不写"通过"也不写"破了没掉"）。
4. **客户端相关项**（`§11 #4/#5/#6` 等）本任务不覆盖。
5. **§11 #20 的"工作台"** 用 crafter 等价路径（同一 `RecipeManager` 判定），没有真人手摆工作台（headless 无客户端）。

**偏差 1（需要调度方裁决的表述问题）**：任务书写"转换需要**下界之星**当催化剂（D-47），记得先 `/give` 星星"。
实测：**三条新配方都不需要下界之星**（`molotov.json` / `zangief_cane.json` / `wild_apple.json` 里没有 `catalyst` 字段，
转换前后催化剂槽的 1 个星星原样不动）。真正吃下界之星的是 `awakened_torchflower` / `awakened_pitcher_plant`
的**正向**转换（D-12），我用第 5 次转换（torchflower）当场证明了这条路径（`catalystSlotAfter=empty`）。
所以"记得先给星星"这个动作在本任务里是**无害但非必需**的；建议把任务书这句改成
"转换器 GUI 有下界之星催化剂槽（D-47），三条新配方不占用它；火把花/瓶子草正向才扣 1 个"。

**偏差 2（headless 的等价替代）**：没有客户端，因此"玩家"由 NeoForge `FakePlayer` 承担：
- `/give` 是**真实 vanilla `give`**，在 FakePlayer 身份下执行并抓回原版反馈原文（`Gave 1 [Nether Star] to F75Probe`）；
- 转换是**真实 `ConverterMenu#clickMenuButton(BUTTON_CONVERT)`**（真实方块实体 + 真实 `ServerPlayer` 子类），
  不是直调 `ConverterBlockEntity` 的私有方法；
- 放置方块用 `/setblock`（无手可放），投掷用 `/summon` + `Motion:[0,0,0]` 自由落体（F-68/F-70 的同一手法）。
这三条都是**上一批任务已经用过的手测等价路径**，本任务沿用。

**偏差 3（V-24 的再次印证）**：任务书要求在闭环里核经验，实测扣的是**点数**不是等级：
`marginXp=188->185` 而 `levels` 从 11 掉到 10（那一步 `points` 恰好为 0，扣 3 点跨了一级）。
所以"各少 3 点"应读作"各少 3 个经验点"。

## 10. 证据文件

| 文件 | 内容 |
|---|---|
| `.work/f75-verify-out.txt` | 五步闭环的完整命令与返回原文 + 75 条断言（`PASS 75 / FAIL 0`） |
| `.work/f75-wet-out.txt` | §11 #8 湿态（边长 5）补充，14 条断言 0 FAIL |
| `.work/f75-server.log` | 最终一轮服务端完整日志（含 `莫洛托夫 落点/候选` 21 行） |
| `.work/f75-server-wet.log` | 湿态补充那一轮的服务端日志 |
| `.work/f75-server-attempt2.log` / `.work/f75-verify-out-attempt2.txt` | 被作废的上一轮（脚手架 bug）原始输出，留着做处置记录 |
| `.work/f75-verify.js` / `.work/f75-wet.js` | RCON 驱动器（断言都在这里） |
| `.work/f75-build.log` / `.work/f75-data-1.log` | build 与 runData 的输出 |
| `.work/f75-smoke.js` / `.work/f75-stop.js` | 探针冒烟测试 / RCON 停服脚本 |
