# Phase 27 证据 · farmstorm v0.1.1（F-178 ~ F-184）

> 执行者：唯一执行者（Phase 27）。工作目录 `D:\project_farm`，基线 commit `5500295`。
> 本文件只记读数与命令；结论行见回报正文。

---

## 0. 速览（25 条验收的去向）

| # | 验收 | 读数 | 状态 |
|---|---|---|---|
| 1 | `add_plus\|NO_PLUS_ICONS\|build_marker\|compose_icon` = 0 | **0** | 通过 |
| 2 | `--dry-run` 退出 0、计划 26 项、无「绿色「+」」 | exit **0**；item 行 **26**（总 99 条）；「绿色」命中 **0** | 通过 |
| 3 | 其余 24 张 sha256 与动手前逐张相同 | 动手前/后各 29 张快照对照：**只有 wild_apple.png 变**，其余 **28 张逐张相同**（其中 24 张＝带「+」的 25 张减去 wild_apple） | 通过 |
| 4 | `git status` 本任务只动 gen_textures.py 与 wild_apple.png | 该两项 + 其余六个允许文件（见 §8.3） | 通过 |
| 5 | wild_apple.png 64×64 / 6,946 B / sha256 = 源图 | **64×64 / 6,946 B / 0edf673a…7f9**（源图同三项逐字相同） | 通过 |
| 6 | 删图重跑：64×64、与源图逐字节相同、不报尺寸断言、exit 0 | exit **0**，产物 **6,946 B / sha 与源图相同**，日志无「尺寸断言」 | 通过（暂存输出目录，见 D-8） |
| 7 | 连跑两次产物 sha256 相同；其余 24 张不变 | 两轮 99 条产物逐条比对 **diff = 0**；仓库侧 28 张不变 | 通过 |
| 8 | `grep -c wild_apple_grid` = 0 | **0** | 通过 |
| 9 | `compressed_hay_block` 段只有 `sound(...)` 一行变；`SoundType.GRASS` = 0 | diff 只有 :79 一行；`SoundType.GRASS` **0** | 通过 |
| 10 | 五类声音游戏内对照 | — | **未验证**（§10.1）+ 代码层证据 |
| 11 | `outpost_core` 段只新增 `.lightLevel(...)` 一行；`grep -c lightLevel` ≥ 1 | diff 只有 +1 行 `.lightLevel(state -> 15)`；命中 **1** | 通过 |
| 12 | 游戏内 F3 Light = 15 与刷怪平台 | — | **未验证**（§10.2）+ 代码层证据 |
| 13 | 无旧 toml 起服：`[pumpkin_turret]` 恰 5 键 = 15/3/1/40/0.45 | `run-p27`（干净目录）实测 5 键，见 §5.1 | 通过 |
| 14 | 保留 0.1.0 旧 toml 升级：60 / 1 / 补 0.45 | 旧 toml 副本实测 `fireIntervalTicks = 60`、`projectileDamage = 1`、`bulletSpeed = 0.45`（自动补） | 通过 |
| 15 | `bulletSpeed` 手改 5.0 重启回到值域内；注释含 `0.0 ~ 4.0` | 重启后文件值 **4.0**，日志 `is not correct. Correcting`；注释行 `# Range: 0.0 ~ 4.0` | 通过 |
| 16 | `60 刻 = 3 秒` = 0、`40 刻 = 2 秒` = 1；`farmlandBonus` 注释含「额度」 | **段内** 0 / 1；**全文件** 1 / 2（另两处属无关键，见 D-4）；「额度」2 | 通过（按段内口径） |
| 17 | 两份 lang 各 316 键、键集相等、各含 bulletSpeed 1 条 | zh 316 / en 316，键集相等，bulletSpeed 各 **1**；工具 §3 段 `lang 键集一致，共 316 键` | 通过 |
| 18 | `TurretBullet.SPEED` 0 命中；`SPEED` 在 TurretBullet.java = 0 | 全 `src/main/java` **0**；本文件 **0**（大小写敏感） | 通过 |
| 19 | `pumpkinTurretBulletSpeed` 两文件各 ≥ 1 | TurretBullet **1** / PumpkinTurret **1** | 通过 |
| 20 | build 退出 0；`FarmstormEntityTypes.java` diff 空 | exit **0**；该文件 `git diff` 长度 **0** | 通过 |
| 21 | `applyHit` ≥ 3、`MIN_STEP` ≥ 2、`hitResolved` ≥ 3 | **3 / 2 / 4** | 通过 |
| 22 | 贴脸 5 发 5 中 / 3 格外弹道不变 / 单发只结算一次 | — | **未验证**（§10.3）+ 代码层证据 §7.3 |
| 23 | `.tools\gw.cmd build` 退出 0 | exit **0**，`BUILD SUCCESSFUL in 9s` | 通过 |
| 24 | verify_project 的 `[FAIL]`/`[STALE]` 合计恰 1 条、键名只有 farmland.limitPerPlayer、退出码非 0 | 实得 **2 条**（`[FAIL]` 1 + `[STALE]` 1）；**键级 MISMATCH 恰 1 条 = farmland.limitPerPlayer**；退出码 **1** | **偏差 D-5** |
| 25 | `git status --porcelain` 只落在允许文件 + 证据 + build 产物 + run-p27*；`run/` 零改动 | 见 §8.3；`run/` 未被触碰（主 toml sha/字节/mtime 与动手前逐项相同） | 通过 |

---

## 1. F-178 · 生成器删掉叠「+」机制

**文件**：`tools/gen_textures.py`

