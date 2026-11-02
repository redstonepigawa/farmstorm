# 任务书 · farmstorm v0.1.1 · Phase 30（F-199 ~ F-205）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> **这是全项目四个「第一次」：第一个自定义网络包、第一份 CLIENT config、第一批 `KeyMapping`、第一条客户端指令。** 每条都要求**先写最小用例跑通、再写正式逻辑**（30 号 §8 的 ① ② ⑤ ⑦ ⑧）。

---

## 0. 目标

左上角一块浮窗，一行一个**自己 + 队友**的全部核心（含未加载区块、含其它维度），形如 `#1 [overworld] (x, y, z) 自己`。

| 任务 | 标题 | 复杂度 | 依赖 |
|---|---|---|---|
| **F-199** | 1 个 S2C 只读包：payload / codec / 注册 / 快照 | L / 3.0 | — |
| **F-200** | 广播器：组装 / 三级排序 / 去抖 / 10 个时机 | L / 3.0 | F-199、F-189、F-187 |
| **F-201** | CLIENT config 3 键 + 3 条语言键 | M / 1.5 | — |
| **F-202** | 2 个 `KeyMapping` + 2 条描述键 | M / 1.5 | F-201 |
| **F-203** | 浮窗渲染 + 1 条「自己」标识键 | L / 3.0 | F-199、F-200、F-201 |
| **F-204** | 拖拽定位 `CoreHudDragScreen` | M / 1.5 | F-203、F-202、F-201 |
| **F-205** | 客户端指令 `/farmstorm hud` + 3 条反馈键 | M / 1.5 | F-201 |

**执行顺序**：`F-199 → F-200 → F-203 → F-204` 是主链；`F-201 → F-202 / F-205` 挂在一侧。

## 1. 边界

**允许新增**：`network/` 三件（payload、注册、broadcaster 按 30 号 §2.1 命名）、`client/ClientCoreList`、`client/CoreHudRenderer`、`client/FarmstormKeys`、`client/CoreHudDragScreen`、`client/CoreHudClientCommands`、`config/FarmstormClientConfig`。
**允许改**：`Farmstorm.java`（注册点，`registerConfig` 的 `ModConfig.Type.SERVER` 之后加一行 CLIENT）、`client/FarmstormClient.java`（注册渲染 / 按键 / 客户端指令）、`datagen/ModLangProvider.java`（**+9 条**）、`blockentity/OutpostCoreBlockEntity.java`（**只追加广播那一段**）、`event/FarmstormGameEvents.java`（`+4` 个玩家事件 + `ServerTickEvent.Post`）、datagen 生成的 `src/generated/resources/assets/farmstorm/lang/*.json`。

**绝对不可动**：
- `config/FarmstormConfig.java` —— **不新增任何东西**（CLIENT 3 键必须进独立的 `FarmstormClientConfig`）
- `farmstorm-server.toml` 必须仍 **166 键 / 58 组**，且**不出现** `hud` 段
- 注册面仍 **93**（网络包 payload / KeyMapping / CLIENT config / 客户端指令四者都不进 `BuiltInRegistries`）
- `outpost/OutpostCoreIndex.java`、`farmland/FarmlandLedger.java`、`team/TeamResolver.java`（Phase 29 的成品，只读）
- `docs/` 任何文件；`build.gradle`、`settings.gradle`、`.tools/`；`.work/v011-*.md` 四份台账
- 服务端侧不得引用任何 `net.minecraft.client.*` 类型（否则专用服务器 `NoClassDefFoundError`）

## 2. 必读

1. `docs/zh/32-PROMPT-MAIN-0.1.1.md` 全文
2. `docs/zh/33-PROMPT-TASKS-0.1.1.md` §0.2、**§5（Phase 30 全节，含 11 条翻车点）**、§8
3. `docs/zh/31-TASKS-0.1.1.md` F-199 ~ F-205 详情（约 569~704 行）
4. `docs/zh/30-TECH-0.1.1.md` **§2.1（网络包全节，144~269 行）**、**§2.4（客户端构件全节，528~681 行：浮窗四条前置、行格式、`10n + 3`、配色、拖拽、客户端指令）**、§4.2（client 3 键）、§7 的 #6 ~ #15 与 #29、§8 的 ① ② ⑤ ⑥ ⑦ ⑧ 六条待验证 API
5. `docs/zh/28-DECISIONS-0.1.1.md` D-142 ~ D-144、D-146
6. `.work/v011-BRIEF.md` §1.4 / §1.5 + `.work/v011-verdicts.md`（A-34 / A-41）

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`，基线 exit 0。**唯一入口，禁止直接调 gradle** |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服 | `run-p30`（复制 `run/eula.txt` + `run/server.properties`；`enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），`FS_RUN_LOCK=run-p30 cmd /c ""<repo>\.tools\gw.cmd" runServer -PfsRunDir=run-p30" > .work\p30-server.log 2>&1`，RCON `stop`。**专用服务器必须能起、无 `NoClassDefFoundError`** |
| 客户端 | 本机**无法操作客户端 GUI**（按键、拖拽、截图）。浮窗渲染 / 拖拽 / 按键类验收一律「未验证」+ 代码层证据 |
| `run/config` | 目前**没有** `farmstorm-client.toml`（本 Phase 第一次生成） |
| A-40 | 已关闭：`verify_project.py` 此刻应已 `FAIL 0`、config 段 166、退出码 0 |

