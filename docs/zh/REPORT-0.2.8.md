# v0.2.6 → v0.2.8 变更汇报（1.20.1 分支 → 1.21.1 主线）

> 写于 2026-11-08。承接 `docs/zh/REPORT-0.2.5.md`（那份只讲 v0.2.5，已冻结不再追加）。
> 本文覆盖 v0.2.6 / v0.2.7 / v0.2.8 三次发布，重点是**哪些需要 1.21.1 侧跟进**。

## 0. 版本与产物

| 项 | 值 |
|---|---|
| 分支 | `1.20.1` |
| 版本 | 0.2.5 → **0.2.6** → **0.2.7** → **0.2.8** |
| 产物 | `farmstorm-0.2.8+mc1.20.1.jar` — 1,143,539 B |
| SHA256 | `976EABE5E849B6C8E83A55C885438026061D7B21AE436EF4E082D9A7407E6F80` |
| 加载器 | Forge 47.x **与** NeoForge 47.x 共用同一个 jar |

### 追踪总表：1.21.1 要不要跟进

| 版本 | 改动 | 1.21.1 |
|---|---|---|
| 0.2.6 | 末影龙剪影崩溃（`ClassCastException`） | **要修**（主线同一行同样的强转） |
| 0.2.7 | 伤害来源 `IllegalAccessError` | **不用**（仅双加载器场景） |
| 0.2.8 | 莫洛托夫 `reach=0` NaN 卡死生物 | **不用**（主线已有守卫） |
| 0.2.8 | `sweep` 只扫主世界导致下界/末地永久 `NoAI` | **建议自查** |
| 0.2.8 | 灰度 `loadEffect` 在光影整合包里活不过一帧 | **建议自查** |
| 0.2.8 | 火瓶威力默认 0 → 1 | 数值，作者决定 |
| 0.2.8 | 炮台类作物取消放置数量上限 | **需要同步** |

---
## 1. 末影龙剪影崩溃（**1.21.1 主线同样存在**）

### 现象（真机游玩崩溃）

```
Description: Unexpected error
java.lang.ClassCastException: class net.minecraft.client.renderer.entity.EnderDragonRenderer
  cannot be cast to class net.minecraft.client.renderer.entity.LivingEntityRenderer
    at com.dashpig.farmstorm.client.WildHunterOutline.onRenderGuiPre(WildHunterOutline.java:126)
```

触发条件：吃**狂野苹果** → 获得狂野猎人效果 → 红色剪影开始渲染 → 附近存在**末影龙**时崩溃。

### 根因

剪影的循环里直接强转：

```java
LivingEntityRenderer renderer =
        (LivingEntityRenderer) minecraft.getEntityRenderDispatcher().getRenderer(entity);
```

**并非所有 `LivingEntity` 的渲染器都是 `LivingEntityRenderer`**。末影龙就是反例：`EnderDragonRenderer` 直接继承 `EntityRenderer`。
对象本身仍是 `LivingEntity`（所以能进候选集），但强转必然失败。

### 修法（v0.2.6）

```java
if (!(minecraft.getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer<?, ?> living)) {
    continue;   // 末影龙等非 LivingEntityRenderer 的目标跳过，不画剪影
}
((LivingEntityRenderer) living).render(entity, yaw, capturedPartialTick, poseStack, shell, LightTexture.FULL_BRIGHT);
```

### ⚠️ 1.21.1 侧需要同样修

我核对过 1.21.1 主线 `src/main/java/com/dashpig/farmstorm/client/WildHunterOutline.java` 第 113~114 行，**是同一行同样的强转，同样会崩**。建议按上面的写法加 `instanceof` 守卫。

同类候选（渲染器不是 `LivingEntityRenderer` 的 LivingEntity）：末影龙；若以后接了别的 mod 的非标准生物渲染器也可能踩到——用 `instanceof` 守卫一次覆盖。

### 验收

在末影龙附近吃狂野苹果（或直接 `/effect give` 给狂野猎人），剪影正常显示其他生物、不崩溃、末影龙不画剪影。

---

## 2. v0.2.6 变更清单

| # | 改动 | 类型 |
|---|---|---|
| 1 | `WildHunterOutline` 剪影渲染加 `instanceof LivingEntityRenderer` 守卫 | 崩溃修复（**两端共有**） |
| 2 | `mod_version` 0.2.5 → 0.2.6 | 版本 |

产物：`farmstorm-0.2.6+mc1.20.1.jar` — 1,140,861 B — SHA256 `B395C8524A74F0CA59C4AB0E1EAA6BA6237DDA4D6A6E55D9ECBF0401DC6A7FE6`

---

## 3. 伤害来源的访问权限崩溃（**仅 1.20.1 双加载器场景**）

### 现象（用户反馈，胡萝卜陷阱引爆时崩溃）

```
Description: Ticking block entity
java.lang.IllegalAccessError: class com.dashpig.farmstorm.registry.FarmstormDamageTypes
  tried to access private method
  'DamageSource DamageSources.m_269298_(ResourceKey, Entity)'
    at com.dashpig.farmstorm.registry.FarmstormDamageTypes.cropBlast(FarmstormDamageTypes.java:37)
    at com.dashpig.farmstorm.behavior.impl.BlastCarrotTrap.trigger(BlastCarrotTrap.java:84)
```

### 根因：访问转换器（AT）差异

代码调的是 `level.damageSources().source(KEY, owner)`。这个**两参重载在原版里不是 public**，靠 access transformer 放宽。

我把两个加载器的 `META-INF/accesstransformer.cfg` 做了逐行差分，**只有 7 条差异，全部在 `world.damagesource`**：

