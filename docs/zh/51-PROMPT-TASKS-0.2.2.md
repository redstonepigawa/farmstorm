# 51 号 v0.2.2 分阶段任务清单（Phase Prompts）

> 投喂方式：**每个 Phase 开工时只投该 Phase 的一节**；同一时间只投一节。每节从前到后独立可执行——把一节单独交给实现代理，不需要它读过上一节，也不需要它读过本文件的其它节。

| 项 | 值 |
|---|---|
| 文档编号 | 51-PROMPT-TASKS |
| 版本 | v0.2.2（MC 1.21.1 / NeoForge 21.1.213 / Java 21） |
| 读者 | 投喂编码代理的人 + 被投喂的 CLI 编码代理 |
| 上游 | `49-TASKS-0.2.2.md`（**F-267 ~ F-285 的唯一来源**：编号、标题、复杂度、人日、依赖、Phase 归属）、`48-TECH-0.2.2.md`（§1 注册面 98 与口径外 3 类、§2 七个构件、§3 的 **33** 行动作面、§4 config 176 / 60、§5 语言键 340、§7 风险 RT-62 ~ RT-70、§8 的 **30** 条验收、§9 的 **10** 条待验证、§10 的 13 条差异）、`47-DESIGN-0.2.2.md`（5 条需求 R1 ~ R5、**27** 条边界 B-01 ~ B-27）、`49 号 §5` 的裁决 A-82 ~ A-91 |
| 同级 | 50 号（常驻主提示词，只投一次，与本文件同批产出）。本文件**不重复**它的硬约束与红线口径；每节只重述 §0.2 的六条，且每节都能脱离 50 号单独执行 |
| 内容 | **8 个 Phase / 19 个任务（F-267 ~ F-285）**；每节固定六小节：前置 / 本阶段目标 / 任务清单 / 出口判据 / 翻车点 / 汇报格式 |
| 出口判据 | 8 节合计 **52** 条（每节 5 ~ 7 条），全部以 `- [ ]` 开头；前置用普通列表，因此 `grep -c '^- \[ \]'` 数出来的就是判据条数。判据只做「本节任务自证」，不新增验收维度；全量口径在 Phase 53：48 号 §8 的 **30** 条 + 47 号 §8 的 **27** 条 = **57** 条（49 号 F-283 / F-284 逐条承接） |
| 边界 | 只读上游：不改 `docs/zh/46` ~ `docs/zh/49`、不改 `docs/zh/00` ~ `45`、不改 `docs/en/`、不改 `src/`、不改 `tools/`、不改 `.work/`；只新建本文件 |

---

## 0. 投喂顺序与约束

### 0.1 八节投喂顺序（Phase 46 → 53）与每节的前置

| 顺序 | 投哪一节 | Phase · 里程碑 | 任务 | 人日 | 本节前置（缺一条都不许开） |
|---|---|---|---|---|---|
| 1 | §1 | 46 · **M42** | F-267 | 0.5 | 工作树干净（`git status` 只出现本节要改的 **3** 个文件）；`.tools\gw.cmd build` 退出码 0；基线是 **0.2.1 已发布态**（`gradle.properties:11` 的 `mod_version=0.2.1`）。**本节是第一个 Phase，没有上一节** |
| 2 | §2 | 47 · **M43** | F-268 ~ F-269 | 2.0 | §1.4 的**第 1 / 2 / 3 条**已绿（新 jar 产出、`0.2.2` 命中 6 处、`git diff gradle.properties` 只 1 行）；`src/main/java` 的 Java 文件数 **149**、`textures/item/` **29** 张 PNG；窗口在本节 F-268 打开 |
| 3 | §3 | 48 · **M44** | F-270 ~ F-271 | 1.0 | §2.4 的**第 1 / 4 / 6 条**已绿（启动日志 `98 / 34 … → OK`、`src/` 下 `97` 命中 0、创造栏 34 项）；`client/OutpostCoreScreen.java` 仍是 **42** 行、`render` 覆写 **0** 处 |
| 4 | §4 | 49 · **M45** | F-272 ~ F-274 | 3.5 | §3.4 的**第 4 / 5 / 6 条**已绿（`blit(` 2 处、像素差 0 px、槽位四个 y 未动）；`util/FarmstormTags.java` 仍是 **6** 个 `TagKey`（3 方块 + 3 物品）；`OutpostCoreBlockEntity` 的两处判据在 `:393` / `:783` |
| 5 | §5 | 50 · **M46** | F-275 ~ F-277 | 4.5 | §4.4 的**第 1 / 2 / 5 / 7 条**已绿（标签 7 个 key、产物只有培养器一个成员、新 config 是 4 / 24、注释八处已改）；两张新注册表在 `src/` 下仍是 **0** 命中 |
| 6 | §6 | 51 · **M47** | F-278 ~ F-280 | 2.5 | §5.4 的**第 1 / 2 / 3 / 5 条**已绿（POI 与职业各 1 个、`.register(modBus)` 14 行且 POI 在前、Java 文件数 152）；标签产物 `acquirable_job_site.json` 已在 `data/minecraft/` 下 |
| 7 | §7 | 52 · **M48** | F-281 ~ F-282 | 2.0 | §6.4 的**第 1 / 4 / 5 条**已绿（`NORMAL` 22 条、分支在农民之前、重启服务端后界面只 2 项）；`ModLangProvider` 的 `:10` / `:14-19` 一个字都没动过（窗口纪律第 2 条） |
| 8 | §8 | 53 · **M49** | F-283 ~ F-285 | 7.5 | §7.4 的**全部 7 条**已绿（三处关窗完成，`zh = 340` / `en = 340`）；`.work/v022-verify/` 可写；F-267 ~ F-282 共 **17** 个任务已落地 |
| 9 | 收尾 | 不在 Phase 内 | — | — | §8.4 全过：**57** 条（30 + 27）与 **19** 个任务全部有结论 |

读表三条：

1. **顺序是投喂顺序，不是唯一可执行顺序。** 49 号 §3.1 的链 2（`F-268 → F-275 → F-277`）与链 5（`F-272 → F-273 → F-274`）互不相交，可以分给两个人并行；但**一次只投一节**。并行时 §4 的前置只按它自己那条链算，不必等 §3 的像素比对。
2. **§5 不能提到 §4 之前**：F-275 / F-277 各要往 `Farmstorm.java` 的构造器里插一行，而那一段的顺序纪律（POI 在前、职业在后）要靠 §4 已经把 `FarmstormTags` 的 7 个 key 定下来才不会与 §5.4 的标签计数打架。倒过来投就是「拿一份标签还没定的仓库去加两张注册表」。
3. **§7 不能提到 §6 之前**：F-281 要改 `ModLangProvider` 的两处总数注释，而 `:14-19` 那句「33 item + …」里的 `33` 由 F-268 改成 `34`；F-281 的两条语言键里有一条是职业名，职业由 F-277 注册。倒过来投就是「给一个还不存在的职业写名字」。

### 0.2 每节开头必须原样贴的六条（不依赖上下文）

```text
① 红线两条：Mixin 0（src/ 下 @Mixin 与 *.mixins.json 均 0 命中）；自定义网络包累计 2 个，协议串仍 "2"，
   本版不新增。两条都不许为了任何一条交易 / POI 需求破例。
② 中间态窗口 = F-268（开）→ F-281（关）。窗口内只写「本条键存在 / 不存在」+「本条自己的计数」
   （贴图**仍 29 张** / 标签 7 个 key / 交易 22 条 / 池 4 条）；语言键总数 340 只在 F-281 断言一次。
③ Farmstorm.java:176-179 的断言（total == 97 → 98、items == 33 → 34）必须与 plain_chives 注册
   同一个提交；任何中间状态的启动日志必打 FAIL（格式串在 :180-184）。
④ POI 必须进 minecraft:acquirable_job_site 标签（F-276）；不加的后果是村民永不认领，而且不报错。
⑤ 「上限 4 / 绑定 24」只认新生成的 run/config/farmstorm-server.toml；盘上老 toml :753 = 2、
   :765 = 16 的红是期望中的红，必须与新 config 的绿成对跑。
⑥ 交易相关验收必须重启服务端（/reload 不重发 VillagerTradesEvent）；读数只认 latest.log /
   /data get / lang/*.json / 源码 grep 计数，不写「正常」「手感」。
```

### 0.3 速查表（一屏：Phase ↔ 里程碑 ↔ 任务号 ↔ 人日 ↔ 出口判据一句话）

| Phase | 里程碑 | 任务号 | 个数 | 人日 | 出口判据（一句话） | 本节条数 |
|---|---|---|---|---|---|---|
| 46 发布面 | M42 | F-267 | 1 | 0.5 | 产物 `farmstorm-0.2.2+mc1.21.1.jar`；`0.2.1` 命中 **0**、`0.2.2` 命中 **6** | 5 |
| 47 R2 平凡韭菜 | M43 | F-268 ~ F-269 | 2 | 2.0 | 启动日志 `98 / 34 … → OK`；脚本 `98 / 98`；贴图 **30** 张 | 7 |
| 48 R4 界面两处缺行 | M44 | F-270 ~ F-271 | 2 | 1.0 | 悬停第一行是物品名；`blit(` **2** 处（`v = 0` / `126`）；像素差 **0 px** | 6 |
| 49 R3 标签与名额 | M45 | F-272 ~ F-274 | 3 | 3.5 | 标签 **7** 个 key、成员 **1** 个；新 config **4 / 24**；老 toml **2 / 16** | 7 |
| 50 R1 两张注册表 | M46 | F-275 ~ F-277 | 3 | 4.5 | 两张表各 **1** 个对象；`.register(modBus)` **14** 行（POI 在前）；`acquirable_job_site` 含 `farmstorm:botanist` | 6 |
| 51 R1 两档交易 | M47 | F-278 ~ F-280 | 3 | 2.5 | `NORMAL` **22** 条、界面 **2** 项、5 级 **1** 项史诗补货两次不变 | 7 |
| 52 文案与收口 | M48 | F-281 ~ F-282 | 2 | 2.0 | `zh = 340` / `en = 340` / 双向差集 **0**；README 两段文案在位 | 7 |
| 53 验收与签字 | M49 | F-283 ~ F-285 | 3 | 7.5 | 30 + 27 = **57** 条一条不漏；签字含 **8** 项读数 | 7 |
| **合计** | **M42 ~ M49** | **F-267 ~ F-285** | **19** | **23.5** | 裸人日 23.5（`8 × 0.5 + 9 × 1.5 + 2 × 3.0`），含 25 % 缓冲 **29.375**；关键路径 **14.5** 人日 / 9 个任务 | **52** |

### 0.4 中间态窗口的读数台账（本版窗口与 0.2.1 不同：**一个窗口、三处观察面**）

| 段 | 任务边界 | `items` 注册数 | 语言键 | 段内能断言什么 |
|---|---|---|---|---|
| 窗口前 | F-267（Phase 46） | **33** | **338** | 可断言 33 / 338（此间无增删），但**不许**断言 34 / 340；本节判据里两个数一个都不写 |
| 窗口内 A | F-268 ~ F-269（Phase 47） | 实际 **34** | 实际 **338** | 只写「`plain_chives` 这条在注册表里存在」+「`items` 期望 **34**」+「贴图**仍 29 张**」；**不许**写 340 |
| 窗口内 B | F-270 ~ F-280（Phase 48 ~ 51） | 实际 **34** | 实际 **338** | 只写本条对象的读数（tooltip 出现 / 两段 blit / 标签 7 个 key / 两张新表各 1 个 / 22 项 / 4 池）；**总数一律不写** |
| 关窗 | F-281（Phase 52） | **34** | **340** | 一次写 340 × 2 + 前缀分组 + 两处注释同步；这是全文第一次出现 340 |
| 关窗后 | F-282 ~ F-285（Phase 52 ~ 53） | 34 | 340 | 98 / 3 类 / 176 / 60 / 7 / 340 六个数都可断言（F-285 签字引用 F-281 的读数） |

三条读法与 0.2.1 的差别：

1. **本版窗口横跨五个 Phase（47 ~ 51）**，比 0.2.1 的「§4 开、§5 关」长一倍——因为 `items` 与语言键的观察面（注册表 / json 键集）之间夹着两张新注册表与三个新类。⇒ **五个 Phase 的出口判据里 340 出现 0 次**，这是本文件最容易写错的一处。
2. **注册面只在 F-268 断言一次**：`98` 这个数在 §2.4 第 1 条出现，此后 §3 ~ §6 一律只写「日志仍 `98 … → OK`」而不重算逐类。
3. **`ModLangProvider` 的类注释一律不动**，直到 F-281 一次改齐（`:10` 的 299、`:14-19` 的 338、`33 item` → `34 item` + `1 villager`）。中途改注释意味着每个任务都要重算一遍总数，且必然有一个算错。

