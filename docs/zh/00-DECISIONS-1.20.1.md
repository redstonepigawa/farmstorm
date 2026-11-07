# 00 号 1.20.1 移植决策记录

> 项目 / 目标：**farmstorm 的 1.20.1（NeoForge 47.1.106）移植分支** / 基线 = 1.21.1 的 **v0.2.4 已实现态** / 唯一真相源
> 依据：`.work/PORT-1201-HANDOFF.md`（下称 **HANDOVER**，含 12 条探针实测事实 P-01~P-12）、v0.2.4 的 `docs/zh/58`~`63`（1.21.1 侧口径）、作者在移植期拍板的 4 条决定
> 编号约定：本工作区**从 0 重新起号**，与 1.21.1 的 D-225~/F-312~/Phase 66~ **不续接**。

---

## 0. 阅读约定

### 0.1 编号（本工作区从 001 起）

| 系列 | 本工作区起始 | 谁写 |
|---|---|---|
| 文档号 | **00** | 本文 |
| 决策 D-xx | **D-001** | 本文（00 号） |
| 需求 R-x | **R1** | 01 号 |
| 边界 B-xx | **B-01** | 01 号 |
| 任务 F-xx | **F-001** | 03 号 |
| Phase / 里程碑 | **1 / M1** | 03 号 |
| 硬约束 HC-xx | **HC-001** | 04 号 |
| 禁止行为 N-xx | **N-001** | 04 号 |
| 风险 RT-xx | **RT-001** | 02 号 |
| 裁决 A-xx | **A-001** | 03 号 |

四条规矩：① 本文只发 `D-xx`；② 1.21.1 的 `D-001`~`D-254` 一律以 1.21.1 主线文档为准，本文**只引用不覆盖**；③ 引用 1.21.1 编号时必须写全前缀（如「1.21.1 的 D-208」）；④ 本工作区编号一旦发布不再复用。

### 0.2 术语表（判定边界，不是同义词循环）

**移植面**
：从 1.21.1 搬到 1.20.1 时必须改的东西——`net.neoforged.*` 的 189 行 import / 65 个符号（HANDOVER §4）、5 个子系统（HANDOVER §5）、构建链（HANDOVER §3）。判定边界：移植面**不含**玩法逻辑，逻辑一行不改。

**目标面**
：1.20.1 的**运行时与 API 形态**——`net.minecraftforge.*` 命名空间、`RegistryObject`、`SimpleChannel`、JSON 配方、NBT 存档、`pack_format 15`、`META-INF/mods.toml`。判定边界：目标面**不是**「另一个 mod」，玩法与数值必须与 v0.2.4 逐项一致。

**子系统重写**
：不能靠改 import 完成的五块——数据组件/附件、网络、配方、进度、效果与投射物（HANDOVER §5）。判定边界：它**不是**「重构」，接口契约按 1.20.1 重建，行为按 v0.2.4 保留。

**数字面**
：注册对象 / config / 语言键 / 网络包 / 进度 / 配方 / listing 这七组读数。1.20.1 侧唯一变化是**注册面 92 → 91**（`data_component_type` 不存在），其余六组冻结。

### 0.3 四字段写法

每条决策固定 **结论 / 依据 / 落地位置 / 补充**，顺序不变；结论是祈使句，依据必须追到 HANDOVER 的 P 编号或 v0.2.4 的 D/F 编号或源码 `文件:行号`。

---

## 1. 决策清单（D-001 ~ D-014）

### 1.1 范围与工作区（D-001 ~ D-004）

#### D-001 · 目标 = 1.20.1 / NeoForge 47.1.106，基线 = 1.21.1 v0.2.4 已实现态
- **结论**：本分支的 `minecraft_version=1.20.1`、`neo_version=1.20.1-47.1.106`；基线取 1.21.1 的 **v0.2.4 已实现态**（151 个 Java 文件 / 注册面 92 / config 175 / 语言键 321 × 2）。
- **依据**：作者决定 A（完整移植）；HANDOVER §1 基线快照；HANDOVER P-02（坐标）、P-03（NG7 7.0.171 可用）。
- **落地位置**：`gradle.properties`（`minecraft_version` / `neo_version`）；`docs/zh/00`~`05`。
- **补充**：基线口径写「已实现」不写「已发布」。否决「以 1.21.1 的某次中间提交为基线」——v0.2.4 是唯一有 32 条技术验收 + 31 条边界 + 签字可对照的完整态。

