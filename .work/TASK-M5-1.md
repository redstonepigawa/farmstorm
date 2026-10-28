# 任务书 M5-1 · 方块状态与模型（F-49、F-50）

> 先读 `.work/PLAN.md` §0/§4/§6~§9。前置：M1-A 注册层、M5-0 datagen 三件套已交付；材质 87 张已就位。

## 1. 必读
1. `docs/zh/02-TECH-SPEC.md` §6.2（20 个 blockstate 清单）、§6.3（62 个方块模型的命名规则）、§6.4（22 个物品模型，含「5 个部署型物品模型直接引用方块模型」）。
2. `docs/zh/05-PROMPT-MAIN.md` §4.7（资源一律 DataGen，禁止手写同名 JSON —— R-03）。

## 2. 交付物

### 2.1 `datagen/ModBlockStateProvider.java`
- **20 个 blockstate**：14 个作物方块（`age` 0~3 四变体，模型 `<base>_stage{0..3}`）、5 个部署方块（`face × facing × floating` 全覆盖；悬空复用同一模型）、1 个转换器（4 朝向）。
- **62 个方块模型**：56 个作物阶段模型（父模型 `minecraft:block/crop`）+ 5 个部署方块模型 + 1 个转换器模型（`cube_bottom_top` + 正面独立贴图）。
- `<base>` 取法 = 方块 ID 去掉末尾 `_crop`/`_stem`/`_stalk`/`_block`/`_bush`（TECH §6.3）。
- **贴图名必须与已就位的 87 张材质逐字对应**（`src/main/resources/assets/farmstorm/textures/block/` 下的实际文件名即真理；先 `Get-ChildItem` 列一遍再写 provider，不要凭文档猜）。

### 2.2 `datagen/ModItemModelProvider.java`
- **22 个物品模型**；5 个部署型物品模型 `parent` 直接指向 `farmstorm:block/<id>`（TECH §6.4，不额外画图）；其余 `item/generated` + `layer0` 指向已有贴图。

### 2.3 `datagen/FarmstormDataGen.java`
- 把上面两个 provider 接进 `GatherDataEvent`（**不要加 `bus` 属性**，见 R-13）。
- 已有 3 个 provider（damage type / block tags / item tags）保持不动。**顺序注意**：block tags 必须先于 item tags 构造（item 用 `blockTags.contentsGetter()`），新增的两个 provider 放在末尾即可。

## 3. 约束
- 只写 `datagen/ModBlockStateProvider.java`、`datagen/ModItemModelProvider.java`、`datagen/FarmstormDataGen.java`，以及它们生成的 `src/generated/resources/`。
- 不要碰 `src/main/java` 其它包、`src/main/resources`（材质已定稿）、`build.gradle`、`registry/**`。
- 不手写任何 `assets/**/*.json`（会被下次 runData 覆盖）。

## 4. 验收
1. `cmd /c ""D:\project_farm\.tools\gw.cmd" runData -PfsRunDir=run-m51` → BUILD SUCCESSFUL。
2. 计数证据：`blockstates/*.json` = **20**、`models/block/*.json` = **62**、`models/item/*.json` = **22**（逐个 `Get-ChildItem -Recurse | Measure-Object`）。
3. 抽查内容：贴出 1 个作物 blockstate（含 4 个 age 变体）、1 个部署方块 blockstate（含 face/facing/floating）、`models/item/blast_carrot.json`（应 parent 到 `farmstorm:block/blast_carrot`）、`models/block/converter.json`。
4. **贴图引用零悬空**：写个小脚本或 PowerShell，把生成的每个 model 里的 `textures` 值解析成 `assets/farmstorm/textures/<path>.png` 并检查存在性，输出「悬空引用 0 条」。这一条是本任务最关键的自检——材质名与模型名对不上就会出现紫黑方块。
5. `gw.cmd build -PfsRunDir=run-m51` 绿；`python tools/verify_project.py` 仍 FAIL 0。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外给出「20/62/22 的实际计数」「悬空贴图引用检查输出」「与 TECH §6.3 命名规则的任何偏差」。
