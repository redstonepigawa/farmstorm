# Farmstorm 1.21.1 → 1.20.1（NeoForge 47）移植交接文档

> 作者决策：**走 A 路线——完整移植**，保留全部行为与注册对象（92 → 91，见 §6）。
> 基线：**v0.2.4 已实现态**（`mod_version=0.2.4`，HEAD 在 v0.2.4 收尾提交之后）。
> 事实分两类：**【已证实】**＝本机探针真跑出来的；**【待验证】**＝按 Forge 47 已知口径推断，实现时必须当场确认。
> 探针工程：`D:/project_farm/.probe-1201`（已在 `.gitignore` 第 39 行忽略，不进版本库，可直接复用或删除）。
> 本文路径一律用 `/` 书写，Windows 下等价（PowerShell 与 Gradle 都接受）。

---

## 0. 一句话结论

**能移植，但这是「往回跨三代 API」**：1.20.1 的 NeoForge 47 仍叫 **Forge**——`net.minecraftforge.*` 命名空间、`RegistryObject`、`SimpleChannel`、JSON 配方、NBT 存档；
而当前代码是按 1.21.1 的 NeoForge 写的——`net.neoforged.*`、`DeferredHolder`、`StreamCodec`、codec 配方、数据组件。
**逻辑几乎不用改，接口要整层换**：仓库有 **189 行 / 57 个文件** 的 `net.neoforged.*` import、**65 个不同符号**，另有 5 个子系统必须重写。

**修正后的人日：38–60 裸 / 48–75 含 25% 缓冲**（比最初 40–66 略降：JDK 17 与镜像代理两块不确定性已被探针消掉）。

---

## 1. 基线快照（移植前必须复核，全部实测）

| 项 | 值 | 复核命令 |
|---|---|---|
| Java 源文件 | **151** | `(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` |
| Java 代码行 | **15,752** | 同上 + `Get-Content | Measure-Object -Line` |
| 注册对象 | **92**（items 28 / blocks 28 / BE 8 / entity 13 / effect 4 / sound 3 / recipe_type 1 / recipe_serializer 1 / tab 1 / damage_type 3 / data_component 1 / menu 1） | 启动日志自检整行 |
| server config | **175 键 / 60 push / 36 顶层节** | `python -X utf8 tools/verify_project.py` |
| client config | **3 键**（`hud.enabled/x/y`） | 同上 |
| 语言键 | **321 × 2**（`configuration` 组 178） | 同上 |
| 网络包 | **2**（`core_list` / `cultivator_link`，S2C 只读，协议串 `"3"`） | `grep playToClient(` |
| Mixin | **0** | `grep @Mixin` |
| 进度 / 转换配方 / 培养器投入物 / 交易 listing | 16 / 21 / 17 / 26 | 见 v0.2.4 签字记录 |
| 资源 | `src/main/resources` 131 文件；`src/generated/resources` 232 文件；`pack_format = 34` | — |
| 构建链 | `net.neoforged.moddev` **2.0.148** + Gradle **8.10** + JDK **21**（`.tools/jdk21`） | `build.gradle:4`、`gradle.properties` |

---

## 2. 探针已证实的事实（12 条，逐条给「怎么验的」）

