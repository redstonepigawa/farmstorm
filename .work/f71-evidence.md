# F-71 证据 · 转换器有序 3×3 合成配方与 converter_fuel 物品标签

HEAD: `6e352b7`（F-70 提交后，**未 commit**）｜改动 **2 个文件**：`datagen/ModRecipeProvider.java`、`datagen/ModItemTagsProvider.java`
产物：`data/farmstorm/recipe/converter.json`、`data/farmstorm/tags/item/converter_fuel.json`（均由 runData 生成）

```
$ git diff --stat
 .../farmstorm/datagen/ModItemTagsProvider.java     | 51 ++++++++++++++++-
 .../farmstorm/datagen/ModRecipeProvider.java       | 65 +++++++++++++++++++++-
 2 files changed, 111 insertions(+), 5 deletions(-)
```

## ⚠️ 0. 前置阻塞：HEAD 上 `runData --all` 本来就是坏的（不是 F-71 引入）

第一次跑规定命令 `gw.cmd runData -PfsRunDir=run-f71` 直接失败，栈顶**不在** F-71 改的两个文件里：

```
$ .tools\gw.cmd runData -PfsRunDir=run-f71        # .work/f71-data-1.log
Caused by: java.lang.IllegalStateException: 部署方块缺少外观定义: zangief_cane
        at ...datagen.ModBlockStateProvider.deployedModel(ModBlockStateProvider.java:112)
        at ...datagen.ModBlockStateProvider.deployedStates(ModBlockStateProvider.java:90)
        at ...datagen.ModBlockStateProvider.registerStatesAndModels(ModBlockStateProvider.java:63)
FAILURE: Build failed with an exception.
```

事实链（可用 `git log` / 读文件复核）：

| 事实 | 依据 |
|---|---|
| `zangief_cane` 是 F-65（`ab33fff`）新注册的 `DeployedCropBlock` | `registry/FarmstormBlocks.java`、`FarmstormItems.ZANGIEF_CANE` 用 `DeployableCropItem` |
| `ModBlockStateProvider` 的两张表都没收它 | `DEPLOYED_INVISIBLE` = {blast_carrot, fang_trap_cactus}；`DEPLOYED_VANILLA_PARENT` = {pumpkin_turret, awakened_torchflower, awakened_pitcher_plant} |
| 该文件自 v0.0.1 起没被改过 | `git log -- ModBlockStateProvider.java` 只有 `caebd51`（v0.0.1） |
| 失败与 F-71 无关 | F-71 只加 recipe provider / item tags provider；报错发生在**客户端 blockstate provider**，跑在它们之前 |

也就是说：**从 F-65 落地起，带 `--all` 的 runData 一直跑不通**（缺 `zangief_cane` 的 blockstate/model 定义）。
它属于 blockstate/模型口径（TECH §6.2「20 个 blockstate」在 F-65 之后应为 21），不在 F-71 边界内，
本轮**没有**去改 `ModBlockStateProvider`（改了会顺带产出第 3、4 个新 JSON，违反 F-71 的产物清单）。

为避免越界又不手写 JSON，生成时把 CLI 的 `--all` 换成 `--server`（F-71 两个产物都是 server 侧数据），
用一个**临时 init script** 改 NeoForge 写出的参数文件，不动 `build.gradle`：

```
$ .tools\gw.cmd runData -PfsRunDir=run-f71 -I .work\f71-init.gradle
```
`.work/f71-init.gradle`：在 `projectsEvaluated` 里给 runData 挂 `doFirst`，把
`build/moddev/dataRunProgramArgs.txt` 中的 `--all` 改写成 `--server`（该文件每次配置期重写，故在任务执行前改）。
客户端 provider 一个都不跑 ⇒ 不写、也不改任何客户端资源，产物仍是 runData 生成的、不是手写的。

**建议**（交回调度方定夺）：`zangief_cane` 的部署方块外观归后续 blockstate 任务补（`DEPLOYED_VANILLA_PARENT` 加一条即可），
补完后 `runData --all` 才能恢复；不补则后续所有 datagen 任务都要走 `--server` 绕行。

