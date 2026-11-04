# 证据 · farmstorm v0.2.1 · Phase 41（F-248 ~ F-250）

> 执行者：本会话（Phase 41 唯一执行者）。工作目录 `D:\project_farm`（Windows / pwsh）。
> 基线：Phase 40 已提交（`52497c4`）、`mod_version=0.2.1`、build exit 0、`OutpostCoreBlockEntity` 782 行（UTF-8）。
> 行号一律用 `[IO.File]::ReadAllLines($p, [Text.Encoding]::UTF8)` 取（默认 `Get-Content` 会读成 668 行）。
> 本次**未做任何 git 写操作**：只跑了 `git status` / `git diff`。

---

## 0. 结论

| 任务 | 判据 | 结果 |
|---|---|---|
| F-248 | `getChunkNow` 1 / `getChunk(` 0 / `getChunkAt(` 0 / `getBlockEntity(` 0 / `new ArrayList` 2 / `type.isInstance` 1 / `BlockEntityType` 0 | 通过 |
| F-248 | build exit 0、Java 文件数 145 → 146 | 通过 |
| F-249 | `REBIND_INTERVAL_TICKS = 100`（:98）；`rebindDue` 模数走该常量、体内无小写 `interval`、无 `interval <= 0` 守卫 | 通过 |
| F-249 | `rebindNearby` 首行 `ownerUuid == null` 返回；`pruneCultivators(`/`removeCultivator(`/`cultivators()` 各 0；`addCultivator(` 1、`setCore(` 1 且前者先出现；`comparingDouble` 1 + `thenComparingInt` 3（x/y/z） | 通过 |
| F-249 | 全仓 `pruneCultivators(` 调用点仍 1（:336，在 `cultivators()` 里）；`removeCultivator(` 调用点仍 0 | 通过 |
| F-250 | 独立 `if`、在粒子门之前、不 `return`；`now` 上移一行 | 通过 |
| F-250 | 粒子间隔 **40** 下 100 刻内补上 | 通过（实测） |
| F-250 | 粒子间隔 **1200** 下仍 100 刻内补上（**成对**） | 通过（实测） |
| F-250 | 满 16 即停（20 个候选）+ 再等 300 刻仍 16 | 通过（实测） |
| F-250 | `/setblock` 无主核 + 1 个未绑定 ⇒ 300 刻仍为空 | 通过（实测） |
| F-250 | `CultivatorBlock` 里「不自动认领」/「不会绑上」命中 0；`setPlacedBy` diff 空；该类仍 162 行 | 通过 |

---

## 1. 改动面（三段提交请按 F-248 / F-249 / F-250 切）

```
$ git -C D:\project_farm status --porcelain
 M src/main/java/com/dashpig/farmstorm/block/CultivatorBlock.java
 M src/main/java/com/dashpig/farmstorm/blockentity/OutpostCoreBlockEntity.java
?? src/main/java/com/dashpig/farmstorm/util/AreaBlockEntities.java      ← F-248 新增（未 add 时不计入 diff --stat）

$ git -C D:\project_farm diff --stat
 .../dashpig/farmstorm/block/CultivatorBlock.java   |  6 +-
 .../blockentity/OutpostCoreBlockEntity.java        | 84 +++++++++++++++++++++-
 2 files changed, 86 insertions(+), 4 deletions(-)
```

- `src/main/java` 的 `.java` 计数：**145 → 146**。
- build：`cmd /c ""D:\project_farm\.tools\gw.cmd" build` → **exit 0**（`BUILD SUCCESSFUL in 9s`，最后一跑全 up-to-date）。
- F-249 的 84 行插入里，`REBIND_INTERVAL_TICKS` 6 行 + `rebindDue` 12 行 + `rebindNearby` 55 行（含 F-250 的 `added` 计数与 INFO 4 行）。
- 仅 `git diff` 的 141 行全文在 `.work/_p41_diff.txt`。

---

## 2. F-248 段 · `util/AreaBlockEntities.java`（新，52 行）

四类 grep 计数（`Get-Content -Raw` + `[regex]::Matches`，UTF-8）：

| 模式 | 命中 |
|---|---|
| `getChunkNow` | **1**（唯一调用点，第 44 行） |
| `getChunk(` | **0** |
| `getChunkAt(` | **0** |
| `getBlockEntity(` | **0** |
| `new ArrayList` | **2**（结果列表 + 活映射副本） |
| `type.isInstance` | **1** |
| `BlockEntityType` | **0** |