| # | 事实 | 怎么验的 | 对移植的意义 |
|---|---|---|---|
| **P-01** | **现有 `moddev` 插件不支持 1.20.1** | 用 1.20.1 建工程 → `Could not find net.neoforged:neoforge:47.1.106`；换成 `1.20.1-47.1.106` 仍找不到 | 构建链**必须换旧插件** `net.neoforged.gradle.userdev`（NG7） |
| **P-02** | 1.20.1 的 NeoForge 坐标是 **`net.neoforged:forge:1.20.1-47.1.106`** | 探针 HEAD 该 pom → **200**；`net.neoforged:neoforge` 的 maven-metadata 里最新是 `20.6.x`（该 artifactId 从 20.2+ 才有） | 依赖行写 `implementation "net.neoforged:forge:${neo_version}"` |
| **P-03** | **NG7 `7.0.171` 可用**，NeoForm 1.20.1 全链路跑通 | `neoFormExtractServer → … → neoFormRecompile` 全执行成功（仅 deprecation 提示），随后 `compileJava` 成功 | 插件版本有据可依，不必再试错 |
| **P-04** | **现有镜像代理零改动可服务 1.20.1** | `.tools/neoforge-proxy.mjs` + `.tools/init.gradle` 把 `maven.neoforged.net` 改写为 `127.0.0.1:8899`；1.20.1 依赖与 NeoForm 固件全部经它拉通 | 不写新代理；但**构建期间代理必须存活**（被回收时立刻 `Connection refused`） |
| **P-05** | **本机已有完整 JDK 17，不必下载** | `%APPDATA%/.minecraft/runtime/java-runtime-beta` 的 `release` 写 `JAVA_VERSION="17.0.15"`，且 `bin/javac.exe` **存在**（JDK 不是 JRE）；Gradle 用它当 toolchain 编译成功 | 省掉 180 MB 下载 |
| **P-06** | `org.gradle.java.installations.paths` **必须逗号分隔** | 用分号写成一条 → `Directory 'D:/…jdk21;C:/…java-runtime-beta' does not exist` | 改 `gradle.properties` 别踩 |
| **P-07** | **仓库外构建不可行（沙箱）** | 在 `D:/farmstorm-probe-1201` 建工程：目录可手动创建，但 Gradle 派生进程写 `.gradle/` 报 `Cannot create directory` / `拒绝访问`（`FileHasher`） | 移植工作区**必须留在仓库内** |
| **P-08** | **垂直切片编译通过** | 作物方块（`extends CropBlock`）+ 方块实体（`saveAdditional(CompoundTag)` / `load(CompoundTag)`，无 Provider）+ 2 个注册对象 + 1 条 `SimpleChannel` S2C ⇒ `BUILD SUCCESSFUL`，产出 `probe1201-0.0.1+mc1.20.1.jar`（9,933 B） | 注册面 / 作物 / 存档 / 网络四类风险各有实证 |
| **P-09** | 1.20.1 用 **`net.minecraftforge.*`** 命名空间 | 第一版按 `net.neoforged.*` 写 → **26 个编译错误**，全部是「包不存在 / 符号找不到」；改到 `net.minecraftforge.*` 后只剩 1 个方法签名错 | 搬运量主体就是这 189 行 import |
| **P-10** | 三处**非纯改包名**（已实测） | `DeferredHolder` → **`RegistryObject`**；`SimpleChannel.sendToPlayer(msg, player)` → **`send(PacketDistributor.PLAYER.with(() -> player), msg)`**；`@Mod` / `@SubscribeEvent` / `IEventBus` 在 `net.minecraftforge.fml.common` / `eventbus.api` | 「换个前缀」不够，得逐符号过（§4） |
| **P-11** | **服务端启动未验证** | NG7 的 `runServer` 卡在 `neoFormJoined1.20.1-...DownloadAssets`（1.20.1 资源包 ~300 MB），4 分钟无进展，已停 | 第一件事是把这步打通 |
| **P-12** | 元数据文件是 **`META-INF/mods.toml`**（不是 `neoforge.mods.toml`） | 探针按 `mods.toml` 写（`modLoader="javafml"`、`loaderVersion="[47,)"`），随 jar 编译打包 | 1.21.1 的 `neoforge.mods.toml` 要改名并改字段 |

---

## 3. 目标工程配置（可直接照抄探针）

`gradle.properties` 关键四行：

```properties
minecraft_version=1.20.1
neo_version=1.20.1-47.1.106
org.gradle.java.installations.auto-detect=false
org.gradle.java.installations.paths=D:/project_farm/.tools/jdk21,C:/Users/DASHpig/AppData/Roaming/.minecraft/runtime/java-runtime-beta
```

`build.gradle` 骨架：

