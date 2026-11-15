# 11 号 v0.3.0（1.20.1）分阶段提示词（每个 Phase 投喂一节）

> **投喂方式**：先投 10 号一次（`docs/zh/10-PROMPT-MAIN-0.3.0-1.20.1.md`，HC / N 两系列与陷阱清单全程常驻）；此后每个 Phase 开工时**只投本文对应的那一节**——投一节、做一节、按该节 6 小节里的「完成后必须回报什么」交回一份回报，再投下一节。
> **只读上游**：06（决策 D-015 ~ D-052，唯一真相源）/ 07（需求与 B-13 ~ B-28）/ 08（技术面、RT-011 ~ RT-020 与验收 #1 ~ #32）/ 09（F-022 ~ F-045、Phase 7 ~ 14、A-007 ~ A-012）。四份只读，**本文不改口径**，与 06 号冲突时报给作者、不自行取舍。
> **编号**：本文只发一个系列——出口判据 **E-7-1 起**，形状 `E-<Phase>-<序>`，段内从 1 连续编号、不跳号，末条 **E-14-7**（共 56 条）。其余系列一律写全前缀（D-015 起 / B-13 起 / RT-011 起 / F-022 起 / Phase 7 起 / A-007 起 / HC-011 起 / N-009 起），不认领下游编号。
> **行号**：本文引用的每一处行号都取 06 号与 `.work/v030/facts-A/B/C` 的实读值（那份报告用 `read` 与 `Select-String` 双口径核过），未复核的一处不引。

## §0 固定口径（每节开工照此执行）

```text
① 红线：Mixin 0；网络包 2 个；不改 1.21.1 主线（D:/project_farm 只读）；不留 net.neoforged 兼容层；
   D-018 冻结的数值一个不动。
② 中间态窗口：W1 注册面（F-024 开 → F-042 关）、W2 语言键表头（F-024 开 → F-032 关）；
   另有既有红 1 条：检查器 §2 的 data_component_type 缺项从基线起就在，由 F-042 与 W1 同批清掉。
③ 命名空间 = net.minecraftforge.*；协议串 "1" → "2"；注册面 91（11 类）→ 96（12 类）；语言键 321 → 328。
④ 判据只认五类读数：计数 / 日志整行 / /data get / 源码 grep / 像素或字节。感受类措辞不进任何 E 判据。
⑤ 构建走 D:/project_farm/.tools 那一套（Gradle 8.10 + jdk21 + 镜像代理 127.0.0.1:8899 + java.base 补丁），
   入口就是仓库根的 build-1.20.1.cmd（其 :41-47 的七项环境变量），与 04 / 05 号 §0 第 ⑤ 条同一套。
```

四条读法：

1. **一次只投一节**，节序单调 §1（Phase 7）→ §8（Phase 14），不得调换、不得跳节：每节的前置条件里至少有一条读数来自上一节的 E 判据，跳过的节那条读数就是「未发生」，它不会自己变绿。
2. **「禁写」是时序不是禁令**：某一节禁写的文件只是在那一节还读不准；到它归属的那一节，它立刻变成必写项（§11）。
3. **名词首次并列**（06 号 D-051）：控制终端（`farmstorm:control_terminal`）、农作物培养器（`farmstorm:cultivator`）、农作物转换器（`farmstorm:crop_converter`）、据点核心（`farmstorm:outpost_core`）、膨胀麦（`farmstorm:puffed_wheat`）、压缩干草块（`farmstorm:compressed_hay_block`）、干草块（`minecraft:hay_block`）；其后只用中文名。
4. **E 判据优先读数**：每条都写成「怎么验：… 通过的样子：…」，通过的样子必须是数、日志整行、`/data get` 输出、grep 命中数或像素值，不写「看起来对」。

