# 任务书 M1-A · 工程脊梁（Phase 1 + Phase 2 的注册层）

> 派发者：调度 Agent。你是执行者，本任务书是你的全部边界。先读 `.work/PLAN.md` §0 与 §4。

## 0. 一句话目标

让仓库从「空骨架」变成「64 个注册对象全部在 registry 内、117 个配置键全部就位、`compileJava` 与 `runServer` 双绿」的可玩地基。本任务**不实现**任何作物玩法效果（那是 M2~M4），但**必须**让每个注册对象真实存在、能 `/setblock`、能 `/summon`、能被指认。

## 1. 必读（按顺序，最多这些）

1. `.work/PLAN.md` §0 环境事实 + §4 通用纪律（含构建命令）。
2. `docs/zh/02-TECH-SPEC.md` §2 目录结构、§3 注册体系总表（64 个 ID 逐字）、§4.1~§4.5 类契约、§4.11 配置 117 键。
3. `docs/zh/01-DESIGN.md` §3.8（config 唯一命名与数值来源，117 项的原始表格）。
4. `docs/zh/00-DECISIONS.md` §3（D-01~D-41）与 §4（15 作物注册名总表）。

不要通读 `01-DESIGN.md` 全文与 `02-TECH-SPEC.md` 全文；只读上面点名的小节。

## 2. 交付物（按文件分组）

### 2.1 主类与配置
- `src/main/java/com/dashpig/farmstorm/Farmstorm.java`：按 TECH §4.1 完整实现（8 个 `DeferredRegister` 挂 mod 总线、注册 SERVER config、`MOD_ID`/`LOGGER`/`TEAMS`、`id(String)`）。事件监听在 M2 再加，本任务可只挂 config 与注册。
- `config/FarmstormConfig.java`：**117 键全部落地**（R-01），按 `01-DESIGN.md §3.8.1/§3.8.3/§3.8.4/§3.8.5` 逐项，键名与默认值逐字照抄；每个键用 `.translation("farmstorm.configuration.<group>.<key>")`；提供 `bake()`（重建幸运浆果千分比表与爆炸阈值快照，校验 8 项概率和 = 100，不合则回退默认 + `LOGGER.warn`）。

### 2.2 共享契约（后面所有代理都依赖，签名一旦定下不得改）
- `team/TeamResolver.java`、`team/ScoreboardTeamResolver.java`：按 TECH §4.2 逐字实现（含 `Owner` record、`isAlly`、`teamName`、`untamedFallback` 口径）。
- `util/CropProfile.java`：`CropProfile.ALL` 恰好 15 条，含 `id`/`cropBlock`/`seedItem`/`kind`/`allowedFaces`/`throwStack`，另有 15 个 `public static final` 常量（`PUFFED_WHEAT` 等）供物品注册引用。
- `behavior/DeployedKind.java`：5 个枚举值（`BLAST_CARROT` / `FANG_TRAP_CACTUS` / `PUMPKIN_TURRET` / `AWAKENED_TORCHFLOWER` / `AWAKENED_PITCHER_PLANT`）。
- `util/DistanceMetrics.java`：`chebyshev` / `euclidean`。
- `util/DeployedOwnership.java`：归属读写（只 `ownerUuid` + `ownerName`，C-9）。
- `util/FarmstormTags.java`：`EXPLOSION_IMMUNE` / `DEPLOYED_CROPS` / `MODIFIED_CROPS` / `SEEDS` 四个 tag key。
- `registry/FarmstormDamageTypes.java`：2 个 `ResourceKey<DamageType>` + `DamageSource` 构造工具。
- `registry/FarmstormDataComponents.java` + `util/TempEnchantRecord.java`：`farmstorm:temporary_enchantments`，`Codec` + `StreamCodec`。

### 2.3 全部 9 个 registry 文件（64 个 ID 逐字照抄 TECH §3）
`FarmstormItems`(22) / `FarmstormBlocks`(20) / `FarmstormBlockEntities`(6) / `FarmstormEntityTypes`(9) / `FarmstormMenus`(1) / `FarmstormRecipes`(RecipeType 1 + Serializer 1) / `FarmstormCreativeTabs`(1) / `FarmstormDamageTypes`(2) / `FarmstormDataComponents`(1)。

