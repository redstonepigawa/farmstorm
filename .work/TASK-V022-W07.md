# 任务书 · farmstorm v0.2.2 · W07（F-283 → F-284 → F-285，**同一子代理顺序做三件**）

> 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。**本波只有你一个子代理**。
> 前置：W01~W06 已落地并提交（HEAD `c382af5`）。**F-267~F-282 共 16 个任务全部落地。**

## 0. 三个任务（详见 49 号对应小节）

| 顺序 | 任务 | 复杂度 | 交付物 |
|---|---|---|---|
| 1 | **F-283** 48 号 §8 的 **30 条**验收全量执行 | L / 3.0 | `.work/v022-verify/acceptance.md`（表头 + **30** 行） |
| 2 | **F-284** 47 号 §8 的 **27 条**边界复跑 | L / 3.0 | `.work/v022-verify/boundaries.md`（表头 + **27** 行，分组） |
| 3 | **F-285** 红线、注册面与「19 项必改」签字 | M / 1.5 | `.work/v022-verify/signoff.md`（读数表 + **16 行任务核对表** + **3 类口径外登记**） |

> 🔴 **F-283 / F-284 / F-285 一律不改码**：验收不通过就**如实记失败**，不许就地改判据。`git status` 只允许新增 `.work/v022-verify/` 三份文件。

## 1. 必读

1. `docs/zh/48-TECH-0.2.2.md` —— **§8 的 30 条验收**（F-283 的逐条判据）、§7 待验证 API、§10、§1（注册面 98 + 口径外累计 3 类）
2. `docs/zh/47-DESIGN-0.2.2.md` —— **§8 的 27 条边界**（F-284 的逐条判据）
3. `docs/zh/49-TASKS-0.2.2.md` —— **F-283 / F-284 / F-285 小节** + §0.5 死命令 + §5（A-82~A-91）
4. `docs/zh/46-DECISIONS-0.2.2.md` —— 18 条裁定（尤其 #7 / #11 / #13 / #17 / #18）
5. `.work/v022-BRIEF.md` —— §0 基线、§3.1（注册面 98 + **口径外累计 3 类**）、§3.4（**19 项必改**）、RT-62~RT-70
6. `.work/V022-DISPATCH.md` —— **我这一路的裁定台账**（下面 §3 摘了必须遵守的那几条）

## 2. 已实测基线（HEAD `c382af5`，可直接引用，别再重测）

| 项 | 值 |
|---|---|
| 注册面 | **98** = items **34** / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 |
| **口径外累计 3 类** | ① `farmstorm:ai_suspend`（附件类型，0.2.1）② `PoiType`（本版第 2 类）③ `VillagerProfession`（本版第 3 类）—— **都不计入 98**，`verify_project.py` 既不核存在也不核计数 |
| config | **176 键 / 60 组**（`b.push(` = `b.pop()` = 60；**行首 live `.define*` = 176**） |
| config 默认值 | `maxPerPlayer = 4`（`FarmstormConfig:943`）、`cultivatorCapacity = 24`（`:956`） |
| 语言键 | **340 × 2**（`farmstorm.configuration.*` = 179、`tooltip.farmstorm.*` = 20） |
| 网络包 / 协议串 / Mixin | **2** / `"2"` / **0** |
| Java 文件数 | **154** |
| `FarmstormTags` | **7** 个 TagKey（含 `PROCESSING_BLOCK`） |
| POI 标签产物 | `src/generated/resources/data/minecraft/tags/**point_of_interest_type**/acquirable_job_site.json`，`values = [farmstorm:botanist]` |

## 3. 必须遵守的取证纪律（我这一路踩出来的，逐条有台账编号）

1. **D-43：`gw.cmd build` 返回 exit 0 不证明任何东西** —— 全 UP-TO-DATE 时它什么都没编。**凡「源码真的编译过」的断言，必须给 `gw.cmd compileJava --rerun` 的 exit code。**
2. **D-27：jar / 构建产物类读数一律放构建锁内**，或改读 `build/classes/java/main/**`。锁外读主 jar 会撞上并发 rebuild 的 `zip END header not found`。
3. **D-20：grep 判据必须用词边界**。`instanceof CultivatorBlock` 的裸子串会命中 `CultivatorBlockEntity`，机械 grep 必然假红。
4. **D-18 / A-85：断言按字面不可满足时，收敛成白名单**。`instanceof CultivatorBlock` 在 `src/main/java` 下改后仍有 **3** 处既有命中（`CultivatorBlockEntity:257`、`ModItemModelProvider:65`、`ModBlockStateProvider:79`），不属 D-194 判据面 ⇒ 判据写成「落点文件 `OutpostCoreBlockEntity` 内 = 0」。
5. **D-25：POI 标签目录是 `point_of_interest_type`，不是 `poi_type`**。写成短名会假红。
6. **D-41 / D-47：数 config 用「行首 live」口径 = 176**。全量含注释是 **177**（多的是 `FarmstormConfig:940` 被注释掉的 `farmlandBonus`），**不要把 177 当结论**。
7. **D-44：「`getOffer` 抽签恰 1 处」按语义写** —— `random.nextInt` 全串有 2 处（抽作物 + 定价），**不要用机械 grep**。
8. **D-34：「先 POI 后职业」的注册行序没有运行期可观测差异**（NeoForge 的 RegisterEvent 派发顺序由 `BuiltInRegistries` 字段声明顺序决定，职业表其实先派发）⇒ 这条**只能静态验**，不许写运行期判据。
9. **D-37：「`299` 命中 0」不可满足**（沿革段 `:24` / `:37` 必须保留历史读数）⇒ 判据写「`299` 只许出现在沿革段」。
10. **F-283 / F-284 不改码**；失败如实记录；渲染 / 听感 / 需第二客户端 → 标 **【未验证】** + 代码层证据，**不许编读数**。

