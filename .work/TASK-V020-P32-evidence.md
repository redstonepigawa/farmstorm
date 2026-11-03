# Phase 32 证据 · v0.2.0（F-213 / F-214 / F-215）

> 执行者：Phase 32 唯一执行者。工作目录 `D:\project_farm`。基线 = v0.1.1 已发布态（`git describe` = `v0.1.1-5-ge12a812`，`git diff --stat v0.1.1 -- src` 为空）。
> 本文按 §8 的 8 项组织，并按 **F-213 / F-214 / F-215 分段**（调度者据此三次提交）。
> 所有「改前值」都是在动手前实测的（38 号陷阱 20 元规则）。

---

## 0. 改前实测值（陷阱 20 基线）

| 判据 | 改前实测 |
|---|---|
| `gradle.properties:11` | `mod_version=0.1.1`（`0.1.1` 命中 1） |
| `farmstorm-0.1.1` @ `一键构建.cmd` | **2** 命中（`:11` 主 jar、`:12` `-sources.jar`） |
| `farmstorm-0.2.0+mc1.21.1` @ `一键构建.cmd` | **0** |
| `farmstorm-0.1.1` @ `检查环境.cmd` | **1** 命中（`:14` 的 `if exist`） |
| `farmstorm-0.2.0+mc1.21.1` @ `检查环境.cmd` | **0** |
| `build/libs` | `farmstorm-0.1.1+mc1.21.1.jar` 1063181 B + `-sources.jar` 978402 B |
| lang `zh_cn.json` / `en_us.json` | 各 **323** 键，zh-only 0 / en-only 0 |
| `tooltip.farmstorm` 前缀 | 两份各 **16** |
| `tooltip.farmstorm.tier*` | 两份各 **0** |
| `textures/item` PNG | **29** 张（sha256 清单存 `.work/p32-tex-before.txt`） |
| `new CropProfile(` @ CropProfile.java | **10**（7 直接构造 + 3 工厂内部） |
| 注册面断言常量 | `total == 93` / `menu_types == 1`（`Farmstorm.java:147-150`） |

---

## 1. `git diff --stat` 原文

```
 gradle.properties                                  |  2 +-
 .../resources/assets/farmstorm/lang/en_us.json     |  4 ++
 .../resources/assets/farmstorm/lang/zh_cn.json     |  4 ++
 src/main/java/com/dashpig/farmstorm/Farmstorm.java | 17 ++++++
 .../dashpig/farmstorm/datagen/ModLangProvider.java |  5 ++
 .../dashpig/farmstorm/item/DeployableCropItem.java |  9 +++
 .../dashpig/farmstorm/item/ModifiedCropItem.java   | 17 ++++++
 .../java/com/dashpig/farmstorm/item/SeedItem.java  |  9 +++
 .../com/dashpig/farmstorm/util/CropProfile.java    | 65 ++++++++++++----------
 ...70\200\351\224\256\346\236\204\345\273\272.cmd" |  4 +-
 ...43\200\346\237\245\347\216\257\345\242\203.cmd" |  2 +-
 11 files changed, 105 insertions(+), 33 deletions(-)
```

未跟踪新增 1 个文件：`src/main/java/com/dashpig/farmstorm/util/CropTier.java`。
合计 **12 个路径 = 任务书 §1 的 10 个文件 + datagen 重新生成的 2 份 lang**，多一个都没有。

---

## 2. F-213 段 · 版本号 0.2.0 与两支脚本的产物名

### 2.1 三个文件的改动行

`git diff gradle.properties`（只有第 11 行）：
```
@@ -8,7 +8,7 @@ loader_version_range=[4,)
 mod_license=All Rights Reserved
-mod_version=0.1.1
+mod_version=0.2.0
 mod_group_id=com.dashpig.farmstorm
```

