# F-77 证据 —— 狂野苹果食用与授予效果（Lane A / Phase 13）

- 结论：**已实现并通过三条判据**。改动 **2 个文件**（可做路径限定提交）：
  1. `src/main/java/com/dashpig/farmstorm/registry/FarmstormItems.java` —— `WILD_APPLE` 接 `foodProperties(...)`
  2. `src/main/java/com/dashpig/farmstorm/item/FoodCropItem.java` —— `onEaten` 增 `WILD_APPLE` 分支 + `applyWildHunter`
- 环境：`run-p13`（server-port 25631 / rcon 25641），`FS_RUN_LOCK=run-p13`。
- 构建：`gw.cmd compileJava` 与 `gw.cmd build` 均 **BUILD SUCCESSFUL**（日志 `.work/f77-compile.log` / `.work/f77-build.log`）。

## 0. 改动细节（供审查，逐段）

`FarmstormItems.java`
```java
/** D-54 / F-77：营养与饱和度取 {@code wild_apple.nutrition} 4 / {@code wild_apple.saturationModifier} 1.2（金苹果同值）。 */
public static final DeferredItem<FoodCropItem> WILD_APPLE = cropItem(CropProfile.WILD_APPLE,
        properties -> new FoodCropItem(CropProfile.WILD_APPLE, properties),
        foodProperties(CropProfile.WILD_APPLE,
                FarmstormConfig.wildAppleNutrition, FarmstormConfig.wildAppleSaturationModifier));
```
→ 复用既有 `foodProperties()`（`nutrition(FarmstormConfig.registrationInt(..))` + `saturationModifier((float) registrationDouble(..))` + `alwaysEdible()`），
与甜菜！/ 幸运浆果同一条路径，没有新增键、没有新增类。

`FoodCropItem.java`
```java
if (profile == CropProfile.WILD_APPLE) {
    applyWildHunter(player);
}
...
/**
 * A-02：狂野苹果只负责施加 {@code farmstorm:wild_hunter}，**时长不自己存键**，
 * 一律读 {@code wild_hunter.durationTicks}（默认 700 刻）；amplifier 0。
 */
private static void applyWildHunter(ServerPlayer player) {
    int duration = FarmstormConfig.wildHunterDurationTicks.get();
    if (duration <= 0) {
        return;
    }
    player.addEffect(new MobEffectInstance(FarmstormMobEffects.WILD_HUNTER, duration, 0));
}
```
→ **没有**新增 `wild_apple.durationTicks` 之类的键（A-02）。

## 1. 判据1：吃一颗后 `farmstorm:wild_hunter` 剩余时长 700 ± 2 刻、amplifier 0 —— 通过

探针日志（`run-p13/logs/latest.log`，18:09:12）：

```text
[F77PROBE] reset foodLevel=6 saturation=0.0 effects=[]
[F77PROBE] after_wild_apple foodLevel=10 saturation=9.6 effects=[farmstorm:wild_hunter dur=700 amp=0]
```

`dur=700`（判定区间 698–702）、`amp=0`。

## 2. 判据2：hunger +4 —— 通过；Saturation 读数 9.6（文档写 4.8，见 §5）

同一行：`foodLevel 6 → 10`（**+4**，与 `wild_apple.nutrition` 默认 4 一致）。

## 3. 判据3：金苹果不授予 `wild_hunter` —— 通过；反向配方 = 1 个金苹果

```text
[F77PROBE] after_golden_apple foodLevel=10 saturation=9.6 effects=[minecraft:regeneration dur=100 amp=1, minecraft:absorption dur=2400 amp=0]
```
→ 金苹果走完同一条 `finishUsingItem` 路径，effects 里**没有** `farmstorm:wild_hunter`（D-54 双向配方不会互相污染）。

反向配方（`src/generated/resources/data/farmstorm/recipe/wild_apple.json`，F-73 产出、本任务未改）：
```json
{ "type": "farmstorm:converting",
  "input": { "item": "minecraft:golden_apple" },
  "result": { "count": 1, "id": "farmstorm:wild_apple" },
  "reverse_xp_cost": 3 }
```
反向视图由 `recipe/ConverterRecipe#reverse()`（`ConverterRecipe.java:101-111`）在运行时镜像：
`Ingredient.of(this.result.copyWithCount(1))` 作输入、`sourceItems[0].copyWithCount(1)` 作产出 → **狂野苹果 ×1 → 金苹果 ×1**。
（转换器管线的运行时行为在 F-71/F-73/F-75 已验收，本任务不重复跑；此处给的是该条配方的定义与镜像语义。）

