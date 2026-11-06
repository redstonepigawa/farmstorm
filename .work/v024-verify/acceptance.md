# F-326 · 60 号 §8 的 25 条验收执行记录（v0.2.4）

> 执行者：验收执行子代理（调度者 session-b777045e-99f1-48ef-93c9-a66b94ac60e6）｜工作目录 `D:\project_farm`
> 依据：`docs/zh/60-TECH-0.2.4.md` §8（25 条）、§9（RT-83 ~ RT-91）；`docs/zh/61-TASKS-0.2.4.md` F-326 行；`docs/zh/63-PROMPT-TASKS-0.2.4.md` Phase 72（E-72-1 ~ E-72-6）
> 基线（开工实测，非引用）：build 退出码 **0**；检查器 **FAIL 0 / WARN 1**、生成 toml 键 **175**、注册面 **92 / 92**、语言键 **321**；Java 文件 **151**；`farmland`（区分大小写）**2**（只在 `ModLangProvider.java:39-40`）
> 边界自检：`src/`、`tools/`、`docs/` 一行未改；本任务只**新建** `.work/v024-verify/` 下的 `acceptance.md`、`f326-cmds-*.txt`、`f326-src-count.ps1` 与 `logs/f326-*.log`；未跑 add / commit / checkout / stash / diff，只跑过一次只读 `git status --porcelain`（第 22 条要求）。

---

## 0. 结论分布（25 条）

| 结论 | 条数 | 编号 |
|---|---|---|
| 通过 | **22** | 见下方逐条清单 |
| 不通过 | **1** | #22 |
| 未验证（无图形客户端 / 无双客户端录音） | **2** | #20、#25 |

逐条结论：

~~~text
#1  通过      #2  通过      #3  通过      #4  通过      #5  通过
#6  通过      #7  通过      #8  通过*     #9  通过      #10 通过
#11 通过      #12 通过      #13 通过      #14 通过      #15 通过*
#16 通过      #17 通过      #18 通过*     #19 通过      #20 未验证
#21 通过      #22 不通过    #23 通过      #24 通过      #25 未验证

（* = 字面判据与实际口径有偏差，读数与偏差逐条写在正文，登记见 §5）
通过 22 + 不通过 1 + 未验证 2 = 25
~~~

分类计数（按 61 号 F-326 / 63 号 E-72-1 的点名编号实点）：源码类 **14** = #4 #5 #6 #7 #8 #9（6）+ #13 #14 #15 #16（4）+ #18 #19（2）+ #21 #23（2）；构建与检查器类 **5** = #1 #2 #3 #22 #24；进游戏类 **6** = #10 #11 #12 #17 #20 #25。`14 + 5 + 6 = 25`。

---

## 1. 源码类（14 条，给 grep 命中数）

口径：整文件**子串出现次数**（`Index.Ordinal`，区分大小写）与命中**行数**两列并列；用 `[IO.File]::ReadAllLines(p, [Text.Encoding]::UTF8)` 读全文后在内存里数，不经 `git`。原始输出：第一遍 `logs/f326-src-count.log`（含 #3 全树扫描、#4 ~ #9、#13 ~ #16、#18 的 occ/lines 两列）、第二遍 `logs/f326-src-count2.log`（#19 / #21 / #23 的行原文、#15 常量声明、#8 映射块、#18 农民段、#25 播放面扫描）。

### #4 `CoreBound.java` 的三个方法签名

- **动作**：`blockentity/CoreBound.java` 数 `BlockPos corePos();`、`void setCore(BlockPos pos);`、`void clearCore();`，再数第四方法 `kind(`。
- **读数**：三签名各 `occ=1 lines=1`；`kind(` `occ=0`；文件 24 行。
- **结论**：**通过**（第四方法命中 0）。
- **证据**：`logs/f326-src-count.log` 段 `### #4`。

### #5 `CropConverterBlockEntity.java` 的 `syncCore` / `sendBlockUpdated` / `Block.UPDATE_CLIENTS`

- **动作**：同文件数三串。
- **读数**：`private void syncCore` `occ=1 lines=1`；`sendBlockUpdated` `occ=1 lines=1`；`Block.UPDATE_CLIENTS` `occ=2 lines=2`（`:250` `setWorking` 的 `server.setBlock(..., Block.UPDATE_CLIENTS)` 与 `syncCore` 那处）。`import net.minecraft.world.level.block.Block;` 在 `:23`，一处 import 支撑两处使用。
- **结论**：**通过**（RT-87 的「只写调用不写方法」未复现：定义 1 + 同步 1）。
- **证据**：同上段 `### #5`。

### #6 `OutpostCoreBlockEntity.java` 的 `AreaBlockEntities.collect` 与 `CultivatorBlockEntity.class`

- **动作**：同文件数两串。
- **读数**：`AreaBlockEntities.collect` `occ=1 lines=1`（`:621`）；`CultivatorBlockEntity.class` `occ=0 lines=0`。
- **结论**：**通过**（按标签一次发现，旧的按类枚举已清）。
- **证据**：`### #6`。

