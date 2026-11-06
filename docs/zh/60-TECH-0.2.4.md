# 60 号 v0.2.4 技术规格

> 依据：`docs/zh/58-DECISIONS-0.2.4.md`（下称 58 号，D-225 ~ D-252，唯一真相源）+ `.work/v024-BRIEF.md`（下称 BRIEF）+ `plan0.2.4.txt` 的 4 条需求。
> 读者：实现方。本文写到「照着能写代码」的程度——每处改动给位置、形状、判据与验收读法。
> 冲突处理：58 号与源码冲突报作者、不自行取舍；BRIEF 与源码冲突以源码为准并把差异登记在 §9。
> 行号纪律：本文行号按 UTF-8 实读（`read` 工具逐行核过），不抄上游结论。
> 术语：「核心浮窗」= `client/CoreHudRenderer`（58 号 D-240），**不是** `OutpostCoreScreen`（54 格容器界面，本版一行不动，红线 5）。

---

## §1 口径总表

§1.1 全版数字只有一行变，其余十一行冻结在基线值（58 号 D-227）。

| 项 | 基线 | 本版目标 | 算式 | 出处 |
| --- | --- | --- | --- | --- |
| 注册面 | 92 | **92** | 92 − 0 + 0 | `tools/verify_project.py:458-471`；`Farmstorm.java:180-183` |
| server config | 175 键 / 60 组 | **175 / 60** | 175 − 0 + 0 | `run/config/farmstorm-server.toml` 实点；58 号 D-227 |
| client config | 3 键 | **3** | 3 − 0 + 0 | `FarmstormClientConfig.java:25-27` |
| 语言键 | 321 × 2 | **321 × 2** | 321 − 0 + 0 | `ModLangProvider.java:14-16`；两语言文件实点 |
| └ `configuration` 组 | 178 | **178** | 178 − 0 + 0 = 175 + 3 | `ModLangProvider.java:14` |
| **Java 文件数** | 152 | **151** | 152 − 2 + 1 = 151 | 删除 `farmland/FarmlandGuard.java` 与 `farmland/FarmlandLedger.java`（58 号 D-253）；新增 `blockentity/CoreBound.java`（58 号 D-228） |
| 网络包 | 2 | **2** | 2 − 0 + 0 | `FarmstormNetwork.java:42`、`:47` |
| Mixin | 0 | **0** | 0 + 0 | 全仓 `*.mixins.json` 命中 0（不含 `.scratch`） |
| 进度 | 16 | **16** | 16 − 0 + 0 | `src/generated/resources/data/farmstorm/advancement/` 16 个 json |
| 转换配方 | 21 | **21** | 21 − 0 + 0 | `src/generated/resources/data/farmstorm/recipe/` 含 `farmstorm:converting` 的文件 21 个 |
| 培养器投入物 | 17 | **17** | 6 + 11 | `CultivatorBlockEntity.java:269-283`（不经 config，随配方表走） |
| 交易 listing | 26 | **26** | 22 + 1 + 3 | 只改 xp 值，不增减项（58 号 D-237、D-239） |
| 检查器 FAIL | 0 | **0** | `EXPECTED` / `EXPECTED_COUNTS` 不改 | 58 号 D-227 |

§1.2 唯一变化的数字是 Java 文件数，算式与落点：

- 算式：**152 − 2 + 1 = 151**。152 是 v0.2.3 的记录基线；− 2 是删除 `farmland/FarmlandGuard.java`（225 行）与 `farmland/FarmlandLedger.java`（555 行）（58 号 D-253，空目录一并删除）；+ 1 是新建 `src/main/java/com/dashpig/farmstorm/blockentity/CoreBound.java`（58 号 D-228、§3.1 第 1 行）；此后再不删任何 Java 文件。
- 当前实点读数：`(Get-ChildItem -Recurse src/main/java -Filter *.java).Count` = **150**（本会话实点，= 152 − 2；`CoreBound.java` 尚未新建，所以此刻不是 151）。
- **检查器不改**：`tools/verify_project.py:479-480` 只把源码目录下的 `*.java` 拼成一个字符串做内容匹配（`all_java = "\n".join(read(f) for f in java.rglob("*.java"))`），全文不含任何「文件个数」断言；`EXPECTED_COUNTS`（`:458-471`）与 `expected_total = sum(EXPECTED_COUNTS.values())`（`:610`）数的都是注册 ID，不是文件。
- 连带的散文口径：151 只出现在 `signoff` / `README` 这类说明文字里，不进检查器、不进注册自检。

§1.3 三条口径纪律：

- 注册面、协议、配置、语言键四个面本版零变化，§2 / §5 / §6 / §7 各给一条可复现的证明，不写「无变化」四个字。
- 每条改动的判据只能是：计数、日志行、`/data get` 读数、源码 grep 命中、像素比对（§8）。
- 58 号未定的落点一律写「由 D-xxx 定」，本文不自己发明数值。

---

## §2 注册面逐项

§2.1 十二类小计（本版每类 `− 0 + 0`，相加仍是 92）：

| # | 类 | 基线 | 本版 | 算式 |
| --- | --- | --- | --- | --- |
| 1 | `items` | 28 | **28** | 28 − 0 + 0 |
| 2 | `blocks` | 28 | **28** | 28 − 0 + 0 |
| 3 | `block_entity_type` | 8 | **8** | 8 − 0 + 0 |
| 4 | `entity_type` | 13 | **13** | 13 − 0 + 0 |
| 5 | `mob_effect` | 4 | **4** | 4 − 0 + 0 |
| 6 | `sound_event` | 3 | **3** | 3 − 0 + 0 |
| 7 | `recipe_type` | 1 | **1** | 1 − 0 + 0 |
| 8 | `recipe_serializer` | 1 | **1** | 1 − 0 + 0 |
| 9 | `creative_mode_tab` | 1 | **1** | 1 − 0 + 0 |
| 10 | `damage_type` | 3 | **3** | 3 − 0 + 0 |
| 11 | `data_component_type` | 1 | **1** | 1 − 0 + 0 |
| 12 | `menu_type` | 1 | **1** | 1 − 0 + 0 |
| — | **合计** | **92** | **92** | 28+28+8+13+4+3+1+1+1+3+1+1 = 92 |

§2.2 不动的四份证明：

1. **期望值出处**：`tools/verify_project.py:458-471` 的 `EXPECTED_COUNTS` 就是上表十二行，逐行同值；`:610` 用 `sum(EXPECTED_COUNTS.values())` 现算总数，不写第二份 92。
2. **运行时自检**：`Farmstorm.java:180-183` 断言 `total == 92` 且十二类逐个相等（`items == 28 && blocks == 28 && blockEntities == 8 && entityTypes == 13 && mobEffects == 4 && soundEvents == 3 && recipeTypes == 1 && recipeSerializers == 1 && creativeTabs == 1 && damageTypes == 3 && dataComponents == 1 && menuTypes == 1`）。本版一行不改这段断言。
3. **改动面反证**：58 号 §3.1（新增 6 项）与 §3.2（修改 17 项）共 23 处，没有一处是 `registry/` 下的类；`FarmstormBlocks` / `FarmstormItems` / `FarmstormBlockEntities` / `FarmstormSounds` / `FarmstormMenus` / `FarmstormRecipes` 全部不在改动面内（58 号 D-246 另把 `FarmstormSounds` 写成显式禁改）。
4. **两个易错类**：`sound_event` = 3 的现值由 `registry/FarmstormSounds.java:10` 的注释「3 个 sound_events」与 `REGISTER` 条目数共同给出；`menu_type` = 1 是 v0.2.3 删掉 `converter` 菜单后的现值（`verify_project.py:456` 的算式注释），本版不动。

§2.3 Mixin = 0 的证明：全仓 `*.mixins.json` 命中 **0**（`.scratch` 反编译源不计）；本版不新增 mixin 配置文件，故 0 + 0。

---

## §3 资源面

§3.1 资源文件清点（本会话实点，本版一个都不改）：

