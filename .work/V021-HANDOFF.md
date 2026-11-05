# v0.2.1 交接文档（给接手的新会话）

> 写于 v0.2.1 第 43 个任务落地之后。当前 **HEAD = `0acaa08`**，`git describe` = **`v0.2.0-10-g0acaa08`**，工作树 **干净**（`git status` 里只有 `.work/` 的历史未跟踪文件）。
> 作者的口径是「子代理干活、调度者只做派发与复核」。**接手者继续当调度者，不要自己写实现代码。**

---

## 1. 任务是什么

把 **farmstorm v0.2.1** 从文档实现成可发布版本，最后打 tag 发 Release。

| 项 | 值 |
|---|---|
| 上游文档 | `docs/zh/40`~`45`（`40-DECISIONS / 41-DESIGN / 42-TECH / 43-TASKS / 44-PROMPT-MAIN / 45-PROMPT-TASKS`，各有一份 `docs/en` 翻译） |
| 规模 | **6 个 Phase（40~45）/ 20 个任务（F-247~F-266）/ 27.5 人日裸（含 25% 缓冲 34.375）** |
| 判据 | **67 条** = 31 条技术验收（42 号 §8）+ 36 条设计边界（41 号 §12） |
| 阅读顺序 | 40（唯一真相源）→ 41 → 42 → 43 → 44（主提示词）→ 45（分阶段任务） |
| 关键编号 | 决策 **D-173~D-186** / 任务 **F-247~F-266** / Phase **40~45** / 里程碑 **M36~M41** / 裁决 **A-70~A-81** / 风险 **RT-51~RT-60b** |

**本版五条需求（R1~R5）**：R1 培养器周期补绑 · R2 核心 54 格 tooltip 全覆盖 · R3/R4 AI 干预（第 13 类附件 `farmstorm:ai_suspend`）· R5 陷阱伤害倍率 ×3。

---

## 2. 作者本轮拍板的四项（不要重问）

| # | 问题 | 定为 |
|---|---|---|
| F-1 | 交付终点 | **全部 6 个 Phase + 收尾**，含 `dist/` jar、README 更新、tag `v0.2.1` 与 GitHub Release |
| F-2 | 并行度 | **严格串行**，一个 Phase 一个子代理 |
| F-3 | 渲染类验收 | 服务端 / 协议层能验的照做；**渲染与听感类标【未验证】并列表**，不许编读数 |
| F-4 | 文档冲突 | **以源码 / 40 号 / 已有裁决为准**，子代理自行适配 + 登记偏差；`docs/40`~`45` **全程不改** |

作者原话（可直接引用）：「开工后遇到无法解决的问题或者需要我决策的问题时处理方式和之前一样」= **能跳过就跳过，最终统一列表；跳不过就停下来汇报**。

---

## 3. 必须知道的七个环境坑（都是踩过的）

1. **构建入口**：本会话里 `cmd /c ""D:\project_farm\.tools\gw.cmd" build"` 会被 pwsh 参数解析拆坏（cmd 报 `'"...\gw.cmd build' is not recognized`）。改用 **`& 'D:\project_farm\.tools\gw.cmd' build`**（同一入口，实测 exit 0）。纯 cmd 窗口里原式正常。**禁止直接调 gradle。**
2. **toml 的 BOM 陷阱**：手工写 `run*/config/farmstorm-server.toml` 必须 **UTF-8 无 BOM**（`UTF8Encoding($false)`）。带 BOM 会让 NightConfig 报 `Invalid bare key` 并把配置**重建回默认**——Phase 41 踩过一次，那一轮的三组读数全部作废。本机 pwsh 是 5.1，`Set-Content -Encoding utf8` 会带 BOM。
3. **行号陷阱**：`OutpostCoreBlockEntity.java` 用默认 `Get-Content` 会读成 **668 行**（实际 **782+**）。取行号一律用 `[IO.File]::ReadAllLines($p, [Text.Encoding]::UTF8)`。
4. **python 不在 PATH**：用 `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`，先设 `$env:PYTHONIOENCODING='utf-8'`。
5. **起服必须用独立目录**：`run-p4x`（复制 `run/eula.txt` + `run/server.properties`；`enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），加 `FS_RUN_LOCK=run-p4x` + `-PfsRunDir=run-p4x`。**永远不要在 `run/` 里起服**。`run-*/` 已被 `.gitignore` 覆盖。
6. **git push 的两道坎**：① `git-credential-manager` 会拉起 `sh.exe` 建命名管道，被沙箱拦（`couldn't create signal pipe, Win32 error 5`）⇒ 用 `gh auth token` 内联 URL + `-c credential.helper= -c core.askPass=`；② 到 github.com:443 的连接**间歇超时**，需要重试（实测最多 4 次才成）。令牌在输出里要替换成 `***`。
7. **工具假绿（A-66）**：`tools/verify_project.py` 在 **F-261 之前**期望集仍是 0.2.0 口径 —— 它打 `已核 registry 对象: 97 / 97` 且 `FAIL 0` 就退出 0，但**还没核 `[trap]` 的那 1 个键与语言键 +1**。**F-261 之前任何判据都不许引用它的 `FAIL 0`。**

