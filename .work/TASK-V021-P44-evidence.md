# Phase 44 证据 · farmstorm v0.2.1 · F-261 / F-262 / F-263

> 执行者：Phase 44 执行子代理（调度者：lead）。工作目录 `D:\project_farm`，Windows / pwsh 5.1。
> 顺序：**F-261 → F-262 → F-263**（未换序）。
> **本 Phase 没有任何 git 写操作**（只跑过 `git status` / `git diff` / `git log` / `git describe`）；提交由调度者按任务分段做。
> `git diff --stat docs/` **为空**（42 号一字未改，副本只落在 `$env:TEMP`）。

## 0. 环境与基线

| 项 | 值 |
|---|---|
| HEAD | `ba9ac61 docs(v0.2.1): 交接文档 —— 任务 / 已完成 Phase 40~43 / 未完成 Phase 44~45 与收尾` |
| `git describe` | `v0.2.0-11-gba9ac61` |
| python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（`$env:PYTHONIOENCODING='utf-8'`，一律 `-X utf8`） |
| 起服 lane | `run-p44`（`FS_RUN_LOCK=run-p44`、`-PfsRunDir=run-p44`、rcon **25644** / `p44rcon`）；**没有在 `run/` 里起服** |
| 手边脚本 | `.work/p44_rcon.py`（RCON 驱动）/ `.work/p44_check.py`（解析器契约自检）/ `.work/p44_neg.py`+`.work/p44_mkvariant.py`（反例副本）/ `.work/p44_lang.py`（语言键计数） |

**改前基线（本次亲自复跑，逐条落在下面的判据表里）**

| 项 | 改前实测 |
|---|---|
| `python -X utf8 tools/verify_project.py` | FAIL 0 / WARN 1，**退出码 0**；文档口径键数 **175** / 生成 toml 键数 **175**；`已核 registry 对象: 97 / 97` |
| `FarmstormConfig.java` | 1136 行；行首 `.define*` **176**；`b.push(` **60**；`b.pop()` **60**；全文 `.define` 出现行 **178** |
| `run/config/farmstorm-server.toml` | **801** 行 / **60** 段 / **175** 键；`[trap]` 在 **:731**，**只有旧 3 键**；`damageMultiplier` 命中 **0** |
| lang | `zh=338 en=338 zh_only=0 en_only=0`；`farmstorm.configuration.*` **179**；`tooltip.farmstorm.*` **20** |
| `ModLangProvider.java` | 480 行；`config(entries,` **179**；`:13-16` 里 `337`=1 / `338`=0、`178`=1 / `179`=0；`:237` = 「139 个配置项」 |
| `tools/verify_project.py` | 500 行；无 `expected_config_keys_v021`；不读 42 号；文档串 `:14` 写「= 175 个键」 |

---

## F-261 · `tools/verify_project.py` 新解析器 `expected_config_keys_v021`

### 1. `git diff --stat`（F-261 改动只有这一个文件）

```
 tools/verify_project.py | 74 +++++++++++++++++++++++++++++++++++++++++++++----
 1 file changed, 69 insertions(+), 5 deletions(-)
```

### 2. 逐处改动（文件 + 行号 + 改前 / 改后）

