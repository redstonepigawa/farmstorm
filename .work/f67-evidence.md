# F-67 证据（从轮转日志抢救，代理中途死亡）

> 原代理 `3eaa40d3` 在执行中断开、无回报。它的探针 `src/.../debug/ProbeF67.java` 已删除，工作区已清理、编译已验证。
> 下面的矩阵结果是**从 `run-f67/logs/2026-10-29-1.log.gz`（已轮转的归档）里捞回来的**——教训：证据别只留在 `run-*/logs/latest.log`，重启就轮转没了。

## 方块属性核对（结论：**零改动**，F-65 建方块时已按规格写好）

```
id=farmstorm:compressed_hay_block
class=net.minecraft.world.level.block.Block          ← 普通 Block，无自定义子类 ✅
defaultDestroyTime=30.0                               ← 硬度 30 ✅
getDestroySpeed=30.…                                  ← 与上面一致 ✅
mapColor=… (COLOR_BROWN)                              ← ✅
mineable/pickaxe=false  needs_diamond_tool=false      ← 标签未加，属 F-74（见下）
farmstorm:explosion_immune=false  farmstorm:blast_proof=false
```

## A-06 抗性后果矩阵（**四行全部实测通过**）

| 来源 | power | 抗性 | 阈值 | `rule.canBreak` | 结果 |
|---|---|---|---|---|---|
| 瓜！ `BlastRule.grenade()` | 3 | 6.0 | 6.0 | **false** | **方块存活** ✅ |
| 瓜片子 `BlastRule.sliceBomb()` | 2 | 6.0 | 6.0 | **false** | **方块存活** ✅ |
| 育碧 `BlastRule.UNBREAKABLE_SKIP` | 4 | 6.0 | 3600000.0 | **true** | **方块被破坏** ✅ |
| 毒育碧 `BlastRule.VANILLA` | 2 | 6.0 | 3600000.0 | **true** | **方块被破坏** ✅ |
| 原版爆炸（`Level#explode`, power=4, TNT 口径） | 4 | 6.0 | — | 走原版公式 | **方块被破坏** ✅ |

最后一行是 A-06 最反直觉的一条，实测成立：**破障类（瓜！/瓜片子）炸不动的方块，原版 TNT 炸得动**——因为原版走的是逐格累积衰减，不是我们的 `BlastRule`。

## 未通过的验收项（原因：依赖 F-74，不是缺陷）

| 验收项 | 现状 | 依据 |
|---|---|---|
| 空手左键 30 秒不掉落 | 探针 `hasCorrectToolForDrops=false`，空手确实不掉；但探针同时报「空手 destroyBlock 成功」——那是探针**直接调 `destroyBlock`** 绕过了挖掘进度，不是真实生存行为 | 真实行为需按挖掘进度验证 |
| **钻石镐能挖并掉本体** | ❌ 现状挖穿要 **150 秒**且 0 掉落：`mineable/pickaxe=false`、`needs_diamond_tool=false` → 钻石镐与空手同样 0.000333 进度/tick | `13-TASKS` F-74 才交付这两条标签与战利品表；`12-TECH §7.2 #36` 也把 `needs_diamond_tool` 归在资源清单 |

**结论**：F-67 的方块属性部分 **零改动通过**；矩阵部分 **四行全过**；「钻石镐挖出本体」这一条**必须等 F-74**，已在 PLAN V-14 登记为 F-74 的收口项。

## 更正：上一版写的「钻石镐 750 秒」是错的（FakePlayer 假象）

`Player#getDigSpeed` 末尾有 `if (!onGround()) f /= 5.0F;`。探针用的 FakePlayer 没落地，白吃 5 倍减速，于是算出 0.2 速度、750 秒。
`setOnGround(true)` 之后的**真实玩家口径**：空手与钻石镐都是 `0.000333 进度/tick`（`= 1.0 / 30 硬度 / 100 非正确工具`）→ **挖穿需 3002 tick = 150 秒**。

## 真实挖掘补测（走 `handleBlockBreakAction(START)` + 逐 tick `tick()`，不瞬破）

| 用例 | 进度/tick | tick 数 | 期望进度 | 方块被破坏 | 掉落 | BLOCK_MINED |
|---|---|---|---|---|---|---|
| 空手 左键 30 秒 | 0.000333 | 600 | 0.200 | **false** | **0** | 0→0 |
| 钻石镐 左键 30 秒 | 0.000333 | 600 | 0.200 | **false** | **0** | 0→0 |
| 空手 挖到穿 | 0.000333 | 3002 | — | true | **0** | 0→0 |
| 钻石镐 挖到穿 | 0.000333 | 3002 | — | true | **0** | 0→0 |

读法：**30 秒连方块都没破**（进度 20%），掉落 0、`BLOCK_MINED` 不动 → 「空手左键 30 秒不掉落任何物品」成立且比原判据更强。
真挖穿后**仍然 0 掉落**，说明卡住掉落入口的是 `requiresCorrectToolForDrops` 的工具门槛，**不是**战利品表缺失（两者都在 F-74 补齐）。

> 注：1.21.1 里 `tick()` 只累计进度，真正的破坏发生在客户端发 `STOP_DESTROY_BLOCK` 之后；所以「30 秒」用例只发 START 不发 STOP，与真实客户端行为一致。

## 更正二：`BLOCK_MINED 0→0` 这条论据不成立（F-74 发现）

上面表格里用 `BLOCK_MINED` 恒 0→0 来推断「`playerDestroy` 从未执行」——**这个推断无效**：
`FakePlayer#awardStat` 是空实现（`FakePlayer.java:100`），所以该计数器**无论掉不掉都是 0→0**，没有判别力。

**结论本身不变**（当时卡住掉落的确实是 `requiresCorrectToolForDrops` 的工具门槛），但论据要换成：
`canHarvestBlock` 判定 + 实际掉落实体计数 + `Block.getDrops()` 直算。F-74 用这三条重测后确认：
空手/木镐/铁镐挖穿后掉落均为 0，钻石镐挖穿掉本体 1 个（113 tick）。
