# 31 号 v0.1.1 任务拆分与开发大纲（Task Breakdown & Roadmap）

> 项目：农场风云 / Farmstorm · 版本：**v0.1.1**（MC 1.21.1 / NeoForge 21.1.213 / Java 21）
> 依据：`docs/zh/28-DECISIONS-0.1.1.md`（D-124 ~ D-147，24 条，唯一真相源）+ `docs/zh/29-DESIGN-0.1.1.md`（10 条需求 R1 ~ R10、39 条边界 B-01 ~ B-39）+ `docs/zh/30-TECH-0.1.1.md`（§3 改动面 44 行、§4.1 config 58 组 166 键、§4.2 client 3 键、§4.3 lang 323 键、§7 验收 38 条、§8 待验证 API 8 条、§9 差异 9 条、RT-31 ~ RT-41）+ `.work/v011-BRIEF.md`（事实卡）+ 裁决 **A-30 ~ A-41**（`.work/v011-verdicts.md`，12 条，本文原样收录并逐条落到任务）。
> 本文把规格拆成 **37** 个「一次可独立提交、可独立验证」的任务，编号 **F-176 ~ F-212**，Phase **26 ~ 31** 与里程碑 **M22 ~ M27** 一一对应，裁决编号接 A-41 后新立 **A-42 ~ A-56**（15 条；其中 **A-44 已作废，由 A-47 取代**），全文共收录 **27 条（A-30 ~ A-56）**。
> 覆盖：30 号 §3 的 **44 行**改动面（+ 补记 2 行，A-42 / A-55）、**+0 注册对象**（12 类逐类点名）、**166 + 3** config、**1 张 PNG**（就地替换、新增 0）、**RT-31 ~ RT-41 十一条**、30 号 §8 的 **8 条**待验证 API；未覆盖 0 条（逐表见 §6）。
> 本文件只读上游：不改 `docs/zh/00` ~ `30`、不改 `docs/en`、不改 `src/`、不改 `.work/` 的三份台账；只新建本文件。

---

## 0. 阅读约定

### 0.1 任务粒度（五条）

一个任务 = 一次可独立提交、可独立验证的改动。判据沿用 `25-TASKS-0.1.0.md §0.1` 的口径，逐条照抄、不改写：

1. **一个任务 = 一次可独立提交、可独立验证的改动。**
2. **提交后 `.tools\gw.cmd build` 退出码 0**，且不依赖同批次中尚未提交的任务。
3. **每个任务自带至少一条能在 §0.3 列出的位置观察到的验收断言**；断言里必须出现具体数值、键名或路径。
4. **回滚该提交不会让其它已完成任务失效**（回滚只影响依赖它的后续任务，依赖关系写在「依赖」字段里）。
5. **不跨 Phase 边界**：一个任务的交付物全部落在它所属 Phase 的名称范围内；跨 Phase 的前置只写在「依赖」列里。

一条**本文补充**的边界（对应 25 号 §0.1 的第 5 条）：任务只动 30 号 §3 的 44 行、补记 2 行（A-42 / A-55）、两处 schema 变更（`FarmlandLedger` v2、`OutpostCoreIndex.Entry.gameTime`）、2 处删除（`farmland` config 组、服务端指令树）、语言键 +10 / −2、1 张 PNG、以及本版新增的 5 个客户端类 + 1 个 payload + 1 个 ticket 类。表外的改动一律先改 28 号再拆任务。

三条跨任务的硬约定（写在这里，任务里不再重复）：

- **A-37**：任何验收都**不许**写「某方法的调用点数 = N」这类描述动工前状态的判据（例如「`FarmlandLedger.remove` 调用点 = 1」）。绑定制引入「摘绑定」之后写入口不再唯一，这类断言实现后必红且无法判断是缺陷还是预期。判据一律换成可数的**文件数 / 键数 / 命中数 / 日志行 / 存档字段**。
- **A-40**：`tools/verify_project.py` 在 F-177 改完之前，它的「FAIL 0」不是通过判据。F-177 ~ F-191 之间的任务**一律不引用**该工具结论；只有 F-192 起的任务可以引用（F-192 是 `[farmland]` 段消失、期望集与 toml 首次对齐的那一点）。
- **A-42**：30 号 §3 的 44 行**漏列**了「删 `farmland.limitPerPlayer` 整组」这一动作面（它在 30 号 §2.3 第 3 行与 §4.1 的删表里有，但不在 44 行里）。本文在表 A 末尾以**补记行**登记它，44 行不重编号，总动作面按 47 个算（44 + 3）。

### 0.2 复杂度与工作量换算（S/M/L → 0.5/1.5/3.0）

换算口径与 `25-TASKS-0.1.0.md §0.2` 完全一致，**一字不改**，本版不调整：

| 复杂度 | 含义 | 折算人日 |
|---|---|---|
| S | 单文件或纯数据；≤ 1 个类，或 ≤ 20 个 JSON | **0.5** |
| M | 2 ~ 4 个文件，跨 1 ~ 2 个包，或新增 1 个完整行为实现 | **1.5** |
| L | ≥ 5 个文件或跨 3 个包，含运行期状态机 / 序列化 / 事务 / 渲染管线 | **3.0** |

本版 37 个任务按此分布：**S 5 个 / M 22 个 / L 10 个**，折算合计 `5 × 0.5 + 22 × 1.5 + 10 × 3.0 = 2.5 + 33.0 + 30.0 = 65.5` 人日（不含缓冲）。

三处**不按「改一行」估时**的说明（与 25 号 §0.2 末段的处理同一条纪律）：

1. **凡 schema / 序列化改动按上一档计**：F-188（`FarmlandLedger` v2）按 L 计 **3.0** 人日；F-189（`OutpostCoreIndex.Entry.gameTime`）只加一个字段，仍按 M 计 **1.5** 人日——它的成本在「老档缺失取 `Long.MIN_VALUE`、不为比较加载区块、两次启动可复现」这三条上，不在字段本身。
2. **凡「首次」按上一档计**：F-199（全项目第一个自定义包）、F-202（第一个 `KeyMapping`）、F-201（第一份 CLIENT config）、F-205（第一条客户端指令）都是本仓第一次，按 L / M 计而不是按「加一个类」计 0.5。
3. **凡验收任务不按「跑一遍」估时**：F-208 ~ F-211 每条要覆盖 19 ~ 20 个条目，每个条目至少一次动作 + 读音 + 记录，按 M 计。

### 0.3 验收标准的写法

每条验收必须能在下列位置之一被观察到：

1. `run/logs/latest.log` 的断言输出（下称 `latest.log`；25 号 §0.3 写作 `logs/latest.log`，指同一文件）；
2. 游戏内 `/execute` `/data get` `/setblock` `/give` `/effect` 的返回值；
3. F3 读数（`Light` / `Chunk` / 实体列表）；
4. `config/*.toml`（`farmstorm-server.toml` / `farmstorm-client.toml`）或 `src/generated/resources` 的文件内容；
5. `build/libs` 与 `dist` 的产物（含 `jar tf` 的条目清单）；
6. `grep` 计数（文件数 / 命中数 / 键数）；
7. PNG 字节比对（尺寸 + sha256）。

**禁止**「表现正常」「手感良好」「符合预期」这类不可观测表述。四条写法约定（沿用 25 号 §0.3）：

1. 断言里必须出现具体数值、键名或文件路径（例：「`[pumpkin_turret]` 段恰 5 键、`fireIntervalTicks = 40`」，不写「配置正确」）。
2. 手测类断言必须写清**对照场景**（例：「正 / 负对照各播一次 `stone.break` 与 `grass.break`」）。
3. 引用上游自检项时写编号（例 `30 号 §7 #17`），不重抄判据。
4. 少数断言用结构性证据（`grep` 计数、`git diff` 的范围、`jar tf` 条目、PNG 的 sha256）——它们指向文件的确定内容，与上面七类同等有效，且只在无法用那七类判定时使用。

本文件里的 `grep` 一律指「对指定文件的字面量计数」，Windows 下用 `rg -c` 等价执行，结果相同。

### 0.4 本版特有的一条边界：A-40 的时序约束

**A-40 的结论**：`tools/verify_project.py:222-237` 现在用四份**旧文档**（`01-DESIGN` / `12-TECH-0.0.2` / `18-TECH-0.0.3` / `24-TECH-0.1.0`）现算 config 期望键集再与 `run/config/farmstorm-server.toml` 比；本版删 1 键（`farmland.limitPerPlayer`）、增 1 键（`pumpkin_turret.bulletSpeed`）之后，期望集必然对不上，工具会报 FAIL。**在它按本版口径改完之前，「工具全绿」不可用。**

本文据此定三条排期规则：

1. **改造任务是 F-177，排在 Phase 26 第二位**，早于任何引用工具结论的任务。
2. **中间态窗口 = F-177 落地 ~ F-191 提交**。窗口内工具 §1 段必然是红的，且**只允许出现两条键级差异**：缺 `pumpkin_turret.bulletSpeed`、多 `farmland.limitPerPlayer`。这两条差异本身就是窗口期的正向判据（见 F-177 验收第 3 条）：多报或少报一个键名，都说明解析器写错了。F-182 落地（`bulletSpeed` 进 toml）之后，「缺」的那条消失，只剩「多」的那一条，直到 F-192 删掉 `[farmland]` 段。
3. **窗口内所有任务不引用工具结论**（A-40 的原文允许的第二种写法）。窗口在 **F-192** 关闭：`[farmland]` 段消失、删旧 toml 重新生成之后，`结论：FAIL 0 项`、退出码 0、config 段 `166` 同时成立。自此之后（F-192 ~ F-212）任务可以引用工具。

> 为什么把改造放在最前而不是最后：窗口期无论怎么排都存在（第一处 config 改动落地时旧期望集就红了），把解析器提前能让「红的那两条」从第一天起就是**已知且可断言**的；把解析器压到后面，则整个 Phase 27 ~ 29 的每次验收都在读一份算错的期望集。

---

## 1. 任务总表

列：任务号 / 标题 / Phase / 复杂度 / 人日 / 依赖 / 交付物 / 对应决策 / 覆盖的改动面行号（30 号 §3 的行号，`补` = 表 A 末尾的补记行）。

| 任务号 | 标题 | Phase | 复杂度 | 人日 | 依赖 | 交付物 | 对应决策 | 改动面行号 |
|---|---|---|---|---|---|---|---|---|
| F-176 | 版本号 0.1.1 与两支构建脚本的 jar 名 / banner | 26 | S | 0.5 | — | `gradle.properties`、两支 `.cmd` | D-124、D-147 | #1、#2 |
| F-177 | `verify_project.py` 的 config 期望来源改指 v0.1.1 | 26 | M | 1.5 | — | 工具新增 1 个解析器 + 1 张剔除清单 | A-40 | #43（#44 归 F-212） |
| F-178 | 生成器删掉叠「+」机制 | 27 | M | 1.5 | — | `tools/gen_textures.py`（5 处 + 2 个调用点） | D-125 | #4 |
| F-179 | 狂野苹果直通、尺寸断言放开与图入库 | 27 | L | 3.0 | F-178 | 生成器 4 处 + `wild_apple.png` | D-126、A-32 | #3、#5、#6 |
| F-180 | 压缩干草块音效 `GRASS` → `STONE` | 27 | S | 0.5 | — | `registry/FarmstormBlocks.java:79` | D-130、A-31 | #7 |
| F-181 | 据点核心亮度 15 | 27 | S | 0.5 | — | `registry/FarmstormBlocks.java:82-89` | D-141 | #8 |
| F-182 | `pumpkin_turret` 三键 + 1 条语言键 + 2 处注释 | 27 | M | 1.5 | — | `FarmstormConfig.java`、`ModLangProvider.java` | D-127、D-128、D-129、RT-33 | #9、#10、#11 |
| F-183 | 删 `TurretBullet.SPEED` 并改两处读点 | 27 | M | 1.5 | F-182 | `TurretBullet.java:28`、`PumpkinTurret.java:75` | D-129、A-30 | #12、#15 |
| F-184 | 炮弹近距收敛与共用结算体 | 27 | L | 3.0 | F-183 | `TurretBullet.java:90-119` | D-129 | #13、#14 |
| F-185 | 朋友的韭：从落地一次性改为 `effectTicks` 刻的场 | 28 | L | 3.0 | — | `entity/thrown/FriendChivesProjectile.java` | D-131、D-132、D-133、A-33 | #16、#17、#42 |
| F-186 | 区块卸载即结束（读档 `Settled` 首刻自杀） | 28 | M | 1.5 | F-185 | 同文件的读档分支 | D-133、RT-35 | #16（读档段） |
| F-187 | `TeamResolver` 的 `Owner × Owner` 重载 | 29 | M | 1.5 | — | `team/TeamResolver.java`、`team/ScoreboardTeamResolver.java` | D-138 | #31 |
| F-188 | `FarmlandLedger` v2：绑定表、`orphans`、兼容读与写入口 | 29 | L | 3.0 | — | `farmland/FarmlandLedger.java` | D-134、D-137、A-47 | #19、#22、#23（schema 1） |
| F-189 | `OutpostCoreIndex.Entry.gameTime` 与三个只读访问器 | 29 | M | 1.5 | — | `outpost/OutpostCoreIndex.java` | A-39、D-142、D-138 | #32（schema 2） |
| F-190 | 启动补建 `backfill` | 29 | M | 1.5 | F-188、F-189 | `FarmlandLedger` + 启动闸 | D-137、A-47 | #23（补建段） |
| F-191 | 额度 / 已用改口径，删 `base` 三件套 | 29 | M | 1.5 | F-187、F-188、F-189 | `FarmlandLedger` + 指令只读回显 | D-139、D-138 | #20、#21 |
| F-192 | 删 config 的 `farmland` 整组与 1 条语言键 | 29 | S | 0.5 | F-191 | `FarmstormConfig.java`、`ModLangProvider.java` | D-139、A-42 | 补记行 |
| F-193 | `FarmlandGuard.check` 选核 + 单核空位 | 29 | L | 3.0 | F-187、F-188、F-189 | `farmland/FarmlandGuard.java:69-75` | D-135、A-39 | #24 |
| F-194 | 锄地成功时写入绑定 | 29 | M | 1.5 | F-193 | `event/FarmstormGameEvents.java:194-217` | D-135 | #24（写入口） |
| F-195 | 耕地保护的离线判定与踩踏面 | 29 | M | 1.5 | F-187、F-188 | `FarmlandGuard.java:92-107`、`:120-134` | D-138、A-45 | #25、#26 |
| F-196 | 核被毁：先迁移、三支降级 | 29 | L | 3.0 | F-188、F-189、F-195 | `OutpostCoreBlockEntity.java:315-379` | D-136、A-35 | #27、#28 |
| F-197 | 删 `/farmstorm farmlandlimit` 整棵服务端指令树 | 29 | M | 1.5 | F-191 | 删 1 文件 + 3 处注册 + 1 条语言键 | D-140、RT-40 | #33、#34 |
| F-198 | 核心所在区块的运行期临时 ticket | 29 | M | 1.5 | F-189 | `outpost/CoreChunkTickets.java` + 3 处释放 + 起服补票 | D-145、A-38 | #39、#29 / #30 的 ticket 段 |
| F-199 | 1 个 S2C 只读包：payload / codec / 注册 / 快照 | 30 | L | 3.0 | — | `network/` 三个类 + `client/ClientCoreList` | D-142、RT-31 | #36 |
| F-200 | 广播器：组装 / 三级排序 / 去抖 / 10 个时机 | 30 | L | 3.0 | F-199、F-189、F-187 | `network/CoreListBroadcaster.java` + 6 个事件监听 | D-142、A-39、RT-31 | #35、#29 / #30 的广播段 |
| F-201 | CLIENT config 3 键 + 3 条语言键 | 30 | M | 1.5 | — | `config/FarmstormClientConfig.java`、`Farmstorm.java:56` | D-144 | #37 |
| F-202 | 2 个 `KeyMapping` + 2 条描述键 | 30 | M | 1.5 | F-201 | `client/FarmstormKeys.java` + 消费点 | D-144 | #38（Keys 段） |
| F-203 | 浮窗渲染 + 1 条「自己」标识键 | 30 | L | 3.0 | F-199、F-200、F-201 | `client/CoreHudRenderer.java` | D-143、A-34、A-41 | #38（Renderer 段） |
| F-204 | 拖拽定位 `CoreHudDragScreen` | 30 | M | 1.5 | F-203、F-202、F-201 | `client/CoreHudDragScreen.java` + 编辑态例外 | D-144 | #38（DragScreen 段） |
| F-205 | 客户端指令 `/farmstorm hud` + 3 条反馈键 | 30 | M | 1.5 | F-201 | `client/CoreHudClientCommands.java` | D-144 | #38（ClientCommands 段） |
| F-206 | 语言键总账 323 × 2 与 `ModLangProvider` 注释 | 31 | M | 1.5 | F-182、F-192、F-197、F-201、F-202、F-203、F-205 | 两份 lang + datagen 注释 | 30 号 §4.3、RT-40 | #40 |
| F-207 | 构建产物与 `dist/` 放行 | 31 | S | 0.5 | F-176、F-179、F-198、F-199、F-203、F-206 | `dist/` + `jar tf` 清单 | D-124、D-147、A-38 | #2（产物面）、#36 |
| F-208 | 30 号 §7 的 #1 – #19 验收 | 31 | M | 1.5 | F-182、F-188、F-189、F-190、F-191、F-192、F-193、F-194、F-197、F-199、F-200、F-201、F-202、F-203、F-204、F-205、F-206 | 验收记录（表格落盘） | 30 号 §7 | —（验收面） |
| F-209 | 30 号 §7 的 #20 – #38 验收 | 31 | M | 1.5 | F-177、F-179、F-180、F-181、F-182、F-183、F-184、F-185、F-186、F-192、F-196、F-198、F-199、F-200、F-203 | 验收记录 | 30 号 §7、A-46 | —（验收面） |
| F-210 | 29 号 §11 的 B-01 – B-20 边界复跑 | 31 | M | 1.5 | F-189、F-191、F-193、F-194、F-195、F-196、F-199、F-200、F-201、F-203、F-204 | 边界记录 | 29 号 §11、A-46 | —（边界面） |
| F-211 | 29 号 §11 的 B-21 – B-39 边界复跑 | 31 | M | 1.5 | F-178、F-179、F-180、F-181、F-182、F-184、F-185、F-186、F-188、F-190、F-196 | 边界记录 | 29 号 §11、A-46 | —（边界面） |
| F-212 | 全量汇总与上游一致性签字 | 31 | L | 3.0 | F-208、F-209、F-210、F-211 | 汇总记录 | 全部 D-124 ~ D-147 | #41、#44 |

任务数：`212 − 176 + 1 = 37`，连续无跳号；Phase 6 个；里程碑 6 个。

---

## 2. 任务详情

按 Phase 分组。每个任务固定六项：**目标 / 涉及文件 / 实现要点 / 依赖 / 验收（至少 1 条，可观察）/ 回滚影响**；复杂度与人日写在标题行。

### Phase 26 · 基线与工具链（F-176 ~ F-177）

阶段目标：先把版本号与工具这两件「取数」的事钉死，后面五个 Phase 的每次验收才有一把可信的尺子。

#### F-176 · 版本号 0.1.1 与两支构建脚本的 jar 名 / banner（S·0.5）

- **目标**：把版本号与它对外的三处载体一次改齐——`gradle.properties` 的 `mod_version`、两支 `.cmd` 里写死的 jar 名（含 `-sources.jar`）、两支 `.cmd` 头部的 banner 版本串——使构建产物名、脚本、文档三处不再互相脱钩。
- **涉及文件**：`gradle.properties:11`；`检查环境.cmd:5`、`:14`；`一键构建.cmd:5`、`:11-12`。
- **实现要点**：
  1. `mod_version=0.1.0` → `0.1.1`；`minecraft_version` / `neo_version` / Java 21 三行一字不动（30 号 §1.1）。
  2. jar 名两处逐字替换：`farmstorm-0.1.1+mc1.21.1.jar` 与 `farmstorm-0.1.1+mc1.21.1-sources.jar`。
  3. banner 的 `Farmstorm v0.0.1` → `Farmstorm v0.1.1`（**本文补充**：D-124 的补充写明「版本号只有 `gradle.properties:11` 一处来源，其余文档只引用」，而两支脚本的 banner 是第五、第六处写死的版本串；只改 jar 名会让 banner 继续声明 0.0.1，与 D-124 的单一来源冲突。此条并入本任务，不单独立裁决）。
  4. 两支脚本的其余行（`chcp 65001`、`PY` 路径、`gw.cmd build`、`pause`、`:fail` 段）一字不动。
- **依赖**：—
- **验收**：
  1. `.tools\gw.cmd build` 退出码 0；`build/libs/farmstorm-0.1.1+mc1.21.1.jar` 与 `farmstorm-0.1.1+mc1.21.1-sources.jar` 同时存在；`build/libs` 里没有 `farmstorm-0.1.0*` 与 `farmstorm-0.0.1*` 的残留。
  2. `jar tf build/libs/farmstorm-0.1.1+mc1.21.1.jar` 含 `META-INF/neoforge.mods.toml`，其 `version` 行是 `0.1.1`。
  3. `grep -c "farmstorm-0.0.1"` 在两支脚本里都是 **0**；`grep -c "farmstorm-0.1.1+mc1.21.1"` 在 `检查环境.cmd` 为 **1**、在 `一键构建.cmd` 为 **2**；两支脚本 banner 的版本串同为 `v0.1.1`。
  4. `git diff gradle.properties` 只有第 11 行一行变化。
- **回滚影响**：只影响产物名与版本串。回滚后 `build/libs` 回到 0.1.0 命名，不使任何已完成任务失效（jar 名不被代码引用，只有两支脚本读它）。

#### F-177 · `verify_project.py` 的 config 期望来源改指 v0.1.1（M·1.5）

- **目标**：让工具的 config 期望集由 30 号 §4.1 的 58 组现算，并把本版删掉的 `farmland.limitPerPlayer` 从并集里剔除；此后本版的「工具全绿」才可引用（A-40）。
- **涉及文件**：`tools/verify_project.py`（`:89` 的 `expected_config_keys_v010` 之后新增 `expected_config_keys_v011`；`:213` 起的 `main()` 里接入并集；文件头 `:2-12` 的 docstring 补一行来源）。
- **实现要点**：
  1. 新函数读 `docs/zh/30-TECH-0.1.1.md` §4.1 的 58 行表：取「组」列（嵌套组形如 `blast_carrot.nausea`）与「键」列反引号里的键名，拼成 `<组>.<键>`；行内的「（**改** 1 → 3）」这类注解由反引号解析自然跳过，不需要额外的正则分支。
  2. 解析后自校验：组数 = **58**、键数 = **166**，不等就 `fail` 并打印实际数与第一处不合规的表行号。
  3. `REMOVED_KEYS = {"farmland.limitPerPlayer"}`：在并集表达式**旁边**统一扣掉，不散落到两处（否则下一个删键的人会漏）。
  4. 期望集仍是「并集去重」的既有结构（`01-DESIGN` + `12-TECH` + `18-TECH` + `24-TECH` + `30-TECH` − `REMOVED`），**不新开第二份硬编码键清单**（那是本工具最忌讳的退化）。
  5. 解析失败要能定位：报错含文件名 + 表行号，不静默跳过。
