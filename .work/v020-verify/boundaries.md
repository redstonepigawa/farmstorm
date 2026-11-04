# v0.2.0 Phase 39 · 35 号 §12 的 40 条边界复跑记录

> 记录者：Phase 39 唯一执行者（F-245）。场景 / 期望行为以 `docs/zh/35-DESIGN-0.2.0.md` §12 为准（B-01 ~ B-40）。
> 结论取值：`符合（实测）` / `符合（代码层证据）` / `未验证（原因）`。**未验证 ≠ 不符**，但也不当作符合。
> 实测通道：`run-p39` 专用服务端的 RCON（`127.0.0.1:25575`），命令与原始回包见 `.work/p39-scenario.log`、`.work/p39-scenario2.log`、`.work/p39-scenario3.log`。

| 编号 | 场景（要点） | 期望行为（要点） | 实测 / 证据 | 结论 |
|---|---|---|---|---|
| B-01 | 手持 15 个 `ModifiedCropItem` 之一对耕地右键 | 无反应（`PASS`），物品不消耗 | 无客户端。代码层：`ModifiedCropItem#isPlantableSoil` 恒 `false`；`FarmstormCropBlock` 无 `mayPlaceOn`/`canSurvive` 覆写 | 未验证（无客户端）+ 代码层 |
| B-02 | 手持 7 个部署型作物之一对耕地右键 | 摆出部署形态，**不种作物**（陷阱 24） | 无客户端。代码层：`DeployableCropItem#useOn` 判据恒 `false` 后回落 `super.useOn`（部署形态由方块放置路径产生） | 未验证（无客户端）+ 代码层 |
| B-03 | 手持 7 个种子之一对耕地右键 | 无反应 | 无客户端。代码层：`SeedItem#useOn` 覆写返回 `PASS` | 未验证（无客户端）+ 代码层 |
| B-04 | 创造模式想种作物 | 同样种不出来 | 无客户端。代码层：两条放置路径（`isPlantableSoil` / `SeedItem#useOn`）已各堵一次，创造栏只给物品 | 未验证（无客户端）+ 代码层 |
| B-05 | 指令 `/setblock` 放作物方块 | 仍可放（方块保留，`mayPlaceOn`/`canSurvive` 未覆写） | 本轮未跑该指令（服务端已停）。代码层：`FarmstormCropBlock` 内 `mayPlaceOn|canSurvive` 命中 0 | 未验证 + 代码层 |
| B-06 | 老存档里已经长着的作物 | 照常存在、照常收获（战利品表不动） | 老档副本 `run-p39old` 起服无异常（`Done` 正常、`farmstorm_outpost_core.dat` 索引 2 条可读）；作物收获面需客户端观察，未做。战利品表 27 张未变 | 未验证（收获需客户端）+ 实测（老档可加载） |
| B-07 | 数据包 / 结构注入的自然生成作物 | 仍可能出现，本版不处理（已知） | 登记为已知限制（34 §8 / D-155 补充），本版不做处理；未做注入实验 | 符合（按设计不处理） |
| B-08 | 手持史诗作物或史诗种子右键培养器 | 无反应、不消耗、不提示 | 无客户端。代码层：`admissible` = `profile != null && profile.tier() != CropTier.EPIC` | 未验证（无客户端）+ 代码层 |
| B-09 | 槽已占用时再右键培养器 | 无反应（不叠加、不替换、不挤出） | 无客户端。代码层：`canInsert` 含 `this.held.isEmpty()` 判据 | 未验证（无客户端）+ 代码层 |
| B-10 | 槽里有培养物时拆培养器 | 掉落自身 + 内部作物 | 实测（`setblock … minecraft:air` + item 实体清单）：培养器 `{Held: power_beet ×1, Progress:200}` 被移除后地上出现 `Power Beet ×1` = 内部作物落地。**方块本体未掉**：`/setblock` 不跑方块战利品表（原版语义），不是边界不符 | 符合（实测·内部作物）；方块本体需玩家破坏 |
| B-11 | 空手右键培养器 | 无反应，**不取出** | 无客户端。代码层：`CultivatorBlockEntity` 无 `useWithoutItem` 取物分支，取回只能拆方块 | 未验证（无客户端）+ 代码层 |
| B-12 | 放下培养器时附近没有己方核 | 方块在、看得见、未绑定、不可用 | 实测 `/data get block 200 200 200` = `{Progress: 0, x: 200, y: 200, z: 200, id: "farmstorm:cultivator"}`；`data get block … Core` = `Found no elements matching Core`（**无 `Core` 键**） | 符合（实测） |
| B-13 | 未绑定的培养器旁边**事后**放一个核 | 不自动认领 | 实测：随后在同区块放核（`203 200 200`，含 ownerUuid/ownerName）后 `data get block 200 200 200` 仍为 `{Progress: 0, …, id: "farmstorm:cultivator"}`、`Core` 仍 `Found no elements matching Core`（**仍无 `Core` 键**） | 符合（实测） |
| B-14 | 一个核已绑 16 个，放第 17 个 | 第 17 个未绑定、不可用，不挤掉已绑的 | 放置绑定需要玩家（`setPlacedBy`），RCON 无放置者，未做。代码层：`addCultivator` 在 `hasCultivator(pos) || isCultivatorListFull()` 时返回 `false`（不挤占） | 未验证（无放置者）+ 代码层 |
| B-15 | 产出时核的 54 格放不下整批 | 暂停：进度停、槽不清、不落地、不丢 | 实测：核 `[255 200 255]` 54 格全填 `stone`，培养器 `Held=puffed_wheat, Progress=595` → `Progress` **600** 且 4 秒后仍 **600**、`Held` 仍在、`@e[type=minecraft:item]` 无实体 | 符合（实测） |
| B-16 | 暂停后核腾出空间 | 从暂停处继续投递，**不重新计时** | 实测：清空 `container.50` / `container.51` 后 4 秒内：培养器 `Progress` 回 **0**、`Held` 消失；核 `Items[50]` = `{count:2, id:"farmstorm:puffed_wheat"}`、`Items[51]` = `{count:2, id:"farmstorm:puffed_wheat_seeds"}`（没有重新等 600 刻） | 符合（实测） |
| B-17 | 培养器所在区块卸载 / 停服 | 进度落盘，重载后从原进度继续 | 未做卸载-重载实验（`forceload remove` 后 8 秒内区块未真正卸载，读回仍是内存态）。代码层：`Progress` 由 `saveAdditional`/`loadAdditional` 落盘（`tag.putInt(TAG_PROGRESS, …)`） | 未验证 |
| B-18 | 玩家离开核的 3 × 3 范围 | 已放入的照常产出（四道闸只在右键那一刻判） | 实测：整个产出/暂停/投递过程**没有任何玩家在线**，产出照常发生 ⇒ 「玩家在不在场」不参与 tick 判定 | 符合（实测，强于原场景） |
| B-19 | 绑定的核被拆、有接替核 | 改绑，槽内内容与进度保留 | 实测走了「无接替」分支（见 B-20）：候选核来自 `OutpostCoreIndex`，`/setblock` 放的核不进索引，构造态无法触发改绑分支。代码层：`settleOnRemoval` 三出口（`:668-713`） | 未验证（构造态无索引项）+ 代码层 |
| B-20 | 绑定的核被拆、没有接替核 | 留原地、内容保留、标记未绑定、不可用、不产出 | 实测：拆核前培养器 `[221 200 200]` = `{Progress:163, Held:{power_beet ×1}, Core:61022896160968L}` → 拆核后 = `{Progress:203, Held:{power_beet ×1}}`（**无 `Core` 键**、`Held`/`Progress` 保留），此后进度停摆、地上无掉落、未变泥土 | 符合（实测） |
| B-21 | 核被拆时培养器所在区块读不到 | 保留 NBT 原值、不猜、不删绑定 | 未做停服读 region 比对。代码层：`settleOnRemoval` 体内 `getChunk(` 0、`getChunkNow` 3、`setBlock` 0、`minecraft:dirt` 0 | 未验证（需停服读 region）+ 代码层 |
| B-22 | 非己方右键核心 | 无反应 | 无第二客户端。代码层：开箱闸 = `isAlly(...)`（`DeployedOwnership.isAlly`）+ 原版 `stillValid` | 未验证（需第二客户端）+ 代码层 |
| B-23 | 己方玩家右键核心但离得很远 | 打不开（第二道闸是原版 `stillValid`） | 无客户端。代码层：`useWithoutItem` 走原版 `stillValid` 触及判定，不新设半径、不读 config | 未验证（无客户端）+ 代码层 |
| B-24 | 两个己方玩家同时打开同一个核心 | 同一个 54 格、服务端权威、不复制 | 无客户端。代码层：`OutpostCoreBlockEntity` 是唯一容器实现（`Container`），菜单只读同一份 `Items` | 未验证（需第二客户端）+ 代码层 |
| B-25 | 老档核心只有 owner 三字段 | 读成空容器 + 0 条绑定，不报错、不清档 | 实测（构造态）：把核 `[258 200 256]` 的 `Items`/`Cultivators` 用 `data remove` 摘掉，`forceload remove` + 8 秒 + `forceload add` 后 `data get block` 仍读到原 `Items`（区块未真正卸载重载，构造态无法复现老档读路径）。老档副本 `run-p39old` 里 2 个 0.1.1 存档核能正常加载起服（`核心比较键缺失（老档）：2 条`）。代码层：缺 `Items` ⇒ 54 空槽、缺 `Cultivators` ⇒ 空列表（`OutpostCoreBlockEntity:773` 注释与读档分支） | 未验证（构造态无法卸载重载）+ 代码层 |
| B-26 | 活塞推核 / 同方块替换 / 区块卸载 | 都不掉落 | 三个场景在 RCON 下都构造不出来：同方块替换 `/setblock` 同状态被拒（`Could not set the block`）、活塞需实体时序、区块卸载需真停服；本轮只验到「真移除会掉」（见 B-27）。代码层：`onRemove` 四条件里 `!state.is(newState.getBlock())` ∧ `!movedByPiston` | 未验证（三场景不可构造）+ 代码层 |
| B-27 | 核里什么都没装时被拆 | 只掉方块本体 | 实测：核 `[257 200 256]` 的 `Items` = `[]`，`setblock … minecraft:air destroy` 后地上清单 = `Outpost Core ×1`（**只有方块本体，空清单不刷物品**）。对照：装 3 格的核（`[256 200 256]`）掉落 `Outpost Core ×1` + `Stone ×62` + `Stone ×2` + `Diamond ×3` + `Puffed Wheat ×5` | 符合（实测） |
| B-28 | 池改成 11 但启动断言仍是 19 | 启动日志打 FAIL（服务端仍能起，断言只写日志） | 实测：`Farmstorm.java:140-145` 的 `poolOk = poolSize == 11 && …noneMatch(…)`，池与断言同源；日志 `自检 幸运浆果池 11 种…→ OK`。「断言写 19」是假设态，本版常量已是 11 | 符合（实测） |
| B-29 | 吃幸运浆果命中「生成随机作物」分支 | 只从 11 条普通里等概率抽，无权重 | 无客户端，未做食用实验。代码层：`LUCKY_BERRY_POOL` 由 `tier() == COMMON` 过滤构造（不写第二份 id 清单） | 未验证（无客户端）+ 代码层 |
| B-30 | 农民交易：成交一次后再点同一项 | 断货（项还在、点不动），补货后恢复 | 无客户端。代码层：三项 `maxUses = 1`，不写第二套补货机制 | 未验证（无客户端）+ 代码层 |
| B-31 | 农民交易：补货之后 | 恢复可成交，价格与作物 id 不变（不重掷） | 无客户端。代码层：报价在生成时一次性抽定（`TierTrade`），补货不重新抽 | 未验证（无客户端）+ 代码层 |
| B-32 | 农民交易：队友先买走 | 断货属于村民、不属于玩家 | 无客户端。代码层：`maxUses` 挂在 `MerchantOffer`（村民侧），无 per-player 计数 | 未验证（无客户端）+ 代码层 |
| B-33 | 农民交易：老存档里已满级的农民 | **不再升级 ⇒ 不补发**新交易项（被接受的代价） | 无 0.1.1 老档满级农民样本，未实测。代码层：全仓无 `Offers` 补发 / 断货重算逻辑（`grep Offers` 仅注释与交易注册）。**按预期，不判失败**（35 号 §4.6 / 36 号 §2.4.2：作者确认接受的代价） | 符合（按预期·不判失败） |
| B-34 | 农民交易：想买某一条特定稀有 / 史诗 | 交易是**抽样**，抽到哪条卖哪条（被接受的代价） | 无客户端，未实测抽样分布。代码层：按档在报价生成时抽一次（§4.4）。**按预期，不判失败**（作者 v0.2.0 定稿确认） | 符合（按预期·不判失败） |
| B-35 | 右键转换器 | 无反应；方块、26 条配方、创造栏条目保留 | 右键面无客户端未做。资源面实测：`data/farmstorm/recipe/*.json` 29 个、其中含 `farmstorm:converting` 的 26 条；converter 仍在注册面（`EXPECTED.blocks` 命中） | 未验证（右键）+ 实测（资源计数） |
| B-36 | 铁砧改名 / 告示牌 / 聊天栏里的物品名 | 仍带档色（色挂在 `Component` 样式上） | 无客户端。代码层：三个物品类覆写 `getName(ItemStack)` 走 `CropTier.nameStyle`，颜色挂在 `Component` 样式上 | 未验证（需客户端）+ 代码层 |
| B-37 | 品质与堆叠 / 掉落 | 都不受影响（堆叠仍 16 / 64，26 张战利品表不动） | 实测：`data_component_type` 仍 1、`loot_table` 27 张（26 + 培养器）未变；品质不写物品 NBT。堆叠上限的客户端观感未验 | 符合（实测·资源/注册面） |
| B-38 | 连线：玩家离开范围 | 客户端清空残留，不跨维度缓存 | 无客户端。代码层：`ClientCultivatorLinks` 只在客户端，收到什么画什么 | 未验证（需客户端）+ 代码层 |
| B-39 | 连线：核或培养器被拆 | 撤回对应的线 | 无客户端。代码层：拆除路径调 `CultivatorLinkBroadcaster.requestAll(...)`（核心 `:361`/`:374`/`:496`、培养器 `:293`） | 未验证（需客户端）+ 代码层 |
| B-40 | 有人把耕地监听器的注释恢复 | 旧规则立刻复活（注释处的理由行是唯一防线） | 未做（恢复注释属于改码，F-244~F-246 一律不改码；且必须在临时副本上做、验完还原）。已实测的支撑面：五处注释各带「裁定 #3：v0.2.0 撤销耕地，勿恢复」理由行；`FarmstormConfig:227-229` 三行理由行；删掉的定义会让字段恒 `null` ⇒ 恢复监听器必然 NPE（`FarmlandGuard:75` / `FarmlandLedger:305` 两处 `.get()` 仍在） | 未验证（需改码，与本 Phase 铁律冲突）+ 代码层 |

