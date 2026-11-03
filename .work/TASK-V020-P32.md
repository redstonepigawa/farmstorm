# 任务书 · farmstorm v0.2.0 · Phase 32（F-213 ~ F-215）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> 本文件是完整任务书；先读完「必读」再动手。

---

## 0. 目标（3 个任务，三个几乎不重叠的文件组）

| 任务 | 标题 | 复杂度 | 依赖 | 一句话交付物 |
|---|---|---|---|---|
| **F-213** | 版本号 0.2.0 与两支脚本的产物名 | S / 0.5 | — | `gradle.properties:11` 的 `mod_version` 改 `0.2.0`；`一键构建.cmd:11-12`（含 `-sources.jar` 那行）与 `检查环境.cmd:14` 的 jar 名同步 |
| **F-214** | 品质载体：`CropTier` + `CropProfile` 第 9 字段 + 22 条档案 | L / 3.0 | — | 新建 `util/CropTier.java`；`util/CropProfile.java` 的 record 头、22 条档案、三个私有工厂**一次改完**；`Farmstorm.java` 新增一行品质自检 |
| **F-215** | 品质视觉：名色 + tooltip 一行 + 4 条语言键 | M / 1.5 | F-214 | 三个物品类各覆写 `getName`；`appendCropTooltip` 末尾追加一行；两份 lang 各 +4 条 `tooltip.farmstorm.tier*` |

**执行顺序**：`F-213`（独立）→ `F-214` → `F-215`。
三个任务的**文件集互不重叠**（F-213 三个构建文件 / F-214 三个 Java + Farmstorm.java / F-215 三个物品类 + ModLangProvider + 两份 lang），所以做完后调度者会**按任务分三次提交**——你的回报必须按任务分段，便于切分。

## 1. 边界

**只允许改这 10 个文件**（39 号 §1.1 的清单，逐字）：
1. `gradle.properties`（仅第 11 行）
2. `一键构建.cmd`（`:11`、`:12`）
3. `检查环境.cmd`（`:14`）
4. `src/main/java/com/dashpig/farmstorm/util/CropTier.java`（**新建**）
5. `src/main/java/com/dashpig/farmstorm/util/CropProfile.java`
6. `src/main/java/com/dashpig/farmstorm/Farmstorm.java`
7. `src/main/java/com/dashpig/farmstorm/item/ModifiedCropItem.java`
8. `src/main/java/com/dashpig/farmstorm/item/DeployableCropItem.java`
9. `src/main/java/com/dashpig/farmstorm/item/SeedItem.java`
10. `src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java`
+ datagen 重新生成的 `src/generated/resources/assets/farmstorm/lang/{zh_cn,en_us}.json`

**绝对不可动**：
- `docs/` 任何文件（34~39 与 00~33 全冻结）
- `textures/item` 的 **29** 张 PNG、`models/item/*.json`、`FarmstormEntityTypes`（`clientTrackingRange(4)` / `updateInterval(10)`）
- **注册面仍 93**：`registry/` 四个文件的 `git diff` 必须为空；`menu_types` 仍 1（注册窗口 F-224 才开，绝不许提前）
- `ModLangProvider.java:13-16` 的**总数注释**一字不动（语言键窗口 F-215 → F-241，总数在 F-241 才写）
- `FarmstormConfig.java`（config 窗口 F-220 才开）
- `build.gradle`、`settings.gradle`、`.tools/`、`.work/v020-*` 与 `docs` 台账
- 8 条「种子 = 自身」的档案的 `seedItem`（见 §5 翻车点）

## 2. 必读（按顺序）

1. `docs/zh/38-PROMPT-MAIN-0.2.0.md` —— **全文**。HC-97~HC-120 / N-93~N-114 / 31 条陷阱（尤其**第 20 条元规则**：每条判据改前先跑一遍）/ §5 事实与推断的分界线 / §6 冲突处理 / §7 回报格式 / §8 开工确认清单。
2. `docs/zh/39-PROMPT-TASKS-0.2.0.md` —— §0.2 的五条重申、**§1（Phase 32 全节）**。
3. `docs/zh/37-TASKS-0.2.0.md` —— **F-213 / F-214 / F-215 详情**（约 132~167 行）、§0.4（一个任务一个提交）、§5.3 A-64、§5.4 A-65。
4. `docs/zh/34-DECISIONS-0.2.0.md` —— D-148（版本号）、**D-149 / D-150 / D-151**（品质三连）；**§4.1 的 22 条品质表**是唯一数据源。
5. `docs/zh/36-TECH-0.2.0.md` —— **§2.1**（第 9 字段位置、名色表、tooltip 行、「本文确定」1~3）、**§4.3.2 第 4~7 行**（4 条语言键）、§1.3 的基线（`tooltip.farmstorm` 16 / `textures/item` 29 / `data_components` 1）。
6. `docs/zh/35-DESIGN-0.2.0.md` —— §2.2 / §2.5。