- **依赖**：—
- **验收**：
  1. `python -X utf8 tools/verify_project.py` 的 §1 段打印「30-TECH §4.1：58 组 / 166 键」与「剔除已删键 1 个：farmland.limitPerPlayer」。
  2. `git diff --stat docs/` 为空：工具只读上游文档，不回改它们。
  3. **中间态判据（A-40）**：F-182 提交之前，§1 段的 `[FAIL]` / `[STALE]` 行合计**恰好两条**且键名只有 `pumpkin_turret.bulletSpeed`（缺）与 `farmland.limitPerPlayer`（多）；F-182 之后到 F-192 之前**恰好一条**（只多 `farmland.limitPerPlayer`）。出现第三个键名即判本任务失败（这是解析器写错的唯一征兆）。
  4. 失败路径可用：把 §4.1 的「组」列临时改一行（本地实验、不入库）后运行，退出码非 0 且报错含文件名与行号；恢复后退出码回到 0。
  5. `grep -c "expected_config_keys_v011" tools/verify_project.py` ≥ **2**（定义 + 调用）。
- **回滚影响**：回滚后期望集退回四份旧文档的并集，工具对本版的 166 键不再成立；F-208 / F-209 / F-212 三条引用工具结论的任务失效（它们按 A-40 排在本任务之后）。不使其它任务失效。

### Phase 27 · 材质、数值与方块属性（F-178 ~ F-184）

阶段目标：把「看得见」与「摸得着」的那七件事一次做完——一张图、一个生成器、五类声音、一格光照、三个数值、两处读点、一段收敛。

#### F-178 · 生成器删掉叠「+」机制（M·1.5）

- **目标**：把「物品贴图右下角叠绿色 `+`」这条规则从生成器里整体删掉（D-125），使重跑生成器不会再把 `+` 加回来；本版**不重生成**其余 24 张图，它们的字节必须不变。
- **涉及文件**：`tools/gen_textures.py`（`:274-275` `NO_PLUS_ICONS`、`:407` `add_plus`、`:546-560` `build_marker()`、`:612-623` 右下角贴附、`:974-975` 合成分支、`:1127` 与 `:1141-1142` 的调用点与 dry-run 文案）。
- **实现要点**：
  1. 删 `NO_PLUS_ICONS` 与 `add_plus` 字段：`Spec` 不再有「要不要叠」这一维，`compose()` 不再接受 marker 参数。
  2. 删 `build_marker()`、右下角贴附（`left = 9 / top = 9`）与 `compose_icon()`；`compose()` 只留「源图 → 尺寸归一」一条返回路径。
  3. 删 `:1127` 的调用与 `:1141-1142` 的 dry-run 文案（否则 dry-run 仍打印「+ 绿色「+」」）。
  4. `ICONS` 表的 26 条、`TEXTURE_SIZE = 16`、`make_placeholder()` 一律不动（本任务只删机制，不作产出清单）。
- **依赖**：—
- **验收**：
  1. `grep -c "add_plus\|NO_PLUS_ICONS\|build_marker\|compose_icon" tools/gen_textures.py` = **0**。
  2. `python -X utf8 tools/gen_textures.py --dry-run` 退出码 0，计划 **26** 项，输出里不再出现「绿色「+」」字样。
  3. 删掉入库的 `wild_apple.png` 后跑一次生成器：产出的这张是 **16×16** 且右下角**没有**绿十字——像素统计里那 33 px 的 `#3FA34D` 消失（这是 D-125 的机制层验收；64×64 直通在 F-179）。
  4. 其余 **24** 张 PNG 的 sha256 与提交前逐张相同（**本节不重跑那 24 张**，以提交字节为准；重跑不属于发布动作，A-61；D-125 补充的 `25 − 1 = 24`）。
  5. `git status` 显示只改了 `tools/gen_textures.py` 与重生成的 `wild_apple.png`。
- **回滚影响**：回滚会把叠机制放回，下次重跑会把 `+` 贴回每一张；F-179 的直通分支不依赖本任务（它只改 `wild_apple` 一条路径），回滚不使 F-179 失效。

#### F-179 · 狂野苹果直通、尺寸断言放开与图入库（L·3.0）

- **目标**：`textures/item/wild_apple.png` 换成 64×64 源图的原样输出；同时把生成器里三条会把它换回 16×16 或直接让构建失败的回滚点同版改掉（D-126 / A-32）。
- **涉及文件**：`tools/gen_textures.py:154`（默认源图）、`:849-881`（`wild_apple_grid()`）、`:884-895`（`draw_wild_apple()`）、`:982-983`（`expected_size()`）、`:1151-1154`（尺寸断言）；`assets/farmstorm/textures/item/wild_apple.png`。
- **实现要点**：
  1. `WILD_APPLE_SOURCE` 默认值改指 `wild_apple_64x64.png`；环境变量覆盖入口（`:155`）保留。
  2. `wild_apple` 走直通：`draw_wild_apple()` 直接返回源图副本，不再进 `wild_apple_grid()` 的 16 网格众数取色；`wild_apple_grid()` 若已无调用者就删除（不留死代码）。
  3. `expected_size()`：对 `wild_apple` 返回源图实际尺寸（`(64, 64)`），对 GUI 仍返回 `GUI_SIZE`，对其余 spec 仍返回 `(TEXTURE_SIZE, TEXTURE_SIZE)`；`:1153` 的断言**保留、不删**（A-32 的原话是「断言本身保留，只放开期望尺寸」）。
  4. 入库 PNG 是源图的逐字节副本：不改尺寸、不量化、不动 alpha。
  5. 模型 `models/item/wild_apple.json`、物品 id、`ICONS` 表条目与顺序、语言键、注册面一律不动（D-126 补充）。
- **依赖**：F-178
- **验收**：
  1. `textures/item/wild_apple.png` 是 **64×64**、**6,946 B**、sha256 与源图相同。
  2. 删掉入库的这张、跑 `python -X utf8 tools/gen_textures.py`：产物 64×64、与源图逐字节相同，且不报「尺寸断言失败」（`expected_size()` 与 `:1153` 已按 A-32 一起放开）。
  3. 连跑两次，产物 sha256 相同；其余 24 张 PNG 的 sha256 以**提交字节**为准、不变（**本节不重跑那 24 张**；重跑不属于发布动作，A-61）。
  4. `grep -c "wild_apple_grid" tools/gen_textures.py` = **0**；dry-run 里 `wild_apple` 与某个 GUI spec 的期望尺寸两行不同。
  5. 游戏内物品栏里狂野苹果仍占 1 格、不破图（显示面积仍是 16×16 GUI px，与贴图分辨率无关，RT-37）。
- **回滚影响**：回滚把图换回 16×16、把直通与断言放开一并撤掉；不影响 F-178 的机制删除，也不影响其它任务。

#### F-180 · 压缩干草块音效 `GRASS` → `STONE`（S·0.5）

- **目标**：`compressed_hay_block` 的 `SoundType` 换成 `STONE`；五类声音一起变（A-31），硬度 / 抗爆 / 工具 / 颜色一律不动。
- **涉及文件**：`registry/FarmstormBlocks.java:79`。
- **实现要点**：`sound(SoundType.GRASS)` → `sound(SoundType.STONE)`；`:76-78` 的 `mapColor(COLOR_BROWN)` / `strength(30.0F, 6.0F)` / `requiresCorrectToolForDrops()` 一字不动。
- **依赖**：—
- **验收**（五类各一条，对应 30 号 §7 #24 ~ #28）：
  1. **破坏**：音色与 `/playsound minecraft:block.stone.break block @s` 一致，与 `minecraft:block.grass.break` 明显不同。
  2. **放置**：同上，对照 `block.stone.place` / `block.grass.place`。
  3. **踩踏**：同上，对照 `block.stone.step` / `block.grass.step`。
  4. **挖掘**：同上，对照 `block.stone.hit` / `block.grass.hit`。
  5. **落地**：从 3 格高落到方块上，对照 `block.stone.fall` / `block.grass.fall`。
  6. 挖掘耗时不变：同一把镐挖掉它的刻数与改前相同（硬度 30 未动）。
  7. `git diff registry/FarmstormBlocks.java` 只有 `:79` 一行。
- **回滚影响**：只影响听感。回滚后压缩干草块回草音，不使其它任务失效。

#### F-181 · 据点核心亮度 15（S·0.5）

- **目标**：`outpost_core` 注册时加 `.lightLevel(state -> 15)`，其余方块属性一字不动（D-141）。
- **涉及文件**：`registry/FarmstormBlocks.java:82-89`。
- **实现要点**：在既有注册链尾部追加 `.lightLevel(state -> 15)`；`MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `sound(STONE)` / `pushReaction(BLOCK)` 不动。不做成 config（D-141 补充已否决）。
- **依赖**：—
- **验收**：
  1. 放下核心，F3 的 `Light` 读数为 **15**；拆掉核心后同一位置回落到放置前的读数（原版光照更新，无残留）。
  2. 夜里在核心旁：**15 格内**有光斑、**16 格外**无光；水平与垂直各测一次（`15 − 0 = 15`，29 号 §4.3 第 2 条）。
  3. 高光斑区不刷怪：在光斑正中与 15 格外各放一个刷怪平台，100 秒内正中**0** 只、外侧 ≥ 1 只（RT-36）。
  4. `git diff registry/FarmstormBlocks.java` 只有一行 `.lightLevel(...)` 新增。
- **回滚影响**：只影响光照与刷怪。回滚后核不再发光，不使其它任务失效（浮窗与绑定制都不读亮度）。

#### F-182 · `pumpkin_turret` 三键：`bulletSpeed` 新增、间隔 40、伤害 3（M·1.5）

- **目标**：把 man瓜（`farmstorm:pumpkin_turret`）的三行数值落进 config，并同改两处会立刻失真的注释与一条语言键（D-127 / D-128 / D-129 / RT-33 / RT-38）。
- **涉及文件**：`config/FarmstormConfig.java:425-433`（三键 + 字段声明）、`:911`（`farmlandBonus` 的注释）；`datagen/ModLangProvider.java`（+1 条 `farmstorm.configuration.pumpkin_turret.bulletSpeed`）。
- **实现要点**：
  1. `fireIntervalTicks` 的 `defineInRange` 默认值 60 → **40**；**同一处的注释 `:431` 现在写「60 刻 = 3 秒」，必须同改成 40 刻 = 2 秒**（**本文补充**：注释不是数据但会立刻失真，纪律与 30 号 §9 第 7 条「改键必须同改类注释」同一条；此条并入本任务，不单独立裁决）。
  2. `projectileDamage` 默认 1 → **3**，值域 0–100 不动（每发读一次，既有先例）。
  3. 新增 `bulletSpeed`：放在 `pumpkin_turret` 组内、`fireIntervalTicks` 之后（30 号 §3 第 11 行），double、默认 **0.45**、值域 `0.0 ~ 4.0`（与 `melon_grenade.submunitionSpeed` 同量纲同档），带 `.translation(...)`。
  4. `farmlandBonus` 的注释补一句「本键同时是每核容量与队伍额度基数（额度 = 本键 × 己方核数）」（RT-33、30 号 §2.2.6 的「一键两义」）。
  5. 语言键只加 1 条，`.translation(...)` 的键名与语言文件里的键逐字一致；总数由 F-206 统一核对。
- **依赖**：—
- **验收**：
  1. 删旧 `run/config/farmstorm-server.toml` 起服：`[pumpkin_turret]` 段恰 **5** 键，`fireIntervalTicks = 40`、`projectileDamage = 3`、`bulletSpeed = 0.45`、`targetRadius = 15`、`knockbackLevel = 1`（30 号 §7 #2 / #3）。
  2. 保留旧 toml 直接升级：`fireIntervalTicks` 仍 60、`projectileDamage` 仍 1，**而 `bulletSpeed` 被自动补成 0.45**（D-147、RT-38、30 号 §7 #4）。
  3. 值域生效：把 `bulletSpeed` 手改成 `5.0` 后重启，加载值回到值域内；toml 的注释行含 `0.0 ~ 4.0`。
  4. `grep -c "60 刻 = 3 秒" config/FarmstormConfig.java` = **0**、`grep -c "40 刻 = 2 秒"` = **1**；`farmlandBonus` 的注释含「额度」二字（`git diff` 可见）。
  5. 中英语言文件各出现 **1** 条 `farmstorm.configuration.pumpkin_turret.bulletSpeed`，两份键集仍相等。
- **回滚影响**：回滚后三键回 60 / 1 / 无键；F-183、F-184 会因读不到 `pumpkinTurretBulletSpeed` 而编译失败——它们排在本任务之后，属「依赖它的后续任务」。

#### F-183 · 删 `TurretBullet.SPEED` 并改两处读点（M·1.5）

- **目标**：把硬编码常量 `TurretBullet.SPEED = 0.6` 删掉，两处读者（每刻步长与出膛初速）同读 `pumpkin_turret.bulletSpeed`（D-129 / A-30）。
- **涉及文件**：`entity/TurretBullet.java:28`、`:90-96`；`behavior/impl/PumpkinTurret.java:75`。
- **实现要点**：
  1. 删 `:28` 的常量声明；`steerTowards` 与 `PumpkinTurret` 的出膛初速都改成 `FarmstormConfig.pumpkinTurretBulletSpeed.get()`（每发读一次，不缓存成字段）。
  2. 两处**必须同一次提交**：这个常量有两个读者，只改一处直接编译失败（A-30 的原始发现，事实卡 §3.2 第 7 行只点了其中一处）。
  3. 本任务**不改收敛逻辑**（F-184 才改）：`steerTowards` 仍是「归一化 × 速度」，`onHitEntity` 仍在原位。
  4. 客户端预测不动：`FarmstormEntityTypes` 的 `clientTrackingRange(4)` / `updateInterval(10)` 一个字不改。
- **依赖**：F-182
- **验收**：
  1. `grep -rn "TurretBullet.SPEED" src/main/java` **0 命中**；`grep -c "SPEED" entity/TurretBullet.java` = **0**。
  2. `grep -c "pumpkinTurretBulletSpeed"` 在 `entity/TurretBullet.java` 与 `behavior/impl/PumpkinTurret.java` **各 ≥ 1**。
  3. 同源可证：把 `bulletSpeed` 手改成 `0.1` 重启，`/data get entity @e[type=farmstorm:turret_bullet,limit=1] Motion` 三分量模长 ≈ 0.1（±0.01），出膛那一瞬与逐刻位移一致。
  4. `.tools\gw.cmd build` 退出码 0；`FarmstormEntityTypes` 的 `git diff` 为空。
- **回滚影响**：回滚需连带恢复常量；F-184 读同一个键、排在本任务之后，属依赖它的后续任务。

#### F-184 · 炮弹近距收敛与共用结算体（L·3.0）

- **目标**：消除贴脸过冲（`dist ≤ step` 时按剩余距离收敛、`dist ≤ MIN_STEP` 直接判中），并把 `onHitEntity` 的结算抽成共用方法、加 `hitResolved` 闸；**不引入软追踪**（D-129）。
- **涉及文件**：`entity/TurretBullet.java:90-96`（`steerTowards`）、`:98-119`（结算体）。
- **实现要点**：
  1. `steerTowards` 三分支：`dist > step` 与改前完全一致；`MIN_STEP < dist ≤ step` 时 `setDeltaMovement(delta)`（一步走完剩余距离，不越过）；`dist ≤ MIN_STEP` 时直接结算并 `discard()`。
  2. `applyHit(Entity)` 从 `:98-119` **原样搬家**（伤害类型 `farmstorm:void_bolt`、进度 `turret_hit`、击退算式一个字符不改），`onHitEntity` 与直判都走它。
  3. `hitResolved` 闸防双结算（`discard()` 之后原版 `move()` 仍可能在同刻产生第二次回调）。
  4. **不做**：转向角上限、最大追击时长、制导参数、换实体父类（D-129 第 3 条；墓碑 §6 第 8 条）。
- **依赖**：F-183
- **验收**：
  1. 目标站在炮台 **1 格内**：连续 5 发全部命中（目标掉 **15** 点血 = 5 × 3），5 秒内观察不到炮弹反向位移（30 号 §7 #35）。
  2. 目标在 **3 格外**：弹道形状与改前一致（每刻重设方向、速度模长恒定），命中数与改前同量级。
  3. 单发只结算一次：一次命中目标血量只掉 **3** 点；`latest.log` 里同一发只有一条伤害记录。
  4. 撞方块仍 `discard()`、不炸方块；伤害类型、击退等级 1、进度照发（不进新分支）。
  5. `grep -c "applyHit"` ≥ **3**（定义 + 两处调用）；`grep -c "MIN_STEP"` ≥ **2**。
- **回滚影响**：回滚后贴脸过冲复现；F-182 / F-183 的数值改动不受影响，不使其它任务失效。

### Phase 28 · 朋友的韭持续场（F-185 ~ F-186）

阶段目标：把「落地闪一下」换成「落地后 10 秒的场」，并把它的两个已知崩溃面（实体泄漏、跨卸载残留）一次堵死。

#### F-185 · 朋友的韭：从落地一次性改为 `effectTicks` 刻的场（L·3.0）

- **目标**：投掷物落地后变成一个持续 `friend_chives.effectTicks` 刻的场：每刻扫落点半径 6 格球体、施加 `effectTicks − x` 刻 Q弹、第 `effectTicks` 刻自毁；落地后置零速度、无重力、免碰撞、保持可见；音效仍只在落地那一刻判一次（D-131 / D-132 / D-133 / A-33）。
- **涉及文件**：`entity/thrown/FriendChivesProjectile.java`（`keepAliveAfterImpact` + 新增 `tick()` + `:56-80` 的施加/播音拆分）；`entity/ThrownCropEntity.java`（**只核对不改**，30 号 §3 第 18 / 42 行）。
- **实现要点**：
  1. 重写 `keepAliveAfterImpact()` 返回 `true`；基类 `:116-118` 的默认仍是 `false`，别的子类不受影响。
  2. `onImpact` 只做三件事：读一次 `fieldLength = friend_chives.effectTicks`（默认 200、值域 20 ~ 1200）、施加 `elapsed = 0` 那一批、按 `:71-73` 的既有判据播一次音；随后 `setDeltaMovement(Vec3.ZERO)` + `setNoGravity(true)` + `noPhysics = true`，`fieldTick = 0`。
  3. `tick()`：`super.tick()` → `fieldTick++` → `>= fieldLength` 则 `discard()` → 否则 `applyField(level, position(), fieldTick)`。
  4. `applyField` 的时长一律由 `fieldLength` 派生（`fieldLength − elapsed`），**代码里不出现第二个 200**（A-33）；每次写入的时长比上一刻少 1 刻，所以既不续期也不提前结束。
  5. 不新增方块实体承载场（那会给 `friend_chives` 新增 `block_entity_type`，注册面就不再是 +0）；Q弹 amplifier 仍为 0、不重写它的 `tick`。
- **依赖**：—
- **验收**：
  1. 时序（30 号 §7 #30）：站进球内的生物 Q弹剩余时长逐刻递减（第 1 刻 **200**、第 2 刻 **199**……第 199 刻 **1**）；第 **200** 刻实体消失（`/data get entity @e[type=farmstorm:friend_chives]` 查不到）。
  2. 200 不是常量：把 `friend_chives.effectTicks` 改成 **100** 重启重测，递减止于 100、第 **100** 刻消失（A-33）。
  3. 音效（D-132 / #31）：空投（球内 0 生物）不播 `farmstorm:friend_chives`；命中 ≥ 1 只播 **1** 次（`latest.log` 里整场只一条）。
  4. 落地物理：落点在水流里时实体位置 30 秒内不变（±0 格）；踩上去不被推动。
  5. `git diff entity/ThrownCropEntity.java` 为空；`grep -c "200" entity/thrown/FriendChivesProjectile.java` 的命中不含时长字面量。
- **回滚影响**：回滚后回到落地一次性；F-186（卸载即结束）失去意义但不会编译失败，不使其它任务失效。

#### F-186 · 区块卸载即结束：读档 `Settled` 首刻自杀（M·1.5）

- **目标**：堵住「落地后实体被存进区块 NBT、加载回来既不自毁也不 tick 场」的永久泄漏（RT-35 / D-133）：场不写 NBT 续命、不跨维度恢复。
- **涉及文件**：`entity/thrown/FriendChivesProjectile.java`（`readAdditionalSaveData` + `tick()` 的首刻判据）。
- **实现要点**：
  1. `readAdditionalSaveData` 读基类写的 `Settled` 键，置 `loadedSettled`。
  2. `tick()` 在 `super.tick()` 之后第一件事：`loadedSettled` 为真就 `discard()`。
  3. `fieldTick` **不写 NBT**（D-133：区块卸载即结束）。
  4. 只依赖基类既有键，不新增自定义 NBT 键（`ThrownCropEntity:145-157` 的存取范围不动）。
- **依赖**：F-185
- **验收**：
  1. 场存在时走出 200 格强制卸载再回来：实体不存在、没有残留；退出重进世界也不复活（30 号 §7 #32）。
  2. 卸载前后各查一次：`/data get entity @e[type=farmstorm:friend_chives]` 在回来后的**第一个 tick 之后**就查不到。
  3. NBT 面：用 NBT 查看器读该实体所在区块，实体只有基类的 `Settled` 等键，没有 `fieldTick` / `FieldTick` 之类自造键。
  4. `grep -c '"Settled"' entity/thrown/FriendChivesProjectile.java` ≥ **1**。
- **回滚影响**：回滚后跨卸载会留下永久实体（RT-35 复现）；不影响 F-185 的会话内行为。

### Phase 29 · 耕地-核心绑定制（F-187 ~ F-198）

阶段目标：本版最大的一处结构替换——把「范围内即受管」换成「显式绑定」，并把它牵动的六条链路（己方判定、存档、补建、索引比较键、额度、迁移）与两处删除（config 组、服务端指令树）、一处新增（区块 ticket）一次做完。

#### F-187 · `TeamResolver` 的 `Owner × Owner` 重载（M·1.5）

- **目标**：让「己方」判定能只凭 UUID + 名字完成（离线也有效）：给 `TeamResolver` 加 `Owner × Owner` 重载，`Player` 版变成一行委托（D-138）。
- **涉及文件**：`team/TeamResolver.java`、`team/ScoreboardTeamResolver.java`。
- **实现要点**：
  1. 新重载 `boolean isAlly(ServerLevel level, Owner owner, Owner target)`；现有逻辑原样搬进去，`Player` 版只做 `new Owner(uuid, scoreboardName)` 再委托。
  2. 不新开第三条判据：`util/DeployedOwnership.isAlly(...)` 的签名与调用点不动。
  3. `teamName(...)` 本来就按名字查记分板成员表（离线可用），本任务不改它。
  4. `untamedFallback` 的两种取值语义与现状逐条等价（`"self"` 下无队伍玩家只算自己）。
- **依赖**：—
- **验收**：
  1. `grep -c "isAlly(ServerLevel" team/TeamResolver.java` 与 `team/ScoreboardTeamResolver.java` **各 2**（两个重载）；`Player` 版方法体 3 行以内（纯委托）。
  2. 行为零变化：同队队友踩己方田被拦、非队友踩同一块田照常变泥土——改前 / 改后两组对照结果相同。
  3. 离线可判：把田主 B 踢下线，另一名在线队友 C 跳踩同一块田，判定仍成立（完整观察面在 F-195）。
  4. `.tools\gw.cmd build` 退出码 0；`util/DeployedOwnership.java` 的 `git diff` 为空。
- **回滚影响**：回滚后 F-191（额度按核数算己方）与 F-195（离线保护）失去离线判据，但编译仍可用旧 `Player` 版；两者排在本任务之后。

#### F-188 · `FarmlandLedger` v2：绑定表、`orphans`、兼容读与写入口（L·3.0）

- **目标**：把账本从「玩家 → 坐标集合」改成「耕地 → 唯一核」的绑定表，落盘升到 `dataVersion: 2` 并兼容读 v1；`byOwner` 变成派生视图、不再落盘（D-134 / D-137 / A-47）。
- **涉及文件**：`farmland/FarmlandLedger.java`（`:51` 本体、`:128-158` 记账、`:203-219` 惰性校验、`:228-264` 落盘/读盘；新增 `bind` / `unbind` / `migrate` / `bindingsOf` / `orphanCount` / `rebuildByCore`）。
- **实现要点**：
  1. 本体三份：`LinkedHashMap<BlockPos, Binding> bindings`（唯一真相）、`Map<CoreKey, LinkedHashSet<BlockPos>> byCore`（内存反查，**不落盘**）、`LinkedHashMap<BlockPos, UUID> orphans`（老档补不上，**要落盘**——不落盘则补建不可重试，「保持不动」会退化成「永久遗忘」）。
  2. 两个 record：`Binding(pos, owner, coreDimension, corePos)`、`CoreKey(dimension, pos)`；`binding.owner` 与核主**可以不同**（D-136 的迁移按耕地主判同盟）。
  3. 落盘：`dataVersion: 2` + `bindings`（4 键）+ `orphans`（2 键）；**不再写 `owners`**；缺失 `dataVersion` 视为 1。
  4. 兼容读：v1 的 `owners` **全部**读成 `orphans`（不猜绑定）；读入即校验（已不是 `minecraft:farmland` 且区块已加载的条目当场丢弃）；`rebuildByCore()` 从 `bindings` 重建反查；`orphans` 用 `LinkedHashMap` 只为落盘稳定可读，**它的顺序不参与任何判定**（A-47：补建在 F-190 里按 `BlockPos` 单级全序重排后逐条处理）。
  5. `setDirty()` 时机：`bind` / `unbind` / `migrate` / `adoptOrphan` 是；`rebuildByCore` 否；`validated` **只在真丢了条目时**是。
  6. 惰性清理必须**同时剪 `byCore`**（30 号 §9 第 3 条：只剪 `bindings` 会留下幽灵耕地占住 16 格容量）。
- **依赖**：—
- **验收**：
  1. 手工构造一份 `dataVersion: 2` + 1 条 `bindings` 的 `.dat` 放进 `world/data/`：启动后 `latest.log` 的「耕地绑定指纹 …（**1** 条）」非空；同一份存档再启动一次，指纹相同。
  2. 0.1.0 旧档（只有 `owners`）启动：不崩、`latest.log` 一条「旧格式：N 条读入 orphans」，抽样 `/execute if block … minecraft:farmland` 的方块一个没动。
  3. 保存后形态：`/save-all` 后 NBT 含 `dataVersion: 2`、`bindings`、`orphans` 三个键，**没有** `byCore` 键。
  4. 降级方向：把 v2 存档交给 0.1.0 的 jar 读，读到空账本且不铲世界（30 号 §2.2.3 第 1 条；这是可接受的降级方向）。
  5. 写入口齐备：`grep -c "public void bind\|public void unbind\|public void migrate\|bindingsOf\|orphanCount" farmland/FarmlandLedger.java` ≥ **5**。
  6. 幽灵耕地：把一条已绑坐标 `/setblock air` 后触发一次惰性校验，`bindings` 与 `byCore` 的条数**同时减 1**（`latest.log` 的「绑定指纹（M 条）」与「反查集合 N 条」两行同时变）。
- **回滚影响**：回滚把账本退回 v1（绑定信息丢失，但不铲世界）；F-190、F-191、F-193、F-195、F-196 全部依赖它，排在其后。

#### F-189 · `OutpostCoreIndex.Entry` 增加 `gameTime` 与三个只读访问器（M·1.5）

- **目标**：把 A-39 的比较键第一级落到索引侧并落盘；同时给 `ownerNames` / `count` / 条目迭代加只读访问器（A-39 / D-142 / D-138）。
- **涉及文件**：`outpost/OutpostCoreIndex.java`（`:50` 的 `Entry`、`:209-238` 的 `save`、`:240-266` 的 `load`、`:56` 的 `ownerNames`、`count`、`record(...)` 的签名）。
- **实现要点**：
  1. `record Entry(ResourceLocation dimension, BlockPos pos, long gameTime)`；落盘每条多一个 `gameTime` 键。
  2. 写入时机：`OutpostCoreBlockEntity#setOwner` 里**同一个** `placedGameTime` 同时写进 BE 与索引（同方法、同变量、同 tick ⇒ 两个副本不可能漂移）。
  3. 读取：老档条目缺 `gameTime` 取 `Long.MIN_VALUE`（视作最早、不伪造时间），启动打一条「核心比较键缺失（老档）：N 条」；**不做**「趁加载时从 BE 补写」（那会让值依赖加载时机，破坏可复现性）。
  4. 只读访问器：`entriesOf(...)` / `owners()` / `count(server, uuid)`；**不复活** `isOwnerOrAlly`（它吃 `Player`，离线不安全）。
  5. 比较路径只读索引：不 `getChunk` / 不 `getBlockEntity` / 不 `hasChunkAt`（A-39 的结构性保证，不是靠约定）。
