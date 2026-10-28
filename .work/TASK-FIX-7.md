# 任务书 FIX-7 · 作物类别建模成「多标签集合」

> 作者指出：**一个作物可以有多个类别标签**（DEC §4 的表里多数作物就是两个甚至三个）。当前实现没有这个概念，必须补上，并让**行为由标签驱动**而不是手工接线。

## 1. 现状（已核实，不用你再查）
- `util/CropProfile` 是个 record：`(id, cropBlock, seedItem, kind, allowedFaces, throwStack)`，**没有任何类别字段**。
- `behavior/DeployedKind` 是「部署行为种类」单值枚举（BLAST_CARROT/FANG_TRAP_CACTUS/PUMPKIN_TURRET/AWAKENED_TORCHFLOWER/AWAKENED_PITCHER_PLANT），不是类别。
- 「破障类」在代码里**不存在为标签**，只是 FIX-6 时在各爆炸调用点手工选 `BlastRule.NEVER_BREAK` 或破坏规则：
  `MelonSliceProjectile` / `UbiServerProjectile` / `ToxicUbiServerProjectile` / `MelonGrenadeProjectile` / `BlastCarrotTrap` / `LuckyBerryRoller`。
- 全仓没有 `CropCategory` / `DEMOLITION` 之类枚举（grep 过）。

## 2. 交付物

### 2.1 新枚举 `util/CropCategory.java`
六个值，对应 `01-DESIGN.md §2.3` 的六类：
`TRAP`（陷阱类）/ `TURRET`（炮台类）/ `DEMOLITION`（破障类）/ `BUFF`（buff类）/ `CONSTRUCTION`（建造类）/ `DAMAGE`（伤害类）。
- 每个值带 `translationKey()` → `tooltip.farmstorm.category.<lowerName>`（例如 `tooltip.farmstorm.category.demolition`）。
- 加一个 `Codec`（`StringRepresentable` 或简单 `Codec`）备用，**不要**加进 config、不要新增注册对象。

### 2.2 `CropProfile` 增加 `Set<CropCategory> categories`
按 `00-DECISIONS.md §4` 的表**逐条**填（这一列现在是唯一真相，别再凭印象）：

| 作物常量 | 类别（DEC §4 原文） |
|---|---|
| PUFFED_WHEAT | 建造类 |
| BLAST_CARROT | 陷阱类、伤害类 |
| UBI_SERVER | 破障类、伤害类 |
| TOXIC_UBI_SERVER | buff类、破障类、伤害类 |
| POWER_BEET | buff类 |
| MELON_SLICE_BOMB | 伤害类 **+ 破障类**（作者 2026-10-28 指定新增，见 PLAN R-48） |
| MELON_GRENADE | 破障类、伤害类 |
| PUMPKIN_TURRET | 炮台类、伤害类 |
| AWAKENED_TORCHFLOWER | 炮台类、增益类（C-4：分类保留「炮台」，实现按 AuraProvider） |
| AWAKENED_PITCHER_PLANT | 炮台类、增益类 |
| FANG_TRAP_CACTUS | 陷阱类、伤害类 |
| BAMBOO_FIRECRACKER | 伤害类 |
| RED_MUSHROOM_PUFF | buff类 |
| BROWN_MUSHROOM_PUFF | buff类 |
| LUCKY_BERRY | buff类 |

加两个便利方法：`Set<CropCategory> categories()`（record 自动有）、`boolean has(CropCategory)`、`boolean canBreakBlocks()`（= `has(DEMOLITION)`）。

### 2.3 行为改为**由标签驱动**
- `explosion/BlastRule` 加一个工厂：`static BlastRule forCrop(CropProfile profile, BlastRule whenDemolition)` —— 非破障类直接返回 `NEVER_BREAK`，破障类返回传入的破坏规则（阈值仍由调用点决定）。
- 六个爆炸调用点全部改成走这个工厂：
  - `MelonSliceProjectile` → `BlastRule.forCrop(profile(), BlastRule.sliceBomb())`
  - `UbiServerProjectile` / `ToxicUbiServerProjectile` / `MelonGrenadeProjectile` → 同理（各自保留原阈值规则）
  - `BlastCarrotTrap` → `BlastRule.forCrop(CropProfile.BLAST_CARROT, BlastRule.VANILLA)`
  - `LuckyBerryRoller` 小爆炸 → `BlastRule.forCrop(CropProfile.LUCKY_BERRY, BlastRule.UNBREAKABLE_SKIP)`
- 目的：**把「哪类作物能破方块」收敛到 DEC §4 那一列**，以后只改 profile 一处。

### 2.4 工具提示显示**全部**类别
- `CropProfile` 的 tooltip 现在用 `tooltip.farmstorm.type.{trap|turret|aura}`（单值）。改成遍历 `categories` 逐个拼 `tooltip.farmstorm.category.*`，多个类别用 `、` 或 `, ` 连接（中英各自的分隔符）。
- 语言文件里**新增 6 个** `tooltip.farmstorm.category.*` 键；旧的 3 个 `tooltip.farmstorm.type.*` 键若不再被引用，一并删掉（避免留死键），并在回报里给出新的总键数。

## 3. 约束
- 不改 `00-DECISIONS.md` / `01-DESIGN.md`（HC-06）；DEC §4 的那一列照抄，不重新设计。
- 不新增注册对象、不加 config 键、不改 117 键集合。
- `DeployedKind` 保持不动（它是行为实现种类，与类别是两回事，别合并）。

## 4. 验收
1. `gw.cmd compileJava` / `build` 绿、零 warning；`verify_project.py` FAIL 0（注意语言键数会变，报告新总数）。
2. **启动自检**：打印 15 条「作物 ID → 类别集合」，逐条与 DEC §4 对照（这是最关键的一条证据）。
3. `runData` 后中英语言键集仍完全一致；贴出新的总键数。
4. **运行时回归**（用 `runServer` + RCON，或用已有探针手法）：
   - 瓜片子炸泥土 → 泥土消失（破障类，含在 tags 里）；
   - 胡罗贝触发 → 周围方块不动（无 DEMOLITION 标签）；
   - 至少一个**多标签**作物的 tooltip 能同时显示两个类别（例如火把花「炮台类、增益类」）—— 用截图或 tooltip 文本日志为证。
5. 贴出「改前手工接线 vs 改后由标签驱动」的对照：证明把 `MELON_SLICE_BOMB` 的 `DEMOLITION` 标签临时去掉后，**不改任何爆炸调用点**，它就不再破方块（然后就改回）。这条是本次重构的核心价值，必须验。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外给：15 条类别表、新语言键总数、以及 §4.5 的「只改标签就改变行为」对照证据。
## 6. 更正（作者提醒）：`型` 与 `类` 是正交的两个维度

`01-DESIGN.md` §2.2 类型（Type）= 投掷型 / 部署型 / 种植型 / 食用型；§2.3 类别（Category）= 陷阱类 / 炮台类 / 破障类 / buff类 / 建造类 / 伤害类；§2.4 明确两者正交。DEC §4 的表也是两列分开。

因此 `CropProfile` 要同时持有两个**独立**字段：
1. `CropType type` —— **单值**，取 DEC §4「类型」列：投掷型 8 种 / 部署型 5 种 / 食用型 2 种；种植型按 D-10 不赋值。
2. `Set<CropCategory> categories` —— **多值**，取 DEC §4「类别」列（+ 作者给瓜片子加的破障类）。

约束：两者不得互相推导；不得用 `DeployedKind` 代替 `type`（C-4：分类是炮台、实现是光环）。启动自检分两栏打印，便于逐条对照 DEC §4。