## §1 Phase 7 · 版本串与终端贴图（M7，F-022 ~ F-023）
### 7.1 本阶段要做什么
把 `mod_version` 推到 **0.3.0**（D-015），再把素材包的 7 个贴图文件逐字节手放进来、一次收齐贴图三个数（D-023）。本节只碰发布面与资源面：`src/main/java` 一行不动，注册面与语言面要到 Phase 8 才开窗。
### 7.2 前置条件
- 首节，无上一节。基线读数（06 号 §0.4）：`mod_version=0.2.8`（`gradle.properties:11`）、`textures/block` **84** png + **5** mcmeta = **89**、Java **151**、注册面 **91 / 11 类**、检查器既有红 **1** 条、语言键 **321** × 2。
- jar 名的唯一派生式是 `build.gradle:6` 的 `version = mod_version + "+mc" + minecraft_version`；两支 `.cmd` 里唯一写死的 jar 名是 `remap-1.20.1.cmd:11` 的用法示例（现为陈样 `0.2.4`），不是现行值。
- 窗口纪律：本节在两窗之外，`tools/verify_project.py` 与 `ModLangProvider.java:10-16` 都不必碰（贴图与版本串都不进这两处）。
### 7.3 任务清单
| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-022 | 版本串 0.3.0 | S | `gradle.properties:11` 读 `0.3.0`；产物名 `farmstorm-0.3.0+mc1.20.1.jar`（`build.gradle:6` 派生）；`src/` 里 `0.3.0` 命中 **0** |
| F-023 | 终端贴图 7 文件手放 | S | `textures/block` 的 png 84 + 4 = **88**、mcmeta 5 + 3 = **8**、两者之和 89 + 7 = **96**；三张动画 PNG 高 **768**、底面 **32**；三个 mcmeta 各 `frametime` 2 / `frames` 24（验收 #5、#29） |
### 7.4 出口判据
1. **E-7-1 版本串单一来源**：怎么验：`Select-String -Path gradle.properties -Pattern mod_version` 与 `Select-String -Path src -Recurse -Pattern 0.3.0` 各跑一次。通过的样子：前者读 `mod_version=0.3.0`；后者命中 **0** 行。
2. **E-7-2 产物名与退出码**：怎么验：跑一次 `build-1.20.1.cmd`（§0 第 ⑤ 条那一套），再 `Get-ChildItem build\libs -Filter *0.3.0*`。通过的样子：退出码 **0**、构建末尾 `BUILD SUCCESSFUL` 命中 **1**；`farmstorm-0.3.0+mc1.20.1.jar` 恰 **1** 个（`build.gradle:6` 派生；`build/libs` 里 0.2.4 ~ 0.2.8 的旧 jar 是既有产物，本节不清理、也不作判据）。
3. **E-7-3 贴图三个数分开读**：怎么验：`Get-ChildItem src\main\resources\assets\farmstorm\textures\block -Filter *.png` 与 `-Filter *.png.mcmeta` 各数一次，再把两数相加。通过的样子：png **88**、mcmeta **8**、两者相加 **96**；三个数任一串位都算不通过（验收 #29）。
4. **E-7-4 帧条尺寸与 mcmeta 帧数**：怎么验：读四张 PNG 的像素高度与三个 mcmeta 的 `frames` 长度。通过的样子：front / side / top 三张高 **768** = 24 × 32、底面 **32**；三个 mcmeta 的 `frametime` 为 `2`、`frames` 恰 **24** 项（0 ~ 23）⇒ 一轮 48 刻 = 2.4 秒（验收 #5）。
5. **E-7-5 素材字节与落点**：怎么验：逐字节比对素材包（D-023：三张动画 PNG 4191 / 3851 / 3513 B、底面 1102 B、三个 mcmeta 各 322 B）与落地的那 7 个文件。通过的样子：7 个文件字节逐一相等；文件名保留 `_a_` 体例（与既有 `cultivator_a_top.png` 同形）。
6. **E-7-6 注册 id 不带 `_a`（负向）**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/registry -Pattern control_terminal_a`。通过的样子：命中 **0**——`_a` 只活在贴图文件名里（D-020 的补充）。
7. **E-7-7 源码面零改动（负向）**：怎么验：`git status --porcelain` 与 `(Get-ChildItem src\main\java -Recurse -Filter *.java).Count`。通过的样子：`src/main/java` 下 **0** 个路径变化；计数 **151**。
### 7.5 常见翻车点
- 把 `0.3.0` 也写进 `src/main/java` 或 `mods.toml`：版本号出现第二处来源，下一版要改两遍 → 预防：E-7-1 的 grep 命中 **0**。
- 把 `remap-1.20.1.cmd:11` 的示例 `0.2.4` 当成现行 jar 名去改别处：真派生式在 `build.gradle:6`，改错地方产物名不变 → 预防：E-7-2 的产物名读数。
- mcmeta 照抄项目里 5 个既有的 16 帧最小形：只播前 16 帧、第 17 ~ 24 帧永不出现；把 32×768 重采样成 32×512 得到同一症状（RT-016）→ 预防：E-7-4 的两个读数同读。
### 7.6 完成后必须回报什么
- `git diff --stat` 原文与 `gradle.properties:11` 的前后两行；构建退出码、`build\libs` 的完整清单、两轮 `Select-String` 的命中行数（0 / 1）。
- 7 个贴图文件的路径 + 字节 + PNG 像素尺寸；三个 mcmeta 的 `frames` 长度。
- 没做的：贴图在游戏里的播放效果归验收 #5 的帧数与像素读数，本节只读文件面。

## §2 Phase 8 · 注册面与外观（M8，F-024 ~ F-025）
### 8.1 本阶段要做什么
注册控制终端的方块与方块物品（D-020、D-021、D-022）并让它进两份语言文件，再产出 4 个朝向 variant 的 blockstate、1 张 cube 模型与 item 模型（D-022）。本节**同时打开两个窗口**：W1 注册面（`blocks` / `items` 各 28 → 29）与 W2 语言键表头（321 → 323），此后注册面进入窗口期：检查器那 1 条既有红要留到 F-042 才清，期间不许碰它。
### 8.2 前置条件
- Phase 7 已交付：**E-7-1**（`mod_version=0.3.0`）、**E-7-3**（贴图三数 88 / 8 / 96）、**E-7-7**（Java 仍 151）。
- 现状读数（实读）：`items` **28** / `blocks` **28**（facts-A §4）、注册面 **91（11 类）**、语言键 **321** × 2、`models/block` **78**、`models/item` **28**、`blockstates` **28**。
- 窗口纪律：本节起 W1 与 W2 同时打开，`tools/verify_project.py` 与 `ModLangProvider.java:10-16` 禁写（§11）；注册 id 不带 `_a` 已由 E-7-6 锁死。
### 8.3 任务清单
| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-024 | 方块与物品注册 | M | `blocks` 28 + 1 = **29**、`items` 28 + 1 = **29**、注册面 91 + 2 = **93**；两份 lang 各 321 + 2 = **323** 且键集相等；Java 151 + 1 = **152**（新建 `block/ControlTerminalBlock.java`） |
| F-025 | 模型与 blockstate | M | `blockstates` 28 + 1 = **29**、`models/block` 78 + 1 = **79**、`models/item` 28 + 1 = **29**；`facing=` 键恰 **4** 个；文件里 `working` 命中 **0**（验收 #4、#27） |
### 8.4 出口判据
1. **E-8-1 两个注册项**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/registry -Pattern control_terminal` 与一次真启动的日志。通过的样子：`FarmstormBlocks` / `FarmstormItems` 各命中 **1** 行；日志整行读 `自检 注册对象 93 个：items 29 / blocks 29 / …`、结尾是 **FAIL**（断言值仍是 91，W1 期望红）；检查器仍打 `已核 registry 对象: 92 / 92`（ID 清单与计数表都在禁写文件里，本节不碰）。
2. **E-8-2 五项属性照铁砧**：怎么验：读注册处。通过的样子：`strength(5.0F, 1200.0F)`、`mapColor(MapColor.METAL)`、`requiresCorrectToolForDrops()`、`sound(SoundType.ANVIL)`、`pushReaction(PushReaction.BLOCK)` 各命中 **1**（D-021；五个方法在 `BlockBehaviour.java:1112 / 1064 / 1213 / 1102 / 1173`）。
3. **E-8-3 水平朝向 4 variant**：怎么验：`Get-Content src\generated\resources\assets\farmstorm\blockstates\control_terminal.json -Raw` 数 `variants` 的键。通过的样子：恰 **4** 个键 `facing=north` / `east` / `south` / `west`，`y` 依次不写 / 90 / 180 / 270（`ModBlockStateProvider.java:277-284` 的 `facingRotationY`）；`working` 命中 **0**（验收 #4、#27）。
4. **E-8-4 语言键到 323 且两文件同键**：怎么验：用 `ConvertFrom-Json` 读两份 lang 的顶层键数。通过的样子：各 **323** 键、键集逐字相同；`item.farmstorm.control_terminal` 与 `block.farmstorm.control_terminal` 各命中 **1**。
5. **E-8-5 表头仍未改（W2 内，负向）**：怎么验：`git diff src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java`。通过的样子：命中 **0** 行——表头现读 `28 + 28 + 13 + 32 + 178 + 42 = 321`（`ModLangProvider.java:10-16`、facts-C §10.5），到 F-032 才一次改到 328。
6. **E-8-6 三个模型计数**：怎么验：三条计数命令（`models/block` / `models/item` / `blockstates`）。通过的样子：**79** / **29** / **29**；`models/item/control_terminal.json` 的 `parent` 为 `farmstorm:block/control_terminal`。
7. **E-8-7 战利品表与配方不动（负向）**：怎么验：数 `src\generated\resources\data\farmstorm\loot_tables\blocks` 与 `recipe`。通过的样子：loot_tables 仍 **28**、recipe 仍 **25**（战利品表归 F-041、合成配方归 F-040）。
### 8.5 常见翻车点
- 现在就去 `EXPECTED_COUNTS` 补 `control_terminal`：W1 提前关窗，Phase 9 ~ 13 每一步都「已绿」，缺项不再报（D-052 / §11）→ 预防：E-8-1 的期待红读数加 §11 的 diff 命中 **0**。
- blockstate 落进兜底 `simpleBlock` 或漏一个 `else if` 分支：4 个朝向里 3 个找不到模型（`ModBlockStateProvider.java:72-92` 的分派链）→ 预防：E-8-3 的 `facing=` 键数 **4**。
- 给终端加 `working` 之类工作态属性：三个动画面自己播、代码侧 0 行（D-022 的补充），多出的属性会要求 8 个 variant → 预防：E-8-3 的 `working` 命中 **0**。
### 8.6 完成后必须回报什么
- 两个注册文件的 diff 原文与那五项属性各所在行；三条模型计数（79 / 29 / 29）与 blockstate 的 4 个 variant 键原文。
- 两份 lang 的键数（各 323）与两条新键原文；`git diff ModLangProvider.java` 的空输出。
- 启动日志那一行的原文（实数 93、断言 91、FAIL 结尾）与 registry grep 的两行命中；检查器仍读 `92 / 92`。

