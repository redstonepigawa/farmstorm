# 02 号 1.20.1 移植技术规格

> 上游：`00-DECISIONS-1.20.1.md`（D-001~D-014）、`01-DESIGN-1.20.1.md`（R1~R7 / B-01~B-12）、`HANDOVER-1.20.1.md`（P-01~P-12 + 65 符号全表）。
> 本文负责：**落点、数字、验收、风险**。不写排期（03 号），不写提示词（04/05 号）。

---

## 0. 阅读约定

**0.1 判据只认五类读数**：计数（文件 / 键 / 命中）、日志整行、`/data get` 读数、源码 grep 字符串、像素或字节比对。禁用「正常」「可用」「符合预期」。

**0.2 数字纪律**：本文每个数字必须能追到 §1 基线表、HANDOVER 的探针事实，或两者的加减法。新数值一律写「由实测」。

**0.3 行号纪律**：本文不写行号（1.20.1 侧行号必然漂移），一律写 `文件#方法/字段`。

---

## 1. 数字面（基线 + 目标）

| 项 | 1.21.1 v0.2.4 | 1.20.1 目标 | 依据 |
|---|---|---|---|
| 注册对象 | 92（12 类） | **91（11 类）** | D-009 / D-013 |
| └ items / blocks | 28 / 28 | 28 / 28 | 不变 |
| └ block_entity_type / entity_type | 8 / 13 | 8 / 13 | 不变 |
| └ mob_effect / sound_event | 4 / 3 | 4 / 3 | 不变 |
| └ recipe_type / recipe_serializer | 1 / 1 | 1 / 1 | 不变 |
| └ creative_tab / damage_type | 1 / 3 | 1 / 3 | 不变 |
| └ data_component_type | 1 | **0（整类消失）** | 1.20.1 无此注册表 |
| └ menu_type | 1 | 1 | 不变 |
| server config | 175 键 / 60 push / 36 顶层节 | 同 | 不变 |
| client config | 3 键 | 3 键 | 不变 |
| 语言键 | 321 × 2 | 321 × 2 | 不变 |
| 网络包 | 2（协议串 "3"） | **2（协议串 "1"）** | D-008 |
| 进度 / 转换配方 | 16 / 21 | 16 / 21 | 不变 |
| 培养器投入物 / 交易 listing | 17 / 26 | 17 / 26 | 不变 |
| Java 文件 / 代码行 | 151 | **151 / 16,080** | D-009 + 实测（F-017 回灌） |
| `pack_format` | 34 | **15** | D-014 |
| 元数据 | `META-INF/neoforge.mods.toml` | **`META-INF/mods.toml`** | D-014 |
| Mixin | 0 | 0 | 红线 |

> 实测读数（回灌）：Java 代码行 **16,080**（151 个 `*.java` 逐文件行数求和；旧读数 15,752 作废）；`src/main/resources` **131** 文件、`src/generated/resources` **232** 文件。

---

## 2. 目标工程配置（以探针实测为准）

```groovy
plugins {
    id "java-library"
    id "net.neoforged.gradle.userdev" version "7.0.171"
}
java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
runs {
    configureEach { systemProperty "forge.logging.console.level", "debug"; modSource project.sourceSets.main }
    client { client() }
    server { server(); programArgument "--nogui" }
}
dependencies { implementation "net.neoforged:forge:1.20.1-47.1.106" }
```

`gradle.properties` 与 `build.gradle` 里的构建项（实测文件；mod 元数据组略）：

```properties
minecraft_version=1.20.1
neo_version=1.20.1-47.1.106
loader_version_range=[47,)
mod_version=0.2.4
org.gradle.jvmargs=-Xmx3G -XX:ActiveProcessorCount=2 --patch-module java.base=D:/project_farm/.tools/jdk-patch/classes
org.gradle.parallel=false
org.gradle.caching=true
org.gradle.java.installations.auto-detect=false
org.gradle.java.installations.paths=D:/project_farm/.tools/jdk21,C:/Users/DASHpig/AppData/Roaming/.minecraft/runtime/java-runtime-beta
```

四条硬要求（每条都踩过）：

