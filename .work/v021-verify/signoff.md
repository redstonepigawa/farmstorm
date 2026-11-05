# F-266 · 红线、注册面与上游一致性签字

> 版本 v0.2.1 / HEAD `2e870d7`（`git describe` = `v0.2.0-13-g2e870d7`）/ 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。
> 本文件**只签字、不改码**：`git diff --stat`（全部 tracked 文件）**为空**；`docs/zh/40`~`45` 的 `git diff --stat` **为空**；本 Phase **无任何 git 写操作**（只跑过 `git status` / `git diff` / `git log` / `git show`）。

## 1. 八项读数（逐项给数，不写「通过」）

| # | 项 | 口径 | 读数 | 证据 |
|---|---|---|---|---|
| 1 | `@Mixin` | **`src/` 口径** | **0** | `src/**/*.java` 全树 grep `@Mixin` → 0 命中 |
| 2 | `*.mixins.json` | **`src/` 口径** | **0**（全仓 **1**，在 `.scratch/p25a/src/neoforge.mixins.json`，实验残留、不在 `src/`、不参与构建 ⇒ 口径外） | `Get-ChildItem src -Recurse -Filter *.mixins.json` → 0；全仓 → 1（点名那一个文件，42 号 §9 第 10 条） |
| 3 | `.playToClient` | `FarmstormNetwork` | **2**（`:38` `CoreListPayload`、`:43` `CultivatorLinkPayload`）；`PROTOCOL_VERSION = "2"`（`:25`） | `FarmstormNetwork.java:25,38,43` |
| 4 | 注册面（12 类相加） | `Farmstorm#logRegistrySelfCheck` | **97**（items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → **OK**） | 启动日志原文 `自检 注册对象 97 个：… → OK`（`run-p45/logs/latest.log`，五次起服都一致）；`verify_project.py` → `已核 registry 对象: 97 / 97` |
| 5 | `data_components` | 12 类的第 11 类 | **1**（`farmstorm:temporary_enchantments`） | 同上启动行里的 `data_components 1` |
| 6 | 附件类型（第 13 类） | **口径外** | **1**（`farmstorm:ai_suspend`，`registry/FarmstormAttachments.java:36-39`）——**不计入 97** | 源码原文 + 见本文件 §3 |
| 7 | config 键 / 组 | **引用 F-262 的关窗读数**（本节不重算） | **176 键 / 60 段**（`run/config/farmstorm-server.toml`：805 行 / 60 段 / 176 键；`[trap]` 4 键、`damageMultiplier = 3.0` 在 `:747`） | F-262 三处读数 + 本次复跑 `verify_project.py`：`文档口径键数: 176` / `生成 toml 键数: 176` / `[OK] 176 个配置键与文档逐字一致` |
| 8 | 语言键集 × 2 | **引用 F-263 的关窗读数**（本节不重算） | `zh = 338` / `en = 338` / `zh-only 0` / `en-only 0`；`farmstorm.configuration.* = 179`；`tooltip.farmstorm.* = 20` | F-263 的六个数 + 本次复跑 `json.load` 取键集（六个读数逐字相同） |

## 2. 「不改」两条的逐字证据（行号 + 片段）

**2.1 12 类断言**（42 号 §7 #29 写 `167-172`、43 号 F-266 写 `169-172`；**HEAD 实测 `Farmstorm.java:176-179`**，偏差已登记）

```
:176         boolean ok = total == 97
:177                 && items == 33 && blocks == 27 && blockEntities == 8 && entityTypes == 13
:178                 && mobEffects == 4 && soundEvents == 3 && recipeTypes == 1 && recipeSerializers == 1
:179                 && creativeTabs == 1 && damageTypes == 3 && dataComponents == 1 && menuTypes == 2;
```

逐字比对：`git show v0.2.0:src/main/java/com/dashpig/farmstorm/Farmstorm.java` 的 `:169-172` 与 HEAD 的 `:176-179` **四行完全相同**（`same_text=True`）；`git diff --unified=0 v0.2.0` 对本文件只有两处 hunk（`:61-63` 三条注释 + `FarmstormAttachments.REGISTER.register(modBus)`；`:83-84` 两条注释 + `MobAiSuspension.register(...)`），**没有任何一行落在断言段里**。构造器 `.register(modBus)` = **12** 行（`:48,49,50,51,52,54,55,56,57,59,60,63`）。

**2.2 5 处 `getEntitiesOfClass(Monster.class, …)`**

