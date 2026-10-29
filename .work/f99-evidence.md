# F-99 证据 —— Phase 15 出口（Lane A 部分）

环境：`run-p13` / 25631 / `FS_RUN_LOCK=run-p13`；单个客户端（`.work/client-quickplay.gradle`）。
**未跑 `runData`**（F-98 归 Lane B，避免撞车）；本节全部是只读核对。

## 1. 资源计数 —— ⚠️ 实测 302，文档写 299（需裁决口径）

统计口径：`src/main/resources` + `src/generated/resources` 全量文件，排除 `.cache` 与 `.gitkeep`。

| 类型 | 实测 | 文档 (13-TASKS #17) | 差 |
|---|---|---|---|
| 总数 | **302** | 299 | +3 |
| JSON | **195** | 192 | **+3** |
| PNG | 100 | 100 | ✓ |
| OGG | 2 | 2 | ✓ |
| GLSL | 3（`.vsh` 1 + `.fsh` 2） | 2 | +1 |
| TOML | 1 | 1 | ✓ |
| `pack.mcmeta` | 1 | 1 | ✓ |

**+3 JSON 的来源**：其中 **2 个是 F-82 改造新增**的 PostChain 资源
（`assets/farmstorm/shaders/program/grayscale.json`、`assets/farmstorm/shaders/post/grayscale.json`），
第 3 个待定；另 `shaders/program/grayscale.fsh` 使 GLSL 从 2 变 3。
> 结论：手写全屏 pass 换成官方 `PostChain` 的代价就是 +2 JSON / +1 GLSL。建议文档把 #17 改成 302（或声明「F-82 改造后 +3」）。

## 2. 语言 / config / 自检行 —— 全过

| 项 | 读数 |
|---|---|
| `zh_cn.json` | **263** 键 |
| `en_us.json` | **263** 键 |
| 键集相等 | `Compare-Object` **无输出**（相等） |
| `farmstorm.configuration.*` | **139** |
| `tooltip.farmstorm.*` | **16** |
| `run-p13/config/farmstorm-server.toml` | **139** 键 |
| 自检 注册对象 | `自检 注册对象 77 个：items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1 → OK` |
| 自检 实体渲染器 | `自检 实体渲染器 11/11 → OK` |

## 3. §11 的 22 条 —— 本轮可判的已全部取到

| # | 项目 | 读数 | 判定 |
|---|---|---|---|
| 1 | 19 条作物档案 | `自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK` | ✅ |
| 2 | 77 个注册对象 | 见 §2 原文 | ✅ |
| 3 | 幸运浆果池 16 | `自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK` | ✅ |
| 4 | 11 个渲染器 | `自检 实体渲染器 11/11 → OK` | ✅ |
| 5 | 灰度链加载 | `Shader could not be loaded` / `GL_INVALID` = **0 行**；`灰度 PostChain 加载 farmstorm:shaders/post/grayscale.json`；作者确认无卡顿 | ✅ |
| 6 | 两个声音事件 | `sounds.json` 内 `wild_hunter_loop` / `zangief_piledriver` 均在；注册自检 `sound_events 2` | 间接 ✅（`/playsound` 需在场玩家，留给 #22 那次一起） |
| 7 | 莫洛托夫落点 | Phase 12（F-69/F-70/F-75）已验，本轮未重跑 | 引用 |
| 8 | 压缩膨胀麦沿用 | Phase 12（F-68/F-75） | 引用 |
| 9 | 压缩干草块抗性 | Phase 12（F-74/F-75） | 引用 |
| 10 | 桑吉尔蔗三相位 | Lan B（F-88~F-93） | 引用 |
| 11 | 只对非己方 | Lane B（F-87/F-93） | 引用 |
| 12 | 输入锁 | Lane B（F-92/F-93） | 引用 |
| 13 | 剪影三条件 | F-83 逐条验过 + 作者真机确认（`febad56`） | ✅ |
| 14 | 冲刺走服务端复核 | F-85：真包 → `吸附挥击 速度 0.60` → `目标 … 夹角 26.0 度 距离 2.40` → `结算 … 伤害 1.4 击退 0.4`；无 `moved too quickly!`（本轮日志 0 行） | ✅ |
| 15 | 击杀续时无上限 | F-85：6 连杀 `13899→14058→14218→14378→14538→14698`（每只 +160），6 行 `续时` | ✅ |
| 16 | 配置键 139 + 两侧相等 | config **139** / `farmstorm.configuration.*` **139** | ✅ |
| 17 | 资源 299 | **实测 302**（见 §1，+3 来自 F-82 改造） | ⚠️ 文档口径 |
| 18 | 语言 263 + 中英相等 | 263 / 263 / 相等 / tooltip 16 | ✅ |
| 19 | 三个新进度 | F-98（Lane B，`184e24f`） | 引用 |
| 20 | 5 条新配方 | Phase 12（F-71/F-72/F-73）+ F-77 双向 | 引用 |
| 21 | 0 个自定义包 | `CustomPacketPayload` = **0** | ✅ |
| 22 | 客户端在专用服务器上不崩 | 服务端日志 `NoClassDefFoundError` = **0**、客户端 = **0**；F-85 期间客户端连 `run-p13` 专用服务器并跑通属性/索敌/伤害/续时/灰度/剪影/循环音 | ✅ |

## 4. V-47 红线（两种读数）

用法：`CustomPacketPayload` **0**、`@Mixin` **0**、`org.spongepowered` **0**。
字面 `Mixin` = **5 处，全是解释性注释**：`InputLockHandler:30/114`、`WildHunterCombatEvents:19`、`WildHunterSwingSettler:12`、`FarmstormExplosion:36`。

## 5. jar 内容（`build/libs/farmstorm-0.0.2+mc1.21.1.jar`，780 KB / 485 条目）

| 期望 | 实测 |
|---|---|
| 2 个 `.ogg` | `assets/farmstorm/sounds/wild_hunter_loop.ogg`、`zangief_piledriver.ogg` ✅ |
| `sounds.json` | `assets/farmstorm/sounds.json` ✅ |
| 4 个灰度相关资源 | 实际 **7 个**：`shaders/core/grayscale.{json,vsh,fsh}`、`post_effect/grayscale.json`、`shaders/post/grayscale.json`、`shaders/program/grayscale.{json,fsh}` ✅（多出来的是 PostChain 改造所需） |
| 语言 | `lang/zh_cn.json`、`lang/en_us.json` ✅ |
| Probe | **0 个** ✅ |

⚠️ 小瑕疵：jar 里带了 `assets/farmstorm/post_effect/.gitkeep`（占位文件）。目录已非空，可删；不影响功能。

## 6. V-67 配方书分类（前一件任务，已验完）

- `Unknown recipe category`：**24 条 → 0 条**；
- 打开背包/配方书 UI：`[V67PROBE] inventory opened screen=true` + 截图 `.work/v67-shots/inventory-recipebook-ok.png`，无异常、无新 crash-report。
