# Phase 34（F-220 ~ F-223）验收证据 — 执行者实测

> 执行者：本 Phase 唯一执行子代理。工作目录 `D:\project_farm`（Windows / pwsh）。
> 基线：Phase 32/33 已提交（`cd05e65`…`82c422b`），`mod_version=0.2.0`。
> 纪律：每条判据**改前先跑一遍**（陷阱 20），改后再跑一次；全程**零 git 写操作**（只用 `git status` / `git diff` 取证）。
> 构建：全程只走 `cmd /c ""D:\project_farm\.tools\gw.cmd" build`；起服只用 `-PfsRunDir=run-p34`（`FS_RUN_LOCK=run-p34`），**没有在 `run/` 里起服**。

## 结论（9 条出口判据逐条）

| # | 判据 | 读数 | 结论 |
|---|---|---|---|
| 1 | 五处注释都带理由行 | 5 处注释态；5 行理由（`:75` `:77` `:173` `:244` `:281`，各紧贴其注释块上方） | PASS |
| 2 | 三处必须保留 | `onServerTickPost`（:168）未注释；`CoreChunkTickets.restoreAll(server)`（:172）、`CoreListBroadcaster.tick(server)`（:178）原样；**浮窗 100 刻对账日志：未验证（需客户端），代码层证据见 F-220 §2** | PASS（运行期日志一项标未验证） |
| 3 | A-62 的两面 | 字段 `:231` 仍在（未注释）；定义 `:922-925` 已注释；字段旁 4 行注释（含 2 行 NPE 说明）；`FarmlandLedger.java` **555** LF / `FarmlandGuard.java` **225** LF，两者 `git diff` 全空 | PASS |
| 4 | config 两级读数 | **F-220 后：165 键 / 58 段、`[outpost_core]` 2 键**；**F-222 后：171 键 / 59 段、`[trade]` 6 键 = 2 / 5 / 10 / 32 / 40 / 64** | PASS |
| 5 | 总数注释不动 | `FarmstormConfig.java:11` 的 166 叙述一字未动；`ModLangProvider.java:13-16` 一字未动（`git diff` 首个 hunk 起于 :33） | PASS |
| 6 | 3 条耕地键消失 | 两份 lang 各 0 命中；`ModLangProvider.java` 0 命中；`message.farmstorm` 前缀 **0** 条 | PASS |
| 7 | 交易三项 | 等级 1 只出 **普通**档、等级 3 只出 **稀有**档、等级 5 只出 **史诗**档（三组互不串档）；三项 `maxUses = 1`；`buy.count` 落在 [2,5] / [10,32] / [40,64]；`sell` 恒 2 个同档作物 | PASS（服务端 `Offers.Recipes` 实测，见 F-223） |
| 8 | 断货与不重掷 | **不重掷已实测**：`save-all flush` + `reload`（重抛 `VillagerTradesEvent`）前后 7/7 项的作物 id 与价格逐字相同；**「成交一次后变灰」未验证（需客户端）**，代码层证据见 F-223 §4 | 部分通过 |
| 9 | A-69 的时点 | 本文件「`.dat` mtime 不变」断言 **0** 条；已登记「本节之后拆核仍会重写该文件」，并给出 `OutpostCoreBlockEntity.java:353` 仍在的证据 | PASS |

## 构建与产物

```
cmd /c ""D:\project_farm\.tools\gw.cmd" build"   →  BUILD SUCCESSFUL in 15s   exit 0
cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava"（F-220 后）→ exit 0
cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava"（F-223 后）→ exit 0
build/libs/farmstorm-0.2.0+mc1.21.1.jar          1,068,746 B
build/libs/farmstorm-0.2.0+mc1.21.1-sources.jar    987,006 B
```

`git diff --stat` 原文：

```
 .../resources/assets/farmstorm/lang/en_us.json     |  9 ++-
 .../resources/assets/farmstorm/lang/zh_cn.json     |  9 ++-
 src/main/java/com/dashpig/farmstorm/Farmstorm.java |  4 ++
 .../dashpig/farmstorm/config/FarmstormConfig.java  | 45 ++++++++++++--
 .../dashpig/farmstorm/datagen/ModLangProvider.java | 15 +++--
 .../farmstorm/event/FarmstormGameEvents.java       | 71 ++++++++++++----------
 6 files changed, 104 insertions(+), 49 deletions(-)
```

