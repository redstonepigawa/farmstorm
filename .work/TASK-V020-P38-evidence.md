# Phase 38（F-238 ~ F-240）实测证据

> 执行者：Phase 38 唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> 起服目录 `run-p38`（`FS_RUN_LOCK=run-p38`，`-PfsRunDir=run-p38`）；客户端目录 `run-p38c`。**未在 `run/` 里起服**，`run/config` 未动。
> 未做任何 git 写操作；本文所有 `git` 输出均为只读命令原文。

---

## 0. 总体

### 0.1 改动面（`git diff --stat` 原文）

```
 .../blockentity/CultivatorBlockEntity.java         |  9 +++++++
 .../blockentity/OutpostCoreBlockEntity.java        | 13 ++++++++++
 .../dashpig/farmstorm/client/FarmstormClient.java  | 27 ++++++++++++++++++-
 .../farmstorm/event/FarmstormGameEvents.java       |  9 +++++++
 .../farmstorm/network/FarmstormNetwork.java        | 30 +++++++++++++++++-----
 5 files changed, 80 insertions(+), 8 deletions(-)
```

新增（未跟踪，因此不在 `diff --stat` 里）四个文件：

```
?? src/main/java/com/dashpig/farmstorm/client/ClientCultivatorLinks.java
?? src/main/java/com/dashpig/farmstorm/client/CultivatorLinkRenderer.java
?? src/main/java/com/dashpig/farmstorm/network/CultivatorLinkBroadcaster.java
?? src/main/java/com/dashpig/farmstorm/network/CultivatorLinkPayload.java
```

`FarmstormGameEvents.java` 不在任务书 §1 的「允许改/增」清单里，但 **37 号 F-239 的「涉及文件」把它列进来**
（`:103` / `:114` / `:120` / `:128` 四个请求点 + `onServerTickPost` 的 tick 入口），且 §6 的验收 1/3/4 只能在这些点挂载。
处理：**只增不改**（+9 行，全是 `CultivatorLinkBroadcaster.` 的一行调用 + 1 行 import），登记为偏差 1。

### 0.2 构建（唯一入口 `cmd /c ""D:\project_farm\.tools\gw.cmd" build`）

| 阶段 | 命令 | 退出码 | 日志 |
|---|---|---|---|
| 改前基线 | `gw.cmd build` | **0** | `.work/p38-build-baseline.log` |
| F-238~F-240 首轮 | `gw.cmd compileJava` | **0** | `.work/p38-compile-A.log` |
| 同上（打全量） | `gw.cmd build` | **0** | `.work/p38-build-A.log` |
| 探针轮（待验证 ②） | `gw.cmd compileJava` | **0** | `.work/p38-compile-probe.log` |
| 日志带「刻」 | `gw.cmd build` | **0** | `.work/p38-build-B.log` |
| **空 handler 轮**（临时） | `gw.cmd build` | **0** | `.work/p38-build-emptyhandler.log` |
| **终稿**（还原 handler + 删探针） | `gw.cmd build` | **0** | `.work/p38-build-final.log` |

### 0.3 负向面（全部为「改后」读数）

| 判据 | 命令 | 读数 |
|---|---|---|
| 注册自检 | `Select-String run-E.log 自检 注册对象` | `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`（**未变**） |
| `CoreListPayload` 一字不动 | `git diff --stat -- network/CoreListPayload.java` | **空** |
| `farmland/` `registry/` `docs/` `build.gradle` `.tools/` `run/` | 一条 `git diff --stat --` 覆盖六处 | **空** |
| `@Mixin` / `mixins.json` | 全 `src` 递归计数 | **0** |
| `playToClient` | `FarmstormNetwork.java` 计数 | **2**（改前 **1**） |
| `playToServer` / `sendToServer` | 全 `src` 递归计数 | **0** |
| `CustomPacketPayload` 实现文件 | 全 `src` 去重 | 恰 **2**：`CoreListPayload.java`、`CultivatorLinkPayload.java` |
| `RANGE_CHUNKS` 定义 | `static final int RANGE_CHUNKS` 全仓计数 | **1**（只在 `OutpostCoreBlockEntity`） |
| `verify_project.py` | `python tools/verify_project.py` | `已核 registry 对象: 93 / 93`、`lang 键集一致，共 337 键`、`结论：FAIL 0 项，WARN 0 项`。**按 A-66 / 陷阱 21 本 Phase 不引用它的 `FAIL 0`**：它仍只核 93 个对象，四处同步属 F-243 |

