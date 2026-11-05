# F-283 · 48 号 §8 的 30 条验收（v0.2.2 / Phase 53 / W07）

> 执行者：W07 执行子代理。HEAD `c382af5`。工作目录 `D:\project_farm`（Windows / pwsh 5.1）。
> 判据来源：`docs/zh/48-TECH-0.2.2.md` §8 的 30 条（表头 + 30 行，一条不漏）。
> 取证纪律：D-43（`compileJava --rerun` 的 exit code）/ D-27（构建产物读数放锁内）/ D-20（词边界）/ D-18·A-85（不可满足的断言收敛成白名单）/ D-25（`point_of_interest_type`）/ D-41·D-47（config 用「行首 live」= 176）/ D-44（`getOffer` 按语义）/ D-34（注册行序只静态验）/ D-37（`299` 只许出现在沿革段）。
> 本 Phase **不改码**：验收不通过就如实记失败，不改判据。

| 编号 | 执行动作 | 读数（原文） | 判定 | 证据 |
|---|---|---|---|---|
| 1 | 跑 `run-w07` 的专用服务端，读启动自检行；另数 `src/main/java` 的 Java 文件 | 日志原文 `[20:02:09] [modloading-worker-0/INFO] [farmstorm/]: 自检 注册对象 98 个：items 34 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`；`src/main/java` 下 `*.java` = **154** | 通过 | `.work/_w07_srv_a.log`；`Get-ChildItem -Recurse -File src\main\java -Filter *.java \| Measure-Object` = 154（与 49 号「149 + 5 个新类」自洽） |
| 2 | 跑 `tools/verify_project.py`（`-X utf8`，runtimes python） | `文档口径键数: 176` / `生成 toml 键数: 176` / `[OK] 176 个配置键与文档逐字一致（toml 176 键）` / `已核 registry 对象: 98 / 98` / `[OK] lang 键集一致，共 340 键` / `结论：FAIL 0 项，WARN 1 项` | 通过 | 工具输出原文。WARN 是既存的 6 条 `trade.*` 逻辑路径 vs 落盘组内拼写（本版未引入，§4.2 已登记） |
| 3 | 读 `verify_project.py` 的 `EXPECTED` / `EXPECTED_COUNTS` 两处 | `:335    "items": CROP_IDS + [` … `:342        "plain_chives",  # v0.2.2 的平凡农作物（F-268 / D-193；非作物，没有 CropProfile）`；`:381    "items": 34,`；两处同属 `8407d74`（F-268）一个提交 | 通过 | `git show --stat 8407d74` = `tools/verify_project.py \| 13 ++++++++-----`。**行号偏差登记**：48 号写 `:334`/`:378`，实测落点 `:342`/`:381`（`EXPECTED = {` 在 `:334`、`EXPECTED_COUNTS = {` 在 `:380`） |
| 4 | 反向：`git diff 214b2bf..HEAD -- tools/verify_project.py`（214b2bf = v0.2.1 签字提交） | 全 diff 仅 5 个 hunk：docstring `97 → 98`、`+ "plain_chives"`、`"items": 33 → 34`、两处 print 串 `97 → 98`。`expected_config_keys*` 七个函数（`:63/:81/:89/:97/:140/:185/:240`）**一行未进 diff**；`added7` 的断言行（`:449 overlap7 = set(added7) & before7` / `:450 union = before7 \| set(added7)`）**行数与内容均未变** | 通过 | diff 原文。config 期望面零改动 = 48 号 §4.4 的方向 |
| 5 | `src/` 全树 grep `@Mixin` / `*.mixins.json`；读 `FarmstormNetwork` | `@Mixin` = **0**；`src/` 下 `*.mixins.json` = **0**（全仓另有 `.scratch/p25a/src/neoforge.mixins.json` 实验残留 1 件，不属 `src/` 口径）；`event.registrar(PROTOCOL_VERSION)` 之下两条 `.playToClient(...)`：`FarmstormNetwork:38` `CoreListPayload.TYPE`、`:43` `CultivatorLinkPayload.TYPE` = **2** 处；`:25 public static final String PROTOCOL_VERSION = "2";` | 通过 | 源码 grep（词边界 `@Mixin\b`）+ 启动日志两条 `自检 网络包注册 … （版本 2 …）→ OK` |
| 6 | 数 `FarmstormTags` 的 `TagKey`；读 `processing_block` 产物 | `TagKey` = **7** = Block 4（`EXPLOSION_IMMUNE` / `BLAST_PROOF` / `DEPLOYED_CROPS` / `PROCESSING_BLOCK`）+ Item 3（`MODIFIED_CROPS` / `SEEDS` / `CONVERTER_FUEL`）；`data/farmstorm/tags/block/processing_block.json` = `{"values":["farmstorm:cultivator"]}` —— **只有 1 个成员** | 通过 | `util/FarmstormTags.java:30`（`PROCESSING_BLOCK = block("processing_block")`）、`:39`（`CONVERTER_FUEL`）；产物原文 |
| 7 | 反向：converter 是否进了 `processing_block`；converter 全套是否被碰过 | `processing_block.json` 成员只有 `farmstorm:cultivator`，**不含** `farmstorm:converter`；`git diff 214b2bf..HEAD -- '*onverter*'` = **0 行**（`ConverterBlock/BlockEntity/Menu/Recipe/RecipeBookCategory/RecipeSerializer/Screen` 七个类逐字未动）；`ModBlockTagsProvider:45 tag(FarmstormTags.PROCESSING_BLOCK).add(FarmstormBlocks.CULTIVATOR.get());` 只加培养器 | 通过（静态面）；「进游戏试转换」需玩家 ⇒ 未执行 | 标签产物 + 零 diff。`/give` 与配方书本身未被本版改动 ⇒ 「照旧」由零 diff 承担 |
| 8 | 两张新表是否显式登记且不进 98 | `registry/FarmstormPoiTypes:32-34` `REGISTER.register("botanist", () -> new PoiType(Set.of(FarmstormBlocks.OUTPOST_CORE.get().defaultBlockState()), 1, 1))` = **1 个 `PoiType`**；`registry/FarmstormVillagerProfessions:37` `REGISTER.register("botanist", () -> new VillagerProfession(...))` = **1 个职业**；`Farmstorm.java` 构造器新增**恰 2 行**：`:66 FarmstormPoiTypes.REGISTER.register(modBus);` / `:67 FarmstormVillagerProfessions.REGISTER.register(modBus);`；48 号 §1.3 存在，并写明「不计入 98 / 工具既不核存在也不核计数」 | 通过 | 源码 + `git diff` 只有这 2 行 `.register(modBus)`（另 2 行是 import，D-23 已登记） |
| 9 | 老 toml 期望红：把升级前的 `run/config/farmstorm-server.toml`（2 / 16）拷进 `run-w07/config/` 后重启 | 重启后 `run-w07/config/farmstorm-server.toml` 仍是 `:753\tmaxPerPlayer = 2` / `:765\tcultivatorCapacity = 16`；键数仍 **176**（NeoForge 只补键/删键，不改已存在键的值） | 通过（**期望中的红**） | `Copy-Item run\config\farmstorm-server.toml → run-w07\config\` 后重启（`.work/_w07_srv_b.log`）；读数为 toml 原文。与 #10 **成对**：同一台机器、同一份 mod，唯一差别是「toml 是否已存在」 |
| 10 | 新 config 的绿：全新 `run-w07`（无 toml）起服后读重生成的 toml | `run-w07/config/farmstorm-server.toml`（首轮全新生成，805 行）：`:753	maxPerPlayer = 4`、`:765	cultivatorCapacity = 24`；键数 = **176**（行首 `[A-Za-z_]…=` 口径）；**段数 = 60** = 顶层 `^[` 表 **36** + 缩进的点号子表 `^\s+[` **24**（`[blast_carrot.nausea]` / `[awakened_torchflower.strength]` …）= **60**，与源码 `b.push(` = `b.pop()` = **60** 一一对上 | 通过 | 生成产物原文 + `FarmstormConfig.java:943 .defineInRange("maxPerPlayer", 4, 1, 64)` / `:956 .defineInRange("cultivatorCapacity", 24, 1, 64)`。**口径说明**：「段数 60」= toml 的**全部表头**（顶层 36 + 缩进子表 24），不是「只有 36 组」；`run/config` 那份旧 toml 也是 36 + 24 + 176 键，两份同构 ⇒ 结构由源码 `push/pop` 决定、与默认值无关 |
| 11 | 读两份 `lang/*.json` 的键集（`-Encoding UTF8`，按行匹配） | `zh = 340` / `en = 340` / `zh-only = 0` / `en-only = 0`；`item.farmstorm.plain_chives` 与 `entity.minecraft.villager.farmstorm.botanist` 两份**都在**；`farmstorm.configuration.* = 179`、`tooltip.farmstorm.* = 20` | 通过 | 两份 json 键集比对；服务端把村民显示成 `Botanist` 也是职业名键已生效的旁证 |
| 12 | POI 类型的直接读数（`PoiTypes.hasPoi(outpost_core.defaultBlockState())`） | 无命令可读 POI 注册表；`/data get block` 只出方块实体 NBT。按 48 号 §8 #12 的括注「若不可用则只按 #13 判」降级。代码层：`FarmstormPoiTypes:34` 的匹配集是 `Set.of(OUTPOST_CORE.get().defaultBlockState())` 单例、`maxTickets = 1`、`validRange = 1` | 【未验证】+ 代码层证据 | 降级理由原文（48 号 §8 #12 括注）；`FarmstormPoiTypes.java:32-34` |
| 13 | 放 1 个据点核心 + 1 个无业村民，读其职业 | `data get entity @e[tag=p,limit=1] VillagerData` → `{profession: "farmstorm:botanist", level: 1, type: "minecraft:plains"}`；`Brain.memories."minecraft:job_site"` → `{value: {pos: [I; 70, -59, 36], dimension: "minecraft:overworld"}}`（正是我放的那唯一一个核） | 通过 | rcon 读数原文。实体名直接显示为 `Botanist` |
| 14 | 一核一村民：同核旁再拉 1 个无业村民；再放第 2 个核 | (a) 单核 + 两村民 45 秒后：`p` = `farmstorm:botanist` + `job_site [I;70,-59,36]`；`q` = `{profession: "minecraft:none", …}` 且 `job_site` → `Found no elements matching Brain.memories."minecraft:job_site"`（占票失败）。(b) 放第 2 个核 `66,-59,40` 后 50 秒：`q` = `{profession: "farmstorm:botanist", …}`、`job_site` = `{pos: [I; 66, -59, 40]}`（正是新放的那个核） | 通过 | rcon 读数原文（含一次**重跑**：首轮场景里我误留了 2 个核，发现后清场重做，本条只认重跑后的干净读数）。「不会反复原地打转」无直接观测量 ⇒ 以「`q` 无 `job_site` 记忆 ⇒ 无寻路目标」作代码层替代证据 |
| 15 | 有主核也能上班（防「好心加判据」） | 把一个核写成有主：`data merge block 76 -59 76 {ownerUuid:[I;1,2,3,4],ownerName:"W07Tester"}` → `data get block` 回读确认 `ownerUuid: [I; 1, 2, 3, 4], ownerName: "W07Tester"`；40 秒后该核**被认领**：某村民 `job_site = {pos: [I; 76, -59, 76], …}` | 通过 | rcon 读数原文。核的认领不看 `ownerUuid` 是否为空（与 `rebindNearby` 的「无主不补」方向相反，这正是裁定要区分的东西） |
| 16 | 反向（**RT-64 的主形态**）：把 `acquirable_job_site` 那条标签的产物临时移走、重建、重启 ⇒ 同样场景下村民**永不认领** | ① 先记产物 hash：`git hash-object` = **d12c464bca3fca7128d4e279ed7031019518fe0a**，内容 `{ "values": [ "farmstorm:botanist" ] }`；`Move-Item` 到 `$env:TEMP\w07_aqs\`（**不是删除**）。② 构建锁内：`gw.cmd build` = `BUILD SUCCESSFUL in 9s` / `5 actionable tasks: 3 executed` / **EXITCODE=0**（`processResources` + `jar` 都 executed）；`gw.cmd compileJava --rerun` = **EXITCODE=0**。③ 确认产物真的不带它：`build/resources/main/.../acquirable_job_site.json` 存在 = **False**、jar 里 `*acquirable_job_site*` 条目 = **0**（同一次构建里 `data/farmstorm/tags/block/processing_block.json` **仍在** ⇒ 对照组正常，不是构建全崩）。④ 起服放核 `145,-59,145` + 无业村民：**90 秒后** `VillagerData = {profession: "minecraft:none", level: 1, type: "minecraft:plains"}`、`Brain.memories."minecraft:job_site"` → `Found no elements matching`、`potential_job_site` → `Found no elements matching`、`Xp = 0`；随后把村民 `tp` 到核旁 2 格并**再加一个**，再等 **80 秒**：两个村民**仍是** `minecraft:none`、两个都**无** `job_site`、第二个也**无** `potential_job_site`，而 `data get block 145 -59 145` 仍是 `id: "farmstorm:outpost_core"`（核好好地在）。⑤ 立刻 `Move-Item` 移回，`git hash-object` 回读 = **d12c464bca3fca7128d4e279ed7031019518fe0a**（相等）；锁内 `gw.cmd build` = **EXITCODE=0** / `gw.cmd compileJava --rerun` = **EXITCODE=0**，标签回到 `build/resources` 与 jar（条目数 = **1**） | **通过**（反证成立） | **对照**：#13 同一套场景（有标签时）**45 秒内**就认领了；本条 170 秒不认领 ⇒ 标签是**必需**的，不是可选优化。**恢复三证**：`git status --porcelain -- src/generated` = 空、`git hash-object` 相等、`git diff --stat -- src/generated` = 空；全仓 tracked diff = 空。**做法偏离登记**：BRIEF #19 原本写的是「注释掉 `ModPoiTypeTagsProvider` 的 tag 行」，我按调度者授权的等价做法执行——**只移走 datagen 产物、不碰任何 Java 源码**，因此全程 tracked 源码零改动 |
| 17 | 列表长度 22 | `BotanistTrade.java` 里 `new BotanistTrade(` = **22** 处（`:61` ~ `:82`，注释编号 1~22 逐条对齐）；`:60 public static final List<BotanistTrade> NORMAL = List.of(` 收尾于 `:82` | 通过 | 源码 grep `new BotanistTrade\(` = 22。**未用**机械 `random.nextInt` 判据（D-44） |
| 18 | 可见 2 项 + 换一个新植物学家至少一项不同 | `p`：`Offers = {Recipes: [{maxUses: 8, sell {count: 1, id: "minecraft:poisonous_potato"}, buy {count: 5, id: "minecraft:emerald"}, xp: 2}, {sell {count: 1, id: "minecraft:bamboo"}, buy {count: 5, id: "minecraft:emerald"}}]}` = **2 项**；`q`：`[{sell carrot, buy 4 emerald}, {sell beetroot, buy 5 emerald}]` = **2 项**，与 `p` 的两项**全不同** | 通过 | rcon 读数原文。**重启服务端**后读（不走 `/reload`，§0.5 死命令 4） |
| 19 | 史诗固定 1 项（5 级）+ 同物品同价位 + 价格 40~64 | `level: 5` 的植物学家：`Offers = {Recipes: [{maxUses: 1, sell {count: 1, id: "farmstorm:lucky_berry"}, buy {count: 61, id: "minecraft:emerald"}, xp: 2}]}` = **恰 1 项**史诗（`farmstorm:lucky_berry` 是 `CropProfile` 史诗档 4 条之一），价格 **61** ∈ [40, 64]，`maxUses: 1`；跨 2 个补货窗口后重读 `Offers` **逐字相同**（附录 A-1） | 通过 | rcon 读数原文（两次）。**登记**：「一个 5 级植物学家同时持有普通 2 + 史诗 1」需玩家交易涨等级（`increaseMerchantCareer`），无人上线做不到 ⇒ 「补货两次仍同物品同价位」以两次相同读数证明，不以「一次成型」证明 |
| 20 | tooltip 出现（★必须开界面看） | **未执行**：本会话无法启动 Minecraft 客户端、无法做鼠标悬停。代码层证据：`OutpostCoreScreen:48-52` 有 `render` 覆写，第一句 `super.render(...)`、第二句 `this.renderTooltip(graphics, mouseX, mouseY)` | 【未验证】 | 铁律第 8 条（渲染类标【未验证】+ 代码层证据，不编读数）+ D-4（字节码/源码不得冒充悬停读数） |
| 21 | tooltip 的详情行（附魔行 / 效果行 / 最右列不裁切） | **未执行**：同上，需开界面看。本版 `renderTooltip` 走原版 `AbstractContainerScreen` 实现（未覆写、未接管定位）⇒ 「翻到左侧」与原版同码 | 【未验证】 | 铁律第 8 条；47 号 B-20 的「本版不接管定位」同理只能静态说 |
| 22 | 背景与格子像素级对齐（★必须开界面看） | **未执行**：需截图比对。代码层：`renderBg` 两段 blit 的 `h` 分别为 `rows*18+17 = 125` 与 `96`（和 = 221，比 `imageHeight` 222 少 1 px，原版如此）、第二段 `y = topPos + 125` | 【未验证】 | 铁律第 8 条；像素结论须真截图，本会话无客户端 |
| 23 | 两次 blit 的形态 + `render` 覆写 | `OutpostCoreScreen:43` `graphics.blit(BACKGROUND, x, y, 0.0F, 0.0F, IMAGE_WIDTH, upper, TEXTURE_WIDTH, TEXTURE_HEIGHT);`（v = 0）；`:45` `graphics.blit(BACKGROUND, x, y + upper, 0.0F, 126.0F, IMAGE_WIDTH, 96, TEXTURE_WIDTH, TEXTURE_HEIGHT);`（v = 126）；`:41 int upper = OutpostCoreMenu.ROWS * 18 + 17;` ⇒ 第二段 y = `topPos + 125`、高 96。`graphics.blit(` 计 **2** 处；`public void render(` 覆写 **1** 处（`:49-52`），第一句 `super.render(graphics, mouseX, mouseY, partialTick);` | 通过 | 源码逐行原文 |
| 24 | 上限 4（新 config）：第 5 个被拒绝、物品不消耗不掉落 | **未执行**：`maxPerPlayer` 的判定点在放置交互里按玩家 UUID 计数（`OutpostCoreIndex`），专用服务器无人上线 ⇒ 无法构造。配置面已证：`run-w07` 新 toml `maxPerPlayer = 4`（#10） | 【未验证】 | 铁律第 8 条。配置值本身见 #10 的读数 |
| 25 | 绑定 24（新 config）：25 个培养器只绑 24、第 25 个绑不上、已绑的不动 | 有主核 `44,-59,44` + `fill` 落下 5×5 = 25 个培养器；`data get block 44 -59 44 Cultivators` = **24** 条 long。解码（`BlockPos.asLong`）后 = 5×5 网格 25 点里**去掉 `(34,-59,34)`** —— 它的 `distSqr` = **200**，而已绑 24 条里最大的 `distSqr` = **181**（`(35,-59,34)`）⇒ 被裁的正是最远那个。相隔一个补绑周期（100 刻）再读：24 个 long **逐位、同序**相同 | 通过 | rcon 读数原文 + 解码脚本输出。日志另有 `补绑：核 … 本轮新增 N 条（累计 24/24，周期 100 刻）` |
| 26 | 注释已同步：四个串在 `src/` 下的命中 | `默认 16` = **3**（全在 A-85 白名单：`ZangiefPiledriverController:52`「上升速度上限…默认 16 格」/ `WildHunterClientState:15`「loopSoundRange 默认 16.0」/ `FarmstormConfig:841`「向上抬多少格（默认 16）」）；`名额 16` = **0**；`上界 16` = **0**；`× 16` = **0**；`CultivatorLinkPayload:36 public static final int MAX_LINKS = 1024;` | 通过（A-85 白名单口径） | 四个串的 grep 原文。**判据收敛说明**：48 号 #26 字面写「命中 0，它们不含上述四个串」，实测这 3 行**确实含** `默认 16` 且都不是名额语义 ⇒ 按 A-85 / D-29 的先例收敛成「3 处白名单」，理由与 48 号 §1.2 的 `FarmstormConfig:841` 例外同源 |
| 27 | 平凡韭菜拿得到：创造栏可见 + `/give` 可用 | 静态面：`FarmstormItems:131` 注册 `plain_chives`（不走 `cropItem`）、`:149 items.add(PLAIN_CHIVES);` 进创造栏清单、`models/item/plain_chives.json` 产物存在、`textures/item/plain_chives.png` 存在（否则 datagen 的 `basicItem()` 会抛 `Texture … does not exist`，D-11）。**「创造栏里亲眼看到 + `/give` 执行」需客户端/玩家 ⇒ 未执行** | 【未验证】+ 代码层证据 | 铁律第 8 条。注册与产物链已闭合，但「看得见」只能开界面 |
| 28 | 平凡韭菜能吃（2 点、非 alwaysEdible、不能种/喂/合成） | `FarmstormItems:131-135`：`otherProperties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build())` —— **没有** `alwaysEdible()`（那个修饰符只在本文件 `:199` 的 `foodProperties(...)` 里，而平凡韭菜不走那条路）；`modified_crops.json` 无 `plain_chives`、`seeds.json` 无 `plain_chives`、`converter_fuel.json` 只有 `farmstorm:friend_chives`；全仓无 `plain_chives` 配方（`recipe` 产物目录里无同名 json） | 【未验证】+ 静态证据 | 源码 + 三个标签产物原文。**判据拆分说明**：本条一半是静态、一半要玩家，整体按最弱项记【未验证】，静态证据齐全 |
| 29 | 平凡韭菜能被植物学家卖 + 四个数一字不变 | 22 条清单第 **20** 行 = `BotanistTrade.java:80 new BotanistTrade(FarmstormItems.PLAIN_CHIVES.get()),            // 20 本版唯一新造的交易项（D-193）`；启动日志 `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK` | 通过 | 源码 + 日志原文。四个数 22 / 12 / 7 / 3 一字未变，`CropProfile.ALL.size() == 22` 的断言仍在 `Farmstorm.java:128-129` |
| 30 | 贴图真的存在（16×16 PNG）+ `textures/item/` 文件数 30 | `src/main/resources/assets/farmstorm/textures/item/plain_chives.png`：PNG 签名 `\x89PNG\r\n\x1a\n` = True，IHDR 宽高 = **16 × 16**，213 bytes。同目录 `*.png` = **30**（改前 29） | 通过 | 目录计数 + IHDR 解包 |

## 附录 A · 读数底稿（只列判据本身需要、正文放不下的部分）

### A-1 · #19 / B-02 / B-03 的补货不变性：同一村民两次读数（间隔 135 秒 > 2 × 1200 刻）

```
第一次（生成后立刻读）
data get entity @e[tag=lvl5,limit=1] Offers
  Botanist has the following entity data: {Recipes: [{maxUses: 1, sell: {count: 1, id: "farmstorm:lucky_berry"}, buy: {count: 61, id: "minecraft:emerald"}, xp: 2}]}
data get entity @e[tag=p,limit=1] Offers
  Botanist has the following entity data: {Recipes: [{maxUses: 8, sell: {count: 1, id: "minecraft:poisonous_potato"}, buy: {count: 5, id: "minecraft:emerald"}, xp: 2}, {maxUses: 8, sell: {count: 1, id: "minecraft:bamboo"}, buy: {count: 5, id: "minecraft:emerald"}, xp: 2}]}

第二次（135 秒后，已跨两个补货窗口）
data get entity @e[tag=lvl5,limit=1] Offers
  Botanist has the following entity data: {Recipes: [{maxUses: 1, sell: {count: 1, id: "farmstorm:lucky_berry"}, buy: {count: 61, id: "minecraft:emerald"}, xp: 2}]}
data get entity @e[tag=p,limit=1] Offers
  Botanist has the following entity data: {Recipes: [{maxUses: 8, sell: {count: 1, id: "minecraft:poisonous_potato"}, buy: {count: 5, id: "minecraft:emerald"}, xp: 2}, {maxUses: 8, sell: {count: 1, id: "minecraft:bamboo"}, buy: {count: 5, id: "minecraft:emerald"}, xp: 2}]}
```

⇒ 物品、数量、基准价三项两次逐字相同。机制面：补货只走原版 `Villager#restock()` → `MerchantOffer#resetUses()`，**不会**重调 `getOffer`（`BotanistEpicTrade.java:20-23`），所以「同一村民一个存档内只卖一个作物 id、一个基准价」。

### A-2 · #25 的 24 条解码（`BlockPos#asLong`）

```
COUNT=24
POS=[(38,-59,38),(37,-59,38),(38,-59,37),(37,-59,37),(36,-59,38),(38,-59,36),(36,-59,37),(37,-59,36),
     (35,-59,38),(38,-59,35),(36,-59,36),(35,-59,37),(37,-59,35),(34,-59,38),(38,-59,34),(35,-59,36),
     (36,-59,35),(34,-59,37),(37,-59,34),(35,-59,35),(34,-59,36),(36,-59,34),(34,-59,35),(35,-59,34)]
MAX_DIST_SQR=181 AT (35,-59,34)
MISSING=[(34,-59,34)]             # 5×5 网格 25 点里唯一没被绑的
MISSING_DIST=[(200,(34,-59,34))]  # 它的 distSqr = 200 > 已绑最大 181 ⇒ 裁掉的正是最远那个
```

### A-3 · 四条必测组合的执行情况

| 组合 | 状态 | 说明 |
|---|---|---|
| **#9 + #10** | 成对跑完 | 先读全新 run 目录（4 / 24 = 绿），再读拷入旧 toml 的重启（2 / 16 = 期望中的红）。同一机器、同一份 mod，唯一变量是「toml 存不存在」 |
| **#13 + #16** | **成对跑完** | #13 证「有标签 ⇒ 45 秒内认领」；#16 证「把标签产物移走重建后 ⇒ 170 秒、两个村民、紧贴核也**永不认领**」。只跑 #13 分不清「标签生效」与「POI 自动生效」，这一对才把 §2.3.5 那条隐蔽依赖逼出来 |
| **#17 + #18** | 成对跑完 | #17 列表 22（源码计数）；#18 可见 2（`Offers.Recipes` 长度 + 两个村民抽到不同项） |
| **#20 + #22** | **都没跑** | 两者都必须**真的开界面看**，本会话无 GUI 客户端。只给了代码层证据（`render` 覆写存在 / 两段 blit 的 v 与 h），**按 D-4 不得冒充悬停与像素读数** |

### A-4 · 未验证清单与理由（一条不藏）

| 条目 | 为什么没跑 | 已给的替代证据 |
|---|---|---|
| ~~#16~~ | **已补跑，不再是未验证**：按调度者授权，只移走 datagen 产物（不碰 Java 源码）后重建 + 重启 + 观察，反证成立（见 #16 行） | 恢复三证见 #16 行与附录 A-5 |
| #12（POI 直接读数） | 无命令可读 POI 注册表；48 号 §8 #12 括注已允许降级到 #13 | `FarmstormPoiTypes:34` 单例 + `maxTickets=1` |
| #20 / #21 / #22（tooltip / 详情行 / 像素对齐） | 需真人开 54 格界面做悬停与截图比对；本会话无客户端 | `OutpostCoreScreen:48-52` 的 `render` 覆写、`:43/:45` 两段 blit 的 v/h；D-4 明令不得用字节码或源码冒充悬停读数 |
| #24（上限 4 的行为） | `maxPerPlayer` 按玩家 UUID 计数，专用服务器无人上线 ⇒ 无法构造「第 5 个被拒绝」 | 配置值本身已在 #10 验到 `maxPerPlayer = 4` |
| #27（创造栏可见 / `/give`） | 需客户端或在线玩家 | 注册 + 创造栏清单 + 模型产物 + 贴图存在（缺贴图 datagen 会抛 `Texture … does not exist`，D-11） |
| #28 的「吃一个回 2 点 / 饱食满吃不下」 | 需玩家 | `nutrition(2)` / `saturationModifier(0.1F)` / 无 `alwaysEdible()` / 三个标签产物均不含 `plain_chives` |

### A-5 · #16 反证实验的完整链路与恢复证据（本 Phase 唯一一次动仓库内产物）

**授权边界**：只覆盖 #16 这一条；**不许改任何 Java 源码**（实测全程 tracked 源码零改动）。

| 步 | 动作 | 读数 |
|---|---|---|
| 1 | 记 hash：`git hash-object src/generated/resources/data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` | `d12c464bca3fca7128d4e279ed7031019518fe0a`；内容 `{ "values": [ "farmstorm:botanist" ] }`；`git status -- <path>` 空 |
| 2 | `Move-Item` 到 `$env:TEMP\w07_aqs\acquirable_job_site.json`（**不删**） | `EXISTS_AFTER_MOVE=False`；备份 hash 同一串；`git status -- <path>` = ` D ` |
| 3 | 构建锁内 `gw.cmd build` | `BUILD SUCCESSFUL in 9s` / `5 actionable tasks: 3 executed, 2 up-to-date`（`processResources` 与 `jar` 均 executed）/ **EXITCODE=0** |
| 4 | 构建锁内 `gw.cmd compileJava --rerun` | `BUILD SUCCESSFUL in 4s` / `1 executed` / **EXITCODE=0** |
| 5 | 确认产物真不带标签 | `build/resources/main/.../acquirable_job_site.json` = **False**；jar 里 `*acquirable_job_site*` = **0**；**对照**：`processing_block.json` 仍在 ⇒ 不是构建整体失败 |
| 6 | 起服 + 放核 + 无业村民 | 核 `145,-59,145` = `id: "farmstorm:outpost_core"`；村民 `{profession: "minecraft:none"}` |
| 7 | 等 **90 秒** | `minecraft:none`；`job_site` = `Found no elements matching`；`potential_job_site` = `Found no elements matching`；`Xp = 0` |
| 8 | 加强：`tp` 到核旁 2 格 + **再加一个**村民，再等 **80 秒** | 两个村民**都**是 `minecraft:none`、**都无** `job_site`；第二个**也无** `potential_job_site`；核仍在 |
| 9 | **立刻**移回 | `RESTORED_EXISTS=True`；hash = `d12c464bca3fca7128d4e279ed7031019518fe0a`（**与第 1 步相等**）；`git status -- <path>` 空 |
| 10 | 锁内重建 + 重编译 | `gw.cmd build` = `BUILD SUCCESSFUL in 10s` / `3 executed` / **EXITCODE=0**；`compileJava --rerun` = **EXITCODE=0**；标签回到 `build/resources` 与 jar（条目 **1**） |

**三条恢复证据（调度者点名要的）**：

```
[1] git status --porcelain -- src/generated   → EMPTY (clean)
[2] git hash-object <path>                     → d12c464bca3fca7128d4e279ed7031019518fe0a  MATCH=True
[3] git diff --stat -- src/generated            → EMPTY (no diff)
全仓 git status --porcelain（tracked 部分）    → NO TRACKED CHANGES
```

**结论**：`minecraft:acquirable_job_site` 这条标签是村民认领的**必要条件**——移走后 170 秒、两个村民、紧贴核仍不认领；移回并重建后（#13 的读数）45 秒内认领。RT-64 的主形态被证伪。
