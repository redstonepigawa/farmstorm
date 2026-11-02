# 任务书 · farmstorm v0.1.1 · Phase 26（F-176 ~ F-177）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> 本文件是完整任务书；先读完「必读」再动手。

---

## 0. 目标

完成 31 号任务书的两个任务，一件都不多做：

| 任务 | 标题 | 复杂度 | 一句话交付物 |
|---|---|---|---|
| **F-176** | 版本号 0.1.1 与四支构建脚本的 jar 名 / banner | S / 0.5 | `gradle.properties:11` 的 `mod_version` 改 `0.1.1`；四支 `.cmd` 的 banner 与写死的 jar 名同步到 0.1.1；产物变 `build/libs/farmstorm-0.1.1+mc1.21.1.jar` |
| **F-177** | `verify_project.py` 的 config 期望来源改指 v0.1.1 | M / 1.5 | 新增 `expected_config_keys_v011(...)` 解析 30 号 §4.1 的 58 组 / 166 键并入并集，同时在并集**旁边**扣掉 `REMOVED_KEYS = {"farmland.limitPerPlayer"}` |

**执行顺序**：先 F-177，后 F-176（31 号 §0.4 的排期：F-177 必须早于任何引用工具结论的任务；两者提交顺序可互换）。

## 1. 边界（可动 / 不可动）

**只允许改这 6 个文件**：
1. `gradle.properties`（仅第 11 行）
2. `检查环境.cmd`（banner `:5`、jar 名 `:14`）
3. `一键构建.cmd`（banner `:5`、jar 名 `:11` `:12`）
4. `启动客户端.cmd`（banner `:5`）
5. `启动服务器.cmd`（banner `:5`）
6. `tools/verify_project.py`（新增解析器 + 并集接入 + 文件头 docstring）

**还要做的一处产物清理**：删掉 `build/libs/farmstorm-0.1.0+mc1.21.1.jar` 与 `-sources.jar`（0.0.1 的本来就不存在）。**不要删** 0.0.2 / 0.0.3 / 0.0.4 的、**不要动** `dist/`。

**绝对不可动**：
- `docs/` 下任何文件（28~33 与 00~27 全部冻结；工具只读上游，不改上游）
- `src/` 下任何 Java、任何资源
- `run/config/farmstorm-server.toml`（**不许删、不许手改**——它是 0.1.0 的产物，F-177 的中间态判据靠它）
- `.work/v011-BRIEF.md`、`.work/v011-verdicts.md`、`.work/v011-reqs.md`、`.work/v011-delivery.md`
- `build.gradle`、`settings.gradle`、`.tools/`

## 2. 必读（按顺序）

1. `docs/zh/32-PROMPT-MAIN-0.1.1.md` —— 全文。HC / N 硬约束在这里。
2. `docs/zh/33-PROMPT-TASKS-0.1.1.md` —— §0.2（三行重申）、**§1（Phase 26 全节）**、§8（跨 Phase 注意事项）。
3. `docs/zh/31-TASKS-0.1.1.md` —— §2 里的 F-176 与 F-177 详情（约 140~175 行）、§0.4（A-40 时序）、表 A 的行 1 / 2 / 43 / 44。
4. `docs/zh/30-TECH-0.1.1.md` —— **§4.1（58 组 / 166 键的表，972~1052 行左右）**、§2.3（删键登记）、§1.1（平台与构建）。
5. `docs/zh/28-DECISIONS-0.1.1.md` —— D-124（版本号单一来源）、D-139（删 `farmland.limitPerPlayer`）、D-147（升级须知）。
6. `.work/v011-BRIEF.md`（事实卡，§1.10 版本号连带）+ `.work/v011-verdicts.md`（裁决台账，注意 A-51 / A-52）。

## 3. 环境事实（调度者已实测，不必重复确认）