- **依赖**：—
- **验收**：
  1. 放 1 个核后读存档：`farmstorm_outpost_core.dat` 的 `entries` 里那条含 `gameTime`，值与 `/data get block` 读到的 `placedGameTime` 相同。
  2. 老档（无 `gameTime`）启动：日志「核心比较键缺失（老档）：N 条」，N = 老核数；启动两次 N 相同（不随时间漂移）。
  3. `grep -c "getChunk\|getBlockEntity\|hasChunkAt" outpost/OutpostCoreIndex.java` = **0**。
  4. 三个访问器存在且被外部使用：`grep -rn "entriesOf\|\.owners()\|\.count(" outpost/OutpostCoreIndex.java` ≥ **3**（定义 + 至少一处调用）。
  5. `isOwnerOrAlly` 仍只有定义、无调用者：`grep -rn "isOwnerOrAlly" src/main/java` 恰 **1** 条。
- **回滚影响**：回滚后索引少了 `gameTime`，F-193 的平手判据退化为两级（A-39 失效）、F-200 的跨维度排序仍可用维度 id；F-190 / F-193 / F-200 依赖它。

#### F-190 · 启动补建：无绑定耕地按迁移谓词补建（M·1.5）

- **目标**：服务端启动后跑一次补建，把 `orphans` 里的耕地按「覆盖 + 同盟 + 有空位，最近优先」绑到核上；补不上的保持耕地、保持无绑定（D-137 / A-47）。
- **涉及文件**：`farmland/FarmlandLedger.java`（`backfill(server)`）、`event/FarmstormGameEvents.java`（一次性启动闸）。
- **实现要点**：
  1. 遍历顺序：**先把候选耕地按 `BlockPos#compareTo`（y → z → x）单级全序升序排序，再逐条处理**（A-47 rev.2）；`orphans` 的持久化顺序、`byOwner` 的迭代顺序、任何 Map / List 的顺序**都不参与判定**。为什么是 `BlockPos` 单级而不是「维度 + 坐标」：耕地这一侧的落盘只有 `pos`、没有维度字段（30 号 §2.2.2 第 2 点），维度只有核才有。
  2. 谓词与迁移**同一条**（覆盖 + 与耕地主同盟 + 该核有空位），多个候选取 `distSqr` 最近、平手按 `placedGameTime` → 维度 id → 坐标字典序（A-39 的三级键只管**候选核之间**，不用于遍历顺序）。
  3. 整段**没有 `setBlock`**；只在首个 `ServerTickEvent.Post` 跑一次（一次性布尔闸），不放在构造器里（构造器阶段 `MinecraftServer` 还没就绪）。
  4. 收尾一条 INFO：「耕地补建：绑定 N 块 / 保持未绑定 M 块（老档共 T 块，按 `BlockPos` 全序）」。
- **依赖**：F-188、F-189
- **验收**：
  1. 用 0.1.0 旧档启动，日志出现「耕地补建：绑定 N 块 / 保持未绑定 M 块」；**同一份存档连跑两次，N 与 M 完全相同**（30 号 §7 #19、A-47）。
  2. **顺序不参与判定（A-47 rev.2）**：把该 `.dat` 里 `orphans` 段的两条记录对调后重跑，绑定的坐标集合与 N/M 与对调前**完全相同**；日志里「按 `BlockPos` 全序」字样仍在。
  3. 补不上的地块仍是 `minecraft:farmland`：逐个 `/execute if block … minecraft:farmland` 全部成功，**0** 个变泥土。
  4. 补建只跑一次：同一次运行里不出现第二条「耕地补建」日志；重启才再跑。
  5. `grep -c "setBlock" farmland/FarmlandLedger.java` = **0**（补建整段无世界写入）。
  6. 容量是硬顶：造 20 块老田 + 1 个核（容量 16），**N = 16、M = 4**，且第 17 块起的方块原地不动；**哪 16 块能绑上由 `BlockPos` 全序决定**（坐标最小的 16 块），把 `orphans` 顺序打乱也不变。
- **回滚影响**：回滚后老档的田全部保持无绑定（不受保护、不铲）；F-193 起的绑定链路仍可用，不使其它任务失效。

#### F-191 · 额度与已用改口径，删 `base` 三件套（M·1.5）

- **目标**：`capacity = farmlandBonus × 己方核数`、`used = 绑定表里己方名下的块数`；删掉 `baseOverride` / `base()` / `setBase()` / `personalCapacity` 的 base 参数 / `sumOverMembers` 的 base 参数（D-139 / D-138）。
- **涉及文件**：`farmland/FarmlandLedger.java:57-58`、`:72-98`、`:101-121`、`:185-196`；`command/FarmstormCommands.java:50-57`（临时改成只读回显，使提交仍可编译）。
- **实现要点**：
  1. `capacity(level, player)` 走 `OutpostCoreIndex.count(server, uuid)` 的己方求和（含其它维度、离线也有）。
  2. `used(...)` 数 `bindings` 里 `owner` 与 player 互为己方的条数；`orphans` **不计入**（没绑定就不占名额）。
  3. `members(...)` 的在线遍历消失；`farmlandLimitPerPlayer` 的**唯一读者** `base()` 一并消失（键本身由 F-192 删）。
  4. 指令侧只做最小改动：`FarmstormCommands` 的执行体改成「回显 `used / capacity`」、不再调 `setBase`；指令树暂留，整棵删除在 F-197。
  5. 「一键两义」的注释由 F-182 负责，本任务只改算式。
- **依赖**：F-187、F-188、F-189
- **验收**：
  1. 四个算例逐条对上（29 号 §3.4）：单人 0 核 → **0**；单人 1 核 → **16**；单人 2 核且地全在 A 的 3×3 里 → 额度 **32** 但实际只能开垦 **16**；同一格被 A、B 覆盖且 A 满 → 仍可绑 B（末条在 F-193 复跑）。
  2. 队友下线不改额度：A、B 同队各放 1 核，B 下线后 A 的额度仍是 **32**（改前会变小）。
  3. `grep -c "baseOverride\|personalCapacity\|sumOverMembers" farmland/FarmlandLedger.java` = **0**；`FarmstormConfig.java` 里 `farmlandLimitPerPlayer` **仍存在**（本任务不删键）。
  4. `used` 与绑定同源：锄 1 格后 `used = 1`；把该格 `/setblock air` 并触发惰性校验后 `used = 0`。
  5. `/farmstorm farmlandlimit 8` 仍能执行，但**不改额度**，只回显 `used / capacity`；执行前后额度读数相同。
  6. `.tools\gw.cmd build` 退出码 0。
- **回滚影响**：回滚需同时恢复 `setBase` 与指令调用（指令目前只是回显，不会崩）；F-192、F-193、F-197 依赖它。

#### F-192 · 删 config 的 `farmland` 整组与 1 条语言键（S·0.5）

- **目标**：删掉 `farmland` 这一组（`limitPerPlayer` 键 + 字段 + 注释 + 翻译），使 `farmstorm-server.toml` 不再有 `[farmland]` 段、组数 **59 → 58**（D-139）。**这一动作面在 30 号 §3 的 44 行里没有对应行（A-42 的补记行）**。
- **涉及文件**：`config/FarmstormConfig.java:226`（字段声明）、`:904-908`（整组）；`datagen/ModLangProvider.java:393-394`（语言键生成段）。
- **实现要点**：
  1. 删 `b.push("farmland")` 到 `b.pop()` 的整段、字段声明与它的 getter。
  2. 删 `farmstorm.configuration.farmland.limitPerPlayer` 的生成段；datagen 重跑后该键从两份语言文件消失。
  3. 其余 57 组、`outpost_core` 组一字不改。
  4. 本任务是 A-40 中间态的**收口点**：删旧 toml、重新生成之后工具 §1 段应首次转绿。
  5. **`base()` 是 `farmland.limitPerPlayer` 的唯一读者（`FarmlandLedger.java:72-74`），所以 F-191 必须先于 F-192；这不是风格顺序，是编译顺序**（A-56）：F-192 先提交的话，`base()` 会引用一个已删的字段，那次提交编译不过。
- **依赖**：F-191
- **验收**：
  1. 删旧 `run/config/farmstorm-server.toml` 起服：两份产物里 `[farmland]` 段**不存在**，段行（`^\s*\[`）数 = **58**，键数 = **166**（30 号 §7 #2）。
  2. `grep -c "farmland" config/FarmstormConfig.java` = **0**；`grep -c "farmland.limitPerPlayer"` 在两份 lang JSON 里都是 **0**。
  3. 保留旧 toml 不删时启动：`[farmland]` 段残留但**无人读取**；工具对残留段报 STALE 并提示「重新生成即可消除」——删 toml 重跑一次即消失（这是已知的可恢复中间态）。
  4. `python -X utf8 tools/verify_project.py` 在重新生成 toml 之后：`结论：FAIL 0 项`、退出码 0、§1 段显示 **166**（A-40 收口；这是本文件里第一条可以引用工具结论的任务）。
- **回滚影响**：回滚后 `[farmland]` 段回到 toml、组数回 59；F-197（删指令）不依赖它，回滚不使 F-197 失效，但会让 F-208 / F-209 / F-212 的 `[farmland]` 断言回到红。

#### F-193 · `FarmlandGuard.check`：选核 + 单核空位（L·3.0）

- **目标**：把锄地闸的三态判定扩成「范围闸 → 候选集（覆盖 ∩ 己方 ∩ 有空位）→ 取最近 → 返回选中的核」，`QUOTA_FULL` 的语义变为「最近的那个己方核满了」（D-135 / A-39 / RT-33）。
- **涉及文件**：`farmland/FarmlandGuard.java:69-75`（`check`）；新增 `Decision` record。
- **实现要点**：
  1. 新增 `record Decision(Verdict verdict, @Nullable CoreKey core)` 与携带结果的重载；三态结构（`ALLOW` / `NO_CORE` / `QUOTA_FULL`）不变。
  2. 距离口径 `BlockPos#distSqr`（三维欧氏平方）：3×3 判定本身已是切比雪夫，距离再用切比雪夫会在对角方向大量并列。
  3. 平手三级键：`placedGameTime` 升序 → 维度 id → `BlockPos#compareTo`（y → z → x）；**不得依赖任何 Map / List 的遍历顺序**（A-39）。
  4. 候选集**先过滤再取最近**（不能先取最近再判空位，否则「最近的满、别处有空位」会被误拒，与 `16 × 核数` 的总额度自相矛盾）。
  5. `QUOTA_FULL` 的语言键名不改（30 号 §9 第 4 条），只改文案内容。
- **依赖**：F-187、F-188、F-189
- **验收**：
  1. 第 **17** 格被拒并提示「耕地额度已满 16/16」；该核名下仍只有 **16** 条（30 号 §7 #17）。
  2. `outpost_core.farmlandBonus` 改成 **2** 后重启：第 **3** 格即被拒，且队伍容量同步变 2（RT-33）。
  3. 三级键（30 号 §7 #18 的 ①②③）：相距 40 格两核之间锄 1 格 → 绑较近那个；等距 → 绑 `placedGameTime` 更早的；同刻等距（离线把两个 `gameTime` 改成同一个值）→ 绑坐标字典序在前的。
  4. 「最近满、另一个有空位」→ 绑到有空位的那个（29 号 B-17）。
  5. 拒绝时不扣耐久（29 号 B-16）：比较操作前后 `ItemDamage` 读数。
- **回滚影响**：回滚后 `check` 回到两态判定，F-194 失去选中的核而无法编译（两任务相邻且有依赖）。

#### F-194 · 锄地成功时写入绑定（M·1.5）

- **目标**：`FarmstormGameEvents` 的锄地闸在 `ALLOW` 分支里把 `ledger.add(...)` 换成 `ledger.bind(owner, coreKey, pos)`，同一 tick 内完成写绑定 + 记账 + 放耕地（D-135）。
- **涉及文件**：`event/FarmstormGameEvents.java:194-217`（`:206` 的 `ledger.add(...)`）。
- **实现要点**：
  1. 用 `check` 返回的 `Decision.core` 作绑定目标；`Decision` 的 `core` 为空时按 `NO_CORE` / `QUOTA_FULL` 原路拒绝。
  2. `bind(player.getUUID(), new CoreKey(level.dimension().location(), decision.core()), pos)`。
  3. 不新增提示文案、不新增语言键、不改拒绝路径的扣耐久语义。
- **依赖**：F-193
- **验收**：
  1. 放 1 个核、锄 1 格：`bindings` 里出现 1 条，`coreDimension = "minecraft:overworld"`、`corePos` = 该核坐标（30 号 §7 #16）。
  2. 绑定在同一 tick 完成：锄完立刻 `/save-all` 读 NBT 就能看到该条（不用等下一次自动保存）。
  3. 双核覆盖区：在 A、B 两核的 3×3 交界锄 1 格，`bindings.corePos` 落在较近那个。
  4. 被拒的两种情形**一条绑定都不产生**（`bindings` 条数不变）。
  5. `grep -c "ledger.add(" event/FarmstormGameEvents.java` = **0**。
- **回滚影响**：回滚后锄地又不写绑定，F-193 的选核结果无人消费；F-196（迁移）不受影响（它读 `bindings`，可能为空）。

#### F-195 · 耕地保护的离线判定与踩踏面（M·1.5）

- **目标**：耕地保护判据从「遍历在线玩家」换成「耕地 → 绑定 → 核主 UUID」，归属者离线也有效；踩踏面按「有绑定即受保护」（D-138 / A-45）。
- **涉及文件**：`farmland/FarmlandGuard.java:92-107`（`inOwnRange`）、`:120-134`（`protectsFromTrample`）。
- **实现要点**：
  1. `protectsFromTrample(level, pos)`：先判方块仍是 `minecraft:farmland`，再查 `bindings` 是否有这一格 —— **有绑定即受保护**。
  2. 理由（A-45）：踩踏的动作方可能是一只没有队伍的 `Monster`；若按「己方」实时判，生物会把己方田踩坏，与 0.1.0 的 D-120 第 3 项（玩家与生物都要拦）冲突。绑定只在己方锄地时产生，所以「有绑定」就是「己方关系曾成立」的存储证据。
  3. `inOwnRange(level, player, pos)` 改成读绑定表 + `isAlly(核主, player)`（这一条有明确查询者，按实时己方判）。
  4. 换队后：**额度与浮窗**两面立刻按新队重算（F-191 / F-200 各有一条验收），**踩踏面保持受保护**（绑定未变）——三面的差异写进 29 号 B-18 的收窄版。
- **依赖**：F-187、F-188
- **验收**：
  1. 核主下线后他的田踩不坏：A 放核并锄 1 格，A 退出，B（非同队）跳踩该格 → 仍是 `minecraft:farmland`（D-138 的 bug 2 不再复现）。
  2. 对照：同一位置一块**无绑定**的耕地照常被踩成泥土。
  3. 生物踩踏：僵尸跳到有绑定的耕地上 → 不变泥土（A-45 保住的 D-120 第 3 项）。
  4. 换队后踩踏面不变（A-45）：把核主换到另一队，同一格仍踩不坏；同一时刻**额度**读数按新队重算——两行读数并排取证。
  5. `grep -c "getPlayerList().getPlayers()" farmland/FarmlandGuard.java` = **0**。
- **回滚影响**：回滚后保护退回在线遍历（bug 2 复现）；F-196 不受影响，不使其它任务失效。

#### F-196 · 核被毁：先迁移、三支降级（L·3.0）

