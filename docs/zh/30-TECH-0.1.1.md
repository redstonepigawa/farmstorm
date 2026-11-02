# 30 号 v0.1.1 技术规格（Technical Spec）

> 项目：农场风云 / Farmstorm · 版本 **0.1.1**（MC 1.21.1 / NeoForge 21.1.213 / Java 21）。
> 唯一真相源：`docs/zh/28-DECISIONS-0.1.1.md`（D-124 ~ D-147，共 24 条）。本文**只引用**决策号，不新开决策、不改口径；与 28 号冲突时以 28 号为准。
> 事实底座：`.work/v011-BRIEF.md`（§1 源码事实 / §2 25 条裁定 / §3 增量清单 / §4 风险 RT-31 ~ RT-41 / 附录 A 的 93 个 ID）。
> 体例参照：`docs/zh/24-TECH-0.1.0.md`（**只学目录结构与表格口径，不引用它的任何数值**）。
> 红线新口径：**只剩「无 Mixin」一条**（D-146）。「0 自定义网络包」**整体废除**，本版新增 1 个（D-142）且**不设上限**；但每新增一个包仍须在决策记录里登记。
> 写作纪律：本文的每个计数都由本文自己从源码逐条点出并写明文件:行号（§1.2 / §4.1 / §4.3）；凡本文自定的技术参数（payload 名、NBT 键名、ticket 等级、行距等）一律标「**本文确定**」并给理由（汇总见附录 A）；**不写任务排期与工时**（属 31 号）；任务编号 F-xx 由 31 号发，本文只按 §7 的验收编号（#1 ~ #38）追责。

---

## 0. 阅读约定

### 0.1 本文负责什么

| 项 | 内容 |
|---|---|
| 负责 | 注册面 93 的**逐类独立点名**与计数方法（§1.2）；本版注册增量 +0 的理由（§1.3）；语言键与 server config 键的现状基线（§1.4）；七个技术构件（网络包、绑定表、指令删除、客户端构件、区块 ticket、炮弹收敛、韭场载体）的类设计（§2）；对 0.1.0 的增/改/删总表（§3，47 行）；config 全表与语言键增删（§4）；资源清单（§5）；RT-31 ~ RT-41 的验证方式（§6）；**38 条**验收自检（§7）；待验证 API 8 条（§8）；与上游的差异登记（§9，8 条；已裁决的 A-30 ~ A-49 不重复登记，唯一例外是 A-49 的落点就在 §9 第 5 条） |
| 不负责 | 玩法叙述、时序表、数值表（29 号）；任务拆分、Phase、里程碑、工时（31 号）；主提示词与阶段提示词（32 / 33 号）；21 个字段的「技术参数」以外的设计取舍（28 号） |
| 不动的文件 | `src/`、`docs/zh/00` ~ `29`、`docs/en/*`、`.work/v011-BRIEF.md`、`tools/verify_project.py` 一律只读；本文只新建 `docs/zh/30-TECH-0.1.1.md` 一个文件 |

### 0.2 计数纪律

1. **注册对象、config 键、语言键三个数，本文全部自己数**，并写出「数了哪些文件的哪些行」。不抄事实卡 §0、不抄 28 号 §3.4。
2. 三张表各自满足「行数 = 声明的计数」「小计 = 逐行相加」「跨表不重不漏」。
3. 数值只允许三个来源：事实卡 §1 / §2 / §3、两数算术、本文标「本文确定」的技术参数（并给理由）。config 默认值**只能**取事实卡 §3.4。
4. 读取文件一律用 `[IO.File]::ReadAllLines($p,[Text.Encoding]::UTF8)`（本机 `Get-Content` 会把无 BOM 的 UTF-8 中文读成 GBK 乱码）。

---

## 1. 基线与注册面核对

### 1.1 平台与构建

| 项 | 值 | 证据 |
|---|---|---|
| 版本号 | 0.1.1（`mod_version`） | `gradle.properties:11` 现为 `mod_version=0.1.0`，本版改 0.1.1（D-124） |
| 构建基线 | MC 1.21.1 / NeoForge 21.1.213 / Java 21 | `gradle.properties:2-3`、`:16-17`、`.tools/jdk21`（21.0.2） |
| 构建入口 | `.tools\gw.cmd`（唯一入口，参数原样转发 gradle） | `gw.cmd:41`；任务名 `runClient` / `runServer` / `runData` / `build` 由 `build.gradle:28-47` 声明 |
| 运行目录 | `run/`（可用 `-PfsRunDir=` 隔离） | `build.gradle` 的 `gameDirectory` 属性 |
| Java 文件数 | **125 个 / 15,785 行**（`src/main/java` 全树，逐个 `ReadAllLines` 计数） | **本文自己数的**：`block` 4 / `blockentity` 3 / `behavior` 18（`behavior` 9 + `behavior/impl` 9）/ `client` 20 + `client/render` 1 / `combat` 6 / `command` 1 / `config` 1 / `datagen` 10 / `effect` 4 / `entity` 2 + `entity/thrown` 12 / `event` 3 / `explosion` 3 / `farmland` 2 / `combo` 无 / `item` 5 / `menu` 1 / `outpost` 1 / `recipe` 2 / `registry` 11 / `team` 2 / `trap` 2 / `util` 10 / 根 1 |
| `client` 包类数 | **21**（20 个顶层 + `client/render/TurretBulletRenderer`） | 本文实测，与事实卡 §1.5 的「21 个类」一致（§1.4 复核） |

> 说明：`behavior` 18 = `behavior` 顶层 9 + `behavior/impl` 9；`entity` 2 + `entity/thrown` 12 = 14。两者都按目录分别列出、相加即 125。

### 1.2 注册面逐类点名（**本文自己从源码数**，共 93）

**计数方法（可复现）**：逐个打开 `src/main/java/com/dashpig/farmstorm/registry/` 下 11 个文件，按「注册调用 + 注册 id 字面量」逐条数，不按 `EXPECTED_COUNTS` 反推。三类计数口径：

- `DeferredRegister#register(...)` / `registerItem` / `registerBlock` / `registerComponentType` 调用：数**调用次数**；
- 走工厂方法注册的（`cropItem` / `seedItem` / `crop` / `deployed` / `projectile`）：数**工厂调用次数**，id 由 `CropProfile.path()` 或首个参数字面量给出；
- `damage_type` 是**数据驱动**（`FarmstormDamageTypes.java:14` 的注释：实例由数据包 JSON 提供，代码侧只有 `ResourceKey`），数**静态 `ResourceKey` 常量个数**。

| # | registry | 数了什么（文件:行号） | 逐条 ID | 条数 |
|---|---|---|---|---|
| 1 | `Registries.ITEM` | `FarmstormItems.java`：22 次 `cropItem(...)`（`:36,38,40,42,44,47,49,51,53,55,57,59,61,63,65,70,72,75,79,85,91,95`）+ 7 次 `seedItem(...)`（`:89,100,101,102,103,104,106`）+ 3 次 `registerItem(...)`（`:110,113,116`） | 22 作物/物品：`puffed_wheat` `blast_carrot` `ubi_server` `toxic_ubi_server` `power_beet` `melon_slice_bomb` `melon_grenade` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `lucky_berry` `molotov` `zangief_cane` `wild_apple` `compressed_puffed_wheat` `friend_chives` `point_defense_fungus` `vibration_bean`；7 播种物：`chive_seeds` `puffed_wheat_seeds` `power_beet_seeds` `melon_grenade_seeds` `pumpkin_turret_seeds` `awakened_torchflower_seeds` `awakened_pitcher_plant_pod`；3 方块物品：`converter` `compressed_hay_block` `outpost_core` | **32** |
| 2 | `Registries.BLOCK` | `FarmstormBlocks.java`：15 次 `crop(...)`（`:26,27,28,29,30,31,32,33,34,35,36,37,38,39,43`）+ 8 次 `deployed(...)`（`:46,47,48,50,52,55,62,65`）+ 3 次 `registerBlock(...)`（`:69,73,82`） | 15 作物方块：`puffed_wheat_crop` `blast_carrot_crop` `ubi_server_crop` `toxic_ubi_server_crop` `power_beet_crop` `melon_grenade_stem` `pumpkin_turret_stem` `awakened_torchflower_crop` `awakened_pitcher_plant_crop` `fang_trap_cactus_block` `bamboo_firecracker_stalk` `red_mushroom_puff_block` `brown_mushroom_puff_block` `lucky_berry_bush` `friend_chives_crop`；8 部署方块：`blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `zangief_cane` `point_defense_fungus` `vibration_bean`；3 其它：`converter` `compressed_hay_block` `outpost_core` | **26** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | `FarmstormBlockEntities.java:24,28,31,34,38,42,50`（7 次 `REGISTER.register`） | `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `converter` `outpost_core` | **7** |
| 4 | `Registries.ENTITY_TYPE` | `FarmstormEntityTypes.java`：12 次工厂 `projectile(...)`（`:31,33,35,37,39,41,43,45,49,51,55,57`，工厂体在 `:72`）+ 1 次 `REGISTER.register("turret_bullet", ...)`（`:61`） | `puffed_wheat` `ubi_server` `toxic_ubi_server` `melon_slice_bomb` `melon_grenade` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `molotov` `compressed_puffed_wheat` `friend_chives` `vibration_bean` `turret_bullet` | **13** |
| 5 | `Registries.MOB_EFFECT` | `FarmstormMobEffects.java:24,28,32,36` | `wild_hunter` `zangief_piledriver` `q_bounce` `confusion` | **4** |
| 6 | `Registries.SOUND_EVENT` | `FarmstormSounds.java:21,26,31` | `wild_hunter_loop` `zangief_piledriver` `friend_chives` | **3** |
| 7 | `Registries.RECIPE_TYPE` | `FarmstormRecipes.java:23` | `converting` | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | `FarmstormRecipes.java:25` | `converting` | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | `FarmstormCreativeTabs.java:16` | `main` | **1** |
| 10 | `Registries.DAMAGE_TYPE` | `FarmstormDamageTypes.java:20,23,26`（3 个 `ResourceKey` 常量，数据驱动） | `void_bolt` `crop_blast` `zangief_slam` | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `FarmstormDataComponents.java:16` | `temporary_enchantments` | **1** |
| 12 | `Registries.MENU` | `FarmstormMenus.java:17` | `converter` | **1** |
| | **合计** | | | **93** |

逐类小计（与上表逐行相等）：

```
items                 32
blocks                26
block_entity_type      7
entity_type           13
mob_effect             4
sound_event            3
recipe_type            1
recipe_serializer      1
creative_mode_tab      1
damage_type            3
data_component_type    1
menu_type              1
--------------------------------
相加 = 32+26+7+13+4+3+1+1+1+3+1+1 = 93
```

**三条独立算法互为校验**：本文逐条点名 = **93**；`Farmstorm#logRegistrySelfCheck`（`Farmstorm.java:124-146`）的 12 条断言相加 = **93**（`:138-141` 逐类写死）；`tools/verify_project.py` 的 `EXPECTED_COUNTS`（`:197-210`）+ `EXPECTED` 的 ID 清单（`:157-194`）相加 = **93**。本文的数**不是**从后两者抄来的——它是按 §1.2 的「调用次数」口径重新数出来的，两条旁证只用来确认口径没跑偏。

### 1.3 本版注册面增量 = +0

| 本版新增的东西 | 为什么不进 registry |
|---|---|
| 1 个自定义网络包（D-142） | payload 通过 `RegisterPayloadHandlersEvent` / `NetworkRegistry` 注册，**不写 `BuiltInRegistries`**；`CustomPacketPayload.Type` 只是一个 `ResourceLocation` 记录（`CustomPacketPayload.java` 的 `record Type<T>(ResourceLocation id)`），不是注册项 |
| 2 个 `KeyMapping`（D-144） | 按键注册进客户端的 `Options#keyMappings` 数组（`RegisterKeyMappingsEvent#register` 的实现就是 `ArrayUtils.add(options.keyMappings, key)`），与 registry 无关 |
| CLIENT config 3 键（D-144） | `ModConfigSpec` 是配置文件模型，不进 registry |
| `/farmstorm hud …` 客户端指令（D-144） | Brigadier 指令树是运行期注册（`RegisterClientCommandsEvent`），不进任何 registry |
| 耕地绑定表（D-134） | `SavedData` 落盘，不进 registry |
| 区块 ticket（D-145） | `TicketType.create(...)` 是普通对象，1.21.1 **没有任何 ticket 注册表**（见 §2.5） |

⇒ **注册面仍为 93，与 §1.2 逐项相等**。本版的「+0」不是「没加东西」，而是「加的东西都不在 registry 面上」，这正是事实卡 §3.1 与 D-146 的口径。

### 1.4 语言键与 config 键的现状基线（本文自己数）

**SERVER config（现状 166 键 / 59 组）**：数法是读 `config/FarmstormConfig.java` 的静态块（`:268` 的 `static {` 到 `:928` 的 `SPEC = b.build();`），用「`b.push("…")` / `b.pop()` 维护组栈、`.define("…")` / `.defineInRange("…")` / `.defineInList("…")` 计一键」的规则逐键落地。本文用脚本跑了一遍并逐组人工复核，结果：

```
键 166 / 组 59
组 = 35 个顶层组 + 24 个嵌套组（blast_carrot.nausea 1；awakened_torchflower 下 6；
     awakened_pitcher_plant 下 3；red_mushroom_puff 下 2；brown_mushroom_puff 下 2；
     lucky_berry 下 6；power_beet 下 4）
```

**CLIENT config（现状 0 键）**：`Farmstorm.java:56` 是全仓唯一 `registerConfig`，类型 `ModConfig.Type.SERVER`；`ModConfig.Type.CLIENT` 全仓 0 命中（事实卡 §1.5，本文复核）。

**语言键（现状 315 × 2）**：读 `src/generated/resources/assets/farmstorm/lang/zh_cn.json` 与 `en_us.json`，`ConvertFrom-Json` 后取键集：`zh=315`、`en=315`、键集完全相同。按前缀分布（本文自己分的组，相加 315）：

```
farmstorm.configuration   166   ← 与 server config 键数逐键相等（1:1）
advancements.farmstorm     36
item.farmstorm             32
block.farmstorm            26
tooltip.farmstorm          16
entity.farmstorm           13
gui.farmstorm               8
death.attack                6
effect.farmstorm            4
subtitles.farmstorm         3
message.farmstorm           2
command.farmstorm           1
container.farmstorm         1
itemGroup.farmstorm         1
--------------------------------
166+36+32+26+16+13+8+6+4+3+2+1+1+1 = 315
```

**两条交叉核对的结论**：① 语言键里的 `farmstorm.configuration.*` **恰好 166 条**，与 `FarmstormConfig` 的 166 个 `define*` 一一对应（`ModLangProvider.java:438` 的 `config(...)` 是唯一来源）；② 语言键里的 `block.farmstorm.*` = 26、`item.farmstorm.*` = 32、`entity.farmstorm.*` = 13，与 §1.2 的 blocks / items / entity_type 三个数逐个相等——这三组是「注册面 ↔ 资源面」的硬约束，任何一个 ID 改名都会在这里炸出来。

---

## 2. 本版新增的技术构件（逐个给类设计）

### 2.1 网络包（**全项目第一个自定义包**）

#### 2.1.1 事实与边界

本仓此前自定义包 **0 个**（`CustomPacketPayload` / `RegisterPayloadHandlersEvent` / `PacketDistributor` / `SimpleChannel` 等 17 个关键字全 0 命中，事实卡 §1.4）。因此本节**先给签名证据、再给设计**：凡签名，本文已用本地源码 jar 核对，核对来源写在括号里；凡仍不确定的语义，一律进 §8。

核对来源（只读，未改动）：`D:\project_farm\.tools\gradle-home\caches\modules-2\files-2.1\net.neoforged\neoforge\21.1.213\…\neoforge-21.1.213-sources.jar` 里的 `PayloadRegistrar.java` / `RegisterPayloadHandlersEvent.java` / `PacketDistributor.java` / `RegisterClientCommandsEvent.java` / `RegisterKeyMappingsEvent.java`，以及 `…\neoformruntime\intermediate_results\sourcesAndCompiledWithNeoForge_…_output.jar` 里的 `StreamCodec.java` / `ByteBufCodecs.java` / `CustomPacketPayload.java` / `TicketType.java`。**这些签名不是猜的**；§8 里挂的是「运行时行为」，不是「有没有这个方法」。

已核对的签名：

| API | 签名（逐字） |
|---|---|
| `RegisterPayloadHandlersEvent` | `public PayloadRegistrar registrar(String version)`；事件实现 `IModBusEvent` ⇒ 挂在 **mod 总线** |
| `PayloadRegistrar` | `public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> reader, IPayloadHandler<T> handler)` |
| `PayloadRegistrar` 默认线程 | 私有 `register(...)` 里 `if (this.thread == HandlerThread.MAIN) handler = new MainThreadPayloadHandler<>(handler);` ⇒ **默认已在主线程** |
| `PacketDistributor` | `public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads)` |
| `CustomPacketPayload.Type` | `public static record Type<T extends CustomPacketPayload>(ResourceLocation id)` ⇒ 用 `new Type<>(Farmstorm.id("core_list"))` 构造，**不要**用 `CustomPacketPayload.createType(String)`（它走 `ResourceLocation.withDefaultNamespace`，会落进 `minecraft:` 命名空间） |
| `StreamCodec#composite` | 1 ~ 6 个字段的 6 个重载（`StreamCodec.java` 逐个核对） |
| `ByteBufCodecs` | `BOOL` / `VAR_INT` / `STRING_UTF8` 字段；`list()` 与 `list(int)` 返回 `CodecOperation`（配合 `StreamCodec#apply`） |
| `ResourceLocation.STREAM_CODEC` / `BlockPos.STREAM_CODEC` | 两者都存在（`javap` 核对） |
| `IPayloadContext` | `Player player()`、`CompletableFuture<Void> enqueueWork(Runnable)`、`PacketFlow flow()`、`default void reply(CustomPacketPayload)` |

#### 2.1.2 payload 设计（本文确定）

包名 `com.dashpig.farmstorm.network`；四个类（`network/` 3 个 + **仅客户端**的 `client/network/ClientCoreListHandler` 1 个）：

| 类 | 职责 |
|---|---|
| `CoreListPayload`（`network/`） | payload 本体；`implements CustomPacketPayload`；`public static final Type<CoreListPayload> TYPE = new Type<>(Farmstorm.id("core_list"))`；字段 `List<Core> cores` 与 `int total`；嵌套 `public record Core(ResourceLocation dimension, BlockPos pos, String ownerName, boolean self)`（本文确定：4 个字段正好是 D-143 的「维度 + 坐标 + 归谁 + 是否自己」） |
| `CoreListBroadcaster`（`network/`，仅服务端） | 组装、去抖、按收件人下发；唯一的发送入口调用 `PacketDistributor.sendToPlayer` |
| `ClientCoreList` / `ClientCoreListHandler`（`client/` 与 `client/network/`，仅客户端） | 客户端快照持有者（`volatile List<Core> cores` + `volatile int total`，只被 handler 写、只被渲染读）/ S2C handler（`enqueueWork` 写快照） |

codec（本文确定：4 字段用 4 元 `composite`，列表用 `list(64)` 封顶）：

```java
// Core 的 codec：4 个字段 → StreamCodec.composite 的 4 元重载
public static final StreamCodec<ByteBuf, Core> CORE_STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, Core::dimension,
        BlockPos.STREAM_CODEC,         Core::pos,
        ByteBufCodecs.STRING_UTF8,     Core::ownerName,
        ByteBufCodecs.BOOL,            Core::self,
        Core::new);

// 整个 payload：列表 + 真实总数（total 与 cores.size() 可以不等，见 2.1.6）
public static final StreamCodec<RegistryFriendlyByteBuf, CoreListPayload> STREAM_CODEC = StreamCodec.composite(
        CORE_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_CORES)), CoreListPayload::cores,
        ByteBufCodecs.VAR_INT,                                  CoreListPayload::total,
        CoreListPayload::new);
```

**为什么不把维度写成 `String`**：`ResourceLocation.STREAM_CODEC` 与 `writeResourceLocation` 同源，长度前缀与校验都由原版负责，自己拼字符串等于重写一次序列化（否决）。
**为什么 `total` 单独发**：D-143 的「最多 8 行核心 + 1 行 `+N`」（A-34）要求客户端知道**真实总数**；若只发截断后的列表，`+N` 只能显示 `64 − 8`，是错的（本文确定）。

