# v0.2.2 开发派发台账（调度者）

> 上游：`docs/zh/46`~`51`（只读、已冻结）、`plan0.2.2.txt`、`.work/v022-BRIEF.md`（**唯一口径来源**）。
> 基线提交：`86a3acc docs(v0.2.2): 46~51 六份中文文档 + plan0.2.2 + README`。

## 0. 本轮作者拍板的两项（2026-11-05）

| # | 问题 | 定为 |
|---|---|---|
| G-1 | 交付终点 | **同 v0.2.1**：跑完 Phase 46~53 → `dist/` 两支 jar + 安装说明 + README → tag `v0.2.2` → GitHub Release |
| G-2 | 并行度 | **按 49 号 §3.3 的并行簇跑**（稳定 3 路；不再是一个 Phase 一个子代理的严格串行） |

沿用 v0.2.1 的三条不变口径（未经重新裁定，按上版执行）：

- **F-3 渲染/听感类**：服务端与代码层能验的照做；渲染与听感类标【未验证】并列表，**不许编读数**。
- **F-4 文档冲突**：以源码 / 46 号 / 已有裁决为准，子代理自行适配 + 登记偏差；`docs/40`~`51` **全程不改**（`docs/en` 停在 v0.2.1）。
- **子代理禁止任何 git 写操作**；提交由调度者按任务分段做。

## 1. 基线事实（实测，2026-11-05）

| 项 | 值 |
|---|---|
| git | `86a3acc`（上一 tag `v0.2.1` = `635d3c5`） |
| `mod_version` | **0.2.1**（F-267 改成 0.2.2） |
| Java 文件 | **149** |
| 注册面 | **97**（items 33 / blocks 27 / …；F-268 起变 **98**） |
| 口径外 | **1 类**（`farmstorm:ai_suspend`）；本版再 +2 类（`PoiType` / `VillagerProfession`）⇒ 累计 **3 类** |
| config | **176 键 / 60 组**（本版**只改 2 个默认值**：`maxPerPlayer` 2→4、`cultivatorCapacity` 16→24） |
| 语言键 | **338 × 2** ⇒ 本版目标 **340 × 2** |
| 网络包 / Mixin | **2** / **0** |
| 规模 | **19 个任务（F-267~F-285）/ 8 个 Phase（46~53）/ 23.5 裸人日 / M42~M49** |
| 判据 | **30 条技术验收（48 号 §8）+ 27 条边界（47 号 §8）= 57 条** |

## 2. 波次计划（7 波、19 任务、并行度 ≤3）

| 波 | 并行任务 | 依赖已满足 | 备注 |
|---|---|---|---|
| **W01** | **F-267** ‖ **F-268** ‖ **F-270** | — | F-268 是「双面起点」，`FarmstormItems` + `Farmstorm.java` 断言 + `verify_project.py` 期望面**同一提交** |
| **W02** | **F-272** ‖ **F-269** ‖ **F-271** | F-268、F-270 | F-269 等 F-268；F-271 等 F-270（同文件，串在天然后续） |
| **W03** | **F-275** ‖ **F-273** | F-268、F-272 | F-275 改 `Farmstorm.java`（F-268 之后，无冲突） |
| **W04** | **F-277** ‖ **F-276** ‖ **F-274** | F-275、F-273 | F-276/F-277 都等 F-275 但文件不相交；F-277 再改 `Farmstorm.java` |
| **W05** | **F-278** ‖ **F-279** ‖ **F-281** | F-268、F-277 | F-281 是**关窗**任务，必须早于 F-283 |
| **W06** | F-280 → F-282（一个子代理顺序做两件） | F-277/278/279；F-281 | F-282 等 F-280 与 F-281 |
| **W07** | F-283 → F-284 → F-285（一个子代理顺序做） | 全部 | Phase 53 全量验收与签字；**不改码** |

**为什么不是 4 路**：49 号 §3.3 给的簇 A 上限是 4，但 `gw.cmd build` 共用 `GRADLE_USER_HOME`，4 个并发构建会互相踩；本台账把**构建串行化**（见 §3），于是 3 路是「构建排队时间」与「并行收益」的平衡点。

## 3. 构建串行化（本版新增的纪律）

