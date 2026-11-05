# 任务书 · farmstorm v0.2.2 热修（作者 2026-11-05 口头改判）

> 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。**本波只有你一个子代理，两件事都归你。**
> 背景：v0.2.2 已 tag/发布，作者看过成品后提了两条修改，**就地改 v0.2.2 并重发**（tag 会 force 重打，由调度者做）。

## 0. 两件事

| # | 作者原话 | 根因 | 改成 |
|---|---|---|---|
| **M1** | 「植物学家职业的村民只有新手和大师等级的交易项可以刷新出来，前面的刷新不出来」 | `VillagerTradeEvents:41` 把 22 项**全挂 `get(1)`**、`:43` 只把史诗挂 `get(5)` ⇒ 2/3/4 级是空表，原版每级独立表、空表就什么都抽不出来 | 22 项**摊到 1~4 级 = 6 / 6 / 6 / 4**，史诗仍在 **5 级** |
| **M2** | 「平凡农作物使用对应改良农作物的材质」 | `gen_textures.py:267` 的 `plain_chives` 候选链为空、走 `draw_plain_chives()` 画的 213 B 占位稿 | 物品模型的 `layer0` **直接指向 `farmstorm:item/friend_chives`**；删占位 PNG；退役专用画法 |

## 1. 必读

1. `src/main/java/com/dashpig/farmstorm/event/VillagerTradeEvents.java`（53 行，植物学家分支在 `:39-45`）
2. `src/main/java/com/dashpig/farmstorm/event/BotanistTrade.java`（`NORMAL` 是 **22** 条的 `List.of`，顺序稳定）
3. `src/main/java/com/dashpig/farmstorm/datagen/ModItemModelProvider.java`（95 行，`:51-53` 是 `else { basicItem(item); }`；`basicItem` 会写 `parent: item/generated` + `layer0: <模组>:item/<path>`）
4. `tools/gen_textures.py`：`ICONS` 表的 `("plain_chives", "平凡韭菜", ())` 在 `:267`；`PLAIN_CHIVES_*` 常量在 `:772-784`；`draw_plain_chives()` 在 `:787+`；`ICON_DRAWERS` 在 `:889+` 附近
5. `docs/zh/46-DECISIONS-0.2.2.md` 裁定 **#11**（普通交易全放新手级）与 **#17**（每级只抽 2 项）—— **M1 是对 #11 的改判**，`docs/` 冻结不改，你只需在回报里登记

## 2. 已实测基线

| 项 | 值 |
|---|---|
| HEAD | `9229ed7`（tag `v0.2.2`） |
| 注册面 / config / lang | **98** / **176·60** / **340 × 2** |
| Java 文件 | **154** |
| `plain_chives.png` | 213 B，16×16，程序画的占位稿 |
| `friend_chives.png` | **867 B**，作者原画转的，同一目录 |
| `models/item/plain_chives.json` | 现由 `basicItem` 生成，`layer0 = farmstorm:item/plain_chives` |

## 3. 硬约束

1. **不许动农民那 3 条交易**（`VillagerTradeEvents:49-51`，裁定 #7）。`git diff -U0` 里 `add(TierTrade` 命中必须为 **0**。
2. **不许动 `TierTrade`**；**不许动 `BotanistTrade` / `BotanistEpicTrade` 的内容**（22 项与 1 项、价格、`maxUses` 都不变）。
3. **`docs/` 一字不改**；`build.gradle` / `.tools/` / `run/` 不许改。
4. **构建 / datagen 必须走互斥锁**；**禁止直接调 gradle**；datagen 用 `-PfsRunDir=run-fix`。
5. **`build` exit 0 不证明你的文件被编译过** ⇒ 必须补 **`gw.cmd compileJava --rerun`** 的 exit code。
6. **不许自己新定数值**；拿不准写「未关闭的问题」。

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

## 5. M1 的具体做法

`get(1)`~`get(4)` 依次收 `BotanistTrade.NORMAL` 的 `[0,6) / [6,12) / [12,18) / [18,22)`，`get(5)` 仍收 `BotanistEpicTrade.EPIC`。用 `subList(...)` + `addAll(...)`，**不要**把 22 项复制成四份字面量。

改完必须给：**每级的条数**（1级 6 / 2级 6 / 3级 6 / 4级 4 / 5级 1，合计 23 条登记、但与「22 项普通 + 1 项史诗」一致）；`get(1)`~`get(5)` 的 `add`/`addAll` 调用点行号；农民三行的 `git diff` 证据。

## 6. M2 的具体做法

1. `ModItemModelProvider` 里给 `plain_chives` 加一条分支（放在 `:47` 的 `compressed_*` 分支旁边、`else basicItem` 之前）：写出 `parent: item/generated`、`layer0: farmstorm:item/friend_chives` 的模型（用 `withExistingParent(path, "item/generated").texture("layer0", modLoc("item/friend_chives"))` 一类写法，按该文件现有 API 形态）。
2. **删** `src/main/resources/assets/farmstorm/textures/item/plain_chives.png`。
3. `gen_textures.py`：删 `:267` 的 `("plain_chives", …)` 条目、`PLAIN_CHIVES_*` 常量、`draw_plain_chives()`、`ICON_DRAWERS` 里的对应项。**别动别的条目。**
4. 跑 datagen（锁内，`-PfsRunDir=run-fix`），确认 `src/generated/resources/assets/farmstorm/models/item/plain_chives.json` 变成 `layer0 = farmstorm:item/friend_chives`，且 **`gen_textures.py --list` 的总数从 100 变成 99**。

## 7. 判决性验收

**M1**
1. `get(1)`~`get(5)` 的条数依次 = **6 / 6 / 6 / 4 / 1**；2/3/4 级**不再是空表**。
2. 22 项的 id 与顺序**一字未变**（`BotanistTrade.java` 未改，给 `git diff` 空证据）。
3. 农民三行 `git diff -U0` 里 `add(TierTrade` 命中 **0**。
4. 运行期（若起服）：放核 + 村民 ⇒ 认领成 `farmstorm:botanist`；**至少能证明 2/3/4 级表非空**（可用 `/data get entity <villager> Offers` 看 `Recipes` 吗？—— 若命令读不到，就如实标「静态已验」，别编）。

**M2**
1. `models/item/plain_chives.json` 的 `layer0` = **`farmstorm:item/friend_chives`**（给文件原文）。
2. `src/main/resources/assets/farmstorm/textures/item/` 下**没有** `plain_chives.png`；`friend_chives.png` 仍在（867 B，sha256 未变）。
3. 锁内 `gw.cmd build` 后，主 jar 里：`assets/farmstorm/models/item/plain_chives.json` 存在且内容如上、`assets/farmstorm/textures/item/friend_chives.png` 存在、**`assets/farmstorm/textures/item/plain_chives.png` 不存在**。
4. `gen_textures.py --list` 总数 **100 → 99**；`--dry-run` 不再列出 `plain_chives`。
5. `compileJava --rerun` exit 0。

## 8. 回报（`send_message` 给 lead）

**按 M1 / M2 分两段**，每段给：`git diff --stat` + 逐处行号与改前/改后原文 + 每条断言的改前/改后实测值 + 命令原文（锁等待、退出码、datagen 产物原文）+ **偏离登记**（尤其：46 号裁定 #11 被作者改判这件事怎么记）+ 未验证清单 + 未关闭的问题。

最后写明：无任何 git 写操作、**没动农民那 3 条交易**、没动 `TierTrade`。
