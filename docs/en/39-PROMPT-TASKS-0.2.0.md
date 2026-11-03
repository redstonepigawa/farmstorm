# 39 · v0.2.0 Phase-by-Phase Task List (Phase Prompts)

> How to feed it: **feed the master prompt (document 38) once first**, then **when a Phase starts, feed that Phase's section only**; feed only one section at a time. Every section is executable on its own from beginning to end — hand one section to the implementation agent alone, and it needs neither the previous section nor any other section of this file.

| Item | Value |
|---|---|
| Document number | 39-PROMPT-TASKS |
| Version | v0.2.0 (MC 1.21.1 / NeoForge 21.1.213 / Java 21) |
| Readers | the person feeding a coding agent + the CLI coding agent being fed |
| Upstream | `37-TASKS-0.2.0.md` (**the only source of F-213 ~ F-246**: numbers, titles, Phase ownership, complexity, person-days, dependencies), `36-TECH-0.2.0.md` (the 97 of the registry surface in §1.2, the 175 / 60 of config in §4.1, the 337 language keys in §4.3, the 36 acceptance entries in §8, the 13 to-be-verified APIs in §9), `35-DESIGN-0.2.0.md` (11 requirements, B-01 ~ B-40), `34-DECISIONS-0.2.0.md` (D-148 ~ D-172, 19 rulings) |
| Peer | `38-PROMPT-MAIN-0.2.0.md` (the resident master prompt, fed once only). **This file writes it into no dependency**: every section carries the five lines of §0.2, so a section can be fed alone without document 38 |
| Content | **8 Phases / 34 tasks (F-213 ~ F-246)**; every section has the same six subsections: Preconditions / Goal of this phase / Task list / Exit criteria / Pitfalls / Reporting format |
| Exit criteria | **66 entries** across the 8 sections (7 ~ 10 per section), all starting with `- [ ]`. A criterion only proves "this section's own tasks" and adds no new acceptance dimension; the full wording is in §8: **36** entries of document 36 §8 + **40** entries of document 35 §12 = **76** entries |
| Boundary | read the upstream only: change none of `docs/zh/34` ~ `docs/zh/37`, nothing under `docs/en/`, nothing in `src/`, none of the three ledgers under `.work/`; create this file only |

---

## 0. Feeding order and constraints

### 0.1 The feed order of the eight sections (Phase 32 → 39) and each section's preconditions

| Order | Which section to feed | Phase · milestone | Tasks | Person-days | Preconditions of this section (do not open unless every one holds) |
|---|---|---|---|---|---|
| 1 | §1 | 32 · **M28** | F-213 ~ F-215 | 5.0 | the work tree is clean (`git status` shows only the 10 files this section changes); `.tools\gw.cmd build` exits 0; the baseline is **the released 0.1.1 state** (`src/` has 0 changes since the `v0.1.1` tag, D-148). **This section is the first Phase and has no previous section** |
| 2 | §2 | 33 · **M29** | F-216 ~ F-219 | 2.0 | **entries 1 / 3 / 4** of §1.4 are green (the artifact name 0.2.0, all 7 constructions carrying `CropTier`, per tier 11 / 7 / 4); the log already carries `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` |
| 3 | §3 | 34 · **M30** | F-220 ~ F-223 | 6.5 | **entries 1 / 3 / 5** of §2.4 are green (the pool 11 in the same commit as the assertion, each of the two planting paths blocked once); `tier()` is readable (F-214 landed) — trade draws by tier, and without `tier()` it cannot land |
| 4 | §4 | 35 · **M31** | F-224 ~ F-228 | 12.5 | **entries 1 / 6** of §3.4 are green (each of the five comments carries its reason line, the 3 farmland keys have 0 hits in both lang files); `[outpost_core]` no longer has `farmlandBonus`. **The registry self-check window opens here**: when work starts `total` is still 93 / `→ OK` |
| 5 | §5 | 36 · **M32** | F-229 ~ F-233 | 9.5 | **entries 1 / 5 / 6 / 7** of §4.4 are green (state A 96 / the container face / state B 97 / the close-out `97 … → OK`); `menu_types 2` and `block_entities 8` are in place |
| 6 | §6 | 37 · **M33** | F-234 ~ F-237 | 7.0 | **entries 3 / 4** of §5.4 are green (`Cultivators` on disk, the four criteria of placement binding) — F-232 is F-235's precondition; the toml reading of the 4 `[outpost_core]` keys has been taken |
| 7 | §7 | 38 · **M34** | F-238 ~ F-240 | 7.5 | **entry 5** of §5.4 is green (the three exits of migration) — F-233 is F-239's precondition; a single-player client and a dedicated server can be started at the same time; a second client is available for the "not your own" comparison and for the range check |
| 8 | §8 | 39 · **M35** | F-241 ~ F-246 | 11.0 | **every** exit criterion of §1 ~ §7 is green, of which §6.4 entries 4 / 5 and §7.4 entries 5 / 6 are the body of the 36 acceptance entries; the three lagging windows reach their close-out point at the same time (the registry is already closed, the language keys close in this section, config closes in this section) |
| 9 | close-out | not inside any Phase | — | — | every exit criterion of §8 passes: **76** entries (36 + 40) and **34 tasks** all have a conclusion |

Three notes on reading the table:

1. **The order is the feed order, not the only executable order.** `§4` holds two starting lines that do not depend on each other (the F-224 cultivator line / the F-226 core line) and may go to two people; but **feed one section at a time**, and do only the F-xx of the section being fed — never mix work across sections.
2. **`§6` must not be moved before `§5`**: `isUsable` has to read the core-side list (F-231), the yield has to call `acceptAll` (F-226), and migration has to re-bind (the write shape of F-232). Miss any one of the three and the cultivator only idles.
3. **F-244 of `§8` waits for all 26 tasks of `§1` ~ `§7` (F-215 ~ F-240) to land.** The three ledgers F-241 / F-242 / F-243 may run in parallel first, but F-246 must wait for all three records F-243, F-244 and F-245 to be complete — the signature is the last step, not the first.

### 0.2 The five lines that must be restated at the head of every section (context-independent)

After the master prompt (document 38) has been fed once, paste the five lines below **verbatim** at the head of every feed (after document 38, before the body of this section). These five lines are the only wording of this section that may be "executed without explanation"; the section does not repeat them:

```text
① Only one red line is left: no Mixin (@Mixin / mixins.json at 0 hits repo-wide). Custom network packets have no cap (D-146 / D-172):
   this version has 2 in total, 1 before F-238 (farmstorm:core_list) and 2 after it (+ farmstorm:cultivator_link); the client never answers back.
② The three totals hold only at their own close-out point: registry objects 97 only after F-228, config 175 keys / 60 groups only after F-242,
   language keys 337 × 2 only after F-241. Mid-way write only "this key exists / does not exist" and "the toml interval reading of this item";
   do not write the close-out number as this section's pass condition, and do not change the total comments of ModLangProvider:13-16 or FarmstormConfig.
③ tools/verify_project.py is not a criterion before F-243 (A-66): it prints `已核 registry 对象: 93 / 93` for the registry surface and exits 0,
   and reports "toml 多而文档没记" for the config section. After F-243, quoting it must come with `已核 registry 对象: 97 / 97` —
   writing only "结论：FAIL 0 项" is a false green and silently checks 4 objects short.
④ The registry self-check window = F-224 (opens) → F-228 (closes): inside the window write only the actual breakdown of the FAIL line (state A 96 / state B 97),
   and the only place allowed to write → OK is the close-out task F-228 (start a server for real once after the 5 constants are changed all at once).
⑤ A-62: for outpost_core.farmlandBonus comment out the definition only (FarmstormConfig.java:909-912) and keep the field declaration (:227);
   A-69: the "no read, no write" criterion for farmstorm_farmland.dat may only be executed after F-233 — before that settleOnRemoval is still its last reader.
```

### 0.3 Quick reference (one screen: Phase ↔ milestone ↔ task IDs ↔ person-days ↔ exit criterion in one sentence)

| Phase | Milestone | Task IDs | Count | Person-days | Exit criterion (one sentence) |
|---|---|---|---|---|---|
| 32 Baseline and the tier carrier | M28 | F-213 ~ F-215 | 3 | 5.0 | the artifact `farmstorm-0.2.0+mc1.21.1.jar`; all 7 `new CropProfile(` carry `CropTier`; per tier 11 / 7 / 4; `tooltip.farmstorm` 16 → 20 |
| 33 Narrowing the rule surface | M29 | F-216 ~ F-219 | 4 | 2.0 | the pool `11 种… → OK` and `== 19` at 0 hits; the 15 crops and the 7 seeds cannot be planted, and the 7 deployables still place as usual; an empty-hand right-click on the converter does nothing while the 29 recipes are still there |
| 34 Retiring the farmland system and trade | M30 | F-220 ~ F-223 | 4 | 6.5 | each of the 5 comments carries its reason line, the `FarmstormConfig.java:227` field is kept; toml 165 / 58 → 171 / 59; the 3 farmer entries sit at 1 / 3 / 5, `maxUses = 1`, prices land in [2,5] / [10,32] / [40,64] |
| 35 The registry surface, resources and the container skeleton | M31 | F-224 ~ F-228 | 5 | 12.5 | two FAILs during the window (96 with four items / 97 with `menu_types 2`), and `97 … → OK` after F-228; `blockstates` 27 / `models` 106 / `loot_table` 27 / mcmeta 2 |
| 36 Core interaction, binding and migration | M32 | F-229 ~ F-233 | 5 | 9.5 | the 54-slot screen and its two gates (9 blocks away it does not open); four conditions give 0 drops; `Cultivators` holds 1 entry; re-bind / stay in place / NBT one byte unchanged; `setBlock` 0 |
| 37 Cultivator behaviour | M33 | F-234 ~ F-237 | 4 | 7.0 | `[cultivator]` 600 / 2, the whole file 175 / 60; Epic refused; 600 ticks give +2 and +2, with the core full `Progress == cycleTicks` and nothing lost; breaking it drops 1 + 1 |
| 38 Linking | M34 | F-238 ~ F-240 | 3 | 7.5 | 2 lines of `自检 网络包注册`, protocol string 2; N links inside the range, an empty packet outside it; 0 lines of `Missing elements in vertex` over 10 minutes |
| 39 Close-out and full acceptance | M35 | F-241 ~ F-246 | 6 | 11.0 | 337 × 2 with identical key sets, 175 / 60, `97 / 97` and `FAIL 0` holding at the same time; 36 + 40 = 76 all pass; `@Mixin` 0 |
| **Total** | **M28 ~ M35** | **F-213 ~ F-246** | **34** | **61.0** | bare person-days 61.0 (`11 × 0.5 + 9 × 1.5 + 14 × 3.0`), with the 25 % buffer **76.25**; critical path **21.0 person-days / 8 tasks** (the core line) |

### 0.4 The reading ledger of the three lagging windows

| Window | Opens | Closes | What may be written mid-window only | Changed all at once when the window closes |
|---|---|---|---|---|
| Registry self-check | **F-224** | **F-228** | the actual breakdown of the FAIL line: state A = **96** (`items 33 / blocks 27 / block_entities 8 / menu_types 1`); state B = **97** with `menu_types 2` | the 5 constants `93 → 97`, `32 → 33`, `26 → 27`, `7 → 8`, `1 → 2` + the two `93` in the method comment |
| Language keys | **F-215** | **F-241** | "this key exists / does not exist" + the per-prefix bucket delta (`tooltip` 16 → 20, `container` 1 → 2, `message` 2 → 0) | **337** in each of the two lang files and the item-by-item composition of `ModLangProvider.java:13-16` |
| config | **F-220** | **F-242** | the toml interval reading of this task only: **165 / 58** → **171 / 59** → **172 / 59** → **173 / 59** → **175 / 60** | **175 keys / 60 groups** and the group-by-group check (`b.push(` = **60**) |

