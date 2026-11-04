# 任务书 · farmstorm v0.2.0 · Phase 35（F-224 ~ F-228）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> **本版最大也最险的一段**：注册自检窗口在这里**开**又**关**；两件「全项目第一次」都在这（第一个 `*.png.mcmeta`、第一次把非容器 BE 升级成容器）。两件都要求**先写最小用例跑通、再写正式逻辑**。

## 0. 目标（5 个任务）

| 任务 | 标题 | 复杂度 | 依赖 | 交付物 |
|---|---|---|---|---|
| **F-224** | 培养器注册三件套 + 创造栏 | L / 3.0 | — | 新建 `block/CultivatorBlock.java` 与 `blockentity/CultivatorBlockEntity.java`（骨架）；三个 registry 各 +1；`creativeTabItems()` 加一项；**窗口开** |
| **F-225** | 培养器资源、双模型与 datagen 三处 | L / 3.0 | F-224 | 7 个材质文件 + 双模型 + 两态 blockstate + item 模型 + 战利品表；`usesBlockModel` 加 `CultivatorBlock`；`getKnownBlocks()` 加一条 |
| **F-226** | 核心改容器：54 格 + `Items` + 两个同步方法 | L / 3.0 | — | `OutpostCoreBlockEntity` 实现 `Container`（54 格）+ NBT `Items` + `getUpdateTag` / `getUpdatePacket` + 老档兼容读；新增 `outpost_core.storageSlots` |
| **F-227** | 核心菜单 + 屏幕 + `menu_types` +1 | L / 3.0 | F-226 | 新建 `menu/OutpostCoreMenu.java` 与 `client/OutpostCoreScreen.java`；`FarmstormMenus` +1；屏幕注册 +1；**窗口最后一次注册增量** |
| **F-228** | 注册自检 97 收口（关窗） | S / 0.5 | F-224、F-227 | `Farmstorm.java:147-150` 的 5 个常量一次改齐（93→97、32→33、26→27、7→8、1→2）+ 方法注释两处 `93`；**窗口关** |

**执行顺序**：F-224 → F-225 → F-226 → F-227 → F-228。回报**按五个任务分五段**。

## 1. 边界

**允许改/增**：`block/CultivatorBlock.java`（新）、`blockentity/CultivatorBlockEntity.java`（新）、`menu/OutpostCoreMenu.java`（新）、`client/OutpostCoreScreen.java`（新）、`registry/FarmstormBlocks.java`、`FarmstormBlockEntities.java`、`FarmstormItems.java`、`FarmstormMenus.java`、`blockentity/OutpostCoreBlockEntity.java`、`config/FarmstormConfig.java`（**只加 `storageSlots`**）、`Farmstorm.java`（**F-228 的 5 个常量 + 自检挂载**）、`client/FarmstormClient.java`（屏幕注册）、`datagen/ModItemModelProvider.java`（`usesBlockModel`）、`datagen/ModLootTableProvider.java`（`getKnownBlocks`）、`textures/block/*`（新增 7 个）、以及 datagen 生成的 `src/generated/resources/**`。

**绝对不可动**：
- `farmland/` 两个类（555 / 225 LF，一行不改）、`event/` 的耕地注释面、`docs/`、`build.gradle`、`.tools/`、`run/`
- **除 `storageSlots` 外任何 config 键**；`FarmstormConfig` 的 166 / 58 两处**总数叙述**（收口在 F-242）
- `ModLangProvider.java:13-16` 的类注释（语言键收口在 F-241）
- **培养器不注册菜单**（`menu_types` 的 +1 只给核心存储；再注册一个就是提前变 97）
- `textures/item` 现有 29 张 + 不新增任何 `textures/item` 文件
- `FarmstormEntityTypes`、`network/`、`team/`、`util/`、`item/` 三个物品类

## 2. 必读

