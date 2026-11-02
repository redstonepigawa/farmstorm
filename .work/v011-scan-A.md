# v0.1.1 事实扫描报告 · A（据点核心 / 耕地 / HUD / 音效 / 基线）

扫描时间：2026-11-02。全部行号以当前工作区文件为准；「实测」= 本次实际执行的命令。

## 0. 结论摘要（只讲事实分布）

1. 组 1 三个文件全部存在：`OutpostCoreBlock.java` 108 行、`OutpostCoreBlockEntity.java` 415 行、`OutpostCoreIndex.java` 277 行；索引是 `SavedData`，落盘名 `farmstorm_outpost_core`。
2. 据点核心方块实体没有 `getUpdateTag` / `getUpdatePacket`，只有 `setChanged`（1 处，`OutpostCoreBlockEntity.java:102`）。
3. 组 2 的账本是 `FarmlandLedger`（`SavedData`，落盘名 `farmstorm_farmland`）；拆核清算只在 `OutpostCoreBlockEntity#setRemoved → settleOnRemoval` 这一条链上；踩踏保护 `FarmlandGuard#protectsFromTrample` 以「在线玩家的 `ServerPlayer` 实体」为锚。
4. 组 3 自定义网络包 0 个；BE 同步先例 2 个文件（`DeployedCropBlockEntity` 双方法、`ConverterBlockEntity` 只有 `getUpdateTag`）；`@Mixin` 0 个。
5. 组 4：HUD 先例 1 个（`WildHunterHud.java`，77 行，`RenderGuiEvent.Post`）；`client` 包下 21 个类；keybind 注册 0、CLIENT 类型 config 0。
6. 组 5：`compressed_hay_block` 当前 `.sound(SoundType.GRASS)`（`FarmstormBlocks.java:79`）；全仓只出现 3 个 `SoundType` 值（GRASS / STONE / CROP），`SoundType.STONE` 唯一先例是 `outpost_core`（`:88`）。
7. 组 6：12 类注册对象共 93 个 ID，逐条点名与 `EXPECTED_COUNTS` 完全一致；config 键 166；lang 315 键且中英键集相同。
8. `tools/verify_project.py` 实跑结果：`FAIL 0 项，WARN 0 项`，exit code 0。
9. `gradle.properties:11` 的 `mod_version=0.1.0`，`build/libs` 与 `dist/` 最新产物均为 `0.1.0`；仓库里没有 `0.1.1` 产物。
10. `OutpostCoreIndex#isOwnerOrAlly`（`:99-113`）在全仓 0 个调用点；`/farmstorm farmlandlimit` 是唯一改额度的指令入口。

## 1. 据点核心方块

### 1.1 `src/main/java/com/dashpig/farmstorm/block/OutpostCoreBlock.java`（108 行）

- `:32` `public class OutpostCoreBlock extends BaseEntityBlock`；`:33` `CODEC = simpleCodec(OutpostCoreBlock::new)`；`:40-42` `codec()` 返回 `CODEC`。
- `:45-48` 覆写 `getRenderShape` 返回 `RenderShape.MODEL`（注释说明 `BaseEntityBlock` 默认是 `INVISIBLE`）。
- `:50-53` `newBlockEntity` 返回 `new OutpostCoreBlockEntity(pos, state)`。
- `:56-67` 覆写 `getTicker`：`:59-61` `level.isClientSide` 为真时返回 `null`；否则返回 lambda，`:62-66` 仅当 `blockEntity instanceof OutpostCoreBlockEntity` 时调 `OutpostCoreBlockEntity.serverTick(...)`。
- `:77-85` 放置逻辑：`getStateForPlacement` 中 `:81` 判 `player != null && OutpostCoreIndex.countOwned(player) >= FarmstormConfig.outpostCoreMaxPerPlayer.get()` → `:82` 返回 `null`（拒绝放置）；否则 `:84` 返回 `defaultBlockState()`。
- `:88-94` `setPlacedBy`：`:91` 判 `placer instanceof Player player && level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core` → `:92` `core.setOwner(player)`。
- `:101-107` **实际在用的破坏回调是 `onRemove`**：`:103` 条件 `!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof OutpostCoreBlockEntity core` → `:104` `core.release()`；`:106` 调 `super.onRemove(...)`。
- 本类**没有**覆写 `playerWillDestroy`、`affectNeighborsAfterRemoval`、`getShape`、`getCollisionShape`、`getDestroyProgress`（全文 108 行逐行读完；另实测 grep `playerWillDestroy|affectNeighborsAfterRemoval|getShape(|getCollisionShape|getDestroyProgress` 在本文件中仅命中 `:29` 的一行注释）。
- `:25-30` 注释声明注册属性写在 `FarmstormBlocks`（见 1.4），本类不覆写 `getDestroyProgress`。

### 1.2 `blockentity/OutpostCoreBlockEntity.java`（415 行）

字段（全部实例字段，`:75-85`）：

- `:76` `@Nullable private UUID ownerUuid;`
- `:78` `@Nullable private String ownerName;`
- `:79` `private long placedGameTime;`
- `:85` `private boolean cleared;`（一次性闸，注释 `:81-84`；**不写 NBT**，`saveAdditional` 里没有它）
- 静态常量：`:46` `TAG_PLACED_GAME_TIME = "placedGameTime"`；`:49` `RANGE_CHUNKS = 1`；`:52` `RING_BLOCKS = 16 * (RANGE_CHUNKS*2+1) = 48`；`:54` `RING_STEP = 4`；`:55-56` 每边 12 点 / 单圈 48 点；`:59` `RING_DUST = DustParticleOptions((0.30,0.85,0.90), 1.0)`；`:62` `RING_Y_OFFSET = 0.1`；`:65-66` `RING_MARGIN = 16.0` / `RING_MARGIN_DECIMATION = 4`；`:73` `static Map<UUID,Long> LAST_RING_TICK = new HashMap<>()`。

NBT key 逐个点名（写/读各一处）：

- `ownerUuid`、`ownerName`：`:404` `DeployedOwnership.write(tag, ownerUuid, ownerName)` / `:411-412` `DeployedOwnership.readUuid/readName`；键名定义在 `util/DeployedOwnership.java:18-19`（`public static final String TAG_OWNER_UUID = "ownerUuid"` / `TAG_OWNER_NAME = "ownerName"`）。
- `placedGameTime`：`:405` `tag.putLong(TAG_PLACED_GAME_TIME, ...)` / `:413` `tag.getLong(...)`。

方法实现情况（任务问的四个 + `setChanged` + ticker）：

- `saveAdditional`：**有**，`:401-406`（先 `super`，再写上面 3 个键）。
- `loadAdditional`：**有**，`:408-414`（先 `super`，再读上面 3 个键）。
- `getUpdateTag`：**没有**（本文件无此方法；全仓实测只有 `DeployedCropBlockEntity:456`、`ConverterBlockEntity:419` 两处）。
- `getUpdatePacket`：**没有**（全仓实测只有 `DeployedCropBlockEntity:464` 一处）。
- `setChanged`：**有 1 处**，`:102`（在 `setOwner` 内，紧跟 `OutpostCoreIndex.record` 之后）。
- ticker：方块侧 `getTicker`（`OutpostCoreBlock.java:56-67`）→ 本类静态 `serverTick`（`:179-192`）；`:183` 从 `FarmstormConfig.outpostCoreBoundaryParticleIntervalTicks` 取间隔，`:184-186` `interval <= 0` 直接返回，`:188` 相位 `floorMod(now + floorMod(pos.hashCode(), interval), interval) != 0` 则返回，否则 `:191` `emitBoundaryRing(...)`。

其它方法要点：