## 3. 环境事实（调度者已实测，不必重复确认）

| 项 | 值 |
|---|---|
| 基线 | `git describe` = `v0.1.1-5`；`git diff --stat v0.1.1 -- src` **为空**；`mod_version=0.1.1` |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build` 基线 **exit 0**。**唯一入口，禁止直接调 gradle** |
| `build/libs` | 现有 `farmstorm-0.1.1+mc1.21.1.jar` 与 `-sources.jar` |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（`python` **不在 PATH**），先 `$env:PYTHONIOENCODING='utf-8'` |
| `CropProfile.java` 实测 | 三个私有工厂在 **`:191` / `:197` / `:203`**；`new CropProfile(` 全文件 **10** 处 = 档案区 7 处直接构造 + 三个工厂内部各 1 处（**两个数都要报**，别只报 10） |
| `Farmstorm.java` 自检 | `:114` 的 `自检 作物 {}`、`:122` 的 `自检 幸运浆果池 {}` 已在；品质自检行要新增在既有自检段里 |
| lang | 两份各 **323** 键；`tooltip.farmstorm` 前缀 **16** 条；跑 `runData` 重生成 |
| 起服 | `run-p32`（复制 `run/eula.txt` + `run/server.properties`：`enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），`FS_RUN_LOCK=run-p32 cmd /c ""<repo>\.tools\gw.cmd" runServer -PfsRunDir=run-p32"`。**不要在 `run/` 里起服** |
| 手工改文件 | 必须 UTF-8 **无 BOM**（本机 pwsh 5.1 的 `Set-Content -Encoding utf8` 会带 BOM） |

## 4. 调度者已定的口径（与文档冲突时以本节为准）

1. **文档与源码冲突 → 以源码 / 34 号为准**，自行适配，登记进「文档-源码偏差表」；**不许改 `docs/`**。
2. **禁止任何 git 写操作**（commit/add/checkout/reset/stash）；只许 `git status` / `git diff` / `git log` 取证。**提交由调度者做**。
3. 渲染 / 听感类验收标「未验证」+ 原因；能给的代码层证据给到文件:行号。
4. **一个任务一组文件，回报按任务分段**（调度者据此一任务一提交，37 §0.4）。
5. 拿不准的写进「未决问题」——**不许自己定新数值**，不许「先这样后面再改」。

## 5. 本 Phase 的三个窗口状态（必须记牢）

| 窗口 | 本 Phase 状态 |
|---|---|
| 注册自检 | **未开**（F-224 才开）。本节注册读数必须仍是 **93**、`menu_types` 仍 **1** |
| 语言键 | **本节开窗**（F-215）。只许写「4 条键存在」+「`tooltip.farmstorm` 16 → 20」；**不许写总数**，`ModLangProvider.java:13-16` 的注释一字不动 |
| config | **未开**（F-220 才开）。`FarmstormConfig.java` 一行不动 |

## 6. 实现要点

### F-213
`mod_version=0.1.1` → `0.2.0`；`一键构建.cmd:11`（主 jar）与 `:12`（`-sources.jar`）里的 `0.1.1` → `0.2.0`；`检查环境.cmd:14` 的 `if exist` 路径同样替换。`minecraft_version` / `neo_version` / toolchain 三行不动。**两支脚本的 banner 不写版本串，不要顺手加**。改完真跑一次构建确认新产物名。

### F-214
- `CropTier` 枚举：`COMMON` / `RARE` / `EPIC` + `translationKey()` + `color()` + `nameStyle()`（36 §2.1 的「本文确定」1~3）。
- `CropProfile` record 末尾追加 `CropTier tier`，**位置在 `throwStack` 之后**。
- 22 条档案按 **34 §4.1 逐条**补值：普通 **11** / 稀有 **7** / 史诗 **4**。
- 三个私有工厂各加一个 `CropTier` 形参并原样转发。
- `Farmstorm.java` 新增一行 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`。
- **不新增** `data_component_type`（仍 1）、不写物品 NBT、不新开注册对象。
- 7 处直接构造 + 三个工厂 + 22 条档位**同一次改完**（漏一处就是编译失败）。

### F-215
- `ModifiedCropItem`（15 个）、`DeployableCropItem`（7 个，**不继承** `ModifiedCropItem`）、`SeedItem`（7 个）**各覆写** `getName(ItemStack)` 走 `tier().nameStyle(...)`（WHITE / BLUE / GOLD）。
- `appendCropTooltip` **末尾**追加一行 `tooltip.farmstorm.tier`（外壳灰，档名子组件带自己的颜色）；**不与类别行合并**。
- 新增 **4** 条键：`tooltip.farmstorm.tier.common` / `.rare` / `.epic` + `tooltip.farmstorm.tier`（36 §4.3.2 第 4~7 行）。
- 贴图 / 模型 / 物品栏尺寸一律不动。

## 7. 验收标准（= 39 号 §1.4 的 7 条，逐条给读数）

1. **产物名与版本行（F-213）**：`.tools\gw.cmd build` 退出码 **0**；`build/libs/farmstorm-0.2.0+mc1.21.1.jar` 与 `-sources.jar` 同时在位；`build/libs` 里 `farmstorm-0.1.1*` / `farmstorm-0.1.0*` 残留 **0**；`jar tf` 读 `META-INF/neoforge.mods.toml` 的 `version` 行为 `0.2.0`。
2. **三处字面量一次改齐（A-65）**：`farmstorm-0.1.1` 在两支脚本各 **0** 命中；`farmstorm-0.2.0+mc1.21.1` 在 `一键构建.cmd` **2**、`检查环境.cmd` **1**；`git diff gradle.properties` 只有第 11 行。
3. **record 契约一次改完（F-214）**：档案区 `new CropProfile(` **7** 处（全文件 **10** = 7 + 3 个工厂内部），每处实参表末尾都有 `CropTier.`；三个工厂各多一个 `CropTier` 形参并转发；`Farmstorm.java` 的 `22 / 12 / 7 / 3 / 2` 五个数一个不动。
4. **逐档点数**：`CropTier.COMMON` **11** / `CropTier.RARE` **7** / `CropTier.EPIC` **4**（枚举声明与工厂形参不计）；日志出现 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`。
5. **名色与 tooltip（F-215）**：三档各取一个 —— **本机无法操作客户端 GUI，标「未验证」**，但必须给代码层证据：三个类各自的 `getName` 覆写（文件:行号）＋ 四色映射＋ tooltip 追加点。
6. **语言键只增 4 条**：两份 lang 各新增 **4** 条 `tooltip.farmstorm.tier*`；`tooltip.farmstorm` 前缀 **16 → 20**；**不写总数**；`ModLangProvider.java:13-16` 注释一字不动。
7. **注册面与贴图不动（负向）**：`git diff --stat textures/` 为空；`textures/item` 仍 **29** 张且逐张 sha256 与动手前相同（**动手前先存清单**）；起服日志 `自检 注册对象 93 个：… → OK`，12 类分项与基线相同（`menu_types` 仍 **1**）。

## 8. 必须回报的证据（按任务分段）

1. `git diff --stat` 原文。
2. **F-213 段**：三个文件的改动行；`build` 退出码；两支 jar 的路径与字节数；`jar tf` 的版本行；两个 `grep -c` 的读数。
3. **F-214 段**：`CropTier.java` 的枚举与四个方法签名；record 头改前→改后；22 条档案的档位逐条点名（按 34 §4.1 对照）；三个工厂的形参变更；10 处 `new CropProfile(` 的位置；逐档点数；品质自检行原文。
4. **F-215 段**：三个类的 `getName` 覆写（文件:行号）；tooltip 追加点；4 条键的键名与落点；两份 lang 的键数与 `tooltip.farmstorm` 前缀读数；`ModLangProvider:13-16` 未改的证据。
5. **负向面**：`textures/item` 29 张的 sha256 清单（动手前 / 收尾后两份对照）；`registry/` 的 diff 为空；`FarmstormConfig.java` diff 为空。
6. **文档-源码偏差表**：文档写的 vs 实测的 vs 你按哪条做的。
7. **未验证清单**（每条一行 + 为什么本机做不了）。
8. **未关闭的问题**。

## 9. 证据落盘

完整输出写进 `.work/TASK-V020-P32-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 10. 完成定义

§7 的 7 条**全部有读数或有「未验证」+ 原因**，证据已落盘，回报含 8 项且**按任务分段**。
