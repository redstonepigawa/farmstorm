# Phase 37（F-234 ~ F-237）实测证据

> 执行者：Phase 37 唯一执行者。工作目录 `D:\project_farm`。所有读数取自 2026-11-04 的实跑，日志在 `.work/p37-*.log`。
> 起服目录 `run-p37`（`FS_RUN_LOCK=run-p37`，`-PfsRunDir=run-p37`），未在 `run/` 里起服，未动 `run/config`。

## 0. 总体

### 0.1 改动面（`git diff --stat` 原文）

```
 .../resources/assets/farmstorm/lang/en_us.json     |   2 +
 .../resources/assets/farmstorm/lang/zh_cn.json     |   2 +
 .../dashpig/farmstorm/block/CultivatorBlock.java   |  49 +++++
 .../blockentity/CultivatorBlockEntity.java         | 234 ++++++++++++++++++++-
 .../dashpig/farmstorm/config/FarmstormConfig.java  |  17 ++
 .../dashpig/farmstorm/datagen/ModLangProvider.java |   4 +
 6 files changed, 297 insertions(+), 11 deletions(-)
```

允许改的四个源文件 + 两份生成的 lang，**没有第七个文件**。

### 0.2 构建

| 命令 | 退出码 | 日志 |
|---|---|---|
| `gw.cmd compileJava` | **0** | `.work/p37-compile-A.log`（BUILD SUCCESSFUL in 23s） |
| `gw.cmd build` | **0** | `.work/p37-build-A.log`（BUILD SUCCESSFUL in 15s） |
| `gw.cmd runData -PfsRunDir=run-p37data` | **0** | `.work/p37-data-A.log`（BUILD SUCCESSFUL in 30s） |
| `gw.cmd build`（**终稿**，含重新生成的 lang） | **0** | `.work/p37-build-final.log`（BUILD SUCCESSFUL in 15s） |

### 0.3 负向面

| 判据 | 命令 | 读数 |
|---|---|---|
| 注册自检 | `Select-String .work/p37-run-A.log 自检 注册对象` | `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK` |
| `OutpostCoreBlockEntity` 未动 | `git diff --stat -- .../OutpostCoreBlockEntity.java` | **空** |
| `farmland/ registry/ network/ team/ item/ menu/ client/ docs/ build.gradle .tools/ run/` 未动 | 同名 `git diff --stat --` 一条命令覆盖八处 | **空** |
| `ModLangProvider:13-16` / config 总数注释未动 | `git diff` 两个文件 | 只有 `@@ -426,6 +426,10 @@` 与 `@@ -236,6 +236,12 @@` / `@@ -945,6 +951,17 @@` 三个 hunk，**类注释那几行不在 hunk 内** |
| `verify_project.py` | `python tools/verify_project.py` | `已核 registry 对象: 93 / 93`、`FAIL 0 项，WARN 0 项` —— **A-66 未收口，本 Phase 不引用这个结论**（它只核 93 个对象、只认 166 个 config 键；F-243/F-242 才同步） |

---

## 1. F-234 · `cultivator` 组 2 键 + 2 条语言键

### 1.1 新组与两键的定义原文（`config/FarmstormConfig.java`）

字段区（`:239-244`）：

```java
    // D-163 / F-234：培养器的**顶层组**（与 outpost_core 同级，不是嵌套组）。
    /** 培养周期（刻），默认 600 = 30 秒 × 20 刻/秒。 */
    public static ModConfigSpec.IntValue cultivatorCycleTicks;
    /** 单次产出数量（作物与种子各这么多），默认 2。 */
    public static ModConfigSpec.IntValue cultivatorYield;
```

静态块（`outpost_core` 的 `b.pop()` 之后、`trade` 的 `b.push` 之前，`b.push("cultivator")` 是**顶层组**）：