## 4. 验证手段与替代说明（为什么不是客户端右键吃）

- 本机 OS 级输入注入不可用：客户端窗口拿不到前台（`SetForegroundWindow`/`SwitchToThisWindow`/`AppActivate` 均无效，
  `GetForegroundWindow()` 始终不是客户端），`keybd_event`/`mouse_event` 只会打到别的前台窗口；
  `PostMessage` 路径需要窗口处于 in-game 而非暂停屏，无法盲证。
- 两次客户端进程都在运行中途以 **exit -1** 结束（`run-p13/logs` 与 `.work/f77-client-a.log` 里没有崩溃栈、没有 `Crash report`，
  最后一行只是 18:04:23 的 RCON 聊天回显），且 `/data get entity Dev` 随后返回 `No entity was found` —— 进程被外部终止。
- 食用在服务端只有**一个入口**：`ItemStack#finishUsingItem`（客户端右键只是触发它）。
  因此用一次性探针在 **真实 `ServerPlayer`（NeoForge `FakePlayerFactory`）** 上直接调这个入口，
  `Player#eat` → `FoodData#eat` → 营养 / 饱和度 / 效果全部走真实代码，读数与「客户端吃一口」等价。

## 5. 顺带发现：文档的 Saturation 期望值算式漏了原版 ×2（建议勘误，不改代码）

`13-TASKS-0.0.2.md` F-77 判据 2 写「`Saturation` 差为 4.8（4 × 1.2）」。实测与实测对照：

| 食物 | nutrition | saturationModifier | 实测 saturation 增量 |
|---|---|---|---|
| `farmstorm:wild_apple` | 4 | 1.2 | **9.6** |
| `minecraft:golden_apple`（同一次探针） | 4 | 1.2 | **9.6** |

原因：原版 `FoodData#eat` 的公式是 `saturation += nutrition * saturationModifier * 2.0F`（1.21.1），
所以 4 × 1.2 × 2 = 9.6；文档的 4.8 = 4 × 1.2 少了末尾的 2 倍。
config 注释与 DESIGN 都写「取原版金苹果同值」，实测两者**逐位相同（都是 9.6）**，因此**语义是对的、代码不用改**；
建议把 F-77 判据 2 的期望值改成 9.6（或写成「= 金苹果同值」）。

## 6. 探针（已删净，按 PARALLEL §3.5 声明存在过）

- 探针文件：`src/main/java/com/dashpig/farmstorm/ProbeF77.java`（`@EventBusSubscriber` + 一次性开关文件 `run-p13/f77-probe.enabled`），
  已在取数后**删除**；开关文件同时删除。
- 交付前自查三项全 0（针对本任务）：
  - `src` 下 `F77Probe` 相关文件：0（`Get-ChildItem -Recurse -Filter "*Probe*"` 只剩 Lane B 的 `client/P14Probe.java`，不是我的）；
  - `build/classes` 下 `*Probe*`：只剩 `client/P14Probe.class`（Lane B 的）；
  - 主 jar `farmstorm-0.0.2+mc1.21.1.jar` 内包含 `F77` 的条目：**0**；`Probe` 条目只剩 `com/dashpig/farmstorm/client/P14Probe.class`。
- ⚠️ 提醒调度者：**Lane B 的 `client/P14Probe.java` 目前在源码里、也进了 jar**，出货前必须清掉（不是我的文件，我没动）。

## 7. 清场

- 服务端 `/stop` 收尾；`run-p13/f77-probe.enabled` 已删；收尾后 `:25631/:25641` 无 LISTENING。
- 未 commit；未碰 `client/FarmstormClient.java`、`Farmstorm.java`、`docs/`、`datagen/`、`src/generated/`、
  `behavior/impl/ZangiefCaneTrap.java`、`effect/ZangiefPiledriverEffect.java`、`effect/WildHunterEffect.java`（后者本任务不需要改）。
