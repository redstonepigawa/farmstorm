# 42 号 v0.2.1 技术规格

> 项目：农场风云 / Farmstorm · 版本 **0.2.1**（MC 1.21.1 / NeoForge 21.1.213 / Java 21），基线 **0.2.0 已发布态**（`gradle.properties:11` 现为 `mod_version=0.2.0`）。
> 唯一真相源：`docs/zh/40-DECISIONS-0.2.1.md`（D-173 ~ D-186，共 **14 条**）+ `.work/v021-BRIEF.md` §2 的 **17 条**作者裁定（#1~#12 有 D 号映射，#13~#17 是写作期间追加、40 号尚未转写，见 §9 第 6 条）。本文**只引用**决策号与裁定号，不新开决策、不改口径；与 40 号冲突时报给作者，冲突本身登记在本文 §9。
> 事实底座：`.work/v021-BRIEF.md` §1（源码事实，带行号）、§3（增量与 12 项必改）、§4（RT-51 ~ RT-60）。**本卡与源码冲突时以源码为准**——本文所有行号都是写稿时用 `[IO.File]::ReadAllLines($p, UTF8)` 重读源码后**自己核过的**，与 `BRIEF` 不一致的地方逐条登记在 §9。
> 扫描旁证：`.work/v021-scan.md` 只用于定位，不进任何计数。
> 体例参照：`docs/zh/36-TECH-0.2.0.md`（只学 §1 基线核对 / §2 类设计 / §3 动作面表 / §4 config 全表 / §6 存档 / §7 风险 / §8 验收 / §9 待验证 / §10 差异登记的**目录结构**，不引用它的任何数值）。
> 红线（两条，均不变）：**无 Mixin（0 个，继续禁止）**；自定义网络包**不设上限**，本版**不新增**，累计仍 **2 个**（D-186）。
> 写作纪律：注册面 **97**、server config **175 → 176 键 / 60 组**、语言键 **337 → 338 × 2** 三个数**全部由本文自己逐条点名**（方法写在 §1.2 / §1.3 / §4.1 / §4.3），不抄 `BRIEF` §0、不抄 40 号；凡本文自定的技术参数一律标「**本文确定**」并给理由（汇总见 §0.3）；**不写任务拆分与工时**（属 43 号）、**不写未定事项与占位标记**。

---

## 0. 阅读约定

### 0.1 本文负责什么

| 项 | 内容 |
|---|---|
| 负责 | 注册面 **97** 的逐类独立点名（§1.2）+ **口径外第 13 类**的显式登记（§1.2.1）；server config **175 → 176 键 / 60 组**与语言键 **337 → 338 × 2** 的逐键点名（§1.3、§4）；五个技术构件（周期补绑 / tooltip / AI 干预与恢复 / 敌对谓词 / 陷阱倍率）的类设计与调用面（§2）；对 0.2.0 的增 / 改 / 删总表 **31 行**（§3）；存档与兼容五问（§5）；RT-51 ~ RT-60 的验证方式（§6）；**31 条**验收自检（§7）；待验证 API **10 条**（§8）；与上游的差异登记 **16 条**（§9） |
| 不负责 | 为什么定成 X（40 号）；玩家可见表现与时序表（41 号）；任务、Phase、里程碑（43 号）；主提示词与阶段提示词（44 / 45 号） |
| 不动的文件 | `src/` 下**既有**文件一律只读（本文只描述「要改成什么」，不落地代码）；`docs/zh/00` ~ `41`、`docs/en/*`、`.work/*` 一律只读；本文只新建 `docs/zh/42-TECH-0.2.1.md` 一个文件 |

### 0.2 计数纪律

1. **注册对象、config 键、语言键三个数，本文全部自己数**，并写出「数了哪个文件的哪几行」。不抄 `BRIEF` §0、不抄 40 号 §3、不抄 `verify_project.py` 的 `EXPECTED_COUNTS`。
2. 三张表各自满足「行数 = 声明的计数」「小计 = 逐行相加」「跨表不重不漏」。
3. 数值只允许四个来源：40 号的决策号、`BRIEF` §1 / §2 / §3、两个已给数值的算术（`175 + 1 = 176`、`337 + 1 = 338`、`97 + 0 = 97`、`LCM(40, 100) = 200`）、以及本文标「本文确定」的技术参数（每个都给理由）。**不发明玩法数值**。
4. 读文件一律用 UTF-8（本机 `Get-Content` 无 `-Encoding` 时会把无 BOM 的 UTF-8 中文读成 GBK 乱码，并**改变行号**：同一份 `FarmstormConfig.java` 会从 1131 行读成 1095 行，行号全错）。本文所有行号取自 UTF-8 读法。

### 0.3 「本文确定」的清单（15 项，逐项正文有理由）

| # | 参数 | 值 | 正文 |
|---|---|---|---|
| 1 | 补绑周期常量与相位公式 | `REBIND_INTERVAL_TICKS = 100`；`floorMod(now + floorMod(pos.hashCode(), 100), 100) == 0` | §2.1 |
| 2 | 补绑门的插入位置 | 在**粒子门之前**、独立 `if`、**不** `return` | §2.1 |
| 3 | 「区域枚举方块实体」的形态 | `util/AreaBlockEntities#collect(ServerLevel, BlockPos, int, Class<T>) → List<T>`（新列表，只走 `getChunkNow`） | §2.1 |
| 4 | AI 原值的载体与运行期索引 | 落盘：`AttachmentType<Suspend>`（`registry/FarmstormAttachments`）；内存只留 `Set<UUID>` 观测索引 + `Map<UUID, Long>` 的震动豆到期刻（**不含原值**） | §2.3 |
| 5 | 附件的数据形状 | `Suspend(boolean originalNoAi, int sources)` + `NONE` 哨兵；`Codec` 序列化、`sources == 0` 不落盘；**不存任何时间戳**（裁定 #16） | §2.3 |
| 6 | 震动豆删 AI 的常量 | `AI_SUSPEND_TICKS = 80`（**不**新设 config 键） | §2.3 |
| 7 | mount 查询入口 | `QbounceMountController#isMountMode` 由 `private`（`:199`）改 `public`，**不新增第二个方法** | §2.3 |
| 8 | 敌对谓词的类与方法 | `util/Hostility#isHostile(@Nullable Entity)` | §2.4 |
| 9 | 爆炸来源标记 | `FarmstormExplosion` 第 9 个构造参数 `boolean trapSourced`；保留 8 参重载委托 `false` | §2.5 |
| 10 | 倍率键的字段名 / 值域 / 注释 | `trapDamageMultiplier`；`defineInRange("damageMultiplier", 3.0, 1.0, 10.0)` | §2.5、§4.1.2 |
| 11 | AI 对账的挂载点 | `ServerTickEvent.Post` + `EventPriority.HIGHEST`（必须先于 `QbounceMountController.onServerTick` 的默认优先级） | §2.3 |
| 12 | `verify_project.py` 的新解析契约 | 新增 `expected_config_keys_v021`，按 `#### 4.1.1` ~ `#### 4.1.2` 界定、7 列、行数断言 `1` | §4.1.1 |
| 13 | 无主核的判据 | `rebindNearby` 第一行 `if (this.ownerUuid == null) return;`（现成形态 `emitBoundaryRing:535-537`） | §2.1.3 |
| 14 | 候选的排序键 | `distSqr` 升序 → `x` → `y` → `z` 字典序（与 `selectCore:310-315` 同构） | §2.1.3 |
| 15 | 口径外第 13 类的登记形态 | 附件类型 `farmstorm:ai_suspend` 单列在 §1.2.1：名字 / 注册表 / 不计入 97 的理由 / 工具不核它 | §1.2.1 |

---

## 1. 基线与注册面核对

### 1.1 平台与构建（0.2.0 已发布态）

| 项 | 值 | 证据 |
|---|---|---|
| Minecraft / NeoForge / Java | **1.21.1 / 21.1.213 / 21** | `gradle.properties:2,3`；toolchain `:21-23`（`org.gradle.java.installations.paths=D:/project_farm/.tools/jdk21`） |
| `mod_version` | **0.2.0**（本版改 0.2.1） | `gradle.properties:11` |
| 构建入口 | `.tools\gw.cmd`（唯一入口，无子命令分发） | `一键构建.cmd:8` 只 `call` 它 |
| 运行目录 | `run/` | `run/config/` 下两份 toml 的实际位置 |
| `src/main/java` Java 文件数（现状） | **145** | 本文自己数的（`Get-ChildItem -Recurse -Filter *.java` 全树计数）；本版新增 **4 个类**（§2 逐个点名：`util/AreaBlockEntities`、`combat/MobAiSuspension`、`util/Hostility`、`registry/FarmstormAttachments`），改后 **149** |
| 注册自检入口 | `Farmstorm#logRegistrySelfCheck`（`:154-176`），`Farmstorm.java:80` 调用 | `:167-168` 求和、`:169-172` 逐类断言、`:173` 日志格式串 |
| server config 生成位置 | `FarmstormConfig` 静态块 `:289` ~ `:995`（`SPEC = b.build()` 在 `:995`） | 本文 §1.3 的计数范围 |
| 语言生成位置 | `datagen/ModLangProvider`（`config(entries, …)` 共 **178** 次） | 本文 §1.3 / §4.3 |

### 1.2 注册面逐类点名（**本文自己从源码数**，确认 **97 / +0**）

**计数方法（可复现）**：逐个打开 `src/main/java/com/dashpig/farmstorm/registry/` 下 **11 个**注册类，按「注册调用次数 + 注册 id 字面量」逐条数，**不按 `EXPECTED_COUNTS` 反推、不看 `Farmstorm.java` 的断言值、不抄 `BRIEF` §0**。三条口径：

- `DeferredRegister#register` / `registerItem` / `registerBlock` / `registerComponentType`：数**调用次数**（工厂方法体里的那一次只算一次）；
- 走工厂方法注册的（`cropItem` / `seedItem` / `crop` / `deployed` / `projectile`）：数**工厂调用次数**，id 由 `CropProfile.path()` 或首个参数字面量给出；
- `damage_type` 是**数据驱动**（`FarmstormDamageTypes.java:14` 的注释：实例由数据包 JSON 提供，代码侧只有 `ResourceKey`），数**静态 `ResourceKey` 常量个数**。

| # | registry | 现在数了什么（文件:行号，**UTF-8 行号**） | 逐条 ID | 现状 | 本版 | 改后 |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | `FarmstormItems.java`：**22** 次 `cropItem(...)`（`:37,39,41,43,45,48,50,52,54,56,58,60,62,64,66,71,73,76,80,86,92,96`）+ **7** 次 `seedItem(...)`（`:90,101,102,103,104,105,107`）+ **4** 次 `registerItem(...)`（`:111,114,117,121`） | 22 作物：`puffed_wheat` `blast_carrot` `ubi_server` `toxic_ubi_server` `power_beet` `melon_slice_bomb` `melon_grenade` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `lucky_berry` `molotov` `zangief_cane` `wild_apple` `compressed_puffed_wheat` `friend_chives` `point_defense_fungus` `vibration_bean`；7 种子：`chive_seeds` `puffed_wheat_seeds` `power_beet_seeds` `melon_grenade_seeds` `pumpkin_turret_seeds` `awakened_torchflower_seeds` `awakened_pitcher_plant_pod`；4 方块物品：`converter` `compressed_hay_block` `outpost_core` `cultivator` | **33** | **+0** | **33** |
| 2 | `Registries.BLOCK` | `FarmstormBlocks.java`：**15** 次 `crop(...)`（`:28,29,30,31,32,33,34,35,36,37,38,39,40,41,45`）+ **8** 次 `deployed(...)`（`:48,49,50,52,54,57,64,67`）+ **4** 次 `registerBlock(...)`（`:71,75,84,95`） | 15 作物方块：`puffed_wheat_crop` `blast_carrot_crop` `ubi_server_crop` `toxic_ubi_server_crop` `power_beet_crop` `melon_grenade_stem` `pumpkin_turret_stem` `awakened_torchflower_crop` `awakened_pitcher_plant_crop` `fang_trap_cactus_block` `bamboo_firecracker_stalk` `red_mushroom_puff_block` `brown_mushroom_puff_block` `lucky_berry_bush` `friend_chives_crop`；8 部署方块：`blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `zangief_cane` `point_defense_fungus` `vibration_bean`；4 其它：`converter` `compressed_hay_block` `outpost_core` `cultivator` | **27** | **+0** | **27** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | `FarmstormBlockEntities.java`：**8** 次 `REGISTER.register`（`:25,29,32,35,39,43,51,56`） | `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `converter` `outpost_core` `cultivator` | **8** | **+0** | **8** |
| 4 | `Registries.ENTITY_TYPE` | `FarmstormEntityTypes.java`：**12** 次工厂 `projectile(...)`（`:31,33,35,37,39,41,43,45,49,51,55,57`，工厂体 `:70-78`）+ **1** 次 `REGISTER.register("turret_bullet", …)`（`:61`） | `puffed_wheat` `ubi_server` `toxic_ubi_server` `melon_slice_bomb` `melon_grenade` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `molotov` `compressed_puffed_wheat` `friend_chives` `vibration_bean` `turret_bullet` | **13** | **+0** | **13** |
| 5 | `Registries.MOB_EFFECT` | `FarmstormMobEffects.java:23,27,31,35` | `wild_hunter` `zangief_piledriver` `q_bounce` `confusion` | **4** | **+0** | **4** |
| 6 | `Registries.SOUND_EVENT` | `FarmstormSounds.java:20,25,30` | `wild_hunter_loop` `zangief_piledriver` `friend_chives` | **3** | **+0** | **3** |
| 7 | `Registries.RECIPE_TYPE` | `FarmstormRecipes.java:22` | `converting` | **1** | +0 | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | `FarmstormRecipes.java:24` | `converting` | **1** | +0 | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | `FarmstormCreativeTabs.java:16` | `main` | **1** | +0 | **1** |
| 10 | `Registries.DAMAGE_TYPE` | `FarmstormDamageTypes.java:20,23,26`（3 个 `ResourceKey` 常量） | `void_bolt` `crop_blast` `zangief_slam` | **3** | +0 | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `FarmstormDataComponents.java:15` | `temporary_enchantments` | **1** | +0 | **1** |
| 12 | `Registries.MENU` | `FarmstormMenus.java:17,21` | `converter` `outpost_core` | **2** | +0 | **2** |
| | **合计** | | | **97** | **+0** | **97** |

逐类小计（与上表逐行相等）：

```
items                 33
blocks                27
block_entity_type      8
entity_type           13
mob_effect             4
sound_event            3
recipe_type            1
recipe_serializer      1
creative_mode_tab      1
damage_type            3
data_component_type    1
menu_type              2
--------------------------------
相加 = 33+27+8+13+4+3+1+1+1+3+1+2 = 97
```

**三条独立算法互为校验**：本文逐条点名 = **97**；`Farmstorm#logRegistrySelfCheck`（`Farmstorm.java:154-176`）的 12 条运行期断言相加 = **97**（`:167-168` 求和、`:169-172` 逐类）；`tools/verify_project.py` 的 `EXPECTED_COUNTS`（`:325-338`）= **97**。本文的数**不是**从后两者抄的——它是按上表「调用次数」口径重新数出来的，两条旁证只用来确认口径没跑偏。

**本版 +0 的四条理由（与 D-175 一致，此处只写技术面）**：补绑不新增方块 / 方块实体（它复用 `outpost_core` 与 `cultivator` 两个既有 BE 之间已有的绑定列表）；tooltip 走原版 `AbstractContainerScreen` 链路（§2.2），不新增屏幕类；敌对谓词是 `util/` 里的静态方法（§2.4）；倍率是 config 键（§2.5），不是注册对象。AI 原值的载体是 **`AttachmentType`**（§2.3），它**不进上表 12 类**——理由见下一段。

> ⚠️ **一处会让「+0」误判的口径**：本版新增 4 个 Java 类（§1.1），其中 3 个**一个注册调用都没有**——`AreaBlockEntities` / `Hostility` 是纯函数，`MobAiSuspension` 只持有两个内存容器与一个 `@EventBusSubscriber`；第 4 个 `FarmstormAttachments` **有** 1 个注册调用，但落在 NeoForge registry。若验收按「新增类数」推断注册面，会得出 101 的错误结论；注册面只按上表 12 类的**注册调用**数。

#### 1.2.1 口径外的第 13 类：`farmstorm:ai_suspend`（`ATTACHMENT_TYPES`，**不计入 97**）

裁定 #17 要求这件事**必须显式写出来、不许隐形**——「工具不数的东西等于没登记」，那和 A-66 的假绿是同一性质。所以本版除了上面 12 类，还**另注 1 个附件类型**，逐项点名：

