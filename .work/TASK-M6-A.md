# 任务书 M6-A · 进度授予 + 创造模式口径收口（F-47、F-33）

> 先读 `.work/PLAN.md` §0/§4/§6~§12。前置：M1~M5 全部已交付。

## 1. 目标

补上**唯一还缺的一条功能链**：12 条进度里有 8 条没有原版触发器，需要服务端在正确时机 `award(...)`。顺带核对创造模式的两条口径。

## 2. 必读
1. `docs/zh/02-TECH-SPEC.md` §6.7（12 条进度表 + 「用 `minecraft:impossible` + 服务端 `award` 授予」的实现补充）。
2. `docs/zh/01-DESIGN.md` §6（进度清单与触发条件原文）、§7.6（创造模式玩家）。
3. `docs/zh/00-DECISIONS.md`：D-07、D-24、C-7。

## 3. 交付物

### 3.1 在正确时机授予 8 条进度
以 `datagen/ModAdvancementProvider.AWARD_REQUIRED` 为准：

| 进度 ID | 触发时机（要在代码里找到唯一的正确点） |
|---|---|
| `farmstorm:first_conversion` | `ConverterBlockEntity#tryConvert` 成功之后 |
| `farmstorm:nether_star_toll` | 同上，但仅当本次是「原版→激活版」且消耗了下界之星 |
| `farmstorm:first_throw` | 投掷型物品成功生成投掷物实体之后（`ThrowableCropItem#use` 的服务端分支） |
| `farmstorm:demolition` | 破障类作物爆炸**至少破坏 1 个方块**之后（在 `FarmstormExplosion` 的破坏段或投掷物 onImpact 之后判定） |
| `farmstorm:turret_hit` | 玩家被 man瓜 导弹命中（`TurretBullet` 命中且目标是 ServerPlayer） |
| `farmstorm:aura_bath` | 玩家首次从火把花/瓶子草光环获得效果（`AuraProvider` 两个实现施加成功之后） |
| `farmstorm:fang_dance` | 仙人掌陷阱触发（`FangTrapCactusTrap` 触发瞬间） |
| `farmstorm:lucky_jackpot` | 幸运浆果抽中 `Outcome.CREATIVE` 分支 |

要求：
- 用 `ServerPlayer#getAdvancements().award(holder, "criterion_name")`（或等价的 `AdvancementHolder` 取法），criteria 名称必须与 `ModAdvancementProvider` 生成的 JSON 里一致 —— **先读那 8 个 JSON 确认 criteria 键名**。
- 授予只在服务端（HC-03），且不得新增网络包。
- ⚠️ 这几处都在别人写好的文件里，**只允许加最小的授予调用**：不要重构、不要改行为语义、不要顺手格式化。逐文件在回报里说明加了哪几行。

### 3.2 创造模式两条口径核对（只核对，发现问题先回报不要擅自改语义）
- F-33 ③：创造模式玩家站在陷阱旁**不触发**（`BlastCarrotTrap#findTrigger` 是否跳过 `instabuild` 玩家）。
- F-33 ④：创造模式玩家挖 `pumpkin_turret` **仍要 10 秒**（`DeployedCropBlock#getDestroyProgress` 是否对创造模式也返回固定速率）。

## 4. 验收
1. `gw.cmd compileJava` / `build` 绿，**零 warning**。
2. `gw.cmd runServer -PfsRunDir=run-m6a` 出现 `Done (`，日志 0 ERROR / 0 FATAL。
3. 8 条进度逐一给出「授予点：文件:行」与「criteria 名」的对照表。
4. 能在专用服务器上实测的（`first_conversion` 可以用 `/execute` + 临时假玩家路径，`first_throw` 需要玩家）**如实区分实测与静态论证**，不要写「已验证」。
5. `python tools/verify_project.py` FAIL 0 / WARN 0。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外给 3.1 的对照表与 3.2 的核对结论。
