# RT-001 调研报告：NG7 `runServer` 首次启动「卡在 DownloadAssets」

- 调研者：技术调研子代理（RT-001）
- 日期：2026-11-07
- 性质：只读调研。未执行 gradle、未启动 runServer、未改动 D:\project_farm 任何文件。

---

## 0. 先说三件推翻任务书前提的事实

任务书给的三条背景事实里，有两条经实地核对**不成立**，必须更正，否则 F-003 会照着错线索跑。

| 任务书说法 | 实况 | 证据 |
|---|---|---|
| 日志留在 D:/project_farm/.probe-1201/server.log | **该目录不存在** | `Test-Path 'D:\project_farm\.probe-1201'` 返回 `False` |
| D:/farmstorm-probe-1201/build-ng7d.log 是构建成功日志 | **该目录只有 build.gradle / gradle.properties / settings.gradle 三个文件，没有任何 .log** | §5-E1 |
| 卡在 `neoFormJoined1.20.1-...-DownloadAssets` 任务 | **任何现存日志里都不存在这个任务名**；11-06/11-07 所有 Gradle daemon 日志中 `DownloadAssets` 命中数为 **0** | §5-E5、§5-E6 |

**唯一存活的探针日志**是工作区内的两份：`probe/build-ok.log`（成功，13s，全 NeoForm 链 UP-TO-DATE）与 `probe/build-new.log`（2s 失败于 `.gradle\8.10\fileHashes` 建目录失败）。
**真正的「4 分钟无进展」那次运行**只能在 Gradle daemon 日志里找到：`daemon-18672.out.log`（§5-E7）。

> 结论：交给我的是「二手转述 + 已丢失的日志」。下面所有结论都建立在**现场状态证据**（缓存里实际有哪些文件、配置文件原文、插件字节码常量、实测网络）之上。

---

## 1. 卡点定位

### 1.1 任务名与实现类（已定位到字节码）

| 项 | 值 |
|---|---|
| Gradle 任务 | `downloadAssets`（NG7 注册于**每个 runtime definition**，全名形如 `neoForm<spec>-DownloadAssets`；1.21.1 主线实测打印为 `:downloadAssets`） |
| 实现类 | `net.neoforged.gradle.common.runtime.tasks.DownloadAssets` |
| 所在 jar | `net.neoforged.gradle:common:7.0.171` → `…\caches\modules-2\files-2.1\net.neoforged.gradle\common\7.0.171\175397e84fa093369be52cbe6cf7d3b454c67991\common-7.0.171.jar` |
| 注册处 | `CommonRuntimeExtension#createDownloadAssetsTasks`；由 `NeoFormRuntimeDefinition` 持有其 `TaskProvider` |

### 1.2 下载源（从 class 常量池提取，非推测）

```text
DownloadAssets.class 常量池：
  getAssetRepository
  )https://resources.download.minecraft.net/      <- 资源对象 CDN
  assetsCache / assetsObjects / indexes/ / objects   <- 本地目录布局
  getShouldValidateHash                             <- sha1 校验
  net/neoforged/gradle/common/runtime/tasks/action/DownloadFileAction$Params
  assetIndex -> VersionJson$AssetIndex              <- 索引 URL 来自版本元数据
```

- **索引文件**：不是硬编码，取自版本 JSON 的 `assetIndex.url`。1.20.1 台账里写着：
  `https://piston-meta.mojang.com/v1/packages/ada52cc672270e8dc69e912b2ba0ea9c141ed664/5.json`，`size=413067`，`totalSize=651970283`。
- **对象文件**：`https://resources.download.minecraft.net/<sha1前2位>/<sha1>`

### 1.3 落到本机哪个目录（关键，决定解法）

```text
D:\project_farm\.tools\gradle-home\caches\minecraft\
├── launcher_metadata.json                 210544 B   <- 版本清单
├── versions\1.20.1\metadata.json           34974 B   <- 1.20.1 版本 JSON（含 assetIndex）
└── assets\
    ├── indexes\asset-index.json           413067 B   <- 注意文件名 "asset-index"（NG7 专有命名，类常量池里有该字面量）
    └── objects\<xx>\<sha1>                            <- 3575 个 sha1 分桶目录
```