| 位置（改后行号） | 内容 |
|---|---|
| `:2-8` | 模块 docstring：把「右下角叠绿色 +」改为「D-125 全局作废、生成器不叠角标」 |
| `:63-66` → 删 | `PLUS_SIZE` / `PLUS_ARM` / `PLUS_MARGIN` / `PLUS_COLOR` |
| `Spec`（`:174-188`） | 删 `add_plus: bool = False` 字段 |
| `:274-275` → 删 | `NO_PLUS_ICONS` 及其注释 |
| `:407` → 删 | `build_specs()` 里的 `add_plus=item_id not in NO_PLUS_ICONS,` |
| 原 `:546-560` → 删 | `build_marker()` |
| 原 `:612-623` → 删 | `compose_icon()` |
| `compose()` | 签名 `compose(spec, resolved)`；删 `if spec.add_plus: return compose_icon(...)` 分支 |
| `main()` | 删 `marker = build_marker()`、删 dry-run 的 `action += " + 绿色「+」"`、`compose(spec, resolved)` |

**读数**

```text
Select-String tools\gen_textures.py -Pattern 'add_plus|NO_PLUS_ICONS|build_marker|compose_icon' -CaseSensitive  -> 0
Select-String tools\gen_textures.py -Pattern 'marker' -CaseSensitive                                              -> 0
Select-String tools\gen_textures.py -Pattern 'wild_apple_grid' -CaseSensitive                                     -> 0
```

**dry-run（完整命令与结果）**

```text
& python -X utf8 tools\gen_textures.py --vanilla-assets <26.3 jar> --out .work\p27-gen-out --dry-run
DRYRUN_EXIT=0
item 行 26 条；'^[dry-run] textures' 共 99 条；「绿色」命中 0
[dry-run] 共 99 条：将写入 99 个，源 jar（未写任何文件）
```

> 任务书 §7 #2 写的 `python ... --dry-run` 少了 `--vanilla-assets`/`--out`（脚本 CLI 要求这两项，省略会 exit 2）。按 D-3 处理：报「item 26 行 / 总 99 条」。

**机制层证据（D-125 的「33 px 绿十字消失」）**

| 图 | 仓库（改前） | 生成器产物（改后） |
|---|---|---|
| `textures/item/puffed_wheat.png` | 16×16，`#3FA34D` **33 px** | 16×16，`#3FA34D` **0 px** |
| `textures/item/wild_apple.png` | 16×16，`#3FA34D` **33 px** | **64×64**，`#3FA34D` **0 px** |

---
## 2. F-179 · 狂野苹果直通、尺寸断言放开与图入库

**文件**：`tools/gen_textures.py`、`src/main/resources/assets/farmstorm/textures/item/wild_apple.png`

| 位置（改后行号） | 内容 |
|---|---|
| `:150-152` | `WILD_APPLE_SOURCE = Path(r"C:\Users\DASHpig\Downloads\wild_apple_64x64.png")`；环境变量 `FARMSTORM_WILD_APPLE_SOURCE` 保留；删 `WILD_APPLE_COVERAGE` |
| `:809-826` | 新增 `wild_apple_source_size()`；`draw_wild_apple()` 改为 `raw.convert("RGBA")` 直通；`wild_apple_grid()` 整个删除 |
| `:843-852` | `RAW_COPY_SOURCES = {"wild_apple": wild_apple_source}`（写盘走原字节拷贝） |
| `:926-940` | `expected_size()`：GUI → `GUI_SIZE`；原字节直通项 → 源图实际尺寸；其余 → 16×16 |
| `:950-960` | `compose()` 末尾的尺寸归一改用 `expected_size(spec)`（64×64 不再被缩回 16×16） |
| `:1146-1162` | `:1153` 的尺寸断言**保留**（只放开期望值，A-32）；写盘分支新增原字节拷贝 |

**读数**

```text
仓库 wild_apple.png            : 0edf673aee2dd3001fbcc2096ebbbe4d24f30e6ef569139062ecc4f1d05cb7f9  6946 B  64x64 RGBA
源图 wild_apple_64x64.png      : 0edf673aee2dd3001fbcc2096ebbbe4d24f30e6ef569139062ecc4f1d05cb7f9  6946 B  64x64 RGBA
git diff -- wild_apple.png     : Bin 571 -> 6946 bytes
```

**删图重跑（两轮）**

```text
Remove-Item .work\p27-gen-out\...\item\wild_apple.png
python -X utf8 tools\gen_textures.py --vanilla-assets <jar> --out .work\p27-gen-out   -> RUN2_EXIT=0
完成：共 99 条，写入 99 个（源 jar）      <- 无「尺寸断言失败」
重生成的 wild_apple.png = 0edf673a…7f9 / 6946 B（与源图逐字节相同）
第一轮 vs 第二轮 99 条产物逐条比对：diff = 0
```

**暂存输出 vs 仓库（幂等性对照）**

| 目录 | 逐字节相同 | 不同 |
|---|---|---|
| `textures/block` 72 张（56 阶段 + 16 方块） | **72** | 0 |
| `textures/gui/converter.png` | **相同** | — |
| `textures/item/converter.png`（唯一免叠「+」的图标） | **相同** | — |
| 其余 25 张带「+」的图标 | 0 | 全部（差异就是那 33 px 绿十字，字节一律变小） |
| `wild_apple.png` | — | 16×16/571 B → 64×64/6,946 B |

> 结论：生成器对其余 97 张是幂等的，差别只有「角标被删」与「狂野苹果换图」两件已裁定的事。

---

## 3. F-180 · 压缩干草块音效 GRASS → STONE

**文件**：`src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java:79`

```diff
-                    .sound(SoundType.GRASS));
+                    .sound(SoundType.STONE));
```

- 同链 `.mapColor(MapColor.COLOR_BROWN)` / `.strength(30.0F, 6.0F)` / `.requiresCorrectToolForDrops()` 一字未动（diff 只有上面一行）。
- `Select-String registry/FarmstormBlocks.java -Pattern 'SoundType.GRASS'` → **0**。
- 五类声音同源的代码层依据：`SoundType` 的 `getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound` 全部由方块属性 `sound(...)` 决定，换成 `SoundType.STONE` 后五类一起变（A-31）。
- 游戏内听感对照：**未验证**（§10.1）。

