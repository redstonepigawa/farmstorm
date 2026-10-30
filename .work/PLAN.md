# Farmstorm 实施计划（调度者维护）

> 本文件是本轮实施的裁决与排期记录。**上游文档（plan.txt / docs/zh/00~02）只读**（HC-06）。

## 0. 环境事实（已核实，非推测）

| 项 | 值 |
|---|---|
| JDK | `C:\Program Files\Java\jdk-21.0.12.1`（作者安装，21.0.12.1） |
| Gradle | `D:\project_farm\.tools\gradle-8.10`（8.10，本地解压，无 wrapper 下载） |
| Gradle 用户目录 | `D:\project_farm\.tools\gradle-home`（必须在工作区内，否则 native-platform 加载失败） |
| 唯一构建入口 | `cmd /c ""D:\project_farm\.tools\gw.cmd" <tasks>` |
| TLS 限制 | 系统 Schannel 不可用（SEC_E_NO_CREDENTIALS）；Java/Node 的 TLS 栈正常 |
| 站点阻断 | `maven.neoforged.net` 按 SNI 阻断；已由 `.tools/neoforge-proxy.mjs` 本地镜像代理绕开 |
| 其余依赖源 | Maven Central / Gradle Plugin Portal / piston-meta / libraries.minecraft.net 均直连可用 |

`gw.cmd` 会按需拉起镜像代理（127.0.0.1:8899），再用 init script 把 `https://maven.neoforged.net` 重写到代理。**所有构建必须走它**，直接调 gradle 会因为解析不到 NeoForge 而失败。

## 1. 裁决记录（文档内部冲突，调度者已定，不再上报）

| 编号 | 冲突 | 裁决 |
|---|---|---|
| R-01 | `03-TASKS.md` F-05 写「58 键」，而 `02-TECH-SPEC §4.11`、`01-DESIGN §3.8`、DoD-15 均为 **117 键** | 一律按 **117 键** 实现（58 是其中子集：14 通用 + 44 效果）。config 任务一次落地全部 117 项 |
| R-02 | `03-TASKS.md` F-02 说「8 个 DeferredRegister」，`02-TECH-SPEC §2` 列 9 个 registry 文件 | 8 个 `DeferredRegister` + `FarmstormDamageTypes`（只持有 `ResourceKey`，伤害类型是数据驱动的，不参与 DeferredRegister） |
| R-03 | `03-TASKS.md` F-13 要求伤害类型 JSON 手写，而 HC-07/N-10 要求 DataGen 生成 | 数据包 JSON（damage_type / recipe / loot / tags / advancement）由 DataGen Provider 生成到 `src/generated/resources`，不手写同名 JSON |
| R-04 | F-51 材质要求原版贴图，但本机只有 MC 26.3 客户端 | 按作者选择：解包本机 26.3 客户端取原版作物贴图叠加，版本差异在交付说明中登记 |
| R-05 | `01-DESIGN §3.8.5` 概率用百分比，`02-TECH-SPEC §4.12` 内部用千分比 | 配置层百分比（校验和 = 100），`bake()` 乘 10 取整为千分比，和恒为 1000 |
| R-06 | 资源包/数据包版本 | `pack.mcmeta` 用 `pack_format: 34`（1.21.1 资源包格式） |
| R-07 | 注释语言 | 代码注释与交付说明用中文（与文档集一致），标识符用英文语义命名（D-26） |

## 2. 排期（作者选择：每个里程碑停下来汇报）

| 里程碑 | 内容 | 派发方式 |
|---|---|---|
| M1 | Phase 1 + 2：骨架 + 64 注册对象 + 117 配置键 | 先 1 个「脊梁」子代理，随后 3 个并行补 M1 交付面 |
| M2 | Phase 3 + 4：爆炸/事件地基 + 5 个部署行为 | 1 个地基代理 → 4 个并行行为代理 |
| M3 | Phase 5：8 个投掷物 | 4 个并行代理 |
| M4 | Phase 6 + 7：转换器 + 食用型 | 3 个并行代理 |
| M5 | Phase 8 + 9：数据与资源 + DataGen | 4 个并行代理 |
| M6 | Phase 10：验收与发布 | 我亲自跑验收脚本 |

## 3. 文件所有权（避免并行写冲突）

并行批次内每个子代理只写自己那一段文件；`registry/` 只在「脊梁」阶段由单个代理写全，之后任何代理**不得**新增注册条目（需要新增时上报，由调度者改）。

## 4. 每个子代理的通用纪律

1. 先读任务书 + 任务书点名的文档小节，不要通读全部文档。
2. 写完立刻 `cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava`，通过才继续。收尾跑 `build`。
3. 不写 TODO、不注释掉实现、不伪造 API；签名不确定就查 TECH，仍不确定标 `TODO(api-unverified)` 并在回报里列为未决问题（HC-14）。
4. 数值、注册名、config 键名逐字照抄文档（HC-01 / HC-02），一个字母都不改。
5. 不引入 Mixin、不新增依赖、不改 `plan.txt` 与 `docs/zh/00~02`（HC-04 / HC-05 / HC-06）。
6. 回报按 `05-PROMPT-MAIN.md §7` 的模板，附命令原文与关键输出行。
## 5. 工具链定案（INFRA-01 收口，2026-10-27）

**唯一构建入口**：`cmd /c ""D:\project_farm\.tools\gw.cmd" <tasks>`（compileJava / build / runServer / runData）。
直接调 `gradle` 会同时丢掉镜像代理与 java.base 补丁，必然失败。

| 事实 | 内容 |
|---|---|
| 根因 | 本机 Windows 令牌下 `sun.nio.fs.WindowsFileSystemProvider.hasDesiredAccess()` 恒返回 false → `Files.isWritable()` 恒 false → JDK zipfs 把 zip 定为只读 → NeoFormRuntime 的 `recompile` 写第一个 class 就抛 `ReadOnlyFileSystemException`（无 message，日志显示 `: null`） |
| 修法 | 注释该处 throw，编成补丁 class，经 `JDK_JAVA_OPTIONS=--patch-module java.base=...` 注入 |
| 落点 | 全部在 `.tools/`（`gw.cmd` + `jdk-patch/` + `README.md`）；`build.gradle` / `settings.gradle` / `src/` 零引用 |
| 副作用 | 每个 JVM 往 stderr 打一行 `NOTE: Picked up JDK_JAVA_OPTIONS: ...`，纯噪声 |
| 换正常机器 | 删 `gw.cmd` 里那两行 `JDK_PATCH_DIR` 即可（`.tools/README.md` 有写） |
| 笔误修正 | `gradle.properties` 的 `loader_version_range` 由 `[4,` 改回 `[4,)`（依据 TECH §1.1；这是调度者初次落文件时的笔误） |
| `run/eula.txt` | 新文件（`eula=true`），`runServer` 必需；`run/**` 不进交付物 |

已验证：`build` 连续三次成功（1m13s / 10s / 11s），`runServer` 起到 `Done (14.732s)`；
`runData` 任务图 dry-run 全通，实跑等注册层落地后复验。
## 6. M1 验收与裁决（2026-10-27）

**M1 出口证据（调度者独立复跑，非子代理陈述）**：
- `gw.cmd build` → `BUILD SUCCESSFUL in 9s`；`build/libs/farmstorm-0.0.1+mc1.21.1.jar` 102,454 B；46 个 `.java`。
- `gw.cmd runServer` → `Done (11.632s)!`，日志 `[*/ERROR]` / `[*/FATAL]` = 0。
- `tools/verify_project.py` → FAIL 0：**117 个配置键与文档逐字一致**（文档口径现算 vs `run/config/farmstorm-server.toml`），64 个注册 ID 字面量齐全，无 Mixin / TODO / 调试残留 / 空 catch。

| 编号 | 事项 | 裁决 |
|---|---|---|
| R-08 | `stack.*` 只在注册期生效（物品堆叠数不可变，SERVER 配置启动后才加载） | **接受现状**。NeoForge 注册与 SERVER 配置加载时序的硬约束，不为它改架构。默认值严格等于 D-16（16/64），117 键仍在（DoD-15 不受影响）。写进交付说明的已知偏差。 |
| R-09 | `ThrownCropEntity#onImpact` 不是 `abstract`（TECH §4.7 要求 abstract） | **接受**。M3 在 8 个子类重写；基类默认 = 「无额外效果」，是合法默认而非空实现。 |
| R-10 | `playerWillDestroy` 在 1.21.1 不能取消破坏 → 改落 `IBlockExtension#onDestroyedByPlayer`；`getDestroyProgress` 是 4 参 protected；`PistonBehavior` 实为 `PushReaction`；`defineInList` 传 `List.of` 会 NPE | **以真实 API 为准**（HC-14 / TECH §10.2「以构建时实际拉取为准」）。登记为文档与实现的差异，**不改上游文档**（HC-06）。 |
| R-11 | 客户端 `isAlly` 恒 true（HC-03：客户端不做归属判定） | **接受**。后果仅是挖掘进度条外观；服务端 `onDestroyedByPlayer` 否决，功能正确。写进交付说明。 |
| R-12 | 作物土壤沿用 `CropBlock` 仅耕地，未做每作物土壤差异（DESIGN §3.2.2 的原版参照） | **推迟到 M5**，与 DataGen 一起处理。 |
## 7. M4-C 带来的三条结论（2026-10-27）

| 编号 | 事项 | 结论 |
|---|---|---|
| R-13 | M4-C 报告「M1 两处 `@EventBusSubscriber` 挂错总线（默认 GAME），`GatherDataEvent` / `RegisterMenuScreensEvent` 不会触发」 | **不成立，调度者已实测证伪**：在 `FarmstormDataGen#gatherData` 里放一条 `LOGGER.info`，分别用 `bus = Bus.MOD` 与**不带 `bus`** 各跑一次 `runData`，两次钩子都触发 1 次 → NeoForge 21.1.213 已按事件类型自动路由。另 `bus` 标了 deprecated-for-removal，带上会新增 4 条 deprecation 警告（违反 DoD-2）。**最终保留不带 `bus` 的写法**，原代码正确。 |
| R-14 | reload 监听器不调 `barrier.wait(...)` 会让整次 `/reload` 挂死（`SimpleReloadInstance.preparingListeners` 永不空，服务端起不到 `Done`） | 采纳为**硬规矩**：本仓库任何新增 `PreparableReloadListener` 必须 `barrier.wait(Unit.INSTANCE).thenRun(...)`。M4-C 已按此实现并有实测对照（坏版本卡死 / 修好后 `Done (6.801s)`）。 |
| R-15 | 并行子代理同时 `runServer` 会撞 `run/world/session.lock`，并互相删 `run/logs/latest.log` | 采纳为**硬规矩**：并行批次里每个子代理跑服务端必须自带独立 `level-name` 与 `server-port`（例：`-PfsLevel=world-m2a -PfsPort=25598`，或直接改 `run/server.properties` 再还原）。调度者的串行验收用默认值。 |
## 8. M2-A 裁决（2026-10-27）

| 编号 | 事项 | 裁决 |
|---|---|---|
| R-16 | RT-05 ③「登录时与 playerdata 存档模式比对」不可实现（登录后两者恒等） | **接受现实现**：内存账本 + 登出即还原 + 登录对账（同一次运行内重连可救）。要严格实现需独立持久字段，与 D-18「不写存档」直接冲突，不为它破 D-18。写进交付说明。 |
| R-17 | `Explosion` 无 10 参构造器 / `shouldBlockExplode` 在 `ExplosionDamageCalculator` / `ignoreExplosion` / `EnchantmentHelper#getItemEnchantmentLevel` 已废弃 / 服务端 `Level#addParticle` 空实现 | **以真实 API 为准**（HC-14），登记不改文档。 |
| R-18 | `blast_proof` 是 TECH §2 之外的第 5 个 tag（RT-09 要求给整合包作者豁免口子） | **保留**。tag 不是 registry 对象，不破坏「64 个注册对象」口径。 |
| R-19 | 心跳日志开关刻意不进 config（117 键被 HC-01 锁死） | **接受**。用 `-Dfarmstorm.debugHeartbeat=true` 或运行期 setter。要进 config 必须先回写 DESIGN §3.8 变 118 键，成本远大于收益。 |
| R-20 | `BlastRule.UNBREAKABLE_SKIP` 与 `VANILLA` 数值相同 | **保留两个常量**，与 TECH §4.9 逐字对齐；语义差别写在注释里。 |
## 9. 运行期硬规矩与基础设施补丁（2026-10-27 晚）