实现要点（全文见源码）：

```java
public static <T extends BlockEntity> List<T> collect(ServerLevel level, BlockPos center, int chunkRadius,
                                                      Class<T> type) {
    List<T> found = new ArrayList<>();
    int centerChunkX = center.getX() >> 4;
    int centerChunkZ = center.getZ() >> 4;
    for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
        for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
            if (chunk == null) {
                continue;                             // 不在内存：跳过，不写状态、不记日志
            }
            for (BlockEntity candidate : new ArrayList<>(chunk.getBlockEntities().values())) {
                if (type.isInstance(candidate)) {
                    found.add(type.cast(candidate));
                }
            }
        }
    }
    return found;
}
```

- 两阶段：先把 `chunk.getBlockEntities()` 的活映射**复制**进新 `ArrayList`（`:46`），再在副本上过滤 → 遍历期间别处增删不会 `ConcurrentModificationException`。
- 只走 `ServerChunkCache#getChunkNow`（`:42`），取不到即 `continue`，不加载区块（RT-52）。
- 不排序、不缓存：排序归 F-249，缓存会让「写入即占位」失效。

---

## 3. F-249 段 · 常量 + 两个方法

### 3.1 常量（`OutpostCoreBlockEntity:96-98`，紧邻 `:91` 的 `TICK_LOG_ROUNDS`）

```java
/**
 * 补绑周期（F-249 / D-176）：100 刻一轮。**编译期常量、不是 config 键**（D-185：本版 config 只 +1），
 * 因此不存在「配成 0」的可能，补绑的相位门也就不需要边界粒子门那条非零守卫。
 */
private static final int REBIND_INTERVAL_TICKS = 100;
```

### 3.2 `rebindDue`（`:507-517`）

```java
private static boolean rebindDue(BlockPos pos, long now) {
    return Math.floorMod(now + Math.floorMod(pos.hashCode(), REBIND_INTERVAL_TICKS),
            REBIND_INTERVAL_TICKS) == 0;
}
```

- 方法体（`:514-517`）内小写 `interval` 命中 **0**（区分大小写实测）；`REBIND_INTERVAL_TICKS` 出现 2 次。
- 没有 `interval <= 0` 守卫（常量取不到 0；守卫是粒子门的事，两套不互借）。

### 3.3 `rebindNearby`（`:519-572`）

```java
private void rebindNearby(ServerLevel server) {
    if (this.ownerUuid == null) {                          // ← 方法体第一行
        return;                                            // 裁定 #13：/setblock 出来的核没有归属者
    }
    int cap = FarmstormConfig.outpostCoreCultivatorCapacity.get();
    if (cultivatorCount() >= cap) {
        return;                                            // 满员：这一轮连枚举都不做
    }
    int added = 0;
    List<CultivatorBlockEntity> candidates =
            AreaBlockEntities.collect(server, this.getBlockPos(), RANGE_CHUNKS, CultivatorBlockEntity.class);
    candidates.sort(Comparator
            .comparingDouble((CultivatorBlockEntity candidate) -> candidate.getBlockPos().distSqr(this.getBlockPos()))
            .thenComparingInt(candidate -> candidate.getBlockPos().getX())     // ① x
            .thenComparingInt(candidate -> candidate.getBlockPos().getY())     // ② y（漏了就不稳）
            .thenComparingInt(candidate -> candidate.getBlockPos().getZ()));   // ③ z
    for (CultivatorBlockEntity candidate : candidates) {
        if (cultivatorCount() >= cap) {
            break;                                         // 每绑一个重读：满即停
        }
        if (candidate.corePos() != null) {
            continue;                                      // 已绑（含指向别的核）⇒ 跳过，不夺
        }
        if (hasCultivator(candidate.getBlockPos())) {
            continue;                                      // 核侧已有这一条 ⇒ 跳过（两侧不一致只跳过，不摘）
        }
        if (addCultivator(candidate.getBlockPos())) {
            candidate.setCore(this.getBlockPos());          // 先核侧、后培养器侧
            added++;
        }
    }
    if (added > 0) {
        Farmstorm.LOGGER.info("补绑：核 {} 本轮新增 {} 条（累计 {}/{}，周期 {} 刻）",
                this.getBlockPos(), added, cultivatorCount(), cap, REBIND_INTERVAL_TICKS);
    }
}
```

