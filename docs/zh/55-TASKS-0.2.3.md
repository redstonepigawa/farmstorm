# 55 号 v0.2.3 任务拆分

> 依据：52 号（**D-203 ~ D-224，22 条**，唯一真相源）/ 53 号（5 条需求 R1 ~ R5 + 边界 **B-28 ~ B-58**）/ 54 号（注册面 **92** / config **175 键 / 60 push** / 语言键 **321 × 2** / 验收 **32 条** / 风险 **RT-71 ~ RT-82** / §10 差异 **15 条**）；任务 **F-286 起**、Phase **54 起**、里程碑 **M50 起**、裁决 **A-92 起**。
>
> 本文把 52 / 53 / 54 三份规格拆成 **26 个「一次可独立提交、可独立验证」的任务**，编号 **F-286 ~ F-311**，Phase **54 ~ 65**，里程碑 **M50 ~ M61**，裁决 **A-92 ~ A-101**（10 条，均为本文写作期新立）。
>
> 覆盖：54 号的 **53 行动作面**（表 A，53/53）、注册面 **92**（表 B，12 类逐类 + 口径外 3 类 + 4 个不进注册面的构件）、config **175 键 / 60 push**（表 C，删 3 加 2）、语言键 **321 × 2**（表 D，删 23 / 加 4 / 改名 2 / 改值 2）、风险 **RT-71 ~ RT-82（12 条）**（表 E）、待验证 API **14 条**（表 F）。
>
> 本文只读上游：**不改** `docs/zh/52` ~ `docs/zh/54`、不改 `docs/zh/00` ~ `51`、不改 `docs/en/*`、不改 `src/`、不改 `tools/`、不改 `.work/`；只新建本文件。

---

## 0. 阅读约定

### 0.1 任务粒度（五条判据）

一个任务 = 一次可独立提交、可独立验证的改动。五条判据沿用 43 号 §0.1（源头是 37 号 / 31 号），本版不改写、只重述：

1. **一个任务 = 一次可独立提交、可独立验证的改动。**
2. **提交后 `.tools\gw.cmd build` 退出码 0**，且不依赖同批次里尚未提交的任务。
3. **每个任务自带至少一条可观察断言**，断言里必须出现具体数值、键名、路径或日志片段。
4. **回滚该提交不会让其它已完成任务失效**（只影响依赖它的后续任务，依赖写在 §1 的「依赖」列与任务标题行）。
5. **不跨 Phase 边界**：一个任务的交付物全部落在它所属 Phase 的名称范围内。

### 0.2 复杂度与人日换算（S/M/L → 0.5/1.5/3.0）

换算口径与 43 号 §0.2 完全一致，本版不调整：

| 复杂度 | 含义 | 折算人日 |
|---|---|---|
| S | 单文件或纯数据；≤ 1 个类，或 ≤ 3 个文件里的字面量替换 | **0.5** |
| M | 2 ~ 4 个文件，跨 1 ~ 2 个包，或新增 1 个完整行为实现 | **1.5** |
| L | ≥ 5 个文件或跨 3 个包，含运行期状态机 / 序列化 / 协议 / 渲染管线 / 跨类接口改造 | **3.0** |

本版 26 个任务按此分布：**S 4 / M 16 / L 6**，折算 `4 × 0.5 + 16 × 1.5 + 6 × 3.0 = 2.0 + 24.0 + 18.0 = **44.0 人日**`（不含缓冲）。

**计时的四条口径**：

1. 凡「**编译强制**」的任务（F-287 的改名连带、F-290 的构造器收缩、F-295 的 `seed_cycle` 连带）按它实际牵动的文件数计，**不许拆**——拆开的那一半单独存在时编译不过（§0.5 死命令 1 ~ 3）。
2. 凡**验收任务**（F-309 / F-310）不按「跑一遍」估时，按 L 计：54 号 §8 的 32 条里 4 条要重生成 config、4 组必须成对跑、3 条要进游戏动手；53 号 §9 的 31 条里有 4 条是负向判据（判「没发生」）。
3. 凡「**只改注释**」（F-299 的 `_comment` 段、F-300 的 `:241-249`、F-307 的两处总数注释）不计独立 S，一律并进它所在的行为任务——注释与代码分两次提交就留下「注释说 22、代码是 17」的窗口。
4. 凡「**协议面**」（F-302 的 `Link` + 协议串）按 M 计而不是 S：一次要把 record、单条 codec、列表 codec 的兼容面、协议串四处同时押对，返工概率高于普通 M。

### 0.3 中间态窗口（本版特有，必读）

**事实**：本版的三类改动落在**三个不同的观察面**上——

- **id 面**：`FarmstormBlocks.java:71-72` / `FarmstormItems.java:112-113` / `FarmstormBlockEntities.java:43-45` 的注册 id、`tools/verify_project.py:334-343` 的 `EXPECTED` id 清单、`data/farmstorm/loot_table/blocks/crop_converter.json` 的路径；
- **计数面**：`Farmstorm.java:180-183` 的 12 个 `==`、`tools/verify_project.py:380-393` 的 `EXPECTED_COUNTS`；
- **语言键面**：`src/generated/resources/assets/farmstorm/lang/{zh_cn,en_us}.json` 的 340 → 321。

三个面的同步点被拆在三个任务上（id 面 **F-287**、语言面 **F-307**、计数面 **F-308**）。于是从「方块已改名」到「自检数字收口」之间存在一段**任何总数都不能断言**的窗口。

**窗口 = F-287（开）→ F-308（关）**。中间的分段与每段真实的计数值：

| 段 | 任务边界 | `items` | `blocks` | `menu_type` | 语言键 | 段内可断言 / 禁止断言 |
|---|---|---|---|---|---|---|
| 窗口前 | F-286 | 34 | 27 | 2 | 340 | 可断言 34 / 27 / 2 / 340；**禁写 92** |
| 段一（id 面换、计数未动） | F-287 ~ F-288 | 34 | 27 | 2 | 340 | 可断言「`crop_converter` 已在注册表」「`converter` 已不在」「工具 98/98」；**禁写 28 / 1 / 92** |
| 段二（减项一：界面） | F-289 ~ F-294 | 34 | 27 | **1** | 340 | 可断言本条对象的读数（贴图 84 张 / mcmeta 5 / 模型 78）；**禁写任何注册总数** |
| 段三（减项二：种子） | F-295 ~ F-296 | **27** | 27 | 1 | 340 | 可断言「7 个种子 id 不存在」「`#farmstorm:seeds` 成员 0」「进度 17 条」；**禁写 28** |
| 段四（增项落地） | F-297 ~ F-298 | **28** | **28** | 1 | 340 | 可断言「两个新 id 各 1 个」「`seeds` 成员 1」；**禁写 92 / 321** |
| 段五（红线与配置） | F-299 ~ F-306 | 28 | 28 | 1 | 340 | 可断言协议串 `"3"` / config 175 键 / 投入物 17 个 / 炮弹 300 刻；**禁写 92 / 321** |
| 关窗（语言） | F-307 | 28 | 28 | 1 | **321 × 2** | 可断言 321 × 2 与前缀分组；**禁写 92** |
| 关窗（计数） | F-308 | 28 | 28 | 1 | 321 × 2 | 一次写 `98 → 92` 的算式 + 工具 `92 / 92` |
| 关窗后 | F-309 ~ F-311 | 28 | 28 | 1 | 321 × 2 | 六个数全部可断言（F-311 签字） |

**段四的顺序细节**：`blocks` 在 **F-297** 到 28，`items` 在 **F-298** 才到 28；两个任务之间 `blocks = 28 / items = 27` 是**真实状态**，不许把 28 / 28 一起提前写。

**四条窗口纪律**（F-287 ~ F-307 的每个任务都适用）：

1. **不许**把 `92` 写进任何验收断言，除非该任务正是 **F-308**；也不许写 `98`，唯一例外是 **F-287**（它原样粘贴启动日志的 98，用来证明「改名不计数」）。
2. **`Farmstorm.java:180-183` 在窗口内一行不动**。从 **F-289** 起启动日志会打 `FAIL`——这是**窗口内的期望红**，各任务按「期望红」记录，不许当缺陷修，也不许提前把 98 改成 92。
3. **三个面各有一个关窗任务，不许合并**：id 面 F-287、语言面 F-307、计数面 F-308。合并任何一个都会让另一个面在没有安全网的状态下被改。
4. **窗口内的任务只写「本条对象的存在性 + 本条自己的计数」**（例：贴图目录 84 张 / 配方 21 条 / 投入物 17 个 / 标签成员 1 个）。

### 0.4 本版读数纪律（四条）

- **行号一律 UTF-8 口径**：本机默认 `Get-Content` 会把无 BOM 的 UTF-8 中文读成 GBK 并吞掉换行，行号随之全错（48 号 §0.2 第 4 条）。本文所有行号按 `[IO.File]::ReadAllLines($p, UTF8)` 复核过；与 54 号逐处不一致的地方登记在 §4 的 A-97。
- **「原版」与「本仓」分两棵树**：`net/minecraft/**` 在 `.scratch/p25a/src/`，本仓在 `src/main/java/com/dashpig/farmstorm/`。凡写「`src/` 下命中 0」的验收，**不许**写成全仓 `grep -r`（那会把反编译源算进来）。
- **数 toml 只认 `run/config/farmstorm-server.toml`**：仓里另有几十份 `run-*/config/` 旧副本，拿错一份会得出「键数不是 175」的结论。本文件数三个数：键 **175** / `push` **60** / 顶层节 **36**。
- **不写「正常」「手感」**：判据只能是五类——计数（文件数 / 键数 / 命中数）、日志行、`/data get` 读数、源码 grep 字符串、像素比对。

### 0.5 一个任务 = 一个提交 + 六条顺序死命令

这六条是**提交边界**，不是建议；违反任一条都不需要「发现 bug」才能暴露——它们各自有一个当场可见的坏结果。

| # | 死命令 | 落点任务 | 违反后的当场后果 |
|---|---|---|---|
| 1 | 改名的**全部编译期引用**必须与注册改名同一提交（`FarmstormBlocks.CONVERTER` / `ConverterBlock` / 两条标签 / 进度判据 / 两个 datagen provider） | **F-287** | 编译不过；漏一个 provider 则 datagen 抛 `IllegalStateException` |
| 2 | `ConverterRecipe` 的字段收缩必须与 `ModRecipeProvider` 的两个助手同一提交 | **F-290** | 7 个实参喂 2 个形参 ⇒ 编译不过 |
| 3 | 删 7 个种子物品必须与删 `seed_cycle` 同一提交 | **F-295** | `ModAdvancementProvider:96-104` 引用已删物品，编译不过；产物侧 `BuiltInRegistries.ITEM.get(key)` 拿 `null` 会让 datagen 崩 |
| 4 | 删 `SeedItem.java` 必须在「7 个种子删完」∧「`profileOf` 的 `instanceof SeedItem` 消失」之后 | **F-299**（依赖 F-295） | 早删 ⇒ `CultivatorBlockEntity:161` 与 `FarmstormItems:175-177` 引用不到类，编译不过 |
| 5 | `Link` 加字段必须与 `PROTOCOL_VERSION` `"2"` → `"3"` 同一提交 | **F-302** | 混装环境下协商通过、随后按旧 codec 解新字节流 ⇒ 客户端**必崩** |
| 6 | `Kind` 的三个广播落点（`ORDER` / `fingerprint` / 组装）必须与渲染换色同一提交 | **F-304** | 「kind 变了不重发」⇒ 拆了培养器换转换器后那条线永远青色，而服务端看起来一切正常 |

---

## 1. 任务总表（八列）

| 任务号 | 名称 | Phase | 里程碑 | 复杂度 | 人日 | 依赖 | 一句话交付物 |
|---|---|---|---|---|---|---|---|
| F-286 | 版本号 0.2.3 与 6 处版本串 | 54 | M50 | S | 0.5 | — | `mod_version=0.2.3` + 两支 `.cmd` 的 jar 名同步 |
| F-287 | 三处注册改名 + 两个类改名 + 编译期引用（**开窗**） | 55 | M51 | L | 3.0 | — | 全仓只剩 `crop_converter`，工具仍 98 / 98 |
| F-288 | 铁砧七项属性 + 进 `processing_block` 标签 | 55 | M51 | M | 1.5 | F-287 | 硬度 5.0 / 抗性 1200 / 标签成员 1 → 2 |
| F-289 | 删界面三件套 + `MenuType` + finder 内联 | 55 | M51 | M | 1.5 | F-287 | Java 文件 154 → 151，`menu_types` 2 → 1 |
| F-290 | `ConverterRecipe` 七字段 → 二字段 + 序列化器 | 56 | M52 | M | 1.5 | — | 配方 JSON 只剩 `input` / `result` |
| F-291 | 配方表 26 → 21 + 转换器合成配方替换 | 56 | M52 | M | 1.5 | F-290 | 21 条单向无成本 + 6 铁 2 玻璃 1 红石 |
| F-292 | `CropConverterBlock` 行为 + 8 个 variant + 两张模型 | 57 | M53 | L | 3.0 | F-288 | `FACING × WORKING` 八态、工作才动 |
| F-293 | `CropConverterBlockEntity` 四步状态机重写 | 57 | M53 | L | 3.0 | F-292、F-289、F-290 | 单槽 + 进度 + 绑定核 + 自动投递 |
| F-294 | 转换器的贴图面：7 新增 + 3 mcmeta + 7 删除 + gui 清空 | 57 | M53 | M | 1.5 | F-292 | 方块 png 84 / mcmeta 5 / gui 0 |
| F-295 | 删 7 个种子 + 档案置空 + 战利品表 + `seed_cycle` + 6 张图 | 58 | M54 | M | 1.5 | — | `items` 34 → 27，item 贴图 28 → 22 |
| F-296 | 删 `nether_star_toll` 与进度面收口 | 58 | M54 | S | 0.5 | F-295 | 进度 18 → 16（17 条在 F-295 已到） |
| F-297 | `PlainCropBlock` + `plain_chives_crop` + 4 阶段 blockstate | 59 | M55 | M | 1.5 | F-287、F-295 | 平凡韭菜能种，`blocks` 27 → 28 |
| F-298 | `PlainSeedItem` + `plain_chives_seeds` + 标签 + 模型分派 | 59 | M55 | M | 1.5 | F-297 | 可播种种子，`items` 27 → 28 |
| F-299 | 培养器准入判据反转 + 删 `SeedItem.java` | 60 | M56 | M | 1.5 | F-291、F-295 | 收 17 个投入物，改良作物一个不收 |
| F-300 | 培养器产出两组 + `hasOwnSeed` 改写 | 60 | M56 | M | 1.5 | F-299、F-298 | 2 + 2；自身即种子的 11 条给 4 个 |
| F-301 | 培养器合成配方 4 铁 + 4 玻璃 + 1 红石 | 60 | M56 | S | 0.5 | F-291 | `cultivator.json` 首次存在 |
| F-302 | `Link.Kind` + 单条 codec + `PROTOCOL_VERSION` `"3"` | 61 | M57 | M | 1.5 | — | 每条线多 1 字节，包数仍 2 |
| F-303 | 核侧 `CultivatorKinds` + 三写入点 + `rebindNearby` 扩两种机器 | 61 | M57 | L | 3.0 | F-302、F-292、F-293 | 0.2.2 存档无损，先放机器后放核也能绑 |
| F-304 | 广播三处 + 渲染按 `Kind` 选色 | 61 | M57 | M | 1.5 | F-303 | 培养器青不变，转换器红（0.92 / 0.22 / 0.22） |
| F-305 | config 删 3 加 2 | 62 | M58 | M | 1.5 | F-293 | 175 键 / 60 push / 顶层节 36 |
| F-306 | `TurretBullet` 出膛 300 刻后删除 | 62 | M58 | S | 0.5 | F-305 | 15 秒后静默 `discard()` |
| F-307 | 语言键 340 → 321 × 2 + 两处类注释（**关语言面**） | 63 | M59 | M | 1.5 | F-289、F-295、F-296、F-305 | 321 × 2，前缀分组六段 |
| F-308 | 自检收口 92 / 28 / 28 / 1 + 工具计数（**关计数面**） | 63 | M59 | M | 1.5 | F-287 ~ F-307 | 日志 `92 … OK`、脚本 `92 / 92` |
| F-309 | 54 号 §8 的 **32 条**验收全量执行 | 64 | M60 | L | 3.0 | F-308 | `.work/v023-verify/acceptance.md` |
| F-310 | 53 号 §9 的 **31 条**边界复跑 | 64 | M60 | L | 3.0 | F-309 | `.work/v023-verify/boundaries.md` |
| F-311 | 红线、注册面、19 项必改与 README 签字 | 65 | M61 | M | 1.5 | F-309、F-310 | `.work/v023-verify/signoff.md` |