```groovy
plugins {
    id 'java-library'
    id 'net.neoforged.gradle.userdev' version '7.0.171'
}
java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
runs {
    configureEach { systemProperty 'forge.logging.console.level', 'debug'; modSource project.sourceSets.main }
    client { client() }
    server { server(); programArgument '--nogui' }
}
dependencies { implementation "net.neoforged:forge:${project.neo_version}" }
```

构建命令（沿用仓库既有环境，**不要直接调 gradle**）：

```powershell
$env:JAVA_HOME='D:/project_farm/.tools/jdk21'
$env:GRADLE_USER_HOME='D:/project_farm/.tools/gradle-home'
$env:TEMP='D:/project_farm/.tools/tmp'; $env:TMP=$env:TEMP
$env:JAVA_TOOL_OPTIONS='-Duser.language=en -Duser.country=US -XX:ActiveProcessorCount=2'
& 'D:/project_farm/.tools/gradle-8.10/bin/gradle.bat' --init-script 'D:/project_farm/.tools/init.gradle' -p <工程目录> --no-watch-fs build
```

> 注意：`.tools/gw.cmd` 会启动代理并加 `--patch-module java.base=…`（本机 `Files.isWritable` 恒 false 的补丁）。探针证明 **JDK 21 + 补丁 + NG7 + JDK17 toolchain 可以共存**；但 `gw.cmd` 的 `-p D:/project_farm` 是硬编码，换目录要改或在外面包一层。

---

## 4. 命名空间搬运清单（65 个符号，按包分组）

> `×N` = 该 import 在仓库出现次数（合计 189）。标 **⚠** 的是**不是纯改前缀**的。

### 4.1 事件总线 / 加载器（6）

| 现（net.neoforged.*） | 1.20.1（net.minecraftforge.*） | 备注 |
|---|---|---|
| `bus.api.IEventBus` ×4 | `eventbus.api.IEventBus` | 纯前缀 |
| `bus.api.SubscribeEvent` ×24 | `eventbus.api.SubscribeEvent` | 纯前缀 |
| `bus.api.EventPriority` ×2 | `eventbus.api.EventPriority` | 纯前缀 |
| `fml.common.Mod` ×1 | `fml.common.Mod` | 纯前缀 |
| `fml.common.EventBusSubscriber` ×24 | `fml.common.Mod.EventBusSubscriber` | ⚠ 变成内部类 |
| `fml.ModContainer` ×1 | `fml.ModContainer` | 包位置不同 |

### 4.2 配置 / 侧标记（5）

| 现 | 1.20.1 |
|---|---|
| `neoforge.common.ModConfigSpec` ×4 | ⚠ `common.ForgeConfigSpec`（类名也变） |
| `fml.config.ModConfig` ×1 | `fml.config.ModConfig` |
| `fml.event.config.ModConfigEvent` ×2 | `fml.event.config.ModConfigEvent` |
| `api.distmarker.Dist` ×13 | `api.distmarker.Dist` |

### 4.3 注册面（5）

| 现 | 1.20.1 | 备注 |
|---|---|---|
| `neoforge.registries.DeferredRegister` ×13 | `registries.DeferredRegister` | 纯前缀（**已实测**） |
| `neoforge.registries.DeferredHolder` ×12 | ⚠ `registries.RegistryObject` | 类名变（**已实测**） |
| `neoforge.registries.DeferredBlock` ×1 | ⚠ 无对应 | 用 `DeferredRegister<Block>` + `RegistryObject<Block>` |
| `neoforge.registries.DeferredItem` ×2 | ⚠ 无对应 | 同上 |
| `neoforge.registries.NeoForgeRegistries` ×1 | ⚠ `registries.ForgeRegistries` | 尤其附件注册 |

### 4.4 客户端事件与数据生成（22 + 1）

