# 农场风云 / Farmstorm

Minecraft 1.21.1 · NeoForge 21.1.213 · Java 21 · modid `farmstorm` · v0.0.1

> 把原版农作物改造成 PvP 道具：同一种作物可以种在地里、扔出去、架在墙上、或者吃掉。
> Vanilla crops re-armed as PvP gadgets: plant them, throw them, mount them, or eat them.

---

## ✅ 当前状态：v0.0.1 已实现并通过验收

D-01 说的「文档先行、代码由提示词驱动」已经跑完一轮：**代码已按 59 个任务、10 个 Phase 全部落地**，并经作者多轮真机反馈迭代（渲染、GUI、玩法口径）。

作物分类严格按 DESIGN §2 拆成**正交两维**：`CropType` 单值（投掷型 8 / 部署型 5 / 食用型 2）+ `CropCategory` 多值（陷阱/炮台/破障/buff/建造/伤害），行为（如「能否破坏方块」）由标签驱动而不是写死在调用点。

| 项 | 状态 | 证据 |
|---|---|---|
| 中文 / 英文文档 | ✅ 7 + 7 份 | 未改动（HC-06） |
| Java 源码 | ✅ **72 个类** | `src/main/java/com/dashpig/farmstorm/` |
| 注册对象 | ✅ **64 / 64** | `tools/verify_project.py` 逐 ID 核对 |
| 配置键 | ✅ **117 / 117** | 文档口径现算 ↔ `run/config/farmstorm-server.toml` 逐键一致 |
| 资源文件 | ✅ **261**（JSON 169 + PNG **91** + TOML 1） | 比 TECH §6.1 的 257 多 4：作者点名新增的 4 张转换器侧面作物贴图 |
| 语言键 | ✅ 222 × 2（中英键集一致） | 见 `.work/PLAN.md` R-29 / R-49（216 → 222 的全部差值已逐条说明） |
| 构建 | ✅ `gw.cmd build` → `farmstorm-0.0.1+mc1.21.1.jar` | `build/libs/` |
| 专用服务器 | ✅ `runServer` → `Done (9.889s)`，0 ERROR / 0 FATAL | `.scratch/final-server.log` |
| DataGen | ✅ `runData` 幂等（第二次 `written: 0`） | `.scratch/final-data.log` |
| 材质 | ✅ 91 张 PNG（15 作物图标带绿「+」；转换器=工作台底+四面作物） | 连跑两次 md5 一致 |

### 怎么构建

```cmd
cmd /c ""D:\project_farm\.tools\gw.cmd" build        :: 产出 build/libs/farmstorm-0.0.1+mc1.21.1.jar
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer    :: 起专用服务器（首次需 run/eula.txt）
cmd /c ""D:\project_farm\.tools\gw.cmd" runData      :: 重新生成 src/generated/resources
cmd /c ""D:\project_farm\.tools\gw.cmd" runClient    :: 起客户端
```

`gw.cmd` 负责：设 JDK 21 / `GRADLE_USER_HOME` / `TEMP`、拉起 NeoForge 镜像代理（本机到 `maven.neoforged.net` 的 TLS 被按 SNI 阻断）、注入 java.base 补丁（本机 JDK 的 ACL 判定恒 false，会让 zipfs 变只读）、`runServer` 目录锁。**不要绕过它直接调 gradle**。换一台正常机器时按 `.tools/README.md` 删掉补丁那两行即可。

> 实施过程中的全部裁决（R-01~R-55）、文档与实现的差异登记、以及每个里程碑的验收证据，都在 [`.work/PLAN.md`](.work/PLAN.md)；各阶段任务书在 `.work/TASK-*.md`。

### 玩家怎么装（不需要懂构建）

1. 用 PCL / HMCL 建一个 **Minecraft 1.21.1 + NeoForge 21.1.x** 的版本（本机实测用 21.1.265 亦可）。
2. 打开该版本的 `mods` 文件夹（没有就新建），把 `farmstorm-0.0.1+mc1.21.1.jar` 放进去。
3. 启动游戏。创造模式物品栏最后会多一个「农场风云」标签页，22 个物品都在里面。