---

## 4. F-181 · 据点核心亮度 15

**文件**：`.../registry/FarmstormBlocks.java:90`

```diff
                     .pushReaction(PushReaction.BLOCK)
+                    .lightLevel(state -> 15));
```

- `MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `sound(SoundType.STONE)` / `pushReaction(PushReaction.BLOCK)` 全部原样。
- `Select-String FarmstormBlocks.java -Pattern 'lightLevel'` → **1**（`Block` 的 `lightLevel` 是 `ToIntFunction<BlockState>`，用 lambda）。
- 未做成 config（D-141 补充：否决）。
- F3 读数 / 光斑 / 刷怪平台：**未验证**（§10.2）。

---
## 5. F-182 · pumpkin_turret 三键 + 1 条语言键 + 2 处注释

**文件**：`config/FarmstormConfig.java`（`:72` / `:425-438` / `:916-917`）、`datagen/ModLangProvider.java`（`:14-15` / `:32-33` / `:257`）、`src/generated/resources/assets/farmstorm/lang/{zh_cn,en_us}.json`

| 位置（改后） | 改动 |
|---|---|
| `:428` | `projectileDamage` 默认 **1 → 3**（值域 0–100 不动） |
| `:432` | 注释「60 刻 = 3 秒」→「**40 刻 = 2 秒**」 |
| `:434` | `fireIntervalTicks` 默认 **60 → 40**（值域 1–72000 不动） |
| `:436-438` | 新增 `bulletSpeed`：double、默认 **0.45**、值域 **0.0 ~ 4.0**、带 `.translation(...)` |
| `:72` | 字段声明 `pumpkinTurretBulletSpeed` |
| `:916-917` | `farmlandBonus` 注释补「本键同时是每核容量与队伍额度基数（额度 = 本键 × 己方核数，RT-33）」 |
| `ModLangProvider:257` | `config(entries, "pumpkin_turret.bulletSpeed", "导弹速度（格/刻）", "Missile speed (blocks per tick)")` |
| `ModLangProvider:14-15, 32-33` | 类注释同步：config **166 → 167**、总数 **315 → 316**，并补 v0.1.1 沿革一句（30 号 §9 第 7 条） |

**5.1 无旧 toml 起服（`run-p27`，干净目录）**

```text
env FS_RUN_LOCK=run-p27
cmd /c "D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-p27 > .work\p27-server.log 2>&1
-> 自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 /
   sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 /
   data_components 1 / menu_types 1 -> OK

run-p27/config/farmstorm-server.toml（新建，19,105 B，段 59，键 167）
[pumpkin_turret]
  #索敌切比雪夫半径                # Default: 15    # Range: 0 ~ 64
  targetRadius = 15
  #导弹命中造成的虚空伤害点数       # Default: 3     # Range: 0 ~ 100
  projectileDamage = 3
  #命中时施加的击退等级             # Default: 1     # Range: 0 ~ 10
  knockbackLevel = 1
  #D-28：开火间隔（40 刻 = 2 秒）    # Default: 40    # Range: 1 ~ 72000
  fireIntervalTicks = 40
  #导弹速度（格/刻），0.45 = 每秒 9 格；范围 0.0 ~ 4.0   # Default: 0.45  # Range: 0.0 ~ 4.0
  bulletSpeed = 0.45
```

> 复现（第二份干净目录）：另起 `run-p27-fresh`（同样无 config）再起服一次，读数逐项相同
> —— 19,105 B / 段 59 / 键 167 / `[pumpkin_turret]` 5 键 = 15 / 3 / 1 / 40 / 0.45（日志 `.work/p27-server-fresh.log`）。
> 该目录的 toml 保留原样供复核；`run-p27` 那份后来被值域测试改成过 5.0，故另留这一份干净样本。

**5.2 保留 0.1.0 旧 toml 直接升级（`run-p27-old`）**

旧 toml = `Copy-Item run\config\farmstorm-server.toml`（sha `03a68f6b…afa9` / 18,879 B / 166 键，与主 toml 逐字节相同）。

```text
-> 起服后 run-p27-old/config/farmstorm-server.toml（19,105 B、167 键）
[pumpkin_turret]
  projectileDamage = 1        <- 仍是旧值
  fireIntervalTicks = 60      <- 仍是旧值
  bulletSpeed = 0.45          <- 自动补上（RT-38 / D-147）
  （注释行已被改写成新默认值：# Default: 3 / # Default: 40）
```

**5.3 值域（`bulletSpeed` 手改 5.0）**

```text
（UTF-8 无 BOM 改写 run-p27/config/farmstorm-server.toml：bulletSpeed = 5.0）
-> 起服日志 [Server thread/WARN] [ne.ne.fm.co.ConfigTracker/CONFIG]:
   Configuration file D:\project_farm\run-p27\config\farmstorm-server.toml is not correct. Correcting