| 项 | 内容 |
|---|---|
| 注册名 | `farmstorm:ai_suspend` |
| 类型 | `AttachmentType<Suspend>`（`Suspend(boolean originalNoAi, int sources)`，见 §2.3.1） |
| 注册表 | **`NeoForgeRegistries.Keys.ATTACHMENT_TYPES`**（`AttachmentType.java:35`：「Data attachment types must be registered to the registry」） |
| 注册点 | 新文件 `registry/FarmstormAttachments.java`：`DeferredRegister<AttachmentType<?>>` + 1 个 `REGISTER.register("ai_suspend", …)`；并在 `Farmstorm.java` 的构造器里与其他 register 一起 `.register(modBus)`（**这是本版唯一改动 `Farmstorm.java` 的地方**，见 §3 #29） |
| 为什么不进 97 | 97 的口径是**本项目 12 类**（§1.2 的表：items / blocks / block_entities / entity_types / mob_effects / sound_events / recipe_types / recipe_serializers / creative_tabs / damage_types / data_components / menu_types），全部落在 `BuiltInRegistries`（伤害类型是数据驱动，数 `ResourceKey`）。`ATTACHMENT_TYPES` 是 **NeoForge 自己新增的 registry**，不在这 12 类里 ⇒ **97 不变**、`data_components` 仍是 **1** |
| 为什么不是「绕过 D-175」 | **附件 ≠ 数据组件**：D-175 否决的是 `data_components`（12 类里的第 11 类，现值 1 个 `farmstorm:temporary_enchantments`），加一个就变 2 ⇒ 97 → 98。本版用的是 **NeoForge 的实体附件注册表**，口径外、97 不变。两句话必须一起读，否则读者会以为「换了个名字绕过了自己的否决」 |
| `verify_project.py` 会不会核它 | **不会**。它对注册面只做两件事：① `EXPECTED`（`:281-321`）的 12 组 id 逐个在源码里 grep 字面量；② `EXPECTED_COUNTS`（`:325-338`）的 12 个计数相加 = 97。附件类型**不在任何一组里** ⇒ 它**既不核存在、也不核计数**。这是已知的覆盖缺口，**本版不扩工具**（D-174；与 A-66 同一类既存问题），靠这一小节 + §3 #29 + §7 #29 三条人工登记兜住 |
| 会不会同步到客户端 | **不会**。没有 `.sync(...)`（`AttachmentType.Builder` 的 `sync` 重载一个都不用），附件只活在服务端与存档 |
| 会不会随死亡复制 | **不会**。`copyOnDeath()` 不调用（默认 `false`，`AttachmentType.java:43`），这是 §2.3.3 路径④ 的前提 |

### 1.3 config 与语言键基线（自己点名）

**SERVER config（现状 175 键 / 60 组）**：数法是读 `config/FarmstormConfig.java` 的静态块（`:289` 的 `static {` 到 `:995` 的 `SPEC = b.build();`），按「`b.push("…")` / `b.pop()` 维护组栈、行首 `.define(...)` / `.defineInRange(...)` 计一键」逐键落地。本文跑了两遍并逐组人工复核：

```
行首 .define* 的行数           = 175   ← 全部是 live 定义
被注释的定义                   =   1   ← :935 的 // .defineInRange("farmlandBonus", 16, 0, 1024);
b.push(...) 次数               =  60
b.pop() 次数                   =  60
                                    ⇒ 175 键 / 60 组
```

三个可复现的旁证：① `.define` 字面命中 **177** 行 = 175 live + 1 注释行（`:935`）+ 1 行只提到 `.defineInRange` 的注释（`:229`，讲的是 `:909-912` 那段历史）；② `run/config/farmstorm-server.toml` 的段数**现在不可信**（见 §9 第 2 条），只能用源码数；③ `trap` 段落在 `:917`（`push`）~ `:927`（`pop`），三键在 `:920` / `:923` / `:926`，与 `BRIEF` §1.6 一致。

**CLIENT config（现状 3 键）**：`config/FarmstormClientConfig` 的 `hud.enabled` / `hud.x` / `hud.y`（`builder.push("hud")` 一次）。本版**不动**，且**不计入** 175（`BRIEF` §0 的既有口径），但**计入** §4.3 的语言键。

**语言键（现状 337 × 2，中英键集完全相同）**：读 `src/generated/resources/assets/farmstorm/lang/zh_cn.json` 与 `en_us.json`，`ConvertFrom-Json` 后取键集：`zh = 337`、`en = 337`、zh-only = **0**、en-only = **0**。两条可复现的旁证：`farmstorm.configuration.*` 共 **178** 条（= 175 server + 3 client）、`tooltip.farmstorm.*` 共 **20** 条（与 `BRIEF` §0 一致）；`datagen/ModLangProvider` 里 `config(entries, "…")` 恰好 **178** 次，与语言文件的 178 条一一对应。

### 1.4 两条红线（网络包仍 2 个、Mixin 0）

| 项 | 现状（本文复核） | v0.2.1 | 证据 |
|---|---|---|---|
| 自定义网络包 | **2 个**：`farmstorm:core_list`（`FarmstormNetwork.java:38`）、`farmstorm:cultivator_link`（`:43`），协议版本 `"2"` | **2 个，不新增** | `FarmstormNetwork.java:25`（`PROTOCOL_VERSION = "2"`）、`:38` / `:43` 两处 `.playToClient`；全仓 `playToClient\|playToServer` 命中 **2** 处注册调用 |
| Mixin | **0 个** | **0 个**，继续禁止 | `src/` 全树 `@Mixin` 命中 **0**；`build.gradle` / `gradle.properties` / `neoforge.mods.toml` 无 mixin 段；`src/` 下 `*.mixins.json` **0** 个（⚠️ 全仓有 1 个在 `.scratch/p25a/src/neoforge.mixins.json`，是实验残留、不在 `src/`、不参与构建，见 §9 第 10 条） |

两条读法（写错任一条，§7 的 #2 会红）：

1. 网络包**不进注册面**——payload 走 `RegisterPayloadHandlersEvent`，不写 `BuiltInRegistries`，所以 §1.2 的 97 里没有它。
2. Mixin 的 0 是**禁项**，不是「本版没轮到」：本版五个构件全部用继承 + 事件总线 + 原生扩展点实现（尤其**没有**用 Mixin 去改原版 `EvokerFangs` 实现尖牙 ×3，裁定 #11 + D-186）。

---

## 2. 五个技术构件

### 2.1 周期补绑（相位门独立 / 3×3 已加载区块枚举 / <16 / 与放置绑定的互斥）

#### 2.1.1 两套门必须各自独立（本版最大的实现陷阱）

`serverTick`（`OutpostCoreBlockEntity:507-525`）现在只做**三件事**，且是**顺次早退**的：

```
:508-510  非 ServerLevel          → return
:511-513  interval <= 0            → return        （interval = outpost_core.boundaryParticleIntervalTicks，默认 40、范围 20~1200，:939-941）
:515      now = getGameTime()
:516-518  粒子相位门不成立         → return        （floorMod(now + floorMod(pos.hashCode(), interval), interval) != 0）
:519      emitBoundaryRing
:521-524  每 TICK_LOG_ROUNDS(=30) 轮一条 INFO
```

**陷阱**：把补绑接在 `:519` 之前或之后（即粒子门下游），补绑只在「粒子门恰好放行」的那些 tick 才跑 ⇒ 实际周期 = `LCM(40, 100) = 200` 刻，且 `boundaryParticleIntervalTicks` 一改（20 / 60 / 1200）周期就跟着漂：设成 1200 时补绑变成 1200 刻一次，需求「每 100 刻」当场失效。

**本文确定（1）**：补绑**自己一套门**，且门放在粒子门**之前**、用独立 `if`、**不 `return`**。改造后 `serverTick` 是四件事：

```
public static void serverTick(Level level, BlockPos pos, BlockState state, OutpostCoreBlockEntity core) {
    if (!(level instanceof ServerLevel server)) return;              // 原 :508-510，不动
    long now = server.getGameTime();                                  // 原 :515 上移一行（纯取数、无副作用）
    if (rebindDue(pos, now)) {                                        // 新增：独立门
        core.rebindNearby(server);                                    // 新增：不 return
    }
    int interval = FarmstormConfig.outpostCoreBoundaryParticleIntervalTicks.get();  // 原 :511
    if (interval <= 0) return;                                        // 原 :512-513，不动
    if (Math.floorMod(now + Math.floorMod(pos.hashCode(), interval), interval) != 0) return;  // 原 :516-518，不动
    core.emitBoundaryRing(server, pos, now, interval);                 // 原 :519，不动
    // 原 :521-524 的日志段，不动
}
```

- **唯一的结构性改动是把 `:515` 的 `now` 上移一行**（`emitBoundaryRing` 与日志段用的还是同一个 `now`，无第二次取数）。其余既有行一字不改——这满足 D-174 的「不顺手改 0.2.0 原有内容」：上移是补绑门的**必要条件**，不是顺手优化。
- 两门**互不阻塞**：补绑为真时粒子门仍按自己的相位判一次，二者之间没有 `return` 交叠。
- 相位错开：`floorMod(pos.hashCode(), 100)` 落在 `0~99`，同一 3×3 里多个核各占一个 tick，不会同刻一起扫（裁定 #2）。

**本文确定（1 续）**：常量与门方法（写在 `OutpostCoreBlockEntity` 的常量区，紧邻 `:91` 的 `TICK_LOG_ROUNDS`）：

```
private static final int REBIND_INTERVAL_TICKS = 100;      // plan:9 的「每100刻」，不是 config 键

private static boolean rebindDue(BlockPos pos, long now) {
    return Math.floorMod(now + Math.floorMod(pos.hashCode(), REBIND_INTERVAL_TICKS),
                         REBIND_INTERVAL_TICKS) == 0;
}
```

**不设 `interval <= 0` 守卫的理由**：`REBIND_INTERVAL_TICKS` 是编译期常量 100，`floorMod(x, 100)` 恒不抛；而粒子门那个守卫（`:512-513`）是为了防 `interval` 配成 0 时除零（config 范围取不到，但编译期证明不了）。两套门的守卫**不能互相借用**——这正是「复用粒子门」会连带出错的第二处。

#### 2.1.2 「区域枚举方块实体」：全仓 0 处先例

**现成实现**：`ChunkMap` 0 处；`LevelChunk#getBlockEntities()` 仅 **1** 处（`DeployedCropBlockEntity:227-236` 的 `countDeployedInChunk`，**单区块、只数自己**，`:230` 遍历 `.values()`）；`level.getAllEntities()` 2 处（`QbounceMountController:220`、`QbounceObserverSync:164`，都是实体、不是方块实体）。⇒ 必须自己写。

**本文确定（3）**：新增 `util/AreaBlockEntities.java`，唯一方法：

```
public static <T extends BlockEntity> List<T> collect(ServerLevel level, BlockPos center,
                                                      int chunkRadius, Class<T> type)
```

四条实现纪律（每一条都对应一个会翻车的点）：

1. **只走 `ServerChunkCache#getChunkNow(int,int)`，禁止 `Level#getChunk` / `getChunkAt`**：`getChunkNow` 取不到已加载区块时返回 `null` ⇒ **跳过该区块**（不强制加载，裁定 #2）。现成形态：`OutpostCoreBlockEntity:296-300`（选核）与 `:384-387`（惰性清理）都是这么写的，本版不破这个惯例。`Level#getBlockEntity(pos)` 也不能用——它内部走 `getChunkAt`，**会触发加载**（`:264-267` 的注释已把这条写死）。
2. **区块范围 = `RANGE_CHUNKS`（`:75`，值为 1）**：`cx-1..cx+1` × `cz-1..cz+1` = **9 个区块**，与 `inRange`（`:454-465`）的切比雪夫 ≤ 1 是同一组数字。**不新设第二个半径常量**——`:74` 与 `:77` 的注释已经把「与 D-118、D-121 同一组数字」写成纪律，本版不破例。
3. **返回新 `ArrayList`，不是 `chunk.getBlockEntities()` 的视图**：`LevelChunk#getBlockEntities()` 返回的是**该区块自己的活 `blockEntities` 映射**（`LevelChunk.java:528-530`，`return this.blockEntities;`），遍历期间若发生方块实体增删会 `ConcurrentModificationException`。本版照 `QbounceMountController.adoptExisting`（`:213` 的注释「先收集再挂，避免边遍历实体列表边 addFreshEntity」）的**两阶段**写法：先收集成新列表，再在第二阶段写绑定。
4. **按 `Class<T>` 过滤，不按 `BlockEntityType` 查**：补绑候选就是 `CultivatorBlockEntity`（`instanceof` 语义与 `DeployedCropBlockEntity:231`、`OutpostCoreBlockEntity:301`、`:689` 同形）。`type.isInstance(be)` 即可，不需要 `FarmstormBlockEntities.deployedOf` 那套 kind 分流。

> 📌 **「跳过未加载」是防御分支，不是常态**：核的自有票 `CoreChunkTickets` 给中心区块 `distance = 1`（`CoreChunkTickets.java:17-18`、`:43-44` 的 `DISTANCE = 1`），按 `:25-29` 的口径 ⇒ **中心区块 entity-ticking（方块实体照常 tick）、邻圈 8 区块已加载到 FULL** ⇒ **3×3 常态下 9 个区块全部在内存**。所以补绑不是「大多数轮次扫不到东西」的低频行为，而是**每 100 刻稳定扫满 9 个区块**；「跳过未加载」只在三种情况下生效：① 票尚未生效（核刚放下 / 起服补票之前，`:64-68` 的 `restoreAll`）；② 票被别的路径释放过；③ 无主核（但那种核已被 §2.1.3 的第一行挡掉，根本走不到枚举）。**写日志与写验收都按「常态扫满 9 块」这个前提**，不要写成「偶尔扫一次」。

**成本**：每核每 100 刻扫 9 个区块的 `blockEntities` 映射。9 个区块的方块实体数是「几十」量级，100 刻一次 ⇒ 折算到每刻不足 1 次映射遍历。这是 `:326` 那句「每刻 × 16 条 × 核数，代价不成比例」的正面答案：**不是每刻、也不扫 16 条**。

#### 2.1.3 写入边界：只补未绑定、不夺已绑、<16 才补、满即停

**本文确定（1 续）**：`rebindNearby` 的判据与顺序：

```
private void rebindNearby(ServerLevel server) {
    if (this.ownerUuid == null) return;                                 // 裁定 #13：无主核不补绑
    int cap = FarmstormConfig.outpostCoreCultivatorCapacity.get();      // :949-951，默认 16
    if (cultivatorCount() >= cap) return;                               // 满则整轮跳过（不枚举）
    List<CultivatorBlockEntity> candidates =
            AreaBlockEntities.collect(server, getBlockPos(), RANGE_CHUNKS, CultivatorBlockEntity.class);
    candidates.sort(Comparator                                          // 裁定 #14：距离升序 → 坐标字典序
            .comparingDouble((CultivatorBlockEntity c) -> c.getBlockPos().distSqr(this.getBlockPos()))
            .thenComparingInt(c -> c.getBlockPos().getX())
            .thenComparingInt(c -> c.getBlockPos().getY())
            .thenComparingInt(c -> c.getBlockPos().getZ()));
    for (CultivatorBlockEntity cultivator : candidates) {
        if (cultivatorCount() >= cap) break;                            // 每绑一个重读，满即停
        if (cultivator.corePos() != null) continue;                      // 已绑（含指向别的核）⇒ 跳过，不夺
        if (hasCultivator(cultivator.getBlockPos())) continue;            // 保守：核侧已有 ⇒ 跳过
        if (addCultivator(cultivator.getBlockPos())) {                    // 先写核侧
            cultivator.setCore(this.getBlockPos());                       // 再写培养器侧缓存（两侧都写）
        }
    }
}
```

七条硬约束（逐条给理由）：

1. **容量读 `cultivatorCount()` / `isCultivatorListFull()`（`:334`、`:343`），不读 `cultivators()`（`:328`）**：后者会先调 `pruneCultivators()`（`:329`、`:380-391`）并可能 `setChanged`，与补绑在同一循环里交错会让「本次已绑的坐标」在后续判定中被重复或漏计（D-176 补充）。
2. **`addCultivator` 自己会拒绝重绑与满员**（`:352`），但补绑循环**仍要**在 `cap` 处 `break`——否则满员后还会对剩下几十个候选逐个调一次「注定返回 false」的方法，白跑一遍枚举（RT-53）。
3. **不调用 `removeCultivator`（`:366-377`）**：补绑**只加不摘**（D-177）。「两侧不一致」的候选一律**跳过**而不是「先绑再摘」——D-165 对「使用前校验不一致」的处置是按未绑定（不可使用），补绑若照做会造出第二个主。该方法的调用点因此**仍是 0**，本版不启用、不删除（40 号 §7 ① 交给 42 号定的那一问，结论就是这一条）。
4. **不额外调广播**：`addCultivator` 里已经 `setChanged() + syncBindings() + CultivatorLinkBroadcaster.requestAll(...)`（`:356-362`）。一轮最多 16 次 `requestAll`，而它有 10 刻去抖（`CultivatorLinkBroadcaster.MIN_INTERVAL_TICKS = 10`，`:54`），合并后最多多发一次包。补绑**再加**一次请求只会增加噪声。
5. **每轮最多补满一次**：同一轮里绑到 `cap` 就 `break`，下一轮（100 刻后）若又有空位再补。不做「一次扫完所有空位再回来补」的两遍循环——那会让「满则停」的边界变模糊。

**两条新裁定的落点（#13 / #14，逐条给理由）**：

6. **无主核不补绑（裁定 #13）**：`/setblock` 放出来的核 `ownerUuid == null`（`setOwner` 是唯一的写入点，`:410-419`），此时「己方」无从判定、16 个名额也无主，补绑它等于把别人的培养器记到一个没有主人的核名下。判据放在方法**第一行**（在容量与枚举之前），现成形态是 `emitBoundaryRing` 的 `:535-537`（`if (this.ownerUuid == null) return;`，注释写「`/setblock` 出来的核心没有归属者」）。⚠️ 这一条**只**加在补绑上：`emitBoundaryRing` 的那一处是既有代码，本版一字不改。
7. **候选排序确定化（裁定 #14）**：一轮扫到的候选通常多于空位，`List` 的枚举顺序来自 `chunk.getBlockEntities()` 的 `HashMap` 迭代序 ⇒ **不排序就无法保证同一存档两次运行绑到同一批**（`HashMap` 的顺序在同一 JVM 版本内稳定、但换版本 / 换扩容历史就会变）。排序键与 0.2.0 放置时的 `selectCore:310-315` **同构**：`distSqr` 升序 → `x` → `y` → `z`。注意这里**没有** `placedGameTime` 这一级——培养器没有「放置时刻」字段，也没有 UUID 可比；坐标三元组在同一个世界里对同一格是**唯一**的，足以定序（`selectCore` 之所以多一级 `placedGameTime`，是因为它排的是**核**，核有那个字段）。

#### 2.1.4 与「放置绑定」的互斥（RT-51）