## §3 Phase 9 · 方块实体、连线协议与类型表（M9，F-026 ~ F-028）
### 9.1 本阶段要做什么
让终端成为方块实体、三态模式写进自己的 NBT（D-030）；给线上码追加 `TERMINAL(2)` 并把协议串升到 **"2"**、绿线取 `0.30 / 0.85 / 0.30`（D-035 ~ D-038）；再把终端同时写进 `farmstorm:processing_block` 与 `KIND_BY_BLOCK`（D-024）——后两处必须同一个提交改完，且 `Kind.TERMINAL` 先于 `KIND_BY_BLOCK`。
### 9.2 前置条件
- Phase 8 已交付：**E-8-1**（注册面 93）、**E-8-3**（4 variant）；`block_entity_type` 现 **8**（facts-A §4）。
- 现状读数（实读）：`KIND_BY_BLOCK` **2** 条（`OutpostCoreBlockEntity.java:123-125`）；`Kind` **2** 值 `CULTIVATOR(0)` / `CROP_CONVERTER(1)`（`CultivatorLinkPayload.java:73-75`）、`id()` 在 `:84-86`、`byId` 在 `:89-96`（未知码回落 `CULTIVATOR`）；`PROTOCOL_VERSION = "1"`（`FarmstormNetwork.java:32`）；渲染 `switch` 两分支无 `default`（`client/CultivatorLinkRenderer.java:100-109`），青 `:51-53`、红 `:59-61`。
- 窗口纪律：本节在 W1 + W2 内，`tools/verify_project.py` 与 `ModLangProvider.java:10-16` 禁写。
### 9.3 任务清单
| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-026 | 方块实体与三态 NBT | M | `block_entity_type` 8 + 1 = **9**、注册面 93 + 1 = **94**；`/data get block <终端> Mode` 初始读 `off`、退出重进不变；Java 152 + 1 = **153** |
| F-027 | `Kind.TERMINAL` 与协议 "2" | M | `Kind` **3** 值、`byId(2)` = `TERMINAL` 且两旧值 id 未动（验收 #24）；`PROTOCOL_VERSION` 读 `"2"`、启动日志两行带「版本 2」（验收 #20）；渲染分支 **3** 个、绿 `0.30 / 0.85 / 0.30`（验收 #12）；网络包仍 **2** |
| F-028 | 标签与 `KIND_BY_BLOCK` 同改 | S | `processing_block` 的 `values` **3** 项、`KIND_BY_BLOCK` **3** 条；核两条列表同长同下标、末位 `terminal`（验收 #25） |
### 9.4 出口判据
1. **E-9-1 三态 NBT 存序列化名**：怎么验：`/data get block <终端坐标> Mode`，再 `Select-String -Path src/main/java/com/dashpig/farmstorm/blockentity/ControlTerminalBlockEntity.java -Pattern ordinal`。通过的样子：初始读到字符串 `off`（不是数字）；`ordinal` 命中 **0**——NBT 里存 `off` / `cultivator` / `converter`（D-030 的否决项 3）。
2. **E-9-2 方块实体类型与注册面**：怎么验：一次真启动的日志加 `Select-String -Path src/main/java/com/dashpig/farmstorm/registry -Pattern control_terminal`。通过的样子：日志读 `自检 注册对象 94 个：…`（断言仍 91 ⇒ FAIL，W1 期望红）；registry 下三处命中（方块 / 物品 / 方块实体类型）各 **1**；检查器仍读 `92 / 92`。
3. **E-9-3 线上码只追加不重排**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/network/CultivatorLinkPayload.java -Pattern "CULTIVATOR\(0\)|CROP_CONVERTER\(1\)|TERMINAL\(2\)"`。通过的样子：三个字面量各命中 **1**、枚举值总数 **3**，既有两值的数值一个没变（验收 #24）。
4. **E-9-4 协议串 "2" 与启动日志**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/network/FarmstormNetwork.java -Pattern PROTOCOL_VERSION` 与一次真启动的日志。通过的样子：字面量 `"2"`；日志两行带「版本 2」（验收 #20）；`CHANNEL.messageBuilder(...)` 仍 **2** 处（`:51` / `:60`）。
5. **E-9-5 绿线三色、既有两色不动**：怎么验：读 `client/CultivatorLinkRenderer.java` 的 `switch` 与三组常量，进游戏同屏比对像素。通过的样子：`case` **3** 个、`default` 命中 **0**；绿像素 **0x4D / 0xD9 / 0x4D**，青（`0.30 / 0.85 / 0.90`）与红（`0.92 / 0.22 / 0.22`）逐字未变（验收 #12）。
6. **E-9-6 类型表与标签同提交**：怎么验：`git show --stat -1` 看 F-028 那个提交。通过的样子：同一提交里既有 `datagen/ModBlockTagsProvider.java:45-47` 的成员 2 → 3，也有 `OutpostCoreBlockEntity.java:123-125` 的第 3 条；构建退出码 **0**。
7. **E-9-7 两条列表同长同下标**：怎么验：放下终端后读 `/data get block <核坐标> Cultivators` 与 `CultivatorKinds`。通过的样子：两条等长、末位下标相同、`Kinds` 末位读 `terminal`（验收 #25）；补绑的候选过滤（`OutpostCoreBlockEntity.java:621-622`）能扫到它。
### 9.5 常见翻车点
- 先改 `KIND_BY_BLOCK` 再加枚举：`Map.of` 引用不到 `Link.Kind.TERMINAL`，`cannot find symbol`、编译不过 → 预防：E-9-6 的同一提交判据（顺序纪律①）。
- 只改标签不改类型表（或反之）：前者「被发现、不绑定」，后者「扫不到」，两种半边改动都不报错（D-024 的补充）→ 预防：E-9-6 两处同提交加 E-9-7 的候选过滤读数。
- 顺手多加第三个网络包同步模式：协议要再升一位、网络包从 2 变 3（06 号 §4 红线 7）→ 预防：E-9-4 的 **2** 处 `messageBuilder`。
### 9.6 完成后必须回报什么
- `Kind` 三成员、渲染 `switch` 三分支、`PROTOCOL_VERSION` 一行的原文。
- `/data get` 三次读数：`Mode` 初始值、退出重进后的值、`Cultivators` 与 `CultivatorKinds` 两条列表。
- 构建退出码、启动日志两行原文（`自检 注册对象 94 个：…`）、检查器仍读 `92 / 92`。
- `git show --stat -1` 的提交内容（含两处同改的证据）。

