# 03 号 1.20.1 移植任务拆分

> 上游：`00-DECISIONS`（D-001~D-014）、`01-DESIGN`（R1~R7 / B-01~B-12）、`02-TECH`（数字面 / 5 子系统 / 25 条验收 / RT-001~RT-010）、`HANDOVER`（P-01~P-12）。
> 本文把移植拆成 **21 个任务 F-001 ~ F-021**、**7 个 Phase**、**6 个里程碑 M1 ~ M6**、**6 条裁决 A-001 ~ A-006**。

---

## 0. 阅读约定

**0.1 任务粒度**：一个任务 = 一次可独立提交、可独立验证的改动；提交后 `build` 退出码 0；自带至少一条可观察断言。

**0.2 复杂度换算**：S = 0.5 人日（单文件或纯字面量替换）、M = 1.5（2~4 文件或一个新行为）、L = 3.0（≥5 文件或跨包状态机 / 序列化 / 协议 / 渲染）。

**0.3 无中间态窗口**：本移植**没有**「总数暂时不成立」的窗口期——数字面在 P1 结束时就应当是 91（不是先 92 再改），任一阶段 `自检 … → OK` 变红即真 bug（A-001）。

**0.4 判据只认五类读数**（同 02 号 §0.1）。

---

## 1. 任务总表

| 任务 | 名称 | Phase | 里程碑 | 复杂度 | 人日 | 依赖 |
|---|---|---|---|---|---|---|
| F-001 | 工程骨架换 NG7（build/settings/gradle.properties/mods.toml/pack.mcmeta） | 1 | M1 | M | 2.0 | — |
| F-002 | 构建与运行脚本适配（gw 包装 / jar 名 / 端口与 run 目录） | 1 | M1 | S | 0.5 | F-001 |
| F-003 | `runServer` 打通（`DownloadAssets` 卡点，RT-001） | 1 | M1 | M | 1.5 | F-001 |
| F-004 | 符号搬迁 A 组：事件总线 / 加载器 / 配置 / 侧标记 | 2 | M2 | M | 2.0 | F-001 |
| F-005 | 符号搬迁 B 组：注册面（`DeferredHolder` → `RegistryObject`） | 2 | M2 | M | 2.5 | F-004 |
| F-006 | 符号搬迁 C 组：客户端事件与 datagen 生成器 | 2 | M2 | M | 2.5 | F-004 |
| F-007 | 符号搬迁 D 组：通用事件 / tick / 村庄 / 爆炸 | 2 | M2 | M | 2.0 | F-004 |
| F-008 | 符号搬迁 E 组：网络骨架（先能编译，行为空） | 2 | M2 | M | 1.5 | F-004 |
| F-009 | 数据组件 → 物品 NBT（02 号 §4.1） | 3 | M3 | L | 3.0 | F-005 |
| F-010 | 实体附件 → 实体持久 NBT（02 号 §4.2） | 3 | M3 | L | 3.0 | F-005 |
| F-011 | 网络重写：SimpleChannel + 协议串 "1"（02 号 §4.3） | 3 | M3 | L | 3.0 | F-008 |
| F-012 | 配方重写（02 号 §4.4） | 3 | M3 | M | 2.5 | F-006 |
| F-013 | 进度重写（02 号 §4.5） | 3 | M3 | M | 2.0 | F-006 |
| F-014 | 效果与投射物改名（02 号 §4.5） | 3 | M3 | M | 1.5 | F-007 |
| F-015 | 存档签名统一（`RegistryFriendlyByteBuf` / `RecipeInput` 清零、`ItemStack.save/of`） | 4 | M4 | M | 2.0 | F-005、F-009 |
| F-016 | 行为回归（25 条验收 + v0.2.4 的 31 条边界） | 4 | M4 | L | 4.0 | F-009 ~ F-015 |
| F-017 | 六件套回灌（02/03 与 04/05 的实测值） | 5 | M5 | S | 1.0 | F-016 |
| F-018 | 检查器 1.20.1 期望源（91 / 11 类 / config 175） | 5 | M5 | M | 2.0 | F-005 |
| F-019 | 验收 / 边界 / 签字三份记录 | 5 | M5 | L | 3.0 | F-016、F-018 |
| F-020 | 打包与安装说明（`dist/` + README） | 6 | M6 | S | 1.0 | F-019 |
| F-021 | 最终签字（红线、数字面、19 项对照） | 6 | M6 | S | 1.0 | F-020 |

