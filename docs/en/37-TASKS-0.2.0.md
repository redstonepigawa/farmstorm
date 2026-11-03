# Document 37 · v0.2.0 Task Breakdown

> Basis: document 34 (25 decisions + 19 rulings) / document 35 (11 requirements + 40 boundaries) / document 36 (97 registry objects / 46 action-surface rows / 175 config keys / 36 acceptance entries), tasks from F-213, Phases from 32, milestones from M28, adjudications from A-62
>
> This document breaks the three specs 34 / 35 / 36 into **34 tasks that can each be "committed and verified independently"**, numbered **F-213 ~ F-246**, with Phase **32 ~ 39** mapping one-to-one onto milestones **M28 ~ M35**, and adjudications **A-62 ~ A-69** (8 entries; A-62 is named by item 7 of document 36 §10, A-63 ~ A-69 are newly established while this document was written).
> Coverage: the **46 action-surface rows** of document 36 §3 (table A, 46/46), the registry surface **93 + 4 = 97** (table B), config **166 − 1 + 10 = 175 keys / 58 + 2 = 60 groups** (table C), language keys **323 − 3 + 17 = 337** (table D), risks **RT-42 ~ RT-50, nine entries** (table E), APIs to verify **13 entries** (table F); 0 uncovered.
> This document only reads upstream: it does not modify `docs/zh/34` ~ `docs/zh/36`, does not modify `docs/en`, does not modify `src/`, and does not modify the three ledgers of `.work/`; it only creates this file.

---

## 0. Reading Conventions

### 0.1 Task Granularity (the five items carried over from 0.1.1)

One task = one change that can be committed and verified independently. The five criteria follow document 31 §0.1 (whose source is document 25 §0.1); this version does not rewrite them, only restates them:

1. **One task = one change that can be committed and verified independently.**
2. **After the commit, `.tools\gw.cmd build` exits with code 0**, and the task does not depend on a task of the same batch that is not yet committed.
3. **Each task carries at least one observable assertion**, and the assertion must contain a concrete value, key name or path.
4. **Reverting that commit does not invalidate other completed tasks** (it affects only later tasks that depend on it; the dependency is written in the "Depends on" column of §1's table and on the task's title line).
5. **No Phase boundary is crossed**: all deliverables of a task fall inside the name scope of the Phase it belongs to.

Three hard conventions **specific to this version** (written here, not repeated in the tasks):

- **A-62**: the field declaration at `FarmstormConfig.java:227` is **kept**; only the definition at `:909-912` is commented out. No task may modify the two classes `FarmlandLedger` / `FarmlandGuard` (not a single line), nor turn that key into a hard-coded constant.
- **A-63**: the Lucky Berry pool's 11 entries **are derived from `tier()`** and may not be written as a second hand-written id list.
- **A-69**: `farmstorm_farmland.dat`'s "no reads, no writes" holds only **after F-233** — the `.dat` file's last runtime reader is `settleOnRemoval` (`FarmlandLedger.of` at `OutpostCoreBlockEntity.java:353`), and it is rewritten whole only by F-233. Between F-220 and F-233 a core teardown still reads and rewrites that file, so no task may write "the `.dat` mtime is unchanged" as an acceptance assertion before F-233.

### 0.2 Complexity and Workload Conversion (S/M/L → 0.5/1.5/3.0)

The conversion basis is exactly that of document 31 §0.2 (whose source is document 25 §0.2) and is not adjusted in this version:

| Complexity | Meaning | Converted person-days |
|---|---|---|
| S | Single file or pure data; ≤ 1 class, or literal replacement in ≤ 3 files | **0.5** |
| M | 2 ~ 4 files, across 1 ~ 2 packages, or 1 new complete behaviour implementation | **1.5** |
| L | ≥ 5 files or across 3 packages, including a runtime state machine / serialization / container contract / render pipeline / cross-class interface rework | **3.0** |

Distributed under this rule, this version's 34 tasks are **S 11 / M 9 / L 14**, converting to `11 × 0.5 + 9 × 1.5 + 14 × 3.0 = 5.5 + 13.5 + 42.0 = **61.0 person-days**` (buffer excluded).

Four notes on items **not estimated as "changing one line"**:

1. **Any "first time in the project" counts one tier up**: F-223 (the first villager-trade code, 0 hits for `villager` / `trade` across the repository), F-225 (the mod's first `*.png.mcmeta`), F-226 (the first "non-container BE upgraded into a container"), F-238 / F-240 (drawing lines in world space for the first time) all count as L, not 0.5 for "adding one class".
2. **Any record-contract change counts as L**: F-214's actual diff is small, but it is a one-off contract change over 22 profiles + 3 private factories (document 36 §2.1 records it as "10 construction sites + 15 factory arguments"), and it **cannot be split** (see F-214's easy-to-miss point ①).
3. **Any container / menu contract rework counts as L**: neither F-226's 7 `Container` methods plus NBT compatibility, nor F-227's 54 + 36 slot layout with `quickMoveStack`, is work that "copying the 216 lines" would finish.
4. **Any acceptance task is not estimated as "running it once"**: F-244 covers 36 entries and F-245 covers 40, each entry needing at least one setup + one reading + one record, so both count as L.

### 0.3 The **in-between window** (this version's 6th granularity rule)

**Fact**: the registry self-check has **5 constants** at `Farmstorm.java:147-150` — `total == 93`, `items == 32`, `blocks == 26`, `blockEntities == 7`, `menuTypes == 1` (the other 8 classes each have a constant too, unchanged in this version). This version's registry surface grows by a net 4 items, and those 4 land **in two batches** (the cultivator trio in one, the core menu in the other), while the assertions can only be changed to match at the very end ⇒ a window necessarily exists in which "the actual count has already changed while the assertion constant is still 93".

**Window = F-224 (opens) → F-228 (closes)**. Four segments of state (each readable verbatim from `run/logs/latest.log`):

| Segment | Task boundary | Actual `total` | 12-class breakdown | Self-check line suffix | Notes |
|---|---|---|---|---|---|
| Before the window | F-213 ~ F-223 | **93** | same as 0.1.1 | `→ OK` | these 11 tasks may assert OK normally |
| State A | after F-224 lands | **96** | `items 33 / blocks 27 / block_entities 8 / menu_types 1` | `→ FAIL` | the cultivator trio +3 |
| State B | after F-227 lands | **97** | `… / menu_types 2` | `→ FAIL` | the menu +1; **the numbers are all right, but the assertion constant is still 93** |
| Window closed | after F-228 lands | **97** | as above | `→ OK` | the 5 constants changed together |

**Four window disciplines** (they apply to every task from F-224 to F-227; F-228 is the closing task, so item 1 does not apply to it):

1. **Do not** write "`自检 注册对象 … → OK`" as an acceptance assertion. Inside the window only the **actual breakdown numbers in the FAIL line** may be asserted (state A writes 96 and the four items, state B writes 97 and `menu_types 2`), and "FAIL is the correct value during the window" goes into the task body. The only task allowed to write OK is the closing task F-228 — what it writes is "after the change this line turns OK".
2. Inside the window **the other self-check lines must stay green**: `自检 作物 22 种…`, `自检 作物品质 普通 11 / 稀有 7 / 史诗 4`, `自检 幸运浆果池 11 种…`, `自检 网络包注册` (two lines after F-238). These four are the window's **negative criteria** — if any one of them goes red it is a real fault, not the window.
3. **The closing task F-228 changes the 5 numbers together in one commit** (`93 → 97`, `32 → 33`, `26 → 27`, `7 → 8`, `1 → 2`), starts the server once to check the log, and **carries no other change** (`git diff` touches only those 5 numbers and that method comment).
4. **There is more than one window, and config's totals have to be split into two things**: the language-key total (323 in the comment at `ModLangProvider.java:13-16`) changes only in the closing task, the window being **F-215 → F-241**; intermediate tasks **never touch that comment** (otherwise every task would have to recompute the comment, and one task would necessarily get it wrong), the criterion writes only "this key exists / does not exist", and the total is asserted only in F-241. On the config side, **the interval readings of `run/config/farmstorm-server.toml` (171 / 172 / 173 / 175 keys) may be asserted in intermediate tasks** — they are measured values available from one server start and change as soon as config changes; whereas **the group count of `b.push(` in `FarmstormConfig.java` and the total key count written in the source comment are asserted only when F-242 closes** — those are texts that need manual syncing.

