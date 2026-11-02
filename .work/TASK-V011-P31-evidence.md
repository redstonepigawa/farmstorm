# TASK-V011-P31-evidence · Phase 31（F-206 ~ F-212）+ 收尾交付 · 全量证据

> 执行者：Phase 31 唯一执行者（子代理）。工作目录 `D:\project_farm`（Windows / pwsh）。
> 铁律遵守情况：**F-206 / F-207 改了文件；F-208 ~ F-212 一行代码没动**（只产出 `.work/v011-verify/` 下的记录）。
> **未做任何 git 写操作**（只用了 `git status` / `git diff` / `git log` / `git check-ignore` / `git grep`）。
> 分册：`F-206.md` / `F-207.md` / `F-208.md` / `F-209.md` / `F-210.md` / `F-211.md` / `F-212.md`（同目录）。

## 0. 一句话结论

| 项 | 结论 |
|---|---|
| §6 九条验收 | **8 条成立**；**1 条不成立**：B-39 的「重跑生成器后其余 24 张 sha256 不变」被实测证伪（D-03） |
| 77 条判据 | 【实测】20 / 【代码层证据】2 / 【未验证】54 / **不通过 1**（B-39） |
| 37 个任务 | 全部有状态，**未达成 0**（B-39 的不通过记在 F-211 的验收条目里，不是任务未完成） |
| 五处计数 | 93 / 166 / 3 / 323 × 2 / 37 —— 一次读齐 |
| 红线 | `@Mixin\|mixins.json` **0**；`CustomPacketPayload` 恰 **1 文件**；`GameRules.register` **0** |

---

## 1. `git diff --stat` 原文（回报第 1 项）

```
 .work/TASK-V011-P27.md                             |   3 +-
 .work/p27-git-baseline.txt                         | Bin 0 -> 84 bytes
 README.md                                          |  81 ++++++++++++++++++++-
 .../dashpig/farmstorm/datagen/ModLangProvider.java |   7 +-
 4 files changed, 84 insertions(+), 7 deletions(-)
```

说明：

- `.work/TASK-V011-P27.md` 与 `.work/p27-git-baseline.txt` 是**开工前就脏的**（P27 遗留），本次没碰。
- `README.md` 从我接手时的 **59 行**（56+/3−）变成 **81 行**（78+/3−）：**原样保留**，只做两处定点插入（升级须知 + 实测口径），没有整篇重写。
- `ModLangProvider.java` **只改了类注释的键数叙述**（4 行），没有任何 `.translation(…)` 增删。
- `dist/` 与 `build/` 都在 `.gitignore`（`.gitignore:8` `dist/`、`:2` `build/`），所以两支新 jar 与两份安装说明的更新**不进 git 面**——这是仓库既有约定，不是漏提交。
- `docs/` 改动 **0 行**（`git status --porcelain docs` 为空）。

---

## 2. `verify_project.py` 四段完整原文 + 退出码（回报第 2 项）

命令：`$env:PYTHONIOENCODING='utf-8'; & "<python>" tools\verify_project.py`
（原始输出另存 `.work/v011-verify/F-212-verify.txt`）

```
== 1. config：文档键集 vs 生成的 toml ==
  30-TECH §4.1：58 组 / 166 键（v0.1.1 的 58 行数据表现算，其中 165 个键名已在前四份清单里）
  剔除已删键 1 个：farmland.limitPerPlayer
  文档口径键数: 166（…）
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
```

**退出码 = 0**。四段齐全（config / 注册面 / lang / 禁令）+ 结论行 + 退出码，五样同时成立。
工具里**没有** 59 / 165 / 321 这类旧期望值：`59` 已随 `[farmland]` 整组删除变成 58，`165` 只作为「中间量」出现在 §4.1 的说明文字里，`321` 全文 0 次。

---

## 3. 构建、`jar tf` 三组条目、两支 jar 的字节数 + SHA256（回报第 3 项）

### 3.1 构建

命令：`cmd /c ""D:\project_farm\.tools\gw.cmd" build"`

```
> Task :compileJava
> Task :jar
> Task :sourcesJar
> Task :build

BUILD SUCCESSFUL in 10s
5 actionable tasks: 3 executed, 2 up-to-date
EXITCODE=0
```

### 3.2 两支 jar

| 文件 | 字节数 | SHA256 |
|---|---|---|
| `dist/farmstorm-0.1.1+mc1.21.1.jar` | **1,063,181** | `B20E2EA305AE2F1943756696C3DE7090965FD31D525ECC8F370B2F3CF746F2A5` |
| `dist/farmstorm-0.1.1+mc1.21.1-sources.jar` | **978,402** | `0E9A03AC20945CB89C495C047B926E9ECBE1C5AF9D5C906023450675886BDECD` |

