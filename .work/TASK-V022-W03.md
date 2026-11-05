# 任务书 · farmstorm v0.2.2 · W03（F-275 ‖ F-273）

> 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。本波 **2 个子代理并行**，各自只做一个任务。
> 前置：W01（F-267 / F-268 / F-270）、W02（F-272 / F-269 / F-271）已落地并提交。

## 0. 两个任务（详见 49 号对应小节）

| 任务 | 标题 | 复杂度 | 涉及文件（**只许动这些**） | 依赖 |
|---|---|---|---|---|
| **F-275** | `registry/FarmstormPoiTypes`：job site 注册（**口径外第 3 类**） | M / 1.5 | **新增** `registry/FarmstormPoiTypes.java`、`Farmstorm.java`（**只加 1 行** register） | F-268（已落） |
| **F-273** | 绑定判据落点：两处 `instanceof` → 标签判定 | M / 1.5 | `outpost/OutpostCoreBlockEntity.java` 的 `:393` 与 `:783` 两处判据 | F-272（已落） |

> ⚠️ **两处不可互串**：F-275 只碰 `Farmstorm.java` 与一个新类；F-273 只碰 `OutpostCoreBlockEntity` 的两个判据点。**F-274（下一波）也会改 `OutpostCoreBlockEntity`**（容量读取点 `:351` / `:541` 与八处注释），所以 F-273 这一波**先把判据点改干净、不要顺手改容量与注释**。

## 1. 必读

1. `docs/zh/49-TASKS-0.2.2.md` —— **你那个任务的小节** + §0.1~§0.5（尤其 §0.3 中间态窗口纪律）
2. `docs/zh/46-DECISIONS-0.2.2.md` —— F-275→D-196；F-273→D-194
3. `docs/zh/48-TECH-0.2.2.md` —— F-275 看 §2.3.6 的**注册顺序纪律**（`PoiType` 必须早于 `VillagerProfession` 与标签 provider）；F-273 看标签判定的落点节
4. `.work/v022-BRIEF.md` —— §1.4（标签体系范式）、§1.6（POI/职业全仓 0 处）、§3.4 必改第 8 / 19 项、**RT-64**（最容易漏的是 POI 未进 `minecraft:acquirable_job_site`——但那条属 **F-276**，不在本波）

## 2. 已实测基线（W02 之后自己再核一次）

| 项 | 值 |
|---|---|
| 注册面 | **98**（items 34 / blocks 27） |
| `FarmstormTags` | **7** 个 TagKey（W02 的 F-272 已加 `PROCESSING_BLOCK`） |
| POI / 职业 | 全仓 **0 处**（本波 F-275 是第一次引入 `PoiType`） |
| config / lang | **176 / 60** / **338 × 2**（本波都不动） |
| `Farmstorm.java` | 断言 `total == 98` / `items == 34`；注册入口在构造器里逐行 `.register(modBus)` |

## 3. 硬约束

1. **并行纪律**：只动你任务清单里的文件；不改别人的行、不做格式化、不 `git add`。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` / `git log` 取证。提交由调度者做。
3. **`docs/` 一字不改**；`build.gradle` / `.tools/` / `run/` 不许改。
4. **构建必须走互斥锁**（§4）；**禁止直接调 gradle**。
5. **⚠️ 口径外第 3 类不入 98**：`PoiType` **不在** 12 类里，**不许**去改 `Farmstorm.java` 的 `total ==` 断言、也不许动 `verify_project.py` 的 `EXPECTED_COUNTS`。F-275 往 `Farmstorm.java` 只加**一行** register 调用。
6. **不许自己新定数值 / 新定标签名**。拿不准写进「未关闭的问题」，**不要停下来等**。
7. 本波**不要求起服**；运行时不成立的判据标「静态已验 / 运行时待 F-283」，**不许编读数**。

## 4. 构建锁（复制粘贴用）

```powershell
$lock = 'D:\project_farm\.tools\build.lock'
for($i=0; $i -lt 180; $i++){
  if(Test-Path $lock){ $age=(Get-Date)-(Get-Item $lock).LastWriteTime
    if($age.TotalMinutes -gt 15){ Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue } }
  if(New-Item -ItemType Directory -Path $lock -ErrorAction SilentlyContinue){ break }
  Start-Sleep -Seconds 5 }
try { & 'D:\project_farm\.tools\gw.cmd' build } finally { Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue }
```

## 5. 各任务的判决性验收

**F-275**
1. 新类 `registry/FarmstormPoiTypes` 存在，`PoiType` 注册**恰 1 个**，绑定 `FarmstormBlocks.OUTPOST_CORE`，`maxTickets` / `validRange` 的取值按 48 号原文（**不许自拟**）。
2. `Farmstorm.java` **只多 1 行** `.register(modBus)`；12 类断言 `total == 98` **逐字未变**（这是本波最容易踩的界：POI 是口径外第 3 类）。
3. 全仓 `PoiType` 命中：改前 0 → 改后**全部落在新类里**（逐条列出命中位置）。
4. 构建 exit 0。

**F-273**
1. `OutpostCoreBlockEntity:393` 与 `:783` 两处 `instanceof`（按 49 号原文的判据形态）改为 **`FarmstormTags.PROCESSING_BLOCK` 标签判定**，改后这两处的 `instanceof` 命中为 **0**。
2. 改后**行为面等价性论证**：`CULTIVATOR` 已被 F-272 打进该标签 ⇒ 原 `instanceof CultivatorBlock` 的路径全部覆盖；若有第三类方块进了标签，也要能绑定（这正是 R3 的目的，写清这条）。
3. **不动** `:351` / `:541` 的容量读取、**不动**八处「默认 16 / 默认 2」注释（那是 F-274）。
4. 构建 exit 0；`git diff` 只落这一个文件的那两处。

## 6. 回报（`send_message` 给 lead）

- 任务号 + `git diff --stat` + 逐处行号与改前/改后原文；
- **每条验收断言的改前/改后实测值**（计数类给数字，行为类给代码级论证）；
- 命令原文（构建锁等待、`build` 退出码）；
- 偏离登记 + 未验证清单 + 未关闭的问题；
- 明确写出：无任何 git 写操作、没动别人任务的文件、**没把口径外第 3 类算进 98**。