### #7 `rebindNearby` 与 `settleOnRemoval` 两方法体内的 `instanceof CultivatorBlockEntity`

- **动作**：同文件数 `instanceof CultivatorBlockEntity`（方法体是文件子集，全文件 0 ⇒ 两方法体内必为 0）。
- **读数**：`occ=0 lines=0`。两方法体内改的是 `instanceof CoreBound bound`（`:632`）与 `instanceof CoreBound machine`（`:857` `:874`）。
- **结论**：**通过**。
- **证据**：`### #6/#8/#9` 段的 `#7` 行。

### #8 `KIND_BY_BLOCK` 与其中的两个 `Link.Kind` 值

- **动作**：同文件数 `KIND_BY_BLOCK`、`KIND_BY_BLOCK = Map.of(`、`Link.Kind.CULTIVATOR`、`Link.Kind.CROP_CONVERTER`；再逐行读 `:123-125`。
- **读数**：`KIND_BY_BLOCK` `occ=2 lines=2`（`:123` **声明** + `:635` **取值**）；`KIND_BY_BLOCK = Map.of(` `occ=1`；映射块内 `Link.Kind.CULTIVATOR` 1（`:124`）、`Link.Kind.CROP_CONVERTER` 1（`:125`）。全文件 `Link.Kind.CULTIVATOR` 3 处（`:80` 注释、`:124` 映射、`:968` 注释）。
- **结论**：**通过（口径偏差已登记）**：判据「`KIND_BY_BLOCK` 命中 1」按**声明处**成立（`:123` 唯一，未出现第二张映射表）；字面全文件命中是 **2**（多出的一处是读取点 `:635`）。两值在映射行内各 1 与判据逐字相符。登记见 §5 第 2 条。
- **证据**：`logs/f326-src-count.log` 段 `### #6/#8/#9` 与 `logs/f326-src-count2.log` 段 `### #8`（`:123-125` 三行原文）。

### #9 `OutpostCoreBlockEntity.java` 的 `clearCore()` 调用点与四处文案

- **动作**：数 `clearCore()`；数四个串（两个旧串、两个新串、一个不许动的串）；读 `:857-877`。
- **读数**：`clearCore()` `occ=1 lines=1`（`:875` `machine.clearCore();`）。旧串「迁移失败：培养器留原地」**0**；新串「迁移失败：标签方块留原地并标为未绑定」**1**（`:876`）。旧串「迁移失败且该格已不是培养器」**0**；新串「迁移失败且该格已不是处理方块」**1**（`:871`）。「迁移失败且区块未加载」**1**（`:867`，未动）。`:863` 的解释性注释为「以下都是「没有可用的接替核」：先看该格读不读得到、还是不是处理方块（都不动世界）」——本行「培养器」**0**。
- **结论**：**通过**。行号按现文件：判据写的 `:859/:863/:867/:872` 现为 `:863/:867/:871/:876`，四处文案的**内容**按判据逐项成立。全文件「培养器」18 行是注释里的通用说法（名额 / 候选 / 迁移说明），不在判据范围内。
- **证据**：`### #6/#8/#9` 段的 `--- lines containing 培养器` 清单 + `read` `:840-890`。

### #13 `VillagerTradeEvents.java` 的五个常量

- **动作**：数 `XP_LEVEL_1 = 2` / `XP_LEVEL_2 = 5` / `XP_LEVEL_3 = 10` / `XP_LEVEL_4 = 15` / `XP_LEVEL_5 = 30`；旁证 `private static final int`。
- **读数**：五个串各 `occ=1 lines=1`；`private static final int` `occ=0`（即五个常量都是 `public static final`，RT-89 的可见性未错）。
- **结论**：**通过**。
- **证据**：`### #13 VillagerTradeEvents.java`。

### #14 `BotanistTrade.java` 里五个常量的引用次数

- **动作**：数 `XP_LEVEL_1` ~ `XP_LEVEL_5`。
- **读数**：**6 / 6 / 6 / 4 / 0**，前缀四项 `6 + 6 + 6 + 4 = 22`（= `NORMAL` 的 22 个实例逐项带值）。
- **结论**：**通过**（判据只要求前四项相加 22）。
- **证据**：`### #14 BotanistTrade.java`。

### #15 `BotanistEpicTrade.java` 的 `VILLAGER_XP` 与字面量 `= 2`

- **动作**：数 `VILLAGER_XP = VillagerTradeEvents.XP_LEVEL_5`；数全文件 `= 2`；再按正则 `static\s+final[^;]*=\s*2\s*;` 数常量声明。
- **读数**：新串 `occ=1`（`:46`）；全文件 `= 2` `occ=1`，那 1 行是 `:39` 的价格上界注释 `/** 价格上界：64 − 40 + 1 = 25 个取值。 */`；**常量声明里 `= 2` 命中 0**（`:38 :40 :42 :44 :46 :48 :51` 七个 `static final` 无一是 2）。
- **结论**：**通过（口径登记）**：判据写的是「该类里 `= 2` 的**常量**命中 0」——按常量声明成立；按裸子串是 1（那一处是价格注释里的 `... + 1 = 25`，被 `= 2` 这个子串误中）。登记见 §5 第 3 条。
- **证据**：`logs/f326-src-count.log` 段 `### #15/#16`（裸子串 1 + 那 1 行原文）与 `logs/f326-src-count2.log` 段 `### #15`（常量声明 0 + 七个 `static final` 全文）。