- **`--patch-module` 必须写进 `org.gradle.jvmargs`**，不能用 `JDK_JAVA_OPTIONS` 环境变量：后者会被 Gradle 的 toolchain 探测子进程继承，JDK 17 被判成 Invalid，整条链断掉。
- **`org.gradle.parallel=false`**：NeoForm 用虚拟线程并发写同一个 ZipFS，核多时易写冲突。
- **`org.gradle.java.installations.paths` 必须逗号分隔**，且同时含 JDK 21（守护进程，跑 Gradle/NeoForm）与 JDK 17（编译 toolchain，`%APPDATA%/.minecraft/runtime/java-runtime-beta`）；分号写法会被当成一条不存在的路径。
- **`build.gradle` 里控 javac 报错条数**：`tasks.withType(JavaCompile) { options.compilerArgs << '-Xmaxerrs' << '1000' }`；javac 默认 100 条封顶，搬迁期看不到全貌。

**构建命令**（镜像代理必须常驻 `127.0.0.1:8899`；变量赋值不带引号）：

```powershell
$env:JAVA_HOME=D:/project_farm/.tools/jdk21
$env:GRADLE_USER_HOME=D:/project_farm/.tools/gradle-home
$env:TEMP=D:/project_farm/.tools/tmp
$env:TMP=D:/project_farm/.tools/tmp
$env:JAVA_TOOL_OPTIONS=-Duser.language=en -Duser.country=US -XX:ActiveProcessorCount=2
& D:/project_farm/.tools/gradle-8.10/bin/gradle.bat --init-script D:/project_farm/.tools/init.gradle -p D:/project_farm1.20.1 --no-watch-fs build
```

**产物必须做 SRG 重映射（NG7 7.0.171 没有 reobf 任务）**：`build` 产出的 jar 是**官方名（mojmap）**，装进真实实例会 `NoSuchMethodError`（实测崩在 `StringRepresentable.fromEnum`）。发布件一律由 `remap-1.20.1.cmd` 产出：

```text
remap-1.20.1.cmd
└─ tools/remap-to-srg.ps1 + tools/SrgMappingBuilder.java + AutoRenamingTool 2.0.17
   映射 = 本机 Mojang proguard 映射（.gradle/caches/minecraft/versions/1.20.1/client.txt）
        + NeoForm 的 steps/mergeMappings/output.tsrg
   产出 = <输入名>-srg.jar
```

- 三份产物、三档用途：开发用 `build/libs/farmstorm-0.2.4+mc1.20.1.jar`（**1,129,491 B，不能直接装**）；可分发的是 `build/libs/farmstorm-0.2.4+mc1.20.1-srg.jar` 与 `dist/farmstorm-0.2.4+mc1.20.1.jar`（均 **1,138,762 B**）。
- 判据：`-srg.jar` 里 `CropCategory.class` 含 `m_216439_`（`StringRepresentable.fromEnum` 的 SRG 名），残留官方名 = 0。

**`mods.toml` 两条 1.20.1 规则**（`src/main/resources/META-INF/mods.toml`，实测）：① 依赖项写 `mandatory=true`——`type="required"` 是 1.20.2+ 的写法，1.20.1 不认；② NeoForge 47 的 modid 仍是 **`forge`**（改名 `neoforge` 发生在 1.20.2），所以 `[[dependencies.farmstorm]]` 里写 `modId = "forge"`。

**dev 环境 `runServer` 两个坑**（1.20.1 Forge）：① 服端配置**按存档**存放 `<gamedir>/world/serverconfig/forge-server.toml`——放进 `config/forge-server.toml` 里 Forge 根本不读；② `advertiseDedicatedServerToLan` 默认 `true`，会让 `DedicatedServer` 引用客户端类 `LanServerPinger`，dev 环境 `Done (` 之后必崩，需置 `false`。

**数据包目录名：1.20.1 与 1.21 不同，写错就是「静默全部不加载」**（实测踩过，症状是标签全空、配方一条不进、进度只差我们那 16 条）：

| 1.21 目录 | 1.20.1 目录 |
|---|---|
| `data/<ns>/recipe/` | `data/<ns>/recipes/` |
| `data/<ns>/advancement/` | `data/<ns>/advancements/` |
| `data/<ns>/loot_table/` | `data/<ns>/loot_tables/` |
| `data/<ns>/tags/block/` | `data/<ns>/tags/blocks/` |
| `data/<ns>/tags/item/` | `data/<ns>/tags/items/` |

从 1.21.1 直接拷 `src/generated/resources` 时**必须整批改名**；否则 `isProcessingBlock` 用的 `farmstorm:processing_block` 标签为空，据点核心永远绑不上培养器/转换器（表现为「完全不能绑定」，且服务端一点日志都没有）。