```java
        // F-234 / D-163 / 36 号 §4.1.1 #4~#5：培养器顶层组（与 outpost_core 同级，不是嵌套组）。
        // 「2 个种子」这一半**不另设键**（D-163 / 35 号 §6.3 纪律 2），它跟着产出规则走；多设一个键就变 176。
        b.push("cultivator");
        cultivatorCycleTicks = b.comment("培养周期（刻），600 刻 = 30 秒；范围 20 ~ 72000")
                .translation(tr("cultivator.cycleTicks"))
                .defineInRange("cycleTicks", 600, 20, 72000);
        cultivatorYield = b.comment("单次产出数量（作物与种子各这么多）；范围 1 ~ 64")
                .translation(tr("cultivator.yield"))
                .defineInRange("yield", 2, 1, 64);
        b.pop();
```

### 1.2 toml 读数（删旧 toml 起服后生成）

`run-p37` 是全新目录，`run-p37/config/farmstorm-server.toml` 由 server 首次启动时按 spec 写出（旧文件不存在，等价「先删再生」）。

| 项 | 改前（`run-p36/config/…toml`，Phase 36 的产物） | 改后（`run-p37/config/…toml`） |
|---|---|---|
| `^\s*\[` 段数 | **59** | **60** |
| `^\s*[A-Za-z_].*=` 键数 | **173** | **175** |
| `[cultivator]` | 无 | 有，恰 **2** 键 |

`[cultivator]` 段原文：

```toml
[cultivator]
	#培养周期（刻），600 刻 = 30 秒；范围 20 ~ 72000
	# Default: 600
	# Range: 20 ~ 72000
	cycleTicks = 600
	#单次产出数量（作物与种子各这么多）；范围 1 ~ 64
	# Default: 2
	# Range: 1 ~ 64
	yield = 2
```

段内键行只有 `cycleTicks = 600` 与 `yield = 2`。组在文中的位置是 `[outpost_core]` 之后、`[trade]` 之前，与字段/静态块同序。

### 1.3 两条语言键

| 键 | zh_cn | en_us |
|---|---|---|
| `farmstorm.configuration.cultivator.cycleTicks` | 培养周期（刻，600 刻 = 30 秒） | Cultivation cycle in ticks (600 ticks = 30 s) |
| `farmstorm.configuration.cultivator.yield` | 单次产出数量（作物与种子各这么多） | Items per cycle |

落点：`datagen/ModLangProvider.java:429-432`（`config(...)` 两条，紧跟 `outpost_core.cultivatorCapacity` 之后）；生成的两份 lang 各 `@@ -132,6 +132,8 @@` 两行。

键数读数（`ConvertFrom-Json` 数 `PSObject.Properties`）：**改前 335 / 335 → 改后 337 / 337**（zh_cn / en_us 两次都相等）。此数是**区间读数**，337 的收口结论属 F-241。

### 1.4 一处必须记的运行期事实

`cycleTicks` 是**每次结算时现读 config**（`FarmstormConfig.cultivatorCycleTicks.get()` 在 `serverTick` 第 3 步取），不缓存：改 `run-p37/config/farmstorm-server.toml` 的 `cycleTicks` 600 → 100，**不重启**，下一轮培养即刻按 100 刻完成（实测 `[ubi_server ×2]` 于 5 秒内入核）。这条同时证死陷阱 5（不许在代码里留第二份默认值）。

---

## 2. F-235 · 状态机、四道准入闸与放入

### 2.1 三个 NBT 键

| 键 | 类型 | 落点 | 读法 |
|---|---|---|---|
| `Held` | `CompoundTag`（`ItemStack#save` / `ItemStack#parse`） | `saveAdditional:492-494` / `loadAdditional:502-504` | 空槽时**不写键** |
| `Progress` | `int` | `saveAdditional:495` / `loadAdditional:505` | 恒写 |
| `Core` | `long`（`BlockPos#asLong`） | `saveAdditional:496-499` / `loadAdditional:506` | 未绑定时**不写键**（缺键 = 未绑定） |