注册（本文确定：版本串 `"1"`，挂 mod 总线，只在 `play` 阶段、只 CLIENTBOUND）：

```java
// Farmstorm 构造器里：modBus.addListener(FarmstormNetwork::onRegisterPayloads);
public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
    event.registrar("1")                       // 本文确定：握手版本串，codec 一变必须跟着变
         .playToClient(CoreListPayload.TYPE, CoreListPayload.STREAM_CODEC,
                       (payload, context) -> ClientCoreListHandler.handle(payload, context));
}
```

`ClientCoreListHandler` 里**只有一句** `context.enqueueWork(() -> ClientCoreList.set(payload.cores(), payload.total()))`，理由三条：

1. `PayloadRegistrar` 默认已用 `MainThreadPayloadHandler` 包过（见 2.1.1 表格），`enqueueWork` 在主线程上**立即执行**（`IPayloadContext` 的 javadoc 逐字：「For handlers running on the main thread, immediately executes the task」），因此不会多一次排队延迟；
2. 万一以后有人用 `executesOn(HandlerThread.NETWORK)`，这一行仍然安全——它把「必须主线程」写在调用点，而不是依赖注册器的默认值；
3. 客户端 handler **不读世界、不判定、不回包**（D-142 的三条边界），只做一次引用替换，因此没有锁、没有竞态。

**专用服务器不能加载客户端类**：`(payload, context) -> ClientCoreListHandler.handle(...)` 里的目标类只在该 lambda 的合成方法被调用时才解析，而 CLIENTBOUND 的 handler 在服务端永不执行 ⇒ 专用服务器不会 `NoClassDefFoundError`。这一点是本文确定的设计前提，§7 #6 有一条「专用服务器能起服」的断言兜它。

#### 2.1.3 服务端发送入口与去抖（本文确定）

`CoreListBroadcaster` 的四个入口 + 两条时间常数：

| 项 | 取值 | 理由 |
|---|---|---|
| `MIN_INTERVAL_TICKS` | **10**（0.5 秒） | 同一次连锁事件（放核 → 记账 → 边界粒子重建）可能在同刻触发多次请求；10 刻是最长 0.5 秒的可见延迟，人眼仍在「立即」区间 |
| `RECONCILE_INTERVAL_TICKS` | **100**（5 秒） | 用来兜「没有事件但列表变了」的路径（队友换队、离线/上线、漏事件）；20 人服务器每 5 秒 20 次指纹比较，成本可忽略 |
| `MAX_CORES` | **64** | 单包封顶；配合 §2.1.6 的体积估算 |

请求合并规则：`Map<UUID, Long> lastSentTick` + `Set<UUID> pending`；`request(player)` 只把 UUID 放进 `pending`；`ServerTickEvent.Post` 里统一 flush —— 若 `now − lastSentTick ≥ 10` 才真发，否则留到下一 tick。**同一刻的多次请求只发一次**（去抖的单位是「玩家 × 包」，不是事件）。

#### 2.1.4 推送时机清单（RT-31 要求逐条验收）

| # | 时机 | 挂点（全部是服务端、game 总线） | 收件人 | 去抖 / 说明 |
|---|---|---|---|---|
| 1 | **登录** | `PlayerEvent.PlayerLoggedInEvent`（`FarmstormGameEvents:58` 已有同名监听的注册位） | 该玩家 | 立即请求一次；若客户端此时尚未建好世界，由 #10 的对账兜底 |
| 2 | **重连** | 同 #1（服务端不区分首次与重连） | 该玩家 | 与 #1 同一条路径；`PlayerLoggedOutEvent` 里清 `lastSentTick` 与 `pending`，避免用旧刻号误判去抖 |
| 3 | **首次加入**（新玩家第一次进服） | 同 #1 | 该玩家 | 列表为空也发一次（`total=0`），让客户端把「没核」与「没收到包」区分开 |
| 4 | **放置核** | `OutpostCoreBlockEntity#setOwner`（`:95-103`）末尾 `CoreListBroadcaster.requestAll(server)` | 全部在线玩家 | 只影响「己方列表」的玩家会真的变，但收件人集合由每人自己的组装结果决定，所以统一请求 |
| 5 | **摧毁核** | `OutpostCoreBlockEntity#release`（`:166-170`）+ `settleOnRemoval`（`:315-335`）末尾 | 全部在线玩家 | 与 #4 同；迁移（D-136）在 `settleOnRemoval` 里已经改完绑定，故一次广播就够 |
| 6 | **迁移完成** | 同 #5（迁移是拆核的一个分支，不另开事件） | 全部在线玩家 | `release()` 与 `settleOnRemoval` 可能各请求一次；10 刻去抖把它们合并成一次发送 |
| 7 | **换维度** | `PlayerEvent.PlayerChangedDimensionEvent` | 该玩家 | 列表内容不变（跨维度全量，D-143），发它是为了覆盖「客户端在世界切换时丢过一次」的极小窗口；成本 1 包 |
| 8 | **重生** | `PlayerEvent.PlayerRespawnEvent` | 该玩家 | 同 #7，走 10 刻去抖 |
| 9 | **队友变化** | 无事件可用（原版 `Scoreboard` 不抛队伍变更事件）⇒ 由 `RECONCILE_INTERVAL_TICKS = 100` 的**指纹对账**发现 | 变化的那些玩家 | 最长 5 秒延迟；指纹 = 排序后的 `(dimension, pos, ownerName, self)` 列表的 `hashCode()` 与 `total`，不相等才发 |
| 10 | **对账 / 兜底** | `ServerTickEvent.Post` 每 100 刻 | 指纹变化的玩家 | 同时兜住「漏事件」「包被丢」「#1 时客户端还没就绪」三种情况 |

**断线兜底**：服务端在 `PlayerLoggedOutEvent` 清该玩家在 `lastSentTick` / `pending` / `fingerprint` 里的三条记录（不 clean 会让重连后的第一次请求被判成「刚发过」而跳过）。客户端在 `ClientPlayerNetworkEvent.LoggingOut` 里把 `ClientCoreList` 清成空列表 —— 否则**换服务器**后会看到上一个服务器的核（这是本版唯一会跨会话残留的客户端状态）。

#### 2.1.5 组装（服务端权威）

`CoreListBroadcaster.build(ServerPlayer viewer)`：

1. 取 `OutpostCoreIndex.of(level)`；对该索引的**全部 owner UUID** 迭代（需要新增只读访问器，见 §3 第 32 行）；
2. 对每个 owner，用 §2.2.5 的 **UUID 侧己方判定** 与 viewer 比较；不是己方就整段跳过（不把敌对核发给客户端，D-143）；
3. 是己方则把该 owner 的每条 `(dimension, pos)` 收进候选；`ownerName` 取索引里的 `ownerNames`（离线也有，`:55-56`）；`self = owner.equals(viewer.getUUID())`；
4. 取每个候选核的比较键：**只从索引读** `Entry#gameTime`（索引侧落盘的 `gameTime`，见 §2.2.10）——**不读方块实体、不加载区块**。老档条目缺该字段时取 `Long.MIN_VALUE`（视作最早，不伪造时间），并在启动时打一条计数；
5. 排序：**A-39 的三级比较键**——`gameTime` 升序 → **维度 id**（`dimension.toString()`）→ 核坐标字典序（`BlockPos#compareTo`：y → z → x）；三级完全相同只在同一维度同一格出现，物理上不可能；
6. `total = 候选总数`；`cores = 前 MAX_CORES 条`；发 `new CoreListPayload(cores, total)`。

#### 2.1.6 包大小估算（本文确定）

| 字段 | 编码 | 字节 |
|---|---|---|
| `dimension` | `ResourceLocation.STREAM_CODEC`（字符串，长度前缀 2 B + UTF-8） | `2 + len`，`minecraft:overworld` = 19 → **21** |
| `pos` | `BlockPos.STREAM_CODEC`（1 个 long） | **8** |
| `ownerName` | `STRING_UTF8`（2 + UTF-8） | 名字上限 16 → **≤ 18** |
| `self` | `BOOL` | **1** |
| 单核小计 | | **≈ 40 B（典型）/ ≤ 48 B（上限）** |
| `total` | `VAR_INT` | ≤ 5 |
| 列表长度前缀 | `ByteBufCodecs.list` 的 count | ≤ 3 |

⇒ 最坏 `64 × 48 + 8 ≈ 3.1 KB`，典型（8 个核）`≈ 0.4 KB`。相对原版自定义包的上限（1 MiB 量级，具体值见 §8 ⑥）低 2 ~ 3 个数量级，因此不需要分片、不需要压缩。

### 2.2 耕地-核心绑定表（**改存档格式**）

#### 2.2.1 现状（逐行核对）

`farmland/FarmlandLedger.java`（275 行）现有结构：

| 项 | 现状 | 行号 |
|---|---|---|
| 落盘名 | `farmstorm_farmland` → `world/data/farmstorm_farmland.dat` | `:44` |
| 本体 | `Map<UUID, LinkedHashSet<BlockPos>> byOwner`（**外层 `HashMap`**） | `:51` |
| 运行期覆盖 | `@Nullable Integer baseOverride`（**不落盘**） | `:57-58` |
| 额度 | `base()`（`:72-74`）/ `setBase(int)`（`:76-79`）/ `personalCapacity(int,int,int)`（`:82-84`）/ `sumOverMembers(int,int,int...)`（`:92-98`）/ `capacity(level,player)`（`:101-108`） | 同左 |
| 已用 | `used(level,player)`（`:111-121`，取坐标并集基数） | 同左 |
| 记账 | `add`（`:128-135`）/ `remove(BlockPos)`（`:141-158`；1.21.1 现状的写入点只有拆核路径 `OutpostCoreBlockEntity.java:332`，**绑定制之后它不再是唯一写入口**，见 §2.2.4 与 A-37） | 同左 |
| 读取 | `ownedBy(level, player)` / `ownedBy(level, UUID)`（`:161-177`，后者离线可查） | 同左 |
| 成员集 | `members(...)`（`:185-196`，**只取在线玩家**） | 同左 |
| 惰性校验 | `validated(...)`（`:203-219`）：坐标处方块必须仍是 `minecraft:farmland`，区块未加载按「有效」处理 | 同左 |
| 落盘/读盘 | `save`（`:228-244`）：`owners → {uuid → {positions → [{pos: long}]}}`；`load`（`:246-264`） | 同左 |

**关键事实（决定本节的写法）**：`byOwner` 是 `LinkedHashSet` 的内层 + `HashMap` 的外层 —— 「插入顺序即记录顺序」只在**同一个 UUID 内部**成立。另外，玩家自己挖掉耕地时并没有人调用减账入口（1.21.1 现状的写入点在拆核路径上），减项全靠 `validated()` 的惰性丢弃。

#### 2.2.2 v2 结构（本文确定）

```java
// 绑定：一块耕地 ↔ 唯一一个核
public record Binding(BlockPos pos, UUID owner, ResourceLocation coreDimension, BlockPos corePos) {}

// 核的键（跨维度唯一）
public record CoreKey(ResourceLocation dimension, BlockPos pos) {}

// 无核的遗留条目：耕地条目只有 pos（§2.2.2 第 2 点），所以它也没有维度
public record Orphan(BlockPos pos, UUID owner) {}

// 本体三份；bindings 是唯一真相，byCore 是内存反查索引，orphans 是老档补不上的遗留
private final LinkedHashMap<BlockPos, Binding> bindings = new LinkedHashMap<>();
private final Map<CoreKey, LinkedHashSet<BlockPos>> byCore = new HashMap<>();     // 由 bindings 派生，不落盘
private final LinkedHashMap<BlockPos, Orphan> orphans = new LinkedHashMap<>();     // 无核的遗留；落盘顺序仅诊断用
```

四条设计取舍（都标本文确定）：

1. **`byOwner` 不再落盘**（D-134 要求）：`binding.owner` 就是「谁的田」，`byOwner` 只在查询时按 `owner` 过滤派生 —— 双源会漂移。
2. **耕地的坐标仍然不带维度**（沿用 v1）：1.21.1 的旧 `.dat` 里 `pos` 是一个 long，突然加维度字段会让迁移分叉；而「耕地与它的核同维度」在绑定时已被 `inOwnRange` 的同维度过滤（`FarmlandGuard.java:92-107`，同维度过滤在 `:94-96`）保证，因此维度由 `coreDimension` 承载即可。**已知边界**：同一 `(x,y,z)` 在主世界与下界各有一块耕地时，v1 与 v2 都会撞（§9 第 5 条登记，不在本版修）。
3. **`orphans` 要落盘**（本文确定，理由：D-137 说「补不上的保持耕地不动」，若把它们的账目丢掉，下一轮启动就再没有机会补建，「保持不动」会退化成「永久遗忘」；落盘还让补建算法天然幂等）。`orphans` 用 `LinkedHashMap`，但**它的落盘顺序只作诊断 / 展示用，不参与任何判定**——补建顺序按 A-47 的全序排序（§2.2.8），与这里的插入顺序无关。
4. **`binding.owner` 与核主可以不同**：D-136 的迁移判据是「与**耕地主**同盟」，而耕地主是开垦者、核主是放置者，两者在队伍里可能不是同一人 ⇒ 必须逐条存 owner，不能从核反推。

NBT 形态（本文确定：键名全小写驼峰、与 `OutpostCoreIndex` 的 `owners/entries/dimension/pos` 同风格）：

```
dataVersion: 2                       // int；缺失视为 1（= 0.1.0 格式）
bindings: [                          // ListTag<TAG_COMPOUND>，顺序即插入顺序
  { pos: long, owner: "uuid-string", coreDimension: "minecraft:overworld", corePos: long }
]
orphans: [                           // ListTag<TAG_COMPOUND>；老档补建失败的那批（落盘顺序仅诊断用）
  { pos: long, owner: "uuid-string" }
]
```

#### 2.2.3 版本号与向下兼容读法（本文确定）

```java
private static FarmlandLedger load(CompoundTag tag, HolderLookup.Provider registries) {
    FarmlandLedger ledger = new FarmlandLedger();
    int version = tag.contains("dataVersion") ? tag.getInt("dataVersion") : 1;   // 缺失 = v1
    if (version >= 2) {
        readBindings(tag, ledger);        // 逐条校验：坐标处已不是 farmland 的直接丢（沿用 validated 口径）
        readOrphans(tag, ledger);
        // 老 v1 段若同时存在（人工拼过的档），一律并入 orphans，不并入 bindings（不猜绑定）
        readLegacyOwners(tag, ledger);
    } else {
        readLegacyOwners(tag, ledger);    // v1：owners → {uuid → {positions:[{pos}]}} 全部进 orphans
    }
    ledger.rebuildByCore();               // 反查索引从 bindings 重建
    return ledger;
}
```

三条纪律：

1. **只升不降**：v2 的 `save` 永远写 `dataVersion: 2` 并**不再写** `owners` 段。旧版（0.1.0）读 v2 档会读到空账本 —— 这在降级场景下等价于「所有耕地变未绑定」，不会铲世界，是可接受的降级方向（否决「双写两套格式」：双写就回到双源）。
2. **v1 的记录全部读成 `orphans`**（D-137 的口径）：`owners` 里没有核信息，无法猜出绑定关系；直接当绑定是发明数据。
3. **读入即校验**：`pos` 处方块不是 `minecraft:farmland` 且区块已加载的条目当场丢弃（与 `validated` 同一条判据），避免把早已被挖掉的坐标带进容量统计。

#### 2.2.4 `setDirty()` 时机（逐条点名）

| 动作 | 是否 `setDirty()` | 理由 |
|---|---|---|
| `bind(...)`（新绑一条） | 是 | 落盘内容变了 |
| `unbind(pos)`（拆一条） | 是 | 同上 |
| `migrate(...)`（改绑） | 是 | 一条 binding 的 `corePos` 变了 |
| `adoptOrphan(...)`（老档补建成绑定） | 是 | `orphans` 少一条、`bindings` 多一条 |
| `rebuildByCore()` | **否** | 纯内存派生，不落盘 |
| `validated()` 丢弃坏条目 | 是（**仅当真丢了条目**） | 与现状 `:214-217` 同一条纪律：没有变化不写盘 |
| `get` / `of` / 任何读 | 否 | — |

> 现状 `remove(BlockPos)` 里的 `setDirty()`（`:154-156`）与 `add` 里的（`:133`）保持原样语义：v2 的 `remove` = 先 `unbind` 再清 `byCore`，一次 `setDirty`。

#### 2.2.5 己方判定（UUID 侧，离线有效）

D-138 要求「改从 UUID 索引查」。现状的己方判定唯一链路是 `DeployedOwnership.isAlly(level, ownerUuid, ownerName, Player target)`（`util/DeployedOwnership.java:48`）→ `TeamResolver#isAlly(level, Owner, Player)`，**target 必须是 `Player` 实体**，离线玩家拿不到。因此本文确定：给 `TeamResolver` 加一个 Owner ↔ Owner 的重载，`Player` 版本委托它，**不新开第三条判据**：

```java
// team/TeamResolver.java 新增（本文确定）
boolean isAlly(ServerLevel level, Owner owner, Owner target);

// team/ScoreboardTeamResolver.java：把现有逻辑原样搬进新重载，Player 版本变成一行委托
@Override public boolean isAlly(ServerLevel level, Owner owner, Player target) {
    return isAlly(level, owner, new Owner(target.getUUID(), target.getScoreboardName()));
}
@Override public boolean isAlly(ServerLevel level, Owner owner, Owner target) {
    if (owner.uuid().equals(target.uuid())) return true;              // 本人恒为真（现状 :29-31）
    Optional<String> a = teamName(level, owner);                      // 离线可查：按名字查记分板成员表
    if (a.isEmpty()) return FALLBACK_PUBLIC.equals(FarmstormConfig.untamedFallback());  // 现状 :33-37
    Optional<String> b = teamName(level, target);
    return b.isPresent() && a.get().equals(b.get());
}
```

`teamName(...)` 的实现本来就按 `ownerName` 查 `Scoreboard#getPlayersTeam`（`:21-25`），**离线也能用** —— 这是 D-138 能成立的技术根据，不是新增能力。

**已知语义变化（本文确定）**：无队伍玩家在 `untamedFallback = "self"`（默认）下，`isAlly(自己,自己) = true`、对其他人 `false`；改成 `"public"` 时全员互为己方。这与现状 `Player` 版本逐条等价（同一段逻辑搬家），不会改变既有玩法。

**踩踏面的收口（A-45）**：上面这套「有绑定即受保护」**只适用于踩踏面**（`protectsFromTrample`，`:120-134`）。**额度闸（锄地）与浮窗仍按实时己方判定**：锄地走 `FarmlandGuard.check` 的 UUID 侧判据（§2.2.6 / §2.2.7），浮窗走每次发送时的实时 `isAlly`（§2.1.5）——换队后这两处**立即**变化，只有踩踏面保持「绑定存在即保护」，因为踩踏事件本身不带查询者（29 号 B-18 已按 A-45 收窄）。

#### 2.2.6 容量与已用（D-139）

| 量 | 算式 | 说明 |
|---|---|---|
| 每核容量 | `outpost_core.farmlandBonus`（默认 16，范围 0 ~ 1024） | **不新增键**（D-134）；`0` 时该核不能绑任何耕地 |
| 核的空位 | `farmlandBonus − byCore.get(key).size()` | 反查集合的基数；`byCore` 由 `bindings` 派生，**不落盘** |
| 队伍容量 `capacity(level, player)` | `Σ over 己方核: farmlandBonus` = `bonus × 己方核数` | 「己方核」= `OutpostCoreIndex` 里 owner 与 player 互为己方的全部条目（**含其它维度**，离线也有）；需要给索引加一个 `count(MinecraftServer, UUID)` 的公开重载（§3 第 32 行） |
| 已用 `used(level, player)` | `bindings` 里 `owner` 与 player 互为己方的条数 | 与容量同源（D-139）；`orphans` **不计入**（没绑定就不占名额，D-137 的「只是不受保护」） |
| 单核占地 | `byCore.get(key).size()` | 锄地时判「还有空位」就用它；**不用** `bindings` 全表过滤（那退化成 O(n) 全扫） |

**一键两义**（RT-33）：`outpost_core.farmlandBonus` 同时是「每核容量」与「额度基数」。本文确定：在 `FarmstormConfig.java:911` 那一行的 `comment(...)` 里写明「本键同时决定每核可管理的耕地块数与队伍额度基数（容量 = 本键 × 己方核数）」，**不改键名、不加键**。

