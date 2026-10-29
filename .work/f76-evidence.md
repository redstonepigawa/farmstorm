# F-76 证据 —— 狂野猎人三项属性加成（Lane A / Phase 13）

- 结论：**交付物已满足，零源码改动**。`effect/WildHunterEffect.java` 自 F-64 创建后未作修改
  （`git status` 里没有它），三个修饰器 + 固定 id + 配置接线（V-13）都已在 HEAD 上。
- 环境：`run-p13`（server-port **25631** / rcon **25641**，rcon.password=p13rcon），
  客户端 `runClient -PfsRunDir=run-p13 --init-script .work/client-quickplay.gradle`，
  `FS_QUICKPLAY_PORT=25631`，进世界成功（窗口标题 `Minecraft NeoForge* 1.21.1 - Multiplayer (3rd-party Server)`，
  服务端 `Dev joined the game`，客户端 `Connecting to 127.0.0.1, 25631`）。
- 脚本化读数：`.work/f76-rcon.js`（RCON 客户端）+ `.work/f76-phase*.cmd.txt`（命令清单），
  原始输出见 `.work/f76-phase*-out.txt`。

## 0. 静态核对（HEAD c544c0f，未改）

`src/main/java/com/dashpig/farmstorm/effect/WildHunterEffect.java`
- L26-28 三个固定 `ResourceLocation`：`farmstorm:wild_hunter_speed` / `_attack_speed` / `_attack_damage`；
- L41-48 `addAttributeModifiers` 分别挂 `MOVEMENT_SPEED` / `ATTACK_SPEED` / `ATTACK_DAMAGE`；
- L62-71 `apply()` 先按 id 卸再挂，`ADD_MULTIPLIED_TOTAL`，量值 = `percentPerLevel * (amplifier+1) / 100`；
- L50-55 `removeAttributeModifiers` 按同一组 id 逐个 `removeModifier`；
- L42-47 三个百分比来自 `FarmstormConfig.wildHunterSpeedBonus()/AttackSpeedBonus()/AttackDamageBonus()`（V-13 快照）。

## 1. 判据1：三项读数 +30 % / +30 % / +40 %（玩家，真实客户端在世界上）

`.work/f76-phase1-out.txt`（同日 17:31:39 会话，客户端在线，Dev 在世界上）：

| 状态 | Speed | Attack Speed | Attack Damage |
|---|---|---|---|
| 基线（无效果） | 0.10000000149011612 | 4.0 | 1.0 |
| `effect give Dev farmstorm:wild_hunter 700 0` | 0.13000000193715097 | 5.2 | 1.4 |
| 比值 | **×1.30** | **×1.30** | **×1.40** |

`/data get entity Dev attributes`（效果在身时）逐字：

```text
[{id: "minecraft:generic.attack_damage", modifiers: [{amount: 0.4d, id: "farmstorm:wild_hunter_attack_damage",
  operation: "add_multiplied_total"}], base: 1.0d}, ...
 {id: "minecraft:generic.attack_speed", modifiers: [{amount: 0.3d, id: "farmstorm:wild_hunter_attack_speed",
  operation: "add_multiplied_total"}], base: 4.0d}, ...
 {id: "minecraft:generic.oxygen_bonus", base: 0.0d},
 {id: "minecraft:generic.movement_speed", modifiers: [{amount: 0.3d, id: "farmstorm:wild_hunter_speed",
  operation: "add_multiplied_total"}], base: 0.10000000149011612d}]
```

→ 三个 id 与 config 默认值（30 / 30 / 40）逐项对上，amplifier 0 时量值就是 0.3 / 0.3 / 0.4。

> 注：F-76 的交付物是「效果的属性加成」，狂野苹果这个入口属于 **F-77**（`FarmstormItems`），
> 所以本任务用 `/effect give` 施加，不依赖苹果。

## 2. 判据2：`/effect clear` 后回落基线

同一会话续（`.work/f76-phase1-out.txt` 后半）：

```text
> /effect clear Dev farmstorm:wild_hunter
    "Removed effect effect.farmstorm.wild_hunter from Dev"
> /execute as Dev run attribute @s minecraft:generic.movement_speed get
    "Value of attribute Speed for entity Dev is 0.10000000149011612"
> /execute as Dev run attribute @s minecraft:generic.attack_speed get
    "Value of attribute Attack Speed for entity Dev is 4.0"
> /execute as Dev run attribute @s minecraft:generic.attack_damage get
    "Value of attribute Attack Damage for entity Dev is 1.0"
> /data get entity Dev attributes
    "Dev has the following entity data: [{id: "minecraft:generic.attack_damage", base: 1.0d}, ...
     {id: "minecraft:generic.attack_speed", base: 4.0d}, {id: "minecraft:generic.oxygen_bonus", base: 0.0d},
     {id: "minecraft:generic.movement_speed", base: 0.10000000149011612d}]"
```

→ 三项全部回到基线，且 NBT 里 **不再有任何 `farmstorm:wild_hunter_*` 修饰器**（固定 id 卸得掉）。

## 3. 判据3：`wild_hunter.attackDamageBonusPercent` 40.0 → 100.0 → 40.0