### #16 `VILLAGER_XP` 在三类里的命中数

- **动作**：三文件各数 `VILLAGER_XP`。
- **读数**：`BotanistTrade` **0**、`TierTrade` **0**、`BotanistEpicTrade` **2**（`:46` 声明 + `:64` `MerchantOffer` 实参）。旁证：该类 `XP_LEVEL_5` `occ=1`。
- **结论**：**通过**（第二份真相已清，RT-89 未复现）。
- **证据**：`### #15/#16`。

### #18 `VillagerTradeEvents.java` 的四个切片与三个挂载点

- **动作**：数 `subList(0, 6)` / `(6, 12)` / `(12, 18)` / `(18, 22)`，再数 `get(1)` / `get(3)` / `get(5)`；读 `:56-69`。
- **读数**：四个切片各 `occ=1`（`:56-59`）；农民段 `:67-69` 三条挂载 `get(1)` / `get(3)` / `get(5)` 各 **1**。**全文件** `get(1)` / `get(3)` / `get(5)` 各 **2**（植物学家段 `:56` `get(1)`、`:58` `get(3)`，史诗段 `:61` `get(5)`）。
- **结论**：**通过（口径登记）**：判据的「三个挂载点各 1」按农民段成立；全文件 2 是因为植物学家段的 `get(1) get(2) get(3) get(4)` 与史诗段的 `get(5)` 用了同一批下标。`NORMAL` 的 22 项顺序未重排（RT-90 未复现）：四个切片边界逐字为 `(0, 6)` `(6, 12)` `(12, 18)` `(18, 22)`。登记见 §5 第 4 条。
- **证据**：`logs/f326-src-count.log` 段 `### #18` 与 `logs/f326-src-count2.log` 段 `### #18 farmer-section get(n) (lines 62-71)`。

### #19 `CoreHudRenderer.java` 的三个颜色常量

- **动作**：数 `0x4D000000` / `0x80000000` / `0xFF000000`；读 `:40-44` 与 `:77-78`。
- **读数**：`0x4D000000` `occ=1`（`:40` `COLOR_BACKGROUND`）；`0x80000000` `occ=0`；`0xFF000000` `occ=1`（`:41` `COLOR_BORDER`）。绘制两行：`:77` 先铺整块 `COLOR_BORDER`，`:78` 再在内缩 1 px 的矩形铺 `COLOR_BACKGROUND`。
- **结论**：**通过**。
- **证据**：`logs/f326-src-count2.log` 段 `### #19`（五行 0x 常量 + 两行 fill 原文）。

---

### #21 三个 `block/` 文件的客户端分支与 `canInsert`

- **动作**：逐文件读 `isClientSide` / `canInsert` 的全部行；核对客户端分支紧跟的谓词。
- **读数**：
  - `CropConverterBlock.java`：`:122` `if (level.isClientSide)` → `:123-124` 用 **`convertible`**；`canInsert` 全文件 **1** 处（`:133`，在 `:128` 客户端分支**之外**）。
  - `CultivatorBlock.java`：`:118` `if (level.isClientSide)` → `:119-120` 用 **`admissible`**；`canInsert` 全文件 **1** 处（`:129`，在 `:124` 分支之外）。
  - `OutpostCoreBlock.java`：`:132` `if (level.isClientSide)` → `:133` 用 **`BlockItem`**（手持方块 ⇒ `PASS`）；`canInsert` 全文件 **0**。
  - 三个文件另各有 1 处 `isClientSide` 在 `getTicker` 里（CropConverter `:162`、Cultivator `:159`、OutpostCore `:68`），返回 `null`，与放入动作无关。
- **结论**：**通过**：客户端分支内 `canInsert` 命中 **0**；全文件保留的服务端调用是 1 / 1 / 0，与判据的三档数值逐项相等。
- **证据**：`logs/f326-src-count2.log` 段 `### #21`（三文件全部相关行原文）。

### #23 `FarmstormNetwork.java`

- **动作**：数 `PROTOCOL_VERSION = "3"` 与 `.playToClient(`。
- **读数**：`PROTOCOL_VERSION = "3"` `occ=1`（`:29`）；`.playToClient(` `occ=2`（`:42` `CoreListPayload`、`:47` `CultivatorLinkPayload`）。
- **结论**：**通过**（网络包仍 2 个）。
- **证据**：`logs/f326-src-count2.log` 段 `### #23`。

---

## 2. 构建与检查器类（5 条，给命令输出原文）

### #1 检查器（`python -X utf8 tools/verify_project.py`）

