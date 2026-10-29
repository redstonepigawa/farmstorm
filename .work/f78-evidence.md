# F-78 证据 —— 索敌算法：5 格球与 30° 夹角（Lane A / Phase 13）

- 结论：**已实现并通过全部四条判据**。新增 **1 个文件**（可做路径限定提交）：
  `src/main/java/com/dashpig/farmstorm/combat/WildHunterCombat.java`（新包 `combat`）。
- 环境：`run-p13`（server-port 25631 / rcon 25641），`FS_RUN_LOCK=run-p13`。
- 构建：`compileJava` 与 `build` 均 **BUILD SUCCESSFUL**（`.work/f78-compile2.log` / `.work/f78-build.log`）。

## 0. 实现

`WildHunterCombat.resolveTarget(Player)`：
- 候选池：`player.getBoundingBox().inflate(searchRadius)` 的 AABB 粗筛 + `distanceToSqr ≤ radius²` 的球精筛；
- 夹角：眼睛位置 → 目标包围盒中心 与 `getLookAngle()` 的夹角（度）；
- 排除：自己 / `!isAlive()` / `isSpectator()` / `candidate instanceof Player && player.isAlliedTo(other)`；
- 排序：`Comparator.comparingDouble(夹角).thenComparingDouble(player::distanceToSqr)`（夹角最小优先，同角取近）；
- 日志（仅服务端）：`狂野猎人 目标 {id} 夹角 {A} 度 距离 {D}`。
- 键：`wild_hunter.searchRadius`（默认 5.0）、`wild_hunter.searchAngleDegrees`（默认 30.0），每次调用现读。

两处口径说明：
1. **参数类型是 `Player` 不是 `ServerPlayer`**：TECH §3.12 要求「算法只有一份」，F-79 的客户端预测（跑在 `ClientLevel`）与 F-80 的服务端复核都要调它；
   F-78 任务书写的 `resolveTarget(ServerPlayer)` 只是当时的形参写法，收窄到 `ServerPlayer` 会让 F-79 无法复用。
2. **方法名**：13-TASKS F-78 叫 `resolveTarget`，TECH §3.12 叫 `selectTarget`。按任务书用 `resolveTarget`。
   若调度者要保留 TECH 的名字，加一行 `public static LivingEntity selectTarget(Player p) { return resolveTarget(p); }` 即可。

## 1. 判据1 / 2 / 3 / 4 一次跑完（探针，见 §3）

探针摆靶：玩家（FakePlayer）在出生点 `(0.5,-59,0.5)`，`yaw=0`（朝 +Z）、`pitch=0`；
猪统一放在 `pigY = eyeY - 0.45`（与眼睛等高），因此**所有夹角都是纯水平的**，排除垂直分量干扰。
猪 `NoAi=true`，同一 tick 内生成并立即结算（不落地、不移动）。

| 场景 | 候选（名字(Δx,Δz)） | 期望 | 实测 | 判定 |
|---|---|---|---|---|
| S1 正前方 4 格 | P_front(0.50,4.50) | 选中 | `pig[P_front]`，日志 `夹角 0.0 度 距离 4.17` | ✅ 判据1 |
| S2 夹角小者胜（16° vs 4°） | P_left_16deg(-0.70,4.50) / P_right_4deg(0.80,4.50) | 选中 4° 那只 | `pig[P_right_4deg]`，日志 `夹角 4.3 度 距离 4.18` | ✅ 判据2 前半 |
| S3 夹角接近（12.0° vs 14.9°） | P_12deg(1.35,4.50) / P_15deg(1.70,5.00) | 选中 12° 那只 | `pig[P_12deg]`，日志 `夹角 12.0 度 距离 4.25` | ✅ 判别力（两候选只差 3°） |
| S4 同角取近（都 0°） | P_far_4.0(0.50,5.00) / P_near_2.0(0.50,3.00) | 选中近的 | `pig[P_near_2.0]`，日志 `夹角 0.0 度 距离 2.76` | ✅ 判据2 后半 |
| S5 背后 180° | P_behind(0.50,-3.00) | 不选 | `null` | ✅ 判据3 |
| S6 半径外 6 格 | P_far6(0.50,6.50) | 不选 | `null` | ✅ 球 |
| S7 夹角 40°（超出 30°） | P_40deg(3.85,4.50) | 不选 | `null` | ✅ 夹角 |
| S8 同队伍玩家 | 第二个 FakePlayer `F78Ally`，`allied=true` | 不选 | `null` | ✅ 判据3 |
| S9 旁观者 | 第二个 FakePlayer `F78Spec`，`setGameMode(SPECTATOR)` 成功、`isSpectator=true` | 不选 | `null` | ✅ 判据3 |

## 2. 判据4：`wild_hunter.searchRadius` 改成 1.0 后 5 格外的猪不再被选中 —— 通过

停服 → 用 `WriteAllText + UTF8Encoding($false)`（V-33：不加 BOM）把 `searchRadius` 改成 1.0 → 起服，同一探针复跑：

```text
[F78PROBE] S1_single_front candidates=[P_front(0.50,4.50)] picked=null
[F78PROBE] S2_angle_wins candidates=[P_left_16deg(-0.70,4.50), P_right_4deg(0.80,4.50)] picked=null
[F78PROBE] S3_close_angles_12_vs_15 candidates=[P_12deg(1.35,4.50), P_15deg(1.70,5.00)] picked=null
[F78PROBE] S4_same_angle_near_wins candidates=[P_far_4.0(0.50,5.00), P_near_2.0(0.50,3.00)] picked=null
```
（S1 在 5.0 时选得中、在 1.0 时选不中，同一个靶位 —— 键生效的直接对照证据。）
取完数后 `searchRadius` 已改回 5.0，且 `run-p13/config/farmstorm-server.toml` 与动手前副本**逐行相同（diff = 0）**。

## 3. 探针（本轮内已删净）

- `src/main/java/com/dashpig/farmstorm/ProbeF78.java`，一次性开关文件 `run-p13/f78-probe.enabled`；两者都已删除。
- 交付前自查：`src` 下 `*F78*` = 0；`build/classes` 下 `*F78*` = 0；主 jar 内 `F78` 条目 = 0。
- ⚠️ 主 jar 里仍有一个**别人的**探针类：`com/dashpig/farmstorm/client/P14Probe.class`（Lane B 的 `client/P14Probe.java`，不在我的文件范围内，我没动）。

## 4. 未在本轮验证、留给 F-80 的一条

TECH §3.12 的「与相位互斥（`ZangiefPiledriverController.isActive(uuid)` 为真时不吸附）」是**吸附/冲刺**的门槛，
不在纯索敌里；它属于 F-79/F-80 的挂载点（而且 `ZangiefPiledriverController` 是 Lane B 的文件，我只调用不修改），
F-80 接 `ServerboundSwingPacket` 时一并处理，并在 F-80 的证据里给读数。

## 5. 清场

- 服务端 `/stop` 收尾；`:25631/:25641` 无 LISTENING；`.tools/runserver.lock` 已删。
- 未 commit；未碰 `client/FarmstormClient.java`、`Farmstorm.java`、`docs/`、`datagen/`、`src/generated/`、
  `behavior/impl/ZangiefCaneTrap.java`、`effect/ZangiefPiledriverEffect.java`、`effect/WildHunterEffect.java`。
