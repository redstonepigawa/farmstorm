# 33 号 v0.1.1 分阶段任务清单（Phase Prompts）

> 投喂方式：**先投 32 号主提示词一次**，再按 Phase 顺序逐节投喂本文件；同一时间只投一个 Phase。
> 每个 Phase 从头到尾独立可执行——把一节单独投给代理，不需要它看过上一节。

| 项 | 值 |
|---|---|
| 文档编号 | 33-PROMPT-TASKS |
| 版本 | v0.1.1（MC 1.21.1 / NeoForge 21.1.213 / Java 21） |
| 读者 | 投喂编码代理的人 + 被投喂的 CLI 编码代理 |
| 上游 | `31-TASKS-0.1.1.md`（**F-176 ~ F-212 的唯一来源**：编号、标题、Phase 归属、复杂度、人日、依赖）、`30-TECH-0.1.1.md`（§7 的 38 条验收、§8 的 8 条待验证 API、RT-31 ~ RT-41）、`28-DECISIONS-0.1.1.md`（D-124 ~ D-147、§4 红线、§8 裁决台账） |
| 同级 | `32-PROMPT-MAIN-0.1.1.md`（常驻主提示词；本文件**不重复**它的硬约束与完成定义） |
| 内容 | **6 个 Phase / 37 个任务（F-176 ~ F-212）**；每个 Phase 固定六小节：前置条件 / 本节目标 / 任务清单 / 出口判据 / 翻车点 / 回报格式 |
| 边界 | 只读上游：不改 `docs/zh/00` ~ `32`、不改 `docs/en/`、不改 `src/`、不改 `.work/` 的三份台账 |

---

## 0. 投喂顺序与约束

### 0.1 六节投喂顺序（Phase 26 → 31）与每节的前置

| 顺序 | 投哪一节 | Phase · 里程碑 | 任务 | 人日 | 本节前置（缺一条都不许开） |
|---|---|---|---|---|---|
| 1 | §1 | 26 · **M22** | F-176 ~ F-177 | 2.0 | 工作树干净；`.tools\gw.cmd build` 退出码 0；`run/config/farmstorm-server.toml` 是 0.1.0 的产物（59 组、含 `[farmland]`），**本节不许删它** |
| 2 | §2 | 27 · **M23** | F-178 ~ F-184 | 11.5 | §1 的出口判据全过；源图 `wild_apple_64x64.png`（6,946 B）在位；旧 `farmstorm-server.toml` 另存一份副本，用于 F-182 的「保留旧 toml」对照 |
| 3 | §3 | 28 · **M24** | F-185 ~ F-186 | 4.5 | §1 的出口判据全过即可；本节与 §2 / §4 **互不依赖**，可与 §2 或 §4 交替投喂，但仍然一次只投一节 |
| 4 | §4 | 29 · **M25** | F-187 ~ F-198 | 21.5 | §1 的出口判据全过；一份 0.1.0 老档（只有 `owners`、无 `dataVersion`）作为 F-188 / F-190 的 fixture；NBT 查看器在手 |
| 5 | §5 | 30 · **M26** | F-199 ~ F-205 | 15.0 | §4 的 F-187、F-188、F-189 三个任务已提交（`isAlly(Owner, Owner)` 重载、`FarmlandLedger` v2、`Entry.gameTime` 与三个访问器）；全仓 `CustomPacketPayload` 此刻仍是 0 命中 |
| 6 | §6 | 31 · **M27** | F-206 ~ F-212 | 11.0 | §2 / §4 / §5 全部出口判据通过；**A-40 窗口已关闭**（F-192 已落地且删旧 toml 重新生成过）；`.work/v011-verify/` 可写 |
| 7 | §7 | 收尾（不在 Phase 内） | — | — | §6 的出口判据全过，77 条判据与 37 个任务全部有结论 |

读表三条：

1. **本节顺序是投喂顺序，不是唯一可执行顺序。** §2 / §3 / §4 三节互不依赖，都只等 §1；并行投喂时按「一次只投一节、投哪节就只做那节的 F-xx」执行，不要跨节混做。
2. **§5 不能提到 §4 之前**：浮窗的排序键（`gameTime`）与「己方」判定（`isAlly` 重载）分别在 F-189 与 F-187 里，§4 没做完就做 §5，浮窗只有空列表。
3. **§4 内部有两处必须串行**：`F-191 → F-192 → F-197` 是一条删除链（`base()` 先无读者、config 组才可删、指令树才可整棵删；**A-56**：F-192 的声明依赖是 **F-191**——`base()` 是 `farmland.limitPerPlayer` 的唯一读者，属**编译顺序**，不是风格顺序）；`F-193 → F-194 → F-196` 是另一条（选核 → 写绑定 → 迁移都读同一个 `Decision.core`）。两链之间可并行。

### 0.2 每节开头必须重申的三条（不依赖上下文）

把下面三行**原样**贴在每一次投喂的开头（紧跟 32 号主提示词之后、本节正文之前）。这三行是本节唯一被允许「不解释就直接执行」的口径，节内不再重复：

```text
① 红线只剩一条：无 Mixin（@Mixin / mixins.json / @Inject / @Redirect / @Accessor 全仓 0 命中）。
   自定义网络包的红线已整体废除（D-146）：本版有且只有 1 个 S2C 只读包 farmstorm:core_list，客户端不回包。
   「0 个自定义网络包」是 0.1.0 的旧口径，不要照做，也不要写成「本版恰好允许 1 个」。
② 三个数不许动：注册对象 93（12 类逐类 +0）；server config 166 键 + client 3 键（单独一份 farmstorm-client.toml）；
   语言键 323 × 2。本节结束时这三个数可能还没到位，按本节 §x.4 给出的区间读数验收，不许自行改成目标值。
③ tools/verify_project.py 在 F-192 收口之前不能当通过判据（A-40）：窗口期它的 [FAIL] 是预期，
   只允许出现两条键级差异（F-182 之前：缺 pumpkin_turret.bulletSpeed、多 farmland.limitPerPlayer）
   或一条（F-182 之后到 F-191：只多 farmland.limitPerPlayer）。多一个或少一个键名，都说明解析器写错了。
```

### 0.3 速查表（一屏：Phase ↔ 里程碑 ↔ 任务号 ↔ 人日 ↔ 出口判据一句话）

| Phase | 里程碑 | 任务号 | 个数 | 人日 | 出口判据（一句话） |
|---|---|---|---|---|---|
| 26 基线与工具链 | M22 | F-176 ~ F-177 | 2 | 2.0 | 产物名与 banner 都是 0.1.1；工具按 30 号 §4.1 现算出 **58 组 / 166 键**并扣掉 1 个删键，中间态**恰好两条**键级差异，退出码非 0 属预期 |
| 27 材质、数值与方块属性 | M23 | F-178 ~ F-184 | 7 | 11.5 | `wild_apple.png` **64×64 / 6,946 B** 且删图重跑不回落、不报尺寸断言；其余 **24** 张 sha256 不变；压缩干草块**五类**声音是石头；核心 `Light = 15`；`[pumpkin_turret]` 5 键为 **40 / 3 / 0.45 / 15 / 1**；1 格内 5 发 5 中掉 15 点 |
| 28 朋友的韭持续场 | M24 | F-185 ~ F-186 | 2 | 4.5 | 落地后是 `effectTicks` 刻的场（第 1 刻 200、第 199 刻 1、第 **200** 刻消失，改 100 就止于 100）；空投不播音、命中只播 **1** 次；卸载回来实体不存在、不复活 |
| 29 耕地-核心绑定制 | M25 | F-187 ~ F-198 | 12 | 21.5 | 锄地绑最近的己方核、第 **17** 格被拒；核主下线田仍踩不坏；拆核两级（改绑 / 变泥土）；补建「绑定 N 块 / 保持未绑定 M 块」两次启动**相同**；`/farmstorm` 消失；`[farmland]` 段消失、段行 **58**、工具**首次 `FAIL 0`**；核无人时仍 tick 且 `chunks.dat` 不含核坐标 |
| 30 浮窗与数据面 | M26 | F-199 ~ F-205 | 7 | 15.0 | 浮窗 1 行 `#1 [overworld] (x, y, z) 自己`；9 核 → **8 + `+1` = 9 行 / 93 px**（8 核 83 px）；H 开关持久化、J 拖拽 `ESC` 保存、`/farmstorm hud on\|off\|reset` 三动作写盘；换服不残留；全仓 `CustomPacketPayload` **恰 1 文件**、客户端不回包 |
| 31 收口与全量验收 | M27 | F-206 ~ F-212 | 7 | 11.0 | 两份 lang 各 **323** 键且键集相同（`farmstorm.configuration.` **169**）；`dist/farmstorm-0.1.1+mc1.21.1.jar` 可发布、纯服务端可起；**38 + 39 = 77** 条全部有读数、未通过 **0**；红线只剩「无 Mixin」 |
| **合计** | **M22 ~ M27** | **F-176 ~ F-212** | **37** | **65.5** | 裸人日 65.5（`5 × 0.5 + 22 × 1.5 + 10 × 3.0`），含 25 % 缓冲 **81.875**；关键路径 **15.5 人日 / 7 个任务**（浮窗线） |

---

## 1. Phase 26 · 基线与工具链（F-176 ~ F-177）

> 投喂方式：32 号主提示词全文 + §0.2 的三行 + 本节。

### 1.1 前置条件

- 工作树干净：`git status` 只应出现本节要改的文件；`.tools\gw.cmd build` 当前退出码 0。构建入口只认 `.tools\gw.cmd build`，不要直接调 gradle；`dist/` 不属于本节。
- 手边这四份：`30` 号 §4.1 的 58 组 / 166 键表与 §2.3 的删键登记；`28` 号 D-124（版本号单一来源）、D-139（删 `farmland.limitPerPlayer`）、D-147（升级须知）；`31` 号 §0.4（A-40 的三条排期规则）；`31` 号表 A 的第 1 / 2 / 43 / 44 行。
- `run/config/farmstorm-server.toml` 是 **0.1.0 的产物**（59 组、含 `[farmland]`、`pumpkin_turret` 只有 3 键）。本节**不许删它、不许手改它**：删了就没有「缺键」那条差异，A-40 的中间态判据会退化成一条。
- 工具的现状已知：`tools/verify_project.py` 的 config 期望集由**四份旧文档**（`01-DESIGN` / `12-TECH-0.0.2` / `18-TECH-0.0.3` / `24-TECH-0.1.0`）现算（`:222-237` 的四个函数 + `:244` 的并集）。本节之前它对 v0.1.1 是**红**的，但红法还不是「两条键」——那是本节 F-177 的交付物。
- 本阶段不产出任何行为：它只决定「后面的每一次验收有没有一把可信的尺子」。除 `gradle.properties` 一行、四支 `.cmd` 的七处旧版本串、`tools/verify_project.py` 的解析段之外，不许动任何文件。

### 1.2 本节目标

先把两件「取数」的事钉死：工具的 config 期望来源（F-177）与版本号（F-176）。**本节先做 F-177**：31 号 §0.4 的「排在 Phase 26 第二位」是按任务号说的（F-176 在前），与执行顺序不冲突——F-176 只改版本串，不构成任何任务的前置；F-177 必须早于任何会引用工具结论的任务。两者可互换提交顺序，但 **F-177 必须在 §2 及以后任何一节开工之前落地**。

本节结束时能做的事：用一条命令拿到本版的 config 期望集（**58 组 / 166 键**，已扣掉 `farmland.limitPerPlayer`），并且知道它此刻为什么是红的（只有两条键级差异，且两条的键名都知道）；构建产物名、脚本里写死的 jar 名、脚本 banner、`neoforge.mods.toml` 的版本行，四处都是 **0.1.1**。

### 1.3 任务清单（F-176 ~ F-177）

| 任务号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|
| F-176 | 版本号 0.1.1 与两支构建脚本的 jar 名 / banner | S / 0.5 | — | `gradle.properties:11` 的 `mod_version` 改 `0.1.1`；`检查环境.cmd` / `一键构建.cmd` 的 jar 名（含 `-sources.jar`）与 banner 的 `v0.1.1` 同步；产物变 `build/libs/farmstorm-0.1.1+mc1.21.1.jar` |
| F-177 | `verify_project.py` 的 config 期望来源改指 v0.1.1 | M / 1.5 | — | 工具新增 `expected_config_keys_v011(...)`（解析 30 号 §4.1 的 58 组 / 166 键）并入并集，同时在并集**旁边**统一扣掉 `REMOVED_KEYS = {"farmland.limitPerPlayer"}`；解析失败报文件名 + 表行号，不静默跳过 |

阶段小计：**2 个任务、2.0 人日**（S 1 / M 1 / L 0）。

### 1.4 出口判据（可观察）