| 根 | 文件数 | 构成 |
| --- | --- | --- |
| `src/main/resources` | 131 | 110 `.png` + 7 `.json` + 6 `.mcmeta` + 3 `.ogg` + 3 `.fsh` + 1 `.vsh` + 1 `.toml` = 131 |
| `src/generated/resources` | 232 | 220 `.json` + 12 个 `.cache/` 哈希缓存文件 = 232 |
| └ 进度 json | 16 | `data/farmstorm/advancement/` 下 16 个 |
| └ 配方 json | 25 | 其中含 `farmstorm:converting` 的 21 个 |
| └ 标签 json | — | `tags/block/processing_block.json` 成员仍 2 项（`:2-5`） |

§3.2 三条证明：

1. **改动面全是 `.java`**：58 号 §3.1 的 6 个新增物件与 §3.2 的 17 处修改，路径全部落在 `src/main/java/com/dashpig/farmstorm/` 下（`blockentity/` 4 个类、`block/` 3 个类、`event/` 4 个类、`util/` 2 个类、`client/` 1 个类）。58 号 §3.3 的删除项是 0（资源面），即不删贴图、模型、音效、语言文件。
2. **标签产物不变**：标签成员由 `datagen/ModBlockTagsProvider.java:44-47` 生成（`tag(FarmstormTags.PROCESSING_BLOCK).add(CULTIVATOR, CROP_CONVERTER)` 两行），产物 `src/generated/resources/data/farmstorm/tags/block/processing_block.json:2-5` 仍是 `farmstorm:cultivator` 与 `farmstorm:crop_converter` 两项。本版只改「谁读这个标签」（58 号 D-230），不改成员，故产物内容逐字不变；重跑 datagen 允许改 mtime，**内容哈希必须相等**。
3. **无贴图/模型/音效改动**：58 号 D-246 把「不改 `SoundType`、不改 `sound_event` 注册、不新增音频资源」写成显式禁令；110 个 `.png`、3 个 `.ogg`、6 个 `.mcmeta` 与 `assets/farmstorm/sounds.json` 都不在改动面内。

§3.3 资源面验收口径：`git status --porcelain` 的结果里不得出现 `src/main/resources/` 或 `src/generated/resources/` 开头的一行（判据见 §8 的第 22 条）。

---

## §4 行为规格

四条需求按 plan 的编号顺序写（58 号 D-226）。每小节的结构固定：改哪里 → 改成什么形状 → 判据 → 边界。

### §4.1 需求 1 · 绑定统一（D-228 ~ D-235、D-247）

#### §4.1.1 新增接口 `CoreBound`（D-228）

新建 `src/main/java/com/dashpig/farmstorm/blockentity/CoreBound.java`，只声明三个方法：

```java
package com.dashpig.farmstorm.blockentity;

import net.minecraft.core.BlockPos;

public interface CoreBound {
    BlockPos corePos();

    void setCore(BlockPos pos);

    void clearCore();
}
```

三条约束：① 不加第四个方法，尤其不声明 `kind()`（58 号 D-234 —— `Link.Kind` 是协议字段，见 `network/CultivatorLinkPayload.java:69-71`）；② 不引入新的注解来源，可空语义留在实现类的 `@Nullable`（`CropConverterBlockEntity.java:70-71`）；③ 不抽 `AbstractBoundBlockEntity` 基类（58 号 §6 第 1 条）。

#### §4.1.2 两个方块实体实现它（D-229）

| 类 | 现状 | 改法 |
| --- | --- | --- |
| `blockentity/CultivatorBlockEntity` | `:44` 声明 `extends BlockEntity`；三方法已齐（`:66` `corePos()`、`:84` `setCore`、`:94` `clearCore`） | `:44` 改为 `implements CoreBound`，方法体一行不动 |
| `blockentity/CropConverterBlockEntity` | `:40` 声明；只有 `corePos()`（`:71`）与 `setCore`（`:89-92`） | `:40` 改为 `implements CoreBound`；补 `clearCore()` 与 `syncCore()`（§4.1.3） |

两个类同在 `blockentity` 包内，接口不需要 import。

#### §4.1.3 转换器补两个方法（D-229 + D-247）

**这是本版唯一一处「新增方法本身」而不是「补一行调用」的地方**：全仓 `syncCore` 的定义只有 `CultivatorBlockEntity.java:104-110` 一处（`:104` 私有方法、`:109` 是 `sendBlockUpdated(..., Block.UPDATE_CLIENTS)`），`CropConverterBlockEntity` 现无此方法 —— 只写调用会编译不过（58 号 §8 第 1 条、D-247 补充）。

`CropConverterBlockEntity.java:89-92` 改成下面三段（与培养器的 `:84-88`、`:94-101`、`:104-110` 同形）：

```java
public void setCore(BlockPos pos) {
    this.corePos = pos;
    this.setChanged();
    this.syncCore();
}

public void clearCore() {
    if (this.corePos == null) {
        return;
    }
    this.corePos = null;
    this.setChanged();
    this.syncCore();
}

private void syncCore() {
    if (this.level == null || this.level.isClientSide) {
        return;
    }
    BlockState state = this.getBlockState();
    this.level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
}
```

四条约束：① 不需要新增 import —— `Block.UPDATE_CLIENTS` 已在 `:227` 使用、`BlockState` 已在 `:223` 使用；② `clearCore()` 与培养器同形，**不清** `Held` / `Progress`（D-168 继续有效），两个 NBT 键在 `:51`、`:53`；③ 用 `setCore(null)` 表达解绑**不允许**（58 号 D-229 补充 —— 与「NBT 缺 `Core` 键 = 未绑定」的约定冲突，写面见 `:270-272`、读面 `:283`）；④ 两个类各写一份同形私有方法，不抽公共基类。

判据：`Select-String -Path ...\CropConverterBlockEntity.java -Pattern "private void syncCore"` 命中 1；同文件 `sendBlockUpdated` 命中 1；`Block.UPDATE_CLIENTS` 命中 2（`:227` 与新方法各一）。

#### §4.1.4 收集面加按标签的重载（D-231）

`util/AreaBlockEntities` 现有唯一重载是 `collect(ServerLevel, BlockPos, int, Class<T>)`（`:32-33`，按 `type.isInstance` 过滤在 `:44`）。新增第二个重载，**旧重载原样保留**：

```java
public static List<BlockEntity> collect(ServerLevel level, BlockPos center, int chunkRadius,
                                        Predicate<BlockEntity> filter)
```

两条不能破的纪律：① **不为枚举加载区块** —— 区块只从 `level.getChunkSource().getChunkNow(...)` 取，取不到就跳过（`:39-42` 的写法照抄）；② **两阶段拷贝** —— 先用 `new ArrayList<>(chunk.getBlockEntities().values())` 复制再过滤（`:43`），不做边遍历边改。

不许在 `collect` 上加「第四个 `Class` 列表参数」（58 号 D-231 的否决面）。

#### §4.1.5 四处成对 `instanceof` 改接口调用（D-230 + D-232）

| # | 位置 | 现状 | 改成 |
| --- | --- | --- | --- |
| 1 | `OutpostCoreBlockEntity.java:604-608` | 两次 `AreaBlockEntities.collect(..., CultivatorBlockEntity.class)` / `(..., CropConverterBlockEntity.class)` | 一次按标签：`collect(server, this.getBlockPos(), RANGE_CHUNKS, be -> isProcessingBlock(be.getBlockState()))` |
| 2 | `:621-629` | `if (candidate instanceof CultivatorBlockEntity ...) ... else if (candidate instanceof CropConverterBlockEntity ...) ... else continue` | `if (!(candidate instanceof CoreBound bound)) { continue; }` + `kind` 取映射（§4.1.6）+ `cachedCore = bound.corePos()` |
| 3 | `:639-643` | `if (candidate instanceof CultivatorBlockEntity cultivatorBind) ... else if (...) ...` | `((CoreBound) candidate).setCore(this.getBlockPos());`（顺序仍是先核侧后机器侧，`:636-643`） |
| 4 | `:851-855` | `if (nextChunk.getBlockEntity(pos) instanceof CultivatorBlockEntity ...) ... else if (...) ...` | `if (nextChunk.getBlockEntity(pos) instanceof CoreBound bound) { bound.setCore(next.getBlockPos()); }` |

