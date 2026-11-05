# F-284 · 47 号 §8 的 27 条边界（v0.2.2 / Phase 53 / W07）

> 执行者：W07 执行子代理。HEAD `c382af5`。判据来源：`docs/zh/47-DESIGN-0.2.2.md` §8 的 B-01 ~ B-27。
> 分组按 47 号原文：**B-01 ~ B-11 属 R1**（植物学家职业）/ **B-12 ~ B-14 属 R2**（平凡农作物）/ **B-15 ~ B-18 属 R3**（方块标签与名额）/ **B-19 ~ B-27 属 R4 / R5**（界面两个 bug 与计数面）。
> 与 F-283 分开记（48 号验收表不含 B 面）。能复用验收场景的一律复用，并在「证据」列写明复用哪一条。
> 六条**负向判据**（B-02 / B-06 / B-12 / B-13 / B-16 / B-27）的判据是「**没发生**」，每条都写明「没有发生的证据」。
> B-05 与 B-08 是**算术读数**，写数不写形容词。

| 编号 | 组 | 场景 | 期望行为 | 读数（原文） | 判定 | 证据 |
|---|---|---|---|---|---|---|
| B-01 | R1 | 玩家遇到第一个植物学家 | 只卖 **2 项**（22 项里随机 2 项） | `data get entity @e[tag=p,limit=1] Offers` → `{Recipes: [{maxUses: 8, sell {count: 1, id: "minecraft:poisonous_potato"}, buy {count: 5, id: "minecraft:emerald"}, xp: 2}, {maxUses: 8, sell {count: 1, id: "minecraft:bamboo"}, buy {count: 5, id: "minecraft:emerald"}, xp: 2}]}` = **2 项** | 通过 | 复用 #18。参与抽样的池长 = 22 见 #17 |
| B-02 | R1 | 同一个植物学家补货之后 | **仍是那 2 项**、同样的物品、同样的基准价（**负向判据**：不得换内容） | 两次读数（间隔 135 秒 > 2 × 1200 刻）逐字相同：`poisonous_potato` buy 5 / `bamboo` buy 5 —— 物品、数量、基准价三项**未变** | 通过 | 附录 A-1。**没有发生的证据**：补货走 `Villager#restock()` → `MerchantOffer#resetUses()`，不重调 `getOffer`（`BotanistEpicTrade.java:20-23`），故不存在「换内容」的代码路径 |
| B-03 | R1 | 同一个植物学家升到大师级 | 出现 **1 项**史诗报价，且整个存档里同一个 | `level: 5` 的植物学家 `Offers = {Recipes: [{maxUses: 1, sell {count: 1, id: "farmstorm:lucky_berry"}, buy {count: 61, id: "minecraft:emerald"}, xp: 2}]}` = **1 项**；跨 2 个补货窗口重读**逐字相同** | 通过 | 复用 #19 + 附录 A-1 |
| B-04 | R1 | 想看到 22 项里的第 3 项 | 换一个**新**植物学家（各自抽签） | `p` = {poisonous_potato, bamboo}；`q` = {carrot, beetroot} —— 四件物品两两不同 | 通过 | 复用 #18（两个独立村民） |
| B-05 | R1 | 一个满级植物学家的报价总数 | **3 项**（普通 2 + 史诗 1） | **2 + 1 = 3**。两个分量各自实测：1 级植物学家 `Offers.Recipes` 长度 = **2**（`p` / `q` 各测一次）；5 级植物学家的 5 级列表长度 = **1**（史诗）。⇒ **3** | 通过（算术读数） | 分量读数见 #18 / #19。**登记**：单个村民同时持有 3 项需玩家交易涨等级（`increaseMerchantCareer`），无人上线构造不出 ⇒ 按 47 号标注的「算术」口径合成，**不编造一次成型的读数** |
| B-06 | R1 | 玩家把核放在**自己基地**、且核**有主** | 村民**照样认领**、跑去上班（**负向判据**：不得加「必须无主 / 必须己方」的判据） | 有主核 `ownerUuid: [I; 1, 2, 3, 4], ownerName: "W07Tester"` **被认领**：某村民 `job_site = {pos: [I; 76, -59, 76]}` | 通过 | 复用 #15。**没有发生的证据**：`FarmstormPoiTypes` / `FarmstormVillagerProfessions` 都是本版新增类，全文没有任何 `ownerUuid` 读取；认领路径由原版 `AcquirePoi` + `maxTickets` 决定 |
| B-07 | R1 | 第二个村民想认领同一个核 | **认领失败**（一核一村民），需要第二个核 | 单核场景：`p` = `farmstorm:botanist` + `job_site [I;70,-59,36]`；`q` = `{profession: "minecraft:none"}` 且 `job_site` → `Found no elements matching Brain.memories."minecraft:job_site"` | 通过 | 复用 #14(a)（清场重跑后的干净读数） |
| B-08 | R1 | 4 个核都放在同一处 | 最多 **4 个**植物学家、最多 **8 项**普通交易（`4 × 2`） | **4 × 2 = 8**。单位值实测：1 个核 → 1 个植物学家（B-07）；1 个植物学家 → **2** 项普通交易（B-01）。⇒ **8** | 通过（算术读数） | 单位值见 #14 / #18。核与村民的 1:1 由 `maxTickets = 1`（`FarmstormPoiTypes:34`）承担 |
| B-09 | R1 | 玩家想从植物学家买**种子** | 买不到：22 项里没有种子 | 22 条清单里 `seeds` 作为子串命中 **0**；22 条全部是**作物本体**（`Items.WHEAT` / `CARROT` / `POTATO` / …）+ `COMPRESSED_PUFFED_WHEAT` + `PLAIN_CHIVES` | 通过 | `BotanistTrade.java:61-82` 逐行原文 |
| B-10 | R1 | 玩家买到 `compressed_puffed_wheat` | 正常：既非原版农作物、也非平凡农作物，仍按裁定计入 22 项 | 22 条清单第 **19** 行 = `new BotanistTrade(FarmstormItems.COMPRESSED_PUFFED_WHEAT.get()), // 19 二级合成物（D-56，无转换配方）` | 通过 | `BotanistTrade.java:79`；总行数仍 22（未被剔成 21） |
| B-11 | R1 | 玩家想靠转换器换原版货 | 照旧可用（20 条双向 / 2 条单向），与植物学家**并存** | `git diff 214b2bf..HEAD -- '*onverter*'` = **0 行**；`VillagerTradeEvents` 的 v0.2.2 diff 里**没有**农民那三行（`get(1).add(TierTrade.COMMON)` / `get(3).add(TierTrade.RARE)` / `get(5).add(TierTrade.EPIC)`），只**新增**植物学家分支 | 通过 | 零 diff + 农民三行不在 diff 里 ⇒ 两套并存、谁也没被删 |
| B-12 | R2 | 玩家想要平凡韭菜 | 只有三条入口：创造栏 / `/give` / 植物学家交易（**负向判据**：不得加合成配方） | 遍历配方产物目录，含 `plain_chives` 的 json = **0 个**；创造栏清单 `FarmstormItems:149 items.add(PLAIN_CHIVES);`；交易清单第 **20** 行 = `new BotanistTrade(FarmstormItems.PLAIN_CHIVES.get())` | 通过 | **没有发生的证据**：脚本输出 `recipe_json_with_plain_chives=[]` |
| B-13 | R2 | 玩家试着种平凡韭菜 / 当种子用 | **不行**：不是作物、没方块形态、没进 `SEEDS` / `MODIFIED_CROPS`（**负向判据**：不得建 `CropProfile`） | `seeds.json` 无 `plain_chives`、`modified_crops.json` 无 `plain_chives`；启动日志 `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK` ⇒ `CropProfile.ALL.size()` 仍是 **22**，没有变 23 | 通过 | **没有发生的证据**：作物数没涨。`PLAIN_CHIVES` 用 `Item::new` 直接注册，不是 `cropItem` 路径（`FarmstormItems:131`） |
| B-14 | R2 | 玩家把平凡韭菜喂给村民 / 拿去合成 | **不行**：不做任何合成、不参与村民收购（**负向判据**：不得加第二条获取路径） | `plain_chives` 在 `VillagerTradeEvents` 命中 **False**、在 `TierTrade` 命中 **False**；配方产物命中 0 | 通过 | **没有发生的证据**：农民三行逐字未动 ⇒ 收购池没变；植物学家那 22 项全部产生在 **sell** 侧，不收任何东西 |
| B-15 | R3 | 玩家看标签成员 | 只有 `farmstorm:cultivator` 一个 ⇒ 与「培养器 24」等价 | `processing_block.json` = `{"values":["farmstorm:cultivator"]}` | 通过 | 复用 #6 的产物原文 |
| B-16 | R3 | 玩家想把**转换器**绑到核上 | **绑不上**：它不在标签里（**负向判据**：不得顺手把转换器加进标签） | 运行期：一个**空**的有主核 `60,-59,60` + 旁边 2 格的转换器 `58,-59,60`，跨 6 个补绑周期（30 秒）后 `data get block 60 -59 60 Cultivators` = `[]`（两次读数都是空） | 通过 | **没有发生的证据**：`Cultivators` 始终是空数组。代码层 `rebindNearby` 只 `collect(..., CultivatorBlockEntity.class)`，转换器根本不是候选。首版落点误选了已满 24/24 的核（`setblock` 同块返回 `Could not set the block`），因「满员连枚举都不做」不构成判据 ⇒ 换空核重做 |
| B-17 | R3 | 0.2.3 把转换器加进标签之后 | 它**占同一个 24**，不改 config、不改判据 | **说明性边界，本版不做实现。** 本版 config 里 `outpost_core.cultivatorCapacity = 24` 是**唯一**名额键（`FarmstormConfig:956`）；`storageSlots = 54` 与名额无关（`OutpostCoreBlockEntity:349` 与 `OutpostCoreMenu:20` 都写明「与 54 不是一个数」） | 通过（说明性，不执行） | 47 号本条原文即「说明性」；F-284 易漏点④明令不把它当本版任务做 |
| B-18 | R3 | 一个核已绑 24 个 | 第 **25** 个绑不上；已绑的 24 条一条不动 | `Cultivators` = **24** 条 long；解码后 = 5×5 网格 25 点里**只少了 `(34,-59,34)`**（其 `distSqr` = 200 > 已绑最大 181）；跨一个补绑周期再读：24 个 long **逐位同序相同** | 通过 | 复用 #25 + 附录 A-2。这是名额的**唯一行为读点** |
| B-19 | R4 | 悬停 54 格里的**空格子** | **没有** tooltip（不是空框） | **未执行**：需真开界面做鼠标悬停。静态：`OutpostCoreScreen:51 this.renderTooltip(graphics, mouseX, mouseY);` 走原版 `AbstractContainerScreen#renderTooltip`，本类没有覆写它 | 【未验证】 | 铁律第 8 条 + D-4（不得用源码冒充悬停读数） |
| B-20 | R4 | tooltip 超出窗口右侧 | 原版翻到左侧；本版不接管定位 | **未执行**：同上需界面。静态：`OutpostCoreScreen` 全文 53 行只有 `renderBg` / `render` 两个覆写，**没有**覆写 `renderTooltip` 的定位；0.2.1 的四条否决保留 | 【未验证】 | 铁律第 8 条 |
| B-21 | R4 | 非己方玩家右键核心 | **打不开**界面 ⇒ 与 tooltip 无关 | 静态：`OutpostCoreBlock:134-135` `if (!(level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core) \|\| !core.isAlly(player)) { return InteractionResult.PASS; }` —— 非己方原样放行、不开菜单。**行为面需第二个客户端，未执行** | 【未验证】+ 静态证据 | `OutpostCoreBlock.java:129-142` 原文。D-166 保留：本条不改「谁能开」。**静态面已证明非己方拿到的是 PASS（不开菜单）**，但「真的打不开」需第二个客户端坐实 |
| B-22 | R4 | 两次 blit 之后 | 上段 125 + 下段 96 = **221**，比 `imageHeight`（222）少 1 px；原版如此，照抄 | `OutpostCoreScreen:41 int upper = OutpostCoreMenu.ROWS * 18 + 17;`（6×18+17 = **125**）；`:45` 第二段高 **96**；`:25 IMAGE_HEIGHT = 222`。125 + 96 = **221** = 222 − 1 | 通过 | 源码原文。**反向**：没有第三次 blit（`graphics.blit(` 全文恰 2 处，见 #23） |
| B-23 | R4 | 有人想「顺手」改槽位 y 来修那 1 px | **禁止**：槽位 `139 / 157 / 175 / 197` 不动，错位是 blit 次数造成的 | `OutpostCoreMenu:44 private static final int INVENTORY_Y = 139;`、`:46 private static final int HOTBAR_Y = 197;`；3 行主背包由 `:80 INVENTORY_Y + row * SLOT_SIZE` 派生 ⇒ **139 / 157 / 175**，快捷栏 **197**。该类在 v0.2.2 的**唯一**改动是一句 javadoc（`16 → 24`），槽位代码零 diff | 通过 | `git diff 214b2bf..HEAD -- …/OutpostCoreMenu.java` 只有一个 hunk（注释行） |
| B-24 | R5 | 老存档玩家已有 **5** 个核 | **不删核**；只是**放不下第 6 个**（上限只在放置时判） | **未执行**：`maxPerPlayer` 的判定在放置交互里按玩家 UUID 计数（`OutpostCoreIndex`），无人上线构造不出「老存档 5 个 + 放第 6 个」。静态：`OutpostCoreBlock:81` 记明超限时 `return InteractionResult.FAIL`，「物品不消耗、无掉落、也不做 FIFO 挤掉」——**没有任何删核路径** | 【未验证】+ 静态证据 | 铁律第 8 条；`OutpostCoreBlock.java:81` 原文 |
| B-25 | R5 | 老存档玩家看 config | 仍是 `maxPerPlayer = 2`、`cultivatorCapacity = 16` ⇒ 误报「上限没改成 4」 | 把升级前的 toml（2 / 16）拷进 `run-w07/config/` 后重启：仍是 `:753 maxPerPlayer = 2` / `:765 cultivatorCapacity = 16`，键数仍 **176** | 通过 | 复用 #9（与 #10 成对） |
| B-26 | R5 | 语言键漏加 | 玩家看到 raw key（**负向判据**：不得漏） | `item.farmstorm.plain_chives` 与 `entity.minecraft.villager.farmstorm.botanist` 在 zh / en 两份都**存在**；`zh = 340 / en = 340 / zh-only = 0 / en-only = 0` | 通过（反向后验） | **没有发生的证据**：两份键集完全一致、零单边键；服务端把村民显示成 `Botanist`（职业名键已解析） |
| B-27 | R5 | `verify_project.py` 未同步 | `items` 期望仍是 **33** ⇒ 加了一个物品却全绿（**负向判据**：不得假绿） | `EXPECTED_COUNTS["items"]` = **34**（`:381`）、`EXPECTED["items"]` 含 `plain_chives`（`:342`）；工具实跑 `items: 34 个 ID 齐全` / `已核 registry 对象: 98 / 98` | 通过（反向后验） | **没有发生的证据**：工具与源码同提交 `8407d74` 一起改；`98 / 98` 不是 `97 / 97` |

