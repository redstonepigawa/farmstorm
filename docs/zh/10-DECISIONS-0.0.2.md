# 10 · v0.0.2 决策记录（Decision Record）

> 本文件是「农场风云 / Farmstorm」v0.0.2 的唯一真相源：编号接 `00-DECISIONS.md` 的 D-48，覆盖 D-49 ~ D-79。
> v0.0.1 的既有内容仍以 `00-DECISIONS.md` 为准，本文件不覆盖它；v0.0.2 的新增内容以本文件为准。
> 来源：作者 v0.0.2 需求原文 + 施工事实卡 `.work/v002-BRIEF.md`（§2 决策总表、§3 注册对象增量、§4 数字口径表）。施工事实卡是施工期的摊平稿，不属于交付物。

---

## 1. 本文件的效力与范围

| 项 | 值 |
|---|---|
| 文件 | `docs/zh/10-DECISIONS-0.0.2.md`（英文对应 `docs/en/10-DECISIONS-0.0.2.md`） |
| 版本 | 0.0.2 |
| 决策编号区间 | D-49 – D-79，共 31 条，连续无跳号 |
| 覆盖对象 | 交付与文档、4 条新档案与 5 个新物品、三个侵入性效果、媒体管线、4 条新配方、压缩干草块数值、莫洛托夫落点语义、对破障阈值归属的一处口径澄清（D-78）、输入锁实现手段的一处修订（D-79） |
| 不覆盖对象 | D-01 – D-48 与 C-1 – C-10（继承关系见 §7）；`docs/zh/00`–`09` 与 `docs/en/00`–`09` 的任何内容（D-49 冻结） |
| 冲突裁决 | v0.0.1 既有条款 → `00-DECISIONS.md`；v0.0.2 新增条款 → 本文件 |

- **效力**：本文 31 条一经登记即为 v0.0.2 的最终口径。`11-DESIGN-0.0.2.md`、`12-TECH-0.0.2.md`、`13-TASKS-0.0.2.md`、`14-PROMPT-MAIN-0.0.2.md`、`15-PROMPT-TASKS-0.0.2.md` 与实现阶段的代码都必须与本文一致；冲突时以本文为准。
- **与 v0.0.1 的关系**：v0.0.1 的数值、配方、分类与工程约束一律不改（禁止清单见 §6），因此那些内容仍以 `00-DECISIONS.md` 为准。本文只在 §7 逐条说明 D-01 – D-48 的继承、细化与扩充关系。
- **与施工事实卡的关系**：`.work/v002-BRIEF.md` 负责把作者原文摊成事实卡，本文负责把它落成正式决策行。因此本文的「结论」措辞与数字照事实卡 §2 原样转写，不做改写、不重算；「依据」给编号、源码符号或事实卡章节。
- **依据标注**：事实卡里没有、由本文自行补的内容标「本文补充」。全部补充点只有三处：§4 的中文类别名与枚举常量名对照、§5 的口径澄清展开方式、§7 的 48 行继承表——都不引入新数值。
- **引用写法**：引用既有文档写 `01-DESIGN.md §3.4` 这种带文件名的形式；引用源码写从仓库根起的相对路径。
- **禁用词**：施工事实卡禁止的三类不确定表述（未定事项、待办标记、条件含糊的结论）一律不出现。需要判断而事实卡未直接给出时，按 §3 已有决策推导，并在该处标「本文补充」。

---

## 2. 交付物结构（v0.0.2 文档地图）

中文与英文一一对应，编号一致；术语以 `00-DECISIONS.md` 的 C-10 与本文件 §4 第 6 列（英文名）为准。

```
D:\project_farm\docs\zh\
├── 00-DECISIONS.md              v0.0.1 决策记录（冻结，D-49）
├── 01-DESIGN.md  …  09-IMPL-VS-PLAN.md
│                                 v0.0.1 其余 9 份（冻结，D-49）
├── 10-DECISIONS-0.0.2.md        本文件：D-49 … D-79
├── 11-DESIGN-0.0.2.md           7 个新功能的完整设计规格
├── 12-TECH-0.0.2.md             新增注册对象、类设计、资源清单、三个客户端子系统、风险册
├── 13-TASKS-0.0.2.md            F-60 起任务拆分 + 依赖图 + Milestone + 工作量
├── 14-PROMPT-MAIN-0.0.2.md      一份主提示词
└── 15-PROMPT-TASKS-0.0.2.md     一份分阶段任务清单
```

`docs/en/` 下同结构：`00`–`06` 与 `09` 为 v0.0.1 冻结件，`10`–`15` 为本轮新增。`07-PUBLISH.md` 与 `08-DEV-GUIDE.md` 在 v0.0.1 就没有英文版，本轮补齐与否按 D-49 的口径处理：`10`–`15` 六份必须有中英两版，v0.0.1 缺英文的那两份不在本轮补。

| 文件 | 负责内容 | 本文中定义它的决策 |
|---|---|---|
| `10-DECISIONS-0.0.2.md` | D-49 … D-79 逐条成行；口径澄清节；19 条档案总表；明确不做的事；与 v0.0.1 的继承关系 | 全部 31 条 |
| `11-DESIGN-0.0.2.md` | 7 个新功能的数值表、状态机、边界与异常、config 引用 | D-57（6.0 的后果必须写进设计文档）、D-58、D-59、D-64、D-65、D-70、D-73、D-74 |
| `12-TECH-0.0.2.md` | 新增注册对象逐项表、类设计、资源清单、三个客户端子系统方案、风险登记册 RT-10…RT-16、config 新增键、验收自检 | D-50、D-53、D-60、D-61、D-62、D-66、D-68、D-69、D-76 |
| `13-TASKS-0.0.2.md` | F-60 起任务拆分、依赖图、大纲、工作量、关键路径 | D-51、D-52、D-63、D-67（新进度与新配方的任务归属） |
| `14-PROMPT-MAIN-0.0.2.md` | 一份主提示词 | 全部 31 条 |
| `15-PROMPT-TASKS-0.0.2.md` | 一份分阶段任务清单 | 全部 31 条 |
| `README.md` 索引节 | v0.0.2 文档导航 | D-49、D-66（素材来源与非商业自用声明） |

---

## 3. 决策清单

每条一个小节，字段固定四项：编号 / 结论 / 依据 / 落地位置。「结论」照施工事实卡 §2 原样转写。

### 3.1 交付与文档（D-49 ~ D-50）

#### D-49 · v0.0.1 文档冻结，v0.0.2 另起 6 份

- **编号**：D-49
- **结论**：`docs/zh/00`–`09` 与 `docs/en/00`–`09` 一律不动。v0.0.2 在 `docs/zh/` 与 `docs/en/` 各新增 `10`–`15` 六份，编号延续。README 加索引节。
- **依据**：v0.0.1 已发版，其文档与 jar 是同一版的对应关系，回改旧档会让已发布内容与文档脱节，也会让「哪一版说了什么」无法回答。编号延续而非重新编号，是为保住 `00-DECISIONS.md`、v0.0.1 各份文档与代码注释里对 D-01 – D-48 的既有引用。
- **落地位置**：`docs/zh/10`–`15`、`docs/en/10`–`15`、`README.md` 的 v0.0.2 索引节；分工见本文 §2。

#### D-50 · 仍保持 0 个自定义网络包

- **编号**：D-50
- **结论**：三项侵入性效果（灰度、红色高亮、输入锁）所需的「服务端告诉客户端状态」一律借道**隐藏 MobEffect**：效果存在与否 + amplifier + 剩余时长即完整状态，原版 `ClientboundUpdateMobEffectPacket` 免费同步。禁止为此新增 `CustomPacketPayload`。
- **依据**：C-6 已把「不引入自定义 S2C 网络包」定为硬约束，D-40 又排除了 Mixin，隐藏 `MobEffect` 是唯一能在不加包、不改原版类的前提下把「阶段 / 剩余时长」送到客户端并让客户端本地判断的现成通道。三项效果里灰度与红色高亮只需要「开 / 关 + 剩余时长」，输入锁只需要「开 / 关」，桑吉尔蔗的倒转需要三个相位，`amplifier` 的数量足够覆盖。
- **落地位置**：`12-TECH-0.0.2.md`（三项效果的同步方案与 amplifier 相位表）；`mob_effects` 注册 +2（`wild_hunter`、`zangief_piledriver`）；`client/FarmstormClient.java`（客户端读效果状态并驱动后处理与输入拦截）。

### 3.2 增量内容（D-51 ~ D-58）

#### D-51 · 三个新作物都没有种植形态