两个写入时点（放置 `CultivatorBlock:86-102` 的 `:99`+`:100`、迁移 `OutpostCoreBlockEntity:685-691`）与第三个（本节的补绑）**全部在服务端主线程**，且都遵守同一条顺序：**先写核侧 `addCultivator`，成功后才写培养器侧 `setCore`**。因此「同一格被两个核各写一份」只剩一个窗口：**本轮扫描与放置写入恰好同 tick**。

- 同一 tick 内，玩家交互（`setPlacedBy`）在实体 tick 阶段之前完成，核的 `serverTick` 在其后；两个核的 `serverTick` 由区块 tick 顺序单线程先后执行。
- 先写者写完 `setCore` 后，后到者读 `cultivator.corePos() != null` ⇒ 跳过。
- 因此**不需要额外的锁、不需要 `removeCultivator` 兜底、不需要「扫描后再复核一遍」**——`setCore` 的同步写就是互斥面。
- 候选列表里装的是**活的 `CultivatorBlockEntity` 引用**（来自 `getBlockEntities()` 的实例），所以 `setCore` 在本路径上不可能遇到 `null`；「核侧写成功、培养器侧写失败」这条半写路径在补绑里不可达（迁移路径另有出口③，`:695-704`）。

### 2.2 核心储物 tooltip（为何默认链路已足够 / 什么情况会破 / 要不要显式补 `renderTooltip`）

**结论（本文确定）**：**不新增、不覆写**任何渲染方法。`OutpostCoreScreen`（42 行）保持「只覆写 `renderBg`（`:37-41`）」，交付物是**一条显式验收**（§7 #11 ~ #13），不是一段新代码。这是 D-178 的「优先不改」，也是 RT-59 的正面回答。

**为什么默认链路已足够（四道证据）**：

1. `OutpostCoreScreen` 只覆写 `renderBg`（`:37-41`），**没有** `render` 覆写 ⇒ 不存在「覆写 `render` 后忘了调 `super`」这个最常见的破法。
2. **该类**里 `render` / `renderTooltip` / `renderLabels` / `init` 覆写**均为 0**（全类只有 `renderBg` 一个覆写）；**全仓** `renderTooltip` 命中 **0** 处 ⇒ 没有任何一层把 tooltip 拦掉或换成别的东西。（对照：`ConverterScreen:77` 覆写的是 `renderLabels`、`CoreHudDragScreen:66` 覆写的是 `render`——它们不是核心储物界面，与本条无关，也不得顺手改。）
3. 54 个存储格 + 36 个玩家格全是**原版 `net.minecraft.world.inventory.Slot` 实例**（`OutpostCoreMenu:68`、`:79`、`:84`），没有自定义 `Slot` 子类去改 `isActive()` / `getItem()` 这类决定「悬停有没有东西」的行为。
4. 版式是原版双箱：`IMAGE 176×222`、`TEXTURE 256×256`（`:24-28`）、`generic_54.png` ⇒ 槽位坐标与贴图对得上，`hoveredSlot` 不会因坐标错位恒为 `null`。

原版链路本身是：`AbstractContainerScreen#render` 末尾调 `renderTooltip(guiGraphics, mouseX, mouseY)`，后者只在 `hoveredSlot != null && hoveredSlot.hasItem()` 时画。这四道证据分别对应它的四个前置条件，全部满足。⚠️ 原版调用点的行号本机不可核（`BRIEF` §1.2 只给本仓证据），所以**验收只能写成「悬停可见」，不能写成「`render` 里第 N 行调用了它」**。

**什么情况会破（五条，逐条给出现状与判据）**：

| # | 破法 | 现状 | 判据（写进 §7） |
|---|---|---|---|
| 1 | 有人覆写 `render` 且不调 `super` | 0 处 | `OutpostCoreScreen` 的 `render` 覆写数 = 0 |
| 2 | 有人覆写 `renderTooltip` 换成自绘 | 0 处（全仓命中 0） | 全仓 `renderTooltip` 命中 = 0 |
| 3 | 槽位坐标 / `imageWidth` 与贴图错位 ⇒ `hoveredSlot` 恒 `null` | 现版式正确 | 悬停第 1 / 28 / 54 格都能出 tooltip |
| 4 | 自定义 `Slot#isActive()` 返回 `false` | 全原版 `Slot` | `OutpostCoreMenu` 里 `new Slot(` 之外无 `Slot` 子类 |
| 5 | `stillValid` 恒 `false` ⇒ 界面被关 | 走原版语义（`:133-134`） | 打开后站在 8 格内界面不关 |

**要不要显式补 `renderTooltip`**：**不补**。补了只会得到两种结果之一——要么与原版重复画两次 tooltip，要么遮掉原版对定位 / 缩放 / 背景 / 延迟显示的处理（D-178 已否决「重写 `render` 自绘 tooltip」）。**退路（仅当 §7 #11 失败时启用）**：在那次改动里给 `render` 覆写补一次 `super.render(...)`（或显式调一次 `renderTooltip`，且**不**重复调 `super.render`），改动量 ≤ 2 行，仍然不新增渲染代码。

### 2.3 AI 干预与恢复（载体选择 / 记原值 / **五条**清理路径 / RT-54 的残留风险）

#### 2.3.1 载体：落盘在实体上的 `AttachmentType`（裁定 #15），内存只留索引

裁定 #15 定死两件事：**原值记在实体上（DataAttachment 一类）**，且**恢复后必须立刻删除该条数据**。四个候选逐条比对（`BRIEF` §7 授权本文定技术形态）：

| 候选 | 能否扛住停服 / 崩溃 | 与裁定 #15 / D-175 的关系 | 结论 |
|---|---|---|---|
| 纯 `Map<UUID, Boolean>` 内存表 | 停服可（若加归还），**崩溃不行**：实体 NBT 里留着 `NoAI:1b`、表没了 ⇒ 原值丢失 | 裁定 #15 明确「不是内存 `Map<UUID,Boolean>`」 | **否决** |
| `DataComponentType`（0.2.0 已有的 `temporary_enchantments` 那一类） | 能 | 它是 §1.2 的**第 11 类**（现值 1），加一个就变 2 ⇒ **97 变 98**；D-175 否决的正是它 | **否决**（D-175 继续有效） |
| 恢复判据改成「按效果 / 计时器反推」，不存原值 | Q弹 可以，但**推不出原值** | 与裁定 #8「恢复**原值**」冲突（一律恢复成有 AI 就是 `WildHunterAura:119` 的旧口径） | **否决** |
| **`AttachmentType<Suspend>`** | 能：`serialize(Codec)` 随实体落盘（`AttachmentHolder.serializeAttachments:122-145` / `deserializeAttachments:151-172`），崩溃后下次载入读得到；恢复后 `removeData` 立刻清干净 | 注册在 `NeoForgeRegistries.Keys.ATTACHMENT_TYPES`（`AttachmentType.java:35`），**不在 §1.2 的 12 类里** ⇒ **97 不变**；与 D-175 否决的「数据组件」不是同一种东西（§1.2 的下一段 + §9 第 14 条） | **采用** |

**本文确定（4 / 5）**：数据类 `combat/MobAiSuspension.java`（同包于唯一先例 `WildHunterAura`，便于并列阅读；它不是无状态工具，所以不放 `util/`）+ 注册类 `registry/FarmstormAttachments.java`。形状：

```
public record Suspend(boolean originalNoAi, int sources) {          // 只有两个字段：不存任何时间戳（裁定 #16）
    public static final Suspend NONE = new Suspend(false, 0);
    public static final int SRC_VIBRATION_BEAN = 1, SRC_Q_BOUNCE = 2;      // 来源位掩码
    // Codec：record → CompoundTag 手写（两个字段，不引 Codec 组合子，与 TempEnchantRecord 同风格）
}

public static final DeferredHolder<AttachmentType<?>, AttachmentType<Suspend>> AI_SUSPEND =
        REGISTER.register("ai_suspend", () -> AttachmentType.builder(() -> Suspend.NONE)
                .serialize(Suspend.CODEC, s -> s.sources() != 0)          // NONE 不落盘
                .build());
```

四条设计取舍：

1. **为什么用「来源位掩码」而不是 `refCount` + `EnumSet`**：`int` 比 `EnumSet` 好序列化（`Codec.INT` 就够）；「还有几个来源没退出」= `sources != 0`，不需要单独计数。**同一来源重复进入**（同一只怪先后被两颗震动豆命中）时**不翻倍**，`sources` 位不变、到期刻在内存里取 `max`（延长到后一次的到期）——语义是「定身到最晚那次到期」。
2. **`sources == 0` 是唯一的存在判据**：`getData(TYPE)`（`AttachmentHolder.java:74`）在没挂过时返回默认值 `NONE`，所以判「是否处于挂起」只看 `sources != 0`，**不看 `hasData`**（`:68`）——默认值被取用之后 `hasData` 也可能为真，两个口径混用会造出「空的挂起记录」（§8 ⑩）。
3. **落盘只有「原值 + 来源」，没有任何时间戳（裁定 #16）**：震动豆的 80 刻到期刻是**运行期**数据，放内存（`Map<UUID, Long>`）而不进附件——这样「窗口不跨档」是**结构上**成立的（存档里根本没有到期刻可读），而不是靠一段清理代码。裁定 #16 同时否掉了「把到期时刻存进附件」：那正是 40 号 D-179 批评过的「记时点」形态（`WildHunterAura:52` 的 `AI_RESTORE_AT`）。
4. **恢复即删**（裁定 #15 的硬要求）：`release()` 是唯一出口，末尾一定是 `mob.removeData(AI_SUSPEND)`（`AttachmentHolder.java:107`）——不 `setData` 成 `NONE`，`removeData` 之后的 `getData` 会回落到 `NONE`，语义与「从没挂过」完全一致。**落盘面**是 `AttachmentHolder.ATTACHMENTS_NBT_KEY = "neoforge:attachments"`（`:27`），`Entity extends AttachmentHolder`（`Entity.java:131`）⇒ 键在本模组命名空间下、随实体走，不污染别的模组。

#### 2.3.2 记原值：只记第一次进入前的那一个

`WildHunterAura` 的先例**不合本定义**（`AI_RESTORE_AT:52` 存的是「到期时刻」、恢复写死 `setNoAi(false)`，`:119`）——它会把「进入前本来就 `noAi`」的实体**错误地放出来**。本版改为：

```
suspend(mob, sourceBit, vbDeadline):        // vbDeadline 只在 sourceBit == SRC_VIBRATION_BEAN 时给出
    Suspend cur = mob.getData(AI_SUSPEND);              // 没挂过 ⇒ NONE
    if (cur.sources() == 0):
        boolean original = mob.isNoAi();                // 先读，再改（顺序不能反）
        mob.setData(AI_SUSPEND, new Suspend(original, sourceBit));   // 先落盘：原值 + 来源
        mob.setNoAi(true);                              // 再改状态
    else:
        // 已经在挂起：原值一律不动，只加来源位
        mob.setData(AI_SUSPEND, new Suspend(cur.originalNoAi(), cur.sources() | sourceBit));
    if (sourceBit == SRC_VIBRATION_BEAN):
        VB_DEADLINE.merge(mob.getUUID(), vbDeadline, Math::max);     // 到期刻只在内存，永不落盘
    OBSERVED.add(mob.getUUID());                        // 观测索引（不含原值）

release(mob, sourceBit):
    Suspend cur = mob.getData(AI_SUSPEND);
    int left = cur.sources() & ~sourceBit;
    if (left == 0):
        mob.setNoAi(cur.originalNoAi());                // 按原值恢复
        mob.removeData(AI_SUSPEND);                     // 立刻删（裁定 #15）
        VB_DEADLINE.remove(mob.getUUID());              // 内存两件一起清
        OBSERVED.remove(mob.getUUID());
    else:
        mob.setData(AI_SUSPEND, new Suspend(cur.originalNoAi(), left));
        if (sourceBit == SRC_VIBRATION_BEAN) VB_DEADLINE.remove(mob.getUUID());
```

- **顺序写死**：先 `getData` 读原值 → 先写附件 → 再 `setNoAi(true)`。反过来的话，崩在 `setNoAi` 与写附件之间，就会留下「实体没 AI、却没有原值」的永久脏状态（RT-54 的残留风险第一个入口）。
- **同一实体被两处同时删 AI**（先中震动豆、再中 Q弹）：原值只记**第一次**进入前的读数（D-179 补充）。后进入者若覆盖原值，会把「已被本版改成 `true`」的读数当成原值，恢复时把实体永久锁死。
- **`sources` 位掩码**替代 D-179 交给本文定的「进入计数的形式」：`sources` 清零才真正恢复，且「震动豆到点了但 Q弹 还在」不会提前恢复（`left != 0` 走 else 分支）。
- **只有 `SRC_VIBRATION_BEAN` 有内存到期刻**（`VB_DEADLINE`）：80 刻是运行期时长，按裁定 #16 不跨档；`SRC_Q_BOUNCE` 的「到期」根本不用记——判据是「效果还在不在」（§2.3.4）。
- **只对 `Mob`**：谓词与 `WildHunterAura:88` 同形（`instanceof Mob`），玩家恒不在表里（玩家不是 `Mob`）。
- **`OBSERVED` / `VB_DEADLINE` 与附件的关系**：内存两件可以丢、可以脏，**原值不会丢**——它只在附件里。这是裁定 #15 相对纯内存表的关键收益。

#### 2.3.3 五条清理路径（裁定 #15 的「恢复后必须立刻删除，不许留残留」）

| 路径 | 触发 | 谁清理 | 清理动作 |
|---|---|---|---|
| ① Q弹 自然到期 | `LivingEntity.java:790-792` 把效果摘掉 | 每刻对账（§2.3.4）：`!hasEffect` | `release(entity, SRC_Q_BOUNCE)` → 按原值恢复 → `removeData` |
| ② `/effect clear` | `EffectCommands` → `removeAllEffects()`（`LivingEntity.java:911-921`） | 同上 | 同上 |
| ③ 牛奶 | `MilkBucketItem.java:27` → `removeEffectsCuredBy(MILK)`（`LivingEntity.java:3517-3525`） | 同上 | 同上 |
| ④ 实体死亡 / 被移除 | `LivingDeathEvent`，或对账读到 `isDeadOrDying()/isRemoved()` | 对账 | **不写 noAi**（它已经死了），只 `removeData` + 删索引。⚠️ 实体的可序列化附件**默认不随死亡复制**（`AttachmentType.java:43`）⇒ 不会污染掉落物 / 复活实体，本条不会造出第二个宿主 |
| ⑤ 区块卸载后重新载入 | 实体随区块落盘（`neoforge:attachments`），下次载入 | `EntityJoinLevelEvent` → `adopt` | 读到 `sources` 后**先结算震动豆那一路**（裁定 #16：窗口不跨档 ⇒ `release(SRC_VIBRATION_BEAN)`，只保留原值语义）；Q弹 那一路按「效果是否还在身上」继续（§2.3.4） |

- **为什么「区块卸载」也算一条**：卸载时实体被写进区块 NBT（`NoAI:1b` 与附件一起），这一刻**不能**恢复（实体不在世界里，`setNoAi` 没有对象可写）。所以本版的处理是「**载入时结算**」：震动豆那一路在载入时**一律视为已到期**（早恢复无害，见下条）；Q弹 那一路靠效果本身在存档里，天然跨档。
- **震动豆的窗口不跨档（裁定 #16）**：80 刻的到期刻只活在内存（`VB_DEADLINE`），**附件里根本没有它**。于是「跨档」这件事在结构上不可能发生：一次存档 / 载入之后，载入侧的 `adopt` 只能读到「有 `SRC_VIBRATION_BEAN` 来源」而读不到「还剩几刻」，按裁定 #16 一律**按原值恢复 + 删该来源的记录**。代价：生物可能只停手 2 秒而不是 4 秒——罕见（要求它恰好在 80 刻窗口内被卸载），且**提前恢复无害**。裁定 #16 同时**否决**了「把到期时刻也存进附件」：那是 40 号 D-179 已经批评过的「记时点」形态（`WildHunterAura:52` 的 `AI_RESTORE_AT` 就是它）。
- **停服是 ⑤ 的特例**（所有实体都「卸载」了）：本版仍然挂 `ServerStoppingEvent`（先例 `QbounceMountController:157`、`FarmstormGameEvents:191`）做一次**尽力结算**（能写就写：震动豆一律恢复、Q弹 按效果判），目的只是让存档干净、少一次载入时的修正；**正确性不依赖它**——即使那一轮因为区块已卸载而漏了，附件会在下次载入被 `adopt` 接住。

**落盘形状（本文确定（5））**：附件 `farmstorm:ai_suspend`，值为 `Suspend(originalNoAi, sources)`，随实体写进 `neoforge:attachments`（`AttachmentHolder.java:27` 的键名、`:122-145` 的写出、`:151-172` 的读回）。`sources == 0`（`NONE`）**不写**（`serialize(codec, s -> s.sources() != 0)`）⇒ 正常状态下存档里**一个字节都没有**。

**`adopt` 的三条判据**（载入时的结算口径，宁可早恢复、不可永久锁死）：

1. **先结算震动豆**：`sources` 含 `SRC_VIBRATION_BEAN` ⇒ 一律 `release(SRC_VIBRATION_BEAN)`（不读任何时间戳，附件里也没有）。若它是唯一来源 ⇒ 按原值恢复 + 删附件；若还含 Q弹 ⇒ 只清这一位，继续挂起。
2. **再判 Q弹**：`sources` 含 `SRC_Q_BOUNCE` 且 `!mob.hasEffect(Q_BOUNCE)` ⇒ `release(SRC_Q_BOUNCE)`（同样可能触发恢复 + 删附件）；效果还在 ⇒ 加回 `OBSERVED`，交给对账继续收敛。
3. **`originalNoAi == true` 的实体**（进入前本来就没 AI）：恢复时**仍写回 `true`**，不是写 `false`——这正是裁定 #8 与旧先例 `WildHunterAura:119` 的分界。