---

## 1. F-238 · 连线包 + 注册 + 协议版本 2

### 1.1 payload 的 shape 与 codec（`network/CultivatorLinkPayload.java`，新建）

```java
public record CultivatorLinkPayload(List<Link> links) implements CustomPacketPayload {
    public static final int MAX_LINKS = 1024;
    public static final Type<CultivatorLinkPayload> TYPE = new Type<>(Farmstorm.id("cultivator_link"));

    public static final StreamCodec<FriendlyByteBuf, Link> LINK_STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, Link::core,
            BlockPos.STREAM_CODEC, Link::cultivator,
            Link::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, CultivatorLinkPayload> STREAM_CODEC = StreamCodec.composite(
            LINK_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINKS)), CultivatorLinkPayload::links,
            CultivatorLinkPayload::new);

    public record Link(BlockPos core, BlockPos cultivator) {}
}
```

- **不带 `total`、不带维度**：列表是按玩家过滤后的完整集合，永不截断；同维度的绑定才成立（36 号 §2.8.1）。
- `MAX_LINKS = 1024 = 64 × 16`，**只当解码上限**：`CultivatorLinkBroadcaster` 里没有任何 `Math.min(..., MAX_LINKS)`（`grep` 命中 0）。
- 一元 `StreamCodec.composite(listCodec, getter, ctor)` 重载已核对源码：`StreamCodec.java:112`（`<B, C, T1>` 版）。

### 1.2 `FarmstormNetwork` 三处原文

`git diff` 的关键三行（原文）：

```diff
-public static final String PROTOCOL_VERSION = "1";
+public static final String PROTOCOL_VERSION = "2";
...
+                .optional()
+                .playToClient(CultivatorLinkPayload.TYPE, CultivatorLinkPayload.STREAM_CODEC,
+                        (payload, context) -> ClientCultivatorLinks.handle(payload, context));
...
+        Farmstorm.LOGGER.info("自检 网络包注册 {}（版本 {}，S2C 只读，MAX_LINKS {}）→ OK",
+                CultivatorLinkPayload.TYPE.id(), PROTOCOL_VERSION, CultivatorLinkPayload.MAX_LINKS);
```

协议串与 codec **同一个提交**（`:17` 的版本串与 `:40-42` 的第二条 `playToClient` 同批落地）。

### 1.3 两行自检日志原文（`.work/p38-run-E.log`，终稿 build）

```
[11:10:24] [modloading-sync-worker/INFO] [farmstorm/]: 自检 网络包注册 farmstorm:core_list（版本 2，S2C 只读，MAX_CORES 64）→ OK
[11:10:24] [modloading-sync-worker/INFO] [farmstorm/]: 自检 网络包注册 farmstorm:cultivator_link（版本 2，S2C 只读，MAX_LINKS 1024）→ OK
```

**改前实测值**（`.work/p38-run-baseline.log`，基线代码）：

```
自检 网络包注册 farmstorm:core_list（版本 1，S2C 只读，MAX_CORES 64）→ OK      ← 只有这 1 行，版本串 1
```

行数 **1 → 2**，版本串 **1 → 2**，id 从 1 个变成 `farmstorm:core_list` + `farmstorm:cultivator_link`。

### 1.4 专用服务器

- 起服 **无 `NoClassDefFoundError`**：`Select-String run-*.log NoClassDefFoundError|ClassNotFoundException` → **0 行**（跑过的五个服务端日志 run-A ~ run-E 都数过）。
- 原版客户端进服：**未验证**（见 §6 未验证清单第 1 条）；代码层证据是两条 `playToClient` 都挂了 `.optional()`。

---

## 2. F-239 · 连线广播器：范围与五个时机

### 2.1 五个时机与挂载点（`grep` 可复核）