jar 从 Releases 页下载；已知限制与版本要求见 [`.work/PLAN.md`](.work/PLAN.md) 与 Release 说明。

### 发布到 GitHub

仓库没有安装 git、也没有 GitHub 凭据，因此整理成「可直接 push」的状态，步骤见 [`docs/zh/07-PUBLISH.md`](docs/zh/07-PUBLISH.md)（含提交/排除清单、Release 附件做法、以及本机构建环境的特殊适配）。

---

## 文件索引 / File index

### 设计文档 / Design documents

| 中文 | English | 内容 |
|---|---|---|
| [`docs/zh/00-DECISIONS.md`](docs/zh/00-DECISIONS.md) | [`docs/en/00-DECISIONS.md`](docs/en/00-DECISIONS.md) | **唯一真相源**：41 条决策 D-01~D-41、9 条修正 C-1~C-9、15 个注册名 |
| [`docs/zh/01-DESIGN.md`](docs/zh/01-DESIGN.md) | [`docs/en/01-DESIGN.md`](docs/en/01-DESIGN.md) | 策划文档：15 个作物全规格、117 个 config 键、12 条进度、附录 A 矛盾登记 |
| [`docs/zh/02-TECH-SPEC.md`](docs/zh/02-TECH-SPEC.md) | [`docs/en/02-TECH-SPEC.md`](docs/en/02-TECH-SPEC.md) | 技术规格：64 个注册对象、类设计、257 个资源文件、9 条技术风险 |

### 施工文档 / Build documents

| 中文 | English | 内容 |
|---|---|---|
| [`docs/zh/03-TASKS.md`](docs/zh/03-TASKS.md) | [`docs/en/03-TASKS.md`](docs/en/03-TASKS.md) | 59 个任务 F-01~F-59，10 个 Phase，64 个注册对象全覆盖 |
| [`docs/zh/04-ROADMAP.md`](docs/zh/04-ROADMAP.md) | [`docs/en/04-ROADMAP.md`](docs/en/04-ROADMAP.md) | 6 个里程碑 M1~M6，18 条风险，18 项 DoD |

### 提示词 / Prompts

| 中文 | English | 用途 |
|---|---|---|
| [`docs/zh/05-PROMPT-MAIN.md`](docs/zh/05-PROMPT-MAIN.md) | [`docs/en/05-PROMPT-MAIN.md`](docs/en/05-PROMPT-MAIN.md) | 主提示词：14 条硬约束 HC-01~HC-14、14 条禁止行为、PDCA 纪律 |
| [`docs/zh/06-PROMPT-TASKS.md`](docs/zh/06-PROMPT-TASKS.md) | [`docs/en/06-PROMPT-TASKS.md`](docs/en/06-PROMPT-TASKS.md) | 分阶段任务清单：Phase 1~10 各一节，可单独复制投喂 |

### 工具 / Tools

| 文件 | 用途 |
|---|---|
| [`tools/gen_textures.py`](tools/gen_textures.py) | 批量生成材质：原版贴图右下角叠加绿色「+」。21 个映射（15 作物 + 6 种子） |
| [`tools/verify_api.ps1`](tools/verify_api.ps1) | 版本与 API 签名核查：拉真实最新版本号、用 `javap` 验证 4 个不确定签名，输出可直接贴回 §10.2 的 Markdown 表 |
| [`plan.txt`](plan.txt) | 作者原始设想。**只读，不作为规范依据**——已被上面七份文档取代 |

---

## 怎么用 / How to use

### 投喂编码代理的顺序

```
第一轮  05-PROMPT-MAIN.md                             （全局约束，只需投一次）
第二轮  06-PROMPT-TASKS.md 的 §2  Phase 1 那一节        （一次只投一个 Phase）
第三轮  06-PROMPT-TASKS.md 的 §3  Phase 2 那一节
...     直到 Phase 10
```

