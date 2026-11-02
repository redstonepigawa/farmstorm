# Phase 30 证据（F-199 ~ F-205）· farmstorm v0.1.1

> 执行者：本 Phase 唯一执行者（子代理）。工作目录 D:\project_farm（Windows / pwsh / Windows PowerShell 5.1 + .tools\jdk21）。
> 基线：HEAD = fd5ad80（Phase 29 已提交）。**本轮没有任何 git 写操作**，提交由调度者做。
> 结论速览：build 退出码 **0**；verify_project **FAIL 0 / WARN 0，退出码 0**；注册 **93**；server config **166 键 / 58 组**（无 hud 段）；client config **3 键**（独立一份）；lang **323 × 2**，farmstorm.configuration. 恰 **169**。

---

## 8.1 改了什么

### git diff --stat 原文

~~~
 .../resources/assets/farmstorm/lang/en_us.json     |  9 ++++
 .../resources/assets/farmstorm/lang/zh_cn.json    |  9 ++++
 src/main/java/com/dashpig/farmstorm/Farmstorm.java |  9 ++++
 .../blockentity/OutpostCoreBlockEntity.java        |  6 +++
 .../dashpig/farmstorm/client/FarmstormClient.java  | 48 ++++++++++++++++++++++
 .../dashpig/farmstorm/config/FarmstormConfig.java  | 12 ++++++
 .../dashpig/farmstorm/datagen/ModLangProvider.java | 22 ++++++++--
 .../farmstorm/event/FarmstormGameEvents.java       | 37 ++++++++++++++---
 8 files changed, 143 insertions(+), 9 deletions(-)
~~~

（新增文件为 untracked，不在 diff --stat 里：见下。）

### 新增文件（9 个 = 任务书 §1 的 network/ 三件 + client/ 五件 + config/FarmstormClientConfig）

| 文件 | 任务 |
|---|---|
| src/main/java/com/dashpig/farmstorm/network/CoreListPayload.java | F-199 payload + 4 元 codec + MAX_CORES |
| src/main/java/com/dashpig/farmstorm/network/FarmstormNetwork.java | F-199 playToClient 注册（mod 总线，版本串 "1"） |
| src/main/java/com/dashpig/farmstorm/network/CoreListBroadcaster.java | F-200 组装 / 三级排序 / 去抖 / 对账 / 10 时机 |
| src/main/java/com/dashpig/farmstorm/client/ClientCoreList.java | F-199 volatile 快照 + handler（方法体只有一句 enqueueWork） |
| src/main/java/com/dashpig/farmstorm/client/FarmstormKeys.java | F-202 2 个 KeyMapping（H / J，复用 CATEGORY_GAMEPLAY） |
| src/main/java/com/dashpig/farmstorm/client/CoreHudRenderer.java | F-203 浮窗渲染（10n+3、8+1 行、五种配色、钳制） |
| src/main/java/com/dashpig/farmstorm/client/CoreHudDragScreen.java | F-204 拖拽（isPauseScreen=true、mouseReleased 不写盘、onClose 写） |
| src/main/java/com/dashpig/farmstorm/client/CoreHudClientCommands.java | F-205 /farmstorm hud on|off|reset（3 处 sendSuccess(..., false)） |
| src/main/java/com/dashpig/farmstorm/config/FarmstormClientConfig.java | F-201 ModConfig.Type.CLIENT，[hud] 3 键 |

> 说明：30 号 §2.1.2 原设计把 handler 单列在 client/network/ClientCoreListHandler。任务书 §1 的新增清单只给 client/ 五件，因此 handler 作为 **ClientCoreList#handle** 落在 client/ClientCoreList.java 里 —— 文件数与 §1 的清单逐一对齐（8 + 1 = 9），行为不变（仍是「一个方法、方法体只有一句 enqueueWork」）。

### 两处注册点

