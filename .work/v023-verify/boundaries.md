# F-310 · 0.2.3 边界复跑记录（53 号 §9 的 31 条，B-28 ~ B-58）

> 执行者：边界复跑子代理（F-310）。窗口已关：F-286 ~ F-308 全部落地；F-309 的 32 条验收记录在 `acceptance.md`（本记录不复用它的结论，只复用同一批场景：核 / 转换器 / 培养器三件摆设）。
> 记录形态：每条给「边界一句话 / 怎么试（谁 · 在哪 · 做了什么）/ 读数或没有发生的证据 / 结论 / 证据」。
> **不改码**：本轮只新增 `.work/v023-verify/` 下的文件（探针脚本、日志、本记录）；`src/`、`tools/`、`docs/` 零改动（见 §6 的 mtime 取证）。本轮**不跑任何 git 命令**，Mixin 仍为 0（检查器 §4「无 Mixin …」）。

---

## 0. 结论分布

| 统计项 | 条数 | 编号 |
|---|---|---|
| 成立 | **31** | B-28 ~ B-58（逐条见 §2） |
| 不成立 | **0** | — |
| 未验证（整条） | **0** | — |
| 带「未验证子项」 | **2** | B-54（1.5 s 后 15 块里 1 块被邻块更新挤掉，见 §5 第 6 条）、B-57（「其它进度一条不动」无 git 取证，改用 16 个 advancement 的 mtime，见 §5 第 7 条） |

**分组标注**（53 号 §9 末段的七组，一条不漏）：

| 组 | 编号 | 条数 |
|---|---|---|
| R1（转换器） | B-28 B-29 B-30 B-31 B-32 B-33 B-34 B-35 B-36 B-37 | 10 |
| R3（培养器） | B-38 B-39 B-40 B-41 B-42 B-43 B-44 B-45 B-46 | 9 |
| 平凡作物（R2 与 R3 的交叉） | B-47 B-48 B-49 B-50 B-51 | 5 |
| R2（15 个改良作物方块） | B-52 B-53 B-54 | 3 |
| R4（炮弹） | B-55 B-56 | 2 |
| R5（文案） | B-57 | 1 |
| 计数面 | B-58 | 1 |
| 合计 | B-28 ~ B-58 | **31** |

10 + 9 + 5 + 3 + 2 + 1 + 1 = 31，连续无跳号。

---

## 1. 环境、工具链与起服台账

| 项 | 值 |
|---|---|
| 运行目录 | `run-v023`（`-PfsRunDir=run-v023` + `FS_RUN_LOCK=run-v023` 隔离车道），日志 `run-v023/logs/latest.log` |
| 版本 | NeoForge 21.1.213 / MC 1.21.1 / Java 21（`.tools/jdk21`） |
| RCON | `127.0.0.1:25585`，`rcon.password=fsverify` |
| 玩家 | mineflayer 4.39 离线客户端 `FSEval`（`auth=offline`、`version=1.21.1`），右键一律用 `block_place` 真包下发 |
| 读数通道 | 服务端读数一律走 RCON：`/data get block`、`/execute if block`、`/execute if entity`、`/time query gametime`、`/datapack list` |
| 建造位置 | 核 `8 -60 8`、培养器 `10 -60 8`、转换器 `12 -60 8`（另一台 `14` / `16` / `20` 按需）、耕地 `14 -60 4`、15 块作物横排在 `0..14 -60 12`（下方 `0..14 -61 12` 铺耕地）、炮弹 `12.5 -45 8.5` |
| forceload | 每次起服都 `/forceload add -32 -32 48 32`（30 ~ 36 个区块），保证无玩家处方块实体与实体照常 tick |

跑过的命令（原文）：

```text
1) 构建     cmd /c D:\project_farm\.tools\gw.cmd build                    -> BUILD SUCCESSFUL in 9s（5 up-to-date），BUILD-EXIT=0
2) 检查器   python -X utf8 tools/verify_project.py                        -> VERIFY-EXIT=0；175/175、92/92、321、FAIL 0 项 WARN 1 项（§4）
3) 静态面   python -X utf8 .work/v023-verify/f310-static.py（+ -c）       -> logs/f310-static.log / logs/f310-static-c.log
4) 起服     $env:FS_RUN_LOCK=run-v023; gw.cmd -PfsRunDir=run-v023 runServer -> 每次都以 RCON 的 /stop 优雅收尾（stdout 末行 BUILD SUCCESSFUL）
5) 探针     node .work/v023-verify/f310-probe1.js … f310-probe8-read.js   -> logs/f310-probe1.log …（逐条记录里点名）
```

**起服台账（10 次，全部 `/stop` 优雅收尾，无一被 kill）**：

| # | 时刻 | 目的 | 日志 |
|---|---|---|---|
| ① | 03:38 | R1 转换器 + 培养器枚举 + 炮弹 + 第一次装弹 | `logs/f310-session1.log` |
| ② | 03:48 | 掉落 / 核满 / 产出 / B-35 / B-46 临时配方 / B-52 / B-54 | `logs/f310-session2.stdout.log` + `logs/f310-probe2.log` |
| ③ | 03:56 | 补跑（B-56 首读 + 后续脚本框架） | `logs/f310-session3.stdout.log` + `logs/f310-probe3.log` |
| ④ ~ ⑩ | 04:02 ~ 04:14 | 见 §5 第 1 条：探针自身三处工具性 bug 各修一轮；⑥ 是 `cultivator.yield = 3` 的 B-45；⑦⑧ 与 ⑨⑩ 是 B-56 的装弹 / 读档两轮 | `logs/f310-session{4,5,6,7,8,9,10}.stdout.log` |

> 起服次数多于 57 号 §64 建议的 2 ~ 3 次，原因全部是**探针工具 bug**（不是「每条纹一次」）：`/data merge … {Items:[]}` 在 `ContainerHelper.loadAllItems` 下清不空、单条 RCON 命令超过约 1000 字符被截断、`place()` 未定义、B-56 读数窗口错过。逐条见 §5 第 1 条。产出判定读数的 5 次起服是 ①②⑥⑦⑩。

---

## 2. 31 条逐条记录

### 2.1 R1（转换器）B-28 ~ B-37