- **动作**：在仓库根跑检查器，全程唯一一次；EXITCODE **0**。
- **读数**（原文）：
~~~text
== 1. config：文档键集 vs 生成的 toml ==
  [WARN] 6 个键在文档里是逻辑路径、落盘是组内拼写（形状归一后才对上，已登记为文档-源码偏差）: ['trade.common.max -> trade.commonMax', ...]
  [OK]   175 个配置键与文档逐字一致（toml 175 键）
== 2. 92 个注册 ID 逐字核对（全源码树） ==
  已核 registry 对象: 92 / 92
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 321 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch
结论：FAIL 0 项，WARN 1 项
~~~
  §1 段另有独立一行 `生成 toml 键数: 175`，与 `[OK] 175 个配置键...` 互为旁证。
- **结论**：**通过**（FAIL 0 / WARN 1；生成 toml 175 键）。
- **证据**：`logs/f326-check.log`（全文）。WARN 1 = 基线那条 `trade.*.min/max` 的拼写形状，非本轮引入。

### #2 专用服务器首启日志里的 `自检 注册对象`

- **动作**：`$env:FS_RUN_LOCK='run-v024'` + `gw.cmd runServer -PfsRunDir=run-v024`（`run-v024/server.properties`：`server-port=25599`、`enable-rcon=true`、`rcon.port=25575`、`rcon.password=fsverify`），grep 日志。
- **读数**（原文，**恰 1 行**）：
~~~text
[19:12:24] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 92 个：items 28 / blocks 28 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK
~~~
  就绪行：`Done (5.870s)! For help, type "help"`；`RCON running on 0.0.0.0:25575`。
- **结论**：**通过**（1 行、含 `92 个`、十二类数值齐全、行尾 `OK`）。
- **证据**：`logs/f326-server.log`（该串 grep 命中数 = 1）。

### #3 文件数 151 与两条 farmland 读法

- **动作**：`(Get-ChildItem -Recurse src\main\java -Filter *.java -File).Count`；逐文件按 `Contains('farmland')`（区分大小写）扫全树；再数 `outpostCoreFarmlandBonus`。
- **读数**：
~~~text
java file count = 151
src\main\java\com\dashpig\farmstorm\datagen\ModLangProvider.java:39: * v0.1.1 的 F-192 / F-197 再 **−2**：{@code farmstorm.configuration.farmland.limitPerPlayer} 随
src\main\java\com\dashpig\farmstorm\datagen\ModLangProvider.java:40: * {@code farmland} 整组删除、指令反馈键随整棵服务端指令树删除，到 **314** 键。
  total (outpostCoreFarmlandBonus) = 0
~~~
- **结论**：**通过**（151；第一条 grep 恰 **2** 行且都在 `:39` `:40`；第二条 **0**）。
- **证据**：`logs/f326-src-count.log` 段 `### #3`。旁证（也用于第 22 条）：24 个目录、`blockentity/` 下 5 个文件、`farmland/` 目录 `Test-Path = False`。

### #22 `git status --porcelain` 全量过一遍（**不通过**）

- **动作**：只读跑一次 `git status --porcelain`，统计总行数、`src/` 命中、`.java` 路径、`src/main/java/com/dashpig/farmstorm/` 命中、资源面命中。
- **读数**：
~~~text
total porcelain lines = 1491
lines mentioning src/ = 4        （全部是 .work/ 下的 *-src/ 目录：f127-src / p34-src / p35_src / p38-src）
paths ending .java (whole repo) = 3   （.work/DeathFmt.java、.work/_idx_base.java、.work/f118-clean.java）
.java under src/main/java/com/dashpig/farmstorm = 0
src/main/resources + src/generated/resources hits = 0
~~~
- **结论**：**不通过**（判据要求「出现的路径恰 **19** 个 `.java`，三段 14 + 3 + 2」，实测 **0**）。
- **原因（事实，不是推测）**：本版 19 个路径对应的改动——F-328 的 2 删 + 3 改、F-312 ~ F-325 的 12 改 + 1 新增 + 1 只加方法——**已经提交**：`.work/v024-commit-1.txt` ~ `v024-commit-6.txt` 是那几笔提交的说明稿；工作树里 `CoreBound.java`（24 行）在、`farmland/` 目录已不存在、Java 文件 151；而 1491 行 `git status` 里没有一条 ` M ` / ` D ` 形态的 `src/` 记录。因此这条判据在本状态下**产生不出 19**：它不是「没测」，是「读数与判据不符」。
- **两条子断言的分项结论**：`src/main/resources/` 与 `src/generated/resources/` 命中 **0** —— **通过**；「19 个路径全部以 `src/main/java/com/dashpig/farmstorm/` 开头」—— 空集，**无法成立**。
- **替代读数（可复核，都不依赖 git 写操作）**：Java 文件 **151**（`152 − 2 + 1` 的终值）；`blockentity/CoreBound.java` 存在且 24 行；`farmland` 目录不存在（两个删除项确已消失）；`blockentity/` 下 5 个文件。14 + 3 + 2 的**逐路径构成**需 `git show --stat <提交>` 才能复核，本任务禁止 git 写/读历史命令，故未取。
- **证据**：`logs/f326-git-status.log`（1491 行原文）。
- **给调度者的处置建议**：① 若接受「改动已提交」，本条应改判为「以 `git show --stat` 复核 19 个路径」或「HEAD 与工作树一致即通过」；② 若坚持 `git status` 的 19，只能在提交前跑——**当前状态不可回溯**。本条**未改判据、未删改任何源码**。

