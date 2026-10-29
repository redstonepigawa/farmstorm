# F-68 证据 · 压缩膨胀麦投掷物沿用膨胀麦的落点写入

HEAD: bf3f9ea（未 commit）｜ 改动 **2 个文件**（任务书表格里的两个，没有第三个）｜ 方法 **A**（/summon 同坐标、Motion 全 0 自由落体）

## 0. 交付物与「同一份循环」的证据

```
$ git diff --stat
 .../thrown/CompressedPuffedWheatProjectile.java       | 19 +++++++++++++++----
 .../entity/thrown/PuffedWheatProjectile.java           | 12 ++++++++++--
 2 files changed, 25 insertions(+), 6 deletions(-)
```

父类（PuffedWheatProjectile.java，**只有**这 3 处）：

```diff
+    /**
+     * 写入的方块，本类的唯一可变点。D-77：压缩膨胀麦只覆写这个方法，
+     * 落点反推 / 水源判定 / 写入循环 / 抗性底线全部沿用父类那一份，不存在第二份。
+     */
+    protected BlockState fillBlock() {
+        return Blocks.HAY_BLOCK.defaultBlockState();
+    }
+
@@ public void onImpact(...) 未动；landingBlock / hasWaterSource 未动
-    private static void fillHay(ServerLevel level, BlockPos center, int edge) {
+    private void fillHay(ServerLevel level, BlockPos center, int edge) {
         int radius = edge / 2;
-        BlockState hay = Blocks.HAY_BLOCK.defaultBlockState();
+        BlockState hay = this.fillBlock();
```

子类（CompressedPuffedWheatProjectile.java）**类声明与唯一差异**：

```diff
-public class CompressedPuffedWheatProjectile extends ThrownCropEntity {
+public class CompressedPuffedWheatProjectile extends PuffedWheatProjectile {
@@ profile() 保留；onImpact / fillHay / landingBlock / hasWaterSource 一个都没有
+    /** D-77：唯一差异——父类写入的方块从 {@code minecraft:hay_block} 换成压缩干草块。 */
+    @Override
+    protected BlockState fillBlock() {
+        return FarmstormBlocks.COMPRESSED_HAY_BLOCK.get().defaultBlockState();
+    }
```

静态对照（全仓 `src` 里写入语句只有一份）：

```
$ grep -c 'setBlock(pos, hay' (src 全部 .java)         → 1
PuffedWheatProjectile.java:87:  protected BlockState fillBlock() {        ← 父类扩展点
PuffedWheatProjectile.java:101: BlockState hay = this.fillBlock();       ← 唯一写入口
CompressedPuffedWheatProjectile.java:35: protected BlockState fillBlock() {  ← 子类覆写，只有 3 行
```

`Select-String entity/thrown/*.java -Pattern 'Explosion'`：22 行命中里**没有一行**来自 `CompressedPuffedWheatProjectile.java`（父类的唯一命中是 `getExplosionResistance()` 抗性底线，不是 `Explosion`）——A-07「沿用的是落点写入，不是爆炸」成立。
config 侧零改动：`git status --short` 只有上面两个 `M`（`FarmstormConfig` / `FarmstormBlocks` / `datagen/` / `docs/` 全部未动）。

## 1. 为什么用 A 而不是 B

A 走的是**真实玩法路径**：`/summon` → 实体 tick → `onHitBlock` → 基类 `settle` → `onImpact`，四条验收里的「原方块被替换」「无掉落物」「基岩跳过」都能在其中一次性观察到；B（探针直调 `onImpact`）会把这条链路上的一半事实变成「我保证了它等于真实路径」。A 的难点只有「两颗投掷物落在同一坐标」，用 `Motion:[0.0d,0.0d,0.0d]` + 同一 `Pos` 就是纯竖直自由落体，落点由几何唯一决定——实测两次落点立方体中心一致（见 §2 的坐标集合逐格相等），无需靠手感。