### 0.5 三个数的台账（每个数只在一个任务里翻页）

| 数 | 现值 | 改后 | 唯一翻页任务 | 同提交的连带 |
|---|---|---|---|---|
| 注册对象 | **97** | **98** | F-268 | `items` **33 → 34** + `Farmstorm.java:176-179` + 8 处「97」注释 + `verify_project.py` 两处 34 |
| 绑定名额 | **16** | **24** | F-274 | 两个默认值 + **八处**注释（48 号 §2.7.2 的七处 + `:333`，后者按 49 号 A-85 升为必改） |
| 语言键 | **338** | **340** | F-281 | `:10` 的 299 与 `:14-19` 的 338 两处注释 + 两份 `lang/*.json` |

三个数互不推导：`98 = 97 + 1`（注册对象）、`24`（名额）、`340 = 338 + 2`（语言键）。把它们写成同一个算式是本版最容易犯的错。

---

## 1. Phase 46 · 发布面（M42）

### 1.1 前置

- 工作树干净：`git status` 只出现本节要改的 **3** 个文件（`gradle.properties`、`一键构建.cmd`、`检查环境.cmd`）。
- `.tools\gw.cmd build` 当前退出码 **0**；构建入口只认它，不直接调 gradle。
- 基线是 **0.2.1 已发布态**：`gradle.properties:11` 是 `mod_version=0.2.1`；`build/libs` 此刻有 `farmstorm-0.2.1+mc1.21.1.jar`。
- 版本串按**行**是 **6** 处（48 号 §1.1）：`gradle.properties:11` 1 处 + `一键构建.cmd:5` / `:11` / `:12` 3 处 + `检查环境.cmd:5` / `:14` 2 处。不是「4 处」，也不是「5 处」。
- 手边三份：49 号 §1 表的 F-267 行与 §2 的 F-267；48 号 §1.1、§3 #1 ~ #5；`.work/v022-BRIEF.md` §3.4 第 1、2 行。
- 本节在**中间态窗口之前**（F-268 才开窗）：`src/` 与 `run/` 的 diff 必须为空；本节**不引用**注册面与语言键的任何总数（连 33 / 338 都不写，免得开窗后要回头改）。
- 上一节：**本节是第一节**，没有上一节；它的出口判据就是 §2 的前置。

### 1.2 本阶段目标

把版本号与两支构建脚本的产物面钉死——它是全部 jar 名与「检查环境」判据的唯一来源。

本节结束时：`build/libs` 里只有 0.2.2 的产物，`0.2.1` 在 6 处字面量里归 0，而平台三行（`minecraft_version` / `neo_version` / toolchain）一个字节都没动。

### 1.3 任务清单（F-267）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-267 | 版本号 0.2.2 与 6 处版本串 | S | 0.5 | — | `gradle.properties:11` 的 `mod_version` 改 `0.2.2`；`一键构建.cmd:5,11,12` 与 `检查环境.cmd:5,14` 的抬头行 / jar 名同步（含 `-sources.jar` 那行） |

阶段小计：**1 个任务、0.5 人日**（S 1 / M 0 / L 0）。

### 1.4 出口判据

- [ ] **产物名与版本行（F-267）**：怎么验：`.tools\gw.cmd build` 后 `jar tf build/libs/farmstorm-0.2.2+mc1.21.1.jar` 读 `META-INF/neoforge.mods.toml`。通过的样子：退出码 **0**；主 jar 与 `farmstorm-0.2.2+mc1.21.1-sources.jar` 同时在位；`build/libs` 里 `farmstorm-0.2.1*` 残留 **0** 个；`version` 行是 `0.2.2`。
- [ ] **6 处字面量一次改齐（F-267 / 48 号 §1.1）**：怎么验：三个文件逐个数 `0.2.1` 与 `0.2.2` 的命中行。通过的样子：`0.2.1` 命中 **0**；`0.2.2` 命中 = **1**（`gradle.properties`）+ **3**（`一键构建.cmd`）+ **2**（`检查环境.cmd`）= **6**；两处抬头行（各 `:5`）与 `一键构建.cmd:12` 的 `-sources.jar` 都在其中。
- [ ] **平台三行一字不动（负向）**：怎么验：`git diff gradle.properties` 的 hunk 数。通过的样子：只有第 **11** 行一行变化；`minecraft_version` / `neo_version` / toolchain 三行逐字未变（1.21.1 / 21.1.213 / 21）。
- [ ] **本节不碰数值面（负向）**：怎么验：`git status` 与 `git diff --stat src/`。通过的样子：`src/` 与 `run/` 的 diff 为空；本节的任何判据都不引用 `run/config/farmstorm-server.toml` 的键数与组数。
- [ ] **本节不在窗口内（负向 / 窗口纪律）**：怎么验：通读本节的 5 条判据。通过的样子：注册面 `98`、`items` 的 `34`、语言键总数这三个数在本节出现 **0** 次；本节不写「注册面」「语言键」的任何总数，也不提前动 `Farmstorm.java:176-179`。

### 1.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 只改主 jar、漏 `-sources.jar` | `一键构建.cmd:12` 仍按旧名复制，sources 静默不产出 | 6 处逐行点名；`0.2.2` 的命中数合计必须 = **6** |
| 只改 jar 名、漏抬头行 | 产物对了、横幅还写 `Farmstorm v0.2.1` | 两支脚本各 `:5` 单独算一处；抬头行与 jar 名分开数 |
| 漏 `检查环境.cmd:14` | 改名后找不到 jar，环境检查报「jar: 缺失」（实为改名） | 该行与提示文案同一段；改完真跑一次 `检查环境.cmd` |
| 顺手改平台参数 | `neo_version` 或 toolchain 被动，构建缓存全废、与基线不可比 | `git diff gradle.properties` 只允许 **1** 行 |
| 提前把版本号写成第二处来源 | 文档、脚本各自声明版本，两处漂移 | 版本号只有 `gradle.properties:11` 一处来源，其余只引用 |
| 顺手「预修」一个还没开的窗口（例如先改 `FarmstormItems:26`） | 本节变成半个 F-268，开窗动作被拆散，中间状态必打 FAIL | 本节只碰 3 个发布面文件；`97` / `33` / `338` 一个都不写 |

### 1.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；6 处字面量的「文件:行号」逐处点名。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`jar tf` 里 `neoforge.mods.toml` 的条目；三个文件的两轮 `grep -c`；`git diff gradle.properties` 的行数。
- **观察到什么**：`build/libs` 的完整清单（含 `0.2.1*` 残留 0 个）；`version` 行原文；`检查环境.cmd` 一次真跑的输出。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如 `.cmd` 在非 Windows 环境跑不了，写明替代读数）。
- **未关闭的问题**：例如 `build/libs` 里若另有 `-dev.jar` 之类的旁产物，只登记现象，本节不改构建脚本的输出面。

---

## 2. Phase 47 · R2 平凡韭菜（M43）

### 2.1 前置

- 上一节 §1.4 的**第 1 / 2 / 3 条**已绿：`farmstorm-0.2.2+mc1.21.1.jar` 与 sources jar 同时产出、`0.2.2` 命中 6 处、`git diff gradle.properties` 只 1 行。
- 基线读数（UTF-8 读法）：`registry/FarmstormItems.java` 现 **197** 行，物品段 = **22** 次 `cropItem(` + **7** 次 `seedItem(` + **4** 次 `registerItem(`（48 号 §1.2 第 1 行逐条给了行号）；`Farmstorm.java` 的断言在 `:176-179`；`src/main/java` 的 Java 文件数 **149**；`textures/item/` 现 **29** 张 PNG。
- **中间态窗口在本节 F-268 打开**：开窗动作 = `plain_chives` 注册 + `Farmstorm.java:176-179` 的断言 + 8 处「97」注释 + `verify_project.py` 两处期望 **同一个提交**。窗口内本节只写「`plain_chives` 这条在注册表里存在」「`items` 期望 **34**」「贴图**仍 29 张**」；**不写**语言键的任何总数。
- **断言与注册同一提交（死命令）**：只改注册不改断言 ⇒ 启动日志 `:180-184` 打 `→ FAIL`；只改断言不改注册 ⇒ `verify_project.py` 报 `98 / 97`。任一中间状态都是一个**必打 FAIL** 的提交，不许留在历史里。
- 冻结面：`verify_project.py` 的 **config 期望面**一个字节不动（`expected_config_keys*` 五个函数体、期望数 **176**）；`FarmstormItems` 的 `foodProperties(...)` / `otherProperties()` 两个方法体一行不动；`CropProfile` / `CROP_ITEMS` / `MODIFIED_CROPS` / `SEEDS` 在 `src/` 下各 **0** 命中。
- 手边四份：49 号 F-268 / F-269 与 §5.2 / §5.6（A-83 / A-84 / A-87）；48 号 §2.5、§3 #11 / #15 / #16 / #17 / #30 / #33；47 号 §3；`BRIEF` §3.4 第 14、17 行。
- 「`src/` 下命中 0」不许写成全仓 grep：`.scratch/p25a/src/` 是反编译源，会把命中数抬到几十。
- **本节是开窗节，不是关窗节**：语言面（`ModLangProvider:10` / `:14-19`）在本节**一个字都不动**，它由 F-281 一次改齐。

### 2.2 本阶段目标

把 `plain_chives` 从「注册表里的一个 id」变成「玩家在创造栏里看得见、后来还能被植物学家卖出来的物品」，并且**在同一个提交里**把注册面从 97 / 33 推到 98 / 34——这是本版唯一一处「改一半就打 FAIL」的地方。

本节结束时：启动日志 `98 / 34 … → OK`、脚本 `98 / 98`、贴图**仍 29 张**；语言键的总数**不在本节出现**，它由 F-281 一次收口。

### 2.3 任务清单（F-268 ~ F-269）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-268 | `plain_chives` 注册 + 注册面 98 的原子提交（**开窗**） | M | 1.5 | — | `FarmstormItems`（`:124` 前 +1 个 `DeferredItem`、`:127-136` +1 行、`:26` 注释 33 → 34）、`Farmstorm.java:176-179`（98 / 34）、8 处「97」注释、`tools/verify_project.py:334` / `:378` |
| F-269 | 平凡韭菜的可见面：模型改指朋友的韭 + 模型注释 | S | 0.5 | F-268 | `tools/gen_textures.py`（`plain_chives` 那条退役）、`ModItemModelProvider:51-54`（`layer0` 改指 `item/friend_chives`）、`:20` / `:84` 的两处「31 个」 |

阶段小计：**2 个任务、2.0 人日**（S 1 / M 1 / L 0）。

### 2.4 出口判据

