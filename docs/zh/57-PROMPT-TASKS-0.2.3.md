# 57 号 v0.2.3 分阶段任务清单

> 投喂方式：**56 号（主提示词）先投一次、常驻**；此后每个 Phase 开工时**只投本文对应的那一节**，同一时间只投一节。每节从前到后独立可执行——把一节单独交给实现代理，不需要它读过上一节，也不需要它读过本文件的其它节。

| 项 | 值 |
|---|---|
| 文档编号 | 57-PROMPT-TASKS |
| 版本 | v0.2.3（MC 1.21.1 / NeoForge 21.1.213 / Java 21） |
| 读者 | 投喂编码代理的人 + 被投喂的 CLI 编码代理 |
| 上游 | `55-TASKS-0.2.3.md`（**F-286 ~ F-311 的唯一来源**：编号、标题、复杂度、人日、依赖、Phase 归属）/ `54-TECH-0.2.3.md`（§1 五个数、§8 的 **32** 条验收、§9 的 RT-71 ~ RT-82）/ `52-DECISIONS-0.2.3.md`（D-203 ~ D-224）/ `53-DESIGN-0.2.3.md`（B-28 ~ B-58） |
| 同级 | 56 号（常驻主提示词，只投一次，与本文件同批产出）。本文件不重复它的硬约束与红线口径 |
| 内容 | **12 个 Phase / 26 个任务（F-286 ~ F-311）**；每节固定六小节：要做什么 / 前置条件 / 任务清单 / 出口判据 / 常见翻车点 / 必须回报 |
| 出口判据 | 12 节合计 **76** 条（每节 5 ~ 7 条），每条以 `N. **E-<phase>-<n> …**` 开头，编号全局唯一、可 `grep` 计数 |
| 编号约定 | 小节号用 Phase 号打头（`54.1` ~ `65.6`）；文末两节沿用 51 号的 `§9` / `§10` |
| 边界 | 只读上游：不改 `docs/zh/52` ~ `55`、不改 `docs/zh/00` ~ `51`、不改 `docs/en/*`、不改 `src/`、不改 `tools/`、不改 `.work/`；只新建本文件 |

---

## 0. 投喂顺序与固定口径

### 0.1 投喂顺序

**先投 56 号一次**（管红线、硬约束、提交纪律与禁用词，全程常驻）；**此后每个 Phase 开工时只投本文对应的那一节**——投一节、做一节、按该节的「完成后必须回报什么」交一份回报，再投下一节。

三条读法：

1. **一次只投一节**。每节自带前置条件与出口判据，单节脱离其它节也能执行；同时投两节会让两节的「前置条件」互相覆盖，实现代理会按最后读到的那份干活。
2. **投喂顺序不等于唯一可执行顺序**。55 号 §5.2 的五簇并行仍然成立（`F-286` / `F-287` / `F-290` 三者的文件集互不相交），但并行时**一节仍然只投一次**，两节分给两个人。
3. **不跳节**。十二节按 54 → 65 单调；跳过任一节，后面那一节的前置条件里就有一条是「未发生」的读数，并且这一条不会自己变绿。

### 0.2 窗口台账（Phase → F 号范围 → 前置 Phase）

窗口 = **F-287（开，Phase 55）→ F-308（关，Phase 63）**。下表是本文件最重要的一张表：它同时回答「这一节的 F 号是哪几个」「它等谁」「本节处在窗口的哪一段、能断言什么、禁写什么」。

| Phase | F 号范围 | 任务数 | 前置 Phase（等谁） | 窗口段 | 本节可断言 / 禁写 |
|---|---|---|---|---|---|
| 54 | F-286 | 1 | —（首节） | 窗口前 | 可断言 `items` 34 / `blocks` 27 / `menu_type` 2 / item png 29 / 方块 png 84 / mcmeta 2；**禁写 92** |
| 55 | F-287 ~ F-289 | 3 | 54（产物名是本节读数的基线） | 段一 + 段二 | 可断言「`crop_converter` 已在注册表」「`converter` 已不在」「工具 98 / 98」「Java 151」；**禁写 28 / 1 / 92** |
| 56 | F-290 ~ F-291 | 2 | 55（F-287 已改名，配方 id 与方块 id 同一面） | 段二 | 可断言「26 条只留两字段」「21 条」「6 铁 2 玻璃 1 红石」；**禁写任何注册总数** |
| 57 | F-292 ~ F-294 | 3 | 55（F-288 / F-289）、56（F-290 / F-291） | 段二 | 可断言 8 个 variant / 600 刻 / 方块 84 / mcmeta 5 / item 28 / gui 0；**禁写 22 与 92** |
| 58 | F-295 ~ F-296 | 2 | 56（F-291 先删 6 条种子配方，否则本节删物品时 `ModRecipeProvider:86-92` 编译不过） | 段三 | 可断言「7 个种子 id 不存在」「`seeds` 成员 0」「进度 17 → 16」「item 22」；**禁写 28** |
| 59 | F-297 ~ F-298 | 2 | 55（F-287）、58（F-295） | 段四 | 可断言「两个新 id 各 1 个」「`seeds` 成员 1」「Java 152 → 153」；**禁写 92 / 321** |
| 60 | F-299 ~ F-301 | 3 | 56、58、59（配方 21 / 种子删完 / 平凡种子在位） | 段五 | 可断言「投入物 17 个」「4 个同一物品」「Java 152」「`cultivator.json` 存在」；**禁写 92 / 321** |
| 61 | F-302 ~ F-304 | 3 | 57（F-292 / F-293） | 段五 | 可断言协议串 `"3"` / 包数 2 / 青红两色；**禁写 92 / 321** |
| 62 | F-305 ~ F-306 | 2 | 57（F-293） | 段五 | 可断言 config 175 / 60 / 36 与炮弹 300 刻；**禁写 92 / 321** |
| 63 | F-307 ~ F-308 | 2 | 55、57、58、62（要删的键所指向的物件都已下线） | 关窗：语言 F-307 / 计数 F-308 | F-307 可断言 321 × 2；F-308 一次写 `98 → 92` 与工具 `92 / 92` |
| 64 | F-309 ~ F-310 | 2 | 63（F-308 关窗） | 关窗后 | 六个数全部可断言，但只记录、不改码 |
| 65 | F-311 | 1 | 64（F-309 / F-310） | 关窗后 | 八项读数一次给全（签字） |

**四条窗口纪律**（55 号 §0.3 的原文，本节逐字承接）：

1. **不许**把 `92` 写进任何一节的判据，唯一例外是 Phase 63 的 F-308；也不许写 `98`，唯一例外是 Phase 55 的 F-287（原样粘贴启动日志的 98，用来证明「改名不计数」）。
2. **`Farmstorm.java:180-183` 在窗口内一行不动**。从 F-289 起启动日志会打 `FAIL`——这是**窗口内的期望红**，按「期望红」记录，不许当缺陷修，也不许提前把 98 改成 92。
3. **三个面各有一个关窗任务，不许合并**：id 面 F-287、语言面 F-307、计数面 F-308。合并任两个，第三个面就在没有安全网的状态下被改。
4. **窗口内的判据只写「本条对象的存在性 + 本条自己的计数」**（贴图 84 / 配方 21 / 投入物 17 / 标签 1 个）。

### 0.3 每节开头原样贴的六条

```text
① 红线：Mixin 0（src/ 口径，@Mixin 与 *.mixins.json 均 0 命中）；网络包 2 个不变，
   PROTOCOL_VERSION 由 "2" 升 "3"（与 F-302 同一提交），本版不新增第三个包。
② 中间态窗口 = F-287（开）→ F-308（关）。窗口内只写「本条对象存在 / 不存在」+「本条自己的计数」；
   92 / 28 / 1 / 321 四个数分别在 F-308 / F-307 各出现一次，其余各节出现 0 次。
③ Farmstorm.java:180-183 的等式组窗口内一行不动；F-289 起日志打 FAIL 是期望红，不许提前改 92。
④ 贴图四个数各归各的任务：方块 png 84（F-294）、mcmeta 5（F-294）、item png 22（F-295）、gui 0（F-294）。
⑤ config 验收前先删（或改名备份）run/config/farmstorm-server.toml；老 toml 的 2 / 16 是期望红，
   必须与新 config 的绿成对跑。
⑥ 判据只认五类读数：计数 / 日志整行 / /data get / 源码 grep 命中 / 像素比对；不写「正常」「手感」。
```

### 0.4 速查表

| Phase | 里程碑 | 任务号 | 个数 | 人日 | 出口判据（一句话） | 本节点数 |
|---|---|---|---|---|---|---|
| 54 发布面 | M50 | F-286 | 1 | 0.5 | 产物 `farmstorm-0.2.3+mc1.21.1.jar`；`0.2.2` 在 6 处字面量里归 0 | 5 |
| 55 转换器身份 | M51 | F-287 ~ F-289 | 3 | 6.0 | 全仓只剩 `crop_converter`；`menu_types` 1；Java 151 | 7 |
| 56 配方结构与配方表 | M52 | F-290 ~ F-291 | 2 | 3.0 | 21 条单向无成本；6 铁 2 玻璃 1 红石 | 6 |
| 57 转换器行为与资源面 | M53 | F-292 ~ F-294 | 3 | 7.5 | 8 个 variant；四步状态机；84 / 5 / 28 / 0 | 7 |
| 58 删种子与进度 | M54 | F-295 ~ F-296 | 2 | 2.0 | `seeds` 空集；进度 17 → 16；item 贴图 22 | 6 |
| 59 平凡作物 | M55 | F-297 ~ F-298 | 2 | 3.0 | 种子能种下去；`blocks` 28 / `items` 28 | 6 |
| 60 培养器判据与产出 | M56 | F-299 ~ F-301 | 3 | 3.5 | 17 个投入物；2 + 2；`cultivator.json` 存在 | 7 |
| 61 红线 `Kind` | M57 | F-302 ~ F-304 | 3 | 6.0 | 协议串 `"3"`；包数 2；换机 10 刻内变色 | 7 |
| 62 config 与炮弹 | M58 | F-305 ~ F-306 | 2 | 2.0 | 175 / 60 / 36；炮弹第 300 刻消失 | 6 |
| 63 语言键与自检收口 | M59 | F-307 ~ F-308 | 2 | 3.0 | `zh = en = 321`；日志 `92 … → OK` | 7 |
| 64 验收与边界 | M60 | F-309 ~ F-310 | 2 | 6.0 | 32 + 31 条一条不漏 | 6 |
| 65 签字与 README | M61 | F-311 | 1 | 1.5 | 八项读数 + 19 / 19 + README 三段 | 6 |
| **合计** | **M50 ~ M61** | **F-286 ~ F-311** | **26** | **44.0** | 裸 44.0 人日（`4 × 0.5 + 16 × 1.5 + 6 × 3.0`），含 25 % 缓冲 **55.0** | **76** |

