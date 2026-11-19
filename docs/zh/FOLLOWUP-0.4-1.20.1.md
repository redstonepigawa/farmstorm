# FOLLOWUP · 1.20.1 遗留登记（v0.4 起）

> **用途**：v0.3.0 收口时明确「不做、但要记住」的项登记在这里，避免它们只以代码注释的形式存在。
> 每条给：现状 / 风险 / 触发条件 / 做法 / 判定依据。作者裁定日期：2026-11-15（v0.3.0 收口）。
> 代码里的对应注释一律写「移植缺口 / 移植差异 / 移植债（F-xxx）」而不是 `TODO`，
> 因为 `tools/verify_project.py` §4 会把 `// TODO` 当成禁令命中（判据见该脚本 §4）。

## FU-01 · 实体附件（AI 挂起）升级为 Capability —— **作者裁定：v0.4 处理**

- **现状**：`registry/FarmstormAttachments.java` 把「AI 挂起」存进 `Entity#getPersistentData()` 的裸键
  `FarmstormAiSuspend`，序列化仍走 `MobAiSuspension.Suspend#CODEC`（字段形状与 1.21.1 附件一字不差）。
  语义已对齐：只活在服务端与存档、死亡不复制、`sources == 0` 时整键删除。
- **风险（按严重度）**：
  1. **无版本协商**：`getAiSuspend` 解析失败走 `.result().orElse(NONE)` —— 将来改一次 codec 形状，老存档里的
     生物会**静默恢复 AI**（无日志、无报错）。
  2. **键名无命名空间**：`getPersistentData()` 是所有 mod 共用的 CompoundTag，与他模同名键会互相覆盖。
  3. **读-改-写非原子**：单 tick 串行安全；异步或多宿主并发会丢更新。
  4. **实体转换路径**（僵尸 → 溺尸等）下持久 NBT 是否随行取决于原版实现，Capability 的
     `copyOnDeath` / `invalidate` 钩子才可控。
- **做法**：`RegisterCapabilitiesEvent` 注册 → `ICapabilitySerializable<CompoundTag>` 实现 → 读写点换成
  `mob.getCapability(...)`；迁移钩子读旧 NBT 键一次并回写新通道，老存档不丢挂起状态。
- **验收建议**：旧存档加载后挂起状态仍在；改 codec 形状后老存档给出显式迁移日志而不是静默 NONE；
  两处并发写在压力测试下不丢计数。
- **影响面**：`MobAiSuspension`（14 处调用）+ `FarmstormAttachments`（4 处）。

## FU-02 · 视角锁（F-013）—— **作者已接受当前实现，不改**

- **现状**：`client/InputLockHandler#onTurn` 挂 `ViewportEvent.ComputeCameraAngles`，锁定期间把相机
  yaw/pitch 钉回上一帧。1.20.1 没有 `CalculatePlayerTurnEvent`，拦不到 `MouseHandler → LocalPlayer.turn`。
- **可感后果**（已知且接受）：解锁瞬间视角回弹；人物朝向与画面不同步（服务端 yaw/pitch 照转）；
  做不到真正的「灵敏度 0」。
- **若将来要修**：需要 Mixin 或核心模组 —— 与本项目「Mixin 0」铁律冲突，故不建议。
  同样的能力限制还覆盖 F11 全屏 / F2 截图 / F3 调试（类注释已写明，验收不算缺陷）。

## FU-03 · 爆竹「风弹雨」剩余差距（F-012）

- **已修（v0.3.0 收口）**：`BambooFirecrackerProjectile` 的爆炸威力此前写死 `1.2F`，而 `EXPLOSION_POWER = 0.6F`
  （作者要求的减半）自 `33e5da1f` 起从未被引用 —— 已改为引用该常量，实际威力回到 0.6。
- **仍缺**：
  1. **击退修正**：原版风弹有专门的定向推 + 浮空推；这里用通用爆炸击退，推动距离/方向与之不同。
  2. **gust 粒子**：1.20.1 无对应粒子，风弹雨在视觉上是「看不见的推动」，玩家判断不了覆盖范围（PvP 信息面）。
- **做法建议**：以 `level.sendParticles` 起一层近似粒子（可用 `EXPLOSION`/`POOF` 组合）；
  击退若要贴近原版，需要在爆炸后自行对半径内实体施加一次定向冲量（不要改 `Explosion` 本体）。