- [ ] **产物名与版本行（F-176）**：怎么验：`.tools\gw.cmd build` 后 `jar tf` 读 `META-INF/neoforge.mods.toml`。通过的样子：退出码 0；`build/libs/farmstorm-0.1.1+mc1.21.1.jar` 与 `farmstorm-0.1.1+mc1.21.1-sources.jar` 同时在位；`build/libs` 里没有 `farmstorm-0.1.0*` 与 `farmstorm-0.0.1*` 的残留；`version` 行是 `0.1.1`。
- [ ] **七处旧版本串一次改齐（F-176）**：怎么验：`grep -c` 三个模式 + 读两支脚本头部。通过的样子：`farmstorm-0.0.1` 在四支脚本里都是 **0**；`farmstorm-0.1.1+mc1.21.1` 在 `检查环境.cmd` 为 **1**、在 `一键构建.cmd` 为 **2**；四支脚本 banner 的版本串同为 `v0.1.1`；`git diff gradle.properties` 只有第 11 行一行变化。
- [ ] **期望集的两个数（F-177）**：怎么验：`python -X utf8 tools/verify_project.py` 的 §1 段。通过的样子：打印「30-TECH §4.1：**58** 组 / **166** 键」与「剔除已删键 1 个：farmland.limitPerPlayer」。
- [ ] **中间态只允许两条（A-40，本节最硬的一条）**：怎么验：同一次运行里数 §1 段的 `[FAIL]` / `[STALE]` 行与里面的键名。通过的样子：合计**恰好 2 条**，键名只有 `pumpkin_turret.bulletSpeed`（缺）与 `farmland.limitPerPlayer`（多）；**退出码非 0 属预期**，本节不许为了让它归零去动 `run/config` 或 `docs/`。出现第三个键名即判 F-177 失败（那是解析器写错的唯一征兆）。
- [ ] **失败路径可用（F-177）**：怎么验：把 §4.1 的「组」列临时改一行（本地实验、不入库）后运行。通过的样子：退出码非 0，报错含文件名与表行号；恢复后退出码回到 0。
- [ ] **只读上游（F-177）**：怎么验：`git diff --stat docs/`。通过的样子：为空；`grep -c "expected_config_keys_v011" tools/verify_project.py` ≥ **2**（定义 + 调用）。
- [ ] **本节没碰的两处（对照面）**：怎么验：读 `run/config/farmstorm-server.toml` 与两份 lang。通过的样子：`[farmland]` 段仍在、段行 **59**、键 **166**、`pumpkin_turret` 仍 **3** 键；两份 lang 各 **315** 键（语言键的增删从 §2 的 F-182 才开始）。

### 1.5 翻车点

1. **只加来源、不剔键**（F-177 第 3 条、A-40）：`expected_config_keys_v011` 写对了，但 `farmland.limitPerPlayer` 没从并集里扣。翻成什么样：窗口前段看不出问题（那时旧 toml 里这个键还在，两边都有），到 F-192 删掉 `[farmland]` 段之后才暴露成一条**永久的**「缺 `farmland.limitPerPlayer`」——F-192 的 `FAIL 0` 永远不成立，而现场看到的是「工具说少一个我已经删掉的键」。提前发现：`REMOVED_KEYS` 只允许出现在并集表达式**旁边**一次，不许散到两处；F-177 的验收里必须有「剔除已删键 1 个」这句打印。
2. **写一份硬编码的兜底键清单**：解析失败时退回「11 个键写死」。翻成什么样：文档改了工具不报错，静默盲区原地复活——这正是 A-40 要消灭的那类退化。提前发现：工具里只允许一段 config 逻辑；解析失败必须报文件 + 表行号。
3. **为了让工具变绿去改冻结区或删 toml**（A-40 的三条排期规则）：改 `30` 号 §4.1 让期望集对齐现状，或直接删 `run/config/farmstorm-server.toml`。翻成什么样：前者破坏「工具只读上游」（`git diff --stat docs/` 非空），后者把「缺键」那条差异一起抹掉（2 条变 1 条），F-182 的旧 toml 对照也失去基线。提前发现：本节结束前 `[farmland]` 段仍在、`pumpkin_turret` 仍 3 键、`docs/` 的 diff 为空。
4. **按行号取表，不按「组 + 键」两列解析**：30 号 §4.1 的行里带「（**改** 1 → 3）」这类注解。翻成什么样：`bulletSpeed` 被解析成 `0.45` 或带括号的串，自校验的 58 / 166 两个数对不上。提前发现：自校验不等就 fail 并打印第一处不合规的表行号。
5. **banner 只改 jar 名不改版本串**：`grep "farmstorm-0.0.1"` 是 0，但脚本头部仍写 `Farmstorm v0.0.1`。翻成什么样：脚本提示与产物脱钩，作者跑脚本时看到的版本与 jar 名不一致。提前发现：两支脚本的 banner 各读一次，逐字看 `v0.1.1`。
6. **顺手升构建链**：看到 NeoForge 有新版本就改 `build.gradle`，或把 Java 目标改成别的。翻成什么样：`gradle.properties` 的 diff 不止一行，后面所有 jar 与 `neoforge.mods.toml` 的版本行都不再可比。提前发现：`git diff gradle.properties` 只允许第 11 行；`build.gradle` 与 `settings.gradle` 的 diff 必须为空。
7. **提前做 §2 的活**：顺手把 `wild_apple` 换成 64×64，或把 `pumpkin_turret` 的 `bulletSpeed` 补进 config。翻成什么样：中间态从 2 条变 1 条，F-177 的验收失去判据。提前发现：`git status` 只允许 4 个文件（`gradle.properties`、两支 `.cmd`、`tools/verify_project.py`）。

### 1.6 回报格式

- **改了哪些文件**：`git diff --stat` 的原文；逐个任务给文件路径 + 改动行（F-176 七处旧版本串、F-177 新增函数与 `REMOVED_KEYS` 的位置）。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码与产物路径；`python -X utf8 tools/verify_project.py` 的**完整 §1 段原文**与退出码（含那两条差异的键名）；`jar tf` 里 `neoforge.mods.toml` 的版本行；三个 `grep -c` 的读数；`git diff --stat docs/` 的空输出。
- **观察到什么**：58 / 166 两个数；剔除的键名；`[FAIL]` / `[STALE]` 的行数与键名逐条；`run/config/farmstorm-server.toml` 的段行数、键数、`pumpkin_turret` 的键数；两份 lang 的键数（都应 315）。
- **没做的与做不了的**：一节一行，写明「未验证」与原因（例如专用服务器没起、源图不在手边）。
- **未关闭的问题**：例如工具里是否还有第二处硬编码总数、`§1` 段是否还有与本版无关的旧断言。

---

## 2. Phase 27 · 材质、数值与方块属性（F-178 ~ F-184）

> 投喂方式：32 号主提示词全文 + §0.2 的三行 + 本节。

### 2.1 前置条件

- §1 的出口判据全过，尤其是 F-177（工具已按 58 组 / 166 键现算，中间态恰好两条）。本节**可以**读工具，但**只读那两条差异的收缩过程**，不许把 `FAIL 0` 当判据（A-40）。
- 手边这四份：`30` 号 §7 的 #2 ~ #4、#23 ~ #28、#33 ~ #35；`30` 号 §2.6（贴脸收敛的三分支与 `MIN_STEP`）；`28` 号 D-125 ~ D-130、D-141、D-147，以及 A-30 / A-31 / A-32；`31` 号表 A 的第 3 ~ 15 行、表 C 的第 1 ~ 3 行、表 E 的 RT-37 / RT-38。
- 源图 `wild_apple_64x64.png` 在位，**6,946 B**；它是 `wild_apple.png` 的唯一来源，本版不重生成其余 24 张（`25 − 1 = 24`）。
- `run/config/farmstorm-server.toml` 的 0.1.0 副本另存一份：F-182 的「保留旧 toml 直接升级」要它。
- 冻结面已确认：其余 24 张 PNG 的字节、`FarmstormEntityTypes` 的 `clientTrackingRange(4)` / `updateInterval(10)`、`farmlandBonus` 之外的 config 默认值、`models/item/wild_apple.json`、物品与方块 id——全部 `git diff` 必须为空。
- 本节不产出新行为，只改「看得见」与「摸得着」的七件事：一张图、一个生成器、五类声音、一格光照、三个数值、两处读点、一段收敛。

### 2.2 本节目标

把七件事一次做完，并让它们在**同一次投喂**里互相印证：生成器删掉叠「+」机制（F-178）之后，`wild_apple` 才能安全走 64×64 直通（F-179，两条改动同一文件，先删机制再加分支）；方块属性两处各一行（F-180 / F-181）；man瓜 的三键（F-182）→ 两处读点（F-183）→ 收敛与共用结算体（F-184）是一条三步链，前一步不回滚，后一步不许开工。

本节结束时能做的事：把 jar 装进客户端，狂野苹果是 64×64 的新图且删图重跑生成器不回落；其余 24 张物品图右下角仍有绿十字（本版只重生成 1 张）；压缩干草块破坏 / 放置 / 踩踏 / 挖掘 / 落地听起来都是石头；据点核心一放下就亮到 15；man瓜 两秒一发、每发 3 点、弹速 0.45 可在 toml 里改；目标贴到 1 格内连打 5 发全中。

### 2.3 任务清单（F-178 ~ F-184）

| 任务号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|
| F-178 | 生成器删掉叠「+」机制 | M / 1.5 | — | `tools/gen_textures.py` 的 `NO_PLUS_ICONS` / `add_plus` / `build_marker()` / `compose_icon()`、右下角贴附、合成分支、调用点与 dry-run 文案全删；`ICONS` 的 26 条与 `TEXTURE_SIZE = 16` 不动 |
| F-179 | 狂野苹果直通、尺寸断言放开与图入库 | L / 3.0 | F-178 | `WILD_APPLE_SOURCE` 改指 64×64、`draw_wild_apple()` 直通、`expected_size()` 与 `:1153` 的断言**一起**放开（A-32）；入库 PNG 是源图逐字节副本（64×64 / 6,946 B） |
| F-180 | 压缩干草块音效 `GRASS` → `STONE` | S / 0.5 | — | `registry/FarmstormBlocks.java:79` 一个 `SoundType` 换掉；五类声音一起变；硬度 30 / 抗爆 6 / 工具 / 颜色一字不动 |
| F-181 | 据点核心亮度 15 | S / 0.5 | — | `FarmstormBlocks.java:82-89` 注册链尾部追加 `.lightLevel(state -> 15)`；其余方块属性一字不动，不做成 config |
| F-182 | `pumpkin_turret` 三键 + 1 条语言键 + 2 处注释 | M / 1.5 | — | `fireIntervalTicks` 60 → **40**、`projectileDamage` 1 → **3**、新增 `bulletSpeed`（double，**0.45**，`0.0 ~ 4.0`）；「60 刻 = 3 秒」注释同改；`farmlandBonus` 注释补「一键两义」（RT-33）；语言键 +1 |
| F-183 | 删 `TurretBullet.SPEED` 并改两处读点 | M / 1.5 | F-182 | `TurretBullet.java:28` 常量删除；`steerTowards` 与 `PumpkinTurret:75` 的出膛初速**同一次提交**改读 `pumpkinTurretBulletSpeed`（A-30）；收敛逻辑本任务不动 |
| F-184 | 炮弹近距收敛与共用结算体 | L / 3.0 | F-183 | `steerTowards` 三分支（`dist > step` 原样 / 一步走完剩余 / `≤ MIN_STEP` 直判）；`applyHit` 从 `:98-119` 原样搬家 + `hitResolved` 闸；**不引入软追踪** |

阶段小计：**7 个任务、11.5 人日**（S 2 / M 3 / L 2）。

### 2.4 出口判据（可观察）

