# 任务书 · farmstorm v0.2.1 · Phase 40（F-247）

> 派发者：调度 Agent。工作目录 `D:\project_farm`（Windows / pwsh）。**本 Phase 只有 1 个任务、0.5 人日。**

## 0. 目标（F-247 · S·0.5）

把版本号与两支构建脚本的产物面钉死——它是全部 jar 名与「检查环境」判据的唯一来源。

**版本串按行是 6 处（A-70，不是 40 号 §3.2 写的 4 处）**：

| 文件 | 行 | 内容 |
|---|---|---|
| `gradle.properties` | `:11` | `mod_version=0.2.0` → `0.2.1` |
| `一键构建.cmd` | `:5` | banner `Farmstorm v0.2.0` → `v0.2.1` |
| `一键构建.cmd` | `:11` | `farmstorm-0.2.0+mc1.21.1.jar` → `0.2.1` |
| `一键构建.cmd` | `:12` | `farmstorm-0.2.0+mc1.21.1-sources.jar` → `0.2.1` |
| `检查环境.cmd` | `:5` | banner → `v0.2.1` |
| `检查环境.cmd` | `:14` | `if exist ...farmstorm-0.2.0+mc1.21.1.jar` → `0.2.1` |

## 1. 边界

**只允许改这 3 个文件**：`gradle.properties`（仅第 11 行）、`一键构建.cmd`（仅 `:5` `:11` `:12`）、`检查环境.cmd`（仅 `:5` `:14`）。

**绝对不可动**：
- `minecraft_version` / `neo_version` / toolchain 三行（`git diff gradle.properties` **只允许 1 行**）
- `src/`、`run/`、`docs/`、`build.gradle`、`.tools/`、`.work/v02*-*` 台账
- 两支脚本除上述 5 行外的任何内容（`chcp` / `PY` 路径 / `mkdir dist` / `pause` / `:fail` 段）
- **不碰数值面**：本 Phase 在中间态窗口之前（F-258 才开窗），任何判据都不引用 toml 的键数与组数

## 2. 必读

1. `docs/zh/44-PROMPT-MAIN-0.2.1.md` —— 全文（HC-121~HC-137、N-115~N-126、**§4 的 41 条陷阱**、§6.1 的 17 行已知冲突、§7 回报格式、§8 开工确认清单）
2. `docs/zh/45-PROMPT-TASKS-0.2.1.md` —— §0.2 五条重申、**§1（Phase 40 全节）**
3. `docs/zh/43-TASKS-0.2.1.md` —— §1 表的 F-247 行、**§5.1 A-70**
4. `docs/zh/42-TECH-0.2.1.md` —— §3 #1 ~ #5

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | `v0.2.0` tag 之后 1 个提交；工作树干净 |
| `mod_version` | **`0.2.0`**（`gradle.properties:11`） |
| `0.2.0` 字面量命中 | `gradle.properties` **1** / `一键构建.cmd` **3** / `检查环境.cmd` **2** = **6**（与 A-70 一致） |
| `build/libs` | `farmstorm-0.2.0+mc1.21.1.jar` 与 `-sources.jar` |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build` 基线 **exit 0**。**唯一入口，禁止直接调 gradle** |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 40 号 / 已有裁决为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。**提交由调度者做**。
3. 每条判据**改前先跑一遍记下实测值**，改后再跑一次。

## 5. 验收标准（= 45 号 §1.4 的 4 条）

1. **产物名与版本行**：`.tools\gw.cmd build` 退出码 **0**；`build/libs/farmstorm-0.2.1+mc1.21.1.jar` 与 `-sources.jar` 同时在位；`build/libs` 里 `farmstorm-0.2.0*` 残留 **0**；`jar tf` 读 `META-INF/neoforge.mods.toml` 的 `version` 行为 `0.2.1`。
2. **6 处一次改齐**：三个文件里 `0.2.0` 命中 **0**；`0.2.1` 命中 = **1**（gradle.properties）+ **3**（一键构建.cmd）+ **2**（检查环境.cmd）= **6**；两处 `:5` 与 `一键构建.cmd:12` 的 `-sources.jar` 都在其中。
3. **平台三行一字不动**：`git diff gradle.properties` 只第 **11** 行一行变化。
4. **本节不碰数值面**：`src/` 与 `run/` 的 diff 为空；没有任何判据引用 toml 的键数 / 组数。

## 6. 必须回报的证据

1. `git diff --stat` 原文；6 处字面量的「文件:行号」逐处点名。
2. `.tools\gw.cmd build` 退出码；`jar tf` 里 `neoforge.mods.toml` 的版本行原文。
3. 三个文件的两轮 `grep -c`（`0.2.0` / `0.2.1`）；`git diff gradle.properties` 的行数。
4. `build/libs` 的完整清单（含 `0.2.0*` 残留数）。
5. `检查环境.cmd` **真跑一次**的输出（或说明为什么跑不了 + 替代读数）。
6. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 7. 证据落盘

写 `.work/TASK-V021-P40-evidence.md`（新建）。回报正文只贴结论行。