`git diff -- 一键构建.cmd 检查环境.cmd`：
```
-copy /y "build\libs\farmstorm-0.1.1+mc1.21.1.jar" "dist\" >nul
-copy /y "build\libs\farmstorm-0.1.1+mc1.21.1-sources.jar" "dist\" >nul
+copy /y "build\libs\farmstorm-0.2.0+mc1.21.1.jar" "dist\" >nul
+copy /y "build\libs\farmstorm-0.2.0+mc1.21.1-sources.jar" "dist\" >nul
...
-if exist "build\libs\farmstorm-0.1.1+mc1.21.1.jar" (echo jar: OK) else (echo jar: 缺失，先跑 一键构建.cmd)
+if exist "build\libs\farmstorm-0.2.0+mc1.21.1.jar" (echo jar: OK) else (echo jar: 缺失，先跑 一键构建.cmd)
```

### 2.2 build 退出码

命令：`cmd /c ""D:\project_farm\.tools\gw.cmd" build"` → `BUILD SUCCESSFUL in 16s` / **exit 0**。

### 2.3 两支 jar 的路径与字节数

```
farmstorm-0.2.0+mc1.21.1-sources.jar  981546 bytes
farmstorm-0.2.0+mc1.21.1.jar          1065894 bytes
```
`farmstorm-0.1.1*` / `farmstorm-0.1.0*` 残留 = **0**（改前基线留下的两支旧 jar 已作为构建产物清出 `build/libs`；`build/` 被 `.gitignore:2` 忽略，不影响任何受版本控制的文件）。

### 2.4 `jar tf` / mods.toml 的版本行

```
mods.toml: version = "0.2.0"
mods.toml: versionRange = "[21.1.0,)"
mods.toml: versionRange = "[1.21.1]"
```

### 2.5 两个 `grep -c` 的读数（改前 → 改后）

| 图案 | `一键构建.cmd` | `检查环境.cmd` |
|---|---|---|
| `farmstorm-0.1.1` | 2 → **0** | 1 → **0** |
| `farmstorm-0.2.0+mc1.21.1` | 0 → **2** | 0 → **1** |
| `0.1.1` @ `gradle.properties` | 1 → **0** | — |

### 2.6 banner 版本串补改（调度者拍板后追加）

初版按 §1 的行边界只改了产物名，banner（`:5`）保持 `Farmstorm v0.1.1` 未动，登记为未关闭问题 1。
调度者复核后**拍板补改**，理由：37 号 F-213 易漏点③「banner 不再写版本串」是**文档记错**——
v0.1.1 期 banner 已从 `Farmstorm v0.0.1` 改成 `Farmstorm v0.1.1`，所以它确实带版本串；
banner 写 0.1.1 而产物名写 0.2.0，正是 F-213 要消灭的那类脱钩。

改动（**只改版本串，其余 echo 文案一字不动**，含 `v0.1.1` 前后的双空格）：

```
一键构建.cmd:5  -echo   Farmstorm v0.1.1  -  一键构建
                +echo   Farmstorm v0.2.0  -  一键构建
检查环境.cmd:5  -echo   Farmstorm v0.1.1  -  验收自检
                +echo   Farmstorm v0.2.0  -  验收自检
```

读数（改前 → 改后）：

| 图案 | `一键构建.cmd` | `检查环境.cmd` |
|---|---|---|
| `Farmstorm v0.1.1` | 1 → **0** | 1 → **0** |
| `Farmstorm v0.2.0` | 0 → **1** | 0 → **1** |

`git status --porcelain` 仍是这两个文件（文件数不变，各自多 1 行改动）：
```
 M "\344\270\200\351\224\256\346\236\204\345\273\272.cmd"
 M "\346\243\200\346\237\245\347\216\257\345\242\203.cmd"
```
`git diff --stat` 这两个文件由 `4 +-` / `2 +-` 变为：
```
 "\344\270\200\351\224\256\346\236\204\345\273\272.cmd" | 6 +++---
 "\346\243\200\346\237\245\347\216\257\345\242\203.cmd" | 4 ++--
 2 files changed, 5 insertions(+), 5 deletions(-)
```
即 `一键构建.cmd` 3 行改动（`:5`/`:11`/`:12`）、`检查环境.cmd` 2 行改动（`:5`/`:14`）。
编码未变：两份仍 **UTF-8 无 BOM**、CRLF 行尾（21 / 15 个 CRLF）。
**未重跑 build**（只改 echo 文案，不影响产物，产物名与 `mods.toml` 版本行的读数见 §2.3 / §2.4 仍然成立）。