## 4. 调度者已定的口径

1. 文档与源码冲突 → **以源码为准**，登记偏差表；不许改 `docs/`。
2. 已裁决未回灌条目按 `.work/v011-verdicts.md` 执行并注明 A-xx：
   - **A-34**：浮窗「8 行上限」的 `+N` **不占**这 8 行 ⇒ **8 行核心 + 另加 1 行**溢出 = 最高 **9 行**；总高 8 行 **83 px** / 9 行 **93 px**。
   - **A-41**：版式**以 29 号的分解为准**（行高 9 + 行距 1 + 内边距 2），等价于面板高 `10n + 3`。
3. **禁止任何 git 写操作**。只许 `git status` / `git diff` 取证；提交由调度者做。
4. 客户端 GUI 类验收标「未验证」+ 原因；服务端 / 静态能查的照做。
5. 遇到做不下去的：停下，写清阻塞点与已做部分。

## 5. 红线与三个数

- 红线只剩「无 Mixin」。**网络包红线已整体废除**：本版有且只有 **1 个 S2C 只读包 `farmstorm:core_list`**，客户端**不回包**。「0 个自定义网络包」是 0.1.0 的旧口径，**不许写成通过条件，也不许写成「本版允许 1 个」**。
- 本 Phase 结束时：注册 **93**、server config **166 键 / 58 组**（**不出现 `hud` 段**）、client config **3 键**（独立一份 `farmstorm-client.toml`，不计入 166）、语言键 **323 × 2**（`314 + 3 + 2 + 1 + 3`），`farmstorm.configuration.` 前缀恰 **169**（`166 + 3`）。

## 6. 实现要点（逐条）

### F-199（第一个自定义网络包）
- payload：`List<Core>` + **单独发的 `int total`**（`total` 是**截断前**的总数，不许写成 `cores.size()`）
- 4 元 `StreamCodec`；`MAX_CORES = 64`
- `Type` 必须走 `Farmstorm.id("core_list")`，注册名必须是 **`farmstorm:core_list`**（不许 `CustomPacketPayload.createType("core_list")` 落进 `minecraft:`）
- `playToClient` 注册；客户端 handler **只有一句 `enqueueWork`**，方法体里**不许**直接引用客户端类（否则专用服务器 `NoClassDefFoundError`）
- 客户端 `volatile` 快照
- payload 只读：客户端不发包，`grep -rn "playToServer|sendToServer"` = **0**

### F-200（广播器）
- 候选按 `gameTime` → 维度 id → `BlockPos#compareTo` 排序，**比较键只从索引读**（不 `getChunk` / 不 `getBlockEntity`）
- `MIN_INTERVAL_TICKS = 10` 去抖；`RECONCILE_INTERVAL_TICKS = 100` 对账
- **10 个推送时机逐条挂**（登录 / 放置 / 摧毁 / 换维度 / 队伍变更 / 登出清三条记录 …按 30 号 §2.1 的清单）
- 只发**自己 + 队友**的核；敌对核不外发

### F-201（CLIENT config）
新增 `config/FarmstormClientConfig`：`ModConfig.Type.CLIENT`，`[hud]` 三键 `enabled = true` / `x = 4`（0 ~ 3840）/ `y = 4`（0 ~ 2160）；写盘走 `ConfigValue#set` + `SPEC.save()`；语言键 **+3**。

### F-202（2 个 KeyMapping）
`client/FarmstormKeys`：开关 **H** / 拖拽 **J**；分类**复用 `KeyMapping.CATEGORY_GAMEPLAY`**（**不自建分类**，否则多一条 `key.categories.*`）；在既有 `ClientTickEvent.Post` 里 `consumeClick()` 消费；语言键 **+2**。

### F-203（浮窗渲染）
`client/CoreHudRenderer`：四条前置早退（含「0 核不画」）；行格式 `#%d [%s] (%d, %d, %d) %s`；面板高 `10n + 3`（8 行 83 px / 9 行 93 px）；宽度按 `font.width` 实测；绘制前按屏幕尺寸钳制；五种配色；`+N` 用**纯数字字符串拼接**（`"+" + (total − 8)`），**不新增语言键**；语言键 **+1**（「自己」标识）。