1. `docs/zh/38-PROMPT-MAIN-0.2.0.md` —— 全文（HC-97 / HC-98 / HC-110 / HC-111、陷阱 20 / 21 / 22 / 26 / 27 / 30、§7 回报格式）
2. `docs/zh/39-PROMPT-TASKS-0.2.0.md` —— §0.2 五条重申、**§4（Phase 35 全节：7 条出口判据 / 8 条翻车点）**、§0.4（三个滞后窗口台账）
3. `docs/zh/37-TASKS-0.2.0.md` —— F-224 ~ F-228 详情（约 277~346 行）、§0.3（中间态窗口）、§5.3 A-64
4. `docs/zh/36-TECH-0.2.0.md` —— **§1.2（97 逐类点名）**、§2.5（培养器全节）、§2.6（核心容器全节）、§5（资源清单）、§9 ⑦（`storageSlots` 值域退路）
5. `docs/zh/35-DESIGN-0.2.0.md` —— §6.2 ~ §6.5、§8.3（两道闸）
6. `docs/zh/34-DECISIONS-0.2.0.md` —— D-160 ~ D-167、D-171、裁定 #8 / #17

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | Phase 32/33/34 已提交（`cd05e65`…`0ab8edf`）；build exit 0；`mod_version=0.2.0` |
| 注册现状 | **93**（`items 32 / blocks 26 / block_entities 7 / … / menu_types 1`），自检 `→ OK` |
| lang 现状 | 两份各 **330**；`message.farmstorm` 前缀 **0** |
| toml 现状 | `run/config/farmstorm-server.toml` 仍是**旧产物**（58 段，无 `[trade]`）——它在 F-242 才重生。**本节读 toml 一律在独立 `run-p35` 目录先删再生**，不要动 `run/config` |
| 材质源 | `C:\Users\DASHpig\Downloads\cultivator_a_animation_pack`（40 文件：37 PNG + 2 mcmeta + README）；三张帧条 `cultivator_a_top/side/bottom`，两张静态首帧 `editable_frames/side_00.png` / `top_00.png` |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`（可 `compileJava` / `runData`）。**唯一入口，禁止直接调 gradle** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服 | `run-p35`（复制 `run/eula.txt` + `run/server.properties`），`FS_RUN_LOCK=run-p35`；**不要在 `run/` 里起服**；手工写文件 UTF-8 **无 BOM**（带 BOM 会让 nightconfig 报 `Invalid bare key` 重建配置） |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 34 号 / 已有裁决为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。提交由调度者做。
3. 每条判据**改前先跑一遍记下实测值**（陷阱 20），改后再跑一次。
4. 窗口内**不许**把 `→ OK` 写进任何判据（见 §5）。
5. 客户端渲染类验收标「未验证」+ 代码层证据。

## 5. 注册自检窗口（本 Phase 的核心纪律）

| 时点 | 应有读数 |
|---|---|
| 开工 | `93 个 … menu_types 1 → OK` |
| F-224 落地后 | `96 个：items 33 / blocks 27 / block_entities 8 / … / menu_types 1 → **FAIL**` |
| F-227 落地后 | `97 个：… / menu_types 2 → **FAIL**` |
| F-228 落地后 | `97 个：… menu_types 2 → **OK**` |

**四条窗口纪律**：① 不许把 `→ OK` 写成 F-224~F-227 的任何一条判据；② 其它四条自检行（`作物 22 种` / `作物品质 11/7/4` / `幸运浆果池 11` / `网络包注册`）必须仍 `→ OK`，任何一条红都是真故障；③ F-228 的提交**不夹带别的改动**；④ `ModLangProvider:13-16` 与 config 总数注释一字不动。

## 6. 实现要点

### F-224
- `CultivatorBlock`：铁砧属性 —— `strength(5.0F, 1200.0F)` + `MapColor.METAL` + `requiresCorrectToolForDrops()` + `SoundType.ANVIL` + `PushReaction.BLOCK`；`working` 是 **blockstate 布尔属性**。
- `CultivatorBlockEntity` 骨架（行为在 F-234~F-237）。
- 三个 registry 各 +1：`FarmstormBlocks` / `FarmstormBlockEntities` / `FarmstormItems`；`creativeTabItems()` **加 CULTIVATOR**（A-64：本版没有配方，不加就谁都拿不到）。
- 培养器**不注册菜单**。

### F-225
- 7 个文件落 `textures/block/`：3 张帧条 + 2 张静态首帧 + **2 个 `*.png.mcmeta`**（`frametime 2` / `interpolate false` / `frames 0..15`）；**两张 mcmeta 必须逐字节相同**；文件名不许改。
- 双模型：`working=false` 引用静态首帧、`working=true` 引用帧条；`blockstates/cultivator.json` **恰 2 个 variant**。
- `models/item/cultivator.json` 的 parent 必须是 `farmstorm:block/cultivator`（出现 `item/generated` / `layer0` 就说明 `usesBlockModel` 没加）。
- `ModItemModelProvider.usesBlockModel` 加 `|| block instanceof CultivatorBlock`；`ModLootTableProvider.getKnownBlocks()` 加一条。
- 动画「空闲静态」靠**双贴图 + blockstate 属性**，不靠代码暂停（陷阱 27）。

### F-226
- `OutpostCoreBlockEntity implements Container`，`SLOT_COUNT = 54`（6 × 9 双箱 = 27 × 2）。
- 新增 `outpost_core.storageSlots`：默认 **54**、值域 **54–54**（若 `ModConfigSpec` 拒绝，走 36 §9 ⑦ 退路 `54..55` + 创建菜单时钳到 54，**格数不许变**）。
- NBT `Items`；老档缺键当空（54 空槽）。
- `getUpdateTag` **只写 owner 三键**，绝不塞 `Items`、不写绑定；`getUpdatePacket` 走 `ClientboundBlockEntityDataPacket.create(this)`。
- **54 ≠ 16**：54 是格子数，16 是培养器名额（后者在 F-231，本节不建）。

### F-227
- `OutpostCoreMenu` + `OutpostCoreScreen`；`FarmstormMenus.REGISTER.register` **2 处**；`FarmstormClient` 里 **2 行** `event.register(...Screen::new)`。
- 此时界面**还开不了**（F-229 才接 `useWithoutItem`），是预期中间态。

### F-228
- `Farmstorm.java:147-150` 的 5 个常量一次改齐：`93→97`、`32→33`、`26→27`、`7→8`、`1→2`；方法注释里两处 `93` 同步。
- 该提交**只动这些**（+ F-224 需要的自检挂载已在 F-224 里）。

## 7. 验收标准（= 39 号 §4.4 的 7 条，逐条给读数）

1. **窗口状态 A（F-224）**：`96 个：items 33 / blocks 27 / block_entities 8 / … / menu_types 1 → FAIL`，**FAIL 是预期**。
2. **培养器可获取**：`/give` 拿得到、放下有 BE；创造栏「工具方块」段末多 1 项；`FarmstormItems.java` 里 `cultivator` 命中 ≥ **2**（注册 + 创造栏）。
3. **资源计数**：`blockstates 26→27`、`models 103→106`、`loot_table 26→27`、`*.png.mcmeta 0→2`、`textures/block` 新增 **5** 张 PNG。
4. **双态与模型**：`blockstates/cultivator.json` 恰 2 variant；`models/item/cultivator.json` parent 是 `farmstorm:block/cultivator`；两张 mcmeta 逐字节相同。
5. **容器面**：`/data get block <core>` 出现 `Items`；老档升级读成 **54** 空槽无异常；`getUpdateTag` 只含 owner 三键。
6. **窗口状态 B（F-227）**：`97 个：… / menu_types 2 → FAIL`；`REGISTER.register` **2** 处；`FarmstormClient` 两行 Screen 注册。
7. **关窗（F-228）**：`97 个：… menu_types 2 → OK`；`Farmstorm.java` 里 `93` 残留 0。
8. **窗口负向四条**（任一次起服）：另四条自检行仍 `→ OK`。
9. **另外两个数没被顺手改**：F-226 之后独立目录重生的 toml **172 键 / 59 段**、`[outpost_core]` **3** 键（`maxPerPlayer` / `boundaryParticleIntervalTicks` / `storageSlots`）；`ModLangProvider:13-16` 与 config 总数注释一字不动。

## 8. 必须回报的证据（按五个任务分段）

1. `git diff --stat` 原文；`.tools\gw.cmd build` 退出码；`runData` 的两次。
2. **F-224 段**：三个 registry 各一行；两个新类的类声明；创造栏那一项；**状态 A 的完整自检行原文**。
3. **F-225 段**：12 个资源文件逐个点名（含两张 mcmeta 的 sha256 相同）；五个目录计数；三个 json 的关键行；`usesBlockModel` 与 `getKnownBlocks` 的改动行。
4. **F-226 段**：类声明 / 字段区 / NBT / 两个同步方法的改动；`storageSlots` 的定义与被接受的实测读数；`/data get block` 的 `Items` 读数；老档升级后的槽数。
5. **F-227 段**：两个新类的类声明；两个注册点；**状态 B 的完整自检行原文**。
6. **F-228 段**：5 个常量的 diff；**关窗后完整自检行原文**；`93` 残留读数。
7. **负向面**：四次起服里另四条自检行原文；`farmland/` 两个类 diff 为空；`registry` 只动了该动的四个文件；`docs/` 零改动。
8. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 9. 证据落盘

写 `.work/TASK-V020-P35-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 10. 完成定义

§7 的 9 条全部有读数或有「未验证」+ 原因；**窗口三次状态（96 / 97 / 97-OK）的日志原文齐全**；回报按五个任务分段。