| # | 文件:行 | 改前 | 改后 |
|---|---|---|---|
| 1 | `tools/verify_project.py:14` | 模块串末句 `... outpost_core.farmlandBonus）= 175 个键` | 同句保留，**其后新增 2 行**：`+ docs/zh/42-TECH-0.2.1.md §4.1.1（v0.2.1 的 1 行：增 1 / 删 0，F-261）⇒ v0.2.1 加 / 1 个键，合计 **176** 个键` |
| 2 | `tools/verify_project.py:184-231`（新增） | 无 | `def expected_config_keys_v021(tech7: Path) -> tuple[list[str], list[str]]`（形照 `:137-179` 的 `expected_config_keys_v012`；docstring 用 `r"""` 以免正则里的 `\.` 触发 SyntaxWarning） |
| 3 | `tools/verify_project.py:408` | 无 | `tech7 = root / "docs/zh/42-TECH-0.2.1.md"`（**把 42 号真的接进读取面**） |
| 4 | `tools/verify_project.py:432-437` | 无 | `try: added7, removed7 = expected_config_keys_v021(tech7)` / `except (ValueError, OSError): fail("42-TECH §4.1.1 解析失败…")` |
| 5 | `tools/verify_project.py:438` | `if added4 is None or added5 is None or added6 is None:` | `... or added7 is None:` |
| 6 | `tools/verify_project.py:445-447` | `union = before6 \| set(added6)` | `before7 = before6 \| set(added6)` / `overlap7 = set(added7) & before7` / `union = before7 \| set(added7)` |
| 7 | `tools/verify_project.py:449-451` | `gone = REMOVED_KEYS \| set(removed6)` | `gone = REMOVED_KEYS \| set(removed6) \| set(removed7)`（注释同步补一句「v0.2.1 的第 3 个由 42-TECH §4.1.1 现算」） |
| 8 | `tools/verify_project.py:457-458` | 无 | 多打一行 `42-TECH §4.1.1：1 行（增 1 / 删 0；v0.2.1 的 1 行数据表现算，其中 0 个键名与前面的清单重名）` |
| 9 | `tools/verify_project.py:463-464` | 推导串 `+ 36-TECH §4.1.1 {len(added6)} 键；其中…` | 中间插入 `+ 42-TECH §4.1.1 {len(added7)} 键`，再接回「；其中…」 |

**未动**（按要求逐条核过）：`EXPECTED_COUNTS` 一行未改（注册面仍 97）；`REMOVED_KEYS` 未改；v012 的 11 行断言（旧 `:172-178`）未改；`added3 != 19` 断言未改；`only_variant` 的 WARN 分支未改。

### 3. 四条解析契约逐条落地（42 号 §4.1.1）

| 契约 | 落地方式（`verify_project.py:184-231`） | 实测 |
|---|---|---|
| ① 用 `^#### 4\.1\.1\b` 与随后的 `^#### 4\.1\.2\b` **界定范围** | `:196` `head` / `:200` `tail`，只在 `text[head.start():head.start()+tail.start()]` 内扫行 | head 行 = **642**，tail 行 = **659** |
| ② 逐行恰 **7** 列、第 2 列全匹配 `^`([A-Za-z0-9_]+\.[A-Za-z0-9_.]+)`$`、第 6 列以「增」/「删」开头 | `:210-223`：`len(cols) != 7` 抛错；`re.fullmatch` 判第 2 列；`cols[5].strip("*")` 后判「增」/「删」，两者都不是也抛错 | `COLS=7 COL2='`trap.damageMultiplier`' COL6='**增**' COL2_MATCH=True` |
| ③ 行数断言 **== 1** | `:224-230`：`len(added)+len(removed) != 1` 抛错，再断言「增 1 / 删 0」 | `ROWS=1` |
| ④ 结果并进 `main()` 的 `union` | `:445-451`（见上表 #6 / #7） | 工具打印 `文档口径键数: 176`（改前 175），`42-TECH §4.1.1：1 行（增 1 / 删 0）` |

**数据行不靠全文正则**：候选行先在本节范围内用 `^\|\s*\d+\s*\|` 认，再逐列严判；42 号 §3 / §7 的同形表**不在** `4.1.1`~`4.1.2` 区间内，未被扫到（实测本节内只有 1 行以「| 数字 |」开头）。

### 4. 判据 2：直接调函数取返回值（不看「工具退出 0」）

```
$env:PYTHONIOENCODING='utf-8'; & '<python>' -X utf8 .work/p44_check.py
RET_ADDED=["trap.damageMultiplier"]
RET_REMOVED=[]
HEAD_LINE=642
TAIL_LINE=659
ROWS=1
COLS=7 COL2='`trap.damageMultiplier`' COL6='**增**' COL2_MATCH=True
GREP_42=13          # 全文出现次数
GREP_42_LINES=12    # grep -c 口径（含该串的行数）
EXITCODE=0
```