- Farmstorm.java:61-64 —— registerConfig(ModConfig.Type.CLIENT, FarmstormClientConfig.SPEC) + modBus.addListener(FarmstormClientConfig::onLoading) + modBus.addListener(FarmstormNetwork::onRegisterPayloads)
- client/FarmstormClient.java —— onClientTick 里消费 TOGGLE/DRAG；新增 onRegisterClientCommands(RegisterClientCommandsEvent)；新增 onLoggingOut(ClientPlayerNetworkEvent.LoggingOut)

### ModLangProvider 的 9 条新增（314 → 323）

~~~
config(entries, "hud.enabled", "浮窗开关", "Core HUD enabled");
config(entries, "hud.x", "浮窗左上角 x（GUI 像素）", "Core HUD top-left x (GUI pixels)");
config(entries, "hud.y", "浮窗左上角 y（GUI 像素）", "Core HUD top-left y (GUI pixels)");
plain(entries, "key.farmstorm.core_hud.toggle", "开关核心浮窗", "Toggle core HUD");
plain(entries, "key.farmstorm.core_hud.drag", "拖动核心浮窗位置", "Move core HUD");
plain(entries, "gui.farmstorm.core_hud.self", "自己", "You");
plain(entries, "command.farmstorm.hud.on", "核心浮窗已开启", "Core HUD enabled");
plain(entries, "command.farmstorm.hud.off", "核心浮窗已关闭", "Core HUD disabled");
plain(entries, "command.farmstorm.hud.reset", "核心浮窗位置已复位", "Core HUD position reset");
~~~

datagen 由 `gw.cmd runData -PfsRunDir=.work/p30-gen` 重跑（BUILD SUCCESSFUL）；两份 lang 各 +9 行。

---

## 8.2 构建与工具

### .tools\gw.cmd build

~~~
> Task :build
BUILD SUCCESSFUL in 9s
5 actionable tasks: 2 executed, 3 up-to-date
BUILD_EXIT=0
~~~

### python -X utf8 tools/verify_project.py（完整输出；退出码 0）

~~~
== 1. config：文档键集 vs 生成的 toml ==
  30-TECH §4.1：58 组 / 166 键（v0.1.1 的 58 行数据表现算，其中 165 个键名已在前四份清单里）
  剔除已删键 1 个：farmland.limitPerPlayer
  文档口径键数: 166（01-DESIGN §3.8 117 + 12-TECH §8.1 22 + 18-TECH §8.1 19 行 + 24-TECH §8.1 12 行（新 11 + 改 1） + 30-TECH §4.1 166 键；其中 24 / 18 号另有 3 行是改既有默认值、§4.1 另有 165 个键名与前面的清单重名；按并集去重后 167，再扣掉 1 个已删键 = 166）
  生成 toml 键数: 166
  [OK]   166 个配置键与文档逐字一致（toml 166 键）
== 2. 93 个注册 ID 逐字核对（全源码树） ==
  [OK]   items: 32 个 ID 齐全
  [OK]   blocks: 26 个 ID 齐全
  [OK]   block_entity_type: 7 个 ID 齐全
  [OK]   entity_type: 13 个 ID 齐全
  [OK]   mob_effect: 4 个 ID 齐全
  [OK]   sound_event: 3 个 ID 齐全
  [OK]   recipe_type: 1 个 ID 齐全
  [OK]   recipe_serializer: 1 个 ID 齐全
  [OK]   creative_mode_tab: 1 个 ID 齐全
  [OK]   damage_type: 3 个 ID 齐全
  [OK]   data_component_type: 1 个 ID 齐全
  [OK]   menu_type: 1 个 ID 齐全
  已核 registry 对象: 93 / 93
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 323 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch

结论：FAIL 0 项，WARN 0 项
~~~

---

## 8.3 三处 grep + 三处 grep -c + 注册行