> 📌 裁定 #16 顺带**消掉了一个候选设计**：如果采纳「把到期刻也落盘」，就必须再配一条「时间倒流守卫」（`deadline - now > 80` 就按已到期处理，先例 `OutpostCoreBlockEntity:540-541` 的 `LAST_RING_TICK`）。现在落盘的记录里**没有任何时间戳**，回档 / 复制世界都不需要特判——这是「不存时点」相对「存时点 + 各种兜底」的直接收益，登记在 §9 第 16 条。

#### 2.3.4 Q弹：恢复判据按「效果是否还在身上」，不配一次性计时器（RT-56）

**为什么不能用计时器**：`FriendChivesProjectile` 落地时读 `friend_chives.effectTicks` 当**场长**（`:82`），之后**每刻重扫**并写「场长 − 已过刻数」（`:155-172`，`:156` 算 `ticks`、`:167` 施加）。同一次场里效果被**反复施加**、时长逐刻递减。任何「第一次施加时配一个 N 刻计时器」都会：① 被每刻的重复施加重置（永不恢复）；或 ② 在效果还在时提前恢复（定身被中途解除）。

**本文确定（4 续）**：进入面与退出面**分成两处**，因为两处的可用钩子不对称（§8 ③ 已核）：

- **进入**：`QbounceEffect` 覆写 `MobEffect#onEffectAdded(LivingEntity, int)`（`MobEffect.java:86`），转发给 `MobAiSuspension`。这个方法**只在「之前没有同一效果」的分支被调用**：`LivingEntity.addEffect` 在 `:957-961` 判定 `mobeffectinstance == null` 时才 `p_147208_.onEffectAdded(this)`，重扫刷新走 `:962` 的 `update`、**不触发**。⇒ 反复施加不会重复写附件（`sources` 位掩码不变）。
- **退出**：**不在效果类里做**——21.1.213 的 `MobEffect` **没有 `onEffectRemoved`**（只有 `:86` 的 `onEffectAdded`）。退出由**每刻对账**读 `hasEffect` 决定：

```
sweep(server):                                      // 每刻，O(索引大小)
  for id in copyOf(OBSERVED):                       // 遍历副本：release 会改索引
      Mob mob = resolve(server, id)                 // 逐维度 getEntity
      if (mob == null):
          OBSERVED.remove(id); VB_DEADLINE.remove(id)   // 观测不到 ⇒ 只丢内存两件，附件一律留着
          continue                                      // 载入时由 adopt 结算（§2.3.3 判据 1/2）
      if (mob.isRemoved() || mob.isDeadOrDying()):  release 全部来源（删附件，不写 noAi）   // 路径④
      else:
          Suspend s = mob.getData(AI_SUSPEND)
          if (s.sources() 含 Q_BOUNCE && !mob.hasEffect(Q_BOUNCE)) release(SRC_Q_BOUNCE)     // 路径①②③
          if (s.sources() 含 Q_BOUNCE && QbounceMountController.isMountMode()) release(SRC_Q_BOUNCE)  // §2.3.5 第二层
          if (s.sources() 含 VIBRATION_BEAN && now >= VB_DEADLINE.getOrDefault(id, now)) release(SRC_VIBRATION_BEAN)
```

> 实现注记：同一次对账里可能连着触发两次 `release`（Q弹 与震动豆同刻到期）。第二次之前必须**重读** `mob.getData(AI_SUSPEND)`——第一次可能已经把附件删掉、`sources` 归零；拿着旧的 `s` 再 `release` 一次虽然幂等，但日志会翻倍、`setNoAi` 会多写一次。

- **三条自动移除路径全部覆盖，且证据在 21.1.213 源码里（不是推断）**：
  - **自然到期** → `LivingEntity.java:790-792` 先 post `MobEffectEvent.Expired`，再走 `onEffectRemoved`；效果从 `activeEffects` 移除后 `hasEffect` 变假。
  - **`/effect clear`** → `EffectCommands` 调 `removeAllEffects()` → `LivingEntity.java:911-921`（`:920` 过 `EventHooks.onEffectRemoved`）→ `hasEffect` 变假。
  - **牛奶** → `MilkBucketItem.java:27` 调 `removeEffectsCuredBy(EffectCures.MILK)` → `LivingEntity.java:3517-3525`（`:3524` 同一个 hook）→ `hasEffect` 变假。
  - 三条路径的**共同点**：无论走哪条，对账下一次读到的都是「效果不在了」。这就是「按效果是否还在身上判」的实现——**不依赖任何一种移除事件的送达**，所以 21.1.213 里事件语义的细微差别都不影响正确性（对账是权威，**本版一个移除事件都不挂**，少一处依赖）。
- **「观测不到」不等于「实体没了」**：区块卸载后 `level.getEntity(uuid)` 返回 `null`，此时**绝不能**当成死亡去删附件——删了就丢了原值（RT-54 的第二个入口）。本文定为：观测不到就**只丢内存两件**（索引 + 到期刻），附件留着；载入时由 `adopt` 的判据 1/2 结算（裁定 #16：震动豆那一路在载入时一律按已到期处理）。因此**不需要**「观测不到多少刻后放弃」这类上限常量——**内存可以随便丢，附件才是真相**。
- **死亡路径不挂事件**：`LivingDeathEvent`（先例 `QbounceMountController:124-130`）**不必**挂——对账读到 `isRemoved()/isDeadOrDying()` 就走路径④。少挂一个事件就少一处「事件没送达」的失效面。

**mount 态跳过（裁定 #7）与第二层（切换时先恢复）**（§2.3.5）。

#### 2.3.5 mount 互斥：只问一个入口，且不得自己解析 config

`isMountMode()` 现在是 `private`（`QbounceMountController:199-201`），效果类与 AI 表都拿不到。**本文确定（7）**：把 `:199` 的 `private static boolean isMountMode()` 改成 **`public static boolean isMountMode()`**，**不新增第二个方法**——同一件事不许有两份实现（这是 40 号术语表「mount 的真相只有一处」的落地方式，改动量 1 个修饰符）。

- **效果类与 AI 表都不得解析 `qbounce.poseMode`**（`QbounceMountController:50` 的 `POSE_MODE_MOUNT` 是唯一字面量，`:200` 是唯一读取点）。`MobAiSuspension` 只调 `isMountMode()`。
- **两层语义**：
  - 第一层（进入时是 mount）：`suspend()` 时 `if (sourceBit == SRC_Q_BOUNCE && QbounceMountController.isMountMode()) return;` ⇒ 不写附件、不删 AI，实体照常动。
  - 第二层（删了之后切到 mount）：对账里那一行 `if (含 Q_BOUNCE && isMountMode()) release(SRC_Q_BOUNCE)` ⇒ 一旦切到 mount，先把 AI 按原值放回去、删附件，**同一刻再交给 `QbounceMountController` 去挂坐骑**，否则坐骑会带着一个 `noAi` 的实体跑 0 移动。
- **顺序保证（本文确定（11））**：对账挂在 `ServerTickEvent.Post` 且 `@SubscribeEvent(priority = EventPriority.HIGHEST)`；`QbounceMountController.onServerTick`（`:173-196`）不写 priority ⇒ NORMAL。HIGHEST 先跑 ⇒ 第二层在同一 tick 内先恢复、挂载后发生。
- **`MobAiSuspension` 不得改动挂载逻辑**：它只读 `isMountMode()`，不碰 `ACTIVE`、不 discard 坐骑、不 `startRiding`。

#### 2.3.6 震动豆的接入（D-180）

`VibrationBeanTrap#trigger`（`:74-105`）现在只给球内**玩家**施 `CONFUSION`（`:83-91`），对生物 0 伤害 / 0 效果 / 0 位移。本版在 `:91` 之后、`:92` 取 `pos` 之前**追加一段**（既有四步——玩家混乱 / 爆炸音 / `consume` / 授进度——一字不改）：

```
for (Mob mob : level.getEntitiesOfClass(Mob.class, TrapBehavior.searchBox(pos, radius))) {
    if (!mob.isAlive()) continue;
    if (center.distanceTo(mob.position()) >= radius) continue;   // 与触发端同一个 < 口径
    MobAiSuspension.suspend(mob, MobAiSuspension.SRC_VIBRATION_BEAN, level.getGameTime() + AI_SUSPEND_TICKS);
}
```

- **半径不新设第二个数**：沿用 `vibration_bean.triggerRadius`（`:900-902`，默认 4.0），与 `findTrigger`（`:50`）与混乱端（`:79`）同一个键、同一个「严格小于」。
- **`AI_SUSPEND_TICKS = 80` 是常量，不是 config 键**（裁定 #5；D-180 已否决「把 80 刻写成新 config 键」——那会变 177）。
- **「80 刻」是 AI 被删的时长，不是混乱时长**：后者是 `vibration_bean.confusionTicks`（`:903-905`，默认同为 80）。两者**同值不同义**，不得合并。
- **范围是触发那一刻的一次性快照**：陷阱在 `:97` 被 `consume` 掉，之后进入半径的生物不会被补删，提前离开的也不会提前恢复（40 号 §7 ⑥）。
- **`Mob` 已经排除玩家**（`Player` 不是 `Mob`），代码里不写 `!(mob instanceof Player)` 这类冗余分支；同形先例 `WildHunterAura:88` 之所以写了，是因为它的入参是 `LivingEntity`（本方法的集合泛型已经是 `Mob`）。
- **免疫闸不参与**：`TrapImmunity.passGate`（`TrapImmunity.java:42-45`）对非玩家恒放行，所以生物永远走得到 `trigger`；本版不改它。
- ⚠️ **一条既存不一致（41 号报出，本版不修，但验收要避开它）**：`findTrigger` 会筛创造 / 旁观 / 己方（`:51-54`），而 `trigger` 的混乱循环遍历**全部** `level.players()`、**不筛**（`:83-91`）⇒ 创造 / 旁观玩家**不会**触发震动豆，却会被**别人**触发的震动豆晕到。这是 0.2.0 就有的口径，与 4 条需求无关（D-174 挡在门外），本版**一行不改**。处置：登记在 §9 第 13 条；§7 #14 / #15 一律用**生存模式、非己方、非旁观**的玩家与生物测，不要用创造号做「不触发」的反向断言（那一测会红，但红的是既存口径，不是本版回归）。

#### 2.3.7 一处必须写明的交叉后果（震动豆 × mount）

若某生物**正被 mount 分支挂着坐骑**，又恰好被震动豆命中：按裁定 #4（范围内的**所有**非玩家生物）本版**不豁免**——它会被删 AI 80 刻，于是「有坐骑 + 不动」持续 80 刻，之后按原值恢复、坐骑照旧（`QbounceMountController.validate:307-316` 的三条校验不看 `noAi`）。裁定 #7 的 mount 豁免只属于 Q弹 那条链路，不能横向扩用到震动豆（那是 RT-57 式的「顺手改口径」）。这一条登记为 RT-55 的已知后果，验收 §7 #14 / #15（震动豆）与 #18（mount）分开测、不合并。

### 2.4 敌对谓词（`Enemy` ∪ `MobCategory.MONSTER` 的实现位置与命名）

**本文确定（8）**：新增 `util/Hostility.java`（无状态纯函数，与 `util/DeployedOwnership`、`util/DistanceMetrics` 同类）：

```
public static boolean isHostile(@Nullable Entity entity) {
    if (!(entity instanceof LivingEntity)) return false;
    if (entity instanceof Player) return false;                     // 保险：玩家既不是 Enemy，类别也不是 MONSTER
    return entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER;
}
```

四条事实（都在 21.1.213 源码里核过）：

1. `Enemy` 是**纯标记接口**：`net/minecraft/world/entity/monster/Enemy.java` 共 11 行，只有 6 个 `XP_REWARD_*` 常量、**没有任何方法** ⇒ `instanceof Enemy` 就是全部判定。
2. `Monster extends PathfinderMob implements Enemy`（`Monster.java:29`）⇒ 原版 `Monster` **已经**在 `Enemy` 里，并集的增量只来自两类：类别是 `MONSTER` 但没实现 `Enemy` 的模组生物、实现了 `Enemy` 但不在 `monster` 包里的生物。
3. 类别取值走 `EntityType#getCategory()`（`EntityType.java:1043`），不是反射、不是 `instanceof Monster`。
4. 玩家恒不在内：`Player` 既不是 `Enemy`，类别也不是 `MONSTER`；显式那一行是给读者看的，不是防漏。

**使用点只有 2 处**（D-182）：`explosion/FarmstormExplosion#hurtCapped`（`:159` 的遍历里）与 `behavior/impl/ZangiefCaneTrap#monsterBranch`（`:111-118`）。**不回改**既有 5 处 `getEntitiesOfClass(Monster.class, …)`（`BlastCarrotTrap:59`、`FangTrapCactusTrap:54`、`VibrationBeanTrap:59`、`ZangiefCaneTrap:174`、`PumpkinTurret:109`）——那改的是陷阱的**触发面**，不是伤害面（RT-57）。

### 2.5 陷阱倍率（`trap.damageMultiplier` 的类型与范围 / `FarmstormExplosion` 的来源标记 / 两处乘点）

#### 2.5.1 键与字段

**本文确定（10）**：在 `FarmstormConfig` 的 `trap` 段（`:917` push ~ `:927` pop）、`trapMaxTrapsPerPlayer`（`:924-926`）之后、`b.pop()`（`:927`）之前插入：

```
trapDamageMultiplier = b.comment("陷阱对敌对生物的伤害倍率（1.0 = 不改；只作用于 blast_carrot 与 zangief_cane）；范围 1.0 ~ 10.0")
        .translation(tr("trap.damageMultiplier"))
        .defineInRange("damageMultiplier", 3.0, 1.0, 10.0);
```

- 类型 **double**（D-183）；默认 **3.0**（裁定 #10 / D-183）。
- **值域 `1.0 ~ 10.0`（本文确定）**：下界取 1.0 = 「不改伤害」，而不是 0.0——0.0 会造出第三个语义（伤害归零），既不是「×3」也不是「关掉倍率」，且 `blast_carrot` 的 `min(raw, cap)` 公式在结果为 0 时会让「命中但不掉血」变成静默异常态。上界 10.0 = 3.0 的三倍余量，且 `cap 7 × 10 = 70` 仍在 float 精确整数区（≤ 2^24），不会出现 69.99999 这种读数。
- 字段声明写在 `:223-225` 那三个 `trap*` 字段之后（`public static ModConfigSpec.DoubleValue trapDamageMultiplier;`）。`tr()` 的前缀是 `farmstorm.configuration.`（`:1000-1002`）⇒ 语言键 = **`farmstorm.configuration.trap.damageMultiplier`**。

#### 2.5.2 `FarmstormExplosion` 的来源标记（最小限度）

`FarmstormExplosion` 有 **7 处** `new`（本文自己 grep 全仓核过）：

| # | 调用点 | 性质 | 是否 ×3 |
|---|---|---|---|
| 1 | `behavior/impl/BlastCarrotTrap.java:84` | **陷阱**（`blast_carrot`） | **是** |
| 2 | `entity/thrown/MelonGrenadeProjectile.java:42` | 投掷物（瓜！） | 否 |
| 3 | `entity/thrown/MelonSliceProjectile.java:40` | 投掷物（瓜片子） | 否 |
| 4 | `entity/thrown/MolotovProjectile.java:94` | 投掷物（莫洛托夫） | 否 |
| 5 | `entity/thrown/ToxicUbiServerProjectile.java:43` | 投掷物（毒育碧服务器） | 否 |
| 6 | `entity/thrown/UbiServerProjectile.java:41` | 投掷物（育碧服务器） | 否 |
| 7 | `util/LuckyBerryRoller.java:212` | 幸运浆果小爆炸 | 否 |

**七处用的是同一个伤害源** `farmstorm:crop_blast`（`FarmstormDamageTypes:23`，构造法 `:36-38` 的 `cropBlast`）⇒ 在 `:179` 无脑乘会把 2~7 一起 ×3。

**本文确定（9）**：加第 9 个构造参数 `boolean trapSourced`，**并保留原 8 参签名作为重载、委托 `false`**：

```
public FarmstormExplosion(ServerLevel level, @Nullable Entity sourceEntity, DamageSource damageSource,
                          double x, double y, double z, float radius, BlastRule rule, float damageCap) {
    this(level, sourceEntity, damageSource, x, y, z, radius, rule, damageCap, false);   // 6 处非陷阱调用零改动
}

public FarmstormExplosion(…, float damageCap, boolean trapSourced) {
    super(…); this.trapSourced = trapSourced;   // 其余四行照旧
}
```

只有 `BlastCarrotTrap:84-86` 改成传 `true`（`damageCap` 之后加一个参数）。**为什么不用另外两条路**：

- **不改伤害源**：新增一个 `farmstorm:trap_blast` 会同时动 `damage_type` 注册面（§1.2 的 `:10` 行 3 → 4，97 → 98）与 `#minecraft:is_explosion` 标签语义（爆炸保护 / 抗性附魔 / 死亡讯息全跟着变）。D-186 已否决「用 Mixin 改尖牙」，同理也不能为了一个倍率新开伤害类型。
- **不按 `BlastRule` 反查作物**：`BlastRule.forCrop(profile, whenDemolition)`（`BlastRule.java:78-80`）**只返回规则、把档案丢掉**——返回值里没有作物身份，`BlastRule` 的字段只有 `extraBreakable` / `maxResistance` / `dropsBlocks`。反查需要一个「规则 → 作物」的映射，而不同作物可能产出同一条规则（`NEVER_BREAK` 会被所有非破障类作物共享），反查会误判。且 6 处非陷阱调用里也有走 `forCrop` 的（如 `MelonGrenadeProjectile` 的 `BlastRule.grenade()` 族），按规则判等于按「破坏能力」判「是不是陷阱」——两件事无关。
- **不用 `damageSource.getEntity()` 判**：那是「谁放的」，不是「哪条链路」；玩家放的瓜！与玩家部署的胡罗贝在这一点上无法区分。

