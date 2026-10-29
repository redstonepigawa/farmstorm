# F-70 证据 · 球内候选枚举与逐格掷火 / 岩浆块

HEAD: `08a57ec`（F-69 提交后，**未 commit**）｜改动 **1 个文件**：`entity/thrown/MolotovProjectile.java`（+91 / −1）
服务端: `-PfsRunDir=run-f70`（全新世界 flat / seed 680068 / rcon 25610）｜无客户端（本轮验收不需要玩家）

## 0. 交付物

```
$ git diff --stat
 .../farmstorm/entity/thrown/MolotovProjectile.java | 92 +++++++++++++++++++++-
 1 file changed, 91 insertions(+), 1 deletion(-)
```

```java
@Override
protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
    this.trigger(level, pos);
    this.scatter(level, this.landingBlock(pos, directHit == null));
}

/** DESIGN §3.4：命中方块时落点方块 = 命中面内侧方块；命中实体时退化为命中点所在方块。 */
private BlockPos landingBlock(Vec3 pos, boolean blockHit) {
    if (!blockHit) {
        return BlockPos.containing(pos);
    }
    Vec3 into = pos.subtract(this.position());
    if (into.lengthSqr() < 1.0E-6) {
        return BlockPos.containing(pos);
    }
    return BlockPos.containing(pos.add(into.normalize().scale(LANDING_PROBE)));
}

private void scatter(ServerLevel level, BlockPos center) {
    double radius = FarmstormConfig.molotovEffectRadius.get();
    double radiusSqr = radius * radius;
    int reach = (int) Math.ceil(radius);
    List<BlockPos> candidates = new ArrayList<>();
    for (int dx = -reach; dx <= reach; dx++) {
        for (int dy = -reach; dy <= reach; dy++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx * dx + dy * dy + dz * dz > radiusSqr) {
                    continue;
                }
                BlockPos cell = center.offset(dx, dy, dz);
                if (!level.getBlockState(cell).isSolidRender(level, cell)) {
                    continue;
                }
                if (!level.getBlockState(cell.above()).isAir()) {
                    continue;
                }
                candidates.add(cell);
            }
        }
    }
    double fireChance = FarmstormConfig.fireChance();
    RandomSource random = level.getRandom();
    int fire = 0;
    int magma = 0;
    for (BlockPos cell : candidates) {
        boolean isFire = random.nextFloat() * 100.0F < fireChance;
        BlockState state = isFire ? Blocks.FIRE.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState();
        level.setBlock(cell.above(), state, WRITE_FLAGS);
        if (isFire) {
            fire++;
        } else {
            magma++;
        }
    }
    Farmstorm.LOGGER.info(String.format(Locale.ROOT, "莫洛托夫 候选 %d 火 %d 岩浆 %d",
            candidates.size(), fire, magma));
}
```

### 0.1 block update flag：选 `Block.UPDATE_CLIENTS`（任务书要求说明理由）

`private static final int WRITE_FLAGS = Block.UPDATE_CLIENTS;` —— **与 TECH §3.7 判据 4 写的 `UPDATE_ALL` 不同**，
按任务书「不要逐格 `UPDATE_ALL` 触发大范围连锁」的要求改动，理由三条：

1. **没有邻居需要重算**：本方法只往「原本是空气」的位置写，候选方块自身一个字节都不动。存活 / 流动 / 掉落 / 附着
   这些依赖邻居状态的判定，没有任何一条会因这次写入而改变；`UPDATE_NEIGHBORS` 在这一发里是纯开销。
2. **免得放大成连锁**：一次投掷是「几百格各写一次」的批量写入（本轮实测单发最多 115 次）。逐格 `UPDATE_ALL` 会把
   每次写入放大成 6 面向邻居更新 + 一次 `neighborChanged` 派发；岩浆块还是**实心方块**，通知邻居就会拉起红石 /
   流体 / 重力方块的连锁判定——这正是任务书要避免的那类副作用。
3. **客户端同步仍然要**：`UPDATE_CLIENTS` 保证火与岩浆块当场可见，不必等下一次区块刷新。

光源 / 方块实体这些是 `Level#onBlockStateChange` 走的，不受 flag 影响；`setBlock` 的 `onPlace` 也照常执行。

### 0.2 三处与文档字面不同、需要记一笔的地方