方法段（`:519-572`，含 javadoc）实测计数：

| 模式 | 命中 |
|---|---|
| `pruneCultivators(` | **0** |
| `removeCultivator(` | **0**（连裸名 `removeCultivator` 也是 0） |
| `cultivators()` | **0** |
| `addCultivator(` | **1** |
| `setCore(` | **1**（文本位置在 `addCultivator(` 之后） |
| `comparingDouble` | **1** |
| `thenComparingInt` | **3**（x → y → z，逐级点名） |

### 3.4 全仓两个方法名的调用点（负向）

```
pruneCultivators(   → OutpostCoreBlockEntity.java:336（调用点，在 cultivators() 里，原 :329，因 +7 行下移）
                      OutpostCoreBlockEntity.java:387（声明）
removeCultivator(   → OutpostCoreBlockEntity.java:373（只有声明；CultivatorLinkBroadcaster:35 的 javadoc 写法不带括号）
```

→ `pruneCultivators(` 调用点 **1**、`removeCultivator(` 调用点 **0**，与本 Phase 之前一致。

---

## 4. F-250 段 · 接线 + INFO + `CultivatorBlock:73-84`

### 4.1 `serverTick` 改前（原 `:507-525`，UTF-8 行号）

```java
public static void serverTick(Level level, BlockPos pos, BlockState state, OutpostCoreBlockEntity core) {
    if (!(level instanceof ServerLevel server)) {
        return;
    }
    int interval = FarmstormConfig.outpostCoreBoundaryParticleIntervalTicks.get();
    if (interval <= 0) {
        return;                                       // 防 floorMod 除零；config 范围 20~1200 取不到 0
    }
    long now = server.getGameTime();
    if (Math.floorMod(now + Math.floorMod(pos.hashCode(), interval), interval) != 0) {
        return;
    }
    core.emitBoundaryRing(server, pos, now, interval);
    // F-198 的可观测面：每 30 轮（interval = 40 刻 ⇒ 1200 刻 ≈ 60 秒）一条，证明「无玩家靠近时方块实体仍在 tick」。
    long round = Math.floorDiv(now + Math.floorMod(pos.hashCode(), interval), interval);
    if (Math.floorMod(round, TICK_LOG_ROUNDS) == 0) {
        Farmstorm.LOGGER.info("核心 tick 轮次 {}：{}（间隔 {} 刻，区块票 farmstorm_core）", round, pos, interval);
    }
}
```

### 4.2 `serverTick` 改后（`:584-607`）

```java
public static void serverTick(Level level, BlockPos pos, BlockState state, OutpostCoreBlockEntity core) {
    if (!(level instanceof ServerLevel server)) {
        return;
    }
    long now = server.getGameTime();                         // ← 原 :515 上移（纯取数、无副作用）
    // 补绑自带一套相位门（F-250 / D-176）：独立 if、放在边界粒子门**之前**、不 return。
    // 接在粒子门之后会被筛成 LCM(40, 100) = 200 刻，并随 boundaryParticleIntervalTicks 漂移。
    if (rebindDue(pos, now)) {
        core.rebindNearby(server);
    }
    int interval = FarmstormConfig.outpostCoreBoundaryParticleIntervalTicks.get();
    if (interval <= 0) {
        return;                                       // 防 floorMod 除零；config 范围 20~1200 取不到 0
    }
    if (Math.floorMod(now + Math.floorMod(pos.hashCode(), interval), interval) != 0) {
        return;
    }
    core.emitBoundaryRing(server, pos, now, interval);
    ...（日志段一字未改，仍用同一个 now）
}
```

同时给 `serverTick` 的 javadoc 补了一句「四件事的顺序（早退 → 补绑 → 粒子门 → 粒子 + 日志），两道门之间没有 return 交叠」。

### 4.3 补绑 INFO 的完整原文（服务端日志逐字）

