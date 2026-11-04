# Phase 36 证据（F-229 ~ F-233）

> 执行者：本节唯一执行者。目录 `D:\project_farm`（Windows / pwsh）。构建入口 `cmd /c ""D:\project_farm\.tools\gw.cmd" build`。
> 起服目录：`run-p36`（`FS_RUN_LOCK=run-p36`）；**`run/config` 一个字节未动**。

---

## 1. 改了什么 + 构建

### 1.1 `git diff --stat` 原文

```
 .../resources/assets/farmstorm/lang/en_us.json     |   1 +
 .../resources/assets/farmstorm/lang/zh_cn.json     |   1 +
 .../dashpig/farmstorm/block/CultivatorBlock.java   |  39 ++-
 .../dashpig/farmstorm/block/OutpostCoreBlock.java  |  77 +++++-
 .../blockentity/CultivatorBlockEntity.java         |  65 ++++-
 .../blockentity/OutpostCoreBlockEntity.java        | 284 +++++++++++++++++----
 .../dashpig/farmstorm/config/FarmstormConfig.java  |   7 +
 .../dashpig/farmstorm/datagen/ModLangProvider.java |   3 +
 8 files changed, 417 insertions(+), 60 deletions(-)
```

8 个文件全部落在任务书 §1 的**允许改**白名单内（4 个 java + config 的 1 个键 + datagen 的 1 条键 + runData 生成的 2 份 lang）。

### 1.2 构建与工具面

| 项 | 读数 |
|---|---|
| `gw.cmd build`（改前基线） | `BUILD SUCCESSFUL in 14s`，**exit 0** |
| `gw.cmd build`（全部落地后） | `BUILD SUCCESSFUL in 14s`，**exit 0** |
| `gw.cmd runData` | exit 0，`written: 2`（只有两份 lang，`git diff --stat -- src/generated` = 1 + 1 行） |
| `tools/verify_project.py` | `已核 registry 对象: 93 / 93`，`FAIL 0 项` —— **A-66：待 F-243 同步，此读数少核 4 个对象**。本节任何判据都**没有引用**这个 `FAIL 0`。 |

---

## 2. F-229 段 · 核心存储打开：两道闸

### 2.1 `useWithoutItem` 全文（`OutpostCoreBlock.java:116-142`）

```java
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core) || !core.isAlly(player)) {
            return InteractionResult.PASS;
        }
        MenuProvider provider = this.getMenuProvider(state, level, pos);
        if (provider == null) {
            return InteractionResult.PASS;
        }
        player.openMenu(provider);
        return InteractionResult.CONSUME;
    }
```
（上面就是文件里的原文，未做删改。）

- **只判己方**：唯一的分支是 `core.isAlly(player)`；方法体内**没有距离量、没有常量、没有 `RANGE_CHUNKS`**。
- 非己方 → `PASS`（不吞右键）；己方 → `CONSUME` + `player.openMenu`。
- 客户端 `SUCCESS`（HC-03 的既有取舍，照 `ConverterBlock.java:81-87`）。

### 2.2 `getMenuProvider` 全文（`OutpostCoreBlock.java:108-114`）

```java
    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new OutpostCoreMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                Component.translatable("container.farmstorm.outpost_core"));
    }
```

### 2.3 `stillValid` 用原版语义的证据

**本任务没有改 `OutpostCoreMenu`（diff 为 0）** —— F-227 已经落地了 B 闸的形态，原文（`menu/OutpostCoreMenu.java:127-135`）：

```java
    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, FarmstormBlocks.OUTPOST_CORE.get());
    }
```
→ 落到 `AbstractContainerMenu.stillValid(access, player, block)` → `Container.stillValidBlockEntity(be, player)`，原版默认距离参数是 `4.0F`。`OutpostCoreBlockEntity#stillValid` 也走 `Container.stillValidBlockEntity(this, player)`（`:232-236`）。**没有新常量、没有新 config 键**。

