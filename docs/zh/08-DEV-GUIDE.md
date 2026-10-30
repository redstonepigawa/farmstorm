# 08 · 开发者手册

> 面向接手这个仓库的人。读完应该能：改代码 → 验证 → 提交，并且不踩我们已经踩过的坑。
> 设计文档见 `00-DECISIONS.md`（唯一真相源）与 `01-DESIGN.md` / `02-TECH-SPEC.md`；**那三份是只读的**，与实现冲突时按 `00-DECISIONS.md` 裁决，实测与文档不符时改代码并在 `.work/PLAN.md` 登记，不要改文档。

## 1. 一句话速览

| 项 | 值 |
|---|---|
| 形态 | Minecraft 1.21.1 · NeoForge 21.1.x · Java 21 模组 |
| modid | `farmstorm` · 包名 `com.dashpig.farmstorm` |
| 规模 | **74 个 Java 类 / 6,889 行**；169 个生成的 JSON + 91 张材质 |
| 注册对象 | 64 个（v0.0.1：items 22 / blocks 20 / BE 6 / entity 9 / recipe 2 / tab 1 / damage 2 / component 1 / menu 1）；**v0.0.2 为 77 个**（13 个新注册对象，明细见 `13-TASKS-0.0.2.md` F-66） |
| config | **117 键**（v0.0.1，封闭集合，见 §4.3）；**v0.0.2 为 139 键**（117 + 22） |
| 架构 | **服务端权威**；0 个自定义网络包；**无 Mixin** |

## 2. 代码地图

包结构是硬的（`02-TECH-SPEC.md` §2 定的），不要新增顶层包。

| 包 | 文件 / 行 | 职责 |
|---|---|---|
| `config` | 1 / 674 | 117 个配置键 + `bake()` 派生快照（幸运浆果千分比表、爆炸阈值） |
| `datagen` | 10 / 1055 | 全部资源生成的 Provider：blockstate/模型/配方/战利品/进度/语言/tag/伤害类型 |
| `util` | 10 / 777 | `CropProfile`（15 条静态档案）、`CropType`/`CropCategory`、`LuckyBerryRoller`、`TempEnchantHandler`、`DistanceMetrics` 等 |
| `blockentity` | 2 / 751 | `DeployedCropBlockEntity`（部署物状态机 + tick 分发）、`ConverterBlockEntity`（转换事务） |
| `entity` | 10 / 737 | `ThrownCropEntity` 基类 + 8 个投掷物 + `TurretBullet` |
| `behavior` | 11 / 630 | 行为策略：`DeployedBehavior` / `TrapBehavior` / `TurretBehavior` / `AuraProvider` + 5 个 impl |
| `registry` | 9 / 417 | 只放 `DeferredRegister` 与 Supplier，**不含逻辑** |
| `block` | 3 / 417 | `FarmstormCropBlock`（14 作物共用）、`DeployedCropBlock`（5 部署共用）、`ConverterBlock` |
| `explosion` | 3 / 351 | `FarmstormExplosion`（三段式）、`BlastRule`（破坏策略）、`ExplosionQueue`（同 tick 限流） |
| `item` | 5 / 306 | `ModifiedCropItem` 基类 + 投掷/部署/播种/食用四个子类 |
| `client` | 3 / 189 | **仅客户端**：菜单屏、导弹渲染、事件订阅。专用服务器不会加载 |
| `menu` / `recipe` / `event` / `team` | 1+2+1+2 / 500 | 转换器菜单、转换配方与序列化、game 总线事件、己方判定 |
| （根）`Farmstorm.java` | 1 / 85 | 模组入口、8 个 registry 挂载、启动自检 |

改代码前先想清楚落在哪个包：**注册去 `registry`、行为去 `behavior`/`entity`/`blockentity`、显示去 `client`、资源去 `datagen`**。

## 3. 三个不可动摇的约束

### 3.1 服务端权威
所有会改变世界状态的判定（伤害、掉落、归属、效果、随机、转换结果）**只能在服务端**。客户端不发任何数值、不决定任何结果。`client` 包里的类不得出现在专用服务器可达的路径上。

### 3.2 数值只有一个来源
玩法数值一律从 `FarmstormConfig` 读；源码方法体里**不许出现"看起来是玩法数值"的字面量**。
内部常量有指定位置（安全阀 6 个：部署/投掷冷却、单区块/单玩家上限、爆炸与效果熔断），**不许挪进 config**。

