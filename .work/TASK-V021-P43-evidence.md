# Phase 43 证据 · farmstorm v0.2.1（F-257 ~ F-260）

> 执行者：本会话唯一执行者。工作目录 `D:\project_farm`（pwsh）。构建一律 `& 'D:\project_farm\.tools\gw.cmd' build`。
> **窗口纪律（A-74 / 43 号 §0.3）**：本文件出现的 config 与语言键**总数一律是「区间读数」或上下文读数，不是断言**。config 侧收口在 **F-262**、语言侧在 **F-263**、工具期望面在 **F-261**。本文件只断言：**本条键存在**、`[trap]` 段内 **4** 键、`damageMultiplier = 3.0` 出现 **1** 次。
> 起服目录 `run-p43`（`FS_RUN_LOCK=run-p43`、`-PfsRunDir=run-p43`）；**没有在 `run/` 里起服**，`run/config` 未动。

---

## 0. 基线与「改前实测」（每条判据改前都跑过一遍）

| # | 项 | 改前实测 | 取法 |
|---|---|---|---|
| 1 | `src/main/java` Java 文件数 | **148** | `Get-ChildItem -Recurse src/main/java -Filter *.java -File` |
| 2 | `FarmstormConfig` 行首 `.define*` | **175** | `[IO.File]::ReadAllLines(..., UTF8)` 后 `^\s*\.define` |
| 3 | `b.push(` / `b.pop()` | **60 / 60** | 同上，`b\.push\(` / `b\.pop\(\)` |
| 4 | `damageMultiplier` 命中 | **0** | 同上，`damageMultiplier` |
| 5 | `run/config/farmstorm-server.toml` | 801 行 / 60 段 / 175 键；`[trap]` @731 三个键（735 `immunityTicks` / 739 `dustIntervalTicks` / 743 `maxTrapsPerPlayer`） | `^\s*\[` / `^\s*[A-Za-z0-9_]+\s*=` |
| 6 | 语言键 | zh **337** / en **337**，zh-only **0**、en-only **0**；`farmstorm.configuration.*` **178**；`trap.damageMultiplier` **0** | `ConvertFrom-Json` 键集 |
| 7 | `ModLangProvider` `config(entries,` 次数 | **178** | 同源文件行计数 |
| 8 | 全仓 `Enemy` | **0 命中** | `grep -r '\bEnemy\b' src/main/java` |
| 9 | `MobCategory` 使用行 | **2**（`FarmstormEntityTypes:62` / `:73`，两处都是 `MISC`；另 1 行 import） | 同上 |
| 10 | `isHostile` | **0** | 同上 |
| 11 | `new FarmstormExplosion` | **7** 处 | 同上 |
| 12 | `FarmstormExplosion` 构造器声明 | **1** 个 | `public FarmstormExplosion(` |
| 13 | `FarmstormDamageTypes` 的 `ResourceKey<DamageType>` | **3** | 同源文件 |
| 14 | `ZangiefCaneTrap` 行数 | **183** | UTF-8 行数 |

---

## 1. F-257 · `util/Hostility#isHostile`（新文件）

### 1.1 改了哪些文件

```
?? src/main/java/com/dashpig/farmstorm/util/Hostility.java   （新文件，git status 可见，未被 .gitignore：check-ignore RC=1）
```

四个分支按 42 号 §2.4 的骨架、顺序一致（非 `LivingEntity` → false；玩家 → false；`instanceof Enemy` → true；`getType().getCategory() == MobCategory.MONSTER` → true）。
类注释写明「并集的实际增量只来自两类模组生物」，方法注释写明「玩家那一支是保险，让『玩家一定不乘倍率』这条验收读得出来」。

### 1.2 读数（改后）

| 判据 | 读数 |
|---|---|
| 全仓 `Enemy` | **1 个文件**（`util/Hostility.java`，8 行命中：package 引用 + import + 注释 + 判定） |
| `isHostile` 定义 | **1** 处（`Hostility.java:31`） |
| `isHostile` 使用点 | **2** 处（`FarmstormExplosion.java:198`、`ZangiefCaneTrap.java:115`） |
| `MobCategory.MONSTER` | **1** 处（新增） |
| `MobCategory.MISC` | 仍 **2** 处（`FarmstormEntityTypes:62`、`:73`） |
| Java 文件数 | 148 → **149** |
| `gw.cmd build` | 退出码 **0**（`.work/p43_build2.log`） |