- **编号**：D-51
- **结论**：`molotov` / `zangief_cane` / `wild_apple` 的 `CropProfile.cropBlock`、`seedItem` 均为 `null`，不注册作物方块、不注册种子。先例：`melon_slice_bomb`（同样无作物方块）。三者只从转换器产出。
- **依据**：先例 `melon_slice_bomb` 在 v0.0.1 同样是 `cropBlock` 与 `seedItem` 双空，说明档案模型本来就允许「无种植形态」，本条不需要新接口。这三件的获得途径是转换器（D-54 与施工事实卡 §3.2 的三条转换配方），给它们加种植形态反而会让「转换器是这个版本的入口」这一设计失去意义。
- **落地位置**：`util/CropProfile.java`（三条档案的 `cropBlock`、`seedItem` 置 `null`）；`registry/FarmstormItems.java`（不注册种子物品）；`datagen/ModItemModelProvider.java`（物品图标走 `item/generated`）；`12-TECH-0.0.2.md` 的档案表。

#### D-52 · 转换器合成配方

- **编号**：D-52
- **结论**：有序 3×3：中心 `minecraft:crafting_table`，周围八格收物品标签 `farmstorm:converter_fuel`，产出 1 个 `farmstorm:converter`。该标签内容 = 原版农作物 + 本模组全部改良作物（19 条档案里凡是能被当作「农作物」的物品）。v0.0.1 的转换器此前没有任何配方，本配方是**首条**。
- **依据**：v0.0.1 的合成配方数为 0，转换器只有物品与 GUI、没有获得途径；转换配方要能被用上，必须先给转换器一条合成路径。用标签而不是写死物品清单，是为让「原版农作物 + 改良作物」这个集合只维护一处，也便于后续加档时不改配方。
- **落地位置**：`datagen/ModRecipeProvider.java`（有序 3×3 合成，合成配方 +1）；`util/FarmstormTags.java` + `datagen/ModItemTagsProvider.java`（新建 `farmstorm:converter_fuel`，item tags 新建 +1）。

#### D-53 · 媒体管线口径

- **编号**：D-53
- **结论**：Minecraft 声音引擎只解 Ogg Vorbis。三个源文件一律先转换再进 `src/main/resources`：
  - 音频 → **mono, 64 kbps Ogg Vorbis**
  - 效果图标 JPG → **18×18 PNG**，无透明通道要求
  转换脚本随文档交付，缺 `ffmpeg` 时**直接报错退出并打印安装指引**，不许静默跳过、不许回退成原扩展名。
  （本机实测：**已有可用 ffmpeg**——7.1 essentials 构建，带 libvorbis，位于 `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`，但**不在 PATH 上**，运行脚本时要用 `-Ffmpeg` 传绝对路径。另：Trae CN 自带的 6.1.1 是裁剪构建、**无 libvorbis**，不可用。npm/maven/镜像三个源全部不可达，所以脚本不下载任何东西。）
- **依据**：两个音频源是 MP3、图标源是 JPG，直接改名塞进 jar 会在加载期报错而不是静默降级。脚本选择「缺 ffmpeg 直接报错退出」，是因为静默跳过会做出「两个效果没声音、一个效果没图标但构建显示成功」的假通过，与 D-66 的交付要求冲突。
- **落地位置**：`12-TECH-0.0.2.md` 的资源清单与转换脚本说明；转换脚本随文档交付；`assets/farmstorm/sounds.json` 引用的 3 个 Ogg 与效果图标进 `src/main/resources`。

#### D-54 · 狂野苹果转换配方双向

- **编号**：D-54
- **结论**：`minecraft:golden_apple` ⇄ `farmstorm:wild_apple`，经验成本 3，无催化剂。与 v0.0.1 那 13 条双向配方同形（`ConverterRecipe`，`bidirectional=true`）。
- **依据**：D-12 把转换成本定为每次 3 点经验、催化剂只在火把花与瓶子草上需要，本条照此办理，狂野苹果不占下界之星槽。双向的理由与 v0.0.1 那 13 条一致：`ConverterRecipe` 只有一个实现，双向是它的常态形态，单向需要额外配置。转换配方总数因此从 21 变成 24（施工事实卡 §4 的数字口径表）。
- **落地位置**：`datagen/ModRecipeProvider.java`（转换配方 +1，`bidirectional=true`）；`config/FarmstormConfig.java` 的成本键沿用既有转换成本；`12-TECH-0.0.2.md` 的转换配方清单。

#### D-55 · 幸运浆果随机池定死 16 种

- **编号**：D-55
- **结论**：`LUCKY_BERRY_POOL` = 19 条档案中排除 `lucky_berry`、`wild_apple`、`compressed_puffed_wheat` 三件，**结果 16 条**。排除狂野苹果的理由：它是本版最强道具，不该由 1 颗浆果随机产出；排除压缩膨胀麦的理由：它是「二级合成物」，不是改良农作物（D-56）。该池从「`ALL` 减去幸运浆果」的自动推导改为**显式排除三件（两人一物）**。
- **依据**：自动推导式在档案全是「改良农作物」时成立；本轮新增 `wild_apple` 与 `compressed_puffed_wheat` 之后，推导会把两者自动吸进随机池，其中狂野苹果会破坏兑换强度，压缩膨胀麦则根本不是农作物（D-56）。所以推导必须换成显式清单。落到池内容上：19 条档案减去 `lucky_berry`、`wild_apple`、`compressed_puffed_wheat` 三件，得 16 条，与施工事实卡 §4 的「幸运浆果池 14 → 16」对上，也与 `12-TECH-0.0.2.md` §11 #3 的自检文案、`13-TASKS-0.0.2.md` 的 F-62 同一口径。
- **落地位置**：`util/CropProfile.java`（`LUCKY_BERRY_POOL` 改为显式排除三件——该常量定义在 `CropProfile`，`util/LuckyBerryRoller.java` 只消费它，逻辑不改）；`config/FarmstormConfig.java` 无新增键（池内容固定，不进 config）。

#### D-56 · 压缩膨胀麦进档案表

- **编号**：D-56
- **结论**：`compressed_puffed_wheat` 是第 19 条 `CropProfile`，类型 `THROWABLE`、类别 `CONSTRUCTION`，但 `cropBlock` / `seedItem` / `kind` 全空，且**不进转换器配方、不进幸运浆果池**。它属于「二级合成物」，不是「改良农作物」。
- **依据**：它是二级合成物（4 个 `puffed_wheat` 合成 1 个，D-67），原料本身就是改良作物。若它参与转换配方与随机池，会形成「改良作物 → 改良作物」的捷径，并让随机池自我增殖。§4：`CropProfile` 15 → 19、投掷型 8 → 10、建造类 1 → 3。
- **落地位置**：`util/CropProfile.java`（第 19 条档案）；`datagen/ModRecipeProvider.java`（不进转换配方）；`Farmstorm.java` 的启动自检文案（作物 19 种）；`12-TECH-0.0.2.md` 的档案表。

#### D-57 · 压缩干草块数值

- **编号**：D-57
- **结论**：`BlockBehaviour.Properties.of().strength(30.0F, 6.0F).requiresCorrectToolForDrops()`。六面方块、不可旋转（不用 pillar/axis）。材质 = 原版干草块侧面的灰度版。
- **依据**：实测结果——`ANCIENT_DEBRIS` 是 `strength(30.0F, 1200.0F)`，`HAY_BLOCK` 是 `strength(0.5F)`（同一对数字同时管硬度与抗性）。取 6.0 的后果（**必须写进设计文档**）：瓜！与瓜片子（阈值 6.0）**破不动**它（判定是 `resistance < maxResistance`，6.0 不小于 6.0）；育碧服务器与毒育碧服务器（阈值 3.6e6）**破得动**；**原版爆炸（含 TNT）也破得动**——原版公式逐格累积 `f -= (resistance + 0.3) * 0.3`、`f > 0` 即破，TNT 威力 4 时 f 起始区间 [2.8, 5.2]，6.0 抗性挡不住圆石级别的方块。
- **落地位置**：`registry/FarmstormBlocks.java`（`strength(30.0F, 6.0F).requiresCorrectToolForDrops()`，普通 `Block`）；`datagen/ModBlockStateProvider.java`（六面同贴图，无 axis 变体）；`datagen/ModLootTableProvider.java`；`datagen/ModBlockTagsProvider.java`（`minecraft:mineable/pickaxe` 与 `minecraft:needs_diamond_tool` 各追加一处）；`11-DESIGN-0.0.2.md` 必须写出 6.0 对瓜！/瓜片子、育碧服务器/毒育碧服务器与原版爆炸（含 TNT）的后果。

