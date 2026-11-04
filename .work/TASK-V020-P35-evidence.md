# Phase 35（F-224 ~ F-228）证据 · farmstorm v0.2.0

工作目录 `D:\project_farm`；执行方式：`.tools\gw.cmd`（compileJava / runData / build / runServer）；起服用独立 `run-p35`（+ `run-p35c` 客户端、`run-p35old` 老档夹具）；**`run/config` 一个字节没动**。

## 0. 改前基线（陷阱 20：判据先跑一遍记实测值）

| 判据 | 改前实测 |
|---|---|
| 注册自检 | `自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK` |
| 另四条 | 作物 22 种 / 作物品质 11-7-4 / 幸运浆果池 11 / 网络包注册 四条全 `→ OK` |
| blockstates / models / loot_table / mcmeta / textures\block | 26 / 103 / 26 / **0** / 79 |
| toml（run-p35 独立目录重生） | **171 键 / 59 段**，`[outpost_core]` **2** 键，`[trade]` 6 键 |
| lang | 330 × 2 |
| git | 工作树干净（仅 .work 未跟踪） |
| build | exit 0 |

## 1. F-224 · 培养器注册三件套 + 创造栏

### 改动
```
新建 block/CultivatorBlock.java            （WORKING BooleanProperty / codec / RenderShape.MODEL / 服务端 getTicker）
新建 blockentity/CultivatorBlockEntity.java（骨架：held / progress / corePos，serverTick 空实现；**不 implements Container**）
registry/FarmstormBlocks.java       + CULTIVATOR（MapColor.METAL / strength(5.0F,1200.0F) / requiresCorrectToolForDrops / SoundType.ANVIL / PushReaction.BLOCK）
registry/FarmstormBlockEntities.java + CULTIVATOR（单方块例，Builder.of(CultivatorBlockEntity::new, CULTIVATOR)）
registry/FarmstormItems.java         + CULTIVATOR BlockItem + creativeTabItems() 加 CULTIVATOR
datagen/ModLangProvider.java         + item.farmstorm.cultivator / block.farmstorm.cultivator
```
三个 registry 各一行原文：
```java
public static final DeferredBlock<CultivatorBlock> CULTIVATOR = REGISTER.registerBlock("cultivator", CultivatorBlock::new, ...);
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CultivatorBlockEntity>> CULTIVATOR = REGISTER.register("cultivator", () -> BlockEntityType.Builder.of(CultivatorBlockEntity::new, FarmstormBlocks.CULTIVATOR.get()).build(null));
public static final DeferredItem<BlockItem> CULTIVATOR = REGISTER.registerItem("cultivator", properties -> new BlockItem(FarmstormBlocks.CULTIVATOR.get(), properties), otherProperties());
```
创造栏：`creativeTabItems()` 末尾 `items.add(CULTIVATOR);`（A-64）。**培养器没有注册菜单**。
类声明：`public class CultivatorBlock extends BaseEntityBlock` / `public class CultivatorBlockEntity extends BlockEntity`。

### 状态 A（窗口期正确值 = FAIL）
```
自检 注册对象 96 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → FAIL
```
同一次起服的另四条（全 `→ OK`）：`作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2`、`作物品质 普通 11 / 稀有 7 / 史诗 4`、`幸运浆果池 11 种（…）`、`网络包注册 farmstorm:core_list（版本 1，S2C 只读，MAX_CORES 64）`。

### 可获取
```
> setblock 4 -59 0 farmstorm:cultivator      → Changed the block at 4, -59, 0
> data get block 4 -59 0                      → {x: 4, y: -59, z: 0, id: "farmstorm:cultivator"}      ← 有方块实体
> setblock 5 -59 0 farmstorm:cultivator[working=true] → Changed the block at 5, -59, 0              ← working 属性存在
> give @a farmstorm:cultivator                → No player was found                                  ← id 解析成功（不是 Unknown item）
```
`FarmstormItems.java` 里 `cultivator` 命中 **3** 处（:121 注册、:122 构造、:134 创造栏）≥ 2。

