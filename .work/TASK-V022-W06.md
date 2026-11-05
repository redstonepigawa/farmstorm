# 任务书 · farmstorm v0.2.2 · W06（F-280 → F-282，**同一子代理顺序做两件**）

> 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。**本波只有你一个子代理**，两件事**按顺序**做、各自独立提交（提交由调度者做）。
> 前置：W01~W05 已落地并提交（HEAD `96a884e`）。

## 0. 两个任务（详见 49 号对应小节）

| 顺序 | 任务 | 复杂度 | 涉及文件（**只许动这些**） | 依赖 |
|---|---|---|---|---|
| 1 | **F-280** `VillagerTradeEvents`：植物学家分支 + 注释漂移 | S / 0.5 | `event/VillagerTradeEvents.java`（**只加分支与注释，不许动农民那 3 行**） | F-277 / F-278 / F-279（均已落） |
| 2 | **F-282** `README.md`：已知限制 + 升级须知 | S / 0.5 | `README.md` | F-280、F-281（F-281 已落） |

## 1. 必读

1. `docs/zh/49-TASKS-0.2.2.md` —— **F-280 / F-282 小节** + §0.1~§0.5
2. `docs/zh/46-DECISIONS-0.2.2.md` —— F-280→D-197、D-200；F-282→D-198、D-202
3. `docs/zh/48-TECH-0.2.2.md` —— §2.4.5（等级挂点）、§10（README 的落点）、A-86（README 不在 48 号 §3 的 33 行里但 46 号把它当落地位置）
4. `.work/v022-BRIEF.md` —— 裁定 **#7 / #17 / #18**、**RT-63 / RT-66 / RT-67**、§1.8（改默认值对老存档无效）

## 2. 已实测基线（HEAD `96a884e`）

| 项 | 值 |
|---|---|
| 注册面 | **98**（items 34 / blocks 27）；口径外累计 **3 类** |
| Java 文件数 | **154** |
| config / lang | **176 键 / 60 组**（默认值已改 4 / 24，**键数不变**）/ **340 × 2**（关窗已完成） |
| `VillagerTradeEvents` | 只处理 `VillagerProfession.FARMER`，`:33` `get(1).add(TierTrade.COMMON)` / `:34` `get(3).add(TierTrade.RARE)` / `:35` `get(5).add(TierTrade.EPIC)`（**这三行逐字不许动**） |
| `BotanistTrade.NORMAL` | **22** 个 listing（静态 `List.of`） |
| `BotanistEpicTrade.EPIC` | **1** 个单实例 |
| 植物学家职业 | `FarmstormVillagerProfessions.BOTANIST` |

## 3. 硬约束

1. **禁止任何 git 写操作**；只许 `git status` / `git diff` / `git log` 取证。提交由调度者做。
2. **`docs/` 一字不改**；`build.gradle` / `.tools/` / `run/` 不许改；`src/generated/**` 不许手改。
3. **构建必须走互斥锁**；**禁止直接调 gradle**。
4. **不许动农民那 3 条交易**（裁定 #7）；**不许动 `TierTrade`**。
5. **不许自己新定数值 / 名称**；拿不准写「未关闭的问题」，**不要停下来等**。
6. **`build exit 0` 不证明你的文件被编译过**（D-43）—— 补一条 **`gw.cmd compileJava --rerun`** 的读数。
7. 运行时不成立的判据标「静态已验 / 运行时待 F-283」，**不许编读数**。

## 4. 构建锁（复制粘贴用）

```powershell
$lock = 'D:\project_farm\.tools\build.lock'
for($i=0; $i -lt 180; $i++){
  if(Test-Path $lock){ $age=(Get-Date)-(Get-Item $lock).LastWriteTime
    if($age.TotalMinutes -gt 15){ Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue } }
  if(New-Item -ItemType Directory -Path $lock -ErrorAction SilentlyContinue){ break }
  Start-Sleep -Seconds 5 }
try { & 'D:\project_farm\.tools\gw.cmd' build } finally { Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue }
```

## 5. 两件事的判决性验收

**F-280**
1. `VillagerTradeEvents` 加一条 **植物学家分支**：`VillagerProfession` 为 `FarmstormVillagerProfessions.BOTANIST` 时，`getTrades().get(1)` 挂 **22 条** `BotanistTrade.NORMAL`、`getTrades().get(5)` 挂 **1 条** `BotanistEpicTrade.EPIC`。
2. **农民那 3 行逐字未变**（给 `git diff` 证据，不是眼看）。
3. **等级挂点必须在这里验**（D-42）：`get(1)` 的 22 条、`get(5)` 的 1 条 —— 逐条给计数。这是 49 号把「全部挂新手级」放在本任务的原因；我 W05 任务书误把它写进了 F-278，已登记。
4. **`TierTrade.EPIC` 与 `BotanistEpicTrade.EPIC` 同名不同类**（D-46）：同一作用域里必须写**全限定类名**，不许只写 `EPIC`。
5. 类注释 `:10-11` 的漂移一并改（D-200；具体改什么读 49 号原文）。
6. `gw.cmd compileJava --rerun` exit 0。

**F-282**
1. `README.md` 的**已知限制**加本版新增的玩家可见限制（至少：每个植物学家只卖 22 项里的 **2** 项 —— 裁定 #17，要写「多找几个植物学家才能凑齐」；一个植物学家只卖 **1** 个史诗作物 —— 裁定 #18）。
2. **升级须知**加「从 v0.2.1 升级」：config 只改**默认值**、**老存档不追溯**（RT-63 / BRIEF §1.8：老 toml 仍是 `maxPerPlayer = 2` / `cultivatorCapacity = 16`，要生效必须删 toml 重生成）；**不新增 config 键**（仍 176 / 60）；语言键 338 → 340。
3. **不许动** README 里「数字快照」「版本历史」「当前发布」这几节（那是收尾阶段的事，F-282 只写已知限制与升级须知）。
4. 若 README 里有与事实冲突的旧表述（比如仍写 175 键 / 337 键），**只登记不改**（留给收尾统一）。

## 6. 回报（`send_message` 给 lead）

**按两个任务分两段**，每段给：`git diff --stat` + 逐处行号与改前/改后原文 + 每条验收断言的改前/改后实测值 + 构建读数（**含 `compileJava --rerun`**）+ 偏离登记 + 未验证清单 + 未关闭的问题。最后写明：无任何 git 写操作、没动农民那 3 条交易。
