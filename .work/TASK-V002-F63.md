# 任务书 V002-F-63 · `DeployedKind.ZANGIEF_CANE` + 三处穷尽 switch + 回填 kind

## 目标（`docs/zh/13-TASKS-0.0.2.md` §3 F-63，经 V-01 / V-03 修订后）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 F-62（已提交 `36a6ea8`）
- 核心机制：`DeployedKind` 加了新枚举值后，**所有穷尽 switch 会编译失败**（`12-TECH §3.14` 的编译期强制），所以本任务必须一次把三处补齐。

## 交付物（严格按此，不要多做）

| # | 文件 | 改动 |
|---|---|---|
| 1 | `behavior/DeployedKind.java` | 新增枚举值 `ZANGIEF_CANE(10)`（扫描间隔 10 刻，排在既有 5 个之后） |
| 2 | `util/CropProfile.java` | **回填 `ZANGIEF_CANE` 常量的 `kind` 字段**为 `DeployedKind.ZANGIEF_CANE`（V-01：F-62 时该值不存在才留 null；**不回填则 F-65 的 `byKind()` 会抛 `IllegalArgumentException`**），并删掉/改写那条「待 F-63 回填」的 javadoc |
| 3 | `behavior/ZangiefCaneTrap.java`（**新建，本任务是骨架**） | `implements TrapBehavior` 的**最小可编译骨架**：`findTrigger(...)` 返回 `null`、`trigger(...)` 空实现。类注释写明「骨架：真实球形判定与相位驱动由 F-86 / F-87 填充」 |
| 4 | `behavior/DeployedBehaviors.java` | +1 个静态单例字段（指向上面的骨架）+1 个 `case ZANGIEF_CANE ->` 分支 |
| 5 | `registry/FarmstormBlockEntities.java` | `deployedOf` 的穷尽 switch 加 `case ZANGIEF_CANE -> FANG_TRAP_CACTUS.get();`（复用既有 BE type，**block_entities 仍 6**） |

## 明确**不做**的两件事（越界即返工）

1. **不要把 `FarmstormBlocks.ZANGIEF_CANE.get()` 加进 `FANG_TRAP_CACTUS` 的 `BlockEntityType.Builder` 方块列表。**
   原任务书要求这一步，但该方块是 **F-65** 的交付物，现在引用它必然编译不过。**这一步整体移交 F-65**（V-03）。
2. **不要注册 `ZangiefCaneTrap` 的方块**、不要动 `FarmstormBlocks`。

## 验收（三条 + 自证）

1. 能观察到 `DeployedKind` 的 **6 个枚举值与各自扫描间隔**，`ZANGIEF_CANE` = **10** 刻。
   - 允许用**临时探针**打印并事后删净（源码 / `build/classes` / jar 三处 `Probe` 计数为 0）；若你选择加常驻自检行，注意 BRIEF 只给 `Farmstorm.java` 留了「新增 2 行自检」的预算，请说明你怎么处理这个冲突。
2. `cmd /c ""D:\project_farm\.tools\gw.cmd" build -PfsRunDir=run-f63` 退出码 0，编译日志**没有** `the switch expression does not cover all possible input values`。
3. 注册自检行里的 `block_entities` 仍是 **6**（一个 type 收两个方块，未新增 BE）。

补充硬门禁：`python -X utf8 tools/verify_project.py` **FAIL 0**（117 键 / 64 注册 ID 不变）；零 warning。

## 边界

- 只许动上表 5 个文件（其中 `ZangiefCaneTrap.java` 是新建）。
- 不许改 `FarmstormConfig.java`、`FarmstormItems/Blocks/EntityTypes`、`docs/`、`Farmstorm.java`（除非验收 1 的常驻自检方案需要，且必须说明理由）。
- 不许新增注册对象。

## 回报

- 6 个 kind 与间隔的实际输出原文。
- `git diff --stat` + `build` 最后两行 + `verify_project.py` 结论行。
- 明确回答：`ZangiefCaneTrap` 骨架里你写了什么、`DeployedBehaviors.of(ZANGIEF_CANE)` 现在返回什么、以及 F-86 接手时该改哪两处。
- **不要 commit**。