`build/libs` 的同名两份与上表**字节数和 SHA256 完全相同**；`build/libs` 已清到只剩这两支（删掉 0.0.2 / 0.0.3 / 0.0.4 共 6 个产物）。

### 3.3 `jar tf` 三组条目（原文见 `.work/v011-verify/F-207-jar.txt`）

**第一组 · 一张替换 PNG + 无新增资源**

```
assets/farmstorm/textures/item/wild_apple.png   0.1.1=1  0.1.0=1   （64×64 / 6,946 B / sha256 0edf673a…1d05cb7f9）
png 0.1.1/0.1.0: 113 / 113      added png: []
json 0.1.1/0.1.0: 224 / 224     added json: []
ogg 0.1.1/0.1.0: 3 / 3          added ogg: []
```

**第二组 · 三个新类各 1 条**（真实路径带 `com/dashpig/` 前缀，见偏差 D-01）

```
com/dashpig/farmstorm/network/CoreListPayload.class          0.1.1=1  0.1.0=0
com/dashpig/farmstorm/client/CoreHudRenderer.class           0.1.1=1  0.1.0=0
com/dashpig/farmstorm/outpost/CoreChunkTickets.class         0.1.1=1  0.1.0=0
com/dashpig/farmstorm/client/FarmstormKeys.class             0.1.1=1  0.1.0=0
com/dashpig/farmstorm/client/CoreHudDragScreen.class         0.1.1=1  0.1.0=0
com/dashpig/farmstorm/client/CoreHudClientCommands.class     0.1.1=1  0.1.0=0
com/dashpig/farmstorm/client/ClientCoreList.class            0.1.1=1  0.1.0=0
com/dashpig/farmstorm/config/FarmstormClientConfig.class     0.1.1=1  0.1.0=0
com/dashpig/farmstorm/command/FarmstormCommands.class        0.1.1=0  0.1.0=1   ← 指令树已删，要求 0
```

**第三组 · 版本行**（`META-INF/neoforge.mods.toml` 原文节选）

```toml
[[mods]]
modId = "farmstorm"
version = "0.1.1"
displayName = "Farmstorm"
authors = "dashpig"
```

条目集合 diff（0.1.1 − 0.1.0）：**新增 18 条**全是 `.class` 或目录、**移除 2 条**（`command/` 与 `FarmstormCommands.class`）——没有任何新增 PNG / JSON / OGG。

---

## 4. 两份 lang 的键数与 15 项前缀分布（回报第 4 项）

脚本：`.work/v011-verify/tools/lang_count.py`（对 `src/generated/resources/assets/farmstorm/lang/{zh_cn,en_us}.json`）

```
zh_cn.json keys= 323
  dist: 169 + 36 + 32 + 26 + 16 + 13 + 9 + 6 + 4 + 3 + 2 + 2 + 3 + 1 + 1 = 323
en_us.json keys= 323
  dist: 169 + 36 + 32 + 26 + 16 + 13 + 9 + 6 + 4 + 3 + 2 + 2 + 3 + 1 + 1 = 323
set(zh)==set(en): True     zh-only: []     en-only: []     OTHER keys: []
farmlandlimit: 0 / 0
```

15 项逐项：`farmstorm.configuration 169 / advancements.farmstorm 36 / item.farmstorm 32 / block.farmstorm 26 / tooltip.farmstorm 16 / entity.farmstorm 13 / gui.farmstorm 9 / death.attack 6 / effect.farmstorm 4 / subtitles.farmstorm 3 / key.farmstorm 2 / message.farmstorm 2 / command.farmstorm 3 / container.farmstorm 1 / itemGroup.farmstorm 1`。
`grep -c '"farmstorm.configuration\.'` = **169**；`grep -c "299 + 16 = 315"` = **0**。

---

## 5. 77 条逐条结论表（回报第 5 项，两条表）

全文在四个分册，逐行都带编号与证据位置：

| 表 | 文件 | 行数 | 【实测】 | 【代码层证据】 | 【未验证】 | 不通过 |
|---|---|---|---|---|---|---|
| 38 条前半 #1~#19 | `.work/v011-verify/F-208.md` | 19 | 8 | 0 | 11 | 0 |
| 38 条后半 #20~#38 | `.work/v011-verify/F-209.md` | 19 | 5 | 1 | 13 | 0 |
| 39 条前半 B-01~B-20 | `.work/v011-verify/F-210.md` | 20 | 2 | 0 | 18 | 0 |
| 39 条后半 B-21~B-39 | `.work/v011-verify/F-211.md` | 19 | 5 | 1 | 12 | **1** |
| **合计** | — | **77** | **20** | **2** | **54** | **1** |