并行子代理**可以各自跑构建**，但必须走这把互斥锁（目录原子创建；超 15 分钟视为陈旧锁可直接接管）：

```powershell
$lock = 'D:\project_farm\.tools\build.lock'
for($i=0; $i -lt 180; $i++){
  if(Test-Path $lock){
    $age = (Get-Date) - (Get-Item $lock).LastWriteTime
    if($age.TotalMinutes -gt 15){ Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue }
  }
  if(New-Item -ItemType Directory -Path $lock -ErrorAction SilentlyContinue){ break }
  Start-Sleep -Seconds 5
}
try { & 'D:\project_farm\.tools\gw.cmd' build } finally { Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue }
```

- **禁止直接调 gradle**；**禁止** `cmd /c ""...\gw.cmd" build"` 形式（本机会被 pwsh 拆坏）。
- 起服用各自的 lane（`run-w0Na` 等），`FS_RUN_LOCK` 与 `-PfsRunDir` 同值；**永远不要在 `run/` 里起服**，也**不要动 `run/config/farmstorm-server.toml`**。

## 4. 调度者的复核动作（每波后）

1. 读 `git status` / `git diff --stat`，确认改动面只落在本波任务清单内、且并行子代理之间**没有互相踩文件**。
2. 亲自跑一次 `gw.cmd build` 与 `python -X utf8 tools/verify_project.py`（**不采信自述**）。
3. 通过则**按任务分段提交**（一个任务一个提交；同一文件无法切时合并并在提交信息里写明原因）。
4. 不通过则带编译器原文/读数重派该任务；同一任务最多重派两次。
## 5. 波次登记（调度者裁定与偏差，逐条留痕）

### W01

| # | 事项 | 裁定 |
|---|---|---|
| D-1 | `registry/FarmstormAttachments.java` 被 F-268 lane 改动，但不在我给 F-268 列的「涉及文件」里 | **合法，通过**。那是 A-84 要求的「97」计数注释同步（97→98，共 8 处落点之一）。是我把 F-268 的文件清单写窄了，**A-84 优先于任务书 §0 的文件清单**。后续波次以「49 号 + A-8x」为准，任务书清单只作提示 |
| D-2 | `client/ConverterScreen.java` 与 `OutpostCoreScreen` **同根因**（`extends AbstractContainerScreen` 却没有 `render` 覆写 ⇒ 同样不出 tooltip） | **本版不动**（不在 plan0.2.2 / 47 号 R1~R4 范围，凭 F-4「文档冲突以文档为准」不扩范围）。登记为 **0.2.3 候选**，写进最终交付说明的「已知问题」 |
| D-3 | F-270 把 `render` 覆写在 `renderBg` **之后**（与原版 `ContainerScreen` 顺序相反） | **保持现状**。理由：49 号把 `renderBg` 钉在 `:37-41`，F-271 的涉及文件引用的就是这个行号；放在其后可让该引用在 W02 仍成立、且 `renderBg` 在 diff 里只作上下文出现 |
| D-4 | F-270 在字节码层面自证（`javap -p -c` 显示 `invokespecial render` → `invokevirtual renderTooltip`） | **采纳为额外证据**。48 号 §8 #20~#23 的**游戏内**读数仍标「静态已验 / 运行时待 F-283」，不许拿字节码冒充悬停读数 |
| D-5 | F-267 的「`build/libs` 里 `farmstorm-0.2.1*` 残留 = 0」实测 **2 枚**（mtime 早于本波构建，`build/` 被 `.gitignore` 排除） | **波次收尾时删除**（`Remove-Item build\libs\farmstorm-0.2.1*`）。理由：`build/` 不进 git、且 `dist/` 已归档 0.2.1 的两支 jar 作历史发布件；留着会让 F-283 按 49 号断言判红。删除动作登记在此 |
| D-6 | F-267 落地后「README 说当前发布 v0.2.1、代码已是 v0.2.2」的不一致窗口 | **接受该窗口**。按 49 号，README 由 **F-282** 收口；F-282 在 W06，届时一并改版本行 / jar 名 / 数字快照。窗口期内不许任何任务顺手改 README |
| D-7 | 「全仓还剩哪些 `0.2.1`」的口径 | **以 tracked 文件（`git grep`）为准**；`build/` / `dist/` / `.scratch/` / `run*/` / `.tools/` / 未跟踪的 `.work/` 另列不计入。「合法残留」四类：v0.2.1 冻结文档、v0.2.2 规格里的基线描述、`.work/` 历史轮次、需求原文 `plan0.2.1/0.2.2.txt` |
| D-8 | F-267 顺带跑过 `verify_project.py` 得到 `98/98`、`items 34` | **不作为 F-267 的读数引用**（读到的是 F-268 在途改动）。F-268 的注册面读数由我提交后统一复跑 |
### W02

