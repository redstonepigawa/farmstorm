# v0.2.0 Phase 39 · 红线、注册面与上游一致性签字

> 签字者：Phase 39 唯一执行者（F-246）。前置：F-243 已同步工具、F-244 / F-245 两份记录已落盘。
> 本文件只做**收口核对**，不产生新改动（F-244 ~ F-246 一律不改码）。

## 一、8 行签字表

| # | 判据 | 实测值 | 结论 |
|---|---|---|---|
| ① | 注册面 **97**（12 类分项） | 启动日志 `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`；`Farmstorm.java:169-172` 断言 `total == 97 && items == 33 && blocks == 27 && blockEntities == 8 && … && menuTypes == 2`；工具 `已核 registry 对象: 97 / 97` | 签字通过 |
| ② | config **175 键 / 60 组** | `run/config/farmstorm-server.toml`：`^\s*\[` 60 段（顶层 36 + 嵌套 24）、`^\s*[A-Za-z_].*=` 175 键；36 个组逐组键数与 36 号 §4.1 全对（mismatch 0）；`[outpost_core]` 4 键无 `farmlandBonus`；`[cultivator]` 2（600 / 2）、`[trade]` 6（2/5/10/32/40/64）；`FarmstormConfig.java` 的 `b.push(` = **60**；工具 `175 个配置键与文档逐字一致（toml 175 键）` | 签字通过 |
| ③ | 语言键 **337 × 2** | `zh_cn.json` 337 / `en_us.json` 337，`zh_only 0` / `en_only 0`；逐前缀 `farmstorm.configuration 178` / `item 33` / `block 27` / `container 2` / `tooltip 20` / `message 0`，其余 9 桶 36/13/9/6/4/3/3/2/1；`ModLangProvider.java` 里 `323` 命中 0、`337` 命中 1；工具 `lang 键集一致，共 337 键` | 签字通过 |
| ④ | 动作面 **46 / 46** | 36 号 §3 的 46 行在 F-244 的 36 条验收与 35 号 §12 的 40 条边界里逐行落到观察点（见 `acceptance.md` / `boundaries.md` 的行号交叉）；表外 4 项：版本号（`gradle.properties:11` = 0.2.0）、两支 `.cmd` 的 jar 名、`creativeTabItems()` 含 `CULTIVATOR`、本工具期望面（F-243） | 签字通过（承 Phase 32~38 已提交记录 + 本 Phase 复核读数） |
| ⑤ | 红线三项：`@Mixin` **0** / `playToClient` **2** / 协议串 **`2`** | `src/` 下 `@Mixin` 命中 0、`*.mixins.json` 文件 0；`FarmstormNetwork.java` 的 `playToClient` 2 处、`playToServer|sendToServer` 0；`public static final String PROTOCOL_VERSION = "2";`；`CustomPacketPayload` 实现 2 个（`CoreListPayload` / `CultivatorLinkPayload`） | 签字通过 |
| ⑥ | `Farmstorm.java` 的 **5** 个自检数字 | `total == 97` / `items == 33` / `blocks == 27` / `blockEntities == 8` / `menuTypes == 2`（`:169-172`），其余 8 类不变；窗口期已由 F-228 关闭 | 签字通过 |
| ⑦ | `farmstorm_farmland.dat` 仍在且 mtime 未变（**F-233 之后**） | 老档副本 `run-p39old/world/data/farmstorm_farmland.dat`：升级前 `mtime 2026-11-02 02:55:28` / size 274 / SHA256 `94A88F5F8054F711F014EAD94D1A0C39B0F1F783F1F75B84D330A884B4B24572`；该副本起服（`Done (…)`、老档 2 条核载入）并停服后三项**全部不变**；同目录 `neoforge_data_attachments.dat` / `raids.dat` 被重写为 `2026-11-04 11:53:12` | 签字通过 |
| ⑧ | 两个死类行数逐字未变 | `farmland/FarmlandLedger.java` **555** 行（LF 计数 555）、`farmland/FarmlandGuard.java` **225** 行（LF 计数 225）；两文件 `git status` 无改动 | 签字通过 |

## 二、34 行任务核对表（F-213 ~ F-246）

> 口径：F-213 ~ F-240 由 Phase 32~38 逐任务提交并在各自证据文件里留读数；本 Phase 只复核它们的**横切总数**（97 / 175 / 337 / 红线三项 / 两个死类）。F-241 ~ F-246 由本轮直接执行。