站点 1 的标签判据**复用已有实现**：`isProcessingBlock(BlockState)` 在 `:887-889`，方法体就是 `state.is(FarmstormTags.PROCESSING_BLOCK)`（判据出处 58 号 D-194）。本版不新写第二份标签判定。

#### §4.1.6 kind 的单一映射（D-235，58 号标「本文补充，作者可否决」）

接口化之后「这台机器的 kind 是谁」必须另有落点：`CoreBound` 不声明 `kind()`（D-234），标签本身也存不下类型（`util/FarmstormTags.java:30` 是 `TagKey<Block>`，产物 `processing_block.json:2-5` 只有方块 id）。落法是一张表：

```java
private static final Map<Block, Link.Kind> KIND_BY_BLOCK = Map.of(
        FarmstormBlocks.CULTIVATOR.get(), Link.Kind.CULTIVATOR,
        FarmstormBlocks.CROP_CONVERTER.get(), Link.Kind.CROP_CONVERTER);
```

三条约束：① 取值点写成 `KIND_BY_BLOCK.get(candidate.getBlockState().getBlock())`，取到 `null` 就 `continue` —— **没登记进表的新标签成员只被发现、不写 kind、不绑定**（58 号 §8 第 4 条的已知限制）；② 不得复用方法名 `kindOf`：`:373-379` 已有 `kindOf(BlockPos)`（迁移取 kind），同名不同参会造出第二份 kind 真相，本版只在 `:619` 附近就地取表；③ 新增标签成员时必须同时登记本表与 `datagen/ModBlockTagsProvider.java:44-47`（两处同改，BRIEF §7 第 1 条）。

#### §4.1.7 清理出口覆盖两个方块（D-233，bug 的正面修复）

`:866-873` 现在是「区块在内存 ⇒ 判标签 ⇒ 若是培养器则 `clearCore()`」，转换器没有分支。改成按接口调用：

```java
if (cultivatorChunk.getBlockEntity(pos) instanceof CoreBound bound) {
    bound.clearCore();                    // 出口②：留原地、清 Core、内容保留、不可使用
    Farmstorm.LOGGER.info("迁移失败：标签方块留原地并标为未绑定 {}", pos);
}
```

三条约束：① `clearCore()` 的**调用点仍是 1 处**（原 `:871` 合并进新分支，58 号 D-233 补充）；② 日志文本去掉「培养器」字样（旧串「迁移失败：培养器留原地并标为未绑定」在 `:872`），改后旧串命中 0；③ 出口③ 的两行按 61 号 A-108 同批改：`:867` 的日志串改为「迁移失败且该格已不是处理方块」，`:859` 注释去「培养器」字样；`:863` 的「区块未加载」一行不动。

#### §4.1.8 绑定状态机（四态 + 九条迁移）

| 态 | 判据（全部可读） | 出处 |
| --- | --- | --- |
| **A 未绑定** | `corePos == null`，且 NBT 无 `Core` 键 | `:270-272`、`:283` |
| **B 有效绑定** | `corePos != null` ∧ `core()` 非 null ∧ 核侧 `hasCultivator(pos)` ∧ `inRange(corePos, pos)` | `:102-115`、`:381-382` |
| **C 失效绑定** | `corePos != null` 但 B 的后三项任一为假 | 同上 |
| **D 悬空缓存** | `corePos != null` 而该坐标方块已不是核心（0.2.3 的 bug 态） | 病因链见 58 号 D-233 依据 |

| 事件 | 起点 → 终点 | 写面 | 依据 |
| --- | --- | --- | --- |
| 放置，且选到核 | A → B | 先 `addCultivator(pos, kind)` 再 `setCore(corePos)` | `CultivatorBlock.java:100-102`、`CropConverterBlock.java:104-106` |
| 放置，未选到核 | A → A | 不写 `Core` 键（缺键即未绑定） | `CultivatorBlock.java:97-99`、`CropConverterBlock.java:101-103` |
| 补绑，缓存为空且核未满 | A → B | 核侧先写、机器侧后写 | `:614-645` |
| 补绑，核已满 | A → A | 不写 | `:615-617` |
| 补绑，缓存非空 | 任何态 → 不变 | 不写（不夺、不清） | `:630-632` |
| 拆核，有接替核 | B → B′（迁移改绑） | 按 `kindOf(pos)` 搬 kind + `setCore(next)` | `:845-857` |
| 拆核，无接替核 | B / D → A | `clearCore()` | `:866-873` |
| 拆核，区块未加载 | 不变 | 不写、只记一行日志 | `:862-864` |
| 拆机器 | 方块实体消失 | 核侧条目由惰性清理摘掉 | `:434-452` |

D → A 这一条就是需求 1 的修复本体：0.2.3 里转换器停在 D，而补绑对非空缓存一律跳过（`:630-632`），于是永久拒绝。

#### §4.1.9 边界（每条都要在实现里落成一行判据）

| 情形 | 期望 | 依据 |
| --- | --- | --- |
| 转换器的核被拆，3 × 3 区块内还有己方核 | 走迁移改绑，`Core` 写成新核坐标，核侧两条列表同长 | `:845-857` |
| 转换器的核被拆，无接替核 | `Core` 键消失，`Held` / `Progress` 保留，机器不可使用 | `:866-873`、`CropConverterBlockEntity.java:249-260` |
| 核被拆且该格区块不在内存 | 保留 NBT 原值，不动世界 | `:862-864` |
| 机器已绑定别的核 | 补绑跳过，不夺 | `:630-632` |
| 机器缓存非空但核已改绑到本核 | 核侧已有条目，跳过 | `:633-635` |
| 标签成员未登记进 `KIND_BY_BLOCK` | 被发现、不绑定、日志无新增行 | §4.1.6 |
| 一次拆核涉及 3 台机器 | `added` 计数与实际绑定数一致，日志一行 | `:646-649` |
| 拆核后同一 tick 放新核 | 新核的补绑在下一个 `REBIND_INTERVAL_TICKS` 窗口生效（补绑每周期扫一圈） | `:604-649` |

### §4.2 需求 2 · 交易经验分级（D-236 ~ D-239）

#### §4.2.1 五个常量只在 `VillagerTradeEvents` 定义一次（D-236）

在 `event/VillagerTradeEvents` 的类体里加五行，右值一律写字面量：

```java
public static final int XP_LEVEL_1 = 2;
public static final int XP_LEVEL_2 = 5;
public static final int XP_LEVEL_3 = 10;
public static final int XP_LEVEL_4 = 15;
public static final int XP_LEVEL_5 = 30;
```

三条约束：① 可见性必须是 `public` —— 三个交易类要**引用**同一份定义（D-236：其余类只引用不复制），`private` 会让它们只能各自抄一份；② 右值是字面量 ⇒ 它们是**编译期常量**，javac 内联，`VillagerTradeEvents` 读 `BotanistTrade.NORMAL`、`BotanistTrade.NORMAL` 读这五个常量，两者不互相触发类初始化；③ 1 级取 **2**（保留现值），不取原版 `VillagerTrades.java:70` 的 `XP_LEVEL_1_SELL = 1`（裁定 #2 明写保留 2）。

不引用原版那五个符号：它们是 `private static final`（`.scratch/p25a/src/net/minecraft/world/entity/npc/VillagerTrades.java:70-78`），本版只取数值。

#### §4.2.2 `BotanistTrade`：record 加 `xp` 分量（D-237）

| 位置 | 现状 | 改法 |
| --- | --- | --- |
| `:38` | `public record BotanistTrade(Item item) implements VillagerTrades.ItemListing` | 加第二分量：`(Item item, int xp)` |
| `:49` | `public static final int VILLAGER_XP = 2;` | **删掉**（留下就是第二份真相） |
| `:60-82` | 22 个 `new BotanistTrade(Items.X)` 字面量 | 每项补第二实参：第 1~6 项 `XP_LEVEL_1`、第 7~12 项 `XP_LEVEL_2`、第 13~18 项 `XP_LEVEL_3`、第 19~22 项 `XP_LEVEL_4` |
| `:91` | `MAX_USES, VILLAGER_XP, PRICE_MULTIPLIER` | 中间一项改成 `this.xp`，参数位置不动 |
| `:41`、`:43`、`:45`、`:47`、`:51` | `MIN_PRICE` / `MAX_PRICE` / `MAX_USES` / `COUNT` / `PRICE_MULTIPLIER` | 一行不改（红线 6） |

