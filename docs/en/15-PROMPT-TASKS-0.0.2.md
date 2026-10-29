# 15 · v0.0.2 Phased Task List (Task Prompt)

| Item | Value |
|---|---|
| Document ID | 15-PROMPT-TASKS |
| Version | v0.0.2 |
| Audience | The person feeding the coding agent + the CLI coding agent being fed (Claude Code / Codex class) |
| Upstream | `.work/v002-BRIEF.md` (the single source of wording), `docs/zh/10-DECISIONS-0.0.2.md` (D-49 ~ D-79), `11-DESIGN-0.0.2.md`, `12-TECH-0.0.2.md`, `13-TASKS-0.0.2.md` (the single source for F-60 ~ F-99) |
| Peer | `14-PROMPT-MAIN-0.0.2.md` (the resident main prompt; this file does **not** repeat its hard constraints or its definition of done) |
| Loading method | Feed once per Phase: the full text of `14-PROMPT-MAIN-0.0.2.md` + the matching section of this file |
| Contents | 5 Phases / 40 tasks (F-60 ~ F-99); task IDs, titles, complexity, person-days, and dependencies align verbatim with `13-TASKS-0.0.2.md` |
| Boundaries | Upstream is read-only: do not change `06-PROMPT-TASKS.md` (the frozen v0.0.1 file), `10` / `11` / `12` / `13`, `.work/v002-BRIEF.md`, or `src/` |

---

## 0. How to Use This File (for humans)

### 0.1 Division of Labor Between This File and 14-PROMPT-MAIN-0.0.2.md

| File | What it governs | Feed frequency |
|---|---|---|
| `14-PROMPT-MAIN-0.0.2.md` | Global discipline and hard constraints (the engineering red lines, code conventions, execution discipline, definition of done, report format, prohibited actions, known traps) | Resident in every phase |
| `15-PROMPT-TASKS-0.0.2.md` (this file) | Which tasks to do this round, what counts as passing, and how to open the next phase after delivery | One section at a time |

Three rules:

1. This file does not rewrite the hard constraints; it cites their IDs only. When you see `D-xx` (a decision) go back to `10`, `RT-xx` (a risk) and `§11 #n` (an acceptance self-check) go back to `12`, `A-0x` (an adjudication) and `F-xx` go back to `13`, and `HC-15` ~ `HC-28` and `N-15` ~ `N-28` go back to `14` — do not interpret from memory.
2. Adjudication order on conflict: `10-DECISIONS-0.0.2.md` (the single source of truth for v0.0.2) > `12-TECH-0.0.2.md` / `11-DESIGN-0.0.2.md` > `13-TASKS-0.0.2.md` > this file; `.work/v002-BRIEF.md` is the flattened construction-period draft, and where it conflicts with `10`, `10` wins (A-06 in §0.4 was adjudicated by exactly this rule). Existing v0.0.1 content still follows `00-DECISIONS.md`.
3. This file's acceptance criteria only tighten within the definition of done of `14-PROMPT-MAIN-0.0.2.md`; they invent no new deliverables.

### 0.2 How to Feed It

1. Open this file, find the section for the current Phase (the heading looks like `## 2. Phase 11 · Foundation and Registry Increment (F-60 ~ F-66)`), and copy the whole section starting from the heading.
2. Feed it to the coding agent together with the **full text** of `14-PROMPT-MAIN-0.0.2.md` — **one Phase at a time**.
3. Wait for it to hand back a report per `14`'s report format, then accept item by item against this section's "phase exit criteria".
4. Once acceptance passes, open the next phase with the hand-off wording in §7.

Two prohibitions:

- Do not feed the §1 overview table along with it — the agent will try to finish all five phases at once.
- Do not force Phase 15 into numeric order: `F-97` (the 41 language entries) depends on `F-98` (the three advancements), because the 6 advancement text keys depend on 3 advancement paths; doing `F-97` first leaves 6 keys missing.

### 0.3 How to Judge That a Phase Is Complete

All three gates must be green to pass; missing one is handled as "not done":

1. **Exit criteria**: every item of this section's "phase exit criteria" can be pointed to a log line, the return value of `/execute` / `/data get` / `/advancement` / `/playsound`, the content of a file under `src/generated/resources` or of `config/farmstorm-server.toml`, or the artifact in `build/libs` / `dist`. "Implemented but not verified" does not pass.
2. **Task coverage**: every F-xx in the task list has identifiable evidence. When the agent skipped one, require it to be made up per `14`'s definition of done — do not fill it in yourself.
3. **Repository state**: `.tools\gw.cmd build` exits 0; `git status` shows only files inside this section's boundary; the frozen files `docs/zh/00`–`09` and `docs/en/00`–`09`, `.work/v002-BRIEF.md`, and the fields of the 15 existing `CropProfile`s are untouched (BRIEF §3.4).

Order of handling when a phase fails: first have it self-check per `14`'s self-check section → if that still fails, copy the failing acceptance items back into the task prompt and dispatch once more → only after two failures, redo it in a new session, naming in the hand-off which item it is stuck on.

### 0.4 The Five Latest Adjudications (they override the old wording of 13-TASKS-0.0.2.md §0.4; getting them wrong means rework)

| # | Adjudication | Old wording (judged wrong) | Correct wording |
|---|---|---|---|
| **A-01** | Molotov needs exactly one probability key | `molotov.fireChance` + `molotov.magmaChance` as two independent percentages; or a second key for magma blocks plus a "the two sum to 100" normalization rule | Land only `molotov.fireChance` (double, default 85.0 %, range 0.0–100.0); **the remaining percentage goes to magma blocks automatically**, with no second key (which is why the new keys total 22) |
| **A-03** | Zangief Cane phase 2 is served tick by tick with `fallTicks`, not at a fixed speed | `zangief_cane.dropVelocity`: set the downward velocity once on entering phase 2, then leave it constant | `zangief_cane.fallTicks` (int, default 40 ticks, range 1–200); **reset the downward velocity every tick as "remaining height ÷ remaining ticks"** so the player lands within that duration. The reason: the plan's constraint is a **time** ("land within 1.7–2.0 seconds"), and a fixed speed necessarily fails once the height changes |
| **A-05** | Default of the horizontal spin speed | `zangief_cane.spinDegreesPerTick = 24.0` (the old text of BRIEF §8), or writing 24 while saying "1440 degrees per second" in the same sentence | `zangief_cane.spinDegreesPerTick = 72.0` (double, range 0.0–360.0): 1440 ÷ 20 ticks/second = 72°/tick; over 40 ticks that accumulates 2880° = 8 full turns. The default-value text of the language key `farmstorm.configuration.zangief_cane.spinDegreesPerTick` says 72.0 as well |
| **A-06** | The consequence matrix of Compressed Hay Block's resistance 6.0 | "The Melon Grenade cannot blow it up, the Ubi Server can, and **vanilla TNT cannot either**" — treating 6.0 as able to stop vanilla explosions | Melon Grenade and Melon Slice Bomb (threshold 6.0) **cannot break it** (the check is `resistance < maxResistance` and `6.0 < 6.0` is false); Ubi Server and Toxic Ubi Server (threshold 3.6e6) **can break it**; **vanilla explosions (TNT included) can break it too** (the vanilla formula accumulates cell by cell, `f -= (resistance + 0.3) * 0.3`, and it breaks as soon as `f > 0`; with TNT power 4 the starting range of f is [2.8, 5.2]) |
| **A-07** | What D-77's "follow Puffed Wheat" actually follows | "The explosion power and damage cap follow Puffed Wheat" — a sentence invented from nothing: there is no `Explosion` call anywhere in `PuffedWheatProjectile` | Follow the two **landing-write** rules: ① the cube's edge length is 3 dry / 5 in water, written directly over air / fluid / solid blocks, and the original block drops nothing; ② when the original block at the target cell has resistance ≥ 3.6e6, skip it. The only change is swapping the written block from `minecraft:hay_block` to `farmstorm:compressed_hay_block` |

### 0.5 Number Quick Reference

| # | Quantity | Value | Source |
|---|---|---|---|
| 1 | Number of `CropProfile`s | **19** (15 → 19) | BRIEF §3.1 / §4; `10-DECISIONS-0.0.2.md` §4; `12-TECH-0.0.2.md` §3.1 |
| 2 | Total registered objects | **77** (64 → 77; the startup self-check line carries the 12-category subtotals) | BRIEF §4; `12-TECH-0.0.2.md` §2.1 / §11 #2 |
| 3 | config keys | **139** (117 + 22 new) | BRIEF §8; `12-TECH-0.0.2.md` §8.1 / §11 #16 |
| 4 | Total resource files | **302** (265 + 37 new; JSON 195 / PNG 100 / OGG 2 / GLSL 3 / TOML 1 / `pack.mcmeta` 1, excluding the 11 under `.cache`) | BRIEF §4.1; `12-TECH-0.0.2.md` §7.1 / §11 #17 |
| 5 | Language-file keys | **263** (222 + 41 new; the Chinese and English key sets must be equal) | BRIEF §8; `12-TECH-0.0.2.md` §7.3 / §11 #18 |
| 6 | Resource deliverables | **40 rows** (PNG 9 / OGG 2 / JSON 26 / GLSL 3), all landed | `12-TECH-0.0.2.md` §7.2; `13-TASKS-0.0.2.md` §9.3 |
| 7 | New language keys | **39** (config 22 + item 5 + block 2 + effect 2 + subtitles 2 + advancements 6) | `12-TECH-0.0.2.md` §7.3; `13-TASKS-0.0.2.md` F-97 |
| 8 | New config keys | **22** (molotov 3 / wild_apple 2 / wild_hunter 11 / zangief_cane 6) | BRIEF §8's single list; `12-TECH-0.0.2.md` §8.1 |
| 9 | Advancements | **15** (12 + 3) | BRIEF §4; `10-DECISIONS-0.0.2.md` D-63; `12-TECH-0.0.2.md` §11 #19 |
| 10 | Conversion recipes | **24** (21 + 3) | BRIEF §4; `10-DECISIONS-0.0.2.md` D-54; `13-TASKS-0.0.2.md` F-73 |
| 11 | Type distribution (Throwable / Deployable / Edible) | **10 / 6 / 3** | BRIEF §4; the count-consistency table in `10-DECISIONS-0.0.2.md` §4 |
| 12 | Demolition class | **4** (unchanged) | BRIEF §4; `10-DECISIONS-0.0.2.md` §5.3; D-42 |
| 13 | Tasks | **40** / **5** Phases (F-60 ~ F-99; 7 / 9 / 10 / 8 / 6) | `13-TASKS-0.0.2.md` §2 / §12.2 |
| 14 | Person-days | **60.5** (S 10 / M 23 / L 7; = 5.0 + 34.5 + 21.0), plus a 25 % buffer to **75.625** (recorded as 75.6) | `13-TASKS-0.0.2.md` §0.2 / §2 |

Two reminders on the wording:

- The three v0.0.1 bases are **117** (config) / **262** (resources) / **222** (language keys), not the earlier 216 / 257 in `06-PROMPT-TASKS.md` — those two figures have been superseded by measurement; BRIEF §4.1 wins.
- The `.cache` directory under `src/generated/resources` holds **11 extensionless files**; that is DataGen cache, it **does not go into the jar and does not count toward 302**; deduct it before counting resources.

---

## 1. Overview Table (for humans; do not feed it together with a phase)