#### D-58 · 莫洛托夫落点语义

- **编号**：D-58
- **结论**：落点触发一次**威力 0** 的爆炸，该爆炸**只当落点触发器**：0 实体伤害、0 方块破坏（原版 `Explosion` 半径 0 时伤害公式得 Infinity，天然无伤）。真正的效果是：以落点为球心、半径 4 格内**每个上表面接触空气的方块**独立掷一次，85% 在其**上方那个空气位**放火，15% 在其**上方那个空气位**放岩浆块。都不替换、不破坏原方块。
- **依据**：原文写的触发依据是「破坏权限只由破障类决定（D-19 铁律），而莫洛托夫的类别是「伤害类、建造类」，没有破障标签」。按 `00-DECISIONS.md` §3，「方块破坏权限只由 `CropCategory.DEMOLITION` 决定」是 **D-48**，D-19 管的是抗性阈值；结论不变，这条依据的编号应按 D-48 读（本文补充：编号指向的更正，不改口径）。
- **落地位置**：`entity/thrown/` 的莫洛托夫投掷物（`entity_types` +2 之一）；`config/FarmstormConfig.java`（`molotov.effectRadius` 4.0 格、`molotov.fireChance` 85.0 %、`molotov.triggerExplosionPower` 0 三个键；余下的百分比自动放岩浆块，不设第二个概率键——施工事实卡 §8 的裁定 A-01）；`11-DESIGN-0.0.2.md` 的莫洛托夫节。

### 3.3 三个侵入性效果（D-59 ~ D-77）

本组编号区间是 D-59 ~ D-77。主线是三项侵入性效果——灰度（D-61）、红色剪影（D-62）、输入锁（D-60）——以及它们落地必须同批解决的表现层、操作层、同步层与内容层：相机（D-59 / D-70）、进度（D-63）、索敌（D-64）、吸附冲刺（D-65）、音频（D-66 / D-76）、桑吉尔蔗本体（D-68 / D-69 / D-71 / D-72 / D-73）、狂野猎人本体（D-74 / D-75）、压缩膨胀麦（D-67 / D-77）。分组只是为了读起来成块，编号与先后顺序仍按事实卡。

| 子系统 | 涉及决策 | 技术落点 |
|---|---|---|
| 画面接管（相机） | D-59、D-70 | `ViewportEvent.ComputeCameraAngles`，只改渲染层角度 |
| 世界灰度（HUD 保持原色） | D-61 | 原版 `PostChain`（`GameRenderer#loadEffect`），世界段末尾执行 |
| 红色剪影 | D-62 | `RenderLivingEvent.Post` 重绘模型 |
| 输入锁 | D-60 | 五个事件各管一段（**已由 D-79 修订为七个机制**） |
| 狂野猎人索敌与位移 | D-64、D-65 | 客户端预测 + 服务端判定 |
| 状态同步 | D-50、D-69 | 隐藏 `MobEffect` 的 amplifier 当相位号 |
| 桑吉尔蔗本体 | D-68、D-71、D-72、D-73 | 部署方块与球形触发判定 |
| 音频 | D-66、D-76 | `sounds.json` + 客户端循环 `SoundInstance` |
| 进度 | D-63 | `ModAdvancementProvider` +3 |
| 压缩膨胀麦 | D-67、D-77 | 无序合成 + 沿用膨胀麦落点写入 |

#### D-59 · 桑吉尔蔗的「上下倒转」= roll + pitch 叠加

- **编号**：D-59
- **结论**：画面绕视线轴滚 180°（roll = 180）**且** pitch 取反，两者同时生效。只做其中一个是错的。
- **依据**：只做 roll 看起来是「画面侧翻」，只做 pitch 取反看起来是「抬头看天花板」，两者叠加才是原文要的「上下倒转」。铁律：只改渲染层角度，不写回 `player.getYRot()` / `getXRot()`，玩家真实朝向不变，服务端权威不破。
- **落地位置**：`client/FarmstormClient.java`（`ViewportEvent.ComputeCameraAngles`）；`12-TECH-0.0.2.md` 的相机节；`11-DESIGN-0.0.2.md` 的效果节。

#### D-60 · 键鼠锁定范围

- **编号**：D-60
- **结论**：锁：移动键、跳跃、攻击、使用/放置、快捷栏切换（1–9 与滚轮）、打开背包（E）、聊天（T / 斜杠）。**只留 ESC**。依据：锁死 ESC 会让玩家以为游戏卡死。
- **依据**：原文给出的唯一理由是「锁死 ESC 会让玩家以为游戏卡死」，因此 ESC 必须始终放行。技术上五个事件各管一段（**已由 D-79 修订为七个机制**，见 §3.5；原口径：移动/跳跃/潜行、攻击/使用、快捷栏 1–9、滚轮、背包/聊天），缺一个就会漏，这条是 RT-13 的评审对象。
- **落地位置**：`client/FarmstormClient.java`（`MovementInputUpdateEvent`、`InputEvent.InteractionKeyMappingTriggered`、`InputEvent.Key`、`InputEvent.MouseScrollingEvent`、`ScreenEvent.Opening`）；`12-TECH-0.0.2.md` 的输入锁节与 RT-13。

#### D-61 · 黑白（世界灰度，HUD 保持原色）

- **编号**：D-61
- **结论**：灰度后处理作用于**整个世界渲染**（方块、实体、粒子），**HUD / GUI 保持原色**。原稿写的是「整屏灰度、含 GUI」，v0.0.2 施工时按 V-56 改口径。
- **依据**：原稿的理由是「只褪世界会留下彩色物品栏与血条」。实际能落地的只有原版 `PostChain`：它的 `process()` 在 `GameRenderer:1027`（`renderLevel` 之后）、GUI 绘制（`GameRenderer:1073`）之前执行，因此天然只灰世界；要含 GUI 只能自己接管 framebuffer 并手写 target 管理，v0.0.2 实测在这条路上拿到的是「灰在了不该灰的时候」（V-48），且 `post_effect/` 目录在 NeoForge 21.1.213 没有加载器（V-49）。世界段的观感已经覆盖灰度效果的主体，故按 V-56 接受「HUD 原色」。
- **落地位置**：`assets/farmstorm/shaders/post/grayscale.json`（`PostChain` 链）+ `shaders/program/grayscale.json` + `shaders/core/grayscale.fsh`；`client/GrayscalePass.java`（`GameRenderer#loadEffect` / `shutdownEffect`）；`assets/farmstorm/post_effect/grayscale.json` 保留为 RT-10 的预留挂载点（当前无加载器，不参与运行）；`12-TECH-0.0.2.md` 的灰度节与 RT-10（禁止用雾色或贴图替换冒充灰度）。

#### D-62 · 红色高亮只画可见的

- **编号**：D-62
- **结论**：只给**视线内可见**（无方块遮挡）且距离 **≤ 64 格**的生物画红色剪影。排除自己与同队伍玩家。不穿墙。
- **依据**：穿墙高亮等于把「你看不见的敌人」也标出来，强度远超原版发光效果；64 格是原文给定的距离上限，两者一起构成强度上限。排除自己与同队伍玩家沿用 D-06 的己方口径。技术上必须让 `LivingEntityRenderer#render(...)` 重画并带重入守卫，不能用 `EntityRenderDispatcher#render`，否则会递归触发自身事件；`MultiBufferSource` 还必须按顶点格式过滤，否则拴绳（`POSITION_COLOR_LIGHTMAP`）会让客户端直接崩（V-58）。
- **落地位置**：`client/FarmstormClient.java`（`RenderLivingEvent.Post` + `player.hasLineOfSight` + 距离判定）；`assets/farmstorm/textures/misc/wild_hunter_red.png`（颜色烘进贴图，V-60）；`12-TECH-0.0.2.md` 的剪影节与 RT-11。

#### D-63 · 新增 3 个进度

- **编号**：D-63
- **结论**：`first_molotov`（初次投出莫洛托夫）、`zangief_hit`（桑吉尔蔗首次命中）、`first_wild_hunter`（首次获得狂野猎人效果）。既有 12 个不动。
- **依据**：三个新玩法都需要「首次达成」的回执，否则玩家无法确认自己触发过，也没有可展示的收集要素。既有进度不动，是因为 C-7 已把进度数与 lang 键数钉成一一对应，改动既有条目会连带改语言文件。施工事实卡 §4：进度 12 → 15。
- **落地位置**：`datagen/ModAdvancementProvider.java`（+3）；`datagen/ModLangProvider.java`（进度 lang 键按 C-7 的口径同步生成，中英两份键集必须相等）；`13-TASKS-0.0.2.md` 的进度任务。