| 项 | 值 |
|---|---|
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`，基线 **exit 0**（UP-TO-DATE）。**唯一构建入口，禁止直接调 gradle / gradle.bat** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（`python` **不在 PATH**，直接敲只会得到 Store 占位符报错）。用法：`$env:PYTHONIOENCODING='utf-8'; & '<上面那个路径>' -X utf8 tools/verify_project.py` |
| 工具基线 | 166 键 OK / 93 个注册 ID / lang 315 / `结论：FAIL 0 项` / exit 0 |
| toml 现状 | `run/config/farmstorm-server.toml` 在位：**59 段**（`^\s*\[` 数），含 `[farmland]` 段，`[pumpkin_turret]` 只有 3 键 |
| 四支 .cmd 现状（实测） | banner `Farmstorm v0.0.1` 出现在 `检查环境:5`、`一键构建:5`、`启动客户端:5`、`启动服务器:5`；jar 名 `farmstorm-0.0.1+mc1.21.1` 出现在 `检查环境:14`（1 处）、`一键构建:11` `:12`（2 处）。**共 5 条 banner + 3 条 jar 名 = 8 处**（文档说「七处」是漏数了 `启动客户端` 的 banner，按实测 8 处改齐） |
| 注册面 | 93（本 Phase 不许变） |

## 4. 调度者已定的口径（与文档冲突时以本节为准）

1. **文档与源码冲突 → 以源码为准**，自行适配，并把每条偏差登记进回报的「文档-源码偏差表」；**不许改 `docs/`**。
2. `.work/v011-verdicts.md` 里已裁决但未回灌的条目：**按裁决口径执行**，回报里注明「按 A-xx 执行」。与本 Phase 直接相关的：
   - **A-51**：`一键构建.cmd` 的 jar 名在 `:`**11 与 12**（不是 10-11）；`检查环境.cmd:14` 不用改行号。**实测已确认为 :11 / :12**。
   - **A-52**：数 toml 段用 `^\s*\[`（实测现状 59 段；`^\[` 只有 35 段，永远数不出 58）。
3. **禁止任何 git 写操作**（commit / add / checkout / reset / stash / restore / clean）。只允许 `git status`、`git diff`、`git diff --stat` 取证。**提交由调度者做**。
4. 客户端 GUI 类验收本机无法操作：能静态/无头取得的照做，其余标「未验证」并写明原因。
5. 遇到做不下去的：**停下并写明阻塞点 + 已做部分**，不要改判据蒙过去。

## 5. 本 Phase 的红线与三个数

- 红线只剩一条：**无 Mixin**（`@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor` 全仓 0 命中）。「0 个自定义网络包」是 0.1.0 的旧口径，**已整体废除**（D-146），不许写成通过条件。
- 三个数本 Phase **一个都不许变**：注册对象 **93**、server config **166**、语言键 **315 × 2**。

## 6. A-40 中间态（本 Phase 最硬的一条，做错就判 F-177 失败）

F-177 落地之后、F-192 收口之前，工具对 v0.1.1 **必然是红的**，红的方式有且只有「键级差异」：

- F-182 提交之前：**恰好 2 条** —— 缺 `pumpkin_turret.bulletSpeed`（源码还没有）、多 `farmland.limitPerPlayer`（源码字段还在、toml 还在，但期望集里已被剔除）
- F-182 ~ F-191：**恰好 1 条** —— 只多 `farmland.limitPerPlayer`

**出现第三个键名即判 F-177 失败**（那是解析器写错的唯一征兆）。
本 Phase 结束时：`[FAIL]` / `[STALE]` 行合计 **恰好 2 条**，退出码**非 0 属预期**。
**不许**为了让工具归零去改 `run/config/` 或 `docs/`。**不许**写一份硬编码的兜底键清单。

## 7. F-177 的实现要点（逐条对齐 31 号）

1. 新函数读 `docs/zh/30-TECH-0.1.1.md` §4.1 的 **58 行**数据表：取第 2 列「组」（嵌套组形如 `blast_carrot.nausea`）与第 4 列里**所有反引号内的键名**，拼成 `<组>.<键>`。行内 `（**改** 1 → 3）`、`（**新** 0.45）` 这类注解在反引号外，解析天然跳过，不需要额外分支。
2. 解析后自校验：**组数 = 58、键数 = 166**，不等就 `fail` 并打印实际数与**第一处不合规的表行号**（文件名 + 行号，不静默跳过）。
3. `REMOVED_KEYS = {"farmland.limitPerPlayer"}`：在并集表达式**旁边**统一扣掉，不散落到两处。
4. 期望集仍是既有结构：`(01-DESIGN + 12-TECH + 18-TECH + 24-TECH + 30-TECH) − REMOVED_KEYS`，**不新开第二份硬编码键清单**。
5. 打印要求：§1 段必须打出「30-TECH §4.1：**58** 组 / **166** 键」与「剔除已删键 1 个：farmland.limitPerPlayer」两行。
6. 文件头 docstring（`:2-12`）补一行本版来源。
7. `grep -c "expected_config_keys_v011" tools/verify_project.py` ≥ **2**（定义 + 调用）。

## 8. F-176 的实现要点

1. `gradle.properties:11`：`mod_version=0.1.0` → `0.1.1`。`minecraft_version` / `neo_version` / Java 相关行**一字不动**。
2. 四支 `.cmd` 的 banner：`Farmstorm v0.0.1` → `Farmstorm v0.1.1`（5 处：四支脚本各 1 处，`检查环境` `:5`）。
3. jar 名：`farmstorm-0.0.1+mc1.21.1` → `farmstorm-0.1.1+mc1.21.1`（`检查环境:14` 1 处；`一键构建:11` release、`:12` sources 各 1 处）。
4. 脚本其余行（`chcp 65001`、`PY` 路径、`gw.cmd build`、`pause`、`:fail` 段、`mkdir dist`）**一字不动**。
5. 跑一次 `.tools\gw.cmd build`，产出 `build/libs/farmstorm-0.1.1+mc1.21.1.jar` 与 `-sources.jar`。

## 9. 验收标准（逐条给读数，缺一条就是没做完）

**F-176**
1. `.tools\gw.cmd build` 退出码 **0**；`build/libs/farmstorm-0.1.1+mc1.21.1.jar` 与 `farmstorm-0.1.1+mc1.21.1-sources.jar` 同时在位；`build/libs` 里**没有** `farmstorm-0.1.0*` 与 `farmstorm-0.0.1*` 残留。
2. `jar tf build/libs/farmstorm-0.1.1+mc1.21.1.jar` 含 `META-INF/neoforge.mods.toml`，解开读 `version` 行是 `0.1.1`。
3. `grep -c "farmstorm-0.0.1"` 在四支脚本里都是 **0**；`grep -c "farmstorm-0.1.1+mc1.21.1"` 在 `检查环境.cmd` = **1**、在 `一键构建.cmd` = **2**；四支脚本 banner 的版本串同为 `v0.1.1`。
4. `git diff gradle.properties` **只有第 11 行一行变化**；`build.gradle` / `settings.gradle` 的 diff **为空**。

**F-177**
5. `verify_project.py` §1 段打印「30-TECH §4.1：58 组 / 166 键」与「剔除已删键 1 个：farmland.limitPerPlayer」。
6. **中间态恰好 2 条**：`[FAIL]` / `[STALE]` 行合计 = 2，键名只有 `pumpkin_turret.bulletSpeed`（缺）与 `farmland.limitPerPlayer`（多）；退出码非 0 属预期。
7. 失败路径可用：把 §4.1 的「组」列**临时**改一行（改完立刻改回，`git diff --stat docs/` 最终必须为空）后运行，退出码非 0 且报错含文件名 + 表行号；恢复后退出码回到 0。
8. `git diff --stat docs/` **为空**。
9. `grep -c "expected_config_keys_v011" tools/verify_project.py` ≥ **2**。
10. **本节没碰的对照面**：`run/config/farmstorm-server.toml` 仍在、段数（`^\s*\[`）仍 **59**、含 `[farmland]` 段、`[pumpkin_turret]` 仍 **3** 键；两份 lang 仍各 **315** 键。
11. `git status --porcelain` 里本版改动**只允许**这 6 个文件 + `build/` 产物 + 你新建的证据文件。

## 10. 必须回报的证据（正文只贴结论行，长日志进证据文件）

1. `git diff --stat` 原文。
2. 逐个任务的文件路径 + 改动行（F-176 的 8 处版本串逐处点名；F-177 新函数与 `REMOVED_KEYS` 的行号）。
3. `.tools\gw.cmd build` 的退出码 + 两支产物路径与字节数。
4. `verify_project.py` 的**§1 段完整原文** + 退出码 + `[FAIL]`/`[STALE]` 的行数与键名逐条。
5. `jar tf` 里 `neoforge.mods.toml` 的版本行。
6. 三个 `grep -c` 的读数；`git diff --stat docs/` 的空输出。
7. `run/config/farmstorm-server.toml` 的段数 / 键数 / `[pumpkin_turret]` 键数；两份 lang 的键数。
8. **文档-源码偏差表**：文档写的 vs 实测的 vs 你按哪条做的（例如 A-51 的行号、"七处" vs 8 处）。
9. **没做的与做不了的**：一节一行，写明「未验证」+ 原因。
10. **未关闭的问题**。

## 11. 证据落盘

把完整命令输出写进 `.work/TASK-V011-P26-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 12. 完成定义

上表 11 条验收**全部有读数**、证据文件已落盘、回报含 10 项。任何一条做不到就写「未验证」+ 原因，**不许写「已通过」**。
