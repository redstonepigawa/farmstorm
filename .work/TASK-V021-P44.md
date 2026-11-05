# 任务书 · farmstorm v0.2.1 · Phase 44（F-261 ~ F-263）

> 工作目录 `D:\project_farm`（Windows / pwsh / PowerShell 5.1）。**3 个任务、2.5 人日、本节是「关窗节」：三处各只关一次。**
> 上游只读：`docs/zh/40`~`docs/zh/45` **一字不改**（F-4）。

## 0. 任务（详见 43 号 §Phase 44 / 45 号 §5）

| 任务 | 标题 | 复杂度 | 关什么窗 |
|---|---|---|---|
| **F-261** | `tools/verify_project.py` 新解析器 `expected_config_keys_v021` | M / 1.5 | **工具期望面** |
| **F-262** | config 总账 **176** 键 / **60** 组 | S / 0.5 | **config 侧** |
| **F-263** | 语言键总账 **338 × 2** + `ModLangProvider` 类注释 | S / 0.5 | **语言侧** |

> 任务边界、涉及文件、验收断言**一律以 43 号 F-261~F-263 详情 + 45 号 §5 为准**；本任务书只补跨任务的硬约束与已实测的基线。

## 1. 必读

1. `docs/zh/45-PROMPT-TASKS-0.2.1.md` **§5 全节**（§5.1 前置 / §5.3 任务清单 / §5.4 出口判据 7 条 / §5.5 翻车点）/ §0.2 五条重申 / §0.4 窗口台账
2. `docs/zh/43-TASKS-0.2.1.md` **F-261~F-263 详情** + §5.5 **A-74**（窗口钉死）/ §5.6 **A-75**（解析器四条契约 + 文档与解析器同一提交）
3. `docs/zh/42-TECH-0.2.1.md` **§4.1.1**（7 列表与四条解析契约）、§4.1.2、**§4.2**（config 三条独立校验）、**§4.3**（语言侧两条纪律）、§7 #2 / #5 / #22、§9 第 4 / 11 条
4. `tools/verify_project.py` 的 `expected_config_keys_v012`（`:137-187`）与 `main()`（`:341-402`）

## 2. 已实测基线（改前读数，调度者亲自跑过；你要在改前再跑一次并记录）

| 项 | 实测值 |
|---|---|
| git | HEAD `ba9ac61`；`git describe` = `v0.2.0-11-gba9ac61`；工作树只有 `.work/` 未跟踪 |
| `config/FarmstormConfig.java` | 1136 行；`b.push(` **60**、`b.pop()` **60**、行首 `.define*` **176**（live，注释行不计） |
| `[trap]` 源码 | `trapDamageMultiplier` + `.translation(tr("trap.damageMultiplier"))` + `.defineInRange("damageMultiplier", 3.0, 1.0, 10.0)` **已在** |
| `run/config/farmstorm-server.toml` | **801 行 / 60 段 / 175 键**；`[trap]` 在 `:731`，**只有旧 3 键**（无 `damageMultiplier`）⇒ **过期，必须重生成** |
| lang（`src/generated/resources/assets/farmstorm/lang/`） | `zh_cn.json` **338** / `en_us.json` **338**；zh-only **0** / en-only **0**；`farmstorm.configuration.*` **179**；`tooltip.farmstorm.*` **20** |
| `datagen/ModLangProvider.java` | 480 行；`:14` 有「**178** config（= 175 server + 3 client）」、`:16` 有「= **337**」；`config(entries,` 次数 **179**；`:237` 是「139 个配置项」 |
| `tools/verify_project.py` | 500 行；**尚无** `expected_config_keys_v021`；**未读** `docs/zh/42-TECH-0.2.1.md`（`main():350-355` 只读 01/12/18/24/30/36 六份）；文档串 `:14` 写着「= 175 个键」 |

> **注意两处与交接文档的出入（已实测，以实测为准）**：
> ① 交接文档说 lang「两份各 337」，实测已是 **338 × 2**（F-258 加的 `trap.damageMultiplier` 翻译已进生成物）⇒ **F-263 不需要再改 json**，只改类注释。
> ② 交接文档说 `run/config`「已含 `[trap]`」，实测 `[trap]` 是**既有组**（旧 3 键），缺的只是新键 `damageMultiplier` 与总数 176。

## 3. 硬约束

