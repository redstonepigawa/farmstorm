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
