# 任务书 · farmstorm v0.2.2 · W04（F-277 ‖ F-276 ‖ F-274）

> 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。本波 **3 个子代理并行**，各自只做一个任务。
> 前置：W01~W03 已落地并提交（HEAD `4ea74f1`）。

## 0. 三个任务（详见 49 号对应小节）

| 任务 | 标题 | 复杂度 | 涉及文件（**只许动这些**） | 依赖 |
|---|---|---|---|---|
| **F-277** | `registry/FarmstormVillagerProfessions`：植物学家职业 | M / 1.5 | **新增** 该类、`Farmstorm.java`（**只加 2 行**：import + 1 行 register） | F-275（已落） |
| **F-276** | `datagen/ModPoiTypeTagsProvider`：进 `minecraft:acquirable_job_site` | M / 1.5 | **新增** 该类、`datagen/FarmstormDataGen`（接线 1 处） | F-275（已落） |
| **F-274** | 两个默认值 4 / 24 + 八处注释同提交 | M / 1.5 | `config/FarmstormConfig`（`:941` / `:954` 的值 + `:238` / `:952` 注释）、`blockentity/OutpostCoreBlockEntity`（`:116` / `:349` 注释）、`network/CultivatorLinkPayload`（`:31-34` 注释）、`farmland/FarmlandLedger`（`:394` 注释） | F-273（已落） |

## 1. 必读

1. `docs/zh/49-TASKS-0.2.2.md` —— **你那个任务的小节** + §0.1~§0.5
2. `docs/zh/46-DECISIONS-0.2.2.md` —— F-277→D-196；F-276→D-196；F-274→D-195、D-201
3. `docs/zh/48-TECH-0.2.2.md` —— F-277 看 §2.3.6 的顺序纪律 + §2.3.3 的 `maxTickets`/`freeTickets`；F-276 看 §10 第 11 条与 RT-64；F-274 看 §4.1 的两个键
4. `.work/v022-BRIEF.md` —— §1.3（两个上限的读取点）、**§1.7**（`MAX_LINKS` 注释算术）、**§1.8**（改默认值不更新老存档 toml）、§3.2 / §3.4 第 12/13/18 项、**RT-63 / RT-64 / RT-68**

## 2. 已实测基线（HEAD `4ea74f1`）

| 项 | 值 |
|---|---|
| 注册面 | **98**（items 34 / blocks 27）；口径外累计 **2 类**（`ai_suspend` + 本波前的 `PoiType`） |
| Java 文件数 | **150** |
| `FarmstormPoiTypes` | 已落：`REGISTER` / `BOTANIST_KEY` / `BOTANIST`（`Set.of(OUTPOST_CORE.defaultBlockState())`, `maxTickets=1`, `validRange=1`）；`Farmstorm.java:64` 是它的 register 行 |
| `FarmstormTags` | **7** 个 TagKey（**不许**再 +1；POI 标签用原版 `PoiTypeTags`，不属 `FarmstormTags`） |
| config / lang | **176 / 60** / **338 × 2** |
| 当前默认值 | `maxPerPlayer = 2`（`:941`）、`cultivatorCapacity = 16`（`:954`） |

## 3. 硬约束

1. **并行纪律**：只动你任务清单里的文件；不改别人的行、不做格式化、不 `git add`。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` / `git log` 取证。提交由调度者做。
3. **`docs/` 一字不改**；`build.gradle` / `.tools/` / `run/` 不许改（datagen 用 `-PfsRunDir=run-w04x` 隔离）。
4. **构建/datagen 必须走任务书 §4 的互斥锁**；**禁止直接调 gradle**。
5. **注册面仍 98**：`PoiType` / `VillagerProfession` 都是**口径外**，**不许**碰 `Farmstorm.java` 的 `total == 98`、**不许**动 `verify_project.py` 的 `EXPECTED_COUNTS`。
6. **不许自己新定数值 / 名称**；拿不准写「未关闭的问题」，**不要停下来等**。
7. 本波不要求起服；运行时不成立的判据标「静态已验 / 运行时待 F-283」，**不许编读数**。

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

**F-277**
1. 新类存在；`VillagerProfession` 注册**恰 1 个**（植物学家），workstation 谓词引用 `FarmstormPoiTypes.BOTANIST_KEY`（`heldJobSite` / `acquirableJobSite` 形态按 48 号原文）。
2. `Farmstorm.java` **只加 2 行**：import + 1 行 `.register(modBus)`，且该行必须**排在 `FarmstormPoiTypes.REGISTER.register(modBus)`（现 `:64`）之后** —— 顺序纪律，写清理由。
3. `.register(modBus)` 计数 **13 → 14**；`total == 98` / `items == 34` 逐字未变；`EXPECTED_COUNTS` 未动。
4. 构建 exit 0；jar 内可见新类。

**F-276**
1. 新类 `datagen/ModPoiTypeTagsProvider` 存在，把 `FarmstormPoiTypes.BOTANIST_KEY` 写进 **`minecraft:acquirable_job_site`** 标签（用原版 `PoiTypeTags.ACQUIRABLE_JOB_SITE`，**不是** `FarmstormTags`）。
2. 在 `datagen/FarmstormDataGen` 里接线（否则 provider 永远不被调度）。
3. **判决性读数（RT-64 的主形态）**：跑 `runData`（锁内，`-PfsRunDir=run-w04x`）后，`src/generated/resources/data/minecraft/tags/poi_type/acquirable_job_site.json` 生成，`values` 含 **`farmstorm:botanist`**。**这条不绿就等于「村民永不认领」**，而它不会报错、只会静默失效。
4. 既有 `src/generated` 产物**不被改脏**（跑完给 `git status --porcelain -- src/generated`）。

**F-274**
1. `FarmstormConfig:941` 默认 **2 → 4**、`:954` 默认 **16 → 24**（只改默认值，**范围上界不动**）。
2. **八处注释同提交**（BRIEF §3.4 #18 列了至少 6 处，`FarmstormConfig:238` / `:952`、`OutpostCoreBlockEntity:116` / `:349`、`CultivatorLinkPayload:31-34`、`FarmlandLedger:394`）；按 A-90，`CultivatorLinkPayload` 的注释区间是 **`:31-34`**、`:35` 是常量行。逐处列改前/改后。
3. `CultivatorLinkPayload` 的算术注释要自洽：`MAX_LINKS = 1024` 与「每玩家 4 核 × 24 = 96」的关系（BRIEF §1.7 / RT-68）。**常量值不动**，只改注释。
4. **不动 `run/config`**（`.gitignore` 覆盖、且本波禁改 `run/`）。但要写清一条：**改默认值对老存档无效**（BRIEF §1.8 / RT-63）——老 toml 里仍是 2 / 16，F-283 若要在运行期读 4 / 24，必须先删 toml 重生成。
5. config 键数**仍 176 / 60 组**（只改默认值，不加键）；`EXPECTED_COUNTS` 未动；构建 exit 0。

## 6. 回报（`send_message` 给 lead）

- 任务号 + `git diff --stat` + 逐处行号与改前/改后原文；
- **每条验收断言的改前/改后实测值**；
- 命令原文（构建/datagen 锁等待、退出码、产物路径与内容原文）；
- 偏离登记 + 未验证清单 + 未关闭的问题；
- 明确写出：无任何 git 写操作、没动别人任务的文件、**没把口径外第 3 类算进 98**。