### 0.5 关键事实（照抄口径，一个字不许自创）

| 口径 | 0.2.2 | v0.2.3 | 算式 / 依据 |
|---|---|---|---|
| 注册面 | 98 | **92** | `items 28 / blocks 28 / BE 8 / entity 13 / mob_effect 4 / sound 3 / recipe_type 1 / recipe_serializer 1 / creative_tab 1 / damage_type 3 / data_component 1 / menu_type 1`；`98 − 7 + 1 + 1 − 1 = 92` |
| server config | 176 键 / 60 push | **175 键 / 60 push** | `176 − 3 + 2 = 175`；`[converter]` 换成 `[crop_converter]`，push 仍 60 |
| 语言键 | 340 × 2 | **321 × 2** | `340 − 23 + 4 = 321` |
| Java 文件 | 154 | **152** | `154 − 3（GUI 三件）− 1（`SeedItem`）+ 2（`PlainCropBlock` / `PlainSeedItem`）= 152` |
| 转换配方 | 26 | **21** | `26 − 6 + 1 = 21`，全部单向、无经验、无催化剂 |
| 培养器投入物 | 0（原版进不去） | **17** | `21 − 4 = 17`（火把花 / 瓶子草荚 / 甜浆果 / 金苹果） |
| 进度 | 18 | **16** | `18 − 2 = 16`（`seed_cycle` / `nether_star_toll`） |
| 方块贴图 | 84 png / 2 mcmeta | **84 png / 5 mcmeta** | 删 7 张加 7 张；mcmeta **2 → 5** |
| item 贴图 | 29 | **22** | `29 − 1 − 6 = 22`（重命名不计增删） |
| `#farmstorm:seeds` | 6 | **1** | `6 − 6 + 1 = 1`（`plain_chives_seeds` 顶上，标签不会空） |
| `PROTOCOL_VERSION` | `"2"` | **`"3"`** | `Link` 从 2 字段变 3 字段，codec 变了就必须升 |

---

## Phase 54 · 发布面（M50）

### 54.1 本阶段要做什么

把 `mod_version` 推到 **0.2.3**，并把两支 `.cmd` 里写死的 jar 名与抬头行一次改齐 **6 处字面量**。这一节只碰发布面：它是全部 jar 名与「检查环境」判据的唯一来源，也是后面十一节「产物存在」类读数的前提；`src/` 与 `run/` 一行不动，注册面与语言面一个字不提。

### 54.2 前置条件

- 工作树干净：`git status` 只出现本节要改的 **3** 个文件（`gradle.properties`、`一键构建.cmd`、`检查环境.cmd`）。
- `.tools\gw.cmd build` 当前退出码 **0**；基线是 **0.2.2 已实现态**（git `79b71f1`），`gradle.properties:11` 是 `mod_version=0.2.2`，`build/libs` 此刻有 `farmstorm-0.2.2+mc1.21.1.jar`。
- 版本串按**行**是 **6** 处：`gradle.properties:11` 1 处 + `一键构建.cmd:5` / `:11` / `:12` 3 处 + `检查环境.cmd:5` / `:14` 2 处。不是 4 处，也不是 5 处。
- 本节在**中间态窗口之前**（窗口由 F-287 打开）：`src/` 与 `run/` 的 diff 必须为空；本节判据里 `92` / `175` / `321` / `152` 出现 **0** 次。
- 上一节：**本节是第一节**；本节 54.4 的读数就是 55.2 的前置。

### 54.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-286 | 版本号 0.2.3 与 6 处版本串 | S | `build/libs` 里 `farmstorm-0.2.3+mc1.21.1.jar` 与 `-sources.jar` 同时在位；`0.2.2` 在三个文件里命中 **0**、`0.2.3` 命中 **6** |

### 54.4 出口判据

1. **E-54-1 产物名与版本行**：怎么验：`.tools\gw.cmd build` 后 `jar tf build/libs/farmstorm-0.2.3+mc1.21.1.jar` 读 `META-INF/neoforge.mods.toml`。通过的样子：退出码 **0**；主 jar 与 `farmstorm-0.2.3+mc1.21.1-sources.jar` 同时存在；`build/libs` 里 `farmstorm-0.2.2*` 残留 **0** 个；`version` 行解析为 `0.2.3`。
2. **E-54-2 六处字面量一次改齐**：怎么验：三个文件逐个数。通过的样子：`0.2.2` 命中 **0**；`0.2.3` 命中 `1 + 3 + 2 = 6`，其中两处抬头行（各 `:5`）与 `一键构建.cmd:12` 的 `-sources.jar` 都在内。
3. **E-54-3 平台三行未动（负向）**：怎么验：`git diff gradle.properties` 的 hunk 数。通过的样子：只有第 **11** 行变化；`minecraft_version` / `neo_version` / toolchain 三行逐字未变（1.21.1 / 21.1.213 / 21）。
4. **E-54-4 本节不碰数值面（负向）**：怎么验：`git status` 与 `git diff --stat src/ run/`。通过的样子：两处 diff 均为空；本节 5 条判据里 `92` / `175` / `321` / `152` 出现 **0** 次。
5. **E-54-5 版本号单一来源（负向）**：怎么验：`grep -rn "0\.2\.3" src/main/java`。通过的样子：命中 **0**；`0.2.2` 在 `src/main/java` 同样命中 **0**——版本号只在 `gradle.properties:11` 一处来源。

### 54.5 常见翻车点

- **只改主 jar、漏 `-sources.jar`**：`一键构建.cmd:12` 仍按旧名复制，sources 静默不产出；预防：6 处逐行点名，E-54-2 的命中数合计必须 = 6。
- **只改 jar 名、漏抬头行**：产物对了、横幅还写 `Farmstorm v0.2.2`；预防：两支脚本各 `:5` 单独算一处，抬头与 jar 名分开数。
- **顺手改平台参数或顺手写 92**：构建缓存全废，或提前把窗口的总数写进发布面；预防：`git diff gradle.properties` 只允许 1 行，E-54-4 的四串命中 0。

### 54.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；6 处字面量的「文件:行号」逐处点名。
- **跑了什么命令、看到什么**：`.tools\gw.cmd build` 的退出码、`jar tf` 里 `neoforge.mods.toml` 的条目、三个文件的两轮 `grep -c`、`git diff gradle.properties` 的行数；读数是 `build/libs` 的完整清单（含 `0.2.2*` 残留 0 个）、`version` 行原文、一次真跑 `检查环境.cmd` 的输出。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如 `.cmd` 在非 Windows 环境跑不了，写明替代读数）。

---

## Phase 55 · 转换器身份：改名、属性与界面下线（M51）

### 55.1 本阶段要做什么

把 `farmstorm:converter` 换成 `farmstorm:crop_converter`（方块、方块物品、方块实体三个注册点 + 两个类名），把方块属性整段换成**铁砧七项**并进 `farmstorm:processing_block` 标签，把界面整套下线（三个文件 + `MenuType` + 配方书 finder 内联）。本节 F-287 **打开中间态窗口**：id 面在这一节换，计数面要到 Phase 63 的 F-308 才收口。

### 55.2 前置条件

- 54.4 的 E-54-1 / E-54-2 / E-54-3 已绿；基线（UTF-8 读法）：`FarmstormBlocks.java:71-72`、`FarmstormItems.java:112-113`、`FarmstormBlockEntities.java:43-45`、`FarmstormMenus.java:12,17-18`、`FarmstormClient.java:62,73-76`，`ConverterMenu` 216 行 / `ConverterScreen` 86 行 / `ConverterRecipeBookCategory` 24 行。
- 窗口在本节 F-287 打开：id 面换、计数面不动；本节**禁写** 28 / 1 / 92，允许原样粘贴 98（全文唯一例外）。
- 冻结面：`ConverterBlockEntity` 的 `Container` 契约本节不碰（留给 Phase 57 的 F-293）；`CultivatorLinkPayload.MAX_LINKS = 1024` 不动。
- 手边：55 号 §0.5 死命令 1 与 Phase 55 的三个任务；54 号 §2.1、§2.6、§8 #1 / #3 / #30 / #31。

### 55.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-287 | 三处注册改名 + 两个类改名 + 编译期引用（**开窗**） | L | 启动日志整行仍是 `98 … → OK`；`src/main/java` 下 `ConverterBlock` 与 `FarmstormBlocks.CONVERTER` 各命中 **0** |
| F-288 | 铁砧七项属性 + 进 `processing_block` 标签 | M | `FarmstormBlocks.java:71-72` 五项全中、`3.5F` 与 `MapColor.STONE` 归 0；`processing_block.json` 的 `values` 恰 **2** 项 |
| F-289 | 删界面三件套 + `MenuType` + finder 内联 | M | Java 文件 **154 → 151**；`menu_types` 落 1（日志期望红）；`registerRecipeCategoryFinder(` **1** 处、`Unknown recipe category` **0** 条 |

### 55.4 出口判据

1. **E-55-1 启动日志整行（F-287）**：怎么验：起服读 `run/logs/latest.log` 里 `Farmstorm.java:184-188` 打出的那一行。通过的样子：原文是 `自检 注册对象 98 个：items 34 / blocks 27 / … / menu_types 2 → OK`，末字 `OK`；这一条是全文唯一允许写 98 的判据。
2. **E-55-2 工具 98 / 98（F-287）**：怎么验：`python -X utf8 tools/verify_project.py`。通过的样子：打印 `已核 registry 对象: 98 / 98`、`结论：FAIL 0 项`；`EXPECTED["items"]` 与 `EXPECTED["blocks"]` 里 `crop_converter` 各 **1** 处、`converter` 各 **0** 处。
3. **E-55-3 改名残留（F-287）**：怎么验：`src/main/java` 下 grep 三个串。通过的样子：`ConverterBlock` 命中 **0**、`FarmstormBlocks.CONVERTER` 命中 **0**；`"converter"` 只剩进度 id 与注释里的旧名，共 **≤ 3** 行。
4. **E-55-4 Java 文件 154 → 151（F-289）**：怎么验：数 `src/main/java` 的 `*.java` + 查三个路径。通过的样子：**151**；`ConverterMenu.java` / `ConverterScreen.java` / `ConverterRecipeBookCategory.java` 都不存在；`FarmstormMenus` 里 `REGISTER.register(` 恰 **1** 行（`outpost_core`）、`"converter"` 命中 **0**。
5. **E-55-5 属性五项（F-288）**：怎么验：读注册行 + 游戏内三次动作。通过的样子：`MapColor.METAL` / `strength(5.0F, 1200.0F)` / `requiresCorrectToolForDrops` / `SoundType.ANVIL` / `PushReaction.BLOCK` **五项全中**；空手挖不掉落、木镐挖掉本体、TNT 贴脸炸方块仍在。
6. **E-55-6 两个标签产物（F-288）**：怎么验：datagen 后读两份 json。通过的样子：`data/farmstorm/tags/block/processing_block.json` 的 `values` 恰 **2** 项（`farmstorm:cultivator` 与 `farmstorm:crop_converter`）；`minecraft:mineable_with_pickaxe` 含 `farmstorm:crop_converter`。
7. **E-55-7 finder 内联与期望红（F-289）**：怎么验：读 `FarmstormClient` + 起客户端看日志。通过的样子：`registerRecipeCategoryFinder(` **1** 处、`ConverterRecipeBookCategory` 与 `ConverterScreen` 各命中 **0**；`run/logs/latest.log` 里 `Unknown recipe category` 命中 **0**；启动日志 `menu_types 1` 整行 `→ FAIL` 按「期望红：`menu_types` 已 1，断言仍 2，F-308 收口」记录。

