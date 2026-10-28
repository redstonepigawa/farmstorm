# 任务书 M5-D · 87 张材质（F-51）

> 先读 `.work/PLAN.md` §0/§4。本任务**只产出 PNG**，不碰任何 Java 源码与 datagen。

## 1. 目标

用 `tools/gen_textures.py` 批量产出 `src/main/resources/assets/farmstorm/textures/` 下的 87 张占位材质：**item 22 张 + block 64 张 + gui 1 张**（TECH §6.5）。

## 2. 素材来源（作者已选定）

解包**本机**的 Minecraft 客户端 jar 取原版作物贴图，再叠加右下角绿色「+」：
- jar 路径：`D:\minecraft\.minecraft\versions\26.3-NeoForge_26.3.0.99\26.3-NeoForge_26.3.0.99.jar`
- 里面是标准 `assets/minecraft/textures/block/*.png`（小麦/胡萝卜/马铃薯/甜菜根/西瓜/南瓜/火把花/瓶子草/仙人掌/竹子/蘑菇/甜浆果等）。
- **注意版本差异**：这是 MC 26.3 而不是 1.21.1，部分贴图可能已重绘或改名。找不到某个原版贴图时，**不要报错停下**：退回程序生成的占位色块（`gen_textures.py` 已有该能力），并在回报里逐条列出「哪些用了原版、哪些退回占位、哪些找不到」。

## 3. 交付物

1. `src/main/resources/assets/farmstorm/textures/item/<22 个 id>.png` —— 15 个作物图标右下角必须有绿色「+」（plan 第 8 条），尺寸断言 16×16。
2. `src/main/resources/assets/farmstorm/textures/block/<base>_stage{0..3}.png` 共 56 张（14 作物 × 4；`<base>` 取法见 TECH §6.3：去掉末尾 `_crop`/`_stem`/`_stalk`/`_block`/`_bush`）。
3. `.../textures/block/` 下 5 张部署方块贴图 + 3 张转换器（`converter_top` / `_side` / `_front`）= 8 张。
4. `src/main/resources/assets/farmstorm/textures/gui/converter.png` —— **176×166**。
5. 若 `tools/gen_textures.py` 需要改动才能跑通（路径约定、缺图回退、幂等），**可以改它**，但保留 `--list` / `--dry-run` / `--vanilla-assets` / `--out` 四个开关的语义，并把最终用法写进脚本头注释。

## 4. 约束
- 禁止手绘、禁止引入图片库；只用 Pillow（运行时自带）。
- **幂等**：连续跑两次输出 md5 必须一致（F-51 验收③）。
- 只写 `src/main/resources/assets/farmstorm/textures/**` 与 `tools/gen_textures.py`；**不要碰** `src/main/java`、`datagen`、`build.gradle`、`src/generated`。
- 不新增第三方依赖。

## 5. 验收
1. 贴出一次完整运行日志（`--dry-run` 与实跑各一次）。
2. 文件计数证据：item 22 / block 64 / gui 1 = **87**，逐个 `Get-ChildItem` 汇总。
3. 尺寸证据：随机抽 5 张 item + 5 张 block + gui，用 Pillow 打印 `(w,h)`；gui 必须是 176×166，item 必须 16×16。
4. 幂等证据：连跑两次后对全目录算一次汇总 md5（或逐文件 md5 拼接），两次一致。
5. `cmd /c ""D:\project_farm\.tools\gw.cmd" build` 仍绿（材质进了 jar 的 `assets/farmstorm/textures/`，贴一条 jar 内文件计数）。

## 6. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外给出「原版命中 / 退回占位 / 找不到」三类清单，方便作者后续用真 1.21.1 资源替换。