```
[01:58:59] [Server thread/INFO] [farmstorm/]: 补绑：核 BlockPos{x=0, y=-60, z=0} 本轮新增 1 条（累计 1/16，周期 100 刻）
[01:59:29] [Server thread/INFO] [farmstorm/]: 补绑：核 BlockPos{x=200, y=-60, z=0} 本轮新增 1 条（累计 1/16，周期 100 刻）
[02:00:54] [Server thread/INFO] [farmstorm/]: 补绑：核 BlockPos{x=200, y=-60, z=0} 本轮新增 1 条（累计 2/16，周期 100 刻）
[02:01:25] [Server thread/INFO] [farmstorm/]: 补绑：核 BlockPos{x=200, y=-60, z=0} 本轮新增 1 条（累计 3/16，周期 100 刻）
[02:02:05] [Server thread/INFO] [farmstorm/]: 补绑：核 BlockPos{x=400, y=-60, z=0} 本轮新增 16 条（累计 16/16，周期 100 刻）
[02:08:59] [Server thread/INFO] [farmstorm/]: 补绑：核 BlockPos{x=2400, y=-60, z=0} 本轮新增 1 条（累计 1/16，周期 100 刻）
```

三要素齐：核坐标 `BlockPos{x=.., y=.., z=..}` + 本轮新增条数 + 累计/上限（另附周期）。本轮无新增时不打日志。

### 4.4 `CultivatorBlock:73-84` 注释 diff（`git diff`，其余一字未动）

```diff
@@ -81,3 +81,3 @@ public class CultivatorBlock extends BaseEntityBlock {
-     * <p><b>没有可用核时方块照样放下</b>，但永久处于未绑定态、不可使用，且**不自动认领**
-     * ——事后在旁边补一个核也不会绑上（D-165 补充 / 35 号 §12 B-12、B-13）。刻意的：
-     * 补绑会让「我放的时候有没有核」这件事失去意义。
+     * <p><b>没有可用核时方块照样放下</b>，此刻处于未绑定态、不可使用；但**不再永久如此**——核每
+     * 100 刻会扫一遍 3 × 3 已加载区块，把「没人要」的培养器补进自己的名额（F-250 / D-176、D-177）。
+     * 补绑只补未绑定的、不夺已绑的，也不改变「放下时有没有核」这件事的判定。
```

- 旧说法「不自动认领」命中 **0**、「不会绑上」命中 **0**；`CultivatorBlock.java` 总行数仍 **162**。
- `setPlacedBy`（`:85-102`）的 ``git diff`` 为空：`:99` `if (core.addCultivator(pos)) {` / `:100` `cultivator.setCore(core.getBlockPos());` 逐字未变。
- 注释块仍是 12 行（`:73-84`），`*/` 仍在 `:84`，逐行边界对齐（A-71）。

---

## 5. 运行时读数（专用服务端 `run-p41`，`FS_RUN_LOCK=run-p41`）

环境：`gw.cmd runServer -PfsRunDir=run-p41`（复制 `run/eula.txt` + `run/server.properties`；端口 25641 / RCON 25642；superflat，`level-seed=410041`）。
**测量方法统一用 `/tick freeze` + `/tick step N`**，这样「100 刻」是精确刻数而不是墙钟估算；世界除这几步外不推进。

### 5.1 夹具（必须交代的口径）

本机**没有可用客户端**（no player），因此：
- 核用 `/setblock <x> -60 0 farmstorm:outpost_core` 放，再用 `/data merge block <x> -60 0 {ownerUuid:[I;0,0,0,1],ownerName:"RCON"}` 写入归属——`DeployedOwnership.write/readUuid` 用的就是 `ownerUuid` 这个 NBT 键（`putUUID` = int[4]），`data merge` 会走 `loadAdditional` 把它读回字段；这与「玩家放置时 `setOwner` 写同一字段」是同一个判据位（`rebindNearby` 第一行只看 `this.ownerUuid`）。
- 因为没走 `setOwner`，`OutpostCoreIndex` / `CoreChunkTickets` 没被登记 ⇒ 用 `/forceload add` 把 3 × 3 区块钉在内存里（**比核自有票更难**：区块不是靠核的票加载的，但`getChunkNow` 一样取得到）。
- 培养器一律 `/setblock` / `/fill` 放置（无 placer ⇒ 不经过 `setPlacedBy`，天然处于未绑定态）。

### 5.2 间隔 40（默认值）· 核 (200,-60,0)

```
/tick freeze            → time query gametime = 3438
/setblock 206 -60 0 farmstorm:cultivator
/data get block 200 -60 0 Cultivators → []            （2 条旧绑定之外的这一条还没绑）
/tick step 100
time query gametime = 3538                             （= 3438 + 100，精确 100 刻）
/data get block 200 -60 0 Cultivators → [55525337206724L, 56075093020612L, 56624848834500L]
                          解码 = (202,-60,0) (204,-60,0) (206,-60,0)
/data get block 206 -60 0 Core        → 54975581392836L = (200,-60,0)   ← 两侧都写
```

