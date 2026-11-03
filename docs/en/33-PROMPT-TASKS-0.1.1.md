# 33 · v0.1.1 Phased Task List (Phase Prompts)

> How to feed it: **feed the master prompt of document 32 once first**, then feed this file section by section in Phase order; only one Phase at a time.
> Every Phase is executable on its own from beginning to end — hand one section to the agent alone, and it needs no sight of the previous section.

| Item | Value |
|---|---|
| Document number | 33-PROMPT-TASKS |
| Version | v0.1.1 (MC 1.21.1 / NeoForge 21.1.213 / Java 21) |
| Readers | the person feeding a coding agent + the CLI coding agent being fed |
| Upstream | `31-TASKS-0.1.1.md` (**the only source of F-176 ~ F-212**: numbers, titles, Phase ownership, complexity, person-days, dependencies), `30-TECH-0.1.1.md` (the 38 acceptance entries of §7, the 8 to-be-verified APIs of §8, RT-31 ~ RT-41), `28-DECISIONS-0.1.1.md` (D-124 ~ D-147, the red lines of §4, the adjudication ledger of §8) |
| Peer | `32-PROMPT-MAIN-0.1.1.md` (the resident master prompt; this file **does not repeat** its hard constraints and its definition of done) |
| Content | **6 Phases / 37 tasks (F-176 ~ F-212)**; every Phase has the same six subsections: Preconditions / Goal of this phase / Task list / Exit criteria / Pitfalls / Reporting format |
| Boundary | read the upstream only: change none of `docs/zh/00` ~ `32`, nothing under `docs/en/`, nothing in `src/`, none of the three ledgers under `.work/` |

---

## 0. Feeding order and constraints

### 0.1 The feeding order of the six sections (Phase 26 → 31) and each section's preconditions

| Order | Which section to feed | Phase · milestone | Tasks | Person-days | Preconditions of this section (open only if every one holds) |
|---|---|---|---|---|---|
| 1 | §1 | 26 · **M22** | F-176 ~ F-177 | 2.0 | the work tree is clean; `.tools\gw.cmd build` exits 0; `run/config/farmstorm-server.toml` is a 0.1.0 product (59 groups, `[farmland]` present), and **this section must not delete it** |
| 2 | §2 | 27 · **M23** | F-178 ~ F-184 | 11.5 | every exit criterion of §1 passes; the source image `wild_apple_64x64.png` (6,946 B) is in place; the old `farmstorm-server.toml` is saved as a separate copy, for the "keep the old toml" comparison of F-182 |
| 3 | §3 | 28 · **M24** | F-185 ~ F-186 | 4.5 | every exit criterion of §1 passing is enough; this section **does not depend on** §2 / §4 and may be fed alternately with §2 or §4, but still only one section at a time |
| 4 | §4 | 29 · **M25** | F-187 ~ F-198 | 21.5 | every exit criterion of §1 passes; a 0.1.0 legacy save (only `owners`, no `dataVersion`) as the fixture for F-188 / F-190; an NBT viewer at hand |
| 5 | §5 | 30 · **M26** | F-199 ~ F-205 | 15.0 | the three tasks F-187, F-188 and F-189 of §4 are committed (the `isAlly(Owner, Owner)` overload, `FarmlandLedger` v2, `Entry.gameTime` and the three accessors); `CustomPacketPayload` across the repo is still at 0 hits at this moment |
| 6 | §6 | 31 · **M27** | F-206 ~ F-212 | 11.0 | all exit criteria of §2 / §4 / §5 pass; **the A-40 window is closed** (F-192 has landed and the old toml has been deleted and regenerated once); `.work/v011-verify/` is writable |
| 7 | §7 | close-out (not inside any Phase) | — | — | every exit criterion of §6 passes, and all 77 criteria and all 37 tasks have a conclusion |

Three notes on reading the table:

1. **The order here is the feeding order, not the only executable order.** §2 / §3 / §4 do not depend on one another and each waits only on §1; when feeding them in parallel, follow "one section at a time, and do only the F-xx of the section being fed" — do not mix the sections.
2. **§5 cannot be moved before §4**: the HUD overlay (the overlay)'s sort key (`gameTime`) and the "allied" test (the `isAlly` overload) live in F-189 and F-187 respectively; start §5 before §4 is finished and the HUD overlay holds an empty list only.
3. **Two stretches inside §4 must be serial**: `F-191 → F-192 → F-197` is a deletion chain (`base()` must first have no reader, only then may the config group go, and only then may the command tree go whole; **A-56**: the declared dependency of F-192 is **F-191** — `base()` is the only reader of `farmland.limitPerPlayer`, a matter of **compile order**, not of style order); `F-193 → F-194 → F-196` is the other one (core selection → binding write → migration all read the same `Decision.core`). The two chains may run in parallel.

### 0.2 The three lines that must be restated at the head of every section (context-independent)

Paste the three lines below **verbatim** at the head of every feed (right after the master prompt of document 32 and before the body of this section). These three lines are the only wording of this section that may be "executed without explanation", and the section does not repeat them:

```text
① Only one red line is left: no Mixin (@Mixin / mixins.json / @Inject / @Redirect / @Accessor at 0 hits repo-wide).
   The red line on custom network packets has been abolished outright (D-146): this version has exactly 1 S2C read-only packet farmstorm:core_list, and the client sends none back.
   "0 custom network packets" is the old 0.1.0 wording: do not follow it, and do not write it as "this version happens to allow 1".
② Three numbers must not be touched: registry objects 93 (12 categories, +0 per category); server config 166 keys + client 3 keys (a separate farmstorm-client.toml);
   323 language keys × 2. At the end of this section these three numbers may not have arrived yet: accept against the interval reading §x.4 of this section gives, and do not change it to the target value on your own.
③ tools/verify_project.py cannot serve as a pass criterion until F-192 is closed out (A-40): during the window its [FAIL] is expected,
   and only two key-level differences may appear (before F-182: pumpkin_turret.bulletSpeed missing, farmland.limitPerPlayer extra)
   or one (after F-182 up to F-191: only farmland.limitPerPlayer extra). One key name more or less, and the parser is wrong.
```

### 0.3 Quick reference (one screen: Phase ↔ milestone ↔ task IDs ↔ person-days ↔ exit criterion in one sentence)

| Phase | Milestone | Task IDs | Count | Person-days | Exit criterion (one sentence) |
|---|---|---|---|---|---|
| 26 Baseline and tool chain | M22 | F-176 ~ F-177 | 2 | 2.0 | the artifact name and the banner are both 0.1.1; the tool recomputes **58 groups / 166 keys** per document 30 §4.1 and deducts the 1 deleted key; the intermediate state shows **exactly two** key-level differences, and a non-zero exit code is expected |
| 27 Textures, values and block properties | M23 | F-178 ~ F-184 | 7 | 11.5 | `wild_apple.png` is **64×64 / 6,946 B**, and deleting the image and re-running neither falls back nor raises a size assertion; the sha256 of the other **24** is unchanged; the five sound categories of Compressed Hay Block are stone; the core's `Light = 15`; the 5 keys of `[pumpkin_turret]` are **40 / 3 / 0.45 / 15 / 1**; 5 shots within 1 block, 5 hits, 15 points lost |
| 28 The Friend Chives persistent field | M24 | F-185 ~ F-186 | 2 | 4.5 | once landed it is a field of `effectTicks` ticks (tick 1 → 200, tick 199 → 1, gone at tick **200**; change it to 100 and it stops at 100); an empty drop plays no sound, and a hit plays exactly **1**; after an unload and return the entity does not exist and does not revive |
| 29 The farmland–core binding regime | M25 | F-187 ~ F-198 | 12 | 21.5 | hoeing binds to the nearest allied core, and the **17th** block is refused; an offline core owner's farmland still cannot be trampled; the two levels of core removal (re-bind / turn to dirt); the backfill "耕地补建：绑定 N 块 / 保持未绑定 M 块" is **identical** on two startups; `/farmstorm` disappears; the `[farmland]` section disappears, section lines **58**, and the tool's **first `FAIL 0`**; with nobody around the core still ticks, and `chunks.dat` does not contain the core's coordinates |
| 30 The HUD overlay and the data face | M26 | F-199 ~ F-205 | 7 | 15.0 | the overlay shows 1 line `#1 [overworld] (x, y, z) 自己`; 9 cores → **8 + `+1` = 9 lines / 93 px** (8 cores 83 px); H toggles and persists, J drags, `ESC` saves, `/farmstorm hud on\|off\|reset` writes all three actions to disk; nothing is left behind on a server switch and enemy cores are never sent out; `CustomPacketPayload` repo-wide is **exactly 1 file**, the client sends nothing back, and a dedicated server starts normally |
| 31 Close-out and full acceptance | M27 | F-206 ~ F-212 | 7 | 11.0 | **323** keys in each of the two lang files and the key sets identical (`farmstorm.configuration.` **169**); `dist/farmstorm-0.1.1+mc1.21.1.jar` is releasable and a pure server can start; **38 + 39 = 77** entries all have a reading, **0** failing; the only red line left is "no Mixin" |
| **Total** | **M22 ~ M27** | **F-176 ~ F-212** | **37** | **65.5** | bare person-days 65.5 (`5 × 0.5 + 22 × 1.5 + 10 × 3.0`), with the 25 % buffer **81.875**; critical path **15.5 person-days / 7 tasks** (the HUD overlay line) |

---

## 1. Phase 26 · Baseline and tool chain (F-176 ~ F-177)

> How to feed it: the full text of the master prompt of document 32 + the three lines of §0.2 + this section.

### 1.1 Preconditions

- The work tree is clean: `git status` should show only the files this section changes; `.tools\gw.cmd build` currently exits 0. The only recognised build entry point is `.tools\gw.cmd build`; do not call gradle directly, and `dist/` is outside this section.
- These four at hand: the 58 groups / 166 keys table of `30` §4.1 and the deleted-key register of §2.3; `28`'s D-124 (the single source of the version number), D-139 (deleting `farmland.limitPerPlayer`), D-147 (upgrade notes); `31` §0.4 (the three scheduling rules of A-40); `31` Table A rows 1 / 2 / 43 / 44.
- `run/config/farmstorm-server.toml` is a **0.1.0 product** (59 groups, `[farmland]` present, `pumpkin_turret` with only 3 keys). This section **must neither delete it nor edit it by hand**: delete it and the "missing key" difference is gone, and the intermediate-state criterion of A-40 degenerates to a single entry.
- The tool's current state is known: the config expectation set of `tools/verify_project.py` is recomputed from **four old documents** (`01-DESIGN` / `12-TECH-0.0.2` / `18-TECH-0.0.3` / `24-TECH-0.1.0`) (the four functions at `:222-237` + the union at `:244`). Before this section it is **red** against v0.1.1, but not yet red in the "two keys" way — that is the deliverable of F-177 in this section.
- This phase produces no behaviour at all: it only decides "whether every later acceptance has a trustworthy ruler". Apart from one line of `gradle.properties`, the seven old-version strings of the four `.cmd` scripts and the parsing stretch of `tools/verify_project.py`, no file may be touched.

### 1.2 Goal of this phase

First nail down the two "take the numbers" items: the tool's config expectation source (F-177) and the version number (F-176). **This section does F-177 first**: "second place in Phase 26" in `31` §0.4 is said by task number (F-176 comes first) and does not conflict with the execution order — F-176 only changes a version string and is the precondition of no task; F-177 must come before any task that would cite a conclusion of the tool. The two may swap commit order, but **F-177 must land before any section from §2 onwards starts work**.

What can be done at the end of this section: get this version's config expectation set with one command (**58 groups / 166 keys**, `farmland.limitPerPlayer` already deducted), and know why it is red at this moment (only two key-level differences, and the key names of both are known); the artifact name, the jar name hard-coded in the scripts, the script banner and the version line of `neoforge.mods.toml` — all four are **0.1.1**.

### 1.3 Task list (F-176 ~ F-177)

| Task ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-176 | Version number 0.1.1 and the jar names / banners of the two build scripts | S / 0.5 | — | `mod_version` at `gradle.properties:11` becomes `0.1.1`; the jar names of `检查环境.cmd` / `一键构建.cmd` (including `-sources.jar`) and the `v0.1.1` of their banners follow; the artifact becomes `build/libs/farmstorm-0.1.1+mc1.21.1.jar` |
| F-177 | Point the config expectation source of `verify_project.py` at v0.1.1 | M / 1.5 | — | the tool gains `expected_config_keys_v011(...)` (parsing the 58 groups / 166 keys of document 30 §4.1) and folds it into the union, while deducting `REMOVED_KEYS = {"farmland.limitPerPlayer"}` uniformly **beside** the union; a parse failure reports the file name + the table row number and does not skip silently |

Phase subtotal: **2 tasks, 2.0 person-days** (S 1 / M 1 / L 0).

### 1.4 Exit criteria (observable)

