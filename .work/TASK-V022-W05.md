# 任务书 · farmstorm v0.2.2 · W05（F-278 ‖ F-279 ‖ F-281）

> 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。本波 **3 个子代理并行**，各自只做一个任务。
> 前置：W01~W04 已落地并提交（HEAD `45aec5e`）。

## 0. 三个任务（详见 49 号对应小节）

| 任务 | 标题 | 复杂度 | 涉及文件（**只许动这些**） | 依赖 |
|---|---|---|---|---|
| **F-278** | `event/BotanistTrade`：**22 项**普通交易 | M / 1.5 | **新增** `event/BotanistTrade.java` | F-268（已落） |
| **F-279** | `event/BotanistEpicTrade`：**固定 1 项**史诗交易 | S / 0.5 | **新增** `event/BotanistEpicTrade.java` | — |
| **F-281** | 两条语言键 + 两处计数注释（**关窗**） | M / 1.5 | `datagen/ModLangProvider`、`src/generated/resources/assets/farmstorm/lang/{zh_cn,en_us}.json` | F-268、F-277（已落） |

> ⚠️ **F-278 / F-279 只建类，不接线**：`VillagerTradeEvents` 的分支接线是 **F-280（W06）**。你们建完类不许去改 `VillagerTradeEvents`。

## 1. 必读

1. `docs/zh/49-TASKS-0.2.2.md` —— **你那个任务的小节** + §0.1~§0.5
2. `docs/zh/46-DECISIONS-0.2.2.md` —— F-278→D-197；F-279→D-199；F-281→D-189、D-193
3. `docs/zh/48-TECH-0.2.2.md` —— 交易两节（**A-89：交易拆成两个类**，46 号只写了一个）；语言侧 §4.3（两条纪律）
4. `.work/v022-BRIEF.md` —— §1.2（村民交易的现有实现 `VillagerTradeEvents` / `TierTrade`）、§3.3（语言键）、裁定 **#17 / #18**、**RT-66 / RT-67**

## 2. 已实测基线（HEAD `45aec5e`）

| 项 | 值 |
|---|---|
| 注册面 | **98**（items 34 / blocks 27）；口径外累计 **3 类**（`ai_suspend` / `PoiType` / `VillagerProfession`） |
| Java 文件数 | **152** |
| 职业 / POI | 已落：`FarmstormPoiTypes.BOTANIST`（`maxTickets=1`）、`FarmstormVillagerProfessions.BOTANIST`；`acquirable_job_site` 标签产物已在（`data/minecraft/tags/point_of_interest_type/`） |
| 语言键 | **338 × 2**（`farmstorm.configuration.*` = **179**、`tooltip.farmstorm.*` = **20**）；`ModLangProvider:13-19` 的类注释仍是 338 / 179 |
| `VillagerTradeEvents` | 只处理 `VillagerProfession.FARMER`，`getTrades().get(1)/(3)/(5)` 各 add 一项 `TierTrade`（**不许动**） |
| `TierTrade` | `record` + `MAX_USES=1` / `CROP_COUNT=2` / 抽签只在 `getOffer` 首生成时做一次（F-279 的机制样板） |

## 3. 硬约束

1. **并行纪律**：只动你任务清单里的文件；不改别人的行、不做格式化、不 `git add`。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` / `git log` 取证。提交由调度者做。
3. **`docs/` 一字不改**；`build.gradle` / `.tools/` / `run/` 不许改（datagen 用 `-PfsRunDir=run-w05x` 隔离）。
4. **构建/datagen 必须走任务书 §4 的互斥锁**；**禁止直接调 gradle**。
5. **不许动农民那 3 条交易**（裁定 #7：农民与植物学家并存）。**不许改 `VillagerTradeEvents`**。
6. **不许自己新定数值 / 名称**；拿不准写「未关闭的问题」，**不要停下来等**。
7. 本波不要求起服；运行时不成立的判据标「静态已验 / 运行时待 F-283」，**不许编读数**。

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

## 5. 各任务的判决性验收

**F-278**
1. 新类 `event/BotanistTrade` 存在，**恰 22 项**普通交易 listing（20 个原版物品 + 平凡韭菜 + 压缩膨胀麦 —— 逐项点名列出这 22 个 id，**不许现编**，按 48 号原文的清单）。
2. 价格闭区间 **3-6**、`maxUses = 8`、每次 **1 个**；全部挂在**新手级（1 级）**。
3. 实现风格照 `TierTrade`（`implements VillagerTrades.ItemListing`），**不要**顺手改 `TierTrade` 本身。
4. 构建 exit 0；`git diff` 只多这一个新文件。

**F-279**
1. 新类 `event/BotanistEpicTrade` 存在，**恰 1 项**史诗 listing：首次生成时在 **4 个史诗作物**里随机抽 1 个，之后整个存档不变（与 `TierTrade` 同机制：抽签只在 `getOffer` 首生成时做一次，补货只 `resetUses()` **不重抽**）。
2. 价格 **40-64**、`maxUses = 1`；等级按文档（**大师级 5 级**，`docs` 里标为「本文补充」）。
3. ⚠️ **不许**实现成「每次补货重抽」—— 裁定 #18 已否掉（`Villager.restock()` 只 `resetUses()`，不重调 `getOffer`，报价一经生成即写进村民 NBT，**做不到**）。
4. 构建 exit 0；`git diff` 只多这一个新文件。

**F-281（关窗）**
1. 两条语言键：`item.farmstorm.plain_chives` 与植物学家职业名（`entity.minecraft.villager.farmstorm.botanist` 一类，按 48 号原文）。**中英各一条，键集必须逐字相等**。
2. 跑 `runData`（锁内，`-PfsRunDir=run-w05x`）后，两份 `lang/*.json` 各 **338 → 340**；`farmstorm.configuration.*` **仍 179**（本版不加 config 键）；`tooltip.farmstorm.*` 仍 **20**。逐个给数。**不许估**。
3. `ModLangProvider:13-19` 的类注释改成与键集一致（`338 → 340`，config 仍是 179；构成行按实际增减写）。**这一条是本版语言侧的关窗，改完全版本不许再增删语言键**。
4. ⚠️ **不许碰** `ModLangProvider:429`（F-274 刚改过的那行「24 是名额、54 是格子数」）与 `:24` / `:381` 的条数注释（那是语言键条数不是名额）。
5. 既有 338 条翻译的**文案内容不得改动**（`runData` 前备份两份 json 的 sha256，跑后逐个复核「只多不少、旧值不变」）。

## 6. 回报（`send_message` 给 lead）

- 任务号 + `git diff --stat` + 逐处行号与改前/改后原文（F-278 要**点名列 22 项 id**）；
- **每条验收断言的改前/改后实测值**（F-281 给 6 个数：zh / en / zh-only / en-only / configuration / tooltip）；
- 命令原文（锁等待、`build`/`runData` 退出码、产物路径）；
- 偏离登记 + 未验证清单 + 未关闭的问题；
- 明确写出：无任何 git 写操作、没动别人任务的文件、**没动农民那 3 条交易**。