分档依据是 `VillagerTradeEvents.java:44-47` 的四个切片边界 0 / 6 / 12 / 18 / 22 —— 位置决定等级，所以 `xp` 必须按位置逐项写死在字面量里，不能在 `getOffer` 里推。**22 行的顺序与行内注释不得改**（红线 4）。

#### §4.2.3 `TierTrade`：record 加 `xp` 分量（D-237）

| 位置 | 现状 | 改法 |
| --- | --- | --- |
| `:37-38` | `record TierTrade(CropTier tier, List<CropProfile> pool, ModConfigSpec.IntValue minKey, ModConfigSpec.IntValue maxKey)` | 尾部加 `, int xp` |
| `:45` | `public static final int VILLAGER_XP = 2;` | **删掉** |
| `:49-54` | 三个常量由 `of(tier, minKey, maxKey)` 造 | 三档分别传 `XP_LEVEL_1`（COMMON，挂 1 级）/ `XP_LEVEL_3`（RARE，挂 3 级）/ `XP_LEVEL_5`（EPIC，挂 5 级） |
| `:56-58` | `of(...)` 三参数 | 加第四参 `int xp` 并透传给构造器 |
| `:79` | `MAX_USES, VILLAGER_XP, PRICE_MULTIPLIER` | 中间一项改成 `this.xp` |
| `:41`、`:43`、`:47` | `CROP_COUNT` / `MAX_USES` / `PRICE_MULTIPLIER` | 一行不改（红线 6） |

三档的等级出处是 `VillagerTradeEvents.java:55-57` 的 `get(1)` / `get(3)` / `get(5)`。

#### §4.2.4 `BotanistEpicTrade`：只改常量（D-238）

| 位置 | 现状 | 改法 |
| --- | --- | --- |
| `:46` | `public static final int VILLAGER_XP = 2;` | 改为 `= VillagerTradeEvents.XP_LEVEL_5;`（值 30） |
| `:45` | 注释「成交经验（沿用 `{@link TierTrade#VILLAGER_XP}` 的 2）」 | 重写为指向 `VillagerTradeEvents.XP_LEVEL_5`，删掉「2」 |
| `:28` | 类 javadoc「`{@link TierTrade#VILLAGER_XP}` 的 xp = 2」 | **同批重写** —— 改常量后它是事实错误，且 §4.2.3 删掉 `TierTrade.VILLAGER_XP` 后它是悬挂链接 |
| `:38`、`:40`、`:42`、`:44` | `MIN_PRICE` / `MAX_PRICE` / `MAX_USES` / `COUNT` | 一行不改 |
| `:64` | `MAX_USES, VILLAGER_XP, PRICE_MULTIPLIER` | 不动（仍引用本类常量） |

:28 这一行 58 号 D-238 的落地位置没点到（只写 `:45` 与 `:46`），但它是同一处口径的第三份副本，随本条同改。

#### §4.2.5 等级 → xp 的落成表（合计 26 = 23 + 3）

| 职业 | 等级 | 挂载 | 条数 | xp | 常量 |
| --- | --- | --- | --- | --- | --- |
| 植物学家 | 1 | `NORMAL.subList(0,6)` | 6 | 2 | `XP_LEVEL_1` |
| 植物学家 | 2 | `subList(6,12)` | 6 | 5 | `XP_LEVEL_2` |
| 植物学家 | 3 | `subList(12,18)` | 6 | 10 | `XP_LEVEL_3` |
| 植物学家 | 4 | `subList(18,22)` | 4 | 15 | `XP_LEVEL_4` |
| 植物学家 | 5 | `BotanistEpicTrade.EPIC` | 1 | 30 | `XP_LEVEL_5` |
| （小计） | — | — | **23** | — | 6 + 6 + 6 + 4 + 1 = 23 |
| 农民 | 1 | `TierTrade.COMMON` | 1 | 2 | `XP_LEVEL_1` |
| 农民 | 3 | `TierTrade.RARE` | 1 | 10 | `XP_LEVEL_3` |
| 农民 | 5 | `TierTrade.EPIC` | 1 | 30 | `XP_LEVEL_5` |
| （小计） | — | — | **3** | — | 1 + 0 + 1 + 0 + 1 = 3 |
| **合计** | — | — | **26** | — | 23 + 3 = 26 = 22 + 1 + 3 |

不许按 `CropTier` 查表推 xp（58 号 §6 第 2 条：植物学家 1~4 级全是 `NORMAL` 切片，按档位推会把四个等级算成同一个值）。

### §4.3 需求 3 · 核心浮窗背景（D-240 ~ D-242）

一行改动，落在 `client/CoreHudRenderer.java:40`：

```java
private static final int COLOR_BACKGROUND = 0x4D000000;
```

算式：0.30 × 255 = 76.5 ≈ **77 = `0x4D`**；77 ÷ 255 = **30.2%**。现状 `0x80` = 128，128 ÷ 255 = **50.2%**（裁定 #3、#4）。

不动的五项：

1. `:41` 的 `COLOR_BORDER = 0xFF000000` 一行不改（D-242）。
2. `:77-78` 两条 `fill` 的几何（外框整块 + 内底内缩 1 px）不改。
3. `:30-38` 的版式常数（`LINE_HEIGHT` / `LINE_GAP` / `PADDING` / `PANEL_EXTRA_HEIGHT` / `MAX_CORE_LINES`）不改。
4. 不加 config 键：client config 仍是 3 键（`FarmstormClientConfig.java:25-27`），server 仍是 175 键（裁定 #4 硬编码）。
5. 不加语言键：`gui.farmstorm.core_hud.self`（`:47`）是唯一为浮窗存在的新增键，本版不动它。

像素判据：面板内层底色像素 alpha = **77**（`0x4D`），外框 1 px 像素 alpha = **255**（`0xFF`）；两者都要读，只读一个是无效验收。

### §4.4 需求 4 · 放置音效（D-243 ~ D-246）

#### §4.4.1 根因一行话

三个方块的状态级 `useItemOn` 都返回 `PASS_TO_DEFAULT_BLOCK_INTERACTION`（原版 `BlockBehaviour.java:205-208`），客户端因此落到 `useWithoutItem`（`MultiPlayerGameMode.java:337-341`）；而 mod 的客户端分支无条件 `SUCCESS`，`SUCCESS` 属「已消费」（`InteractionResult.java:11-13`）⇒ `:339-340` 直接 return ⇒ 客户端的 `itemstack.useOn`（`:352` / `:355`）一次都不执行 ⇒ `BlockItem.java:85-93` 的本地放置音从不播放。服务端不受影响（`ServerPlayerGameMode.java:369` → `:386` 真的放置、真的广播），而广播把放置者本人当排除者（`Level.java:410-412` → `ServerLevel.java:994-1004`），能补上这一半的客户端本地播放（`ClientLevel.java:527-529`）正好被掐掉。

#### §4.4.2 三处客户端分支（D-243 + D-245）

`block/CropConverterBlock.java:121-122`：

```java
if (level.isClientSide) {
    return level.getBlockEntity(pos) instanceof CropConverterBlockEntity converter
            && converter.convertible(player.getMainHandItem())
            ? InteractionResult.SUCCESS : InteractionResult.PASS;
}
```

`block/CultivatorBlock.java:117-118`：

```java
if (level.isClientSide) {
    return level.getBlockEntity(pos) instanceof CultivatorBlockEntity cultivator
            && cultivator.admissible(player.getMainHandItem())
            ? InteractionResult.SUCCESS : InteractionResult.PASS;
}
```

`block/OutpostCoreBlock.java:131-132`：

