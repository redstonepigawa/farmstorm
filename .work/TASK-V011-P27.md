# 任务书 · farmstorm v0.1.1 · Phase 27（F-178 ~ F-184）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。
> 本文件是完整任务书；先读完「必读」再动手。

---

## 0. 目标（7 个任务，三条互不相干的链）

| 任务 | 标题 | 复杂度 | 依赖 | 一句话交付物 |
|---|---|---|---|---|
| **F-178** | 生成器删掉叠「+」机制 | M / 1.5 | — | `tools/gen_textures.py` 的 `NO_PLUS_ICONS` / `add_plus` / `build_marker()` / `compose_icon()`、右下角贴附、合成分支、调用点与 dry-run 文案全删；`ICONS` 的 26 条与 `TEXTURE_SIZE = 16` 不动 |
| **F-179** | 狂野苹果直通、尺寸断言放开与图入库 | L / 3.0 | F-178 | `WILD_APPLE_SOURCE` 改指 64×64、`draw_wild_apple()` 直通、`expected_size()` 与 `expected_size` 的断言**一起**放开（A-32）；入库 PNG 是源图逐字节副本（64×64 / 6,946 B） |
| **F-180** | 压缩干草块音效 `GRASS` → `STONE` | S / 0.5 | — | `registry/FarmstormBlocks.java` 一个 `SoundType` 换掉；五类声音一起变 |
| **F-181** | 据点核心亮度 15 | S / 0.5 | — | 注册链尾部追加 `.lightLevel(state -> 15)`；其余属性一字不动，不做成 config |
| **F-182** | `pumpkin_turret` 三键 + 1 条语言键 + 2 处注释 | M / 1.5 | — | `fireIntervalTicks` 60 → **40**、`projectileDamage` 1 → **3**、新增 `bulletSpeed`（double，**0.45**，`0.0 ~ 4.0`）；两处注释同改；语言键 +1 |
| **F-183** | 删 `TurretBullet.SPEED` 并改两处读点 | M / 1.5 | F-182 | `:28` 常量删除；`steerTowards` 与 `PumpkinTurret` 的出膛初速**同一次提交**改读 `pumpkinTurretBulletSpeed`（A-30）；收敛逻辑本任务不动 |
| **F-184** | 炮弹近距收敛与共用结算体 | L / 3.0 | F-183 | `steerTowards` 三分支（`dist > step` 原样 / 一步走完剩余 / `≤ MIN_STEP` 直判）；`applyHit` 从 `:98-119` 原样搬家 + `hitResolved` 闸；**不引入软追踪** |

**执行顺序**：`F-178 → F-179`（同一文件）、`F-182 → F-183 → F-184`（同一条编译链）、`F-180` / `F-181` 随时可插。前一步不回滚，后一步不许开工。

## 1. 边界

**允许改**：`tools/gen_textures.py`、`src/main/resources/assets/farmstorm/textures/item/wild_apple.png`、`src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java`、`.../config/FarmstormConfig.java`、`.../datagen/ModLangProvider.java`、`.../entity/TurretBullet.java`、`.../behavior/impl/PumpkinTurret.java`、以及 datagen 重新生成的 `src/generated/resources/assets/farmstorm/lang/*.json`。

**绝对不可动**：
- `docs/` 任何文件（28~33 与 00~27 全冻结）
- `models/item/wild_apple.json`、物品 id、`ICONS` 表条目与顺序、`TEXTURE_SIZE = 16`
- 其余 **24 张** PNG 的字节（本版只重生成 1 张，`25 − 1 = 24`）
- `FarmstormEntityTypes.java`（`clientTrackingRange(4)` / `updateInterval(10)` 一个字节都不改）
- `run/config/farmstorm-server.toml`（**本 Phase 不许删、不许手改**——F-177 的 A-40 中间态靠它；要测「删旧 toml」就复制一份到独立的 `run-p27` 目录去测）
- `build.gradle`、`settings.gradle`、`.tools/`、`.work/v011-BRIEF.md` / `v011-verdicts.md` / `v011-reqs.md` / `v011-delivery.md`
- 本 Phase **不许动**注册面（仍 93）与其它 config 组的默认值