#### D-64 · 狂野猎人索敌口径

- **编号**：D-64
- **结论**：候选 = 以玩家为球心 **5 格**内、与玩家视线方向夹角 **≤ 30°** 的 `LivingEntity`，排除自己、同队伍玩家、旁观者。排序：夹角最小者优先；夹角相同取距离近者。「画面中最靠近准星」即「与视线夹角最小」，服务端算得出来，不需要读客户端鼠标位置。
- **依据**：把「最靠近准星」等价成「与视线夹角最小」之后，客户端与服务端可以跑同一套确定性算法，既不需要读鼠标位置，也不需要为此新增同步包（D-50）。排除项与 D-06 的己方口径一致，旁观者排除与 v0.0.1 三处陷阱的 `isSpectator()` 豁免一致。
- **落地位置**：`client/FarmstormClient.java`（本地预测同一目标）；服务端同算法的判定入口（新增类，命名由 `12-TECH-0.0.2.md` 定）；`config/FarmstormConfig.java`（5 格与 30° 各一个键）；`11-DESIGN-0.0.2.md` 的狂野猎人节。

#### D-65 · 吸附用冲刺，不用瞬移

- **编号**：D-65
- **结论**：服务端按当前距离算出一个初速，让玩家在 **3 刻**内跨到位；速度以 `setDeltaMovement` 下发，**客户端照常模拟移动与碰撞**。撞墙即停、不补位、不穿透。落点被方块占据时该次冲刺直接失败（本次不吸附）。
- **依据**：瞬移会跳过碰撞检测与位移插值，撞墙就穿墙或卡进方块；冲刺让客户端照常跑原版碰撞，"撞墙即停"是原版手感的自然结果。RT-12 已登记「驱动方判错」的风险，首选方案是客户端预测位移 + 服务端判定，红线仍是不新增网络包（D-50）。
- **落地位置**：`12-TECH-0.0.2.md` 的冲刺节与 RT-12；`client/FarmstormClient.java`（本地按下攻击键时的预测位移）；服务端在收到 `ServerboundSwingPacket` 时的判定入口；`config/FarmstormConfig.java`（`wild_hunter.dashTicks` 3 刻；清单见施工事实卡 §8）。

#### D-66 · 音频打包与出处

- **编号**：D-66
- **结论**：三个源文件转换后打包进 jar。README 与 Release 说明里**必须**注明来源与非商业自用：
  - `Animals.mp3` → `wild_hunter` 循环音
  - SF6 片段 `BV1qu411W7ha_846-850.mp3` → 桑吉尔蔗触发音
  - 桌面 JPG → 狂野猎人效果图标
- **依据**：三个素材都不是原创音源，公开发布必须写明来源与非商业自用，否则 Release 存在版权风险。写成「必须」而不是「建议」，是因为漏掉之后无法补正已经分发的 jar 说明。
- **落地位置**：`README.md` 的来源声明节；`docs/zh/07-PUBLISH.md` 的 Release 说明模板（该文件是 v0.0.1 冻结件，只按其模板填写，不改它本身）；`12-TECH-0.0.2.md` 的资源清单。

#### D-67 · 压缩膨胀麦配方无序

- **编号**：D-67
- **结论**：无序合成：任意位置放 **4 个** `farmstorm:puffed_wheat` → **1 个** `farmstorm:compressed_puffed_wheat`。
- **依据**：4 换 1 是无序的等价关系，做成有序只会强迫玩家记住摆放位置而不产生任何收益；这也是本版唯一一条「改良作物 → 二级合成物」的配方，配方计数同样要跟着走（施工事实卡 §4：合成配方 0 → 2，本条是第 2 条）。
- **落地位置**：`datagen/ModRecipeProvider.java`（无序合成）；`13-TASKS-0.0.2.md` 的配方任务；`12-TECH-0.0.2.md` 的配方清单。

#### D-68 · 桑吉尔蔗世界隐形

- **编号**：D-68
- **结论**：部署方块走 v0.0.1 的陷阱类口径（D-48）：世界里**完全隐形**（空模型、无 elements），只保留 `textures.particle`；物品图标独立走 `item/generated`。
- **依据**：D-48 已把「陷阱类部署方块在世界中完全隐形」定为外观口径，桑吉尔蔗是陷阱类（§4 第 17 行：类别 `TRAP`），直接沿用即可。物品图标必须独立走 `item/generated`，否则物品栏里也会跟着变成空白图标。
- **落地位置**：`datagen/ModBlockStateProvider.java`（空模型 + `textures.particle`）；`datagen/ModItemModelProvider.java`（`item/generated`）；`behavior/DeployedKind.java`（`DeployedKind` 枚举 +1 值 `ZANGIEF_CANE`）；`registry/FarmstormBlocks.java`。

#### D-69 · 桑吉尔蔗的漂浮等级

- **编号**：D-69
- **结论**：「漂浮 7 效果 1 秒」= `MobEffects.LEVITATION`，amplifier **6**（= VII 级），持续 **20 刻**。在触发后第 **14 刻**施加。
- **依据**：原版效果等级从 amplifier 0（I 级）起算，「漂浮 7」对应 amplifier 6；20 刻正好 1 秒。第 14 刻施加而不是立刻施加，是留给 D-59 的倒转与 D-70 的自转先演一段，时序由该条给定。相位切换靠重新 apply 同 id、不同 amplifier 的效果完成（D-50）。
- **落地位置**：桑吉尔蔗的触发与相位逻辑（`DeployedKind.ZANGIEF_CANE` 的相位表）；`12-TECH-0.0.2.md` 的 amplifier 相位表；`11-DESIGN-0.0.2.md` 的相位时序表。

#### D-70 · 水平转速

- **编号**：D-70
- **结论**：落地前持续 yaw **每刻 +72°**（1440°/秒 ÷ 20 刻/秒 = 72°/刻；40 刻累计 2880° = 8 整圈），方向为逆时针（yaw 递增）。
- **依据**：原文（`plan0.0.2.txt` 第 29 行）是「1440 度每秒」；除以每秒 20 刻得 72°/刻，40 刻累计 2880° = 8 整圈，方向给定为逆时针（yaw 递增）。自转在客户端按 tick 累加，不依赖服务端逐刻同步，因此不产生额外包（D-50）；写入点仍是 `ComputeCameraAngles` 的 `setYaw(yaw + accumulated)`，不写回玩家真实朝向（D-59 的铁律）。
- **落地位置**：`client/FarmstormClient.java`（`ViewportEvent.ComputeCameraAngles` 的 yaw 累加）；`config/FarmstormConfig.java`（`zangief_cane.spinDegreesPerTick`，默认 72.0，范围 0.0–360.0；清单见施工事实卡 §8）；`12-TECH-0.0.2.md` 的相机节。

#### D-71 · 桑吉尔蔗只对玩家生效

- **编号**：D-71
- **结论**：触发者只认**非己方玩家**，不含生物。创造模式玩家与旁观者不触发（与 v0.0.1 三处陷阱的 `isCreative()` / `isSpectator()` 豁免一致）。
- **依据**：D-07 规定陷阱类与炮台类只针对非己方玩家、不处理生物（man瓜 的例外是 D-43 单独给的，不适用于本条）。创造与旁观豁免与 v0.0.1 三处陷阱的既有判定保持一致，避免同一个类别里出现两套豁免。
- **落地位置**：桑吉尔蔗的触发判定入口；`11-DESIGN-0.0.2.md` 的桑吉尔蔗节；`12-TECH-0.0.2.md` 的陷阱判定表。

#### D-72 · 触发后静默消失

- **编号**：D-72
- **结论**：一次性陷阱，触发后方块**静默移除、不产生掉落物**（先例：胡罗贝 D-46）。本条只约束**触发消失**这一条路径，不约束战利品表，两条路径分开：
  - 触发后陷阱静默移除 → 走 `level.removeBlock` / `consume()`，**不经过战利品表**，因此不掉落；
  - 玩家自己把部署方块挖掉 → **正常走战利品表掉自身 1 个**（与 v0.0.1 的五个部署方块一致）。