`/data get block` 实测（未绑定）：
`5, 159, 0 has the following block data: {Progress: 0, x: 5, y: 159, z: 0, id: "farmstorm:cultivator"}` —— **无 `Core`、无 `Held`**。

### 2.2 `serverTick` 状态机四步（全文）

```java
    public static void serverTick(Level level, BlockPos pos, BlockState state, CultivatorBlockEntity cultivator) {
        if (level.isClientSide) {
            return;
        }
        // 第 1 步：空槽 ⇒ 不可用，进度归零（槽被清空后才可能重新计时，半途中的产出不存在）。
        if (cultivator.held.isEmpty()) {
            if (cultivator.progress != 0) {
                cultivator.progress = 0;
                cultivator.setChanged();
            }
            cultivator.setWorking(false);
            return;
        }
        // 第 2 步：未绑定 / 核侧不认 / 核不在 ⇒ 不 tick，但**不清槽、不清进度**（D-168 出口②③）。
        if (!cultivator.isUsable()) {
            cultivator.setWorking(false);
            return;
        }
        int cycleTicks = FarmstormConfig.cultivatorCycleTicks.get();
        // 第 3 步：有货且可用 ⇒ 累计一刻。
        if (cultivator.progress < cycleTicks) {
            cultivator.progress++;
            cultivator.setChanged();
            cultivator.setWorking(true);
            return;
        }
        // 第 4 步：progress == cycleTicks ⇒ 本刻结算一次。
        if (cultivator.deliver()) {
            cultivator.held = ItemStack.EMPTY;
            cultivator.progress = 0;
            cultivator.setChanged();
            cultivator.setWorking(false);
        } else {
            // 核满暂停（D-164）：progress **停在 cycleTicks 不清零**，下一 tick 会再走到第 4 步，
            // 核腾出空间的那一 tick 自动完成投递 —— 「从暂停处继续、不重新计时」由此天然成立。
            cultivator.setWorking(false);
        }
    }
```

`setWorking` 只在**值真变化**时写一次 blockstate，且带 `UPDATE_CLIENTS`：

```java
    private void setWorking(boolean working) {
        if (!(this.level instanceof ServerLevel server)) {
            return;
        }
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof CultivatorBlock) || state.getValue(CultivatorBlock.WORKING) == working) {
            return;
        }
        server.setBlock(this.getBlockPos(), state.setValue(CultivatorBlock.WORKING, working), Block.UPDATE_CLIENTS);
    }
```

`working` 走的是 **blockstate 属性**（`CultivatorBlock.WORKING`），不是 BE 同步字段；外部读数用 `execute if block <pos> farmstorm:cultivator[working=true]`。

### 2.3 四道闸 = 一次判定

```java
    public boolean isUsable() {                       // 闸 1 范围 + 闸 2 绑定
        if (this.corePos == null) { return false; }
        OutpostCoreBlockEntity core = this.core();
        if (core == null) { return false; }
        if (!core.hasCultivator(this.getBlockPos())) { return false; }
        return OutpostCoreBlockEntity.inRange(this.corePos, this.getBlockPos());
    }

    public static boolean admissible(ItemStack stack) {   // 闸 3 手持档：只读 tier()
        if (stack.isEmpty()) { return false; }
        CropProfile profile = profileOf(stack);
        return profile != null && profile.tier() != CropTier.EPIC;
    }

    public boolean canInsert(ItemStack stack) {           // 四闸合并成一次判定
        return this.isUsable() && this.held.isEmpty() && admissible(stack);
    }
```

`profileOf` 用**物品类**反查档案（不写任何 id 字符串）：

```java
    private static CropProfile profileOf(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ModifiedCropItem crop) { return crop.profile(); }
        if (item instanceof DeployableCropItem deployed) { return deployed.profile(); }
        if (item instanceof SeedItem seed) { return seed.profile(); }
        return null;
    }
```

`useWithoutItem`（`CultivatorBlock.java:114-128`）：

