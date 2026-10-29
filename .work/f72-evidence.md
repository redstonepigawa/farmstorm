# F-72 证据 · 压缩膨胀麦无序合成（4 × 膨胀麦 → 1）

HEAD: `af1287f`（V-23 之后，**未 commit**）｜改动 **1 个 java 文件**：`datagen/ModRecipeProvider.java`
产物：`src/generated/resources/data/farmstorm/recipe/compressed_puffed_wheat.json`（**由 runData 生成**，非手写）

```
$ git status --porcelain
 M src/main/java/com/dashpig/farmstorm/datagen/ModRecipeProvider.java
?? .work/TASK-V002-F72.md
?? .work/f72-build.log
?? .work/f72-data-1.log
?? .work/f72-data-2.log
?? .work/f72-server.log
?? .work/f72-verify-out.txt
?? .work/f72-verify.js
?? src/generated/resources/data/farmstorm/recipe/compressed_puffed_wheat.json
```

## 1. 改动（只加一条配方，复用 F-71 的 `recipeWithoutAdvancement()`）

```java
    private static void compressedPuffedWheat(RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, FarmstormItems.COMPRESSED_PUFFED_WHEAT.get())
                .requires(FarmstormItems.PUFFED_WHEAT.get(), 4)
                .unlockedBy("has_puffed_wheat", has(FarmstormItems.PUFFED_WHEAT.get()))
                .save(recipeWithoutAdvancement(recipeOutput), Farmstorm.id("compressed_puffed_wheat"));
    }
```

- `buildRecipes` 里 `converter(recipeOutput)` 之后加一行 `compressedPuffedWheat(recipeOutput)`；类注释提一句（D-67 / F-72）。
- `requires(ItemLike, int)` 逐个展开成 4 份原料 ⇒ `ingredients` 落盘为 4 组（不是 1 组 count:4）。
- `save(...)` 走 `recipeWithoutAdvancement(recipeOutput)`（F-71 建立的包装），`advancement` 传 null。

## 2. 两次 runData（幂等）

```
$ .\.tools\gw.cmd runData -PfsRunDir=run-f72        # 第一次  （.work/f72-data-1.log）
> Task :runData
[16:20:01] [main/INFO] [minecraft/HashCache]: Caching: total files: 181, old count: 180, new count: 182, removed stale: 0, written: 1
BUILD SUCCESSFUL in 17s

$ .\.tools\gw.cmd runData -PfsRunDir=run-f72        # 第二次  （.work/f72-data-2.log）
> Task :runData
[16:20:31] [main/INFO] [minecraft/HashCache]: Caching: total files: 181, old count: 181, new count: 182, removed stale: 0, written: 0
BUILD SUCCESSFUL in 16s
```

`written: 1` → 正好一个新文件；`written: 0` → 幂等。**没有** `--all` 绕行（V-23 修好了，直接用 `--all` 默认参数）。

## 3. `recipeWithoutAdvancement()` 生效的直接证据

```
$ git status --porcelain src/generated
?? src/generated/resources/data/farmstorm/recipe/compressed_puffed_wheat.json

$ Get-ChildItem src\generated\resources\data\farmstorm\advancement -Recurse -Directory
（无输出 ⇒ 没有 advancement/recipes/ 目录）

$ 既有 advancement 文件（12 个，全是 story 进度，没有被新增）
aura_bath, converter, demolition, fang_dance, first_conversion, first_deploy,
first_throw, lucky_jackpot, nether_star_toll, root, seed_cycle, turret_hit
```

只有 1 个 `??`、没有任何 `M`，也没有第三个 advancement JSON。

## 4. 生成的 JSON 全文（`compressed_puffed_wheat.json`）

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    {
      "item": "farmstorm:puffed_wheat"
    },
    {
      "item": "farmstorm:puffed_wheat"
    },
    {
      "item": "farmstorm:puffed_wheat"
    },
    {
      "item": "farmstorm:puffed_wheat"
    }
  ],
  "result": {
    "count": 1,
    "id": "farmstorm:compressed_puffed_wheat"
  }
}
```

## 5. 验收 1（JSON 形状）——通过

```
$ $j = Get-Content .../compressed_puffed_wheat.json -Raw | ConvertFrom-Json ; 取值
type=minecraft:crafting_shapeless
ingredient_groups=4
ingredient_ids=farmstorm:puffed_wheat,farmstorm:puffed_wheat,farmstorm:puffed_wheat,farmstorm:puffed_wheat
distinct_ids=1
result.id=farmstorm:compressed_puffed_wheat  result.count=1
```

## 6. 验收 2 / 3（真实服务端实测）——通过

**判定方法（说清）**：headless 没有客户端，`crafting_shapeless` 的匹配与工作台走**同一个 `RecipeManager`**，
故用 **crafter 方块**（9 格容器，槽 `container.0..8` = 3×3 行优先格位；红石上升沿触发一次合成）代替人工摆工作台。
每轮先 `data get block` 复述容器 9 格实际内容（证明摆的是 4 个**独立格位**各 1 个膨胀麦），
脉冲后 `data get entity @e[type=minecraft:item,limit=1]` 看产物实体。

```
## B. 验收 2-a：4 个膨胀麦放在四角（槽 0/2/6/8）
> data get block 104 100 100
    ... {Items: [{count: 1, Slot: 0b, id: "farmstorm:puffed_wheat"}, {count: 1, Slot: 2b, ...},
                {count: 1, Slot: 6b, ...}, {count: 1, Slot: 8b, ...}], id: "minecraft:crafter"}