- [ ] **启动日志整行（F-268 / 48 号 §8 #1）**：怎么验：起服后读 `run/logs/latest.log` 里 `Farmstorm.java:180-184` 打出的那一行。通过的样子：原文是 `自检 注册对象 98 个：items 34 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`，末尾是 `OK` 而不是 `FAIL`。
- [ ] **工具两行读数（F-268 / 48 号 §8 #2）**：怎么验：`python -X utf8 tools/verify_project.py`。通过的样子：打印 `已核 registry 对象: 98 / 98`；第 1 节 config 两行仍是 **176**（本任务不加键）；退出码 **0**。
- [ ] **断言只动两处、其余 11 处不动（F-268 / 49 号 A-83）**：怎么验：读 `Farmstorm.java:176-179` 并逐个数 `==`。通过的样子：`total == 98` **1** 处、`items == 34` **1** 处；**其余 11 个逐类 `==`**（`blocks 27` / `block_entities 8` / `entity_types 13` / `mob_effects 4` / `sound_events 3` / `recipe_types 1` / `recipe_serializers 1` / `creative_tabs 1` / `damage_types 3` / `data_components 1` / `menu_types 2`）逐字未变——是 11 个，不是 10 个。
- [ ] **8 处注释同提交（F-268 / 49 号 A-84）**：怎么验：`grep -n '\b97\b'` 在 `src/main/java` 下。通过的样子：改前 **9** 行（8 行注释 + `:176` 的代码），改后 **0** 行；8 处 = `Farmstorm.java:62` / `:156` / `:157` / `:189` + `registry/FarmstormAttachments.java:13` / `:16` / `:18` / `:22`。其中 `:156` 的沿革序列与 `Attachments:18` 的「97 → 98」按**改写**处理而不是逐字替换（见 §2.5 第 2 条），两行的语义仍成立。Java 文件数仍 **149**（本任务不新增类）。
- [ ] **工具两处期望面（F-268 / 48 号 §8 #3、#4）**：怎么验：读 `tools/verify_project.py:334` 与 `:380`（`EXPECTED` 里的 `plain_chives` 在 `:342`、`EXPECTED_COUNTS` 的 `"items"` 在 `:381`）。通过的样子：`EXPECTED["items"]` 的长度 = **34**（含 `plain_chives`）、`EXPECTED_COUNTS["items"] == 34`，且两处**同一个提交**；`EXPECTED_COUNTS` 的其余 **11** 项逐字未变；config 期望面（`expected_config_keys*` 五个函数体、期望数 176）**一个字节未改**。
- [ ] **进创造栏、不进作物面（F-268 / 48 号 §8 #28、#29）**：怎么验：读 `FarmstormItems#creativeTabItems()` 与作物自检行。通过的样子：该清单多 **1** 行、总数 **33 → 34** 项；`Farmstorm#logSelfCheck` 的「作物 22 种 / 投掷 12 / 部署 7 / 食用 3」四个数**一字不变**；`CROP_ITEMS` / `MODIFIED_CROPS` / `SEEDS` 里 `plain_chives` 命中 **0**。
- [ ] **贴图与模型注释（F-269 / 48 号 §8 #30、§10 #9）**：怎么验：数 `textures/item/` 的 `.png` 数并读 PNG 头四个字节，再 grep 注释。通过的样子：文件数**仍 29**（本版**不新增**）、`textures/item/plain_chives.png` **不存在**；`models/item/plain_chives.json` 的 `layer0` = `farmstorm:item/friend_chives`，该 PNG 的 IHDR 宽高都是 **16**；`ModItemModelProvider.java` 里 `31 个` 命中 **0**（`:20` 与 `:84` 两处都改），`basicItem(` 仍 **1** 处调用。

### 2.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 只改 `total` 不改 `items`（或反过来） | 启动日志 `:180-184` 直接 `→ FAIL`，而脚本可能仍绿 ⇒ 两套闸互相矛盾（RT-70 的第二种形态） | 两个数、8 处注释、工具两处期望**同一个提交**；判据 1 与判据 5 成对看 |
| 把 `:156` 的「64 → 77 → 90 → 97」与 `Attachments:18` 的「加一个就 97 → 98」逐字替换成 98 | 前者丢掉 97 这一环、后者变成「98 → 98」，注释自相矛盾——这正是 RT-68 要挡的那类错 | 8 处里 **6 处直改**、**2 处改写措辞**（沿革段补一句「v0.2.2 起为 98」并把假设式改成「再进一类就 99」）；判据 4 的「命中 0」只对 `\b97\b` 成立，两处改写的文本里不出现裸 `97` |
| 把 `plain_chives` 放进 `CROP_ITEMS` / `CropProfile` | `Farmstorm#logSelfCheck` 的 `taxonomyOk` 当场 FAIL；22 个作物变 23 | 它走 `registerItem(...)`，不进 `cropItem(...)`；判据 6 的四个数逐字比对 |
| 顺手动 `verify_project.py` 的 **config** 期望面 | 把「改默认值」误当成「改期望面」，去动 176 ⇒ 方向错（48 号 §4.4） | config 期望面一个字节不改；判据 5 的负向半句单独核一遍 |
| 给 `plain_chives` 加合成配方 / 加种子 / 建方块形态 | 多出第四条获取路径；`blocks` 27 → 28、`entity_types` 13 → 14，§2.4 的整行日志崩 | 裁定 #3 明令不做配方；`CropProfile` / `SEEDS` 命中 0 |
| 顺手改 `ModLangProvider` 的两处总数注释 | 窗口内提前改语言面 ⇒ 后面的每节都要重算总数，必有一节算错 | 窗口纪律第 2 条：`ModLangProvider` 一个字不动，直到 F-281 |
| 只补注册不改模型 | 注册表里有、创造栏里是紫黑格 | 判据 7 单独核 PNG 张数（仍 29）+ 模型 `layer0` + IHDR；提醒「datagen 不出 PNG」 |

### 2.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；F-268 的四处（`FarmstormItems` 三段、`Farmstorm.java:176-179`、两个类的 8 行注释、`verify_project.py:334` / `:380`）逐处点名；F-269 的两处模型注释、`ModItemModelProvider` 的 `layer0` 分支与 `gen_textures.py` 里退役的那一条。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；一次起服的日志读法；`python -X utf8 tools/verify_project.py` 的完整输出；`grep -c '\b97\b'` 改前改后两次；`textures/item/` 的文件计数。
- **观察到什么**：`latest.log` 那条自检行的**完整原文**（含 12 个逐类读数与末尾的 `OK`）；`EXPECTED_COUNTS["items"]` 的前后两行；`ModItemModelProvider` 两处注释的前后对照。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如「紫黑格」只能进游戏看，写明替代读数：PNG 张数（仍 29）+ 模型 `layer0` + IHDR 尺寸）。
- **未关闭的问题**：例如 `plain_chives` 的 `saturationModifier = 0.1` 与「饱食 2」的配套关系是否与 47 号 §3.3 逐字一致，本节只登记实现取值、不重开设计。

---

## 3. Phase 48 · R4 前半：界面两处缺行（M44）

### 3.1 前置

- 上一节 §2.4 的**第 1 / 4 / 6 条**已绿：启动日志 `98 / 34 … → OK`、`src/` 下 `97` 命中 0、创造栏 34 项。
- 基线读数（UTF-8 读法）：`client/OutpostCoreScreen.java` **42** 行（`:21-42` 类体、`:37-41` 只覆写 `renderBg`）、`render` 覆写 **0** 处、全仓 `renderTooltip` 命中 **0**；`menu/OutpostCoreMenu.java` 的 `ROWS = 6`（`:31`）、槽位 y = **139 / 157 / 175 / 197**（`:44` / `:46` / `:80`）。
- **本节在中间态窗口内 B 段（F-270 ~ F-280）**：本节不新增任何语言键、不动 config，判据里语言键总数出现 **0** 次；只写「本条键存在 / 不存在」与「本条的计数」（`blit(` 2 处、`render` 覆写 1 处）。
- 冻结面：槽位四个 y、`imageWidth` **176**、`imageHeight` **222**、贴图尺寸 **256 × 256**、`inventoryLabelY` **128** 全部不动；`OutpostCoreScreen` 里 `renderBg` 之外的三个方法（`renderLabels` / `init` / `Slot`）一个都不加。
- 手边四份：49 号 F-270 / F-271 与 §5.1（A-77 不适用于本版，只借用它的数法）；48 号 §2.1 / §2.2、§3 #6 / #7、§8 #20 ~ #23；47 号 §5.1 / §5.2；`BRIEF` §6 硬禁令第 2 条。
- **两个 bug 都是「缺行」**：不是调参、不是优化、不是「补一个功能」。缺一次 `renderTooltip` 调用、缺一次 `blit`——把修法写成「优化渲染」就是 RT-62。
- 环境：能起单人客户端，且能截 1 张 54 格界面图与 0.2.1 的旧图并排（像素比对的唯一证据面）。

### 3.2 本阶段目标

把两个「看起来像调参、实际是缺行」的 bug 按缺行修掉——tooltip 少一次 `renderTooltip` 调用、背景少一次 blit。

本节结束时：悬停有 tooltip 且第一行是物品名；背包区物品与格子像素级重合（0 px，改前 1 px），而槽位坐标一个都没动。

### 3.3 任务清单（F-270 ~ F-271）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-270 | `OutpostCoreScreen` 补 `render` 覆写（tooltip） | S | 0.5 | — | `client/OutpostCoreScreen.java:21-42` 里新增 **1** 个 4 参 `render` 覆写（先 `super.render(...)`、后 `this.renderTooltip(...)`）；`:37-41` 的 `renderBg` 一行不动 |
| F-271 | `renderBg` 一次 blit → 两次 blit | S | 0.5 | F-270 | `client/OutpostCoreScreen.java:37-41`：上段 `v = 0` 高 **125**、下段 `v = 126` 高 **96** 画到 `topPos + 125`；行数常量取 `OutpostCoreMenu.ROWS`（= 6） |

阶段小计：**2 个任务、1.0 人日**（S 2 / M 0 / L 0）。

### 3.4 出口判据

- [ ] **tooltip 出现（F-270 / 48 号 §8 #20）**：怎么验：己方玩家打开 54 格界面，悬停第 **1 / 28 / 54** 格（放好物品）与一个**空**格子、一个玩家背包格。通过的样子：三格里 tooltip **都出现**且**第一行是物品名**；空格子**没有** tooltip（不是空框）；背包格与 54 格区表现一致。
- [ ] **tooltip 的详情行与越界翻转（F-270 / 48 号 §8 #21）**：怎么验：悬停带附魔的物品与带本模组效果组件的物品，再悬停**最右侧一列**。通过的样子：多出附魔行 / 效果行且样式与普通箱子一致；最右列的 tooltip **完整**（原版翻到左侧，本版不接管定位）。
- [ ] **`render` 覆写的形态（F-270 / 48 号 §2.1.2）**：怎么验：读 `OutpostCoreScreen` 并数调用点。通过的样子：`renderTooltip(` 命中 **1**、`super.render(` 命中 **1** 且文本上**先**出现；`render` 覆写 **1** 处（4 参 `GuiGraphics, int, int, float`，`@Override` 在位）；`renderBg` 覆写仍 **1** 处且逐字未变；`public` 可见性只在既有方法上，没有把 `renderTooltip` 改成 `public`。
- [ ] **两次 blit 的形态（F-271 / 48 号 §8 #23）**：怎么验：读 `renderBg` 的方法体。通过的样子：`blit(` 恰 **2** 处；两个 `v` 分别是 **0** 与 **126**；第二段的 y 是 `topPos + 125`、高 **96**；贴图尺寸仍 **256 × 256**（不是 176 × 222）；`imageWidth` 仍 **176**、`imageHeight` 仍 **222**。
- [ ] **像素级对齐（F-271 / 48 号 §8 #22）**：怎么验：把同一种物品分别放进 54 格区与玩家背包区，截图并与 0.2.1 的旧图并排比对背包 3 行。通过的样子：背景格顶边与物品 16×16 顶边相差 **0 px**（改前 **1 px**）；截图文件名进汇报。
- [ ] **槽位与行数（F-271 / 负向）**：怎么验：读 `OutpostCoreMenu:44-46` / `:80`、`OutpostCoreScreen` 的 UTF-8 行数与 `git diff --stat`。通过的样子：槽位 y 仍是 **139 / 157 / 175 / 197**（一个都没动）；`OutpostCoreScreen` 由 **42** 行只增不减、终值 **53**（F-270 的两次 blit +6、F-271 的 `render` 覆写 +5：注解 + 签名 + `super.render` + `renderTooltip` + 收尾）；精确值仍以 `git diff --stat` 的 `+N / −M` 为证据；`menu_types` 仍 **2**（不新增屏幕类）。

### 3.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 覆写 `render` 却不调 `super.render(...)` | 背景、槽位、`renderLabels` 与拖拽物全被 tooltip 盖住——0.2.1 明令否决的四条之一 | 判据 3 要求两条调用都在位，且 `super.render(` 在文本上先出现 |
| 把签名写成 3 参 | 变成新重载而不覆盖，`@Override` 直接编译失败 | 判据 3 点明 4 参（`GuiGraphics, int, int, float`） |
| 顺手接管 tooltip 的定位 / 缩放 / 越界翻转 | 最右列的 tooltip 被裁、或与普通箱子样式不一致（RT-59 的同类） | 只写「有 tooltip」与「最右列完整」，不实现任何定位代码 |
| 把下段 `v` 写成 **125** | 下段整条贴图错 1 px，改完比改前更歪 | 判据 4 把 `125`（目标 y）与 `126`（贴图 v）分开点名 |
| 下段高写成 `IMAGE_HEIGHT − 125 = 97` | 补了原版没有的第 222 行像素，边缘出现一条贴图残迹 | 高固定 **96**；`125 + 96 = 221` 比 222 少 1 px 是原版行为，照抄 |
| 按 plan 的「上移」去改槽位 y | 54 格与背包格一起错位（47 号 §5.2.4） | 槽位四个 y 写进冻结面；判据 6 单独核 |
| 顺手把 `ModLangProvider` / `FarmstormItems:26` 的注释也「对齐」一下 | 窗口内提前动语言面 / 已关的注册面注释，判据里的读数基线漂移 | 本节只碰 `OutpostCoreScreen` 一个文件；语言键总数命中 0 是判据外的硬约束 |