| 现 | 1.20.1 | 备注 |
|---|---|---|
| `neoforge.client.event.ClientTickEvent` ×5 | ⚠ `event.TickEvent.ClientTickEvent` + `phase == END` | 1.21 的 `.Post` 不存在 |
| `neoforge.client.event.RenderGuiEvent` ×4 | `client.event.RenderGuiEvent` | **待验证**（类在，但方法与 `GuiGraphics` 取法要现场看） |
| `neoforge.client.event.RenderLevelStageEvent` ×3 | `client.event.RenderLevelStageEvent` | 纯前缀 |
| `neoforge.client.event.InputEvent` ×3 | `client.event.InputEvent` | 纯前缀 |
| `neoforge.client.event.ViewportEvent` ×2 | `client.event.ViewportEvent` | 纯前缀 |
| `neoforge.client.event.MovementInputUpdateEvent` ×2 | `client.event.MovementInputUpdateEvent` | 纯前缀 |
| `neoforge.client.event.ScreenEvent` ×1 | `client.event.ScreenEvent` | 纯前缀 |
| `neoforge.client.event.EntityRenderersEvent` ×1 | `client.event.EntityRenderersEvent` | 纯前缀 |
| `neoforge.client.event.RegisterKeyMappingsEvent` ×1 | `client.event.RegisterKeyMappingsEvent` | 纯前缀 |
| `neoforge.client.event.RegisterMenuScreensEvent` ×1 | `client.event.RegisterMenuScreensEvent` | 纯前缀 |
| `neoforge.client.event.RegisterRecipeBookCategoriesEvent` ×1 | `client.event.RegisterRecipeBookCategoriesEvent` | 纯前缀 |
| `neoforge.client.event.RegisterClientCommandsEvent` ×1 | `client.event.RegisterClientCommandsEvent` | 纯前缀 |
| `neoforge.client.event.ClientPlayerNetworkEvent` ×1 | `client.event.ClientPlayerNetworkEvent` | 纯前缀 |
| `neoforge.client.event.RenderLivingEvent` ×1 | `client.event.RenderLivingEvent` | 纯前缀 |
| `neoforge.client.event.RenderBlockScreenEffectEvent` ×1 | `client.event.RenderBlockScreenEffectEvent` | 纯前缀 |
| `neoforge.client.event.CalculatePlayerTurnEvent` ×1 | `client.event.CalculatePlayerTurnEvent` | **待验证**（1.20.1 是否有此事件） |
| `neoforge.client.model.generators.{BlockModelBuilder, BlockStateProvider, ConfiguredModel, ItemModelProvider, ModelFile, VariantBlockStateBuilder}` ×6 | `client.model.generators.*` | 纯前缀，但 datagen 细节有出入 |
| `neoforge.data.event.GatherDataEvent` ×1 | `data.event.GatherDataEvent` | 纯前缀 |

### 4.5 通用 / 事件（8 + 17）

| 现 | 1.20.1 | 备注 |
|---|---|---|
| `neoforge.common.NeoForge` ×1 | ⚠ `common.MinecraftForge` | 事件总线入口改名 |
| `neoforge.common.conditions.ICondition` ×1 | ⚠ `common.crafting.conditions.ICondition` | **待验证** |
| `neoforge.common.data.{BlockTagsProvider, DatapackBuiltinEntriesProvider, ExistingFileHelper, LanguageProvider}` ×5 | `common.data.*` | ⚠ `DatapackBuiltinEntriesProvider` 是 1.20.2+ 概念，**必须现场确认替代** |
| `neoforge.event.EventHooks` ×1 | ⚠ `event.ForgeEventFactory` | 类名变 |
| `neoforge.event.entity.{EntityJoinLevel, LivingDamage, LivingDeath, LivingFall, LivingIncomingDamage, MobEffectEvent, AttackEntityEvent, PlayerEvent}` ×8 | `event.entity.*` | 纯前缀 |
| `neoforge.event.level.ExplosionEvent` ×1 | `event.level.ExplosionEvent` | 纯前缀 |
| `neoforge.event.server.{ServerStarted, ServerStopped, ServerStopping}` ×3 | `event.server.*` | 纯前缀 |
| `neoforge.event.tick.{ServerTickEvent, LevelTickEvent, PlayerTickEvent}` ×3 | ⚠ `event.TickEvent.{ServerTickEvent, LevelTickEvent, PlayerTickEvent}` | 都变成 `TickEvent` 内部类 |
| `neoforge.event.village.VillagerTradesEvent` ×1 | `event.village.VillagerTradesEvent` | 纯前缀 |