- [ ] **一张图（F-179）**：怎么验：读 PNG 头、字节数与 sha256，再删图重跑生成器。通过的样子：`textures/item/wild_apple.png` 是 **64×64**、**6,946 B**、sha256 与源图相同；删掉它再跑 `python -X utf8 tools/gen_textures.py`，产物仍是 64×64、与源图逐字节相同，且**不报「尺寸断言失败」**；连跑两次产物 sha256 相同。
- [ ] **其余 24 张不动（F-178 / F-179）**：怎么验：入库前先存一份 24 张的 sha256 清单，本节结束后逐张比——**以提交字节为准，本节不重跑那 24 张**（重跑不属于发布动作，A-61）。通过的样子：24 张逐张相同；`grep -c "add_plus\|NO_PLUS_ICONS\|build_marker\|compose_icon"` = **0**；`--dry-run` 计划 **26** 项且输出里不再出现「绿色「+」」。
- [ ] **五类声音（F-180）**：怎么验：破坏 / 放置 / 踩踏 / 挖掘 / 落地各一次正负对照（`/playsound` 播 `block.stone.*` 与 `block.grass.*`）。通过的样子：五类都与 `stone` 一致、与 `grass` 明显不同；同一把镐挖它的刻数与改前相同；`git diff registry/FarmstormBlocks.java` 只有 `:79` 一行。
- [ ] **一格光照（F-181）**：怎么验：放下核心读 F3 的 `Light`，夜里看光斑，光斑正中与 15 格外各放一个刷怪平台。通过的样子：`Light = 15`；**15 格内**有光斑、**16 格外**无光（水平与垂直各一次）；100 秒内正中 **0** 只怪、外侧 ≥ 1 只。
- [ ] **三键与两条注释（F-182）**：怎么验：删旧 toml 起服读 `[pumpkin_turret]`；再把旧 toml 放回起服一次。通过的样子：删旧后恰 **5** 键为 **40 / 3 / 0.45 / 15 / 1**；保留旧 toml 时 `fireIntervalTicks` 仍 60、`projectileDamage` 仍 1、**`bulletSpeed` 被自动补成 0.45**；`grep -c "60 刻 = 3 秒"` = **0**、`"40 刻 = 2 秒"` = **1**；`farmlandBonus` 的注释里能读到「额度」二字。
- [ ] **同源可证（F-183）**：怎么验：把 `bulletSpeed` 手改 `0.1` 重启，读炮弹 `Motion`。通过的样子：`grep -rn "TurretBullet.SPEED" src/main/java` **0 命中**；`pumpkinTurretBulletSpeed` 在 `TurretBullet.java` 与 `PumpkinTurret.java` **各 ≥ 1**；`Motion` 三分量模长 ≈ **0.1**（±0.01），出膛那一瞬与逐刻位移一致；`FarmstormEntityTypes` 的 diff 为空。
- [ ] **贴脸不再过冲（F-184）**：怎么验：目标站到 1 格内连打 5 发，再站到 3 格外打一组。通过的样子：5 发全中、目标掉 **15** 点、5 秒内看不到炮弹反向位移；单发只掉 **3** 点、`latest.log` 里同一发只有一条伤害记录；3 格外的弹道形状与改前一致。
- [ ] **A-40 的窗口在这节收缩一次（收口不在本节）**：怎么验：跑一次工具，只数 §1 段的差异行。通过的样子：F-182 提交后差异从 **2 条变 1 条**（只剩「多 `farmland.limitPerPlayer`」），退出码**仍非 0**——这是预期，不许去改 `docs/` 或删 `[farmland]` 段。
- [ ] **语言键的区间读数**：怎么验：数两份 lang。通过的样子：各 **316** 键（`315 + F-182 的 1 条`），中英键集仍相等。

### 2.5 翻车点

1. **只删机制、不删 dry-run 文案**（F-178 第 3 条）：`compose_icon()` 删了，但 `--dry-run` 仍打印「绿色「+」」。翻成什么样：机制已经没了、输出还在描述它，下一个人照着输出以为机制还在。提前发现：`--dry-run` 的输出里搜「+」的整句。
2. **把「重跑」当验收动作**（A-61）：以为「删图重跑一次」能顺带证明其余 24 张也没问题，或者既然机制删了就「一次清干净」。翻成什么样：24 张的绿十字被一起抹掉——这本身是 D-125 的**预期结果**、不是缺陷，但它与「本版只重生成 1 张、25 − 1 = 24」的账直接冲突（28 号 §7 第 1 条），B-38 的验收变成无对象；发布件的判据也因此改成「提交字节不变」。提前发现：24 张的 sha256 清单逐张比，**只比、不重跑**。
3. **尺寸断言只放开一半**（A-32 的原话是「一起放开」）：改了 `expected_size()` 没改 `:1153` 的断言，或反之。翻成什么样：64×64 直通输出直接 `SystemExit`，或断言通过了但期望尺寸来源仍是旧函数。提前发现：删图重跑一次，退出码必须 0。
4. **「直通」写成「再走一遍 16 网格取色」**：产物回 16×16 或颜色被众数化。翻成什么样：PNG 头不是 64×64，字节数不是 6,946；sha256 与源图不同。提前发现：三个数一起读（尺寸 / 字节 / sha256）。
5. **`TurretBullet.SPEED` 只改一处读者**（A-30 的原始发现）：只改 `steerTowards`，忘了 `PumpkinTurret:75` 的出膛初速。翻成什么样：编译失败；若两处都改了但只改一半语义（一处在构造时缓存），出膛与逐刻不同源。提前发现：`grep -rn "TurretBullet.SPEED"` 0 命中 + 两个文件各 ≥ 1 命中 config 键 + 手改 `0.1` 后 `Motion` 模长对得上。
6. **`bulletSpeed` 放到组外或另开一组**：翻成什么样：组数 59 → 60，§4 的 F-192 再也做不到「段行 58」；工具会多报一个组前缀。提前发现：`[pumpkin_turret]` 恰 5 键，且段名没有多出第二个。
7. **值域抄错**：写成 `0.0 ~ 1.0` 或 `0.0 ~ 10.0`。翻成什么样：手改 5.0 后加载值被钳到 1.0 或原样 5.0，与「回到值域内」的断言红；toml 注释行也不是 `0.0 ~ 4.0`。提前发现：读 toml 的注释行 + 手改一次重启。
8. **用「软追踪」解决贴脸过冲**（D-129 第 3 条明令不做）：加转向角上限、最大追击时长、制导参数。翻成什么样：3 格外的弹道形状变了，与「每刻重设方向、速度模长恒定」的既有口径直接冲突。提前发现：`git diff` 只允许三分支收敛 + `applyHit` 搬家 + `hitResolved`，一处制导参数都不许多。
9. **顺手改了炮台的客户端预测参数**：`clientTrackingRange(4)` / `updateInterval(10)`。翻成什么样：远处炮弹的抖动变了，F-184 的「3 格外形状一致」失去对照。提前发现：`FarmstormEntityTypes.java` 的 diff 为空。

### 2.6 回报格式

- **改了哪些文件**：`git diff --stat` 原文；七条各自的文件 + 行号（`FarmstormBlocks.java` 两行、`FarmstormConfig.java` 三键两注释、`TurretBullet.java` 三段、`PumpkinTurret.java` 一行、`gen_textures.py` 三处、`wild_apple.png`）。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`python -X utf8 tools/gen_textures.py`（含 `--dry-run`）两次的退出码与「26 项」；`python -X utf8 tools/verify_project.py` 的差异行数与键名；`jar tf` 里 `wild_apple.png` 的条目；四个 `grep -c`（`add_plus` 组、`60 刻 = 3 秒`、`TurretBullet.SPEED`、`pumpkinTurretBulletSpeed`）。
- **观察到什么**：PNG 的尺寸 / 字节 / sha256 与 24 张的 sha256 清单；五类声音的正负对照各一行；F3 的 `Light` 读数与光斑的三层观察；两张 toml 的 `[pumpkin_turret]` 五键读数（40 / 3 / 0.45 / 15 / 1；旧 toml 为 60 / 1 / 15 / 1，**无 `bulletSpeed`** ⇒ 4 键）；`Motion` 模长；5 发 5 中的血量与单发 3 点；两份 lang 的键数（各 316）。
- **没做的与做不了的**：写「未验证」并给原因（例如没有第二台客户端做声音对照）。
- **未关闭的问题**：例如 64×64 贴图在远距离下的观感、炮弹在区块边界上的收敛行为。

---

## 3. Phase 28 · 朋友的韭持续场（F-185 ~ F-186）

> 投喂方式：32 号主提示词全文 + §0.2 的三行 + 本节。

### 3.1 前置条件

- §1 的出口判据全过即可；本节的 F-185 **无任何前置**，与 §2 / §4 三条线互不依赖。§2 没做也不影响本节。
- 手边这四份：`30` 号 §2.7（场本体、`keepAliveAfterImpact` 的既有钩子、`Settled` 键）、§7 的 #30 ~ #32；`28` 号 D-131 / D-132 / D-133 与 A-33；`31` 号表 A 的第 16 ~ 18、42 行、表 E 的 RT-34 / RT-35；`29` 号 §8.2 的时序表。
- 键的现状：`friend_chives.effectTicks` 默认 **200**、值域 **20 ~ 1200**；`friend_chives.soundRange` 默认 **32.0**（`sounds.json` 本版**不补** `attenuation_distance`，可闻半径由 `max(音量, 1) × 16` 承担）。
- 冻结面：`entity/ThrownCropEntity.java` **只读**（`keepAliveAfterImpact` 的基类默认必须仍是 `false`，其它子类不受影响）；不新增方块实体（那会给 `friend_chives` 加一个 `block_entity_type`，注册面就不再是 +0）；不重写 Q弹 的 `tick`。
- 本节不新增任何 config 键、不新增任何注册对象、不新增任何语言键。

### 3.2 本节目标

把「落地闪一下」换成「落地后 `effectTicks` 刻的场」（F-185），再把它的两个已知崩溃面一次堵死：实体被存进区块 NBT、加载回来既不自毁也不 tick 场（F-186）。两件事必须按顺序做：先有场，才有「场不该跨卸载」。

本节结束时能做的事：把朋友的韭扔出去，落地处半径 6 格的球体里每个生物每刻被刷一次 Q弹，剩余时长逐刻递减，第 `effectTicks` 刻实体消失；把 `effectTicks` 改成 100 再测，递减止于 100；空投（球内 0 生物）不响，命中 ≥ 1 只只响一次；落点在水流里，场的位置 30 秒不动；走出 200 格再回来，场不存在。

### 3.3 任务清单（F-185 ~ F-186）

| 任务号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|
| F-185 | 朋友的韭：从落地一次性改为 `effectTicks` 刻的场 | L / 3.0 | — | `keepAliveAfterImpact()` 返回 `true` + 新增 `tick()`；`onImpact` 只做「读一次 `fieldLength` / 施加第 0 批 / 播一次音」+ 落定三件套（置零速度 / 无重力 / 免碰撞）；`applyField` 的时长一律由 `fieldLength − elapsed` 派生（A-33） |
| F-186 | 区块卸载即结束（读档 `Settled` 首刻自杀） | M / 1.5 | F-185 | `readAdditionalSaveData` 读基类的 `Settled` 置 `loadedSettled`；`tick()` 在 `super.tick()` 之后第一件事判它并 `discard()`；`fieldTick` **不写 NBT** |

阶段小计：**2 个任务、4.5 人日**（S 0 / M 1 / L 1）。

### 3.4 出口判据（可观察）

- [ ] **时序（F-185）**：怎么验：站进球内的生物，逐刻读 Q弹剩余时长。通过的样子：第 1 刻 **200**、第 2 刻 **199**……第 199 刻 **1**；第 **200** 刻 `/data get entity @e[type=farmstorm:friend_chives]` 查不到。
- [ ] **200 不是常量（F-185 / A-33）**：怎么验：把 `effectTicks` 改 **100** 重启重测；再改 **1200** 看它按时长自毁、不累积。通过的样子：递减止于 100、第 **100** 刻消失；1200 那一轮不出现第二个场叠加。
- [ ] **音效只一次（F-185 / D-132）**：怎么验：空投一次、命中 ≥ 1 只一次，各读 `latest.log`。通过的样子：空投 **0** 条 `farmstorm:friend_chives`；命中整场只有 **1** 条，10 秒内不重复。
- [ ] **落定物理（F-185 / RT-35）**：怎么验：把落点选在水流里，再站上去。通过的样子：实体位置 30 秒内不变（±0 格）；踩上去不被推动；没有重力下坠。
- [ ] **卸载即结束（F-186 / RT-35）**：怎么验：场存在时走出 200 格强制卸载再回来；再退出世界重进。通过的样子：回来后**第一个 tick 之后**就查不到该实体；重进世界也不复活。
- [ ] **NBT 面（F-186 / D-133）**：怎么验：用 NBT 查看器读该实体所在区块。通过的样子：实体只有基类的 `Settled` 等键，**没有** `fieldTick` / `FieldTick` 之类自造键。
- [ ] **基类零改动（F-185）**：怎么验：`git diff entity/ThrownCropEntity.java` + 两处 `grep`。通过的样子：diff 为空；`keepAliveAfterImpact` 的基类默认仍是 `false`；`grep -c '"Settled"' entity/thrown/FriendChivesProjectile.java` ≥ **1**。
- [ ] **三个数没动**：怎么验：读注册面与两份 lang、`run/config`。通过的样子：注册对象仍 **93**（`block_entity_type` 仍 **7**）；两份 lang 各 **316** 键；`run/config/farmstorm-server.toml` 组数仍 **59**、键 **166**。

### 3.5 翻车点