### 3.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；F-270 的 5 行覆写与 `renderBg` 的「零改动」证据（覆写数 **1**）；F-271 的两段 `blit` 与 `upper` 变量的取值来源（`OutpostCoreMenu.ROWS`）。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；三次进游戏的悬停（有物品 / 空格 / 背包格）；两处源码 grep（`renderTooltip(` / `blit(`）；`OutpostCoreScreen` 的 UTF-8 行数。
- **观察到什么**：tooltip 的**第一行原文**（中文名）；最右列的完整截图；12 组像素比对的行读数（背包 3 行各 1 组）；类行数 42 → 47。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如第二客户端不可用时，「非己方打不开界面」一条只能引用 0.2.1 的既有读数）。
- **未关闭的问题**：例如 221 vs 222 少的那 1 px 在 4K 缩放下是否可见，本节只记截图、不改实现。

---

## 4. Phase 49 · R3 标签与名额 + 两个默认值（M45）

### 4.1 前置

- 上一节 §3.4 的**第 4 / 5 / 6 条**已绿：`blit(` 2 处、像素差 0 px、槽位四个 y 未动。
- 基线读数（UTF-8 读法）：`util/FarmstormTags.java` 现 **6** 个 `TagKey`（`:18` / `:24` / `:27` 三个 `TagKey<Block>` + `:30` / `:33` / `:36` 三个 `TagKey<Item>`）；`datagen/ModBlockTagsProvider#addTags` 现 **5** 条 `tag(...)`（`:24` / `:37` / `:42` / `:44` / `:62`）；`OutpostCoreBlockEntity` 的两处判据在 `:393`（`pruneCultivators`）与 `:783`（迁移失败路径）。
- 默认值现状：`config/FarmstormConfig:943` 是 `maxPerPlayer` 的 `defineInRange(..., 2, 1, 64)`、`:956` 是 `cultivatorCapacity` 的 `defineInRange(..., 16, 1, 64)`；盘上 `run/config/farmstorm-server.toml:753` = **2**、`:765` = **16**。
- **本节仍在中间态窗口内**：本节的三个任务都不新增语言键，判据里语言键总数出现 **0** 次；「上限 4 / 绑定 24」是新 config 的读数，与语言面无关。
- **老 toml 的红是期望中的红**：改默认值**不会**更新已存在的 toml（NeoForge 只补键 / 删键）⇒ 判据 6 专门用老 toml 断言「仍是 2 / 16」，它必须与判据 5（新 config 的绿）**成对**跑。只跑一条就是误判。
- 冻结面：`MAX_LINKS = 1024`（`network/CultivatorLinkPayload:36`）一个字符不动；`FarmstormConfig` 的 `b.push(` / `b.pop()` 各 **60**、行首 `.define*` **176**；`OutpostCoreBlockEntity:546-547` 的 `AreaBlockEntities.collect(..., CultivatorBlockEntity.class)` 不改；`addCultivator(BlockPos)` 签名不动；`converter` 的方块 / 配方 / `CONVERTER_FUEL` / 菜单全部照旧。
- 手边四份：49 号 F-272 / F-273 / F-274 与 §5.3 / §5.4（A-85 / A-90）；48 号 §2.6 / §2.7、§3 #18 ~ #28、§8 #6 / #9 / #10 / #24 ~ #26；47 号 §4、§5.3 ~ §5.5；`BRIEF` §3.4 第 5、6、12、13、18 行。
- **三个「16」的白名单**：`FarmstormConfig:841`（桑吉尔蔗抬升 16 格）、`behavior/ZangiefPiledriverController:52`（16 格 / 15 刻）、`client/WildHunterClientState:15`（`loopSoundRange` 默认 16.0）——这三个数与本键无关，改它们会引入新错误。

### 4.2 本阶段目标

把「能不能被核绑定」的判据从**类型**换成**标签**，并把两个名额默认值改成 4 / 24；同时把八处写死旧默认值的注释一次改齐。

本节结束时：`FarmstormTags` 恰 7 个 key、产物 `processing_block.json` 只有培养器一个成员、新生成的 toml 是 4 与 24、`src/` 下旧默认值的四个串只剩 3 处白名单命中。

### 4.3 任务清单（F-272 ~ F-274）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-272 | `FarmstormTags.PROCESSING_BLOCK` + 标签产物 | S | 0.5 | — | `util/FarmstormTags.java:18-36` 第 7 个 `TagKey<Block>`；`datagen/ModBlockTagsProvider` +1 条 `tag(...)`；产物 `data/farmstorm/tags/block/processing_block.json` |
| F-273 | 绑定判据落点：两处 `instanceof` → 标签判定 | M | 1.5 | F-272 | `blockentity/OutpostCoreBlockEntity:393` / `:783` 改调同一个私有静态辅助方法（判据即「该 state 带 `PROCESSING_BLOCK` 标签」） |
| F-274 | 两个默认值 4 / 24 + 八处注释同提交 | M | 1.5 | F-273 | `config/FarmstormConfig:943` 与 `:956` 的第二实参；8 处注释（`:236` / `:238` / `:952` / `:333` / `OutpostCoreBlockEntity:116` / `:349`、`OutpostCoreMenu:30`、`CultivatorLinkPayload:31-34`（改前）） |

阶段小计：**3 个任务、3.5 人日**（S 1 / M 2 / L 0）。

### 4.4 出口判据

- [ ] **标签 7 个 key（F-272 / 48 号 §8 #6）**：怎么验：读 `FarmstormTags` 数 `TagKey` 声明。通过的样子：声明数 = **7**（`TagKey<Block>` **4** 个 + `TagKey<Item>` **3** 个）；`PROCESSING_BLOCK` 的 path 字面量是 `processing_block`；**不是 8**——POI 那条标签用的是原版 `PoiTypeTags.ACQUIRABLE_JOB_SITE`，不在本类里。
- [ ] **标签产物只有一个成员（F-272 / 48 号 §8 #6、#7）**：怎么验：跑一次 datagen 后读 `src/generated/resources/data/farmstorm/tags/block/processing_block.json`。通过的样子：文件存在，`values` 里**只有** `farmstorm:cultivator` 一个成员；`converter` 命中 **0**；`ModBlockTagsProvider` 的 `tag(` 调用 **5 → 6**。
- [ ] **判据落点（F-273 / 48 号 §2.6.3）**：怎么验：grep 两个 `instanceof` 与辅助方法。通过的样子：`instanceof CultivatorBlock` 在**落点文件 `OutpostCoreBlockEntity`** 内命中 **0**（全仓另有 **3** 处既有命中：`CultivatorBlockEntity:257`、`ModItemModelProvider:69`、`ModBlockStateProvider:79`，属按方块类型判别的 datagen / 方块自身逻辑，**不属本判据面、不许改**）；新辅助方法的声明 **1** 处、调用 **2** 处（`:393` 与 `:783` 是改前位置）。
- [ ] **类型面不放开（F-273 / 负向）**：怎么验：读 `OutpostCoreBlockEntity:546-547` 与 `:358`。通过的样子：`AreaBlockEntities.collect(..., CultivatorBlockEntity.class)` 仍是 **1** 处且参数未变；`addCultivator(BlockPos)` 的签名与 `:783` 的日志文案逐字未变；行为等价（把已绑位置换成非标签方块 ⇒ 该条被摘；换成 `cultivator` ⇒ 保留）。
- [ ] **新 config 的绿（F-274 / 48 号 §8 #10、#24、#25）**：怎么验：备份后删掉 `run/config/farmstorm-server.toml`，重启让游戏重生成。通过的样子：重生成的 toml 里 `maxPerPlayer = 4`、`cultivatorCapacity = 24`；键 **176**、段 **60**；游戏内同一玩家放第 **4** 个核成功、第 **5** 个被拒绝，且被拒绝时物品**不消耗、不掉落**。
- [ ] **老 toml 的期望红（F-274 / 48 号 §8 #9）**：怎么验：把升级前的 toml 放回再启动，读 `:753` 与 `:765`。通过的样子：读到 `maxPerPlayer = 2` 与 `cultivatorCapacity = 16`——**这条红是正常的**，必须与上一条成对记录，并写明「默认值只对新装生效」。绑定侧的读数：一个核周围放 **25** 个培养器 ⇒ 绑定表恰 **24** 条且不再增长，第 25 个绑不上，已绑的 24 条顺序与坐标一条不变。
- [ ] **八处注释与常量（F-274 / 48 号 §8 #26、49 号 A-85 / A-90）**：怎么验：`grep '默认 16\|名额 16\|上界 16\|× 16'` 在 `src/main/java` 下 + 数结构与常量。通过的样子：改前 **11** 行，改后 **3** 行且全部落在白名单 {`FarmstormConfig:841`、`ZangiefPiledriverController:52`、`WildHunterClientState:15`}（`OutpostCoreBlockEntity:333` 的「× 16 条」按 A-85 升为必改，改成 24）；`b.push(` 与 `b.pop()` 仍各 **60**、行首 `.define*` 仍 **176**；`MAX_LINKS = 1024` 仍 **1** 处。

### 4.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 把 POI 那条标签也写成自己的 `TagKey` | `FarmstormTags` 变 **8**，与 48 号 §2.6.1 自相矛盾，`FarmstormTags` 的注释也错 | 判据 1 明写 4 + 3 = 7；POI 标签走原版 `PoiTypeTags`，由 F-276 补 |
| 顺手把 `converter` 一起 `add` 进标签 | 越了范围闸（裁定 #5 整条搁置），B-16 当场红 | 判据 2 的负向半句；产物里 `converter` 命中 0 |
| 把候选发现也换成标签 | 等于给 0.2.3 的转换器预留接口，裁定 #5 明令禁止；且非 `CultivatorBlockEntity` 没有写回 `Core` 缓存的入口 | 只有两处判据改，`:546-547` 与 `:358` 进冻结面 |
| 以为改了 `defineInRange` 就等于玩家看到 4 / 24 | 老存档 toml **不会**变，拿老 toml 判这两条**必然假红**（RT-63） | 判据 5 与 6 成对；汇报里两条必须相邻出现 |
| 漏 `OutpostCoreBlockEntity:333` 的「× 16 条」 | 48 号 §8 #26 的 grep 永远留一个命中，判据 7 的「3 处白名单」不成立 | 按 A-85 把它列进八处；改后 grep 只有 3 行白名单 |
| 顺手改了三处无关的 16（`:841` / `:52` / `:15`） | 抬升高度、相位常数、音效范围被换成 24 ⇒ 三处玩法数值被动 | 白名单三条点名写死；判据 7 只允许这 3 行 |
| 把 `MAX_LINKS` 抬到 4096 | 协议串仍 `"2"` 的兼容承诺多一个不必要的变更点（D-201 已否决） | 常量在 `:36`（改前 `:35`），冻结面；只改 `:31-34`（改前）的 javadoc |
| 注释分两次提交 | 留下「注释说 16、代码是 24」的窗口——比不写注释更糟 | 八处注释与两个数字同一个提交；判据 7 一次核全部 |
| 以为「名额改 24」要顺手改语言键的描述 | 去动 `ModLangProvider` 或两份 `lang/*.json` ⇒ 窗口内提前开第二个口子，F-281 的「一次改齐」失效 | 两个键的描述**不含默认值**（48 号 §2.7.1 第 2 条）⇒ 语言键**不用改**；本节判据里语言键总数出现 0 次 |

### 4.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；F-272 的常量与 provider 那一行、产物 json 的路径；F-273 的辅助方法与两处调用点行号；F-274 的两个 `defineInRange` 与八处注释的「文件:行号」逐条。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；一次 datagen；两次起服（新 config / 老 toml）；`grep -c '默认 16\|名额 16\|上界 16\|× 16'` 改前改后；`b.push` / `b.pop` / `.define*` 三个计数。
- **观察到什么**：`processing_block.json` 的完整内容；新生成 toml 的两行原文与键 / 段计数；老 toml 的两行原文（标「期望中的红」）；`/data get block <core> Cultivators` 的 24 条读数与第 25 个绑不上的现象。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如「第 5 个核被拒绝」需要创造模式给 5 个核，写明替代读数：`countOwned` 的日志行）。
- **未关闭的问题**：例如标签只有 1 个成员时「按标签算 24」与「按类型算 24」无差异（47 号 §4.2），本节只登记这条等价、不声称有行为差异。