- **目标**：`settleOnRemoval` 只处理本核名下的耕地，先迁移给接替核；迁不动 → 变泥土——**两级：改绑 / 变泥土**（A-60 收窄）；「迁不动且区块未加载 → 只摘绑定」降为**防御性分支**、正常运行不可达（D-136 / A-35 / A-60）。
- **涉及文件**：`blockentity/OutpostCoreBlockEntity.java:315-335`、`:353-366`、`:369-379`。
- **实现要点**：
  1. 保留 `:316-326` 的「真移除 / 卸载」判据**一个字不改**；从 `bindingsOf(dead)` 取本核名下集合。
  2. 候选：覆盖该耕地 ∧ 与**耕地主**同盟 ∧ 仍有空位；多个候选按 `distSqr` → 三级键（与锄地同一条）。
  3. 两个出口 + 一条防御分支：改绑（`migrate`）、`setBlock(DIRT, UPDATE_ALL)` + 摘绑定；「只摘绑定」为防御分支（A-60：票恒覆盖同一 3×3，正常运行不可达）。
  4. **先算完集合、再动世界**（沿用 `:312-313` 的既有理由，避免边遍历边改账本）；**不为清算加载区块**（不 `getChunk`）。
  5. 不通知耕地主、不掉落、不给补偿（D-136 补充）。
- **依赖**：F-188、F-189、F-195
- **验收**：
  1. 迁移成功（30 号 §7 #20）：A 名下 3 块田、B 覆盖其中 2 块，拆 A 后那 2 块的 `bindings.corePos` 变成 B、第 3 块变 `minecraft:dirt`；B 名下总数 ≤ **16**。
  2. 迁不动 + 区块已加载（#21）：A 名下 1 块田、周围无接替核、人就站在田边，拆 A 后该田变泥土且**没有掉落物**。
  3. 走远 200 格用 `/execute in <dim> run setblock <核坐标> air` 拆核（#22 / A-60）→ 该田变 `minecraft:dirt`（与近处一致）、无掉落物；`bindings` 那条被摘除；日志**不出现**「迁移失败且区块未加载：只摘绑定」（该串在 `OutpostCoreBlockEntity.java:374`，正常运行不出现，以代码审阅确认）。
  4. 两支降级（29 号 B-19 / B-20）：接替核存在但已满 → 泥土；接替核覆盖但与耕地主不同盟 → 泥土。
  5. 地块已不是耕地（B-21）：跳过、摘绑定、不动世界（该位置原来是石头时石头不变）。
  6. 只处理本核名下：另一个核的田（`bindings.corePos` 指向 B）在拆 A 后**完全不动**（bug 1 的症状不复现）。
- **回滚影响**：回滚后核被毁又回到「范围内全铲」（bug 1 复现）；F-198 的 ticket 释放挂在 `release()` 上，不依赖本任务的迁移分支。

#### F-197 · 删 `/farmstorm farmlandlimit` 整棵服务端指令树（M·1.5）

- **目标**：删掉 `command/FarmstormCommands.java` 整文件、3 处注册、1 条语言键与 `ModLangProvider` 的对应段；服务端不再有 `/farmstorm`（D-140 / RT-40）。
- **涉及文件**：`command/FarmstormCommands.java`（删除）、`event/FarmstormGameEvents.java:4`、`:62`、`:71-73`、`datagen/ModLangProvider.java:404-406`。
- **实现要点**：
  1. 整文件删除；import、`gameBus.addListener(...)`、`onRegisterCommands` 方法体三处一并删。
  2. 删 `command.farmstorm.farmlandlimit.set` 的生成段与两份语言文件里的产物。
  3. **不保留空壳**：`/farmstorm` 根节点不注册。
  4. 客户端 `/farmstorm hud` 是另一条注册（`RegisterClientCommandsEvent`），本任务不碰、也不改它的根节点语义。
- **依赖**：F-191
- **验收**：
  1. 专用服务器控制台执行 `/farmstorm` 返回「未知的指令」；`/farmstorm farmlandlimit 5` 在服务端失败（30 号 §7 #12 的服务端一半）。
  2. 单人客户端执行 `/farmstorm farmlandlimit 5` 同样失败；客户端补全里不出现 `farmlandlimit`（客户端 `hud` 子树的完整补全在 F-205）。
  3. `grep -c "farmlandlimit"` 在两份 lang JSON 与 `ModLangProvider.java` 里都是 **0**。
  4. `grep -rn "FarmstormCommands" src/main/java` **0 命中**；`git status` 显示该文件被删除。
  5. 服务端启动日志没有新增异常，`gw build` 退出码 0。
- **回滚影响**：回滚把指令树放回，但 `setBase` 已在 F-191 删除，因此**本任务与 F-191 是同一条回滚链**：回滚本任务必须同时回滚 F-191，否则编译失败。

#### F-198 · 核心所在区块的运行期临时 ticket（M·1.5）

- **目标**：每个已放置的核在自己所在的那 1 个区块上加一张自有 `TicketType`、`distance = 1`（方块实体 tick 的最小票），核被毁 / 停服即释放；**不写 `world/data/chunks.dat`**（D-145 / A-38 / A-43 / RT-39）。
- **涉及文件**：`outpost/CoreChunkTickets.java`（新增）、`blockentity/OutpostCoreBlockEntity.java:95-103`、`:166-170`、`event/FarmstormGameEvents.java`（`ServerStoppingEvent` + 启动补票）。
- **实现要点**：
  1. `TicketType.create("farmstorm_core", Comparator.comparingLong(ChunkPos::toLong))`，`distance = 1`；`addRegionTicket` / `removeRegionTicket` 四个参数完全一致。
  2. 释放三处：`release()`、`settleOnRemoval` 末尾的 `release()`、停服 `releaseAll`；释放必须幂等（多删一次是无操作，少删一次就是泄漏）。
  3. **启动补票**：票是内存态、停服即失，重启后按 `OutpostCoreIndex` 的每条坐标重新申请（先补票、后补绑定）。
  4. **只用裸 `TicketType`**：`ServerLevel#setChunkForced` 与 NeoForge `TicketController#forceChunk` 都会写 `chunks.dat`，两者都否决（A-38）。
  5. **口径按 A-43 写**：`distance = 1` 时中心区块 level 32（方块实体 tick），紧邻一圈 level 33（FULL、加载但不 tick），再外一圈不再加载。所以「只加载 1 个区块」的准确含义是「**只有 1 个区块被 tick**，另外 8 个被加载到 FULL」；文档与验收一律按 tick 面写，**不写「只有 1 个区块被加载」**（那一条必红）。
- **依赖**：F-189
- **验收**：
  1. 无玩家在核附近（走远 200 格让本地区块卸载）时，该核的方块实体仍 tick：`latest.log` 里它的边界粒子轮次计数按 **40** 刻持续增长；拆核后走远 200 格，F3 的 `Chunk` 行显示该区块**未加载**（30 号 §7 #36）。
  2. `world/data/chunks.dat`（含 `DIM-1` / `DIM1`）的 `Forced` 数组里**不含**该核的 `ChunkPos.toLong()`（#36）。
  3. 停服再起服：票重建（重启后核的边界粒子在 40 刻内恢复）；`grep -c "setChunkForced\|forceChunk" src/main/java` = **0**。
  4. 幂等释放：连拆两个核、再停服，释放次数 = 申请次数；日志里每条带 `farmstorm_core` 类型名。
  5. tick 面口径（A-43）：中心区块的 F3 状态是「方块实体 tick 中」，紧邻一圈是「已加载」，两圈之外是「未加载」——三层各截一次。
- **回滚影响**：回滚后核只在区块加载时 tick（远处边界粒子不刷新）；F-199 / F-200 不依赖它；`chunks.dat` 全程不受影响（本任务从不写它）。

### Phase 30 · 浮窗与数据面（F-199 ~ F-205）

阶段目标：把本版唯一一条「破红线」的需求做完——1 个 S2C 只读包 + 一块自绘浮窗 + 2 个键位 + 3 键 CLIENT config + 1 条客户端指令。

#### F-199 · 1 个 S2C 只读包：payload、codec、注册与客户端快照（L·3.0）

- **目标**：全项目第一个自定义网络包——`farmstorm:core_list` 的 S2C payload + `StreamCodec` + `playToClient` 注册 + 客户端 handler 与快照；客户端只读、不回包、不做任何判定（D-142 / RT-31）。
- **涉及文件**：`network/CoreListPayload.java`、`network/ClientCoreListHandler.java`、`network/FarmstormNetwork.java`（注册）、`client/ClientCoreList.java`、`Farmstorm.java`（构造器挂 mod 总线监听）。
- **实现要点**：
  1. payload：`List<Core> cores` + `int total`；`Core(dimension, pos, ownerName, self)` 四字段；`TYPE = new Type<>(Farmstorm.id("core_list"))`——**不用** `CustomPacketPayload.createType(String)`（它会落进 `minecraft:` 命名空间）。
  2. codec：4 元 `StreamCodec.composite` + `ByteBufCodecs.list(MAX_CORES = 64)`；`total` **单独发**（`+N` 要真实总数，截断后的 `cores.size()` 答不了）。
  3. 注册：`event.registrar("1").playToClient(...)`，挂 mod 总线，只在 `play` 阶段、只 CLIENTBOUND。
  4. handler 只有一句 `context.enqueueWork(() -> ClientCoreList.set(...))`；客户端类只在 lambda 的合成方法被调用时解析，专用服务器不会 `NoClassDefFoundError`。
  5. 客户端快照：`volatile List<Core>` + `volatile int total`，只被 handler 写、只被渲染读。
- **依赖**：—
- **验收**：
  1. 全仓 `grep -rln "CustomPacketPayload" src/main/java` 恰 **1** 个文件（`network/CoreListPayload.java`）；`grep -rn "playToServer\|sendToServer" src/main/java` **0 命中**（30 号 §7 #6）。
  2. 专用服务器能完成启动、无 `NoClassDefFoundError`；`latest.log` 的注册行含 `farmstorm:core_list`（#6）。
  3. 单机发一个 `total=0` 的空包：客户端日志一条「收到核心列表 0 条（total=0）」；此时浮窗未落地，用日志取证。
  4. `grep -c "MAX_CORES" network/CoreListPayload.java` ≥ **2**（常量 + codec 使用）。
  5. `python -X utf8 tools/verify_project.py` 退出码 0、`结论：FAIL 0 项`（A-40 已收口）。
- **回滚影响**：回滚后浮窗没有数据面（F-200 / F-203 失去上游）；客户端快照类保留为空实现也不会崩，不使其它任务失效（除 F-200、F-203）。

#### F-200 · 广播器：组装、三级排序、去抖与 10 个推送时机（L·3.0）

- **目标**：把「己方核心列表」在服务端组装好并按 10 个时机推给该收的玩家；断线 / 换服不残留（D-142 / A-39 / RT-31）。
- **涉及文件**：`network/CoreListBroadcaster.java`、`event/FarmstormGameEvents.java`（+4 个玩家事件 + `ServerTickEvent.Post`）、`blockentity/OutpostCoreBlockEntity.java:95-103`、`:166-170`。
- **实现要点**：
  1. 组装：遍历索引全部 owner → 与 viewer 做 UUID 侧己方判定（F-187 的重载）→ 收候选 → 比较键**只从索引读** `gameTime`（不读 BE、不加载区块）→ 三级排序 → `total` = 候选总数、`cores` = 前 64 条。
  2. 去抖：`MIN_INTERVAL_TICKS = 10` 的 `lastSentTick` + `pending`，`ServerTickEvent.Post` 统一 flush；同一刻多次请求只发一次。
  3. 对账：`RECONCILE_INTERVAL_TICKS = 100` 的指纹比较，兜「队友换队 / 上线 / 漏事件」。
  4. 10 个时机照 30 号 §2.1.4 的表逐条挂；登出清 `lastSentTick` / `pending` / `fingerprint` 三条记录；客户端在 `ClientPlayerNetworkEvent.LoggingOut` 里清空快照。
  5. 收件人由每人自己的组装结果决定（不把敌对核发给客户端）。
- **依赖**：F-199、F-189、F-187
- **验收**：
  1. 单人 1 核：日志一条「核心列表下发：<玩家> 1 条（total=1）」（30 号 §7 #7 的日志一半）。
  2. 主世界 1 核 + 下界 1 核：站在任一维度收到的包是 `total=2`，两行维度标识分别是 `overworld` / `the_nether`，传送后不变（#8）。
  3. 排序（#9 的 ①④）：先放 A（t=100）再放 B（t=200）→ A 在前；同一份存档重启两次，`latest.log` 的两行「核心比较键指纹」完全相同。
  4. 去抖：同一刻放核 + 拆核（连锁触发）只产生 **1** 个包（同一刻只有一条下发记录）。
  5. 换服不残留（#29）：从服务器 X 退出进服务器 Y（Y 无核），浮窗立即消失；回 X 再出现。
  6. 敌对核不外发：第二客户端的核不在任何包内（日志「本包只含 N 条」对照 N 的实际值）。
- **回滚影响**：回滚后浮窗没数据（F-203 只画空）；F-199 的包仍在但无人发，不使其它任务失效。

#### F-201 · CLIENT config 3 键与 3 条语言键（M·1.5）

- **目标**：新增 `config/FarmstormClientConfig.java`（首次 `ModConfig.Type.CLIENT`），落盘 `config/farmstorm-client.toml` 的 `[hud]` 三键（D-144）。
- **涉及文件**：`config/FarmstormClientConfig.java`（新增）、`Farmstorm.java:56` 之后一行、`datagen/ModLangProvider.java`（+3 条）。
- **实现要点**：
  1. `enabled`（bool，**true**）、`x`（int，**4**，0 ~ 3840）、`y`（int，**4**，0 ~ 2160）；三个都有 `.translation(...)`。
  2. 注册点只引用 `FarmstormClientConfig`（它只依赖 `ModConfigSpec`，是公共类）；**不放进 `FarmstormConfig`**。
  3. 运行期写盘走 `ConfigValue#set(T)` + `SPEC.save()`，禁手写 `Files.write`。
  4. 值域只挡明显坏值，真正的边界在绘制 / 拖拽时按屏幕尺寸二次钳制。
- **依赖**：—
- **验收**：
  1. 首次起客户端生成 `run/config/farmstorm-client.toml`，`[hud]` 段 **3** 键，值 `enabled = true`、`x = 4`、`y = 4`；专用服务器**不生成**这一份（30 号 §7 #37 的载体面）。
  2. 手改 `x = 8` 重启客户端：内存值读到 8（日志一条「hud 位置读入 (8, 4)」）。
  3. 值域生效：把 `x` 改成 `5000` 重启，加载值被钳到 **3840**。
  4. `farmstorm-server.toml` 里不出现 `hud` 段；两份 toml 互不引用（拷给别人不会带走位置）。
  5. 中英各 **3** 条 `farmstorm.configuration.hud.*`，两份键集仍相等。
- **回滚影响**：回滚后按键与指令没有持久化载体（F-202 / F-205 依赖它），F-203 渲染读不到开关；三者排在其后。

#### F-202 · 2 个 `KeyMapping` 与 2 条描述键（M·1.5）

- **目标**：首次注册 `KeyMapping`（开关浮窗 **H**、进入拖拽 **J**），mod 总线 + `Dist.CLIENT`，并在既有 `ClientTickEvent.Post` 里消费（D-144）。
- **涉及文件**：`client/FarmstormKeys.java`（新增）、`client/FarmstormClient.java:78-81`、`datagen/ModLangProvider.java`（+2 条）。
- **实现要点**：
  1. 两个 `KeyMapping`，分类复用 `KeyMapping.CATEGORY_GAMEPLAY`（自建分类要多 1 条 `key.categories.*`，与 323 的账对不上）。
  2. `@EventBusSubscriber(modid, bus = MOD, value = Dist.CLIENT)` + `RegisterKeyMappingsEvent` 注册。
  3. 消费：`while (TOGGLE.consumeClick()) toggleHud();` / `while (DRAG.consumeClick()) openDragScreen();`；toggle 写 `enabled` 并存盘 + 动作栏回显。
- **依赖**：F-201
- **验收**：
  1. 按键界面的「游戏操作」页能看到两条本模组的键位，默认 **H / J**（30 号 §7 #11 的一半）。
  2. 按 H：`farmstorm-client.toml` 的 `enabled` 在 true / false 之间翻转；重启客户端保持上次状态（#11）。
  3. 按 J：打开一个 Screen（F-204 落地前是临时空 Screen，判据是「Screen 被打开且单人暂停」）。
  4. 中英各 **2** 条 `key.farmstorm.core_hud.*`，键集相等；`grep -c "key.categories.farmstorm"` = **0**（没有自建分类）。
  5. `grep -c "RegisterKeyMappingsEvent" client/FarmstormKeys.java` = **1**。
- **回滚影响**：回滚后开关只能靠指令（F-205），拖拽没有入口（F-204 的 Screen 只能被指令调起）；不使其它任务失效。

#### F-203 · 浮窗渲染 `CoreHudRenderer`（L·3.0）

- **目标**：`RenderGuiEvent.Post` 自绘左上角列表：一行一个核、**8 行核心 + 1 行 `+N`**、面板高 **93 px**，版式按 A-41 的「行高 9 / 行距 1 / 内边距 2」，可开关、按实测宽度钳制（D-143 / A-34 / A-41）。
- **涉及文件**：`client/CoreHudRenderer.java`（新增）、`datagen/ModLangProvider.java`（+1 条 `gui.farmstorm.core_hud.self`）。
- **实现要点**：
  1. 四条前置：无玩家 / 开关关 → 返回；`hideGui` 且非拖拽 → 返回；`screen != null` 且不是 `CoreHudDragScreen` → 返回；`ClientCoreList` 空 → 返回（**0 核不画**）。
  2. 行格式 `#%d [%s] (%d, %d, %d) %s`：序号 1 基、维度 id 的 **path**（不新增语言键）、自己取 `gui.farmstorm.core_hud.self`、队友取 `ownerName`。
  3. 面板高 `10n + 3`（8 行 83 px / 9 行 93 px，这是 29 号 §5.6 三个数的等价推论）；宽度 = 最长行 `font.width` + 4；绘制原点 `(hud.x, hud.y)` 先钳到 `[0, guiWidth − 宽] × [0, guiHeight − 高]`。
  4. 配色：背景 `0x80000000`、边框 `0xFF000000`、自己 `0xFF55FF55`、队友 `0xFFFFFFFF`、`+N` 灰 `0xFFAAAAAA`。
  5. `+N` 是 `"+" + (total − 8)` 的纯数字拼接，**不新增语言键**；序号按截断前的全量排序编号（`+N` 时编号连续不断）。
- **依赖**：F-199、F-200、F-201
- **验收**：
  1. 单人放 1 个核：浮窗出现 1 行，形如 `#1 [overworld] (x, y, z) 自己`；面板高 **13 px**（30 号 §7 #7）。
  2. 9 个己方核：**8 行核心 + 1 行 `+1`** = 9 行、面板总高 **93 px**；8 个核时 8 行、**83 px**、无 `+N`（#10、A-34）。
  3. 跨维度：主世界 + 下界各 1 核 → 2 行、行首 `overworld` / `the_nether`；传送后不变（#8）。
  4. 0 核不画：拆掉唯一核心后整块面板消失，`+N` 也不出现（29 号 B-01）。
  5. 开关：按 H 关掉后不画；`hideGui`（F1）时也不画（B-10 的一半）。
  6. 与蓄力条不相交：`guiHeight = 240` 时浮窗底边 y = 97，蓄力条带从底算 `[47, 53)`，两者不重叠（B-11）。
  7. 中英各 **1** 条 `gui.farmstorm.core_hud.self`。
- **回滚影响**：回滚后屏幕回到无浮窗，F-204 / F-205 的交互对象消失；不使其它任务失效。

#### F-204 · 拖拽定位 `CoreHudDragScreen`（M·1.5）

- **目标**：按 J 打开、拖动改 `hud.x` / `hud.y`、`ESC` 保存并退出；拖拽态是「编辑态」，浮窗继续画（D-144）。
- **涉及文件**：`client/CoreHudDragScreen.java`（新增）、`client/CoreHudRenderer.java`（编辑态例外）、`client/FarmstormKeys.java`（DRAG 分支）。
- **实现要点**：
  1. `isPauseScreen()` 返回 `true`（单人自动暂停）。
  2. `mouseClicked` 落在面板矩形内才开始拖，记抓取偏移；`mouseDragged` 写预览坐标并钳制；`mouseReleased` 结束拖动但**不写盘**；`onClose()` 写 config + `SPEC.save()`。
  3. 编辑态例外：`hideGui` 与 `screen != null` 两条早退在拖拽态都不生效（否则玩家看不到自己在拖什么）。
  4. 不画面板本体：面板仍由渲染器画，保证「拖的时候看到的就是最终效果」。
- **依赖**：F-203、F-202、F-201
- **验收**：
  1. 按 J：单人暂停（画面静止、世界不 tick）（30 号 §7 #14）。
  2. 拖到画面右下角后 `ESC`：`hud.x` / `hud.y` 等于落点坐标；重进游戏位置不变（#14）。
  3. 拖到屏幕外松手：`hud.x ≤ guiWidth − 面板宽`、`hud.y ≤ guiHeight − 面板高`，越界部分 **0 px**（#15，四个角各试一次）。
  4. 拖拽中改窗口尺寸：重新钳制，面板完整可见（29 号 B-08）。
  5. `ESC` 只做保存与退出：Screen 关闭后不弹暂停菜单（B-09）。
- **回滚影响**：回滚后位置只能靠手改 toml 或 `hud reset`（F-205）；F-203 的绘制钳制仍在，不使其它任务失效。

#### F-205 · 客户端指令 `/farmstorm hud <on|off|reset>` 与 3 条反馈键（M·1.5）

- **目标**：客户端指令注册（`RegisterClientCommandsEvent`），三个动作分别写 `enabled` / 复位位置，反馈用 `sendSuccess(..., false)`（本地显示、不广播）（D-144）。
- **涉及文件**：`client/CoreHudClientCommands.java`（新增）、`client/FarmstormClient.java`（`RegisterClientCommandsEvent` 订阅）、`datagen/ModLangProvider.java`（+3 条）。
- **实现要点**：
  1. `dispatcher.register(Commands.literal("farmstorm").then(Commands.literal("hud")...))`，三个子命令**不带** `requires(...)`。
  2. `reset` 只复位位置（`x = y = 4` 并 `save()`），**不动 `enabled`**。
  3. 反馈 `sendSuccess(..., false)`，与已删的服务端指令（`true`）相反。
  4. 该指令与服务端注册点不共用根节点语义（服务端已无 `/farmstorm`，F-197）。
- **依赖**：F-201
- **验收**：
  1. `/farmstorm hud off` → `farmstorm-client.toml` 的 `enabled = false`；`on` → `true`；`reset` → `x = 4`、`y = 4` 且 `enabled` 不变（30 号 §7 #13）。
  2. 单人客户端补全 `/farmstorm ` 只有 `hud`，其下只有 `on` / `off` / `reset`；`/farmstorm farmlandlimit 5` 失败（#12 的客户端一半）。
  3. 反馈只出现在本地聊天栏，不进服务器广播（第二客户端看不到）。
  4. 中英各 **3** 条 `command.farmstorm.hud.*`，键集相等。
  5. `grep -c "sendSuccess" client/CoreHudClientCommands.java` = **3**，第二参数都是 `false`。
