# Phase 13 / Phase 14 并行作业规则（两个 lane 共用）

> 依据：`docs/zh/13-TASKS-0.0.2.md` §8「并行度分析」。
> 文档原文：簇 C（Phase 13，F-78/F-82/F-83/F-84/F-77）理论上限 5；簇 D（Phase 14，F-91/F-92）为 2；
> 但 §8.3 明确 **实际受同文件冲突约束**，稳定并行按 **3 路**算，且点名了冲突文件：
> `F-79/F-82/F-83/F-84/F-91/F-92 都改 client/FarmstormClient.java`。
>
> 本文件把「怎么在同一个工作区里安全并行」写死，两个 lane 一字不改地照此执行。

## 1. 两条 lane

| Lane | 承担的 Phase | 任务顺序（串行链内不要跳） |
|---|---|---|
| **A** | Phase 13 | F-76 → F-77 / F-78 / F-82 / F-83 / F-84（这五个可并行推进，但同一 lane 内仍按顺序做）→ F-79 → F-80 → F-81 → **F-85 出口回归** |
| **B** | Phase 14 | F-86 → F-87 → F-88 → F-89 / F-90 → F-91 / F-92 → **F-93 出口回归** |

上游前置都已就位：F-63（`DeployedKind.ZANGIEF_CANE`）、F-65（方块与物品）、F-64（两个效果）、F-61（22 个 config 键）、F-66（两个投掷物）均已完成。

## 2. 文件归属（最重要的一张表）

### 2.1 调度者独占，**任何 agent 都不许改**

| 文件 | 为什么独占 |
|---|---|
| `client/FarmstormClient.java` | 两条 lane 都要往里挂东西，是唯一真正的写入热点 |
| `Farmstorm.java` | 自检文案与注册挂载，改错会影响全局启动 |

**落地协议**：你新建自己的类（例如 `client/GrayscalePass.java`、`client/SilhouetteRenderer.java`、`client/CameraHandler.java`、`client/InputLockHandler.java`、`client/LoopSoundController.java`…），
然后在回报里**逐字写出**需要加到这两个文件里的行（含 import），例如：

```
在 client/FarmstormClient.java 的 registerRenderers/构造处追加：
  NeoForge.EVENT_BUS.addListener(GrayscalePass::onRenderGui);
  NeoForge.EVENT_BUS.addListener(SilhouetteRenderer::onRenderLiving);
```

调度者会统一接线并提交，**不要自己动手**。

### 2.2 Lane A（Phase 13）拥有

- `client/` 下**除 `FarmstormClient.java` 之外**的一切（新建类、超类、工具）
- `effect/WildHunterEffect.java`（属性加成与食用授予相关调整）
- `entity/` 下与索敌/位移相关的既有类（F-78 / F-79 / F-80 需要时）
- 客户端资源：`assets/farmstorm/shaders/core/**`、`post_effect/**`、`textures/misc/**`（F-82 灰度用）

### 2.3 Lane B（Phase 14）拥有

- `behavior/impl/ZangiefCaneTrap.java`（F-86 起把骨架换成真实实现）
- `effect/ZangiefPiledriverEffect.java`（相位语义相关）
- `registry/FarmstormBlocks.java`（F-86 的 `deployed(ZANGIEF_CANE, …)` 调整）
- `client/` 下**相机与输入锁的新类**（F-91 / F-92）：`client/CameraHandler.java`、`client/InputLockHandler.java` 等

### 2.4 谁都不许动

- `docs/**`（上游文档，回写由调度者统一做）
- `tools/gen_textures.py`、`src/generated/**`（见 §3 禁止 runData）
- 对方的文件：Lane A 不碰 `behavior/impl/ZangiefCaneTrap.java`；Lane B 不碰 `effect/WildHunterEffect.java`
- `datagen/**`（Phase 15 才动）

> **需要别人文件时的唯一正确做法**：停下来，在回报里写「我需要改 X，理由是 Y」，**不要偷偷改**。

## 3. 硬规矩

### 3.1 禁止 `runData`

`src/generated` 与 HashCache 是**全仓唯一**状态，两条 lane 同时生成必然互相踩。
Phase 13 / 14 的任务都是代码改动，不需要新生成物。
**真需要生成物时停下来问调度者**，由调度者串行跑。

### 3.2 运行目录与端口固定分配

| Lane | 运行目录 | server-port | rcon.port |
|---|---|---|---|
| A | `run-p13` | **25631** | **25641** |
| B | `run-p14` | **25651** | **25661** |

一律用 `-PfsRunDir=<自己那份>`。**绝不要用别人的目录或端口**，也不要用默认的 `run/`。

### 3.3 不 commit

两条 lane 都不提交。调度者按任务做**路径限定提交**，保证「一个任务一次提交」。
因此**不要把无关文件的改动混进来**——混进来会让提交无法拆分。

### 3.4 每次收尾必须清场，但**只清自己的**

> ⚠️ **禁止 `Get-Process java | Stop-Process -Force` 这种一刀切**（V-37）。
> 这台机器上同时跑着另一条 lane 的服务端/客户端，加上 Gradle daemon，一刀切会把**别人的进程**杀掉。
> 症状极具迷惑性：对方看到的是「客户端/服务端无崩溃栈、无 crash report，直接 exit -1」，会误判成自己的代码问题。
> v0.0.2 施工期真发生过：Lane A 的客户端两次「18:04 还在收聊天回显，18:06 就 No entity was found」。