任务数：`311 − 286 + 1 = 26`，连续无跳号；Phase **12** 个（54 ~ 65）；里程碑 **12** 个（M50 ~ M61），一一对应。

---

## 2. Phase 54 ~ 65 的任务详情

每个任务固定五节：**目标 / 交付物（文件:行号）/ 验收断言 / 依赖 / 回滚影响**。依赖写在标题行，`依赖 —` 表示无前驱。

### Phase 54 · 发布面（F-286）

阶段目标：把版本号与两支构建脚本的产物面钉死——它是全部 jar 名与「检查环境」判据的唯一来源。出口判据：`farmstorm-0.2.3+mc1.21.1.jar` 产出，`0.2.2` 在 6 处字面量里归 0。
#### F-286 · 版本号 0.2.3 与 6 处版本串（S · 0.5 人日｜依赖 —）

- **目标**：把 `mod_version` 改成 **0.2.3**，并让两支 `.cmd` 里写死的 jar 名与抬头行跟着改，使「一键构建」的复制步骤与「检查环境」的存在性检查仍指向真实产物（52 号 D-203）。
- **交付物**：`gradle.properties:11`；`一键构建.cmd:5`（抬头）、`:11`、`:12`（jar / sources-jar）；`检查环境.cmd:5`（抬头）、`:14`（jar）。共 **6 处字面量**。
- **验收断言**：
  1. `.tools\gw.cmd build` 退出码 0；`build/libs/` 下有 `farmstorm-0.2.3+mc1.21.1.jar` 与 `farmstorm-0.2.3+mc1.21.1-sources.jar`，`farmstorm-0.2.2*` 残留 **0** 个。
  2. `0.2.3` 在三个文件里的命中数 = **6**；`0.2.2` 在这三个文件里命中 **0**。
  3. `jar tf build/libs/farmstorm-0.2.3+mc1.21.1.jar` 含 `META-INF/neoforge.mods.toml`，其 `version` 解析为 `0.2.3`。
- **依赖**：—。
- **回滚影响**：只影响产物名与两支脚本的横幅；回滚后 F-311 签字里的 jar 名读数失效，其余 25 个任务不受影响。

### Phase 55 · 转换器身份：改名、属性与界面下线（F-287 ~ F-289）

阶段目标：把 `farmstorm:converter` 换成 `farmstorm:crop_converter`（三个注册点 + 两个类名），把属性整段换成铁砧七项，把界面整套下线。出口判据：全仓不再有 `converter` 这个注册 id、`menu_types` 为 1、Java 文件 151。
#### F-287 · 三处注册改名 + 两个类改名 + 编译期引用（**开窗**）（L · 3.0 人日｜依赖 —）

- **目标**：把方块、方块物品、方块实体三处注册 id 从 `converter` 改成 `crop_converter`（52 号 D-206），方块类 `ConverterBlock` → `CropConverterBlock`，并在**同一提交**里把全部编译期引用改到新名——本任务只改名，不动行为、不动属性。
- **交付物**：`FarmstormBlocks.java:71-72`（常量名 `CONVERTER` → `CROP_CONVERTER`、id 字面量、`CropConverterBlock::new`）；`block/ConverterBlock.java` → `block/CropConverterBlock.java`（类名 / 构造器 / `simpleCodec` 同步，`:83-87` 的 `useWithoutItem` 与 `:70-76` 的 `getMenuProvider` **一行不动**）；`FarmstormItems.java:112-113`；`FarmstormBlockEntities.java:43-45`（**id 与常量名改，`ConverterBlockEntity::new` 保留到 F-293**）；`datagen/ModBlockTagsProvider.java:48`；`datagen/ModAdvancementProvider.java:82,84`（进度 id 字面量 `farmstorm:converter` **不改**）；`datagen/ModItemModelProvider.java:69`；`datagen/ModBlockStateProvider.java:181`；`tools/verify_project.py:334-343` 的 `EXPECTED` id 清单（`items` 与 `blocks` 两组各换 1 条）。
- **验收断言**：
  1. 启动日志整行 `自检 注册对象 98 个：items 34 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`——**改名不计数**，98 与 `OK` 都在；这条是本任务唯一允许出现的总数，且必须原样粘贴日志。
  2. `python tools/verify_project.py` 仍打印 `已核 registry 对象: 98 / 98`、`结论：FAIL 0 项`；`EXPECTED["items"]` 与 `EXPECTED["blocks"]` 里 `crop_converter` 各 **1** 处、`converter` 各 **0** 处。
  3. `src/main/java` 下 `ConverterBlock` 命中 **0**（`ConverterBlockEntity` 是另一个类，仍在）；`FarmstormBlocks.CONVERTER` 命中 **0**；`"converter"` 的命中只剩 `ModAdvancementProvider` 的进度 id 与注释里的旧名，共 **≤ 3** 行。
  4. Java 文件数仍 **154**（改名不计数）；`.tools\gw.cmd build` 退出码 0（改名连带一次到位的证据是「它能编过」）。
- **依赖**：—。
- **回滚影响**：回滚到旧 id 后，老存档里已放置的 `crop_converter` 变缺失方块（52 号 D-206 的既定价）；`EXPECTED` 的 id 清单必须同批回滚，否则工具红。**窗口在本任务打开**（§0.3），回滚即关窗，后续 20 个任务的窗口纪律随之失效。
#### F-288 · 铁砧七项属性 + 进 `processing_block` 标签（M · 1.5 人日｜依赖 F-287）

- **目标**：把方块属性从 `MapColor.STONE` + `strength(3.5F)` 整段换成铁砧七项（52 号 D-207、54 号 §2.1.1），并把 `crop_converter` 加进 `farmstorm:processing_block`（成员 1 → 2），使它能被据点核心认成可绑定方块。
- **交付物**：`FarmstormBlocks.java:71-72`（`MapColor.METAL` / `strength(5.0F, 1200.0F)` / `requiresCorrectToolForDrops` / `SoundType.ANVIL` / `PushReaction.BLOCK` 五项 + 两个取值）；`datagen/ModBlockTagsProvider.java:44-45`（`tag(FarmstormTags.PROCESSING_BLOCK).add(...)` 加第二个成员）；产物 `data/farmstorm/tags/block/processing_block.json`、`data/farmstorm/tags/block/mineable_with_pickaxe.json`。
- **验收断言**：
  1. `FarmstormBlocks.java:71-72` 的注册行含 `MapColor.METAL` / `strength(5.0F, 1200.0F)` / `requiresCorrectToolForDrops` / `SoundType.ANVIL` / `PushReaction.BLOCK` **五项全中**；`3.5F` 与该文件里的 `MapColor.STONE` 在 `crop_converter` 那两行命中 **0**（培养器那两行不算）。
  2. `data/farmstorm/tags/block/processing_block.json` 的 `values` 恰 **2** 项：`farmstorm:cultivator` 与 `farmstorm:crop_converter`；`mineable_with_pickaxe.json` 含 `farmstorm:crop_converter`。
  3. 游戏内三条读数：空手挖 ⇒ **不掉落**（`requiresCorrectToolForDrops`）；木镐挖 ⇒ 掉本体；TNT 贴脸炸 ⇒ 方块**仍在**（抗性 1200.0）。
- **依赖**：F-287。
- **回滚影响**：回滚到 3.5F / STONE 后，`mineable_with_pickaxe` 的成员仍是新名（F-287 已改），不产生编译问题；但「抗性 1200」这条验收随之失效，且 F-303 的绑定实测会退化成「一炸就没」。
#### F-289 · 删界面三件套 + `MenuType` + finder 内联（M · 1.5 人日｜依赖 F-287）

- **目标**：把转换器的界面整套下线：删 `ConverterMenu` / `ConverterScreen` / `ConverterRecipeBookCategory` 三个文件、删 `MenuType`（`menu_type` 2 → 1）、删 `getMenuProvider`，并把配方书 finder **内联**进 `FarmstormClient`（52 号 D-208、54 号 §2.6）。
- **交付物**：整文件删 `menu/ConverterMenu.java`（216 行）、`client/ConverterScreen.java`（86 行）、`client/ConverterRecipeBookCategory.java`（24 行）；`FarmstormMenus.java:12`（类注释「2 个 MenuType」→ 1）、`:17-18`（整段删）；`FarmstormClient.java:62`（屏幕注册行）、`:73-76`（handler 体改一行内联 `event.registerRecipeCategoryFinder(FarmstormRecipes.CONVERTING_TYPE.get(), holder -> RecipeBookCategories.UNKNOWN);`）；`CropConverterBlock` 的 `getMenuProvider` 整段删。
- **验收断言**：
  1. Java 文件数 154 → **151**（−3）；三个被删路径都不存在；`FarmstormMenus` 里 `REGISTER.register(` 恰 **1** 行（`outpost_core`），`"converter"` 命中 **0**。
  2. 启动日志的 `menu_types 1`，整行变 `→ FAIL`——**这是窗口内的期望红**（§0.3 纪律 2），记录方式写「期望红：`menu_types` 已 1，断言仍 2，F-308 收口」，不许当缺陷修。
  3. `FarmstormClient.java` 里 `registerRecipeCategoryFinder(` = **1** 处、`ConverterRecipeBookCategory` 命中 **0**、`ConverterScreen` 命中 **0**；客户端启动后 `run/logs/latest.log` 里 `Unknown recipe category` 命中 **0**（21 条 `converting` 配方不再刷 WARN）。
  4. `CropConverterBlock` 里 `getMenuProvider` 命中 **0**、`MenuProvider` import 命中 **0**；`ConverterBlockEntity` 仍 `implements Container`（本任务不碰它，留给 F-293）。
- **依赖**：F-287。
- **回滚影响**：回滚即恢复三个文件与 `menu_type 2`；`ConverterBlockEntity` 的双槽容器逻辑随之复活，但 F-293 的重写会与它冲突（必须先回滚 F-293 再回滚本任务）。

### Phase 56 · 配方结构与配方表（F-290 ~ F-291）

阶段目标：把 `farmstorm:converting` 从「7 字段双向收成本」压成「2 字段单向无成本」，并把 26 条压到 21 条。出口判据：21 条 JSON 只有 `input` / `result`、无 `NETHER_STAR`、转换器配方是 6 铁 2 玻璃 1 红石。
#### F-290 · `ConverterRecipe` 七字段 → 二字段 + 序列化器（M · 1.5 人日｜依赖 —）

- **目标**：把 `ConverterRecipe` 的七个字段压到 `input` / `result` 两个，删 `reverse()`，同步 `ConverterRecipeSerializer` 的 `MapCodec` 与 `StreamCodec`，并把 `ModRecipeProvider` 的两个助手从 7 参改 2 参单向（52 号 D-209、54 号 §4.2.1）。**配方条数不动**（仍 26）。
- **交付物**：`recipe/ConverterRecipe.java`（`:19-25` 七字段 → 2；`:27-28` 构造器 7 参 → 2 参；`:106-110` `reverse()` 删）；`recipe/ConverterRecipeSerializer.java`（7 字段 `MapCodec` → 2；手写 `StreamCodec` 只剩两段）；`datagen/ModRecipeProvider.java:187-196`（`twoWay` → `oneWay`，2 参）、`:199-209`（`oneWayWithNetherStar` → 并进 `oneWay`）、`:67-99` 的 26 行调用逐行改成 2 参。
- **验收断言**：
  1. `ConverterRecipe.java` 里 `xpCost` / `catalyst` / `catalystCount` / `bidirectional` / `reverseXpCost` / `reverse(` 全部命中 **0**；`public ConverterRecipe(` 恰 **1** 处、形参 **2** 个。
  2. `grep -c 'new ConverterRecipe(' src/main/java` = **26**（条数不变，本任务只改形状）；`NETHER_STAR` 在 `ModRecipeProvider.java` 命中 **0**。
  3. 产物侧：`data/farmstorm/recipe/` 里 `"type": "farmstorm:converting"` 的 json 仍 **26** 个，每个只有 `input` / `result` 两个字段、无 `xp_cost` / `catalyst`；启动日志与 `/reload` 后都没有 `Couldn't parse recipe`。
  4. `gw.cmd build` 退出码 0（字段与调用点同批到位的证据）。
- **依赖**：—。
- **回滚影响**：回滚即恢复 7 字段与 26 条双向配方；26 个 JSON 需重新 datagen（旧字段版本会被新 codec 读失败），F-291 必须一并回滚。
#### F-291 · 配方表 26 → 21 + 转换器合成配方替换（M · 1.5 人日｜依赖 F-290）

- **目标**：删 6 条种子转换配方（26 → 20）、新增「平凡韭菜 → 朋友的韭」（20 → 21），并把转换器的合成配方从「8 个燃料围 1 个工作台」换成 **6 铁锭 + 2 玻璃 + 1 红石粉**（52 号 D-209、D-217、D-206）。
- **交付物**：`datagen/ModRecipeProvider.java:86-92`（6 条种子 `twoWay` 整段删）、`:97` 之后新增 `oneWay(recipeOutput, "friend_chives", FarmstormItems.PLAIN_CHIVES.get(), FarmstormItems.FRIEND_CHIVES)`、`:60-61` 的 `converter(...)` 方法体换成 `铁 铁 铁 / 玻璃 红石 玻璃 / 铁 铁 铁`、配方 id 改 `farmstorm:crop_converter`。
- **验收断言**：
  1. `data/farmstorm/recipe/` 里 `"type": "farmstorm:converting"` 的 json = **21**；`puffed_wheat_seeds.json` / `power_beet_seeds.json` / `melon_grenade_seeds.json` / `pumpkin_turret_seeds.json` / `awakened_torchflower_seeds.json` / `awakened_pitcher_plant_pod.json` 六个文件名全部不存在。
  2. `friend_chives.json` 存在且 `"type"` = `farmstorm:converting`、`input` 侧物品 = `farmstorm:plain_chives`；两条史诗配方（`awakened_torchflower` / `awakened_pitcher_plant`）仍在 21 条里且 `catalyst` 字段**不存在**。
  3. `crop_converter.json` 的材料计数：`minecraft:iron_ingot` **6**、`minecraft:glass` **2**、`minecraft:redstone` **1**；配方 id 的字面量是 `farmstorm:crop_converter`。
  4. `data/farmstorm/recipe/` 的文件总数 = **24**（21 条 `converting` + `crop_converter` / `outpost_core` / `compressed_puffed_wheat`）；F-301 之后才到 25。
- **依赖**：F-290。
- **回滚影响**：回滚即恢复 26 条与旧合成配方；`friend_chives` 回归「唯一没有转换配方的作物」（52 号 D-217 的补充），F-299 的 17 个投入物算式退回 16。

### Phase 57 · 转换器行为与资源面（F-292 ~ F-294）

阶段目标：把改过名的方块从「装饰方块」变成真正会工作的机器，并给足贴图与模型。出口判据：8 个 variant、四步状态机、`working` 只在有货且可用时为真、贴图 84 张 + 5 个 mcmeta。
#### F-292 · `CropConverterBlock` 行为 + 8 个 variant + 两张模型（L · 3.0 人日｜依赖 F-288）