- [ ] **Artifact name and version line (F-176)**. How to verify: after `.tools\gw.cmd build`, read `META-INF/neoforge.mods.toml` through `jar tf`. What passing looks like: exit code 0; `build/libs/farmstorm-0.1.1+mc1.21.1.jar` and `farmstorm-0.1.1+mc1.21.1-sources.jar` both in place; no leftover `farmstorm-0.1.0*` or `farmstorm-0.0.1*` in `build/libs`; the `version` line is `0.1.1`.
- [ ] **Seven old-version strings changed in one go (F-176)**. How to verify: `grep -c` on three patterns + reading the head of the two scripts. What passing looks like: `farmstorm-0.0.1` is **0** in all four scripts; `farmstorm-0.1.1+mc1.21.1` is **1** in `检查环境.cmd` and **2** in `一键构建.cmd`; the version string in all four scripts' banners is the same `v0.1.1`; `git diff gradle.properties` shows one changed line only, line 11.
- [ ] **The two numbers of the expectation set (F-177)**. How to verify: the §1 section of `python -X utf8 tools/verify_project.py`. What passing looks like: it prints "30-TECH §4.1：**58** 组 / **166** 键" and "剔除已删键 1 个：farmland.limitPerPlayer".
- [ ] **Only two entries in the intermediate state (A-40, the hardest one in this section)**. How to verify: in the same run, count the `[FAIL]` / `[STALE]` lines of the §1 section and the key names inside them. What passing looks like: **exactly 2** in total, with the key names only `pumpkin_turret.bulletSpeed` (missing) and `farmland.limitPerPlayer` (extra); **a non-zero exit code is expected**, and this section must not touch `run/config` or `docs/` to drive it to zero. A third key name means F-177 failed (that is the only symptom of a parser written wrong).
- [ ] **The failure path works (F-177)**. How to verify: temporarily change one row of the "groups" column of §4.1 (a local experiment, not committed) and run it. What passing looks like: a non-zero exit code, with the error carrying the file name and the table row number; after restoring it the exit code returns to 0.
- [ ] **Read the upstream only (F-177)**. How to verify: `git diff --stat docs/`. What passing looks like: empty; `grep -c "expected_config_keys_v011" tools/verify_project.py` ≥ **2** (the definition + the call).
- [ ] **The two places this section did not touch (the control surface)**. How to verify: read `run/config/farmstorm-server.toml` and the two lang files. What passing looks like: the `[farmland]` section is still there, section lines **59**, keys **166**, `pumpkin_turret` still **3** keys; **315** keys in each of the two lang files (the adding and deleting of language keys starts only at F-182 in §2).

### 1.5 Pitfalls

1. **Adding the source but not deducting the key** (item 3 of F-177, A-40): `expected_config_keys_v011` is written correctly, but `farmland.limitPerPlayer` is not deducted from the union. What it turns into: the early part of the window shows nothing wrong (at that time the key is still in the old toml, so both sides have it), and it is only after F-192 deletes the `[farmland]` section that it surfaces as a **permanent** "missing `farmland.limitPerPlayer`" — the `FAIL 0` of F-192 never holds, and what the operator sees is "the tool says I am short of a key I already deleted". Catching it early: `REMOVED_KEYS` may appear exactly once, **beside** the union expression, and must not be scattered over two places; the acceptance of F-177 must contain the printed line "剔除已删键 1 个".
2. **Writing a hard-coded fallback key list**: falling back on "11 keys hard-wired" when the parse fails. What it turns into: the documents change and the tool raises nothing, and the silent blind spot comes back on the spot — exactly the kind of degeneration A-40 exists to kill. Catching it early: only one stretch of config logic is allowed in the tool; a parse failure must report the file + the table row number.
3. **Changing the frozen zone or deleting the toml to turn the tool green** (the three scheduling rules of A-40): changing `30` §4.1 so the expectation set matches the status quo, or simply deleting `run/config/farmstorm-server.toml`. What it turns into: the former breaks "the tool reads the upstream only" (`git diff --stat docs/` non-empty), the latter wipes the "missing key" difference along with it (2 entries become 1), and the old-toml comparison of F-182 loses its baseline too. Catching it early: before this section ends the `[farmland]` section is still there, `pumpkin_turret` still has 3 keys, and the diff of `docs/` is empty.
4. **Reading the table by row number instead of parsing the two columns "group + key"**: the rows of `30` §4.1 carry annotations such as "(**change** 1 → 3)". What it turns into: `bulletSpeed` is parsed as `0.45` or as a string with brackets, and the self-check numbers 58 / 166 do not match. Catching it early: fail when the self-check does not match, and print the row number of the first non-conforming table row.
5. **Changing only the jar name in the banner, not the version string**: `grep "farmstorm-0.0.1"` gives 0, but the script head still says `Farmstorm v0.0.1`. What it turns into: the script's prompt is decoupled from the artifact, and the author sees a version that does not match the jar name while running the script. Catching it early: read the banner of both scripts once each and look at `v0.1.1` character by character.
6. **Upgrading the build chain along the way**: changing `build.gradle` because a new NeoForge version appeared, or moving the Java target somewhere else. What it turns into: the diff of `gradle.properties` is more than one line, and every later jar and version line of `neoforge.mods.toml` stops being comparable. Catching it early: `git diff gradle.properties` may show line 11 only; the diff of `build.gradle` and `settings.gradle` must be empty.
7. **Doing §2's work early**: changing `wild_apple` to 64×64 along the way, or adding `pumpkin_turret`'s `bulletSpeed` into config. What it turns into: the intermediate state goes from 2 entries to 1, and the acceptance of F-177 loses its criterion. Catching it early: `git status` may show 4 files only (`gradle.properties`, the two `.cmd` scripts, `tools/verify_project.py`).

### 1.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; for each task the file paths + the changed lines (the seven old-version strings of F-176, the new function and the position of `REMOVED_KEYS` of F-177).
- **Commands run**: the exit code and artifact path of `.tools\gw.cmd build`; the **complete §1 section, verbatim**, and the exit code of `python -X utf8 tools/verify_project.py` (including the key names of those two differences); the version line of `neoforge.mods.toml` inside `jar tf`; the readings of the three `grep -c` runs; the empty output of `git diff --stat docs/`.
- **What was observed**: the two numbers 58 / 166; the name of the deducted key; the line count and the key names of `[FAIL]` / `[STALE]`, one by one; the section line count, the key count and the key count of `pumpkin_turret` in `run/config/farmstorm-server.toml`; the key count of the two lang files (both should be 315).
- **Not done and not doable**: one line per item, stating "not verified" and why (for example the dedicated server was not started, the source image was not at hand).
- **Open questions**: for example whether the tool still holds a second hard-coded total, and whether the §1 section still holds old assertions unrelated to this version.

---

## 2. Phase 27 · Textures, values and block properties (F-178 ~ F-184)

> How to feed it: the full text of the master prompt of document 32 + the three lines of §0.2 + this section.

### 2.1 Preconditions

- Every exit criterion of §1 passes, F-177 in particular (the tool already recomputes at 58 groups / 166 keys, and the intermediate state has exactly two entries). This section **may** read the tool, but **only to watch the two differences shrink**; `FAIL 0` must not be taken as the criterion (A-40).
- These four at hand: `30` §7's #2 ~ #4, #23 ~ #28, #33 ~ #35; `30` §2.6 (the three branches of point-blank convergence and `MIN_STEP`); `28`'s D-125 ~ D-130, D-141, D-147, and A-30 / A-31 / A-32; `31` Table A rows 3 ~ 15, Table C rows 1 ~ 3, Table E's RT-37 / RT-38.
- The source image `wild_apple_64x64.png` is in place, **6,946 B**; it is the only source of `wild_apple.png`, and this version does not regenerate the other 24 (`25 − 1 = 24`).
- The 0.1.0 copy of `run/config/farmstorm-server.toml` is saved separately: "keep the old toml and upgrade in place" of F-182 needs it.
- The frozen surface is confirmed: the bytes of the other 24 PNGs, `clientTrackingRange(4)` / `updateInterval(10)` of `FarmstormEntityTypes`, the config defaults other than `farmlandBonus`, `models/item/wild_apple.json`, the item and block ids — every `git diff` of these must be empty.
- This section produces no new behaviour; it changes only the seven "visible" and "touchable" things: one image, one generator, five sound categories, one block of light, three values, two read points, one stretch of convergence.

### 2.2 Goal of this phase

Finish the seven things in one go and let them corroborate each other **within the same feed**: only after the generator drops the "+" overlay mechanism (F-178) can `wild_apple` safely take the 64×64 pass-through (F-179 — the two changes touch the same file, delete the mechanism first and add the branch second); the two block properties are one line each (F-180 / F-181); the three keys of the Pumpkin Turret (man瓜) (F-182) → the two read points (F-183) → the convergence and the shared settlement body (F-184) are a three-step chain, the previous step not being rolled back and the next step not being allowed to start.

What can be done at the end of this section: put the jar into a client and Wild Apple is the new 64×64 image, and deleting the image and re-running the generator does not fall back; the other 24 item textures still carry the green cross in the bottom-right corner (this version regenerates only 1); breaking / placing / stepping on / mining / landing of Compressed Hay Block all sound like stone; the Outpost Core lights up to 15 as soon as it is placed down; the Pumpkin Turret fires one shot every two seconds, 3 points each, and the 0.45 projectile speed is editable in the toml; with the target within 1 block, 5 shots in a row all hit.

### 2.3 Task list (F-178 ~ F-184)

| Task ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-178 | The generator drops the "+" overlay mechanism | M / 1.5 | — | `NO_PLUS_ICONS` / `add_plus` / `build_marker()` / `compose_icon()` of `tools/gen_textures.py`, together with the bottom-right attachment, the composition branch, the call sites and the dry-run wording, are all deleted; the 26 entries of `ICONS` and `TEXTURE_SIZE = 16` are untouched |
| F-179 | Wild Apple pass-through, the size assertion released, and the image checked in | L / 3.0 | F-178 | `WILD_APPLE_SOURCE` points at 64×64, `draw_wild_apple()` passes straight through, `expected_size()` and the assertion at `:1153` are released **together** (A-32); the checked-in PNG is a byte-for-byte copy of the source image (64×64 / 6,946 B) |
| F-180 | Compressed Hay Block sound `GRASS` → `STONE` | S / 0.5 | — | one `SoundType` replaced at `registry/FarmstormBlocks.java:79`; all five sound categories change together; hardness 30 / blast resistance 6 / tool / colour not one character changed |
| F-181 | Outpost Core light level 15 | S / 0.5 | — | `.lightLevel(state -> 15)` appended to the end of the registration chain at `FarmstormBlocks.java:82-89`; the other block properties not one character changed, and it is not made into config |
| F-182 | The three keys + 1 language key + 2 comments of `pumpkin_turret` | M / 1.5 | — | `fireIntervalTicks` 60 → **40**, `projectileDamage` 1 → **3**, a new `bulletSpeed` (double, **0.45**, `0.0 ~ 4.0`); the "60 刻 = 3 秒" comment changed with them; the `farmlandBonus` comment gains "one key, two meanings" (RT-33); language keys +1 |
| F-183 | Delete `TurretBullet.SPEED` and change the two read points | M / 1.5 | F-182 | the constant at `TurretBullet.java:28` deleted; the muzzle speed of `steerTowards` and of `PumpkinTurret:75` **in the same commit** changed to read `pumpkinTurretBulletSpeed` (A-30); the convergence logic is not touched by this task |
| F-184 | Projectile point-blank convergence and the shared settlement body | L / 3.0 | F-183 | `steerTowards`'s three branches (`dist > step` as before / finish the remainder in one step / `≤ MIN_STEP` decided directly); `applyHit` moved as-is from `:98-119` + the `hitResolved` gate; **no soft tracking introduced** |

Phase subtotal: **7 tasks, 11.5 person-days** (S 2 / M 3 / L 2).

### 2.4 Exit criteria (observable)

- [ ] **One image (F-179)**. How to verify: read the PNG header, the byte count and the sha256, then delete the image and re-run the generator. What passing looks like: `textures/item/wild_apple.png` is **64×64**, **6,946 B**, and its sha256 equals the source image's; delete it and run `python -X utf8 tools/gen_textures.py` again, and the product is still 64×64, byte for byte identical to the source image, and **does not report "尺寸断言失败"**; two consecutive runs give the same sha256.
- [ ] **The other 24 untouched (F-178 / F-179)**. How to verify: store a sha256 list of the 24 before checking in, and compare them one by one after this section ends — **the committed bytes are the reference, and this section does not re-run those 24** (re-running is not a release action, A-61). What passing looks like: all 24 identical one by one; `grep -c "add_plus\|NO_PLUS_ICONS\|build_marker\|compose_icon"` = **0**; `--dry-run` plans **26** items and the output no longer contains "绿色「+」".
- [ ] **The five sound categories (F-180)**. How to verify: one positive and one negative control each for break / place / step / mine / land (`/playsound` playing `block.stone.*` and `block.grass.*`). What passing looks like: all five match `stone` and clearly differ from `grass`; mining it with the same pickaxe takes the same number of ticks as before; `git diff registry/FarmstormBlocks.java` shows line `:79` only.
- [ ] **One block of light (F-181)**. How to verify: place the core down and read `Light` on F3, look at the light patch at night, and put one mob-spawning platform at the very centre of the patch and one 15 blocks outside. What passing looks like: `Light = 15`; there is a light patch **within 15 blocks** and no light **16 blocks out** (once horizontally and once vertically); within 100 seconds the centre spawns **0** mobs and the outside spawns ≥ 1.
- [ ] **Three keys and two comments (F-182)**. How to verify: delete the old toml, start the server and read `[pumpkin_turret]`; then put the old toml back and start the server once more. What passing looks like: after deleting the old one there are exactly **5** keys, **40 / 3 / 0.45 / 15 / 1**; with the old toml kept, `fireIntervalTicks` is still 60, `projectileDamage` still 1, and **`bulletSpeed` is filled in automatically as 0.45**; `grep -c "60 刻 = 3 秒"` = **0** and `"40 刻 = 2 秒"` = **1**; the comment on `farmlandBonus` still yields the two characters 额度.
- [ ] **Provably the same source (F-183)**. How to verify: change `bulletSpeed` to `0.1` by hand, restart, and read the projectile's `Motion`. What passing looks like: `grep -rn "TurretBullet.SPEED" src/main/java` gives **0 hits**; `pumpkinTurretBulletSpeed` appears **≥ 1** in each of `TurretBullet.java` and `PumpkinTurret.java`; the magnitude of the three components of `Motion` is ≈ **0.1** (±0.01), and the instant of leaving the muzzle agrees with the per-tick displacement; the diff of `FarmstormEntityTypes` is empty.
- [ ] **Point-blank no longer overshoots (F-184)**. How to verify: have the target stand within 1 block and fire 5 shots in a row, then stand 3 blocks away and fire one group. What passing looks like: all 5 hit, the target loses **15** points, and no backwards displacement of the projectile is visible within 5 seconds; a single shot costs only **3** points, and `latest.log` holds exactly one damage record for the same shot; the trajectory shape at 3 blocks out is the same as before the change.
- [ ] **The A-40 window shrinks once in this section (the close-out is not here)**. How to verify: run the tool once and count only the difference lines of the §1 section. What passing looks like: after F-182 is committed the differences go **from 2 entries to 1** (only "`farmland.limitPerPlayer` extra" left), and the exit code is **still non-zero** — that is expected; do not go and change `docs/` or delete the `[farmland]` section.
- [ ] **The interval reading of the language keys**. How to verify: count the two lang files. What passing looks like: **316** keys each (`315 + the 1 of F-182`), and the Chinese and English key sets are still equal.