#### D-002 · 工作区隔离：`D:/project_farm1.20.1`，工具链复用 1.21.1 主线
- **结论**：源码与文档落在新工作区 `D:/project_farm1.20.1`；`Gradle 8.10 / JDK 21 / 镜像代理 / java.base 补丁` 一律复用 `D:/project_farm/.tools`，**不复制、不新建**。
- **依据**：作者决定；HANDOVER P-04（代理零改动可用）、P-05（JDK 17 用 Mojang 自带）、P-06（installations.paths 逗号）、P-07（仓库外构建受沙箱限制）。
- **落地位置**：`D:/project_farm1.20.1/README.md` 的工具链段；构建命令一律走 `D:/project_farm/.tools/...`。
- **补充**：**P-07 是硬约束**：本工作区外的 Gradle 子进程会被拒绝写入，所以在本工作区跑构建的前提是「该目录本身是会话工作区」或「从 DSH 之外的终端跑」。否决「复制一份 .tools 进新工作区」：会让镜像代理与 JDK 补丁出现第二份真相。

#### D-003 · 文档编号从 0 起，六件套 + 各编号系列从 001
- **结论**：六件套为 `00-DECISIONS` / `01-DESIGN` / `02-TECH` / `03-TASKS` / `04-PROMPT-MAIN` / `05-PROMPT-TASKS`；`D/F/Phase/HC/N/RT/A` 各系列从 001 起，**不与 1.21.1 续接**。
- **依据**：作者决定 4；`D:/project_farm1.20.1/README.md` 的目录约定。
- **落地位置**：`docs/zh/00`~`05`。
- **补充**：交接文档 `HANDOVER-1.20.1.md` **不属于六件套**（它是输入），放根目录。引用 1.21.1 的编号时必须带「1.21.1 的」前缀，避免两个 D-001 混淆。

#### D-004 · 版本号**不变**（沿用 0.2.4），靠产物名里的游戏版本分辨
- **结论**：`mod_version` 与 1.21.1 主线保持同一序列，移植**不改号**（仍 `0.2.4`）；两个平台靠产物名区分：`farmstorm-0.2.4+mc1.20.1.jar` 与 `farmstorm-0.2.4+mc1.21.1.jar`。
- **依据**：作者裁定（移植期）——「模组版本号不要变，可以通过游戏版本号分辨」；`build.gradle` 的 `version = mod_version + "+mc" + minecraft_version` 本身已带 MC 后缀。
- **落地位置**：`gradle.properties`（不动 `mod_version`）；`一键构建.cmd` / `检查环境.cmd` 的 jar 名按新平台改；`README.md`。
- **补充**：`mods.toml` 的 `version` 也随之为 `0.2.4`，与主线同名，但 MC 依赖范围不同（`[1.20.1,1.21)` vs `[1.21.1,1.22)`）。**连带约束**：两个平台 jar 文件名不同、`modid` 与版本号相同，同实例只会加载其中一份 ⇒ 安装说明必须写清「按实例的 MC 版本各放一份，不要混放」。

### 1.2 构建链（D-005 ~ D-006）

#### D-005 · 构建插件从 `moddev` 换成 `net.neoforged.gradle.userdev`（NG7）
- **结论**：`build.gradle` 改用 `id 'net.neoforged.gradle.userdev' version '7.0.171'`；依赖行写 `implementation "net.neoforged:forge:1.20.1-47.1.106"`。
- **依据**：HANDOVER P-01（`moddev` 2.0.148 找不到 `net.neoforged:neoforge` 的任何 1.20.1 版本）、P-02（1.20.1 的 artifactId 是 `forge`）、P-03（NG7 7.0.171 + NeoForm 全链路实测通过）。
- **落地位置**：`build.gradle`（plugins / runs / dependencies）、`settings.gradle`（pluginManagement 加 NeoForged maven）、`gradle.properties`。
- **补充**：`runs` 块用 NG7 的 DSL（`modSource project.sourceSets.main`、`server { programArgument '--nogui' }`）。否决「留在 moddev 上改 `neoForge.version`」：插件把 artifactId 硬编码成 `neoforge`，无配置项可改。