| # | 事项 | 裁定 |
|---|---|---|
| D-9 | F-272 顺带改了两处类 javadoc 计数（`FarmstormTags:11` 6→7、`ModBlockTagsProvider:13` 3→4），超出「涉及文件」的字面动作 | **通过，保留**。依据 49 号 §0.2 第 4 条「只改注释并进它所在的行为任务，不许分两次提交」+ BRIEF §3.4 #18「注释与代码相反比不写注释更糟」。7 = 4 block + 3 item、4 + 2 = 6 与 `tag(` 调用数自洽 |
| D-10 | 标签命名口径：`plan0.2.2.txt:22` 写 `farmstorm_processing_block`，46 号 D-194 / BRIEF 裁定 #14 / 49 号写 `farmstorm:processing_block` | **按 46 号 D-194 单一实现**：`block("processing_block")` ⇒ 落盘 `farmstorm:processing_block`，产物 `processing_block.json`。**不实现 plan 的旧写法**（D-194 补充段明写「代码与文档一律按带冒号的形式写」） |
| D-11 | `gw.cmd runData` 在 HEAD 上是**红的**：`plain_chives` 已注册但无贴图 ⇒ `ModItemModelProvider:52 basicItem()` 抛 `Texture farmstorm:item/plain_chives does not exist` | **判为窗口 A 的预期中间态，不是缺陷**（49 号 §0.3：F-268 ~ F-269）。处置：F-269 先把 PNG 放进 `textures/item/` 再跑 `runData`；**波次收尾时我亲自再跑一次 `runData` + `build`**，届时 `src/generated` 除本波新增的两个产物外**不应再产生 diff** —— 这本身就是「产物稳定」的强证据 |
| D-12 | F-272 明确**没有**在 F-269 半成品状态下重跑 datagen（避免替别的 lane 生成 `models/item/plain_chives.json`） | **认可这个边界感**。产物归属必须与任务归属一致，否则提交时切不开 || D-13 | F-271 用 `OutpostCoreMenu.ROWS` 限定名，而非 48 号 §2.2.2 示例里的裸 `ROWS` | **通过**。`src/` 全树 `^import static` 命中 0，不为 5 行改动首开风格先例；数值来源仍是 `OutpostCoreMenu.ROWS`（= 6），没写死常量 |
| D-14 | 49 号 F-271 断言 3 写「类行数 42 → **约 47**」，实测 **53**（F-270 已 +6 → 48，F-271 再 +5） | **以实测 53 为准**，登记为文档偏差，进最终**回灌清单**（`docs/` 冻结不改）。这不是 F-271 的错，是 49 号把两次改动合计写成了 47 || D-15 | F-269 把 `ModItemModelProvider:20` 里那串「15 作物 + 7 播种物 + 2 二级合成物 + 4 新物品 + 1 转换器」（合计 29）一并删了，比 48 号 §10 #9 的字面略多 | **通过**。留着会让同一行「数量由注册表决定」与写死的 29 并存，正是 BRIEF §3.4 #18 说的「注释与代码相反比不写更糟」。而且那串本来就已过期（实际 31 → 现 34） |
| D-16 | F-269 把贴图**先出到临时目录、只把新增那一张拷进仓库**，没用全量 `--out` | **通过，且这是正确做法**。全量 `--out` 会重写 98 张，其中 24 张物品图标会丢掉旧绿色「+」——A-61 明写重跑不属于发布动作。生成器仍是唯一产线（`--list` / `--dry-run` / 实际出图都走过），且重跑幂等（同 sha256） |
| D-17 | `gen_textures.py` 头部与 `build_specs()` docstring 的计数（99 张 / 91 条 / 26 或 22 个 id）实际是 100 / 27，F-269 未动 | **只登记、不修**。属**既存漂移**（改前就自相矛盾），与 v0.2.1 留下的 `ModLangProvider:10`「299 键」同类，不在 46~51 号的任何任务动作面内。进最终「回灌候选」清单，不扩本版范围 |

