# F-98 动态验收计划（等场地）

读数工具（都不需要客户端）：
- `.work/f98-adv-check.js` —— 读 `run-p14/world/advancements/<uuid>.json`，列出该玩家已授予的 `farmstorm:*` 进度与 criteria。
  当前基线（刚跑过）：`first_throw` 已授予；`first_molotov / zangief_hit / first_wild_hunter` 都是 **no**（干净起点）。
- toast 用游戏内 `Screenshot.grab` 探针抓（探针源码在 `.work/F9XProbe.java.txt`，跑到前才拷进 src，跑完删）。

## 开跑前
```
# 全部 revoke 干净，保证观察到的都是本轮新授予
/advancement revoke @p everything
```

## ① first_molotov · 丢一颗莫洛托夫到脚边（不要求命中）
```
/give @p farmstorm:molotov
# 客户端右键朝地面丢（探针做不到右键；改为服务端等价路径，见下）
```
**问题**：右键需要真实鼠标输入，本机送不进去（V-34）。两条替代方案：
- (a) 让作者/你手点一次（最真实）；
- (b) 服务端等价物：`/summon` 出来的实体**没有归属者**，不能用来验「投掷者得进度」。
  真正等价的做法是让客户端执行一次 `use` —— 探针可以在客户端调 `minecraft.gameMode.useItem(player, hand)`，
  这条路会走服务端 `ThrowableCropItem#use`，归属者与真实投掷一致。**要不要我给探针加这一条**（`use` 动作）？

## ② zangief_hit · 双账号对照
**约束**：受害者必须真的在 `level.players()` 里（陷阱只遍历 `level.players()`），所以第二条**必须有第二个在线玩家**。
单客户端能做到的一半：
```
# 陷阱归属者写成一个离线 uuid（模拟「别人布的陷阱」），我踩上去
/setblock 100 6 100 farmstorm:zangief_cane
/data merge block 100 6 100 {ownerUuid:[I;9,9,9,9],ownerName:"GhostOwner"}
/tp @p 100.5 6.0 100.5
# 期望：陷阱触发、我被打桩机；但 farmstorm:zangief_hit 对我 **不授予**
```
另一半（**部署者得**）需要两个在线账号。两条路：
- (A) 你授权一个短窗口：我起 **Dev2** 客户端（已备好 `.work/client-quickplay-p14user.gradle`，`FS_P14_USER=Dev2`），
  让 Lane A 的 `Dev` 客户端连到我的 25651；我放陷阱（Dev2 部署），Dev 踩上去 → 期望 **Dev2 得**、**Dev 不得**。
  风险正是作者遇到的「两个客户端同屏」那一档（GPU/上下文争用），需要你点头。
- (B) 你或作者手点两个账号跑一遍，我把命令与读数脚本给你，你贴结果我整理进证据。

## ③ first_wild_hunter · 指令给效果（不限定来源）
```
/advancement revoke @p only farmstorm:first_wild_hunter
/effect give @p farmstorm:wild_hunter 5 0
# 期望：toast「狂野猎人」；存档里 first_wild_hunter 变 granted
```

## ④ 反向：/summon 的莫洛托夫不推进 first_molotov
```
/advancement revoke @p only farmstorm:first_molotov
/summon farmstorm:molotov 100.5 8 100.5 {Motion:[0.0d,-0.1d,0.0d]}
# 期望：first_molotov 仍为 no（无归属者 ⇒ onAddedToLevel 里的 getOwner() 不是 ServerPlayer）
```

## 每条要留的证据
1. 命令原文 + 服务端返回；
2. `.work/f98-adv-check.js` 的读数（granted / no）；
3. toast 截图（能抓到时）；
4. 本文件保存成 `.work/f98-dynamic-evidence.md`。