## 2. F-225 · 培养器资源、双模型与 datagen 三处

### 2.0 「全项目第一次」的最小用例（先跑通再写正式逻辑）
只拷 **一对** `cultivator_a_side.png` + `cultivator_a_side.png.mcmeta`，起一次 dev 客户端（`run-p35c`）：
```
[08:17:13] [Render thread/INFO] [minecraft/ReloadableResourceManager]: Reloading ResourceManager: vanilla, mod_resources, mod/farmstorm, mod/neoforge
[08:17:18] [Render thread/INFO] [minecraft/TextureAtlas]: Created: 1024x512x4 minecraft:textures/atlas/blocks.png-atlas
```
`grep 'ERROR' ` **0 行**、`grep 'animat|mcmeta|Invalid|sprite' ` 只命中 `Loaded 0 entity animations`。
⇒ 「同名同 PNG 同目录放 mcmeta 足够」在资源加载层成立（36 号 §9 ③ 的第一问）。之后才落正式资源。

### 2.1 12 个资源文件逐个点名
| # | 文件 | 备注 |
|---|---|---|
| 1 | `textures/block/cultivator_a_side.png` | 5421 B，32 × 512（16 帧帧条） |
| 2 | `textures/block/cultivator_a_side.png.mcmeta` | 242 B，sha256 `F54D973C…95809D` |
| 3 | `textures/block/cultivator_a_top.png` | 5869 B，32 × 512 |
| 4 | `textures/block/cultivator_a_top.png.mcmeta` | 242 B，sha256 `F54D973C…95809D` ← **与 #2 逐字节相同** |
| 5 | `textures/block/cultivator_a_bottom.png` | 1755 B，32 × 32 静态 |
| 6 | `textures/block/cultivator_a_side_still.png` | 2203 B，拷自 `editable_frames/side_00.png`（sha `D3CA259C…`），**不带 mcmeta** |
| 7 | `textures/block/cultivator_a_top_still.png` | 2328 B，拷自 `editable_frames/top_00.png`（sha `CD80202A…`），**不带 mcmeta** |
| 8 | `assets/farmstorm/blockstates/cultivator.json` | datagen |
| 9 | `assets/farmstorm/models/block/cultivator.json` | datagen（空闲） |
| 10 | `assets/farmstorm/models/block/cultivator_working.json` | datagen（工作） |
| 11 | `assets/farmstorm/models/item/cultivator.json` | datagen |
| 12 | `data/farmstorm/loot_table/blocks/cultivator.json` | datagen |

mcmeta 全文（两份相同）：`{"animation":{"frametime":2,"interpolate":false,"frames":[0,…,15]}}`。

### 2.2 五个目录计数（改前 → 改后）
| 目录 | 改前 | 改后 |
|---|---|---|
| `assets/farmstorm/blockstates` | 26 | **27** |
| `assets/farmstorm/models` | 103 | **106** |
| `data/farmstorm/loot_table/blocks` | 26 | **27** |
| `src/main/resources/**/*.png.mcmeta` | 0 | **2** |
| `textures/block/*.png` | 79 | **84**（+5） |
`textures/item` 仍 **29** 张、**没有** `cultivator.png`。

### 2.3 三个 json 的关键行
```json
// blockstates/cultivator.json —— 恰 2 个 variant
{"variants":{"working=false":{"model":"farmstorm:block/cultivator"},
             "working=true" :{"model":"farmstorm:block/cultivator_working"}}}
// models/block/cultivator.json
{"parent":"minecraft:block/cube_bottom_top","textures":{"bottom":"farmstorm:block/cultivator_a_bottom","side":"farmstorm:block/cultivator_a_side_still","top":"farmstorm:block/cultivator_a_top_still"}}
// models/block/cultivator_working.json
{"parent":"minecraft:block/cube_bottom_top","textures":{"bottom":"…_a_bottom","side":"…_a_side","top":"…_a_top"}}
// models/item/cultivator.json —— 无 item/generated、无 layer0
{"parent":"farmstorm:block/cultivator"}
```
战利品表：`{"type":"minecraft:block","pools":[{"entries":[{"type":"minecraft:item","name":"farmstorm:cultivator"}],"rolls":1.0}]}`（掉 1 个自身；也顺带证明 BlockItem 存在）。