## 1. 两次 runData 的输出行（幂等）

```
$ .tools\gw.cmd runData -PfsRunDir=run-f71 -I .work\f71-init.gradle    # 第一次
[f71-init] dataRunProgramArgs.txt: --all -> --server
[16:06:37] [main/INFO] [minecraft/HashCache]: Caching: total files: 171, old count: 169, new count: 172, removed stale: 0, written: 2
BUILD SUCCESSFUL in 18s

$ .tools\gw.cmd runData -PfsRunDir=run-f71 -I .work\f71-init.gradle    # 第二次
[f71-init] dataRunProgramArgs.txt: --all -> --server
[16:07:13] [main/INFO] [minecraft/HashCache]: Caching: total files: 171, old count: 171, new count: 172, removed stale: 0, written: 0
BUILD SUCCESSFUL in 20s
```

`written: 2` → 正好两个新文件；`written: 0` → 幂等。日志：`.work/f71-data-2.log`、`.work/f71-data-3.log`。

## 2. git status src/generated（只有预期的 2 个新文件）

```
$ git status --porcelain src/generated
?? src/generated/resources/data/farmstorm/recipe/converter.json
?? src/generated/resources/data/farmstorm/tags/item/converter_fuel.json
```

没有别的文件被改（`M` 一个都没有）——即没有动到别的 provider。

## 3. 两个生成 JSON 的完整内容

`src/generated/resources/data/farmstorm/recipe/converter.json`
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "key": {
    "F": {
      "tag": "farmstorm:converter_fuel"
    },
    "T": {
      "item": "minecraft:crafting_table"
    }
  },
  "pattern": [
    "FFF",
    "FTF",
    "FFF"
  ],
  "result": {
    "count": 1,
    "id": "farmstorm:converter"
  }
}
```

`src/generated/resources/data/farmstorm/tags/item/converter_fuel.json`
```json
{
  "values": [
    "minecraft:wheat", "minecraft:carrot", "minecraft:potato", "minecraft:poisonous_potato",
    "minecraft:beetroot", "minecraft:melon_slice", "minecraft:melon", "minecraft:pumpkin",
    "minecraft:torchflower", "minecraft:pitcher_plant", "minecraft:cactus", "minecraft:bamboo",
    "minecraft:red_mushroom", "minecraft:brown_mushroom", "minecraft:sweet_berries",
    "minecraft:crimson_fungus", "minecraft:sugar_cane", "minecraft:golden_apple",
    "farmstorm:puffed_wheat", "farmstorm:blast_carrot", "farmstorm:ubi_server",
    "farmstorm:toxic_ubi_server", "farmstorm:power_beet", "farmstorm:melon_slice_bomb",
    "farmstorm:melon_grenade", "farmstorm:pumpkin_turret", "farmstorm:awakened_torchflower",
    "farmstorm:awakened_pitcher_plant", "farmstorm:fang_trap_cactus", "farmstorm:bamboo_firecracker",
    "farmstorm:red_mushroom_puff", "farmstorm:brown_mushroom_puff", "farmstorm:lucky_berry",
    "farmstorm:molotov", "farmstorm:zangief_cane", "farmstorm:wild_apple"
  ]
}
```
（上面为便于阅读折行；磁盘上是逐条一行的标准 datagen 输出，共 **36 项**。）

## 4. 验收 1（JSON 形状）

```
$ (Get-Content .../converter.json -Raw | ConvertFrom-Json) |> 取值
type=minecraft:crafting_shaped  result=farmstorm:converter  count=1
pattern=FFF/FTF/FFF  keyF.tag=farmstorm:converter_fuel  keyT.item=minecraft:crafting_table
```
八个外围格全部指向**同一个 tag**（`pattern` 里 8 个 `F` → `key.F.tag`），中心 `T` 是工作台，产物 1 个。

## 5. 验收 2（标签内容 + 逐条命中抽验）

内容口径取 **DESIGN §2.5**（BRIEF 只给口径，DESIGN 把它落成规则）：
**标签 = 19 条档案中除 `compressed_puffed_wheat` 之外的 18 条改良作物物品 + 它们各自的原版参照（18 项）= 36**。

| 分类 | 入标签？ | 依据 |
|---|---|---|
| 原版 15 个参照（wheat/carrot/potato/poisonous_potato/beetroot/melon_slice/melon/pumpkin/torchflower/pitcher_plant/cactus/bamboo/red_mushroom/brown_mushroom/sweet_berries） | ✅ | 01-DESIGN §5，DESIGN §2.5 原版一侧 |
| 新增三条的原版参照（crimson_fungus / sugar_cane / golden_apple） | ✅ | 【P-2】【P-3】D-54，DESIGN §2.5 |
| v0.0.1 的 15 条改良作物 | ✅ | DESIGN §2.5 改良一侧 |
| **molotov / zangief_cane / wild_apple** | ✅ **入** | 它们是 D-51 的「改良作物」本身，DESIGN §2.5 明确列进改良一侧的「新增 3 条」；「无作物方块 / 无种子」只说明种植形态缺失，不影响它们是不是农作物（D-51 结论：三者只从转换器产出，而转换器燃料收的正是这类物品） |
| **compressed_puffed_wheat** | ❌ **不入** | D-56：它是「二级合成物」不是改良农作物；DESIGN §2.5 明确排除，§2.5 排除项 ② |
| 6 个播种物（puffed_wheat_seeds / power_beet_seeds / melon_grenade_seeds / pumpkin_turret_seeds / awakened_torchflower_seeds / awakened_pitcher_plant_pod） | ❌ | 是种子不是农作物，DESIGN §2.5 排除项 ① |
| compressed_hay_block | ❌ | 是方块不是农作物，DESIGN §2.5 排除项 ③ |
| enchanted_golden_apple / apple / converter | ❌ | DESIGN §6「附魔金苹果不在标签」；apple 不在 18 个原版参照里；转换器不在标签（DESIGN §2.6 #3 明说把转换器放进八格不成立） |

**在真实服务端里逐条命中（`/execute if items block ... #farmstorm:converter_fuel`）**，摘录 `.work/f71-verify-out.txt`：

