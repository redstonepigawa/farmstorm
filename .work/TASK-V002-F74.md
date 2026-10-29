# 任务书 V002-F-74 · 两张战利品表 + 三条方块标签（**V-14 收口点**）

## 目标（13-TASKS-0.0.2.md §3 F-74）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 F-67（已 `76ea279`/`bf3f9ea`）
- **本任务是 V-14 的收口点**：F-67 时「钻石镐挖压缩干草块掉本体」过不了，因为工具标签与战利品表都在这里。

## 交付物

| # | 文件 | 改动 |
|---|---|---|
| 1 | `datagen/ModLootTableProvider.java` | +2 张：`compressed_hay_block` 掉自身；`zangief_cane` 走**部署方块口径**（与既有 5 个部署方块同形） |
| 2 | `datagen/ModBlockTagsProvider.java` | ① `minecraft:mineable/pickaxe` **追加** `compressed_hay_block`；② **新建** `minecraft:needs_diamond_tool`；③ `farmstorm:deployed_crops` **追加** `zangief_cane` |
| 3 | 产物（runData 生成，不许手写） | `data/farmstorm/loot_table/blocks/{compressed_hay_block,zangief_cane}.json`、`data/minecraft/tags/block/needs_diamond_tool.json`、被追加的 `mineable/pickaxe.json` 与 `farmstorm/tags/block/deployed_crops.json` |

## 验收（四条 + V-14 收口）

1. **钻石镐挖压缩干草块 → 掉本体**；**木镐挖 30 秒 → 不掉**（`requiresCorrectToolForDrops` + `needs_diamond_tool` 两条共同生效）。
2. `data/minecraft/tags/block/needs_diamond_tool.json` 的 `values` 为 `["farmstorm:compressed_hay_block"]`（新建文件）。
3. `data/minecraft/tags/block/mineable/pickaxe.json` 的 `values` **追加**了 `farmstorm:compressed_hay_block`，**既有条目逐条仍在**（是追加不是覆盖）。
4. `data/farmstorm/tags/block/deployed_crops.json` 的 `values` 含 `farmstorm:zangief_cane`；`data/farmstorm/loot_table/blocks/zangief_cane.json` 存在，且**玩家自己挖掉时掉自身**（触发消失不掉是 F-87 的事，见 A-08）。

## 怎么做「挖」这条验收（F-67 的教训，直接照抄）

- 用 `gameMode.handleBlockBreakAction(START_DESTROY_BLOCK)` + 逐 tick `gameMode.tick()`，**不要**直接调 `destroyBlock`（那会绕过挖掘进度与工具门槛，得到假结果）。
- **FakePlayer 必须 `setOnGround(true)`**：`Player#getDigSpeed` 末尾有 `if (!onGround()) f /= 5.0F`，F-67 因此把 150 秒误算成 750 秒。
- 「30 秒不掉」的正确判据是**进度不够、方块没破**（F-67 实测 600 tick 只到 20%），而不是"破了但没掉"。两种都要报：600 tick 后方块是否还在、掉落是否为 0。
- 请把两个工具的实测数字报出来：`进度/tick`、600 tick 的进度、挖穿所需 tick 数。

## datagen 纪律

- `runData --all` 已修好（V-23），直接用 `gw.cmd runData -PfsRunDir=run-f74`，**不要**写 init script 绕行。
- 生成后跑第二次应 `written: 0`；`git status --porcelain src/generated` 只应出现预期文件（2 张新 loot + 1 个新 tag + 2 个被追加的 tag），冒出别的要查明。
- 既有 21 条转换配方、既有 12 条进度、既有方块标签的其它条目**一个都不许动**。

## 环境纪律

- `-PfsRunDir=run-f74`；`/forceload add` 用**方块坐标**（V-16）；脚本开头清场 + 基线断言（V-17）；
- 证据写 `.work/`；跑完**当场** `Stop-Process java` 并删 `.tools/runserver.lock`。

## 边界

- 只许动 `datagen/ModLootTableProvider.java`、`datagen/ModBlockTagsProvider.java` + 上面那些生成文件。
- 不许动 `ModBlockStateProvider` / `ModItemModelProvider` / `ModRecipeProvider` / `tools/gen_textures.py` / config / docs。

## 回报

- 四条验收的实际命令与返回原文；**V-14 的两个工具对照数字**（进度/tick、600 tick 进度、挖穿 tick、掉落数）。
- 两个新 loot JSON 与 `needs_diamond_tool.json` 全文；`mineable/pickaxe.json` 追加前后的对比（证明既有条目没丢）。
- 两次 `written` 行、`build` 最后两行、`Probe` 残留计数。
- **不要 commit**。