### 1.3 5 处触发面（未改，逐字比对）

| 文件 | 本 Phase 前 | 改后 | 说明 |
|---|---|---|---|
| `BlastCarrotTrap` | :59 | **:59** | 逐字未变 |
| `FangTrapCactusTrap` | :54 | **:54** | 逐字未变 |
| `PumpkinTurret` | :109 | **:109** | 逐字未变 |
| `VibrationBeanTrap` | **:68** | **:68** | 未改；⚠️ 文档写的是 `:59`，那是 0.2.0 的值，**F-254 在 Phase 42 已把它移到 :68**（见偏差表 D-2） |
| `ZangiefCaneTrap` | :174 | **:180** | 内容逐字未变，行号因 F-260 在 :112 之后插入乘点而整体下移（见偏差表 D-1） |

---

## 2. F-258 · `trap.damageMultiplier` 键与翻译（**开窗**）

### 2.1 改了哪些文件（4 个文件、4 处）

```
M src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java   +5
M src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java  +3
M src/generated/resources/assets/farmstorm/lang/zh_cn.json          +1
M src/generated/resources/assets/farmstorm/lang/en_us.json          +1
```

| 处 | 行号（改后） | 内容 |
|---|---|---|
| 字段声明 | `FarmstormConfig.java:226-227` | `public static ModConfigSpec.DoubleValue trapDamageMultiplier;`（`trapMaxTrapsPerPlayer` 之后） |
| `[trap]` 段定义 | `FarmstormConfig.java:929-931` | `b.comment(...).translation(tr("trap.damageMultiplier")).defineInRange("damageMultiplier", 3.0, 1.0, 10.0)`（`:924-926` 的 `maxTrapsPerPlayer` 之后、`:933` 的 `b.pop()` 之前） |
| 语言（中英同一行） | `ModLangProvider.java:414-416` | `config(entries, "trap.damageMultiplier", 中文, 英文)` |
| 两份 lang | `zh_cn.json:243` / `en_us.json:243` | `farmstorm.configuration.trap.damageMultiplier`（按字母序插在 `trap.dustIntervalTicks` 之前，与生成器顺序一致） |

文案照 42 号 §4.3 两行原文（中文：「陷阱对敌对生物的伤害倍率（1.0 = 不改；只作用于 blast_carrot 与 zangief_cane）」；英文：「Damage multiplier of traps against hostile mobs (1.0 = unchanged; blast_carrot and zangief_cane only)」）。
**本任务没有改** `blast_carrot.maxDamage`（仍 7）、`zangief_cane.slamDamage`（仍 7.5）、`ModLangProvider.java:13-16` 的类注释（一字未动）。

### 2.2 开窗第一条：本条键存在（运行期，重生成后的 toml）

起服一次（`run-p43`）后，服务端按新 SPEC 重写了配置文件：

```text
run-p43/config/farmstorm-server.toml：805 行 / 60 段 / 176 键（区间读数；旧值 175）
BOM first3 = 91,99,111（"[" "c" "o"）⇒ UTF-8 无 BOM
[trap] @731
   735: immunityTicks = 100
   739: dustIntervalTicks = 100
   743: maxTrapsPerPlayer = 16
   747: damageMultiplier = 3.0        ← 段内 4 键，本键出现 1 次
全文 `damageMultiplier` 命中 = 1
```

源码侧：`FarmstormConfig` 行首 `.define*` 里 `("damageMultiplier"` 命中 **1**。
启动自检（同一进程）：`自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`。

### 2.3 开窗第二条：组数不变、全文只写区间

`b.push(` = **60**、`b.pop()` = **60**（`trap` 是既有组，只加键不建组）；行首 `.define*` = 176（**区间读数**：旧代码 175 → 新代码 176）。
**未写 config 总数结论**（收口在 F-262）。