-> 文件内加载值：bulletSpeed = 4.0（回到 0.0 ~ 4.0 上界）
```

**5.4 注释 greps**

```text
Select-String config/FarmstormConfig.java -Pattern '60 刻 = 3 秒' -CaseSensitive  -> 1（:565 fangTrapSlownessDurationTicks，与本键无关）
Select-String config/FarmstormConfig.java -Pattern '40 刻 = 2 秒' -CaseSensitive  -> 2（:432 本键 + :923 outpost_core.boundaryParticleIntervalTicks）
pumpkin_turret 段（:422-439）内：'60 刻 = 3 秒' -> 0，'40 刻 = 2 秒' -> 1
Select-String config/FarmstormConfig.java -Pattern '额度' -CaseSensitive          -> 2（:917 新增 + :910 farmland.limitPerPlayer 的旧注释）
```

**5.5 语言键**

```text
zh_cn.json keys = 316 / en_us.json keys = 316 / 键集相等 = True
farmstorm.configuration.pumpkin_turret.bulletSpeed: zh 1 条、en 1 条
"farmstorm.configuration.pumpkin_turret.bulletSpeed": "导弹速度（格/刻）"
"farmstorm.configuration.pumpkin_turret.bulletSpeed": "Missile speed (blocks per tick)"
```

---

## 6. F-183 · 删 `TurretBullet.SPEED` 并改两处读点

| 文件 | 改动 |
|---|---|
| `entity/TurretBullet.java` | 删常量（原 `:27-28`）；`steerTowards` 改读 `FarmstormConfig.pumpkinTurretBulletSpeed.get()` |
| `behavior/impl/PumpkinTurret.java:75` | `direction.scale(TurretBullet.SPEED)` → `direction.scale(FarmstormConfig.pumpkinTurretBulletSpeed.get())` |

```text
Get-ChildItem src\main\java -Recurse -Filter *.java | Select-String 'TurretBullet\.SPEED' -CaseSensitive -> 0
Select-String entity/TurretBullet.java -Pattern 'SPEED' -CaseSensitive                                        -> 0
Select-String entity/TurretBullet.java -Pattern 'pumpkinTurretBulletSpeed'                                   -> 1
Select-String behavior/impl/PumpkinTurret.java -Pattern 'pumpkinTurretBulletSpeed'                           -> 1
FarmstormEntityTypes.java 的 git diff 长度                                                                   -> 0
```

> 取证中的自纠：给 `bulletSpeed` 写的注释里最初出现字面量 `TurretBullet.SPEED`，会让 #18 的 0 命中变 1，
> 已改写为「原硬编码常量 0.6 已删」（`FarmstormConfig:435`），复测 0 命中。

---

## 7. F-184 · 炮弹近距收敛与共用结算体

**文件**：`entity/TurretBullet.java`

| 行（改后） | 内容 |
|---|---|
| `:27-28` | `private static final double MIN_STEP = 0.05;` |
| `:43-44` | `private boolean hitResolved;` |
| `:96-111` | `steerTowards` 三分支：`dist <= MIN_STEP` → `resolveHit(aim)`；`dist <= step` → `setDeltaMovement(delta)`；否则 `setDeltaMovement(delta.normalize().scale(step))` |
| `:113-121` | `onHitEntity` → `resolveHit(result.getEntity())` |
| `:123-131` | `resolveHit`：`hitResolved` 闸 → `applyHit` → `discard()` |
| `:133-157` | `applyHit`：原 `:98-119` 的结算体原样搬家（`discard()` 移出） |

**7.1 三分支逐行（代码层证据）**

```java
private void steerTowards(Entity aim) {
    double step = FarmstormConfig.pumpkinTurretBulletSpeed.get();
    Vec3 delta = aim.getBoundingBox().getCenter().subtract(this.position());
    double dist = delta.length();
    if (dist <= MIN_STEP) {
        this.resolveHit(aim);            // 分支③ 贴脸直判
        return;
    }
    if (dist <= step) {
        this.setDeltaMovement(delta);    // 分支② 一步走完剩余距离
        return;
    }
    this.setDeltaMovement(delta.normalize().scale(step));   // 分支① 与改前逐字一致
}
```

**7.2 「原样搬家」的 diff 证据**：`git diff` 里 `hit.hurt(void_bolt, projectileDamage)` / `FarmstormAdvancements.award(serverPlayer, "turret_hit")` / 击退算式三段显示为**未改动的上下文行**（只有 `Entity hit = result.getEntity();` 与 `this.discard();` 被移出）。伤害类型 `farmstorm:void_bolt`、进度 `turret_hit`、击退算式一个字符未改。

**7.3 单发只结算一次（代码层）**：两条入口都收在 `resolveHit`，闸在最前：`if (this.hitResolved) return; this.hitResolved = true;`。

```text
Select-String entity/TurretBullet.java -Pattern 'applyHit' -CaseSensitive    -> 3（定义 + 调用 + resolveHit 的 javadoc 提及）
Select-String entity/TurretBullet.java -Pattern 'MIN_STEP' -CaseSensitive    -> 2（定义 + 分支③ 判定）
Select-String entity/TurretBullet.java -Pattern 'hitResolved' -CaseSensitive -> 4（字段 + 闸两处 + onHitEntity 注释）
```

**7.4 未做（D-129 第 3 条）**：无转向角上限、无最大追击时长、无制导参数、实体父类未换（仍是 `ThrowableItemProjectile`）。

---
## 8. 收口

### 8.1 build

```text
cmd /c "D:\project_farm\.tools\gw.cmd" compileJava   -> exit 0（BUILD SUCCESSFUL in 12s）
cmd /c "D:\project_farm\.tools\gw.cmd" build         -> exit 0（BUILD SUCCESSFUL in 9s）
产物：build/libs/farmstorm-0.1.1+mc1.21.1.jar  1,023,754 B  2026/11/2 23:49:09
jar tf 的条目：assets/farmstorm/textures/item/wild_apple.png
从 jar 解出该条目：0edf673a…7f9 / 6,946 B（与源图逐字节相同）
```

### 8.2 verify_project.py（A-40 中间态）

```text
python -X utf8 tools/verify_project.py -> EXITCODE=1
  30-TECH §4.1：58 组 / 166 键（v0.1.1 的 58 行数据表现算，其中 165 个键名已在前四份清单里）
  剔除已删键 1 个：farmland.limitPerPlayer
  文档口径键数: 166（…按并集去重后 167，再扣掉 1 个已删键 = 166）
  生成 toml 键数: 166
  已核 registry 对象: 93 / 93
  [OK]   lang 键集一致，共 316 键
  [STALE] run/config/farmstorm-server.toml 过期（mtime 2026-11-02 02:39:02，早于 config/FarmstormConfig.java 的 2026-11-02 23:47:50）：缺 1 个键、多 1 个键，重新生成即可消除（缺 ['pumpkin_turret.bulletSpeed']，多 ['farmland.limitPerPlayer']）；跑一次 .tools\gw.cmd runServer 让它重新生成后再验
  [FAIL] toml 多 1 个键且源码里存在（文档漏记 MISMATCH）: ['farmland.limitPerPlayer']