### 2.4 两道闸的实测（`run-p36`，服务端包面读数）

客户端是 mineflayer（不认识自定义 menu type），所以界面读数取**服务端下发的包**：`RAW_OPEN` = `ClientboundOpenScreenPacket`，`RAW_ITEMS` = `ClientboundContainerSetContentPacket`。

| 判据 | 读数 | 结论 |
|---|---|---|
| 己方空手右键（距核中心 1.12 格） | `RAW_OPEN windowId=1 inventoryType=26 windowTitle={"translate":"container.farmstorm.outpost_core"}` + `RAW_ITEMS slots=90 nonEmpty=81=1364x8` | **开 90 槽 = 54 存储 + 36 玩家背包**；标题键 `container.farmstorm.outpost_core` → zh_cn lang = 「据点核心」（已核 `src/generated/resources/assets/farmstorm/lang/zh_cn.json`）；槽位 81 命中玩家快捷栏第 0 格，映射正确 |
| 存 3 个物品 → 关掉 → 再开 | `RAW_ITEMS window=1 slots=90 nonEmpty=0=1x1,1=805x3,5=800x2,81=1364x8` | **3 件仍在**（slot 0/1/5） |
| 非己方（BOB，`team list` = 「There are no teams」，非队友） | `USE ok (2,160,0) dist=2.06`，**没有 `RAW_OPEN`** | 不开界面 ✓ |
| 距离闸：rawuse 逐档试 | 1.12 / 3.04 / 4.03 / 5.02 / 6.02 / **6.12 开**；7.02 / 8.02 **不开** | 打开的门槛落在 **(6.12, 7.03)** 之间（服务端交互距离闸），**不是** 8 格 |
| 打开后走远 | 1.12 格开（window 3）→ `tp` 到 12.5 格 → `RAW_CLOSE {"windowId":3}` / `WINDOW_CLOSE` | **原版 `stillValid` 把界面关掉** ✓（8.5 = 4.5 + 4.0 的上限被越过） |

**登记一处与文档口径的差**：39 号 §5.4 写「**8 格内**打开、**9 格外**发不出交互」。实测服务端在 6.12 格能开、7.03 格不开 —— 原版 `ServerGamePacketListenerImpl` 的交互距离闸比 `stillValid` 的 8.5 格更紧；真人的原版客户端在 `blockInteractionRange`（4.5）之外根本不发 `block_place`，所以「8 格内打开」这一句在真人客户端上是**更严**而不是更松。8.5 格这个数只对「已经打开之后界面还开不开」成立，与 36 号 §2.6.4 读法 3 的原文一致。

---

## 3. F-230 段 · 核心破坏掉落

### 3.1 四个条件的代码原文（`OutpostCoreBlock.java:167-176`）

```java
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core) {
            if (!movedByPiston) {
                Containers.dropContents(level, pos, core);
            }
            core.release();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
```
四个条件：① `!state.is(newState.getBlock())`（真移除 ∧ 非同方块替换）② `!movedByPiston` ③ 该格 BE 是 `Container`（这里收紧成 `OutpostCoreBlockEntity`）④ 走 `Containers.dropContents`。

### 3.2 四个场景的实际掉落清单（`/execute as @e[type=item] run scoreboard players add` 计数 + `Item.id` 逐条读）

