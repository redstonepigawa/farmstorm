# 证据 · F-247（Phase 40 · v0.2.1 版本串 6 处）

> 执行者：唯一执行子代理。工作目录 `D:\project_farm`（Windows / pwsh）。
> 任务书：`.work/TASK-V021-P40.md`；判据来源：45 号 §1.4（4 条）、43 号 §5.1 A-70、42 号 §3 #1~#5、44 号 HC-136 / HC-137。
> 基线 HEAD：`26cefd0`（v0.2.0 tag 之后 1 个提交）。**未做任何 git 写操作**。

---

## 1. 改了哪些文件（`git diff --stat` 原文）

```
 gradle.properties                                      | 2 +-
 "\344\270\200\351\224\256\346\236\204\345\273\272.cmd" | 6 +++---
 "\346\243\200\346\237\245\347\216\257\345\242\203.cmd" | 4 ++--
 3 files changed, 6 insertions(+), 6 deletions(-)
```

`git status --porcelain --untracked-files=no`：

```
 M gradle.properties
 M "\344\270\200\351\224\256\346\236\204\345\273\272.cmd"
 M "\346\243\200\346\237\245\347\216\257\345\242\203.cmd"
```

### 6 处字面量逐处点名（A-70 / HC-136）

| # | 文件:行 | 改前 | 改后 |
|---|---|---|---|
| 1 | `gradle.properties:11` | `mod_version=0.2.0` | `mod_version=0.2.1` |
| 2 | `一键构建.cmd:5`（抬头） | `echo   Farmstorm v0.2.0  -  一键构建` | `echo   Farmstorm v0.2.1  -  一键构建` |
| 3 | `一键构建.cmd:11`（主 jar） | `copy /y "build\libs\farmstorm-0.2.0+mc1.21.1.jar" "dist\"` | `...farmstorm-0.2.1+mc1.21.1.jar...` |
| 4 | `一键构建.cmd:12`（**-sources.jar**） | `copy /y "build\libs\farmstorm-0.2.0+mc1.21.1-sources.jar" "dist\"` | `...farmstorm-0.2.1+mc1.21.1-sources.jar...` |
| 5 | `检查环境.cmd:5`（抬头） | `echo   Farmstorm v0.2.0  -  验收自检` | `echo   Farmstorm v0.2.1  -  验收自检` |
| 6 | `检查环境.cmd:14`（存在性检查） | `if exist "build\libs\farmstorm-0.2.0+mc1.21.1.jar"` | `...farmstorm-0.2.1+mc1.21.1.jar...` |

改法：按字节读写（latin1 直通、非命名字节零改），因此 `chcp` / `PY` 路径 / `mkdir dist` / `pause` / `:fail` 段与全部中文逐字节未变。

---

## 2. 跑了什么

### `.tools\gw.cmd build`（唯一构建入口，未直接调 gradle）

```
cmd /c ""D:\project_farm\.tools\gw.cmd" build"
...
> Task :jar
> Task :sourcesJar
> Task :build

BUILD SUCCESSFUL in 11s
5 actionable tasks: 3 executed, 2 up-to-date
PLAINEXIT=0
```

**退出码 0**（`PLAINEXIT=0`）。

### `jar tf` 与 `neoforge.mods.toml` 的版本行

```
cmd /c ""...\jdk21\bin\jar.exe" tf build\libs\farmstorm-0.2.1+mc1.21.1.jar" | Select-String "mods.toml"
META-INF/neoforge.mods.toml
```

`jar xf` 抽出的 `META-INF/neoforge.mods.toml` 关键行原文：

```
line 1: modLoader = "javafml"
line 2: loaderVersion = "[4,)"
line 6: modId = "farmstorm"
line 7: version = "0.2.1"        <-- 版本行
line 8: displayName = "Farmstorm"
```

### 三个文件的两轮 `grep`

| 文件 | 改前 0.2.0 / 0.2.1 | 改后 0.2.0 / 0.2.1 |
|---|---|---|
| `gradle.properties` | 1 / 0 | **0 / 1** |
| `一键构建.cmd` | 3 / 0 | **0 / 3** |
| `检查环境.cmd` | 2 / 0 | **0 / 2** |
| 合计 | 6 / 0 | **0 / 6** |

改后 `0.2.1` 的 6 个命中行（逐行原文）：

```
gradle.properties:11: mod_version=0.2.1
一键构建.cmd:5:  echo   Farmstorm v0.2.1  -  一键构建
一键构建.cmd:11: copy /y "build\libs\farmstorm-0.2.1+mc1.21.1.jar" "dist\" >nul
一键构建.cmd:12: copy /y "build\libs\farmstorm-0.2.1+mc1.21.1-sources.jar" "dist\" >nul
检查环境.cmd:5:  echo   Farmstorm v0.2.1  -  验收自检
检查环境.cmd:14: if exist "build\libs\farmstorm-0.2.1+mc1.21.1.jar" (echo jar: OK) else (echo jar: 缺失，先跑 一键构建.cmd)
```

### `git diff gradle.properties`（平台三行一字不动）

变化行数 = **1**（一个 hunk，`-1 / +1`）：

```
-mod_version=0.2.0
+mod_version=0.2.1
```

`minecraft_version=1.21.1` / `neo_version=21.1.213` / toolchain 两行（`auto-detect` / `paths`）**未出现在 diff 里**。

### 编码面（`.cmd` 必须 UTF-8 无 BOM + CRLF）

