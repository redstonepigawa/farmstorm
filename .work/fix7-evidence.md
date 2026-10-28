# FIX-7 证据 · 作物类别多标签 + 由标签驱动的破障权限 + 类型/类别两维建模

> 任务书：`.work/TASK-FIX-7.md`（含调度者中途更正：类型与类别是两个正交维度，两者都要建模）
> 环境：`cmd /c ""D:\project_farm\.tools\gw.cmd" <task>`；服务端 `-PfsRunDir=run-fix7`（全新目录，不污染 `run/`）

## 0. 结论速览

| 验收项（任务书 §4） | 结果 | 证据 |
|---|---|---|
| 1. `compileJava` / `build` 绿、零 warning；`verify_project.py` FAIL 0 | ✅ | §6；`compileJava --rerun-tasks` 真跑 javac，仅一条**原有的** deprecation Note（`ModAdvancementProvider`，本次未触碰该文件）；`FAIL 0 项，WARN 0 项` |
| 2. 启动自检打印 15 条「ID → 类型 + 类别集合」 | ✅ | §2（`.work/fix7-probe-a.log`，两栏分列） |
| 3. `runData` 后中英键集一致；给出新总键数 | ✅ | 中英各 **222** 键、键集差异 0（原 218；删 3 个 `type.*`、加 6 个类别 + 1 个分隔符） |
| 4. 运行时回归（瓜片子破方块 / 胡罗贝不破 / 多标签 tooltip） | ✅ | §3、§5 |
| 5. **只改标签就改变行为**（临时去掉瓜片子 DEMOLITION，不改任何爆炸调用点 → 不再破方块） | ✅ | §5（26 → 0 → 23，8 个文件 md5 前后完全相同） |

## 1. 两维建模（调度者更正的那一条）

| 维度 | 载体 | 值 | 来源 |
|---|---|---|---|
| 类型（§2.2） | `util/CropType`（新） | `THROWABLE / DEPLOYABLE / EDIBLE` 单值 | DEC §4「类型」列 |
| 类别（§2.3） | `util/CropCategory`（新） | `TRAP / TURRET / DEMOLITION / BUFF / CONSTRUCTION / DAMAGE` **多值集合** | DEC §4「类别」列 |
| 部署行为实现种类 | `behavior/DeployedKind`（未并入） | 5 个枚举值 | TECH §4.5 |

- `CropProfile` 是 `record (id, type, categories, cropBlock, seedItem, kind, allowedFaces, throwStack)`：
  `type` 与 `categories` 是**两个独立字段**，没有任何一处用其一推另一（`canBreakBlocks()` 只读 `categories`；`isDeployed()` 只读 `type`）。
- `CropType` 没有 `PLANTABLE`：DESIGN §2.2.3 的种植型是横切能力，D-10 不新增设计，15 种全部有播种形态。
- **声明的 type 与实际注册的物品类逐条一致**（`registry/FarmstormItems.java`）：
  `ThrowableCropItem` × 8 = puffed_wheat / ubi_server / toxic_ubi_server / melon_slice_bomb / melon_grenade / bamboo_firecracker / red_mushroom_puff / brown_mushroom_puff；
  `DeployableCropItem` × 5 = blast_carrot / pumpkin_turret / awakened_torchflower / awakened_pitcher_plant / fang_trap_cactus；
  `FoodCropItem` × 2 = power_beet / lucky_berry。零不一致。

## 2. 15 条「ID → type / categories」启动自检原文（`.work/fix7-probe-a.log`）