| 编号 | 事项 | 结论 |
|---|---|---|
| R-21 | 并行子代理同时 `runServer` 会撞 `run/world/session.lock`，并互相覆盖 `run/logs/latest.log`（实测两次让某个代理的 JVM 报 FATAL） | **已在 `.tools/gw.cmd` 内实现目录锁**：任务串里含 `runServer` 时先原子 `mkdir .tools/runserver.lock` 拿到独占，跑完释放（最长等 900×2s）。仍需各自用独立 `level-name` 避免世界状态互相污染。 |
| R-22 | `build.gradle` 的 `runs.data` 缺 `programArguments`（只有 `data()`），导致 `runData` 日志 `Initializing Data Gatherer for mods []` 且 `written: 0` | **已修**（调度者亲自落盘）：补 `--mod <id> --all --output src/generated/resources --existing src/main/resources`。实测 `mods [farmstorm]`、`written: 10`。 |
| R-23 | DataGen 生成的 damage_type JSON 省略 `"effects":"hurt"` | **接受**：`DamageType.DIRECT_CODEC` 用 `optionalFieldOf("effects", HURT)`，与默认值相同的可选字段会被省略，原版同一位置也省略。语义等价，禁止为此手写 JSON（R-03）。登记为「与 TECH §6.7 的字面差异」。 |
| R-24 | 部署限流闸门放在 `DeployedCropBlock#getStateForPlacement` 返回 null（M2-B 提出） | **批准**：F-25 的「不消耗物品 / 不产生掉落」只有放置前拦截才成立，回滚方案必然产生掉落。方块侧只加一行判定，闸门本体仍在 BE。 |
## 10. M5-1 裁决（2026-10-27）

| 编号 | 事项 | 裁决 |
|---|---|---|
| R-25 | 61 个植物模型加了 `"render_type": "minecraft:cutout"`（TECH §6.3 未写） | **批准**。全仓无 `ItemBlockRenderTypes` 注册，不加则透明区变实心黑块。NeoForge 支持模型 JSON 的 render_type 且 `minecraft:cutout` 已预注册；后续加客户端注册也不冲突。 |
| R-26 | 转换器模型未 `parent: cube_bottom_top`，改用 `block/block` + 内联 element | **批准**。vanilla `cube_bottom_top` 没有 front 面，且材质只有 top/side/front 三张（无 bottom），照字面写会得到「无正面 + 死引用」。 |
| R-27 | 部署方块几何取 `block/cross`；悬空态 y +90°；y 旋转用 vanilla 口径 north=0（不用 `Direction#toYRot()`，后者差 180°） | **批准**，均有实测依据（`rotationY` 断言 90 的倍数；client jar 的 furnace/orientable 口径）。 |
| R-28 | 87 张材质里 7 张不被任何模型引用（6 张 §6.4 明令不画图 + 1 张 GUI 背景） | **登记为文档口径差异**，不算缺陷。 |
## 11. 语言键 218 vs 文档 216（已核实，非缺陷）

TECH §7.1 的 216 键算法是 `22 item + 20 block + 9 entity + 1 itemGroup + 1 container + 8 gui + 10 tooltip + 4 death + 24 advancement + 117 config`。
实测生成的两份语言文件都是 **218 键**，逐前缀核对：`item 22 / block 20 / entity 9 / itemGroup 1 / container 1 / gui 8 / tooltip 12 / death 4 / advancements 24 / farmstorm(config) 117`。
差值 **+2** 全部来自 `tooltip`：TECH §7.1 把 `.type.<kind>` 记作 **1 个键**，但实际有 3 个 kind（`trap` / `turret` / `aura`），故 10 → 12。
**裁决 R-29**：按实现取 3 个键（218 总数），登记为「TECH §7.1 计数口径与实现口径差 2」，不改文档、不删键。
## 12. M4-D / M5-2 裁决（2026-10-28）

| 编号 | 事项 | 裁决 |
|---|---|---|
| R-30 | 转换的**反向**是否收 3 点经验：TECH §4.8.5 的 `reverse_xp_cost` 默认值是 0（并注「D-12：反向免费」），而 DESIGN §3.1.5 #2 写「每次转换扣 3 点」，D-12 首句也是「每次转换扣 3 点经验值」 | **判反向也收 3 点**，M5-2 显式写 `reverse_xp_cost: 3` 是对的。理由：D-12 首句与 DESIGN §3.1.5 都是无限定方向的「每次转换」；D-12 里的「反向免费」出现在**下界之星**那一句的语境里（DESIGN §3.1.5 #4「下界之星只对两个作物生效，且只在正向」）。TECH 的 0 只是字段默认值 + 一句误归属的括注。字段本身保留，数据包作者仍可覆写。 | 
| R-31 | D-18 第①级「90 秒硬还原」计时器 M2-A 没接线，M4-D 把它放在 `LuckyBerryRoller`（`@EventBusSubscriber` + `PlayerTickEvent.Post`）而不是 `event/FarmstormGameEvents` | **接受现位置**。行为等价、已实测（FakePlayer：CREATIVE→20 刻后 SURVIVAL，重复抽中不叠加，下线即还原）。账本仍在 `FarmstormGameEvents`，没有第二份状态。 |
| R-32 | 8 个进度没有原版触发器，需要服务端 `award(...)`，而全仓 `grep award(` = 0 | **M6 必须补**。清单以 `ModAdvancementProvider.AWARD_REQUIRED` 为准：`first_conversion` / `nether_star_toll` / `first_throw` / `demolition` / `turret_hit` / `aura_bath` / `fang_dance` / `lucky_jackpot`。 |
| R-33 | 1.21.1 API 差异（`ChestBlockEntity#setLootTable` 单参 + `setLootTableSeed`；`Creeper` 无 `setPowered`；`MobEffects` 实名 `DAMAGE_BOOST/DAMAGE_RESISTANCE/JUMP/MOVEMENT_SPEED/DIG_SPEED`；`finishUsingItem` 必须 `super` 先行） | 以真实 API 为准（HC-14），登记不改文档。 |
## 13. M3-B 裁决（2026-10-28）

| 编号 | 事项 | 裁决 |
|---|---|---|
| R-34 | 膨胀麦用**内禀** `Block#getExplosionResistance()` 判 3.6e6 地板，因此不查 `#farmstorm:explosion_immune`（火把花/瓶子草会被 hay 覆盖） | **批准**。D-14 管的是「爆炸」，膨胀麦是**方块替换**不是爆炸；DESIGN §7.11 明确「膨胀麦会覆盖部署方块（包括火把花/瓶子草）」。用 context 敏感抗性反而会让这两者挡住膨胀麦，与 §7.11 直接冲突。 | 
| R-35 | 膨胀麦落点方块靠「命中点沿飞行方向反推 0.1 格」得到（基类 `onImpact` 只给 Vec3，不给 `BlockHitResult`） | **接受**。棱/角退化时可能偏 1 格，属已知精度限制；若要彻底解决需改基类签名，收益不抵返工。 | 
| R-36 | `melon_grenade.horizontalJitter` 按 `rand(-jit, jit)` 实现（DESIGN §4.7 写「各取正值 [0,0.3]」） | **接受现实现**（config 名即「上界」语义，双向抖动更符合「散开」直觉）。登记为文档与实现的差异。 | 
## 14. F-33 两条口径的处置（2026-10-28，调度者亲自实施）

DESIGN §7.6 L1053 原文：陷阱与炮台只锁定**非己方且非创造模式**的玩家；L1060：旁观者不被光环覆盖。M6-A 核对后发现两处未实现，处置如下：

| 项 | 结论 |
|---|---|
| F-33 ③ 创造玩家不触发陷阱 / 不被炮台锁定 | **已修**（3 处一行判定）：`BlastCarrotTrap:42`、`FangTrapCactusTrap:40`、`PumpkinTurret:87` 各加 `player.isCreative()` 跳过。DoD-08 明确要求这一条。 |
| 旁观者不被光环覆盖 | **已补**：`TorchflowerAura.apply` / `PitcherPlantAura.apply` 开头加 `isSpectator()` 早退（DESIGN §7.6 L1060）。 |
| F-33 ④ 创造玩家挖 `pumpkin_turret` 仍 10 秒 | **登记为已知偏差，不实现**。方法层已满足（`getDestroyProgress` 恒返回 `1/(deployTurretMiningTicks)`、无模式分支），但 1.21.1 在算进度**之前**就分流：`ServerPlayerGameMode#handleBlockBreakAction` 对创造直接 `destroyAndAck`，客户端 `MultiPlayerGameMode#startDestroyBlock` 同理。要真做到必须在 `onDestroyedByPlayer` 里做「起始刻 → 到时放行」的有状态否决，属于新增机制且易与 RT-02 的纯函数约束打架；DoD-09 的原始口径（「pumpkin_turret 固定 10 秒」）不含创造模式。 |
| `demolition` 覆盖偏宽（任何本模组爆炸破坏 ≥1 方块即授予，含胡罗贝陷阱与幸运浆果小爆炸） | **接受**。`FarmstormExplosion` 拿不到「来源是不是投掷型作物」的信号；收窄要动 6 个调用点，收益不抵风险。登记。 |
## 15. 作者实测反馈的三条外观/崩溃问题（2026-10-28）

| 编号 | 问题 | 处置 |
|---|---|---|
| R-37 | **投掷物一扔就崩客户端**：`NullPointerException ... "entityrenderer" is null` at `EntityRenderDispatcher.shouldRender`。根因：`FarmstormClient` 只注册了 `turret_bullet` 的渲染器，8 个投掷物实体类型一个都没有。 | FIX-1：给全部 9 个实体类型注册渲染器（8 个用 `ThrownItemRenderer`）。**这是本次唯一一类只有真客户端才能发现的缺陷**——本机无客户端，`runServer` 全绿也覆盖不到渲染路径。 |
| R-38 | 部署方块放下后看不见 / 外观怪 | 按作者补充的口径分两类：**陷阱类（胡罗贝、仙人掌）世界里完全隐形**（空模型，仅保留 `particle` 贴图）；**炮台/光环类（man瓜、火把花、瓶子草）用原版方块模型**。 |
| R-39 | man瓜 / 瓜！手持图标怪异（用了扁平侧面贴图） | 两个 item 模型直接 `parent` 到 `minecraft:block/pumpkin` / `minecraft:block/melon`，手持与投掷物都渲染成 3D 原版方块。 |
| R-40 | 陷阱隐形后**物品图标不能一起隐形** | 陷阱两件物品的 item 模型必须改为 `item/generated` + `layer0: farmstorm:item/<id>`，不得再 `parent` 到空方块模型。 |
## 16. FIX-2 裁决（2026-10-28）

| 编号 | 事项 | 裁决 |
|---|---|---|
| R-41 | 瓶子草用 `minecraft:block/pitcher_crop_top_stage_4`（整格几何）而非 `pitcher_plant_top`（几何 y=-5~11，会下沉 5px 插进下方方块） | **批准**。两者贴图相同、都是原版模型；整格几何才适合单独占一格的部署方块。 |
| R-42 | 7 张 PNG 变为无模型引用（3 张 block + 4 张 item 的部署方块贴图） | **保留不删**。它们仍是 `textures.particle` 与被替换外观的来源，删了将来换回反而要重跑脚本。按 R-28 归类为「文档口径差异」。 |
| R-43 | `render_type` 用「写在自家模型 JSON 里」而不是 `ItemBlockRenderTypes` 注册 | **批准**。有源码证据（`ExtendedBlockModelDeserializer:66-68` 证明不随 parent 继承；`NamedRenderTypeManager:44-50` 证明 `minecraft:cutout` 已预注册），且与 FIX-1 在 `client/` 包的改动零冲突。 |
## 17. 第二轮实测反馈的修复（2026-10-28）

| 编号 | 问题 | 根因 | 修复 |
|---|---|---|---|
| R-44 | 炮台/光环类部署方块放下后仍然看不见（换原版模型也没用） | **不是材质问题**：`DeployedCropBlock extends BaseEntityBlock`，父类 `getRenderShape()` 默认返回 `RenderShape.INVISIBLE`（假定你自己挂 `BlockEntityRenderer`），方块模型根本不渲染。`ConverterBlock` 有这一行、`DeployedCropBlock` 从 M1 起就漏了。 | 加 `getRenderShape → RenderShape.MODEL`。隐形陷阱不受影响：它们走 MODEL + 空模型（无 elements）。 |
| R-45 | 转换器 GUI 完全错乱 | `GuiGraphics.blit` 的 **6 参重载把纹理尺寸硬编码成 256×256**（javap 字节码 `sipush 256 / sipush 256`），我们的 `converter.png` 是 176×166 → 只采源图左上 (121.0,107.6) 再放大铺满，背景与槽位框整体错位（快捷栏框 dy+75 直接掉出面板）。**排除了槽位坐标不匹配的怀疑**：Pillow 程序化找到 PNG 里 38 个 16×16 槽位区，与 `ConverterMenu` 的 38 个 Slot 坐标逐像素相等（最大偏差 0）。 | `ConverterScreen.renderBg` 改 9 参 blit（显式 176×166 UV）；补 `inventoryLabelY` 与「物品栏」标签；提示语超宽时不画。贴图侧补标题/背包刻线与输入→预览箭头，其余 86 张 PNG md5 不变。 |
| R-46 | 客户端侧问题只能靠真客户端发现 | 本机在 FIX-1 之前从未跑过 `runClient`，`runServer` 全绿也覆盖不到渲染路径。 | 已建立可复用的客户端验证手法：`runClient` + 临时 init script 注入 `--quickPlayMultiplayer` 连本机 dev 服务端 + RCON 驱动 + `Screenshot.takeScreenshot` 截图。**以后所有客户端改动都必须做到截图级验证**。注意：`--args` 在本仓库不可用（会被当 devlaunch 主类），必须走 init script 在 doFirst 里改 `clientRunProgramArgs`。 |
## 18. 型 / 类 两维建模与 FIX-7 裁决（2026-10-28）