| 文件 | `v0.2.0` 行号 | HEAD 行号 | 行文本逐字相同 |
|---|---|---|---|
| `BlastCarrotTrap` | 59 | **59** | ✅ `for (Monster monster : level.getEntitiesOfClass(Monster.class, TrapBehavior.searchBox(pos, radius))) {` |
| `FangTrapCactusTrap` | 54 | **54** | ✅ 同上 |
| `VibrationBeanTrap` | 59 | **68** | ✅ `for (Monster monster : level.getEntitiesOfClass(Monster.class,`（换行续行） |
| `ZangiefCaneTrap` | 174 | **180** | ✅ `for (Monster monster : level.getEntitiesOfClass(Monster.class, TrapBehavior.searchBox(pos, maxDistance))) {` |
| `PumpkinTurret` | 109 | **109** | ✅ `for (Monster monster : level.getEntitiesOfClass(Monster.class, searchBox(pos, radius))) {` |

⇒ 「未动」成立在**文本**上；`VibrationBeanTrap` +9 行（F-254 新增段）、`ZangiefCaneTrap` +6 行（F-257~F-260）把行号推后。`Hostility` 使用点 **2** 个（`ZangiefCaneTrap:115`、`FarmstormExplosion:198`）。

**2.3 尖牙与震动豆的 0 伤害**

`FangTrapCactusTrap.java` 在 `v0.2.0..HEAD` 的 `git diff --stat` 里**不出现**（未改）；`:87-109` 仍是原版尖牙生成（`:98 EntityType.EVOKER_FANGS.create(level)` → `:107 level.addFreshEntity(fangs)`），两文件 `hurt(` 命中 **0**。`vibration_bean` 的 0 伤害同结论（`VibrationBeanTrap` 全 134 行无 `hurt(` / `damageSources`）。

## 3. 第 13 类登记（裁定 #17 / A-73）

> 另注 **1** 个附件类型 `farmstorm:ai_suspend`，不计入 **97**，`verify_project.py` 既不核存在也不核计数。

- 注册名 `farmstorm:ai_suspend`，类型 `AttachmentType<MobAiSuspension.Suspend>`，注在 `NeoForgeRegistries.Keys.ATTACHMENT_TYPES`（口径外，不属 12 类里的任何一类）。
- **覆盖缺口**：它**不在 `EXPECTED`（`tools/verify_project.py:281-321`）的任何一组** id 里、**也不在 `EXPECTED_COUNTS`（`:325-338`）的 12 个计数相加里** ⇒ 工具**既不核它存在、也不核它计数**。本版不扩工具（D-174），缺口由「42 号 §1.2.1 + F-252 验收 5 + 本签字」三处人工登记兜住。
- **附件 ≠ 数据组件**：D-175 否决的是 `data_components`（加一个就 97→98）；附件是 NeoForge 自己的注册表 ⇒ **97 不变、`data_components` 仍是 1**。
- 不挂 `.sync(...)`、不开 `copyOnDeath()`；`sources == 0` 的 `NONE` **不落盘**（`serialize(codec, s -> s.sources() != 0)`，实测恢复后 `neoforge:attachments` 整个键不存在）。

## 4. 任务核对表（F-247 ~ F-266，20 行）