---

## 3. F-214 段 · 品质载体

### 3.1 `CropTier.java` 的枚举与四个方法签名

| 行 | 内容 |
|---|---|
| `:19` | `public enum CropTier implements StringRepresentable {` |
| `:21` | `COMMON(ChatFormatting.WHITE),` |
| `:23` | `RARE(ChatFormatting.BLUE),` |
| `:25` | `EPIC(ChatFormatting.GOLD);` |
| `:35` | `public String getSerializedName()` → `name().toLowerCase(Locale.ROOT)` |
| `:40-41` | `public String translationKey()` → `"tooltip.farmstorm.tier." + getSerializedName()` |
| `:45` | `public ChatFormatting color()` |
| `:53` | `public Component nameStyle(Component base)` → `base.copy().withStyle(this.color)` |

### 3.2 record 头改前 → 改后

```
-        int throwStack) {
+        int throwStack,
+        CropTier tier) {
```
第 9 字段位置在 `throwStack` **之后**（36 §2.1「本文确定」），record 头现落在 `:32-45`。

### 3.3 22 条档案的档位逐条点名（对照 34 §4.1）

| # | 档案 | 34 §4.1 档位 | 本次写入 | 落点 |
|---|---|---|---|---|
| 1 | `puffed_wheat` | 普通 | `CropTier.COMMON` | `:52` |
| 2 | `blast_carrot` | 普通 | `CropTier.COMMON` | `:54` |
| 3 | `ubi_server` | 稀有 | `CropTier.RARE` | `:57` |
| 4 | `toxic_ubi_server` | 稀有 | `CropTier.RARE` | `:59` |
| 5 | `power_beet` | 稀有 | `CropTier.RARE` | `:61` |
| 6 | `melon_slice_bomb` | 普通 | `CropTier.COMMON` | `:66` |
| 7 | `melon_grenade` | 普通 | `CropTier.COMMON` | `:68` |
| 8 | `pumpkin_turret` | 普通 | `CropTier.COMMON` | `:70` |
| 9 | `awakened_torchflower` | 史诗 | `CropTier.EPIC` | `:74` |
| 10 | `awakened_pitcher_plant` | 史诗 | `CropTier.EPIC` | `:78` |
| 11 | `fang_trap_cactus` | 普通 | `CropTier.COMMON` | `:80` |
| 12 | `bamboo_firecracker` | 普通 | `CropTier.COMMON` | `:83` |
| 13 | `red_mushroom_puff` | 普通 | `CropTier.COMMON` | `:85` |
| 14 | `brown_mushroom_puff` | 普通 | `CropTier.COMMON` | `:87` |
| 15 | `lucky_berry` | 史诗 | `CropTier.EPIC` | `:89` |
| 16 | `molotov` | 普通 | `CropTier.COMMON` | `:95` |
| 17 | `zangief_cane` | 稀有 | `CropTier.RARE` | `:100` |
| 18 | `wild_apple` | 史诗 | `CropTier.EPIC` | `:105` |
| 19 | `compressed_puffed_wheat` | 稀有 | `CropTier.RARE` | `:110` |
| 20 | `friend_chives` | 稀有 | `CropTier.RARE` | `:117` |
| 21 | `point_defense_fungus` | 稀有 | `CropTier.RARE` | `:126` |
| 22 | `vibration_bean` | 普通 | `CropTier.COMMON` | `:135` |

