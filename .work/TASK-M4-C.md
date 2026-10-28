# 任务书 M4-C · 转换器事务与界面（Phase 6：F-34 ~ F-39）

> 先读 `.work/PLAN.md` §0/§4/§6。前置：M1-A 已交付（`ConverterBlock` / `ConverterBlockEntity` / `ConverterMenu` / `ConverterRecipe` + Serializer 的类型与注册都在）。

## 1. 目标

把转换器从「能放下的单槽方块」变成「原版 ↔ 改良 的完整闭环」：配方匹配、事务化 `tryConvert`、菜单数据同步与按钮。

## 2. 必读
1. `docs/zh/02-TECH-SPEC.md` §4.8（四件套全节，含 JSON schema 与 `tryConvert` 的校验顺序）、§8.3（防刷）。
2. `docs/zh/01-DESIGN.md` §3.1（转换器：结构 / 结算顺序伪代码 / 关键约束）。
3. `docs/zh/00-DECISIONS.md`：D-11、D-12、D-24。

## 3. 交付物（只动这四个文件所属的包）
`recipe/ConverterRecipe.java`、`recipe/ConverterRecipeSerializer.java`、`blockentity/ConverterBlockEntity.java`、`menu/ConverterMenu.java`、`block/ConverterBlock.java`。

### 3.1 `ConverterRecipe`
- 按 TECH §4.8.5 的 7 个字段与 `matches` / `assemble` / `getResultItem` / `getSerializer` / `getType`。
- `reverse()` 返回 input/result 互换的**运行时视图**，不落盘。
- `Ingredient.CODEC` 的 JSON 形态以实际 API 为准。

### 3.2 `ConverterRecipeSerializer`
- `MapCodec` 7 字段按 schema；`streamCodec()` 必须真实实现（M1 已手写，核对字段顺序与 codec 一致）。

### 3.3 `ConverterBlockEntity#tryConvert`
- **校验顺序即安全性**，严格按 TECH §4.8.2 的 6 步：空槽 → 正向配方 → 经验（不足直接拒绝、**绝不扣等级**）→ 催化剂 → 才动状态 → `setChanged` + `sendBlockUpdated`。
- 反向匹配：`getAllRecipesFor` 遍历比对 `result`，结果缓存到 `cachedRecipe`；缓存必须能被 `RecipeManager` 重载失效（RT-07：`RecipesUpdatedEvent` 清空 + `tryConvert` 内 `byKey` 校验）。
- 催化剂不消耗主手物品本身。

### 3.4 `ConverterMenu`
- 槽位 0 输入 / 槽位 1 只读预览；`ContainerData` = `[xpCost, canConvert, reverse]`。
- `clickMenuButton(player, id)` 只接受 `BUTTON_CONVERT = 0`。
- `quickMoveStack` 只处理输入槽与玩家背包。

### 3.5 `ConverterBlock`
- `useWithoutItem` → `openMenu`；`onRemove` → `Containers.dropContents`；`getRenderShape` = MODEL；`strength(3.5F)` + `requiresCorrectToolForDrops()`。

## 4. 不做
- 不写 `data/farmstorm/recipe/*.json`（M5 的 DataGen 生成）。
- 不改 `registry/**`。
- 不做 GUI 像素（M5）。

## 5. 验收
1. `gw.cmd compileJava` / `build` 绿；`runServer` 出现 `Done (`。
2. 事务性证据：走查 `tryConvert` 的 6 步顺序，写清「哪一行保证不扣等级」；连点 10 次空转不改变物品与经验（用日志或命令验证，并写清验证方式）。
3. `/reload` 后缓存失效路径可指认（`RecipesUpdatedEvent` 的监听位置 + `byKey` 校验行）。
4. `grep` 证据：`ConverterBlockEntity` 内不得出现任何客户端判定分支。

## 6. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外写清 `tryConvert` 的 6 步对应行号、以及给 M5 的配方 JSON 字段约定。