```java
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof CultivatorBlockEntity cultivator)) {
            return InteractionResult.PASS;
        }
        ItemStack heldStack = player.getMainHandItem();
        if (!cultivator.canInsert(heldStack)) {
            return InteractionResult.PASS;
        }
        cultivator.insert(heldStack.split(1));
        return InteractionResult.CONSUME;
```

### 2.4 四种右键场景的 `/data get block` 读数

摆场：核 `(0,159,0)`（ALICE 放置，`setPlacedBy` 走完），培养器 `(1,159,0)`（ALICE 放置）。

| 场景 | 操作 | 实测读数 |
|---|---|---|
| 放置绑定成立 | — | 核 `Cultivators: [274877907103L]`（= `(1,159,0).asLong()`）；培养器 `Core: 159L` |
| 史诗**作物** ×4 | `awakened_torchflower` / `awakened_pitcher_plant` / `lucky_berry` / `wild_apple` 各右键一次 | 4 次全为 `Held` 不存在（`execute if data block 1 159 0 Held` → `Test failed`），`Progress: 0`，手持物 `count: 8` 始终不变 |
| 史诗**种子** ×2 | `awakened_torchflower_seeds` / `awakened_pitcher_plant_pod` | 同上，`Test failed` ×2，手持物不减 |
| 空手右键 | `unequip` 后右键 | `Test failed`（不进槽、**不取出**，B-11） |
| 未绑定培养器 | `/setblock 5 159 0 farmstorm:cultivator`（`setblock` 不走 `setPlacedBy` ⇒ 无绑定）后手持 `puffed_wheat` 右键 | `5, 159, 0 has the following block data: {Progress: 0, …}` —— 无 `Held`、**无 `Core`**；`Test failed`；手持物 16 → 16 |
| 普通作物（正例） | `puffed_wheat` 右键 | `Held: {count: 1, id: "farmstorm:puffed_wheat"}`、`Progress: 348`（持续涨）、`execute if block 1 159 0 farmstorm:cultivator[working=true]` → `Test passed`；手持物 16 → 15 |
| 槽已占用再右键 | 再右键一次（手持 `puffed_wheat`） | **前** `{Progress: 403, Held: {count: 1, …}, hand 14}` → **后** `{Progress: 533, Held: {count: 1, …}, hand 14}`：不叠加、不替换、**进度不归零**、手持物不减 |

合计 7 次拒绝（6 个史诗 + 1 次空手），全部 `commands.execute.conditional.fail`，`pass` 计数 0（`Select-String` 计数：`fail=7 / pass=0`）。

### 2.5 进度持久（两轮）

**第一轮 · 走远 200 格再回来（不重启）**

| 时刻 | 玩家坐标（`data get entity ALICE Pos`） | `Progress` |
|---|---|---|
| T1 | `1.5, 159.0, 3.5` | **250** |
| T2 | `250.5, 72.0, 250.5`（离培养器 249 格） | **396**（照常涨，B-18） |
| T3 | `1.5, 159.0, 3.5` | **583** |

之后正常结算（T3 后核里 `puffed_wheat 6 → 6`，见 §3 的同一轮），**没有归零、没有重新计时**。

**第二轮 · 停服重载（决定性读数）**

为了把「重启后读数」与「重启后累积」区分开，本轮把 `cycleTicks` 临时设为 3000，把进度推到四位数：

| 事件 | 时间 | `Progress` |
|---|---|---|
| 停服前 RCON 读 | — | **1421**（`Held: {count: 1, id: "farmstorm:puffed_wheat"}`） |
| `save-all flush` + `stop` | — | — |
| 重启后 `Done (12.748s)` | 10:18:06 | — |
| **重启后第 1 次读** | 10:18:07 | **1466** |
| 之后每秒一次 | 10:18:09 / 10:18:10 / 10:18:12 | 1499 / 1531 / 1563 |