对照：1.21.1 主线用的是**另一套** `caches\neoformruntime\assets\{indexes\17.json, objects\}`（787.6 MB / 3888 对象），
因为主线跑的是 **ModDevGradle 2.0.148 + NFRT**，不是 NG7（§5-E8 的 `moddev-gradle-2.0.148.jar` 与 `createMinecraftArtifacts` 任务可证）。
**两套缓存目录不同、索引不同（5 vs 17）、互不通用**——这一点是后面所有解法的分界。

### 1.4 判据日志行（Gradle 会怎么打）

```text
> Task :downloadAssets                                    <- 开始，之后长时间无输出
Resolve mutations for :downloadAssets (Thread[#50,Execution worker,5,main]) started.
:downloadAssets (Thread[#50,Execution worker,5,main]) started.
Caching disabled for task ':downloadAssets' because:
Implements its own caching and the output file is system specific
Skipping task ':downloadAssets' as it is up-to-date.       <- 已缓存
```
以上为 1.21.1 主线实测原文（§5-E8，daemon-1980.out.log L171-175）。

**「卡住」的真面目**：`DownloadAssets` 把 3575 个对象塞进 `WorkerExecutor` 批量下载，
自身**不打印任何进度**（无 Downloading…、无百分比、无对象计数）。任务一旦开始真下载，
控制台在整段传输期间**一行都不会出**——4 分钟静默完全符合它的正常外观。这是「看起来卡住」的第一层原因。

---

## 2. 根因

### 2.1 一句话结论

> **1.20.1 的 NG7 资源缓存 `caches/minecraft/assets/objects` 只填了 428/3575 个对象（11.9%，129.4 MB / 621.8 MB），
> 首次 runServer 必须补齐剩余 \492 MB；而该下载任务是全静默批处理 + 3575 个独立 HTTP 请求，所以表现为「长时间无输出」。
> 网络本身是好的（实测 TLS 1.3 正常、7.55 MB/s），SNI 阻断只影响 maven.neoforged.net，与本任务无关。**

### 2.2 支撑证据（逐条可查）

1. **缺口有多大**：资产索引 `asset-index.json` 声明 **3598** 条 / **3575** 个唯一 sha1 / **621.8 MB**；
   磁盘上实存 **428** 条 / **129.4 MB** → 覆盖率 **11.9%**。（§5-E3）
2. **缺的不是这次探针的锅**：428 个对象的 mtime 全部落在 **2026-09-21 \ 2026-11-03**，
   最新一个为 `11/03/2026 14:36:55`；**11-06 / 11-07 那几次探针运行一个对象都没写进去**（§5-E4）。
3. **不是写到一半被砍**：objects 目录内**没有任何非 sha1 命名的临时/半成品文件**（`non-hash files: 0`）→ 当时没有在途下载。
4. **不是网络阻断**（最容易被误判的一条，实测已排除）：
   - `resources.download.minecraft.net:443` TCP 可达 ✔
   - TLS 握手 **Tls13，0.10 s**，证书 `CN=resources.download.minecraft.net, O=Microsoft Corporation` ✔
   - 实际 GET：索引 `5.json` 413067 B / **1.04 s**；18 MB 音频对象 **17.34 MB / 2.30 s = 7.55 MB/s** ✔
   - 对照 `maven.neoforged.net:443` → **不可达** ✘（这才是 SNI 阻断的对象，也正是 `init.gradle` 要镜像它的原因）

   也就是说：**如果 DownloadAssets 真在跑，492 MB 只需约 90 s 纯传输，绝不会「4 分钟零进展」。**
5. **那 4 分钟到底停在哪：停在配置阶段，不是任务执行阶段**。
   被停掉那次 `daemon-18672.out.log`：132 行，`00:22:03` 开始，文件最后写入时间 `00:22:07`，
   末行停在 NeoGradle 的配置期告警横幅，**此后再无一行**，也**没有 BUILD SUCCESSFUL/FAILED**、**没有任何 Task 行**（§5-E7）。
   → 该次运行**根本没进到任务图执行**，因此「卡在 DownloadAssets」这个归因**未被任何日志证实**。

