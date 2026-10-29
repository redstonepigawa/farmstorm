# 任务书 V002-F-62 · 4 条新作物档案 / ALL 19 条 / 幸运浆果池 16 条

## 目标（逐字取自 `docs/zh/13-TASKS-0.0.2.md` §3 F-62）

- **复杂度**：M ｜ **折算人日**：1.5 ｜ **依赖**：F-60（已完成，提交 `cd2c230`）
- **交付物**：`util/CropProfile.java`（+4 常量 `MOLOTOV` / `ZANGIEF_CANE` / `WILD_APPLE` / `COMPRESSED_PUFFED_WHEAT`，`ALL` 19 条，`LUCKY_BERRY_POOL` 改显式排除 `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`）；`Farmstorm.java` 的两行自检文案

## 新增 4 条档案的确切字段

以 `docs/zh/10-DECISIONS-0.0.2.md` §4 的 16~19 行为准（**逐字照抄，不许自己编**）：

| # | ID | 型 | 类 | 中文名 | 英文名 | 可种 |
|---|---|---|---|---|---|---|
| 16 | `molotov` | THROWABLE | DAMAGE, CONSTRUCTION | 莫洛托夫！ | Molotov | 是 |
| 17 | `zangief_cane` | DEPLOYABLE | TRAP | 桑吉尔蔗 | Zangief Cane | 是 |
| 18 | `wild_apple` | EDIBLE | BUFF | 狂野苹果 | Wild Apple | 是 |
| 19 | `compressed_puffed_wheat` | THROWABLE | CONSTRUCTION | 压缩膨胀麦 | Compressed Puffed Wheat | 是 |

注意：D-51 明确 **`molotov` / `zangief_cane` / `wild_apple` 三者无种植形态**（`cropBlock` 与 `seedItem` 均为 `null`，不注册方块与种子）；`compressed_puffed_wheat` 按 D-67 走「4 个膨胀麦合成」，同样没有自己的作物方块。**四条都要确认这一点**，别照着"可种"列误解。

`:nth-child` 提示：v0.0.1 的 `melon_slice_bomb` 就是"无方块无种子"的先例（`new CropProfile(..., null, null, null, Set.of(), THROWABLE_STACK_TIER)`），照它写。

## 边界（硬性）

- **只许改两个文件**：`src/main/java/com/dashpig/farmstorm/util/CropProfile.java`、`src/main/java/com/dashpig/farmstorm/Farmstorm.java`（仅两行自检文案）。
- **不许改** v0.0.1 那 15 条既有档案的任何字段（BRIEF §3.4 冻结）：`git diff util/CropProfile.java` 必须**只含新增行**，既有行一个字符都不变。
- **不许**新增 config 键、不许动 `FarmstormConfig.java`（那是 F-61 的活）。
- **不许**改 `docs/` 下任何文件（0.0.1 冻结 + 0.0.2 是上游）。
- **不许**新增任何注册对象（方块/物品/实体在 F-65/F-66）。

## 验收（三条，逐条给证据）

1. 启动日志**逐字**出现：
   `自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK`
2. 启动日志**逐字**出现：
   `自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK`
3. `git diff util/CropProfile.java` 只含新增行（贴 `--stat` + 抽样证明既有 15 条未变）。

补充硬门禁：`cmd /c ""D:\project_farm\.tools\gw.cmd" build -PfsRunDir=run-f62` 退出码 0、零 warning；`python -X utf8 tools/verify_project.py` 仍 **FAIL 0**（本轮不动 config/注册，117 与 64 都应照旧通过）。

## 怎么跑服务端拿日志

```cmd
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-f62 --console=plain
```
起来看到 `Done (` 即可 `stop`。两个自检行在 `modloading-worker` 阶段就会打印，用 `Select-String` 抓 `自检` 五行（v0.0.1 有三行 + 本轮两行 = 五行）。

## 回报要求

- 贴出**五行自检原文**（不是截图、不是复述）。
- 贴 `git diff --stat`、`build` 的最后两行、`verify_project.py` 的结论行。
- 明确写：`LUCKY_BERRY_POOL` 你是怎么实现的（显式 `Set` 排除还是列表过滤），以及为什么 16 = 19 − 3。
- 未验证项一律写「未验证」，不要用"应该没问题"。
- **不要 commit**，改完留在工作区，由调度者统一验收后提交。
