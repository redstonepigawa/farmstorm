# v0.2.2 热修记录（作者 2026-11-05 看过成品后提出的两条）

> 本文件是 `.work/v022-verify/` 三份验收记录的**补丁说明**：那次验收跑的是**热修前**的构建，下列条目在热修后行为已变。

## 改了什么

| # | 作者原话 | 根因 | 改成 |
|---|---|---|---|
| **M1** | 「植物学家职业的村民只有新手和大师等级的交易项可以刷新出来，前面的刷新不出来」 | `VillagerTradeEvents:41` 把 22 项全挂 `get(1)` ⇒ 2/3/4 级是空表（原版每级独立表） | 22 项摊到 1~4 级 = **6 / 6 / 6 / 4**（`subList` + `addAll`），史诗仍在 5 级 |
| **M2** | 「平凡农作物使用对应改良农作物的材质」 | `gen_textures.py` 的 `plain_chives` 走专用画法，产出 213 B 占位稿 | 物品模型 `layer0` **直接指向 `farmstorm:item/friend_chives`**；删占位 PNG；`gen_textures.py` 退役整条专用画法（`--list` 100 → **99**） |

## 哪些验收条目受影响

| 编号 | 热修前的读数 | 热修后 |
|---|---|---|
| **#18**（植物学家各卖 2 项、两人不同） | 从 **22 项**里各抽 2 项、两人四项全不同 | 每级从**该级的 6 / 6 / 6 / 4 项**里各抽 2 项；一个满级植物学家最多看到 **8 项**（4 级 × 2），仍要多找几个才凑齐 |
| **#30**（`plain_chives` 的可见面） | 自带一张 16×16 程序绘制占位贴图（`plain_chives.png`，213 B） | 不再有独立贴图：模型 `layer0 = farmstorm:item/friend_chives`，`textures/item/` 由 30 张变 **29** 张 |
| **#15 / #19**（职业与史诗） | — | **不受影响**（史诗仍恰 1 项、仍在 5 级、仍 40-64 / `maxUses = 1`） |

其余 27 条技术验收与 27 条边界**不受这两条改动影响**（注册面仍 98、config 仍 176·60、语言键仍 340×2、网络包仍 2、Mixin 仍 0）。

## 热修后的实测（子代理 + 调度者各跑一遍）

| 项 | 读数 |
|---|---|
| 分级条数 | `get(1)`~`get(5)` = **6 / 6 / 6 / 4 / 1**；`git diff -U0` 里 `add(TierTrade` 命中 **0**（农民三行逐字未变，仅行号 49-51 → 55-57） |
| 运行期分级（子代理，起服 `run-fix` + RCON） | 2 级：pitcher_plant + bamboo / melon + bamboo；3 级：brown_mushroom + golden_apple；4 级：warped_fungus + cocoa_beans；5 级：史诗 1 项（40 绿 / `maxUses` 1）；阳性对照：原版 farmer 2 级同样抽 2 项 |
| 模型 | `{"parent":"minecraft:item/generated","textures":{"layer0":"farmstorm:item/friend_chives"}}` |
| 贴图 | `plain_chives.png` **不存在**；`friend_chives.png` 867 B / sha256 未变；`textures/item/` **29** 张 |
| jar | 620 entries；`friend_chives.png` **1** 条、`plain_chives.png` **0** 条 |
| 构建 | 锁内 `compileJava --rerun` **exit 0**、`build` **exit 0** |
| datagen | `written: 1`（只重写 `models/item/plain_chives.json`），`removed stale: 0` |

## 未验证

- **客户端目视**：`plain_chives` 在物品栏里是否就是「朋友的韭」那张图，只有「模型 json + jar 内资源」两层证据，没有人眼读数。
- **1 级的表非空**：探针无法给 1 级村民挂职业（用原版 `minecraft:farmer` 复现同样现象 ⇒ 探针/原版侧怪癖，与本改动无关），故 1 级只有静态证据（`subList(0,6)` + 22 项未变）。

## 登记给作者的文档偏差（`docs/` 冻结，未改）

- `46-DECISIONS` 裁定 **#11**「普通交易全放新手级」被本次作者改判为「1~4 级 6/6/6/4」；裁定 **#17**「每级只抽 2 项」**不变**。
- `48-TECH §2.5.3`、`49-TASKS` F-269 的「目录 29 → 30」、`50-PROMPT-MAIN` 第 19 行、`51-PROMPT-TASKS` 的 F-269 验收，热修后全部过期。