1. **场写 NBT 续命**：把 `fieldTick` 存进 `saveAdditional`，读档接着跑。翻成什么样：区块卸载再回来场「复活」，跨卸载泄漏照旧（RT-35 的原始症状），F-186 白做。提前发现：区块 NBT 里搜 `fieldTick` / `FieldTick`，0 命中。
2. **`applyField` 里出现第二个 200**：写成 `200 - elapsed` 或把 `fieldLength` 再抄一遍常量。翻成什么样：`effectTicks` 改 100 后仍跑 200 刻，A-33 的判据直接红。提前发现：两轮（200 / 100）各测一次时序，两轮的消失刻数必须等于各自配置值；代码里只允许 `fieldLength` 一个来源。
3. **只改钩子、不落定物理**：`keepAliveAfterImpact` 返回 true 但没置零速度 / 没关重力 / 没免碰撞。翻成什么样：场落地后被水流冲走或被活塞推走，位置 30 秒内改变；视觉上「场在漂」。提前发现：落点在水流里位置 ±0 格；踩上去不被推动。
4. **音效判据搬进 `tick()`**：每刻判一次「有生物就播」。翻成什么样：整场播 200 次（或每次有人进出就播一次），`latest.log` 里命中音刷屏。提前发现：整场只有 1 条命中音，且空投 0 条。
5. **为了「场」新增方块实体**：给 `friend_chives` 加一个 `block_entity_type` 来承载计时。翻成什么样：注册面 93 → 94，十二类里的 `block_entity_type` 从 7 变 8，F-212 的读数与工具全红。提前发现：`grep -c "BlockEntityType"` 不新增；F-212 之前的每一次注册读数都只看 12 类的既有数字。
6. **改了基类的默认值**：把 `ThrownCropEntity` 的 `keepAliveAfterImpact` 默认改成 true。翻成什么样：别的投掷物（瓜片子、膨胀麦、其它种子）落地后不消失，全场是滞留实体。提前发现：该文件的 `git diff` 为空。
7. **自毁判据的位置或起点错了**：判在 `super.tick()` 之前，或 `fieldTick` 从 1 开始。翻成什么样：第 199 刻就消失，或第 200 刻还在（差一刻）。提前发现：三个点都测——第 1 刻 200、第 199 刻 1、第 200 刻查不到。
8. **读档自杀误伤会话内的场**：把 `loadedSettled` 的判据挂在「落地就置位」上。翻成什么样：投出去当场消失，根本形不成场。提前发现：会话内 200 刻的时序正常，只有「卸载 → 重载」这一条路径才消失。

### 3.6 回报格式

- **改了哪些文件**：`git diff --stat` 原文；`FriendChivesProjectile.java` 的四处（钩子、`onImpact`、`tick()`、读档分支）+ 新增方法名；`ThrownCropEntity.java` 为空 diff 的证据。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`python -X utf8 tools/verify_project.py` 的 §1 段（只读，不当判据）；两处 `grep`（`"Settled"`、`200`）；NBT 查看器的读取方式。
- **观察到什么**：200 与 100 两轮的逐刻读数（第 1 / 2 / 199 / 200 刻）；空投与命中的音效条数；水流里 30 秒的位置读数；卸载前后的两条查询结果；区块 NBT 的键名清单。
- **没做的与做不了的**：写「未验证」并给原因（例如没有第二台客户端做听觉对照）。
- **未关闭的问题**：例如 10 个场同时存在时的服务端 tick 时间（RT-34）、场与实体碰撞箱的相互作用。

---

## 4. Phase 29 · 耕地-核心绑定制（F-187 ~ F-198）

> 投喂方式：32 号主提示词全文 + §0.2 的三行 + 本节。**本节最长，仍然一次投完**：12 个任务里 4 条可并行分叉，但要按 §4.1 的两条串行链排。

### 4.1 前置条件

- §1 的出口判据全过；§2 / §3 **不构成前置**。本节做完之前，§5 不许开（浮窗的排序键与己方判定都在本节）。
- 手边这几份：`30` 号 §2.2（绑定表全节：本体三份、两个 record、落盘、兼容读、`setDirty` 表、惰性清理）、§2.5（ticket，含 `distance = 1` 的加载面）、§7 的 #16 ~ #22、#36；`29` 号 §3.4 / §3.6 / §3.10 与 §11 的 B-15 ~ B-25；`28` 号 D-134 ~ D-140、D-145 与 A-39 / A-42 / A-43 / A-45 / A-47；`31` 号表 A 的第 19 ~ 35、39 行、表 B.2、表 C 的第 1 ~ 4 行、表 E 的 RT-32 / RT-33 / RT-39 / RT-40。
- 两份 fixture 必须先备好：① 0.1.0 的 `world/data/farmstorm_farmland.dat`（只有 `owners`、无 `dataVersion`）；② 一份 20 块老田 + 1 个核的存档（容量 16，用于 F-190 的硬顶判据）。NBT 查看器在手。
- 键的现状：`[farmland]` 段仍在（组数 59）、`farmland.limitPerPlayer` 仍被 `base()` 读、`outpost_core.farmlandBonus` 默认 **16**、`outpost_core.maxPerPlayer` 默认 **2**。
- 两条串行链，不许打散（**A-56**：F-192 的声明依赖是 **F-191**——`base()` 是 `farmland.limitPerPlayer` 的唯一读者，属**编译顺序**，不是风格顺序）：**`F-191 → F-192 → F-197`**（`base()` 先无读者 → config 组才可删 → 指令树才可整棵删）；**`F-193 → F-194 → F-196`**（选核 → 写绑定 → 迁移读同一个 `Decision.core`）。两条链之间可并行；`F-187` / `F-188` / `F-189` 三个无前置的任务先落地，`F-190` / `F-191` / `F-193` / `F-195` / `F-198` 才可开。
- 冻结面：`util/DeployedOwnership.java` 的签名与调用点、`isOwnerOrAlly`（保持「只有定义、无调用者」）、`Verdict` 的三态结构与 `QUOTA_FULL` 的语言键名、`Farmstorm.java` 的 12 类自检数字。

### 4.2 本节目标

本版最大的一处结构替换：把「范围内即受管」换成「显式绑定」，并把牵动的六条链路（己方判定、存档、补建、索引比较键、额度、迁移）、两处删除（config 组、服务端指令树）、一处新增（区块 ticket）一次做完。

本节结束时能做的事：拿锄头在核的 3×3 区块里开垦，每一格绑给**最近的**己方核；第 17 格被拒并提示「耕地额度已满 16/16」；核主下线后他的田照样踩不坏，没有绑定的田照常变泥土；拆核先把田迁给接替核、迁不动才变泥土（**远端拆核与近处一致，同样变泥土**）；拿 0.1.0 老档启动会打印「耕地补建：绑定 N 块 / 保持未绑定 M 块」，同一份存档跑两次 N / M 一字不差；`/farmstorm` 整个消失；`farmstorm-server.toml` 里没有 `[farmland]` 段、段行 **58**、键 **166**；工具**第一次**输出 `结论：FAIL 0 项`（A-40 在此关闭）；核在没人的时候仍 tick，而 `chunks.dat` 里查不到它。

### 4.3 任务清单（F-187 ~ F-198）

| 任务号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|
| F-187 | `TeamResolver` 的 `Owner × Owner` 重载 | M / 1.5 | — | 新重载 `isAlly(ServerLevel, Owner, Owner)`，`Player` 版退化成一行委托；`DeployedOwnership.isAlly` 不动；`untamedFallback` 语义逐条等价 |
| F-188 | `FarmlandLedger` v2：绑定表、`orphans`、兼容读与写入口 | L / 3.0 | — | 三份本体（`bindings` / 派生的 `byCore` 不落盘 / `orphans` 要落盘）+ 两个 record + `dataVersion: 2` + v1 全读成 `orphans` + `bind` / `unbind` / `migrate` / `bindingsOf` / `orphanCount` / `rebuildByCore`；惰性清理**同时剪 `byCore`** |
| F-189 | `OutpostCoreIndex.Entry.gameTime` 与三个只读访问器 | M / 1.5 | — | `record Entry(dimension, pos, long gameTime)` 落盘；老档缺值取 `Long.MIN_VALUE` 并打计数；加 `entriesOf` / `owners` / `count(server, uuid)`；比较路径不 `getChunk` / 不 `getBlockEntity` |
| F-190 | 启动补建 `backfill` | M / 1.5 | F-188、F-189 | 一次性启动闸 + 按 `BlockPos#compareTo`（y → z → x）**单级全序**排序后逐条处理（A-47）；谓词与迁移同一条；整段没有 `setBlock`；收尾一条 INFO 带 N / M / T |
| F-191 | 额度 / 已用改口径，删 `base` 三件套 | M / 1.5 | F-187、F-188、F-189 | `capacity = farmlandBonus × 己方核数`、`used` = 绑定表里己方名下的条数、`orphans` 不计入；删 `baseOverride` / `base()` / `setBase()` / `personalCapacity` 与 `sumOverMembers` 的 base 参数；指令侧临时改只读回显保住编译 |
| F-192 | 删 config 的 `farmland` 整组与 1 条语言键 | S / 0.5 | F-191 | 删字段、整组、getter 与语言键生成段；删旧 toml 重新生成后段行 **58**、键 **166**；这是 A-40 的收口点 |
| F-193 | `FarmlandGuard.check` 选核 + 单核空位 | L / 3.0 | F-187、F-188、F-189 | 新增 `record Decision(Verdict, @Nullable CoreKey)`；候选集**先过滤再取最近**；三级键 `placedGameTime` → 维度 id → `BlockPos#compareTo`；`QUOTA_FULL` 语义变为「最近的己方核满了」 |
| F-194 | 锄地成功时写入绑定 | M / 1.5 | F-193 | 锄地闸的 `ALLOW` 分支把 `ledger.add(...)` 换成 `ledger.bind(owner, coreKey, pos)`；同一 tick 内完成写绑定 + 记账 + 放耕地；被拒时一条绑定都不产生 |
| F-195 | 耕地保护的离线判定与踩踏面 | M / 1.5 | F-187、F-188 | `protectsFromTrample` 按「有绑定即受保护」（A-45）；`inOwnRange` 读绑定表 + `isAlly(核主, player)`；`getPlayerList().getPlayers()` 0 命中 |
| F-196 | 核被毁：先迁移、三支降级 | L / 3.0 | F-188、F-189、F-195 | 只处理 `bindingsOf(dead)`；改绑 / `setBlock(DIRT)` + 摘绑定的**两个出口 + 一条防御分支**（「只摘绑定」，A-60 正常运行不可达）；先算完集合再动世界；不为清算加载区块；不通知、不掉落、不补偿 |
| F-197 | 删 `/farmstorm farmlandlimit` 整棵服务端指令树 | M / 1.5 | F-191 | 整文件删除 + 3 处注册删除 + 1 条语言键；不保留空壳，`/farmstorm` 根节点不注册；客户端 `hud` 子树不碰 |
| F-198 | 核心所在区块的运行期临时 ticket | M / 1.5 | F-189 | 自有 `TicketType`（`distance = 1`）+ 三处幂等释放 + 起服按索引补票；**只用裸 `TicketType`**，`setChunkForced` / `forceChunk` 两者都否决（A-38）；口径按 tick 面写（A-43） |

阶段小计：**12 个任务、21.5 人日**（S 1 / M 8 / L 3）。

### 4.4 出口判据（可观察）

- [ ] **绑定的落盘形态（F-188 / F-194）**：怎么验：锄 1 格后 `/save-all`，再用 NBT 查看器读 `farmstorm_farmland.dat`。通过的样子：含 `dataVersion: 2`、`bindings` 里 **1** 条，键为 `pos` / `owner` / `coreDimension` / `corePos`，`coreDimension = "minecraft:overworld"`；**没有** `byCore` 键；同一份存档再启动一次，「耕地绑定指纹（1 条）」非空且指纹相同。
- [ ] **额度与第 17 格（F-191 / F-193）**：怎么验：一个核名下连开到第 17 格；再把 `farmlandBonus` 改 **2** 重启重测。通过的样子：第 **17** 格被拒并提示「耕地额度已满 16/16」，该核名下仍只有 **16** 条；改 2 之后第 **3** 格即被拒、队伍容量同步变 2；被拒时 `ItemDamage` 前后相同。
- [ ] **三级键可复现（F-189 / F-193）**：怎么验：两核相距 40 格 → 等距 → 离线把两个 `gameTime` 改成同值，各锄一格；再连跑两次启动。通过的样子：依次绑「较近」、「`gameTime` 更早」、「坐标字典序在前」；两次启动的「核心比较键指纹」与「耕地绑定指纹」两行完全相同。
- [ ] **保护面（F-195 / A-45）**：怎么验：核主下线后由非同队玩家跳踩；同一位置一块无绑定的耕地做对照；僵尸跳踩有绑定的耕地；换队后再踩一次。通过的样子：前两者分别「仍是 `minecraft:farmland`」与「变泥土」；僵尸踩不坏；换队后踩踏面不变、而额度读数按新队重算（两行并排取证）。
- [ ] **拆核两级（F-196）**：怎么验：① A 名下 3 块、B 覆盖其中 2 块，拆 A；② 无接替核且人在田边，拆 A；③ 同样无接替核、但先走远 200 格，用 `/execute in` 拆 A。通过的样子：① **有接替核 → 改绑**：那 2 块改绑到 B、第 3 块变 `minecraft:dirt`，B 名下 ≤ **16**；② **无接替核 → 变 `minecraft:dirt`**、**没有掉落物**；③ **远近都一样**：远端拆核同样变 `minecraft:dirt`、无掉落，日志**不出现**「迁移失败且区块未加载：只摘绑定」——那条串以代码审阅确认存在于 `OutpostCoreBlockEntity.java:374`、**正常运行不出现**。
- [ ] **补建可复现（F-190 / A-47）**：怎么验：用 0.1.0 老档连跑两次；把 `.dat` 里 `orphans` 的两条记录对调再跑一次；再造 20 块老田 + 1 个核。通过的样子：日志「耕地补建：绑定 N 块 / 保持未绑定 M 块（老档共 T 块，按 `BlockPos` 全序）」，两次 **N 与 M 完全相同**，对调后绑定的坐标集合与 N / M 也不变；20 田 1 核时 **N = 16、M = 4**；补不上的田逐个 `/execute if block … minecraft:farmland` 全成功、**0** 个变泥土。
- [ ] **两处删除（F-192 / F-197）**：怎么验：删旧 toml 起服读段行与键数；控制台敲 `/farmstorm`；`grep` 三处。通过的样子：`[farmland]` 段不存在、段行（`^\[`）**58**、键 **166**、`pumpkin_turret` 仍 5 键；`/farmstorm` 与 `/farmstorm farmlandlimit 5` 都失败；`grep -c "farmlandlimit"` 在两份 lang 与 `ModLangProvider` 里都是 **0**；`grep -rn "FarmstormCommands"` **0 命中**。
- [ ] **A-40 在这里关闭**：怎么验：删旧 toml、重新生成之后跑一次工具。通过的样子：`结论：FAIL 0 项`、退出码 **0**、§1 段 **166**。这是本文件里**第一条**可以引用工具结论的判据；从这个判据之后，`FAIL 0` 才可以写进验收记录。
- [ ] **ticket 的 tick 面（F-198 / A-43）**：怎么验：走远 200 格让本地卸载，读边界粒子轮次；拆核后再走远；读三个维度的 `chunks.dat`；停服再起服。通过的样子：核的粒子按 **40** 刻持续增长；拆核后该区块 F3 显示**未加载**；`Forced` 数组里**不含**该核的 `ChunkPos.toLong()`；重启后票重建；`grep -c "setChunkForced|forceChunk"` = **0**；三层区块状态各截一次（中心在 tick / 紧邻一圈已加载 / 再外未加载），**不许**写「只有 1 个区块被加载」。
- [ ] **语言键的区间读数**：怎么验：数两份 lang。通过的样子：各 **314** 键（`316 − 2`，即 F-192 与 F-197 各删 1 条），中英键集仍相等。

