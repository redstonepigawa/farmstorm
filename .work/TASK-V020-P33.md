# 任务书 · farmstorm v0.2.0 · Phase 33（F-216 ~ F-219）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> **4 个任务都是 S / 0.5，互不依赖，但验收必须分开写、不合并。**

## 0. 目标

| 任务 | 标题 | 一句话交付物 | 涉及文件 |
|---|---|---|---|
| **F-216** | 幸运浆果池 11 与断言同提交 | 池改「只留 `tier() == COMMON`」；`Farmstorm.java` 断言 `19 → 11` 与日志文案**同一次提交** | `util/CropProfile.java:139-143`、`Farmstorm.java:117-123` |
| **F-217** | 种植路径①：`isPlantableSoil` 恒 `false` | 判据体改返回 `false`，原「是 `FarmBlock` 才允许」留成注释；调用点 `:72` 不动 | `item/ModifiedCropItem.java:101-107` |
| **F-218** | 种植路径②：`SeedItem#useOn` 返回 `PASS` | 新增 `useOn` 覆写返回 `InteractionResult.PASS` | `item/SeedItem.java` |
| **F-219** | 转换器 `useWithoutItem` 返回 `PASS` | 方法体整体替换为返回 `PASS`；`getMenuProvider` 与掉落保留 | `block/ConverterBlock.java:78-86` |

**执行顺序**：按 F-216 → F-217 → F-218 → F-219。**每条独立成组**（上表文件列互不重叠），做完我按四个任务分四次提交。

## 1. 边界

**只允许改这 5 个文件**：`util/CropProfile.java`、`Farmstorm.java`、`item/ModifiedCropItem.java`、`item/SeedItem.java`、`block/ConverterBlock.java`。

**绝对不可动**：
- `docs/` 任何文件；`build.gradle`、`.tools/`、`run/`
- **注册面仍 93**：`registry/` 四个文件、`FarmstormMenus`、`ConverterScreen` 零改动；`menu_types` 仍 1（注册窗口 F-224 才开）
- `config/FarmstormConfig.java` 一行不动（config 窗口 F-220 才开）；toml 仍 **166 键 / 58 段**、`[outpost_core]` 仍 **3** 键
- `ModifiedCropItem.java:53-55` 的 `canPlantAt`（全仓 0 调用者的死代码，**不删**）
- `FarmstormCropBlock` 不动（`mayPlaceOn` / `canSurvive` 本来就没覆写）
- `Farmstorm.java:112-113` 的 `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2` 一行
- 22 条档案的档位与第 9 字段（F-214 已冻结）
- `ModLangProvider.java:13-16` 的构成注释（语言键窗口 F-215 → F-241，总数在 F-241）
- `data/farmstorm/recipe/` 的 29 个 json

## 2. 必读

1. `docs/zh/38-PROMPT-MAIN-0.2.0.md` —— 全文（尤其 HC-106 / HC-107、陷阱 20 元规则、陷阱 23 / 24 / 29、§7 回报格式）
2. `docs/zh/39-PROMPT-TASKS-0.2.0.md` —— §0.2 五条重申、**§2（Phase 33 全节：前置 / 目标 / 任务表 / 7 条出口判据 / 7 条翻车点 / 回报格式）**
3. `docs/zh/37-TASKS-0.2.0.md` —— F-216 ~ F-219 详情（约 169~221 行）、§5.2 A-63
4. `docs/zh/36-TECH-0.2.0.md` —— §2.2（两条路径的写法与负向清单）、§2.9
5. `docs/zh/35-DESIGN-0.2.0.md` —— §3（两条路径逐条对照）、§5（池 19 → 11 与被移出的 8 条）、B-02 / B-35
6. `docs/zh/34-DECISIONS-0.2.0.md` —— D-152 / D-155 / D-156 / D-170、裁定 #19

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 基线 | Phase 32 已提交（`cd05e65` / `40fdeb9` / `d1c6789`）；`mod_version=0.2.0`；`build/libs` 两支 0.2.0 jar |
| 现状 | 注册 **93**（`menu_types 1`）；lang **327 × 2**（`tooltip.farmstorm` 20）；toml **166 键 / 58 段 / `[outpost_core]` 3 键** |
| 日志 | 已有 `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`；`自检 幸运浆果池 19 种…` 仍是 **19**（本 Phase 的 F-216 改它） |
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build` 基线 exit 0。**唯一入口，禁止直接调 gradle** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服 | `run-p33`（复制 `run/eula.txt` + `run/server.properties`），`FS_RUN_LOCK=run-p33 … runServer -PfsRunDir=run-p33`；**不要在 `run/` 里起服**；手工写文件 UTF-8 无 BOM |

## 4. 已定口径

1. 文档与源码冲突 → **以源码 / 34 号为准**，登记偏差；**不许改 `docs/`**。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` 取证。提交由调度者做。
3. 每条判据**改前先跑一遍记下实测值**（陷阱 20），改后再跑一次。
4. 一个任务一组文件，**回报按 F-216 / F-217 / F-218 / F-219 分四段**。
5. 渲染 / 听感 / 需第二客户端的项标「未验证」+ 原因。