### W02 收尾闸门（调度者亲自跑，2026-11-05）

| 检查 | 读数 |
|---|---|
| `gw.cmd build` | **exit 0** |
| `gw.cmd runData -PfsRunDir=run-f269-data` | **exit 0**（窗口 A 关闭：`plain_chives` 贴图就位后不再抛 `Texture ... does not exist`） |
| **runData 之后再跑一遍的 `src/generated` 变更面** | 仍**只有** `models/item/plain_chives.json` 与 `tags/block/processing_block.json` 两个新文件，**0 个 `M`** ⇒ datagen 产物稳定（D-11 判据成立） |
| `verify_project.py` | `98 / 98`、`FAIL 0 / WARN 1`、**exit 0** |
| `plain_chives.png` | **16 × 16 / 213 B**；`textures/item` 下 **30** 张；`models/item` 下 **34** 个 json |
| `ModItemModelProvider` 的「31 个」 | **0**（`basicItem(` 仍 **1** 处调用） |
### W03

| # | 事项 | 裁定 |
|---|---|---|
| D-18 | 49 号 F-273 断言 1「`instanceof CultivatorBlock` 在 **`src/main/java` 下命中 0**」按字面**不可达**：改后全仓仍有 **3** 处既有命中（`CultivatorBlockEntity:257` 判自己的方块类型、`ModItemModelProvider:65` / `ModBlockStateProvider:79` 按方块类型出 datagen 产物） | **改写为「落点文件（`OutpostCoreBlockEntity`）内词边界命中 0」**，实测该文件 **2 → 0**。那 3 处不属 D-194 判据面，改动会越范围且破坏 datagen。**先例：本版 A-85**（48 号 §8 #26 的 grep 断言按字面不可满足 ⇒ 改写为「3 处白名单」），同一种处置。进**回灌清单**（49 号断言措辞） |
| D-19 | 我在任务书里写的「`FarmstormTags.PROCESSING_BLOCK` 在文件里命中 2」与 49 号的「声明 1 / 调用 2」不能同时是「2 处代码判定」 | **按 49 号的结构验收**：单辅助 `isProcessingBlock`（声明 **1** / 调用 **2**），代码里 `PROCESSING_BLOCK` 只出现 **1** 次（`:803`），字面命中 2 的另一处在 javadoc。**是我任务书措辞不准**，子代理没有为此写两份实现，正确 |
| D-20 | 裸子串 `grep "instanceof CultivatorBlock"` 会命中 `CultivatorBlockEntity`（改后仍有 2 处） | **闸必须用词边界**（`instanceof CultivatorBlock\b`）或限定文件。写进 F-283 / F-285 的判据实现纪律，避免自动闸假红 |
| D-21 | F-273 顺手删了死 import `CultivatorBlock`、加了 `FarmstormTags`（净 0 行） | **通过**。两处判据改完后该类在本文件零引用，留着就是死依赖；一删一加使 `:393` / `:783` 行号不漂移 || D-22 | F-275 的 PoiType 匹配集写 `Set.of(OUTPOST_CORE.get().defaultBlockState())` 单例，而我的派单措辞是「其全部 blockstate 的集合」 | **按 48 号 §2.3.2 的单例保留**。`OutpostCoreBlock` 没有任何 `Property`（无 `createBlockStateDefinition`），全部 blockstate 就是这 1 个，两种写法等价；原版 `PoiTypes.getBlockStates` 是 private 复用不了。**是我派单措辞不准** |
| D-23 | F-275 让 `Farmstorm.java` 多了 **2** 行（1 行 import + 1 行 `.register(modBus)`），而我说「只加 1 行」 | **通过**。验收口径是对 `.register(modBus)` 计数 **12 → 13**；新类在 `registry` 子包，跨包引用必须 import，不 import 就得写全限定名、反而离 48 号 §2.3.6 写死的形态更远。措辞进回灌清单 |
| D-24 | F-275 使 `src/main/java` 的 Java 文件数 **149 → 150** | 记为基线事实（原 `FarmstormPoiTypes` 是新类）。F-285 签字与 F-283 的 `#1` 类断言要用 **150**，不是 149 |
### W04