```
> item replace block 100 100 100 container.0 with minecraft:wheat        (同理 golden_apple / crimson_fungus / farmstorm:puffed_wheat / farmstorm:molotov / farmstorm:wild_apple)
> execute if items block 100 100 100 container.0 #farmstorm:converter_fuel
    "Test passed, count: 1
"    → [PASS] 命中标签 ×6（原版 3 条 + 模组 3 条）
> execute if items block 100 100 100 container.0 #farmstorm:converter_fuel   （物品换成 diamond / enchanted_golden_apple / puffed_wheat_seeds / compressed_puffed_wheat / apple / converter）
    "Test failed
"              → [PASS] 未入标签 ×6
```

## 6. 验收 3（摆一次得 1 个；换一格钻石无产物）

无客户端（headless），crafting_shaped 的匹配与工作台走同一个 `RecipeManager` 判定，
故用 **crafter 方块**（9 格容器，槽 0..8 就是 3×3 行优先格位；红石上升沿触发一次合成）代替人工摆工作台。
八格故意**混搭**八种不同燃料（wheat / golden_apple / crimson_fungus / molotov / puffed_wheat / wild_apple / carrot / lucky_berry），
一并验证 DESIGN §2.6 #2「八格可混搭」。

```
## B. 正例
> item replace block 104 100 100 container.4 with minecraft:crafting_table
    "Replaced a slot at 104, 100, 100 with [Crafting Table]
"
  [PASS] 中心格就位
> setblock 105 100 100 minecraft:redstone_block   → 等一下 → setblock 105 100 100 minecraft:air
> data get entity @e[type=minecraft:item,limit=1]
    "Converter has the following entity data: { ... Item: {count: 1, id: \"farmstorm:converter\"}, ... }"
> execute if data entity @e[type=minecraft:item,limit=1] Item{id:"farmstorm:converter"}
    "Test passed, count: 1
"     → [PASS] 产出 item 实体 id = farmstorm:converter
> execute if data entity @e[type=minecraft:item,limit=1] Item{count:1}
    "Test passed, count: 1
"     → [PASS] 产出数量 = 1

## C. 反例：把一格换成 minecraft:diamond
> item replace block 108 100 100 container.0 with minecraft:diamond
    "Replaced a slot at 108, 100, 100 with [Diamond]
"
> （红石上升沿后） execute if entity @e[type=minecraft:item]
    "Test failed
"               → [PASS] 无任何 item 实体

## D. 反例 2：中心不是工作台（钻石块）
> item replace block 104 100 100 container.4 with minecraft:diamond_block
> execute if entity @e[type=minecraft:item]
    "Test failed
"               → [PASS] 无任何 item 实体
```