> data get entity @e[type=minecraft:item,limit=1]
    "item.farmstorm.compressed_puffed_wheat has the following entity data: { ... Item: {count: 1,
     id: \"farmstorm:compressed_puffed_wheat\"}, ...}"
> execute if data entity @e[type=minecraft:item,limit=1] Item{id:"farmstorm:compressed_puffed_wheat"}
    "Test passed, count: 1"          → [PASS] 产物 id
> execute if data entity @e[type=minecraft:item,limit=1] Item{count:1}
    "Test passed, count: 1"          → [PASS] 产物数量 = 1

## C. 验收 2-b：换一组位置（槽 1/3/5/7）——证明「任意位置」
> data get block 108 100 100   → Items 的 Slot 是 1b/3b/5b/7b
> execute if data entity @e[type=minecraft:item,limit=1] Item{id:"farmstorm:compressed_puffed_wheat"}
    "Test passed, count: 1"          → [PASS]
> execute if data entity @e[type=minecraft:item,limit=1] Item{count:1}
    "Test passed, count: 1"          → [PASS]

## D. 验收 3-a：只放 3 个（槽 0/4/8）
> data get block 100 100 100   → Items 只有 3 条（Slot 0b/4b/8b）
> data get entity @e[type=minecraft:item,limit=1]
    "No entity was found"
> execute if entity @e[type=minecraft:item]
    "Test failed"                    → [PASS] 无任何产物

## E. 验收 3-b：放 5 个（槽 0/1/3/4/8）
> data get block 112 100 100   → Items 共 5 条
> data get entity @e[type=minecraft:item,limit=1]
    "No entity was found"
> execute if entity @e[type=minecraft:item]
    "Test failed"                    → [PASS] 无任何产物

## F. 附加边界：4 个膨胀麦堆在**同一格**（container.4 一叠 count:4）
> data get block 104 100 104   → {Items: [{count: 4, Slot: 4b, id: "farmstorm:puffed_wheat"}]}
> execute if entity @e[type=minecraft:item]
    "Test failed"                    → [PASS] 一叠 4 个 ≠ 4 份原料，无产物
```

脚本：`.work/f72-verify.js`；完整输出：`.work/f72-verify-out.txt`，末尾 `### 全部断言通过 (0 FAIL)`（7 条断言）。

服务端加载无异常：`[16:20:58] [main/INFO] [minecraft/RecipeManager]: Loaded 1313 recipes`（无解析错误行）。

## 7. build

```
$ .\.tools\gw.cmd build        # .work/f72-build.log
> Task :build

BUILD SUCCESSFUL in 9s
5 actionable tasks: 2 executed, 3 up-to-date

$ build\libs
farmstorm-0.0.2+mc1.21.1.jar          2,178,869
farmstorm-0.0.2+mc1.21.1-sources.jar  2,104,913
```

## 8. 环境纪律

| 项 | 记录 |
|---|---|
| 运行目录 | `-PfsRunDir=run-f72`（全新 flat 世界，seed 720072，server-port 25611 / rcon 25612 / `f72rcon`） |
| 服务端 | `Done (1.454s)! For help, type "help"` + `RCON running on 0.0.0.0:25612` |
| forceload | `forceload add 96 96 112 112`（**方块坐标**，V-16） |
| 清场基线 | `fill 96 96 96 112 112 112 minecraft:air` + `kill @e[type=minecraft:item]` → `execute if block 104 100 100 minecraft:air` = `Test passed` |
| 收尾 | `Get-Process java \| Stop-Process -Force` → java 进程 **0**；`.tools/runserver.lock` 已删（`Test-Path` = False） |
| 未提交 | 未 commit（`git status` 见文首） |

## 9. 一句话结论

三条验收全过：JSON 是 `crafting_shapeless` + 4 组同一物品 + `result.count: 1`；
crafter 实测两种摆法（四角 / 1-3-5-7）各得 1 个压缩膨胀麦，3 个与 5 个都无产物；
`recipeWithoutAdvancement()` 生效（`written: 1`、generated 下只多 1 个 recipe JSON、没有 `advancement/recipes/`）；
未 commit。