#### 2.2.7 锄地绑核（D-135）

```
FarmlandGuard.check(level, player, pos) 的状态机（三态不变）：
  1) 范围闸：pos 是否落在任一己方核的 3×3 区块内？否 → NO_CORE
  2) 候选集：上述覆盖 pos 的己方核里，filter( byCore 空位 > 0 )
  3) 候选为空 → QUOTA_FULL（语义从「队伍额度满」变为「最近的那个己方核满了」，键名不改，见 §9 第 4 条）
  4) 取候选里 distSqr(core.pos, pos) 最小者；平手按 A-39 三级键：placedGameTime 升序 → 维度 id → 核坐标字典序（BlockPos#compareTo）
  5) 返回 ALLOW + 选中的核（新增一个携带结果的重载，如 record Decision(Verdict verdict, @Nullable CoreKey core)）
```

`FarmstormGameEvents.onRightClickBlock`（`:194-217`）在 `ALLOW` 分支里把 `ledger.add(level, player, pos)` 换成 `ledger.bind(player.getUUID(), new CoreKey(level.dimension().location(), decision.core()), pos)`。

**距离口径（本文确定）**：`BlockPos#distSqr(BlockPos)`（两个方块中心的三维欧氏距离平方）。理由：D-135 只写「最近」，而 3×3 区块的判定本身是切比雪夫（`:139-145`），若距离也用切比雪夫，同一个「最近」会在对角方向给出多个并列，绑核结果不可预期；欧氏距离在同维度下几乎不会并列，剩下的并列交给下面这条裁决。

**平手判据（A-39，本节定死，迁移与补建同用）**：

1. 第一级 `placedGameTime` **升序**（先放置的核优先）；
2. 第二级 **维度 id**：`coreKey.dimension().toString()` 升序（**A-39 定稿补的第三级**：两个不同维度的核可能同一刻放置、坐标也完全相同，两级会在这一步并列，必须再有一级分开）；
3. 第三级 **核坐标字典序**：`BlockPos#compareTo`（`Vec3i` 的既有实现：先 y、再 z、最后 x）；
4. **不得依赖任何 `Map` / `List` 的插入顺序**：`OutpostCoreIndex.byOwner` 是 `HashMap`（`:53`）、`FarmlandLedger.byOwner` 的外层也是 `HashMap`（`:51`），跨 owner 的遍历顺序不稳定；把「先放置」挂在插入顺序上会让同一份存档在两次启动里给出不同的绑核结果。

> **三条判据的分工（A-47 取代 A-36，不许混）**：**A-47 管「补建按什么顺序处理」**——`BlockPos` 单级全序（y → z → x，见 §2.2.8），因为账本条目只有 `pos`；**A-39 管「候选核之间怎么比」**——任何一次「在多个候选核里选一个」的判定（锄地绑核、迁移、补建）都用上面**三级键**；**两者的键长故意不同**：核心有维度（`OutpostCoreIndex.Entry` 带 `dimension`）、耕地没有（`FarmlandLedger` 的条目只有 `pos`，`:46-48`）。两处都不许看任何 `Map` / `List` 的迭代顺序。

#### 2.2.8 启动补建（D-137，伪代码）

```
backfill(server):                                  // 只在服务端启动后跑一次
  total = orphans.size(); bound = 0; kept = 0
  work = orphans.values().sortedBy(BlockPos#compareTo)   // ★ A-47 rev.2：耕地只有 pos，单级即全序（y → z → x）
                                                        //   与 §2.2.7 的「候选核三级键」不同：核心有维度、耕地没有
  for (item in work):
      if (区块已加载 && 方块已不是 farmland)  → orphans.remove(item.pos) ; continue   // 方块没了，条目作废
      candidates = 与 item.owner 互为己方的核
                   where inRange(core, item.pos) && byCore(core).size() < bonus
      if (candidates.isEmpty()) → kept++ ; continue      // ★补不上：保持不动，也不动世界
      core = candidates.minBy(distSqr, then A39KEY)       // A39KEY = (gameTime, 维度 id, BlockPos#compareTo)
      bind(item.owner, core, item.pos); orphans.remove(item.pos); bound++
  LOGGER.info("耕地补建：绑定 {} 块 / 保持未绑定 {} 块（老档共 {} 块，按 BlockPos 全序）", bound, kept, total)
```

三条硬约束：

1. **补建绝不写方块**：整段没有 `setBlock`，「补不上保持耕地不动」由此在代码层面成立（D-137）。
2. **补建只跑一次**：由服务端启动钩子调用（与 `Farmstorm` 的既有初始化同层：`FarmstormGameEvents.register(...)` 之后、首个 `ServerTickEvent.Post` 之前，用一次性 `boolean` 闸；本文确定用「首个 `ServerTickEvent.Post` 里跑一次」而不是构造器里跑，因为构造器阶段 `MinecraftServer` 还没就绪）。
3. **补建的处理顺序 = `BlockPos` 单级全序**（A-47 rev.2）：`work` 先按 `BlockPos#compareTo`（y → z → x）排序，再逐条处理；`orphans` 的落盘顺序与 `byOwner` 的 `HashMap` 迭代顺序**都不参与判定**。账本条目只有 `pos`，同一个 `pos` 在账本里只有一条 ⇒ 单级比较就是全序；与 §2.2.7 的候选核三级键**故意不同**（核心有维度、耕地没有）。容量 16 是硬顶，顺序改变会改变结果 ⇒ 顺序是验收面（§7 #19 的 N / M 必须可复现）。

#### 2.2.9 迁移（D-136，伪代码）

```
settleOnRemoval()   // OutpostCoreBlockEntity:315-335，改后
  ...(保留 :316-326 的「真移除 / 卸载」判据，一个字不改)...
  CoreKey dead = new CoreKey(level.dimension().location(), corePos)
  List<Binding> mine = ledger.bindingsOf(dead)               // 只处理本核名下
  Map<BlockPos, CoreKey> rebind = new LinkedHashMap<>()
  List<BlockPos> toDirt = new ArrayList<>()
  List<BlockPos> unbindOnly = new ArrayList<>()
  for (b in mine):
      candidates = 己方核（与 b.owner 互为己方）
                   where core != dead
                     && inRange(candidate, b.pos)
                     && byCore(candidate).size() < bonus
      if (!candidates.isEmpty()):
          rebind.put(b.pos, nearest(candidates))   // nearest = distSqr → A39KEY（三级）
      else if (区块已在内存 && 方块仍是 farmland):
          toDirt.add(b.pos)                                  // 迁不动 + 看得见 ⇒ 变泥土（D-136）
      else:
          unbindOnly.add(b.pos)                              // ★ A-35：迁不动 + 区块未加载
                                                             //   ⇒ 只摘绑定、世界不动（不 setBlock、不加载区块）
  // 先算完，再动世界（沿用 :312-313 的既有理由：避免边遍历边改账本）
  for ((pos, core) in rebind): ledger.migrate(pos, core)     // 改绑 + 同步 byCore
  for (pos in toDirt):    server.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL)
                          ledger.remove(pos)
  for (pos in unbindOnly): ledger.remove(pos)                // 只摘账，不碰方块
  this.release()                                             // 已含 ticket 释放 + 广播（§2.5）
```

**A-35 的分支是本版最容易写错的一条**：核被毁时，若某块耕地的区块**根本不在内存里**，实现者无法确认那一格还是不是耕地；此时**降级为「失去保护」而不是「变泥土」**。理由与 `:304-310` 的既有取舍同向：错判方向的代价不对称——少铲一块只是失去保护，多铲一块是数据丢失。实现上**不许**为了铲这块地去 `getChunk(...)` 强制加载（那会连锁加载一批区块，比「临时 ticket」的语义还重）。

**「最近优先」的顺序与锄地同一条**（`distSqr` 最近 → 三级比较键 `placedGameTime` → 维度 id → 坐标字典序，A-39），理由同上：两套距离口径会让「绑给近的」与「迁给近的」给出不同答案（本文确定）。

**注意「不通知耕地主」**（D-136 补充）：迁移不写聊天、不发包变化提示，改绑只体现在下一次浮窗刷新。

#### 2.2.10 比较键的载体：`placedGameTime` 落在索引侧（A-39）

**结论**：比较键的第一级 `placedGameTime` 由 **`OutpostCoreIndex` 的条目承载并落盘**；比较路径只读索引，**不读方块实体、不加载任何区块**。

**数据结构**：`OutpostCoreIndex.Entry` 增加 `long gameTime` —— `record Entry(ResourceLocation dimension, BlockPos pos, long gameTime)`；落盘每条为 `{dimension, pos, gameTime}`（`OutpostCoreIndex:209-238` 的既有 `entries` 列表里加一个键）。

**写入时机（四处，逐条点名）**：

| # | 时机 | 写什么 | 落点 |
|---|---|---|---|
| 1 | **放置** | 把 `OutpostCoreBlockEntity#setOwner`（`:95-103`）里**同一个** `this.placedGameTime` 同时写进 BE 与索引：`record(server, placer, pos, placedGameTime)` | 同一方法、同一变量、同一 tick ⇒ 两个副本不可能漂移 |
| 2 | **摧毁** | 条目随 `release()` → `index.remove(...)` 一起消失，`gameTime` 不需要单独维护 | `OutpostCoreBlockEntity:166-170` |
| 3 | **读档** | `load` 读 `gameTime`；**缺失**（0.1.0 的索引没有这个字段）取 `Long.MIN_VALUE` | `OutpostCoreIndex:240-266` |
| 4 | **补建** | **不写** —— 补建只动耕地绑定，不产生核，也不改核的比较键 | §2.2.8 |

**候选核区块未加载时怎么拿到比较值**：**从索引拿**。索引是 `SavedData`（`OutpostCoreIndex:41` 的 `farmstorm_outpost_core`），读取不需要任何区块在内存；因此取值路径上不存在 `getChunk` / `getBlockEntity` / `hasChunkAt` 调用 —— 「**不为比较而加载候选核所在区块**」是结构性的（不是靠约定），A-39 的这条硬约束由此成立。

**为什么不在方块实体上读（否决）**：BE 只有区块在内存时才存在（`level.getBlockEntity` 返回 `null`）。即使 D-145 的 ticket 最终会加载每个核的区块，**加载有先后**（世界刚开、ticket 尚未生效、维度未 tick），比较值就会随加载时机漂移，同一份存档两次启动可能给出不同排序 —— 这正是 A-39 要禁的不可复现。

**老档（0.1.0 索引）的边界**：没有 `gameTime` 的条目一律取 `Long.MIN_VALUE`（视作「早于一切已知时间」），老核之间靠第二、三级键分开；**不做「趁加载时从 BE 补写」** —— 那会让值依赖「当时区块有没有被加载」，反而破坏可复现性。启动时打一条计数：`核心比较键缺失（老档）：N 条`。

**可观测性（A-39 的验收面，§7 #9 / #18 就按这三行断言）**：

1. 启动 + 补建之后各打一条 INFO：`核心比较键指纹 <hex16>（N 条）`；内容 = 按 `uuid` 排序后逐行 `uuid gameTime dimension pos` 的 SHA-256 前 16 位（逐条内容在 DEBUG：`核心比较键 <uuid> <gameTime> <dimension> <pos>`）。
2. 同一时刻打一条 INFO：`耕地绑定指纹 <hex16>（M 条）`；内容 = 按 `(pos, coreDimension, corePos)` 排序后逐行 `pos|coreDimension|corePos` 的 SHA-256 前 16 位。
3. 两个指纹只依赖存档内容与比较键，不依赖遍历顺序或加载时机 ⇒「同一份存档两次启动、同样的候选集 ⇒ 同一套绑定」可以只比两行日志。

### 2.3 服务端指令树的删除

| # | 删什么 | 位置 | 删完的后果 |
|---|---|---|---|
| 1 | `command/FarmstormCommands.java` **整文件 58 行** | 全文 | `/farmstorm farmlandlimit` 消失；`base` 失去唯一写入口（D-139） |
| 2 | 服务端指令注册 **3 处** | `event/FarmstormGameEvents.java:4`（import）、`:62`（`gameBus.addListener(FarmstormGameEvents::onRegisterCommands)`）、`:71-73`（`onRegisterCommands` 方法体，`:72` 调 `FarmstormCommands.register`） | 服务端 `RegisterCommandsEvent` 上不再有本模组的监听 |
| 3 | config **整组 `farmland`**（1 键） | `config/FarmstormConfig.java:904-908`（`push` / `comment` / `translation` / `defineInRange` / `pop`）+ 字段声明 `:226` | `farmstorm-server.toml` 不再有 `[farmland]` 段；组数 59 → 58 |
| 4 | 语言键 **2 条** | `command.farmstorm.farmlandlimit.set`（`zh_cn.json:64`）、`farmstorm.configuration.farmland.limitPerPlayer`（`zh_cn.json:145`）；生成它们的 `datagen/ModLangProvider.java:393-394` 与 `:404-406` 各删一段 | 语言键 315 → 323（净 +8，见 §4.3） |
| 5 | `FarmlandLedger` 的 base 三件套 | `:57-58`（`baseOverride` 字段）、`:72-74`（`base()`）、`:76-79`（`setBase`）、`:82-84`（`personalCapacity`）、`:92-98`（`sumOverMembers`） | `capacity` 改成「bonus × 己方核数」，不再有 base 参数 |

**删完后 `/farmstorm` 在服务端不存在**：专用服务器控制台执行 `/farmstorm` 会得到「未知指令」。**但单人/客户端里 `/farmstorm` 仍在**——那是 D-144 的客户端指令 `/farmstorm hud …`（`RegisterClientCommandsEvent` 注册到客户端派发器），与 §2.3 删掉的服务端指令**不共用注册点、不共用根节点语义**。§7 #12 的断言按这条区分写（本文确定：这条区分必须写进验收，否则会拿「单人还能敲出 `/farmstorm`」误判成删除失败）。

### 2.4 客户端构件（**全项目第一批**）

现状：`client` 包 21 个类（§1.1）、`KeyMapping` 注册 **0**、CLIENT 类型 config **0**、客户端指令 **0**（事实卡 §1.5，本文复核）。本版新增 **6** 个类（表中第 2 行合并了 2 个：`ClientCoreList` 在 `client/`、`ClientCoreListHandler` 在 `client/network/`）：

| 新类 | 位置 | 职责 |
|---|---|---|
| `FarmstormKeys` | `client/` | 2 个 `KeyMapping` 常量 + `RegisterKeyMappingsEvent` 注册 |
| `ClientCoreList` / `ClientCoreListHandler` | `client/` / `client/network/` | payload 快照 / S2C handler（`enqueueWork` 写快照；仅客户端加载，见 §2.1.2） |
| `CoreHudRenderer` | `client/` | 浮窗绘制（`RenderGuiEvent.Post`） |
| `CoreHudDragScreen` | `client/` | 拖拽定位的 `Screen` |
| `CoreHudClientCommands` | `client/` | `/farmstorm hud <on\|off\|reset>` 的注册与执行 |

#### 2.4.1 2 个 `KeyMapping`（本文确定）

```java
public static final KeyMapping TOGGLE = new KeyMapping(
        "key.farmstorm.core_hud.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H,
        KeyMapping.CATEGORY_GAMEPLAY);
public static final KeyMapping DRAG = new KeyMapping(
        "key.farmstorm.core_hud.drag",   InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J,
        KeyMapping.CATEGORY_GAMEPLAY);
```

- 构造器用已核对的 `KeyMapping(String, InputConstants.Type, int, String)`（`javap` 核对，1.21.1 共 6 个构造器，本版取「string + type + keycode + category」这一个）；
- **分类复用 `KeyMapping.CATEGORY_GAMEPLAY`**（本文确定）：自带分类要新增一条 `key.categories.*` 语言键，而复用原版分类的代价只是「按键出现在原版的『游戏操作』页」——语言键少 1 条，且 1.21.1 的按键界面本来就按分类分页（否决自建分类：多一条语言键，且与 §4.3 的 323 对不上账）；
- **默认键 H / J**（本文确定）：1.21.1 的原版默认键位表里 H 与 J 都没有绑定（原版占用的是 W/A/S/D、空格、Shift、Ctrl、E、Q、F、T、Tab、M 中键、`/`、P、F5、F11、L、1~9 等）；选两个相邻的、非功能键的字母键便于「开浮窗 + 进拖拽」连按。§8 ⑤ 保留一条「与其它模组撞键」的运行时确认。

注册（**mod 总线**，已核对 `RegisterKeyMappingsEvent` 实现 `IModBusEvent`，且 `@EventBusSubscriber` 有 `bus()` 属性）：

```java
@EventBusSubscriber(modid = Farmstorm.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FarmstormKeys {
    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
        event.register(DRAG);
    }
}
```

消费在既有客户端 tick 钩子里（`client/FarmstormClient.java:78-81` 的 `ClientTickEvent.Post`）：

```java
while (FarmstormKeys.TOGGLE.consumeClick()) { toggleHud(); }         // 取反 hud.enabled + 写盘 + 动作栏回显
while (FarmstormKeys.DRAG.consumeClick())   { openDragScreen(); }    // Minecraft#setScreen(new CoreHudDragScreen())
```

#### 2.4.2 CLIENT config 3 键（**第一次 `ModConfig.Type.CLIENT`**）

新建 `config/FarmstormClientConfig.java`（**不放进 `FarmstormConfig`**：两份 spec 的生命周期与加载侧都不同，混在一个类里会让既有 166 键的 `bake()` 语义变复杂）：

```java
public static final ModConfigSpec SPEC;
public static ModConfigSpec.BooleanValue hudEnabled;
public static ModConfigSpec.IntValue hudX;
public static ModConfigSpec.IntValue hudY;

static {
    ModConfigSpec.Builder b = new ModConfigSpec.Builder();
    b.push("hud");
    hudEnabled = b.comment("浮窗开关（客户端本地设置；按键与 /farmstorm hud 都会改它）")
                  .translation("farmstorm.configuration.hud.enabled")
                  .define("enabled", true);
    hudX = b.comment("浮窗左上角的 GUI 像素 x（绘制时还会按屏幕宽二次钳制）")
            .translation("farmstorm.configuration.hud.x")
            .defineInRange("x", 4, 0, 3840);
    hudY = b.comment("浮窗左上角的 GUI 像素 y（绘制时还会按屏幕高二次钳制）")
            .translation("farmstorm.configuration.hud.y")
            .defineInRange("y", 4, 0, 2160);
    b.pop();
    SPEC = b.build();
}
```

- 注册点：`Farmstorm` 构造器里 `Farmstorm.java:56` 之后加一行 `container.registerConfig(ModConfig.Type.CLIENT, FarmstormClientConfig.SPEC);`（**`Farmstorm.java` 里不能出现任何 client-only 类引用**，因此这行只引用 `FarmstormClientConfig`——它只依赖 `ModConfigSpec`，是公共类）；
- **生成位置**：`<gameDir>/config/farmstorm-client.toml`（`gameDir` = `run/` 或 `-PfsRunDir=` 指定的目录）。与 `farmstorm-server.toml` 并列，**不进 server 那份**；
- **默认值 3 个**：`enabled = true`、`x = 4`、`y = 4`（D-144 第 3 条 + 事实卡 §2 表下注的 `(4,4)`）；
- **`x` / `y` 的取值范围**（本文确定）：`0 ~ 3840` / `0 ~ 2160`（4K 横屏 GUI 像素的常见上界）。真正的边界在绘制与拖拽时按 `guiWidth − 面板宽` / `guiHeight − 面板高` 二次钳制，config 的 int 值域只用来挡住明显的坏值（否决「用 `0 ~ 1920`」：4K 屏上会把浮窗永远钉在左半屏）；
- **运行期写盘**：`FarmstormClientConfig.HUD_ENABLED.set(v); FarmstormClientConfig.SPEC.save();`（`ConfigValue#set(T)` 与 `ModConfigSpec#save()` 均已 `javap` 核对存在）。**不要**手写 `Files.write`。

#### 2.4.3 客户端指令 `/farmstorm hud …`

注册：`RegisterClientCommandsEvent`（已核对存在，`net.neoforged.neoforge.client.event`，**game 总线、仅客户端**，`getDispatcher()` / `getBuildContext()`）。挂在 `client/FarmstormClient.java` 的 `@EventBusSubscriber(Dist.CLIENT)` 上即可（该类已是 game 总线订阅者）：