- **目标**：给方块加 `WORKING` 属性、`getTicker`、`setPlacedBy`、`onRemove`，把 `useWithoutItem` 从无条件 `PASS` 改成「客户端 `SUCCESS` + 服务端四闸合并判定」，并把 blockstate 生成器重写成 `FACING × WORKING` 的 **8 个 variant**、两张模型（52 号 D-208、D-211；54 号 §4.1.1、§3.2）。
- **交付物**：`block/CropConverterBlock.java`（照 `CultivatorBlock:44` 的 `WORKING`、`:59-61` 的 `RenderShape.MODEL`、`:86-102` 的 `setPlacedBy`、`:113-128` 的 `useWithoutItem`、`:140-147` 的 `onRemove`、`:150-161` 的 `getTicker` 逐节写）；`datagen/ModBlockStateProvider.java:160-166`（`CONVERTER_FACE_TEXTURES` 换成 7 个新文件）、`:172-184`（`converterStates` → `cropConverterStates`，8 variant）。
- **验收断言**：
  1. `blockstates/crop_converter.json` 的 variant = **8**：`working=false` 指 `farmstorm:block/crop_converter`、`working=true` 指 `farmstorm:block/crop_converter_working`；四个 `y` 依次 `0 / 90 / 180 / 270`；`uvlock` 全为 `true`。
  2. `models/block/crop_converter.json` 与 `crop_converter_working.json` 的 `parent` 都是 `minecraft:block/cube`，六个面齐全：`north` = `_front_still` / `_front`，`up` = `_top_still` / `_top`，`down` = `_bottom`（两态同值），`south` / `east` / `west` = `_side_still` / `_side`；`particle` 取 `_front_still` / `_front`。
  3. 源码：`CropConverterBlock` 里 `WORKING` 声明 **1** 处、`getRenderShape` **1** 处、`getTicker` **1** 处、`setPlacedBy` **1** 处、`onRemove` **1** 处、`getMenuProvider` **0** 处；`BlockStateProperties.HORIZONTAL_FACING` 引用 **1** 处。
  4. 游戏内：空手右键（槽空、无绑定）⇒ **无反应且手持物不消耗**；有绑定且槽空时手持小麦右键 ⇒ 手上少 **1** 个、方块状态变 `working=true`、模型切到 `crop_converter_working`。
- **依赖**：F-288。
- **回滚影响**：回滚即回到「单态 4 variant + 右键 PASS」的装饰方块；F-293 的状态机失去方块侧的 ticker 与属性，必须一并回滚；F-303 的 `setPlacedBy` 写入点也随之中断。
#### F-293 · `CropConverterBlockEntity` 四步状态机重写（L · 3.0 人日｜依赖 F-292、F-289、F-290）

- **目标**：把 435 行的双槽容器重写成「单槽 + 进度 + 绑定核」的三个字段与四步状态机，**不再 `implements Container`**，产出走核侧 `acceptAll`，并按 54 号 §4.1.6 把 `first_conversion` 的授予点搬进 `deliver()` 成功支（52 号 D-208）。
- **交付物**：`blockentity/ConverterBlockEntity.java` → `blockentity/CropConverterBlockEntity.java`（整文件重写：字段 `held` / `progress` / `corePos` 三个 NBT 键 `Held` / `Progress` / `Core`；四步 `serverTick`；`canInsert` 四闸；`releaseHeld`；`deliver`）；`FarmstormBlockEntities.java:45`（`ConverterBlockEntity::new` → `CropConverterBlockEntity::new`）；授予点落 `deliver()` 成功支，读 `OutpostCoreBlockEntity#owner()`。
- **验收断言**：
  1. 类里 `implements Container` 命中 **0**、`ContainerData` 命中 **0**、`getMenuProvider` 命中 **0**；NBT 键恰 **3** 个（`Held` / `Progress` / `Core`）。
  2. 状态机四步可读：`serverTick` 内 `progress <` / `progress ==` 两个比较各 **1** 处；`acceptAll(` 调用 **1** 处，且清槽语句在 `acceptAll` 返回 `true` 之后（顺序可 `grep -n` 证）。
  3. 行为（核有空位）：放核 → 放转换器 → 手持小麦右键 ⇒ 第 **600** 刻核里多 **1** 个 `farmstorm:puffed_wheat`，槽清空、`working` 回 `false`；把 `crop_converter.processTicks` 改成 20 后重跑 ⇒ 第 **20** 刻产出（证明读 config 而不是写死）。
  4. 核满：把核的 54 格塞满后投料、等 **700** 刻 ⇒ 不投递、`working=false`、槽里小麦仍在、`progress` 停在 600；腾出 1 格后 **下一 tick** 完成投递（不重新计时）。
  5. 拆除：工作中挖掉方块 ⇒ 掉出**那 1 个**输入物，`progress` 归零，产物不掉地。
- **依赖**：F-292、F-289、F-290。
- **回滚影响**：回滚即恢复双槽容器与 `Container` 契约（漏斗又能塞进去）；`first_conversion` 退回「无授予点」的不可达状态，F-296 的进度面验收会假红。
#### F-294 · 转换器的贴图面：7 新增 + 3 mcmeta + 7 删除 + gui 清空（M · 1.5 人日｜依赖 F-292）

- **目标**：把新机器的贴图包落位（7 张 png + 3 个 mcmeta），删掉旧转换器的 **7 张**方块贴图、1 张死物品图与 1 张界面图（54 号 §3.1、§3.4、§3.6 第 8 行、§3.7；52 号 D-211）。
- **交付物**：新增 `assets/farmstorm/textures/block/crop_converter_a_{front,front_still,side,side_still,top,top_still,bottom}.png` + 三个 `.mcmeta`（`{"animation":{"frametime":2}}`，只给 `_front` / `_side` / `_top`）；删 `converter_front` / `converter_side` / `converter_side_{wheat,carrot,potato,beetroot}` / `converter_top` 七张；删 `textures/item/converter.png`（死文件）与 `textures/gui/converter.png`。
- **验收断言**：
  1. 目录计数：`textures/block/*.png` = **84**、`*.mcmeta` = **5**、`textures/item/*.png` = **28**（本任务只删 `converter.png`，种子的 7 张归 F-295）、`textures/gui/*.png` = **0**。
  2. `converter_*.png` 在 `textures/block/` 下命中 **0**；`converter.png` 在 `textures/item/` 与 `textures/gui/` 下各命中 **0**。
  3. 三个 mcmeta 的 JSON 与 `{"animation":{"frametime":2}}` 逐字相同；7 张新 png 的 IHDR 尺寸一致（同一贴图包切出，宽高相等）。
  4. 游戏内：`F3` 看该方块，空闲时正面是静态图；把 `processTicks` 改小后投料 ⇒ 正面开始逐帧变化（16 帧 / 32 刻一循环）。
- **依赖**：F-292。
- **回滚影响**：回滚即恢复 7 张旧贴图与 2 个 mcmeta，但 `cropConverterStates` 仍在引用 7 个新文件名 ⇒ 模型丢贴图变紫黑格；因此回滚本任务必须同时回滚 F-292 的 blockstate。

### Phase 58 · 删种子与进度（F-295 ~ F-296）

阶段目标：把 7 个种子物品与它们的全部引用面清干净，并把依赖种子的进度删掉。出口判据：`#farmstorm:seeds` 成员 0、`items` 27、进度 17 条、`FarmstormCropBlock` 的剪刀掉落为 `Items.AIR`。
#### F-295 · 删 7 个种子 + 档案置空 + 战利品表 + `seed_cycle` + 6 张图（M · 1.5 人日｜依赖 —）

- **目标**：删掉 7 个种子物品，把 22 条档案的 `seedItem` 一律置 `null`，作物战利品表去掉种子条目，删 `seed_cycle` 进度，并清掉 6 张种子贴图、把 `chive_seeds.png` **重命名**成 `plain_chives_seeds.png`（52 号 D-212、D-214；§0.5 死命令 3）。
- **交付物**：`FarmstormItems.java:91`（`CHIVE_SEEDS`）、`:102-109`（6 条）、`:35`（`SEED_ITEMS` 列表删）、`:175-177`（`seedItem(...)` 工厂删）、`:7`（`SeedItem` import 删）、`:140-151`（创造栏不再 `addAll(SEED_ITEMS)`）；`util/CropProfile.java`（7 处显式种子路径 `:51,60,67,70,74,77,116` + 15 条自指，全部改 `null`）；`datagen/ModLootTableProvider.java:71,81,84,86,89,91,105`（7 处种子参数删，作物本体掉落不动）；`datagen/ModAdvancementProvider.java:95-106`（`seed_cycle` 整段删）；`datagen/ModItemTagsProvider.java:42-48`（`#farmstorm:seeds` 成员 6 → 0）；`textures/item/` 删 6 张、`chive_seeds.png` → `plain_chives_seeds.png`；`tools/verify_project.py:334-343` 的 `EXPECTED["items"]` 删 7 条。
- **验收断言**：
  1. 7 个种子 id 在 `data/farmstorm/` 与 `src/generated/` 下的产物里命中 **0**；`src/main/java` 里 `puffed_wheat_seeds` / `power_beet_seeds` / `melon_grenade_seeds` / `pumpkin_turret_seeds` / `awakened_torchflower_seeds` / `awakened_pitcher_plant_pod` / `chive_seeds` 七个串各自命中 **0**。
  2. `data/farmstorm/tags/item/seeds.json` 存在且 `values` 是**空数组**（`6 − 6 = 0`）；`data/farmstorm/tags/item/converter_fuel.json` **仍存在**（本任务不动它，F-311 的「不改清单」要逐字证）。
  3. `textures/item/*.png` = **22**（28 − 6，重命名不计）；`plain_chives_seeds.png` 存在、`chive_seeds.png` 不存在；`data/farmstorm/loot_table/blocks/friend_chives_crop.json` 里 `farmstorm:chive_seeds` 命中 **0**。
  4. `data/farmstorm/advancement/` 的 json = **17**（18 − 1）；`seed_cycle.json` 不存在；`FarmstormCropBlock:161-163` 的空分支保留，游戏内对成熟作物用剪刀 ⇒ 掉落 **0** 个种子（`getBaseSeedId()` 返回 `Items.AIR`）。
  5. 启动日志的 `items 27`，整行 `→ FAIL`——**期望红**（§0.3 段三），记录方式写「期望红：`items` 已 27，断言仍 34，F-308 收口」。
- **依赖**：—。
- **回滚影响**：回滚即恢复 7 个种子与 `seed_cycle`；`plain_chives_seeds.png` 已改名，回滚时必须改回 `chive_seeds.png`，否则 F-298 的新物品丢贴图。F-299 的「删 `SeedItem.java`」依赖本任务，回滚会连带它。
#### F-296 · 删 `nether_star_toll` 与进度面收口（S · 0.5 人日｜依赖 F-295）

- **目标**：删掉 `nether_star_toll`（门槛随下界之星一起没了，52 号 D-214 第 2 条），把进度面收到 **16** 条（52 号 D-214）。
- **交付物**：`datagen/ModAdvancementProvider.java:91-93`（`nether_star_toll` 整段删）；产物 `data/farmstorm/advancement/nether_star_toll.json` 删。
- **验收断言**：
  1. `data/farmstorm/advancement/` 的 json = **16**（`18 − 2`）；`seed_cycle.json` 与 `nether_star_toll.json` 都不存在。
  2. `farmstorm:converter`（放置进度）**仍在**且判据是 `crop_converter`（F-287 已改）；`farmstorm:first_conversion` **仍在**，其授予点在 `CropConverterBlockEntity#deliver` 的成功支（F-293 已搬）。
  3. `nether_star_toll` 与 `seed_cycle` 两个 id 在 `src/main/java` 与 `src/generated/` 下命中 **0**（删掉的 id 不复用，52 号 D-214 第 3 条）。
- **依赖**：F-295。
- **回滚影响**：回滚只恢复一条 `impossible` 进度（它的达成条件已不存在，会变成永远拿不到的进度格）；不影响其它任务。

### Phase 59 · 平凡作物（F-297 ~ F-298）

阶段目标：把平凡韭菜从「一个食物物品」升级成可种植作物：一个方块类、一个种子类、4 阶段 blockstate、1 张掉落表。出口判据：种子能种下去、能长到 `age=3`、破坏掉 1 个平凡韭菜 + 0~3 个种子。
#### F-297 · `PlainCropBlock` + `plain_chives_crop` + 4 阶段 blockstate（M · 1.5 人日｜依赖 F-287、F-295）

- **目标**：新建 `PlainCropBlock extends CropBlock`（不接 `CropProfile`）并注册 `farmstorm:plain_chives_crop`，blockstate 出 4 个阶段、贴图**复用**朋友的韭 4 张 stage 图，掉落表新增一张（52 号 D-215、裁定 #18；54 号 §3.5、§4.3.4）。
- **交付物**：新建 `block/PlainCropBlock.java`（`age 0~3`、四个碰撞箱照 `FarmstormCropBlock:48-53`、生长读同一组 `growth.*` config）；`FarmstormBlocks.java:102-104` 之间插入注册行（属性照 `crop(...)` 工厂的四项）；`datagen/ModBlockStateProvider.java` 新增 `plainCropStates(...)` 与 `instanceof PlainCropBlock` 分支（放在 `FarmstormCropBlock` 分支之后）；`datagen/ModLootTableProvider.java` 新增 `plain_chives_crop` 一张表。
- **验收断言**：
  1. `blockstates/plain_chives_crop.json` 恰 **4** 个 variant（`age=0..3`），每个指向 `farmstorm:block/plain_chives_stageN`；`models/block/plain_chives_stage0..3.json` 四个文件存在，`textures.crop` = `farmstorm:block/friend_chives_stage0..3`（**不是** `plain_chives_stageN`）。
  2. `textures/block/*.png` 仍 **84**（复用朋友的韭 4 张，**新增 0 张**）；`textures/block/plain_chives_stage0.png` 不存在。
  3. Java 文件数 151 → **152**；`blocks` 的注册数 27 → **28**（`PlainCropBlock` 不在 `CropProfile.ALL` 里：`CropProfile` 的档案数仍 **22**）。
  4. 游戏内：`/setblock` 放 `plain_chives_crop[age=3]` ⇒ 模型是十字作物（不是六面立方块）；破坏 ⇒ 掉 **1** 个 `farmstorm:plain_chives` + **0~3** 个 `plain_chives_seeds`。
- **依赖**：F-287、F-295。
- **回滚影响**：回滚即删掉方块与 4 个模型；F-298 的种子没有可放置的方块，必须一并回滚；F-300 的「17 个投入物」算式不受影响（平凡韭菜物品早在 0.2.2 就在）。
#### F-298 · `PlainSeedItem` + `plain_chives_seeds` + 标签 + 模型分派（M · 1.5 人日｜依赖 F-297）

