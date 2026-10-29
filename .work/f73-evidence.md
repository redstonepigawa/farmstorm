# F-73 证据 · 三条新转换配方（crimson_fungus ⇄ molotov / sugar_cane ⇄ zangief_cane / golden_apple ⇄ wild_apple）

HEAD: `748fa9d`（**未 commit**）｜改动 **1 个文件**：`datagen/ModRecipeProvider.java`（+5 / −1 行，唯一删除行是类注释里的条数）
产物：`data/farmstorm/recipe/molotov.json`、`zangief_cane.json`、`wild_apple.json`（均由 `runData` 生成，未手写）

```
$ git diff --stat
 src/main/java/com/dashpig/farmstorm/datagen/ModRecipeProvider.java | 6 +++++-
 1 file changed, 5 insertions(+), 1 deletion(-)

$ git diff -- src/.../datagen/ModRecipeProvider.java   （只看 +/- 实质行）
- * 21 条农产品转换配方（TECH §6.7 / D-11 / D-12）+ 1 条转换器合成配方（D-52 / F-71）
+ * 24 条农产品转换配方（TECH §6.7 / D-11 / D-12；F-73 补 3 条，D-54）+ 1 条转换器合成配方（D-52 / F-71）
+        // 22-24：F-73 新增三条作物本体双向（D-54），与上面同形：无催化剂、正反向同价
+        twoWay(recipeOutput, "molotov", Items.CRIMSON_FUNGUS, FarmstormItems.MOLOTOV);
+        twoWay(recipeOutput, "zangief_cane", Items.SUGAR_CANE, FarmstormItems.ZANGIEF_CANE);
+        twoWay(recipeOutput, "wild_apple", Items.GOLDEN_APPLE, FarmstormItems.WILD_APPLE);
```

新增的三条走的还是 v0.0.1 那 13 条用的同一个 `twoWay(...)`：`bidirectional=true`、`XP_COST=3`、
`Ingredient.EMPTY`（无催化剂）、`NO_CATALYST=1`。所以「沿用既有键、默认 3、无催化剂」是**同一条代码路径**，
不是新写一套常量。

## 1. 三个生成 JSON 全文

`src/generated/resources/data/farmstorm/recipe/molotov.json`
```json
{
  "type": "farmstorm:converting",
  "input": {
    "item": "minecraft:crimson_fungus"
  },
  "result": {
    "count": 1,
    "id": "farmstorm:molotov"
  },
  "reverse_xp_cost": 3
}
```

`src/generated/resources/data/farmstorm/recipe/zangief_cane.json`
```json
{
  "type": "farmstorm:converting",
  "input": {
    "item": "minecraft:sugar_cane"
  },
  "result": {
    "count": 1,
    "id": "farmstorm:zangief_cane"
  },
  "reverse_xp_cost": 3
}
```

`src/generated/resources/data/farmstorm/recipe/wild_apple.json`
```json
{
  "type": "farmstorm:converting",
  "input": {
    "item": "minecraft:golden_apple"
  },
  "result": {
    "count": 1,
    "id": "farmstorm:wild_apple"
  },
  "reverse_xp_cost": 3
}
```

## 2. 验收 1（JSON 形状：type / bidirectional / experience）

| 文件 | keys | type | input | result.count | reverse_xp_cost |
|---|---|---|---|---|---|
| molotov.json | type,input,result,reverse_xp_cost | farmstorm:converting | minecraft:crimson_fungus | 1 | 3 |
| zangief_cane.json | type,input,result,reverse_xp_cost | farmstorm:converting | minecraft:sugar_cane | 1 | 3 |
| wild_apple.json | type,input,result,reverse_xp_cost | farmstorm:converting | minecraft:golden_apple | 1 | 3 |

**关于 `bidirectional` 与 `experience` 两个键不在 JSON 里**（如实记录，不是漏做）：
`ConverterRecipeSerializer.CODEC` 把「等于默认值」的可选字段省略掉——
`Codec.INT.optionalFieldOf("xp_cost", 3)`、`Codec.BOOL.optionalFieldOf("bidirectional", true)`、
`Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.EMPTY)`、`optionalFieldOf("catalyst_count", 1)`。
既有 21 条也一模一样（对照组 `puffed_wheat.json` 的 keys 与上表逐字相同，`reverse_xp_cost` 同为 3）。
即：**磁盘上不写 = codec 默认值 = `bidirectional=true` / `xp_cost=3` / 无催化剂**。
运行期三个值都实测到了（见 §3：`menu.xpCost()` 恒为 3；反向配方命中 `reverse=1`；
反向扣 3 点说明 `reverse_xp_cost=3` 生效）。要「字段显式落盘」必须改 codec，那会连带改既有 21 条产出，
与验收 4（既有 21 条不动）冲突，故不做。

## 3. 验收 2（真实 `ConverterMenu` + `clickMenuButton(BUTTON_CONVERT)`）

### 调用路径（可复核）