与 34 §4.1 逐条一致。8 条「种子 = 自身」的档案（`blast_carrot` / `ubi_server` / `toxic_ubi_server` / `fang_trap_cactus` / `bamboo_firecracker` / `red_mushroom_puff` / `brown_mushroom_puff` / `lucky_berry`）的 `seedItem` **一个未动**（diff 里只有追加的 tier 实参）。

### 3.4 三个工厂的形参变更

| 工厂 | 行 | 变更 |
|---|---|---|
| `crop` | `:198` | `+ CropTier tier`（在 `seedItemPath` 后、`CropCategory...` 前），转发到 record |
| `throwable` | `:204` | 同上 |
| `deployed` | `:210` | `+ CropTier tier`（在 `DeployedKind kind` 后、`Set<CropCategory>` 前），转发到 record |

### 3.5 10 处 `new CropProfile(` 的位置

`new CropProfile(` 改前 **10** / 改后 **10**（不增不减）：
- 7 处直接构造（实参表末尾均带 `CropTier.X`）：`melon_slice_bomb`、`molotov`、`zangief_cane`、`wild_apple`、`compressed_puffed_wheat`、`point_defense_fungus`、`vibration_bean`；
- 3 处工厂内部（转发 `tier` 形参）：`crop` / `throwable` / `deployed`。

### 3.6 逐档点数

```
CropTier.COMMON in CropProfile.java: 11
CropTier.RARE   in CropProfile.java: 7
CropTier.EPIC   in CropProfile.java: 4
```
（枚举声明在 `CropTier.java`、工厂形参写的是裸类型 `CropTier tier`，两者均不计入。）

### 3.7 品质自检行原文（`run-p32/logs/latest.log`）

```
[modloading-worker-0/INFO] [farmstorm/]: 自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK
[modloading-worker-0/INFO] [farmstorm/]: 自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK
```
`Farmstorm.java:130-132`：`boolean tierOk = common == 11 && rare == 7 && epic == 4;` + `LOGGER.info("自检 作物品质 普通 {} / 稀有 {} / 史诗 {} → {}", ...)`。
`Farmstorm.java:112-113`（现 `:112-113` 的 `22 / 12 / 7 / 3 / 2`）五个数**一个未动**，`git diff` 里没有它们的删除行。

---

## 4. F-215 段 · 品质视觉

### 4.1 三个类的 `getName` 覆写（文件:行号）

| 类 | 覆盖物品数 | `getName(ItemStack)` | 实现 |
|---|---|---|---|
| `item/ModifiedCropItem.java` | **15**（`ThrowableCropItem` 12 + `FoodCropItem` 3） | `:71-73` | `return this.profile.tier().nameStyle(super.getName(stack));` |
| `item/DeployableCropItem.java` | **7** | `:41-43` | 同上（本类 `extends BlockItem`，**不继承** `ModifiedCropItem`，故独立覆写） |
| `item/SeedItem.java` | **7** | `:38-40` | 同上（`extends ItemNameBlockItem`，另立一支） |

`15 + 7 + 7 = 29` 的构造面实测（`registry/FarmstormItems.java`）：
`new ThrowableCropItem` 12 + `new FoodCropItem` 3 + `new DeployableCropItem` 7 + `seedItem(...)` 7（7 处调用点，每处产出一个 `SeedItem`）= **29**。

### 4.2 四色映射

`CropTier.java:21/23/25` → `COMMON = ChatFormatting.WHITE` / `RARE = ChatFormatting.BLUE` / `EPIC = ChatFormatting.GOLD`；
`nameStyle` 在 `CropTier.java:53-55`。**未复用原版 `Rarity`**：`grep -rn "Rarity" src/main/java` = 0。

### 4.3 tooltip 追加点

`ModifiedCropItem.java:126` `appendCropTooltip(@Nullable CropProfile profile, List<Component> tips)`：
- `:139` 原有的类别行 `tips.add(line.withStyle(ChatFormatting.GRAY));`
- `:140-144` **新增**品质行（在类别行**之后**、另起一行、不与类别行合并）：
  ```java
  tips.add(Component.translatable(TIER_KEY,
          Component.translatable(profile.tier().translationKey())).withStyle(ChatFormatting.GRAY));
  ```