### 2.4 翻译与键集

| 项 | 读数 |
|---|---|
| `farmstorm.configuration.trap.damageMultiplier` 在 zh_cn / en_us | 各 **1** 次 |
| zh-only / en-only | **0 / 0** |
| `farmstorm.configuration.*`（上下文读数） | 178 → 179 |
| `config(entries,` 次数（上下文读数） | 178 → 179 |
| `ModLangProvider.java:13-16` 类注释 | `git diff` 0 行 |

（语言键总数 **不写**：收口在 F-263。）

### 2.5 读数的路径偏差

任务书要求「起服用 `run-p43`，不在 `run/` 里起服」⇒ 本 Phase 重生成的是 `run-p43/config/farmstorm-server.toml`。
盘上 `run/config/farmstorm-server.toml` **仍是旧值**（本次收尾读数：801 行 / 60 段 / 175 键 / `damageMultiplier` 命中 0），**本 Phase 没有复制或改写它**；F-262 按 42 号 §4.2 校验② 自己跑一次 `runServer` 重生成。两个文件是同一份 SPEC 生成的，读数一致。

---

## 3. F-259 · `FarmstormExplosion` 第 9 参 `trapSourced` + 乘点①（**同一提交**，A-72）

### 3.1 改了哪些文件（2 个）

```
M src/main/java/com/dashpig/farmstorm/explosion/FarmstormExplosion.java  +24 -2
M src/main/java/com/dashpig/farmstorm/behavior/impl/BlastCarrotTrap.java +1 -1
```

| 处 | 行号（改后） | 内容 |
|---|---|---|
| 字段 | `:63-64` | `private final boolean trapSourced;` |
| 8 参重载 | `:66-73` | 方法体**只有一行** `this(level, sourceEntity, damageSource, x, y, z, radius, rule, damageCap, false);` |
| 9 参构造器 | `:75-94` | `damageCap` 之后加 `boolean trapSourced`，其余 super(...) 与四个赋值照旧 + `this.trapSourced = trapSourced;` |
| 乘点① | `:193-201` | `float dealt = (float) Math.min(raw, this.damageCap);` → `if (this.trapSourced && Hostility.isHostile(entity)) { dealt *= FarmstormConfig.trapDamageMultiplier.get().floatValue(); }` → `entity.hurt(this.damageSource, dealt);` |
| 唯一传第 9 参的调用点 | `BlastCarrotTrap.java:84-86` | `... BlastRule.forCrop(CropProfile.BLAST_CARROT, BlastRule.VANILLA), damageCap, true);` |

**A-72 落实**：第 9 参与乘点在同一次改动里完成（本次工作区改动本身就是一次提交的粒度；没有出现「参数加了没人乘」或「乘点先到、参数后到」的中间态）。
**没做**：不新增伤害源（`damage_type` 仍 **3** 个 `ResourceKey`）、不按 `BlastRule` 反查作物、不按 `damageSource.getEntity()` 判、`:160` 的 `ignoreExplosion` 与 `:144-147` 的旧口径未动。

### 3.2 结构读数

| 判据 | 读数 |
|---|---|
| `new FarmstormExplosion` | 仍 **7** 处（`BlastCarrotTrap:84` / `MelonGrenadeProjectile:42` / `MelonSliceProjectile:40` / `MolotovProjectile:94` / `ToxicUbiServerProjectile:43` / `UbiServerProjectile:41` / `LuckyBerryRoller:212`） |
| 传第 9 参的调用点 | **1** 处（`damageCap, true)` 全仓命中 1） |
| 构造器声明 | **2** 个（8 参 + 9 参） |
| 6 处非陷阱实参表 | `git diff` 对那 6 个文件 **0 行**（`git diff -- <6 个路径> \| Measure-Object -Line` = 0） |
| `FarmstormDamageTypes` `ResourceKey<DamageType>` | 仍 **3** |

### 3.3 运行期读数（胡罗贝，倍率 3.0 默认）

