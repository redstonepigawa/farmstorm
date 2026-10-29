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
| **钻石镐能挖并掉本体** | ❌ 现状**挖不动**：`mineable/pickaxe=false`、`needs_diamond_tool=false` → 钻石镐与空手同样 0.2 速度、**750 秒** | `13-TASKS` F-74 才交付这两条标签与战利品表；`12-TECH §7.2 #36` 也把 `needs_diamond_tool` 归在资源清单 |

**结论**：F-67 的方块属性部分 **零改动通过**；矩阵部分 **四行全过**；「钻石镐挖出本体」这一条**必须等 F-74**，已在 PLAN V-14 登记为 F-74 的收口项。