| 编号 | 边界（一句话） | 怎么试（谁 · 在哪 · 做了什么） | 读数 / 没有发生的证据 | 结论 | 证据 |
|---|---|---|---|---|---|
| **B-28** | 一台转换器算「可用」＝已绑定 ∧ 核侧列表认 ∧ 3×3 区块内，三条同时成立 | FSEval 在 `12 -60 8` 放下转换器（核 `8 -60 8` 在范围内）→ 手持小麦真右键 → 再只清空核侧 `Cultivators` / `CultivatorKinds`（机器侧 `Core` 缓存保留）→ 立刻右键 → 最后把机器侧 `Core` 键删掉，等 100 刻补绑扫描 → 再右键 | ① 基线 `ACCEPT=YES`、`Progress` 起涨、`working=true`；② 只清核侧后 `heldTag="Found no elements matching Held"`、`ACCEPT=NO`，而读数时机器侧仍是 `Core: 2199023292356L`（缓存里有、核侧没有 ⇒ 不算），距清空只差 **37 刻**（< 100 刻扫描周期）；③ 删掉 `Core` 键后 0.5 s 内补绑把它收回 `Cultivators`，再右键 `ACCEPT=YES` | **成立** | `logs/f310-probe1.log`（B-28-BASELINE / B-28-NEG / B-28-SWEEP）；代码 `CropConverterBlockEntity.java:102-115`（isUsable 四段） |
| **B-29** | 3×3 区块＝以核所在区块为中心、切比雪夫距离 ≤ 1 的 9 个区块 | FSEval 在 `-8 -60 8`（区块 -1，距核区块 0 的距离 1）与 `40 -60 8`（区块 2，距离 2）各放一台转换器，各手持小麦右键 | 边界内：`CoreKey=2199023292356L`（已绑定）、`ACCEPT=YES`；第 2 圈：`CoreKey=Found no elements matching Core`（未绑定）、`ACCEPT=NO`、核侧列表里没有它。对照：核到 `40` 的方块水平距离只有 **32 < 48**，若按方块半径算就会绑定 ⇒ 实测未绑定，说明判的是区块距离 | **成立** | `logs/f310-probe1.log`（B-29-EDGE / B-29-FAR / B-29-NOTE）；`OutpostCoreBlockEntity.java:522-528`（`inRange`） |
| **B-30** | 往槽里放东西＝21 条配方之一的输入 ∧ 槽空 ∧ 机器可用 ∧ 右键只放 1 个 | FSEval 依次：槽空时拿小麦右键（正例）；槽满时再拿小麦右键；槽空时拿木棍右键（非配方输入）；槽空时拿火把花右键（第 21 条之一） | 小麦 `ACCEPT=YES(minecraft:wheat)`、手持消耗 1；槽满再点：`Held` 仍是那 1 个、手持 `count: 2` **原样不动**；木棍 `ACCEPT=NO`；火把花 `ACCEPT=YES(minecraft:torchflower)`（转化器收全 21 条，与培养器的 17 条不是同一口径） | **成立** | `logs/f310-probe1.log`（B-28-BASELINE / B-30-FULL / B-30-NONINPUT / B-30-EPICINPUT）；`CropConverterBlock.java:118-133`、`CropConverterBlockEntity.java:129-137` |
| **B-31** | 一次只取 1 个，其余留在手上 | FSEval 手持 **3 个小麦**，对同一台转换器右键一次 | 手持 `{count: 3}` → `{count: 2}`；槽内 `{count: 1, id: "minecraft:wheat"}`（只取 1 个，没有清空整叠） | **成立** | `logs/f310-probe1.log`（B-31/B-30 行）；`heldStack.split(1)` 于 `CropConverterBlock.java:131` |
| **B-32** | **取回输入：不能**（空手右键无反应，只能拆方块；漏斗抽出不算） | FSEval 放入 1 小麦后：① 空手连续右键 **3 次**；② 手持胡萝卜右键（想「换回来」）；③ 全程记录世界内 item 实体数 | ① 3 次空手右键后 `heldBefore` 与 `heldAfter3EmptyClicks` **完全相同**（都是 `{count: 1, id: "minecraft:wheat"}`）、`itemEntitiesAfter=Test failed`（**没有任何物品被丢出来**）、手上 `Found no elements matching SelectedItem`；② 胡萝卜 `count: 1` **未消耗**、`Held` 仍是小麦 | **成立（负向）** | `logs/f310-probe1.log`（B-32 / B-32b）；`CropConverterBlock.java:114-133`（空手走 `useWithoutItem` 四道闸，槽满 ⇒ `PASS`） |
| **B-33** | 拆掉工作中的转换器：槽里的输入物掉落、进度归零且不掉落产物 | ① FSEval 放小麦 + `Progress=300` → `/setblock … air`（人站 6 格外）→ 数掉落；② 真用石镐挖（人站 1.2 格内）→ 数掉落 + 读玩家背包；③ 重放方块读 `Progress` | ① 掉落 **恰好 1 个**：`Wheat … Item: {count: 1, id: "minecraft:wheat"}`，世界里没有 `puffed_wheat`；② 挖的那次掉落 0 个实体，但玩家背包多出 `{count: 1, Slot: 1b, id: "minecraft:carrot"}`（输入物被身旁玩家当场捡走），地面上留的是方块本体 `Crop Converter`；③ 重放后 `{Progress: 0, …}` — 进度归零 | **成立** | `logs/f310-probe4.log`（B-33a / B-33c）、`logs/f310-probe5.log`（B-33-dig / B-33-dig2 / BOT-INVENTORY）；`CropConverterBlock.java:144-151`、`CropConverterBlockEntity.java:249-260` |
| **B-34** | 核满时到点：槽与进度都保留、`working` 落回 false、核腾出空间的下一 tick 自动投递 | FSEval 把核 54 格塞满 64 小麦 → 转换器 `Held=wheat / Progress=600` → 等 1.5 s 读 → 再把第 53 格改成 air（其余 53 格仍是 64 小麦）→ 每 100 ms 采样 | 满核时 `Progress=600` 停住、`Held={count: 1, id: "minecraft:wheat"}` 仍在、`working=false` 断言 true、核里 `PRODUCT-BEFORE-FREE false`（产物没丢地上也没进核）；腾格后**第一次采样（0.1 s）就完成投递**：`done={"i":0,"g":62754}`、`Progress=0`、槽空、核第 53 格变 `{count: 1, Slot: 53b, id: "farmstorm:puffed_wheat"}` | **成立** | `logs/f310-probe5.log`（B-34 全段）；`CropConverterBlockEntity.java:148-187`（第 4 步只在 `progress == processTicks()` 结算） |
| **B-35** | 绑定失效（核被拆 / 核侧列表没它）：留原地、内容保留、不可使用 | 两台机器各留内容（转换器 `Held=wheat P=200`、培养器 `Held=carrot P=200`）→ `/setblock 8 -60 8 air` 拆掉核 → 0.8 s 与 2.8 s 各读一次 | 方块仍在：`convB-block=true cult-block=true`；内容仍在（两件 `Held` 都在）；进度**冻结**：`P=234 -> 234`（2 s 内零增长）；右键 `ACCEPT=NO` 且手持不消耗；机器侧 `Core` 键仍指向那个坐标（没有自动改绑最近的核——最近的核已不存在） | **成立** | `logs/f310-probe2.log`（B-35 BEFORE / BREAK-CORE / AFTER / PROGRESS-FROZEN / INSERT-AFTER-CORE-GONE）；`CropConverterBlockEntity.java:161-165`（第 2 步「不清槽不清进度」） |
| **B-36** | 工作时右键：无反应（`PASS`） | FSEval 放入小麦（`Progress` 已涨）→ 手持**胡萝卜**右键一次 → 空手再右键一次，两次之间读到 `Progress` 仍在涨 | 换输入：`Held` 仍是 `{count: 1, id: "minecraft:wheat"}`、胡萝卜 `count: 1` 未消耗、`Progress 19 -> 43`（继续计时）；空手：`Held` 不变、`Progress 43 -> 64`、`itementities=Test failed`（**没有中途取出、也没有中途换输入**） | **成立** | `logs/f310-probe1.log`（B-36-SWAP / B-36-EMPTYHAND） |
| **B-37** | **漏斗 / 投掷器：不能塞、不能抽**（不实现 `Container`） | FSEval 在转换器 `14 -60 8` 上下各放一个漏斗（下：`facing=down` 拉取；上：`facing=down` 推入 + 预先塞 16 小麦），跑 **80 刻**（每 8 刻一次 ⇒ 10 次机会）后读三处 | 下方漏斗 `Items = []`（抽不到东西）；上方漏斗 `Items = [{count: 16, …}]`（**16 个一个没少**，推不进去）；转换器 `Held` 仍是那 1 小麦、`Progress 12 -> 94` 照常计时 | **成立（负向）** | `logs/f310-probe1.log`（B-37 SETUP / AFTER80T / HOPPER-BELOW / HOPPER-ABOVE）；`CropConverterBlockEntity.java:34-37`（「刻意不实现 Container」） |