## 4. 🔴 起服前的硬前置（RT-63 / D-33，**不做这两条 `#9` / `#10` 必判错**）

盘上 `run/config/farmstorm-server.toml` **仍是旧值**：`maxPerPlayer = 2`（`:753`）、`cultivatorCapacity = 16`（`:765`）。**NeoForge 只补键/删键、不改已存在键的值。**

- **要验新默认值 4 / 24（48 号 §8 #10）**：用**全新的 run 目录** `run-w07`（它没有 toml ⇒ 会按新默认值重新生成），或删掉该 toml 让它重生成。
- **要验老存档不追溯（48 号 §8 #9）**：把旧的 `run/config/farmstorm-server.toml`（2 / 16）**拷进** `run-w07/config/` 再起服，读到的应当仍是 **2 / 16**。
- 这两条是**成对的**：`#9` 的「红」（读不到 4/24）是**期望中的红**，必须与 `#10` 一起跑。

**起服纪律**：`$env:FS_RUN_LOCK='run-w07'; & 'D:\project_farm\.tools\gw.cmd' runServer -PfsRunDir=run-w07`；**永远不要在 `run/` 里起服**；**不许改 `run/config/farmstorm-server.toml`**（那是 0.2.1 的关窗读数，被我核过）。rcon 端口/密码避开 25575（例如 25647 / w07rcon）。

## 5. 构建 / datagen / 起服统一纪律

```powershell
$lock = 'D:\project_farm\.tools\build.lock'
for($i=0; $i -lt 180; $i++){
  if(Test-Path $lock){ $age=(Get-Date)-(Get-Item $lock).LastWriteTime
    if($age.TotalMinutes -gt 15){ Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue } }
  if(New-Item -ItemType Directory -Path $lock -ErrorAction SilentlyContinue){ break }
  Start-Sleep -Seconds 5 }
try { & 'D:\project_farm\.tools\gw.cmd' build } finally { Remove-Item $lock -Recurse -Force -ErrorAction SilentlyContinue }
```

- 构建 / datagen / jar 读数**一律在锁内**；**禁止直接调 gradle**。
- python 用 `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（PATH 里的 `python` 是 WindowsApps 占位符）。
- 读 Java 行号一律 `[IO.File]::ReadAllLines($p,[Text.Encoding]::UTF8)`。
- `gw.cmd runServer` 在 `/stop` 之后可能报 `exit code 1` 而日志是 `BUILD SUCCESSFUL`（0.2.1 起的老现象）⇒ 判成败看日志，但退出码与日志尾行都要写进证据。

## 6. 三份记录的要求

**`acceptance.md`**：表头 + `#1`~`#30` 共 **30** 行。每行五列：`编号 | 执行动作 | 读数（原文） | 通过/失败/【未验证】 | 证据`。**每条都要有读数原文**，不是「已验」两个字。

**`boundaries.md`**：表头 + `B-01`~`B-27` 共 **27** 行，按 47 号 §8 的分组标注，每组一条汇总。

**`signoff.md`**：三块 —— ① **读数表**（注册面 98 **逐类点名** / config 176·60 / lang 340×2 / 网络包 2 / 协议串 2 / Mixin 0 / Java 154 / `FarmstormTags` 7）；② **16 行任务核对表**（`F-267`~`F-282` 逐条：任务号 / 一句话交付 / 证据位置 / 状态）；③ **3 类口径外登记**（`ai_suspend` / `PoiType` / `VillagerProfession`，逐个写明「不计入 98、`verify_project.py` 既不核存在也不核计数」）。

> **19 项必改**（BRIEF §3.4）的逐项核对也放进 `signoff.md` —— 一项一行「改了没有 / 落在哪个文件:行号」。

## 7. 增量落盘（**关键**）

每完成一批（例如 `#1~#10`）就**写进文件**，不要在内存里攒到最后。上下文一断就全丢。顺序建议：

1. **纯命令行可做的**（注册面 / config / lang / grep 类）→ 先写 `acceptance.md` 的这一批。
2. 起 `run-w07`，做需要服务端的条目 → 再写盘。
3. 27 条边界（能复用验收场景就复用）→ 写 `boundaries.md`。
4. 最后写 `signoff.md`。

## 8. 回报（`send_message` 给 lead）

- 三份记录各自的**行数** + 通过 / 失败 / 【未验证】 条数；
- **失败条目的完整清单**（编号 + 读数 + 原因），**一条都不许藏**；
- 读数表（signoff 那块）；
- 偏离登记 + 未验证清单 + 未关闭的问题；
- `git status` 原文 + 明确写出「`src/` 与 `docs/` 的 diff 为空、本 Phase 无任何 git 写操作」。
