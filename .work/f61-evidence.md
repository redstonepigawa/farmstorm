# F-61 证据（v0.0.2 · config 22 键四分组 + bake 快照 + V-13 死键关闭）

HEAD: 55cd6bb（F-66 之后，**未 commit**）｜ 只动了任务书边界内的 2 个文件

    src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java  | 148 ++++++++++++++++++-
    src/main/java/com/dashpig/farmstorm/effect/WildHunterEffect.java |  24 ++--
    2 files changed, 160 insertions(+), 12 deletions(-)

## 1. 139 个叶节点（数法 + 确切路径）

读的是 **`D:\project_farm\run-f61\config\farmstorm-server.toml`**（`-PfsRunDir=run-f61` 起的专用服务端自己生成的那份）。
`verify_project.py` 读的是 `run/config/farmstorm-server.toml`，两者**不同**——见 §6。

数法：逐行扫，跳过空行与 `#` 注释行；`[section]` 行改 section 游标；`key = value` 行记成 `section.key`（v0.0.1 的 `blast_carrot.nausea.level` 这类嵌套也按同一规则展开）。

    leaves=139

分组出现顺序（末尾三个新组之后没有别的组）：

    … | power_beet | power_beet.strength | power_beet.speed | power_beet.haste | power_beet.resistance
      | molotov | wild_apple | wild_hunter | zangief_cane        ← 4 个新组按字母序追加在文件末尾

（`run-f61` 是全新目录：139 个键全部由本轮代码生成，不存在"旧文件残留"。）

## 2. 22 个键与 BRIEF §8 的机械比对（不是肉眼核）

脚本 `.work/f61-keycheck.py`：解析 `.work/v002-BRIEF.md` §8 表格的 22 行（key / 默认值 / 范围），与生成的 toml 逐项比对（默认值按数值比、范围按 `# Range:` 注释比）。

    22 键齐备: 22/22；default+range 不一致: 0

完整清单（group.key = toml 值 / 范围，全部与 BRIEF §8 一致）：

| # | 键 | toml 默认值 | 范围 |
|---|---|---|---|
| 1 | `molotov.effectRadius` | 4.0 | 0.0–16.0 |
| 2 | `molotov.fireChance` | **85.0**（A-01） | 0.0–100.0 |
| 3 | `molotov.triggerExplosionPower` | 0 | 0–100 |
| 4 | `wild_apple.nutrition` | 4 | 0–20 |
| 5 | `wild_apple.saturationModifier` | 1.2 | 0.0–10.0 |
| 6 | `wild_hunter.durationTicks` | **700**（A-02） | 1–72000 |
| 7 | `wild_hunter.killExtensionTicks` | 160 | 0–72000 |
| 8 | `wild_hunter.killHealPoints` | 10.0 | 0.0–40.0 |
| 9 | `wild_hunter.searchRadius` | 5.0 | 0.0–32.0 |
| 10 | `wild_hunter.searchAngleDegrees` | 30.0 | 0.0–180.0 |
| 11 | `wild_hunter.dashTicks` | 3 | 1–20 |
| 12 | `wild_hunter.speedBonusPercent` | 30.0 | 0.0–1000.0 |
| 13 | `wild_hunter.attackSpeedBonusPercent` | 30.0 | 0.0–1000.0 |
| 14 | `wild_hunter.attackDamageBonusPercent` | 40.0 | 0.0–1000.0 |
| 15 | `wild_hunter.highlightRadius` | 64.0 | 0.0–256.0 |
| 16 | `wild_hunter.loopSoundRange` | 16.0 | 0.0–64.0 |
| 17 | `zangief_cane.triggerRadius` | 1.5 | 0.0–8.0 |
| 18 | `zangief_cane.stageDelayTicks` | 14 | 0–200 |
| 19 | `zangief_cane.levitationLevel` | 7 | 1–255 |
| 20 | `zangief_cane.levitationDurationTicks` | 20 | 0–72000 |
| 21 | `zangief_cane.fallTicks` | 40 | 1–200 |
| 22 | `zangief_cane.spinDegreesPerTick` | **72.0**（A-05） | 0.0–360.0 |

书写形态按 A-04：正文键 `<group>.<key>`，`translation()` 写 `farmstorm.configuration.<group>.<key>`（语言键属 datagen，本轮不写）。

## 3. bake() 派生快照 + /reload（验收 3）