### 4.5 翻车点

1. **幽灵耕地占住容量**（`30` 号 §9 第 3 条）：惰性清理只从 `bindings` 剪，不从派生的 `byCore` 剪。翻成什么样：核名下只剩 15 块，却一直提示「16/16」，玩家以为额度坏了。提前发现：把一条已绑坐标 `/setblock air` 后触发一次惰性校验，`bindings` 与 `byCore` 的条数**同时**减 1（日志里「绑定指纹（M 条）」与「反查集合 N 条」两行同时变）。
2. **补建顺序不可复现**（A-47 rev.2）：按 `orphans` 的落盘顺序、或按 `byOwner`（`HashMap`）的迭代顺序逐条处理。翻成什么样：同一份存档两次启动 N / M 不同，`30` 号 §7 #19 直接红。提前发现：连跑两次 + 把 `.dat` 里两条 `orphans` 对调再跑，两次共三组读数必须一致。
3. **旧 toml 没删，配置不生效**（F-192）：以为改完代码新默认值就会生效。翻成什么样：`[farmland]` 段残留、段行仍 59、`bulletSpeed` 也补不上；工具对残留段报 STALE，被误判成「工具改错了」。提前发现：删 `run/config/farmstorm-server.toml` 重新生成后段行 58 / 键 166；残留是**已知的可恢复中间态**，不是缺陷。
4. **先取最近、再判空位**（F-193 第 4 条）：候选集没先过滤。翻成什么样：「最近的满、别处还有空位」被误拒，与 `16 × 核数` 的总额度自相矛盾。提前发现：`29` 号 B-17 的对照（最近满、另一核有空位 → 绑到有空位的那个）。
5. **补建里出现 `setBlock`**（F-190 第 3 条）：为了让「补不上的」看起来干净，顺手铲成泥土。翻成什么样：老档启动后一片田变泥土，且 M 恒为 0。提前发现：`grep -c "setBlock" farmland/FarmlandLedger.java` = **0**；补不上的逐块 `/execute if block` 全成功。
6. **为清算加载区块**（A-35 / A-60）：`bindingsOf` 之后逐块 `getChunk`（而不是 `getChunkNow`）判方块，或者为「未加载」这条并不存在的情形另写一支。翻成什么样：拆核时一次强制加载连锁一批区块（卡顿）；而正确行为是全程 `getChunkNow`——票已保证核名下耕地的区块在内存，走远 200 格拆核田照样变泥土。提前发现：`grep -c "getChunk" blockentity/OutpostCoreBlockEntity.java` 的读点不新增；第③支的方块读数是 `minecraft:dirt`、且不出现「只摘绑定」日志。
7. **额度仍按在线成员求和**（F-191）：`capacity` 继续走 `getPlayerList()`。翻成什么样：队友下线后 A 的额度变小（改前的 bug 原样保留）。提前发现：A、B 同队各 1 核，B 下线后 A 的额度仍是 **32**；`grep -c "baseOverride|personalCapacity|sumOverMembers"` = **0**。
8. **改了 `QUOTA_FULL` 的语言键名**（`30` 号 §9 第 4 条）：觉得语义变了就顺手改键。翻成什么样：两份 lang 少一条、多一条，323 的账在 §6 之前就错位。提前发现：`message.farmstorm.farmland.quota_full` 与 `.no_core` 都仍在，只改文案内容。
9. **指令树留空壳**（F-197）：删了子指令但 `/farmstorm` 根节点还注册。翻成什么样：控制台不报「未知的指令」，玩家敲 `/farmstorm` 得到一个空列表。提前发现：服务端与单人两边都敲一次 `/farmstorm`；`FarmstormCommands` 0 命中。
10. **ticket 写成 `setChunkForced` / `forceChunk`**（A-38）：图省事用原版强制加载。翻成什么样：`chunks.dat` 的 `Forced` 数组里出现核坐标，永久强制加载、拆核也不释放。提前发现：`grep -c "setChunkForced|forceChunk"` = **0**；三个维度的 `chunks.dat` 各查一次。
11. **把 A-43 写成「只加载 1 个区块」**：文档与验收都会写成必红的那一条。提前发现：只写 tick 面（中心 level 32 / 紧邻 33 / 再外不加载），三层各截一次。

### 4.6 回报格式

- **改了哪些文件**：`git diff --stat` 原文；12 条各自的文件（含被删的 `command/FarmstormCommands.java`）与新增类（`outpost/CoreChunkTickets.java`）；三处注册删除的行号。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`python -X utf8 tools/verify_project.py` 的**完整输出**与退出码（本节第一次允许引用 `FAIL 0`，必须给原文）；`/farmstorm` 两边各一次；六处 `grep -c`（`setBlock`、`getPlayerList().getPlayers()`、`baseOverride` 组、`farmlandlimit`、`FarmstormCommands`、`setChunkForced|forceChunk`）。
- **观察到什么**：`bindings` / `orphans` 的 NBT 键与条数；两次启动的 N / M 与两行指纹；第 17 格与 `farmlandBonus = 2` 两次拒绝的文案原文；拆核三支的方块读数与日志行；补建日志的**原文**；段行 58 / 键 166；ticket 的粒子计数与三层区块状态。
- **没做的与做不了的**：写「未验证」并给原因（例如没有第二台客户端）。
- **未关闭的问题**：例如 A-49 的耕地跨维度同坐标撞车（已知限制，本版不修，只登记）、`byCore` 与 `bindings` 在极端中断下的漂移面。

---

## 5. Phase 30 · 浮窗与数据面（F-199 ~ F-205）

> 投喂方式：32 号主提示词全文 + §0.2 的三行 + 本节。

### 5.1 前置条件

- §4 的 **F-187、F-188、F-189** 已提交：`isAlly(ServerLevel, Owner, Owner)` 重载、`FarmlandLedger` v2、`OutpostCoreIndex.Entry.gameTime` 与三个只读访问器。少任何一个，本节只能画出空列表。
- 手边这几份：`30` 号 §2.1（payload / codec / 注册 / 客户端快照 / 三级排序 / 去抖 / 10 个时机）、§2.4（浮窗四条前置、行格式、`10n + 3`、配色、拖拽、客户端指令）、§4.2（client 3 键）、§7 的 #6 ~ #15 与 #29、§8 的 ① ② ⑤ ⑥ ⑦ ⑧ 六条待验证 API；`28` 号 D-142 ~ D-144 与 D-146；`31` 号表 B.3（六个不进注册面的构件）、表 C 的第 5 ~ 7 行、表 E 的 RT-31。
- 环境：能同时起「单人客户端」与「专用服务器」；`run/config` 下**没有** `farmstorm-client.toml`（它是本节第一次生成的）。
- 四条第一次：这是全仓第一个自定义网络包、第一份 CLIENT config、第一批 `KeyMapping`、第一条客户端指令。每条都按上一档估工，且每条都要求**先写最小用例跑通、再写正式逻辑**（`30` 号 §8 的 ① ② ⑤ ⑦ ⑧）。
- 冻结面：注册面仍 **93**（网络包 / 按键 / CLIENT config / 客户端指令四者都不进 `BuiltInRegistries`）；`farmstorm-server.toml` 仍 **166** 键 / **58** 组且**不出现** `hud` 段；`FarmstormConfig` 不新增任何东西。

### 5.2 本节目标

把本版唯一一条「破红线」的需求做完：1 个 S2C 只读包 + 一块自绘浮窗 + 2 个键位 + 3 键 CLIENT config + 1 条客户端指令。数据面（F-199）→ 广播（F-200）→ 渲染（F-203）是本版最长的一条串行链；CLIENT config（F-201）与键位 / 拖拽 / 指令（F-202 / F-204 / F-205）挂在它两侧。

本节结束时能做的事：左上角出现一块面板，一行一个己方核，形如 `#1 [overworld] (x, y, z) 自己`；9 个核时是 8 行核心 + 1 行 `+1`、面板高 93 px；按 **H** 开关并持久化，按 **J** 进拖拽、`ESC` 保存位置，`/farmstorm hud on|off|reset` 三个动作都写进 `config/farmstorm-client.toml`；换服不残留、敌对核不外发；全仓只有 1 个自定义包，客户端不回包，专用服务器能正常起。

### 5.3 任务清单（F-199 ~ F-205）

| 任务号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|
| F-199 | 1 个 S2C 只读包：payload / codec / 注册 / 快照 | L / 3.0 | — | `farmstorm:core_list` 的 payload（`List<Core>` + 单独发的 `int total`）+ 4 元 `StreamCodec` + `MAX_CORES = 64` + `playToClient` 注册 + 客户端 handler（只有一句 `enqueueWork`）+ `volatile` 快照 |
| F-200 | 广播器：组装 / 三级排序 / 去抖 / 10 个时机 | L / 3.0 | F-199、F-189、F-187 | `CoreListBroadcaster`：候选按 `gameTime` → 维度 id → `BlockPos#compareTo` 排序（比较键**只从索引读**）；`MIN_INTERVAL_TICKS = 10` 去抖；`RECONCILE_INTERVAL_TICKS = 100` 对账；10 个时机逐条挂；登出清三条记录 |
| F-201 | CLIENT config 3 键 + 3 条语言键 | M / 1.5 | — | 新增 `FarmstormClientConfig`（`ModConfig.Type.CLIENT`），`[hud]` 三键 `enabled = true` / `x = 4`（0 ~ 3840）/ `y = 4`（0 ~ 2160）；写盘走 `ConfigValue#set` + `SPEC.save()`；语言键 +3 |
| F-202 | 2 个 `KeyMapping` + 2 条描述键 | M / 1.5 | F-201 | `FarmstormKeys`：开关 **H** / 拖拽 **J**，分类复用 `KeyMapping.CATEGORY_GAMEPLAY`（不自建分类）；在既有 `ClientTickEvent.Post` 里 `consumeClick()` 消费；语言键 +2 |
| F-203 | 浮窗渲染 + 1 条「自己」标识键 | L / 3.0 | F-199、F-200、F-201 | `CoreHudRenderer`：四条前置早退（含「0 核不画」）、行格式 `#%d [%s] (%d, %d, %d) %s`、面板高 `10n + 3`（8 行 83 / 9 行 93）、宽度按 `font.width` 实测、绘制前按屏幕尺寸钳制、五种配色 |
| F-204 | 拖拽定位 `CoreHudDragScreen` | M / 1.5 | F-203、F-202、F-201 | `isPauseScreen()` 返回 `true`；`mouseClicked` 落在面板内才拖、`mouseDragged` 写预览并钳制、`mouseReleased` 不写盘、`onClose()` 才写 config；编辑态例外让 `hideGui` / `screen != null` 两条早退失效 |
| F-205 | 客户端指令 `/farmstorm hud` + 3 条反馈键 | M / 1.5 | F-201 | `RegisterClientCommandsEvent` 注册 `hud <on\|off\|reset>`，三个子命令**不带** `requires`；`reset` 只复位位置不动 `enabled`；反馈 `sendSuccess(..., false)`（本地显示、不广播）；语言键 +3 |

阶段小计：**7 个任务、15.0 人日**（S 0 / M 4 / L 3）。

### 5.4 出口判据（可观察）