### 2.4 datagen 三处改动行
```java
// ModItemModelProvider.usesBlockModel
return block instanceof ConverterBlock || block instanceof OutpostCoreBlock || block instanceof CultivatorBlock;
// ModLootTableProvider.generate()
this.add(FarmstormBlocks.CULTIVATOR.get(), this.createSingleItemTable(FarmstormBlocks.CULTIVATOR.get()));
// ModLootTableProvider.getKnownBlocks() 末尾
FarmstormBlocks.CULTIVATOR.get());          // 手写清单 26 → 27
// ModBlockStateProvider.registerStatesAndModels 分派（在兜底分支之前）
} else if (block instanceof CultivatorBlock) { cultivatorStates(block);
```

### 2.5 客户端复核（正式资源 + 模型）
起第二次 dev 客户端：`blocks.png-atlas Created: 1024x512x4`，且日志里 **`cultivator` 命中 0 次**、无 `Missing model for variant` / `Unable to load model: 'farmstorm:item/cultivator'`（最小用例时这三条是有的，加完 `usesBlockModel` 后消失）。动画观感与「切换不闪帧」留「未验证」。

## 3. F-226 · 核心改容器：54 格 + Items + 两个同步方法

### 3.0 「全项目第一次（非容器 BE → 容器）」的最小用例
第一步只加 **Container + 54 格 + NBT Items**（不含 canAccept / 同步方法 / config 键），起服实测（见 3.2、3.3），通过后再加正式逻辑。

### 3.1 改动
```java
public class OutpostCoreBlockEntity extends BlockEntity implements Container {
    public static final int SLOT_COUNT = 54;                      // 6 × 9 双箱；与培养器名额 16 不是一个数
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
```
- 容器契约七方法：`getContainerSize` / `isEmpty` / `getItem` / `removeItem`（`ContainerHelper.removeItem`）/ `removeItemNoUpdate`（`ContainerHelper.takeItem`）/ `setItem` / `clearContent`，+ `stillValid` = `Container.stillValidBlockEntity(this, player)`。
- 整批插入：`canAccept(List<ItemStack>)`（只读模拟）、`acceptAll(List<ItemStack>)`（模拟通过才写，成功后 `setChanged()` + 同 tick `sendBlockUpdated(…, Block.UPDATE_CLIENTS)`）、私有 `simulateAccept`（先合并同物品未满槽、再占空槽；任一件放不完即整批返回 null）。
- NBT（`saveAdditional` / `loadAdditional` 各加一行）：
```java
ContainerHelper.saveAllItems(tag, this.items, registries);    // 键名 ContainerHelper.TAG_ITEMS = "Items"
ContainerHelper.loadAllItems(tag, this.items, registries);    // 缺 Items 键 ⇒ 一次都不 set ⇒ 54 个空槽
```
- 全仓 `saveAllItems` 只有 **1 处**（`:576`，就在 `saveAdditional` 内）。
- 两个同步方法：
```java
@Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    CompoundTag tag = new CompoundTag();
    DeployedOwnership.write(tag, this.ownerUuid, this.ownerName);   // 只写 owner 三键
    tag.putLong(TAG_PLACED_GAME_TIME, this.placedGameTime);          // 不写 Items、不写 Cultivators
    return tag;
}
@Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
```
- config：`outpost_core.storageSlots`，`defineInRange("storageSlots", 54, 54, 54)` + `public static ModConfigSpec.IntValue outpostCoreStorageSlots;` + 1 条 lang 键。**格数读法**：菜单创建时 `Math.min(SLOT_COUNT, …get())`（见 F-227）。