- `:95-103` `setOwner(Player)`：写 `ownerUuid`（`:96`）、`ownerName = placer.getGameProfile().getName()`（`:97`）、`placedGameTime = placer.level().getGameTime()`（`:98`）；`:99-101` 若 `level` 是 `ServerLevel` 则 `OutpostCoreIndex.of(server).record(server, placer, pos)`；`:102` `setChanged()`。
- `:108-115` `owner()`：`level.getServer().getPlayerList().getPlayer(ownerUuid)`，离线返回 `null`。
- `:122-127` `isAlly(Player)`：非 `ServerLevel` 返回 `true`；否则 `DeployedOwnership.isAlly(server, ownerUuid, ownerName, player)`。
- `:134-136` / `:139-145` `inRange`：区块坐标切比雪夫距离 `<= RANGE_CHUNKS`（`:144`）。
- `:166-170` `release()`：`ServerLevel` 且 `ownerUuid != null` 时 `OutpostCoreIndex.of(server).remove(server, ownerUuid, pos)`。
- `:201-233` `emitBoundaryRing`：`:202-204` `ownerUuid == null` 直接返回；`:208` 清理 `LAST_RING_TICK`；`:210-232` 遍历 `server.players()`，条件依次 `isAlly`（`:211`）、`ringGap <= RING_MARGIN`（`:215`）、余量带只在 `fullTier` 轮发（`:218`）、本轮未收过（`:221-224`）；`:228-230` 逐包 `player.connection.send(packet)`。
- `:252-267` `ringPackets`：一圈 48 个 `ClientboundLevelParticlesPacket`（`:253` 容量 `RING_POINTS`）；`:271-273` 每点 `count=1`、三个 `dist=0`、`overrideLimiter=true`。
- `:315-335` / `:353-366` / `:369-379` / `:382-385`：拆核清算，见 §2.2。
- `:391-399` `setRemoved`：先 `super`，`:394-396` `cleared` 为真则返回，`:397` 置 `true`，`:398` `settleOnRemoval()`。

### 1.3 `outpost/OutpostCoreIndex.java`（277 行）

- `:39` `public final class OutpostCoreIndex extends SavedData`。
- 数据结构：`:50` `private record Entry(ResourceLocation dimension, BlockPos pos) {}`；`:53` `private final Map<UUID, List<Entry>> byOwner = new HashMap<>()`；`:56` `private final Map<UUID, String> ownerNames = new HashMap<>()`。
- 索引键：外层键是**玩家 UUID 的字符串形式**（`:234` `owners.put(owner.toString(), ownerTag)`，读取 `:244` `UUID.fromString`），每条 Entry 是「维度 + 坐标」。
- 落盘名/路径：`:41` `DATA_NAME = "farmstorm_outpost_core"`；`:40` 注释写明文件是 `world/data/farmstorm_outpost_core.dat`；`:59-63` `server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(...), DATA_NAME)`。
- 加载时机：`get(MinecraftServer)`（`:59-63`）首次被调用时由 `DataStorage.computeIfAbsent` 触发；`of(ServerLevel)`（`:66-68`）转发到 `get`。调用点实测 5 处：`OutpostCoreBlockEntity.java:100`（record）、`:168`（remove）、`OutpostCoreBlock.java:81`（countOwned）、`FarmlandLedger.java:105`（countOwned）、`FarmlandGuard.java:100`（owned）。
- 落盘格式（`:209-238`）：`owners → { <uuid> → { name, entries → [ { dimension, pos } ] } }`；键名常量 `:43-47`（`owners` / `name` / `entries` / `dimension` / `pos`），`pos` 用 `BlockPos.asLong()`（`:230`）。
- `:171-187` `validated(...)`：惰性校验，丢坏条目并 `setDirty()`；`:189-202` `isStillOwnedCore`：维度取不到 → `false`；`!level.hasChunkAt(pos)` → **按有效**（`:194-196`）；方块不是 `OutpostCoreBlock` → `false`；方块实体不是 `OutpostCoreBlockEntity` 或 `ownerUuid` 不匹配 → `false`（`:200-201`）。
- `:99-113` `isOwnerOrAlly(...)`：遍历 `byOwner` 全部键 + `validated`，坐标相同则 `DeployedOwnership.isAlly`。**实测全仓无调用点**（grep `isOwnerOrAlly` 只命中定义行 `:99`）。
- `:76-82` `countOwned(Player)`：`player.getServer() == null` → `0`；否则 `get(server).count(...)`。
- 其它入口：`:119-128` `record`（`ownerNames.put(owner.getUUID(), owner.getScoreboardName())`，同坐标先删后加，`setDirty`）、`:131-144` `remove`（空列表时连 `ownerNames` 一起删）、`:147-159` `ownedIn`、`:162-164` `count`、`:240-266` `load`、`:268-276` `parseUuid`（坏 UUID 只 warn）。

### 1.4 注册处

`registry/FarmstormBlocks.java`：

- `:23` `REGISTER = DeferredRegister.createBlocks(Farmstorm.MOD_ID)`。
- `:82-89` `outpost_core` 全链：`REGISTER.registerBlock("outpost_core", OutpostCoreBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(30.0F, 1200.0F).requiresCorrectToolForDrops().sound(SoundType.STONE).pushReaction(PushReaction.BLOCK))`。
  - 硬度 30.0F（`:86`）、抗爆 1200.0F（`:86`）、`requiresCorrectToolForDrops`（`:87`）、音效 `SoundType.STONE`（`:88`）、`PushReaction.BLOCK`（`:89`）、`MapColor.STONE`（`:85`）。
  - **没有** `.lightLevel(...)`、没有 `.noOcclusion()`、没有 `randomTicks()`（实测 grep `lightLevel|noOcclusion|randomTicks|offsetType|instabreak` 在本文件只命中 `:99` `.instabreak()` 与 `:109` `.noOcclusion()`，两者都在作物/部署方块的工厂方法里）。

`registry/FarmstormBlockEntities.java`：

- `:19-20` `REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ...)`。
- `:49-51` `REGISTER.register("outpost_core", () -> BlockEntityType.Builder.of(OutpostCoreBlockEntity::new, FarmstormBlocks.OUTPOST_CORE.get()).build(null))`（单方块）。
- `:56-67` `deployedOf(DeployedKind)` 只服务 5 个部署 type，`outpost_core` 不在其中。

## 2. 耕地额度与保护

### 2.1 耕地「记账」：`farmland/FarmlandLedger.java`（275 行）

- `:42` `public final class FarmlandLedger extends SavedData`；`:44` `DATA_NAME = "farmstorm_farmland"`（注释 `:43`：`world/data/farmstorm_farmland.dat`）。
- 存储结构逐字段：
  - `:51` `private final Map<UUID, LinkedHashSet<BlockPos>> byOwner = new HashMap<>()`（插入顺序即记录顺序）。
  - `:57-58` `@Nullable private Integer baseOverride;`（指令运行期覆盖值；注释 `:53-56` 明确「刻意不进 save」）。
- NBT 键逐条：`:46` `owners`、`:47` `positions`、`:48` `pos`；写法 `:228-244`（`owners → { <uuid> → { positions → [ { pos: long } ] } }`），读法 `:246-264`。
- 落盘：`save` / `load` 两个 `@Override` 都在（`:228-244`、`:246-264`）；实例入口 `:61-64` `server.overworld().getDataStorage().computeIfAbsent(...)`，`:67-69` `of(ServerLevel)`。
- 额度算式：`:72-74` `base()`（`baseOverride` 优先，否则 `FarmstormConfig.farmlandLimitPerPlayer`）；`:82-84` `personalCapacity = base + bonusPerCore * ownedCores`；`:92-98` `sumOverMembers`（逐成员求和）；`:101-108` `capacity(level, player)` = 成员集合的额度和，`ownedCores[i] = OutpostCoreIndex.countOwned(members.get(i))`（`:105`），bonus 取 `FarmstormConfig.outpostCoreFarmlandBonus`（`:107`）。
- 已用数：`:111-121` `used(...)`，单成员取 `validated(...).size()`，多成员取坐标并集大小。
- 写入口：`:128-135` `add(level, player, pos)`（重复坐标返回 `false`，`setDirty`）；`:141-158` `remove(BlockPos)`（遍历所有归属者，空集合时删键）。
- 读入口：`:161-163` `ownedBy(level, Player)`、`:175-177` `ownedBy(level, UUID)`（不吃在线实体）、`:185-196` `members(...)`（**只用在线玩家**：`:186` `level.getServer().getPlayerList().getPlayers()`，逐个 `DeployedOwnership.isAlly`）。
- 惰性校验：`:203-219` `validated`（不是 `farmland` 就丢并 `setDirty`）；`:221-226` `isStillFarmland`（`!level.isLoaded(pos)` 时按有效处理）。
- 调用点实测（grep `FarmlandLedger\.|ledger\.`）：`FarmstormGameEvents.java:202/206/215`（锄地记账与提示）、`OutpostCoreBlockEntity.java:328/332/355/363`（拆核清算）、`FarmstormCommands.java:51-55`（改 base）、`FarmlandGuard.java:73-74/124/129`。**`ledger.remove(...)` 只有 `OutpostCoreBlockEntity.java:332` 一个调用点**（`FarmlandLedger.java:137-140` 的类注释写「耕地被挖掉 / 炸掉时当场减项」，但仓库里没有第二个调用点）。