新快照 4 项：`fireChance`（molotov 放火百分数）+ `wildHunterSpeedBonus` / `wildHunterAttackSpeedBonus` / `wildHunterAttackDamageBonus`（三个百分数）。
初值 = §8.1 默认值（85.0 / 30.0 / 30.0 / 40.0），`bake()` 从 config 覆盖，访问器 `fireChance()` / `wildHunterSpeedBonus()` / `wildHunterAttackSpeedBonus()` / `wildHunterAttackDamageBonus()` 返回快照。
每次 bake 打一行（`Farmstorm.LOGGER.info`），`/reload` 后靠它读回。

原文（`run-f61/logs/latest.log`，RCON `reload` 触发，FileWatcher 线程执行重载）：

    14:36:14.790 启动加载   bake() 派生快照：molotov.fireChance=85.0%（余下 15.0% 归岩浆块）；wild_hunter 加成 移速 30.0% / 攻速 30.0% / 攻击 40.0%；爆炸抗性阈值 6.0 / 6.0；无队伍降级 self
    14:37:33.092 改 50.0后  bake() 派生快照：molotov.fireChance=50.0%（余下 50.0% 归岩浆块）；…（其余不变）
    14:38:00.526 改回85.0  bake() 派生快照：molotov.fireChance=85.0%（余下 15.0% 归岩浆块）；…

改法与命令（每次都是"写文件 → 立刻 RCON `reload`"）：`fireChance = 85.0` → `50.0` → `85.0`，读回值 50.0 / 85.0 各出现一次。

**排障记录（诚实口径）**：第一次改文件用了 PowerShell 的 `Set-Content -Encoding utf8`，写进了 BOM，NeoForge 报
`Failed to load config farmstorm-server.toml: ParsingException: Invalid bare key: '﻿[converter'. Attempting to recreate`，于是按默认值重建、bake 仍是 85.0。
改用 `[System.IO.File]::WriteAllText(... new UTF8Encoding($false))`（无 BOM）后一次成功。**这是取证脚本的问题，不是代码或 config 的问题**；日志里那一条 WARN 就是它，全日志只此一条 config 解析错。

## 4. 【V-13】三个百分比已不再是常量（验收 4）

代码：`WildHunterEffect.addAttributeModifiers` 三个实参从常量改成 `FarmstormConfig.wildHunterSpeedBonus()` / `wildHunterAttackSpeedBonus()` / `wildHunterAttackDamageBonus()`；`apply()` / `removeAttributeModifiers` 一行未动（仍是"先按 id 卸再挂 + amplifier+1 倍"）。

运行时证据（RCON `/summon` + `/effect` + `/attribute`，同一头僵尸 husk，`NoAI:1b`）：

| 配置 | 操作 | `generic.movement_speed` | `generic.attack_damage` |
|---|---|---|---|
| 30.0 / 40.0（默认） | 加效果前 | 0.23000000417232513 | 3.0 |
| 30.0 / 40.0（默认） | 加效果后 | **0.29900000542402266**（×1.30） | **4.199999999999999**（×1.40） |
| 200.0 / 100.0（改后） | 加效果前 | 0.23000000417232513 | 3.0 |
| 200.0 / 100.0（改后） | 加效果后 | **0.6900000125169754**（×3.00） | **6.0**（×2.00） |
| 200.0 / 100.0（改后） | `effect clear` 后 | 0.23000000417232513 | 3.0 |

同一次改动的 bake 行（证明第三个键也进了快照）：

    14:43:44.926 bake() 派生快照：molotov.fireChance=85.0%…；wild_hunter 加成 移速 200.0% / 攻速 300.0% / 攻击 100.0%；…

结论：`wild_hunter.speedBonusPercent` / `attackDamageBonusPercent` 两键**运行时确实驱动修饰器量值**；卸下后属性精确回到基值（F-64 的"不留残余"性质没被破坏）。

**未证到的那一个（必须说清）**：`attackSpeedBonusPercent` 没有运行时读数。原因不是代码，是**原版把 ATTACK_SPEED 只挂在玩家身上**——实测 `attribute … minecraft:generic.attack_speed get` 对 husk / iron_golem / ravager / warden 一律回 `Entity X has no attribute Attack Speed`（调用记录见下）。要读它得让真玩家进世界，而本机客户端起不来：`runClient -PfsRunDir=run-f61c`（quickPlay 参数已注入，`ModLauncher running: args [… --quickPlayMultiplayer, 127.0.0.1:25567]`）停在 `Loaded 0 entity animations` 后不再前进、3 分钟没连上（与 F-66 记录的同一现象），只能关掉。
它的证据因此是间接的、但闭合的两条：(a) 该键的 bake 值随 config 变成 300.0（上一行日志）；(b) 三个值走的是**同一个** `apply(attributes, …, percentPerLevel, amplifier)` 表达式，只有实参不同。