作者提醒：**「型」和「类」不是一回事**。`01-DESIGN.md` §2.2 类型（Type，单值：投掷型/部署型/种植型/食用型）与 §2.3 类别（Category，多值：陷阱类/炮台类/破障类/buff类/建造类/伤害类）是**两个正交维度**，§2.4 明文如此，DEC §4 也是两列分开。此前实现只有 `DeployedKind`（部署行为实现种类），既不是型也不是类，属于建模缺失。

| 编号 | 事项 | 裁决 |
|---|---|---|
| R-47 | 新增 `CropType`（单值）与 `CropCategory`（多值集合），`CropProfile` 同时持有两个**独立**字段，互不推导；`DeployedKind` 保持为「部署行为怎么实现」，不并入任何一维 | **采纳**。种植型按 D-10 不赋值；15 条按 DEC §4 两列逐条填。 |
| R-48 | **瓜片子增加「破障类」标签**（作者 2026-10-28 指定） | **采纳**。DEC §4 原文仍只有「伤害类」，属作者口径变更，登记不改文档。破障类因此为 4 种：育碧 / 毒育碧 / 瓜！ / 瓜片子。 |
| R-49 | tooltip 分隔符新增第 7 个键 `tooltip.farmstorm.category_separator` | **批准**。中英分隔符只能由语言文件给出（公共代码读客户端语言会在专用服务端加载客户端类）。语言键总数 218 → **222**（删 3 个 `tooltip.farmstorm.type.*`，加 6 个 `tooltip.farmstorm.category.*` + 1 个分隔符）。 |
| R-50 | 删除 `DeployedKind#tooltipCategory` | **批准**。它指向被删的 3 个语言键，留着就是悬空方法。任务书里「DeployedKind 保持不动」的本意是「不要并进类别模型」，不是「一个成员都不许动」。 |
| R-51 | 启动自检增加分类哨兵 | **采纳**（调度者亲自加）：`自检 作物 15 种：投掷型 8 / 部署型 5 / 食用型 2；破障类 4 → OK`。一行 INFO，数字一变就说明 DEC §4 那张表被动过。 |

**FIX-7 的核心验收（证明行为真由标签驱动）**：同一场景跑三次，唯一改动是 `CropProfile` 里瓜片子的一行标签——
- 带 `DEMOLITION`：炸掉 **26** 块泥土；
- 去掉 `DEMOLITION`：**0** 块（同批育碧服务器仍破 102 块，证明不是全关）；
- 改回：**23** 块；
期间 6 个爆炸调用点 + `BlastRule` + `CropProfile` 的 MD5 前后完全相同。
## 19. FIX-8 裁决（2026-10-28）

| 编号 | 事项 | 裁决 |
|---|---|---|
| R-52 | 胡罗贝伤害退化为 1 点：爆炸中心 `pos.getCenter()` 埋在陷阱方块内部 → `Explosion#getSeenPercent` 归零 → 公式保底 `+1.0` 生效 | **修根因**：`BlastCarrotTrap` 把 `be.consume()` 提到 `ExplosionQueue.submit()` **之前**，让爆炸执行时方块已经是空气。实测 1.0 → **7.0**（= `blast_carrot.maxDamage`）。 |
| R-53 | 调度者建议的「把中心挪到方块顶面」被实测否掉 | **采纳子代理结论**：顶面中心 seen 只有 0.4（顶面是碰撞箱边界，射线仍被自己挡掉大半），按公式贴脸仅 ≈5.56 < cap。**文档保留这个否定记录**，避免以后有人再试同一招。 |
| R-54 | 防御层：`hurtCapped` 在 `seen <= 0` 时把**取样中心**沿 +Y 抬到最近空格重算 | **采纳**。抬升最多 4 格、边界 +1e-3 为内部常量；距离衰减、raw 公式、`min(raw, cap)` 逐字未动，**没有引入常数加权**。实证：中心埋石头时伤害 6.1975546，与手算 0.7205×0.4 精确吻合。 |
| R-55 | 蘑菇落地特效用 `ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r,g,b)` | **采纳**（1.21.1 已无 `EntityEffectParticleOption`）。红 `0xC03030`×32 + SMOKE×12、棕 `0x8B5A2B`×28 + SMOKE×10，扩散半径复用各自 `radius` config（改半径特效随动），粒子数为内部常量，**未新增 config 键**（117 保持）。 |

**教训登记**：FIX-6 的探针日志里其实已经写着「胡罗贝 0.94 / 1.00」伤害，调度者当时按「伤害生效」放行，没有拿它跟 `maxDamage = 7` 核对——同一个数字既是「生效」的证据也是「退化」的证据。今后所有伤害类验收必须**同时给出实测值与 config 上限的比对**。
## 20. v0.0.2 施工期裁决（2026-10-29 起）

> 0.0.2 的决策记录是 `docs/zh/10-DECISIONS-0.0.2.md`（D-49~D-79）与 `13-TASKS-0.0.2.md` §0.4（A-01~A-09）。
> 本节只记**这二者没有覆盖、施工时新冒出来**的问题。累积几条后再统一回写文档，避免每发现一条就动一轮中英同步。

