# 任务书 V002-F-71 · 转换器合成配方与 converter_fuel 标签

## 目标（13-TASKS-0.0.2.md §3 F-71）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 F-65（ab33fff）
- 一句话：让转换器第一次变成可合成物——有序 3×3，中心工作台、周围八格收 farmstorm:converter_fuel；并新建这个物品标签。

## 交付物

| # | 文件 | 改动 |
|---|---|---|
| 1 | datagen/ModRecipeProvider.java | 一条 ShapedRecipeBuilder：3×3，中心 minecraft:crafting_table，其余 8 格全部 #farmstorm:converter_fuel → 1 × farmstorm:converter |
| 2 | datagen/ModItemTagsProvider.java | 新建物品标签 farmstorm:converter_fuel |
| 3 | 产物（由 runData 生成，不许手写） | src/generated/resources/data/farmstorm/recipe/converter.json 与 .../tags/item/converter_fuel.json |

## 标签内容（以 10-DECISIONS-0.0.2.md 的 D-52 为准）

D-52 口径：原版农作物 + 本模组全部改良作物。落地至少覆盖：
- 原版：minecraft:wheat / carrot / potato / beetroot（以及原版里同属农作物的其它项，按 D-52 判断）
- 本模组 19 条档案里有物品的那些

注意有若干条档案没有作物方块与种子（例如 molotov / zangief_cane / wild_apple / compressed_puffed_wheat）。它们的物品是否入标签，要按 D-52 与 11-DESIGN 的设计意图判断，不要凭直觉猜；判断依据写进回报。

## 验收（四条）

1. converter.json 的 type 为 minecraft:crafting_shaped，八个 key 位置指向同一 tag，result.item 为 farmstorm:converter、count 为 1。
2. converter_fuel.json 的 values 覆盖上面那批；逐条 /give 抽验 5 条命中（原版与模组各取几条）。
3. 工作台按配方摆一次得 1 个 farmstorm:converter；把任一格换成 minecraft:diamond 后无产物。
4. 回归：既有 21 条转换配方 JSON 的 git diff 必须为空（BRIEF §3.4 冻结）。

## datagen 纪律

- 只用 gw.cmd runData -PfsRunDir=run-f71 生成，不许手写 src/generated 下的 JSON（下次 runData 会覆盖）。
- 生成后跑第二次 runData，应输出 written: 0（幂等）；把两次输出都贴出来。
- 生成后 git status --porcelain src/generated 只应出现预期的那 2 个新文件；若冒出别的文件被改，说明动到了不该动的 provider，要查明原因。

## 环境纪律（前面几轮踩过的）

- 跑服务端用 -PfsRunDir=run-f71；forceload 用方块坐标（V-16）；
- 脚本开头清场与基线断言；世界污染就删 run-f71/world 重跑（V-17）；
- 改 run-*/config/*.toml 不要用带 BOM 的 Set-Content；
- 证据写 .work/；跑完服务端当场 Stop-Process java 并删 .tools/runserver.lock。

## 边界

- 只许动上面那两个 datagen 文件（加 runData 产出的 2 个 JSON）。
- 不许动 FarmstormItems / FarmstormBlocks / config / docs / 既有 21 条配方。

## 回报

- 两个生成 JSON 的完整内容（它们很小）。
- 验收 4 的 git diff 命令与空输出证据。
- 标签内容的判断依据（哪些入了、哪些没入、为什么）。
- 两次 runData 的输出行。
- build 最后两行、Probe 残留计数。
- 不要 commit。
