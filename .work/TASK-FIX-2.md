# 任务书 FIX-2 · 部署方块模型与两件手持物品外观

> 作者实测反馈两条外观问题。只改 **datagen provider + 生成物**，不改 Java 行为逻辑。

## 1. 问题一：5 个部署方块放下后看起来是透明/没有材质

现状：`deployed` 的 5 个方块（`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus`）用的是 `minecraft:block/cross` + 自家 16×16 贴图，作者反馈放下后「没有材质是透明的」。

**作者要求的做法：直接用原版方块模型。**

| 方块 | 目标模型（以客户端 jar 里真实存在的为准） |
|---|---|
| `pumpkin_turret`（man瓜） | `minecraft:block/pumpkin` |
| `fang_trap_cactus`（赢过输过没死过） | `minecraft:block/cactus` |
| `awakened_torchflower`（激活的火把花） | 原版火把花方块模型（先确认 jar 里的确切名字） |
| `awakened_pitcher_plant`（激活的瓶子草） | 原版瓶子草方块模型（同上） |
| `blast_carrot`（胡罗贝） | 原版没有胡萝卜方块，退回可见的作物模型（如 `minecraft:block/carrots_stage3`），并在回报里说明 |

**做法**：`datagen/ModBlockStateProvider.java` 里这 5 个方块的模型改为 `parent` 到对应原版模型（仍要保留 `face`/`facing`/`floating` 三种变体与旋转，blockstate 结构不变，只换模型指向）。

**关键自检**：原版模型里的 `render_type`（cross 类模型多是 cutout）**不会随 parent 继承到我们的方块注册上**。你必须确认这 5 个方块最终要么模型自带 `render_type`、要么有 `ItemBlockRenderTypes` 注册，否则十字形模型会渲染成实心黑块。任选一种并说明你选了哪种。

## 2. 问题二：man瓜 与 瓜！ 拿在手上的图标怪异

现状：`models/item/pumpkin_turret.json` 与 `melon_grenade.json` 用的是扁平 16×16 图标（分别取了 `block/pumpkin_side` 与 `block/melon_side`）。

**作者要求：换成原版的西瓜和南瓜。** 做法：把这两个 item 模型直接 `parent` 到原版方块模型：
- `models/item/pumpkin_turret.json` → `{"parent": "minecraft:block/pumpkin"}`
- `models/item/melon_grenade.json` → `{"parent": "minecraft:block/melon"}`

这样手持与投掷物（`ThrownItemRenderer` 用物品模型）都会渲染成 3D 的南瓜/西瓜，和原版一致。

## 3. 约束
- 只改 `datagen/ModBlockStateProvider.java`、`datagen/ModItemModelProvider.java` 与 `src/generated/resources/` 生成物；必要时可改 `client/` 下的 render type 注册（与 FIX-1 冲突时**先问调度者**，不要两个代理同时改同一个文件）。
- 不手写 `assets/**/*.json`，一律走 `runData`。
- 不碰 `src/main/java` 的其它包、`src/main/resources` 的 PNG、`build.gradle`。

## 4. 验证
1. `cmd /c ""D:\project_farm\.tools\gw.cmd" runData -PfsRunDir=run-fix2` → BUILD SUCCESSFUL；blockstates 仍 20、models/block 仍 62、models/item 仍 22（结构不变，只换 parent）。
2. 贴出这 5 个方块模型的 JSON 全文 + 2 个 item 模型 JSON 全文。
3. 悬空贴图/父模型检查：写脚本把生成物里所有 `parent` 与 `textures` 解引用，确认 **0 条悬空**（之前 M5-1 有现成脚本可复用，在 `.work/` 下找）。
4. `build` 绿；`python -X utf8 tools/verify_project.py` FAIL 0 / WARN 0。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外说明 render_type 你选了哪种方案、以及胡罗贝最终用了什么模型。