### 2.4 被注册引用的类（本任务只做「让注册成立 + 状态与存档正确」的部分）
- `block/FarmstormCropBlock.java`：14 个作物方块共用；`age` 0~3；`randomTick` 生长；`getShape`/`codec`/`getSeedItem` 按 CropBlock 契约；**不注册 BlockItem**（D-34）。
- `block/DeployedCropBlock.java`：5 个部署方块共用；`FACE`/`FACING`/`FLOATING` 三属性；`getStateForPlacement` 的附着判定（TECH §4.4 四条规则）；`updateShape` 悬空/掉落；`getDestroyProgress`（`pumpkin_turret` 固定 `turretMiningTicks`，火把花/瓶子草非己方 0、己方正常）；`playerWillDestroy` 二次校验；`codec()`。
- `blockentity/DeployedCropBlockEntity.java`：6 个存档/同步字段（`ownerUuid`/`ownerName`/`placedGameTime`/`pendingWaves`/`fuseTicks`/`detached`，**无队伍快照**）、`setOwner`、`isAlly`、`consume()`、`serverTick`（错峰 + 冷却 + 行为分发入口；`behavior` 字段本任务保持 null，M2 再填，不得写成空方法占位）、`getUpdateTag`/`getUpdatePacket`。
- `block/ConverterBlock.java` + `blockentity/ConverterBlockEntity.java`：方块 + 容器（`Container` 接口、单槽、存档、破坏掉落）。`tryConvert` 的事务逻辑放 M4，本任务只做 `getContainerSize/getItem/setItem/removeItem/stillValid/save/load`。
- `item/ModifiedCropItem.java` / `ThrowableCropItem.java` / `DeployableCropItem.java` / `SeedItem.java` / `FoodCropItem.java`：按 TECH §4.3 的继承树与构造签名；本任务只要求**构造、堆叠数（D-16）、tooltip、`useOn` 播种分流**成立；投掷/食用效果在 M3/M4。
- `entity/ThrownCropEntity.java` + `entity/thrown/*.java`(8) + `entity/TurretBullet.java`：按 TECH §4.7 的基类契约（`onImpact` 抽象、`onHitBlock`/`onHitEntity` 在基类、`submunition` 同步、`keepAliveAfterImpact`）；各子类 `onImpact` 在 M3 实现，本任务只需让基类与 9 个 `EntityType` 成立且可 `/summon` 不崩。
- `menu/ConverterMenu.java`、`recipe/ConverterRecipe.java` + `ConverterRecipeSerializer.java`：按 TECH §4.8.3/§4.8.5 的签名与 JSON schema；`streamCodec` 必须真实实现（不可空实现）。
- `client/FarmstormClient.java`（`@EventBusSubscriber(Dist.CLIENT)` + 菜单屏注册）、`client/ConverterScreen.java`、`client/render/TurretBulletRenderer.java`：本任务只需能编译且专用服务器不加载；具体渲染在 M4/M5。

## 3. 明确不做（不要越界）
- 不实现 5 个 `behavior/impl/*`（M2）。
- 不实现 `explosion/*`（M2）。
- 不实现 `explosion`/`util/TempEnchantHandler`/`util/LuckyBerryRoller`（M2/M4）。
- 不写任何 `data/` 或 `assets/` 资源 JSON（M5 由 DataGen 生成），但 `datagen/FarmstormDataGen.java` 的骨架要建好并挂到 `GatherDataEvent`（provider 可为空列表）。
- 不改 `registry/` 之外的既有文件（`build.gradle` / `gradle.properties` / `mods.toml` / `pack.mcmeta` 已由调度者写好，不要动）。

## 4. 约束（违反即返工）
- HC-01/HC-02：117 个键名与默认值、64 个注册 ID 必须逐字一致，不接受「差不多」。
- HC-04：无 Mixin。HC-05：不加依赖。HC-07：config 键是封闭集合。
- HC-08/C-9：部署物**只存** `ownerUuid` + `ownerName`，不存任何队伍快照、不在物品组件里存 owner。
- 不写 `TODO`、不注释掉实现、不留空方法体的假实现；本任务范围外的东西**不要建文件**。
- 包结构严格照 TECH §2，不新增顶层包。

## 5. 验收（全部要能在证据里指认）
1. `cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava` → BUILD SUCCESSFUL。
2. `... gw.cmd build` → BUILD SUCCESSFUL，产出 `build/libs/farmstorm-0.0.1+mc1.21.1.jar`。
3. `... gw.cmd runServer` → 日志出现 `Done (` 且无 `ERROR`。跑起来后 `Ctrl+C` 或让进程自然结束，把 `Done (` 那一行贴回。
4. `config/farmstorm-server.toml` 生成后：`Select-String -Pattern "^\s*\w+\s*=" ` 计数 = 117，且七个通用段 `[converter]` `[stack]` `[deploy]` `[teams]` `[explosion]` `[throwable]` `[food]` 都在。**注意**：NeoForge 的 SERVER 配置在 `runServer` 首次启动时才写盘，路径为 `run/config/farmstorm-server.toml`。
5. 64 个 ID 全部可指认：贴出 `grep -c` 或 `Select-String` 的计数证据（items 22 / blocks 20 / BE 6 / entity 9 / recipe 2 / tab 1 / damage 2 / component 1 / menu 1 = 64）。
6. 全仓 `Select-String -Pattern "Mixin|mixins.json"` 命中 0。

## 6. 回报（按 `docs/zh/05-PROMPT-MAIN.md §7` 模板）
必须包含：改动文件清单、三条命令的原文与关键输出行、上面 6 条验收项逐条对应的证据、未决问题清单（API 签名没把握的一律列出来）、以及**你为后续 M2~M5 定下的契约签名**（哪些类/方法后面的人要调用）。