| 项 | 文档字面 | 本轮做法 | 依据 |
|---|---|---|---|
| 球心 | DESIGN §5.6 判据 2：判定原点取**落点精确坐标** `landingVec` | 取**落点方块**（整数格），`dx²+dy²+dz² ≤ r²` | 任务书把判据指到 `12-TECH §3.7` 判据 1，那里写的就是 `center.distSqr(pos) <= r*r`；「球内约 268 格」这个数也只对整数球心成立。取舍见 §1 的实测：候选数与独立复算完全一致 |
| 候选判据 | DESIGN §5.7 条件 B：`isFaceSturdy(level, pos, Direction.UP)` | `isSolidRender(level, pos)` | 任务书铁律 2 与 `12-TECH §3.7` 判据 2 都点名 `isSolidRender`。台阶 / 雪层在两种判据下都是「非实心」，本轮实测两者行为一致（§2） |
| 球内格数 | `12-TECH §3.7` 写「球内约 268 格」 | 整数格实测 **257**（`d² ≤ 16`：1+21+37+45+49+45+37+21+1） | 268 是文档的粗估；本轮的「单发写入上限」以实测为准 |

概率键：只读 `molotov.fireChance`（走 F-61 的派生快照 `FarmstormConfig.fireChance()`，bake() 每次重载刷新），
岩浆块是它的余量，**全仓没有 `magmaChance`**（A-01）：

```
$ Select-String -Path run-f70/config/farmstorm-server.toml -Pattern 'effectRadius|fireChance|magma'
	effectRadius = 4.0
	fireChance = 85.0          ← 只有一个概率键
```

## 1. 验收 1 · 只往「实心且上方是空气」的格子上方追加，原方块一格不少（D-58）

测试台：浮空 5 层石板（y = 5 / 7 / 9 / 11 / 13，各 9×9，y=11 与 y=13 留 3×3 投掷孔），
`forceload add 96 96 104 104`（方块坐标）。投掷点在 `(100.5, 16.0, 100.5)`、`Motion:[0,0,0]` 自由落体，
命中 y=9 石板顶面 → 落点 `(100.50, 10.05, 100.50)` → 落点方块 `(100,9,100)`。

清场 + 基线（`.work/f70-phase6-final.log`）：

```
> fill 96 4 96 104 15 104 minecraft:air        → "No blocks were filled"
> fill 96 5|7|9|11|13 96 104 y 104 stone       → 各 "Successfully filled 81 block(s)"
> fill 99 11 99 101 11 101 air / 同理 y=13     → "Successfully filled 9 block(s)"
  [pre] execute if block 100 9 100 minecraft:stone   -> "Test passed"
  [pre] execute if block 100 10 100 minecraft:air    -> "Test passed"
  [pre] execute if block 100 8 100 minecraft:air     -> "Test passed"
  [pre] execute if block 99 11 100 minecraft:air     -> "Test passed"   （投掷孔）
  [pre] @e[type=item] -> "Test failed"（= 0 个掉落物）
```

投后**逐格扫描整个核对区 810 格**（x 96..104 / y 5..14 / z 96..104），扫描口径：候选上方期望火或岩浆块，
其余格期望与基线逐格相同（`/execute if block <x> <y> <z> <期望方块>`）：

| 阶段 | 日志 | 候选 | 实测命中候选上方 | 不符格数 |
|---|---|---|---|---|
| 验收 1（doFireTick=false） | `莫洛托夫 候选 115 火 94 岩浆 21` | 115 | 火 94 + 岩浆 21 = **115** | **0 / 810** |
| 复测（doFireTick=true，默认值） | `莫洛托夫 候选 115 火 102 岩浆 13` | 115 | 火 102 + 岩浆 13 = **115** | **0 / 810** |

原文（复测）：

```
  [log] [29Oct2026 15:54:04.555] [Server thread/INFO] [farmstorm/]: 莫洛托夫 候选 115 火 102 岩浆 13
  [scan] 区域 810 格；预测候选 115；实测 火 102 + 岩浆 13 = 115
  [scan] 不符格数 = 0
  [post] @e[type=item] -> "Test failed"（= 0 个掉落物）
```

这一台同时覆盖「平地投一颗」的字面口径：y=9 那一层本身就是半径 4 圆盘的平地（49 个候选），
多出来的 66 个候选来自 y=5 / 7 / 11 三层悬空石板（用来把单发写入量抬到 115、逼近 268 的上限）。
若只算平地那一层，本轮结果即「平地投一颗 → 候选 49、区域逐格 0 不符」。