| # | 事项 | 裁定 |
|---|---|---|
| D-25 | 产物目录我写错成 `data/minecraft/tags/poi_type/`，实际是 **`point_of_interest_type`** | **我的措辞错，以实测与 48 号 §2.3.5 第 3 条 / 49 号 F-276 断言 1 为准**。推导链已核：`Registries.java:177` 的 `POINT_OF_INTEREST_TYPE = createRegistryKey("point_of_interest_type")` → `TagsProvider` 目录 → `data/minecraft/tags/point_of_interest_type/`。**F-283 / F-285 的判据必须按长名写**，写短名会假红 |
| D-26 | F-276 用 HashCache 证明 datagen 幂等：第 1 轮 `written: 1 / removed stale: 0`，第 2 轮 `written: 0` | **采纳为「既有产物零改动」的强证据**，比 `git status` 更硬（内容相同时 git status 看不到）。`src/generated` 跑后仅有 1 条 `??`、0 个 `M` |
| D-27 | 锁外读主 jar 撞见一次 `zip END header not found`（并行 lane 同时在 rebuild jar） | **登记为操作纪律**：jar / 构建产物类读数一律**放锁内**，或改读 `build/classes/java/main/**`。写进 F-283 / F-285 的取证纪律 |
| D-28 | F-276 的旁证：`TagsProvider#run` 对引用条目做存在性校验（缺条目即抛「missing following references」） | **采纳**。本次 datagen 绿 ⇒ `farmstorm:botanist` 在 datagen 期的注册表 lookup 里确实存在 ⇒ F-275 的 POI 注册在 datagen 期生效，不是写了个空引用。可作为 F-283 `#13` 的额外抓手 || D-29 | F-274 的 A-85 确定值 grep（`默认 16\|名额 16\|上界 16\|× 16`）：改前 **11** → 改后 **3**，逐个落在白名单 `ZangiefPiledriverController:52` / `WildHunterClientState:15` / `FarmstormConfig:841` | **通过**。3 个白名单都不是名额语义（上升速度上限 16 格 / loopSoundRange 16.0 / 甘蔗抬升 16 格），不动正确 |
| D-30 | `FarmlandLedger:394`（「容量 16 是硬顶」）与 `:327`（「那 16 格容量」）实为**耕地额度基数** `farmlandBonus`，与 `cultivatorCapacity` 无关 | **确认不改**。BRIEF §3.4 #18 把它们归进「默认 16 的注释」是 BRIEF 的记账错误。进回灌清单 |
| D-31 | **纠正我自己在 W04 中途说的「`FarmstormConfig` 的两个默认值行号由 `:941`/`:954` 漂到 `:943`/`:956`」** | **这句话是错的**。HEAD 上本来就是 `:943`/`:956`；48 号 §2.7.1 自己写的是「`:941` comment / `:942` translation / `:943` defineInRange」，与实读逐行一致。是 **BRIEF #12 与我的 W04 任务书把 comment 行当成了 define 行**。性质是「引用口径写错」，不是「注释增行漂移」。台账以本条为准 |
| D-32 | 唯一真实的行号漂移是 `CultivatorLinkPayload` 的 `MAX_LINKS`：`:35 → :36`（F-274 的注释增行所致，常量值 1024 未变） | 登记；A-90 文档里的 `:35` 引用随之失效，进回灌清单 |
| D-33 | F-274 给出的 F-283 前置：老 toml 仍是 `maxPerPlayer = 2` / `cultivatorCapacity = 16`（实测 `run/config` 未动） | **登记为 F-283 的硬前置**：要读 4 / 24 必须**先删/改名该 toml 让游戏重生成**；拿老 toml 判这两条必然假红。且 48 号 §8 #9 的「红」是**期望中的红**，必须与 #10 成对跑 || D-34 | F-277 用源码证明：「先 POI 后职业」**没有运行期可观测差异** —— RegisterEvent 派发顺序由 `GameData.getRegistrationOrder()` 固定，`BuiltInRegistries` 里 `VILLAGER_PROFESSION:169` 声明**早于** `POINT_OF_INTEREST_TYPE:172`，职业表的事件其实先派发；构造器行序只影响可读性 | **采纳，并纠正我在 W04 任务书里的要求**（我要求它写「运行期理由」，实际上没有运行期理由可写）。F-283 对这条**只能静态验**（grep 行序），不许写运行期判据。配套纪律进 F-278/F-279：**职业 supplier 里不得在 RegisterEvent 阶段急切解析 POI holder** |
| D-35 | F-277 观察：F-274 改了 `ModLangProvider:429`（行内注释 16→24），而 §0.3 窗口纪律第 2 条说「`ModLangProvider` 类注释一律不动到 F-281」 | **判定合规**。第 2 条约束的是**类注释**（`:13-19` 的键构成），F-274 改的是某条 config 条目的行内注释，两者不同。F-281 仍独占 `:13-19` 的关窗 |
| D-36 | F-277 实测 POI 的 register 行在 **`:65`**（我 W04 任务书写「现 `:64`」，差 1） | 我的记账错。改后 `:66` POI / `:67` 职业；`total == 98` 由 `:178` → **`:180`**、`items == 34` 由 `:179` → **`:181`**。F-283 / F-285 引用这些行号时用新值 |
### W05