| 场景 | 掉落清单 | 结论 |
|---|---|---|
| 核里 3 组不同物品（stone×1 / diamond×3 / apple×2），**玩家挖掉**（creative dig，4 格外，避免拾取） | `n=4`：`minecraft:stone`×1、`minecraft:diamond`×3、`minecraft:apple`×2、+ 1 个无关的 `minecraft:egg`（附近鸡下的蛋，环境噪声） | **3 组全掉** ✓（另一次近距离挖时 apple 被 ALICE 捡走，`Inventory` 里读到 `minecraft:apple x2`，同样是「掉了」） |
| **活塞推动** | 空（`n=0`）；活塞读数为 `piston[extended=true]` **未命中** → `extended=false` | 0 掉落 ✓。原因是结构性的：核心注册处是 `PushReaction.BLOCK`（`FarmstormBlocks.java:91`），活塞**推不动**它 ⇒ `movedByPiston=true` 这条路对本方块不可达；`!movedByPiston` 作为防御条件保留 |
| `/setblock` 同方块替换 | 空（`n=0`） | 0 掉落 ✓。附注：`/setblock` 同方块**会把容器内容清空**——已用原版 `minecraft:chest` 做对照复现（`data get block` 从 `Items:[{stone x5}]` 变成 `Items:[]`），是**原版命令行为**，不是本模组引入 |
| 区块卸载（`save-all` + `stop` → 重起） | 重起后 `data get block 2 160 0` → `Items: [{count: 7, Slot: 0b, id: "minecraft:stone"}, {count: 3, Slot: 1b, id: "minecraft:diamond"}]` | 物品仍在核里 ✓ |
| **空核**被挖掉（survival + 钻石镐） | `n=1`：`farmstorm:outpost_core` | 只掉 1 个方块本体 ✓ |

### 3.3 一条与判据等价的补充读数

`/setblock <core> air` 造成的「真移除」**不掉物品**（`n=0`），原因同样用原版箱子对照证明（`/setblock <chest> air` 也是 0 掉落）：`/setblock` 在 `onRemove` 之前就把 `Container` 清空了。**掉落只由玩家破坏路径验证**，这也是真实玩家的唯一路径。

---

## 4. F-231 段 · 核侧 16 条绑定列表 + `cultivatorCapacity`

### 4.1 字段与 NBT（`OutpostCoreBlockEntity.java`）

```java
    private static final String TAG_CULTIVATORS = "Cultivators";
    private final List<BlockPos> cultivators = new ArrayList<>();
```
落盘（`saveAdditional`）：`ListTag` of `LongTag`（每条 `BlockPos#asLong()`），空列表也写；读盘（`loadAdditional`）：`tag.getList(TAG_CULTIVATORS, Tag.TAG_LONG)`，缺键 ⇒ 空列表。

### 4.2 五个访问方法 + 惰性清理

```java
    public List<BlockPos> cultivators()      // 先 pruneCultivators() 再 List.copyOf（**返回副本**）
    public int cultivatorCount()             // 不触发清理（候选过滤每帧要问很多次）
    public boolean hasCultivator(BlockPos)
    public boolean isCultivatorListFull()    // size >= FarmstormConfig.outpostCoreCultivatorCapacity.get()
    public boolean addCultivator(BlockPos)   // 已绑或满员返回 false，**不挤掉已绑的**
    public boolean removeCultivator(BlockPos)
```
`pruneCultivators()` 用 `ServerChunkCache#getChunkNow`：区块取不到 ⇒ **保留**该条（读不到 ≠ 方块没了）；只在 `cultivators()` 这一条低频路径上跑。

### 4.3 `cultivatorCapacity` 定义（`FarmstormConfig.java`）

```java
        outpostCoreCultivatorCapacity = b.comment("一个核心最多绑定的培养器数量；范围 1 ~ 64（与 storageSlots 的 54 无关）")
                .translation(tr("outpost_core.cultivatorCapacity"))
                .defineInRange("cultivatorCapacity", 16, 1, 64);
```
默认 **16**、值域 **1–64**；语言键 `farmstorm.configuration.outpost_core.cultivatorCapacity` 中英各 1 条（runData 生成）。

### 4.4 toml 读数（**删旧 toml 起服**）

| 时点 | 读数 |
|---|---|
| 改前（基线，run-p36 全新生成） | `sections: 59` / `keys: 172` / `[outpost_core] keys = 3`（maxPerPlayer / boundaryParticleIntervalTicks / storageSlots） |
| F-231 后（`Remove-Item run-p36/config/farmstorm-server.toml` 再起服） | `sections: 59` / `keys: 173` / `[outpost_core] keys = 4` → 正文 `maxPerPlayer=2 / boundaryParticleIntervalTicks=40 / storageSlots=54 / cultivatorCapacity=16` |