### 3.2 容器面实测（run-p35，`/data get block`）
```
> data get block 0 -59 0
  0, -59, 0 has the following block data: {placedGameTime: 0L, x: 0, y: -59, Items: [], z: 0, id: "farmstorm:outpost_core"}
> data merge block 0 -59 0 {Items:[{Slot:0b,id:"minecraft:stone",count:3},{Slot:53b,id:"minecraft:dirt",count:1}]}
  Modified block data of 0, -59, 0
> data get block 0 -59 0 Items
  [… {count: 3, Slot: 0b, id: "minecraft:stone"}, {count: 1, Slot: 53b, id: "minecraft:dirt"}]
```
⇒ `Items` 键存在（0 条时为空表）、**Slot 53 可写 ⇒ 确为 54 格**。

### 3.3 老档升级（真夹具，不是口头推断）
把 `run-p35/world` 整份拷成 `run-p35old/world`，用 NBT 手术把 region `r.0.0.mca` 里 **三个** `farmstorm:outpost_core` 的 `Items` 键删掉（模拟 0.1.1 存下的核心；写回前后大小 4403 → 4362 B，仍装得进原扇区）。手术前后标签键集：
```
/block_entities/1 before=['Items','id','keepPacked','placedGameTime','x','y','z']  after=['id','keepPacked','placedGameTime','x','y','z']   （3 个核心同样处理）
```
起服（`run-p35old`）后：
```
> data get block 0 -59 0 → {… Items: [], …}    ← 54 个空槽
> data get block 6 -59 0 → {… Items: [], …}
> data get block 7 -59 0 → {… Items: [], …}
> data merge block 0 -59 0 {Items:[{Slot:53b,id:"minecraft:dirt",count:1}]} → Modified
> data get block 0 -59 0 Items → [{count: 1, Slot: 53b, id: "minecraft:dirt"}]
```
日志里与异常相关只有 3 条 WMI 警告（`COM exception querying Win32_*`，非模组代码），**无异常栈**。

### 3.4 config 实测（run-p35 独立目录删后重生）
```
sections: 59      keys: 172        ← 171 → 172
[outpost_core] keys = 3           ← maxPerPlayer / boundaryParticleIntervalTicks / storageSlots
#核心存储的格子数；范围 54 ~ 54（钉死，改它会与界面版式脱节）
# Default: 54
# Range: 54 ~ 54
storageSlots = 54
```
⇒ **36 号 §9 ⑦ 的疑问关闭**：`ModConfigSpec` **接受** `min == max` 的退化区间，**未启用** `54..55` 退路。`[trade]` 仍 6 键。

## 4. F-227 · 核心菜单 + 屏幕 + menu_types +1

### 改动
```
新建 menu/OutpostCoreMenu.java      public class OutpostCoreMenu extends AbstractContainerMenu
新建 client/OutpostCoreScreen.java  public class OutpostCoreScreen extends AbstractContainerScreen<OutpostCoreMenu>
registry/FarmstormMenus.java        + OUTPOST_CORE（REGISTER.register 共 2 处）
client/FarmstormClient.java         + event.register(FarmstormMenus.OUTPOST_CORE.get(), OutpostCoreScreen::new);（Screen 注册共 2 行）
datagen/ModLangProvider.java        + container.farmstorm.outpost_core
```
- 版式：`ROWS 6 × COLUMNS 9 = 54`，存储格 `x = 8 + 18·col`、`y = 18 + 18·row`；玩家背包 3 行 `y = 139 / 157 / 175`、快捷栏 `y = 197`；`imageWidth 176` / `imageHeight 222`；`inventoryLabelY = 128`。
- 贴图：复用原版 `minecraft:textures/gui/container/generic_54.png`（256 × 256），新增 GUI 贴图 0 张；`renderBg` 用 `graphics.blit(BACKGROUND, leftPos, topPos, 0.0F, 0.0F, 176, 222, 256, 256)`。
- `quickMoveStack` 两向 0–53 ⇄ 54–89（`TOTAL_SLOTS = 90`），照原版 `ChestMenu` 两段。
- `stillValid`：`stillValid(this.access, player, FarmstormBlocks.OUTPOST_CORE.get())`（原版语义，裁定 #17 的 B 闸）；客户端兜底 `SimpleContainer(54)`；槽数 = `Math.min(SLOT_COUNT, FarmstormConfig.outpostCoreStorageSlots.get())`，只在创建时读一次。