## 分组汇总（四组，逐组一条）

| 组 | 覆盖 | 通过 | 【未验证】 | 失败 | 汇总 |
|---|---|---|---|---|---|
| **R1 · 植物学家职业** | B-01 ~ B-11（11 条） | 11 | 0 | 0 | 「2 项可见 / 22 项池 / 一核一村民 / 有主核照领 / 补货不重抽」全部成立；B-05 = **3**、B-08 = **8** 为算术读数（分量各实测） |
| **R2 · 平凡农作物** | B-12 ~ B-14（3 条） | 3 | 0 | 0 | 三条负向判据全绿：没有配方、没有 `CropProfile`（作物数仍 22）、不参与收购 |
| **R3 · 方块标签与名额** | B-15 ~ B-18（4 条） | 4 | 0 | 0 | 标签只 1 个成员；转换器**绑不上**（运行期 `Cultivators = []`）；第 25 个绑不上且已绑 24 条逐位不动 |
| **R4 / R5 · 界面两个 bug 与计数面** | B-19 ~ B-27（9 条） | 5 | 4 | 0 | B-19 / B-20 只有开界面能判；B-21（非己方打不开）/ B-24（放不下第 6 个）的行为面需第二个客户端或在线玩家 ⇒ 这四条标【未验证】，各附静态证据。B-22 / B-23 / B-25 / B-26 / B-27 通过 |
| **合计** | **27 条** | **23** | **4** | **0** | 23 + 4 + 0 = 27 |