**A 的一个前置条件（上一轮踩过的坑，这轮又踩了一次）**：专用服务器无玩家在线时区块不 entity-tick，必须 `/forceload`。而且 `/forceload add` 的参数是**方块列坐标**，不是区块坐标——我第一遍写 `forceload add 6 6` 得到 `Marked chunk [0, 0]`，测试区（区块 6,6）没被加载，逐格 `execute if block` 全部返回 `That position is not loaded`，投掷物落在未加载区块里当场消失，六项统计全 0。**那一遍整轮作废**，正确写法是 `forceload add 96 96 104 104` → `Marked chunk [6, 6]`。

作废那一遍还留下一批**冻结在未加载区块里、随区块加载复活**的投掷物，导致修正后第一次复跑出现「干态 36 格 hay + 9 格 compressed」的脏数据（36 = 两个中心差 1 格的 3³ 立方体并集，4×3×3）。处置：RCON `stop` → 删掉 `run-f68/world` → 全新世界重跑，并在脚本开头加 `kill @e[type=farmstorm:puffed_wheat|compressed_puffed_wheat|item]` + 1.5 s 静置 + 清场后基线断言。**下面的数字全部来自全新世界的那一遍**（log 里的 `[pre] 清场后基线 hay=0 compressed=0`）。

## 2. 场景 A · 干态：同一坐标先投膨胀麦、再投压缩膨胀麦（验收 1 / 2）

测试台：`fill 96 5 96 104 9 104 minecraft:stone`（9×9×5 石板）、上方 `fill 97 10 97 103 12 103 minecraft:air`；
两颗都在 `100.5 12.0 100.5` 以 `{Motion:[0.0d,0.0d,0.0d]}` 召唤，落点立方体中心 = `100,9,100`。
逐格统计区域 = `x 97..103 × y 6..12 × z 97..103`（343 格，含 5³ 湿态立方体 + 一圈余量）。

```
    [pad] fill stone -> "Successfully filled 405 block(s)\n" | fill air -> "No blocks were filled\n"
    [pre] 清场后基线 hay=0 compressed=0（必须都为 0）
> execute if block 100 9 100 minecraft:stone
    "Test passed\n"
    baseline hay / compressed: 0 / 0
> summon farmstorm:puffed_wheat 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}
    "Summoned new Puffed Wheat\n"
    entity gone after 631 ms
    [A1] 膨胀麦写入 minecraft:hay_block 格数 = 27        ← 3³ = 27
    [A1] 同区域 farmstorm:compressed_hay_block 格数 = 0
    [A1 item] /execute if entity @e[type=item] -> "Test failed\n"   ← 物品实体 0
    [pad] fill stone -> "Successfully filled 18 block(s)\n" | fill air -> "Successfully filled 9 block(s)\n"   ← 18+9=27，复位的正是那 27 格
> summon farmstorm:compressed_puffed_wheat 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}
    "Summoned new entity.farmstorm.compressed_puffed_wheat\n"
    entity gone after 816 ms
    [A2] 压缩膨胀麦写入 farmstorm:compressed_hay_block 格数 = 27
    [A2] 同区域 minecraft:hay_block 格数 = 0
    [A2 item] /execute if entity @e[type=item] -> "Test failed\n"
    [A] 位置集合完全相同 = true（27 vs 27）
    [A] 无掉落物（两次投掷后 item 实体均为 0）= true
```

「位置集合完全相同」的判据（两层，第二层更强）：
1. 逐格 `/execute if block` 得到的 27 个坐标**排序后逐元素相等**（脚本 `same()`：长度相同 + 每个元素相同）；
2. 投第二颗之前没有复位方格（只复位过），第二颗写完时 `minecraft:hay_block` 在整片 343 格里**剩 0 格**且 `compressed_hay_block` = 27 → 第一颗写的 27 格被**全部覆盖**，即两个集合相等（若有任何一格没被覆盖，hay 计数会 >0）。