- **目标**：新建 `PlainSeedItem extends ItemNameBlockItem`（`useOn` 走原版放置流程，**不许**复用 `SeedItem`），注册 `farmstorm:plain_chives_seeds`，进创造栏与 `#farmstorm:seeds` 标签，并在 `ModItemModelProvider` 里给它 `basicItem` 分派（52 号 D-216、红线 2）。
- **交付物**：新建 `item/PlainSeedItem.java`；`FarmstormItems.java:109` 之后插入注册行（`PlainSeedItem(FarmstormBlocks.PLAIN_CHIVES_CROP.get(), properties)`）；`:140-151` 创造栏 `add(PLAIN_CHIVES_SEEDS)`；`datagen/ModItemTagsProvider.java:42-48`（`#farmstorm:seeds` 成员 0 → 1）；`util/FarmstormTags.java:35` 类注释改成「1 个播种物（平凡韭菜种子）」；`tools/verify_project.py:334-343` 加两个新 id。
- **验收断言**：
  1. Java 文件数 152 → **153**（本任务新增 1 个类；终态 152 的算式见 §4 的 A-98 与 F-299）；`items` 的注册数 27 → **28**。
  2. `data/farmstorm/tags/item/seeds.json` 的 `values` 恰 **1** 项：`farmstorm:plain_chives_seeds`；`FarmstormTags.java:35` 的注释命中 `6 个独立播种物` **0** 次。
  3. `models/item/plain_chives_seeds.json` 的 `parent` = `minecraft:item/generated`、`textures.layer0` = `farmstorm:item/plain_chives_seeds`，且该 png 存在（F-295 已重命名）；创造栏里该物品**不显紫黑格**（截图像素：与 `friend_chives.png` 同色系）。
  4. 游戏内：手持 `plain_chives_seeds` 右键耕地 ⇒ 生成 `farmstorm:plain_chives_crop[age=0]`，手上少 **1** 个；骨粉连用到 `age=3`。
- **依赖**：F-297。
- **回滚影响**：回滚即删掉种子类与物品，`#farmstorm:seeds` 退回空集；F-300 的「2 平凡韭菜 + 2 平凡韭菜种子」产出会指向不存在的物品 ⇒ 必须先回滚 F-300。

### Phase 60 · 培养器判据与产出（F-299 ~ F-301）

阶段目标：把培养器的准入判据从「有档案且非史诗」反转为「是转换配方的输入且输出非史诗」，产出改成固定两组，并补上第一条合成配方。出口判据：17 个投入物、改良作物一个收不进、自身即种子给 4 个、`cultivator.json` 存在。
#### F-299 · 培养器准入判据反转 + 删 `SeedItem.java`（M · 1.5 人日｜依赖 F-291、F-295）

- **目标**：把 `admissible` 从「有 `CropProfile` 且非史诗」反转为「是 21 条转换配方的输入且其输出档案 `tier() != EPIC`」，把 `profileOf` 改名 `outputProfileOf` 并改成「输入 → 配方 → 输出档案」，重写 `:131-137` 的 `_comment`，并在同一提交删掉已无引用的 `SeedItem.java`（52 号 D-218、D-212；§0.5 死命令 4）。
- **交付物**：`blockentity/CultivatorBlockEntity.java:138-144`（`admissible` 方法体整个换掉）、`:151-165`（`profileOf` → `outputProfileOf`，走 `getRecipeFor` 再读输出档案）、`:216-227`（`deliver` 读新方法）、`:131-137`（注释重写）；删 `item/SeedItem.java`（实读 **54 行**）。
- **验收断言**：
  1. `CultivatorBlockEntity` 里 `instanceof SeedItem` 命中 **0**、`profileOf(` 命中 **0**、`outputProfileOf(` 声明 **1** 处 + 调用 **1** 处；`SeedItem.java` 路径不存在。
  2. Java 文件数 153 → **152**（−1 个 `SeedItem.java`）；`src/main/java` 下 `SeedItem` 命中 **0**。
  3. 行为抽验 6 条：小麦 / 胡萝卜 / 毒马铃薯 / 平凡韭菜**收得进**；火把花 / 金苹果**收不进**（`PASS`、槽保持空、手持物不消耗）；`farmstorm:puffed_wheat`（改良作物）**收不进**。
  4. 判据只有一个来源：临时加一条 `converting` 配方（改完构建产物后）⇒ 对应输入物**自动多收一个**，删掉那条配方即恢复；代码里不存在 17 个物品的白名单（`grep -c 'minecraft:wheat'` 在 `CultivatorBlockEntity` = **0**）。
- **依赖**：F-291、F-295。
- **回滚影响**：回滚即恢复「只收带档案的物品」；`SeedItem.java` 一起复活，`FarmstormItems` 的 `seedItem(...)` 工厂已在 F-295 删除 ⇒ 回滚本任务必须同时回滚 F-295，否则编译不过。
#### F-300 · 培养器产出两组 + `hasOwnSeed` 改写（M · 1.5 人日｜依赖 F-299、F-298）

- **目标**：把 `batch()` 改成**永远两组**：投入物 × `cultivator.yield` + 对应种子 × `cultivator.yield`；自身即种子的 11 条给 **4 个同一物品**（52 号 D-219、裁定 #16）。
- **交付物**：`blockentity/CultivatorBlockEntity.java:229-239`（`batch` 常量两组）、`:241-249`（`hasOwnSeed` 的语义从「有没有独立种子」改成「两组怎么给」，注释整段重写）。
- **验收断言**：
  1. 同一台培养器依次喂：小麦 ⇒ **2 小麦 + 2 小麦种子**；胡萝卜 ⇒ **4 胡萝卜**；毒马铃薯 ⇒ **4 毒马铃薯**（不是马铃薯）；平凡韭菜 ⇒ **2 平凡韭菜 + 2 平凡韭菜种子**。
  2. `cultivator.yield` 改成 **3** 后重跑 ⇒ 小麦给 **3 + 3**、胡萝卜给 **6**（两组同乘一个键，证明没拆第二个键）；config 键数不变。
  3. 源码：`batch(` 里 `new ItemStack(` 恰 **2** 处；`hasOwnSeed` 命中 **0**（该判据已废），`seedItem()` 在该文件命中 **0**。
  4. 源码注释不再出现 `5 条给 2 + 2` 与 `13 条只给 2`（`:241-249` 的旧注释已重写）。
- **依赖**：F-299、F-298。
- **回滚影响**：回滚即回到「自身即种子只给一组」，11 条投入物的产出减半（本版唯一一处平衡变更被撤回）；F-310 的 B-42 / B-43 / B-44 边界验收随之失效。
#### F-301 · 培养器合成配方 4 铁 + 4 玻璃 + 1 红石（S · 0.5 人日｜依赖 F-291）

- **目标**：新增培养器的有序 3×3 配方 `铁锭 玻璃 铁锭 / 玻璃 红石粉 玻璃 / 铁锭 玻璃 铁锭` ⇒ 1 个 `farmstorm:cultivator`，`unlockedBy` 用原版材料（52 号 D-220；54 号 §0.3 第 13 项）。
- **交付物**：`datagen/ModRecipeProvider.java`（新增 `cultivator(recipeOutput)` 与调用行，排在 `:61` 的 `converter(...)` 之后）；产物 `data/farmstorm/recipe/cultivator.json`。
- **验收断言**：
  1. `data/farmstorm/recipe/` 的文件总数 = **25**（24 + 1）；`cultivator.json` 存在且 `"type"` = `minecraft:crafting_shaped`。
  2. 材料计数：`minecraft:iron_ingot` **4**、`minecraft:glass` **4**、`minecraft:redstone` **1**；产物 `farmstorm:cultivator` × 1。
  3. `unlockedBy` 的判据物品是**原版**物品（`minecraft:glass`），`#farmstorm:` 前缀在该 json 里命中 **0**。
  4. 游戏内：工作台摆出该形状 ⇒ 产出 1 个培养器；配方书在**首次进入世界**时即可见（不依赖本模组独有标签）。
- **依赖**：F-291。
- **回滚影响**：回滚即删掉 `cultivator.json`，培养器回到「只能创造 / `/give` 取得」（0.2.2 的 A-64 状态）；不影响 F-300 的产出面。

### Phase 61 · 红线 `Kind`（F-302 ~ F-304）

阶段目标：给连线加一个类型字节，让转换器的连线是红色，并把 `Kind` 落到核侧存档里（不按方块类型反查）。出口判据：协议串 `"3"`、包数仍 2、换机器后一个去抖窗口内变色。
#### F-302 · `Link.Kind` + 单条 codec + `PROTOCOL_VERSION` `"3"`（M · 1.5 人日｜依赖 —）

- **目标**：给 `CultivatorLinkPayload.Link` 加第三个字段 `Kind`（`CULTIVATOR` / `CROP_CONVERTER`，线上各占 1 字节），单条 codec 扩到三段，未知码按 `CULTIVATOR` 解，并把 `PROTOCOL_VERSION` 从 `"2"` 升到 **`"3"`**（52 号 D-210；54 号 §5.1、§5.5；§0.5 死命令 5）。
- **交付物**：`network/CultivatorLinkPayload.java:42-45`（单条 codec 三段，第三段 `ByteBufCodecs.BYTE`）、`:53`（`record Link(BlockPos core, BlockPos cultivator, Kind kind)` + `enum Kind`）、`:48-50` 与 `:36`（列表 codec 与 `MAX_LINKS = 1024` **不动**）；`network/FarmstormNetwork.java:25`（`"2"` → `"3"`）。
- **验收断言**：
  1. `Link` 的 record 组件 = **3** 个；`Kind` 枚举成员 = **2** 个；单条 codec 的 `composite(` 组件段 = **3**。
  2. `MAX_LINKS = 1024` 仍 **1** 处、值未变；`playToClient(` 仍 **2** 处、两个包 id 未变。
  3. `PROTOCOL_VERSION = "3"` 命中 **1** 处、`"2"` 在该文件命中 **0**；启动日志的两行自检显示版本 **3** 与 `MAX_LINKS 1024`。
  4. 单条 `Link` 的线上字节数从 `2 × BlockPos` 变 `2 × BlockPos + 1`：用一个 1 条连线的 payload 做编解码往返，字节长度差 = **1**。
- **依赖**：—。
- **回滚影响**：回滚即回到 2 字段与协议串 `"2"`；已升级的客户端连旧服务端会拿不到这条通道（`optional()` 降级为「没有连线」），不会崩；F-303 / F-304 必须一并回滚（它们读 `kind()`）。
#### F-303 · 核侧 `CultivatorKinds` + 三写入点 + `rebindNearby` 扩两种机器（L · 3.0 人日｜依赖 F-302、F-292、F-293）

- **目标**：在核侧 `Cultivators`（`long[]`）旁边**旁挂**一条 `CultivatorKinds`（`int[]`，同长，`0` = 培养器 / `1` = 转换器，缺键 ⇒ 全按 `CULTIVATOR`），把 `addCultivator` 加上 `Kind` 参数并改齐**三个写入点**，同时把 `rebindNearby` 的候选收集从「只认培养器」扩到两种机器（52 号 D-210；54 号 §5.2；上游 0.2.3 更新第 5 条）。
- **交付物**：`blockentity/OutpostCoreBlockEntity.java:120`（新增 `List<Link.Kind> kinds` 或等长数组）、`:335-371`（`addCultivator(BlockPos, Link.Kind)`、`removeCultivator` 两条列表同删）、`:537-572`（`rebindNearby`：`:546-547` 的 `AreaBlockEntities.collect(...)` 改为同时收集 `CultivatorBlockEntity.class` 与 `CropConverterBlockEntity.class`，`:563` 传对应 kind）、`:750-795`（迁移改绑传 kind）、`:848-870`（读档：缺键 ⇒ 全 `CULTIVATOR`，两键长度不一致按较短者截断并打 WARN）；`block/CultivatorBlock.java:99`（传 `CULTIVATOR`）；`block/CropConverterBlock.java` 的 `setPlacedBy`（传 `CROP_CONVERTER`）；`blockentity/CropConverterBlockEntity` 的 `setCore` / `corePos()`。
- **验收断言**：
  1. 落盘：新存档的核 NBT 里 `Cultivators` 与 `CultivatorKinds` 长度**相等**；`CultivatorKinds` 的元素只有 `0` / `1` 两种取值。
  2. 0.2.2 存档（含一个绑了 3 台培养器的核）读档 ⇒ 绑定仍在、`kinds` 全 `0`、连线仍青色；日志里 `CultivatorKinds` 缺失的 WARN 命中 **0**（缺键是合法状态，不告警）。
  3. 三个写入点各 **1** 处：放置绑定（`CultivatorBlock:99` + `CropConverterBlock#setPlacedBy`）、周期补绑（`:563`）、迁移改绑（`:768`）；`addCultivator(` 的单参重载命中 **0**。
  4. **先放机器后放核**：先放一台转换器（无核）→ 再在 3 × 3 区块内放核 ⇒ 补绑日志出现 `补绑：核 … 本轮新增 1 条（累计 1/24，周期 … 刻）`，转换器的 `corePos` 非空、`CultivatorKinds` 该位为 `1`。
  5. 名额共享：培养器 11 台 + 转换器 13 台 ⇒ 第 **25** 台（任一种）绑不上，已绑的 24 条坐标与 kind 一条不变。
- **依赖**：F-302、F-292、F-293。
- **回滚影响**：回滚即删掉 `CultivatorKinds`（新存档里那一列变孤儿键，读档时被忽略）；`rebindNearby` 退回「只认培养器」，先放机器后放核的转换器**永远绑不上**，F-304 的换机变色也失去数据来源。
#### F-304 · 广播三处 + 渲染按 `Kind` 选色（M · 1.5 人日｜依赖 F-303）

- **目标**：把 `Kind` 卷进排序与指纹、按同一下标组装 `Link`，并让渲染按 `kind()` 选颜色：培养器保持青色（一个字节都不许变），转换器用 **`(0.92F, 0.22F, 0.22F, 1.0F)`**（52 号 D-210；54 号 §5.3、§5.4；红线 3；§0.5 死命令 6）。
- **交付物**：`network/CultivatorLinkBroadcaster.java:76-78`（`ORDER` 补第三键 `kind().id()`）、`:217-219`（从 `Cultivators` + `CultivatorKinds` 按同一下标配对读出）、`:227-234`（`fingerprint` 把 `kind().id()` 卷进哈希）；`client/CultivatorLinkRenderer.java:41-44`（两组颜色常量；青色三个分量逐字不变）、`:71-79`（循环里按 `link.kind()` 取色）。
- **验收断言**：
  1. 源码：`ORDER` 的 `thenComparing*` 段 = **3**；`fingerprint` 里对 `kind()` 的引用 = **1** 处；渲染端 `switch (link.kind())` 或等价分派 **1** 处，且 `level` / `getBlockState` 在该文件的渲染路径里命中 **0**。
  2. 颜色值：`CULTIVATOR` 的三个分量仍是 `0.30F / 0.85F / 0.90F`（与 0.2.2 逐字相同）；`CROP_CONVERTER` 是 `0.92F / 0.22F / 0.22F`，且不等于 `1.0F / 0.0F / 0.0F`。
  3. 同屏放一台培养器与一台转换器（同一个核）⇒ 培养器的线青色、转换器的线红色；端点小框与主线同色。
  4. **换机变色**：拆掉已绑定的培养器、原位置换放一台转换器 ⇒ 那条线在 **10 刻**（`MIN_INTERVAL_TICKS`）内从青变红；反向（转换器换培养器）同样在 10 刻内变回青色。
- **依赖**：F-303。
- **回滚影响**：回滚即回到「全部青色」与两键排序；`Kind` 仍在协议里（F-302 未回滚），客户端会按 kind 收到字节但渲染端忽略 ⇒ 不崩、只是没有红色；F-303 的 kind 列变成无人读取的数据。

### Phase 62 · config 与炮弹（F-305 ~ F-306）

阶段目标：把 3 个旧键换成 2 个新键，并给 man瓜炮弹加寿命。出口判据：新生成的 toml 是 175 键 / 60 push / 36 节、`[converter]` 命中 0、炮弹第 300 刻消失。
#### F-305 · config 删 3 加 2（M · 1.5 人日｜依赖 F-293）