---

## 4. 已完成（Phase 40~43，10 个提交）

```
0acaa08  F-257~F-260   陷阱伤害倍率（[trap] 4 键 + 第 9 参与乘点①）+ 桑吉尔蔗 22.5
1934c8f  F-255+F-256   Q弹三条路径 1 刻内恢复 + tooltip 覆盖 90 槽（F-256 零改动）
9cbb4e8  F-254         震动豆范围内所有非玩家生物 NoAI 80 刻
9c6b21a  F-252+F-253   第 13 类附件 farmstorm:ai_suspend + MobAiSuspension
902fd8e  F-251         isMountMode 可见性 private → public
88db378  F-249+F-250   补绑相位门与写入边界 + serverTick 接线 + 注释同改
910dee7  F-248         util/AreaBlockEntities#collect
52497c4  F-247         版本号 0.2.1 与两支 .cmd 的 6 处版本串
26cefd0  docs(v0.2.1)  40~45 六份文档 + plan0.2.1 + README「下一版文档」   ← 基线
224040a  docs(v0.2.0)  Release v0.2.0 正文入库                          ← 上一版收尾
```

### 已落地的内容与关键读数

| Phase | 内容 | 判决性读数 |
|---|---|---|
| **40** F-247 | 版本号 0.2.1，两支 `.cmd` 的 **6 处**版本串（A-70：`:5` 抬头 ×2 + 三个 jar 名 + `gradle.properties:11`） | `0.2.0` 命中 **0**；`0.2.1` 命中 **1+3+2 = 6**；jar 内 `version = "0.2.1"` |
| **41** F-248~250 | `util/AreaBlockEntities#collect`（只走 `getChunkNow`）+ `REBIND_INTERVAL_TICKS=100` + `rebindDue`/`rebindNearby` + `serverTick` 独立门（**在粒子门之前、不 return**） | 粒子间隔 **40 与 1200 下都在 100 刻内补上**（成对判决）；满 16 即停；无主核不补；`pruneCultivators(` 调用点仍 1、`removeCultivator(` 仍 0 |
| **42** F-251~256 | `isMountMode` 提升可见性；第 13 类附件 `farmstorm:ai_suspend`（**不计入 97**，A-73）；`MobAiSuspension`（进记原值 / 出按原值恢复 / 五条路径）；震动豆 80 刻；Q弹三路径 | 震动豆**恰 80 刻**回原值；强杀重启后**同刻（≤1 刻）**恢复；Q弹 `sources` 恒 2、原值不变；mount 下层「恢复」**先于**「挂坐骑」；启动日志仍 `97 → OK` |
| **43** F-257~260 | `util/Hostility` + `FarmstormExplosion` 8 参重载 / 9 参构造 / 乘点①（A-72 同一提交）+ `[trap]` 组 4 键（**config 窗口开**）+ 7 处调用只有 1 处传第 9 参 + 桑吉尔蔗乘点② | 胡罗贝→僵尸 **7.0 / 21.0 / 70.0**；桑吉尔蔗 **7.5 / 22.5 / 75.0**；非陷阱 4 处三次**完全相同**（6.0 / 2.0 / 0.0 / 8.0）；`[trap]` 段内 **4** 键、`b.push(` 仍 **60**、行首 `.define*` **176** |

### 当前代码面事实