### 55.5 常见翻车点

- **改名漏一个 datagen provider**：`ModBlockStateProvider:181` 或 `ModItemModelProvider:69` 仍写旧名 ⇒ datagen 抛 `IllegalStateException`，而编译是过的；预防：E-55-3 的三串归零。
- **把 `FarmstormClient:75` 整行删掉**：21 条 `converting` 配方各刷一条 `Unknown recipe category` WARN；预防：类文件照删、finder **内联**进 `FarmstormClient`，E-55-7 直接数 WARN。
- **顺手把 92 写进 `Farmstorm.java`**：两套闸（启动自检与检查器）立刻相反；预防：窗口纪律 1，本节判据里 92 / 28 / 1 出现 0 次。

### 55.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；三处注册行与两个类名的「改前 → 改后」；三个被删文件的路径与行数。
- **跑了什么命令、看到什么**：`.tools\gw.cmd build` 退出码、`python -X utf8 tools/verify_project.py` 的完整输出、一次 datagen、每一串 grep 的命中数；读数是 E-55-1 的日志整行**原文**、`processing_block.json` 与 `mineable_with_pickaxe.json` 的完整内容、`registerRecipeCategoryFinder(` 那一行的原文。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如 TNT 抗性读数在单人创造下如何复现，写明替代读数）。

---

## Phase 56 · 配方结构与配方表（M52）

### 56.1 本阶段要做什么

把 `farmstorm:converting` 从「7 字段 + 双向 + 收经验 + 收催化剂」压成「2 字段 + 单向 + 无成本」，条数从 **26** 压到 **21**，并把转换器的合成配方从「8 个燃料围一个工作台」换成 **6 铁锭 + 2 玻璃 + 1 红石粉**。本节不动注册面、不动语言面、不动贴图。

### 56.2 前置条件

- 55.4 的 E-55-1 / E-55-3 / E-55-4 已绿（改名到位、Java 151）。
- 基线：`ConverterRecipe.java` 112 行（七字段 `:19-25`、七参构造器 `:27-28`、`reverse()` `:106-110`），`ModRecipeProvider.java:67-99` 的 26 行、`:187-196` 的 `twoWay`、`:199-209` 的 `oneWayWithNetherStar`；产物侧 `data/farmstorm/recipe/` 里 `"type": "farmstorm:converting"` 现 **26** 个。
- **死命令 2**：字段收缩必须与 `ModRecipeProvider` 的两个助手**同一提交**，否则 7 个实参喂 2 个形参，当场编译不过。
- 冻结面：`recipe_type` / `recipe_serializer` 各 **1** 不变；三条合成配方（`crop_converter` / `outpost_core` / `compressed_puffed_wheat`）本节只改 `crop_converter` 一条。
- 手边：55 号 F-290 / F-291 与 §0.5 死命令 2；54 号 §4.2.1、§4.2.2、§8 #9 ~ #12。

### 56.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-290 | `ConverterRecipe` 七字段 → 二字段 + 序列化器 | M | `xpCost` / `catalyst` / `catalystCount` / `bidirectional` / `reverseXpCost` / `reverse(` 各命中 **0**；`grep -c 'new ConverterRecipe('` = **26** |
| F-291 | 配方表 26 → 21 + 转换器合成配方替换 | M | `converting` JSON **21** 个；六个种子配方文件名全不存在；`crop_converter.json` 是 **6 铁 2 玻璃 1 红石** |

### 56.4 出口判据

1. **E-56-1 字段收缩（F-290）**：怎么验：读 `ConverterRecipe` 与序列化器。通过的样子：五个字段与 `reverse(` 各命中 **0**；`public ConverterRecipe(` 恰 **1** 处、形参 **2** 个；`MapCodec` 与手写 `StreamCodec` 都只剩两段。
2. **E-56-2 调用点仍是 26（F-290）**：怎么验：`grep -c 'new ConverterRecipe(' src/main/java`。通过的样子：**26**（本任务只改形状、不改条数）；`NETHER_STAR` 在 `ModRecipeProvider.java` 命中 **0**。
3. **E-56-3 产物只留两字段（F-290）**：怎么验：数产物 json + `/reload` 看日志。通过的样子：`"type": "farmstorm:converting"` 仍 **26** 个，每个只有 `input` / `result` 两个字段、无 `xp_cost` / `catalyst`；日志里没有 `Couldn't parse recipe`。
4. **E-56-4 21 条（F-291）**：怎么验：数 `data/farmstorm/recipe/` 里 `"type": "farmstorm:converting"` 的 json。通过的样子：**21** 个；`puffed_wheat_seeds` / `power_beet_seeds` / `melon_grenade_seeds` / `pumpkin_turret_seeds` / `awakened_torchflower_seeds` / `awakened_pitcher_plant_pod` 六个文件名全不存在；`friend_chives.json` 存在且 `input` 侧物品 = `farmstorm:plain_chives`。
5. **E-56-5 转换器合成配方（F-291）**：怎么验：读 `crop_converter.json`。通过的样子：`minecraft:iron_ingot` **6**、`minecraft:glass` **2**、`minecraft:redstone` **1**；配方 id 的字面量是 `farmstorm:crop_converter`。
6. **E-56-6 目录总数与不改面（F-291 / 负向）**：怎么验：数整个目录 + 读两个注册行。通过的样子：`data/farmstorm/recipe/` = **24** 个文件（21 条 `converting` + 3 条合成）；`recipe_type` / `recipe_serializer` 各 **1**；`Farmstorm.java:180-183` 一字未动。

### 56.5 常见翻车点

- **先删字段后改助手（或反过来）**：7 个实参喂 2 个形参，编译不过；预防：死命令 2，E-56-1 与 E-56-2 必须同一次 build 通过。
- **把 `converting` 类型删掉、改成代码里的静态映射表**：21 条 JSON 全部读不出（52 号 §6 第 3 条已否决）；预防：类型与序列化器各 1 不变，E-56-6。
- **顺手把两条史诗配方也删了**：21 变 19，Phase 60 的 17 个投入物变 15 个；预防：E-56-4 的 21 与两条史诗仍在。

### 56.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；字段「七 → 二」的前后对照；两个助手的方法签名。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、`grep -c 'new ConverterRecipe('`、一次 datagen、`/reload` 后的日志片段；读数是 21 个配方文件名清单、`friend_chives.json` 与 `crop_converter.json` 的完整材料计数、`FAIL 0 项` 那一行原文。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如旧数据包读失败属已知限制，写明只登记不修）。

---

## Phase 57 · 转换器行为与资源面（M53）

### 57.1 本阶段要做什么

把改过名的方块从「右键无反应的装饰方块」变成真正会工作的机器：`WORKING` 属性、`getTicker`、单槽 + 进度 + 绑定核的**四步状态机**、自动投递进核、拆方块掉输入；同时把 7 张新贴图 + 3 个 mcmeta 落位，生成 **8 个 variant** 与**两张模型**。这是本版改动量最大的一节（7.5 人日、两个 L）。

### 57.2 前置条件

- 55.4 的 E-55-5 / E-55-6 已绿（属性、标签）；56.4 的 E-56-4 已绿（21 条配方在位）。
- 基线：`ConverterBlockEntity` 435 行、`implements Container`、双槽（输入 + 催化剂），可照抄的先例是 `CultivatorBlock.java:44`（`WORKING`）、`:86-102`（`setPlacedBy`）、`:113-128`（`useWithoutItem`）、`:140-147`（`onRemove`）、`:150-161`（`getTicker`）；资源基线：`textures/block/` 现 **84** 张 png + **2** 个 mcmeta，`textures/item/` 现 **29** 张（含 `converter.png`），`textures/gui/` 现 **1** 张。
- **本节按代码常量验 600 刻**：`crop_converter.processTicks` 由 Phase 62 的 F-305 才加入，本节读不到该键；差异与复核方式登记在 §9 第 11 行。
- 冻结面：`MelonGrenadeProjectile` / `MelonSliceProjectile` 不动；`MAX_LINKS = 1024` 不动（红线在 Phase 61）。

### 57.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-292 | `CropConverterBlock` 行为 + 8 个 variant + 两张模型 | L | `blockstates/crop_converter.json` 恰 **8** 个 variant；`WORKING` / `getTicker` / `setPlacedBy` / `onRemove` 各 **1** 处、`getMenuProvider` **0** |
| F-293 | `CropConverterBlockEntity` 四步状态机重写 | L | 类里 `implements Container` **0**、NBT 键 **3** 个；投料后第 **600** 刻核里多 **1** 个产物 |
| F-294 | 转换器的贴图面：7 新增 + 3 mcmeta + 7 删除 + gui 清空 | M | 方块 png **84** / mcmeta **5** / item png **28** / gui **0** |

### 57.4 出口判据

1. **E-57-1 8 个 variant（F-292）**：怎么验：读 `blockstates/crop_converter.json`。通过的样子：variant = **8**；`working=false` 指 `farmstorm:block/crop_converter`、`working=true` 指 `farmstorm:block/crop_converter_working`；四个 `y` 依次 `0 / 90 / 180 / 270`；`uvlock` 全为 `true`。
2. **E-57-2 两张模型的六面（F-292）**：怎么验：读两个 model 文件。通过的样子：`parent` 都是 `minecraft:block/cube`；`north` = `_front_still` / `_front`、`up` = `_top_still` / `_top`、`down` = `_bottom`（两态同值）、`south` / `east` / `west` = `_side_still` / `_side`；`particle` 取 `_front` 那一张。
3. **E-57-3 成员与状态机抽头（F-292 / F-293）**：怎么验：逐串 grep 两个类。通过的样子：`WORKING` 声明 **1**、`getRenderShape` **1**、`getTicker` **1**、`setPlacedBy` **1**、`onRemove` **1**、`getMenuProvider` **0**、`HORIZONTAL_FACING` 引用 **1**；`implements Container` **0**、`ContainerData` **0**、NBT 键恰 `Held` / `Progress` / `Core` **三个**。
4. **E-57-4 第 600 刻产出（F-293）**：怎么验：放核 → 放转换器 → 手持小麦右键，逐刻读 `/data get block <pos> Progress`。通过的样子：第 **599** 刻读到 **599**；第 **600** 刻核里多 **1** 个 `farmstorm:puffed_wheat`，槽清空、`working=false`。
5. **E-57-5 核满暂停不丢（F-293）**：怎么验：把核的 54 格塞满后投料、等 **700** 刻，再腾 1 格。通过的样子：`progress` 停在 **600**、槽里小麦仍在、`working=false`、核里没有产物；腾出 1 格后**下一 tick** 完成投递，不重新计时。
6. **E-57-6 拆除与不可取回（F-293 / 负向）**：怎么验：工作中挖掉方块；槽里有货时空手右键与换物右键。通过的样子：掉出**那 1 个**输入物、产物不掉地、`progress` 归零；两种右键都**无反应**（`PASS`）、不取出、不替换、不消耗手持物。
7. **E-57-7 资源四数（F-294）**：怎么验：数三个目录 + 读三个 mcmeta。通过的样子：方块 png **84** / mcmeta **5** / item png **28** / gui **0**；`converter_*.png` 在 `textures/block/` 命中 **0**；三个 mcmeta 与 `{"animation":{"frametime":2}}` 逐字相同；空闲时正面是静态图、投料后开始逐帧变化。