1. `new ConverterMenu(id, fakePlayer.getInventory(), fakePlayer, ContainerLevelAccess.create(level, pos))`
   —— 走 `ConverterMenu(int, Inventory, Player, ContainerLevelAccess)` 这个**服务端**构造器；
   内部 `resolveContainer(access)` 拿到的容器就是方块实体本身（`level.getBlockEntity(pos) instanceof ConverterBlockEntity`）。
2. 输入：`menu.getSlot(ConverterMenu.SLOT_INPUT).set(new ItemStack(input, 1))` —— `Slot#set` → `Container#setItem`，
   和客户端点击落进槽位是同一条路（不直接调方块实体的私有方法）。
3. 触发：**`menu.clickMenuButton(fakePlayer, ConverterMenu.BUTTON_CONVERT)`**
   —— `ConverterScreen` 的按钮发的 `ServerboundContainerButtonClickPacket` 最终也落在
   `AbstractContainerMenu#clickMenuButton`（`ConverterScreen` 第 56 行 `handleInventoryButtonClick(containerId, ConverterMenu.BUTTON_CONVERT)`）。
   这条路径依次经过 `player instanceof ServerPlayer` → `container instanceof ConverterBlockEntity` →
   `converter.stillValid(player)` → `converter.tryConvert(serverPlayer)`。
4. 玩家：`FakePlayerFactory.getMinecraft(level)`（NeoForge `FakePlayer`，是 `ServerPlayer` 子类，带经验能力）。
   `/xp query` 用**它自己的** `createCommandSourceStack().withPermission(4)` 执行并抓回命令输出文案——
   FakePlayer 不在玩家列表里，`@a/@p` 选不到它，但 `@s` 从它自己的命令源解析得到。
5. 起手经验：先清零再 `giveExperiencePoints(200)` → 11 级、进度 12 点、total 200。

### 实测读数（`.work/f73-verify-out.txt`，一次跑通 0 FAIL）

| case | 输入 → 产物 | clickMenuButton | `/xp query @s levels` 前→后 | `/xp query @s points` 前→后 | totalExperience 前→后 | 背包 |
|---|---|---|---|---|---|---|
| wild_forward | minecraft:golden_apple → farmstorm:wild_apple | **true** | 11 → 11 | **12 → 9** | 200 → 197 | wild_apple ×1 |
| wild_reverse | farmstorm:wild_apple → minecraft:golden_apple | **true** | 11 → 11 | **9 → 6** | 197 → 194 | + golden_apple ×1 |
| molotov | minecraft:crimson_fungus → farmstorm:molotov | **true** | 11 → 11 | **6 → 3** | 194 → 191 | + molotov ×1 |
| zangief | minecraft:sugar_cane → farmstorm:zangief_cane | **true** | 11 → 11 | **3 → 0** | 191 → 188 | + zangief_cane ×1 |

每个 case 另有 7 条断言全 PASS：`menu.canConvert()=true`、`menu.xpCost()=3`、返回 true、背包出现产物、
total 减 3、经验点减 3、未跨级、输入槽清空、转换后 `canConvert()=false`。
每个 case 的 `ContainerData[2]`（reverse）也一起打了：正向 `reverse=0`、反向 `reverse=1`
（说明反向命中的是同一份 JSON 的 `reverse()` 视图，不是另一条配方）。

### ⚠️ 与验收文案的一处偏差（请调度方定夺，我没有替它"圆"过去）

任务书写的是「`/xp query @s levels` 两次各**少 3 点**」。实测：**扣的是 3 个经验点（points / totalExperience），
不是 3 级**；11 级时进度点 12→9、total 200→197，`levels` 读数保持 11 不变。
依据在代码里：`ConverterBlockEntity.plan()` 判 `player.totalExperience < xpCost`，
执行期 `player.giveExperiencePoints(-plan.xpCost())`，`XP_COST=3` 是**点**；`ConverterRecipe` 的字段名也是 `xp_cost`。

为了把「那 3 点确实扣在玩家身上、且跨级时会掉级」也钉死，加跑了一个边界 case：

```
（boundary）把状态摆到 11 级、进度 0 点、total=187
转换前：/xp query @s levels -> [Minecraft] has 11 experience levels [rc=11] / points -> ... 0 experience points [rc=0] / total=187
clickMenuButton(player, BUTTON_CONVERT) = true
转换后：/xp query @s levels -> [Minecraft] has 10 experience levels [rc=10] / points -> ... 24 experience points [rc=24] / total=184
  [PASS] boundary：3 点扣除跨级时 levels 正好掉 1 级
  [PASS] boundary：total 仍然只减 3
```

即：进度点够时 levels 不动；进度点为 0 时扣 3 点会跨一级、levels 掉 1。
所以「levels 各少 3 点」在实现口径下应读作「各少 3 个经验点」；若文档要字面成立，得改成扣 3 级，
那是玩法口径变更（D-12 明写「每次转换扣除的经验点数」「绝不扣等级」），**不在 F-73 边界内，我没动**。

### 探针与残留

- 临时探针类 `com.dashpig.farmstorm.probe.F73Probe`（只在验证期存在，注册了 `/f73probe setup|convert`），
  **已删除**；`Select-String -Recurse src -Pattern 'F73Probe'` = **0**，`build\classes` 下 `*F73Probe*` = **0**，
  `build/libs/farmstorm-0.0.2+mc1.21.1.jar` 内 `*Probe*` 条目 = **0**。