| 项 | 值 |
|---|---|
| `modal_version` / `gradle.properties:11` | **0.2.1** |
| `build/libs` | `farmstorm-0.2.1+mc1.21.1.jar` + `-sources.jar` |
| Java 文件数 | **149** |
| 注册面 | **97**（第 13 类附件 `ai_suspend` 走 `ATTACHMENT_TYPES`，**不计入**） |
| 网络包 | **2**（`core_list` + `cultivator_link`），协议串 `"2"`，本版不新增 |
| 红线 | `@Mixin` / `mixins.json` 全仓 **0** |
| config 源码面 | `b.push(` **60**、`b.pop()` **60**、行首 `.define*` **176**（`[trap]` 4 键已在） |
| 语言键 | 两份各 **337**（`[trap]` 的 1 条已加，**总数收口在 F-263**） |

> ⚠️ **一处需接手者核实的现象**：`run/config/farmstorm-server.toml` 现在**已含 `[trap]`、60 段**，而 Phase 43 的回报说「run/config 仍是旧值、无 damageMultiplier」。**不要依赖现值**：F-262 的做法是**删掉它再起服重生成**，以重生成结果为准。

---

## 5. 未完成（Phase 44、45、收尾）

### Phase 44 · 数值面与工具收口（F-261~263，2.5 人日）——三处关窗

**必须按这个顺序**（45 号 §5 的前置断言）：

| 顺序 | 任务 | 做什么 | 关窗判据 |
|---|---|---|---|
| 1 | **F-261** | 工具期望面：`tools/verify_project.py` 加 `[trap]` 的解析器（从 42 号 §4.1.1 那一行**现算**，**不写第二份手写清单**）+ 并入并集 | 解析器返回 **1** 个键；`grep -c 'trap\.damageMultiplier'` ≥ **2**；**文档 + 解析器同一提交**（A-75） |
| 2 | **F-262** | config 总账：**删掉 `run/config/farmstorm-server.toml` 重新起服重生成** | **176 键 / 60 组**（`b.push(` 60、`b.pop()` 60）；`[trap]` 4 键 |
| 3 | **F-263** | 语言侧：两份 lang 各 **338**，`ModLangProvider.java:13-16` 的构成注释改到与键集一致 | 键集逐字相等；`farmstorm.configuration` 从 **178 → 179** |

**窗口纪律**：三处关窗**各自只断言一次自己的总数**；关窗**必须早于 F-264**（否则验收拿的是注释还停在上一个版本数的规格去核）。

**A-75 契约**：新解析器四条契约 + 「文档 + 解析器同一提交」。
**A-74**：两个数值面窗口的开闭任务钉死（config 侧 F-258 开 / F-262 关；工具 F-258 开 / F-261 关；语言侧 F-258 开 / F-263 关）。

### Phase 45 · 全量验收与签字（F-264~266，7.5 人日）

| 任务 | 做什么 |
|---|---|
| **F-264** | 42 号 §8 的 **31 条**技术验收，写 `.work/v021-verify/acceptance.md`（31 行 + 表头） |
| **F-265** | 41 号 §12 的 **36 条**边界，写 `.work/v021-verify/boundaries.md`（36 行 + 表头） |
| **F-266** | 红线、注册面与上游一致性：`.work/v021-verify/signoff.md`（8 行签字表 + 20 行任务核对表 + 第 13 类登记） |

**F-264~266 一律不改码**，只产出记录；`git status` 只允许出现 `.work/v021-verify/`。

### 收尾（不在 Phase 内）

1. `& 'D:\project_farm\.tools\gw.cmd' build` → 把 `farmstorm-0.2.1+mc1.21.1.jar` 与 `-sources.jar` 复制到 `dist/`（`dist/` 被 `.gitignore` 排除，不进 git）。
2. 更新 `dist/安装说明.txt` 与 `dist/install-notes.txt` 到 0.2.1（版本行 / 本版改了什么 / 从 0.2.0 升级必看 / 已知限制 / 两支 jar 的字节数与 SHA256 / 素材与非商业声明）。
3. 更新 `README.md`：版本行去掉「v0.2.1 只有文档，代码未实现」、把「下一版文档」并入现行文档、加 v0.2.1 到版本历史、数字快照改成 97 / 176·60 / 338×2 / 67 条验收、已知限制换成本版内容。
4. 提交 → `git tag -a v0.2.1` → push `main` + `refs/tags/v0.2.1`（用第 3 节第 6 条的绕过方式，并重试）。
5. `gh release create v0.2.1 dist/<两支 jar> dist/install-notes.txt --title "Farmstorm 农场风云 v0.2.1" --notes-file .work/RELEASE-v0.2.1.md`。
   **Release 正文必须复制 README 的「素材来源与非商业声明」一节**（作者硬要求：jar 一旦分发就无法补正说明）。