- [ ] **一个包、不回包（F-199）**：怎么验：`grep` 两处 + 起专用服务器。通过的样子：`grep -rln "CustomPacketPayload" src/main/java` 恰 **1** 个文件；`grep -rn "playToServer|sendToServer"` **0 命中**；专用服务器完成启动、无 `NoClassDefFoundError`；`latest.log` 的注册行含 `farmstorm:core_list`；`grep -c "MAX_CORES"` ≥ **2**。
- [ ] **下发与排序（F-200）**：怎么验：单人 1 核；先放 A（t=100）再放 B（t=200）；同一份存档重启两次；同一刻连锁放核 + 拆核；从服务器 X 退出去 Y。通过的样子：日志「核心列表下发：<玩家> 1 条（total=1）」；A 在 B 前；两次重启的「核心比较键指纹」两行完全相同；连锁只产生 **1** 个包；Y 上浮窗**立即消失**，回 X 再出现。
- [ ] **跨维度与满员（F-200 / F-203）**：怎么验：主世界 1 核 + 下界 1 核；再构造 9 个己方核。通过的样子：2 行、行首分别是 `overworld` / `the_nether`，传送后不变；9 核时 **8 行核心 + 1 行 `+1` = 9 行**、面板高 **93 px**，8 核时 8 行、**83 px**、无 `+N`。
- [ ] **一份新 toml（F-201）**：怎么验：首次起客户端读 `config/farmstorm-client.toml`；再起专用服务器看它有没有被生成。通过的样子：`[hud]` 恰 **3** 键、值 `true` / `4` / `4`；服务器**不生成**这一份；`farmstorm-server.toml` 里没有 `hud` 段；手改 `x = 5000` 重启后加载值被钳到 **3840**。
- [ ] **两个键位（F-202）**：怎么验：按键界面看两条键位；按 H 三次；按 J 一次。通过的样子：「游戏操作」页能看到两条、默认 **H / J**；`enabled` 在 true / false 之间翻转且重启保持；J 打开一个 Screen 且单人世界**暂停**；`grep -c "key.categories.farmstorm"` = **0**。
- [ ] **拖拽（F-204）**：怎么验：拖到右下角后 `ESC`；拖到屏外松手（四个角各一次）；拖拽中改 GUI 缩放。通过的样子：`hud.x` / `hud.y` 等于落点坐标、重进游戏不变；越界部分 **0 px**，面板完整可见；改缩放后重新钳制；关闭后**不弹**暂停菜单。
- [ ] **一条客户端指令（F-205）**：怎么验：`/farmstorm hud off|on|reset` 各一次；看补全；看第二客户端。通过的样子：三动作分别写 `enabled = false` / `true` / `x = y = 4` 且 `enabled` 不变；补全 `/farmstorm ` 只有 `hud`，其下只有 `on` / `off` / `reset`；`/farmstorm farmlandlimit 5` 失败；反馈只出现在本地聊天栏。
- [ ] **三个数没动（本节最容易被顺手破坏的一条）**：怎么验：读注册面、`farmstorm-server.toml`、两份 lang。通过的样子：注册对象仍 **93**；server config 仍 **166** 键 / **58** 组；两份 lang 各 **323** 键（`314 + 3 + 2 + 1 + 3`），`farmstorm.configuration.` 恰 **169**（`166 + 3`）。

### 5.5 翻车点

1. **`Type` 用 `CustomPacketPayload.createType("core_list")`**：图省事不走 `Farmstorm.id`。翻成什么样：包落进 `minecraft:` 命名空间，客户端收到的是另一个 id，浮窗永远空。提前发现：注册行必须是 `farmstorm:core_list`。
2. **`total` 写成 `cores.size()`**：截断后 `+N` 恒为 0。翻成什么样：9 个核时画 8 行、`+N` 变成 `+0` 或直接不画，A-34 的 93 px 判据红。提前发现：构造 9 核看 `+1`；`total` 必须是**截断前**的总数，单独发。
3. **handler 直接引用客户端类**：把 `ClientCoreList.set(...)` 写进方法体而不是 lambda 里。翻成什么样：专用服务器启动时 `NoClassDefFoundError`，纯服务端起不来。提前发现：专用服务器起一次；`enqueueWork` 的 lambda 是唯一入口。
4. **CLIENT config 塞进 `FarmstormConfig`**：翻成什么样：`farmstorm-server.toml` 多 3 键（166 → 169），工具 §1 段立刻红；而且这份配置会在专用服务器上被当成服务端配置加载，位置读数与开关在客户端根本不生效。提前发现：`[hud]` 只在 `farmstorm-client.toml` 里，`ModConfig.Type.CLIENT` 是唯一正确的类型。
5. **`KeyMapping` 自建分类**：多一条 `key.categories.*`。翻成什么样：语言键 323 的账对不上（324），且按键界面多出一个分类。提前发现：分类必须是 `KeyMapping.CATEGORY_GAMEPLAY`；`grep -c "key.categories.farmstorm"` = 0。
6. **`+N` 用语言键**：觉得「+1」应该可翻译。翻成什么样：多 1 条键，`30` 号 §4.3 的 10 条新增变成 11 条。提前发现：`"+" + (total − 8)` 是纯数字拼接，语言键只有那 10 条。
7. **早退条件把拖拽态也挡掉**：`hideGui` 与 `screen != null` 两条在拖拽时也生效。翻成什么样：按 J 之后玩家看不到自己在拖什么。提前发现：J 之后面板仍画（编辑态例外）。
8. **`mouseReleased` 就写盘**：或 `ESC` 顺手打开暂停菜单。翻成什么样：拖到一半松手就落盘，位置与最终预览不一致；`ESC` 后叠一个暂停菜单。提前发现：拖拽中间不写盘、关闭后不弹菜单。
9. **`isPauseScreen()` 返回 false**：翻成什么样：单人世界里拖拽时世界继续 tick，怪照打、作物照长，位置读数与拖动时刻对不上。提前发现：J 之后画面静止、世界不 tick。
10. **反馈用 `sendSuccess(..., true)`**：与已删的服务端指令同一个写法。翻成什么样：反馈被广播给全服，第二客户端的聊天栏跟着刷。提前发现：`grep -c "sendSuccess"` = **3** 且第二参数都是 `false`。
11. **客户端指令加 `requires(...)`**：翻成什么样：单人补全里一个子命令都不出现。提前发现：补全读到 `on` / `off` / `reset` 三项。

### 5.6 回报格式

- **改了哪些文件**：`git diff --stat` 原文；新增 8 个文件逐个点名（`network/` 三件、`client/` 五件）与两处注册点（`Farmstorm.java:56` 之后一行、`FarmstormClient.java`）；`datagen/ModLangProvider.java` 的 9 条新增。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`python -X utf8 tools/verify_project.py` 的完整输出；两处 `grep -rln` / `grep -rn`；`grep -c` 三处（`MAX_CORES`、`key.categories.farmstorm`、`sendSuccess`）；专用服务器启动日志的注册行。
- **观察到什么**：浮窗 1 行 / 2 行 / 8 行 / 9 行的四组截图与面板高度像素；日志里「核心列表下发」与两行指纹；`farmstorm-client.toml` 的三键读数与钳制读数；H / J 三动作的写盘读数；拖拽四角与缩放两组；换服前后两帧。
- **没做的与做不了的**：写「未验证」并给原因（例如第二客户端不可用、`②` / `⑥` 两条待验证 API 没能确认）。
- **未关闭的问题**：例如 `MAX_CORES = 64` 的单包大小实测（`30` 号 §8 ⑥）、`LoggingOut` 在「被踢出」路径下是否触发（⑧）。

---

## 6. Phase 31 · 收口与全量验收（F-206 ~ F-212）

> 投喂方式：32 号主提示词全文 + §0.2 的三行 + 本节。

### 6.1 前置条件

- §2 / §4 / §5 的出口判据全过；**A-40 窗口已关闭**（F-192 已落地，且删旧 `farmstorm-server.toml` 重新生成过一次）。从本节开始，`结论：FAIL 0 项` 才可以写进验收记录——这是 A-40 与 `31` 号 §0.4 第 3 条的分界。
- 手边这几份：`30` 号 §7 的 **38 条**（#1 ~ #38）、§4.3 的语言键逐条清单、§8 的 8 条待验证 API；`29` 号 §11 的 **39 条**（B-01 ~ B-39）；`28` 号 D-124 / D-147 与 §4.5（README 要改的三处）；`31` 号 §6 的七张表（A ~ G）与 §7 的 20 条自检。
- 目录与产物：`.work/v011-verify/` 可写（截图 / 录屏 / sha256 清单都落这里）；`dist/` 目录存在；`build/libs` 里此刻可能有旧 jar，本节结束前必须清到只剩 0.1.1 的两支。
- 本节七个任务里（F-206 ~ F-212），**F-206 与 F-207 改文件**（语言键注释与产物），**F-208 ~ F-212 一律不改码**（它们只产出记录）。「不改码」是本节最容易被破坏的约束：验收不通过就回到对应任务重做，**不许就地改判据、不许改实现让记录变绿**。
- 冻结面：语言键**只许改类注释、不许增删键**（增删已在 §2 / §4 / §5 各自完成）；`30` 号 §7 的 38 条与 `29` 号 §11 的 39 条**都不许改写**（A-46：边界 ≠ 验收条目，两套各自成立）。

### 6.2 本节目标

把三件事一次收口：语言键总账（F-206，`315 − 2 + 10 = 323`）、发布产物（F-207）、两套判据（`30` 号 §7 的 38 条 + `29` 号 §11 的 39 条 = **77 条**，分四个批次跑完）。F-212 是唯一的汇合点：77 条 + 37 个任务 + 七张表 + 五处计数全部要有结论。

本节结束时能做的事：把 jar 丢进专用服务器，只装模组本体的玩家就能进服；中文界面无缺字（浮窗、额度提示、客户端指令反馈都在）；`dist/farmstorm-0.1.1+mc1.21.1.jar` 可以发布；作者手里有一份 77 行结论与 37 行任务状态的汇总记录，逐条能指到日志行、命令返回值或文件路径。

### 6.3 任务清单（F-206 ~ F-212）

| 任务号 | 标题 | 复杂度 / 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|
| F-206 | 语言键总账 323 × 2 与 `ModLangProvider` 注释 | M / 1.5 | F-182、F-192、F-197、F-201、F-202、F-203、F-205 | 逐类复核 `configuration` **169** / `gui.farmstorm` **9** / `command.farmstorm` **3** / `key.farmstorm` **2**；只改类注释的键数叙述（「299 + 16 = 315」那一类），**不增删任何键**；两份键集逐字相同 |
| F-207 | 构建产物与 `dist/` 放行 | S / 0.5 | F-176、F-179、F-198、F-199、F-203、F-206 | `build/libs` → `dist/` 的两支 jar；`jar tf` 条目逐项对上（1 张替换 PNG、无新增 PNG / JSON、三个新类各 1 条）；`neoforge.mods.toml` 的 `version="0.1.1"`；`chunks.dat` 不含核坐标 |
| F-208 | 30 号 §7 的 #1 – #19 验收 | M / 1.5 | F-182、F-188、F-189、F-190、F-191、F-192、F-193、F-194、F-197、F-199、F-200、F-201、F-202、F-203、F-204、F-205、F-206 | 19 条逐条四列记录（动作 / 预期 / 实测 / 结论）；不改码 |
| F-209 | 30 号 §7 的 #20 – #38 验收 | M / 1.5 | F-177、F-179、F-180、F-181、F-182、F-183、F-184、F-185、F-186、F-192、F-196、F-198、F-199、F-200、F-203 | 19 条逐条四列记录；#36 按 tick 面写（A-43）、#38 引用工具时带四段原文；不改码 |
| F-210 | 29 号 §11 的 B-01 – B-20 边界复跑 | M / 1.5 | F-189、F-191、F-193、F-194、F-195、F-196、F-199、F-200、F-201、F-203、F-204 | 20 条一一对应（编号必须出现在验收条目里），其中 **14** 条在 30 号 §7 里没有对应条目；纯观感类附截图（`.work/v011-verify/B-xx.png`）；A-49 只在末尾列一行、不设通过项 |
| F-211 | 29 号 §11 的 B-21 – B-39 边界复跑 | M / 1.5 | F-178、F-179、F-180、F-181、F-182、F-184、F-185、F-186、F-188、F-190、F-196 | 19 条一一对应，其中 **7** 条在 30 号 §7 里没有对应条目；观感 / 听觉类附截图或录屏；B-24 / B-25 先备「20 块老田 + 1 核」的存档再跑 |
| F-212 | 全量汇总与上游一致性签字 | L / 3.0 | F-208、F-209、F-210、F-211 | 77 条结论 + 37 个任务状态 + 五处计数（93 / 166 / 3 / 323 × 2 / 37）+ 七张表覆盖数 + 红线三个计数 |

阶段小计：**7 个任务、11.0 人日**（S 1 / M 5 / L 1）。

### 6.4 出口判据（可观察）