| 文件 | BOM | CRLF / 总 LF |
|---|---|---|
| `一键构建.cmd` | False | 21 / 21 |
| `检查环境.cmd` | False | 15 / 15 |
| `gradle.properties` | False | 0 / 23（基线即 LF，字节数 673 → 673 未变） |

---

## 3. 观察到什么

### `build/libs` 完整清单（构建后、清残留后）

构建刚结束时是 4 个（Gradle `build` 不清理旧产物，旧的 `0.2.0` 仍在）：

```
farmstorm-0.2.0+mc1.21.1-sources.jar  1041068  2026/11/4 11:31:01
farmstorm-0.2.0+mc1.21.1.jar          1119199  2026/11/4 11:09:24
farmstorm-0.2.1+mc1.21.1-sources.jar  1041068  2026/11/5 1:46:56
farmstorm-0.2.1+mc1.21.1.jar          1119198  2026/11/5 1:46:55
```

**处置（已注册为动作）**：手工删除两个已核过绝对路径的旧产物（构建输出，非源码；`build/` 不进 `git status`）：

```
removed: D:\project_farm\build\libs\farmstorm-0.2.0+mc1.21.1.jar
removed: D:\project_farm\build\libs\farmstorm-0.2.0+mc1.21.1-sources.jar
```

清后清单：

```
farmstorm-0.2.1+mc1.21.1-sources.jar  1041068
farmstorm-0.2.1+mc1.21.1.jar          1119198

count 0.2.0* = 0 ; count 0.2.1* = 2
```

### `检查环境.cmd` 真跑一次（`"x" | & "D:\project_farm\检查环境.cmd"`，退出码 0）

```
============================================
  Farmstorm v0.2.1  -  验收自检
============================================

--- 1) 静态验收（注册名 / 配置键 / 语言键 / 禁令扫描）---
== 1. config：文档键集 vs 生成的 toml ==
  文档口径键数: 175
  生成 toml 键数: 175
  [WARN] 6 个键在文档里是逻辑路径、落盘是组内拼写 ... trade.common.max -> trade.commonMax ...
  [OK]   175 个配置键与文档逐字一致（toml 175 键）
== 2. 97 个注册 ID 逐字核对（全源码树） ==
  已核 registry 对象: 97 / 97
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 337 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch

结论：FAIL 0 项，WARN 1 项

--- 2) 产物计数 ---
jar: OK
Press any key to continue . . .
EXIT=0
```

关键读数：抬头已显示 **v0.2.1**；第 2 段 **`jar: OK`** —— 说明 `:14` 的新名字指到了真实产物（若漏改会打印「jar: 缺失」）。

> ⚠️ 这个「FAIL 0」**不**作为本 Phase 的证据：该工具的期望集仍是 0.2.0 口径（175 键 / 337 语言键 / 无 `expected_config_keys_v021`），按 44 号 §8 第 8 条不得引用（HC-135）。此处只取它的「jar: OK」。

---

## 4. 文档-源码偏差表（只登记，不改 `docs/`）

| # | 位置 | 文档怎么说 | 实测 | 处置 |
|---|---|---|---|---|
| 1 | 40 号 §3.2 第 2 行 / §7 ⑤ | 版本串「共 **4 处**」 | 实测 **6 处**（`gradle.properties` 1 + `一键构建.cmd` 3 + `检查环境.cmd` 2） | 已有裁决 **A-70 / HC-136** 校正为 6；本次按 6 处执行，文档不改 |
| 2 | `检查环境.cmd` 的静态验收段 | 未涉及 | 仍按 0.2.0 口径（175 键 / 337 语言键），`expected_config_keys_v021` 未加 | 属 F-257 / F-264 范围，本 Phase 不动（HC-135、RT-60） |
| 3 | 45 号 §1.1 前置「工作树干净」 | 只出现本节要改的 3 个文件 | 3 个 tracked 文件被改；`.work/` 下另有大量未跟踪历史文件（本 Phase 前即存在） | 现象登记；`git status` 的 tracked 面与前置一致 |

## 5. 未验证清单

| 项 | 原因 / 替代读数 |
|---|---|
| `一键构建.cmd` 整段真跑 | 它会 `pause` 并向 `dist\` 写入发布产物（副作用出边界）。替代读数：`gw.cmd build` 退出码 0（它调的就是同一命令）+ 其 `:11/:12` 的两个目标名与 `build/libs` 里实际在位文件名**逐字一致** |
| 客户端 / 服务端启动验证 | 本 Phase 不要求（只钉版本串与产物名）；留给 Phase 41 起的运行类验收 |
| 提交 | **禁止 git 写操作**，由调度者提交 |

## 6. 未关闭的问题

1. `Gradle build` 不清理旧名产物 → 每次改版本号后 `build/libs` 会留下上一个版本的 jar。本 Phase 用「手工删两个旧产物」达到「`0.2.0*` 残留 0」；**构建脚本的输出面本节不改**（45 号 §1.6 的纪律）。若要根治，需要一个 `clean` 或 `delete` 任务，属后续 Phase 的事。
2. `dist/` 里有历史发布归档，含 `farmstorm-0.2.0+mc1.21.1.jar` 与 `-sources.jar`（2026/11/4）。F-247 的判据只针对 `build/libs`，`dist/` 未动、未清理——发布归档是否要按版本清理，请调度者裁定。
3. `build/libs` 无 `-dev.jar` 之类旁产物（实测只有 2 个文件），无需登记更多。
