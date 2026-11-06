# 62 号 v0.2.4 主提示词（实现方开工前只读一次）

> 用途：实现方在动第一行代码之前读一次本文，按 61 号（`docs/zh/61-TASKS-0.2.4.md`）的 16 个任务逐个开工。本文不复述 59 / 60 的论证，只给五样东西——现状读数、不许动的东西、不许做的动作、会炸的地方、交付与回报口径。
> 上游：58 号（`docs/zh/58-DECISIONS-0.2.4.md`，D-225 ~ D-252，唯一真相源）/ 59 号（R1 ~ R4、B-59 ~ B-73、已知影响 4 条）/ 60 号（§4.1 ~ §4.4、§8 的 25 条验收、RT-83 ~ RT-91）/ 61 号（F-312 ~ F-327、Phase 66 ~ 72、M62 ~ M68、A-102 ~ A-110）/ `.work/v024-BRIEF.md`（下称 BRIEF，§2 的 7 条作者裁定）。
> 编号：本文只发两个系列——硬约束 **HC-173 起**、禁止行为 **N-155 起**。其余系列一律写全前缀（D-225 起 / B-59 起 / F-312 起 / Phase 66 起 / M62 起 / RT-83 起 / A-102 起 / E-66-1 起），不认领下游编号。
> 行号：本文行号按 `read` 工具 UTF-8 实读（每次 offset + limit ≤ 200）。不要用 `pwsh` 的 `Get-Content` 数行号，见陷阱 25。
> 判据：只认五类可观察读数——计数、日志行、`/data get` 读数、源码 grep 命中、像素比对。感受类措辞不进任何断言。
> 冲突：上游打架时按 **决策 > 技术 > 任务** 取顺序，一律报给作者，不自行取舍（见 §6）。

---

## 1. 事实卡（18 行，每行一个可观察读数）

| # | 事实（读数 / 现状） | 出处 |
|---|---|---|
| 1 | Java 文件 **150** 个（farmland 两个类已删），本版目标 **151**（152 − 2 + 1） | `tools/verify_project.py:479-480` 只拼内容、不校验文件数；`(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` 实点 = 150 |
| 2 | 注册面 **92**（28+28+8+13+4+3+1+1+1+3+1+1），运行时断言 `total == 92` | `Farmstorm.java:180` ~ `:183` |
| 3 | server config **175** 键 / 60 组；client config **3** 键 | `FarmstormClientConfig.java:25-27` |
| 4 | 语言键 **321 × 2**，`farmstorm.configuration.` 前缀 **178** | `ModLangProvider.java:14`；两份 lang 实点各 321 / 前缀 178 |
| 5 | 基线 `COMMIT 52749b9` | `docs/zh/58-DECISIONS-0.2.4.md:3`；`git rev-parse --short HEAD` 实读 = `52749b9` |
| 6 | `farmstorm:processing_block` 标签 **2 个成员**（cultivator、crop_converter） | `processing_block.json:2-5`、`ModBlockTagsProvider.java:45-47` |
| 7 | 26 条交易 listing 的 xp **全为 2**（22 + 1 + 3） | `BotanistTrade.java:49`、`TierTrade.java:45`、`BotanistEpicTrade.java:46` |
| 8 | 等级切片边界 **0 / 6 / 12 / 18 / 22**；农民挂 1 / 3 / 5 级 | `VillagerTradeEvents.java:44-47`、`:55-57` |
| 9 | 浮窗内层底色现值 `0x80000000`（alpha `0x80` = 128，128 ÷ 255 = 50.2%） | `CoreHudRenderer.java:40` |
| 10 | 浮窗边框 `0xFF000000` 不透明；两条 `fill` 画外框整块 + 内底内缩 1 px | `CoreHudRenderer.java:41`、`:77-78` |
| 11 | 「核心浮窗」= HUD 叠加层，订阅 `RenderGuiEvent.Post`；54 格容器界面是另一个类 | `CoreHudRenderer.java:27`、`:52`；`OutpostCoreScreen.java:21` |
| 12 | 候选发现现状是**两次按类枚举** | `OutpostCoreBlockEntity.java:605`、`:607` |
| 13 | 补绑对「机器侧已有缓存」一律跳过，不判该坐标还有没有核 | `OutpostCoreBlockEntity.java:630` |
| 14 | 清理出口只判培养器，转换器无分支 | `OutpostCoreBlockEntity.java:870-872` |
| 15 | 全仓 `clearCore` 定义 **1 处** + 调用 **1 处** | `CultivatorBlockEntity.java:94`、`OutpostCoreBlockEntity.java:871` |
| 16 | 转换器**没有** `syncCore`；全仓唯一定义在培养器 | `CultivatorBlockEntity.java:104` |
| 17 | 三处 `useWithoutItem` 的客户端分支无条件 `SUCCESS` | `CropConverterBlock.java:121-122`、`CultivatorBlock.java:117-118`、`OutpostCoreBlock.java:131-132` |
| 18 | 转换器 `convertible` 是 `private`（`:129`）；培养器 `admissible` 是 `public`（`:143`）；`core()` 要求 `ServerLevel` | `CropConverterBlockEntity.java:129`、`:232`、`:234`；`CultivatorBlockEntity.java:143` |