| # | 时机 | 挂载点（文件:方法） |
|---|---|---|
| 1 | 进范围 | `CultivatorLinkBroadcaster#tick` → 每 `RESCAN_INTERVAL_TICKS = 20` 刻 `rescan()`（指纹变则入待发集） |
| 2 | 出范围 | 同上；组装结果为空 ⇒ **发空列表**（不是不发） |
| 3 | 绑定变化（培养器放置 / 被拆 / 迁移改绑） | `OutpostCoreBlockEntity#addCultivator`、`#removeCultivator`、`CultivatorBlockEntity#releaseHeld` 各一句 `CultivatorLinkBroadcaster.requestAll(...)` |
| 4 | 核被拆 | `OutpostCoreBlockEntity#release()` 里紧跟 `CoreListBroadcaster.requestAll` 的一句 |
| 5 | 登录 / 换维度 / 重生 / 登出 | `FarmstormGameEvents`：`:108`（登录）、`:127`（换维度）、`:135`（重生）、`:120`（登出 `onLogout`）；tick 入口 `:186`（`onServerTickPost`，与 `CoreListBroadcaster.tick` 同处） |

### 2.2 常量

```java
public static final int MIN_INTERVAL_TICKS = 10;
public static final int RESCAN_INTERVAL_TICKS = 20;
```

### 2.3 范围判据只调用 `inRange`（没有第二份范围公式）

`CultivatorLinkBroadcaster.java` 里与范围相关的全部行（`grep 'inRange|RANGE_CHUNKS|>> 4'`）：

```
42:  * <p><b>范围判据只有一处</b>：{@link OutpostCoreBlockEntity#inRange(BlockPos, BlockPos)}（{@code RANGE_CHUNKS = 1}）。
174: * 服务端权威组装：... → **{@code inRange} 范围判定** → ...
206:                 if (!OutpostCoreBlockEntity.inRange(entry.pos(), viewerPos)) {
210:                     .getChunkNow(entry.pos().getX() >> 4, entry.pos().getZ() >> 4);
```

`:210` 的 `>> 4` 是**区块查询坐标**（`getChunkNow` 的参数），不是范围算式；本类里没有任何 `Math.max(Math.abs(...))` 形态的切比雪夫判据。
全仓 `static final int RANGE_CHUNKS` 计数 = **1**。

### 2.4 下发读数（全部取自服务端日志 `连线下发` / `连线组装`，行内带「刻」）

| 场景 | 读数（原文） |
|---|---|
| 客户端刚登录、范围内无核 | `连线下发：Dev 0 条（维度 minecraft:overworld，刻 34753）` |
| 站到核旁（范围判定成立） | 逐条绑定变化：`连线下发：Dev 1 条（…刻 35595）` → `连线下发：Dev 2 条（…刻 35636）`，N = 该核已绑定的培养器数（`data get block 204 200 204 Cultivators` → 2 条） |
| **走出范围**（`tp Dev 5000 100 5000`） | `连线下发：Dev 0 条（…刻 21380）` —— **空列表真的发出去了**（不是不发） |
| 走回范围 | `连线下发：Dev 2 条（…刻 21860）` |
| **拆一个培养器**（`setblock 202 200 200 minecraft:air destroy`） | `连线下发：Dev 1 条（…刻 22184）`；同一轮 `data get block 200 200 200 Cultivators` → `[55250460115144L]`（2 → 1） |

**「立刻」的刻级读数**（同一批 RCON，0.35 秒间隔，见 §5 的 RCON 间隔说明）：

```
125.823  >>> time query gametime  => The time is 22735
126.235  >>> setblock 201 200 200 minecraft:air destroy  => Changed the block at 201, 200, 200
126.645  >>> time query gametime  => The time is 22751
服务端：连线下发：Dev 0 条（维度 minecraft:overworld，刻 22744）
```

拆方块落在 22735～22751 之间、包落在 **22744**；`22744 % 20 = 4` ⇒ **不是 20 刻重扫的整点**，是事件驱动。
（对照：`22744 - 22735 = 9` 刻，其中 `time query` 与 `setblock` 之间本身隔了约 8 刻的 RCON 往返，即拆完 ≤1 刻就发包。）

### 2.5 去抖读数