| 任务 | 一句话交付 | 证据位置 | 状态 |
|---|---|---|---|
| F-247 | 版本号 0.2.1 与两支 `.cmd` 的 **6 处**版本串（A-70） | `gradle.properties:11` + `一键构建.cmd:5,11,12` + `检查环境.cmd:5,14` | 已落 |
| F-248 | `util/AreaBlockEntities#collect`（区域枚举方块实体，0 先例） | `AreaBlockEntities.java:32-51`（只用 `getChunkNow`，两阶段拷贝） | 已落 |
| F-249 | 补绑相位门 `rebindDue` + 写入边界 `rebindNearby` | `OutpostCoreBlockEntity:514-517 / 537-572` | 已落 |
| F-250 | `serverTick` 接线 + `CultivatorBlock` 注释 + 一条补绑 INFO（A-71） | `OutpostCoreBlockEntity:584-607`；`acceptance.md #6/#7/#8` 的 `补绑：核 …` 三行 | 已落 |
| F-251 | `isMountMode` 可见性 `private → public` | `QbounceMountController`；`MobAiSuspension:99` 调用 | 已落 |
| F-252 | `Suspend` + `farmstorm:ai_suspend`（第 13 类） | `MobAiSuspension:52-63`；`FarmstormAttachments.java:36-39` | 已落 |
| F-253 | `MobAiSuspension`：进记原值 / 出按原值恢复 / 五条清理路径 | `MobAiSuspension:98-186 / 189-232 / 238-269` | 已落 |
| F-254 | 震动豆：范围内所有非玩家生物删 AI **80 刻** | `VibrationBeanTrap:104-124`；`acceptance.md #14` | 已落 |
| F-255 | Q弹：效果存续期删 AI、mount 跳过 | `MobAiSuspension:99-101 / 207-213`；`acceptance.md #16~#18` | 已落 |
| F-256 | 核心储物 tooltip：保证默认链路 + 3 条验收 | `OutpostCoreScreen`（42 行、0 个 tooltip 覆写）+ `OutpostCoreMenu:68/79/84`（3 处 `new Slot(` / **90** 槽位，A-77） | 已落（观感面见 §6） |
| F-257 | `util/Hostility#isHostile`（0 先例） | `Hostility.java` 40 行；使用点 2 个 | 已落 |
| F-258 | `trap.damageMultiplier` 键与中英翻译（开窗） | `FarmstormConfig`；`run/config/farmstorm-server.toml:744-747`；解析器返回 1 键 | 已落 |
| F-259 | `FarmstormExplosion` 第 9 参 `trapSourced` + 乘点①（同一提交，A-72） | `FarmstormExplosion:198`（`this.trapSourced && Hostility.isHostile(entity)`） | 已落 |
| F-260 | `ZangiefCaneTrap#monsterBranch` 乘点② + 日志打乘后值 | `ZangiefCaneTrap:115`；`acceptance.md #19` 的 7.38 → 22.14 | 已落 |
| F-261 | `verify_project.py` 新解析器 `expected_config_keys_v021` | 实跑 `ADDED=['trap.damageMultiplier'] REMOVED=[]` | 已落 |
| F-262 | config 总账 **176 键 / 60 组**（关窗） | `.work/TASK-V021-P44-evidence.md` 的 F-262 节（三处读数） | 已落（本节只引用） |
| F-263 | 语言键总账 **338 × 2**（关窗） | 同上 F-263 节（六个数） | 已落（本节只引用） |
| F-264 | 42 号 §7 的 31 条验收全量执行 | `.work/v021-verify/acceptance.md`（表头 + **31** 行） | 已落（**失败 0 条**：`#19` 以 `armor=0` 重测后通过；`#9` 按裁定改为「构造不可达 · 代码层判据成立」，决策项见 §5） |
| F-265 | 41 号 §9 的 36 条边界复跑 | `.work/v021-verify/boundaries.md`（表头 + **36** 行 + 5 条组汇总） | 已落（**失败 0 条 / 未构造 0 条**；36 条分布 = 通过 **31** + 构造不可达 **2**（`B-03` / `B-12`）+ 【未验证】 **3**（`B-13/14/15`）；六条负向判据 **B-04 / B-06 / B-16 / B-25 / B-26 / B-33 全部成立**） |
| F-266 | 红线、注册面与上游一致性签字 | 本文件 | 已落 |

## 5. 上游一致性与差异登记

- **上游一字未改**：`git diff --stat docs/zh/40-DECISIONS-0.2.1.md docs/zh/41-DESIGN-0.2.1.md docs/zh/42-TECH-0.2.1.md docs/zh/43-TASKS-0.2.1.md docs/zh/45-PROMPT-TASKS-0.2.1.md` → **空**。
- **42 号 §9 的 16 条差异**：本版按 **A-70 ~ A-81** 的处置，逐条不回改上游（1/2/3/4/6/10/12 为描述与口径类；5/11/13/14/15/16 为登记类；7/8/9 是 42 号自己已办的口子）。12 条裁决落点：A-70（6 处版本串）/ A-71（接线 + 注释 + INFO 同提交）/ A-72（第 9 参与乘点①同提交）/ A-73（第 13 类三处写死）/ A-74（两个数值面窗口的开闭）/ A-75（解析器四条契约）/ A-76（**22.5**）/ A-77（3 处调用 / 90 槽位）/ A-78（**149** / register 11→12）/ A-79（不设上限常量）/ A-80（卸载走「载入时结算」）/ A-81（`setNoAi` 实为 2 处、原值表改为落盘）。
- **本次实测新登记的三处偏差**（只登记、不改判据）：
  1. `#1` 的 Java 文件数：42 号表写 `148`，按 45 号 §6.4 / A-78 一律写 **149**（实测 `149`）。
  2. `Farmstorm.java` 的 12 类断言行号：42 号写 `167-172`、43 号写 `169-172`，HEAD 实测 **`:176-179`**（文本逐字未变）。
  3. `VibrationBeanTrap` / `ZangiefCaneTrap` 的 `Monster` 过滤行号：上游写 59 / 174，HEAD 实测 **68 / 180**（文本逐字未变）；`pruneCultivators` 调用点上游写 `:329`，实测 **`:336`**。