- **目标**：删 `[converter]` 整组 3 键、加 `[crop_converter] processTicks`（默认 600，范围 20 ~ 72000）与 `[pumpkin_turret] bulletLifetimeTicks`（默认 300，范围 1 ~ 72000），使 server config 从 **176 键 / 60 push** 变 **175 键 / 60 push**（52 号 D-223；54 号 §6.1、§6.2）。
- **交付物**：`config/FarmstormConfig.java:19-21`（三个字段删）、`:295-305`（`b.push("converter")` 整组换成 `b.push("crop_converter")` + `processTicks`）、`:444-461`（`bulletLifetimeTicks` 加在 `bulletSpeed` 之后、`b.pop()` 之前）；`datagen/ModLangProvider.java:243-245` 的三条 config 键**留给 F-307 删**（本任务不动语言面）。
- **验收断言**：
  1. **先删（或改名备份）`run/config/farmstorm-server.toml`**，起服后数新文件：`^\[converter\]` 命中 **0**、`^\[crop_converter\]` 命中 **1**；`processTicks = 600`、`bulletLifetimeTicks = 300`。
  2. 新 toml 的键数 = **175**、顶层节 = **36**；`FarmstormConfig` 的 `b.push(` / `b.pop()` 仍各 **60**、行首 `.define*` 从 178 变 **177**（含 1 行注释）。
  3. 把升级前的老 toml 放回去再起服 ⇒ `maxPerPlayer = 2` / `cultivatorCapacity = 16` 仍是旧值、`[converter]` 段仍在——**这条红是期望中的红**，证明「老文件不回写默认值」；它必须与第 1 条**成对跑**。
  4. 行为：把 `processTicks` 改成 **20** ⇒ 转换器第 20 刻产出；把 `bulletLifetimeTicks` 改成 **40** ⇒ 炮弹 2 秒后消失。
- **依赖**：F-293。
- **回滚影响**：回滚即恢复 3 个旧键（`crop_converter` 那台机器会因读不到键而 NPE 或退回硬编码，F-293 的产出节奏失效）；toml 会在下次起服时被重写回 `[converter]` 段。
#### F-306 · `TurretBullet` 出膛 300 刻后删除（S · 0.5 人日｜依赖 F-305）

- **目标**：给 `TurretBullet` 加寿命：出膛后第 `pumpkin_turret.bulletLifetimeTicks`（默认 **300** 刻 = 15 秒）无条件静默 `discard()`；**只作用于 `TurretBullet`**（52 号 D-221；红线 6）。
- **交付物**：`entity/TurretBullet.java:76-90`（`tick()` 最开头、`launchOrigin` 取样之后插入寿命判定）。
- **验收断言**：
  1. 源码：`tick()` 里对 `pumpkinTurretBulletLifetimeTicks` 的引用 **1** 处，位置在 `steerTowards(` **之前**（`grep -n` 的行号更小）；`readAdditionalSaveData` 里没有「已飞刻数」的持久化（该键命中 **0**）。
  2. 游戏内：放 man瓜炮台对天空开火，`F3` 实体列表里数 `turret_bullet` 的存活 ⇒ 第 **300** 刻消失；无粒子、无音效、无进度弹出。
  3. 把 `bulletLifetimeTicks` 改成 **40** ⇒ 第 40 刻消失（证明读 config）。
  4. `MelonGrenadeProjectile.java` 与 `MelonSliceProjectile.java` 的 `git diff` 为 **0 行**；瓜！仍然落地才结算。
- **依赖**：F-305。
- **回滚影响**：回滚即恢复「无限飞」；`bulletLifetimeTicks` 变成一个无人读取的键（仍在 toml 里），F-309 的 `#25` 验收与 F-310 的 B-55 / B-56 失效。

### Phase 63 · 语言键与自检收口（F-307 ~ F-308）

阶段目标：把语言键从 340 收到 321 × 2（关语言面），把自检数字与检查器计数一次改齐（关计数面）。出口判据：`zh = en = 321`、日志 `92 … OK`、脚本 `92 / 92`。
#### F-307 · 语言键 340 → 321 × 2 + 两处类注释（**关语言面**）（M · 1.5 人日｜依赖 F-289、F-295、F-296、F-305）

- **目标**：删 23 键、加 4 键、改名 2 键、改值 2 键，使语言键从 **340 × 2** 变 **321 × 2**；同时把 `ModLangProvider:10` 与 `:13-16` 的两处总数注释一次改齐（52 号 D-222、D-224；54 号 §7）。
- **交付物**：`datagen/ModLangProvider.java`：`:88-93,103`（7 个种子键删）、`:177,180-187`（`container.farmstorm.converter` + 8 条 `gui.farmstorm.converter.*` 删）、`:243-245`（3 条 config 键删）、`:222-223`（`nether_star_toll` / `seed_cycle` 各 2 键删）、`:220`（`converter` 描述的中文改「农作物转换器」，英文不动）、`:229`（`fang_dance` 描述中英同改）、新增 4 键（`block.farmstorm.plain_chives_crop` / `item.farmstorm.plain_chives_seeds` / `farmstorm.configuration.crop_converter.processTicks` / `farmstorm.configuration.pumpkin_turret.bulletLifetimeTicks`）、`:94` 与 `:178`、`:219` 两个 `converter` 键**改名**为 `crop_converter`；`:10` 的「各 340 键」→「各 **321** 键」、`:13-16` 的条目构成改 28 + 28 + 32 + 178 + 13 + 42。
- **验收断言**：
  1. `zh_cn.json` 与 `en_us.json` 各 **321** 键；`zh-only = 0` / `en-only = 0`。
  2. 前缀分组六段：`item.` **28** / `block.` **28** / `advancements.` **32** / `farmstorm.configuration.` **178** / `entity.` **13** / 其余（`plain` 组）**42**；`28 + 28 + 32 + 178 + 13 + 42 = 321`。
  3. 改名 2 键各 **1** 处（`item.farmstorm.crop_converter` / `block.farmstorm.crop_converter`），旧键 `item.farmstorm.converter` 与 `block.farmstorm.converter` 命中 **0**；总数不是 323（按「删 1 加 1」数会得出 323，52 号 D-224 点过这个坑）。
  4. `ModLangProvider.java` 里 `340` 命中 **0**、`321` 命中 **2**（`:10` 与 `:16` 各 1）；游戏内方块名显示「农作物转换器」（不是「农产品转换器」、不是 raw key），`fang_dance` 描述显示「触发一次「刺球」」。
- **依赖**：F-289、F-295、F-296、F-305。
- **回滚影响**：回滚即恢复 340 键，但那 23 个被删的键所指向的物件（种子 / 界面 / 旧 config / 两条进度）都不存在 ⇒ 恢复的是**孤儿键**；回滚本任务必须同时回滚 F-296 与 F-295。
#### F-308 · 自检收口 92 / 28 / 28 / 1 + 工具计数（**关计数面**）（M · 1.5 人日｜依赖 F-287 ~ F-307）

- **目标**：把 `Farmstorm.java:180-183` 的四个值一次改成 **92 / 28 / 28 / 1**（其余 8 个逐类断言逐字不动），并把 `tools/verify_project.py:380-393` 的 `EXPECTED_COUNTS` 改成 **28 / 28 / 1**（52 号 D-205；54 号 §2.4 第 7、8 行）。这是窗口的**关窗任务**。
- **交付物**：`Farmstorm.java:180-183`（`total == 98` → **92**、`items == 34` → **28**、`blocks == 27` → **28**、`menuTypes == 2` → **1**）；`tools/verify_project.py:380-393`（`items` 34 → 28、`blocks` 27 → 28、`menu_type` 2 → 1）。
- **验收断言**：
  1. 启动日志整行 `自检 注册对象 92 个：items 28 / blocks 28 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK`。
  2. `python tools/verify_project.py` 打印 `已核 registry 对象: 92 / 92`、`结论：FAIL 0 项`；第 1 节的两行键数都是 **175**。
  3. `Farmstorm.java` 里 `== 98` 命中 **0**、`total == 92` 命中 **1**；`items == 28` / `blocks == 28` / `menuTypes == 1` 各 **1**，其余 8 个逐类 `==` 逐字未变（8 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1）。
  4. `EXPECTED_COUNTS` 的 12 项相加 = **92**；`EXPECTED` 的 config 期望面（`expected_config_keys*` 五个函数体）**一个字节不改**（54 号 §8 #4 的反向判据）。
- **依赖**：F-287 ~ F-307（全量注册面落地）。
- **回滚影响**：回滚即回到 98 / 34 / 27 / 2 的断言，与真实注册面相反 ⇒ 启动日志立刻 `FAIL`；本任务是窗口的关窗点，回滚等于重新开窗，F-309 与 F-311 的所有总数读数为假。

### Phase 64 · 验收与边界（F-309 ~ F-310）

阶段目标：把 54 号 §8 的 32 条与 53 号 §9 的 31 条逐条跑掉，落成两份可复核记录。出口判据：一条不漏、四条必测组合成对跑、四条负向判据写明「没有发生的证据」。
#### F-309 · 54 号 §8 的 **32 条**验收全量执行（L · 3.0 人日｜依赖 F-308）

- **目标**：逐条执行 54 号 §8 的 **32** 条验收，记录到 `.work/v023-verify/acceptance.md`（含表下的**四条必测组合**）。
- **交付物**：新建 `.work/v023-verify/acceptance.md`；只读 `run/logs/latest.log`、`run/config/farmstorm-server.toml`、`src/generated/.../lang/*.json`、`/data get`、游戏内截图。
- **验收断言**：
  1. 记录里 `#1` ~ `#32` **32** 条一条不漏，每条给：动作、读数、通过 / 失败、证据（日志整行原文 / `/data get` 输出 / 截图文件名）。
  2. 四条必测组合**成对跑**：`#7` 与 `#8`（新 config 绿 / 老 toml 期望红）、`#13` 与 `#16`（能产出 / 核满不丢）、`#17` 与 `#19`（收原版 / 不收改良）、`#24` 与 `#23`（视觉变色 / blockstate 数据）。
  3. `#1` 的日志整行与 `#2` 的 `92 / 92` 都是原文粘贴；`#22` 的目录计数是 `84 / 5 / 22 / 0` 四个数一起给。
  4. 与 52 号、53 号、54 号任一处冲突的读数单独列一节「差异」，**不改上游**。
- **依赖**：F-308。
- **回滚影响**：本任务只产出记录文件，回滚不改变代码；但 F-311 的签字依赖它，回滚即签字失效。
#### F-310 · 53 号 §9 的 **31 条**边界复跑（L · 3.0 人日｜依赖 F-309）

- **目标**：逐条跑 53 号 §9 的 **31** 条边界（`B-28` ~ `B-58`），记录到 `.work/v023-verify/boundaries.md`（与验收分开记，因为 54 号 §8 不含 B 面）。
- **交付物**：新建 `.work/v023-verify/boundaries.md`；只读游戏内与源码。
- **验收断言**：
  1. 记录里 `B-28` ~ `B-58` **31** 条一条不漏，按 53 号的分组标注（R1 / R3 / 平凡作物 / R2 / R4 / R5 / 计数面）。
  2. 四条负向判据写「没有发生的证据」：B-32（不能取回）、B-37（漏斗塞不进）、B-41（培养器不收改良作物）、B-52（15 个作物方块仍无 `BlockItem`）。
  3. 计数面 B-58 的十个数一次给全：`92 / 28 / 28 / 1 / 175 / 60 / 321 × 2 / 21 / 17 / 152`；任何一个用旧值（98 / 34 / 27 / 2 / 176 / 340 / 26 / 154）报交付即判失败。
  4. B-46（新增一条转换配方 ⇒ 投入物 17 变 18）用**临时配方**实测，测完删掉；B-57（只改描述）与 F-307 的读数一致。
- **依赖**：F-309。
- **回滚影响**：只产出记录；回滚不影响代码，但 F-311 的边界签字失效。

### Phase 65 · 签字与 README（F-311）

阶段目标：把红线、注册面、19 项必改与三组已知影响签成一份可复核的记录，并把玩家可见的三段话落到 README。
#### F-311 · 红线、注册面、19 项必改与 README 签字（M · 1.5 人日｜依赖 F-309、F-310）

- **目标**：签字确认六件事——红线（0 Mixin / 2 包）、注册面 **92** + 口径外 **3 类**、数值面 **175 / 60** 与 **321 × 2**、**Java 152**、`BRIEF` §5 的 **19 项必改**逐条落点、不改清单；同时把 README 的三段话落盘（52 号 §8 第 2 条、D-206、D-212、D-219）。
- **交付物**：新建 `.work/v023-verify/signoff.md`；`README.md`（新增三段：升级须知「老的 `converter` 方块变缺失方块 + 7 种种子失效」、已知影响「史诗作物只需 30 秒」、平衡变更「自身即种子 11 条产出翻倍」）。
- **验收断言**：
  1. 八项读数齐：Mixin **0**（`src/` 口径）、`*.mixins.json` **0**（`src/`）、`playToClient(` **2**、`PROTOCOL_VERSION` = `"3"`、注册面 **92**、口径外 **3 类**、config **175 / 60**、语言键 **321 × 2**、Java **152**。
  2. 「19 项必改」**19 / 19** 有落点任务（逐条给任务号，见 §3 表 A 表下注）。
  3. `README.md` 三段齐全：`run/config/farmstorm-server.toml` + `删` + `重生成` 三个串同段共现；`下界之星` 与 `30 秒` 同段；`翻倍` 与 `4 个` 同段。
  4. 不改清单逐字未变：`TierTrade`、农民三行、`#farmstorm:converter_fuel` 标签、`OutpostCoreBlockEntity` 的补绑算法、`verify_project.py` 的 config 期望面、`MelonGrenadeProjectile` / `MelonSliceProjectile` 两个类（`git diff` 0 行）。
  5. `docs/zh/52` ~ `54`、`docs/zh/00` ~ `51`、`docs/en/*`、`.work/v023-BRIEF.md`、`plan0.2.3.txt` 的 mtime 未变（本版只新增 `docs/zh/55` 与本记录）。
- **依赖**：F-309、F-310。
- **回滚影响**：只影响签字与 README 文档；回滚不影响代码，但交付面失去唯一一份「19 / 19 + 八项读数」的复核记录。

---

## 3. 覆盖对照表（六张）

表 A ~ 表 F 对应 54 号的 §2 ~ §7。每张表末行给「覆盖数 / 总数」。

### 表 A · 54 号的动作面 **53 行** → 任务