### 状态 B（窗口期正确值 = FAIL）
```
自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → FAIL
```
同一次起服另四条仍 `→ OK`。注册点计数：`FarmstormMenus` 里 `REGISTER.register` **2** 处；`FarmstormClient` 里 `event.register(…Screen::new)` **2** 行（:53 / :55）。
界面此时**开不了**是预期中间态：`OutpostCoreBlock` 里 `getMenuProvider` / `useWithoutItem` 命中 **0**（F-229 才接）。

### 第二次 runData
`lang` 两份各 **334** 键（330 → 332（F-224 两条）→ 333（F-226 storageSlots）→ 334（F-227 container））；`container.farmstorm.outpost_core` = 据点核心 / Outpost Core；`farmstorm.configuration.outpost_core.storageSlots` = 核心存储的格子数 / Storage slots per core。生成物只多出 5 个 cultivator 文件 + 两份 lang。

## 5. F-228 · 注册自检 97 收口（关窗）

### 5 个常量的 diff（原文）
```diff
-        boolean ok = total == 93
-                && items == 32 && blocks == 26 && blockEntities == 7 && entityTypes == 13
+        boolean ok = total == 97
+                && items == 33 && blocks == 27 && blockEntities == 8 && entityTypes == 13
                 && mobEffects == 4 && soundEvents == 3 && recipeTypes == 1 && recipeSerializers == 1
-                && creativeTabs == 1 && damageTypes == 3 && dataComponents == 1 && menuTypes == 1;
+                && creativeTabs == 1 && damageTypes == 3 && dataComponents == 1 && menuTypes == 2;
```
注释同步 3 处（不是 2 处，见偏差表 D-2）：`64 → 77 → 90 → 93` → `… → 97`、`相加不等于 93` → `…97`、`总数 93 的断言` → `总数 97 的断言`。
153 行的 13 参数格式串**一个字没改**。

### 关窗后完整自检行（原文）
```
自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK
```
`Farmstorm.java` 里 `93` 命中 **0**、`97` 命中 **4**。

## 6. 负向面与红线

| 项 | 读数 |
|---|---|
| 七次起服里另四条自检行 | 每次全 `→ OK`（见 §1 / §4 / §5 引文） |
| `farmland/` 两个类 | `git diff --stat` **空** |
| `docs/` | `git diff --stat` **空** |
| `registry` 改动面 | 恰 **4** 个文件（Blocks / BlockEntities / Items / Menus） |
| `Run/config` | 未改动（全部读数取 `run-p35` / `run-p35old`） |
| `@Mixin` / `mixins.json` | **0 / 0** |
| `textures/item` | 仍 29 张，无 `cultivator.png` |
| Java 源 BOM | 0 个文件带 BOM |
| build | `EXITCODE=0`（`farmstorm-0.2.0+mc1.21.1.jar`，`mod_version` 未动） |
| `ModLangProvider.java:13-16` | 一字未动（diff 里那 6 行区域 0 命中） |
| `FarmstormConfig` 总数注释 | 未动（diff 仅 +7 行，0 删除） |

`git diff --stat`（原文）：
```
.../resources/assets/farmstorm/lang/en_us.json     |   4 +
 .../resources/assets/farmstorm/lang/zh_cn.json     |   4 +
 src/main/java/com/dashpig/farmstorm/Farmstorm.java |  12 +-
 .../blockentity/OutpostCoreBlockEntity.java        | 166 ++++++++++++++++++++-
 .../dashpig/farmstorm/client/FarmstormClient.java  |   2 +
 .../dashpig/farmstorm/config/FarmstormConfig.java  |   7 +
 .../farmstorm/datagen/ModBlockStateProvider.java   |  27 ++++
 .../farmstorm/datagen/ModItemModelProvider.java    |  11 +-
 .../dashpig/farmstorm/datagen/ModLangProvider.java |   8 +
 .../farmstorm/datagen/ModLootTableProvider.java    |  10 +-
 .../farmstorm/registry/FarmstormBlockEntities.java |   9 +-
 .../farmstorm/registry/FarmstormBlocks.java        |  14 +-
 .../dashpig/farmstorm/registry/FarmstormItems.java |   9 +-
 .../dashpig/farmstorm/registry/FarmstormMenus.java |   7 +-
 14 files changed, 274 insertions(+), 16 deletions(-)
```
新增未跟踪（11 个源文件 + 12 个资源）：见 §7。