---

## 5. Phase 50 · R1 前半：两张全新注册表（M46）

### 5.1 前置

- 上一节 §4.4 的**第 1 / 2 / 5 / 7 条**已绿：标签 7 个 key、产物只有培养器一个成员、新 config 是 4 / 24、八处注释已改。
- 基线读数（UTF-8 读法）：`Farmstorm.java` 构造器的 `.register(modBus)` 现 **12** 行（`:48` ~ `:63`，其中 `FarmstormAttachments` 在 `:63`）；`datagen/FarmstormDataGen` 现 **11** 个 `addProvider`；`src/main/java` 的 Java 文件数**本节开始前是 149**、本节结束时 **152**（三个任务各 +1）。
- **本节仍在中间态窗口内**：两张新注册表都**不产生语言键**（POI 类型与标签没有语言键，职业名由 F-281 写一条）⇒ 本节判据里语言键总数出现 **0** 次；「职业名」这条语言键**不许**在本节顺手加，否则等于把关窗动作拆成三处。
- **顺序纪律**：`FarmstormPoiTypes` 的 `.register(modBus)` **先于** `FarmstormVillagerProfessions`，两行都排在 `FarmstormAttachments`（`:63`）之后。理由：职业的谓词引用 POI 的 `ResourceKey`，先 POI 后职业与原版读法一致。
- **两张表都不进 98**：口径外累计 **3 类**（`ATTACHMENT_TYPES` 0 增量 + `VillagerProfession` +1 + `PoiType` +1）。`verify_project.py` 的 `EXPECTED` 12 组里没有这两类 ⇒ 它**既不核存在也不核计数**，判据只能写「源码里各 1 个 + 启动日志仍是 `98 … → OK`」。
- **POI 必须进 `minecraft:acquirable_job_site`**（F-276）：不加的后果是村民永不认领、而且**不报错**——代码路径是 `Villager.java:181-182` 的 `POI_MEMORIES` 只认这条标签。产物必须落在 `data/minecraft/tags/point_of_interest_type/acquirable_job_site.json`，写进 `data/farmstorm/` 等于没加。
- 冻结面：`Farmstorm.java:176-179` 的断言逐字未变（POI / 职业不进 98）；`FarmstormAttachments` 一行不动；`FarmstormDataGen` 里既有的 11 个 provider 一个都不动；`sound_events` 仍 **3**（`workSound` 传 `null`）。
- 手边四份：49 号 F-275 / F-276 / F-277 与 §5.6（A-82 的 19 项里 #8 / #9 / #19 落在这里）；48 号 §1.3、§2.3.1 ~ §2.3.6、§3 #8 / #9 / #10 / #20 / #21、§8 #8 / #12 ~ #16、§9 的 ① ② ③ ⑥ ⑩；47 号 §2.2；`BRIEF` §3.4 第 8、9、19 行。

### 5.2 本阶段目标

把本项目**第一次**引入的两张原版注册表（`PoiType` / `VillagerProfession`）落地并接线，同时把「没有那条标签就永不认领」的隐蔽依赖补上。

本节结束时：两只新表各 1 个对象、`.register(modBus)` 12 → 14 行（POI 在前）、`acquirable_job_site.json` 含 `farmstorm:botanist`、Java 文件数 149 → 152。

### 5.3 任务清单（F-275 ~ F-277）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-275 | `registry/FarmstormPoiTypes`（job site，口径外第 3 类） | M | 1.5 | F-268 | 新 1 个类（`DeferredRegister<PoiType>` + `BOTANIST` + `BOTANIST_KEY`；id `farmstorm:botanist`；匹配集 = 据点核心的 `defaultBlockState()` 单例；`maxTickets = 1`、`validRange = 1`）+ `Farmstorm.java` +1 行 |
| F-276 | `datagen/ModPoiTypeTagsProvider`：进 `acquirable_job_site` | M | 1.5 | F-275 | 新 1 个类（继承抽象 `TagsProvider<PoiType>`、5 参构造器第四参 `null`、`addTags` 只写一条）+ `FarmstormDataGen` +1 个 `addProvider`（挂 `event.includeServer()`） |
| F-277 | `registry/FarmstormVillagerProfessions`（植物学家） | M | 1.5 | F-275 | 新 1 个类（六字段：`"botanist"` / 两个谓词都只认 `BOTANIST_KEY` / 两个空 `ImmutableSet` / `workSound = null`）+ `Farmstorm.java` +1 行（**排在 POI 之后**） |

阶段小计：**3 个任务、4.5 人日**（S 0 / M 3 / L 0）。

### 5.4 出口判据

- [ ] **POI 注册与取值（F-275 / 48 号 §8 #12）**：怎么验：读新类并数注册。通过的样子：恰 **1** 个 `PoiType` 注册，id 字面量 `botanist`；`maxTickets` = **1**、`validRange` = **1**；匹配集**恰好 1 个** state（据点核心的 `defaultBlockState()`，不是空集、不是第二个方块）；启动日志仍是 `98 … → OK`（POI 不进 98）。
- [ ] **职业六字段（F-277 / 48 号 §2.3.4）**：怎么验：读新类逐字段比对。通过的样子：恰 **1** 个职业注册，id 字面量 `botanist`；`requestedItems` 与 `secondaryPoi` 都是**空集**；`workSound` 是 `null`；两个谓词**只认 POI key**——不按方块、不按 `ownerUuid`、不按距离，且**不是** `ALL_ACQUIRABLE_JOBS`。
- [ ] **注册行数 12 → 14 且 POI 在前（F-275 / F-277 / 48 号 §3 #10）**：怎么验：数 `Farmstorm.java` 构造器的 `.register(modBus)` 并看行序。通过的样子：**14** 行；`FarmstormPoiTypes` 那一行的行号**小于** `FarmstormVillagerProfessions` 那一行；两行都在 `FarmstormAttachments`（`:63`）之后；`:176-179` 的断言**逐字未变**（相对 F-268 之后的状态，即 `total == 98` / `items == 34`）。
- [ ] **POI 标签产物（F-276 / 48 号 §8 #8、#16）**：怎么验：跑一次 datagen 后读产物路径与内容。通过的样子：`src/generated/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` 存在，`values` 含 `farmstorm:botanist`；`data/farmstorm/` 下**没有**同名文件；没有多出 `minecraft:village` 与 `minecraft:bee_home` 两份不该由我们写的文件；`FarmstormDataGen` 的 `addProvider` **11 → 12**，新 provider 挂在 `event.includeServer()`。
- [ ] **Java 文件数（F-275 ~ F-277 / 48 号 §1.1）**：怎么验：数 `src/main/java` 的 `.java` 文件。通过的样子：**149 → 152**（三个任务各 +1，逐节记录 150 / 151 / 152；本版终态 **154**）；`menu_types` 仍 **2**、`blocks` 仍 **27**。
- [ ] **认领的行为读数（F-283 复核，本节只留可复现步骤）**：怎么验：放 1 个据点核心 + 1 个**无业**村民，站到核旁 2 格内后读 `/data get entity <villager> VillagerData.Profession`。通过的样子：读数是 `"farmstorm:botanist"`；同一核旁再拉 1 个无业村民 ⇒ 第二个**不会**变成植物学家（`maxTickets = 1` 的语义）；**有主**的核（`ownerUuid` 非空）**照样**被认领。反向那一条（临时注释掉 `tag(...)` 再 datagen ⇒ 永不认领）由 §8.4 的 `#16` 执行，本节只负责让「加着」成为默认态。

### 5.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 只写标签 provider、忘了挂 `gatherData` | datagen 根本不跑它，产物不生成，而源码「看起来全对」 | 判据 4 直接核产物路径与内容，不看「代码写了」 |
| 继承原版 `PoiTypeTagsProvider` | 会连带生成 `minecraft:village` 与 `minecraft:bee_home` 两份不该由我们写的标签文件 | 判据 4 的负向半句；继承抽象 `TagsProvider<PoiType>` |
| 目录写成 `data/farmstorm/...` | 命名空间错，等于没加 ⇒ 村民永不认领且不报错（RT-64 的主形态） | 判据 4 点名 `data/minecraft/tags/point_of_interest_type/` |
| 谓词用 `ALL_ACQUIRABLE_JOBS` | 原版 13 种工作站全变成植物学家的候选，村民会跑去别人的工作站上班 | 判据 2 明写「不是 `ALL_ACQUIRABLE_JOBS`」 |
| 自作聪明加「必须无主 / 必须己方」 | 把核放在自己基地的玩家**永远**招不到植物学家（RT-65） | 判据 6 用**有主核**做正向读（防「好心加判据」） |
| 注册顺序写反（先职业后 POI） | 与 48 号 §2.3.6 的顺序纪律相反，读代码的人会怀疑职业引用了未注册的 POI | 判据 3 比行号大小 |
| 顺手在 `ModLangProvider` 加职业名的语言键 | 窗口被提前开一个口子，F-281 的「一次改齐」变成三次 | 本节判据里语言键总数与职业名的语言键出现 0 次；语言面留到 §7 |
| 把 POI / 职业算进 98 | 得出 100 的错误结论，`verify_project.py` 却仍然绿 | 判据 1 与 3 都写「启动日志仍是 98」；口径外 3 类写进汇报 |

### 5.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；三处新类与两个 +1 行（行号 + 先后顺序）；F-276 的 provider 挂载点；产物 json 的完整内容。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；一次 datagen；一次起服读日志；`/data get entity` 三次（第一个村民 / 第二个村民 / 有主核）；`src/main/java` 的文件计数。
- **观察到什么**：`latest.log` 自检行仍是 `98 … → OK`；`VillagerData.Profession` 的三组读数原文；`acquirable_job_site.json` 的 `values` 数组。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如「村民要走到 2 格内」是异步的，写明等待刻数与失败时的重试动作）。
- **未关闭的问题**：例如 `AssignProfessionFromJobSite` 的 `findFirst()` 择业顺序（48 号 §9 第 ⑥ 条）——本版靠「没有第二个职业认同一个 POI」规避，本节只登记、不加优先级 hack。

---

## 6. Phase 51 · R1 后半：两档交易与分支接线（M47）

### 6.1 前置

- 上一节 §5.4 的**第 1 / 2 / 3 / 5 条**已绿：POI 与职业各 1 个、`.register(modBus)` 14 行且 POI 在前、Java 文件数 152。
- 基线读数（UTF-8 读法）：`event/VillagerTradeEvents.java` **37** 行（`:29-36` 是 `onVillagerTrades`、`:10-11` 是类注释里那句「每次 `TagsUpdatedEvent`（数据包重载，含起服）」）；`VillagerProfession.FARMER` 的判断在 `:30`，农民三行 `add` 在 `:33` / `:34` / `:35`；`src/main/java` 的 Java 文件数 **152**。
- **本节仍在中间态窗口内**：两档交易**不新增语言键**（交易项复用物品名），判据里语言键总数出现 **0** 次；`config` 键数仍 **176**（价格是类常量，不读 `trade.*` 的 6 个键）。
- **交易相关验收必须重启服务端**：`VillagerTradingManager` 只在 `UpdateCause == SERVER_DATA_LOAD` 时抛 `VillagerTradesEvent`，`/reload` 不重发。`/reload` 之后看到旧列表是**正常**的，不是「分支没生效」。
- 补充一条源码事实：`getTrades().get(1)` 与 `.get(5)` **不会**返回 `null`（管理器把 1 ~ 5 级全填成非空列表），所以不需要 `put(new ArrayList<>(...))` 退路，写回 `VillagerTrades.TRADES` 由管理器负责。
- 冻结面：农民那**三行 `add` 一行不动**；`TierTrade` 一行不动；原版各职业的交易表不清空、不改写；`FarmstormVillagerProfessions.BOTANIST` 的引用只出现在新分支里。
- 手边四份：49 号 F-278 / F-279 / F-280 与 §5.6（A-82 的 19 项里 #10 / #11 落在这里）；48 号 §1.1、§2.4.1 ~ §2.4.5、§3 #12 / #13 / #14 / #29、§8 #17 ~ #19、§9 的 ④ ⑤ ⑦；47 号 §2.3 ~ §2.7、§6 的第 1 / 3 / 6 / 8 / 10 / 12 / 16 ~ 20 行。

### 6.2 本阶段目标

给植物学家挂上 22 条普通交易（可见 2 条）与 1 条固定史诗，并把分支写进事件处理器而不动农民那三行。

本节结束时：`NORMAL.size() == 22`、界面可见 **2** 项、5 级出现 **1** 项史诗且补货两次不变、农民三行逐字未变。