### 4.5 `getContainerSize()` 与 16 的关系

```java
    @Override
    public int getContainerSize() {
        return SLOT_COUNT;      // 54
    }
```
**返回 54，不含 16**。见 §9 偏差表第 1 条：任务书写「读 `storageSlots`」，源码是常量 `SLOT_COUNT`（`= 54`），`storageSlots` 值域钉死 54–54，两者恒等，本节未改。

### 4.6 `Cultivators` 的实测

| 场景 | 读数 |
|---|---|
| 空核 | `data get block 2 160 0` → `Cultivators: []` |
| 绑 1 个培养器后（核在 (-10,160,0)） | `Cultivators: [160L]`（160L = `BlockPos(0,160,0).asLong()`）→ **1 条 long** ✓ |
| `cultivatorCapacity = 2` 时第 3 个 | 见 §5.4：第 3 个培养器**放得下**（`id: farmstorm:cultivator` 在位）但**没有 `Core` 键** = 未绑定；核侧列表仍是 2 条，**没有挤掉已绑的** ✓ |

---

## 5. F-232 段 · 放置时绑定：四判据 + 两侧写入

### 5.1 选核的实现落点

选核逻辑放在 `OutpostCoreBlockEntity#selectCore`（F-232 放置与 F-233 迁移**共用同一份实现**），`CultivatorBlock#setPlacedBy` 只负责「先核后培」的写入顺序。

```java
    public static OutpostCoreBlockEntity selectCore(ServerLevel level, BlockPos pos, TeamResolver.Owner self,
                                                    @Nullable BlockPos exclude) {
        ...
        for (UUID owner : index.owners()) {
            if (!Farmstorm.TEAMS.isAlly(level, new TeamResolver.Owner(owner, index.ownerName(owner)), self)) {
                continue;                             // 判据 2：己方
            }
            for (OutpostCoreIndex.Entry entry : index.entriesOf(server, owner)) {
                if (!entry.dimension().equals(dimension)) { continue; }
                if (entry.pos().equals(exclude)) { continue; }
                if (!inRange(entry.pos(), pos)) { continue; }            // 判据 1：覆盖（3 × 3 区块）
                LevelChunk chunk = level.getChunkSource()
                        .getChunkNow(entry.pos().getX() >> 4, entry.pos().getZ() >> 4);
                if (chunk == null) { continue; }                          // 不加载区块
                if (!(chunk.getBlockEntity(entry.pos()) instanceof OutpostCoreBlockEntity core)) { continue; }
                if (core.isCultivatorListFull()) { continue; }             // 判据 3：未满
                candidates.add(core);
            }
        }
        candidates.sort(Comparator
                .comparingDouble((OutpostCoreBlockEntity core) -> core.getBlockPos().distSqr(pos))
                .thenComparingLong(OutpostCoreBlockEntity::placedGameTime)
                .thenComparingInt(core -> core.getBlockPos().getX())
                .thenComparingInt(core -> core.getBlockPos().getY())
                .thenComparingInt(core -> core.getBlockPos().getZ()));
        return candidates.isEmpty() ? null : candidates.get(0);
    }
```
- **先过滤再取最近**（`isCultivatorListFull()` 在 `candidates.add` 之前），不是先取最近再判空位。
- 判据 4 的三级键：`distSqr`（三维欧氏平方）→ `placedGameTime` 升序 → 坐标字典序。
- 只读索引 + `getChunkNow` + **从那个 `LevelChunk` 上取方块实体**（不碰 `Level#getBlockEntity`，它会走 `getChunkAt`）。

### 5.2 两侧写入的落点（`CultivatorBlock#setPlacedBy`）