**不要一次把全部十份文档塞给代理。**主提示词 + 当前 Phase，就够它干活了。

### 代理该读哪几份

- **永远**：`00-DECISIONS.md`（裁决优先）、`05-PROMPT-MAIN.md`（纪律）
- **做玩法**：加 `01-DESIGN.md`
- **写代码**：加 `02-TECH-SPEC.md`
- **找任务**：加 `03-TASKS.md` 或当前 Phase 那一节

---

## 关键数字 / Key numbers

| 指标 | 值 |
|---|---|
| 改良作物 | 15 种 |
| registry 对象 | 64 个（items 22 / blocks 20 / BE 6 / entity 9 / recipe 2 / tab 1 / damage_type 2 / component 1 / menu 1） |
| config 键 | 117 个 |
| 资源文件 | 257 个（JSON 169 + TOML 1 + PNG 87） |
| 进度 | 12 条 |
| 任务 | 59 个，10 个 Phase |
| 工作量 | 96.0 人日裸 / 120.1 含缓冲；3 路并行约 40 人日 |
| 关键路径 | 18.5 人日 / 11 个任务 |

---

## 施工前的三件事（第 1、2 件已在实现中关闭）

### 1. 版本号与签名 —— ✅ 已实证

`NeoForge 21.1.213` + ModDevGradle `2.0.148` + MC 1.21.1 的组合**已实际跑通**：`createMinecraftArtifacts` 完成了完整 NeoForm 流水线（下载 → 反编译 → 打补丁 → 重建 → 编译 5364 个游戏源文件），`build` / `runServer` / `runData` 三端全绿。

§10.2 里那 6 处「以构建时实际拉取为准」在实现中**全部遇到了**，实际签名与退化方案的选择记录在 `.work/PLAN.md`（R-10 / R-17 / R-33）：

| 文档说法 | 1.21.1 实际 |
|---|---|
| `Explosion` 10 参构造器 | 13 参（粒子 ×2 + 音效） |
| `Explosion#shouldBlockExplode` | 在 `ExplosionDamageCalculator#shouldBlockExplode` |
| `playerWillDestroy` 可取消破坏 | 只返回 `BlockState`；否决落 `IBlockExtension#onDestroyedByPlayer` |
| `PistonBehavior` | `PushReaction` |
| `MobEffects.STRENGTH/RESISTANCE/JUMP_BOOST/SPEED/HASTE` | `DAMAGE_BOOST/DAMAGE_RESISTANCE/JUMP/MOVEMENT_SPEED/DIG_SPEED` |
| `Creeper#setPowered` / `ChestBlockEntity#setLootTable(2 参)` | 均不存在（改用存档键 / 单参 + `setLootTableSeed`） |
| `RecipesUpdatedEvent`（RT-07） | 纯客户端事件；服务端改在 `AddReloadListenerEvent` 上做缓存失效 |

> 上游文档按 HC-06 未改；差异全部登记在 `.work/PLAN.md`。