### 2.2 「核心被摧毁 → 范围内己方耕地转泥土」：bug 1 现场

位置：`blockentity/OutpostCoreBlockEntity.java`，入口 `setRemoved`（`:391-399`）→ `settleOnRemoval`（`:315-335`）→ `doomedFarmland`（`:353-366`）→ `collectFarmland`（`:369-379`）→ `isFarmlandLoaded`（`:382-385`）。世界写入只有 `:331` 一行 `server.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL)`。

判定条件逐条抄：

1. `:394-398`：`this.cleared` 为 `true` 就返回；否则置 `true` 再清算（一次性闸）。
2. `:316`：`!(this.level instanceof ServerLevel server) || this.ownerUuid == null` → 直接返回。
3. `:319`：`corePos = this.getBlockPos()`。
4. `:320-323`：`chunk = server.getChunkSource().getChunkNow(coreX >> 4, coreZ >> 4)`；`chunk == null` → 返回（注释：区块不在内存时「无法证明方块没了 ⇒ 什么都不动」）。
5. `:324-326`：`chunk.getBlockState(corePos).getBlock() instanceof OutpostCoreBlock` → 返回（判定为「区块卸载 / 停服」）。
6. `:328-329`：`ledger = FarmlandLedger.of(server)`；`doomed = doomedFarmland(server, ledger, corePos, ownerUuid)`。
7. `:330-333`：对每个 `pos` → `setBlock(DIRT, UPDATE_ALL)` + `ledger.remove(pos)`。
8. `:334`：`this.release()`。
9. `:355`：**归属者本人一路**：`collectFarmland(server, ledger.ownedBy(server, ownerUuid), ...)`（按 UUID 读账本，离线也查得到）。
10. `:356-364`：**队友一路**：遍历 `server.players()`；`:357-359` UUID 与归属者相同则跳过；`:360-362` `!DeployedOwnership.isAlly(server, ownerUuid, ownerName, candidate)` 则跳过；`:363` 否则 `collectFarmland(server, ledger.ownedBy(server, candidate), ...)`。
11. `:371-373`：`!inRange(corePos, pos)` → 跳过（3×3 区块切比雪夫距离）。
12. `:374-376`：`!isFarmlandLoaded(server, pos)` → 跳过。
13. `:377`：加入 `LinkedHashSet`（`:354`，同一坐标只铲一次）。
14. `:383-384`：`isFarmlandLoaded` = `chunk != null && chunk.getBlockState(pos).is(Blocks.FARMLAND)`（只读 `getChunkNow`，不触发加载）。

同一清算链上的另一处事实：`OutpostCoreBlock.java:104` 在 `onRemove` 里先调了一次 `core.release()`，`:334` 又调一次；`release()` 本身（`:166-170`）只做 `OutpostCoreIndex.remove(...)`。

### 2.3 「耕地不会被踩坏」：bug 2 现场

- 判定类：`farmland/FarmlandGuard.java`，方法 `protectsFromTrample(ServerLevel, BlockPos)`（`:120-134`）。
- 逐条条件：
  1. `:121-123` 坐标处方块必须 `is(Blocks.FARMLAND)`，否则 `false`。
  2. `:124` `ledger = FarmlandLedger.of(level)`。
  3. `:125` 遍历 `level.getServer().getPlayerList().getPlayers()`（**只取在线玩家**）。
  4. `:126-128` `!inOwnRange(level, candidate, pos)` → 跳过。
  5. `:129-131` `ledger.ownedBy(level, candidate).contains(pos)` → `true`。
  6. `:133` 循环走完返回 `false`。
- `inOwnRange(level, player, pos)`（`:92-107`）自身也遍历在线玩家：`:93-96` 跳过不同维度的候选者；`:97-99` `!DeployedOwnership.isAlly(level, player.getUUID(), player.getScoreboardName(), candidate)` → 跳过；`:100-104` `OutpostCoreIndex.owned(candidate)` 里任一核心 `inRange(core, pos)` → `true`。
- 锚点事实：`OutpostCoreIndex.owned(Player)`（`:85-90`）的入参是 `Player` 实体，`!(player.level() instanceof ServerLevel)` 时返回 `List.of()`；`FarmlandGuard` 的两处判定（`:93`、`:125`）都从在线玩家列表出发。类注释 `:114-115` 自己写明「账本归属者全员离线时不受保护——`DeployedOwnership#isAlly` 的身份锚点是一个在线 `Player`」。
- 调用点：`event/FarmstormGameEvents.java:229-233` `onFarmlandTrample`，`:230` 命中则 `event.setCanceled(true)`；事件类型 `BlockEvent.FarmlandTrampleEvent`（`:229`）。

### 2.4 锄地额度闸（同一文件的另一半）

- `FarmlandGuard.isTillAction`（`:54-63`）：`stack.canPerformAction(ItemAbilities.HOE_TILL)` 且方块 ∈ {`GRASS_BLOCK`, `DIRT_PATH`, `DIRT`} 且上方 `isAir()`。
- `FarmlandGuard.check`（`:69-75`）：先 `inOwnRange` → 不中返回 `NO_CORE`；否则 `used < capacity ? ALLOW : QUOTA_FULL`。
- `Verdict` 枚举（`:38-45`）；提示键常量 `:32`（`message.farmstorm.farmland.no_core`）、`:35`（`message.farmstorm.farmland.quota_full`）。
- 调用点：`FarmstormGameEvents.java:194-217`，其中 `:206` `ledger.add(level, player, pos)`（放行时同刻记账）、`:209-211` `setCanceled(true)` + `setUseBlock(TriState.FALSE)`、`:212-216` 动作栏提示。
- 额度写入指令：`command/FarmstormCommands.java:36-43`（`/farmstorm farmlandlimit <value>`，权限 2，范围 `0..4096`，`:27-31`），`:50-57` 只调 `ledger.setBase(value)` 并回显 `command.farmstorm.farmlandlimit.set`。

### 2.5 `isAlly` / `DeployedOwnership` / 队伍判定现状

- `util/DeployedOwnership.java`：`:18-19` 两个 NBT 键常量；`:24-35` `write`（只写 `ownerUuid` / `ownerName`，无队伍字段）；`:37-45` `readUuid` / `readName`；`:48-53` `isAlly(ServerLevel, UUID, String, Player)`：`ownerUuid == null` → `false`，否则 `Farmstorm.TEAMS.isAlly(level, new TeamResolver.Owner(ownerUuid, ownerName), target)`。
- `team/TeamResolver.java`：`:13` 接口；`:16` `record Owner(UUID uuid, @Nullable String name)`；`:19` `boolean isAlly(ServerLevel, Owner, Player)`；`:22` `Optional<String> teamName(...)`。
- `team/ScoreboardTeamResolver.java`：`:14` 唯一实现；`:17-18` `FALLBACK_SELF = "self"` / `FALLBACK_PUBLIC = "public"`；`:21-25` `teamName` 按 `owner.name()` 查 `scoreboard.getPlayersTeam(name)`；`:28-40` `isAlly`：`:29-31` 同 UUID 恒真；`:32-37` owner 无队伍时返回 `FALLBACK_PUBLIC.equals(FarmstormConfig.untamedFallback())`；`:38-39` 否则比较 `target.getTeam().getName()`。
- 单例注入：`Farmstorm.java:39` `public static final TeamResolver TEAMS = new ScoreboardTeamResolver();`。
- 队伍相关配置：`FarmstormConfig` 的 `teams` 组 2 键（`resolver` 默认 `"scoreboard"` 行 `:308-310`；`untamedFallback` 默认 `"self"` 行 `:311-313`）。