### 3.3 型与类是正交二维（容易搞混）
| 维度 | 基数 | 取值 | 位置 |
|---|---|---|---|
| **型 Type** | **单值** | 投掷型 / 部署型 / 食用型 | `CropProfile#type()`（`CropType`） |
| **类 Category** | **多值** | 陷阱 / 炮台 / 破障 / buff / 建造 / 伤害 | `CropProfile#categories()`（`Set<CropCategory>`） |

两者**不得互相推导**，也不能用 `DeployedKind`（部署行为实现种类，见 C-4「分类是炮台、实现是光环」）顶替任何一维。

「能不能破坏方块」**由 `DEMOLITION` 标签驱动**：爆炸调用点统一写 `BlastRule.forCrop(profile, 破坏规则)`，非破障类自动退化为 `NEVER_BREAK`。**不要**在调用点自己判断作物类型。

## 4. 构建与运行

### 4.1 必须走 `gw.cmd`

```cmd
cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava   :: 只编译，最快反馈
cmd /c ""D:\project_farm\.tools\gw.cmd" build          :: 出 jar
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer      :: 起专用服务器
cmd /c ""D:\project_farm\.tools\gw.cmd" runData        :: 重新生成 src/generated/resources
cmd /c ""D:\project_farm\.tools\gw.cmd" runClient      :: 起开发客户端
```

它负责四件缺一不可的事：设 JDK 21 与 `GRADLE_USER_HOME`、拉起 NeoForge 镜像代理、注入 java.base 补丁、给 `runServer` 加目录锁。**直接调 `gradle` 会失败**（详见 `07-PUBLISH.md` §6）。

一键脚本：`一键构建.cmd` / `启动客户端.cmd` / `启动服务器.cmd` / `检查环境.cmd`。

### 4.2 并行跑服务端要隔开目录
两个进程同时 `runServer` 会撞 `run/world/session.lock` 并互相覆盖日志。用 `-PfsRunDir=` 指定独立运行目录：

```cmd
cmd /c ""...\gw.cmd" runServer -PfsRunDir=run-myfeature
```

`gw.cmd` 另有 `runserver.lock` 目录锁做串行兜底（含陈旧锁自动接管）。

### 4.3 改 config 要注意
117 个键是**封闭集合**（v0.0.1 口径；v0.0.2 为 **139** 键）：不许新增、不许改名、不许删除。真要加键，先改 `01-DESIGN.md §3.8`（0.0.2 的新键记在 `12-TECH-0.0.2.md §8`），否则 `tools/verify_project.py` 会失败。
⚠️ 改**默认值**不会影响已生成的 `config/farmstorm-server.toml`——要让新默认值生效，得删掉该文件重新生成，或手动改值。

⚠️ **改 `run-*/config/farmstorm-server.toml` 时不要用 `Set-Content -Encoding utf8`**：PowerShell 会写 **UTF-8 BOM**，NeoForge 判文件无效、改名成 `.bak` 并按默认值重建，改动被静默吞掉；服务器运行中改还会被 FileWatcher 回写。正确做法是**先停服**，再用 `[System.IO.File]::WriteAllText($path, $text, (New-Object System.Text.UTF8Encoding($false)))`，写完核对首字节是 `91,99,111`（`[co`）而不是 `239,187,191`（BOM）（V-33）。

## 5. 一次典型改动的工作流

```text
1) 读 .work/PLAN.md 相关裁决 + 02-TECH-SPEC 对应小节（不要通读全部文档）
2) 说出三句话：改哪个类 / 涉及哪个注册 ID 或 config 键 / 用哪条约束兜底
3) 写代码 → cmd /c ""...gw.cmd" compileJava        （一次只做一件可编译验证的事）
4) 涉及资源 → gw.cmd runData，确认产物真的变了（幂等时 written: 0 是对的）
5) 收尾 → gw.cmd build + python -X utf8 tools/verify_project.py（必须 FAIL 0）
6) git add -A && git commit -m "..." && git push
```

## 6. 验收工具

`tools/verify_project.py`（用仓库运行时的 python 跑，PATH 里的 `python` 可能是 Microsoft Store 空壳）：