重启后 1 秒就是 **1466**，不是几十 —— 若 `Progress` 没落盘，此刻只可能是「重启后累积的那 ~20 刻」。`Held` 同样原样保留（槽没清空）。

---

## 3. F-236 · 产出与核满暂停

### 3.1 批次决定的代码

```java
    /** 产出批次（36 号 §2.5.4）：有独立种子 ⇒ {@code [作物 × yield, 种子 × yield]}，否则只 {@code [作物 × yield]}。 */
    private static List<ItemStack> batch(CropProfile profile) {
        int amount = FarmstormConfig.cultivatorYield.get();
        List<ItemStack> batch = new ArrayList<>(2);
        batch.add(new ItemStack(FarmstormItems.forProfile(profile).get(), amount));
        if (hasOwnSeed(profile)) {
            ResourceKey<Item> seed = profile.seedItem();
            batch.add(new ItemStack(BuiltInRegistries.ITEM.get(seed.location()), amount));
        }
        return batch;
    }

    /**
     * 「有独立种子物品」的判据（36 号 §2.5.4）：{@code seedItem() != null} <b>且</b>它不等于 {@code id()}。
     * 22 条档案里 8 条的 {@code seedItem} 指向**自身**，只写 {@code != null} 会让这些档案的产出翻倍
     * （D-163 补充明确否决）。准入面内因此是 5 条给 2 + 2、13 条只给 2。
     */
    private static boolean hasOwnSeed(CropProfile profile) {
        ResourceKey<Item> seed = profile.seedItem();
        return seed != null && !seed.location().equals(profile.id());
    }
```

### 3.2 `canAccept` / `acceptAll` 调用点

```java
    private boolean deliver() {
        OutpostCoreBlockEntity core = this.core();
        if (core == null) { return false; }
        CropProfile profile = profileOf(this.held);
        if (profile == null) {
            // 不可达：准入闸只放带档案的物品进来。真到了这里也不销毁物品 —— 留在槽里，拆方块可原样取回。
            return false;
        }
        return core.acceptAll(batch(profile));
    }
```

只有**一处**投递调用（`acceptAll`），传入的是**一次性算好的整批** `List<ItemStack>`；`OutpostCoreBlockEntity.simulateAccept` 内部先整批模拟、任一件放不完就 `null` ⇒ 一个都不写。两个改动文件里**没有**逐件判定、没有 `ItemEntity`（`grep -c` = **0**）、没有 `getInventory`（`grep -c` = **0**）；`.add(` 只有 3 处，分别是 `batch.add(...)` ×2（`List#add`）与 `builder.add(WORKING)`（`StateDefinition.Builder#add`）。

### 3.3 三种投料的产出清单（核的 `Items` 读数）

cycleTicks = 600（默认值），yield = 2。每种投料跑满一轮后读 `data get block 0 159 0 Items`。

| 投料 | 产前 | 产后 | 增量 |
|---|---|---|---|
| `puffed_wheat`（普通，**有独立种子**） | `[]` | `[{count: 2, Slot: 0b, id: "farmstorm:puffed_wheat"}, {count: 2, Slot: 1b, id: "farmstorm:puffed_wheat_seeds"}]` | **+2 作物 +2 种子** |
| `ubi_server`（稀有，**种子 = 自身**） | `puffed_wheat 6 / seeds 6` | `…, {count: 2, Slot: 2b, id: "farmstorm:ubi_server"}` | **只 +2 作物**，无种子 |
| `chive_seeds`（稀有种子） | `…, ubi_server 2` | `…, {count: 2, Slot: 3b, id: "farmstorm:friend_chives"}, {count: 2, Slot: 4b, id: "farmstorm:chive_seeds"}` | **+2 作物 +2 种子** |
| `friend_chives`（同一档案的父作物） | 上一行 | `…, friend_chives 4, chive_seeds 4` | **+2 作物 +2 种子**（与 `chive_seeds` **完全相同**） |