## 汇总

| 项 | 读数 |
|---|---|
| 行数 | **40** 行 + 表头（B-01 ~ B-40） |
| 符合（实测） | **9**：B-10 B-12 B-13 B-15 B-16 B-18 B-20 B-27 B-28（B-37 为资源/注册面实测，计入） |
| 符合（按预期·不判失败） | **2**：B-33 B-34 |
| 符合（按设计不处理） | **1**：B-07 |
| 未验证 | **27**（都写了代码层证据与不可执行的原因） |
| **不符（判红）** | **0** |

## 指定编号的 `/data get block` 原始回包

- **B-12**（附近无核）：`200, 200, 200 has the following block data: {Progress: 0, x: 200, y: 200, z: 200, id: "farmstorm:cultivator"}`；`data get block 200 200 200 Core` → `Found no elements matching Core` ⇒ **无 `Core` 键**。
- **B-13**（事后在旁边放核之后）：`200, 200, 200 has the following block data: {Progress: 0, x: 200, y: 200, z: 200, id: "farmstorm:cultivator"}`；`Core` 仍 `Found no elements matching Core` ⇒ **仍无 `Core` 键**（不自动认领）。
- **B-15**（核 54 格全满 + Progress=595）：`252, 200, 255 has the following block data: 600`；4 秒后第二次读仍是 `600`；`Held` = `{count: 1, id: "farmstorm:puffed_wheat"}` ⇒ **`Progress` 停在 600、`Held` 保留**；该培养器 `Core` = `Core:70093867315400L`（**有**）。
- **B-16**（腾出 2 格后）：`Progress` = `0`、`Held` → `Found no elements matching Held`；核 `255 200 255 Items[50]` = `{count: 2, Slot: 50b, id: "farmstorm:puffed_wheat"}`、`Items[51]` = `{count: 2, Slot: 51b, id: "farmstorm:puffed_wheat_seeds"}` ⇒ **从暂停处直接投递，不重新计时**。
- **B-19**（有接替核）：**未取到**——候选核来自 `OutpostCoreIndex`，`/setblock` 放的核不进索引，构造态走不到改绑分支（已在表内标未验证）。
- **B-20**（无接替核）：拆核前 `221, 200, 200 … {Progress: 163, Held: {count: 1, id: "farmstorm:power_beet"}, …, Core: 61022896160968L}` → 拆核后 `221, 200, 200 … {Progress: 203, Held: {count: 1, id: "farmstorm:power_beet"}, …, id: "farmstorm:cultivator"}`，`data get block 221 200 200 Core` → `Found no elements matching Core` ⇒ **`Core` 键消失、内容与进度保留**。

## B-26 / B-27 的实际掉落清单

- **B-27**（空核 `[257 200 256]`，`Items: []`，`setblock … minecraft:air destroy`）：`Outpost Core ×1` —— 只有方块本体，**不刷物品**。
- **B-26 的正面样本**（装 3 格的核 `[256 200 256]`，`setblock … minecraft:air destroy`）：`Outpost Core ×1` / `Stone ×62` / `Stone ×2` / `Diamond ×3` / `Puffed Wheat ×5`（核内容 3 格全部落地）。
- **活塞推动 / 同方块替换 / 区块卸载**三种「不掉」场景：**未取到**——`/setblock` 同状态被原版拒绝（`Could not set the block`）、活塞需实体时序、区块卸载需真停服，已在表内标未验证。
