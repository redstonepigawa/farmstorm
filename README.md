# 农场风云 / Farmstorm

Minecraft 1.21.1 · NeoForge 21.1.213 · Java 21 · modid `farmstorm` · v0.0.1（已发布）· v0.0.2（已发布）· v0.0.3（已发布）· **v0.0.4（当前发布版）**

> 把原版农作物改造成 PvP 道具：同一种作物可以种在地里、扔出去、架在墙上、或者吃掉。
> Vanilla crops re-armed as PvP gadgets: plant them, throw them, mount them, or eat them.

---

## ✅ 当前状态：v0.0.1 已实现并通过验收

D-01 说的「文档先行、代码由提示词驱动」已经跑完一轮：**代码已按 59 个任务、10 个 Phase 全部落地**，并经作者多轮真机反馈迭代（渲染、GUI、玩法口径）。

作物分类严格按 DESIGN §2 拆成**正交两维**：`CropType` 单值（投掷型 8 / 部署型 5 / 食用型 2）+ `CropCategory` 多值（陷阱/炮台/破障/buff/建造/伤害），行为（如「能否破坏方块」）由标签驱动而不是写死在调用点。

| 项 | 状态 | 证据 |
|---|---|---|
| 中文 / 英文文档 | ✅ 7 + 7 份 | 未改动（HC-06） |
| Java 源码 | ✅ **74 个类** | `src/main/java/com/dashpig/farmstorm/` |
| 注册对象 | ✅ **64 / 64** | `tools/verify_project.py` 逐 ID 核对 |
| 配置键 | ✅ **117 / 117** | 文档口径现算 ↔ `run/config/farmstorm-server.toml` 逐键一致 |
| 资源文件 | ✅ **262**（JSON 169 + PNG **91** + TOML 1 + `pack.mcmeta` 1） | 比 TECH §6.1 的 257 多 4：作者点名新增的 4 张转换器侧面作物贴图 |
| 语言键 | ✅ 222 × 2（中英键集一致） | 见 `.work/PLAN.md` R-29 / R-49（216 → 222 的全部差值已逐条说明） |
| 构建 | ✅ `gw.cmd build` → `farmstorm-0.0.1+mc1.21.1.jar` | `build/libs/` |
| 专用服务器 | ✅ `runServer` → `Done (9.889s)`，0 ERROR / 0 FATAL | `.scratch/final-server.log` |
| DataGen | ✅ `runData` 幂等（第二次 `written: 0`） | `.scratch/final-data.log` |
| 材质 | ✅ 91 张 PNG（15 作物图标带绿「+」；转换器=工作台底+四面作物） | 连跑两次 md5 一致 |

---

## 🚀 v0.0.2 索引（已实现；本节与文末「素材来源」即 F-99 交付的两节）

v0.0.2 走的是同一套「文档先行」流程，但深度不同：这一版新接三个 0.0.1 完全没有的子系统——**世界灰度后处理**（原版 `PostChain`，只灰世界不灰 HUD）、**客户端重绘红色剪影**、**键鼠输入拦截**——所以六份文档里技术规格最厚（`12-TECH-0.0.2.md`）。**实现已完成**，下表是文档侧口径，紧接着的「实测口径」是发布时该引用的数字。

| 项 | 值 | 依据 |
|---|---|---|
| 新增决策 | **D-49 ~ D-79**，31 条 | `10-DECISIONS-0.0.2.md` |
| 裁决记录 | **A-01 ~ A-11**，11 条 | `13-TASKS-0.0.2.md` §0.4 + `15-PROMPT-TASKS-0.0.2.md` §0.4 |
| 新增内容 | 7 项：转换器合成配方、莫洛托夫！、桑吉尔蔗、压缩干草块、压缩膨胀麦、狂 野 猎 人、狂野苹果 | `11-DESIGN-0.0.2.md` |
| 任务 | **40 个**（F-60 ~ F-99），5 个 Phase（11 ~ 15） | `13-TASKS-0.0.2.md` |
| 工作量 | **60.5 人日裸 / 75.6 含缓冲**；关键路径 14.5 人日 / 9 个任务 | `13-TASKS-0.0.2.md` §2 §11 |
| 技术风险 | **RT-10 ~ RT-16**，7 条新增 | `12-TECH-0.0.2.md` §10 |

### v0.0.2 实测口径（发布说明引用这一份）

| 项 | 实测值 | 怎么核 |
|---|---|---|
| 注册对象 | **77**（items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1） | 启动日志 `自检 注册对象 77 个 … → OK` |
| 实体渲染器 | **11 / 11** | 客户端日志 `自检 实体渲染器 11/11 → OK` |
| config 键 | **139** 条（117 + 22） | `run/config/farmstorm-server.toml` 叶节点 |
| 语言键 | **263 × 2**（中英键集逐字相等） | 两份 `assets/farmstorm/lang/*.json` 键数；`set(zh) == set(en)` |
| 资源文件 | **305** 个（JSON 197 / PNG 100 / OGG 2 / GLSL 4 / TOML 1 / `pack.mcmeta` 1） | `git ls-files` 出清单后扣 `.cache` 的 11 个与 3 个 `.gitkeep` |
| 进度 | **15** 条（v0.0.1 的 12 + 3） | `data/farmstorm/advancement/` |
| 转换配方 / 合成配方 | **24 / 2**（+3 / +2） | `src/generated/resources/data/farmstorm/recipe/` |
| 幸运浆果池 | **16** 种（19 条档案里排除幸运浆果、狂野苹果、压缩膨胀麦） | `CropProfile.LUCKY_BERRY_POOL` |
| 任务 | **40 个**（F-60 ~ F-99），5 个 Phase：11 = 7 / 12 = 9 / 13 = 10 / 14 = 8 / 15 = 6 | `13-TASKS-0.0.2.md` |

### 已知限制（每次发布都必须一并说明）