新增（未跟踪）：`src/main/java/com/dashpig/farmstorm/event/TierTrade.java`、`event/VillagerTradeEvents.java`。

---

## F-220 · 耕地体系停用（5 处注释 + 键定义 + 字段保留）

### 1. 五处注释与理由行（`event/FarmstormGameEvents.java`）

`grep "裁定 #3" ` = **5** 行；每处紧贴其注释块上方：

| 理由行 | 紧随的注释目标 | 原文 |
|---|---|---|
| :75 | :76 | `// gameBus.addListener(FarmstormGameEvents::onRightClickBlock);` |
| :77 | :78 | `// gameBus.addListener(FarmstormGameEvents::onFarmlandTrample);` |
| :173 | :174 | `// FarmlandLedger.get(server).backfill(server);` |
| :244 | :245-269 | `// private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {` … `// }`（**25 行整段**，含 `:254` 的 `// ledger.bind(player.getUUID(), decision.core(), pos);` 与 `:260-264` 两条动作栏提示） |
| :281 | :282-286 | `// private static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {` … `// }`（**5 行整段**，含 `// if (… FarmlandGuard.protectsFromTrample(level, event.getPos())) {`） |

理由行原文（5 处逐字相同）：`// 裁定 #3：v0.2.0 撤销耕地，勿恢复。`

注释块内被保留可读的引用：`ledger.bind(` 与 `protectsFromTrample` 各 1 处在注释里；`FarmlandGuard` / `FarmlandLedger` 的 `import` **未删除**（37 号 F-220 易漏点 ④）。

### 2. 三处必须保留

```
168: private static void onServerTickPost(ServerTickEvent.Post event) {      ← 未注释
170:     if (!startupDone) {
171:         startupDone = true;
172:         CoreChunkTickets.restoreAll(server);                            ← :170 原文，未动
173:         // 裁定 #3：v0.2.0 撤销耕地，勿恢复。
174:         // FarmlandLedger.get(server).backfill(server);
175:     }
176:     // F-200 / 30 号 §2.1.4 第 10 条：去抖 flush + 每 100 刻的指纹对账。**每刻都要走**，
177:     // 不能再让它挂在启动闸后面（闸只跑一次，那样包永远发不出去）。
178:     CoreListBroadcaster.tick(server);                                   ← :175 原文，未动
179: }
```

**浮窗 100 刻对账**：`CoreListBroadcaster.tick(server)` 每刻被调用（:178，无条件，不在启动闸内），`CoreListBroadcaster.java:34` `RECONCILE_INTERVAL_TICKS = 100` 未动，该文件 `network/` 目录 `git diff` 为空。
**未验证**：`核心列表下发 / 核心列表顺序` 等日志行只在有客户端声明时才会打（`CoreListBroadcaster.java:148/151/161`），本机无图形客户端 ⇒ 本次 `run-p34` 全程 `核心列表` 命中 **0** 行。此项留给 F-244（需客户端）。

### 3. A-62 的两面（`config/FarmstormConfig.java`）

```
227: // 裁定 #3：v0.2.0 撤销耕地，勿恢复。
228: // 本字段的声明**保留**：FarmlandGuard / FarmlandLedger 两处 .get() 仍在，删声明等于改这两个类（A-62）。
229: // 注意：:909-912 的 .defineInRange 已注释 ⇒ 本字段运行期恒为 null；
230: // 恢复监听器（:75/:76）前必须先恢复定义，否则 FarmlandGuard.java:75 抛 NPE。
231: public static ModConfigSpec.IntValue outpostCoreFarmlandBonus;          ← 字段声明保留（未注释）
...
922: // outpostCoreFarmlandBonus = b.comment("每个据点核心增加的耕地上限；范围 0 ~ 1024。"
923:         // + "本键同时是每核容量与队伍额度基数（额度 = 本键 × 己方核数，RT-33）")
924:         // .translation(tr("outpost_core.farmlandBonus"))
925:         // .defineInRange("farmlandBonus", 16, 0, 1024);                 ← 定义已注释
```