**正确做法：起进程时记下 PID，收尾只杀那个 PID。**

```powershell
# 起服务端/客户端时用 Start-Process 拿 PID，或起完立刻记录：
$srv = Start-Process -PassThru -FilePath ...   # 或从任务管理器记下
# 收尾：只杀自己的
Stop-Process -Id $srv.Id -Force -ErrorAction SilentlyContinue

# 服务端优雅停机优先用 RCON stop，不要靠 kill
```

**只有在你确认全机没有别的 lane 在跑时**（先 `Get-Process java` 看一遍），才允许清理残留；即便如此也要先看清窗口标题（`Minecraft server` / `Minecraft NeoForge*`）判断归属。

锁文件同理：默认锁是 `.tools/runserver.lock`，你自己的 lane 锁是 `.tools/runserver-<FS_RUN_LOCK>.lock`——**只删自己那把**。

### 3.5 探针必须能自我关闭，且必须删净

- 临时探针若需要每次开服自动跑，**必须**加一个开关文件（参考 F-67 的 `run-*/f67-probe.enabled`），默认不跑。
- 交付前自查三处：`src/` 里 `Probe` 计数、`build\classes` 下 `*Probe*` 文件数、jar 内 `Probe` 条目数，**都必须为 0**。
- 注意：`LANDING_PROBE`、`landingBlock` 是**既有玩法常量/方法**，不算探针，别误删。

### 3.6 证据写在 `.work/`，不要只留在运行目录

`run-*/logs/latest.log` **重启就会轮转**（F-67 的结果就是从 `2026-10-29-1.log.gz` 里抢救回来的）。
每条验收的原文都要落到 `.work/<任务号>-evidence.md` 与配套 `.log`。

## 4. 客户端验证：现在**能进世界**了（Phase 13 必须用它）

Phase 13 的灰度与剪影是**渲染**功能，必须给**截图级证据**，不接受「代码看起来对」。

```powershell
# 0) 先起一台服务端（自己 lane 的目录与端口）
# 1) 让客户端直连进世界：用现成的 init script
$env:FS_QUICKPLAY_PORT = "25631"    # 换成自己 lane 的端口
cmd /c ""D:\project_farm\.tools\gw.cmd" runClient -PfsRunDir=run-p13 `
     --init-script "D:\project_farm\.work\client-quickplay.gradle" --console=plain
```

**判据**（别再把主菜单误判成卡死）：
- 主菜单：窗口标题是 `Minecraft NeoForge* 1.21.1`，日志**不再新增**（这是正常的）。
- 进世界：窗口标题变成 `… - Multiplayer (3rd-party Server)`，服务端日志出现 `joined the game`。
- `jstack` 在本机被沙箱拒绝，不要指望线程栈。

截图用 `Screenshot.takeScreenshot(...)` 或直接截窗口；**没有图就不算验证**。`--args` 无效、`--quickPlaySingleplayer` 无效（会卡无障碍引导页）。

## 5. 已知坑清单（直接照做，不要重新踩）

| 编号 | 坑 | 正确做法 |
|---|---|---|
| V-16 | `/forceload add <x> <z>` 是**方块坐标**不是区块坐标 | 用 `forceload add 96 96 112 112` 这种写法；否则测试区不加载、统计全 0 |
| V-17 | 冻结实体会随区块加载**复活**，污染复跑 | 脚本开头 `kill` 三连 + 静置 + 清场后基线断言；污染就删 `run-*/world` 重跑 |
| V-19 | `setBlock` 的 update flag | `onPlace` 与 flag 无关（`LevelChunk:283` 无条件调），`blockUpdated` 才受 `flags & 1` 控制；只写空气位时 `UPDATE_CLIENTS` 足够，别用 `UPDATE_ALL` 触发连锁 |
| V-24 | 经验：扣的是**点数**不是等级 | `/xp query @s points` 才是判据；`levels` 可能不变 |
| V-15 | 代理失败会留下带 `@EventBusSubscriber` 的探针 | 见 §3.5 |
| V-27（本轮新） | `Player#getDigSpeed` 有 `if (!onGround()) f /= 5.0F` | 用 FakePlayer 测挖掘/交互前先 `setOnGround(true)` |

## 6. 出口回归（各自 lane 的最后一件）

- **F-85（Lane A）**：Phase 13 出口。灰度/剪影/索敌/冲刺/续时五条都要有截图或日志级证据；`build` exit 0；三条自检（77 / 19 种 / 16 种）仍在；`sounds.json` 里的循环音能播。
- **F-93（Lane B）**：Phase 14 出口。三个相位按时序切换、相机 roll 180 + pitch 取反 + yaw 自转、输入锁只留 ESC（F11/F2/F3 拦不住属已知限制）、触发后不掉落（A-08）。

## 7. 冲突发生时的处理

1. 发现想改的文件不在自己名下 → **停**，回报「需要 X，理由 Y」。
2. 发现对方刚改了你依赖的文件 → **停**，回报「依赖 X 的最新状态，请调度者确认」。
3. 发现工作区有你没写过的改动 → **停**，不要回滚、不要覆盖，回报即可（可能是另一个 lane 在写）。

> 之前真发生过一次：调度者自己改了三处，某代理的 `edit` 报 `old_string not found`，差点被当成工具故障排查。**看到不认识的改动，默认是另一个 lane 干的，先问。**