### 2.2 R3（培养器）B-38 ~ B-46

| 编号 | 边界（一句话） | 怎么试（谁 · 在哪 · 做了什么） | 读数 / 没有发生的证据 | 结论 | 证据 |
|---|---|---|---|---|---|
| **B-38** | 培养器收的是「21 条配方输入里、输出档案非史诗的 **17** 个」 | FSEval 把 21 条 `farmstorm:converting` 的输入**逐条**真右键喂进同一台培养器（每条之前清槽），逐条记 `Held` | `B-38 SUMMARY accepted=17 rejected=4`；21 行逐条 `VERDICT=MATCH`（收：bamboo/carrot/brown_mushroom/cactus/plain_chives/melon/melon_slice/crimson_fungus/warped_fungus/beetroot/wheat/pumpkin/red_mushroom/poisonous_potato/potato/cocoa_beans/sugar_cane；拒：pitcher_plant/torchflower/sweet_berries/golden_apple） | **成立** | `logs/f310-probe1.log`（B-38 逐行 + SUMMARY）；`CultivatorBlockEntity.java:143-185` |
| **B-39** | 4 条史诗输入：火把花、瓶子草荚、甜浆果、金苹果 | 同上，四条各单独再试一次（读完立刻读手持与槽） | 四条全 `ACCEPT=NO`：`heldTag="Found no elements matching Held"`、手持 `count: 1` 未消耗（torchflower / pitcher_plant / sweet_berries / golden_apple 各一行） | **成立** | `logs/f310-probe1.log`（B-39 四行） |
| **B-40** | 培养器收平凡韭菜（算第 17 个投入物） | 同 B-38 的枚举里 `farmstorm:plain_chives` 那一条 | `ACCEPT=YES(farmstorm:plain_chives)`、`Progress` 起涨；它进的是「输出档案 = friend_chives（RARE，非史诗）」这一支，不是「没有 CropProfile 就不收」 | **成立** | `logs/f310-probe1.log`（B-38 plain_chives 行）；`CultivatorBlockEntity.java:160-185` |
| **B-41** | **培养器收改良作物：不收**（「它也是农作物」不算理由） | FSEval 依次拿 5 件改良作物右键：`puffed_wheat`、`blast_carrot`、`friend_chives`、`lucky_berry`、`ubi_server` | 五条全 `ACCEPT=NO`：`heldTag="Found no elements matching Held"`，五件手持 `count: 1` **一件都没被吃掉** | **成立（负向）** | `logs/f310-probe1.log`（B-41 五行）；改良作物是转换的**输出**，21 条配方的 input 全是原版物品 + plain_chives |
| **B-42** | 有独立种子的 6 个投入物：2 个投入物 + 2 个对应种子 | FSEval 逐条右键 → `Progress` 置 599 跳过 600 刻等待 → 读核内产物（每条之前清空核） | `wheat => wheat×2 + wheat_seeds×2`；`beetroot => beetroot×2 + beetroot_seeds×2`；`melon_slice => melon_slice×2 + melon_seeds×2`；`melon => melon×2 + melon_seeds×2`；`pumpkin => pumpkin×2 + pumpkin_seeds×2`；`plain_chives => fs:plain_chives×2 + fs:plain_chives_seeds×2` — 六条全对 | **成立** | `logs/f310-probe5.log`（B-42 六行）；`CultivatorBlockEntity.java:270-276` |
| **B-43** | 自身即种子的 11 个投入物：4 个同一物品 | 同 B-42，跑剩下 11 条 | 11 条全是「4×同一物品」：bamboo 4 / carrot 4 / brown_mushroom 4 / cactus 4 / crimson_fungus 4 / warped_fungus 4 / red_mushroom 4 / poisonous_potato 4 / potato 4 / cocoa_beans 4 / sugar_cane 4；**没有**出现「2 + 2 两种物品」 | **成立** | `logs/f310-probe5.log`（B-43 十一行）；`CultivatorBlockEntity.java:278-288` |
| **B-44** | 毒马铃薯 / 甘蔗 / 可可豆的「对应种子」＝它自己，给 4 个 | 从 B-43 的三行单独摘出核对 | `poisonous_potato => [{count: 4}]`（不是 `minecraft:potato`）；`sugar_cane => [{count: 4}]`；`cocoa_beans => [{count: 4}]` — 三行都没有手写映射到马铃薯或别的物品 | **成立** | `logs/f310-probe5.log`（B-43 三行） |
| **B-45** | `cultivator.yield` 改值：两组同乘同一个值 | 把 `run-v023/config/farmstorm-server.toml` 的 `yield = 2` 改成 **3**（备份 `logs/f310-yield2-backup.toml`），起服⑥后同一台培养器喂三种投入物 | `carrot => carrot×6`（3 + 3 同一物品）；`wheat => wheat×3 + wheat_seeds×3`；`plain_chives => fs:plain_chives×3 + fs:plain_chives_seeds×3` —— 两组**同乘 3**，不是「只乘一组」也不是「拆成两个键」；测完把 toml 还原成 `yield = 2` | **成立** | `logs/f310-b45-inline.txt`（会话⑥控制台原文）；`CultivatorBlockEntity.java:261-267`（一个 `amount` 供两组） |
| **B-46** | 新增一条转换配方 ⇒ 培养器自动多收一个投入物（17 → 18） | 起服②前把临时数据包 `run-v023/world/datapacks/f310-temp`（`data/farmstorm/recipe/f310_apple.json`：`apple → puffed_wheat`，非史诗输出）放进世界；FSEval 拿**苹果**右键培养器 → `/datapack disable "file/f310-temp"` → 再拿苹果右键 | 启用时 `B-46-WITH-TEMP minecraft:apple => ACCEPT=YES`（第 18 个投入物，苹果本身没有任何 CropProfile）；停用后 `B-46-WITHOUT-TEMP minecraft:apple => ACCEPT=NO`（回到 17）；`/datapack list` 前后各一条原文；测完**删掉**临时数据包（`Test-Path` = False） | **成立** | `logs/f310-probe2.log`（B-46 全段）；`CultivatorBlockEntity.java:143-149`（判据只有一个来源，没有白名单） |