- **依据**：D-46 已把「触发后静默消失、不产生掉落物」定为一次性陷阱的**触发路径**行为，并说明胡罗贝此前的掉落是爆炸炸掉自身方块后走战利品表所致、已从规则层消除——它约束的是触发那一下，不是战利品表本身。v0.0.1 的五个部署方块（`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus`）本来就有战利品表、玩家挖掉时掉自身，桑吉尔蔗不改变这一点；实现也不能靠删表来达成触发不掉落。
- **落地位置**：桑吉尔蔗的触发逻辑（静默 `removeBlock` / `consume()`，不触战利品表）；`datagen/ModLootTableProvider.java`（保留「掉自身 1 个」的战利品表，与 v0.0.1 的五个部署方块同形）；`11-DESIGN-0.0.2.md` 的桑吉尔蔗节；`12-TECH-0.0.2.md` §3.14 与 §7.2 的战利品表条目按此口径对齐。

#### D-73 · 触发半径是球形

- **编号**：D-73
- **结论**：1.5 格按**球形欧氏距离**判定（不是切比雪夫）。
- **依据**：D-20 规定陷阱与炮台的触发判定用切比雪夫距离，但切比雪夫会把对角方向的触发范围放大成方形，与玩家看到的圆形观感不一致。本条是对 D-20 的一次显式例外，只作用于桑吉尔蔗，不改 D-20 本身（见 §7 的 D-20 行）。
- **落地位置**：桑吉尔蔗的触发判定（球形欧氏距离）；`config/FarmstormConfig.java`（1.5 格键）；`11-DESIGN-0.0.2.md` 的桑吉尔蔗节。

#### D-74 · 击杀续时无上限

- **编号**：D-74
- **结论**：狂野猎人期间每击杀一个生物：剩余时长 **+8 秒**、回复 **10 点生命**。没有次数上限（原文未写上限，不擅自加）。
- **依据**：原文只给了单次增量，没有给次数上限；自行封顶会改变作者给出的强度曲线，属于未经授权的平衡调整（D-21 已把「不做平衡调整」定成原则）。因此实现不得加自己的计数器。
- **落地位置**：狂野猎人的击杀结算入口；`config/FarmstormConfig.java`（+8 秒与 10 点各一个键，键名由 `12-TECH-0.0.2.md` 定，本文补充）；`11-DESIGN-0.0.2.md` 的狂野猎人节。

#### D-75 · 击退用原版默认强度

- **编号**：D-75
- **结论**：「若成功造成伤害则击退目标」= 调原版 `LivingEntity#knockback` 的默认强度，不自订数值。
- **依据**：自订击退强度会与本版其它伤害来源的手感不一致，也会绕过原版的击退抗性与属性加成。调原版方法则天然继承这些规则，且不需要为它新增 config 键。
- **落地位置**：狂野猎人的伤害结算入口；`11-DESIGN-0.0.2.md` 的狂野猎人节；`12-TECH-0.0.2.md` 的伤害结算表。

#### D-76 · 循环音用客户端 SoundInstance

- **编号**：D-76
- **结论**：Animals 由客户端一个循环 `SoundInstance` 持续播放，音源跟随玩家，衰减距离 16 格（V-83 校准：现为 **32 格**，见 `12-TECH §4.6`），声音分类 `SoundSource.PLAYERS`（不占用 `MUSIC`，不打断原版背景音乐）。服务端不每刻重发播放指令。
- **依据**：服务端逐刻下发播放指令会产生大量原版包，且玩家移动时声音会抖；客户端循环 `SoundInstance` 自己跟随玩家，是唯一既不加包（D-50）又不抖动的做法。分类用 `PLAYERS` 才不会占用 `MUSIC` 通道、打断原版背景音乐。
- **落地位置**：`client/FarmstormClient.java`（`SoundInstance` 子类，`isLooping() = true`，每 tick 更新位置）；`assets/farmstorm/sounds.json`（`wild_hunter_loop`）；`12-TECH-0.0.2.md` 的音频节与 RT-14。

#### D-77 · 压缩膨胀麦沿用膨胀麦的落点写入

- **编号**：D-77
- **结论**：先纠正一处事实：**膨胀麦没有爆炸段**。`src/main/java/com/dashpig/farmstorm/entity/thrown/PuffedWheatProjectile.java` 只做两件事——① 以命中方块为中心把立方体范围替换成 `minecraft:hay_block`（干态边长 3；若范围内存在 `#minecraft:water` 则边长 5）；② 跳过爆炸抗性 ≥ 3.6e6 的方块（`BlastRule.UNBREAKABLE_RESISTANCE`）。它没有触发任何 `Explosion`，因此不存在「爆炸威力」与「伤害上限」。
  所以 `compressed_puffed_wheat` 沿用膨胀麦的是这两条，**只把写入的方块从 `minecraft:hay_block` 换成 `farmstorm:compressed_hay_block`**：
  - 立方体写入范围：干态边长 3、遇水边长 5，覆盖空气 / 流体 / 实体方块直接写入，原方块不产生掉落；
  - 抗性底线：目标位置原方块抗性 ≥ 3.6e6 时跳过不写。
- **依据**：压缩膨胀麦的原料就是膨胀麦（D-67），落点写入的语义不该因为多合成一步就变异；沿用 `puffed_wheat` 的这两条即同时继承 D-38 的 3.6e6 底线，不需要另立规则。`PuffedWheatProjectile` 里没有任何 `Explosion` 调用，所以「爆炸威力」与「伤害上限」这两项在两件投掷物上都不存在，没有可沿用的东西。
- **落地位置**：`entity/thrown/` 的压缩膨胀麦投掷物（`entity_types` +2 之一）；`12-TECH-0.0.2.md` 的投掷物表与落点写入逻辑；不新增爆炸相关的 config 键（施工事实卡 §8 的唯一清单里没有爆炸威力 / 伤害上限键，本文补充）。

### 3.4 勘误（D-78）

> 本节登记的是 D-78，内容是**口径澄清**，不是对 v0.0.1 文档的纠错：那一版文档与实现在阈值归属上一直是同一个口径。本节登记的三处偏差都在符号与覆盖层面，行为都是对的。

#### D-78 · 破障阈值归属澄清（口径澄清）

- **编号**：D-78
- **结论**：先纠正一个曾经的误读：`02-TECH-SPEC.md §4.9` 的投掷物表并没有把破障类统一到同一个阈值——它给 `UbiServerProjectile` 标的就是 `BlastRule.UNBREAKABLE_SKIP`，并注明「破障类不设抗性阈值」；`00-DECISIONS.md` 的 D-19 原文也只约束**瓜片子与瓜！**两件。v0.0.1 文档在阈值归属上没有行为错误。真正需要登记的是三处**符号与覆盖**层面的偏差（行为都对，写得不严）：
  1. `02-TECH-SPEC.md §4.9` 投掷物表把 `MelonSliceProjectile` 与 `MelonGrenadeProjectile` 的规则写成常量 `BlastRule.COBBLESTONE_6`；实现用的是读 config 的工厂 `BlastRule.sliceBomb()` / `BlastRule.grenade()`（默认值同为 6.0，行为等价、符号不符）。而同一份文档 §4.11 又列出了 `sliceResistThreshold` / `grenadeResistThreshold` 两个键，§4.9 与 §4.11 对不上。
  2. 同一张表没有写 `ToxicUbiServerProjectile` 用哪条规则；实现是 `BlastRule.VANILLA`，阈值 3.6e6。
  3. `02-TECH-SPEC.md §4.9` 正文把「爆炸抗性 ≥ 圆石 6.0 不破」写成 `BlastRule` 的通用职责，容易被读成破障类统一阈值；准确说法是「**这是 D-19 给瓜片子与瓜！这两件定的阈值**，其它破障投掷物走 3.6e6 底线」。

  逐件对照表与后果见本文 §5.3 与 §5.5。v0.0.1 文档按 D-49 冻结不改，本条只登记在 v0.0.2 文档内。
- **依据**：实测 `src/main/java/com/dashpig/farmstorm/entity/thrown/` 下的四个投掷物类与 `explosion/BlastRule.java`：判定式是 `resistance < UNBREAKABLE_RESISTANCE && resistance < this.maxResistance`，`UNBREAKABLE_RESISTANCE = 3.6e6F`，`VANILLA` 与 `UNBREAKABLE_SKIP` 取的都是它，只有 `grenade()` 与 `sliceBomb()` 才读 config 的两个 6.0 键；`config/FarmstormConfig.java` 里这两个键确实存在（默认 6.0），即 §4.11 记的是实现，§4.9 的常量名只是简化写法。原版爆炸一侧的公式依据见 D-57。
- **落地位置**：本文 §5（展开与影响面）；`11-DESIGN-0.0.2.md` 与 `12-TECH-0.0.2.md` 的破障口径按 §5.3 逐件写；`02-TECH-SPEC.md` 按 D-49 不改。

