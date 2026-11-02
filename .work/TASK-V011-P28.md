# 任务书 · farmstorm v0.1.1 · Phase 28（F-185 ~ F-186）

> 派发者：调度 Agent。你是本 Phase 的唯一执行者。工作目录 `D:\project_farm`（Windows / pwsh）。

---

## 0. 目标（2 个任务、一条链）

| 任务 | 标题 | 复杂度 | 依赖 |
|---|---|---|---|
| **F-185** | 朋友的韭：从落地一次性改为 `effectTicks` 刻的场 | L / 3.0 | — |
| **F-186** | 区块卸载即结束（读档 `Settled` 首刻自杀） | M / 1.5 | F-185 |

把「落地闪一下」换成「落地后 `friend_chives.effectTicks`（默认 200 刻 = 10 秒）的持续场」，并把两个崩溃面（实体泄漏、跨卸载残留）一次堵死。**F-185 做完再动 F-186**，两者都在同一个文件里。

## 1. 边界

**允许改**：`src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java` —— **只有这一个文件**。

**绝对不可动**：
- `entity/ThrownCropEntity.java`（基类只读；`keepAliveAfterImpact()` 的默认必须仍是 `false`，爆竹不受影响）——其 `git diff` 必须为空
- 不新增方块实体（那会给 `friend_chives` 加一个 `block_entity_type`，注册面就不再是 +0）
- 不重写 Q弹（`effect/QbounceEffect.java`）的 `tick`；amplifier 仍为字面量 **0**
- `docs/` 任何文件、`run/`、`build.gradle`、`.work/v011-*.md` 四份台账
- 不动任何 config 键（`friend_chives.effectTicks` 默认 200 / 值域 20~1200、`hitRadius` 默认 6.0、`soundRange` 默认 32.0 全部保持现默认值）
- `sounds.json` 不动（本版不补 `attenuation_distance`；可闻半径由 `max(音量,1) × 16` 承担）

## 2. 必读

1. `docs/zh/32-PROMPT-MAIN-0.1.1.md` 全文
2. `docs/zh/33-PROMPT-TASKS-0.1.1.md` §0.2、**§3（Phase 28 全节）**、§8
3. `docs/zh/31-TASKS-0.1.1.md` F-185 / F-186 详情（约 304~339 行）
4. `docs/zh/30-TECH-0.1.1.md` **§2.7（818~903 行，含完整伪代码与四条要点）**、§7 的 #30 ~ #32
5. `docs/zh/28-DECISIONS-0.1.1.md` D-131 / D-132 / D-133 与裁决 **A-33**
6. `docs/zh/29-DESIGN-0.1.1.md` §8.2 的时序表
7. `.work/v011-BRIEF.md` §1.8（朋友的韭现状逐行）

## 3. 环境事实（调度者已实测）

| 项 | 值 |
|---|---|
| 构建 | `cmd /c ""D:\project_farm\.tools\gw.cmd" build`，基线 exit 0。**唯一入口，禁止直接调 gradle**；可先 `compileJava` 快验 |
| Python | `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（`python` 不在 PATH），先 `$env:PYTHONIOENCODING='utf-8'` |
| 起服（验行为用） | 复制 `run/eula.txt` 与 `run/server.properties` 到独立目录 `run-p28`（`enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），`FS_RUN_LOCK=run-p28 cmd /c ""<repo>\.tools\gw.cmd" runServer -PfsRunDir=run-p28" > .work\p28-server.log 2>&1`，RCON 发 `stop`。**禁止在 `run/` 里起服** |
| 注册面 | 93（本 Phase 不许变） |
| 三个数 | 注册 93 / server config 166 / 语言键 316×2（Phase 27 之后的值；若 Phase 27 未落地则是 315×2，**本 Phase 不动任何键**，按你开工时的实测值记账并写清） |

## 4. 调度者已定的口径

1. 文档与源码冲突 → **以源码为准**，登记偏差表；不许改 `docs/`。
2. 已裁决未回灌条目按 `.work/v011-verdicts.md` 执行并注明 A-xx。本 Phase 相关：**A-33** —— 场长 = `friend_chives.effectTicks`，施加量 = `effectTicks − x`；代码里**只有 `fieldLength` 一个来源**，`200` 字面量不许出现第二次。
3. **禁止任何 git 写操作**。只许 `git status` / `git diff` 取证；提交由调度者做。
4. 客户端 GUI 类验收（音效听感、时序观察）能无头/日志取得的照做，其余**标「未验证」+ 原因**。
5. 遇到做不下去的：停下，写清阻塞点与已做部分。