| 限制 | 现象 / 口径 | 依据 |
|---|---|---|
| **灰度只作用于世界，HUD 保持原色** | 方块 / 实体 / 粒子变灰，物品栏、血条、聊天栏仍是彩色。原版 `PostChain` 的 `process()` 在 `GameRenderer:1027` 执行、GUI 在 `:1073` 绘制，含 GUI 没有可靠实现路径，故按此口径发布 | V-56 / V-61；`12-TECH §4.1` |
| **相位 2 的「视角锁定」只是降速，不是锁死** | `CalculatePlayerTurnEvent#setMouseSensitivity(0)` 之后 `MouseHandler#turnPlayer` 的系数仍是 `0×0.6+0.2 = 0.2`，相对默认 0.5 约慢 **15.6 倍**；无 Mixin 无法归零 | V-31；`12-TECH §4.3` |
| **F11 / F2 / F3 / ESC 拦不住** | 这四个键的原版处理在 `KeyboardHandler#keyPress` 内部、早于任何 NeoForge 钩子，Mixin-free 够不着；D-60 的锁定范围本来也不含它们 | `12-TECH §4.3` |
| **1×1 石柱构型的落地时长偏短** | 3×3 及以上台面三个地形都是 40 ± 2 刻；1×1 石柱会让玩家漂浮段的横向漂移把自己挪出柱顶、下坠中途被柱体截住（实测 23 刻），属真实几何结果，不作缺陷 | V-46；`12-TECH §11 #10` |
| **狂野猎人期间打开设置界面，亮度滑条显示为满** | 水下可视性需要临时抬高 `options.gamma()`，而原版选项系统没有「临时覆盖」 | 已知限制；关闭界面 / 效果结束后滑条与亮度都恢复原值（V-78） |
| **火把花光环：离开范围后火焰附加最多再留 15 秒** | `TempEnchantHandler.revokeAll()` 目前**没有调用者**，离开光环不会立刻撤销，只由 300 刻到期撤销 | 已知限制（验收口径是「15 秒内消失」）；若要「离开立即撤销」需加「上一刻在光环内、这一刻不在」的边沿检测（V-77） |

> 之前登记过的「配方书分类 `Unknown recipe category: farmstorm:convert`」**已在 v0.0.2 修掉**（`client/ConverterRecipeBookCategory` 走 `RegisterRecipeBookCategoriesEvent#registerRecipeCategoryFinder`），不再列为已知限制。

**两条硬约束继续有效，且这一版更吃紧**：0 个自定义网络包（C-6 / D-50，客户端状态全部借隐藏 `MobEffect` 同步）、无 Mixin（D-40）。此外 v0.0.1 的 `00`–`09` 与 `docs/en/00`–`09` **全部冻结不动**（D-49），v0.0.2 只写增量。

