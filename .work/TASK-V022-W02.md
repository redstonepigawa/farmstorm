# 任务书 · farmstorm v0.2.2 · W02（F-272 ‖ F-269 ‖ F-271）

> 工作目录 `D:\project_farm`（Windows / pwsh 5.1）。本波 **3 个子代理并行**，各自只做一个任务。
> 前置：W01（F-267 / F-268 / F-270）已落地并提交。

## 0. 三个任务（详见 49 号对应小节）

| 任务 | 标题 | 复杂度 | 涉及文件（**只许动这些**） | 依赖 |
|---|---|---|---|---|
| **F-272** | `FarmstormTags.PROCESSING_BLOCK` + 标签产物 | S / 0.5 | `util/FarmstormTags`、`datagen/ModBlockTagsProvider`、`data/farmstorm/tags/block/processing_block.json` | — |
| **F-269** | 平凡韭菜的可见面：贴图 + 模型注释 | S / 0.5 | `gen_textures.py`、`plain_chives.png`、`datagen/ModItemModelProvider` | **F-268**（W01 已完成） |
| **F-271** | `renderBg` 一次 blit → 两次 blit | S / 0.5 | `client/OutpostCoreScreen.java` 的 `renderBg:37-41` | **F-270**（W01 已完成） |

> ⚠️ **F-271 与 F-270 同文件**：F-270 已经在这个文件里加了 `render` 覆写。你**只改 `renderBg`**，不许碰 F-270 加的那一段，也不许做格式统一。

## 1. 必读

1. `docs/zh/49-TASKS-0.2.2.md` —— **你那个任务的小节** + §0.1~§0.5
2. `docs/zh/46-DECISIONS-0.2.2.md` —— F-272→D-194；F-269→D-193；F-271→D-191
3. `docs/zh/48-TECH-0.2.2.md` —— 对应实现节（F-271 尤其看 §1.1 Bug B 的「两次 blit」原文与 `ContainerScreen.java:31-37`）
4. `.work/v022-BRIEF.md` —— §1.1（两个 bug 的根因）、§1.4（标签体系范式）、§3.4 必改清单第 5/6/16 项

## 2. 已实测基线（W01 之后，开工前自己再核一次）

| 项 | 值 |
|---|---|
| 注册面 | **98**（items **34**，含 `farmstorm:plain_chives`） |
| `mod_version` | **0.2.2** |
| `FarmstormTags` | 现有 **6** 个 TagKey（本波 F-272 加到 **7**） |
| `OutpostCoreScreen` | 已含 F-270 加的 `render` 覆写；`renderBg:37-41` 仍是**一次 blit** |
| config / lang | **176 / 60** / **338 × 2**（本波不动） |

## 3. 硬约束

1. **并行纪律**：只动你任务清单里的文件；不改别人的行、不做格式化、不 `git add`。
2. **禁止任何 git 写操作**；只许 `git status` / `git diff` / `git log` 取证。提交由调度者做。
3. **`docs/` 一字不改**；`build.gradle` / `.tools/` / `run/` 不许改。
4. **构建必须走互斥锁**（§4）；**禁止直接调 gradle**。
5. 本波**不要求起服**；做不了的运行时判据标「静态已验 / 运行时待 F-283」，**不许编读数**。
6. **不许自己新定数值**；拿不准写进「未关闭的问题」。

## 4. 构建锁（复制粘贴用）

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

## 5. 各任务的判决性验收

**F-272**
1. `FarmstormTags` 的 `TagKey<Block>` 从 **6 → 7**，新键名 `PROCESSING_BLOCK` 落盘标签 id 为 **`farmstorm:processing_block`**（注意 BRIEF 与 49 号的命名差：以 49 号 + 46 号 D-194 的最终口径为准，若两处不一致**登记偏差并以 46 号为准**）。
2. `ModBlockTagsProvider` 里 `tag(FarmstormTags.PROCESSING_BLOCK).add(FarmstormBlocks.CULTIVATOR.get())`。
3. 跑一次 datagen（或构建）后 `src/generated/resources/data/farmstorm/tags/block/` 下出现对应 json，内容逐字核对。
4. 构建 exit 0；既有 6 个标签产物**内容不变**。

**F-269**
1. `plain_chives.png` 生成/落在 `src/main/resources/assets/farmstorm/textures/item/`，尺寸与既有物品贴图一致（16×16），文件非空且不是占位纯色以外的意外内容。
2. `ModItemModelProvider` 里有 `plain_chives` 的条目（A-87：48 号 §10 #9 的「31 个」动作）。
3. 构建 exit 0；`src/generated/resources/assets/farmstorm/models/item/plain_chives.json` 生成且内容合法。

**F-271**
1. `renderBg` 从**一次 blit** 改为**两次 blit**：上段高 `rows*18+17`（6 行 ⇒ **125**）从 `v=0` 画到 `y=0`；下段高 **96** 从 `v=126` 画到 `y=125`（照 `ContainerScreen.java:31-37`）。
2. 槽位坐标**不动**（`OutpostCoreMenu:44-46` 的 139/157/175/197 是原版 `ChestMenu` 6 行常量，本来就对）。
3. F-270 加的 `render` 覆写**逐字未变**。
4. 构建 exit 0；`git diff` 只出现 `renderBg` 那一段。

## 6. 回报（`send_message` 给 lead）

- 任务号 + `git diff --stat` + 逐处行号与改前/改后原文；
- **每条验收断言的改前/改后实测值**；
- 命令原文（构建锁等待、`build` 退出码、datagen 产物路径与内容）；
- 偏离登记 + 未验证清单 + 未关闭的问题；
- 明确写出：无任何 git 写操作、没动别人任务的文件。