两次 27 格坐标（原文，两行逐字相同）：

```
[A] 膨胀麦 27 格坐标: 100,10,100 100,10,101 100,10,99 100,8,100 100,8,101 100,8,99 100,9,100 100,9,101 100,9,99 101,10,100 101,10,101 101,10,99 101,8,100 101,8,101 101,8,99 101,9,100 101,9,101 101,9,99 99,10,100 99,10,101 99,10,99 99,8,100 99,8,101 99,8,99 99,9,100 99,9,101 99,9,99
[A] 压缩   27 格坐标: 100,10,100 100,10,101 100,10,99 100,8,100 100,8,101 100,8,99 100,9,100 100,9,101 100,9,99 101,10,100 101,10,101 101,10,99 101,8,100 101,8,101 101,8,99 101,9,100 101,9,101 101,9,99 99,10,100 99,10,101 99,10,99 99,8,100 99,8,101 99,8,99 99,9,100 99,9,101 99,9,99
```

「原方块全部被替换」：底面积顶层 9 格石板 + 侧面 18 格石板/空气全部变成方块 —— `[pad]` 复位命令返回 `Successfully filled 18 block(s)`（y=8 与 y=9 两层各 9 格 hay，被复位成石板）+ `9 block(s)`（y=10 那层 9 格 hay，被复位成空气），合计正好 27，说明两次都把这 27 格原方块覆盖掉了。无掉落物：`execute if entity @e[type=item]` 两次都是 `Test failed`（= 0 个物品实体）；清场那次 `/kill @e[type=item]` 也回 `"No entity was found"`（即 `/kill` 前后物品实体计数均为 0），全程没有任何方块掉落。

## 3. 场景 B · 湿态：干态立方体内有水源 ⇒ 边长 5（验收 1 · 湿态）

在干态立方体内部 `101 8 100` 放一个被石板完全包住的水源方块（6 面皆石，不会流走；`execute if block 101 8 100 minecraft:water` 回 `Test passed`）。两次投掷前都重新复位石板并重新放水（第二颗若不复位就找不到水源，边长会退回 3 —— 这是为了让两颗看到**同样的输入状态**）。

```
> setblock 101 8 100 minecraft:water
    "Changed the block at 101, 8, 100\n"
> execute if block 101 8 100 minecraft:water
    "Test passed\n"
> summon farmstorm:puffed_wheat 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}
    "Summoned new Puffed Wheat\n"
    entity gone after 612 ms
    [B1] 膨胀麦（遇水）minecraft:hay_block 格数 = 125      ← 5³ = 125
    [B1 item] /execute if entity @e[type=item] -> "Test failed\n"
> summon farmstorm:compressed_puffed_wheat 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}
    "Summoned new entity.farmstorm.compressed_puffed_wheat\n"
    entity gone after 817 ms
    [B2] 压缩膨胀麦（遇水）farmstorm:compressed_hay_block 格数 = 125
    [B2] 同区域 minecraft:hay_block 格数 = 0
    [B2 item] /execute if entity @e[type=item] -> "Test failed\n"
    [B] 位置集合完全相同 = true（125 vs 125）
    [B] 无掉落物 = true
```

两次 125 格坐标逐元素相等（脚本判定 `true`；完整 125 条原文见 `.work/f68-verify-out.txt` 的 `[B] 膨胀麦 125 格坐标` / `[B] 压缩 125 格坐标` 两行）。

**这一条同时证明「读的是既有键」**：边长 3→5 的变化只可能来自 `puffed_wheat.dryEdge=3` / `puffed_wheat.wetEdge=5` 与「水源只在干态立方体里判一次」那段逻辑（两个类共用同一份 `onImpact`），压缩版没有任何自己的边长/水源代码。

## 4. 场景 C · 抗性底线：minecraft:bedrock 逐格跳过（验收 3）

