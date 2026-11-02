# Phase 26 证据 · F-176 / F-177（farmstorm v0.1.1）

- 执行者：Phase 26 唯一执行者（子代理）
- 日期：2026-11-02（本机时钟）/ 作业开始 2026-11-02 22:xx，结束 23:41
- 工作目录：`D:\project_farm`（Windows / pwsh）
- 任务书：`.work/TASK-V011-P26.md`（138 行，已全文读完）
- 必读已读：32 号（HC/N + §T 表）、33 号 §0.2 / §1、31 号 §0.4 / F-176 / F-177 / 表 A 行 1·2·43·44、30 号 §4.1（:966-1052）、28 号 D-124 / D-139 / D-147、`.work/v011-BRIEF.md` §1.10、`.work/v011-verdicts.md`（A-40 / A-51 / A-52 / A-56）
- 执行顺序：**先 F-177，后 F-176**（照任务书 §0）

---

## 0. 基线（改动前实测）

```text
python -X utf8 tools/verify_project.py   -> 结论：FAIL 0 项，WARN 0 项   EXITCODE=0
  文档口径键数: 166 / 生成 toml 键数: 166 / [OK] 166 个配置键
  已核 registry 对象: 93 / 93 ; lang 键集一致，共 315 键 ; 无 Mixin / TODO / 调试残留 / 空 catch
run/config/farmstorm-server.toml：764 行（含行尾），^\s*\[ = 59 段，^\[ = 35 段，含 [farmland]
[pumpkin_turret] 段在 :159，实际 4 键
build/libs：0.0.2 / 0.0.3 / 0.0.4 / 0.1.0 各 jar + sources（共 8 个）
dist/：0.0.1 / 0.0.2 / 0.0.3 / 0.0.4 / 0.1.0 jar + install-notes.txt + 安装说明.txt（本 Phase 不动）
git status：README.md 已是 M（进入本节前就脏，非本 Phase 所改）
```

---

## 1. F-177 · `tools/verify_project.py` 的 config 期望来源改指 v0.1.1

### 1.1 改动位置（改后行号，共 3 处）

| # | 位置 | 内容 |
|---|---|---|
| 1 | `:2`、`:5-13`（文件头 docstring，原 `:2-12`） | 标题 `Farmstorm v0.1.0` → `v0.1.1`；新增来源行「+ docs/zh/30-TECH-0.1.1.md §4.1（v0.1.1 的 58 组 / 166 键，F-177）」；把「按集合去重后是 166 个键」改为「去重后 167，再扣掉本版删掉的 1 个键（REMOVED_KEYS，D-139）是 166 个键」 |
| 2 | `:136` | `REMOVED_KEYS = {"farmland.limitPerPlayer"}`（+ 2 行注释，:134-135） |
| 3 | `:139-190` | 新函数 `def expected_config_keys_v011(tech5: Path) -> tuple[list[str], int]`（52 行），`:288` 取 `tech5 = root / "docs/zh/30-TECH-0.1.1.md"`，`:303` 调用，`:315` 并集旁扣键 `exp = union - REMOVED_KEYS`，`:316`/`:318` 两条打印 |

- `REMOVED_KEYS` 全文件出现 6 次：`:136` 定义、`:315` 唯一一次扣减、`:318`/`:324` 仅在打印算式里引用、`:12` 在 docstring 里提到。**扣减点只有一处**（任务书 §7.3）。
- `grep -c "expected_config_keys_v011"` = **2**（`:139` 定义 + `:303` 调用）≥ 2 ✔
- 解析器不做兜底键清单：解析失败只 `fail(带文件名+表行号)`，`exp_ok=False`，跳过 toml 比对（`:330` 附近），**没有**任何硬编码回退集。

### 1.2 `git diff -- tools/verify_project.py`（原文，节选关键段已全文落盘于本节）