Three notes on reading it:

1. **Each of the three windows changes its "total" only inside the close-out task**, and the close-out task carries no other change (F-228's `git diff` touches only those 5 numbers and that comment; F-241 / F-242 each touch their own ledger and nothing else).
2. **Every server start inside the window reads the source text**: the registry self-check line sits in `run/logs/latest.log`, the key sets of the two lang files under `src/generated/resources/assets/farmstorm/lang/`, the whole config in `run/config/farmstorm-server.toml`. All three can be re-read; do not rely on memory.
3. **The three windows do not start and end in the same Phase**: the registry window falls entirely inside §4; the language-key window opens in §1 and closes in §8; the config window opens in §3 and closes in §8. A cross-section reference always writes "the interval reading of this section", never "already in place".

---

## 1. Phase 32 · Baseline and the tier carrier (M28)

### 1.1 Preconditions

- [ ] The work tree is clean: `git status` shows only the **10** files this section changes (`gradle.properties`, `一键构建.cmd`, `检查环境.cmd`, the new `util/CropTier.java`, `util/CropProfile.java`, `Farmstorm.java`, `item/ModifiedCropItem.java`, `item/DeployableCropItem.java`, `item/SeedItem.java`, `datagen/ModLangProvider.java`).
- [ ] `.tools\gw.cmd build` currently exits **0**; the only recognised build entry point is `.tools\gw.cmd build` — do not call gradle directly.
- [ ] The baseline is **the released 0.1.1 state**: `git diff --stat v0.1.1 -- src` is empty (D-148); `build/libs` holds `farmstorm-0.1.1+mc1.21.1.jar` at this moment.
- [ ] These four at hand: the 22-row tier table of document 34 §4.1 and the derived set of §4.3; document 36 §2.1 (the position of the 9th field, the name colour table, the tooltip line) and §4.3.2 rows 4 ~ 7; the baseline of document 36 §1.3 (`tooltip.farmstorm` **16** entries, `textures/item` **29** PNGs, `data_components` **1**); document 35 §2.2 / §2.5.
- [ ] The version number has exactly one source: `gradle.properties:11`. The two `.cmd` scripts are **carriers of the artifact face** (`一键构建.cmd:11-12`, `检查环境.cmd:14`, A-65), and the banner writes no version string — that was unified in the 0.1.1 round, so do not add one in passing here.
- [ ] Frozen surface: the **29** PNGs of `textures/item`, `models/item/*.json`, the `clientTrackingRange(4)` / `updateInterval(10)` of `FarmstormEntityTypes`, the registry surface (still **93**) — all four `git diff` runs must be empty.
- [ ] Previous Phase: **this section is the first one** and has no previous section; its exit criteria are exactly §2's preconditions.

### 1.2 Goal of this phase

Nail down the two things "version number" and "tier": the former is the name source of every artifact (F-213), the latter is the **only data source** of the three places — trade / cultivator admission / the Lucky Berry pool (F-214); then let the 29 item names take their colour by tier and the tooltip gain one line (F-215).

At the end of this section the world has two more capabilities: the artifact name agrees with the version number; the 22 crops go from "a heap of same-coloured ids" to "a set readable in three tiers and filterable by tier". And not one of the textures, the stack sizes, the recipes or the registry surface has been touched.

### 1.3 Task list (F-213 ~ F-215)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-213 | Version number 0.2.0 and the artifact names of the two scripts | S | 0.5 | — | `mod_version` at `gradle.properties:11` becomes `0.2.0`; the jar names of `一键构建.cmd:11-12` (including the `-sources.jar` line) and `检查环境.cmd:14` follow |
| F-214 | The tier carrier: `CropTier` + the 9th field of `CropProfile` + the 22 profiles | L | 3.0 | — | a new `util/CropTier.java`; the record head `:32-40`, the 22 profiles `:47-128` and the three private factories `:191-207` of `util/CropProfile.java` changed in one go; `Farmstorm.java` gains one tier self-check line |
| F-215 | The tier visual: name colour + one tooltip line + 4 language keys | M | 1.5 | F-214 | each of the three item classes overrides `getName`; one line appended to the end of `appendCropTooltip`; +4 `tooltip.farmstorm.tier*` entries in each of the two lang files |

Phase subtotal: **3 tasks, 5.0 person-days** (S 1 / M 1 / L 1).

### 1.4 Exit criteria

- [ ] **Artifact name and version line (F-213)**: How to verify: after `.tools\gw.cmd build`, read `META-INF/neoforge.mods.toml` through `jar tf build/libs/farmstorm-0.2.0+mc1.21.1.jar`. What passing looks like: exit code **0**; the main jar and `farmstorm-0.2.0+mc1.21.1-sources.jar` both in place; **0** leftovers of `farmstorm-0.1.1*` and `farmstorm-0.1.0*` in `build/libs`; the `version` line is `0.2.0`.
- [ ] **Three literals changed in one go (F-213 / A-65)**: How to verify: two `grep -c` runs + `git diff gradle.properties`. What passing looks like: `farmstorm-0.1.1` has **0** hits in each of `一键构建.cmd` and `检查环境.cmd`; `farmstorm-0.2.0+mc1.21.1` has **2** / **1** hits respectively; `git diff gradle.properties` shows one changed line only, line **11**.
- [ ] **The record contract changed in one go (F-214)**: How to verify: `grep -c "new CropProfile("` + reading the three factories through. What passing looks like: **7** direct constructions, every one of them ending its argument list with a `CropTier.` argument; each of the three private factories `crop` / `throwable` / `deployed` gains one `CropTier` formal parameter and forwards it unchanged; not one of the five numbers `22 / 12 / 7 / 3 / 2` at `Farmstorm.java:112-113` moves.
- [ ] **Per-tier counts (F-214 / D-149)**: How to verify: count `CropTier.` in the profile region + read the startup log. What passing looks like: `CropTier.COMMON` **11**, `CropTier.RARE` **7**, `CropTier.EPIC` **4** (the enum declaration and the factory formal parameters do not count); the log carries `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`.
- [ ] **Name colour and tooltip (F-215)**: How to verify: put one of each tier into the inventory, open the tooltip, then rename one with an anvil. What passing looks like: Common white / Rare blue / Epic gold; the tooltip gains one line "品质：稀有" **after** the existing category line; `DeployableCropItem` (**7**) and `SeedItem` (**7**) change colour too — not only `ModifiedCropItem`; record how the colour behaves after the anvil rename per document 36 §9 ⑥, and do not write a new implementation in this section.
- [ ] **Only 4 language keys added (F-215, the total is not in this section)**: How to verify: the key sets of the two lang files. What passing looks like: **4** new `tooltip.farmstorm.tier*` entries each (the three `.common` / `.rare` / `.epic` + one for the line format); the `tooltip.farmstorm` prefix goes **16 → 20**; **do not write the total** — the class comment of `ModLangProvider.java:13-16` is untouched character for character (the total comes at F-241).
- [ ] **Registry surface and textures untouched (negative)**: How to verify: `git diff --stat textures/` + start the server and read the self-check line. What passing looks like: `textures/item` still holds **29** PNGs, each sha256 identical to before the commit; `自检 注册对象 93 个：… → OK`, with the 12 per-category items identical to 0.1.1 (`menu_types` still **1**).

### 1.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| The record committed in several goes | miss one of the 7 direct constructions or one of the 15 factory arguments and it does not compile, and the rollback surface goes from 1 commit to 3 | one commit for F-214: the 7 `new CropProfile(` + the three factories + the 22 `tier` entries all inside the same `git diff` |
| Changing the 8 "seed = itself" `seedItem` entries in passing | those 8 (`blast_carrot` / `ubi_server` / `toxic_ubi_server` / `fang_trap_cactus` / `bamboo_firecracker` / `red_mushroom_puff` / `brown_mushroom_puff` / `lucky_berry`) point at themselves; change one and F-236's boolean test reads it as "has its own seed", the yield goes from 2 to 2 + 2, and the account `5 给 2+2 / 13 只给 2` is void on the spot | this section only fills in the tier of the 9th field; the diff of `seedItem` must be empty |
| Name colour changed in the parent class only | `DeployableCropItem` (7) does not extend `ModifiedCropItem` and the `SeedItem` (7) branch sits elsewhere ⇒ **14** items still carry a white name | each of the three classes overrides it; `grep -c "getName"` ≥ **1** in each of the three files; walk through the inventory item by item |
| The tier written as a second id list | trade / admission / pool each keep their own list, and after one `tier()` change the three fall out of step (the original symptom of RT-49) | `CropProfile` is the only place allowed to define it; every downstream reader goes through `tier()` (A-63 asks the same of the pool) |
| "Fixing" an existing typo in passing | the 8 / 5 / 2 at `CropType.java:17,19,21` and the "6 个" at `SeedItem.java:13` get changed, and the diff gains lines unrelated to this task | document 34 §8 item 4 names two of them; **change neither**; `git diff --stat` in this section may cover **10** files only |
| Opening the registry window early | registering something in passing as a preparation for F-224, `自检 注册对象` goes from 93 to 94, and every `→ OK` of §2 / §3 becomes void | the registry reading of this section must still be **93**; the diff of the four files under `registry/` is empty |
| Changing the language-key total comment in passing | `ModLangProvider.java:13-16` becomes 327 and by F-241 there is no baseline left to compare | the comment is untouched; this section only verifies "4 keys exist + the prefix 16 → 20" |

### 1.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the three literals of F-213, the record head + the 7 constructions + the three factories + the 22 profiles of F-214, and for F-215 how many lines each of the three classes takes + where the 4 keys sit (file:line).
- **Commands run**: the exit code of `.tools\gw.cmd build`; the two sets of entries of `jar tf`; the readings of the two `grep -c` runs; the changed-line count of `git diff gradle.properties`.
- **What was observed**: the verbatim text of the two lines `自检 作物品质` and `自检 作物`; the per-tier counts 11 / 7 / 4; `tooltip.farmstorm` 16 → 20; the sha256 list of the 29 PNGs and the comparison against the pre-commit state.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example the tier colour after an anvil rename can only be seen on a single-player session on site).
- **Open questions**: for example the two to-be-verified APIs of document 36 §9 ⑥ (whether `CUSTOM_NAME` overrides the tier colour, whether the rarity wrapper overrides the sub-component colour) — this section only gathers evidence and draws no conclusion.

---

## 2. Phase 33 · Narrowing the rule surface (M29)

### 2.1 Preconditions

- [ ] **entries 1 / 3 / 4** of the previous section §1.4 are green: the artifact name and version line (`0.2.0`), the record contract changed in one go, per tier 11 / 7 / 4.
- [ ] The log already carries `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`; the line `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK` stays unchanged.
- [ ] These four at hand: document 35 §3 (the two paths compared entry by entry), §5 (the pool 19 → 11 and the 8 removed entries), §9.2 / B-35; document 36 §2.2 (how paths ① / ② are written and the negative list), §2.9; document 34 D-152 / D-155 / D-156 / D-170, ruling #19; document 37 A-63.
- [ ] Frozen surface: the tier and the 9th field of the 22 profiles were frozen at F-214 and are not changed here; `FarmstormCropBlock` is untouched (`mayPlaceOn` / `canSurvive` were never overridden); `canPlantAt` at `ModifiedCropItem.java:53-55` has 0 callers repo-wide and is **not deleted**.
- [ ] The arithmetic base is at hand: `19 − 8 = 11` (what is removed is **6** Rare + **2** Epic inside the pool); the `18 = 11 + 7` of the admission face has nothing to do with this section — do not use it as a criterion.
- [ ] The registry window has not opened yet: this section is still `total = 93 / → OK` (it opens only at F-224).

### 2.2 Goal of this phase

Land the four "player-perceivable rule changes" independently: the berry pool narrows (F-216), each of the two planting paths is blocked once (F-217 / F-218), the converter loses its function (F-219). The four do not depend on one another and may be spread over four days, but **each acceptance must be written separately and never merged**.