读法：第 1 行是本版唯一变化的数字；第 2 ~ 4 行是本版冻结面；第 9 ~ 11 行是需求 3，第 12 ~ 16 行是需求 1，第 7 ~ 8 行是需求 2，第 17 ~ 18 行是需求 4。

---

## 2. 硬约束 HC-173 ~ HC-188（16 条，每条可判定）

- **HC-173** · 不改 `Farmstorm.java:180` 起的十二类注册自检，运行日志的「92 个」本版前后相等（92 − 0 + 0）；依据：D-227、`Farmstorm.java:180` ~ `:183`。
- **HC-174** · 不新增、不删除任何注册 ID，`tools/verify_project.py:458` 的 `EXPECTED_COUNTS` 十二项逐项 diff 为 0；依据：D-227、BRIEF §6 红线 1。
- **HC-175** · server config 保持 175 键 / 60 组、client config 保持 3 键，`FarmstormClientConfig.java:25-27` 三行 diff 为 0；依据：D-227、裁定 #4、红线 2。
- **HC-176** · 语言键保持 321 × 2 且 `farmstorm.configuration.` 前缀 178 个，两份 lang 顶层键数各 321；依据：D-227、`ModLangProvider.java:14`。
- **HC-177** · `PROTOCOL_VERSION = "3"`（`FarmstormNetwork.java:29`）与两个 `playToClient`（`:42`、`:47`）不动，`CultivatorLinkPayload.Link` 的三分量（`:63`）不动；依据：D-227、红线 3。
- **HC-178** · `VillagerTradeEvents.java:44-47` 的四条 `subList` 边界保持 0 / 6 / 12 / 18 / 22，`:55-57` 的三条 `get(n)` 挂载位置不动；依据：D-239、红线 4。
- **HC-179** · `BotanistTrade.java:60-82` 的 22 项顺序与行内 `// 1` ~ `// 22` 序号注释逐行不动，只在每项补第二实参；依据：D-237、红线 4、RT-90。
- **HC-180** · `OutpostCoreScreen` 本版 diff 为 0 行，浮窗的颜色改动只落在 `CoreHudRenderer.java:40`；依据：D-240、D-242、红线 5。
- **HC-181** · 三个 `block/` 文件的客户端分支只调用 `convertible` / `admissible` / `BlockItem` 判定，`canInsert` 在这三个文件里命中 0；依据：D-244。
- **HC-182** · 转换器的 `syncCore()` 按**新写方法**交付（`private void syncCore` 定义 1 处 + `sendBlockUpdated` 1 处），不是只补一行调用；依据：D-247、RT-87、BRIEF §5 第 3 行。
- **HC-183** · `KIND_BY_BLOCK` 与 `ModBlockTagsProvider.java:45-47` 的成员集合必须同步：标签加一个成员，同一提交在映射表加一条；依据：D-235、RT-86、BRIEF §7 #1。
- **HC-184** · `CoreBound` 只声明三个方法，`kind(` 在该文件命中 0；依据：D-228、D-234、`CultivatorLinkPayload.java:69`。
- **HC-185** · 转换器 `clearCore()` 不清槽与进度：方法体内 `held` / `progress` 命中 0，NBT 键 `Held` / `Progress` 保留；依据：D-229、D-168、`CropConverterBlockEntity.java:51`、`:53`。
- **HC-186** · `AreaBlockEntities` 的旧重载（`AreaBlockEntities.java:32-33`）一行不动，新增重载用 `getChunkNow`（`:39`）取区块；依据：D-231。
- **HC-187** · 交易项的 `MIN_PRICE` / `MAX_PRICE` / `MAX_USES` / `COUNT` / `PRICE_MULTIPLIER` 逐行 diff 为 0；依据：D-237、红线 6。
- **HC-188** · 本版只改 `.java`：`git status --porcelain` 里 `src/main/resources/` 与 `src/generated/resources/` 命中 0；依据：D-246、60 号 §3.3。