| Phase | Title | Tasks | Task IDs | Person-days | Prerequisite phases | Exit criteria (one sentence) |
|---|---|---|---|---|---|---|
| Phase 11 | Foundation and Registry Increment | 7 | F-60 ~ F-66 | 11.0 | — | `gw.cmd build` produces the 0.0.2 jar; all four self-check lines are OK (19 crops / 77 registered objects / 16 berry pool entries / 11-11 renderers); `farmstorm-server.toml` has 139 leaf nodes |
| Phase 12 | Compressed Items and Molotov! | 9 | F-67 ~ F-75 | 10.5 | Phase 11 | the 5 new recipes work; throwing a Molotov only appends fire / magma block and not one original block goes missing; Compressed Hay Block does not break to the Melon Grenade or the Melon Slice Bomb and does break to the Ubi Server and to TNT |
| Phase 13 | Wild Hunter | 10 | F-76 ~ F-85 | 16.0 | Phase 11 | after eating one Wild Apple, seven abilities are observable (attributes / attraction / damage knockback / time extension / world-only grayscale / red silhouette / loop sound), and `/effect clear` resets all of them |
| Phase 14 | Zangief Cane | 8 | F-86 ~ F-93 | 11.5 | Phase 11 | the three phases run through in order; landing time from all three heights falls within 40 ± 2 ticks; of the 21 hand-tested key bindings only ESC is the exception; `Rotation` is never written back |
| Phase 15 | Resources, Audio, Advancements and Acceptance | 6 | F-94 ~ F-99 | 11.5 | Phase 11 + 12 + 13 + 14 | 302 / 263 / 139 / 77 all line up; all 22 self-checks of `12-TECH` §11 pass; `dist/farmstorm-0.0.2+mc1.21.1.jar` is produced |
| **Total** | **5 Phases** | **40** | **F-60 ~ F-99** | **60.5** | | |

Reading notes:

- In the official dependency graph Phase 12 / 13 / 14 **do not depend on each other**; all three content lines wait only on Phase 11, so they can be given to three people separately (clusters B / C / D in `13-TASKS-0.0.2.md` §1.1 and §8.1). This file still lays them out in numeric order; reordering becomes meaningful only when those three lines run in parallel.
- The critical path runs through Phase 14: `F-60 → F-62 → F-63 → F-86 → F-87 → F-88 → F-92 → F-93 → F-99` = 14.5 person-days / 9 tasks, only 24 % of 60.5. The schedule is not pinned by the longest chain, so to shorten it add parallelism first — do not compress the critical path.
- Phase 15 is the only convergence point: `F-99` depends on F-75 / F-85 / F-93 / F-94 / F-96 / F-97, and if any one of the four earlier phases has not passed, F-99 may not start.
- `F-97` comes after `F-98` in numbering and its dependency column says it waits for `F-98` — **the feed order is F-98 → F-97**, the only place in this whole file where "numeric order ≠ execution order".
- Complexity distribution: S 10 / M 23 / L 7. The 7 L tasks are `F-64` (two new registers), `F-79` (the dash), `F-82` (grayscale), `F-92` (input lock), `F-94` (the media pipeline), `F-95` (textures), and `F-99` (acceptance and packaging) — all seven are "one mistake and the whole piece is redone", so feed them with generous context.

---

## 2. Phase 11 · Foundation and Registry Increment (F-60 ~ F-66)

> Feed method: the full text of `14-PROMPT-MAIN-0.0.2.md` + this section; feed one phase at a time.

### 2.1 Prerequisites