| 检查项 | 内容 |
|---|---|
| config | 从 `01-DESIGN.md §3.8` 现算 117 键，与 `run/config/farmstorm-server.toml` **逐键比对**（**仍是 v0.0.1 口径**：0.0.2 实际 139 键，脚本未升级前这一项对 22 个新键零覆盖） |
| 注册 ID | 64 个 ID 的全源码字面量核对 —— **这是子集检查**（只验「文档那 64 个能否在源码里找到字面量」），既不查多余对象也不查各 registry 总数，所以 0.0.2 新增的 13 个对象它完全看不见，仍然 FAIL 0 |
| 语言 | `zh_cn` 与 `en_us` 键集必须完全相同 |
| 禁令 | 无 `@Mixin`、无 `TODO(api-unverified)`、无 `printStackTrace`/`System.out.println`、无空 catch、无 `[PROBE]` 残留 |

其它门禁：`runData` 第二次跑应 `written: 0`（幂等）；jar 里不得含 `*Probe*` 类（**要扫 jar 本身**——`verify_project.py` 目前只扫源码，删了源码但没重建时陈旧 class 仍会进 jar）；`mods.toml` 解析出的版本必须等于 `gradle.properties` 的 `mod_version`。

⚠️ **脚本的风险不是「变红」而是「静默失明」**：0.0.2 口径（77 注册对象 / 139 config 键 / 261 资源 / 263 语言键位）它一条都没覆盖，跑出 FAIL 0 并不代表文档与实现一致。升级脚本时必须对每个 registry 同时校验「缺失」与「多余」，并加上 jar 内 `Probe` 条目为 0 这条。

## 7. 已经踩过的坑（照这个表躲）

