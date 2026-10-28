# 任务书 FIX-5 · 转换器方块贴图（工作台底 + 四面作物）

> 作者要求：转换器方块改成**原版工作台材质作底**，**侧方四个面分别加上小麦 / 胡萝卜 / 马铃薯 / 甜菜根**的贴图。

## 1. 交付物

### 1.1 新贴图（`tools/gen_textures.py` 的**方块贴图**段）
- 以原版 `minecraft:block/crafting_table_side` 为底，分别叠加 `minecraft:item/wheat` / `item/carrot` / `item/potato` / `item/beetroot` 四个作物图标（居中、等比缩到约 9~10 px、必要时加 1px 暗描边保证可读），产出 4 张 16×16：
  `textures/block/converter_side_wheat.png` / `..._carrot.png` / `..._potato.png` / `..._beetroot.png`
- 顶面沿用原版 `crafting_table_top`；底面也给它一个（用 `crafting_table_top` 或一张纯 side 均可，说明你的选择）。
- **只用定点替换改「方块贴图」那一段**，不要整文件重写（另一个代理同时在改这个文件的 GUI 段）。
- 连跑两次幂等；除转换器相关的这几张外，其余 PNG 的 md5 不得变化。

### 1.2 方块模型（`datagen/ModBlockStateProvider.java` 的 converter 段）
- 模型改成四面不同贴图：`north/east/south/west` 分别指向上面 4 张，`up`/`down` 指向顶/底贴图，`particle` 取其中一张。
- 结构仍是 `cube` 全方块；`parent` 可以用 `minecraft:block/cube`（或继续内联 element，以能正常渲染为准）。
- **blockstate 的 4 个 `facing` 变体必须保留**（`models/block/converter.json` 仍只有一个模型文件，靠 blockstate 的 `y` 旋转实现朝向）。注意：模型四面不同后，`facing` 旋转会让作物顺序跟着转——这是可接受的，但要确认 4 个 facing 都指向同一个模型、没有空变体。

## 2. 验证
1. `gw.cmd runData -PfsRunDir=run-fix5` → BUILD SUCCESSFUL；`models/block/converter.json` 内容贴回；`blockstates/converter.json` 仍是 4 个变体。
2. 贴图计数：`textures/block/converter*` 由 3 张变成几张，逐张列名 + 尺寸（都必须是 16×16）。
3. **悬空引用检查**：把生成物里所有 `textures` 值解引用到真实 PNG，输出「悬空 0 条」。
4. **截图（必须做）**：`runClient -PfsRunDir=run-fix5`，在玩家四周放一圈（东/南/西/北各一个）转换器并截图，确认四个侧面分别能看到小麦/胡萝卜/马铃薯/甜菜根，顶面是工作台顶。
5. `build` 绿、零 warning；`python -X utf8 tools/verify_project.py` FAIL 0 / WARN 0。

## 3. 边界
只改：`tools/gen_textures.py` 的方块贴图段、`datagen/ModBlockStateProvider.java` 的 converter 段、`src/main/resources/assets/farmstorm/textures/block/converter*.png`、`src/generated/**` 对应生成物。不要碰 Java 其它包、`menu/`、`client/`、`blockentity/`、GUI 贴图段。

## 4. 回报
按 `05-PROMPT-MAIN.md §7` 模板；必须含：4 张新贴图的文件名与尺寸、模型 JSON 全文、悬空引用检查输出、以及**截图**（说明每个方向看到的是哪个作物）。
