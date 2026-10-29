# F-66 证据（v0.0.2 · 2 投掷物骨架 / 2 entity_types / 渲染器 11/11 / 注册对象 77 + V-05 收口）

HEAD: d9ed6ec（未 commit）｜ 改动 4 个文件 + 新增 2 个文件 ｜ 只动了任务书表格里的 6 个文件

## 1. 自检「注册对象 77」（服务端 run-f66，逐字）

    [29Oct2026 14:06:09.773] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 77 个：items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1 → OK

数法（不写死常量断言常量）：
- 12 类里的 11 类都是 `X.REGISTER.getEntries().size()`（`FarmstormItems/Blocks/BlockEntities/EntityTypes/MobEffects/Sounds/Recipes.TYPE_REGISTER + SERIALIZER_REGISTER/CreativeTabs/DataComponents/Menus`），现场数条目；
- `damage_types` 是数据驱动、代码侧只有 `ResourceKey`（R-02 无 DeferredRegister），这一类数的是 `FarmstormDamageTypes` 里声明的静态 `ResourceKey` 常量本身（反射取 `getDeclaredFields()` 里 static 且类型为 `ResourceKey` 的字段数）；
- 总数 = 12 类相加；任一类不等于 TECH §2.1 小计或总数不等于 77 就打 `FAIL`（打印的是数出来的真值）。

同一条也出现在客户端日志（`14:09:54.685`），说明两侧注册一致。

## 2. 客户端「实体渲染器 11/11」+ 11 个实体 ID（逐字）

    [29Oct2026 14:09:55.520] [Render thread/INFO] [farmstorm/]: 自检 实体渲染器 11/11 → OK
    [29Oct2026 14:09:55.520] [Render thread/INFO] [farmstorm/]: 自检 已注册渲染器的实体 ID：[farmstorm:puffed_wheat, farmstorm:ubi_server, farmstorm:toxic_ubi_server, farmstorm:melon_slice_bomb, farmstorm:melon_grenade, farmstorm:bamboo_firecracker, farmstorm:red_mushroom_puff, farmstorm:brown_mushroom_puff, farmstorm:molotov, farmstorm:compressed_puffed_wheat, farmstorm:turret_bullet]

抓取方式：`cmd /c ""D:\project_farm\.tools\gw.cmd" runClient -PfsRunDir=run-f66"` 真起客户端（PID 10636，窗口标题 `Minecraft NeoForge* 1.21.1`，WorkingSet 1.3 GB，日志尾部有 `Stopping!` 与 job exit 0），自检在客户端初始化阶段打印，不需要进世界。完整日志：`.work/f66-client.log`（101 行）。
顺带记录：客户端主菜单停在 `Loaded 0 entity animations` 之后、`14:10:39 Stopping!`（用 `CloseMainWindow()` 优雅关闭，gradle exit 0）。

## 3. 两条 /summon + 落地后周围方块不变（服务端 run-f66 + RCON）

### 3.1 环境事实（先说清楚，否则这条会被误读）

专用服务器**无玩家在线时区块不 entity-tick**（没有 player ticket → 区块不在 ENTITY_TICKING 状态），实测：世界时间在走（`time query gametime` 2432→2500→2568），但实体完全冻结——
`/summon farmstorm:molotov ~ ~3 ~` 之后 15 秒，实体仍停在 `Pos [0.0d, -57.0d, 0.0d]`、`Motion [0.0d,0.0d,0.0d]`、`HasBeenShot 0b`（tick 一次就会置 1）；同一坐标召唤的原版盔甲架也一动不动。

`/forceload add 0 0` 让该区块回到 entity-ticking 后，**那颗冻结的莫洛托夫立刻落地、结算并消失**（`data get entity @e[type=farmstorm:molotov] Pos` → `No entity was found`）——顺带证明骨架版「命中 → 基类 settle 只结算一次 → 无行为 → discard」这条链路是通的。

下表所有数据都是在 forceload 生效的前提下重跑的完整序列。

### 3.2 原文（RCON，控制台源位置 0,-60,0；`/summon` 的返回值）

    > summon farmstorm:molotov ~ ~3 ~
      "Summoned new entity.farmstorm.molotov\n"
    > summon farmstorm:compressed_puffed_wheat ~ ~3 ~
      "Summoned new entity.farmstorm.compressed_puffed_wheat\n"

（显示名是原始 lang 键 `entity.farmstorm.molotov`，因为这两个实体的 lang 键还没生成——见 §7。成功消息本身已证明 `farmstorm:molotov` / `farmstorm:compressed_puffed_wheat` 两个 entity_type 注册成功。）

### 3.3 方块不变（`/execute if block ... run say <标记>`，判定读服务端日志）

基线（两颗投掷物之前，3×3 地面 + 地下 + 空气，全部命中）：

    [Rcon] f66-b2-grass--1--1  f66-b2-grass--1-0  f66-b2-grass--1-1
    [Rcon] f66-b2-grass-0--1   f66-b2-grass-0-0   f66-b2-grass-0-1
    [Rcon] f66-b2-grass-1--1   f66-b2-grass-1-0   f66-b2-grass-1-1
    [Rcon] f66-b2-dirt        （0,-62,0 = minecraft:dirt）
    [Rcon] f66-b2-air         （0,-60,0 = minecraft:air）

莫洛托夫落地后（14:08:52，落地判据是同一时刻实体已消失），同样 9 格 grass + 空气仍然命中：

    [Rcon] f66-a2-grass--1--1 … f66-a2-grass-1-1（9 行全中）
    [Rcon] f66-a2-air

压缩膨胀麦落地后（14:08:56）：

    [Rcon] f66-c2-grass--1--1 … f66-c2-grass-1-1（9 行全中）
    [Rcon] f66-c2-air