`puffed_wheat` 那一轮又重复了一次（第二次投料后 `4 / 4`），两次增量都是 2 + 2。

### 3.4 600 刻的实测节拍

投料 02:01:26（`Progress: 0`）→ 02:01:43 读 `Progress: 348` → 02:01:56 前后入核。约 30 秒 / 600 刻，与 `cultivator.cycleTicks = 600` 一致（伪码是「先 `++` 再判等」，所以投递落在第 `cycleTicks + 1` 刻，见 §5 偏差 5）。

### 3.5 核满暂停 → 清出 2 格 → 自动投递

摆场：`item replace block 0 159 0 container.0..53 with minecraft:stone 64`（54 行）把核塞满，读到 `Items` 有 **54 个 `stone × 64`**。

| 步骤 | 读数 |
|---|---|
| 投料前的地面物品 | `execute if entity @e[type=minecraft:item,distance=..32]` → `Test passed, count: 1`（世界里的既有一件，与本机无关） |
| 投 `puffed_wheat` 后等 36 秒 | `1, 159, 0 has the following block data: {Progress: 600, Held: {count: 1, id: "farmstorm:puffed_wheat"}, …}` —— **`Progress == cycleTicks`、`Held` 还在、没有归零** |
| `working` | `execute if block 1 159 0 farmstorm:cultivator[working=true]` → **`Test failed`**（暂停态显示静态首帧，D-161） |
| 地面物品 | `count: 1` —— **与投料前同一件，没有新增**（不落地、不丢） |
| 手持物 | `11`（投料后没再动） |
| 清出 2 格 | `item replace block 0 159 0 container.0/1 with minecraft:air` |
| 清出后 ~1.5 秒 | `Progress: 0`（清空）、`Items[0] = {count: 2, id: "farmstorm:puffed_wheat"}`、`Items[1] = {count: 2, id: "farmstorm:puffed_wheat_seeds"}` —— **下一 tick 自动完成投递，没有重新等 600 刻** |

---

## 4. F-237 · 培养器破坏掉落

### 4.1 `onRemove` 全文（`CultivatorBlock.java:141-147`）

```java
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !movedByPiston
                && level.getBlockEntity(pos) instanceof CultivatorBlockEntity cultivator) {
            Containers.dropContents(level, pos, cultivator.releaseHeld());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
```

四条件：**真移除**（`!state.is(newState.getBlock())`）∧ **非活塞推动**（`!movedByPiston`）∧ **方块实体是本类型**（`instanceof` 分支，兼作空值保护）。第一条同时挡住「`working` 属性变化触发的那次 `onRemove`」——属性变化时新旧 state 属于同一方块，`state.is(...)` 为真，条件不成立，不掉落。

`releaseHeld()` 在 BE 侧，返回 `NonNullList<ItemStack>`（不是 `Container`）：

```java
    public NonNullList<ItemStack> releaseHeld() {
        NonNullList<ItemStack> contents = NonNullList.withSize(1, ItemStack.EMPTY);
        contents.set(0, this.held);
        this.held = ItemStack.EMPTY;
        this.progress = 0;
        return contents;
    }
```

`grep` 计数（两个改动文件内）：`NonNullList` **7** 处、`Containers.dropContents` **1** 处（+1 处 javadoc 引用）、`Container` 接口实现 **0** 处。

### 4.2 三种挖法的掉落清单

因为 mineflayer 对「未知方块」不发挖掘包（`dig` 返回但方块仍在，实测 `execute if block 9 159 0 minecraft:air` → `Test failed`），破坏改用 `/setblock <pos> minecraft:air destroy` —— 它在服务端走 `ServerLevel.destroyBlock(pos, true)`（`SetBlockCommand.java:92-93`），与玩家挖掉同一条链：先跑战利品表、再经 `setBlock` 触发 `onRemove`。等价性在同一场用石头标定过：`setblock 20 159 0 stone` → `setblock 20 159 0 air destroy` → 掉 `1 × minecraft:cobblestone`。