At the end of this section: eating a Lucky Berry rolls only 11 Common crops; the 15 crops that have a crop block and the 7 seeds do nothing on farmland and are not consumed; the 7 deployable items still place as usual; a right-click on the converter does nothing at all, while the block, the 29 recipes and the creative-tab entry are all still there.

### 2.3 Task list (F-216 ~ F-219)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-216 | The Lucky Berry pool at 11, in the same commit as the assertion | S | 0.5 | F-214 | the pool at `util/CropProfile.java:139-143` becomes "keep `tier() == COMMON` only"; the assertion `19 → 11` and the log wording at `Farmstorm.java:117-123` change in the same commit |
| F-217 | Planting path ①: `isPlantableSoil` constantly `false` | S | 0.5 | — | the body of the predicate at `item/ModifiedCropItem.java:101-107` returns `false`, and the old "only a `FarmBlock` is allowed" stays as a comment; the call site `:72` is untouched |
| F-218 | Planting path ②: `SeedItem#useOn` returns `PASS` | S | 0.5 | — | `item/SeedItem.java` gains a `useOn` override returning `InteractionResult.PASS`; the chain `ItemNameBlockItem` → `BlockItem#useOn` → `place()` is cut |
| F-219 | The converter's `useWithoutItem` returns `PASS` | S | 0.5 | — | the body of `block/ConverterBlock.java:78-86` is replaced wholesale by a `PASS` return; `getMenuProvider` (`:70-76`) and the drops (`:88-96`) are kept |

Phase subtotal: **4 tasks, 2.0 person-days** (S 4 / M 0 / L 0).

### 2.4 Exit criteria