**合计**：`2.0+0.5+1.5 + 2.0+2.5+2.5+2.0+1.5 + 3.0+3.0+3.0+2.5+2.0+1.5 + 2.0+4.0 + 1.0+2.0+3.0 + 1.0+1.0 = **43.5 人日**`（裸）；含 25% 缓冲 = **54.4**。

---

## 2. 逐任务（目标 / 交付物 / 验收断言 / 依赖 / 回滚影响）

### Phase 1 · 构建链（M1）

#### F-001 · 工程骨架换 NG7（M · 2.0）
- **目标**：把 `build.gradle` / `settings.gradle` / `gradle.properties` 换成 NG7 版（02 号 §2），元数据改 `META-INF/mods.toml`，`pack_format` 改 15。
- **交付物**：`build.gradle`、`settings.gradle`、`gradle.properties`、`src/main/resources/META-INF/mods.toml`、`pack.mcmeta`。
- **验收**：`build` 退出码 0；`gradle :dependencies` 能解析到 `net.neoforged:forge:1.20.1-47.1.106`；`mods.toml` 里 `loaderVersion="[47,)"`。
- **依赖**：—。
- **回滚**：退回 moddev 后整个 1.20.1 构建不可用（P-01），等于回滚 Phase 1。

#### F-002 · 构建与运行脚本适配（S · 0.5）
- **目标**：`.tools` 侧的包装脚本指向新工作区；jar 名随 `+mc1.20.1`。
- **交付物**：构建命令封装（工作区内脚本或复用主线 `gw.cmd` 的 `-p` 参数）；`一键构建.cmd` / `检查环境.cmd` 的 jar 名。
- **验收**：一条命令能 build；产物名 `farmstorm-0.2.4+mc1.20.1.jar`。
- **依赖**：F-001。
- **回滚**：只影响脚本，不影响源码。

#### F-003 · `runServer` 打通（M · 1.5）
- **目标**：解决 RT-001（`DownloadAssets` 卡住），使服务端能起到 `Done (`。
- **交付物**：预置 assets 或跑 `--nogui` 的最小启动路径；`run/eula.txt`。
- **验收**：日志出现 `Done (` 与 `自检 注册对象 …` 行；`java` 进程可优雅 `stop`。
- **依赖**：F-001。
- **回滚**：只影响运行验证能力，不影响构建。

### Phase 2 · 命名空间搬迁（M2）

#### F-004 · A 组：事件总线 / 加载器 / 配置 / 侧标记（M · 2.0）
- **目标**：`net.neoforged.bus.api.*`、`fml.common.*`、`fml.config.*`、`ModConfigSpec`、`api.distmarker.Dist` 全部迁到 `net.minecraftforge.*`。
- **交付物**：约 20 文件（`Farmstorm`、两个 Config、24 处 `@EventBusSubscriber`）。
- **验收**：`grep -c "net.neoforged.bus"` = 0；`grep -c "net.neoforged.fml"` = 0；`grep -c ModConfigSpec` = 0；`build` 退出码 0。
- **依赖**：F-001。
- **回滚**：与 F-005~F-008 同批回滚（编译互相依赖）。

#### F-005 · B 组：注册面（M · 2.5）
- **目标**：`DeferredRegister` / `DeferredHolder` / `DeferredBlock` / `DeferredItem` / `NeoForgeRegistries` 按 02 号 §3 的 1~3 条改。
- **交付物**：`registry/` 下全部类 + 引用它们的 6 个文件。
- **验收**：`grep -c DeferredHolder` = 0；`grep -c DeferredBlock\|DeferredItem` = 0；`build` 退出码 0。注册面**不作 92 断言**：本分支无中间态窗口（§0.3 / A-001），实测该处已是 **91（11 类）**。
- **依赖**：F-004。
- **回滚**：影响面最广（约 12 处 / 6 文件），回滚代价高。