- 为什么需要它：headless 下没有客户端 GUI，`clickMenuButton` 只能在服务端用真实菜单 + 真实方块实体 + 真实
  `ServerPlayer` 子类（FakePlayer）触发；vanilla 命令做不到（`@a/@p` 选不到 FakePlayer）。
- 驱动器：`.work/f73-verify.js`（RCON）；完整输出：`.work/f73-verify-out.txt`；
  服务端日志里 94 行 `[F73PROBE]` 摘录：`.work/f73-probe-log.txt`。

## 4. 验收 3（`farmstorm:converting` 共 24 条）

```
$ (Select-String -Path src\generated\resources\data\farmstorm\recipe\*.json -Pattern '"type": "farmstorm:converting"' | Measure-Object).Count
24
$ (Get-ChildItem src\generated\resources\data\farmstorm\recipe -File).Count
26          # 24 条转换 + converter.json（合成）+ compressed_puffed_wheat.json（无序合成）
```

## 5. 验收 4（既有 21 条与转换成本键零改动）

```
$ git status --porcelain src/generated
?? src/generated/resources/data/farmstorm/recipe/molotov.json
?? src/generated/resources/data/farmstorm/recipe/wild_apple.json
?? src/generated/resources/data/farmstorm/recipe/zangief_cane.json     ← 只有这 3 个 ??，没有任何 M

$ git diff --numstat -- src/generated
（空输出）

$ git status --porcelain          # 全仓（除 .work 证据文件外）
 M src/main/java/com/dashpig/farmstorm/datagen/ModRecipeProvider.java
?? <上面 3 个新 JSON>

$ git diff -- src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java
（空输出）  → converter.xpCost 的默认值仍是 .defineInRange("xpCost", 3, 0, 1000)
```

即：21 条既有转换配方逐字节未动、`reverse_xp_cost` 未被改写、config 未动。
本任务唯一被 `git diff` 记到的"删除"行只有 `ModRecipeProvider` 类注释里的条数（21 → 24），与数据无关。

## 6. 两次 `runData --all`（V-23 已修好，直接用，无 init script）

```
$ .tools\gw.cmd runData -PfsRunDir=run-f73        # 第一次  .work/f73-data-1.log
[16:27:39] [main/INFO] [minecraft/HashCache]: Caching: total files: 184, old count: 181, new count: 185, removed stale: 0, written: 3
BUILD SUCCESSFUL in 17s

$ .tools\gw.cmd runData -PfsRunDir=run-f73        # 第二次  .work/f73-data-2.log
[16:28:05] [main/INFO] [minecraft/HashCache]: Caching: total files: 184, old count: 184, new count: 185, removed stale: 0, written: 0
BUILD SUCCESSFUL in 17s
```

`written: 3` = 正好三个新 JSON；`written: 0` = 幂等。

## 7. build

```
$ .tools\gw.cmd build                             # .work/f73-build.log（删掉 3 个 class 与 2 个 jar 后重跑）
> Task :build
BUILD SUCCESSFUL in 8s
5 actionable tasks: 2 executed, 3 up-to-date

$ build\libs
farmstorm-0.0.2+mc1.21.1.jar          2,179,834
farmstorm-0.0.2+mc1.21.1-sources.jar  2,105,882
jar 内 data/farmstorm/recipe/ 条目 27（26 个文件 + 目录），含 molotov/wild_apple/zangief_cane 三条；
jar 内 *Probe* 条目 0。
```

## 8. 环境纪律

| 项 | 记录 |
|---|---|
| 运行目录 | `-PfsRunDir=run-f73`（runData / runServer 同目录） |
| 服务端 | `.tools\gw.cmd runServer -PfsRunDir=run-f73`，`Done (0.417s)` + `RCON running on 0.0.0.0:25620`（server-port 25619） |
| forceload | `forceload add 96 96 112 112`（**方块坐标**，V-16）→ "Marked 4 chunks in Overworld" |
| 清场基线 | `fill 96 96 96 112 112 112 minecraft:air` + `kill @e[type=minecraft:item]` → `execute if block 104 100 100 minecraft:air` = Test passed |
| 收尾 | RCON `stop` → `Stopping the server`，`Get-Process java` = **0**，`.tools/runserver.lock` = **False**（已删） |
| 提交 | **未 commit** |

## 9. 一句话结论

三条配方按 v0.0.1 同形落地（双向、经验 3、无催化剂），`runData --all` 两次 `written: 3 / 0`，
`farmstorm:converting` 24 条，既有 21 条与 config 零改动；
验收 2 走的是真实 `ConverterMenu#clickMenuButton(BUTTON_CONVERT)`，金苹果 ⇄ 狂野苹果各成功一次，
外加另两条正向与一个跨级边界 case，共 30 条断言全 PASS。
**唯一需要调度方裁决的是文案口径**：「`/xp query @s levels` 少 3 点」实测是 levels 不变、**points 少 3**
（实现与 D-12 一致，扣经验点不扣等级）。