- `TIER_KEY = "tooltip.farmstorm.tier"` 在 `:36`。

3 个调用点覆盖 29 个物品：`ModifiedCropItem.java:59`、`DeployableCropItem.java:33`、`SeedItem.java:30`。

### 4.4 4 条键的键名与落点

| 键 | zh | en | 落点 |
|---|---|---|---|
| `tooltip.farmstorm.tier.common` | 普通 | Common | `ModLangProvider.java:195` |
| `tooltip.farmstorm.tier.rare` | 稀有 | Rare | `:196` |
| `tooltip.farmstorm.tier.epic` | 史诗 | Epic | `:197` |
| `tooltip.farmstorm.tier` | 品质：%s | Tier: %s | `:198` |

### 4.5 两份 lang 的键数与 `tooltip.farmstorm` 前缀读数

```
改前: zh 323 / en 323，tooltip.farmstorm 各 16，tooltip.farmstorm.tier* 各 0
改后: zh 327 / en 327，tooltip.farmstorm 各 20，zh-only 0 / en-only 0
```
**「16 → 20」是本节唯一允许写的前缀读数**；327 只是本轮窗口内的实测观察值，**不作为断言**——
语言键总数 **337 × 2 只在 F-241 之后**成立（HC-98 / 39 号 §0.4）。
jar 内实测：`assets/farmstorm/lang/zh_cn.json` 327 键 / `tooltip.farmstorm` 20；`en_us.json` 同。

### 4.6 `ModLangProvider.java:13-16` 未改的证据

该文件在整个 Phase 内只有 **1 个 diff hunk**，位置在 `:194`（新增 5 行），类注释区完全无 hunk：
```
@@ -193,0 +194,5 @@ public final class ModLangProvider extends LanguageProvider {
+        // ---- 品质三档 + 1 条行格式（D-150 / D-151）；键名体例照 CropCategory.translationKey() ----
+        plain(entries, "tooltip.farmstorm.tier.common", "普通", "Common");
+        plain(entries, "tooltip.farmstorm.tier.rare", "稀有", "Rare");
+        plain(entries, "tooltip.farmstorm.tier.epic", "史诗", "Epic");
+        plain(entries, "tooltip.farmstorm.tier", "品质：%s", "Tier: %s");
```
`:14-16` 仍写着「32 item + 26 block + … + 169 config + … = **323**」——**一字未动**，总数留给 F-241。

---

## 5. 负向面

### 5.1 `textures/item` 29 张 sha256 清单（动手前 / 收尾后对照）

```
before lines= 29   after lines= 29   IDENTICAL= True
```
两份完整清单：`.work/p32-tex-before.txt`（动手前）、`.work/p32-tex-after.txt`（收尾后），逐行 sha256 + 文件名完全一致。

### 5.2 四条冻结面的 diff

```
registry/                : 0 行
config/FarmstormConfig.java : 0 行
src/main/resources/assets/farmstorm/textures : 空
src/main/resources/assets/farmstorm/models/item : 空
docs/                    : 空
registry/FarmstormEntityTypes.java : 空
```

### 5.3 注册面（本节未开窗，必须仍是 93 / menu_types 1）

```
自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 /
sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 /
data_components 1 / menu_types 1 → OK
```
12 类分项与 0.1.1 基线**逐项相同**，`menu_types` 仍 **1**（`data_components` 仍 **1**，未新增数据组件）。
网络包仍是 `farmstorm:core_list`（版本 1，S2C 只读，MAX_CORES 64）——**1 个**，第 2 个在 F-238。

---

## 6. 文档-源码偏差表