### 57.5 常见翻车点

- **把动态 PNG 的暂停交给代码**（红线 4）：空闲时动画照播，看起来像还在工作；预防：两张模型 + 静态副本不带 mcmeta，E-57-2 的 `_still` 指向逐字核。
- **先清槽再投递**：`acceptAll` 失败时产物凭空消失（RT-74），玩家的 30 秒白等；预防：清槽语句必须在 `acceptAll` 返回 `true` 之后，E-57-3 读顺序、E-57-5 用核满实测。
- **回滚 F-294 不同时回滚 F-292**：blockstate 仍引用 7 个新文件名 ⇒ 模型丢贴图变紫黑格；预防：两个任务的回滚在 55 号的「回滚影响」里绑定。

### 57.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；两个类的成员清单；7 张新 png 与 3 个 mcmeta 的落位路径。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、一次 datagen、逐刻 `/data get` 的取样命令、三个目录的计数命令；读数是 8 个 variant 的完整片段、两张模型六面的贴图名、600 刻与 700 刻两组读数、资源四数。
- **没做的与做不了的**：一条一行写「未验证」与原因，其中必须有一条写明「`processTicks` 键尚未加入，本节的 600 刻是代码常量」并给出 Phase 62 的复核任务号。

---

## Phase 58 · 删种子与进度（M54）

### 58.1 本阶段要做什么

把 7 个种子物品与它们的全部引用面清干净（物品、档案、战利品表、标签、6 张图；6 条种子配方已在 Phase 56 删除），删掉依赖种子的 `seed_cycle`，再把 `chive_seeds.png` **重命名**成 `plain_chives_seeds.png`，给 Phase 59 的新物品留位。

### 58.2 前置条件

- 56.4 的 E-56-4 已绿（21 条配方；6 条种子转换配方已在 F-291 删除），57.4 的 E-57-7 已绿（item png 28）；基线：`FarmstormItems.java:91`（`CHIVE_SEEDS`）、`:102-109`（6 条）、`:35`（`SEED_ITEMS`）、`:175-177`（`seedItem(...)`）、`:140-151`（创造栏），`CropProfile` 的 7 处显式种子路径与 15 条自指，`ModLootTableProvider.java:71,81,84,86,89,91,105` 七处。
- **死命令 3**：删 7 个种子必须与删 `seed_cycle` **同一提交**；早删或晚删都是编译不过或 datagen 崩。
- 冻结面：`data/farmstorm/tags/item/converter_fuel.json` 本节**不动**（Phase 65 要逐字证它还在）；`crop(...)` 工厂与 15 个改良作物方块一行不动（裁定 6）。
- 手边：55 号 F-295 / F-296 与 §0.5 死命令 3；54 号 §2.3、§8 #21 / #27。

### 58.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-295 | 删 7 个种子 + 档案置空 + 战利品表 + `seed_cycle` + 6 张图 | M | 七个 id 在 `src/main/java` 与 `src/generated/` 各命中 **0**；`seeds.json` 的 `values` 是空数组；item png **22** |
| F-296 | 删 `nether_star_toll` 与进度面收口 | S | `data/farmstorm/advancement/` = **16** 个 json；两个 id 命中 **0** |

### 58.4 出口判据

1. **E-58-1 七个种子归零（F-295）**：怎么验：七个串分别在两个范围里数。通过的样子：`src/main/java` 与 `src/generated/` 下每个串各命中 **0**；`SeedItem` 的 import 与 `seedItem(...)` 工厂同步消失。
2. **E-58-2 标签空集（F-295）**：怎么验：读两份标签产物。通过的样子：`data/farmstorm/tags/item/seeds.json` 的 `values` 是**空数组**（`6 − 6 = 0`）；`converter_fuel.json` **仍存在**且内容未变。
3. **E-58-3 item 贴图 22 与改名（F-295）**：怎么验：数 `textures/item/*.png` + 查两个文件名。通过的样子：**22** 张（28 − 6，重命名不计）；`plain_chives_seeds.png` 存在、`chive_seeds.png` 不存在。
4. **E-58-4 战利品表去种子（F-295）**：怎么验：读一张作物表。通过的样子：`data/farmstorm/loot_table/blocks/friend_chives_crop.json` 里 `farmstorm:chive_seeds` 命中 **0**；15 张作物表的作物本体掉落条目一条不少。
5. **E-58-5 进度 17 条（F-295）**：怎么验：数 `data/farmstorm/advancement/` + 游戏内用剪刀。通过的样子：**17** 个 json；`seed_cycle.json` 不存在；对成熟作物用剪刀 ⇒ 掉落 **0** 个种子（`getBaseSeedId()` 返回 `Items.AIR`）。
6. **E-58-6 进度 16 条与期望红（F-296）**：怎么验：再数一次 + 读启动日志。通过的样子：**16** 个 json；`nether_star_toll.json` 不存在；`converter.json` 仍在且判据是 `crop_converter`；`first_conversion.json` 仍在；两个删掉的 id 在 `src/` 与 `src/generated/` 命中 **0**；日志 `items 27` 整行 `→ FAIL` 按「期望红：`items` 已 27，断言仍 34，F-308 收口」记录。

### 58.5 常见翻车点

- **先删物品后改战利品表**：datagen 阶段 `BuiltInRegistries.ITEM.get(key)` 拿到 `null` 直接崩；预防：同一提交，E-58-1 与 E-58-4 一起看。
- **把 `CropProfile.seedItem` 字段一起删**：`FarmstormCropBlock.getBaseSeedId()` 的 `null` 分支没处落，编译不过；预防：字段保留、22 条的值全置 `null`（52 号 D-212 补充）。
- **顺手删 `converter_fuel` 标签**：Phase 65 的「不改清单」当场红，而 52 号 D-209 并未要求删它；预防：E-58-2 的后半句。

### 58.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；7 个种子的删除点与 6 张图的文件名；`chive_seeds.png → plain_chives_seeds.png` 的改名证据。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、一次 datagen、两个范围的七个 grep、两个目录的文件计数；读数是 `seeds.json` 的完整内容（空数组）、进度目录的 json 数（17 → 16）、日志 `items 27` 那一行原文（标「期望红」）。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如剪刀掉落需要成熟作物，写清怎么用 `/setblock` 造一株）。

---

## Phase 59 · 平凡作物（M55）

### 59.1 本阶段要做什么

把平凡韭菜从一个「吃了回 2 点饱食度的物品」升级成**可种植作物**：新的 `PlainCropBlock` 与 `PlainSeedItem`、4 阶段 blockstate、1 张掉落表；种子必须真的能种下去，且**不许**复用 `SeedItem`（它的 `useOn` 无条件返回 `PASS`）。

### 59.2 前置条件

- 58.4 的 E-58-1 / E-58-3 已绿（7 个种子清零、`plain_chives_seeds.png` 已在位）；55.4 的 E-55-3 已绿（改名到位）。
- 基线：方块注册插在 `FarmstormBlocks.java:102-104` 之间，物品注册插在 `FarmstormItems.java:109` 之后；`CropProfile` 仍 **22** 条；`textures/block/` 仍 **84** 张（复用 `friend_chives_stage0~3.png`，本节新增 **0** 张）；**红线 2**：种子不许复用 `SeedItem`（它的 `useOn` 无条件 `PASS`），新类必须是 `PlainSeedItem extends ItemNameBlockItem`，`useOn` 走原版放置流程。
- 冻结面：`CropProfile.ALL` 仍 22 条、`LUCKY_BERRY_POOL` 与 `#farmstorm:modified_crops` 不动；`#farmstorm:seeds` 这个 `TagKey` 本身不删、只换成员。
- 手边：55 号 F-297 / F-298；54 号 §3.5、§4.3.4、§8 #20 / #21。

### 59.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-297 | `PlainCropBlock` + `plain_chives_crop` + 4 阶段 blockstate | M | `blockstates/plain_chives_crop.json` 恰 **4** 个 variant；四个模型的 `textures.crop` 指 `friend_chives_stageN`；`blocks` **27 → 28** |
| F-298 | `PlainSeedItem` + `plain_chives_seeds` + 标签 + 模型分派 | M | `seeds.json` 恰 **1** 项；种子右键耕地能种下去；`items` **27 → 28** |

### 59.4 出口判据

1. **E-59-1 4 阶段 blockstate 与模型（F-297）**：怎么验：读 blockstate 与四个 model。通过的样子：variant 恰 **4** 个（`age=0..3`）；`models/block/plain_chives_stage0..3.json` 四个文件存在，`textures.crop` = `farmstorm:block/friend_chives_stage0..3`（**不是** `plain_chives_stageN`）。
2. **E-59-2 贴图不新增（F-297 / 负向）**：怎么验：数 `textures/block/*.png`。通过的样子：仍 **84**；`textures/block/plain_chives_stage0.png` 不存在。
3. **E-59-3 注册与档案（F-297）**：怎么验：读日志与 `CropProfile`。通过的样子：`blocks` 落 **28**；`CropProfile` 的档案数仍 **22**；Java 文件 **151 → 152**。
4. **E-59-4 种子与标签（F-298）**：怎么验：读 `seeds.json` 与类注释。通过的样子：`items` 落 **28**；`values` 恰 **1** 项 `farmstorm:plain_chives_seeds`；`FarmstormTags.java:35` 的注释里 `6 个独立播种物` 命中 **0**。
5. **E-59-5 模型分派（F-298）**：怎么验：读 `models/item/plain_chives_seeds.json`。通过的样子：`parent` = `minecraft:item/generated`、`layer0` = `farmstorm:item/plain_chives_seeds`，该 png 存在（F-295 已改好名）。
6. **E-59-6 能种（F-298）**：怎么验：右键耕地、骨粉、破坏。通过的样子：生成 `farmstorm:plain_chives_crop[age=0]`、手上少 **1** 个；骨粉用到 `age=3`；破坏成熟作物掉 **1** 个 `farmstorm:plain_chives` + **0~3** 个种子；Java 文件本节末 **153**（终态 152 由 Phase 60 的 F-299 收）。