| 编号 | 冲突 | 裁决 |
|---|---|---|
| V-01 | `12-TECH §3.1` 要求 `ZANGIEF_CANE` 档案的 `kind = DeployedKind.ZANGIEF_CANE`，但该枚举值是 **F-63** 的交付物，而 F-63 依赖 F-62 —— F-62 施工时该值不存在，引用即编译不过 | **F-62 先留 `kind = null`**（`CropProfile` 的 `kind` 本就是 `@Nullable`，`melon_slice_bomb` 是先例），并在常量 javadoc 写明「待 F-63 回填」；`type` 仍是 `DEPLOYABLE`，故自检「部署型 6」不受影响。**F-63 必须同时回填 `CropProfile.ZANGIEF_CANE` 的 `kind` 字段**——否则 F-65 注册部署方块时 `DeployedCropBlock:60` 的 `byKind(...)` 会抛 `IllegalArgumentException`（已核实该调用链）。 |
| V-02 | `LUCKY_BERRY_POOL` 在 F-62 就变成 16 条，含 `molotov` / `zangief_cane`，但这两件的物品要到 **F-65** 才注册 | **接受这个过渡窗口**（F-62 → F-65 之间没有可分发的产物）。`LuckyBerryRoller:176` 的 `FarmstormItems.forProfile(profile).get()` 在 F-65 之前若被掷中会抛异常。**F-65 的验收追加一条**：16 条池逐项 `forProfile(p).get()` 不抛异常，且掷一次幸运浆果的「生成随机作物」分支能正常产出。 |
| V-03 | F-63 原任务书要求「把 `ZANGIEF_CANE` 方块加进既有 `BlockEntityType.Builder` 的方块列表」，但该方块是 **F-65** 的交付物；且 `DeployedBehaviors` 的穷尽 switch 需要一个行为单例，而 `ZangiefCaneTrap` 按 F-86 才是交付物 | **F-63 = 枚举值 + `CropProfile.kind` 回填 + `ZangiefCaneTrap` 最小骨架 + `DeployedBehaviors` case + `FarmstormBlockEntities.deployedOf` case**（后者只加 `case ZANGIEF_CANE -> FANG_TRAP_CACTUS.get()`，不碰 builder 方块列表）。**builder 方块列表的那一行移交 F-65**；**F-86 接手时把骨架替换为真实 `TrapBehavior` 实现**。 |
| V-04 | F-63 任务书写「`findTrigger` 返回 `null`」，但 `TrapBehavior#tick` 是 `findTrigger(be).ifPresent(...)`；且 `13-TASKS` 把 `ZangiefCaneTrap` 写在 `behavior/` 而既有 5 个实现都在 `behavior/impl/` | **返回 `Optional.empty()`**（调度者任务书笔误，子代理纠正正确）。**类移到 `behavior/impl/`**（与既有 5 个实现和 0.0.1 的 `03-TASKS` 惯例一致，`13-TASKS` 的路径是笔误）。顺带修 `CropProfile.byKind` 的 javadoc 计数 5 → 6。 |
| V-05 | `molotov` / `compressed_puffed_wheat` 的物品要 `Supplier<EntityType>`，但这两个 entity_type 是 F-66 的交付物 → F-65 用了「首次投掷时按注册 ID 从 `BuiltInRegistries.ENTITY_TYPE` 懒解析」+ 一处 `@SuppressWarnings("unchecked")`。**与既有 8 个投掷物的 `FarmstormEntityTypes.X::get` 直引写法不一致** | 能跑但属临时态。**F-66 改为直引**（`FarmstormEntityTypes.MOLOTOV::get` / `COMPRESSED_PUFFED_WHEAT::get`），并删掉 `FarmstormItems.projectile(profile)` 辅助方法与那处 `@SuppressWarnings`。 |
| V-06 | `wild_apple` 注册为 `FoodCropItem` 但**没有 `.food(...)`**，当前不可食用（营养/饱和度键在 F-61，`FoodProperties` 与授予效果按 13-TASKS 归 F-77） | 接受这个中间态（注册存在、`/give` 可用、暂不可吃）。**F-77 收口**：补 `FoodProperties` 并验「吃下后营养恢复 + 授予 `wild_hunter`」。 |
| V-07 | `tools/verify_project.py` 的注册 ID 段是**子集检查**（`missed = [i for i in ids if f'"{i}"' not in all_java]`，133~147 行）：只验文档那 64 个能否在源码找到字面量，**既不检查多余对象、也不检查各 registry 总数** → 本轮新增 7 个对象它**零覆盖**且仍然 FAIL 0 | **修正风险清单 §E 的表述**：危险不是"脚本变红"，而是"**静默失明**"。需要**单列一件活**（F-66 之后）：把脚本升级到 0.0.2 口径（77 注册对象 / 139 config 键 / 261 资源 / 263 语言键位），并对每个 registry 同时校验「缺失」与「多余」。 |
| V-08 | F-64 验收命令写 `/effect give @s farmstorm:zangief_piledriver 10 2` 并要求「HUD 无图标」，但 1.21.1 的 `MobEffect` **没有** `shouldRenderHUD()`/`shouldRenderIcon()`，`/effect` 的五参构造令 `showIcon = visible`，裸命令必然 `show_icon:1b` | **验收命令改为五参带 `true`**：`/effect give @s farmstorm:zangief_piledriver 10 2 true` → `show_icon:0b, show_particles:0b`。**不加客户端 `IClientMobEffectExtensions`**：真实路径由施加方用六参构造 `(…, false, false)` 保证（TECH §5.2 t=0 行即如此）。约定已写进 `ZangiefPiledriverEffect` 的 javadoc。需回写 `13-TASKS` F-64 验收与 `15-PROMPT-TASKS` 对应条目。 |
| V-09 | F-64 验收写 `/data get entity @s ActiveEffects`，1.21.1 实际键名是 **`active_effects`**（小写），原命令报 `Found no elements matching` | 验收命令改正为 `active_effects`。需回写 `13-TASKS` / `15-PROMPT-TASKS`。 |
| V-10 | `wild_hunter` 三项属性修饰器的百分比暂用 BRIEF 默认常量 **30/30/40**（`FarmstormConfig` 的 `wild_hunter.*` 三键要 F-61 才落，而 F-64 不许改 config） | **F-76 收口**：把三个常量换成三个 config 读，挂/卸逻辑不动。已留单点接缝与注释。 |
| V-11 | F-66 新增的两个 entity_type 缺语言键（`entity.farmstorm.molotov` / `entity.farmstorm.compressed_puffed_wheat`），`/summon` 成功消息显示原始键名 | 属 datagen 的 +39 条语言键（F-96/F-97 口径）。**F-96 收口**：补这两个键并确认中英齐。 |
| V-12 | F-66 用 `java.lang.reflect` 数 `damage_types`（全仓首次反射），理由是 `FarmstormDamageTypes` 不在白名单、加 `ALL` 常量会破边界 | **不接受反射**：改为在 `Farmstorm.java`（白名单内）用 `List.of(CROP_BLAST, VOID_BOLT).size()` 显式列举。理由：`getDeclaredFields()` 依赖字段形状，改名或换同类常量会悄悄改变结果；显式列举改名会编译失败。新增伤害类型时该列表必须同步，总数 77 的断言会兜住。 |
| V-13 | F-61 落地 `wild_hunter.*` 三键后，若 `WildHunterEffect` 仍读常量 30/30/40，则这三个 config 键是**死键**（改了不生效） | **比 13-TASKS 的排期提前收口**：F-61 同时把 `WildHunterEffect` 的三个百分比改为读 config（V-10 就此关闭，不必等 F-76）。 |
| V-14 | F-67 验收第 2 条要求「换钻石镐一次破坏并取出本体方块」，但 `mineable/pickaxe` 与 `needs_diamond_tool` 两条标签、以及战利品表，都是 **F-74** 的交付物（`12-TECH §7.2 #33/#36`、`13-TASKS` F-74）。实测当前状态：钻石镐与空手同样 0.2 速度、**750 秒挖不动** | **F-67 按「属性核对 + 矩阵实测」收口**（属性零改动、矩阵四行全过）；**「钻石镐挖出本体」移交 F-74 收口**：F-74 完成后必须复测「钻石镐挖 → 掉本体」与「木镐挖 30 秒 → 不掉」。 |
| V-15 | 子代理失败会留下**未提交的探针**（F-67 的 `src/.../debug/ProbeF67.java` 带 `@EventBusSubscriber`，每次开服自动跑）；且证据只写进 `run-*/logs/latest.log`，**重启就轮转丢失** | 调度者在验收时必须查两件事：① `git status` 有无 `debug/`、`*Probe*` 残留；② 证据须落 `.work/` 而不是 run 目录。F-67 的矩阵结果就是从 `2026-10-29-1.log.gz` 里捞回来的。 |
| V-16 | `/forceload add <x> <z>` 的参数是**方块列坐标**不是区块坐标：写 `forceload add 6 6` 只标记了区块 `[0,0]`，测试区（区块 6,6）未加载 → `execute if block` 全报 "That position is not loaded"，投掷物在未加载区块里当场消失，整轮统计为 0 | 测试前用**方块坐标**：`forceload add 96 96 104 104`。凡是要让实体/方块 tick 的验收，先 `forceload` 再断言"位置已加载"。 |
| V-17 | 作废那一轮留下的**冻结实体随区块加载复活**，污染下一次复跑（干态出现 36 格 hay 而不是 27） | 验收脚本开头必须有「`kill` 三连 + 静置 1.5s + 清场后基线断言」（目标位置 hay=0 / compressed=0），基线不成立就整轮作废。世界被污染时最省事的处置是删 `run-*/world` 全新重跑。 |
| V-18 | **调度者任务书写错**：F-69 验收 4 写「威力改成 4 → 出现方块破坏」，与 `v002-BRIEF.md` §9.2 的 **A-10**（「调大后只增加对实体伤害，**永远不破坏方块**，破坏权限与威力无关」）直接冲突。子代理发现后没有照做，而是按 A-10 实现并上报 | **按 A-10 修正验收**：威力 4 的判据改为「爆心内实体出现原版击退（`Motion` 由 0 变非零）、方块破坏仍 0、血量不变；改回 0 则击退消失」。这样仍然能证伪「没读 config 的空实现」。已改 `.work/TASK-V002-F69.md`。**教训同 V-04**：验收条件必须先与治理裁决对齐，否则会被一个遵守裁决的正确实现判为失败。 |
| V-19 | F-70 用 `Block.UPDATE_CLIENTS`（2）而非 `12-TECH §3.7` 判据 4 写的 `UPDATE_ALL`（3） | **采纳子代理的选择**，并已用源码证实其安全性：`LevelChunk#setBlockState:283` 调 `onPlace` 是**无条件**的（只 gate 在 `!isClientSide`），所以火照样排 tick、照样蔓延；`markAndNotifyBlock:269` 才把 `blockUpdated`（邻居更新）gate 在 `flags & 1`，`UPDATE_CLIENTS` 恰好跳过它，避免岩浆块/火触发红石·流体·重力连锁。`updateNeighbourShapes` 仍会跑（形状更新，如支撑物消失），这是想要的。→ 文档应把 §3.7 判据 4 改成 `UPDATE_CLIENTS` 并写明理由。 |
| V-20 | 球心口径：子代理按 `12-TECH §3.7` 判据 1 取**落点所在整数格**，而 `11-DESIGN §5.6` 判据 2 写「原点取落点精确坐标 `landingVec`」——两份文档互相矛盾 | **采纳整数格**（否则「球内格数」没有定义；实测 257 正是整数球 `d²≤16` 的分层和）。→ 需回写 DESIGN §5.6 与 §3.7 同口径。 |
| V-21 | 候选判据：子代理按任务书与 `12-TECH §3.7` 判据 2 用 `isSolidRender(level,pos)`，而 `11-DESIGN §5.7` 条件 B 写 `isFaceSturdy(level,pos,Direction.UP)` | **采纳 `isSolidRender`**。两者对台阶/雪层结论一致（实测 4/4 不放置、候选 45 = 49−4），但 `isFaceSturdy` 会让「顶面完整的有向方块」也通过，语义比设计意图宽。→ 需回写 DESIGN §5.7。 |
| V-22 | `12-TECH §3.7` 写「球内约 268 格」，实测整数球（半径 4、`d²≤16`）是 **257**（1+21+37+45+49+45+37+21+1） | 268 是粗估，非错误；但「上限约 268 次 setBlock」这句应改为 **257**，避免后续按 268 做容量判断。 |
| V-23 | **`runData --all` 从 F-65 起一直是坏的**：F-65 注册了 `zangief_cane`（DeployedCropBlock）与 `compressed_hay_block`（普通 Block），但 `ModBlockStateProvider` 的两张外观表都没登记、`registerStatesAndModels` 的 dispatch 也没有普通方块分支（**不加 else 会静默跳过**，连 blockstate 都不生成）。表现是 `deployedModel` 抛 `部署方块缺少外观定义`；补齐后又依次撞 `block/zangief_cane` → `item/molotov` → `item/zangief_cane` → `item/wild_apple` → `item/compressed_puffed_wheat` 五处贴图缺失。 | **本次一并修根因**（不再让每个 datagen 任务写 init script 绕 `--all`→`--server`）：① `DEPLOYED_INVISIBLE` 加 `zangief_cane`；② dispatch 加 `else { simpleBlock(block); }`；③ `ModItemModelProvider` 加 `compressed_hay_block` → `withExistingParent(block/compressed_hay_block)` 分支（按 TECH §3.14，省一张 item 贴图）；④ `tools/gen_textures.py` 的 `BLOCK_TEXTURES` 加 2 条、`ICONS` 加 4 条并重新生成（**走生成器，不手画 PNG**）。结果：`--all` written 9 → 0，幂等成立。贴图源是临时起手，最终外观由 **F-95** 审定。 |
| V-24 | F-73 验收写「`/xp query @s levels` 两次各少 3 点」，实测扣的是**经验点数**（`points 12→9→6`、`total 200→197→194`），**`levels` 读数不变**（11→11）。D-12 原文即「扣经验点数、绝不扣等级」 | **行为正确，是验收措辞错**（且 `13-TASKS` F-73 原文同样含混）。→ 文档应改为「`/xp query @s points` 各少 3」「`/xp query @s levels` 不变」；跨级临界（进度点耗尽）时仍只扣 3 点、等级随之掉 1 级，这一点应写进验收作为边界用例。 |
| V-25 | F-73 验收要求 JSON 里 `bidirectional 为 true`、`experience 为 3`，但 `ConverterRecipeSerializer` 的 codec 用 `optionalFieldOf` 省略等于默认值的字段 → 生成物里**没有这三个键**，既有 21 条同样如此 | **验收措辞应改为**「省略即默认（`bidirectional=true`、`xp_cost=3`、无催化剂）」，并补一句「运行期值需实测」（F-73 已实测 xpCost=3 与反向命中）。不要为了「字面落盘」去改 codec——那会连带改既有 21 条产物。 |
| V-26 | **调度者已入库的论据无效**：F-67 证据用 `BLOCK_MINED 0→0` 推断「`playerDestroy` 从未执行」，但 `FakePlayer#awardStat` 是**空实现**（`FakePlayer.java:100`）→ 该计数器恒 0→0，没有判别力 | 结论不变（卡住掉落的确实是工具门槛），但**论据换成** `canHarvestBlock` + 实际掉落实体计数 + `Block.getDrops()` 直算。已回改 `.work/f67-evidence.md`。**教训**：用统计量/计数器做证据前，先确认它在被测环境里真的会变（`FakePlayer` 会静默吞掉一大批原版副作用）。 |
| V-28 | F-74 验收「木镐挖 30 秒 → 不掉」实测成立，但**真正卡住的是「30 秒连方块都没破」**（600 tick 只到 40% 进度）；木镐/铁镐**真挖穿后**是「破了、掉落 0」 | 两种情形的措辞必须分开写：「600 tick 后进度 0.400、方块仍在、掉落 0」≠「破了但没掉」。F-75 与后续回归记录都按前者表述。 |
| V-30 | F-92 的 `ScreenEvent.Opening` 若严格「只放行 PauseScreen」，相位 2 里 `/kill` 会被拦掉死亡屏，玩家在无 UI 状态下卡死且解锁后旧屏不会自动恢复 | **额外放行 `DeathScreen`**（F-90 的验收要在相位 2 里 `/kill`）。→ 文档应把「只放行 PauseScreen」改为「放行 PauseScreen 与 DeathScreen」。 |
| V-31 | `CalculatePlayerTurnEvent#setMouseSensitivity(0)` **不是真正的锁**：`MouseHandler#turnPlayer` 里 `d2 = sens × 0.6 + 0.2`，灵敏度 0 时系数仍为 **0.2**，相对默认 0.5 约慢 **15.6 倍而非 0** | 无 Mixin 时无法归零，作为**已知限制**记录。F-93 出口记录必须写「转速降至约 1/15.6」而**不是**「视角已锁死」。 |
| V-32 | F-88/F-89/F-90 共用一个新文件 `behavior/ZangiefPiledriverController.java`，按 hunk 拆不出三个独立提交 | 接受**一个提交覆盖三任务**（它们本是同一状态机的三个相位，文档也把 F-89/F-90 列为串行链）。 |
| V-33 | 改 `run-*/config/farmstorm-server.toml` 时 PowerShell `Set-Content -Encoding utf8` 会写 **UTF-8 BOM** → NeoForge 判文件无效、改名成 `.bak` 并按默认值重建，**改动被静默吞掉**；且服务器运行中改会被 FileWatcher 回写 | **必须**先停服，再用 `[System.IO.File]::WriteAllText(path, text, (New-Object System.Text.UTF8Encoding($false)))`，写完核对首字节是 **91,99,111**（`[co`）而不是 239,187,191（BOM）。F-61 也踩过同一条，现补全正确写法。 |
| V-34 | 本会话**无法切换前台窗口**（`SetForegroundWindow`/`SwitchToThisWindow`/`AppActivate` 全部无效），`CopyFromScreen` 只能截到桌面最上层的无关窗口 | **截图级证据必须走游戏内 `Screenshot.takeScreenshot`**（读 framebuffer，与窗口焦点无关）。触发方式：客户端探针 + 开关文件（PARALLEL §3.5），交付前删净并在证据里声明探针存在过。适用于 F-82/F-83（Lane A）与 F-91/F-92（Lane B）。 |
| V-35 | F-77 判据 2 写「Saturation 差为 4.8（4 × 1.2）」，实测 **9.6** | 1.21.1 的 `FoodData#eat` 是 `saturation += nutrition × saturationModifier × 2.0F`，文档漏了末尾 ×2。**代码不动**（同一次探针里原版金苹果也是 9.6，与 DESIGN「取原版同值」一致）。→ 判据改为「= 金苹果同值（实测 9.6）」，不写死数字。 |
| V-36 | 探针类 `client/P14Probe.java` 留在源码树里，**且已被 build 进 jar**（`com/dashpig/farmstorm/client/P14Probe.class`） | 探针只允许在**采集证据期间**存在；一轮采完即删；**每次 `build` 前**先确认 `src` 无探针。要反复使用的采集，探针不得放进 `src/main`。→ **V-07 的脚本升级要新增一条**：jar 内 `Probe` 条目必须为 0（目前 `verify_project.py` 只扫源码，扫不到 jar）。 |
| V-37 | **调度者写的并行规则本身有害**：PARALLEL §3.4 原文要求「收尾 `Get-Process java \| Stop-Process -Force`」，在共享机器上会杀掉**另一条 lane 的进程**。症状是对方看到「无崩溃栈、无 crash report、直接 exit -1」，极难归因 | **改为 PID 级清理**：起进程时记 PID，收尾只 `Stop-Process -Id <自己的>`；服务端优先用 RCON `stop` 优雅停机；锁只删自己那把（`runserver-<FS_RUN_LOCK>.lock`）。只有确认全机没有别的 lane 时才允许清残留，且要先看窗口标题判断归属。Lane A 的客户端两次 `exit -1` 即由此造成。 |
| V-39 | F-90 验收「四格高台落地 40±2 刻」实测 **23 刻**：相位 2 逐刻实测 Y 降 **0.23 格/刻**，而 `h/n` ≈ 0.14 → 设定速度之外还叠了原版重力；平地与两格因剩余高度小、误差仍在 ±2 内，只有四格暴露 | **批准修**：先在相位 2 打印每刻 `getDeltaMovement().y` 与 `getY()` 定位叠加来源，再改用 **`setNoGravity(true)`**（比逐刻对削重力干净）；**`noGravity` 必须在所有退出路径恢复**（落地 / `/kill` / 掉线 / `/effect clear` / 维度切换），漏一条玩家就永久悬空。三种地形都要落回 40±2。 |
| V-40 | **删了探针源码但没重建**，陈旧 class 仍留在 `build/classes` 与 jar 里（`ProbeF80` 即此：`src` 已无、产物仍在）。本轮两次探针进 jar（`P14Probe` / `ProbeF80`）都是同一机制 | **每次回报任务完成前先重建**，并确认三处为 0：`src` / `build\classes` / **jar 内**。`verify_project.py` 的升级（V-07）必须加这条 jar 扫描——它现在只扫源码。 |
| V-41 | **`clean build` 在并行时是危险操作**：`:clean` 会先删 `build/`，若另一条 lane 的 java 正占用 `build/moddev/artifacts/*.jar`，删除会失败并把 `build/` **删掉一半**，后续构建状态不可信 | **并行期间禁止 `clean`**，只用增量 `build`。确需 clean 时先确认全机没有别的 lane 在跑（`Get-Process java` 看窗口标题判断归属），并在 clean 后重新完整 build 一次。 |
| V-42 | F-80 与 F-81 共用 `combat/WildHunterCombat.java` 与新建的 `WildHunterCombatEvents.java`（攻击挂载点与击杀挂载点在同一事件类里），拆不出两个干净提交 | 接受**一个提交覆盖 F-80 + F-81**（与 V-32 同性质）。 |
| V-43 | **行为缺口（结构性）**：F-80 的服务端结算挂在 `AttackEntityEvent`，它只在**真的打中实体**时触发。因此「按下攻击键、30° 锥内有目标、但没打中任何实体」这一路径**不会**做服务端吸附结算；而客户端预测（F-79）仍会让玩家冲过去。NeoForge 1.21.1 没有 `ServerboundSwingPacket` 的公开事件，D-40 禁 Mixin、D-50 禁自订包 → **无包无 Mixin 时无法补齐** | 记入 **F-85 / F-93 出口记录**，并作为**作者决策项**上报：可选 (a) 接受（冲刺是客户端表现，伤害仍按原版攻击 + 40% 加成结算）；(b) 放宽 D-40 或 D-50 中的一条。**不得**由实现方私自绕过。 |
| V-44 | 作者口径：狂野猎人 buff 期间，**吸附判定成功即必中**——不管原版那一下有没有真的碰到实体。F-80 之前因「无 `ServerboundSwingPacket` 公开事件」退到 `AttackEntityEvent`，留下 V-43 的空挥缺口 | **这是恢复 `BRIEF §7.6` 原意**（原文即要求服务端在收到挥击包时独立算一遍并结算）。实现改为**服务端双信号检测**：`PlayerTickEvent.Post` 里观察 ① swing 状态跃变（`swinging`/`swingTime`/`attackAnim`，`LivingEntity:185/188/195`）② 冲刺水平位移，两者同时成立才结算；单用 ① 会误判右键/挖掘，单用 ② 会误判爆炸击退。**必须去抖**：真实命中会同时触发 `AttackEntityEvent`，同一次挥击只准结算一次。判定仍走 `resolveTarget`，不许放宽 5 格/30°。→ 文档回写：`BRIEF §7.6` 与 `TECH §3.12` 需注明落地机制（无事件可用，靠 swing 状态 + 位移做服务端感知）。 |
| V-45 | Phase 14 的 `ZangiefCaneTrap.java` 同时承载 F-86（扫描读数）、F-87（触发判定）、F-88（trigger 里一行 begin）三个任务的改动，按文件拆不开 | 提交粒度定为**两组**：`e1239e3` 服务端（DeployedBehaviors + ZangiefCaneTrap + ZangiefPiledriverController = F-86~F-90）、`e374bb0` 客户端（ZangiefClientState + CameraController + InputLockHandler = F-91+F-92）。 |
| V-46 | F-90 的「四格高台」验收：3×3 台面三种地形均 42 刻通过；但 **1×1 石柱**构型仍 23 刻 | **不改功能规格**。23 刻的原因是玩家漂浮段的横向漂移把自己挪出柱顶，下坠中途被自己站的柱子截住 —— 这是**真实几何结果**，不是伺服错误。→ 在 F-93 与文档里写明：验收用的「高台」指 **≥3×3 台面**；1×1 柱面属「中途被柱体截住」的边缘构型，不作缺陷处理。**不把测试台尺寸写进功能规格**（那是把脚手架固化成需求）。 |
| V-47 | F-85 验收 3 写 `grep -rn "Mixin" src/main/java` 为 0 处，但实际有 **5 处命中，全部是 javadoc 注释**（如「无 Mixin 够不着」「D-40/D-50 又禁 Mixin 与自订包」），它们在解释**为什么**要用绕行方案，删掉是净损失 | **判据改为查实际用法**：`@Mixin` 注解 0 处、`mixins.json`/`mixins.*.json` 0 个、`org.spongepowered` 引用 0 处、`build.gradle` 无 mixin 依赖。F-85 记录里**两种读数都报**（字面命中 5、全部注释；实际用法 0）。不要把解释性注释洗掉来凑字面 0。 |
| V-48 | F-82 灰度**功能不成立**：有效果帧 `meanChroma 50.78`、无效果帧 `50.35`（应趋 0）；但 `/effect clear` 之后那帧却是 `meanLum 77 / meanChroma 9.75`（又暗又灰）。**灰是灰在了不该灰的时候**，说明呈现图像与状态**滞后一帧或错位**，不是「pass 没跑」 | 先查最便宜的三件事（别一上来重写 GL）：① `active()` 门控是否写反；② pass 是否**采样自己正在写的 render target**（`12-TECH §4.1` 骨架即如此，GL 读写反馈结果未定义）；③ 是否走了「先渲染到 RT、再全屏 blit」但 blit 用的是上一帧的 RT。修法优先 **blit 到临时 RT 再采样**；若 NeoForge 1.21.1 提供受支持的 `PostChain`/`post_effect` 加载器，**优先改用官方机制**（目标管理由框架负责，少手写 GL 状态）。 |
| V-49 | `post_effect/grayscale.json` 在 NeoForge 21.1.213 上**无加载器读取**（全 jar 只有原版 `PostChain`/`PostPass`，无注册入口），是死资源；但它被 `13-TASKS §9.3 #15` 计入 36 个资源文件 | **保留文件**（不破坏计数），但**不在 JSON 内加注释**（严格 JSON，`//` 会让将来的加载器直接失败）。「RT-10 退化分支的预留挂载点、当前无加载器」这一说明写进 `12-TECH` 与 `13-TASKS`。另：`12-TECH §4.1` 骨架里的 `RenderTarget` 是抽象类，实际要用 `TextureTarget`——文档需订正（Lane A 首次编译即撞此坑）。 |
| V-64 | `TECH §7.3` 的语言键表写「261 = 222 + 39」，实测补齐后是 **263 = 222 + 41**：39 行表**漏了 2 条实体名**（`entity.farmstorm.molotov` / `entity.farmstorm.compressed_puffed_wheat`），缺它们时实体名在召唤反馈与死亡信息里显示为裸键 | 以实现为准：文档改为 **263 / +41**。另：F-98 的 3 条进度共 6 个键已在本次语言任务里先立好，F-98 落地时**不需要再动语言表**。 |
| V-65 | `item/bamboo_firecracker.png` 与 `item/compressed_puffed_wheat.png` 的仓库现状与 `gen_textures.py` 输出不一致（SHA256 不同） | 逐像素比对：**形状与透明度完全一致，差异只在颜色/明度**；`bamboo_firecracker` 那 165 个差异像素还全部落在 **alpha=0 的全透明区域**（`(0,0,0,0)` vs `(255,255,255,0)`），肉眼不可见。判定为 v0.0.1 期间的**有意手调**，**不回滚、不重生成**。→ `TECH §6.5` 补一句：生成器输出与仓库现状允许存在颜色级手调，重跑生成器前需逐张比对。 |