```java
        OutpostCoreBlockEntity core = OutpostCoreBlockEntity.selectCore(server, pos,
                new TeamResolver.Owner(player.getUUID(), player.getScoreboardName()), null);
        if (core == null) {
            return;                                   // 未绑定：不写 Core 键（缺键就是未绑定）
        }
        if (core.addCultivator(pos)) {                 // 先核侧（唯一真相）
            cultivator.setCore(core.getBlockPos());    // 再培养器侧（引用缓存）
        }
```
`CultivatorBlockEntity` 新增 `Core` 键（`long` = `BlockPos#asLong()`）、`corePos()` / `setCore` / `clearCore`，未绑定时 `saveAdditional` 不写该键。

### 5.3 四个对照的 `/data get block` 原文

**(a) A 满 16、B 有空位 → 绑 B（两侧一致）**
```
A = (2,160,0)  Cultivators: [16 条 long ...]        （用 /data merge 填到 16 条，容量 2 ⇒ 满）
B = (-10,160,0) Cultivators: []                     placedGameTime: 30680L
培养器 (0,160,0)  →  {x: 0, y: 160, z: 0, id: "farmstorm:cultivator", Core: -2748779069280L}
B 侧复查         →  Cultivators: [160L]             160L = BlockPos(0,160,0).asLong()
A 侧复查         →  Cultivators 仍是那 16 条（未被写）
```
`asLong(-10,160,0) = -2748779069280`（已用同一套位算法交叉验证），**两侧一致** ✓

**(b) 同刻两核等距 → 绑 `placedGameTime` 更小者，且重跑同答案**
```
C = (5,160,10)   placedGameTime: 33044L   Cultivators: []
D = (-5,160,10)  placedGameTime: 33259L   Cultivators: []
培养器 (0,160,10) → Core: 1374389575840L = asLong(5,160,10)   ← 绑 C（时间更小）
培养器 (0,160,11) → Core: 1374389575840L = asLong(5,160,10)   ← 同样绑 C
C 侧 → Cultivators: [41120L, 45216L]（= 两个培养器坐标）
D 侧 → Cultivators: []
```
两次独立放置 → 同一个答案（确定性），平手由 `placedGameTime` 定序 ✓

**(c) 附近无己方核 → 缺 `Core` 键**
```
200, 100, 200 → {x: 200, y: 100, z: 200, id: "farmstorm:cultivator"}     ← 没有 Core 键
```

**(d) 事后补核 → `Core` 仍缺失（不自动认领）**
```
202, 100, 200 → 核，placedGameTime: 34762L，Cultivators: []             ← 新核列表也是空的
200, 100, 200 → {x: 200, y: 100, z: 200, id: "farmstorm:cultivator"}     ← 仍然没有 Core 键
```

---

## 6. F-233 段 · 迁移三出口（`settleOnRemoval` 整段改写）

### 6.1 整段改写后的全文（`OutpostCoreBlockEntity.java:655-700`）