场景：`fill 0 100 0 24 100 40 stone` 修平台 → `summon minecraft:zombie 8.5 101 8.5`（`NoAI:1b`、`max_health=200`、**`generic.armor=0`**、`Health:200f`）→ `setblock 9 101 8 farmstorm:blast_carrot` → 读 `Health`。

```text
Zombie has the following entity data: 200.0f   （炸前）
Zombie has the following entity data: 179.0f   （炸后）          → 21.0
```

对照（同一爆炸的**另一个靶型**，玩家 ALICE，`difficulty normal`，`power` 探针 8 时两次落在封顶段）：

```text
ALICE has the following entity data: 20.0f
ALICE has the following entity data: 13.0f     → 7.0            （= min(raw,7) × 1.0）
```

- **21.0 = min(raw, 7) × 3**：僵尸被 `Hostility.isHostile` 判为 true 后才乘；玩家恒 7.0（= cap），**从不随倍率放大**（倍率 10.0 时玩家仍 5.5，若被乘应是 55）。
- 僵尸读数要在 `generic.armor = 0` 下取：`Zombie` 自带 `ARMOR = 2`，会把 21.0 削到 20.664（1.6 %）、22.5 削到 22.14——**这不是倍率问题**，是原版护甲吸收。

---

## 4. F-260 · `ZangiefCaneTrap#monsterBranch` 乘点② + 日志打乘后值

### 4.1 改了哪些文件（1 个）

```
M src/main/java/com/dashpig/farmstorm/behavior/impl/ZangiefCaneTrap.java  +6
```

| 处 | 行号（改后） | 内容 |
|---|---|---|
| import | `:12` | `import com.dashpig.farmstorm.util.Hostility;` |
| 乘点② | `:112-117` | `double amount = ...slamDamage.get();` 之后、`source == null` / `amount > 0.0` 两个判定**之前**：`if (Hostility.isHostile(target)) { amount *= FarmstormConfig.trapDamageMultiplier.get(); }` |

**日志行（`:132-134`）一行未改**：它打的就是 `amount` 这个变量 ⇒ 乘后值自动生效（格式串 `桑吉尔蔗 生物结算 t=%d 生物 %s 中心距 %.2f 伤害 %.1f 击退 %.1f（不进相位链）` 逐字未变）。

### 4.2 运行期读数（日志原文，三态倍率）

```text
[02:47:34] 桑吉尔蔗 生物结算 t=1338 生物 Zombie 中心距 1.12 伤害 22.5 击退 0.4（不进相位链）    倍率 3.0
[02:50:06] 桑吉尔蔗 生物结算 t=4368 生物 Zombie 中心距 1.12 伤害 22.5 击退 0.4（不进相位链）    倍率 3.0
[02:52:25] 桑吉尔蔗 生物结算 t=7158 生物 Zombie 中心距 1.12 伤害 7.5  击退 0.4（不进相位链）    倍率 1.0
[02:53:42] 桑吉尔蔗 生物结算 t=8698 生物 Zombie 中心距 1.12 伤害 7.5  击退 0.4（不进相位链）    倍率 1.0
[02:56:55] 桑吉尔蔗 生物结算 t=12548 生物 Zombie 中心距 1.12 伤害 75.0 击退 0.4（不进相位链）    倍率 10.0
[02:57:43] 桑吉尔蔗 生物结算 t=13518 生物 Zombie 中心距 1.12 伤害 22.5 击退 0.4（不进相位链）    倍率 3.0
[03:02:14] 桑吉尔蔗 生物结算 t=18928 生物 Zombie 中心距 1.12 伤害 0.0  击退 0.4（不进相位链）    slamDamage=0
```

对应 `Health` 读数（`generic.armor=0`）：倍率 3.0 ⇒ 200.0 → **177.5**（−22.5，**不是 11.25**，A-76）；1.0 ⇒ 200.0 → **192.5**（−7.5）；10.0 ⇒ 200.0 → **125.0**（−75.0）。

### 4.3 `slamDamage = 0` 与玩家分支