| 现象 | 根因 | 正确做法 |
|---|---|---|
| 方块放下后完全看不见 | `BaseEntityBlock#getRenderShape` **默认 `INVISIBLE`** | 必须重写返回 `RenderShape.MODEL`；隐形陷阱才靠"空模型（无 elements）" |
| 十字/植物模型渲染成黑块 | 原版 `block/cross` 的 `render_type` **不随 parent 继承** | 在自己的模型 JSON 里显式写 `"render_type": "minecraft:cutout"` |
| GUI 背景糊成一团、槽位全错位 | `GuiGraphics.blit` 的**六参重载把纹理尺寸硬编码成 256×256** | 用九参重载显式传真实尺寸 |
| 爆炸伤害恰好只有 1 点 | 爆炸中心在实心方块内 → `Explosion#getSeenPercent` 归零 → 公式只剩 `+1.0` 保底 | 让中心落在空气里（先移除方块再爆炸）；`hurtCapped` 另有"抬升取样中心"的防御 |
| 扔投掷物客户端崩 `entityrenderer is null` | 实体类型没注册客户端渲染器 | 每个 `EntityType` 都要 `registerEntityRenderer`；`ThrowableItemProjectile` 用 `ThrownItemRenderer` |
| 界面上根本没有按钮 | 只有菜单层实现了 `clickMenuButton`，Screen 层没加控件 | 服务端逻辑与客户端控件是两件事，`Screen#init` 里必须 `addRenderableWidget` |
| `/reload` 后配方仍命中旧的 | `RecipesUpdatedEvent` 是**纯客户端**事件 | 服务端在 `AddReloadListenerEvent` 里清缓存，并做 `byKey` 身份校验 |
| `/reload` 直接卡死、服务器起不到 Done | `PreparableReloadListener` 没调 `barrier.wait(...)` | 所有 reload listener 必须 `barrier.wait(Unit.INSTANCE).thenRun(...)` |
| 无玩家时实体完全不动 | 没人的区块**不 tick** | 测试前 `forceload add`，并轮询确认实体真的进入了世界再计时。**`forceload add` 的参数是方块列坐标不是区块坐标**：想加载区块 (6,6) 要写 `forceload add 96 96 104 104`，写成 `forceload add 6 6` 只标记了区块 (0,0)，`execute if block` 会全报 `That position is not loaded`，投掷物在未加载区块里当场消失、整轮统计为 0 |
| 复跑时结果莫名偏大（如干态 36 格 hay） | 上一轮作废时留下的**冻结实体随区块加载复活** | 验收脚本开头先「`kill` 三连 + 静置 1.5s + 清场后基线断言」（目标位置 hay=0 / compressed=0），基线不成立整轮作废；世界被污染时直接删 `run-*/world` 全新重跑 |
| 临时探针类混进 jar | 删了源码**但没重建**，陈旧 class 还在 `build/classes` 与 jar 里 | 删源码后**重新 build**，并确认三处为 0：`src` / `build\classes` / **jar 内**。⚠️ **并行期间禁止 `clean`**：`:clean` 会先删 `build/`，若另一条 lane 的 java 正占用 `build/moddev/artifacts/*.jar`，删除会失败并把 `build/` 删掉一半，后续构建状态不可信——只用增量 `build`；确需 clean 时先确认全机没有别的 lane 在跑（`Get-Process java` 看窗口标题判断归属），clean 后重新完整 build 一次 |
| `runData` 只生成 0 个文件 | `runs.data` 缺 `programArguments` | `--mod <id> --all --output ... --existing ...`（已在 `build.gradle` 里） |
| 材质生成后竹子图标变了 | 用了 1.21.1 客户端 jar | 材质脚本必须指向 `26.3` 客户端 jar（`gen_textures.py --vanilla-assets`） |
| 1.21.1 编译不过的常用签名 | 文档按旧版写的 | `MobEffects.DAMAGE_BOOST/DAMAGE_RESISTANCE/JUMP/MOVEMENT_SPEED/DIG_SPEED`；`PushReaction`；`ColorParticleOption.create(...)`；`Creeper` 无 `setPowered` |
| 临时附魔到期后附魔还在（如火焰附加永不消失） | `TempEnchantHandler.grant` 遇到「已过期但 20 刻扫描器还没清掉」的旧记录会 `continue` 跳过，随后 `previous = stack.getEnchantmentLevel(...)` 读到的是**当前（临时）等级 1**，新记录写成 `previous=1`，到期只会「还原」成 1 | `grant()` **开头先 `revokeExpired(stack, now)`**，再读 `previous`；`previous` 必须是玩家真实等级（干净物品 = 0）。V-77 |
| 灰度后处理让水下完全看不见 | 水下画面偏暗、低对比，取亮度后糊成一片均匀的灰；初步以为是「太暗」，试着抬伽马、去雾、去蓝色蒙版都不够 | **给水下单独一条灰度链**（提亮暗部 + 拉对比），而不是停掉灰度。判据：作者开 F3+F4 面板（后处理未生效）时画面正常 ⇒ 元凶就是后处理。V-81 |
| 索敌锥比预期宽得多（面前一片全中） | `searchAngleDegrees` 直接当半角用了：设 90 实际是 180° 半球，不是 90° 锥 | 配置值按**全顶角**解释、比较时取半角（`maxAngle * 0.5`）；默认 60 = 半角 30°（V-82） |
| 伤害日志显示「名义 8.4」但实际只掉 5，误判成封顶没生效 | 封顶与盔甲减免都在 `hurt()` 内部结算，日志只打了名义值 | 日志固定打**双读数** `名义伤害 X 实际掉血 Y`（V-82） |
| 起完服务端杀不掉：`Stop-Process -Id <pid>` 报 `Access is denied` | 沙箱不允许终止**别的作业**里的进程（服务端是另一个后台作业起的） | ① 优先在 `server.properties` 里开 `enable-rcon=true`，用 RCON `stop`（优雅停机、世界会落盘）；② 否则用 `job_kill` 取消**你自己那个后台作业**（F-101 就是这么收尾的，java 归零）；③ 强杀会跳过 `gw.cmd` 里的 `rmdir %RUNLOCK%`，留下 `runserver-<tag>.lock` **目录**，按「无 java 进程 + 目录空」判据确认后手工删 |
| 服务端日志里找不到「自检 实体渲染器 11/11」 | 那行来自 `client/FarmstormClient` 的 `logRendererSelfCheck()`，**客户端专属**；dedicated server 不加载 `client/` 包 | 验渲染器自检必须起**客户端**（看 `[Render thread/INFO]`）；runServer 的日志只该有注册对象 / 作物 / 池 三条，别把它当成「没生效」 |
| 用 `Get-Content` 数中文文档行数，比实际**少两三成**，看起来像文件被截断 | 仓库里的 `.md` 全是**无 BOM 的 UTF-8**；中文 Windows 上 `Get-Content` 默认按 **GBK(936)** 解码，UTF-8 三字节序列被成对吞掉 ⇒ 行数和字都变少（`主提示词` 会被读成 `涓绘彁绀鸿瘝`）。英文文档几乎全 ASCII，任何 8 位解码都对，所以症状是「英文正常、中文全错」 | 量行数一律用 `[IO.File]::ReadAllLines($p).Count` 或 `Get-Content -Encoding utf8`。判据：行数必须等于 `.work/v003-manifest.md` 的「行数」列。**误诊代价很高**——会把完好的文档当成截断去重写（V-89） |
| 依附方块被挖，上面的陷阱方块**掉两个** | `updateShape` 返回 AIR 后，**原版形变更新自己会走 `updateOrDestroy → destroyBlock(pos, true)` 按战利品表掉一次**；我们又在 `updateShape` 里 `dropResources` 一次 ⇒ 两条路径各掉一个。老的「放这里免得 `onRemove` 复制」注释防错了对象 | **`updateShape` 里不要自己掉落**，交给原版移除路径（一定掉、只掉一次）；要「不掉」时先 `removeBlock(pos, false)` 再返回 AIR。判据：破坏依附方块掉 1、玩家直接挖也掉 1（V-86） |
| 服务端发的音效「钉在原地」不跟玩家走 | `ClientboundSoundPacket` 把**坐标烘进包里**——发包那一刻的位置被固化，玩家走开声音留在原地 | 要跟随就用 **`ClientboundSoundEntityPacket`**（绑实体，客户端 `EntityBoundSoundInstance` 每刻从实体读坐标）。判据：实例坐标↔玩家当前 = 0.00 **且** 实例坐标↔发包旧坐标 = 距离（V-88） |
| 改了 config 默认值/键名，但跑起来还是旧行为 | **`run-*/config/farmstorm-server.toml` 不会自动跟随代码**：键缺失取默认值（可能正是你要改的那个）、旧键名残留则完全不生效。本轮踩过两次（`run-p13` 缺 `deployDropOnSupportLost`；`run-p14` 还是 `levitationLevel` 时代的旧键） | 改 config 后固定动作：**删掉 `run-*/config/farmstorm-server.toml` 让它重新生成**，或起服前 `Select-String` 核一遍键集（旧键 0 处、新键各 1 处）。否则整轮读数都在读默认值（V-87） |