- **回滚影响**：回滚后开关只剩按键（F-202），`reset` 失去入口；不使其它任务失效。

### Phase 31 · 收口与全量验收（F-206 ~ F-212）

阶段目标：把语言键总账、发布产物与两套判据（30 号 §7 的 38 条验收 + 29 号 §11 的 39 条边界）一次收口，产出一份可复核的验收记录。

#### F-206 · 语言键总账 323 × 2 与 `ModLangProvider` 注释（M·1.5）

- **目标**：把本版语言键的增删一次对平：**315 − 2 + 10 = 323**，中英键集逐字相同，`ModLangProvider` 类注释里的键数叙述与产物一致（30 号 §4.3 / §9 第 7 条 / RT-40）。
- **涉及文件**：`datagen/ModLangProvider.java`（`:15`、`:27-32` 的注释）、`src/generated/resources/assets/farmstorm/lang/zh_cn.json` / `en_us.json`（datagen 产物）。
- **实现要点**：
  1. 逐类复核：`farmstorm.configuration` **169**（= 166 server + 3 client）、`gui.farmstorm` **9**、`command.farmstorm` **3**、`key.farmstorm` **2**，其余类别与改前相同。
  2. 类注释里「299 + 16 = 315」那一类叙述改成改后的算式；不新增 / 不删减任何键（增删已由 F-182 / F-192 / F-197 / F-201 / F-202 / F-203 / F-205 各自完成）。
  3. 不动 `en_us` 的文案风格；两份键集必须逐字相同。
- **依赖**：F-182、F-192、F-197、F-201、F-202、F-203、F-205
- **验收**：
  1. 两份 lang 各 **323** 键、键集完全相同（工具 §3 段「lang 键集一致，共 323 键」；30 号 §7 #5）。
  2. `grep -c '"farmstorm.configuration.'` 恰 **169**；`grep -c farmlandlimit` 两份都是 **0**（#5）。
  3. 前缀分布逐行相加 = 323（`169 + 36 + 32 + 26 + 16 + 13 + 9 + 6 + 4 + 3 + 2 + 2 + 3 + 1 + 1`）。
  4. 类注释与产物一致：注释里出现的四个数（315、323、169、2）都能在产物里读到对应事实；`grep -c "299 + 16 = 315"` = **0**。
- **回滚影响**：回滚只把注释改回旧叙述、不改变产物；键集由各任务的产物决定，回滚本任务不会少键。

#### F-207 · 构建产物与 `dist/` 放行（S·0.5）

- **目标**：产出可发布的 `farmstorm-0.1.1+mc1.21.1.jar` 与 `dist/` 副本，`jar tf` 条目逐项对上（1 张替换 PNG、无新增 PNG / JSON），`chunks.dat` 不含核坐标（D-124 / D-147 / A-38）。
- **涉及文件**：`build/libs/`、`dist/`（产物，不入库）。
- **实现要点**：`.tools\gw.cmd build` → 复制 jar 与 `-sources.jar` 到 `dist/`；核对 `META-INF/neoforge.mods.toml` 的版本、`textures/item/wild_apple.png` 的尺寸、无新增图条目。
- **依赖**：F-176、F-179、F-198、F-199、F-203、F-206
- **验收**：
  1. `dist/farmstorm-0.1.1+mc1.21.1.jar` 存在；`jar tf` 里 `assets/farmstorm/textures/item/wild_apple.png` **1** 条，本版无新增 PNG 条目（与改前条目清单相比只差这一张的内容）。
  2. `jar tf` 里 `farmstorm/network/CoreListPayload.class`、`farmstorm/client/CoreHudRenderer.class`、`farmstorm/outpost/CoreChunkTickets.class` 各 **1** 条。
  3. `META-INF/neoforge.mods.toml` 的 `version="0.1.1"`；`build/libs` 里没有 0.1.0 / 0.0.1 的残留产物。
  4. `world/data/chunks.dat`（含 `DIM-1` / `DIM1`）的 `Forced` 数组不含任何核的 `ChunkPos.toLong()`（30 号 §7 #36）。
  5. 纯服务端可起：专用服务器加载该 jar 完成启动，`latest.log` 无 `NoClassDefFoundError`。
- **回滚影响**：只影响发布产物、不影响源码；回滚后 `dist/` 回到上一版 jar。

#### F-208 · 30 号 §7 的 #1 – #19 验收（M·1.5）

- **目标**：把 `30-TECH-0.1.1.md` §7 的前半 19 条逐条跑一遍，产出可复核的验收记录（不改码）。
- **涉及文件**：验收记录（新建，落 `.work/` 或提交信息）；不改源码。
- **实现要点**：每条给「动作 / 预期 / 实测 / 结论」四列；观察位置按 §0.3 的七类；涉及工具的条目（#1 的注册面、#5）在 A-40 收口后引用 `FAIL 0`。
- **依赖**：F-182、F-188、F-189、F-190、F-191、F-192、F-193、F-194、F-197、F-199、F-200、F-201、F-202、F-203、F-204、F-205、F-206
- **验收**：
  1. #1 – #19 逐条有实测读数与观察位置；未通过 **0** 条；任一条不通过即回到对应任务重做，**不就地改判据**。
  2. #1 的启动日志逐字含「自检 注册对象 93 个：…」，十二类逐项相等。
  3. #5 的两份 lang 是 **323** 键、`farmstorm.configuration.` **169** 条、`farmlandlimit` **0** 命中。
  4. #19 的 N 与 M 两次启动相同（A-47），补不上的方块仍是 `minecraft:farmland`。
  5. 记录以表格落盘，每条四列齐全，不得只写「通过」。
- **回滚影响**：不改码，无回滚面。

#### F-209 · 30 号 §7 的 #20 – #38 验收（M·1.5）

- **目标**：把 §7 的后半 19 条跑一遍（含迁移三支、A-35 降级、韭场时序、ticket 释放、man瓜 三项、红线扫描与 A-40 工具口径）。
- **涉及文件**：验收记录；不改源码。
- **实现要点**：逐条给四列；#38 的判据是「工具改完之后」的 `FAIL 0` + 退出码 0 + config 166（A-40）；#37 的三个计数一次读齐。
- **依赖**：F-177、F-179、F-180、F-181、F-182、F-183、F-184、F-185、F-186、F-192、F-196、F-198、F-199、F-200、F-203
- **验收**：
  1. #20 – #38 逐条有实测读数；未通过 **0** 条。
  2. #22 变 `minecraft:dirt`（与近处一致）、无掉落物，且日志**不出现**「迁移失败且区块未加载：只摘绑定」；该串以代码审阅确认存在于 `OutpostCoreBlockEntity.java:374`、正常运行不出现。
  3. #30 的两轮（200 / 100）都成立；#32 的卸载回来不残留。
  4. #33 的 F3 `Light` = **15**；#34 的三项读数（≈2000 ms / **3** 点 / ≈ **33** 刻）。
  5. #36 的 `chunks.dat` 不含核坐标；#37 的 `@Mixin` **0** 命中、`CustomPacketPayload` 恰 **1** 文件、工具 `93 / 93`；#38 的工具输出 `结论：FAIL 0 项`、退出码 0、config 段 **166**。
- **回滚影响**：不改码，无回滚面。

#### F-210 · 29 号 §11 的 B-01 – B-20 边界复跑（M·1.5）

- **目标**：把 29 号 §11 的前 **20 条**边界（B-01 ~ B-20：浮窗 14 条 + 耕地前 6 条）**逐条**做对照实验，每条边界对应一条验收；其中 **14 条**（B-01、B-02、B-03、B-05、B-08、B-10、B-11、B-13、B-14、B-15、B-17、B-18、**B-19、B-20**）在 30 号 §7 里**没有**对应条目（#20 只写「迁移成功 / 变泥土」，不区分「接替核已满」与「不同盟」两因），本任务是它们唯一的判据（A-46）。
- **涉及文件**：边界记录 `.work/v011-verify/F-210.md`；截图 / 录屏放 `.work/v011-verify/`；不改源码。
- **实现要点**：
  1. 一条边界一条验收，编号必须出现在验收条目里；判据落在 §0.3 的七类位置之一。
  2. **纯观感类**（B-01、B-02、B-10、B-11）写「人工目视 + 截图存 `.work/v011-verify/<B-xx>.png`」，**不许跳过、不许只写「通过」**。
  3. 两条读数并排的（B-18 的额度面与踩踏面）必须在同一段记录里同时给出，避免只验一半。
  4. A-49（耕地跨维度同坐标撞车）是已登记的**已知限制**，**不在**这 39 条里，本任务不为它设通过项，只在记录末尾列一行。
- **依赖**：F-189、F-191、F-193、F-194、F-195、F-196、F-199、F-200、F-201、F-203、F-204
- **验收**（**20 条 = B-01 ~ B-20 一一对应**）：
  1. **B-01** 0 核不画：拆掉唯一核心后整块面板与 `+N` 都不出现（人工目视 + 截图 `.work/v011-verify/B-01.png`）。
  2. **B-02** 1 核高度：只放 1 个核，面板高 **13 px**（截图 + 像素测量，`.work/v011-verify/B-02.png`）。
  3. **B-03** 队友核心 + 队友离线：B 放核后下线，A 的浮窗仍有那一行、名字是 B 的 `ownerName`。
  4. **B-04** 换维度：主世界 → 下界 → 末地各切一次，行数与顺序不变，行首维度标识仍是**核自己的**维度。
  5. **B-05** 重连首帧：重新登录后第一帧为 0 行，随后一次刷新出现完整列表（两帧截图 + 日志时间戳顺序）。
  6. **B-06** 超过 8 个核：8 行核心 + 1 行 `+N`，N = 总数 − 8。
  7. **B-07** 拖到屏外：四个角各试一次，`hud.x ≤ guiWidth − 宽`、`hud.y ≤ guiHeight − 高`，越界 **0 px**。
  8. **B-08** 拖拽中改 GUI 缩放：缩放 2 → 4，面板被重新钳制且完整可见。
  9. **B-09** `ESC` 只保存：Screen 关闭后不弹暂停菜单，`hud.x` / `hud.y` 已是落点值。
  10. **B-10** F1 / 背包：`F1` 与 `E` 打开背包时都不画；拖拽态（J）下仍画（截图 `.work/v011-verify/B-10a.png` / `B-10b.png`）。
  11. **B-11** 与蓄力条：`guiHeight = 240` 时浮窗底边 y = 97、蓄力条带从底算 `[47, 53)`，两元素不重叠（截图量像素）。
  12. **B-12** 同刻两核：离线把两核 `gameTime` 改成同一值 → 按维度 id → 坐标字典序排；两次重启顺序相同。
  13. **B-13** 敌对核 / 核心物品：第二客户端（不同队）的核不出现在列表；手持核心物品不产生行。
  14. **B-14** 首帧未收到列表：断线重连瞬间按 0 行处理，不画上一次会话的数据。
  15. **B-15** 无覆盖核不能开垦：站在核 3×3 之外锄地 → 失败、方块不变、提示 `no_core`、耐久不变。
  16. **B-16** 覆盖核都满：满 16 格后第 17 格被拒 + `QUOTA_FULL`，`ItemDamage` 前后相同。
  17. **B-17** 最近满但另一核有空位：绑到有空位的那个（`bindings.corePos` = 另一核的坐标）。
  18. **B-18** 换队：额度与浮窗立刻按新队重算、**踩踏面仍受保护**（三行读数并排，口径见 A-45）。
  19. **B-19** 核毁 + 接替核已满：该田变 `minecraft:dirt`（不等空位）。
  20. **B-20** 核毁 + 接替核不同盟：变泥土，且**不通知耕地主**（无聊天、无包变化提示）。
- **回滚影响**：不改码，无回滚面。

#### F-211 · 29 号 §11 的 B-21 – B-39 边界复跑（M·1.5）

- **目标**：把 29 号 §11 的后 **19 条**边界（B-21 ~ B-39：耕地后段 5 条 + man瓜 3 条 + 朋友的韭 7 条 + 干草 / 核心 / 苹果 4 条）**逐条**做对照实验，每条边界对应一条验收；其中 **7 条**（**B-21**、B-24、B-25、B-26、B-31、B-32、B-35）在 30 号 §7 里**没有**对应条目（#21 只写「迁不动 + 区块在内存 → 泥土」，不覆盖「地块已不是耕地」这一支），本任务与 F-210 合起来正好 21 条（14 + 7，A-46）。
- **涉及文件**：边界记录 `.work/v011-verify/F-211.md`；截图 / 录屏 / sha256 清单放 `.work/v011-verify/`；不改源码。
- **实现要点**：
  1. 一条边界一条验收，编号必须出现在验收条目里；判据落在 §0.3 的七类位置之一。
  2. **纯观感类**（B-36 的五类声音、B-38 的抽检）写「人工目视 / 人工听觉 + 截图或录屏存 `.work/v011-verify/<B-xx>-<子项>.*`」，**不许跳过**。
  3. B-24 / B-25 依赖「老档 + 容量硬顶」的构造，先在本地备一份 20 块老田 + 1 个核的存档再跑，操作序列写进记录。
  4. A-49（耕地跨维度同坐标撞车）是已登记的**已知限制**，**不在**这 39 条里，本任务不为它设通过项，只在记录末尾列一行。
- **依赖**：F-178、F-179、F-180、F-181、F-182、F-184、F-185、F-186、F-188、F-190、F-196
- **验收**（**19 条 = B-21 ~ B-39 一一对应**）：
  1. **B-21** 核毁时地块已非耕地：把该格 `/setblock stone` 后拆核 → 跳过、摘绑定、石头不变。
  2. **B-22** 核毁时的区块边界（A-60）：走远 200 格后 `/execute in` 拆核 → 该田变 `dirt`（与近处一致）、无掉落；「只摘绑定」为不可达的防御分支，以代码审阅确认 `OutpostCoreBlockEntity.java:374` 的日志串存在、正常运行不出现。
  3. **B-23** 补建时某核已满：该地保持 `farmland`、无绑定、**踩得坏**（对照：同一批里绑上的那块踩不坏）。
  4. **B-24** 老档超额：20 块老田 + 1 核（容量 16）→ 16 块受保护、4 块不受保护（逐块踩坏对照），**0 个变泥土**。
  5. **B-25** 补建失败的恢复：下一次启动若仍无空位则仍不受保护；挖掉后重开垦一次即恢复保护（两条路径各验一次）。
  6. **B-26** 目标在飞行途中死亡：`target` 置空、炮弹直线飞出、撞方块消失，`latest.log` 无 NPE。
  7. **B-27** 贴脸（`dist < 0.45`）：收敛命中，5 发 5 中。
  8. **B-28** 旧 toml 不迁移：保留 0.1.0 的 toml → `fireIntervalTicks = 60`、`projectileDamage = 1`，`bulletSpeed` 自动补 **0.45**。
  9. **B-29** 空投：球内 0 生物不播音，**但场仍持续满时长**，第 `effectTicks` 刻实体消失。
  10. **B-30** 中途进入：拿 `effectTicks − x` 刻，仍在第 `effectTicks` 刻到期。
  11. **B-31** 中途离开球：效果随人走完、**不提前结束**（剩余时长仍按原到期时刻递减）。
  12. **B-32** 投掷者本人站球内：同样中招（`/data get` 的 Q弹 `Duration > 0`）。
  13. **B-33** 区块卸载：场结束，重载不续（实体不存在）。
  14. **B-34** 第 `effectTicks` 刻自毁：`/data get entity @e[type=farmstorm:friend_chives]` 查不到。
  15. **B-35** 两场重叠：取更强（同 amplifier 比时长），先中招者的到期时刻**不被缩短**。
  16. **B-36** 五类声音：破坏 / 放置 / 踩踏 / 挖掘 / 落地各一次正负对照（人工听觉 + `/playsound` 对照；录屏存 `.work/v011-verify/B-36-<类>.mp4`）。
  17. **B-37** 光照与刷怪：F3 `Light = 15`、15 格内有光斑；光斑正中 100 秒 **0** 只怪、15 格外 ≥ 1 只。
  18. **B-38** 其余 24 张仍带「+」：24 张 PNG 的 sha256 清单 + 抽 3 张目视确认右下角绿十字仍在（截图 `.work/v011-verify/B-38-*.png`）。
  19. **B-39** 生成器与发布件（A-61）：**本节不重跑生成器**（不属于发布动作）；入库的 `wild_apple.png` 为 **64×64 / 6,946 B / 与源图同 sha256**，其余 24 张与提交前逐张相同（以 git 字节为准）；重跑会剥掉那 24 张的绿十字，属 D-125 的**预期结果**、不判不通过。
- **回滚影响**：不改码，无回滚面。

#### F-212 · 全量汇总与上游一致性签字（L·3.0）

- **目标**：把 38 条验收 + 39 条边界 + 37 个任务的状态汇成一份结论，并逐条复核本文件 §5 的 **27 条**裁决（A-30 ~ A-56，其中 A-44 作废）与 §6 的七张表。
- **涉及文件**：汇总记录；不改源码。
- **实现要点**：五处计数一次读齐；七张表逐张复核覆盖数；裁决逐条给出落地任务与回灌状态；仍开着的分歧列为 0 条。
- **依赖**：F-208、F-209、F-210、F-211
- **验收**：
  1. 38 + 39 = **77 条**逐条有结论，未通过 **0** 条；37 个任务全部有「完成 / 未完成」状态。
  2. 五处计数一次读齐：注册 **93**、server config **166**、client config **3**、语言键 **323 × 2**、任务 **37**。
  3. 七张表逐张成立：表 A **44/44**（+ 补记 2 行）、表 B **20** 项、表 C **7** 项、表 D **5** 项、表 E **11/11**、表 F **8/8**、表 G **17** 行（其中 **8** 条新裁决各有落地任务，A-44 作废）。
  4. 红线只留「无 Mixin」：`grep -rn "@Mixin\|mixins.json" src/main/java` **0** 命中；`CustomPacketPayload` 恰 **1** 文件；`GameRules.register` 仍 **0** 命中（工程约束保留，D-146 补充）。
  5. `python -X utf8 tools/verify_project.py` → `结论：FAIL 0 项`、退出码 0、config 段 **166**、`93 / 93`、`323 键`。
- **回滚影响**：不改码，无回滚面。

---

## 3. Phase 与里程碑

六个 Phase 与六个里程碑一一对应（M22 ~ M27）。每个 Phase 给一句**出口判据**：该 Phase 全部任务提交后，系统处于什么可观察状态。

| Phase | 名称 | 任务 | 任务数 | 人日 | 里程碑 |
|---|---|---|---|---|---|
| 26 | 基线与工具链 | F-176 ~ F-177 | 2 | 2.0 | **M22** |
| 27 | 材质、数值与方块属性 | F-178 ~ F-184 | 7 | 11.5 | **M23** |
| 28 | 朋友的韭持续场 | F-185 ~ F-186 | 2 | 4.5 | **M24** |
| 29 | 耕地-核心绑定制 | F-187 ~ F-198 | 12 | 21.5 | **M25** |
| 30 | 浮窗与数据面 | F-199 ~ F-205 | 7 | 15.0 | **M26** |
| 31 | 收口与全量验收 | F-206 ~ F-212 | 7 | 11.0 | **M27** |
| **合计** | | **F-176 ~ F-212** | **37** | **65.5** | **M22 ~ M27** |

**Phase 26 · 基线与工具链 → M22**
出口判据：`build/libs/farmstorm-0.1.1+mc1.21.1.jar` 存在且 `neoforge.mods.toml` 的 `version=0.1.1`；`tools/verify_project.py` 的 config 期望集由 30 号 §4.1 现算（**58 组 / 166 键**）并剔除 `farmland.limitPerPlayer`，中间态只剩两条键级差异（缺 `bulletSpeed`、多 `farmland.limitPerPlayer`）。

**Phase 27 · 材质、数值与方块属性 → M23**
出口判据：`wild_apple.png` 是 **64×64 / 6,946 B**，删图重跑生成器不回落、不报尺寸断言；生成器里 `add_plus` / `build_marker` / `compose_icon` **0 命中**且其余 24 张 PNG 字节不变；压缩干草块**五类**声音全是石头类；核心 F3 的 `Light = 15`；删旧 toml 起服后 `[pumpkin_turret]` 5 键为 40 / 3 / 0.45 / 15 / 1；目标贴 1 格内连打 5 发全中、每发掉 3 点。

**Phase 28 · 朋友的韭持续场 → M24**
出口判据：落地后是 `friend_chives.effectTicks` 刻（默认 200、可配 100 / 1000）每刻扫描的场，第 `effectTicks` 刻实体消失；空投不播音、命中只播一次；区块卸载回来不留残留、不复活。

**Phase 29 · 耕地-核心绑定制 → M25**
出口判据：绑定制全链可跑——锄地绑最近的己方核、第 17 格被拒、核主下线仍受保护、拆核先迁移（改绑 / 变泥土 / 只摘绑定三支）、老档启动补建 N/M 可复现、`/farmstorm farmlandlimit` 消失、`[farmland]` 段消失且组数 **58**、核所在区块有临时 ticket 且 `chunks.dat` 不含核坐标；此 Phase 结束时 `verify_project.py` 首次 `FAIL 0`（A-40 收口）。

**Phase 30 · 浮窗与数据面 → M26**
出口判据：主世界 + 下界各 1 核时浮窗 **2 行**、9 核时 **9 行 93 px**（8 + `+1`）；按 **H** 开关持久化、按 **J** 拖拽 `ESC` 保存、`/farmstorm hud on|off|reset` 三个动作都改到 `farmstorm-client.toml`；换服不残留；全仓 `CustomPacketPayload` 恰 1 文件且客户端不回包。

**Phase 31 · 收口与全量验收 → M27**
出口判据：两份语言文件各 **323** 键且键集相同；`dist/farmstorm-0.1.1+mc1.21.1.jar` 可发布、纯服务端可起；30 号 §7 的 **38 条**与 29 号 §11 的 **39 条**全部通过（未通过 0 条）；红线只剩「无 Mixin」；`verify_project.py` 输出 `FAIL 0` / 退出码 0 / `93 / 93` / `166` / `323 键`。

---

## 4. 工时汇总

### 4.1 按 Phase