> ✅ **媒体管线已就位并实测通过**：`tools/convert_media.ps1` 已写好，三个源素材能转成两个 mono 64 kbps OGG（`wild_hunter_loop` **352 KB / 47.14 s / 48 kHz**、`zangief_piledriver` **33 KB / 44.1 kHz**）与一张 18×18 PNG（1.4 KB）。本机跑法固定为 `powershell.exe -NoProfile -ExecutionPolicy Bypass -File …` 且输出重定向到文件，**不要加 `-Force`**（会重编已验收的产物，Ogg 字节不可复现）。本机 ffmpeg 在 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`（7.1 essentials，带 libvorbis）——**它不在 PATH 上，要传 `-Ffmpeg`**；Trae CN 自带的 6.1.1 是裁剪构建、没有 libvorbis，不能用。脚本不下载任何东西（npm / maven / 镜像都不可达）；缺 ffmpeg 或源文件时直接退 1，一个文件都不写。

### 怎么构建

```cmd
cmd /c ""D:\project_farm\.tools\gw.cmd" build        :: 产出 build/libs/farmstorm-0.0.2+mc1.21.1.jar
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer    :: 起专用服务器（首次需 run/eula.txt）
cmd /c ""D:\project_farm\.tools\gw.cmd" runData      :: 重新生成 src/generated/resources
cmd /c ""D:\project_farm\.tools\gw.cmd" runClient    :: 起客户端
```

`gw.cmd` 负责：设 JDK 21 / `GRADLE_USER_HOME` / `TEMP`、拉起 NeoForge 镜像代理（本机到 `maven.neoforged.net` 的 TLS 被按 SNI 阻断）、注入 java.base 补丁（本机 JDK 的 ACL 判定恒 false，会让 zipfs 变只读）、`runServer` 目录锁。**不要绕过它直接调 gradle**。换一台正常机器时按 `.tools/README.md` 删掉补丁那两行即可。

> 实施过程中的全部裁决（R-01~R-55）、文档与实现的差异登记、以及每个里程碑的验收证据，都在 [`.work/PLAN.md`](.work/PLAN.md)；各阶段任务书在 `.work/TASK-*.md`。

### 玩家怎么装（不需要懂构建）

1. 用 PCL / HMCL 建一个 **Minecraft 1.21.1 + NeoForge 21.1.x** 的版本（本机实测用 21.1.265 亦可）。
2. 打开该版本的 `mods` 文件夹（没有就新建），把 `farmstorm-<版本>+mc1.21.1.jar` 放进去。当前发布件见 `dist/`（**`0.0.2` 是当前发布版**，`0.0.1` 保留作对照；同目录的 `安装说明.txt` 已更新到 0.0.2，含「v0.0.2 有什么」与完整已知限制）。
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

### v0.0.2 增量文档 / v0.0.2 delta documents

| 中文 | English | 内容 |
|---|---|---|
| [`docs/zh/10-DECISIONS-0.0.2.md`](docs/zh/10-DECISIONS-0.0.2.md) | [`docs/en/10-DECISIONS-0.0.2.md`](docs/en/10-DECISIONS-0.0.2.md) | **v0.0.2 唯一真相源**：30 条决策 D-49~D-78、19 条作物档案总表、对 v0.0.1 文档的口径澄清 |
| [`docs/zh/11-DESIGN-0.0.2.md`](docs/zh/11-DESIGN-0.0.2.md) | [`docs/en/11-DESIGN-0.0.2.md`](docs/en/11-DESIGN-0.0.2.md) | 设计增量：7 个新功能全规格、桑吉尔蔗时序表与相位表、狂野猎人 8 项子规格、13 条边界 |
| [`docs/zh/12-TECH-0.0.2.md`](docs/zh/12-TECH-0.0.2.md) | [`docs/en/12-TECH-0.0.2.md`](docs/en/12-TECH-0.0.2.md) | 技术增量：77 个注册对象核对、三个客户端子系统方案、36 个新资源、22 条新 config、RT-10~RT-16、22 条验收自检 |
| [`docs/zh/13-TASKS-0.0.2.md`](docs/zh/13-TASKS-0.0.2.md) | [`docs/en/13-TASKS-0.0.2.md`](docs/en/13-TASKS-0.0.2.md) | 任务拆分与大纲：40 个任务 F-60~F-99、5 个 Phase、M7~M11、9 条裁决 A-01~A-09、四张覆盖对照表 |
| [`docs/zh/14-PROMPT-MAIN-0.0.2.md`](docs/zh/14-PROMPT-MAIN-0.0.2.md) | [`docs/en/14-PROMPT-MAIN-0.0.2.md`](docs/en/14-PROMPT-MAIN-0.0.2.md) | v0.0.2 主提示词：14 条硬约束 HC-15~HC-28、14 条禁止行为 N-15~N-28、15 条已知陷阱 |
| [`docs/zh/15-PROMPT-TASKS-0.0.2.md`](docs/zh/15-PROMPT-TASKS-0.0.2.md) | [`docs/en/15-PROMPT-TASKS-0.0.2.md`](docs/en/15-PROMPT-TASKS-0.0.2.md) | v0.0.2 分阶段任务清单：Phase 11~15 各一节（每节固定 6 项），可单独复制投喂 |

### v0.0.4（当前发布版）/ v0.0.4 (current release)

v0.0.4 只改 Q弹 与朋友的韭命中音，**没有新增玩法**，也没有新增增量文档（改动量与口径都写在这一节与 `.work/PLAN.md` 的 V-158 里）。

| 改动 | 说明 | 验证方式 |
|---|---|---|
| 朋友的韭命中音 158 s → **10 s** | 之前是整首曲子没截断 | ffmpeg 输出侧 `-t 10`；独立 Vorbis 解析 granule 480,000 → **10.000000 s**；`-fflags +bitexact` 使三次重建字节相同 |
| **别人也看得见 Q弹 的模型变化** | 两层根因：① 姿势参数取自本机玩家时钟；② **原版只把效果包发给实体自己的连接**，观察者根本不知道别人身上的效果 | 相位改按实体计时；服务端每 20 刻向 64 格内观察者补发原版效果包、三条快路径补发移除包。实测：持有者自收 1 条、观察者每秒 1 条、到期/清除/死亡各有移除包 |
| **Q弹 只锁移动** | 之前与桑吉尔蔗 共用七段全锁 | 拆成 `fullyLocked()`（桑吉尔蔗，第②~⑦段）与 `movementLocked()`（含 Q弹，第①段）；混乱的锁优先改判移动锁 |
| **Q弹 期间强制第三人称** | 按 F5 会被拉回，结束恢复 | 新增 `client/QbounceThirdPersonController`；记下 → 每刻写 `THIRD_PERSON_BACK` → 消失/死亡/退世界恢复 |

红线未变：`CustomPacketPayload` **0**、`@Mixin` **0**（用的是原版效果包与声音包）。
关键数字与 v0.0.3 相同：注册 **90** / config **155** / 语言键 **299 × 2** / 作物 22 / 进度 18。

### v0.0.3 增量文档 / v0.0.3 delta documents

| 中文 | English | 行数（中/英） | 内容 |
|---|---|---|---|
| [`docs/zh/16-DECISIONS-0.0.3.md`](docs/zh/16-DECISIONS-0.0.3.md) | [`docs/en/16-DECISIONS-0.0.3.md`](docs/en/16-DECISIONS-0.0.3.md) | 619 / 622 | **v0.0.3 唯一真相源**：23 条决策 D-80~D-102（含 §3.6 的 5 条补充裁定）、22 条档案总表、对 v0.0.2 既有内容的改动登记 |
| [`docs/zh/17-DESIGN-0.0.3.md`](docs/zh/17-DESIGN-0.0.3.md) | [`docs/en/17-DESIGN-0.0.3.md`](docs/en/17-DESIGN-0.0.3.md) | 1032 / 1037 | 设计增量：7 个条目全规格（Q弹 两条坐姿分支、混乱、朋友的韭、近防菌、震动豆、刺球、桑吉尔蔗）、§12 十四条边界、附录 A 差异登记 |
| [`docs/zh/18-TECH-0.0.3.md`](docs/zh/18-TECH-0.0.3.md) | [`docs/en/18-TECH-0.0.3.md`](docs/en/18-TECH-0.0.3.md) | 966 / 974 | 技术增量：基线 **77 → 90**、config **139 → 158**、语言键 **263 → 300**、31 个新资源、RT-17~RT-23、17 条验收自检 |
| [`docs/zh/19-TASKS-0.0.3.md`](docs/zh/19-TASKS-0.0.3.md) | [`docs/en/19-TASKS-0.0.3.md`](docs/en/19-TASKS-0.0.3.md) | 910 / 921 | 任务拆分与大纲：**36 个任务 F-100~F-135**、5 个 Phase（16~20）、M12~M16、9 条裁决 A-10~A-18、五张覆盖对照表 |
| [`docs/zh/20-PROMPT-MAIN-0.0.3.md`](docs/zh/20-PROMPT-MAIN-0.0.3.md) | [`docs/en/20-PROMPT-MAIN-0.0.3.md`](docs/en/20-PROMPT-MAIN-0.0.3.md) | 380 / 387 | v0.0.3 主提示词：**HC-29~HC-50 共 22 条**、**N-29~N-50 共 22 条**、22 条已知陷阱 |
| [`docs/zh/21-PROMPT-TASKS-0.0.3.md`](docs/zh/21-PROMPT-TASKS-0.0.3.md) | [`docs/en/21-PROMPT-TASKS-0.0.3.md`](docs/en/21-PROMPT-TASKS-0.0.3.md) | 627 / 639 | v0.0.3 分阶段任务清单：Phase 16~20 各一节（每节固定 6 项），§9 自检 36/36，§0.5 数字速查 15 行 |

> v0.0.3 那六份的**中文合计 4534 行 / 英文合计 4580 行**。

### v0.1.0 增量文档 / v0.1.0 delta documents

| 中文 | English | 行数（中/英） | 内容 |
|---|---|---|---|
| [`docs/zh/22-DECISIONS-0.1.0.md`](docs/zh/22-DECISIONS-0.1.0.md) | [`docs/en/22-DECISIONS-0.1.0.md`](docs/en/22-DECISIONS-0.1.0.md) | 641 / 653 | **v0.1.0 唯一真相源**：21 条决策 D-103~D-123、§4.2 的 19 条改动面、对 0.0.4 既有内容的改动登记 |
| [`docs/zh/23-DESIGN-0.1.0.md`](docs/zh/23-DESIGN-0.1.0.md) | [`docs/en/23-DESIGN-0.1.0.md`](docs/en/23-DESIGN-0.1.0.md) | 1197 / 待补 | 设计增量：9 条需求全规格（蓄力条、朋友的韭材质、膨胀麦、瓜、生长、陷阱四项、耕地额度与据点核心、狂野苹果）、§11 十三条边界 |
| [`docs/zh/24-TECH-0.1.0.md`](docs/zh/24-TECH-0.1.0.md) | [`docs/en/24-TECH-0.1.0.md`](docs/en/24-TECH-0.1.0.md) | 955 / 待补 | 技术增量：基线 **90 → 93**、config **155 → 166**、语言键 **299 → 315**、PNG +5 / JSON +5、RT-24~RT-30、18 条验收 |
| [`docs/zh/25-TASKS-0.1.0.md`](docs/zh/25-TASKS-0.1.0.md) | [`docs/en/25-TASKS-0.1.0.md`](docs/en/25-TASKS-0.1.0.md) | 982 / 993 | 任务拆分与大纲：**40 个任务 F-136~F-175**、5 个 Phase（21~25）、M17~M21、10 条裁决 A-19~A-28、六张覆盖对照表 |
| [`docs/zh/26-PROMPT-MAIN-0.1.0.md`](docs/zh/26-PROMPT-MAIN-0.1.0.md) | [`docs/en/26-PROMPT-MAIN-0.1.0.md`](docs/en/26-PROMPT-MAIN-0.1.0.md) | 407 / 416 | v0.1.0 主提示词：**HC-51~HC-76 共 26 条**、**N-51~N-74 共 24 条**、22 条已知陷阱 |
| [`docs/zh/27-PROMPT-TASKS-0.1.0.md`](docs/zh/27-PROMPT-TASKS-0.1.0.md) | [`docs/en/27-PROMPT-TASKS-0.1.0.md`](docs/en/27-PROMPT-TASKS-0.1.0.md) | 650 / 661 | v0.1.0 分阶段任务清单：Phase 21~25 各一节（每节 6 项）、§9 自检 40/40、§0.5 速查 16 行 |

> v0.1.0 那六份的**中文合计 4832 行 / 英文合计 4916 行**（英文四份的行数随最后的裁决回灌略有变动，以文件为准）。

### 实施文档 / Implementation documents

| 文件 | 内容 |
|---|---|
| [`docs/zh/08-DEV-GUIDE.md`](docs/zh/08-DEV-GUIDE.md) | **开发者手册**：代码地图、三个不可动摇的约束、构建与调试手法、**13 条已踩过的坑对照表**、不要做的事 |
| [`docs/zh/09-IMPL-VS-PLAN.md`](docs/zh/09-IMPL-VS-PLAN.md) | **实现与原计划的差异对照**（12 组）：原计划写的 / 实现是什么 / 依据（D/C/R 编号） |
| [`docs/zh/07-PUBLISH.md`](docs/zh/07-PUBLISH.md) | 发布指南：提交/排除清单、Release 做法、更新与发版流程、本机构建环境适配 |
| [`.work/PLAN.md`](.work/PLAN.md) | 决策记录 R-01~R-55 + 文档与实现的差异登记（**唯一真相源的补充**） |
| [`.work/TASK-*.md`](.work) | 各阶段任务书与验收标准 |

### 工具 / Tools

| 文件 | 用途 |
|---|---|
| [`tools/gen_textures.py`](tools/gen_textures.py) | 批量生成 91 张材质（原版贴图 + 绿色「+」叠加、转换器四面作物）。`--vanilla-assets` 必须指向 **26.3 客户端 jar** |
| [`tools/verify_project.py`](tools/verify_project.py) | 静态验收：注册对象 / config 键 / 语言键一致性 / 禁令扫描 |

> ✅ **实测（0.0.4 树）：跑通。** 输出 `已核 registry 对象: 90 / 90`、`config 155`、`lang 299`、`结论：FAIL 0 项`，退出码 0。
>
> ⚠️ **但它有一个静默盲区**：新增注册对象时，若只往 EXPECTED 清单里加 ID 而不同步 EXPECTED_COUNTS，它会报错；两边都不动则**继续输出全绿、静默漏检**。v0.1.0 要加 3 个对象（F-136），动作顺序是「先加清单 → 再改计数 → 再加第四个 config 来源」（见 24-TECH §1.4）。 |
| [`tools/verify_api.ps1`](tools/verify_api.ps1) | 版本与 API 签名核查：拉真实最新版本号、用 `javap` 验证 4 个不确定签名，输出可直接贴回 §10.2 的 Markdown 表 |
| [`tools/convert_media.ps1`](tools/convert_media.ps1) | **v0.0.2 媒体管线**：把仓库外的三个源素材转成两个 mono 64 kbps OGG 与一张 18×18 效果图标 PNG。ffmpeg 按 `-Ffmpeg` → PATH → `tools/ffmpeg/bin` → `.tools/ffmpeg/bin` 顺序找；缺 ffmpeg / 缺 libvorbis / 缺源文件一律退 1 且不写任何文件；`-CheckOnly` 只校验环境，`-Force` 强制重编，否则输出比源新就跳过。不下载任何东西。⚠️ 两个 `.ogg` **不可字节复现**（Ogg 容器每次写随机 serial），验收别拿 ogg 的 md5 当判据，比时长 / 声道 / 码率 |
| [`plan0.0.1.txt`](plan0.0.1.txt) | 作者原始设想。**只读，不作为规范依据**——已被上面七份文档取代 |

---

## 怎么用 / How to use

### 投喂编码代理的顺序

```
第一轮  05-PROMPT-MAIN.md                             （全局约束，只需投一次）
第二轮  06-PROMPT-TASKS.md 的 §2  Phase 1 那一节        （一次只投一个 Phase）
第三轮  06-PROMPT-TASKS.md 的 §3  Phase 2 那一节
...     直到 Phase 10
```

**不要把整套文档一次塞给代理。**主提示词 + 当前 Phase，就够它干活了。

### v0.0.2 的投喂顺序

```
第一轮  14-PROMPT-MAIN-0.0.2.md                          （v0.0.2 全局约束，只投一次）
第二轮  15-PROMPT-TASKS-0.0.2.md 的 §2  Phase 11 那一节     （一次只投一个 Phase）
第三轮  15-PROMPT-TASKS-0.0.2.md 的 §3  Phase 12 那一节
...     直到 Phase 15
```

做玩法时加 `11-DESIGN-0.0.2.md`，写代码时加 `12-TECH-0.0.2.md`，找任务时加 `13-TASKS-0.0.2.md` 或当前 Phase 那一节。**`10-DECISIONS-0.0.2.md` 要一直在场**——v0.0.2 与 v0.0.1 冲突时以它为准。

### v0.0.3 的投喂顺序

```
第一轮  20-PROMPT-MAIN-0.0.3.md                          （v0.0.3 全局约束，只投一次）
第二轮  21-PROMPT-TASKS-0.0.3.md 的 §2  Phase 16 那一节     （一次只投一个 Phase）
第三轮  21-PROMPT-TASKS-0.0.3.md 的 §3  Phase 17 那一节
...     直到 Phase 20
```

做玩法时加 `17-DESIGN-0.0.3.md`，写代码时加 `18-TECH-0.0.3.md`，找任务时加 `19-TASKS-0.0.3.md` 或当前 Phase 那一节。**`16-DECISIONS-0.0.3.md` 要一直在场**——v0.0.3 与 v0.0.2 冲突时以它为准。

> ✅ **Phase 16 的 `tools/verify_project.py` 已在 0.0.3/0.0.4 期间修好**：现在跑出来是 `90 / 90`、config 155、lang 299、`FAIL 0 项`、退出码 0。
>
> （此前这里写「它还是 0.0.1 时代的硬编码、跑出来 `FAIL 1 项`」——那是 0.0.2 时期的状态，已过时。）

### v0.1.0 的投喂顺序

```
第一轮  26-PROMPT-MAIN-0.1.0.md                          （v0.1.0 全局约束，只投一次）
第二轮  27-PROMPT-TASKS-0.1.0.md 的 §2  Phase 21 那一节     （一次只投一个 Phase）
第三轮  27-PROMPT-TASKS-0.1.0.md 的 §3  Phase 22 那一节
...     直到 Phase 25
```

做玩法时加 `23-DESIGN-0.1.0.md`，写代码时加 `24-TECH-0.1.0.md`，找任务时加 `25-TASKS-0.1.0.md` 或当前 Phase 那一节。**`22-DECISIONS-0.1.0.md` 要一直在场**——v0.1.0 与 0.0.4 冲突时以它为准。

> ⚠️ **Phase 21 的第一件事是把 `tools/verify_project.py` 扩到新对象**（F-136）。它现在是**绿的**（90 / 155 / 299、`FAIL 0`），但它有一个**静默盲区**：往 `EXPECTED` 清单加 ID 却不同步 `EXPECTED_COUNTS` 会红；两边都不动则一直全绿、静默漏检新增的 3 个对象。固定顺序是「先加清单 → 再改计数到 93/166/315 → 再加第四个 config 来源」，中间态的红是预期行为。

### 代理该读哪几份

- **永远**：`00-DECISIONS.md`（裁决优先）、`05-PROMPT-MAIN.md`（纪律）
- **做玩法**：加 `01-DESIGN.md`
- **写代码**：加 `02-TECH-SPEC.md`
- **找任务**：加 `03-TASKS.md` 或当前 Phase 那一节

---

## 关键数字 / Key numbers

| 指标 | v0.0.1（已实现） | v0.0.2（**实测**） | v0.0.3（已实现；列内为**当时的文档口径**） | v0.1.0（**文档口径，未实现**） |
|---|---|---|---| |
| 改良作物档案 | 15 种 | **19 种**（投掷 10 / 部署 6 / 食用 3；破障 4） | **22 种**（投掷 12 / 部署 7 / 食用 3；破障 4） | **22 种**（不变；本版不新增档案，但瓜！与瓜片子移除破障类） |
| registry 对象 | 64 个 | **77 个**（+5 items / +2 blocks / +2 entity / +2 mob_effect / +2 sound_event） | **90 个**（+4 items / +3 blocks / +2 entity / +2 mob_effect / +1 sound_event / +1 damage_type） | **93 个**（+3：`outpost_core` 的 block / block_entity / item） |
| config 键 | 117 个 | **139 个**（+22） | **158 个**（+19：16 新 + 3 改默认值） | **166 个**（+11：新增 11） |
| 资源文件 | 262 个（实测；169 JSON + 91 PNG + 1 TOML + 1 `pack.mcmeta`） | **305 个**（JSON 197 / PNG 100 / OGG 2 / GLSL 4 / TOML 1 / `pack.mcmeta` 1） | **336 个**（+31：JSON +22 / PNG +8 / OGG +1） | **+10 个**（JSON +5 / PNG +5；另有 6 张 PNG 就地替换） |
| 语言键 | 222 × 2（中英相等） | **263 × 2**（+41） | **299 × 2**（+36） | **315 × 2**（+16） |
| 进度 | 12 条 | **15 条**（+3） | **18 条**（+3） | **18 条**（不变） |
| 转换配方 / 合成配方 | 21 / 0 | **24 / 2** | **26 / 2**（+2 转换） | **26 / 3**（+1 合成：据点核心） |
| 幸运浆果池 | 14 种 | **16 种**（排除幸运浆果、狂野苹果、压缩膨胀麦） | **19 种**（D-97：三条新档案全部纳入，推导式不动） | **19 种**（不变） |
| 任务 | 59 个，10 个 Phase | **40 个（F-60~F-99），5 个 Phase** | **36 个（F-100~F-135），5 个 Phase** | **40 个（F-136~F-175），5 个 Phase** |
| 工作量 | 96.0 人日裸 / 120.1 含缓冲 | **60.5 人日裸 / 75.6 含缓冲** | **60.0 人日裸 / 75.0 含缓冲** | **63.0 人日裸 / 78.75 含缓冲** |
| 关键路径 | 18.5 人日 / 11 个任务 | **14.5 人日 / 9 个任务** | **18.0 人日 / 11 个任务** | **22.0 人日 / 11 个任务** |
| 技术风险 | RT-01 ~ RT-09 | **RT-01 ~ RT-16**（+RT-10 ~ RT-16） | **RT-01 ~ RT-23**（+RT-17 ~ RT-23） | **RT-01 ~ RT-30**（+RT-24 ~ RT-30） |

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

### 2. 材质 —— ✅ 已生成（91 张）

`tools/gen_textures.py` 已重写成一条生产线，直接吃客户端 jar（不解包），产出 **91 张 PNG**：22 item + 56 作物阶段 + 12 方块（含 4 张转换器侧面作物贴图）+ 1 GUI。

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

**Status: v0.0.1 is implemented, built and released; v0.0.2 is fully specified but not yet implemented.**

### Documents

v0.0.1 (implementation period, frozen — D-49):

- `docs/en/00-DECISIONS.md` — single source of truth: 48 decisions (D-01…D-48), 10 corrections (C-1…C-10)
- `docs/en/01-DESIGN.md` — gameplay design: all 15 crops, 117 config keys, 12 advancements
- `docs/en/02-TECH-SPEC.md` — technical spec: 64 registry objects, class design, 257 resource files, 9 risks
- `docs/en/03-TASKS.md` — 59 tasks (F-01…F-59) across 10 phases
- `docs/en/04-ROADMAP.md` — 6 milestones (M1…M6), 18 risks, 18 DoD items
- `docs/en/05-PROMPT-MAIN.md` — main system prompt: 14 hard constraints, 14 prohibitions
- `docs/en/06-PROMPT-TASKS.md` — phase-by-phase prompt, one section per phase
- `docs/en/09-IMPL-VS-PLAN.md` — implementation-vs-plan diff register

v0.0.2 delta:

- `docs/en/10-DECISIONS-0.0.2.md` — **v0.0.2 source of truth**: 30 decisions (D-49…D-79), 19 crop profiles, scope clarifications for three v0.0.1 spots
- `docs/en/11-DESIGN-0.0.2.md` — design delta: full specs for 7 new features, the Zangief Cane timing/phase tables, 8 sub-specs for Wild Hunter, 13 boundary cases
- `docs/en/12-TECH-0.0.2.md` — tech delta: 77 registry objects verified, three client subsystems, 36 new resource files, 22 new config keys, RT-10…RT-16, 22 acceptance checks
- `docs/en/13-TASKS-0.0.2.md` — 40 tasks (F-60…F-99), 5 phases, M7…M11, rulings A-01…A-09, four coverage tables
- `docs/en/14-PROMPT-MAIN-0.0.2.md` — v0.0.2 main prompt: 14 hard constraints (HC-15…HC-28), 14 prohibitions (N-15…N-28), 15 known pitfalls
- `docs/en/15-PROMPT-TASKS-0.0.2.md` — v0.0.2 phase prompt: one section per phase (11…15), six fixed items each

### How to feed a coding agent

**v0.0.1** (existing behaviour, frozen):

1. Send `05-PROMPT-MAIN.md` once.
2. Send exactly one phase section from `06-PROMPT-TASKS.md` at a time.
3. Add `01-DESIGN.md` for gameplay work and `02-TECH-SPEC.md` for code.

**v0.0.2** (new content only):

1. Send `14-PROMPT-MAIN-0.0.2.md` once.
2. Send exactly one phase section from `15-PROMPT-TASKS-0.0.2.md` at a time (Phase 11 … 15).
3. Add `11-DESIGN-0.0.2.md` for gameplay work and `12-TECH-0.0.2.md` for code.
4. Keep `10-DECISIONS-0.0.2.md` in context at all times — it wins over v0.0.1 on any conflict.

Never dump a whole document set at once.

### Before you start coding

1. **Build through `.tools\gw.cmd`, never `gradle` directly.** It pins JDK 21, sets `GRADLE_USER_HOME`/`TEMP`, starts the NeoForge mirror proxy and injects a `java.base` patch (this machine's JDK returns false from every ACL check, which makes zipfs read-only).
2. **Run the media pipeline with an explicit `-Ffmpeg`.** `tools/convert_media.ps1` already produces both Ogg Vorbis files plus the 18×18 icon. This machine's ffmpeg lives at `D:\dsh\default\sf6_audio\tools\ffmpeg.exe` (7.1 essentials with libvorbis) and is **not on PATH**; the 6.1.1 bundled with Trae CN is trimmed and has no libvorbis. The script downloads nothing (npm, Maven and the mirror are all unreachable) and exits 1 without writing anything when ffmpeg or a source is missing (D-53).
3. **Version numbers are now verified by an actual build.** `NeoForge 21.1.213` builds and runs; `tools/verify_api.ps1` re-checks the four signatures that matter if you bump the platform.
4. **The English documents are translations.** If they conflict with `docs/zh/`, the Chinese originals win.

### Scope of v0.0.1

15 modified crops across 3 types (throwable / deployable / edible) and 6 categories (trap / turret / demolition / buff / construction / damage). Conversion happens through the Crop Converter block — three slots plus an explicit button, with a nether-star catalyst for the two awakened flowers. Team allegiance is resolved through the vanilla scoreboard via a `TeamResolver` abstraction. No Mixin. Server-authoritative. Zero custom network packets.

### Scope of v0.0.2

Seven additions on top of a frozen v0.0.1: the converter finally gets a crafting recipe; Molotov! (a thrown grenade that seeds fire and magma on the impact surface); Zangief Cane (a trap that flips your view, levitates you and spins you on the way down); Compressed Hay Block; Compressed Puffed Wheat; the Wild Hunter status effect (whole-screen greyscale, red silhouettes, crosshair-snapped melee lunge, a looping 16-block theme, and kill-based extension); and Wild Apple. Still no Mixin, still server-authoritative, still zero custom network packets — client state rides on hidden `MobEffect` instances instead.

### v0.0.2 at a glance

| Item | v0.0.2 (measured) |
|---|---|
| Registry objects | **77** |
| config keys | **139** |
| Language keys | **263 × 2** (zh_cn / en_us key sets identical) |
| Resource files | **305** (JSON 197 / PNG 100 / OGG 2 / GLSL 4 / TOML 1 / `pack.mcmeta` 1) |
| Advancements | **15** |
| Tasks | **40** (F-60…F-99) across 5 phases (11: 7, 12: 9, 13: 10, 14: 8, 15: 6) |

### v0.0.3 at a glance (documented, not implemented)

| Item | v0.0.3 (documentation) |
|---|---|
| Registry objects | **90** (77 + 13) |
| config keys | **158** (139 + 19: 16 new, 3 changed defaults) |
| Language keys | **300 × 2** (263 + 37) |
| Resource files | **336** (305 + 31: JSON +22 / PNG +8 / OGG +1) |
| Advancements | **18** (15 + 3) |
| Crop profiles | **22** (19 + 3: Friend Chives, Point Defense Fungus, Vibration Bean) |
| Tasks | **36** (F-100…F-135) across 5 phases (16: 6, 17: 8, 18: 8, 19: 8, 20: 6) |
| Workload | **60.0 person-days raw / 75.0 buffered**; critical path **18.0 person-days / 11 tasks** |
| Risk register | RT-01 … **RT-23** (+RT-17 … RT-23) |

**Feed order:** `20-PROMPT-MAIN-0.0.3.md` once, then exactly one phase section from `21-PROMPT-TASKS-0.0.3.md` at a time (Phase 16 … 20). Keep `16-DECISIONS-0.0.3.md` in context — it wins over v0.0.2 on any conflict.

> ⚠️ **Phase 16 starts by fixing `tools/verify_project.py`** (F-100). It is still hardcoded for 0.0.1 and currently reports `FAIL 1`. Until it is fixed there is no trustworthy baseline for any count-based acceptance.

**Known limitations (always state these in the release notes):**

- **Greyscale covers the world only; the HUD stays coloured.** The vanilla `PostChain` runs at `GameRenderer:1027`, before GUI rendering at `:1073`; there is no reliable Mixin-free way to include the HUD (V-56 / V-61).
- **The phase-2 "look lock" only slows the camera down (~1/15.6), it does not freeze it.** `setMouseSensitivity(0)` still leaves a factor of `0.2` in `MouseHandler#turnPlayer` (V-31).
- **F11 / F2 / F3 / ESC cannot be blocked** — their vanilla handling happens inside `KeyboardHandler#keyPress`, before any NeoForge hook.
- **The 1×1 pillar landing time is short** (~23 ticks vs 40 ± 2 on a ≥3×3 platform): the player drifts off the pillar top and is caught by the pillar itself. Geometric, not a defect (V-46).
- **With Wild Hunter active, the brightness slider in the settings screen shows as full** — underwater visibility needs a temporary raise of `options.gamma()`, and the vanilla options system has no "temporary override". Known limitation; closing the screen / the effect restores both the slider and the brightness (V-78).
- **Torchflower aura: fire aspect lingers up to 15 more seconds after leaving the radius** — `TempEnchantHandler.revokeAll()` currently has **no caller**, so leaving the aura does not revoke immediately and only the 300-tick expiry does. Known limitation (acceptance is "gone within 15 seconds"); an immediate revoke would need an edge check of "inside the aura last tick, outside this tick" (V-77).

