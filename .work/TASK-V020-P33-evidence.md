# Phase 33（F-216 ~ F-219）验收证据 — 调度者复核

> 情况说明：本 Phase 的执行子代理在写完代码、`compileJava` exit 0 之后**被网络故障打断**，未产出回报与证据文件。
> 工作树里的 5 个文件改动经逐段读 diff 确认**四个任务都已落地**；下面全部读数为**调度者亲自复跑**所得。

## 结论

四个任务全部落地：浆果池按 `tier() == COMMON` 推导为 **11** 条且断言与日志同改（F-216）；`isPlantableSoil` 恒 `false`、原判据留成注释（F-217）；`SeedItem#useOn` 返回 `PASS`（F-218）；`ConverterBlock.useWithoutItem` 返回 `PASS`、菜单链与掉落保留（F-219）。
注册面仍 **93**、`menu_types 1`、toml 仍 **58 段**、`registry/` / `config/` / `data/` / `docs/` 零 diff。

## 证据

### 构建

```
cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava"  → exit 0
cmd /c ""D:\project_farm\.tools\gw.cmd" build"        → exit 0
```

### F-216 · 幸运浆果池 11

| 判据 | 读数 |
|---|---|
| 池构造式 | `CropProfile.java:150` `.filter(profile -> profile.tier() == CropTier.COMMON)` |
| 池段内第二份 id 清单 | `grep -c "CropProfile\."` 于 142~150 行 = **0** |
| `== 19` 残留 | `Farmstorm.java` **0** 处 |
| 三个 `!=` 另一半 | 仍在（`noneMatch(... LUCKY_BERRY / WILD_APPLE / COMPRESSED_PUFFED_WHEAT)`） |
| 断言值 | `poolSize == 11` |

起服实测（`run-p33\logs\latest.log`，`Done` 之后 52 秒）：

```
自检 幸运浆果池 11 种（按 tier() 只留 COMMON，不含 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK
```

### F-217 · 种植路径①

| 判据 | 读数 |
|---|---|
| `isPlantableSoil` 可执行体 | `return false`；原 `instanceof FarmBlock` 判据只存在于**注释块**（`:120`） |
| 可执行体里 `instanceof FarmBlock` | **0**（`:56` 那处在 `canPlantAt`，按 §1 边界**不删**） |
| 调用点 | `isPlantableSoil` 命中 2 处 = 定义 + `:85` 调用点，调用点未改 |
| `canPlantAt` 死代码 | 仍在（1 处定义），未删 |

### F-218 · 种植路径②

| 判据 | 读数 |
|---|---|
| `InteractionResult.PASS` | `SeedItem.java` **1** 处 |
| `useOn` 方法 | **1** 个（`:42`）；另一处命中是 import 的 `UseOnContext` |
| `appendHoverText`（tooltip） | 仍在 **1** 处 |

### F-219 · 转换器

| 判据 | 读数 |
|---|---|
| `InteractionResult.PASS` | `ConverterBlock.java` **1** 处（`useWithoutItem` 方法体整体替换） |
| `getMenuProvider` | 仍在（菜单链保留） |
| `FarmstormMenus` 的 `REGISTER.register` | **1** 处 |
| 配方计数（两个数分开） | 全量 **29** = `converting` **26** + `crafting_shaped` **2** + `crafting_shapeless` **1** |

### 负向面

| 面 | diff 行数 |
|---|---|
| `src/main/java/.../registry/` | **0** |
| `src/main/java/.../config/` | **0** |
| `src/generated/resources/data/` | **0** |
| `src/main/java/.../client/`（ConverterScreen / FarmstormMenus 等） | **0** |
| `docs/` | **0** |
| `run/config/farmstorm-server.toml` 段数 | **58**（未动） |

起服自检（同一次 `run-p33`）：

```
自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK
自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK
自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / … / menu_types 1 → OK
自检 网络包注册 farmstorm:core_list（版本 1，S2C 只读，MAX_CORES 64）→ OK
```

## 未验证（本机无图形客户端，需真人）

1. **15 个作物物品右键耕地**的实际反应（方块不出现 / 手持物不减 / 无提示）——代码层已证判据恒 `false` 且调用点未改。
2. **7 个种子右键耕地**：同上，代码层已证 `useOn` 返回 `PASS`。
3. **7 个部署型物品右键方块顶面照常摆出**（B-02，确认「cut 的是种作物不是摆方块」）——代码层已证只改了 `isPlantableSoil`，`DeployableCropItem#useOn` 未触碰。
4. **吃 `lucky_berry` 的抽样记录**（抽 N 次产出全落 11 条普通集合内）——需客户端喂食；池构造的代码层证据已给。
5. **空手右键转换器**的实际观感（无界面 / 无提示）——代码层已证返回 `PASS`、`getMenuProvider` 保留。

## 文档-源码偏差

1. **37 号 F-213 易漏点③「两支脚本的 banner 不写版本串」是文档记错**——它们在 v0.1.1 期就带 `Farmstorm v0.1.1`。已在 F-213 的提交里一并改成 `v0.2.0`（见 Phase 32 证据）。
2. 本 Phase 未发现新的文档-源码冲突。

## 未关闭的问题

无。执行子代理被网络打断，本文件由调度者按自己的读数补写；四个任务的改动本身完整、可编译、可起服。