#### 2.5.3 两个乘点

**乘点 ①：`FarmstormExplosion#hurtCapped`（`:179`）**

```
double raw = (impact * impact + impact) / 2.0 * 7.0 * reach + 1.0;         // 原 :178，不动
float dealt = (float) Math.min(raw, this.damageCap);                       // 原 :179 的封顶
if (this.trapSourced && Hostility.isHostile(entity)) {
    dealt *= (float) FarmstormConfig.trapDamageMultiplier.get();
}
entity.hurt(this.damageSource, dealt);                                     // 原 :179
```

- **乘的是封顶后的最终伤害**，不是 `damageCap`、不是 `radius`：`blast_carrot` 的公式是 `min(raw, cap)`，`raw` 多数命中都低于 7 ⇒ 「乘 cap」在 `raw < cap` 时完全无效；「乘 `explosionPower`」改的是范围与公式里的 `reach`（等于改陷阱的物理面）。
- **敌对闸**：`entity` 是 `LivingEntity`（`:159` 的遍历类型）。玩家与非敌对生物不乘。
- **不炸自己的旧口径不变**：`:144-147` 的注释明确「不分敌我、不宽恕使用者」，本版不碰。

**乘点 ②：`ZangiefCaneTrap#monsterBranch`（`:111-118`）**

```
double amount = FarmstormConfig.zangiefCaneSlamDamage.get();               // 原 :111，默认 7.5
if (Hostility.isHostile(target)) {
    amount *= FarmstormConfig.trapDamageMultiplier.get();
}
DamageSource source = FarmstormDamageTypes.zangiefSlam(level, deployer);   // 原 :112
…
} else if (amount > 0.0) {
    target.hurt(source, (float) amount);                                   // 原 :118
}
```

- 把乘法放在 `source == null` / `amount > 0` 两个判定**之前**，`slamDamage` 配 0 时乘完仍是 0，行为不变。
- **现有 INFO 日志（`:126-128`）打印的是 `amount`** ⇒ 乘完再打，验收读数（§7 #19）才能与 `hurt` 的实际值对上。日志格式串不改，只是值变成乘后值。
- **玩家分支不动**：`ZangiefPiledriverController:303`/`:311` 的伤害不在倍率面内（D-184）。

**生效面（4 个 `TRAP` 作物里 2 个参与，RT-58）**：`blast_carrot` ✅ / `zangief_cane` ✅ / `fang_trap_cactus` ❌（伤害在原版 `EvokerFangs` 里，`FangTrapCactusTrap:87-109`，仓内 0 处 `hurt`）/ `vibration_bean` ❌（0 伤害）。任何写「所有陷阱 ×3」的句子都是错的。

---

## 3. 动作面总表（对 0.2.0 的增 / 改 / 删，逐行 31 条）

列说明：**类别** = 发布 / 类 / 注释 / 数据 / 配置 / 语言 / 工具 / 验收 / 不改。**现值** = 0.2.0 已发布态的实际内容；**改后** = v0.2.1 的目标态。决策号一律引 40 号，风险号一律引 `BRIEF` §4。

| # | 类别 | 路径或 类#方法 | 现值 | 改后 | 决策号 | 风险号 |
|---|---|---|---|---|---|---|
| 1 | 发布 | `gradle.properties:11` | `mod_version=0.2.0` | **`0.2.1`** | D-173 | — |
| 2 | 发布 | `一键构建.cmd:5` | 抬头 `Farmstorm v0.2.0` | **`v0.2.1`** | D-173 | — |
| 3 | 发布 | `一键构建.cmd:11,12` | jar / sources-jar 名各含 `0.2.0` | **`0.2.1`**（两处） | D-173 | — |
| 4 | 发布 | `检查环境.cmd:5` | 抬头 `Farmstorm v0.2.0` | **`v0.2.1`** | D-173 | — |
| 5 | 发布 | `检查环境.cmd:14` | `farmstorm-0.2.0+mc1.21.1.jar` | **`0.2.1`** | D-173 | — |
| 6 | 类 | `blockentity/OutpostCoreBlockEntity#serverTick`（`:507-525`） | 三件事（早退 → 粒子门 → 粒子 + 每 30 轮日志） | **四件事**：`now` 上移一行 → 补绑门（独立 `if`，不 return）→ 粒子门 → 粒子 + 日志 | D-176 | RT-51、RT-53 |
| 7 | 类 | `OutpostCoreBlockEntity` 常量区（紧邻 `:91`） | 无 | **新增** `REBIND_INTERVAL_TICKS = 100` | D-176 | RT-52 |
| 8 | 类 | `OutpostCoreBlockEntity#rebindDue(BlockPos, long)` | 不存在 | **新增**（独立相位公式，模数 100） | D-176 | RT-51 |
| 9 | 类 | `OutpostCoreBlockEntity#rebindNearby(ServerLevel)` | 不存在 | **新增**（两阶段：收集 → 写绑定；`cap` 处 `break`） | D-177 | RT-53 |
| 10 | 类 | `util/AreaBlockEntities.java` | 不存在 | **新增**（`collect(ServerLevel, BlockPos, int, Class<T>)`，只走 `getChunkNow`） | D-177 | RT-52 |
| 11 | 注释 | `block/CultivatorBlock.java:81-83` | 「**不自动认领**……事后在旁边补一个核也**不会绑上**」 | **必须改**：「运行期每 100 刻补绑未绑定的（D-176）」 | D-176 | RT-51 |
| 12 | 类 | `client/OutpostCoreScreen.java`（42 行） | 只覆写 `renderBg`（`:37-41`） | **一行不改**（判据与退路见 §2.2） | D-178 | RT-59 |
| 13 | 验收 | 本文 §7 | tooltip / 悬停 **0 条** | **+3 条**（#11 ~ #13） | D-178 | RT-59 |
| 14 | 类 | `behavior/impl/VibrationBeanTrap#trigger`（`:74-105`） | 只给球内玩家 `CONFUSION`（`:83-91`），对生物 0 作用 | **追加**：球内所有非玩家生物删 AI **80 刻**（记原值） | D-180 | RT-54、RT-56 |
| 15 | 类 | `VibrationBeanTrap` 常量区 | 无 | **新增** `AI_SUSPEND_TICKS = 80`（不新增 config 键） | D-180 | RT-56 |
| 16 | 类 | `effect/QbounceEffect`（20 行、0 覆写） | 纯状态载体，`applyEffectTick` 走空实现 | **覆写 `onEffectAdded(LivingEntity, int)`** 一个方法，转发给 AI 表（**不**覆写 `applyEffectTick`） | D-181 | RT-55、RT-56 |
| 17 | 类 | `behavior/QbounceMountController#isMountMode`（`:199`） | `private static` | **`public static`**（同一实现，不新增第二个方法） | D-181 | RT-55 |
| 18 | 类 | `combat/MobAiSuspension.java` | 不存在 | **新增**（`sources` 位掩码 + 观测索引 + 内存到期刻 + 五条清理路径 + 每刻对账） | D-179 | RT-54 |
| 19 | 类 | `registry/FarmstormAttachments.java` | 不存在 | **新增**（`AttachmentType<Suspend>`，`farmstorm:ai_suspend`，注在 `ATTACHMENT_TYPES`） | D-179 | RT-54 |
| 20 | 类 | `util/Hostility.java` | 不存在 | **新增**（`isHostile(Entity)`，`Enemy` ∪ `MONSTER`） | D-182 | RT-57 |
| 21 | 类 | `explosion/FarmstormExplosion` 构造器（`:69-78`） | 8 参 | **9 参**（+`boolean trapSourced`），**保留 8 参重载委托 `false`** | D-184 | RT-57、RT-58 |
| 22 | 类 | `FarmstormExplosion#hurtCapped`（`:178-179`） | `entity.hurt(src, min(raw, cap))` | 敌对 ∧ `trapSourced` 时 × 倍率 | D-184 | RT-58 |
| 23 | 类 | `ZangiefCaneTrap#monsterBranch`（`:111-118`、日志 `:126-128`） | `hurt(src, amount)`，日志打原值 | 敌对时 × 倍率，**日志打乘后值** | D-184 | RT-58 |
| 24 | 配置 | `config/FarmstormConfig.java`（`trap` 段 `:917-927`） | 3 键 / 60 组 / 175 键 | **4 键**（+`damageMultiplier` 3.0、1.0~10.0）/ 60 组 / **176 键** | D-183、D-185 | RT-60、RT-60b |
| 25 | 语言 | `datagen/ModLangProvider.java`（`:412-413` 之后） | 178 条 `config(entries, …)` / 337 键 | **+1 条**（`trap.damageMultiplier`）/ **338 键 × 2** | D-185 | RT-60 |
| 26 | 工具 | `tools/verify_project.py`（`main()` `:349-402`） | 期望键集 = 6 份文档的固定小节；无 42 号解析 | **新增** `expected_config_keys_v021`（读本文 §4.1.1，1 行）+ 期望 176 | D-185 | RT-60、RT-60b |
| 27 | 不改 | `Farmstorm.java:167-172`（12 类断言 97）/ `network/FarmstormNetwork.java`（2 包、协议 `"2"`） | 97 / 2 包 | **原样**（本版 +0） | D-175、D-186 | — |
| 28 | 不改 | `FangTrapCactusTrap:87-109`（原版 `EvokerFangs`）/ `vibration_bean`（0 伤害）/ 5 处 `Monster` 过滤 | 原样 | **原样**（尖牙与震动豆不参与 ×3；触发面不动） | D-184、D-182 | RT-58、RT-57 |
| 29 | 类 | `Farmstorm.java` 构造器（12 个 register 的 `.register(modBus)` 区） | 12 个 register | **+1 行**（`FarmstormAttachments.REGISTER.register(modBus)`）；`:167-172` 的 12 类断言**一行不动** | D-179、D-175 | RT-54、RT-60 |
| 30 | 类 | `OutpostCoreBlockEntity#rebindNearby` 的**前置闸与排序** | 不存在 | **+无主核闸**（`ownerUuid == null` 直接返回，裁定 #13）+ **候选排序**（`distSqr` → `x` → `y` → `z`，裁定 #14） | D-177 | RT-51、RT-53 |
| 31 | 数据 | 震动豆 80 刻的到期刻 | 无 | **只存内存**（`VB_DEADLINE`），**不落盘**（裁定 #16：窗口不跨档） | D-180 | RT-54 |

**31 行的分类计数**：发布 **5**（#1~5）+ 类 **18**（#6~10, 12, 14~23, 29, 30）+ 注释 **1**（#11）+ 验收 **1**（#13）+ 数据 **1**（#31）+ 配置 **1**（#24）+ 语言 **1**（#25）+ 工具 **1**（#26）+ 不改 **2**（#27, #28）= **31**。

**`BRIEF` §3.4 的 12 项必改逐项落点**：① `gradle.properties` → #1；② 两支 `.cmd` 的 jar 名 + 抬头行 → #2~#5（**4 行**，抬头行 `BRIEF` 原文已列，见 §9 第 1 条）；③ `serverTick` 加补绑 → #6~#9；④ 区域枚举工具 → #10；⑤ tooltip → #12、#13；⑥ 震动豆 → #14、#15；⑦ `QbounceEffect` → #16（+ #18 承载恢复判据）；⑧ 两处乘点 + 来源标记 → #21~#23；⑨ 敌对工具 → #20；⑩ `verify_project.py` → #26；⑪ `CultivatorBlock` 类注释 → #11；⑫ `isMountMode` 可见性 → #17。**12 项全部有着落，无一项落在「不改」行**。

**`BRIEF` §3.4 未列、但本版必须做的 4 项**：#18、#19、#29（AI 原值的载体、它的注册类、以及主类里那一行注册）与 #31（到期刻不落盘）。它们不在 12 项里，因为都不落在某个既有文件的某一行上——#19 / #29 是裁定 #15 / #17 的直接产物，#31 是裁定 #16 的直接产物。

---

## 4. config 全表（只列变化的组 + 总数核对）

### 4.1 变化逐键

#### 4.1.1 增 / 改 / 删（**1 行**：增 1 / 删 0 / 改 0）

| # | 完整键路径 | 类型 | 默认 | 值域 | 动作 | 决策号 |
|---|---|---|---|---|---|---|
| 1 | `trap.damageMultiplier` | double | **3.0** | **1.0–10.0** | **增** | D-183 |

（`trap` 组 **3 → 4** 键；组数不变 ⇒ **175 + 1 = 176 键 / 60 组**。本版**没有**删键、**没有**改既有默认值。）

**解析契约（本文确定（12），写给 `tools/verify_project.py` 的下一个维护者）**：本文 §4.1.1 的表与 `36-TECH-0.2.0.md` §4.1.1 **同格式**（7 列：序号 / 反引号内的 `组.键` / 类型 / 默认 / 值域 / 动作 / 决策号）。新增的 `expected_config_keys_v021` 必须：

1. 用 `^#### 4\.1\.1\b` 与随后的 `^#### 4\.1\.2\b` **界定范围**（不能全文扫 `^\|\s*\d+\s*\|`——本文 §3 与 §7 的表也以 `| 1 |` 开头）；
2. 逐行要求恰好 **7** 列，第 2 列匹配 `` ^`([A-Za-z0-9_]+\.[A-Za-z0-9_.]+)`$ ``，第 6 列以「增」/「删」开头；
3. 行数断言 **== 1**（增 1 / 删 0），与 `expected_config_keys_v012` 的 11 行断言同形（`verify_project.py:172-178`）；
4. 把结果并进 `main()` 的 `union`（`:382-391`），并把期望数从 175 改 176。

**⚠️ 只写文档不改解析器 = 假绿照旧**：现行 `verify_project.py` 只读 6 份固定文档的固定小节（`main():349-355` 的 `design` / `tech` / `tech3` / `tech4` / `tech5` / `tech6`），**它今天不会读本文**。所以「把 `trap.damageMultiplier` 写进文档」是**必要但不充分**条件——两者必须同一个提交（§7 #22 就是这条的验收）。

#### 4.1.2 值域与字段名的理由

| 项 | 取值 | 理由 |
|---|---|---|
| 字段名 | `trapDamageMultiplier` | 与同段三个字段 `trapImmunityTicks` / `trapDustIntervalTicks` / `trapMaxTrapsPerPlayer`（`:223-225`）同一命名法：`组名 + 键名首字母大写` |
| 类型 | `ModConfigSpec.DoubleValue` | D-183 定 double；同段三个都是 `IntValue`，只有这一个走 `defineInRange(String, double, double, double)` 重载 |
| 下界 | **1.0** | 1.0 = 「不改伤害」。取 0.0 会出现第三态（命中但不掉血），且 `blast_carrot` 的 `min(raw, cap)` 在 0 倍时表现为静默异常 |
| 上界 | **10.0** | 3.0 的三倍余量；`cap 7 × 10 = 70` 在 float 精确整数区内（≤ 2^24），验收读数不会出现 69.99999 |
| 默认 | **3.0** | 裁定 #10 / D-183，本文不改 |
| 翻译键 | `farmstorm.configuration.trap.damageMultiplier` | `tr()` 的前缀是 `farmstorm.configuration.`（`FarmstormConfig:1000-1002`） |
| 落盘键 | `[trap]` 段的 `damageMultiplier` | 与 `trap` 段既有三键同段；`b.push("trap")` (`:917`) / `b.pop()` (`:927`) 不动 |

**不改的三件事**：`blast_carrot.maxDamage`（7）、`zangief_cane.slamDamage`（7.5）、`trap` 段既有 3 键的默认值——倍率是**新键**、在结算处乘（裁定 #10）。

### 4.2 总数核对（176 / 60）

| 项 | 0.2.0 | 本版 | v0.2.1 | 算法 |
|---|---|---|---|---|
| server config 键 | **175** | **+1** | **176** | 行首 `.define*` 计数：`175 + 1 = 176`（§1.3 的方法，逐键复核） |
| server config 组 | **60** | **+0** | **60** | `b.push` 次数 60；`trap` 是既有组，只加键不建组 |
| client config 键 | 3 | +0 | **3** | `hud.enabled/x/y`，不计入 176 |
| `.define` 行总数（含注释） | 177 | +1 | **178** | 175 live + 1 注释（`:935`）+ 1 提及（`:229`），改后 176 + 2 |

**三条独立校验**：① 源码行首 `.define*` = 176；② `run/config/farmstorm-server.toml` 重生成后取键 = 176（⚠️ 现盘那份是旧版残留，见 §9 第 2 条，验前必须先跑一次 `runServer` 让它重写）；③ `verify_project.py` 的并集 = 176（需先按 §4.1.1 的契约加解析器）。

**组数为什么仍是 60**：`trap` 组早就存在（`:917` 的 `b.push("trap")`），本版只在组内加一个键 ⇒ 「顶层组 / 嵌套组」的划分一点不动，落盘 toml 的 `[section]` 行数也不变。

### 4.3 语言键增删（+1 → 338）

**现状 337 × 2**（方法见 §1.3）。本版 **+1** ⇒ **338 × 2**。

| # | 键 | 语言 | 位置 | 文案（本文确定） |
|---|---|---|---|---|
| 1 | `farmstorm.configuration.trap.damageMultiplier` | zh_cn | `ModLangProvider` 的 `:412-413` 之后 | 「陷阱对敌对生物的伤害倍率（1.0 = 不改；只作用于 blast_carrot 与 zangief_cane）」 |
| 2 | 同上 | en_us | 同一行的第四参数 | `"Damage multiplier of traps against hostile mobs (1.0 = unchanged; blast_carrot and zangief_cane only)"` |

**怎么数（可复现）**：

```
ConvertFrom-Json 两侧键集：zh = 338、en = 338、zh-only = 0、en-only = 0
farmstorm.configuration.* ：179（= 176 server + 3 client）
其它前缀不变（tooltip.farmstorm 仍 20、advancements 36、item 32、block 26 …… 相加 = 338）
ModLangProvider 的 config(entries, …) 次数：178 → 179
```