### #24 配置段/键与两份语言键

- **动作**：数 `run-v024/config/farmstorm-server.toml`（判据写 `run/config/...`，两份都数）的 `[段]` 行与 `键 = 值` 行；两份 lang 用 `ConvertFrom-Json` 数键与 `farmstorm.configuration.` 前缀。
- **读数**：
~~~text
TOML D:\project_farm\run-v024\config\farmstorm-server.toml : lines=803 sections=[60] keyValueLines=[175]
TOML D:\project_farm\run\config\farmstorm-server.toml       : lines=803 sections=[60] keyValueLines=[175]
LANG ...\assets\farmstorm\lang\en_us.json : keys=321  farmstorm.configuration.*=178
LANG ...\assets\farmstorm\lang\zh_cn.json : keys=321  farmstorm.configuration.*=178
~~~
- **结论**：**通过**（60 段 / 175 行 / 321 × 2 / 178，五项全中；两份 toml 同值说明 `run/` 与 `run-v024/` 未分叉）。
- **证据**：内联命令原文即上表，读数已抄录在此（无独立日志文件）。

---

## 3. 进游戏类（6 条）

服务器只起 **1 次**（19:12:17 ~ 19:15:55，`run-v024`，端口 25599 / RCON 25575），三类场景串在同一次会话里；结尾优雅 `stop`，停后 `Get-Process java` = **0**、25575/25599 无 LISTENING。

### #10 拆掉转换器所绑的核，读 `Core`

- **动作**：`forceload add 384 384 447 447` → 在 `400 100 400` 放核、`404 100 400` 放转换器 → `data merge block 400 100 400 {ownerUuid:[I;16909060,84281096,151521032,218761968],ownerName:"f326v",Cultivators:[111050676043876L],CultivatorKinds:[1]}` 与 `data merge block 404 100 400 {Core:109951164416100L,Progress:0,Held:{id:"minecraft:wheat",count:1}}` → `setblock 400 100 400 minecraft:air` → 读转换器。
- **读数**（RCON 原文）：
~~~text
拆前 data get block 404 100 400 Core  -> 404, 100, 400 has the following block data: 109951164416100L
拆前 ... Held                        -> {count: 1, id: "minecraft:wheat"}
拆前 ... Progress                    -> 61
拆后 data get block 404 100 400 Core  -> Found no elements matching Core
拆后 ... Held                        -> {count: 1, id: "minecraft:wheat"}
拆后 ... Progress                    -> 68
日志 [19:13:49] [farmstorm/]: 迁移失败：标签方块留原地并标为未绑定 BlockPos{x=404, y=100, z=400}
~~~
- **结论**：**通过**（`Core` 键消失 = 未绑定；`Held` / `Progress` 仍在；出口② 的日志 1 行）。
- **证据**：`logs/f326-bind2.log`（RCON 全文）、`logs/f326-server.log`（迁移日志）。
- **环境事实（写清，避免误读）**：`/setblock` 出来的核**没有归属者**。第一轮（不带 `ownerUuid`）拆核后 `Core` **仍是** `109951164416100L`、日志 0 行——原因是 `settleOnRemoval()` 第一行 `ownerUuid == null` 就早退（裁定 #13：无主核不迁移、不补绑）。这一次不是缺陷，是把「归属者」当成了场景前置条件；带 `ownerUuid` 后两条路径都按设计触发。原始对比见 `logs/f326-bind.log` 与 `logs/f326-bind2.log`。

### #11 3 × 3 区块内放新核，等一个补绑周期

- **动作**：`setblock 410 100 400 farmstorm:outpost_core`（同区块 (25,25)，在 `RANGE_CHUNKS = 1` 的 3 × 3 内）+ `data merge` 写归属者，等 **14 秒（280 刻 ≈ 2.8 个 100 刻周期）**。
- **读数**（日志原文，**恰 1 行**）：
~~~text
[19:13:57] [Server thread/INFO] [farmstorm/]: 补绑：核 BlockPos{x=410, y=100, z=400} 本轮新增 1 条（累计 1/24，周期 100 刻）
~~~
  「累计」由 0 → **1**。同一时段另有上一轮存档遗留的核 `BlockPos{x=205, y=100, z=200}` **没有**补绑日志。
- **结论**：**通过**（本轮新增 1 条、累计 1/24）。
- **证据**：`logs/f326-server.log`；`补绑` 全文命中 = 1 行。

### #12 串起 #10 与 #11 再读 `Core`