### 4.6 网络（3，全部要改结构）

| 现 | 1.20.1 | 备注 |
|---|---|---|
| `neoforge.network.PacketDistributor` ×2 | `network.PacketDistributor` | 纯前缀 |
| `network.event.RegisterPayloadHandlersEvent` ×1 | ⚠ 不存在 | 改在 `FMLCommonSetupEvent` 里 `NetworkRegistry.newSimpleChannel(...)` |
| `network.handling.IPayloadContext` ×2 | ⚠ 不存在 | 用 `NetworkEvent.Context`（`enqueueWork` + `setPacketHandled(true)`） |

### 4.7 附件（1，最麻烦）

| 现 | 1.20.1 | 备注 |
|---|---|---|
| `neoforge.attachment.AttachmentType` ×1 | ⚠ **1.20.1 无数据附件 API** | 退到 **Capability**（`net.minecraftforge.common.capabilities`）或实体 NBT / 计分板；`FarmstormAttachments`（28 行）整类重写 |

---

## 5. 必须重写的 5 个子系统（不是改名）

| # | 子系统 | 现实现 | 1.20.1 要做的事 | 规模 |
|---|---|---|---|---|
| 1 | **数据组件 / 附件** | `FarmstormDataComponents`（17）+ `TempEnchantRecord`（30）+ `TempEnchantHandler`（136）+ `FarmstormAttachments`（28） | 组件与附件 API **都没有**：临时附魔改 NBT 或 Capability；`ai_suspend` 改 Capability 或实体 NBT。**注册面 −1（92 → 91）** | 重写 ~200 行 + 存档兼容分支 |
| 2 | **网络** | `CoreListPayload`（38）+ `CultivatorLinkPayload`（70）+ `FarmstormNetwork`（38） | 两个 payload → `SimpleChannel` 手写 encode/decode；版本协商改 `newSimpleChannel(...)`；`StreamCodec` 全消失 | 重写 ~150 行 |
| 3 | **配方** | `ConverterRecipe`（55）+ `ConverterRecipeSerializer`（27） | 1.20.1 的 `Recipe` 是 `matches(Container, Level)` / `assemble(Container, RegistryAccess)` / `getSerializer()`；序列化走 `fromJson/toNetwork/fromNetwork`；**21 条配方 JSON 形状也会变** | 重写 ~110 行 + 重新 datagen |
| 4 | **进度** | `ModAdvancementProvider`（138，`AdvancementHolder`） | 1.20.1 用 `Advancement` / `Advancement.Builder`，`requirements` 与 `parent` 挂法不同；16 条逐条核 | 改写 ~140 行 |
| 5 | **效果与投射物** | 4 个 `MobEffect`、`TurretBullet`（151）、`ThrownCropEntity`（125） | 效果 tick 谓词是 **`isDurationEffectTick`**；投射物是 **`onHitEntity` / `onHitBlock`**（不是 `hitTargetOrDeflectSelf`） | 改写 ~40 行 + 逐类核对 |

**另有全局签名差异（机械但要逐个改）**：

- `HolderLookup.Provider` / `RegistryOps`：1.20.1 **没有**（**46 处 / 15 文件**）
- `RecipeInput` / `SingleRecipeInput` / `RecipeHolder`：改 `Container` + `getRecipeFor(RecipeType, Container, Level)`（**13 处 / 3 文件**）
- `RegistryFriendlyByteBuf`：改 `FriendlyByteBuf`（**9 处 / 4 文件**）
- `DataComponents`：见子系统 1（**17 处 / 5 文件**）
- `StreamCodec` / `ByteBufCodecs`：见子系统 2（**26 处 / 4 文件**）

---

## 6. 数字面变化（文档与检查器都要跟着改）