| Phase | 任务数 | S | M | L | 人日 |
|---|---|---|---|---|---|
| 26 基线与工具链 | 2 | 1 | 1 | 0 | `1 × 0.5 + 1 × 1.5` = **2.0** |
| 27 材质、数值与方块属性 | 7 | 2 | 3 | 2 | `2 × 0.5 + 3 × 1.5 + 2 × 3.0` = **11.5** |
| 28 朋友的韭持续场 | 2 | 0 | 1 | 1 | `1 × 1.5 + 1 × 3.0` = **4.5** |
| 29 耕地-核心绑定制 | 12 | 1 | 8 | 3 | `1 × 0.5 + 8 × 1.5 + 3 × 3.0` = **21.5** |
| 30 浮窗与数据面 | 7 | 0 | 4 | 3 | `4 × 1.5 + 3 × 3.0` = **15.0** |
| 31 收口与全量验收 | 7 | 1 | 5 | 1 | `1 × 0.5 + 5 × 1.5 + 1 × 3.0` = **11.0** |
| **合计** | **37** | **5** | **22** | **10** | **65.5** |

### 4.2 总计（两个数 + 算式）

- **裸人日**：`5 × 0.5 + 22 × 1.5 + 10 × 3.0 = 2.5 + 33.0 + 30.0 = **65.5 人日**`
- **含 25% 缓冲**：`65.5 × 1.25 = **81.875 人日**`

三条互相校验：

1. 复杂度汇总：S 5 + M 22 + L 10 = **37** = 任务数；
2. 人日汇总：`2.5 + 33.0 + 30.0 = 65.5` = 六个 Phase 小计之和 `2.0 + 11.5 + 4.5 + 21.5 + 15.0 + 11.0 = 65.5`；
3. 缓冲：`65.5 × 1.25 = 81.875`，与里程碑表（§3）逐行相加相同。

> 缓冲只吸收返工与调试，不吸收范围变化。0.1.0 的对照：40 个任务 / 63.0 裸人日 / 78.75 含缓冲；本版 37 个任务的裸人日略高，原因是两处 schema 变更与四个「首次」（第一个自定义包、第一份 CLIENT config、第一批 `KeyMapping`、第一条客户端指令）各按上一档计。

### 4.3 关键路径（按依赖图取最长前驱）

```
F-199 网络包 3.0                             （累计 3.0）
 └─ F-200 广播器 3.0                          （累计 6.0）   ← 另需 F-189 的比较键先落地
     └─ F-203 浮窗渲染 3.0                     （累计 9.0）
         └─ F-206 语言键总账 1.5               （累计 10.5）
             └─ F-208 §7 的 #1–#19 1.5         （累计 12.0）
                 └─ F-212 汇总签字 3.0          （累计 15.0）
```

**关键路径 = 15.0 人日 / 6 个任务**，占总量 65.5 的 **22.9 %**。这条链是纯依赖推算的结果（每个节点取所有前驱的累计最大值）：浮窗的数据面（F-199 → F-200）→ 渲染（F-203）→ 语言键总账（F-206）→ 前半验收（F-208）→ 汇总（F-212）。**浮窗线是最长的一条，不是绑定制线**——绑定制线（F-188 → F-195 → F-196 → F-211）只有 `3.0 + 1.5 + 3.0 + 1.5 = 9.0` 人日，因为它的分支多、可并行度高。

**并列的第二条 15.0**：`F-199 → F-200 → F-203 → F-204 → F-210 → F-212`。两条链在 `F-203` 之后分叉，各自再汇到 F-212；排期时把 F-204 与 F-206 视作同一层即可（二者都只等 F-203）。

次长链（同样按依赖取最长前驱，不进关键路径）：

| 排名 | 链 | 人日 |
|---|---|---|
| 3 | F-188 → F-195 → F-196 → F-211 → F-212 | 12.0 |
| 4 | F-182 → F-183 → F-184 → F-209 → F-212 | 10.5 |
| 5 | F-178 → F-179 → F-211 → F-212 | 9.0 |

**关键路径不到总量四分之一**，说明本版工期由**总工作量与并行度**决定，而不是由最长的链决定：想缩短日历工期，优先加并行（Phase 29 的 12 个任务里有 4 条可并行的分叉），其次才是压这条浮窗链。反过来说，**F-199 → F-200 → F-203 这一段（9.0 人日）不能压**：它是本版唯一一条「先有数据面、才有画面、才有验收」的串行链，压掉任何一环都会让后面三批验收全部无判据。

### 4.4 并行度

| 簇 | 位置 | 可并行任务 | 并行度 | 说明 |
|---|---|---|---|---|
| A | Phase 26 ~ 27 | F-178、F-180、F-181、F-182 | 4 | F-176 → F-177 是串行链（先改版本、再改工具）；F-178 / F-180 / F-181 / F-182 只等 F-177（它们都不改工具），四条互不共享文件 |
| B | Phase 27 | F-183 → F-184 | 1 | 两任务改同一个类，天然串行；可与簇 A 并行 |
| C | Phase 28 ~ 29 | F-185 → F-186 与 F-187、F-188、F-189 | 4 | 韭场线与绑定制线互不依赖，只共享 F-177 之后的基线 |
| D | Phase 29 | F-190、F-191、F-195 | 3 | 三条都在 F-188 之后分叉；F-191 → F-192 → F-197 是一条删除链（其中 F-191 → F-192 是**编译顺序**，见 A-56），F-193 → F-194 → F-196 是另一条 |
| E | Phase 30 | F-201、F-199 | 2 | CLIENT config 与网络包互不依赖；F-202 ~ F-205 依次收口到 F-203 |
| F | Phase 31 | F-207、F-208、F-209、F-210、F-211 | 5 | F-206 之后分五条：F-207 发布链、F-208 前半验收、F-209 后半验收、F-210 / F-211 边界；F-212 是强制汇合点（它等全部四条验收产线） |

**并行度上限 4**（簇 A / C 各 4）。实际受**同文件冲突**约束：F-178 与 F-179 都改 `tools/gen_textures.py`（已串成 178 → 179）；F-183 与 F-184 都改 `TurretBullet.java`（已串成 183 → 184）；F-188 / F-190 / F-191 都改 `FarmlandLedger.java`（三者分属 Phase 29 的不同位置，按 188 → 189 / 191 排）；F-193 / F-195 都改 `FarmlandGuard.java`（按 193 → 195 排）；F-199 / F-200 都改 `network/`（按 199 → 200 排）。按「一个任务只追加自己那一段」的约定后，可稳定并行 **3 路**：`65.5 ÷ 3 ≈ 21.8 人日`，缓冲后 ≈ **27.3 人日**（约 5.5 周 @ 5 天/周）；单人串行 `81.875` 人日。

---

## 5. 裁决 A-30 ~ A-56 收录与回灌状态（27 条）

本节按三段收：**表内 12 行**原样收录 `.work/v011-verdicts.md` 的 A-30 ~ A-41；**§5.1** 本文新立 / 复核改判的 8 条（A-42 ~ A-49，其中 **A-44 已作废、由 A-47 取代**）；**§5.2** 其余 7 条（A-50 ~ A-56：A-50 / A-51 / A-53 / A-55 是 28 / 30 号侧的登记，A-52 落 30 号，A-54 / A-56 是 31 号自己的修正单）。合计 **27 条（A-30 ~ A-56），连续无跳号**。

| 编号 | 结论（一句话） | 落在哪个任务 | 回灌目标文档 | 状态 |
|---|---|---|---|---|
| **A-30** | man瓜 弹速提成 config 后，`behavior/impl/PumpkinTurret.java:75` 的发射点**必须同步改读 config**——删常量不是单点改动 | **F-183**（两处读点同一次提交） | 28 号 D-129 | 台账：**已回灌** ｜ 本稿复核：已回灌 |
| **A-31** | 压缩干草块改音效影响**五类**声音（破坏 / 放置 / 踩踏 / 挖掘 / 落地），验收项比原估多 2 条 | **F-180**（五条正负对照） | 28 号 D-130 补充 | 台账：**已回灌** ｜ 本稿复核：已回灌 |
| **A-32** | `gen_textures.py` 的**尺寸断言与 `expected_size()` 一起放开**，否则 64×64 直通输出会 `SystemExit` | **F-179** | 28 号 §3.2 第 14 行 + §3.3 第 8 行 | 台账：**已回灌** ｜ 本稿复核：已回灌 |
| **A-33** | 朋友的韭「200」的口径：**场长与施加基准都取 `friend_chives.effectTicks`**，施加量 = `effectTicks − x` | **F-185**（代码里只留 `fieldLength` 一个来源） | 28 号 D-131 | 台账：**已回灌** ｜ 本稿复核：已回灌 |
| **A-34** | 浮窗「8 行」是 **8 行核心**，`+N` **另加 1 行**，最高 **9 行**（面板高 83 px → 93 px） | **F-203** | 28 号 D-143 结论第 4 项 | 台账：**已回灌** ｜ 本稿复核：已回灌 |
| **A-35** | 核被毁时「找不到接替者 + 该耕地所在区块未加载」→ **只摘绑定、世界不动** | **F-196**（三分支里的第三支） | 28 号 D-136 补充 | 台账：**已回灌** ｜ 本稿复核：已回灌；**A-60 收窄：第三支降为防御分支**（票恒覆盖同一 3×3 ⇒ 正常运行不可达，拆核实际两级） |
| **A-36** | 老档补建的遍历顺序**原**定为 `FarmlandLedger.byOwner` 的记录次序（`LinkedHashSet`，`FarmlandLedger.java:51`）；**该口径已由 A-47 取代**（A-44 作废，见后） | **F-190**（改按 A-47 的全序排序）、**F-188**（v2 落盘不再给顺序附加语义） | 28 号 D-137 补充 | 台账：**已回灌** ｜ 本稿复核：已回灌；**顺序部分已被 A-47 覆盖** |
| **A-37** | **验收断言不得写「调用点数 = 1」**；绑定制引入「摘绑定」后写入口不再唯一 | **本文件 §0.1 的三条硬约定**（全文 37 个任务共同遵守） | 31 号验收章 + 30 号 §7 | 台账：**待回灌（31 号出稿时收口）** ｜ 本稿复核：**本稿即收口点**，已落 §0.1；30 号 §7 表头纪律①已按同口径写 |
| **A-38** | 强制加载的落盘文件是 **`world/data/chunks.dat`**（不是 `level.dat`）；`setChunkForced()` 与 `TicketController#forceChunk` **都会写它 ⇒ 两者都否决** | **F-198**（只用裸 `TicketType` + `addRegionTicket`） | 28 号 D-145 + 事实卡 §2 #24 + 30 号 §2.5 | 台账：**28 号已回灌 / 30 号待** ｜ 本稿复核：30 号 §2.5.1-2、§3 第 39 行、§7 #36 已写，**已回灌** |
| **A-39** | 「平手按先放置」的实现口径 = **`placedGameTime` 升序 → 维度 id → 坐标字典序**；**不得依赖任何 Map / List 的插入顺序**；取值载体不加载区块 | **F-189**（载体落索引）、**F-193**（候选比较）、**F-190**（补建同用）、**F-200**（浮窗同用） | 28 号 D-135 + 29 号 §3.4 + 30 号 §2.2 / §7 #9 | 台账：**28、29 已回灌 / 30 待** ｜ 本稿复核：30 号 §2.2.10 已写，**已回灌** |
| **A-40** | `verify_project.py` 的 config 期望来源必须改指 v0.1.1；**在它改完之前「工具全绿」不可用** | **F-177**（改造）、**F-192**（收口点）、F-208 / F-209 / F-212（引用面） | 30 号 §3 + §7 + 31 号任务 | 台账：**30、31 待** ｜ 本稿复核：30 号 §3 第 43 行 + §7 #38 已写；31 号即本稿 §0.4，**已回灌** |
| **A-41** | 浮窗版式统一到 29 号的分解（**行高 9 + 行距 1 + 内边距 2**），30 号的 `10n + 3` 是等价推论 | **F-203**（照 29 号 §5.6 的三个数实现） | 30 号 §2.4.4 + 29 号 §5.6 | 台账：**待回灌** ｜ 本稿复核：29 号 §5.6「唯一口径（A-41）」段与 30 号 §2.4.4 都已按 A-41 写，**已回灌** |

### 5.1 本文新立 / 改判的 8 条（A-42 ~ A-49，其中 A-44 已作废）

**A-42 · 30 号 §3 的 44 行改动面漏列「删 config `farmland` 组」这一动作面**

- **结论**：删 `FarmstormConfig.java:226` 的字段与 `:904-908` 的整组、删 `farmstorm.configuration.farmland.limitPerPlayer` 的生成段，仍是本版的动作面；它的权威出处是 30 号 §2.3 第 3 行与 §4.1 的「被删的那一组单独登记」表，**不在 44 行里**。44 行不重编号，本文在表 A 末尾以**补记行**登记它，动作面总数按 **47 = 44 + 3** 计（A-42 一行 + A-55 两行）。
- **依据**：`BRIEF` §3.2 第 18 行（「`FarmstormConfig.java:904,907` → 删除整组」）与 28 号 §3.3 第 1 行都要求删；30 号 §2.3 / §4.1 也写了，唯独 §3 的 44 行没有这一行——下游若只读 §3 会整体漏掉删键。
- **落地任务**：**F-192**（表 A 补记行）。
- **回灌目标**：30 号 §3（加一条表下补记或第 45 行）+ 32 号陷阱章。
- **状态**：**待回灌**（本稿新立）。

**A-43 · 「只加载 1 个区块」的口径：tick 面 1 个，加载面 3×3**

- **结论**：D-145 的「那 1 个区块」准确含义是「**只有 1 个区块被 tick**（ticket level 32，方块实体 tick）」，紧邻一圈 8 个区块被 ticket 等级传播加载到 FULL（level 33，加载但不 tick）、再外一圈不再加载。因此「不是 3×3」在 tick 面成立、在**加载面不成立**。文档与验收一律按 tick 面写：**禁止**把「只有 1 个区块被加载」写成断言（那一条必红）。
- **依据**：28 号 D-145「只加载那 1 个区块（不是 3×3）」与 30 号 §2.5.2「加载面的诚实描述」（`distance = 1` ⇒ 中心 32、邻居 33）；两处字面冲突，且 30 号 §8 ③ 把「实际加载面是否就是 3×3」列为待验证——口径必须先定死，否则验收无法写。
- **落地任务**：**F-198**（实现与三层截图）、**F-209**（#36 的断言按 tick 面改写）。
- **回灌目标**：28 号 D-145 补充 + 29 号 §11（边界清单）+ 30 号 §7 #36。
- **状态**：**待回灌**（本稿新立）。

**A-44 · （已作废）老档补建的遍历顺序曾判为取 `orphans` 的记录次序，由 A-47 取代**

- **原结论（已不成立）**：补建的遍历顺序取 `orphans` 迁移时冻结的记录次序；A-39 的三级键只管候选核之间，不用于遍历顺序。
- **作废理由**：0.1.0 老档**没有 `orphans` 键**——`orphans` 是 30 号新加的落盘字段。老档补建时「候选耕地」只能从 `FarmlandLedger.byOwner` 派生，而 **`byOwner` 是 `HashMap`**（A-39 已确认），派生出来的次序跨 owner 不稳定 ⇒ 直接撞上 30 号 §7 #19「同一份存档两次启动结果相同」。原判把「迁移时冻结顺序」当成可复现的来源，但冻结动作本身就跑在 `HashMap` 的迭代上，等于没有冻结点。
- **状态**：**已作废，由 A-47 取代**（A-44 的「取 `orphans` 记录次序」被推翻；**A-47 自身又经历 rev.2**——排序键从「维度 + 坐标」降为 **`BlockPos` 单级全序**，因为耕地这一侧落盘只有 `pos`、没有维度）。A-44 与 A-36 的顺序部分都保留为历史，**不删行**（删了会让后来者照着旧指令实施）。

**A-45 · 踩踏面的判据是「有绑定即受保护」，不是「实时判己方」**

- **结论**：`protectsFromTrample` 按「该格有绑定 ⇒ 受保护」判；**额度**与**浮窗**两面仍按实时「己方」判。换队之后：额度与浮窗立刻按新队重算，**踩踏面保持受保护**（绑定未变）。29 号 B-18 的「换队后可能立刻失去保护」收窄为「额度与浮窗两面立即变化，踩踏面不变」。
- **依据**：28 号 D-138 要求三面「共用这一套判定」；29 号 B-18 据此写「换队后可能立刻失去保护」；而 30 号 §9 第 8 条指出 `protectsFromTrample(level, pos)` 没有查询者参数，改成「有绑定即受保护」并在附录 A #30 升为技术裁定。**为什么不能取 D-138 的字面读法**：踩踏的动作方可能是一只没有队伍的 `Monster`，按「己方」实时判会把生物判成非己方、于是生物能踩坏己方田，与 0.1.0 的 D-120 第 3 项（玩家与生物都要拦）直接冲突；绑定只在己方锄地时产生，所以「有绑定」就是己方关系曾成立的存储证据。
- **落地任务**：**F-195**（实现）、**F-210**（B-18 的收窄版读数）。
- **回灌目标**：28 号 D-138 补充（加一句踩踏面的例外）+ 29 号 §3.10 与 B-18 + 30 号 §9 第 8 条（从「登记」升为裁决）。
- **状态**：**待回灌**（本稿新立）。

**A-46 · 30 号 §7 的 38 条验收不覆盖 29 号 §11 的 39 条边界**

- **结论**：38 条验收与 39 条边界**不是一一对应**：13 条完全对上、5 条只有部分断言、**21 条没有任何对应断言**（B-01、B-02、B-03、B-05、B-08、B-10、B-11、B-13、B-14、B-15、B-17、B-18、B-19、B-20、B-21、B-24、B-25、B-26、B-31、B-32、B-35）。边界清单以 29 号 §11 为准，由 **F-210 / F-211** 逐条验收；**不要求** 30 号 §7 增补到一一对应（那会让 #1 ~ #38 整体重编号，并让 #38 对 A-40 的引用断链）。32 号陷阱章要写明「边界 ≠ 验收条目」。
- **依据**：逐条比对 29 号 §11 与 30 号 §7 的表（本文 §6 表 G 第 17 行给出分类计数）；另有 11 条验收没有对应边界（#1、#2、#3、#5、#6、#11、#13、#14、#16、#37、#38），它们属工程面（计数 / 产物 / 工具 / 红线），设计使然。
- **落地任务**：**F-210**（B-01 ~ B-20）、**F-211**（B-21 ~ B-39）、**F-209**（#37 / #38 的收口）。
- **回灌目标**：30 号 §7 表头（加一句「本表不覆盖 29 号 §11 的边界，后者由 31 号 F-210 / F-211 承担」）+ 32 号陷阱章。
- **状态**：**待回灌**（本稿新立）。

**A-47 · 老档补建一律先按 `BlockPos` 单级全序排序再逐条处理（取代 A-44；本文按 rev.2 定稿）**

- **结论（rev.2）**：老档补建（含 0.1.0 老档的首次补建）一律先把候选耕地按 `BlockPos#compareTo`（`Vec3i` 的既有实现，比较序 y → z → x）做单级全序升序排序，再逐条处理；`orphans` 的持久化顺序**不参与判定**。可复现性判据两条：① 同一份存档连续两次启动，N 与 M 完全相同；② 把 `orphans` 段里两条记录在 `.dat` 里对调后重跑，绑定的坐标集合与 N/M 与对调前**完全相同**。
- **为什么排序键是 `BlockPos` 单级、而不是「维度 + 坐标」**（rev.2 的修正）：`FarmlandLedger` 的 NBT 里耕地只有 `pos`（一个 `long`）、**没有维度字段**（30 号 §2.2.2 第 2 点：v1 与 v2 一样，耕地坐标不带维度）；而 `OutpostCoreIndex.Entry` 才带 `dimension`。**核心有维度、耕地没有**——两者键不一致是数据决定的，给耕地硬造一个维度键会与落盘格式冲突，也会重新引入「同一 `(x,y,z)` 在两个维度」的撞键问题（30 号 §9 第 5 条已登记为本版不修的已知限制）。`BlockPos` 单级全序对耕地这一侧是完备的：同一份账本里不会出现两个相同 `pos`。
- **依据**：0.1.0 老档没有 `orphans` 键（它是 30 号新增的落盘字段），候选只能从 `byOwner`（`HashMap`，A-39 已确认跨 owner 顺序不稳定）派生；把「迁移时冻结」当顺序来源仍然跑在 `HashMap` 的迭代上，撞 30 号 §7 #19 的可复现性要求。
- **落地任务**：**F-190**（补建按 `BlockPos` 全序排序）、**F-188**（v2 不给 `orphans` 的顺序附加语义）、**F-208**（#19 的 N/M + 对调取证）。
- **回灌目标**：28 号 D-137 补充（把「按 `byOwner` 记录次序」改成「按 `BlockPos` 全序」）+ 29 号 §3.6 读法 2（方向正确，键从「维度 path + 坐标」收成 `BlockPos` 单级）+ 30 号 §2.2.8（删掉 `orphans` 顺序段）+ A-36 的记录加一句「顺序部分被 A-47 取代」。
- **状态**：**待回灌**（本稿改判；rev.2 的排序键已与作者口径对齐）。

**A-48 · 语言键算式结构上漏了 2 条 `KeyMapping` 描述键（321 → 323）**

- **结论**：28 号 §3.4 的算式 `315 − 2 + 1 + 3 + 1 + k` **结构上少一项 `+2`**（两个 `key.farmstorm.*` 描述键）；完整算式是 `315 − 2 + 10 = 323`，其中 10 = `bulletSpeed` 1 + CLIENT config 3 + `KeyMapping` 描述键 2 + 浮窗「自己」1 + 指令反馈 3。本版语言键总数一律写 **323**（中英各一份），任何得 321 的算式都不再成立。
- **依据**：30 号 §4.3 逐条点名 10 条新增（#1 ~ #10）与 2 条删除；`BRIEF` §3.5 已补算式；30 号 §9 第 1 条把这条登记为「算式漏 2 条」。
- **落地任务**：**F-206**（总账 323 × 2）、**F-202**（那 2 条 `key.farmstorm.*`）、**F-182 / F-201 / F-203 / F-205**（其余 8 条）。
- **回灌目标**：28 号 §3.4（算式补 `+2`）+ 32 号（禁用 321 的写法）。
- **状态**：**已入裁决台账**（A-48）；28 号 §3.4 待回灌。

**A-49 · 耕地跨维度同坐标撞车：本版不修，列已知限制**

- **结论**：`FarmlandLedger` 没有维度字段（0.1.0 起如此），于是下界 `(0, 64, 0)` 的耕地会「借」到主世界同坐标那份绑定的保护，额度也会把两个维度的同坐标算成一块。**本版不修**：plan 未要求；老档 NBT 只有 `pos`、无法回溯维度；耕地侧本版已经叠了一次存档升版（v1 → v2），再改维度键等于同一份档连升两次。
- **依据**：30 号 §9 第 5 条（已登记为「已知限制，本版不修」）+ 30 号 §2.2.2 第 2 点（耕地坐标沿用 v1、不带维度）+ A-47 rev.2（排序键因此只能是 `BlockPos` 单级，取不到维度）。
- **落地任务**：**不产生实现任务**；由 **F-188**（落盘口径不动，`pos` 仍是唯一耕地键）、**F-210 / F-211**（作为已知限制记录，**不写成通过 / 失败项**）、**F-212**（汇总里列为已知限制）承接。
- **回灌目标**：30 号 §9 已知限制 + 32 号陷阱章。
- **状态**：**已入裁决台账**（A-49）；30 / 31 / 32 待回灌。