The former "`Unknown recipe category: farmstorm:convert`" warning is **fixed** in v0.0.2 (`RegisterRecipeBookCategoriesEvent#registerRecipeCategoryFinder`) and is no longer a known limitation.

---

### v0.1.0 at a glance (documented, not implemented)

| Item | v0.1.0 (documentation) |
|---|---|
| Registry objects | **93** (90 + 3: `outpost_core` block / block entity / item) |
| config keys | **166** (155 + 11 new) |
| Language keys | **315 × 2** (299 + 16) |
| Resource files | **+10** (JSON +5 / PNG +5); 6 more PNGs are replaced in place |
| Advancements | **18** (unchanged) |
| Crop profiles | **22** (unchanged, but Melon Grenade and Melon Slice Bomb lose `Demolition`) |
| Tasks | **40** (F-136…F-175) across 5 phases (21: 7, 22: 9, 23: 9, 24: 7, 25: 8) |
| Workload | **63.0 person-days raw / 78.75 buffered**; critical path **22.0 person-days / 11 tasks** |
| Risk register | RT-01 … **RT-30** (+RT-24 … RT-30) |

**Feed order:** `26-PROMPT-MAIN-0.1.0.md` once, then exactly one phase section from `27-PROMPT-TASKS-0.1.0.md` at a time (Phase 21 … 25). Keep `22-DECISIONS-0.1.0.md` in context — it wins over v0.0.4 on any conflict.