「不符 0」同时说明四件事：**每个**候选上方都有火或岩浆块（没漏）；**没有**任何非候选位置被写（没多）；
原方块（5 层石板 = 387 格 stone，含 2 个 3×3 孔）逐格未变；没有掉落物。
候选数 115 = 脚本按同一套判据独立复算的结果（`{y=5:1, y=7:37, y=9:49, y=11:28}`），不是抄日志。

顺带验到「候选先裁完再写」这条自定纪律：y=9 层的 49 个候选上方写到 y=10，y=11 层的 28 个候选上方写到 y=12；
若边裁边写，y=10 上的岩浆块（实心）自己会变成新候选，实测候选数会大于复算值——本发两轮都是 115 / 115，没有多出一层。

## 2. 验收 2 · 非 `isSolidRender` 的方块（台阶 / 雪层）上方不放置任何东西

测试台：单层石板 y=9（9×9），在半径 4 的圆盘内埋 4 个非实心方块（`12-TECH §3.7` 判据 2）：

```
> setblock 98 10 100 minecraft:stone_slab[type=bottom]  → "Changed the block at 98, 10, 100"
> setblock 102 10 100 minecraft:stone_slab[type=top]    → "Changed the block at 102, 10, 100"
> setblock 100 10 98 minecraft:snow[layers=1]           → "Changed the block at 100, 10, 98"
> setblock 100 10 102 minecraft:snow[layers=4]          → "Changed the block at 100, 10, 102"
```

| 项 | 结果 |
|---|---|
| 日志 | `莫洛托夫 候选 45 火 43 岩浆 2` |
| 候选数 | **45 = 平地圆盘 49 − 4**（4 个非实心方块挡住了下方那 4 格的上方空气位，自己也没被当成候选） |
| 上方一格仍是空气 | 4/4 `"Test passed"`（98,11,100 / 102,11,100 / 100,11,98 / 100,11,102） |
| 非实心方块本身没被替换 | 4/4 `"Test passed"` |
| 区域 486 格（x 96..104 / y 9..14 / z 96..104）扫描 | **不符格数 = 0** |

原文见 `.work/f70-phase2.log`。

## 3. 验收 3 · 连投 20 颗，岩浆块占比落在 10 %–20 %

统计口径按任务书：**只取日志里放置瞬间打出的 F / M**，不数世界里的方块（火会自己蔓延 / 熄灭）。
每颗之间清场重建（`fill … air` + 5 层石板 + 2 个孔），因此每颗的候选集合完全相同（都是 115），
`doFireTick` 用默认的 `true`（火照常自然演化，与统计无关）。

| # | 候选 | 火 | 岩浆 | 候选=火+岩浆 | # | 候选 | 火 | 岩浆 | 候选=火+岩浆 |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 115 |100 |15 | ✓ | 11 | 115 |105 |10 | ✓ |
| 2 | 115 |97 |18 | ✓ | 12 | 115 |94 |21 | ✓ |
| 3 | 115 |103 |12 | ✓ | 13 | 115 |94 |21 | ✓ |
| 4 | 115 |102 |13 | ✓ | 14 | 115 |101 |14 | ✓ |
| 5 | 115 |96 |19 | ✓ | 15 | 115 |98 |17 | ✓ |
| 6 | 115 |96 |19 | ✓ | 16 | 115 |98 |17 | ✓ |
| 7 | 115 |100 |15 | ✓ | 17 | 115 |87 |28 | ✓ |
| 8 | 115 |95 |20 | ✓ | 18 | 115 |102 |13 | ✓ |
| 9 | 115 |97 |18 | ✓ | 19 | 115 |100 |15 | ✓ |
| 10 | 115 |94 |21 | ✓ | 20 | 115 |102 |13 | ✓ |

```
合计：20 投，候选 2300，火 1961，岩浆 339
岩浆块 / (火 + 岩浆块) = 339 / 2300 = 14.74 %      ← 落在 10 %–20 % 区间内
火比例                = 1961 / 2300 = 85.26 %      ← 目标 85.0 %
每投候选均为 115：true；每投自检 候选=火+岩浆：true
```

20 投之间**没有任何一次** `候选 ≠ 火 + 岩浆`：每个候选恰好掷一次骰子、只产出一种结果（DESIGN §5.8 判据 4）。

### 3.1 加测：余量语义（A-01 的直接证据）

| toml | 日志 | 说明 |
|---|---|---|
| `fireChance = 0.0` | `莫洛托夫 候选 115 火 0 岩浆 115` | 100 % 岩浆块 |
| `fireChance = 100.0` | `莫洛托夫 候选 115 火 115 岩浆 0` | 100 % 火 |
| `fireChance = 85.0`（已改回） | `莫洛托夫 候选 115 火 95 岩浆 20` | 恢复正常 |