在同一个干态 3³ 立方体里放 9 格 `minecraft:bedrock`（8 个角 + 1 个面心 `101,9,100`），中心 `100,9,100` 仍是石板以保证还能落在同一坐标投压缩膨胀麦。

```
    [C] 基岩 (99,8,99) 仍为 bedrock = true      …（9 格逐格 true，省略中间 7 行）
    [C] 基岩 (101,9,100) 仍为 bedrock = true
> summon farmstorm:compressed_puffed_wheat 100.5 12.0 100.5 {Motion:[0.0d,0.0d,0.0d]}
    "Summoned new entity.farmstorm.compressed_puffed_wheat\n"
    entity gone after 818 ms
    [C] 投掷后 9 格基岩逐格 /execute if block minecraft:bedrock = [true,true,true,true,true,true,true,true,true]
    [C] 全为真 = true
    [C] 同一 3x3x3 立方体内被写成 compressed_hay_block 的格数 = 18（期望 27-9=18）
    [C] 残留 hay_block 格数 = 0
    [C] 无掉落物 = true
```

9 格全真 = 抗性 3.6e6 的方块被跳过；同一立方体另外 **18 格被写成压缩干草块** = 这一段写入循环确实跑了（不是「整个立方体都没写」）。27−9=18 精确吻合。

## 5. 验收 4 · config 无新增键（run-f68 现场生成的 toml，原文）

```
$ Select-String run-f68/config/farmstorm-server.toml -Pattern '^\[|puffed|compressed'
1:  [converter]      13: [stack]         23: [deploy]        33: [teams]
39: [explosion]      49: [throwable]     55: [food]
61: [puffed_wheat]   …（共 26 个分组，无 [compressed_puffed_wheat]）

61  [puffed_wheat]
        #干态替换立方体边长
        # Default: 3
        # Range: 1 ~ 15
        dryEdge = 3
        #遇水扩容后的立方体边长
        # Default: 5
        # Range: 1 ~ 15
        wetEdge = 5

键总数 139（与 13-TASKS「config 键数一律 22 条 / 总数 139 条」一致，零新增）
$ Select-String run-f68/config/farmstorm-server.toml -Pattern 'compressed'  → 0 命中
```

`git status --short` 里没有 `config/FarmstormConfig.java`、没有 `datagen/`——两个类读的就是 `FarmstormConfig.puffedWheatDryEdge` / `puffedWheatWetEdge`。

## 6. 构建与残留

```
$ cmd /c ""D:\project_farm\.tools\gw.cmd" build"      → BUILD SUCCESSFUL in 11s
构建日志最后两行：
BUILD SUCCESSFUL in 11s
5 actionable tasks: 3 executed, 2 up-to-date
（.work/f68-build.log 里 warning: / error: 均 0 命中）
```

Probe 残留计数（三处，全 0）：

| 位置 | 命令 | 计数 |
|---|---|---|
| 源码 | `Select-String src/**/*.java -Pattern 'Probe'` | **2**，但两处都是既有的玩法常量 `LANDING_PROBE`（`PuffedWheatProjectile.java:31` 声明 / `:64` 使用），**没有任何探针类** |
| build/classes | `Get-ChildItem -Recurse build\classes -Filter '*Probe*'` | **0** |
| jar | `ZipFile` 里名字含 Probe 的条目 | **0**（`farmstorm-0.0.2+mc1.21.1.jar`） |

## 7. 环境收尾

```
RCON stop → "Stopping the server" → Thread RCON Listener stopped → gradle job exit 0
Get-Process java        → 无
.tools/runserver.lock   → 已删除（不存在）
```

证据文件：`.work/f68-verify.js`（RCON 脚本）、`.work/f68-verify-out.txt`（本轮 98 行逐条原文）、`.work/f68-verify-console.log`、`.work/f68-build.log`、`.work/f68-server.log`（服务端完整日志）。
**未 commit。**