**同刻连锁事件 ⇒ 只发 1 包**（最强的一处）：拆核触发 `settleOnRemoval`，一次迁移里连发 **3 次** `requestAll`
（2 次 `addCultivator` + 1 次 `release()`），全部落在同一刻 ⇒ 服务端只产生 **1** 个包：

```
[11:02:09] 连线下发：Dev 2 条（维度 minecraft:overworld，刻 26801）      ← 3 次请求合并成 1 包
```

**10 刻窗口确实生效**：随后 4 刻后又发生一次绑定变化（拆培养器），被压到 `上一次发送 + 10` 刻才发：

```
[11:02:09] 连线下发：Dev 1 条（维度 minecraft:overworld，刻 26811）      ← 26801 + 10
```

另一次同批（0 延时）两条 `setblock` 的读数：`刻 27340` 与 `刻 27350`（同样是 +10）。
两条命令落在相邻刻（RCON 单条往返约 60 ms ≈ 1.2 刻），因此是「1 包立即 + 1 包被推迟 10 刻」，
**不是**同一刻的两包 —— 判据「同一刻只发 1 包」由上面 26801 那一轮成立。

**不重复刷包**：`FINGERPRINTS` 与上一次发送比对，状态不变时 20 刻重扫不会重发。实测：`刻 25922` 发出 2 条之后，
在没有任何变化的 4 秒（80 刻）里 `连线下发` **0 行新增**。

### 2.6 `hasChannel` 与「未声明就不发」

不装模组的连接（mineflayer 机器人）没有声明这条 optional 通道，服务端只组装不发送：

```
连线组装（客户端未声明 farmstorm:cultivator_link，跳过发送）：ALICE 2 条（刻 25922）
```

---

## 3. F-240 · 客户端只读与线框渲染

### 3.1 两个客户端类

- `client/ClientCultivatorLinks.java`：`volatile List<Link>`，handler **只做 `enqueueWork`**；
  `clear()` 三个挂载点（登出 / 不在世界里 / 维度变化）。
- `client/CultivatorLinkRenderer.java`：`RenderLevelStageEvent` 的 `Stage.AFTER_PARTICLES`。

handler 原文（`ClientCultivatorLinks.java:33-35`）：

```java
    public static void handle(CultivatorLinkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> set(payload.links()));
    }
```

方法体里**没有任何渲染类引用**；服务端类路径上也从不解析 `ClientCultivatorLinks`（只在 lambda 体内出现）。

### 3.2 渲染代码片段（`CultivatorLinkRenderer.java`）

```java
    private static final float RED = 0.30F;
    private static final float GREEN = 0.85F;
    private static final float BLUE = 0.90F;
    private static final float ALPHA = 1.0F;
    private static final double ENDPOINT_HALF = 0.125;          // 0.25 格小框

    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        ...
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());
        PoseStack poseStack = new PoseStack();                   // 新建，**不用** event.getPoseStack()
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (CultivatorLinkPayload.Link link : links) {
            LevelRenderer.renderLineBox(poseStack, consumer,
                    new AABB(core.x, core.y, core.z, cultivator.x, cultivator.y, cultivator.z),
                    RED, GREEN, BLUE, ALPHA);
            LevelRenderer.renderLineBox(poseStack, consumer, endpoint(link.core()), RED, GREEN, BLUE, ALPHA);
            LevelRenderer.renderLineBox(poseStack, consumer, endpoint(link.cultivator()), RED, GREEN, BLUE, ALPHA);
        }
        buffers.endBatch(RenderType.lines());
    }
```

- `renderLineBox` 命中 3 处调用 + 1 处 javadoc 引用（`grep` 读数：`CultivatorLinkRenderer.java:24,74,77,78`）。
- **没有** `RenderType.create` / `defaultVertexFormat` / `addVertex` 的痕迹（`grep` 0）。
- 线宽 1.0 由 `RenderType.lines()` 自带（`RenderType.java:606` 的 `LineStateShard(OptionalDouble.empty())`，本机源码已核）。

### 3.3 `clear()` 的挂载点（`FarmstormClient.java`）