- `grep -c 'trap\.damageMultiplier' docs/zh/42-TECH-0.2.1.md` = **12**（≥ 2 ✓）；表格数据行被契约正则命中 **1** 行 ✓。
- 改前该函数不存在（`ModuleNotFound`/无属性），改后返回**恰 1 个键** `['trap.damageMultiplier']` ✓。

### 5. 判据 3：反例必须红（`$env:TEMP` 副本，42 号本体一字未改）

副本建在 `C:\Users\DASHpig\AppData\Local\Temp\dsh-hwKjrW\p44neg_<变体>_<随机>\`（`$env:TEMP` 下），运行方式 `python -X utf8 tools/verify_project.py --root <副本根>`。

**变体 A：§4.1.1 改成 2 行**（在 :646 后插入 `| 2 | `trap.maxTrapsPerPlayer` | int | **1** | **1-8** | **增** | D-183 |`）

```
== 1. config：文档键集 vs 生成的 toml ==
  [FAIL] 42-TECH §4.1.1 解析失败，期望键集无法现算：42-TECH-0.2.1.md:642：§4.1.1 从第 642 行起只解析出2 行（增 2 / 删 0，行号 [646, 647]），文档口径是 1 行
结论：FAIL 13 项，WARN 1 项
EXITCODE=1
```

**变体 B（额外自检）：表行只留 6 列**

```
  [FAIL] 42-TECH §4.1.1 解析失败，期望键集无法现算：42-TECH-0.2.1.md:646：§4.1.1 的表行应 7 列（序号/键路径/类型/默认/值域/动作/决策号），实际 6 列：| 1 | `trap.damageMultiplier` | double | **3.0** | **1.0–10.0** | **增**