## 5. 红线与三个数

- 红线只剩「无 Mixin」。网络包限制已整体废除，不许写成通过条件。
- 本 Phase 不新增注册对象、不新增 config 键、不新增语言键。三个数一个都不许动。

## 6. 实现要点

### F-185（严格按 `30-TECH §2.7.2` 的伪代码落地）
1. 字段三个：`private int fieldTick = -1;`（-1 = 还没落地，**不写 NBT**）、`private boolean loadedSettled;`、`private int fieldLength;`
2. `keepAliveAfterImpact()` 重写返回 `true`。
3. `onImpact(ServerLevel, Vec3, @Nullable Entity)` 只做：读一次 `fieldLength = FarmstormConfig.friendChivesEffectTicks.get()` → `applyField(level, pos, 0)` → 播音（既有 `:70-79` 的判据：球内 ≥1 才播、只播一次）→ `setDeltaMovement(Vec3.ZERO)` + `setNoGravity(true)` + `noPhysics = true` → `fieldTick = 0`。
4. `tick()`：`super.tick()` → 非服务端 return → `loadedSettled` 为真则 `discard()` → `fieldTick < 0` return → `fieldTick++` → `>= fieldLength` 则 `discard()` → 否则 `applyField(level, this.position(), fieldTick)`。
5. `applyField(level, center, elapsed)`：`ticks = fieldLength − elapsed`，`<= 0` 直接 return；AABB 粗筛 + `distanceTo(c.position()) <= radius` 欧氏复核；`new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true)`（amplifier 仍是字面量 0）。
6. 不新增方块实体；不改 Q弹 的 `tick`。

### F-186
1. `readAdditionalSaveData` 里 `super.readAdditionalSaveData(tag)` 之后读 `tag.getBoolean("Settled")` 置 `loadedSettled`。
2. `tick()` 在 `super.tick()` 之后第一件事判 `loadedSettled` → `discard()`。
3. `fieldTick` **不写 NBT**。
4. 只依赖基类既有键，不新增自定义 NBT 键。

## 7. 验收标准（逐条给读数或「未验证」）

1. **时序（#30）**：站进球内的生物 Q弹剩余时长逐刻递减（第 1 刻 200、第 2 刻 199……第 199 刻 1）；第 200 刻实体消失。**能起服就用 RCON 实测**（`/data get entity @e[type=farmstorm:friend_chives]`、`/effect give` 读数）；起不了就标「未验证」并给出代码层逐行证据。
2. **200 不是常量（A-33）**：把 `friend_chives.effectTicks` 改成 100 重启重测，递减止于 100、第 100 刻消失；**或**给出代码层证据说明 `200` 字面量只出现在 config 默认值一处。
3. **音效（#31）**：空投不播、命中 ≥1 只播 1 次（`latest.log` 整场一条）。
4. **落地物理**：落点在水流里实体 30 秒不动（±0 格）；踩上去不被推动。代码层证据：三件套已在 `onImpact` 落地。
5. `git diff entity/ThrownCropEntity.java` **为空**；`grep -c "keepAliveAfterImpact"` 在基类仍是默认 `false`。
6. **卸载即结束（#32）**：走 200 格卸载再回来实体不存在；`grep -c '"Settled"' entity/thrown/FriendChivesProjectile.java` ≥ **1**。
7. `.tools\gw.cmd build` 退出码 **0**。
8. `git status --porcelain` 里本版改动只落在 `FriendChivesProjectile.java` + 证据文件 + `run-p28*` 目录（**`run/` 不能有一行改动**）。
9. 三个数不变：注册 93 / server config 166 / 语言键与开工时相同（写清实测值）。

## 8. 必须回报的证据

1. `git diff --stat` 原文 + `FriendChivesProjectile.java` 的完整 diff。
2. `.tools\gw.cmd build` 退出码。
3. 时序读数（若起服）：第 1 / 2 / 199 / 200 刻的 `/data` 或效果读数；改 100 那一轮的同四项。
4. `grep -c` 读数：`"Settled"`、`200`（并说明每处命中是什么）、`keepAliveAfterImpact`、`noPhysics`。
5. **文档-源码偏差表**。
6. **未验证清单**（每条一行：条目 + 为什么本机做不了）。
7. **未关闭的问题**。

## 9. 证据落盘

完整输出写进 `.work/TASK-V011-P28-evidence.md`（新建）。回报正文只贴结论行与第一条错误。

## 10. 完成定义

§7 的 9 条全部有读数或有「未验证」+ 原因，证据已落盘，回报含 7 项。