| V-27 | `Player#getDigSpeed` 末尾有 `if (!onGround()) f /= 5.0F`，FakePlayer 默认不落地 → 挖掘速度被白砍 5 倍（F-67 曾据此把 150 秒误报成 750 秒） | 用 FakePlayer 测挖掘/交互前必须先 `setOnGround(true)`，并在证据里写明。→ `08-DEV-GUIDE` 测试小节补一句。 |
| V-29 | F-75 任务书写「转换需要下界之星当催化剂，先 /give 星星」，实测不成立：三条新配方不占用催化剂槽，带 1 个星星进槽三次转换后原样还在 | 真正扣星星的是 `awakened_torchflower` / `awakened_pitcher_plant` 的正向转换（D-12）。→ `13-TASKS` F-75 与 `TECH` 转换器小节改为「催化剂槽存在，但只有火把花/瓶子草正向消耗 1 个下界之星」。 |
| V-38 | 任务边界泄漏：同一文件里先写下一任务的代码再回报当前任务，调度者无法判断边界，可能把半成品提交进上一任务 | 规则：一个任务回报时其代码必须完整，文件里不能有下一任务未验证的代码；需要提前写就事先声明。 |
| V-50 | F-83 剪影与作者预期严重不符：① 漏掉 `LivingEntityRenderer#render` 画模型前的 `scale(-1,-1,1)`（`:101`）与 `translate(0,-1.501,0)`（`:103`），而 `RenderLivingEvent.Post` 在 `popPose()`（`:137`）之后触发（`:139`）→ 红模型倒挂且悬空 1.5 格；② 用 `event.getPackedLight()` → 暗处即暗；③ 与本体几何重合会 z-fighting | 作者口径：模型本体染红、任意亮度明显可见。修法：补回两个变换、光照 `LightTexture.FULL_BRIGHT`、重画时轻微放大（1.008）。方法论教训：中心区红色最大值指标在模型倒挂悬浮时同样成立 → 指标只能量化有没有红，不能判断像不像，必须配人工看图。 |
| V-51 | 作者指定循环音改用 `D:\dsh\default\Animals_from_3m04s.mp3`（47.14 s 剪辑），替换原整体版 | 经 `tools/convert_media.ps1`（mono / 64 kbps / libvorbis）：`1,782,670 → 352,061` 字节，jar `2,180,993 → 791,643`。管线默认 `-Animals` 同步改为新源。运行姿势：本机 `pwsh` 不在子进程 PATH、ExecutionPolicy 为 Restricted → 必须用 `powershell.exe -NoProfile -ExecutionPolicy Bypass -File` 并把输出重定向到文件（管道捕获在沙箱下失败）。`-Force` 会顺带重编 `zangief_piledriver.ogg`，因 Ogg 流序列号随机、字节不可复现，已退回。 |
| V-52 | 剪影与本体无法完全重叠 —— 重放变换链的路走不通（`LivingEntityRenderer#scale(entity,…)` 是 protected，各渲染器还有自己的平移缩放） | 改为让渲染器自己再画一遍：包一层 `MultiBufferSource` 把请求的 RenderType 换成纯色类型，配 `FULL_BRIGHT`，调 `renderer.render(...)`；变换链由渲染器执行 → 几何必然重合。必须加重入守卫（`render()` 内部会再 post `RenderLivingEvent`）。 |
| V-53 | 作者实测：移动时循环音明显卡顿，左右平移最明显 | 根因：`SoundEngine` 每 tick 才 `setSelfPosition`（`:245-253`），听者按帧更新；`relative=false` 时 OpenAL 仍按听者到声源的向量做左右声像 → 横向移动时声像来回跳。修法：自己的光环 `relative=true`（坐标归零）、别人的 `relative=false + Attenuation.LINEAR` 交给引擎衰减、`volume` 恒 1.0（删掉每刻手写音量以避免双重衰减）。 |
| V-54 | 剪影变白：`RenderType.entityTranslucentEmissive` 的着色器故意忽略 ColorModulator（emissive 的定义即不受光照与色彩调制器影响），`setShaderColor(红)` 完全没进画面 | 颜色烘进贴图：1×1 RGBA `(255,38,38,115)`（D-62 的 0.45）→ `textures/misc/wild_hunter_red.png`，继续用 emissive 类型；删掉 `setShaderColor`（留着只会误导）。 |
| V-55 | F-82 手写全屏 quad 两次尝试均不可见（门控通、pass 每 240 帧执行、ping-pong 已做，画面仍不变） | 改走原版 `PostChain`：`GameRenderer#loadEffect` 是 public（`:319`），内部 `new PostChain(...)` 并置 `effectActive=true`（`:327`），执行点 `:1027`。链 JSON 见 `shaders/post/invert.json`（targets + passes，末条 blit 回 `minecraft:main`）。关键坑：targets 必须是数组（`PostChain.load:63` 只认 `isArrayNode`），写成对象会被整段跳过并报输出目标不存在；program JSON 必须声明 `ProjMat`（`blit.vsh` 靠它定位全屏四边形）。限制：`process()` 在 `renderLevel` 之后、GUI 之前 → 只灰度世界。 |
| V-56 | 作者裁决：F-82 灰度只作用于世界，不含 HUD/背包 | 采纳。F-82 验收改为「世界灰度、HUD 保持原色」，并把 HUD 保持原色写成明确的设计选择而非缺陷（原版 creeper/spider 效果同理）。→ 改 `13-TASKS` F-82 标题与验收、`12-TECH` 灰度小节、D-61 一带。 |
| V-57 | 灰度上线后暴露两处：① 红色剪影被灰度吃掉（灰度是世界级后处理，执行于世界渲染之后）；② 循环音完全无声（上一轮把 self 设成 `relative=true` 却仍写世界坐标、且用 `LINEAR` → 声源被扔到几百格外，增益归零） | ①把剪影挪到灰度之后（GUI 阶段）：`AFTER_ENTITIES` 捕获投影/modelview/相机，`RenderGuiEvent.Pre` 恢复状态重画；`AFTER_LEVEL`（`:1273`）在灰度之前，不能用。②`relative=true` 时坐标必须归零且用 `Attenuation.NONE`；`relative=false` 才写世界坐标 + `LINEAR`。 |
| V-58 | 剪影渲染把客户端崩了两次（`crash-2026-10-29_19.38.28` / `19.45.22`）：`IllegalStateException: Missing elements in vertex: UV0, UV1, Normal`，栈顶 `EntityRenderer.renderLeash(:133)`。原因：`MultiBufferSource` 把所有 RenderType 无条件替换成 entity 格式，而 `RenderType.LEASH` 用 `DefaultVertexFormat.POSITION_COLOR_LIGHTMAP`（无 UV/Normal） | 按顶点格式过滤：只有 `type.format() == DefaultVertexFormat.NEW_ENTITY` 才替换，其余原样放行。调查教训：客户端无栈直接退出时先看 `run-<dir>/crash-reports/` —— 前三次误判为外部 kill，实际都有完整报告。 |
| V-59 | 方法名不统一：`13-TASKS` F-78 写 `resolveTarget`、`TECH §3.12` 写 `selectTarget` | 以实现为准统一为 `resolveTarget`，不加别名（两个名字并存必然各自漂移）。形参用 `Player` 而非 `ServerPlayer`（算法只有一份，客户端预测要复用）。 |
| V-60 | `TECH §4.2` 剪影小节与实现三处不符 | ① `RenderLivingEvent.Post` 触发时 poseStack 已 pop，不能直接复用；② `entityTranslucentEmissive` 忽略 ColorModulator，红色必须烘进贴图；③ 剪影要画在灰度之后（GUI 阶段），否则被后处理一起灰掉。 |
| V-61 | F-76 验收 1 写「F3 的 Speed / Attack Speed / Attack Damage 读数」 | 1.21.1 的 F3 面板不显示属性数值。改为 `/attribute @s minecraft:generic.movement_speed get` 等等价读数；F3 仅作辅助。 |
| V-62 | `TECH §6.2` 写 `-ar 44100`，而作者指定素材是 48 kHz | 口径改为保留源采样率：`zangief_piledriver` 44.1 kHz、`wild_hunter_loop` 48 kHz。并注明 Ogg 每次重编码流序列号随机，md5 不能作判据，只能比时长/声道/码率。 |
| V-63 | 客户端日志两条真缺陷：① `ERROR SoundManager: Missing subtitle translation` ×2 —— 生成的语言文件里 `subtitles.*` 键为 0；② `WARN ClientRecipeBook: Unknown recipe category: farmstorm:convert` ×3 | ① 已由 F-97 补齐（穷举推导出 35 条缺失，不止这两条）。② 记为已知外观缺陷（仅影响配方书分组），留到 F-99 出口验收一并处理。 |
| V-66 | **F-80b 的「吸附必中」在真实链路里其实不成立**：双信号要求 `newSwing && speed >= DASH_SPEED` **同一 tick** 同时成立，但真包实测 `ServerboundSwingPacket` 与位移包**不同 tick 到达**（位移常晚 1–2 刻）→ 服务端零结算。只有探针直接调 `tick()` 时才「看起来对」 | 改为记 `swingTick`，允许 **2 刻窗口**配对（`SWING_WINDOW = 2`），去抖逻辑不变。修复前：玩家位移 0.5→3.82 但猪 10.0f→10.0f、服务端零日志；修复后：`吸附挥击 速度 0.60` → `目标 夹角 26.0 度 距离 2.40` → `结算 伤害 1.4`，猪 10.0f→8.6f 且只掉一次。**教训**：合成状态（直接调方法）能骗过验收，**必须走真包**才能发现时序差。→ 文档回写：`TECH §3.12` 的双信号说明要补「两信号允许跨 tick，窗口 ≥2 刻」。 |
| V-67 | `WARN ClientRecipeBook: Unknown recipe category: farmstorm:converting/<id>` **实测 24 条**（不是 3 条）。根因（`ClientRecipeBook#getCategory` 1.21.1）：vanilla 的 switch 只认 `CRAFTING/SMELTING/BLASTING/SMOKING/CAMPFIRE/STONECUTTING/SMITHING`，自定义 `RecipeType` 落到 else 分支；NeoForge 留了钩子 `RecipeBookManager.findCategories(type, holder)`，没人注册就 warn 并回落 `RecipeBookCategories.UNKNOWN` | 修法走**官方入口** `RegisterRecipeBookCategoriesEvent#registerRecipeCategoryFinder(CONVERTING_TYPE, holder -> RecipeBookCategories.UNKNOWN)`。**返回 `UNKNOWN` 而不是 `CRAFTING_MISC`**（这一点比调度者最初的建议更对）：转换配方只能在转换器方块里做，塞进工作台 misc tab 会让用户看着能合成却摆不进合成格、还有踩 `PlaceRecipe` 的风险；`UNKNOWN` 是 vanilla 自带分类，行为与现状一致，只是不再刷警告。 |
| V-68 | Phase 15 视觉四项验收（作者未在场，由调度者读图判定） | **四项全过**：① F-94③ 两条 `/playsound` 客户端无 `Unable to play unknown soundEvent`、也无 `Missing subtitle translation`，而反面对照（不存在的音效）确实报错——证明前两条的"成功"不是假的（服务端对任意 id 都回 `Played`，判据只能在客户端日志）；② F-94④ 效果图标 18×18，红粉+白灰图案，非紫黑棋盘格；③ F-95②/F-96④ 快捷栏 5 格全有内容、无一紫黑，前四张带绿「+」；④ F-95④ 桑吉尔蔗方块只有选中框无几何体，破坏时炸出绿色碎片且 12 帧连拍像素数单调衰减（442→97，证明是瞬态粒子而非实体），该模型只有 `textures.particle` ⇒ 粒子用的就是它。探针三处 0/0/0（jar 481 条目）。 |
| V-69 | F-98 三条进度的 `award` 接线：`first_molotov` 在 `onAddedToLevel()`、`zangief_hit` 在 `ZangiefCaneTrap.trigger()`、`first_wild_hunter` 用 `MobEffectEvent.Added` | 三处口径都按 `11-DESIGN §9.2` 落实：①**投出即判定**用 `onAddedToLevel`（不进构造器）——`/summon` 走两参构造无归属者、天然不推进，而投掷路径在 `addFreshEntity` 之前已 `setOwner`，所以进世界时 `getOwner()` 就是投掷者；②`zangief_hit` 授予**部署者**（父进度是部署者树的 `first_deploy`），被命中者不推进，归属者离线或 `/setblock` 放置则跳过；③`first_wild_hunter` **不用** `FoodCropItem`（那只覆盖狂野苹果一个来源，与「不限定来源」冲突），改 `MobEffectEvent.Added`（`MobEffectEvent.java:171`，带 `getOldEffectInstance()`），`getOldEffectInstance() == null` 才判，续时/刷新不重复推进。全仓 12 处 award 调用点覆盖 11 个 `AWARD_REQUIRED` id。 |
| V-70 | **同一功能两套实现并存**：剪影在世界阶段（`WildHunterOutline.onRenderLivingPost`，旧画法）与 GUI 阶段（`onRenderGuiPre`，新画法）**两条都接着线**，每个目标每帧被 `renderer.render(...)` 跑两次。旧的那次还会被灰度吃掉，只因新的盖在上面才看不出来 | 交付前审查发现（Lane A 独立审 diff 时揪出）。**教训**：当初让「旧路径当退路保留」是合理的，但**没有给退路设定失效条件**——新路径验证通过后没人回头删。规则：留退路时必须同时写下「何时删」，否则它会静默变成每帧开销。已删旧路径及其 3 处接线（`FarmstormClient` 的 `onRenderLivingPost` + import、`WildHunterClientHooks` 转发、`WildHunterOutline` 方法本体），全仓 `onRenderLivingPost` = 0、真正 `renderer.render(` 调用只剩 GUI 阶段 1 处。同时清理 `@Deprecated` 空方法 2 个、死方法 `loopRange()`、无用 import `Mth`，并改正两处误导性类注释。 |
| V-71 | Lane B 的动态验收里探针 `gameMode.useItemOn(...)` **两次返回 FAIL、方块没放下去**，原因未查明；它改用 `/setblock` + `/data merge block {ownerUuid,ownerName}` 绕过 | **作者真机右键验证：可以正常放下**（`DeployableCropItem#useOn` 先试耕地播种、`PASS` 则走 `BlockItem` 普通放置）。所以 FAIL 是**探针构造的 `BlockHitResult` 有问题**，不是产品缺陷。另作者确认「放置者不会被自己的陷阱弹起」是**正确行为**（F-87 的归属者豁免 / D-71）。→ 文档无需改；但 `08-DEV-GUIDE` 可补一句：**探针调 `useOn/useItemOn` 需要构造合法的 `BlockHitResult`（含命中面与精确坐标），否则会拿到 FAIL**——这类"探针假阴性"和"合成状态假阳性"是同一枚硬币的两面。 |
| V-72 | **莫洛托夫图标原型错**：作者指出它的原型是**绯红菌**，而生成器里写的是 `item/splash_potion`（F-95 时我按"临时起手、最终外观由 F-95 审定"随手选的） | 改 `gen_textures.py` 的 ICONS 条目为 `("item","crimson_fungus")`（fallback `block/crimson_fungus`），重生成并只覆盖 `item/molotov.png`。新图是暗红菌盖 + 绿「+」，与转换配方的原料（`minecraft:crimson_fungus`）一致。 |
| V-73 | **生成器确定性再次确认 + 一处仓库漂移**：`compressed_hay_block.png` 的仓库版本与 `gen_textures.py` 输出不一致（48/256 像素亮度不同、最大差 55），而同一生成器连跑两次输出**完全一致**（SHA 相同）⇒ 生成器确定，是**仓库那份漂移了**（F-95 时代理称"SHA 逐张比对后只拷那 1 张"，与实际不符） | 按项目规则「生成器是唯一来源」**采纳生成器输出**（两者都是灰度、16×16，F-95 的色相 0 判据不受影响）。→ 提醒：`V-65` 里那两个"允许手调"的 `item/bamboo_firecracker.png` / `item/compressed_puffed_wheat.png` 是**图标**、差异只在颜色/透明像素；而**方块贴图**不该有手调空间。 |
| V-74 | **作者实机游玩后对桑吉尔蔗提的改动**：① 相位 1 的漂浮上升高度不足、相位 2 下落太慢；② 相机应「上下倒转 4 tick 后开始旋转直到结束」；③ 触发音效要由玩家发出 | 三相位重做：相位 1 改为**快速升高 15 格**（不再用漂浮效果，伺服向上，速度 = 剩余高度 ÷ 剩余刻数）；相位 2 **不再限制下落速度**，进相位先还重力 + 清速度，之后完全不写速度，只做落地检测（日志改「自然坠落」）；音效改 `level.playSound(null, player, …)` 以玩家为声源。**坠落伤害不取消**（全仓 grep `causeFallDamage/onLivingFall/fallDistance` = 0 处，模组从来没有取消逻辑）：抬 15 格落硬地约 15 点伤害（7.5 颗心），符合作者要的痛感；唯一旋钮是 `riseHeightBlocks`。副作用：抬升期间 `fallDistance` 累加 ⇒ **抬多高就摔多狠**，两个量绑在同一键上。 |
| V-75 | 相机口径歧义：作者原话「视角旋转从视角上下倒转 4 tick 后开始直到结束」有两种读法 | **裁定为主语是「旋转」**：`inverted()` = 进入相位 2 起恒真（倒置保持到结束，这是打桩机的核心观感）；`spinning()` = `fallingTicks > spinDelayTicks`（默认 4）起一直转到结束。**`fallingTicks` 以进入相位 2 为 0**，不以效果生效为 0（否则玩家还在升高的 15 刻里就转起来了）。另一种读法（倒转只持续 4 刻后回正）不采纳，因为它会取消倒置观感且与 D-59 冲突。 |
| V-76 | config 键改名（键总数 **139 不变**）：`levitationLevel` → `riseHeightBlocks`(15)、`levitationDurationTicks` → `riseTicks`(15)、`fallTicks` → `spinDelayTicks`(4) | 相位 1 不再是漂浮，这三个键原语义全部失效；留着就是死键。按「只改名不增删」口径处理，`stageDelayTicks`(14) 保留。`ModLangProvider` 三条文案同步，zh/en 各 263 键不变。 |
| V-77 | **火把花的火焰附加附魔不会消失**（作者实机发现）。已排查：`grantFireAspect` → `TempEnchantHandler.grant(..., refreshTicks×3 = 300 刻)`；`FarmstormGameEvents:55` 每 20 刻调 `tick(player)` → `revokeExpired` → `restore`；`restore` 只在 `current == granted` 时才还原。**接线是齐的**，所以要么到期没触发、要么还原无效 | 待实测定位（两个重点怀疑：① `EnchantmentHelper.updateEnchantments(stack, c -> c.set(ench, 0))` 在 1.21.1 是否真能移除；② `revokeAll(stack)` **全仓 0 调用者**，javadoc 却说「离开光环时用」——若设计本意是离开即撤销，这条路径从未接上，只剩 300 刻兜底）。 |
| V-78 | **狂野猎人在水下完全看不见**；作者建议持续期间调高伽马 | 客户端方案：激活时记录并抬高 `Minecraft#options.gamma()`，失效时**精确还原**；还原必须覆盖**五条退出路径**（到期 / `/effect clear` / 死亡 / 退出世界 / 掉线）。这是**玩家本地设置**，漏一条就会永久改坏且事后极难归因。只改内存值、不写 `options.txt`；不用夜视（会改变玩法口径）。 |
| V-79 | 桑吉尔蔗三相位**实测收口数据**（作者口径全部落地并复测） | **相位时长**：14 / 15 / 22 刻（两轮一致）。**抬升**：`riseHeightBlocks` 默认改 **16** 后实测 **14.86 格**（目标 15，差 0.14）——原 15 只到 14.01，因为相位 1 最后一次伺服的速度在相位 2 入口被清零；snap 到目标 Y 无效（被客户端位置回写覆盖）。**速度上限** `MAX_RISE_SPEED` 由 1.5 放宽到 **4.0**（先试 3.0 仍被夹：16/5 = 3.2 > 3.0 → 只抬 12.0 格），改后 `riseTicks=5` 抬 15.59 格、5 刻完 ⇒ **两键解耦成立**。**坠伤 12.00**（HP 20→8，石头与草一致；落水 0，符合原版）。**机制更正**：`fallDistance` 在**抬升段不累加**（原版向上移动递减归零），伤害 = **从最高点落到地面的高度 − 3**，不是「累计行程」——若累加则 14 格应造成 25 点而非实测 11/12。**noGravity 五条全绿**（落地 8.70 / `/kill` 8.70 / `/effect clear` 9.21 / 换维度 9.89 / **掉线**：相位 1 内杀客户端，重连后无 noGravity、半秒落 8.70）。**相机**：相位 2 天地颠倒（截图）+ 自转 17 刻×72°=1224°、相位 2 共 21 刻 ⇒ 延迟 **4 刻**。 |
| V-80 | **在 pwsh 脚本里用 `& gw.cmd ... 2>&1` 捕获 Gradle 输出会挂死**（不是慢，是不返回）：两条 lane 都踩过，我自己也踩了两次（那份 `rebuild.ps1` 跑了 26 分钟零输出，最后被作者手动取消）。根因是沙箱下子进程管道 stdio 不可用（与 Node `child_process` 的 EPERM 同源），`2>&1` 让 PowerShell 去接管子进程输出流，于是永久等待。 | **唯一可靠写法**：`cmd /c ""<绝对路径>\gw.cmd" build ... > "<日志文件>" 2>&1"` —— 重定向由 cmd 自己做、不经过 PowerShell 管道；跑完用 `$LASTEXITCODE` 判结果、用 `Select-String` 读日志。**判据**：超过 5 分钟没有日志文件产出就说明写错了，立即停，别等。 |
| V-81 | **狂野猎人在水下仍然看不清**：作者反馈「去掉雾也还是不好」，并给出一条决定性观察——**开 F3+F4 面板画面会变彩色，此时水下视野正常** | 说明元凶是**灰度后处理**，不是亮度/雾。源码核对：`GameRenderer:1027` 的后处理开关只有 `postEffect != null && effectActive`，与屏幕无关；真正的原因是水下画面本身偏暗、低对比，取亮度后细节糊成一片均匀的灰。**方案（作者选定第二案）**：给水下**单独一条链** `shaders/post/grayscale_underwater.json` + `program/grayscale_underwater.{json,fsh}`，仍是黑白，但三步处理 —— ① `pow(luma, 0.62)` 提亮暗部；② 围绕中灰 0.5 拉伸 1.45 倍拉对比；③ ×1.08 轻微整体提亮。`GrayscalePass` 按 `isEyeInFluid(WATER)` 在两条链间切换，切换只动 GL、状态变化才执行。实测日志：`14:19:38 卸载 grayscale → 加载 grayscale_underwater`（下水）、`14:19:43 卸载 underwater → 加载 grayscale`（出水），全程零着色器错误。 |
| V-82 | **索敌角度被当成「半角」用**：`searchAngleDegrees` 直接与夹角比较，设 90 实际是 **180° 半球**（面前一片全中），作者反馈「像扇形不像锥」。代码本身一直是三维点积（含俯仰），错的是**语义**。同批还有一处日志误导：`扇形结算` 只打**名义伤害**，而伤害封顶与盔甲减免都在 `hurt()` 内部结算（名义 8.4 实际掉 5），会让人误判「封顶没生效」 | ① 配置值按**全顶角**解释、比较时取半角（`maxAngle * 0.5`），默认 **90 → 60**（= 半角 30°），config 注释与语言键同步改为「索敌锥全顶角」；② 日志固定打**双读数** `名义伤害 X 实际掉血 Y`。两条都已写进 `08-DEV-GUIDE §7` 坑表。 |
| V-83 | **`wild_hunter.loopSoundRange` 是死键**：广播半径在 `WildHunterAura` 里**写死 32**，而 config 默认值仍停在 **16** 且**没有任何代码读它** ⇒ 玩家改它毫无效果；文档写 32、config 写 16、行为是 32，三处打架。核对 `plan0.0.2.txt` 与实现时发现 | 广播半径改为**读 `FarmstormConfig.wildHunterLoopSoundRange`**，删掉写死常量；默认值 **16 → 32**（与 `sounds.json` 的 `attenuation_distance: 32` 对齐）。文档同步 25 处（默认值 6 / attenuation_distance 8 / 引用与机制 11），并删掉对**早已删除的** `WildHunterClientState.loopRange()` 的引用（V-70 清理死代码时删的，文档没跟）。 |
| V-84 | **发布件三处不一致**（作者两次追问才发现）：① `install-notes.txt` 没跟着 jar 更新——正文写了新特性、附件还是旧版，其中最糟的是附件里「30° 锥」「击杀续时无上限」两句已变成**错的**；② release 的 tag 停在 `42a25c1`，落后 HEAD **4 个提交**，而 GitHub 自动生成的 `Source code` 压缩包是**按 tag 打包**的 ⇒ 源码包缺八项特性与水下灰度链，只有 jar 是新的 | 根因是「只更新最显眼的那个（jar）」。**处置**：① 重写安装说明并重传；② `git tag -f v0.0.2 HEAD && git push --force origin refs/tags/v0.0.2`；③ 新增 `tools/verify_release.ps1` 把「tag == HEAD / jar 哈希 / 安装说明哈希 / 正文含本版改动」四处做成可执行检查（exit 1 表示不能发），并**自测过失败路径**（旧 tag 会正确报 FAIL）；④ `08-DEV-GUIDE §8.4` 写进检查表与七步发布顺序。**教训**：发布件的「跟着一起变的东西」不能靠记性，要么脚本化、要么固定流程。 |
| V-85 | **狂野猎人的循环音只有本人能听见**。客户端 `WildHunterLoopSound` 里原本有一条「给其他带效果的玩家也建实例」的分支（`relative=false` + 世界坐标 + LINEAR），但它依赖 `A.hasEffect(WILD_HUNTER)` —— **前提不成立**：原版 `ClientboundUpdateMobEffectPacket` 只发给持有者本人，B 侧永远看不到 A 身上的效果 ⇒ 那条分支一次都没执行过 | 修法：**服务端广播**。`WildHunterAura` 向「同维度、≤32 格、**排除持有者**」的玩家发原版 `ClientboundSoundPacket`（零自定义 payload，不破 D-50），**每 900 刻重发**（曲长 942.8 刻，尾巴叠 2.1 s，宁叠不留空）。**按收件人独立计时**（键 = 持有者+收件人），否则「新人走进 32 格」与「断线重连」都要等满 900 刻（45 s）才听到第一声。客户端那条死分支整套删除、注释改成真实原因。实测：2 人在线 N=1、A 无双份、150 格外不发包、+900 刻准时重发、B 侧零本地实例、**B 重连后 4 ms 内收到**。 |
| V-86 | **依附方块被挖时，上面的陷阱方块掉两个**。用 `EntityJoinLevelEvent` 抓物品实体堆栈实证：两条**不同**路径各掉一次 —— ① `DeployedCropBlock#updateShape` 里的 `Block.dropResources`；② 原版 `NeighborUpdater#executeShapeUpdate → Block#updateOrDestroy → Level#destroyBlock(pos, true)` 按战利品表再掉一次。老注释「掉落放在这里而不是 onRemove，免得复制」**防错了对象**：复制源不是 `onRemove`，是原版自己的移除路径 | 删掉 `updateShape` 里的 `dropResources`，掉落交给原版（一定掉、只掉一次）；`dropOnSupportLost=false` 时先 `server.removeBlock(pos, false)` 再返回 AIR。实测：破坏依附方块 2 → **1**、玩家直接挖 **1**、手工单次 `dropResources` 1。⚠️ 依赖「形变更新必走 destroyBlock」这一原版行为（1.21.1 双向验过）；`/setblock`、`fill` 换成空气是**不掉落**语义，与原版方块一致。 |
| V-87 | **改了 config 默认值/键名，跑起来还是旧行为**——今天踩两次：`run-p13` 的 toml **缺** `deployDropOnSupportLost` 键（取默认 true，导致「false 分支」压根没被测到）；`run-p14` 还是 `levitationLevel` 时代的**旧键名**（Lane B 差点在旧键上测新参数） | 根因：**`run-*/config/farmstorm-server.toml` 不会跟随代码**。固定动作：改 config 后**删掉该 toml 让它重新生成**，或起服前 `Select-String` 核键集（旧键 0 处、新键各 1 处）。已写进 `08-DEV-GUIDE §7` 坑表。 |
| V-88 | 循环音广播修好后**又暴露一层**：别人听到的声音**钉在「发效果那一刻的位置」**，玩家走开声音留在原地 | 根因：`ClientboundSoundPacket` 把坐标烘进包里。改用 **`ClientboundSoundEntityPacket`**（绑持有者本人）——客户端 `EntityBoundSoundInstance` 每刻从实体读坐标（源码 `:34-36` 已核）。**判据更正**：调度者最初写「B 回到 A 原位 ⇒ delta≈20」是**错的**（delta 的定义是「实例↔实体」）；正确判据是**两条同时成立**：实例↔A 当前 = **0.00** *且* 实例↔发包旧坐标 = **20.00**。实测四段（站定 / A 走开 20 格 / B 回原位 / A 走回）全部 delta=0.00，900 刻重发与排除持有者照旧。**人耳那一半（音量衰减、声像方位）仍待作者验**。 |
| V-89 | **我用 `Get-Content` 数中文文档行数，误判 v0.0.3 六份中文文档「被截断」**（量出 zh 3079 vs 声称 4534，差 1455 行，且每份都比英文少一个标题） | 实际是**读取端编码问题**：仓库 `.md` 全是无 BOM 的 UTF-8，中文 Windows 上 `Get-Content` 默认按 GBK(936) 解码，UTF-8 三字节序列被成对吞掉 ⇒ 行数少两三成；英文几乎全 ASCII 所以看着正常。用 `[IO.File]::ReadAllLines()` 重测：**12 份逐份与声称值差 0**，合计 zh **4534** / en **4580**，抽样三份的字节数/行数/sha256 与 `.work/v003-manifest.md` 逐项吻合。**教训**：这是本轮第二次因「读法」而非「写坏」造成误判（另一次是 `swing 同 tick`）。规则已写进 `08-DEV-GUIDE §7`：量行数一律指定 UTF-8，判据对照 manifest。 |
| V-90 | **v0.0.3 文档把 config 键数算多了 3**：`18-TECH §8.1` 的 19 行里 `#13/#14/#15` 是 `fang_trap_cactus` 的**既有键、只改默认值**，但 §8.3 / §11 #2 / `19-TASKS` F-104 / F-134 / F-135 都按「139 + 19 = 158」写 | 实测 **155**（139 + 16 新键）。三条硬读数：源码 `.define*` 计数 155、全新 run 目录首次起服生成的 `farmstorm-server.toml` 叶节点 155、`tools/verify_project.py` 用**显式并集去重**（`overlap = added3 ∩ (base ∪ added)`）后自算 155。连带口径：lang 的 `farmstorm.configuration.*` = **155**、F-134 的 300 应为 **297**、「F-104 之后 282」应为 **279**。**待订正位置**：`18-TECH §8.3 / §11 #2`、`19-TASKS F-104 验收 1/5`、`F-134`、`F-135`。交付者拒绝为凑 158 造 3 个不存在的键——正确。 |
| V-91 | **`runData` 在 Phase 16 跑不通**，两份生成的 lang 停在 263 键 / 139 条 configuration，**落后源码 16 条** | 两个阻塞，均属后续阶段：① `IllegalArgumentException: Texture farmstorm:block/friend_chives_stage0 does not exist`（`ModBlockStateProvider.java:81`，F-130 的四张 PNG）；② `IllegalStateException: 部署方块缺少外观定义: point_defense_fungus`（`ModBlockStateProvider.java:116`，`DEPLOYED_VANILLA_PARENT` 缺项，F-117）。**解除条件**：资产到位 + 外观表补齐 → 跑一次 `runData`，lang 263 → **279**。在此之前不要用「lang 键数」当验收依据。 |
| V-92 | **Phase 16 收口**（F-100~F-105 六个任务全部完成并推送）。硬读数：注册 **90**（12 类 31/25/6/13/4/3/1/1/1/3/1/1）、config **155**、作物 **22**（投掷 12 / 部署 7 / 食用 3；破障 4）、幸运浆果池 **19**、`DeployedKind` **8**；`tools/verify_project.py` → `FAIL 0`、`90 / 90`、`155 / 155`、`lang 263`。 | **三项已知残余（不阻塞 Phase 17，已登记）**：① **生成物 lang 仍 263 键 / configuration 139 条**——`runData` 被 F-130 的四张 `friend_chives_stage0..3` 贴图与 F-117 的 `DEPLOYED_VANILLA_PARENT` 缺项挡住（V-91），解除后 lang 263 → 279；**在此之前不得用「lang 键数」当验收依据**。② **F-104 的热重载读回**（`point_defense_fungus.maxIntercepts` 改 3 → `/reload` → 读回）推迟到 Phase 18——该值目前没有可观测的读回通道，等炮台行为落地才能验。③ **`/give` 与补全**未单独跑：已被工具 registry 段（items 31 逐个 ID 齐全）与 F-103 的 `/summon`、`/setblock` 冒烟覆盖；客户端补全需起客户端，留到 Phase 20 回归一并做。另记一处**文档与实现的措辞差**：`21-PROMPT-TASKS` 说四个新 config 分组「按字母序追加在文件末尾」，实际是**定义顺序**（qbounce 659 / friend_chives 675 / point_defense_fungus 689 / vibration_bean 703）——NeoForge 按代码 push 顺序写盘，此差异仅外观、不改。 |
| V-93 | **作者离开期间的运行约定**（2026-10-31 凌晨）：作者要求「持续推进，除非遇到解决不了的问题或需要他决策的事件」；一旦触到这两类，**先把持久目标 `goal-8079c0df` 暂停**，把「问题 / 候选方案 / 各自代价 / 我的建议」写成清单留在本文件，等他回来拍板。 | 判定「需要决策」的四类：① 缺素材（音频/贴图/图标）且无法自造；② 文档与实现冲突且两者都能自圆其说；③ 破坏性变更（版本号、config 键改名、语义变更）会牵连已发布内容；④ 需要主观判断的观感/手感（只有作者能定）。其余一律自行裁决并记录。 |
| V-94 | **F-112「保留哪条坐姿分支」的时点**：作者裁定「可以最后再做决定，不影响其他内容实现」 | 处置：`F-108`（render 分支：模型改 riding 姿态）与 `F-109`（mount 分支：隐形盔甲架坐骑）**两条都实现、都能用 `qbounce.poseMode` 单值切换**，落选的那条只改「默认关闭」、代码一行不删（文档原要求）。**不在 Phase 17 出口处卡住**：F-112 的对照数据（观感截图 / AI 表现 / 碰撞差异 / 第三方模型兼容性）随 Phase 17 一并采集存档，等作者在最终验收前拍板，届时只改 `qbounce.poseMode` 的默认值一处。 |
| V-95 | **v0.0.3 四件源素材已确认在位**（F-130 不会被素材卡住）：Q弹图标源 `C:\Program Files (x86)\Steam\steamapps\workshop\content\431960\3796093604\preview.jpg`（622 KB）、朋友的韭贴图源 `…\Tencent Files\…\525a9e86…_720.png`（453 KB）、韭菜种子贴图源 `…\231896ac…_720.png`（176 KB）、朋友的韭音效 `D:\dsh\default\pengyoudejiu.mp3`（7276 KB）。 | 四条路径均 `Test-Path` 为真。另：`textures/mob_effect/` 现只有 `wild_hunter.png`（1412 B），`q_bounce.png` 待 F-130 由 preview.jpg 生成（18×18，居中裁切缩放）；在此之前真机会显示原版 missing texture——**这是预期，不是缺陷**。 |
| V-96 | **`ServerStoppedEvent` 阶段世界已关闭，隐形坐骑会留在存档里**：F-109 原按 §4.3 只挂 `ServerStoppedEvent`，实测该阶段 `getLevel(dim).getEntity(..)` 返回 null ⇒ `discard=false`，**重启后世界里残留 2 个 armor_stand** | 改挂 **`ServerStoppingEvent`**（世界还活着时 discard），保留 `ServerStoppedEvent` 作清表兜底。重测：停服前 `清表 reason=服务器即将停止 共 1 个（discard 1）`，重启后 `execute if entity @e[type=armor_stand]` → `Test failed`（存档零残留）。这是 F-109 唯一一处对文档的偏离，理由与读数已写进代码注释。 |
| V-97 | **`render` 分支变不出「坐姿」**：§4.2 要求 `RenderLivingEvent.Pre` 里设 `model.riding = true`，实测 21.1.213 字节码里该事件在 `render` 入口（偏移 3）触发，而 `model.riding` 在**偏移 89** 由 `isPassenger() && getVehicle().shouldRiderSit()` 重写 ⇒ Pre 里的赋值被覆盖 | **结论：坐姿只能由 mount 分支产生**；render 分支实际只提供「压低 + 旋转 + 压缩」三项变换。该行按 §4.2 保留并在注释写明，**但 F-112 的两条分支对照必须据此重述**——render 分支不是「另一种坐姿实现」，而是「无坐姿的变换版」。此发现已在本轮回报里明确，留给作者在 F-112 拍板时参考。 |
| V-98 | **客户端启动时报「自检 实体类型有 13 个，渲染器只注册了 11 个」**，并伴随 `friend_chives` / `vibration_bean` / `chive_seeds` 的 model 缺失告警 | **属 Phase 16 之后的预期状态，不是缺陷**：F-103 把 `entity_types` 提到 13，而两个新投掷物的渲染器与三处资源要等 Phase 18（F-114/F-122）与 Phase 20（F-130）才落地。**判据时点**：这两个数应当在 Phase 20 出口前收敛到 13 / 13；在那之前看到这条 ERROR 不必排查。 |
| V-99 | **F-112 两条坐姿分支对照数据**（只采数据，保留哪条由作者定）。对照表：

