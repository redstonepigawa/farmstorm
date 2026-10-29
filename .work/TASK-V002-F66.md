# 任务书 V002-F-66 · 2 个投掷物骨架、2 个 entity_types、渲染器 11/11、注册对象 77

## 目标（`13-TASKS-0.0.2.md` §3 F-66，经 V-05 追加）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 **F-64（`d9ed6ec`）与 F-65（`ab33fff`）**，两者都已完成
- 这是 Phase 11 的收口任务：本任务完成后，`entity_types` 到 11、`mob_effects` 到 2、`sound_events` 到 2，注册对象总数 **64 → 77** 才完整。

## 交付物

| # | 文件 | 内容 |
|---|---|---|
| 1 | `entity/thrown/MolotovProjectile.java`（新建） | 骨架：`profile()` 返回静态常量、`onImpact` 只接「每次生成只结算一次」的钩子。**行为留空**——落点纵火/岩浆在 F-69/F-70 |
| 2 | `entity/thrown/CompressedPuffedWheatProjectile.java`（新建） | 同形骨架；落点写入在 F-68 |
| 3 | `registry/FarmstormEntityTypes.java`（+2） | 两个 `projectile(...)`，**参数与既有 8 个逐字一致**（`MobCategory.MISC` / `sized(0.25F, 0.25F)` / `clientTrackingRange(4)` / `updateInterval(10)`） |
| 4 | `client/FarmstormClient.java` | 2 个新 `ThrownItemRenderer`；`ALL_ENTITY_TYPES` 从 9 条补到 **11** 条 |
| 5 | `Farmstorm.java` | 新增「注册对象 77」自检行（文案见下） |
| 6 | **【V-05 收口】** `registry/FarmstormItems.java` | 把 `molotov` / `compressed_puffed_wheat` 的 entity type supplier 从「懒解析」改成 **`FarmstormEntityTypes.MOLOTOV::get` / `COMPRESSED_PUFFED_WHEAT::get` 直引**，并**删掉** `projectile(profile)` 辅助方法与那处 `@SuppressWarnings("unchecked")`（既有 8 个投掷物就是这个写法，统一掉） |

## 自检文案（逐字，别改标点）

```
自检 注册对象 77 个：items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1 → OK
```

照既有三条自检（chebyshev / euclidean / 权重和 / 作物 / 浆果池）的写法：**一条 INFO，数字对不上就打印 FAIL**。数法要**真的去数**（各 `DeferredRegister` 的条目数或等价来源），不要写死常量再断言常量等于常量。

## 验收

1. 上面那行自检在专用服务器启动日志里**逐字**出现且为 `OK`。
2. **客户端**日志出现 `自检 实体渲染器 11/11 → OK`，紧随一行列出 11 个实体 ID，含 `farmstorm:molotov` 与 `farmstorm:compressed_puffed_wheat`。
3. `/summon farmstorm:molotov ~ ~2 ~` 与 `/summon farmstorm:compressed_puffed_wheat ~ ~2 ~` 各返回成功；实体落地后周围方块 `/execute if block` 全部不变（骨架版不接行为）。
4. V-05 收口后：`FarmstormItems` 里不再有 `BuiltInRegistries.ENTITY_TYPE` 与 `@SuppressWarnings`；两个物品的 supplier 都是 `FarmstormEntityTypes.X::get`。

硬门禁：`build -PfsRunDir=run-f66` exit 0、零 warning；`自检 作物 19 种 … → OK` 与 `自检 幸运浆果池 16 种 … → OK` 仍在。

## 客户端验收怎么做（别放弃这条）

这条是「客户端证据」类验收，**不接受"代码看起来对"**。做法参考 0.0.1 的既有手法：
- `cmd /c ""D:\project_farm\.tools\gw.cmd" runClient -PfsRunDir=run-f66`，渲染器自检应在**客户端初始化阶段**就打印（不需要进世界），所以起客户端 → 抓 `logs/latest.log` 里那两行 → 关掉即可。
- 如果客户端起不来（图形环境/驱动问题），**如实写"客户端未验证"并连同失败日志一起报**，不要用静态分析冒充。

## 边界

- 只许动上表 6 个文件。**不许**动 `FarmstormConfig.java`（F-61）、`datagen/*`、`docs/`。
- **不许**给两个投掷物实现任何落点行为（F-68/F-69/F-70）。
- `verify_project.py` 是子集检查，它绿**不能**当你新增对象有效的证据（V-07）。

## 回报

- 「注册对象 77」与「渲染器 11/11」两行的**原文**。
- 三条 summon 与 `if block` 的返回原文。
- V-05 收口前后 `FarmstormItems` 的 diff 片段。
- `git diff --stat`、`build` 最后两行、客户端日志抓取方式与原文。
- **不要 commit**。