### 5.2 其余 7 条（A-50 ~ A-56）

这 7 条不改本文的任何口径，只登记「谁改了哪份上游 / 哪处本文自处理」，以便台账与 31 号对账。

| 编号 | 结论（一句话） | 落点：任务或文档 | 回灌状态 |
|---|---|---|---|
| **A-50** | 28 号 D-131 的投掷物**基类路径写错**：正确的是 `entity/ThrownCropEntity.java`（该类在 `entity/` 顶层，不在 `entity/thrown/` 下，与 30 号 §1.2 的「`entity` 顶层 2 个 = `ThrownCropEntity` + `TurretBullet`」一致） | 28 号 D-131 | **已回灌 28 号**；本文 F-185 与表 A 第 18 行引用的路径本来就是 `entity/ThrownCropEntity.java`，无需改 |
| **A-51** | `一键构建.cmd` 里的 jar 名在 **`:11-12`**、不是 `:10-11`（`:10` 是 `if not exist "dist" mkdir "dist"`） | 28 号 D-147 + 事实卡 §1.10 | **已回灌 28 号**；本文 F-176 与表 A 第 2 行已按 `:11-12` 写 |
| **A-52** | 30 号 §7 #2 的 toml 段计数判据 `^\[` 数不到**嵌套段**（`[a.b]` 会被 `\[` 后面的第一个字符吃掉），须改 `^\s*\[` | 30 号 §7 #2 | **已回灌 30 号**；本文 F-192 验收第 1 条已按 `^\s*\[` 写 |
| **A-53** | `OutpostCoreIndex.Entry` 加 `gameTime` 是本版的**第二处存档格式变更**，要进 28 号 §3.2（原来只登记了 `FarmlandLedger` 升 v2 一处） | 28 号 §3.2 | **已回灌 28 号**；本文表 B 的 B.2 行 2 与 F-189 已按「第二处 schema」写 |
| **A-54** | 本文依赖列与验收归属四处不自洽（F-208 依赖错配、三处缺前置、21 vs 18、表 A 行 23/24 归属错位） | **31 号（本文自处理）** | **本文自处理**：依赖列按「承接项 → 产出任务」机械重算、§1 与 §2 逐字一致、21 条边界分配 14 + 7、表 A 行 23/24 按实际归属改写、F-185 标题统一为长版 |
| **A-55** | 30 号 §3 的改动面缺 `FarmlandLedger.java:128-158`（记账 `add` / `remove`）与 `:203-219`（惰性校验 `validated`）两段 | 30 号 §3 | **已回灌 30 号**；本文由 **F-188**（两段都在它「涉及文件」里）与 **F-191**（capacity / used 消费 `validated` 的丢弃结果）覆盖，表 A 的补记段另有一行说明 |
| **A-56** | **F-192 必须依赖 F-191（编译顺序，非风格顺序）**：`farmland.limitPerPlayer` 的唯一读者是 `FarmlandLedger.base()`（`FarmlandLedger.java:72-74`），而删 `base()` 是 F-191 | **31 号（本文自处理）** | **本文自处理**：§1 与 §2 的依赖均为 `F-191`，并在 F-192 的实现要点里写明这条是编译顺序 |

> 编号说明：A-50 ~ A-53、A-55 记在 28 / 30 号侧，A-54 / A-56 是 31 号的修正单；7 条都**不改变**本文 A-42 ~ A-49 的任何结论。

> 回灌纪律（照 28 号 §0.3 第 4 条）：A-42 ~ A-49 只改**对应的那几处**，不改任何决策的结论句；与本文冲突时以回灌后的上游正文为准。**A-44 与 A-36 的顺序部分保留为历史**（A-44 已作废、由 A-47 取代；A-47 自身 rev.2 把排序键收成 `BlockPos` 单级），不删行——台账里删行会让后来者照着旧指令实施。

---

## 6. 七张覆盖对照表（本文件最重要的部分）

表 A ~ 表 F 是六张覆盖表，表 G 是上游一致性核对（第七张，专门抓三份上游之间互相矛盾 / 单向漏项的地方）。每张表的末行给「覆盖数 / 总数」。

### 表 A · 改动面覆盖（30 号 §3 的 44 行 → 任务）

| §3 行 | 路径或类#方法 | 增 / 改 / 删 | 负责任务 |
|---|---|---|---|
| 1 | `gradle.properties:11` | 改 | F-176 |
| 2 | `检查环境.cmd:14`、`一键构建.cmd:11-12`、两支 `.cmd` 的 banner 行 | 改 | F-176（产物面复核 F-207） |
| 3 | `assets/farmstorm/textures/item/wild_apple.png` | 改 | F-179 |
| 4 | `tools/gen_textures.py` 叠「+」机制（`:274-275`、`:407`、`:546-560`、`:612-623`、`:974-975`） | 改 | F-178 |
| 5 | `tools/gen_textures.py:154`、`:849-881`、`:884-895` | 改 | F-179 |
| 6 | `tools/gen_textures.py:982-983`、`:1151-1154` | 改 | F-179 |
| 7 | `registry/FarmstormBlocks.java:79` | 改 | F-180 |
| 8 | `registry/FarmstormBlocks.java:82-89` | 改 | F-181 |
| 9 | `config/FarmstormConfig.java:425-427` | 改 | F-182 |
| 10 | `config/FarmstormConfig.java:431-433` | 改 | F-182 |
| 11 | `config/FarmstormConfig.java`（`pumpkin_turret` 组内、`:433` 之后） | 增 | F-182 |
| 12 | `entity/TurretBullet.java:28` | 删 | F-183 |
| 13 | `entity/TurretBullet.java:90-96` | 改 | F-184 |
| 14 | `entity/TurretBullet.java:98-119` | 改 | F-184 |
| 15 | `behavior/impl/PumpkinTurret.java:75` | 改 | F-183 |
| 16 | `entity/thrown/FriendChivesProjectile.java` | 改 | F-185（场本体）、F-186（读档分支） |
| 17 | `entity/thrown/FriendChivesProjectile.java:56-80` | 改 | F-185 |
| 18 | `entity/ThrownCropEntity.java:95-118` | 核（不改） | F-185（核对） |
| 19 | `farmland/FarmlandLedger.java:51` | 改 | F-188 |
| 20 | `farmland/FarmlandLedger.java:57-58, 72-79, 82-98` | 删 | F-191 |
| 21 | `farmland/FarmlandLedger.java:101-121` | 改 | F-191 |
| 22 | `farmland/FarmlandLedger.java:228-264` | 改 | F-188 |
| 23 | `farmland/FarmlandLedger.java`（新方法） | 增 | F-188（`bind` / `unbind` / `migrate` / `bindingsOf` / `orphanCount` / `rebuildByCore`）、F-190（`backfill`） |
| 24 | `farmland/FarmlandGuard.java:69-75` | 改 | F-193（选核 + 单核空位的判定）；**F-194 的写入点不在此类**，在 `event/FarmstormGameEvents.java:206`（30 号 §3 未单列该行，属第 24 行的决策落地） |
| 25 | `farmland/FarmlandGuard.java:92-107` | 改 | F-195 |
| 26 | `farmland/FarmlandGuard.java:120-134` | 改 | F-195 |
| 27 | `blockentity/OutpostCoreBlockEntity.java:315-335` | 改 | F-196 |
| 28 | `blockentity/OutpostCoreBlockEntity.java:353-379` | 改 | F-196 |
| 29 | `blockentity/OutpostCoreBlockEntity.java:95-103` | 改 | F-198（ticket 段）、F-200（广播段） |
| 30 | `blockentity/OutpostCoreBlockEntity.java:166-170` | 改 | F-198（ticket 段）、F-200（广播段） |
| 31 | `team/TeamResolver.java`、`team/ScoreboardTeamResolver.java` | 增 | F-187 |
| 32 | `outpost/OutpostCoreIndex.java` | 增 / 改 | F-189（`gameTime` / 落盘 / 访问器）、F-200（消费） |
| 33 | `command/FarmstormCommands.java` | 删 | F-197 |
| 34 | `event/FarmstormGameEvents.java:4`、`:62`、`:71-73` | 删 | F-197 |
| 35 | `event/FarmstormGameEvents.java`（+6 个监听） | 增 | F-200（4 个包推送监听）、F-198（`ServerStoppingEvent`） |
| 36 | `network/CoreListPayload.java` 等三件 | 增 | F-199（payload / handler）、F-200（broadcaster） |
| 37 | `config/FarmstormClientConfig.java` + `Farmstorm.java:56` 后一行 | 增 | F-201 |
| 38 | `client/` 5 个新类 | 增 | F-202、F-203、F-204、F-205（`ClientCoreList` 由 F-199） |
| 39 | `outpost/CoreChunkTickets.java` | 增 | F-198 |
| 40 | `datagen/ModLangProvider.java:393-394`、`:404-406`、新增 10 条、类注释 | 改 | F-206（总账 / 注释）+ F-182 / F-192 / F-197 / F-201 / F-202 / F-203 / F-205（各自的键） |
| 41 | `Farmstorm.java:124-146` 注册面自检 | 核（不改） | F-212 |
| 42 | `entity/ThrownCropEntity.java`（`keepAliveAfterImpact` 默认） | 核（不改） | F-185 |
| 43 | `tools/verify_project.py:222-237` | 改 | F-177 |
| 44 | `tools/verify_project.py` 其余三段 | 核（不改） | F-212 |
| **补记 1** | `config/FarmstormConfig.java:226`、`:904-908`、`ModLangProvider.java:393-394`（A-42，未进 30 号的 44 行） | 删 | **F-192** |
| **补记 2** | `farmland/FarmlandLedger.java:128-158`（记账 `add` / `remove`）与 `:203-219`（惰性校验 `validated`）——30 号 §3 的 44 行未单列这两段（A-55） | 改 | **F-188**（两段的重写与 `byCore` 双剪）、**F-191**（`capacity` / `used` 消费校验结果） |

**44/44 覆盖**（+ 补记 2 行，A-42 / A-55；动作面总数 47 = 44 + 3，未覆盖 0）。

### 表 B · 注册对象与 schema 变更（+0 注册对象，2 处 schema）

**B.1 注册面 12 类逐类 +0**（基线 93，30 号 §1.2；本版一个注册项都不加，D-146 / §1.3）

| registry | 基线 | 本版增量 | 核对任务 |
|---|---|---|---|
| `Registries.ITEM` | 32 | **+0** | F-212（#1 的十二类读数） |
| `Registries.BLOCK` | 26 | **+0** | 同上 |
| `Registries.BLOCK_ENTITY_TYPE` | 7 | **+0** | 同上 |
| `Registries.ENTITY_TYPE` | 13 | **+0** | 同上 |
| `Registries.MOB_EFFECT` | 4 | **+0** | 同上 |
| `Registries.SOUND_EVENT` | 3 | **+0** | 同上 |
| `Registries.RECIPE_TYPE` | 1 | **+0** | 同上 |
| `Registries.RECIPE_SERIALIZER` | 1 | **+0** | 同上 |
| `Registries.CREATIVE_MODE_TAB` | 1 | **+0** | 同上 |
| `Registries.DAMAGE_TYPE` | 3 | **+0** | 同上 |
| `Registries.DATA_COMPONENT_TYPE` | 1 | **+0** | 同上 |
| `Registries.MENU` | 1 | **+0** | 同上 |
| **合计** | **93** | **+0** | 32+26+7+13+4+3+1+1+1+3+1+1 = **93** |

**B.2 两处 schema 变更**（各自独立提交、独立回滚，不合并）

| # | 存档 | 变更 | 兼容读法 | 任务 |
|---|---|---|---|---|
| 1 | `world/data/farmstorm_farmland.dat` | 升 **v2**：`dataVersion` + `bindings`（`pos` / `owner` / `coreDimension` / `corePos`）+ `orphans`（`pos` / `owner`）；**不再写 `owners`** | 缺 `dataVersion` 视为 v1，`owners` 全部读成 `orphans`（不猜绑定） | **F-188** |
| 2 | `world/data/farmstorm_outpost_core.dat` | `Entry` 增 `long gameTime` 并落盘（A-39 的比较键载体） | 缺该字段取 `Long.MIN_VALUE`，启动打计数 | **F-189** |

**B.3 六个「加了但不进注册面」的构件**（这是 +0 的全部理由）

| # | 构件 | 为什么不进 registry | 任务 |
|---|---|---|---|
| 1 | 1 个自定义网络包（`farmstorm:core_list`） | payload 走 `RegisterPayloadHandlersEvent`，`Type` 只是 `ResourceLocation` 记录 | F-199 |
| 2 | 2 个 `KeyMapping` | 按键进客户端 `Options#keyMappings` 数组 | F-202 |
| 3 | CLIENT config 3 键 | `ModConfigSpec` 是配置文件模型 | F-201 |
| 4 | 客户端指令 `/farmstorm hud` | Brigadier 指令树是运行期注册（`RegisterClientCommandsEvent`） | F-205 |
| 5 | 耕地绑定表 | `SavedData` 落盘 | F-188 |
| 6 | 区块 ticket | `TicketType.create(...)` 是普通对象，1.21.1 没有 ticket 注册表 | F-198 |

### 表 C · config 覆盖（server 166 键中「改 2 增 1 删 1」+ client 3 键）

| # | 键 | 类型 | 默认 | 改 / 增 / 删 | 落键任务 | 读点任务 |
|---|---|---|---|---|---|---|
| 1 | `pumpkin_turret.fireIntervalTicks` | int | 60 → **40** | 改 | F-182 | F-184（间隔由 `TurretBehavior` 既有读取点消费，不动） |
| 2 | `pumpkin_turret.projectileDamage` | int | 1 → **3** | 改 | F-182 | F-184（结算体搬家，读取点不动） |
| 3 | `pumpkin_turret.bulletSpeed` | double | **0.45**（值域 0.0 ~ 4.0） | **增** | F-182 | F-183（两处读点） |
| 4 | `farmland.limitPerPlayer` | int | — | **删**（整组消失） | F-192（补记行，A-42） | 无读者（`base()` 已在 F-191 删除） |
| 5 | `hud.enabled`（client） | bool | **true** | 增 | F-201 | F-202（按键）、F-203（渲染）、F-205（指令） |
| 6 | `hud.x`（client） | int | **4**（0 ~ 3840） | 增 | F-201 | F-203（钳制）、F-204（拖拽）、F-205（reset） |
| 7 | `hud.y`（client） | int | **4**（0 ~ 2160） | 增 | F-201 | 同上 |
| **合计** | server **166** + client **3** | | | 改 2 / 增 1 / 删 1 + client 3 | | |

算式与核对：

1. **server**：`166 − 1 + 1 = 166`（删 `farmland.limitPerPlayer`、增 `pumpkin_turret.bulletSpeed`），总数持平但**组成变了**。
2. **组数**：`59 − 1 = 58`（`farmland` 整组消失，新键落在既有的 `pumpkin_turret` 组里）。
3. **client**：`+3`，**不计入** 166，单独一份 `farmstorm-client.toml`；与 server 的 166 相加是 169，正好等于语言键里 `farmstorm.configuration` 的 169 条（30 号 §4.3 的强校验）。
4. 新键的三条属性（类型 / 默认 / 值域）逐项照 30 号 §4.1 的「新增键的三个属性」表，本文不复算。

### 表 D · 资源覆盖（新增 0 / 就地替换 1 张 PNG + 生成器三处 + 2 份 lang）

| # | 文件 | 新增 / 替换 | 处理 | 任务 |
|---|---|---|---|---|
| 1 | `assets/farmstorm/textures/item/wild_apple.png` | **就地替换 1 张** | 16×16 / 571 B → **64×64 / 6,946 B**，源图逐字节副本 | F-179 |
| 2 | `tools/gen_textures.py`（`NO_PLUS_ICONS` / `add_plus` / `build_marker` / `compose_icon` / 合成分支 / 调用点 / dry-run 文案） | 生成器**第 1 处** | 叠「+」机制整体删除 | F-178 |
| 3 | `tools/gen_textures.py:154`、`:849-881`、`:884-895` | 生成器**第 2 处** | `wild_apple` 直通 + 默认源图改指 64×64 | F-179 |
| 4 | `tools/gen_textures.py:982-983`、`:1151-1154` | 生成器**第 3 处** | `expected_size()` 与尺寸断言一起放开（A-32） | F-179 |
| 5 | `assets/farmstorm/lang/zh_cn.json`、`en_us.json` | 就地替换 **2 份**（文件数不变） | 由 datagen 重生成，**315 → 323** 键 | F-206（总账）+ 各自的任务 |
| **合计** | 新增 **0**、就地替换 **1 张 PNG + 2 份 lang**；生成器改动 **3 处** | | | **未覆盖 0** |

不新增的文件（必须写明，否则会有人去补）：**没有**新 PNG、**没有**新 JSON（模型 / 方块状态 / 战利品 / 配方 / 进度全 0）、`sounds.json` 不动（30 号 §5 定为「不补 `attenuation_distance`」）、ogg 不动。

### 表 E · 风险覆盖（RT-31 ~ RT-41，每条至少一个任务负责验证）

| 编号 | 风险 | 负责验证的任务 | 该任务怎么验 |
|---|---|---|---|
| **RT-31** | 首次引入自定义网络包：codec / 注册 / 断线 / 跨维度 / 推送时机任一处漏掉，浮窗显示错数据 | F-199、F-200、F-203 | F-199 验「恰 1 个 payload 文件 + 0 个 `playToServer` + 专用服务器能起服」；F-200 验 10 个时机与去抖（同一刻连锁事件只 1 包）、换服不残留；F-203 验 9 行 / 93 px 的显示 |
| **RT-32** | 绑定表重构 + 存档升版：老档读到空账本、补建铲老田、迁移算错 | F-188、F-190、F-196 | F-188 用 fixture 与旧档各启动一次、v2 与 0.1.0 双向读；F-190 验「同一份存档两次启动 N/M 相同」且 `setBlock` 0 命中；F-196 验三支分支与「无掉落」 |
| **RT-33** | `outpost_core.farmlandBonus` 一键两义（容量 + 额度基数） | F-182、F-191、F-193 | F-182 把双义写进注释（`git diff` 可见）；F-191 验「改键后额度 = 16 × 核数」；F-193 验「键改 2 后第 3 格被拒且额度同步变 2」 |
| **RT-34** | 朋友的韭每刻扫球的开销 | F-185 | 同时存在 10 个场时服务端 tick 时间不劣化到可见卡顿；`effectTicks` 改 1200 后场按时长自毁、不累积 |
| **RT-35** | 落地实体存活且可见：重力 / 碰撞 / 跨卸载泄漏 | F-185、F-186 | F-185 验落点在水流里 30 秒不动、第 `effectTicks` 刻消失；F-186 验卸载回来实体不存在、不复活 |
| **RT-36** | 亮度 15：强光传播 15 格、该位置永不刷怪 | F-181 | F3 的 `Light` = 15；15 格内光斑、16 格外无光；光斑正中 100 秒 0 只怪、外侧 ≥ 1 只 |
| **RT-37** | 64×64 贴图：显示面积仍 16×16，重跑生成器会回滚 | F-179 | 验 PNG 是 64×64 / 6,946 B / sha256 相同；删图重跑仍 64×64 且不报断言；物品栏不破图 |
| **RT-38** | man瓜 两个默认值对已落盘 toml 无效 | F-182 | 两条对照：删旧 toml → 40 / 3 / 0.45；保留旧 toml → 60 / 1 / 0.45（`bulletSpeed` 自动补） |
| **RT-39** | 区块 ticket 泄漏 | F-198 | 拆核后走远 200 格区块可卸载；停服再起服票重建；释放次数 = 申请次数；`chunks.dat` 不含核坐标 |
| **RT-40** | 删指令树的连带（`baseOverride` / 2 条语言键 / 注释 / 文档） | F-197、F-192、F-206 | F-197 验「服务端 `/farmstorm` 未知指令、`FarmstormCommands` 0 命中」；F-192 验 `[farmland]` 段消失；F-206 验 `farmlandlimit` 在两份 lang 里 0 命中、323 键对平 |
| **RT-41** | 破红线后既有文档仍写「0 网络包」 | F-207、F-212 | F-207 的 `jar tf` 与 F-212 的红线扫描：`@Mixin` 0 命中、`CustomPacketPayload` 恰 1 文件；文档面由 32 号按 D-146 重写（不在本文件范围） |
| **合计** | **11 条** | | **11/11，未覆盖 0** |

### 表 F · 待验证 API 覆盖（30 号 §8 的 8 条 → 由哪个任务在哪一步确认 + 退路）

**这张表是本版「实现到一半才发现接口不存在」的唯一预防**：每条都要落到**具体任务的具体步骤**，退路要具体到能直接执行。