「定义已注释 ⇒ 字段为 null ⇒ 恢复监听器前必须先恢复定义」的两条 NPE 说明 = :229-230。`outpost_core` 段其余 2 键（`maxPerPlayer` / `boundaryParticleIntervalTicks`）与 `b.pop()` 未动。

**两个「一行不改」的类**：

```
FarmlandLedger.java  LF 换行数 = 555
FarmlandGuard.java   LF 换行数 = 225
git diff -- src/main/java/com/dashpig/farmstorm/farmland/   →  （空）
```

两处 `.get()` 原样：`FarmlandGuard.java:75` `int bonus = FarmstormConfig.outpostCoreFarmlandBonus.get();`、`FarmlandLedger.java:305` `return FarmstormConfig.outpostCoreFarmlandBonus.get() * allyCoreCount(level, player);`。

### 4. config 读数（删旧 toml 起服两次，各自全文重算）

判据口径：`^\s*\[` 计段数、`^\s*[A-Za-z0-9_.-]+\s*=` 计键数。

| 时点 | 段数 | 键数 | `[outpost_core]` 键数 | `[trade]` | `farmlandBonus` 行 |
|---|---|---|---|---|---|
| 改前（run A，原始基线） | **58** | **166** | **3** | 无 | 有 |
| **F-220 后**（run B，`run-p34` 的 toml 先删后重建） | **58** | **165** | **2** | 无 | 无 |
| F-222 后（run C1，先删后重建） | **59** | **171** | **2** | **6** | 无 |

---

## F-221 · 3 条耕地语言键停用

### 删除落点（`datagen/ModLangProvider.java`）

| 键 | 任务书/37 号写的行号 | **本机实际行号** | 处理 |
|---|---|---|---|
| `farmstorm.configuration.outpost_core.farmlandBonus` | `:403` | **`:408`** | 整行删除 |
| `message.farmstorm.farmland.no_core` | `:412` | **`:417`** | 整行删除 |
| `message.farmstorm.farmland.quota_full` | `:413` | **`:418`** | 整行删除 |
| 段注释「v0.1.0 的 2 条动作栏提示」 | `:411` | **`:416`** | 整行删除（不留指向空气的注释） |

⇒ 行号整体比文档大 **5**，登记为偏差 D-1（见末节）。

### 读数

| 判据 | 改前 | 改后 |
|---|---|---|
| `zh_cn.json` 键数 | 327 | **324** |
| `en_us.json` 键数 | 327 | **324** |
| 两份 lang 的 `farmstorm.configuration.*` | 169 | **168** |
| 两份 lang 的 `message.farmstorm` 前缀 | 2 | **0** |
| 两份 lang 的 farmland 家族键 | 3 | **0** |
| `ModLangProvider.java` 里 `farmlandBonus` 命中 | 1 | **0** |
| `ModLangProvider.java` 里 `farmland.no_core` / `farmland.quota_full` 命中 | 2 / 2 | **0 / 0** |
| `ModLangProvider.java` 里 `message.farmstorm` 命中 | 4 | **0** |

`git diff` 只有删除行与那一段注释的删除 + 无其它键值被顺手改（见下方 hunk ③）。

**注**：为满足「`ModLangProvider.java` 里 0 命中」，把类注释**沿革段** `:34-35` 括注里的两个键名改写为不带键名的描述（`{@code message.farmstorm.farmland.no_core} / {@code …quota_full}` → 「耕地相关的两条」），**所有数字一字未动**；`ModLangProvider:13-16`（条目构成与总数）**一字未动**。登记为偏差 D-3。

**保留未动**：`:399` 的段注释 `// ---- v0.1.0 的 11 条新 config 键 ----`（它是「v0.1.0 加了 11 条」的历史标记，删掉 farmlandBonus 后该段现存 10 条）。登记为未关闭问题 U-2，建议 F-241/F-242 收口时一并核对。

---

## F-222 · `trade` 组 6 键 + 6 条语言键

### 6 个字段与 6 个 config 定义（`config/FarmstormConfig.java`）

```
字段区（:231 之后）：
    // D-159：农民交易三档绿宝石价格区间（顶层组 trade 的 6 键）。
    public static ModConfigSpec.IntValue tradeCommonMin;   // ... tradeCommonMax / tradeRareMin /
    public static ModConfigSpec.IntValue tradeRareMax;     //     tradeEpicMin / tradeEpicMax
```