## 5. 实现要点（照做，不要自创）

### F-216
- 池从「排除 3 条」改成 **`tier() == COMMON` 的 filter 构造**（A-63：**不许**写成手写 11 条 id 的清单）。
- `Farmstorm.java` 的断言 `19 → 11`，**保留**「不含 `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`」的另半条（三个 `!=`）；日志文案同改。
- 池与断言**同一个提交**（RT-50）。

### F-217
- `isPlantableSoil` 的**判据体**改 `return false`；原「是 `FarmBlock` 才允许」的实现**留成注释**（不删）。
- 调用点 `ModifiedCropItem.java:72` 不动。

### F-218
- `SeedItem` 新增 `useOn` 覆写返回 `InteractionResult.PASS`（切断 `ItemNameBlockItem` → `BlockItem#useOn` → `place()`）。
- `appendHoverText`（tooltip）必须仍在。

### F-219
- `ConverterBlock.useWithoutItem` 方法体整体替换为返回 `InteractionResult.PASS`。
- `getMenuProvider`（`:70-76`）与掉落（`:88-96`）**保留**——只停「右键开界面」，不停方块本身。

## 6. 验收标准（= 39 号 §2.4 的 7 条，逐条给读数）

1. **池 11 与断言同提交（F-216）**：日志 `自检 幸运浆果池 11 种…→ OK`；池的 11 条 `tier()` 全是 `COMMON`；`== 19` 在 `Farmstorm.java` **0** 处；三个 `!=` 仍在；池段内 `grep -c "CropProfile\."` = **0**（无第二份 id 清单）。
2. **只滚普通（F-216）**：吃 `lucky_berry` 直到命中「生成随机作物」分支，逐次记产出 id（抽样次数写进记录）——全部落在 11 条普通集合内，三条被排除项各 **0** 次。**本机无客户端，若无法喂食则标「未验证」+ 给出池构造的代码层证据。**
3. **路径①（F-217）**：手持 **15** 个有作物方块的作物逐一右键耕地 → 方块不出现、手持物不减、无提示；`isPlantableSoil` 可执行体里没有 `instanceof FarmBlock`（只剩注释），方法仍被 `:72` 调用 1 处。
4. **部署型不误伤（F-217 / B-02）**：**7** 个部署型物品右键完整方块顶面 → 照常摆出部署形态。
5. **路径②（F-218）**：**7** 个种子右键耕地、再右键泥土 → 方块不出现、种子不减、无提示；`SeedItem.java` 里 `InteractionResult.PASS` 命中 **1** 处、`useOn` 命中 **1** 处；`appendHoverText` 仍在。
6. **转换器（F-219）**：空手右键 → 无界面、无提示、手持物不减；`data/farmstorm/recipe/` 的 json 计数 **29**（**26** `converting` + 2 shaped + 1 shapeless，**两个数分开写**）；`FarmstormMenus` 的 `REGISTER.register` 仍 **1** 处。
7. **注册面与 config 不动（负向）**：起服 `自检 注册对象 93 个 … → OK`；toml 仍 **166 键 / 58 段**、`[outpost_core]` 仍 **3** 键；`config/FarmstormConfig.java` diff 为空。

## 7. 必须回报的证据（按四个任务分段）

1. `git diff --stat` 原文 + `.tools\gw.cmd build` 退出码。
2. **F-216 段**：池改前 → 改后的构造式；11 条 id 与其 `tier()` 逐条点名；`== 19` 与三个 `!=` 的 grep 读数；自检行原文；池段内 `CropProfile\.` 命中数。
3. **F-217 段**：`isPlantableSoil` 改后全文（含注释）；`instanceof FarmBlock` 的可执行命中数；调用点；15 个作物 + 7 个部署型的右键矩阵（能真机就给读数，不能就标未验证）。
4. **F-218 段**：新增的 `useOn` 全文；`InteractionResult.PASS` 与 `useOn` 的 grep 读数；7 个种子的右键矩阵。
5. **F-219 段**：`useWithoutItem` 改后全文；`getMenuProvider` 与掉落段未改的证据；recipe 计数 29 / 26 两个数；`REGISTER.register` 命中数。
6. **负向面**：注册自检行原文；toml 的三项读数；`registry/`、`FarmstormConfig.java`、`FarmstormMenus`、`ConverterScreen`、`data/` 的 diff 全空。
7. **文档-源码偏差表** + **未验证清单** + **未关闭的问题**。

## 8. 证据落盘

写 `.work/TASK-V020-P33-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 9. 完成定义

§6 的 7 条全部有读数或有「未验证」+ 原因；回报按四个任务分段。