```diff
@@ -1,13 +1,15 @@
-r"""Farmstorm v0.1.0 静态验收器。
+r"""Farmstorm v0.1.1 静态验收器。
                    + docs/zh/24-TECH-0.1.0.md §8.1（v0.1.0 的 12 行，F-142），并与
-                   run/config/farmstorm-server.toml 逐键比对。两份 TECH 各含「改既有默认值」的行
-                   （v0.0.3 是 fang_trap_cactus 的三个键、v0.1.0 是 melon_grenade.horizontalJitter），
-                   键名与前面的清单重名，按集合去重后是 166 个键
+                   + docs/zh/30-TECH-0.1.1.md §4.1（v0.1.1 的 58 组 / 166 键，F-177），并与
+                   run/config/farmstorm-server.toml 逐键比对。18 / 24 / 30 号各含「改既有默认值」的行
+                   （v0.0.3 是 fang_trap_cactus 的三个键、v0.1.0 是 melon_grenade.horizontalJitter、
+                   v0.1.1 是 pumpkin_turret 的三行），键名与前面的清单重名，按集合去重后是
+                   167 个键，再扣掉本版删掉的 1 个键（`REMOVED_KEYS`，D-139）是 166 个键
@@ -129,6 +131,66 @@
+# 本版从期望集里剔除的键（30 号 §2.3 的删键登记 / D-139：整组删 farmland）。
+# 只许在 main() 的并集表达式旁边扣一次——散成两处，下一个删键的人就会漏扣一处。
+REMOVED_KEYS = {"farmland.limitPerPlayer"}
+
+def expected_config_keys_v011(tech5: Path) -> tuple[list[str], int]:
+    （解析 §4.1 的 5 列表：组取第 2 列、键取第 4 列反引号内键名，拼 <组>.<键>；
+     行级校验列数 / 组名格式 / 「键数」列与解析结果一致；聚合校验 58 组 / 166 键 / 无重复键；
+     任一不合规 raise ValueError(f"{文件名}:{表行号}：…")）
@@ -236,18 +299,29 @@
+    try:
+        added5, groups5 = expected_config_keys_v011(tech5)
+    except (ValueError, OSError) as exc:
+        fail(f"30-TECH §4.1 解析失败，期望键集无法现算：{exc}")
+        added5 = None
+    if added4 is None or added5 is None:
+        ...
+        union = set(base) | set(added) | set(added3) | set(added4) | set(added5)
+        # 已删键在并集旁边扣一次（REMOVED_KEYS 的唯一用处），不散落到解析函数里
+        exp_ok, exp = True, union - REMOVED_KEYS
+        print(f"  30-TECH §4.1：{groups5} 组 / {len(added5)} 键 …")
+        print(f"  剔除已删键 {len(REMOVED_KEYS)} 个：{'、'.join(sorted(REMOVED_KEYS))}")
```

期望集结构未变：`(01-DESIGN §3.8 + 12-TECH §8.1 + 18-TECH §8.1 + 24-TECH §8.1 + 30-TECH §4.1) − REMOVED_KEYS`，**没有新开第二份硬编码键清单**。

### 1.3 运行结果（`python -X utf8 tools/verify_project.py`，全文）

```text
== 1. config：文档键集 vs 生成的 toml ==
  30-TECH §4.1：58 组 / 166 键（v0.1.1 的 58 行数据表现算，其中 165 个键名已在前四份清单里）
  剔除已删键 1 个：farmland.limitPerPlayer
  文档口径键数: 166（01-DESIGN §3.8 117 + 12-TECH §8.1 22 + 18-TECH §8.1 19 行 + 24-TECH §8.1 12 行（新 11 + 改 1） + 30-TECH §4.1 166 键；其中 24 / 18 号另有 3 行是改既有默认值、§4.1 另有 165 个键名与前面的清单重名；按并集去重后 167，再扣掉 1 个已删键 = 166）
  生成 toml 键数: 166
  [FAIL] toml 缺 1 个键且源码里也没有（文档/代码不一致 MISMATCH）: ['pumpkin_turret.bulletSpeed']
  [FAIL] toml 多 1 个键且源码里存在（文档漏记 MISMATCH）: ['farmland.limitPerPlayer']
== 2. 93 个注册 ID 逐字核对（全源码树） ==
  [OK]   items: 32 个 ID 齐全
  [OK]   blocks: 26 个 ID 齐全
  [OK]   block_entity_type: 7 个 ID 齐全
  [OK]   entity_type: 13 个 ID 齐全
  [OK]   mob_effect: 4 个 ID 齐全
  [OK]   sound_event: 3 个 ID 齐全
  [OK]   recipe_type: 1 个 ID 齐全
  [OK]   recipe_serializer: 1 个 ID 齐全
  [OK]   creative_mode_tab: 1 个 ID 齐全
  [OK]   damage_type: 3 个 ID 齐全
  [OK]   data_component_type: 1 个 ID 齐全
  [OK]   menu_type: 1 个 ID 齐全
  已核 registry 对象: 93 / 93
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 315 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch

结论：FAIL 2 项，WARN 0 项
EXITCODE=1
```