---

## 3. 禁止行为 N-155 ~ N-166（12 条，每条三段）

- **N-155** · 在 `OutpostCoreScreen` 里找「核心浮窗」的背景常量
  - **不要做什么**：不要把 `OutpostCoreScreen.java` 列进本版改动面，也不要为「面板更透」去调它的贴图或颜色。
  - **为什么**：它是据点核心的 54 格容器界面（`OutpostCoreScreen.java:21`），浮窗是 HUD 叠加层（`CoreHudRenderer.java:52`），两者只是都能「浮在屏幕上」。
  - **会看到什么后果**：自检时浮窗底色仍读到 `0x80000000`（`CoreHudRenderer.java:40`），而容器界面被改坏，红线 5 判错且回滚面多一个文件。
- **N-156** · 把「30%」读成透明度，或给浮窗挂 config 键
  - **不要做什么**：不要写 `0xB3`（0.70 × 255 ≈ 179），不要加 `hud.opacity` 一类的新键。
  - **为什么**：0.70 × 255 = 178.5 ≈ 179 比现状 128 更不透明，与「增加透明度」相反；加键会同时破 config 3 键与语言键 321 × 2（`FarmstormClientConfig.java:25-27`）。
  - **会看到什么后果**：验收 #19 的 `0x80000000` 归零与 `0x4D000000` 命中 1 双双不成立，配置面 FAIL 由 0 变正。
- **N-157** · 在客户端分支里调用 `canInsert`
  - **不要做什么**：不要把服务端那条含 `isUsable()` 的准入判据搬到 `if (level.isClientSide)` 分支里。
  - **为什么**：`canInsert` 含 `isUsable()`（`CropConverterBlockEntity.java:118-119`），而 `core()` 要求 `ServerLevel`（`:232`、`:234`），客户端恒取不到 ⇒ 谓词恒假。
  - **会看到什么后果**：右键整台机器一次都不接走，比 0.2.3 更差；验收 #21 的 `canInsert` 归零也不成立。
- **N-158** · 用 `setCore(null)` 表达解绑
  - **不要做什么**：不要把「清缓存」写成「写一个空坐标」，也不要让 `setCore` 变成读写两用。
  - **为什么**：NBT 的约定是「缺 `Core` 键 = 未绑定」（写面 `CropConverterBlockEntity.java:269-272`），写 null 与缺键在读数上分不开。
  - **会看到什么后果**：验收 #10 的「`Core` 键不存在」读不出来，D → A 那条迁移失去可观察证据。
- **N-159** · 只补一行 `this.syncCore();` 而不写方法本身
  - **不要做什么**：不要把 D-247 读成「在 `setCore` 里加一行调用」。
  - **为什么**：转换器没有 `syncCore`，全仓唯一定义在 `CultivatorBlockEntity.java:104`。
  - **会看到什么后果**：`cannot find symbol`，编译当场失败；这不是静默失败，但 F-314 的整个提交停摆。
- **N-160** · 在 `clearCore()` 里清 `Held` / `Progress`
  - **不要做什么**：不要把「解绑」与「排空机器」写成同一件事。
  - **为什么**：D-168 的「不清槽、不清进度」继续有效（`CropConverterBlockEntity.java:51`、`:53`）。
  - **会看到什么后果**：拆核后机器里那件东西与进度一起消失，玩家丢货；验收 #10 的 `Held` / `Progress` 仍在不成立。
- **N-161** · 把「按标签发现」与「按类枚举」在同一方法里各留一半
  - **不要做什么**：不要只在 `:605` / `:607` 里删一条，也不要把发现改了而清理出口（`:870-872`）留着。
  - **为什么**：这两处是同一台机器的两个面，只改一半就是 58 号 §6 第 5 条的否决面（半个修复）。
  - **会看到什么后果**：转换器仍停在死坐标（`OutpostCoreBlockEntity.java:630` 对非空缓存一律跳过），或者下一个标签成员永远拿不到绑定。