## 7. 文档-源码偏差表

| # | 文档写的 | 实测 / 采用 | 处置 |
|---|---|---|---|
| D-1 | 36 号 §2.6.3：玩家背包 `y = 140`、快捷栏 `y = 198`（「原版常量」） | 原版 `ChestMenu.java:56-72` 在 6 行时是 `103 + (6-4)*18 = 139` / `161 + 36 = 197` | 采用 **139 / 157 / 175 / 197**（否则与复用的 `generic_54.png` 槽位框错 1 像素）；已在 `OutpostCoreMenu` 类注释里登记 |
| D-2 | 37 号 F-228：方法注释里**两处** `93` | `Farmstorm.java` 实有 **3** 处注释含 `93`（:149 / :150 / :182） | 三处一起改成 97（否则「`93` 命中 0」不成立） |
| D-3 | 37 号 F-228：`:126-131` / `:147-150` 等行号 | 实际行号为 `:148-153`（注释）/ `:169-172`（常量）——F-223 在构造函数里加了 2 行导致整体下移 | 按内容定位，不按行号 |
| D-4 | 36 号 §2.6.3：「`renderBg` 用**六参** blit」 | 六参形式内部把 `IMAGE_WIDTH×IMAGE_HEIGHT` 当贴图尺寸；`generic_54.png` 实为 256 × 256 | 用九参 `blit(…, 176, 222, 256, 256)`，语义正确（`ConverterScreen` 那条注释记的是同一坑的反向） |
| D-5 | 30/39 号：`ModConfigSpec` 可能拒绝 `54..54`（§9 ⑦，退路 `54..55` + 钳制） | **接受**：toml 落 `Range: 54 ~ 54`，起服无异常 | 退路未启用，格数恒 54 |

## 8. 未验证清单

| 项 | 原因 |
|---|---|
| 培养器动画的观感（帧速、切换那一帧是否闪默认图；RT-47 / 36 号 §9 ③ 的第二、三问） | 需要带玩家的客户端现场看；本泳道只做到「资源加载 + 模型解析零报错」，观感留 F-244 |
| 创造栏「工具方块」段末多 1 项的**截图** | 无玩家会话，无法自动化 GUI 交互；代码层证据：`FarmstormItems.creativeTabItems()` 末尾 `items.add(CULTIVATOR)`（:134）+ 创造页 `displayItems` 消费该列表 |
| 54 格界面实际打开、槽位对位、`quickMoveStack` 手感 | F-229 才接 `useWithoutItem` / `getMenuProvider`，本节按设计「界面开不了」 |
| 老档升级后**打开界面**看 54 个空格 | 同上（F-229） |

## 9. 未关闭的问题

1. `getUpdateTag` 只写 owner 三键（不塞 `Items`）：客户端方块实体因此看不到 54 格内容。若 F-229 之后出现「比较器 / 渲染需要物品」的实际需求，按 36 号 §9 ④ 的退路只放「非空槽数 + 首槽物品」（≤ 200 B），仍不搬 54 格。
2. `acceptAll` 成功后调 `sendBlockUpdated`，但 `getUpdateTag` 不带 `Items` ⇒ 这条刷新只覆盖「绑定面」；F-239 连线包落地前，客户端不会因为 `acceptAll` 而看到物品变化（预期）。
3. 培养器的 `serverTick` 目前是空实现（F-235 起填）；`working` 属性已存在但还没有任何代码写它——世界里不会出现 `working=true` 的方块，属预期中间态。