### 2.3 平凡作物（R2 与 R3 的交叉）B-47 ~ B-51

| 编号 | 边界（一句话） | 怎么试（谁 · 在哪 · 做了什么） | 读数 / 没有发生的证据 | 结论 | 证据 |
|---|---|---|---|---|---|
| **B-47** | 平凡韭菜**不进档案**，`CropProfile` 仍 22 条 | 读 `CropProfile.java` 的 `ALL` 列表 + 数成员；再对整份文件搜 `plain`；并与启动自检行对照 | `ALL` 成员数 = **22**（PUFFED_WHEAT … VIBRATION_BEAN 逐条列出，没有 plain 任何一条）；整份文件里 `plain` 出现 **0** 次；启动自检 `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3`、`自检 作物品质 普通 11 / 稀有 7 / 史诗 4` | **成立** | `logs/f310-static.log`（B-47 段）、`logs/f310-session1.log`（自检行） |
| **B-48** | 平凡韭菜种子**能种**：走原版放置流程（复用 `SeedItem` 不算，那个类的 `useOn → PASS`） | FSEval 在 `14 -60 4` 放耕地、上方留空 → 手持 2 个种子右键**耕地本身**（`block_place` 真包） | 手持 `{count: 2}` → `{count: 1}`（消耗 1）；`14 -59 4` 变成 `farmstorm:plain_chives_crop[age=0]`（`age0=true`，1.5 s 后仍在）；耕地本身仍是 `minecraft:farmland`；类为 `PlainSeedItem extends ItemNameBlockItem`（原版 `BlockItem#place`），`SeedItem.java` 已在 F-299 删除 | **成立** | `logs/f310-probe4.log`（B-48 全段）；`PlainSeedItem.java:16-19` |
| **B-49** | 平凡韭菜**不进抽奖池、不进标签** | 读 `LUCKY_BERRY_POOL` 定义 + `#farmstorm:modified_crops` 标签 + `#farmstorm:seeds` 标签；与自检的池大小对照 | 池的定义是 `ALL.stream().filter(tier() == COMMON)`（没有手写清单），自检原文 `自检 幸运浆果池 11 种（… 不含 lucky_berry / wild_apple / compressed_puffed_wheat）`；`plain_chives` 不在 `ALL` ⇒ 结构上进不了池；`#farmstorm:modified_crops` = 15 项且不含 plain_chives；`#farmstorm:seeds` = 恰 1 项 `farmstorm:plain_chives_seeds` | **成立** | `logs/f310-static.log`（B-49 段）、`logs/f310-session1.log`（池自检行） |
| **B-50** | 平凡韭菜方块贴图**复用朋友的韭**的 4 张 stage 图（新增美术资源不算） | 读 4 个方块模型 JSON 的 `textures`；再数 `textures/block/*.png` 并筛 `plain_chives*` | `plain_chives_stage0..3.json` 的 `crop` 依次是 `farmstorm:block/friend_chives_stage0` … `_stage3`；`textures/block/*.png` 共 **84** 张，其中 `friend_chives_stage0..3.png` 4 张存在、`plain_chives*.png` **0** 张（没有新增美术资源，也没有走同名兜底分支） | **成立** | `logs/f310-static.log`（B-50 段） |
| **B-51** | 平凡韭菜的注册是**净新增**（blocks +1、items +1，不是拿它替换某个已删种子） | 读 `FarmstormItems` / `FarmstormBlocks` 的注册；比对 7 个已删种子 id；再用启动自检的注册计数与 53 号 §7 的增量对照 | `plain_chives_crop` 是新注册方块（blocks **27 → 28**，+1）；新增的**物品**是 2 个：`plain_chives` 与 `plain_chives_seeds`；7 个旧种子 id（`puffed_wheat_seeds` 等）全部**不在**注册里 ⇒ 不是替换；启动自检 `注册对象 92 个：items 28 / blocks 28` | **成立**（措辞偏差见 §5 第 2 条） | `logs/f310-static.log`（B-51 段）、`logs/f310-session1.log`（自检行） |