| # | 待验证的 API / 语义 | 由哪个任务在哪一步确认 | 确认不了时的退路 |
|---|---|---|---|
| **①** | `PayloadRegistrar#playToClient` / `StreamCodec` / `PacketDistributor#sendToPlayer` 的**运行时**行为：握手版本串 `"1"`、单机内存连接是否仍序列化、专用服务器是否会加载客户端 handler 类 | **F-199 第一步**：先只发 `total=0` 的空包，单机与专用服务器各起一次，看 handler 是否被调用（日志一行） | 若单机不序列化导致 handler 不触发 ⇒ 改成客户端在 `ClientPlayerNetworkEvent.LoggingIn` 后**主动请求一次**。这会变成 **2 个包**，必须回 28 号登记（D-146：不设上限，但每个包都要登记），不许自行加 |
| **②** | `RegisterClientCommandsEvent` 的注册形态：客户端派发器与服务器下发的指令树如何合并、`sendSuccess(..., false)` 在客户端指令里的回显位置 | **F-205 第一步**：先注册 `/farmstorm` 根 + `hud` 一个字面量，看单人补全里是否出现 `hud` | 若客户端指令被服务器树覆盖 ⇒ 只保留「按键 + CLIENT config」两条载体（D-144 的第 1、3 项已足够，指令只是便利），F-205 缩成一条「按键等价物」 |
| **③** | `TicketType.create(String, Comparator)` 自建类型是否被 `DistanceManager` 接受；`removeRegionTicket` 是否要求四参完全一致；`distance = 1` 的实际加载面（A-43） | **F-198 第一步**：放 1 个核后看 F3 的三层区块状态 + 拆核后走远 200 格看是否卸载 | 自建类型被拒 ⇒ 退回 `TicketType.PORTAL`（`BlockPos` value、`timeout = 300`）并在每次 `serverTick` 重续——**这会改变释放语义**，必须回 28 号登记；`distance = 1` 不 tick 方块实体 ⇒ 改 `distance = 2`（加载面 5×5，A-43 的表述同步改） |
| **④** | `sounds.json` 的 `attenuation_distance` 与 `playSound` 的 volume 是否叠加（30 号 §5 已定**相乘**） | **F-185 的音效验收**：站在落点 32 格与 33 格各听一次（`#31`） | 若 32 格听不见 ⇒ 调 `friend_chives.soundRange`（config 键，默认 32.0、值域 0 ~ 64），**不改 `sounds.json`**（改它会破坏「0 新增 JSON」，且会把可闻半径变成 64） |
| **⑤** | `KeyMapping` 与 `RegisterKeyMappingsEvent` 的注册时机（`Options` 加载后、按键界面打开前）；默认键 **H / J** 是否撞键；Screen 打开时 `consumeClick()` 是否仍计数 | **F-202 第一步**：打开按键界面确认两条键位可见、连按 H/J 各触发一次 | 注册太晚（界面里看不到）⇒ 把注册挪到 `FMLClientSetupEvent#enqueueWork`；H/J 撞键 ⇒ 只改默认键码，不动语言键与设计 |
| **⑥** | 原版自定义包的**单包大小上限**具体数值（本文只用到 3.1 KB 量级） | **F-203 的 9 行满员场景**（`#10`）：64 条候选时包仍正常到达 | 若上限低于预期（不可能低于 3 KB）⇒ 把 `MAX_CORES` 从 **64** 降到 **32**（`total` 仍能算对 `+N`，客户端只画 9 行） |
| **⑦** | 拖拽 `Screen` 的鼠标坐标口径：`mouseClicked` / `mouseDragged` 的 `(mx,my)` 是否已是 **GUI 缩放后**的坐标 | **F-204 第一步**：把面板拖到已知的 `(100, 100)`，比对 `hud.x` / `hud.y` 是否为 100 | 若不同坐标系 ⇒ 在 `mouseClicked` 里先做 `mx * guiWidth / windowWidth` 换算（系数取 `Minecraft#getWindow` 的宽高） |
| **⑧** | `ClientPlayerNetworkEvent.LoggingOut` 在「单人退出到主菜单」与「被踢出」两种路径下是否都触发 | **F-200 的换服测试**（`#29`）：从服务器 X 退出进服务器 Y，看浮窗是否立即消失 | 若不触发 ⇒ 在 `ClientTickEvent.Post` 里判 `Minecraft#level == null` 时清空快照（多一次每刻判空，成本可忽略） |
| **合计** | **8 条（①~⑧）** | **8/8 落到具体任务与具体步骤** | **8/8 给出可执行退路** |

### 表 G · 上游一致性核对（28 / 29 / 30 三份之间，以及事实卡与 30 号 §3 之间）

统计：**17 行**——**真分歧 6 条**（G-1、G-9、G-10、G-11、G-12、G-17；**6 条全部已立裁决**：G-1 → A-48、G-9 → A-47、G-10 → A-45、G-11 → A-43、G-12 → A-42、G-17 → A-46）、**已裁决一致 6 条**（G-2 ~ G-7）、**记法差异 2 条**（G-8、G-13）、**单向漏项 3 条**（G-14 ~ G-16）。

| # | 分歧点 | 28 号说 | 29 号说 | 30 号说 | 以谁为准 | 是否已立裁决 |
|---|---|---|---|---|---|---|
| G-1 | **语言键总数**（321 vs 323） | §3.4 算式 `315 − 2 + 1 + 3 + 1 + k`，`k` = 指令反馈条数（候选 3），最终值「由 30 号写死」⇒ 取 k = 3 得 **321** | 只说浮窗「自己」标识用一条键、维度 path 不新增 | §4.3 逐条点名 10 条新增，`315 − 2 + 10 = **323**`；多出的 2 条是 `KeyMapping` 的描述键 | **323**（30 号 §4.3 的逐条清单可逐个核对；28 号的算式漏了 2 条按键描述键） | **已立 A-48**（本文原写「已结、不立 A 号」，2026-11-02 复核改判；台账已录） |
| G-2 | **8 行 vs 9 行** | D-143：最多 **8 行核心**，超出另加 1 行 `+N` ⇒ 最高 **9 行** | §5.5：核心行 8，`+N` 不占 8 行 ⇒ 最高 9 行 | §2.4.4：最大行数 **9** | **8 + 1 = 9** | **已裁决 A-34** |
| G-3 | **83 px vs 93 px** | D-143 第 4 项未给高度 | §5.6：满 9 行 **93** px（`9×9 + 8×1 + 2×2`），1 行 13 px | §2.4.4：8 行 **83** px、9 行 **93** px | **8 行 83 / 9 行 93**（同一算式 `10n + 3` 的两个取值） | **已裁决 A-34**（93 是 9 行的值） |
| G-4 | **行高分解**（9+1+2 vs `10n+3`） | — | §5.6：行高 9 / 行距 1 / 内边距 2 | §2.4.4：面板高 `10n + 3` | **取 29 号 §5.6 的三个数**；`10n + 3` 是等价推论（`9n + (n−1) + 4`） | **已裁决 A-41** |
| G-5 | **五类 vs 三类声音** | D-130 补充原文写「破坏 / 放置 / 踩踏」三类 | §6.3 按五类写（加挖掘 / 落地） | §3 第 7 行、§7 #24–#28 按五类 | **五类** | **已裁决 A-31** |
| G-6 | **`effectTicks` vs 200** | D-131（回灌后）场长与施加基准都取 `friend_chives.effectTicks` | §8.2 同一口径 | §2.7.2 代码里只留 `fieldLength` 一个来源 | **`effectTicks`**，200 只是默认值 | **已裁决 A-33** |
| G-7 | **两级 vs 三级比较键** | D-135 补充：`placedGameTime` → 维度 id → 坐标字典序（三级） | §3.3 / §5.4 三级 | §2.2.7 / §2.1.5 三级（`BlockPos#compareTo` 是 y → z → x） | **三级** | **已裁决 A-39** |
| G-8 | **166 vs 165** | §3.4：`166 − 1 + 1 = 166`，组 59 → 58 | §9 第 46 行：166 / 58 组 | §4.1：166 / 58 组；§4.3 的注里出现「`farmstorm.configuration` 从 166 → 165」 | **166 / 58**；165 是「删键后、加 `bulletSpeed` 前」的中间量，不是总数 | 记法差异，不立裁决 |
| G-9 | **老档补建的遍历顺序** | D-137（A-36 回灌后）：按 `FarmlandLedger.byOwner` 的记录次序（`LinkedHashSet`） | §3.6 读法 2：**先按（维度 path, x, y, z）排序**再逐条补建，不得依赖 `byOwner` 迭代 | §2.2.8：按 `orphans` 的记录次序（= v1 `byOwner` 的记录次序，迁移时冻结） | **取 `BlockPos#compareTo` 单级全序（y → z → x）排序后遍历（A-47 rev.2）**——耕地落盘只有 `pos`、没有维度，三维键在耕地侧不可得；两侧「按记录次序」的口径都作废；A-39 的三级键只用于候选核比较 | **A-44（作废）→ A-47（rev.2）** |
| G-10 | **踩踏面的判据** | D-138：耕地保护 / 额度 / 浮窗**共用同一套**己方判定（实时、离线有效） | B-18 / §3.10：换队后保护按己方实时重算 ⇒ 可能**立即失去保护** | §9 第 8 条 + 附录 A #30：踩踏面「**有绑定即受保护**」（没有查询者参数，生物无队伍） | **踩踏面「有绑定即受保护」**；额度与浮窗两面仍实时判己方；B-18 收窄 | **本文新立 A-45** |
| G-11 | **强制加载「1 个区块」** | D-145：只加载那 **1 个区块**（**不是** 3×3） | §3.10 未展开（只引 D-145） | §2.5.2：`distance = 1` ⇒ 中心 tick、紧邻一圈加载到 FULL ⇒ **加载面是 3×3** | **tick 面 1 个、加载面 3×3**；文档与验收按 tick 面写，禁止写「只有 1 个区块被加载」 | **本文新立 A-43** |
| G-12 | **44 行 vs 事实卡 27 项** | §3.3 第 1 行要求删 `farmland.limitPerPlayer` | —（29 号不列改动面） | §3 的 44 行**没有**这一行（只在 §2.3 第 3 行与 §4.1 删表里有）；事实卡 §3.2 第 18 行有 | **必须删**；44 行不重编号，本文以补记行登记，动作面 47 = 44 + 3 | **本文新立 A-42** |
| G-13 | **尺寸断言的引述行号**（四种写法） | §3.2 第 14 行写 `:1152-1154` | — | §2.6 写 `:1153-1154`、§3 第 6 行与 §5 写 `:1151-1154`、§7 #23 写 `:1153` | **实测 `:982-983` + `:1151-1154`**（`image = compose` 在 1151、断言在 1153）；**验收不写行号作判据**（行号会随改动漂移，与 A-37 同精神） | 记法差异，不立裁决 |
| G-14 | **config 注释写死「60 刻 = 3 秒」** | D-127 只给默认值 | — | §3 第 10 行只改默认值，未点注释 | 改默认值**必须同改注释**（纪律同 30 号 §9 第 7 条） | 单向漏项，并入 **F-182**，不立裁决 |
| G-15 | **两支 `.cmd` 的 banner `v0.0.1`** | D-147 只点 jar 名（`:14` / `:11-12`） | — | §3 第 2 行同样只点 jar 名 | banner 一并改 `v0.1.1`（D-124：版本号只有一处来源） | 单向漏项，并入 **F-176**，不立裁决 |
| G-16 | **`+N` 的颜色** | — | §5.5：灰色 `0xFFAAAAAA` | §2.4.4 只列自己 / 队友 / 背景 / 边框，未列 `+N` | **取 29 号 `0xFFAAAAAA`**，30 号补列一行 | 单向漏项，不立裁决（不影响验收判据） |
| G-17 | **38 条验收 vs 39 条边界** | — | §11：**39 条**（B-01 ~ B-39，**本版不增 B-40**，见 A-49） | §7：**38 条**（#1 ~ #38） | **不一一对应**：13 条完全对上、5 条部分覆盖、**21 条无对应断言**——`B-01`、`B-02`、`B-03`、`B-05`、`B-08`、`B-10`、`B-11`、`B-13`、`B-14`、`B-15`、`B-17`、`B-18`、`B-19`、`B-20`、`B-21`、`B-24`、`B-25`、`B-26`、`B-31`、`B-32`、`B-35`（**21 个编号逐条点名**）；另有 11 条验收无对应边界（#1、#2、#3、#5、#6、#11、#13、#14、#16、#37、#38，工程面）。边界以 29 号 §11 为准：**F-210 逐条验 B-01 ~ B-20（20 条）**、**F-211 逐条验 B-21 ~ B-39（19 条）** | **本文新立 A-46**（已加严：21 条编号逐条点名 + 每条落到一条验收，见 F-210 / F-211） |
| **合计** | **17 行** | | | | 真分歧 **6**（全部已裁） / 已裁决 **6** / 记法差异 **2** / 单向漏项 **3** | 表 G 派生 **8 条**（A-42 ~ A-49；其中 A-44 作废）；另有 A-50 ~ A-56 七条见 §5.2，不来自表 G |

表 G 的读法三条：

1. **G-2 ~ G-7 是「同一件事的两种写法」**：已被 A-31 / A-33 / A-34 / A-39 / A-41 裁过，本文只做收录，不改任何一条的结论；G-1 由 **A-48** 收口（总数 323），G-8 属中间量的记法差（165 是「删键后、加 `bulletSpeed` 前」的中间量，不是总数）。
2. **G-9 ~ G-12 是真分歧**：三份上游对同一件事给出**不同的行为结果**（遍历顺序决定老档里哪些田能绑上；踩踏面决定换队后田还防不防踩；加载面决定 F3 上看到几个区块；动作面决定删不删一个 config 组）。四条**都已裁决**：G-9 → **A-47（rev.2，`BlockPos` 单级；A-44 作废）**、G-10 → A-45、G-11 → A-43、G-12 → A-42。
3. **G-13 ~ G-16 是单向漏项**：只有一方写了、另一方漏了，处理办法是「取写了的那一方 + 在另一边补一句」，不改变任何决策。G-17 是覆盖面的分歧，处理办法是「边界由专门的验收任务承担，不要求 30 号 §7 一一对应」。

---

## 7. 覆盖自检（交稿前逐条过）

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 任务编号连续、无跳号 | `grep -c "^#### F-"` = **37**；`grep -oE "F-2[0-9]{2}" \| sort -u` = 37 个；区间 `F-176 ~ F-212` ⇒ `212 − 176 + 1 = 37` | **通过** |
| 2 | Phase 编号连续 | 26 / 27 / 28 / 29 / 30 / 31，6 个，与任务区间一一覆盖 | **通过** |
| 3 | 里程碑编号连续 | M22 ~ M27，6 个，与 Phase 一一对应 | **通过** |
| 4 | 裁决编号 | A-30 ~ A-41 原样收录 **12** 条（与 `.work/v011-verdicts.md` 逐条对齐）；新立 A-42 ~ A-49 **8** 条（A-44 作废）；另收 A-50 ~ A-56 **7** 条；合计 **27** 条（A-30 ~ A-56），连续无跳号 | **通过** |
| 5 | 表 A 覆盖 | 44 行一行不漏 + 补记 2 行（A-42 / A-55）；未覆盖 **0** | **44/44（+3）** |
| 6 | 表 B 覆盖 | 12 类注册面逐类 +0（合计 93）；**2** 处 schema 各自独立成任务；**6** 个不进注册面的构件各有任务 | **20 项** |
| 7 | 表 C 覆盖 | server 改 2 / 增 1 / 删 1 + client 3；算式 `166 − 1 + 1 = 166`、`59 − 1 = 58`、`+3` | **7 项** |
| 8 | 表 D 覆盖 | 新增 0、就地替换 1 张 PNG + 2 份 lang、生成器 3 处；`sounds.json` 与 ogg 不动 | **5 项** |
| 9 | 表 E 覆盖 | RT-31 ~ RT-41 共 **11** 条，每条至少一个任务负责验证，且给了「怎么验」 | **11/11** |
| 10 | 表 F 覆盖 | 30 号 §8 的 **8** 条，每条落到**具体任务的具体步骤** + **具体退路**，无「实现时注意」 | **8/8** |
| 11 | 表 G | **17** 行；真分歧 6 条（全部已裁）、已裁决 6、记法差异 2、单向漏项 3；G-17 的 **21** 条边界编号已逐条点名 | **17 行** |
| 12 | 每个任务可追溯 | 37 个任务全部能追到「决策号 / 30 号 §3 的某一行 / 某条验收 / 某条风险」之一；无凭空任务 | **通过** |
| 13 | 每个任务 ≥ 1 条可观察验收 | 逐条读过：37 个任务全部有 ≥ 1 条落在 §0.3 七类位置的断言；无「表现正常」「手感良好」 | **通过** |
| 14 | A-37 纪律 | 全文不出现「调用点数 = N」类判据；涉及写入口的表述一律换成「`grep -c` 的命中数」或「存档里的条数」 | **通过** |
| 15 | A-40 时序 | F-177 早于所有引用工具结论的任务；F-177 ~ F-191 一律不引用 `FAIL 0`；F-192 为收口点 | **通过** |
| 16 | 时点性事实未作断言 | 全文无「改前有 N 个调用点」这类会过期的判据；凡引用改前状态只用于「`git diff` 的范围」与「对照实验的两侧」 | **通过** |
| 17 | 工时自洽 | `5 × 0.5 + 22 × 1.5 + 10 × 3.0 = 65.5`；按 Phase 相加 = 65.5；`65.5 × 1.25 = 81.875`；S+M+L = 37 | **通过** |
| 18 | 依赖无环、编号递增、两处逐字一致（A-54）；§5 覆盖完整 | 每个任务的「依赖」编号都小于自身编号；按 Phase 排序即拓扑序，无回边；**§1 表的依赖列与 §2 详情的「依赖」行逐字相同**（37 / 37，脚本比对 0 处不一致）；**§5 覆盖 A-30 ~ A-56 共 27 条，无跳号**（12 + 8 + 7） | **通过** |
| 19 | 禁用词与语气 | 逐条通读：未出现未定事项标记、待办占位标记、条件含糊的结论；全文无第二人称敬语，一律用「作者」「实现者」 | **通过** |
| 20 | 只新建一个文件 | `git status` 只多出 `docs/zh/31-TASKS-0.1.1.md`；28 / 29 / 30、事实卡、裁决台账、`src/` 一行未改 | **通过** |
| 21 | A-47 的排序键口径（rev.2） | 全文补建遍历只写 `BlockPos#compareTo` 单级全序（y → z → x），**没有**「维度 + 坐标」的遍历键；排序用词只剩 A-39 一处禁令（`grep` 命中 **1**） | **通过** |
| 22 | A-46 的加严 | 表 G-17 逐条点名 21 个边界编号；F-210 / F-211 各 20 / 19 条验收与「B-01 ~ B-20」「B-21 ~ B-39」一一对应；**无 §7 对应条目的边界数 = F-210 的 14 + F-211 的 7 = 21**（与表 G-17 的 21 个编号逐一对上）；纯观感类均写明「人工目视 / 听觉 + 截图 / 录屏存 `.work/v011-verify/`」 | **通过** |
| 23 | A-54 的四处修正 | ① F-208 / F-209 / F-210 / F-211 / F-207 的依赖列按「承接项 → 产出任务」重算，§1 与 §2 逐字一致；② 21 条边界在 F-210 / F-211 里分配为 14 + 7；③ 表 A 行 23 的 `orphanCount` 归 F-188、行 24 只留 F-193 的判定（F-194 写入点在事件类）；④ F-185 的标题两处统一为长版 | **通过** |

---

## 附录 A　任务号 ↔ 决策号 ↔ 验收号 三向索引

「30 号 §7」列给该任务直接负责的验收编号；「29 号 §11」列给该任务直接负责的边界编号；`—` 表示该任务不直接对应（它的验证来自别处，如 `git diff` 范围或计数）。

| 任务 | 决策号 | 30 号 §7 | 29 号 §11 | 风险 |
|---|---|---|---|---|
| F-176 | D-124、D-147 | #2（产物名）、#3 的载体 | — | RT-38 |
| F-177 | A-40（30 号 §3 第 43 行） | #38（收口点） | — | RT-40 |
| F-178 | D-125 | —（机制层，验收写在任务里） | B-38 | RT-37 |
| F-179 | D-126、A-32 | #23 | B-38、B-39 | RT-37 |
| F-180 | D-130、A-31 | #24、#25、#26、#27、#28 | B-36 | — |
| F-181 | D-141 | #33 | B-37 | RT-36 |
| F-182 | D-127、D-128、D-129 | #2、#3、#4 | B-28 | RT-33、RT-38 |
| F-183 | D-129、A-30 | #34 | — | — |
| F-184 | D-129 | #35 | B-26、B-27 | — |
| F-185 | D-131、D-132、D-133、A-33 | #30、#31 | B-29、B-30、B-31、B-32、B-34、B-35 | RT-34、RT-35 |
| F-186 | D-133 | #32 | B-33 | RT-35 |
| F-187 | D-138 | — | — | — |
| F-188 | D-134、D-137、A-47 | #16 | — | RT-32 |
| F-189 | A-39、D-142、D-138 | #9（比较键面） | B-12 | RT-31 |
| F-190 | D-137、A-47 | #19 | B-23、B-24、B-25 | RT-32 |
| F-191 | D-139、D-138 | #17 | — | RT-33 |
| F-192 | D-139、A-42 | #2、#5 | — | RT-40 |
| F-193 | D-135、A-39 | #17、#18 | B-15、B-16、B-17 | RT-33 |
| F-194 | D-135 | #16、#18 | — | — |
| F-195 | D-138、A-45 | — | B-15、B-18 | — |
| F-196 | D-136、A-35 | #20、#21、#22 | B-19、B-20、B-21、B-22 | RT-32 |
| F-197 | D-140 | #12 | — | RT-40 |
| F-198 | D-145、A-38、A-43 | #36 | — | RT-39 |
| F-199 | D-142 | #6 | B-14 | RT-31 |
| F-200 | D-142、A-39 | #7、#8、#9、#29 | B-04、B-05、B-12 | RT-31 |
| F-201 | D-144 | #11（开关载体）、#13 | — | — |
| F-202 | D-144 | #11 | — | — |
| F-203 | D-143、A-34、A-41 | #7、#8、#10 | B-01、B-02、B-03、B-06、B-10、B-11、B-13 | RT-31 |
| F-204 | D-144 | #14、#15 | B-07、B-08、B-09 | — |
| F-205 | D-144 | #12、#13 | — | — |
| F-206 | 30 号 §4.3、D-129 / D-144 的连带 | #5 | — | RT-40 |
| F-207 | D-124、D-147、A-38 | #36 | — | RT-41 |
| F-208 | 30 号 §7 #1 – #19 | #1 – #19 | — | — |
| F-209 | 30 号 §7 #20 – #38、A-40 | #20 – #38 | — | — |
| F-210 | 29 号 §11 B-01 – B-20、A-46 | — | B-01 – B-20（**20 条逐条对应**） | — |
| F-211 | 29 号 §11 B-21 – B-39、A-46 | — | B-21 – B-39（**19 条逐条对应**） | — |
| F-212 | 全部 D-124 ~ D-147、A-42 ~ A-49 | #37、#38 | 全部 **39** 条（已知限制 A-49 不设通过项） | 全部 11 条 |

三条对账说明：

1. **决策号覆盖**：D-124 ~ D-147 共 24 条，逐条至少出现在一个任务的「决策号」列里；D-129 出现 4 次（F-182 / F-183 / F-184 / F-206），因为它同时管数值、常量删除、收敛与语言键四处。
2. **验收号覆盖**：30 号 §7 的 #1 ~ #38 由 F-208（#1–#19）与 F-209（#20–#38）两批全包；本表只标「任务直接负责」的那几条，避免把 37 行写成 38 行。
3. **边界号覆盖**：29 号 §11 的 B-01 ~ B-39 由 F-210（B-01–B-20）与 F-211（B-21–B-39）两批全包；其中 **36 条**已在实现任务的验收里被单点断言（本表逐条标出），剩下 3 条（B-23、B-24、B-25）只由 F-211 覆盖——它们都依赖「老档 + 容量硬顶」的构造，不适合挂在某个实现任务的验收里。