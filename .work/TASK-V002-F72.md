# 任务书 V002-F-72 · 压缩膨胀麦无序合成

## 目标（13-TASKS-0.0.2.md §3 F-72）

- 复杂度 **S** ｜ 0.5 人日 ｜ 依赖 F-65（已 `ab33fff`）
- 一句话：4 个膨胀麦无序合成 1 个压缩膨胀麦。这是 Phase 12 里最小的一件，别做多。

## 交付物

| # | 文件 | 改动 |
|---|---|---|
| 1 | `datagen/ModRecipeProvider.java` | 一条 `ShapelessRecipeBuilder`：4 × `farmstorm:puffed_wheat` → 1 × `farmstorm:compressed_puffed_wheat` |
| 2 | 产物（由 `runData` 生成，**不许手写**） | `src/generated/resources/data/farmstorm/recipe/compressed_puffed_wheat.json` |

## 两个必须复用的既有做法（F-71 刚建立的）

1. **必须用 `recipeWithoutAdvancement()` 包装**：`ShapedRecipeBuilder#save` / `ShapelessRecipeBuilder#save` 会顺带写出 `advancement/recipes/...` 的第三个 JSON。既有 21 条转换配方与本版新配方都传 null 进度，产物只留一个 recipe JSON。
2. **产物只由 `runData` 生成**，不许手写 `src/generated` 下的 JSON。

## 验收（三条）

1. 生成的 JSON：`type` 为 `minecraft:crafting_shapeless`，`ingredients` 为 **4 组同一物品**（`farmstorm:puffed_wheat`），`result.count` 为 **1**、`result.id` 为 `farmstorm:compressed_puffed_wheat`。
2. 工作台任意位置放 **4 个膨胀麦 → 得 1 个压缩膨胀麦**（`/data get entity @s Inventory` 前后核对，或用 crafter 方块判定，方法自选但要说清）。
3. 放 **3 个无产物**、放 **5 个无产物**。

## datagen 纪律

- `gw.cmd runData -PfsRunDir=run-f72`（**现在 `--all` 已经修好了，不需要任何 init script 绕行**——V-23 刚把 F-65 遗留的缺外观/缺贴图补掉）。
- 生成后跑**第二次**，应 `written: 0`；把两次的 `written` 行都贴出来。
- `git status --porcelain src/generated` **只应出现那 1 个新 recipe JSON**。若冒出别的文件，说明动到了不该动的地方，要查明。

## 环境纪律

- 跑服务端用 `-PfsRunDir=run-f72`；`/forceload add` 用**方块坐标**（V-16）；脚本开头清场 + 基线断言（V-17）；
- 证据写 `.work/`；跑完**当场** `Get-Process java | Stop-Process -Force` 并删 `.tools/runserver.lock`。

## 边界

- 只许动 `datagen/ModRecipeProvider.java`（+ 那 1 个生成 JSON）。
- **不许**动 `ModBlockStateProvider` / `ModItemModelProvider` / `tools/gen_textures.py` / config / docs / 既有配方。

## 回报

- 生成的 JSON **全文**（很小）。
- 验收 2/3 的实际命令与返回原文。
- 两次 `runData` 的 `written` 行；`git status --porcelain src/generated` 原文。
- `build` 最后两行。
- **不要 commit**。