```java
@SubscribeEvent
public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
    CoreHudClientCommands.register(event.getDispatcher());
}

// CoreHudClientCommands
dispatcher.register(Commands.literal("farmstorm").then(Commands.literal("hud")
        .then(Commands.literal("on")   .executes(ctx -> set(ctx.getSource(), true)))   // feedback: command.farmstorm.hud.on
        .then(Commands.literal("off")  .executes(ctx -> set(ctx.getSource(), false)))  // feedback: command.farmstorm.hud.off
        .then(Commands.literal("reset").executes(ctx -> reset(ctx.getSource())))));    // feedback: command.farmstorm.hud.reset
```

- **权限**：不设 `requires(...)`（客户端指令只改本地显示，设权限反而会让单人创造模式以外的玩家用不了）；
- **反馈**：`source.sendSuccess(() -> Component.translatable("command.farmstorm.hud.on"), false)`——`false` = 不进聊天广播，只在本地显示（本文确定：与现状 `FarmstormCommands:53` 的 `true` 相反，因为这是本地开关，没有「别人要知道」的必要）；
- **`reset`**：把 `x` / `y` 写回 `4` / `4` 并 `save()`，`enabled` 不动（本文确定：`reset` 的语义是「位置复位」，不是「恢复默认开关」；D-144 只给了三个动作名，这里定它的边界）。

#### 2.4.4 浮窗渲染（`CoreHudRenderer`）

注册形态照 `client/WildHunterHud.java`：`@EventBusSubscriber(modid, value = Dist.CLIENT)` + `@SubscribeEvent public static void onRenderGuiPost(RenderGuiEvent.Post event)`（`:19`、`:40-41`），绘制全走 `GuiGraphics#fill`（`:60-67`）与 `#drawString`（已核对签名：`drawString(Font, String, int, int, int, boolean)`）。

前置条件（**与 `WildHunterHud:46` 的差别只有一处**，本文确定）：

```java
Minecraft mc = Minecraft.getInstance();
if (mc.player == null || !FarmstormClientConfig.HUD_ENABLED.get()) return;      // 无玩家 / 开关关
if (mc.options.hideGui && !isDragging) return;                                   // hideGui 优先级最高，拖拽态例外
if (mc.screen != null && !(mc.screen instanceof CoreHudDragScreen)) return;      // 有别的 Screen 时不画
if (ClientCoreList.cores().isEmpty()) return;                                    // 0 核不画面板
```

**为什么拖拽态要例外**：`WildHunterHud:46` 的 `minecraft.screen != null` 会让 HUD 在 Screen 打开时整体消失，而拖拽模式必须**看着自己拖的东西**（D-144 补充已把这条定为技术必然）。同理 `hideGui`（F1）在拖拽态也放开——玩家按 J 进来就是要看见它。

版式（**以 A-41 为准**：行高 **9** / 行距 **1** / 内边距 **2**，面板高 **10n + 3** ⇒ 8 行 83 px、9 行 93 px；A-34 的 93 px 是它的**等价推论**，两套分解不许并存。配色取 D-143 与事实卡表下注；29 号只定字形与配色细节，**不得改行数、行高、面板高**）：

| 项 | 取值 | 理由 |
|---|---|---|
| 行高 / 行距 / 内边距 | **9 px / 1 px / 2 px**（`Font#lineHeight` = 9 已核对） | **A-41**：与 29 号 §5.6 同口径；+3 的分解以 29 号 §5.6 为准，本文不再独立定义 |
| 面板宽 | `max(font.width(row)) + 4`（逐帧算，不缓存；左右各 2 px） | 名字长度可变；`Font#width(String)` 已核对 |
| 面板高 | `10n + 3`（= `n × (9 + 1) + 3`）⇒ **8 行 = 83 px、9 行 = 93 px** | A-41 的等价推论；8 行核心 + 1 行溢出 = 最高 9 行 = **93 px**（A-34） |
| 最大行数 | **9 行**（8 行核心 + 1 行 `+N`） | A-34 |
| 背景 | `0x80000000`（半透明黑） | 与 `WildHunterHud` 的 `COLOR_EMPTY = 0x60101010` 同一族但更黑，保证白字可读 |
| 边框 | `0xFF000000`，1 px（`fill` 四次） | 与 `WildHunterHud:60` 同法 |
| 自己 | `0xFF55FF55`（绿） | D-143 / 事实卡 §2 表下注 |
| 队友 | `0xFFFFFFFF`（白） | 同上 |
| 行格式 | `#%d [%s] (%d, %d, %d) %s` | 第 1 项 = 1 基序号（**按截断前的全量排序**编号，所以 `+N` 时编号连续不断），第 2 项 = 维度 id 的 **path**（`overworld` / `the_nether` / `the_end`，不新增语言键），第 5 项 = 自己取 `gui.farmstorm.core_hud.self`、队友取 `ownerName` |
| 溢出 | 第 9 行画 `"+" + (total − 8)`，**占一行**（A-34：不挤进前 8 行） | D-143 第 4 条 + A-34；`+N` 是纯数字拼接，**不新增语言键**（本文确定：语言键已经算到 323，且 `+N` 在任何语言下都是同一个符号） |
| 绘制原点 | `(hudX, hudY)`，绘制前钳制到 `[0, guiWidth − 宽] × [0, guiHeight − 高]` | 屏幕尺寸变化（窗口拉伸 / GUI 缩放）后旧坐标可能越界 |

#### 2.4.5 拖拽 `Screen`（`CoreHudDragScreen`）

| 项 | 设计（本文确定） |
|---|---|
| 打开 | `Minecraft#setScreen(new CoreHudDragScreen())`（不暂停世界，但 `isPauseScreen()` 返回 `true` ⇒ 单人自动暂停，D-144 第 4 条） |
| 命中 | `mouseClicked(mx, my, button)`：若 `(mx,my)` 落在面板矩形内 → `dragging = true`，记 `grabDx = mx − hudX`、`grabDy = my − hudY`；否则忽略（不关界面） |
| 拖动 | `mouseDragged(mx, my, button, dx, dy)`：`hudX = clamp(mx − grabDx)`、`hudY = clamp(my − grabDy)`；钳制范围与 §2.4.4 的绘制钳制同一表达式 |
| 释放 | `mouseReleased(...)` → `dragging = false`（**不写盘**：一次拖动会触发几百次释放前的位置变化，逐帧写盘没有意义） |
| 保存 | `onClose()`（ESC / 关闭）→ 把当前 `hudX` / `hudY` 写进 config 并 `SPEC.save()`；**取消**路径不存在（D-144 只给「ESC 保存」） |
| 背景 | `renderBackground` 默认实现即可（1.21.1 的 `Screen#renderBackground` 画模糊/暗化）；**不画**面板本体——面板由 `CoreHudRenderer` 的拖拽例外负责，保证「拖的时候看到的就是最终效果」 |
| 与 `hideGui` 的关系 | 拖拽态无视 `hideGui`（F1）：进入拖拽就是编辑态（§2.4.4 前置条件第 2 条） |
| 与 `screen != null` 的关系 | 只有 `CoreHudDragScreen` 自己例外；任何其它 Screen 打开时浮窗都不画（包括 `ConverterScreen`、聊天栏） |
| 单人暂停的影响 | 暂停时 `ClientTickEvent` 仍在跑（客户端 tick 不受暂停影响），但拖拽只依赖鼠标事件，不依赖 tick，因此暂停不影响手感 |

#### 2.4.6 `farmstorm-client.toml` 的生成位置与生命周期

- 首次启动客户端时由 `ModConfig.Type.CLIENT` 的加载器创建：`run/config/farmstorm-client.toml`；专用服务器**不会**生成这一份（CLIENT 类型只在客户端加载）；
- 内容 = `[hud]` 段 3 键（§4.2）；
- 与 `farmstorm-server.toml` 的关系：**互不引用**。U 盘拷一份 server toml 给别人不会带走浮窗位置，反之亦然；
- 手工改 toml 后重启客户端生效（与 server 配置同一条规则：`ConfigValue#get()` 读的是加载后的值）。

### 2.5 核心所在区块的运行期临时加载（D-145）

#### 2.5.1 已核对的 1.21.1 ticket 形态

| 事实 | 证据（本地源码 jar） |
|---|---|
| `TicketType` 可**自建**：`public static <T> TicketType<T> create(String name, Comparator<T> comparator)` 与带 `int timeout` 的重载 | `net/minecraft/server/level/TicketType.java` |
| 1.21.1 **没有 ticket 持久化**：`net/minecraft/server/level/` 下只有 `TicketType` / `Ticket`，没有 `TicketStorage`；唯一的「强制加载落盘」是 `world/data/chunks.dat`（`ForcedChunksSavedData.FILE_ID = "chunks"`） | 逐一枚举该包下的类；`ForcedChunksSavedData.java` |
| 加/删票：`ServerChunkCache#addRegionTicket(TicketType<T>, ChunkPos, int distance, T value)` / `removeRegionTicket(同参)` | `ServerChunkCache.java` |
| 票 level = `33 − distance`（`ChunkLevel.byStatus(FullChunkStatus.FULL) − distance`） | `DistanceManager#addRegionTicket` |
| `level ≤ 31` = `ENTITY_TICKING`、`≤ 32` = `BLOCK_TICKING`、`≤ 33` = `FULL` | `ChunkLevel.fullStatus` / `isBlockTicking` |
| 方块实体 tick 的闸是 `Level#shouldTickBlocksAt` → `ServerLevel#shouldTickBlocksAt` → `inBlockTickingRange`（即 **level ≤ 32**） | `Level.tickBlockEntities` 里的 `else if (flag && this.shouldTickBlocksAt(...))`；`ServerLevel#shouldTickBlocksAt` |

#### 2.5.2 选择与等级（本文确定）

```java
public final class CoreChunkTickets {
    /** 本文确定：自有 ticket 类型，timeout = 0（不超时）；名字进日志，便于 /debug 与排查。 */
    private static final TicketType<ChunkPos> CORE = TicketType.create("farmstorm_core",
            Comparator.comparingLong(ChunkPos::toLong));
    /** 本文确定：distance = 1 ⇒ ticket level 32 = BLOCK_TICKING。 */
    private static final int DISTANCE = 1;

    public static void acquire(ServerLevel level, BlockPos core) {
        ChunkPos cp = new ChunkPos(core);
        level.getChunkSource().addRegionTicket(CORE, cp, DISTANCE, cp);
    }
    public static void release(ServerLevel level, BlockPos core) {
        ChunkPos cp = new ChunkPos(core);
        level.getChunkSource().removeRegionTicket(CORE, cp, DISTANCE, cp);
    }
}
```

**为什么是 distance = 1（本文确定，理由给完整）**：D-145 要「那 1 个区块」。`distance = 0` 会给 level 33（`FULL`）——区块被加载但**方块实体不 tick**，而核的边界粒子挂在 `serverTick`（`OutpostCoreBlockEntity:179-192`，由 `OutpostCoreBlock:58-67` 的 `getTicker` 注册）⇒ 必须 `level ≤ 32`。`distance = 1` 正好给 32：**方块实体 tick 的最小票**。`distance = 2` 给 31（`ENTITY_TICKING`），代价是把加载面从 3×3 扩到 5×5（等级每向外一圈 +1，31/32/33 三档才有意义），没有必要。

**加载面的诚实描述（本文确定 / A-43，且必须写进 29 号的边界清单）**：ticket 的等级会向外传播，每个区块 +1。`distance = 1` 时中心区块 = 32（block-ticking，**方块实体会 tick**），紧邻一圈 = 33（`FULL`：加载、但不 tick），再外一圈 = 34（不再加载）。所以「只加载 1 个区块」的准确说法是「**只有 1 个区块被 tick，另外 8 个被加载到 FULL 级**」；D-145 的「不是 3×3」在**tick 面**上成立，在**加载面**上是 3×3 —— 这是 ticket 机制的固有传播，不是实现选择。**A-43 因此规定：验收只写 tick 面，禁止写「只有 1 个区块被加载」。**

**A-38：D-145 的「不落盘」精确到什么文件**。D-145 原文写「**不写 `level.dat`**」，这个落点是错的——1.21.1 的强制加载与 `level.dat` 无关。本文按 A-38 把口径统一改成：**不写 `world/data/chunks.dat`**（`ForcedChunksSavedData`，`FILE_ID = "chunks"`，键 `Forced`；`ServerChunkCache` 的 data 目录按维度分，下界 / 末地各是 `world/DIM-1/data/chunks.dat` / `world/DIM1/data/chunks.dat`）。

**被否决的两条路径（都写这个文件，所以都不能用）**：

| 被否决的写法 | 它落到哪 | 否决理由 |
|---|---|---|
| `ServerLevel#setChunkForced(cx, cz, true)` | `getDataStorage().computeIfAbsent(ForcedChunksSavedData.factory(), "chunks")` ⇒ `<维度数据目录>/chunks.dat` | **持久化**：停服再开那个区块仍被强制加载，与 D-145 的「运行期临时」直接冲突；而且它是全局强制（`ChunkMap` 的 forced 集合），没有「随核释放」的成对语义 |
| NeoForge `TicketController#forceChunk(level, owner, cx, cz, add, ticking)` | 同样落到 `ForcedChunksSavedData::getBlockForcedChunks`（`TicketController.java` 的 `forceChunk` 第三个实参就是那个 tracker） | 理由同上：它是 NeoForge 给 `ForcedChunksSavedData` 套的一层「ticket 控制器」，名字像 ticket，落盘语义与 `setChunkForced` 一模一样 |

⇒ 只能用**裸 `TicketType` + `addRegionTicket`**：1.21.1 的 `server/level` 包里没有任何 ticket 持久化（2.5.1 第二行逐条枚举过），票是纯内存态，停服即消失，这与 D-145 的「运行期临时」是同一件事。**A-38 的落点就是本段与 §2.5.1 的第二行。**

#### 2.5.3 申请与释放（D-145 + RT-39）

| 时机 | 动作 | 挂点 |
|---|---|---|
| 放置核 | `acquire(level, pos)` | `OutpostCoreBlockEntity#setOwner`（`:95-103`）末尾 |
| 核被毁（真移除） | `release(level, pos)` | `OutpostCoreBlockEntity#release`（`:166-170`）里，与索引摘除同一处 |
| 迁移/清算分支 | 同「核被毁」 | `settleOnRemoval`（`:315-335`）末尾的 `this.release()` 已经覆盖 |
| 停服 | `releaseAll(server)` 遍历在线维度释放 | 新增 `ServerStoppingEvent` 监听（已核对存在），兜底 RT-39 |
| 启动补建 | 遍历 `OutpostCoreIndex` 的每条 `(dimension, pos)` → `acquire` | 与 §2.2.8 的补建同一段启动逻辑（先补票、后补绑定：补票只依赖坐标，不依赖区块加载） |
| 可观测性（A-43） | 订阅 `ChunkTicketLevelUpdatedEvent`（已核对：`getLevel()` / `getChunkPos()` / `getOldTicketLevel()` / `getNewTicketLevel()` / `getChunkHolder()`），只对本模组的核坐标打 DEBUG：`核心区块票 <dim> <chunk> old->new` | 新增调试监听；§7 #36 靠它证明「中心 32 / 邻居 33」的 tick 面 |

**为什么启动时要补票**：票是内存态，停服即失。若只在放置时申请，重启后所有老核的区块都不再被 tick —— 边界粒子与 `settleOnRemoval` 的判据都会静默失效（这是 RT-39 的另一半，事实卡只写了「泄漏」）。
**释放为什么必须幂等**：`release()` 的调用点有三条（`OutpostCoreBlock:101-107`、`settleOnRemoval:334`、停服兜底），`removeRegionTicket` 的参数必须与 `addRegionTicket` **完全一致**（同 type、同 pos、同 distance、同 value）；多删一次是无操作，少删一次就是泄漏。

### 2.6 man瓜 炮弹的近距离收敛（D-129）

#### 2.6.1 现状逐行分析与过冲成因

| 行 | 现状 | 后果 |
|---|---|---|
| `TurretBullet.java:28` | `public static final double SPEED = 0.6;`（硬编码） | 速度不可调；且**它有两个读者**（`:95` 与 `PumpkinTurret.java:75`，A-30 已补为裁决） |
| `:73-87` `tick()` | 服务端且 `target.isAlive()` → `steerTowards(target)`，然后 `super.tick()` | 每刻重设速度 |
| `:90-96` `steerTowards` | `delta = 目标碰撞箱中心 − 自身`；`delta.lengthSqr() < 1e-4` 时 **直接 return（保留上一刻速度）**；否则 `setDeltaMovement(delta.normalize().scale(SPEED))` | ★ 过冲的成因就在这两行 |
| `:98-119` `onHitEntity` | 伤害 + 进度 + 击退 + `discard()` | 命中判定完全依赖原版 `move()` 产生碰撞 |
| `:121-128` `onHitBlock` | `discard()`，不炸方块 | 撞墙即消失 |

**过冲的三步链条**（逐行）：
1. 当 `dist < SPEED`（贴脸，典型 0.3 格）时，`:95` 仍然给出**长度 0.6** 的速度 ⇒ 本刻位移 0.6 格，越过目标中心 0.3 格；
2. 下一 tick，`delta` 的方向翻了 180°，长度还是 0.6 ⇒ 又越过 0.3 格；于是炮弹在目标两侧各 0.3 格之间**每刻掉头一次**（这就是作者看到的「绕圈 / 抖动」）；
3. 更糟的是 `:92-94` 的早退：当位移把炮弹送到与目标中心几乎重合的位置（`lengthSqr < 1e-4`）时，它**不再改速度**，按上一刻的 0.6 格速度笔直飞走 —— 贴脸时既可能永远撞不上，也可能在某一刻偶然擦到碰撞箱。原版 `move()` 的碰撞检测是扫掠的（`Entity#move` 用 `getBoundingBox().expandTowards(delta)`），所以**只有「这一步的扫掠体与目标的碰撞箱相交」才算命中**；每刻掉头意味着扫掠体反复横跳，交错时不相交。

#### 2.6.2 修复（本文确定）

```java
/** 本文确定：命中判定的最小步长。小于它就认为「已到目标中心」，直接结算，不再指望 move() 的扫掠。 */
private static final double MIN_STEP = 0.05;
/** 本文确定：防止 :98-119 的结算体被两条路径各跑一次。 */
private boolean hitResolved;

private void steerTowards(Entity aim) {
    double step = FarmstormConfig.pumpkinTurretBulletSpeed.get();      // ← 速度改 config 的读取路径
    Vec3 delta = aim.getBoundingBox().getCenter().subtract(this.position());
    double dist = delta.length();
    if (dist <= MIN_STEP) {
        this.resolveHit(aim);                                          // 直接判中（不再等 move）
        return;
    }
    if (dist <= step) {
        this.setDeltaMovement(delta);                                   // 一步走完剩余距离：不越过、不抖动
        return;
    }
    this.setDeltaMovement(delta.normalize().scale(step));               // 正常距离：原行为不变
}

private void resolveHit(Entity hit) {
    if (this.hitResolved) return;
    this.hitResolved = true;
    applyHit(hit);                                                      // 从 :98-119 抽出的结算体
    this.discard();
}

@Override
protected void onHitEntity(EntityHitResult result) {
    super.onHitEntity(result);
    if (!(this.level() instanceof ServerLevel)) return;
    this.resolveHit(result.getEntity());                                // 同一条结算体，防双结算
}
```

**「按剩余距离收敛」与「直接判中」两条都做了，各有分工**（本文确定）：
- `MIN_STEP < dist ≤ step`：**收敛**（`setDeltaMovement(delta)`）。这一步的位移正好等于剩余距离，扫掠体必然覆盖目标中心，原版 `move()` 会给出 `EntityHitResult`，走正常的 `onHitEntity`；
- `dist ≤ MIN_STEP`：**直接判中**。此时位移已接近 0，`move()` 的扫掠体退化成一点，是否命中取决于目标碰撞箱是否恰好包含该点 —— 这不是可依赖的行为，必须直判。没有这一条，「贴脸」会退化成 2.6.1 的第 3 步。
- `hitResolved` 闸保证两条路径不会对同一发炮弹结算两次（`resolveHit` 里 `discard()` 之后原版 `move()` 仍可能在同一刻产生第二次命中回调）。