索引（编号 → 结论）见 `F-212.md` §1.2 与 §1.3。

**A-46 核对**：F-210 承担 14 条「§7 无对应断言」的边界（B-01、B-02、B-03、B-05、B-08、B-10、B-11、B-13、B-14、B-15、B-17、B-18、B-19、B-20）；F-211 承担 7 条（B-21、B-24、B-25、B-26、B-31、B-32、B-35）；14 + 7 = **21**，与 31 号表 G-17 的名单逐一对上。**38 条与 39 条没有互相替代。**

**A-37 核对**：全部 77 条里 **没有**「调用点数 = 1」这类断言；写作入口一律换成「NBT 里的条数」（例如「`bindings` 该核 16 条」）。
**A-52 核对**：toml 段数用 `^\s*\[`（58 段；`^\[` 只有 34 段）。
**A-57 核对**：`SoundType.GRASS` / `lightLevel` 的 grep 全部带 `src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java` 完整路径执行（读数 0 / 1）。
**A-58 核对**：本次不判 `man瓜` 首次出现形态（那是文档面判据，不属 77 条），未新增混用。
**A-49 核对**：耕地跨维度撞车在 `F-210.md` 与 `F-211.md` **各只列一行**「已知限制，本版不修」，不设通过项、不写成缺陷。

---

## 6. 37 行任务核对表（回报第 6 项）

见 `F-212.md` §二（任务号 / 标题 / 是否达成 / 证据位置）。汇总：

- 37 行全部有状态；**未达成 0**。
- F-176 ~ F-205 的达成证据是各自的提交（`5500295` / `11b8635` / `11329ca` / `fd5ad80` / `baedaad`）＋本次复测到的读数。
- F-206 / F-207 的达成证据是本次的两个产物面（lang 323 × 2、两支 jar）。
- F-208 ~ F-212 的达成证据就是四份验收记录 + 本文件。
- **54 条【未验证】不改变任务状态**：那是环境限制（本机无可渲染 / 可出声客户端），已逐条点名。

---

## 7. 七张表的覆盖数 + 五处计数 + 红线三计数（回报第 7 项）

### 7.1 七张表

| 表 | 应覆盖 | 复核 | 结论 |
|---|---|---|---|
| A · 改动面 | **44/44** + 补记 2 行 | 1~44 行逐行有负责任务；补记 1 = F-192、补记 2 = F-188 / F-191；动作面 47 = 44 + 3 | 成立（行数写法三处不一致，见 D-06） |
| B · 注册与 schema | **20 项** | 12（注册面逐类 +0）+ 2（两处 schema）+ 6（不进注册面的构件） | 成立 |
| C · config | **7 项** | 改 2 + 增 1 + 删 1 + client 3 | 成立 |
| D · 资源 | **5 项** | 1 张 PNG + 生成器 3 处 + 2 份 lang；新增 PNG/JSON 均 0 | 成立 |
| E · 风险 | **11/11** | RT-31 ~ RT-41 每条都有负责任务；本次实测覆盖 RT-33 / RT-38 / RT-39 / RT-40 / RT-41 | 成立 |
| F · 待验证 API | **8/8** | ①~⑧ 各有任务 + 步骤 + 退路；⑤③ 的两条分支本次真实出现 | 成立 |
| G · 上游一致性 | **17 行** | G-1 ~ G-17；真分歧 6（全部已裁）/ 已裁决 6 / 记法差异 2 / 单向漏项 3 | 成立 |

### 7.2 五处计数（一段读齐）

```
注册对象      93            （启动日志 + 工具 93 / 93）
server config 166 键 / 58 组（run/config/farmstorm-server.toml）
client config 3 键 / 1 组  （run-p30/config/farmstorm-client.toml：enabled/x/y）
语言键        323 × 2       （两份键集逐字相同）
任务          37            （F-176 ~ F-212）
```

### 7.3 红线三计数

```
grep -rn "@Mixin\|mixins.json" src/main/java          → 0 处
grep -rln "CustomPacketPayload" src/main/java          → 1 个文件
        （src/main/java/com/dashpig/farmstorm/network/CoreListPayload.java）
grep -rn "GameRules.register" src/main/java            → 0 处
（另：src/main/resources 下 *.mixins.json = 0；playToServer|sendToServer = 0）
```