- [ ] **两份 323（F-206）**：怎么验：数两份 lang 的键并比键集；跑工具读 §3 段。通过的样子：各 **323** 键、键集逐字相同；`grep -c '"farmstorm.configuration.'` 恰 **169**；`grep -c farmlandlimit` 两份都是 **0**；前缀分布逐行相加 = 323；`grep -c "299 + 16 = 315"` = **0**。
- [ ] **发布产物（F-207）**：怎么验：`jar tf` + `neoforge.mods.toml` + 纯服务端起服。通过的样子：`dist/farmstorm-0.1.1+mc1.21.1.jar` 与 `-sources.jar` 在位；`assets/farmstorm/textures/item/wild_apple.png` **1** 条、本版无新增 PNG 条目；`farmstorm/network/CoreListPayload.class`、`farmstorm/client/CoreHudRenderer.class`、`farmstorm/outpost/CoreChunkTickets.class` 各 **1** 条；`version="0.1.1"`；`build/libs` 里没有 0.1.0 / 0.0.1 的残留；专用服务器无 `NoClassDefFoundError`。
- [ ] **38 条前半（F-208）**：怎么验：按 `30` 号 §7 的 #1 ~ #19 逐条跑。通过的样子：**19** 条逐条有实测读数与观察位置，未通过 **0** 条；#1 的启动日志逐字含十二类；#5 是 323 / 169 / `farmlandlimit` 0；#19 的 N 与 M 两次启动相同。
- [ ] **38 条后半（F-209）**：怎么验：按 #20 ~ #38 逐条跑。通过的样子：**19** 条逐条有读数；#22 变 `minecraft:dirt`（与近处一致）、无掉落，且日志**不出现**「迁移失败且区块未加载：只摘绑定」（该串在 `OutpostCoreBlockEntity.java:374`，正常运行不出现，以代码审阅确认）；#30 的两轮（200 / 100）都成立；#33 的 `Light = 15`；#36 的 `chunks.dat` 不含核坐标；#38 是工具的四段原文 + 退出码 0 + config 段 **166**。
- [ ] **39 条边界（F-210 / F-211）**：怎么验：按 `29` 号 §11 逐条做对照实验。通过的样子：**20 + 19 = 39** 条一一对应、每条编号出现在验收条目里；其中 **14 + 7 = 21** 条在 30 号 §7 里没有对应条目（就是 A-46 逐条点名的那 21 个编号），由这两批唯一承担；纯观感 / 听觉类附截图或录屏；A-49 只在两份记录末尾各列一行，**不设通过项**。
- [ ] **七张表与五处计数（F-212）**：怎么验：读汇总记录。通过的样子：表 A **44/44**（+ 补记 1 行）、表 B **20** 项、表 C **7** 项、表 D **5** 项、表 E **11/11**、表 F **8/8**、表 G **17** 行；五处计数一次读齐：注册 **93**、server config **166**、client config **3**、语言键 **323 × 2**、任务 **37**；77 条与 37 个任务全部有结论。
- [ ] **红线三个计数（F-212）**：怎么验：三个 `grep`。通过的样子：`@Mixin|mixins.json` **0 命中**；`CustomPacketPayload` 恰 **1** 个文件；`GameRules.register` 仍 **0** 命中（工程约束保留）。
- [ ] **工具四段一次读齐（A-40 已收口）**：怎么验：`python -X utf8 tools/verify_project.py`。通过的样子：`93 / 93`、`166 个配置键与文档逐字一致`、`323 键`、`结论：FAIL 0 项`，退出码 **0**。四段缺一段或退出码非 0，就不许宣布可发布。

### 6.5 翻车点

1. **验收记录只写「通过」**：77 条里一条读数都没有。翻成什么样：看起来全绿，作者复核时无从下手；F-212 的汇总就等于把「通过」抄了 77 遍。提前发现：每条四列，「实测」列每格必须是刻数 / 格数 / 键数 / 坐标 / 日志行之一。
2. **用 38 条代替 39 条，或反过来**（A-46）：只跑 `30` 号 §7，或只跑 `29` 号 §11。翻成什么样：`38 + 39 = 77` 的账对不上；21 条无对应断言的边界（B-01、B-02、B-03、B-05、B-08、B-10、B-11、B-13、B-14、B-15、B-17、B-18、B-19、B-20、B-21、B-24、B-25、B-26、B-31、B-32、B-35）一条都没验。提前发现：两批条数分开报，逐条编号在记录里可见。
3. **把 A-49 写成通过项或缺陷**：翻成什么样：39 条里多出第 40 条，或记录里出现「修复耕地跨维度撞车」。提前发现：它只在记录末尾列一行，写明「已知限制，本版不修」。
4. **改码让验收变绿**：看到 #30 的时序差一刻就去调 `effectTicks`，看到 B-23 的田不受保护就去改 `backfill`。翻成什么样：改了实现、判据也跟着改，三轮验收全部作废。提前发现：三批验收的 `git status` 只允许出现 `.work/v011-verify/` 与 `dist/`。
5. **`dist/` 里放旧 jar，或只放 jar 不放 `-sources.jar`**：翻成什么样：`neoforge.mods.toml` 的版本行不是 0.1.1，作者拿到的包行为对不上。提前发现：`jar tf` 读版本行；`build/libs` 只允许两支 0.1.1 产物。
6. **F-206 顺手加键或删键**：为了「补齐 323」多加一条 `gui.*`。翻成什么样：324、`configuration` 不再是 169。提前发现：`git diff datagen/ModLangProvider.java` 里不出现新的 `.translation(...)` 或删除的生成段，只改注释。
7. **把中间量当总数**（`31` 号表 G 的 G-8 / A-48）：写 165（删键后、加 `bulletSpeed` 前）或 321（漏两条按键描述键）。翻成什么样：汇总里的语言键数与本文件对不上，README 与 Release 说明也跟着错。提前发现：323 与 169 两个数**同时**读，且 `314 + 3 + 2 + 1 + 3 = 323` 的算式成立。
8. **先跑 F-209 再补 F-208**：两批的号段不重叠，但 #5 / #6 / #7 读的是 F-206 / F-199 / F-203 的产物。翻成什么样：同一份记录里前后读数不一致。提前发现：按号序做（F-206 → F-207 → F-208 → F-209 → F-210 → F-211 → F-212）。

### 6.6 回报格式

- **改了哪些文件**：`git diff --stat` 原文；F-206 只有 `ModLangProvider.java` 的注释段；F-207 只有 `dist/` 与 `build/libs` 的产物（不入库）。
- **跑了什么命令**：`.tools\gw.cmd build` 的退出码；`python -X utf8 tools/verify_project.py` 的**四段完整原文**与退出码；`jar tf` 的三组条目；三个红线的 `grep`；`sha256sum`（或 `Get-FileHash`）对 `dist` 两支 jar 的读数。
- **观察到什么**：两份 lang 的键数与 15 项前缀分布（`169 + 36 + 32 + 26 + 16 + 13 + 9 + 6 + 4 + 3 + 2 + 2 + 3 + 1 + 1`）；77 条逐条结论表；37 行任务状态表；七张表的覆盖数；五处计数。
- **没做的与做不了的**：写「未验证」并给原因（例如某条观感类没有录屏）。
- **未关闭的问题**：A-49 的已知限制一行；以及任何「只做到看起来对、拿不出读数」的项。

---

## 7. 收尾提示词（最后一个 Phase 之后投一次）

> 投喂方式：32 号主提示词全文 + 本节（**不再附任何 Phase 小节**）。这一节只做核对与修复，不新增任何功能、不改任何判据。

**谁做最终验收**：F-212 的执行者（在 §6 里跑完四个批次的同一个代理）出结论、产出证据；**签字是作者的动作**，按本节末尾那三条判据做，只看读数与文件，不看代理的自我陈述。代理写「已通过」而拿不出证据位置的行，一律按「未验证」处理，汇总表里也不许把它算成通过。

```text
项目收尾。请对 farmstorm v0.1.1 做一次发布前自检，只做核对与修复，不新增功能、不改验收判据。

1. 取数（A-40 已收口，本节可以引用工具结论）
   跑 python -X utf8 tools/verify_project.py，四段必须同时成立：
   ① 注册面「已核 registry 对象: 93 / 93」，12 类逐类 32 / 26 / 7 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1；
   ② config 段「166 个配置键与文档逐字一致」，无 MISMATCH、无 real_extra；
   ③ lang 段「lang 键集一致，共 323 键」；
   ④ 「结论：FAIL 0 项」、退出码 0。
   任何一段红就先停下汇报，不要宣布可以发布。工具里若还残留旧期望值（例如 59 组、165 键、321 键），按「未完成」处理。

2. 构建与产物（F-207）
   跑 .tools\gw.cmd build（不要直接调 gradle），退出码必须是 0；
   产物 build/libs/farmstorm-0.1.1+mc1.21.1.jar 与 farmstorm-0.1.1+mc1.21.1-sources.jar 复制到 dist/；
   jar tf dist/farmstorm-0.1.1+mc1.21.1.jar 里 META-INF/neoforge.mods.toml 的 version 必须是 "0.1.1"、modId 是 farmstorm；
   build/libs 里不许有 0.1.0 / 0.0.1 的残留；dist 里那份与刚构建的这份必须是同一个文件（比字节数与 sha256）；
   记录 dist 两支 jar 的 字节数 + SHA256（前 8 位与后 5 位），写进 README 的版本行与 Release 说明。

3. jar 里必须有的东西（新增 0 张 PNG、0 个 JSON，就地替换 1 张 PNG）
   jar tf 逐条点名：assets/farmstorm/textures/item/wild_apple.png（64×64 / 6,946 B，本版唯一的就地替换）；
   以及三个新类的 class 条目各 1 条：farmstorm/network/CoreListPayload.class、farmstorm/client/CoreHudRenderer.class、
   farmstorm/outpost/CoreChunkTickets.class；
   再确认「不新增」的三类确实不在：没有新 PNG、没有新 JSON（模型 / 方块状态 / 战利品 / 配方 / 进度全是 0）、sounds.json 与 ogg 未动。
   缺任何一条就停下来汇报。

4. 77 条判据 + 37 个任务 + 七张表
   打开 docs/zh/30-TECH-0.1.1.md §7，输出 38 行核对表（编号 / 结论 / 证据位置）；
   打开 docs/zh/29-DESIGN-0.1.1.md §11，输出 39 行核对表（编号 / 结论 / 证据位置）；
   两表合计 77 行，未通过 0；每条的「证据位置」必须能指到日志行、命令返回值或文件路径，
   只写「通过」的条目一律按「未验证」处理。
   再打开 docs/zh/31-TASKS-0.1.1.md，按 F-176 ~ F-212 输出 37 行任务核对表（任务号 / 标题 / 是否达成 / 证据位置）。
   最后逐张复核 31 号 §6 的七张表：表 A 44/44（+ 补记 1 行）、表 B 20 项、表 C 7 项、表 D 5 项、表 E 11/11、表 F 8/8、表 G 17 行。

5. 五处计数一次读齐
   注册 93、server config 166、client config 3、语言键 323 × 2、任务 37。
   五个数写在同一段里，缺一个就停下；任何一个与 README 的版本行不一致，先改 README 再宣布可发布。

6. 红线只扫三条
   grep -rn "@Mixin\|mixins.json" src/main/java 为 0 处；
   grep -rln "CustomPacketPayload" src/main/java 恰 1 个文件（network/CoreListPayload.java），且 grep -rn "playToServer\|sendToServer" 为 0；
   grep -rn "GameRules.register" src/main/java 为 0（工程约束保留，不属于两条红线）。
   注意：网络包红线已整体废除（D-146），不许再把「0 个自定义网络包」写成通过条件，也不许写成「本版允许 1 个」。

7. 12 份文档与 README 的关系
   本版 12 份 = 中文 6 份（28-DECISIONS / 29-DESIGN / 30-TECH / 31-TASKS / 32-PROMPT-MAIN / 33-PROMPT-TASKS）
   + 英文 6 份（同号译本，中文冻结后统一翻译；docs/en 已有 28 ~ 33 六份同号译本）。
   README 不计入这 12 份：它是仓库门面与索引，本版要改三处 —— ① 红线段改成「只留无 Mixin、网络包限制已整体废除」；
   ② 升级须知（旧 toml 里 pumpkin_turret 的两个默认值不生效、/farmstorm farmlandlimit 已删、老档首次启动会补建）；
   ③ 版本表与 dist 条目（0.1.1 的字节数与 SHA256）。
   12 份里，28 号是唯一真相源，31 号是任务号与验收归属的唯一来源；本文件（33 号）与 32 号都不重抄判据。

8. 回报
   按 32 号主提示词的回报格式输出，另外追加四张小表：资源清点表、五处计数表、77 条判据表、37 行任务核对表。
   不要贴完整日志（每条命令只贴结论行与第一条错误）；任何没跑过或没验证的项一律写「未验证」，不得写成「已通过」。

9. 不许做的事
   不改 28 / 29 / 30 / 31 的任何一条判据；不改 docs/zh/00~32 与 docs/en；不为了让某条变绿去改实现；
   不新增任何 key / PNG / JSON / 注册对象；不把 A-49（耕地跨维度同坐标撞车）写成通过项或缺陷。
```

收尾时的三条判据（人眼复核，不看代理的自我陈述）：