| 场景 | 读数 |
|---|---|
| `zangief_cane.slamDamage = 0` | `Health` 200.0 → **200.0**（无 `hurt`）；日志 `伤害 0.0`（乘完仍是 0） |
| 玩家踩到（`tp ALICE 8.5 101 4.5`，陷阱 `9 101 4`） | 走相位链：`桑吉尔蔗 相位 0 → 相位 1 → 相位 2`，`桑吉尔蔗 落地耗时 22 刻（自然坠落）`，`落地结算 玩家 ALICE 固定伤害 7.50 点 HP 20.00 → 12.50 归属 无（已生效）` ⇒ **7.5 未被乘**（该次运行倍率为 1.0；倍率 10.0 的对照由 `blast_carrot` 的玩家读数给出） |

---

## 5. 倍率三态读数总表（同一几何、同一代码、只改 `trap.damageMultiplier`）

僵尸：`NoAI:1b`、`max_health 200`、`armor 0`、`Health:200f`；每项前 `fill` 修平台（否则前一次爆炸留下的坑会让下一发的落点漂移 —— 实测过一次，见偏差表 D-6）。

| 靶 / 爆炸 | 倍率 1.0 | 倍率 3.0（默认） | 倍率 10.0 | 结论 |
|---|---|---|---|---|
| 胡罗贝 → 僵尸（`blast_carrot`，trapSourced=true，敌对） | **7.0** | **21.0** | **70.0** | `min(raw,7) × mult` |
| 桑吉尔蔗 → 僵尸（`zangief_cane`，乘点②） | **7.5** | **22.5** | **75.0** | `slamDamage × mult` |
| `melon_slice_bomb` → 僵尸（非陷阱，cap 6） | **6.0** | **6.0** | **6.0** | **三次完全相同** |
| `ubi_server` → 僵尸（非陷阱，cap 2） | **2.0** | **2.0** | **2.0** | **三次完全相同** |
| `molotov` 落点 → 僵尸（非陷阱，`damageCap = 0.0F`） | **0.0** | **0.0** | **0.0** | **三次完全相同** |
| `melon_grenade` 父爆炸 → 僵尸（非陷阱，cap 8） | **8.0** | **8.0** | **8.0** | **三次完全相同** |
| 胡罗贝 → 玩家（非敌对） | 5.0 | 5.5 | 7.0 | 5.5 | **7.0** | 5.5 | 上限恒 7.0，**从不 ×mult** |

**判决性读数**：6 处非陷阱里的 4 处（`melon_slice_bomb` / `ubi_server` / `molotov` / `melon_grenade`）在 1.0 与 10.0 下**逐点相同**（6.0 / 2.0 / 0.0 / 8.0），而陷阱两处在同一对照下从 7.0 → 70.0、7.5 → 75.0。
另外两处非陷阱（`MelonGrenadeProjectile` 已覆盖、`ToxicUbiServerProjectile` / `LuckyBerryRoller` 未单独跑）见未验证清单 U-3。

**玩家读数为什么不总是 7.0**：`min(raw, cap)` 里玩家的 `raw` 恰好落在 cap 7 的边界（服务端与客户端位置有亚格抖动，`getSeenPercent` 的采样点会翻边），实测在 **5.0 ~ 7.0** 之间跳；**上限恒等于 cap 7.0，且 10.0 时仍是 5.5**（若被乘应是 55）。僵尸用 `NoAI` 固定位置，读数完全稳定。

---

## 6. 文档-源码偏差表