| # | 事项 | 裁定 |
|---|---|---|
| D-37 | 49 号 F-281 验收 3 写「`299` 命中 **0**、`338` 命中 0、`340` 命中 2」，与「不许碰 `:24` / `:37` 的沿革段」**不可同时成立** | **改写为「`299` 只许出现在沿革段（`:24` / `:37`）」**，实测 `299` = **2**（两处都是历史读数，改了就是篡改历史）、`338` = **0**、`340` = **2**。与 D-18 同一种处置（断言按字面不可满足 ⇒ 收敛成白名单）。进回灌清单 |
| D-38 | `ModLangProvider` 原 `:43`（现 `:48`）「**166 个** server config 键」与 `:14` 的「**176** server」自相矛盾，是 HEAD 就有的陈旧读数 | **只登记、不修**（同 D-17 类：既存漂移，46~51 号没给它派任务）。进「回灌候选」清单，不扩本版范围 |
| D-39 | F-281 的关窗读数：`zh 340 / en 340 / zh-only 0 / en-only 0 / farmstorm.configuration.* 179 / tooltip.farmstorm.* 20`；构成 16 组相加 = **340**；两份 json 逐键比对 `added=2 / removed=0 / value_changed=0` | **通过**。既有 338 条文案零改动、零删除。**语言侧关窗完成，此后本版不许再增删语言键** |
| D-40 | 职业键用 `plain(entries, "entity.minecraft.villager.farmstorm.botanist", …)` 写全路径，**没有**用 `entity(...)` 助手 | **正确**。`entity(...)` 的前缀是 `entity.farmstorm.`，而 `Villager#getTypeName` 拼的是 `entity.minecraft.villager.` + 命名空间 `farmstorm.` + 职业 id，两者不同 || D-41 | F-278 报「`FarmstormConfig` 定义点 177（`.define(` 6 + `.defineInRange(` 168 + `.defineInList(` 3）vs 文档 176」 | **不是矛盾，是口径**：实测**全量命中（含注释）= 177**，**行首 live = 176**，差的那 1 处是 `:940` 的 `// .defineInRange("farmlandBonus", 16, 0, 1024);` —— F-192 / D-139 停用耕地整组时注释掉的。与 v0.2.1 的「live 175 + 1 注释行」同一条规则。**F-285 签字若数 config，必须用「行首 live」口径并写明这一处注释行**，否则会得出 177 的错误结论 |
| D-42 | F-278 指出「全部挂在新手级（1 级）」不在它的文件里：`VillagerTrades.ItemListing` 没有等级概念，等级由事件处理器 `getTrades().get(1)` 决定 ⇒ 归 F-280 | **它是对的**。我在 W05 任务书把等级写进了 F-278 的验收，与 49 号口径不一致。**等级挂点改由 F-280 承担**（F-280 已有「植物学家分支」的活，正好一并验 `get(1)` 22 条） || D-43 | F-279 发现：**`gw.cmd build` 返回 exit 0 并不证明你的文件被编译过** —— 它那次 `compileJava` 是 `UP-TO-DATE`，因为并行 lane 的构建先把两个新类一起编了 | **采纳为全版纪律**：并行波次里「构建 exit 0」只证明「那一刻整树可编译」，不证明「本任务的源码被编译」。补证手段 = **`gw.cmd compileJava --rerun`**（只重跑该任务、约 12 秒，代价远小于 `clean build`）。写进 F-283 / F-285 的取证纪律；我也在 W05 收尾用它亲自验了一次（exit 0） |
| D-44 | 49 号 §F-279 断言「`getOffer` 里随机抽取调用**恰 1 处**」：实测 `random.nextInt` **2** 处（`:57` 抽作物、`:60` 定价） | **按语义判通过** —— 「抽取」专指抽作物，**恰 1 处**；定价那处与 `TierTrade:75` 同形。**F-283 的判据必须写成「池抽取（`pool.get(random.nextInt(...))`）恰 1 处」**，禁止用机械的「`random.nextInt` 全串 == 1」——那样会把定价也算进去，逼着实现改成 `Mth.nextInt` 破坏与 §2.4.2 的同形 |
| D-45 | F-278 与 F-279 的价格取法是否统一成 `Mth.nextInt` | **不统一，保持 `random.nextInt(MIN + nextInt(MAX-MIN+1))`**，与 48 号 §2.4.2 原文及 `TierTrade:75` 同形。可读性优先于「少一处随机调用」 |
| D-46 | `TierTrade.EPIC` 与 `BotanistEpicTrade.EPIC` 同名不同类，F-280 接线时同一作用域可能同时出现 | **F-280 必须写全限定类名**（`TierTrade.EPIC` / `BotanistEpicTrade.EPIC`），避免读者误认为同一个常量 |
| D-47 | F-278 的 Q1「`FarmstormConfig` 定义点 177 vs 文档 176」 | 见 D-41：**177 = 176 live + 1 注释行（`:940` 的 `farmlandBonus`）**，不是矛盾。F-285 数 config 用「行首 live」口径 |
### W06