> ⚠️ **Phase 21 starts by extending `tools/verify_project.py`** (F-136). The tool is **green** today (90 / 155 / 299, `FAIL 0`), but it has a **silent blind spot**: adding IDs to `EXPECTED` without updating `EXPECTED_COUNTS` turns it red, while touching neither keeps it green and silently blind to the 3 new objects. Fixed order: add the ID list → bump the counts to 93 / 166 / 315 → add the fourth config source. The red state in between is expected.
## 素材来源与非商业声明 / Asset provenance

本项目**不是全部原创**：下列素材取自第三方，**版权归各自权利人**，本项目不对其主张任何权利。

| 产物 | 来源 | 处理 |
|---|---|---|
| `sounds/wild_hunter_loop.ogg` | `Animals_from_3m04s.mp3`（非原创音乐；作者指定取 **3m04s 起的剪辑**，47.14 s） | 转 mono / 64 kbps Ogg Vorbis（保留源采样率 48 kHz），作「狂 野 猎 人」状态期间循环播放 |
| `sounds/zangief_piledriver.ogg` | SF6 视频片段 `BV1qu411W7ha`（846–850 段） | 同上，作桑吉尔蔗触发音 |
| `textures/mob_effect/wild_hunter.png` | 作者提供的桌面 JPG | 居中裁切缩放为 18×18 PNG |
| `textures/item/*.png`、`textures/block/*.png`（含 `compressed_hay_block.png` 的灰度版与 `misc/wild_hunter_red.png` 的 1×1 红图） | **原版 Minecraft 1.21.1 客户端 jar** 的 `assets/minecraft/textures/{item,block}/*.png`（本机 `26.3-NeoForge_26.3.0.99.jar`） | 由 `tools/gen_textures.py` 用标准库 `zipfile` **直读 jar、不解包**，按需做灰度化 / 缩放到 16×16 / 右下角叠绿色「+」，产物是本模组的占位素材；**不修改原图、不在仓库内分发原版素材本体** |
| `sounds/friend_chives.ogg`（v0.0.3） | `pengyoudejiu.mp3`（非原创音乐，7.45 MB） | 转 mono / 64 kbps Ogg Vorbis，作朋友的韭命中音；**32 格**可闻 |
| `textures/mob_effect/q_bounce.png`（v0.0.3） | Steam 创意工坊 `431960/3796093604/preview.jpg`（1024×1024） | 居中裁切缩放为 18×18 PNG，作「Q弹」状态图标 |
| `textures/item/friend_chives.png`、`textures/item/chive_seeds.png`（v0.0.3） | 作者提供的两张 QQ 图片（1254×1254 RGBA） | 缩放为 16×16 PNG |
| `textures/block/friend_chives_stage0..3.png`（v0.0.3） | **本项目自绘占位**（洋红 `#D81E8A`） | 作者明确「方块材质暂不绘制，先实现功能」，这 4 张不是第三方素材，出终稿时直接替换 |
| `textures/block/outpost_core_a_{top,side,bottom}.png`（v0.1.0） | 作者提供的 `outpost_core_all_textures\32x32\a_runic\` 三张 **32×32**（2745 / 2534 / 2513 B） | 原样拷入，走 `cube_bottom_top` 六面区分；同目录另有 `b_industrial` / `c_obsidian` / `d_royal` 三套风格备选 |
| `textures/item/wild_apple.png`（v0.1.0 替换） | 作者桌面的 `2797B56FEF56AF13125F333A6989632A.png`（**1294×1216** RGBA，1,113,442 B） | 缩到 **16×16** 后替换原图 |

### 使用条件（务必照此分发）

1. **非商业**：不得将上述素材或其转换产物用于任何商业用途。
2. **署名**：转载、二次分发本模组时，必须连同本表一并保留。
3. **范围**：这条限制针对上表里**所有非原版派生的第三方素材**——两个 v0.0.2 音频、v0.0.2 效果图标，以及 v0.0.3 的 `friend_chives.ogg`、`q_bounce.png` 与两张物品贴图；原版贴图派生图**另按 Minecraft 使用条款处理**（见下），`friend_chives_stage0..3.png` 是自绘占位、模组自身的代码也按 `## 许可 / License` 一节处理。
4. **可移除**：若权利人提出异议，维护者会移除对应素材并发布替代版本；发现问题的请联系仓库维护者。