## 3. 服务端 → 客户端的数据面

### 3.1 自定义网络包：0

查过的关键字（实测 grep，路径 `D:\project_farm\src`，大小写不敏感的那一轮单独标注）：

`CustomPacketPayload`、`RegisterPayloadHandlersEvent`、`PacketDistributor`、`SimpleChannel`、`NetworkRegistry`、`IPayloadContext`、`PayloadRegistrar`、`registerPayload`、`StreamCodec`、`(?i)payload`、`(?i)simplechannel`、`(?i)packetdistributor`、`(?i)networkregistry`、`ClientboundCustomPayloadPacket`、`ServerboundCustomPayloadPacket`、`SetEntityData`、`ClientboundLevelChunkWithLightPacket`。

结果：

- `StreamCodec` 命中 2 个文件，均非网络包注册：`util/TempEnchantRecord.java:8/27/37`（数据组件的 `networkSynchronized` 用）、`recipe/ConverterRecipeSerializer.java:7/33-34`（配方序列化器）。
- `(?i)payload` 命中 2 处，均为注释：`behavior/QbounceObserverSync.java:39`（说明「红线是不新增自定义网络包」）、`combat/WildHunterAura.java:179`（说明用的是原版包）。
- 其余关键字 0 命中。仓库无 `RegisterPayloadHandlersEvent` 订阅、无 `PacketDistributor` 调用。

### 3.2 实现了 `getUpdatePacket` / `getUpdateTag` 的 BlockEntity：2 个文件

- `blockentity/DeployedCropBlockEntity.java`：
  - `:455-460` `public CompoundTag getUpdateTag(HolderLookup.Provider)` → `writeSynced(tag, registries)`；
  - `:462-466` `@Nullable public Packet<ClientGamePacketListener> getUpdatePacket()` → `ClientboundBlockEntityDataPacket.create(this)`；
  - `:468-...` `writeSynced` 写 6 个同步字段（ownerUuid / ownerName / placedGameTime / pendingWaves / fuseTicks / detached，注释 `:468`）。
- `blockentity/ConverterBlockEntity.java`：`:418-428` `getUpdateTag`（只写 `input` / `catalyst` 两个键）；**没有** `getUpdatePacket`。
- 其余 BlockEntity（`OutpostCoreBlockEntity`）都没有这两个方法。
- `onDataPacket` / `handleUpdateTag` / `loadWithComponents` 覆写：实测 0 命中（走原版默认路径）。

### 3.3 被用来带自定义数据的原版 `ClientboundXxxPacket`

实测 grep `Clientbound[A-Za-z]+Packet` 命中 30 处，逐个点名（文件:行）：

- `blockentity/OutpostCoreBlockEntity.java:21`（import）、`:209`、`:228`、`:238`（注释）、`:252`、`:253`、`:270`、`:271` —— `ClientboundLevelParticlesPacket`，用于边界粒子（每圈 48 个包，`:228-230` 逐包 `connection.send`）。
- `blockentity/DeployedCropBlockEntity.java:28`（import）、`:465` —— `ClientboundBlockEntityDataPacket.create(this)`。
- `combat/WildHunterAura.java:12`（import）、`:211` —— `new ClientboundSoundEntityPacket(...)`（绑定实体的循环音）；`:179-181`、`:195` 为注释。
- `behavior/ZangiefPiledriverController.java:15`（import）、`:362` —— `new ClientboundSetEntityMotionPacket(player)`。
- `behavior/QbounceObserverSync.java:13-14`（import）、`:209`、`:217` —— `new ClientboundUpdateMobEffectPacket(holder.getId(), instance, false)`；`:227-228` —— `new ClientboundRemoveMobEffectPacket(record.entityId, FarmstormMobEffects.Q_BOUNCE)`。
- 注释里提到但未使用的包名：`effect/ZangiefPiledriverEffect.java:9`、`behavior/TrapBehavior.java:76`、`client/WildHunterLoopSound.java:17/19`、`client/QbounceClientState.java:120`。

没有 `ClientboundCustomPayloadPacket`、没有 `SetEntityData`、没有 `ClientboundLevelChunkWithLightPacket`（实测 0 命中）。

### 3.4 Mixin：0

- 实测 grep `@Mixin|mixin`（`src` 全树）→ `No matches found`。
- `build.gradle:1-5` 插件只有 `java-library`、`maven-publish`、`net.neoforged.moddev`；无 mixin 插件、无 `mixin` 配置块。
- `tools/verify_project.py:314-330` 的禁令扫描把 `@Mixin` 与 `mixins.json` 都算了，实跑结果为 `[OK] 无 Mixin / TODO / 调试残留 / 空 catch`。

## 4. 客户端 HUD 先例

### 4.1 `client/WildHunterHud.java`（77 行）

- 注册方式：`:19` `@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)`；`:40-41` `@SubscribeEvent public static void onRenderGuiPost(RenderGuiEvent.Post event)`。
- 坐标常量：`:22` `HOTBAR_WIDTH = 182`（声明但本方法未使用）；`:24` `BAR_WIDTH = 20`；`:25` `BAR_HEIGHT = 4`；`:26` `BAR_GAP = 4`；`:27` `BAR_Y_FROM_BOTTOM = 52`。
- 宽度/高度算式：`:52-53` `totalWidth = MAX_CHARGES * BAR_WIDTH + (MAX_CHARGES - 1) * BAR_GAP`；`:54` `startX = (screenWidth - totalWidth) / 2`；`:55` `y = screenHeight - BAR_Y_FROM_BOTTOM`；单条高 `BAR_HEIGHT = 4`。
- 绘制 API：全部走 `GuiGraphics#fill` —— `:60` 边框、`:61` 空槽底、`:63` 满格、`:67` 回充中的一格；颜色常量 `:30-36`（`0xFFFF4444` / `0xFF8A1A1A` / `0x60101010` / `0xFF000000`）。
- 遮挡处理：没有与其它 HUD 元素求交的逻辑；`:46` 只在 `minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null` 时直接不画。位置由 `:27` 常量与类注释 `:14-16` 的行带算式决定（快捷栏 `[h-22,h-1]`、经验条 `[h-29,h-27]`、生命/饥饿 `[h-39,h-30]`）。
- 开关：无独立开关；`:42-44` 以 `WildHunterClientState.active()` 作为唯一前置条件。无 config 项、无 keybind。
- 其它：`:71-75` 在 `LOGGER.isDebugEnabled()` 时打一条 debug 日志。

### 4.2 `client` 包下全部类（21 个，逐个点名 + 职责一行）

