# 任务书 V002-F-69 · 莫洛托夫落点触发器

## 目标（`13-TASKS-0.0.2.md` §3 F-69）

- 复杂度 M ｜ 1.5 人日 ｜ 依赖 F-66（已 `55cd6bb`）
- 一句话：莫洛托夫落地时在落点做一次**无伤害、不破坏方块**的爆炸（当前默认威力 **0**，所以表现上"什么都不该发生"），并把落点与威力打进日志。

## 现状（已核对）

`entity/thrown/MolotovProjectile.java` 现在只有 `profile()`，**不重写 `onImpact`** → 落地只走基类 `settle` 的一次性结算，除了实体消失什么都不做。落点纵火/岩浆块归 **F-70**，本轮不做。

## 交付物（只动这一个文件）

在 `MolotovProjectile` 里：

1. 重写 `onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit)`，在其内部调用新增的 `trigger(Vec3)`。
2. `trigger(Vec3)` 做**一次**：
   ```
   new FarmstormExplosion(level, owner, FarmstormDamageTypes.cropBlast(level, owner),
           x, y, z, FarmstormConfig.molotovTriggerExplosionPower(), BlastRule.NEVER_BREAK, 0F).detonate()
   ```
   三个要点：**威力读 `molotov.triggerExplosionPower()`（默认 0）**、**`BlastRule.NEVER_BREAK`**（永不破方块）、**伤害上限 `0F`**（造成 0 伤害）。
3. 打印一行日志，**格式逐字**：
   ```
   莫洛托夫 落点 (x, y, z) 威力 {P}
   ```
   （x/y/z 用实际坐标，保留合理位数；P 是本次读到的威力值）

## 验收（四条）

1. **无伤害**：平地投一颗，`/data get entity @p Health` 投前投后**相同**（`damageCap = 0F`）。
2. **不破坏方块**：以落点为心 3×3×3 逐格 `/execute if block` 与投前**逐一对齐**，无任何方块变化（`NEVER_BREAK`）。
3. **日志**：出现 `莫洛托夫 落点 (x, y, z) 威力 0`，其中 0 与 `molotov.triggerExplosionPower` 默认值一致。
4. **键确实驱动威力**（**原写法与裁决 A-10 冲突，已修正**）：把 `molotov.triggerExplosionPower` 改成 `4` → 重投 → **爆心内的实体会出现原版击退**（`Motion` 由 `[0,0,0]` 变为非零），而**方块破坏仍为 0、血量仍不变**；改回 `0` → 击退消失、`Motion` 回到 `[0,0,0]`。

> **修正说明**：本条原先写「威力 4 → 出现方块破坏」，这与 A-10 直接矛盾——`v002-BRIEF.md` §9.2 原文：「默认 0 时零副作用；调大后只增加对实体伤害，**永远不破坏方块**，破坏权限与威力无关」。莫洛托夫没有破障标签，`BlastRule` 恒为 `NEVER_BREAK`，所以威力 4 下**必然 0 破坏**。
>
> 修正后仍然能证明「这个键被读到」：默认 0 时什么都不发生，写成空实现也能过 1~3；而**威力 4 时出现击退、0 时击退消失**，只有真正把值传进 `FarmstormExplosion` 的 radius 才会有这个差异。
> 可选补强（本轮代理做了）：临时构建把规则换成 `BlastRule.of(power, false)` 验证破坏量 107 vs 0，**跑完必须回退并重建**，`javap` 复核产物里是 `NEVER_BREAK`。

## 环境纪律（前两轮踩过的坑，直接照做）

- **`/forceload add` 用方块坐标**（`forceload add 96 96 104 104`），不是区块坐标——写 `6 6` 只会标记区块 `[0,0]`，测试区不加载，统计全是 0（PLAN V-16）。
- **脚本开头必须有清场基线**：`kill @e[type=item]` 等三连 + 静置 + 「目标位置方块 = 期望值」的断言；不成立就整轮作废、必要时删 `run-f69/world` 全新重跑（PLAN V-17：冻结实体会随区块加载复活污染复跑）。
- **证据写进 `.work/`**，不要只留 `run-*/logs/`（会轮转丢失）。
- 跑完服务端**当场** `Get-Process java | Stop-Process -Force`，再删 `.tools/runserver.lock`。
- 用 `-PfsRunDir=run-f69`；`python` 用 `C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe -X utf8`。

## 边界

- 只许动 `entity/thrown/MolotovProjectile.java`。
- **不许**做落点纵火、岩浆块生成（F-70），**不许**动 config / datagen / docs。
- **不许**新增网络包、不许绕过 `FarmstormExplosion` 自己造 `Explosion`（0 包与服务端权威是红线）。

## 回报

- 四条验收的**实际命令与返回原文**（尤其验收 4 的前后对照：破坏了多少格 / 哪些格）。
- `trigger` 与 `onImpact` 的代码片段。
- `git diff --stat`、`build` 最后两行、`Probe` 残留计数（注意：`LANDING_PROBE` 是既有常量，不算探针）。
- **不要 commit**。