改了 `fireChance` 之后岩浆块比例整段跟着走 → 证明岩浆是**这个键的余量**，不是第二个键（`.work/f70-phase5-keys.log`）。

## 4. 验收 4 · `molotov.effectRadius` 改成 1.0 后候选数明显变小

用 `edit` 工具改 `run-f70/config/farmstorm-server.toml`（无 BOM，首字节 `91,99,111` = `[co`），
`FileWatcher` 当场重载并打出新 `bake()` 行，**没有** `ParsingException` / `Invalid bare key` / `Attempting to recreate`：

```
[29Oct2026 15:52:24.639] [FileWatcher-1-thread-1/INFO] [farmstorm/]: bake() 派生快照：molotov.fireChance=85.0%（余下 15.0% 归岩浆块）…
```

| `effectRadius` | 日志 | 世界侧核对 |
|---|---|---|
| **1.0** | `莫洛托夫 候选 5 火 4 岩浆 1` | 落点周围 5 格（y=10）火 4 + 岩浆 1，无遗漏；4 格外 `100 10 104` 仍是空气 `"Test passed"` |
| 4.0（改回） | `莫洛托夫 候选 115 火 95 岩浆 20` | —— |

**115 → 5**（同一测试台、同一天花板、唯一变量是那一行），且 5 = 以 `(100,9,100)` 为心、半径 1.0 的整数球内候选数
（中心 1 + 四邻 4；`(100,10,100)` 是空气、`(100,8,100)` 上方不是空气，各自被两条判据淘汰）。半径确实读 config。

## 5. 验收 5 · 单次投掷在同一刻内完成，无 `Can't keep up`

| 证据 | 值 |
|---|---|
| 两条日志行的时间差（同一 `onImpact` 内，爆炸行 → 洒火行） | 15:51:18.912 → **.920**（8 ms）；15:53:39.555 → **.556**（1 ms）；15:54:04.554 → **.555**（1 ms） |
| 抛物线之外的整段（召唤 → 实体结算消失） | 1073–1200 ms（自由落体 6 格 + 命中），本轮 27 发全部同量级 |
| 投后 `tick query` | `Average time per tick: 0.6ms (Target: 50.0ms)` / `P50 0.4ms P95 1.7ms P99 10.7ms`——离 50 ms 上限很远 |
| 单发写入量 | 最多 **115** 次 `setBlock`（浮空 5 层测试台）；理论上限 = 半径 4 整数球内格数 **257** |
| 服务端日志 `Can't keep up` 行数 | **0**（27 发全程；`Select-String -Pattern "Can't keep up"` → 0） |

## 6. 构建与残留

```
$ cmd /c ""D:\project_farm\.tools\gw.cmd" build"
BUILD SUCCESSFUL in 8s
5 actionable tasks: 5 up-to-date
（.work/f70-build-final.log 里 warning: / error: 命中数 = 0，exit=0）
```

| 位置 | 命令 | 计数 |
|---|---|---|
| 源码 | `Select-String src/**/*.java -Pattern 'Probe'` | **4**，全是既有玩法常量 `LANDING_PROBE`（`MolotovProjectile` 42/94 行 + `PuffedWheatProjectile` 31/64 行），**无探针类** |
| build/classes | `Get-ChildItem -Recurse build\classes -Filter '*Probe*'` | **0** |

`git status`：只有 `M src/main/java/com/dashpig/farmstorm/entity/thrown/MolotovProjectile.java` 一处改动，
其余是 `.work/` 下的证据文件。**未 commit。**

## 7. 环境收尾

```
RCON stop → "Stopping the server" → 维度全部保存 → "Thread RCON Listener stopped" → gradle 作业 exit 0
Get-Process java      → 无（空）
.tools/runserver.lock → 不存在（未残留）
run-f70/config/farmstorm-server.toml → effectRadius = 4.0 / fireChance = 85.0（全部改回默认）
```

证据文件：`.work/f70-phase1.log`（验收 1 首发）、`f70-phase2.log`（验收 2）、`f70-phase3.log`（验收 3 ×20）、
`f70-phase4-radius1.log`（验收 4）、`f70-phase5-keys.log`（A-01 加测）、`f70-phase6-final.log`（验收 1 复测 + 验收 5）、
`f70-build-final.log`（构建）、本文件。