## §4 Phase 10 · 绑定与面板（M10，F-029 ~ F-032）
### 10.1 本阶段要做什么
让终端与两台机器同形地走放置绑定与三出口（D-026、D-027）、给它一个 **0 槽菜单**（D-034）、在 `FMLClientSetupEvent` 的 `enqueueWork` 里用 `MenuScreens.register` 注册屏幕（D-034），再补 5 个面板语言键把总数收到 **328** 并同步表头——W2 至此关窗（D-018）。
### 10.2 前置条件
- Phase 9 已交付：**E-9-2**（`block_entity_type` 9 / 合计 94）、**E-9-1**（`Mode` 可读可存）、**E-9-6**（标签 3 成员）。
- 既有先例（实读）：放置绑定照 `CultivatorBlock.setPlacedBy`（`:82-98`：`:90-91` 取核、`:95-97` 先 `addCultivator` 再 `setCore`、无核 `:92-94` 直接返回不写 `Core` 键）；范围判据 `inRange` 在 `OutpostCoreBlockEntity.java:532-534` / `:537-543`；名额判据 `isCultivatorListFull` 在 `:401-403`、`addCultivator` 在 `:413-426`；界面注册唯一先例 `client/FarmstormClientSetup.java:31-34`（`:33` 的 `enqueueWork`）；`MenuType` 只有 `(MenuSupplier, FeatureFlagSet)` 一个构造（`MenuType.java:49-52`），开屏走 `player.openMenu`（`OutpostCoreBlock.java:139`）。
- 窗口纪律：本节仍在 W1 + W2 内；注册面到 **95** 而检查器期望仍 92（期望红）；`ModLangProvider.java:10-16` 由 F-032 一次改到 328。
### 10.3 任务清单
| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-029 | 放置绑定与三出口 | M | 3×3 区块内先核后终端：`Core` 键有值、核侧两条列表各 +1；超 3×3 区块：`Core` 键缺失；名额满 24 时第 25 台不绑定（验收 #10 的绑定半条）；Java 仍 **153** |
| F-030 | MenuType 与 0 槽菜单 | M | `menu_type` 1 + 1 = **2**、注册面 94 + 1 = **95**；`addSlot` 命中 **0**、`quickMoveStack` 命中 **1** 且返回 `ItemStack.EMPTY`；`ContainerData` **2** 格；Java 153 + 1 = **154** |
| F-031 | 面板屏幕与注册 | M | `MenuScreens.register` 命中 **1** 且在 `FMLClientSetupEvent` 的 `enqueueWork` 内（验收 #31）；右键标题读「控制终端」、槽位 **0**（验收 #6）；`Mode` 依次 `off → cultivator → off → converter`（验收 #7）；未绑定两按钮 `active` 均 `false`、库存 0 变化（验收 #26）；Java 154 + 1 = **155** |
| F-032 | 面板 5 语言键 | S | 两份 lang 各 323 + 5 = **328** 且键集相等（验收 #3）；表头算式 `29 + 29 + 13 + 32 + 178 + 47 = 328` 与实读一致；W2 关窗 |
### 10.4 出口判据
1. **E-10-1 两种绑定读数**：怎么验：3×3 区块内先放核再放终端，读 `/data get block <核坐标> Cultivators` 与终端的 `Core` 键；再在 3×3 之外放一台。通过的样子：第一次两条列表各 +1 且 `Core` 有值；第二次 `Core` 键缺失（缺键即未绑定，`CultivatorBlock.java:92-94` 的同形）；写入顺序是「先 `addCultivator` 成功、后 `setCore`」。
2. **E-10-2 0 槽菜单形状**：怎么验：读 `menu/ControlTerminalMenu.java` 的 `addSlot` / `quickMoveStack` / `stillValid` 三处与 `super(...)` 的实参。通过的样子：`addSlot` 命中 **0**；`quickMoveStack` 命中 **1** 且返回 `ItemStack.EMPTY`；`stillValid` 走 `stillValid(this.access, player, ...)` 形态（`OutpostCoreMenu.java:133-135` 同形）；`super(...)` 传入**非 null** 的 `MenuType`（`AbstractContainerMenu.java:74-79`：`menuType == null` 时 `getType()` 抛异常，RT-015）。
3. **E-10-3 屏幕注册时序与客户端专属**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/client/FarmstormClientSetup.java -Pattern MenuScreens.register`，再读该行所在方法与类注解。通过的样子：命中 **1**；所在方法是 `onClientSetup(FMLClientSetupEvent)`、调用包在 `event.enqueueWork(...)` 里（`:32-33`），类注解带 `value = Dist.CLIENT`（`:25`）。
4. **E-10-4 右键开面板与置灰**：怎么验：右键一台未绑定的终端，再右键一台已绑定的。通过的样子：两次都开界面、槽位 **0**、标题读「控制终端」（验收 #6）；未绑定时状态行写「未绑定」、两个模式按钮 `active` 均 `false` 且核库存 0 变化（验收 #26）。
5. **E-10-5 三态轮转**：怎么验：绑定后连点三次，读 `/data get block <终端坐标> Mode` 与 `ContainerData` 格 0。通过的样子：`off → cultivator → off → converter`（验收 #7）；再点已亮的那个退回 `off`（D-031）。
6. **E-10-6 语言键到 328 且表头只改一次**：怎么验：`ConvertFrom-Json` 读两份 lang 的键数，再 `git log -p -- src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java` 看 `:10-16` 那段。通过的样子：各 **328** 键、键集逐字相同（验收 #3）；表头读 `29 + 29 + 13 + 32 + 178 + 47 = 328`；该段只在 F-032 一个提交里改过（命中 **1** 次），`git diff` 里 `321` 命中 **0**。
7. **E-10-7 网络包不增（负向）**：怎么验：`Select-String -Path src -Recurse -Pattern messageBuilder`。通过的样子：命中仍 **2**（`FarmstormNetwork.java:51` / `:60`）；面板状态走 `addDataSlots` 的 **2** 格 `ContainerData`，没有第三个包（06 号 §4 红线 7、D-034）。
### 10.5 常见翻车点
- 漏 `MenuScreens.register`：客户端只有一条 warn、屏幕类不生效，右键终端看不到界面（RT-014）→ 预防：E-10-3 的命中 **1** 加 E-10-4 的一次真右键。
- 菜单用 `super(null, id)` 构造：`getType()` 抛 `UnsupportedOperationException`，开屏链路取 `getType()` 时炸（RT-015）→ 预防：E-10-2 的非 null `MenuType` 判据。
- 先写终端自己的 `Core` 缓存、后调 `addCultivator`：核侧返 false 时留下「核说没绑、终端说绑了」的假象 → 预防：E-10-1 的两侧读数同读。
- 面板里加槽位或嵌 54 格核库存：等于第二个核存储界面，点击搬运逻辑写两遍（D-034 的否决项 7）→ 预防：E-10-2 的 `addSlot` 命中 **0**。
### 10.6 完成后必须回报什么
- 三组绑定读数原文（区块内 / 超范围 / 名额满）。
- 菜单与屏幕的关键行原文：`addSlot` 计数、`quickMoveStack` 一行、`MenuScreens.register` 一行、`ContainerData` 的格数。
- 两份 lang 的键数（各 328）加表头原文加 `git log -p` 里该段的改动次数。
- 启动日志的注册对象总数（95）与那一行的期待红说明；`messageBuilder` 命中 **2**。

## §5 Phase 11 · 投料循环（M11，F-033）
### 11.1 本阶段要做什么
落服务端每 **20** 刻一轮的投料：一轮里对每台目标机器最多投 1 个，准入判据一律借机器自己的闸、终端不自建白名单（D-032、D-033）；四条静默边界（`OFF` / 槽非空 / 名额满 / 超 3×3 区块）各一条读数。
### 11.2 前置条件
- Phase 10 已交付：**E-10-1**（绑定两侧读数）、**E-10-2**（0 槽菜单与两格 `ContainerData`）、**E-10-5**（三态轮转到 `cultivator`）。
- 判据只借不改（实读）：培养器 `canInsert` = `isUsable() && held.isEmpty() && admissible(...)`（`CultivatorBlockEntity.java:152-154`）；转换器 `canInsert`（`CropConverterBlockEntity.java:141-143`）；史诗四件要到 Phase 12 才放行，本节不拿它们做读数。
- 节拍是编译期常量（D-032），**不新增 config 键**：server config 仍 **175**、client config 仍 **3**；同类写死先例是 `OutpostCoreBlockEntity.java:111` 的 `REBIND_INTERVAL_TICKS = 100`。窗口纪律：本节在 W1 内，`tools/verify_project.py` 禁写。
### 11.3 任务清单
| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-033 | 每 20 刻投料循环 | L | 相邻两次投料的 `gameTime` 差恒 **20**（验收 #8）；一轮核 **64 → 63**、机器槽 **1**；槽非空后 **200** 刻内核库存变化 **0**（验收 #9）；名额满 24 后第 25 台不投料、库存 0 变化（验收 #10）；超 3×3 区块不投料（验收 #11）；Java 仍 **155** |
### 11.4 出口判据
1. **E-11-1 节拍恒 20 刻**：怎么验：切培养模式、核里放 3 组合法投入物，按世界刻记录相邻两次投料的 `gameTime`。通过的样子：差值恒 **20**（验收 #8）；节拍常量的定义命中 **1** 且声明含 `private static final`；config 面按源码定义数读——`FarmstormConfig.java` 的 `define` 家族调用仍 **175** 处、client config 仍 **3** 处。
2. **E-11-2 一轮每台 1 个**：怎么验：同一场景读核库存与机器投入槽。通过的样子：核 **64 → 63**、机器槽 **1**（验收 #9）。
3. **E-11-3 槽非空不再塞**：怎么验：槽非空后继续跑 **200** 刻再读核库存。通过的样子：变化 **0**（验收 #9 的后半）。
4. **E-11-4 名额满与超范围两条静默边界**：怎么验：名额占满 24 后再放第 25 台；另在 3×3 区块之外放一台，各跑 **200** 刻。通过的样子：两种情形核库存变化都是 **0**（验收 #10、#11）；第 25 台 `Core` 键缺失、面板按钮置灰。
5. **E-11-5 `OFF` 不消费库存**：怎么验：`/data get block <终端坐标> Mode` 读到 `off` 后跑 **200** 刻。通过的样子：核库存变化 **0**（D-031）。
6. **E-11-6 判据只借不改（负向）**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/blockentity/ControlTerminalBlockEntity.java` 三串。通过的样子：`Items.` 命中 **0**、`CropTier` 命中 **0**、`canInsert(` 命中 **2**（两台机器各一处调用，没有第三份判据，D-033）。
7. **E-11-7 史诗四件不进本节验收**：怎么验：在本节的验收记录里搜四个原版物品 id。通过的样子：`minecraft:torchflower` / `minecraft:pitcher_plant` / `minecraft:sweet_berries` / `minecraft:golden_apple` 命中 **0**；那四件的读数归 **E-12-5**（顺序纪律⑤）。
### 11.5 常见翻车点
- 把 20 刻做成 config 键：server config 由 175 变 176，五份文档的数字与检查器的键集口径全部要改（D-032）→ 预防：E-11-1 的 `define` 家族计数 **175**。
- 终端自建白名单：判据变两处，数据包加一条转换配方时两边不同步（D-033）→ 预防：E-11-6 的 `Items.` 与 `CropTier` 命中 **0**。
- 拿史诗四件验收投料：Phase 12 之前培养器仍拒收这四件，读数表现为「投不进去」，会被误判成投料 bug 并去改投料代码（顺序纪律⑤）→ 预防：E-11-7 的四串命中 **0**。
- 一轮里对同一台连续塞到满：「一轮每台 1 个」当场消失（B-17）→ 预防：E-11-2 与 E-11-3 的两读数同读。
### 11.6 完成后必须回报什么
- 相邻两次投料的 `gameTime` 差值与第一组三读数（核库存、机器槽、槽非空后的 200 刻）。
- 四条边界的 200 刻读数（各 0 变化）与第 25 台的 `Core` 键读数。
- 节拍常量一行原文、config 定义数（175 / 3）、三串 grep 的命中数（0 / 0 / 2）。