| 行 | 54 号出处 | 动作 | 落点任务 |
|---|---|---|---|
| 1 | §2.1 行 1 | `FarmstormBlocks.java:71-72` 方块 id 与常量名 | F-287 |
| 2 | §2.1 行 2 | `FarmstormItems.java:112-113` 方块物品 | F-287 |
| 3 | §2.1 行 3 | `FarmstormBlockEntities.java:43-45` 方块实体 | F-287 |
| 4 | §2.1.1 | 铁砧七项（METAL / 5.0F / 1200.0F / 工具 / ANVIL / BLOCK） | F-288 |
| 5 | §2.2 行 1 | 新增 `PLAIN_CHIVES_CROP`（`FarmstormBlocks.java:102-104` 之间） | F-297 |
| 6 | §2.2 行 2 | 新增 `PLAIN_CHIVES_SEEDS`（`FarmstormItems.java:109` 之后） | F-298 |
| 7 | §2.3 行 1~7 | 删 7 个种子物品（`FarmstormItems.java:91,102-109`） | F-295 |
| 8 | §2.3 连带·物品面 | `SEED_ITEMS` 列表、`seedItem(...)` 工厂、`SeedItem` import | F-295 |
| 9 | §2.3 连带·战利品面 | 15 张作物表去种子参数（7 处引用） | F-295 |
| 10 | §2.3 连带·标签面 | `#farmstorm:seeds` 成员 6 → 0 | F-295 |
| 11 | §2.3 连带·进页面 | `seed_cycle` 整段删（`:95-106`） | F-295 |
| 12 | §2.3 行 8 | 删 `MenuType CONVERTER` + `FarmstormMenus:12` 注释 | F-289 |
| 13 | §2.6 行 1~2 | 整文件删 `ConverterMenu` / `ConverterScreen` | F-289 |
| 14 | §2.6 行 3 + 末段 | 删 `ConverterRecipeBookCategory`，finder **内联**进 `FarmstormClient:73-76` | F-289 |
| 15 | §2.4 行 1 | 战利品表路径 `blocks/crop_converter.json` | F-288 |
| 16 | §2.4 行 2 | `ModBlockStateProvider:160-184` 重写 | F-292 |
| 17 | §2.4 行 3 | `ModItemModelProvider:68-70` 判 `CropConverterBlock` | F-287 |
| 18 | §2.4 行 4 | `ModBlockTagsProvider:44-45` `processing_block` 加成员 | F-288 |
| 19 | §2.4 行 5 | `ModBlockTagsProvider:48` `mineable_with_pickaxe` 改名 | F-287 |
| 20 | §2.4 行 6 | `ModAdvancementProvider:82-85` 换方块，进度 id 不变 | F-287 |
| 21 | §2.4 行 7 | `Farmstorm.java:180-183` 四值改 92 / 28 / 28 / 1 | F-308 |
| 22 | §2.4 行 8 | `verify_project.py:334-393` 的 `EXPECTED` id 清单 | F-287 / F-295 / F-298 |
| 23 | §2.4 行 8（计数） | `EXPECTED_COUNTS` 改 28 / 28 / 1 | F-308 |
| 24 | §3.1 | 7 张新方块贴图 + 3 个 mcmeta | F-294 |
| 25 | §3.2 | 8 个 variant（`FACING` × `WORKING`）+ 两张模型 | F-292 |
| 26 | §3.3 | item model 产物 `models/item/crop_converter.json` | F-287 |
| 27 | §3.4 | 删 7 张旧 `converter_*.png` | F-294 |
| 28 | §3.5 | `plain_chives_crop` 4 阶段 blockstate + `instanceof PlainCropBlock` + 掉落表 | F-297 |
| 29 | §3.6 | item 贴图 29 → 22（6 删 + 1 改名 + `converter.png`） | F-294 / F-295 |
| 30 | §3.7 | 删 `textures/gui/converter.png` | F-294 |
| 31 | §4.1.1 | `CropConverterBlock` 的 12 个成员（含删 `getMenuProvider`） | F-289（删）/ F-292（其余） |
| 32 | §4.1.2 | 三字段（`Held` / `Progress` / `Core`）+ 四步 `serverTick` | F-293 |
| 33 | §4.1.3 | 四道准入闸（范围 / 绑定 / 手持 / 空槽） | F-293 |
| 34 | §4.1.4 | 三种边界（拆方块 / 核满 / 绑定失效） | F-292（拆）/ F-293（后两个） |
| 35 | §4.1.6 | `first_conversion` 授予点搬到 `deliver()` 成功支 | F-293 |
| 36 | §4.2.1 | `ConverterRecipe` 七字段 → 二字段 + 序列化器 | F-290 |
| 37 | §4.2.2 行 1~5 | 26 → 21、6 铁 2 玻璃 1 红石、新增 `friend_chives` | F-291 |
| 38 | §4.2.2 行 6 | 培养器配方 4 铁 4 玻璃 1 红石 | F-301 |
| 39 | §4.3.1 | 准入判据反转 + `profileOf` → `outputProfileOf` | F-299 |
| 40 | §4.3.2 | 17 个投入物两条档 + `hasOwnSeed` 改写 | F-300 |
| 41 | §4.4 | `TurretBullet` 300 刻寿命 | F-306 |
| 42 | §4.5 | `fang_dance` 描述中英同改 | F-307 |
| 43 | §5.1 | `Link` 三字段 + 单条 codec 三段 + 未知码 | F-302 |
| 44 | §5.2 | 核侧 `CultivatorKinds` 旁挂 + 三个写入点 + 读档默认 | F-303 |
| 45 | §5.3 | `ORDER` / `fingerprint` / 组装 三处补 kind | F-304 |
| 46 | §5.4 | 渲染按 `Kind` 选色（青不变 / 红 0.92,0.22,0.22） | F-304 |
| 47 | §5.5 | `PROTOCOL_VERSION` `"2"` → `"3"` | F-302 |
| 48 | §6.1 | 删 `[converter]` 三键 | F-305 |
| 49 | §6.2 | 加 `crop_converter.processTicks` 与 `pumpkin_turret.bulletLifetimeTicks` | F-305 |
| 50 | §7.1 | 删 23 键 | F-307 |
| 51 | §7.2 | 加 4 键 | F-307 |
| 52 | §7.3 | 改名 2 键 | F-307 |
| 53 | §7.4 | 改值 2 键 | F-307 |

**表 A 的行数核对**：上表正是 **53 行**（编号 1 ~ 53）；其中第 22、29、31、34 四行各挂了两个以上任务（同一行动作落在两处）。`54 号 §2 ~ §7` 的动作面 **53 / 53** 有落点，未覆盖 **0**。

**表下注（三条）**：

1. **`BRIEF` §5 的 19 项必改 → 任务**（19 / 19）：#1 F-287 + F-288；#2 F-297；#3 F-287；#4 F-295 + F-298；#5 F-295 + F-298；#6 F-287；#7 F-289；#8 F-289；#9 F-287 + F-292；#10 F-293；#11 F-290；#12 F-291 + F-301；#13 F-305；#14 F-296（`converter` 判据在 F-287、`seed_cycle` 在 F-295、`nether_star_toll` 在本条）；#15 F-295；#16 F-288 + F-292 + F-297；#17 F-307；#18 F-295 + F-298；#19 F-308。
2. **表外动作 4 项**（不计入 54 号的 53 行，各有独立验收）：① 删 `SeedItem.java`（F-299，A-98）；② `PROTOCOL_VERSION` 升 `"3"`（F-302，54 号 §5.5 已列、`BRIEF` 与 52 号未列）；③ `rebindNearby` 扩到两种机器（F-303，上游 0.2.3 更新第 5 条）；④ `README.md` 三段文案（F-311，52 号 §8 第 2、4 条）。
3. **一行多任务只有 5 处**（第 22、23、29、31、34 行），每处都写明了拆法；其余 35 行一行一任务，回滚粒度清晰。

### 表 B · 注册面 **92**（12 类逐类）+ 口径外 **3 类**

**B.1 12 类逐类**

| # | registry | 基线 | 本版增量 | 改后 | 落点任务 | 核对任务 |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 34 | −7（种子）+1（`plain_chives_seeds`） | **28** | F-295、F-298 | F-308 |
| 2 | `Registries.BLOCK` | 27 | +1（`plain_chives_crop`） | **28** | F-297 | F-308 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 8 | 0（`converter` 是改名） | **8** | F-287 | F-308 |
| 4 | `Registries.ENTITY_TYPE` | 13 | 0 | **13** | — | F-308 |
| 5 | `Registries.MOB_EFFECT` | 4 | 0 | **4** | — | F-308 |
| 6 | `Registries.SOUND_EVENT` | 3 | 0（新配方不加音效） | **3** | — | F-308 |
| 7 | `Registries.RECIPE_TYPE` | 1 | 0（`converting` 保留） | **1** | — | F-308 |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | 0 | **1** | F-290（改形状不改计数） | F-308 |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | 0（进既有栏） | **1** | F-298 | F-308 |
| 10 | `Registries.DAMAGE_TYPE` | 3 | 0 | **3** | — | F-308 |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | 0 | **1** | — | F-308 |
| 12 | `Registries.MENU` | 2 | −1（`converter`） | **1** | F-289 | F-308 |
| **合计** | | **98** | **−8 + 2** | **92** | | `28+28+8+13+4+3+1+1+1+3+1+1 = 92` |

**B.2 口径外 3 类**（不计入 92，必须显式登记）

| # | registry | 基线 | 本版增量 | 改后 | 落点任务 | 核对任务 |
|---|---|---|---|---|---|---|
| 13 | `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` | 1 | 0 | 1（**口径外**） | — | F-311（人工登记） |
| 14 | `Registries.VILLAGER_PROFESSION` | 1 | 0 | 1（**口径外**） | — | F-311 |
| 15 | `Registries.POINT_OF_INTEREST_TYPE` | 1 | 0 | 1（**口径外**） | — | F-311 |

**B.3 四个「加了但不进注册面」的构件**

| # | 构件 | 为什么不进注册面 | 任务 |
|---|---|---|---|
| 1 | `block/PlainCropBlock` | 是方块类，占的是 `BLOCK` 那 1 个额（已计入第 2 行） | F-297 |
| 2 | `item/PlainSeedItem` | 同上，占 `ITEM` 那 1 个额（已计入第 1 行） | F-298 |
| 3 | `CultivatorKinds` | NBT 键，不是注册对象 | F-303 |
| 4 | `datagen/ModLangProvider` 的 4 条新键 | 语言键不是注册对象 | F-307 |

**覆盖：12 类 + 3 个口径外类 + 4 个构件 = 19 项，未覆盖 0**（合计行 **92**，口径外**累计 3 类**）。**工具缺口承认**：`verify_project.py` 的 `EXPECTED` 12 组里**没有**第 13 / 14 / 15 三行 ⇒ 它既不核这三类的存在、也不核计数；这条缺口由表 B.2 + F-303 的验收 + F-311 的签字三处人工兜住。

### 表 C · config **175 键 / 60 push**（删 3 加 2）

| # | 完整键路径 | 类型 | 现值 | 改后 | 值域 | 动作 | 落点任务 |
|---|---|---|---|---|---|---|---|
| 1 | `converter.xpCost` | int | 3 | — | 0 ~ 1000 | **删**（`FarmstormConfig.java:19,296-298`） | F-305 |
| 2 | `converter.netherStarForwardCost` | int | 1 | — | 0 ~ 64 | **删**（`:20,299-301`） | F-305 |
| 3 | `converter.netherStarOnReverse` | boolean | `false` | — | — | **删**（`:21,302-304`） | F-305 |
| 4 | `crop_converter.processTicks` | int | — | **600** | 20 ~ 72000 | **加**（新节，占 `[converter]` 原位置 `:295`） | F-305 |
| 5 | `pumpkin_turret.bulletLifetimeTicks` | int | — | **300** | 1 ~ 72000 | **加**（既有节 `:444-461` 内） | F-305 |
| **合计** | **5 行 = 删 3 / 加 2 / 改 0** | | | | | **`176 − 3 + 2 = 175`；`push` `60 − 1 + 1 = 60`；顶层节 `36`** | |

**三处核对**：① `[converter]` 的 `b.push` / `b.pop` 被 `[crop_converter]` 的同一对替换 ⇒ `push` 仍是 **60**；② `bulletLifetimeTicks` 落在既有节内 ⇒ 顶层节仍 **36**；③ 语言键侧 `farmstorm.configuration.*` 从 179 变 **178**（F-307），与键数 175 + CLIENT 3 相符。

**两条纪律**：① **核对任何 config 断言之前先删（或改名备份）`run/config/farmstorm-server.toml`**，否则会读到 0.2.1 时代的 `maxPerPlayer = 2` / `cultivatorCapacity = 16`（A-95）；② `verify_project.py` 的 config 期望面**一个字节不改**（54 号 §8 #4）。

### 表 D · 语言键 **321 × 2**

| # | 动作 | 键数 | 明细 | 落点任务 |
|---|---|---|---|---|
| 1 | **删** 7 个种子物品键 | −7 | `item.farmstorm.{puffed_wheat_seeds,power_beet_seeds,melon_grenade_seeds,pumpkin_turret_seeds,awakened_torchflower_seeds,awakened_pitcher_plant_pod,chive_seeds}` | F-307 |
| 2 | **删** 转换器 GUI | −9 | `container.farmstorm.converter` + `gui.farmstorm.converter.{convert,xp_cost,no_recipe,need_catalyst,not_enough_xp,forward,reverse,tooltip}` | F-307 |
| 3 | **删** 转换器 config | −3 | `farmstorm.configuration.converter.{xpCost,netherStarForwardCost,netherStarOnReverse}` | F-307 |
| 4 | **删** 两条进度 | −4 | `advancements.farmstorm.{seed_cycle,nether_star_toll}.{title,description}` | F-307 |
| 5 | **加** 平凡作物与两条 config | +4 | `block.farmstorm.plain_chives_crop` / `item.farmstorm.plain_chives_seeds` / `farmstorm.configuration.crop_converter.processTicks` / `farmstorm.configuration.pumpkin_turret.bulletLifetimeTicks` | F-307 |
| 6 | **改名** 2 键（键数不变） | 0 | `item.farmstorm.converter` → `item.farmstorm.crop_converter`、`block.farmstorm.converter` → `block.farmstorm.crop_converter` | F-307 |
| 7 | **改值** 2 键（键数不变） | 0 | `advancements.farmstorm.fang_dance.description`（中英同改）、`advancements.farmstorm.converter.description`（中文换术语） | F-307 |
| **合计** | | **−23 + 4 = −19** | **`340 − 19 = 321`（× 2）** | |

**逐前缀校验（改后）**：`item.` **28** / `block.` **28** / `advancements.` **32** / `farmstorm.configuration.` **178** / `entity.` **13** / `plain` 组 **42**（`itemGroup` 1 + `container` 1 + `gui` 0 + `tooltip` 20 + `effect` 4 + `subtitles` 3 + `death` 6 + `key` 2 + `command` 3 + `villager` 1 + `message` 0 + `tooltip` 补 1）⇒ `28 + 28 + 32 + 178 + 13 + 42 = 321`。

**两条纪律**：① **中英同键同数**（同一张 `ENTRIES` 表按列写出，`zh-only = 0` / `en-only = 0`）；② **语言键没有自动闸**——`verify_project.py` 只比两份键集相等，**不校验 321**，关语言面只能靠 F-307 的人工计数。

### 表 E · 风险 **RT-71 ~ RT-82（12 条）** → 任务

| 编号 | 风险（54 号 §9） | 负责验证的任务 | 该任务怎么验 |
|---|---|---|---|
| **RT-71** | 改名不彻底：三处注册改了、字符串或数据路径没改 | F-287、F-288、F-309 | F-287 验 `ConverterBlock` / `FarmstormBlocks.CONVERTER` 命中 0；F-288 验两个标签产物；F-309 的 `#3` / `#22` / `#30` 三条独立判据 |
| **RT-72** | 92 与两套闸不同步（自检 / 检查器 / 注册面） | F-308、F-309、F-311 | F-308 把两处**同提交**改齐；F-309 的 `#1` 与 `#2` 成对跑；F-311 的八项读数 |
| **RT-73** | 手持闸写成「非史诗」（把培养器判据抄给转换器） | F-293、F-309 | F-293 的验收 3（火把花放进转换器必须成功）；F-309 的 `#17` 与 `#19` 成对跑 |
| **RT-74** | 核满时的投递失败被当成成功（产物消失或复制） | F-293、F-309 | F-293 的验收 4（`progress` 停在 600、槽不丢）；F-309 的 `#16` |
| **RT-75** | `Kind` 加了字段但没进指纹 / 排序 / 组装 | F-304、F-309 | F-304 的验收 1 与 4（换机 10 刻内变色）；F-309 的 `#24` |
| **RT-76** | 协议串没升 | F-302、F-309 | F-302 的验收 3（`"3"` 命中 1、`"2"` 命中 0）；F-309 的 `#5` |
| **RT-77** | 平凡作物走错资源分支（复用 `cropStates` 或漏 `instanceof`） | F-297、F-309 | F-297 的验收 1 / 2（模型 `textures.crop` 指向 `friend_chives_stageN`、贴图仍 84）；F-309 的 `#22` / `#23` |
| **RT-78** | 把动态 PNG 的暂停交给代码 | F-292、F-294、F-309 | F-292 的验收 1 / 2（两态两模型）；F-294 的验收 3（只有三个 mcmeta）；F-309 的 `#23` |
| **RT-79** | 语言键数目算错（改名当删加、漏删进度键） | F-307、F-310 | F-307 的验收 1 / 2 / 3；F-310 的 B-58（十个数一次给全） |
| **RT-80** | 拿老 toml 做验收 | F-305、F-309 | F-305 的验收 1 / 3 **成对**写（新 config 绿 / 老 toml 期望红）；F-309 的 `#7` + `#8` |
| **RT-81** | 炮弹寿命写错位置或扩大范围 | F-306、F-309 | F-306 的验收 1（插入点在 `steerTowards` 之前）与 4（两个类 diff 0 行）；F-309 的 `#25` / `#26` |
| **RT-82** | 平凡韭菜种子复用了 `SeedItem` | F-298、F-309 | F-298 的验收 1 / 4（构造器是 `PlainSeedItem`，能种下去）；F-309 的 `#20` |
| **合计** | **12 条** | | **12 / 12，未覆盖 0** |