#### F-006 · C 组：客户端事件与 datagen 生成器（M · 2.5）
- **目标**：`client.event.*` 与 `client.model.generators.*` 迁包；tick 类事件按 02 号 §3 第 8 条改成 `TickEvent.*`。
- **交付物**：`client/` 与 `datagen/` 下相关类。
- **验收**：`grep -c "neoforge.client"` = 0；`build` 退出码 0；`runData` 能跑（若有 datagen 入口）。
- **依赖**：F-004。
- **回滚**：与 F-005 同批。

#### F-007 · D 组：通用事件 / tick / 村庄 / 爆炸（M · 2.0）
- **目标**：`neoforge.event.*`、`NeoForge.EVENT_BUS`、`EventHooks` 按 02 号 §3 第 5、6 条改。
- **交付物**：`event/`、`combat/`、`explosion/` 下相关类。
- **验收**：`grep -c "neoforge.event"` = 0；`grep -c EventHooks` = 0；`build` 退出码 0。
- **依赖**：F-004。
- **回滚**：同批。

#### F-008 · E 组：网络骨架（M · 1.5）
- **目标**：先把网络类改成能编译的 `SimpleChannel` 骨架（消息体仍是空的），行为留给 F-011。
- **交付物**：`network/FarmstormNetwork` + 两个消息类骨架。
- **验收**：`grep -c CustomPacketPayload` = 0；`build` 退出码 0。
- **依赖**：F-004。
- **回滚**：F-011 依赖它，回滚需同批。

### Phase 3 · 子系统重写（M3）

#### F-009 · 数据组件 → NBT（L · 3.0）
- **目标**：按 02 号 §4.1 把 `temporary_enchantments` 改成物品 NBT `FarmstormTempEnchants`；删 `FarmstormDataComponents` 与 `data_component_type` 注册。
- **交付物**：`util/TempEnchantHandler`、`util/TempEnchantRecord`、`Farmstorm`（注册面）。
- **验收**：`grep -c DataComponent` = 0；注册对象变 **91**；`/data get entity <玩家> Inventory` 能看到该键；到期后键消失。
- **依赖**：F-005。
- **回滚**：会把注册面退回 92，须与 F-018（检查器）一并回滚。

#### F-010 · 实体附件 → 实体持久 NBT（L · 3.0）
- **目标**：按 02 号 §4.2 把 `ai_suspend` 落到**实体持久 NBT**（`Mob#getPersistentData()`）+ 工具类 `FarmstormAttachments`，键 `FarmstormAiSuspend`；**不做 Capability**（Capability 化是可选的后续项）。
- **交付物**：`registry/FarmstormAttachments`（`getAiSuspend` / `setAiSuspend` / `removeAiSuspend`）、`effect/` 与 `trap/` 的调用点（共 14 个存取点）。
- **验收**：`grep -c AttachmentType` = 0；`/data get entity <怪>` 含该键；震动豆 / Q弹 的停手与恢复符合 02 号 #19。
- **依赖**：F-005。
- **回滚**：停手功能失效（观感级 bug），须与 F-016 的验收一并作废。

#### F-011 · 网络重写（L · 3.0）
- **目标**：按 02 号 §4.3 完成两个包的真实收发与协议串 "1"。
- **交付物**：`network/CoreListPayload`、`network/CultivatorLinkPayload`、`network/FarmstormNetwork`、两个 broadcaster 的发送点。
- **验收**：发送点 2 处；客户端 `ClientCultivatorLinks` 有内容；双端日志无 `Unknown packet`；混装被拒且不崩（02 号 #25）。
- **依赖**：F-008。
- **回滚**：连线与浮窗双失效。

#### F-012 · 配方重写（M · 2.5）
- **目标**：按 02 号 §4.4 实现 `ConverterRecipe` / `Serializer`（1.20.1 接口）。
- **交付物**：`recipe/ConverterRecipe`、`recipe/ConverterRecipeSerializer`、`datagen/ModRecipeProvider`。
- **验收**：目录 21 份 `converting`；解析报错 0；21 条输入都能被转换器认。
- **依赖**：F-006。
- **回滚**：转换器无法工作（史诗档不可得）。