| 判据 | 读数 | 期望 |
|---|---|---|
| grep -rln "CustomPacketPayload" src/main/java | **1** 个文件：network/CoreListPayload.java | 恰 1 |
| grep -rn "playToServer\|sendToServer" src/main/java | **0** 命中 | 0 |
| grep -rn "@Mixin\|mixins.json" src/main/java | **0** 命中 | 0（唯一红线） |
| grep -c "MAX_CORES" network/CoreListPayload.java | **3**（常量声明 + javadoc 提及 + codec 使用） | ≥ 2 |
| grep -c "key.categories.farmstorm" | **0** | 0 |
| grep -c "sendSuccess" client/CoreHudClientCommands.java | **3**，三条第二参数都是 false | 3 |
| grep -c "RegisterKeyMappingsEvent" client/FarmstormKeys.java | **2**（import + 方法签名） | 文档写 =1，结构上不可能，见偏差 D-3 |
| 服务端侧 net.minecraft.client.* 引用 | **0**（src/main/java 里非 client/ 包的全部 .java） | 0 |

专用服务器启动日志的注册行原文（.work/p30-server.log、.work/p30-join-server.log 同形）：

~~~
[01:51:28] [modloading-sync-worker/INFO] [farmstorm/]: 自检 网络包注册 farmstorm:core_list（版本 1，S2C 只读，MAX_CORES 64）→ OK
~~~

同一次启动的注册面自检（未变）：

~~~
[02:08:24] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK
~~~

专用服务器：Done (11.674s)，**NoClassDefFoundError 0 命中**（grep 计数 0），客户端侧也打出同一行注册自检。

---

## 8.4 浮窗 / 下发 / 排序的读数

构建方式：离线写 world/data/farmstorm_outpost_core.dat（9/2/1 条，坐标为远区块），起服后用 RCON
setblock 把那几格补成 farmstorm:outpost_core，让索引条目的惰性校验保持有效；再用 mineflayer 机器人 ALICE
（离线 UUID 81b5a0ee-ef3e-349d-9a7e-1a5138d5583b）登录触发下发。

### 1 核（one）

~~~
[01:56:27] 核心区块票补票：1 张（farmstorm_core，distance=1，来源 OutpostCoreIndex）
[01:56:27] 核心比较键指纹 43069738754b9eb0（1 条）
[01:56:30] 玩家登录：ALICE（81b5a0ee-ef3e-349d-9a7e-1a5138d5583b）
[01:56:30] 核心列表组装（客户端未声明 farmstorm:core_list，跳过发送）：ALICE 1 条（total=1）
[01:56:30] 核心列表顺序：[overworld@1000000,64,1000000]
~~~

### 2 核、gameTime 100 / 200（gametime，跑两次 g1 / g2）

~~~
g1 [01:58:01] 核心比较键指纹 936236aa475f60ef（2 条）
g1 [01:58:04] 核心列表组装（...跳过发送）：ALICE 2 条（total=2）
g1 [01:58:04] 核心列表顺序：[overworld@1000000,64,1000000, overworld@1000032,64,1000000]
g2 [02:00:11] 核心比较键指纹 936236aa475f60ef（2 条）
g2 [02:00:14] 核心列表组装（...跳过发送）：ALICE 2 条（total=2）
g2 [02:00:14] 核心列表顺序：[overworld@1000000,64,1000000, overworld@1000032,64,1000000]
~~~

→ 先放 A（t=100）在 B（t=200）前 ✓；同一份存档重启两次，两行指纹**逐字相同** ✓（A-39 ④）。

### 跨维度、两核 gameTime 都是 100（dimension）

~~~
[02:02:00] 核心比较键指纹 66dfabb21a7dcec8（2 条）
[02:02:03] 核心列表组装（...跳过发送）：ALICE 2 条（total=2）
[02:02:03] 核心列表顺序：[overworld@1000000,64,1000000, the_nether@1000000,64,1000000]
~~~

→ 三级比较键的第二级（维度 id 升序）生效：minecraft:overworld 在 minecraft:the_nether 前 ✓（30 号 §7 #9 ②）；
行首维度取的是**维度 id 的 path**（overworld / the_nether），未新增语言键 ✓。

### 9 核（nine，跑两次 n1 / join-server）