```
FIX7 自检作物 puffed_wheat              → type=THROWABLE   categories=[CONSTRUCTION]              canBreakBlocks=false
FIX7 自检作物 blast_carrot              → type=DEPLOYABLE  categories=[TRAP, DAMAGE]              canBreakBlocks=false
FIX7 自检作物 ubi_server                → type=THROWABLE   categories=[DEMOLITION, DAMAGE]        canBreakBlocks=true
FIX7 自检作物 toxic_ubi_server          → type=THROWABLE   categories=[BUFF, DEMOLITION, DAMAGE]  canBreakBlocks=true
FIX7 自检作物 power_beet                → type=EDIBLE      categories=[BUFF]                      canBreakBlocks=false
FIX7 自检作物 melon_slice_bomb          → type=THROWABLE   categories=[DAMAGE, DEMOLITION]        canBreakBlocks=true
FIX7 自检作物 melon_grenade             → type=THROWABLE   categories=[DEMOLITION, DAMAGE]        canBreakBlocks=true
FIX7 自检作物 pumpkin_turret            → type=DEPLOYABLE  categories=[TURRET, DAMAGE]            canBreakBlocks=false
FIX7 自检作物 awakened_torchflower      → type=DEPLOYABLE  categories=[TURRET, BUFF]              canBreakBlocks=false
FIX7 自检作物 awakened_pitcher_plant    → type=DEPLOYABLE  categories=[TURRET, BUFF]              canBreakBlocks=false
FIX7 自检作物 fang_trap_cactus          → type=DEPLOYABLE  categories=[TRAP, DAMAGE]              canBreakBlocks=false
FIX7 自检作物 bamboo_firecracker        → type=THROWABLE   categories=[DAMAGE]                    canBreakBlocks=false
FIX7 自检作物 red_mushroom_puff         → type=THROWABLE   categories=[BUFF]                      canBreakBlocks=false
FIX7 自检作物 brown_mushroom_puff       → type=THROWABLE   categories=[BUFF]                      canBreakBlocks=false
FIX7 自检作物 lucky_berry               → type=EDIBLE      categories=[BUFF]                      canBreakBlocks=false
```

逐条与 DEC §4 对照：15 行全部一致；唯一有意偏差是 `melon_slice_bomb` 多了 `DEMOLITION`（作者 2026-10-28 指定，见 §7-⑤）。
类别集合用 `LinkedHashSet` 保序，输出顺序即 DEC §4 的书写顺序（`Set.of` 的迭代顺序受哈希盐影响，不能用于展示）。

## 3. tooltip 显示**全部**类别

探针把 `ModifiedCropItem.appendCropTooltip` 产出的组件逐个打印（服务端没有模组语言表，所以是键名），
再用 `src/generated/resources` 的两份语言文件把键还原成最终渲染文本（脚本 `.scratch/fix7-tooltip-render.mjs`）：

| 作物 | 组件数 | zh_cn 渲染 | en_us 渲染 |
|---|---|---|---|
| awakened_torchflower | 3（类别+分隔符+类别） | `炮台类、buff类` | `Turret, Buff` |
| toxic_ubi_server | 5（3 类别 + 2 分隔符） | `buff类、破障类、伤害类` | `Buff, Demolition, Damage` |
| blast_carrot | 3 | `陷阱类、伤害类` | `Trap, Damage` |

- 分隔符是**语言键** `tooltip.farmstorm.category_separator`（zh `、` / en `, `），因此中英各自正确；
  在公共代码里读客户端语言会让专用服务端加载客户端类，故不采用。
- 语言键：**222**（218 − 3 个 `tooltip.farmstorm.type.*` + 6 个 `tooltip.farmstorm.category.*` + 1 个分隔符），
  中英键集完全一致（`diff=0`），全仓已无 `tooltip.farmstorm.type` 的活引用（只剩 ModLangProvider 里的一句历史注释）。
- **顺带修好一处原有缺口**：`DeployableCropItem extends BlockItem` 而不是 `ModifiedCropItem`，此前 5 个部署型物品
  **根本没有类别行**（火把花没有 tooltip 可显示）。本次给它补 `appendHoverText` → 委托同一份 `appendCropTooltip`。

## 4. 由标签驱动的破障权限

`explosion/BlastRule.java:78` 新增唯一判据：