→ 冷启动 100 刻内补上（这一轮实际是在 100 刻内某一刻触发，见 `:100` 那一步的 40 刻版本读数）。

### 5.3 间隔 1200（判决性）· 核 (2400,-60,0)

配置：`run-p41/config/farmstorm-server.toml` 的 `boundaryParticleIntervalTicks = 1200`（**无 BOM 写入**），启动后复查：
- `Select-String -Pattern 'Failed to load config'` = **0**（配置被接受，未被重建）；
- 启动后文件仍是 `boundaryParticleIntervalTicks = 1200`，mtime 停在启动前（02:07:38）——配置没有被回写覆盖；
- 同一跑里 `核心 tick 轮次` 命中 **0**（1300 刻内）：对比间隔 40 的跑，同样的刻数会在 round 30 打出一条「间隔 40 刻」。这就是「运行期确实用的是 1200 而不是 40」的行为侧证据。

```
/tick freeze            → time query gametime = 7307
/setblock 2400 -60 0 farmstorm:outpost_core + data merge ownerUuid
/setblock 2402 -60 0 farmstorm:cultivator
/data get block 2400 -60 0 Cultivators → []           （核绑 0 个）
/tick step 100
time query gametime = 7407                             （= 7307 + 100）
/data get block 2400 -60 0 Cultivators → [660256732483524L] = (2402,-60,0)
/data get block 2402 -60 0 Core        → 659706976669636L = (2400,-60,0)
/tick step 1200 → time query gametime = 8607
/data get block 2400 -60 0 Cultivators → [660256732483524L]（仍是 1 条，无重复、无多绑）
```

→ 间隔 1200 时仍然 **100 刻内**补上（若复用粒子门，这里要等 1200 刻才可能补）。与 5.2 **成对**。

> ⚠️ 口径事故（如实登记）：第一次把配置改成 1200 时用了 `Set-Content -Encoding UTF8`，写出的文件带了 UTF-8 BOM，NightConfig 解析失败：
> `Failed to load config farmstorm-server.toml: ParsingException: Invalid bare key: '﻿[converter'. Attempting to recreate`，
> 服务端把配置**重建回 40**。那一跑（核 x=1200 / 1400 / 1800 三条）因此**不能算 1200 的证据**，只能算间隔 40 的重复读数
> （三颗全新核同样 100 刻内补上：(1202,-60,0) / (1402,-60,0) / (1802,-60,0)，各自的 `Core` 分别指回 1200/1400/1800）。
> 改用 `[IO.File]::WriteAllText(..., UTF8Encoding($false))` 重写后按 5.3 重测。

### 5.4 满则停 · 核 (400,-60,0)，20 个未绑定 + 容量 16

```
/tick freeze
/forceload add 384 -16 431 31
/setblock 400 -60 0 farmstorm:outpost_core + data merge ownerUuid
/fill 402 -60 0 421 -60 0 farmstorm:cultivator      → Successfully filled 20 block(s)
time query gametime = 3824
/data get block 400 -60 0 Cultivators → []
/tick step 100 → time query gametime = 3924
/data get block 400 -60 0 Cultivators → 16 条：
[110500918595524L, 110775796502468L, 111050674409412L, 111325552316356L, 111600430223300L,
 111875308130244L, 112150186037188L, 112425063944132L, 112699941851076L, 112974819758020L,
 113249697664964L, 113524575571908L, 113799453478852L, 114074331385796L, 114349209292740L,
 114624087199684L]
   解码 = (402..417, -60, 0)，恰是「距离升序 → x → y → z」的前 16 名；(418..421,-60,0) 未绑。
/tick step 300（另加解冻后继续跑）→ time query gametime = 4064，再查一次：
/data get block 400 -60 0 Cultivators → 同上 16 条，一字未变
日志：补绑：核 BlockPos{x=400, y=-60, z=0} 本轮新增 16 条（累计 16/16，周期 100 刻）
```

### 5.5 无主核不补 · 核 (800,-60,0)（`/setblock` 放，没写 owner）

```
/tick freeze
/setblock 800 -60 0 farmstorm:outpost_core
/setblock 803 -60 0 farmstorm:cultivator
/data get block 800 -60 0 ownerUuid    → Found no elements matching ownerUuid
/data get block 800 -60 0 Cultivators  → []
/tick step 300 → time query gametime = 4626
/data get block 800 -60 0 Cultivators  → []            （仍是空）
/data get block 803 -60 0 Core         → Found no elements matching Core   （培养器侧也没写）
```