## 2. 必读

1. `docs/zh/32-PROMPT-MAIN-0.1.1.md` —— 全文
2. `docs/zh/33-PROMPT-TASKS-0.1.1.md` —— §0.2、**§2（Phase 27 全节）**、§8
3. `docs/zh/31-TASKS-0.1.1.md` —— F-178 ~ F-184 详情（约 180~299 行）
4. `docs/zh/30-TECH-0.1.1.md` —— **§2.6（man瓜 近距收敛，745~817 行，含逐段伪代码）**、§7 的 #2 ~ #4 / #23 ~ #28 / #33 ~ #35、§9 第 7 条
5. `docs/zh/28-DECISIONS-0.1.1.md` —— D-125 ~ D-130、D-141、D-147，以及裁决 A-30 / A-31 / A-32
6. `.work/v011-BRIEF.md` §1.1 / §1.6 / §1.7 / §3.4 / §3.5 / §4（RT-37 / RT-38）
7. `.work/v011-verdicts.md`（A-30 ~ A-32、A-52）

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`，基线 exit 0。**唯一入口，禁止直接调 gradle** |
| 增量编译 | 可先 `.tools\gw.cmd compileJava` 快验，最后再 `build` |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（`python` **不在 PATH**）；先 `$env:PYTHONIOENCODING='utf-8'` |
| 源图 | `C:\Users\DASHpig\Downloads\wild_apple_64x64.png` —— **6,946 B / 64×64**，在位 |
| 入库图现状 | `src\main\resources\assets\farmstorm\textures\item\wild_apple.png` = **571 B / 16×16**（带 33 px 的 `#3FA34D` 绿十字） |
| 生成器锚点（实测，行号可能与文档略有偏移） | `:59 TEXTURE_SIZE=16`、`:154 WILD_APPLE_SOURCE`（现指桌面 `2797B56F...png`）、`:155` 环境变量名、`:180 add_plus`、`:275 NO_PLUS_ICONS`、`:407 add_plus=`、`:546 build_marker`、`:612 compose_icon`、`:843 wild_apple_source`、`:849 wild_apple_grid`、`:890/895 draw_wild_apple`、`:974-975`、`:982 expected_size`、`:1127 marker`、`:1141`、`:1152` |
| lang 现状 | 两份各 **315** 键（`src/generated/resources/assets/farmstorm/lang/{zh_cn,en_us}.json`），键集相等 |
| lang 生成方式 | `datagen/ModLangProvider.java` 里的 `.translation(key, zh, en)`，跑 `.tools\gw.cmd runData -PfsRunDir=run-p27` 重生成 |
| 起服验配置（推荐做法） | 复制 `run/eula.txt` 与 `run/server.properties` 到独立目录 `run-p27`（`server.properties` 已带 `enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），然后 `FS_RUN_LOCK=run-p27 cmd /c ""<repo>\.tools\gw.cmd" runServer -PfsRunDir=run-p27" > .work\p27-server.log 2>&1`，看到 `Done (...)` 后 RCON 发 `stop`。**禁止在 `run/` 目录里起服**（不要动主 `run/config`） |
| 注册面 | 93（本 Phase 不许变） |

## 4. 调度者已定的口径

1. **文档与源码冲突 → 以源码为准**，自行适配，登记进「文档-源码偏差表」；不许改 `docs/`。
2. `.work/v011-verdicts.md` 里已裁决未回灌条目按裁决口径执行并注明 A-xx。本 Phase 相关：
   - **A-30**：`TurretBullet.SPEED` 有**两个**读者（`TurretBullet` 的每刻步长 + `behavior/impl/PumpkinTurret` 的出膛初速），必须同一次提交改完。
   - **A-31**：`SoundType` 换掉后是 **五类**声音（破坏 / 放置 / 踩踏 / 挖掘 / 落地）全变石头类，验收条数比原估多 2 条。
   - **A-32**：尺寸断言与 `expected_size()` **一起**放开；断言本身**保留不删**。
   - **A-52**：数 toml 段用 `^\s*\[`。
3. **禁止任何 git 写操作**（commit/add/checkout/reset/stash/restore/clean）。只许 `git status` / `git diff` 取证。提交由调度者做。
4. 客户端 GUI 类验收（音效听感、F3 光照读数、贴脸 5 发血量、刷怪平台）本机无法操作：能静态/无头取得的照做，其余**标「未验证」+ 原因**。
5. 遇到做不下去的：停下，写清阻塞点与已做部分。

## 5. 红线与三个数

- 红线只剩「无 Mixin」。网络包限制已整体废除（本 Phase 不涉及），不许写成通过条件。
- 本 Phase 结束时预期：注册对象 **93**（不变）、server config **166**（不变，`pumpkin_turret` 组 4→5 键、总数不变）、语言键 **316 × 2**（`315 + 1`）。
- **A-40 中间态在本 Phase 收缩一次**：F-182 提交后，`verify_project.py` 的差异从 **2 条变 1 条**（只剩「多 `farmland.limitPerPlayer`」），退出码**仍非 0**——这是预期。**不许**为了让它归零去改 `docs/`、删 `run/config/farmstorm-server.toml` 或改 `FarmstormConfig` 的其它组。

## 6. 实现要点（逐条，与 31 号对齐）

### F-178
1. 删 `NO_PLUS_ICONS` 与 `Spec.add_plus` 字段：`Spec` 不再有「要不要叠」这一维，`compose()` 不再接受 marker 参数。
2. 删 `build_marker()`、右下角贴附（`left = 9 / top = 9`）与 `compose_icon()`；`compose()` 只留「源图 → 尺寸归一」一条返回路径。
3. 删 `:1127` 的 `marker = build_marker()` 调用与 `--dry-run` 里「+ 绿色「+」」那段文案（否则 dry-run 仍打印它）。
4. `ICONS` 的 26 条、`TEXTURE_SIZE = 16`、`make_placeholder()` **一律不动**。
5. **本任务不重生成其余 24 张**：只允许 `wild_apple.png` 有字节变化。

### F-179
1. `WILD_APPLE_SOURCE` 默认值改指 `Path(r"C:\Users\DASHpig\Downloads\wild_apple_64x64.png")`；环境变量覆盖入口保留。
2. `draw_wild_apple()` **直接返回源图副本**（RGBA 原样），不再进 `wild_apple_grid()` 的 16 网格众数取色；`wild_apple_grid()` 若已无调用者就删除（不留死代码）。
3. `expected_size()`：对 `wild_apple` 返回源图实际尺寸 `(64, 64)`，对 GUI 仍返回 `GUI_SIZE`，其余仍 `(TEXTURE_SIZE, TEXTURE_SIZE)`；**尺寸断言保留、只放开期望尺寸**（A-32）。
4. 入库 PNG 是源图**逐字节副本**：不改尺寸、不量化、不动 alpha。
5. `models/item/wild_apple.json`、物品 id、`ICONS` 表条目与顺序、语言键、注册面一律不动。

### F-180
`registry/FarmstormBlocks.java` 里 `compressed_hay_block` 的 `sound(SoundType.GRASS)` → `sound(SoundType.STONE)`；同链 `mapColor(COLOR_BROWN)` / `strength(30.0F, 6.0F)` / `requiresCorrectToolForDrops()` 一字不动。

### F-181
`outpost_core` 注册链尾部追加 `.lightLevel(state -> 15)`（**注意**：`Block` 的 `lightLevel` 只接受 `ToIntFunction<BlockState>`，用 lambda）；`MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `sound(STONE)` / `pushReaction(BLOCK)` 不动。**不做成 config**。

### F-182
1. `fireIntervalTicks` 的 `defineInRange` 默认值 60 → **40**；同处注释「60 刻 = 3 秒」→「40 刻 = 2 秒」。
2. `projectileDamage` 默认 1 → **3**，值域 0–100 不动。
3. 新增 `bulletSpeed`：放在 `pumpkin_turret` 组内、`fireIntervalTicks` 之后；double、默认 **0.45**、值域 `0.0 ~ 4.0`，带 `.translation(...)`。
4. `farmlandBonus` 的注释补一句「本键同时是每核容量与队伍额度基数（额度 = 本键 × 己方核数）」（RT-33）。
5. 语言键只加 **1** 条 `farmstorm.configuration.pumpkin_turret.bulletSpeed`；跑 `runData` 重生成两份 lang，键集仍相等。

### F-183
删 `:28` 常量；`steerTowards` 与 `behavior/impl/PumpkinTurret` 的出膛初速**同一次提交**都改读 `FarmstormConfig.pumpkinTurretBulletSpeed.get()`（每发读一次，**不缓存成字段**）。`FarmstormEntityTypes` 一字不改。

### F-184
严格按 `30-TECH §2.6.2` 的伪代码落地：
- `private static final double MIN_STEP = 0.05;`、`private boolean hitResolved;`
- `steerTowards` 三分支：`dist <= MIN_STEP` → `resolveHit(aim)` 并 return；`dist <= step` → `setDeltaMovement(delta)`；否则 `setDeltaMovement(delta.normalize().scale(step))`
- `resolveHit(Entity)`：`hitResolved` 闸 → `applyHit` → `discard()`
- `applyHit(Entity)`：从 `:98-119` **原样搬家**（伤害类型 `farmstorm:void_bolt`、进度 `turret_hit`、击退算式一个字符不改），`discard()` 移出
- `onHitEntity` 改走 `resolveHit(result.getEntity())`
- **不做**：转向角上限、最大追击时长、制导参数、换实体父类

## 7. 验收标准（逐条给读数，缺一条就是没做完）

**F-178**
1. `grep -c "add_plus\|NO_PLUS_ICONS\|build_marker\|compose_icon" tools/gen_textures.py` = **0**。
2. `python -X utf8 tools/gen_textures.py --dry-run` 退出码 0，计划 **26** 项，输出里不再出现「绿色「+」」。
3. 其余 **24** 张 PNG 的 sha256 与动手前逐张相同（**动手前先存一份清单**）。
4. `git status` 里本任务只改了 `tools/gen_textures.py` 与 `wild_apple.png`。

**F-179**
5. `textures/item/wild_apple.png` 是 **64×64**、**6,946 B**、sha256 与源图相同。
6. 删掉入库那张再跑 `python -X utf8 tools/gen_textures.py`：产物 64×64、与源图逐字节相同、**不报尺寸断言失败**，退出码 0。
7. 连跑两次产物 sha256 相同；其余 24 张 sha256 仍不变。
8. `grep -c "wild_apple_grid" tools/gen_textures.py` = **0**。

**F-180**
9. `git diff registry/FarmstormBlocks.java` 里 `compressed_hay_block` 段只有 `sound(...)` 一行变化；`grep -c "SoundType.GRASS" src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java` = **0**。
10. 五类声音（破坏/放置/踩踏/挖掘/落地）游戏内对照 —— **本机无法操作客户端，标「未验证」**，并在证据里写清「按 `SoundType.STONE` 的五类取值，代码层已确认五类同源」。

**F-181**
11. `git diff registry/FarmstormBlocks.java` 里 `outpost_core` 段只有 `.lightLevel(...)` 一行新增；`grep -c "lightLevel" .../FarmstormBlocks.java` ≥ 1。
12. 游戏内 `Light = 15` 与刷怪平台 —— **标「未验证」**。

**F-182**
13. 在独立 `run-p27` 目录（没有旧 toml 的状态）起服：`run-p27/config/farmstorm-server.toml` 的 `[pumpkin_turret]` 段恰 **5** 键 = `targetRadius 15 / projectileDamage 3 / knockbackLevel 1 / fireIntervalTicks 40 / bulletSpeed 0.45`；给读数。
14. 把 **0.1.0 的旧 toml 副本**放进一个干净目录再起服一次：`fireIntervalTicks` 仍 **60**、`projectileDamage` 仍 **1**，而 `bulletSpeed` 被自动补成 **0.45**（RT-38）。
15. 值域：`bulletSpeed` 手改 `5.0` 重启后加载值回到值域内；toml 注释行含 `0.0 ~ 4.0`。
16. `grep -c "60 刻 = 3 秒"` = **0**、`grep -c "40 刻 = 2 秒"` = **1**；`farmlandBonus` 注释含「额度」。
17. 两份 lang 各 **316** 键且键集相等；各含 `farmstorm.configuration.pumpkin_turret.bulletSpeed` 1 条。

**F-183**
18. `grep -rn "TurretBullet.SPEED" src/main/java` **0 命中**；`grep -c "SPEED" entity/TurretBullet.java` = **0**。
19. `grep -c "pumpkinTurretBulletSpeed"` 在 `entity/TurretBullet.java` 与 `behavior/impl/PumpkinTurret.java` **各 ≥ 1**。
20. `.tools\gw.cmd build` 退出码 0；`FarmstormEntityTypes.java` 的 `git diff` 为空。

**F-184**
21. `grep -c "applyHit"` ≥ **3**（定义 + 两处调用）；`grep -c "MIN_STEP"` ≥ **2**；`grep -c "hitResolved"` ≥ **3**。
22. 贴脸 5 发 5 中 / 3 格外弹道不变 / 单发只结算一次 —— **游戏内项标「未验证」**，但要给出代码层证据（三分支逐行、`resolveHit` 闸、三处读点）。

**收口**
23. `.tools\gw.cmd build` 退出码 **0**。
24. `verify_project.py`（用 `run/config/farmstorm-server.toml` 那份主 toml，**不许删**）的 `[FAIL]`/`[STALE]` 行合计 **恰好 1 条**、键名只有 `farmland.limitPerPlayer`，退出码非 0（预期）。
25. `git status --porcelain` 里本版改动只落在 §1 允许的文件 + 证据文件 + `build/` 产物 + 你新建的 `run-p27*` 目录（**`run/` 不能有一行改动**）。

## 8. 必须回报的证据

1. `git diff --stat` 原文。
2. 七条各自的文件 + 行号。
3. `.tools\gw.cmd build` 退出码；`gen_textures.py`（含 `--dry-run`）两次的退出码与「26 项」。
4. 24 张 PNG 的 sha256 清单（动手前 / 收尾后两份对照）。
5. `wild_apple.png` 的尺寸 / 字节 / sha256，以及源图的同三项。
6. 两张 toml 的 `[pumpkin_turret]` 五键读数（新 40/3/0.45/15/1；旧 toml 60/1/—/15/1）。
7. `verify_project.py` 的差异行数与键名；两份 lang 的键数。
8. 六个 `grep -c` 的读数（`add_plus` 组、`60 刻 = 3 秒`、`TurretBullet.SPEED`、`pumpkinTurretBulletSpeed`、`applyHit`、`MIN_STEP`）。
9. **文档-源码偏差表**（文档行号 vs 实测行号、措辞差异）。
10. **未验证清单**（每条一行：条目 + 为什么本机做不了）。
11. **未关闭的问题**。

## 9. 证据落盘

完整输出写进 `.work/TASK-V011-P27-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 10. 完成定义

§7 的 25 条**全部有读数或有「未验证」+ 原因**，证据文件已落盘，回报含 11 项。