### 59.5 常见翻车点

- **复用 `SeedItem`**（RT-82）：种子**种不下去**，本版第 2 条需求整体落空；预防：E-59-6 的种植判据，且 `PLAIN_CHIVES_SEEDS` 的构造器必须是 `PlainSeedItem`。
- **漏 `instanceof PlainCropBlock` 分支**（RT-77）：落到 `simpleBlock`，世界里的韭菜变六面方块**且不报错**；预防：E-59-1 的四个模型与 `textures.crop`。
- **用 `grep chive_seeds` 验旧种子归零**：`plain_chives_seeds` 含这个子串，会得假红；预防：判据用 `\bchive_seeds\b` 词边界，或只数 `item.farmstorm.chive_seeds` 这个整键。

### 59.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；两个新类的路径与构造器；blockstate 与四个模型的落位。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、一次 datagen、四个目录/文件计数、三次 grep；读数是 `plain_chives_crop.json` 的四个 variant、一个模型文件的 `textures.crop`、种植与骨粉的两次游戏内读数。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如「不显紫黑格」只能进游戏看，写明替代读数：png 存在 + 模型 `layer0`）。

---

## Phase 60 · 培养器判据与产出（M56）

### 60.1 本阶段要做什么

把培养器的准入判据从「有 `CropProfile` 且非史诗」**整体反转**成「是 21 条转换配方的输入且输出档案不是史诗」，产出改成固定**两组**（投入物 × `cultivator.yield` + 对应种子 × `cultivator.yield`），补上第一条合成配方，并删掉已经没有引用的 `SeedItem.java`。

### 60.2 前置条件

- 56.4 的 E-56-4 已绿（21 条配方是判据的唯一来源）；58.4 的 E-58-1 已绿（7 个种子删完）；59.4 的 E-59-4 已绿（平凡韭菜种子在位）。
- 基线：`CultivatorBlockEntity.java:131-137`（`_comment`）、`:138-144`（`admissible`）、`:151-165`（`profileOf`）、`:216-227`（`deliver`）、`:229-239`（`batch`）、`:241-249`（`hasOwnSeed`）；`SeedItem.java` 实读 **54** 行。
- **死命令 4**：删 `SeedItem.java` 必须在「7 个种子删完」∧「`instanceof SeedItem` 消失」之后——两个条件在本节内部按 F-299 的顺序满足。
- 17 的算式：`21 − 4 = 17`，排除火把花 / 瓶子草荚 / 甜浆果 / 金苹果四条**史诗输入**；冻结面：`cultivator.cycleTicks` 与 `cultivator.yield` 两个键不拆、不增，`outpost_core.cultivatorCapacity` 仍 24。

### 60.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-299 | 培养器准入判据反转 + 删 `SeedItem.java` | M | `instanceof SeedItem` 与 `profileOf(` 各 **0**；`outputProfileOf(` 声明 **1** + 调用 **1**；Java **153 → 152** |
| F-300 | 培养器产出两组 + `hasOwnSeed` 改写 | M | 小麦 ⇒ **2 + 2**；胡萝卜 ⇒ **4**；毒马铃薯 ⇒ **4**；`batch(` 里 `new ItemStack(` 恰 **2** 处 |
| F-301 | 培养器合成配方 4 铁 + 4 玻璃 + 1 红石 | S | `cultivator.json` 首次存在；`iron_ingot` **4** / `glass` **4** / `redstone` **1** |

### 60.4 出口判据

1. **E-60-1 判据反转的源码面（F-299）**：怎么验：逐串 grep `CultivatorBlockEntity` 与全树。通过的样子：`instanceof SeedItem` **0**、`profileOf(` **0**、`outputProfileOf(` 声明 **1** + 调用 **1**；`SeedItem.java` 路径不存在；`src/main/java` 下 `SeedItem` 命中 **0**；Java **152**。
2. **E-60-2 收得进的抽验（F-299）**：怎么验：同一台培养器依次右键六种物品。通过的样子：小麦 / 胡萝卜 / 毒马铃薯 / 平凡韭菜**收得进**；火把花 / 金苹果**收不进**（`PASS`、槽保持空、手持物不消耗）。
3. **E-60-3 改良作物一个不收（F-299 / 负向）**：怎么验：拿 `farmstorm:puffed_wheat` 右键培养器。通过的样子：无反应（`PASS`）、槽空、手持物不消耗；这条与 E-60-2 必须成对记录。
4. **E-60-4 判据只有一个来源（F-299）**：怎么验：一次临时配方实测。通过样子：`grep -c 'minecraft:wheat' CultivatorBlockEntity.java` = **0**（不存在 17 条手写白名单）；临时加一条 `converting` 配方 ⇒ 投入物 **17 → 18**，删掉那条配方即恢复。
5. **E-60-5 产出两组（F-300）**：怎么验：同一台培养器依次喂四种。通过的样子：小麦 ⇒ 2 小麦 + 2 小麦种子；胡萝卜 ⇒ **4 胡萝卜**；毒马铃薯 ⇒ **4 毒马铃薯**（不是马铃薯）；平凡韭菜 ⇒ 2 平凡韭菜 + 2 平凡韭菜种子。
6. **E-60-6 一个键管两组（F-300）**：怎么验：`cultivator.yield = 3` 后重跑。通过的样子：小麦 **3 + 3**、胡萝卜 **6**；config 键数不变。
7. **E-60-7 配方与源码形态（F-301 / F-300）**：怎么验：数目录 + 读 json + 读 `batch`。通过的样子：`data/farmstorm/recipe/` = **25** 个文件；材料 **4 / 4 / 1**；`unlockedBy` 里 `#farmstorm:` 命中 **0**；`batch(` 里 `new ItemStack(` 恰 **2** 处、`hasOwnSeed` 命中 **0**。

### 60.5 常见翻车点

- **判据抄成「非史诗」或把改良作物放进来**：17 个投入物的语义整体错，且转换器/培养器共用一个 `admissible`；预防：E-60-2 与 E-60-3 成对。
- **手写 17 条白名单**：与配方表漂移（RT-49 同性质），改一条 tier 必漏一边；预防：E-60-4 的临时配方实测。
- **在 F-295 之前删 `SeedItem.java`**：`CultivatorBlockEntity:161` 引用不到类，编译不过；预防：死命令 4 与窗口台账里 Phase 58 的前置。

### 60.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；`admissible` 与 `outputProfileOf` 的前后方法体；`batch` 的两行产出。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、一次 datagen、六种物品的右键、（临时配方那一次）加与删的两轮构建；读数是六条收/不收的读数、四组产出数量、`cultivator.json` 的材料计数。
- **没做的与做不了的**：一条一行写「未验证」与原因，其中必须有一条写明「临时配方测试后已删除，工作树干净」。

---

## Phase 61 · 红线 `Kind`（M57）

### 61.1 本阶段要做什么

给连线加一个**类型字节**（`Kind`：`CULTIVATOR` / `CROP_CONVERTER`），让转换器的连线是红色、培养器保持青色，并把 `Kind` 落进核侧存档——**不按方块类型在渲染期反查**。这是本版唯一的协议变更：包数仍是 2，`PROTOCOL_VERSION` 从 `"2"` 升到 `"3"`。

### 61.2 前置条件

- 57.4 的 E-57-3 已绿（`CropConverterBlock` 与 `CropConverterBlockEntity` 已存在）。
- 基线：`CultivatorLinkPayload.java:36`（`MAX_LINKS = 1024`）、`:42-45`（单条 codec）、`:48-50`（列表 codec）、`:53`（`record Link(BlockPos core, BlockPos cultivator)`）；`CultivatorLinkBroadcaster.java:76-78`（`ORDER`）、`:217-219`（组装）、`:227-234`（`fingerprint`）；`CultivatorLinkRenderer.java:41-44`（颜色常量）、`:71-79`（循环）。
- **死命令 5**：`Link` 加字段必须与 `PROTOCOL_VERSION` `"2"` → `"3"` **同一提交**；**死命令 6**：三处广播必须与渲染换色**同一提交**；冻结面：青色 `0.30 / 0.85 / 0.90` 三个分量一个字节不许变，`MAX_LINKS` 仍 1024，包数仍 **2**，红线 3 明令不许 `getBlockState`。
- 手边：55 号 F-302 ~ F-304、§0.5 死命令 5 / 6；54 号 §5.1 ~ §5.5、§8 #5 / #24 / #32。

### 61.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-302 | `Link.Kind` + 单条 codec + `PROTOCOL_VERSION` `"3"` | M | `Link` 组件 **3** 个、`Kind` **2** 个成员；`PROTOCOL_VERSION = "3"` 命中 1、`"2"` 命中 0 |
| F-303 | 核侧 `CultivatorKinds` + 三写入点 + `rebindNearby` 扩两种机器 | L | `Cultivators` 与 `CultivatorKinds` 等长且元素只 0 / 1；先放机器后放核也能绑上 |
| F-304 | 广播三处 + 渲染按 `Kind` 选色 | M | `ORDER` **3** 段、`fingerprint` 里 `kind()` **1** 处；换机 **10 刻**内变色 |

### 61.4 出口判据

1. **E-61-1 协议结构（F-302）**：怎么验：读 payload 类 + 数注册调用。通过的样子：`Link` 的 record 组件 **3** 个、`Kind` 枚举成员 **2** 个、单条 codec 的 `composite(` 组件段 **3**；`MAX_LINKS = 1024` 仍 **1** 处且值未变；`playToClient(` 仍 **2** 处、两个包 id 未变。
2. **E-61-2 协议串（F-302）**：怎么验：读 `FarmstormNetwork` + 起服看两条自检。通过的样子：`PROTOCOL_VERSION = "3"` 命中 **1**、`"2"` 在该文件命中 **0**；启动日志显示版本 **3** 与 `MAX_LINKS 1024`。
3. **E-61-3 字节差 1（F-302）**：怎么验：取一条连线的 payload 做编解码往返。通过的样子：字节长度差 = **1**（`2 × BlockPos` → `2 × BlockPos + 1`）。
4. **E-61-4 落盘等长（F-303）**：怎么验：`/data get block <core> Cultivators` 与 `CultivatorKinds`。通过的样子：两条数组长度**相等**；`CultivatorKinds` 的元素只有 **0 / 1** 两种取值。
5. **E-61-5 老存档无损（F-303）**：怎么验：拿 0.2.2 存档（含一个绑了 3 台培养器的核）读档。通过的样子：绑定仍在、`kinds` 全 **0**、连线仍青色；日志里 `CultivatorKinds` 缺失的 WARN 命中 **0**（缺键是合法状态，不告警）。
6. **E-61-6 先放机器后放核（F-303）**：怎么验：先放转换器（无核）→ 3 × 3 区块内放核。通过的样子：补绑日志出现 `补绑：核 … 本轮新增 1 条（累计 1/24，周期 … 刻）`；转换器的 `corePos` 非空、该位 kind = **1**；培养器 11 台 + 转换器 13 台 ⇒ 第 **25** 台绑不上、已绑的 24 条不变。
7. **E-61-7 换机变色（F-304）**：怎么验：同屏两类机器 + 拆换一次。通过的样子：同屏青 / 红两色；拆掉培养器原位置放转换器 ⇒ 那条线 **10 刻内**从青变红，反向同样；`CULTIVATOR` 仍是 `0.30F / 0.85F / 0.90F`、`CROP_CONVERTER` 是 `0.92F / 0.22F / 0.22F`；渲染路径里 `level` / `getBlockState` 命中 **0**。