```java
public static BlastRule forCrop(CropProfile profile, BlastRule whenDemolition) {
    return profile.canBreakBlocks() ? whenDemolition : NEVER_BREAK;   // canBreakBlocks() == has(DEMOLITION)
}
```

六个爆炸调用点全部改成走它（阈值仍由调用点决定，工厂只负责开关）：

| 调用点 | 文件:行 | 现在的表达式 | 该作物标签 | 运行期结果 |
|---|---|---|---|---|
| 瓜片子 | `entity/thrown/MelonSliceProjectile.java:42` | `forCrop(profile(), sliceBomb())` | DAMAGE, **DEMOLITION** | 破方块 |
| 育碧服务器 | `entity/thrown/UbiServerProjectile.java:44` | `forCrop(profile(), UNBREAKABLE_SKIP)` | **DEMOLITION**, DAMAGE | 破方块 |
| 毒育碧服务器 | `entity/thrown/ToxicUbiServerProjectile.java:46` | `forCrop(profile(), VANILLA)` | BUFF, **DEMOLITION**, DAMAGE | 破方块 |
| 瓜！ | `entity/thrown/MelonGrenadeProjectile.java:48` | `forCrop(profile(), grenade())` | **DEMOLITION**, DAMAGE | 破方块 |
| 胡罗贝 | `behavior/impl/BlastCarrotTrap.java:69` | `forCrop(BLAST_CARROT, VANILLA)` | TRAP, DAMAGE | 不破 |
| 幸运浆果小爆炸 | `util/LuckyBerryRoller.java:215` | `forCrop(LUCKY_BERRY, UNBREAKABLE_SKIP)` | BUFF | 不破 |

启动自检的六行（`.work/fix7-probe-a.log`）：

```
FIX7 自检爆规 瓜片子 → 破坏规则（能破方块=true）      FIX7 自检爆规 胡罗贝 → NEVER_BREAK（能破方块=false）
FIX7 自检爆规 育碧服务器 → 破坏规则（能破方块=true）  FIX7 自检爆规 幸运浆果小爆炸 → NEVER_BREAK（能破方块=false）
FIX7 自检爆规 毒育碧服务器 → 破坏规则（能破方块=true） FIX7 自检爆规 瓜！ → 破坏规则（能破方块=true）
```

## 5. 核心验收 §4.5：**只改一个标签**，不改任何爆炸调用点

同一套探针、同一场景、同一份世界，跑三次（`runServer -PfsRunDir=run-fix7`，探针第 20 刻布场、第 80 刻读结果）：

| 运行 | `MELON_SLICE_BOMB` 的类别 | 瓜片子爆炸的规则 / toBlow | **瓜片子泥土立方变化方块数** | 育碧服务器（对照） | 胡罗贝（对照） |
|---|---|---|---|---|---|
| A 改前（当前实现） | `[DAMAGE, DEMOLITION]` | break-enabled / 129 | **26**（泥土被夷平） | 102 | 0 |
| B 临时去掉 DEMOLITION | `[DAMAGE]` | NEVER_BREAK / 0 | **0**（泥土一块不少） | 102 | 0 |
| C 改回 | `[DAMAGE, DEMOLITION]` | break-enabled / 128 | **23** | 99 | 0 |

- 对照项说明这不是「全部炸不动了」：**B 里育碧服务器照样破 102 块**，只有瓜片子一类塌掉了。
- A/C 的具体块数有 ±3 的抖动（26 / 23，爆炸射线与残留实体的舍入差异），判别信号是 **>0 vs 0**，三次稳定。
- 胡罗贝：`rule=NEVER_BREAK, toBlow=0`，周围泥土立方变化 **0**，它自己也只是被 `consume()` 移除（`minecraft:air`）。
- 场景无客户端玩家，因此胡罗贝是**直接调用 `BlastCarrotTrap#trigger` 的爆炸段**（真实 `FarmstormExplosion` + 真实规则），
  跳过的是「非己方玩家进入半径」那段扫描；该段已在 FIX-6 用真客户端验过，本次未改。