结论：FAIL 13 项，WARN 1 项
EXITCODE=1
```

两条都**带文件名 + 行号**报红，不是静默通过 ✓。副本跑出 FAIL 13 项是因为 `--root` 指向的只是 `docs/` 骨架（没有 `src/main/java`、没有 `run/`），与本判据无关。

`git diff --stat docs/zh/42-TECH-0.2.1.md` → **空**；42 号 SHA256 = `DDF5C8360868054DA4971ADE115A902188BBE83A0E1A91CFDA8C34A3FF1E3EF1`。

### 6. 判据 1 的两段读数（**中间态如实登记**）

| 时点 | 文档口径键数 | 生成 toml 键数 | 末段判词 | 退出码 |
|---|---|---|---|---|
| 改前（F-261 之前） | 175 | 175 | `[OK] 175 个配置键与文档逐字一致（toml 175 键）` | **0** |
| F-261 改后、F-262 重生成**之前** | **176** | 175 | `[STALE] run/config/farmstorm-server.toml 过期…缺 1 个键…缺 ['trap.damageMultiplier']` | **1**（= 工具**真的**开始期待新键，被旧 toml 拦下；这正是 F-261 的目的） |
| F-262 步骤 A/B 之后（终局） | **176** | **176** | `[OK] 176 个配置键与文档逐字一致（toml 176 键）` | **0** |

> 中间态那一行是**顺序的必然后果**：`run/config/farmstorm-server.toml` 的重生成属于 F-262 的步骤 A，F-261 不许抢跑。F-261 自己的断言（解析器返回 1 键 + 期望数 176）已在该行成立；「两行都是 176」是 §5.4 第 1 条的 **Phase 级**读数，落在 F-262 之后，见 F-262 节。

### 7. 偏离登记（F-261）

- **F-261-D1**：`tools/verify_project.py` 的模块文档串按任务书 §5 写了「合计 **176** 个键」（关的是**工具期望面**，A-74 / 45 号 §0.3 纪律第 1 条允许的唯一例外之一）；同一处**没有**写「60 组」，也**没有**写「338」。F-258~F-260 的窗口段读数未受影响。
- **F-261-D2**：新增函数的 docstring 用 `r"""`（原始串），因为契约正则里的 `\.` 在普通串里会触发 `SyntaxWarning: invalid escape sequence`。语义无变化，`-W error::SyntaxWarning` 下也能干净通过。
- **F-261-D3**：反例副本的**建树方式**：python `shutil.copytree` 在 `$env:TEMP` 下建子目录被拒（`PermissionError: [WinError 5]`，本机沙箱对 TEMP 的写规则），改用 pwsh `Copy-Item -Recurse` 建副本后一切正常；副本仍在 `$env:TEMP` 下，**没有**落到仓库里。

---

## F-262 · config 总账 176 键 / 60 组（**关窗**）

### 1. 步骤 A：重生成（取数之前完成，且**不在 `run/` 里起服**）

1. 建 lane：`New-Item run-p44`；复制 `run/eula.txt`（`eula=true`）与 `run/server.properties`，改 `rcon.port=25644` / `rcon.password=p44rcon`（`enable-rcon=true`、`server-port=25565` 保持）。
2. **把盘上那份过期 toml 挪走**：
   ```
   Move-Item run\config\farmstorm-server.toml -> run\config\farmstorm-server.toml.stale-20261105.bak
   （挪前 SHA256=A7BD449A516B2E5B06FAB1472CE37BF93179376D068B9C0D186D72127F1B2AB4，801 行 / 20113 字节）
   ```
   `run/config/` 里另有一份更早的 `farmstorm-server.toml.stale-20261029.bak`（15006 字节）与 `farmstorm-server-1.toml.bak`（17406 字节），**本次未覆盖、未删除**。
3. 起服（pwsh 后台作业，日志落 `.work/p44_server1.log`）：
   ```
   $env:FS_RUN_LOCK='run-p44'
   & 'D:\project_farm\.tools\gw.cmd' runServer -PfsRunDir=run-p44 *>&1 | Out-File .work\p44_server1.log -Encoding utf8
   ```
   **日志关键行（126 行全文）**：
   ```
   57: [08:18:17] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK
   93: [08:18:35] [Server thread/INFO] [minecraft/DedicatedServer]: Done (14.689s)! For help, type "help"
   96: [08:18:35] [Server thread/INFO] [minecraft/RconThread]: RCON running on 0.0.0.0:25644
   …（/stop）…
   125: BUILD SUCCESSFUL in 2m 22s
   126: 7 actionable tasks: 1 executed, 6 up-to-date
   作业退出码：GW_EXIT=0
   ```
4. `/stop` 走 RCON（`.work/p44_rcon.py` + `.work/p44_cmd_stop.txt`）：
   ```
   [auth] [(1, 2, '')]
   >>> /stop
       (10, 0, 'Stopping the server\n')
   ```
   停服尾行：`[08:20:13] ThreadedAnvilChunkStorage: All dimensions are saved` → `BUILD SUCCESSFUL in 2m 22s`；`Get-Process java` = **0** 个。
5. **字节复制**（`run/` 起服会让 P45 复核跑进旧值，这是唯一能让**规范路径**拿到重生成结果的办法）：
   ```
   Copy-Item run-p44\config\farmstorm-server.toml -> run\config\farmstorm-server.toml
   SHA256_SRC=A51513FE9B45291A97F19D547CC44D35375B75CF95755D3B087BD05EBA836206
   SHA256_DST=A51513FE9B45291A97F19D547CC44D35375B75CF95755D3B087BD05EBA836206
   SHA256_EQUAL=True
   DST_BYTES=20300 BOM=False
   ```

### 2. 步骤 B：三处读数（各取一次）

**读数①源码**（`config/FarmstormConfig.java`，`[IO.File]::ReadAllLines`）

| 项 | 改前 | 改后 |
|---|---|---|
| 行首 `.define*`（live，注释行不计） | 176 | **176** |
| `b.push(` | 60 | **60** |
| `b.pop()` | 60 | **60** |
| 全文 `.define` 出现行（含 `:935` 附近的注释行与 `:229` 的提及） | 178 | **178** |

> 源码本 Phase **一字未改**（F-258 已把 `trapDamageMultiplier` 与 `.defineInRange("damageMultiplier", 3.0, 1.0, 10.0)` 写进 `:929-931`），所以这一处的「改前 = 改后」是**正确结果**，不是漏检。全文 `.define` 178 = 176 live + 1 注释 + 1 提及，与 42 号 §4.2 的「`.define` 行总数（含注释）178」一致。

**读数②规范路径 toml**（`run/config/farmstorm-server.toml`，重生成后）

| 项 | 改前（过期） | 改后（重生成） |
|---|---|---|
| 行数 | 801 | **805** |
| 段（`^\s*\[`） | 60 | **60** |
| 键（`^\s*[A-Za-z0-9_]+\s*=`） | 175 | **176** |
| `[trap]` 段位置 | :731（旧 3 键） | **:731（4 键）** |
| `damageMultiplier` 全文件命中 | 0 | **1** |
| BOM | — | **无** |

`[trap]` 段原文（重生成后 `:731-747`，键 4 个）：

```
731: [trap]
732: 	#触发后陷阱大类免疫的刻数，100 刻 = 5 秒；范围 0 ~ 1200
735: 	immunityTicks = 100
736: 	#陷阱 dust 粒子的间隔（刻），100 刻 = 5 秒；范围 20 ~ 1200
739: 	dustIntervalTicks = 100
740: 	#每玩家陷阱名额，第 17 个挤掉最早的一个；范围 1 ~ 128
743: 	maxTrapsPerPlayer = 16
744: 	#陷阱对敌对生物的伤害倍率（1.0 = 不改；只作用于 blast_carrot 与 zangief_cane）；范围 1.0 ~ 10.0
745: 	# Default: 3.0
746: 	# Range: 1.0 ~ 10.0
747: 	damageMultiplier = 3.0
```

**读数③工具并集**（`python -X utf8 tools/verify_project.py`，重生成后）

```
  [OK]   176 个配置键与文档逐字一致（toml 176 键）
  已核 registry 对象: 97 / 97
结论：FAIL 0 项，WARN 1 项
EXITCODE=0
```

并单独的键集确认：
```
toml_keys_total= 176
has_trap_damageMultiplier= True
hud_keys= []
```

### 3. 判据 5：toml 的取数纪律

- 读数**只**来自 `run/config/farmstorm-server.toml`，且取数前刚跑过一次 `runServer`（`run-p44` lane），并**字节复制**过去（SHA256 相等已给）。
- **`run-*/config/` 的旧副本未被当基线**：本机 `run-*` 目录下共 57 份 `config/farmstorm-server.toml`，排除本次与 P43 的 lane 后是 **55** 份，键数最多 **175 / 60 段**（`run-p39old` / `run-p37` / `run-p38`），最少 **117 / 46 段**（`run-f66` 等）——**没有任何一份**等于 176，所以 176 这个读数只可能来自规范路径。
- **client 3 键未计入**：`run/config/farmstorm-server.toml` 里 `hud` 命中 **0**、`[hud]` 段 **0**；`run/config/farmstorm-client.toml` 不存在（client config 只在客户端侧生成）。那 3 个 `hud.*` 键只出现在语言键的 `farmstorm.configuration.*`（179 = 176 + 3，见 F-263）。

### 4. 偏离登记（F-262）

- **F-262-D1**：`run/config/` 里已存在一份更早的 `.stale-20261029.bak`；本次按任务书用 `yyyyMMdd=20261105` 新建 `farmstorm-server.toml.stale-20261105.bak`，**没有覆盖**旧备份（不删别人的证据）。
- **F-262-D2**：任务书/45 号 §5.1 提到旧副本「约 121 键 / 36 段」，本次实测抽样是 **117 键 / 46 段**（`run-f66`）到 **175 键 / 60 段**（`run-p39old`）。**结论方向不变**（都 < 176，都不能当基线），具体数字以本次实测为准。
- **F-262-D3**：本次 `/stop` 后 `gw.cmd runServer` 的退出码是 **0**（`GW_EXIT=0`），**没有**复现 P43 的 U-5（`/stop` 后报 exit 1 而日志是 `BUILD SUCCESSFUL`）。两条都记：退出码 0 + 日志尾行 `BUILD SUCCESSFUL in 2m 22s`。

---

## F-263 · 语言键总账 338 × 2（**关窗**）

### 1. 逐处改动（只改两句类注释，两份 lang 只核不改）

| # | 文件:行 | 改前 | 改后 |
|---|---|---|---|
| 1 | `datagen/ModLangProvider.java:14` | `… + **178** config（= 175 server + 3 client）+ 36 advancement + 6 death` | `… + **179** config（= 176 server + 3 client）+ 36 advancement + 6 death` |
| 2 | `datagen/ModLangProvider.java:16` | ` * = **337**。（P29 时是 314：同表但 config 166 / gui 8、无 key / command 两组。）` | ` * = **338**。（同上括号内容逐字未变）` |

`git diff` 原文（`-U1`）：

```
diff --git a/src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java b/src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java
index a0cd580..b04c481 100644
--- a/src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java
+++ b/src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java
@@ -13,5 +13,5 @@ import net.neoforged.neoforge.common.data.LanguageProvider;
  * 不存在「两份键各自维护」的可能。**条目构成（v0.2.0 / P39 实测，改表时必须同步本段）**：
- * 33 item + 27 block + 4 effect + 3 subtitles + **178** config（= 175 server + 3 client）+ 36 advancement + 6 death
+ * 33 item + 27 block + 4 effect + 3 subtitles + **179** config（= 176 server + 3 client）+ 36 advancement + 6 death
  * + 20 tooltip + 13 entity + 9 gui + 2 container + 1 itemGroup + 0 message + 2 key + 3 command
- * = **337**。（P29 时是 314：同表但 config 166 / gui 8、无 key / command 两组。）
+ * = **338**。（P29 时是 314：同表但 config 166 / gui 8、无 key / command 两组。）
  *
```

**未动**：两份 `lang/*.json`（`git status` 里没有它们）、`:10` 的「各 **299** 键（v0.0.3 口径）」、`:13` 的「v0.2.0 / P39 实测」、`:16` 括号内的 P29 说明、`:237` 的「139 个配置项」。

### 2. 六个语言读数（`python -X utf8 .work/p44_lang.py`，改前 / 改后各一次）

| 项 | 改前 | 改后 | 断言 |
|---|---|---|---|
| `zh_cn.json` 键数 | 338 | **338** | = 338 ✓ |
| `en_us.json` 键数 | 338 | **338** | = 338 ✓ |
| zh-only | 0 | **0** | = 0 ✓ |
| en-only | 0 | **0** | = 0 ✓ |
| `farmstorm.configuration.*` | 179 | **179** | = 176 server + 3 client ✓ |
| `tooltip.farmstorm.*` | 20 | **20** | 仍 20 ✓ |
| `ModLangProvider` 的 `config(entries,` 次数 | 179 | **179** | = 179 ✓ |
| `:13-16` 里 `337` / `338` 命中 | 1 / 0 | **0 / 1** | 337=0、338≥1 ✓ |
| `:13-16` 里 `178` / `179` 命中 | 1 / 0 | **0 / 1** | 同步 ✓ |
| `:237` 「139 个配置项」 | 存在 | **存在（逐字未变）** | ✓ |

前缀分桶（改后，相加 = 338）：`advancements.farmstorm 36 / block.farmstorm 27 / command.farmstorm 3 / container.farmstorm 2 / death.attack 6 / effect.farmstorm 4 / entity.farmstorm 13 / farmstorm.configuration 179 / gui.farmstorm 9 / item.farmstorm 33 / itemGroup.farmstorm 1 / key.farmstorm 2 / subtitles.farmstorm 3 / tooltip.farmstorm 20`。

### 3. 偏离登记（F-263）

- **F-263-D1**：43 号 F-263 断言 3 写「`config(entries,` 次数 = **179**（改前 178）」，实测**改前就已经是 179** —— F-258 把 `config(entries, "trap.damageMultiplier", …)` 那一行加进去了。本 Phase **没有**改动任何一条 `config(entries,` 行，只核。断言「= 179」成立。
- **F-263-D2**：两份 `lang/*.json` 实测**改前已是 338 × 2**（交接文档写 337 × 2），与任务书 §2 的实测基线一致 ⇒ 本任务**只改注释，不碰 json**。

---

## 判据表（45 号 §5.4 的 7 条）

| # | 判据 | 实测值 | 通过 |
|---|---|---|---|
| 1 | 工具退出码 0；config 段同时打印「文档口径键数: 176」与「生成 toml 键数: 176」；注册面 `97 / 97` | `文档口径键数: 176` / `生成 toml 键数: 176` / `[OK] 176 个配置键与文档逐字一致（toml 176 键）` / `已核 registry 对象: 97 / 97` / `结论：FAIL 0 项，WARN 1 项` / `VERIFY_EXIT=0` | ✅ |
| 2 | 新解析函数返回**恰 1 个**键；`grep -c 'trap\.damageMultiplier'` ≥ 2；表行被契约正则命中 **1** 行；四条契约逐条落地 | `RET_ADDED=["trap.damageMultiplier"]` / `RET_REMOVED=[]`；grep = **12**；`ROWS=1 COLS=7 COL2_MATCH=True COL6='**增**'`；range = 642→659；并进 union（`:445-451`） | ✅ |
| 3 | 反例必须红（带文件名 + 行号）；`git diff --stat docs/` 为空 | `42-TECH-0.2.1.md:642：…只解析出2 行（…行号 [646, 647]），文档口径是 1 行`，EXITCODE=1；另有 6 列变体 `42-TECH-0.2.1.md:646：…实际 6 列`，EXITCODE=1；`git diff --stat docs/` 空 | ✅ |
| 4 | config 三处读数：源码 176 / 60 / 60；规范 toml 60 段 / 176 键 / `[trap]` 4 键；工具并集 176 | `.define*=176`、`push=60`、`pop=60`；toml `805 行 / 60 段 / 176 键`，`[trap]` `:731-747` 4 键、`damageMultiplier = 3.0` 且全文件命中 1；工具 `176 / 176` | ✅ |
| 5 | toml 取数纪律：只认 `run/config/`、取数前起过 `runServer`；点名 `run-*/` 旧副本未被当基线、client 3 键未计入 | 起服 `run-p44` → `Done (14.689s)` → `/stop` → 字节复制（SHA256 相等）；55 份旧副本最多 175 键/60 段，无一等于 176；server toml 里 `hud` 命中 0、`[hud]` 段 0 | ✅ |
| 6 | zh = en = 338、zh-only = en-only = 0；`farmstorm.configuration.*` = 179；tooltip 20；`config(entries,` = 179；`:13-16` 里 `337`=0 / `338`≥1；`:237` 未变 | 338 / 338 / 0 / 0；179；20；179；`SEG_13_16_337=0 SEG_13_16_338=1`；`:237` = `// ---- 139 个配置项…`（`L237_139=1`，diff 里无该行） | ✅ |
| 7 | 关窗复核：记录里写明窗口内没把总数写成结论，并写明此后增删键即超范围 | 见下「关窗复核」 | ✅ |

## 关窗复核（§5.4 第 7 条 / A-74）

- **窗口内（F-258 ~ F-260）的验收没有把 config / 语言键的总数写成结论**：那三个任务的记录在 `.work/TASK-V021-P43-evidence.md`，本 Phase **未改动**该文件；其内写的是「`[trap]` 段内 4 键 / 本条键存在 / 区间读数」。
- **本 Phase 三个总数各自只在自己的关窗任务里断言一次**：
  - 工具期望面 → **F-261**（工具打印的 `176`，以及解析器返回 1 键）；
  - config 侧 → **F-262**（`176 键 / 60 组`，三处读数各一次）；
  - 语言侧 → **F-263**（`338 × 2`，六个数各一次）。
- **此后任何任务再增删 config 键或语言键都是超范围**：窗口已在 F-263 关闭；F-264 ~ F-266 只能**复跑** `#2` / `#4` / `#5` 的读数，不得改键。
- F-261 的模块文档串里出现「合计 **176** 个键」，属任务书 §5 明确要求的「工具期望面」读数（A-74 允许的例外），**不是** config 总账的结论；同一处没有写「60 组」、没有写「338」。

## 未验证清单

| # | 项 | 原因 |
|---|---|---|
| U-1 | 游戏内/多客户端行为读数 | 本 Phase 三处关窗都是**静态读数**（源码计数 / toml 计数 / 工具并集 / 语言键集），不需要第二客户端；任何渲染或听感结论本 Phase 未做，也不在 §5.4 的 7 条里。 |
| U-2 | `gw.cmd runServer` 收尾 exit code 的**成因** | 本次是 `GW_EXIT=0`，未复现 P43 的 U-5（`/stop` 后 exit 1 而日志 `BUILD SUCCESSFUL`），因此没有可核的对象；成因仍开放。 |
| U-3 | 语言键总数的**自动闸** | `verify_project.py` 第 3 节只比 zh / en **键集相等**，不校验 338（42 号 §9 第 4 条）⇒ 338 这个数只能人工数，工具不覆盖。 |
| U-4 | 42 号 §4.2 的「`.define` 行总数（含注释）178」 | 本次用「全文 `.define` 出现行数」核到 178，一致；但该数**不在**任何一条验收判据里，口径（行首 vs 全文）也没被文档钉死 ⇒ 只作旁证，不作判据。 |

## 未关闭的问题

1. **`run/config/farmstorm-server.toml` 现在是「字节复制的产物」**：`run/` 不许起服，所以它只能由 `run-p44/config/` 复制而来。若 P45 的 F-264 需要重跑工具读数，**不要**先起服改写 `run-p44/config/` 再读 `run/config/`；要么重走一次「runServer + 字节复制」，要么直接信任现有副本（SHA256 `A51513FE…`）。
2. **`run/config/` 里现有 3 份 .bak**（`farmstorm-server-1.toml.bak`、`stale-20261029.bak`、`stale-20261105.bak`）。是否在 P45 收尾时清理，未定；本 Phase 不删别人的证据文件。
3. **`.define` 计数的口径**：42 号 §4.2 表里既有「行首 `.define*` = 176」又有「`.define` 行总数（含注释）178」，两者靠「行首 / 全文」区分。若 P45 想把 178 变成判据，需要另行钉死正则（本 Phase 只按行首 176 判）。
4. **`ModLangProvider.java:10`（「各 **299** 键」）与 `:237`（「139 个配置项」）仍是既存漂移**（42 号 §9 第 11 条）。本版不动；P45 签字时不应把它算作本 Phase 的漏改。

## 构建与收尾

```
& 'D:\project_farm\.tools\gw.cmd' build
BUILD_EXIT=0
…
> Task :build
BUILD SUCCESSFUL in 10s
5 actionable tasks: 2 executed, 3 up-to-date
```

## 最终工作树状态（取证，无写操作）

```
git status --porcelain=v1（排除未跟踪的 .work/）
 M src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java
 M tools/verify_project.py

git diff --stat
 .../dashpig/farmstorm/datagen/ModLangProvider.java |  4 +-
 tools/verify_project.py                            | 74 ++++++++++++++++++++--
 2 files changed, 71 insertions(+), 7 deletions(-)

git diff --stat docs/
（空）

git log --oneline -1
ba9ac61 docs(v0.2.1): 交接文档 —— 任务 / 已完成 Phase 40~43 / 未完成 Phase 44~45 与收尾
```

- 只改了 **2 个文件**：`tools/verify_project.py`（F-261）与 `ModLangProvider.java`（F-263，只 2 行注释）。F-262 **只做核对与记录，零源码改动**。
- `run/`、`run-*/` 被 `.gitignore` 的 `:3` / `:4` 忽略，`run-p44` 不进 git（`git status` 里 0 命中）。
- **本 Phase 无任何 git 写操作**（未 commit / tag / push / stash / checkout / add）。
- 完整工具输出原文：`.work/p44_verify_final.txt`；起服日志原文：`.work/p44_server1.log`；构建日志：`.work/p44_build1.log`。
