# 任务书 FIX-6 · 爆炸破坏权限、胡罗贝掉落、炮台射程与索敌

> 作者四条玩法修改。先读 `.work/PLAN.md` §0/§4/§6~§17。这是作者的口径变更，**与上游文档冲突的地方一律按作者为准**，并在回报里逐条登记。

## 1. 只有「破障类」作物能破坏方块

作者原话：「所有没有破障类标签的作物不能破坏方块」。
破障类（DEC §4）= **育碧服务器 / 毒育碧服务器 / 瓜！**。其余（瓜片子、胡罗贝、幸运浆果小爆炸）**一律不得破坏任何方块**。

做法：在 `explosion/BlastRule.java` 增加一个「永不破坏」预置规则（形如 `new BlastRule(s -> false, 0.0F, false)`，`canBreak` 恒 false），然后：

| 调用点 | 现在 | 改成 |
|---|---|---|
| `entity/thrown/MelonSliceProjectile`（瓜片子） | `BlastRule.sliceBomb()` | **永不破坏** |
| `entity/thrown/MelonGrenadeProjectile`（瓜！，破障类） | `grenade()` | 不变（可破坏） |
| `entity/thrown/UbiServerProjectile`（育碧，破障类） | `UNBREAKABLE_SKIP` | 不变 |
| `entity/thrown/ToxicUbiServerProjectile`（毒育碧，破障类） | `VANILLA` | 不变 |
| `behavior/impl/BlastCarrotTrap`（胡罗贝） | 现规则 | **永不破坏** |
| `util/LuckyBerryRoller` 的 `SMALL_EXPLOSION` | `UNBREAKABLE_SKIP` | **永不破坏** |

注意：瓜！的 4 片子母弹是 `MelonSliceProjectile`，自动跟着变成不破坏——这符合「只有破障类能破坏」。

## 2. 胡罗贝触发后**不掉落、直接消失**（已按作者澄清更正）

作者澄清：胡罗贝触发后应当**静默消失、不产生任何掉落物**。

实际缺陷是：**胡罗贝的爆炸把胡罗贝自己炸掉了**，方块被破坏后走战利品表（`loot_table/blocks/blast_carrot.json` 掉自身）才产生掉落物。

改法：**不加任何掉落逻辑**。按 §1 把胡罗贝的爆炸规则改成「永不破坏任何方块」之后，它连自己都炸不掉；`consume()` 保持 `removeBlock(pos, false)`（本就不掉落）。

注意：`ExplosionQueue` 可能把爆炸推迟 ≤1 tick，「先 consume 还是先爆炸」的顺序不保证——用 NO_BREAK 规则同时覆盖两种情况，不要依赖调用顺序。

## 3. 所有爆炸不分敌我造成伤害

**我先查过：`FarmstormExplosion#hurtCapped` 遍历的是 `level.getEntitiesOfClass(LivingEntity.class, box)`，没有任何 `isAlly` 过滤——机制上已经是「不分敌我」。**
所以这一条你要做的是**验证而不是改**：
1. 用 grep/读码证据确认：从 `FarmstormExplosion` 到各爆炸调用点，全链路没有 `isAlly`/`TEAMS` 过滤伤害。
2. 用运行时证据：在服务端起服 + forceload 区块，放一个实体（例如僵尸 / 盔甲架 / 玩家替身）在爆炸范围内，构造一次爆炸，确认它**确实掉血**（注意：`Exposure` 视线判定会让躲在方块后的实体伤害为 0，测试场要开阔）。
3. 如果发现任何过滤（包括 `ignoreExplosion` 之外的自定义判断），删掉它。
4. 回报里明确写「原本已满足 / 我删掉了哪一处」——**不要假装修了一个不存在的问题**。

## 4. man瓜：射程 15 + 攻击敌对生物 + 归属放置者