1. **顺序不可换**：F-261 → F-262 → F-263。F-262 的第三个读数就是 F-261 的工具并集；关窗必须早于 F-264。
2. **A-74 窗口纪律**：本节三处关窗**各自只断言一次自己的总数**；F-261 提交里**不许**写「176 / 60」或「338 × 2」作为结论（工具打印的 176 除外，它关的是工具期望面）；F-262 只写 config 的 176 / 60；F-263 只写 lang 的 338 × 2。
3. **A-75**：解析器与 `docs/zh/42-TECH-0.2.1.md` §4.1.1 的表**绑定**；`docs/` 一字不改（§4.1.1 的那一行在基线提交 `26cefd0` 里已经在，你只需**核**它在，不许改它）。格式被改坏时按 `expected_config_keys_v012` 的形态**带文件名 + 行号抛 `ValueError`**（宁可红，不要静默少核）。
4. **四条解析契约逐条落地**（42 号 §4.1.1）：① 用 `^#### 4\.1\.1\b` 与随后的 `^#### 4\.1\.2\b` **界定范围**，**禁止全文扫** `^\|\s*\d+\s*\|`（42 号 §3 与 §7 的表第一列也是数字）；② 逐行**恰好 7 列**、第 2 列全匹配 `^`([A-Za-z0-9_]+\.[A-Za-z0-9_.]+)`$`、第 6 列以「增」/「删」开头；③ 行数断言 **== 1**；④ 结果并进 `main()` 的 `union`。
5. **`EXPECTED_COUNTS` 一行不动**（注册面仍 **97**，本版 +0；第 13 类 `ai_suspend` 也不进它）。
6. **`REMOVED_KEYS` / `gone` 不动**。
7. **禁改**：`docs/`（全部）、`build.gradle`、`.tools/`、`src/main/java/**`（F-262 只核对不改码；`ModLangProvider.java` 只动 `:13-16` 那两句注释）、`src/generated/**`、`run-*/` 旧副本。
8. **`ModLangProvider.java:237` 的「139 个配置项」逐字未变**（42 号 §9 第 11 条的既存漂移，不在本版范围）；`:10`~ `:12` 的「299 键（v0.0.3 口径）」「v0.2.0 / P39 实测」也**不动**。
9. **禁止任何 git 写操作**：只许 `git status` / `git diff` / `git log` 取证。提交由调度者做，**按任务分段**。
10. 每条判据**改前先跑一遍记下实测值**，改后再跑一次；读数只用四类：文件数与键数、日志行、`/data get`、源码 grep 计数。
11. **不许编读数**；渲染 / 听感 / 需第二客户端 → 标【未验证】+ 代码层证据。

## 4. 环境事实