- **N-162** · 为修音效去换 `SoundType` 或新增 `sound_event`
  - **不要做什么**：不要动 `FarmstormBlocks.java:78` / `:108` 的 `SoundType.ANVIL`，也不要新增声音事件。
  - **为什么**：根因是客户端预测被 `SUCCESS` 掐断，音效资源一行都没错。
  - **会看到什么后果**：注册面 92 加 1 变 93（`Farmstorm.java:180` 的自检当场 FAIL），并改掉全模组的放置音。
- **N-163** · 重排 `NORMAL` 的 22 行，或在 `getOffer` 里按 `CropTier` 推 xp
  - **不要做什么**：不要按等级把 22 行重新排序，也不要写「档位 → 经验」的查表。
  - **为什么**：等级由 `subList` 位置决定（`VillagerTradeEvents.java:44-47`），1 ~ 4 级全是 `NORMAL` 切片（`BotanistTrade.java:60-82`）。
  - **会看到什么后果**：每一级的商品集合全变，需求 2 从「改经验」变成「改商品」；用档位推会把四个等级算成同一个值。
- **N-164** · 保留旧 `VILLAGER_XP` 类常量「作兼容」
  - **不要做什么**：不要留下 `BotanistTrade.java:49` 与 `TierTrade.java:45` 那两个常量。
  - **为什么**：两个 record 已经逐项携带 `xp`，留下常量就是同一等级的第二份真相。
  - **会看到什么后果**：验收 #16 的 `VILLAGER_XP` 三类命中 0 / 0 / 2 变成别的数，改一处忘一处。
- **N-165** · 把 `BotanistEpicTrade:28` 的 javadoc 留到下一批
  - **不要做什么**：不要只改 `:46` 的常量而放过 `:28` 的 `xp = 2` 与 `:45` 的注释。
  - **为什么**：F-321 删掉 `TierTrade.VILLAGER_XP` 之后，`:28` 的 `{@link TierTrade#VILLAGER_XP}` 是悬挂链接，且 `xp = 2` 成为事实错误。
  - **会看到什么后果**：验收 #15 的「该类 `= 2` 命中 0」不成立，javadoc 与代码脱节（RT-89）。
- **N-166** · 把静态读链或 datagen 时间戳写成实测结论
  - **不要做什么**：不要写「已验证」除非进游戏跑出读数；不要用文件 mtime 判断产物变没变。
  - **为什么**：「只有放置者听不见」（`ServerLevel.java:994-1004` 与 `ClientLevel.java:527-529`）与副手放置都只有静态推链，D-252 要求按未实测登记。
  - **会看到什么后果**：签字把未实测写成已验证，下一版把同一个坑重走一遍（RT-88、RT-91）。

---

## 4. 陷阱清单（26 条，每条四栏）

- **陷阱 1 · 把「核心浮窗」当成 `OutpostCoreScreen`**
  - **症状**：改了 54 格容器界面的颜色或贴图，`CoreHudRenderer` 的底色读数仍是 `0x80000000`。
  - **原因**：两个名字都能「浮在屏幕上」，前者订阅 `RenderGuiEvent.Post`（`CoreHudRenderer.java:52`），后者是 `AbstractContainerScreen`（`OutpostCoreScreen.java:21`）。
  - **正确做法**：只改 `CoreHudRenderer.java:40` 一行常量，不碰 `OutpostCoreScreen`。
  - **依据**：D-240、红线 5、`CoreHudRenderer.java:40`。
- **陷阱 2 · 改 `:41` 的边框，以为改的是背景**
  - **症状**：面板背后的地形没变清楚，1 px 边界反而糊掉。
  - **原因**：`:40` 是 `COLOR_BACKGROUND`、`:41` 是 `COLOR_BORDER`；两条 `fill` 先画外框整块（`:77`）再画内底内缩 1 px（`:78`）。
  - **正确做法**：只改 `:40`，`:41` 与 `:77-78` 的几何一行不动。
  - **依据**：D-242、`CoreHudRenderer.java:77-78`。
- **陷阱 3 · 把「30%」按透明度读，取 alpha `0xB3`**
  - **症状**：面板比 0.2.3 更不透明，玩家反馈「更糊了」。
  - **原因**：0.70 × 255 = 178.5 ≈ 179 = `0xB3`（70.2%），比现状 128（50.2%）更厚。
  - **正确做法**：按不透明度读，0.30 × 255 = 76.5 ≈ 77 = `0x4D`。
  - **依据**：BRIEF 裁定 #3、D-241。