结论：FAIL 2 项，WARN 0 项
```

- **键级 MISMATCH 恰好 1 条**，键名只有 `farmland.limitPerPlayer` ⇒ 与调度者「F-182 后只剩这一条」一致。
- 行数由 2（P26：`[FAIL]` 2 + `[STALE]` 0）变为「`[FAIL]` 1 + `[STALE]` 1」= **仍 2 条**：`bulletSpeed` 现在已存在于源码而旧 toml 里没有，工具的 `stale_keys` 分支因此把它从 `[FAIL]` 挪到 `[STALE]`。**没有**为了让它归零去动 `docs/`、`run/config/` 或其它 config 组（D-5）。

### 8.3 git status（本 Phase 的落点）

```text
git --no-pager diff --stat
 .work/TASK-V011-P27.md                             |   3 +-      <- 动手前已是 M（基线）
 README.md                                          |  59 ++++++-  <- 动手前已是 M（基线）
 .../resources/assets/farmstorm/lang/en_us.json     |   1 +
 .../resources/assets/farmstorm/lang/zh_cn.json     |   1 +
 .../farmstorm/behavior/impl/PumpkinTurret.java     |   2 +-
 .../dashpig/farmstorm/config/FarmstormConfig.java  |  14 +-
 .../dashpig/farmstorm/datagen/ModLangProvider.java |   6 +-
 .../com/dashpig/farmstorm/entity/TurretBullet.java |  49 ++++++-
 .../farmstorm/registry/FarmstormBlocks.java        |   5 +-
 .../assets/farmstorm/textures/item/wild_apple.png  | Bin 571 -> 6946 bytes
 tools/gen_textures.py                              | 148 ++++++------
 11 files changed, 169 insertions(+), 119 deletions(-)