有网的机器上，[`tools/verify_api.ps1`](tools/verify_api.ps1) 能把这张表一次性清空：

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force   # 绕过执行策略，不改系统设置
.\tools\verify_api.ps1                     # 全量核查
.\tools\verify_api.ps1 -SkipJavap          # 没有 JDK 时只查版本号
.\tools\verify_api.ps1 -OutFile docs\version-check.md
```

输出一张 Markdown 表（核查项 / 结果 / 出处 / 置信度），直接贴进中英两版 §10.2。网络失败、缺 `build.gradle`、缺 JDK 三种情况各自隔离，不会中断。

### 2. 材质 —— ✅ 已生成（87 张）

`tools/gen_textures.py` 已重写成一条生产线，直接吃客户端 jar（不解包），产出 **87 张 PNG**：22 item + 56 作物阶段 + 8 方块 + 1 GUI。

```bash
python -X utf8 tools/gen_textures.py --list
python -X utf8 tools/gen_textures.py --vanilla-assets <客户端.jar 或解包资源根> --out src/main/resources --dry-run
python -X utf8 tools/gen_textures.py --vanilla-assets <客户端.jar 或解包资源根> --out src/main/resources
```

- 本次用的是本机 **MC 26.3** 客户端：命中原版 75 张 / 次选原版 11 张 / 纯色占位 **0** 张。
- 幂等：连跑两次全目录 md5 一致（`F0988D9B4D23DD69869FE128534F6576`）。
- 版本差异与逐张来源清单见 `.work/PLAN.md` 与 `.scratch/m5d-run.log`；换成真 1.21.1 资源只需改 `--vanilla-assets` 重跑，**不要手改 PNG**。

### 3. 英文版是翻译，不是另写的

`docs/en/` 全部由 `docs/zh/` 翻译而来。**若两者冲突，以中文版为准**（中文版才是经过九轮裁决与全文 grep 校验的那一份）。

---

## 已知的设定取舍

原设想里有几处笔误和空白，已在 `00-DECISIONS.md` §5 逐条登记（C-1~C-9）。最值得知道的四条：

| 问题 | 处理 |
|---|---|
| 条目数其实是 **15** 不是 16 | 幸运浆果随机池因此为 14 种（C-1 / D-17） |
| 瓜片子「爆炸抗性 ≥ -1」是笔误 | 改为「≥ 圆石 6.0」，与瓜！对齐（C-2 / D-19） |
| 「所有玩家」与「只针对非己方」冲突 | **范围效果不筛敌我**，敌我筛选只管是否触发（D-27） |
| 火把花/瓶子草算炮台类却要挖 10 秒 | **只有 `pumpkin_turret` 硬挖 10 秒**（C-8 / D-37） |

---

## English

A Minecraft 1.21.1 NeoForge mod that turns vanilla crops into PvP gadgets.

**Status: documentation complete, zero lines of Java.** The design and build documents are finished; implementation is driven by the two prompt files.

### Documents

- `docs/en/00-DECISIONS.md` — single source of truth: 41 decisions (D-01…D-41), 9 corrections (C-1…C-9)
- `docs/en/01-DESIGN.md` — gameplay design: all 15 crops, 117 config keys, 12 advancements
- `docs/en/02-TECH-SPEC.md` — technical spec: 64 registry objects, class design, 257 resource files, 9 risks
- `docs/en/03-TASKS.md` — 59 tasks (F-01…F-59) across 10 phases
- `docs/en/04-ROADMAP.md` — 6 milestones (M1…M6), 18 risks, 18 DoD items
- `docs/en/05-PROMPT-MAIN.md` — main system prompt: 14 hard constraints, 14 prohibitions
- `docs/en/06-PROMPT-TASKS.md` — phase-by-phase prompt, one section per phase

### How to feed a coding agent

1. Send `05-PROMPT-MAIN.md` once.
2. Send exactly one phase section from `06-PROMPT-TASKS.md` at a time.
3. Add `01-DESIGN.md` for gameplay work and `02-TECH-SPEC.md` for code.

Never dump all ten documents at once.

### Before you start coding

1. **Version numbers are unverified.** The authoring environment had no outbound network access. `NeoForge 21.1.213` is sourced from documentation URLs and Maven artifact paths, not page contents. Run `./gradlew build` first and write the result back into `02-TECH-SPEC.md` §10.2.
2. **Textures were never generated.** `tools/gen_textures.py` passes syntax checks but has never produced a PNG. Run it with `--dry-run` first.
3. **The English documents are translations.** If they conflict with `docs/zh/`, the Chinese originals win.

### Scope of v0.0.1

15 modified crops across 4 types (throwable / deployable / plantable / edible) and 6 categories (trap / turret / demolition / buff / construction / damage). Conversion happens through a single-slot Arcane Converter block. Team allegiance is resolved through the vanilla scoreboard via a `TeamResolver` abstraction. No Mixin. Server-authoritative. Zero custom network packets.

---

## 许可 / License

未定 / TBD by the author.