**与 `onHitEntity` 的衔接**：`applyHit(Entity)` 是 `:98-119` 的原样搬家（`hurt(void_bolt, projectileDamage)` → 玩家发 `turret_hit` 进度 → 击退 → 不 `discard()`，`discard` 移到 `resolveHit`），**伤害类型、进度 ID、击退算式一个字符都不改**。这样「撞到方块」仍是 `onHitBlock` 的 `discard()`，「撞到实体」与「贴脸直判」共用一份结算。

**速度改 config 后的两个读取点**（本文确定：都读同一个键，不给炮口初速另立键）：

| 位置 | 改法 |
|---|---|
| `TurretBullet.java:28` | 删常量；`:95` 改成 `FarmstormConfig.pumpkinTurretBulletSpeed.get()` |
| `behavior/impl/PumpkinTurret.java:75` | `bullet.setDeltaMovement(direction.scale(TurretBullet.SPEED))` → `…direction.scale(FarmstormConfig.pumpkinTurretBulletSpeed.get())`（**这一处事实卡 §3.2 第 7 行没点，A-30 已补为裁决**） |

**为什么不缓存成字段**：`ModConfigSpec.DoubleValue#get()` 读的是加载后的内存值，没有 IO；`TurretBullet:106` 对 `projectileDamage` 本来就是每发读一次（既有先例）。引入第三份快照反而多一个「重载后忘了 bake」的坑（否决）。

**不改客户端预测的原因**（D-129 补充 + HC-03）：追踪只在服务端（`TurretBullet.java:23-25` 的类注释），客户端拿到的是实体同步位置并做插值；`FarmstormEntityTypes.java:60-66` 的 `clientTrackingRange(4)` / `updateInterval(10)` 一个字不改。速度从 0.6 降到 0.45 会让「10 刻一次位置更新」在视觉上更平滑（每更新间隔位移从 6 格降到 4.5 格），不需要客户端侧的任何补偿。

### 2.7 朋友的韭 持续场的载体（D-131 ~ D-133）

#### 2.7.1 现状与三个坑

| 事实 | 行号 | 对本节的约束 |
|---|---|---|
| `settle()` → `onImpact()` → **`!keepAliveAfterImpact()` 才 `discard()`** | `ThrownCropEntity.java:95-104` | 重写 `keepAliveAfterImpact()` 返回 `true` 就能活下来 |
| 默认 `keepAliveAfterImpact()` 返回 `false`，唯一重写者是爆竹 | `:116-118` | 本版是第二个重写者 |
| 寿命检查只在 `!settled` 时生效 | `:65-72` | ★ **落地后没有任何机制回收它**：漏了自毁就是永久实体泄漏（RT-35） |
| `settled` 是 `private`，无访问器 | `:33`、`:99` | ★ 子类读不到「我落地了没」——必须自己记 |
| `onImpact` 每次生成恰好调用一次 | `:95-104` | 可以拿它当「落地那一刻」的锚点 |
| NBT 里 `Settled` / `Submunition` 会被存取 | `:145-157` | ★ **区块卸载再加载**后 `settled` 仍为 `true`，而子类的「落地刻数」若没持久化就会被重置 ⇒ 永久泄漏 |

#### 2.7.2 载体设计（本文确定）

```java
public class FriendChivesProjectile extends ThrownCropEntity {
    /** 本文确定：落地后的刻数；-1 = 还没落地。不写 NBT（D-133：区块卸载即结束）。 */
    private int fieldTick = -1;
    /** 本文确定：读档时若发现 NBT 里已经是 settled，就等第一个 tick 自杀。 */
    private boolean loadedSettled;
    /** A-33：场长取 config，代码里不出现第二个「200」。 */
    private int fieldLength;

    @Override protected boolean keepAliveAfterImpact() { return true; }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        this.fieldLength = FarmstormConfig.friendChivesEffectTicks.get();  // A-33：默认 200，值域 20 ~ 1200
        applyField(level, pos, 0);                 // 既有 :56-69 的球扫 + 施加，改成带 elapsed 参数
        playHitSoundOnce(level, pos, applied);     // 既有 :70-79 的播音逻辑，只在落地这一刻跑（D-132）
        this.setDeltaMovement(Vec3.ZERO);          // 落地置零速度（D-133）
        this.setNoGravity(true);                   // 构造器已设（:43-51），这里再显式一次防子类改动
        this.noPhysics = true;                     // 免碰撞：水流 / 活塞 / 实体推不动它（D-133）
        this.fieldTick = 0;
    }

    @Override
    public void tick() {
        super.tick();                                        // 基类的寿命检查因 settled 已失效（:65-72）
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.loadedSettled) { this.discard(); return; }  // 读档时已落地 ⇒ 本场在卸载时结束
        if (this.fieldTick < 0) return;                      // 还没落地
        this.fieldTick++;
        if (this.fieldTick >= this.fieldLength) { this.discard(); return; }  // 第 effectTicks 刻自毁
        applyField(level, this.position(), this.fieldTick);  // 每刻重扫一次球
    }

    /** 本文确定 + A-33：施加 (effectTicks − elapsed) 刻，amplifier 仍为 0（既有字面量不改）。 */
    private void applyField(ServerLevel level, Vec3 center, int elapsed) {
        int ticks = this.fieldLength - elapsed;              // elapsed = 0 时就是 effectTicks（默认 200）
        if (ticks <= 0) return;
        int radius = ...;                                    // friend_chives.hitRadius（默认 6.0，字面量 6 不变）
        for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(radius),
                c -> c.isAlive() && center.distanceTo(c.position()) <= radius)) {
            t.addEffect(new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadedSettled = tag.getBoolean("Settled");      // 基类写的键（:149）
    }
}
```

四条要点：

1. **`effectTicks − elapsed` 的算法**（A-33）：场长 `L = friend_chives.effectTicks`（默认 **200**，值域 **20 ~ 1200**）；`elapsed = 0`（落地那一刻）施加 `L`；之后每刻 +1，第 `k` 刻施加 `L − k`。**每刻写入的时长都比上一刻少 1 刻**，于是「第 `L` 刻之后效果自然结束」与「场结束时效果刚好走完」两件事同时成立。若写死 200，服主把键改成 100 时就会出现「场跑 200 刻、每人只拿 `100 − x`」的自相矛盾；若写死 200 的时长又会永久续期（D-131 补充已点明）。代码里因此**只有 `this.fieldLength` 一个来源**，`200` 这个字面量不出现第二次。
2. **落地后的物理收敛**：`setDeltaMovement(Vec3.ZERO)` + `setNoGravity(true)` + `noPhysics = true` 三件套（`noPhysics` 是 `Entity` 的既有字段，1.21.1 有效）。三者的分工：置零速度防「被上一刻的动量带走」、无重力防「缓慢下坠」、免碰撞防「水流 / 活塞 / 实体推走」。
3. **可见性**：`getDefaultItem()` 沿用基类（`ThrownCropEntity:52-56`，按 `profile().id()` 取 `farmstorm:friend_chives` 物品），渲染沿用 `ThrownItemRenderer`（`FarmstormClient:105-117` 已注册），因此**不新增模型、贴图、渲染器**，落地后就是一个悬在落点的「韭」物品形态（D-133）。
4. **区块卸载即结束**：靠 `loadedSettled` 在第一个 tick 自杀。**不能**只依赖「实体随区块一起消失」：1.21.1 的区块卸载会把实体**存进区块 NBT**，加载回来时 `settled` 仍是 true、`fieldTick` 却是 -1，于是它既不 tick 场也不自毁（`super.tick()` 的寿命检查被 `settled` 挡住）—— 那就是一个永不消失的实体。

#### 2.7.3 性能估算（RT-34）

| 量 | 估算 | 依据 |
|---|---|---|
| 每刻每场 | 1 次 `AABB` 粗筛（`inflate(6.0)` = 13×13×13 格）+ 对粗筛结果逐个 `distanceTo` | `getEntitiesOfClass` 内部按区块实体列表遍历，不做全图扫 |
| 场长 | `friend_chives.effectTicks`（默认 200 刻 = 10 秒，值域 20 ~ 1200 ⇒ 最长 60 秒） | A-33；`throwable.lifetimeTicks` 默认 100（`FarmstormConfig:328`）只约束**落地前**，落地后由本类的 `fieldLength` 自毁约束 |
| 同时在场的场数 | 每次投掷最多 1 场；场数上限 = 玩家投掷速率 × 场长 | 投掷物本身有冷却与寿命，场不会无限累积 |
| 最坏量级 | 10 场同时存在 ⇒ 每刻 10 次 AABB + 约 10×n 次距离比较（n = 场内实体数，通常 < 20） | 与既有的 `QbounceObserverSync` / 陷阱扫描同量级 |

**结论**：不需要空间索引、不需要隔刻扫描（D-131 明确「每刻」）。真正需要防的是「场数无上限」——那由 `fieldLength` 自毁与投掷物本身的冷却兜住；§7 #30 有一条「第 `effectTicks` 刻实体消失」的断言。

---

## 3. 改动面总表（对 0.1.0 的增 / 改 / 删，逐行）

表 **47 行**。「改后」列只写**目标状态**，不写实现步骤（实现在 §2）。风险号见 §6；`A-xx` 指 `.work/v011-verdicts.md` 的裁决（已立为裁决的条目在本表按裁决写，不在 §9 重复登记）。

| # | 类别 | 路径或类#方法 | 现值 | 改后 | 决策 | 风险 |
|---|---|---|---|---|---|---|
| 1 | 改 | `gradle.properties:11` | `mod_version=0.1.0` | `0.1.1` | D-124 | — |
| 2 | 改 | `检查环境.cmd:14`、`一键构建.cmd:10-12` | `farmstorm-0.0.1+mc1.21.1.jar`（及 `-sources.jar`） | `farmstorm-0.1.1+mc1.21.1.jar` / `-sources.jar` | D-147 | RT-38 |
| 3 | 改 | `assets/farmstorm/textures/item/wild_apple.png` | 16×16 / 571 B / 88 色 | **64×64 原图原样**（源图 6,946 B） | D-126 | RT-37 |
| 4 | 改 | `tools/gen_textures.py` 叠「+」机制（`:274-275`、`:407`、`:546-560`、`:612-623`、`:974-975`） | 25 张带「+」 | 机制删除（本版只重生成 `wild_apple` 1 张） | D-125 | RT-37 |
| 5 | 改 | `tools/gen_textures.py:154`、`:849-881`、`:884-895` | 桌面旧图 + `wild_apple_grid()` 的 16 网格众数取色 | 直读 64×64 源图、原样输出（`wild_apple` 走直通分支） | D-126 | RT-37 |
| 6 | **改** | **`tools/gen_textures.py:982-983`（`expected_size()` 恒返回 16×16）与 `:1151-1154`（`image.size != expected_size(spec)` → `SystemExit`）** | 尺寸断言只接受 16×16 | **两处一起放开**：`expected_size()` 对 `wild_apple` 返回源图实际尺寸，`:1153` 的断言随之成立 | D-126 / **A-32** | RT-37 |
| 7 | 改 | `registry/FarmstormBlocks.java:79` | `SoundType.GRASS` | `SoundType.STONE`（**5 类声音一起变**：破坏 / 放置 / 踩踏 / 挖掘 / 落地） | D-130 / **A-31** | — |
| 8 | 改 | `registry/FarmstormBlocks.java:82-89` | 无 `lightLevel` | `.lightLevel(state -> 15)` | D-141 | RT-36 |
| 9 | 改 | `config/FarmstormConfig.java:425-427` | `projectileDamage` 默认 1 | 默认 **3** | D-128 | RT-38 |
| 10 | 改 | `config/FarmstormConfig.java:431-433` | `fireIntervalTicks` 默认 60 | 默认 **40** | D-127 | RT-38 |
| 11 | 增 | `config/FarmstormConfig.java`（`pumpkin_turret` 组内、`:433` 之后） | — | `bulletSpeed`（double，`0.45`，值域 `0.0 ~ 4.0`） | D-129 | RT-33 |
| 12 | 删 | `entity/TurretBullet.java:28` | `SPEED = 0.6` | 常量删除 | D-129 | — |
| 13 | 改 | `entity/TurretBullet.java:90-96` | 每刻 `normalize × SPEED` | 近距收敛（`dist ≤ step` 收敛 / `dist ≤ 0.05` 直判） | D-129 | — |
| 14 | 改 | `entity/TurretBullet.java:98-119` | 结算写在 `onHitEntity` | 抽 `applyHit`，`onHitEntity` 与直判共用，加 `hitResolved` 闸 | D-129 | — |
| 15 | **改** | **`behavior/impl/PumpkinTurret.java:75`** | `bullet.setDeltaMovement(direction.scale(TurretBullet.SPEED))`（**`TurretBullet.SPEED` 的第二个读者**） | `direction.scale(FarmstormConfig.pumpkinTurretBulletSpeed.get())` | D-129 / **A-30** | — |
| 16 | 改 | `entity/thrown/FriendChivesProjectile.java` | 落地一次性、无 `tick()` | `keepAliveAfterImpact()=true` + `tick()` + 落定三件套 + 第 `effectTicks` 刻自毁 + `loadedSettled` | D-131 / D-133 / **A-33** | RT-34 / RT-35 |
| 17 | 改 | `entity/thrown/FriendChivesProjectile.java:56-80` | 施加 + 播音同刻；常量 200 隐含在 `effectTicks` 的默认值里 | 播音留 `onImpact`（只一次），施加迁 `tick()`，时长 **`effectTicks − elapsed`**（场长读 config，代码里不出现第二个 200） | D-131 / D-132 / **A-33** | RT-34 |
| 18 | 核 | `entity/ThrownCropEntity.java:95-118` | 既有钩子 | **不改**（本版只重写钩子，不新开生命周期） | D-131 | — |
| 19 | 改 | `farmland/FarmlandLedger.java:51` | 仅 `byOwner` | `bindings` + 派生 `byCore` + `orphans`（`LinkedHashMap`；落盘顺序仅诊断用，不参与判定） | D-134 / **A-47** | RT-32 |
| 20 | 删 | `farmland/FarmlandLedger.java:57-58, 72-79, 82-98` | `baseOverride` / `base()` / `setBase` / `personalCapacity` / `sumOverMembers` | 全删；`capacity = bonus × 己方核数` | D-139 | RT-40 |
| 21 | 改 | `farmland/FarmlandLedger.java:101-121` | `capacity` 遍历在线成员、`used` 取并集 | `capacity` 按 UUID 侧核数、`used` 数绑定条数 | D-139 / D-138 | RT-33 |
| 22 | 改 | `farmland/FarmlandLedger.java:228-264` | v1 `owners` 落盘 | v2 `dataVersion/bindings/orphans` + 兼容读 + `rebuildByCore` | D-137 | RT-32 |
| 23 | 增 | `farmland/FarmlandLedger.java`（新方法） | — | `bind` / `migrate` / `unbind` / `bindingsOf(CoreKey)` / `backfill(server)` / `orphanCount()`；补建按 `BlockPos` 单级全序处理 | D-134 / D-136 / D-137 / **A-47** | RT-32 |
| 24 | 改 | `farmland/FarmlandGuard.java:69-75` | 三态里只有范围 + 额度 | 加「选核 + 单核空位」，`QUOTA_FULL` 语义细化 | D-135 | RT-33 |
| 25 | 改 | `farmland/FarmlandGuard.java:92-107` | `inOwnRange` 遍历在线玩家 | 改绑定表 / 索引查（离线安全） | D-138 | — |
| 26 | 改 | `farmland/FarmlandGuard.java:120-134` | `protectsFromTrample` 遍历在线玩家 | 改「有绑定即受保护」 | D-138 | — |
| 27 | 改 | `blockentity/OutpostCoreBlockEntity.java:315-335` | 直接清算 | 先迁移；**迁移失败分两支：区块已加载 → 变泥土；区块未加载 → 只摘绑定、世界不动**；先算后写 | D-136 / **A-35** | RT-32 |
| 28 | 改 | `blockentity/OutpostCoreBlockEntity.java:353-379` | 两路在线遍历 + 3×3 | 只处理本核名下（`bindingsOf`） | D-136 | — |
| 29 | 改 | `blockentity/OutpostCoreBlockEntity.java:95-103` | `setOwner` 只写归属 + 索引 | 追加：申请 ticket、请求广播 | D-145 / D-142 | RT-39 |
| 30 | 改 | `blockentity/OutpostCoreBlockEntity.java:166-170` | `release` 只摘索引 | 追加：释放 ticket、请求广播 | D-145 / D-142 | RT-39 |
| 31 | 增 | `team/TeamResolver.java`、`team/ScoreboardTeamResolver.java` | 只有 `Owner × Player` | 加 `Owner × Owner` 重载，`Player` 版委托 | D-138 | — |
| 32 | 增 / 改 | `outpost/OutpostCoreIndex.java` | `ownerNames` / `count` 为 `private`，`isOwnerOrAlly` 吃 `Player`；`Entry` 只有 `(dimension, pos)` | 加 `entriesOf(...)` / `owners()` / `count(server, uuid)` 只读访问器；**`Entry` 增加 `long gameTime` 并落盘**、`record(...)` 增加 `gameTime` 参数（A-39 的比较键载体，见 §2.2.10） | D-142 / D-138 / **A-39** | RT-31 |
| 33 | 删 | `command/FarmstormCommands.java` | 整文件 58 行 | 删除 | D-140 | RT-40 |
| 34 | 删 | `event/FarmstormGameEvents.java:4`、`:62`、`:71-73` | 3 处指令注册 | 删除 | D-140 | RT-40 |
| 35 | 增 | `event/FarmstormGameEvents.java` | 0 个浮窗相关监听 | 加 6 个：登录 / 登出 / 换维度 / 重生 / `ServerTickEvent.Post` / `ServerStoppingEvent` | D-142 / D-145 | RT-31 / RT-39 |
| 36 | 增 | `network/CoreListPayload.java`、`network/CoreListBroadcaster.java`、`client/network/ClientCoreListHandler.java` | 0 个自定义包 | 1 个 S2C payload + 组装 + handler | D-142 | RT-31 |
| 37 | 增 | `config/FarmstormClientConfig.java` + `Farmstorm.java:56` 后一行 | 无 CLIENT config | `ModConfig.Type.CLIENT` 3 键 | D-144 | — |
| 38 | 增 | `client/FarmstormKeys.java`、`CoreHudRenderer`、`CoreHudDragScreen`、`CoreHudClientCommands`、`ClientCoreList` | `client` 21 类 | +5 类；浮窗满员 **9 行 / 93 px**（8 核心 + 1 溢出） | D-143 / D-144 / **A-34** | — |
| 39 | 增 | `outpost/CoreChunkTickets.java` | 无 ticket | 自有 `TicketType` + `distance = 1` | D-145 | RT-39 |
| 40 | 改 | `datagen/ModLangProvider.java:393-394, 404-406`（删）+ 新增 10 条 + 类注释 `:15`、`:27-32` 的键数叙述 | 315 键 | 323 键（§4.3） | D-127 ~ D-144 | — |
| 41 | 核 | `Farmstorm.java:124-146` 注册面自检 | 断言 93 / 12 类 | **不改**（+0，断言仍然成立） | — | — |
| 42 | 核 | `entity/ThrownCropEntity.java`（`keepAliveAfterImpact` 的既有默认） | 默认 `false` | **不改**（只在子类重写） | D-131 | — |
| 43 | **改** | `tools/verify_project.py:222-237` 的期望来源：`expected_config_keys` / `_v002` / `_v003` / `_v010` 四个函数 + `:244` 的并集 | 期望集 = `01-DESIGN §3.8` + `12-TECH §8.1` + `18-TECH §8.1` + `24-TECH §8.1` 四份**旧文档**的并集 | 增加对本版口径的解析（新的 `expected_config_keys_v011(...)`，表形与本文 §4.1 的 58 组一致），并把它并入期望集；同时把已删的 `farmland.limitPerPlayer` 从并集里**剔除**。判据仍是「期望集 == toml 键集」 | **A-40** | RT-40 |
| 44 | 核 | `tools/verify_project.py` 的其余三段（93 个注册 ID / lang 键集一致 / 禁令扫描） | 现有断言 | **不改**：注册面仍 93（§1.2），lang 段读产物（323 × 2 会自动对上），禁令扫描与 D-146 的红线一致 | A-40 | RT-40 |
| 45 | 删（补记） | `config/FarmstormConfig.java:904-908`（`push` / `comment` / `translation` / `defineInRange` / `pop`）+ 字段声明 `:226`；`run/config/farmstorm-server.toml` | `[farmland]` 段与 `limitPerPlayer` 键存在（默认 0，范围 0 ~ 4096） | **整组删除**：`farmstorm-server.toml` 的 `[farmland]` 段随之消失（组 59 → 58；键 166 − 1 + 1 = 166） | D-139 / **A-42**（补记行，动作本体见 §2.3 第 3 项） | RT-40 |
| 46 | 改（补记） | `farmland/FarmlandLedger.java:128-158`（`add` / `remove` 两个写入口） | 只维护 `byOwner` 的坐标集合；`remove(BlockPos)` 按坐标跨 owner 摘除 | 增「核绑定参数」（记账变成 `bind(owner, coreKey, pos)`）与「`byCore` 同步维护」（每个写入口都要同时改 `bindings` 与反查索引） | D-134 / D-135 / **A-55** | RT-32 |
| 47 | 改（补记） | `farmland/FarmlandLedger.java:203-219`（惰性校验 `validated`） | 只丢弃 `byOwner` 里「已不是 farmland」的坐标 | 命中后**同时剪 `byCore` 与绑定表**（只清一侧会让幽灵耕地占住每核 16 格的名额） | D-134 / **A-55** | RT-32（幽灵耕地） |