### 6.3 任务清单（F-278 ~ F-280）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-278 | `event/BotanistTrade`：22 项普通交易 | M | 1.5 | F-268 | 新 1 个类（`record BotanistTrade(Item)` + 6 个类常量 + `NORMAL` 22 个静态实例；闭区间随机价、不抽签、不返回 `null`） |
| F-279 | `event/BotanistEpicTrade`：固定 1 项史诗 | S | 0.5 | — | 新 1 个类（`record BotanistEpicTrade(List<CropProfile> pool)` + 常量 40 / 64 / 1 / 1 / 2 / 0.0F + 单实例 `EPIC`，池 = 4 个 `CropTier.EPIC`） |
| F-280 | `VillagerTradeEvents`：植物学家分支 + 注释漂移 | S | 0.5 | F-277、F-278、F-279 | `event/VillagerTradeEvents:29-36` 加分支（写在前、带 `return`）；`:10-11` 的注释改成「只在 `SERVER_DATA_LOAD`；`/reload` 不重发」 |

阶段小计：**3 个任务、2.5 人日**（S 2 / M 1 / L 0）。

### 6.4 出口判据

- [ ] **22 条普通交易（F-278 / 48 号 §8 #17）**：怎么验：`grep -c 'new BotanistTrade('` 与 `BotanistTrade.NORMAL.size()`。通过的样子：前者 = **22**；后者 = **22**；清单第 **19** 项是 `farmstorm:compressed_puffed_wheat`、第 **20** 项是 `farmstorm:plain_chives`（47 号 §2.3 的对应两行）；`getOffer` 里**没有**第二次抽签、**不返回 `null`**。
- [ ] **六个类常量与键数不变（F-278 / 48 号 §2.4.3）**：怎么验：读常量并数 config 键。通过的样子：`3` / `6` / `8` / `1` / `2` / `0.0F` 六个值逐一在位；`FarmstormConfig` 的键数仍 **176**、组数仍 **60**（价格是类常量，不读 `trade.*`、不新增键）。
- [ ] **史诗池与常量（F-279 / 48 号 §8 #19 的前半）**：怎么验：读池的构造与常量。通过的样子：池大小 = **4**，成员是 `awakened_torchflower` / `awakened_pitcher_plant` / `lucky_berry` / `wild_apple`；常量 = `40` / `64` / `1` / `1` / `2` / `0.0F`；**池抽取**恰 **1** 处（`pool.get(random.nextInt(pool.size()))`，`BotanistEpicTrade:57`）；**定价另有一处 `random.nextInt`**（`MIN_PRICE + random.nextInt(MAX_PRICE - MIN_PRICE + 1)`，与 `TierTrade:75` 同形）⇒ **不许用机械 grep 数 `random.nextInt` 总数**；进的是 **5 级**。
- [ ] **分支接线与农民三行（F-280 / 48 号 §3 #14、#29）**：怎么验：读 `VillagerTradeEvents:29-36` 与 `:10-11`。通过的样子：植物学家分支**写在农民早退之前**且**带 `return`**，两段**不是**合并成一个 `if`；`VillagerProfession.FARMER` 的判断仍在且**在植物学家分支之后**；`:33` / `:34` / `:35` 三行 `add` 的文本逐字未变；`:10-11` 里 `TagsUpdatedEvent` 命中 **0**、`SERVER_DATA_LOAD` 命中 ≥ **1**。
- [ ] **界面只见 2 项（F-280 / 48 号 §8 #18，必须重启服务端）**：怎么验：改完交易后**重启服务端**（不用 `/reload`），与植物学家开界面。通过的样子：只显示 **2** 项普通交易（不是 22 项）；换一个**新**植物学家 ⇒ 至少有一项不同；记录里写明「重启」二字与启动的 `gameTime`。
- [ ] **5 级 1 项史诗且补货不变（F-280 / 48 号 §8 #19，必须重启服务端）**：怎么验：把植物学家练到 5 级，看报价；再让它补货**两次**。通过的样子：出现 **1** 项史诗报价；补货两次后**仍是同一个物品**，价格仍落在 **40 ~ 64**；记录里不出现「每次补货重抽」这类表述（RT-67）。
- [ ] **文件数与不越界（F-278 / F-279 / 负向）**：怎么验：数 Java 文件与 `TierTrade` 的改动面。通过的样子：`src/main/java` 的 Java 文件数 **152 → 154**（F-278、F-279 各 +1）；`TierTrade` 的 `diff` 为空；`VillagerTrades.TRADES` 的静态表没有被本版改写（只在事件里 `add`）。

### 6.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 两个职业合并成一个 `if` | 改到农民的口径，等于推翻裁定 #7；农民会多出 22 项 | 判据 4 要求两段分开、各自 `return`，且农民三行逐字未变 |
| 忘了 `return` | 分支穿到农民那三行 ⇒ 农民列表 3 → 25 项 | 同上；判据 5 与 6 在重启后看界面 |
| 在 `getOffer` 里再抽一次签 | 「22 项」变成「22 × 每次刷新」，列表长度不可复现 | 判据 1 明写不抽签；`size() == 22` 是唯一能抓到的闸 |
| 某条 listing 返回 `null` | 该条**静默消失**（原版对 null 是「跳过、不计数、不报错」），列表少于 22 条 | 判据 1 的 `size() == 22` 逐条兜；不许写 `null` 分支 |
| 用 `/reload` 判分支是否生效 | 看到旧列表，误判成「代码没生效」，进而去改本来没坏的代码（`VillagerTradingManager` 只认 `SERVER_DATA_LOAD`） | 判据 5 / 6 都写「重启服务端」；汇报里写明重启方式 |
| 把 22 项摊错级别（全塞 1 级、或摊到 5 级），或为了「都能看到」自建池 | 推翻裁定 #11（1~4 级 = 6 / 6 / 6 / 4）；满级可见数的算术（`4 × 2 + 1 = 9`）也不成立 | 摊到 1~4 级；判据 5 只要求 1 级那 **2** 项 |
| 把史诗写成「4 个都卖」或「补货换一个」 | 玩家会去刷补货然后报 bug（RT-67）；4 项也超出「固定 1 项」 | 判据 3 点明池 4 取 1；判据 6 用「补货两次仍是同一物品」验 |
| 顺手给交易项加 config 键 | 176 → 178，与 D-189 冲突 | 判据 2 核 176 / 60；价格写类常量 |
| 顺手给交易界面加语言键（标题 / 说明） | 窗口内新增第二条语言键 ⇒ 玩家的 raw key 风险前移，且 F-281 算出的总数不再是最终值 | 22 项复用物品名、史诗复用作物名，交易面**没有**自有语言键；本节判据里语言键总数出现 0 次 |

### 6.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；两个新类的常量与实例数；`VillagerTradeEvents` 的分支块与 `:10-11` 的注释前后对照；农民三行的「逐字未变」证据（行号 + 原文）。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；**两次启动服务端**（改交易前 / 改交易后各一次，不用 `/reload`）；`grep -c 'new BotanistTrade('`；游戏内三次开界面（第一个植物学家 / 新的植物学家 / 5 级）。
- **观察到什么**：界面上 2 项的**物品名原文**；两个植物学家的差异项；5 级史诗的报价（物品名 + 价格）；补货两次后的同一项与价格区间。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如「练到 5 级」需要时间，写明替代做法：创造模式经验瓶或直接 `/data` 调整；仍要求重启后的读数）。
- **未关闭的问题**：例如 22 项里 20 个原版物品的清单顺序与 47 号 §2.3 的第 1 ~ 22 行是否逐行一致，本节只登记实现顺序、不改设计。

---

## 7. Phase 52 · 玩家可见文案与总数收口（M48）

### 7.1 前置

- 上一节 §6.4 的**第 1 / 4 / 5 条**已绿：`NORMAL` 22 条、分支在农民之前且农民三行未变、重启服务端后界面只 2 项。
- 基线读数（UTF-8 读法）：`datagen/ModLangProvider.java` 现 **480** 行，`:10` 是「各 **299** 键（v0.0.3 口径）」、`:14-19` 是条目构成段（`33 item + … = 338`），条目表 `buildEntries()` 在 `:70`、物品段第一条在 `:73`，`item(...)` 助手在 `:453`、`plain(...)` 在 `:466`；两份 `lang/*.json` 现各 **338** 键。
- **本节是关窗节**：F-281 一次改齐 `:10` 的 299、`:14-19` 的 338 与 `33 item` → `34 item` + `1 villager`，**并且**加两条语言键。`340` 在**各 Phase 的判据里第一次出现**，且只出现在 §7.4 的条目里；窗口内五个 Phase（47 ~ 51）的判据里它是 0 次。
- 两条语言键的写法不同：物品名用 `item(...)` 助手（前缀是 `item.farmstorm.`）；职业名**必须**用 `plain(...)` 写全路径 `entity.minecraft.villager.farmstorm.botanist`——**不能用** `entity(...)`，它的前缀是 `entity.farmstorm.`，拼不出这个键，玩家会看到 raw key。
- 冻结面：`farmstorm.configuration.*` 仍 **179**（本版不加 config 键）；`tooltip.farmstorm.*` 仍 **20**；`block.` 27 / `effect.` 4 / `subtitles.` 3 / `advancements.` 36 / `death.attack.` 6 / `gui.` 9 / `container.` 2 / `itemGroup.` 1 / `key.` 2 / `command.` 3 十一个桶一个都不动。
- 手边四份：49 号 F-281 / F-282 与 §5.6（A-86 / A-88 / A-91）；48 号 §5、§3 #31 / #32、§8 #11 / #27；47 号 §2.4 / §2.6 / §5.5 / §5.6 / 附录 A 第 2、3 行；`BRIEF` §3.3。
- 环境：中文那份**不要**用默认 `Get-Content | ConvertFrom-Json` 直读（本机默认编码会把无 BOM 的 UTF-8 读成乱码并让解析器报错）——用 `-Encoding UTF8`，或用行匹配计数。

### 7.2 本阶段目标

把语言键从 338 推到 340 并把两处总数注释一次改齐（**关窗**），同时把两段玩家可见文案落到 README。

本节结束时：`zh = 340` / `en = 340` / 双向差集 0；`ModLangProvider` 里 338 归 0；README 的「22 项 / 2 项」与「删 toml 重生成」都在。

### 7.3 任务清单（F-281 ~ F-282）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-281 | 两条语言键 + 两处计数注释（**关窗**） | M | 1.5 | F-268、F-277 | `datagen/ModLangProvider`：物品段 +1 条 `plain_chives`、新起「村民职业」段 +1 条职业名；`:10` 的 299 与 `:14-19` 的 338 改 **340**、`33 item` → `34 item` + `1 villager`；产物两份 `lang/*.json` |
| F-282 | `README.md`：已知限制 + 升级须知 | S | 0.5 | F-280、F-281 | `README.md` 新增两段：① 22 种 / 每个植物学家只随机卖 **2 种** / 要多找几个才能凑齐；② 升级须知第一条（先现象「我升级了，怎么还是 2？」再给两条解法）；既有版本段 / 玩法段不改 |

阶段小计：**2 个任务、2.0 人日**（S 1 / M 1 / L 0）。

### 7.4 出口判据