```

- `git status --porcelain` 的非 `??` 行 = 上面 11 个文件（`.work/TASK-V011-P27.md` 与 `README.md` 属基线残留）。
- `??` 里非 `.work/`、非 `build/` 的条目：**0**（`run-p27` / `run-p27-old` 被 gitignore，与 `run/` 同）。
- `run/` 零改动：`run/config/farmstorm-server.toml` 的 sha256 `03a68f6b…afa9` / 18,879 B / mtime `2026-11-02 02:39:02` / 段 59 / 键 166，与动手前逐项相同；本 Phase 未在 `run/` 起过服。
- `FarmstormEntityTypes.java`：`git diff` 长度 0。

### 8.4 24 张 PNG 的 sha256 清单（动手前 / 收尾后）

两份清单全文见 `.work/p27-png-baseline.txt` 与 `.work/p27-png-final.txt`（各 29 行，格式：sha256  字节  文件名）。
`Compare-Object` 的结果只有 `wild_apple.png` 一行进出：

```text
0edf673aee2dd3001fbcc2096ebbbe4d24f30e6ef569139062ecc4f1d05cb7f9  6946  wild_apple.png   =>   （收尾后）
391360e8ede4edd5e2a83679500ad3247a2921d381b92fc7d1db6ff90aae9182  571   wild_apple.png   <=   （动手前）
changed entries: 2   ->  其余 28 张逐张相同
```

其余 28 张（逐张相同的那些，sha256 / 字节 / 文件名）：

```text
de0f73e6874d948ef4ff611f494721ec070be7ddd93594033e6c2e782a99fb1d  367  awakened_pitcher_plant.png
89e5ba266b5e32ebe8f9249a0be46874c685993d88b1d14718328b4a59c0a4ef  395  awakened_pitcher_plant_pod.png
0c1135cb55f9ffd425d6fb54007122c74f4b47107ccec0418af6d7a061376b55  363  awakened_torchflower.png
5eae1fb76c6dee0e1e6227c3c3ea3a38c8b63beac53f394d77d29a63c387058c  320  awakened_torchflower_seeds.png
c88aa6348f03f05a59784e0257aef7f994a8d99069411006bb599a46729c2dcf  299  bamboo_firecracker.png
6fac2dfb14ebb5a5d1b96d3aaa9853c8c9e9be76e9ee7713c186f40f56c2c437  391  blast_carrot.png
3fb180262b917912c837caa33d75c5e5966611326ce63e84743fcfbe666013c3  213  brown_mushroom_puff.png
5b353ddc28dd4c4ea0b7df42123bda2c43af5bda74eff0c5e8876c185ba30301  735  chive_seeds.png
81fecc55e22015500deee31629d1d335b419ec143227026d52cdb335fb20a059  469  compressed_puffed_wheat.png
6fcd4ffe3c5a9284a64b2d6ea8dc3004ba38a1ca7e52ab7dfd0d4a180603d1f0  587  converter.png
187ba1884a2a3ea29e7a86da22ec99865312048583e851391f06b8d7412ff8da  420  fang_trap_cactus.png
1208f270a5b484b3d053be9d3ad00cf74bddc6feba1fbc6c813120528b9f1015  867  friend_chives.png
f53df204b0ffcdf25dbc1437156e2be0729bf53b49f62cdaa2e2ae9a768a5307  347  lucky_berry.png
291f7d0cf85e6dd4ce27c100179ba26a10c3d197ecc27dfa861ea6ed7c0cd86f  307  melon_grenade.png
10ccaa8d2d0bf717ca3aa71d4899722b0bd855124ef7433c3e21fccde3f7f95d  235  melon_grenade_seeds.png
8258a73d4ea7aa639d79de0442a5dfa1aeceb3c9a6cb4b85f1bba6b90a79ca79  328  melon_slice_bomb.png
e08a3eea32d8b3d288fe23c5a99679d582baa58ad20c7b080eaee6f33cc33900  324  molotov.png
7f26d07fdf70d034f5632a626d6cfcdf73e64be8b810c4614c200203696ff8de  304  power_beet.png
cb25e11c0110ab659d88e3d107f45b0622755509f9795bd723a1f676f70841ab  252  power_beet_seeds.png
75f101ae1c491d868c410388a108e49a6ae0414456ea1b9f8dba056f722887a4  398  puffed_wheat.png
45fbd0f4b7ab209f1a2d14b6014c293c969deb30d8c017a35417725444e6c2c4  192  puffed_wheat_seeds.png
bc2165bcbc393db12b20e7ed24e3536f048e0fc4af067d042f13f991ca8ba26d  320  pumpkin_turret.png
799ff4241dd5113f473a80488961d0f91c52e50c1aaf9ef0e128cb4fd07b7024  197  pumpkin_turret_seeds.png
2ca3b94c61b2cdfaf5e9ee73c26ada5a57363878c4007b3d96dc0d878bc12ea3  194  red_mushroom_puff.png
a2f2318330e0adce0effad8d273df72676a875cfa657394134a144b15add7476  455  toxic_ubi_server.png
0479dbed3ba33b5a6cfe4d25dd0538756cd832981b8ebaaa0b7824f65ced6047  319  ubi_server.png
27f886f1d8986eb1697a64efcadfaa6faece4658ad214efd24be49ad60eb7a30  258  vibration_bean.png
0edf673aee2dd3001fbcc2096ebbbe4d24f30e6ef569139062ecc4f1d05cb7f9  6946  wild_apple.png
a3c027878827a5e2139caec5e8abb0d444173f58372475604130d7e8970b807e  355  zangief_cane.png
```


**动手前（29 张）**

```text
de0f73e6874d948ef4ff611f494721ec070be7ddd93594033e6c2e782a99fb1d  367  awakened_pitcher_plant.png
89e5ba266b5e32ebe8f9249a0be46874c685993d88b1d14718328b4a59c0a4ef  395  awakened_pitcher_plant_pod.png
0c1135cb55f9ffd425d6fb54007122c74f4b47107ccec0418af6d7a061376b55  363  awakened_torchflower.png
5eae1fb76c6dee0e1e6227c3c3ea3a38c8b63beac53f394d77d29a63c387058c  320  awakened_torchflower_seeds.png
c88aa6348f03f05a59784e0257aef7f994a8d99069411006bb599a46729c2dcf  299  bamboo_firecracker.png
6fac2dfb14ebb5a5d1b96d3aaa9853c8c9e9be76e9ee7713c186f40f56c2c437  391  blast_carrot.png
3fb180262b917912c837caa33d75c5e5966611326ce63e84743fcfbe666013c3  213  brown_mushroom_puff.png
5b353ddc28dd4c4ea0b7df42123bda2c43af5bda74eff0c5e8876c185ba30301  735  chive_seeds.png
81fecc55e22015500deee31629d1d335b419ec143227026d52cdb335fb20a059  469  compressed_puffed_wheat.png
6fcd4ffe3c5a9284a64b2d6ea8dc3004ba38a1ca7e52ab7dfd0d4a180603d1f0  587  converter.png
187ba1884a2a3ea29e7a86da22ec99865312048583e851391f06b8d7412ff8da  420  fang_trap_cactus.png
1208f270a5b484b3d053be9d3ad00cf74bddc6feba1fbc6c813120528b9f1015  867  friend_chives.png
f53df204b0ffcdf25dbc1437156e2be0729bf53b49f62cdaa2e2ae9a768a5307  347  lucky_berry.png
291f7d0cf85e6dd4ce27c100179ba26a10c3d197ecc27dfa861ea6ed7c0cd86f  307  melon_grenade.png
10ccaa8d2d0bf717ca3aa71d4899722b0bd855124ef7433c3e21fccde3f7f95d  235  melon_grenade_seeds.png
8258a73d4ea7aa639d79de0442a5dfa1aeceb3c9a6cb4b85f1bba6b90a79ca79  328  melon_slice_bomb.png
e08a3eea32d8b3d288fe23c5a99679d582baa58ad20c7b080eaee6f33cc33900  324  molotov.png
7f26d07fdf70d034f5632a626d6cfcdf73e64be8b810c4614c200203696ff8de  304  power_beet.png
cb25e11c0110ab659d88e3d107f45b0622755509f9795bd723a1f676f70841ab  252  power_beet_seeds.png
75f101ae1c491d868c410388a108e49a6ae0414456ea1b9f8dba056f722887a4  398  puffed_wheat.png
45fbd0f4b7ab209f1a2d14b6014c293c969deb30d8c017a35417725444e6c2c4  192  puffed_wheat_seeds.png
bc2165bcbc393db12b20e7ed24e3536f048e0fc4af067d042f13f991ca8ba26d  320  pumpkin_turret.png
799ff4241dd5113f473a80488961d0f91c52e50c1aaf9ef0e128cb4fd07b7024  197  pumpkin_turret_seeds.png
2ca3b94c61b2cdfaf5e9ee73c26ada5a57363878c4007b3d96dc0d878bc12ea3  194  red_mushroom_puff.png
a2f2318330e0adce0effad8d273df72676a875cfa657394134a144b15add7476  455  toxic_ubi_server.png
0479dbed3ba33b5a6cfe4d25dd0538756cd832981b8ebaaa0b7824f65ced6047  319  ubi_server.png
27f886f1d8986eb1697a64efcadfaa6faece4658ad214efd24be49ad60eb7a30  258  vibration_bean.png
391360e8ede4edd5e2a83679500ad3247a2921d381b92fc7d1db6ff90aae9182  571  wild_apple.png
a3c027878827a5e2139caec5e8abb0d444173f58372475604130d7e8970b807e  355  zangief_cane.png
```
> 其中带「+」的 25 张里有 24 张不变（D-125 的 25 − 1 = 24）；另外 4 张不变的是 `converter.png`（免叠）与 `chive_seeds` / `friend_chives` / `vibration_bean`（非生成器产出）。

---
## 9. 文档-源码偏差表

| # | 文档位置 | 文档说法 | 实测 | 处理 |
|---|---|---|---|---|
| D-1 | 31 号 F-178 验收 3 | 删掉入库的 `wild_apple.png` 后跑生成器，产物是 **16×16** 且无绿十字 | 与 F-179 同批落地后产物是 **64×64**；「无绿十字」成立（`#3FA34D` 0 px） | 机制层换用 `puffed_wheat`（33 px → 0 px）证明；尺寸按 F-179 的 64×64 验 |
| D-2 | 任务书 §1 / §7 #3 / 33 号 §2.1「其余 **24** 张（25 − 1）」 | 仓里是 25 张 | `textures/item` 实有 **29** 张 = 26 个 ICONS 产物 + 3 张 convert_media 产物；带「+」的是 **25** 张（`converter` 免叠）⇒ 25 − 1 = 24 指带「+」的那些 | 两个数都报：带「+」的 24 张不变；目录内共 28 张不变 |
| D-3 | 任务书 §7 #2 / 31 号 F-178 验收 2 | `python -X utf8 tools/gen_textures.py --dry-run` 退出 0、计划 **26** 项 | 脚本 CLI 要求 `--vanilla-assets` 与 `--out`（缺则 exit 2）；目标表共 99 条，其中 item **26** 条 | 用带齐参数的 dry-run：exit 0、item 行 26、总 99 条、无「绿色「+」」 |
| D-4 | 任务书 §7 #16 / 31 号 F-182 验收 4 | `grep -c "60 刻 = 3 秒"` = 0、`"40 刻 = 2 秒"` = 1 | 全文件另有两处无关命中：`fangTrapSlownessDurationTicks` 的「60 刻 = 3 秒」、`outpost_core.boundaryParticleIntervalTicks` 的「40 刻 = 2 秒」 | 按 §6「同处注释」口径限 `pumpkin_turret` 段（:422-439）计数：0 / 1；全文件读数 1 / 2 一并登记，未去改无关键的注释 |
| D-5 | 任务书 §7 #24（A-40 窗口） | `[FAIL]` / `[STALE]` 合计**恰好 1 条** | 实得 2 条：`[FAIL]` 1 + `[STALE]` 1。`bulletSpeed` 已进源码但旧 toml 未重生 ⇒ 工具的 `stale_keys` 分支把它记为 `[STALE]`；**键级**差异确为 1 条（`farmland.limitPerPlayer`），退出码 1 | 按「键级 1 条」认定 A-40 收缩成立；要行数也归 1，唯一合法路径是重生 `run/config` 的 toml（本 Phase 明令禁止），故登记不改 |
| D-6 | 31 号 F-179 验收 4 | dry-run 里 `wild_apple` 与 GUI 的期望尺寸「两行不同」 | 原 dry-run 不打印期望尺寸 | dry-run 行尾加「期望 WxH」：`wild_apple … 期望 64x64`、`gui/converter … 期望 176x166` |
| D-7 | 任务书 §6 F-179.4 / 31 号 F-179.4 | 入库 PNG 是源图**逐字节副本** | `Image.save()` 必然重编码：同一张 64×64 RGBA 重编码后 **7,172 B**（≠ 6,946 B），sha 也不同 | 新增 `RAW_COPY_SOURCES` + `shutil.copyfile`：解码走 Pillow（供尺寸断言），写盘用源图原字节 |
| D-8 | 任务书 §1 / §6 F-178.5 | 「本任务不重生成其余 24 张」 | 全量跑一次写到 `src/main/resources` 会把 25 张带「+」的图标一起改写（机制已按 D-125 删除） | 全量跑一律指向暂存目录 `.work/p27-gen-out`；仓库侧只写 `wild_apple.png` 一张 |
| D-9 | 30 / 31 号的 gen_textures 行号（`:154`/`:155`/`:275`/`:407`/`:546`/`:612`/`:849`/`:884-895`/`:974-975`/`:982`/`:1127`/`:1141`/`:1152`）与 `FarmstormConfig:425-433`、`FarmstormBlocks:79/82-89`、`TurretBullet:28/90-96/98-119` | 各行号 | 改前逐条核对**全部命中** | 无动作；正面记录 |
| D-10 | 30 号 §9 第 7 条（改键必须同改类注释） | `ModLangProvider` 类注释里的键数叙述 | 注释里的「166 config / 315」在本 Phase 必须同步 | 已改 166 → 167、315 → 316 并补沿革一句；`:35` 的「155 个 config 键」是更早的历史陈述，本轮未动（见 §11.3） |