### 2.4 R2（15 个改良作物方块）B-52 ~ B-54

| 编号 | 边界（一句话） | 怎么试（谁 · 在哪 · 做了什么） | 读数 / 没有发生的证据 | 结论 | 证据 |
|---|---|---|---|---|---|
| **B-52** | **15 个改良作物方块：保留注册、无 `BlockItem`、正常玩法得不到**（补 BlockItem 不算；连方块删掉不算） | ① 静态：逐个数 15 个方块 id 在 `FarmstormItems` 里有没有对应物品；② FSEval 执行 `/give … farmstorm:puffed_wheat_crop 1` 与 `/give … farmstorm:lucky_berry_bush 1`；③ 拆一块看掉什么；④ `/setblock` 15 个方块确认方块本身还在 | ① 15 个方块 id **全部** `BlockItem=False`（含 puffed_wheat_crop / lucky_berry_bush / friend_chives_crop …）；② 两条 give 的原文：`Unknown item 'farmstorm:puffed_wheat_crop'`、`Unknown item 'farmstorm:lucky_berry_bush'`；③ 成熟作物拆掉掉的是**物品** `farmstorm:puffed_wheat`（`Puffed Wheat`），不是方块物品；④ 15 个方块 `setblock` 全部生效 | **成立（负向）** | `logs/f310-static.log`、`logs/f310-probe2.log`（两条 give）、`logs/f310-probe4.log`（B-53 / B-54） |
| **B-53** | 15 张方块战利品表：**删掉种子那一条、保留作物本体**（整表删掉不算；保留种子条目不算） | ① 静态解析 15 张 `loot_table/blocks/*.json`，列出每个 pool 的 entry 名；② 游戏内拆一块 `puffed_wheat_crop[age=3]`、再拆一块 `[age=0]`，读掉落实体 | ① 15 张表都是 2 个 pool，entry 分别是「作物本体 × 2 次」（age=3 支 + inverted 支），`SEED-ENTRIES=[]` —— 一张表里**没有任何 `*_seeds` 条目**；② 成熟：掉落实体 `Puffed Wheat … Item: {count: 1, id: "farmstorm:puffed_wheat"}`（`DROP-EXISTS count: 2`：本体 + 影子）；未成熟：同样只掉 `Puffed Wheat` | **成立** | `logs/f310-static.log`（B-53 段）、`logs/f310-probe4.log`（B-53 AGE3 / AGE0 两行） |
| **B-54** | 老存档里的 15 个方块**照旧存在、照旧可拆**（「保留但不可获得」不等于删除） | FSEval 在 `0..14 -60 12`（下方 `0..14 -61 12` 铺耕地）逐块 `/setblock` 15 个方块，逐块 `execute if block` 复核；随后用镐拆其中两块 | `B-54 PRESENT 15/15`（15 块全部存在且方块类型读数一致）；`DIG` 两块的原文都是 `-> air serverAir=true`（照旧可拆、掉落见 B-53）；1.5 s 后 `PRESENT-AFTER-1.5s 14/15`（1 块被邻块更新挤掉，见 §5 第 6 条） | **成立**（含 1 条子项说明） | `logs/f310-probe2.log`（B-54 15 行）、`logs/f310-probe4.log`（B-54 PRESENT / DIG） |

### 2.5 R4（炮弹）B-55 ~ B-56

| 编号 | 边界（一句话） | 怎么试（谁 · 在哪 · 做了什么） | 读数 | 结论 | 证据 |
|---|---|---|---|---|---|
| **B-55** | 300 刻的寿命**只作用于 `TurretBullet`**（瓜！的母弹与子弹不算；其它投掷物不算） | FSEval 用 RCON 在 `12.5 -45 8.5` 各召唤一次 `farmstorm:turret_bullet` / `farmstorm:melon_slice_bomb` / `farmstorm:melon_grenade`（都带 `NoGravity` 与零速度），每 200 ms 采一次「还在不在」直到消失；再静态核对哪个类读 `pumpkinTurretBulletLifetimeTicks` | `TURRET-GONE ticks=300`（召唤 `g=53171`、消失 `g=53471`）；`melon_slice_bomb-GONE ticks=104`；`melon_grenade-GONE ticks=104` —— 瓜系走的是**另一个键** `throwable.lifetimeTicks = 100`（toml 第 41 行），不是 300；静态：`pumpkinTurretBulletLifetimeTicks` 全树只有 `TurretBullet.java:83` 读 | **成立** | `logs/f310-probe2.log`（B-55 五行）；`run-v023/config/farmstorm-server.toml:41,171`；`TurretBullet.java:81-86` |
| **B-56** | 读档后的炮弹**重新计 300 刻**（保存「已飞刻数」不算） | 会话⑨：召唤炮弹 → 让它飞 **268 刻**（< 300，仍存活）→ `save-all flush` → `/stop`；会话⑩：起服后**立刻**连 RCON 读数，再每 250 ms 采到消失 | 读档后第 1 次读（`gt=67660`）炮弹**还在**；`gt=67845` 消失 ⇒ 读档后至少又活 **185 刻**。若「已飞刻数」被持久化，此刻数上限只有 300 − 268 = **32 刻**，实测 185 刻 ≫ 32 ⇒ 排除持久化，读档后从 0 重计（按阈值倒推：读档时约已活 115 刻，合计 ≈ 300）；同一区块原版 `arrow` / `item` 读档后仍在（`ARROW-AFTER Test passed`），说明不是存档问题 | **成立** | `logs/f310-b56-inline.txt`（会话⑦⑧⑨⑩ 原文）；`TurretBullet.java:171-184`（`addAdditionalSaveData` 只写 `Target`）、`:81-82` 注释「已飞刻数不持久化」 |