### 4.1 默认射程 5 → 15
- `config/FarmstormConfig.java` 的 `pumpkinTurretTargetRadius` 默认值 `defineInRange("targetRadius", 5, 0, 64)` → **15**，范围上界保持 64。
- ⚠️ **必须提醒作者**：改默认值不会影响已经生成的 `run/config/farmstorm-server.toml` 或他存档里的 config。回报里要写一句「已有配置需要删掉该文件重新生成，或手动把 `targetRadius` 改成 15」。

### 4.2 攻击一切敌对生物
现状：`PumpkinTurret.findTarget` 只找 `ServerPlayer`（非旁观、非创造、非己方），加可选的投掷物拦截。
改：目标集合 = **非己方玩家（保持现有排除规则）+ 敌对生物（`net.minecraft.world.entity.monster.Monster`，例如僵尸/骷髅/苦力怕等）+ 可选的投掷物拦截（原逻辑不变）**，取**最近**的一个开火。
- 注意「敌对生物」指原版 hostile mob；**不要**把动物/村民算进去。
- 保持切比雪夫距离口径（D-20），并保持在冷却窗口内不索敌的既有行为。

### 4.3 伤害归属 = man瓜的放置者
- `TurretBullet` 命中时用的是 `FarmstormDamageTypes.voidBolt(server, this.getOwner())`；只要**开火时把 owner 设成部署者**，死亡信息/击杀归属就正确。
- 检查 `PumpkinTurret#fire`：现在是否给 `TurretBullet` 设了 owner？没设就用 `be.owner(level)`（在线时）设上；**放置者离线时**，允许 owner 为 null（不要为此去查离线玩家的 Entity）。
- 回报里给出证据：一次命中后 `damageSource.getEntity()` 是放置者。

## 5. 边界
可改：`explosion/BlastRule.java`、`entity/thrown/*.java`、`entity/TurretBullet.java`、`behavior/impl/BlastCarrotTrap.java`、`behavior/impl/PumpkinTurret.java`、`blockentity/DeployedCropBlockEntity.java`、`util/LuckyBerryRoller.java`、`config/FarmstormConfig.java`。
不要碰 `registry/`、`menu/`、`client/`、`datagen/`、`tools/`。

## 6. 验收
1. `gw.cmd compileJava` / `build` 绿、零 warning。
2. 「只有破障类破坏方块」：**逐调用点**列出「这个投掷物/装置用的是什么规则、能不能破方块」，并给出运行时证据（例如：瓜片子炸泥土后泥土**还在**；育碧炸泥土后泥土没了）。
3. 胡罗贝：触发后原地出现 1 个 `farmstorm:blast_carrot` 掉落物（截图或日志证据）。
4. 爆炸伤害：给出「僵尸被炸掉血」的运行时证据 + 全链路无 `isAlly` 的读码证据。
5. man瓜：`targetRadius` 新默认值 15；僵尸进入 15 格后被锁定开火（运行时证据）；命中后 `damageSource.getEntity()` = 放置者（日志证据）。
6. 若改了 config 默认值，`verify_project.py` 的 117 键比对必须仍是 FAIL 0（键名没变，只是默认值变了）。
7. 临时探针交付前删净（`*Probe*.class` = 0，src 无 probe 痕迹）。

## 7. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外给：四条改动的逐条「改前/改后」、与上游文档冲突的登记清单（例如 D-07 只说炮台锁玩家、现在要加敌对生物；D-21 数值原样、现在射程 5→15）、以及 config 默认值变更对已有存档的影响提示。
## 8. 追加（作者实测）：爆炸必须也伤使用者

`FarmstormExplosion#hurtCapped` 第 145 行 `if (entity == directSource || entity.ignoreExplosion(this)) continue;` —— `directSource` 就是投掷者/部署者，**使用者被显式跳过**。原版 `Explosion` 没有这种自我排除。

改：删掉 `entity == directSource ||`，只保留 `ignoreExplosion` 判断；`directSource` 局部变量一并删除。

影响：部署者站在自己的陷阱旁也会被炸（符合「不分敌我」）。`TurretBullet` 走独立 `hurt` 路径，不受影响。

验收：使用者自己掉血 + 同场僵尸也掉血，给改动前后对照。