**Why the window closes at F-228 rather than later**: F-228 is "the first thing after the last registry increment (F-227)". Pulling the closing forward lets the 18 tasks from F-229 on cite `→ OK` freely; pushing it to Phase 39 would give every server start in Phase 36 ~ 38 a permanently red line of noise (RT-50's corrected wording: the assertion only writes a log line and does not throw, so red ≠ crash, but real faults would be buried under the noise).

### 0.4 One Task = One Commit

1. **One task, one commit**; the commit message starts with the task number (`F-224: …`); the commit content lines up with the task's "Files touched" item by item — one extra file means out of scope.
2. **No drive-by fixes of existing blemishes**: document 34 §8 registers 8 of them (`CropType.java:17,19,21`'s 8/5/2, `SeedItem.java:13`'s "6", `FarmstormBlocks.java:25`'s "14", `LuckyBerryRoller.java:170`'s "14-entry pool", etc.) and this version **changes none of them**; they appear in "Easy-to-miss points" to mean "do not fix them in passing", not as to-dos.
3. **Rollback granularity = commit granularity**: every task's acceptance assertions must still describe the system after **reverting only that one commit** (without relying on another commit of the same batch).
4. **Do not write "call sites = N" as a criterion**: always replace it with a file count / key count / hit count / log line / save field (the same spirit as A-37, document 31 §0.1).

---

## 1. Task Overview

Columns: task number / title / Phase / complexity / person-days / depends on / deliverables / decisions / covered action-surface row numbers of document 36 (`表外` = not among the 46 rows of document 36 §3, the reason being in the adjudications of §5).

| Task | Title | Phase | Complexity | Person-days | Depends on | Deliverables | Decisions | Action-surface rows |
|---|---|---|---|---|---|---|---|---|
| F-213 | Version 0.2.0 and the artifact names in the two scripts | 32 | S | 0.5 | — | `gradle.properties`, the two `.cmd` scripts | D-148, A-65 | 表外 |
| F-214 | The tier carrier: `CropTier` + `CropProfile`'s 9th field + 22 profiles | 32 | L | 3.0 | — | new `util/CropTier.java`, `util/CropProfile.java`, the tier self-check line in `Farmstorm.java` | D-149, D-150 | #6, #7, #8 |
| F-215 | Tier visuals: name color + one tooltip line + 4 language keys | 32 | M | 1.5 | F-214 | 3 item classes + `ModLangProvider` | D-151 | #10, #11, #12, #13 |
| F-216 | The Lucky Berry pool at 11 and the assertion in the same commit | 33 | S | 0.5 | F-214 | `util/CropProfile.java`, `Farmstorm.java` | D-156, A-63, RT-50 | #9, #46 (the pool half) |
| F-217 | Planting path ①: `isPlantableSoil` constantly `false` | 33 | S | 0.5 | — | `item/ModifiedCropItem.java` | D-152 | #14, #16 |
| F-218 | Planting path ②: `SeedItem#useOn` returns `PASS` | 33 | S | 0.5 | — | `item/SeedItem.java` | D-152 | #15 |
| F-219 | The converter's `useWithoutItem` returns `PASS` | 33 | S | 0.5 | — | `block/ConverterBlock.java` | D-170 | #44 |
| F-220 | Retire the farmland system: 5 comment-outs + the key definition + the field kept | 34 | M | 1.5 | — | `event/FarmstormGameEvents.java`, `config/FarmstormConfig.java` | D-153, D-154, A-62, RT-42 | #17~#22, #25, #26 |
| F-221 | 3 farmland language keys retired | 34 | S | 0.5 | F-220 | `datagen/ModLangProvider.java` | D-153 | #23, #24 |
| F-222 | The `trade` group's 6 keys + 6 config language keys | 34 | M | 1.5 | F-220 | `config/FarmstormConfig.java`, `ModLangProvider` | D-159 | #45 (+6) |
| F-223 | 3 farmer trades: `VillagerTradeEvents` + `TierTrade` | 34 | L | 3.0 | F-222 | 2 new classes under `event/`, the hook in `Farmstorm.java` | D-157, D-158, D-159, RT-46 | #27, #28, #29 |
| F-224 | The cultivator registration trio + the creative tab | 35 | L | 3.0 | — | new `block/`, `blockentity/` 2 classes + 3 registries + the creative tab | D-160, D-171, A-64 | #1, #2, #3, #30, #31 |
| F-225 | Cultivator resources, the two models and three datagen places | 35 | L | 3.0 | F-224 | 7 resource files + 3 datagen providers | D-160, D-161, RT-47 | 表外 (document 36 §5) |
| F-226 | The core becomes a container: 54 slots + `Items` + two sync methods | 35 | L | 3.0 | — | `blockentity/OutpostCoreBlockEntity.java`, config, lang | D-166, RT-44 | #33, #34, #35, #36, #45 (+1) |
| F-227 | The core menu + screen + `menu_types` +1 | 35 | L | 3.0 | F-226 | new `menu/`, `client/` 2 classes + 1 registry + the screen registration | D-166, D-171 | #4, #40 |
| F-228 | Registry self-check closes at 97 (the window closes) | 35 | S | 0.5 | F-224, F-227 | `Farmstorm.java:147-150` and the method comment | D-171 | #5, #46 (the registry half) |
| F-229 | Opening the core's storage: two gates | 36 | M | 1.5 | F-227 | `block/OutpostCoreBlock.java`, the menu's `stillValid` | D-166, author's ruling #17 | #38 |
| F-230 | Core destruction drops | 36 | S | 0.5 | F-226 | `block/OutpostCoreBlock.java:101-107` | D-167 | #39 |
| F-231 | The core's 16-entry binding list + `cultivatorCapacity` | 36 | M | 1.5 | F-226 | `OutpostCoreBlockEntity`, config, lang | D-165 | #34 (`Cultivators`), #45 (+1) |
| F-232 | Binding on placement: four criteria + writes on both sides | 36 | L | 3.0 | F-224, F-231 | `block/CultivatorBlock.java`, `CultivatorBlockEntity`, the index query | D-165 | #30, #31 (the cache) |
| F-233 | Migration's three exits: `settleOnRemoval` rewritten whole | 36 | L | 3.0 | F-232 | `blockentity/OutpostCoreBlockEntity.java:341-394` | D-168, RT-43, A-69 | #37 |
| F-234 | The `cultivator` group's 2 keys + 2 language keys | 37 | S | 0.5 | F-224 | `config/FarmstormConfig.java`, `ModLangProvider` | D-163 | #32 (the config face), #45 (+2) |
| F-235 | The state machine, the four admission gates and insertion | 37 | L | 3.0 | F-232, F-234 | `CultivatorBlockEntity`, `CultivatorBlock` | D-160, D-162, D-164 | #30 (admission), #31 (the state machine) |
| F-236 | Output and pausing when the core is full | 37 | L | 3.0 | F-226, F-235 | `CultivatorBlockEntity`, `OutpostCoreBlockEntity`'s whole-batch insert | D-163, D-164 | #32 |
| F-237 | Cultivator destruction drops | 37 | S | 0.5 | F-224 | `block/CultivatorBlock.java`'s `onRemove` | D-160 supplement | #31 (drops) |
| F-238 | The link packet + registration + protocol version 2 | 38 | M | 1.5 | — | new `network/CultivatorLinkPayload.java`, `FarmstormNetwork` | D-169, D-172, RT-45 | #41, #43 |
| F-239 | The link broadcaster: range and five timings | 38 | L | 3.0 | F-233, F-238 | new `network/CultivatorLinkBroadcaster.java` + the request points | D-169 | #42 (broadcaster) |
| F-240 | Client read-only state and wireframe rendering | 38 | L | 3.0 | F-238 | new `client/ClientCultivatorLinks.java`, `client/CultivatorLinkRenderer.java` | D-169, RT-45 | #42 (the client's 2 classes) |
| F-241 | The language-key ledger at 337 and the class comment | 39 | M | 1.5 | F-215, F-221, F-222, F-224, F-226, F-227, F-231, F-234 | the two lang files + the `ModLangProvider` comment | document 36 §4.3 | 表外 (document 36 §4.3) |
| F-242 | The config ledger 175 keys / 60 groups | 39 | M | 1.5 | F-220, F-222, F-226, F-231, F-234 | `run/config/farmstorm-server.toml` (regenerated) + a group-by-group check | document 36 §4.1 | #45 (the ledger) |
| F-243 | The verification tool synced to 97 / 175 / 337 | 39 | S | 0.5 | F-228, F-241, F-242 | `tools/verify_project.py` | A-66 | 表外 |
| F-244 | Document 36 §8's 36 acceptance entries | 39 | L | 3.0 | F-215 ~ F-240 (see §3) | the acceptance record `.work/v020-verify/acceptance.md` | document 36 §8 | — (the acceptance face) |
| F-245 | Document 35 §12's 40 boundary re-runs | 39 | L | 3.0 | F-244 | the boundary record `.work/v020-verify/boundaries.md` | document 35 §12 | — (the boundary face) |
| F-246 | Red line, registry surface and upstream-consistency sign-off | 39 | M | 1.5 | F-243, F-244, F-245 | the sign-off record `.work/v020-verify/signoff.md` | D-172, A-62~A-69 | — (the closing face) |

Task count: `246 − 213 + 1 = 34`, contiguous with no gap; 8 Phases; 8 milestones.

---

## 2. Task Details, Phase 32 ~ 39

Grouped by Phase. Each task has exactly six items: **Goal / Files touched (with line numbers) / Concrete changes / Implementation notes / Easy-to-miss points / Acceptance assertions**; the dependency is on the task's title line (`Depends on: —` means none).

### Phase 32 · Baseline and the Tier Carrier (F-213 ~ F-215)

Phase goal: nail down the two things "version number" and "tier" — the former is the name source of every artifact, the latter the **single data source** for the three places trade / cultivator admission / Lucky Berry pool (RT-49). Exit criterion: `farmstorm-0.2.0+mc1.21.1.jar` is produced, the startup log shows `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`, and the 29 item names are colored by tier with 0 texture changes.

#### F-213 · Version 0.2.0 and the artifact names in the two scripts (S · 0.5 person-days | Depends on: —)

- **Goal**: change `mod_version` to 0.2.0 and let the jar names hard-coded in the two `.cmd` scripts follow, so that the copy step of `一键构建.cmd` and the existence check of `检查环境.cmd` still point at real artifacts (D-148; the script lines are not among the 46 rows of document 36 §3, see A-65).
- **Files touched**: `gradle.properties:11`; `一键构建.cmd:11-12`; `检查环境.cmd:14`.
- **Concrete changes**: `mod_version=0.1.1` → `mod_version=0.2.0`; the `0.1.1` inside the source names at `一键构建.cmd:11` and `:12` (including the `-sources.jar` line) → `0.2.0`; the `if exist` path at `检查环境.cmd:14` replaced the same way. The three lines `minecraft_version` / `neo_version` / toolchain are not changed by a single character (document 36 §1.1).
- **Implementation notes**: the jar name is assembled from `mod_version`, so only a real build after the change can confirm the new name; the two scripts are **carriers of the artifact face**, not the source of the version number (the version number has only one place, `gradle.properties:11`, D-148 supplement).
- **Easy-to-miss points**: ① the `-sources.jar` line at `:12` is the easiest to leave with only the main jar changed; ② the prompt text at `检查环境.cmd:14` ("先跑 一键构建.cmd") is bound to the same path, and missing it makes the environment check report "jar: 缺失"; ③ the banners of the two scripts no longer carry a version string (unified in the 0.1.1 cycle), so do not add it in passing in this task.
- **Acceptance assertions**:
  1. `.tools\gw.cmd build` exits with code 0; `build/libs/` holds `farmstorm-0.2.0+mc1.21.1.jar` and `farmstorm-0.2.0+mc1.21.1-sources.jar`, with no `farmstorm-0.1.1*` leftover.
  2. The hit count of `farmstorm-0.1.1` in the two scripts = **0 / 0**; the hit count of `farmstorm-0.2.0+mc1.21.1` = **2** (`一键构建.cmd`) / **1** (`检查环境.cmd`).
  3. `jar tf build/libs/farmstorm-0.2.0+mc1.21.1.jar` contains `META-INF/neoforge.mods.toml`, whose `version` line parses as `0.2.0`.

#### F-214 · The tier carrier: `CropTier` + `CropProfile`'s 9th field + 22 profiles (L · 3.0 person-days | Depends on: —)

- **Goal**: each of the 22 crop profiles gets **exactly one tier** (Common 11 / Rare 7 / Epic 4, document 34 §4.1), carried by `CropProfile`'s 9th field plus a new enum (D-150); **the 7 direct `new CropProfile(...)` sites and the 15 factory arguments must all change in one go**, or it does not compile.
- **Files touched**: new `util/CropTier.java`; `util/CropProfile.java:32-40` (the record header), `:47-128` (the 22 profiles, of which the 7 direct constructions are at `:58`, `:87`, `:92`, `:97`, `:102`, `:117`, `:125`), `:191-207` (the three private factories `crop` / `throwable` / `deployed`); the existing self-check block between `Farmstorm.java:112-123` (one tier self-check line added, document 36 §8 #4).
- **Concrete changes**: append `CropTier tier` to the end of the record (after `throwStack`, the reason being document 36 §2.1's); `CropTier` holds `COMMON` / `RARE` / `EPIC`, `translationKey()`, `color()`, `nameStyle()` (items 1~3 of document 36 §2.1's "[this document's decision]"); the 22 profiles get their values one by one per document 34 §4.1; each of the three factories gains one `CropTier` parameter and forwards it; the startup self-check gains one line `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`.
- **Implementation notes**: the tier is a **profile-level static property** — no data component is added, no item NBT is written, no object is registered (`data_components` stays 1, D-150); the 22 profiles define the tier in this one place only, and trade / admission / the pool all read the same `tier()` (the mitigation (a) of document 36 §7 RT-49).
- **Easy-to-miss points**: ① the 7 direct constructions must all change **in the same commit** (miss one and the record's parameter count is wrong, so compilation fails) — this is the only reason the task cannot be split; ② the 8 profiles whose "seed = itself" (`blast_carrot`, `ubi_server`, `toxic_ubi_server`, `fang_trap_cactus`, `bamboo_firecracker`, `red_mushroom_puff`, `brown_mushroom_puff`, `lucky_berry`) must **not** have `seedItem` changed in passing when the tier is added; it still points at itself; ③ none of the five existing numbers `22 / 12 / 7 / 3 / 2` at `Farmstorm.java:112-113` moves; ④ `compressed_puffed_wheat` counting as Rare while its ingredient `puffed_wheat` counts as Common is the author's wording (document 34 D-149 supplement), not a typo.
- **Acceptance assertions**:
  1. `new CropProfile(` occurs exactly **7** times in `CropProfile.java`, and every one of them ends its argument list with a `CropTier.` argument.
  2. The startup log shows `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` (document 36 §8 #4).
  3. Counting the profile block by tier: `CropTier.COMMON` **11** times, `CropTier.RARE` **7** times, `CropTier.EPIC` **4** times (the enum declaration and the factory parameters are not counted).
  4. The line `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK` is unchanged (an existing assertion is not altered by accident).

#### F-215 · Tier visuals: name color + one tooltip line + 4 language keys (M · 1.5 person-days | Depends on: F-214)

- **Goal**: the names of the 29 items (22 crops + 7 seeds) are colored by tier (Common white / Rare blue / Epic gold), and the tooltip gains **one more line** of tier description after the existing category line (D-151); textures / models / inventory size do not move at all.
- **Files touched**: `item/ModifiedCropItem.java:57-60` (the existing `appendHoverText`, the landing point of the name-color override), `:113-127` (`appendCropTooltip`, the only implementation of the tier line); `item/DeployableCropItem.java:18` (**does not extend** `ModifiedCropItem`, so it must override independently), `:33` (the tooltip call site); `item/SeedItem.java:16` (`extends ItemNameBlockItem`), `:28-31`; the tooltip section of `datagen/ModLangProvider.java` (4 entries added, in the same section as `tooltip.farmstorm.category.*`).
- **Concrete changes**: each of the three classes overrides `getName(ItemStack)` through `tier().nameStyle(...)` (document 36 §2.1's name-color table: `WHITE` / `BLUE` / `GOLD`); `appendCropTooltip` appends **at its end** one line `tooltip.farmstorm.tier` (the shell grey, the tier-name sub-component carrying its own color, document 36 §2.1); 4 keys are added: `tooltip.farmstorm.tier.common` / `.rare` / `.epic` + `tooltip.farmstorm.tier` (rows 4~7 of document 36 §4.3.2).
- **Implementation notes**: the name color must hang on **the name `Component` itself** (one override, and anvil / sign / chat / container preview all show the same color), not be drawn a second time in a GUI (D-151 supplement); the tooltip gains **only one line** and is not merged with the category line (that is another set of semantics); the 22 crop textures and 7 seed textures do not change (document 36 §5's negative list).
- **Easy-to-miss points**: ① `DeployableCropItem` does not extend `ModifiedCropItem`, so changing only the parent class misses **7** deployable items; ② `SeedItem` must be overridden separately too (7 of them); ③ the 4 keys are "3 tier names + 1 line format", neither 1 nor 3; ④ renaming an item in an anvil goes through `CUSTOM_NAME` and the name color may be lost — that is the item to verify of document 36 §9 ⑥, its verification and fallback being in F-244 (this task does not change the implementation).
- **Acceptance assertions**:
  1. The two language files each gain 4 `tooltip.farmstorm.tier*` entries, and the `tooltip.farmstorm` prefix goes from 16 entries to **20**.
  2. In game: Common crop names are white, Rare blue, Epic gold; the tooltip has one more line after the category line, "品质：稀有".
  3. `textures/item` still holds **29** PNGs, and each one's sha256 is identical to the pre-commit value (document 36 §5's negative list).

### Phase 33 · Narrowing the Rule Surface (F-216 ~ F-219)

Phase goal: land the 4 "player-visible rule changes" each on its own — the berry pool, the two planting paths, the converter. Exit criterion: eating a berry yields only Common produce, neither crops nor seeds can be planted, deployable items can still be placed, and right-clicking the converter does nothing; the four acceptances do not depend on each other.

#### F-216 · The Lucky Berry pool at 11 and the assertion in the same commit (S · 0.5 person-days | Depends on: F-214)

- **Goal**: the pool goes from 19 entries to **11** and equals exactly the Common tier's whole set, **derived from `tier()`** (A-63); the startup assertion and the log text change **in the same commit** (D-156, RT-50).
- **Files touched**: `util/CropProfile.java:138-143` (the pool's construction and the comment above it); `Farmstorm.java:117-123` (the assertion + the log text).
- **Concrete changes**: the pool's construction goes from "exclude 3" to "keep only `tier() == COMMON`", and the comment follows as "Common tier only = 11 entries"; the assertion `poolSize == 19` → `== 11`; the log text's "(排除 lucky_berry / wild_apple / compressed_puffed_wheat)" changes to wording consistent with the derivation; the second half of the assertion (the three `!=` checks) is **kept** (D-156's original text asks for the other half, "does not contain the three").
- **Implementation notes**: the arithmetic `19 − 8 = 11` (what is moved out is the 6 Rare + 2 Epic inside the pool); the drawing algorithm `LuckyBerryRoller.java:172-180` is not changed by a character; the pool's 11 entries and `CropTier.COMMON`'s 11 must come from one source (A-63's reason: a hand-written list of 11 ids would form a second truth and conflict with the mitigation of document 36 §7 RT-49).
- **Easy-to-miss points**: ① change the pool but not the assertion → one permanently red FAIL on every startup (it does not throw, does not exit, and the server starts normally, but the log is distorted, RT-50's corrected wording); ② change the assertion but not the pool → the assertion goes red; ③ the log text is one of the "two places" RT-50 names (the other is the assertion value); ④ do not touch `LuckyBerryRoller.java:170`'s javadoc "14-entry pool" in passing — that is the existing blemish of document 34 §8 item 8.
- **Acceptance assertions**:
  1. Startup log: `自检 幸运浆果池 11 种…→ OK`.
  2. The pool's entry count = **11**, and all 11 have `tier()` = `COMMON` (cross-checked with F-214's item 3 count).
  3. Eating `lucky_berry` and hitting the "generate a random crop" branch rolls only the 11 Common crops (document 36 §8 #35).
  4. `== 19` has **0** hits in `Farmstorm.java`.

#### F-217 · Planting path ①: `isPlantableSoil` constantly `false` (S · 0.5 person-days | Depends on: —)

- **Goal**: the 15 profiles carried by `ModifiedCropItem` that have crop blocks can no longer be planted on the ground (D-152 ①); the "place it" behaviour of deployable items is kept.
- **Files touched**: `item/ModifiedCropItem.java:101-107` (the predicate body); `:72` (the only call site, **untouched**); `:53-55` (the dead code `canPlantAt`, **untouched**).
- **Concrete changes**: the predicate's body is changed to return `false`, and the old "only a `FarmBlock` is allowed" check is left as a comment for reference (document 36 §2.2 path ①'s wording); neither `ModifiedCropItem#useOn` nor `DeployableCropItem#useOn` changes.
- **Implementation notes**: `plantCrop` gets `false` at `:72` and returns `PASS` directly, earlier than `consume(1)` at `:96` ⇒ the held item is not consumed; the other branches of `plantCrop` (no crop block, `canPlace`, `canSurvive`) are untouched; `FarmstormCropBlock` does not move — `mayPlaceOn` / `canSurvive` were never overridden (document 36 §2.2's negative list).
- **Easy-to-miss points**: ① closing only this one path misses **7 seeds** (RT-48) — this task owns path ① only, path ② being F-218, and the two acceptances are **written separately, never merged**; ② `canPlantAt` (`:53-55`) has 0 callers across the repository and **must not be deleted** (D-152 supplement: deleting dead code is an existing cleanup, not this version's); ③ the 7 deployable items right-clicked on farmland turn into "place the deployed form", which is the **visible consequence** of the cut and not a missed block (D-152 supplement, document 35 §3.2's ①').
- **Acceptance assertions**:
  1. Take the 15 crop items that have crop blocks and right-click farmland with each: no block appears, the held item is not consumed, and there is no prompt (document 36 §8 #6).
  2. The 7 deployable items (`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` / `zangief_cane` / `point_defense_fungus`) right-clicked on the top face of a full block: placed as usual (document 36 §8 #8).
  3. `isPlantableSoil`'s executable body holds no `instanceof FarmBlock` (only the comment line is left); the method is still called from `:72` (1 hit).

#### F-218 · Planting path ②: `SeedItem#useOn` returns `PASS` (S · 0.5 person-days | Depends on: —)

- **Goal**: the 7 standalone seed items can no longer grow a crop on farmland, and they do not consume the held item (D-152 ②).
- **Files touched**: `item/SeedItem.java:16` (the class declaration and its parent), `:28-31` (the only existing override; the new override goes after it).
- **Concrete changes**: a new `useOn` override returns `PASS` (document 36 §2.2 path ②), cutting the vanilla planting chain `ItemNameBlockItem` → `BlockItem#useOn` → `place()`; everything inherited besides planting is untouched.
- **Implementation notes**: `PASS` means "do nothing here", not `FAIL` (`FAIL` would make the caller believe it was rejected); `PASS` comes before `BlockItem#place`'s `consume` ⇒ the seed is not consumed; holding a seed and right-clicking a chest / sign or any other interaction is unaffected (the three semantics of document 36 §2.2).
- **Easy-to-miss points**: ① the 7 seeds have only **one implementation** (all go through `SeedItem`), so one change covers 7 — do not go looking class by class; ② the 8 profiles whose "seed = itself" (the seed item is the crop itself) **do not take this path** and are covered by F-217; ③ the class comment at `SeedItem.java:13` says "6 standalone sowing items" while the measurement is 7 — an existing typo, not changed in this version (document 34 §8 item 4).
- **Acceptance assertions**:
  1. Right-click farmland with each of the 7 seeds: no block appears, the seed is not consumed, no prompt (document 36 §8 #7).
  2. Holding a seed and right-clicking dirt (not farmland) still does nothing.
  3. `SeedItem.java` has `InteractionResult.PASS` **1** hit and `useOn` **1** hit; `appendHoverText` is still there (the tooltip does not regress).

#### F-219 · The converter's `useWithoutItem` returns `PASS` (S · 0.5 person-days | Depends on: —)

- **Goal**: right-clicking the converter does nothing and opens no screen (D-170); the block, the block entity, the menu chain, the 29 recipes and the creative-tab entry are **all kept**.
- **Files touched**: `block/ConverterBlock.java:78-86` (changed); `:70-76` (`getMenuProvider`, kept), `:88-96` (drops, kept).
- **Concrete changes**: `useWithoutItem`'s body is replaced whole so that it returns `PASS`, the same statement on the client and the server, no longer branching (document 36 §2.9); `getMenuProvider` stays but has no caller.
- **Implementation notes**: `PASS` rather than `SUCCESS` / `CONSUME` (the latter two swallow the right-click); `ConverterMenu` / `ConverterScreen` / `FarmstormMenus.CONVERTER` do not move at all.
- **Easy-to-miss points**: ① deleting the screen or menu registration in passing ⇒ `menu_types` goes from 1 to 0, the registry surface becomes 96 and conflicts with 97; ② "temporarily removing the feature" does not include deleting the 26 `converting` recipes (deleting them means rebuilding them when the feature comes back, D-170 supplement); ③ the Chinese name stays as it is, "农产品转换器", and **the language key is not changed** (author's ruling #19).
- **Acceptance assertions**:
  1. Right-click the converter with an empty hand: no screen, no prompt, the held item is not consumed (document 36 §8 #36).
  2. The converter is still in the creative tab and still craftable; the json count in `data/farmstorm/recipe/` = **29** (26 `converting` + 2 shaped + 1 shapeless).
  3. `FarmstormMenus.java`'s `REGISTER.register` still has **1** hit (2 after F-227).

### Phase 34 · Retiring Farmland and the Trades (F-220 ~ F-223)

Phase goal: zero the farmland system at runtime (keeping the code, the save and the blocks), and at the same time build "the only entrance to the Epic tier" — the farmer trades. Exit criterion: tilling is not blocked, trampling is not protected, the `[outpost_core]` section of `farmstorm-server.toml` has no `farmlandBonus`, and the farmer trade list shows 3 of this mod's entries.

#### F-220 · Retire the farmland system: 5 comment-outs + the key definition + the field kept (M · 1.5 person-days | Depends on: —)

- **Goal**: comment out the farmland system's 5 runtime entries (D-153): two registration lines, two implementation blocks and one line of startup backfill; `onServerTickPost` itself and the other two lines inside it are **kept** (otherwise the overlay packet stops); for `farmlandBonus` **only the definition is commented out and the field is kept** (A-62).
- **Files touched**: `event/FarmstormGameEvents.java:75` (the `onRightClickBlock` registration), `:76` (the `onFarmlandTrample` registration), `:241-265` (the tilling-block implementation), `:277-281` (the trample-protection implementation), `:171` (`FarmlandLedger.get(server).backfill(server)`); `config/FarmstormConfig.java:227` (the field, kept), `:909-912` (the definition, commented out).
- **Concrete changes**: each of the five comment-outs carries one reason line "裁定 #3：v0.2.0 撤销耕地，勿恢复"; the field declaration at `:227` is kept as it is, with two comment lines beside it — the definition is commented out ⇒ the field is `null` at runtime ⇒ **the definition must be restored before** `:75` / `:76` are restored, or `FarmlandGuard.java:75` throws an NPE (document 36 §2.3's comment text); `:170`'s `CoreChunkTickets.restoreAll` and `:173-175`'s `CoreListBroadcaster.tick` are not changed by a character.
- **Implementation notes**: **the three comment-outs (the two registration lines + `backfill`) and keeping `onServerTickPost` must be one commit** — commenting out only the registration lines but not the implementations leaves dead code referring to two retained classes, while commenting out the implementations but not the registration lines fails to compile (the `addListener` method reference breaks); after `:171` is commented out, `FarmlandLedger.get()`'s only runtime call site is `settleOnRemoval` (A-69), and this task **does not touch** `OutpostCoreBlockEntity`.
- **Easy-to-miss points**: ① `FarmlandLedger` (555 lines) / `FarmlandGuard` (225 lines) — **not one line changed** (D-153, document 34 §7 item 1); ② the `.dat` **is not read, not written, not deleted** (D-154), but its "no reads, no writes" only fully holds at F-233 (A-69); ③ `settleOnRemoval` still calls `FarmlandLedger.of` (`:353`) and `ledger.migrate/remove` (`:384`/`:388`/`:391`) right now, and those lines are rewritten whole by F-233, so this task does not touch them; ④ do not delete the `FarmlandGuard` / `FarmlandLedger` imports from `FarmstormGameEvents` — the two method bodies still refer to them (they must stay readable inside comments).
- **Acceptance assertions**:
  1. All five comment points are in the commented state (`onRightClickBlock` / `onFarmlandTrample`'s registration lines and bodies, the `backfill` single line), each with a "裁定 #3" reason line above it.
  2. `onServerTickPost` is **not commented out**; both `CoreChunkTickets.restoreAll(server)` (`:170`) and `CoreListBroadcaster.tick(server)` (`:175`) are in the body, and the overlay's per-100-tick reconciliation log still prints (document 36 §8 #10).
  3. The field declaration at `FarmstormConfig.java:227` is still there, the definition at `:909-912` is commented out, and the two comment lines beside the field read "restore the definition before restoring the listeners, otherwise NPE" (document 36 §8 #12).
  4. Delete the old `farmstorm-server.toml` and start the server: the `[outpost_core]` section has **2 keys** (`maxPerPlayer` / `boundaryParticleIntervalTicks`) and the whole file has **165 keys / 58 sections** (`storageSlots` and `cultivatorCapacity` come back in F-226 / F-231).
  5. In game: tilling outside one's own core's range still makes farmland, trampling one's own farmland still turns it into dirt, and the two action-bar prompts no longer appear (document 35 §7.4).

#### F-221 · 3 farmland language keys retired (S · 0.5 person-days | Depends on: F-220)

- **Goal**: the 3 farmland language keys are no longer generated (D-153); the `message.farmstorm` prefix bucket is emptied.
- **Files touched**: `datagen/ModLangProvider.java:403` (`farmstorm.configuration.outpost_core.farmlandBonus`), `:412` (`message.farmstorm.farmland.no_core`), `:413` (`message.farmstorm.farmland.quota_full`), plus the section comment above them at `:411`.
- **Concrete changes**: the three entries are removed from the `ENTRIES` table (which shows as "no longer written into the two lang files"); that "2 action-bar prompts of v0.1.0" block at `:411` is handled along with them, leaving no comment pointing at air.
- **Implementation notes**: "deleting" in datagen means "no longer producing"; the keys' definitions were already commented out in F-220 and the prompt emitters were commented out in F-220 too, so the three keys have no reader at this point (document 36 §4.3.1).
- **Easy-to-miss points**: ① deleting only the two `message.*` entries and missing `farmstorm.configuration.outpost_core.farmlandBonus` (the config screen then shows a bare key name); ② missing the section comment at `:411`; ③ do not change `ModLangProvider`'s class comment here (the total is closed once in F-241, see §0.3 discipline 4).
- **Acceptance assertions**:
  1. None of the 3 keys exists in the two lang files; the `message.farmstorm` prefix has **0** entries (document 36 §8 #13).
  2. The hit count of `farmlandBonus` / `farmland.no_core` / `farmland.quota_full` in `ModLangProvider.java` = **0**.
  3. `git diff` shows only deleted lines and the change to that section comment; no other key's value was changed in passing.

#### F-222 · The `trade` group's 6 keys + 6 config language keys (M · 1.5 person-days | Depends on: F-220)

- **Goal**: add the top-level group `trade` with min / max for the three tiers, 6 keys in all (D-159), and give the 6 keys 6 config language keys (document 36 §4.1.1 #6~#11, §4.3.2 #12~#17).
- **Files touched**: `config/FarmstormConfig.java` (6 static fields added beside the field block `:227-231`; a new `trade` section after `b.pop()` at `:919` of the `outpost_core` section in the static block); the config section of `datagen/ModLangProvider.java` (6 entries added).
- **Concrete changes**: `trade.common.min` = 2, `trade.common.max` = 5, `trade.rare.min` = 10, `trade.rare.max` = 32, `trade.epic.min` = 40, `trade.epic.max` = 64, all ranges **1–64** (the 5th of document 36 §4.1.1's five range reasons); the 6 key names `farmstorm.configuration.trade.{common,rare,epic}.{min,max}`.
- **Implementation notes**: `trade` is a **top-level group** (a sibling of `outpost_core`), the group count `58 + 1 = 59`; the two runtime disciplines "swap when `min > max`" and "take a single value when `min == max`" are implemented **only once, at F-223's read point** (the three runtime disciplines of document 36 §4.1.1), and this task does not write a second copy.
- **Easy-to-miss points**: ① the 6 language keys are 1:1 with the 6 config keys (convention 2 of document 36 §4.3.2), and missing one leaves a bare key name on the config screen; ② the "how many per offer" quantity **does not go into config** (document 35 §4.3's 3rd reason); one more key would push 175 to 176; ③ do not write the `trade` section as a nested group of `outpost_core` (nesting would count the groups as 61).
- **Acceptance assertions**:
  1. Delete the old toml and start the server: `[trade]` appears in `run/config/farmstorm-server.toml` with exactly **6 keys**, values `2 / 5 / 10 / 32 / 40 / 64` in that order.
  2. The whole file has **171 keys / 59 sections** (counted by `^\s*\[`; this is a toml interval reading, not the source-code ledger — the source-side ledger is in F-242).
  3. The two lang files each gain 6 `farmstorm.configuration.trade.*` entries.

#### F-223 · 3 farmer trades: `VillagerTradeEvents` + `TierTrade` (L · 3.0 person-days | Depends on: F-222)

- **Goal**: give the farmer villager 3 trade entries, one per tier, unlocked in level slots **1 / 3 / 5**, each with `maxUses = 1`, taking only emeralds and giving 2 crops of that tier (D-157, D-158, D-159); no legacy-save migration.
- **Files touched**: new `event/VillagerTradeEvents.java`, `event/TierTrade.java`; the `Farmstorm.java:71-73` area (hooked onto the same bus next to `FarmstormGameEvents.register(NeoForge.EVENT_BUS)`); reads F-222's 6 `trade` keys.
- **Concrete changes**: subscribe to `VillagerTradesEvent`, handle `VillagerProfession.FARMER` only, and add one entry each to `getTrades().get(1)` / `get(3)` / `get(5)` (document 36 §2.4's three-line handler); `TierTrade` implements `VillagerTrades.ItemListing` and in `getOffer` draws the crop id and the price **once** (document 36 §2.4's five steps: draw 1 entry uniformly → read the interval and swap → take a random closed-interval value → `ItemCost(EMERALD, price)` → `MerchantOffer(..., maxUses = 1, xp = 2, priceMultiplier = 0.0F)`).
- **Implementation notes**: both the draw and the pricing happen **when the offer is first generated** (D-159's timing discipline) and are fixed afterwards; no second out-of-stock / restock mechanism is written (out-of-stock is carried by `maxUses = 1`, restock by the vanilla `resetUses`, D-158); no vanilla trade entry is changed and no seeds are given (document 35 §4.3 note 1).
- **Easy-to-miss points**: ① writing the level slots as 2 / 4 ⇒ not one trade entry is visible (RT-46); ② re-rolling the price at the moment of the trade ⇒ the same offer costs a different price per click; ③ a farmer already at the top level in a legacy save **does not get** the new entries (vanilla semantics; this version **does no migration**: no re-issue logic, no `Offers` NBT change, document 36 §2.4.2's ⚠️); ④ if the list from `getTrades().get(1)` is immutable, take document 36 §9 ①'s fallback (`put` a new `ArrayList`) — this is the site of item to verify ①.
- **Acceptance assertions**:
  1. The farmer trade screen shows **3** of this mod's entries, in level slots **1 / 3 / 5** (document 36 §8 #15).
  2. In `/data get entity <villager> Offers.Recipes` the three entries' `maxUses` = **1**, and their `buy.count` falls in **[2,5] / [10,32] / [40,64]** respectively (document 36 §8 #17).
  3. Each entry's `sell` is **2** crops of the same tier; after one trade the entry stays in the list and merely greys out (document 36 §8 #16).
  4. After a restock the price and the crop id are unchanged (the offer is not re-rolled, D-158).

### Phase 35 · Registry Surface, Resources and the Container Skeleton (F-224 ~ F-228)

Phase goal: land all 4 of this version's registry increments (the cultivator trio → the core menu), with the matching resources, container skeleton and menu skeleton, and close the registry self-check window once at the end. Exit criterion: `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / … / menu_types 2 → OK`; `blockstates` 27, `models` 106, `loot_table` 27.

#### F-224 · The cultivator registration trio + the creative tab (L · 3.0 person-days | Depends on: —)

- **Goal**: land the block / block entity / block item trio of `farmstorm:cultivator` (D-160, D-171) and put it into the creative tab (A-64); **this is the opening of the registry self-check window** (§0.3's state A).
- **Files touched**: new `block/CultivatorBlock.java` (the `WORKING` property, `codec`, `getRenderShape`, the `getTicker` declaration), new `blockentity/CultivatorBlockEntity.java` (skeleton + `newBlockEntity` wiring); `registry/FarmstormBlocks.java` (+1 after `:82-90`'s `outpost_core`), `registry/FarmstormBlockEntities.java` (+1 after `:50`), `registry/FarmstormItems.java` (+1 after `:116-117`'s `outpost_core` BlockItem); `registry/FarmstormItems.java:122-129` (`creativeTabItems()`, A-64); `datagen/ModLangProvider.java` (+2 entries: block name / item name).
- **Concrete changes**: the block's properties follow the anvil: `strength(5.0F, 1200.0F)` + `MapColor.METAL` + `requiresCorrectToolForDrops` + `SoundType.ANVIL` + `PushReaction.BLOCK` (document 36 §2.5.1's item-by-item table); `working` is a `BooleanProperty`, default `false`; `getRenderShape` returns `MODEL` (`BaseEntityBlock`'s default is `INVISIBLE`); `getTicker` returns **on the server only**; two language keys `block.farmstorm.cultivator` / `item.farmstorm.cultivator` (document 36 §4.3.2 #1~#2).
- **Implementation notes**: the block entity **does not implement `Container`** (document 36 §2.5.2's reason: a hopper / dropper would bypass the admission surface); the block entity only gets its skeleton here, the three NBT keys and the state machine being F-235; **this task does not run `runData`** (blockstate / models / loot table are F-225).
- **Easy-to-miss points**: ① not entering `creativeTabItems()` means it **cannot be obtained** — this version has no cultivator recipe (document 36 §5's 29 recipes unchanged), so the creative tab is the only entrance (A-64); ② do not register a menu in passing (the cultivator has no screen; `menu_types`' +1 is for the core's storage only); ③ `outpost_core`'s `registerItem` + `otherProperties()` is the only precedent, so write it after that one; ④ the problem "a registered block not added to `ModLootTableProvider.getKnownBlocks()`" is handled in F-225, so do not change that file early in this task.
- **Acceptance assertions**:
  1. Startup log `自检 注册对象 96 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → FAIL` — **FAIL is the correct value during the window**, and this line must not be written as OK (§0.3).
  2. `/give` yields `farmstorm:cultivator`; `/setblock` places it; `/data get block <pos>` shows the block entity.
  3. The "tools and blocks" segment of the creative tab gains one cultivator entry at its end; `cultivator` has ≥ **2** hits in `FarmstormItems.java` (registration + creative tab).
  4. The self-check's other lines are still all green (`自检 作物`, `自检 作物品质`, `自检 幸运浆果池`).

#### F-225 · Cultivator resources, the two models and three datagen places (L · 3.0 person-days | Depends on: F-224)

- **Goal**: land 7 texture files + two models + the two-state blockstate + the item model + the loot table (document 36 §5's 12 files), and stop datagen from asking for a non-existent `item/cultivator.png`.
- **Files touched**: `src/main/resources/assets/farmstorm/textures/block/` gains 7 files (document 36 §5 table #1~#7: 3 PNGs + 2 mcmeta + 2 static first frames); `datagen/ModBlockStateProvider.java:69-85` (`registerStatesAndModels`'s dispatch, the fallback branch at `:80-84`); `datagen/ModItemModelProvider.java:56-59` (`usesBlockModel` and the comment above it); `datagen/ModLootTableProvider.java:124-131` (the `add` call area), `:189-216` (`getKnownBlocks`'s 26-entry list), `:37` (the class comment "26").
- **Concrete changes**: three faces use `cube_bottom_top`: the idle model refers to `cultivator_a_top_still` / `cultivator_a_side_still` / `cultivator_a_bottom`, the working model refers to the two frame strips + `_bottom` (document 36 §2.5.5's two model tables); `blockstates/cultivator.json` gets two variants (`working=false` / `true`); `usesBlockModel` gains `CultivatorBlock`; the blockstate goes through `partialState().with(WORKING, false/true)` rather than `simpleBlock` (document 36 §5's knock-on change 2); the loot table drops itself.
- **Implementation notes**: the three names `cultivator_a_top` / `_side` / `_bottom` **may not change** (the texture pack's README says so explicitly); the two static first frames are copied from `editable_frames/side_00.png` / `top_00.png` and named `*_still` (document 36 §2.5.5's "[this document's decision]" 13); the two mcmeta files are byte-for-byte identical, `frametime 2`, `frames 0..15` (document 36 §8 #19).
- **Easy-to-miss points**: ① **`usesBlockModel` without `CultivatorBlock`** ⇒ `ModItemModelProvider` falls to `basicItem` and asks for a non-existent `textures/item/cultivator.png`, showing the purple-black checker in game (document 36 §5, §10 #10) — this is the task's pit 1, and the comment above `usesBlockModel` ("only ConverterBlock and OutpostCoreBlock") must change with it; ② the fallback branch would generate a `cubeAll` + a non-existent `block/cultivator.png` for the cultivator, so the dispatch must come **before** the fallback; ③ `getKnownBlocks()` is **hand-written 26 entries**, and adding only `add(...)` without this list means no loot table is generated and breaking the block drops nothing; ④ the two `_still` images **carry no** mcmeta.
- **Acceptance assertions**:
  1. `src/generated/resources/assets/farmstorm/blockstates/cultivator.json` has exactly **2** variants (`working=false` / `working=true`), the latter's model being `farmstorm:block/cultivator_working`.
  2. `models/block/cultivator.json` and `models/block/cultivator_working.json` exist, both with the parent model `minecraft:block/cube_bottom_top`.
  3. `models/item/cultivator.json`'s parent is `farmstorm:block/cultivator` (if `item/generated` or `layer0` shows up, `usesBlockModel` was missed).
  4. `data/farmstorm/loot_table/blocks/cultivator.json` exists, and breaking it drops **1** of itself.
  5. Count reconciliation: `blockstates` 26 → **27**, `models` 103 → **106**, `loot_table` 26 → **27**, `*.png.mcmeta` 0 → **2**, `textures/block` gains **5** PNGs.
  6. In game, F3's targeted block state shows the idle model (the static frame) when `working=false`.

#### F-226 · The core becomes a container: 54 slots + `Items` + two sync methods (L · 3.0 person-days | Depends on: —)

- **Goal**: turn `OutpostCoreBlockEntity` from "stores only the owner" into a **54-slot** container with `implements Container`, add `Items` to NBT, supply `getUpdateTag` / `getUpdatePacket`, and read legacy saves compatibly (D-166, RT-44); at the same time add `outpost_core.storageSlots` (default 54).
- **Files touched**: `blockentity/OutpostCoreBlockEntity.java:48` (the class declaration), `:82-86` (the field block), `:411-424` (`saveAdditional` / `loadAdditional`); the `config/FarmstormConfig.java:913-918` area (`outpost_core` section +1 key); `datagen/ModLangProvider.java` (+1 config key name).
- **Concrete changes**: a 54-slot `NonNullList` + the `Container` interface's seven methods (document 36 §2.6.1's method-by-method shape); NBT gains `Items` (`ContainerHelper`'s `TAG_ITEMS`); `getUpdateTag` writes **only the owner's three keys**, not `Items`, not the bindings (document 36 §2.6.1's size plan); `getUpdatePacket` uses `ClientboundBlockEntityDataPacket.create(this)`; two **whole-batch** insert methods `canAccept` / `acceptAll` are added (compute first, then write; document 36 §2.6.1's three disciplines); `storageSlots` defaults to **54** with range **54–54** (document 36 §4.1.1 #3).
- **Implementation notes**: a legacy save missing `Items` ⇒ `loadAllItems` reads an empty list ⇒ **54 empty slots**, no error and no save wipe (document 36 §2.6.2); after `acceptAll` succeeds it must `setChanged()` and `sendBlockUpdated` in the same tick; if `storageSlots`' range is rejected by `ModConfigSpec`, take document 36 §9 ⑦'s fallback (`54..55` + clamping to 54 when the menu is created), and **the slot count may not become any other value**.
- **Easy-to-miss points**: ① **54 is the slot count, 16 is the cultivator allowance** (author's ruling #8); writing them the wrong way round voids a whole block of acceptance; ② extra old keys in a legacy save are **not read, not deleted** and stay in the `CompoundTag` as they are (document 36 §2.6.2's line 4); ③ this task **adds no menu registration** (`menu_types` +1 happens only in F-227, otherwise F-224's window state A would not match); ④ do not put `Items` into `getUpdateTag` (a few KB more per chunk send; RT-44's positive answer is "do not put it in").
- **Acceptance assertions**:
  1. `/data get block <core>` shows the `Items` key (an empty list when there are 0 items).
  2. A legacy save (a core saved by 0.1.1) after the upgrade: the core loads as usual, `/data get block` reads 54 empty slots, and there is no exception stack (opening the screen is verified only in F-229; document 36 §8 #24's precondition is here).
  3. Delete the old toml and start the server: the `[outpost_core]` section has **3 keys** (`maxPerPlayer` / `boundaryParticleIntervalTicks` / `storageSlots`), and the whole file has **172 keys / 59 sections** (a toml interval reading, not the source-code ledger).
  4. There is no third place writing `Items` besides `saveAdditional` / `loadAdditional`; `getUpdateTag`'s returned body holds only the owner's three keys (1 manual check).

#### F-227 · The core menu + screen + `menu_types` +1 (L · 3.0 person-days | Depends on: F-226)

- **Goal**: land `outpost_core`'s 54-slot menu and screen, `menu_types` 1 → 2 (D-166, D-171); **this is the last registry increment inside the window** (§0.3's state B).
- **Files touched**: new `menu/OutpostCoreMenu.java`, new `client/OutpostCoreScreen.java`; `registry/FarmstormMenus.java:16-17` (+1 entry); `client/FarmstormClient.java:52-54` (+1 screen registration line); `datagen/ModLangProvider.java` (+1 `container.farmstorm.outpost_core`).
- **Concrete changes**: 54 slots (6 rows × 9 columns, x = 8 + 18·col, y = 18 + 18·row) + the player inventory's 3 rows (y = 140) + the hotbar (y = 198), `imageWidth = 176` / `imageHeight = 222`, reusing the vanilla `generic_54.png` (document 36 §2.6.3's "[this document's decision]" 12); `quickMoveStack` both ways (0–53 ⇄ 54–89); the client falls back to `SimpleContainer(54)`; title key `container.farmstorm.outpost_core`.
- **Implementation notes**: the screen's `renderBg` uses the **six-argument** `blit` (the five-argument form computes UVs against 256×256 and shifts everything, document 36 §2.6.3); the menu reads `storageSlots` once, at creation only; `stillValid` uses vanilla semantics (wired up when F-229 lands).
- **Easy-to-miss points**: ① forgetting the screen registration at `FarmstormClient.java:52-54` ⇒ opening the screen crashes or shows black; ② `quickMoveStack`'s 54 + 36 slot ranges are the project's first, so follow vanilla `ChestMenu`'s two segments (document 36 §9 ⑧); ③ once `menu_types` is +1 the self-check line is **97 + FAIL** (the assertion constant is still 93), and **this task must not write OK** (§0.3).
- **Acceptance assertions**:
  1. Startup log `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / … / menu_types 2 → FAIL` (the correct value during the window).
  2. `FarmstormMenus.REGISTER.register` has **2** hits; the `FarmstormClient.java:52-54` area has 2 `event.register(...Screen::new)` lines.
  3. One language key `container.farmstorm.outpost_core` is added — 1 entry in each of the two lang files.
  4. In game the screen still **cannot be opened** at this point (the opening action is F-229) — this is the expected intermediate state, not a defect.

#### F-228 · Registry self-check closes at 97 (the window closes) (S · 0.5 person-days | Depends on: F-224, F-227)

- **Goal**: change **the 5 constants at `Farmstorm.java:147-150` together** (`93 → 97`, `items 32 → 33`, `blocks 26 → 27`, `blockEntities 7 → 8`, `menuTypes 1 → 2`), start the server once to check the log line `→ OK`, and close §0.3's window.
- **Files touched**: `Farmstorm.java:147-150` (the 5 constants); `:126-131` (the method comment's "64 → 77 → 90 → **93**" and "the sum is not **93**"); `:151-155` (the 13-parameter format string, **not one character changed**).
- **Concrete changes**: change only the 5 numeric constants and the 93 in those two comments; the other 8 classes' constants (`entityTypes 13` / `mobEffects 4` / `soundEvents 3` / `recipeTypes 1` / `recipeSerializers 1` / `creativeTabs 1` / `damageTypes 3` / `dataComponents 1`) — **none of them moves** (document 36 §1.2's old → new table).
- **Implementation notes**: the 13 placeholders' argument order does not change (change the values, not the string, document 36 §9 ⑬); after the change the server must **really be started once** to read the log — reading the code is not enough.
- **Easy-to-miss points**: ① changing only `total` and not the four items ⇒ still FAIL; ② missing the two 93s in the method comment (the comment then lies); ③ adding an item to `damageTypeCount()`'s 3 `ResourceKey`s in passing (this version has no new damage type; document 36 §1.2's last paragraph says explicitly "add no item"); ④ this task **carries** no other change (`git diff` is only this block).
- **Acceptance assertions**:
  1. `run/logs/latest.log` shows `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK` (document 36 §8 #1).
  2. `93` has **0** hits in `Farmstorm.java`; `97` ≥ **1**.
  3. `git diff` touches only the 5 constants and the two comments, and the format string is unchanged.

### Phase 36 · Core Interaction, Binding and Migration (F-229 ~ F-233)

Phase goal: turn the core into a complete carrier — "a chest that opens + an anchor that binds 16 cultivators + something that hands the cultivators over when torn down". Exit criterion: an ally right-clicks within the vanilla reach to open the 54-slot screen, breaking it drops the whole contents, one core binds 16 cultivators, and when the core is destroyed the cultivators are rebound or fail in place (**they do not turn into dirt**).

#### F-229 · Opening the core's storage: two gates (M · 1.5 person-days | Depends on: F-227)

- **Goal**: right-clicking the core opens the 54-slot storage; the two gates are **being an ally** + **the vanilla container's reach** (D-166 supplement, author's ruling #17); a non-ally gets `PASS`.
- **Files touched**: `block/OutpostCoreBlock.java` (add `useWithoutItem` and `getMenuProvider` before `onRemove` at `:101-107`); `menu/OutpostCoreMenu.java` (built in F-227; this task supplies `stillValid`).
- **Concrete changes**: the server checks ally first (`isAlly`, D-138's existing judgment) and returns `PASS` for a non-ally; if it passes, `player.openMenu(state.getMenuProvider(...))` → `CONSUME`; the client `return SUCCESS` (HC-03's existing trade-off, following `ConverterBlock.java:81-83`); the menu's `stillValid` uses `Container.stillValidBlockEntity` (vanilla's default distance 4.0).
- **Implementation notes**: **no new distance constant and no new config key** (author's ruling #17); the 3×3 chunks are the range of binding / linking, **not** a condition for opening (document 36 §2.6.4's reading 2; document 35 §8.3's ⚠️ is out of date, document 36 §10 #13); in practice the screen stays open within about 8.5 blocks and vanilla closes it when the player walks away.
- **Easy-to-miss points**: ① a non-ally must get `PASS`, not `SUCCESS` (`SUCCESS` swallows the right-click); ② right-clicking while holding a block goes through placement first and does not open the screen (document 36 §9 ⑨), so acceptance always says "empty hand"; ③ do not judge the distance inside `useWithoutItem` yourself (that would double up with vanilla `stillValid` and add one more magic number).
- **Acceptance assertions**:
  1. An ally right-clicks the core with an empty hand: the **54-slot** screen opens (6 rows × 9 columns + the player inventory), titled "据点核心"; after storing 3 items, close and reopen — the items are still there (document 36 §8 #24).
  2. A non-ally right-clicks: **no screen** (document 36 §8 #25's ①).
  3. An ally right-clicks **9 blocks** from the core: the client cannot send the interaction and no screen opens; having opened it within **8 blocks** and then walked to 9 blocks, the screen is closed by vanilla `stillValid` (document 36 §8 #25's ②).

#### F-230 · Core destruction drops (S · 0.5 person-days | Depends on: F-226)

- **Goal**: when the core is **really removed**, all 54 slots drop; being pushed by a piston / replaced by the same block / chunk unload drops nothing (D-167).
- **Files touched**: `block/OutpostCoreBlock.java:101-107` (a drop branch added inside `onRemove`).
- **Concrete changes**: keep the existing `core.release()`, and append the four-condition drop: not the same block + not moved by a piston + that position's BE is a `Container` ⇒ `Containers.dropContents` (following `ConverterBlock.java:88-96`).
- **Implementation notes**: the drop and "removing the index + migrating the cultivators" are **two things with two hooks** (`onRemove` vs `setRemoved` / `settleOnRemoval`, D-167 supplement); `getStateForPlacement` / `setPlacedBy` / `getTicker` do not move.
- **Easy-to-miss points**: ① missing `!movedByPiston` ⇒ a piston push spawns a copy of the items in place (a duplication bug); ② do not write the 54-slot drop into `settleOnRemoval` (F-233 handles only bindings and the index); ③ the core resists explosions at 1200 and by default cannot be blown up, so **do not add a separate explosion branch** (document 35 §8.4 note ①).
- **Acceptance assertions**:
  1. Put 3 different stacks in the core and break it: those 3 stacks appear on the ground (document 36 §8 #26).
  2. Piston push: **0** drops; replacement by the same block (`/setblock` to the same block): **0** drops; chunk unload (walk away and come back): the items are still in the core.
  3. An empty core broken: only 1 of the block itself drops (document 35 §12 B-27).

#### F-231 · The core's 16-entry binding list + `cultivatorCapacity` (M · 1.5 person-days | Depends on: F-226)

- **Goal**: the core-side 16-entry binding list becomes **the single truth** (the capacity carrier + the lookup surface), and `outpost_core.cultivatorCapacity` is added (default 16) (D-165, author's ruling #8).
- **Files touched**: `blockentity/OutpostCoreBlockEntity.java` (beside the field block `:82-86` + `Cultivators` added to the NBT at `:411-424` + the following 5 methods `cultivatorCount` / `hasCultivator` / `addCultivator` / `removeCultivator` / `cultivators`); `config/FarmstormConfig.java` (+1 key after `:912` in the `outpost_core` section); `datagen/ModLangProvider.java` (+1 entry).
- **Concrete changes**: `List<BlockPos>` is persisted as `Cultivators` (a Long list, ≤ 16 entries); a legacy save missing the key ⇒ an empty list (an old core bound farmland, not cultivators, document 36 §2.6.2); `cultivators()` does one lazy `getChunkNow` cleanup on the two low-frequency paths **core selection / placement** (document 36 §2.7's last paragraph); `cultivatorCapacity` defaults to **16** with range **1–64** (document 36 §4.1.1 #2).
- **Implementation notes**: the single truth of the binding is on the core side; `Core` in the cultivator BE is only a **reference cache** and is validated against the core-side list before use (D-165); when the list is full at 16, the 17th does not push out an already bound one (document 35 §8.2's worked example 2); the runtime does **not** run a `getChunkNow` check every tick (document 36 §2.7 explicitly rejects it: 256 chunk queries per tick).
- **Easy-to-miss points**: ① putting 16 into the container's slot count (54 is the slot count, author's ruling #8); ② putting the binding list into `getUpdateTag` (rejected by document 36 §2.6.1); ③ `Cultivators`' key name must match document 36 §2.6.1's capitalization (`Items` / `Cultivators` both start with a capital letter).
- **Acceptance assertions**:
  1. `/data get block <core>` shows `Cultivators` (absent or an empty list when empty); after binding 1 cultivator it is **1** long entry.
  2. Delete the old toml and start the server: the `[outpost_core]` section has **4 keys** (`cultivatorCapacity` / `storageSlots` / `maxPerPlayer` / `boundaryParticleIntervalTicks`), and the whole file has **173 keys / 59 sections** (a toml interval reading, not the source-code ledger).
  3. After `cultivatorCapacity = 2` is set (once F-232 has landed), whether a 3rd cultivator can be placed: **it can be placed but stays unbound** (document 35 §8.2's worked example 2).

#### F-232 · Binding on placement: four criteria + writes on both sides (L · 3.0 person-days | Depends on: F-224, F-231)

- **Goal**: when a cultivator is placed it automatically binds to the **nearest** core among those that "cover it + are allied + have a free slot"; the distance is `BlockPos#distSqr`, a tie goes by `placedGameTime` ascending and then by the core's coordinate lexicographic order; **both** the core side and the cultivator side are written (D-165).
- **Files touched**: `block/CultivatorBlock.java` (`setPlacedBy`); `blockentity/CultivatorBlockEntity.java` (the `Core` cache field lands here, the NBT key `Core` is set up first, the state machine being F-235); `blockentity/OutpostCoreBlockEntity.java` (`addCultivator`); `outpost/OutpostCoreIndex.java` (the candidate core query, same convention as 0.1.1).
- **Concrete changes**: the candidate set = covers (reusing `inRange`) + allied + list not full; the comparison key follows D-165's two levels; the write = the core-side list + the cultivator-side `Core`; after writing, `setChanged()` + `sendBlockUpdated` (document 36 §2.6.1's sync timing).
- **Implementation notes**: **no runtime drift / periodic rebinding** — the write happens only at "placement" and "the core is torn down" (D-165 supplement, document 35 §12 B-13); when there is no core to bind to, the block is still placed and stays unbound (document 35 §12 B-12); cross-dimension does not hold (the candidate set comes from cores of the same dimension).
- **Easy-to-miss points**: ① using a Chebyshev or Manhattan distance instead of `distSqr` (D-165's original text is the three-dimensional Euclidean square); ② missing the second-level key on a tie ⇒ two runs of the same save give different answers (document 35 §8.2's worked example 4); ③ loading a chunk in order to "find the core" (only look at loaded chunks, `getChunkNow`); ④ when the two sides disagree, treat it as **unbound** (D-165), not as "the cultivator's cache wins".
- **Acceptance assertions**:
  1. Two cores both cover it, A is already full at 16 and B has a free slot: it binds to **B** (both sides' `/data get block` agree).
  2. Two cores placed in the same tick at the same distance: it binds to the one with the smaller `placedGameTime`; running the same save again gives the same result.
  3. No allied core nearby: after placement the cultivator **lacks the `Core` key** (unbound), and right-clicking does nothing (document 35 §12 B-12).
  4. Placing a core next to it afterwards: the `Core` key is **still missing** (no automatic claiming, document 35 §12 B-13).

#### F-233 · Migration's three exits: `settleOnRemoval` rewritten whole (L · 3.0 person-days | Depends on: F-232)

- **Goal**: when the core is torn down, rebind the 16 cultivators one by one; those with no successor core **stay in place, keep their contents and are marked unbound** (D-168, RT-43); there is **no "turn into dirt" branch**; this is also the moment the `.dat` file's last runtime reader disappears (A-69).
- **Files touched**: `blockentity/OutpostCoreBlockEntity.java:341-394` (rewritten whole, including `FarmlandLedger.of` at `:353`, `FarmlandGuard.selectCore` at `:363` and `setBlock(DIRT)` at `:387`); `blockentity/CultivatorBlockEntity.java` (receives the rebinding).
- **Concrete changes**: follow document 36 §2.7's seven steps — the real-removal test (`getChunkNow` + that position is no longer the core block) → copy the binding list before iterating → for each entry pick another core that "covers + is allied + has a free slot" → three exits (rebind / clear `Core` and mark unbound / skip defensively) → **clear only `cultivators`, not `items`** → `release()` closes; `setBlock` counts 0 in the method body.
- **Implementation notes**: no chunk is loaded for the migration (`getChunkNow` throughout); items are `onRemove`'s job (F-230), the two hooks dividing the work; exit ③ keeps the NBT as it is, guessing nothing and deleting no binding (the same direction as A-60 in document 34 D-168's supplement); `release()` is still the last line.
- **Easy-to-miss points**: ① the migration empties the slots (the reverse of document 35 §12 B-19); ② writing "turn into dirt" or "break the cultivator and drop its items" when it cannot migrate (RT-43, the same logic as document 34 §7 item 1); ③ changing the list while iterating it (document 36 §2.7 step 4 requires a copy first); ④ moving the 54-slot drop in here too (that is F-230's hook).
- **Acceptance assertions**:
  1. Core torn down + a successor core exists: the cultivator's `Core` points at the new core, `Held` / `Progress` unchanged (document 36 §8 #27).
  2. Core torn down + no successor core: the cultivator **stays in place**, the `Core` key disappears, `Held` / `Progress` are kept, and right-clicking does nothing (document 36 §8 #28).
  3. The cultivator's chunk is not in memory: the NBT is **not one byte different** (`Core` is still the old core's coordinates, document 36 §8 #29).
  4. `setBlock` has **0** hits inside `settleOnRemoval`'s method body (document 36 §8 #30); `FarmlandLedger` / `FarmlandGuard` references in this class (imports included) count **0**.

### Phase 37 · Cultivator Behaviour (F-234 ~ F-237)

Phase goal: turn the cultivator from "a block that can be placed" into a complete machine that "admits by tier, produces 2 + 2 every 600 ticks, pauses when the core is full and gives its contents back when broken". Exit criterion: Common / Rare can be inserted, Epic is refused, 600 ticks produce into the core, a full core pauses without loss, and breaking it drops the internal crop.

#### F-234 · The `cultivator` group's 2 keys + 2 language keys (S · 0.5 person-days | Depends on: F-224)

- **Goal**: add the top-level group `cultivator` with its 2 keys `cycleTicks` (600) / `yield` (2) (D-163) + 2 config language keys (document 36 §4.1.1 #4~#5, §4.3.2 #8~#9).
- **Files touched**: `config/FarmstormConfig.java` (the field block + the static block, a new `cultivator` section after the `outpost_core` section); the config section of `datagen/ModLangProvider.java` (+2 entries).
- **Concrete changes**: `cultivator.cycleTicks` defaults to **600** with range **20–72000**; `cultivator.yield` defaults to **2** with range **1–64** (document 36 §4.1.1's five range reasons); the two key names `farmstorm.configuration.cultivator.cycleTicks` / `.yield`.
- **Implementation notes**: the "2 seeds" half of the output **gets no config key of its own** (it follows the output rule, D-163, document 35 §6.3 discipline 2); `600 = 30 × 20` (30 seconds × 20 ticks/second).
- **Easy-to-miss points**: ① making "2 seeds" a key too ⇒ the key count becomes 176; ② writing `cycleTicks`' lower bound as 1 (the lower bound is 20, document 36 §4.1.1); ③ the group must be top-level (nesting would make the group count 61).
- **Acceptance assertions**:
  1. Delete the old toml and start the server: the `[cultivator]` section has exactly **2 keys**, values **600 / 2**.
  2. The whole file has **175 keys / 60 sections** (counted by `^\s*\[`; a toml interval reading, not the source-code ledger) — this is this version's final config value, and later tasks may not add or remove keys again.
  3. The two lang files each gain 2 `farmstorm.configuration.cultivator.*` entries.
  4. After `cycleTicks = 20` is set (once F-236 has landed), it produces once every 20 ticks.

#### F-235 · The state machine, the four admission gates and insertion (L · 3.0 person-days | Depends on: F-232, F-234)

- **Goal**: the cultivator's three NBT keys (`Held` / `Progress` / `Core`), the server-side per-tick state machine, the four admission gates and the insertion action (D-160, D-162, D-164).
- **Files touched**: `blockentity/CultivatorBlockEntity.java` (the three fields, `serverTick`, `isUsable`, `setWorking`); `block/CultivatorBlock.java` (`getTicker`'s server branch, `useWithoutItem`).
- **Concrete changes**: the state machine's four steps (empty slot → not usable → timing → complete / paused, document 36 §2.5.2); the four admission gates merged into one judgment (bound + in range + held tier + empty slot, document 36 §2.5.3); `useWithoutItem`'s order: client `SUCCESS` → any gate failing `PASS` → passing then `split(1)` + `CONSUME`; `working` writes the blockstate only when the value really changes (`UPDATE_CLIENTS`).
- **Implementation notes**: Epic **4 crops + 2 Epic seeds** are all refused (D-162), and admission judges **by `tier()`, not by an id whitelist**; right-clicking an occupied slot does nothing (no stacking, no replacing); right-clicking with an empty hand **does not take the item out** (document 35 §12 B-11); `isUsable` checks only the core-side list hit + `inRange` and **does not look at chunks** every tick (document 36 §2.7's last paragraph).
- **Easy-to-miss points**: ① judging admission by an id whitelist (it drifts the moment `tier` changes; D-162 explicitly rejects it); ② writing the blockstate once per tick (document 36 §2.5.2 requires writing only on change); ③ making `working` BE sync only and not going through the blockstate (D-161 explicitly requires the property); ④ an unbound cultivator must still be placeable and visible, merely unusable (D-165).
- **Acceptance assertions**:
  1. Insert a Common crop: `Held` has a value, `Progress` starts climbing from 0, and F3 shows `working=true`; the normal output path, outside a full core, is verified in F-236.
  2. Insert an Epic crop / 2 Epic seeds: **no reaction**, the held item is not consumed (document 36 §8 #23).
  3. Right-click an occupied slot again: no reaction; right-click an unbound cultivator: no reaction.
  4. Chunk unload / server restart and reload: `Progress` continues from its value and does not restart the timer (document 35 §12 B-17).

#### F-236 · Output and pausing when the core is full (L · 3.0 person-days | Depends on: F-226, F-235)

- **Goal**: settle once every 600 ticks, producing **2 crops** (plus **2 seeds** when the profile has a standalone seed item), delivered as one batch into the bound core; if the core cannot take it, **pause**, and continue from the pause point once space frees up (D-163, D-164).
- **Files touched**: `blockentity/CultivatorBlockEntity.java` (the completion branch and the batch assembly); `blockentity/OutpostCoreBlockEntity.java` (`canAccept` / `acceptAll`, built in F-226).
- **Concrete changes**: the output batch is decided by the profile (a standalone seed item ⇒ `[crop × yield, seed × yield]`, otherwise `[crop × yield]`); `acceptAll` succeeding ⇒ clear the slot + `Progress = 0` + `working=false`; failing ⇒ `Progress` stops at `cycleTicks`, `working=false`, and **nothing drops, nothing is lost** (document 36 §2.5.2 step 4).
- **Implementation notes**: the criterion for "has a standalone seed item" is `seedItem() != null` **and** it is not equal to `id()` (document 36 §2.5.4's boolean); the 8 profiles whose "seed = itself" therefore fall into "no standalone seed"; inside the admission surface **5 give 2+2 and 13 give 2 only** (`18 = 5 + 13`, document 35 §6.4); when what was inserted is a seed, the output is still decided by the **parent crop profile** (`chive_seeds` and `friend_chives` give the same result); "it fits" is judged **for the whole batch** (D-164 supplement).
- **Easy-to-miss points**: ① judging "has seeds" by `seedItem != null` alone ⇒ the 8 profiles whose seed is themselves double their output (document 36 §2.5.4 explicitly rejects it); ② restarting the timer when the core is full (D-164 requires continuing from the pause point, and `Progress` not being reset makes that automatic); ③ the output dropping as an `ItemEntity` or going into the player's inventory (the plan's original words are "into the core", D-164 explicitly rejects it); ④ judging "it fits" item by item (the whole batch is required).
- **Acceptance assertions**:
  1. Feed it `puffed_wheat`: after 600 ticks the core has **+2** crops + **+2** seeds (document 36 §8 #21).
  2. Feed it `ubi_server` (seed = itself): only **+2** crops, no seeds.
  3. Feed it `chive_seeds`: the output is **exactly the same** as feeding `friend_chives` (2 crops + 2 seeds).
  4. The core is filled up: `Progress == cycleTicks`, the items do not drop and are not lost; after clearing 2 slots the delivery completes automatically (document 36 §8 #22).

#### F-237 · Cultivator destruction drops (S · 0.5 person-days | Depends on: F-224)

- **Goal**: when the cultivator is broken it drops **itself + the internal crop**; a piston push does not drop a duplicate (D-160 supplement).
- **Files touched**: `block/CultivatorBlock.java` (`onRemove`); `datagen/ModLootTableProvider.java` (the loot table was built in F-225 and drops the block itself).
- **Concrete changes**: after the four conditions (not the same block + not moved by a piston), use the `Containers.dropContents(Level, BlockPos, NonNullList)` overload to drop the one internal item (document 36 §2.5.6: the block entity is **not** a `Container`, hence the `NonNullList` overload).
- **Implementation notes**: the block itself comes from the loot table, so the two do not overlap; on a piston push the contents travel with the block entity and are not duplicated on the ground.
- **Easy-to-miss points**: ① using the `Container` overload (the block entity does not implement `Container`, so it does not compile); ② missing `!movedByPiston` (duplication); ③ writing "drop itself" into `onRemove` too (duplicating the loot table, dropping 2).
- **Acceptance assertions**:
  1. Break it with a crop in the slot: **1** block + **1** of that crop drop (document 35 §12 B-10).
  2. Break it with an empty slot: only **1** block drops.
  3. Piston push: **0** drops.

### Phase 38 · Links (F-238 ~ F-240)

Phase goal: add the 2nd S2C read-only packet and the client-side wireframe, and draw the "core ↔ cultivator" binding. Exit criterion: standing beside one's own core shows the lines, walking away makes them disappear, breaking a block removes its line at once, the client stays online for a long time without crashing, and the server logic does not depend on the links.

#### F-238 · The link packet + registration + protocol version 2 (M · 1.5 person-days | Depends on: —)

- **Goal**: add the 2nd custom network packet `farmstorm:cultivator_link` (S2C read-only), and take the protocol version string `"1"` → `"2"` (D-169, D-172).
- **Files touched**: new `network/CultivatorLinkPayload.java`; `network/FarmstormNetwork.java:17` (`PROTOCOL_VERSION`), `:22-31` (the second `playToClient`), `:35-36` (the self-check log lines).
- **Concrete changes**: `Record Link(BlockPos core, BlockPos cultivator)` + `MAX_LINKS = 1024`; the `StreamCodec` per document 36 §2.8.1 (two `BlockPos.STREAM_CODEC`s composed + `ByteBufCodecs.list(MAX_LINKS)`); **no `total`, no dimension**; `.optional().playToClient(...)`; the self-check gains one line (or becomes two lines), printing both packet ids and the version string.
- **Implementation notes**: the protocol string must go from `"1"` to `"2"` (change the codec and it must follow, document 36 §2.8.1 and the comment at `FarmstormNetwork.java:16`); `MAX_LINKS = 1024 = 64 × 16` (`maxPerPlayer`'s upper bound × `cultivatorCapacity`'s upper bound); `core_list`'s existing registration and handler do not change by a character (do not extend it, D-169 supplement).
- **Easy-to-miss points**: ① forgetting the protocol string ⇒ a client with the new mod cannot hand-shake with an old server, or the reverse; ② the self-check still being 1 line (document 36 §8 #2's criterion is **two** lines); ③ stuffing the links into `core_list`'s codec (D-169 explicitly rejects it: a full snapshot and a range delta are two kinds of data).
- **Acceptance assertions**:
  1. `latest.log` shows **2** `自检 网络包注册` lines, the ids being `farmstorm:core_list` and `farmstorm:cultivator_link`, both with version string **2**.
  2. `playToClient` has **2** hits in `FarmstormNetwork.java`.
  3. A pure vanilla client can join (the optional channel); a dedicated server starts with no `NoClassDefFoundError` (document 36 §8 #2).
  4. `@Mixin` still has **0** hits across the repository.

#### F-239 · The link broadcaster: range and five timings (L · 3.0 person-days | Depends on: F-233, F-238)

- **Goal**: push the bindings inside the range to each player, and send an empty list when a player leaves the range so no stale line remains; debounce 10 ticks, rescan 20 ticks (D-169).
- **Files touched**: new `network/CultivatorLinkBroadcaster.java`; `event/FarmstormGameEvents.java:103`, `:120`, `:128` (the existing request points for login / dimension change / respawn), `:114` (clearing the record on logout); `blockentity/OutpostCoreBlockEntity.java` (one request added at the binding change and at `release()`).
- **Concrete changes**: the structure follows `CoreListBroadcaster` (the debounce convention, the `hasChannel` recipient test, `requestAll`); the range criterion reuses `OutpostCoreBlockEntity.inRange(corePos, playerBlockPos)`; the five timings follow document 36 §2.8.2's table (entering the range / leaving it / a binding change / the core being torn down / login, dimension change, respawn and logout).
- **Implementation notes**: **do not write a second range formula** (document 36 §2.8.2 explicitly requires reusing `inRange`, `RANGE_CHUNKS = 1`); leaving the range sends an **empty list** rather than nothing; `MAX_LINKS` is only a decode cap and does not truncate.
- **Easy-to-miss points**: ① not sending an empty packet on leaving the range ⇒ the client's lines hang forever (document 35 §12 B-38); ② missing one `requestAll` when the core is torn down (there is already one at the end of `release()`, and the link broadcaster must add one at the same place); ③ recomputing "is the player inside the 3×3 chunks" on its own (it would drift from `inRange`).
- **Acceptance assertions**:
  1. Standing beside one's own core (inside the 3×3 chunks): a packet with N links arrives, N = the number of cultivators bound to that core (document 36 §8 #31).
  2. Walking out of the range: an empty list arrives and the client's lines disappear (the second half of document 36 §8 #31).
  3. Breaking one cultivator: the corresponding line disappears **at once**, without waiting 20 ticks (document 36 §8 #32).
  4. Chained events in the same tick (placement + a binding change) send only **1** packet (debounce 10 ticks).

#### F-240 · Client read-only state and wireframe rendering (L · 3.0 person-days | Depends on: F-238)

- **Goal**: the client keeps the last received link list and renders it **read-only**; draw wireframes in world space (D-169, RT-45).
- **Files touched**: new `client/ClientCultivatorLinks.java`, new `client/CultivatorLinkRenderer.java`; `client/FarmstormClient.java` (where the render event and the handler are hooked up).
- **Concrete changes**: the handler only does `enqueueWork` (following `ClientCoreList`), clearing on a server switch / disconnect; rendering hooks `RenderLevelStageEvent`'s `Stage.AFTER_PARTICLES`; **create** a new `PoseStack` and `translate(-cam.x, -cam.y, -cam.z)`; use `LevelRenderer.renderLineBox` + `RenderType.lines()`; color RGB `0.30 / 0.85 / 0.90`, alpha `1.0`, line width 1.0; a 0.25-block end box at each end (document 36 §2.8.3's "[this document's decision]" 9).
- **Implementation notes**: the client is **read-only** in three respects (draw what was received, cache nothing across dimensions, an empty list really clears, D-169 supplement); the line width and the `RenderType` are not self-made (document 36 §2.8.3); the color reuses the same numbers as the core boundary particles (`OutpostCoreBlockEntity.java:63`) and invents no new color.
- **Easy-to-miss points**: ① hand-writing vertices without the `NORMAL` element ⇒ `Missing elements in vertex` and the client crashes (document 36 §7 RT-45); ② modifying the `getPoseStack()` the event hands over ⇒ translating every later stage along with it; ③ not clearing the cache after a dimension change (a `clear()` is needed); ④ calling a rendering class on the server side (a dedicated server gets `NoClassDefFoundError`).
- **Acceptance assertions**:
  1. Standing beside one's own core: a **visible wireframe** runs between the core and each bound cultivator, with a 0.25-block box at each end (document 36 §8 #31).
  2. Walking away: the lines disappear; changing dimension: the residue is cleared (document 35 §12 B-38).
  3. The client online continuously for 10 minutes: **0** `Missing elements in vertex` / `IllegalStateException` in `latest.log` (document 36 §8 #33).
  4. After temporarily making the handler a no-op, binding / output / migration / core-break drops are **all as usual** (document 36 §8 #34, the server does not depend on the links).

### Phase 39 · Closing and Full Acceptance (F-241 ~ F-246)

Phase goal: level the three totals (337 / 175 / 97), run the 36 acceptance entries and the 40 boundaries to the end, and sign the red line and upstream consistency. Exit criterion: the two lang files each hold 337 keys with identical key sets, `farmstorm-server.toml` has 175 keys / 60 sections, the registry surface is 97, `@Mixin` is 0, there are 2 network packets, all 36 and 40 entries pass, and the four upstream documents' `git diff` is empty.

#### F-241 · The language-key ledger at 337 and the class comment (M · 1.5 person-days | Depends on: F-215, F-221, F-222, F-224, F-226, F-227, F-231, F-234)

- **Goal**: the two language files each hold **337** keys with exactly identical key sets, and `ModLangProvider`'s entry-composition comment is synced (document 36 §4.3.3).
- **Files touched**: `datagen/ModLangProvider.java:13-16` (the class comment's entry-composition section); `src/generated/resources/assets/farmstorm/lang/zh_cn.json`, `en_us.json` (re-run `runData`).
- **Concrete changes**: re-run datagen; the class comment becomes `33 item + 27 block + 4 effect + 3 subtitles + 178 config (= 175 server + 3 client) + 36 advancement + 6 death + 20 tooltip + 13 entity + 9 gui + 2 container + 1 itemGroup + 0 message + 2 key + 3 command = 337` (document 36 §4.3.3's prefix-by-prefix table).
- **Implementation notes**: `178 = 169 − 1 + 10` (1 farmland config key deleted, 10 added); `tooltip` from 16 → 20 (+4 tier-related); `container` from 1 → 2; `message` from 2 → 0. Chinese and English sharing the same keys is guaranteed by construction (the same `ENTRIES` table written to two columns), so zh-only / en-only is necessarily 0.
- **Easy-to-miss points**: ① the 169 → 178 in the comment is the easiest to miss (config includes 3 CLIENT keys); ② `message.farmstorm` goes to 0 but its prefix line must not vanish into thin air (a comment that lists by prefix must write `+ 0 message`); ③ changing only the json and not the comment means the next person recomputes from the old comment; ④ `ModLangProvider.java:10`'s "299 keys (v0.0.3 basis)" is a historical sentence and is not changed.
- **Acceptance assertions**:
  1. The two json files each hold **337** keys with exactly identical key sets (`Compare-Object` gives an empty result) (document 36 §8's convention).
  2. Prefix by prefix: `farmstorm.configuration` **178** / `item` **33** / `block` **27** / `container` **2** / `tooltip` **20** / `message` **0**, and the other 9 buckets unchanged.
  3. `323` has **0** hits in `ModLangProvider.java`; `337` ≥ **1**.

#### F-242 · The config ledger 175 keys / 60 groups (M · 1.5 person-days | Depends on: F-220, F-222, F-226, F-231, F-234)

- **Goal**: after `farmstorm-server.toml` is regenerated it holds **175 keys / 60 groups**, `[outpost_core]` has 4 keys and **no** `farmlandBonus`, `[cultivator]` has 2 keys and `[trade]` 6 keys (document 36 §4.1).
- **Files touched**: `run/config/farmstorm-server.toml` (deleted and regenerated); `config/FarmstormConfig.java` (checked group by group, no value changed again).
- **Concrete changes**: delete the old server toml, start the server once so it regenerates, then check group by group against document 36 §4.1's 36-group table: `outpost_core 4`, `cultivator 2`, `trade 6`, and the other 33 groups have the same key counts as 0.1.1.
- **Implementation notes**: `166 − 1 + 2 + 2 + 6 = 175`; `58 + 2 = 60` (two new top-level groups `cultivator` and `trade`; the `outpost_core` section is still there, not a delete-and-recreate of the group).
- **Easy-to-miss points**: ① **without deleting the old toml** `farmlandBonus` stays in the file as an orphan key (NeoForge does not actively delete unknown keys), and document 36 §8 #11's criterion only holds under **delete and regenerate**; ② count the groups by `^\s*\[` (not `^\[`, the same pit as A-52); ③ the 3 CLIENT keys are **not counted into** 175, so do not add them; ④ do not count `outpost_core`'s 4 keys as 3 (`maxPerPlayer` / `boundaryParticleIntervalTicks` / `cultivatorCapacity` / `storageSlots`).
- **Acceptance assertions**:
  1. `farmstorm-server.toml` has **175 keys / 60 sections**, `[outpost_core]` has **4 keys** and no `farmlandBonus` (document 36 §8 #11).
  2. `[cultivator]` **2 keys** (600 / 2), `[trade]` **6 keys** (2 / 5 / 10 / 32 / 40 / 64).
  3. `farmstorm-client.toml` still has **3 keys** (`hud.enabled` / `hud.x` / `hud.y`).
  4. `FarmstormConfig.java`'s `b.push(` count = **60**.

#### F-243 · The verification tool synced to 97 / 175 / 337 (S · 0.5 person-days | Depends on: F-228, F-241, F-242)

- **Goal**: sync `tools/verify_project.py`'s expectation surface to v0.2.0, otherwise it will **silently check 4 registered objects fewer** (A-66).
- **Files touched**: `tools/verify_project.py:258-272` (`EXPECTED_COUNTS`), its 12 classes' `EXPECTED` lists (`:190-256`), `:139-183` (after `expected_config_keys_v011`), `:283-296` (the union of the five old documents), `:362-374` (the printing of `total / 93`).
- **Concrete changes**: add the new ids to each of `EXPECTED`'s 4 classes (`cultivator` ×3, the `outpost_core` menu); change 5 numbers in `EXPECTED_COUNTS` (97 / 33 / 27 / 8 / 2); add `outpost_core.farmlandBonus` to `REMOVED_KEYS`; the expected key set **may only be computed live from documents 34 / 36** (add a segment reading document 36 §4.1.1's 11 rows), and the 10 new keys **may not** be copied into a second list; if document parsing fails, let the tool report an error and exit and the task stops there; the printing string `total / 93` → `total / 97`.
- **Implementation notes**: the tool's criterion is "list length = expected count" + "every id appears somewhere in the whole source tree", so **changing only the counts and not the lists** goes red at once; the config expectation comes from five **old documents**, and without adding document 36 the 10 new keys would be reported as "in the toml but not in the documents".
- **Easy-to-miss points**: ① changing only `EXPECTED_COUNTS` and not `EXPECTED`; ② adding only the ids and not changing the printing string (still `93`); ③ hard-coding the new keys into a second list (the degeneration the tool most fears, the same discipline as A-40 in document 31 §5); ④ the tool's §4 prohibition scan has a "source placeholder marker" regex whose scope is `.java` under `src/` — this file is not in it and is unaffected.
- **Acceptance assertions**:
  1. `python -X utf8 tools/verify_project.py` exits with code 0 and prints `已核 registry 对象: 97 / 97`.
  2. In the same output the config section says `175 个配置键与文档逐字一致` and the lang section `共 337 键`.
  3. The live criterion: temporarily change `EXPECTED_COUNTS["blocks"]` to 28 and run — the exit code is not 0; after restoring it returns to 0.
  4. `git diff --stat docs/` is empty (the tool only reads upstream and never writes it back).

#### F-244 · Document 36 §8's 36 acceptance entries (L · 3.0 person-days | Depends on: F-215 ~ F-240, see §3)

- **Goal**: execute document 36 §8's **36** acceptance entries one by one and record them, with 0 failing.
- **Files touched**: new record `.work/v020-verify/acceptance.md`; screenshots / recordings go into `.work/v020-verify/`.
- **Concrete changes**: in the order #1 ~ #36, each entry written as "the criterion verbatim / where observed / the actual value / pass", the expected value **taking document 36 §8's original text as the authority** (not restated here); #6~#8, #18~#20, #23~#26, #31~#34 are manual scenarios, one screenshot each.
- **Implementation notes**: #1 (`→ OK`) must be run after F-228; #14 (the `.dat` mtime unchanged) must be run after F-233 (A-69); #21~#22 need a "nearly full core" setup; #35's pool assertion is the same log line as F-216's.
- **Easy-to-miss points**: ① writing #1's expected value as the window's FAIL (taking an old log as the result); ② using a save from before F-233 to verify #14; ③ recording only "pass" without the actual value (nothing can be re-checked); ④ mixing the 36 entries and the 40 boundaries into one record (the boundaries are F-245).
- **Acceptance assertions**:
  1. The record has **36** lines (+ the header), `未通过 = 0`.
  2. Every line's "where observed" and "actual value" columns are non-empty.
  3. #1's log text is **verbatim identical** to document 36 §8 #1's string (including `→ OK` and the 12 items).
  4. The manual scenarios' screenshot count ≥ **13** (#6~#8 three, #18~#20 three, #23~#26 four, #31~#34 four).

#### F-245 · Document 35 §12's 40 boundary re-runs (L · 3.0 person-days | Depends on: F-244)

- **Goal**: re-run document 35 §12's **B-01 ~ B-40** one by one, with 0 non-conforming.
- **Files touched**: new record `.work/v020-verify/boundaries.md`.
- **Concrete changes**: in B-number order, each written as "scenario / expected behaviour / measured / conclusion"; B-05 ~ B-07 are command and legacy-save scenarios, B-26 ~ B-27 drop scenarios, B-33 ~ B-34 trade sampling, and B-40 is "restoring the comment revives it at once".
- **Implementation notes**: B-33 (a top-level farmer in a legacy save gets no re-issue) and B-34 (trades are sampling) are **accepted costs**, verified by "take a top-level farmer from a 0.1.1 legacy save and see that it has no new entry", and **are not judged failures** (document 36 §2.4.2); B-40 is verified by restoring the comment on a **temporary working copy**, then reverting after the check, and it must not be committed.
- **Easy-to-miss points**: ① judging B-33 a defect; ② judging B-34 as "this villager is broken"; ③ using a save from before F-233 to verify B-20 / B-21; ④ verifying only one of B-26's three no-drop scenarios (piston / same block / unload).
- **Acceptance assertions**:
  1. The record has **40** lines (+ the header), `不符 = 0`.
  2. B-12 / B-13 / B-15 / B-16 / B-19 / B-20 each have an actual NBT value from `/data get block` (the presence or absence of the `Core` key is the key point).
  3. B-26 / B-27 each record the actual drop list (an empty list must say "empty", never be left blank).
  4. B-40's record states "restored on a temporary copy, reverted after the check, `git status` has no residue".

#### F-246 · Red line, registry surface and upstream-consistency sign-off (M · 1.5 person-days | Depends on: F-243, F-244, F-245)

- **Goal**: sign off the 8 closing criteria one by one (D-172, A-62 ~ A-69's landing check), and confirm that the four upstream documents are not changed by a character.
- **Files touched**: new record `.work/v020-verify/signoff.md`; reads only `docs/zh/34` ~ `docs/zh/37`, `src/`.
- **Concrete changes**: an 8-row sign-off table — ① registry surface 97 (the 12 classes' items); ② config 175 keys / 60 groups; ③ language keys 337 × 2; ④ action surface 46/46 (table A); ⑤ `@Mixin` 0, `.playToClient` 2, protocol string `2`; ⑥ `Farmstorm.java`'s 5 self-check numbers; ⑦ `farmstorm_farmland.dat` still there and its mtime unchanged (after F-233); ⑧ `FarmlandLedger` 555 lines / `FarmlandGuard` 225 lines unchanged character for character.
- **Implementation notes**: the red line's "no cap on network packets" **does not mean** "add them freely" — this version has only 1 new packet (D-172); the two retained classes' **not one line changed** is A-62 / RT-42's hard constraint, and the line count is its checkable form.
- **Easy-to-miss points**: ① verifying the `.dat`'s "mtime unchanged" before F-233 (A-69); ② writing "protocol string 2" as "version 2.0"; ③ missing the sign-off on "the four upstream documents' `git diff` is empty" — this file's premise is that it only reads upstream; ④ counting the 46 rows as 47 (the off-table items are not rows, see the note under table A in §6).
- **Acceptance assertions**:
  1. `git diff --stat docs/zh/34-DECISIONS-0.2.0.md docs/zh/35-DESIGN-0.2.0.md docs/zh/36-TECH-0.2.0.md` is **empty**.
  2. `@Mixin` has **0** hits across the repository; `playToClient` has **2** hits in `FarmstormNetwork.java`; `PROTOCOL_VERSION = "2"`.
  3. One startup log and three counts hold at once: `97` / `175 keys 60 sections` / `337 keys × 2`, and they are verbatim identical to F-243's three output lines (`已核 registry 对象: 97 / 97`, `175 个配置键与文档逐字一致`, `共 337 键`).
  4. `FarmlandLedger.java` **555** lines, `FarmlandGuard.java` **225** lines (unchanged character for character); `world/data/farmstorm_farmland.dat` exists and its mtime is the same as before the upgrade.
  5. All 8 rows of the sign-off table are green, and every row in the record gives "criterion / measured value / conclusion".

---

## 3. Dependency Graph and Parallelism

### 3.1 Seven Dependency Chains (sketch)

The diagram draws only the **predecessor → successor** trunk and does not try to draw every edge (F-241 actually waits for 8 predecessors, F-244 for 26):

```
Chain 1 (version and rules):    F-213      (no predecessor, doable on day one)
Chain 2 (tier):                 F-214 → F-215 ─┐
                                  └→ F-216     ├→ F-241 → F-243
Chain 3 (retirement and trade): F-220 → F-221  │
                                  └→ F-222 → F-223
Chain 4 (core):                 F-226 → F-227 → F-228 ─┐
                                  ├→ F-229             ├→ F-242 → F-243
                                  ├→ F-230             │
                                  └→ F-231 → F-232 → F-233 → F-235 → F-236
Chain 5 (cultivator):           F-224 → F-225          │
                                  ├→ F-234 ────────────┘
                                  ├→ F-237
                                  └→ F-232 (see chain 4)
Chain 6 (links):                F-238 → F-239 (also needs F-233)
                                  └→ F-240
Closing:                        F-244 → F-245 → F-246 (F-244 waits for F-215 ~ F-240 to all land)
```

**Three ways to read the diagram**:

1. **The only hard serial segment is F-231 → F-232 → F-233 → F-235 → F-236** (binding → binding on placement → migration → state machine → output). The order cannot be swapped: the state machine's `isUsable` reads the core-side list (F-231), the output calls `acceptAll` (F-226), and the migration rewrites the binding (F-232's write form).
2. **F-224 and F-226 are two mutually independent starting points**: the cultivator block lands first (registration + resources), and the core's container starts another branch (container → menu → interaction → drops). The two meet only at F-232 (binding on placement).
3. **F-239 has to wait for F-233**: the link's "binding change" timing includes migration (rebinding), and while the migration has not landed the broadcaster is one trigger short, leaving a hole where "the line still hangs after the core is torn down" (document 35 §12 B-39).

### 3.2 Critical Path

Taking the longest predecessor per dependency (each node takes the maximum cumulative completion time of all its predecessors):

```
F-226 core becomes a container  3.0   (cumulative 3.0)
 └─ F-231 binding list          1.5   (cumulative 4.5)
     └─ F-232 binding on placement 3.0 (cumulative 7.5, also needs F-224's 3.0; take the larger, 7.5)
         └─ F-235 state machine 3.0   (cumulative 10.5, also needs F-234's 3.5)
             └─ F-236 output    3.0   (cumulative 13.5)
                 └─ F-244 acceptance 3.0 (cumulative 16.5)
                     └─ F-245 boundaries 3.0 (cumulative 19.5)
                         └─ F-246 sign-off 1.5 (cumulative 21.0)
```

**Critical path = 21.0 person-days / 8 tasks**, **34.4%** of the 61.0 total. This chain is the result of a pure dependency derivation.

**A tied second 21.0**: `F-226 → F-231 → F-232 → F-233 → F-239 → F-244 → F-245 → F-246` (`3.0 + 1.5 + 3.0 + 3.0 + 3.0 + 3.0 + 3.0 + 1.5 = 21.0`). It forks after F-232 into "migration → links" instead of "state machine → output", and both reach 13.5 before F-244 — when scheduling, treat F-233 and F-235 as the same layer.

The next-longest chains (again taking the longest predecessor per dependency, not entering the critical path):

| Rank | Chain | Person-days |
|---|---|---|
| 3 | F-224 → F-234 → F-235 → F-236 → F-244 → F-245 → F-246 | 3.0 + 0.5 + 3.0 + 3.0 + 3.0 + 3.0 + 1.5 = **17.0** |
| 4 | F-226 → F-227 → F-228 → F-229 → F-244 → F-245 → F-246 | 3.0 + 3.0 + 0.5 + 1.5 + 3.0 + 3.0 + 1.5 = **15.5** |
| 5 | F-220 → F-222 → F-223 → F-244 → F-245 → F-246 | 1.5 + 1.5 + 3.0 + 3.0 + 3.0 + 1.5 = **13.5** |
| 6 | F-238 → F-239 → F-244 → F-245 → F-246 | 1.5 + 3.0 + 3.0 + 3.0 + 1.5 = **12.0** (F-239 is really gated by F-233's 10.5) |

> **How to read it**: the first four chains all converge on F-244, so **every L before F-244 is parallel**; to shorten the calendar duration, squeeze `F-231 → F-232 → F-235 → F-236` first (4 of the 6 tasks are L, 10.5 person-days in all), not the acceptance (however hard you squeeze it, acceptance cannot go below the 6.0 person-days of F-244 + F-245).

### 3.3 Parallel Clusters

| Cluster | Phase | Parallelizable tasks | Parallelism | Notes |
|---|---|---|---|---|
| A | 32 ~ 34 | F-213, F-214, F-217, F-218, F-219, F-220 | 6 | the six share no file (F-214 and F-216 are serial; F-220 → F-221 / F-222 are serial) |
| B | 35 | F-224 → F-225 and F-226 → F-227 → F-228 | 2 | the cultivator line and the core line do not depend on each other and meet only before F-228 (the window closes) |
| C | 36 | F-229, F-230, F-231 → F-232 → F-233 | 3 | three branches: menu interaction, drops, binding |
| D | 37 | F-234, F-237 and F-235 → F-236 | 2 | config and drops can fill gaps; the state machine and the output are serial by nature |
| E | 38 | F-238 → F-239 and F-238 → F-240 | 2 | the data face goes first; the broadcaster and the renderer can run two people in parallel |
| F | 39 | F-241, F-242, F-243 | 3 | the three ledgers share no file; F-244 / F-245 / F-246 close in turn |

**The parallelism ceiling is 6** (cluster A), in practice constrained by **same-file conflicts**: F-214 / F-216 both change `CropProfile.java` (serial); F-220 / F-222 / F-226 / F-231 / F-234 all change `FarmstormConfig.java` (serial in the order 220 → 222 → 226 → 231 → 234); F-224 / F-232 / F-235 / F-237 all change `CultivatorBlock.java` / `CultivatorBlockEntity.java` (serial); F-215 / F-221 / F-222 / F-224 / F-226 / F-227 / F-231 / F-234 / F-241 all change `ModLangProvider.java` (each appends its own segment in Phase order, F-241 closing at the end). Under the convention "one task appends only its own segment", **3 lanes** can run in parallel reliably.

---

## 4. Workload Summary

### 4.1 By Phase and Milestone

| Phase | Name | Tasks | Task count | S | M | L | Person-days | Milestone |
|---|---|---|---|---|---|---|---|---|
| 32 | Baseline and the Tier Carrier | F-213 ~ F-215 | 3 | 1 | 1 | 1 | `0.5 + 1.5 + 3.0` = **5.0** | **M28** |
| 33 | Narrowing the Rule Surface | F-216 ~ F-219 | 4 | 4 | 0 | 0 | `4 × 0.5` = **2.0** | **M29** |
| 34 | Retiring Farmland and the Trades | F-220 ~ F-223 | 4 | 1 | 2 | 1 | `0.5 + 2 × 1.5 + 3.0` = **6.5** | **M30** |
| 35 | Registry Surface, Resources and the Container Skeleton | F-224 ~ F-228 | 5 | 1 | 0 | 4 | `0.5 + 4 × 3.0` = **12.5** | **M31** |
| 36 | Core Interaction, Binding and Migration | F-229 ~ F-233 | 5 | 1 | 2 | 2 | `0.5 + 2 × 1.5 + 2 × 3.0` = **9.5** | **M32** |
| 37 | Cultivator Behaviour | F-234 ~ F-237 | 4 | 2 | 0 | 2 | `2 × 0.5 + 2 × 3.0` = **7.0** | **M33** |
| 38 | Links | F-238 ~ F-240 | 3 | 0 | 1 | 2 | `1.5 + 2 × 3.0` = **7.5** | **M34** |
| 39 | Closing and Full Acceptance | F-241 ~ F-246 | 6 | 1 | 3 | 2 | `0.5 + 3 × 1.5 + 2 × 3.0` = **11.0** | **M35** |
| **Total** | | **F-213 ~ F-246** | **34** | **11** | **9** | **14** | **61.0** | **M28 ~ M35** |

### 4.2 Totals (Two Numbers + the Arithmetic)

- **Raw person-days**: `11 × 0.5 + 9 × 1.5 + 14 × 3.0 = 5.5 + 13.5 + 42.0 = **61.0 person-days**`
- **With a 25% buffer**: `61.0 × 1.25 = **76.25 person-days**`

Three cross-checks:

1. Complexity summary: `11 + 9 + 14 = 34` = the task count;
2. Person-day summary: `5.5 + 13.5 + 42.0 = 61.0` = the sum of the eight Phase subtotals `5.0 + 2.0 + 6.5 + 12.5 + 9.5 + 7.0 + 7.5 + 11.0 = 61.0`;
3. Buffer: `61.0 × 1.25 = 76.25`.

> The buffer absorbs rework and debugging only, not scope change. Compared with 0.1.1 (37 tasks / 65.5 raw person-days), this version has 3 fewer tasks and 4.5 fewer raw person-days: the change comes from two places — this version has 4 pure S tasks (Phase 33) and 3 S closing items, while the cut farmland trio "write the binding + backfill + quota" was 3 M tasks in 0.1.1; the price is that L's share rises from 27% to 41% (container, menu, migration and the state machine are all L).

### 4.3 Calendar Duration After Parallelism

- **Single serial**: `76.25 person-days`, about **15.3 weeks** at 5 days/week.
- **A stable 3 lanes** (§3.3's last paragraph): `61.0 ÷ 3 ≈ 20.3` raw person-days, `20.3 × 1.25 ≈ **25.4 person-days** ≈ **5.1 weeks** after the buffer (@ 5 days/week).
- **The critical path's lower bound**: `21.0 person-days` (§3.2); even with unlimited people it does not go below that — **this chain cannot be squeezed**: container → binding → binding on placement → state machine → output → acceptance → sign-off is a serial relation of "first the data structure, then the state machine, then the acceptance", and squeezing any link leaves the three later acceptance batches with no criterion at all.
- **The gap to the 3 lanes**: `25.4 − 21.0 = 4.4 person-days`, i.e. after 3 lanes only 4.4 person-days of slack remain; to go faster one can only add people to the L tasks **off** the critical path (F-225 resources, F-240 rendering, F-244 / F-245 acceptance).

---

## 5. Adjudication Ledger (from A-62)

This section collects **8 entries**: A-62 is named by item 7 of document 36 §10 and aligned with document 36 §10's two hints to document 37; A-63 ~ A-69 are newly established while this document was written. Each entry gives: conclusion / basis / landing place / status.

### 5.1 A-62 · `farmlandBonus`: comment out the definition only, keep the field declaration

- **Conclusion**: the field declaration at `FarmstormConfig.java:227` is **kept as it is**; only the `.defineInRange(...)` definition at `:909-912` is commented out; the two `.get()` calls at `FarmlandGuard.java:75` and `FarmlandLedger.java:305` are **kept as they are** (unreachable at runtime).
- **Basis**: item 7 of document 36 §10 (the only **substantive** conflict that needs the author's ruling; item 1 of document 36 §10's two hints); document 34 D-153's "comment out the reads" and "not one line changed in the two classes" are mutually exclusive; the two read points really are in those two classes (`FarmlandGuard.java:75`, `FarmlandLedger.java:305`, source checked). Document 34 §3.3 line 5 registers "the field and the definition" together as retired, and document 35 §7.2 landing 6 also writes "field, definition → comment out" — **both wordings are back-filled by this entry**.
- **Reason (for keeping the declaration)**: keeping the declaration lets the two dead classes still compile — `FarmlandGuard` / `FarmlandLedger` are a "not one line changed" hard constraint (author's ruling #3, RT-42), and deleting the field would stop them compiling, which amounts to changing them by the back door.
- **Cost**: the field is `null` at all times now; if someone in future restores only the listeners `:75` / `:76` and not the definition, the first till gets a `NullPointerException` instead of the default value — this is exactly the concrete shape of RT-42's "wrong restoration". **Mitigation**: write two comment lines beside `:227` (the definition is commented out ⇒ the field is null ⇒ the definition must be restored before the listeners), and one line "裁定 #3：v0.2.0 撤销耕地，勿恢复" in the comments at `:75` / `:76`.
- **Rejected**: ① changing 1 line in each of those two classes (deleting the `.get()`) — violates D-153 and author's ruling #3's "not one line of the body"; ② turning it into the hard-coded constant 16 — that amounts to quietly keeping the farmland quota, in direct conflict with D-153's "the quota concept disappears altogether" (document 34 §6 D-139's row).
- **Landing place**: **F-220** (the implementation and the comments), **F-242** (`[outpost_core]` has no such key), **F-244** (document 36 §8 #12's acceptance). Back-fill targets: document 34 §3.3 line 5, document 35 §7.2 landing 6.
- **Status**: **landed** (this version's wording; A-62 is entry one).

### 5.2 A-63 · The Lucky Berry pool's 11 entries must be derived from `tier()`, not written as a second id list

- **Conclusion**: `LUCKY_BERRY_POOL` goes from "exclude 3" to "keep only `tier() == COMMON`", and **may not** be written as a hand-written list of 11 `CropProfile` references.
- **Basis**: the source `CropProfile.java:139-143` is already a filter construction (not a literal list); D-156 requires "the 11 entries equal exactly the Common tier's whole set"; document 36 §7 RT-49's mitigation (a) requires "the 22 define the tier in one place and all three readers read the same `tier()`". A hand-written list would make "tier" and "pool" two truths: after one tier changes the pool does not follow, and RT-49's defence fails on the spot.
- **Landing place**: **F-216** (the pool's construction and comment), **F-244** (document 36 §8 #35's acceptance is written as "all 11 are `COMMON`", not as an id enumeration).
- **Status**: **landed** (newly established by this document).

### 5.3 A-64 · The cultivator's creative-tab entry must be added; the 46 rows of document 36 §3 do not include it

- **Conclusion**: `FarmstormItems.creativeTabItems()` (`:122-129`) must gain `CULTIVATOR`.
- **Basis**: `:122-129` is an **explicit list** (crops → sowing items → the three tool blocks); an item registered but not in this list does not appear in the creative tab. And this version has **no** cultivator recipe (document 36 §5's recipes are still 29 and none of the item-by-item names produces a cultivator), nor any survival-mode acquisition path ⇒ not adding it means "registered but unobtainable". Document 36 §2.9 used the same reasoning for the converter ("a decorative block must be obtainable"), and the same reasoning must apply to a new block.
- **Landing place**: **F-224** (the creative-tab line), **F-244** (acceptance: one more entry at the end of the creative tab's segment).
- **Status**: **landed** (newly established by this document; the 46 rows of document 36 §3 do not move and this entry adds no row).

### 5.4 A-65 · The jar names in the two `.cmd` scripts must change with the version number; D-148's landing places miss them

- **Conclusion**: the `farmstorm-0.1.1+mc1.21.1[.jar|-sources.jar]` hard-coded in `一键构建.cmd:11-12` and `检查环境.cmd:14` must change to `0.2.0`.
- **Basis**: document 34 D-148's landing places list only `gradle.properties:11`, the six documents and `README.md`, and **not** these two scripts; the 46 rows of document 36 §3 have no script row either. Yet in the 0.1.1 cycle these two places were already hard-coded to the 0.1.1 jar names (`一键构建.cmd:11-12`, `检查环境.cmd:14`, measured) ⇒ changing only `mod_version` would make `一键构建.cmd`'s `copy` copy a non-existent file and `检查环境.cmd` report "jar: 缺失".
- **Landing place**: **F-213** (the three literals), **F-244** (the artifact-face acceptance).
- **Status**: **landed** (newly established by this document; the action **does not enter** table A's 46 rows, for the same reason as A-64).

### 5.5 A-66 · `tools/verify_project.py`'s expectation surface must be synced, or it silently checks 4 objects fewer

- **Conclusion**: `EXPECTED` (the 12 classes' id whitelists), `EXPECTED_COUNTS` (the 93 baseline), the config union of the five old documents, and the `total / 93` printing string — all four places must be synced to 97 / 175 / 337.
- **Basis**: document 36 §1.2 says "`tools/verify_project.py`'s `EXPECTED_COUNTS` after the change = 97", but neither its §3 (46 rows) nor its §8 (36 entries) leaves room for reworking the tool. The tool's 2nd section is "every id in the list appears in the source tree + the list length = `EXPECTED_COUNTS`", and **it does not look at extra registered objects at all** — so during v0.2.0 it would still print `已核 registry 对象: 93 / 93` and exit 0, which is more dangerous than going red (A-40 in document 31 §5 is the same class of problem, but that version went red; this one passes silently). The config section is the same: its expectation set comes from the five old documents `01 / 12 / 18 / 24 / 30`, and the 10 new keys would be reported as "in the toml but not in the documents".
- **Landing place**: **F-243** (the four syncs + the live criterion), **F-246** (the sign-off cites the tool's three output lines).
- **Status**: **landed** (newly established by this document).

### 5.6 A-67 · Author's rulings #17 ~ #19 get no D numbers: document 37 issues only A numbers

- **Conclusion**: this document always cites "author's ruling #17 / #18 / #19" and **does not** open new D numbers for them; the correspondence is written as "author's ruling #17 + D-166", "author's ruling #18 + D-157", "author's ruling #19 + D-170". If the D numbers are to be registered, document 34 issues them when back-filling (this version's D numbers still stop at D-172).
- **Basis**: document 34 §0.1's numbering rule 1 ("this document issues only D-xx ... when citing other series write the full prefix, do not number them yourself, do not pre-spend downstream numbers") and rule 4 ("this document establishes no new adjudications: the few places in the writing-period report that need the author's ruling are registered by document 37 from A-62 on"); document 34 §2 note 2 hands this matter to document 37 as well. Yet item 15 of document 36 §10 requires "the D numbers are to be registered by document 37 after A-62" — which conflicts with document 34's numbering discipline: document 37's number range is **A-xx**, not D-xx.
- **Landing place**: the "Decisions" column throughout this document (F-229 writes "author's ruling #17", F-223 writes "author's ruling #18 + D-157", F-219 writes "author's ruling #19 + D-170"); back-fill targets: document 34 §0.1 / §2 note 2 (add "rulings #17~#19 issue no separate D numbers") and item 15 of document 36 §10.
- **Status**: **landed** (newly established by this document; document 34 is not changed).

### 5.7 A-68 · Document 34 gives `FarmlandLedger`'s on-disk-name line number as `:44`, while it is `:53`

- **Conclusion**: the constant of `farmstorm_farmland`'s on-disk name is at `FarmlandLedger.java:53` (`DATA_NAME`), `:55` is the label name of `dataVersion`, and `:44` is a javadoc paragraph about `coreDimension`. This document and the later acceptance always write `:53`.
- **Basis**: document 34 D-154's "basis" and §3.3 line 7 both write `:44`; document 36 §6① writes `:53`; the source measures `:53` (the javadoc of `:42-53` through to the definition of `DATA_NAME`). Document 34 §9 does not register this item.
- **Landing place**: **F-220**'s "Files touched" (which does not cite `:44`), the `.dat`-related check in **F-246**'s sign-off; back-fill targets: document 34 D-154 and §3.3 line 7.
- **Status**: **landed** (newly established by this document; document 34 is not changed).

### 5.8 A-69 · D-154's "no reads, no writes" is a criterion that holds only after F-233

- **Conclusion**: the last runtime reader of `farmstorm_farmland.dat` is `OutpostCoreBlockEntity#settleOnRemoval` (`FarmlandLedger.of(server)` at `:353`, and the `migrate` / `remove` calls at `:384` / `:388` / `:391` which `setDirty`). After F-220 comments out the two listeners and the one backfill line, **a core teardown still reads and rewrites this file** until F-233 rewrites the whole block. Therefore the criterion "the file is still there and its mtime is unchanged" can only be executed **after F-233**.
- **Basis**: the source `FarmlandLedger.java:104` (`of`), `:114-121` (`setDirty` after `bind`), `:131` / `:144` / `:180` (`setDirty` after `migrate` / `remove`), `OutpostCoreBlockEntity.java:353`; D-154's "not read, not written, not deleted" and document 36 §8 #14's criterion.
- **Landing place**: **F-220**'s easy-to-miss points (no mtime assertion before F-233), **F-233**'s goal (the last reader disappears), **F-244**'s #14 execution time, **F-246**'s sign-off item 7.
- **Status**: **landed** (newly established by this document; it changes no decision, only the **time** of the acceptance).

---

## 6. Coverage Mapping Tables (six)

Tables A ~ F correspond to document 36 §3 / §1.2 / §4 / §4.3 / §7 / §9. The last row of each table gives "covered / total".

### Table A · The 46 action-surface rows of document 36 §3 → tasks

| Row | Category | Path or class#method | Add / change / delete | Responsible tasks |
|---|---|---|---|---|
| 1 | registration | `registry/FarmstormBlocks.java` 26 → 27 | add | F-224 |
| 2 | registration | `registry/FarmstormBlockEntities.java` 7 → 8 | add | F-224 |
| 3 | registration | `registry/FarmstormItems.java` 32 → 33 | add | F-224 |
| 4 | registration | `registry/FarmstormMenus.java` 1 → 2 | add | F-227 |
| 5 | registration | the other 8 classes (`entity_types` / `mob_effects` / `sound_events` / `recipe_types` / `recipe_serializers` / `creative_tabs` / `damage_types` / `data_components`) unchanged | check | F-228 (per-class reading), F-246 (sign-off) |
| 6 | class | `util/CropTier.java` | add | F-214 |
| 7 | class | `util/CropProfile.java:32-40` (9 fields) | change | F-214 |
| 8 | data | `util/CropProfile.java:47-128` (22 tiers) | change | F-214 |
| 9 | data | `util/CropProfile.java:139-143` (the pool's 11 entries) | change | F-216 |
| 10 | class | `item/ModifiedCropItem#getName(ItemStack)` | change | F-215 |
| 11 | class | `item/DeployableCropItem#getName(ItemStack)` | change | F-215 |
| 12 | class | `item/SeedItem#getName(ItemStack)` | change | F-215 |
| 13 | class | `item/ModifiedCropItem#appendCropTooltip` (`:113-127`) | change | F-215 |
| 14 | class | `item/ModifiedCropItem#isPlantableSoil` (`:101-107`) | change | F-217 |
| 15 | class | `item/SeedItem#useOn` | change | F-218 |
| 16 | class | `block/FarmstormCropBlock` (untouched) | check | F-217 (negative check), F-246 |
| 17 | retire | `event/FarmstormGameEvents.java:75` | retire | F-220 |
| 18 | retire | `event/FarmstormGameEvents.java:76` | retire | F-220 |
| 19 | retire | `event/FarmstormGameEvents.java:241-265` | retire | F-220 |
| 20 | retire | `event/FarmstormGameEvents.java:277-281` | retire | F-220 |
| 21 | retire | `event/FarmstormGameEvents.java:171` | retire | F-220 |
| 22 | retire | `config/FarmstormConfig.java:909-912` (`:227` kept) | retire | F-220, F-242 |
| 23 | retire | `datagen/ModLangProvider.java:403` | retire | F-221 |
| 24 | retire | `datagen/ModLangProvider.java:412-413` | retire | F-221 |
| 25 | data | `farmland/FarmlandLedger.java` / `FarmlandGuard.java` (not one line changed) | check | F-220 (negative registration), F-246 |
| 26 | data | `farmstorm_farmland.dat` (not read / not written / not deleted) | retire | F-220 (the code face), F-244 (#14, time per A-69) |
| 27 | class | `event/VillagerTradeEvents.java` | add | F-223 |
| 28 | class | `event/TierTrade.java` | add | F-223 |
| 29 | data | the 3 trade entries (levels 1/3/5, `maxUses = 1`, 2 per trade) | add | F-223 |
| 30 | class | `block/CultivatorBlock.java` | add | F-224 (skeleton), F-235 (admission), F-232 (placement) |
| 31 | class | `blockentity/CultivatorBlockEntity.java` | add | F-224 (skeleton), F-235 (state machine), F-237 (drops) |
| 32 | data | cultivator cycle / output / pausing on a full core | add | F-234 (config), F-236 (behaviour) |
| 33 | class | `blockentity/OutpostCoreBlockEntity.java:48` (`implements Container`) | change | F-226 |
| 34 | data | `OutpostCoreBlockEntity` NBT (`:411-424`) `+Items` `+Cultivators` | change | F-226 (`Items`), F-231 (`Cultivators`) |
| 35 | class | `OutpostCoreBlockEntity#getUpdateTag` | add | F-226 |
| 36 | class | `OutpostCoreBlockEntity#getUpdatePacket` | add | F-226 |
| 37 | class | `OutpostCoreBlockEntity#settleOnRemoval` (`:341-394`) | change | F-233 |
| 38 | class | `block/OutpostCoreBlock#useWithoutItem` | add | F-229 |
| 39 | class | `block/OutpostCoreBlock#onRemove` (`:101-107`) | change | F-230 |
| 40 | class | `menu/OutpostCoreMenu.java` + `client/OutpostCoreScreen.java` | add | F-227 |
| 41 | class | `network/CultivatorLinkPayload.java` | add | F-238 |
| 42 | class | `network/CultivatorLinkBroadcaster.java` + `client/ClientCultivatorLinks.java` + `client/CultivatorLinkRenderer.java` | add | F-239 (broadcaster), F-240 (the client's 2 classes) |
| 43 | class | `network/FarmstormNetwork.java:17,29-31` (protocol `"1"` → `"2"`) | change | F-238 |
| 44 | retire | `block/ConverterBlock.java:78-86` | retire | F-219 |
| 45 | config | `config/FarmstormConfig.java` 166 keys / 58 groups → 175 / 60 | change | F-220 (−1), F-222 (+6), F-226 (+1), F-231 (+1), F-234 (+2), F-242 (the ledger) |
| 46 | data | `Farmstorm.java:119` (the pool assertion) / `:147-150` (the registry assertion) | change | F-216 (the pool half), F-228 (the registry half) |

**46/46 covered, 0 uncovered**. Category counts: registration 5 rows (#1~#5), class 23 rows (#6, #7, #10~#16, #27, #28, #30, #31, #33, #35~#43), data 8 rows (#8, #9, #25, #26, #29, #32, #34, #46), retire 9 rows (#17~#24, #44), config 1 row (#45) — identical item by item to the category counts below the table of document 36 §3.

**Two notes below the table**:

1. **The 4 off-table action-surface items** (**not counted into the 46 rows**, so they take no row number): ① the version number at `gradle.properties:11` (F-213, A-65); ② the jar names in the two `.cmd` scripts (F-213, A-65); ③ the creative-tab entry in `FarmstormItems.creativeTabItems()` (F-224, A-64); ④ the expectation surface of `tools/verify_project.py` (F-243, A-66). All four are actions "not included in document 36 §3 but fatal if left undone", each with an adjudication behind it, and they **do not** stretch table A to 50 rows.
2. **Rows split across several tasks** (#26, #30, #31, #32, #34, #42, #45, #46) are all divided by "who touches this segment", and every segment has its own acceptance, so reverting any one of those tasks does not affect the other segments.

### Table B · Registry surface +4 (97) → tasks

**B.1 The 12 classes one by one** (baseline 93, named from the source by document 36 §1.2 itself)

| # | registry | Baseline | This version's increment | After | Landing task | Checking task |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 32 | **+1** (`cultivator`'s BlockItem) | **33** | F-224 | F-228 |
| 2 | `Registries.BLOCK` | 26 | **+1** (`cultivator`) | **27** | F-224 | F-228 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 7 | **+1** (`cultivator`) | **8** | F-224 | F-228 |
| 4 | `Registries.MENU` | 1 | **+1** (`outpost_core`'s storage screen) | **2** | F-227 | F-228 |
| 5 | `Registries.ENTITY_TYPE` | 13 | +0 | 13 | — | F-228 |
| 6 | `Registries.MOB_EFFECT` | 4 | +0 | 4 | — | F-228 |
| 7 | `Registries.SOUND_EVENT` | 3 | +0 | 3 | — | F-228 |
| 8 | `Registries.RECIPE_TYPE` | 1 | +0 | 1 | — | F-228 |
| 9 | `Registries.RECIPE_SERIALIZER` | 1 | +0 | 1 | — | F-228 |
| 10 | `Registries.CREATIVE_MODE_TAB` | 1 | +0 | 1 | — | F-228 |
| 11 | `Registries.DAMAGE_TYPE` | 3 | +0 | 3 | — | F-228 |
| 12 | `Registries.DATA_COMPONENT_TYPE` | 1 | +0 (the tier goes through a static field, D-150) | 1 | — | F-228 |
| **Total** | | **93** | **+4** | **97** | | `33+27+8+2+13+4+3+1+1+1+3+1 = 97` |

**B.2 The 4 new ids named one by one** (all enter `tools/verify_project.py`'s `EXPECTED`, F-243)

| # | id | Category | Landing task |
|---|---|---|---|
| 1 | `farmstorm:cultivator` (block) | block | F-224 |
| 2 | `farmstorm:cultivator` (block entity type) | block_entity_type | F-224 |
| 3 | `farmstorm:cultivator` (BlockItem) | item | F-224 |
| 4 | `farmstorm:outpost_core` (MenuType) | menu_type | F-227 |

**B.3 The five components "added but not entering the registry surface"** (everything new besides the +4)

| # | Component | Why it does not enter the registry | Task |
|---|---|---|---|
| 1 | the 2nd network packet (`farmstorm:cultivator_link`) | the payload goes through `RegisterPayloadHandlersEvent` and does not enter `BuiltInRegistries` (D-172, document 34 §5.3 item 4) | F-238 |
| 2 | the `CropTier` enum and the 9th field | static fields, not registered (D-150) | F-214 |
| 3 | the 3 farmer trade entries | they go through NeoForge events, not registration (D-157) | F-223 |
| 4 | the core's 16-entry binding list | block-entity NBT, not registered (D-165) | F-231 |
| 5 | the 54-slot container | a `Container` interface implementation, not registered (D-166) | F-226 |

### Table C · Config 175 keys / 60 groups → tasks (10 added / 1 deleted)

| # | Key | Type | Default | Range | Action | Key-landing task | Language-key task |
|---|---|---|---|---|---|---|---|
| 1 | `outpost_core.farmlandBonus` | int | ~~16~~ | ~~0–1024~~ | **delete** (the definition is only commented out, the field is kept, A-62) | F-220 | F-221 |
| 2 | `outpost_core.cultivatorCapacity` | int | **16** | 1–64 | add | F-231 | F-231 |
| 3 | `outpost_core.storageSlots` | int | **54** | 54–54 (fallback see document 36 §9 ⑦) | add | F-226 | F-226 |
| 4 | `cultivator.cycleTicks` | int | **600** | 20–72000 | add (new group) | F-234 | F-234 |
| 5 | `cultivator.yield` | int | **2** | 1–64 | add (new group) | F-234 | F-234 |
| 6 | `trade.common.min` | int | **2** | 1–64 | add (new group) | F-222 | F-222 |
| 7 | `trade.common.max` | int | **5** | 1–64 | add | F-222 | F-222 |
| 8 | `trade.rare.min` | int | **10** | 1–64 | add | F-222 | F-222 |
| 9 | `trade.rare.max` | int | **32** | 1–64 | add | F-222 | F-222 |
| 10 | `trade.epic.min` | int | **40** | 1–64 | add | F-222 | F-222 |
| 11 | `trade.epic.max` | int | **64** | 1–64 | add | F-222 | F-222 |
| **Total** | **11 rows = 1 deleted + 10 added** | | | | | **`166 − 1 + 10 = 175`** | 10 language keys |

**Three checks**:

1. **Group count**: `58 + 1 (trade) + 1 (cultivator) = 60`; `outpost_core` **gains keys rather than being deleted and re-created** (3 → 4 keys), document 36 §4.1.2.
2. **Intermediate key counts** (each segment is countable from `farmstorm-server.toml`): after F-220 **165 / 58** → after F-222 **171 / 59** → after F-226 **172 / 59** → after F-231 **173 / 59** → after F-234 **175 / 60**.
3. **The 3 CLIENT keys do not move** (`hud.enabled` / `hud.x` / `hud.y`), are **not counted into** 175, but do count into the language keys' `farmstorm.configuration` (document 36 §4.2 / §4.3).

### Table D · Language keys 337 → tasks (3 deleted / 17 added)

| # | Key | Action | Landing task |
|---|---|---|---|
| 1 | `farmstorm.configuration.outpost_core.farmlandBonus` | **delete** | F-221 |
| 2 | `message.farmstorm.farmland.no_core` | **delete** | F-221 |
| 3 | `message.farmstorm.farmland.quota_full` | **delete** | F-221 |
| 4 | `item.farmstorm.cultivator` | add | F-224 |
| 5 | `block.farmstorm.cultivator` | add | F-224 |
| 6 | `container.farmstorm.outpost_core` | add | F-227 |
| 7 | `tooltip.farmstorm.tier.common` | add | F-215 |
| 8 | `tooltip.farmstorm.tier.rare` | add | F-215 |
| 9 | `tooltip.farmstorm.tier.epic` | add | F-215 |
| 10 | `tooltip.farmstorm.tier` | add | F-215 |
| 11 | `farmstorm.configuration.cultivator.cycleTicks` | add | F-234 |
| 12 | `farmstorm.configuration.cultivator.yield` | add | F-234 |
| 13 | `farmstorm.configuration.outpost_core.cultivatorCapacity` | add | F-231 |
| 14 | `farmstorm.configuration.outpost_core.storageSlots` | add | F-226 |
| 15 | `farmstorm.configuration.trade.common.min` | add | F-222 |
| 16 | `farmstorm.configuration.trade.common.max` | add | F-222 |
| 17 | `farmstorm.configuration.trade.rare.min` | add | F-222 |
| 18 | `farmstorm.configuration.trade.rare.max` | add | F-222 |
| 19 | `farmstorm.configuration.trade.epic.min` | add | F-222 |
| 20 | `farmstorm.configuration.trade.epic.max` | add | F-222 |
| **Total** | **20 rows = 3 deleted + 17 added** | | **`323 − 3 + 17 = 337`** (the ledger is in F-241) |

**Prefix-by-prefix check** (after the change, the same table as document 36 §4.3.3): `farmstorm.configuration 169 → 178` (−1 +10), `item 32 → 33`, `block 26 → 27`, `container 1 → 2`, `tooltip 16 → 20`, `message 2 → 0`, and the other 9 buckets unchanged; `178 + 36 + 33 + 27 + 20 + 13 + 9 + 6 + 4 + 3 + 3 + 0 + 2 + 1 + 2 = 337`.

### Table E · Risks RT-42 ~ RT-50 → tasks

| ID | Risk (document 36 §7) | Task responsible for verification | How that task verifies it |
|---|---|---|---|
| **RT-42** | retiring the farmland is "commenting out the call sites", not deleting code; a wrong restoration revives the old rules | F-220, F-233, F-246 | F-220 checks that the 5 comment-outs each carry a reason line + the two NPE lines at `:227`; F-233 checks that the two classes' references inside `OutpostCoreBlockEntity` fall to 0; F-246 checks that the two classes' line counts 555 / 225 are unchanged character for character |
| **RT-43** | `settleOnRemoval` is rewritten whole, and the three exits' semantics are redefined | F-233, F-245 | F-233 checks the three exits + `setBlock` at 0 hits; F-245 runs B-19 / B-20 / B-21 one by one (rebind / stay in place marked unbound / keep the old value when the chunk cannot be read) |
| **RT-44** | the core gains `Container` + 16 bindings + 54 slots, so NBT size and sync must be recomputed and legacy saves need compatibility | F-226, F-227, F-230, F-231 | F-226 checks that `getUpdateTag` writes only three keys + a legacy save reads as 54 empty slots; F-227 checks that the slot count equals `getContainerSize()`; F-230 checks the four-condition drops; F-231 checks that the binding list does not enter `getUpdateTag` |
| **RT-45** | wireframe rendering for the first time in the project (0 precedents of `RenderType.lines`) | F-238, F-239, F-240 | F-238 checks protocol string 2 and the two self-check lines; F-239 checks the range and the empty packet clearing residue; F-240 checks `renderLineBox` + 0 `Missing elements in vertex` in 10 minutes |
| **RT-46** | villager trades for the first time in the project (0 lines of villager code) | F-222, F-223, F-245 | F-222 checks the 6 keys' ranges; F-223 checks the level slots 1/3/5 and `maxUses = 1` and the price falling in range; F-245 runs B-30 ~ B-34 one by one (out of stock / restock / sampling / no re-issue for legacy saves) |
| **RT-47** | animated textures for the first time in the project (0 mod `.png.mcmeta`) | F-225, F-244 | F-225 checks 2 mcmeta + 2 variants + `_still` carrying no mcmeta; F-244 checks document 36 §8 #18 ~ #20 (two states / mcmeta / an empty slot not moving) |
| **RT-48** | cutting planting has two paths, and closing only one leaks | F-217, F-218, F-245 | F-217 and F-218 **one acceptance each, not merged**; F-245 runs B-01 ~ B-04 (15 crops / 7 deployable / 7 seeds / creative mode) |
| **RT-49** | the tier is a new concept, and the 22 tiers must be consistent one by one | F-214, F-216, F-223, F-235 | F-214 checks the per-tier counts 11/7/4; F-216 checks the pool = the whole Common set; F-223 checks the three tiers' sampling pools; F-235 checks the admission surface 18 = 11 + 7; F-244's #3 compares document 34 §4.1 item by item |
| **RT-50** | the pool goes 19 → 11, so the assertion at `Farmstorm.java:119` goes red (it only writes a log, it does not crash) | F-216, F-244 | F-216 **pool and assertion in the same commit**, checking `11 种…→ OK` and that the "does not contain the three" half is kept; F-244's #35 compares the log verbatim |
| **Total** | **9 entries** | | **9/9, 0 uncovered** |

### Table F · APIs to verify, 13 entries → tasks

| # | API / semantics to verify (document 36 §9) | Confirmed by which task at which step | Fallback when it cannot be confirmed |
|---|---|---|---|
| ① | the mutability of `VillagerTradesEvent`'s list and the order during a reload | **F-223 first step**: hook only one entry first and start the server to see it in the villager list; then change it to three | the list is immutable ⇒ `event.getTrades().put(level, new ArrayList<>(旧 + 新))` (document 36 §9 ①'s fallback, same wording) |
| ② | whether `RenderType.lines()` is drawable in the `AFTER_PARTICLES` stage | **F-240 first step**: draw one line at fixed coordinates first (not depending on the packet) and see whether it is visible | the stage is unusable ⇒ fall back to `AFTER_TRIPWIRE_BLOCKS`; if the `bufferSource` has already been flushed ⇒ capture the matrix in `AFTER_ENTITIES` and redraw in `RenderGuiEvent` (document 36 §9 ②'s fallback) |
| ③ | mcmeta animation + static-frame dual textures at the blockstate layer | **F-225 first step**: after `runData` look at `blockstates/cultivator.json`'s two states, then go in game and see whether the idle state is still | frame flicker on the switch ⇒ use static images for both models and a separate transition image for the working state; if animation only at `working=true` does not hold ⇒ go back to document 34 (not done in this version) |
| ④ | NBT compatibility after the core gains `Container`, and the size of `getUpdateTag` | **F-226 first step**: put a core into a legacy save → it reads as 54 empty slots; store 3 items → they are still there after a restart | if one place on the client really needs the items ⇒ put only "the number of non-empty slots + the first slot's item" into `getUpdateTag` (≤ 200 bytes) |
| ⑤ | the detection timing of "pause output" (judged once at completion) | **F-236 first step**: fill the core up → see `Progress == cycleTicks` and not moving | if a 1-second delay in resuming is unacceptable ⇒ add one "wake the bound cultivators" callback in the core's `setItem` / `removeItem` (the judgment timing does not move) |
| ⑥ | the implementation of `CropTier`'s name color (`getName` + `withStyle`) | **F-215 first step**: take one Common / Rare / Epic each and look at the colors in the inventory | if an anvil rename loses the tier color ⇒ wrap one more layer in `ItemTooltipEvent`; if the rarity wrapper overrides it ⇒ have `getName` return `Component.empty().append(带色文本)` |
| ⑦ | whether `ModConfigSpec` accepts `min == max` (`storageSlots` 54–54) | **F-226 first step**: start the server and see whether config loading reports an error | rejected ⇒ change to `54..55` + `Math.min(54, 读值)` when the menu is created (document 36 §9 ⑦'s fallback, the slot count unchanged) |
| ⑧ | `quickMoveStack`'s boundaries with 54 + 36 slots | **F-227 first step**: after opening the screen, `Shift+左键` items in both directions | boundaries wrong ⇒ rewrite after vanilla `ChestMenu`'s two segments (0–53 / 54–89), with `Slot#mayPlace` as the fallback |
| ⑨ | the reachability of right-clicking the cultivator while holding a block | **F-235 first step**: right-click the cultivator while holding a block and confirm that placement is what happens | no fallback (vanilla `ServerPlayerGameMode`'s order); write "a held block cannot be inserted" into the player-visible notes |
| ⑩ | `Containers.dropContents(Level, BlockPos, NonNullList)` working with the cultivator | **F-237 first step**: break it with a crop in the slot and look at where the drops land | if the position is poor ⇒ switch to `Containers.dropItemStack(...)` and drop item by item |
| ⑪ | the effect of `MerchantOffer`'s `priceMultiplier = 0.0F` on vanilla discounts | **F-223 second step**: after a trade, check repeatedly whether the base price still equals the offer | if there is a side effect ⇒ change to `0.05F` (the base price may go +1, and the interval's upper bound is still caught by `clamp`) |
| ⑫ | whether `setBlockUpdated` on a binding change is enough for the overlay / links to refresh | **F-232 first step**: after placement / migration see whether the overlay and the links change at once | if they do not refresh ⇒ call `CoreListBroadcaster.requestAll(server)` once more at the three change points |
| ⑬ | whether the self-check format string still lines up after 97 (13 parameters) | **F-228 first step**: start the server, read the whole line and compare it item by item with document 36 §8 #1's string | if the parameters are misaligned ⇒ adjust only the argument order, never the format string; acceptance always goes by **content grep** (`自检 注册对象 97 个`) rather than by line number |
| **Total** | **13 entries** | **13/13 land on a concrete task and a concrete step** | **13/13 give an executable fallback** |

---

## 7. Coverage Self-Check (this document's own acceptance)

| # | Check item | Criterion | Result |
|---|---|---|---|
| 1 | Task numbers contiguous, no gap | `grep -c "^#### F-"` = **34**; the range `F-213 ~ F-246` ⇒ `246 − 213 + 1 = 34` | pass |
| 2 | Phase and milestone contiguous | Phase `32 ~ 39`, **8** in all; milestones `M28 ~ M35`, **8** in all, one-to-one (§4.1's table) | pass |
| 3 | Adjudication numbers | A-62 (named by document 36) + A-63 ~ A-69, **8** in all, contiguous with no gap; A-62 is the first and sits at §5.1 | pass |
| 4 | Table A coverage | the **46 rows** of document 36 §3, not one missing, **0** uncovered; the category counts (registration 5 / class 23 / data 8 / retire 9 / config 1) identical item by item to the counts below document 36 §3's table | **46/46** |
| 5 | Table B coverage | the 12 classes one by one (93 → 97 in total); the 4 new ids named one by one; the 5 components outside the registry surface each have a task | **21 items** (12 + 4 + 5) |
| 6 | Table C coverage | 11 rows (1 deleted + 10 added); `166 − 1 + 10 = 175`; groups `58 + 2 = 60`; the five intermediate key counts are self-consistent | **11 rows** |
| 7 | Table D coverage | 20 rows (3 deleted + 17 added); `323 − 3 + 17 = 337`; prefix-by-prefix sum = 337 | **20 rows** |
| 8 | Table E coverage | RT-42 ~ RT-50, **9** entries, each with at least one task responsible for verification and given a "how to verify" | **9/9** |
| 9 | Table F coverage | document 36 §9's **13** entries, each landing on "a concrete step of a concrete task" + "a concrete fallback", with no "watch out during implementation" | **13/13** |
| 10 | Every task has ≥ 1 observable acceptance item | read the 34 tasks one by one: all have assertions falling on `latest.log` / `*.toml` / `src/generated` / `/data get` / in-game / a `grep` count; none says "behaves normally" or "feels good" | pass |
| 11 | Every task has a dependency | §1's "Depends on" column + the tasks' title lines' `Depends on`, 34 / 34 consistent; of which **9 have no predecessor (`—`)**: F-213, F-214, F-217, F-218, F-219, F-220, F-224, F-226, F-238 | pass |
| 12 | No cycles, numbers increase | every task's dependency numbers are smaller than its own; ordering by Phase is a topological order with no back edge | pass |
| 13 | Workload self-consistent | `11 × 0.5 + 9 × 1.5 + 14 × 3.0 = 61.0`; the eight Phase subtotals sum to 61.0; `61.0 × 1.25 = 76.25`; `S + M + L = 11 + 9 + 14 = 34` | pass |
| 14 | In-between window self-consistent | window = F-224 → F-228; the three segments' numbers `93 / 96 / 97` line up item by item with the breakdowns (items 33, blocks 27, BE 8, menu 1 → 2); the acceptances of the four tasks F-224 ~ F-227 **all exclude** "`自检 注册对象 … → OK`", and the only one writing OK is the closing task F-228 | pass |
| 15 | No invented numbers | 97 / 175 / 337 / 46 / 36 / 40 / 25 / 22 / 13 / 9 / 11 / 7 / 4 / 18 / 5 / 19 / 600 / 54 / 16 / 2 / 3 all trace back to documents 34, 35, 36 or their arithmetic; no new gameplay value is invented | pass |
| 16 | Forbidden words and tone | no undecided-item marker and no to-do placeholder anywhere; the person is always "the author" / "the implementer", never a second-person honorific; conclusion sentences are always imperative | pass |
| 17 | Off-table items do not stretch table A | table A is exactly 46 rows; the 4 off-table actions (version number / the `.cmd` scripts / the creative tab / the tool) are written in the note below the table and each has an adjudication (A-64 / A-65 / A-66) | pass |
| 18 | Only one file created | beyond this document no file changes: `docs/zh/34` ~ `36`, `docs/en`, `src/` and `.work/` are not changed by a character | pass |

---

## Appendix A Task ↔ Decision ↔ Acceptance Three-Way Index

The "document 36 §8" column gives the acceptance numbers this task is directly responsible for; the "document 35 §12" column gives the boundary numbers it is directly responsible for; `—` means no direct counterpart (the verification comes from a count, an artifact or the tool).

| Task | Decisions | Document 36 §8 | Document 35 §12 | Risk |
|---|---|---|---|---|
| F-213 | D-148, A-65 | — (the artifact face) | — | — |
| F-214 | D-149, D-150 | #3, #4 | B-37 | RT-49 |
| F-215 | D-151 | — (name color / tooltip manual check) | B-36 | RT-49 |
| F-216 | D-156, A-63 | #35 | B-28, B-29 | RT-50 |
| F-217 | D-152 | #6, #8 | B-01, B-02, B-05 | RT-48 |
| F-218 | D-152 | #7 | B-03 | RT-48 |
| F-219 | D-170, author's ruling #19 | #36 | B-35 | — |
| F-220 | D-153, D-154, A-62, A-69 | #9, #10, #12, #14 | B-40 | RT-42 |
| F-221 | D-153 | #13 | — | — |
| F-222 | D-159 | #17 (the carrier) | — | RT-46 |
| F-223 | D-157, D-158, D-159, author's ruling #18 | #15, #16, #17 | B-30 ~ B-34 | RT-46 |
| F-224 | D-160, D-171, A-64 | #1 (the window opens) | B-10, B-12 | RT-44, RT-47 |
| F-225 | D-160, D-161 | #18, #19 | — | RT-47 |
| F-226 | D-166 | #24 (the precondition), #26 (the container face) | B-24, B-25 | RT-44 |
| F-227 | D-166, D-171 | #24 (the screen face) | B-24 | RT-44 |
| F-228 | D-171 | #1 (the window closes) | — | — |
| F-229 | D-166, author's ruling #17 | #24, #25 | B-22, B-23 | RT-44 |
| F-230 | D-167 | #26 | B-26, B-27 | RT-44 |
| F-231 | D-165 | #21 (the precondition) | B-14 | RT-44 |
| F-232 | D-165 | #27 (the precondition) | B-12, B-13, B-14 | RT-44 |
| F-233 | D-168, A-69 | #27, #28, #29, #30 | B-19, B-20, B-21 | RT-43 |
| F-234 | D-163 | — (the config face; §8 has no dedicated entry) | — | — |
| F-235 | D-160, D-162, D-164 | #20, #23 | B-08, B-09, B-11, B-17 | RT-44, RT-47 |
| F-236 | D-163, D-164 | #21, #22 | B-15, B-16, B-18 | RT-44 |
| F-237 | D-160 supplement | — (asserted inside the task) | B-10 | — |
| F-238 | D-169, D-172 | #2 | — | RT-45 |
| F-239 | D-169 | #31, #32 | B-38, B-39 | RT-45 |
| F-240 | D-169 | #31, #33, #34 | B-38, B-39 | RT-45 |
| F-241 | document 36 §4.3 | #13 (the key-set face) | — | — |
| F-242 | document 36 §4.1, D-153 | #11 | — | — |
| F-243 | A-66, document 36 §1.2 | — (the tool face) | — | — |
| F-244 | document 36 §8, all 36 entries | #1 ~ #36 | (the B face is F-245's) | all 9 entries |
| F-245 | document 35 §12, all 40 entries, A-69 | — | B-01 ~ B-40 (**40 entries, one-to-one**) | all 9 entries |
| F-246 | D-172, A-62 ~ A-69 | #1, #2, #14 (the sign-off face) | — | RT-42, RT-45 |

Three reconciliation notes:

1. **Decision coverage**: D-148 ~ D-172, 25 entries, each appearing at least once in some task's "Decisions" column; D-166 appears most (F-226, F-227, F-229, F-244 — four places), because it governs the container, the menu, the access condition and legacy compatibility at once.
2. **Acceptance coverage**: document 36 §8's #1 ~ #36 are fully covered by F-244 (#3, #4 etc. are first verified once in the implementation task's own acceptance, which is "one layer earlier" and does not replace F-244).
3. **Boundary coverage**: document 35 §12's B-01 ~ B-40 are fully covered by F-245 (40 entries, one-to-one); 23 of them are already asserted at a single point in implementation tasks' acceptance, and the remaining 17 (B-04, B-06, B-07, B-18, B-22, B-23, B-25, B-28, B-29, B-31, B-32, B-33, B-34, B-36, B-37, B-40 and half of B-05) are re-run only in F-245 — they either depend on constructed scenarios such as legacy saves / commands / sampling, or are "should not happen" negative criteria.