| 项 | 值 |
|---|---|
| 构建 | `& 'D:\project_farm\.tools\gw.cmd' build`（**本会话里 `cmd /c ""...\gw.cmd" build"` 会被 pwsh 拆坏**；**禁止直接调 gradle**） |
| python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`，先 `$env:PYTHONIOENCODING='utf-8'`；跑工具用 `-X utf8` |
| 起服 | `$env:FS_RUN_LOCK='run-p44'; & 'D:\project_farm\.tools\gw.cmd' runServer -PfsRunDir=run-p44`（先复制 `run/eula.txt` + `run/server.properties` 到 `run-p44/`，改 rcon 端口/密码避开 25575）；**永远不要在 `run/` 里起服** |
| toml 读写 | 手工写必须 **UTF-8 无 BOM**（带 BOM 会让 NightConfig 报 `Invalid bare key` 并重建默认值）；**读行号一律 `[IO.File]::ReadAllLines($p,[Text.Encoding]::UTF8)`**（默认 `Get-Content` 会少数行） |
| 收尾退出码 | `gw.cmd runServer` 在 `/stop` 之后**可能报 exit code 1 而日志是 `BUILD SUCCESSFUL`**（P43 的 U-5，未核成因）⇒ 判成败看日志，不看这个退出码；但**必须在证据里同时写退出码与日志尾行** |
| `run/` 与 `run-*/` | 都被 `.gitignore`（`:3` / `:4`），**不进 git**；`run/config/farmstorm-server.toml` 是本机产物 |

## 5. 逐任务实现要点

### F-261 · `expected_config_keys_v021`

- 新增函数（形照 `:137-187` 的 `expected_config_keys_v012`）：
  `def expected_config_keys_v021(tech7: Path) -> tuple[list[str], list[str]]`，读 `docs/zh/42-TECH-0.2.1.md` §4.1.1，返回 `(增键, 删键)`。
- `main()` 里：加 `tech7 = root / "docs/zh/42-TECH-0.2.1.md"`、`added7, removed7 = expected_config_keys_v021(tech7)`、`before7 = union` 之类，把 `added7` 并入 `union`、`removed7` 并入 `gone`，并照 `:394-402` 的形状**多打一行**「42-TECH §4.1.1：N 行（增 a / 删 b…）」，同时把 `:397-402` 的推导串补上 42 号那一段。
- **模块文档串**（`:9-14`）跟着改：写清「v0.2.1 的 42 号 §4.1.1 加 1 个键 ⇒ 176」，**但不许**在别处把 176 / 60 写成验收结论（那是 F-262 的事）。
- 结构未变时的行为必须与今天一致：`expected_config_keys_v012` 的 11 行断言、`added3 != 19` 断言、`only_variant` 的 WARN 都**不许动**。
- **反例自检（必做）**：把 42 号**复制到 `$env:TEMP`** 造一份「§4.1.1 有 2 行」的副本，用 `--root` 指向一份临时根（或在临时目录里放最小 `docs/` 骨架）跑一次，确认**报红并带文件名 + 行号**。42 号本体 `git diff --stat` 必须为空。

### F-262 · config 总账（关窗）

- **步骤 A（重生成，必须在取数之前）**：把 `run/config/farmstorm-server.toml` 挪成 `run/config/farmstorm-server.toml.stale-<yyyymmdd>.bak`；起一次 `runServer -PfsRunDir=run-p44` 让它生成 `run-p44/config/farmstorm-server.toml`；确认服务端启动日志正常后 `/stop`；**把 `run-p44/config/farmstorm-server.toml` 字节复制到 `run/config/farmstorm-server.toml`**（`run/` 不许起服，这是让工具读到的规范路径拿到重生成结果的唯一办法），并在证据里给出两份文件的 **SHA256 相等**与 `run-*/config/` 旧副本**未被采用**的说明。
- **步骤 B（三处读数，各取一次）**：
  1. 源码：行首 `.define*` = **176**（`:935` 附近的注释行不计）；`b.push(` = `b.pop()` = **60**。
  2**规范路径 toml**：`run/config/farmstorm-server.toml` 段（`^\s*\[`）= **60**、键（`^\s*[A-Za-z0-9_]+\s*=`）= **176**、`[trap]` 段 **4** 键（含 `damageMultiplier = 3.0`，且 `damageMultiplier` 全文件命中 **1**）。
  3. 工具并集：`python -X utf8 tools/verify_project.py` 的 config 段两行都是 **176**，注册面仍 `已核 registry 对象: 97 / 97`，退出码 **0**。
- **不许改源码**；client 的 3 个 `hud.*` 键**不计入** 176（它们只计入语言键的 `farmstorm.configuration.*`）。
- 关窗记录里写明：F-258 ~ F-261 的验收里**没有**把 config / 语言键的总数写成结论（§0.3 纪律第 1 条的复核），且此后任何任务再增删键都是超范围。

### F-263 · 语言侧（关窗）

- **只改两句注释**：`ModLangProvider.java:14` 的「`**178** config（= 175 server + 3 client）`」→「`**179** config（= 176 server + 3 client）`」；`:16` 的「`= **337**`」→「`= **338**`」。其余一字不动。
- **两份 lang 只核不改**（实测已是 338 × 2）：用 `ConvertFrom-Json` 或 python 各数一次键集，给出 zh / en / zh-only / en-only / `farmstorm.configuration.*` / `tooltip.farmstorm.*` 六个数。
- 四个断言：① zh = en = **338**、zh-only = en-only = **0**；② `farmstorm.configuration.*` = **179**（= 176 server + 3 client）、`tooltip.farmstorm.*` 仍 **20**；③ `config(entries,` 次数 = **179**；④ 改后 `:13-16` 里 `337` 命中 **0**、`338` 命中 ≥ **1**；`:237` 的「139 个配置项」逐字未变。

## 6. 验收（以 45 号 §5.4 的 7 条为准，调度者按这 7 条复核）

1. `python -X utf8 tools/verify_project.py` 退出码 **0**；config 段同时打印「文档口径键数: **176**」与「生成 toml 键数: **176**」；注册面 `97 / 97`。
2. 新解析函数对 `docs/zh/42-TECH-0.2.1.md` §4.1.1 返回**恰 1 个**键 `["trap.damageMultiplier"]`（**直接调函数取返回值**，不看「工具退出 0」）；`grep -c 'trap\.damageMultiplier' docs/zh/42-TECH-0.2.1.md` ≥ **2**；§4.1.1 表行被契约正则命中 **1** 行；四条契约逐条落地。
3. **反例必须红**：TEMP 副本改成 2 行 ⇒ 报红带文件名 + 行号；`git diff --stat docs/` 为空。
4. **config 三处读数**：源码 176 / 60 / 60；规范 toml 60 段 / 176 键 / `[trap]` 4 键；工具并集 176。
5. **toml 取数纪律**：读数只来自 `run/config/farmstorm-server.toml` 且取数前起过一次 `runServer`；记录点名 `run-*/` 旧副本未被当基线、client 3 键未计入。
6. **语言键与类注释**：zh = en = 338、zh-only = en-only = 0；`farmstorm.configuration.*` = 179；tooltip 20；`config(entries,` = 179；`:13-16` 里 `337` = 0、`338` ≥ 1；`:237` 未变。
7. **关窗复核**：记录里写明窗口内没把总数写成结论，并写明此后增删键即超范围。

## 7. 必须回报的证据（写 `.work/TASK-V021-P44-evidence.md`）

按 **F-261 / F-262 / F-263 三段**分写，每段都要：

1. `git diff --stat` 原文 + 逐处改动（文件 + 行号 + 改前/改后原文）。
2. 跑过的命令原文（含 `gw.cmd build` 的退出码、`runServer` 的日志尾行与退出码、`python -X utf8 tools/verify_project.py` 的**完整输出**、两次数键集）。
3. 每条判据的**改前实测值 / 改后实测值**对照表。
4. **偏离登记**（与 43/45 号原文不一致处）+ **未验证清单**（一条一行，带原因）+ **未关闭的问题**。
5. 明确写出：本 Phase **没有**任何 git 写操作；`docs/` `git diff` 为空。