---
### 3.5 输入锁的实现修订（D-79）

> 本节登记 D-79：`12-TECH-0.0.2.md` §4.3 原先按「`InputEvent.Key` 取消」写，**经源码核实该方案不成立**，由本节修订。逐条证据见 `.work/FINDING-RT13-INPUT-LOCK.md`。

#### D-79 · 输入锁改按「抽干消费」实现，并补视角锁一路

- **编号**：D-79
- **结论**：D-60 的「五个事件各管一段」修订为「**七个机制**」，并改写三处具体手段：
  1. **键盘（快捷栏 1–9 / Q / E / F / F5 等）**：`InputEvent.Key` **不可取消**（`InputEvent.java:214` 未实现 `ICancellableEvent`），且 `ClientHooks.onKeyInput` 是 `KeyboardHandler#keyPress` 的**最后一句**（`:485`），位于 `KeyMapping.set/click`（`:483-484`）之后。→ 改为在该事件里**抽干 `consumeClick()`**：`handleKeybinds()`（`Minecraft.java:1837`）要到 tick 里才消费（`:1944`），先抽干即等于拦截。
  2. **鼠标左/右键**：改用 `InputEvent.MouseButton.Pre`（可取消、最先执行，`MouseHandler.java:82`）；`InteractionKeyMappingTriggered` 保留作第二道网，覆盖 `startAttack` / `startUseItem` / `continueAttack` 三条路径。
  3. **视角（原稿缺）**：新增 `CalculatePlayerTurnEvent#setMouseSensitivity(0)`（`ClientHooks.java:368`）——这是**唯一无 Mixin 的视角锁**方案。
- **已知限制**：F11 全屏、F2 截图、F3 调试、ESC 暂停在 `keyPress` 内、`onKeyInput` **之前**就执行完，无 Mixin 时拦不住；原稿「放行 ESC」是伪需求（拦不拦都会生效）。
- **状态复位**：锁定生效瞬间若左键正被按住，`keyAttack.isDown()` 保持 true（`Minecraft.java:2035`），必须主动清零，否则挖掘不会停。
- **依据**：`.work/FINDING-RT13-INPUT-LOCK.md`（源码取自 `neoforge-21.1.213-sources.jar`，结论逐条带 `文件:行号`）。
- **落地位置**：`12-TECH-0.0.2.md` §4.3 的事件表与说明段落。

## 4. 作物注册名总表（19 条）

前 15 行的中文名、registry id、类型与类别照 `00-DECISIONS.md` §4；英文名按 C-10 取 registry id 的 Title Case；后 4 行照施工事实卡 §3.1。中文名一律保留原文里的感叹号。

| 序号 | registry id | 类型 | 类别 | 中文名 | 英文名 | v0.0.2 新增 |
|---|---|---|---|---|---|---|
| 1 | `puffed_wheat` | THROWABLE | CONSTRUCTION | 膨胀麦 | Puffed Wheat | 否 |
| 2 | `blast_carrot` | DEPLOYABLE | TRAP, DAMAGE | 胡罗贝 | Blast Carrot | 否 |
| 3 | `ubi_server` | THROWABLE | DEMOLITION, DAMAGE | 育碧服务器 | Ubi Server | 否 |
| 4 | `toxic_ubi_server` | THROWABLE | BUFF, DEMOLITION, DAMAGE | 毒育碧服务器 | Toxic Ubi Server | 否 |
| 5 | `power_beet` | EDIBLE | BUFF | 甜菜！ | Power Beet | 否 |
| 6 | `melon_slice_bomb` | THROWABLE | DAMAGE, DEMOLITION | 瓜片子 | Melon Slice Bomb | 否 |
| 7 | `melon_grenade` | THROWABLE | DEMOLITION, DAMAGE | 瓜！ | Melon Grenade | 否 |
| 8 | `pumpkin_turret` | DEPLOYABLE | TURRET, DAMAGE | man瓜 | Pumpkin Turret | 否 |
| 9 | `awakened_torchflower` | DEPLOYABLE | TURRET, BUFF | 激活的火把花 | Awakened Torchflower | 否 |
| 10 | `awakened_pitcher_plant` | DEPLOYABLE | TURRET, BUFF | 激活的瓶子草 | Awakened Pitcher Plant | 否 |
| 11 | `fang_trap_cactus` | DEPLOYABLE | TRAP, DAMAGE | 赢过输过没死过 | Fang Trap Cactus | 否 |
| 12 | `bamboo_firecracker` | THROWABLE | DAMAGE | 爆竹 | Bamboo Firecracker | 否 |
| 13 | `red_mushroom_puff` | THROWABLE | BUFF | ？！菇菇！？ | Red Mushroom Puff | 否 |
| 14 | `brown_mushroom_puff` | THROWABLE | BUFF | ？！棕色菇菇色棕！？ | Brown Mushroom Puff | 否 |
| 15 | `lucky_berry` | EDIBLE | BUFF | 幸运浆果 | Lucky Berry | 否 |
| 16 | `molotov` | THROWABLE | DAMAGE, CONSTRUCTION | 莫洛托夫！ | Molotov | 是 |
| 17 | `zangief_cane` | DEPLOYABLE | TRAP | 桑吉尔蔗 | Zangief Cane | 是 |
| 18 | `wild_apple` | EDIBLE | BUFF | 狂野苹果 | Wild Apple | 是 |
| 19 | `compressed_puffed_wheat` | THROWABLE | CONSTRUCTION | 压缩膨胀麦 | Compressed Puffed Wheat | 是 |

说明：

- registry id 即 `CropProfile.id()`，也是物品 / 方块 / 实体的注册 ID；部署型作物的方块与物品同名。
- 类型与类别的中英对照（本文补充：枚举常量名取自 `util/CropType.java` 与 `util/CropCategory.java`）：

  | 类型中文 | `CropType` | 类别中文 | `CropCategory` |
  |---|---|---|---|
  | 投掷型 | `THROWABLE` | 陷阱类 | `TRAP` |
  | 部署型 | `DEPLOYABLE` | 炮台类 | `TURRET` |
  | 食用型 | `EDIBLE` | 破障类 | `DEMOLITION` |
  | — | — | buff类 | `BUFF` |
  | — | — | 建造类 | `CONSTRUCTION` |
  | — | — | 伤害类 | `DAMAGE` |

- `molotov` 的类别是 DAMAGE + CONSTRUCTION（撒火与撒岩浆块都算在世界里写方块），`compressed_puffed_wheat` 是纯 CONSTRUCTION；`zangief_cane` 是纯 TRAP、没有 DAMAGE 标签，与 `blast_carrot`、`fang_trap_cactus` 不同。
- 前 15 行的「v0.0.2 新增」为否，后 4 行为是：15 + 4 = 19。
- 本版新增的方块 `compressed_hay_block` 不占本表任何一行：它是二级合成物的承载方块（D-57、D-77），物品形态是它的 BlockItem，英文显示名按 C-10 取 Compressed Hay Block，中文名为压缩干草块。

计数自洽（行号即上表序号，与施工事实卡 §4 的数字口径表逐项对齐）：

| 维度 | 值 | 行号 |
|---|---|---|
| 类型 · 投掷型 | 10 | 1、3、4、6、7、12、13、14、16、19 |
| 类型 · 部署型 | 6 | 2、8、9、10、11、17 |
| 类型 · 食用型 | 3 | 5、15、18 |
| 类别 · 破障类 | 4 | 3、4、6、7 |
| 类别 · 陷阱类 | 3 | 2、11、17 |
| 类别 · 炮台类 | 3 | 8、9、10 |
| 类别 · buff 类 | 8 | 4、5、9、10、13、14、15、18 |
| 类别 · 建造类 | 3 | 1、16、19 |
| 类别 · 伤害类 | 9 | 2、3、4、6、7、8、11、12、16 |
| 合计 | 19 | 1 – 19 |

启动自检文案（`Farmstorm#logSelfCheck` 打印，改数字要同步改；本表的 19 / 10 / 6 / 3 / 4 必须与它一致）：

```
自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK
```

---

## 5. 对 v0.0.1 文档的勘误

本节把 D-78 展开。三处偏差都是**符号与覆盖**层面的不严，而且都落在同一份文档的同一节——`02-TECH-SPEC.md` §4.9：实现与文档在阈值归属上一致（瓜！与瓜片子 6.0，育碧服务器与毒育碧服务器 3.6e6），缺的只是写法、覆盖面与措辞。本节不复述任何被纠正过的说法，只给位置、实现与正确口径。

### 5.1 哪几处写得不够严