- v0.0.1 is released: `dist/farmstorm-0.0.1+mc1.21.1.jar` is in place, the 74 `.java` files under `src/main/java/com/dashpig/farmstorm/` compile, and `.tools\gw.cmd build` exits 0 (BRIEF §1).
- `12-TECH-0.0.2.md` §2.1 (the 77-object check table), §3.1 (the 4 new `CropProfile` constants), §3.3 / §3.4 (the two new `DeferredRegister`s), §8.1 (the 22-key list), and §11 #1–#6 have been read; the 19 existing files in BRIEF §3.3's whitelist have been copied into this phase's change list.
- Each task in this phase appends only its own piece: `registry/FarmstormItems.java` and `registry/FarmstormBlocks.java` are written by F-65, `registry/FarmstormEntityTypes.java` and `client/FarmstormClient.java` by F-66, and the two tasks never edit the same file at the same time.
- The frozen inventory has been confirmed untouched: `docs/zh/00`–`09`, `docs/en/00`–`09`, any field of the 15 existing `CropProfile`s, the default values of the existing 117 config keys, the 21 existing conversion recipes, and the 12 existing advancements (BRIEF §3.4).
- The only accepted build entry is `.tools\gw.cmd`; do not call gradle directly (BRIEF §1).
- This phase produces no audio, textures, or models (those three arrive in Phase 15's `F-94` / `F-95` / `F-96`); the `wild_hunter` HUD icon will be a purple-black square at this point, and F-64's acceptance only requires "the effect exists and `/data get` reads `amplifier` and `show_icon`", not that the icon looks good.

### 2.2 Goal

Stand the v0.0.2 registration layer up in one pass: the version switched to 0.0.2, the 22 new config keys, the 19 crop profiles, the 6th `DeployedKind` enum value, the two new `DeferredRegister`s `mob_effects` / `sound_events`, 5 items / 2 blocks / 2 entity types, and renderers at 11/11. The four startup self-check lines (19 / 77 / 16 / 11-11) all OK and 139 leaf nodes in `farmstorm-server.toml` are the only shape in which this phase is complete — the following three phases only fill in behavior and no longer change the shape of these four places.

### 2.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-line deliverable |
|---|---|---|---|---|
| F-60 | Build baseline switch and 5 new resource directories | S / 0.5 | — | `mod_version` in `gradle.properties` switches to 0.0.2; create the five new directories `sounds/`, `textures/misc/`, `textures/mob_effect/`, `shaders/core/`, `post_effect/` under `assets/farmstorm/`; zero changes to `build.gradle` |
| F-61 | config: 22 keys in four groups | M / 1.5 | F-60 | `FarmstormConfig` lands molotov 3 / wild_apple 2 / wild_hunter 11 / zangief_cane 6 = 22 keys + the `bake()` baked snapshot |
| F-62 | 4 new crop profiles, `ALL` at 19, and a 16-entry Lucky Berry pool | M / 1.5 | F-60 | `CropProfile` gains 4 constants and `ALL` reaches 19; `LUCKY_BERRY_POOL` changes to explicitly exclude 3 entries → 16, with the startup self-check text updated in step |
| F-63 | New `DeployedKind` enum value and the two exhaustive switches | M / 1.5 | F-62 | `ZANGIEF_CANE(10)` + the static singleton and `case` in `DeployedBehaviors` + the `case` and block list in `FarmstormBlockEntities` |
| F-64 | Two new `DeferredRegister`s: `mob_effects` and `sound_events` | L / 3.0 | F-60 | `WildHunterEffect` (visible, with an icon, three attribute modifiers attached and removed) / `ZangiefPiledriverEffect` (hidden, `amplifier` used as the phase number) + both register classes hung on the mod bus |
| F-65 | 5 items, 2 blocks, and the fuel tag key | M / 1.5 | F-62 | `items` gains 5 (including the `compressed_hay_block` BlockItem) / `blocks` gains 2 (including `deployed(ZANGIEF_CANE, …)`) / `TagKey<Item> CONVERTER_FUEL` |
| F-66 | 2 throwable class skeletons, 2 entity_types, renderers at 11/11 | M / 1.5 | F-64, F-65 | `MolotovProjectile` / `CompressedPuffedWheatProjectile` skeletons (`onImpact` keeps only the "settle once per spawn" hook) + 2 `projectile(...)` + 2 `ThrownItemRenderer` + the two self-check lines "77 objects" and "11-11" |

### 2.4 Phase exit criteria

- [ ] **F-60 build**: run `.tools\gw.cmd build`. How to check: read the exit code and the `build/libs` directory. Passing looks like: exit code 0, `build/libs/farmstorm-0.0.2+mc1.21.1.jar` exists (the version in the file name comes from `gradle.properties`), and `git diff` of `build.gradle` is empty.
- [ ] **F-60 five new directories**: `Get-ChildItem -Directory src\main\resources\assets\farmstorm`. Passing looks like: all five of `sounds`, `textures\misc`, `textures\mob_effect`, `shaders\core`, `post_effect` are present (the five-directory table in `12-TECH` §1.2).
- [ ] **F-61 key total**: start a server once, then count the leaf nodes of `run\config\farmstorm-server.toml`. Passing looks like: **139** of them; the four new groups appear at the end of the file in alphabetical order (`molotov` → `wild_apple` → `wild_hunter` → `zangief_cane`); spot-check three defaults — `molotov.fireChance = 85.0`, `wild_hunter.durationTicks = 700`, `zangief_cane.spinDegreesPerTick = 72.0` (A-01 / A-05).
- [ ] **F-61 hot reload**: change `molotov.fireChance` to `50.0`, then `/reload`. Passing looks like: one `bake()` line appears in the log and the value reads back as 50.0; after changing it back to 85.0 it takes effect the same way.
- [ ] **F-62 two self-check lines**: read the first 100 lines of `logs/latest.log`. Passing looks like: these appear verbatim — `自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK` (self-check: 19 crops — Throwable 10 / Deployable 6 / Edible 3; Demolition 4 → OK) and `自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK` (self-check: 16 Lucky Berry pool entries, excluding those three). (BRIEF §4 verbatim + `12-TECH` §11 #1 / #3.)
- [ ] **F-62 frozen inventory**: `git diff util/CropProfile.java`. Passing looks like: added lines only, no `-` lines; not one character of the existing 15 entries' fields changed, and the order matches rows 1–15 of `10-DECISIONS-0.0.2.md` §4.
- [ ] **F-63 enum and switches**: read the kind → singleton mapping lines in the startup log and run one compile. Passing looks like: one line per enum value, six in total, with `ZANGIEF_CANE`'s scan interval written as 10 ticks; the compile log has no `the switch expression does not cover all possible input values`; `block_entities` is still 6 in the registry self-check line (the compile-time enforcement of `12-TECH` §3.14).
- [ ] **F-64 the two effects**: `/effect give @s farmstorm:wild_hunter 10` and `/effect give @s farmstorm:zangief_piledriver 10 2 true` (**the fifth argument `true` is required**: the 1.21.1 five-argument `/effect` makes `showIcon = visible`, so omitting it yields `show_icon:1b`, V-08). Passing looks like: the first shows an icon slot, the second has no HUD icon and no inventory description; in `/data get entity @s active_effects` (**lower-case**, the 1.21.1 NBT key; `ActiveEffects` reports `Found no elements matching`, V-09) `zangief_piledriver` has `amplifier` 2 and `show_icon` false (the phase channel of D-50 holds).
- [ ] **F-64 the two sound events**: `/playsound farmstorm:wild_hunter_loop master @s ~ ~ ~ 1 1` and `/playsound farmstorm:zangief_piledriver master @s ~ ~ ~ 1 1`. Passing looks like: both commands succeed (the audio files arrive in `F-94`, so silence passes here) and both events appear in the `/playsound` completion list (`12-TECH` §11 #6).
- [ ] **F-65 five items, two blocks**: five `/give` plus two `/setblock`. Passing looks like: all 7 commands return success and the 5 new items are visible in the creative tab `farmstorm:main`; after `/setblock ~ ~1 ~ farmstorm:zangief_cane` the world shows no visible geometry (empty model, D-68); `/data get block <coords>` reads back the `owner` field (reusing `DeployedCropBlockEntity`).
- [ ] **F-64 / F-66 the other two self-check lines**: the log of that same startup. Passing looks like: these appear verbatim — `自检 注册对象 77 个：items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1 → OK` (self-check: 77 registered objects) and `自检 实体渲染器 11/11 → OK` (self-check: entity renderers 11/11) — and the very next line lists the 11 entity IDs (including `farmstorm:molotov` and `farmstorm:compressed_puffed_wheat`).
- [ ] **F-66 the two new entities**: `/summon farmstorm:molotov ~ ~2 ~` and `/summon farmstorm:compressed_puffed_wheat ~ ~2 ~`. Passing looks like: both succeed without a crash; when the entity lands, `/execute if block` shows every surrounding block unchanged (the skeleton `onImpact` has no behavior wired yet).
- [ ] **Dedicated server**: `runServer` (not `runClient` only). Passing looks like: the startup log has no `NoClassDefFoundError` (both effect classes and both register classes sit on the common side and do not depend on `Dist.CLIENT`).
- [ ] **Repository state**: `git status` and `git diff --stat`. Passing looks like: only whitelisted files and new files appear; `docs/`, `.work/`, and the 15 existing profiles are unchanged.

### 2.5 Common failure modes

1. **`ZANGIEF_CANE` added without filling in the two exhaustive switches, or plastered over with `throw new UnsupportedOperationException()`**: both `DeployedBehaviors.of()` and `FarmstormBlockEntities.create()` are exhaustive `switch`es, so adding a value without changing them does not compile; and when you plaster it over with `throw`, compilation is green but placing one Zangief Cane with `/setblock` crashes the server when the chunk loads. Catch it early: run `.tools\gw.cmd build` the moment the enum value is added — `the switch expression does not cover all possible input values` in the log is exactly this; if `block_entities` goes from 6 to 7 in the registry self-check, a new BE was registered in passing (that is a wrong change, not a fix).
2. **Re-counting the 22 keys yourself, inventing the key names yourself**: the old drafts of `11-DESIGN` contain key names already rejected by A-01 / A-02 / A-03, such as `magmaChance`, `wild_apple.hunterDurationTicks`, `zangief_cane.dropVelocity`; transcribe one key name wrong and one `farmstorm.configuration.*` language key goes missing, breaking both 139 and the equality "language keys = config keys". Catch it early: copy BRIEF §8's 22 lines into a check table, compare them line by line against `farmstorm-server.toml`, then count the language keys carrying the `farmstorm.configuration.` prefix — both numbers must be 139.
3. **The self-check text gets "polished" into a synonym**: the four self-check lines are accepted verbatim (`12-TECH` §11 #1–#4), so changing `→ OK` to `OK` or writing "投掷型 10" as "投掷 10 种" fails — and F-62 / F-66's exit criteria are a verbatim comparison of exactly these lines. Catch it early: paste BRIEF §4's source lines into the task prompt and compare character by character with `Select-String` when the report comes back; report even character-level differences.
4. **"Tidying up" the 15 existing profiles**: reordering the array while taking `CropProfile.ALL` from 15 to 19, or rewriting `LUCKY_BERRY_POOL`'s derivation wholesale, touches the fields of the existing 15 (BRIEF §3.4's red line). Catch it early: `git diff` may show added lines only — any `-` line is a failure; the order of `ALL`'s existing 15 entries must map one-to-one onto rows 1–15 of `10-DECISIONS` §4.
5. **The two new `DeferredRegister`s leak into the client side**: once `WildHunterEffect` / `ZangiefPiledriverEffect` import `net.minecraft.client.*`, a dedicated server hits `NoClassDefFoundError` at startup (`12-TECH` §11 #22). Catch it early: this phase must run `runServer` once; `grep -rn "net.minecraft.client" effect/` must hit 0.
6. **`compressed_hay_block`'s properties written into F-65 ahead of time**: F-65 only registers the entry; `strength(30.0F, 6.0F).requiresCorrectToolForDrops()` is F-67's deliverable, and writing a `Properties` in both places creates "one value with two sources". Catch it early: this phase allows exactly one construction of that entry, and no `30.0F` literal may appear.

### 2.6 What to report

- The path of `build/libs/farmstorm-0.0.2+mc1.21.1.jar` and the conclusion line of `gw.cmd build`; `git diff` of `build.gradle` is empty.
- The command and result for counting the leaf nodes of `farmstorm-server.toml` (139) + the four new group names + the verbatim text of the three sampled defaults.
- The verbatim text of the four self-check lines (paste the log, do not paraphrase).
- The return lines of the 5 `/give` and 2 `/setblock` commands, plus the `owner` read-back of one `/data get block`.
- The `amplifier` and `show_icon` readings of the two effects in `/data get entity @s active_effects`.
- `DeployedKind`'s six kind → singleton mapping lines.
- Open questions (especially the `AttributeModifier` constructor signature under 21.1.213, the timing of hanging the two `DeferredRegister`s on the mod bus, and where `bake()` is called).

---

## 3. Phase 12 · Compressed Items and Molotov! (F-67 ~ F-75)

> Feed method: the full text of `14-PROMPT-MAIN-0.0.2.md` + this section; feed one phase at a time.

### 3.1 Prerequisites

- Every exit criterion of Phase 11 passes, especially `F-65` (5 items / 2 blocks) and `F-66` (the two throwable skeletons + 2 entity_types) — 6 of this phase's 9 tasks change things those two built.
- `12-TECH-0.0.2.md` §3.7 (`MolotovProjectile`), §3.8 (`CompressedPuffedWheatProjectile`), §3.9 (`compressed_hay_block` adds no block class), and §11 #7 / #8 / #9 / #20 have been read.
- The per-block blast-resistance table in `10-DECISIONS-0.0.2.md` §5.3 has been copied into the task prompt: that table agrees with D-57 in BRIEF §2, and any "TNT cannot blow it up" wording is judged wrong per A-06 in §0.4.
- Known v0.0.1 facts: `fillHay` in `entity/thrown/PuffedWheatProjectile.java` hard-codes `Blocks.HAY_BLOCK`, and that file is inside BRIEF §3.3's whitelist, so a small change is allowed (extract `protected BlockState fillBlock()` and let subclasses override it); the change follows the adjudication in `13-TASKS` §12.3 item 4.
- This phase adds no mod dependency, adds no network packet, and changes neither the 21 existing conversion recipes nor the existing crop explosion behavior (BRIEF §3.4).

### 3.2 Goal

Land the three pieces of content (D-57 Compressed Hay Block, D-77 Compressed Puffed Wheat, D-58 Molotov) and the 5 new recipes, and fill in Compressed Hay Block's two block tags and two loot tables. At the end of the phase a player can walk the whole line: crafting table makes a converter → the converter trades out the three new items → 4 Puffed Wheat make 1 Compressed Puffed Wheat → throw it and burn a patch (not one original block missing) → lay a patch of Compressed Hay Block, which the Melon Grenade cannot blow up but TNT can.

### 3.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-line deliverable |
|---|---|---|---|---|
| F-67 | Compressed Hay Block: block properties and the resistance matrix | M / 1.5 | F-65 | A plain `Block` + `strength(30.0F, 6.0F).requiresCorrectToolForDrops()`, the same texture on all six faces, no axis variant, no new block class |
| F-68 | Compressed Puffed Wheat projectile: reuse the landing write | M / 1.5 | F-66, F-67 | The subclass swaps only the written block: `fillBlock()` extracted into the parent, overridden by the subclass; the 3 dry / 5 wet edges and the 3.6e6 resistance floor are inherited as they are |
| F-69 | Molotov landing trigger | M / 1.5 | F-66 | The landing fires one zero-power explosion (`NEVER_BREAK`, `damageCap = 0`) purely as a trigger, and prints one landing log line |
| F-70 | Candidate enumeration in the sphere and per-cell fire / magma placement | M / 1.5 | F-69, F-61 | For every block in the radius-4 sphere whose "top face touches air", roll per cell: 85 % place fire in the air position above it, the rest place a magma block, and none replace the original block |
| F-71 | Converter crafting recipe and the `converter_fuel` tag | M / 1.5 | F-65 | Shaped 3×3 (crafting table in the center + eight cells of `#farmstorm:converter_fuel`) producing 1 converter + the new item tag |
| F-72 | Compressed Puffed Wheat shapeless recipe | S / 0.5 | F-65 | Shapeless: 4 × `farmstorm:puffed_wheat` → 1 × `farmstorm:compressed_puffed_wheat` |
| F-73 | Three new conversion recipes | S / 0.5 | F-65, F-71 | Crimson Fungus ⇄ Molotov / Sugar Cane ⇄ Zangief Cane / Golden Apple ⇄ Wild Apple, bidirectional, 3 XP, no catalyst |
| F-74 | Loot tables for Compressed Hay Block and Zangief Cane, plus two block tags | M / 1.5 | F-67 | Two loot tables + an addition to `mineable/pickaxe` + a new `needs_diamond_tool` + an addition to `deployed_crops` |
| F-75 | Phase 12 exit regression | S / 0.5 | F-68, F-70, F-71, F-72, F-73, F-74 | The five-step loop runs through once + the three self-checks of `§11 #7 / #8 / #20` + the jar is larger than v0.0.1's |

### 3.4 Phase exit criteria

- [ ] **F-67 hardness and drops**: `/setblock ~ ~ ~ farmstorm:compressed_hay_block`, then read the targeted-block panel of F3 and left-click bare-handed for 30 seconds. Passing looks like: hardness **30**; bare-handed or with a wooden pickaxe, after 600 ticks the **block is still there, progress ~0.400, 0 drops** (that is 'not broken through yet', not 'broken but no drop'). 'A diamond pickaxe breaks it in one go and yields the block itself' is **handed over to F-74** (the `mineable/pickaxe` and `needs_diamond_tool` tags plus the loot table are all F-74 deliverables, V-14).
- [ ] **F-67 four blast-resistance cases**: blow it up once each with the Melon Grenade, the Melon Slice Bomb, and the Ubi Server, then ignite one `/summon tnt`, reading back `/execute if block ~ ~ ~ farmstorm:compressed_hay_block` after each. Passing looks like: the four return values are **1 / 1 / 0 / 0** in that order (A-06: the two melon items' 6.0 threshold cannot break it, the 3.6e6 threshold and vanilla explosions can).
- [ ] **F-68 cell-for-cell consistency**: at the same coordinates throw a Puffed Wheat first and a Compressed Puffed Wheat second, recording each once. Passing looks like: both writes produce the same cube edge length (27 cells dry / 125 cells near water), the position sets are identical, and the only difference is the block ID (`minecraft:hay_block` vs `farmstorm:compressed_hay_block`); every original block is replaced and the drops are 0 (the item-entity count before and after `/kill @e[type=item]` is the same).
- [ ] **F-68 resistance floor**: throw one Compressed Puffed Wheat next to `minecraft:bedrock` (resistance 3.6e6). Passing looks like: cell-by-cell `/execute if block` reads true for every bedrock position (skipped, not written).
- [ ] **F-68 no new keys**: inspect `config/farmstorm-server.toml`. Passing looks like: there is no new `compressed_puffed_wheat.*` or `puffed_wheat.*` key, and it still reads the existing `puffed_wheat.dryEdge` / `wetEdge`.
- [ ] **F-69 no damage, no destruction**: on flat ground throw one Molotov, then read back health and the 3×3×3 `/execute if block`. Passing looks like: `/data get entity @p Health` is the same before and after; the 3×3×3 at the landing point matches cell by cell, not one block destroyed (`NEVER_BREAK` + `damageCap = 0`).
- [ ] **F-69 key-driven**: read the log line. Passing looks like: `莫洛托夫 落点 (x, y, z) 威力 0` appears, matching the config default of 0; after changing the key to 4 and throwing again, entities inside the blast show vanilla knockback (`Motion` goes from 0 to non-zero), **block destruction stays 0** and health is unchanged; changing it back to 0 removes the knockback (A-10: raising the power only adds entity damage and never breaks blocks, V-18).
- [ ] **F-70 append, not destroy**: throw one on flat ground and compare cell by cell before and after. Passing looks like: inside the radius-4 sphere, only cells that are "solid with air above" gain fire or a magma block above them, and every original block still matches the before state; no block is placed above blocks that fail `isSolidRender`, such as slabs or snow layers.
- [ ] **F-70 ratio and cost**: throw 20 in a row and read the three log values. Passing looks like: `magma blocks / (fire + magma blocks)` falls in the **10 %–20 %** band (a coarse check of 85 : 15); the three log values can be summed directly against each other; the server has no `Can't keep up` line.
- [ ] **F-70 radius read from config**: change `molotov.effectRadius` to `1.0` and throw again. Passing looks like: the `候选 {N}` ("candidates {N}") number in the log gets clearly smaller.
- [ ] **F-71 recipe shape**: inspect the two outputs. Passing looks like: `data/farmstorm/recipe/converter.json` has `type` `minecraft:crafting_shaped`, eight `key` positions pointing at the same tag, `result` `farmstorm:converter` with `count` 1; and `data/farmstorm/tags/item/converter_fuel.json`'s `values` cover the 4 vanilla crops (`minecraft:wheat` / `carrot` / `potato` / `beetroot`) plus every item among the 19 profiles that can serve as a crop.
- [ ] **F-71 craft it once**: place the recipe once at a crafting table, then replace any one cell with `minecraft:diamond`. Passing looks like: the first yields 1 `farmstorm:converter` and the second yields nothing; `git diff` of the 21 existing conversion recipe JSONs is empty.
- [ ] **F-72 shapeless crafting**: inspect the JSON and three placements. Passing looks like: `type` is `minecraft:crafting_shapeless`, `ingredients` are 4 groups of the same item, `result.count` is 1; 4 in gives 1 out, while 3 and 5 give nothing.
- [ ] **F-73 the three conversions**: inspect the three JSONs and two conversions. Passing looks like: all three JSONs have `type` `farmstorm:converting`, and `bidirectional` / `experience` / the catalyst are **omitted because they equal the defaults** (the codec uses `optionalFieldOf`, so none of the three keys is written; the existing 21 recipes are the same, V-25); golden apple ⇄ Wild Apple succeeds in both directions, `/xp query @s points` drops by 3 twice and `/xp query @s levels` **stays unchanged** (points, never levels, D-12 / V-24); there are **24** `farmstorm:converting` recipes under `src/generated/resources/data/farmstorm/recipe/`. Put 1 nether star in the catalyst slot as a control: all three recipes **leave it untouched** (only the torchflower / pitcher plant forward conversion consumes one, V-29).
- [ ] **F-74 two tables, two tags**: inspect `src/generated/resources` and mine twice. Passing looks like: a diamond pickaxe yields the block itself; after 600 ticks with a wooden pickaxe progress is 0.400, the block is still there and drops 0 ('not broken through' ≠ 'broken but no drop', V-28); `data/minecraft/tags/block/needs_diamond_tool.json` has `values` `["farmstorm:compressed_hay_block"]`; `mineable/pickaxe.json` is an append (every existing entry still there), not an overwrite; `data/farmstorm/tags/block/deployed_crops.json` contains `farmstorm:zangief_cane` and `data/farmstorm/loot_table/blocks/zangief_cane.json` exists and drops itself.
- [ ] **F-75 five-step loop**: run the whole line once in a single-player save — craft a converter → trade out the three items → 4 into 1 → throw a Molotov → throw a Compressed Puffed Wheat. Passing looks like: each step leaves one `/give` or `/execute if block` reading; all three of `12-TECH` §11 #7 / #8 / #20 pass.
- [ ] **F-75 artifact**: compare the size of the two jars. Passing looks like: `build/libs/farmstorm-0.0.2+mc1.21.1.jar` exists and is larger than `dist/farmstorm-0.0.1+mc1.21.1.jar`.
- [ ] **Whole-phase red line**: `grep -r "CustomPacketPayload" src/main/java`. Passing looks like: 0 matches.

### 3.5 Common failure modes

1. **Reading A-07 as "reuse the explosion"**: the old wording in `12-TECH` §3.8 has sentences like "power / damage cap follow Puffed Wheat", and copying them attaches an `Explosion` to Compressed Puffed Wheat — while Puffed Wheat has no explosion stage at all, so the result is "the blocks are placed and a ring is blown up on top". Catch it early: `grep -n "Explosion" entity/thrown/CompressedPuffedWheatProjectile.java` must hit 0; the cell-by-cell `/execute if block` results of the two throws differ only in the block ID.
2. **Copying "TNT cannot blow up Compressed Hay Block"**: that is the wording A-06 rejected, and copying it ends with the resistance rewritten as something like `strength(30.0F, 1200.0F)`, so neither the Melon Grenade nor TNT can blow it up and F-67's items ③④ both fail. Catch it early: right after `/setblock`, ignite a `/summon tnt` once and verify; the resistance value is only ever `6.0F`.
3. **The Molotov destroys original blocks in passing**: D-58 wants fire / magma blocks **appended** at "the air position above", with the condition "the top face touches air"; writing it as "replace the block with fire" or "destroy inside the sphere first, then place fire" turns F-70's "not one original block missing" red immediately. Catch it early: save one 3×3×3 `/execute if block` result before and after the throw and diff them; there is no second probability key besides `fireChance` (A-01).
4. **Writing the `converter_fuel` tag as a fixed item list**: D-52 wants a tag, with all eight cells pointing at `#farmstorm:converter_fuel`; writing eight `item` fields, or filling the tag with only the 4 vanilla items, breaks "the crops among the 19 profiles also work as fuel" and the JSON shape also fails F-71's item ①. Catch it early: the `tag` field of all eight `key` entries in `converter.json` is character-identical; the number of `values` lines in `converter_fuel.json` matches the check table.
5. **The three recipes corrupting the existing 21 in one `ModRecipeProvider` edit**: `F-71` / `F-72` / `F-73` share that file, so reordering the `accept` calls or changing a shared constant changes the JSON of the 21 existing conversion recipes. Catch it early: after every `runData`, run `git status src/generated/resources/data/farmstorm/recipe/` — only 5 new files may appear and none of the existing 21 may show `M`.
6. **Making `compressed_hay_block` a rotatable pillar**: D-57 explicitly says the same texture on all six faces and no `pillar` / `axis`. Adding an axis makes `blockstates/compressed_hay_block.json` carry several variants, and F-96's item ① (exactly 1 `cube_all` variant) then needs rework. Catch it early: when writing the `strength(...)` line, confirm there is no `.rotate(...)` call.

### 3.6 What to report

- The reading for each step of the five-step loop (craft a converter / trade the three items / 4 into 1 / throw a Molotov / throw a Compressed Puffed Wheat).
- The `/execute if block` return values of the four blast-resistance cases (should be 1 / 1 / 0 / 0) and the verdict of the two `/summon tnt` runs.
- The Molotov's three kinds of log: `落点 … 威力 0` ("landing … power 0"), `候选 {N} 火 {F} 岩浆 {M}` ("candidates {N} fire {F} magma {M}"), and the coarse band from throwing 20 in a row.
- The verbatim `/loot give` output of the two loot tables; the `values` lines of the two block tags.
- The path list of the 5 new recipe JSONs, plus the conclusion "`git diff` of the existing 21 is empty".
- The size figures of the two jars.
- Open questions (especially how `ThrownItemRenderer` picks a texture for a custom throwable, the key by which `ConverterRecipe` looks up a bidirectional recipe, and the behavior of `FarmstormExplosion` under `damageCap = 0`).

---

## 4. Phase 13 · Wild Hunter (F-76 ~ F-85)

> Feed method: the full text of `14-PROMPT-MAIN-0.0.2.md` + this section; feed one phase at a time.

### 4.1 Prerequisites

- All of Phase 11 passes, above all `F-64` (the two effects + the two sound events) and `F-61` (the 11 `wild_hunter.*` keys); `F-65`'s `wild_apple` item also landed in Phase 11.
- BRIEF §7.1 / §7.2 / §7.4 / §7.6 / §7.7, `12-TECH-0.0.2.md` §4.1 / §4.2 / §4.5 / §4.6, and the four risks `RT-10` / `RT-11` / `RT-12` / `RT-14` have been read.
- Client classes may appear only in the `client/` package (the 6 classes of `12-TECH` §3.13); no server-side path may import `net.minecraft.client.*` (`12-TECH` §11 #22).
- The two red lines are confirmed: no `CustomPacketPayload` (D-50), no Mixin (D-40). The dash must follow `12-TECH` §4.5's "client-predicted displacement + server adjudication" — `ServerPlayer#setDeltaMovement` does not move the client player (RT-12).
- The only texture this phase produces itself is `assets/farmstorm/textures/misc/wild_hunter_red.png` (1×1, F-83's deliverable); the other 5 item / block textures and the 2 OGG files arrive in Phase 15.

### 4.2 Goal

Land the two invasive effects (world-only grayscale D-61, red silhouette D-62) and the whole Wild Hunter gameplay: three attribute bonuses, 5-block 30° target acquisition, a 3-tick attraction dash, damage and knockback settlement, kill time extension with no cap, and a following loop sound. The phase exit is that all seven abilities are observable within 700 ticks of eating one Wild Apple, and that `/effect clear @s` resets all seven.

### 4.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-line deliverable |
|---|---|---|---|---|
| F-76 | Wild Hunter's three attribute bonuses | M / 1.5 | F-61, F-64 | Speed +30 % / attack speed +30 % / damage +40 %, three fixed `ResourceLocation`s + `ADD_MULTIPLIED_TOTAL`, removed cleanly when the effect is cleared |
| F-77 | Eating the Wild Apple and granting the effect | M / 1.5 | F-65, F-76 | `FoodProperties` (nutrition 4 / saturation 1.2) + applying `farmstorm:wild_hunter` after eating (duration read from `wild_hunter.durationTicks` 700, A-02) |
| F-78 | Target-acquisition algorithm: a 5-block sphere and a 30° angle | M / 1.5 | F-76 | `resolveTarget()`: within a radius-5 sphere, angle ≤ 30°, excluding self / teammates / spectators; the smallest angle wins, ties take the nearer one + one target log line |
| F-79 | The dash's client-predicted displacement | L / 3.0 | F-78 | Give velocity locally when the attack key is pressed, the player holds a valid effect, and the client resolves the same target; cover the distance in 3 ticks; if the landing spot is occupied by a block, skip the attraction this time |
| F-80 | Server review and damage / knockback settlement | M / 1.5 | F-78 | On an incoming `ServerboundSwingPacket`, resolve the target independently with the same algorithm, settle damage, and after "damage was actually dealt" call vanilla `LivingEntity#knockback` at default strength |
| F-81 | Kill time extension with no count cap | S / 0.5 | F-80 | Each kill adds +160 ticks of remaining duration and restores 10 health, with no counter written anywhere (D-74) |
| F-82 | World-only grayscale (the HUD keeps its colours) | L / 3.0 | F-60, F-76 | The vanilla `PostChain`: `GameRenderer#loadEffect` loads `shaders/post/grayscale.json` (`targets` must be an array) + `shaders/program/grayscale.json` + `shaders/core/grayscale.fsh`; `post_effect/grayscale.json` is kept as a reserved mount point (no loader today) |
| F-83 | Red silhouette: visible and no farther than 64 blocks | M / 1.5 | F-76 | `RenderLivingEvent.Post` redraws through `LivingEntityRenderer#render(...)` with `MultiBufferSource` **filtered by vertex format** (V-58) + `hasLineOfSight` + distance ≤ 64; the colour is baked into `textures/misc/wild_hunter_red.png` (V-60); `AFTER_ENTITIES` captures the camera and `RenderGuiEvent.Pre` redraws after grayscale |
| F-84 | Loop sound `SoundInstance` | M / 1.5 | F-76 | A client-side loop sound instance, `isLooping() = true`, audible distance 16 blocks, `SoundSource.PLAYERS`; your own instance uses `relative=true` with zeroed coordinates and `Attenuation.NONE`, everyone else's uses `relative=false` with world coordinates and `LINEAR`, volume fixed at 1.0 (V-53) |
| F-85 | Phase 13 exit regression | S / 0.5 | F-77, F-79, F-80, F-81, F-82, F-83, F-84 | All seven abilities run through once + the three self-checks of `§11 #13 / #14 / #15` + 0 packets and 0 Mixin |

### 4.4 Phase exit criteria

- [ ] **F-76 three readings**: eat one Wild Apple, then run `/attribute @s minecraft:generic.movement_speed get`, `minecraft:generic.attack_speed get` and `minecraft:generic.attack_damage get` (**the F3 panel does not show attribute values**, V-61). Passing looks like: +30 % / +30 % / +40 % respectively, matching the config defaults item by item; after `/effect clear @s farmstorm:wild_hunter` all three are back to baseline.
- [ ] **F-76 key-driven**: change `wild_hunter.attackDamageBonusPercent` to `100.0` and re-enter the save. Passing looks like: the reading of `/attribute @s minecraft:generic.attack_damage get` doubles; after changing it back to 40.0 it is restored.
- [ ] **F-77 duration and values**: after eating one, read `/effect` and `/data get entity @s active_effects`. Passing looks like: remaining duration **700 ticks ± 2** with `amplifier` 0; the hunger row differs by 4 points before and after, and `Saturation` differs by **the same value as the vanilla golden apple (9.6 measured)** — `FoodData#eat` is `nutrition × saturationModifier × 2.0F`, so 4.8 is not written into the criterion (V-35).
- [ ] **F-77 bidirectional recipe**: convert a Wild Apple back to a Golden Apple with the converter, then eat the Golden Apple. Passing looks like: it yields 1 `minecraft:golden_apple`; eating a Golden Apple does not grant `farmstorm:wild_hunter`.
- [ ] **F-78 target log and angle**: place a pig within 5 blocks straight ahead and attack once. Passing looks like: the log shows `狂野猎人 目标 minecraft:pig 夹角 {A} 度 距离 {D}` ("Wild Hunter target … angle {A} degrees distance {D}") with `A <= 30`.
- [ ] **F-78 ordering and exclusions**: place one on each side at the same distance, then make one side nearer at the same angle; place one behind you, one teammate, and one spectator. Passing looks like: the smaller angle is chosen, and ties take the nearer one; an angle > 30°, a teammate, and a spectator are all never chosen; after changing `wild_hunter.searchRadius` to `1.0` a pig beyond 5 blocks is no longer chosen.
- [ ] **F-79 on target in three ticks**: with the effect on, attack a pig within 5 blocks and 30° once. Passing looks like: the F3 coordinate difference ≈ the initial distance and the move happens within 3 ticks; `/data get entity @p Pos` lines up with the target position.
- [ ] **F-79 walls and occupied cells**: put a stone wall between the player and the target and attack; then fill the target cell with `/setblock` and attack. Passing looks like: in the first case the dash stops on the near side of the wall, without passing through or snapping into place; in the second case there is no attraction this time (the player's coordinates do not change); `logs/latest.log` has no `moved too quickly!`.
- [ ] **F-80 damage and knockback**: attack a pig within 30° once, recording health, `Pos` and `/attribute @s minecraft:generic.attack_damage get` before the attack. Passing looks like: the decrease in `/data get entity <target> Health` equals that attribute reading (**F3 does not show attribute values**, V-61); `Pos` differs by ≥ 0.2 blocks (D-75 vanilla default strength).
- [ ] **F-80 two control groups**: attack a mob beyond 30°; attack a teammate. Passing looks like: the first is a normal attack with no attraction and the player's coordinates unchanged; the second neither attracts nor removes health.
- [ ] **F-81 six kills in a row**: read `/effect` after each kill. Passing looks like: each kill adds **+160 ticks** of remaining duration and 6 `狂野猎人 续时` ("Wild Hunter extended") lines appear in the log; the effect is still there after the sixth; health is +10 each time; `git grep -nE "killCount|maxKill"` produces no output.
- [ ] **F-82 world-only grayscale**: with the effect on, take one F3 screenshot that includes the inventory and the chat. Passing looks like: the **world goes black and white while the HUD keeps its original colours** (blocks / entities / particles greyed, the hearts, the green experience bar and the coloured chat text unchanged, V-56); the client log has no `Could not load program` / `Shader … could not be loaded` and no `GL_INVALID`, and shows the two lines for loading and unloading the grayscale PostChain; the F3 `ms` difference between effect on and off is < 3 ms; colour is back within 1 tick of `/effect clear`.
- [ ] **F-83 the silhouette's three conditions**: stand within 5 blocks looking straight at a pig, at 65 blocks away, and in front of / behind a wall once each; then bring a **leashed mob** within 64 blocks (the crash scenario of V-58). Passing looks like: only mobs that are ≤ 64 blocks + unobstructed + not allied are drawn as a red silhouette; yourself and teammates are not drawn; the client log has no `StackOverflowError` and the F3 `ms` difference is < 3 ms; after changing `highlightRadius` to `8.0`, mobs beyond 10 blocks are no longer drawn. **A human must look at the picture**: the silhouette matches the body outline, is **not upside down and does not float 1.5 blocks above the ground** (the two transforms of V-50), and keeps its brightness inside an unlit cave (`FULL_BRIGHT`) — counting red pixels only proves 'there is red', not 'it looks right'.
- [ ] **F-84 loop sound**: measure the distance with two players, **then let A strafe left and right**, and play `/playsound minecraft:music.game` at the same time. Passing looks like: audible at 8 blocks and inaudible at 20 (16-block attenuation); **no stutter and no left/right image jumping while A strafes** (V-53: `relative=true` with zeroed coordinates); vanilla background music is not cut (the category is `PLAYERS`, not `MUSIC`); it stops within 1 tick of `/effect clear`; nothing is left after a death and respawn; `wild_hunter_loop` appears no more than 2 times in `latest.log` over the whole effect.
- [ ] **F-85 exit regression**: run the seven items plus the two structural assertions. Passing looks like: all three of `12-TECH` §11 #13 / #14 / #15 pass; all seven are reset after `/effect clear @s`; `grep -r "CustomPacketPayload" src/main/java` and `grep -rn "Mixin" src/main/java` both return 0 matches.

### 4.5 Common failure modes

1. **Writing the dash as "server-side `setDeltaMovement`" (RT-12)**: the symptom is "the attack key does nothing, the log resolved the right target, damage was settled, but the player did not move", and the server-side movement check then pulls the player back. Player displacement is simulated on the client to begin with, so the driver must sit on the client (`12-TECH` §4.5's client prediction + server adjudication); the red line is not adding a packet for it. Catch it early: first print a `setDeltaMovement` log line to confirm whether the client moved, then check `latest.log` for `moved too quickly!`.
2. **Going back to a hand-written pass (RT-10 / V-48 / V-49 / V-56)**: binding the main target inside `RenderGuiEvent.Post` and then sampling it is GL read/write feedback (undefined results, and in practice 'grey at the wrong moment'); the `post_effect/` directory has no loader in NeoForge 21.1.213, so a file placed there is never read. The correct route is the vanilla `PostChain` (`GameRenderer#loadEffect`), at the cost of greying the **world only and leaving the HUD in its original colours** — per V-56 that is this version's settled scope, not a defect. Spot it early: look for `Could not load program` / GL errors under `[Render thread/ERROR]`; a screenshot where the world greys and the HUD stays coloured is the expected result.
3. **Replacing the whole `MultiBufferSource` for the silhouette (V-58 / RT-11)**: swapping every RenderType for the entity format unconditionally makes a leash (`POSITION_COLOR_LIGHTMAP`, no UV0/UV1/Normal) throw `IllegalStateException: Missing elements in vertex` in `BufferBuilder.endLastVertex`, and **the client dies outright** with no stack shown. Filter by `type.format() == DefaultVertexFormat.NEW_ENTITY` and pass everything else through. Redrawing via `EntityRenderDispatcher#render` is the other dead end (recursion → `StackOverflowError`); call `LivingEntityRenderer#render(...)` with a re-entrancy guard. Spot it early: when the client exits with no stack, check `run-<dir>/crash-reports/` first instead of assuming an external kill, then re-test with a leashed mob in view.
4. **The Wild Apple keeping its own duration key (A-02)**: `wild_apple.hunterDurationTicks` was rejected; the duration has exactly one source, `wild_hunter.durationTicks`. Two keys fight each other between "eating an apple gives 700 ticks" and "the effect gets refreshed to another duration by some other source", and the toml leaf count becomes 140 instead of 139. Catch it early: `grep -rn "hunterDurationTicks" src/` hits 0; the `wild_apple` group holds only the `nutrition` and `saturationModifier` keys.
5. **The loop sound re-issuing a play command every tick (D-76)**: a server-side `playSound` inside `PlayerTickEvent` floods vanilla packets and the sound stutters as the player moves. It must be a client-side `SoundInstance` subclass with `isLooping() = true` updating position every tick; the category must be `SoundSource.PLAYERS` (using `MUSIC` cuts off vanilla background music and F-84's item ② goes red). Catch it early: `wild_hunter_loop` appears no more than 2 lines in `latest.log` over the whole effect period.
6. **Target acquisition reading the client's mouse position (D-64)**: D-64 already equates "closest to the crosshair" with "smallest angle to the view vector", and both ends running the same deterministic algorithm is enough; reading the mouse position amounts to requiring a new sync packet and walks straight into D-50. Catch it early: `grep -rn "MouseHandler" combat/` hits 0, and the client and server entry points call the same static method.

### 4.6 What to report

- The F3 readings of the three attributes and the before / after comparison around `/effect clear`.
- One verbatim `狂野猎人 目标 … 夹角 … 距离 …` log line, plus the conclusions of the three control groups (left / right, same angle, behind).
- The four dash results: on target in 3 ticks, stops at a wall, no attraction into an occupied target cell, no `moved too quickly!`.
- The remaining-duration sequence over six kills in a row and the empty output of `git grep killCount`.
- The three pieces of evidence for grayscale / silhouette / loop sound (the `ms` difference, no `StackOverflowError`, the `wild_hunter_loop` line count, the two-player listening distance).
- Open questions (especially `RenderGuiEvent.Post`'s render-target behavior under 21.1.213, the audible-distance hook of `SoundInstance#attributes`, and `entityTranslucentEmissive`'s handling of alpha).

---

## 5. Phase 14 · Zangief Cane (F-86 ~ F-93)

> Feed method: the full text of `14-PROMPT-MAIN-0.0.2.md` + this section; feed one phase at a time.

### 5.1 Prerequisites

- All of Phase 11 passes, especially `F-63` (`ZANGIEF_CANE(10)` and the two exhaustive switches), `F-64` (the hidden effect `zangief_piledriver`), `F-61` (the 6 `zangief_cane.*` keys), and `F-65` (the block and the item) — 7 of this phase's 8 tasks depend on them.
- BRIEF §7.3 / §7.4 / §7.5, `12-TECH-0.0.2.md` §5.1–§5.7 (the matrix of the three phases and the five client subsystems), and the two risks `RT-13` / `RT-15` have been read.
- `/setblock ~ ~ ~ farmstorm:zangief_cane` already works after Phase 11; being invisible in the world is the intended wording (empty model, D-68), and the model file `models/block/zangief_cane.json` only arrives in `F-96` — this phase's visibility acceptance requires only "no `elements`", not the model file's existence, and not the break-particle texture (`F-95`).
- The sync plan is confirmed: the three phases ride the hidden effect's `amplifier` (0 / 1 / 2), the downward velocity goes through `setDeltaMovement` (`ServerPlayer` broadcasts it via `ClientboundSetEntityMotionPacket`), and no packet is added anywhere (D-50).

### 5.2 Goal

Land the Deployable trap Zangief Cane and its three-phase presentation (inversion → levitation VII for 20 ticks → tick-by-tick served fall + 72°/tick self-rotation), and at the same time accept the third invasive effect (the input lock, leaving only ESC) and the camera (D-59 / D-70). The phase exit is: "step within 1.5 blocks and the world turns upside down at once, no key but ESC responds, the player lands in about 2 seconds, within 100 ms of landing the screen and the input are fully reset, and the block vanishes silently without dropping".

### 5.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-line deliverable |
|---|---|---|---|---|
| F-86 | Deployment block and trap behavior wiring | M / 1.5 | F-63, F-65 | `deployed(ZANGIEF_CANE, …)` follows the trap class's invisible wording, and `ZangiefCaneTrap` (a `TrapBehavior` implementation) is registered as a static singleton in `DeployedBehaviors` |
| F-87 | Spherical trigger test and exemptions | M / 1.5 | F-61, F-86 | Trigger when the **spherical Euclidean distance** from the block center to the player is ≤ 1.5, skipping spectators / creative / teammates and accepting players only; on trigger call `consume()` |
| F-88 | Phase 0: already inverted, waiting to float | M / 1.5 | F-64, F-87 | The server-side three-phase state machine; phase 0 = `amplifier 0` with the duration read from `stageDelayTicks` 14 ticks, re-issuing the same id at `amplifier 1` when it expires |
| F-89 | Phase 1: levitation VII for 20 ticks | S / 0.5 | F-88 | `MobEffects.LEVITATION` with amplifier = level − 1 = 6 for 20 ticks, applied on tick 14 |
| F-90 | Phase 2: tick-by-tick served fall and landing detection | M / 1.5 | F-89 | Every tick reset the downward velocity to remaining height ÷ remaining ticks; clear the effect on landing; the target duration is `fallTicks` 40 ticks (A-03) |
| F-91 | Camera: roll 180 + pitch negated + yaw self-rotation | M / 1.5 | F-61, F-88 | `ViewportEvent.ComputeCameraAngles`: `setRoll(180f)` + `setPitch(-pitch)` taking effect together; in phase 2 accumulate `yaw += 72.0` every tick |
| F-92 | Input lock: leave only ESC | L / 3.0 | F-88 | Five events each cover one area (movement / attack and use / 1–9 / scroll wheel / inventory and chat), letting `GLFW_KEY_ESCAPE` through, accepted against a 21-item key-by-key hand-test table |
| F-93 | Phase 14 exit regression | S / 0.5 | F-90, F-91, F-92 | Three-phase timing + landing time from three heights + the 21-item key table + 0 packets |

### 5.4 Phase exit criteria

- [ ] **F-86 block and singleton**: `/setblock ~ ~ ~ farmstorm:zangief_cane`, then read the kind → singleton mapping lines in the startup log. Passing looks like: the command returns success, the world shows no visible geometry but the break particles use `zangief_cane.png`; the `ZANGIEF_CANE` line in the mapping points at `ZangiefCaneTrap`.
- [ ] **F-86 scan interval**: read the timestamps of two `桑吉尔蔗 扫描` ("Zangief Cane scan") log lines. Passing looks like: they differ by **10 ticks**.
- [ ] **F-87 triggers and non-triggers**: stand once at a distance of 1.0 blocks from the block center, once at 2 blocks on the same axis, and once at "1.5 blocks diagonally". Passing looks like: only the first triggers; 2 blocks on the same axis does not; "1.5 blocks diagonally away" (exactly the position Chebyshev would wrongly hit) does **not** trigger either (D-73's spherical wording).
- [ ] **F-87 exemptions**: have a creative-mode player, a spectator, a teammate, and a mob each step on it once. Passing looks like: none of the four triggers (D-71).
- [ ] **F-87 silent disappearance**: after the trigger, look at the block and the item-entity count. Passing looks like: `/execute if block` returns 0 (the block is gone); the item-entity count before and after `/kill @e[type=item]` is the same (0 drops, D-72's trigger path).
- [ ] **F-88 phase 0**: right after the trigger, read `/data get entity @s active_effects`. Passing looks like: `farmstorm:zangief_piledriver` has `amplifier` **0** with **14 ticks ± 1** remaining; the log shows `桑吉尔蔗 相位 0` ("Zangief Cane phase 0").
- [ ] **F-88 phase switch**: read again after 14 ticks. Passing looks like: the same id's `amplifier` becomes **1** and no second effect record appears; after changing `stageDelayTicks` to `30` phase 0 lasts 30 ticks.
- [ ] **F-89 levitation**: during phase 1, read Y on F3 and the effect back. Passing looks like: Y rises every tick; `minecraft:levitation` has `amplifier` **6** with **20 ticks** remaining; during phase 0 (the first 14 ticks) Y does not change; after changing `levitationLevel` to 3 the `amplifier` becomes 2.
- [ ] **F-90 three heights**: step on it once each on flat ground / a two-block platform / a four-block platform. Passing looks like: the N in all three `落地耗时 {N} 刻` ("landing time {N} ticks") log lines falls within **40 ± 2** (the watershed of `12-TECH` §11 #10).
- [ ] **F-90 landing cleanup and time serving**: read the effect after landing; change `fallTicks` to `80` and re-test all three heights; `/kill @s` during phase 2 and respawn. Passing looks like: after landing `farmstorm:zangief_piledriver` can no longer be read; all three heights become about 80 ticks; after respawn there is no leftover velocity or effect.
- [ ] **F-91 inversion and self-rotation**: watch the screen from phase 0 on; during phase 2 read `/data get entity @s Rotation`. Passing looks like: the view is **upside down and rolled** (roll and pitch take effect together; doing only one is visible to the eye); in phase 2 yaw advances +72° per tick, 2880° = 8 full turns over 40 ticks; the `Rotation` reading does **not** change (only the render-layer angles move).
- [ ] **F-91 reset**: compare screenshots after the effect disappears. Passing looks like: within 1 tick all three camera axes return to the level line they had before the effect.
- [ ] **F-92 key-by-key hand test**: test key by key per the 21 items of `12-TECH` §10 `RT-13` and fill in the table. Passing looks like: with W/A/S/D/space/shift the F3 coordinates do not move; left and right click produce no attack and no use; 1–9 and the wheel cannot switch the hotbar; E does not open the inventory and T / `/` do not open chat; **ESC does open the pause menu**, and `/kill @s` during phase 2 shows the death screen normally (allowing only `PauseScreen` would freeze the player, V-30); every key is restored within 1 tick after the effect ends. **Record the look item as 'turn speed drops to about 1/15.6', not 'the view is locked'** (`setMouseSensitivity(0)` still leaves a factor of 0.2, V-31).
- [ ] **F-93 exit regression**: run the three of `12-TECH` §11 #10 / #11 / #12 + the phase log + the structural assertions. Passing looks like: all three pass; the four phase log lines (phase 0 / phase 1 / phase 2 / end) are present and `落地耗时 {N} 刻` appears once; `grep -r "CustomPacketPayload" src/main/java` returns 0. The exit record carries three more wordings: the look item reads 'turn speed drops to about 1/15.6' rather than 'locked' (V-31); all five `noGravity` exit paths were checked one by one (V-39); 'platform' is stated as ≥3×3 (V-46).
- [ ] **RT-15 reset**: `/effect clear @s` in the middle of phase 2. Passing looks like: within 100 ms the screen and the input are fully restored and the log shows 「状态复位（效果被外部清除）」 ("state reset (effect cleared externally)").

### 5.5 Common failure modes

1. **Copying Chebyshev for the trigger test** (D-73 is an explicit exception made for Zangief Cane): the existing trap implementations use Chebyshev distance, so copying someone else's test wrongly triggers at "1.5 blocks diagonally", and that is exactly the position F-87's item ② targets. Catch it early: stand once at 1.0 and once at 1.5 blocks diagonally — Chebyshev also treats 1.0 as a hit, while the spherical test accepts only Euclidean distance; judge the two controls together (2 blocks on the same axis does not trigger, 1.5 blocks diagonally does not trigger).
2. **Writing the fall at a fixed speed (A-03)**: the `dropVelocity` style — "set the downward velocity once on entering phase 2" — cannot land within 40 ticks from a four-block platform and lands early from a two-block one. The symptom is the three heights' `落地耗时 {N} 刻` scattered between 20 and 90 ticks. Catch it early: measure all three heights before signing off; measuring flat ground alone does not pass; after `fallTicks` becomes 80 all three numbers must grow together.
3. **Writing the camera angles back into the player's real orientation**: doing `player.setXRot(...)` right after `setPitch(-pitch)` makes `/data get entity @s Rotation` change during phase 2, breaking server authority (D-59's iron rule), and the view is also garbled after landing. Catch it early: read `Rotation` once during phase 2; `grep -rn "setXRot\|setYRot" client/ZangiefCameraController.java` hits 0.
4. **Registering only three events for the input lock (RT-13 / D-60)**: `MovementInputUpdateEvent` + `InteractionKeyMappingTriggered` + `InputEvent.Key` look sufficient, but the scroll wheel switching slots goes through `InputEvent.MouseScrollingEvent` and the inventory and chat go through `ScreenEvent.Opening` — miss those two and F-92's items ③④ go red, and "E still opens the inventory" is a particularly conspicuous break. Catch it early: hand-test all 21 items and keep the record; do not spot-check.
5. **The client not resetting after the hidden effect is cleared externally (RT-15)**: when the player runs `/effect clear @s` during phase 2 or a third-party "clear negatives" removes it, a client that reads the state only "when the effect changes" stays upside down with the keys locked forever. The client must self-check every tick whether the phase effect is still there, reset itself when it is not, and log it. Catch it early: enter phase 2, run `/effect clear @s`, and the screen and keyboard must be fully restored within 100 ms.
6. **Leaving two effect records behind on a phase switch**: forgetting to remove the old effect of the same id before switching makes `/data get entity @s active_effects` show two `zangief_piledriver` entries, and F-88's item ② "no second effect record appears" goes red immediately. Catch it early: right after the 14-tick switch, read back how many entries `active_effects` holds, then check whether the client hit a 1-tick window of `null`.

### 5.6 What to report

- The four phase log lines (phase 0 / 1 / 2 / end) + `落地耗时 {N} 刻` for the three heights.
- The 21-item key hand-test table (each item "blocked / got through") and the single exception, ESC.
- The three `/data get entity @s active_effects` read-backs in phases 0 / 1 / 2 (`amplifier` and remaining ticks).
- The unchanged `/data get entity @s Rotation` read-back during phase 2.
- The verbatim reset log line of `/effect clear @s`.
- The before and after counts proving the trigger disappearance drops nothing (one pair for the block and one for `item` entities).
- Open questions (especially whether `ComputeCameraAngles`'s roll survives later events under 21.1.213, when the client should read `getEffect`, and how `ScreenEvent.Opening` judges the ESC path).

---

## 6. Phase 15 · Resources, Audio, Advancements and Acceptance (F-94 ~ F-99)

> Feed method: the full text of `14-PROMPT-MAIN-0.0.2.md` + this section; feed one phase at a time. Internal order of this phase: F-94 → F-95 → F-96 → F-98 → F-97 → F-99 (F-97 waits for F-98).

### 6.1 Prerequisites

- Every exit criterion of Phase 12 / 13 / 14 passes: `F-99` depends on `F-75` / `F-85` / `F-93` / `F-94` / `F-96` / `F-97`, and if any one of those has not passed, F-99 may not start.
- `12-TECH-0.0.2.md` §6.1–§6.4 (the three source files with their conversion parameters, and the conversion script), §7.1–§7.4 (the 36-line resource list / the 41-line language keys / the checking method), and the 22-item self-check table of §11 have been read and copied into a check-off list.
- **This machine has ffmpeg, but it is not on PATH** (BRIEF §2's D-53 measurement: the usable build is `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`, 7.1 essentials with libvorbis; Trae CN's 6.1.1 has no libvorbis and is unusable; and all three sources — npm / maven / mirror — are unreachable). Running `F-94` therefore requires passing an absolute `-Ffmpeg` path. `F-94` still comes first in this phase so that failure shows up early (RT-14); the agent may not hunt for a substitute source or change the conversion spec on its own.
- The provenance and use of the three assets are confirmed (D-66): `Animals.mp3` → the `wild_hunter` loop sound; the SF6 clip `BV1qu411W7ha_846-850.mp3` → the Zangief Cane trigger sound; the desktop JPG → the Wild Hunter effect icon. The provenance statement goes into `README.md` (an F-99 deliverable).
- This phase may change whitelisted files only; everything under `src/generated/` is produced by Providers alone and must not be edited by hand.

### 6.2 Goal

Fill in the rest of the 40 resource deliverables (6 item / block textures, 2 OGG files, 1 effect icon, 9 model-side JSONs, 3 advancements) plus the 41 language keys, then align the **302 / 263 / 139 / 77** counts, run all 22 self-checks of `12-TECH` §11, and produce a releasable jar.

### 6.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-line deliverable |
|---|---|---|---|---|
| F-94 | Audio and effect-icon pipeline | L / 3.0 | F-60, F-64, F-84 | `tools/convert_media.ps1` (errors out when ffmpeg is missing) + 2 mono 64 kbps OGG files + `sounds.json` (2 events, each with a subtitle) + an 18×18 effect icon |
| F-95 | 6 item / block textures | L / 3.0 | F-60, F-65 | 4 16×16 item icons + the grayscale Compressed Hay Block texture + the Zangief Cane particle texture |
| F-96 | Blockstates and models: 9 JSONs | M / 1.5 | F-65, F-67, F-95 | 2 blockstates + 2 block models + 5 item models (the Zangief Cane model has only `textures.particle` and no `elements`) |
| F-97 | The 41 language entries and Chinese-English equality | S / 0.5 | F-61, F-64, F-65, F-98 | `ModLangProvider` lands 41 entries → 263 keys in each of the two files with equal key sets; `tooltip.farmstorm.*` still has 16 |
| F-98 | Three new advancements | S / 0.5 | F-69, F-76, F-87 | `first_molotov` / `zangief_hit` / `first_wild_hunter` with parents `first_throw` / `first_deploy` / `root`, bringing the advancement total to 15 |
| F-99 | Confirming the four counts, the three-mode regression, and packaging | L / 3.0 | F-75, F-85, F-93, F-94, F-96, F-97 | 302 / 263 / 139 / 77 aligned + a reading recorded for each of the 22 self-checks + the `dist` jar + the index section and the asset-attribution section of `README.md` |

### 6.4 Phase exit criteria

- [ ] **F-94 the two OGG files**: run `ffprobe` once on each `.ogg`. Passing looks like: codec `vorbis`, 1 channel (mono), bitrate ≈ 64 kbps, duration > 0 seconds.
- [ ] **F-94 the missing-ffmpeg failure path**: run the script on a machine without ffmpeg. Passing looks like: a non-zero exit code, the installation instructions printed to stdout, and **no falling back to the original extension, no silent skip** (D-53).
- [ ] **F-94 `sounds.json` and the icon**: read the file and go in-game. Passing looks like: both events carry a `subtitle` and both referenced files exist (no missing-file error from `processResources`); `wild_hunter.png` is 18×18 and the HUD icon is visible and not purple-black.
- [ ] **F-94 sound audibility and the provenance statement**: run the two `/playsound` commands in-game, then read `README.md`. Passing looks like: both are audible; `README` and the Release notes state the provenance of all three assets and the non-commercial personal-use declaration (D-66).
- [ ] **F-95 six PNGs**: read the dimensions of each one. Passing looks like: all 6 files are present and all are 16×16; the 4 new crop icons in the creative tab are not purple-black; the Compressed Hay Block texture is the same on all six faces with hue 0 (grayscale), the same shape as vanilla `hay_block_side`, and a brightness difference ≤ 8 %.
- [ ] **F-95 the Zangief Cane particle**: place one in the world and mine it out. Passing looks like: the block itself has no visible geometry and the break particles use `zangief_cane.png` (compare screenshots).
- [ ] **F-96 the nine JSONs**: read the files. Passing looks like: `blockstates/compressed_hay_block.json` has exactly **1 variant** (`cube_all`) whose `model` points at `farmstorm:block/compressed_hay_block`; `models/block/zangief_cane.json` has only `textures.particle` and **no `elements`**; `models/item/zangief_cane.json`'s `parent` is `minecraft:item/generated`; `models/item/compressed_hay_block.json`'s `parent` is `farmstorm:block/compressed_hay_block`.
- [ ] **F-96 the inventory**: open the creative tab and the inventory. Passing looks like: all 5 items render normally, with no purple-black and no missing-model warning.
- [ ] **F-97 key count and equality**: after `runData`, compare the two `.json` files. Passing looks like: **263** keys in each (222 + 41) and `set(zh) == set(en)` is True; the six-group breakdown of the 39 lines matches group by group (config 22 + item 5 + block 2 + effect 2 + subtitles 2 + advancements 6); `tooltip.farmstorm.*` is still 16.
- [ ] **F-98 the three advancements**: really trigger each one once, then query with `/advancement`. Passing looks like: throwing a Molotov pops `first_molotov`, a Zangief Cane hit pops `zangief_hit`, and gaining the effect pops `first_wild_hunter`, one toast each; `/advancement` finds all three IDs; the advancement total is **15**; `git diff` of the existing 12 is empty.
- [ ] **F-98 JSON shape**: read the three files. Passing looks like: `parent` is `farmstorm:first_throw` / `farmstorm:first_deploy` / `farmstorm:root` respectively, `frame` is `task`, `show_toast` is true.
- [ ] **F-99 302 resources**: `git ls-files 'src/main/resources' 'src/generated/resources'`, then deduct the 11 extensionless files under `.cache` and the 3 `.gitkeep` files. Passing looks like: **302** in total; JSON **195**, PNG **100**, OGG **2**, GLSL **3**, TOML 1, `pack.mcmeta` 1.
- [ ] **F-99 the other three numbers**: language keys, toml, self-check lines. Passing looks like: **263** keys in each language file with equal key sets; **139** keys in `config/farmstorm-server.toml`; both the `自检 注册对象 77 个 …` and `自检 实体渲染器 11/11 → OK` lines are in the log (self-check: 77 registered objects / entity renderers 11/11).
- [ ] **F-99 the 22 self-checks**: run them item by item per `12-TECH` §11. Passing looks like: all 22 pass, each with a recorded reading (one line each: ID / verdict / evidence location).
- [ ] **F-99 known cosmetic defect**: `WARN ClientRecipeBook: Unknown recipe category: farmstorm:convert` ×3 is **fixed** in this version (`RegisterRecipeBookCategoriesEvent#registerRecipeCategoryFinder`, V-67) — confirm the warning no longer appears and that the README does not list it as a known limitation.
- [ ] **F-99 structure and dedicated server**: run the two `grep` commands and start a server once. Passing looks like: `grep -r "CustomPacketPayload" src/main/java` returns 0; Mixin is checked by **actual usage** (`@Mixin` annotations, `mixins*.json`, `org.spongepowered`, `build.gradle` dependencies all 0, with the 5 literal hits all being explanatory comments, V-47); the dedicated server logs no `NoClassDefFoundError` and effect-related features work after a client connects.
- [ ] **F-99 jar contents**: `jar tf dist/farmstorm-0.0.2+mc1.21.1.jar`. Passing looks like: both `.ogg` files, `assets/farmstorm/sounds.json`, and the 4 grayscale-related resources (`shaders/core/grayscale.json` / `.vsh` / `.fsh`, `post_effect/grayscale.json`) are visible.

### 6.5 Common failure modes

1. **The script silently skipping when ffmpeg is missing (RT-14 / D-53)**: treating "no usable ffmpeg was found" as "skip this step" keeps the build green, leaves no `.ogg` in the jar, and leaves both effects silent — and that only surfaces at F-99's `jar tf`. D-53 requires that a missing ffmpeg **fails outright with installation instructions** and a non-zero exit code. Catch it early: run `pwsh -File tools/convert_media.ps1 -CheckOnly` first — without `-Ffmpeg` and with nothing on PATH it must print the installation guidance and exit 1, while a usable path must report the environment as usable and exit 0.
2. **Passing off an mp3 renamed to `.ogg`**: the name shows up in `jar tf` and `processResources` does not complain, but it does not play at runtime and `ffprobe` exposes it at once. Catch it early: run `ffprobe` on every `.ogg` and verify `vorbis` / mono / 64 kbps; never judge by file name and size alone.
3. **A Zangief Cane model that carries `elements` (D-68)**: writing the model like an ordinary block puts a block in plain sight — Zangief Cane must be fully invisible, keeping only `textures.particle`. This is the mistake that is glaringly obvious once made and extremely easy to copy-paste into DataGen. Catch it early: `elements` must hit 0 in `models/block/zangief_cane.json`; after `/setblock` take a screenshot and confirm nothing is visible.
4. **Adding the language entries in Chinese only (A-04 / `12-TECH` §7.4)**: the key names of the 22 `farmstorm.configuration.*` entries must match the 22 toml keys verbatim (the prose form is `<group>.<key>`, the language key is `farmstorm.configuration.<group>.<key>`); adding them only to `zh_cn.json` and missing `en_us.json` flips `set(zh) == set(en)` to False immediately. Catch it early: after every `runData`, diff the key sets and count 263 and the 16 of `tooltip.farmstorm.*` at the same time.
5. **A wrong advancement parent, so it is "visible but unverifiable"**: `first_molotov`'s parent is `farmstorm:first_throw`, `zangief_hit`'s is `first_deploy`, `first_wild_hunter`'s is `root`; with a wrong parent path the ID still shows up under `/advancement`, but no toast pops on a real trigger. Catch it early: really trigger all three and watch the toasts; do not substitute `/advancement grant`.
6. **F-97 scheduled before F-98**: the 6 advancement text keys depend on the 3 advancement paths, so doing the language file first leaves 6 keys missing and 263 becomes 257. Catch it early: append the three entries to `ModAdvancementProvider`'s `AWARD_REQUIRED` before touching `ModLangProvider`.

### 6.6 What to report

- The commands and results for the four counts (302 / 263 / 139 / 77) and the six-way breakdown of the resources (JSON 195 / PNG 100 / OGG 2 / GLSL 3 / TOML 1 / `pack.mcmeta` 1).
- The key counts of the two language files, the empty verdict of the key-set diff, and the 16 of `tooltip.farmstorm.*`.
- The three verbatim `ffprobe` lines of the two `.ogg` files (codec / channels / bitrate), plus the script's exit code and message text when ffmpeg is missing.
- A reading for each of the 22 self-checks (one line each: ID / verdict / evidence location).
- The toasts of the three advancements and the `/advancement` query results; the `parent` line of each of the three JSONs.
- The presence in `jar tf dist/farmstorm-0.0.2+mc1.21.1.jar` of the two `.ogg` files, `sounds.json`, and the 4 grayscale resources.
- The verbatim lines showing 0 matches for the two `grep` commands.
- Open questions (especially the semantics of `sounds.json`'s `attenuation_distance` under 21.1.213, and how a wooden pickaxe behaves once `needs_diamond_tool` is stacked on top of `requiresCorrectToolForDrops`).

---

## 7. Inter-Phase Hand-Off Wording (hand-off)

Template (replace the square brackets with real content; send one at a time):

~~~text
The previous phase (Phase N · [title]) has passed acceptance against the exit criteria of 15-PROMPT-TASKS-0.0.2.md: [one sentence of evidence, for example "the build produced the 0.0.2 jar, 139 config keys, and all four self-check lines are OK"].
This phase begins: Phase N+1 · [title].
Follow the six parts of the "Phase N+1" section of 15-PROMPT-TASKS-0.0.2.md strictly: first confirm "prerequisites" item by item and report, then touch the first line of code; do only the F-xx tasks in that section's task list, do not implement the next phase in advance, and when done hand the report back in the report format of 14-PROMPT-MAIN-0.0.2.md.
~~~

Five ready-to-paste instances:

| Hand-off | A sentence you can paste directly |
|---|---|
| Phase 11 → 12 | The previous phase (Phase 11 · Foundation and Registry Increment) is complete: `.tools\gw.cmd build` exited 0 and produced `farmstorm-0.0.2+mc1.21.1.jar`, all four self-check lines are OK (19 crops / 77 registered objects / 16 berry pool entries / 11-11 renderers), and `farmstorm-server.toml` has 139 leaf nodes. This phase begins: Phase 12 · Compressed Items and Molotov! — follow the "Phase 12" section of 15-PROMPT-TASKS-0.0.2.md, confirm "prerequisites" item by item before touching the first line of code, do only F-67 ~ F-75, and hand the report back in the report format of 14-PROMPT-MAIN-0.0.2.md. |
| Phase 12 → 13 | The previous phase (Phase 12 · Compressed Items and Molotov!) is complete: the 5 new recipes work, throwing a Molotov only appends fire and magma blocks with not one original block missing, Compressed Hay Block does not break to the Melon Grenade or the Melon Slice Bomb but does break to the Ubi Server and to TNT, and the conversion recipe total is 24. This phase begins: Phase 13 · Wild Hunter — follow the "Phase 13" section of 15-PROMPT-TASKS-0.0.2.md, do only F-76 ~ F-85, and hand the report back in the report format of 14. |
| Phase 13 → 14 | The previous phase (Phase 13 · Wild Hunter) is complete: all seven are observable — the three attribute bonuses, the 5-block 30° attraction dash, damage and knockback, kill extension without a cap, world-only grayscale (the HUD keeps its original colours), red silhouettes on visible mobs, and the 16-block looping sound; everything resets after `/effect clear`; `CustomPacketPayload` and `Mixin` match counts are both 0. This phase starts Phase 14 · Zangief Cane; follow section 5 of 15-PROMPT-TASKS-0.0.2.md … |
| Phase 14 → 15 | The previous phase (Phase 14 · Zangief Cane) is complete: the three phases (inversion → levitation VII for 20 ticks → falling self-rotation) run through in order, landing time from all three heights (flat ground / two blocks / four blocks) falls within 40 ± 2 ticks, only ESC is the exception in the 21-item key hand test, and `/data get entity @s Rotation` never changes throughout. This phase begins: Phase 15 · Resources, Audio, Advancements and Acceptance — follow the "Phase 15" section of 15-PROMPT-TASKS-0.0.2.md, work in the internal order F-94 → F-95 → F-96 → F-98 → F-97 → F-99, and hand the report back in the report format of 14. |
| Phase 15 → closing | The previous phase (Phase 15 · Resources, Audio, Advancements and Acceptance) is complete: 302 resources / 263 language keys (Chinese and English equal) / 139 config / 77 registered objects all line up, all 22 self-checks of `12-TECH` §11 have a recorded reading, and `dist/farmstorm-0.0.2+mc1.21.1.jar` exists |

---

## 8. The Whole-Project Closing Prompt (fed after Phase 15 passes)

~~~text
Project close-out. Perform one complete delivery check on farmstorm v0.0.2: verify and fix only, add no functionality.

1. Build
   Run .tools\gw.cmd clean build (do not call gradle directly) and confirm the artifact is build/libs/farmstorm-0.0.2+mc1.21.1.jar;
   unpack it and confirm META-INF/neoforge.mods.toml resolves the version as 0.0.2 and modId as farmstorm.

2. Resource inventory (you must count class by class; reporting only a total is invalid)
   Re-run .tools\gw.cmd runData and count class by class per the wording of 12-TECH-0.0.2.md §7.1 / §7.4;
   the total must be 302: JSON 195 / PNG 100 / OGG 2 / GLSL 3 / TOML 1 / pack.mcmeta 1,
   excluding the 11 extensionless files of .cache under src/generated/resources (they do not go into the jar and do not count toward 302).
   Run runData once more: git diff --stat must be empty (no trace of hand-edited generated output).
   Each of the two language files has 263 keys and set(zh) == set(en) is True; among them tooltip.farmstorm.* is still 16.
   config/farmstorm-server.toml has 139 leaf nodes, and the language keys carrying the farmstorm.configuration. prefix are also 139.

3. The 22 acceptance self-checks
   Open docs/zh/12-TECH-0.0.2.md §11, run #1 ~ #22 item by item, and output a 22-row check table
   with three columns per row: ID / verdict / evidence location (log line, command return value, or file path).
   #1 ~ #4 are verbatim comparisons against the startup log (19 crops / 77 registered objects / 16 berry pool entries / 11-11 renderers),
   #21 and #22 are structural assertions (0 grep matches, and the dedicated server does not crash); the rest are hand tests.

4. What the jar must contain
   jar tf dist/farmstorm-0.0.2+mc1.21.1.jar, confirming all three groups of entries below are present:
   ① assets/farmstorm/sounds/wild_hunter_loop.ogg and zangief_piledriver.ogg (the two .ogg files);
   ② assets/farmstorm/sounds.json;
   ③ the 4 grayscale-related resources: shaders/core/grayscale.json, grayscale.vsh, grayscale.fsh, post_effect/grayscale.json.
   If any one is missing, stop and report; do not declare it releasable.
   Do not judge the .ogg files by name alone: run ffprobe on each, confirming vorbis, mono, about 64 kbps.

5. The three red lines
   grep -r "CustomPacketPayload" src/main/java and grep -rn "Mixin" src/main/java both return 0 matches;
   git diff of docs/zh/00~09 and docs/en/00~09 is empty (v0.0.1 frozen files, D-49);
   give one piece of "unchanged" evidence each for the fields of the 15 existing CropProfile entries, the default values of the existing 117 config keys,
   the existing 21 conversion recipes, and the existing 12 advancements (git diff shows added lines only, or the corresponding file's diff is empty).

6. Check all 40 tasks against the definition of done
   Open docs/zh/13-TASKS-0.0.2.md and output a 40-row check table over F-60 ~ F-99,
   with four columns per row: task ID / title / met or not / evidence location.
   Judge by the definition of done of 14-PROMPT-MAIN-0.0.2.md and the three gates of §0.3,
   then match each acceptance assertion of that task in 13-TASKS one by one; missing one means "not met".
   If any row is "not met", stop and report; do not declare it releasable.

7. Report format
   Output in the report format of 14-PROMPT-MAIN-0.0.2.md, and append three small tables: the resource inventory table, the 22-item self-check table, and the 40-row task check table.
   Do not paste complete logs (for each command paste only the conclusion line and the first error); write "unverified" for anything you did not run or did not verify,
   and never write it as "passed".
~~~

Three criteria for the close-out (reviewed by human eyes, not by the agent's self-report):

1. The six classes in the resource inventory table sum to **302**, and every row can be pointed to a directory; the 11 under `.cache` really are left out.
2. All **22** items of `12-TECH-0.0.2.md` §11 are green and each has an evidence location — #9 (TNT can blow up Compressed Hay Block) and #21 (0 network packets) must be named individually.
3. The task check table has **40 rows** and "not met" is 0; if there is one, make it up and re-run this section — without making it up, no release.

---

## 9. Consistency Between This File and 13-TASKS-0.0.2.md (self-check record)

| Phase | `13-TASKS` task range | `13-TASKS` task count | Tasks carried over into §x.3 of this file | Equal |
|---|---|---|---|---|
| Phase 11 | F-60 ~ F-66 | 7 | 7 | equal |
| Phase 12 | F-67 ~ F-75 | 9 | 9 | equal |
| Phase 13 | F-76 ~ F-85 | 10 | 10 | equal |
| Phase 14 | F-86 ~ F-93 | 8 | 8 | equal |
| Phase 15 | F-94 ~ F-99 | 6 | 6 | equal |
| **Total** | **F-60 ~ F-99** | **40** | **40** | **equal (the difference set is empty)** |

Four more self-checks:

| Check | Method | Verdict |
|---|---|---|
| Task IDs and titles | Extract the 40 `#### F-xx · title` headings from `13-TASKS-0.0.2.md` and compare them line by line against the "ID + title" of §2.3 ~ §6.3 of this file | 40 / 40 identical verbatim |
| Complexity and person-days | Proofread S / M / L and the converted person-days phase by phase, then add them against the subtotals of `13-TASKS` §2 | 11.0 + 10.5 + 16.0 + 11.5 + 11.5 = **60.5 person-days**; the three counts S 10 / M 23 / L 7 agree |
| Dependency column | Compare line by line against the "dependencies" field of each task block | 40 / 40 agree, including the one numeric inversion where `F-97` depends on `F-98` |
| Adjudication citations | This file cites adjudication IDs only (`A-01` / `A-03` / `A-05` / `A-06` / `A-07`) and does not copy the source text of `13-TASKS` §0.4 | Follows the single-source convention; §0.4's five entries are rewritten as "old wording / correct wording" without changing the verdict |

Maintenance convention: if `13-TASKS-0.0.2.md` revises a task's title, dependencies, or person-days, this section and §2.3 ~ §6.3 must be compared again; this file does not change titles, IDs, or add tasks on its own.

## Translator's notes

1. §0.2's second prohibition says "do not force Phase 15 into numeric order" and gives `F-97` → `F-98` as the reason; §6's feed order (F-98 before F-97) repeats the same point. Both are reproduced as written.
2. The startup self-check log lines (`自检 作物 19 种…`, `自检 幸运浆果池 16 种…`, `自检 注册对象 77 个…`, `自检 实体渲染器 11/11 → OK`) and the runtime log-line templates (`莫洛托夫 落点 … 威力 0`, `候选 {N} 火 {F} 岩浆 {M}`, `狂野猎人 目标 … 夹角 … 距离 …`, `狂野猎人 续时`, `桑吉尔蔗 扫描`, `桑吉尔蔗 相位 0`, `落地耗时 {N} 刻`, `状态复位（效果被外部清除）`) are kept in Chinese verbatim, because they are compared character by character against the logs; an English gloss follows in parentheses at first occurrence only.
3. §0.4 A-01 says Molotov gets exactly one probability key and that "the new keys therefore total 22", while §0.5 row 8 lists the same 22 keys by group (molotov 3). Both figures are reproduced as written; this translation does not reconcile the group count with A-01's one-key rule.
4. `§6.4`'s F-99 acceptance repeats the "263 keys in each language file" requirement already asserted by F-97, and `§9` sums the five phases as 11.0 + 10.5 + 16.0 + 11.5 + 11.5 = 60.5; both are carried over unchanged.
5. The `~~~text` fences of §7 and §8 are reproduced exactly as in the source rather than converted to triple backticks.