```java
if (level.isClientSide) {
    return player.getMainHandItem().getItem() instanceof BlockItem ? InteractionResult.PASS
            : InteractionResult.SUCCESS;
}
```

#### §4.4.3 谓词的可见性与选择（D-244 / D-245）

| 项 | 现状 | 改法 |
| --- | --- | --- |
| `CropConverterBlockEntity#convertible` | `:129` `private` | 提到 `public`（`blockentity` 与 `block` 不同包，不提就编译不过） |
| `CultivatorBlockEntity#admissible` | `:143` 已是 `public`，判据 `:143-149`（内含 `outputProfileOf` 与 `tier != EPIC`） | 不用改可见性；服务端 `canInsert` 的物品侧闸（`:152-154`）就是它 |
| `CultivatorBlockEntity#outputProfileOf` | `:163-164` `private`，返回 `@Nullable CropProfile` | **不动**（`admissible` 已经包住它） |
| `canInsert` | `:118-120` / `:152-154`，含 `isUsable()` | **禁止**在客户端分支里调用：`core()` 要求 `ServerLevel`（`CropConverterBlockEntity.java:230-238`），客户端恒假 |
| 据点核心的谓词 | 它不收物品（`:134-142` 是 `openMenu`），故用「手持 `BlockItem` ⇒ 让路」 | D-245（58 号标「本文补充，作者可否决」） |
| 归属判定 | 客户端不做 | `:126` 的 HC-03 继续有效 |

客户端可调性的静态依据：`convertible` 走 `level.getRecipeManager()`（`:134-136`），`admissible` 经 `outputProfileOf`（`:147`、`:169-170`）走同一个入口，客户端 `ClientLevel.getRecipeManager()` 返回 `connection.getRecipeManager()`（`ClientLevel.java:609-610`），配方由 `ClientPacketListener.java:1500` 的 `handleUpdateRecipes` 填充。静态链成立，进游戏仍需实测（D-252）。

#### §4.4.4 服务端一律不改

四道闸与开界面分支一行不动：`CropConverterBlock.java:124-132`、`CultivatorBlock.java:120-128`、`OutpostCoreBlock.java:134-142`。本版只改**客户端预测面**（58 号 §5 第 3 条：准入面不动）。

#### §4.4.5 五情形判据表

| # | 情形 | 客户端返回 | 服务端返回 | 玩家看到的 |
| --- | --- | --- | --- | --- |
| 1 | 手持输入物，机器可用 | `SUCCESS` | `CONSUME` | 物品进槽，无放置 |
| 2 | 手持输入物，机器已满或未绑定 | `SUCCESS` | `PASS` | 无反应（已知影响 D-251） |
| 3 | 手持非输入方块 | `PASS` | `PASS` → `useOn` 放置 | 方块放下，本地音效播放（需求 4 的正面修复） |
| 4 | 空手 | `PASS` | `PASS` | 两侧都无动作 |
| 5 | 手持 `BlockItem` 右键己方核 | `PASS` | `CONSUME`（开界面） | 界面打开 + 客户端预测的方块被服务端回滚（已知影响，§9 的 RT-85） |

情形 2 的补充：手持的是方块时，服务端仍会走 `useOn` 把它放下（`ServerPlayerGameMode.java:378-387`），这是 0.2.3 就有的既有行为，本版不改变它。

#### §4.4.6 显式禁令（D-246）

不得为修本条改动 `SoundType`、`sound_event` 注册或任何音频资源；两个方块现用 `SoundType.ANVIL`（58 号 D-246 引 `FarmstormBlocks.java:78`、`:108`），place 音 = `SoundEvents.ANVIL_PLACE`（原版 `SoundType.java:58-60`）。改音效会新增一个 `sound_event` 注册项、破注册面 92（§2）。

---

## §5 协议

§5.1 本版协议零变化，三个不动的凭据：

| 项 | 现值 | 出处 | 本版 |
| --- | --- | --- | --- |
| 协议串 | `"3"` | `network/FarmstormNetwork.java:29` | 不动（红线 3） |
| 包 1 | `farmstorm:core_list`（S2C 只读，`optional()`） | `:42-43` | 不动 |
| 包 2 | `farmstorm:cultivator_link`（S2C 只读，`optional()`） | `:47-48` | 不动 |
| `Link.Kind` 线上码 | `CULTIVATOR = 0`、`CROP_CONVERTER = 1`（各 1 字节） | `network/CultivatorLinkPayload.java:69-71` | 不动 |
| `Link` 的三分量 | `core` / `cultivator` / `kind` | `:63` | 不动 |
| 上限常量 | `MAX_CORES` / `MAX_LINKS` | `:54`、`:56` | 不动 |

§5.2 两条证明：

1. 58 号 §3.1（新增 6 项）与 §3.2（修改 17 项）里**没有** `network/` 下的任何文件：`FarmstormNetwork`、`CultivatorLinkPayload`、`CoreListPayload` 都不在改动面内，故 2 − 0 + 0 = 2。
2. `CoreBound` 是方块实体侧的接口，**不进协议**：它不声明 `kind()`（D-234），`Link.Kind` 仍是协议字段、仍由方块在放置时自报（`CultivatorBlock.java:100`、`CropConverterBlock.java:104`），kind 的搬运仍走核侧的 `cultivatorKinds` 平行列表（`OutpostCoreBlockEntity.java:139`）与 NBT 旁挂键（`:938-944`）。

§5.3 `PROTOCOL_VERSION` 不动的验收：`Select-String -Path src/main/java/com/dashpig/farmstorm/network/FarmstormNetwork.java -Pattern "PROTOCOL_VERSION = "` 命中 1 行且值为 `"3"`；`.playToClient(` 命中 2 行。

---

## §6 配置

§6.1 server 侧 175 键 / 60 组，逐个区块（组名（键数）按家族归并，六十个组名一个不少）：

| 家族 | 组数 | 键数 | 组名（键数） | 本版 |
| --- | --- | --- | --- | --- |
| A 通用与系统 | 7 | 12 | `crop_converter`(1) `stack`(2) `deploy`(3) `teams`(2) `explosion`(2) `throwable`(1) `food`(1) | 不动 |
| B 早期作物 | 7 | 21 | `puffed_wheat`(2) `blast_carrot`(3) `blast_carrot.nausea`(2) `ubi_server`(2) `toxic_ubi_server`(4) `melon_slice_bomb`(2) `melon_grenade`(6) | 不动 |
| C 炮台与火把花 | 8 | 22 | `pumpkin_turret`(6) `awakened_torchflower`(4) 及其 `strength` / `resistance` / `haste` / `speed` / `water_breathing` / `night_vision`（各 2） | 不动 |
| D 瓶子草 | 4 | 10 | `awakened_pitcher_plant`(4) 及其 `regeneration` / `saturation` / `absorption`（各 2） | 不动 |
| E 陷阱与蘑菇 | 8 | 21 | `fang_trap_cactus`(9) `bamboo_firecracker`(2) `red_mushroom_puff`(1) 及其 `nausea` / `blindness`（各 2）、`brown_mushroom_puff`(1) 及其 `weakness` / `slowness`（各 2） | 不动 |
| F 幸运浆果 | 7 | 24 | `lucky_berry`(6) 及其 `regeneration` / `strength` / `resistance` / `jump_boost` / `speed`（各 2）、`chance`(8) | 不动 |
| G 力量甜菜 | 5 | 10 | `power_beet`(2) 及其 `strength` / `speed` / `haste` / `resistance`（各 2） | 不动 |
| H 其余作物与首领 | 8 | 36 | `molotov`(3) `wild_apple`(2) `wild_hunter`(11) `zangief_cane`(8) `qbounce`(4) `friend_chives`(3) `point_defense_fungus`(3) `vibration_bean`(2) | 不动 |
| I 全局与据点 | 6 | 19 | `growth`(2) `trap`(4) `outpost_core`(4) `cultivator`(2) `trade`(6) `compressed`(1) | 不动 |
| **合计** | **60** | **175** | 7+7+8+4+8+7+5+8+6 = 60 组；12+21+22+10+21+24+10+36+19 = 175 键 | 175 − 0 + 0 |