#### F-013 · 进度重写（M · 2.0）
- **目标**：按 02 号 §4.5 把 16 条进度改到 `Advancement.Builder`。
- **交付物**：`datagen/ModAdvancementProvider`、`util/FarmstormAdvancements`。
- **验收**：目录 16 份；`converter` / `first_conversion` 仍在且 id 不变。
- **依赖**：F-006。
- **回滚**：进度不可授予。

#### F-014 · 效果与投射物（M · 1.5）
- **目标**：按 02 号 §4.5 改 4 个 `MobEffect` 与 2 个投射物。
- **交付物**：`effect/*`、`entity/TurretBullet`、`entity/ThrownCropEntity`。
- **验收**：`grep -c shouldApplyEffectTickThisTick` = 0；`grep -c hitTargetOrDeflectSelf` = 0；`build` 退出码 0；炮台 300 刻消失（02 号 #23）。
- **依赖**：F-007。
- **回滚**：效果 / 投射物行为退化。

### Phase 4 · 存档签名与回归（M4）

#### F-015 · 存档签名统一（M · 2.0）
- **目标**：`RegistryFriendlyByteBuf` / `RecipeInput` **两类**残留清零，`ItemStack.save/of` 改无 provider 版本。**`HolderLookup` 不作判据**：1.20.1 的 datagen API（`ForgeAdvancementProvider`、`DatapackBuiltinEntriesProvider`）本来就收 `HolderLookup.Provider`，当前 23 处全是合法用法。
- **交付物**：约 15 文件（方块实体、菜单、物品）。
- **验收**：`grep -c RegistryFriendlyByteBuf` = 0；`grep -c RecipeInput` = 0；`grep -c HolderLookup` **允许 > 0**（23 处合法用法，不作判据）；`build` 退出码 0。
- **依赖**：F-005、F-009。
- **回滚**：编译不过，必须整段回滚。

#### F-016 · 行为回归（L · 4.0）
- **目标**：跑 02 号 §7 的 25 条验收 + v0.2.4 的 31 条边界。
- **交付物**：`.work/v024-verify/` 下的记录（验收 / 边界两份）。
- **验收**：25 / 25 有读数；31 条边界逐条成立或不成立登记；不通过项 0 或逐条说明。
- **依赖**：F-009 ~ F-015。
- **回滚**：记录作废，源码不受影响。

### Phase 5 · 文档与检查器（M5）

#### F-017 · 六件套回灌（S · 1.0）
- **目标**：把实测值回灌 02 / 03（数字面、Java 文件数），并核对 04 / 05 的判据仍成立。
- **交付物**：`docs/zh/02`~`05`。
- **验收**：文档里的数字与检查器输出逐项一致。
- **依赖**：F-016。
- **回滚**：无源码影响。

#### F-018 · 检查器 1.20.1 期望源（M · 2.0）
- **目标**：`verify_project.py` 的 `EXPECTED` / `EXPECTED_COUNTS` 改 11 类 91，并补 1.20.1 的 config 期望解析器（从 02 号 §1 现算）。
- **交付物**：`tools/verify_project.py`。
- **验收**：`91 / 91`、`175`、`321`、`FAIL 0`；把某个计数改错会当场 FAIL（负向自测）。
- **依赖**：F-005。
- **回滚**：检查器失真。

#### F-019 · 三份记录（L · 3.0）
- **目标**：验收 / 边界 / 签字三份记录齐全。
- **交付物**：`.work/v024-verify/acceptance.md`、`boundaries.md`、`signoff.md`。
- **验收**：25 / 25、31 / 31、八项读数齐。
- **依赖**：F-016、F-018。
- **回滚**：交付面失去复核。

### Phase 6 · 打包与签字（M6）

#### F-020 · 打包与安装说明（S · 1.0）
- **目标**：先跑 **SRG 重映射**（`remap-1.20.1.cmd` → `-srg.jar`；NG7 7.0.171 没有 reobf 任务，`build` 出的官方名 jar 装进真实实例会 `NoSuchMethodError`），jar 才进 `dist/`；README 与安装说明写「按 MC 版本各放一份」与协议串差异。
- **交付物**：`dist/farmstorm-0.2.4+mc1.20.1.jar`（与 `build/libs/…-srg.jar` 同字节，均 1,138,762 B）、`README.md`、`安装说明.md`。
- **验收**：jar 可被 1.20.1 实例加载（内容含 SRG 名，如 `m_216439_`）；`dist/` 里不出现开发用的官方名 jar（1,129,491 B）；说明里两个平台各一行。
- **依赖**：F-019。
- **回滚**：无源码影响。