**十二条的共同点**：每条缓解都是「**一处定义 + 一条可 grep 的字符串 + 一条行为验收**」，没有一条靠「记得小心」。RT-71 / RT-72 / RT-79 的落点是能数的文件与键；RT-73 / RT-74 / RT-80 / RT-82 的落点是必须真进游戏做一次的动作；RT-75 / RT-76 / RT-78 / RT-81 的落点是一行代码或一个字节。

### 表 F · 待验证 API / 技术前提 **14 条** → 任务

编号照 54 号的行文顺序（§2.1.1、§3.1、§3.5、§4.1.2 ~ §4.1.6、§4.3.4、§5.1、§5.5）。

| # | 待验证的 API / 语义（54 号） | 由哪个任务在哪一步确认 | 确认不了时的退路 |
|---|---|---|---|
| 1 | `StreamCodec.composite` 的**三段**重载能容纳 3 个 `Link` 字段（§5.1） | F-302 的验收 1 | 退到「手写 `StreamCodec.of`」，包结构与字节数不变 |
| 2 | `ByteBufCodecs.BYTE.map(Kind::byId, Kind::id)`（§5.1） | F-302 的验收 1 / 4 | 退到裸 `BYTE` + 客户端 `Kind.byId`，语义相同 |
| 3 | 未知 kind 码按 `CULTIVATOR` 解（§5.1） | F-302 + F-309 的 `#32`（老存档） | 若旧客户端收到 1 就画错色 ⇒ 把未知码改成「不画这条线」并登记 |
| 4 | `ItemNameBlockItem` 的 `useOn` 走原版放置流程（§4.3.4） | F-298 的验收 4 | 若放不下去 ⇒ 覆写 `useOn` 并显式调 `super`，行为与 0.2.2 的原版种子一致 |
| 5 | `CropBlock` 子类可以不带档案（§3.5、§4.3.4） | F-297 的验收 1 / 4 | 若 `CropBlock` 的某些钩子要求档案 ⇒ 改为 `FarmstormCropBlock` + 一个**不注册**的空档案（不，会污染 `ALL`）⇒ 报作者 |
| 6 | 核侧 `acceptAll(List<ItemStack>)` 的「先算后写、失败一个都不写」（§4.1.2） | F-293 的验收 4 | 若它半写 ⇒ 改调单件接口并在本类做回滚，或报作者扩接口 |
| 7 | `getRecipeFor(SingleRecipeInput)` 能当手持闸（§4.1.3） | F-293 的验收 3（火把花能进、改良作物也能进） | 若查不到 ⇒ 退到「按 21 条配方的输入物品建只读集合」，但那会多一份副本（RT-49 同性质） |
| 8 | `Containers.dropContents` 的四个条件（§4.1.4） | F-292 的验收 4 / F-293 的验收 5 | 若不掉落 ⇒ 手写 `Block.popResource`，条件不变 |
| 9 | 铁砧七项属性是**整体替换**而不是叠加（§2.1.1） | F-288 的验收 1 / 3 | 若某项被别的默认值覆盖 ⇒ 显式再写一次并登记到 F-309 的差异节 |
| 10 | `RANGE_CHUNKS = 1` 的 3 × 3 区块判据（§4.1.3） | F-303 的验收 4（先放机器后放核） | 若补绑不触发 ⇒ 扩 `rebindNearby` 的收集类（本版已做）仍失败 ⇒ 报作者 |
| 11 | `minecraft:block/crop` 父模型 + `renderType = cutout`（§3.5） | F-297 的验收 1 / 4 | 若渲染成黑块 ⇒ 显式写 `renderType`，不改父模型 |
| 12 | `frametime: 2` 是贴图元数据，代码碰不到（§3.1） | F-292 的验收 4 / F-294 的验收 3 | 若空闲仍在播 ⇒ 只能换更长的静态图，**不许**写暂停代码（红线 4） |
| 13 | `FarmstormAdvancements.award` 对已授予的重复调用幂等（§4.1.6） | F-293 的验收 3（连产两次不崩） | 若不幂等 ⇒ 在授予前判 `player.getAdvancements().getOrStartProgress(...).isDone()` |
| 14 | `PROTOCOL_VERSION` 协商与 `optional()` 降级（§5.5） | F-302 的验收 3 + F-309 的 `#5` | 若旧客户端直接崩 ⇒ 改为 `playToClient` 前判版本，仍不许加包 |
| **合计** | **14 条** | **14 / 14 落到具体任务与具体步骤** | **14 / 14 给出可执行退路** |

---

## 4. 裁决台账（A-92 起）

本节收 **10 条**：全部是本文写作期新立，状态均为**已落**；上游文件一字不改（差异只登记在这里与回报里）。

### 4.1 A-92 · 改名任务与自检任务的提交顺序：F-287 开窗、F-308 关窗

- **结论**：注册改名与自检收口**不许同提交**，也不许相邻：改名在 **F-287**（Phase 55），自检收口在 **F-308**（Phase 63），中间 20 个任务按 §0.3 的四条窗口纪律执行。
- **依据**：id 面（`EXPECTED` 的 id 清单）在改名那一刻就要换，计数面（`Farmstorm.java:180-183` + `EXPECTED_COUNTS`）只有在 7 个种子删完（F-295）、两个新对象加完（F-297 / F-298）、`MenuType` 删完（F-289）之后才能写 92；三个前置任务分属三个 Phase，任何「合并成一个原子提交」的写法都要把四十余处改动压进一个提交（54 号 §2.4 的六面 + §2.5 的五面）。
- **落地位置**：§0.3 的窗口表与四条纪律；F-287 的验收 1（原样粘贴 98）；F-308 的验收 1 ~ 3；§6 自检第 12 条。
- **否决**：「改名与自检同提交」——它要求改名任务同时把 92 写死，而此时 `items` 还是 34、`menu_type` 还是 2 ⇒ 启动即 FAIL，比窗口红更糟；「每个计数变化的任务各改一次断言」——`total == 98` 与 12 个逐类 `==` 是**一个等式组**，部分更新必然让 `total` 与逐类之和打架。

### 4.2 A-93 · `plain_chives_seeds` 的贴图重命名归属 **F-295**

- **结论**：`chive_seeds.png` → `plain_chives_seeds.png` 的**重命名**归 **F-295**（删种子的任务），不归 F-294（转换器资源面），也不归 F-298（新物品注册）。
- **依据**：`chive_seeds` 物品在 **F-295** 被删，同一提交里把它的 png 改名，任何时刻都**没有任何物品指向一张不存在的图**；若放到 F-294（F-295 之前），`chive_seeds` 物品自 F-294 起就丢图（紫黑格，`ModItemModelProvider:55-57` 的兜底会去找 `textures/item/chive_seeds.png`）；若放到 F-298（F-295 之后），`plain_chives_seeds` 物品自 F-298 起丢图。
- **落地位置**：F-295 的交付物（7 张种子图）与验收 3（`plain_chives_seeds.png` 存在、`chive_seeds.png` 不存在）；F-298 的验收 3（模型指向的是已存在的 png）。
- **否决**：「资源面统一在一个任务里做」（把 8 个 item 图 + 7 个方块图 + 1 个 gui 图并成一个 S）——它会让贴图面横跨四个任务的语义（转换器 / 种子 / 新物品 / 界面），回滚粒度与验收观察面都碎掉。

### 4.3 A-94 · 红线 `Kind` 与指纹的绑定：三个任务、三处落点、一处都不许漏

- **结论**：把 `Kind` 拆成 **F-302（字段 + codec + 协议串）→ F-303（核侧落盘 + 写入点 + 补绑）→ F-304（`ORDER` / `fingerprint` / 组装 + 渲染）** 三步；`ORDER`、`fingerprint`、组装三处**必须与渲染换色同一提交**（F-304）。F-302 与 F-303 之间的中间态是安全的（`kind` 恒为 `CULTIVATOR`，颜色与 0.2.2 相同）。
- **依据**：`CultivatorLinkBroadcaster.java:76-78` 只比两个 `long`、`:227-234` 只哈希两个 `long`——`Kind` 不进这两处就是「kind 变了不重发」（BRIEF §7 第 6 条点名的两个静默失效点）；渲染换色若先落地，会得到「颜色按 kind 取、但 kind 从没变过」的假绿。
- **落地位置**：F-304 的验收 1（`thenComparing*` 3 段、`fingerprint` 里 `kind()` 1 处）；F-304 的验收 4（换机 10 刻内变色）；§0.5 死命令 6。
- **否决**：「把 `Kind` 与渲染合成一个 L 任务」——那会把协议、存档、广播、渲染四层压在同一个提交里，任一环出错都要整体回滚，且 0.2.2 存档的兼容面无法单独验。

### 4.4 A-95 · config 验收前必须删掉 `run/config/farmstorm-server.toml`

- **结论**：**F-305** 与 **F-309 的 `#7` / `#8`** 的 config 断言一律以「先删（或改名备份）`run/config/farmstorm-server.toml`、再起服」为前提；不写迁移代码，不手改 toml。
- **依据**：盘上的 toml 是陈旧的——`:753` 的 `maxPerPlayer = 2`、`:765` 的 `cultivatorCapacity = 16`，而 0.2.2 的代码默认值已是 4 / 24（54 号 §6.4）；NeoForge 的 config 只在**键增删**时重写文件，改默认值不回写。
- **落地位置**：F-305 的验收 1 与 3（成对）；§3 表 C 的两条纪律；F-309 的验收 2。
- **否决**：「把 toml 里那两行手改一下再验」——手改的是玩家的文件，验完就忘 ⇒ 下一版又来一次假红；也否决「加一段一次性迁移」——覆盖玩家自己的设置比「不生效」更糟（0.2.2 已否决）。

### 4.5 A-96 · `FarmstormClient:75` 内联而**不是**删除

- **结论**：`ConverterRecipeBookCategory` 类文件照删，但它的 `registerRecipeCategoryFinder` 必须**内联**进 `FarmstormClient:73-76`；`FarmstormClient:62`（屏幕注册）直接删。
- **依据**：`converting` 类型本版**保留**（52 号 D-209），21 条配方仍在；没有 finder 时客户端会为每条未知 `RecipeType` 刷一条 `Unknown recipe category` WARN ⇒ 删干净等于一次刷 **21 条**。
- **落地位置**：F-289 的交付物与验收 3（`registerRecipeCategoryFinder(` = 1、`Unknown recipe category` = 0）。
- **否决**：「整类删、不内联」——21 条 WARN 会淹没日志里真正的 recipe 解析错误；「保留那个类」——它是纯客户端注册，留着就是 153 与 152 之外的第 4 个文件。

### 4.6 A-97 · 旧转换器方块贴图是 **7 张**，不是 6 张

- **结论**：`textures/block/` 下要删的 `converter*.png` 是 **7 张**；F-294 的验收按 7 张写（`converter_*.png` 命中 **0**），并按「84 +7 −7 = 84」核方块 png 总数。
- **依据**：`BRIEF` §1.1 与 52 号 §3.3 第 12 项写「6 张」；实读目录是 7 张（`converter_front` / `converter_side` 两张**孤儿** + `converter_side_{wheat,carrot,potato,beetroot}` + `converter_top`），其中只有 5 张被 0.2.2 的模型引用（`ModBlockStateProvider:160-166`）。
- **落地位置**：F-294 的交付物与验收 2；表 A 第 27 行。
- **否决**：「照 6 张写」——留一张孤儿 png，下一版的目录计数就永远对不上「84」这个数。

### 4.7 A-98 · Java 文件数是 **152**，算式里多一个 `SeedItem.java` 减项

- **结论**：终态 Java 文件 **152**（不是 153，也不是 154）：`154 − 3（GUI 三件）− 1（`SeedItem.java`）+ 2（`PlainCropBlock` / `PlainSeedItem`）= 152`。
- **依据**：`SeedItem.java` 实读 **54 行**；在 7 个种子删完（F-295）且 `CultivatorBlockEntity:161` 的 `instanceof SeedItem` 随判据反转消失（F-299）之后，它没有任何引用——`FarmstormItems` 的 `seedItem(...)` 工厂（`:175-177`）、`SEED_ITEMS` 列表（`:35`）、`ModifiedCropItem:116` 的注释是它仅剩的三处，前两处随 F-295 删、第三处是注释。
- **落地位置**：F-289 的验收 1（154 → 151）、F-298 的验收 1（152 → 153）、F-299 的验收 2（153 → 152）、F-310 的 B-58（十个数里的最后一个）、F-311 的验收 1。
- **附注**：54 号 §1.5 与 `BRIEF` §3.2 写 **153**，本条按**实读引用面**改判；不改上游，差异随本任务一并报告。

### 4.8 A-99 · `PROTOCOL_VERSION` 挂在 F-302 的同一提交里

- **结论**：`"2"` → **`"3"`** 的动作归 **F-302**（`Link` 加字段的那一提交），不并入 F-303（落盘）或 F-304（广播与渲染）。
- **依据**：`FarmstormNetwork.java:22-24` 的既有纪律是「codec 一变必须跟着变」；`Link` 从 2 字段变 3 字段就是 codec 变了。留在 `"2"` 会让 0.2.2 客户端与 0.2.3 服务端协商通过、随后按旧 codec 解新字节流 ⇒ **必崩**的解码错误；`optional()` 的语义只在版本不同时才让旧客户端拿不到这条通道。
- **落地位置**：F-302 的交付物（`FarmstormNetwork.java:25`）与验收 3；§0.5 死命令 5；表 A 第 47 行；表 F 第 14 条。
- **否决**：「等 F-304 一起改」——F-303 与 F-304 之间会存在一段「服务端已经在发 3 字段、协议串还是 2」的混装崩溃窗口。

### 4.9 A-100 · `rebindNearby` 扩两种机器是本版的**独立落点**，不并入通用写入点

- **结论**：`OutpostCoreBlockEntity:537-572` 的 `rebindNearby` 必须把候选收集（`:546-547`）从「只认 `CultivatorBlockEntity.class`」扩到两种机器，并给每条候选传对应 `Kind`；它是 F-303 的**独立验收项**（验收 4），不许当成「三个写入点」的一部分含糊带过。
- **依据**：`:547` 的 `AreaBlockEntities.collect(..., CultivatorBlockEntity.class)` 只按类型收集；不扩的话「先放转换器、后放核」这条最常见的手顺下，转换器**永远绑不上**，而服务端不报错、也不打日志——与 RT-71 同性质的静默失效。三个写入点（放置 / 补绑 / 迁移）里只有补绑这一条会因此漏掉转换器。
- **落地位置**：F-303 的交付物与验收 4（补绑日志 `补绑：核 … 本轮新增 1 条`）；§3 表 A 表下注的表外动作第 ③ 项。
- **否决**：「让转换器自己每隔 N 刻找核」——那是第二套补绑算法，与核侧的「先核后机器」纪律相反（`:529` 的注释已把顺序写死）。

### 4.10 A-101 · 验收与边界**分两份、串行**，不合并成一份记录