- [ ] **两份 lang 各 340 键（F-281 / 48 号 §8 #11）**：怎么验：`-Encoding UTF8` 读两份 `lang/*.json`，按行匹配 `^\s*"[^"]+"\s*:` 计数，再取键集做双向差。通过的样子：`zh = 340`、`en = 340`；`zh-only = 0`、`en-only = 0`；`item.farmstorm.plain_chives` 与 `entity.minecraft.villager.farmstorm.botanist` 两条各出现 **1** 次。⚠️ 计数只数**键**不数**行**：改前两份文件的物理行数是 340（338 键 + 2 个花括号），改后是 342——拿行数当键数会得到 342。
- [ ] **逐前缀分组（F-281 / 48 号 §5、49 号 表 D）**：怎么验：按前缀分组数。通过的样子：`farmstorm.configuration.*` = **179**；`item.farmstorm.*` = **34**；`entity.minecraft.villager.` 前缀组 = **1**（新组）；`entity.farmstorm.*` 仍 **13**、`tooltip.farmstorm.*` 仍 **20**。⚠️ 写成「`entity.` = 13」是错的——新键也以 `entity.` 开头，按首段数会得 14；必须写成 `entity.farmstorm.` 与 `entity.minecraft.villager.` 两个前缀分别数。
- [ ] **两处计数注释（F-281 / 49 号 A-88）**：怎么验：grep `ModLangProvider.java` 的三个数。通过的样子：`338` 命中 **0**；`340` 命中 **2**（`:10` 与 `:16` 各 1）；`:14-19` 的条目构成段里 `34 item` 与 `1 villager` 都在。关于 `299`：`:10` 改成 340 之后，`:24` 与 `:37` 两处**沿革段**里的 299 按 §7.5 第 1 条保留（那是历史事实，不是现行总数）——因此本节**不写**「299 命中 0」，改写成「`299` 只在沿革段（`:24` / `:37`）出现 **2** 次，`:10` 那处已不在」。
- [ ] **游戏内显示（F-281 / 48 号 §8 #27）**：怎么验：进游戏看创造栏与该植物学家的交易界面。通过的样子：显示「平凡韭菜」与「植物学家」，**不是** raw key、不是紫黑格。
- [ ] **README 的已知限制（F-282 / 47 号 §2.4）**：怎么验：读新增段。通过的样子：`22` 与 `2 项` 在同一段共现；`多找几个` 命中 ≥ **1**；换村民才有新项这层意思在位；不写成「植物学家卖 22 项」、也不写成「每个植物学家卖 22 项」。
- [ ] **README 的升级须知（F-282 / 47 号 §5.5）**：怎么验：读新增段并 grep 四个串。通过的样子：先给现象（「我升级了，怎么还是 2？」）再给解法；`run/config/farmstorm-server.toml`、`maxPerPlayer`、`删`、`重生成` 四个串都在；`每次补货重抽` 命中 **0**。
- [ ] **关窗复核（F-281 / 窗口纪律）**：怎么验：回读 §2.4 ~ §6.4 的判据。通过的样子：窗口内 **5 个 Phase（47 ~ 51）** 的判据里 `340` 出现 **0** 次；注册面 `98` 只在 §2.4 断言一次，此后只以「日志仍 98」的形式出现；本节的 340 是**最终值**不是下限（若实现期发现还需要第三条键，回头改 48 号与 47 号 §5.6，不许在代码里偷偷加）。

### 7.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 把 `ModLangProvider:24` / `:37` 两处沿革段的 `299` 也改成 `340` | 沿革被篡改（「v0.0.3 收到 340」是假历史，且 `:37` 的 `299 + 16 = 315` 算式当场崩），而判据若写「`299` 命中 0」就是在逼人改它 | 判据 3 的写法：只要求 `338` 归 0、`340` 命中 2；两处沿革段保留 299 并写进汇报 |
| 用 `entity(...)` 助手拼职业名 | 前缀变成 `entity.farmstorm.`，键永远命中不到 ⇒ 玩家看到 raw key | 判据 1 核两条键各 1 次；判据 4 进游戏看名字 |
| 只改 json 不改注释（或反过来） | 同一个类里 299 / 338 / 340 三个数打架，下一位读者按第一段核会得出「少了 41 条」的错结论（A-88） | 判据 3 一次核三个数；同一个提交 |
| 用「行数」数语言键 | 改前 340 行 / 改后 342 行，与「键数 340」撞车，读数全错 | 判据 1 明写只数键；`zh-only` / `en-only` 用键集算 |
| 把 `entity.` 当成一个桶数 | 新键也在这个前缀下 ⇒ 13 变 14，`340` 的加总算不平 | 判据 2 把 `entity.farmstorm.` 与 `entity.minecraft.villager.` 分开数 |
| README 只写「植物学家卖 22 项」 | 玩家把「我这儿只有 2 项」读成 bug（RT-66 剩下的那一半风险就是表述） | 判据 5 要求 `22` 与 `2 项` 同段共现 + `多找几个` ≥ 1 |
| 顺手加第三条语言键 | 340 从最终值变成下限，48 号与 47 号 §5.6 的口径同时失效 | 48 号 §5 纪律 3：需要第三条必须回头改上游文档，不许在代码里偷偷加 |
| 顺手改 README 的旧版本说明段 | plan 之外一行不改（D-188 范围闸） | 判据 5 / 6 只核新增段；`git diff README.md` 的 hunk 数进汇报 |

### 7.6 汇报格式

- **改了哪些文件**：`git diff --stat` 原文；F-281 的两条条目（片段 + 所在段）、`:10` / `:14-19` 的注释前后对照、两份 json 的键数；F-282 的两段新文案全文。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；一次 datagen；两次键集读数（`-Encoding UTF8`）；按前缀分组的计数命令；`grep -c '338\|340\|299'` 在 `ModLangProvider` 上的读数。
- **观察到什么**：`zh` / `en` 两个数、zh-only / en-only 两个差、`340` 的 2 处行号；游戏内「平凡韭菜」与「植物学家」两处显示；README 新增段与 grep 的四个串。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如「创造栏里是中文还是 raw key」只能在中文语言环境下看，写明切换语言的操作）。
- **未关闭的问题**：例如 `farmstorm.configuration.*` 的 179 是否含 CLIENT 的 3 键（`hud.enabled` / `hud.x` / `hud.y`）——本节按 48 号 §1.4 的口径登记为「176 server + 3 client」，不改计数。

---

## 8. Phase 53 · 全量验收与签字（M49）

### 8.1 前置

- 上一节 §7.4 的**全部 7 条**已绿：两份 lang 各 340 键、双向差集 0、`338` 归 0、README 两段在位、关窗复核通过。**窗口已于 F-281 关闭**，本节是关窗后，`98 / 3 类 / 176 / 60 / 7 / 340` 六个数都可以断言。
- F-267 ~ F-282 共 **17** 个任务已落地；`.work/v022-verify/` 可写（`acceptance.md` / `boundaries.md` / `signoff.md` 三份记录）。
- 环境：能同时起「单人客户端」与「专用服务器」；第二客户端用于「非己方」与像素对照；Mixin 口径只按 `src/` 数（`.scratch/p25a/src/neoforge.mixins.json` 是实验反编译残留，不是本仓）。
- 手边四份：49 号 F-283 / F-284 / F-285 与 §5（A-82 ~ A-91）；48 号 §7 的 **9** 条风险、§8 的 **30** 条验收、§9 的 **10** 条待验证、§10 的 13 条差异；47 号 §8 的 **27** 条边界；`BRIEF` §3.4 的 **19** 项必改。
- 本节三个任务里 **F-283 / F-284 一律不改码**（只产出记录）；验收不通过就回对应任务重做，不许就地改判据。`git status` 只允许 `.work/v022-verify/` 下的三份新文件。
- 四条必测组合**不能拆**：`#9 + #10`（老 toml vs 新 config）、`#13 + #16`（能认领 vs 靠的是那条标签）、`#17 + #18`（列表 22 vs 可见 2）、`#20 + #22`（都要**真的开界面看**）；另有两组要成对跑：`#3` 与 `#4`（两处期望面 34 / config 期望面没被改）。
- `#1` 的 Java 文件数按 **154** 记（149 + 5 个新类：`FarmstormPoiTypes` / `FarmstormVillagerProfessions` / `ModPoiTypeTagsProvider` / `BotanistTrade` / `BotanistEpicTrade`）。

### 8.2 本阶段目标

把 48 号 §8 的 30 条与 47 号 §8 的 27 条逐条跑掉，并把红线、注册面、数值面与「19 项必改」签成一份可复核的记录。

本节结束时：两份记录一条不漏；签字含八项读数与口径外 3 类的登记；上游文件一字未改。

### 8.3 任务清单（F-283 ~ F-285）

| 任务 | 标题 | 复杂度 | 人日 | 依赖 | 本阶段交付物 |
|---|---|---|---|---|---|
| F-283 | 48 号 §8 的 **30 条**验收全量执行 | L | 3.0 | F-267 ~ F-282 | `.work/v022-verify/acceptance.md`（30 条逐条读数 + 四条必测组合） |
| F-284 | 47 号 §8 的 **27 条**边界复跑 | L | 3.0 | F-283 | `.work/v022-verify/boundaries.md`（B-01 ~ B-27 分四组） |
| F-285 | 红线、注册面与「19 项必改」签字 | M | 1.5 | F-283、F-284 | `.work/v022-verify/signoff.md`（八项读数 + 口径外 3 类 + 19 项落点 + 不改清单） |

阶段小计：**3 个任务、7.5 人日**（S 0 / M 1 / L 2）。

### 8.4 出口判据

- [ ] **30 条一条不漏（F-283）**：怎么验：读 `.work/v022-verify/acceptance.md`。通过的样子：`#1` ~ `#30` 共 **30** 条，每条有动作、读数、结论与证据（不是「已验」两个字）；`#1` 的日志整行与 `#2` 的 `98 / 98` 是**原文粘贴**；`#1` 的 Java 文件数按 **154** 写；`#12` 这条 POI 读数按 48 号 §8 #12 的括注降级到 `#13`，并在记录里写明降级理由。
- [ ] **四条必测组合成对（F-283 / 48 号 §8 表下注）**：怎么验：按记录里的相邻条目核对。通过的样子：`#9 + #10`、`#13 + #16`、`#17 + #18`、`#20 + #22` 四对**相邻且都通过**；`#3 + #4` 成对（`EXPECTED["items"]` 34 与 config 期望面一字未改）；`#16`（临时去掉标签 ⇒ 永不认领）与 `#9`（老 toml 仍是 2 / 16）都按「**期望中的红**」记录，并写明理由与对应的绿条。
- [ ] **界面类的两条要有图（F-283 / RT-62）**：怎么验：读 `#20` / `#22` 两条附的证据。通过的样子：各附 **1** 张截图（tooltip、像素对齐），并写明与 0.2.1 旧图的对照结论（**1 px → 0 px**）；两处都注明「已真的开界面看」，不是读代码推的。
- [ ] **27 条分四组（F-284）**：怎么验：读 `.work/v022-verify/boundaries.md`。通过的样子：`B-01` ~ `B-27` 共 **27** 条一条不漏，按归属分四组（B-01 ~ B-11 属 R1、B-12 ~ B-14 属 R2、B-15 ~ B-18 属 R3、B-19 ~ B-27 属 R4 / R5），每组一条汇总；与 `acceptance.md` 分开记，不重跑同一条。
- [ ] **六条负向与三条算术（F-284 / 47 号 §8）**：怎么验：读记录里的六条负向条目与三条算术读数。通过的样子：B-02（补货不换内容）、B-06（有主核照样认领）、B-12（没加配方）、B-13（没建 `CropProfile`）、B-16（转换器绑不上）、B-27（工具未同步会假绿）六条全部「**没发生**」且各写明证据；B-05 = **3 项**（普通 2 + 史诗 1）、B-08 = **8 项**（`4 × 2`）、B-18 = 第 **25** 个绑不上且已绑的 24 条顺序与坐标一条不变。
- [ ] **签字八项读数（F-285 / 48 号 §1.5、§1.2、§1.3、§4.2、§5）**：怎么验：读 `.work/v022-verify/signoff.md` 的八行。通过的样子：Mixin **0**（`src/` 口径）、`src/` 下 `*.mixins.json` **0**、网络包注册调用 **2** 且协议串仍 `"2"`、注册面 **98**、`data_components` **1**、口径外 **3 类**（`ATTACHMENT_TYPES` 1 / `VillagerProfession` 1 / `PoiType` 1）、config **176 / 60**、lang **340 × 2**（这一行**引用 §7.4 的读数**，不重算）。
- [ ] **19 项必改与不改清单（F-285 / 49 号 A-82、A-86、A-87）**：怎么验：逐条比对 `BRIEF` §3.4 的 19 行与 48 号 §3 表下的 5 行「不改」。通过的样子：「19 项必改」**19 / 19** 有落点任务，其中 #17（`Farmstorm.java:176-179` 的断言）→ F-268、#19（POI 标签）→ F-276 各自可追；不改清单逐字未变、有行号与片段证据：`TierTrade` / 农民三行、`converter` 的方块 / 配方 / `CONVERTER_FUEL` / 菜单、`Farmstorm#logSelfCheck` 的作物 / 品质 / 池断言、`verify_project.py` 的 config 期望面、`OutpostCoreBlockEntity:537-570` 的补绑。上游一致性：`docs/zh/46` ~ `49`、`docs/zh/00` ~ `45`、`docs/en/*`、`src/`、`tools/` 的 mtime 未变（本版只新增 `docs/zh/50` / `51` 与这三份记录）。

### 8.5 翻车点