```java
    private void settleOnRemoval() {
        if (!(this.level instanceof ServerLevel server) || this.ownerUuid == null) {
            return;
        }
        BlockPos corePos = this.getBlockPos();
        LevelChunk chunk = server.getChunkSource().getChunkNow(corePos.getX() >> 4, corePos.getZ() >> 4);
        if (chunk == null) {
            return;                                       // 区块不在内存：无法证明方块没了 ⇒ 什么都不动
        }
        if (chunk.getBlockState(corePos).getBlock() instanceof OutpostCoreBlock) {
            return;                                       // 方块还在原地：这是区块卸载 / 停服，不是移除
        }
        // 步骤 4：**先复制一份再遍历** —— 本方法会改 this.cultivators，边遍历边改会跳过或重复处理同一项。
        List<BlockPos> bound = this.cultivators();
        TeamResolver.Owner self = new TeamResolver.Owner(this.ownerUuid, this.ownerName);
        for (BlockPos pos : bound) {
            // 出口①：接替核 = 覆盖 ∧ 己方（相对本核归属者）∧ 未满，最近优先；本次已写走的名额自然包含在内
            OutpostCoreBlockEntity next = selectCore(server, pos, self, corePos);
            if (next != null && next.addCultivator(pos)) {
                LevelChunk nextChunk = server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                if (nextChunk != null
                        && nextChunk.getBlockEntity(pos) instanceof CultivatorBlockEntity cultivator) {
                    cultivator.setCore(next.getBlockPos());        // 两侧都写：核侧已写，这里写培养器侧缓存
                }
                continue;
            }
            // 以下都是「没有可用的接替核」：先看培养器那一格读不读得到、还是不是培养器（都不动世界）
            LevelChunk cultivatorChunk = server.getChunkSource()
                    .getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (cultivatorChunk == null) {
                Farmstorm.LOGGER.info("迁移失败且区块未加载：保留 NBT 原值 {}", pos);   // 出口③
                continue;
            }
            if (!(cultivatorChunk.getBlockState(pos).getBlock() instanceof CultivatorBlock)) {
                Farmstorm.LOGGER.info("迁移失败且该格已不是培养器：保留 NBT 原值 {}", pos);  // 出口③
                continue;
            }
            if (cultivatorChunk.getBlockEntity(pos) instanceof CultivatorBlockEntity cultivator) {
                cultivator.clearCore();                    // 出口②：留原地、清 Core、内容保留、不可使用
                Farmstorm.LOGGER.info("迁移失败：培养器留原地并标为未绑定 {}", pos);
            }
        }
        this.cultivators.clear();       // 只清绑定列表；items 不动（掉落是 onRemove 的 Containers.dropContents）
        this.setChanged();
        this.release();                 // 最后一行：摘索引 + 释放区块票 + 请求刷新浮窗列表（既有方法）
    }
```
七步齐：真移除判定（`getChunkNow` + 该格已不是 `OutpostCoreBlock`）→ 复制列表 → 逐条选接替核 → 三出口 → 只清 `cultivators` → `release()`。**没有「变泥土」、没有「打掉培养器掉物品」、没有 `setBlock`。**

### 6.2 三出口的实测读数

**(i) 出口①：有接替核 → 改绑，两侧都改**
```
拆前：C=(5,160,10) Cultivators:[41120L,45216L]；D=(-5,160,10) Cultivators:[]
拆 C（/setblock 5 160 10 air）
拆后：(0,160,10) → Core: -1374389493600L = asLong(-5,160,10)   ← 改绑到 D
      (0,160,11) → Core: -1374389493600L = asLong(-5,160,10)   ← 改绑到 D
      D 侧     → Cultivators: [41120L, 45216L]                 ← 核侧也写了，两侧一致
日志：核心区块票释放 + 0 条「迁移失败」
```
`Held` / `Progress` 不变：**未验证** —— 这两个字段要到 F-235 才建（见 §9）。

**(ii) 出口②：无接替核 → 留原地、`Core` 键消失、内容保留、不变泥土、不掉落**
```
拆 D（/setblock -5 160 10 air）
拆后：(0,160,10) → {x: 0, y: 160, z: 10, id: "farmstorm:cultivator"}   ← Core 键消失，方块**还在**
      (0,160,11) → {x: 0, y: 160, z: 11, id: "farmstorm:cultivator"}   ← Core 键消失，方块**还在**
      execute if block 0 160 10 farmstorm:cultivator → 命中（p36e=1）
      execute if block 0 160 10 minecraft:dirt      → 未命中（p36d 未设置）⇒ **没变泥土**
      掉落计数 n=0（此前 kill 过；之后的 1~3 个实体读出来是 azalea / glow_ink_sac / egg，全是环境掉落，没有 core / cultivator）
日志：迁移失败：培养器留原地并标为未绑定 BlockPos{x=0,y=160,z=10} / {x=0,y=160,z=11}
```