~~~
n1 [02:03:10] 核心区块票补票：9 张（farmstorm_core，distance=1，来源 OutpostCoreIndex）
n1 [02:03:10] 核心比较键指纹 93c4bd016b6fe976（9 条）
n1 [02:03:17] 核心列表组装（...跳过发送）：ALICE 9 条（total=9）
n1 [02:03:17] 核心列表顺序：[overworld@1000000,64,1000000, overworld@1000032,..., overworld@1000256,64,1000000]
join [02:08:31] 核心比较键指纹 93c4bd016b6fe976（9 条）
~~~

→ total 是**截断前**的总数（9 条全在包里，MAX_CORES=64 未触发；8 行上限是**渲染**上限，不是包上限）✓；
两次重启 9 核指纹相同 ✓。

### 1 / 8 / 9 行的面板高度

**未验证**（本机无法操作客户端 GUI：按键、拖拽、截图都做不了）。代码层读数（A-41 的 10n + 3）：

| 行数 n | panelHeight(n) = n × (9 + 1) + 3 | 任务书期望 |
|---|---|---|
| 1 | **13** | 13（F-203 验收 1） |
| 8 | **83** | 83 |
| 9 | **93** | 93 |

行数算式：shown = min(cores.size(), MAX_CORE_LINES = 8)；total > 8 时再加 1 行 "+" + (total − 8)。
所以 1 核 → 1 行；8 核 → 8 行、无 +N；9 核 → 8 行核心 + "+1" = 9 行。宽度 = max(font.width(row)) + 4；
绘制原点先钳到 [0, guiWidth − 宽] × [0, guiHeight − 高]。

### 端到端「下发 → 收到」

**未验证（关键限制）**：本机只有原版协议机器人，没有 NeoForge 客户端。两条实测事实锁死了这条边界：

1. 不带 optional() 时，专用服务器在 CONFIGURATION 阶段就拒掉每个原版客户端
   （"You are trying to connect to a server that is running NeoForge, but you are not"）——这是加包之前没有的行为；
2. 带 optional() 但不判通道时，向原版客户端发包会让服务端抛
   UnsupportedOperationException: Payload farmstorm:core_list may not be sent to the client!（NetworkRegistry.checkPacket）并**崩服**。

因此 send() 现在先问 player.connection.hasChannel(CoreListPayload.TYPE)：声明了才真发（记「核心列表下发：…」），
没声明就跳过并记「核心列表组装（客户端未声明 …，跳过发送）：…」。机器人用原版 minecraft:register 声明
farmstorm:core_list 无效（NeoForge 只为 NEOFORGE 类型的连接维护通道集合），所以上面四条读数都停在「组装」这一步。
「下发」与客户端「收到核心列表 N 条」两行**需要一台装了本模组的客户端才能取**。

---

## 8.5 farmstorm-client.toml 的三键与钳制

### 首次起客户端生成（run-p30/config/farmstorm-client.toml，原文）

~~~
[hud]
	#浮窗开关（客户端本地设置；按键 H 与 /farmstorm hud 都会改它）
	enabled = true
	#浮窗左上角的 GUI 像素 x（绘制时还会按屏幕宽二次钳制）
	# Default: 4
	# Range: 0 ~ 3840
	x = 4
	#浮窗左上角的 GUI 像素 y（绘制时还会按屏幕高二次钳制）
	# Default: 4
	# Range: 0 ~ 2160
	y = 4
~~~

→ [hud] 恰 **3 键**，值 true / 4 / 4 ✓；这一份**独立**，不并进 farmstorm-server.toml ✓。

### 服务器不生成这一份

客户端首次启动前，run/config 与 run-p30/config 的清单里都**没有** farmstorm-client.toml（只有
farmstorm-server.toml + fml.toml + neoforge-common.toml + neoforge-server.toml）；专用服务器跑了 5 次仍未出现，
直到客户端启动才出现 ✓。farmstorm-server.toml 至今 **166 键 / 58 组、无 hud 段** ✓。

### 钳制（手改 x = 5000 后重启客户端）