#### D-006 · JDK 17 用 Mojang 自带运行时，`installations.paths` 逗号分隔
- **结论**：`org.gradle.java.installations.paths` 写 `D:/project_farm/.tools/jdk21,C:/Users/DASHpig/AppData/Roaming/.minecraft/runtime/java-runtime-beta`；`auto-detect=false` 保持。
- **依据**：HANDOVER P-05（后者是带 javac 的完整 JDK 17.0.15）、P-06（分号写法会把它当成一条不存在的路径）。
- **落地位置**：`gradle.properties`。
- **补充**：Gradle 守护进程继续跑 JDK 21（需要 `--patch-module java.base=...jdk-patch/classes` 绕过本机 `Files.isWritable` 恒 false），编译任务跑 JDK 17 toolchain；两者共存已被探针验证。

### 1.3 代码面（D-007 ~ D-012）

#### D-007 · 命名空间整体搬迁到 `net.minecraftforge.*`，65 个符号逐个对版本
- **结论**：把 189 行 `net.neoforged.*` import 全部换到 `net.minecraftforge.*`；65 个符号按 HANDOVER §4 的表逐个处理，其中 **8 个不是纯改前缀**（`DeferredHolder→RegistryObject`、`DeferredBlock/DeferredItem→无对应`、`NeoForgeRegistries→ForgeRegistries`、`ModConfigSpec→ForgeConfigSpec`、`NeoForge→MinecraftForge`、`EventHooks→ForgeEventFactory`、`EventBusSubscriber→Mod.EventBusSubscriber`、`Tick*Event→TickEvent.*`）。
- **依据**：HANDOVER P-09（按 `net.neoforged.*` 写直接 26 个编译错误）、P-10（三处实测改名）、§4 全表。
- **落地位置**：全仓 `src/main/java`。
- **补充**：判据是 `grep -c "net.neoforged." src/main/java` = 0 且构建退出码 0；**不允许**用「留一层 `net.neoforged` 兼容包」过关（那会造出第二份真相）。

#### D-008 · 网络层重写为 `SimpleChannel`，协议串从 `"1"` 起
- **结论**：两个 S2C 包（`core_list` / `cultivator_link`）改用 `NetworkRegistry.newSimpleChannel(...)` + 手写 `encode/decode/handle`；`PROTOCOL_VERSION`（等价物：信道版本串）取 **`"1"`**；包数仍 **2**，不加第三个。
- **依据**：作者决定 1；HANDOVER §4.6（`RegisterPayloadHandlersEvent` / `IPayloadContext` 在 1.20.1 不存在）、P-10（`sendToPlayer` → `send(PacketDistributor.PLAYER...)`）。
- **落地位置**：`network/CoreListPayload`、`network/CultivatorLinkPayload`、`network/FarmstormNetwork`、两个 broadcaster 的发送点。
- **补充**：协议串 `"1"` 与 1.21.1 的 `"3"` **必然互斥**，混装时握手直接拒绝（不崩）。`Link` 的 `Kind` 字段语义必须保留（红线）。

