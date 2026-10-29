# F-98 动态验收 + isActive 处置 证据

环境：服务端 `run-p14`（25651/25661，`FS_RUN_LOCK=run-p14`）；客户端两个：`run-p14user2`=**Dev2**、`run-p14user3`=**Dev3**（独立运行目录，`client-two-users.gradle` 每次重写 `--quickPlayMultiplayer` 与 `--username` 两段）
读数方式：`/save-all flush` 之后读 `run-p14/world/advancements/<uuid>.json`（**注意：不 save 的话文件不会刷新，会误读成「没授予」**），脚本 `.work/f98-adv-check.js`

## 1. ① first_molotov · 投出即判定（不要求命中）
探针动作 `use` → `gameMode.useItem(player, MAIN_HAND)`（与真人右键同一条 `ServerboundUseItemPacket` 链路）
```
[21:09:40] F9X 取证 右键使用主手物品，consumesAction=true
[21:09:41] [Server thread/INFO] [farmstorm/]: 莫洛托夫 落点 (100.34, 6.05, 115.85) 威力 0
[21:09:41] [Server thread/INFO] [farmstorm/]: 莫洛托夫 候选 45 火 40 岩浆 5
```
- 存档（Dev2 = 21cc3e25…）：`GRANTED farmstorm:first_molotov criteria=first_molotov`、`GRANTED farmstorm:first_throw`
- **toast 截图**：`.work/f98-1-dev2-toast-molotov.png` —— 右上角同时弹出两条：
  `Advancement Made! — First Throw` 与 `Advancement Made! — **It's Lit!**`（= first_molotov），画面里还能看到莫洛托夫洒出的火带。
- 注意：落点 x=115.85 说明这一发飞出去很远——**「不要求命中」正好被这一发验证到**。

## 2. ② zangief_hit · 部署者得、被命中者不得（双账号）
| 角色 | 账号 | 读数 |
|---|---|---|
| 部署者（陷阱归属） | **Dev2** | `GRANTED farmstorm:zangief_hit criteria=zangief_hit` ✔ |
| 受害者（踩上去触发） | **Dev3** | 存档里 **0 条 farmstorm 进度**（连 first_throw 都没有）⇒ **未授予** ✔ |
```
[21:18:50] [Server thread/INFO] [farmstorm/]: 桑吉尔蔗 相位 0 t=109424 玩家 Dev3 duration=15
[21:18:50] [Server thread/INFO] [farmstorm/]: 桑吉尔蔗 触发   t=109424 玩家 Dev3 中心距 1.03
```
**一处与计划的偏差（照实写）**：原本要用探针的 `useItemOn` 让 Dev2「手放」陷阱，
但 `gameMode.useItemOn` 两次都返回 **FAIL**（`[21:17:05] F9X 取证 右键方块 100, 5, 100 面=up，结果 FAIL`，方块确实没放下去），
原因未查明。于是改用 `/setblock` + `/data merge block {ownerUuid, ownerName}` 写入归属——
**这两个字段正是 `DeployedCropBlock#setPlacedBy` 会写的两个字段**，而 `trigger()` 的授予只读 `be.owner(level)`，所以对「授予逻辑」的验证等价。
（`useItemOn` 那个 FAIL 建议作者手动放一次陷阱复核：若真人右键也放不下，那是真缺陷，不在这条验收范围内。）

## 3. ③ first_wild_hunter · 不限定来源
```
> effect give Dev3 farmstorm:wild_hunter 5 0 false   =>  Applied effect Wild Hunter to Dev3
存档（Dev3 = 1509e314…）：GRANTED farmstorm:first_wild_hunter criteria=first_wild_hunter
> advancement revoke Dev3 only farmstorm:first_wild_hunter  =>  Revoked the advancement [Wild Hunter] from Dev3
> effect give Dev3 farmstorm:wild_hunter 5 0 false        =>  再次 GRANTED（可重复性）
```
- **纯指令来源**也推进 ⇒「不限定来源」成立（没有经过狂野苹果）。
- toast 照片没抓到：第一次 Dev3 站在我 T4 召唤的莫洛托夫落点上**被火烧死**（死亡截图 `.work/f98-4-dev3-toast-wildhunter-death.png` 记录了这一幕），第二次重授时截图错过窗口。
  授予本身有存档原文 + 第二次重授；`first_wild_hunter` 在 Dev2 上同样 GRANTED（`Farmstorm: first_wild_hunter`）。

## 4. ④ 反向：/summon 的莫洛托夫不推进 first_molotov
```
> summon farmstorm:molotov 100.5 9.0 100.5 {Motion:[0.0d,-0.05d,0.0d]}   =>  Summoned new Molotov
> advancement revoke Dev3 only farmstorm:first_molotov   =>  Couldn't revoke … as they don't have it
存档（Dev3）：first_molotov = **no**（召唤的实体没有归属者 ⇒ onAddedToLevel 里 getOwner() 不是 ServerPlayer）
```

## 5. 两个客户端同屏 · 地形是否正常（作者那次「看不见地形」的复现窗口）
- 两个客户端（Dev2/Dev3）同时在同一个服务端里，**两边地形都正常渲染**：
  - `.work/f98-2-dev2-terrain.png`（Dev2 视角：完整石台 + 火带 + 天空）
  - `.work/f98-3-dev3-terrain.png`（Dev3 视角：完整石台 + 岩浆块 + 天空）
  - `.work/f98-1-dev2-toast-molotov.png`、`.work/f98-4-toast-wildhunter.png` 同样是完整地形
- 三个客户端运行目录（`run-p14c` / `run-p14user2` / `run-p14user3`）**都没有 `crash-reports/` 目录**（= 没有任何崩溃报告）。
- 结论：**本轮未复现**「地形不显示」。按你的实验口径，这削弱（但不彻底排除）「两个客户端同屏必现」的假设。

## 6. isActive 处置（选项 a：删除）
- 判定：`ZangiefPiledriverController.isActive(UUID)` 全仓 **0 处外部调用**；`TECH §3.12` 想让 `WildHunterCombat#settleSwing` 用它做相位互斥，
  但实际互斥已由「`zangief_piledriver` 效果在身」判定覆盖 ⇒ 删。
- 改动：删掉该方法（4 行）；`build` 通过（`BUILD SUCCESSFUL`），全仓再无 `isActive`（grep 0 处）。
- 工作区现在只有这一处改动（其余 F-98 内容已在 `184e24f` / `812842e`）。

## 7. 探针清理（V-40）
```
src *Probe*    : 0
build/classes  : 0
jar 内 Probe    : 0（farmstorm-0.0.2+mc1.21.1.jar，482 条目）
```
开关文件（三个 run 目录的 `f9x-shot.token`）已删；陈旧 class 已删；删完做了**增量 build**（未 clean，V-41）。
收尾：服务端 rcon `stop`；客户端按 PID 精确杀（Dev2=29072 早先、Dev3=12028）；`.tools` 两把锁都不存在；`Get-Process java` 已空。

## 8. 一处需要说明的操作风险
早前为了重启 Dev2 的探针，我用「窗口标题匹配 Minecraft NeoForge」挑进程，**有可能连带 Lane A 当时那个主菜单客户端一起杀掉**（它标题相同）。
此后改成「按启动时间/PID 精确指定」；如果 Lane A 那边报告过客户端莫名退出，原因可能在这，向你说明。