| 成员 | NeoForge 47.1.106 | Forge 47.4.28 |
|---|---|---|
| `DamageSources.m_269079_(ResourceKey)` | 放宽为 public | **没有** |
| `DamageSources.m_269298_(ResourceKey, Entity)` | 放宽为 public | **没有** |
| `DamageSources.m_268998_(ResourceKey, Entity, Entity)` | 放宽为 public | **没有** |
| `DamageSources.f_268645_` | 放宽为 public | **没有** |
| `CombatTracker.f_268553_ / m_19298_ / m_289215_` | 放宽为 public | **没有** |

所以这段代码在 NeoForge 上编译通过、运行正常，在 Forge 上一跑就 `IllegalAccessError`。

### 修法（v0.2.7）

绕开 AT，直接用在 **vanilla 里本就是 public** 的 `DamageSource` 构造器（语义完全相同——原重载的实现就是这一行）：

```java
private static DamageSource build(Level level, ResourceKey<DamageType> key, @Nullable Entity owner) {
    Holder<DamageType> type = level.registryAccess()
            .registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(key);
    return new DamageSource(type, owner);
}
```

三个伤害类型（`void_bolt` / `crop_blast` / `zangief_slam`）统一走这个 `build()`。

### 同类风险已被扫描干净

7 条 AT 差异里有 3 条属于 `CombatTracker`，项目里 **0 处引用**；剩余 `player.damageSources().playerAttack(player)` 不在差异表内（两边都公开）。**全项目再无依赖 NeoForge 放宽的调用。**

### 1.21.1 侧：不需要改

主线 `FarmstormDamageTypes` 第 32 / 37 / 50 行同样是 `damageSources().source(KEY, owner)`，但 1.21.1 只有 NeoForge 一个加载器，AT 由 NeoForge 提供 → 正常工作。
这条**只是双加载器场景**（Forge 47 + NeoForge 47 共用一个 jar）才会踩到。

---

## 4. v0.2.7 变更清单

| # | 改动 | 类型 |
|---|---|---|
| 1 | `FarmstormDamageTypes` 改用 `new DamageSource(Holder, Entity)` | 崩溃修复（**1.20.1 双加载器独有**） |
| 2 | `mod_version` 0.2.6 → 0.2.7 | 版本 |

产物：`farmstorm-0.2.7+mc1.20.1.jar` — 1,141,065 B — SHA256 `B64EAE95797807B6E3E220204EE36538953A579E1E347BB940A66F578FC140E2`

---

## 5. v0.2.8 汇总（四项）

| # | 改动 | 1.21.1 要不要跟进 |
|---|---|---|
| 1 | 莫洛托夫落点爆炸 `reach <= 0` 时跳过击退（NaN 卡死生物） | **不用**——主线 `FarmstormExplosion` 第 171 行本来就有 `if (reach <= 0.0)` 守卫，这是 1.20.1 移植时重写击退逻辑丢掉的 |
| 2 | `sweep` 改为遍历全部维度 + `EntityJoinLevelEvent` 兜底 | **建议自查**：若主线 `WildHunterAura.sweep` 也只传主世界，下界/末地会永久 `NoAI`（AI 全停且不受重力） |
| 3 | 灰度改为自持 `PostChain`（`AFTER_LEVEL` 直接 `process()`） | **建议自查**：主线的 `GrayscalePass` 仍走 `GameRenderer#loadEffect`，在装有光影模组（Oculus/Iris）的整合包里可能同样活不过一帧 |
| 4 | `molotov.triggerExplosionPower` 默认 0 → 1 | 玩法数值，作者决定；方块破坏恒 `NEVER_BREAK`，与威力无关 |
| 5 | 炮台类作物取消放置数量上限（原单玩家 32 / 单区块 8） | **需要同步**，否则两版玩法不一致；冷却 5 刻保留 |

### 第 1 项的证据（供对照）

```
double distance = Math.sqrt(entity.distanceToSqr(center)) / (double) reach;
if (distance > 1.0) continue;          // NaN > 1.0 为 false → 拦不住
```

`reach = radius * 2`，而莫洛托夫原本传 `power = 0`：

- 直接命中 → `0/0 = NaN` → `impact/dampened/knockback` 全部 NaN → `setDeltaMovement` 写入 NaN → 实体速度永久损坏
- 落在脚边 → `正数/0 = Infinity` → 被 `> 1.0` 拦下 → 无事发生（所以「只有直接命中才出问题」）

### 第 3 项的证据（实机日志，某整合包）

```
16:19:41.229  灰度 PostChain 加载 shaders/post/grayscale.json
16:19:41.280  灰度 PostChain 被外部关闭
…  整场 714 条，每 ~100ms 重装一次，画面从未灰过一帧
```

### 第 2 项的症状判别（给排查用）

`NoAI` 的准确语义（1.20.1 源码）：`Mob.isEffectiveAi()` = `super && !isNoAi()`，为假时 `LivingEntity.aiStep` 跳过 `serverAiStep()`（感知 / 目标选择 / 寻路 / 移动控制全停），且 `travel()` 不施加重力。
但**着火、受伤、被击退、状态效果照常**。
判别命令：`/data get entity @e[type=!player,limit=1,sort=nearest] NoAI` —— 返回 `1b` 才是 AI 被剥夺；`0b` / 无此标签说明是别的机制（例如被方块卡住）。

产物：`farmstorm-0.2.8+mc1.20.1.jar` — 1,143,539 B — SHA256 `976EABE5E849B6C8E83A55C885438026061D7B21AE436EF4E082D9A7407E6F80`