## §6 Phase 12 · 可挖门槛与史诗准入（M12，F-034 ~ F-035）
### 12.1 本阶段要做什么
把三个处理方块补进 `#minecraft:mineable/pickaxe`（管挖速）并新增 `#minecraft:needs_stone_tool`（管算不算正确工具，D-039、D-040），再把培养器准入闸里那道史诗比较整条删掉（D-042）；金镐落在哪一档只记录不断言（D-041）。本节顺手盖住一个既有缺口：培养器既有 `requiresCorrectToolForDrops()` 又不在任何 `mineable/*` 里，破坏时不掉任何东西（D-039 的补充、B-23）。
### 12.2 前置条件
- Phase 11 已交付：**E-11-2**（一轮每台 1 个）；`processing_block` 的 3 个成员已在 Phase 9 落地（E-9-6），本节不改那三行。
- 现状读数（实读）：`ModBlockTagsProvider.java:49-63` 的 `MINEABLE_WITH_PICKAXE` 现 **10** 个成员、缺 `CULTIVATOR` 与 `CONTROL_TERMINAL`；`:67` 是 `NEEDS_DIAMOND_TOOL` 的一行写法（1 个成员）；`needs_stone_tool` 在本文件不存在（facts-C §10.1）。
- 判定链换成 1.20.1 的符号：`ServerPlayerGameMode.java:249` 的 `canHarvestBlock` → `ForgeHooks.java:226-232`（读 `requiresCorrectToolForDrops`）→ `DiggerItem.java:64-82`（`NEEDS_STONE_TOOL` 分支在 `:74`）→ `TierSortingRegistry.java:123-134`；**1.20.1 没有 `DataComponents` 与 Tool 组件**（facts-B §0 第 15 行）。窗口纪律：本节在 W1 内，`tools/verify_project.py` 禁写。
### 12.3 任务清单
| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-034 | 两条挖掘标签 | S | `mineable/pickaxe` 10 + 2 = **12** 成员；`needs_stone_tool` **3** 成员；木镐挖培养器掉落 **0** 且方块仍被移除（验收 #13）、石镐挖同一块掉落 **1**（验收 #14）；金镐只记录（验收 #19） |
| F-035 | 史诗准入放行 | S | 火把花（`minecraft:torchflower`）/ 瓶子草（`minecraft:pitcher_plant`）/ 甜浆果（`minecraft:sweet_berries`）/ 金苹果（`minecraft:golden_apple`）各成功投入 **1** 次（验收 #15）；`CropTier.EPIC` 在 `CultivatorBlockEntity.java` 命中 **0**；`Items.` 在该文件命中 **0**；转换配方仍 **21**、进度仍 **16** |
### 12.4 出口判据
1. **E-12-1 pickaxe 成员 12**：怎么验：`Get-Content src\generated\resources\data\minecraft\tags\blocks\mineable\pickaxe.json -Raw` 数 `values`。通过的样子：**12** 项（10 + 2），新增两项恰为 `farmstorm:cultivator` 与 `farmstorm:control_terminal`（转换器原本就在名单里）。
2. **E-12-2 `needs_stone_tool` 成员 3**：怎么验：`Get-Content src\generated\resources\data\minecraft\tags\blocks\needs_stone_tool.json -Raw`。通过的样子：文件存在、`values` **3** 项，恰为 `farmstorm:cultivator` / `farmstorm:crop_converter` / `farmstorm:control_terminal`。
3. **E-12-3 木镐 0 掉落（方块仍被移除）**：怎么验：木镐各挖一台培养器、转换器、终端，各数掉落物。通过的样子：三次掉落都是 **0**；判据是掉落物计数、不是「挖不动」——`ServerPlayerGameMode.java:253` 的 `removeBlock` 与 `:255-257` 的 `playerDestroy` 是两个独立调用（验收 #13）。
4. **E-12-4 石镐 1 个**：怎么验：石镐挖同样三块。通过的样子：三次都掉 **1** 个对应方块物品（验收 #14）。
5. **E-12-5 四件各成功投入 1 次**：怎么验：手持那四个原版物品各右键培养器一次，每次读投入槽。通过的样子：四次都读到投入槽非空、一个都不被拒（验收 #15）；这一组读数只在本节出现（E-11-7 已把本节之外那份封到 0）。
6. **E-12-6 比较整条删除、白名单不出现（负向）**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/blockentity/CultivatorBlockEntity.java -Pattern "CropTier.EPIC|Items."`，再读 `admissible` 的方法体。通过的样子：两串命中都是 **0**；方法体仍是一句返回（`CultivatorBlockEntity.java:143-149`，改动只在 `:148` 的 `&&` 之后）；`CropTier.java:25` 那句已作废的档位口径同批改写（D-042、06 号 §5.1）。
7. **E-12-7 金镐只记录 + 产出面不动**：怎么验：金镐挖同样三块并把读数写进回报；再 `git diff` 看 `batch` 段。通过的样子：金镐那三行**不含** PASS / FAIL 判定（D-041、RT-020）；`batch` 段与产出倍率 diff 命中 **0** 行，转换配方仍 **21**、进度仍 **16**。
### 12.5 常见翻车点
- 只加 `mineable/pickaxe` 不加 `needs_stone_tool`：木镐挖得慢但照样掉，「石镐及以上」没落地（D-040）→ 预防：E-12-3 的三次 **0**。
- 只加 `needs_stone_tool` 不加 pickaxe：挖速停在徒手级，玩家拿到的是「掉但挖很久」→ 预防：E-12-1 的 **12**。
- 把金镐写成断言：与 `incorrect_for_gold_tool` / `TierSortingRegistry` 的真值可能相反，变成必红条目（D-041）→ 预防：E-12-7 的读数行不含判定。
- 在培养器里补一份史诗白名单：判据变两处，数据包加配方时两边不同步（D-043）→ 预防：E-12-6 的 `Items.` 命中 **0**。
### 12.6 完成后必须回报什么
- 两份标签 JSON 的 `values` 条数与逐条内容（12 与 3）。
- 3 块 × 3 种镐的 9 个掉落读数：木镐 / 石镐两组带判定，金镐那 3 个只记录。
- `admissible` 改后的完整方法体、`git diff` 的 `-1` / `+1` 行数、转换配方 21 / 进度 16 两个读数。
- 没做的：金镐算不算「石镐及以上」不由本节判定（D-041，06 号 §8 第 1 条）。

## §7 Phase 13 · 干草块掉落（M13，F-036 ~ F-039）
### 13.1 本阶段要做什么
新建 `util/PuffedHayData`（`extends SavedData`，D-045），在膨胀麦把干草块真正写进那一格之后登记坐标（D-046），再新建限定 `minecraft:blocks/hay_block` 一张表的全局战利品修饰器与它的序列化器注册（D-047、D-048），最后由 datagen 产出**两份**数据文件（D-049）。本节是 1.20.1 唯一一处机制替换：注册面在这一节收到 **96（12 类）**，Java 从 155 收到 **159**。
### 13.2 前置条件
- Phase 12 已交付：**E-12-4**（石镐掉 1 个，用作掉落对照）；注册面现 **95**、Java 现 **155**。
- 写入点（实读）：`entity/thrown/PuffedWheatProjectile.java:104-123` 的 `fillHay`——半径 `edge/2` 在 `:105`、抗性跳过 `:113-115`、白名单跳过 `:116-118`、`setBlock` 在 `:119`；压缩膨胀麦覆写 `fillBlock()` 复用同一个 `fillHay`（`CompressedPuffedWheatProjectile.java:34-37`）。
- 1.20.1 的签名（实读）：`SavedData.save(CompoundTag)` 单参（`SavedData.java:15`）、`DimensionDataStorage.computeIfAbsent(Function, Supplier, String)` 三参（`:38`）、`ServerLevel.getDataStorage()`（`ServerLevel.java:1148`）；先例 `outpost/OutpostCoreIndex.java:76-79` 与 `trap/TrapFifoIndex.java:59-60`。
- GLM 面（实读）：`IGlobalLootModifier`（`:30`，`apply` 在 `:78-79`、`codec()` 在 `:84`）、`LootModifier` 只留 `doApply`（`:68-69`）、注册表键 `ForgeRegistries.java:187`、`LootContextParams.ORIGIN` 是 `Vec3`（`LootContextParams.java:18`）、`apply` 整表替换（`ForgeHooks.java:1196-1198` ← `LootTable.java:118`）、清单命名空间固定 `forge`（`LootModifierManager.java:53`，`:75` 的注释「不在清单里的修饰器不生效」）。窗口纪律：本节仍在 W1 内（注册面到 96，F-042 才关窗）；`tools/verify_project.py` 禁写。
### 13.3 任务清单
| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-036 | PuffedHayData | M | `run/world/data/farmstorm_puffed_hay.dat` 落盘、跨重启条数不变（验收 #32）；`save(CompoundTag)` 单参、`computeIfAbsent` 的三个实参依次是「读函数 / 构造器 / 落盘名」；Java 155 + 1 = **156** |
| F-037 | 落点登记 | S | 掷一次膨胀麦：记录条数 = 实际写入格数（验收 #16 的前半）；被抗性挡掉的格子不在集合；压缩膨胀麦掷出后条数变化 **0** |
| F-038 | 战利品修饰器类与注册 | L | `global_loot_modifier_serializers` 0 + 1 = **1**、注册面 95 + 1 = **96（12 类）**；挖记录内干草块掉落 **0** 且条数 **−1**（验收 #16）；压缩干草块照旧掉 **1**（验收 #17）；老存档干草块照旧掉 **1**（验收 #18）；Java 156 + 2 = **158** |
| F-039 | 数据文件 provider | M | 两份数据文件落在 `data/farmstorm/loot_modifiers/puffed_hay.json` 与 `data/forge/loot_modifiers/global_loot_modifiers.json`；清单 `entries` 含 `farmstorm:puffed_hay`（验收 #30）；loot modifier 数据文件 0 + 2 = **2**；Java 158 + 1 = **159** |
### 13.4 出口判据
1. **E-13-1 记录落盘与跨重启**：怎么验：`Test-Path run/world/data/farmstorm_puffed_hay.dat`，重启服务端前后各读一次条数与内容。通过的样子：文件存在；两次条数相等且 ≥ **1**、内容逐条相同（验收 #32）。
2. **E-13-2 登记数 = 实写格数（含压缩膨胀麦负向）**：怎么验：掷一次膨胀麦，数落点实写格数与记录条数；再掷一次压缩膨胀麦，读前后条数。通过的样子：前两数相等、被抗性挡掉的格子不在集合；压缩膨胀麦那次条数变化 **0**（`hay.is(Blocks.HAY_BLOCK)` 守卫，D-046、RT-017）。
3. **E-13-3 GLM 序列化器注册与十二类静态计数 96**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/registry -Pattern GLOBAL_LOOT_MODIFIER_SERIALIZERS` 加十二类的源码字段计数。通过的样子：`DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ...)` 命中 **1**，泛型为 `Codec<? extends IGlobalLootModifier>`；`global_loot_modifier_serializers` 计 **1**、十二类相加 **96**（29 + 29 + 9 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 2）；启动日志仍读 **95 个**（第 12 类要到 F-043 才进 Java 的求和，本节属 W1 期望红）。
4. **E-13-4 `ORIGIN` 转换与消费语义**：怎么验：读 `loot/PuffedHayLootModifier.java`。通过的样子：`context.getParam(LootContextParams.ORIGIN)` 取到 `Vec3` 后经 `BlockPos.containing(...)` 转坐标（RT-012）；命中记录时 `doApply` 返回**空表**，`setCanceled` 命中 **0**（D-048）。
5. **E-13-5 只挂 hay_block 一张表**：怎么验：`Get-Content src\generated\resources\data\farmstorm\loot_modifiers\puffed_hay.json -Raw`。通过的样子：`conditions` 里恰 **1** 条 `forge:loot_table_id`（Forge 自带的条件类型，注册在 `ForgeMod.java:663`）且 `loot_table_id` 的值为 `minecraft:blocks/hay_block`；`type` 指向 `farmstorm:puffed_hay`；没有第二张表（D-047、B-27）。
6. **E-13-6 清单登记与命名空间（RT-011）**：怎么验：`Get-Content src\generated\resources\data\forge\loot_modifiers\global_loot_modifiers.json -Raw`。通过的样子：文件在 **forge** 命名空间下（`data/forge/`）、`replace` 为 `false`、`entries` 含 `farmstorm:puffed_hay`（验收 #30）；两份 JSON 都由 datagen 产出——`runData` 后 `git status` 里是这两份新文件，手改时间戳不变。
7. **E-13-7 三条对照掉落读数**：怎么验：石镐挖记录内的一格、挖一格压缩干草块、挖一格老存档里已存在的干草块。通过的样子：**0** 且条数 −1（验收 #16）、**1**（验收 #17）、**1**（验收 #18）；`event/FarmstormGameEvents.java:67-79` 的监听数仍 **9**（拦截面换了、数字不动，06 号 §7 第 8 行）。
### 13.5 常见翻车点
- 清单漏登记，或把清单写到 `data/farmstorm/` 下：`LootModifierManager` 只读 `forge` 命名空间那一份（`:53`），修饰器静默不生效、没有日志（RT-011）→ 预防：E-13-6 的路径与 `entries` 判据。
- 把 `ORIGIN` 当 `BlockPos` 用：1.20.1 是 `Vec3`，编译不过或坐标错一位（RT-012）→ 预防：E-13-4 的 `BlockPos.containing` 判据。
- 登记点放在两个跳过分支之前，或不判 `hay.is(Blocks.HAY_BLOCK)`：被挡掉的格子与压缩干草块一起进集合（D-046、RT-017）→ 预防：E-13-2 的两数相等加压缩膨胀麦条数 **0** 变化。
- 改用 `setCanceled(true)` 或 `BlockEvent.BreakEvent`：语义从「清掉落」变成「取消破坏」，与第三方处理器互相干扰（D-048、06 号 §6 第 11 项）→ 预防：E-13-4 的 `setCanceled` 命中 **0**。
### 13.6 完成后必须回报什么
- 记录文件的 `Test-Path` 结果与重启前后的条数、内容。
- 三组对照读数：记录内 / 压缩干草块 / 老存档 的掉落数与条数变化。
- 十二类源码计数（相加 96）与 `global_loot_modifier_serializers` 的 1；启动日志仍读 95 个（第 12 类归 F-043）；两份 GLM JSON 的路径与 `entries` 原文。
- 没做的：老存档里已存在的落点干草块不在记录里、照旧掉落（B-26），本节不回溯扫描。