### 61.5 常见翻车点

- **只改 record 与 codec、没进 `ORDER` / `fingerprint`**（RT-75）：kind 变了不重发，颜色永不更新，而服务端看起来一切正常；预防：E-61-7 的换机条是唯一能抓它的判据。
- **协议串没升**（RT-76）：混装环境协商通过，随后按旧 codec 解新字节流 ⇒ 客户端必崩；预防：死命令 5 + E-61-2。
- **渲染期按方块类型反查**（红线 3）：未加载区块上随机闪回青色；预防：E-61-7 的后半句与 `Kind` 字节本身。

### 61.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；`Link` 前后两版 record；三个写入点与三处广播的行号；两处颜色常量。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、一次编解码往返、`/data get` 两次、拆换各一次；读数是两条自检行原文、`Cultivators` / `CultivatorKinds` 两条数组、补绑日志整行、换色前后的截图文件名。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如双端混装需要两台机器，写明替代读数与风险登记）。

---

## Phase 62 · config 与炮弹（M58）

### 62.1 本阶段要做什么

把 `[converter]` 整组 3 个 config 键换成 `[crop_converter] processTicks` 与 `pumpkin_turret.bulletLifetimeTicks` 两个新键（**175 键 / 60 push**），并给 man瓜炮弹加「出膛 **300** 刻后静默删除」的寿命。前者让 Phase 57 的 600 刻从代码常量变成可配置项，后者是本版唯一一处「投射物到期消失」。

### 62.2 前置条件

- 57.4 的 E-57-4 已绿（转换器能按 600 刻产出）；61.4 的 E-61-1 / E-61-2 已绿。
- 基线：`FarmstormConfig.java:19-21`（三个字段）、`:295-305`（`b.push("converter")` 整组）、`:444-461`（`[pumpkin_turret]` 组）；`b.push(` / `b.pop()` 各 **60**、行首 `.define*` **178** 行（含 1 行注释）。
- **验收前置**：先删（或改名备份）`run/config/farmstorm-server.toml`；盘上那份是陈旧的（`maxPerPlayer = 2` / `cultivatorCapacity = 16`），拿它判 175 必然假红。
- 冻结面：`MelonGrenadeProjectile` / `MelonSliceProjectile` 两个类 diff **0** 行（红线 6），`verify_project.py` 的 config 期望面一个字节不改；手边：55 号 F-305 / F-306，54 号 §6.1 ~ §6.4、§8 #7 / #8 / #25 / #26。

### 62.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-305 | config 删 3 加 2 | M | 新 toml `^\[converter\]` **0** / `^\[crop_converter\]` **1**；键 **175**、节 **36**、push **60**；`processTicks = 600`、`bulletLifetimeTicks = 300` |
| F-306 | `TurretBullet` 出膛 300 刻后删除 | S | 第 **300** 刻消失；`readAdditionalSaveData` 无「已飞刻数」；两个瓜类 diff 0 |

### 62.4 出口判据

1. **E-62-1 新 toml 五读数（F-305）**：怎么验：删掉旧 toml 后起服，数新文件。通过的样子：`^\[converter\]` 命中 **0**、`^\[crop_converter\]` 命中 **1**；键 **175**、顶层节 **36**、`b.push(` / `b.pop()` 各 **60**。
2. **E-62-2 两个新键（F-305）**：怎么验：读 toml 与 `FarmstormConfig`。通过的样子：`processTicks = 600`（值域 20 ~ 72000）、`bulletLifetimeTicks = 300`（值域 1 ~ 72000）；行首 `.define*` 从 178 变 **177**。
3. **E-62-3 老 toml 期望红（F-305）**：怎么验：把升级前的老 toml 放回去再起服。通过的样子：仍是 `maxPerPlayer = 2` / `cultivatorCapacity = 16` 且 `[converter]` 段仍在；**必须与 E-62-1 成对记录**，写明「老文件不回写默认值」。
4. **E-62-4 键真的被读（F-305）**：怎么验：`processTicks` 改成 **20** 后重跑。通过的样子：转换器第 **20** 刻产出（不再是 600）；这一条同时补上 Phase 57 的键面复核。
5. **E-62-5 炮弹寿命（F-306）**：怎么验：放 man瓜炮台对天空开火，数实体存活。通过的样子：第 **300** 刻 `turret_bullet` 消失、无粒子 / 音效 / 进度；`bulletLifetimeTicks = 40` ⇒ 第 **40** 刻消失。
6. **E-62-6 范围与残留（F-306 / 负向）**：怎么验：读插入点 + diff 两个瓜类 + grep 三个旧键。通过的样子：寿命判定在 `steerTowards(` **之前**（`grep -n` 行号更小）；两个瓜类的 `git diff` **0 行**；`converter.xpCost` / `netherStarForwardCost` / `netherStarOnReverse` 在 `src/main/java` 与 toml 里各命中 **0**。

### 62.5 常见翻车点

- **拿盘上老 toml 判 175**（RT-80）：假红（读到旧键）或假绿（键数凑巧对上却漏看节名）；预防：E-62-1 与 E-62-3 成对，先删再起服。
- **手改玩家的 toml**：验完就忘，下一版再来一次；预防：只删或备份，不改内容。
- **寿命写进 `steerTowards` 或扩到瓜类**（RT-81）：追踪弹寿命不准 / 瓜类战术被改；预防：E-62-6 的两条。

### 62.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；三个旧键的删除点与两个新键的插入点；寿命判定的那一行。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、两次起服（新 config / 老 toml 放回）、`^\[...\]` 两轮 grep、`b.push` / `b.pop` / `.define*` 三个计数；读数是新 toml 的两行原文与键 / 节计数、老 toml 的两行原文（标「期望红」）、炮弹消失刻数与两个瓜类的 diff 行数。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如炮弹计数要逐刻观察，写明取样频率与容差一 tick 的处理）。

---

## Phase 63 · 语言键与自检收口（M59）

### 63.1 本阶段要做什么

把语言键从 **340 × 2** 收到 **321 × 2**（**关语言面**），再把自检断言与检查器计数一次改成 **92 / 28 / 28 / 1**（**关计数面**）。两个关窗动作分两个任务、不许合并——这是全文件唯一允许写出 `92` 与 `321` 的地方，也是窗口的关闭点。

### 63.2 前置条件

- 55.4 的 E-55-7、58.4 的 E-58-5 / E-58-6、62.4 的 E-62-1 全部已绿（9 个界面键、4 个进度键、3 个 config 键所指向的物件都已下线）。
- 基线：`ModLangProvider.java:10` 的 340、`:13-16` 的构成段，两份 `lang/*.json` 各 **340** 键，`Farmstorm.java:180-183` 现仍是 98 / 34 / 27 / 2；`tools/verify_project.py:334-343` 的 `EXPECTED` 已随 F-287 / F-295 / F-298 更新，`:380-393` 的 `EXPECTED_COUNTS` 仍是 34 / 27 / 2。
- **窗口纪律 3**：语言面 F-307 必须在计数面 F-308 **之前**——语言面没有自动闸（`verify_project.py` 只比两份键集相等，不校验 321），人工计数必须在关窗前做完。
- 冻结面：`farmstorm.configuration.*` 从 179 变 **178**；`verify_project.py` 的 config 期望面一个字节不改。

### 63.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-307 | 语言键 340 → 321 × 2 + 两处类注释（**关语言面**） | M | `zh = en = 321`、双向差集 **0**；前缀六段 `28 / 28 / 32 / 178 / 13 / 42` |
| F-308 | 自检收口 92 / 28 / 28 / 1 + 工具计数（**关计数面**） | M | 启动日志 `92 … → OK`（原文）；脚本 `92 / 92`、`结论：FAIL 0 项` |

### 63.4 出口判据

1. **E-63-1 两份 lang 各 321（F-307）**：怎么验：`-Encoding UTF8` 按 `^\s*"[^"]+"\s*:` 数**键**（不数行）。通过的样子：`zh = 321`、`en = 321`；`zh-only = 0` / `en-only = 0`。
2. **E-63-2 前缀六段（F-307）**：怎么验：按前缀分组数。通过的样子：`item.` **28** / `block.` **28** / `advancements.` **32** / `farmstorm.configuration.` **178** / `entity.` **13** / 其余（`plain` 组）**42**；`28 + 28 + 32 + 178 + 13 + 42 = 321`。
3. **E-63-3 改名两键（F-307）**：怎么验：读两份键集。通过的样子：`item.farmstorm.crop_converter` 与 `block.farmstorm.crop_converter` 各 **1** 处；旧键 `item.farmstorm.converter` / `block.farmstorm.converter` 命中 **0**；总数**不是** 323。
4. **E-63-4 两处注释（F-307）**：怎么验：grep 三个数 + 进游戏看两处文案。通过的样子：`340` 命中 **0**、`321` 命中 **2**（`:10` 与 `:16` 各 1）；方块名显示「农作物转换器」，`fang_dance` 描述显示「触发一次「刺球」」。
5. **E-63-5 启动日志整行 92（F-308）**：怎么验：起服读自检行。通过的样子：`自检 注册对象 92 个：items 28 / blocks 28 / block_entities 8 / … / menu_types 1 → OK` 原文粘贴，末尾是 `OK`。
6. **E-63-6 工具 92 / 92（F-308）**：怎么验：`python -X utf8 tools/verify_project.py`。通过的样子：打印 `已核 registry 对象: 92 / 92`、`结论：FAIL 0 项`；第 1 节两行键数都是 **175**。
7. **E-63-7 两处断言同提交（F-308）**：怎么验：读 `Farmstorm.java` 与 `EXPECTED_COUNTS`。通过的样子：`== 98` 命中 **0**、`total == 92` 命中 **1**；`items == 28` / `blocks == 28` / `menuTypes == 1` 各 **1**；其余 8 个逐类 `==` 逐字未变；`EXPECTED_COUNTS` 12 项相加 = **92**；config 期望面五个函数体一字未改。