- **陷阱 4 · 想让 30% 可配，加一个 `hud.opacity` 键**
  - **症状**：config 与语言文件同时出现新键，检查器与人工计数两头对不上。
  - **原因**：裁定 #4 要求硬编码；加一个 config 键要连带 1 条语言键，321 × 2 当场破。
  - **正确做法**：常量写死 `0x4D000000`，不新增键、不改 `FarmstormClientConfig.java:25-27`。
  - **依据**：裁定 #4、D-241、D-227。
- **陷阱 5 · 客户端谓词复用 `canInsert`**
  - **症状**：右键转换器一次都不接走，比 0.2.3 还差。
  - **原因**：`canInsert` 含 `isUsable()`（`CropConverterBlockEntity.java:118-119`），`core()` 要求 `ServerLevel`（`:232`、`:234`），客户端恒假。
  - **正确做法**：转换器用 `convertible`（`:129`），培养器用 `admissible`（`CultivatorBlockEntity.java:143`）。
  - **依据**：D-244、58 号 §6 第 6 条。
- **陷阱 6 · 直接在 `block/` 里调 `convertible`，忘了它是 `private`**
  - **症状**：`convertible has private access in CropConverterBlockEntity`。
  - **原因**：`CropConverterBlockEntity.java:129` 现为 `private`，`block` 与 `blockentity` 不同包。
  - **正确做法**：提权放在 F-314 同一提交，不塞进 F-324。
  - **依据**：A-104、`CropConverterBlockEntity.java:129`。
- **陷阱 7 · 以为转换器已有 `syncCore`，只补一行调用**
  - **症状**：`cannot find symbol: method syncCore()`。
  - **原因**：全仓唯一定义在 `CultivatorBlockEntity.java:104`，转换器从来没写过。
  - **正确做法**：新写私有方法，并在 `setCore`（`CropConverterBlockEntity.java:89`）与 `clearCore` 各调一次。
  - **依据**：D-247、RT-87、BRIEF §5 第 3 行。
- **陷阱 8 · 新方法里忘了 `Block.UPDATE_CLIENTS`**
  - **症状**：服务端已清缓存，客户端仍显示旧绑定，方块更新不下发。
  - **原因**：同步靠 `sendBlockUpdated(..., Block.UPDATE_CLIENTS)`，该常量在转换器里的先例是 `CropConverterBlockEntity.java:227`。
  - **正确做法**：照 `CultivatorBlockEntity.java:104-110` 同形写，新方法内 `sendBlockUpdated` 恰 1 处。
  - **依据**：D-247、60 号 §4.1.3。
- **陷阱 9 · 用 `setCore(null)` 表达解绑**
  - **症状**：`/data get block` 读到 `Core` 键仍存在。
  - **原因**：约定是「缺 `Core` 键 = 未绑定」，写面在 `CropConverterBlockEntity.java:269-272`。
  - **正确做法**：新增 `clearCore()`，只置空字段 + 标脏 + 同步。
  - **依据**：D-229 补充、58 号 §8 第 1 条。
- **陷阱 10 · `clearCore()` 顺手把槽清空**
  - **症状**：拆核后机器里那一个物品与进度一起没了。
  - **原因**：`Held` / `Progress` 的键在 `CropConverterBlockEntity.java:51`、`:53`，解绑不该碰它们。
  - **正确做法**：`clearCore()` 方法体内 `held` / `progress` 命中 0。
  - **依据**：D-168、D-229、验收 #10。
- **陷阱 11 · 给 `AreaBlockEntities.collect` 加第四个 `Class` 列表参数**
  - **症状**：调用点写成「一次转两次」的形状。
  - **原因**：这正是 D-230 要删掉的那种形状；旧重载在 `AreaBlockEntities.java:32-33`。
  - **正确做法**：新增 `Predicate<BlockEntity>` 重载，旧 `Class<T>` 重载原样保留。
  - **依据**：D-231、58 号 §6 第 5 条。
- **陷阱 12 · 重载里用 `level.getBlockEntity` 逐个查**
  - **症状**：补绑把没加载的区块拉进内存，一 tick 卡一下。
  - **原因**：纪律是不为枚举加载区块，`AreaBlockEntities.java:39` 用的是 `getChunkNow`。
  - **正确做法**：照抄 `:39-42` 的两段取块写法，取不到就跳过。
  - **依据**：60 号 §4.1.4 约束 ①、`AreaBlockEntities.java:39`。