## §8 Phase 14 · 配方、检查器与收尾（M14，F-040 ~ F-045）
### 14.1 本阶段要做什么
给控制终端一条与两台机器都不相同的 3×3 合成配方（D-029）并补一张单物品战利品表，再把注册面断言的两处同批收到 **96 / 12 类**（D-052：检查器 `EXPECTED_COUNTS` 与 `Farmstorm.java` 的 `logRegistrySelfCheck`），最后更新 `README.md`、写交付清单并做一次数据面复核（datagen 重跑与 32 条验收收口）。本节是 W1 的关窗点。
### 14.2 前置条件
- Phase 13 已交付：**E-13-3**（注册面 96 / 12 类）、**E-13-6**（两份 GLM 数据文件）；F-042 与 F-043 必须同批（顺序纪律④）。
- 现状读数（实读）：`EXPECTED_COUNTS` 12 类相加 **92**（`tools/verify_project.py:458-471`，`:469` 是 1.20.1 不存在的 `data_component_type`，`:448` 要求 `temporary_enchantments` ⇒ 脚本必打 **1** 条 FAIL）；运行期自检是 **11 类 91**（`Farmstorm.java:192-195`，注释 `:191` 写明降类原因，`:172-175` 与 `:205` 仍是旧口径）；配方 JSON **25**、合成配方 **4**、战利品表 **28**、进度 **16**、Java **159**。
- 配方与战利品表先例（实读）：培养器 `IGI` / `GRG` / `IGI`（`ModRecipeProvider.java:122-124`）、转换器 `III` / `GRG` / `III`（`:100-102`）、剥进度的助手 `recipeWithoutAdvancement`（`:174-201`）；单物品表写法 `ModLootTableProvider.java:131-138`、`getKnownBlocks` 在 `:200-231`。窗口纪律：本节关 W1（F-042 之后任何 FAIL 都按缺陷处理），F-042 那一提交只改 `tools/`、`src/` 命中 **0**。
### 14.3 任务清单
| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-040 | 合成配方 | S | 配方 JSON 25 + 1 = **26**、合成配方 4 + 1 = **5**；`GIG` / `IRI` / `GIG` 三个 pattern 各命中 **1**（四角玻璃 + 四边铁锭 + 正中红石）；`IGI` / `GRG` / `IGI` 仍各命中 1；进度仍 **16** |
| F-041 | 战利品表 | S | 方块战利品表 28 + 1 = **29**；石镐挖终端掉落 **1**；`getKnownBlocks()` 里 `CONTROL_TERMINAL` 命中 1 |
| F-042 | 检查器收到 96 / 12 类 | S | `EXPECTED_COUNTS` 12 类相加 **96**（29 / 29 / 9 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 2）；`data_component_type` 的槽位换成 `global_loot_modifier_serializers`；`temporary_enchantments` 命中 **0**；检查器 `96 / 96`、FAIL **0**、WARN ≤ **1**（验收 #1、#2） |
| F-043 | 启动自检收到 96 / 12 类 | S | `Farmstorm.java:192-195` 读 `total == 96` 与 12 类逐项、日志格式串添第 12 组；`:191` 与 `:172-175` / `:205` 的旧口径同批改写；启动日志整行 `自检 注册对象 96 个：… → OK` |
| F-044 | README 与交付清单 | S | `README.md` 里 `06` ~ `11` 六个文件名各命中 1；版本行含 `0.3.0`；交付清单六行三列（行数 / 字节 / SHA256）都有值 |
| F-045 | 数据面复核与 datagen 重跑 | M | `models/block` **79** + `loot_tables` **29** + `recipe` **26** + `blockstates` **29**（验收 #22）；`advancement` **16** + `converting` **21**（验收 #23）；Java **159**（验收 #21）；终端恰 **5** 个产物文件（验收 #28）；32 条验收逐条有读数 |
### 14.4 出口判据
1. **E-14-1 配方 JSON 26 与图样**：怎么验：数 `src\generated\resources\data\farmstorm\recipe` 与 `Select-String -Pattern pattern` 六个图样。通过的样子：**26** 个 JSON（21 + 5）；`GIG` / `IRI` / `GIG` 各命中 **1**、产物是控制终端、数量 **1**；`IGI` / `GRG` / `IGI` 仍各命中 1（两条配方不串）；`advancement` 仍 **16**（解锁进度被剥掉）。
2. **E-14-2 战利品表 29 与 `getKnownBlocks()`**：怎么验：数 `loot_tables/blocks`，再 `Select-String -Path src/main/java/com/dashpig/farmstorm/datagen/ModLootTableProvider.java -Pattern CONTROL_TERMINAL`。通过的样子：**29** 张（28 + 1）；`generate` 与 `getKnownBlocks` 两处各命中 **1**；构建退出码 **0**（漏登记会在 validate 阶段报缺表）。
3. **E-14-3 检查器 96 / 96 且 12 类逐项相等**：怎么验：`python -X utf8 tools/verify_project.py`。通过的样子：§2 打出 `已核 registry 对象: 96 / 96`、12 个分组各打「N 个 ID 齐全」且无 `[FAIL]`；12 类逐项 **29 / 29 / 9 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 2** 与 `EXPECTED_COUNTS` 逐键相等（验收 #1）。
4. **E-14-4 FAIL 0、WARN ≤ 1、既有红清零**：怎么验：同一份输出的末段 `结论：FAIL … 项，WARN … 项` 与全文搜 `data_component_type` / `temporary_enchantments`。通过的样子：FAIL **0**、WARN ≤ **1**；两个字面量在脚本里命中 **0**（验收 #2、RT-019）。注意 §2 是「字面量是否出现在源码里」的弱判据（`:614`），因此 `"puffed_hay"` 必须作为 Java 字符串字面量出现，否则 `global_loot_modifier_serializers` 那一组照样报缺。
5. **E-14-5 启动自检 96 / 12 类（与 F-042 同批）**：怎么验：`Select-String -Path src/main/java/com/dashpig/farmstorm/Farmstorm.java -Pattern "total ==|global_loot"` 与一次真启动日志。通过的样子：`total == 96`、12 类逐项各命中 **1**、日志格式串含第 12 组；日志整行为 `自检 注册对象 96 个：… → OK`；两处断言在同一次构建后可同时读绿（顺序纪律④）。
6. **E-14-6 Java 159 与四个资源计数**：怎么验：`.java` 计数加五条目录计数（`models/block` / `loot_tables/blocks` / `recipe` / `blockstates` / `advancement`）。通过的样子：Java **159**；**79 / 29 / 26 / 29 / 16**；`converting` 仍 **21**；终端恰 **5** 个产物文件（blockstate + block 模型 + item 模型 + 战利品表 + 配方）（验收 #21、#22、#23、#28）。
7. **E-14-7 32 条验收收口与上游零改动**：怎么验：把验收 #1 ~ #32 逐条落进回报——本文 §1 ~ §8 引用到的编号去重后恰为 **32** 个（#10 分挂 §4 的绑定与 §5 的投料、#32 分挂 §7 的落盘与 §8 的收口），再对 06 ~ 09 四份上游跑 `git diff --stat`。通过的样子：32 条各有读数、无一条写「未验证」；四份上游命中 **0** 行；`README.md` diff 只动版本行与文档索引。
### 14.5 常见翻车点
- 图样照抄培养器（`IGI` / `GRG` / `IGI`）：两条同料同图样的 `crafting_shaped` 共存，合成台只稳定命中其中一条（D-029 的补充）→ 预防：E-14-1 的六个 pattern 读数。
- 忘用 `recipeWithoutAdvancement`：进度 16 变 17，D-018 的冻结值当场破 → 预防：E-14-1 的进度 **16**。
- 只改 Java 自检、不改脚本（或反之）：「日志绿、脚本红」的既有错位被固化，两处必须同批到 96 / 12 类（D-052）→ 预防：E-14-3 与 E-14-5 同读。
- 把 `data_component_type` 删掉却不补新类：12 类变 11 类、总数 95，与「12 类 96」不等 → 预防：E-14-4 的字面量命中 **0** 加 E-14-3 的逐键相等。
### 14.6 完成后必须回报什么
- `EXPECTED_COUNTS` 的完整原文、12 行 ID 清单的新增行、`:609` 与 `:621` 两处旧口径注释的改后原文。
- 检查器末段原文：FAIL 0 / WARN ≤ 1 与 `96 / 96` 两行。
- 启动自检整行原文与 `Farmstorm.java:191-195` 的改后原文。
- 六个终态计数（Java 159 / 模型 79 / 战利品表 29 / 配方 26 / blockstates 29 / 进度 16）与终端的 5 个产物文件清单，加 32 条验收的收口表（编号 → 读数 → 结论）。