JSON 内部还有两处 schema 差异，同一批要改（工具：`tools/fix-generated-1201.mjs`）：
- 配方 `result`：1.21 的 `{"id":…,"count":…}` → 1.20.1 的 `{"item":…,"count":…}`（1.20.1 的 `ConverterRecipeSerializer` 走 `ShapedRecipe.itemStackFromJson` 读 `item`）。
- 进度 `display.icon`：1.21 的 `{"id":…}` → 1.20.1 的 `{"item":…}`；并删掉 1.20.1 不认识的 `sends_telemetry_event`。
- 进度的 `ItemPredicate`：1.20.1 的 `items` **只收物品 id，不收 `#tag`**，写成 `{"items":[{"items":["#tag"]}]}` 会报 `Non [a-z0-9_.-] character in namespace`。要么展开成显式 id 列表，要么用 `tag` 字段。

**1.20.1 世界渲染：相机变换只在 `RenderLevelStageEvent` 的 PoseStack 里（最坑的一条）**

`GameRenderer` 里 `applyModelViewMatrix()` 只有两个调用点，**都在 `renderLevel()` 之后、且都在 GUI 路径**（源码实测）。也就是说：

- 整个世界渲染期间，shader 的 `ModelViewMat` **从未被写成相机矩阵**，世界的相机变换完全靠 `event.getPoseStack()` 在 CPU 端烘进顶点；
- `RenderSystem.getModelViewMatrix()` / `getModelViewStack()` 在世界阶段拿到的是**上一次 GUI 的残留**。

因此**任何在世界空间画几何的代码（世界阶段或 GUI 阶段重放）都必须用 `event.getPoseStack()`**：

- 世界阶段（如 `AFTER_PARTICLES` 的连线）：`event.getPoseStack()` + `pushPose()` → `translate(-cameraPos)` → 画 → `popPose()`。push/pop 包住就不会污染后续阶段，不必新建 PoseStack。
- GUI 阶段重放（如狂野猎人剪影）：在 `AFTER_ENTITIES` 捕获 **`event.getPoseStack().last().pose()`** 作为 modelview，GUI 阶段 `set` 回去再 `applyModelViewMatrix()`；捕获 `RenderSystem` 的矩阵必错。

> 这条是 1.21.1 经验在 1.20.1 失灵的典型：1.21.1 里 `getModelViewMatrix()` 就是相机矩阵（所以 `NOTE-wild-hunter-outline-V50.md` 记录的做法在那边成立），1.20.1 不是。
> 症状特征：**偏移量随玩家移动而变化**（误差与相机位置相关），而不是固定的常数偏差。

**同一事件上的多个相机处理器必须显式定序（桑吉尔蔗踩过）**

1.20.1 没有 `CalculatePlayerTurnEvent`（1.21 才有），所以「输入锁 → 灵敏度 0」只能退化成挂 `ViewportEvent.ComputeCameraAngles` 把 `yaw/pitch` 钉回冻结值。而桑吉尔蔗的倒转 / 自转**也要改同一个事件的角度**，且它的三个相位本来就在锁定期内（D-60 要求全锁）：

| 处理器 | 优先级 | 行为 |
|---|---|---|
| `InputLockHandler.onTurn` | NORMAL（默认） | 锁定期内把 `yaw/pitch` 钉回冻结值 |
| `ZangiefCameraController.onComputeCameraAngles` | **必须 LOW** | 在冻结基准上叠加 `roll=180`、`pitch` 取反、`yaw` 自转 |

若两者同为默认优先级，冻结那一方若后执行就会把效果整段覆盖 —— **症状极具有迷惑性：日志里「自转累计 1368.0 度（19 刻 × 72.0）」完全正常，画面却一动不动**（只有 `roll` 侥幸存活）。

**规矩**：凡是同一 tick 内对同一个 `ComputeCameraAngles` 叠加改动的处理器，一律用 `EventPriority` 显式定序（叠加方用 `LOW`，基准方用默认），并在类注释里写死这个依赖。

---

## 3. 八个「非纯改前缀」符号的具体改法