- **陷阱 13 · 边遍历 `chunk.getBlockEntities().values()` 边过滤**
  - **症状**：偶发 `ConcurrentModificationException`。
  - **原因**：`AreaBlockEntities.java:43` 的两阶段拷贝就是为这件事。
  - **正确做法**：先 `new ArrayList<>(...)` 复制再过滤。
  - **依据**：60 号 §4.1.4 约束 ②。
- **陷阱 14 · 加标签成员时只改一边（标签或映射）**
  - **症状**：新方块被补绑发现，却永远绑不上，日志一行不增。
  - **原因**：`KIND_BY_BLOCK.get(...)` 取到 `null` 就 `continue`；标签成员由 `ModBlockTagsProvider.java:45-47` 生成。
  - **正确做法**：映射表与标签生成处同一提交同改，两处成员集合相等。
  - **依据**：D-235、RT-86、58 号 §8 第 4 条。
- **陷阱 15 · 复用 `kindOf(BlockPos)` 当类型映射**
  - **症状**：同一个「这台机器是什么」出现两份来源，迁移与补绑取值不一致。
  - **原因**：`OutpostCoreBlockEntity.java:373` 已有 `kindOf(BlockPos)`，它按核侧列表取 kind，与「方块 → kind」不是同一件事。
  - **正确做法**：新建静态 `KIND_BY_BLOCK`，就地取表，不新写同名方法。
  - **依据**：60 号 §4.1.6 约束 ②、`OutpostCoreBlockEntity.java:373`。
- **陷阱 16 · 只改发现、不改清理（半个修复）**
  - **症状**：转换器仍停在死坐标，再放多少核都绑不上。
  - **原因**：`OutpostCoreBlockEntity.java:605` / `:607` 与 `:870-872` 是同一台机器的两个面。
  - **正确做法**：五处 + 映射表同一提交（F-317），不拆成两个提交。
  - **依据**：A-103、59 号附录 A 表下注 1。
- **陷阱 17 · 只改清理、不改发现**
  - **症状**：这一台修好了，下一个进标签的方块仍拿不到绑定。
  - **原因**：发现路径仍是两个类名硬编码（`:605`、`:607`），第三个成员根本不会被收集。
  - **正确做法**：一次按 `isProcessingBlock`（`:887`）收集，删掉两次按类枚举。
  - **依据**：D-230、59 号附录 A 表下注 1。
- **陷阱 18 · 以为拆核清理走 `onRemove`**
  - **症状**：改了 `OutpostCoreBlock` 的那处，绑定一点没变。
  - **原因**：清理走 `setRemoved()` → `settleOnRemoval()`，`OutpostCoreBlock.onRemove` 只掉落 + `release()`，不碰绑定。
  - **正确做法**：改 `OutpostCoreBlockEntity.java:870-872` 这一处出口②。
  - **依据**：BRIEF §1 第 3 条、`OutpostCoreBlockEntity.java:870`。
- **陷阱 19 · 顺手把 `:867` 的日志也改掉**
  - **症状**：验收 #9 从「一条旧串归零」变成「两条旧串归零」，回滚面变大。
  - **原因**：`:867` 属出口③（`:863`、`:867` 两行），不在 58 号 §3.2 的 23 处动作面内。
  - **正确做法**：只改 `:872`，`:863` 与 `:867` 两行不动。
  - **依据**：A-108、60 号 §4.1.7 约束 ③。
- **陷阱 20 · 指望补绑自愈，不修清理出口**
  - **症状**：以为加一段「按当前坐标查核」就能修好，改完转换器仍僵死。
  - **原因**：`OutpostCoreBlockEntity.java:630` 对非空缓存一律 `continue`，不判该坐标还有没有核。
  - **正确做法**：修出口②（`clearCore()` 覆盖标签内全部方块），补绑保持原判据。
  - **依据**：D-233、BRIEF §1 第 5 条。
- **陷阱 21 · 按 `CropTier` 查表推 xp**
  - **症状**：植物学家 1 ~ 4 级拿到同一个值，需求 2 当场落空。
  - **原因**：等级与档位不同轴——1 ~ 4 级全是 `NORMAL` 切片（`VillagerTradeEvents.java:44-47`）。
  - **正确做法**：按位置逐项写死第二实参：6 / 6 / 6 / 4 = 22。
  - **依据**：D-236、D-237、58 号 §6 第 2 条。