| 对照项 | render 分支 | mount 分支 |
|---|---|---|
| 是否有坐姿 | 无（model.riding 在偏移 89 被 isPassenger && shouldRiderSit 重写，render 下非乘客 ⇒ 写入 false） | 有（真乘客 ⇒ 写入 true；ArmorStand 未覆写 shouldRiderSit ⇒ 默认 true） |
| 自主位移（194 刻） | [+1.95, 0, +2.66] | **[0, 0, 0]** |
| 注入 Motion=[0.6,0,0] 净位移 | [+1.21, 0, -0.09]（被摩擦吃掉） | **[0, 0, 0]**（下一刻 Motion 被改写成 [-0.003,-0.078,-0.003]，位置不动） |
| /tp 指令 | 生效（0.5 → 5.5） | **指令回成功但实体没动**（rideTick 偏移 1 setDeltaMovement(ZERO) + 偏移 25-40 positionRider 钉住） |
| 能被推动/挤开 | 能（sub [-0.44,0,+0.29]、牛 [+0.53,0,-0.35]） | **不能**（ArmorStand.isPushable 恒 false、doPush 空、hasPhysics false） |
| 碰撞箱 | 不适用（无坐骑） | Marker=1b ⇒ MARKER_DIMENSIONS = fixed(0,0)，**0×0** |
| 乘客 Y 偏移 | 不适用 | **随体型变**：僵尸（高 1.95）-0.70、牛（高 1.4）0.00；僵尸 OnGround=0b |
| 第三方模型认不认 riding | **无法测**（本机 run*/mods 全部 0 个 jar） | 同左 |
| 观感 | 留待作者 | 留待作者 |