| # | 文档写的 | 实测 | 我按哪条做 |
|---|---|---|---|
| 1 | 任务书 §6 / 37 号 F-213：「两支脚本的 banner 不写版本串（0.1.1 期已统一）」 | `一键构建.cmd:5` 与 `检查环境.cmd:5` **确有** `echo   Farmstorm v0.1.1  -  …` | 初版按 §1 行边界（`:11`/`:12` 与 `:14`）只改产物名、banner 未动；**调度者已拍板补改**，两处 `Farmstorm v0.1.1` → `v0.2.0`（见 §2.6） |
| 2 | 36 §2.1「构造面 = 10 处构造点 + 15 处工厂实参」 | `new CropProfile(` 改前改后各 **10**（7 直接 + 3 工厂）✓；工厂实参 15 处 ✓ | 一致，无需适配 |
| 3 | 36 §1.3 / 事实卡 4：`textures/item` **29** 张、`tooltip.farmstorm` **16** | 实测 29 / 16 ✓ | 一致 |
| 4 | 39 §1.1 前置称工作树干净 | 实测 `git status` 有大量 `.work/` 未跟踪文件与 1 个 `.work/p31-cmd-ALICE2.txt` 之类历史残留（均不在 `src/`、`docs/` 下） | 视为既有工作区状态；本 Phase 的受控改动面仍严格等于 12 个路径 |
| 5 | 36 §4.3.3 称 `ModLangProvider` 注释显示 `16 tooltip` | `ModLangProvider.java:15` 写的正是 `+ 16 tooltip` ✓ | 一致；本轮改成 20，留 F-241 同步注释 |

---

## 7. 未验证清单

| # | 项 | 为什么本机做不了 |
|---|---|---|
| 1 | 三档名色（普通白 / 稀有蓝 / 史诗金）在背包、tooltip、铁砧、告示牌、聊天栏的实际呈现 | 需要图形客户端与人眼确认；本次只起 dedicated server，未起 `runClient` |
| 2 | tooltip 恰好多 1 行「品质：普通/稀有/史诗」的视觉版式 | 同上；代码层证据见 §4.3 |
| 3 | 铁砧改名（`CUSTOM_NAME`）后档色是否保留 | 36 §9 ⑥ 明确登记为待验证项，归属 F-244，本任务只取证不改实现 |
| 4 | 29 个物品逐个在背包里确认变色 | 需要客户端 GUI；代码层已证 3 个类分别覆写 `getName` 且构造面 15+7+7=29 |

---

## 8. 未关闭的问题

1. ~~**两支 `.cmd` 的 banner 仍写 `v0.1.1`**~~ → **已关闭**。调度者拍板补改，`Farmstorm v0.1.1` → `Farmstorm v0.2.0`，读数与证据见 §2.6。同时确认 37 号 F-213 易漏点③「banner 不再写版本串」是**文档记错**（v0.1.1 期 banner 已带版本串），已记入偏差表第 1 行。
2. **`自检 幸运浆果池 19 种 … → OK` 仍是 19**（不是 HC-98 的 11）。这是 **F-216** 的交付物（D-156 / A-63 / RT-50），本 Phase 未越界改动，该行当前为绿是因为断言常量也是 19。
3. `build/libs` 里改前遗留的两支 `farmstorm-0.1.1+mc1.21.1*.jar` 已被清出以满 **残留 0** 的判据；`build/` 由 `.gitignore:2` 忽略，不进版本控制，可在下次构建时重新生成。

---

## 9. 起服取证方式（可复现）

```
New-Item -ItemType Directory -Path run-p32
Copy-Item run\eula.txt, run\server.properties run-p32\
$env:FS_RUN_LOCK='run-p32'
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-p32"
```
`run-p32` 由 `.gitignore:4 run-*/` 忽略；`run/logs/latest.log` 的 mtime 仍是 23:43:50（runData 时刻），
证明**没有在 `run/` 里起服**。停服走 rcon（`run-p32` 的 `server.properties` 已带 `enable-rcon=true` /
`rcon.port=25575` / `rcon.password=fsverify`）：`stop` → `Stopping the server` → `Thread RCON Listener stopped`，之后 java 进程数 **0**。