---

## 6. 负向面

| 检查 | 结果 |
|---|---|
| `FarmstormConfig.java` diff | **空**（`git status` 不含该文件）；`b.push(` = **60**、`b.pop()` = **60**、行首 `.define*` = **175**，与改前一致 |
| `registry/` `network/` `client/` `menu/` `docs/` `build.gradle` diff | **空**（`git status --porcelain` 对这几个路径零输出） |
| `CultivatorBlock` 的 `setPlacedBy`（`:85-102`） | diff **空** |
| `CultivatorBlock.java` 总行数 | **162**（未变） |
| Mixin | 未新增（本 Phase 不碰 `mixins`） |
| 网络包 | `PROTOCOL_VERSION` 未动、`network/` diff 空 ⇒ 本版仍是 2 个包，不新增 |
| 全仓 `pruneCultivators(` 调用点 | **1**（`OutpostCoreBlockEntity:336`） |
| 全仓 `removeCultivator(` 调用点 | **0**（只有 `:373` 的声明） |
| `git` 写操作 | **0 次**（只跑 `status` / `diff`） |

---

## 7. 文档-源码偏差表

| # | 文档说法 | 源码/实测 | 处置 |
|---|---|---|---|
| 1 | 45 号 `:329` 是 `pruneCultivators()` 调用点 | 本 Phase 在该行之前加了 import 1 行 + 常量 6 行 ⇒ 现为 `:336` | 只记偏差，不改 `docs/`；调用点数量仍是 1 |
| 2 | 43 号 `:366` 是 `removeCultivator` 声明 | 现为 `:373`（同上位移） | 同上 |
| 3 | 45 号 `serverTick:507-525` | 改前确为 507-525；改后为 584-607（前面既有插入） | 行号随改动位移，属预期 |
| 4 | 42 号 §2.1.3 给的 `rebindNearby` 样板不含日志 | 本实现按 A-71 多一条 INFO 与一个 `added` 计数 | 文档已要求 INFO（43 号 F-250 / §5.2 A-71），样板只是省略 |
| 5 | 45 号 §2.4 第 7 条要求 `CultivatorBlock` 的「该类总行数仍 162」 | 实测 162 | 一致 |

---

## 8. 未验证清单（一条一行，写明原因）

1. **未加载区块跳过**：没做「把培养器放进 3×3 边缘区块再卸载」的现场验证。做法须 `forceload remove` 或走远 >6 区块让区块卸载，再等 ≥100 刻并观察服务端日志无该区块的加载行——本轮所有 3×3 都用 `/forceload add` 钉住，因此这条只由源码纪律（`getChunkNow` 取不到即 `continue`，不写状态不记日志）保证。
2. **同 3×3 内两个核各配 1 个未绑定培养器**时两条 INFO 的 `gameTime` 是否相等（42 号 §7 #8）：未构造该场景，本节不下结论。
3. **`ConcurrentModificationException` 的现场触发**：未做「遍历时并发增删」的对抗测试；只有静态证据（`new ArrayList` 复制活映射）。
4. **玩家真实放置路径下的补绑**（`setPlacedBy` + `OutpostCoreIndex` + 核自有票全链路）：本机无客户端，夹具用 `/setblock` + `/data merge ownerUuid` + `/forceload` 替代（见 5.1）。
5. **客户端可见面**：补绑不新增包、不新增同步面，未做客户端读数。

---

## 9. 未关闭的问题

1. `rebindNearby` 只按 3×3 区块收候选，**不**对候选再做 `inRange` 的 Chebyshev 复核——与 `AreaBlockEntities` 的区块半径天然等价（同一组 `RANGE_CHUNKS`），但若将来有人在 `AreaBlockEntities` 里加半径常量，这里会失配。记一笔，不在本 Phase 处理。
2. 满员后每 100 刻仍会走一次「读容量 → 直接 return」，不枚举；成本可忽略，但这条路径没有任何日志，因此「满则停」只能靠 `/data get block` 侧读数（本轮已测）。
3. `/tick step` 是**异步**推进的（命令立即返回，服务端在后续循环里跑完 N 刻），读数必须在 step 之后等待；本轮用 SLEEP + `time query gametime` 复核了每步的实际刻数，未按命令返回值当刻数。