| 时点 | 代码 |
|---|---|
| 登出 / 换服 | `onLoggingOut` 里 `ClientCultivatorLinks.clear(); lastDimension = null;` |
| 逐刻兜底（不在世界里） | `onClientTick` 的 `level == null` 分支 |
| **换维度** | `onClientTick` 里比对 `level.dimension()` 与 `lastDimension`，变了就 `clear()`（连线包不带维度，只能由客户端自己记） |

### 3.4 待验证 ②：`AFTER_PARTICLES` + `RenderType.lines()` 可画性（先画固定坐标线）

按 36 号 §9 ② / 37 号表 F ② 的先后：先加一条**固定坐标**的探针线（`(200.5,200.5,200.5) → (202.5,200.5,200.5)`，不依赖任何网络包）并打出一次性日志，
确认这条渲染路径能跑通、再让正式逻辑接管：

```
[10:54:25] [Render thread/INFO] [farmstorm/]: 自检 固定坐标线已绘制（AFTER_PARTICLES + RenderType.lines，待验证 ②）
```

同一客户端进程内此后继续画真实连线（同一 `getBuffer(RenderType.lines())` + `renderLineBox` 调用链），
`Missing elements in vertex` / `IllegalStateException` 各 **0** 条 ⇒ **无需**退到 `AFTER_TRIPWIRE_BLOCKS`。
探针在终稿前整段删除：`grep -n 'PROBE\|probeLogged'` 在两个客户端类里 **0 命中**，`gw.cmd build` 退出码 0。

### 3.5 客户端收到的包（`run-p38c/logs` 与 stdout 重定向日志）

```
[11:11:33] [Render thread/INFO] [farmstorm/]: 收到连线 0 条
[11:12:14] [Render thread/INFO] [farmstorm/]: 收到连线 1 条
[11:12:16] [Render thread/INFO] [farmstorm/]: 收到连线 2 条
```

与服务端 `连线下发` 的条数、刻一一对应。

### 3.6 空 handler 那一轮（36 号 §8 #34 / 验收 7）

做法：把 `ClientCultivatorLinks#handle` 临时改成 `context.enqueueWork(() -> { });`（用 `// EMPTY-HANDLER-START/END` 夹住），
`gw.cmd build` 退出码 **0**，重启 `run-p38` 后跑四条线（**服务端侧读数，不需要客户端**）：

| 线 | 操作 | 读数（原文） |
|---|---|---|
| ① 绑定 | 机器人放核心 `(215,200,215)` + 培养器 `(216,200,215)` | 核 `Cultivators: [59373628780744L]`；培养器 `{Progress: 535, Held: {count: 1, id: "farmstorm:puffed_wheat"}, Core: 59098750873800L}` |
| ② 产出 | 600 刻后 | 核 `Items: [{count: 2, Slot: 0b, id: "farmstorm:puffed_wheat"}, {count: 2, Slot: 1b, id: "farmstorm:puffed_wheat_seeds"}]`；培养器 `{Progress: 0, Core: …}`（`Held` 键消失） |
| ③ 迁移 | `setblock 215 200 215 minecraft:air destroy`（无接替核） | 日志 `迁移失败：培养器留原地并标为未绑定 BlockPos{x=216, y=200, z=215}`；培养器 NBT `{Progress: 0, x: 216, y: 200, z: 215, id: "farmstorm:cultivator"}` —— **无 `Core` 键**，出口② |
| ④ 拆核掉落 | 核里放 5 钻石后 `setblock … destroy` | `Outpost Core has the following entity data: {count: 1, id: "farmstorm:outpost_core"}` / `Diamond has the following entity data: {count: 5, id: "minecraft:diamond"}` |

同一轮服务端照常组装连线（`连线组装（客户端未声明 …）：ALICE 1 条`），说明**服务端逻辑不依赖客户端是否收到包**。

**还原读数**：