| 任务 | 标题 | 结论 | 读数 / 证据 |
|---|---|---|---|
| F-213 | 版本号 0.2.0 与两支脚本产物名 | 达成 | `build/libs` 产物名 0.2.0；`.work/TASK-V020-P32-evidence.md` |
| F-214 | CropTier + CropProfile 第 9 字段 + 22 条档案 | 达成 | 日志 `作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` |
| F-215 | 品质视觉：名色 + tooltip 一行 + 4 条语言键 | 达成 | `tooltip` 桶 20（16 → 20），本轮实测 |
| F-216 | 幸运浆果池 11 与断言同提交 | 达成 | 日志 `幸运浆果池 11 种…→ OK`；`Farmstorm.java:140-145` |
| F-217 | 种植路径①：`isPlantableSoil` 恒 false | 达成 | 代码层（边界 B-01） |
| F-218 | 种植路径②：`SeedItem#useOn` 返回 PASS | 达成 | 代码层（边界 B-03） |
| F-219 | 转换器 `useWithoutItem` 返回 PASS | 达成 | 代码层（边界 B-35） |
| F-220 | 耕地体系停用：5 处注释 + 键定义 + 字段保留 | 达成 | `FarmstormGameEvents` 5 处注释态；`FarmstormConfig:231` 声明仍在、`:932` 定义注释 |
| F-221 | 3 条耕地语言键停用 | 达成 | 两份 lang 里三条键 0 命中，`message.farmstorm` 归 0 |
| F-222 | `trade` 组 6 键 + 6 条 config 语言键 | 达成 | toml `[trade]` 6 键 = 2/5/10/32/40/64 |
| F-223 | 农民交易 3 项：`VillagerTradeEvents` + `TierTrade` | 达成 | 代码层（#15 未验证项已登记） |
| F-224 | 培养器注册三件套 + 创造栏 | 达成 | 注册面 items/blocks/block_entities 各 +1（33/27/8） |
| F-225 | 培养器资源、双模型与 datagen 三处 | 达成 | `blockstates` 27 / `models` 106 / `loot_table` 27 / `mcmeta` 2 / 5 张 `cultivator_a_*` PNG |
| F-226 | 核心改容器 54 格 + Items + 两个同步方法 | 达成 | `outpost_core.storageSlots = 54`；边界 B-27 掉落清单实测 |
| F-227 | 核心菜单 + 屏幕 + `menu_types` +1 | 达成 | `menu_types 2`；`EXPECTED.menu_type` 2 |
| F-228 | 注册自检 97 收口（关窗） | 达成 | 日志 `97 … menu_types 2 → OK`；`93` 在 `Farmstorm.java` 0 命中 |
| F-229 | 核心存储打开：两道闸 | 达成 | 代码层（#25 未验证项已登记） |
| F-230 | 核心破坏掉落 | 达成 | #26 真移除掉落清单实测（Core ×1 + Stone ×62 + Stone ×2 + Diamond ×3 + Puffed Wheat ×5） |
| F-231 | 核侧 16 条绑定列表 + `cultivatorCapacity` | 达成 | toml `cultivatorCapacity = 16`；核心 NBT `Cultivators` 键实测 |
| F-232 | 放置时绑定：四判据 + 两侧写入 | 达成 | 两侧 NBT 实测（`Cultivators=[…L]` ↔ `Core:…L`） |
| F-233 | 迁移三出口：`settleOnRemoval` 整段改写 | 达成 | 方法体 `setBlock` 0 / `getChunk(` 0 / `getChunkNow` 3；出口②实测（B-20） |
| F-234 | `cultivator` 组 2 键 + 2 条语言键 | 达成 | toml `[cultivator]` 2 键 = 600 / 2 |
| F-235 | 状态机、四道准入闸与放入 | 达成 | #22 / B-15 实测（Progress 600 停） |
| F-236 | 产出与核满暂停 | 达成 | #21 / #22 / B-16 实测（+2 作物 +2 种子） |
| F-237 | 培养器破坏掉落 | 达成 | B-10 实测（内部作物落地） |
| F-238 | 连线包 + 注册 + 协议版本 2 | 达成 | `playToClient` 2、`PROTOCOL_VERSION = "2"`、两行网络包自检 |
| F-239 | 连线广播器：范围与五个时机 | 达成 | `CultivatorLinkBroadcaster.requestAll` 4 处（服务端单向请求） |
| F-240 | 客户端只读与线框渲染 | 达成 | `CultivatorLinkRenderer` + `ClientCultivatorLinks`（渲染观感未验证，已登记） |
| F-241 | 语言键总账 337 与类注释 | 达成 | 337 × 2、逐前缀 178/33/27/2/20/0；`ModLangProvider:13-16` 改前→改后见证据文件 |
| F-242 | config 总账 175 键 / 60 组 | 达成 | 删旧 toml 重生成后 175 / 60，36 组逐组全对，`b.push(` = 60 |
| F-243 | 验证工具同步 97 / 175 / 337 | 达成 | 四处同改 + 活判据（28 → 退出 1；复原 → 退出 0）；四段读数见证据文件 |
| F-244 | 36 号 §8 的 36 条验收 | 达成（含未验证项，未通过 0） | `.work/v020-verify/acceptance.md`（36 行；截图 0/13 未达成，已登记） |
| F-245 | 35 号 §12 的 40 条边界复跑 | 达成（含未验证项，不符 0） | `.work/v020-verify/boundaries.md`（40 行；B-33 / B-34 按预期不判失败） |
| F-246 | 红线、注册面与上游一致性签字 | 达成 | 本文件（8 行签字表 + 34 行核对表） |

**未达成 0**（34 行逐行有结论；两处「未达成」性质的子项已在 F-244 段登记：手动场景截图 0 / 13）。

## 三、上游一致性（只读）

```
$ git diff --stat -- docs/zh/34-DECISIONS-0.2.0.md docs/zh/35-DESIGN-0.2.0.md docs/zh/36-TECH-0.2.0.md docs/zh/37-TASKS-0.2.0.md
（空）
$ git diff --stat -- docs/
（空）
$ git status --porcelain -- src tools docs
 M src/main/java/com/dashpig/farmstorm/datagen/ModLangProvider.java
 M tools/verify_project.py
```

四份上游文档（34 / 35 / 36 / 37）与 `docs/` 全树一字未改；`src/` 的唯一改动是 F-241 的 4 行类注释，`tools/` 的唯一改动是 F-243。

## 四、本文件的时点声明（A-69 / A-66）

- `.dat` 的 mtime 判据（第 ⑦ 项）在 **F-233 之后**执行：老档副本起服是 `2026-11-04 11:53` 这一次，晚于 F-233 的提交。
- `.dat` mtime 判据在本 Phase 的三份记录里**只出现 2 处**：`acceptance.md` 的 #14、本文件的第 ⑦ 项。
- 工具结论（`97 / 97` + `FAIL 0`）只出现在 F-243 之后；F-243 之前的读数（`93 / 93`、退出 0）只作为「改前实测值」登记在证据文件里。