## 8. 调试手法

**服务端**：`gw.cmd runServer -PfsRunDir=run-xxx` → 起好后用 RCON 下命令（`run-xxx/server.properties` 里开 `enable-rcon`）。验收世界状态用 `execute if block ...`，比读日志可靠。

**客户端**（渲染/界面问题只能这样验证）：`gw.cmd runClient -PfsRunDir=run-xxx`。

> **客户端「无栈直接退出」先看 crash-reports**：进程突然没了、`latest.log` 里没有任何 `Exception` 时，别先假定是外部 kill——去 `run-<dir>/crash-reports/` 看一眼。v0.0.2 有三次被判成「被别的 lane 杀了」，实际每次都有完整报告（例如 `IllegalStateException: Missing elements in vertex: UV0, UV1, Normal`，来自剪影渲染把拴绳塞进 entity 缓冲区，V-58）。

#### ⚠️ 先记住这条：主菜单不写日志，**别把静默当卡死**

客户端初始化到 `Loaded 0 entity animations` 之后就**不再输出任何日志**——那不是卡住，是**已经停在标题画面**了。
v0.0.2 施工期有两次因此误判「客户端进不去世界」，各浪费了半小时。判定方法只有两种：

```powershell
# 1) 看窗口标题：在主菜单是 "Minecraft NeoForge* 1.21.1"，进世界后会变成 "… - Multiplayer (3rd-party Server)"
Get-Process java | Where-Object { $_.MainWindowTitle } | Select-Object Id, MainWindowTitle

# 2) jstack 在本仓库被沙箱拒绝（"拒绝访问"），不要指望线程栈。
```

#### 服务端假玩家（FakePlayer）测量的三个坑