---

## 8. `README` 三处 diff 与两份安装说明的版本行（回报第 8 项）

### 8.1 README（原文全文见 `.work/v011-verify/README-diff.txt`）

| 三处要求（33 号 §7 第 7 条第 ②③ 项 + 28 号 §4.5） | 现状 |
|---|---|
| ① 红线段改成「只留无 Mixin；网络包限制已整体废除」 | **接手时已具备**（README:190-192「红线口径在本版变更（本节是全仓唯一现行口径）…整体废除…不是放宽为 1 个、不是本版例外」），本次**未改** |
| ② 升级须知 | **本次新增** `### v0.1.1 升级须知（从 0.1.0 升级必读）`：旧 toml 两个默认值不生效（60 / 1，附实测证据行）、`bulletSpeed` 自动补 0.45、`/farmstorm farmlandlimit` 已删（只剩客户端 `hud`）、老档首次启动补建（16/4 日志行）、想用新默认值就删 toml |
| ③ 版本表与 dist 条目（0.1.1 的字节数与 SHA256） | **本次新增** `### v0.1.1 实测口径（发布件）`：两支 jar 的字节数 + SHA256、`dist` 与 `build/libs` 同文件、`version="0.1.1"`、工具四读数、资源 113 PNG / 新增 0、三条红线 |

新增的 22 行 diff（节选；**原有 56 行未提交改动原样保留**）：

```diff
+### v0.1.1 升级须知（从 0.1.0 升级必读）
+| 项 | 行为 | 证据 |
+| 旧 `farmstorm-server.toml` 里 `pumpkin_turret` 的**两个默认值不生效** | … `fireIntervalTicks` 仍是 **60**、`projectileDamage` 仍是 **1** … | 实测（`.work/v011-verify/p31-run4.log`） |
+| 新键 `pumpkin_turret.bulletSpeed` **自动补** | … `bulletSpeed = 0.45` | 同上 |
+| `/farmstorm farmlandlimit` **已删** | … 只剩客户端 `/farmstorm hud <on|off|reset>` | 实测：`Unknown or incomplete command` |
+| 老档**首次启动会补建** | … 容量 16 是硬顶，补不上的保持耕地、不受保护 | 实测：绑定 16 / 保持未绑定 4，两次相同 |
+### v0.1.1 实测口径（发布件）
+| 发布件（主） | `dist/farmstorm-0.1.1+mc1.21.1.jar` **1,063,181 B** / SHA256 `B20E2EA3…F746F2A5` | …
+| 发布件（源码） | `dist/farmstorm-0.1.1+mc1.21.1-sources.jar` **978,402 B** / SHA256 `0E9A03AC…886BDECD` | …
```

### 8.2 两份安装说明（`dist/安装说明.txt` 与 `dist/install-notes.txt`，各 12,280 B，**SHA256 相同**、UTF-8 无 BOM）

```
Farmstorm 农场风云  v0.1.1  —— 安装说明
  【从 v0.1.0 升级必看】          ← 新增（四点：旧默认值不生效 / bulletSpeed 自动补 / 指令已删 / 老档补建 + 存档升版提示）
  【本版改了什么（相对 v0.1.0）】  ← 新增（9 条）
  【已知限制（请照实了解）】      ← 追加 5 条（浮窗观感 6 项、包未下发、A-49 跨维度撞车、五类听感、F3 光照）
【文件校验（v0.1.1）】
  文件名 : farmstorm-0.1.1+mc1.21.1.jar
  大小   : 1,063,181 字节
  SHA256 : B20E2EA305AE2F1943756696C3DE7090965FD31D525ECC8F370B2F3CF746F2A5
  文件名 : farmstorm-0.1.1+mc1.21.1-sources.jar （源码包，不装）
  大小   : 978,402 字节
  SHA256 : 0E9A03AC20945CB89C495C047B926E9ECBE1C5AF9D5C906023450675886BDECD
```

---

## 9. 文档-源码偏差表 与 未验证清单（回报第 9 项）

偏差表全文见 `F-212.md` §六（D-01 ~ D-09）。摘要：