### 关于原版贴图派生图

`textures/item/*.png` 与 `textures/block/*.png` 是**从原版客户端 jar 读出来再变换**的派生图，不是原创素材，也不属于上表的三个第三方素材：

- 它们受 **Minecraft 使用条款（EULA / Brand and Asset Guidelines）** 约束，本项目不对其主张任何权利；
- 仓库里只有**变换后的小尺寸占位图**，不含原版 jar 的原始文件，也不重新分发原版素材；
- 若权利人要求，换成自绘素材即可——`tools/gen_textures.py` 的 `--out` 指向任意目录，替换后重新 `runData` 即可，不影响代码。

### 已知风险（维护者注）

本仓库目前是**私有**的，上述素材属非商业自用范围。**若将来转为公开仓库**，即使仍然非商业，第三方音乐与游戏音效的再分发在这类项目的惯例中仍存在被投诉的可能。

转公开前建议二选一：
- **替换**：换成 CC0 / 原创音源（推荐，一次性解决）；
- **保留 + 声明**：保持本节的署名与非商业条款，并接受随时可能需要撤下的风险。

> 本节由 `docs/zh/10-DECISIONS-0.0.2.md` 的 D-66 要求（「README 与 Release 说明里必须注明来源与非商业自用」）。**发布 Release 时必须把这节内容复制进 Release 说明**，因为 jar 一旦分发就无法补正说明。

---

## 许可 / License

未定 / TBD by the author。
