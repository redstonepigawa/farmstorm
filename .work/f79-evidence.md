# F-79 证据 —— 冲刺的客户端预测位移（Lane A / Phase 13）

- 结论：**逻辑已实现并验证**（(b) 纯函数断言）；真实按键那一遍按调度者裁决留到 **F-85 出口回归**。
- 改动 **2 个新文件**：
  1. `src/main/java/com/dashpig/farmstorm/combat/WildHunterDash.java`（**新建**，纯位移数学，不碰客户端类）
  2. `src/main/java/com/dashpig/farmstorm/client/WildHunterDashClient.java`（**新建**，按键/状态接线；本身不挂事件）
- 构建：`build` **BUILD SUCCESSFUL**（`.work/f79-build.log`）。

## 0. 分工（为什么拆成两个类）

任务书要求「冲刺位移 = 客户端预测」。但本机 OS 级输入注入不可用（窗口拿不到前台），真实按键在 F-79 阶段无法驱动。
按调度者裁决的方案 (b)：**位移数学抽成纯函数放进 `combat/`**（服务端探针可喂坐标直接断言），
`client/WildHunterDashClient` 只做「本地攻击键 + 自己带效果 + 本地算出同一目标 → 每刻写速度」的接线。
服务端 `WildHunterCombat` 与客户端共用 `WildHunterDash` / `WildHunterCombat.resolveTarget`，位移规则只有一份。

## 1. 判据 1：3 刻内跨到位（`wild_hunter.dashTicks` = 3）

探针（`ProbeF79`，开关 `run-p13/f79-probe.enabled`）：

```text
[F79PROBE] dashTicks=3 start=0.50,0.50 pig=0.50,4.50
[F79PROBE] S1_open attached=true landing=0.500,3.300 travel=2.800 gapToTarget=1.200
[F79PROBE] S5_velocity velocity=0.000,0.933 after3ticks=0.500,3.300 equalsLanding=true
```

- 初距 4.0 格 → 位移 **2.800 格**（= 4.0 − STOP_GAP 1.2），落点离目标 **1.200 格**（够得着，不重叠）；
- 每刻速度 0.933 = 2.800 / 3；**3 刻积分后与落点逐位相同**（`equalsLanding=true`）。✅

## 2. 判据 2：中间有石墙 → 停在墙前

在玩家与猪之间（z = 2.0 处，脚 + 头两格）放石头后：

```text
[F79PROBE] S2_wall attached=true landing=0.500,1.750 travel=1.250 gapToTarget=2.750
```

落点 z = **1.750**，墙的方块占据 z ∈ [2, 3)，**没有越过墙面**；位移被截到 1.250 格（不穿墙、不补位）。✅

## 3. 判据 3：目标格被占满 → 本次不吸附

把猪所在的脚 / 头两格填成石头：

```text
[F79PROBE] S3_target_cell_filled attached=false landing=0.500,0.500 travel=0.000 gapToTarget=4.000 playerPos=0.500,0.500
```

`attached=false`、落点 = 起点、位移 0，玩家坐标不变。✅

附带一个更细的场景（落点本身被占，不是不吸附而是停在它前面）：

```text
[F79PROBE] S4_landing_blocked attached=true landing=0.500,2.750 travel=2.250 gapToTarget=1.750
```

## 4. 判据 4 / 5

- 判据4（`logs/latest.log` 无 `moved too quickly!`）：**本轮未取**——它只在真实客户端跑位移时才会出现，
  与真实按键那一条一起留到 **F-85**（调度者已裁决）。服务端探针不写玩家坐标，不产生该日志。
- 判据5：`grep -r "CustomPacketPayload" src/main/java` = **0 处**（已核；本任务没有新增任何包，全走原版效果同步 + 客户端本地预测）。✅

## 5. 需要调度者接线（`client/FarmstormClient.java` 是独占文件，我没有动）

已用一个临时类编译验证过这两段的 API 拼写（验证后临时类已删除）：

```java
// import
import com.dashpig.farmstorm.client.WildHunterDashClient;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

// 方法：加在 FarmstormClient 里（它已经是 @EventBusSubscriber(modid, Dist.CLIENT)）
@SubscribeEvent
public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
    if (event.isAttack()) {
        WildHunterDashClient.onAttackKey();
    }
}

@SubscribeEvent
public static void onClientTick(ClientTickEvent.Post event) {
    WildHunterDashClient.onClientTick();
}
```

不接线的后果：`WildHunterDashClient` 永远是死代码，按下攻击键不会有任何预测位移（也不会崩）。

## 6. 探针与产物

- `ProbeF79` + `run-p13/f79-probe.enabled` 已删除；临时 API 验证类 `DashBindingsScratch` 也已删除。
- 重建 `build` 后三处计数：`src` 0 / `build/classes` 0 / jar 内 `Probe|Scratch` **0**。

## 7. 清场

- 服务端 `/stop`；`:25631/:25641` 无 LISTENING；未 commit；
- 未碰 `client/FarmstormClient.java`、`Farmstorm.java`、`docs/`、`datagen/`、`src/generated/`、Lane B 的文件。