- **`#19` 的 0.984 折减已定案（探针陷阱，非实现问题）**：僵尸自带 `generic.armor = 2`，对 7.0 的削减 = `min(20, max(0.4, 2 − 7/2))/25 = 0.4/25 = 1.6%` ⇒ ×0.984（`7.0×0.984 = 6.888`、`7.5×0.984 = 7.38`）。**显式 `armor=0` 后**实测 `blast_carrot` vs 僵尸 **7.0 / 21.0 / 70.0**、`zangief_cane` vs 僵尸 **7.5 / 22.5 / 75.0**、玩家侧 **7.0**（三档）⇒ **42 号 §7 #19 的 21.0 / 22.5 在 `armor=0` 下成立**。
- **`B-03` / `#9` 是给作者的统一决策项（本签字不替作者选）**：口径事实 = 核票 `DISTANCE = 1` ⇒ 已放置已 tick 的核其 3×3 九区块**全部已加载**，该前提在核活着时**不可达**（A-43 反证）；本行真正守的是 **RT-52**，代码面成立（`AreaBlockEntities:39` 只用 `getChunkNow`、全仓 0 处 `level.getChunk`）。处置待作者定：① 判为既存口径并回灌 42 号 §7 #9 与 41 号 `B-03`（措辞与判据一并改）；② 另开修复任务改取块判据。**本 Phase 不改码、不重测。**
- **`B-19` 已单独构造并通过**（原「未单独构造」作废）：释放刻 **195164** = 触发②(**195084**) + 80，触发间隔 **26 刻**；`sources` 恒 1、`originalNoAi` 恒 `0b`、无第二条 `挂起` 日志、第一颗的 195138 已过仍挂起 ⇒ **刷新不叠加**。
- **`B-03` / `B-12` 的决策项已扩到两条边界**（本签字不替作者选）：除 `B-03` / `#9` 外，`B-12`（重启后票恢复前区块未加载）同属 A-43 反证 —— `CoreChunkTickets.java:31` 写明「票是内存态、停服即失：重启后按 `OutpostCoreIndex` 的每条坐标重新申请（`restoreAll`，`:65-67`）」，实测重启日志 `核心区块票补票：2 张（farmstorm_core，distance=1，来源 OutpostCoreIndex）`；核必须先被加载才 tick，故「核在 tick 而 3×3 某区块未加载」不可能同时成立。处置仍是①回灌上游 / ②另开修复任务，**本 Phase 不改码、不重测**。
- **本轮新构造通过六条**：`B-08`（拆核迁移 → `Core` 清空 → 下一轮 `本轮新增 16 条`，无重复坐标）、`B-10`（死条目保留、列表恒 16、新候选不补）、`B-19`、`B-21`（`球内玩家 1 施加成功 1` / `本轮生物 0 删 AI 0`）、`B-26`（`ai_suspend 恢复` 09:17:24.814 **早于** `q_bounce mount 挂上` 09:17:24.816，**2 ms**）、`B-33`（同一爆炸：牛 **7.0 → 7.0**、僵尸 **7.0 → 70.0**）。

## 6. 未验证清单（一条一行）

| 项 | 原因 |
|---|---|
| `#11` / `#12` / `B-13` / `B-14` / `B-15` / `B-16` 的悬停观感 | 本机**没有游戏客户端**（只有专用服务端 + RCON + mineflayer 离线机器人），无渲染面；只给代码层证据（`OutpostCoreScreen` 4 项覆写 = 0、`src/` 内 `renderTooltip` = 0） |
| `#17` ③ 喝牛奶恢复 | RCON 无法让生物喝牛奶；三条路径同一出口（`hasEffect` 收敛），代码面成立 |
| `#20` 的 `lucky_berry` 小爆炸 | 无法用命令构造；另 5 处实测三档不变 |
| `B-03` / `B-12` 的**前提**本身 | 构造不可达（A-43 反证），已按裁定改判；处置列入 §5 的决策项 |
| `B-27` ③ 喝牛奶那一条 | 与 `#17` ③ 同一限制：RCON 无法让生物喝牛奶；四条路径同一出口（`hasEffect` 收敛），代码面成立 |