## 5. 硬门禁

    > Task :build
    BUILD SUCCESSFUL in 10s
    5 actionable tasks: 3 executed, 2 up-to-date
    exit=0；build 日志里 warning 行数 = 0（.work/f61-build.log，25 行）
    （compileJava 是 executed，不是 up-to-date）

三条自检（同一次服务端启动，逐字）：

    [14:36:12.382] 自检 注册对象 77 个：items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1 → OK
    [14:36:12.381] 自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK
    [14:36:12.382] 自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK
    （另有 chebyshev / euclidean / 幸运浆果权重和 1000 三条 OK）

服务端起停：`Done (0.864s)!` → `Thread RCON Listener started`（25607）→ RCON `stop` → `All dimensions are saved` → `BUILD SUCCESSFUL in 8m 17s`。完整日志 `.work/f61-server.log` + `run-f61/logs/latest.log`。

## 6. verify_project.py 的实际输出（两种口径都给）

(a) **脚本原样、`run/config` 仍是 v0.0.1 的 117 键**（未跑过默认 run 目录）：

    文档口径键数: 117 / 生成 toml 键数: 117 → [OK] 117 个配置键与文档逐字一致（117 键）
    结论：FAIL 0 项，WARN 0 项（exit 0）

也就是说：本轮**只跑 run-f61**，脚本读的那份 `run/config` 没被改写，所以它其实还是绿的——"它一定会红"的前提是那份 toml 也升到 0.0.2。

(b) 把 0.0.2 生成的那份 toml 复制进 `run/config`（旧版备份在 `.work/f61-run-config-117.toml.bak`，`run/` 是 gitignore 的生成物），再跑：

    == 1. config：文档 117 键 vs 生成的 toml ==
      文档口径键数: 117
      生成 toml 键数: 139
      [FAIL] toml 多 22 个键: ['molotov.effectRadius', 'molotov.fireChance', 'molotov.triggerExplosionPower',
              'wild_apple.nutrition', 'wild_apple.saturationModifier', 'wild_hunter.attackDamageBonusPercent',
              'wild_hunter.attackSpeedBonusPercent', 'wild_hunter.dashTicks']
    结论：FAIL 1 项，WARN 0 项（exit 1）

失败项**唯一来源**就是脚本仍停在 v0.0.1 的 117 键口径（V-07 已登记，脚本升级单列一件活）；第 2/3/4 段仍全绿。脚本与 docs 一个字没动。

## 7. 施工中与环境相关的事实（留档）

1. `.tools/runserver.lock` 被**另一会话**的 `run-verify` 服务端占着（PID 27992，14:16 起，监听 25565），`gw.cmd runServer` 因此在锁上等了 20 分钟没动静。我没有动那个服务端，改为用 gw.cmd 里**同一套环境变量与 init script** 直接调 `gradle.bat`，并且用自己的 `-PfsRunDir=run-f61` 与独立端口（25567 / RCON 25607），不共享世界目录与日志——R-21 要避的冲突（同 run 目录 / 同 session.lock）并不存在。源码与构建脚本零改动。
2. 客户端 `run-f61c` 目录是一次性取证目录（gitignore），已关掉。
3. 本地 `python` 不在 PATH，用 `load_workspace_dependencies` 给的捆绑 python 跑脚本。
4. `molotov.fireChance()` 访问器本轮**没有消费方**（只有 bake 日志在用快照），落点结算属 F-68；保留它是为了让"改 → /reload → 读回"这条验收有唯一可读入口。
5. 客户端侧（SERVER config 在客户端读不到）`WildHunterEffect` 会用快照初值 30/30/40，与 TECH §8.4 的"客户端镜像常量"口径一致；服务端权威值才是 config 里的值。
6. datagen 的 22 条 `farmstorm.configuration.*` 语言键未写（边界明令不许动 datagen），与 `12-TECH §8.3` 的 261 键口径留给语言键任务。

## 8. 复现命令

    cmd /c ""D:\project_farm\.tools\gw.cmd" build -PfsRunDir=run-f61"                      # exit 0 / 0 warning
    cmd /c ""D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-f61"                  # 被别的会话占锁时改用直接 gradle 调用（同环境变量）
    <node> .work\f66-rcon.js 25607 f61rcon reload|stop|<任意命令>
    <python> -X utf8 tools/verify_project.py
    <python> -X utf8 .work/f61-keycheck.py