### 2.6 R5（文案）B-57

| 编号 | 边界（一句话） | 怎么试 | 读数 | 结论 | 证据 |
|---|---|---|---|---|---|
| **B-57** | `fang_dance` **只改描述**，中英各一处（改标题不算、改判据不算、动其它进度不算） | 读两份 `lang/*.json` 的四个键；读 `advancement/fang_dance.json` 的 `criteria`；再比对 16 个 advancement 与两份 lang 的 mtime | 描述：zh `触发一次「刺球」` / en `Trigger a "Fang Ball" once`（各一处）；标题**未变**：`最扎人的一集` / `The Itchiest Episode`；判据**未变**：`{"fang_dance": {"trigger": "minecraft:impossible"}}`；`fang_dance.json` mtime `2026-11-02 00:40:41`，两语 lang mtime `2026-11-06 03:01:09`（F-307 那次），16 个 advancement 里最晚的是 `converter.json 2026-11-06 01:00:22`（F-287），**没有一个落在 03:01** ⇒ 只动了语言值；键数仍 321 × 2（值变键不变） | **成立**（「其它进度一条不动」的子项以 mtime 取证，见 §5 第 7 条） | `logs/f310-static.log`（B-57 段）、`logs/f310-static-c.log`（16 个 mtime） |

### 2.7 计数面 B-58

| 编号 | 边界（一句话） | 怎么试 | 读数（十个数一次给全） | 结论 | 证据 |
|---|---|---|---|---|---|
| **B-58** | 计数面：**92 / 28 / 28 / 1 / 175 / 60 / 321 × 2 / 21 / 17 / 152**（任何一项用旧值 98 / 34 / 27 / 2 / 176 / 340 / 26 / 154 报交付即判失败） | ① 起服读自检行；② 跑 `tools/verify_project.py`；③ 数 `b.push(`；④ 数两份 lang 的键；⑤ 数 `farmstorm:converting` 配方；⑥ 游戏内枚举数培养器投入物；⑦ 数 `src/main/java` 的 `.java` | **92 / 28 / 28 / 1 / 175 / 60 / 321 × 2 / 21 / 17 / 152**（逐项来源见 §4）；十项全部是新值，旧值一个都没出现 | **成立** | §4 的十行来源；`logs/f310-session1.log`、`logs/f310-verify.log`、`logs/f310-static.log`、`logs/f310-probe1.log` |

---

## 3. 四条负向判据的「没有发生的证据」（原文）

E-64-5 要求这四条写「谁去试、试了什么、看到了什么（即没发生）」，不是「没测」也不是「应该不会」。

### B-32 · 不能取回输入

- **谁去试**：mineflayer 离线客户端 `FSEval`（真玩家、真 `block_place` 包）。**试了什么**：转换器槽里有 1 个小麦时，空手连续右键 **3 次**；再手持胡萝卜右键 1 次。
- **看到了什么（没发生）**：3 次空手右键之后 `heldBefore` 与 `heldAfter3EmptyClicks` **完全相同**（`same=true`）；世界内 `itemEntitiesAfter=Test failed`（**没有物品被丢出来让玩家捡**）；手持 `Found no elements matching SelectedItem`（**手上什么也没拿到**）；胡萝卜那次 `hand={count: 1, id: "minecraft:carrot"}` **未消耗**、槽内仍是小麦。
- **原文**（`logs/f310-probe1.log`）：
  ```text
  B-32 heldBefore=12, -60, 8 … {count: 1, id: "minecraft:wheat"} heldAfter3EmptyClicks=12, -60, 8 … {count: 1, id: "minecraft:wheat"} same=true itemEntitiesAfter=Test failed hand=Found no elements matching SelectedItem
  B-32b held=…{count: 1, id: "minecraft:wheat"} hand=FSEval … {count: 1, id: "minecraft:carrot"}
  ```

### B-37 · 漏斗塞不进、抽不出

- **谁去试**：同上（RCON 放漏斗，服务端自己 tick）。**试了什么**：转换器上下各一个漏斗 —— 下方 `facing=down`（尝试抽走槽里的小麦），上方 `facing=down` 且预先塞 **16 个小麦**（尝试推进去）；跑 **80 刻**（每 8 刻一次机会 ⇒ 10 次）。
- **看到了什么（没发生）**：下方漏斗 `Items` 读数是 `[]`（**一件都没被抽走**）；上方漏斗读数仍是 `[{count: 16, Slot: 0b, id: "minecraft:wheat"}]`（**16 个一个没少，一件都没推进去**）；转换器 `Held` 仍是那 1 个小麦，`Progress 12 -> 94` 照常涨（机器没被打断）。
- **原文**（`logs/f310-probe1.log`）：
  ```text
  B-37 HOPPER-BELOW items=14, -61, 8 … []
  B-37 HOPPER-ABOVE items=14, -59, 8 … [{count: 16, Slot: 0b, id: "minecraft:wheat"}]
  B-37 AFTER80T held=…{count: 1, id: "minecraft:wheat"} progress=…12->…94
  ```

### B-41 · 培养器不收改良作物

- **谁去试**：`FSEval`。**试了什么**：对同一台（已绑定、可用、槽已清空的）培养器依次真右键 5 件改良作物：`farmstorm:puffed_wheat`、`farmstorm:blast_carrot`、`farmstorm:friend_chives`、`farmstorm:lucky_berry`、`farmstorm:ubi_server`。
- **看到了什么（没发生）**：五条读数全是 `heldTag="Found no elements matching Held"`（**槽一直是空的**）且 `handAfter={count: 1 …}`（**手上那件没被吃掉**）。对照：同一台机器上 `farmstorm:plain_chives` 与 `minecraft:carrot` 是 `ACCEPT=YES`（机器确实可用）。
- **原文**（`logs/f310-probe1.log`）：
  ```text
  B-41 farmstorm:puffed_wheat => heldTag="Found no elements matching Held" handBefore={count: 1 handAfter={count: 1 ACCEPT=NO
  （blast_carrot / friend_chives / lucky_berry / ubi_server 四行同形，全 ACCEPT=NO）
  对照 B-38 farmstorm:plain_chives => … ACCEPT=YES(farmstorm:plain_chives)
  ```