| 翻车点 | 症状 | 预防 |
|---|---|---|
| 只跑 `#10` 不跑 `#9` | 会把「默认值只对新装生效」读成回归，或者反过来把它当缺陷 | 判据 2：`#9` + `#10` 相邻成对，`#9` 标注「期望中的红」 |
| 用 `/reload` 判交易（`#18`） | 看到旧列表，误判「分支没生效」，去改本来没坏的代码 | 判据 2 的组合里 `#17 + #18` 一律**重启服务端**；记录里写重启时间 |
| 用老存档 toml 判 `#24` / `#25` | 必然假红，且会被当成「上限没改成 4」 | `#24` / `#25` 必须在新生成的 config 下跑；老 toml 只服务 `#9` |
| 把 B-02 写成「补货后价格变了」再判红 | 价格是闭区间随机，只有**物品**与**基准价区间**可比 | 判据 5 的负向条目只比物品与区间；不写「价格完全一样」 |
| 把 POI / 职业算进 98 | 得出 100 的错误结论，而 `verify_project.py` 仍然绿 | 判据 6 把口径外 3 类单列成一行，写明「不计入 98、工具既不核存在也不核计数」 |
| 用全仓 `*.mixins.json` 计数 | `.scratch/p25a/src/neoforge.mixins.json` 会让读数变成 **1** | Mixin 口径按 `src/`：`@Mixin` 0、`*.mixins.json` 0 |
| 签字只写「通过」不给读数 | 无法复核，等于没签 | 判据 6 / 7 要求逐项给读数与行号；「通过」两个字不算 |
| 验收不通过就改判据 / 就地改码 | 三轮验收作废，`git status` 里出现 `src/` 的改动 | F-283 / F-284 不改码；`git status` 只允许 `.work/v022-verify/` |

### 8.6 汇报格式

- **改了哪些文件**：`git status` 原文；本节只新增三份记录，`src/`、`tools/` 与 `docs/` 的 diff 必须为空。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`python -X utf8 tools/verify_project.py` 的三段原文（config 两行 / 注册面 / 结论）；四次 `grep`（`@Mixin`、`*.mixins.json`、payload 注册、`PROTOCOL_VERSION`）；`git diff --stat docs/`；两次起服（新 config / 老 toml 放回）。
- **观察到什么**：30 条逐条结论（`acceptance.md` 30 行）；27 条逐条结论分四组（`boundaries.md` 27 行）；八项读数（`signoff.md`）；两条界面截图的文件名与 0.2.1 旧图的对照结论。
- **没做的与做不了的**：一条一行写「未验证」与原因（例如双端联机的 `#13` 客户端一条需要第二台机器，写明替代读数与风险登记方式）。
- **未关闭的问题**：例如 48 号 §9 的第 ⑧ 条（未知职业 id 在 `VillagerData.CODEC` 上的解码行为）本机没有 DFU 源码可读，本节只按「卸载模组后回落成 `none`」的现有口径登记，不做硬失败方向的结论。

---

## 9. 跨阶段纪律的落点台账

| # | 纪律 | 落在哪几节的「前置 / 翻车点 / 判据」 | 违反后现场看到什么 |
|---|---|---|---|
| 1 | 中间态窗口 **F-268 → F-281**（注册面 F-268、可见面 F-269、语言面 F-281） | 前置：§2（开窗）、§3 ~ §6（窗口内，各有一句）、§7（关窗）、§8（已关，只引用）；翻车点：§2（动 `ModLangProvider`）、§3（顺手对齐注释）、§5（顺手加职业名的语言键）、§7（沿革段的 299）、§8（重算总数）；判据：§2 第 5 / 7 条、§7 第 1 ~ 3 条与第 7 条（关窗复核）、§8 第 6 条（引用） | 窗口内出现一个总数 ⇒ 之后每一节都要跟着改，必有一节算错。本版窗口横跨 **5 个 Phase**，是 0.2.1 的两倍长 |
| 2 | `Farmstorm.java:176-179` 的断言与 `plain_chives` 注册**同一提交**，中间状态必打 FAIL | 前置：§2（死命令那一条）；翻车点：§2 第 1 行；判据：§2 第 1 / 3 / 5 条成对看 | 启动日志 `→ FAIL` 而脚本仍绿 ⇒ 两套闸互相矛盾（RT-70 的第二种形态） |
| 3 | POI 必须进 `minecraft:acquirable_job_site`，否则永不认领且不报错 | 前置：§5（含代码路径 `Villager.java:181-182`）；翻车点：§5 第 1 / 3 行；判据：§5 第 4 条（产物路径 + 命名空间）、§8 第 2 条（`#16` 反向） | 村民四处走、界面无任何报错——最难定位的一类失败（RT-64） |
| 4 | 「上限 4 / 绑定 24」必须用新生成的 config；老 toml `:753` = 2、`:765` = 16 是期望中的红 | 前置：§4（含盘上读数）、§8；翻车点：§4 第 4 行、§8 第 1 / 3 行；判据：§4 第 5 / 6 条（**必须成对**）、§8 第 2 条 | 拿老 toml 判这两条必然假红；只跑 `#10` 会把「只对新装生效」读成回归 |
| 5 | 交易相关验收必须**重启服务端**（`/reload` 不重发 `VillagerTradesEvent`） | 前置：§6（含 `SERVER_DATA_LOAD` 依据）、§8；翻车点：§6 第 5 行、§8 第 2 行；判据：§6 第 5 / 6 条、§8 第 2 条 | `/reload` 之后看到旧列表是正常的，会被误判成「分支没生效」 |
| 6 | 改默认值要同改 **八处**注释；注册面 97 → 98 要同改 **八处**「97」注释——两组各 8 处，互不重叠 | 前置：§2（8 处「97」：`Farmstorm.java:62/156/157/189` + `FarmstormAttachments:13/16/18/22`）、§4（8 处默认值：`FarmstormConfig:236/238/952` + `OutpostCoreBlockEntity:116/349/333` + `OutpostCoreMenu:30` + `CultivatorLinkPayload:31-34`（改前））；翻车点：§2 第 2 行、§4 第 5 / 8 行；判据：§2 第 4 条、§4 第 7 条 | 注释与代码相反，下一位读者按注释推算得出 32 / 1024 之类的错数（RT-68）；本版两组共 **16** 行注释，是 0.2.1 的三倍工作量 |
| 7 | 三处「原子提交」不许拆（F-268 / F-274 / F-281） | 前置：§2 / §4 / §7 各一句；翻车点：三节的最后一行；判据：§2 第 4 条、§4 第 7 条、§7 第 3 条 | 拆开就留下「注册了没断言」「改了值没改注释」「加了键没改总数」三种半截状态 |

## 10. 本文件的一致性自检

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 任务号连续、无跳号、无重号 | 八张任务表合计 **19** 行；区间 `F-267 ~ F-285` ⇒ `285 − 267 + 1 = 19` | 通过 |
| 2 | Phase 与里程碑一一对应 | Phase **46 ~ 53** 共 **8** 个；里程碑 **M42 ~ M49** 共 **8** 个；每节标题带自己的里程碑号 | 通过 |
| 3 | 每节六个小节、顺序固定 | 每个 Phase 内都是「前置 / 本阶段目标 / 任务清单 / 出口判据 / 翻车点 / 汇报格式」；**6** 个小节 × **8** 个 Phase = **48** 个 Phase 内三级小标题（另有 §0 的 5 个），顺序无一处调换 | 通过 |
| 4 | 任务号 / 标题 / 复杂度 / 人日 / 依赖与 49 号一致 | 19 行逐行比对 49 号 §1 表与各任务标题行：编号 **19/19**、标题 **19/19**、S/M/L **19/19**、人日 **19/19**、依赖 **19/19**（依赖为 `—` 的共 **5** 个：F-267 / F-268 / F-270 / F-272 / F-279——49 号 §1 表逐行如此，而 49 号 §7 第 10 条写「4 个」漏了 F-279，本文按表取 5） | 通过 |
| 5 | 人日自洽 | 八节小计 `0.5 + 2.0 + 1.0 + 3.5 + 4.5 + 2.5 + 2.0 + 7.5 = 23.5`；复杂度 `S 8 / M 9 / L 2`；`23.5 × 1.25 = 29.375` | 通过 |
| 6 | 出口判据总数与分布 | 合计 **52** 条：§1 **5** / §2 **7** / §3 **6** / §4 **7** / §5 **6** / §6 **7** / §7 **7** / §8 **7**；每条以 `- [ ]` 开头，每节 ≥ **5** 条（硬底是 4） | 通过 |
| 7 | 中间态窗口写对 | 窗口只落在 §2（F-268 开）与 §7（F-281 关）：§2 只写「这条在注册表里存在」+「`items` 期望 34」+「贴图**仍 29 张**」，§3 ~ §6 只写本条自己的计数，§7 一次写 340 × 2，§8 只引用不重算；`340` 在 §2 ~ §6 **0** 次 | 通过 |
| 8 | toml 取数纪律 | `run/config/farmstorm-server.toml` 在 §4 与 §8 的前置与判据里各出现 ≥ 1 次，且都要求「新生成」；老 toml 的 `:753` / `:765` 只出现在「期望中的红」的句子里 | 通过 |
| 9 | 三处原子提交与两组八处注释 | F-268（注册 + 断言 + 8 处「97」）、F-274（2 个默认值 + 8 处注释）、F-281（2 条语言键 + 2 处计数注释）三处都写明「同一提交」与「拆开后的当场后果」；两组 8 处注释在 §9 第 6 行分开点名，互不重叠 | 通过 |
| 10 | 数字不发明 | 98 / 34 / 97 / 33 / 27 / 8 / 13 / 4 / 3 / 1 / 2 / 149 / 154 / 176 / 60 / 338 / 340 / 179 / 7 / 6 / 5 / 30 / 29 / 22 / 2 / 4 / 40 / 64 / 3-6 / 25 / 96 / 1024 / 4096 / 125 / 126 / 96 / 221 / 222 / 176 / 139 / 157 / 175 / 197 / 42 / 47 / 27 / 19 / 57 / 52 —— 每个都能追到 49 号 §1 ~ §6、48 号 §1 ~ §9、47 号 §2 ~ §8，或它们的算术（`97 + 1 = 98`、`33 + 1 = 34`、`338 + 2 = 340`、`149 + 5 = 154`、`6 × 18 + 17 = 125`、`125 + 96 = 221`、`4 × 24 = 96`、`6 − 3 + 1 = 4`、`64 − 40 + 1 = 25`、`29 + 1 = 30`、`4 × 2 = 8`、`2 + 1 = 3`、`30 + 27 = 57`）；新增的只有本文件的节号与判据编号 | 通过 |
| 11 | 禁用词、语气与改动面 | 全文无未定事项标记与占位标记；人称用「作者」「实现者」，不用第二人称敬语；结论句一律祈使；只新建本文件（`docs/zh/46` ~ `49`、`docs/zh/00` ~ `45`、`docs/en`、`src/`、`tools/`、`.work/` 一字未改） | 通过 |
| 12 | 源码行号复核 | 本文引用的全部行号（`gradle.properties:11`；`Farmstorm.java:48-63`/`:62`/`:156`/`:157`/`:176-179`/`:180-184`/`:189`；`FarmstormItems:26/111-121/127-136`；`FarmstormAttachments:13/16/18/22`；`FarmstormTags:18/24/27/30/33/36`；`OutpostCoreScreen:21-42/37-41`；`OutpostCoreMenu:30/31/44/46/80`；`OutpostCoreBlockEntity:116/333/349/393/546-547/783`；`FarmstormConfig:236/238/841/943/952/956`；`CultivatorLinkPayload:31-35`；`VillagerTradeEvents:10-11/29-36`；`ModBlockTagsProvider:24/37/42/44/62`；`FarmstormDataGen` 的 11 个 `addProvider`；`ModLangProvider:10/14-19/24/37/70/73/453/466`；`verify_project.py:334/378`；`run/config/farmstorm-server.toml:753/765`）均按 UTF-8 实读复核，与 48 / 49 号的引用逐处一致；`textures/item/` = **29** 张、Java 文件数 = **149**、`ModItemModelProvider:20/84` 的「31 个」、`FarmstormItems` 的 22 + 7 + 4 = **33** 次注册调用均实测 | 通过 |

---

> **本文的六个可复核判据**（交稿时逐条给读数）：任务 19 个 / Phase 8 个 / M42 ~ M49 八个里程碑 / 裸 23.5 人日（缓冲后 29.375）/ 出口判据 **52** 条（`5 + 7 + 6 + 7 + 6 + 7 + 7 + 7`）/ 中间态窗口 **F-268 → F-281**（跨 5 个 Phase）。