- **动作**：接上两条之后读转换器的 `Core`、新核的 `Cultivators` / `CultivatorKinds`。
- **读数**（RCON 原文，A → D → B 顺序）：
~~~text
A（拆核前）  Core = 109951164416100L        （= BlockPos(400,100,400).asLong）
D（拆核后）  Core = Found no elements matching Core
B（补绑后）  Core = 112699943485540L        （= BlockPos(410,100,400).asLong）
新核 410 100 400 Cultivators      = [111050676043876L]   （= 转换器 (404,100,400).asLong）
新核 410 100 400 CultivatorKinds  = [1]                  （CROP_CONVERTER）
新核 410 100 400 ownerUuid        = [I; 16909060, 84281096, 151521032, 218761968]
~~~
  长整数按 `((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF)` 复算：`400/100/400 → 109951164416100`、`410/100/400 → 112699943485540`、`404/100/400 → 111050676043876`（三处逐位可复算）。
- **结论**：**通过**（读数等于新核坐标；核侧两条平行列表 `[111050676043876L]` / `[1]` 与机器侧缓存互为一致）。
- **证据**：`logs/f326-bind2.log`。

### #17 `/data get entity <村民> Offers` 按 `xp:` 分组

- **动作**：`forceload add 480 480 543 543` → 按 profession + level 批量召唤（植物学家 1/2/3/4/5 级 = 8/8/24/8/8 只，农民 1/3/5 级各 24 只，共 138 只）→ 每组 `execute as @e[tag=<T>] run data get entity @s Offers`。
- **读数**（汇总，逐组原始见 `logs/f326-offers-agg.log`）：

  | 组 | 召出 | Offers 条数 | xp 直方 | distinct listing（sell, xp） |
  |---|---|---|---|---|
  | b1（植物 1） | 8 | 16 | xp=2:16 | **6** = wheat / carrot / potato / poisonous_potato / beetroot / melon_slice |
  | b2（植物 2） | 8+10 | 35 | xp=5:35 | **6** = melon / pumpkin / torchflower / pitcher_plant / cactus / bamboo |
  | b3（植物 3） | 24 | 48 | xp=10:48 | **6** = red_mushroom / brown_mushroom / sweet_berries / crimson_fungus / sugar_cane / golden_apple |
  | b4（植物 4） | 8 | 16 | xp=15:16 | **4** = compressed_puffed_wheat / plain_chives / cocoa_beans / warped_fungus |
  | b5（植物 5） | 8 | 8 | xp=30:8 | 史诗 **1** 条（4 种抽签结果 lucky_berry / wild_apple / awakened_torchflower / awakened_pitcher_plant，同 xp=30） |
  | f1（农民 1） | 24 | 10 条本模组项 | xp=2:10 | 价格 2~5（签名 `maxUses=1` ∧ `sell.count=2` ∧ `buy=emerald`） |
  | f3（农民 3） | 24 | 11 条本模组项 | xp=10:11 | 价格 10~30 |
  | f5（农民 5） | 24 | 15 条本模组项 | xp=30:15 | 价格 43~64 |

  xp 分组 tally（判据口径：植物学家每级 listing 数由代码切片给定，史诗 1 条，农民三档各 1 条）：
~~~text
xp:2  = b1(6) + 农民 COMMON(1)        = 7
xp:5  = b2(6)                         = 6
xp:10 = b3(6) + 农民 RARE(1)          = 7
xp:15 = b4(4)                         = 4
xp:30 = 植物学家史诗(1) + 农民 EPIC(1) = 2
合计 = 7 / 6 / 7 / 4 / 2 = 26
~~~
- **结论**：**通过**（`7 + 6 + 7 + 4 + 2 = 26`）。判据明写「只依赖 `Offers` 的读数，服务端单人即可复现」，本读数即为该形状。
- **证据**：`logs/f326-offers.log`（8 组原始回复，`xp:` token 共 237）、`logs/f326-offers-b2.log`（b2 补 10 只后的第 2 次读数）、`logs/f326-offers-agg.log`（聚合输出，含逐组 distinct 与 tally）。
- **口径边界（不藏）**：游戏里一个村民只存**被抽到那几项**，所以「26 条 listing 的行级总数」不可能由单条命令直读；「该级挂几条」由代码切片（22 = 6 + 6 + 6 + 4）+ 史诗 1 + 农民 3 承担，「每条被抽到的项都带本级那个唯一的 xp」由 138 只村民、237 个 xp token 的实测承担。b2 第一次只覆盖 5/6（缺 `torchflower`），补 10 只后 6/6。

### #20 面板截图取色（内层 1 px 与边框 1 px）

- **结论**：**未验证**。原因：需要**图形客户端**（截图后按像素取 alpha），本会话无客户端、无显示设备，起不了 Minecraft 客户端。
- **替代读数（常量 + 绘制顺序，可复核）**：
  - `CoreHudRenderer.java:40` `private static final int COLOR_BACKGROUND = 0x4D000000;` ⇒ alpha = `0x4D` = **77**（77 / 255 = **30.196%**）；
  - `:41` `private static final int COLOR_BORDER = 0xFF000000;` ⇒ alpha = `0xFF` = **255**；
  - 绘制顺序 `:77` 先铺整块边框色、`:78` 再在内缩 1 px 的矩形铺底色 ⇒ **内层像素取到底色（alpha 77）、边框 1 px 环取到边框色（alpha 255）**，与判据期望的 77 / 255 同构；
  - 旁证：`0x80000000`（v0.2.3 的不透明底）全文件 **0**（见 #19）。