脚本：`.work/f71-verify.js`；完整输出：`.work/f71-verify-out.txt`（21 条断言，末尾 `### 全部断言通过 (0 FAIL)`）。

## 7. 验收 4（既有 21 条转换配方零改动）

```
$ (Get-ChildItem src\generated\resources\data\farmstorm\recipe -File).Count
22                                     # 21 条既有转换配方 + 1 条新合成配方
$ (Select-String -Path ...\recipe\*.json -Pattern '"type": "farmstorm:converting"' | Measure-Object).Count
21                                     # farmstorm:converting 仍是 21 条
$ git status --porcelain -- src/generated/resources/data/farmstorm/recipe
?? src/generated/resources/data/farmstorm/recipe/converter.json     # 只有新增，没有 M
$ git diff --stat -- src/generated/resources/data/farmstorm/recipe
（空输出）
$ git diff --exit-code -- src/generated/resources/data/farmstorm/recipe ; echo diff-exit
diff-exit=0
```

## 8. build 与探针残留

```
$ .tools\gw.cmd build        # .work/f71-build-final.log
> Task :check UP-TO-DATE
> Task :build

BUILD SUCCESSFUL in 9s
5 actionable tasks: 2 executed, 3 up-to-date

$ build/libs
farmstorm-0.0.2+mc1.21.1.jar          2,172,540
farmstorm-0.0.2+mc1.21.1-sources.jar  2,098,352
```

Probe 残留：源码 `Select-String -Pattern 'Probe'` = **4**，全是既有玩法常量 `LANDING_PROBE`
（`MolotovProjectile` 42/94 行、`PuffedWheatProjectile` 31/64 行，与 F-70 基线一致，**无探针类**）；
`build\classes` 下 `*Probe*` 文件名 = **0**。

## 9. 环境纪律

| 项 | 记录 |
|---|---|
| 运行目录 | `-PfsRunDir=run-f71`（全新 flat 世界，seed 680068，rcon 25610 / \"f71rcon\"，server-port 25609） |
| 服务端 | `.tools\gw.cmd runServer -PfsRunDir=run-f71`，`Done (0.667s)` + `RCON running on 0.0.0.0:25610` |
| forceload | `forceload add 96 96 112 112`（**方块坐标**，V-16） |
| 清场基线 | `fill 96 96 96 112 112 112 minecraft:air` + `kill @e[type=minecraft:item]` → `execute if block 104 100 100 minecraft:air` = Test passed |
| 收尾 | `Stop-Process -Name java -Force` → java 进程 0；`.tools/runserver.lock` 已删（`Test-Path` = False） |
| 未提交 | `git status --porcelain` 只有 2 个 M（两个 java）+ 2 个 ??（两个新 JSON）+ `.work/` 证据文件 |

## 10. 一句话结论

四个验收全过：`converter.json` 形状正确（八格同 tag、中心工作台、产物 1 个）；
`converter_fuel.json` 36 项按 DESIGN §2.5 落全，服务端逐条命中/未命中各 6 条实测；
crafter 实测「中心工作台 + 八格混搭燃料」得 1 个 `farmstorm:converter`，任一格换钻石、或中心换钻石块均无产物；
既有 21 条转换配方 `git diff` 为空。
唯一遗留是**不在本任务边界内**的前置阻塞：`ModBlockStateProvider` 缺 `zangief_cane` 外观定义，使 `runData --all` 在 HEAD 上必然失败（详见 §0）。