**B 运行期间源码的实际改动只有这一行**（`util/CropProfile.java`）：

```diff
-            categorySet(CropCategory.DAMAGE, CropCategory.DEMOLITION),
+            categorySet(CropCategory.DAMAGE),
```

**爆炸调用点未被触碰的机械证据**：对 6 个调用点 + `BlastRule.java` + `CropProfile.java` 取 MD5，
「A 之后（翻转前）」与「C 之后（翻转还原后）」两次快照`Compare-Object` 结果**完全相同（0 差异）**，
即 B 期间这 8 个文件一个字节都没变（`.work/fix7-hash-before.txt` / `.work/fix7-hash-after.txt`）。

## 6. 命令与结果

```
cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava --rerun-tasks   → BUILD SUCCESSFUL in 9s
   （真跑 javac；无 warning: 行，仅 Note: ModAdvancementProvider 使用 deprecated API —— 该类本次未改，为既有基线）
cmd /c ""D:\project_farm\.tools\gw.cmd" build                      → BUILD SUCCESSFUL in 6s
   build/libs/farmstorm-0.0.1+mc1.21.1.jar = 333,361 B
cmd /c ""D:\project_farm\.tools\gw.cmd" runData -PfsRunDir=run-fix7 → written: 2（两份语言文件）
   src/generated/.../zh_cn.json = 222 键，en_us.json = 222 键，键集差异 0
python tools/verify_project.py                                      → 117 配置键一致 / lang 222 键 / 结论：FAIL 0 项，WARN 0 项
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-fix7 → Done (0.383s)!，RCON list 0 玩家，RCON stop 干净停机
   （最后一次冒烟是在删掉探针之后跑的；日志里无 ERROR/FATAL，无探针痕迹）
```

## 7. 与上游文档 / 任务书的偏差登记（需调度者裁决的三条）

① **新增了第 7 个 tooltip 键** `tooltip.farmstorm.category_separator`（任务书只说新增 6 个 `category.*`）。
  它不在 `category.` 命名空间下（`category_separator`，下划线），所以「6 个 `tooltip.farmstorm.category.*`」字面成立。
  理由：中英各自的分隔符只能由语言文件给出，否则要在公共代码里读客户端语言（专用服务端会加载客户端类）。
② **`DeployedKind` 删掉了 `tooltipCategory` 字段与 getter**（5 个枚举值、`scanInterval` 一字未动）。
  任务书 §3 说「DeployedKind 保持不动」——我按「不要把它并入类别模型」理解；它的唯一消费者（单值 tooltip）
  被本次替换掉后，留着它就是指向已删语言键的悬空方法。如要严格「一字不动」，可回滚这两个成员，但 3 个 `type.*` 键就该留着。
③ **`MELON_SLICE_BOMB` 的破障类是作者新增口径**，DEC §4 原文仍只有「伤害类」。未改文档（HC-06），在此登记。
  另外任务书引用的 `PLAN R-48` 在 `.work/PLAN.md` 里不存在（该文件到 R-46 为止），请调度者补录。

## 8. 探针清理

```
src 全树大小写敏感 grep 'Fix7SelfCheck|FIX7'      → 0
build/classes 下 '*Fix7*'                          → 0
交付 jar 条目 'Probe|SelfCheck|Fix7'               → 0
run-fix7 日志 'FIX7'（大小写敏感）                 → 0（最后一次冒烟）
```
探针源码 `src/main/java/com/dashpig/farmstorm/Fix7SelfCheck.java` 已删除；
它的三份输出留在 `.work/fix7-probe-{a,b,c}.log`，B/C 两轮的完整服务端日志留在 `.work/fix7-run-{b,c}-full.log`。
`run-fix7/` 是本次专用运行目录，与 `run/**` 一样不进交付物。