- **验收建议**：与 1.21.1 并排录屏对比推动距离；粒子在昼夜与远距离下都可见。

## FU-04 · 检查器 §1 的 6 个 `trade.*` 键（WARN）

- **现象**：`docs/zh/02-TECH-1.20.1.md` §2.1 的键清单按主线写法是逻辑路径 `trade.common.min`，
  落盘是组内拼写 `trade.commonMin`（`b.push("trade")` + `defineInRange("commonMin")` ⇒ `trade.commonMin`）。
  脚本用 `doc_key_variants` 形状归一后判为 `[WARN]` 而不是 FAIL —— 属既有的文档-源码表述差。
- **两种收口**：① 把清单里这 6 个键改写成落盘拼写；② 保留逻辑路径、把该 WARN 改成「已登记偏差」不再计数。
- **触发条件**：只有这 6 个键；新增键不受影响。

## FU-05 · 两处注释计数陈旧（v0.3.0 之外的旧账）

- `registry/FarmstormItems.java` 的 `// ---- v0.0.3 新增的 4 条 ----` → 该段实际 **3** 条。
- `registry/FarmstormBlocks.java` 的 `// ---- v0.0.3 新增的 3 个方块 ----` → 该段实际 **1** 条。
- 都只影响可读性，不影响构建与产物。

## FU-06 · 运行时判据的补测清单（08 号 §8 的 #1~#32）

v0.3.0 期间 `runServer` 曾因**过期 classpath 缓存**起不来（详见 FU-07），下面这些项当时只能记「未测」。
服务端现已可正常启动（`Done (11.074s)`、两行「版本 2」自检、生成 175 键 `farmstorm-server.toml`），
但这些仍必须**玩家实体**才能测：

- **面板类**：#6 右键开面板 / 0 槽、#7 `/data get block … Mode` 流转与跨重启、#26 未绑定按钮置灰、状态行。
- **投料类**：#8 相邻两次投料 gameTime 差恒 20、#9 核 64→63 与机器槽 1、#10 名额满 24、#11 超 3×3 区块。
- **挖掘/投掷类**：#13 木镐掉 0、#14 石镐掉 1、#19 金镐只记录、#15 史诗四件各投入 1 次、
  #16~#17 膨胀麦干草块掉 0 与压缩麦掉 1、#32 `.dat` 跨重启条数一致。
- **渲染类**：#12 绿线像素 `0x4D/0xD9/0x4D`。

可脚本化的那部分（`/setblock` + `/data merge block` + `/data get` + RCON）不需要人，可按需补做。

## FU-07 · 工具链与环境须知（本机实测，非代码问题）

1. **构建入口**：`cmd /c .work\toolchain\build-local.cmd [args]`。工具链是从 `D:\project_farm\.tools`
   逐字节复制进 workspace 的（`gradle-home` 48 528 文件 / jdk21 / gradle-8.10 / init.gradle），
   因为 DSH 沙箱对工作区外只读，而 Gradle 必须写缓存、native lib、daemon 日志与 TEMP。
2. **镜像代理**：`127.0.0.1:8899` 必须存活（`node .work\toolchain\neoforge-proxy.mjs`）。
   掉线时不仅依赖解析失败，`runServer` 连配置阶段都会以 `userdevClasspathElementProducer is null` 报 NPE。
