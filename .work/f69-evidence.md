# F-69 证据 · 莫洛托夫落点触发器

HEAD: aae2e44（未 commit）｜ 改动 **1 个文件**：`entity/thrown/MolotovProjectile.java`
服务端: `-PfsRunDir=run-f69`（全新世界，flat，seed 680068，rcon 25610）｜客户端: `run-f69c`（quickplay 加入 127.0.0.1:25609）

## 0. 交付物

```
$ git diff --stat
 .../farmstorm/entity/thrown/MolotovProjectile.java | 35 ++++++++++++++++++++--
 1 file changed, 32 insertions(+), 3 deletions(-)
```

```java
@Override
protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
    this.trigger(level, pos);
}

/**
 * D-58 的落点爆炸：威力是唯一可变项，破坏与伤害两个入口按设计关死（A-10 明确「破坏权限与威力无关」，
 * 所以这里没有 {@code BlastRule.forCrop} 分支，只有 {@link BlastRule#NEVER_BREAK} 一个常量）。
 */
private void trigger(ServerLevel level, Vec3 pos) {
    int power = FarmstormConfig.molotovTriggerExplosionPower.get();
    new FarmstormExplosion(level, this.getOwner(), FarmstormDamageTypes.cropBlast(level, this.getOwner()),
            pos.x, pos.y, pos.z, power, BlastRule.NEVER_BREAK, 0.0F).detonate();
    Farmstorm.LOGGER.info(String.format(Locale.ROOT, "莫洛托夫 落点 (%.2f, %.2f, %.2f) 威力 %d",
            pos.x, pos.y, pos.z, power));
}
```

一处与任务书字面的偏差：任务书写 `trigger(Vec3)`，实际签名是 `trigger(ServerLevel, Vec3)`——`FarmstormExplosion` 的构造只要 `ServerLevel`，
`onImpact` 本来就有这个参数，传进来比在方法里 `(ServerLevel) this.level()` 强转干净。私有方法，无 API 影响。
`damageCap` 写 `0.0F`（任务书 `0F`，同值）。

产物级核对（`javap -p -c build/classes/.../MolotovProjectile.class`）：

```
 0: getstatic  FarmstormConfig.molotovTriggerExplosionPower:ModConfigSpec$IntValue
13: new        FarmstormExplosion
44: getstatic  BlastRule.NEVER_BREAK:BlastRule          ← 没有 BlastRule.of
48: invokespecial FarmstormExplosion."<init>":(ServerLevel;Entity;DamageSource;DDDF;BlastRule;F)V
51: invokevirtual FarmstormExplosion.detonate:()Z
```

## 1. 测试台与清场基线（每轮都先跑）

```
> forceload add 96 96 104 104          → "Marked chunk [6, 6] in Overworld to be force loaded"   （方块列坐标，不是区块坐标）
> fill 96 5 96 104 9 104 minecraft:stone → "Successfully filled 405 block(s)"
> fill 96 10 96 104 13 104 minecraft:air → "No blocks were filled"
> kill @e[type=farmstorm:molotov] / kill @e[type=item] / kill @e[tag=f69probe] / kill @e[tag=f69mob]   + 800 ms 静置
> execute if block 100 9 100 minecraft:stone → "Test passed"
> execute if block 100 10 100 minecraft:air  → "Test passed"
    [pre] 区域 567 格（x 96..104 / y 6..12 / z 96..104）不符格数 = 0
    [pre] @e[type=item] -> "Test failed"（= 0 个）
    [pre] @e[type=farmstorm:molotov] -> "Test failed"（= 0 个）
```

投掷方式：`/summon farmstorm:molotov 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}`（纯竖直自由落体），命中石板顶面 → 落点 Vec3 = `(100.5, 10.05, 100.5)`，
落点方块 `(100,10,100)`。3×3×3 = x/z 99..101、y 9..11（27 格）；核对区 567 格（9×7×9）。
逐格判定用 `/execute if block <x> <y> <z> <期望方块>`：y≤9 期望石板、y≥10 期望空气——期望值不符即「有方块变了」。

## 2. 验收 1 · 无伤害（`damageCap = 0F`）

玩家 `Dev` 在线（`runClient` quickplay），投掷前 `/tp @p 102.5 10.0 100.5`（距爆心 2.05 格）：

| 阶段 | 配置威力 | 命令 | 返回原文 |
|---|---|---|---|
| 投前 | 0 | `data get entity @p Health` | `Dev has the following entity data: 20.0f` |
| 投后 | 0 | `data get entity @p Health` | `Dev has the following entity data: 20.0f` |
| 投前 | **4** | `data get entity @p Health` | `Dev has the following entity data: 20.0f` |
| 投后 | **4** | `data get entity @p Health` | `Dev has the following entity data: 20.0f` |