**该出现的缺席项全部缺席**（日志里 0 命中）：`f66-a2-fire` / `f66-a2-hay` / `f66-a2-molotov-left` / `f66-a2-item-left` / `f66-c2-fire` / `f66-c2-hay` / `f66-c2-cpw-left` / `f66-c2-item-left`
→ 没有火、没有干草块、没有残留实体、没有掉落物；骨架版确实不接任何落点行为（F-68/F-69 的活）。

## 4. V-05 收口（`registry/FarmstormItems.java`）

```diff
     public static final DeferredItem<ThrowableCropItem> MOLOTOV = cropItem(CropProfile.MOLOTOV,
-            properties -> new ThrowableCropItem(CropProfile.MOLOTOV, projectile(CropProfile.MOLOTOV), properties));
+            properties -> new ThrowableCropItem(CropProfile.MOLOTOV, FarmstormEntityTypes.MOLOTOV::get, properties));
     public static final DeferredItem<ThrowableCropItem> COMPRESSED_PUFFED_WHEAT = cropItem(CropProfile.COMPRESSED_PUFFED_WHEAT,
             properties -> new ThrowableCropItem(CropProfile.COMPRESSED_PUFFED_WHEAT,
-                    projectile(CropProfile.COMPRESSED_PUFFED_WHEAT), properties));
+                    FarmstormEntityTypes.COMPRESSED_PUFFED_WHEAT::get, properties));
@@ …
-    private static Supplier<EntityType<? extends ThrownCropEntity>> projectile(CropProfile profile) { …懒解析… }
-    @SuppressWarnings("unchecked")
-    private static EntityType<? extends ThrownCropEntity> thrownProjectile(EntityType<?> type) { … }
-（同时删掉随之无用的 import：ThrownCropEntity / Supplier / EntityType）
```

断言（扫 `FarmstormItems.java`）：`BuiltInRegistries.ENTITY_TYPE` 0 命中、`@SuppressWarnings` 0 命中、`private static … projectile(` 0 命中、两条 `FarmstormEntityTypes.X::get` 直引各 1 处（L71 / L79）。

## 5. 硬门禁

    > Task :build
    BUILD SUCCESSFUL in 9s
    5 actionable tasks: 5 up-to-date
    exit=0 ；build 日志里 warning/WARN 行数 = 0

补充（诚实口径）：本次 `build` 的 `compileJava` 是 up-to-date，所以另跑了 `compileJava --rerun -PfsRunDir=run-f66` 强制重编译（exit 0），javac 只吐两条 `Note:`——`datagen/ModAdvancementProvider.java uses or overrides a deprecated API`（**未改动的既有文件**，与 F-66 无关）+ `Recompile with -Xlint:deprecation`。零 `warning:` 行。

`git diff --stat`：

    src/main/java/com/dashpig/farmstorm/Farmstorm.java            | 48 ++++++++++++++++++++++
    src/main/java/com/dashpig/farmstorm/client/FarmstormClient.java | 10 +++--
    src/main/java/com/dashpig/farmstorm/registry/FarmstormEntityTypes.java | 10 ++++-
    src/main/java/com/dashpig/farmstorm/registry/FarmstormItems.java      | 27 +-----------
    4 files changed, 66 insertions(+), 29 deletions(-)
    （另有 2 个新文件：entity/thrown/MolotovProjectile.java、entity/thrown/CompressedPuffedWheatProjectile.java）

其余三条自检仍在（同一次服务端启动）：`自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK`、`自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK`。

## 6. verify_project.py（V-07 口径：绿 ≠ 新对象有效）

    [OK] 117 个配置键与文档逐字一致 ；[OK] lang 键集一致，共 222 键 ；[OK] 无 Mixin / TODO / 调试残留 / 空 catch
    items 22 / blocks 20 / block_entity_type 6 / entity_type 9（= v0.0.1 的 64 个 ID 子集检查）；已核 registry 对象: 62 / 64
    结论：FAIL 0 项，WARN 0 项（exit 0）

它看不见本轮 +2 个 entity_type（也不校验各 registry 总数），因此不能当作新增对象成立的证据；上面 §1/§2/§3 才是。

## 7. 已知缺口 / 未验证

1. **lang 键未生成**：`entity.farmstorm.molotov` 与 `entity.farmstorm.compressed_puffed_wheat` 还不存在于 `src/generated/resources/assets/farmstorm/lang/zh_cn.json`（对比：`entity.farmstorm.puffed_wheat` 在 L55 有键），因此 `/summon` 的成功消息显示原始键名。属 datagen（F-96 口径），且本任务边界明令不许动 `datagen/*` —— 留给语言键任务。
2. **视觉未验证**：只验到「11 个渲染器注册成功 + 客户端初始化无崩溃」；投掷物飞行时的实际画面、物品模型是否贴图正确没看（需要投掷 / 进世界，属 F-69/F-70 的验收动作）。
3. **客户端未进世界**：停在主菜单（渲染器自检在初始化阶段打印，任务书 §客户端验收明确「不需要进世界」）。
4. `run-f66/server.properties` 不是源码：`enable-rcon=true / rcon.port=25606 / rcon.password=f66rcon / online-mode=false / level-type=minecraft\:flat / level-seed=660066`，只为取证。flat 预设解析时服务端打了一行 `No key layers in MapLike[{}]`（原版 flat 生成器设置的默认回退），不影响世界生成与本次验收。

## 8. 取证文件

- `.work/f66-server.log`（服务端完整日志，163 行）
- `.work/f66-client.log`（客户端完整日志，101 行）
- `.work/f66-build.log` / `.work/f66-compile.log`（门禁日志）
- `.work/f66-verify.log`（verify_project.py）
- `.work/f66-rcon.js`（RCON 客户端，复用 f65 的那份）
