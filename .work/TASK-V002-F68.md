# 任务书 V002-F-68 · 压缩膨胀麦投掷物：沿用落点写入

## 目标（`13-TASKS-0.0.2.md` §3 F-68）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 F-66（`55cd6bb`）、F-67（`76ea279`/`bf3f9ea`）
- 一句话：让压缩膨胀麦**完全沿用**膨胀麦的落点写入语义，唯一差别是写入的方块 ID。做法是把写入的方块抽成扩展点，**不要复制一份逻辑**。

## 现状（已核对）

- `entity/thrown/PuffedWheatProjectile.java`：`onImpact` → `private static void fillHay(ServerLevel, BlockPos, int)`，第 93 行写死 `Blocks.HAY_BLOCK.defaultBlockState()`；`LANDING_PROBE = 0.1` 的落点反推、水源判定、干 3 / 湿 5、3.6e6 抗性底线都在这个方法里。
- `entity/thrown/CompressedPuffedWheatProjectile.java`：F-66 的骨架，只有 `profile()`，**不重写 `onImpact`**（所以现在落地什么都不做）。

## 交付物

1. `entity/thrown/PuffedWheatProjectile.java`：把「写入哪个方块」抽成 `protected BlockState fillBlock()`（返回 `Blocks.HAY_BLOCK.defaultBlockState()`），`fillHay` 改为**实例方法**内部调用它。**其余语义一行不动**（落点反推、水源判定、干湿边长、3.6e6 底线）。
2. `entity/thrown/CompressedPuffedWheatProjectile.java`：改为继承同一套流程——重写 `fillBlock()` 返回 `FarmstormBlocks.COMPRESSED_HAY_BLOCK.get().defaultBlockState()`，并让 `onImpact` 走父类实现（若父类方法签名需要调整，一并处理）。

**判据**：`git diff` 里必须看得出「同一份写入逻辑被两个类共用」，而不是出现第二份几乎相同的循环。

## 验收（四条，逐条给原文）

1. **同等写入**：同一坐标先投一颗膨胀麦、再投一颗压缩膨胀麦，两次写入的立方体边长一致（干态 **3**、水面附近遇水 **5**），逐格 `/execute if block` 统计的**格数相同**（干态应为 27 = 3³，湿态 125 = 5³——把实际数报出来）。
2. **位置集合完全相同**，唯一差异是方块 ID（`minecraft:hay_block` vs `farmstorm:compressed_hay_block`）；原方块全部被替换且**无掉落物**（`/kill @e[type=item]` 前后物品实体计数为 0）。
3. **抗性底线**：在 `minecraft:bedrock`（抗性 3.6e6）旁边投掷，`minecraft:bedrock` 逐格 `/execute if block` **全为真**（跳过不写）。
4. `config/farmstorm-server.toml` 里**没有**新增的 `compressed_puffed_wheat.*` 或 `puffed_wheat.*` 键——两者读同一组既有键（`puffed_wheat.dryEdge` / `wetEdge`）。

## 怎么把「同坐标两次投掷」做稳

投掷物落点受初速与随机影响，靠手感投两次很难同坐标。允许下面两种做法，**二选一并在回报里说明你用了哪种**：
- **A（首选）**：`/summon` 两颗投掷物在**同一坐标、同一初速、Motion 全 0**，让它们自由落体到同一平面；落地后再统计。
- **B**：临时探针**直接调用同一个 `onImpact` 入口**（不是复制逻辑）在固定坐标结算两次，逐格比对；探针用完删净并报 `Probe` 计数为 0。

## 边界

- 只许动上表 2 个文件。**不许**动 `FarmstormConfig`、`FarmstormBlocks`、`datagen/*`、`docs/`。
- **不许**新增 config 键（验收 4 就是查这个）。
- **不许**把 `fillHay` 复制一份给压缩版。

## 环境纪律（这两条上一轮踩过）

- **证据写进 `.work/` 的文件**，不要只留在 `run-*/logs/`——重启会轮转，上一轮就是从 `.gz` 里捞回来的。
- 跑完服务端**当场** `Get-Process java | Stop-Process -Force`，再删 `.tools/runserver.lock`；留进程会让下一个任务在锁上干等。
- 用 `-PfsRunDir=run-f68`；系统 `python` 是 Store 占位符，用 `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe -X utf8`。

## 回报

- 你用了 A 还是 B，以及为什么。
- 四条验收的**实际命令与返回原文**；干态/湿态的**格数**要写出具体数字。
- `git diff --stat` 与两个类的关键 diff 片段（证明是共用而不是复制）。
- `build` 最后两行；`Probe` 残留计数（源码 / `build\classes` / jar 三处）。
- **不要 commit**。