| # | 现（1.21.1） | 1.20.1 改法 | 受影响面 |
|---|---|---|---|
| 1 | `DeferredHolder<T, R>` | 全换 `RegistryObject<R>`；`.get()` / `.getId()` 用法不变 | 12 处 / 约 6 文件 |
| 2 | `DeferredBlock<T>` / `DeferredItem<T>` | 无对应 → 声明成 `DeferredRegister<Block>` + `RegistryObject<Block>` | `FarmstormBlocks` / `FarmstormItems` |
| 3 | `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` | 无对应 → 实体持久 NBT（`Entity#getPersistentData()`）+ 工具类 `FarmstormAttachments`；键 `FarmstormAiSuspend`，**不做 Capability** | `registry/FarmstormAttachments`（14 个存取点） |
| 4 | `ModConfigSpec` | `ForgeConfigSpec`；`defineInRange` / `define` / `push` / `pop` 同名 | `FarmstormConfig` / `FarmstormClientConfig` |
| 5 | `NeoForge.EVENT_BUS` | `MinecraftForge.EVENT_BUS` | 注册入口 |
| 6 | `EventHooks.*` | `ForgeEventFactory.*`（逐个核方法名） | 爆炸 / 交互类 |
| 7 | `@EventBusSubscriber` | `@Mod.EventBusSubscriber`；`Dist` 仍在 `net.minecraftforge.api.distmarker` | 24 处 |
| 8 | `ClientTickEvent` / `ServerTickEvent` / `LevelTickEvent` / `PlayerTickEvent` | 改 `TickEvent.<X>TickEvent` + 判 `event.phase == TickEvent.Phase.END` | 14 处 |

> 其余 57 个符号是纯改前缀（`net.neoforged.*` → `net.minecraftforge.*`），全表见 HANDOVER §4。

---

## 4. 五个子系统的重写规格

### 4.1 数据组件 → 物品 NBT（D-009）

- 现状：`FarmstormDataComponents` 注册 `temporary_enchantments`；`TempEnchantHandler` 读写组件。
- 目标：物品 NBT 键 **`FarmstormTempEnchants`**（ListTag），元素是 **4 个字段**：`{enchantment:"<附魔注册 id>", granted:int, previous:int, expireAt:long}`（即 `TempEnchantRecord` 的 codec 形状）；读写走 `stack.getOrCreateTag()` / `stack.getTag()`。
- 字段口径：`granted` = 本次授予的等级；`previous` = **授予前的原等级**（0 = 原本没有该附魔）；`expireAt` = 到期的**绝对 `gameTime`**（不是剩余刻数，避免存档停顿后漂移）。
- **为什么必须留 `previous`**：只记「临时等级」的三字段写法（`id/lvl/until`）撤销时不知道该退回哪一级——原本已带同种附魔的物品会被抹成 0 或停在临时等级；留 `previous` 才能逐项还原，且与 1.21.1 组件版的字段一对一。
- 契约：① 堆叠与合并语义与组件版一致；② 到期移除在服务端 tick 结算；③ 掉落 / 死亡不丢（NBT 随物品走）。
- 判据：`grep -c DataComponent` = 0；`/data get entity <玩家> Inventory` 能看到该键；到期后键消失、等级回到 `previous`。

### 4.2 实体附件 → 实体持久 NBT（D-009）

- 现状：`ATTACHMENT_TYPES` 注册 `ai_suspend`（第 13 类附件，不计入 92）。
- 目标：**实体持久 NBT**（`Mob#getPersistentData()`，随实体 NBT 落盘）+ 工具类 `registry/FarmstormAttachments`（`getAiSuspend` / `setAiSuspend` / `removeAiSuspend`）；键 **`FarmstormAiSuspend`**，全仓 **14 个存取点**。**当前实现不走 Capability。**
- 契约：① 停手与恢复必须写回**原值**（原本 `noAi=true` 的怪恢复后仍是 true）；② `sources == 0` 时整个键移除（存档里不出现该键）；③ 死亡不复制（与原 `copyOnDeath = false` 一致）；④ 只在服务端读写，客户端不读。
- 判据：`grep -c AttachmentType` = 0；起服后 `/data get entity <怪>` 含该键；震动豆 / Q弹 行为与 v0.2.4 的 B-30 / B-31 一致。
- 备注：**Capability 化是可选的后续项，不是当前实现**——只有出现「多宿主并发写 + 版本协商」需求时才升级成 `RegisterCapabilitiesEvent` + `ICapabilitySerializable`（`FarmstormAttachments` 里留了 `TODO(F-010)`）。

### 4.3 网络（D-008）