两处必须点明的现状（实现方不得「顺手补」）：

1. `outpost_core` 是 **4** 键不是 5 键：`farmlandBonus` 的字段声明与被注释的 `.define` 已在 v0.2.4 随 `farmland` 包一并删除（58 号 D-254），故 `outpost_core` 现为 **4** 键（`maxPerPlayer` / `boundaryParticleIntervalTicks` / `storageSlots` / `cultivatorCapacity`，`FarmstormConfig.java:927-944`）；这是 F-192 / D-139 / A-42 撤销耕地之后的进一步清理，不是本版恢复这个键。本版**不恢复**这个键，不新增、不删除任何活跃的 config 键（裁定 #4、红线 2）。
2. `trade` 是 6 键（`tradeCommonMin` / `tradeCommonMax` / `tradeRareMin` / `tradeRareMax` / `tradeEpicMin` / `tradeEpicMax`，`FarmstormConfig.java:242-247`）：需求 2 只改交易的 **xp 值**，价格区间一行不改（58 号 D-237、红线 6）。

§6.2 client 侧 3 键，逐个区块：

| 键 | 落盘形状 | 出处 | 本版 |
| --- | --- | --- | --- |
| `hud.enabled` | `[hud]` 段 `enabled`，布尔，默认 `true` | `FarmstormClientConfig.java:25`、`:32-34` | 不动 |
| `hud.x` | `[hud]` 段 `x`，整数，默认 `4`（`0 ~ 3840`） | `:26`、`:35-37` | 不动 |
| `hud.y` | `[hud]` 段 `y`，整数，默认 `4` | `:27`、`:38-40` | 不动 |

§6.3 不动的三条证明：

1. **改动面反证**：58 号 §3.1 / §3.2 的 23 处没有一处落在 `config/FarmstormConfig.java` 或 `config/FarmstormClientConfig.java`；`config/` 下唯一的改动是删掉 `farmlandBonus` 的死字段与四行被注释的 `.define`（58 号 D-254，删掉的都不是活跃键）⇒ 175 − 0 + 0、3 − 0 + 0。需求 3 按裁定 #4 **硬编码** `0x4D000000`（`CoreHudRenderer.java:40`），不加 `hud.opacity` 之类的新键。
2. **落盘读数**：`run/config/farmstorm-server.toml` 现存 175 行 `键 = 值`、60 个 `[段]`（`Select-String -Pattern "^\[" ` 命中 60）。这两数本版前后必须相等。
3. **检查器不动**：`tools/verify_project.py` 的 `EXPECTED` / `EXPECTED_COUNTS` 与期望键集（`:492-543` 的并集现算）一律不改，故 WARN 1（6 个 config 键的文档-源码形状偏差，BRIEF §7 第 5 条）保持原状、FAIL 仍 0。

---

## §7 语言键

§7.1 321 键的构成（`ModLangProvider.java:14-16` 的按组复核，本会话用两个语言文件实点核对）：

| 组 | 键数 | 算式 |
| --- | --- | --- |
| `item.` | 28 | 28 − 0 + 0 |
| `block.` | 28 | 28 − 0 + 0 |
| `advancements.` | 32 | 32 − 0 + 0 |
| `farmstorm.configuration.` | 178 | 175 + 3（server + client） |
| `entity.farmstorm.` | 13 | 13 − 0 + 0 |
| 其余（effect 4 / subtitles 3 / death 6 / tooltip 20 / villager 1 / gui 1 / container 1 / itemGroup 1 / key 2 / command 3） | 42 | 4+3+6+20+1+1+1+1+2+3 = 42 |
| **合计** | **321** | 28+28+32+178+13+42 = 321 |

§7.2 三条证明：

1. **两份文件同键集**：`src/generated/resources/assets/farmstorm/lang/en_us.json` 与 `zh_cn.json` 各 **321** 个顶层键（本会话实点，两数相等）。键集一致由构造方式保证：两种语言从同一张 `ENTRIES` 表按列写盘（`ModLangProvider.java:12-13`）。
2. **没有新增键的动因**：本版不新增注册项（§2 的 92）、不新增 config 键（§6 的 175 + 3）、不改任何物品/方块/进度的注册名（58 号 §3.3 删除项 0）。语言键由注册名与 config 键派生，前两者零变化 ⇒ 321 − 0 + 0。
3. **没有删除与改名的动因**：本版删掉的只有 `farmland` 两个类（58 号 D-253，两者都不携带语言键），注册项与活跃 config 键一个不删（58 号 §3.3）；唯一改了文字的地方是 `util/FarmstormTags.java:29` 的**代码注释**（D-248），注释不进语言文件。

§7.3 验收读数：两个 json 的顶层键数各 321，且 `farmstorm.configuration.` 前缀命中 178（175 + 3）。

---

## §8 验收标准

25 条（裁定 #7 的下限是 20）。每条给三样东西：怎么做 / 期望看到什么 / 依据；判据只用计数、日志行、`/data get` 读数、源码 grep 命中、像素比对五类。