### B-52 · 15 个改良作物方块仍无 `BlockItem`

- **谁去试**：静态逐 id 核对 + `FSEval` 在游戏内 `/give` + 拆块。
- **试了什么 / 看到了什么（没发生）**：① 15 个方块 id 逐个查 `FarmstormItems` 的注册 ⇒ **15/15 `BlockItem=False`**（没有一个补了方块物品）；② `/give FSEval farmstorm:puffed_wheat_crop 1` ⇒ `Unknown item 'farmstorm:puffed_wheat_crop'`（**物品不存在，给不出来**）；`/give … farmstorm:lucky_berry_bush 1` ⇒ `Unknown item 'farmstorm:lucky_berry_bush'`；③ 拆一块成熟的 `puffed_wheat_crop[age=3]` ⇒ 掉落实体是 `Puffed Wheat … Item: {count: 1, id: "farmstorm:puffed_wheat"}`（**掉的是作物物品，不是方块物品**）；④ 同时 15 个方块 `/setblock` 全部放得下（方块本身**没有**被删）。
- **原文**：`logs/f310-static.log` → `puffed_wheat_crop BlockItem=False` … 共 15 行；`logs/f310-probe2.log` → `B-52 GIVE-BLOCK-ITEM Unknown item 'farmstorm:puffed_wheat_crop'`、`B-52 GIVE-BLOCK-ITEM2 Unknown item 'farmstorm:lucky_berry_bush'`；`logs/f310-probe4.log` → `B-54 PRESENT 15/15`、`B-53 AGE3 DROP Puffed Wheat …`

---

## 4. B-58 的十个数（一行原文 + 逐项来源）

**一行读数（新值，十项一次给全）**：

```text
92 / 28 / 28 / 1 / 175 / 60 / 321 × 2 / 21 / 17 / 152
```

| # | 数 | 口径 | 读数原文 | 来源 |
|---|---|---|---|---|
| 1 | **92** | 注册对象合计 | `自检 注册对象 92 个：items 28 / blocks 28 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK`；检查器 `已核 registry 对象: 92 / 92` | `logs/f310-session1.log`、`logs/f310-verify.log` |
| 2 | **28** | `items` | 同一行的 `items 28` | 同上 |
| 3 | **28** | `blocks` | 同一行的 `blocks 28` | 同上 |
| 4 | **1** | `menu_types` | 同一行的 `menu_types 1` | 同上 |
| 5 | **175** | server config 键数 | `文档口径键数: 175`、`生成 toml 键数: 175`、`[OK] 175 个配置键与文档逐字一致（toml 175 键）`；静态复算 toml 里含 `=` 的非注释行 = 175 | `logs/f310-verify.log`、`logs/f310-static.log` |
| 6 | **60** | config 的 `b.push()` 调用次数 | `FarmstormConfig.java` 里 `b.push(` 命中 **60** 处（注意：**不是** toml 顶层节数，实测顶层节 = 36，见 §5 第 3 条） | `logs/f310-static.log` + 静态 grep 计数 |
| 7 | **321 × 2** | 语言键（zh_cn / en_us） | `zh 键数 = 321 / en 键数 = 321`；检查器 `[OK] lang 键集一致，共 321 键` | `logs/f310-static.log`、`logs/f310-verify.log` |
| 8 | **21** | `farmstorm:converting` 配方条数 | 逐文件解析：`converting recipes: count: 21`（21 + 3 `crafting_shaped` + 1 `crafting_shapeless` = 25 个文件） | `logs/f310-static.log` |
| 9 | **17** | 培养器可收投入物 | 游戏内 21 条逐条右键：`B-38 SUMMARY accepted=17 rejected=4`（静态口径 21 − 4 史诗 = 17，两者一致） | `logs/f310-probe1.log` |
| 10 | **152** | Java 文件数 | `src/main/java` 递归 `.java` = **152** | `logs/f310-static.log` |

**检查器结论行**：`结论：FAIL 0 项，WARN 1 项`（`VERIFY-EXIT=0`；那条 WARN 是 `trade.*` 六个键的「文档逻辑路径 → 落盘拼写」形状归一，F-309 已登记，不影响 175/175）。

---

## 5. 差异、口径说明与观察（只登记，不改上游、不改码）