## §9 数字面四列对照
只有 06 号 D-018 冻结的那些数进这张表；中间读数一律写成算式，任何一行都不许出现这 13 行以外的值（Java 的中间读数 152 / 153 / 154 / 155 / 158 按 06 号 §3.1 那八个增项的落地顺序推得，终态 **159** 由 F-045 复核）。

| 项 | 基线（v0.2.8） | Phase 7 后 | Phase 14 后 | 依据 |
|---|---|---|---|---|
| `mod_version` | 0.2.8 | 0.3.0 | 0.3.0 | D-015、`gradle.properties:11` |
| 注册面 | 91（11 类） | 91（11 类） | 96（12 类） | D-018、facts-A §4 |
| └ items / blocks | 28 / 28 | 28 / 28 | 29 / 29 | D-020、facts-A §4 |
| └ block_entity_type / menu_type | 8 / 1 | 8 / 1 | 9 / 2 | D-025、D-034 |
| └ global_loot_modifier_serializers | 0 | 0 | 1 | D-047 |
| 语言键 | 321 | 321 | 328 | D-018、`ModLangProvider.java:10-16` |
| Java 文件 | 151 | 151 | 159 | facts-A §3.1、06 号 §7 第 9 行 |
| 网络包 / 协议串 / Mixin | 2 / "1" / 0 | 2 / "1" / 0 | 2 / "2" / 0 | D-037、06 号 §4 红线 6 |
| server / client config | 175 / 3 | 175 / 3 | 175 / 3 | facts-A §5、D-032 |
| 战利品表 / 转换配方 / 合成配方 / 配方 JSON | 28 / 21 / 4 / 25 | 28 / 21 / 4 / 25 | 29 / 21 / 5 / 26 | D-018、D-029 |
| 进度 / 方块模型 / blockstates / item 模型 | 16 / 78 / 28 / 28 | 16 / 78 / 28 / 28 | 16 / 79 / 29 / 29 | D-018、facts-A §3.5 |
| 贴图 png / mcmeta / 文件总数 | 84 / 5 / 89 | 88 / 8 / 96 | 88 / 8 / 96 | D-023、facts-A §3.5 |
| 事件监听 / loot modifier 数据文件 | 9 / 0 | 9 / 0 | 9 / 2 | D-047、D-049 |