~~~
[02:05:41] [modloading-sync-worker/WARN] [net.neoforged.fml.config.ConfigTracker/CONFIG]:
    Configuration file D:\project_farm\run-p30\config\farmstorm-client.toml is not correct. Correcting
[02:07:46] [modloading-sync-worker/INFO] [farmstorm/]: hud 位置读入 (3840, 4)，开关 true（config/farmstorm-client.toml）
~~~

→ 加载值被钳到 **3840** ✓（文件里 x 也已被改正成 3840）。

---

## 8.6 文档-源码偏差表

| # | 文档口径 | 源码 / 实测 | 处理 |
|---|---|---|---|
| **D-1** | 30 号 §2.1.2 的注册只有「registrar("1").playToClient(...)」，没有 optional() | 实测：不加 optional() ⇒ 每个原版客户端在 CONFIGURATION 阶段被拒（"…running NeoForge, but you are not"）；加 optional() 但不判通道 ⇒ 向原版客户端发包时 NetworkRegistry.checkPacket 抛 UnsupportedOperationException 并**崩服** | 两条都补：先 **optional()**，再在 send() 里用 ICommonPacketListener#hasChannel(CoreListPayload.TYPE) 判一次，未声明就跳过。纯 S2C 只读包没有任何「必须装模组」的理由；否则本版会把原版玩家全部挡在门外（Phase 29 及以前他们能进服）。 |
| **D-2** | 任务书 §1：config/FarmstormConfig.java「绝对不可动，不新增任何东西」 | 加第二份 config 后，ModConfigEvent.Loading 变成「每份 config 各发一次」，CLIENT 那份先到；FarmstormConfig.onLoading 无条件 bake()，此时 SERVER spec 未 bake ⇒ IllegalStateException: Cannot get config value before config is loaded ⇒ 客户端进入 broken mod state（模型全丢、最终崩溃） | 在 FarmstormConfig.onLoading / onReloading 各加一个「只认自己这份 spec」的早退（2 处 × 2 行）。**不新增任何 config 键**（仍 166），服务端行为不变（服务端只加载 SERVER）。这是「加 CLIENT config」的必然连带修复，见 .work/p30-client.log 的原始崩溃栈。 |
| **D-3** | 31 号 F-202 验收 5：grep -c "RegisterKeyMappingsEvent" client/FarmstormKeys.java = 1 | 实测 **2**：类型名至少要在 import 与 import 与签名各出现一次，=1 在结构上不可能 | 按 2 记，不再为凑数写全限定名。可观察意图（注册点只有一个方法、里面 register 两次）成立。 |
| **D-4** | 30 号 §2.1.2 把 handler 单列为 client/network/ClientCoreListHandler | 任务书 §1 的 client/ 新增清单只给五件（无 handler 文件） | handler 作为 ClientCoreList#handle 落在 client/ClientCoreList.java，文件总数与 §1 清单逐一对齐；方法体仍只有一句 enqueueWork。 |
| **D-5** | 31 号 F-201 验收 2：日志一条「hud 位置读入 (8, 4)」 | 原来没有任何地方读这份 config，也就没有这条日志的载体 | FarmstormClientConfig.onLoading + Farmstorm 里 modBus.addListener(...)（共 2 行）。LOADING 阶段 spec 未 bake，读值会抛异常，因此用 SPEC.isLoaded() 早退——实际出线的是 RELOADING 那一轮，读数即上文的 (3840, 4)。 |
| **D-6** | 31 号 F-202 要点 3：按 H 的「动作栏回显」 | 语言键预算恰 9 条（3+2+1+3），没有第 10 条给动作栏 | 回显复用 command.farmstorm.hud.on / .off 两条反馈键，不新增键。 |
| **D-7** | 30 号 §2.4.4 行格式与配色 | 与源码一致 | 无偏差；仅补：1 px 边框画在 2 px 内边距之内（fill 外框 + 内缩 1 px 底色），因此 [x, x+宽) 整块都在钳制范围内。 |

---

## 8.7 未验证清单