#### D-009 · 数据组件与实体附件**不砍**：改 NBT / Capability 重写
- **结论**：`temporary_enchantments`（组件）与 `ai_suspend`（附件）在 1.20.1 侧用 **NBT 或 Capability** 重写，语义与 v0.2.4 一致（临时附魔的堆叠/到期、AI 原值落盘/恢复）。
- **依据**：作者决定 2（不砍）；HANDOVER §4.7（`AttachmentType` 在 1.20.1 不存在）、§5 子系统 1。
- **落地位置**：`util/TempEnchantHandler`、`util/TempEnchantRecord`、`registry/FarmstormAttachments`、`Farmstorm`（注册面）。
- **补充**：这一条让注册面**少 1 类**（`data_component_type` 不存在）⇒ 92 → **91**（D-013）。否决「用计分板」：那是把数据挪到玩家身上，与「物品自带」语义不符。**实现落定（回灌）**：两处都走 NBT——物品键 `FarmstormTempEnchants`、实体持久 NBT 键 `FarmstormAiSuspend`（`registry/FarmstormAttachments` 工具类，14 个存取点）；Capability 未采用，Capability 化是可选的后续项。

#### D-010 · 配方与进度按 1.20.1 的接口重建
- **结论**：`ConverterRecipe` 改 1.20.1 的 `Recipe`（`matches(Container, Level)` / `assemble(Container, RegistryAccess)` / `getSerializer()`）+ `RecipeSerializer.fromJson/toNetwork/fromNetwork`；`ModAdvancementProvider` 改用 `Advancement` / `Advancement.Builder`。
- **依据**：HANDOVER §4.5、§5 子系统 3/4；v0.2.4 的 21 条配方与 16 条进度是判据基线。
- **落地位置**：`recipe/ConverterRecipe`、`recipe/ConverterRecipeSerializer`、`datagen/ModRecipeProvider`、`datagen/ModAdvancementProvider`。
- **补充**：21 条 `converting` 配方的**语义**（单向、无成本、result 恒 1）不变，但 JSON 形状会变（`Ingredient` 写法随版本）。

#### D-011 · 效果与投射物按 1.20.1 钩子改名
- **结论**：`MobEffect` 的 tick 谓词用 **`isDurationEffectTick`**；投射物用 **`onHitEntity` / `onHitBlock`**；其余钩子逐类核。
- **依据**：HANDOVER §5 子系统 5。
- **落地位置**：`effect/*`（4 个）、`entity/TurretBullet`、`entity/ThrownCropEntity`。
- **补充**：`TurretBullet` 的 300 刻寿命与 `Q弹`/`混乱` 的停手语义必须保留（v0.2.4 验收 #25 与 B-55/B-56 的对照项）。

#### D-012 · 现有玩法行为与数值一行不改
- **结论**：除上述接口层外，四道闸、培养器 17 个投入物与两组产出、交易经验 2/5/10/15/30、浮窗底色 30.2%、放置音客户端预测、绑定统一（`processing_block` 标签 + kind 单一映射）全部按 v0.2.4 原样实现。
- **依据**：v0.2.4 的 32 条技术验收 + 31 条边界；作者决定（不砍线）。
- **落地位置**：全仓。
- **补充**：任何「顺手改平衡」都算越界，必须回到 01 号走边界条目。

### 1.4 数字面与存档（D-013 ~ D-014）

#### D-013 · 数字面：注册面 92 → **91**，其余六组冻结
- **结论**：注册面 12 类 → **11 类**（去 `data_component_type`），总数 **91**；server config **175** / push **60**、client config **3**、语言键 **321 × 2**、网络包 **2**、进度 **16**、转换配方 **21**、培养器投入物 **17**、交易 listing **26** 全部不变。
- **依据**：HANDOVER §6；v0.2.4 签字记录。
- **落地位置**：`Farmstorm#logSelfCheck`、`tools/verify_project.py` 的 `EXPECTED` / `EXPECTED_COUNTS`、`docs/zh/02-TECH`。
- **补充**：检查器**必须继续从文档现算**，不许把这七个数字写死；1.20.1 侧需要一份新的 config 期望来源（对应 1.21.1 侧的 54 号解析器）。

#### D-014 · 存档不迁移：只支持新世界
- **结论**：不写 1.21.1 → 1.20.1 的存档转换；老世界打不开属**预期**，写进已知限制。
- **依据**：作者决定 3；方块/组件 id 与 `data_component_type` 在目标面不存在。
- **落地位置**：`docs/zh/01-DESIGN` 的已知影响；`README.md`。
- **补充**：`pack_format` 34 → **15**、元数据 `neoforge.mods.toml` → **`mods.toml`** 也在此条下（都是「朝向目标面」的格式切换，不是兼容层）。