| # | 文档写的 | 实测 / 本 Phase 做法 | 处理 |
|---|---|---|---|
| D-1 | 43 号 F-260 验收 5 / 45 号 §4.4：「`ZangiefCaneTrap:174` 的行号逐字未变；该类总行数仍 **183**」 | 该类现 **189** 行，`getEntitiesOfClass(Monster.class, …)` 在 **:180**（内容逐字未变） | 与 42 号 §2.5.3「乘点插在 `:111-118` 两个判定之前」**不可同时成立**（只加 import 也已经 ≥ 184 行）。按 42 号执行，登记不改上游（44 号 §6 ②） |
| D-2 | 45 号 §4.1 / §4.4：`VibrationBeanTrap:59` 的 `Monster` 过滤「逐字未变」 | 开工前该行已在 **:68**（F-254 在 Phase 42 把它下移），本 Phase 未改该文件 | 记为「文档行号是 0.2.0 基线」，判据改用**内容逐字 + 现状行号** |
| D-3 | 42 号 §2.5.3 的乘点①示例用 `(float) FarmstormConfig.trapDamageMultiplier.get()` | 直接这样写**编译失败**：`Double cannot be converted to float`（`error: incompatible types`） | 改成 `.get().floatValue()`（语义相同；`.work/p43_build1.log` 是失败证据、`p43_build2.log` 是 exit 0） |
| D-4 | 42 号把凉 `FarmstormExplosion` 的行号写作 `:69-78` / `:178-179` | 改后：字段 `:63-64`、8 参 `:66-73`、9 参 `:75-94`、乘点 `:193-201` | 行号漂移，属预期（插入新重载） |
| D-5 | 45 号 §4.4：「`run/config/farmstorm-server.toml`（先跑一次 `runServer` 重生成）」 | 本 Phase 按任务书在 `run-p43` 起服 ⇒ 重生成的是 `run-p43/config/farmstorm-server.toml`（805 行 / 60 段 / 176 键 / `[trap]` 4 键）；`run/config` 仍 175 键 | 读数取 `run-p43/config`；F-262 自己重生成 `run/config` |
| D-6 | 42 号 §7 #20 / 45 号 §4.4：「`melon_grenade` / `molotov` 在 1.0 与 10.0 下读数相同」 | `molotov` 的落点爆炸 `damageCap = 0.0F`（`MolotovProjectile:95`）⇒ 直接伤害恒 **0.0**，它的伤害只来自火；`melon_grenade` 有 4 枚子母弹，总伤害会随机 | `molotov` 记为 0.0 三次相同；`melon_grenade` 只取父爆炸（8.0 三次相同，子母弹本次未命中该僵尸）；另用 `melon_slice_bomb` / `ubi_server` 给出「非陷阱不随倍率变」的数值读数 |
| D-7 | 44 号事实卡：「`MobCategory` 命中 2 → 3」 | 全仓 `MobCategory` **行**命中：import 1 + 使用 2 → import 1 + 使用 3（`MISC` 2 + `MONSTER` 1） | 使用行口径 2 → 3 成立；按文件行计要算 import 那一行（第 3 行是导入，不是使用行） |
| D-8 | — | `trap.immunityTicks = 100`（5 秒）是**按实体**的陷阱免疫：同一玩家 5 秒内踩第二颗陷阱**不触发** | 本 Phase 的玩家读数之间一律留 ≥ 6 s（早前两次「玩家 0 伤害」就是这个免疫，不是代码缺陷） |
| D-9 | — | `Zombie` 自带 `generic.armor = 2` ⇒ 原始读数被削 1.6 %（21.0 → 20.664、22.5 → 22.14） | 探针里显式 `attributes:[{id:"minecraft:generic.armor",base:0.0d}]`，否则会把「护甲吸收」误读成「倍率错」 |

---

## 7. 未验证清单

| # | 未验证的东西 | 原因 |
|---|---|---|
| U-1 | 「桑吉尔蔗对**牛 / 村民**（非敌对）= 7.5」 | **该路径不可达**：`findTrigger` 只返回 `ServerPlayer` 与 `Monster` 实例，而 `Monster` 全部实现 `Enemy` ⇒ 非敌对生物永远不会成为 `monsterBranch` 的 target。给了代码层证据（`Hostility.isHostile` 四分支 + 判定位置），F-260 验收 2 建议改成「代码层：非敌对不乘」 |
| U-2 | 「玩家读数恒为 7.0」 | 玩家 `raw` 正好压在 cap 边界，实测 5.0 ~ 7.0（见 §5 末段）。**上限是 7.0**、且 10.0 下仍是 5.5（没有 ×mult） |
| U-3 | `ToxicUbiServerProjectile` / `LuckyBerryRoller` 两个非陷阱调用点的**数值**读数 | 一个要剧毒云、一个要「吃幸运浆果」，本环境无真人客户端可操作；给了代码层证据：两处都是 8 参重载 ⇒ `trapSourced = false`，且 `git diff` 对 6 处调用文件 0 行 |
| U-4 | 客户端渲染 / tooltip 面上的任何东西 | 本环境无游戏客户端（只有专用服务端 + RCON + mineflayer 协议机器人）；本 Phase 无渲染改动 |
| U-5 | `gw.cmd runServer` 收尾的退出码 | 日志结尾是 `BUILD SUCCESSFUL in 17m 8s`，但后台作业报 `exit code: 1`；停在 `/stop` 之后，成因未核（服务端日志没有异常栈） |