| 文件 | 行数 | 职责（取自类注释首句） |
|---|---|---|
| `client/ConfusionInputHandler.java` | 44 | 混乱的客户端输入反转，只反转方向分量；与 `InputLockHandler` 冲突时锁优先 |
| `client/ConverterRecipeBookCategory.java` | 24 | 消除 `Unknown recipe category: farmstorm:converting/<id>` 警告 |
| `client/ConverterScreen.java` | 86 | 转换器 GUI（贴图、文案、转换按钮） |
| `client/FarmstormClient.java` | 136 | 客户端入口，`@EventBusSubscriber(Dist.CLIENT)` 的汇合点 |
| `client/FogSuppressor.java` | 43 | 狂野猎人期间去掉迷雾 |
| `client/GammaBoost.java` | 56 | 狂野猎人期间的伽马提升 |
| `client/GrayscalePass.java` | 48 | 世界灰度，走官方 PostChain 机制 |
| `client/InputLockHandler.java` | 186 | 桑吉尔蔗的输入锁（锁定区间 = 效果三相位） |
| `client/QbounceClientState.java` | 222 | Q弹 的客户端状态机 |
| `client/QbouncePoseRenderer.java` | 83 | Q弹 的 `render` 分支渲染 |
| `client/QbounceThirdPersonController.java` | 87 | Q弹 期间的第三人称强制 |
| `client/render/TurretBulletRenderer.java` | 61 | 炮台导弹渲染（复用潜影贝导弹模型） |
| `client/WildHunterClientHooks.java` | 20 | 客户端每刻汇合点 |
| `client/WildHunterClientState.java` | 43 | 狂野猎人客户端状态缓存 |
| `client/WildHunterDashClient.java` | 208 | 狂野猎人客户端冲刺预测（含右键四向位移） |
| `client/WildHunterHud.java` | 77 | 狂野猎人充能条 HUD |
| `client/WildHunterLoopSound.java` | 88 | 循环音 `SoundInstance` |
| `client/WildHunterOutline.java` | 128 | 红色剪影（`highlightRadius` 默认 64 格内） |
| `client/WildHunterUseKeyHandler.java` | 34 | 右键四向位移的输入拦截 |
| `client/ZangiefCameraController.java` | 38 | 桑吉尔蔗相机（相位 2 起 roll 180° + pitch 取反） |
| `client/ZangiefClientState.java` | 138 | 桑吉尔蔗客户端状态快照 |

（行数实测 `[IO.File]::ReadAllLines(...,UTF8).Count`。）

### 4.3 keybind 与 CLIENT config

- `KeyMapping` 注册：**0**。实测 grep `RegisterKeyMappingsEvent` → 0 命中；`KeyMapping` 出现处只在 `client/InputLockHandler.java:4`（import）、`:132`（`options.keyHotbarSlots`）、`:135`（`options.keyDrop / keyInventory / keySwapOffhand / keyTogglePerspective`）、`:174-184`（`drain` / `drainOne` 消费原版按键的点击）。
- CLIENT 类型 config：**0**。实测 grep `registerConfig|ModConfig\.Type` 全仓只命中 `Farmstorm.java:56` `container.registerConfig(ModConfig.Type.SERVER, FarmstormConfig.SPEC);`。
- 其它客户端注册：`FarmstormClient.java:46-49` `RegisterMenuScreensEvent`（`ConverterScreen`）、`:57-60` `RegisterRecipeBookCategoriesEvent`、`:103-119` `EntityRenderersEvent.RegisterRenderers`（13 个实体类型）、`:67-72` `InputEvent.InteractionKeyMappingTriggered`、`:78-81` `ClientTickEvent.Post`、`:88-91` `RenderLevelStageEvent`、`:93-96` `RenderGuiEvent.Pre`。

## 5. 压缩干草块音效

- 注册行：`registry/FarmstormBlocks.java:73-79` `REGISTER.registerBlock("compressed_hay_block", Block::new, ...)`。
- `.sound(...)` 当前值：`SoundType.GRASS`，在 `:79`。
- 同一 Properties 链的其它值：`mapColor(MapColor.COLOR_BROWN)`（`:76`）、`strength(30.0F, 6.0F)`（`:77`）、`requiresCorrectToolForDrops()`（`:78`）。
- 全模组 `SoundType` 取值逐个点名（实测 grep `SoundType\.`，命中 4 处，全在 `FarmstormBlocks.java`）：
  1. `:79` `SoundType.GRASS` —— `compressed_hay_block`；
  2. `:88` `SoundType.STONE` —— `outpost_core`；
  3. `:100` `SoundType.CROP` —— `crop(...)` 工厂，被 15 个作物方块使用（调用点 `:26-39`、`:43`）；
  4. `:110` `SoundType.CROP` —— `deployed(...)` 工厂，被 8 个部署方块使用（调用点 `:46-66`）。
- `SoundType.STONE` 的先例：只有 `outpost_core` 一处（`:88`）。
- `converter`（`:69-70`）的注册链里没有 `.sound(...)`。

## 6. 全仓基线计数（逐条点名）

### 6.1 12 类注册对象（ID 逐个点名）

**items：32 个**

作物/物品 22：`puffed_wheat`、`blast_carrot`、`ubi_server`、`toxic_ubi_server`、`power_beet`、`melon_slice_bomb`、`melon_grenade`、`pumpkin_turret`、`awakened_torchflower`、`awakened_pitcher_plant`、`fang_trap_cactus`、`bamboo_firecracker`、`red_mushroom_puff`、`brown_mushroom_puff`、`lucky_berry`、`molotov`、`zangief_cane`、`wild_apple`、`compressed_puffed_wheat`、`friend_chives`、`point_defense_fungus`、`vibration_bean`
播种物 7：`puffed_wheat_seeds`、`power_beet_seeds`、`melon_grenade_seeds`、`pumpkin_turret_seeds`、`awakened_torchflower_seeds`、`awakened_pitcher_plant_pod`、`chive_seeds`
方块物品 3：`converter`、`compressed_hay_block`、`outpost_core`
（出处：`registry/FarmstormItems.java:36-117`；路径来自 `CropProfile`（`util/CropProfile.java:47-128`）或 `seedItem(...)` 的字面量 `:100-107`。）

**blocks：26 个**

作物方块 15：`puffed_wheat_crop`、`blast_carrot_crop`、`ubi_server_crop`、`toxic_ubi_server_crop`、`power_beet_crop`、`melon_grenade_stem`、`pumpkin_turret_stem`、`awakened_torchflower_crop`、`awakened_pitcher_plant_crop`、`fang_trap_cactus_block`、`bamboo_firecracker_stalk`、`red_mushroom_puff_block`、`brown_mushroom_puff_block`、`lucky_berry_bush`、`friend_chives_crop`
部署方块 8：`blast_carrot`、`pumpkin_turret`、`awakened_torchflower`、`awakened_pitcher_plant`、`fang_trap_cactus`、`zangief_cane`、`point_defense_fungus`、`vibration_bean`
其它 3：`converter`、`compressed_hay_block`、`outpost_core`
（出处：`FarmstormBlocks.java:26-89`。）

**block_entity_type：7 个** —— `blast_carrot`、`pumpkin_turret`、`awakened_torchflower`、`awakened_pitcher_plant`、`fang_trap_cactus`、`converter`、`outpost_core`（`FarmstormBlockEntities.java:23-51`）。

**entity_type：13 个** —— `puffed_wheat`、`ubi_server`、`toxic_ubi_server`、`melon_slice_bomb`、`melon_grenade`、`bamboo_firecracker`、`red_mushroom_puff`、`brown_mushroom_puff`、`molotov`、`compressed_puffed_wheat`、`friend_chives`、`vibration_bean`、`turret_bullet`（`FarmstormEntityTypes.java:31-66`）。

**mob_effect：4 个** —— `wild_hunter`、`zangief_piledriver`、`q_bounce`、`confusion`（`FarmstormMobEffects.java:23-36`）。

**sound_event：3 个** —— `wild_hunter_loop`、`zangief_piledriver`、`friend_chives`（`FarmstormSounds.java:20-32`）。

**recipe_type：1 个** —— `converting`（`FarmstormRecipes.java:22-23`）。
**recipe_serializer：1 个** —— `converting`（`FarmstormRecipes.java:24-25`）。
**creative_mode_tab：1 个** —— `main`（`FarmstormCreativeTabs.java:16`）。
**damage_type：3 个** —— `void_bolt`（`:20`）、`crop_blast`（`:23`）、`zangief_slam`（`:26`），声明为 `ResourceKey`，无 `DeferredRegister`（`FarmstormDamageTypes.java:14` 注释）。
**data_component_type：1 个** —— `temporary_enchantments`（`FarmstormDataComponents.java:16`）。
**menu_type：1 个** —— `converter`（`FarmstormMenus.java:16-17`）。

合计：32+26+7+13+4+3+1+1+1+3+1+1 = **93**。

对照 `tools/verify_project.py`：