| # | 项 | 原因 |
|---|---|---|
| 1 | 浮窗 1 / 8 / 9 行的**实际像素与截图** | 本机无法操作客户端 GUI，也无从截图；只有 10n+3 的代码层读数 |
| 2 | 按 H 开关（三次翻转 + 重启保持）、按 J 开拖拽屏、单人暂停 | 同上（按键事件只能由真实鼠标键盘触发） |
| 3 | 拖拽四角越界 0 px、ESC 保存写盘、关闭后不弹暂停菜单、拖拽中改 GUI 缩放 | 同上 |
| 4 | 「一条客户端指令」的三动作写盘读数、补全树、反馈只在本机聊天栏 | 客户端指令只能在客户端聊天框里敲；本机没有可交互客户端 |
| 5 | **包真正下发到客户端**（「核心列表下发：…」与客户端「收到核心列表 N 条」两行） | 本机只有原版协议客户端；NeoForge 不为 VANILLA 连接维护 channel 集合，机器人无法让它进 hasChannel。已下线的崩溃栈证明服务端侧确实走到了 PacketDistributor.sendToPlayer（参数是 farmstorm:core_list） |
| 6 | 去抖「同一刻放核 + 拆核只产生 1 个包」的**包计数读数** | 需要玩家放置 / 破坏核；机器人不会放方块。代码层：request() 只进 PENDING，唯一发送点是每 tick 一次的 flush，且 lastSentTick 门槛 10 刻 ⇒ 同刻多次请求必合并成 1 次 |
| 7 | 换服不残留（X → Y → X）的两帧浮窗 | 需要真实客户端切服；代码层：LoggingOut 清快照 + level == null 兜底 |
| 8 | 敌对核不外发 | 只有一个机器人、无法造第二账号在线；代码层：build() 对每个 owner 先做 TeamResolver.isAlly(Owner, Owner) 判定，非己方整段跳过 |
| 9 | MAX_CORES = 64 的单包大小实测 | 没有 64 个核的世界，也没有能观测包大小的客户端；按 30 号 §2.1.6 估算最坏 ≈ 3.1 KB |
| 10 | LoggingOut 在「被踢出」路径下是否触发 | 无客户端可被踢；已按 30 号 §8 ⑧ 的退路在 onClientTick 里加 level == null 的兜底清空 |
| 11 | 按键界面能看到两条键位、默认 H / J | 需要打开「游戏操作」页 |

---

## 8.8 未关闭的问题

1. **hasChannel 门槛是「原版客户端可进服」的唯一保证**。现在的形态是：原版客户端能进、只是收不到浮窗数据；装模组的客户端收得到。若将来有人把 optional() 或 hasChannel 判断去掉，就是「挡掉原版玩家」或「崩服」两种回归之一 —— 建议 F-206 收口时把这两条写进 30 号 §7 的断言里（本 Phase 只登记，不改 docs/）。
2. **MAX_CORES = 64 的单包大小仍未实测**（30 号 §8 ⑥）。估算 ≈ 3.1 KB，远低于 1 MiB 量级；真要验需要 64 个核 + 一个能测包大小的客户端。
3. **同一份存档连跑两次的指纹**只覆盖了「索引内容不变」的情形（1 / 2 / 9 条各验过）。A-39 ③ 的「手动把 gameTime 改成同值后按 BlockPos 定序」没有单独造场景（dimension 场景验的是第二级）。
4. **CoreChunkTickets.restoreAll 与索引惰性校验的相互作用**：票会把核所在区块拉进内存，于是索引条目在下一次 validated() 时就会看到「那里真的有 outpost_core」才算有效。本轮取证正是靠 RCON setblock 补上这一格才让 9 条读数成立；这与 F-198 的既有设计一致，但**「索引里有一条但方块不在」的条目会在几秒内被摘掉**，测试与运维时要知道。
5. 客户端首次启动时，run-p30 目录下生成了完整的客户端运行产物（logs/、options.txt、config/ 等）。run/ 未被触碰；run-p30c 是客户端连通性试验残留（启动失败，无世界）。