```
$ git status --porcelain -- src/main/java/com/dashpig/farmstorm/{client,network,blockentity,event}
 M src/main/java/com/dashpig/farmstorm/blockentity/CultivatorBlockEntity.java
 M src/main/java/com/dashpig/farmstorm/blockentity/OutpostCoreBlockEntity.java
 M src/main/java/com/dashpig/farmstorm/client/FarmstormClient.java
 M src/main/java/com/dashpig/farmstorm/event/FarmstormGameEvents.java
 M src/main/java/com/dashpig/farmstorm/network/FarmstormNetwork.java
?? src/main/java/com/dashpig/farmstorm/client/ClientCultivatorLinks.java
?? src/main/java/com/dashpig/farmstorm/client/CultivatorLinkRenderer.java
?? src/main/java/com/dashpig/farmstorm/network/CultivatorLinkBroadcaster.java
?? src/main/java/com/dashpig/farmstorm/network/CultivatorLinkPayload.java
```

**没有** `EMPTY-HANDLER` / `PROBE` 残留（`grep` 0 命中），终稿 `gw.cmd build` 退出码 0。

### 3.7 不崩（10 分钟在线，终稿 build）

```
$ Select-String .work/p38-client-G.log -SimpleMatch 'Setting user'
[11:11:20] [Render thread/INFO] [minecraft/Minecraft]: Setting user: Dev
$ Select-String .work/p38-client-G.log -SimpleMatch 'Missing elements in vertex' | Measure-Object
Count = 0
$ Select-String .work/p38-client-G.log -SimpleMatch 'IllegalStateException' | Measure-Object
Count = 0
$ (Get-Process java | Where-Object { $_.MainWindowTitle -like '*Minecraft NeoForge*' } | Measure-Object).Count
1                    ← 读数时刻 11:22:29，进程仍在（在线 11 分 09 秒）
```

期间客户端一直在画 2 条真实连线（`收到连线 2 条` 之后没有新的清空），即「每帧走一次
`getBuffer(RenderType.lines())` + 3 次 `renderLineBox` + `endBatch`」的路径连续执行 11 分钟无异常。
日志里出现 `Exception` 字样的 8 行全部与本模组无关：5 行是 oshi 的 `COM exception querying Win32_Processor`、
1 行是 `sessionserver.mojang.com` 读超时（无外网）、1 行是 PowerShell 重定向产生的 `NativeCommandError` 文本。

### 3.8 终稿轮的端到端复核（探针已删、handler 已还原）

```
服务端：[11:12:14] 连线下发：Dev 1 条（维度 minecraft:overworld，刻 35595）
服务端：[11:12:16] 连线下发：Dev 2 条（维度 minecraft:overworld，刻 35636）
客户端：[11:12:14] 收到连线 1 条 / [11:12:16] 收到连线 2 条
```

---

## 4. 复现跑法

```powershell
# 1) 起服（不要在 run/ 里起）
New-Item -ItemType Directory run-p38 -Force
Copy-Item run\eula.txt,run\server.properties run-p38\
$env:FS_RUN_LOCK='run-p38'
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-p38" *> .work\p38-run-E.log
# 2) 客户端（quickPlay 必须走 init 脚本注入：moddev 每次启动都会重写 clientRunProgramArgs.txt）
$env:FS_RUN_LOCK='run-p38c'
cmd /c D:\project_farm\.work\p38-client-B.cmd *> .work\p38-client-G.log
#    首次进入 run-p38c 会卡在「Narrator」引导页 —— 按 ESC 即可关闭并继续 quickPlay。
# 3) 摆场：核 / 培养器必须**由玩家放置**（setblock 不走 setPlacedBy ⇒ 不绑定）。
#    本机 mineflayer 的物品名映射在 1.21.1 是坏的（SlotComponent 解析报错），因此用 hotbar 槽位 + rawuse 原始 block_place 包：
#    item replace entity ALICE hotbar.0 with farmstorm:outpost_core 1
#    bot: slot 0 / rawuse <x> <y-1> <z> 1
# 4) 读数：.work/p38r.py（每命令间隔 0.7s）、.work/p38-tick.py（可调间隔，用于刻级取证）
```

---

## 5. 文档–源码偏差表