| 项 | 1.21.1 现值 | 1.20.1 目标 | 原因 |
|---|---|---|---|
| 注册对象 | 92（12 类） | **91（11 类）** | `data_component_type` 在 1.20.1 不存在 |
| Java 文件 | 151 | 以实际为准，**不许为凑数拆合类** | — |
| server config | 175 / 60 | 175 / 60 | 本移植不加键 |
| 语言键 | 321 × 2 | 321 × 2 | — |
| 网络包 | 2（协议串 `"3"`） | 2（协议串重起，建议 `"1"`） | 实现换了，握手语义不同 |
| 进度 / 配方 / 投入物 / listing | 16 / 21 / 17 / 26 | 不变 | — |
| `pack_format` | 34 | **15** | 1.20.1 资源包格式 |
| 元数据 | `META-INF/neoforge.mods.toml` | **`META-INF/mods.toml`** | 1.20.1 用 Forge 格式 |
| 检查器 | 期望 92 | 改 91，并去掉 `data_component_type` 一组 | 工具**从文档现算**，要同时补 1.20.1 的文档来源 |

---

## 7. 建议的阶段与任务

| 阶段 | 内容 | 产出判据 | 人日 |
|---|---|---|---|
| **P0 打通构建** | NG7 骨架、toolchain 逗号写法、代理常驻、`mods.toml`、`pack_format 15`、`runServer` 能起（解决 P-11） | `runServer` 打出 `Done (` 且自检行出现 | 1–2 |
| **P1 命名空间搬运** | 189 行 / 65 符号按 §4 搬；先让它**能编译**，行为不动 | `build` 退出码 0；`net.neoforged.` 命中 0 | 4–6 |
| **P2 注册面与存档** | `RegistryObject`、`ItemStack.save/of`、`saveAdditional/load`、Provider 清零 | 自检 `91 … OK`；新世界可读写 | 3–5 |
| **P3 五个子系统** | 组件/附件、网络、配方、进度、效果与投射物 | 连线包能收发；21 条配方 0 条解析错；16 条进度能授予 | 8–12 |
| **P4 行为回归** | 四道闸、培养器产出、交易经验、浮窗底色、放置音、绑定统一、`ai_suspend` | 复用 v0.2.4 的 32 条技术 + 31 条边界逐条复跑 | 6–10 |
| **P5 文档与签字** | 六份新文档 + 检查器 1.20.1 来源 + 三份记录 | 与 v0.2.4 同形的三份记录齐全 | 8–12 |
| **P6 打包分发** | `dist/` jar、1.20.1 安装说明、双端同版提示 | jar 能被 PCL/HMCL 1.20.1 实例加载 | 1–2 |
| **合计** | | | **31–49 裸 / 39–61 含缓冲** |

> 与 §0 的 38–60 差异：P4 压到 6–10 是因为验收清单已存在可直接复用；若在 Capability 或 datagen 撞深水区，按上限取。

---

## 8. 每阶段的可执行判据（别写「正常」「可用」）

- **P0**：`runServer` 日志出现 `Done (` 与 `Preparing level`；运行时 `java -version` = 17，Gradle daemon = 21。
- **P1**：`grep -c "net.neoforged." src/main/java` = **0**；构建退出码 **0**。
- **P2**：自检整行 = `自检 注册对象 91 个：… → OK`；`grep -c "HolderLookup\|RegistryFriendlyByteBuf"` = 0。
- **P3**：`Couldn't parse recipe` = 0；`/data get entity <村民> Offers` 按 xp 分组仍是 7/6/7/4/2 = 26；`ClientCultivatorLinks` 有内容。
- **P4**：v0.2.4 的 32 条技术验收 + 31 条边界按 1.20.1 重跑，不通过逐条登记。
- **P5**：`python -X utf8 tools/verify_project.py` 打印 `91 / 91`、`175`、`321`、`FAIL 0`。

---

## 9. 坑与红线

**坑（探针已踩）**