### 2.5 Pitfalls

1. **Deleting the mechanism but not the dry-run wording** (item 3 of F-178): `compose_icon()` is deleted, but `--dry-run` still prints "绿色「+」". What it turns into: the mechanism is gone and the output still describes it, so the next person reads the output and believes the mechanism is still there. Catching it early: search the whole line containing "+" in the `--dry-run` output.
2. **Taking "a re-run" for an acceptance action** (A-61): assuming that "delete the image and re-run once" also proves the other 24 are fine, or cleaning everything up in one go since the mechanism is gone. What it turns into: the green crosses of the 24 are wiped along with it — which is itself D-125's **expected outcome**, not a defect, but it collides head-on with the account "this version regenerates only 1, 25 − 1 = 24" (`28` §7 item 1) and the acceptance of B-38 loses its object; the release criterion is therefore "the committed bytes do not change". Catching it early: compare the sha256 list of the 24 one by one — **compare only, never re-run**.
3. **Releasing only half of the size assertion** (A-32's own words are "release together"): changing `expected_size()` but not the assertion at `:1153`, or the other way round. What it turns into: the 64×64 pass-through output hits `SystemExit` straight away, or the assertion passes while the source of the expected size is still the old function. Catching it early: delete the image and re-run once; the exit code must be 0.
4. **Writing "pass-through" as "run through the 16-cell colour picking once more"**: the product goes back to 16×16 or the colours get mode-quantised. What it turns into: the PNG header is not 64×64 and the byte count is not 6,946; the sha256 differs from the source image. Catching it early: read the three numbers together (size / bytes / sha256).
5. **Changing only one reader of `TurretBullet.SPEED`** (the original finding of A-30): changing only `steerTowards` and forgetting the muzzle speed at `PumpkinTurret:75`. What it turns into: it does not compile; and if both are changed but the semantics are only half changed (one of them cached at construction time), the muzzle and the per-tick displacement come from different sources. Catching it early: 0 hits for `grep -rn "TurretBullet.SPEED"` + ≥ 1 hit of the config key in each of the two files + the `Motion` magnitude matching after changing the value to `0.1` by hand.
6. **Putting `bulletSpeed` outside the group or opening a second group**: what it turns into: the group count 59 → 60, and F-192 in §4 can never reach "section lines 58" again; the tool reports one group prefix too many. Catching it early: `[pumpkin_turret]` has exactly 5 keys, and no second section name appears.
7. **Copying the value range wrong**: writing `0.0 ~ 1.0` or `0.0 ~ 10.0`. What it turns into: after changing the value to 5.0 by hand the loaded value is clamped to 1.0 or stays 5.0, red against the "inside the range again" assertion; the toml comment line is not `0.0 ~ 4.0` either. Catching it early: read the toml's comment line + change it by hand once and restart.
8. **Using "soft tracking" to cure the point-blank overshoot** (item 3 of D-129 forbids it outright): adding a turn-angle cap, a maximum pursuit time, guidance parameters. What it turns into: the trajectory shape at 3 blocks out changes, which collides head-on with the existing wording "direction reset every tick, speed magnitude constant". Catching it early: `git diff` may show only the three-branch convergence + the `applyHit` move + `hitResolved`, and not one guidance parameter more.
9. **Changing the turret's client prediction parameters along the way**: `clientTrackingRange(4)` / `updateInterval(10)`. What it turns into: the jitter of distant projectiles changes, and "the shape at 3 blocks out is the same" of F-184 loses its control. Catching it early: the diff of `FarmstormEntityTypes.java` is empty.

### 2.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the file + line numbers of each of the seven (`FarmstormBlocks.java` two lines, `FarmstormConfig.java` three keys and two comments, `TurretBullet.java` three stretches, `PumpkinTurret.java` one line, `gen_textures.py` three places, `wild_apple.png`).
- **Commands run**: the exit code of `.tools\gw.cmd build`; the exit codes and the "26 items" of two runs of `python -X utf8 tools/gen_textures.py` (including `--dry-run`); the number of difference lines and the key names of `python -X utf8 tools/verify_project.py`; the entry of `wild_apple.png` inside `jar tf`; the four `grep -c` runs (the `add_plus` group, `60 刻 = 3 秒`, `TurretBullet.SPEED`, `pumpkinTurretBulletSpeed`).
- **What was observed**: the size / bytes / sha256 of the PNG and the sha256 list of the 24; one line each for the positive and negative controls of the five sound categories; the `Light` reading on F3 and the three-layer observation of the light patch; the readings of `[pumpkin_turret]` in the two tomls (40 / 3 / 0.45 / 15 / 1; the old toml is 60 / 1 / 15 / 1 — it has no `bulletSpeed`, i.e. 4 keys); the `Motion` magnitude; the health loss of 5 hits out of 5 shots and the 3 points of a single shot; the key count of the two lang files (316 each).
- **Not done and not doable**: write "not verified" and give the reason (for example there is no second client to compare the sound against).
- **Open questions**: for example how a 64×64 texture looks at a distance, and the convergence behaviour of projectiles at chunk borders.

---

## 3. Phase 28 · The Friend Chives persistent field (F-185 ~ F-186)

> How to feed it: the full text of the master prompt of document 32 + the three lines of §0.2 + this section.

### 3.1 Preconditions

- Every exit criterion of §1 passing is enough; F-185 of this section **has no precondition at all** and is independent of the §2 / §4 lines. Skipping §2 does not affect this section.
- These four at hand: `30` §2.7 (the field itself, the existing hook of `keepAliveAfterImpact`, the `Settled` key), §7's #30 ~ #32; `28`'s D-131 / D-132 / D-133 and A-33; `31` Table A rows 16 ~ 18 and 42, Table E's RT-34 / RT-35; `29` §8.2's timing table.
- The current state of the keys: `friend_chives.effectTicks` defaults to **200** with a range of **20 ~ 1200**; `friend_chives.soundRange` defaults to **32.0** (`sounds.json` **does not add** `attenuation_distance` in this version, and the audible radius is carried by `max(音量, 1) × 16`).
- Frozen surface: `entity/ThrownCropEntity.java` is **read-only** (the base class's default of `keepAliveAfterImpact` must still be `false`, and the other subclasses are unaffected); no block entity is added (that would give `friend_chives` a `block_entity_type`, and the registry surface would no longer be +0); Q-Bounce's `tick` is not rewritten.
- This section adds no config key, no registry object and no language key.

### 3.2 Goal of this phase

Replace "a flash on landing" with "a field of `effectTicks` ticks after landing" (F-185), then plug its two known crash faces in one go: the entity is stored into the chunk NBT and, once loaded back, neither destroys itself nor ticks the field (F-186). The two must be done in order: the field comes first, and only then does "the field must not cross an unload" have a meaning.

What can be done at the end of this section: throw a Friend Chives and, inside the 6-block-radius sphere at the landing point, every creature is refreshed with Q-Bounce once per tick, the remaining duration counts down tick by tick, and the entity disappears on tick `effectTicks`; change `effectTicks` to 100 and test again, and the countdown stops at 100; an empty drop (0 creatures in the sphere) makes no sound, and a hit on ≥ 1 creature makes it exactly once; with the landing point inside flowing water the field's position does not move for 30 seconds; walk 200 blocks away and come back and the field does not exist.

### 3.3 Task list (F-185 ~ F-186)

| Task ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-185 | Friend Chives: from one-shot on landing to a field of `effectTicks` ticks | L / 3.0 | — | `keepAliveAfterImpact()` returns `true` + a new `tick()`; `onImpact` does only "read `fieldLength` once / apply batch 0 / play the sound once" + the settling trio (zero the velocity / no gravity / no collision); the duration of `applyField` is always derived from `fieldLength − elapsed` (A-33) |
| F-186 | End on chunk unload (on a save load the first tick of `Settled` kills itself) | M / 1.5 | F-185 | `readAdditionalSaveData` reads the base class's `Settled` into `loadedSettled`; the first thing `tick()` does after `super.tick()` is test it and `discard()`; `fieldTick` **is not written to NBT** |

Phase subtotal: **2 tasks, 4.5 person-days** (S 0 / M 1 / L 1).

### 3.4 Exit criteria (observable)

- [ ] **Timing (F-185)**. How to verify: stand a creature inside the sphere and read Q-Bounce's remaining duration tick by tick. What passing looks like: tick 1 **200**, tick 2 **199** … tick 199 **1**; on tick **200** `/data get entity @e[type=farmstorm:friend_chives]` finds nothing.
- [ ] **200 is not a constant (F-185 / A-33)**. How to verify: change `effectTicks` to **100**, restart and measure again; then change it to **1200** and watch it destroy itself on time with no accumulation. What passing looks like: the countdown stops at 100 and it disappears on tick **100**; the 1200 round shows no second field stacked on top.
- [ ] **The sound plays once only (F-185 / D-132)**. How to verify: one empty drop and one hit on ≥ 1 creature, reading `latest.log` for each. What passing looks like: the empty drop gives **0** lines of `farmstorm:friend_chives`; the hit gives **1** line for the whole field, with no repeat within 10 seconds.
- [ ] **Settling physics (F-185 / RT-35)**. How to verify: choose a landing point inside flowing water and then stand on it. What passing looks like: the entity's position does not change within 30 seconds (±0 blocks); standing on it does not push it; there is no gravity fall.
- [ ] **Unload ends it (F-186 / RT-35)**. How to verify: with the field present, walk 200 blocks away to force an unload and come back; then quit the world and re-enter. What passing looks like: after coming back the entity cannot be found **after the first tick**; re-entering the world does not revive it either.
- [ ] **The NBT face (F-186 / D-133)**. How to verify: read the chunk the entity is in with an NBT viewer. What passing looks like: the entity has only the base class's keys such as `Settled`, and **no** self-invented keys such as `fieldTick` / `FieldTick`.
- [ ] **Zero change to the base class (F-185)**. How to verify: `git diff entity/ThrownCropEntity.java` + two `grep` runs. What passing looks like: the diff is empty; the base class's default of `keepAliveAfterImpact` is still `false`; `grep -c '"Settled"' entity/thrown/FriendChivesProjectile.java` ≥ **1**.
- [ ] **The three numbers untouched**. How to verify: read the registry surface, the two lang files and `run/config`. What passing looks like: registry objects still **93** (`block_entity_type` still **7**); **316** keys in each of the two lang files; `run/config/farmstorm-server.toml` still **59** groups and **166** keys.

### 3.5 Pitfalls

1. **Writing the field into NBT to keep it alive**: storing `fieldTick` in `saveAdditional` and carrying on after a load. What it turns into: unload a chunk and come back and the field "revives", the cross-unload leak is exactly as before (the original symptom of RT-35), and F-186 is wasted. Catching it early: search the chunk NBT for `fieldTick` / `FieldTick`, 0 hits.
2. **A second 200 appearing inside `applyField`**: writing `200 - elapsed` or copying `fieldLength` out into a constant again. What it turns into: with `effectTicks` set to 100 it still runs 200 ticks, and the criterion of A-33 goes red outright. Catching it early: measure the timing once in each of the two rounds (200 / 100), and the tick of disappearance must equal the configured value in each round; only one source, `fieldLength`, is allowed in the code.
3. **Changing only the hook, not the settling physics**: `keepAliveAfterImpact` returns true but the velocity is not zeroed / gravity is not switched off / collisions are not exempted. What it turns into: after landing the field is carried off by flowing water or pushed away by a piston, and its position changes within 30 seconds; visually "the field is drifting". Catching it early: the position inside flowing water is ±0 blocks; standing on it does not push it.
4. **Moving the sound criterion into `tick()`**: testing "creatures present, so play" once per tick. What it turns into: the sound plays 200 times over the whole field (or once every time someone enters or leaves), and the hit sound floods `latest.log`. Catching it early: the whole field has exactly 1 hit sound, and the empty drop has 0.
5. **Adding a block entity "for the field"**: giving `friend_chives` a `block_entity_type` to carry the timer. What it turns into: the registry surface 93 → 94, `block_entity_type` among the twelve categories goes from 7 to 8, and the readings and the tool of F-212 all go red. Catching it early: `grep -c "BlockEntityType"` gains nothing; every registry reading before F-212 reads only the existing numbers of the 12 categories.
6. **Changing the base class's default**: changing `ThrownCropEntity`'s `keepAliveAfterImpact` default to true. What it turns into: the other projectiles (Melon Slice Bomb, Puffed Wheat, the other seeds) do not disappear after landing, and the whole world is full of lingering entities. Catching it early: the `git diff` of that file is empty.
7. **The self-destruct test in the wrong place or starting from the wrong point**: testing it before `super.tick()`, or letting `fieldTick` start from 1. What it turns into: it disappears on tick 199, or it is still there on tick 200 (one tick off). Catching it early: measure all three points — tick 1 is 200, tick 199 is 1, tick 200 finds nothing.
8. **The load-time self-destruct catching the field inside the session**: hanging the `loadedSettled` test on "set as soon as it lands". What it turns into: it disappears the moment it is thrown, and no field ever forms. Catching it early: the 200-tick timing inside the session is normal, and it disappears only along the "unload → reload" path.

### 3.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the four places of `FriendChivesProjectile.java` (the hook, `onImpact`, `tick()`, the load branch) + the new method names; the evidence that `ThrownCropEntity.java` has an empty diff.
- **Commands run**: the exit code of `.tools\gw.cmd build`; the §1 section of `python -X utf8 tools/verify_project.py` (read only, not a criterion); the two `grep` runs (`"Settled"`, `200`); how the NBT viewer was read.
- **What was observed**: the tick-by-tick readings of both the 200 and the 100 rounds (ticks 1 / 2 / 199 / 200); the sound line counts of the empty drop and the hit; the position reading over 30 seconds in flowing water; the two query results before and after the unload; the key-name list of the chunk NBT.
- **Not done and not doable**: write "not verified" and give the reason (for example there is no second client for an auditory comparison).
- **Open questions**: for example the server-side tick time with 10 fields present at once (RT-34), and the interaction between the field and entity hitboxes.

---

## 4. Phase 29 · The farmland–core binding regime (F-187 ~ F-198)

> How to feed it: the full text of the master prompt of document 32 + the three lines of §0.2 + this section. **This is the longest section, and it is still fed in one go**: 4 of the 12 tasks can fork in parallel, but they must be laid out along the two serial chains of §4.1.

### 4.1 Preconditions

- Every exit criterion of §1 passes; §2 / §3 **are not preconditions**. Until this section is done, §5 must not open (the HUD overlay's sort key and the allied test are both in this section).
- These at hand: `30` §2.2 (the whole binding-table section: the three bodies, the two records, persistence, the backward-compatible read, the `setDirty` table, the lazy clean-up), §2.5 (the ticket, including the load face of `distance = 1`), §7's #16 ~ #22 and #36; `29` §3.4 / §3.6 / §3.10 and §11's B-15 ~ B-25; `28`'s D-134 ~ D-140, D-145 and A-39 / A-42 / A-43 / A-45 / A-47; `31` Table A rows 19 ~ 35 and 39, Table B.2, Table C rows 1 ~ 4, Table E's RT-32 / RT-33 / RT-39 / RT-40.
- Two fixtures must be ready first: ① the 0.1.0 `world/data/farmstorm_farmland.dat` (only `owners`, no `dataVersion`); ② a save with 20 old farmland plots + 1 core (capacity 16, for the hard-cap criterion of F-190). An NBT viewer at hand.
- The current state of the keys: the `[farmland]` section is still there (59 groups), `farmland.limitPerPlayer` is still read by `base()`, `outpost_core.farmlandBonus` defaults to **16**, `outpost_core.maxPerPlayer` defaults to **2**.
- Two serial chains that must not be broken up (**A-56**: the declared dependency of F-192 is **F-191** — `base()` is the only reader of `farmland.limitPerPlayer`, a matter of **compile order**, not of style order): **`F-191 → F-192 → F-197`** (`base()` first has no reader → only then may the config group go → only then may the command tree go whole); **`F-193 → F-194 → F-196`** (core selection → binding write → migration all read the same `Decision.core`). The two chains may run in parallel; the three tasks with no precondition, `F-187` / `F-188` / `F-189`, land first, and only then may `F-190` / `F-191` / `F-193` / `F-195` / `F-198` open.
- Frozen surface: the signature and call sites of `util/DeployedOwnership.java`, `isOwnerOrAlly` (kept as "a definition only, no caller"), the three-state structure of `Verdict` and the language key name of `QUOTA_FULL`, and the 12-category self-check numbers of `Farmstorm.java`.

### 4.2 Goal of this phase

This version's biggest structural replacement: replacing "inside the range means managed" with "explicit binding", and doing in one go the six chains it drags along (the allied test, the save, the backfill, the index comparison key, the quota, the migration), the two deletions (the config group, the server-side command tree) and the one addition (the chunk ticket).

What can be done at the end of this section: till inside the core's 3×3 chunks with a hoe, and every block is bound to the **nearest** allied core; the 17th block is refused with the message "耕地额度已满 16/16"; after the core owner goes offline his farmland still cannot be trampled, and farmland with no binding turns to dirt as usual; removing a core first migrates its farmland to a successor core and turns it to dirt only when it cannot migrate (**destroying a core remotely behaves exactly like doing it up close — the farmland turns to dirt**); starting from a 0.1.0 legacy save prints "耕地补建：绑定 N 块 / 保持未绑定 M 块", and two runs over the same save give N / M identical to the character; `/farmstorm` disappears entirely; `farmstorm-server.toml` has no `[farmland]` section, section lines **58**, keys **166**; the tool outputs `结论：FAIL 0 项` for the **first** time (A-40 closes here); with nobody around the core still ticks, and `chunks.dat` cannot find it.

### 4.3 Task list (F-187 ~ F-198)

| Task ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-187 | The `Owner × Owner` overload of `TeamResolver` | M / 1.5 | — | a new overload `isAlly(ServerLevel, Owner, Owner)`, with the `Player` version degenerating into a one-line delegation; `DeployedOwnership.isAlly` untouched; `untamedFallback` semantically equivalent item by item |
| F-188 | `FarmlandLedger` v2: the binding table, `orphans`, the backward-compatible read and the write entry points | L / 3.0 | — | the three bodies (`bindings` / the derived `byCore`, not persisted / `orphans`, persisted) + the two records + `dataVersion: 2` + v1 read wholly into `orphans` + `bind` / `unbind` / `migrate` / `bindingsOf` / `orphanCount` / `rebuildByCore`; the lazy clean-up **prunes `byCore` too** |
| F-189 | `OutpostCoreIndex.Entry.gameTime` and the three read-only accessors | M / 1.5 | — | `record Entry(dimension, pos, long gameTime)` persisted; a legacy save missing the value takes `Long.MIN_VALUE` and bumps a counter; `entriesOf` / `owners` / `count(server, uuid)` added; the comparison path calls neither `getChunk` nor `getBlockEntity` |
| F-190 | The startup backfill | M / 1.5 | F-188, F-189 | a one-shot startup gate + sorting by `BlockPos#compareTo` (y → z → x) as a **single-level total order** and then processing item by item (A-47); the predicate is the same one as the migration's; the whole stretch has no `setBlock`; one INFO line at the end carrying N / M / T |
| F-191 | Quota / used change meaning, the `base` trio deleted | M / 1.5 | F-187, F-188, F-189 | `capacity = farmlandBonus × the number of allied cores`, `used` = the number of entries in the binding table under allied names, `orphans` not counted; `baseOverride` / `base()` / `setBase()` / `personalCapacity` and the base parameter of `sumOverMembers` deleted; the command side temporarily becomes a read-only echo to keep it compiling |
| F-192 | Delete config's whole `farmland` group and 1 language key | S / 0.5 | F-191 | the field, the whole group, the getter and the language-key generation stretch deleted; after deleting the old toml and regenerating, section lines **58** and keys **166**; this is the close-out point of A-40 |
| F-193 | `FarmlandGuard.check` core selection + per-core free slot | L / 3.0 | F-187, F-188, F-189 | a new `record Decision(Verdict, @Nullable CoreKey)`; the candidate set is **filtered first and the nearest taken second**; the three-level key `placedGameTime` → dimension id → `BlockPos#compareTo`; the meaning of `QUOTA_FULL` becomes "the nearest allied core is full" |
| F-194 | Write the binding when tilling succeeds | M / 1.5 | F-193 | the `ALLOW` branch of the tilling gate changes `ledger.add(...)` into `ledger.bind(owner, coreKey, pos)`; within the same tick the binding write + the accounting + the farmland placement finish; when refused not one binding is produced |
| F-195 | The offline test of farmland protection and the trample face | M / 1.5 | F-187, F-188 | `protectsFromTrample` follows "a binding means protected" (A-45); `inOwnRange` reads the binding table + `isAlly(core owner, player)`; `getPlayerList().getPlayers()` 0 hits |
| F-196 | Core destroyed: migrate first, three degrading branches | L / 3.0 | F-188, F-189, F-195 | only `bindingsOf(dead)` is handled; the **two exits plus one defensive branch** are re-bind / `setBlock(DIRT)` + detach the binding, with "detach the binding only" as the defensive branch (A-60, unreachable in normal operation); the set is computed completely before the world is touched; no chunk is loaded for the settlement; no notification, no drops, no compensation |
| F-197 | Delete the whole server-side command tree of `/farmstorm farmlandlimit` | M / 1.5 | F-191 | the whole file deleted + 3 registrations deleted + 1 language key; no empty shell kept, and the `/farmstorm` root node is not registered; the client-side `hud` subtree untouched |
| F-198 | A runtime temporary ticket for the chunk the core is in | M / 1.5 | F-189 | an own `TicketType` (`distance = 1`) + three idempotent releases + re-ticketing from the index at startup; **only a bare `TicketType`** — `setChunkForced` / `forceChunk` are both rejected (A-38); the wording follows the tick face (A-43) |

Phase subtotal: **12 tasks, 21.5 person-days** (S 1 / M 8 / L 3).

### 4.4 Exit criteria (observable)

- [ ] **The persisted shape of a binding (F-188 / F-194)**. How to verify: hoe 1 block, run `/save-all`, then read `farmstorm_farmland.dat` with an NBT viewer. What passing looks like: it contains `dataVersion: 2` and **1** entry under `bindings`, with the keys `pos` / `owner` / `coreDimension` / `corePos`, `coreDimension = "minecraft:overworld"`; there is **no** `byCore` key; start the server once more on the same save and "耕地绑定指纹（1 条）" is non-empty and the fingerprint is the same.
- [ ] **The quota and the 17th block (F-191 / F-193)**. How to verify: keep tilling under one core up to the 17th block; then change `farmlandBonus` to **2**, restart and measure again. What passing looks like: the **17th** block is refused with the message "耕地额度已满 16/16", and that core still has only **16** entries under it; after changing it to 2 the **3rd** block is refused straight away and the team capacity becomes 2 accordingly; when refused, `ItemDamage` is the same before and after.
- [ ] **The three-level key is reproducible (F-189 / F-193)**. How to verify: two cores 40 blocks apart → equidistant → change both `gameTime` values to the same value offline, and hoe one block at each; then start the server twice in a row. What passing looks like: the bindings go to "the nearer one", then "the earlier `gameTime`", then "the one earlier in coordinate dictionary order"; the two lines "核心比较键指纹" and "耕地绑定指纹" of the two startups are completely identical.
- [ ] **The protection face (F-195 / A-45)**. How to verify: after the core owner goes offline, have a player who is not a same-team ally jump-trample; use an unbound plot of farmland at the same position as the control; have a zombie jump-trample bound farmland; trample once more after switching teams. What passing looks like: the first two are respectively "still `minecraft:farmland`" and "turned to dirt"; the zombie cannot trample it; after the team switch the trample face is unchanged while the quota reading is recomputed for the new team (evidence for both lines side by side).
- [ ] **The two levels of core removal (F-196)**. How to verify: ① A has 3 blocks and B covers 2 of them, remove A; ② no successor core and the player standing beside the farmland, remove A; ③ again no successor core, but first walk 200 blocks away, then remove A through `/execute in`. What passing looks like: ① **a successor exists → rebind**: those 2 blocks are re-bound to B and the 3rd turns into `minecraft:dirt`, and B has ≤ **16** under it; ② **no successor → it turns into `minecraft:dirt`** with **no drops**; ③ **near and far are the same**: destroying the core remotely also turns it into `minecraft:dirt` with no drops, and the log does **not** show "迁移失败且区块未加载：只摘绑定" — that string is confirmed by code review to exist at `OutpostCoreBlockEntity.java:374` and **does not appear in normal operation**.
- [ ] **The backfill is reproducible (F-190 / A-47)**. How to verify: run twice in a row on a 0.1.0 legacy save; swap the two `orphans` records in the `.dat` and run once more; then build 20 old farmland plots + 1 core. What passing looks like: the log line "耕地补建：绑定 N 块 / 保持未绑定 M 块（老档共 T 块，按 `BlockPos` 全序）", with **N and M completely identical** across the two runs, and with the coordinate set and N / M unchanged after the swap; with 20 plots and 1 core, **N = 16, M = 4**; every plot that cannot be filled in passes `/execute if block … minecraft:farmland` one by one, and **0** turn to dirt.
- [ ] **The two deletions (F-192 / F-197)**. How to verify: delete the old toml, start the server and read the section lines and key count; type `/farmstorm` in the console; `grep` three places. What passing looks like: the `[farmland]` section does not exist, section lines (`^\[`) **58**, keys **166**, `pumpkin_turret` still 5 keys; both `/farmstorm` and `/farmstorm farmlandlimit 5` fail; `grep -c "farmlandlimit"` gives **0** in both lang files and in `ModLangProvider`; `grep -rn "FarmstormCommands"` gives **0 hits**.
- [ ] **A-40 closes here**. How to verify: delete the old toml and run the tool once after the regeneration. What passing looks like: `结论：FAIL 0 项`, exit code **0**, the §1 section **166**. This is the **first** criterion in this file that may cite a conclusion of the tool; only from this criterion onwards may `FAIL 0` be written into an acceptance record.
- [ ] **The tick face of the ticket (F-198 / A-43)**. How to verify: walk 200 blocks away to force a local unload and read the rounds of boundary particles; walk away again after removing the core; read the three dimensions' `chunks.dat`; stop the server and start it again. What passing looks like: the core's particles keep growing every **40** ticks; after removing the core F3 shows that chunk as **not loaded**; the `Forced` array does **not** contain that core's `ChunkPos.toLong()`; the tickets are rebuilt after a restart; `grep -c "setChunkForced|forceChunk"` = **0**; one screenshot each of the three chunk states (the centre ticking / the immediately adjacent ring loaded / beyond that not loaded), and **the wording "only 1 chunk is loaded" must not be used**.
- [ ] **The interval reading of the language keys**. How to verify: count the two lang files. What passing looks like: **314** keys each (`316 − 2`, that is, F-192 and F-197 delete 1 each), and the Chinese and English key sets are still equal.

### 4.5 Pitfalls

1. **Ghost farmland holding capacity** (`30` §9 item 3): the lazy clean-up prunes only from `bindings` and not from the derived `byCore`. What it turns into: only 15 blocks are left under the core, yet it keeps saying "16/16" and the player believes the quota is broken. Catching it early: `/setblock air` one already-bound coordinate and trigger the lazy check once — the entry counts of `bindings` and of `byCore` drop by 1 **at the same time** (the log's "绑定指纹（M 条）" and "反查集合 N 条" lines both change at once).
2. **The backfill order is not reproducible** (A-47 rev.2): processing item by item in the persistence order of `orphans`, or in the iteration order of `byOwner` (a `HashMap`). What it turns into: two startups over the same save give different N / M, and `30` §7 #19 goes red outright. Catching it early: run twice in a row + swap the two `orphans` in the `.dat` and run once more; the three sets of readings together must agree.
3. **The old toml is not deleted and the config does not take effect** (F-192): assuming that the new defaults take effect once the code is changed. What it turns into: the `[farmland]` section lingers, section lines are still 59, and `bulletSpeed` cannot be filled in either; the tool reports STALE on the leftover section, which gets misread as "the tool was changed wrong". Catching it early: after deleting `run/config/farmstorm-server.toml` and regenerating it, section lines 58 / keys 166; a leftover is a **known recoverable intermediate state**, not a defect.
4. **Taking the nearest first and testing the free slot second** (item 4 of F-193): the candidate set was not filtered first. What it turns into: "the nearest is full, there is a free slot elsewhere" is wrongly refused, which contradicts the total quota of `16 × the number of cores`. Catching it early: the control of `29` B-17 (nearest full, another core with a free slot → bound to the one with a free slot).
5. **A `setBlock` appearing inside the backfill** (item 3 of F-190): to make the "cannot be filled in" ones look tidy, shovelling them into dirt along the way. What it turns into: after the legacy save starts up a patch of farmland turns to dirt, and M is always 0. Catching it early: `grep -c "setBlock" farmland/FarmlandLedger.java` = **0**; the ones that cannot be filled in all pass `/execute if block` block by block.
6. **Loading chunks for the settlement** (A-35 / A-60): calling `getChunk` (instead of `getChunkNow`) block by block after `bindingsOf` to judge the block, or writing a separate branch for the non-existent "not loaded" case. What it turns into: removing a core force-loads a cascade of chunks (a stutter), whereas the correct behaviour is `getChunkNow` throughout — the ticket already guarantees the chunk of the farmland under a core is in memory, so destroying the core 200 blocks away still turns the field to dirt. Catching it early: `grep -c "getChunk" blockentity/OutpostCoreBlockEntity.java` gains no new read point; the block reading of branch ③ is `minecraft:dirt` and no "detach the binding only" log appears.
7. **The quota still summed over online members** (F-191): `capacity` keeps going through `getPlayerList()`. What it turns into: after a teammate goes offline A's quota shrinks (the bug from before the change is kept as it was). Catching it early: A and B on the same team with 1 core each, and after B goes offline A's quota is still **32**; `grep -c "baseOverride|personalCapacity|sumOverMembers"` = **0**.
8. **Changing the language key name of `QUOTA_FULL`** (`30` §9 item 4): changing the key along the way because the meaning seemed to have changed. What it turns into: the two lang files lose one and gain one, and the 323 account is off before §6. Catching it early: both `message.farmstorm.farmland.quota_full` and `.no_core` are still there, and only the text content changes.
9. **Leaving an empty shell of the command tree** (F-197): the subcommand is deleted but the `/farmstorm` root node is still registered. What it turns into: the console does not report "unknown command", and the player typing `/farmstorm` gets an empty list. Catching it early: type `/farmstorm` once on the server side and once in single-player; `FarmstormCommands` 0 hits.
10. **Writing the ticket as `setChunkForced` / `forceChunk`** (A-38): taking the vanilla forced load because it is less trouble. What it turns into: the core's coordinates show up in the `Forced` array of `chunks.dat`, it is force-loaded forever, and removing the core does not release it either. Catching it early: `grep -c "setChunkForced|forceChunk"` = **0**; check the three dimensions' `chunks.dat` one by one.
11. **Writing A-43 as "only 1 chunk is loaded"**: the documents and the acceptance would both write the one line that must go red. Catching it early: write only the tick face (the centre at level 32 / the immediate neighbour 33 / beyond that not loaded) and take one screenshot of each of the three layers.

### 4.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the file of each of the 12 (including the deleted `command/FarmstormCommands.java`) and the new class (`outpost/CoreChunkTickets.java`); the line numbers of the three deleted registrations.
- **Commands run**: the exit code of `.tools\gw.cmd build`; the **complete output** and the exit code of `python -X utf8 tools/verify_project.py` (this section is the first that may cite `FAIL 0`, and the verbatim text must be given); `/farmstorm` once on each side; six `grep -c` runs (`setBlock`, `getPlayerList().getPlayers()`, the `baseOverride` group, `farmlandlimit`, `FarmstormCommands`, `setChunkForced|forceChunk`).
- **What was observed**: the NBT keys and entry counts of `bindings` / `orphans`; the N / M of the two startups and the two fingerprint lines; the verbatim wording of the two refusals, the 17th block and `farmlandBonus = 2`; the block readings and log lines of the three core-removal branches; the **verbatim text** of the backfill log line; section lines 58 / keys 166; the particle count of the ticket and the three chunk states.
- **Not done and not doable**: write "not verified" and give the reason (for example there is no second client).
- **Open questions**: for example A-49's cross-dimension same-coordinate collision of farmland (a known limitation, not fixed in this version, registered only), and the drift face of `byCore` and `bindings` under an extreme interruption.

---

## 5. Phase 30 · The HUD overlay and the data face (F-199 ~ F-205)

> How to feed it: the full text of the master prompt of document 32 + the three lines of §0.2 + this section.

### 5.1 Preconditions

- **F-187, F-188 and F-189** of §4 are committed: the `isAlly(ServerLevel, Owner, Owner)` overload, `FarmlandLedger` v2, `OutpostCoreIndex.Entry.gameTime` and the three read-only accessors. Miss any one of them and this section can only draw an empty list.
- These at hand: `30` §2.1 (payload / codec / registration / the client snapshot / the three-level ordering / the debounce / the 10 moments), §2.4 (the four HUD overlay preconditions, the line format, `10n + 3`, the colours, the drag, the client command), §4.2 (the 3 client keys), §7's #6 ~ #15 and #29, the six to-be-verified APIs ① ② ⑤ ⑥ ⑦ ⑧ of §8; `28`'s D-142 ~ D-144 and D-146; `31` Table B.3 (the six components that do not enter the registry surface), Table C rows 5 ~ 7, Table E's RT-31.
- Environment: a single-player client and a dedicated server can be started at the same time; there is **no** `farmstorm-client.toml` under `run/config` (it is first generated by this section).
- Four firsts: this is the repo's first custom network packet, its first CLIENT config, its first batch of `KeyMapping`s, its first client command. Each is estimated one notch up, and each requires **a minimal case written and made to work first, the real logic written second** (`30` §8's ① ② ⑤ ⑦ ⑧).
- Frozen surface: the registry surface is still **93** (none of the network packet / key bindings / CLIENT config / client command enters `BuiltInRegistries`); `farmstorm-server.toml` still has **166** keys / **58** groups and **no** `hud` section; `FarmstormConfig` gains nothing at all.

### 5.2 Goal of this phase

Finish the only "red-line-breaking" requirement of this version: 1 S2C read-only packet + one self-drawn HUD overlay + 2 key bindings + 3 keys of CLIENT config + 1 client command. The data face (F-199) → the broadcast (F-200) → the rendering (F-203) is this version's longest serial chain; CLIENT config (F-201) and the key bindings / drag / command (F-202 / F-204 / F-205) hang off its two sides.

What can be done at the end of this section: a panel appears in the top-left corner, one allied core per line, shaped like `#1 [overworld] (x, y, z) 自己`; with 9 cores it is 8 core lines + 1 line of `+1`, and the panel is 93 px high; **H** toggles it and the setting persists, **J** enters the drag, `ESC` saves the position, and all three actions of `/farmstorm hud on|off|reset` are written into `config/farmstorm-client.toml`; a server switch leaves nothing behind and enemy cores are never sent out; the repo has exactly 1 custom packet, the client sends nothing back, and a dedicated server starts normally.

### 5.3 Task list (F-199 ~ F-205)

| Task ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-199 | 1 S2C read-only packet: payload / codec / registration / snapshot | L / 3.0 | — | the payload of `farmstorm:core_list` (`List<Core>` + an `int total` sent separately) + the 4-element `StreamCodec` + `MAX_CORES = 64` + the `playToClient` registration + the client handler (only one `enqueueWork` statement) + a `volatile` snapshot |
| F-200 | The broadcaster: assembly / three-level ordering / debounce / the 10 moments | L / 3.0 | F-199, F-189, F-187 | `CoreListBroadcaster`: candidates sorted by `gameTime` → dimension id → `BlockPos#compareTo` (the comparison key is **read from the index only**); `MIN_INTERVAL_TICKS = 10` debounce; `RECONCILE_INTERVAL_TICKS = 100` reconciliation; the 10 moments hooked one by one; three records cleared on logout |
| F-201 | CLIENT config 3 keys + 3 language keys | M / 1.5 | — | a new `FarmstormClientConfig` (`ModConfig.Type.CLIENT`) with the three `[hud]` keys `enabled = true` / `x = 4` (0 ~ 3840) / `y = 4` (0 ~ 2160); writing to disk goes through `ConfigValue#set` + `SPEC.save()`; language keys +3 |
| F-202 | 2 `KeyMapping`s + 2 description keys | M / 1.5 | F-201 | `FarmstormKeys`: toggle **H** / drag **J**, reusing `KeyMapping.CATEGORY_GAMEPLAY` as the category (no category of its own); consumed with `consumeClick()` inside the existing `ClientTickEvent.Post`; language keys +2 |
| F-203 | HUD overlay rendering + 1 "self" marker key | L / 3.0 | F-199, F-200, F-201 | `CoreHudRenderer`: the four preconditions for an early return (including "draw nothing with 0 cores"), the line format `#%d [%s] (%d, %d, %d) %s`, the panel height `10n + 3` (8 lines 83 / 9 lines 93), the width measured through `font.width`, clamping to the screen size before drawing, five colour schemes |
| F-204 | Drag positioning `CoreHudDragScreen` | M / 1.5 | F-203, F-202, F-201 | `isPauseScreen()` returns `true`; `mouseClicked` drags only inside the panel, `mouseDragged` writes the preview and clamps it, `mouseReleased` does not write to disk, and only `onClose()` writes config; the edit-state exception makes the `hideGui` / `screen != null` early returns fail |
| F-205 | The client command `/farmstorm hud` + 3 feedback keys | M / 1.5 | F-201 | `RegisterClientCommandsEvent` registers `hud <on\|off\|reset>`, and the three subcommands carry **no** `requires`; `reset` only resets the position and does not touch `enabled`; the feedback is `sendSuccess(..., false)` (displayed locally, not broadcast); language keys +3 |

Phase subtotal: **7 tasks, 15.0 person-days** (S 0 / M 4 / L 3).

### 5.4 Exit criteria (observable)

- [ ] **One packet, nothing sent back (F-199)**. How to verify: `grep` two places + start a dedicated server. What passing looks like: `grep -rln "CustomPacketPayload" src/main/java` gives exactly **1** file; `grep -rn "playToServer|sendToServer"` gives **0 hits**; the dedicated server finishes starting with no `NoClassDefFoundError`; the registration line in `latest.log` contains `farmstorm:core_list`; `grep -c "MAX_CORES"` ≥ **2**.
- [ ] **Dispatch and ordering (F-200)**. How to verify: 1 core in single-player; place A first (t=100) and then B (t=200); restart twice on the same save; place and remove cores in a chain within the same tick; quit server X and go to Y. What passing looks like: the log line "核心列表下发：<玩家> 1 条（total=1）"; A comes before B; the two "核心比较键指纹" lines of the two restarts are completely identical; the chain produces only **1** packet; on Y the HUD overlay **disappears immediately** and comes back on X.
- [ ] **Cross-dimension and a full panel (F-200 / F-203)**. How to verify: 1 core in the overworld + 1 core in the Nether; then construct 9 allied cores. What passing looks like: 2 lines whose line starts are `overworld` / `the_nether`, unchanged after a teleport; with 9 cores **8 core lines + 1 line of `+1` = 9 lines**, panel height **93 px**, and with 8 cores 8 lines, **83 px**, no `+N`.
- [ ] **One new toml (F-201)**. How to verify: start the client for the first time and read `config/farmstorm-client.toml`; then start a dedicated server and see whether it got generated. What passing looks like: `[hud]` has exactly **3** keys with the values `true` / `4` / `4`; the server **does not generate** this file; `farmstorm-server.toml` has no `hud` section; after changing `x = 5000` by hand and restarting, the loaded value is clamped to **3840**.
- [ ] **The two key bindings (F-202)**. How to verify: look at the two bindings in the key-binding screen; press H three times; press J once. What passing looks like: the "游戏操作" (Controls) page shows both, default **H / J**; `enabled` flips between true / false and the setting survives a restart; J opens a Screen and the single-player world **pauses**; `grep -c "key.categories.farmstorm"` = **0**.
- [ ] **The drag (F-204)**. How to verify: drag it to the bottom-right corner and then `ESC`; drag it off-screen and release (once for each of the four corners); change the GUI scale while dragging. What passing looks like: `hud.x` / `hud.y` equal the drop coordinates and are unchanged on re-entering the game; the out-of-bounds part is **0 px** and the panel is fully visible; it re-clamps after a scale change; closing it **does not pop** the pause menu.
- [ ] **One client command (F-205)**. How to verify: `/farmstorm hud off|on|reset` once each; look at the completion; look at a second client. What passing looks like: the three actions write `enabled = false` / `true` / `x = y = 4` respectively, with `enabled` untouched by `reset`; completing `/farmstorm ` offers only `hud`, and under it only `on` / `off` / `reset`; `/farmstorm farmlandlimit 5` fails; the feedback appears only in the local chat column.
- [ ] **The three numbers untouched (the entry in this section easiest to break along the way)**. How to verify: read the registry surface, `farmstorm-server.toml` and the two lang files. What passing looks like: registry objects still **93**; server config still **166** keys / **58** groups; **323** keys in each of the two lang files (`314 + 3 + 2 + 1 + 3`), and `farmstorm.configuration.` exactly **169** (`166 + 3`).

### 5.5 Pitfalls

1. **Using `CustomPacketPayload.createType("core_list")` for the `Type`**: not going through `Farmstorm.id` because it is less trouble. What it turns into: the packet lands in the `minecraft:` namespace, the client receives a different id, and the HUD overlay is forever empty. Catching it early: the registration line must be `farmstorm:core_list`.
2. **Writing `total` as `cores.size()`**: `+N` is always 0 once truncation happens. What it turns into: with 9 cores only 8 lines are drawn and `+N` becomes `+0` or is not drawn at all, and the 93 px criterion of A-34 goes red. Catching it early: construct 9 cores and look at `+1`; `total` must be the total **before** truncation, sent separately.
3. **The handler referencing a client class directly**: writing `ClientCoreList.set(...)` into the method body instead of into a lambda. What it turns into: `NoClassDefFoundError` when the dedicated server starts, and the pure server will not come up. Catching it early: start the dedicated server once; the lambda of `enqueueWork` is the only entry point.
4. **Stuffing CLIENT config into `FarmstormConfig`**: what it turns into: `farmstorm-server.toml` gains 3 keys (166 → 169) and the tool's §1 section goes red at once; moreover this config would be loaded as server config on a dedicated server, and the position readings and the toggle would not take effect on the client at all. Catching it early: `[hud]` lives only in `farmstorm-client.toml`, and `ModConfig.Type.CLIENT` is the only correct type.
5. **Giving `KeyMapping` a category of its own**: one more `key.categories.*`. What it turns into: the 323 account of the language keys no longer adds up (324), and the key-binding screen gains a category. Catching it early: the category must be `KeyMapping.CATEGORY_GAMEPLAY`; `grep -c "key.categories.farmstorm"` = 0.
6. **Using a language key for `+N`**: thinking "+1" ought to be translatable. What it turns into: 1 key more, and the 10 additions of `30` §4.3 become 11. Catching it early: `"+" + (total − 8)` is pure number concatenation, and the language keys are only those 10.
7. **The early-return conditions blocking the drag state too**: the `hideGui` and `screen != null` conditions take effect while dragging as well. What it turns into: after pressing J the player cannot see what he is dragging. Catching it early: after J the panel is still drawn (the edit-state exception).
8. **Writing to disk on `mouseReleased`**: or `ESC` opening the pause menu along the way. What it turns into: releasing halfway through the drag writes it to disk, and the position disagrees with the final preview; after `ESC` a pause menu is layered on top. Catching it early: nothing is written to disk mid-drag, and no menu pops after closing.
9. **`isPauseScreen()` returning false**: what it turns into: while dragging in a single-player world the world keeps ticking, mobs keep attacking and crops keep growing, and the position reading no longer matches the moment of the drag. Catching it early: after J the screen is still and the world does not tick.
10. **Using `sendSuccess(..., true)` for the feedback**: the same spelling as the deleted server-side command. What it turns into: the feedback is broadcast to the whole server and the second client's chat column fills up with it. Catching it early: `grep -c "sendSuccess"` = **3** and the second argument is `false` in every one.
11. **Adding `requires(...)` to the client command**: what it turns into: not one subcommand shows up in single-player completion. Catching it early: completion reads the three items `on` / `off` / `reset`.

### 5.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the 8 new files named one by one (three under `network/`, five under `client/`) and the two registration points (the line after `Farmstorm.java:56`, and `FarmstormClient.java`); the 9 additions of `datagen/ModLangProvider.java`.
- **Commands run**: the exit code of `.tools\gw.cmd build`; the complete output of `python -X utf8 tools/verify_project.py`; the two `grep -rln` / `grep -rn` runs; three `grep -c` runs (`MAX_CORES`, `key.categories.farmstorm`, `sendSuccess`); the registration line of the dedicated server's startup log.
- **What was observed**: the four sets of screenshots for 1 line / 2 lines / 8 lines / 9 lines of the HUD overlay and the pixel height of the panel; "核心列表下发" and the two fingerprint lines in the log; the three-key readings and the clamping reading of `farmstorm-client.toml`; the write-to-disk readings of the three H / J actions; the four corners of the drag and the two scale groups; two frames before and after the server switch.
- **Not done and not doable**: write "not verified" and give the reason (for example the second client is unavailable, the two to-be-verified APIs `②` / `⑥` could not be confirmed).
- **Open questions**: for example the measured single-packet size of `MAX_CORES = 64` (`30` §8 ⑥), and whether `LoggingOut` fires along the "kicked out" path (⑧).

---

## 6. Phase 31 · Close-out and full acceptance (F-206 ~ F-212)

> How to feed it: the full text of the master prompt of document 32 + the three lines of §0.2 + this section.

### 6.1 Preconditions

- Every exit criterion of §2 / §4 / §5 passes; **the A-40 window is closed** (F-192 has landed, and the old `farmstorm-server.toml` has been deleted and regenerated once). From this section onwards `结论：FAIL 0 项` may be written into an acceptance record — this is the boundary between A-40 and item 3 of `31` §0.4.
- These at hand: the **38 entries** of `30` §7 (#1 ~ #38), the item-by-item language-key list of §4.3, the 8 to-be-verified APIs of §8; the **39 entries** of `29` §11 (B-01 ~ B-39); `28`'s D-124 / D-147 and §4.5 (the three places the README has to change); the seven tables (A ~ G) of `31` §6 and the 20 self-checks of §7.
- Directories and artifacts: `.work/v011-verify/` is writable (screenshots / screen recordings / sha256 lists all land here); the `dist/` directory exists; `build/libs` may hold old jars at this moment, and before this section ends it must be cleared down to the two 0.1.1 artifacts only.
- Of this section's seven tasks (F-206 ~ F-212), **F-206 and F-207 change files** (language-key comments and artifacts), and **F-208 ~ F-212 change no code at all** (they only produce records). "Change no code" is the constraint in this section easiest to break: when an acceptance item fails, go back and redo the corresponding task — **do not change the criterion on the spot, and do not change the implementation to make the record green**.
- Frozen surface: for the language keys, **only the class comments may change, and no key may be added or deleted** (the adding and deleting was already done in §2 / §4 / §5); the 38 entries of `30` §7 and the 39 entries of `29` §11 **must not be rewritten** (A-46: a boundary ≠ an acceptance entry, and the two sets each stand on their own).

### 6.2 Goal of this phase

Close out three things in one go: the language-key ledger (F-206, `315 − 2 + 10 = 323`), the release artifact (F-207), and the two criteria sets (`30` §7's 38 entries + `29` §11's 39 entries = **77 entries**, run in four batches). F-212 is the only convergence point: the 77 entries + the 37 tasks + the seven tables + the five counts must all have a conclusion.

What can be done at the end of this section: drop the jar into a dedicated server and a player who installs nothing but the mod itself can join; the Chinese UI has no missing glyphs (the HUD overlay, the quota hint and the client-command feedback are all there); `dist/farmstorm-0.1.1+mc1.21.1.jar` can be released; the author holds a summary record of 77 conclusion lines and 37 task-status lines, each of which can be pointed to a log line, a command return value or a file path.

### 6.3 Task list (F-206 ~ F-212)

| Task ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-206 | The language-key ledger 323 × 2 and the `ModLangProvider` comments | M / 1.5 | F-182, F-192, F-197, F-201, F-202, F-203, F-205 | review category by category `configuration` **169** / `gui.farmstorm` **9** / `command.farmstorm` **3** / `key.farmstorm` **2**; change only the key-count narrative in the class comments (the "299 + 16 = 315" kind) and **add and delete no key**; the two key sets identical to the character |
| F-207 | Build artifacts and the release of `dist/` | S / 0.5 | F-176, F-179, F-198, F-199, F-203, F-206 | the two jars from `build/libs` → `dist/`; the `jar tf` entries match item by item (1 replaced PNG, no added PNG / JSON, 1 entry each for the three new classes); `version="0.1.1"` in `neoforge.mods.toml`; `chunks.dat` does not contain the core's coordinates |
| F-208 | Acceptance of `30` §7's #1 – #19 | M / 1.5 | F-182, F-188, F-189, F-190, F-191, F-192, F-193, F-194, F-197, F-199, F-200, F-201, F-202, F-203, F-204, F-205, F-206 | 19 entries recorded one by one in four columns (action / expected / measured / conclusion); no code changed |
| F-209 | Acceptance of `30` §7's #20 – #38 | M / 1.5 | F-177, F-179, F-180, F-181, F-182, F-183, F-184, F-185, F-186, F-192, F-196, F-198, F-199, F-200, F-203 | 19 entries recorded one by one in four columns; #36 written along the tick face (A-43), #38 quoting the tool's four sections verbatim; no code changed |
| F-210 | Re-run of `29` §11's B-01 – B-20 boundaries | M / 1.5 | F-189, F-191, F-193, F-194, F-195, F-196, F-199, F-200, F-201, F-203, F-204 | 20 entries matched one to one (the number must appear in the acceptance entry), of which **14** have no corresponding entry in `30` §7; the purely visual ones come with a screenshot (`.work/v011-verify/B-xx.png`); A-49 is listed in one line at the end only, with no pass item |
| F-211 | Re-run of `29` §11's B-21 – B-39 boundaries | M / 1.5 | F-178, F-179, F-180, F-181, F-182, F-184, F-185, F-186, F-188, F-190, F-196 | 19 entries matched one to one, of which **7** have no corresponding entry in `30` §7; the visual / auditory ones come with a screenshot or a screen recording; B-24 / B-25 need the "20 old farmland plots + 1 core" save prepared before running |
| F-212 | Full summary and the upstream-consistency sign-off | L / 3.0 | F-208, F-209, F-210, F-211 | 77 conclusions + 37 task states + the five counts (93 / 166 / 3 / 323 × 2 / 37) + the coverage counts of the seven tables + the three red-line counts |

Phase subtotal: **7 tasks, 11.0 person-days** (S 1 / M 5 / L 1).

### 6.4 Exit criteria (observable)

- [ ] **Both counts at 323 (F-206)**. How to verify: count the keys of the two lang files and compare the key sets; run the tool and read the §3 section. What passing looks like: **323** keys each and the key sets identical to the character; `grep -c '"farmstorm.configuration.'` exactly **169**; `grep -c farmlandlimit` **0** in both; the prefix distribution adds up line by line to 323; `grep -c "299 + 16 = 315"` = **0**.
- [ ] **The release artifact (F-207)**. How to verify: `jar tf` + `neoforge.mods.toml` + starting a pure server. What passing looks like: `dist/farmstorm-0.1.1+mc1.21.1.jar` and `-sources.jar` in place; `assets/farmstorm/textures/item/wild_apple.png` **1** entry, with no added PNG entries in this version; `farmstorm/network/CoreListPayload.class`, `farmstorm/client/CoreHudRenderer.class` and `farmstorm/outpost/CoreChunkTickets.class` **1** entry each; `version="0.1.1"`; no leftover 0.1.0 / 0.0.1 in `build/libs`; no `NoClassDefFoundError` on the dedicated server.
- [ ] **The first half of the 38 (F-208)**. How to verify: run #1 ~ #19 of `30` §7 one by one. What passing looks like: all **19** have a measured reading and an observation position, with **0** failing; #1's startup log contains the twelve categories to the character; #5 is 323 / 169 / `farmlandlimit` 0; #19's N and M are the same across two startups.
- [ ] **The second half of the 38 (F-209)**. How to verify: run #20 ~ #38 one by one. What passing looks like: all **19** have a reading; #22 turns into `minecraft:dirt` (the same as up close) with no drops, and the log does **not** show "迁移失败且区块未加载：只摘绑定" (that string is at `OutpostCoreBlockEntity.java:374`, does not appear in normal operation, confirmed by code review); both rounds of #30 (200 / 100) hold; #33's `Light = 15`; #36's `chunks.dat` does not contain the core's coordinates; #38 is the tool's four sections verbatim + exit code 0 + config section **166**.
- [ ] **The 39 boundaries (F-210 / F-211)**. How to verify: do the control experiment for each entry of `29` §11. What passing looks like: **20 + 19 = 39** entries matched one to one, with each number appearing in an acceptance entry; of them **14 + 7 = 21** have no corresponding entry in `30` §7 (exactly the 21 numbers A-46 names one by one), and these two batches are their only carrier; the purely visual / auditory ones come with a screenshot or a screen recording; A-49 is listed in one line at the end of each of the two records, **with no pass item**.
- [ ] **The seven tables and the five counts (F-212)**. How to verify: read the summary record. What passing looks like: Table A **44/44** (+ 1 supplementary row), Table B **20** items, Table C **7** items, Table D **5** items, Table E **11/11**, Table F **8/8**, Table G **17** rows; the five counts read together: registry **93**, server config **166**, client config **3**, language keys **323 × 2**, tasks **37**; the 77 entries and the 37 tasks all have a conclusion.
- [ ] **The three red-line counts (F-212)**. How to verify: three `grep` runs. What passing looks like: `@Mixin|mixins.json` **0 hits**; `CustomPacketPayload` exactly **1** file; `GameRules.register` still **0 hits** (an engineering constraint that is kept).
- [ ] **The tool's four sections read together (A-40 is closed)**. How to verify: `python -X utf8 tools/verify_project.py`. What passing looks like: `93 / 93`, `166 个配置键与文档逐字一致`, `323 键`, `结论：FAIL 0 项`, exit code **0**. If one of the four sections is missing or the exit code is not 0, do not announce that it may be released.

### 6.5 Pitfalls

1. **An acceptance record that says only "passed"**: not one reading among the 77. What it turns into: everything looks green and the author has nowhere to start when reviewing; the summary of F-212 then amounts to copying "passed" 77 times. Catching it early: four columns per entry, and every cell of the "measured" column must be one of ticks / blocks / keys / coordinates / a log line.
2. **Using the 38 in place of the 39, or the other way round** (A-46): running only `30` §7, or only `29` §11. What it turns into: the `38 + 39 = 77` account no longer adds up; not one of the 21 boundaries without a corresponding assertion (B-01, B-02, B-03, B-05, B-08, B-10, B-11, B-13, B-14, B-15, B-17, B-18, B-19, B-20, B-21, B-24, B-25, B-26, B-31, B-32, B-35) is verified. Catching it early: report the two batches' counts separately, and keep every number visible in the record.
3. **Writing A-49 as a pass item or as a defect**: what it turns into: a 40th entry appears among the 39, or "fix the cross-dimension farmland collision" shows up in the record. Catching it early: it appears in one line at the end of the record only, stating "known limitation, not fixed in this version".
4. **Changing code to make the acceptance green**: seeing #30's timing off by one tick and going off to tune `effectTicks`, or seeing B-23's farmland unprotected and going off to change `backfill`. What it turns into: the implementation is changed and the criterion changed with it, and all three acceptance rounds are void. Catching it early: the `git status` of the three acceptance batches may show only `.work/v011-verify/` and `dist/`.
5. **An old jar in `dist/`, or a jar without its `-sources.jar`**: what it turns into: the version line of `neoforge.mods.toml` is not 0.1.1, and the behaviour of the package the author holds does not match. Catching it early: read the version line through `jar tf`; `build/libs` may hold the two 0.1.1 artifacts only.
6. **F-206 adding or deleting a key along the way**: adding one more `gui.*` "to round 323 out". What it turns into: 324, and `configuration` is no longer 169. Catching it early: the `git diff datagen/ModLangProvider.java` shows no new `.translation(...)` and no deleted generation stretch, only comment changes.
7. **Taking an intermediate quantity for the total** (`31` Table G's G-8 / A-48): writing 165 (after the deleted key, before `bulletSpeed`) or 321 (missing two key-binding description keys). What it turns into: the language-key count in the summary does not match this file, and the README and the Release notes go wrong with it. Catching it early: read 323 and 169 **together**, and make the `314 + 3 + 2 + 1 + 3 = 323` formula hold.
8. **Running F-209 first and making up F-208 afterwards**: the two batches' number ranges do not overlap, but #5 / #6 / #7 read the artifacts of F-206 / F-199 / F-203. What it turns into: the readings before and after disagree within the same record. Catching it early: go in number order (F-206 → F-207 → F-208 → F-209 → F-210 → F-211 → F-212).

### 6.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; F-206 changes only the comment stretch of `ModLangProvider.java`; F-207 changes only the artifacts under `dist/` and `build/libs` (not checked into the repo).
- **Commands run**: the exit code of `.tools\gw.cmd build`; the **four sections verbatim** and the exit code of `python -X utf8 tools/verify_project.py`; the three groups of `jar tf` entries; the three red-line `grep` runs; the `sha256sum` (or `Get-FileHash`) readings of the two jars in `dist`.
- **What was observed**: the key counts of the two lang files and the 15-prefix distribution (`169 + 36 + 32 + 26 + 16 + 13 + 9 + 6 + 4 + 3 + 2 + 2 + 3 + 1 + 1`); the 77-line conclusion table; the 37-line task-status table; the coverage counts of the seven tables; the five counts.
- **Not done and not doable**: write "not verified" and give the reason (for example one visual entry has no screen recording).
- **Open questions**: the one known-limitation line of A-49; and any item that only got as far as "looks right" and cannot produce a reading.

---

## 7. The closing prompt (feed it once after the last Phase)

> How to feed it: the full text of the master prompt of document 32 + this section (**no Phase subsection attached any more**). This section only checks and fixes; it adds no feature and changes no criterion.

**Who does the final acceptance**: the executor of F-212 (the same agent that ran the four batches in §6) produces the conclusions and the evidence; **the signature is the author's action**, done against the three criteria at the end of this section, looking only at readings and files and never at the agent's self-description. Any line where the agent writes "passed" but cannot produce an evidence location is handled as "not verified", and must not be counted as passing in the summary table either.

```text
Project wrap-up. Please do one pre-release self-check of farmstorm v0.1.1; only check and fix, add no feature, and change no acceptance criterion.

1. Taking the numbers (A-40 is closed, so this section may cite conclusions of the tool)
   Run python -X utf8 tools/verify_project.py; all four sections must hold at the same time:
   ① the registry section "已核 registry 对象: 93 / 93", with the 12 categories at 32 / 26 / 7 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1;
   ② the config section "166 个配置键与文档逐字一致", with no MISMATCH and no real_extra;
   ③ the lang section "lang 键集一致，共 323 键";
   ④ "结论：FAIL 0 项" and exit code 0.
   If any one section is red, stop and report first; do not announce that it may be released. If old expected values are still left in the tool (for example 59 groups, 165 keys, 321 keys), treat it as "not done".

2. The build and the artifact (F-207)
   Run .tools\gw.cmd build (do not call gradle directly); the exit code must be 0;
   copy the artifacts build/libs/farmstorm-0.1.1+mc1.21.1.jar and farmstorm-0.1.1+mc1.21.1-sources.jar into dist/;
   in jar tf dist/farmstorm-0.1.1+mc1.21.1.jar, the version of META-INF/neoforge.mods.toml must be "0.1.1" and the modId must be farmstorm;
   build/libs must hold no leftover 0.1.0 / 0.0.1; the one in dist must be the very same file as the one just built (compare byte count and sha256);
   record the byte count + SHA256 (first 8 and last 5 characters) of the two jars in dist, and write them into the README's version line and the Release notes.

3. What the jar must contain (0 added PNGs, 0 JSONs, 1 PNG replaced in place)
   jar tf, naming each one: assets/farmstorm/textures/item/wild_apple.png (64×64 / 6,946 B, this version's only in-place replacement);
   plus 1 class entry each for the three new classes: farmstorm/network/CoreListPayload.class, farmstorm/client/CoreHudRenderer.class,
   farmstorm/outpost/CoreChunkTickets.class;
   then confirm that the three "not added" kinds really are absent: no new PNG, no new JSON (models / block states / loot tables / recipes / advancements all 0), and sounds.json and the ogg untouched.
   If any one is missing, stop and report.

4. The 77 criteria + the 37 tasks + the seven tables
   Open docs/zh/30-TECH-0.1.1.md §7 and output a 38-row reconciliation table (number / conclusion / evidence location);
   open docs/zh/29-DESIGN-0.1.1.md §11 and output a 39-row reconciliation table (number / conclusion / evidence location);
   the two tables total 77 rows with 0 failing; each entry's "evidence location" must point to a log line, a command return value or a file path,
   and an entry that says only "passed" is handled as "not verified".
   Then open docs/zh/31-TASKS-0.1.1.md and output a 37-row task reconciliation table for F-176 ~ F-212 (task ID / title / achieved or not / evidence location).
   Finally review the seven tables of `31` §6 one by one: Table A 44/44 (+ 1 supplementary row), Table B 20 items, Table C 7 items, Table D 5 items, Table E 11/11, Table F 8/8, Table G 17 rows.

5. The five counts read together
   Registry 93, server config 166, client config 3, language keys 323 × 2, tasks 37.
   Write the five numbers in the same paragraph; if one is missing, stop; if any of them disagrees with the README's version line, fix the README first and only then announce that it may be released.

6. The red lines: scan three only
   grep -rn "@Mixin\|mixins.json" src/main/java gives 0 places;
   grep -rln "CustomPacketPayload" src/main/java gives exactly 1 file (network/CoreListPayload.java), and grep -rn "playToServer\|sendToServer" gives 0;
   grep -rn "GameRules.register" src/main/java gives 0 (an engineering constraint that is kept, and not one of the two red lines).
   Note: the network-packet red line has been abolished outright (D-146); stop writing "0 custom network packets" as a pass condition, and stop writing "this version allows 1".

7. The relation between the 12 documents and the README
   This version's 12 = 6 Chinese documents (28-DECISIONS / 29-DESIGN / 30-TECH / 31-TASKS / 32-PROMPT-MAIN / 33-PROMPT-TASKS)
   + 6 English ones (the same-numbered translations, translated together after the Chinese is frozen; docs/en now holds documents 28 ~ 33).
   The README is not counted among these 12: it is the repository's front page and index, and this version changes three places — ① the red-line paragraph becomes "only no Mixin is kept, the packet restriction has been abolished outright";
   ② the upgrade notes (two defaults of pumpkin_turret in an old toml do not take effect, /farmstorm farmlandlimit is gone, the first startup of a legacy save backfills);
   ③ the version table and the dist entries (the byte count and SHA256 of 0.1.1).
   Of the 12, document 28 is the single source of truth and document 31 is the only source of task numbers and acceptance ownership; neither this file (33) nor 32 recopies the criteria.

8. Report
   Output in the report format of the master prompt of document 32, and add four small tables: the resource inventory table, the five-count table, the 77-criterion table and the 37-row task reconciliation table.
   Do not paste complete logs (paste only the conclusion line and the first error of each command); anything that was not run or not verified is written as "not verified" and must not be written as "passed".

9. What must not be done
   Do not change any single criterion of 28 / 29 / 30 / 31; do not change docs/zh/00~32 or docs/en; do not change the implementation just to turn some line green;
   do not add any key / PNG / JSON / registry object; do not write A-49 (the cross-dimension same-coordinate farmland collision) as a pass item or as a defect.
```

The three criteria for the wrap-up (checked by human eyes, not taken from the agent's self-description):

1. **The four sections + the exit code**: `93 / 93`, config **166**, lang **323 键**, `结论：FAIL 0 项`, exit code 0 — all five hold at the same time; the tool holds no old expected values such as 59 / 165 / 321.
2. **77 = 38 + 39**: the two reconciliation tables have 38 and 39 rows respectively, each entry has a reading and 0 fail; A-49 appears once, as a known limitation only; the 37-row task reconciliation table has 0 "not achieved".
3. **The artifact can be re-checked**: `dist/farmstorm-0.1.1+mc1.21.1.jar` and `-sources.jar` are in place with their byte counts / SHA256 recorded; `jar tf` can count that 1 PNG and those 3 classes; a pure server can start. Miss any of the three and do not announce that it may be released.

---

## 8. Cross-Phase caveats (the ones that do not belong inside any single Phase)

**8 items** in total. Each states in so many words when it takes effect and what happens if it is broken.

1. **The intermediate-state window of A-40 (F-177 ~ F-192)**: from the moment F-177 lands, `tools/verify_project.py` **is necessarily red** against v0.1.1, and there is exactly and only one way for it to be red: two key-level differences (before F-182: `pumpkin_turret.bulletSpeed` missing, `farmland.limitPerPlayer` extra; F-182 ~ F-191: only `farmland.limitPerPlayer` extra). Inside the window there are **three prohibitions**: do not change `docs/` to make the expectation set line up, do not delete `run/config/farmstorm-server.toml`, and do not write `FAIL 0` into any acceptance record. The window closes only at **F-192** (after deleting the `[farmland]` section + deleting the old toml and regenerating it), and from that point on `FAIL 0` is a criterion. The exit criteria of §1 and the exit criteria of §6 are the two ends of this rule, and the in-section criteria of the four sections in between all steer around the tool.
2. **The order of the two schema changes (F-188 and F-189)**: neither has a precondition and they share no file (`farmland/FarmlandLedger.java` versus `outpost/OutpostCoreIndex.java`), and they land in two different `.dat` files (`farmstorm_farmland.dat` and `farmstorm_outpost_core.dat`), so **they may run in parallel and may also swap order**; but both must land before **F-190 (backfill), F-193 (core selection) and F-196 (migration)** — the backfill reads `orphans` (F-188's field) and breaks ties with `gameTime` (F-189's field), core selection needs the three-level key, and migration needs `bindingsOf`. **The two upgrades must not be merged into one commit**: their rollback faces differ (rolling back the v2 ledger loses the bindings but does not shovel the world; rolling back the index only degrades the three-level key to two levels), and once merged they cannot be rolled back separately.
3. **The HUD overlay line (from F-199) and the binding-regime line (from F-188) can run in parallel — conclusion: yes, but there are four junctions**. ① F-187 (the `isAlly` overload) is the common precondition of F-200 and of F-191 / F-193 / F-195, and must land first; ② F-189 (the `gameTime` carrier) is the common precondition of F-200 and of F-193 / F-196; ③ `blockentity/OutpostCoreBlockEntity.java:95-103` and `:166-170` are changed by F-198 (the ticket stretch) and by F-200 (the broadcast stretch) at the same time, so they must go serial under the agreement "one task only appends its own stretch", and the two sections must not start at once; ④ `event/FarmstormGameEvents.java` is changed by F-190 (the startup gate), F-194 (the binding write), F-197 (deleting 3 registrations), F-198 (server stop + re-ticketing) and F-200 (+4 player events + `ServerTickEvent.Post`), and the five additions must not have any one of them overwritten by another. Apart from these four points, the two lines can be handed to two people separately.
4. **Three same-file serial stretches (across Phases)**: `tools/gen_textures.py` (F-178 → F-179), `entity/TurretBullet.java` (F-183 → F-184), `datagen/ModLangProvider.java` (the seven increments of F-182 / F-192 / F-197 / F-201 / F-202 / F-203 / F-205, with **the final consistency closed only at F-206**). The language-key one needs particular care: the key count seen at the end of any section is only an interval reading (315 → 316 → 315 → 314 → 317 → 319 → 320 → 323), and 323 **must not** be written into a criterion halfway through.
5. **Three pairs of rollback chains**: `F-182 → F-183 → F-184` (after the constant is deleted the two read points must come from the same source, and rolling back F-182 makes the latter two fail to compile); `F-185 → F-186` (rolling back F-185 leaves F-186 meaningless but does not fail to compile); `F-191 ↔ F-197` (`setBase` is already deleted, so rolling back F-197 must roll back F-191 at the same time, or it will not compile). All three pairs must say "roll back in pairs" in the commit message.
6. **Two numbers whose "total holds only at the end point"**: the language keys **323** and the config group count **58**. The intermediate quantities are 315 / 316 / 314 / 317 / 319 / 320 and 59 respectively; each of them holds only within one section and must not be cited across sections; the totals hold only after F-206 (language keys) and F-192 (group count). Conversely, **registry objects 93 and the server config key count 166 are unchanged all the way through** — 166's formula is `166 − 1 + 1`, the total stays level but the composition changes, so no section may write "166 never moved, so it needs no check".
7. **The 38 and the 39 cannot stand in for each other (A-46)**: the 38 entries of `30` §7 are technical self-checks and the 39 entries of `29` §11 are design boundaries; 13 match completely, 5 are partially covered, and **21 have no corresponding assertion at all** (B-01, B-02, B-03, B-05, B-08, B-10, B-11, B-13, B-14, B-15, B-17, B-18, B-19, B-20, B-21, B-24, B-25, B-26, B-31, B-32, B-35), their only criterion being F-210 / F-211 (F-210 carries **14** of them and F-211 carries **7**, 14 + 7 = 21). A further **11 acceptance entries have no corresponding boundary** (#1, #2, #3, #5, #6, #11, #13, #14, #16, #37, #38), and they belong to the engineering face. Both batches of records say "0 failing", but neither may sign for the other. **A-49 is not among the 39** and is registered as a known limitation only. There is one more hard scheduling constraint: the dependency columns of the four acceptance tasks have been recomputed by **A-54** and back-filled into document 31 (F-207 / F-208 / F-209 / F-210 / F-211 each run against the new column, and §6.3 of this file is already in sync), but the execution wording is still "only when all the exit criteria of §2 / §4 / §5 have been run does §6 open", not "open as soon as the dependency column allows".
8. **The red-line wording changed in three places in this version, and every section must be written to the new wording**: `CustomPacketPayload` goes from "0 hits" to "exactly 1 file" (0 before F-199, 1 after F-199, and the red-line scan of §6 is written as 1); `@Mixin` / `mixins.json` is still **0** (the only red line kept); `GameRules.register` is still **0** (an engineering constraint, not one of the two red lines). The in-section scans of §2 / §3 / §4 must no longer write "0 custom network packets" as a pass condition.

---

## 9. Consistency self-check against document 31's task list

| Phase | The task range of `31-TASKS` | The task count of `31-TASKS` | The task count of §x.3 of this file | Equal |
|---|---|---|---|---|
| 26 Baseline and tool chain | F-176 ~ F-177 | 2 | 2 | equal |
| 27 Textures, values and block properties | F-178 ~ F-184 | 7 | 7 | equal |
| 28 The Friend Chives persistent field | F-185 ~ F-186 | 2 | 2 | equal |
| 29 The farmland–core binding regime | F-187 ~ F-198 | 12 | 12 | equal |
| 30 The HUD overlay and the data face | F-199 ~ F-205 | 7 | 7 | equal |
| 31 Close-out and full acceptance | F-206 ~ F-212 | 7 | 7 | equal |
| **Total** | **F-176 ~ F-212** | **37** | **37** | **equal (the difference is empty, `212 − 176 + 1 = 37`)** |

The other four self-checks:

| Check item | Method | Conclusion |
|---|---|---|
| Task numbers and titles | extract the 37 `#### F-xx · title` entries from `31-TASKS-0.1.1.md` and compare them line by line against the "task number + title" of this file | 37 / 37 identical to the character (the numbers are consecutive, with no gaps and no duplicates) |
| Complexity and person-days | proofread S / M / L and the converted person-days phase by phase, then add them up against the subtotals of `31` §4.1 | `2.0 + 11.5 + 4.5 + 21.5 + 15.0 + 11.0 = 65.5`; S 5 / M 22 / L 10; with the 25 % buffer 81.875 |
| The depends-on column | compare line by line against the "depends on" field of each task block (against the new columns after the **A-54** / **A-56** back-fill) | 37 / 37 agree; five of them, F-207 / F-208 / F-209 / F-210 / F-211, are written to A-54's new columns; **F-192 depends on F-191 per A-56**; the two serial chains (`F-191 → F-192 → F-197`, `F-193 → F-194 → F-196`) are already named in §4.1, and the former rests on **compile order** (A-56), not on style order |
| Adjudication citations | this file cites numbers only (A-30 ~ A-56, D-124 ~ D-147, RT-31 ~ RT-41) and does not copy the adjudication text of `31` §5 | in line with the single-source convention; the entry that A-44 is void and superseded by A-47 is written to document 31's current state |

> Memo (A-56): "this entry was at first misread as 'F-192 has no dependency', which came from the three-pairs-into-three compressed spelling of the change list; it has been restored per A-56" — that list wrote the three pairs as `F-179` / `F-188` / `F-192` → `F-178` / `—` / `F-191`, where the `—` corresponds to the **F-188** pair (the original text "— (does not depend on F-187)"), and **the F-192 pair** is "`F-191` (`base()` has no reader any more)" → **`F-191`**; in document 31 F-192's dependency has been `F-191` from beginning to end, so do not change it back to `—` following the compressed spelling.

**Register of five wording inconsistencies** (this file handles them as "document 31's body prevails, no amplification, no renumbering"; all five **have already been back-filled into document 31 by A-54 and corrected**, and this file is in sync, see the status column):

| # | Location | The inconsistency (the text before A-54) | How this file handles it | Status |
|---|---|---|---|---|
| 1 | `31` §1 table vs §2 detail | F-208's dependency column is written two ways: §1 says "F-179, F-180 ~ F-190, F-192, F-196" (including F-187), while §2's item-by-item list **does not include** F-187; neither includes F-191 / F-193 / F-194 (and #16 ~ #18 are exactly the products of those three tasks), and neither includes F-199 ~ F-205 of §5 (and #6 ~ #15 are the HUD overlay stretch) | §6.3 of this file copies the dependencies of the §1 table verbatim and adds no number; the gap and the handling wording are written at the end of §8 item 7 and in this section | **fixed (A-54)** |
| 2 | The dependencies of `31`'s F-207 / F-209 / F-211 | Item 4 of F-207's acceptance has to read `chunks.dat` (which is F-198's face), yet the dependency column has only F-206; F-209 covers #20 ~ #38, but its dependency column does not contain F-179 (#23), F-180 (#24 ~ #28) or F-181 (#33); F-211 covers B-21 ~ B-39, but its dependency column does not contain F-182 (B-28) or F-190 (B-23 / B-24 / B-25) | same as above: copied verbatim, not changed; the gaps are named at the end of §8 item 7, and the execution wording is "run §2 / §4 / §5 completely first, then open §6" | **fixed (A-54)** |
| 3 | The body of `31`'s F-210 and F-211 | The two places say "**12** of them have no corresponding entry in `30` §7" and "**6** of them" respectively, which add up to **18**; but A-46 and Table G row 17 list **21**, and the three missing ones are B-19, B-20 (in F-210's stretch) and B-21 (in F-211's stretch), which neither body names. A-54 has changed them to "**14** of them" and "**7** of them", totalling **21** (B-19 / B-20 / B-21 named as well) | this file is written to the **21** of A-46 / Table G (§8 item 7 names them one by one, including B-19 / B-20 / B-21), not to the 12 + 6 enumeration | **fixed (A-54)** |
| 4 | The ownership of `31` Table A rows 23 / 24 | `orphanCount` is in F-188's list of new methods, yet Table A row 23 records it under F-190; row 24 of `FarmlandGuard` records "F-193 (the test), F-194 (the write entry point)", while F-194's write entry point is actually at `event/FarmstormGameEvents.java:206` | this file is written to the task details (§4.3's F-188 contains `orphanCount`, and F-194's deliverable says "the `ALLOW` branch of the tilling gate"), not to Table A's rewrite of task ownership | **fixed (A-54)** |
| 5 | The title of `31`'s F-185 | The §1 table says "Friend Chives becomes a field of `effectTicks` ticks", while the §2 detail says "Friend Chives: from one-shot on landing to a field of `effectTicks` ticks" | the §2 detail's title is taken (it carries more information, and it agrees with this file's exit criterion "from one-shot to a persistent field"); the short version appears only once, in the quotation of this row, and the body of §3.3 uses the long one | **fixed (A-54)** |

> The five items above were back-filled into document 31 and corrected by **A-54**, and this file is in sync; the "how this file handles it" column keeps the original judgment, and the status is **fixed**.

Maintenance convention: if `31-TASKS-0.1.1.md` revises the title, the dependencies or the person-days of some task, this section and the six task tables of §1 ~ §6 have to re-run the comparison once; this file does not change titles on its own, does not change numbers on its own, does not add tasks on its own, and does not change the two numbers 65.5 person-days and the 15.5 person-day critical path.

---

## Translator's notes

1. **Program output, log lines and hint wordings stay in Chinese, character for character**, because they are literals the implementation must emit rather than prose: `耕地补建：绑定 N 块 / 保持未绑定 M 块（老档共 T 块，按 BlockPos 全序）`, `耕地额度已满 16/16`, `迁移失败且区块未加载：只摘绑定`, `核心列表下发：<玩家> 1 条（total=1）`, `核心比较键指纹`, `耕地绑定指纹（1 条）`, `绑定指纹（M 条）`, `反查集合 N 条`, `结论：FAIL 0 项`, `已核 registry 对象`, `166 个配置键与文档逐字一致`, `lang 键集一致，共 323 键`, `30-TECH §4.1：58 组 / 166 键`, `剔除已删键 1 个：farmland.limitPerPlayer`, `60 刻 = 3 秒`, `40 刻 = 2 秒`, `尺寸断言失败`, `绿色「+」`, `299 + 16 = 315`. Every `grep -c` pattern built on such a string is reproduced unchanged, because a translated pattern would no longer match the source.
2. **The HUD sample line keeps `自己`** (`#1 [overworld] (x, y, z) 自己`), because it is the rendered value of the "self" language key on the Chinese client; where the document speaks of the marker as a concept (F-203's "1 "self" marker key") the English word is used. §2.4's `额度` is kept for the same reason: the criterion is that the two Chinese characters are readable in the `farmlandBonus` comment.
3. **The two `text` blocks** (§0.2's three restated lines and §7's closing prompt) are prompt text meant to be fed to an agent; their prose is translated while every command, path, file name, count and identifier inside them is unchanged, and the fence style (` ```text `) is reproduced as in the source.
4. **A-43 is kept in the positive form only**: §4.4 requires one screenshot each of the three chunk states (the centre ticking / the immediately adjacent ring loaded / beyond that not loaded) and forbids the wording "only 1 chunk is loaded"; the English never uses that phrase as a description of the design.
5. **The tombstones remain legible**: A-40's intermediate-state window (§1.4, §2.4, §4.4, §8 item 1), A-44's "void, superseded by A-47" (§9) and A-47 rev.2 (§4.5) all keep their status words in English. The phase headings are translated, as in this round's documents 28 / 29 / 32, and each one keeps its number and its `(F-xxx ~ F-xxx)` range.
6. **Defects found in the Chinese source while translating have since been fixed upstream** (A-59 and the 31/33 correction batch): the "four vs five" count in §8 item 7, the "six vs seven strings" count in §1.1 / F-176 / §1.5 item 7, the five-key reading in §2.6, the binding-fingerprint literals, and the "`docs/en` reaches 27 only" line in §7 item 7. This translation follows the corrected Chinese source; the list is kept only as a record of what the cross-check caught.