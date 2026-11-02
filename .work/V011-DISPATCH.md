# v0.1.1 开发派发计划（调度者台账）

> 生成于本轮派发前。上游：`docs/zh/28~33`（只读）、`.work/v011-BRIEF.md`、`.work/v011-verdicts.md`。

## 0. 调度者已确认的四项口径（作者本轮拍板）

| # | 问题 | 定为 |
|---|---|---|
| D-1 | 交付终点 | **全部 6 个 Phase（F-176 ~ F-212）**，含 77 条验收与 `dist/` 产物 |
| D-2 | 并行度 | **严格串行**：一个 Phase 一个子代理，前一 Phase 验收通过才开下一个 |
| D-3 | Git | **每个 Phase 验收通过后由调度者提交一次**，只 add 本版相关文件；子代理禁止任何 git 写操作 |
| D-4 | 文档 vs 源码冲突 | **以源码为准**，子代理自行适配并登记偏差；`docs/` 全程冻结不改 |

补充口径（第 2 轮）：

| # | 问题 | 定为 |
|---|---|---|
| D-5 | 验收深度 | 静态 + 无头专用服务器能覆盖的照做；**客户端 GUI 项标「未验证」并逐条列出** |
| D-6 | 未回灌裁决 | **按 `.work/v011-verdicts.md` 的裁决口径执行**（如 A-52 用 `^\s*\[`、A-57 补 `src/main/java/` 前缀），文档保持冻结 |
| D-7 | README / dist | 都做：README 三处、`dist/` 放 jar、更新 `dist/安装说明.txt` 与 `install-notes.txt` |

## 1. 环境基线（调度者实测，2026 本轮）

| 项 | 值 |
|---|---|
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build` → exit 0（UP-TO-DATE） |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（`python` 不在 PATH） |
| 工具基线 | `verify_project.py` → 166 键 OK / 93 个注册 ID / lang 315 / **FAIL 0 项**，exit 0 |
| `run/config/farmstorm-server.toml` | 在位，59 段，含 `[farmland]`，`pumpkin_turret` 3 键 |
| 源图 | `C:\Users\DASHpig\Downloads\wild_apple_64x64.png` 6,946 B 在位 |
| `build/libs` | 残留 0.0.2 / 0.0.3 / 0.0.4 / 0.1.0 四对（F-176 要求清掉 0.1.0 与 0.0.1） |
| Java 文件 | 125 |

## 2. 派发顺序与闸门

| 序 | Phase | 任务 | 人日 | 进入闸门 | 出口闸门（调度者复核点） |
|---|---|---|---|---|---|
| 1 | 26 基线与工具链 | F-176 ~ F-177 | 2.0 | 基线绿（已满足） | 产物名/banner 0.1.1；工具现算 58 组/166 键并剔 1 键；中间态恰 2 条键级差异；`docs/` diff 为空 |
| 2 | 27 材质数值方块属性 | F-178 ~ F-184 | 11.5 | Phase 26 提交 | `wild_apple.png` 64×64/6,946 B/逐字节=s 源图；其余 24 张 sha256 不变；`[pumpkin_turret]` 5 键 40/3/0.45/15/1；`TurretBullet.SPEED` 0 命中；lang 316 |
| 3 | 28 朋友的韭持续场 | F-185 ~ F-186 | 4.5 | Phase 27 提交 | 场长 = `effectTicks`；空投不播；读档 `Settled` 首刻自杀 |
| 4 | 29 耕地-核心绑定制 | F-187 ~ F-198 | 21.5 | Phase 28 提交 | 绑定表落盘 \`dataVersion: 2\`；第 17 格被拒；补建 N/M 两次相同；`/farmstorm` 消失；段行 58、键 166；**工具首次 FAIL 0**（A-40 关闭） |
| 5 | 30 浮窗与数据面 | F-199 ~ F-205 | 15.0 | Phase 29 提交 + F-187/188/189 在位 | `CustomPacketPayload` 恰 1 文件；client toml 3 键；server toml 仍 166/58 无 `hud` 段；lang 323；注册仍 93 |
| 6 | 31 收口与全量验收 | F-206 ~ F-212 | 11.0 | Phase 30 提交 | 两份 lang 323 × 2、`configuration.` 169；`dist/` 两支 jar 版本行 0.1.1；77 条逐条有读数或标未验证；红线三计数 |

## 3. 每个子代理统一的硬约束（写进每份任务书）

1. 构建**只能**走 `.tools\gw.cmd`，禁止直接调 gradle。
2. **禁止任何 git 写操作**（commit / checkout / reset / stash / add / restore）；只允许 `git status` / `git diff` 取证。
3. **禁止改 `docs/` 任何文件**（28~33 与 00~27 全冻结）；禁止改 `.work/v011-BRIEF.md` / `v011-verdicts.md` / `v011-reqs.md` / `v011-delivery.md`。
4. 文档与源码冲突 → 以源码为准，登记偏差表；已裁决未回灌条目 → 按 `.work/v011-verdicts.md` 执行并注明 A-xx。
5. 客户端 GUI 类验收标「未验证」+ 原因，不得写成「已通过」。
6. 只做本 Phase 的 F-xx，不提前做下一 Phase 的活。
7. 证据落盘 `.work/TASK-V011-P<nn>-evidence.md`，回报正文只贴结论行。
8. 遇到做不下去的：停下、写明阻塞点与已做部分，不要改判据蒙过去。

## 4. 调度者的复核动作（每个 Phase 后）

1. 读 `git status` / `git diff --stat`，确认改动面只落在本 Phase 的任务清单内。
2. 亲自跑闸门命令（build / verify_project / grep / 计数），不采信子代理自述。
3. 通过则提交（`git add` 只加本版文件），不通过则带证据重派；同一任务书最多重派两次。