**(iii) 出口③：区块取不到 → NBT 一字节不动**
```
A=(2,160,0) 用 /data merge 写入 8 条远坐标（600..607, y=200）+ 1 条 (5,160,5)
拆 A（/setblock 2 160 0 air）
日志 8 条：迁移失败且区块未加载：保留 NBT 原值 BlockPos{x=600,y=200,z=600} … {x=607,y=200,z=607}
data get block 600 200 600 → That position is not loaded     ← 确实不在内存，且**没有为它加载区块**
```
- 这 8 条走了 `getChunkNow == null` 分支 = **出口③**，代码路径上 `continue`，一个字节都没写。
- 第 9 条 `(5,160,5)` 是石头不是培养器，**在进循环之前就被 `cultivators()` 的惰性清理摘掉了**（36 号 §2.7 末段的既定行为），因此「该格已不是培养器」这条日志**没有出现** —— 该分支在本路径上是**防御性死分支**（保留，见 §9）。

### 6.3 grep 判据（`setBlock` / `getChunkNow` / 两个死类）

| 判据 | 读数 |
|---|---|
| `settleOnRemoval` 方法体内 `setBlock` | **0**（原文里只有注释提到「没有 setBlock」） |
| 方法体内 `getChunkNow` | **3** 处（核区块 1 + 培养器区块 2） |
| 本类 `getChunkAt` / `getChunk(` 实际调用 | **0**（唯一命中在 javadoc 注释里） |
| 本类 `FarmlandLedger` / `FarmlandGuard` 引用（含 import） | **0**（改前 13） |
| 本类 `getChunkNow` 总命中 | 9（3 处代码 + 6 处注释） |

---

## 7. A-69 段 · 两次 mtime 读数

| 时点 | 路径 | mtime（UTC） | Ticks | Size |
|---|---|---|---|---|
| **开工（改任何文件之前）** | `run/world/data/farmstorm_farmland.dat` | `2026-11-01T18:55:28.6794638Z` | `639291561286794638` | 274 |
| **F-233 落地之后** | 同上 | `2026-11-01T18:55:28.6794638Z` | `639291561286794638` | 274 |

**两次完全相同，文件存在。** 本节之前的所有记录里**没有**出现过这条断言（第一次记在开工，第二次记在 F-233 之后）。

---

## 8. 负向面

| 判据 | 读数 |
|---|---|
| 注册自检行（`run-p36` 起服，09:28:30 / 09:07:37 两次） | `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK` |
| `menu_types` / `block_entities` | **2** / **8**，不变 |
| `farmland/` 两个类 diff | **空**（`FarmlandLedger.java` 555 LF / `FarmlandGuard.java` 225 LF 一行未改） |
| `registry/`、`network/`、`client/`、`docs/` diff | **全部空** |
| `outpost/`（`OutpostCoreIndex`）diff | **空**（见 §9 偏差表第 2 条） |
| `run/`、`build.gradle`、`.tools/` | `git status --porcelain` 空 |
| 红线 | 仍然**只有「无 Mixin」**，本节没有新增任何 Mixin，也没有新增网络包 |

---

## 9. 偏差表 / 未验证清单 / 未关闭的问题

### 9.1 文档-源码偏差表