**两条纪律**：① 中文与英文**同键同数**（0.2.0 的基线是 337 × 2、zh-only 0 / en-only 0），只加中文会让 338 × 2 变成 338 / 337；② 语言键**不是自动生成**的——`ModLangProvider` 是逐条手写 `config(entries, "组.键", 中文, 英文)`（现在 178 条），所以「加 config 键」不会自动长出翻译，必须手写这 1 行。

---

## 5. 存档与兼容

五问逐条回答（每条给「落不落盘」与「新进程看到什么」）。

### 5.1 AI 原值是否落盘

**落，落在实体自己的附件里（裁定 #15），恢复即删。** 四层事实：

1. 落盘位置：`AttachmentType<Suspend>`（`farmstorm:ai_suspend`），序列化进实体的 `neoforge:attachments`（`AttachmentHolder.java:27` 的键名、`:122-145` 写出、`:151-172` 读回）；`Entity extends AttachmentHolder`（`Entity.java:131`）。**不新增 `SavedData`**、不写任何 `.dat`、不进 `getUpdateTag`（客户端看不到）。
2. 生命周期：只活「删 AI 的那一段」。`release()` 的唯一出口末尾是 `removeData(AI_SUSPEND)`（`AttachmentHolder.java:107`）⇒ 正常路径下**存档里一个字节都不留**；且 `sources == 0` 的 `NONE` 本来就不写（`serialize(codec, s -> s.sources() != 0)`）。
3. 停服：`ServerStoppingEvent` 做一次**尽力归还**（恢复 + 删附件），让存档干净；但它**不是正确性来源**——凡因区块已卸载而漏掉的，下次载入由附件接住（§2.3.3 路径⑤）。
4. 只在异常停机时残留：此时下次起服 `EntityJoinLevelEvent` 读到 `sources != 0`、按「两个来源都结束了」的判据恢复（§2.3.3 的四条判据）。这就是 `Map<UUID, Boolean>` 解决不了的那一格，也是本版**唯一**新增的持久化面。

**为什么不用 `SavedData`**：`SavedData` 是「按维度 / 全局」的存档对象，而这里的键是实体 UUID、且必须随实体**一起被复制 / 一起被删**（实体被 `/kill` 或区块永久卸载时，SavedData 里的条目会变成永不清理的垃圾）。现成三个 `SavedData`（`FarmlandLedger` / `OutpostCoreIndex` / `TrapFifoIndex`）都是「坐标索引」这类**跨实体、需要主动过期**的数据，与本用途不同构。

### 5.2 Q弹 / 震动豆的临时状态在停服时的处置

| 状态 | 载体 | 停服处置 | 新进程看到什么 |
|---|---|---|---|
| Q弹 效果本体 | 实体 NBT（原版 `active_effects`） | **不动**（它是原版状态，本版不加任何标记） | 效果还在 ⇒ 该实体的附件里若有 `SRC_Q_BOUNCE`，载入时按「效果还在 ⇒ 继续挂起」接住（§2.3.3 判据 2）；**若附件不存在**（例如它重启前就带着效果、本版从没接手过），则**不删 AI**——本版只接管「自己挂起过」的实体 |
| 震动豆的 80 刻到期 | **内存**（`VB_DEADLINE`）——裁定 #16：不落盘 | 尽力结算（能写就按原值恢复 + 删来源位）；写不到就留附件 | 载入时 `adopt` 判据 1：一律按已到期处理 ⇒ 按原值恢复（**提前恢复无害**） |
| mount 记账表 `ACTIVE` | 内存（`QbounceMountController:73`） | `releaseAll`（`:157` / `:164`）discard 坐骑 + 清表 | 空表 + 存档里没有坐骑残留 |
| AI 的观测索引 `OBSERVED` | 内存（`MobAiSuspension`） | 清空（原值在附件里，索引可以随便丢） | 空索引 + 有附件的实体在载入时自动重新入索引 |

**一处必须写明的取舍**：**只有在附件里留过 `SRC_Q_BOUNCE` 的实体，才会在重启后继续被本版挂起**。一个「重启前就带着 Q弹、但本版从没接手过」的实体（例如 0.2.0 的世界里正飘着的场）不会被补删 AI——「剩余时长 → 是否该定身」不是一条可判据，而强行按效果反推会把「进入前本来就 `noAi`」的实体错误放出来（裁定 #8 要避免的正是这一类）。这条边界登记在 RT-55。

### 5.3 不新增 `SavedData`（逐条点名现有三个，全部 0 改动）

| # | 类 | 落盘文件 | 本版 |
|---|---|---|---|
| 1 | `farmland/FarmlandLedger` | `farmstorm_farmland.dat` | 0 改动（运行期 0 调用者，0.2.0 已如此） |
| 2 | `outpost/OutpostCoreIndex` | `farmstorm_outpost_core.dat` | **0 改动**——补绑不写索引：`setOwner` 是唯一的 `record` 调用点（`:410-419`），补绑不碰「玩家 → 核坐标」映射 |
| 3 | `trap/TrapFifoIndex` | 陷阱 FIFO 索引 | 0 改动 |

**核心 BE 的 NBT**：`Cultivators` 键（`:72` / `:757-761` / `:774-780`）**格式不变**（`ListTag<LongTag>`，每条 `BlockPos.asLong()`）。补绑只是往同一个列表里 `add` 条目，**没有新键、没有改类型、没有改版本号** ⇒ 老档直接读得进，新档老版本也读得进（只是看不到多出来的绑定，会走 `pruneCultivators` 的惰性清理，`:380-391`）。**不新增 `DataVersion` 之类的迁移标记**。

### 5.4 老档兼容

| 场景 | 本版行为 |
|---|---|
| 老档里 `NoAI:1b` 的实体（别的模组 / 命令锁的） | **不碰**。它没有 `farmstorm:ai_suspend` 附件 ⇒ 不进索引、不进 `adopt`、不会被「恢复成有 AI」 |
| 老档里的核（`Cultivators` 为空或 ≤ 16 条） | 下一轮（≤ 100 刻）开始补绑，不需要任何迁移动作 |
| 老档里已有 17+ 条 `Cultivators`（理论上不可能，0.2.0 的 `addCultivator` 就拦了） | 补绑 `if (cultivatorCount() >= cap) return;` 直接跳过，**不修剪**（只加不摘） |
| 老档里指向死核的 `Core` 缓存（迁移出口②遗留） | 补绑跳过（`corePos() != null`），与 0.2.0 的 `isUsable()` 判据一致 |

### 5.5 存档面上「多出来的一点点」

唯一增量：每只**正被挂起**的实体在 `neoforge:attachments` 里多一个 ≤ 40 字节的 compound（`sources == 0` 时不写），恢复即删。**不新增文件、不新增 `.dat`、不新增 `DataVersion`、不动区块格式、不新增 `SavedData`。**

---

## 6. 风险册 RT-51 ~ RT-60（逐条：风险 / 触发条件 / 影响 / 缓释 / 由哪个验收覆盖）

| 编号 | 风险 | 触发条件 | 影响 | 缓释 | 由哪个验收覆盖 |
|---|---|---|---|---|---|
| **RT-51** | 周期补绑与「放置时绑定」并存，同一培养器可能被两个核同时认领 | ① 补绑扫描与放置写入同 tick；② 有人把补绑写成「先写培养器侧再写核侧」；③ 有人为了「修不一致」而调 `removeCultivator` | 一个培养器进两个核的列表 ⇒ 违反 0.2.0 D-165 的「一对一」不变量；迁移 / 容量 / 连线三处同时失真 | (a) 写入顺序写死「先核后培」（`addCultivator` 成功才 `setCore`）；(b) 候选判据 `corePos() != null ⇒ 跳过`，后到者读缓存即见先到者；(c) 补绑**不启用** `removeCultivator`（§2.1.3 第 3 条），半写态一律跳过而不是「先绑再摘」；(d) `CultivatorBlock:81-83` 的类注释同改（#11），不留「注释说不行、代码照做」 | §7 #6 ~ #10 |
| **RT-52** | 区域枚举无先例：`LevelChunk#getBlockEntities()` 的返回语义、未加载区块必须跳过 | ① 用 `Level#getChunk` / `getBlockEntity` 代替 `getChunkNow` ⇒ 触发区块加载；② 直接遍历 `chunk.getBlockEntities()` 的活映射 ⇒ `ConcurrentModificationException`；③ 把「未加载」当成「没有培养器」去记日志 / 记状态；④ 反过来以为「常态扫不到几块」（事实是 3×3 常态全已加载，见 §2.1.2 的 📌） | ① 一个核每 100 刻把 9 个区块拉进内存；② 服务端 tick 抛异常；③ 日志噪声 + 误判；④ 验收按「低频」写 ⇒ 把「100 刻内补上」误判成偶发 | (a) 只走 `ServerChunkCache#getChunkNow`，取不到即跳过（`OutpostCoreBlockEntity:296-300` / `:384-387` 的既有惯例）；(b) 返回**新 `ArrayList`**（两阶段，`QbounceMountController:213` 的写法）；(c)「跳过」不写任何状态，也不打日志；(d) 常态全加载的依据写进 §2.1.2：核的自有票 `distance = 1`（`CoreChunkTickets.java:17-18`、`:43-44`）⇒ 中心区块 entity-ticking、邻圈 8 块 FULL | §7 #6、#9 |
| **RT-53** | 一核 16 个上限：补绑到满之后必须停；`pruneCultivators` 与补绑同时跑可能「刚绑上又被摘」 | ① 循环不 `break`；② 补绑里顺手调 `cultivators()`（内含 `pruneCultivators`）；③ 容量用 `cultivatorCount()` 之外的读数 | ① 满员后仍对几十个候选逐个白跑；② `pruneCultivators` 的 `removeIf` 改列表并 `setChanged`，与补绑交错会让「本次已绑的坐标」重复或漏计；③ 容量与 `isCultivatorListFull` 判据不一致 | (a) `if (cultivatorCount() >= cap) break;` 每绑一个重读；(b) 只读 `cultivatorCount()` / `isCultivatorListFull()`（`:334` / `:343`），**不调** `cultivators()`；(c) 不得在补绑路径上调用 `pruneCultivators` / `removeCultivator` | §7 #10 |
| **RT-54** | AI 原值**落盘后的残留风险**：五条清理路径漏一条，存档里就留下一份「过期挂起记录」，下次载入会去动一个本不该动的实体（裁定 #15 的新形态；旧形态「停服丢表 ⇒ 永久没 AI」已被附件消掉） | ① 恢复后忘了 `removeData`；② 把「观测不到（区块卸载）」当成「实体没了」而删附件；③ 先 `setNoAi` 后写附件（崩在中间）；④ 五条路径里漏了死亡 / 卸载任何一条；⑤ 「一律恢复成有 AI」 | ① 附件永不消失 ⇒ 每次载入都白跑一遍、且 `originalNoAi` 越来越不可信；② 原值丢失 + 实体带着 `NoAI:1b` 落盘 ⇒ 永久没 AI（旧 RT-54 的后果又回来了）；③ 同上；④ 死亡实体的附件被复制到别处（已由 `copyOnDeath` 默认 false 挡住，`AttachmentType.java:43`）；⑤ 把「本来就没 AI」的实体放出来 | (a) `release()` 是唯一写出口，末尾必 `removeData`（§2.3.2 的两段代码）；(b) 写入顺序写死「读原值 → 写附件 → `setNoAi(true)`」；(c) 「观测不到」**只删索引、保留附件**，1200 刻上限（§2.3.4）；(d) 附件能同时表达两个来源，恢复判据 = 「两个来源都结束」；(e) 时间倒流守卫；(f) 载入接住（路径⑤）让「停服那一轮漏了」不再是永久损坏 | §7 #23、#24、#28 |
| **RT-55** | Q弹 效果存续期：效果被 `/effect clear`、被牛奶清除、或效果到期，**以及死亡与区块卸载**，五条路径都必须清理 | ① 用一次性计时器；② 只在 `applyEffectTick` 里判；③ 挂的事件漏了一条路径；④ 区块卸载时误判成死亡；⑤ 震动豆定身与 mount 坐骑同刻命中 | ① 每刻重扫让计时器永远重置 ⇒ 永不恢复；或反过来在效果还在时提前恢复；② `QbounceEffect` 现 0 覆写、`applyEffectTick` 走空实现（`:8`）⇒ 压根不会被调；③ 某条路径下附件残留；④ 原值丢失；⑤ 「有坐骑 + 不动」持续 80 刻（§2.3.7，本版**按裁定 #4 不豁免**，登记为已知后果） | (a) 恢复判据 = `hasEffect` 实时读数（对账每刻读，移除事件一条不挂）；(b) 三条自动移除路径的共同收敛点已核：到期 `LivingEntity.java:790`、`/effect clear` `:920`、牛奶 `:3524`（`MilkBucketItem.java:27` → `removeEffectsCuredBy`）；(c) 死亡与卸载分别走路径④ / ⑤，卸载**不删附件**；(d) 对账是权威，事件面**零依赖** | §7 #17、#18、#28 |
| **RT-56** | Q弹 的效果是「反复施加、时长递减」，删 AI 的持续期不能配一次性计时器 | `FriendChivesProjectile` 落地读场长（`:82`）、每刻重扫写「场长 − 已过刻数」（`:155-172`） | 同上（RT-55 的 ①） | (a) 进入面挂 `MobEffect#onEffectAdded`——它在 21.1.213 **只在「之前没有同一效果」的分支**被调（`LivingEntity.java:957-961`），重扫刷新走 `:962` 的 `update`、不触发 ⇒ `sources` 的该位不会重复置位（一个来源只占一位）；(b) 退出面只看 `hasEffect`；(c) **不设**「N 刻后恢复」的任何常量 | §7 #16、#17 |
| **RT-57** | ×3 只作用于敌对：现有 5 处对生物的处理都没有敌我区分，新增判定不能顺手改到别的路径 | ① 把 5 处 `getEntitiesOfClass(Monster.class, …)` 换成新谓词；② 在伤害事件层统一乘；③ 把 `Hostility` 用到触发面上 | ① 陷阱的**触发面**被放宽（非 `Monster` 的敌对生物也开始触发陷阱）⇒ 行为面大改、与 D-174「不改 0.2.0 已有内容」冲突；② 波及非陷阱伤害；③ 同上 | (a) `Hostility` 的**使用点只有 2 处**（`FarmstormExplosion`、`ZangiefCaneTrap#monsterBranch`），写进 §2.4 与 §3 #22/#23；(b) 5 处过滤列进 §3 #28 的「不改」行；(c) 不在任何事件层挂乘 | §7 #19、#20、#27 |
| **RT-58** | 尖牙不参与：验收若照「所有陷阱」写，必然误判 | 4 个 `TRAP` 作物写成「全部 ×3」 | 验收照「所有」写 ⇒ 尖牙（原版伤害）与震动豆（0 伤害）永远测不出 ×3，验收必红；或实现者为了「让验收过」去改原版尖牙（踩红线） | (a) §2.5.3 末的四条逐条点名表（2 是 / 2 否）；(b) §3 #28 的「不改」行；(c) §7 拆成 #19（两个真的 ×3）与 #21（尖牙不变）两条独立验收 | §7 #19、#21 |
| **RT-59** | tooltip 可能本来就是好的：任务会变成「加验收」，容易被写成「改了一堆」 | 实现者不信「默认链路可用」，于是重写 `render` 自绘 tooltip / 加 `Slot` 子类 | 一个 42 行的类变成自绘界面，绕过原版对定位 / 缩放 / 背景 / 延迟显示的处理（D-178 已否决） | (a) §2.2 的四道证据 + 五种破法逐条「现状 0」；(b) §3 #12 的「一行不改」行；(c) 退路写成「验收失败才补 ≤ 2 行」，并给出具体补法 | §7 #11 ~ #13 |
| **RT-60** | `verify_project.py` 的期望面（A-66）：config 175→176、lang 337→338 不同步会**静默少核** | ① 只改源码不改文档；② 只改文档不改解析器；③ 以为「文档写了就等于工具会核」 | 工具照绿，配置多一/少一都发现不了（A-66 的假绿） | (a) 本文 §4.1.1 用**可解析的 7 列表**写死契约（§4.1.1 的「解析契约」四条）；(b) §3 #26 把「加解析器」列成必改项；(c) 期望数 176 与源码 176 由 §7 #2 双向核；(d) **不修**抽取逻辑本身（0.2.0 的既存问题，D-174 挡在门外） | §7 #2、#22 |
| **RT-60b** | `trap.damageMultiplier` 必须**真的写进文档**：期望键集从文档抽取，文档里没有它就等于没加 | 文档只写「新增倍率键」而不写完整键路径、或写进散文而不进表 | 「配置多键 / 文档漏键」双向静默（与 RT-60 同一机制，单列出来因为它是本版**唯一**一个新增键） | (a) 键路径以反引号形式出现在 §4.1.1 的表格第 2 列（可被正则命中）；(b) §4.1.2 再点一次字段名与落盘键名；(c) §7 #22 专门验「文档里真的有这个键」 | §7 #22 |

**十一条的共同点**（写给 43 / 44 号）：每条的缓释都是「**一处定义 + 一行注释 + 一条独立验收**」，没有一条靠「记得小心」。RT-51 / RT-53 的落点是具体方法（`rebindNearby` 的三行判据），RT-55 / RT-56 的落点是具体判据（`hasEffect` 而不是计时器），RT-60 / RT-60b 的落点是**本文 §4.1.1 那张表本身**。

---

## 7. 验收自检清单（31 条）

每条给：编号 / 断言原文 / 观察位置 / 依据。**「观察位置」必须可操作**：`V-nn` 指本文自定的观察点，不引入第二个编号系列。