### 1.4 中间态：恰好 2 条（A-40）

| # | 行 | 键名 | 方向 |
|---|---|---|---|
| 1 | `[FAIL]` | `pumpkin_turret.bulletSpeed` | 缺（源码还没有，F-182 才加） |
| 2 | `[FAIL]` | `farmland.limitPerPlayer` | 多（期望集已剔除，toml 里还在） |

- `[FAIL]` 2 条 + `[STALE]` 0 条 = **2** ✔；**第三个键名不存在** ✔
- 没有 `[STALE]`：`stale_keys` 两条都判不进（缺的那条源码里没有、多的那条源码里有），说明「过期」分支没被误触发。
- 退出码 **1**（非 0，属预期）。**未**为了让工具归零去动 `run/config/` 或 `docs/`。

### 1.5 失败路径（验收 §9.7）

做法：`docs/zh/30-TECH-0.1.1.md` 先 `Copy-Item` 到 `.work/p26-doc-backup.md`（字节备份），
用 `[IO.File]::ReadAllText/WriteAllText`（UTF-8 无 BOM，只替换单行子串，不动行尾）改一行，
跑工具、记录，然后 `Copy-Item` 覆盖回来并删除备份；前后对 `docs/` 全部 40 个 .md 做 SHA256 快照比对。

**测试 A（把「组」列 `converter` → `Converter`）**

```text
    [FAIL] 30-TECH §4.1 解析失败，期望键集无法现算：30-TECH-0.1.1.md:974：§4.1 的「组」列应是单个反引号内的小写路径（形如 `blast_carrot.nausea`），实际是 '`Converter`'
    生成 toml 键数: 跳过（期望集没算出来，见上面的 [FAIL]）
  结论：FAIL 1 项，WARN 0 项
  EXITCODE_A=1
```
→ 含文件名 `30-TECH-0.1.1.md` + 表行号 `974` ✔

**测试 B（把「组」列 `power_beet.strength` → `power_beet.speed`，语法合法但与第 44 行撞组）**

```text
    [FAIL] 30-TECH §4.1 解析失败，期望键集无法现算：30-TECH-0.1.1.md:966：§4.1 解析出 2 个重复键名 ['power_beet.speed.durationTicks', 'power_beet.speed.level']（相关行 ['第 1016 行（power_beet.speed）', '第 1017 行（power_beet.speed）']），组 + 键两列拼出的键集必须两两不同
  结论：FAIL 1 项，WARN 0 项
  EXITCODE_B=1
```
→ 含文件名 + 表行号（966 是 §4.1 表头行、1016/1017 是具体数据行）✔

**恢复证明**

```text
docs/zh/30-TECH-0.1.1.md  SHA256 改动前 = 8BBD603ABD49CD6B0EBD6BC58B787CFA72B93437695D4A0DF38E4BE9DDF0F2A1（145110 B）
docs/zh/30-TECH-0.1.1.md  SHA256 恢复后 = 8BBD603ABD49CD6B0EBD6BC58B787CFA72B93437695D4A0DF38E4BE9DDF0F2A1
restored_identical = True
docs/ 全目录（40 个 .md）SHA256 前后 Compare-Object = 空（逐文件字节一致）
恢复后：EXITCODE=1，[FAIL]/[STALE] 行 = 2（即本 Phase 规定的中间态，不是 0）
```

> **口径说明（见 §5 偏差 D-5）**：31 号 F-177 验收第 4 条 / 33 号 §1.4 写「恢复后退出码回到 **0**」。
> 本 Phase 的 §6 又规定结束时「`[FAIL]`/`[STALE]` 恰好 2 条、退出码非 0 属预期」。两者不可能同时成立
> ——恢复后的稳态就是 2 条红 / exit 1。我按 §6（任务书 §6 / §9.6，最硬的一条）执行：
> 恢复后**报错消失、回到本 Phase 规定的状态**（2 条、exit 1），并把它登记为文档内部矛盾。

---

## 2. F-176 · 版本号 0.1.1 与四支构建脚本的 jar 名 / banner

### 2.1 `gradle.properties`（只改第 11 行）

