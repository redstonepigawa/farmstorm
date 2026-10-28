# 任务书 FIX-8 · 胡罗贝伤害退化为 1 点 + 两种蘑菇落地特效

> 先读 `.work/PLAN.md` §0/§4/§6~§18。两条独立问题，一起做。

## 1. 胡罗贝爆炸伤害只有 1 点（**高优先级**）

作者实测：胡罗贝触发后对范围内的玩家只造成 **1 点**伤害（期望是 config 的 `blast_carrot.maxDamage`，默认 7）。

### 1.1 调度者的初步定位（你要复核，别照抄）
- `behavior/impl/BlastCarrotTrap#trigger` 里 `Vec3 center = pos.getCenter()` —— 这是**陷阱方块自己的几何中心**，位于实心方块内部。
- 同一方法先 `ExplosionQueue.submit(...)` 再 `be.consume()`；队列未满时 `submit` 会**立即执行**，此时方块还在，爆炸中心就埋在方块里。
- `FarmstormExplosion#hurtCapped`：`impact = (1 - d/reach) * Explosion.getSeenPercent(center, entity)`，而 `raw = (impact²+impact)/2 * 7 * reach + 1.0`。
  中心在方块内 → 视线射线立刻被挡 → `seen ≈ 0` → `impact ≈ 0` → `raw = 1.0` → **恰好 1 点**。与作者观察完全吻合。

### 1.2 要求
1. **先测再改**：构造「地面放一个胡罗贝、玩家（或等价实体）站在紧邻格」的场景，测出当前伤害并记进回报（预期 1.0）。
2. 修到「贴脸站位时伤害 = `maxDamage`（默认 7）」：
   - 首选修根因：把爆炸中心挪到**不会被自己方块遮挡**的位置（例如方块顶面中心 `pos.getBottomCenter().add(0, 1.0, 0)`），或把 `consume()` 提到 `submit()` 之前（注意：`ExplosionQueue` 可能延后执行，两种顺序都要正确）。
   - 同时加一层**防御**：`hurtCapped` 里当 `getSeenPercent` 返回 0（中心埋在方块内）时不要让伤害退化成 1——例如先判断中心所在方块是否可穿，必要时把取样中心沿 +Y 抬到空位再算；**不要**引入凭空的常数加权。
3. **不要动公式的其它部分**（威力、reach = power×2、封顶逻辑），TECH §4.9 的口径保持。
4. 回归：其它投掷物的伤害不能变差——至少复测「育碧服务器贴脸 ≤ `ubi_server.maxDamage`(2)」「瓜！贴脸 ≤ 8」，并说明它们是否也受同一 bug 影响（它们的中心在命中点、通常在空气中，理论上不受影响）。

## 2. 两种蘑菇投掷物落地特效

`entity/thrown/RedMushroomPuffProjectile` 与 `BrownMushroomPuffProjectile` 现在落地**只有效果、没有任何视觉**。要求：`onImpact` 时在落点播**贴合物品本身**的粒子。

### 要求
- **红菇**：红色系。建议 `ParticleTypes.ENTITY_EFFECT` + 深红色（如 `0xC03030`）为主，混少量 `ParticleTypes.SMOKE`；表现「一团红色的孢子雾」。
- **棕菇**：棕色系。建议 `ParticleTypes.ENTITY_EFFECT` + 棕色（如 `0x8B5A2B`）为主，混少量 `ParticleTypes.SMOKE`；表现「一团土色孢子雾」。
- 粒子数、扩散半径：用**内部常量**（这不是玩法数值，不进 config——HC-07 的 117 键是封闭集合）。建议 24~40 个、半径沿用该作物的 `radius` config 作为扩散范围（这样半径改了特效跟着改，不会脱节）。
- 只在**服务端**广播（`ServerLevel#sendParticles`），不新增网络包（C-6）。
- 可顺带加一个原版音效（例如 `SoundEvents.SLIME_BLOCK_BREAK` 或更贴合的），**可选**，加了在回报里说明。

## 3. 边界
可改：`behavior/impl/BlastCarrotTrap.java`、`explosion/FarmstormExplosion.java`、`explosion/ExplosionQueue.java`（如需）、`entity/thrown/RedMushroomPuffProjectile.java`、`entity/thrown/BrownMushroomPuffProjectile.java`。
不要碰 `registry/`、`config/`（不加键）、`datagen/`、`client/`、`menu/`、`util/CropProfile`。

## 4. 验收
1. `gw.cmd compileJava` / `build` 绿、零 warning；`verify_project.py` **FAIL 0**，且 **117 个 config 键数量不变**。
2. §1.2 的「改前 1.0 / 改后 = maxDamage」对照，附实际伤害数值（用探针或 RCON 读血量，写清怎么读的）。
3. 育碧 / 瓜！ 的贴脸伤害回归数值。
4. 蘑菇特效：给出粒子确实广播的证据（例如临时统计 `sendParticles` 调用与参数，或客户端截图）。**如果只能静态论证，要明说**。
5. 临时探针交付前删净（`*Probe*.class` = 0，src 无 probe 痕迹）。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；必须含：根因复核结论（是不是我说的那个）、改前/改后伤害数值、粒子参数、以及未验证项。
