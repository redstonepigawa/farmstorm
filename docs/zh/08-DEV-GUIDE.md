# 08 · 开发者手册

> 面向接手这个仓库的人。读完应该能：改代码 → 验证 → 提交，并且不踩我们已经踩过的坑。
> 设计文档见 `00-DECISIONS.md`（唯一真相源）与 `01-DESIGN.md` / `02-TECH-SPEC.md`；**那三份是只读的**，与实现冲突时按 `00-DECISIONS.md` 裁决，实测与文档不符时改代码并在 `.work/PLAN.md` 登记，不要改文档。

## 1. 一句话速览

| 项 | 值 |
|---|---|
| 形态 | Minecraft 1.21.1 · NeoForge 21.1.x · Java 21 模组 |
| modid | `farmstorm` · 包名 `com.dashpig.farmstorm` |
| 规模 | **74 个 Java 类 / 6,889 行**；169 个生成的 JSON + 91 张材质 |
| 注册对象 | 64 个（items 22 / blocks 20 / BE 6 / entity 9 / recipe 2 / tab 1 / damage 2 / component 1 / menu 1） |
| config | **117 键**（封闭集合，见 §4.3） |
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
117 个键是**封闭集合**：不许新增、不许改名、不许删除。真要加键，先改 `01-DESIGN.md §3.8`，否则 `tools/verify_project.py` 会失败。
⚠️ 改**默认值**不会影响已生成的 `config/farmstorm-server.toml`——要让新默认值生效，得删掉该文件重新生成，或手动改值。

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
| config | 从 `01-DESIGN.md §3.8` 现算 117 键，与 `run/config/farmstorm-server.toml` **逐键比对** |
| 注册 ID | 64 个 ID 的全源码字面量核对 |
| 语言 | `zh_cn` 与 `en_us` 键集必须完全相同 |
| 禁令 | 无 `@Mixin`、无 `TODO(api-unverified)`、无 `printStackTrace`/`System.out.println`、无空 catch、无 `[PROBE]` 残留 |

其它门禁：`runData` 第二次跑应 `written: 0`（幂等）；jar 里不得含 `*Probe*` 类；`mods.toml` 解析出的版本必须等于 `gradle.properties` 的 `mod_version`。

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
| 无玩家时实体完全不动 | 没人的区块**不 tick** | 测试前 `forceload add`，并轮询确认实体真的进入了世界再计时 |
| 临时探针类混进 jar | Gradle 会在 `build/tmp` 留 stash | 删源码后跑 `clean build`，并 `grep` jar 内 `Probe` 为 0 |
| `runData` 只生成 0 个文件 | `runs.data` 缺 `programArguments` | `--mod <id> --all --output ... --existing ...`（已在 `build.gradle` 里） |
| 材质生成后竹子图标变了 | 用了 1.21.1 客户端 jar | 材质脚本必须指向 `26.3` 客户端 jar（`gen_textures.py --vanilla-assets`） |
| 1.21.1 编译不过的常用签名 | 文档按旧版写的 | `MobEffects.DAMAGE_BOOST/DAMAGE_RESISTANCE/JUMP/MOVEMENT_SPEED/DIG_SPEED`；`PushReaction`；`ColorParticleOption.create(...)`；`Creeper` 无 `setPowered` |

## 8. 调试手法

**服务端**：`gw.cmd runServer -PfsRunDir=run-xxx` → 起好后用 RCON 下命令（`run-xxx/server.properties` 里开 `enable-rcon`）。验收世界状态用 `execute if block ...`，比读日志可靠。

**客户端**（渲染/界面问题只能这样验证）：`gw.cmd runClient -PfsRunDir=run-xxx`。进世界有两种办法，注意 `--quickPlaySingleplayer` 在本仓库**不生效**（会卡在无障碍引导页）：
- 连本机服务端：init script 往 `build/moddev/clientRunProgramArgs.txt` 追加 `--quickPlayMultiplayer 127.0.0.1:25565`（**不要用 `--args`**，会被 devlaunch 当主类）；
- 进存档：临时客户端钩子调 `Minecraft#createWorldOpenFlows().openWorld(...)`；
- 截图：`Screenshot.takeScreenshot(...)`，产出图能直接看，别用"应该没问题"结案。

**临时探针规则**：验证完必须删干净——源码、`build/classes`、jar 三处都不能留 `*Probe*`；日志里不要留高频 `LOGGER.info`。

## 9. 提交与发版

日常：`git add -A && git commit -m "..." && git push`。
发版（改版本号 → 构建 → tag → push tag → 新 Release 挂新 jar）见 `07-PUBLISH.md` §8。**一个版本一个 tag 一个 Release，不要覆盖旧版**；不要 `git push --force`。

## 10. 不要做的事

- ❌ 改 `plan.txt` / `docs/zh/00~02`（只读）。发现文档缺陷 → 写进 `.work/PLAN.md`，由作者裁决
- ❌ 引入 Mixin、新增构建依赖、新增网络包
- ❌ 新增/改名/删除 config 键（117 封闭）
- ❌ 在 `registry` 包里写逻辑，或在 `client` 包里做判定
- ❌ 手写 `src/generated` 下的 JSON（下次 `runData` 会被覆盖）
- ❌ 手改 PNG（材质一律由 `tools/gen_textures.py` 生成）
- ❌ 留 `TODO` / 空方法 / 注释掉的代码 / 空 catch / 调试输出
- ❌ 把"我跑过了"当证据——**贴命令原文与关键输出行**；没验证的就写"未验证"