- 形态：`SimpleChannel`——信道由 `NetworkRegistry.ChannelBuilder.named(Farmstorm.id("main")).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel()` 建；包逐个用 `CHANNEL.messageBuilder(CoreListPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)` / `(CultivatorLinkPayload.class, 1, NetworkDirection.PLAY_TO_CLIENT)`（**类 + 数字 id + 方向**）登记；两者都在 **`FMLCommonSetupEvent`** 的 `event.enqueueWork(...)` 里完成注册。协议串 **"1"**。
- 编解码：`FriendlyByteBuf` 手写；`CoreListMsg` = `List<Core>`（字段序与 v0.2.4 一致）；`CultivatorLinkMsg` = `List<Link>`，`Link` 三字段 `(BlockPos core, BlockPos cultivator, byte kind)`。
- 发送：`CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg)`。
- 契约：① 包数仍 2；② 客户端不回包；③ `Kind` 未知值回落 `CULTIVATOR`（0）；④ `MAX_LINKS` 上限 1024 保留。
- 判据：双端日志无 `Unknown packet`；`ClientCultivatorLinks` 有内容；混装时握手被拒且不崩。

### 4.4 配方（D-010）

- 形态：`ConverterRecipe implements Recipe<Container>`，`getSerializer()` 返回 `ConverterRecipeSerializer`；JSON 字段保持 **`input` / `result`**。
- 序列化器：`fromJson(ResourceLocation, JsonObject, ...)` / `toNetwork` / `fromNetwork`；内部 `Ingredient.fromJson` + `ItemStack` 读写。
- 契约：21 条单向、无成本、result 恒 1（v0.2.4 的 D-209 口径）。
- 判据：目录 21 份 `converting`；日志里配方解析报错 = 0；21 条输入都能被转换器认（手持闸是全 21 条）。

### 4.5 进度（D-010）与效果 / 投射物（D-011）

- 进度：`Advancement.Builder`（1.20.1 版）16 条；requirements 用 `AdvancementRequirements`；判据 `data/farmstorm/advancement/*.json` = 16，`converter` / `first_conversion` 的 id 不变。
- 效果：4 个 `MobEffect` 的 tick 谓词用 `isDurationEffectTick(int, int)`；`applyEffectTick` 同名。
- 投射物：`TurretBullet` / `ThrownCropEntity` 用 `onHitEntity` / `onHitBlock`；`TurretBullet` 的 300 刻寿命判定仍放在 `tick()` 开头。

---

## 5. 存档与协议

| 项 | 结论 |
|---|---|
| 世界存档 | **不迁移**（D-014）；1.21.1 世界在 1.20.1 打不开属预期 |
| 方块实体 NBT 键 | 保持 v0.2.4 名字：`Held` / `Progress` / `Core` / `Cultivators` / `CultivatorKinds` |
| 实体 NBT 键 | 新增 `FarmstormAiSuspend`（§4.2）；`TurretBullet` 仍只存 `Target` |
| 物品 NBT 键 | 新增 `FarmstormTempEnchants`（§4.1） |
| 协议串 | "1"（D-008） |
| 配置文件名 | `farmstorm-server.toml` / `farmstorm-client.toml` 不变 |

---

## 6. 资源面

| 项 | 1.21.1 | 1.20.1 | 判据 |
|---|---|---|---|
| `pack.mcmeta` | 34 | **15** | 读文件 |
| blockstates / models | 8 variant 转换器 + 4 阶段平凡作物 | 同（JSON 兼容） | 文件计数不变 |
| 贴图 | 方块 84 png / 5 mcmeta、item 22、gui 0 | 同 | 计数不变 |
| 语言文件 | 321 × 2 | 同 | 检查器 |
| datagen 输出 | 232 文件 | 配方 / 进度 JSON 形状变，数量不变 | 文件计数 |

---

## 7. 验收（25 条）