1. **落地判定**：`Player#getDigSpeed` 里有 `if (!onGround()) f /= 5.0F`。用 FakePlayer 测挖掘速度前必须 `setOnGround(true)`，否则读数比真实玩家慢 5 倍，结论会反过来（V-27）。
2. **属性与统计**：`FakePlayer#awardStat` 是**空实现**，`BLOCK_MINED` 一类计数器恒为 0——拿它当「事件有没有触发」的判据没有判别力（V-26）。
3. **原版副作用**：假玩家会静默吞掉一大批原版副作用（统计、部分事件）。用统计量做证据前，先确认它在被测环境里真的会变。

#### 进世界的正确做法（已实测，2026-10-29）

1. **先起一台服务端**，记下端口（`run-xxx/server.properties` 的 `server-port`，`online-mode=false`）。
2. 用 init script 把 quickPlay 参数塞进 MDG 生成的 argfile——`.work/client-quickplay.gradle` 就是现成的：

   ```powershell
   $env:FS_QUICKPLAY_PORT = "25608"
   cmd /c ""D:\project_farm\.tools\gw.cmd" runClient -PfsRunDir=run-client `
        --init-script "D:\project_farm\.work\client-quickplay.gradle" --console=plain"
   ```

   **不要用 `--args`**（会被 devlaunch 当主类吃掉）；参数只能写进 `build/moddev/clientRunProgramArgs.txt`，
   而该文件由 MDG 每次生成，所以必须在 `runClient.doFirst` 里追加。跑完记得把那一段清掉，否则污染下一个人。
3. 成功的标志（客户端与服务端各一行）：

   ```
   [Render thread/INFO] [net.minecraft.client.gui.screens.ConnectScreen/]: Connecting to 127.0.0.1, 25608
   [Server thread/INFO] [net.minecraft.server.MinecraftServer/]: Dev joined the game
   ```

4. `--quickPlaySingleplayer` 在本仓库**不生效**（会卡在无障碍引导页，需 `options.txt` 里 `onboardAccessibility:false`）；进存档只能靠临时钩子调 `Minecraft#createWorldOpenFlows().openWorld(...)`。
5. 截图：`Screenshot.takeScreenshot(...)`，产出图能直接看，别用「应该没问题」结案。

> 一次完整往返（起服 → 客户端进世界 → 截图 → 关掉）约 3 分钟。**收尾只杀自己起的那个进程**：起进程时记 PID，收尾用 `Stop-Process -Id <自己的 PID>`；服务端优先用 RCON `stop` 优雅停机；锁只删自己那把（`runserver-<FS_RUN_LOCK>.lock`）。**不要 `Get-Process java | Stop-Process -Force`**——共享机器上会杀掉另一条 lane 的客户端，症状是对方「无崩溃栈、无 crash report、直接 exit -1」，极难归因（V-37）。

**临时探针规则**：验证完必须删干净——源码、`build/classes`、jar 三处都不能留 `*Probe*`；日志里不要留高频 `LOGGER.info`。**证据要落 `.work/`，不要只留在 `run-*/logs/latest.log`**：run 目录的日志重启就轮转，前一版的矩阵结果就是从 `.log.gz` 里捞回来的（V-15）。验收前先查 `git status` 有无 `debug/`、`*Probe*` 残留。

## 8.4 发布前必查（四处一致，缺一处就是发错东西）

GitHub 的 release 页面上除了你上传的资产，还有两份**自动生成的 `Source code` 压缩包**——它们是**按 tag 打的包**。
所以「发了新 jar」不等于「发布件是对的」：

| # | 检查项 | 为什么会错 | 怎么核 |
|---|---|---|---|
| 1 | **tag 指向 == HEAD** | `gh release create --target main` 只在**创建时**决定 tag 位置；之后 `git push` **不会**移动 tag。tag 停在旧提交时，`Source code` 是旧快照，而 jar 是新的 | `gh api repos/<owner>/<repo>/git/refs/tags/<tag>` 对比 `git rev-parse HEAD` |
| 2 | **release 里的 jar == 本地 dist 的 jar** | 改完代码只 build 没重传，或传错文件 | 比对资产 `digest` 与 `Get-FileHash` |
| 3 | **`install-notes.txt` 也跟着更新** | 最容易漏：只改了 release 正文（`--notes-file`），忘了**附件**里的说明。正文说了新特性、附件还是旧版，读者拿到的是旧行为描述 | 比对 `digest` 与本地 `dist/安装说明.txt` |
| 4 | **release 正文提到本版的改动** | 正文停在上一版文字 | 人工看一眼，或让它含「更新」小节 |