**动作面 47 = 44（原）+ 1（A-42：删 `farmland` 整组）+ 2（A-55：记账写入口 + 惰性校验）**：第 45 行是 **A-42** 的补记（动作本体见 §2.3 第 3 项；第 45 行供 31 号表 A 与 §4.1 交叉引用）；第 46 / 47 行是 **A-55** 的补记——`FarmlandLedger.java:128-158`（`add` / `remove` 写入口）与 `:203-219`（`validated` 惰性校验）在原 44 行里**一行都没有**，而 F-188 的「涉及文件」点了这两段，且绑定制把 `remove` 从单入口变成多入口、`byCore` 反查必须与 `bindings` 同写同剪。**1 ~ 44 不重编号**，补记行与已有行判据一致（第 45 行 ≡ §2.3 第 3 项；第 46 / 47 行与第 19、23 行是同一文件的不同方法，不重复）。

---

## 4. config 全表

### 4.1 server（计入 166）

**计数方法**：读 `config/FarmstormConfig.java` 的静态块（`:268-928`），用 `push`/`pop` 维护组栈、数 `.define(...)` / `.defineInRange(...)` / `.defineInList(...)`；共 **166 键 / 59 组**（§1.4）。**段计数口径（A-52）**：统计 toml 的段必须用 `^\s*\[`——嵌套段是**缩进**的（`\t[blast_carrot.nausea]`），用 `^\[` 只能数到 **35** 个顶层段，永远凑不出 58。本表是**改后**的 58 组：`farmland` 组整组消失（−1 组），`pumpkin_turret` 组 4 → 5 键（+1 键），其余 57 组只列键名与条数。

**本表 58 行（数据行），键数合计 166**。「标记」列：`改` = 默认值变了；`新` = 本版新增；空 = 不动。

| # | 组 | 键数 | 键（逐键点名） | 标记 |
|---|---|---|---|---|
| 1 | `converter` | 3 | `xpCost` `netherStarForwardCost` `netherStarOnReverse` | |
| 2 | `stack` | 2 | `throwable` `other` | |
| 3 | `deploy` | 3 | `turretMiningTicks` `supportMustBeFullOpaque` `dropOnSupportLost` | |
| 4 | `teams` | 2 | `resolver` `untamedFallback` | |
| 5 | `explosion` | 2 | `sliceResistThreshold` `grenadeResistThreshold` | |
| 6 | `throwable` | 1 | `lifetimeTicks` | |
| 7 | `food` | 1 | `eatTicks` | |
| 8 | `puffed_wheat` | 2 | `dryEdge` `wetEdge` | |
| 9 | `blast_carrot` | 3 | `triggerRadius` `explosionPower` `maxDamage` | |
| 10 | `blast_carrot.nausea` | 2 | `level` `durationTicks` | |
| 11 | `ubi_server` | 2 | `explosionPower` `maxDamage` | |
| 12 | `toxic_ubi_server` | 4 | `explosionPower` `maxDamage` `cloudRadius` `cloudDurationTicks` | |
| 13 | `melon_slice_bomb` | 2 | `explosionPower` `maxDamage` | |
| 14 | `melon_grenade` | 6 | `spawnCount` `spawnOffsetY` `horizontalJitter` `explosionPower` `maxDamage` `submunitionSpeed` | |
| 15 | `pumpkin_turret` | **5** | `targetRadius` `projectileDamage`（**改** 1 → 3）`knockbackLevel` `fireIntervalTicks`（**改** 60 → 40）`bulletSpeed`（**新** 0.45） | 改 / 改 / **新** |
| 16 | `awakened_torchflower` | 4 | `auraRadius` `refreshTicks` `fireAspectLevel` `particlesPerRefresh` | |
| 17 | `awakened_torchflower.strength` | 2 | `level` `durationTicks` | |
| 18 | `awakened_torchflower.resistance` | 2 | `level` `durationTicks` | |
| 19 | `awakened_torchflower.haste` | 2 | `level` `durationTicks` | |
| 20 | `awakened_torchflower.speed` | 2 | `level` `durationTicks` | |
| 21 | `awakened_torchflower.water_breathing` | 2 | `level` `durationTicks` | |
| 22 | `awakened_torchflower.night_vision` | 2 | `level` `durationTicks` | |
| 23 | `awakened_pitcher_plant` | 4 | `auraRadius` `refreshTicks` `clearHarmful` `particlesPerRefresh` | |
| 24 | `awakened_pitcher_plant.regeneration` | 2 | `level` `durationTicks` | |
| 25 | `awakened_pitcher_plant.saturation` | 2 | `level` `durationTicks` | |
| 26 | `awakened_pitcher_plant.absorption` | 2 | `level` `durationTicks` | |
| 27 | `fang_trap_cactus` | 9 | `triggerRadius` `outerFangCount` `outerDelayTicks` `outerRadius` `innerFangCount` `innerDelayTicks` `innerRadius` `slownessDurationTicks` `slownessLevel` | |
| 28 | `bamboo_firecracker` | 2 | `spawnIntervalTicks` `durationTicks` | |
| 29 | `red_mushroom_puff` | 1 | `radius` | |
| 30 | `red_mushroom_puff.nausea` | 2 | `level` `durationTicks` | |
| 31 | `red_mushroom_puff.blindness` | 2 | `level` `durationTicks` | |
| 32 | `brown_mushroom_puff` | 1 | `radius` | |
| 33 | `brown_mushroom_puff.weakness` | 2 | `level` `durationTicks` | |
| 34 | `brown_mushroom_puff.slowness` | 2 | `level` `durationTicks` | |
| 35 | `lucky_berry` | 6 | `nutrition` `saturationModifier` `randomCropCount` `lootChestCount` `smallExplosionPower` `creativeDurationTicks` | |
| 36 | `lucky_berry.regeneration` | 2 | `level` `durationTicks` | |
| 37 | `lucky_berry.strength` | 2 | `level` `durationTicks` | |
| 38 | `lucky_berry.resistance` | 2 | `level` `durationTicks` | |
| 39 | `lucky_berry.jump_boost` | 2 | `level` `durationTicks` | |
| 40 | `lucky_berry.speed` | 2 | `level` `durationTicks` | |
| 41 | `lucky_berry.chance` | 8 | `lightning` `random_crops` `regeneration` `loot_chest` `small_explosion` `charged_creeper` `godlike_buff` `creative_mode` | |
| 42 | `power_beet` | 2 | `nutrition` `saturationModifier` | |
| 43 | `power_beet.strength` | 2 | `level` `durationTicks` | |
| 44 | `power_beet.speed` | 2 | `level` `durationTicks` | |
| 45 | `power_beet.haste` | 2 | `level` `durationTicks` | |
| 46 | `power_beet.resistance` | 2 | `level` `durationTicks` | |
| 47 | `molotov` | 3 | `effectRadius` `fireChance` `triggerExplosionPower` | |
| 48 | `wild_apple` | 2 | `nutrition` `saturationModifier` | |
| 49 | `wild_hunter` | 11 | `durationTicks` `killExtensionTicks` `killHealPoints` `searchRadius` `searchAngleDegrees` `dashTicks` `speedBonusPercent` `attackSpeedBonusPercent` `attackDamageBonusPercent` `highlightRadius` `loopSoundRange` | |
| 50 | `zangief_cane` | 8 | `triggerRadius` `stageDelayTicks` `riseHeightBlocks` `riseTicks` `spinDelayTicks` `spinDegreesPerTick` `slamDamage` `cancelFallDamage` | |
| 51 | `qbounce` | 4 | `poseMode` `rotateDegreesPerTick` `squashAmplitude` `squashPeriodTicks` | |
| 52 | `friend_chives` | 3 | `hitRadius` `effectTicks` `soundRange` | |
| 53 | `point_defense_fungus` | 3 | `interceptRadius` `maxIntercepts` `interceptParticles` | |
| 54 | `vibration_bean` | 2 | `triggerRadius` `confusionTicks` | |
| 55 | `growth` | 2 | `minSeconds` `maxSeconds` | |
| 56 | `trap` | 3 | `immunityTicks` `dustIntervalTicks` `maxTrapsPerPlayer` | |
| 57 | `outpost_core` | 3 | `farmlandBonus` `maxPerPlayer` `boundaryParticleIntervalTicks` | |
| 58 | `compressed` | 1 | `replaceOnlyAirAndFluid` | |
| | **合计** | **166** | **58 组** | 改 2 / 新 1 / 删 1 |

**算式与核对**（三个方向都要对上）：

1. 改后 = 改前 166 − 1（删 `farmland.limitPerPlayer`）+ 1（增 `pumpkin_turret.bulletSpeed`）= **166**；
2. 组数 = 改前 59 − 1（`farmland` 整组消失）+ 0（新增键落在既有的 `pumpkin_turret` 组里）= **58**；
3. 本表 58 行键数逐行相加 = **166**（`pumpkin_turret` 按 5 计）。

**被删的那一组单独登记**（不在本表 58 行内）：

| 组 | 键 | 类型 | 默认 | 范围 | 动作 |
|---|---|---|---|---|---|
| `farmland` | `limitPerPlayer` | int | 0 | 0 ~ 4096 | **删**（`FarmstormConfig.java:904-908` + 字段 `:226`；`farmstorm-server.toml` 的 `[farmland]` 段随之消失） |

**新增键的三个属性**（本文确定，值只取事实卡 §3.4）：

| 键 | 类型 | 默认 | 值域（本文确定） | 理由 |
|---|---|---|---|---|
| `pumpkin_turret.bulletSpeed` | double | **0.45** | `0.0 ~ 4.0` | 默认值来自裁定 #7（0.45 格/刻 = 9 格/秒）；值域与既有的 `melon_grenade.submunitionSpeed`（`0.0 ~ 4.0`，`:418`）同档——同为「格/刻」语义的速度键，复用既有量纲比另开一套好 |

**升级须知（D-147，必须写进交付文档）**：① 已落盘的 `farmstorm-server.toml` 里 `fireIntervalTicks` 仍是 60、`projectileDamage` 仍是 1，要生效得删旧 toml 或手改这两键；② `bulletSpeed` 会由配置系统自动补进旧 toml，**不需要手加**；③ `[farmland]` 段在旧 toml 里不会被自动删除（配置系统只补不删），它变成一个「无人读取的段」，建议手工删掉（不影响运行）。

### 4.2 client（3 键，**不计入 166**）

| 键 | 类型 | 默认 | 值域（本文确定） | 用途 | 决策 |
|---|---|---|---|---|---|
| `hud.enabled` | bool | **true** | — | 浮窗开关；按键 H 与 `/farmstorm hud on\|off` 都写它 | D-144 |
| `hud.x` | int | **4** | `0 ~ 3840` | 浮窗左上角 GUI 像素 x；绘制时按 `guiWidth − 面板宽` 二次钳制 | D-144 |
| `hud.y` | int | **4** | `0 ~ 2160` | 浮窗左上角 GUI 像素 y；绘制时按 `guiHeight − 面板高` 二次钳制 | D-144 |

- 落盘文件：`<gameDir>/config/farmstorm-client.toml`（§2.4.2），**单独一份**，不与 server 混写；
- 小计：**3 键 / 1 组（`hud`）**；不计入 §4.1 的 166，也不计入 58 组；
- 三个键**都有** `translation`（本文确定），因此语言键 +3：`farmstorm.configuration.hud.enabled` / `.x` / `.y`。理由：既有 166 个 server 键全部有翻译，配置界面按 key 显示；只给 server 键翻译会让客户端配置页露出裸键名（§9 第 1 条把「是否要加」这条条件句定死为「加」）。

### 4.3 语言键增删（逐条点名 + 自己数出总数）

**改前：315 键（中英键集相同，§1.4）。**

删除 2 条（`datagen/ModLangProvider.java` 里各一段，删完 datagen 重跑即从产物里消失）：

| # | 键 | 现状位置 | 现文案 |
|---|---|---|---|
| 1 | `farmstorm.configuration.farmland.limitPerPlayer` | `zh_cn.json:145`；生成源 `ModLangProvider.java:393-394` | 耕地额度的服务端默认值（0 = 开局谁也耕不了地） |
| 2 | `command.farmstorm.farmlandlimit.set` | `zh_cn.json:64`；生成源 `ModLangProvider.java:404-406` | 耕地额度已设为 %s（本进程内有效，重启回 config 默认） |

新增 **10** 条（逐条给键名、来源与用途；本次新增的 10 条**全部**由本文逐条点名，`k` 不再留空）：

| # | 键 | 用途 | 决策 |
|---|---|---|---|
| 1 | `farmstorm.configuration.pumpkin_turret.bulletSpeed` | 新 config 键的翻译（与其余 165 个 config 键同源同形） | D-129 |
| 2 | `farmstorm.configuration.hud.enabled` | CLIENT config 键 1 | D-144 |
| 3 | `farmstorm.configuration.hud.x` | CLIENT config 键 2 | D-144 |
| 4 | `farmstorm.configuration.hud.y` | CLIENT config 键 3 | D-144 |
| 5 | `key.farmstorm.core_hud.toggle` | 开关浮窗的按键描述（H） | D-144 |
| 6 | `key.farmstorm.core_hud.drag` | 进入拖拽模式的按键描述（J） | D-144 |
| 7 | `command.farmstorm.hud.on` | 客户端指令反馈：已开启 | D-144 |
| 8 | `command.farmstorm.hud.off` | 客户端指令反馈：已关闭 | D-144 |
| 9 | `command.farmstorm.hud.reset` | 客户端指令反馈：位置已复位 | D-144 |
| 10 | `gui.farmstorm.core_hud.self` | 浮窗里「自己」的标识（队友显示真实名字，不需要键） | D-143 |

**算式**：`315 − 2 + 10 = 323`。**改后：323 键 × 2（中英键集完全相同）**。

按前缀复核（改后，相加必须等于 323）：

```
farmstorm.configuration   166 − 1 + 1 + 3 = 169
advancements.farmstorm     36
item.farmstorm             32
block.farmstorm            26
tooltip.farmstorm          16
entity.farmstorm           13
gui.farmstorm               8 + 1 = 9
death.attack                6
effect.farmstorm            4
subtitles.farmstorm         3
key.farmstorm               2      ← 新增的按键前缀
message.farmstorm           2
command.farmstorm           1 − 1 + 3 = 3
container.farmstorm         1
itemGroup.farmstorm         1
--------------------------------
169+36+32+26+16+13+9+6+4+3+2+2+3+1+1 = 323
```

> 注：`farmland.limitPerPlayer` 的删除让 `farmstorm.configuration` 从 166 → 165，再加 `bulletSpeed` 与 3 个 `hud.*` 回到 169 —— 与 §4.1 的「166 个 server 键 + 3 个 client 键」恰好一致（169 = 166 + 3）。这条等式是「config 键 ↔ 翻译键」的强校验，§7 #5 拿它做断言。

---

## 5. 资源清单

| 类别 | 新增 | 就地替换 | 说明 |
|---|---|---|---|
| PNG | **0** | **1**：`assets/farmstorm/textures/item/wild_apple.png`（16×16 / 571 B → **64×64**，源图 6,946 B / 1814 去重 RGBA / 1471 全透明） | D-126；模型 `models/item/wild_apple.json` 的 `layer0` 不变，**不新增第二张贴图** |
| JSON（模型 / 方块状态 / 战利品 / 配方 / 进度） | **0** | **0** | 本版不动任何 JSON 资源 |
| `sounds.json` | **0** | **0** | **本文定为不补 `attenuation_distance`**，理由见下 |
| ogg | **0** | **0** | `sounds/friend_chives.ogg` 83,932 B / 48 kHz mono / 10.000 s 不动 |
| 语言文件 | **0**（文件数不变） | **2**：`zh_cn.json` / `en_us.json` 由 datagen 重生成（315 → 323 键） | §4.3 |

**`sounds.json` 是否补 `attenuation_distance`：结论是「不补」（本文确定，已用源码验证，不再是待验证项）**

- 现状：`sounds.json:16-22` 的 `friend_chives` 未设该键 ⇒ 走 `Sound` 的默认值 **16**；
- 播放侧：`FriendChivesProjectile.java:74-76` 的音量 = `friend_chives.soundRange / 16 = 2.0`；
- 客户端的实际衰减半径公式（`SoundEngine.java` 逐字核对）：

  ```java
  float f  = soundInstance.getVolume();                                    // 2.0
  float f1 = Math.max(f, 1.0F) * (float) sound.getAttenuationDistance();   // max(2,1) × 16 = 32
  ```

  即 **`可闻半径 = max(音量, 1) × attenuation_distance`**，两个量是**相乘**关系，不是相加。
- 所以：**现状已经就是 32 格**（`2.0 × 16 = 32`），补 `"attenuation_distance": 32` 会把它变成 `2.0 × 32 = 64` 格 —— 与 D-132 的「32 格可闻」**不符**，且会让 `friend_chives.soundRange` 这个 config 键的语义从「格」变成「格的一半」（改一个键跳两个量）。
- 该键在 `sounds.json:6` 的先例（`wild_hunter_loop` 用 32）是给**音量 1.0** 的音源用的（1.0 × 32 = 32），不能照抄。
- ⇒ 资源面「0 新增 JSON」成立；28 号 §7 第 5 条移交的这条**已结**，见 §9 第 2 条。

---

## 6. 风险册 RT-31 ~ RT-41

字段：风险 / 触发条件 / 影响面 / 缓释 / 怎么验证（**验证一律指 §7 的验收编号**；F-xx 任务号由 31 号发，本文不预支）。

### RT-31 · 首次引入自定义网络包（S2C）

- **风险**：codec、注册、断线重连、跨维度、推送时机（登录 / 放置 / 摧毁 / 换维度）任一处漏掉，浮窗就显示错数据或干脆不显示。
- **触发条件**：注册漏了 `playToClient`（包发不出去）、handler 里做了客户端判定（越权）、`total` 与 `cores` 语义混用（`+N` 算错）、登出没清快照（换服残留）。
- **影响面**：客户端显示（不影响世界状态、不影响服务端权威）。
- **缓释**：payload 只读不写（客户端不回包，`playToServer` 与 `sendToServer` 全仓 0 命中）；组装全在服务端；`total` 与 `cores` 分成两个字段；`ClientPlayerNetworkEvent.LoggingOut` 清快照；10 刻去抖 + 100 刻指纹对账。
- **验证**：#6（只有一个包、客户端不回包、专用服务器能起服）、#7（单人 1 核 → 浮窗 1 行）、#8（跨维度 2 行）、#9（行内容与排序）、#10（9 行 = 93 px，8 核心 + 1 溢出）、#29（换服不残留）。

### RT-32 · 耕地-核绑定表重构（存档格式升版）

- **风险**：`farmstorm_farmland.dat` 从 v1 升 v2，老档读到空账本；补建把老田铲了；迁移算错把不该变泥土的田变成泥土。
- **触发条件**：`load` 没判 `dataVersion`；`orphans` 落盘丢了；迁移的候选判据少了「有空位」；边遍历边改账本。
- **影响面**：**世界方块**（唯一会动世界的一条：迁移失败分支的 `setBlock(DIRT)`）+ 玩家已开垦的田。
- **缓释**：v1 全部读成 `orphans`（不猜绑定）；补建整段无 `setBlock`；迁移先算完再动世界（沿用 `:312-313` 的既有理由）；`orphans` 落盘让补建幂等。
- **验证**：#16（v2 落盘字段）、#18（老档补建、补不上的不变泥土、两次启动 N/M 相同）、#19（迁移成功）、#20（迁移失败且区块已加载 → 变泥土）、#21（A-35：迁移失败且区块未加载 → 只摘绑定、世界不动）。

