# 任务书 M5-2 · 配方 / 战利品表 / 进度 / 语言（F-44 ~ F-48、F-52）

> 先读 `.work/PLAN.md` §0/§4/§6~§9。前置：M5-0（damage type + tags）、M5-1（blockstate + 模型）、M4-C（转换器配方 schema）已交付。

## 1. 必读
1. `docs/zh/02-TECH-SPEC.md` §6.7（21 条配方表、20 张战利品表规则、12 条进度表及其 frame/announce/background 约定）、§7（语言键命名规范 216 键，含 §7.2 的完整示例条目）。
2. `docs/zh/01-DESIGN.md` §6（12 条进度清单）、§3.1.3（转换配方的输入输出对照）、§3.2.2（播种闭环对应表）、§3.8（117 个 config 键的 `farmstorm.configuration.*` 翻译键 —— 语言文件里必须**全部覆盖**）。
3. `docs/zh/00-DECISIONS.md`：C-7（进度 12 条）、D-12（火把花/瓶子草单向 + 下界之星）、D-29。

## 2. 交付物（4 个 provider + 1 处接线）
`datagen/ModRecipeProvider.java`、`datagen/ModLootTableProvider.java`、`datagen/ModAdvancementProvider.java`、`datagen/ModLangProvider.java`、以及 `datagen/FarmstormDataGen.java` 里的接线。

### 2.1 配方（21 条）
- 逐条按 TECH §6.7 的表；`type` 固定 `farmstorm:converting`；字段约定以 M4-C 回报里的 7 项为准（`input` / `result`（`{"id":...}`）/ `xp_cost` / `catalyst` / `catalyst_count` / `bidirectional` / `reverse_xp_cost`），**每条显式写 `xp_cost: 3`**。
- 火把花 / 瓶子草两条：`bidirectional: false` + `catalyst: {"item":"minecraft:nether_star"}` + `catalyst_count: 1`。

### 2.2 战利品表（20 张）
- 14 张作物表：成熟（`age=3`）按各自原版比例同时掉作物 + 播种物（时运加成照 TECH §6.7 的逐条比例），未成熟只掉 1 个播种物；5 张部署表默认掉自身 + `survives_explosion`；1 张转换器表只掉本体（内容物由 `onRemove` 的 `dropContents` 负责）。

### 2.3 进度（12 条）
- 逐条按 TECH §6.7 的表（ID / 父进度 / 触发条件）；全部 `frame=task`、`show_toast=true`、`announce_to_chat=false`；`root` 的 `background` = `minecraft:textures/block/hay_block_side.png`；`lucky_jackpot` 为 `hidden: true`。
- 没有原版触发器可用的条目（#4 下界之星、#8 破障、#9 炮台命中、#10 光环、#11 尖牙）用 `minecraft:impossible`，由服务端 `award` 授予；**如果当前代码里还没有对应的 award 调用点，在回报里列出「需要 M6 补 award 的 5 个进度 ID」，不要自己去改 behavior/ 或 entity/**。

### 2.4 语言（216 键 × 2 份）
- `zh_cn.json` 与 `en_us.json` **键集必须完全一致**；键数 216（22 item + 20 block + 9 entity + 1 itemGroup + 1 container + 8 gui + 10 tooltip + 4 death + 24 advancement + 117 config）。
- 中文名逐字照抄 `plan.txt`（含「？！菇菇！？」这类全角标点）；英文名按 DEC C-10 的 15 个固定译名 + 其余按注册 ID 的 Title Case（D-26）。
- 117 个 `farmstorm.configuration.<group>.<key>` 必须**全部**出现在两份语言文件里；键名与 `run/config/farmstorm-server.toml` 的段/键逐段对应（可用那个文件做机械校验）。

### 2.5 接线
- 4 个 provider 接进 `GatherDataEvent`（不加 `bus`）；保持已有 5 个 provider 不变。

## 3. 约束
- 只写这 5 个文件 + `src/generated/resources/`；不碰 `src/main/java` 其它包、`src/main/resources`。
- 不手写 `data/**` 或 `assets/lang/**` JSON。

## 4. 验收
1. `gw.cmd runData -PfsRunDir=run-m52` → BUILD SUCCESSFUL；贴出**全量产物计数**（此任务后应接近技术规格的 257 总量：JSON 169 + TOML 1 + PNG 87 —— 逐类给出）。
2. 配方：`recipe/*.json` = **21**；抽查火把花那条（必须是 `bidirectional:false` + 下界之星催化剂）。
3. 战利品：`loot_table/blocks/*.json` = **20**；抽查膨胀麦那条（成熟同时掉作物与种子）。
4. 进度：`advancement/*.json` = **12**；抽查 `root.json`（background 正确）与 `lucky_jackpot.json`（hidden）。
5. 语言：两份文件键数各 **216**、键集 diff 为空；且 117 个 config 翻译键与 `run/config/farmstorm-server.toml` 的 117 个键**逐条对应**（写个脚本做集合比对，贴输出）。
6. `gw.cmd build -PfsRunDir=run-m52` 绿；`python tools/verify_project.py` 的 lang 检查从 WARN 转为 **OK**。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外给出四类产物计数、117 config 翻译键的比对输出、「需要 M6 补 award 的进度 ID」清单、以及任何与 TECH §6/§7 的偏差。