## 偏离与登记

1. **B-05 / B-08 是算术读数**（47 号自己标注「【本文补充：算术】」，F-284 断言 3 也要求「写数不写形容词」）。两个数都写出来了（**3** 与 **8**），但**分量是各自实测、不是一次成型**：B-05 的「普通 2 + 史诗 1」需要玩家交易把同一个村民练到 5 级，无人在线做不到 ⇒ 按「1 级实测 2 + 5 级实测 1」合成；B-08 的「4 × 2」按「1 核 = 1 植物学家、1 植物学家 = 2 项」两个单位值相乘。**没有编造一次成型的读数。**
2. **B-16 首版落点无效，已重做并登记**：第一次我把转换器放在一个**已满 24/24** 的核旁，而 `rebindNearby` 满员时「连枚举都不做」⇒ 空 `Cultivators` 什么也证明不了。换空核重做后读数仍为 `[]`，这一次才构成判据。
3. **B-19 / B-20 / B-21 / B-24 标【未验证】**：四条都卡在「没有第二个客户端 / 没有在线玩家」。给的都是**静态事实**（本版没覆写 `renderTooltip`；非己方 `return InteractionResult.PASS`；无任何删核路径），**不是行为读数**。判定口径统一为：只有真的读到判据要求的可观测终态才算「通过」；判据本身就是代码形态的（B-22 / B-23）例外。
4. **B-24 的静态面已证「没有任何删核路径」**——这正是本条真正的风险点（升级时把超限的核删掉），该风险已排除；未兑现的只是「第 6 个放不下」这一半读数。
5. **B-17 不执行**：47 号原文即标为说明性（0.2.3 的事），F-284 易漏点④明令不把它当本版任务做。