- **结论**：54 号 §8 的 32 条（F-309）与 53 号 §9 的 31 条（F-310）分两份记录、串行执行；F-310 依赖 F-309 是**记录顺序**（同一批场景复用），不是技术依赖。
- **依据**：两份规格的判据面不同——§8 是「做了什么、读数是多少」，§9 是「什么算 / 什么不算」的负向判据（B-32 / B-37 / B-41 / B-52 四条判「没发生」）；合在一起会让负向判据被正向读数淹没。串行是因为 31 条里有 6 条复用 32 条里的同一套场景（核 / 转换器 / 培养器三件摆设）。
- **落地位置**：F-309 与 F-310 的交付物与验收；§5 的执行顺序；F-311 的验收 1（两份记录都要在）。
- **否决**：「两份合并成一份 `verify.md`」——54 号 §8 #32 的存档兼容与 53 号 B-58 的计数面会互相引用，一份文件里既有「读数」又有「边界」，复核时无法判断某条是缺失还是被合并。

---

## 5. 执行顺序

### 5.1 必须串行的段（八段）

| 段 | 串行链 | 为什么不能并行 |
|---|---|---|
| 1 | F-287 → F-292 → F-293 | 同一个方块 / 方块实体两代形态，F-292 的 ticker 指向 F-293 的 `serverTick` |
| 2 | F-290 → F-291 → F-299 | 配方结构与配方表同一份数据，F-299 的判据读的就是 F-291 的 21 条 |
| 3 | F-297 → F-298 | 种子指向方块，方块不存在时 `PlainSeedItem` 的构造参数拿不到 |
| 4 | F-299 → F-300 | 产出函数依赖新判据（`outputProfileOf`） |
| 5 | F-302 → F-303 → F-304 | `Kind` 的字段 → 落盘 → 广播与渲染，后一步读前一步的 API |
| 6 | F-305 → F-306 | 炮弹寿命读 F-305 的新键 |
| 7 | F-307 → F-308 | 语言面先关，计数面后关（§0.3 纪律 3） |
| 8 | F-309 → F-310 → F-311 | 验收 → 边界 → 签字 |

### 5.2 可并行的簇（五簇）

| 簇 | Phase | 可并行任务 | 并行度 | 说明 |
|---|---|---|---|---|
| A | 54 ~ 56 | F-286、F-287、F-290 | 3 | 三者的文件集互不相交（`gradle`+`.cmd` / 注册类 + 两个 provider + 工具 / 配方三件） |
| B | 55 ~ 57 | F-288、F-289、F-291、F-294 | 3 | 都等各自前驱；`F-294` 与 `F-293` 可同时开（一个动资源目录、一个动类） |
| C | 58 ~ 60 | F-295、F-297、F-301、F-302 | 2 | `F-295` 与 `F-302` 完全独立（一个动物品与进度、一个动协议） |
| D | 59 ~ 61 | F-298、F-303、F-305 | 2 | `F-303` 与 `F-305` 分属核侧存档与 config，互不相交；两者都要等 F-293 |
| E | 63 | F-307、F-308 | 2 | 语言表与自检断言不共享文件；两者都必须在 F-309 之前完成 |

**并行度上限 3**。实际受**同文件冲突**约束：`FarmstormItems.java` 被 F-287 / F-295 / F-298 三次改（按序串行）；`FarmstormBlocks.java` 被 F-287 / F-288 / F-297 三次改（串行）；`OutpostCoreBlockEntity.java` 只被 F-303 改一次；`ModRecipeProvider.java` 被 F-290 / F-291 / F-301 三次改（串行）；`ModLangProvider.java` 只在 F-307 改一次。按「一个任务只动自己那一段、不在别人的行上做格式修正」的约定后，**可稳定并行 2 ~ 3 路**。

### 5.3 推荐提交序列

```
F-286 → F-287（开窗）→ {F-288, F-289} → {F-290 → F-291} → {F-292 → F-293, F-294}
      → {F-295 → F-296} → {F-297 → F-298} → {F-299 → F-300, F-301}
      → {F-302 → F-303 → F-304} → {F-305 → F-306}
      → F-307 → F-308（关窗）→ F-309 → F-310 → F-311
```

**读序列四条**：

1. **F-287 必须最先落地**（除版本号）：它打开窗口，后面 20 个任务的验收都按「本条对象」写；先做别的任务会让 `EXPECTED` 的 id 清单先红一遍。
2. **F-295 要早于 F-297**：`seedItem` 置空与 7 张图的下线先做完，平凡作物的新图才有位置落；反之会把「谁的 png 是谁的」搅在一起。
3. **F-299 要早于 F-300**：判据不反转就改产出，会让 `batch()` 的输入还是旧档案。
4. **F-307 要早于 F-308**：语言面 321 与计数面 92 是两个观察面，先关语言面能让 F-308 之后的 `verify_project.py` 一次全绿（语言面没有自动闸，人工计数必须在关窗前完成）。

### 5.4 关键路径

按依赖取最长前驱（每个节点取所有前驱累计完成时间的最大值）：

```
F-287 改名（开窗）      3.0  （累计 3.0）
 └─ F-288 属性         1.5  （累计 4.5）
     └─ F-292 方块行为  3.0  （累计 7.5）
         └─ F-293 状态机 3.0 （累计 10.5）
             └─ F-305 config 1.5（累计 12.0）
                 └─ F-307 关语言面 1.5（累计 13.5）
                     └─ F-308 关计数面 1.5（累计 15.0）
                         └─ F-309 验收 3.0（累计 18.0）
                             └─ F-310 边界 3.0（累计 21.0）
                                 └─ F-311 签字 1.5（累计 22.5）
```

**关键路径 = 22.5 人日 / 10 个任务**，占总量 44.0 的 **51.1%**。这条链是纯依赖推算的结果，不含任何「建议优先」。第二长的链走红线（F-287 → F-292 → F-293 → F-303 → F-304）与收尾（F-309 → F-310 → F-311），合计 **21.0 人日**；两条链共享链头与链尾，1.5 人日的差只在 F-303 与 F-305 之间分叉。

> **读法**：想缩短日历工期，优先压 F-287（关键路径上唯一一个把改名连带到 9 个文件的任务）与 F-292 / F-293（两台机器的形态迁移，共 6.0 人日），而不是压验收（F-309 ~ F-311 共 7.5 人日已在链尾，压它等于把读数做薄）。

### 5.5 人日汇总

| Phase | 名称 | 任务 | 任务数 | S | M | L | 人日 | 里程碑 |
|---|---|---|---|---|---|---|---|---|
| 54 | 发布面 | F-286 | 1 | 1 | 0 | 0 | `0.5` = **0.5** | **M50** |
| 55 | 转换器身份：改名、属性、界面 | F-287 ~ F-289 | 3 | 0 | 2 | 1 | `3.0 + 1.5 + 1.5` = **6.0** | **M51** |
| 56 | 配方结构与配方表 | F-290 ~ F-291 | 2 | 0 | 2 | 0 | `2 × 1.5` = **3.0** | **M52** |
| 57 | 转换器行为与资源面 | F-292 ~ F-294 | 3 | 0 | 1 | 2 | `2 × 3.0 + 1.5` = **7.5** | **M53** |
| 58 | 删种子与进度 | F-295 ~ F-296 | 2 | 1 | 1 | 0 | `1.5 + 0.5` = **2.0** | **M54** |
| 59 | 平凡作物 | F-297 ~ F-298 | 2 | 0 | 2 | 0 | `2 × 1.5` = **3.0** | **M55** |
| 60 | 培养器判据与产出 | F-299 ~ F-301 | 3 | 1 | 2 | 0 | `2 × 1.5 + 0.5` = **3.5** | **M56** |
| 61 | 红线 `Kind` | F-302 ~ F-304 | 3 | 0 | 2 | 1 | `2 × 1.5 + 3.0` = **6.0** | **M57** |
| 62 | config 与炮弹 | F-305 ~ F-306 | 2 | 1 | 1 | 0 | `1.5 + 0.5` = **2.0** | **M58** |
| 63 | 语言键与自检收口 | F-307 ~ F-308 | 2 | 0 | 2 | 0 | `2 × 1.5` = **3.0** | **M59** |
| 64 | 验收与边界 | F-309 ~ F-310 | 2 | 0 | 0 | 2 | `2 × 3.0` = **6.0** | **M60** |
| 65 | 签字与 README | F-311 | 1 | 0 | 1 | 0 | `1.5` = **1.5** | **M61** |
| **合计** | | **F-286 ~ F-311** | **26** | **4** | **16** | **6** | **44.0** | **M50 ~ M61** |

- **裸人日**：`4 × 0.5 + 16 × 1.5 + 6 × 3.0 = 2.0 + 24.0 + 18.0 = **44.0 人日**`
- **含 25% 缓冲**：`44.0 × 1.25 = **55.0 人日**`
- **三条互相校验**：`4 + 16 + 6 = 26` = 任务数；十二个 Phase 小计之和 `0.5 + 6.0 + 3.0 + 7.5 + 2.0 + 3.0 + 3.5 + 6.0 + 2.0 + 3.0 + 6.0 + 1.5 = 44.0`；`44.0 ÷ 3 ≈ 14.7 人日`（稳定 3 路并行，缓冲后 `≈ 18.4`），而**关键路径下界 22.5 人日**才是真实下界。

---

## 6. 本文自检

| # | 检查项 | 判据 | 结果 |
|---|---|---|---|
| 1 | 编号连续 | 任务 `grep -c "^#### F-"` = **26**、区间 `F-286 ~ F-311`；Phase `54 ~ 65` 共 **12** 个；里程碑 `M50 ~ M61` 共 **12** 个（§5.5 表）；裁决 `A-92 ~ A-101` 共 **10** 条，A-92 是第一条且排在 §4.1 | 通过 |
| 2 | 表 A 覆盖 | 54 号 §2 ~ §7 的动作面 **53 / 53** 有落点，未覆盖 **0**；一行多任务的 **4 处**（第 22、29、31、34 行）各自写明拆法 | **53 / 53** |
| 3 | 表 B 覆盖 | 12 类逐类（合计 `28+28+8+13+4+3+1+1+1+3+1+1 = 92`）；口径外 **3 类**各 1 行；4 个构件各有任务 | **19 项** |
| 4 | 表 C 与表 D 覆盖 | 表 C **5 行**（删 3 / 加 2 / 改 0，`176 − 3 + 2 = 175`；`push` 60；顶层节 36）；表 D **7 行**（删 23 / 加 4 / 改名 2 / 改值 2，`340 − 19 = 321 × 2`），逐前缀六段相加 = 321 | **5 行 + 7 行** |
| 5 | 表 E 覆盖 | **12 条**（RT-71 ~ RT-82 连续），每条至少一个任务负责验证且给了「怎么验」 | **12 / 12** |
| 6 | 表 F 覆盖 | **14 条**，每条落到「具体任务的具体步骤」+「具体退路」，无「实现时注意」 | **14 / 14** |
| 7 | 每个任务 ≥ 1 条可观察断言 | 26 个任务逐条读过：全部落在 `latest.log` / `farmstorm-server.toml` / `lang/*.json` / 目录计数 / 源码 grep 计数 / 像素比对 / 游戏内动作上；无「表现正常」「手感良好」 | 通过 |
| 8 | 五个字段齐全 | 26 个任务每个都有 **目标 / 交付物 / 验收断言 / 依赖 / 回滚影响**，无缺项；交付物全部给到 `文件:行号` 或目录 | **26 / 26** |
| 9 | 依赖无环、编号单调 | §1 的「依赖」列与任务标题行一致；每个依赖编号都小于自身编号，按 Phase 排序即拓扑序；无前驱 **5 个**（F-286、F-287、F-290、F-295、F-302） | 通过 |
| 10 | 中间态窗口自洽 | 窗口 = **F-287（开）→ F-308（关）**；九段状态表逐段给真实计数；F-287 是唯一引用 98 的任务、F-308 是唯一写 92 的任务；语言面关窗在 F-307 | 通过 |
| 11 | 六条顺序死命令逐条有落点 | §0.5 的六条分别落在 F-287（#1）、F-290（#2）、F-295（#3）、F-299（#4）、F-302（#5）、F-304（#6）；六条都在 §2 的任务正文里被**再点一次** | 通过 |
| 12 | 「19 项必改」逐项有落点 | 表 A 表下注第 1 条：`19 / 19` 全部给到任务号，无一项悬空 | **19 / 19** |
| 13 | 工时自洽 | `4 × 0.5 + 16 × 1.5 + 6 × 3.0 = 44.0`；十二个 Phase 小计相加 = 44.0；`44.0 × 1.25 = 55.0`；`S + M + L = 4 + 16 + 6 = 26` | 通过 |
| 14 | 「不许发明」 | `92 / 28 / 28 / 1 / 175 / 60 / 36 / 321 × 2 / 21 / 17 / 152 / 84 / 5 / 22 / 0 / 16 / 12 / 14 / 19 / 53 / 26 / 44.0` 全部可追到 52 号的决策号、53 号的 §7 / §9、54 号的 §1 ~ §10，或它们的算术（`98 − 7 + 1 + 1 − 1`、`176 − 3 + 2`、`340 − 23 + 4`、`26 − 6 + 1`、`21 − 4`、`18 − 2`、`154 − 3 − 1 + 2`、`29 − 7`、`84 + 7 − 7`、`2 + 3`）；新增的只有 10 条裁决与 26 个任务号 | 通过 |
| 15 | 禁用词与语气 | 全文无未定事项标记、无占位符；人称只用「作者」「实现者」，无第二人称敬语；结论句一律祈使 | 通过 |
| 16 | 与上游无冲突（已登记的两条除外） | 52 号的 22 条决策、53 号的 31 条边界、54 号的 32 条验收与 12 条风险全部**只引用不改写**；与 54 号不一致的两处（Java 152、旧贴图 7 张）登记在 A-98 / A-97，不改上游文件 | 通过 |
| 17 | 行号复核 | 本文引用的行号逐处按 UTF-8 实读复核：`Farmstorm.java:180-183`、`FarmstormBlocks.java:71-72,102-104`、`FarmstormItems.java:35,91,102-109,112-113,140-151,175-177`、`FarmstormBlockEntities.java:43-45`、`FarmstormMenus.java:12,17-18`、`FarmstormClient.java:62,73-76`、`ConverterRecipe.java:19-25,27-28,106-110`、`ModRecipeProvider.java:60-61,67-99,86-92,187-196,199-209`、`ModLootTableProvider.java:71,81,84,86,89,91,105`、`ModAdvancementProvider.java:82-85,95-106`、`ModBlockStateProvider.java:160-166,172-184`、`ModItemModelProvider.java:55-57,68-70`、`ModItemTagsProvider.java:42-48`、`FarmstormTags.java:35`、`ModLangProvider.java:10,13-16,88-93,103,177,180-187,220,222-223,229,243-245`、`CultivatorBlock.java:44,86-102,99,113-128,140-147,150-161`、`CultivatorBlockEntity.java:131-137,138-144,151-165,216-227,229-239,241-249`、`OutpostCoreBlockEntity.java:120,335-371,537-572,546-547,750-795`、`CultivatorLinkPayload.java:42-45,48-50,53`、`FarmstormNetwork.java:25`、`CultivatorLinkBroadcaster.java:76-78,217-219,227-234`、`CultivatorLinkRenderer.java:41-44,71-79`、`TurretBullet.java:76-90`、`FarmstormConfig.java:19-21,295-305,444-461`、`verify_project.py:334-343,380-393` | 通过 |

---

> **本文的六个可复核判据**（交稿时逐条给读数）：任务 **26** 个 / Phase **12** 个 / 里程碑 **M50 ~ M61** 共 **12** 个 / 裸 **44.0 人日**（缓冲后 **55.0**）/ 六张覆盖表共 `53 + 19 + 5 + 7 + 12 + 14` = **110 行** / 中间态窗口 **F-287 → F-308**。