---

## 2. 作者裁定索引（4 条）

| # | 议题 | 裁定 | 落到决策 |
|---|---|---|---|
| 1 | 协议串 | 从 `"1"` 起 | D-008 |
| 2 | 数据组件那条线 | **不砍**，NBT/Capability 重写 | D-009 |
| 3 | 存档 | 不迁移，只支持新世界 | D-014 |
| 4 | 文档 | 仍六件套，编号从 0 | D-003 |
| 5 | 模组版本号 | **不改号**（仍 0.2.4），靠 `+mc<游戏版本>` 分辨 | D-004 |

---

## 3. 对 1.21.1 v0.2.4 的增 / 改 / 删

**改（接口层，16 项）**：命名空间 189 行 / 65 符号（D-007）、构建插件（D-005）、toolchain 路径（D-006）、两个 payload（D-008）、`FarmstormNetwork`（D-008）、`ConverterRecipe` + Serializer（D-010）、`ModRecipeProvider`（D-010）、`ModAdvancementProvider`（D-010）、4 个 `MobEffect`（D-011）、`TurretBullet` / `ThrownCropEntity`（D-011）、`TempEnchantHandler` / `TempEnchantRecord`（D-009）、`FarmstormAttachments`（D-009）、`Farmstorm#logSelfCheck`（D-013）、`verify_project.py`（D-013）、`pack.mcmeta` + 元数据文件（D-014）。

**删（1 项）**：`FarmstormDataComponents` 与 `data_component_type` 注册（D-009 / D-013）。

**增（3 项）**：`Capability` 适配（若 `ai_suspend` 走 Capability，D-009）、1.20.1 侧 config 期望解析器（D-013）、`docs/zh/00`~`05` 六份（D-003）。

---

## 4. 红线（5 条）

1. **Mixin 0**：不许引入 `@Mixin` / `mixins.json`。
2. **不加第三个网络包**：两个包的语义必须在 1.20.1 原样成立。
3. **不改 1.21.1 主线**：`D:/project_farm` 只读用它的工具链与文档，不许回写。
4. **不留兼容层**：不许造 `net.neoforged` 兼容包或 `DeferredHolder` 别名类来「少改点」。
5. **不顺手改平衡**：数值与行为以 v0.2.4 为准，任何偏离回到 01 号登记。

---

## 5. 墓碑（已否决 / 已放弃的想法）

1. **「先用 moddev 改 version 试 1.20.1」**：P-01/P-02 已证伪，不要再试。
2. **「下载 Temurin 17」**：P-05 证明 Mojang 自带的就是完整 JDK，下载被 GitHub 的 SNI 阻断挡死。
3. **「在 `D:/farmstorm-probe-1201` 建工程」**：P-07 证明沙箱会拒写，已放弃。
4. **「砍掉临时附魔省 3–5 人日」**：作者决定不砍（D-009）。
5. **「1.20.1 分支改号 0.3.0」**：作者否决 —— 版本号不动，靠产物名里的游戏版本分辨（D-004 现口径）。

---

## 6. 与交接文档的分工

| 文档 | 它定什么 | 本文怎么用 |
|---|---|---|
| `HANDOVER-1.20.1.md` | **实测事实**（P-01~P-12）、65 符号映射表、阶段判据 | 每条决策的「依据」都引它的 P 编号；**不复制**它的表 |
| 本文（00 号） | **口径**：一件事是什么、为什么这样定 | 下游 01~05 只引本文，不自己发明 |
| 01 号（设计） | 玩家可见行为与边界 | — |
| 02 号（技术） | 落点、验收、风险 | — |
| 03 号（任务） | 拆解、排期、裁决 | — |
| 04 / 05 号 | 主提示词 / 阶段提示词 | — |

> 本文自检：14 条决策连续无跳号（D-001~D-014）；4 条作者裁定各有落点；红线 5 条；每条的「依据」都能追到 HANDOVER 的 P 编号或 v0.2.4 的编号。