- [ ] **The pool 11 in the same commit as the assertion (F-216)**: How to verify: read the startup self-check line + `grep`. What passing looks like: the log `自检 幸运浆果池 11 种…→ OK`; all 11 `tier()` of the pool are `COMMON`; `== 19` has **0** hits in `Farmstorm.java`; the other half (the three `!=`) — "no `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`" — is still there (document 36 §8 #35).
- [ ] **Only Common rolled (F-216 / D-156)**: How to verify: eat `lucky_berry` until the "generate a random crop" branch is hit, and record the produced id each time (put the number of samples into the record). What passing looks like: every product falls inside the 11-entry Common set; `wild_apple` / `lucky_berry` / `compressed_puffed_wheat` appear **0** times each.
- [ ] **Path ① (F-217)**: How to verify: right-click farmland with each of the **15** crop items that have a crop block. What passing looks like: no block appears, the held item is not consumed, there is no message; the executable body of `isPlantableSoil` contains no `instanceof FarmBlock` (only the comment), and the method is still called from **1** place at `ModifiedCropItem.java:72`.
- [ ] **No collateral damage on deployables (F-217 / document 35 B-02)**: How to verify: right-click the top face of a full block with each of the **7** deployable items (`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` / `zangief_cane` / `point_defense_fungus`). What passing looks like: they place in their deployed form as usual — what was cut is "planting a crop", not "placing a block".
- [ ] **Path ② (F-218)**: How to verify: right-click farmland with each of the **7** seeds, then right-click dirt. What passing looks like: no block appears, the seed is not consumed, there is no message; `InteractionResult.PASS` has **1** hit in `SeedItem.java` and `useOn` has **1** hit; `appendHoverText` is still there (no tooltip regression).
- [ ] **The converter (F-219)**: How to verify: right-click it once with an empty hand; then count the resources and the registrations. What passing looks like: no screen, no message, the held item is not consumed; the json count under `data/farmstorm/recipe/` is **29** (**26** `converting` + 2 shaped + 1 shapeless); `REGISTER.register` in `FarmstormMenus.java` still has **1** hit.
- [ ] **Registry surface and config untouched (negative)**: How to verify: start the server and read the self-check line + read `run/config/farmstorm-server.toml`. What passing looks like: `自检 注册对象 93 个 … → OK` (none of the 4 tasks here enters the registry surface); the toml still holds **166 keys / 58 sections** and `[outpost_core]` still **3** keys; the diff of `config/FarmstormConfig.java` is empty.

### 2.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Only one planting path blocked | the 7 seeds still plant a crop on farmland (the original symptom of RT-48) | F-217 / F-218 **each gets its own acceptance entry, never merged**; the two paths differ entirely in class and call site, so each is blocked once |
| Deleting the `canPlantAt` dead code in passing | it collides with the D-152 supplement, and the diff gains a stretch unrelated to this task | the 3 lines at `ModifiedCropItem.java:53-55` are untouched character for character; "deleting dead code" is not in this version |
| The pool written as 11 hand-written ids | after one `tier()` change the pool no longer follows, and tier and pool become two truths (RT-49's guard fails on the spot) | keep the filter `tier() == COMMON` only (A-63); write no second id list |
| The pool changed but not the assertion (or the reverse) | one permanently red FAIL on every startup; the server still starts, but a real fault is buried under that noise (the corrected wording of RT-50) | pool and assertion in the **same commit**; change both the assertion value and the log wording |
| `PASS` written as `FAIL` / `SUCCESS` | `FAIL` makes the caller think it was refused, `SUCCESS` swallows the right-click | both places are `InteractionResult.PASS`; one `grep -c` in each of the two files |
| The converter's screen or menu registration deleted in passing | `menu_types` 1 → 0, the registry surface 93 → 92, and F-228's 97 never matches | `FarmstormMenus` / `ConverterScreen` untouched; the 29 recipes untouched |
| Writing "the 26 recipes" as a count of the whole directory | document 35 B-35 means 26 `converting` recipes, while the directory actually holds **29** json files; a wrong count turns it red | write the two numbers separately: 26 = the `converting` subset, 29 = the whole |

### 2.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the file and line numbers of each of the four (`CropProfile.java:139-143`, `Farmstorm.java:117-123`, `ModifiedCropItem.java:101-107`, the override lines added in `SeedItem.java`, `ConverterBlock.java:78-86`).
- **Commands run**: the exit code of `.tools\gw.cmd build`; the two `grep -c` runs (`InteractionResult.PASS`, `== 19`); the count of `ls data/farmstorm/recipe/*.json`; one server start to read the self-check line.
- **What was observed**: the 11 ids of the pool and their `tier()`; the sampling record (number of tries + the full table of produced ids); one line each for the right-click results of the three groups 15 + 7 + 7; the four scenarios of the converter (empty hand / seed in hand / creative tab / recipe count).
- **Not done and not doable**: one line each stating "not verified" and the reason (for example there is no second client for the "can it be planted in creative mode" comparison; B-04 goes to F-245).
- **Open questions**: for example the javadoc "14 种池" at `LuckyBerryRoller.java:170` is an existing typo (document 34 §8 item 8); this section neither changes nor cites it.

---

## 3. Phase 34 · Retiring the farmland system and trade (M30)

### 3.1 Preconditions

- [ ] **entries 1 / 3 / 5** of the previous section §2.4 are green: the pool 11 in the same commit as the assertion, path ①, path ②. Entries 2 / 4 / 6 / 7 of §2 are not preconditions of this section, but they must go green before §8.
- [ ] **entries 3 / 4** of §1.4 are green: `tier()` is readable, per tier 11 / 7 / 4 — trade draws by tier, and without it nothing can land.
- [ ] Two fixtures at hand: ① a 0.1.1 legacy save (`world/data/farmstorm_farmland.dat` in place, for the A-69 timing check); ② a farmer villager that can level up (a bed and a composter beside it, so it naturally reaches ranks 1 / 3 / 5).
- [ ] These four at hand: document 34 D-153 / D-154 / D-157 ~ D-159, ruling #3 / #18; document 35 §7.2 (the seven landing points), §7.4 (how it feels to the player), §4.2 (the trade table), §4.5 (out of stock and restock); document 36 §2.3 (the comment list and "three places that must be kept"), §2.4 (the five steps of trade); document 37 A-62 / A-68 / A-69.
- [ ] **A-62's wording is decided by this section**: the field at `FarmstormConfig.java:227` is **kept**, and only the definition at `:909-912` is commented out. Document 34 §3.3 row 5 and document 35 §7.2 landing point 6 say "comment out the field together with the definition" — those two upstream places were never fed back; **execute per A-62**.
- [ ] **A-69's timing**: this section **must not** write "the mtime of `farmstorm_farmland.dat` is unchanged" as a criterion. After F-220 comments out the two listeners and the one backfill line, removing a core still reads and writes it (`settleOnRemoval` is the last reader; only F-233 rewrites that stretch).
- [ ] The edge of the registry window: this section is still **before the window** (`total = 93 / → OK`) and the window opens only at F-224. The current toml is **166 keys / 58 sections**, `[outpost_core]` **3** keys, and `farmstorm-client.toml` keeps its **3** keys unchanged.

### 3.2 Goal of this phase

Retire the farmland system at runtime (delete no code, delete no save, delete no block), and at the same time build "the only entrance to the Epic tier" — the farmer trade.

At the end of this section: hoeing is not intercepted, trampling is not protected, and the two action-bar messages no longer appear; `/farmstorm` still does not exist (deleted in 0.1.1, not restored in this version); in `farmstorm-server.toml` the `[outpost_core]` group has lost `farmlandBonus` and the 6 keys of `[trade]` have appeared; the farmer's trade list shows 3 entries of this mod, unlocked in rank order 1 / 3 / 5, each taking emeralds only and selling once.

### 3.3 Task list (F-220 ~ F-223)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-220 | Retiring the farmland system: 5 comments + the key definition + the field kept | M | 1.5 | — | the registration lines at `event/FarmstormGameEvents.java:75` / `:76`, the implementations at `:241-265` / `:277-281` and the single backfill line at `:171`, 5 places commented out in all; the field at `FarmstormConfig.java:227` kept + the definition at `:909-912` commented out |
| F-221 | Retiring the 3 farmland language keys | S | 0.5 | F-220 | the three entries at `datagen/ModLangProvider.java:403` and `:412-413` removed from `ENTRIES`; the section comment at `:411` dealt with in the same pass |
| F-222 | The 6 keys of the `trade` group + 6 config language keys | M | 1.5 | F-220 | a new top-level group `trade`: `common` 2 / 5, `rare` 10 / 32, `epic` 40 / 64, all ranges 1–64; 6 `farmstorm.configuration.trade.*` |
| F-223 | The 3 farmer trades: `VillagerTradeEvents` + `TierTrade` | L | 3.0 | F-222 | two new classes under `event/`; subscribe to `VillagerTradesEvent`, handle `FARMER` only, add one entry to each of `get(1)` / `get(3)` / `get(5)`, with `maxUses = 1`, `xp = 2`, `priceMultiplier = 0.0F` |

Phase subtotal: **4 tasks, 6.5 person-days** (S 1 / M 2 / L 1).

### 3.4 Exit criteria

- [ ] **All five comments carry their reason line (F-220 / D-153)**: How to verify: `grep` the five places + read the line above each. What passing looks like: `:75`, `:76`, `:241-265`, `:277-281`, `:171` are all in a commented-out state (**5** places in all); each carries **1** line above it reading "裁定 #3：v0.2.0 撤销耕地，勿恢复".
- [ ] **Three places that must be kept (F-220 / document 35 §7.3)**: How to verify: read the whole of `onServerTickPost` + look at the overlay reconciliation log. What passing looks like: `onServerTickPost` is not commented out; `CoreChunkTickets.restoreAll(server)` (`:170`) and `CoreListBroadcaster.tick(server)` (`:175`) are both inside the body; the overlay reconciliation log still prints every **100** ticks (document 36 §8 #10).
- [ ] **The two faces of A-62 (F-220)**: How to verify: read the two places of `FarmstormConfig.java` + count the lines of the two classes. What passing looks like: the field declaration at `:227` is still there, the definition at `:909-912` is commented out, and **2** lines beside the field state "恢复监听器前必须先恢复定义，否则 `FarmlandGuard.java:75` 抛 NPE"; `FarmlandLedger.java` **555** lines and `FarmlandGuard.java` **225** lines are unchanged to the character.
- [ ] **The two-level config reading (F-220 / F-222)**: How to verify: delete the old `farmstorm-server.toml`, start the server twice and read the whole file once each time. What passing looks like: after F-220 `[outpost_core]` has **2** keys (`maxPerPlayer` / `boundaryParticleIntervalTicks`) and the whole file **165 keys / 58 sections**; after F-222 the 6 keys of `[trade]` have appeared (2 / 5 / 10 / 32 / 40 / 64) and the whole file is **171 keys / 59 sections**.
- [ ] **The three total comments untouched (lagging window, the totals come at F-242)**: How to verify: read the two `166` / `58` statements in `FarmstormConfig.java` and the class comment at `ModLangProvider.java:13-16`. What passing looks like: both are unchanged character for character; this section writes only "this key exists / does not exist" and the toml interval readings of the entry above.
- [ ] **The 3 farmland keys are gone (F-221)**: How to verify: check the key sets of the two lang files after `ConvertFrom-Json` + `grep` the provider. What passing looks like: `farmstorm.configuration.outpost_core.farmlandBonus`, `message.farmstorm.farmland.no_core` and `message.farmstorm.farmland.quota_full` have **0** hits in both lang files and in `ModLangProvider.java`; the `message.farmstorm` prefix has **0** entries.
- [ ] **The three trades (F-223)**: How to verify: open the farmer trade screen + `/data get entity <villager> Offers.Recipes`. What passing looks like: **3** entries of this mod appear, at ranks **1 / 3 / 5**; `maxUses` = **1** for all three, and `buy.count` lands in **[2,5] / [10,32] / [40,64]** respectively; each `sell` is **2** crops of the same tier.
- [ ] **Out of stock and no re-roll (F-223 / D-158 / D-159)**: How to verify: after one trade, watch twice in a row (before and after the restock). What passing looks like: the entry is still in the list, greyed out and not clickable; after the restock the price and the crop id are unchanged (`priceMultiplier = 0.0F` locks the base price); a max-level farmer in a legacy save is **not granted the trades again** (document 35 B-33; this version does no migration and does not fail it).
- [ ] **A-69's timing (the entry most easily overstepped in this section)**: How to verify: read the acceptance record of this section through + check one reference point. What passing looks like: **0** assertions in the record saying "the mtime of `farmstorm_farmland.dat` is unchanged"; instead one observable substitute — `FarmlandLedger.of` at `OutpostCoreBlockEntity.java:353` is still inside that method (only F-233 deletes it) — and the record states "after this section removing a core still rewrites that file".

### 3.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Commenting the field out as well, per document 34 / 35 | the `.get()` calls at `FarmlandGuard.java:75` and `FarmlandLedger.java:305` do not compile, and two classes promised "not one line changed" are forced to change | comment out the definition at `:909-912` only; keep the field at `:227` (A-62); the `git diff` of both classes must be empty |
| Commenting out the registration lines only, not the implementations | dead code referencing the two kept classes is left behind; the other way round, commenting out only the implementations breaks the `addListener` method reference and does not compile | the two registration lines + the two implementation stretches + the one backfill line in the **same commit**; `onServerTickPost` kept |
| Commenting out `restoreAll` or `tick` in passing | the overlay packet stops going out and the core chunk tickets are not restored — both are something else entirely | comment out `:171` only; `:170` / `:175` untouched; the reconciliation log every 100 ticks is the criterion |
| Asserting that the `.dat` is unchanged before F-233 | removing a core rewrites that file, so the criterion must go red, and on site it looks like "the document is wrong" | this section only registers "removing a core still reads and writes it"; the mtime criterion goes to §5 entry 7 and §8 entry 7 |
| The trade ranks written as 2 / 4 | not a single trade entry is visible (RT-46) | the ranks are **1 / 3 / 5**; hang only one entry first, start the server and confirm it is visible, then change it to three |
| The price re-rolled when the trade closes | the same offer costs a different price each time it is opened | both the draw and the pricing happen when the first offer is generated; after a restock the price and the id are unchanged |
| `trade` written as a nested group of `outpost_core` | the group count comes out as 61, and F-242's `b.push(` = 60 never matches | `trade` is a **top-level group**, a sibling of `outpost_core`; `[trade]` holds exactly 6 keys |

### 3.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the 5 comments and the 2 comment lines at `:227` of F-220, the three deletions of F-221, the 6 fields + 6 keys of F-222, the two new classes of F-223 and the mounting line in `Farmstorm.java`.
- **Commands run**: the exit code of `.tools\gw.cmd build`; two server starts after deleting the toml; three `grep -c` runs (`farmlandBonus`, `message.farmstorm`, `REGISTER.register`); `git diff --stat` for the two kept classes.
- **What was observed**: the `grep` hits of the five comments; the verbatim `:170` / `:175` of `onServerTickPost`; the two whole-file toml readings (165 / 58, 171 / 59); one line each for `maxUses` / `buy.count` / `sell` of the three trades; two frames before and after a trade and its restock.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example a max-level farmer in a legacy save needs a real legacy save; if only a constructed save exists, state how it was constructed).
- **Open questions**: for example whether the list behind `getTrades().get(1)` is mutable (the to-be-verified API ① of document 36 §9; the fallback is `put`-ting a new `ArrayList`).

---

## 4. Phase 35 · The registry surface, resources and the container skeleton (M31)

### 4.1 Preconditions

- [ ] **entries 1 / 6** of the previous section §3.4 are green: each of the five comments carries its reason line, and the 3 farmland keys have 0 hits in both lang files.
- [ ] `[outpost_core]` no longer has `farmlandBonus` (otherwise the toml reading of F-226 gains 1 key and 172 becomes 173).
- [ ] The source texture files at hand: three frame strips (`cultivator_a_top` / `cultivator_a_side` / `cultivator_a_bottom`, **the names must not be changed**) and two static first frames (`editable_frames/side_00.png` / `top_00.png`), **7** files in all landing in `textures/block/`.
- [ ] **The registry self-check window opens in this section**: at the start of the work the log is `自检 注册对象 93 个 … → OK`; after F-224 lands it is **96 / FAIL**, after F-227 lands **97 / FAIL**, and only at F-228 **97 / OK**.
- [ ] The four window rules (document 37 §0.3): ① do not write `→ OK` into any criterion of this section; ② the other four self-check lines must still be green; ③ the close-out task carries no other change; ④ the total comments of the language keys and of config are untouched.
- [ ] This section holds two "firsts for the whole project", both estimated one notch up: ① the first `*.png.mcmeta` repo-wide (RT-47); ② the first time a "non-container BE" is upgraded into a container (RT-44). Both demand "write the smallest case and get it running first, then write the real logic".
- [ ] **entries 1 / 6** of the previous Phase, §3.4, must be green (that is, preconditions 1 and 2 here); entries 7 / 8 of §3 (the three trades, out of stock and no re-roll) are not preconditions of this section, but they must go green before §8.

### 4.2 Goal of this phase

Land all **4** registry increments of this version (the cultivator trio → the core menu), together with the 12 resource files, the container skeleton and the menu skeleton, and finally close the registry self-check window in one go.

At the end of this section: `farmstorm:cultivator` can be obtained, placed, and has a two-state model and a loot table; the core has become a 54-slot container that can hold items; the menu and the screen are registered (but the screen still does not open — F-229 connects it); the self-check line walks from `93 → OK` through `96 / FAIL` and `97 / FAIL` to `97 → OK`.

### 4.3 Task list (F-224 ~ F-228)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-224 | The cultivator registration trio + the creative tab | L | 3.0 | — | new `block/CultivatorBlock.java` and `blockentity/CultivatorBlockEntity.java` (skeleton); +1 in each of the three registries; one entry added to `creativeTabItems()`; **the window opens** |
| F-225 | Cultivator resources, the two models and three datagen places | L | 3.0 | F-224 | 7 texture files + the two models + the two-state blockstate + the item model + the loot table; `usesBlockModel` gains `CultivatorBlock`; `getKnownBlocks()` gains one entry |
| F-226 | The core becomes a container: 54 slots + `Items` + two sync methods | L | 3.0 | — | `OutpostCoreBlockEntity` implements `Container` (54 slots) + NBT `Items` + `getUpdateTag` / `getUpdatePacket` + a legacy-compatible read; new `outpost_core.storageSlots` |
| F-227 | The core menu + screen + `menu_types` +1 | L | 3.0 | F-226 | new `menu/OutpostCoreMenu.java` and `client/OutpostCoreScreen.java`; `FarmstormMenus` +1; the screen registration +1; **the last registry increment inside the window** |
| F-228 | Close-out of the registry self-check at 97 (closing the window) | S | 0.5 | F-224, F-227 | the 5 constants at `Farmstorm.java:147-150` changed all at once (93 → 97, 32 → 33, 26 → 27, 7 → 8, 1 → 2) + the two 93 in the method comment; **the window closes** |

Phase subtotal: **5 tasks, 12.5 person-days** (S 1 / M 0 / L 4).

### 4.4 Exit criteria

- [ ] **Window state A (F-224)**: How to verify: start the server and read `run/logs/latest.log`. What passing looks like: `自检 注册对象 96 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → FAIL`. **FAIL is the correct value inside the window**; do not turn it into OK, and do not change the assertion constants to make it green.
- [ ] **The cultivator obtainable (F-224 / A-64)**: How to verify: `/give`, `/setblock`, `/data get block`, open the creative tab. What passing looks like: `farmstorm:cultivator` can be obtained, placed, and has a block entity; one more entry appears at the end of the "工具方块" section of the creative tab; `cultivator` has ≥ **2** hits in `FarmstormItems.java` (registration + creative tab).
- [ ] **Resource counts (F-225)**: How to verify: count `src/generated/resources` and `textures/block`. What passing looks like: `blockstates` **26 → 27**, `models` **103 → 106**, `loot_table` **26 → 27**, `*.png.mcmeta` **0 → 2**, `textures/block` gains **5** PNGs (3 frame strips + 2 static first frames).
- [ ] **Two states and the models (F-225 / RT-47)**: How to verify: read the three json files + the two mcmeta files. What passing looks like: `blockstates/cultivator.json` has exactly **2** variants (`working=false` / `true`) and the latter's model is `farmstorm:block/cultivator_working`; the parent of `models/item/cultivator.json` is `farmstorm:block/cultivator` (seeing `item/generated` or `layer0` means `usesBlockModel` was missed); the two mcmeta files are byte-for-byte identical, `frametime 2`, `frames 0..15`, and the two `_still` files carry **no** mcmeta.
- [ ] **The container face (F-226 / RT-44)**: How to verify: `/data get block <core>` + upgrade a legacy save + check `getUpdateTag` by hand once. What passing looks like: the `Items` key appears (an empty list when there are 0 items); a legacy save (a core stored by 0.1.1) reads back as **54** empty slots with no exception stack; the return body of `getUpdateTag` holds only the three owner keys and writes neither `Items` nor the binding.
- [ ] **Window state B (F-227)**: How to verify: start the server and read the log + count the two registration points. What passing looks like: `自检 注册对象 97 个：… / menu_types 2 → FAIL` — every number right, the assertion constant still 93; `FarmstormMenus.REGISTER.register` has **2** hits; `FarmstormClient.java` holds **2** lines of `event.register(...Screen::new)`; at this moment the screen **still does not open** in game (F-229 connects it; an expected in-between state).
- [ ] **Closing the window (F-228)**: How to verify: start a server for real once and read the whole line + look for leftovers of `93` + read the diff. What passing looks like: `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`; `93` has **0** hits in `Farmstorm.java` and `97` ≥ **1**; `git diff` touches only the 5 constants and the two 93 in the method comment, and the **13**-argument format string is unchanged character for character.
- [ ] **The four negative window rules (any server start of F-224 ~ F-227)**: How to verify: read the other four lines in the same log. What passing looks like: `自检 作物 22 种…`, `自检 作物品质 普通 11 / 稀有 7 / 史诗 4`, `自检 幸运浆果池 11 种…` and `自检 网络包注册` are still `→ OK` — any one of them red is a real fault, not the window.
- [ ] **The other two numbers were not changed in passing (lagging window)**: How to verify: read the toml and the two total comments. What passing looks like: after F-226 `run/config/farmstorm-server.toml` holds **172 keys / 59 sections** and `[outpost_core]` **3** keys (`maxPerPlayer` / `boundaryParticleIntervalTicks` / `storageSlots`); `ModLangProvider.java:13-16` and the config total comment are unchanged character for character (the totals come at F-241 / F-242).

### 4.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Writing `→ OK` inside the window | every later server start carries one permanently red noise line and a real fault is buried (the corrected wording of RT-50: log only, do not throw) | this section writes only the actual breakdown of the FAIL line (96 / 97); the only place writing OK is entry 7 (the close-out task's own criterion) |
| Changing the 5 constants early to turn the self-check green | after F-224 the log goes OK while `menu_types` is still 1 short, and it goes red again at F-227 — the window becomes a fiction | the 5 constants change only at F-228, and that commit carries nothing else |
| `usesBlockModel` does not gain `CultivatorBlock` | the item model falls back to `basicItem` and asks for a `textures/item/cultivator.png` that does not exist; in game it is a purple-black square | check three things together: the parent of `models/item/cultivator.json`, the `usesBlockModel` test, and the absence of a new file under `textures/item` |
| Only `add(...)` added, not into `getKnownBlocks()` | the loot table is not generated and breaking the block drops nothing | the hand-written list of `ModLootTableProvider` **26 → 27**; watch the `loot_table` directory count at the same time |
| Stuffing `Items` into `getUpdateTag` | every chunk send grows by a few KB (RT-44's straight answer is "do not stuff it") | `getUpdateTag` writes the three owner keys only; check it by hand **1** time |
| 54 and 16 swapped | 54 is the slot count and 16 the cultivator quota (ruling #8); swapping one of them voids a whole acceptance stretch | keep the two key names apart: `storageSlots` 54, `cultivatorCapacity` 16 (the latter comes at F-231 and is not created here) |
| Writing the range of `storageSlots` as 1–64 | `ModConfigSpec` accepts it, but the slot count turns into another value and the screen layout and `getContainerSize()` become two different things | the range is **54–54**; if it is refused, take the fallback of document 36 §9 ⑦ (`54..55` + clamping to 54 when the menu is created), and the slot count must not change |
| Registering the cultivator menu in passing | `menu_types` gains +1 early, state A becomes 97, and state B and the close-out match nothing | the cultivator has **no menu** (a right-click puts the item straight in); the +1 of `menu_types` goes to the core storage only |

### 4.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the one line each in the three registries of F-224 + the two new classes, the 12 resource files of F-225 named one by one, the class declaration / field region / NBT / two sync methods of F-226, the two new classes + the two registration points of F-227, and the 5 constants of F-228.
- **Commands run**: the exit code of `.tools\gw.cmd build`; the two `runData` runs (one at F-225, one after F-227); the logs of the three server starts (96 / 97 / 97); the five directory counts (`blockstates` / `models` / `loot_table` / mcmeta / `textures/block`).
- **What was observed**: the **complete verbatim text** of the three self-check lines (including FAIL and OK); the other four negative self-check lines; the `Items` reading of `/data get block <core>`; the slot count after the legacy upgrade; a screenshot of the creative tab.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example how the animation looks under shaders needs a client on site; part of RT-47 goes to F-244).
- **Open questions**: for example the trade-off of `getUpdateTag` not carrying `Items` (the to-be-verified API ④ of document 36 §9; the fallback is to put in "the number of non-empty slots + the first slot's item", ≤ 200 bytes).

---

## 5. Phase 36 · Core interaction, binding and migration (M32)

### 5.1 Preconditions

- [ ] **entries 1 / 5 / 6 / 7** of the previous section §4.4 are green: state A (96 / FAIL), the container face, state B (97 / FAIL), the close-out (`97 … → OK`). Entry 5 (the container face) is the direct precondition of F-230 / F-231.
- [ ] **The close-out is confirmed**: from this section on `→ OK` may be cited, but the conclusion of `tools/verify_project.py` **must not** be cited (A-66: it still prints `93 / 93` and exits 0).
- [ ] `menu_types 2` and `block_entities 8` are in place; the "resource counts" of §4 (`blockstates` 27 / `models` 106 / `loot_table` 27) are green.
- [ ] Fixtures: a 0.1.1 legacy core (only the three owner fields); two cores (for the "one full at 16, the other with space" comparison); a cultivator (F-224 has landed).
- [ ] These four at hand: document 36 §2.6.1 ~ §2.6.4, §2.7 (the seven steps), §6 ①②③; document 35 §8.2 ~ §8.5, B-12 ~ B-29; document 34 D-165 ~ D-168, ruling #8 / #17; document 37 A-69.
- [ ] Frozen surface: the vanilla distance of `Container.stillValidBlockEntity` (`4.0F`) gets no new constant and no new config key (ruling #17); `RANGE_CHUNKS = 1` (3×3 chunks) is used for binding and linking only and is **not** an opening condition; `FarmlandLedger` / `FarmlandGuard` are unchanged by one line.
- [ ] **A-69's timing**: this section may write "the `.dat` mtime is unchanged" only **after F-233**. Entry 7 is therefore executed at the end of this section — note one mtime when the work starts and one more after F-233 lands.

### 5.2 Goal of this phase

Turn the core into a complete carrier: "a chest that opens + an anchor that binds 16 cultivators + something that hands the cultivators over when it is broken".

At the end of this section: your own empty hand opens 54 slots, someone else's does not open, and 9 blocks away it does not open; breaking it drops the whole chest, while a piston / the same block / an unload all drop 0; the core side records 16 entries in `Cultivators`; a placed cultivator binds to the nearest core of its owner, ties broken by `placedGameTime`; removing a core re-binds or leaves the cultivator in place marked unbound (**never turns it into dirt**), and the last runtime reader of the `.dat` disappears in this section.

### 5.3 Task list (F-229 ~ F-233)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-229 | Opening the core storage: the two gates | M | 1.5 | F-227 | `block/OutpostCoreBlock.java` gains `useWithoutItem` and `getMenuProvider`; the menu's `stillValid` uses vanilla semantics; not your own gives `PASS` |
| F-230 | The core's break drop | S | 0.5 | F-226 | `onRemove` at `block/OutpostCoreBlock.java:101-107` gains the four-condition drop (`Containers.dropContents`) |
| F-231 | The 16-entry binding list on the core side + `cultivatorCapacity` | M | 1.5 | F-226 | `OutpostCoreBlockEntity` gains `Cultivators` (a Long list, ≤ 16) and the binding accessors; new `outpost_core.cultivatorCapacity` (default 16) |
| F-232 | Binding on placement: the four criteria + writes on both sides | L | 3.0 | F-224, F-231 | `CultivatorBlock#setPlacedBy` picks the core (`distSqr` + `placedGameTime` + coordinate order); both the core-side list and the cultivator-side `Core` are written |
| F-233 | The three exits of migration: `settleOnRemoval` rewritten wholesale | L | 3.0 | F-232 | `OutpostCoreBlockEntity.java:341-394` rewritten wholesale: real-removal test → copy the list → pick a successor core item by item → the three exits → clear `cultivators` only → `release()` |

Phase subtotal: **5 tasks, 9.5 person-days** (S 1 / M 2 / L 2).

### 5.4 Exit criteria

- [ ] **The two gates (F-229 / ruling #17)**: How to verify: right-click once as the owner and once as someone else; then try at 9 blocks and at 8 blocks. What passing looks like: the owner opens the 54-slot screen (6 rows × 9 columns + the player inventory) titled "据点核心" (Outpost Core), stores 3 items, closes and reopens and they are still there; someone else gets **no screen**; at **9** blocks away no interaction is sent, and after opening within **8** blocks, walking to 9 blocks closes it through the vanilla `stillValid`.
- [ ] **The four drop conditions (F-230 / D-167)**: How to verify: one run of each of the four scenarios. What passing looks like: 3 different stacks inside the core all drop when it is broken; a piston push gives **0** drops, `/setblock` with the same block **0** drops, and after a chunk unload the items are still inside the core; an empty core drops **1** block only.
- [ ] **The binding list (F-231 / ruling #8)**: How to verify: `/data get block <core>` + delete the old toml and start the server + change the capacity. What passing looks like: `Cultivators` appears (**1** long after binding 1); `[outpost_core]` has **4** keys and the whole file **173 keys / 59 sections**; with `cultivatorCapacity = 2` the **3rd** cultivator still places but stays unbound (document 35 §8.2 example 2).
- [ ] **Binding on placement (F-232 / D-165)**: How to verify: four comparisons. What passing looks like: A full at 16 and B with space → binds **B** (the two `/data get block` agree); two cores equidistant at the same tick → binds the smaller `placedGameTime`, and re-running the same save gives the same result; no own core nearby → the `Core` key is **missing** and a right-click does nothing; adding a core afterwards → `Core` is still missing (no automatic claim).
- [ ] **The three exits of migration (F-233 / D-168)**: How to verify: three core-removal scenarios. What passing looks like: a successor core exists → re-bound, `Held` / `Progress` unchanged; no successor core → stays in place, the `Core` key disappears, the contents are kept, a right-click does nothing, **it does not turn into dirt and drops nothing**; the chunk is not in memory → the NBT is **one byte unchanged** (`Core` still points at the old core's coordinates).
- [ ] **`setBlock` and the two dead classes (F-233 / RT-43)**: How to verify: `grep` the method body + `grep` the references. What passing looks like: `setBlock` has **0** hits inside the body of `settleOnRemoval`; references to `FarmlandLedger` / `FarmlandGuard` in this class (imports included) fall to **0**.
- [ ] **A-69 holds only after this criterion (executed after F-233)**: How to verify: note the mtime of `world/data/farmstorm_farmland.dat` when the work starts and again after F-233 lands. What passing looks like: the file exists and the two mtimes are identical; no record made before this section carries this assertion (if it does, the boundary was overstepped).
- [ ] **Registration and the window (negative)**: How to verify: start the server and read the self-check line. What passing looks like: `自检 注册对象 97 个 … → OK` unchanged (none of the 5 tasks here enters the registry surface); `menu_types 2` and `block_entities 8` unchanged.
- [ ] **The tool face (A-66)**: How to verify: if `tools/verify_project.py` is run in this section, only one reading may be recorded. What passing looks like: `已核 registry 对象: 93 / 93` is annotated "A-66：待 F-243 同步，此读数少核 4 个对象"; no criterion of this section cites its `FAIL 0`.

### 5.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Judging the distance inside `useWithoutItem` yourself | a double test against the vanilla `stillValid` and one more magic number; how 8.5 blocks away feels gets changed | test "your own" only; leave the distance to the menu's `stillValid` (ruling #17) |
| Treating the 3×3 chunks as an opening condition | standing beside your own core and it will not open (the reverse of document 35 B-23) | `RANGE_CHUNKS = 1` is used for binding and linking only; opening goes through `stillValid` alone |
| Missing `!movedByPiston` | a piston push spawns a fresh copy of the items in place, which becomes a duplication bug | write the four conditions together; verify the four scenarios one by one (3 stacks dropped / piston 0 / same block 0 / unload keeps them) |
| Changing the list while iterating over it during migration | the same cultivator is skipped or handled twice while re-binding | copy `cultivators()` first, as in step 4 of document 36 §2.7, then iterate |
| Writing "turns into dirt" or "knocks the cultivator out and drops items" when it cannot migrate | the three farmland exits of 0.1.1 are copied over (the original symptom of RT-43) | exit ② is "stay in place + clear `Core` + keep the contents"; `setBlock` must be 0 inside the method body |
| Loading chunks to settle the accounts | removing a core force-loads a chain of chunks at once (a stutter) | use `getChunkNow` throughout; when it cannot be read, treat it as exit ③ and leave the NBT values untouched |
| Testing "nearest" before "has space" | "the nearest is full while another place still has space" is wrongly refused, which contradicts the capacity of "16 per core" | **filter the candidate set first** (covers the position + your own + not full), then take the nearest; document 35 §8.2 example 1 is the control for this |
| Writing 16 into `getContainerSize()` | 54 and 16 get mixed up and the screen keeps only 3 rows | `getContainerSize()` returns a reading of `storageSlots` (54); 16 lives in `Cultivators` only |
| Taking an old log from the window as `→ OK` | the two mid-way FAILs of §4 (96 / 97) or the earlier `93 / 93` are taken as passing evidence; `menu_types` is actually already 2 while the assertion constant still sits at 93 | the `→ OK` of this section must come from a **newly started** server after **the close-out**; the line count of the log and the start time must agree |

### 5.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the two additions / changes of `OutpostCoreBlock.java`, the field region / NBT / binding methods / the wholesale rewrite of `:341-394` of `OutpostCoreBlockEntity.java`, and the write points of `CultivatorBlock.java` and `CultivatorBlockEntity.java`.
- **Commands run**: the exit code of `.tools\gw.cmd build`; one server start after deleting the old toml; `grep -c` (`setBlock`, `FarmlandLedger`, `getChunkNow`); the two mtime readings.
- **What was observed**: the slot count and the title of the 54-slot screen; the actual drop list of the four drop scenarios (write "空" for an empty list); the number of entries in `Cultivators`; the verbatim `/data get block` of the three groups (re-bound / stays in place / not in memory); the two mtimes of the `.dat`.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example "the chunk is not in memory" requires stopping the server and comparing the region files; state the tool and the path).
- **Open questions**: for example "does a binding change need one more `CoreListBroadcaster.requestAll`" (the to-be-verified API ⑫ of document 36 §9; the fallback is added in the broadcaster of §7).

---

## 6. Phase 37 · Cultivator behaviour (M33)

### 6.1 Preconditions

- [ ] **entries 3 / 4** of the previous section §5.4 are green: `Cultivators` on disk (F-231) and the four criteria of placement binding (F-232) — the latter is F-235's precondition.
- [ ] The "window state A" of §4 (the `cultivator` trio) and the "resource counts" are green; the `working` property and the two-state model are in place.
- [ ] The toml reading of the **4** `[outpost_core]` keys has been taken (after F-231 it is **173 keys / 59 sections**).
- [ ] These four at hand: document 36 §2.5.2 (the four steps of the state machine), §2.5.3 (the four gates), §2.5.4 (the two yield criteria), §2.5.6 (the break drop); document 35 §6.2 ~ §6.4, §6.8, B-08 ~ B-18; document 34 D-160 ~ D-164, ruling #15; document 37's §6 table F ⑤ ⑦ ⑩.
- [ ] Frozen surface: admission is judged by `tier()` and **not by an id whitelist** (D-162); `working` goes through a blockstate property (D-161); the cultivator BE **does not implement `Container`** (document 36 §2.5.2) — which is why F-237 delivers the drop through a `NonNullList` overload.
- [ ] Lagging window: the language keys (**2** entries of F-234) and config (the **2** keys of `[cultivator]`) only ever write "this key exists" and the interval reading; the totals come at F-241 / F-242.

### 6.2 Goal of this phase

Turn the cultivator from "a block that can be placed" into a complete machine: "admit by tier, yield 2 + 2 every 600 ticks, pause when the core is full, spit everything back when broken".

At the end of this section: Common / Rare go in, Epic (4 crops + 2 Epic seeds) is refused; after 600 ticks the core holds 2 more crops, and the 5 entries that have their own seed add 2 more seeds; when the core is full, `Progress == cycleTicks`, the items neither drop on the ground nor get lost, and once space is freed it resumes from the pause; breaking it drops itself + the one item inside.

### 6.3 Task list (F-234 ~ F-237)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-234 | The 2 keys of the `cultivator` group + 2 language keys | S | 0.5 | F-224 | a new top-level group `cultivator`: `cycleTicks` 600 (20–72000), `yield` 2 (1–64); 2 `farmstorm.configuration.cultivator.*` |
| F-235 | The state machine, the four admission gates and putting the item in | L | 3.0 | F-232, F-234 | three NBT keys (`Held` / `Progress` / `Core`) + the server-side per-tick state machine + the four gates merged into one test + the insertion order of `useWithoutItem` |
| F-236 | The yield and the pause when the core is full | L | 3.0 | F-226, F-235 | one settlement every 600 ticks, the batch decided by the profile (own seed ⇒ crop × yield + seed × yield); when `acceptAll` fails it stops at `cycleTicks` |
| F-237 | The cultivator's break drop | S | 0.5 | F-224 | `onRemove` of `block/CultivatorBlock.java`: after the four conditions, drop the one cultivated item inside through the `NonNullList` overload |

Phase subtotal: **4 tasks, 7.0 person-days** (S 2 / M 0 / L 2).

### 6.4 Exit criteria

- [ ] **The 2 keys of the new group (F-234)**: How to verify: delete the old toml, start the server and read `[cultivator]`. What passing looks like: exactly **2** keys with the values **600 / 2**; the whole file **175 keys / 60 sections** — these two groups are an **interval reading**, and the group-by-group check of `b.push(` and the "total" conclusion come only at F-242.
- [ ] **The four admission gates (F-235 / D-162)**: How to verify: four right-clicks — Common, Epic, occupied slot, unbound. What passing looks like: put a Common crop in → `Held` has a value, `Progress` rises from 0, and F3 shows `working=true`; the 4 Epic crops and the 2 Epic seeds → no reaction, the held item is not consumed; right-click with the slot already occupied → no reaction; right-click an unbound cultivator → no reaction.
- [ ] **Progress persists (F-235 / B-17)**: How to verify: walk 200 blocks away mid-timing and come back; then stop the server and reload. What passing looks like: `Progress` continues from its old value, does not restart the timer, and the slot is not cleared.
- [ ] **The yield classification (F-236 / ruling #15)**: How to verify: one round of 600 ticks for each of the three inputs. What passing looks like: feeding `puffed_wheat` → the core gains **+2** crops **+2** seeds; feeding `ubi_server` (seed = itself) → **+2** crops only; feeding `chive_seeds` and feeding `friend_chives` produce **exactly the same** result.
- [ ] **The pause when the core is full (F-236 / D-164)**: How to verify: fill all 54 slots of the core, then feed it; then free 2 slots. What passing looks like: `Progress == cycleTicks` and it does not advance further, the items do not drop on the ground and are not lost; after **2** slots are freed the delivery completes automatically, without restarting the timer.
- [ ] **The break drop (F-237 / B-10)**: How to verify: three ways of breaking it. What passing looks like: breaking it with a crop in the slot → **1** block + **1** of that crop; an empty slot → **1** block only; a piston push → **0** drops.
- [ ] **Registration and the two totals untouched (negative)**: How to verify: start the server and read the self-check line + read the two total comments. What passing looks like: `自检 注册对象 97 个 … → OK` unchanged; the 2 language keys added here only write "the key exists", and `ModLangProvider.java:13-16` and the config total comment are unchanged character for character.

### 6.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Judging admission by an id whitelist | it drifts the moment `tier` changes and collides with the guard "the 22 entries are defined in one place only" (D-162 rejects it outright) | admission reads `tier()` only; no comparison against a crop id appears anywhere in the `grep` |
| Writing the blockstate once per tick | pointless block updates and client-side jitter | write only when `working` really changes, and with `UPDATE_CLIENTS` |
| `working` driven by BE sync only, not by the property | the model does not switch and the two textures are wasted (D-161 demands the blockstate) | the property is named `working`; verify both variants of `blockstates/cultivator.json` once each |
| Judging "has a seed" with the single condition `seedItem != null` | the 8 "seed = itself" profiles double their yield (document 36 §2.5.4 rejects it outright) | the test is `seedItem() != null` **and** it is not equal to `id()`; inside the admission face **5** entries give 2 + 2 and **13** give 2 only |
| Restarting the timer when the core is full | one full core wastes a whole 600 ticks and the player thinks the machine is broken | `Progress` stops at `cycleTicks` without resetting; once space is freed it continues from the pause |
| Testing "does it fit" item by item | only half goes in while the slot is already cleared ⇒ half the items vanish into thin air | judge the **whole batch** (`canAccept` / `acceptAll`, built at F-226) |
| The yield landing as `ItemEntity` or being stuffed into the player's inventory | it contradicts the plan's own words "the yield goes into the core" (D-164 rejects it outright) | go through `acceptAll` only; the only drop face is the one of F-237 |
| Delivering the inner crop through `Container` | the cultivator BE is not a `Container` and it does not compile | deliver through `NonNullList`; the block itself drops through the loot table, and the two do not overlap |

### 6.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the new group and its two keys in `FarmstormConfig.java`, the 2 entries of `ModLangProvider.java`, the three fields / `serverTick` / `isUsable` / the completion branch of `CultivatorBlockEntity.java`, and `useWithoutItem` and `onRemove` of `CultivatorBlock.java`.
- **Commands run**: the exit code of `.tools\gw.cmd build`; two server starts after deleting the old toml; `/data get block <cultivator>` and `<core>` a few times each; three `grep -c` runs (`tier()`, `seedItem()`, `NonNullList`).
- **What was observed**: the two key readings of `[cultivator]` and the whole-file 175 / 60; the yield list of the three inputs (how many crops / seeds each); the `Progress` reading with the core full and how it changes after clearing; the drop list of the three ways of breaking it.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example the 72000-tick upper bound was not measured, and the recovery delay of the full-core pause was measured only once).
- **Open questions**: for example whether the 1-second recovery delay of "when the pause is detected" is acceptable (the to-be-verified API ⑤ of document 36 §9; the fallback is to add one wake-up callback in the core's `setItem` / `removeItem`).

---

## 7. Phase 38 · Linking (M34)

### 7.1 Preconditions

- [ ] **entry 5** of the previous section §5.4 is green: the three exits of migration (F-233) — it is F-239's precondition, because the broadcaster's "the core was removed" timing hangs on migration and `release()`.
- [ ] Entries 4 / 5 of §6.4 are green (the yield and the full-core pause): the broadcaster's "binding changed" timing has to see the cultivator-side state really changing.
- [ ] Environment: a single-player client and a dedicated server can be started at the same time; a second client (or a second player) is available for "not your own" and for the range comparison.
- [ ] These four at hand: document 36 §2.8.1 ~ §2.8.3, appendix A items 8 / 9; document 35 §8.6, B-38 / B-39; document 34 D-169 / D-172; document 37's §6 table F ② ⑫.
- [ ] **The order around to-be-verified API ②**: whether `RenderType.lines()` is drawable at `AFTER_PARTICLES` — first draw one line at fixed coordinates (independent of the network packet) and confirm it is visible, then write the real logic; the fallback is `AFTER_TRIPWIRE_BLOCKS`.
- [ ] Red-line wording: both packets are S2C read-only and the client sends nothing back; the existing registration and handler of `core_list` are unchanged by one character (do not extend it); `@Mixin` is still **0** repo-wide.
- [ ] There is exactly one range criterion: reuse `OutpostCoreBlockEntity.inRange` (`RANGE_CHUNKS = 1`) and **write no second range formula**; `MAX_LINKS = 1024` is only a decode cap and never truncates.

### 7.2 Goal of this phase

Add the 2nd S2C read-only packet and the client-side wireframe, and draw the "core ↔ cultivator" binding relation.

At the end of this section: standing beside your own core (inside its 3×3 chunks) you see a line between the core and every bound cultivator; walking away makes the lines disappear, a dimension change leaves nothing behind, and breaking a block makes its line vanish at once; the client does not crash while online; after replacing the client handler with an empty implementation, binding / yield / migration / the core break drop all work as usual — the link is a pure observation face.

### 7.3 Task list (F-238 ~ F-240)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-238 | The link packet + its registration + protocol version 2 | M | 1.5 | — | new `network/CultivatorLinkPayload.java` (`Link(core, cultivator)` + `MAX_LINKS = 1024` + the composite codec); a second `playToClient` in `FarmstormNetwork`; `PROTOCOL_VERSION` becomes `"2"`; the self-check line extended to two lines |
| F-239 | The link broadcaster: the range and five timings | L | 3.0 | F-233, F-238 | new `network/CultivatorLinkBroadcaster.java`; five timings (entering the range / leaving it / binding changed / the core removed / login, dimension change, respawn, logout); a 10-tick debounce and a 20-tick rescan; an empty list sent when out of range |
| F-240 | The client-side read-only view and the wireframe rendering | L | 3.0 | F-238 | new `client/ClientCultivatorLinks.java` and `client/CultivatorLinkRenderer.java`; lines drawn in the `AFTER_PARTICLES` stage; `clear()` on a server switch / disconnect |

Phase subtotal: **3 tasks, 7.5 person-days** (S 0 / M 1 / L 2).

### 7.4 Exit criteria

- [ ] **The two packets and the protocol string (F-238)**: How to verify: start the server and read the log + `grep` the registration points. What passing looks like: `latest.log` shows **2** lines of `自检 网络包注册`, with the ids `farmstorm:core_list` and `farmstorm:cultivator_link` and the version string **2** on both; `playToClient` has **2** hits in `FarmstormNetwork.java`; `PROTOCOL_VERSION = "2"`.
- [ ] **A pure vanilla client and a dedicated server (F-238)**: How to verify: join the server with a vanilla client; then start a dedicated server once. What passing looks like: the vanilla client can join (the channel is optional); the dedicated server starts with no `NoClassDefFoundError`; `@Mixin` has **0** hits repo-wide.
- [ ] **Delivery and clearing the residue (F-239 / document 36 §8 #31)**: How to verify: stand beside your own core, walk out of the range, break one cultivator. What passing looks like: a packet with **N** links arrives, N = the number of cultivators bound to that core; out of range an **empty list** arrives and the client's lines disappear; the line of a broken cultivator disappears **at once** (without waiting for the **20**-tick rescan).
- [ ] **Debounce (F-239)**: How to verify: produce a chain of events in the same tick (placement + binding change). What passing looks like: only **1** packet goes out (a **10**-tick debounce); `MAX_LINKS = 1024` is not used as a truncation value.
- [ ] **Rendering (F-240)**: How to verify: stand beside the core and look at the lines, change the dimension, look at the endpoints. What passing looks like: a visible wireframe between the core and every bound cultivator, with **1** small box of **0.25** blocks at each end; the colour RGB `0.30 / 0.85 / 0.90`, alpha `1.0`, line width 1.0; after a dimension change the residue is cleared (document 35 B-38).
- [ ] **No crash (F-240 / RT-45)**: How to verify: keep the client online for **10** minutes and read the client log. What passing looks like: **0** lines of `Missing elements in vertex` and **0** of `IllegalStateException`.
- [ ] **The server side has zero dependency (F-240 / document 36 §8 #34)**: How to verify: temporarily replace the handler with an empty implementation, then re-run the four lines. What passing looks like: binding / yield / migration / the core break drop all work as usual; restore it afterwards and `git status` shows no residue.
- [ ] **The registry surface untouched (negative)**: How to verify: start the server and read the self-check line. What passing looks like: `自检 注册对象 97 个 … → OK` unchanged — neither packet enters `BuiltInRegistries`; payloads go through `RegisterPayloadHandlersEvent`.

### 7.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Forgetting to change the protocol string | a client with the new mod against an old server (or the reverse) fails the handshake and disconnects on join | change the protocol string and the codec in the same commit; `PROTOCOL_VERSION = "2"` has a `grep` criterion |
| The self-check line is still one line | the criterion of document 36 §8 #2 is **two lines**, and it cannot be counted at acceptance | extend the self-check to two lines (or rewrite it as two lines), both carrying the id and the version string |
| Stuffing the links into the `core_list` codec | a full snapshot and a range delta get mixed into one kind of data, and the overlay and the links drag each other down (D-169 rejects it outright) | keep the new packet separate; the codec and handler of `core_list` are unchanged by one character |
| No empty packet when out of range | the client's lines stay hanging and residue remains at a distance (the original symptom of document 35 B-38) | send an **empty list** out of range; both a dimension change and a disconnect need `clear()` |
| Recomputing "is the player inside the 3×3" yourself | it drifts from `inRange` and two ranges give two answers | reuse `OutpostCoreBlockEntity.inRange` only; do not copy `RANGE_CHUNKS = 1` |
| Hand-written vertices missing `NORMAL` | `Missing elements in vertex` and the client crashes (RT-45) | use `LevelRenderer.renderLineBox` + `RenderType.lines()`; do not build your own `RenderType` |
| Changing the `getPoseStack()` the event hands you | the later stages get translated along with it and the whole picture is misaligned | create a new `PoseStack` and only `translate(-cam.x, -cam.y, -cam.z)` |
| Referencing a rendering class on the server side | `NoClassDefFoundError` on the dedicated server | the handler does `enqueueWork` only; rendering classes appear only in client events |

### 7.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the shape and codec of `CultivatorLinkPayload.java`, the three places of `FarmstormNetwork.java` (the version string / the second registration / the self-check line), the five timings of `CultivatorLinkBroadcaster.java`, and the two client classes plus the mounting line of `FarmstormClient.java`.
- **Commands run**: the exit code of `.tools\gw.cmd build`; single-player + dedicated server once each; `grep -c` (`playToClient`, `@Mixin`); the complete grep over the client's 10-minute log.
- **What was observed**: the two self-check lines verbatim; the N inside the range and the number of cultivators on the lines; two screenshots before and after leaving the range and breaking a block; the residue comparison before and after a dimension change; the four functional readings of the empty-handler round.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example when no second client is available, the item "the other side cannot see the lines" must state the substitute method).
- **Open questions**: for example how `RenderType.lines()` looks under shader mods (the second half of document 36 §9 ②); this section only records what is seen on site.

---

## 8. Phase 39 · Close-out and full acceptance (M35)

### 8.1 Preconditions

- [ ] **entries 1 / 4 / 6** of the previous section §7.4 are green: the two lines of the network-packet self-check with protocol string 2, the debounce, and the client staying up for 10 minutes.
- [ ] **Every** exit criterion of §1 ~ §7 is green; of them §6.4 entries 4 / 5 (the yield and the pause) and §7.4 entries 5 / 6 (rendering and no crash) are the body of the 36 acceptance entries.
- [ ] The three lagging windows reach their close-out point at the same time: the registry self-check closed at F-228 (`97 … → OK`); the language keys close in this section at F-241; config closes in this section at F-242.
- [ ] **A-69 is unlocked**: F-233 has landed and the `.dat` has no runtime reader — "the mtime is unchanged" may be executed as a criterion only from this section on.
- [ ] **The false green of A-66 persists up to this section**: before F-243 `tools/verify_project.py` prints `已核 registry 对象: 93 / 93` for the registry surface and exits 0; after F-243, quoting it must come with `97 / 97`.
- [ ] The record directory: `.work/v020-verify/` is writable (`acceptance.md` / `boundaries.md` / `signoff.md` + screenshots).
- [ ] Of the **6** tasks here, F-241 / F-242 / F-243 change files, while F-244 / F-245 / F-246 **change no code at all** (they only produce records); if an acceptance fails, go back to the corresponding task and redo it — do not rewrite the criterion on the spot.
- [ ] Read the upstream only: `docs/zh/34` ~ `docs/zh/36` are unchanged by one character (`git diff --stat docs/` is empty).

### 8.2 Goal of this phase

Square the three totals (337 / 175 / 97), run the 36 acceptance entries and the 40 boundaries to the end, and sign off the red lines and upstream consistency.

At the end of this section: **337** keys in each of the two lang files with identical key sets, `farmstorm-server.toml` at 175 keys / 60 sections, the registry surface at 97, `@Mixin` 0, `playToClient` 2, all 36 + 40 = 76 entries with a reading, and an empty `git diff` for the three upstream documents (34 / 35 / 36) — it can be released.

### 8.3 Task list (F-241 ~ F-246)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-241 | The language-key ledger at 337 and the class comment | M | 1.5 | F-215, F-221, F-222, F-224, F-226, F-227, F-231, F-234 | re-run datagen; **337** keys in each of the two lang files; the item-by-item composition comment at `ModLangProvider.java:13-16` made to agree with the key set |
| F-242 | The config ledger at 175 keys / 60 groups | M | 1.5 | F-220, F-222, F-226, F-231, F-234 | delete the old `farmstorm-server.toml` and regenerate; check every group against the 36 group table of document 36 §4.1; `outpost_core` 4 keys with no `farmlandBonus` |
| F-243 | Syncing the verification tool to 97 / 175 / 337 | S | 0.5 | F-228, F-241, F-242 | four places of `tools/verify_project.py` synced: `EXPECTED` / `EXPECTED_COUNTS` / the config expectation source / the printed string; one live criterion |
| F-244 | The 36 acceptance entries of document 36 §8 | L | 3.0 | F-215~F-240 (see §3) | new `.work/v020-verify/acceptance.md` (36 rows + the header); ≥ 13 screenshots of manual scenarios |
| F-245 | Re-running the 40 boundaries of document 35 §12 | L | 3.0 | F-244 | new `.work/v020-verify/boundaries.md` (40 rows + the header); B-01 ~ B-40 entry by entry |
| F-246 | Signing off the red lines, the registry surface and upstream consistency | M | 1.5 | F-243, F-244, F-245 | new `.work/v020-verify/signoff.md`: an 8-row signature table + an empty `git diff` for the four upstream documents |

Phase subtotal: **6 tasks, 11.0 person-days** (S 1 / M 3 / L 2).

> F-244's dependency is written as "F-215~F-240 (see §3)", character for character identical to the §1 table of document 37; the "§3" inside it means **the dependency graph of document 37 §3** (the §3 of this file is Phase 34, not the same place).

### 8.4 Exit criteria

- [ ] **The language-key ledger (F-241)**: How to verify: take both key sets with `ConvertFrom-Json` + count them prefix by prefix. What passing looks like: **337** keys each with identical key sets (`Compare-Object` is empty); `farmstorm.configuration` **178** / `item` **33** / `block` **27** / `container` **2** / `tooltip` **20** / `message` **0**; `323` has **0** hits in `ModLangProvider.java` and `337` ≥ **1**.
- [ ] **The config ledger (F-242)**: How to verify: delete the old toml, regenerate and read group by group. What passing looks like: **175 keys / 60 sections** (counted with `^\s*\[`); `[outpost_core]` **4** keys and **no** `farmlandBonus`; `[cultivator]` **2** keys (600 / 2), `[trade]` **6** keys (2 / 5 / 10 / 32 / 40 / 64); `farmstorm-client.toml` still **3** keys; `b.push(` appears **60** times in `FarmstormConfig.java`.
- [ ] **The tool's four readings given together (F-243 / A-66)**: How to verify: `python -X utf8 tools/verify_project.py`. What passing looks like: exit code **0**, with `已核 registry 对象: 97 / 97`, `175 个配置键与文档逐字一致`, `共 337 键` and `结论：FAIL 0 项` appearing **at the same time**. A criterion that writes only `FAIL 0` without `97 / 97` is treated as a false green without exception.
- [ ] **The tool's live criterion (F-243)**: How to verify: temporarily change `EXPECTED_COUNTS["blocks"]` to **28**, run it, then change it back. What passing looks like: a non-zero exit code after the change and back to **0** after restoring it; `git diff --stat docs/` is empty (the tool reads the upstream only and never writes back).
- [ ] **The 36 acceptance entries (F-244)**: How to verify: read `.work/v020-verify/acceptance.md`. What passing looks like: **36** rows (+ the header) with **0** failing; both the "观察位置" and the "实际值" column of every row are non-empty; the log verbatim in #1 matches document 36 §8 #1 character for character (including `→ OK` and the 12 items); ≥ **13** screenshots of manual scenarios (three for #6 ~ #8, three for #18 ~ #20, four for #23 ~ #26, four for #31 ~ #34).
- [ ] **The 40 boundaries (F-245)**: How to verify: read `.work/v020-verify/boundaries.md`. What passing looks like: **40** rows (+ the header) with **0** mismatches; B-12 / B-13 / B-15 / B-16 / B-19 / B-20 each carry the actual NBT value from `/data get block` (the presence or absence of the `Core` key is the point); B-26 / B-27 each record the actual drop list (write "空" for an empty list, never leave it blank); B-40 states "restored on a temporary copy, put back after verification, `git status` with no residue".
- [ ] **The timing of the three records (A-69 / A-66)**: How to verify: read the timing-related lines of the three records through. What passing looks like: the `.dat` mtime criterion appears only in #14 of F-244 and item ⑦ of F-246 (**2** places in all), and both after F-233; before F-241 / F-242 there is no conclusion that "the total is in place".
- [ ] **The signature (F-246)**: How to verify: three `grep` runs + count the lines of the two classes + read the mtime. What passing looks like: `git diff --stat docs/zh/34-DECISIONS-0.2.0.md docs/zh/35-DESIGN-0.2.0.md docs/zh/36-TECH-0.2.0.md` is empty; `@Mixin` **0**, `playToClient` **2**, `PROTOCOL_VERSION = "2"`; `FarmlandLedger.java` **555** lines and `FarmlandGuard.java` **225** lines; `world/data/farmstorm_farmland.dat` exists and its mtime matches the one before the upgrade.
- [ ] **The 8-row signature table (F-246)**: How to verify: read `signoff.md`. What passing looks like: the registry surface **97** (12 per-category items) / config **175 keys 60 groups** / language keys **337 × 2** / the action surface **46 / 46** / the three red-line items (`@Mixin` 0, `playToClient` 2, protocol string `2`) / the **5** self-check numbers of `Farmstorm.java` / the `.dat` / the line counts of the two dead classes — every row gives "判据 / 实测值 / 结论".
- [ ] **The 76 entries and the 34 tasks all have a conclusion**: How to verify: the two records and the 34-row task checklist. What passing looks like: 36 + 40 = **76** entries each with a reading and **0** failing; the 34-row task checklist shows **0** "未达成"; each of the two records carries its own "未通过 0" and neither signs for the other (the 36 are the technical face, the 40 the boundary face).

### 8.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Writing only "FAIL 0" | the tool exits 0 even when it checks 4 registry objects short, and acceptance looks all green (A-66) | `97 / 97` and `FAIL 0` **must appear together**; miss one and it is a false green, and the criterion is invalid |
| Changing only `EXPECTED_COUNTS`, not the `EXPECTED` list | the count is right but the list is stale, and the tool goes red at once ("list length = expected count" + "every id appears" both take effect) | change four things together: the list, the counts, the config expectation source, the printed string |
| Hard-coding the 10 new keys into a second list | the tool degenerates from "recompute from the documents" to "copy an answer sheet", and the original defect of A-66 revives on the spot | the expectation set is still computed from the documents (document 36 §4.1.1 joins the union); write no second list |
| Changing the json but not the class comment | `ModLangProvider.java:13-16` still says 323 and the next person recomputes from the old comment | the comment and the key set in the same commit; `323` goes to 0 hits and `337` ≥ 1 |
| Counting the keys without deleting the old toml | `farmlandBonus` stays in the file as an orphan key, 175 becomes 176 and `[outpost_core]` becomes 5 keys | delete the old one, regenerate, then count; count groups with `^\s*\[`, not `^\[` |
| Mixing the 36 and the 40 in one record | the two records stand in for each other, and not one of the 21 boundaries "with no corresponding assertion" is verified | keep the two files apart: `acceptance.md` 36 rows, `boundaries.md` 40 rows |
| Using a save older than F-233 to verify #14 | the `.dat` is judged red and on site it looks like "the implementation is broken" | #14 and signature item ⑦ both come after F-233 (A-69); confirm the F-233 timestamp first |
| Rewriting the criterion when acceptance fails | three rounds of acceptance become void and `src/` changes appear in `git status` | F-244 ~ F-246 here **change no code**; `git status` may show `.work/v020-verify/` only |
| Judging B-33 / B-34 as defects | a max-level farmer in a legacy save is not granted the trades again, and the trade is a sample — these two are **accepted costs** | write "as expected, not failed" per document 35 §4.6 / document 36 §2.4.2 |

### 8.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; F-241 touches only the two lang files + the class comment, F-242 only the regenerated toml, F-243 only `tools/verify_project.py`; F-244 ~ F-246 add the three records and the screenshots.
- **Commands run**: the exit code of `.tools\gw.cmd build`; **all four sections verbatim** of `python -X utf8 tools/verify_project.py` plus its exit code; the key-set result of `Compare-Object`; the three `grep` runs (`@Mixin`, `playToClient`, `PROTOCOL_VERSION`); the line counts of the two classes; the mtime of the `.dat`.
- **What was observed**: the per-prefix readings 337 / 178 / 33 / 27 / 20 / 0; 175 keys / 60 sections and the key counts of the four groups; the verbatim line `97 / 97`; the entry-by-entry conclusions of the 76 entries (36 rows + 40 rows); the 8-row signature table.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example some feel-based entry has no screen recording, or the second client was not available).
- **Open questions**: A-62 asks for a feed-back into document 34 §3.3 and document 35 §7.2 (the field is kept), A-69 asks document 36 §8 #14 to gain the timing, and A-67's D number needs registering — all three are upstream feed-backs; this section only registers them and does none of them.

---

## 9. Where the cross-Phase disciplines land (ledger)

| # | Discipline | Which sections' "Preconditions / Pitfalls / criteria" it lands in | What you see on site once it is violated |
|---|---|---|---|
| 1 | The registry self-check window **F-224 → F-228** | preconditions: §4 (the window opens here), §5 (the close-out is confirmed); pitfalls: §1 (opening the window early), §4 (writing OK inside the window, changing the constants early); criteria: §4 entries 1 / 6 / 7 (96 → 97 → OK) | one `→ OK` criterion inside the window ⇒ every later server start carries one permanently red noise line and a real fault is buried |
| 2 | The language-key window **F-215 → F-241** | preconditions: §1 (only 4 entries added), §8 (the window closes here); pitfalls: §1 (changing the total comment), §3 (writing the total into a criterion), §4, §6, §8; criteria: §1 entry 6, §3 entry 5, §6 entry 7, §8 entry 1 | writing 337 mid-way ⇒ every Phase has to recompute the comment, and one section will get it wrong |
| 3 | The config window **F-220 → F-242** | preconditions: §3 (interval readings), §8 (the window closes here); pitfalls: §3 (treating 175 as a total), §4, §6, §8 (not deleting the old toml); criteria: §3 entries 4 / 5, §4 entry 9, §6 entry 1, §8 entry 2 | treating 175 as a total mid-way ⇒ the group-by-group check of F-242 loses its baseline |
| 4 | The timing of **A-69** (the `.dat` only after F-233) | preconditions: §3, §5 (entry 7 is executed after F-233), §8; pitfalls: §3 (asserting the mtime early), §5 (verifying with an old save); criteria: §3 entry 9, §5 entry 7, §8 entry 7 | asserting the mtime before F-233 ⇒ removing a core rewrites the file, the criterion must go red, and it looks like the document is wrong |
| 5 | The false green of **A-66** (4 objects short and still exit 0) | preconditions: §5 (only allowed to record 93 / 93 with a note), §8; pitfalls: §5, §8 (writing only FAIL 0, hard-coding a second list); criteria: §5 entry 9, §8 entries 3 / 4 | writing only `FAIL 0` ⇒ the 4 new objects are never checked, acceptance is all green and nobody has verified them |
| 6 | **A-62** (`farmlandBonus`: comment out the definition only, keep the field) | preconditions: §3 (this section's wording follows A-62); pitfalls: §3 (commenting the field out as well per document 34 / 35); criteria: §3 entry 3, §8 entry 9 | the field commented out as well ⇒ `FarmlandGuard` / `FarmlandLedger` do not compile and two classes promised "not one line changed" are forced to change |

---

## 10. Consistency self-check of this file

| # | Check | Criterion | Result |
|---|---|---|---|
| 1 | Task numbers consecutive, no gap, no duplicate | **34** rows across the 8 task tables; the range `F-213 ~ F-246` ⇒ `246 − 213 + 1 = 34` | pass |
| 2 | Phase and milestone correspond one to one | Phase **32 ~ 39**, **8** in all; milestones **M28 ~ M35**, **8** in all; every section heading carries its own milestone number | pass |
| 3 | Six subsections per section, fixed order | every section is "Preconditions / Goal of this phase / Task list / Exit criteria / Pitfalls / Reporting format"; **6** subsections × **8** Phases = **48** subsection headings, not one of them out of order | pass |
| 4 | Task number / title / complexity / person-days / dependency agree with document 37 | all 34 rows compared row by row against the §1 table of document 37 and the task title lines: numbers **34/34**, titles **34/34**, S/M/L **34/34**, person-days **34/34**, dependencies **34/34** | pass |
| 5 | Person-days self-consistent | the eight subtotals `5.0 + 2.0 + 6.5 + 12.5 + 9.5 + 7.0 + 7.5 + 11.0 = 61.0`; complexity `S 11 / M 9 / L 14`; `61.0 × 1.25 = 76.25` | pass |
| 6 | Exit-criteria total and distribution | **66** entries in all: section 32 **7** / 33 **7** / 34 **9** / 35 **9** / 36 **9** / 37 **7** / 38 **8** / 39 **10**; every entry starts with `- [ ]` and every entry carries a number, a key name or a path | pass |
| 7 | The in-between window written right | the window falls only in §4 (opens at F-224, closes at F-228): the four related criteria of §4 write only **96 / FAIL** and **97 / FAIL**, and the only one writing OK is the close-out entry; §1 ~ §3 are **before the window** and `93 … → OK` is right there; from §5 on it is `97 … → OK` | pass |
| 8 | The two lagging windows written right | language keys: §1 writes only "4 keys + the prefix 16 → 20" and the total only in §8 entry 1; config: §3 / §4 / §6 write only toml interval readings and the total only in §8 entry 2 | pass |
| 9 | The three A-62 / A-66 / A-69 wordings | A-62 in the §3 preconditions and entry 3; A-66 in §5 entry 9 and §8 entries 3 / 4; A-69 in §3 entry 9 and §5 entry 7 (after F-233) | pass |
| 10 | No invented numbers | 97 / 96 / 93 / 175 / 173 / 172 / 171 / 165 / 337 / 178 / 323 / 76 / 46 / 40 / 36 / 34 / 22 / 18 / 16 / 13 / 11 / 11 / 7 / 5 / 4 / 2 / 600 / 555 / 225 / 54 — every one traces back to document 34 / 35 / 36 / 37 or to arithmetic on them | pass |
| 11 | Read the upstream only, create one file only | `git status` shows this file only; `docs/zh/34` ~ `docs/zh/37`, `docs/en`, `src` and `.work` are unchanged by one character | pass |
| 12 | Banned words and tone | no undecided-item marker and no todo placeholder anywhere; the person is "the implementer / the author", never a second-person honorific; every concluding sentence is imperative | pass |
