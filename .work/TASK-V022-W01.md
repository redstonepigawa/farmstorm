# 任务书 · farmstorm v0.2.2 · W01（F-267 ‖ F-268 ‖ F-270）

> 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。本波 **3 个子代理并行**，各自只做**一个**任务。
> 你是其中**一个**：开工前先确认自己在做哪一个（F-267 / F-268 / F-270），**只许动该任务的涉及文件**。

## 0. 三个任务（详见 49 号对应小节）

| 任务 | 标题 | 复杂度 | 涉及文件（**只许动这些**） |
|---|---|---|---|
| **F-267** | 版本号 0.2.2 与 6 处版本串 | S / 0.5 | `gradle.properties:11`、`一键构建.cmd:5,11,12`、`检查环境.cmd:5,14` |
| **F-268** | `plain_chives` 注册 + 注册面 **98** 的**原子**提交（**开窗**） | M / 1.5 | `FarmstormItems`、`Farmstorm.java`（断言）、`tools/verify_project.py` |
| **F-270** | `OutpostCoreScreen` 补 `render` 覆写（tooltip） | S / 0.5 | `client/OutpostCoreScreen.java`（42 行） |

> **F-269 / F-271 不在本波**（F-269 等 F-268；F-271 与 F-270 同文件、下一波做）。看到 `plain_chives.png` / `renderBg` 的活**不要顺手做**。

## 1. 必读（按顺序）

1. `docs/zh/49-TASKS-0.2.2.md` —— **你那个任务的小节**（目标 / 涉及文件 / 具体改动 / 实现要点 / 易漏点 / 验收断言），以及 §0.1~§0.5（五条粒度 + 中间态窗口 + 顺序死命令）
2. `docs/zh/46-DECISIONS-0.2.2.md` —— 你任务引用的决策号（F-267→D-187；F-268→D-189、D-193；F-270→D-190、D-192）
3. `docs/zh/48-TECH-0.2.2.md` —— 对应实现节（F-268 尤其看 §1 注册面 98 与「口径外累计 3 类」；F-270 看 §1.1 Bug A 的根因与 `ContainerScreen.java:25-29` 的原版写法）
4. `.work/v022-BRIEF.md` —— §0 基线、§1.1（两个 bug 的根因，**推翻 0.2.1 的判断**）、§1.5（物品注册范式）、§1.9（`verify_project.py` 期望面）、§3.4 必改清单

## 2. 已实测基线（2026-11-05，直接用，不要重测）

| 项 | 值 |
|---|---|
| git | `86a3acc`（文档基线；上一 tag `v0.2.1`） |
| `mod_version` | **0.2.1** |
| Java 文件 / 注册面 | **149** / **97**（items 33 / blocks 27） |
| config / lang | **176 键 60 组** / **338 × 2** |
| 网络包 / Mixin | **2** / **0** |
| `verify_project.py` | `EXPECTED_COUNTS` 写死 `items: 33`、`EXPECTED["items"]` 在 `:334` 附近 |

## 3. 硬约束

1. **并行纪律**：本波另有 2 个子代理在改**别的文件**。**只许动你任务清单里的文件**；不要为了「顺手」去改别人的行、不要做格式化、不要 `git add`。
2. **禁止任何 git 写操作**（commit / tag / push / stash / add / checkout 一律不许）；只许 `git status` / `git diff` / `git log` 取证。**提交由调度者做，一个任务一个提交。**
3. **`docs/` 一字不改**（`docs/zh/00`~`51` 全部；`docs/en` 停在 v0.2.1）。`build.gradle` / `.tools/` / `run/` 也不许改。
4. **构建必须走互斥锁**（§4）——本波 3 个并发构建共用 `GRADLE_USER_HOME`，不加锁会互相踩。**禁止直接调 gradle**。
5. **本波不要求起服**。需要运行时的判据：能做就做（用你自己的 lane `run-w01a/b/c`），做不了就标「静态已验 / 运行时待 F-283」并写进证据，**不许编读数**。
6. **不许自己新定数值**。拿不准的写进「未关闭的问题」，**不要停下来等**。
7. `EXPECTED_COUNTS` 除法：F-268 必须把 `items` 33 → **34** 与 `FarmstormItems` 的注册**同一提交**（否则工具与启动自检两套闸互相矛盾）。

## 4. 构建锁（复制粘贴用）

```powershell
$lock = 'D:\project_farm\.tools\build.lock'
for($i=0; $i -lt 180; $i++){
  if(Test-Path $lock){
    $age = (Get-Date) - (Get-Item $lock).LastWriteTime
    if($age.TotalMinutes -gt 15){ Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue }
  }
  if(New-Item -ItemType Directory -Path $lock -ErrorAction SilentlyContinue){ break }
  Start-Sleep -Seconds 5
}
try { & 'D:\project_farm\.tools\gw.cmd' build } finally { Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue }
```

## 5. 各任务的判决性验收（调度者会逐条复核）

**F-267**
1. `gradle.properties:11` = `mod_version=0.2.2`。
2. 两支 `.cmd` 里 `0.2.1` 命中 **0**、`0.2.2` 命中与改前 `0.2.1` 的命中数**相等**（逐处点名行号）。
3. 全仓 `0.2.1` 的命中只剩**历史/文档/存档路径**这类合法处，逐条列出（不许一键替换）。

**F-268**
1. `plain_chives` 进 items 注册、**不走 `cropItem`**（它是非作物），进 `creativeTabItems()`。
2. `Farmstorm.java` 的启动自检断言 `total == 98` / `items == 34`（12 类其余 11 类**逐类 `==` 不动**，A-83 说是 11 个）。
3. `tools/verify_project.py` 的 `EXPECTED["items"]` 加 `plain_chives`、`EXPECTED_COUNTS["items"]` 33 → **34**。
4. 构建 exit 0；**同一个提交**里含这四处（注册 / 断言 / 工具期望 / 8 处「97」注释按 A-84 的落点）。
5. 起服日志（若起了）打印 `自检 注册对象 98 个 … items 34 … → OK`；起不了服就静态验 + 登记。

**F-270**
1. `OutpostCoreScreen` 新增 `render` 覆写：`super.render(...)` 之后调 `this.renderTooltip(g, mouseX, mouseY)`（照 `ContainerScreen.java:25-29`）。
2. 该类**只**增这一处覆写；`renderBg` **不动**（那是 F-271 的活）。
3. 构建 exit 0；`git diff` 只落在该文件。

## 6. 回报（`send_message` 给 lead）

- 你做的**任务号** + 改了哪些文件（`git diff --stat` + 逐处行号与改前/改后原文）；
- **每条验收断言的实测值**（改前 / 改后）；
- 跑过的命令原文（含构建锁等待时间、`build` 退出码；若起了服给日志尾行）；
- **偏离登记**（与 46~51 号原文不一致处）+ **未验证清单** + **未关闭的问题**；
- 明确写出：本任务**无任何 git 写操作**，且**没动别人任务的文件**。