| # | 断言原文 | 观察位置 | 依据 |
|---|---|---|---|
| 1 | 启动日志出现 `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`；且 `src/main/java` Java 文件数 = **148** | `run/logs/latest.log`（`Farmstorm.java:173` 的格式串）；文件计数 | §1.1、§1.2 |
| 2 | `verify_project.py` 第 2 节打印 `已核 registry 对象: 97 / 97`；第 1 节打印「文档口径键数: **176**」与「生成 toml 键数: 176」 | `.tools` 的 python 跑 `tools/verify_project.py` | §4.2、D-185 |
| 3 | 全仓 `@Mixin` = **0**、`src/` 下 `*.mixins.json` = **0**；`FarmstormNetwork` 的 `.playToClient` 恰 **2** 处（`farmstorm:core_list` / `farmstorm:cultivator_link`），`PROTOCOL_VERSION = "2"` | 源码 grep + `FarmstormNetwork.java:25,38,43` | D-186、§1.4 |
| 4 | `trap` 段在 `run/config/farmstorm-server.toml` 里是 **4** 键（`immunityTicks` / `dustIntervalTicks` / `maxTrapsPerPlayer` / **`damageMultiplier = 3.0`**），段数 **60**，总键数 **176**（先跑一次 `runServer` 重生成 toml 再数） | `run/config/farmstorm-server.toml` | §4.1、§4.2 |
| 5 | 语言文件 `zh=338` / `en=338` / zh-only **0** / en-only **0**；`farmstorm.configuration.*` = **179**（= 176 + 3） | `zh_cn.json` / `en_us.json`，`ConvertFrom-Json` 取键集 | §4.3、D-185 |
| 6 | 核绑 0 个培养器、3×3 内放 **1** 个未绑定培养器、`boundaryParticleIntervalTicks = 40`（默认）⇒ **100 刻内**该培养器出现在 `Cultivators` 里（不是 200 刻） | `/data get block <core> Cultivators`；时间戳（`V-01`） | §2.1.1、D-176 |
| 7 | **同一场景把 `boundaryParticleIntervalTicks` 改成 1200** ⇒ 仍然 **100 刻内**补上。这条是「补绑**没有**被粒子门筛掉」的判决性验收：若复用粒子门，此处要等 1200 刻 | 同上（`V-02`） | §2.1.1、RT-51 |
| 8 | 相位错开：同 3×3 里放 **2** 个核、各放 1 个未绑定培养器 ⇒ 两个核的补绑发生在**不同 tick**（日志的 `gameTime` 相差 ≠ 0） | 补绑 INFO 行（`V-03`） | §2.1.1、裁定 #2 |
| 9 | 未加载区块跳过：把培养器放进 3×3 边缘的区块、让它卸载（走远 / `forceload remove`）⇒ 本轮不补；服务端日志**没有**该区块的加载记录，`ChunkMap` 不因补绑增长 | `latest.log` 的区块加载行（`V-04`） | §2.1.2、RT-52 |
| 10 | 满则停：3×3 内放 **20** 个未绑定培养器、`cultivatorCapacity = 16` ⇒ `Cultivators` 恰 **16** 条且**不再增长**；再等 300 刻仍是 16；新增的 4 个不被收录，已有的 16 条**顺序与坐标一个都不变** | `/data get block <core> Cultivators`（`V-05`） | §2.1.3、RT-53 |
| 11 | 悬停 54 格中**任意一格**（取第 1 / 28 / 54 格做样本）里的物品 ⇒ 出现原版 tooltip：物品名 + 详情行（≥ 2 行），且数量为 1 时不显示「1」 | 游戏内（`V-06`） | §2.2、D-178 |
| 12 | 悬停**空槽** ⇒ 无 tooltip；悬停**玩家背包格** ⇒ 同样有 tooltip | 游戏内（`V-06`） | §2.2 |
| 13 | tooltip 链路未被绕过：`OutpostCoreScreen` 的 `render` / `renderTooltip` / `renderLabels` / `init` 覆写数 = **0**；全仓 `renderTooltip` 命中 = **0**；`OutpostCoreMenu` 里 `new Slot(` = 54 + 36 次、无 `Slot` 子类 | 源码 grep | §2.2、RT-59 |
| 14 | 震动豆：半径内 1 只僵尸（`NoAI:0b`）⇒ 触发后 `NoAI:1b`；**80 刻后** `NoAI` 回到 **0b**；同一刻半径内的第 2、3 只生物**也**都被删 AI（不是只有触发者）。⚠️ 用**生存模式、非己方**的玩家触发（既存口径：创造 / 旁观不触发却会被晕到，§2.3.6 末条、§9 第 13 条） | `/data get entity <mob> NoAI`（`V-07`） | §2.3.6、D-180 |
| 15 | 原值语义：先用 `/data merge entity <mob> {NoAI:1b}` 造一只「本来就 `noAi`」的怪，再触发震动豆 ⇒ 80 刻后它**仍是** `NoAI:1b`（不被错误放出来） | 同上（`V-08`） | §2.3.2、裁定 #8 |
| 16 | Q弹 反复施加不重复写附件：`friend_chives` 落在生物身上后，附件的 `sources` 恒为 `SRC_Q_BOUNCE`（不进位两次）、`originalNoAi` 不随后续刷新改变；日志每次施加只打 1 条 `suspend` | 服务端日志 + `/data get entity`（`V-09`） | §2.3.4、RT-56 |
| 17 | Q弹 三条路径都恢复 AI：① 等效果自然到期（`friend_chives.effectTicks = 200`）；② 中途 `/effect clear <mob> farmstorm:q_bounce`；③ 中途让生物喝牛奶（`removeEffectsCuredBy(MILK)`）⇒ 三条路径都在 **1 刻内**把 `NoAI` 恢复成原值 | `/data get entity <mob> NoAI`（`V-10`） | §2.3.4、RT-55 |
| 18 | mount 态不删：`qbounce.poseMode = mount` 时，带 Q弹 的生物 `NoAI` 恒 **0b**（全程不删）；切回 `render` 后下一次施加才删 | 服务端日志 + `NoAI`（`V-11`） | §2.3.5、裁定 #7 |
| 19 | ×3 只对敌对且只有 2 个陷阱：`blast_carrot` 对僵尸 = **21.0**（`min(raw,7) × 3`）、对玩家 = **7.0**；`zangief_cane` 对僵尸 = **22.5**（7.5 × 3）、对玩家走相位链（不进倍率） | 服务端 `hurt` 前的日志 + 血量读数（`V-12`） | §2.5.3、D-184 |
| 20 | 非陷阱爆炸未被 ×3：`melon_grenade` / `molotov` 对僵尸的伤害与 0.2.0 **逐点相同**（把 `damageMultiplier` 从 3.0 调到 1.0 与 10.0 两次，读数不变） | 同上（`V-13`） | §2.5.2、RT-57 |
| 21 | 尖牙不变：`fang_trap_cactus` 对僵尸的伤害与 0.2.0 逐点相同（原版 `EvokerFangs`，`FangTrapCactusTrap:87-109`，仓内 0 处 `hurt`） | 同上（`V-14`） | §2.5.3、RT-58 |
| 22 | `trap.damageMultiplier` 在**本文档**里真的出现且可被机器读：`grep -c 'trap\.damageMultiplier' docs/zh/42-TECH-0.2.1.md` ≥ 2，且 §4.1.1 的表行能被契约正则命中 1 行；`verify_project.py` 新增的解析函数返回 `["trap.damageMultiplier"]` | 文档 + 工具（`V-15`） | §4.1.1、RT-60b |
| 23 | 停服不把窗口带到下一档（裁定 #16）：1 只生物被震动豆定身、**在 80 刻未满时 `/stop`** ⇒ 重启后该生物的 `NoAI` 是 **0b**（提前恢复），且实体的 `neoforge:attachments` 里**没有** `farmstorm:ai_suspend` | 停服后 `latest.log` + 重启后 `/data get`（`V-16`） | §2.3.3、§5.1 |
| 24 | 崩溃残留按附件结算：起服 → 触发定身 → **强杀进程**（不 `/stop`）→ 重启 ⇒ 该生物载入时读到 `sources`、震动豆那一路被当已到期 ⇒ `NoAI` 回到原值（≤ 1 刻），附件被删；Q弹 那一路若效果仍在则**继续**挂起 | `/data get entity` + 服务端日志（`V-17`） | §2.3.3、RT-54 |
| 25 | 不新增 `SavedData`：`world/data/` 下 `.dat` 文件数与本版前**相同**（现 3 个：farmland / outpost_core / 陷阱 FIFO），mtime 只随正常存档变化 | `world/data/` 目录 | §5.3 |
| 26 | 补绑路径不碰两个禁用方法：`OutpostCoreBlockEntity` 里 `removeCultivator(` 的调用点 = **0**、`pruneCultivators(` 的调用点 = **1**（只在 `cultivators()` 里，`:329`） | 源码 grep（方法体范围内） | §2.1.3、§5.4 |
| 27 | `Hostility` 只有 2 个使用点；5 处 `getEntitiesOfClass(Monster.class, …)` 全部**未动**（行号仍是 `BlastCarrotTrap:59` / `FangTrapCactusTrap:54` / `VibrationBeanTrap:59` / `ZangiefCaneTrap:174` / `PumpkinTurret:109`） | 源码 grep | §2.4、RT-57 |
| 28 | 恢复即删附件：任何一次 `release` 之后，实体在 `/data get entity <mob>` 里**看不到** `neoforge:attachments.farmstorm:ai_suspend`；`sources == 0` 的实体**一律没有**这个键（`NONE` 不落盘） | `/data get entity`（`V-18`） | §2.3.2、裁定 #15 |
| 29 | 第 13 类被显式登记且 97 不变：`registry/FarmstormAttachments.java` 有 **1** 个 `AttachmentType` 注册；`Farmstorm.java` 构造器多 **1** 行 `.register(modBus)`；`Farmstorm.java:167-172` 的 12 类断言**一字不改**；启动日志仍是 `自检 注册对象 97 个`；§1.2.1 存在且写明「不计入 97 / 工具不核它」 | 源码 + `latest.log` + 本文 §1.2.1 | 裁定 #17、D-175 |
| 30 | 无主核不补绑：`/setblock` 放一个核（`ownerUuid` 为空）+ 3×3 内 1 个未绑定培养器 ⇒ 等 **300 刻**，该核的 `Cultivators` 仍是 **空** | `/data get block <core> Cultivators`（`V-19`） | §2.1.3、裁定 #13 |
| 31 | 候选排序确定：3×3 内放 **20** 个未绑定培养器、容量 16 ⇒ 绑到的 16 条按 `distSqr` 升序、平手按 `x`→`y`→`z`；**同一存档重跑两次，`Cultivators` 的 16 条逐字相同** | `/data get block <core> Cultivators`（`V-20`） | §2.1.3、裁定 #14 |

**31 条的分布**：发布与计数 **5**（#1~#5）/ 补绑 **7**（#6~#10, #30, #31）/ tooltip **3**（#11~#13）/ AI **7**（#14~#18, #23, #24）/ 倍率 **3**（#19~#21）/ 文档与工具 **1**（#22）/ 存档与附件 **3**（#25, #28, #29）/ 结构与禁令 **2**（#26, #27）= **31**。

**四条必测组合（写给出验收的人）**：① #6 与 #7 **必须成对跑**——只跑 #6 看不出「被粒子门筛成 200 刻」，因为 40 的默认值下 200 刻也「在 300 刻内」；#7 把粒子间隔拉到 1200，任何复用粒子门的实现都会当场暴露。② #14 与 #15 **必须成对跑**——只测普通怪分不清「恢复成有 AI」与「恢复成原值」。③ #19 与 #20 **必须成对跑**——只测陷阱分不清「乘在陷阱那一次」与「乘在所有爆炸上」。④ #23 与 #24 **必须成对跑**——前者验 `ServerStoppingEvent` 那条尽力路径，后者验「附件 + 载入结算」那条兜底路径；只跑 #23 会把「正确性依赖停服事件」这个误解固化下来（而它恰恰不依赖）。

---

## 8. 待验证 API 清单（10 条）

**判定口径**：一条 API 只有满足「类型 / 方法签名 / 调用点参数个数三者都在本机可读的源码里逐字看到」才算「已核」。本机的 NeoForge 源码取自 `.tools/gradle-home/caches/neoformruntime/intermediate_results/sourcesAndCompiledWithNeoForge_c95866ec…_output.jar`（17459 个条目，含 MC 与 NeoForge 的 `.java`）；下文的行号是**该 jar 内文件的行号**，不是本仓行号，已逐处标注。

| # | API 或语义 | 已核到什么程度 | 为什么仍待验证 | 由谁确认 | 退路 |
|---|---|---|---|---|---|
| ① | `LevelChunk#getBlockEntities()` 的返回语义与未加载判定 | **已核签名**：`public Map<BlockPos, BlockEntity> getBlockEntities()` 且 `return this.blockEntities;`（`LevelChunk.java:528-530`）——返回的是**活映射**，不是副本。`ServerChunkCache#getChunkNow` 返回 `@Nullable LevelChunk`（本仓 `OutpostCoreBlockEntity:296-300`、`:384-387` 已在用） | ① 「活映射」在遍历期间被改会不会 CME，本机只有 `DeployedCropBlockEntity:230` 的**单区块、无写入**先例；② `getChunkNow` 对「已加载但本 tick 未 tick」的区块返回什么，没有运行期证据；③ `blockEntities` 是否包含「已创建但未 tick 的 BE」（本版只需要 `CultivatorBlockEntity`，它必然已创建） | 补绑任务（实现后跑 §7 #6~#10） | 若 CME 真的出现 ⇒ 在 `collect` 里先 `new ArrayList<>(chunk.getBlockEntities().values())` 再过滤（现在是同一写法，只是把「拷贝」显式化并 try/catch 兜一层） |
| ② | `Mob#setNoAi` 与「原值」怎么读回 | **已核**：`public void setNoAi(boolean)`（`Mob.java:1399`）、`public boolean isNoAi()`（`:1414`）；`NoAI` 落盘在 `Mob.java:437-438`（只在 true 时写）、读档 `:501`；`isEffectiveAi()` 里 `&& !this.isNoAi()`（`:1396`） | ① `/data get entity <mob> NoAI` 在「值为 false 时键不存在」这一点上能否稳定读数（`NoAI:0b` 与「没有这个键」在 NBT 层是两回事）；② 删 AI 后 `Mob#tick` 的哪些分支被跳过（`:437` 那一处），对「坐骑是否还移动」的实测结论只来自注释 | AI 任务（§7 #14 / #15） | 验收读数改成「有键且为 1b」= 被删、「无键」= 有 AI（把 `false` 与「缺键」当同一态，与 `Mob.java:437` 的写法一致） |
| ③ | `MobEffect` 的 `onEffectAdded` / `onEffectRemoved` 在 21.1.213 的可用性（`QbounceEffect` 现 0 覆写） | **已核（结论是「一有一无」）**：`MobEffect#onEffectAdded(LivingEntity, int)` **存在**（`MobEffect.java:86`），由 `MobEffectInstance#onEffectAdded` 转发（`MobEffectInstance.java:341-343`），且只在 `LivingEntity.addEffect` 的「之前没有同一效果」分支被调（`:957-961`；刷新走 `:962` 的 `update`）。**`onEffectRemoved` 不存在**（`MobEffect.java` 218 行内 0 命中）。退出面由 NeoForge 事件覆盖，且本仓已有先例：`MobEffectEvent.Remove`（`QbounceObserverSync:130`）、`MobEffectEvent.Expired`（`:136`）、`MobEffectEvent.Added`（`QbounceMountController:109`） | ① `onEffectAdded` 在「效果从 NBT 读回」时**不会**被调（`readAdditionalSaveData` 直填 `activeEffects`）——本版的设计正好要求这一点（重启后不接管），但没实测过；② `LivingDeathEvent` 时效果不被摘掉（本仓 `QbounceObserverSync:142` 的注释记过这条），对账要自己处理「实体已死」 | AI 任务（§2.3.4 的实现） | 若 `onEffectAdded` 在某条路径上不触发 ⇒ 补挂 `MobEffectEvent.Added`（带 `getOldEffectInstance() == null` 过滤，照 `QbounceMountController:117-119`），两处同时挂也不会重复（`sources` 位幂等） |
| ④ | `Enemy` 接口与 `MobCategory.MONSTER` 的取值方式 | **已核**：`Enemy` 是纯标记接口（`Enemy.java:1-11`，只有 6 个 `XP_REWARD_*` 常量）；`Monster extends PathfinderMob implements Enemy`（`Monster.java:29`）；类别取值 `public MobCategory getCategory()`（`EntityType.java:1043`） | ① 模组生物的覆盖缺口（既非 `Enemy`、类别也非 `MONSTER` 的「伪敌对」）无法枚举；② `entity.getType().getCategory()` 对非 `Mob` 实体返回 `MISC`（无副作用，但没有运行期证据） | 倍率任务（§7 #19） | 若实测漏判某模组生物 ⇒ 只登记（40 号 §7 ③ 已把这条定为「不扩判据」），不自行加白名单 |
| ⑤ | `verify_project.py` 从文档抽取键集的具体函数 | **已核**：`main()` `:349-355` 读取 6 份固定文档（`01-DESIGN` / `12` / `18` / `24` / `30` / `36`）；`expected_config_keys_v012`（`:137-179`）按 `#### 4.1.1` ~ `#### 4.1.2` 界定、7 列、行数断言 11；并集与删键在 `:382-391`；**它今天不读 `docs/zh/42-*`** | ① 新增解析器后 `main()` 的期望数会从 175 变 176，`30-TECH §4.1` 的 58 组断言（`:228-230`）不受影响，但 `run/config` 那份 toml 必须**先重生成**（现在盘上的是旧版，§9 第 2 条）；② §3「语言键」段（`:464-474`）只比**键集相等**，**不校验 338 这个数**——「语言键 338」没有自动闸 | 工具任务（§7 #2 / #5 / #22） | 若「读 42 号 §4.1.1」的表格格式被后续改动破坏 ⇒ 解析器按 `:172-178` 的形态**带文件名 + 行号抛出**（宁可红，不要静默少核） |
| ⑥ | `EntityJoinLevelEvent` 的触发面（载入时结算） | **未核**：本仓 0 处使用；本机没有它的运行期证据 | ① 它是否覆盖「区块加载把实体放回世界」这一档（而不只是 `/summon` 与首次加入）；② 触发时机与 `getData(AI_SUSPEND)` 的可读性先后（附件在读档时就已反序列化，理论上更早，但没实测） | AI 任务（§7 #23 / #24） | 起服**有界补扫**：照 `QbounceMountController` 的 `ADOPT_RESCAN_INTERVAL_TICKS = 20` / `MAX_ADOPT_RESCANS = 10`（`:63-70`、`:190-194`）做 10 轮；已知缺口：10 秒后才加载的区块里的实体不会被结算（登记为 RT-54 残余） |
| ⑦ | `EventPriority.HIGHEST` 在 `ServerTickEvent.Post` 上的排序 | **未核**：本仓 `ServerTickEvent.Post` 的三个订阅者（`QbounceMountController:174`、`QbounceObserverSync:120`、`FarmstormGameEvents`）都没有写过 priority | 「AI 对账必须先于 mount 挂载」只在逻辑上成立（HIGHEST < NORMAL），没有运行期证据 | AI 任务（§7 #18） | 改挂 `ServerTickEvent.Pre`（事件阶段天然早于所有 `Post` 订阅者，不依赖 priority）；代价是「同一 tick 内刚被删的效果要下一 tick 才恢复」（位移损失 1 刻，可接受） |
| ⑧ | `AttachmentType` 的落盘 / 读回时点与体积 | **已核读写面**：`AttachmentHolder.serializeAttachments`（`:122-145`）、`deserializeAttachments`（`:151-172`）、键名 `neoforge:attachments`（`:27`）、`Entity extends AttachmentHolder`（`Entity.java:131`）；`AttachmentType.builder(Supplier)`（`:95`）、`.serialize(Codec, Predicate)`（`:196`）、`.build()`（`:294`） | ① 实体附件在**区块存档**那条路上的写出时机（`serializeAttachments` 由 `Entity` 的保存钩子调用，本机只读到签名、没读到调用链）；② 每实体一个 ≤ 40 字节 compound 对区块体积的实际影响；③ `.serialize(Codec, Predicate)` 的 `shouldSerialize` 为 false 时**完全不写键**（而不是写空）——这决定「正常状态下存档里零字节」这句话成不成立 | AI 任务（§7 #28） | 若 `shouldSerialize` 仍会写出空 compound ⇒ 改为「`release` 时 `removeData`，且默认值仍是 NONE」，体积影响 ≤ 40 字节，结论不变（只是「零字节」这句话要改成「≤ 40 字节」） |
| ⑨ | `ArmorStand` 坐骑与 `noAi` 的交互（震动豆 × mount 同刻） | **未核**：`QbounceMountController:307-316` 的三条校验不含 `noAi`，只能推断「坐骑会带着不动的实体」 | 「有坐骑 + `noAi`」持续 80 刻的实际表现（会不会被原版 `Mob#tick` 的 `:437` 分支打断骑乘）没有实测 | 验收任务（§7 #14 与 #18 的相邻场景，本版只登记不豁免） | 若实测出现「坐骑被摘 / 实体被弹下车」⇒ 按 RT-55 登记，不改裁定 #4 的生效面 |
| ⑩ | `getData` / `setData` / `removeData` / `hasData` 的默认值语义 | **已核签名**：`hasData`（`IAttachmentHolder.java:24`）、`getData`（`:39`）、`setData`（`:90`）、`removeData`（`:106`）；`AttachmentHolder` 的实现 `:68` / `:74` / `:97` / `:107` | ① `getData` 取默认值时**会不会把默认值写进 map**（决定 `hasData` 能不能当「是否挂起」的判据——本文因此**不用** `hasData`，只用 `sources != 0`）；② `removeData` 之后紧接着 `getData` 是否确定回落到 `NONE` | AI 任务（§7 #28） | 若 `getData` 会写 map ⇒ 也**不影响**本文（存在判据是 `sources != 0`）；若 `removeData` 之后 `getData` 不回落 ⇒ 给默认值提供者加一层 `sources == 0 ? NONE : 已存值` 的兜底 |