```diff
diff --git a/gradle.properties b/gradle.properties
index dffe707..1bd66ae 100644
--- a/gradle.properties
+++ b/gradle.properties
@@ -8,7 +8,7 @@ loader_version_range=[4,)
 mod_id=farmstorm
 mod_name=Farmstorm
 mod_license=All Rights Reserved
-mod_version=0.1.0
+mod_version=0.1.1
 mod_group_id=com.dashpig.farmstorm
 mod_authors=dashpig
 mod_description=Vanilla crops turned into PvP gadgets.
```

`git diff --stat -- build.gradle settings.gradle` = **（空）** ✔
`minecraft_version` / `neo_version` / `neo_version_range` / `loader_version_range` / Java 相关 5 行**一字未动**。

### 2.2 四支 `.cmd`：**7 处**（4 条 banner + 3 条 jar 名），逐处点名

| # | 文件:行 | 改前 | 改后 |
|---|---|---|---|
| 1 | `检查环境.cmd:5` | `echo   Farmstorm v0.0.1  -  验收自检` | `echo   Farmstorm v0.1.1  -  验收自检` |
| 2 | `检查环境.cmd:14` | `if exist "build\libs\farmstorm-0.0.1+mc1.21.1.jar" …` | `… farmstorm-0.1.1+mc1.21.1.jar …` |
| 3 | `一键构建.cmd:5` | `echo   Farmstorm v0.0.1  -  一键构建` | `echo   Farmstorm v0.1.1  -  一键构建` |
| 4 | `一键构建.cmd:11` | `copy /y "build\libs\farmstorm-0.0.1+mc1.21.1.jar" "dist\" >nul` | `… farmstorm-0.1.1+mc1.21.1.jar …`（release） |
| 5 | `一键构建.cmd:12` | `copy /y "build\libs\farmstorm-0.0.1+mc1.21.1-sources.jar" "dist\" >nul` | `… farmstorm-0.1.1+mc1.21.1-sources.jar …`（sources） |
| 6 | `启动客户端.cmd:5` | `echo   Farmstorm v0.0.1  -  启动开发客户端` | `echo   Farmstorm v0.1.1  -  启动开发客户端` |
| 7 | `启动服务器.cmd:5` | `echo   Farmstorm v0.0.1  -  启动专用服务器` | `echo   Farmstorm v0.1.1  -  启动专用服务器` |

- 改法：`[IO.File]::ReadAllText` → `.Replace()` → `WriteAllText(UTF8Encoding($false))`，**只替换单行子串**，行尾 CRLF 与「无 BOM」保持原状（改前/改后字节数完全一致：606 / 584 / 557 / 383；非 ASCII 字节数一致：108 / 78 / 228 / 96）。
- 每处替换前都断言命中数（1 / 1 / 1 / 2 / 1 / 1），没有盲替换。
- 脚本其余行（`chcp 65001`、`PY` 路径、`gw.cmd build`、`pause`、`:fail` 段、`mkdir dist`）**一字未动**。

### 2.3 构建（唯一入口 `.tools\gw.cmd`）

```text
cmd /c ""D:\project_farm\.tools\gw.cmd" build"
> Task :createMinecraftArtifacts UP-TO-DATE
> Task :compileJava UP-TO-DATE
> Task :processResources
> Task :classes
> Task :jarJar NO-SOURCE
> Task :jar
> Task :sourcesJar
> Task :assemble
> Task :compileTestJava NO-SOURCE
> Task :processTestResources NO-SOURCE
> Task :testClasses UP-TO-DATE
> Task :test NO-SOURCE
> Task :check UP-TO-DATE
> Task :build
BUILD SUCCESSFUL in 9s
5 actionable tasks: 3 executed, 2 up-to-date
BUILD_EXITCODE=0
```

### 2.4 产物

```text
build/libs/
  farmstorm-0.0.2+mc1.21.1-sources.jar        738064
  farmstorm-0.0.2+mc1.21.1.jar                814935
  farmstorm-0.0.3+mc1.21.1-sources.jar       2027076
  farmstorm-0.0.3+mc1.21.1.jar               2104420
  farmstorm-0.0.4+mc1.21.1-sources.jar        884603
  farmstorm-0.0.4+mc1.21.1.jar                959376
  farmstorm-0.1.1+mc1.21.1-sources.jar        939520
  farmstorm-0.1.1+mc1.21.1.jar               1016609
```
- 已删：`build/libs/farmstorm-0.1.0+mc1.21.1.jar`、`build/libs/farmstorm-0.1.0+mc1.21.1-sources.jar`
- 保留：0.0.2 / 0.0.3 / 0.0.4 各两支（未动）；`dist/` 未动（本 Phase 不许动）
- `build/libs` 里**没有** `farmstorm-0.1.0*`、也没有 `farmstorm-0.0.1*` ✔