---

## 8. 未关闭的问题

1. **ZangiefCaneTrap 的 183 行判据**（D-1）需要上游裁决：要么承认行数会变、把判据改成「`getEntitiesOfClass(Monster.class, …)` 的内容逐字未变」，要么把乘点写成单行三元表达式（本次没有这么做：可读性差且与 42 号示例不符）。
2. **玩家读数的抖动**（U-2）：若验收要求「恒 7.0」，建议用 `blast_carrot.explosionPower` 探针（本次 8.0 时两次都落在 7.0）或给出「玩家 raw 落在 cap 边界」的说明；不建议为此改代码。
3. **`run/config/farmstorm-server.toml` 仍是旧值**（175 键）：F-262 必须先起一次 `runServer` 重生成，再取 176 / 60 的读数。
4. **`molotov` 的「伤害读数」**（D-6）：它的落点爆炸 cap 是 0，火才是伤害来源；42 号 §7 #20 把它和 `melon_grenade` 并列为「非陷阱伤害读数」，这条口径本身需要澄清。

---

## 9. 命令与证据文件清单

```powershell
# 构建
& 'D:\project_farm\.tools\gw.cmd' build          # → .work/p43_build1.log（失败）/.work/p43_build2.log（EXIT=0）

# 起服（run-p43，rcon 25643 / p43rcon）
$env:FS_RUN_LOCK='run-p43'; & 'D:\project_farm\.tools\gw.cmd' runServer -PfsRunDir=run-p43   # → .work/p43_server1.log

# 运行期读数（RCON 驱动 + 命令文件）
& '<bundled python>' .work\p43_rcon.py .work\p43_t_J.txt 0.35     # 批次 J（1.0 / 3.0 / 10.0 各跑一次）
& '<bundled python>' .work\p43_rcon.py .work\p43_t_M.txt 0.35     # 批次 M（power 探针下两个靶型都落在封顶段）
& '<bundled python>' .work\p43_rcon.py .work\p43_t_S.txt 0.4      # slamDamage = 0
& '<bundled python>' .work\p43_rcon.py .work\p43_t_T.txt 0.5      # 玩家踩桑吉尔蔗（相位链）
```

| 证据文件 | 内容 |
|---|---|
| `.work/p43_build1.log` / `p43_build2.log` | 编译失败（D-3）/ 构建 exit 0 |
| `.work/p43_server1.log` | 起服日志：注册自检 97、`桑吉尔蔗 生物结算` 全部 7 行、`莫洛托夫 落点`、`落地结算` |
| `.work/p43_t_*.txt` / `.work/p43_*_out.txt` | 每个批次的命令原文与原始输出（含每一条 `Zombie/ALICE has the following entity data`） |
| `.work/p43_rcon.py` / `.work/p43_bot.js` | RCON 驱动 / mineflayer 协议机器人（ALICE，offline-mode） |

**收尾状态**：服务端 `/stop` 后 `ThreadedAnvilChunkStorage: All dimensions are saved`；`.tools/runserver-run-p43.lock` 无残留、`java` 进程 0 个；`run-p43/config/farmstorm-server.toml` 已恢复默认（`damageMultiplier = 3.0`、`slamDamage = 7.5`、五个 `explosionPower` 各自默认、`[trap]` 段 4 键、UTF-8 无 BOM）。