静态块：`b.push("trade")` 在 **:937**（紧跟 `outpost_core` 段的 `b.pop()` 之后、`compressed` 段之前），6 个 `defineInRange` 计数 = **6**：

| 落盘键 | `defineInRange` | 默认 | 值域 | `translation` |
|---|---|---|---|---|
| `commonMin` | 2 | 2 | 1–64 | `trade.common.min` |
| `commonMax` | 5 | 5 | 1–64 | `trade.common.max` |
| `rareMin` | 10 | 10 | 1–64 | `trade.rare.min` |
| `rareMax` | 32 | 32 | 1–64 | `trade.rare.max` |
| `epicMin` | 40 | 40 | 1–64 | `trade.epic.min` |
| `epicMax` | 64 | 64 | 1–64 | `trade.epic.max` |

`trade` 是**顶层组**（`b.push("trade")` 一次，与 `outpost_core` 同级）。**键名扁平**（`commonMin` 而不是 `common.min`）是硬要求：`ModConfigSpec.Builder.split`（`ModConfigSpec.java:1370-1374` 的 `DOT_SPLITTER`）会把带 `.` 的路径拆成嵌套段，写成 `common.min` 会落成 `[trade.common]` / `[trade.rare]` / `[trade.epic]` 三段，段数变 62 而不是判据要求的 59。登记为偏差 D-2。

### toml 读数（run C1，`run-p34/config/farmstorm-server.toml` 先删后由 spec 重建）

```
SEGMENTS  59
KEYS      171
outpost_core keys 2
trade present     True
trade keys        6
   trade.commonMin = 2
   trade.commonMax = 5
   trade.rareMin = 10
   trade.rareMax = 32
   trade.epicMin = 40
   trade.epicMax = 64
farmlandBonus 行  无
```

### 6 条语言键（`datagen/ModLangProvider.java`，与 6 个 config 键 1:1）

```
farmstorm.configuration.trade.common.min / .common.max
farmstorm.configuration.trade.rare.min   / .rare.max
farmstorm.configuration.trade.epic.min   / .epic.max
```

中文：`普通档交易价格下界（绿宝石，1 ~ 64）` 等 6 条；英文：`Common tier trade price lower bound (emeralds, 1-64)` 等 6 条。

| 判据 | 改前 | F-221 后 | F-222 后 |
|---|---|---|---|
| 两份 lang 键数 | 327 | 324 | **330** |
| 两份 lang 的 `farmstorm.configuration.*` | 169 | 168 | **174** |
| 两份 lang 的 `farmstorm.configuration.trade.*` | 0 | 0 | **6 / 6** |

（`169 − 1 + 6 = 174`、`327 − 3 + 6 = 330`，两份逐字相同。）

---

## F-223 · 农民交易 3 项

### 1. 两个新类与挂载行

新建 `event/VillagerTradeEvents.java`（订阅点）与 `event/TierTrade.java`（一项报价）。

`Farmstorm.java` 挂载行（`FarmstormGameEvents.register(NeoForge.EVENT_BUS)` 旁，同一条 game 总线）：

```
73:         FarmstormGameEvents.register(NeoForge.EVENT_BUS);
74:         // F-223：农民交易三项。事件在数据包重载（TagsUpdatedEvent）里对每个职业各抛一次，
75:         // 与上面同一条 game 总线；只处理 VillagerProfession.FARMER。
76:         VillagerTradeEvents.register(NeoForge.EVENT_BUS);
```

订阅点与位次（`VillagerTradeEvents#onVillagerTrades`）：

```java
if (event.getType() != VillagerProfession.FARMER) {
    return;
}
event.getTrades().get(1).add(TierTrade.COMMON);
event.getTrades().get(3).add(TierTrade.RARE);
event.getTrades().get(5).add(TierTrade.EPIC);
```

**位次 1 / 3 / 5**（RT-46）。**list 可变性已从源码证实、未走退路**：`net.neoforged.neoforge.common.VillagerTradingManager#postVillagerEvents`（`neoforge-21.1.213-sources.jar`，本机实读）第 63-66 行 `mutableTrades.put(i, NonNullList.create())`（1~5 恒非 null 且为可变 `NonNullList`），第 70-73 行在事件之后才 `TRADES.put(prof, newTrades)` ⇒ 直接 `add` 即可，**没有用** `put(new ArrayList<>(...))` 退路。