| 处 | 位置 | 不严在哪 | 实现是什么 |
|---|---|---|---|
| 1 | `02-TECH-SPEC.md §4.9` 的投掷物表 | 两件瓜的规则写成常量 `BlastRule.COBBLESTONE_6`，而实现是读 config 的工厂（默认同为 6.0，行为等价、符号不符）；且与同一份文档 §4.11 列出的 `sliceResistThreshold` / `grenadeResistThreshold` 两个键对不上。 | `MelonSliceProjectile` → `BlastRule.sliceBomb()`；`MelonGrenadeProjectile` → `BlastRule.grenade()`。 |
| 2 | `02-TECH-SPEC.md §4.9` 的投掷物表 | 同表没有写 `ToxicUbiServerProjectile` 用哪条规则，读者会以为它和两件瓜同一档。 | `BlastRule.VANILLA`，阈值 3.6e6（与 `UbiServerProjectile` 的 `UNBREAKABLE_SKIP` 同为 3.6e6 底线）。 |
| 3 | `02-TECH-SPEC.md §4.9` 的正文 | 把「爆炸抗性 ≥ 圆石 6.0 不破」写成 `BlastRule` 的通用职责，容易被读成破障类统一阈值。 | 6.0 是 D-19 给瓜片子与瓜！这两件定的阈值；其它破障投掷物走 3.6e6 底线。 |

涉及文档是一份（`02-TECH-SPEC.md`），偏差是三条——两处在那张投掷物表里、第三处在正文，两者不矛盾。三处的处置相同：都不改 v0.0.1 文档（D-49），只在 v0.0.2 文档里按 §5.3 的逐件口径写。

### 5.2 实测支撑

源码实测路径（v0.0.1 已发版代码，本轮只读未改）：

| 文件 | 关键符号 |
|---|---|
| `src/main/java/com/dashpig/farmstorm/entity/thrown/UbiServerProjectile.java` | `BlastRule.UNBREAKABLE_SKIP` |
| `src/main/java/com/dashpig/farmstorm/entity/thrown/ToxicUbiServerProjectile.java` | `BlastRule.VANILLA` |
| `src/main/java/com/dashpig/farmstorm/entity/thrown/MelonGrenadeProjectile.java` | `BlastRule.grenade()` |
| `src/main/java/com/dashpig/farmstorm/entity/thrown/MelonSliceProjectile.java` | `BlastRule.sliceBomb()` |
| `src/main/java/com/dashpig/farmstorm/explosion/BlastRule.java` | `UNBREAKABLE_RESISTANCE = 3.6e6F`；`canBreak()` 的判定是 `resistance < UNBREAKABLE_RESISTANCE && resistance < this.maxResistance`；`VANILLA` 与 `UNBREAKABLE_SKIP` 都取 `UNBREAKABLE_RESISTANCE` |
| `src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java` | `grenadeResistThreshold`、`sliceResistThreshold`（默认 6.0，只被 `grenade()` / `sliceBomb()` 读取） |
| 反编译源码 `net/minecraft/world/level/Explosion` | `f` 从 `radius * (0.7 + rand * 0.6)` 起，沿射线每格减 `0.225`，撞到方块再减 `(resistance + 0.3) * 0.3`，`f > 0` 即破 |

### 5.3 正确口径

| 投掷物 | 档案 | 规则 | 生效阈值 | 对压缩干草块（抗性 6.0） |
|---|---|---|---|---|
| `UbiServerProjectile` | `ubi_server` | `BlastRule.UNBREAKABLE_SKIP` | 3.6e6 | **破** |
| `ToxicUbiServerProjectile` | `toxic_ubi_server` | `BlastRule.VANILLA` | 3.6e6 | **破** |
| `MelonGrenadeProjectile` | `melon_grenade` | `BlastRule.grenade()` | 6.0 | 不破（`6.0 < 6.0` 为假） |
| `MelonSliceProjectile` | `melon_slice_bomb` | `BlastRule.sliceBomb()` | 6.0 | 不破（同上） |
| 原版爆炸（TNT 等） | — | 原版 `Explosion` | 逐格累积公式 | **破**（见 D-57 依据） |

- 带 6.0 阈值的**只有瓜！与瓜片子**；育碧服务器与毒育碧服务器只受 3.6e6 底线加 `#farmstorm:blast_proof`、`#farmstorm:explosion_immune` 两个标签约束。
- 判定方向是「抗性严格小于阈值才破」：抗性恰好 6.0 的压缩干草块对两件瓜不破，对育碧服务器、毒育碧服务器与原版爆炸都破。
- 破障类的分类口径不变：仍是 D-42 定的 4 种（`ubi_server` / `toxic_ubi_server` / `melon_grenade` / `melon_slice_bomb`）；被澄清的是阈值归属与实现符号，不是分类。

### 5.4 为什么 v0.0.1 文档不改、只在本文件登记

1. 三处都是符号与覆盖层面的不严，不影响任何行为：文档与实现在阈值归属上一致，瓜！与瓜片子 6.0、育碧服务器与毒育碧服务器 3.6e6 的结论两边相同。
2. D-49 冻结：v0.0.1 文档与已发版 jar 一一对应，回改的收益只是措辞，代价却要动中英两套的交叉引用与已发布版本的一致性。
3. 实现期只读 v0.0.2 文档：`11-DESIGN-0.0.2.md` 与 `12-TECH-0.0.2.md` 按 §5.3 逐件写，实现拿到的就是正确口径，不需要回改旧档。
4. 追溯路径是通的：§7 的 D-19 行注明 D-19 继续有效、适用范围由 D-78 澄清，读 v0.0.1 文档的人顺着版本号就能找到这里。

### 5.5 对 v0.0.2 的连带影响

- D-57 的压缩干草块取值按 §5.3 读：抗性 6.0 对瓜！与瓜片子不破，对育碧服务器与毒育碧服务器破，**原版爆炸（含 TNT）也破得动**——这一条是 `11-DESIGN-0.0.2.md` 必须写出的后果。
- 本次不改 `BlastRule.canBreak()` 的比较逻辑，也不改 `grenadeResistThreshold` / `sliceResistThreshold` 的默认值（§6）。
- `12-TECH-0.0.2.md` 的投掷物表按 `BlastRule` 的常量与工厂逐件写，必须补上 `ToxicUbiServerProjectile` 与原版爆炸两行，不能再用合并写法。

---
## 6. v0.0.2 明确不做的事

以下 11 条是这一版的排除清单，实现阶段遇到任何一条都不许开例外；需要开例外时先改本文，再改代码。

1. **不给三个新作物加种植形态**：`molotov` / `zangief_cane` / `wild_apple` 不注册作物方块、不注册种子，也不给 `CropType` 补「种植型」常量（D-51、D-10）。
2. **不加自定义网络包**：不新增任何 `CustomPacketPayload`，三项侵入性效果与桑吉尔蔗的相位全靠隐藏 `MobEffect` 的原版同步（D-50、C-6）。
3. **不引入 Mixin**：三个侵入性效果全部走 NeoForge 事件与原生扩展点（D-40）。
4. **不改 v0.0.1 任何既有数值与配方**：15 条既有 `CropProfile` 的任何字段、任何既有 config 键的默认值、既有 21 条转换配方、既有 12 个进度一律不动（施工事实卡 §3.4）。
5. **不改破障阈值的比较逻辑**：`BlastRule.canBreak()` 的 `resistance < maxResistance` 不动，`grenadeResistThreshold` 与 `sliceResistThreshold` 的默认值也不动；D-78 澄清的是阈值归属与文档写法，不是改实现（§5.3、§5.5）。
6. **不做光影类模组的兼容检测**：世界灰度（原版 `PostChain`）与 Oculus / Iris 一类后处理模组的冲突只登记为已知不兼容，不写运行时探测，也不做降级分支（RT-16）。
7. **不给压缩干草块做可旋转 pillar**：六面同贴图、无 axis 变体（D-57）。
8. **不新增伤害类型**：`damage_types` +0，仍是 D-39 的 `farmstorm:void_bolt` 与 `farmstorm:crop_blast` 两个；莫洛托夫不造成伤害（D-58）。
9. **不新增注册类目**：`block_entities`、`menu types`、`recipe type` / `serializer`、`data components`、创造标签页在施工事实卡 §3.2 里全是 +0（`zangief_cane` 复用既有 `DeployedCropBlockEntity`，转换配方复用 `ConverterRecipe`）。
10. **不新增自定义指令**：本轮只加 3 个进度（D-24、D-63）。
11. **不回改 v0.0.1 的文档**：`docs/zh/00`–`09` 与 `docs/en/00`–`09` 一份都不改，口径澄清只登记在本文 §5（D-49）。