---

## 10. 未验证清单（本机做不到的）

| # | 条目 | 为什么本机做不了 |
|---|---|---|
| 10.1 | 压缩干草块**五类声音**（破坏 / 放置 / 踩踏 / 挖掘 / 落地）与 `block.stone.*` 的正负对照 | 需图形客户端与听觉判断；本机只有无头专用服务器（能起服、能 RCON，但没有声卡与画面）。代码层已确认五类同源于 `SoundType.STONE`（`getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound`） |
| 10.2 | 据点核心 F3 `Light = 15`、15 格内光斑 / 16 格外无光、光斑正中 100 秒 0 刷怪 / 外侧 ≥ 1 | 同上（F3 调试界面与刷怪观察都要客户端） |
| 10.3 | 贴脸 5 发 5 中、目标掉 15 点、5 秒内无反向位移；3 格外弹道形状与改前一致；单发只掉 3 点、`latest.log` 只剩一条伤害记录 | 需要在客户端里摆炮台、召唤 20 血生物并逐发观察；无头服务器没有可操作的玩家 |
| 10.4 | 物品栏里 64×64 狂野苹果不破图、显示面积仍是 16 GUI px（RT-37） | 需客户端渲染 |
| 10.5 | 把 `bulletSpeed` 手改 0.1 后 `/data get entity @e[type=farmstorm:turret_bullet] Motion` 的模长 ≈ 0.1 | 需要先有炮台与目标把炮弹造出来，再读实体 NBT；本次只验证了「键存在、值域生效、两处读点同源」（grep + 代码） |
| 10.6 | 挖掘耗时不变（同一把镐挖压缩干草块的刻数与改前相同） | 需客户端交互；代码层依据：`strength(30.0F, 6.0F)` 未动，`SoundType` 不参与硬度 |