改 config 必须**重进存档**（`bake()` 绑在 `ModConfigEvent.Loading/Reloading`），因此每态都是「停服 → 改文件 → 起服」。
读数用僵尸（base Attack Damage 3.0）：效果机理与玩家完全同一段代码，且免去每次重启都要重连客户端。

| config | 服务端 bake 日志 | 无效果 | 效果在身 | 修饰器 amount |
|---|---|---|---|---|
| 40.0（默认） | `wild_hunter 加成 移速 30.0% / 攻速 30.0% / 攻击 40.0%` | 3.0 | **4.2** | `0.4d` |
| **100.0** | `wild_hunter 加成 移速 30.0% / 攻速 30.0% / 攻击 100.0%` | 3.0 | **6.0** | `1.0d` |
| 40.0（改回） | `wild_hunter 加成 移速 30.0% / 攻速 30.0% / 攻击 40.0%` | 3.0 | **4.2** | `0.4d` |

原始输出：`.work/f76-phase2-base-out.txt`（40.0）、`.work/f76-phase3-100-out.txt`（100.0）、
`.work/f76-phase3-40-out.txt`（改回；含 `effect clear` → 3.0 的回落读数）。
bake 日志原文在 `run-p13/logs/latest.log`（会话 E 17:47:24.795、会话 F 17:50:32.977）。

## 4. 数据来源与替代说明（为什么不是 F3 截图）

- 1.21.1 的 F3 调试屏**根本不显示任何属性数值**（没有 Speed / Attack Speed / Attack Damage 行；
  那是 1.21.2+ 才有的显示）。所以「F3 读数 +30 %/+30 %/+40 %」在 1.21.1 只能落到
  `/attribute <target> <attr> get` —— 它读的就是 F3 若显示时用的同一个 `AttributeInstance` 值，
  并且本任务**确实是在真实客户端连进世界后**对真实玩家 `Dev` 读的（见 §1）。
- `/attribute @s ...` 从 RCON 执行时没有「自己」，所以用 `execute as Dev run attribute @s ...` 取到玩家实体。
- NBT 路径是 **`attributes`（小写）**，`Attributes` 会返回 `Found no elements matching`。

## 5. 顺带发现（影响后续所有「改 config 验收」，值得进 §5 坑表）

**在 `run-p13/config/farmstorm-server.toml` 上做字符串替换时，PowerShell 的 `Set-Content -Encoding utf8`
会加 UTF-8 BOM（`EF BB BF`），NeoForge 随即判定该 config 文件无法加载：把原文件改名成
`farmstorm-server-N.toml.bak`，并用默认值重建一份新文件 —— 改动被静默吞掉。**

证据链：
1. 17:32 用 `Set-Content` 把 40.0 改成 100.0，服务端 FileWatcher 触发 `bake()`，日志里仍是 `攻击 40.0%`，
   且磁盘上的文件在 12 秒内自己变回 40.0，旁边多出 `farmstorm-server-4.toml.bak`（内含 100.0）。
2. 4 次尝试各留下一个 .bak（`farmstorm-server-1..4.toml.bak`，每个内里都是 100.0），live 文件每次都是 40.0。
3. 正确写法（保留原编码、不加 BOM）：
   ```powershell
   $live = 'D:\project_farm\run-p13\config\farmstorm-server.toml'
   $text = [System.IO.File]::ReadAllText($live, [System.Text.Encoding]::UTF8)
   [System.IO.File]::WriteAllText($live, $text.Replace('attackDamageBonusPercent = 40.0','attackDamageBonusPercent = 100.0'),
       (New-Object System.Text.UTF8Encoding($false)))
   ```
   写完后首字节必须是 `91,99,111`（`[co`）而不是 `239,187,191`（BOM）。改完起服，bake 日志立刻变 `攻击 100.0%`，
   且不再产生新 .bak。
4. 反例：同样用 `Set-Content` 改过一次后，即使随后把值改回 40.0，文件已带 BOM，服务端仍会重建。

## 6. 清场

- 会话全部 `/stop` 收尾；`Get-Process java | Stop-Process -Force`；`.tools/runserver.lock` 已删除；
- 收尾后 `:25631/:25641` 无 LISTENING（只剩 TIME_WAIT）；
- `run-p13/config/farmstorm-server.toml` 已与动手前的原始副本（`.work/f76-config-orig.toml`）**逐行相同**（diff = 0 行）；
- 未新增任何 `*Probe*` 类（`src` 下 0 个），未引入 `CustomPacketPayload`（0 处）；
- 未 commit；未碰 `client/FarmstormClient.java`、`Farmstorm.java`、`docs/`、`datagen/`、`src/generated/`、
  `behavior/impl/ZangiefCaneTrap.java`、`effect/ZangiefPiledriverEffect.java`。

## 7. 给后续任务的一条约束（渲染证据）

本会话里 **窗口前台切换被系统拒绝**（`SetForegroundWindow`/`SwitchToThisWindow`/`AppActivate` 全部无效，
`GetForegroundWindow()` 始终不是客户端窗口），因此「截窗口」只能照到桌面上最上层的别的程序
（实测 `.work\f76-client-shot.png` 截到的是无关的加速器窗口）。
F-82/F-83 这类**必须出图**的验收要走**游戏内 `Screenshot.takeScreenshot(...)`**（读 framebuffer，与窗口焦点无关），
探针必须按 §3.5 加文件开关并交付前删净。