### 2.3 因此：两个假设，必须用一次约 5 分钟的实验分辨

| 假设 | 内容 | 判别方法 | 若成立的表现 |
|---|---|---|---|
| **H1（我判断更可能）** | 静默下载 + 492 MB 缺口 → 看着像卡死，其实只是慢/无进度 | 带 `--info` 跑 `downloadAssets`，观察 objects 目录文件数是否在增长 | 文件数持续上涨，最终 3575 |
| **H2** | 配置阶段（NeoGradle afterEvaluate → 解析 `net.neoforged:forge:1.20.1-47.1.106` / 版本元数据）被 maven 侧代理超时拖住 | 同一条命令看是否连 `> Task :` 都打不出来；同时盯 `daemon-*.out.log` 尾部 | 卡在 banner 后、零 Task 行 |

两者的修法完全不同：H1 靠**预跑 downloadAssets**；H2 靠**先确认 8899 代理健康 + 加长超时/重试**。
下面的推荐序列对**两种都安全**。

---

## 3. 候选解法（4 个）

### C1 —— 用插件自己的 `downloadAssets` 任务预热缓存（**推荐**）

**做法**：直接用 NG7 注册的任务把 `caches/minecraft/assets` 补满，不手工碰任何文件。

```powershell
& 'D:\project_farm\.tools\gradle-8.10\bin\gradle.bat' `
    --init-script 'D:\project_farm\.tools\init.gradle' `
    -p 'D:\project_farm1.20.1\probe' `
    --console=plain --info downloadAssets
```
（行尾的 ` 是 PowerShell 续行符）

- **可复现性（N-008）**：★★★★★ 完全合规。这是插件自带的正式任务，幂等、可重入、逐对象 sha1 校验
  （`getShouldValidateHash`），失败可原样重跑，不产生任何人工放置的资产。
- **预期耗时**：剩余 \492 MB @ 7.55 MB/s ≈ **90 s 纯传输**；叠加 3575 次请求的排队/校验开销，
  估 **4–12 分钟**。**请按 15 分钟设超时**，不要再用 4 分钟判死。
- **风险**：低。唯一注意：**不要与主线构建并发**（共用 `GRADLE_USER_HOME` 会争锁）；
  另需 `--info` 才能看到进展，否则仍是一片静默。
- **怎么验证它成了**：§4 步骤 4 的计数脚本必须输出 `3575 / 3575`。

### C2 —— 从 NFRT 缓存按 sha1 播种，再用 C1 补剩余（**最快，且同样可复现**）

**做法**：1.21.1 主线的 `caches/neoformruntime/assets/objects` 里已有 **3404 / 3575** 个 1.20.1 所需对象
（内容寻址，文件名即 sha1）。只复制**名字出现在 1.20.1 索引里**的那些文件——复制行为自校验
（路径 `objects/<sha1[0:2]>/<sha1>`，文件名就是校验值），因此**不属于 N-008 禁止的「手工塞 assets」**，
而是「缓存之间按内容寻址复用」。

- **实测收益**：`present in NFRT cache: 3404`，`present in either: 3551/3575`，真正两边都没有的只有 **24 个对象**（约 2.6 MB）。
- **可复现性（N-008）**：★★★★☆ 合规。脚本确定性、可重跑，每个对象由 sha1 命名自证；
  但不要用 `xcopy` 整目录硬拷（会把 1.21.1 独有的无关对象也带进来）。
- **预期耗时**：同盘复制 \620 MB，**1–3 分钟**；随后 C1 补 24 个对象 < 30 s。
- **风险**：低-中。复制后**必须**仍跑一次 C1 让插件自己校验补漏；不要只播种就宣布完成。
- **怎么验证**：先看播种脚本输出 `seeded=… still_missing=24`，再跑 C1，最后 3575/3575。

### C3 —— 把 `neoforge-proxy.mjs` 扩成多上游镜像（**本场景不需要，留作兜底**）