---

## 11. 未关闭的问题

1. **verify_project 的行数口径（D-5）**：要让 `[FAIL]` / `[STALE]` 合计回到 1 条，必须让 `run/config/farmstorm-server.toml` 重生一次（`bulletSpeed` 进文件、`[STALE]` 消失、只剩 1 条 `[FAIL]`）。本 Phase 冻结 `run/config`，留给下一个允许起服的 Phase。
2. **`run/config/farmstorm-server.toml` 仍是旧值**：`fireIntervalTicks = 60`、`projectileDamage = 1`、无 `bulletSpeed`。这是 A-40 中间态要的样本，不是缺陷；交付文档的「升级须知」需继续写明「删旧 toml 或手改两键；`bulletSpeed` 会自动补」。
3. **`ModLangProvider` 类注释里的历史数字**：`:35` 仍写「155 个 config 键」，与本版 167 不符。属既有陈旧（本版之前就不符），本轮只同步了 `:14-15` 的条目构成与 `:32-33` 的沿革，未擅自改历史叙述。
4. **生成器全量重跑的语义**：机制删除后，任何人执行 `gen_textures.py`（不带暂存目录）都会把 25 张带「+」的图标刷成无角标版，与 D-125「本版只重生成 1 张」相冲。本版按裁定接受该副作用；下一版批量重生成时一次落地。
5. **取证工具坑（与模组无关，供后续复现参考）**：本机 `pwsh` 实为 Windows PowerShell 5.1，`Set-Content -Encoding utf8` 会写 BOM；带 BOM 的 `farmstorm-server.toml` 会让 NeoForge 的 nightconfig 报 `Invalid bare key`（首个键名被 BOM 污染）并重建配置（本次第一次值域测试因此作废，已改用 `UTF8Encoding($false)` 重做）。手工改 toml 取证必须写 UTF-8 无 BOM。

---

## 12. 复现命令清单

```text
# 构建
cmd /c "D:\project_farm\.tools\gw.cmd" compileJava
cmd /c "D:\project_farm\.tools\gw.cmd" build

# 材质生成器（dry-run）
<py> -X utf8 tools\gen_textures.py --vanilla-assets "D:\minecraft\.minecraft\versions\26.3-NeoForge_26.3.0.99\26.3-NeoForge_26.3.0.99.jar" --out .work\p27-gen-out --dry-run

# 材质生成器（实跑，暂存目录）
<py> -X utf8 tools\gen_textures.py --vanilla-assets <jar> --out .work\p27-gen-out

# 语言文件重生成
cmd /c "D:\project_farm\.tools\gw.cmd" runData -PfsRunDir=run-p27

# 起服（配置取证；每格一个独立目录，绝不在 run\ 起服）
env FS_RUN_LOCK=run-p27 ; cmd /c "D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-p27
<py> .work\p27-rcon.py 25575 fsverify stop

# 工具
<py> -X utf8 tools\verify_project.py
```

`<py> = C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（先设 `PYTHONIOENCODING=utf-8`）。

---

## 13. 落盘的过程文件

| 文件 | 内容 |
|---|---|
| `.work\p27-png-baseline.txt` | 动手前 29 张 sha256 |
| `.work\p27-png-final.txt` | 收尾后 29 张 sha256 |
| `.work\p27-dryrun.log` | 生成器 dry-run 全文 |
| `.work\p27-run1.log` / `.work\p27-run2.log` | 两次实跑（含删图重跑） |
| `.work\p27-run1-hashes.txt` / `.work\p27-run2-hashes.txt` | 两轮 99 条产物 sha256 |
| `.work\p27-gen-textures.diff` | `gen_textures.py` 的完整 diff |
| `.work\p27-rundata.log` | runData 全文 |
| `.work\p27-build.log` | build 全文 |
| `.work\p27-server.log` | run-p27 首次起服（新建 toml） |
| `.work\p27-server-old.log` | run-p27-old（旧 toml 升级） |
| `.work\p27-server-fresh.log` | run-p27-fresh（干净目录复现，toml 原样保留） |
| `.work\p27-server-clamp.log` | 值域测试第一次（BOM 事故，作废） |
| `.work\p27-server-clamp2.log` | 值域测试重做（5.0 → 4.0） |
| `.work\p27-verify.log` | verify_project 全文 |
| `.work\p27-rcon.py` | 关服用的 RCON 小客户端 |
| `.work\p27-git-baseline.txt` | 动手前的 tracked 改动快照 |