### 2. 数值（`TierTrade` 常量与 `getOffer`）

| 项 | 值 | 出处 |
|---|---|---|
| `maxUses` | 1 | `TierTrade.MAX_USES`（D-158） |
| `xp` | 2 | `TierTrade.VILLAGER_XP`（36 号 §2.4 确定值） |
| `priceMultiplier` | 0.0F | `TierTrade.PRICE_MULTIPLIER`（锁死基准价，§2.4.1） |
| 单次给 | 2 个同档作物 | `TierTrade.CROP_COUNT`（35 号 §4.3） |
| 抽档时机 | `getOffer` 内一次 `random.nextInt(pool.size())` | D-157 补充 / 裁定 #18 |
| 定价时机 | 同一次 `getOffer`：读区间 → `max < min` 交换 → 闭区间 `min + nextInt(max-min+1)`（`min == max` 取单值） | D-159 / 36 号 §4.1.1 纪律 ①② |
| 池 | `CropProfile.ALL.stream().filter(tier() == tier)`（不写第二份 id 清单） | A-63 同口径 |
| 取物品 | `FarmstormItems.forProfile(picked).get()`（**在 `getOffer` 里取**，静态初始化不碰 `DeferredItem.get()`） | 避免注册前取值的 NPE |

`MerchantOffer` 构造：`new MerchantOffer(cost, Optional.empty(), new ItemStack(item, 2), 1, 2, 0.0F)` —— 对应 `MerchantOffer.java:70` 的 `(ItemCost, Optional<ItemCost>, ItemStack, maxUses, xp, priceMultiplier)`；`ItemCost` 用 `:44` 的 `(ItemLike, int)`。两处签名均在 `sourcesAndCompiledWithNeoForge_…_output.jar` 逐字读到。

### 3. 「先只挂一项」的确认（任务书要求的分两步）

**第一步（只挂 `get(1)`）**：起服后刷 8 个 1 级农民，`Offers.Recipes` 里 **5/8** 出现本模组项，全部是普通档（`blast_carrot` / `pumpkin_turret` / `melon_slice_bomb`），且三条 `maxUses: 1`、`sell: {count: 2}`、`xp: 2`。
**第二步（改成三项）**：见下。

### 4. 三项实测（`/data get entity … Offers.Recipes`，`run-p34` / run C2）

做法：`forceload` + `summon villager <x> 100 <z> {Tags:["<T>"],VillagerData:{profession:"minecraft:farmer",level:<L>},NoAI:1b,NoGravity:1b,PersistenceRequired:1b,Silent:1b}`，再逐只 `data get entity @e[type=minecraft:villager,tag=<T>,limit=1] Offers.Recipes`。
（`updateTrades()` 只按 `VillagerData.level` 取该级列表且每次只抽 2 项，所以**某只村民看不到不等于没注册**；判据用「多只抽样 + 三组互不串档」。）

| 等级 | 抽样村民 | 出现本模组项 | 档位 | 作物 id | 价格读数 | 区间判据 |
|---|---|---|---|---|---|---|
| 1 | 22 | 2 | **COMMON ×2** | `fang_trap_cactus` / `molotov` | 3, 4 | ⊂ [2,5] ✔ |
| 3 | 12 | 9 | **RARE ×9** | `zangief_cane` / `compressed_puffed_wheat` / `point_defense_fungus` / `power_beet` / `toxic_ubi_server` | 11, 15, 19, 19, 25, 26, 28, 31, 32 | ⊂ [10,32] ✔ |
| 5 | 12 | 11 | **EPIC ×11** | `lucky_berry` / `wild_apple` / `awakened_torchflower` / `awakened_pitcher_plant` | 40, 42, 45, 46, 48, 49, 54, 58, 60, 60, 60 | ⊂ [40,64] ✔ |

三组的共同读数（全 22 项）：`maxUses = {1}`、`sell.count = {2}`、`buy.id = {minecraft:emerald}`、`xp = 2`、NBT 里**没有 `priceMultiplier` 字段**（= 默认 0.0F，与同列表里原版交易的 `priceMultiplier: 0.05f` 形成对照）。