### 2.5 `META-INF/neoforge.mods.toml` 的版本行

`jar tf` 的等价物（`zipfile` 只读，不依赖 PATH 上的 `jar`）：
```text
jar = build/libs/farmstorm-0.1.1+mc1.21.1.jar  bytes=1016609
entries = 566 ; has META-INF/neoforge.mods.toml = True
neoforge.mods.toml 里含 version 的行：
  2: loaderVersion = "[4,)"
  7: version = "0.1.1"          <- mod 版本行
 15: versionRange = "[21.1.0,)"
 22: versionRange = "[1.21.1]"
```

### 2.6 脚本端到端（额外自检，非任务书要求）

`cmd /c "检查环境.cmd <nul"`（`pause` 用 nul 喂掉）：
```text
  [OK]   menu_type: 1 个 ID 齐全
  已核 registry 对象: 93 / 93
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 315 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch
结论：FAIL 2 项，WARN 0 项

--- 2) 产物计数 ---
jar: OK
```
→ `:14` 的新 jar 名与真实产物对上了（`jar: OK`），脚本没被改坏。

---

## 3. 11 条验收读数

| # | 验收 | 读数 | 判定 |
|---|---|---|---|
| 1 | build exit 0；两支 0.1.1 产物在位；无 0.1.0 / 0.0.1 残留 | exit 0；`farmstorm-0.1.1+mc1.21.1.jar` 1016609 B、`-sources.jar` 939520 B；build/libs 无 0.1.0* / 0.0.1* | 通过 |
| 2 | jar 含 `META-INF/neoforge.mods.toml`，`version` = 0.1.1 | 566 条目，命中；`:7 version = "0.1.1"` | 通过 |
| 3 | 四支脚本 `farmstorm-0.0.1` = 0；`farmstorm-0.1.1+mc1.21.1` 检查环境=1、一键构建=2；四支 banner 同为 v0.1.1 | 0 / 0 / 0 / 0 ; 1 / 2 / 0 / 0 ; 四支各 1 处 `Farmstorm v0.1.1` | 通过 |
| 4 | `git diff gradle.properties` 只有第 11 行；build.gradle / settings.gradle diff 为空 | 见 §2.1；两文件 diff = （空） | 通过 |
| 5 | §1 段打印「58 组 / 166 键」与「剔除已删键 1 个：farmland.limitPerPlayer」 | 见 §1.3 前两行 | 通过 |
| 6 | 中间态恰好 2 条，键名只有那两个；退出码非 0 | `[FAIL]` 2 + `[STALE]` 0；`pumpkin_turret.bulletSpeed` / `farmland.limitPerPlayer`；exit 1 | 通过 |
| 7 | 失败路径：改 §4.1 组列 → 非 0 且报文件名+表行号；恢复后回到规定状态 | 见 §1.5（测试 A :974、测试 B :966+1016/1017）；恢复后 exit 1 / 2 条 | 通过（「回到 0」与 §6 矛盾，见偏差 D-5） |
| 8 | `git diff --stat docs/` 为空 | 空输出；另附 40 个 .md 的 SHA256 前后一致 | 通过 |
| 9 | `grep -c "expected_config_keys_v011"` ≥ 2 | = 2 | 通过 |
| 10 | 对照面：toml 仍在、`^\s*\[` 仍 59 段、含 `[farmland]`、`[pumpkin_turret]` 仍 3 键；两份 lang 各 315 | 764 行 / 59 段 / 含 `[farmland]` / lang 315 + 315；**`[pumpkin_turret]` 实测 4 键**（见偏差 D-4） | 通过（1 项读数与文档不符，已登记） |
| 11 | `git status --porcelain` 只允许 6 文件 + build 产物 + 证据文件 | 745 条中：`M` 6 个（gradle.properties、tools/verify_project.py、四支 .cmd）+ `README.md`（进入本节**前**已是 M）；`??` 侧非 .work/docs 的只有 `plan0.1.1.txt`（同样先于本节存在）；`build/` 被 gitignore 故不出现 | 通过 |

### 3.1 对照面原始读数