1. **四段 + 退出码**：`93 / 93`、config **166**、lang **323 键**、`结论：FAIL 0 项`、退出码 0，五样同时成立；工具里没有 59 / 165 / 321 这类旧期望值。
2. **77 = 38 + 39**：两份核对表行数分别是 38 与 39，逐条有读数、未通过 0；A-49 只作为已知限制出现一次；37 行任务核对表「未达成」为 0。
3. **产物可复核**：`dist/farmstorm-0.1.1+mc1.21.1.jar` 与 `-sources.jar` 在位且字节数 / SHA256 已记录；`jar tf` 能数到那 1 张 PNG 与那 3 个 class；纯服务端能起来。三者缺一不许宣布可发布。

---

## 8. 跨 Phase 的注意事项（不放进任何单个 Phase 的那些）

共 **8 条**。每条都写死「什么时候生效、违反了会怎样」。

1. **A-40 的中间态窗口（F-177 ~ F-192）**：`tools/verify_project.py` 从 F-177 落地起对 v0.1.1 **必然是红的**，红的方式有且只有两条键级差异（F-182 之前：缺 `pumpkin_turret.bulletSpeed`、多 `farmland.limitPerPlayer`；F-182 ~ F-191：只多 `farmland.limitPerPlayer`）。窗口内**三条禁令**：不改 `docs/` 让期望集对齐、不删 `run/config/farmstorm-server.toml`、不把 `FAIL 0` 写进任何验收记录。窗口只在 **F-192** 关闭（删 `[farmland]` 段 + 删旧 toml 重新生成之后），从那一点起 `FAIL 0` 才是判据。§1 的出口判据与 §6 的出口判据是这条规则的两端，中间四节的节内判据都绕开工具。
2. **两处 schema 变更的先后（F-188 与 F-189）**：两处都无前置、互不共享文件（`farmland/FarmlandLedger.java` 对 `outpost/OutpostCoreIndex.java`）、落在两个不同的 `.dat`（`farmstorm_farmland.dat` 与 `farmstorm_outpost_core.dat`），**可以并行、也可以互换顺序**；但两者都必须在 **F-190（补建）、F-193（选核）、F-196（迁移）** 之前落地——补建读 `orphans`（F-188 的字段）并用 `gameTime` 破平手（F-189 的字段），选核要用三级键，迁移要用 `bindingsOf`。**两次升版不许合并成一个提交**：它们的回滚面不同（v2 账本回滚会丢绑定但不铲世界；索引回滚只让三级键退化成两级），合并后无法单独回滚。
3. **浮窗线（F-199 起）与绑定制线（F-188 起）可以并行——结论：可以，但有四个交汇点**。① F-187（`isAlly` 重载）是 F-200 与 F-191 / F-193 / F-195 的公共前置，必须最先落地；② F-189（`gameTime` 载体）是 F-200 与 F-193 / F-196 的公共前置；③ `blockentity/OutpostCoreBlockEntity.java:95-103` 与 `:166-170` 同时被 F-198（ticket 段）与 F-200（广播段）改，必须按「一个任务只追加自己那一段」的约定串行，不能两节同时开工；④ `event/FarmstormGameEvents.java` 同时被 F-190（启动闸）、F-194（写绑定）、F-197（删 3 处注册）、F-198（停服 + 补票）、F-200（+4 个玩家事件 + `ServerTickEvent.Post`）改，五处相加不许有任何一处被另一处覆盖。除这四点外，两条线可分别派给两个人。
4. **三处同文件串行（跨 Phase）**：`tools/gen_textures.py`（F-178 → F-179）、`entity/TurretBullet.java`（F-183 → F-184）、`datagen/ModLangProvider.java`（F-182 / F-192 / F-197 / F-201 / F-202 / F-203 / F-205 七处增量，**最终一致性只在 F-206 收口**）。语言键这一处要特别注意：任何一节结束时看到的键数只是区间读数（315 → 316 → 315 → 314 → 317 → 319 → 320 → 323），**不许**在中途把 323 写进判据。
5. **回滚链有三对**：`F-182 → F-183 → F-184`（删常量后两处读点必须同源，回滚 F-182 会让后两者编译失败）；`F-185 → F-186`（回滚 F-185 后 F-186 失去意义但不编译失败）；`F-191 ↔ F-197`（`setBase` 已删，回滚 F-197 必须同时回滚 F-191，否则编译失败）。三对都要在提交信息里写明「成对回滚」。
6. **两个数字的「总数只在终点成立」**：语言键 **323** 与 config 组数 **58**。中间量分别是 315 / 316 / 314 / 317 / 319 / 320 与 59，它们各自只在某一节成立，不许跨节引用；总数只在 F-206（语言键）与 F-192（组数）之后才成立。反过来，**注册对象 93 与 server config 键数 166 全程不变**——166 的算式是 `166 − 1 + 1`，总数持平但组成变了，所以任何一节都不许写「166 没动过所以不用核」。
7. **38 条与 39 条不可互相替代（A-46）**：`30` 号 §7 的 38 条是技术自检，`29` 号 §11 的 39 条是设计边界；13 条完全对上、5 条部分覆盖、**21 条没有任何对应断言**（B-01、B-02、B-03、B-05、B-08、B-10、B-11、B-13、B-14、B-15、B-17、B-18、B-19、B-20、B-21、B-24、B-25、B-26、B-31、B-32、B-35），它们唯一的判据是 F-210 / F-211（F-210 承担其中 **14** 条、F-211 承担 **7** 条，14 + 7 = 21）。另 **11 条验收没有对应边界**（#1、#2、#3、#5、#6、#11、#13、#14、#16、#37、#38），属工程面。两批记录都写「未通过 0」，但谁都不能替谁签字。**A-49 不在 39 条里**，只作已知限制登记。另有一条排期上的硬约束：五个验收任务的依赖列已由 **A-54** 重算并回灌 31 号（F-207 / F-208 / F-209 / F-210 / F-211 各按新列执行，本文件 §6.3 已同步），但执行口径仍是「§2 / §4 / §5 的出口判据全部跑完，才开 §6」，而不是「按依赖列能开工就开工」。
8. **红线口径在本版变了三处，跨节都要按新口径写**：`CustomPacketPayload` 从「0 命中」变成「恰 1 个文件」（F-199 之前是 0，F-199 之后是 1，§6 的红线扫描按 1 写）；`@Mixin` / `mixins.json` 仍是 **0**（唯一保留的红线）；`GameRules.register` 仍是 **0**（工程约束，不属于两条红线）。§2 / §3 / §4 的节内扫描都不许再把「0 个自定义网络包」写成通过条件。

---

## 9. 与 31 号任务书的一致性自检

| Phase | `31-TASKS` 的任务区间 | `31-TASKS` 的任务数 | 本文件 §x.3 的任务数 | 是否相等 |
|---|---|---|---|---|
| 26 基线与工具链 | F-176 ~ F-177 | 2 | 2 | 相等 |
| 27 材质、数值与方块属性 | F-178 ~ F-184 | 7 | 7 | 相等 |
| 28 朋友的韭持续场 | F-185 ~ F-186 | 2 | 2 | 相等 |
| 29 耕地-核心绑定制 | F-187 ~ F-198 | 12 | 12 | 相等 |
| 30 浮窗与数据面 | F-199 ~ F-205 | 7 | 7 | 相等 |
| 31 收口与全量验收 | F-206 ~ F-212 | 7 | 7 | 相等 |
| **合计** | **F-176 ~ F-212** | **37** | **37** | **相等（差集为空，`212 − 176 + 1 = 37`）** |

其余四项自检：

| 检查项 | 方法 | 结论 |
|---|---|---|
| 任务编号与标题 | 从 `31-TASKS-0.1.1.md` 抽 37 个 `#### F-xx · 标题`，与本文件的「任务号 + 标题」逐行比对 | 37 / 37 逐字一致（编号连续、无跳号、无重号） |
| 复杂度与人日 | 逐 Phase 校对 S / M / L 与折算人日，再与 `31` 号 §4.1 的小计相加 | `2.0 + 11.5 + 4.5 + 21.5 + 15.0 + 11.0 = 65.5`；S 5 / M 22 / L 10；含 25 % 缓冲 81.875 |
| 依赖列 | 与每个任务块的「依赖」字段逐行比对（按 **A-54** / **A-56** 回灌后的新列） | 37 / 37 一致；其中 F-207 / F-208 / F-209 / F-210 / F-211 五条按 A-54 的新列写；**F-192 依赖 F-191**（A-56）；两条串行链（`F-191 → F-192 → F-197`、`F-193 → F-194 → F-196`）已在 §4.1 点名，前者依据的是**编译顺序**（A-56），不是风格顺序 |
| 裁决引用 | 本文件只引用编号（A-30 ~ A-56、D-124 ~ D-147、RT-31 ~ RT-41），不复制 31 号 §5 的裁决正文 | 符合单一来源约定；A-44 作废、由 A-47 取代这一条按 31 号的现状写 |

> 备忘（A-56）：「本条最初被误读为『F-192 无依赖』，源于变更清单的三对三压缩写法；已按 A-56 复原」——那份清单把三对写成了 `F-179` / `F-188` / `F-192` → `F-178` / `—` / `F-191`，其中的 `—` 对应的是 **F-188** 那一对（原文「—（与 F-187 互不依赖）」），**F-192 那一对**是「`F-191`（`base()` 已无读者）」→ **`F-191`**；31 号里 F-192 的依赖从头到尾都是 `F-191`，不要再照压缩写法改回 `—`。

**五处口径不一致的登记**（本文件按「以 31 号正文为准、不放大、不改号」处理；五处**全部已由 A-54 回灌 31 号并修正**，本文件已同步，见末列状态）：

| # | 位置 | 不一致（A-54 前的原文） | 本文件的处理 | 状态 |
|---|---|---|---|---|
| 1 | `31` 号 §1 表 vs §2 详情 | F-208 的依赖列两处写法不同：§1 写「F-179、F-180 ~ F-190、F-192、F-196」（含 F-187），§2 逐条列举时**不含** F-187；两处都**不含** F-191 / F-193 / F-194（而 #16 ~ #18 正是这三条任务的产物），也都不含 §5 的 F-199 ~ F-205（而 #6 ~ #15 是浮窗段） | 本文件 §6.3 照录 §1 表的依赖，不补号；缺口与处理口径写在 §8 第 7 条末尾与本节 | **已修（A-54）** |
| 2 | `31` 号 F-207 / F-209 / F-211 的依赖 | F-207 的验收第 4 条要读 `chunks.dat`（那是 F-198 的面），依赖列却只有 F-206；F-209 覆盖 #20 ~ #38，但依赖列不含 F-179（#23）、F-180（#24 ~ #28）、F-181（#33）；F-211 覆盖 B-21 ~ B-39，但依赖列不含 F-182（B-28）、F-190（B-23 / B-24 / B-25） | 同上：照录不改；缺口在 §8 第 7 条末尾点名，执行口径是「§2 / §4 / §5 全部跑完再开 §6」 | **已修（A-54）** |
| 3 | `31` 号 F-210 与 F-211 的正文 | 两处分别写「其中 **12** 条在 30 号 §7 没有对应条目」与「其中 **6** 条」，相加 **18**；而 A-46 与表 G 第 17 行列的是 **21** 条，差的三条是 B-19、B-20（属 F-210 段）与 B-21（属 F-211 段），两处正文都没有点名。A-54 已改为「其中 **14** 条」与「其中 **7** 条」，合计 **21**（B-19 / B-20 / B-21 补点名） | 本文件按 A-46 / 表 G 的 **21** 条写（§8 第 7 条逐个点名，含 B-19 / B-20 / B-21），不按 12 + 6 的枚举 | **已修（A-54）** |
| 4 | `31` 号表 A 行 23 / 24 的归属 | `orphanCount` 在 F-188 的新增方法清单里，表 A 行 23 却记在 F-190 名下；`FarmlandGuard` 行 24 记「F-193（判定）、F-194（写入口）」，而 F-194 的写入口实际在 `event/FarmstormGameEvents.java:206` | 本文件按任务详情写（§4.3 的 F-188 含 `orphanCount`、F-194 的交付物写「锄地闸的 `ALLOW` 分支」），不按表 A 改写任务归属 | **已修（A-54）** |
| 5 | `31` 号 F-185 的标题 | §1 表写「朋友的韭改成 `effectTicks` 刻的场」，§2 详情写「朋友的韭：从落地一次性改为 `effectTicks` 刻的场」 | 取 §2 详情的标题（信息更全，且与本文件的出口判据「从一次性改成持续场」一致）；短版只在本行的引文里出现过一次，正文 §3.3 用的是长版 | **已修（A-54）** |

> 以上 5 项已于 **A-54** 回灌 31 号并修正，本文件已同步；表内「处理」列保留原判断，状态为**已修**。

维护约定：若 `31-TASKS-0.1.1.md` 修订了某个任务的标题、依赖或人日，本节与 §1 ~ §6 的六张任务表要重跑一次比对；本文件不自行改标题、不自行改编号、不自行加任务，也不改 65.5 人日与 15.5 人日关键路径这两个数。