### 63.5 常见翻车点

- **把两个改名键当「删 1 加 1」数**（RT-79）：总数得 323，六段分组对不上；预防：E-63-3 与 E-63-2。
- **语言面没关就动计数面**：F-308 之后还要回头改语言表，等于把窗口重新打开；预防：窗口纪律 3，F-307 先于 F-308。
- **只改 `Farmstorm.java` 不改 `EXPECTED_COUNTS`**（RT-72）：日志绿而脚本红（或反之），两套闸相反；预防：E-63-5 与 E-63-6 成对，同一提交。

### 63.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；23 个删除键与 4 个新增键的清单；两处注释的前后对照。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、一次 datagen、两次键集读数、按前缀分组的计数命令、`python -X utf8 tools/verify_project.py` 的完整输出；读数是 `zh` / `en` 两个数与两个差、六段分组、`340` 归 0 与 `321` 命中 2 的行号、自检整行与 `92 / 92` 原文。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如中文显示要在中文语言环境下看，写明切换步骤）。

---

## Phase 64 · 验收与边界（M60）

### 64.1 本阶段要做什么

把 54 号 §8 的 **32** 条验收与 53 号 §9 的 **31** 条边界逐条跑掉，落成两份可复核记录。本节**不改码**：验收不通过就回对应任务重做，不许就地改判据，也不许把「没测」写成「通过」。

### 64.2 前置条件

- 63.4 全部已绿：窗口已关，`92 / 175 / 60 / 321 × 2 / 152` 各数都可以断言；F-286 ~ F-308 共 **23** 个任务已落地，`.work/v023-verify/` 可写，`run/logs/latest.log` 可读。
- **不改码**：`git status` 只允许 `.work/v023-verify/` 下的两份新文件。
- 四条必测组合不许拆：`#7` + `#8`（新 config 绿 / 老 toml 期望红）、`#13` + `#16`（能产出 / 核满不丢）、`#17` + `#19`（收原版 / 不收改良）、`#24` + `#23`（视觉变色 / blockstate 数据）。
- 手边：54 号 §8 的 32 条与表下注、§9 的 12 条风险；53 号 §9 的 31 条（`B-28` ~ `B-58`）。

### 64.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-309 | 54 号 §8 的 **32 条**验收全量执行 | L | `acceptance.md` 里 `#1` ~ `#32` 一条不漏；每条给动作、读数、结论、证据 |
| F-310 | 53 号 §9 的 **31 条**边界复跑 | L | `boundaries.md` 里 `B-28` ~ `B-58` 31 条一条不漏；四条负向判据写「没有发生的证据」 |

### 64.4 出口判据

1. **E-64-1 32 条一条不漏（F-309）**：怎么验：读 `.work/v023-verify/acceptance.md`。通过的样子：`#1` ~ `#32` 共 **32** 行结论，每行有动作、读数、结论、证据；`#1` 的日志整行与 `#2` 的 `92 / 92` 是**原文粘贴**。
2. **E-64-2 四条组合成对（F-309）**：怎么验：按记录里的相邻条目核对。通过的样子：四对**相邻且都通过**；`#8` 标注「期望中的红」并给出对应的绿条 `#7`。
3. **E-64-3 资源四数一次给全（F-309）**：怎么验：读 `#22`。通过的样子：读数写成 `84 / 5 / 22 / 0` 四个数一起给，缺一个即不算通过。
4. **E-64-4 31 条一条不漏（F-310）**：怎么验：读 `.work/v023-verify/boundaries.md`。通过的样子：`B-28` ~ `B-58` 共 **31** 行，按 R1 / R3 / 平凡作物 / R2 / R4 / R5 / 计数面分组标注。
5. **E-64-5 四条负向（F-310）**：怎么验：读 B-32 / B-37 / B-41 / B-52 四条。通过的样子：四条都写「**没有发生的证据**」（谁去试、试了什么、看到了什么），不是「没测」也不是「应该不会」。
6. **E-64-6 计数面十个数与不改码（F-310 / 负向）**：怎么验：读 B-58 + `git status`。通过的样子：B-58 一次给全 `92 / 28 / 28 / 1 / 175 / 60 / 321 × 2 / 21 / 17 / 152`，任何一个用旧值（98 / 34 / 27 / 2 / 176 / 340 / 26 / 154）报交付即判失败；`git status` 只有两份记录，`src/` 与 `tools/` 的 diff 为空。

### 64.5 常见翻车点

- **只跑 `#7` 不跑 `#8`**：把「默认值只对新装生效」读成回归；预防：E-64-2 的成对纪律。
- **验收不过就地改判据或改码**：三轮验收作废；预防：E-64-6 的后半句，`git status` 只允许两份记录。
- **把两份记录合并成一份**：负向判据被正向读数淹没，复核时无法判断某条是缺失还是被合并；预防：55 号 A-101 的分份纪律。

### 64.6 完成后必须回报什么

- **改了哪些文件**：`git status` 原文；本节只新增两份记录，`src/`、`tools/` 与 `docs/` 的 diff 必须为空。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、`python -X utf8 tools/verify_project.py` 的三段原文、`git diff --stat docs/`、两次起服（新 config / 老 toml）；读数是 32 条逐条结论、31 条逐条结论与四条负向证据、两处界面截图的文件名。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如双端联机那一条需要第二台机器，写明替代读数与风险登记方式）。

---

## Phase 65 · 签字与 README（M61）

### 65.1 本阶段要做什么

把红线、注册面、数值面、**19 项必改**与三组已知影响签成一份可复核的记录，并把玩家可见的三段话落到 `README.md`。这是唯一一次「文档与代码一起交付」的收尾动作：签完这一份，v0.2.3 的交付面就只剩版本号与 jar。

### 65.2 前置条件

- 64.4 全部已绿（`acceptance.md` 与 `boundaries.md` 两份记录在位）；`BRIEF` §5 的 **19** 项必改清单与 52 号 §8 的四条遗留登记在手。
- 不改清单：`TierTrade`、农民三行、`#farmstorm:converter_fuel` 标签、`OutpostCoreBlockEntity` 的补绑算法、`verify_project.py` 的 config 期望面、`MelonGrenadeProjectile` / `MelonSliceProjectile` 两个类。
- 冻结面：`docs/zh/00` ~ `54`、`docs/en/*`、`.work/v023-BRIEF.md`、`plan0.2.3.txt` 的 mtime 不许变（本版只新增 `docs/zh/52` ~ `57` 六份与本记录）。
- 手边：55 号 F-311 与 §3 表 A 表下注第 1 条；52 号 §8 第 2 条与第 4 条。

### 65.3 任务清单

| F 号 | 名称 | 复杂度 | 验收断言 |
|---|---|---|---|
| F-311 | 红线、注册面、19 项必改与 README 签字 | M | `signoff.md` 八项读数齐；`README.md` 三段齐全；19 项必改 **19 / 19** 有落点 |

### 65.4 出口判据

1. **E-65-1 八项读数（F-311）**：怎么验：读 `signoff.md` 的八行。通过的样子：Mixin **0**（`src/` 口径）、`*.mixins.json` **0**、`playToClient(` **2**、`PROTOCOL_VERSION` = `"3"`、注册面 **92**、口径外 **3 类**、config **175 / 60**、语言键 **321 × 2**、Java **152**——逐项给读数与出处，不写「通过」两个字。
2. **E-65-2 19 项必改（F-311）**：怎么验：逐条比对 `BRIEF` §5 与签字表。通过的样子：19 行逐条给落点任务号，计数 **19 / 19**，无一项悬空。
3. **E-65-3 README 三段（F-311）**：怎么验：读新增三段并 grep。通过的样子：升级须知段里 `run/config/farmstorm-server.toml` + `删` + `重生成` 三串**同段共现**；已知影响段里 `下界之星` 与 `30 秒` 同段；平衡变更段里 `翻倍` 与 `4 个` 同段。
4. **E-65-4 不改清单（F-311 / 负向）**：怎么验：六个对象逐个 `git diff`。通过的样子：各 **0 行**；`data/farmstorm/tags/item/converter_fuel.json` 仍存在（Phase 58 没删它）。
5. **E-65-5 上游与发布面（F-311）**：怎么验：读 mtime + 数文档 + 看 jar。通过的样子：`docs/zh/00` ~ `54`、`docs/en/*`、`.work/`、`plan0.2.3.txt` 的 mtime 未变；`docs/zh/` 恰新增 `52` ~ `57` 六份且本文件在位；jar 名仍是 `farmstorm-0.2.3+mc1.21.1.jar`。
6. **E-65-6 口径外 3 类显式登记（F-311）**：怎么验：读签字表的口径外一节。通过的样子：`ATTACHMENT_TYPES` 1 / `VillagerProfession` 1 / `PoiType` 1 三项在位，并写明「不计入 92、`verify_project.py` 既不核存在也不核计数」，由人工兜住。

### 65.5 常见翻车点

- **签字只写「通过」不给读数**：无法复核，等于没签；预防：E-65-1 的逐项出处与 E-65-2 的逐条任务号。
- **回头改上游文档把差异抹平**：把「登记」变成「篡改」；预防：E-65-5 的 mtime 判据。
- **README 写成「史诗作物变便宜了」而不点名「30 秒」**：玩家读不出实际影响，升级后照旧来报 bug；预防：E-65-3 的同段共现。

### 65.6 完成后必须回报什么

- **改了哪些文件**：`git diff --stat` 原文；`signoff.md` 的六节标题；`README.md` 三段全文。
- **跑了什么命令、看到什么**：`gw.cmd build` 退出码、`python -X utf8 tools/verify_project.py` 的结论行、八项读数各自的命令或文件位置、`git diff` 六次；读数是八项读数原文、19 / 19 的落点表、README 三段与 grep 的五个串。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如口径外 3 类没有工具闸，写明人工登记的证据来自哪两个文件）。

---

## 9. 跨 Phase 台账

**读法**：下表的每一行是**一个动作横跨两个以上 Phase**。它回答三件事：起点在哪、终点在哪、谁先谁后；最后一列写违反后的当场后果——这些后果都不是「可能出问题」，而是能当场看到的一种坏状态。