| 编号 | 一句话 |
|---|---|
| D-01 | `jar tf` 的 class 条目文档写 `farmstorm/…`，实际是 `com/dashpig/farmstorm/…` |
| D-02 | HC-78 / N-85 要求 `limitPerPlayer` 的 grep 为 0，但 `src` 下 1 处注释、`tools` 下 1 处（A-40 的剔除清单本身要求它出现） |
| **D-03** | **B-39「其余 24 张 sha256 不变」不成立**（重跑会剥掉 24 张的「+」）→ 本 Phase 唯一不通过 |
| D-04 | `ModLangProvider` 沿革段仍含 299 / 314 / 315 / 316 阶段值（带版本标签，故意保留） |
| D-05 | 30 号 §7 #23 给的生成器命令缺 `--vanilla-assets` / `--out` 两个必填参数 |
| D-06 | 表 A 补记行数在 31 号（+2 / +3）与 33 号（+1）之间不一致 |
| D-07 | #12 的「未知的指令」中文原文在专用服务器控制台实测是英文 `Unknown or incomplete command` |
| D-08 | 33 号说 `build/libs`「可能有」旧 jar，实测有 6 个 0.0.2~0.0.4 产物（已清） |
| D-09 | #7 / #8 断言的「核心列表下发」在只有原版协议客户端时永远不出现，实现里走的是「组装…跳过发送」分支 |

未验证清单全文见 `F-212.md` §七。**54 条**按「缺什么条件」分成 10 组（图形客户端 / 出声客户端 / F3 与刷怪 / 真实投掷 / 开火观测 / 两账号或两服 / 改 config 重启 / 离线改存档重启两次 / 特定两核构造 / 结构性不可达）。

---

## 10. 未关闭的问题（回报第 10 项，含 A-49 一行）

1. **B-39 不通过（D-03）**：`gen_textures.py` 重跑后 25 个 PNG 的 sha256 改变（24 张作物图标 + 压缩干草块侧面），因为 F-178 已把叠「+」机制整体删除。**要么改判据（B-39 第三子句改成「重跑会剥掉 +，属预期」），要么回 28 号登记「有意保留 24 张旧图」**。本 Phase 按铁律不改判据、不改实现。
2. **D-02 / D-07** 两处判据与环境口径冲突，需要上游改口径才能变成可判定。
3. **A-49 已知限制，本版不修**：耕地账本落盘只有 `pos`、无维度字段；跨维度同坐标的两块耕地会互相借到踩踏保护、并在额度里被算成一块。只在两份边界记录末尾各列一行，不设通过项、不写成缺陷。
4. **54 条【未验证】**：本机没有可渲染 / 可出声的图形客户端，这些条目**没测**，不按通过计。
5. `run-p31` 目录留着（`run-*` 在 `.gitignore`），可复核四次起服的日志、存档与 `chunks.dat` 的"不存在"这一事实。

---

## 11. 本次落盘的文件清单

| 路径 | 内容 |
|---|---|
| `.work/v011-verify/F-206.md` … `F-212.md` | 七份分册记录 |
| `.work/v011-verify/F-212-verify.txt` | 工具四段原文 |
| `.work/v011-verify/F-207-jar.txt` | 两支 jar 的条目 diff + `neoforge.mods.toml` + `wild_apple.png` 尺寸/sha256 |
| `.work/v011-verify/F-207-png-compare.txt` | 重跑生成器产物 vs 仓库产物的 sha256 逐项比对 |
| `.work/v011-verify/F-211-B38-pixdiff.txt` | 24 张图标的「+」绿像素逐像素比对 |
| `.work/v011-verify/F-207-gen_textures.log` | 生成器重跑输出 |
| `.work/v011-verify/p31-run1.log` … `p31-run4.log` | 四次起服的 server log |
| `.work/v011-verify/p31-bot-ALICE.log` | 测试机器人的动作栏 / 交互读数 |
| `.work/v011-verify/farmland-bindings.txt` / `run2-bindings.txt` | 绑定表 NBT 解码结果 |
| `.work/v011-verify/fixtures/` | 0.1.0 形态的 `farmstorm_farmland.dat`（20 块老田）与 `farmstorm_outpost_core.dat`（1 个核） |
| `.work/v011-verify/png-before.txt` / `png-backup/` | 24 + 5 张 PNG 的 sha256 清单与原件备份 |
| `.work/v011-verify/gen-out/` | 重跑生成器的 99 个产物 |
| `.work/v011-verify/tools/` | 本次用到的 6 个脚本（`lang_count.py` / `jar_compare.py` / `dump_bindings.py` / `mk_fixture.py` / `cmp_png.py` / `png_pixdiff.py`） |
| `.work/v011-verify/README-diff.txt` / `git-diff-stat.txt` | 取证用的 diff 原文 |
| `run-p31/` | 四次起服的运行目录（`.gitignore` 内） |

**截图**：`.work/v011-verify/B-xx.png` **0 张**——本机没有可渲染的图形客户端，凡需要截图的条目一律按【未验证】登记，**没有伪造任何图片**。