| # | 文档口径 | 源码 / 实测 | 处置 |
|---|---|---|---|
| 1 | 任务书 §1「允许改/增」只列 `outpost/OutpostCoreBlockEntity.java` 或 `blockentity/CultivatorBlockEntity.java`（二选一），未列 `event/FarmstormGameEvents.java` | 37 号 F-239 的「涉及文件」明确写了 `FarmstormGameEvents.java:103,114,120,128` + tick 入口；验收 1/3/4 只能在这些点挂 | 改 `FarmstormGameEvents`，**只增不改**（+9 行，5 处一行调用 + 1 行 import）。登记 |
| 2 | 任务书 §1「或」（两选一） | 两处各加了一行触发：核侧（`addCultivator` / `removeCultivator` / `release`）+ 培养器侧（`releaseHeld`） | 拆培养器时核侧**没有**任何调用点（`removeCultivator` 全仓 0 调用），只挂核侧就满足不了验收 3「拆培养器立刻消失（不等 20 刻重扫）」。两处都是纯新增一行 `requestAll`。登记 |
| 3 | 36 号 §2.8.2 把「培养器被拆」并入时机 #3，触发点写成 `OutpostCoreBlockEntity.setOwner` / `release` | 拆培养器只走 `CultivatorBlock#onRemove` → `CultivatorBlockEntity#releaseHeld` | 触发点补在 `releaseHeld`（唯一可达点）。同一节其余四条时机的挂载点与文档一致 |
| 4 | 36 号 §2.8.2「去抖 10 刻」；`CoreListBroadcaster` 的实现是「距上次发送 ≥10 刻才发」 | 本类沿用同一口径 | 语义是**推迟**不是丢弃：10 刻窗口内的第二次变化会在 `+10` 刻以合并后的状态发出（读数 26801 → 26811）。验收「同一刻只发 1 包」由 3 次 requestAll 合并成 1 包那轮成立 |
| 5 | 任务书 §5「自检行补到**两行**」 | 实现为两条独立 `LOGGER.info`（不是一行拼两个 id） | 36 号 §8 #2 的判据是「两行 `自检 网络包注册`」，按行数可数 |
| 6 | 36 号 §2.8.3「换维度后由 `clear()` 清」，未说检测方式 | 客户端没有「维度变化」事件（`ClientPlayerNetworkEvent` 只有 LoggingIn / LoggingOut） | 在 `FarmstormClient.onClientTick` 比对 `level.dimension()` 与 `lastDimension`，变了就 `clear()` |
| 7 | 任务书 §3「本机可以做：起 dev 客户端看日志」 | 实测可以做，但 `run-p38c` 首次启动会卡在 Narrator 引导页（`options.txt` 的 `onboardAccessibility:false` 未生效），必须按一次 ESC | 操作注意，已写进 §4 复现跑法；不影响判据 |
| 8 | — | moddev 每次启动都会**重写** `build/moddev/clientRunProgramArgs.txt`，直接往里写 quickPlay 参数会被覆盖 | 用额外的 init 脚本在 `runClient.doFirst` 追加（`.work/p38-quickplay.gradle`），且追加前必须补一个换行（生成的文件末尾没有换行） |
| 9 | — | mineflayer 在 1.21.1 的物品名 / 背包映射是坏的（`SlotComponent` 解析 `PartialReadError`），`equip` / `place` 不能用 | 改用 `item replace entity … hotbar.N` + `rawuse`（原始 `block_place` 包）。纯工具面，不改产品代码 |
| 10 | — | 我手动把 `run-p38/config/farmstorm-server.toml` 的 `cycleTicks` 改成 100，服务端启动时按 spec 重写回 600 | 预期行为；空 handler 轮的产出是 600 刻后发生的（读数里 `Progress` 从 535 → 0、`Held` 清空），不影响结论 |
| 11 | 任务书 §1 允许清单里写的是 `outpost/OutpostCoreBlockEntity.java` | 实际路径是 `blockentity/OutpostCoreBlockEntity.java`（`outpost/` 下只有索引与票） | 按源码为准改的 `blockentity/` 那个；登记 |

---

## 6. 未验证清单（每条给原因）