```text
run/config/farmstorm-server.toml  764 行（[IO.File] 按 \r?\n 切） / 18879 B
  ^\s*\[ = 59 段 ； ^\[ = 35 段 ； 含 [farmland] = 1 ； [pumpkin_turret] 在 :159
  [pumpkin_turret] 段内键 = 4：targetRadius=15 / projectileDamage=1 / knockbackLevel=1 / fireIntervalTicks=60
  SHA256 = 03A68F6B445EFA5F278CE07450873D1D163DBDF828A5FBCFF6F63B4A2CECAFA9 ；mtime 2026-11-02 02:39:02（早于本次作业）
  注意：该文件未被 git 跟踪（git ls-files -- run/config 为空），所以「未改」靠「本 Phase 未对它发起任何写操作 + mtime + 读数与基线逐项相同」三条共同支撑
lang: src/generated/resources/assets/farmstorm/lang/zh_cn.json = 315 键
      src/generated/resources/assets/farmstorm/lang/en_us.json = 315 键
git diff --stat -- docs   ->  <empty output>
```

---

## 4. `git diff --stat` 原文（收尾时）

```text
 README.md                                          | 59 ++++++++++++-
 gradle.properties                                  |  2 +-
 tools/verify_project.py                            | 96 +++++++++++++++++++---
 ...70\200\351\224\256\346\236\204\345\273\272.cmd" |  6 +-
 ...12\250\345\256\242\346\210\267\347\253\257.cmd" |  2 +-
 ...12\250\346\234\215\345\212\241\345\231\250.cmd" |  2 +-
 ...43\200\346\237\245\347\216\257\345\242\203.cmd" |  4 +-
 7 files changed, 149 insertions(+), 22 deletions(-)
```

> `README.md` 那 59 行**不是本 Phase 改的**：本节开工前的第一次 `git status --porcelain` 就已经是 ` M README.md`（见 §0 基线）。本 Phase 对 README 零写入。

---

## 5. 文档-源码偏差表

| # | 文档怎么说 | 实测 | 我按哪条做 |
|---|---|---|---|
| D-1 | 任务书 §3 / §8.2：「共 **5 条 banner + 3 条 jar 名 = 8 处**」「5 处：四支脚本各 1 处」（§8.2 自相矛盾） | 仓库根只有 4 支 `.cmd`，每支恰 1 条 banner ⇒ **4 banner + 3 jar = 7 处**（`Farmstorm v` 命中：1/1/1/1） | 按实测改 **7 处**；31 号 / 33 号写的「七处」才是对的，任务书的「8 处」是漏数（多算了 1 条 banner） |
| D-2 | A-51 / BRIEF §1.10：`一键构建.cmd` 的 jar 名在 `:11` `:12` | 实测一致（`:10` 是 `if not exist "dist" mkdir "dist"`） | **按 A-51 执行**：`一键构建.cmd:11`（release）/ `:12`（sources）；`检查环境.cmd:14` 行号不变 |
| D-3 | 31 号 F-176「涉及文件」只点 `检查环境.cmd:5` 与 `一键构建.cmd:5` 的 banner（表 G-15 也只说「两支」） | `启动客户端.cmd:5`、`启动服务器.cmd:5` 同样写死 `v0.0.1` | 按 D-124「版本号只有一处来源」改齐**四支** banner（任务书 §8.2 也是这个意思） |
| D-4 | 任务书 §3 / §9.10、33 号 §1.4：`[pumpkin_turret]` **只有 3 键** | 实测 **4 键**（targetRadius / projectileDamage / knockbackLevel / fireIntervalTicks）；30 号 §4.1 第 15 行也写「4 → 5 键（+1 键）」⇒ 改前就是 4 | 以源码 + 30 号 §4.1 为准，**不改 toml**（本 Phase 唯一要保证的是它没被动过）。「3」疑似从第 15 行「标记」列的 3 个标记（改/改/新）误抄而来；值 59 段 / 166 键 / `[farmland]` 在位的读数全部对上 |
| D-5 | 31 号 F-177 验收 4 / 33 号 §1.4：「恢复后退出码回到 **0**」 | 恢复后是 2 条红 / exit 1（本节 §6 明确要求「恰好 2 条、非 0 属预期」） | 两条判据互斥；按 §6 / §9.6（最硬一条）执行：恢复后回到「2 条 + exit 1」的**规定**状态，并把该句登记为文档内部矛盾（要等 F-192 收口后 exit 才是 0） |
| D-6 | 33 号 §1.5 翻车点 7：「`git status` 只允许 **4 个文件**（gradle.properties、两支 .cmd、tools/verify_project.py）」 | 任务书 §1 与本 Phase 实际边界是 **6 个文件**（四支 .cmd） | 按任务书 §1 / §9.11 的 6 文件口径 |
| D-7 | 31 号 §0.4 / 30 号 §3 第 43 行：工具期望来源在 `tools/verify_project.py:222-237`，并集在 `:244` | 改前实测：四个函数在 `:55-129`，并集与打印在 `:234-250`；`:222-237` 恰好落在 `main()` 的 `try/except` 段 | 行号只当线索，按实际函数定位改（A-40 的结论本身成立） |
| D-8 | 任务书 §9.1「`jar tf`」 | 本机 `jar` 不在 PATH | 用运行时 python 的 `zipfile` 只读等价取数（条目数 + 全文读出 `neoforge.mods.toml`），结论等价 |
| D-9 | `.work/v011-BRIEF.md` §1.10 **自相矛盾**：正文 `:152` 写 `一键构建.cmd:11-12`（与 A-51 一致），而 §1.10 表第 2 行 `:209` 仍写 `:10-11` | 实测 jar 名在 `:11` / `:12`（`:10` 是 `mkdir "dist"`） | **按 A-51 + 正文 `:152` 执行**；`:209` 那一行按 A-51 属于「待回灌」。另：A-52 的 `^\s*\[` 口径实测成立（59 = 35 顶层 + 24 嵌套），本 Phase 全部段计数都用它 |