第二行组是**加强版**：威力 4 时 reach=8 格，玩家确实在爆心 2.05 格内（同一发的击退见 §4），血量仍然 20.0f。
同场的生物探针 `pig`（NoGravity，距爆心 2.05 格）`Health` 三次读数（投前 / 投后 / +1s）都是 `10.0f`，威力 0 与 4 两轮都一样。

## 3. 验收 2 · 不破坏方块（`NEVER_BREAK`）

| 阶段 | 配置威力 | 567 格核对区不符 | 3×3×3（27 格）投前/投后 | 掉落物 |
|---|---|---|---|---|
| P0 | 0 | **0** | 0 / 0 | `@e[type=item] → Test failed`（0 个） |
| P4 | **4** | **0** | 0 / 0 | `Test failed`（0 个） |
| P0R | 0（已改回） | **0** | 0 / 0 | `Test failed`（0 个） |

原文（P4）：`[post] 区域 567 格 … 不符格数 = 0` / `[cube3] 落点为心 3x3x3 共 27 格：投前不符 0 / 投后不符 0`。
即：威力调到 4、爆炸半径确实进到世界（§4 的击退为证），**仍然一格方块都没动**。

## 4. 验收 3 · 日志（格式逐字）

`run-f69/logs/latest.log` 原文三行（一次投掷一行）：

```
[29Oct2026 15:40:03.505] [Server thread/INFO] [farmstorm/]: 莫洛托夫 落点 (100.50, 10.05, 100.50) 威力 0
[29Oct2026 15:40:17.404] [Server thread/INFO] [farmstorm/]: 莫洛托夫 落点 (100.50, 10.05, 100.50) 威力 4
[29Oct2026 15:40:48.647] [Server thread/INFO] [farmstorm/]: 莫洛托夫 落点 (100.50, 10.05, 100.50) 威力 0
```

坐标 `(100.50, 10.05, 100.50)` = 命中点 `(100.5, 10.0, 100.5)` 沿命中面外法线 +0.05（基类 `onHitBlock` 的既有口径）；
威力 0 与 `molotov.triggerExplosionPower` 默认值一致（`run-f69/config/farmstorm-server.toml` 初始 `triggerExplosionPower = 0`）。

## 5. 验收 4 · 键确实驱动威力（**任务书这一条与 A-10 冲突，见下**）

### 5.1 先把冲突摆出来

任务书验收 4 写「把 `molotov.triggerExplosionPower` 改成 4 → 重投 → **出现方块破坏**」。但同一个任务书的交付物要求**恒** `BlastRule.NEVER_BREAK`，
而治理口径明确相反：

- `.work/v002-BRIEF.md:499`（裁决 A-10）：「默认 0 时零副作用；**调大后只增加对实体的伤害，永远不破坏方块**……`BlastRule.forCrop` 恒返回 `NEVER_BREAK`——破坏权限与威力无关」
- `11-DESIGN-0.0.2.md:366/391`：「调大后只增实体伤害，**永不破方块**（裁决 A-10）」
- `FarmstormConfig.java:706` 的 config 注释：「0 = 只当落点触发器（**A-10：任何威力都不破坏方块**）」
- `FarmstormExplosion#breakBlocks` 里 `rule.canBreak` 恒 false → 无论 radius 多大，`broken` 恒 0

所以「正确的实现」在威力 4 下**必然观测到 0 方块破坏**——这一条按字面无法同时满足。实测见下表 P4 行（567 格一格没动）。
为了不让「没读 config 的空实现」也蒙混过关，用下面两条独立证据证明这个键**真的被读到并驱动了这次爆炸**。

### 5.2 证据 A（shipped 代码）：威力 4 时爆炸真的发生了，威力 0 时没有

探针实体 `armor_stand`(103.5 10.0 100.5) 与 `pig`(100.5 11.0 102.5)，都 `NoGravity`，都在爆心 3.0 / 2.05 格内；读 `Motion`（原版爆炸击退走 `Explosion#explode`，`shouldDamageEntity=false` 只关伤害不关击退）：

| 阶段 | 配置威力 | armor_stand Motion（投前 → 投后 → +1s） | pig Motion（投前 → 投后 → +1s） |
|---|---|---|---|
| P0 | 0 | `[0,0,0]` → `[0,0,0]` → `[0,0,0]` | `[0,0,0]` → `[0,0,0]` → `[0,0,0]` |
| P4 | **4** | `[0,0,0]` → `[0.4425, 0.2548, 0]` → `[0.2954, 0.1701, 0]` | `[0,0,0]` → `[0, 0.3847, 0.4486]` → `[0, 0.2568, 0.2995]` |
| P0R | 0（改回） | `[0,0,0]` → `[0,0,0]` → `[0,0,0]` | `[0,0,0]` → `[0,0,0]` → `[0,0,0]` |