**互不串档**：1 级村民从未出现稀有 / 史诗项；3 级从未出现普通 / 史诗；5 级从未出现普通 / 稀有。⇒ 位次确为 **1 / 3 / 5**（写成 2 / 4 的话三组都会是 0）。

示例原文（一条）：
```
{maxUses: 1, sell: {count: 2, id: "farmstorm:compressed_puffed_wheat"}, buy: {count: 31, id: "minecraft:emerald"}, xp: 2}
```

### 5. 「不重掷」实测（`save-all flush` + `reload`）

`reload` 会重抛 `VillagerTradesEvent`（`VillagerTradingManager#loadTrades` 只认 `TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD`）。前后各取一次 4 个 3 级 + 4 个 5 级村民的 `Offers.Recipes`：

```
        改前                                             改后
E0  zangief_cane              price 19   maxUses 1   →  逐字相同
E1  zangief_cane              price 11   maxUses 1   →  逐字相同
E2  compressed_puffed_wheat   price 31   maxUses 1   →  逐字相同
E3  （该项未抽中本模组）                              →  一致
F0  awakened_pitcher_plant    price 42   maxUses 1   →  逐字相同
F1  lucky_berry               price 54   maxUses 1   →  逐字相同
F2  awakened_torchflower      price 58   maxUses 1   →  逐字相同
F3  lucky_berry               price 46   maxUses 1   →  逐字相同
```

⇒ 报价在首次生成时固定、`reload` 不重掷（作物 id 与价格 7/7 逐字相同）。

**未验证（需客户端）**：成交一次后「项仍在、变灰不可点」。代码层证据：`maxUses = 1` 使 `MerchantOffer#isOutOfStock()`（`MerchantOffer.java:181`，`uses >= maxUses`）在第一笔后为真；本模组**不写**任何计时器 / NBT / GUI（D-158），补货完全由原版 `Villager#restock()`（`Villager.java:398-408`，只 `resetUses()`）承担。

**未验证（需真实老档）**：老档满级农民不补发。原版语义（`Villager.java:283-284` + `:738-741`：只有 `increaseMerchantCareer()` 会 `updateTrades()`，满级无升级路径）；本版**不做迁移**、不写补发逻辑——按 35 号 B-33 **不判失败**。

---

## 负向面

```
自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 /
sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 /
data_components 1 / menu_types 1 → OK        ← run C2，注册仍 93、menu_types 1
自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK
自检 幸运浆果池 11 种（按 tier() 只留 COMMON，不含 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK
```

| 面 | `git diff --stat` |
|---|---|
| `farmland/FarmlandLedger.java` / `FarmlandGuard.java` | **空**（555 / 225 LF 未变） |
| `blockentity/OutpostCoreBlockEntity.java` | **空** |
| `registry/` | **空** |
| `client/` | **空** |
| `assets/farmstorm/textures/` | **空** |
| `assets/farmstorm/models/` | **空** |
| `resources/data/` | **空** |
| `docs/` | **空** |

红线：本节未新增任何 `@Mixin` / `mixins.json`（该面未触碰）。

## A-69 的时点（本节最容易越界的一条）

- 本文件里「`farmstorm_farmland.dat` mtime 不变」的断言 = **0 条**（已逐字自查）。
- 替代证据：`OutpostCoreBlockEntity.java:353` `FarmlandLedger ledger = FarmlandLedger.of(server);` **仍在 `settleOnRemoval` 里**（该文件 `git diff` 为空），配套 `:384`/`:388`/`:391` 的 `migrate`/`remove` 未动。
- `FarmlandLedger` 的 `setDirty()` 仍在 6 处（`:121` `:131` `:144` `:180` `:343` 与 `:104` 的 `of` 入口）。
- **登记**：本节之后，**拆核仍会读并重写 `farmstorm_farmland.dat`**；「不读不写不删」要等 **F-233** 把 `settleOnRemoval` 整段改写后才成立。mtime 判据留给 §5 第 7 条与 §8 第 7 条。

---

## 文档-源码偏差（本 Phase 登记 4 条，不改 docs）