---

## 6. 没做的与做不了的

| 项 | 状态 | 原因 |
|---|---|---|
| 客户端 GUI 类验收 | **未验证** | 本 Phase 无客户端面；且本机无法交互开 GUI |
| `启动客户端.cmd` / `启动服务器.cmd` 端到端启动 | **未验证** | 会拉起 Minecraft 客户端/专用服务器并长时间占用；本 Phase 只改了 `:5` 的 `echo` 文案，不构成行为风险；`检查环境.cmd` 已实跑（见 §2.6） |
| `一键构建.cmd` 整支实跑 | **未验证** | 它会 `copy` 到 `dist/`，而任务书 §1 明令「不要动 `dist/`」；构建本体已用 `.tools\gw.cmd build` 直接跑通 |
| `verify_project.py` 在 F-192 之后回到 `FAIL 0` | **未验证（本 Phase 不可验）** | A-40 中间态窗口：F-182 前必然是这 2 条。本轮只证明「红的方式恰好是这两条」 |
| `[FAIL]` 行在 F-182 之后收敛为 1 条 | **未验证** | 需 F-182 落地后才能读；本 Phase 不许提前做 §2 的活（33 号 §1.5 翻车点 7） |

## 7. 未关闭的问题

1. **任务书的「8 处」与实测 7 处**：建议回灌 31 号表 G-15 与任务书 §3/§8（写「七处」+ 明确四支脚本各 1 条 banner）。
2. **「`[pumpkin_turret]` 3 键」错值**：30 号 §4.1 第 15 行是「4 → 5」，0.1.0 的 toml 实测 4 键。建议 33 号 §1.4 与任务书 §3/§9.10 改成 4 键，否则下一个 Phase 会把它当成回归。
3. **F-177 验收 4 的「恢复后退出码回到 0」**：与 §6 互斥，建议改成「退出码回到本节规定的中间态（2 条 / 非 0）」。
4. **`run/config/farmstorm-server.toml` 未被 git 跟踪**：本 Phase 的「不许删、不许手改」只能靠 mtime + 读数取证，没有 diff 兜底。若后续 Phase 需要更强证据，建议把它纳入忽略清单的显式说明或加只读校验脚本。
5. **`verify_project.py` 里是否还有第二处硬编码总数**：已核 `EXPECTED_COUNTS`（93 的 12 类小计）与新旧 config 解析器都靠文档现算；`CROP_IDS` / `EXPECTED` 的 ID 清单本身是硬编码字面量（现状如此，F-212 的 #44 面），本 Phase 未动。
6. **31 号 F-177 验收 3 的「`[FAIL]`/`[STALE]` 合计 = 2」与工具实现**：本工具只有 `stale()` 会打 `[STALE]`，本轮两条都走 `fail()`，所以 `[STALE]` 为 0。若后续有人把两条中的一条改成 `stale()`，合计仍是 2，判据不破。