- **证据**：`logs/f326-src-count2.log` 段 `### #19`（`:40` `:41` 常量行与 `:77` `:78` 两行 fill 原文）。

### #25 双客户端录屏，A 手持泥土右键培养器顶面

- **结论**：**未验证**。原因：判据要 **A / B 两个客户端各一条音频轨**（录屏取 `block.anvil.place` 命中次数），本会话只有一个无头服务端，没有第二台机器、没有图形客户端、没有音频采集。
- **替代读数（三处客户端分支 + 播放面计数，可复核）**：
  - 三处客户端分支逐字在（见 #21 段）：`CropConverterBlock:122`（`convertible` ⇒ 非输入物 `PASS`）、`CultivatorBlock:118`（`admissible` ⇒ `PASS`）、`OutpostCoreBlock:132`（`BlockItem` ⇒ `PASS`）；三处 `PASS` 的注释均写明「让路，别掐断原版的本地放置音（D-243 / D-245）」。
  - `src/main/java/com/dashpig/farmstorm/block/` 下 `playLocalSound` / `playSound` / `SoundEvent` / `SoundEvents` 命中 **0** —— 本模组不在服务端补播、也不在客户端自播，净效果完全由「返回 PASS 让原版流程继续」承担（与 D-243 / D-246「服务端一律不改」一致）。
  - 注册面 `sound_events = 3`（检查器 §2 原文），本版未新增音效（D-246 禁令）——旁证「没有新增播放点」。
- **证据**：`logs/f326-src-count2.log` 段 `### #21` 与 `### #25 sound API scan under block/`（`total sound-API hits under block/ = 0`）；`logs/f326-check.log` 的 `sound_event: 3 个 ID 齐全`。
- **按 D-252 的登记口径**：本条与 RT-88（「只有放置者听不见」/「副手放置」）同属**未实测**，不得写成已验证。

---

## 4. 跑过的命令清单（全部可重跑）

| # | 命令（原样） | 结果 |
|---|---|---|
| 1 | `cmd /c "D:\project_farm\.tools\gw.cmd" build` | EXITCODE **0**；`BUILD SUCCESSFUL in 8s` / `5 actionable tasks: 5 up-to-date`（全任务 up-to-date = 工作树内容与上次编译一致）→ `logs/f326-build.log` |
| 2 | `& '...\python.exe' -X utf8 tools/verify_project.py` | EXITCODE **0**；`结论：FAIL 0 项，WARN 1 项`、`92 / 92`、`321 键`、`生成 toml 键数: 175` → `logs/f326-check.log` |
| 3 | `git status --porcelain`（只读，唯一一次） | **1491** 行；`src/main/java/com/dashpig/farmstorm/` 命 **0** → `logs/f326-git-status.log` |
| 4 | `$env:FS_RUN_LOCK='run-v024'; cmd /c "...gw.cmd" runServer -PfsRunDir=run-v024` | 1 次起服；`Done (5.870s)!`、`RCON running on 0.0.0.0:25575`、`自检 注册对象 92 个 ... → OK` → `logs/f326-server.log` |
| 5 | `python.exe -X utf8 .work/v023-verify/rcon.py 25575 .work/v024-verify/f326-cmds-bind.txt` | 第一轮（无归属者）对照：拆核后 `Core` 未消失、无迁移日志 → `logs/f326-bind.log` |
| 6 | 同上，`f326-cmds-bind2.txt` | **#10 / #11 / #12 主读数** → `logs/f326-bind2.log` |
| 7 | 同上，`f326-cmds-offers-setup.txt`（129 条 summon） | `Summoned new Botanist / Farmer`，失败 0 → `logs/f326-offers-setup.log` 末 6 行 |
| 8 | 同上，`f326-cmds-precheck.txt`（补 `forceload add 480 480 543 543`） | `Marked 16 chunks`，随后 `execute as @e[tag=b1]` 读到 8 只（首轮读数为空的原因：村民站在**未 forceload** 的区块里，选择器找不到） |
| 9 | 同上，`f326-cmds-offers-read.txt` | 237 个 `xp:` token → `logs/f326-offers.log` |
| 10 | 同上，`f326-cmds-b2.txt`（b2 补 10 只 + 重读） | b2 distinct 由 5 → **6**（补上 `torchflower`）→ `logs/f326-offers-b2.log` |
| 11 | 同上，`f326-cmds-stop.txt`（`kill @e[type=minecraft:villager]` + `stop`） | `Killed 138 entities`、`All dimensions are saved`、`Thread RCON Listener stopped`；停后 java 进程 **0**、端口无 LISTENING → `logs/f326-stop.log` |

时间线（`logs/f326-server.log`）：`19:12:24` 自检 → `19:12:33` Done / RCON → `19:13:49` 迁移失败日志 → `19:13:57` 补绑日志 → `19:15:54` stop → `19:15:55` 全维度已保存、`BUILD SUCCESSFUL in 3m 51s`。