1. 选 `moddev` 插件一定死在依赖解析（P-01）；别花时间改 `neoForge { version }`。
2. `neo_version` 必须带 MC 前缀 `1.20.1-47.1.106`；只写 `47.1.106` 会去找不存在的 `neoforge` artifactId。
3. `org.gradle.java.installations.paths` 用**逗号**（P-06）。
4. 代理不常驻 → 立刻 `Connection refused`；手动跑 gradle 前先确认 8899 在听。
5. 仓库外目录别试（P-07），Gradle 子进程会被沙箱拒写。
6. `runServer` 首次要 `run/eula.txt`（`eula=true`），且会先跑 `DownloadAssets`（P-11 卡点）。
7. 1.20.1 的 `BlockBehaviour.Properties` 不强制 `codec()`；但**不要**先把 1.21.1 的 codec 代码删了再想加回来，一次改到位。

**红线（沿用仓库规矩）**

- Mixin **0**。
- 不改上游文档（`docs/zh/00~63`、`docs/en/*`、`plan*.txt`、`.work/v0xx-BRIEF.md`）。
- 网络包数不设上限，但**不加第三个包**：两个包的语义要在 1.20.1 里原样成立。
- 服务端侧不出现 `net.minecraft.client.*`；CLIENT 配置必须独立成 `farmstorm-client.toml`。
- 客户端预测与服务端四道闸的分工不许混。

---

## 10. 作者已拍板的 4 件事（v0.2.4 移植期确认）

1. **协议串**：1.20.1 版从 **`"1"`** 起（与 1.21.1 的 `"3"` 明确区分；混装直接拒绝，不崩）。
2. **数据组件那条线不砍**（走 A 路线完整移植）：临时附魔（`temporary_enchantments`）与 `ai_suspend` 都要在 1.20.1 里保住语义 —— 用 NBT 或 Capability 重写，**不允许**用「1.20.1 版不支持」来回避。这条把 P3 的人日按上限取（12 人日）。
3. **存档不迁移**：明确「只支持新世界」，不写 1.21.1→1.20.1 的存档转换工具；老世界打不开属预期，写进已知限制。
4. **文档仍按六件套**，但**编号从 0 开始**：`00-DECISIONS` / `01-DESIGN` / `02-TECH` / `03-TASKS` / `04-PROMPT-MAIN` / `05-PROMPT-TASKS`，本交接文档不是六件套之一（它是输入），单独放根目录。

---

## 11. 探针工程与可复用资产

| 路径 | 说明 |
|---|---|
| `D:/project_farm/.probe-1201` | 可编译的 1.20.1 工程骨架（NG7 + 1 作物方块 + 1 方块实体 + 1 `SimpleChannel` 包） |
| `.probe-1201/build.gradle`、`gradle.properties`、`settings.gradle` | §3 的实测可用版本，直接抄 |
| `.probe-1201/src/main/resources/META-INF/mods.toml` | 1.20.1 元数据样板 |
| `.probe-1201/build-ng7d.log` | `BUILD SUCCESSFUL` 完整日志（含 NeoForm 各阶段） |
| `.probe-1201/server.log` | `DownloadAssets` 卡住那次的日志（P-11 证据） |
| `.tools/dl-jdk17.mjs`、`probe-mirrors.mjs`、`probe-coords.mjs` | 探针辅助脚本；结论是都不再需要，留作证据 |

---

## 12. 新工作区（v0.2.4 移植期决定）

- **工作区**：`D:/project_farm1.20.1`（与 1.21.1 主线 `D:/project_farm` 并存，互不干扰）。
- **编号**：六件套从 **00** 起（见 §10 第 4 条）；`D` / `F` / `Phase` / `HC` / `N` / `RT` / `A` 各系列也在新工作区里**从 001 重新起**，不与 1.21.1 的编号续接。
- **工具链**：不复制。仍**复用** `D:/project_farm/.tools`（Gradle 8.10 / JDK 21 / 镜像代理 / java.base 补丁）；新工作区只放源码、文档与探针骨架。
- **探针**：已从 `.probe-1201` 拷为 `probe/`，可直接 `build` 验证环境；它**不是**最终工程，最终工程应替换成真正的 mod 目录结构。
- **现状**：工作区当前只有 `README.md` / `HANDOVER-1.20.1.md` / `docs/zh/`（空）/ `probe/`；mod 源码尚未迁入（这是下一步）。