- `:197-210` `EXPECTED_COUNTS` 逐项写死值为 `items 32 / blocks 26 / block_entity_type 7 / entity_type 13 / mob_effect 4 / sound_event 3 / recipe_type 1 / recipe_serializer 1 / creative_mode_tab 1 / damage_type 3 / data_component_type 1 / menu_type 1`。
- `:157-194` `EXPECTED` 里逐类列出的 ID 清单与上面 12 组逐字一致（items 32 = `CROP_IDS` 18 + 14；blocks 26；余同）。
- 实测运行（`python -X utf8 tools/verify_project.py`，用 `检查环境.cmd` 里写的解释器 `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`）：12 组全部 `[OK] … 个 ID 齐全`，`已核 registry 对象: 93 / 93`，结论 `FAIL 0 项，WARN 0 项`，`EXIT=0`。

### 6.2 `tools/verify_project.py` 里其它硬编码期望值

| 行 | 内容 | 含义 |
|---|---|---|
| `:10` | `…按集合去重后是 166 个键` | config 键总数口径 |
| `:11` | `93 个注册 ID <- docs/zh/24-TECH-0.1.0.md §2.1…（12 类，相加 93）` | 注册对象总数口径 |
| `:92` | `静默跳过会让期望集悄悄退回 155` | v0.0.3 的旧键数口径 |
| `:196` | `12 类逐类小计，相加 93` | `EXPECTED_COUNTS` 的注释 |
| `:197-210` | 12 行整数 | 见 6.1 |
| `:233` | `v0.0.3 是 3 行（139 + 16 = 155），v0.1.0 再加 1 行（155 + 11 = 166）` | 键数推导 |
| `:288` / `:300` | `print("== 2. 93 个注册 ID 逐字核对…")` / `已核 registry 对象: {total} / 93` | 输出文案里的 93 |
| `:123-128` | `rows != 12`、`len(fresh) != 11` 抛错 | 24-TECH §8.1 必须 12 行（新 11 + 改 1） |
| `:229-230` | `len(added3) != 19` 则 FAIL | 18-TECH §8.1 必须 19 行 |
| `:302-312` | 无写死数字 | lang 键数只做集合比较并打印实际条数（实测 315） |
| `:314-330` | 6 条正则 | 禁令扫描（Mixin / mixins.json / TODO / printStackTrace / `[PROBE]` / 空 catch） |

`315` / `310` 这两个数字**不在** `verify_project.py` 里，而在 `datagen/ModLangProvider.java` 的类注释：`:15`（`…= **315**`）、`:28`（`两份语言文件到 310 键、config 到 166`）、`:32`（`到 **315** 键（299 + 16…）`）；`:10` 另有旧口径 `299`。实测当前两份 lang 文件各 **315** 键（见 §8）。

### 6.3 `Farmstorm.java` 的启动自检断言（逐条抄）

`logSelfCheck()`（`:74-115`，仅打日志不抛异常）：

1. `:75` `chebyshev = DistanceMetrics.chebyshev(3, 1, 3)`；`:82` 断言 `chebyshev == 3.0`（「期望 3」）。
2. `:76` `euclidean = DistanceMetrics.euclidean(3, 4, 0)`；`:83` 断言 `euclidean == 5.0`（「期望 5.0」）。
3. `:77` `permille = FarmstormConfig.luckyBerryPermille()`，`:78-81` 求和；`:84-85` 断言 `weightSum == 1000`（「期望 1000，即 config 层百分比和 100」）。
4. `:93-102` 遍历 `CropProfile.ALL` 统计 `throwable / deployable / edible` 与 `canBreakBlocks()`；`:103-104` 断言 `CropProfile.ALL.size() == 22 && throwable == 12 && deployable == 7 && edible == 3 && demolition == 2`。
5. `:109` `poolSize = CropProfile.LUCKY_BERRY_POOL.size()`；`:110-112` 断言 `poolSize == 19` 且池中不含 `LUCKY_BERRY` / `WILD_APPLE` / `COMPRESSED_PUFFED_WHEAT`。

`logRegistrySelfCheck()`（`:123-147`）：

- `:124-135` 现数 12 类：`FarmstormItems.REGISTER.getEntries().size()`、`FarmstormBlocks…`、`FarmstormBlockEntities…`、`FarmstormEntityTypes…`、`FarmstormMobEffects…`、`FarmstormSounds…`、`FarmstormRecipes.TYPE_REGISTER…`、`FarmstormRecipes.SERIALIZER_REGISTER…`、`FarmstormCreativeTabs…`、`damageTypeCount()`、`FarmstormDataComponents…`、`FarmstormMenus…`。
- `:136-137` 求和 `total`；`:138-141` 断言 `total == 93 && items == 32 && blocks == 26 && blockEntities == 7 && entityTypes == 13 && mobEffects == 4 && soundEvents == 3 && recipeTypes == 1 && recipeSerializers == 1 && creativeTabs == 1 && damageTypes == 3 && dataComponents == 1 && menuTypes == 1`。
- `:154-157` `damageTypeCount()` 返回 `List.of(CROP_BLAST, VOID_BOLT, ZANGIEF_SLAM).size()`（= 3，不写死常量）。
- `:118` 注释另有沿革数字 `64 → 77 → 90 → 93`。

客户端侧同类自检：`client/FarmstormClient.java:125-135` `logRendererSelfCheck()`，`:126` `expected = FarmstormEntityTypes.REGISTER.getEntries().size()`，`:127` 断言 `ALL_ENTITY_TYPES.size() == expected`，`:128` 打印 `自检 实体渲染器 {}/{} → OK/FAIL`。

### 6.4 代码注释与注册数不一致的两处（事实，不作评价）

- `FarmstormBlocks.java:25` 注释写 `// ---- 14 个作物方块 ----`，该段（`:26-39`）实际注册 15 个；同文件 `:19` 与 `:20` 的类注释写的是 15 个作物。
- `FarmstormCreativeTabs.java:11` 注释写 `1 个创造模式标签页…22 个物品`；`FarmstormItems.creativeTabItems()`（`:122-129`）实际返回 `CROP_ITEMS`（22）+ `SEED_ITEMS`（7）+ 3 个方块物品 = 32 项。

## 7. config

文件：`src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java`（1052 行）。唯一注册：`Farmstorm.java:56` `ModConfig.Type.SERVER`。载体：`ModConfigSpec SPEC`（`:16`，`:928` `SPEC = b.build()`）；生成的 `run/config/farmstorm-server.toml`（763 行，实测存在）。

### 7.1 配置组与每组键数（逐组点名，合计 166）

| # | 组（`b.push`） | 键数 |
|---|---|---|
| 1 | converter | 3 |
| 2 | stack | 2 |
| 3 | deploy | 3 |
| 4 | teams | 2 |
| 5 | explosion | 2 |
| 6 | throwable | 1 |
| 7 | food | 1 |
| 8 | puffed_wheat | 2 |
| 9 | blast_carrot | 3 |
| 10 | blast_carrot.nausea | 2 |
| 11 | ubi_server | 2 |
| 12 | toxic_ubi_server | 4 |
| 13 | melon_slice_bomb | 2 |
| 14 | melon_grenade | 6 |
| 15 | pumpkin_turret | 4 |
| 16 | awakened_torchflower | 4 |
| 17 | awakened_torchflower.strength | 2 |
| 18 | awakened_torchflower.resistance | 2 |
| 19 | awakened_torchflower.haste | 2 |
| 20 | awakened_torchflower.speed | 2 |
| 21 | awakened_torchflower.water_breathing | 2 |
| 22 | awakened_torchflower.night_vision | 2 |
| 23 | awakened_pitcher_plant | 4 |
| 24 | awakened_pitcher_plant.regeneration | 2 |
| 25 | awakened_pitcher_plant.saturation | 2 |
| 26 | awakened_pitcher_plant.absorption | 2 |
| 27 | fang_trap_cactus | 9 |
| 28 | bamboo_firecracker | 2 |
| 29 | red_mushroom_puff | 1 |
| 30 | red_mushroom_puff.nausea | 2 |
| 31 | red_mushroom_puff.blindness | 2 |
| 32 | brown_mushroom_puff | 1 |
| 33 | brown_mushroom_puff.weakness | 2 |
| 34 | brown_mushroom_puff.slowness | 2 |
| 35 | lucky_berry | 6 |
| 36 | lucky_berry.regeneration | 2 |
| 37 | lucky_berry.strength | 2 |
| 38 | lucky_berry.resistance | 2 |
| 39 | lucky_berry.jump_boost | 2 |
| 40 | lucky_berry.speed | 2 |
| 41 | lucky_berry.chance | 8 |
| 42 | power_beet | 2 |
| 43 | power_beet.strength | 2 |
| 44 | power_beet.speed | 2 |
| 45 | power_beet.haste | 2 |
| 46 | power_beet.resistance | 2 |
| 47 | molotov | 3 |
| 48 | wild_apple | 2 |
| 49 | wild_hunter | 11 |
| 50 | zangief_cane | 8 |
| 51 | qbounce | 4 |
| 52 | friend_chives | 3 |
| 53 | point_defense_fungus | 3 |
| 54 | vibration_bean | 2 |
| 55 | growth | 2 |
| 56 | trap | 3 |
| 57 | farmland | 1 |
| 58 | outpost_core | 3 |
| 59 | compressed | 1 |