---

## 7. 与 v0.0.1 决策的继承关系

| 标记 | 含义 |
|---|---|
| 全额继续 | 结论与适用范围都不变 |
| 本次细化 | 结论不变，本版把它的某个参数、例外或落点讲定 |
| 本次扩充 | 结论不变，本版在它管的清单里加了条目 |
| 改口径 | 本版改写了它的一部分口径（只有 D-17 一条） |

| 编号 | 状态 | 说明 |
|---|---|---|
| D-01 | 全额继续 | v0.0.2 同样只出文档与提示词。 |
| D-02 | 本次扩充 | 交付物在 v0.0.1 的全套之外，中英各加 6 份（D-49）。 |
| D-03 | 本次扩充 | 提示词本轮是 `14-PROMPT-MAIN-0.0.2.md` 与 `15-PROMPT-TASKS-0.0.2.md` 两份（D-49）。 |
| D-04 | 全额继续 | 目标读者不变。 |
| D-05 | 本次扩充 | 中英双套的要求不变，覆盖面扩到 `10`–`15`（D-49）。 |
| D-06 | 全额继续 | 己方判定仍走原版计分板队伍；D-64 与 D-71 的排除项都按它筛。 |
| D-07 | 全额继续 | 陷阱类与炮台类仍只针对非己方玩家；桑吉尔蔗按本条不处理生物（D-71）。 |
| D-08 | 本次细化 | 「改良作物都配播种物品」只对 15 条既有档案成立；三条新档案与 `melon_slice_bomb` 一样没有播种形态（D-51）。 |
| D-09 | 全额继续 | 狂野苹果由转换器产出，不给原版用途（D-54）。 |
| D-10 | 全额继续 | 「种植型」仍不新增设计；D-51 与它同向。 |
| D-11 | 本次扩充 | 本体的 GUI 描述已被 D-47 取代；转换配方清单本版加 1 条双向配方（D-54），转换配方 21 → 24。 |
| D-12 | 全额继续 | D-54 的成本 3 点经验与「无催化剂」都按本条办。 |
| D-13 | 全额继续 | 已被 D-37 收窄，本版不动。 |
| D-14 | 全额继续 | 火把花与瓶子草的回收口径未改动。 |
| D-15 | 全额继续 | 临时附魔的授予与移除未改动。 |
| D-16 | 全额继续 | 堆叠口径不改；本版新物品按同一口径归档。 |
| D-17 | 改口径 | D-55：随机池从「`ALL` 减去幸运浆果」的自动推导改为显式排除三件（`lucky_berry` / `wild_apple` / `compressed_puffed_wheat`），池 14 → 16。 |
| D-18 | 全额继续 | 创造模式的 90 秒还原未改动。 |
| D-19 | 全额继续 | 只约束瓜片子与瓜！两件；阈值本身不变。D-78 澄清的是它的适用范围（其它破障投掷物走 3.6e6 底线）与实现符号（工厂 vs 常量），展开见 §5。 |
| D-20 | 本次细化 | D-73 为陷阱类新增一条显式例外：桑吉尔蔗的 1.5 格触发半径用球形欧氏距离。 |
| D-21 | 全额继续 | 本版新数值同样进 `FarmstormConfig`，不做平衡调整。 |
| D-22 | 本次扩充 | 新键追加进 `FarmstormConfig`，既有 117 个键的默认值不改；全量新增键清单由 `12-TECH-0.0.2.md` 出（D-53 / D-58 / D-64 / D-74 各自带新键）。 |
| D-23 | 全额继续 | 专用服务器仍只需资源包。 |
| D-24 | 本次扩充 | 进度 12 → 15（D-63），仍不加自定义指令。 |
| D-25 | 本次细化 | D-53 定死音频与图标的转换规格，D-66 定来源标注；像素仍由作者审校。 |
| D-26 | 全额继续 | 本版英文名按 C-10 的 Title Case 规则取（§4 后 4 行）。 |
| D-27 | 全额继续 | 友伤口径不改。 |
| D-28 | 全额继续 | man瓜 的开火间隔不在本版改动面内。 |
| D-29 | 全额继续 | 食用数值不改。 |
| D-30 | 全额继续 | 尖牙半径不改。 |
| D-31 | 全额继续 | 补全项默认值不改。 |
| D-32 | 全额继续 | 毒育碧服务器的作物方块与播种物独立注册，本版不动。 |
| D-33 | 全额继续 | 幸运浆果生成物的落点偏移不改。 |
| D-34 | 全额继续 | 三条新档案不注册作物方块（D-51），本条清单不扩。 |
| D-35 | 本次扩充 | `zangief_cane` 是部署型，物品即部署方块的 BlockItem，沿用 `useOn` 分流（D-35）；外观走陷阱类口径（D-68）。 |
| D-36 | 全额继续 | 不可破坏的实现方式不改。 |
| D-37 | 全额继续 | 「具体条款优先于泛化条款」的裁决原则本版同样适用（D-57 与 D-78 都按它读）。 |
| D-38 | 全额继续 | D-77 沿用膨胀麦落点写入时一并继承 3.6e6 底线。 |
| D-39 | 全额继续 | 本版不新增伤害类型（`damage_types` +0），仍是 2 个。 |
| D-40 | 全额继续 | 三个侵入性效果全走 NeoForge 事件与隐藏 `MobEffect`（D-50、D-60 ~ D-65、D-76），一处 Mixin 也没有。 |
| D-41 | 全额继续 | 临时附魔仍用物品上的 DataComponent，本版不涉及。 |
| D-42 | 全额继续 | 破障类仍 4 种（`ubi_server` / `toxic_ubi_server` / `melon_grenade` / `melon_slice_bomb`），阈值归属由 D-78 澄清（§5）。 |
| D-43 | 全额继续 | man瓜 的索敌半径与目标范围不改。 |
| D-44 | 全额继续 | man瓜 的伤害归属不改。 |
| D-45 | 全额继续 | 作物爆炸仍不排除实体；莫洛托夫的落点爆炸是 0 伤害（D-58），与本条不冲突。 |
| D-46 | 全额继续 | D-72 沿用「静默移除、不产生掉落物」，作用域限于触发路径（挖掉仍走战利品表）。 |
| D-47 | 全额继续 | 转换器仍是三槽 + 显式按钮；D-52 加的是它的合成配方，不是改 GUI。 |
| D-48 | 全额继续 | 型与类正交仍是唯一建模方式；D-68 沿用陷阱类的世界隐形口径，D-58 沿用「方块破坏权限只由破障类决定」。 |

48 条里 37 条全额继续、10 条被本次细化或扩充（细化 3 条：D-08、D-20、D-25；扩充 7 条：D-02、D-03、D-05、D-11、D-22、D-24、D-35）、1 条改口径（D-17），合计 48 条。

最需要留意的五条：**D-11** 的转换配方清单被 D-54 扩充（转换配方 21 → 24）；**D-17** 的幸运浆果池被 D-55 改口径（池 14 → 16）；**D-19** 继续有效、只约束瓜片子与瓜！两件，适用范围由 D-78 澄清（§5）；**D-48** 的陷阱隐形被 D-68 沿用，同时它的「方块破坏权限只由破障类决定」是 D-58 的依据。**D-40** 的「无 Mixin」也继续有效——本版三个侵入性效果全部靠 NeoForge 事件与隐藏 `MobEffect` 实现，没有开任何一处例外。

## 校订注

> 本节与英文版末尾的 Translator’s notes 对称：那边记录译名与翻译取舍，这边记录中文侧的撰写口径。

1. **权威版本**：本文件是中文原稿，也是 v0.0.2 的权威版本；英文版是同源翻译，两者逐节对应。中英数字必须始终相等——改任一数字时两边同改，**冲突以本文件为准**。
2. **「勘误」是沿用词**：§3.4 与 §5 的标题保留了「勘误」二字，但它们登记的其实是**口径澄清**而非 v0.0.1 文档的错误（见两处正文）。英文版在 Translator’s notes 第 1 条记了同样的判断，不要因为标题而把内容改写成「纠错」。
3. **「两人一物」只在中文有**：D-55 依据里的「两人一物」括注，英文版因「上下文不可还原」而略去（见其 Translator’s notes 第 2 条）。中文保留；若将来要统一，先确认这四个字的确切含义，不要凭猜补写。
4. **§4 的自检日志串不翻译**：那段是 `Farmstorm#logSelfCheck` 的运行时输出原文，英文版也保留中文，因为它是日志字面量。