1. **探针自身的工具性 bug（不是产品缺陷，登记以便复核）**：① `/data merge block … {Items:[]}` 与 `/data modify … Items set value []` 都**清不空**核的 54 格 —— `OutpostCoreBlockEntity.loadAdditional` 走 `ContainerHelper.loadAllItems`，它只写列表里出现的槽，缺的槽保持原值；清空必须逐槽写 `minecraft:air`。② 单条 RCON 命令超过约 1000 字符会被打断（54 槽一条约 1800 字符时连接直接断），必须按 18 槽一段切分。③ `f310-probe5.js` 首版漏了 `place()` 定义（`ReferenceError`）。④ B-56 第一次读档读数在起服后 60 s 才连上，炮弹已按自己的 300 刻到点消失，误判为「没持久化」。这四处各修一轮，是起服次数偏多的唯一原因。
2. **B-51 的「items +1（净）」与实测口径有偏差**：平凡韭菜这一版新增的是**两个**物品 id（`plain_chives` 与 `plain_chives_seeds`）与一个方块 id（`plain_chives_crop`）；`items` 注册总数从 34 降到 28 是因为同版删了 7 个旧种子 id 与 1 个催化剂物品。若把「items +1（净）」读成「本特性只加了一个物品」，与实测不符；本记录按「净新增（不是替换已删种子）」判定成立。
3. **B-58 的「60」口径**：53 号 §7 第 7 行写的是「config 的 `b.push()` 调用次数（不变）」，实测 60 ✓；toml 的**顶层 `[section]` 数是 36**，两者不是同一个数，复核时别互换。
4. **B-33 的「掉落」有两种观察面**：`/setblock … air` 走 `onRemove`，输入物**掉在地上**（人站远时读到 1 个 ItemEntity）；玩家真挖时同一份输入物掉出来后会被**身旁的玩家当场捡走**（背包多出 `carrot` / `cocoa_beans`），地面上留下的是方块本体。两条路径都只交出一件输入物、都不掉产物。
5. **补绑（100 刻扫描）只补「没有 `Core` 键」的机器**：B-28 与 B-35 各观察到一次 —— 机器侧 `Core` 缓存仍指向「已拆的核 / 被重放的核」时，核侧不再认它、补绑也不收它，机器会**永久不可用**（不是「自动改绑最近的核」）。这与 B-35 的判据（留原地、内容保留、不可使用）一致，但与「补绑会把它收回去」的直觉相反，登记备查（**未改码**）。
6. **B-54 的「15/15 存在」与 1.5 s 后 14/15**：15 块逐块 `setblock` 全部生效（`PRESENT 15/15`），1.5 s 后有 **1 块**被邻块的方块更新挤掉 —— 这是原版作物的「需要合适支撑面」行为（`BushBlock` 系列），不是「方块被删」；同批被拆的两块都 `serverAir=true`、掉落作物本体（B-53）。**未定位到具体是哪一块**（那台服已 `/stop`），故记为子项。
7. **B-57 的「其它进度一条不动」没有 git 取证**（本轮纪律不许跑 git）：改用 mtime —— 16 个 advancement 里 15 个是 `2026-11-02 00:40:41`、`converter.json` 是 `2026-11-06 01:00:22`（F-287），两语 lang 是 `2026-11-06 03:01:09`（F-307）；**没有一个进度 JSON 落在 03:01**，因此「只改语言值」成立。
8. **B-46 的转换器那一侧读数是 `ACCEPT=NO`**：不是配方没生效（同一时刻培养器 `ACCEPT=YES`），而是那台转换器在 B-35 里刚经历过「核被拆又重放」，`Core` 缓存失效 ⇒ 不可用（见第 5 条）。B-46 的判据主体是**培养器**，故不影响结论。

---

## 6. 不改码取证（不跑 git）

| 取证方式 | 读数 |
|---|---|
| `src/`、`tools/`、`docs/` 三棵树里 mtime 晚于 **2026-11-06 03:30:00**（本轮开工时刻）的文件 | **0 个** |
| 上述三棵树里 mtime 最新的文件 | `tools/__pycache__/verify_project.cpython-312.pyc` 03:03:44、`src/main/java/…/Farmstorm.java` 03:02:29、`tools/verify_project.py` 03:02:25 —— 都是 F-308 关窗时的改动（`__pycache__` 是 F-309 跑检查器的产物），全部早于本轮 |
| 本轮**写**过的位置 | 只有 `.work/v023-verify/`（`boundaries.md`、`f310-probe*.js`、`f310-*.py`、`logs/f310-*`）与 `run-v023/`（config + world + 临时数据包，已还原 / 删除） |
| `run-v023` 的改动 | `cultivator.yield` 3 → 已还原 2；临时数据包 `world/datapacks/f310-temp` 已删（`Test-Path` = False）；`run-*/` 在 `.gitignore` 内 |
| `acceptance.md`（F-309 的记录） | **只读**，未改（本轮无任何 write / edit 指向它） |
| Mixin | 0（检查器 §4 `[OK] 无 Mixin / TODO / 调试残留 / 空 catch`；本轮不改码） |

---

## 7. 交付物与「没做到的」

```text
.work/v023-verify/boundaries.md              本记录（31 条 + 四条负向 + 十个数）
.work/v023-verify/logs/f310-probe1.log       R1 全部（B-28~B-37）+ B-38/B-39/B-40/B-41 枚举
.work/v023-verify/logs/f310-probe2.log       B-35 / B-46 / B-52 / B-55
.work/v023-verify/logs/f310-probe3.log       B-56 首读（读数作废，登记在 §5 第 1 条）
.work/v023-verify/logs/f310-probe4.log       B-33(a) / B-48 / B-53 / B-54
.work/v023-verify/logs/f310-probe5.log       B-33(dig) / B-34 / B-42 / B-43 / B-44
.work/v023-verify/logs/f310-b45-inline.txt   B-45（会话⑥控制台原文）
.work/v023-verify/logs/f310-b56-inline.txt   B-56（会话⑦⑧⑨⑩ 控制台原文）
.work/v023-verify/logs/f310-static.log       静态面（B-47 / B-49 / B-50 / B-51 / B-52 / B-53 / B-57 / 计数四项）
.work/v023-verify/logs/f310-static-c.log     16 个 advancement 的 mtime
.work/v023-verify/logs/f310-session{1,6}.log 起服①⑥的完整 server log（自检行在 ①）
.work/v023-verify/logs/f310-session{1,2,3,4,5,6,7,8,9,10}.stdout.log  各次起服的 gradle 输出（末行 BUILD SUCCESSFUL）
.work/v023-verify/logs/f310-build.log        gw.cmd build（BUILD-EXIT=0）
.work/v023-verify/logs/f310-verify.log       tools/verify_project.py（VERIFY-EXIT=0）
.work/v023-verify/f310-probe{1..8}.js        探针；f310-static*.py / f310-quick*.py 静态与手动脚本
```

**没做到的 / 打折的（一条一行）**：

- B-54 的「1.5 s 后缺的那一块」未定位到具体 id —— 该次服已 `/stop`，没有第二次起服重放这一格。
- B-57 的「其它进度一条不动」没有 git 取证（本轮纪律不许跑 git），只能用 16 个 advancement 的 mtime 反证。
- B-56 的「≈300」是「读档后实测 ≥185 刻存活 + 阈值 300 刻倒推约 115 刻」的结果，不是从一次起服里从 0 数到 300；决定性的是「持久化只允许 32 刻」这条算术排除。
- B-33 的「真挖」路径里输入物被身旁玩家当场捡走，地面读数为 0 件；本条靠玩家背包与 `setblock` 路径两处合并取证。
- 会话②③④⑤⑦⑧⑨⑩ 的 server log 未逐次归档（`latest.log` 每次起服被覆盖），这些会话的读数以对应的 `f310-probe*.log` / `f310-*-inline.txt` 为准。