3. **过期缓存坑**：`runServer` 报 `java.lang.module.ResolutionException: Module fml_loader reads another
   module named fml_loader` 时，删 `.gradle\configuration\neoForm\<那串>\steps\writeMinecraftClasspathServer\`
   再重跑即可 —— 那份 `classpath.txt` 还指着旧的只读工具链。
4. **datagen**：`build-local.cmd --nd runData`。NeoGradle 默认 data run 不带 `--mod`/`--all`，
   会在 `build.gradle` 的 `runs { data { … } }` 里补（日志出现 `Initializing Data Gatherer for mods []`
   就是这条没生效，provider 零调度、零产物）。
5. **检查器**：`python -X utf8 tools/verify_project.py`（本机 `python` 是 WindowsApps 占位 stub，
   用 DSH 自带的 3.x）。§1 的键清单已冻结在 `docs/zh/02-TECH-1.20.1.md` §2.1，本仓自洽；
   主线 `D:/project_farm/docs/zh` 存在时只做交叉核对。
## FU-08 · 控制终端面板永远显示「未绑定」（v0.3.0 实测缺陷，**已修**）

- **症状**（玩家实测）：终端与据点核心绑定成功后，右键打开的面板仍显示「未绑定」，两个模式按钮始终置灰。
- **根因（两条，缺一不可）**：
  1. **客户端读不到**：客户端菜单由 `MenuType` 的 `MenuSupplier` 构造，`ContainerLevelAccess` 是 `NULL`，
     旧实现把 `ContainerData#get` 改成经 `access.evaluate` 现读 → 客户端恒返回 0。
  2. **服务端发不出**：旧实现把 `ContainerData#set` 写成空实现；而原版同步链路是
     `DataSlot#checkAndClearUpdateFlag`（服务端比对 `get` → 发包）→ 客户端收到后**用 `set` 回填**。
     空实现把收到的值当场丢掉，于是两格永远是 0。
  （08 号 §4.6 原文「两格的 `set` 都是空实现」就是这条缺陷的出处，已在同处加收口修正。）
- **修法**：两格改成普通 `SimpleContainerData(2)`；`ControlTerminalMenu` 覆写 `broadcastChanges()`，
  在 `super` 之前经 `access.evaluate` 把 `mode().ordinal()` 与「核侧是否认这一格」写进两格。
  客户端那份 `access` 是 `NULL`，写格那一步在客户端自动跳过，只保留收到的值。**不新增网络包**（仍是 2 个）。
- **配套日志**（`ControlTerminalBlock#setPlacedBy`，便于下次一眼分辨故障点）：
  - `控制终端 <pos> 已绑定据点核心 <corePos>` —— 绑定成功；
  - `控制终端 <pos> 未绑定：附近没有可用据点核心` —— `selectCore` 没选中（范围 / 归属 / 队伍）；
  - `控制终端 <pos> 绑定失败：据点核心 <corePos> 名额已满或该格已登记` —— 核侧拒收。
- **复核方式**：放一个终端 → 看 `logs/latest.log` 出现哪一行；再看面板状态行。
  若日志是「已绑定」而面板仍显示「未绑定」，说明问题在屏幕侧（当前屏幕每帧读 `menu.bound()`，应已拿到同步值）。
- **注意**：本轮只修了「状态到不了客户端」。若日志是「未绑定：附近没有可用据点核心」，
  那是**另一个**问题（选核判据），需另开一条。
### FU-08 补记（2026-11-19 晚，同一缺陷的第二个坑）

修「状态到不了客户端」时又踩了一个**自己在 `broadcastChanges()` 里引入的崩溃**，一起记下来：

- **现象**：右键终端 → 面板仍显示「未绑定」，且 `latest.log` 里出现
  `Failed to handle packet ServerboundUseItemOnPacket, suppressing error` +
  `java.lang.NullPointerException` → `Optional.of` → `ContainerLevelAccess$2.m_6721_`：
  `ControlTerminalMenu.m_38946_(ControlTerminalMenu.java:105)`。
- **原因**：`ContainerLevelAccess.create(level, pos)` 的 `evaluate` 内部是 **`Optional.of(...)`**（不是
  `ofNullable`），所以传给它的 lambda **绝不能返回 null**。修法里那个写两格状态的 lambda 写成 `return null;`
  就在 `openMenu → addSlotListener → broadcastChanges` 时当场炸，服务端菜单没建起来，客户端两格仍是 0。
- **正确写法**：lambda 返回一个非 null 的哑值（现用 `Boolean.TRUE`）；
  或改成 `access.evaluate(fn)` 的单参形态 + `ifPresent`。
- **判据**：全仓 `access.evaluate(...)` 只有三处（`OutpostCoreMenu:95` 返回 core/fallback、
  `ControlTerminalMenu:78` 返回 boolean、`ControlTerminalMenu:105` 现返回 `Boolean.TRUE`），都非 null。
- **日志取证**（玩家实测，1.20.1-Forge 47.4.28）：
  `控制终端 -8, 56, 49 面板状态：Mode=OFF corePos=BlockPos{x=-11, y=56, z=49} 核方块实体在=true 核认这一格=true`
  —— 这一行同时证明**绑定链路完全正常**，问题只在面板显示层。
