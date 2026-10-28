# 任务书 M2-A · 爆炸与全局事件地基（Phase 3：F-14 ~ F-18）

> 先读 `.work/PLAN.md` §0 与 §4。本任务书是你的全部边界。前置：M1-A 已交付且 `build` 为绿。

## 1. 目标

把「破坏 / 伤害 / 表现」三段式爆炸、爆炸批处理、爆炸免疫、临时附魔账本与 game 总线事件立起来。Phase 4 的 5 个行为全部复用这一层，所以**先把这里做对，再写作物**。

## 2. 必读
1. `docs/zh/02-TECH-SPEC.md` §4.9（`FarmstormExplosion` + `BlastRule`，含伤害封顶公式）、§4.10（`TempEnchantHandler`）、§8.1~§8.3（错峰 / 批处理 / 防刷）、§5.1~§5.4（服务端权威）。
2. `docs/zh/00-DECISIONS.md`：D-14、D-15、D-19、D-27、D-37、D-38、D-39、D-41、C-6、C-8。
3. `docs/zh/01-DESIGN.md` §7.1（爆炸保护）、§7.15（多光环叠加与临时附魔冲突）。
4. `docs/zh/05-PROMPT-MAIN.md` §9 的「已知陷阱」全段（RT-02/03/05/06/08/09 都落在本任务）。

## 3. 交付物

### 3.1 `explosion/FarmstormExplosion.java`、`explosion/BlastRule.java`
- 按 TECH §4.9 的签名实现；三段式 `finalizeExplosion`。
- `BlastRule` 的三个预置常量 + 从 `FarmstormConfig` 读阈值（`explosion.sliceResistThreshold` / `grenadeResistThreshold`）。
- 伤害公式逐字照抄 TECH §4.9 的伪代码；`damageCap <= 0` 时跳过伤害段。
- **任何方块破坏都跳过爆炸抗性 ≥ 3.6e6 的方块**（D-38/T-05），并排除 `#farmstorm:explosion_immune`。
- 击杀归属：`DamageSource` 带 owner。

### 3.2 `explosion/ExplosionQueue.java`
- 用内部常量 `MAX_EXPLOSIONS_PER_TICK = 8`（TECH §4.11.6，**不得挪进 config**，HC-13）。
- 同 tick 超量爆炸入队，`LevelTickEvent.Post` 逐个执行，延迟 ≤ 1 tick。

### 3.3 `event/FarmstormGameEvents.java`
- game 总线：`PlayerTickEvent.Post`（每 20 tick 跑临时附魔自愈 + 光环到期撤销）、`PlayerLoggedInEvent` / `PlayerLoggedOutEvent`（创造模式对账的三级兜底，D-18/RT-05）、`ExplosionEvent.Detonate`（从 `getAffectedBlocks()` 移除 `#farmstorm:explosion_immune`，RT-08）。
- 心跳日志必须可开关（F-15 验收项①）。
- 客户端不得进入任何判定路径（HC-03）。

### 3.4 `util/TempEnchantHandler.java`
- 按 TECH §4.10 逐条：`grant` / `tick` / `revokeExpired` / `revokeAll` / `hasTemporary` / `write` / `read`。
- 账本落在物品的 `farmstorm:temporary_enchantments` 组件（D-41），**不写玩家持久数据**。
- 撤销时只在「当前等级 == 授予等级」才还原（RT-03 不覆盖检查）。
- `expireAt` 用绝对 gameTime。

### 3.5 `util/FarmstormTags.java` 补 tag key；`data/farmstorm/tags/block/explosion_immune.json` 由 DataGen 生成（见 M5；如果在 M5 之前需要，先确认 M5 的 provider 已存在，否则上报）

## 4. 不做
- 不实现 `behavior/**` 的 5 个作物行为（M2-B）。
- 不改 `registry/**`（注册条目已由 M1-A 定稿）。
- 不实现幸运浆果的创造模式分支（M4），但 `PlayerLoggedInEvent` 的对账框架要留出接口**且真实可用**。

## 5. 约束
- HC-13：6 个安全阀常量不得调整、不得进 config。
- HC-03：所有判定在服务端；不新增网络包。
- 不写 TODO / 不注释实现 / 不留空方法。

## 6. 验收
1. `gw.cmd compileJava` 与 `gw.cmd build` 均绿。
2. 单测式断言：在 `Farmstorm` 启动日志里打印 `DistanceMetrics` 的两条断言（`chebyshev(3,1,3)==3`、`euclidean(3,4,0)==5.0`）与幸运浆果权重和 = 1000（若 M4 未落地则打印 config 层百分比和 = 100）。
3. `gw.cmd runServer` 起服出现 `Done (`。
4. 爆炸封顶与抗性阈值：写一个临时验证入口（例如一次 `/summon` 后靠日志断言），证明 `power=4, cap=2` 时中心玩家只掉 2 点血、圆石不破泥土破。**如果无法在无客户端环境下构造，就在回报里说明做不到并给出你实际验证到的部分。**
5. 临时附魔：组件读写往返一致；到期后组件被移除；原等级 2 不被降级。

## 7. 回报
按 `docs/zh/05-PROMPT-MAIN.md §7` 模板；必须写清**为 M2-B 与 M4 暴露的接口签名**（`FarmstormExplosion` 构造参数、`ExplosionQueue.submit(...)`、`TempEnchantHandler` 的公开方法），以及未决问题。