| # | 事项 | 裁定 |
|---|---|---|
| D-48 | F-280 额外改了 `onVillagerTrades` 的 javadoc（原「只处理农民」在加分支后变成假话） | **通过**。这不是「顺手修既存瑕疵」，是本次改动**新造**出来的不实注释，必须同改 |
| D-49 | `VillagerTradeEvents` 实测 53 行，49 号写「约 45」 | 估算值，以实测为准；登记进回灌清单 |
| D-50 | 类首行 `:9`「农民交易注册（F-223 / D-157）」未改（现在本类也挂植物学家） | **保留**。它仍为真、只是不完整（历史来历），与「只处理农民」（已变假）不同性质；登记，留给收尾或下一版 |

### 收尾阶段的 README 待改清单（F-282 §7 逐条登记，只登记不改）

| 位置 | 现文 | 应为 |
|---|---|---|
| `:5` | 当前发布 **v0.2.1**（v0.2.2 只有文档，代码未实现） | 当前发布 **v0.2.2** |
| `:19` | 「史诗档的唯一入口是农民交易」 | 植物学家也卖 **1** 项史诗 |
| `:130` | `### 已知限制（v0.2.1）` | 收尾时统一到 v0.2.2 |
| `:143` | 已知限制「界面没有覆写 `renderTooltip`」 | 对 0.2.1 成立，v0.2.2 已修（F-270） |
| `:182-183` | 代码地图 `items 33 … = 97` | **34 / 98** |
| `:209-217` | verify 工具样例 `97 / 97`、`共 338 键` | **98 / 98、340 键** |
| `:311` | 数字快照 `97`、`338 × 2` | **98 / 176·60 / 340 × 2 / Java 154 / 3 类口径外** |
| `:328` | 版本历史 v0.2.2 行「代码未实现」 | 日期 + 已发布 |
| English `:398`/`:404` | `97 → 98`、`337 → 340`、`共 338 键` | **337 与实测 338 冲突**，一并改 |