## §10 禁用词自检
- 八个禁用词逐词搜一遍，命中全部 **0**（词表照 06 号 §0.3 第 3 条，本文不复述词面，避免自证违规）；「man瓜」五处一字未动（`ModLangProvider.java` 的 `:80` / `:114` / `:123` / `:199` / `:213`），见 D-050。
- 表格与围栏：每张表首尾有竖线、逐行列数一致；本文只有 §0 一处围栏块（语法标记 `text`）；末行非空。

## §11 禁写清单
| # | 文件 / 路径 | 禁写区间 | 解禁点 | 依据 |
|---|---|---|---|---|
| 1 | `tools/verify_project.py` | Phase 8 开工 ~ Phase 14 的 F-042 | F-042（W1 关窗） | D-052、验收 #1 / #2 |
| 2 | `datagen/ModLangProvider.java:10-16` | Phase 8 开工 ~ Phase 10 的 F-032 | F-032（W2 关窗） | D-018 的语言键分组、facts-C §10.5 |
| 3 | `docs/zh/06` ~ `docs/zh/09`、`D:/project_farm/**`、`gradle.properties` 以外的第二处版本来源 | 全章 | 永不（只引不改） | 06 号开头、06 号 §4 红线 6、D-015 |

## §12 顺序纪律表
| # | 顺序纪律 | 起点 | 终点 | 违反会怎样 | 落点判据 |
|---|---|---|---|---|---|
| 1 | F-027 先于 F-028（`Kind.TERMINAL` 先于 `KIND_BY_BLOCK` 第 3 条） | Phase 9 的 F-027 | Phase 9 的 F-028 | `Map.of` 引用不到枚举成员，`cannot find symbol`、编译不过——本版唯一「漏改必报错」处 | E-9-6 |
| 2 | F-026 先于 F-030（三态 NBT 先于菜单的两格 `ContainerData`） | Phase 9 的 F-026 | Phase 10 的 F-030 | 菜单要从方块实体取模式与绑定状态，NBT 未落则两格数据无源可读、面板恒读 0 | E-9-1、E-10-2 |
| 3 | F-038 与 F-039 同批（修饰器类 / 注册与两份数据文件） | Phase 13 的 F-038 | Phase 13 的 F-039 | 只落类不落数据文件：修饰器不在清单里、静默不生效，掉落照旧 1（RT-011） | E-13-3、E-13-6 |
| 4 | F-042 与 F-043 同批（检查器与启动自检） | Phase 14 的 F-042 | Phase 14 的 F-043 | 两处注册面断言口径分叉，「日志绿、脚本红」的既有错位重演（D-052、RT-019） | E-14-3、E-14-5 |
| 5 | 史诗四件不拿 F-033 验收 | Phase 11 的 F-033 | Phase 12 的 F-035 之后 | 放行之前培养器仍拒收那四件，读数表现为「投不进去」，会被误判成投料 bug 并去改投料代码 | E-11-7、E-12-5 |
| 6 | F-023 先于 F-025（贴图落地先于模型与 blockstate） | Phase 7 的 F-023 | Phase 8 的 F-025 | 模型与 blockstate 引用未落地的贴图名，游戏里是紫黑格，datagen 校验不报 | E-7-3、E-8-3 |

> 本文自检：8 个 Phase 节（§1 ~ §8，Phase 7 ~ 14）连续无跳号，每节 6 小节；出口判据 **8 × 7 = 56** 条（E-7-1 ~ E-14-7）、段内连续不跳号；顺序纪律表 **6** 行；禁用词命中 **0**；每张表首尾有竖线、逐行列数一致；末行即本行。