逐行相加：3+2+3+2+2+1+1+2+3+2+2+4+2+6+4+4+2+2+2+2+2+2+4+2+2+2+9+2+1+2 = **79**（第 1~30 组）；
2+1+2+2+6+2+2+2+2+2+8+2+2+2+2+2+3+2+11+8+4+3+3+2+2+3+1+3+1 = **87**（第 31~59 组）；
合计 79 + 87 = **166**。
（注：`b.push` 可以嵌套，例如 `blast_carrot.nausea` 是 `blast_carrot` 里的子组，两者分开计数。）

实测命令与结果：逐行解析 `b.push("…")` / `b.pop()` / `.define*("…"`，输出 `GROUPCOUNT=59 KEYS=166`；键总数 166 与 `run/config/farmstorm-server.toml` 的 166 键一致（`verify_project.py` 实跑 `[OK] 166 个配置键与文档逐字一致（toml 166 键）`）。

### 7.2 `farmland` / `outpost_core` 相关键（全 4 个，逐个抄）

| 键（全路径） | 类型 | 默认值 | 范围 | 注释（原文） | 行号 |
|---|---|---|---|---|---|
| `farmland.limitPerPlayer` | `ModConfigSpec.IntValue` | `0` | `0..4096` | 「耕地额度的服务端默认值，0 = 开局谁也耕不了地；范围 0 ~ 4096」 | push `:904`，define `:907` |
| `outpost_core.farmlandBonus` | `ModConfigSpec.IntValue` | `16` | `0..1024` | 「每个据点核心增加的耕地上限；范围 0 ~ 1024」 | `:911-913` |
| `outpost_core.maxPerPlayer` | `ModConfigSpec.IntValue` | `2` | `1..64` | 「每玩家可放置的核心数；范围 1 ~ 64」 | `:914-916` |
| `outpost_core.boundaryParticleIntervalTicks` | `ModConfigSpec.IntValue` | `40` | `20..1200` | 「边界粒子刷新间隔（刻），40 刻 = 2 秒；范围 20 ~ 1200」 | `:917-919` |

（每键的 `.translation(...)` 走 `tr()`（`:933-935`），键形如 `farmstorm.configuration.outpost_core.farmlandBonus`。）

生成的 toml 对应段（实测行号）：`[farmland]` `:741`、`limitPerPlayer = 0` `:745`；`[outpost_core]` `:747`、`farmlandBonus = 16` `:751`、`maxPerPlayer = 2` `:755`、`boundaryParticleIntervalTicks = 40` `:759`。

### 7.3 其它配置载体

- CLIENT 类型 config：**0**（见 §4.3）。
- `ModConfigSpec` 之外的载体现状：
  - `FarmlandLedger.baseOverride`（`FarmlandLedger.java:57-58`）：指令写、**不落盘**（`:53-56` 注释）。
  - `FarmstormConfig.registrationInt / registrationDouble`（`:1044-1051`）：注册期在 spec 未加载时取 `getDefault()`。
  - `FarmstormConfig.bake()`（方法起始 `:941`）与 `Farmstorm.java:59-60` 的 `onLoading` / `onReloading` 监听：把配置值烘成静态快照。
  - 没有 `GameRule`：`FarmstormCommands.java:18-20` 注释说明「不注册任何 GameRule」。

## 8. 语言键与资源

### 8.1 键总数与中英一致性

- `src/generated/resources/assets/farmstorm/lang/zh_cn.json`：317 行，**315 键**。
- `en_us.json`：317 行，**315 键**。
- 键集比对（实测 `ConvertFrom-Json` 取属性名求差）：`onlyZh=0 onlyEn=0` —— 两侧键集完全相同。
- 交叉验证：`verify_project.py` 实跑 `[OK] lang 键集一致，共 315 键`。
- 条目构成口径（源码注释）：`datagen/ModLangProvider.java:14-15` 「32 item + 26 block + 4 effect + 3 subtitles + 166 config + 36 advancement + 6 death + 16 tooltip + 13 entity + 8 gui + 1 container + 1 itemGroup + 1 command + 2 message = 315」。

### 8.2 `outpost_core` 相关语言键（逐个点名，中/英原文）

| 键 | zh_cn（行号） | en_us（行号） |
|---|---|---|
| `block.farmstorm.outpost_core` | 「据点核心」`zh:53` | `Outpost Core` `en:53` |
| `item.farmstorm.outpost_core` | 「据点核心」`zh:281` | `Outpost Core` `en:281` |
| `farmstorm.configuration.outpost_core.farmlandBonus` | 「每个据点核心增加的耕地上限」`zh:188` | `Farmland quota added per outpost core` `en:188` |
| `farmstorm.configuration.outpost_core.maxPerPlayer` | 「每玩家可放置的据点核心数」`zh:189` | `Outpost cores each player may place` `en:189` |
| `farmstorm.configuration.outpost_core.boundaryParticleIntervalTicks` | 「边界粒子刷新间隔（刻，40 刻 = 2 秒）」`zh:187` | `Boundary particle interval in ticks (40 ticks = 2 s)` `en:187` |

同一机制相关的耕地/指令键（同批被点名，均存在）：

| 键 | zh_cn（行号） | en_us（行号） |
|---|---|---|
| `farmstorm.configuration.farmland.limitPerPlayer` | `zh:145` | `en:145` |
| `command.farmstorm.farmlandlimit.set` | 「耕地额度已设为 %s（本进程内有效，重启回 config 默认）」`zh:64` | `en:64` |
| `message.farmstorm.farmland.no_core` | 「附近没有己方据点核心」`zh:296` | `en:296` |
| `message.farmstorm.farmland.quota_full` | 「耕地额度已满：%s/%s」`zh:297` | `en:297` |

- 没有 `outpost_core` 专属的 tooltip / advancement / death 键（实测 grep `outpost|farmland` 在 zh_cn 只命中上表 9 行）。

### 8.3 方块状态与模型

- `src/generated/resources/assets/farmstorm/blockstates/outpost_core.json`（7 行）：`variants` 只有一个空键 `""` → `{"model": "farmstorm:block/outpost_core"}`（`:2-6`）；无 `facing` / `axis` 等属性分支。
- `src/generated/resources/assets/farmstorm/models/block/outpost_core.json`（8 行）：`parent = "minecraft:block/cube_bottom_top"`（`:2`）；`textures` 三个键 `bottom` → `farmstorm:block/outpost_core_bottom`、`side` → `farmstorm:block/outpost_core_side`、`top` → `farmstorm:block/outpost_core_top`（`:3-7`）。
- `src/generated/resources/assets/farmstorm/models/item/outpost_core.json`（3 行）：`parent = "farmstorm:block/outpost_core"`。
- 贴图文件实测存在：`src/main/resources/assets/farmstorm/textures/block/outpost_core_bottom.png`、`outpost_core_side.png`、`outpost_core_top.png`。
- 战利品表/配方/方块标签存在：`data/farmstorm/loot_table/blocks/outpost_core.json`、`data/farmstorm/recipe/outpost_core.json`、`data/farmstorm/tags/block/{blast_proof,deployed_crops,explosion_immune}.json`（本次未逐字读内容）。