- **陷阱 22 · 加 `xp` 分量时按等级重排 22 行**
  - **症状**：每一级的商品集合全变，玩家发现 1 级卖的东西换了。
  - **原因**：等级由 `subList` 位置决定，顺序即商品表（`BotanistTrade.java:60-82`）。
  - **正确做法**：只补第二实参，行序与 `// 1` ~ `// 22` 注释逐行不动。
  - **依据**：红线 4、RT-90、验收 #18。
- **陷阱 23 · 删了 `TierTrade.VILLAGER_XP` 却没改 `BotanistEpicTrade:28`**
  - **症状**：javadoc 的 `{@link TierTrade#VILLAGER_XP}` 悬挂，`xp = 2` 变成事实错误。
  - **原因**：`BotanistEpicTrade.java:28`、`:45`、`:46` 是同一处口径的三份副本。
  - **正确做法**：三行同批改，`:46` 改为引用 `VillagerTradeEvents.XP_LEVEL_5`。
  - **依据**：D-238、60 号 §4.2.4、RT-89。
- **陷阱 24 · 五个常量写成 `private`**
  - **症状**：三个交易类引用不到，各自抄一份，改一处忘两处。
  - **原因**：`event/` 下现无任何 `XP_LEVEL` 常量（实读命中 0），没有先例可抄。
  - **正确做法**：写成 `public static final int`，右值字面量，只有 `VillagerTradeEvents` 一处定义。
  - **依据**：D-236、RT-89、验收 #13。
- **陷阱 25 · 用 `pwsh` 的 `Get-Content` 数行号**
  - **症状**：报出的行号与实读不一致——本会话实测 `CoreHudRenderer.java`：`Get-Content` 得 142 行、`read` 得 149 行，`COLOR_BACKGROUND` 被数成第 36 行，实为第 40 行。
  - **原因**：`Get-Content` 在这台机器上的行拆分与 `read` 不一致，行号整体前移 4 行。
  - **正确做法**：行号一律用 `read` 工具实读，每次 `offset` + `limit` ≤ 200 行。
  - **依据**：本会话实测 142 / 149 两个读数、`CoreHudRenderer.java:40`。
- **陷阱 26 · 以为检查器会校验 Java 文件数，或把 151 加进去**
  - **症状**：加了 `CoreBound.java` 后担心检查器红；或者反过来把 151 写进 `EXPECTED`。
  - **原因**：`tools/verify_project.py:458` 的 `EXPECTED_COUNTS` 是注册 ID 清单，不是文件数；`:479-480` 只把源码拼成一个字符串做内容匹配。
  - **正确做法**：151 只出现在 F-313 的读数、F-326 的 #3 与签字散文里，检查器一行不改。
  - **依据**：A-109、`tools/verify_project.py:458`。

---

## 5. 交付清单（16 个任务，对齐 60 号 §8 的 25 条验收）

| 勾选 | 任务 | 交付物（位置） | 对应验收 # |
|---|---|---|---|
| [ ] | F-312 | `gradle.properties:11` + 两支 `.cmd` 共 6 处版本串 | —（产物名与版本串进 F-327 签字） |
| [ ] | F-313 | 新建 `blockentity/CoreBound.java`（三方法，Java 文件 151） | #3、#4 |
| [ ] | F-314 | `CropConverterBlockEntity`：`implements`、`clearCore()`、`syncCore()`、`convertible` 提权 | #5 |
| [ ] | F-315 | `CultivatorBlockEntity.java:44` 加 `implements CoreBound`，四个方法体 diff 0 行 | #7 的前提 |
| [ ] | F-316 | `AreaBlockEntities` 新增按谓词重载，旧重载不动 | #6 的前提 |
| [ ] | F-317 | `OutpostCoreBlockEntity` 五处 + `KIND_BY_BLOCK`（需求 1 正面修复） | #6、#7、#8、#9、#10、#11、#12 |
| [ ] | F-318 | `VillagerTradeEvents` 五个 `XP_LEVEL_n` 常量 | #13、#18 |
| [ ] | F-319 | `BotanistEpicTrade.java:28`、`:45`、`:46` 三行同批改 | #15、#16 |
| [ ] | F-320 | `BotanistTrade` record 加 `xp`，22 项逐项带值（6 + 6 + 6 + 4 = 22） | #14、#16 |
| [ ] | F-321 | `TierTrade` record 加 `xp`，三档 + `of` 第四参 | #16 |
| [ ] | F-322 | `CoreHudRenderer.java:40` 改 `0x4D000000` | #19、#20 |
| [ ] | F-323 | `FarmstormTags.java:29` 注释写「2 个成员」 | —（#22 的 14 个路径之一） |
| [ ] | F-324 | `CropConverterBlock.java:121-122`、`CultivatorBlock.java:117-118` | #21、#25 |
| [ ] | F-325 | `OutpostCoreBlock.java:131-132`（手持 `BlockItem` 才让路） | #21 |
| [ ] | F-326 | 新建 `.work/v024-verify/acceptance.md`，25 行读数 | #1 ~ #25 全量 |
| [ ] | F-327 | 新建 `.work/v024-verify/signoff.md` + `README.md` 增量段 | —（已知影响 ×4 / 未实测 ×3 / 十项读数） |