---

## 6. 需要作者决策的 4 项（Phase 43 提出，已按「以源码为准」放行并登记）

1. **43 号 F-260 的验收自相矛盾**：它要求「`ZangiefCaneTrap` 仍 **183** 行、`:174` 行号不变」，但 42 号 §2.5.3 要求在 `:111-118` 插入乘点 —— 只加一个 import 就 ≥184 行。实测 **189 行**、Monster 过滤在 `:180`（**内容逐字未变**）。已按 42 号执行。
2. **「胡罗贝对玩家恒 7.0」不成立**：玩家的 raw 伤害恰好压在 cap 7 的边界，实测在 **5.0~7.0 抖动**，但**上限恒等于 7.0**，且倍率 10.0 时仍是 **5.5**（若被乘应是 55 ⇒ 证明玩家伤害**不乘倍率**是设计如此）。若要求「恒 7.0」，得换一个不贴边的探针。
3. **`molotov` 落点 `damageCap = 0.0F`**，直接伤害恒 0（伤害只来自火）。把它与 `melon_grenade` 并列为「非陷阱伤害读数」没有意义，已改用 `melon_slice_bomb` / `ubi_server`。
4. **「桑吉尔蔗对牛 / 村民 = 7.5」路径不可达**：`findTrigger` 只返回 `ServerPlayer` 与 `Monster`，而 `Monster` 全实现 `Enemy` ⇒ 非敌对生物永远进不了 `monsterBranch`。**建议这条改成代码层判据**。

### 另有 6 条偏差已登记（不改 `docs/`）

`(float) Double` 编译失败改 `.floatValue()`；僵尸自带 `generic.armor=2` 会把 21.0 削成 20.664（探针必须显式 `armor=0`）；`trap.immunityTicks=100` 造成「同一玩家 5 秒内踩第二颗陷阱不触发」的测量陷阱；`0b` 与「缺键」在 NBT 上**同态**；`getData` 会把默认值写进 map（故只认 `sources != 0`）；`EntityJoinLevelEvent` **实测覆盖**区块载入（42 §8⑥ 的退路未启用）。

---

## 7. 接手者的工作方式（照这个跑）

1. **一个 Phase 一个子代理**，串行。任务书写进 `.work/TASK-V021-P<nn>.md`，证据落 `.work/TASK-V021-P<nn>-evidence.md`。
2. 任务书必须含：目标 / 边界（可动与不可动文件清单）/ 必读（精确到文档段落）/ 环境事实 / 已定口径 / 逐任务实现要点 / 验收标准 / 必须回报的证据 8 项。
3. **子代理禁止任何 git 写操作**；只许 `git status` / `git diff` / `git log` 取证。**提交由调度者做**，且**按任务分段提交**（37/43 号的「一个任务一个提交」——同文件无法切时合并并在提交信息里写明原因）。
4. 每个 Phase 后**调度者亲自复跑闸门**（build 退出码、jar 版本行、关键 grep 计数、起服读日志），**不采信子代理自述**。
5. 每条判据要求子代理**改前先跑一遍记实测值**，改后再跑一次。
6. 渲染 / 听感 / 需第二客户端 → **【未验证】+ 代码层证据**，不许编读数。
7. 已落盘的三份参考：`.work/V021-DISPATCH.md`（派发计划）、`.work/TASK-V021-P4*.md`（四份任务书）、`.work/TASK-V021-P4*-evidence.md`（四份证据，可当格式样板）。

---

## 8. 一句话现状

**Phase 40~43 已完成并提交（10 个提交，工作树干净，build exit 0，注册 97、Mixin 0、网络包 2）。剩下的就是 Phase 44 的三处关窗、Phase 45 的 67 条验收与签字、以及打 tag 发 Release。**