**三条「已从待验证降级为已核」的记录**（写在这里，免得 43 / 44 号再当风险）：① `MobEffect#onEffectAdded` 的存在与触发分支（`MobEffect.java:86` + `LivingEntity.java:957-961`）；② 三条效果移除路径的收敛点（到期 `LivingEntity.java:790`、`/effect clear` `:911-921`、牛奶 `MilkBucketItem.java:27` → `LivingEntity.java:3517-3525`，三者都过 `EventHooks.onEffectRemoved`，`EventHooks.java:1050-1056`）；③ `AttachmentType` 的注册表与序列化面（`AttachmentType.java:35` + `AttachmentHolder.java:27/122-145/151-172` + `Entity.java:131`）。

---

## 9. 与上游的差异登记（16 条，**不自行改上游**）

按「与 40 号的矛盾 / 与 `BRIEF` 的矛盾 / 与工具实际行为的偏差 / 源码注释漂移」四类排。**本文只登记，不改任何上游文件。**

| # | 涉及 | 上游写的 | 本文的处理 | 性质 |
|---|---|---|---|---|
| 1 | 40 号 D-173 补充 + §7 ⑤ vs `BRIEF` §3.4 第 2 行 | 40 号说「两条 `.cmd` 的**抬头行**（各 `:5`）也写死 `Farmstorm v0.2.0`，`BRIEF` §3.4 第 2 行**未列入**（登记在 §7 ⑤）」；§7 ⑤ 再说一次「`BRIEF` §3.4 第 2 行只列了 jar 名三处」，并把「抬头行是否同改」交给 42 号定 | **实核：`BRIEF` 第 183 行（§3.4 第 2 行）原文已经写了「与两支脚本的抬头行（`一键构建.cmd:5`、`检查环境.cmd:5`，写死 `Farmstorm v0.2.0`）」** ⇒ 40 号这两处的描述与事实卡不符。本文按 `BRIEF` 执行（**抬头行同改**，见 §3 #2 与 #4），并把「是否同改」这一问消掉（`BRIEF` 已经答了） | 40 号描述失真（不影响结论） |
| 2 | `BRIEF` §0 的 toml 旁证 | `BRIEF` §0 写 config「（与 `run/config/farmstorm-server.toml` 的 **175 key / 60 段**一致） \| 实测」 | **成立，已复核**：`run/config/farmstorm-server.toml` 实测 **801 行 / 60 段 / 175 键**（mtime `2026-11-04 11:29`，键以行首 Tab 缩进为特征），与 `FarmstormConfig.java` 现数一致。⚠️ 但仓库里另有大量 `run-*/config/` 旧副本（`run-verify`、`run-f*` 等，12,238 B 一档，约 **121 键 / 36 段**）——**数键时必须认准 `run/config/` 这一份**。验收前必须先跑一次 `runServer` 重生成（`verify_project.py:437-440` 对这种情况有一条 STALE 提示，判据相同） | 本文初稿误取了旧副本，已更正 |
| 3 | `BRIEF` §0.2 / D-175 / D-185 对工具机制的描述 | 三处都说「`verify_project.py` 的期望键集是**从 `docs/zh/*` 抽取**的」 | **不准确**：它只读 **6 份固定文档的固定小节**（`main():349-355` 的 `01-DESIGN` / `12` / `18` / `24` / `30` / `36`），既不是通配 `docs/zh/*`、也不读 40 ~ 45 号。⇒ 「把新键写进本文」**必要但不充分**：还要按 §4.1.1 的契约新增 `expected_config_keys_v021`，两者同一提交。这条是 A-66 假绿在**本版**的具体形态 | 上游描述不精确（机制本身仍是 A-66） |
| 4 | `BRIEF` §0 / D-185 对语言键的自动闸 | 两处都把「语言键 337 → 338」与 config 键并列为「要同步期望面」 | `verify_project.py` 第 3 节（`:464-474`）**只校验 zh 与 en 键集相等**，**不校验 338 这个数**。⇒ 「语言键 338」在本版**没有自动闸**，只能人工数（§4.3 的 `ConvertFrom-Json` 方法）并写进 §7 #5 | 缺失（会静默） |
| 5 | `BRIEF` §4 的风险编号 | 表里在 RT-56 与 RT-57 之间插了一条 **`RT-60b`**，而 RT 系列本应连续（40 号 §0.1 的接续表说「风险 RT-51 起」由 42 号写） | 本文**不新开 RT 编号**：RT-51 ~ RT-60 逐条成立（§6），`RT-60b` 作为 **RT-60 的子条**单列一行（同一机制的第二个入口：`trap.damageMultiplier` 这一个键），编号沿用 `BRIEF` 的写法、不另起 61。若 43 号要严格连续，把 §6 的最后一行并进 RT-60 即可，本文的验收覆盖不变（§7 #22） | 编号不连续（照抄 `BRIEF`，不改） |
| 6 | `BRIEF` §2 的条数与 40 号的转写 | `BRIEF` 第 5 行写「§2 的 **9 条**是作者访谈当场拍板的」，§2 标题曾写「作者裁定（**11 条**）」；**写作期间作者又追加 #13 ~ #17，`BRIEF` §2 现共 17 条**（本条据作者消息与 `BRIEF` 现行版本） | 本文按 **17 条**引用（#1~#12、#13 无主核、#14 排序、#15 附件落盘、#16 窗口不跨档、#17 第 13 类显式登记）。**40 号仍只转写了 11 条**（D-173 ~ D-186 与 §2 的映射表也只覆盖 #1~#11）⇒ #12 ~ #17 这 6 条**没有 D 号**，需 43 号从 A-70 起补登（本文按纪律不自己开 D 号）。40 号 §2 备注 3 已登记过「9 vs 11」那一处，本文只更新为 17 | 上游已变，40 号需回改（**结论不变**：97 / 176 / 338 三个数都不受影响） |
| 7 | 40 号 §7 ① 交给 42 号的一问 | 「补绑会不会让 `removeCultivator` 变成必需（例如补错之后要能撤），由 **42 号**在 D-177 的框架内定」 | **本文定为「不需要、不启用」**：补绑只加不摘（D-177），「两侧不一致」的候选一律**跳过**而不是「先绑再摘」⇒ 没有「撤」的场景。`removeCultivator`（`:366-377`）的调用点**仍是 0**，本版不启用、不删（删死代码属既存清理，D-174 挡在门外）。§7 #26 是这条的验收 | 上游留的活，本文已办 |
| 8 | 40 号 §7 ⑥ 与 §2.3.7 的交叉后果 | 40 号只说「震动豆的 AI 剥夺是一次性快照」，没说「被定身的生物正被 mount 分支挂着坐骑」时怎么办 | **本文补一条交叉规则**（§2.3.7）：震动豆**不因 mount 豁免**（裁定 #4 没有这个例外），后果是「有坐骑 + 不动」最多 80 刻，之后按原值恢复、坐骑照旧。这条不进 40 号（本文补充），但它是 41 号写玩家可见说明时必须知道的一条 | 缺失（本文补充） |
| 9 | 40 号 §0.2 术语「倍率」的值域 | 「值域与字段名由 42 号在本条框架内定」 | 本文定为 **1.0 ~ 10.0**（§4.1.2 给了四条理由）。与 40 号「默认 3.0、double」不冲突 | 上游留的活，本文已办 |
| 10 | 本仓 Mixin 计数的口径 | `BRIEF` §0 写「Mixin **0** \| 实测」 | **按 `src/` 口径成立**：`src/` 全树 `@Mixin` 0、`*.mixins.json` 0。但**全仓**（含 `.scratch/`）有 **1** 个 `neoforge.mixins.json` 在 `.scratch/p25a/src/`——实验残留，不在 `src/`、不参与构建。验收若用「全仓 `Get-ChildItem -Recurse -Filter *.mixins.json`」会命中 1 ⇒ 本文 §7 #3 把口径写成「`src/` 下 = 0」，并在这里点名那一个文件 | 计数口径（不是缺陷） |
| 11 | 源码注释漂移（不影响计数） | — | 两处：① `FarmstormBlocks.java:43` 的注释写「v0.0.3 新增的 **3 个**方块」，而该段只有 **1** 个（`FRIEND_CHIVES_CROP`，`:45`）；② `ModLangProvider.java:237` 的注释写「**139 个**配置项」，而该文件的 `config(entries, …)` 实为 **178** 次（`:14` 的注释写的是 178，正确）。两处都不影响本文的 blocks **27** / 语言键 **338**（那是由注册调用点与键集数出来的），本版不改注释（D-174） | 源码注释漂移（登记） |
| 12 | 本机读法差异（会让行号全错） | `BRIEF` 与 36 号都用 UTF-8 读法给行号 | 但**默认的 `Get-Content`（无 `-Encoding`）会把两份文件读成不同行数**：`FarmstormConfig.java` = 1131 行（UTF-8）对 1095 行（默认），`OutpostCoreBlockEntity` 等同样有偏移；`FarmstormConfig` 里 `trap` 段的 `:917` / `:920` 在默认读法下变成 `:906` 一带。⇒ 本文所有行号一律 UTF-8（§0.2 第 4 条），并在此点名「40 号与 `BRIEF` 的行号是 UTF-8 口径、可在本机复现」 | 读法口径（先记下来，免得下一个作者对不上） |
| 13 | 41 号报的**既存不一致**（不在本版范围） | 震动豆 `findTrigger` 会筛创造 / 旁观 / 己方（`VibrationBeanTrap:51-54`），而 `trigger` 的混乱循环遍历**全部** `level.players()`、**不筛**（`:83-91`） | 本版**不修**（D-174 的范围闸）。后果：创造 / 旁观玩家不会触发震动豆，却会被**别人**触发的震动豆晕到。处置：§2.3.6 末条写明这条既存口径，§7 #14 / #15 一律用**生存模式、非己方**玩家测，避免把既存行为记成本版回归 | 既存缺陷（登记，本版不动） |
| 14 | 裁定 #17 vs D-175 的措辞 | D-175 以「**会把注册面变成 98**」为由否决「用数据组件承载 AI 原值」；本版改用 `AttachmentType`，读者容易误以为「换了个名字绕过了自己的否决」 | **附件 ≠ 数据组件**：`DataComponentType` 是本项目 12 类里的第 11 类（现值 1 个 `farmstorm:temporary_enchantments`，加一个 ⇒ 2 ⇒ 98），D-175 否决的是它；`AttachmentType` 注在 `NeoForgeRegistries.Keys.ATTACHMENT_TYPES`（口径外）⇒ **97 不变**。裁定 #17 已明确「97 不变，但必须显式登记、不许隐形」⇒ 本文为此单列 **§1.2.1「口径外的第 13 类」**，并在 §3 #19 / #29、§7 #29 三处登记。**建议 40 号在 D-175 的补充里加一句**「否决的是 `data_components`，不是附件」 | 已裁定（#17），建议 40 号补一句措辞 |
| 15 | 裁定 #16 与 D-179「记时点」批评的关系 | D-179 批评 `WildHunterAura:52` 的 `AI_RESTORE_AT`（存「到期时刻」）；那么「把到期刻也存进附件」就是同一形态的复发 | 裁定 #16 明确**否决**「把到期时刻存进附件」⇒ 本文的附件只有 `(originalNoAi, sources)`，震动豆的到期刻只在内存（`VB_DEADLINE`）。**这不是矛盾，是同一批评的延伸**；登记在此，是为了让读者理解「附件里为什么没有时间戳」，并防止后来者「顺手加一个 deadline 字段」 | 口径一致（登记，防复发） |
| 16 | 裁定 #16 顺带**消掉**的一个守卫（设计收敛） | 若不采纳「窗口不跨档」、而是把到期刻也落盘，就必须再配一条「时间倒流守卫」（`deadline - now > 80` 就按已到期处理，先例 `OutpostCoreBlockEntity:540-541` 的 `LAST_RING_TICK`） | 附件不存任何时间戳 ⇒ 回档 / 复制世界不需要特判，该守卫**不需要存在**。`OutpostCoreBlockEntity:540-541` 仍保留为「时间倒流确实会发生」的旁证，但本版没有需要它的地方。这条登记是为了让 43 号知道：**验收里不该出现「回档后时间倒流」这一项**，也不该有人往附件里加 `deadline` 字段 | 设计收敛（登记） |

**四条给 43 号的提示**：

1. 第 3 条是**本版最容易漏的必改**：`verify_project.py` 的解析器不改，`trap.damageMultiplier` 写不写进文档都一样绿。它必须与 `FarmstormConfig` 的那一行**同一个提交**，且与 42 号 §4.1.1 的表格格式绑定。
2. 第 2 条会影响验收的**第一步**：先让 `runServer` 重生成 `run/config/farmstorm-server.toml`，再数 176 / 60；但仓库里有 `run-verify`、`run-f*` 等旧副本（约 121 键 / 36 段）——**认准 `run/config/` 这一份**，别拿旧副本当基线。
3. 第 1 条与第 6 条都是**上游描述失真、结论不变**，不需要作者再裁；第 7 / 8 / 9 / 13 条是 40 号或 41 号明确留给 42 号的口子，本文已各自给了结论（不启用 `removeCultivator` / 震动豆不豁免 mount / 值域 1.0~10.0 / 既存不一致不修）。
4. **第 6 条与第 14 条要给 40 号回改**：前者是「`BRIEF` §2 已 17 条，40 号只转写 11 条」——#12 ~ #17 需要 43 号从 A-70 起补登 D 号；后者是 D-175 的措辞（「否决数据组件」≠「否决附件」）。两条都**不影响** 97 / 176 / 338，也不影响本版任何一条验收。