**这一步不要靠记性，跑脚本**：

```
powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools\verify_release.ps1 -Tag v0.0.2
```

四项全 PASS 才发；任一项 FAIL 会打印原因并以 exit 1 结束。脚本已自测过失败路径（用旧 tag 跑会正确报 FAIL），不是「永远绿」的摆设。

**写 .ps1 的两个坑**（都踩过）：
1. **非 ASCII 的 .ps1 必须带 UTF-8 BOM**：`powershell.exe`（5.1）默认按 GBK 读脚本，中文会被打乱、直接变成语法错误。注意这与 **TOML 相反**——配置文件必须**无 BOM**（V-33）；
2. 别在双引号字符串里嵌 `$(if (...) { ... } else { "" })`：5.1 会解析失败。先把值算进变量再插值。

**发布顺序（照这个走，四项自然一致）**：
```
1. 改完代码 → build → 把 jar 拷进 dist/
2. 更新 dist/安装说明.txt（正文与 jar 同步改，别只改一处）
3. git commit && git push（分支）
4. git tag -f <tag> HEAD && git push --force origin refs/tags/<tag>   ← 保证 tag == HEAD
5. gh release upload --clobber <jar> <install-notes>
6. gh release edit --notes-file <正文>
7. 跑 tools/verify_release.ps1，四项全 PASS
```

## 8.5 已知限制（玩家可见，不是缺陷）

| 现象 | 原因 | 处置 |
|---|---|---|
| 相位 2 期间鼠标转视角只降速、不锁死 | `CalculatePlayerTurnEvent#setMouseSensitivity(0)` 后系数仍为 0.2（慢约 15.6 倍），无 Mixin 归不了零 | 已知限制，验收按「转速降至约 1/15.6」记（V-31） |
| 1×1 石柱构型的相位 2 落地时长偏短（约 23 刻） | 漂浮段的横向漂移把玩家挪出柱顶，下坠中途被自己站的柱子截住；≥3×3 台面是 40 ± 2 刻 | 真实几何结果，不作缺陷；验收用的「高台」指 ≥3×3 台面（V-46） |
| 狂野猎人期间打开设置界面，亮度滑条显示为满 | 水下可视性需要临时抬高 `options.gamma()`，而原版选项系统没有「临时覆盖」 | 已知限制；关闭界面 / 效果结束后滑条与亮度都恢复原值 |
| 火把花光环：离开范围后火焰附加最多再留 15 秒 | `TempEnchantHandler.revokeAll()` 目前**没有调用者**，离开光环不会立刻撤销，只由 300 刻到期撤销 | 已知限制（验收口径是「15 秒内消失」）；若要「离开立即撤销」需加「上一刻在光环内、这一刻不在」的边沿检测 |

> `Unknown recipe category: farmstorm:convert`（`WARN ClientRecipeBook` ×3）**已由 `client/ConverterRecipeBookCategory` 修掉**（走 `RegisterRecipeBookCategoriesEvent#registerRecipeCategoryFinder`，V-67），**不再列为已知限制**。

## 9. 提交与发版

日常：`git add -A && git commit -m "..." && git push`。
发版（改版本号 → 构建 → tag → push tag → 新 Release 挂新 jar）见 `07-PUBLISH.md` §8。**一个版本一个 tag 一个 Release，不要覆盖旧版**；不要 `git push --force`。

## 10. 不要做的事

- ❌ 改 `plan0.0.1.txt` / `docs/zh/00~02`（只读）。发现文档缺陷 → 写进 `.work/PLAN.md`，由作者裁决
- ❌ 引入 Mixin、新增构建依赖、新增网络包
- ❌ 新增/改名/删除 config 键（117 封闭）
- ❌ 在 `registry` 包里写逻辑，或在 `client` 包里做判定
- ❌ 手写 `src/generated` 下的 JSON（下次 `runData` 会被覆盖）
- ❌ 手改 PNG（材质一律由 `tools/gen_textures.py` 生成）
- ❌ 留 `TODO` / 空方法 / 注释掉的代码 / 空 catch / 调试输出
- ❌ 把"我跑过了"当证据——**贴命令原文与关键输出行**；没验证的就写"未验证"
