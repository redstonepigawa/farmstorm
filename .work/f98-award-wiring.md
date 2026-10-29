# F-98 附加 · 三处 award 接线（进度动态验收的前置）

## 1. 改了哪几个文件
| 文件 | 改动 |
|---|---|
| `entity/thrown/MolotovProjectile.java` | 新增 `onAddedToLevel()` 覆写 + 两个 import（`FarmstormAdvancements`、`ServerPlayer`） |
| `behavior/impl/ZangiefCaneTrap.java` | `trigger()` 里新增 4 行：取 `be.owner(level)`，非空则 `award(deployer, "zangief_hit")`；+1 import |
| `event/WildHunterAdvancementEvents.java` | **新建**：`MobEffectEvent.Added` 监听，判 `ServerPlayer` + `effect == WILD_HUNTER` + `getOldEffectInstance() == null` |
| `datagen/ModAdvancementProvider.java` | 只改 `AWARD_REQUIRED` 的 javadoc（原文写「当前代码里也还没有 award 调用点」，已与事实不符） |

## 2. 三处 award 调用点行号
```
entity/thrown/MolotovProjectile.java:73        FarmstormAdvancements.award(thrower, "first_molotov");
behavior/impl/ZangiefCaneTrap.java:72          FarmstormAdvancements.award(deployer, "zangief_hit");
event/WildHunterAdvancementEvents.java:37      FarmstormAdvancements.award(player, "first_wild_hunter");
```

## 3. 三处的口径与「为什么选这个点」

### ① `first_molotov` —— 投出即判定（不要求命中），授予投掷者
- 位置：`MolotovProjectile#onAddedToLevel()`（实体真正进入世界时判一次，不是每 tick）。
- 为什么不用构造器：`/summon` 走两参构造、没有归属者，天然不会推进；投掷路径 `ThrowableCropItem#createProjectile`
  在 `addFreshEntity` 之前就 `setOwner(player)` 了，所以 `onAddedToLevel` 里 `getOwner()` 已是投掷者。
- 守卫：`!level().isClientSide()` + `getOwner() instanceof ServerPlayer`。
- 只判一次：`onAddedToLevel` 每个实体实例只调一次。

### ② `zangief_hit` —— 授予**陷阱的部署者**，不是被命中者
- 位置：`ZangiefCaneTrap#trigger()`，紧跟 `ZangiefPiledriverController.begin(player)`（amplifier 0 已施加）之后。
- 授予对象：`be.owner(level)`（部署者）；归属者离线或方块是 `/setblock` 放的（无归属）时 `null` ⇒ 跳过。
- 父进度是 `first_deploy`（部署者那棵树）⇒ 「你布的东西打中了人」才是成就，被命中者不推进。
- 创造 / 旁观不触发陷阱（D-71 在 `findTrigger` 里已排除）⇒ 天然不会推进。

### ③ `first_wild_hunter` —— 不限定来源
- 位置：**新建** `event/WildHunterAdvancementEvents`，监听 NeoForge `MobEffectEvent.Added`（已核实 21.1.213 存在：
  `net/neoforged/neoforge/event/entity/living/MobEffectEvent.java:171`）。
- 不用 `FoodCropItem`：那只能覆盖「吃狂野苹果」一个来源，与 DESIGN §9.2 第 3 条「吃苹果 / `/effect` / 其它模组给药都算」冲突。
- `getOldEffectInstance() == null` 才判——它为空表示「之前没有这个效果」，正好是「首次获得」；
  续时 / 刷新（old 非空）不会重复推进（进度本身也会去重，这里只是少一次无谓调用）。
- 守卫：`getEntity() instanceof ServerPlayer`（客户端不 award）。

## 4. 静态验收
```
compileJava            : BUILD SUCCESSFUL（exit 0，仅 1 条 deprecation note，来自既有的 AdvancementHolder 用法）
三处 award             : 各 1 处（行号见 §2）
AWARD_REQUIRED 覆盖     : 11 条 / 11 条都有调用点（本轮补齐最后 3 条）
CustomPacketPayload    : 0 处
@Mixin                 : 0 处
src *Probe*            : 0
build/classes *Probe*  : 0
jar 内 Probe 条目       : 0
```

## 5. 动态验收（等排期）需要跑的
1. 创造模式朝地面丢一瓶莫洛托夫 → toast「火了！」；`/advancement` 里 `farmstorm:first_molotov` 变 granted（**不需要命中**）。
2. 用 `/give` 拿桑吉尔蔗、放下、让另一名玩家踩上去（单人可用第二客户端或 `/summon` 不行——必须是玩家） →
   **部署者**收到 toast「手摇重 360」；被命中者不得该进度。
3. `/effect give @p farmstorm:wild_hunter 5 0` → toast「狂野猎人」（不限定来源这条正好用指令验证）。
4. 反向：`/summon farmstorm:molotov`（无归属者）不应推进 `first_molotov`。