| # | 项 | 原因 |
|---|---|---|
| 1 | 纯原版客户端能进服（`optional()` 通道） | 本机只有 dev 客户端与 mineflayer 机器人，没有原版 1.21.1 客户端。代码层证据：两条 `playToClient` 都挂了 `.optional()` |
| 2 | 线的**视觉**：可见、两端 0.25 格小框、颜色 RGB 0.30/0.85/0.90、线宽 1.0 的实际观感 | 人眼类判据，本机做不到（无人工观察环节）。代码层证据见 §3.2；运行期证据只有「该渲染路径每帧执行 11 分钟不抛异常」 |
| 3 | 非己方玩家看不到线（双客户端对照） | 只有 1 个 dev 客户端；己方判定复用 `Farmstorm.TEAMS.isAlly`（与 `core_list`、边界粒子同一口径），未做双客户端实测 |
| 4 | 光影类模组下 `VIEW_OFFSET_Z_LAYERING` 的表现（36 号 §9 ② 后半） | 本机无光影环境 |
| 5 | 20 人服下的重扫开销 | 只跑过 1~2 个玩家；每 20 刻 × 玩家数 × 核数的 `getChunkNow` 是纯哈希查表，未压测 |
| 6 | 原版客户端收到本包（未装模组）时的行为 | 未装模组的连接根本不声明该 channel，服务端走 `hasChannel == false` 分支（已用 mineflayer 实测该分支）。原版客户端连服后是否会因未知 play 包报错，未验 |

---

## 7. 未关闭的问题

1. **「同一刻」在 RCON 下不可精确构造**：单条命令往返约 60 ms ≈ 1.2 刻。因此去抖的「同刻 1 包」证据取自
   代码路径上**必然同刻**的连锁（一次 `settleOnRemoval` 里 3 次 `requestAll` → 1 包，`刻 26801`），而不是 RCON 连发。
2. **去抖是「推迟」不是「丢弃」**：10 刻窗口内的第二次变化会在 `+10` 刻以合并后的状态发出（`26801` → `26811`，`27340` → `27350`）。
   这沿用了 `CoreListBroadcaster` 的既有口径；若期望「窗口内完全静默」，需要改成丢弃语义。
3. `CultivatorBlockEntity#releaseHeld` 里的那句广播请求：这是「拆培养器」唯一能挂的服务端点。
   若将来把核侧 `removeCultivator` 接到 `CultivatorBlock#onRemove`，这一句可以搬回核侧（本版不动）。
4. `RenderType.lines()` 的 output state 是 `ITEM_ENTITY_TARGET`，在「高品质画质」下会走不同的后处理目标；
   本机只跑过默认画质，未在 Fabulous 模式下验观感（属 §6 第 2 条的同族）。
5. `分项小账`：本 Phase 没有新增任何 config 键与语言键（`toml` 仍 175 键 / 60 段、lang 仍 337 键），
   这是预期（网络包与渲染都不进注册面、不要语言键）；F-241 / F-242 的总数收口不受本 Phase 影响。

---

## 8. 完成定义对照（任务书 §6 的 8 条）

| # | 判据 | 结论 | 落点 |
|---|---|---|---|
| 1 | 两个包与协议串 | **有读数** | §1.3 两行自检原文；`playToClient` 1→2；`PROTOCOL_VERSION` 1→2 |
| 2 | 纯原版客户端 / 专用服务器 / `@Mixin` 0 | 部分：专用服务器无 `NoClassDefFoundError` ✅、`@Mixin` 0 ✅、原版客户端**未验证** | §0.3 / §1.4 / §6-1 |
| 3 | 下发与清残留 | **有读数** | §2.4（N 条 / 空列表 / 拆培养器 1 刻内） |
| 4 | 去抖 | **有读数**（同刻 3 请求 → 1 包；+10 刻窗口生效） | §2.5 / §7-1 |
| 5 | 渲染（人眼类） | **未验证** + 代码层证据 | §3.2 / §6-2 |
| 6 | 不崩（10 分钟） | **有读数**：`Missing elements in vertex` 0、`IllegalStateException` 0，在线 11 分 09 秒 | §3.7 |
| 7 | 服务端零依赖（空 handler） | **有读数**（绑定 / 产出 / 迁移 / 拆核掉落四条线全照常，已还原） | §3.6 |
| 8 | 注册面不动 | **有读数**：`自检 注册对象 97 个 … menu_types 2 → OK` | §0.3 |