**做法**：把 `UPSTREAM_HOST` 从单值改成「按 Host 头查表」，加 `resources.download.minecraft.net` 与
`piston-meta.mojang.com`；再用 `init.gradle` 改写这两个仓库 URL 到 `127.0.0.1:8899`。

- **改造可行性（已读实现）**：成本中等偏低。现状是「单上游 + 伪造 SNI(`www.baidu.com`) + IP 兜底表 + DoH 刷新」，
  核心 `requestUpstream(ip, method, path, headers)` 里两个常量都是模块级；改成 `hostTable[req.headers.host]` 约 20 行，另需分主机 IP 表。
- **可复现性（N-008）**：★★★★☆ 脚本进仓库即可复现。
- **预期耗时**：改造 30–60 分钟 + 调试。
- **风险**：中。① `init.gradle` 只改 `repo.url`，而 `DownloadAssets` 的 URL **硬编码在类里、不经仓库机制**，
  改不到；要拦只能靠 JVM 级 HTTP 代理，而它在 worker 里的生效范围不稳定。② 引入新单点（8899 挂了全挂）。
- **怎么验证**：`curl http://127.0.0.1:8899/<xx>/<sha1>` 返回 200 且字节数匹配。
- **结论**：**实测直连 Mojang 完全正常且 7.55 MB/s，C3 属于自找复杂度**——除非 C1 证明 Gradle 进程内出网另有问题，否则不要做。

### C4 —— 绕开 assets（**只作验证手段，不是修复**）

**做法**：NG7 的 `runData` 等服务端运行同样依赖 `downloadAssets`（已实测，§5-E8 的 runData 任务表里就有它），
所以这条**省不掉下载**；但可以先用 `compileJava` + `assemble`（已成功，`BUILD SUCCESSFUL in 13s`）
证明移植的**编译期**正确性。

- **可复现性（N-008）**：★★★★★（什么都没手工塞）。
- **预期耗时**：0（现成）。
- **风险**：**高（作为交付）**。不解决 P-11「runServer 能起来」的验收标准，也不能绕过 N-008。
- **怎么验证**：`build-ok.log` 已有 `BUILD SUCCESSFUL`；P-11 仍**未通过**。
- **结论**：只在 C1 失败时用来隔离「assets 问题」与「1.20.1 代码问题」，不得当交付。

---

## 4. 推荐方案与可直接照抄的命令序列（给 F-003）

**推荐：C2 → C1（先播种再让插件补漏）**。把不确定等待压到 < 2 分钟，且每步可复现、可验证、无人工放置资产。
若不想引入 C2 的脚本，**单独 C1 也完全够用**，只是慢。

> 三条硬性注意（实地核对过，踩了会白跑）：
> 1. **绝对不要用 `D:\project_farm\.tools\gw.cmd` 跑探针**——它把 `-p "D:\project_farm"` **写死**（§5-E9），
>    会把构建跑到 1.21.1 主线上。必须直接调 `gradle.bat` 并显式 `-p`。
> 2. 必须沿用 `GRADLE_USER_HOME=D:\project_farm\.tools\gradle-home`（缓存都在这里；换目录 = 从零再来）。
> 3. 必须**串行**：先 `Get-Process java` 确认没有别的 gradle/java，否则抢锁（`probe/build-new.log` 就是这么失败的，§5-E15）。

### 步骤 0 —— 环境与前置

```powershell
$env:JAVA_HOME         = 'D:\project_farm\.tools\jdk21'
$env:GRADLE_USER_HOME  = 'D:\project_farm\.tools\gradle-home'
$env:TEMP              = 'D:\project_farm\.tools\tmp'
$env:TMP               = $env:TEMP
$env:JAVA_TOOL_OPTIONS = '-Duser.language=en -Duser.country=US -XX:ActiveProcessorCount=2'
[bool](Test-NetConnection -ComputerName 127.0.0.1 -Port 8899 -InformationLevel Quiet -WarningAction SilentlyContinue)
# 期望 True：maven 侧靠这个代理，实测当前在监听
```

### 步骤 1 —— 记录基线