### RT-33 · 一键两义：`outpost_core.farmlandBonus` 同时是容量与额度基数

- **风险**：改这个键会同时改「每核容量」与「队伍额度」，服主只改一处以为只影响一处。
- **触发条件**：文档没写清；`verify_project` 的 166 校验不受影响（键数没变），所以没人会因此报警。
- **影响面**：玩法数值（容量/额度）+ 配置文档。
- **缓释**：在 `FarmstormConfig.java:911` 的 `comment(...)` 里写明双义（§2.2.6）；29 号的数值表必须把「容量 / 额度」两行并列；`QUOTA_FULL` 的提示语义改为「最近的核满了」（§9 第 4 条）。
- **验证**：#17（一核 16 格、第 17 格被拒；`farmlandBonus` 改成 2 后第 3 格被拒且额度同步变 2）。

### RT-34 · 朋友的韭每刻扫球

- **风险**：`effectTicks` 刻 × N 实体 × 每场 = 每刻一次 AABB；场数无上限时会线性叠加。
- **触发条件**：多场重叠、场内实体多（刷怪塔附近）、有人狂投。
- **影响面**：服务端 tick 时间（不是崩溃级）。
- **缓释**：只扫 `LivingEntity`；`AABB` 粗筛 + `distanceTo` 复核（沿用 `:60-62` 的二次筛）；场由 `effectTicks` 自毁收敛；`hitRadius` 上限 16（config 既有值域）。
- **验证**：#30（时序：`effectTicks − x` 递减；改成 100 后第 100 刻消失）、#31（只播一次音）。

### RT-35 · 落地实体存活 `effectTicks` 刻且可见

- **风险**：受重力 / 水流 / 活塞推动；区块卸载后**永久泄漏**（`settled` 挡住寿命检查，`fieldTick` 未持久化）。
- **触发条件**：落点在水里 / 活塞旁；玩家离开后区块卸载再回来。
- **影响面**：实体泄漏（世界文件膨胀、tick 时间），以及「场已结束但实体还在」。
- **缓释**：`setDeltaMovement(ZERO)` + `setNoGravity(true)` + `noPhysics = true`；`readAdditionalSaveData` 读 `Settled` → 首刻自杀（区块卸载即结束，D-133）；`effectTicks` 刻自毁。
- **验证**：#30（第 `effectTicks` 刻消失）、#32（区块卸载即结束、回来不残留）。

### RT-36 · 据点核心亮度 15

- **风险**：强光传播 15 格、该位置永不刷怪（`lightLevel` 影响刷怪判据）。
- **触发条件**：把核放在刷怪平台 / 农场中间。
- **影响面**：玩法（光照 / 刷怪）+ 性能（光照更新）。
- **缓释**：写进 29 号的「已知影响」；不把亮度做成 config（D-141 补充）。
- **验证**：#33（F3 的 `Light` 值 = 15、夜晚有光斑）。

### RT-37 · 64×64 物品贴图

- **风险**：物品栏里显示面积仍是 16×16，高出 4 倍的细节被显示尺寸平均掉；`gen_textures.py` 重跑会把图换回 16×16。
- **触发条件**：重跑 datagen / 生成器；或期待「物品栏里更大」。
- **影响面**：视觉；不影响模型与注册面。
- **缓释**：生成器默认源图与 `add_plus` 同版改掉（§3 第 4、5 行）；**尺寸断言 `:982-983` 与 `:1151-1154` 必须一起放开**（A-32），否则 64×64 输出会 `SystemExit`；29 号写明「显示面积不变」。
- **验证**：#23（PNG 是 64×64、重跑生成器后仍是 64×64、物品栏不破图）。

### RT-38 · man瓜 两个默认值对已落盘 toml 无效

- **风险**：老服务器升级后仍是 60 刻 / 1 点伤害，作者以为改动没生效。
- **触发条件**：`run/config/farmstorm-server.toml` 已存在。
- **影响面**：玩法数值的「实际生效值」。
- **缓释**：代码**不做迁移**（D-147 有意为之）；交付文档单列升级须知（§4.1 末）；`bulletSpeed` 由配置系统自动补。
- **验证**：#3（删旧 toml 后默认值 = 40 / 3 / 0.45）、#4（不删旧 toml 时仍是旧值，且 `bulletSpeed` 被自动补进）。

### RT-39 · 区块 ticket 泄漏

- **风险**：核被毁没释放 ⇒ 区块永远被加载；或停服后票没清干净。
- **触发条件**：`release()` 走了但参数不一致（`removeRegionTicket` 静默失败）；启动补票漏了老核；停服路径没兜底。
- **影响面**：内存 / 世界加载面（不是存档损坏）。
- **缓释**：释放点与 `release()` 同处；`ServerStoppingEvent` 兜底 `releaseAll`；启停补票都从 `OutpostCoreIndex` 读坐标（不依赖区块加载）；票是内存态（1.21.1 无 ticket 持久化）。
- **验证**：#36（拆核后区块可卸载）、#36（`chunks.dat` 里没有核坐标 ⇒ 没写盘）。

### RT-40 · 删 `/farmstorm` 指令树的连带

- **风险**：`baseOverride` / 2 条语言键 / `ModLangProvider` 的注释 / 既有文档里的指令说明，漏一处就是双源。
- **触发条件**：只删指令类、不删 config 键；只删语言键、不删生成源。
- **影响面**：编译（`base()` 无读者后仍有写者）与文档一致性。
- **缓释**：§2.3 的五处删除 + §3 的 47 行总表；`FarmlandLedger` 的 base 三件套一次删净；`ModLangProvider` 的两段与类注释同改（§9 第 7 条）。
- **验证**：#12（服务端无 `/farmstorm`、客户端只有 `hud`）、#5（语言键 323 且无 `farmlandlimit`）。

### RT-41 · 破红线后既有文档仍写「0 网络包」

- **风险**：26 号 HC-73、22 号 §1.1/§6 第 4 条、README 红线段与现状冲突，实现者按旧口径复查会把新包判成违规。
- **触发条件**：任何人拿 0.1.0 的文档当红线复跑。
- **影响面**：文档一致性（不影响代码）。
- **缓释**：28 号 §4.5 已列出要改的六份；本文 §0 与 §2.1 用新口径；32 号写 HC-77 时显式声明「取代 0.1.0 的 HC-73 网络包半条」。
- **验证**：#37（全仓 `@Mixin` 0 命中、`CustomPacketPayload` 恰好 1 个文件、verify_project 的 93/93）、#6。

---

## 7. 验收自检清单

**38 条（#1 ~ #38）**。每条 = 一个具体动作 + **一个含具体数值 / 键名 / 路径的断言** + 观察位置 + 依据。禁写「表现正常」。

**三条纪律**：① 本表不写「某方法的调用点数 = N」这类判据（A-37：绑定制引入「摘绑定」后写入口不再唯一，这类断言实现后必红且无法判断是缺陷还是预期）；② 凡是「全仓只有一处」的表述，本表一律换成可数的**文件数 / 键数 / 命中数**（例如「`CustomPacketPayload` 恰 1 个文件」），不换成「唯一调用点」；③ **A-40**：`tools/verify_project.py` 未按本版口径改完之前，它的「FAIL 0」不是通过判据（见 #38）。

| # | 断言（含具体数值 / 键名 / 路径） | 观察位置 | 依据 |
|---|---|---|---|
| 1 | 启动日志逐字含「自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK」 | `run/logs/latest.log` | §1.2、D-146 |
| 2 | `run/config/farmstorm-server.toml` 里 `[farmland]` 段**不存在**；段（`^\s*\[`，嵌套段是缩进的，见 §4.1）行数 = **58**；`pumpkin_turret` 段恰 **5** 键且 `fireIntervalTicks = 40`、`projectileDamage = 3`、`bulletSpeed = 0.45`、`targetRadius = 15`、`knockbackLevel = 1` | `run/config/farmstorm-server.toml` | §4.1、D-127 ~ D-139 |
| 3 | 删掉旧 `farmstorm-server.toml` 后首次起服：`bulletSpeed` 出现在 `[pumpkin_turret]` 段、值为 `0.45`；`fireIntervalTicks` 为 `40`、`projectileDamage` 为 `3` | 同上 | §4.1、D-147、RT-38 |
| 4 | 保留旧 toml（0.1.0 产物）直接升级：`fireIntervalTicks` 仍为 `60`、`projectileDamage` 仍为 `1`，但 `bulletSpeed` 被自动补成 `0.45` | 同上 | D-147、RT-38 |
| 5 | `src/generated/resources/assets/farmstorm/lang/zh_cn.json` 与 `en_us.json` 各 **323** 键、键集完全相同；`grep -c farmlandlimit` 在两份文件里 **0**；`grep -c '"farmstorm.configuration.'` 恰 **169** | 两个 lang JSON；`tools/verify_project.py` 的 §3 段 | §4.3、RT-40 |
| 6 | 全仓 `grep -rln "CustomPacketPayload" src/main/java` 恰 **1** 个文件（`network/CoreListPayload.java`）；`grep -rn "playToServer\|sendToServer" src/main/java` **0 命中**；起一个**专用服务器**能正常完成启动（无 `NoClassDefFoundError`） | `src/main/java` + 专用服务器 `latest.log` | §2.1、D-142、RT-31 |
| 7 | 单人放置 1 个核后，浮窗出现 **1 行**，形如 `#1 [overworld] (x, y, z) 自己`；`latest.log` 有一条「核心列表下发：<玩家> 1 条（total=1）」 | 游戏内浮窗 + `latest.log` | §2.1.4、D-143 |
| 8 | 主世界放 1 个核、下界放 1 个核，站在任一维度：浮窗 **2 行**，行首维度标识分别为 `overworld` 与 `the_nether`；传送后不变 | 游戏内浮窗 | D-143、RT-31 |
| 9 | 排序确定性与可复现（A-39）：① 先放 A（t=100）、再放 B（t=200）⇒ 浮窗 A 在上；② **离线把索引里两个核的 `gameTime` 改成同一个值**（编辑 `world/data/farmstorm_outpost_core.dat`；两核在不同维度、坐标完全相同）⇒ 重启后按**维度 id 升序**定序（`minecraft:overworld` 在 `the_nether` 前）；③ 同样改 `gameTime`、同维度、坐标不同 ⇒ 按 `BlockPos#compareTo`（y → z → x）定序；④ **同一份存档重启两次，日志里的「核心比较键指纹」两行完全相同**（`核心比较键指纹 <hex16>（N 条）`） | 游戏内浮窗 + 存档 NBT + `latest.log` 的两条指纹行 | A-39、§2.1.5、§2.2.10、D-143 |
| 10 | 构造 9 个己方核：浮窗画 **8 行核心 + 1 行 `+1` = 9 行**，面板总高 **93 px**（8 个核时 8 行、83 px、无 `+N`） | 游戏内浮窗（截图量面板高度） | D-143、A-34、§2.4.4 |
| 11 | 按 **H** 关掉浮窗：`config/farmstorm-client.toml` 里 `[hud] enabled = false`；重启客户端后浮窗仍不显示；再按 H 恢复 | `config/farmstorm-client.toml` + 游戏内 | D-144、§2.4.2 |
| 12 | 专用服务器控制台执行 `/farmstorm`：返回「未知的指令」；单人客户端 `/farmstorm` 的补全只有 `hud`（`on` / `off` / `reset`）；`/farmstorm farmlandlimit 5` 在两种环境都失败 | 服务器控制台 + 单人聊天栏 | D-140、§2.3、RT-40 |
| 13 | `/farmstorm hud off` → `enabled = false`；`/farmstorm hud on` → `true`；`/farmstorm hud reset` → `x = 4`、`y = 4`，且 `enabled` 不变 | `config/farmstorm-client.toml` | D-144、§2.4.3 |
| 14 | 按 **J** 打开拖拽：单人**暂停**（画面静止、世界不 tick）；把浮窗拖到画面右下角后 ESC：`hud.x` / `hud.y` 变成落点坐标；重进游戏位置不变 | 游戏内 + `config/farmstorm-client.toml` | D-144、§2.4.5 |
| 15 | 把浮窗拖到屏幕外松手：`hud.x ≤ guiWidth − 面板宽`、`hud.y ≤ guiHeight − 面板高`，面板完整可见（越界部分为 0 px） | 游戏内（拖到四个角各试一次） | §2.4.4、§2.4.5 |
| 16 | 锄 1 格后看存档：`world/data/farmstorm_farmland.dat` 含 `dataVersion: 2`、`bindings` 里 1 条，键为 `pos` / `owner` / `coreDimension` / `corePos`，其中 `coreDimension = "minecraft:overworld"` | 存档 NBT（离线用 NBT 查看器读） | §2.2.2、D-134、RT-32 |
| 17 | 同一个核名下锄第 **17** 格：被拒并提示「耕地额度已满：16/16」；`bindings` 里该核仍只有 **16** 条。把 `outpost_core.farmlandBonus` 改成 **2** 后重启：第 **3** 格即被拒，且队伍容量同步变为 2 | 游戏内动作栏 + 存档 NBT + toml | D-134、§2.2.6、RT-33 |
| 18 | 三级比较键（A-39）：① 相距 40 格的两个己方核之间、3×3 交界处锄 1 格 ⇒ 绑给**较近**那个（`bindings.corePos` = 近的核坐标）；② 把两核距离改成相同 ⇒ 绑给 `placedGameTime` 更早的那个；③ 同样等距、且离线把两个核的 `gameTime` 改成同一个值 ⇒ 绑给**坐标字典序在前**的那个（`BlockPos#compareTo`）；④ 同一份存档重启两次，日志「耕地绑定指纹」两行完全相同。**注**：第二级（维度 id）在绑定面上不可能触发——候选核必须覆盖同一块耕地 ⇒ 必同维度；它只在 #9 的跨维度浮窗排序里起作用 | 存档 NBT + `latest.log` 的两条指纹行 | D-135、A-39、§2.2.7、§2.2.10 |
| 19 | 用 0.1.0 的 `farmstorm_farmland.dat`（只有 `owners` 段、无 `dataVersion`）启动：日志「耕地补建：绑定 N 块 / 保持未绑定 M 块」；**同一份存档连跑两次，N 与 M 完全相同**（A-47：按 `BlockPos#compareTo` 单级全序处理，与 `HashMap` 迭代顺序无关）；**补不上的那些方块仍是 `minecraft:farmland`**（逐个 `execute if block` 验证，0 个变泥土） | `latest.log` + `/execute if block … minecraft:farmland` | D-137、A-47、§2.2.8、RT-32 |
| 20 | 核 A 名下 3 块田、核 B 覆盖其中 2 块：拆 A 后，那 2 块的 `bindings.corePos` 变成 B、第 3 块变 `minecraft:dirt`；B 名下总数 ≤ 16 | 存档 NBT + 游戏内方块 | D-136、§2.2.9、RT-32 |
| 21 | 核 A 名下 1 块田、周围无接替核、**该区块在内存里**（人就站在田边）：拆 A 后该田变 `minecraft:dirt`，且**没有**掉落物 | 游戏内方块 | D-136、§2.2.9、RT-32 |
| 22 | 核 A 名下 1 块田、周围无接替核、**该区块未加载**（先走远 200 格让区块卸载，再用 `/execute in <dim> run setblock <核坐标> air` 拆核）：该田**仍是 `minecraft:farmland`**（世界一个方块都没动），`bindings` 里那条被摘除；日志里出现「迁移失败且区块未加载：只摘绑定」（A-35） | 游戏内方块 + 存档 NBT + `latest.log` | D-136、A-35、§2.2.9、RT-32 |
| 23 | `textures/item/wild_apple.png` 是 **64×64**（`identify` 或 PIL 读尺寸）；重跑 `python -X utf8 tools/gen_textures.py` 之后**仍是 64×64**、不报「尺寸断言失败」（`expected_size()` 与 `:1153` 已按 A-32 放开）；物品栏里显示不破图 | PNG 文件 + 生成器输出 + 游戏内物品栏 | D-126、A-32、RT-37 |
| 24 | 压缩干草块**破坏**：音色与 `/playsound minecraft:block.stone.break block @s` 一致，与 `minecraft:block.grass.break` 明显不同 | 游戏内（正 / 负对照各播一次） | D-130、A-31、§3 第 7 行 |
| 25 | 压缩干草块**放置**：音色与 `/playsound minecraft:block.stone.place block @s` 一致，与 `minecraft:block.grass.place` 不同 | 同上 | D-130、A-31 |
| 26 | 压缩干草块**踩踏**（走上去）：音色与 `/playsound minecraft:block.stone.step block @s` 一致，与 `minecraft:block.grass.step` 不同 | 同上 | D-130、A-31 |
| 27 | 压缩干草块**挖掘**（按住左键、不挖掉）：打击音与 `/playsound minecraft:block.stone.hit block @s` 一致，与 `minecraft:block.grass.hit` 不同 | 同上 | D-130、A-31 |
| 28 | 压缩干草块**落地**（从 3 格高落到方块上）：落地音与 `/playsound minecraft:block.stone.fall block @s` 一致，与 `minecraft:block.grass.fall` 不同 | 同上 | D-130、A-31 |
| 29 | 从服务器 X 退出、进服务器 Y（Y 上没有任何核）：浮窗**立即消失**（不是保留 X 的列表）；再回 X：列表重新出现 | 游戏内浮窗 | §2.1.4、RT-31 |
| 30 | 朋友的韭落地后（默认 `friend_chives.effectTicks = 200`）：站进球内的生物每刻刷新的 Q弹剩余时长逐刻递减（第 1 刻 200、第 2 刻 199…第 199 刻 1）；第 **200** 刻实体消失（`/data get entity @e[type=farmstorm:friend_chives]` 查不到）。把 `effectTicks` 改成 **100** 后重启重测：递减止于 100、第 **100** 刻消失（证明 200 不是常量） | 游戏内 + `/data get` + toml | D-131 / D-133、A-33、§2.7、RT-34 |
| 31 | 空投（球内无生物）不播 `friend_chives`；命中 ≥1 个生物只播**一次**（`latest.log` 里「命中音」恰 1 条，整场 10 秒内不重复） | `latest.log` + 听觉 | D-132、§2.7 |
| 32 | 场存在时走出 200 格强制卸载再回来：场已结束（实体不存在）、没有残留；重进世界也不复活 | 游戏内 + `latest.log` | D-133、RT-35 |
| 33 | 据点核心所在方块在 F3 的 `Light` 行显示 `15`；夜晚在核旁 15 格内可见光斑，15 格外无光 | F3 + 游戏内 | D-141、RT-36 |
| 34 | man瓜 三连：`fireIntervalTicks = 40` 时两次开火的时间戳差 ≈ 2000 ms；一发命中造成 **3** 点（1.5 颗心）伤害；`bulletSpeed = 0.45` 时 15 格飞行 ≈ **33 刻**（1.65 s） | `latest.log` 时间戳 + 目标血量 | D-127 ~ D-129、RT-38 |
| 35 | 把目标站到炮台 **1 格内**：连续 5 发全部命中（目标掉 15 点血），炮弹不再绕圈 / 抖动；目标在 3 格外时行为与改前一致 | 游戏内（`/summon` 一个 20 血生物计数） | D-129、A-30、§2.6 |
| 36 | 票的 **tick 面**（A-43）：① 放置核后，`ChunkTicketLevelUpdatedEvent` 的调试日志显示**核心所在区块 `newTicketLevel = 32`**（tick 集合内，方块实体照常 tick——边界粒子每 40 刻一圈），**紧邻一圈 `= 33`（border 级，不在 tick 集合）**：把一块 `farmland` 放进紧邻区块，200 刻后它的 `moisture` 不变；② 拆核后同一位置不再有粒子；③ `world/data/chunks.dat` 的 `Forced` 数组**不含**该核的 `ChunkPos.toLong()`。**本条的断言只写 tick 面**：加载面必然是 3×3（§2.5.2），**禁止**把「只有 1 个区块被加载」写成断言 | `ChunkTicketLevelUpdatedEvent` 日志 + 游戏内 + `chunks.dat` | D-145、A-38、A-43、§2.5、RT-39 |
| 37 | 全仓 `grep -rn "@Mixin\|mixins.json" src/main/java` **0 命中**；`grep -rln "CustomPacketPayload" src/main/java` 恰 **1** 个文件；`python -X utf8 tools/verify_project.py` 的注册面段输出「已核 registry 对象: 93 / 93」 | `src/main/java` + 工具输出 | D-146、§1.2、RT-41 |
| 38 | 在 `tools/verify_project.py` 按 **A-40** 改完之前（`:222-237` 增加「本文 §4.1」的解析、并从并集里剔除 `farmland.limitPerPlayer`），任何验收记录**不得**把该工具的「结论：FAIL 0 项」当通过判据；改完后跑一次必须输出「结论：FAIL 0 项」、退出码 0、config 段显示 **166** 键 | 工具输出 + 验收记录 | A-40、§3 第 43 行、RT-40 |