| # | 怎么做 | 期望看到什么 | 依据 |
| --- | --- | --- | --- |
| 1 | `python -X utf8 tools\verify_project.py` | 末段 FAIL 0 / WARN 1；`== 1. config` 段打印的生成 toml 键数 = 175 | 58 号 D-227 |
| 2 | 起专用服务器，grep 日志 `自检 注册对象` | 恰 1 行，含 `92 个`、十二类数值齐全、行尾 `OK` | `Farmstorm.java:180-188` |
| 3 | `(Get-ChildItem -Recurse src/main/java -Filter *.java).Count`；`grep -rn "farmland" src/main/java`（**区分大小写**，不加 `-i` —— `registry/FarmstormVillagerProfessions.java:31` 的 javadoc 引用了原版职业常量 `VILLAGER_FARMLANDS`，含 `FARMLAND`，加 `-i` 会变成 3 命中，与本版无关）；`grep -rn "outpostCoreFarmlandBonus" src/main/java` | 计数输出 `151`；第一个 grep 命中恰 **2** 行，都在 `datagen/ModLangProvider.java` 的 `:39` 与 `:40`；第二个 grep 命中 **0** | §1.2（152 − 2 + 1）；58 号 D-253、D-254 |
| 4 | grep `CoreBound.java` 的三个方法签名 | `BlockPos corePos();` / `void setCore(BlockPos pos);` / `void clearCore();` 各命中 1，第四个方法命中 0 | 58 号 D-228 |
| 5 | grep `CropConverterBlockEntity.java` 的 `private void syncCore` 与 `sendBlockUpdated` | 前者命中 1、后者命中 1、`Block.UPDATE_CLIENTS` 命中 2 | 58 号 D-247 |
| 6 | grep `OutpostCoreBlockEntity.java` 的 `AreaBlockEntities.collect` 与 `CultivatorBlockEntity.class` | 前者命中 1、后者命中 0 | 58 号 D-230 |
| 7 | grep `rebindNearby` 与 `settleOnRemoval` 两方法体内的 `instanceof CultivatorBlockEntity` | 命中 0 | 58 号 D-232、D-233 |
| 8 | grep `KIND_BY_BLOCK` 与其中的两个 `Link.Kind` 值 | `KIND_BY_BLOCK` 命中 1；映射行里 `CULTIVATOR`、`CROP_CONVERTER` 各 1 | 58 号 D-235 |
| 9 | grep `OutpostCoreBlockEntity.java` 的 `clearCore()` 调用点与四处文案（`:859` 注释、`:863`、`:867`、`:872`） | 调用点命中 1；`:872` 旧串「迁移失败：培养器留原地并标为未绑定」命中 0；`:867` 旧串「迁移失败且该格已不是培养器」命中 0、新串「迁移失败且该格已不是处理方块」命中 1；`:859` 行内「培养器」命中 0；`:863` 的「迁移失败且区块未加载」仍命中 1 | 58 号 D-233；61 号 A-108 |
| 10 | 拆掉转换器所绑的核，`/data get block <转换器坐标> Core` | 读数报告该键不存在（未绑定），且 `Held` / `Progress` 仍在 | 58 号 D-233、`CropConverterBlockEntity.java:270-272` |
| 11 | 在 3 × 3 区块内放一个新核，等一个补绑周期 | 日志出现 1 行 `补绑：核 ... 本轮新增 1 条`，且行内 `累计` 增 1 | `OutpostCoreBlockEntity.java:646-649` |
| 12 | 串起第 10 条与第 11 条，再 `/data get block <转换器坐标> Core` | 读数等于新核坐标（一条 `Core` 长整数），三处读数顺序可复查 | 58 号 D-233（D → A → B） |
| 13 | grep `VillagerTradeEvents.java` 的五个常量 | `XP_LEVEL_1 = 2` / `XP_LEVEL_2 = 5` / `XP_LEVEL_3 = 10` / `XP_LEVEL_4 = 15` / `XP_LEVEL_5 = 30` 各命中 1 | 58 号 D-236 |
| 14 | grep `BotanistTrade.java` 里五个常量的引用次数 | `XP_LEVEL_1` 6 次、`XP_LEVEL_2` 6 次、`XP_LEVEL_3` 6 次、`XP_LEVEL_4` 4 次，相加 22 | 58 号 D-237 |
| 15 | grep `BotanistEpicTrade.java` 的 `VILLAGER_XP` 与字面量 `= 2` | `VILLAGER_XP = VillagerTradeEvents.XP_LEVEL_5` 命中 1；该类里 `= 2` 的常量命中 0 | 58 号 D-238 |
| 16 | grep `VILLAGER_XP` 在三类里的命中数 | `BotanistTrade` 0、`TierTrade` 0、`BotanistEpicTrade` 2（声明 + 引用） | 58 号 D-237、D-238 |
| 17 | `/data get entity <村民> Offers`，按 `xp:` 数值分组计数 | `xp:2` 7 项、`xp:5` 6 项、`xp:10` 7 项、`xp:15` 4 项、`xp:30` 2 项，合计 26 | 58 号 D-239；§4.2.5 |
| 18 | grep `VillagerTradeEvents.java` 的四个切片与三个挂载点 | `subList(0, 6)` / `(6, 12)` / `(12, 18)` / `(18, 22)` 各 1；`get(1)` / `get(3)` / `get(5)` 各 1 | 58 号 D-239 |
| 19 | grep `CoreHudRenderer.java` 的三个颜色常量 | `0x4D000000` 命中 1、`0x80000000` 命中 0、`0xFF000000` 命中 1 | 58 号 D-241、D-242 |
| 20 | 面板截图取色（内层 1 px 与边框 1 px） | 内层像素 alpha = 77（`0x4D`）、边框像素 alpha = 255（`0xFF`） | 58 号 D-241、D-242 |
| 21 | grep 三个 `block/` 文件的客户端分支与 `canInsert` | 三处 `if (level.isClientSide)` 后紧跟 `convertible` / `admissible` / `BlockItem` 三选一；**客户端分支内** `canInsert` 命中 0（全文件 `CropConverterBlock` 与 `CultivatorBlock` 各留 **1** 处服务端调用，`OutpostCoreBlock` 为 **0**） | 58 号 D-243、D-244、D-245 |
| 22 | `git status --porcelain` 全量过一遍 | 出现的路径恰 **19** 个 `.java`，三段构成：**14** = 本版计划面（12 改 + 1 新增 `blockentity/CoreBound.java` + 1 只加方法 `util/AreaBlockEntities.java`，58 号 D-228 ~ D-248，落点见 D-228 / D-247）；**3** = 删除批次带出的连带改动（`config/FarmstormConfig` / `event/FarmstormGameEvents` / `outpost/OutpostCoreIndex`，58 号 D-254）；**2** = 删除（`farmland/FarmlandGuard` / `farmland/FarmlandLedger`，58 号 D-253）；14 + 3 + 2 = 19，19 个路径全部以 `src/main/java/com/dashpig/farmstorm/` 开头；`src/main/resources/` 与 `src/generated/resources/` 命中 0 | §3.3；58 号 D-228、D-247、D-253、D-254 |
| 23 | grep `FarmstormNetwork.java` | `PROTOCOL_VERSION = "3"` 命中 1、`.playToClient(` 命中 2 | §5.3 |
| 24 | 计数配置与语言键 | `run/config/farmstorm-server.toml` 的 `[段]` 60 个、`键 = 值` 175 行；两份 lang 各 321 键、`farmstorm.configuration.` 178 键 | §6.3、§7.3 |
| 25 | 双客户端录屏，A 手持泥土右键培养器顶面 | A 侧音频轨 `block.anvil.place` 命中 1 次、B 侧命中 1 次；两侧 `latest.log` 无播放失败行 | 58 号 D-243；未实测项见 RT-88 |

§8 的两条读法：① 第 22 条的 **19** 个路径 = 计划面 14（58 号 §3.2 的 17 处修改去重后的 12 个文件 + §3.1 的第 1 项 `CoreBound.java`（新增）+ 第 4 项 `util/AreaBlockEntities.java`（只加一个重载，其余四个新增物件是方法/常量/映射，落在上面 12 个文件里）：12 + 1 + 1 = 14）+ 删除批次的连带改动 3（`FarmstormConfig` / `FarmstormGameEvents` / `OutpostCoreIndex`，D-254）+ 删除 2（两个 `farmland` 类，D-253）：14 + 3 + 2 = 19；出现第 20 个路径就停下来查是不是越界。② 第 17 条的计数不依赖面板，只依赖 `Offers` 的读数，因此服务端单人即可复现。

第 9 条的扩项（61 号 A-108，与 61 号 F-317 第 7 条同源）：出口③ 的两行随同一批改 —— `:867` 的日志串改成「迁移失败且该格已不是处理方块：保留 NBT 原值 {}」（判据改按 `processing_block` 标签走之后，「已不是培养器」在转换器那一格上是事实错误，与 58 号 D-248 订正 `FarmstormTags.java:29` 同性质）；`:859` 的解释性注释同步去掉「培养器」字样。`:863` 的「迁移失败且区块未加载」**不许动**：它讲的是「区块不在内存」这一路，与标签无关，改它会把两种失败原因混成一种口径。上面三个新读数与第 9 条同表同批跑，条数仍是 25 条——这是第 9 条的读数扩项，不是新增第 26 条。

---

## §9 风险 RT-xx

本文件只发 RT 系列，从 **RT-83** 起连续（58 号 §0.1）。

#### RT-83 · 客户端配方表可用性只有静态链

- **风险**：`convertible` 与 `admissible`（经 `outputProfileOf`）在客户端走 `level.getRecipeManager()`，若客户端配方表为空，两个谓词恒假 ⇒ 客户端一律 `PASS`。
- **触发条件**：客户端尚未收到 `ClientboundUpdateRecipesPacket` 时右键（`ClientPacketListener.java:1500` 之前）。
- **后果**：该窗口内的右键退回 0.2.3 的表现（本地音效缺失），不产生错误方块。
- **规避**：静态依据已核（`ClientLevel.java:609-610`）；进游戏实测由第 25 条承担，未通过前不得写成已验证（D-252）。

#### RT-84 · 客户端判据与服务端裁决的落差

- **风险**：客户端只看物品品类，服务端还看范围 / 绑定 / 空槽（`CropConverterBlockEntity.java:118-120`、`CultivatorBlockEntity.java:152-154`）。
- **触发条件**：手持输入物但机器已满或未绑定。
- **后果**：客户端 `SUCCESS`、服务端 `PASS` ⇒ 玩家看到「无反应」（§4.4.5 情形 2）。
- **规避**：写成已知影响，不作为缺陷或回归（58 号 D-251）；不把 `canInsert` 搬上客户端（D-244）。

#### RT-85 · 己方手持方块右键据点核心的预测与开界面落差