#### F-021 · 最终签字（S · 1.0）
- **目标**：红线（Mixin 0 / 包数 2 / 不改主线 / 无兼容层 / 不动平衡）逐条给读数。
- **交付物**：`signoff.md` 的红线节。
- **验收**：五条红线各 1 行读数；数字面 8 项齐。
- **依赖**：F-020。
- **回滚**：无。

---

## 3. 覆盖表

**表 A · 65 个符号 → 任务**：A 组 6（F-004）、B 组 5（F-005）、C 组 23（F-006）、D 组 25（F-007）、网络 3（F-008 / F-011）、附件 1（F-010）、组件相关 5 处（F-009）——合计 **65 / 65**。

**表 B · 5 个子系统 → 任务**：组件（F-009）、附件（F-010）、网络（F-011）、配方（F-012）、进度（F-013）、效果投射物（F-014）——**5 / 5**（效果投射物按 02 号归入子系统 5）。

**表 C · 25 条验收 → 任务**：#1~#7 → F-001~F-008；#8~#16 → F-009~F-014；#17~#24 → F-016；#25 → F-011。**25 / 25**。

**表 D · 10 条风险 → 任务**：RT-001 → F-003；RT-002 → 全阶段（构建会话约束）；RT-003~RT-008 → F-004~F-014 各自核实；RT-009 → F-011；RT-010 → F-020。**10 / 10**。

---

## 4. 裁决台账（A-001 ~ A-006）

**A-001 · 本移植无中间态窗口**：数字面（91）在 F-009 落地那一刻就成立，其余阶段一律「红即真 bug」，不许记「期望红」。

**A-002 · 符号搬迁按 5 组拆、不按文件拆**：一组一个提交，便于回滚；同组内按包名顺序改，避免「改了一半编译不过」。

**A-003 · F-009 与 F-018 必须同批评审**：删 `data_component_type` 后检查器会立刻红（期望 92 vs 实测 91），两者之间不允许留一个「检查器红着」的提交。

**A-004 · 网络先骨架后行为（F-008 → F-011）**：先用空消息体让编译通过，再填编解码，避免把「编译」和「协议语义」两类错误混在一起排查。

**A-005 · 验收记录按 v0.2.4 的形状**：验收 / 边界 / 签字三份，不合并（沿用 1.21.1 侧 A-101 的分份纪律）。

**A-006 · 版本号不改（D-004）导致的重名风险由文档兜**：安装说明与 README 必须写「按 MC 版本各放一份」，且 `mods.toml` 的 MC 依赖范围写成 `[1.20.1,1.21)`。

---

## 5. 执行顺序与人日汇总

**串行链**：F-001 → F-004 → F-005 → F-009 → F-015 → F-016 → F-017/F-018 → F-019 → F-020 → F-021。

**可并行簇**：① F-002 与 F-004；② F-006 / F-007 / F-008（都在 F-004 之后）；③ F-012 / F-013（F-006 之后）；④ F-010 与 F-011（F-005 / F-008 之后）。并行上限 3。

| Phase | 任务 | 人日 | 里程碑 |
|---|---|---|---|
| 1 构建链 | F-001 ~ F-003 | 4.0 | M1 |
| 2 命名空间 | F-004 ~ F-008 | 10.5 | M2 |
| 3 子系统 | F-009 ~ F-014 | 15.0 | M3 |
| 4 存档与回归 | F-015 ~ F-016 | 6.0 | M4 |
| 5 文档与检查器 | F-017 ~ F-019 | 6.0 | M5 |
| 6 打包与签字 | F-020 ~ F-021 | 2.0 | M6 |
| **合计** | **21 个任务** | **43.5 裸 / 54.4 含缓冲** | M1 ~ M6 |

> 本文自检：21 个任务连续无跳号；每任务五段齐全；四张覆盖表分别 65 / 5 / 25 / 10 全命中；6 条裁决；人日 `4.0+10.5+15.0+6.0+6.0+2.0 = 43.5` 自洽。
