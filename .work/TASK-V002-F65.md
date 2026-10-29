# 任务书 V002-F-65 · 5 个物品、2 个方块、燃料标签键（+ 收 V-03 / V-02 两笔账）

## 目标（`13-TASKS-0.0.2.md` §3 F-65，经 V-02 / V-03 追加）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 F-62（已提交 `36a6ea8`）；F-63 也已完成（`bb74fef`），所以 `DeployedKind.ZANGIEF_CANE` 已存在、`CropProfile.ZANGIEF_CANE.kind` 已回填。

## 交付物

### 1. `registry/FarmstormItems.java`（+5）

严格按 `12-TECH §2.2` 第 1~5 行点名（**类形态不许改**）：

| ID | 类 |
|---|---|
| `molotov` | `ThrowableCropItem` |
| `zangief_cane` | `DeployableCropItem`（部署方块的 BlockItem） |
| `wild_apple` | `FoodCropItem` |
| `compressed_puffed_wheat` | `ThrowableCropItem` |
| `compressed_hay_block` | `BlockItem` |

并按 `12-TECH §3.14` 在 `creativeTabItems()` 里把 `compressed_hay_block` 追加进去（另外 4 个作物走 `CROP_ITEMS` 自动进）。

### 2. `registry/FarmstormBlocks.java`（+2）

- `compressed_hay_block`：普通 `Block`（无自定义子类），属性按 `11-DESIGN` / `D-57`：`BlockBehaviour.Properties.of().strength(30.0F, 6.0F).requiresCorrectToolForDrops()`，六面不可旋转。
- `zangief_cane`：走既有 `deployed(...)` 工厂的**陷阱类口径**（`12-TECH §3.14` 的 `deployed(ZANGIEF_CANE, false, false, false)`；三个布尔的实际含义以 `FarmstormBlocks` 里既有 `deployed` 签名为准，不要猜）。

### 3. `util/FarmstormTags.java`（+1）

新增 `TagKey<Item> CONVERTER_FUEL`（D-52 的转换器燃料标签）。**只要常量**；标签内容（JSON）属 datagen，不归本任务。

### 4. 【V-03 移交】`registry/FarmstormBlockEntities.java` 的 builder 方块列表

把 `FarmstormBlocks.ZANGIEF_CANE.get()` 加进 **`FANG_TRAP_CACTUS` 那个 `BlockEntityType.Builder`** 的方块列表（`deployedOf` 的 `case ZANGIEF_CANE -> FANG_TRAP_CACTUS.get()` 已经在了）。**`block_entities` 仍为 6**——一个 type 收两个方块。

## 验收

1. RCON 五条 `/give @s farmstorm:{molotov,zangief_cane,wild_apple,compressed_puffed_wheat,compressed_hay_block} 1` **全部返回成功**。
2. `/setblock ~ ~ ~ farmstorm:compressed_hay_block` 与 `/setblock ~ ~1 ~ farmstorm:zangief_cane` 各返回成功；`/data get block <zangief_cane 坐标>` 能读到 `owner` 字段（复用既有 `DeployedCropBlockEntity`）。
3. **【V-02 收口】** 幸运浆果池 16 条逐项 `FarmstormItems.forProfile(p).get()` 不再抛异常：写一个临时探针遍历池子打印「16/16 OK」，跑完删净（源码 / `build/classes` / jar 三处 `Probe` 为 0）。同时 `自检 幸运浆果池 16 种 … → OK` 仍在。
4. **视觉验收不在本轮**：`zangief_cane` 的「世界无可见几何体」要靠 `ModBlockStateProvider` 的 `DEPLOYED_INVISIBLE` 与空模型，那是 datagen 任务；本轮只验服务端侧（`/setblock` 成功 + `owner` 可读）。**在回报里明确写「视觉未验证」。**
5. 硬门禁：`build -PfsRunDir=run-f65` 退出码 0、零 warning；`verify_project.py` 仍 **FAIL 0**——注意它现在核的是 **64** 个注册 ID（items 22 / blocks 20 / …），本轮之后应为 **+7**（items 27 / blocks 22），脚本**尚未**升级到 0.0.2 口径，所以它**会报新 ID 缺失或数量不符**。

> ⚠️ 关于验收 5：`tools/verify_project.py` 现在硬编码 v0.0.1 的 64 个 ID 与 117 键。F-65 新增注册对象后它必然 FAIL——**这是预期的**，不要为了让脚本变绿而改脚本或改文档。请在回报里贴出它的实际输出，并明确「失败项全部来自脚本尚未升级，不是实现错误」。升级脚本是 F-66 之后单独一件活。

## 边界

- 只许动：`registry/FarmstormItems.java`、`registry/FarmstormBlocks.java`、`util/FarmstormTags.java`、`registry/FarmstormBlockEntities.java`（仅 builder 列表那一行）。
- **不许动** `Farmstorm.java`（自检「注册对象 77」是 F-66）、`FarmstormConfig.java`（F-61）、`datagen/*`（资源任务）、`docs/`。
- 不许新增 entity_types / mob_effects / sound_events。

## 回报

- 五条 `/give` 与两条 `/setblock` 的**实际返回原文**。
- `/data get block` 的输出原文。
- 池子 16/16 的探针输出原文 + 探针删净证据。
- `git diff --stat`、`build` 最后两行、`verify_project.py` 的**实际输出**（含你判定为"脚本口径未升级"的那部分）。
- **不要 commit**。