读表：25 条的逐条落点见 61 号表 A.2；本清单按任务勾选，#1 ~ #25 全部被至少一个任务勾住，其中 #3、#16、#21 一条挂多任务。F-327 那一行必须成文三件东西——已知影响 4 条（59 号 §7.1 ~ 7.4，逐条标「不是缺陷」）、未实测 3 项（D-252：「只有放置者听不见」/ 副手放置 / 客户端 `getRecipeManager()` 可用性）、十项不动读数（Java 151、注册面 92、server 175、client 3、语言键 321 × 2、`configuration` 178、网络包 2、进度 16、转换配方 21、交易 listing 26）。

---

## 6. 冲突处理

裁决顺序固定：**决策 > 技术 > 任务**，即 58 号（D-225 ~ D-252）> 60 号 > 61 号。执行时只用下面三条，不自创第四条。

| # | 冲突形状 | 处置 | 报给作者的内容 |
|---|---|---|---|
| 1 | 58 号与 60 号不一致 | 按 58 号落地 | 列 60 号的原句与它引用的行号，问作者改哪一侧 |
| 2 | 60 号与 61 号不一致 | 按 60 号落地 | 列任务边界与 §4 规格的差，任务号照旧不改 |
| 3 | 源码与任何上游不一致 | 停下，不落地 | 并列 `文件:行号` 与上游编号，由作者裁 |

登记格式固定一行：冲突主题 / 上游 A 的说法（编号）/ 上游 B 的说法（编号）/ 源码读数（`文件:行号`）/ 需要作者裁定的问题。三条禁令：不自行取舍、不把冲突当笔误、不改任何上游文件（只引上游）。

---

## 7. 回报口径

每次交付固定四段，顺序不变，缺一段视为未交付。

- **结论**：一句，给任务号 + 验收编号 + 达标与否；不写过程。
- **证据**：命令原文 + 关键输出 + 编号。四类证据各自要读数——grep 命中数、`/data get` 读数、日志行原文、像素 alpha；每条附 `文件:行号` 或验收 #。
- **未决问题**：只列需要作者裁定的项，每条给冲突形状与两个候选；没有就写「无」，不写推测。
- **下一步**：下一个任务号 + 它依赖的前置是否已落地（依赖见 61 号 §1 的「依赖」列）。

三条回报禁令：不把静态读链写成实测；不写「应该」「大概」一类推测词；不只给结论不给读数。

---

## 8. 自检（本文对结构要求的逐条落点）

| # | 要求 | 读数 |
|---|---|---|
| 1 | 只发 HC 与 N 两个系列 | HC-173 ~ HC-188 共 16 条，N-155 ~ N-166 共 12 条，两系列连续无跳号 |
| 2 | 其余系列写全前缀 | D-225 起 / B-59 起 / F-312 起 / Phase 66 起 / M62 起 / RT-83 起 / A-102 起 / E-66-1 起，未认领下游编号 |
| 3 | 事实卡 15 ~ 20 行且每行带 `文件:行号` | 18 行，逐行带出处 |
| 4 | 陷阱 ≥ 24 条且每条四栏 | 26 条，症状 / 原因 / 正确做法 / 依据齐全 |
| 5 | 交付清单对齐两个上游 | 16 个任务（F-312 ~ F-327）+ 25 条验收（#1 ~ #25）全覆盖 |
| 6 | 判据只可观察 | 全文只用计数、日志行、`/data get`、grep 命中、像素 alpha 五类 |