| # | 验收 | 期望 |
|---|---|---|
| 1 | 构建 | 退出码 0；产物 `farmstorm-0.2.4+mc1.20.1.jar` |
| 2 | 检查器 | `91 / 91`、`175`、`321`、`FAIL 0` |
| 3 | 启动自检 | `自检 注册对象 91 个：... → OK` |
| 4 | 命名空间 | `grep -c net.neoforged.` = 0 |
| 5 | 注册类型 | `data_component_type` 命中 0；`EXPECTED_COUNTS` 11 项相加 = 91 |
| 6 | 协议串 | 代码内 "1" 命中 1；"3" 命中 0 |
| 7 | 包数 | 发送点 2 处；信道注册 1 处 |
| 8 | 配方 | 目录 21 份 `converting`；0 条解析错 |
| 9 | 进度 | 目录 16 份；`converter` / `first_conversion` 在 |
| 10 | 转换器绑定 | 拆核后转换器 `Core` 键消失、`Held` / `Progress` 保留 |
| 11 | 补绑 | 放新核后一个周期内出现 `补绑：核 ... 本轮新增 1 条` |
| 12 | 转换器产出 | 600 刻后核里多 1 个对应改良作物 |
| 13 | 核满 | 停 600 不丢；腾 1 格后 1 刻内投递 |
| 14 | 培养器准入 | 17 收 / 4 拒（火把花 / 瓶子草荚 / 甜浆果 / 金苹果） |
| 15 | 培养器产出 | 小麦 2+2、胡萝卜 4、毒马铃薯 4、平凡韭菜 2+2 |
| 16 | 平凡韭菜 | 种子能种、能到 age=3、掉落 1 作物 + 0~3 种子 |
| 17 | 交易经验 | Offers 按 xp 分组 7/6/7/4/2 = 26 |
| 18 | 临时附魔 | 物品 NBT `FarmstormTempEnchants` 在，到期消失 |
| 19 | AI 停手 | 震动豆 / Q弹 停手与恢复；原本 noAi 的怪不被放出 |
| 20 | 浮窗底色 | 常量 `0x4D000000`，alpha 77 |
| 21 | 放置音 | 三处客户端分支「手持输入物才 SUCCESS」 |
| 22 | 资源四数 | 方块 png 84 / mcmeta 5 / item png 22 / gui 0 |
| 23 | 炮台寿命 | 第 300 刻消失（可配） |
| 24 | 进度文案 | `fang_dance` 中英均为「刺球」口径 |
| 25 | 混装 | 1.21.1 客户端连 1.20.1 服务端：连线 / 浮窗不可见、不崩 |

---

## 8. 风险（RT-001 ~ RT-010）

| # | 风险 | 影响 | 缓解 |
|---|---|---|---|
| RT-001 | `runServer` 卡在 `DownloadAssets`（P-11） | P0 无法验收 | 预置 assets 或跳过资源下载；先 `--nogui` 起服验证 mod 加载 |
| RT-002 | 沙箱导致本工作区无法构建（P-07） | 全部阶段 | 构建会话以本目录为工作区，或从 DSH 外终端跑 |
| RT-003 | `DatapackBuiltinEntriesProvider` 在 1.20.1 是否存在 | P2 | 现场核实，必要时改 `RegistrySetBuilder` |
| RT-004 | `RenderGuiEvent` 的 `GuiGraphics` 取法差异 | 浮窗 | 现场核实；退化到 PoseStack + GuiGraphics 双参 |
| RT-005 | `ICondition` 包路径 | 配方条件 | 现场核实（1.20.1 在 `common.crafting.conditions`） |
| RT-006 | 附件改走 Capability 时的注册时序 | AI 附件 | **当前不走 Capability**：`ai_suspend` 已按实体持久 NBT 落地（§4.2）；将来升级时在 `RegisterCapabilitiesEvent` 注册 |
| RT-007 | `Advancement.Builder` 的 requirements 语义 | 16 条进度 | 逐条对 v0.2.4 的 json 核 |
| RT-008 | 配方 JSON 的 Ingredient 写法 | 21 条配方 | datagen 重生成后与 v0.2.4 逐字段对比 |
| RT-009 | 协议串 "1" 与主线 "3" 混淆 | 混装 | 两端日志首行打版本串；安装说明写死 |
| RT-010 | 版本号同名（都 0.2.4）导致混放 | 支持成本 | 安装说明明确「按 MC 版本各放一份」 |

---

## 9. 与 1.21.1 的差异登记

见 `01-DESIGN-1.20.1.md` §5（6 条）。本文补三条技术侧差异：① `data_component_type` 整类消失（91 vs 92）；② 物品 / 实体状态载体从组件 / 附件变**物品 NBT / 实体持久 NBT**（不走 Capability）；③ 网络从 payload 体系退回 SimpleChannel，协议串重起。

> 本文自检：数字面 11 项各给依据；8 个非纯前缀符号有改法；5 个子系统各有契约与判据；验收 25 条；风险 10 条。