> **覆盖对照**：注册面 93 → #1 / #37；config 166 + 3 → #2 / #3 / #4；语言键 323 → #5；网络包只有 1 个且客户端不回包 → #6 / #37；绑定表 → #16 / #17 / #18；迁移（含 A-35 的降级分支）→ #20 / #21 / #22；老存档补建（A-47 的 `BlockPos` 单级全序可复现）→ #19；贴图 64×64 与生成器尺寸断言（A-32）→ #23；音效 **5 类**（A-31）→ #24 ~ #28；浮窗满员 9 行 / 93 px（A-34）→ #10；浮窗跨维度 → #8；拖拽持久化 → #14 / #15；换服不残留 → #29；朋友的韭 时序（A-33）→ #30 / #31；区块卸载结束 → #32；亮度 15 → #33；man瓜 三项 → #34；man瓜 近距离（A-30）→ #35；ticket 释放 → #36；三级比较键与可复现（A-39）→ #9 / #18；票不落盘（A-38）→ #36；版式分解（A-41）→ #10；工具口径（A-40）→ #38；票的 tick 面（A-43）→ #36；踩踏面收口（A-45）→ §2.2.5；耕地跨维度撞车（A-49）→ §9 第 5 条（已知限制，不进断言）。

---

## 8. 待验证 API 清单

**8 条**。「已经核过签名」与「仍需运行时确认」分开写：签名都在 §2 标了来源（本地源码 jar），本表只留**运行时行为与语义**。每条给「不确定时的退路」，实现时先写最小用例跑通再写正式逻辑。

| # | 待验证的 API / 语义 | 为什么不确定 | 何时确认 | 不确定时的退路 |
|---|---|---|---|---|
| ① | `PayloadRegistrar#playToClient` / `StreamCodec` / `PacketDistributor#sendToPlayer` 的**运行时**行为：握手版本串 `"1"` 是否影响连接、单机（内存连接）是否仍走序列化、专用服务器是否会去加载客户端 handler 类 | 签名已核对，但本仓此前 **0 个自定义包**，没有任何运行记录；`RegisterPayloadHandlersEvent` 的 javadoc 只说「payloads are always serialized, including memory connections」 | 网络包落地第一刻（§7 #6 / #7） | 若单机不序列化导致 handler 不触发 ⇒ 把「单机也发」改成「`Dist.CLIENT` 侧在 `ClientPlayerNetworkEvent.LoggingIn` 后主动请求一次」（需要新增一个 `playToServer` 请求包——**那会变成 2 个包**，必须回 28 号登记，不许自行加） |
| ② | `RegisterClientCommandsEvent` 的**注册形态**：客户端派发器与服务器下发的指令树如何合并、`sendSuccess(..., false)` 在客户端指令里的回显位置（聊天栏 / 动作栏） | 事件类已核对（game 总线、仅客户端、`getDispatcher()` / `getBuildContext()` 都在），但本仓 **0 个客户端指令**；合并逻辑在 `ClientPacketListener`，未逐行读 | §7 #12 / #13 | 若客户端指令被服务器树覆盖 ⇒ 改用「按键 + CLIENT config」两条载体（D-144 的 1、3 项已足够，指令只是便利） |
| ③ | `TicketType` 的可用形态与非持久 ticket 的加法：`TicketType.create(String, Comparator)` 自建类型是否被 `DistanceManager` 完全接受；`removeRegionTicket` 是否要求四个参数完全一致；`distance = 1` 的实际加载面是否就是 3×3（§2.5.2） | `TicketType` / `ServerChunkCache` / `ChunkLevel` 的源码已核对，但**本仓 0 个 ticket**；NeoForge 另外还有一套 `TicketController`（走 `ForcedChunksSavedData`，本版**不用**） | ticket 落地第一刻（§7 #36） | 若自建类型被拒 ⇒ 退回 `TicketType.PORTAL`（`BlockPos` value、`timeout = 300`）并在每次 `serverTick` 重续（**会改变 §2.5 的释放语义**，必须回 28 号登记）；若 `distance = 1` 不 tick 方块实体 ⇒ 改 `distance = 2`（加载面 5×5，§9 会同步改口） |
| ④ | `sounds.json` 的 `attenuation_distance` 与 `playSound` 的 volume 是否叠加 | **已确定：相乘**（`SoundEngine` 里 `Math.max(volume, 1.0F) * attenuationDistance`），因此 32 格已由 `2.0 × 16` 达成，**不补该键**。剩下的不确定只是「`SoundSource.PLAYERS` 的客户端音量滑条是否进一步缩放 `calculateVolume`」 | §7 #31（听觉 + 日志音量） | 若实测 32 格听不见 ⇒ 调 `friend_chives.soundRange`（config 键，默认 32.0，值域 0 ~ 64），**不改 `sounds.json`**（改它会同时改 165 个键之外的一个资源文件，且与「0 新增 JSON」冲突） |
| ⑤ | 客户端 `KeyMapping` 与 `RegisterKeyMappingsEvent` 的注册时机：mod 总线 + `Dist.CLIENT` 是否足够早（`Options` 加载后、按键界面打开前）；默认键 **H / J** 是否与其它模组撞键；Screen 打开时 `consumeClick()` 是否仍计数 | 事件与 `register(KeyMapping)` 已核对（实现就是 `ArrayUtils.add(options.keyMappings, key)`），但本仓 **0 个按键**；撞键取决于玩家装了什么模组 | §7 #11 / #14 | 若注册太晚（按键界面里看不到）⇒ 把注册挪到 `FMLClientSetupEvent#enqueueWork`；若 H/J 撞键 ⇒ 只改默认键码，不动语言键与设计 |
| ⑥ | 原版自定义包的单包大小上限具体数值 | `ClientboundCustomPayloadPacket` 的 `STREAM_CODEC` 已核对，但上限常量没有逐字找到；本文只用到 3.1 KB 量级 | §7 #10（9 行满员时仍正常） | 若上限低于预期（不可能低于 3 KB）⇒ 把 `MAX_CORES` 从 64 降到 32（`total` 仍能算对 `+N`） |
| ⑦ | 拖拽 `Screen` 的鼠标坐标口径：`mouseClicked` / `mouseDragged` 的 `(mx,my)` 是否已是 **GUI 缩放后**的坐标（与 `hud.x` / `hud.y` 同一坐标系） | `Screen` 的鼠标方法来自 `GuiEventListener` 默认实现，本仓没有可用先例（`ConverterScreen:86` 只有 `render`，没有鼠标逻辑） | §7 #14 / #15 | 若不是同一坐标系 ⇒ 在 `mouseClicked` 里先做一次 `mx * guiWidth / windowWidth` 换算（换算系数用 `Minecraft#getWindow` 的宽高） |
| ⑧ | `ClientPlayerNetworkEvent.LoggingOut` 在「单人退出到主菜单」与「被踢出」两种路径下是否都触发（清空快照的兜底） | 事件类已核对（`LoggingIn` / `LoggingOut` / `Clone` 三个），但本仓 0 使用；单人退出的路径未逐行读 | §7 #29 | 若不触发 ⇒ 在 `ClientTickEvent.Post` 里判 `Minecraft#level == null` 时清空（多一次每刻判空，成本可忽略） |

---

## 9. 与上游的差异登记

**本节只登记，不自行改上游**（28 号 / 29 号 / 事实卡 / `tools/verify_project.py` 一律不动）。逐条给「上游怎么写 → 源码/验证是什么 → 本文怎么处理」。

**排除项**：`.work/v011-verdicts.md` 里**已立为裁决的 A-30 ~ A-49 不在本表重复登记**——它们已在 §2 / §3 / §7 按裁决写死。**唯一例外是 A-49**：它的落点就是本表第 5 条（已知限制），因为它不产生任何动作（不改 §3 改动面、不改 §4 config、不改 §7 验收）。本表其余条目是**本文写作期间新发现的、上游还没裁的**；其中第 3、5 条按作者要求写成「实现注意 / 已知限制」，不进验收断言。

| # | 上游位置 | 上游的说法 | 实测 / 推导 | 本文的处理 |
|---|---|---|---|---|
| 1 | 事实卡 §3.5、28 号 §3.4 | 语言键算式 `315 − 2 + 1 + 3 + 1 + k`，其中 `k` = 客户端指令反馈条数（候选 3），最终值「由 30 号写死」 | 算式**漏了 2 条按键描述键**：D-144 的 2 个 `KeyMapping` 各需一条 `key.farmstorm.*` 翻译，否则按键界面显示裸键名。加上这 2 条后：`315 − 2 + 1 + 3 + 1 + 3 + 2 = 323` | 本文定死 **323**（§4.3 逐条点名 10 条新增）；`k = 3` 定死；漏的 2 条按键键登记在此，**请 29 / 32 号按 323 引用** |
| 2 | 事实卡 §3.3、28 号 §7 第 5 条 | `sounds.json` 是否给 `friend_chives` 补 `"attenuation_distance": 32`「属技术面，移交 30 号核」 | 客户端的可闻半径 = `max(音量, 1) × attenuation_distance`（`SoundEngine` 逐字），两个量**相乘**。现状 `2.0 × 16 = 32` 已经是 32 格；补 32 会变 **64** 格 | 本文定为**不补**（§5），资源面「0 新增 JSON」成立。移交条已结 |
| 3 | 事实卡 §1.3、`FarmlandLedger:203-219` | 账本的惰性校验只清理 `byOwner` 里「已不是耕地」的坐标 | 绑定表化之后，`bindings` 与派生的 `byCore` 会同时漂移：已消失的耕地若只从 `bindings` 里清、不从 `byCore` 里清，容量会被**幽灵耕地**占住，出现「核明明没满却提示已满」 | 本文确定：惰性清理必须**同时剪 `byCore`**（§2.2.4 的 `setDirty` 表）；这条不写成验收判据（A-37 的纪律），只作为实现要求 |
| 4 | 28 号 D-135 的「补充」 | 「`Verdict.QUOTA_FULL` 的提示语言键是否改名由 30 号定（不改键代价更小）」 | 绑定制之后 `QUOTA_FULL` 的含义从「队伍额度用满」变成「范围内最近的己方核已满」；但既有文案「耕地额度已满：%s/%s」在两种语义下都成立（`%s/%s` = 已用/上限） | 本文定为**不改键**（`message.farmstorm.farmland.quota_full` 与 `.no_core` 都保留）；语义变化登记在此，29 号的边界清单要写「提示里的上限是队伍容量，不是单个核的容量」 |
| 5 | 事实卡 §1.3、`FarmlandLedger:46-51` | 账本是「玩家 UUID → 坐标集合」，落盘 `farmstorm_farmland.dat`，**没有维度字段**（0.1.0 起如此） | 两条后果：① 下界 `(0,64,0)` 的耕地会「借」到主世界同坐标的**绑定保护**（`protectsFromTrample(level, pos)` 只查坐标）；② **额度计数**把两个维度的同坐标地算成一块 | **本版不修（A-49），列已知限制**。三条理由照抄：**plan 未要求**；**老档 NBT 只有 `pos`，无法回溯维度**，补维度必须给默认值或懒回填，会引入新的**不可复现面**；耕地侧本版**已叠一次存档升版（v1→v2）**，再叠一次风险大于收益。本版只在绑定记录里保留 `coreDimension`（那是**核**的维度），不由此反推耕地维度 |
| 6 | 事实卡 §0 的「Java 文件数」 | 「由 30 号自己点名数出并写进文档」 | 本文数出 **125 个文件 / 15,785 行**（`src/main/java`，`ReadAllLines` 逐文件计数）；`client` 包 **21** 类（20 顶层 + `client/render/TurretBulletRenderer`） | 写进 §1.1；与事实卡 §1.5 的「21 个类」一致，无冲突 |
| 7 | 事实卡 §3.2 第 26 行 | 「`ModLangProvider.java`：315 键 → 删 2 加 N」 | 该类的**类注释**（`:15`、`:27-32`）把 315 与各段键数写成叙述（「299 + 16 = 315」），删/增键后注释会与产物不符 | 本文确定：改键必须**同改注释**（§3 第 40 行的「改」范围包含注释）；事实卡只点了键、没点注释 |
| 8 | 事实卡 §1.2、`OutpostCoreIndex` | `ownerNames`（`:55-56`）与 `count`（`:162`）被 D-142 / D-138 依赖 | 两者都是 `private`；`isOwnerOrAlly`（`:99-113`）虽然存在但吃 `Player`（离线不安全），本版**不用它** | 本文确定：给索引加三个只读访问器（§3 第 32 行），不复活 `isOwnerOrAlly`（保持它无调用者的现状） |

---

## 附录 A　本文确定的参数（逐条给理由）

| # | 参数 | 取值 | 理由 | 章节 |
|---|---|---|---|---|
| 1 | payload 包名 / 类名 | `com.dashpig.farmstorm.network.CoreListPayload` | 与既有分包风格一致（`outpost/`、`farmland/`、`command/`）；`CoreList` 直接描述内容 | §2.1.2 |
| 2 | payload id | `farmstorm:core_list` | 命名空间必须是本模组（不能用 `CustomPacketPayload.createType`，那会落进 `minecraft:`） | §2.1.2 |
| 3 | 握手版本串 | `"1"` | 首个包；codec 一改必须跟着改，从 1 起便于对照 | §2.1.2 |
| 4 | 单包核数上限 | `MAX_CORES = 64` | 每人 2 核 × 32 人 = 64，覆盖常见服务器；超出用 `total` 表达 | §2.1.6 |
| 5 | 发送去抖 | `MIN_INTERVAL_TICKS = 10` | 同刻连锁事件合并；0.5 秒仍在「立即」区间 | §2.1.3 |
| 6 | 对账周期 | `RECONCILE_INTERVAL_TICKS = 100` | 兜「无事件的队友变化」；5 秒延迟可接受，成本可忽略 | §2.1.4 |
| 7 | 客户端 handler 线程处理 | `context.enqueueWork(...)` | 注册器默认已包 `MainThreadPayloadHandler`；写在调用点更抗未来改动 | §2.1.2 |
| 8 | 排序键（三级） | `gameTime` → `dimension.toString()` → `BlockPos#compareTo`（y → z → x） | A-39 定稿的三级；第三级只在「同刻 + 同维度 + 同坐标」才用到（物理上不可能） | §2.1.5 |
| 9 | 绑定表 NBT 键名 | `dataVersion` / `bindings` / `orphans`；记录内 `pos` / `owner` / `coreDimension` / `corePos` | 与 `OutpostCoreIndex` 的 `owners` / `entries` / `dimension` / `pos` 同风格 | §2.2.2 |
| 10 | 格式版本 | `2`（缺失 = 1） | 缺失即 v1，是唯一能兼容 0.1.0 成品档的判法 | §2.2.3 |
| 11 | 绑定距离口径 | `BlockPos#distSqr`（三维欧氏平方） | 3×3 判定已是切比雪夫；距离再用切比雪夫会在对角方向大量并列 | §2.2.7 |
| 12 | 迁移的候选顺序 | `distSqr` → `placedGameTime` → 坐标字典序（A-39） | 与锄地绑核**同一条**，避免「绑给近的、迁给别的」 | §2.2.9 |
| 13 | 补建执行时机 | 首个 `ServerTickEvent.Post` 里跑一次 | 构造器阶段 `MinecraftServer` 未就绪；一次性布尔闸保证只跑一次 | §2.2.8 |
| 14 | `orphans` 是否落盘 | **落盘** | 不落盘 ⇒ 补建不可重试，「保持不动」退化成「永久遗忘」 | §2.2.2 |
| 15 | ticket 类型名 / 距离 | `"farmstorm_core"` / `distance = 1`（level 32） | 名字进日志便于排查；32 是「方块实体 tick」的最小等级 | §2.5.2 |
| 16 | ticket 释放点 | `release()` + 停服 `releaseAll` | 三条释放路径共用一处；停服兜底防 RT-39 | §2.5.3 |
| 17 | 炮弹命中最小步长 | `MIN_STEP = 0.05` | 小于它时 `move()` 的扫掠体退化成点，命中不可依赖 | §2.6.2 |
| 18 | 炮弹速度值域 | `0.0 ~ 4.0` | 与 `melon_grenade.submunitionSpeed` 同量纲同档 | §4.1 |
| 19 | 韭场自毁刻 | 第 **`effectTicks`** 刻（`fieldTick >= fieldLength` 即 `discard()`） | 口径由裁决 **A-33** 定（场长读 config）；本文确定的是「代码里只留 `fieldLength` 一个来源，不出现第二个 200」 | §2.7.2 |
| 20 | 韭场物理收敛 | `Vec3.ZERO` + `setNoGravity(true)` + `noPhysics = true` | 三件套分别防「动量带走 / 下坠 / 被推」 | §2.7.2 |
| 21 | 读档已落地 | `readAdditionalSaveData` 读 `Settled` → 首刻自杀 | 基类 `Settled` 会挡住寿命检查，不自杀就是永久泄漏 | §2.7.2 |
| 22 | 2 个按键默认键 | H（开关）/ J（拖拽） | 1.21.1 原版默认键位表里两者都未占用 | §2.4.1 |
| 23 | 按键分类 | 复用 `KeyMapping.CATEGORY_GAMEPLAY` | 自建分类要多 1 条语言键，与 323 的账对不上 | §2.4.1 |
| 24 | CLIENT config 值域 | `x: 0 ~ 3840`、`y: 0 ~ 2160` | 4K GUI 像素上界；真正的边界在绘制时按屏幕二次钳制 | §4.2 |
| 25 | `hud reset` 的语义 | 只复位位置（`x/y = 4/4`），不动 `enabled` | D-144 只给动作名；「复位」在浮窗语境下指位置 | §2.4.3 |
| 26 | 指令反馈是否广播 | `sendSuccess(..., false)` | 本地开关没有让其他玩家知道的必要（与已删的服务端指令相反） | §2.4.3 |
| 27 | 浮窗版式 | 行高 **9 px** / 行距 **1 px** / 内边距 **2 px**（A-41，与 29 号 §5.6 同口径）/ 面板高 `10n + 3`（**8 行 83 px / 9 行 93 px**，等价推论）/ 背景 `0x80000000` / 边框 `0xFF000000` / 自己 `0xFF55FF55` / 队友 `0xFFFFFFFF` / 行格式 `#%d [%s] (%d, %d, %d) %s` / `+N` 纯数字拼接 | **A-41 定的分解**（行高 9 + 行距 1 + 内边距 2）；配色取 D-143 与事实卡表下注；`+N` 不新增语言键 | §2.4.4 |
| 28 | 浮窗绘制钳制 | 绘制前钳到 `[0, guiWidth − 宽] × [0, guiHeight − 高]` | 窗口拉伸 / GUI 缩放后旧坐标可能越界 | §2.4.4 |
| 29 | `hideGui` 与拖拽态 | 拖拽态无视 `hideGui` 与 `screen != null` | 编辑态必须看得见自己在拖什么（D-144 补充的技术必然） | §2.4.4 |
| 30 | 踩踏保护的判据 | **有绑定即受保护**（A-45） | 踩踏事件没有查询者；绑定存在就是己方关系的证据。**只适用于踩踏面**：额度闸（锄地）与浮窗仍按**实时**己方判定（换队后立即变化），29 号 B-18 已按此收窄 | §2.2.5、§2.5 |
| 31 | `placedGameTime` 的比较载体 | **索引侧落盘**：`OutpostCoreIndex.Entry.gameTime`（不是读方块实体） | A-39 要求「不为比较而加载候选核所在区块」；索引是 `SavedData`，读取不需要区块，BE 读取则受加载时机影响、不可复现（§2.2.10） | §2.2.10 |