**三条要点（不含推荐）**：① render 给不出坐姿，两条分支不是「两种坐姿实现」而是「无坐姿 vs 有坐姿」；② mount 下乘客被完全钉住——连 /tp 都静默失效（指令回成功、实体不动），对服主与指令用户是意外；③ render 下 AI、推挤、/tp 全部正常，但只有「压低+旋转+压缩」。
**方法学坑**：专用服务器无玩家在线时出生区块不 tick 实体 ⇒ 首轮 6 个采样点全停同一坐标、位移全 0 是**假读数**；必须 /forceload add 强制区块后再采。首轮读数已作废未进表。原始读数在 .work/f112-*.txt / .log（未跟踪）。 |
| V-100 | **§12.3 的拴绳规则没实现**：文档要求「`isLeashed()` 为真则不挂隐形坐骑」，但全源码树 `isLeashed|getLeashHolder` 命中 **0**，`QbounceMountController:176/194` 只判 `isPassenger()` ⇒ mount 分支下**拴绳生物会被挂上隐形坐骑**（拴绳者牵着它、它却被钉住，关系会错乱） | **裁定：补判据，不改文档**。理由是这条规则本身正确（拴着的生物不该被隐形坐骑接管），删规则等于把一个真缺陷写成「设计如此」。改动应为在跳过条件里加 `|| entity.isLeashed()`，一行级。 |
| V-101 | **`adoptExisting()` 承诺但未生效**：`QbounceMountController:60-65` 注释承诺「重启后同样会被认领」，F-113 两次起服实测**均未生效**（存档里带 `q_bounce` 的僵尸、区块已 forceload，重启后无「挂上」日志、世界里 0 个模组坐骑）。机制推测：它只在「状态翻转那一 tick」扫一次，若那时实体尚未进 `level.getAllEntities()` 就永不重试 | **裁定：修（有界周期重扫）**。严重度低（效果的 10 秒内状态不一致），但**代码注释在承诺一件它做不到的事**——这比缺陷本身更坏，下一个人会照着注释相信它。修法：每 N 刻（如 20）对有「有效果但无记录且非乘客」的实体补挂，带上限，避免每刻全量扫。 |
| V-102 | F-113 发现的两条**口径澄清**（非缺陷）：① `Mixin` 字面 grep 在 `src/main/java` 有 5 处命中，全是注释里写「无 Mixin」，工具的禁令扫描按语义判为 OK——**不要按字面计数判失败**；② 朋友的韭 AoE 未实现属 Phase 18，故 §12.12 的运行时读数最早在 F-115 才有。 | 两条均记入，供后续验收时对照，避免把「还没做到」误判成「做错了」。 |
| V-103 | **1.21.1 里 `Entity`/`LivingEntity` 没有 `isLeashed()`**：它是 `Leashable` 的 default 方法，只有 `Mob` 实现（`Mob extends LivingEntity implements … Leashable …`；NeoForge `IEntityExtension` 里 leash 命中 0） | V-100 原本写的「`|| entity.isLeashed()` 一行级」**写不进去**。落地为 helper：`living instanceof Leashable l && l.isLeashed()`，在 `adoptExisting` 候选过滤（:224）与 `ensureMount` 跳过条件（:247）各加一项。真机验证：拴绳僵尸（`leash_knot` + `data merge leash:{UUID:…}`）施加 `q_bounce` 后**无「挂上」日志**、其位置无 armor_stand；未拴绳对照僵尸正常挂上。 |
| V-104 | **原版拴绳不跨重启存活**（附带发现，非本项目缺陷）：重启后 `data get … leash` 变 `Found no elements matching leash`、世界里多出 1 个 lead 物品 ⇒ `tickLeash` 的掉绳路径把它扔了 | 因此重启补扫时该僵尸已不算 leashed、被合法挂上。§12.3 只约束「挂载那一刻」，**不违反规则**。若以后要求「拴绳僵尸跨重启也不挂」，那是原版拴绳恢复语义的事，需另开任务。 |
| V-105 | **`adoptExisting` 的有界补扫取值**（V-101 的修复）：`ADOPT_RESCAN_INTERVAL_TICKS = 20`、`MAX_ADOPT_RESCANS = 10`（+翻转 tick 那次 = 每次翻转最多 11 次扫描，≈起服后 10 秒；用完彻底停扫）。每次翻转最多 11 行 `认领扫描#k` 日志，作为有界性的可观测证据 | **决定性读数**（第一次重启不算数，因为实体在已加载区块里、翻转 tick 就认领到了）：把僵尸放在未加载的 chunk (12,12)、释放坐骑后 `forceload remove`、再停服 → 重启后 `扫描#0 候选=0`、`扫描#1 候选=0`（正是 V-101 的机制），`forceload add` 后 `扫描#2 候选=1 新挂=1` ⇒ **重启到挂上实际等待 40 刻**，由补扫完成。两份日志各自恰好 11 条扫描行后不再出现。 |

