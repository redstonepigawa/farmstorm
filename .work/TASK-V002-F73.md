# 任务书 V002-F-73 · 三条新转换配方

## 目标（13-TASKS-0.0.2.md §3 F-73）

- 复杂度 **S** ｜ 0.5 人日 ｜ 依赖 F-65（`ab33fff`）、F-71（`b33b3a0`）
- 一句话：给三个新作物各加一条**双向转换配方**，经验成本沿用既有键（默认 3），无催化剂。

## 交付物

| 源 → 产物（双向） |
|---|
| `minecraft:crimson_fungus` ⇄ `farmstorm:molotov` |
| `minecraft:sugar_cane` ⇄ `farmstorm:zangief_cane` |
| `minecraft:golden_apple` ⇄ `farmstorm:wild_apple` |

- 代码：`datagen/ModRecipeProvider.java` 里三条 `ConverterRecipe`（与 v0.0.1 那 13 条同形：`bidirectional=true`、经验成本读既有 config 键（默认 3）、**无催化剂**）。
- 产物：`data/farmstorm/recipe/{molotov,zangief_cane,wild_apple}.json`，由 `runData` 生成。

## 验收（四条）

1. 三个 JSON 的 `type` 为 `farmstorm:converting`、`bidirectional` 为 `true`、`experience` 为 **3**。
2. 转换器**真实路径**：金苹果 → 狂野苹果、狂野苹果 → 金苹果各成功一次；`/xp query @s levels` 两次各**少 3 点**。
3. `farmstorm:converting` 类配方在 `src/generated/resources/data/farmstorm/recipe/` 下共 **24** 条（既有 21 + 新增 3）。
4. **回归**：既有 21 条转换配方与转换成本键的默认值一个都没改——`git diff` 只含新增行、`git status` 里那 21 个文件不该出现 `M`。

## 验收 2 怎么做（headless，没有客户端 GUI）

不要图省事直接调 `ConverterBlockEntity` 的私有方法。要走**与 GUI 按钮相同的入口**：
- 构造/使用真实 `ConverterMenu`，放好输入与催化剂槽，然后调 `clickMenuButton(BUTTON_CONVERT)`（`ConverterScreen` 的按钮点下去最终就是这条路径）。
- 玩家用 FakePlayer（需要 `xp` 能力，`giveExperienceLevels` 前后各读一次 `experienceLevel`）。
- 若某个环节 headless 确实做不到，**如实写「未验证」并说明卡在哪**，不要用「代码看起来对」替代。

## datagen 纪律

- `runData --all` 现在**已修好**（V-23），直接用 `gw.cmd runData -PfsRunDir=run-f73`，**不要**再写 init script 绕 `--server`。
- 生成后跑第二次应 `written: 0`；两次都要贴 `written` 行。
- `git status --porcelain src/generated` 只应出现那 **3 个新 JSON**；冒出别的文件就要查明。

## 环境纪律

- 服务端 `-PfsRunDir=run-f73`，端口自定；`/forceload add` 用**方块坐标**（V-16）；脚本开头清场 + 基线断言（V-17）；
- 证据写 `.work/`；跑完**当场** `Stop-Process java` 并删 `.tools/runserver.lock`。

## 边界

- 只许动 `datagen/ModRecipeProvider.java` + 那 3 个生成 JSON。
- 不许动 blockstate / item model / textures / loot / tags / config / docs / 既有配方。

## 回报

- 三个生成的 JSON **全文**。
- 验收 2 的调用路径（你具体调了哪个方法）与 `/xp query` 前后读数。
- 验收 3 的计数命令与结果；验收 4 的 `git diff` 证据。
- 两次 `written` 行、`build` 最后两行。
- **不要 commit**。