- **风险**：客户端按 D-245 返回 `PASS` ⇒ 走 `MultiPlayerGameMode.java:348-358` 的放置预测；服务端仍在 `useWithoutItem` 开界面（`OutpostCoreBlock.java:134-142`）。
- **触发条件**：己方玩家手持 `BlockItem` 右键己方核心。
- **后果**：界面打开，同时客户端预测出的方块被服务端的方块更新回滚（一次视觉闪动），方块不会真的放下。
- **规避**：写成已知影响（58 号 D-245 补充、§8 第 3 条）；若作者改判「空手才 `SUCCESS`」，改的仍是同一处（D-245）。

#### RT-86 · 新标签成员没登记 kind 映射

- **风险**：`CoreBound` 不承担类型（D-234），kind 只在 `KIND_BY_BLOCK` 里。
- **触发条件**：给 `farmstorm:processing_block` 加第三方方块而未同步映射表与 `ModBlockTagsProvider.java:44-47`。
- **后果**：该方块会被补绑发现，但取到 `null` kind ⇒ 只被发现、不绑定，且日志不增行（静默）。
- **规避**：§4.1.6 的三条约束 + 第 8 条；两处同改写进实现注释（58 号 D-235 补充）。

#### RT-87 · 转换器的 `syncCore` 只写调用不写方法

- **风险**：把 D-247 读成「补一行 `this.syncCore();`」，而该类现无此方法（全仓唯一定义在 `CultivatorBlockEntity.java:104-110`）。
- **触发条件**：只改 `setCore` 方法体、不新增私有方法。
- **后果**：编译失败（`cannot find symbol`），属硬失败、不会静默。
- **规避**：§4.1.3 直接给三段代码；第 5 条用 grep 双计数（定义 1 + `sendBlockUpdated` 1）。

#### RT-88 · 「只有放置者听不见」与「副手放置」两条现象未实测

- **风险**：净现象由两条播放路径推出（`ServerLevel.java:994-1004` 排除放置者、`ClientLevel.java:527-529` 只给本人），未双客户端实测。
- **触发条件**：单人环境只验一侧时，容易把「服务端广播有效」当成修复完成。
- **后果**：误判为已修复，或把副手放置当成本版引入的问题（副手不满足 `ServerPlayerGameMode.java:369` 的 `MAIN_HAND` 判定，推测本来就有音效）。
- **规避**：第 25 条规定双客户端双音频轨计数；两项都按未实测登记（58 号 D-252）。

#### RT-89 · xp 常量落点错位留下第二份真相

- **风险**：五个常量写成 `private`（三个交易类引用不到，只能各自抄一份），或保留 `BotanistTrade` / `TierTrade` 的旧 `VILLAGER_XP`。
- **触发条件**：把 D-236 与 D-238 读成一件事，或只加 `xp` 分量忘了删类常量。
- **后果**：同一等级出现两个值，改一处忘一处；`BotanistEpicTrade.java:28` 的 javadoc 还会引用被删符号。
- **规避**：§4.2.1 定可见性为 `public static final`（右值字面量 ⇒ 编译期常量）；第 15、16 条两个 grep 判据；`:28` 同批重写（§4.2.4）。

#### RT-90 · `NORMAL` 的 22 项顺序被顺手重排

- **风险**：加 `xp` 分量时按等级重排 22 行，等级与物品的对应关系整体错位。
- **触发条件**：把「按等级分档」误解为「按等级重排」。
- **后果**：每一级的商品集合全变（等级由 `subList` 位置决定，`VillagerTradeEvents.java:44-47`），需求 2 从「改经验」变成「改商品」。
- **规避**：红线 4 + 第 18 条的四个切片计数；`xp` 按位置逐项写死（§4.2.2）。

#### RT-91 · 把静态读链或 datagen 的 mtime 当结论

- **风险**：① 把 §4.4.1 的读链当实测结果转述；② 重跑 datagen 后按文件时间戳判断「产物变了」。
- **触发条件**：只读代码不下服务器；或用 `git status` 的时间列而非内容哈希做判断。
- **后果**：① 下游把未实测项写成已验证（D-252 的禁令）；② `src/generated/resources` 下文件 mtime 全变，误判为资源面改动。
- **规避**：第 22 条只看路径不看时间；标签产物按**内容哈希**比对（§3.2 第 2 条）。

---

## §10 与上版差异表

| # | 项 | v0.2.3 | v0.2.4 | 依据 |
| --- | --- | --- | --- | --- |
| 1 | Java 文件数 | 152 | **151**（152 − 2 + 1） | 58 号 D-227、D-253 |
| 2 | 绑定契约 | 两个方块实体各写一套 | `blockentity/CoreBound` 三方法接口 | D-228、D-229 |
| 3 | 候选发现 | 两次按 `Class` 枚举（`OutpostCoreBlockEntity.java:604-608`） | 一次按 `farmstorm:processing_block` 标签 | D-230、D-231 |
| 4 | kind 来源 | 两处成对 `instanceof` 分支 | `KIND_BY_BLOCK` 单一映射 | D-234、D-235 |
| 5 | 清理出口 | 只判培养器（`OutpostCoreBlockEntity.java:870-873`） | 按 `CoreBound` 调 `clearCore()`，覆盖两类 | D-233 |
| 6 | 转换器同步 | `setCore` 只写字段 + `setChanged()` | 新增 `syncCore()`，`setCore` / `clearCore` 都调 | D-247 |
| 7 | 成交经验 | 26 条 listing 全部 2 | 2 / 5 / 10 / 15 / 30 五档 | D-236 ~ D-239 |
| 8 | 经验载体 | 三个类常量 `VILLAGER_XP = 2` | `BotanistTrade` / `TierTrade` 逐项携带 `xp`；史诗类常量引用 `XP_LEVEL_5` | D-237、D-238 |
| 9 | 浮窗底色 | `COLOR_BACKGROUND = 0x80000000`（50.2%） | `0x4D000000`（30.2%） | D-241、D-242 |
| 10 | 三处 `useWithoutItem` 客户端分支 | 无条件 `SUCCESS` | 手持输入物才 `SUCCESS`；核改为手持方块让路 | D-243、D-244、D-245 |
| 11 | `FarmstormTags.java:29` 注释 | 「本版唯一成员是培养器，converter 不进（裁定 #5）」 | 「2 个成员（培养器 + 农作物转换器）」，指向 D-194 与标签产物 | D-248 |
| 12 | `farmland` 两个类（780 行） | `farmland/FarmlandGuard.java` 与 `farmland/FarmlandLedger.java` 在位 | 已删除（225 + 555 = 780 行，空目录一并删除） | 58 号 D-253 |

§10 的读法：第 2 ~ 6 行属于同一条需求的实现面，第 7 ~ 8 行属于交易经验，第 9 行属于浮窗，第 10 行属于音效，第 11 行是附带修复，第 12 行是死代码清理（删 `farmland` 两个类）；注册面、协议、配置、语言键四行**不在差异表里**——它们本版零变化（§2 / §5 / §6 / §7 各给证明）。

---

**收尾自检**：十节齐全、节序 §1 → §10 一致；RT 编号从 **RT-83** 到 **RT-91** 连续九条、无跳号；验收 **25** 条 ≥ 20（裁定 #7），编号 1 ~ 25 且每条三栏（怎么做 / 期望看到什么 / 依据）齐全；差异表 **12** 行 ≥ 8；表格首尾有 `|` 且逐行列数一致（共 20 张：§1.1 / §2.1 / §3.1 / §4.1.2 / §4.1.5 / §4.1.8 两张 / §4.1.9 / §4.2.2 / §4.2.3 / §4.2.4 / §4.2.5 / §4.4.3 / §4.4.5 / §5.1 / §6.1 / §6.2 / §7.1 / §8 / §10；§1.2、§2.2、§2.3、§6.3、§7.2、§9 无表）；数字全部是加减法或两数算术（152 − 2 + 1 = 151、92 − 0 + 0 = 92、175 = 12+21+22+10+21+24+10+36+19、321 = 28+28+32+178+13+42、0.30 × 255 = 76.5 ≈ 77 = `0x4D`）；每条断言带 `文件:行号` 或 D 编号；结论用祈使句；已知影响成文（RT-84、RT-85、RT-86）；只引上游、未改任何既有文件。