### F-204（拖拽）
`client/CoreHudDragScreen`：`isPauseScreen()` 返回 **`true`**；`mouseClicked` 落在面板内才拖、`mouseDragged` 写预览并钳制、`mouseReleased` **不写盘**、`onClose()` 才写 config；编辑态例外让 `hideGui` / `screen != null` 两条早退**失效**（否则按 J 之后看不到自己在拖什么）。

### F-205（客户端指令）
`RegisterClientCommandsEvent` 注册 `hud <on|off|reset>`，三个子命令**不带** `requires`；`reset` 只复位位置**不动 `enabled`**；反馈 `sendSuccess(..., false)`（本地显示、不广播）；语言键 **+3**。

## 7. 验收标准（= 33 号 §5.4 的 8 条，逐条给读数或「未验证」）

1. **一个包、不回包**：`grep -rln "CustomPacketPayload" src/main/java` 恰 **1** 个文件；`grep -rn "playToServer|sendToServer"` **0 命中**；专用服务器完成启动、无 `NoClassDefFoundError`；`latest.log` 注册行含 `farmstorm:core_list`；`grep -c "MAX_CORES"` ≥ **2**。
2. **下发与排序**：单人 1 核 → 日志「核心列表下发：<玩家> 1 条（total=1）」；先放 A(t=100) 再放 B(t=200) → A 在 B 前；同一份存档重启两次 → 「核心比较键指纹」两行完全相同；同一刻连锁放核 + 拆核 → 只产生 **1** 个包；从服务器 X 退出去 Y → Y 上浮窗立即消失、回 X 再出现。
3. **跨维度与满员**：主世界 1 核 + 下界 1 核 → 2 行、行首分别 `overworld` / `the_nether`；9 核 → **8 行核心 + 1 行 `+1` = 9 行 / 93 px**，8 核 → 8 行 / **83 px** / 无 `+N`。
4. **一份新 toml**：首次起服/客户端后读 `config/farmstorm-client.toml` —— `[hud]` 恰 **3 键**、值 `true` / `4` / `4`；**服务器不生成**这一份；`farmstorm-server.toml` 里没有 `hud` 段；手改 `x = 5000` 重启后被钳到 **3840**。
5. **两个键位**：按键界面能看到两条、默认 **H / J**；`grep -c "key.categories.farmstorm"` = **0**。
6. **拖拽**：`hud.x` / `hud.y` 等于落点坐标、重进不变；越界部分 **0 px**；关闭后**不弹**暂停菜单。
7. **一条客户端指令**：`/farmstorm hud off|on|reset` 各一次分别写 `enabled = false` / `true` / `x = y = 4` 且 `enabled` 不变；补全 `/farmstorm ` 只有 `hud`，其下只有 `on` / `off` / `reset`；反馈只出现在本地聊天栏；`grep -c "sendSuccess"` = **3** 且第二参数都是 `false`。
8. **三个数没动**：注册 **93**；server config **166 键 / 58 组**；两份 lang 各 **323** 键、`farmstorm.configuration.` 恰 **169**。
9. `.tools\gw.cmd build` 退出码 **0**（最终）。
10. `git status --porcelain` 只落在 §1 允许的新增/修改文件 + 证据 + `run-p30*` 目录。

## 8. 必须回报的证据

1. `git diff --stat` 原文；新增文件逐个点名（`network/` 三件、`client/` 五件、`config/FarmstormClientConfig`）与两处注册点（`Farmstorm.java`、`client/FarmstormClient.java`）；`ModLangProvider` 的 9 条新增。
2. `.tools\gw.cmd build` 退出码；`verify_project.py` **完整输出** + 退出码。
3. 两处 `grep -rln` / `grep -rn`；三处 `grep -c`（`MAX_CORES`、`key.categories.farmstorm`、`sendSuccess`）；专用服务器启动日志的注册行原文。
4. 浮窗 1 / 2 / 8 / 9 行的四组读数（能跑就算，跑不了标「未验证」）；日志里「核心列表下发」与两行指纹原文。
5. `farmstorm-client.toml` 的三键读数与钳制读数。
6. **文档-源码偏差表**。
7. **未验证清单**（每条一行 + 原因）。
8. **未关闭的问题**（含 `MAX_CORES = 64` 的单包大小、`LoggingOut` 在「被踢出」路径下是否触发）。

## 9. 证据落盘

完整输出写进 `.work/TASK-V011-P30-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 10. 完成定义

§7 的 10 条全部有读数或有「未验证」+ 原因；回报含 8 项。**特别注意**：本 Phase 极易顺手破坏「三个数」，收尾必须亲自数一遍。