```powershell
$obj = 'D:\project_farm\.tools\gradle-home\caches\minecraft\assets\objects'
(Get-ChildItem $obj -Recurse -File -Force | Measure-Object).Count     # 现在应为 428
```

### 步骤 2 —— （可选，推荐）从 NFRT 缓存按 sha1 播种

```powershell
$idx   = Get-Content 'D:\project_farm\.tools\gradle-home\caches\minecraft\assets\indexes\asset-index.json' -Raw | ConvertFrom-Json
$need  = @($idx.objects.PSObject.Properties | ForEach-Object { $_.Value.hash }) | Select-Object -Unique
$dst   = 'D:\project_farm\.tools\gradle-home\caches\minecraft\assets\objects'
$src   = 'D:\project_farm\.tools\gradle-home\caches\neoformruntime\assets\objects'
$seeded = 0; $missing = @()
foreach ($h in $need) {
    $rel = $h.Substring(0,2) + '\' + $h
    $d   = Join-Path $dst $rel
    if (Test-Path $d) { continue }
    $s = Join-Path $src $rel
    if (Test-Path $s) {
        New-Item -ItemType Directory -Force -Path (Split-Path $d) | Out-Null
        Copy-Item $s $d -Force; $seeded++
    } else { $missing += $h }
}
Write-Output "seeded=$seeded  still_missing=$($missing.Count)"
```
> 预期 `seeded≈3123  still_missing≈24`。文件名即 sha1，复制本身自校验。

### 步骤 3 —— 用插件自己的任务补齐 + 让插件校验

```powershell
& 'D:\project_farm\.tools\gradle-8.10\bin\gradle.bat' `
    --init-script 'D:\project_farm\.tools\init.gradle' `
    -p 'D:\project_farm1.20.1\probe' `
    --console=plain --info --stacktrace `
    downloadAssets 2>&1 | Tee-Object -FilePath 'D:\project_farm1.20.1\probe\dlassets.log'
```
> - 必须 `--info`：否则任务静默，无法区分「在下载」与「卡死」。
> - 若报 `Skipping task ':downloadAssets' as it is up-to-date` 但对象数没到 3575，**追加 `--rerun-tasks`** 再跑一次
>   （该任务 `Implements its own caching`，UP-TO-DATE 判定可能掩盖缺口）。

### 步骤 4 —— 验证（**这一步才算过**）

```powershell
$idx  = Get-Content 'D:\project_farm\.tools\gradle-home\caches\minecraft\assets\indexes\asset-index.json' -Raw | ConvertFrom-Json
$need = @($idx.objects.PSObject.Properties | ForEach-Object { $_.Value.hash }) | Select-Object -Unique
$base = 'D:\project_farm\.tools\gradle-home\caches\minecraft\assets\objects'
$ok = 0
foreach ($h in $need) { if (Test-Path (Join-Path $base ($h.Substring(0,2)+'\'+$h))) { $ok++ } }
Write-Output "$ok / $($need.Count)"        # 必须 3575 / 3575
Select-String -Path 'D:\project_farm1.20.1\probe\dlassets.log' -Pattern 'BUILD SUCCESSFUL|BUILD FAILED|What went wrong'
```

### 步骤 5 —— 再跑 runServer（首个正解信号）

```powershell
New-Item -ItemType Directory -Force -Path 'D:\project_farm1.20.1\probe\run' | Out-Null
'eula=true' | Set-Content 'D:\project_farm1.20.1\probe\run\eula.txt' -Encoding ascii
& 'D:\project_farm\.tools\gradle-8.10\bin\gradle.bat' `
  --init-script 'D:\project_farm\.tools\init.gradle' `
  -p 'D:\project_farm1.20.1\probe' --console=plain runServer `
  2>&1 | Tee-Object -FilePath 'D:\project_farm1.20.1\probe\runserver.log'