同一份二进制、同一个落点，**唯一变量是 toml 里的那一行**：4 时爆炸半径（reach = 8）够到探针并施加原版击退，0 时 reach = 0 什么都够不到。
这就是「威力值真的进了 `FarmstormExplosion` 的 radius 参数」的间接观测——它同时解释了为什么 3×3×3 在 4 下仍然不动（`NEVER_BREAK` 关的是破坏入口，不是这次爆炸本身）。

### 5.3 证据 B（临时探针构建，已回退）：同一个 config 读数确实决定破坏量

为了把「这个键驱动威力」直接看到方块上，做了一次**临时**改动（只在 `MolotovProjectile.java` 内把规则常量换成 `BlastRule.of(power, false)`，威力表达式一字不改），
石板换成**泥土板**（抗性 0.5，阈值 4 才炸得动），跑完立刻回退并重建：

| 阶段 | 构建 | 配置威力 | 567 格被破坏 | 3×3×3 被破坏 | 日志 |
|---|---|---|---|---|---|
| PR4 | 临时探针（`BlastRule.of(power,false)`） | **4** | **107 格** | 9 格 | `威力 4` |
| PR0 | 同一临时构建 | 0 | **0 格** | 0 格 | `威力 0` |

PR4 被破坏坐标（前 40）：`97,8,99 97,8,100 97,9,98 97,9,99 97,9,100 97,9,101 97,9,102 98,7,98 98,7,99 98,7,100 …`（完整清单见 `.work/f69-verify-PR4.txt`）。

回退后重建，产物级复核：`javap` 里重新出现 `getstatic BlastRule.NEVER_BREAK`、`BlastRule.of` 消失（§0），
`git diff` 里也没有 `BlastRule.of`。**最终交付的就是 NEVER_BREAK 版本**；PR4/PR0 只是量「这个键的读数能驱动多大的破坏预算」。

### 5.4 结论与建议

- 「键被读取」：成立（证据 A + B + 三行日志的 0/4/0 切换）。
- 「威力 4 → 出现方块破坏」：**按 A-10 永不成立**，实测 0 格。这是设计，不是缺陷。
- 建议把验收 4 改写成 A-10 兼容的形式（二选一）：
  1. 「威力改到 4 后，爆心内的实体出现原版击退（`Motion` 由 `[0,0,0]` 变为非零）而血量与方块全部不变；改回 0 后击退也消失」——本轮已按这个口径实测通过（§5.2）。
  2. 「允许一次临时探针构建把规则换成 `BlastRule.of(power,false)`，验证威力 4 破坏 > 0、威力 0 破坏 = 0，随后回退」——本轮实测 107 vs 0（§5.3）。

## 6. 构建与残留

```
$ cmd /c ""D:\project_farm\.tools\gw.cmd" build"
BUILD SUCCESSFUL in 9s
5 actionable tasks: 2 executed, 1 from cache, 2 up-to-date
（.work/f69-build-final.log 里 warning: / error: 均 0 命中）
```

Probe 残留：

| 位置 | 命令 | 计数 |
|---|---|---|
| 源码 | `Select-String src/**/*.java -Pattern 'Probe'` | **2**，都是既有玩法常量 `LANDING_PROBE`（`PuffedWheatProjectile.java:31/:64`），**无探针类** |
| build/classes | `Get-ChildItem -Recurse build\classes -Filter '*Probe*'` | **0** |
| jar | `farmstorm-0.0.2+mc1.21.1.jar` 内含 Probe 的条目 | **0** |

## 7. 环境收尾

```
RCON stop → "Stopping the server" → gradle 作业 exit → java 全部退出（服务端 + 客户端）
Get-Process java      → 无
.tools/runserver.lock → 已删除（不存在）
run-f69/config/farmstorm-server.toml → triggerExplosionPower = 0（已改回默认）
```

踩到的坑（下一轮别重犯）：用 `Set-Content -Encoding utf8`（PS 5.1 = **带 BOM**）改 toml，NeoForge `FileWatcher` 解析失败
（`ParsingException: Invalid bare key: '\uFEFF[converter'`）并「Attempting to recreate」把文件重写成默认值——改 toml 要用无 BOM 的写法（本轮改用 `edit` 工具，之后每次改完都确认 watcher 的 `bake()` 行出现）。

证据文件：`.work/f69-verify.js`（RCON 脚本）、`.work/f69-verify-{P0,P4,P0R,PR0,PR4}.txt`（逐条原文）、
`.work/f69-build-final.log`、`.work/f69-build-probe.log`、`.work/f69-server-final.log`、`.work/f69-server-probe.log`、`.work/f69-client.log`。
**未 commit。**