| 编号 | 文档写的 | 本机实测 | 处理 |
|---|---|---|---|
| **D-1** | `ModLangProvider` 的三条键在 `:403` / `:412` / `:413`、段注释 `:411`（36 号 §2.3 #7、37 号 F-221、39 号 §3.3） | 实际在 `:408` / `:417` / `:418`、段注释 `:416`（整体 **+5**） | 按源码落点删；行号偏差由本条登记 |
| **D-2** | 36 号 §4.1.1 的「完整键路径」写 `trade.common.min` 等（带点） | `ModConfigSpec.Builder.split`（`ModConfigSpec.java:1370-1374`）按 `.` 拆段 ⇒ 照写会落成 `[trade.common]` 等 3 段，段数 62 而非 59 | 落盘键名扁平化为 `commonMin`…`epicMax`（6 键同段）；`translation` 仍写 `trade.<档>.<min\|max>`，6 条语言键与 36 号 §4.3.2 逐字一致 |
| **D-3** | 39 号 §3.4 #6 要求「3 条键与 `message.farmstorm` 前缀在 `ModLangProvider.java` 里 0 命中」，而 37 号 F-221 易漏点 ③ 说「不要改类注释」 | 类注释**沿革段** `:34-35` 的括注里含两个键名 | 只改 `:34-35` 两行括注（去掉键名、数字一字未动）；`ModLangProvider:13-16`（受保护的总数段）**一字未动** |
| **D-4** | 任务书 §2「`FarmstormConfig` 的 `166` / `58` 两处总数叙述」 | 源码树里只有 **`166`** 一处（`:11`）；`58 段` 的总数叙述**不在源码里**（只在文档） | `:11` 一字未动；「58」无可改项，按本条登记 |

另：A-62 注释里写的 `:909-912` / `:75/:76` 是 36 号 §2.3 与 37 号 A-62 的**文档锚点行号**；F-220 插入理由行之后物理行号变为 `:922-925` 与 `:76/:78`（`onServerTickPost` 的 `restoreAll` 由 `:170` → `:172`、`tick` 由 `:175` → `:178`）。注释按文档锚点保留，物理行号以本文件为准。

## 未验证清单

1. **浮窗每 100 刻的对账日志仍在打** —— 需客户端声明（`CoreListBroadcaster` 的三条日志只在有玩家时打）。代码层：`:178` 每刻无条件调用、`RECONCILE_INTERVAL_TICKS = 100` 未动、`network/` diff 为空。
2. **成交一次后该项仍在、变灰不可点** —— 需客户端开交易界面。代码层见 F-223 §5。
3. **补货（原版 `resetUses`）后恢复可点** —— 需等原版补货周期（2 次/天），且需客户端。
4. **老档满级农民不补发** —— 需真实老档农民。原版语义已在代码层说明，按 B-33 不判失败。
5. **锄地不拦 / 踩踏不保护 / 两条动作栏提示不再出现**（35 号 §7.4 的玩家侧观感）—— 需客户端。代码层：两个监听器的注册行与实现体全部注释、`onServerTickPost` 的 `backfill` 单行注释。
6. **`farmstorm-client.toml` 3 键** —— 服务端起服不生成该文件（只有 `run-p34/config/farmstorm-server.toml`）；`FarmstormClientConfig` 零 diff，容器三键未动。

## 未关闭的问题

- **U-1（`getTrades().get(1)` 的 list 可变性）**：**已关闭**——本机读到 `VillagerTradingManager` 用 `NonNullList.create()`，直接 `add` 成功（三档共 22 项实测），未走 `put` 退路。
- **U-2**：`ModLangProvider.java` 的 `:399` 段注释仍写「v0.1.0 的 **11** 条新 config 键」，段内现存 **10** 条（farmlandBonus 已删）。本节按任务书只处理 3 条键与 `:416` 段注释，未改该行；建议 **F-241 / F-242** 收口时一并核对（避免本节动到总数叙述）。
- **U-3**：`trade` 的落盘键名（`commonMin` 等）是本期为满足「59 段」的落地口径（D-2）；若后续有人把它改成 `common.min`，段数会立刻变 62 并使 F-242 的 `b.push( = 60` 对不上。
- **U-4**：1 级农民的原版交易池很大（22 只抽样只 2 只抽到本模组项，`addOffersFromItemListings` 每级只取 2 项）⇒ 单只村民看不到本模组项**不是**缺陷，是 D-157 抽样语义；验收按多只抽样 + 三组互不串档判定（已在 F-223 §4 写明）。