| 挖法 | 操作 | 掉落清单（`as @e[type=minecraft:item] run data get entity @s Item`） |
|---|---|---|
| 槽里有作物 | 培养器 `(9,159,0)` 的 NBT 为 `{Progress: 176, Held: {count: 1, id: "farmstorm:puffed_wheat"}, …}`；`setblock 9 159 0 minecraft:air destroy` | **2 件**：`{count: 1, id: "farmstorm:cultivator"}` + `{count: 1, id: "farmstorm:puffed_wheat"}` |
| 空槽 | 培养器 `(11,159,0)` 的 NBT 为 `{Progress: 0, …}`（无 `Held` 键）；同法破坏 | **+1 件**（该格半径 4 内由 2 → 3，新增的那件是 `{count: 1, id: "farmstorm:cultivator"}`），**没有作物** |
| 活塞推动 | `setblock 13 159 0 farmstorm:cultivator` + `setblock 14 159 0 minecraft:piston[facing=west]` + `setblock 15 159 0 minecraft:redstone_block` | **0 件**（该格半径 8 内 `Test failed`）；同时 `execute if block 13 159 0 farmstorm:cultivator` → `Test passed`、`execute if block 14 159 0 minecraft:piston[extended=true]` → **`Test failed`** —— 活塞**根本没伸出**，因为培养器是 `PushReaction.BLOCK`（`FarmstormBlocks.java:102`），推不动。见 §5 偏差 1 |

方块本体由战利品表掉、内部作物由 `onRemove` 掉，两处不重叠（`loot_table/blocks/cultivator.json` 只有一个 `farmstorm:cultivator`）。

---

## 5. 文档 – 源码偏差表

| # | 文档口径 | 源码 / 实测 | 处置 |
|---|---|---|---|
| 1 | 37 号 F-237 易漏点②「漏 `!movedByPiston`（复制）」暗示培养器**可被活塞推动** | D-160 补充 + `FarmstormBlocks.java:102` 给了 `PushReaction.BLOCK`，活塞推不动它；`movedByPiston=true` 这条路径在本科上**不可达**。实测活塞不伸出、方块不动、0 掉落 | 四条件仍照 `ConverterBlock` 逐字写全（防御性）。**不改代码**，登记。验收判据「活塞推动 → 0 掉落」按字面成立 |
| 2 | 36 号 §2.5.2 第 1 步 `progress = 0`（无条件） | 本实现只在 `progress != 0` 时才写并 `setChanged()`（省掉每刻无意义标脏） | 行为等价，不登记为偏差，仅记写法 |
| 3 | 36 号 §2.5.2 第 3 步「`progress++` 后 return」、第 4 步「`progress == cycleTicks` 才结算」 | 因此投递发生在第 `cycleTicks + 1` 刻（600 刻配置 ⇒ 约 30.05 秒） | 照伪码实现，不改；仅为可复现记时 |
| 4 | 36 号 §2.5.3「可放入物品共 **23** 个」 | 门槛写成「物品带档案 ∧ `tier() != EPIC`」，等价于 18 作物 + 5 种子 = 23；实测 4 史诗作物 + 2 史诗种子全拒、普通/稀有全过 | 一致，无偏差 |
| 5 | 36 号 §2.5.3 `useWithoutItem` 伪码把 `held` 与 `progress = 0` 写在方块侧 | 本实现挪进 `CultivatorBlockEntity.insert()`（方块侧只做「判过再 split(1)」），字段仍私有 | 等价，无偏差 |
| 6 | 36 号 §9 ⑤ 担心「暂停检测的 1 秒恢复延迟」 | 实测**没有延迟**：核腾出空间的**下一 tick** 就投递（清 2 格后 ≤1.5 秒的读数间隔内已完成） | 该待验证项可关，需作者确认 |

## 6. 未验证清单