| # | 文档说 | 源码/实测 | 处置 |
|---|---|---|---|
| 1 | 任务书 ③ / 37 号 F-231：「`getContainerSize()` 读 `storageSlots`（54）」 | 源码是 `return SLOT_COUNT;`（`= 54`），不读 config；`storageSlots` 值域钉死 54–54（F-226），两者恒等 | **不改**（F-226 已验收的形态，改了反而让「只在创建菜单时读一次」这条规则出现第二种读法）。判据按「返回 54、不含 16」读：命中 `return SLOT_COUNT;` 1 处，`16` 只在 `Cultivators` |
| 2 | 37 号 F-232 涉及文件含 `outpost/OutpostCoreIndex.java`（候选核查询） | 任务书 §1 的允许改白名单**不含** `outpost/` | 候选查询改用索引**既有**的只读访问器（`owners()` / `entriesOf()` / `ownerName()`）在 `OutpostCoreBlockEntity#selectCore` 里实现；`OutpostCoreIndex` diff = 0 |
| 3 | 39 号 §5.4：「**8 格内**打开、**9 格外**发不出交互」 | 实测打开门槛在 **(6.12, 7.03)** 之间（服务端交互距离闸），8.5 只对「已打开后还开不开」成立 | 按实测写读数；无代码改动（距离仍全部交给原版） |
| 4 | 36 号 §2.7 步骤 5 的第三支：「该格已不是 CultivatorBlock ⇒ 出口③」 | 该分支可达性被 `cultivators()` 的惰性清理抢在前头（进循环前就摘掉） | 分支**保留**为防御（`level` 不是 `ServerLevel` 时清理会跳过，那时它才是唯一判据）；本次实测未触发 |
| 5 | 37 号 F-231 说 `cultivatorCapacity` 定义落在 `FarmstormConfig`:909-912（`farmlandBonus` 让位） | 实际加在 `storageSlots` 之后、同组末尾 | 键数/段数读数不受影响（`[outpost_core]` 4 键 / 全文 173 键 / 59 段，与 39 号 §5.4 一致） |

### 9.2 未验证清单（每条一句原因）

1. **`Held` / `Progress` 在迁移后不变** —— 这两个字段属于 F-235，本节尚未存在，无法读。
2. **「培养器不可使用 / 右键无反应」的区分度** —— `CultivatorBlock` 还没有 `useWithoutItem`（F-235），现在右键**任何**培养器都没反应，判据在 F-235 之前不可区分。
3. **原版客户端的「9 格发不出交互」** —— 需要真人客户端；本次用 mineflayer 直接发 `block_place` 绕过客户端预测，取到的是**服务端**闸的读数（6.12 开 / 7.03 不开）。
4. **54 格界面的视觉版式（6 行 × 9 列、标题渲染）** —— mineflayer 不含本模组的语言与 menu type，只能读服务端下发的 `open_window` / `container_set_content` 包（90 槽 + 标题键）；屏幕绘制属客户端、需真人复核。
5. **出口③「真培养器 + 区块不在内存」** —— 结构性不可达：被绑的培养器必在核的 3 × 3 内，而 `CoreChunkTickets` 的票到 `release()`（最后一行）才放，所以只要核还在，那一片就在内存。本次用「远坐标假条目」触发同一分支并拿到 8 条日志 + `That position is not loaded` 的反证。
6. **活塞推动时 `movedByPiston=true` 的真实到达** —— `PushReaction.BLOCK` 让活塞推不动核心，这条路径对本方块不可达；`!movedByPiston` 作为防御条件保留（与 `ConverterBlock` 同形，掉落用原版箱子做过 0 掉落对照）。

### 9.3 未关闭的问题

1. **`ContainerHelper.loadAllItems` 不清空列表**：`/data remove block <core> Items` 之后 `Items` 会「复活」（旧内容仍在 `this.items` 里，下一次 `saveAdditional` 又写回去）。这是 `ContainerHelper` 的原版语义（`loadAllItems` 只 `set` 不 `clear`），对「从磁盘冷加载」没有影响（新实例本来就是空槽），但对 `/data` 调试命令有影响。**本版不改**（改它要动到「缺 `Items` ⇒ 54 空槽」这条老档兼容口径），留给 F-244 决定是否登记。
2. **`/setblock` 造成的「真移除」不走掉落**：已用原版箱子证明是命令行为（命令在 `onRemove` 前清空容器）。真人路径（挖掘）已验 3 组全掉。若 36 号 §8 #26 的验收也想覆盖命令路径，需要另写口径。
3. **绑定变化是否需要额外一次 `CoreListBroadcaster.requestAll`**（36 号 §9 ⑫ 的待验证）：本节的放置 / 迁移都只走 `sendBlockUpdated`，没有额外请求浮窗刷新；这一条按 36 号的设计留给 F-239 的广播器。