| # | 横跨的动作 | 起点（Phase / 任务） | 终点（Phase / 任务） | 谁先谁后 | 违反会怎样 |
|---|---|---|---|---|---|
| 1 | 注册改名的 id 面 → 计数面收口 | 55 / F-287（开窗） | 63 / F-308（关窗） | 先改名、后收口，中间 20 个任务按窗口纪律走 | 在 F-287 之前写 92 ⇒ 启动即 FAIL；在 F-308 之前改 `Farmstorm.java` ⇒ 三个面互相追改 |
| 2 | 语言键的删项物件 → 语言面收口 | 55（F-289 删界面三件）、58（F-295 / F-296 删两条进度）、62（F-305 删三个 config 键） | 63 / F-307 | 先下线物件、后收键 | 留下孤儿键，321 的六段分组算不平（RT-79） |
| 3 | 配方结构 → 配方条数 → 培养器判据 | 56 / F-290 → F-291 | 60 / F-299 | 先结构、后条数、最后判据 | 培养器读到 26 条或 6 条种子配方，17 个投入物算成 20 或 14 |
| 4 | 7 个种子 ↔ 6 条种子转换配方 | 56 / F-291（先删配方） | 58 / F-295（再删物品） | 配方先、物品后 | `ModRecipeProvider:86-92` 引用已删物品，编译不过 |
| 5 | `SeedItem.java` 的删除 | 58 / F-295（种子删完） | 60 / F-299（`instanceof` 消失后删类） | 先删种子、再反转判据、最后删类 | `CultivatorBlockEntity:161` 与 `FarmstormItems:175-177` 引用不到类，编译不过 |
| 6 | item 贴图面 | 57 / F-294（−1 到 28）→ 58 / F-295（−6 到 22、改名 1 张） | 59 / F-298（模型分派） | 先删旧图、再改名、最后挂模型 | 任一步提前，`ModItemModelProvider:55-57` 的兜底找不到 png ⇒ 紫黑格 |
| 7 | Java 文件数的四步账 | 55 / F-289（154 → 151）→ 59 / F-297（151 → 152）→ 59 / F-298（152 → 153） | 60 / F-299（153 → 152） | 每节只写本节末的真实值 | 提前写终态 152，与当时真实值差 1，`signoff.md` 的 Java 152 无法复核 |
| 8 | `Kind` 的协议 → 落盘 → 广播渲染 | 61 / F-302 → F-303 | 61 / F-304（同一 Phase，三个任务串行） | 字段先、落盘次、广播与渲染同提交 | 只改 record 与 codec 不进指纹 ⇒ 换机不重发、颜色永不更新（RT-75） |
| 9 | `rebindNearby` 扩两种机器 | 57 / F-292（`setPlacedBy`） | 61 / F-303（收集两种机器） | 机器先有、补绑后扩 | 「先放机器后放核」这条手顺下转换器永不绑定，且不报错（A-100） |
| 10 | 进度面三条 | 57 / F-293（`first_conversion` 授予点搬进 `deliver()`）、58（删两条） | 58 / F-296（收到 16 条）→ 64 / F-309 的 `#27` | 先搬授予点、再删、最后数 | `first_conversion` 无授予点 ⇒ 永远拿不到；16 条的读数假红 |
| 11 | `crop_converter.processTicks` 与 600 刻验收 | 57 / F-293（按代码常量 600 验） | 62 / F-305（键才加入）→ 62 的 E-62-4（改 20 复核） | 先常量、后键、再复核 | 在 Phase 57 直接读该键会读不到，验收写成「键不存在」的假失败 |
| 12 | 三个观察面各有关窗任务 | id 面 55 / F-287、语言面 63 / F-307 | 计数面 63 / F-308 | 三个关窗任务不许合并 | 合并任两个，第三个面就在没有安全网的状态下被改 |

**三条读法**：

1. **第 1、2、12 行是三根主梁**：它们决定「什么时候可以写总数」。任何一节里出现 92 / 28 / 1 / 321，都必须在 Phase 63 之后才是安全的。
2. **第 4、5 行是「编译不过」型**：它们不产生假的绿，只产生红，因此最好排查；真正难的是第 8、9、10 行——三条都**不报错**，只在游戏里表现为「颜色不对」「绑不上」「进度拿不到」。
3. **第 11 行是 55 号的一处顺序缝隙**：`F-293` 的验收写在 Phase 57，而它引用的 config 键在 Phase 62 才出现。本文把 Phase 57 的判据改成读代码常量，把「键真的被读」留给 Phase 62 的 E-62-4；不改 55 号的 F 号与依赖列，差异登记在 §10 第 9 条。

---

## 10. 本文自检

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 编号连续 | 任务 **26** 个（`F-286 ~ F-311`，`311 − 286 + 1 = 26`）；Phase **12** 个（54 ~ 65）；里程碑 **12** 个（M50 ~ M61）；无跳号、无重号 | 通过 |
| 2 | 每节六小节、顺序固定 | 十二节都是「要做什么 / 前置条件 / 任务清单 / 出口判据 / 常见翻车点 / 必须回报」；**6 × 12 = 72** 个三级小标题，顺序无一处调换 | 通过 |
| 3 | 任务号 / 名称 / 复杂度与 55 号一致 | 26 行逐行比对 55 号 §1 表与各任务标题行：编号 **26 / 26**、名称 **26 / 26**、S / M / L **26 / 26**、Phase 归属 **26 / 26**；未增删任何 Phase、未改任何 F 号分配 | 通过 |
| 4 | 人日自洽 | 十二节小计 `0.5 + 6.0 + 3.0 + 7.5 + 2.0 + 3.0 + 3.5 + 6.0 + 2.0 + 3.0 + 6.0 + 1.5 = 44.0`；复杂度 `S 4 / M 16 / L 6`，`4 × 0.5 + 16 × 1.5 + 6 × 3.0 = 44.0`；`44.0 × 1.25 = 55.0` | 通过 |
| 5 | 出口判据总数与分布 | 合计 **76** 条：`5 + 7 + 6 + 7 + 6 + 6 + 7 + 7 + 6 + 7 + 6 + 6`；每节 ≥ **5** 条（12 × 5 = 60 的下限满足）；每条以 `N. **E-<phase>-<n>` 开头，编号全局唯一 | 通过 |
| 6 | 窗口纪律写对 | 注册面总数 **92** 的断言只出现在 Phase 63 的 E-63-5 ~ E-63-7 与关窗后的 E-64-6 / E-65-1 / E-65-6；Phase 54 的 E-54-4 只把它列为「命中 0 次」的禁写串；`98` 的断言只在 Phase 55 的 E-55-1 / E-55-2；语言键总数 **321** 的断言只在 Phase 63 与关窗后的 E-65-1；`items` 28 不早于 E-63-7 断言（E-57-7 的 28 是 item png 张数，E-57-1 ~ E-57-3 里的 292 是任务号、E-61-7 的 `0.92F` 是颜色，三者都不算计数） | 通过 |
| 7 | 判据可观察 | 76 条逐条读过：全部落在**五类读数**上（文件 / 键 / 行数计数、日志整行、`/data get`、源码 grep 命中、像素与截图比对）；无「正常」「手感」「符合预期」这类不可证的表述 | 通过 |
| 8 | 数字不发明 | `92 / 28 / 28 / 1 / 8 / 13 / 4 / 3 / 1 / 175 / 60 / 36 / 321 / 152 / 21 / 17 / 16 / 18 / 84 / 5 / 22 / 0 / 29 / 6 / 3 / 2 / 32 / 31 / 26 / 76 / 44.0 / 55.0` 全部可追到 52 号 §1 / §3、54 号 §1 / §8、55 号 §1 / §3 / §5.5，或它们的算术（`98 − 7 + 1 + 1 − 1`、`176 − 3 + 2`、`340 − 23 + 4`、`154 − 3 − 1 + 2`、`26 − 6 + 1`、`21 − 4`、`18 − 2`、`29 − 1 − 6`、`6 − 6 + 1`、`4 × 0.5 + 16 × 1.5 + 6 × 3.0`）；新增的只有本文件的节号与判据编号 | 通过 |
| 9 | 与 55 号的差异只有两处、且已登记 | ① 55 号 §1 把 `F-295` 的依赖写成「—」，而它的验收 1 要求 `ModRecipeProvider` 的种子串归零，那 6 条配方由 `F-291`（Phase 56）删除 ⇒ 本文在 §0.2 把 Phase 56 写成 Phase 58 的**硬前置**，不改 F 号与依赖列；② 55 号 `F-293` 的验收 3 / 4 引用 `crop_converter.processTicks`，该键由 `F-305`（Phase 62）才加入 ⇒ 本文把 Phase 57 的条款改成读代码常量 600，把「键真的被读」落到 E-62-4；两条都写在 §9 第 3 / 11 行与第 11 行读法里 | 通过 |
| 10 | 禁用词与语气 | 全文无未定事项标记、无占位符、无第二人称敬语；人称只用「作者」「实现者」；结论句一律祈使 | 通过 |
| 11 | 行号与事实复核 | 本文引用的行号按 UTF-8 实读复核：`gradle.properties:11`、`一键构建.cmd:5,11,12`、`检查环境.cmd:5,14`、`Farmstorm.java:180-183,184-188`、`FarmstormBlocks.java:71-72,102-104`、`FarmstormItems.java:35,91,102-109,112-113,140-151,175-177`、`FarmstormBlockEntities.java:43-45`、`FarmstormMenus.java:12,17-18`、`FarmstormClient.java:62,73-76`、`ConverterRecipe.java:19-25,27-28,106-110`、`ModRecipeProvider.java:67-99,86-92,187-196,199-209`、`ModLootTableProvider.java:71,81,84,86,89,91,105`、`ModAdvancementProvider.java:82-85,95-106`、`ModBlockStateProvider.java:160-166,181`、`ModItemModelProvider.java:55-57,69`、`ModItemTagsProvider.java:42-48`、`FarmstormTags.java:35`、`ModLangProvider.java:10,13-16`、`CultivatorBlock.java:44,86-102,113-128,140-147,150-161`、`CultivatorBlockEntity.java:131-137,138-144,151-165,216-227,229-239,241-249`、`OutpostCoreBlockEntity.java:335-371,537-572`、`CultivatorLinkPayload.java:36,42-45,48-50,53`、`CultivatorLinkBroadcaster.java:76-78,217-219,227-234`、`CultivatorLinkRenderer.java:41-44,71-79`、`TurretBullet.java:76-90`、`FarmstormConfig.java:19-21,295-305,444-461`、`verify_project.py:334-343,380-393`；四个实测数：Java **154**、方块 png **84**、mcmeta **2**、item png **29** | 通过 |
| 12 | 只新建本文件 | `docs/zh/52` ~ `55`、`docs/zh/00` ~ `51`、`docs/en/*`、`src/`、`tools/`、`.work/` 一字未改；本文件是本次唯一新增 | 通过 |

---

> **本文的六个可复核判据**（交稿时逐条给读数）：Phase **12** 个（54 ~ 65）/ 任务 **26** 个（F-286 ~ F-311）/ 出口判据 **76** 条（`5 + 7 + 6 + 7 + 6 + 6 + 7 + 7 + 6 + 7 + 6 + 6`）/ 裸 **44.0** 人日（缓冲 **55.0**）/ 中间态窗口 **F-287 → F-308** / 跨 Phase 台账 **12** 行。