| 项 | 原因 |
|---|---|
| `cycleTicks` 上界 **72000**、`yield` 上界 **64** 的运行期行为 | 只跑过 600（默认）/ 100 / 3000 与 yield=2；上下界只由 `defineInRange` 的字面与 toml 的 `Range:` 注释证明 |
| 非本模组物品 / 其它模组物品右键 | 代码判据是 `profileOf(...) == null`；本轮 7 次拒绝覆盖了「史诗」「空手」，没单独跑「拿着钻石镐右键」 |
| 客户端渲染面 | `working` 属性 → 两套模型 / 两套贴图的**视觉**切换未在客户端看；本轮只取 `execute if block … [working=true]` 的服务端 blockstate 读数（`blockstates/cultivator.json` 两个 variant 存在，属资源层证据） |
| 非己方玩家右键的挥手动画 | 未跑（「右键无反应」按行为面口径验） |
| 多台培养器并发结算（>1 台同 tick 投递同一核） | 本轮只有 1 台在跑；并发下的 `acceptAll` 顺序未测 |
| `working=false` 时 `Progress` 仍在涨的边界（未绑定期间进度冻结） | 只由代码路径覆盖（第 2 步 return，不 `progress++`），未做「拔掉核→进度不变→放回核→继续」的实跑 |

## 7. 未关闭的问题

1. **`Progress` 每刻 `setChanged()`**（照原版熔炉 `AbstractFurnaceBlockEntity.serverTick` 的写法）：本机实测无异常，但「每刻标脏」对一个 30 秒读一次的字段是否过重，属世界保存频率的问题，本轮没量化。
2. **暂停恢复时机** = 下一 tick（见 §5 偏差 6）：36 号 §9 ⑤ 的退路（在核的 `setItem` / `removeItem` 里加唤醒回调）**不需要**，因为轮询本身就是每刻；建议作者把 §9 ⑤ 关掉。
3. **`OutpostCoreBlockEntity` 侧无改动**：本轮 `deliver()` 只调 `acceptAll`，但 `simulateAccept` 的「同物品未满槽合并」意味着核里同物品的分散堆会被优先填满（`puffed_wheat` 16 上限），实测同一轮两批产出都并进了同一槽（`Slot 0` 由 2 → 4）。这符合 36 号 §2.6.1 的体例，但与「一次产出一槽」的直觉不同，登记备查。

## 8. 复现跑法（供下一个人重跑）

```powershell
# 1) 建目录（不要在 run/ 里起服）
New-Item -ItemType Directory run-p37
Copy-Item run\eula.txt,run\server.properties run-p37\
# 2) 起服（删旧 toml 后生成新的）
$env:FS_RUN_LOCK='run-p37'
cmd /c "\"D:\project_farm\.tools\gw.cmd\" runServer -PfsRunDir=run-p37" *> .work\p37-run-A.log
# 3) 摆场：平台 y=158、空气 159+，核 (0,159,0) 与培养器 (1,159,0) 必须**由玩家放置**（setblock 不会走 setPlacedBy ⇒ 不绑定）
# 4) 右键 / 读数走 RCON 25575（.work/p37r.py），右键动作走 mineflayer（.work/p37-bot.js）
```

RCON 侧的关键探针：

| 目的 | 命令 |
|---|---|
| 槽内是否有物 | `execute if data block 1 159 0 Held` |
| 进度 | `data get block 1 159 0` |
| 工作态 | `execute if block 1 159 0 farmstorm:cultivator[working=true]` |
| 核内容 | `data get block 0 159 0 Items` |
| 灌核 / 腾格 | `item replace block 0 159 0 container.<n> with <item> <count>` / `… with minecraft:air` |
| 地面掉落 | `execute positioned <x> <y> <z> as @e[type=minecraft:item,distance=..4] run data get entity @s Item` |

注意：`distance=..N` 以**执行位置**为基准 —— 不写 `positioned` 时 RCON 的基准是世界出生点，会静默给出错误答案（本轮踩过一次）。