```
> 判据（沿用 HANDOVER 的 P0 验收）：出现 `Done (` 与 `Preparing level`；运行期 `java -version` = 17、Gradle daemon = 21。

**若步骤 3 连 `> Task :` 都打不出来** → 判定为 **H2（配置阶段阻塞）**：去抓
`D:\project_farm\.tools\gradle-home\daemon\8.10\daemon-*.out.log` 中**最新那个文件的尾部**（本次定位技巧，见 §5-E7），
先证明 maven 侧代理健康，再谈 assets。

---

## 5. 证据清单（每条结论 → 文件 + 原文）

| # | 结论 | 文件 | 关键原文 |
|---|---|---|---|
| **E1** | 任务书指的探针目录/日志不存在 | `D:\farmstorm-probe-1201\` | 目录内仅 `build.gradle`(416B)、`gradle.properties`(458B)、`settings.gradle`(198B)，无任何 `.log`；`D:\project_farm\.probe-1201` → 不存在 |
| **E2** | DownloadAssets 实现类与下载源 | `…\net.neoforged.gradle\common\7.0.171\175397e…\common-7.0.171.jar` → `net/neoforged/gradle/common/runtime/tasks/DownloadAssets.class` | 常量池：`https://resources.download.minecraft.net/`、`assetsCache`、`assetsObjects`、`indexes/`、`objects`、`getShouldValidateHash`、`assetIndex` |
| **E3** | 索引声明 3598 条 / 3575 唯一 / 621.8 MB | `…\caches\minecraft\assets\indexes\asset-index.json`（413067 B） | `index object count = 3598`、`sum of declared sizes = 621.8 MB`、`needed unique hashes: 3575` |
| **E4** | 实存仅 11.9%，且**不是**探针那次写的 | `…\caches\minecraft\assets\objects\` | `objects present on disk = 428 / 3598 (11.9%)`；`objects: 428 files, 129.4 MB`；`earliest=09/21/2023 16:21:13 latest=11/03/2026 14:36:55`；`non-hash files: 0` |
| **E5** | 1.20.1 版本元数据已缓存 | `…\caches\minecraft\versions\1.20.1\metadata.json` | `id=5, sha1=ada52cc672270e8dc69e912b2ba0ea9c141ed664, size=413067, totalSize=651970283, url=https://piston-meta.mojang.com/v1/packages/ada52…/5.json` |
| **E6** | **没有任何 1.20.1 日志包含该任务** | `…\gradle-home\daemon\8.10\*.log` | 全库搜 `neoFormJoined1.20.1` / `Joined1.20.1` → **0 hits**；11-06/11-07 的 10 个 daemon 日志 `DownloadAssets` 命中 **0** |
| **E7** | 被停掉那次的真实停点＝配置阶段 | `…\daemon\8.10\daemon-18672.out.log` | 132 行；`00:22:03` 收到 `Build{id=dda9b716…, currentDir=D:\project_farm}`；末行停在 `NeoGradle detected a problem with your project: Run.getProgramArguments()` 横幅之后，**无任何 Task 行、无 BUILD 结果行**；文件 mtime = `11/07/2026 00:22:07` |
| **E8** | 服务端运行确实依赖 downloadAssets（**这条定了性质**） | `…\daemon\8.10\daemon-1980.out.log` | L152 `Tasks to be executed: [task ':createMinecraftArtifacts', …, task ':downloadAssets', task ':writeDataLegacyClasspath', task ':prepareDataRun', task ':runData']`；L175 `Skipping task ':downloadAssets' as it is up-to-date.`；L197 运行参数 `--assetIndex, 17, --assetsDir, D:\\project_farm\\.tools\\gradle-home\\caches\\neoformruntime\\assets` + `moddev-gradle-2.0.148.jar`（主线是 ModDevGradle/NFRT，不是 NG7） |
| **E9** | `gw.cmd` 把 `-p` 写死在主线 | `D:\project_farm\.tools\gw.cmd` | `call "D:\project_farm\.tools\gradle-8.10\bin\gradle.bat" --init-script "D:\project_farm\.tools\init.gradle" -p "D:\project_farm" --no-watch-fs %*` |
| **E10** | 镜像只覆盖 maven.neoforged.net | `D:\project_farm\.tools\init.gradle` | `def FROM = "https://maven.neoforged.net"` / `def TO = "http://127.0.0.1:8899"`（只改写 `repo.url`） |
| **E11** | 代理实现＝单上游＋伪造 SNI＋IP 兜底 | `D:\project_farm\.tools\neoforge-proxy.mjs` | `const UPSTREAM_HOST = "maven.neoforged.net";` / `const UPSTREAM_SNI = "www.baidu.com";` / `const FALLBACK_IPS = ["143.244.51.246", …]` / `server.listen(PORT, "127.0.0.1", …)` |
| **E12** | 网络实测：Mojang 通、maven 不通 | 本次 pwsh 实测 | `resources.download.minecraft.net TLS OK in 0.1s proto=Tls13 cert-subject=CN=resources.download.minecraft.net, O=Microsoft Corporation`；`OK index 5.json status=200 bytes=413067 in 1.04s`；`OK 18MB object: 17.34 MB in 2.3 s => 7.55 MB/s`；`maven.neoforged.net :443 reachable=False` |
| **E13** | NFRT 缓存是现成的大头 | `…\caches\neoformruntime\assets\` | `indexes\17.json`(449557 B)；objects `3888 files, 787.6 MB`；`present in NFRT cache: 3404`、`present in either: 3551 / 3575`、仅 24 个两边都没有 |
| **E14** | 构建本身通（问题不在移植代码） | `D:\project_farm1.20.1\probe\build-ok.log` | `> Task :compileJava` … `BUILD SUCCESSFUL in 13s` / `28 actionable tasks: 3 executed, 25 up-to-date`；该次**完全没出现 downloadAssets**（assemble 不触发它） |
| **E15** | 并发会直接打死构建 | `D:\project_farm1.20.1\probe\build-new.log` | `> Could not open file hash cache (D:\project_farm1.20.1\probe\.gradle\8.10\fileHashes).` / `> Cannot create directory …` / `BUILD FAILED in 2s` |

---

## 6. 仍未查清的点（如实列出，不要当成已解决）

1. **「卡在 DownloadAssets」这一原始陈述没有任何日志支撑**（E6）。真实停点证据（E7）指向配置阶段。
   要么原始观察有误，要么那份日志已被删除。**F-003 重跑时务必自己留 log（步骤 3/5 已带 Tee-Object）。**
2. **NG7 的 `downloadAssets` 是否真挂在 1.20.1 `runServer` 的依赖链上**：我用**同类服务端运行 `runData`** 的任务表
   （E8）做了强旁证，但**没有直接看到 1.20.1 runServer 的任务表**（那需要跑 gradle，被本次任务边界禁止）。
   步骤 3 可用 `--dry-run` 或实际任务表确认。
3. **`downloadAssets` 的 UP-TO-DATE 判定是否会把残缺缓存误判为「已完成」**：类里是 `Implements its own caching`（E8），
   理论上不该误判，但**未实测**。步骤 3 已备 `--rerun-tasks` 兜底。
4. **H2 的具体阻塞点**（若真发生）：是解析 `net.neoforged:forge:1.20.1-47.1.106` 还是拉取 `piston-meta` 版本清单——
   `daemon-18672` 尾部只有 banner，没有区分线索。需一次带 `--stacktrace` 的重跑 + 线程 dump 才能定位。
5. **耗时估算未在真机验证**：7.55 MB/s 是单对象 GET 测的；3575 个请求经 `WorkerExecutor` 并发下的实际总耗时
   可能明显偏离（小文件 RTT 占主导）。**给 F-003 的超时请按 15 分钟设。**
6. **1.20.1 的 `runServer` 是否真的需要 assets 到「全量 3575」**：服务端通常不加载贴图/音效，
   DownloadAssets 可能只需通过即可。**但不要据此偷工**——先让任务跑通，再谈裁剪。

---

## 7. 一句话交接

> 别再去证「网络为什么不通」——Mojang 侧实测 7.55 MB/s 完全正常，被 SNI 阻断的只有 maven.neoforged.net（代理已覆盖）。
> 真正的活儿是**把 1.20.1 的 `caches/minecraft/assets` 从 428/3575 补到 3575/3575**，
> 用插件自带的 `downloadAssets` 任务去补，并把超时从 4 分钟提到 15 分钟。