## 9. 测试 / 验证工具

### 9.1 `tools/` 逐个点名（实测 `Get-ChildItem`）

| 文件 | 行数 | 作用 |
|---|---|---|
| `tools/verify_project.py` | 338 | 静态验收：① config 键集（文档现算）vs `run/config/farmstorm-server.toml`；② 93 个注册 ID 逐字核对；③ 中英 lang 键集一致；④ 禁令扫描（Mixin / mixins.json / TODO / printStackTrace / `[PROBE]` / 空 catch） |
| `tools/verify_api.ps1` | 240 | 构建基线核对：NeoForge 21.1.x 与 ModDevGradle 2.0.x 最高版本、`Explosion#finalizeExplosion` 可见性、`FoodProperties$Builder` 成员、`PotionContents` 构造器、`Ingredient` CODEC 字段、`build.gradle` 的 `neoForge` 拼写（`:11-18`）；需要 JDK/javap |
| `tools/verify_release.ps1` | 74 | 发布一致性：tag 目标 == HEAD、release jar 的 SHA256 与本地一致、`install-notes.txt` 哈希一致、release body 提到更新段或 tag（`:30-69`）；需要 `gh` |
| `tools/convert_media.ps1` | 233 | 外部素材转码流水线：mp3 → `sounds/*.ogg`（mono 64 kbps）、图标 jpg → `textures/mob_effect/*.png`（18×18）、原画 → `textures/item/*.png`（16×16） |
| `tools/gen_textures.py` | 1183 | 程序化材质生成器（99 张：物品图标 26、作物 14×4=56 阶段图、方块 16、`gui/converter.png` 等） |

另有 `tools/__pycache__/`（目录，运行 verify_project.py 产生的字节码缓存）。

### 9.2 `.tools\gw.cmd`（47 行）到底支持什么

该脚本**没有子命令分发**：`:41` 把 `%*` 原样转发给 Gradle ——
`call "…\gradle-8.10\bin\gradle.bat" --init-script "…\init.gradle" -p "D:\project_farm" --no-watch-fs %*`。
脚本内只有三段分支/环境设定，逐条列出：

1. `:3-12` 固定环境变量：`JAVA_HOME=.tools\jdk21`、`GRADLE_USER_HOME=.tools\gradle-home`、`TEMP`/`TMP=.tools\tmp`、`JAVA_TOOL_OPTIONS=-Duser.language=en -Duser.country=US -XX:ActiveProcessorCount=2`、`NODE=<dsh runtime node>`、`JDK_PATCH_DIR=.tools\jdk-patch\classes` 并在存在 class 时设 `JDK_JAVA_OPTIONS=--patch-module java.base=…`。
2. `:14-18` 代理：探测 `127.0.0.1:8899`，未监听则隐藏启动 `.tools\neoforge-proxy.mjs`，最多等 60×250 ms。
3. `:22-38` 运行目录锁：仅当参数里含 `runServer`（`:27` `findstr /C:"runServer"`）才抢 `.tools\runserver.lock`（设了 `FS_RUN_LOCK` 时为 `runserver-<值>.lock`，`:26`）；陈旧锁判定「无 java 进程」或「锁目录 > 25 分钟」（`:32`）；重试上限 900 次（`:35`）。
4. `:43-45` `runServer` 结束（无论成败）删锁。
5. `:46-47` `endlocal & exit /b %RC%`。

因此「子命令」实际是 Gradle 任务名；`build.gradle:28-47` 声明了三个 run：`runClient`、`runServer`、`runData`（`gameDirectory = file(project.findProperty('fsRunDir') ?: 'run')`，可用 `-PfsRunDir=…` 并行隔离），另加常规 `build`。仓库内 `.cmd` 入口全部经由它：`一键构建.cmd`（`call ".tools\gw.cmd" build`）、`启动客户端.cmd`（`runClient`）、`启动服务器.cmd`（未读全文，实测存在）。

`gw.ps1`（7 行）是另一份薄包装，`JAVA_HOME` 指向 `C:\Program Files\Java\jdk-21.0.12.1`（`:2`），其余与 cmd 版相同的转发（`:6`）。

### 9.3 「跑一次就知道全绿」的自检入口

- 入口：`检查环境.cmd`（15 行）。`:10` 设 `PY=C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`、`:11` `PYTHONIOENCODING=utf-8`、`:12` `"%PY%" -X utf8 "tools\verify_project.py"`；`:14-15` 再检查 `build\libs\farmstorm-0.0.1+mc1.21.1.jar` 是否存在。
- 期望输出（脚本末尾 `:332-334`）：`结论：FAIL {n} 项，WARN {m} 项`，且 `FAIL` 非空时退出码 1，否则 0（`:334`）。
- 本次实测：`结论：FAIL 0 项，WARN 0 项`，`EXIT=0`。
- 同一入口的已知偏差（事实）：`检查环境.cmd:14` 与 `一键构建.cmd:10-11` 里写死的 jar 名是 `farmstorm-0.0.1+mc1.21.1.jar`，而 `gradle.properties:11` 是 `mod_version=0.1.0`，`build/libs`/`dist` 里现有的是 `0.0.2`/`0.0.3`/`0.0.4`/`0.1.0`（实测目录列表）。
- 系统 `python` 不可用（实测：`python --version` 命中 Microsoft Store 别名，输出 `Python was not found; run without arguments to install from the Microsoft Store…`），所以只有 `检查环境.cmd` 里那条绝对路径的解释器能跑通。

## 附：未能确认清单

1. **`converter` 方块的音效实际值** —— `FarmstormBlocks.java:69-70` 的 Properties 链没有 `.sound(...)`，取值由 `BlockBehaviour.Properties` 的默认字段决定；该默认值在原版类里，仓库内无源码（`.tools/_probe_src` 里没有 `BlockBehaviour` 的 `Properties` 内部类），本次无法从仓库取证。
2. **`OutpostCoreBlockEntity` 在客户端的实际数据内容** —— 本类未覆写 `getUpdateTag`，走原版 `BlockEntity` 默认实现；默认实现返回什么、客户端能拿到哪些字段，需要原版源码，仓库内没有。
3. **`props.pushReaction(PushReaction.BLOCK)` 与活塞交互的运行时行为** —— 只能读到注册声明（`FarmstormBlocks.java:89`），没有任何测试或日志证据。
4. **拆核清算（§2.2）在真实世界里是否按预期触发** —— 本次只做静态阅读，没有启动服务端复现；`run-*` 目录里的历史日志未逐条核对是否覆盖该场景。
5. **`FarmlandLedger.remove(BlockPos)` 是否真的「耕地被挖掉 / 炸掉时当场减项」** —— 全仓只有一个调用点（`OutpostCoreBlockEntity.java:332`，拆核清算）。其余破坏路径是否通过别的机制减项（例如惰性 `validated` 过期）本次没有找到代码证据；类注释（`FarmlandLedger.java:137-140`）与调用点数量不一致，无法从仓库判断意图。
6. **`OutpostCoreIndex#isOwnerOrAlly` 是否被反射/数据驱动调用** —— 实测纯文本 grep 0 个调用点，但无法排除运行期反射（本次未做字节码级检查）。
7. **`0.1.1` 的版本口径** —— `gradle.properties:11` 是 `0.1.0`；本次没有找到任何 `0.1.1` 的版本号、changelog 或产物，报告标题按任务书写法保留。
8. **`src/main/resources/assets/farmstorm/textures/**` 的图片来源与 `tools/gen_textures.py` 的 99 张是否一一对应** —— 本次只数了文件存在性，没有逐张比对生成脚本的输出清单。
9. **`data/farmstorm/**` 下 outpost_core 的战利品表 / 配方 JSON 具体内容** —— 本次只确认文件存在，未逐字读。
10. **`run-*` 目录（40+ 个）属于哪一次验收、各自结论** —— 未读，无法归属。