---

## 5. 与文档不一致 / 判据本身有问题的地方（只登记，未改）

1. **#22 的判据状态与当前工作树不匹配（最重的一条）**：判据假设 19 个路径是**未提交**改动；实测这 19 个路径的改动已提交（`.work/v024-commit-1.txt` ~ `v024-commit-6.txt` 为那几笔的说明稿），`git status --porcelain` 对 `src/` 报 0。判据要的读数在当前状态下**不可产生**：需改判为「`git show --stat` 复核 19 个路径」或在提交前跑。
2. **#8 的 `KIND_BY_BLOCK` 命中数**：判据写「命中 1」，字面实测 **2**（`:123` 声明 + `:635` 取值）。按「映射表唯一」的语义（D-235 / RT-86）成立；建议判据改为「`KIND_BY_BLOCK = Map.of(` 命中 1」。
3. **#15 的 `= 2`**：判据写「该类里 `= 2` 的常量命中 0」——按常量声明是 0；裸子串是 1，那一处是 `:39` 的价格注释 `64 − 40 + 1 = 25`（被 `= 2` 误中，且红线 6 不许改）。建议判据写「`VILLAGER_XP = 2` 命中 0」。
4. **#18 的 `get(1)` / `get(3)` / `get(5)`**：判据的「各 1」只在**农民段**成立；全文件各 **2**（植物学家段与史诗段复用同一批下标）。建议判据写「`:67-69` 三行各 1」或「全文件各 2」。
5. **#9 的行号全部平移**：判据写的 `:859` / `:863` / `:867` / `:872` 现为 `:863` / `:867` / `:871` / `:876`（后面插入的 javadoc 与 `KIND_BY_BLOCK` 块把行号推后）。四处**文案**成立，行号引用已失效。
6. **#10 / #11 缺一条前置条件**：判据没写「核必须有归属者」。按 `settleOnRemoval()` 与 `rebindNearby()` 的第一行（`ownerUuid == null` 早退，裁定 #13），`/setblock` 出来的无主核**两条路径都不触发**。第一轮就是这么失败的（`logs/f326-bind.log`：拆核后 `Core` 仍是 `109951164416100L`、日志 0 行）。建议判据补「用带 `ownerUuid` 的核，或由玩家放置」。
7. **63 号 E-72-3 与 60 / 61 号互不一致**：`docs/zh/63-PROMPT-TASKS-0.2.4.md:420` 写「出现的路径恰 **14** 个 `.java`（`12 + 1 + 1`）」，而 60 号 §8 #22 与 61 号 F-326 都是 **19**（`14 + 3 + 2`）。本记录按 60 / 61 的 19 执行（任务书亦给 19）。
8. **`run/config` vs `run-v024/config`（旁证，不影响结论）**：#24 判据写 `run/config/farmstorm-server.toml`，本任务的专用运行目录是 `run-v024/config/farmstorm-server.toml`；两份读数逐项相同（803 行 / 60 段 / 175 键），已并列给出。
9. **`echo` 不是服务端命令**：`/echo` 是数据包函数专用；RCON 里发 `echo ===X===` 一律回 `Unknown or incomplete command`（见各 `f326-cmds-*.txt` 的残留行）。不影响读数，但后续任务若用 `echo` 做分段标记要改法。
10. **RCON 分包会在字符串里插入空格**：b3 第一次聚合里出现 `"minecraft: sugar_cane"`（冒号后多一个空格），把它当独立值会多算 1 个 distinct（该组一度显示 7）。本记录已按去空格口径归一（6 个 distinct）。

---

## 6. 本任务产出的文件（全部新建，都在 `.work/v024-verify/` 下）

- `acceptance.md`（本文件）
- `f326-src-count.ps1`（源码计数脚本；本机 `ExecutionPolicy` 禁止跑 `.ps1`，最终以等价内联命令执行，脚本保留仅作口径留档；对应日志 `logs/f326-src-count.log` 由内联版产生）
- `f326-cmds-bind.txt` / `f326-cmds-bind2.txt`（#10 #11 #12 的两轮 RCON 脚本）
- `f326-cmds-offers-setup.txt` / `f326-cmds-offers-read.txt` / `f326-cmds-b2.txt` / `f326-cmds-precheck.txt` / `f326-cmds-stop.txt`（#17 的召唤、读数、补齐与停机脚本）
- `logs/f326-build.log`、`logs/f326-check.log`、`logs/f326-git-status.log`、`logs/f326-src-count.log`、`logs/f326-src-count2.log`、`logs/f326-server.log`、`logs/f326-bind.log`、`logs/f326-bind2.log`、`logs/f326-offers-setup.log`、`logs/f326-offers.log`、`logs/f326-offers-b2.log`、`logs/f326-offers-agg.log`、`logs/f326-stop.log`

未改：`src/`、`tools/`、`docs/`、`.work/` 既有文件、`dist/`。未跑 git 写操作。