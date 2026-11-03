# 31 · v0.1.1 Task Breakdown & Roadmap

> Project: Farmstorm · Version: **v0.1.1** (MC 1.21.1 / NeoForge 21.1.213 / Java 21)
> Basis: `docs/zh/28-DECISIONS-0.1.1.md` (D-124 ~ D-147, 24 entries, the single source of truth) + `docs/zh/29-DESIGN-0.1.1.md` (10 requirements R1 ~ R10, 39 boundaries B-01 ~ B-39) + `docs/zh/30-TECH-0.1.1.md` (§3 change surface 44 rows, §4.1 config 58 groups / 166 keys, §4.2 client 3 keys, §4.3 lang 323 keys, §7 acceptance 38 entries, §8 APIs to verify 8 entries, §9 differences 9 entries, RT-31 ~ RT-41) + `.work/v011-BRIEF.md` (the fact card) + adjudications **A-30 ~ A-41** (`.work/v011-verdicts.md`, 12 entries, reproduced verbatim by this file and landed on tasks item by item).
> This file breaks the spec into **37** tasks that can each be "committed and verified independently", numbered **F-176 ~ F-212**, with Phase **26 ~ 31** mapping one-to-one onto milestones **M22 ~ M27**, and the adjudication numbers continuing from A-41 with **A-42 ~ A-56** newly established (15 entries; of which **A-44 is void and superseded by A-47**), **27 entries (A-30 ~ A-56)** in total.
> Coverage: the **44 rows** of the change surface in document 30 §3 (+ 2 supplementary rows, A-42 / A-55), **+0 registered objects** (12 classes named one by one), **166 + 3** config, **1 PNG** (replaced in place, 0 added), **RT-31 ~ RT-41, eleven entries**, and the **8 entries** of APIs to verify of document 30 §8; 0 uncovered (table by table in §6).
> This file only reads upstream: it does not modify `docs/zh/00` ~ `30`, does not modify `docs/en`, does not modify `src/`, and does not modify the three ledgers of `.work/`; it only creates this file.

---

## 0. Reading Conventions

### 0.1 Task Granularity (Five Items)

One task = one change that can be committed and verified independently. The criteria follow the wording of `25-TASKS-0.1.0.md §0.1`, copied item by item and not rewritten:

1. **One task = one change that can be committed and verified independently.**
2. **After the commit, `.tools\gw.cmd build` exits with code 0**, and the task does not depend on a task of the same batch that is not yet committed.
3. **Each task carries at least one acceptance assertion observable at a location listed in §0.3**; the assertion must contain a concrete value, key name or path.
4. **Reverting that commit does not invalidate other completed tasks** (a revert only affects later tasks that depend on it, and the dependency is written in the "Depends on" field).
5. **No Phase boundary is crossed**: all deliverables of a task fall inside the name scope of the Phase it belongs to; a cross-Phase prerequisite is written only in the "Depends on" column.

One boundary **[author's supplement]** (corresponding to item 5 of document 25 §0.1): a task touches only the 44 rows of document 30 §3, 2 supplementary rows (A-42 / A-55), two schema changes (`FarmlandLedger` v2, `OutpostCoreIndex.Entry.gameTime`), 2 deletions (the `farmland` config group, the server-side command tree), language keys +10 / −2, 1 PNG, plus the 5 client classes + 1 payload + 1 ticket class new in this version. Any change outside the tables first amends document 28 and only then gets broken into tasks.

Three cross-task hard conventions (written here, not repeated in the tasks):

- **A-37**: no acceptance item **may** state a criterion that describes the state before the work starts, such as "the call-site count of some method = N" (e.g. "call sites of `FarmlandLedger.remove` = 1"). Once the binding regime introduces "unbind", the write entries are no longer unique, and such an assertion is red after implementation and cannot be told apart from a defect or from the intended state. The criterion is always replaced by something countable — **file count / key count / hit count / log line / save field**.
- **A-40**: before `tools/verify_project.py` is reworked in F-177, its "FAIL 0" is not a pass criterion. Tasks between F-177 and F-191 **never cite** that tool's conclusion; only tasks from F-192 on may cite it (F-192 is the point where the `[farmland]` section disappears and the expectation set first agrees with the toml).
- **A-42**: the 44 rows of document 30 §3 **omit** the action surface "delete the whole `farmland.limitPerPlayer` group" (it is in row 3 of document 30 §2.3 and in §4.1's deletion table, but not in the 44 rows). This file registers it as a **supplementary row** at the end of table A, the 44 rows are not renumbered, and the total action surface is counted as 45.

### 0.2 Complexity and Workload Conversion (S/M/L → 0.5/1.5/3.0)

The conversion basis is exactly that of `25-TASKS-0.1.0.md §0.2`, **unchanged by a single character**, and is not adjusted in this version:

| Complexity | Meaning | Converted person-days |
|---|---|---|
| S | Single file or pure data; ≤ 1 class, or ≤ 20 JSON files | **0.5** |
| M | 2 ~ 4 files, across 1 ~ 2 packages, or 1 new complete behavior implementation | **1.5** |
| L | ≥ 5 files or across 3 packages, including a runtime state machine / serialization / transaction / render pipeline | **3.0** |

Distributed under this rule, the 37 tasks of this version are **S 5 / M 22 / L 10**, converting to `5 × 0.5 + 22 × 1.5 + 10 × 3.0 = 2.5 + 33.0 + 30.0 = 65.5` person-days (buffer excluded).

Three notes on items **not estimated as "changing one line"** (the same discipline as the last paragraph of document 25 §0.2):

1. **Any schema / serialization change is counted one tier up**: F-188 (`FarmlandLedger` v2) counts as L at **3.0** person-days; F-189 (`OutpostCoreIndex.Entry.gameTime`) adds only one field and still counts as M at **1.5** person-days — its cost lies in the three points "a missing field in a legacy save takes `Long.MIN_VALUE`, no chunk is loaded for the comparison, two startups are reproducible", not in the field itself.
2. **Any "first time" is counted one tier up**: F-199 (the project's first custom packet), F-202 (the first `KeyMapping`), F-201 (the first CLIENT config), F-205 (the first client command) are all firsts for this repository and count as L / M rather than 0.5 for "adding one class".
3. **Any acceptance task is not estimated as "running it once"**: each of F-208 ~ F-211 covers 19 ~ 20 items, and each item needs at least one action + one reading + one record, so they count as M.

### 0.3 How Acceptance Criteria Are Written

Every acceptance item must be observable at one of the following locations:

1. the assertion output of `run/logs/latest.log` (hereafter `latest.log`; document 25 §0.3 writes it as `logs/latest.log` and means the same file);
2. the return value of `/execute` `/data get` `/setblock` `/give` `/effect` in game;
3. an F3 reading (`Light` / `Chunk` / the entity list);
4. the content of `config/*.toml` (`farmstorm-server.toml` / `farmstorm-client.toml`) or of `src/generated/resources`;
5. an artifact under `build/libs` and `dist` (including the entry list of `jar tf`);
6. a `grep` count (file count / hit count / key count);
7. a PNG byte comparison (dimensions + sha256).

**Forbidden**: unobservable phrasing such as "behaves normally", "feels good" or "meets expectations". Four writing conventions (carried over from document 25 §0.3):

1. An assertion must contain a concrete value, key name or file path (e.g. "`[pumpkin_turret]` has exactly 5 keys, `fireIntervalTicks = 40`", not "the config is right").
2. A manual-test assertion must state the **control scenarios** (e.g. "play `stone.break` and `grass.break` once each as the positive / negative control").
3. When citing an upstream self-check item, write its number (e.g. `document 30 §7 #17`) instead of restating the criterion.
4. A few assertions use structural evidence (`grep` counts, the scope of a `git diff`, `jar tf` entries, a PNG's sha256) — they point at definite file content, are as valid as the seven locations above, and are used only where those seven cannot decide.

In this file `grep` always means "a literal count over the named files", executed equivalently with `rg -c` on Windows, with the same result.

### 0.4 The One Boundary Specific to This Version: A-40's Timing Constraint

**A-40's conclusion**: `tools/verify_project.py:222-237` currently computes the expected config key set from four **old documents** (`01-DESIGN` / `12-TECH-0.0.2` / `18-TECH-0.0.3` / `24-TECH-0.1.0`) and compares it against `run/config/farmstorm-server.toml`; once this version deletes 1 key (`farmland.limitPerPlayer`) and adds 1 key (`pumpkin_turret.bulletSpeed`), the expectation set necessarily disagrees and the tool reports FAIL. **Until it is reworked per this version's wording, "the tool is all green" is unusable.**

Accordingly this file fixes three scheduling rules:

1. **The rework task is F-177, placed second in Phase 26**, earlier than any task that cites a tool conclusion.
2. **The intermediate-state window = F-177 landed ~ F-191 committed**. Inside the window the tool's §1 section is necessarily red, and **only two key-level differences may appear**: `pumpkin_turret.bulletSpeed` missing, `farmland.limitPerPlayer` extra. Those two differences are themselves the window's positive criterion (see acceptance item 3 of F-177): report one key name more or fewer, and the parser is written wrong. After F-182 lands (`bulletSpeed` enters the toml) the "missing" one disappears and only the "extra" one remains, until F-192 deletes the `[farmland]` section.
3. **No task inside the window cites a tool conclusion** (the second form of wording A-40 allows). The window closes at **F-192**: after the `[farmland]` section disappears and the old toml is deleted and regenerated, `结论：FAIL 0 项`, exit code 0 and a config section of `166` hold at the same time. From then on (F-192 ~ F-212) tasks may cite the tool.

> Why the rework goes first rather than last: the window exists however you schedule it (the old expectation set goes red the moment the first config change lands), and moving the parser forward makes "the two red ones" **known and assertable** from the first day; pushing the parser back means every acceptance in Phase 27 ~ 29 reads an expectation set computed wrongly.

---

## 1. Task Overview

Columns: task number / title / Phase / complexity / person-days / depends on / deliverables / decisions / covered change-surface row numbers (row numbers of document 30 §3, `补` = the supplementary row at the end of table A).

| Task | Title | Phase | Complexity | Person-days | Depends on | Deliverables | Decisions | Change-surface rows |
|---|---|---|---|---|---|---|---|---|
| F-176 | Version 0.1.1 and the jar names / banner of the two build scripts | 26 | S | 0.5 | — | `gradle.properties`, the two `.cmd` scripts | D-124, D-147 | #1, #2 |
| F-177 | `verify_project.py`'s config expectation source re-pointed at v0.1.1 | 26 | M | 1.5 | — | the tool gains 1 parser + 1 removal list | A-40 | #43 (#44 belongs to F-212) |
| F-178 | The generator drops the overlaid "+" mechanism | 27 | M | 1.5 | — | `tools/gen_textures.py` (5 places + 2 call sites) | D-125 | #4 |
| F-179 | Wild Apple passthrough, the size assertion relaxed and the image committed | 27 | L | 3.0 | F-178 | 4 places in the generator + `wild_apple.png` | D-126, A-32 | #3, #5, #6 |
| F-180 | Compressed Hay Block's sound `GRASS` → `STONE` | 27 | S | 0.5 | — | `registry/FarmstormBlocks.java:79` | D-130, A-31 | #7 |
| F-181 | Outpost Core light level 15 | 27 | S | 0.5 | — | `registry/FarmstormBlocks.java:82-89` | D-141 | #8 |
| F-182 | `pumpkin_turret`'s three keys + 1 language key + 2 comments | 27 | M | 1.5 | — | `FarmstormConfig.java`, `ModLangProvider.java` | D-127, D-128, D-129, RT-33 | #9, #10, #11 |
| F-183 | Delete `TurretBullet.SPEED` and change the two read points | 27 | M | 1.5 | F-182 | `TurretBullet.java:28`, `PumpkinTurret.java:75` | D-129, A-30 | #12, #15 |
| F-184 | The bullet's close-range convergence and the shared settlement body | 27 | L | 3.0 | F-183 | `TurretBullet.java:90-119` | D-129 | #13, #14 |
| F-185 | Friend Chives: from a one-shot on landing to a field lasting `effectTicks` ticks | 28 | L | 3.0 | — | `entity/thrown/FriendChivesProjectile.java` | D-131, D-132, D-133, A-33 | #16, #17, #42 |
| F-186 | End on chunk unload (self-destruct on the first tick after reading `Settled`) | 28 | M | 1.5 | F-185 | the load branch of the same file | D-133, RT-35 | #16 (load segment) |
| F-187 | `TeamResolver`'s `Owner × Owner` overload | 29 | M | 1.5 | — | `team/TeamResolver.java`, `team/ScoreboardTeamResolver.java` | D-138 | #31 |
| F-188 | `FarmlandLedger` v2: the binding table, `orphans`, the backward-compatible read and the write entries | 29 | L | 3.0 | — | `farmland/FarmlandLedger.java` | D-134, D-137, A-47 | #19, #22, #23 (schema 1) |
| F-189 | `OutpostCoreIndex.Entry.gameTime` and three read-only accessors | 29 | M | 1.5 | — | `outpost/OutpostCoreIndex.java` | A-39, D-142, D-138 | #32 (schema 2) |
| F-190 | The startup backfill `backfill` | 29 | M | 1.5 | F-188, F-189 | `FarmlandLedger` + the startup gate | D-137, A-47 | #23 (backfill segment) |
| F-191 | Quota / used change criterion, and the `base` trio deleted | 29 | M | 1.5 | F-187, F-188, F-189 | `FarmlandLedger` + the command's read-only echo | D-139, D-138 | #20, #21 |
| F-192 | Delete config's whole `farmland` group and 1 language key | 29 | S | 0.5 | F-191 | `FarmstormConfig.java`, `ModLangProvider.java` | D-139, A-42 | the supplementary row |
| F-193 | `FarmlandGuard.check`: core selection + a free slot on that core | 29 | L | 3.0 | F-187, F-188, F-189 | `farmland/FarmlandGuard.java:69-75` | D-135, A-39 | #24 |
| F-194 | Write the binding when tilling succeeds | 29 | M | 1.5 | F-193 | `event/FarmstormGameEvents.java:194-217` | D-135 | #24 (write entry) |
| F-195 | Farmland protection's offline judgment and the trample face | 29 | M | 1.5 | F-187, F-188 | `FarmlandGuard.java:92-107`, `:120-134` | D-138, A-45 | #25, #26 |
| F-196 | Core destroyed: migrate first, three downgrade branches | 29 | L | 3.0 | F-188, F-189, F-195 | `OutpostCoreBlockEntity.java:315-379` | D-136, A-35 | #27, #28 |
| F-197 | Delete the whole `/farmstorm farmlandlimit` server-side command tree | 29 | M | 1.5 | F-191 | 1 file deleted + 3 registrations + 1 language key | D-140, RT-40 | #33, #34 |
| F-198 | A runtime temporary ticket on the chunk the core is in | 29 | M | 1.5 | F-189 | `outpost/CoreChunkTickets.java` + 3 releases + ticket re-issue at startup | D-145, A-38 | #39, the ticket segments of #29 / #30 |
| F-199 | 1 S2C read-only packet: payload / codec / registration / snapshot | 30 | L | 3.0 | — | three classes in `network/` + `client/ClientCoreList` | D-142, RT-31 | #36 |
| F-200 | The broadcaster: assembly / three-level ordering / debounce / 10 timings | 30 | L | 3.0 | F-199, F-189, F-187 | `network/CoreListBroadcaster.java` + 6 event listeners | D-142, A-39, RT-31 | #35, the broadcast segments of #29 / #30 |
| F-201 | CLIENT config 3 keys + 3 language keys | 30 | M | 1.5 | — | `config/FarmstormClientConfig.java`, `Farmstorm.java:56` | D-144 | #37 |
| F-202 | 2 `KeyMapping`s + 2 description keys | 30 | M | 1.5 | F-201 | `client/FarmstormKeys.java` + the consumption points | D-144 | #38 (Keys segment) |
| F-203 | The overlay's rendering + 1 "self" marker key | 30 | L | 3.0 | F-199, F-200, F-201 | `client/CoreHudRenderer.java` | D-143, A-34, A-41 | #38 (Renderer segment) |
| F-204 | Drag positioning, `CoreHudDragScreen` | 30 | M | 1.5 | F-203, F-202, F-201 | `client/CoreHudDragScreen.java` + the edit-mode exception | D-144 | #38 (DragScreen segment) |
| F-205 | The client command `/farmstorm hud` + 3 feedback keys | 30 | M | 1.5 | F-201 | `client/CoreHudClientCommands.java` | D-144 | #38 (ClientCommands segment) |
| F-206 | The language-key ledger 323 × 2 and the `ModLangProvider` comment | 31 | M | 1.5 | F-182, F-192, F-197, F-201, F-202, F-203, F-205 | the two lang files + the datagen comment | document 30 §4.3, RT-40 | #40 |
| F-207 | The build artifact and the `dist/` release | 31 | S | 0.5 | F-176, F-179, F-198, F-199, F-203, F-206 | `dist/` + the `jar tf` list | D-124, D-147, A-38 | #2 (artifact face), #36 |
| F-208 | Document 30 §7's #1 – #19 acceptance | 31 | M | 1.5 | F-182, F-188, F-189, F-190, F-191, F-192, F-193, F-194, F-197, F-199, F-200, F-201, F-202, F-203, F-204, F-205, F-206 | the acceptance record (a table on disk) | document 30 §7 | — (acceptance face) |
| F-209 | Document 30 §7's #20 – #38 acceptance | 31 | M | 1.5 | F-177, F-179, F-180, F-181, F-182, F-183, F-184, F-185, F-186, F-192, F-196, F-198, F-199, F-200, F-203 | the acceptance record | document 30 §7, A-46 | — (acceptance face) |
| F-210 | Document 29 §11's B-01 – B-20 boundary re-run | 31 | M | 1.5 | F-189, F-191, F-193, F-194, F-195, F-196, F-199, F-200, F-201, F-203, F-204 | the boundary record | document 29 §11, A-46 | — (boundary face) |
| F-211 | Document 29 §11's B-21 – B-39 boundary re-run | 31 | M | 1.5 | F-178, F-179, F-180, F-181, F-182, F-184, F-185, F-186, F-188, F-190, F-196 | the boundary record | document 29 §11, A-46 | — (boundary face) |
| F-212 | The full summary and the upstream-consistency signature | 31 | L | 3.0 | F-208, F-209, F-210, F-211 | the summary record | all of D-124 ~ D-147 | #41, #44 |

Task count: `212 − 176 + 1 = 37`, contiguous with no gap; 6 Phases; 6 milestones.

---

## 2. Task Details

Grouped by Phase. Each task has exactly six items: **Goal / Files touched / Implementation notes / Depends on / Acceptance (at least 1, observable) / Rollback impact**; the complexity and person-days are written on the title line.

### Phase 26 · Baseline and Tool Chain (F-176 ~ F-177)

Phase goal: nail down the two "getting the numbers" things — the version number and the tool — first, so that every acceptance in the five phases after it has a trustworthy ruler.

#### F-176 · Version 0.1.1 and the jar names / banner of the two build scripts (S · 0.5)

- **Goal**: bring the version number and its three external carriers into line in one change — `gradle.properties`'s `mod_version`, the jar names hard-coded in the two `.cmd` scripts (including `-sources.jar`), and the banner version string at the head of the two `.cmd` scripts — so that the build artifact name, the scripts and the documents no longer drift apart from one another.
- **Files touched**: `gradle.properties:11`; `检查环境.cmd:5`, `:14`; `一键构建.cmd:5`, `:11-12`.
- **Implementation notes**:
  1. `mod_version=0.1.0` → `0.1.1`; the three lines `minecraft_version` / `neo_version` / Java 21 are not changed by a single character (document 30 §1.1).
  2. The two jar names are replaced verbatim: `farmstorm-0.1.1+mc1.21.1.jar` and `farmstorm-0.1.1+mc1.21.1-sources.jar`.
  3. The banner's `Farmstorm v0.0.1` → `Farmstorm v0.1.1` (**[author's supplement]**: the supplementary item of D-124 states that "the version number has only one source, `gradle.properties:11`, and other documents only cite it", whereas the banners of the two scripts are the fifth and sixth hard-coded version strings; changing only the jar names would leave the banner still declaring 0.0.1, conflicting with D-124's single source. This item is folded into this task and is not adjudicated separately).
  4. The remaining lines of the two scripts (`chcp 65001`, the `PY` path, `gw.cmd build`, `pause`, the `:fail` block) are not changed by a single character.
- **Depends on**: —
- **Acceptance**:
  1. `.tools\gw.cmd build` exits with code 0; `build/libs/farmstorm-0.1.1+mc1.21.1.jar` and `farmstorm-0.1.1+mc1.21.1-sources.jar` both exist; `build/libs` holds no leftover `farmstorm-0.1.0*` or `farmstorm-0.0.1*`.
  2. `jar tf build/libs/farmstorm-0.1.1+mc1.21.1.jar` contains `META-INF/neoforge.mods.toml`, whose `version` line is `0.1.1`.
  3. `grep -c "farmstorm-0.0.1"` is **0** in both scripts; `grep -c "farmstorm-0.1.1+mc1.21.1"` is **1** in `检查环境.cmd` and **2** in `一键构建.cmd`; the banner version strings of both scripts are `v0.1.1`.
  4. `git diff gradle.properties` contains one changed line only, line 11.
- **Rollback impact**: affects only the artifact name and the version strings. After a revert `build/libs` goes back to the 0.1.0 naming, invalidating no completed task (the jar name is not referenced by code; only the two scripts read it).

#### F-177 · `verify_project.py`'s config expectation source re-pointed at v0.1.1 (M · 1.5)

- **Goal**: let the tool compute its expected config key set from the 58 groups of document 30 §4.1, and drop from the union the `farmland.limitPerPlayer` this version deletes; only after that may this version's "the tool is all green" be cited (A-40).
- **Files touched**: `tools/verify_project.py` (a new `expected_config_keys_v011` after `expected_config_keys_v010` at `:89`; the union wired into `main()` starting at `:213`; one source line added to the docstring at `:2-12`).
- **Implementation notes**:
  1. The new function reads the 58-row table of `docs/zh/30-TECH-0.1.1.md` §4.1: it takes the "group" column (a nested group looks like `blast_carrot.nausea`) and the key name inside the backticks of the "key" column and concatenates them into `<group>.<key>`; annotations inside a row such as "（**改** 1 → 3）" are skipped naturally by the backtick parsing and need no extra regex branch.
  2. Self-check after parsing: group count = **58**, key count = **166**; if not, `fail` and print the actual count and the row number of the first non-conforming table row.
  3. `REMOVED_KEYS = {"farmland.limitPerPlayer"}`: subtracted once **next to** the union expression, not scattered over two places (otherwise the next person to delete a key will miss one).
  4. The expectation set is still the existing "union with duplicates removed" structure (`01-DESIGN` + `12-TECH` + `18-TECH` + `24-TECH` + `30-TECH` − `REMOVED`); **do not open a second hard-coded key list** (the degeneration this tool most fears).
  5. A parse failure must be locatable: the error carries the file name + table row number and is never skipped silently.
- **Depends on**: —
- **Acceptance**:
  1. `python -X utf8 tools/verify_project.py`'s §1 section prints "30-TECH §4.1：58 组 / 166 键" and "剔除已删键 1 个：farmland.limitPerPlayer".
  2. `git diff --stat docs/` is empty: the tool only reads the upstream documents and never writes them back.
  3. **Intermediate-state criterion (A-40)**: before F-182 is committed, the `[FAIL]` / `[STALE]` lines of the §1 section total **exactly two**, and the only key names are `pumpkin_turret.bulletSpeed` (missing) and `farmland.limitPerPlayer` (extra); after F-182 and before F-192 there is **exactly one** (only `farmland.limitPerPlayer` extra). A third key name fails this task (it is the only symptom of a parser written wrong).
  4. The failure path works: temporarily change one row of §4.1's "group" column (a local experiment, not committed) and run it — the exit code is not 0 and the error carries the file name and row number; after restoring, the exit code returns to 0.
  5. `grep -c "expected_config_keys_v011" tools/verify_project.py` ≥ **2** (definition + call).
- **Rollback impact**: after a revert the expectation set falls back to the union of the four old documents and the tool no longer holds for this version's 166 keys; F-208 / F-209 / F-212, the three tasks that cite a tool conclusion, are invalidated (they are scheduled after this task per A-40). No other task is invalidated.

### Phase 27 · Textures, Values and Block Properties (F-178 ~ F-184)

Phase goal: finish the seven "visible" and "tangible" items in one pass — one image, one generator, five sound types, one light level, three values, two read points, one convergence segment.

#### F-178 · The generator drops the overlaid "+" mechanism (M · 1.5)

- **Goal**: remove the rule "overlay a green `+` on the bottom-right corner of an item texture" from the generator altogether (D-125), so that re-running the generator no longer puts the `+` back; this version **does not regenerate** the other 24 images, whose bytes must not change.
- **Files touched**: `tools/gen_textures.py` (`:274-275` `NO_PLUS_ICONS`, `:407` `add_plus`, `:546-560` `build_marker()`, `:612-623` the bottom-right corner overlay, `:974-975` the composition branch, `:1127` and `:1141-1142` the call site and the dry-run text).
- **Implementation notes**:
  1. Delete the `NO_PLUS_ICONS` and `add_plus` fields: `Spec` no longer has the "overlay or not" dimension, and `compose()` no longer accepts a marker argument.
  2. Delete `build_marker()`, the bottom-right corner overlay (`left = 9 / top = 9`) and `compose_icon()`; `compose()` keeps only the one return path "source image → size normalisation".
  3. Delete the call at `:1127` and the dry-run text at `:1141-1142` (otherwise the dry-run still prints "+ 绿色「+」").
  4. The 26 entries of the `ICONS` table, `TEXTURE_SIZE = 16` and `make_placeholder()` are left alone (this task only deletes the mechanism and does not touch the output list).
- **Depends on**: —
- **Acceptance**:
  1. `grep -c "add_plus\|NO_PLUS_ICONS\|build_marker\|compose_icon" tools/gen_textures.py` = **0**.
  2. `python -X utf8 tools/gen_textures.py --dry-run` exits with code 0, plans **26** items, and no longer prints "绿色「+」".
  3. Delete the committed `wild_apple.png` and run the generator once: the produced image is **16×16** with **no** green cross at the bottom-right corner — the 33 px of `#3FA34D` disappears from the pixel statistics (this is D-125's mechanism-level acceptance; the 64×64 passthrough is in F-179).
  4. The sha256 of the other **24** PNGs is identical to the pre-commit value image by image (**this section does not re-run those 24**; the committed bytes are the reference; re-running is not a release action, A-61; `25 − 1 = 24` from D-125's supplementary item).
  5. `git status` shows only `tools/gen_textures.py` and the regenerated `wild_apple.png` changed.
- **Rollback impact**: a revert puts the overlay mechanism back, and the next re-run sticks the `+` onto every image; F-179's passthrough branch does not depend on this task (it changes only the one `wild_apple` path), so a revert does not invalidate F-179.

#### F-179 · Wild Apple passthrough, the size assertion relaxed and the image committed (L · 3.0)

- **Goal**: `textures/item/wild_apple.png` becomes the as-is output of the 64×64 source image; at the same time the three rollback points in the generator that would turn it back into 16×16 or make the build fail outright are changed in the same version (D-126 / A-32).
- **Files touched**: `tools/gen_textures.py:154` (the default source image), `:849-881` (`wild_apple_grid()`), `:884-895` (`draw_wild_apple()`), `:982-983` (`expected_size()`), `:1151-1154` (the size assertion); `assets/farmstorm/textures/item/wild_apple.png`.
- **Implementation notes**:
  1. `WILD_APPLE_SOURCE`'s default points at `wild_apple_64x64.png`; the environment-variable override entry (`:155`) is kept.
  2. `wild_apple` goes passthrough: `draw_wild_apple()` returns a copy of the source image directly and no longer enters `wild_apple_grid()`'s 16-cell modal colour picking; if `wild_apple_grid()` has no caller left, delete it (no dead code).
  3. `expected_size()`: for `wild_apple` it returns the source image's actual size (`(64, 64)`), for the GUI it still returns `GUI_SIZE`, and for the other specs it still returns `(TEXTURE_SIZE, TEXTURE_SIZE)`; the assertion at `:1153` is **kept and not deleted** (A-32's own words are "the assertion itself stays, only the expected size is relaxed").
  4. The committed PNG is a byte-for-byte copy of the source image: no resizing, no quantisation, no alpha change.
  5. The model `models/item/wild_apple.json`, the item id, the `ICONS` table's entries and their order, the language keys and the registry surface are all left alone (D-126's supplementary item).
- **Depends on**: F-178
- **Acceptance**:
  1. `textures/item/wild_apple.png` is **64×64**, **6,946 B**, and its sha256 equals the source image's.
  2. Delete the committed image and run `python -X utf8 tools/gen_textures.py`: the output is 64×64, byte-for-byte identical to the source image, and reports no "size assertion failed" (`expected_size()` and `:1153` have been relaxed together per A-32).
  3. Two consecutive runs produce the same sha256; the sha256 of the other 24 PNGs is taken from the **committed bytes** and does not change (**this section does not re-run those 24**; re-running is not a release action, A-61).
  4. `grep -c "wild_apple_grid" tools/gen_textures.py` = **0**; in the dry-run the expected sizes of `wild_apple` and of some GUI spec print on two different lines.
  5. In the in-game inventory the Wild Apple still takes 1 slot and is not a broken image (the display area is still 16×16 GUI px, independent of the texture resolution, RT-37).
- **Rollback impact**: a revert puts the image back to 16×16 and withdraws both the passthrough and the relaxed assertion; it affects neither F-178's mechanism deletion nor any other task.

#### F-180 · Compressed Hay Block's sound `GRASS` → `STONE` (S · 0.5)

- **Goal**: `compressed_hay_block`'s `SoundType` becomes `STONE`; the five sound types change together (A-31), while hardness / blast resistance / tool / colour are all left alone.
- **Files touched**: `registry/FarmstormBlocks.java:79`.
- **Implementation notes**: `sound(SoundType.GRASS)` → `sound(SoundType.STONE)`; the `mapColor(COLOR_BROWN)` / `strength(30.0F, 6.0F)` / `requiresCorrectToolForDrops()` at `:76-78` are not changed by a single character.
- **Depends on**: —
- **Acceptance** (one item per sound type, corresponding to document 30 §7 #24 ~ #28):
  1. **Breaking**: the timbre matches `/playsound minecraft:block.stone.break block @s` and differs clearly from `minecraft:block.grass.break`.
  2. **Placing**: as above, against `block.stone.place` / `block.grass.place`.
  3. **Stepping**: as above, against `block.stone.step` / `block.grass.step`.
  4. **Mining**: as above, against `block.stone.hit` / `block.grass.hit`.
  5. **Falling onto it**: drop onto the block from 3 blocks up, against `block.stone.fall` / `block.grass.fall`.
  6. The mining time is unchanged: the tick count for the same pickaxe to mine it equals the pre-change value (hardness 30 untouched).
  7. `git diff registry/FarmstormBlocks.java` contains one line only, `:79`.
- **Rollback impact**: affects only the listening impression. After a revert the Compressed Hay Block goes back to grass sounds, invalidating no other task.

#### F-181 · Outpost Core light level 15 (S · 0.5)

- **Goal**: add `.lightLevel(state -> 15)` when `outpost_core` is registered, and leave every other block property unchanged by a character (D-141).
- **Files touched**: `registry/FarmstormBlocks.java:82-89`.
- **Implementation notes**: append `.lightLevel(state -> 15)` to the tail of the existing registration chain; `MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `sound(STONE)` / `pushReaction(BLOCK)` do not move. It is not made a config (D-141's supplementary item has rejected that).
- **Depends on**: —
- **Acceptance**:
  1. Place the core: F3's `Light` reading is **15**; after removing the core the same position falls back to the pre-placement reading (vanilla light updates, no residue).
  2. At night beside the core: there is light **within 15 blocks** and no light **beyond 16 blocks**; test once horizontally and once vertically (`15 − 0 = 15`, item 2 of document 29 §4.3).
  3. No mobs spawn in the brightly lit area: put one spawning platform in the centre of the light and one beyond 15 blocks; within 100 seconds the centre has **0** and the outside has ≥ 1 (RT-36).
  4. `git diff registry/FarmstormBlocks.java` contains one added line only, `.lightLevel(...)`.
- **Rollback impact**: affects only lighting and mob spawning. After a revert the core no longer glows, invalidating no other task (neither the overlay nor the binding regime reads the light level).

#### F-182 · `pumpkin_turret`'s three keys: `bulletSpeed` added, interval 40, damage 3 (M · 1.5)

- **Goal**: land the three numeric values of the Pumpkin Turret (man瓜) (`farmstorm:pumpkin_turret`) in config, and change along with them the two comments that distort at once plus one language key (D-127 / D-128 / D-129 / RT-33 / RT-38).
- **Files touched**: `config/FarmstormConfig.java:425-433` (the three keys + the field declarations), `:911` (the comment of `farmlandBonus`); `datagen/ModLangProvider.java` (+1 `farmstorm.configuration.pumpkin_turret.bulletSpeed`).
- **Implementation notes**:
  1. `fireIntervalTicks`'s `defineInRange` default 60 → **40**; **the comment at `:431`, which currently reads "60 刻 = 3 秒", must be changed in the same place to 40 刻 = 2 秒** (**[author's supplement]**: a comment is not data but distorts at once, the discipline being the same as item 7 of document 30 §9, "changing a key must change the class comment along with it"; this item is folded into this task and is not adjudicated separately).
  2. `projectileDamage`'s default 1 → **3**, its range 0–100 unchanged (read once per shot, an existing precedent).
  3. Add `bulletSpeed`: inside the `pumpkin_turret` group, after `fireIntervalTicks` (row 11 of document 30 §3), double, default **0.45**, range `0.0 ~ 4.0` (the same dimension and the same tier as `melon_grenade.submunitionSpeed`), with `.translation(...)`.
  4. The comment of `farmlandBonus` gains one sentence: "this key is at once the per-core capacity and the base of the team quota (quota = this key × the number of allied cores)" (RT-33, the "one key, two meanings" of document 30 §2.2.6).
  5. Only 1 language key is added; the key name of `.translation(...)` and the key in the language file are identical character for character; the total is reconciled by F-206.
- **Depends on**: —
- **Acceptance**:
  1. Delete the old `run/config/farmstorm-server.toml` and start the server: the `[pumpkin_turret]` section has exactly **5** keys — `fireIntervalTicks = 40`, `projectileDamage = 3`, `bulletSpeed = 0.45`, `targetRadius = 15`, `knockbackLevel = 1` (document 30 §7 #2 / #3).
  2. Keep the old toml and upgrade directly: `fireIntervalTicks` is still 60 and `projectileDamage` still 1, **while `bulletSpeed` is automatically filled in as 0.45** (D-147, RT-38, document 30 §7 #4).
  3. The range takes effect: hand-edit `bulletSpeed` to `5.0` and restart, and the loaded value falls back inside the range; the toml's comment line contains `0.0 ~ 4.0`.
  4. `grep -c "60 刻 = 3 秒" config/FarmstormConfig.java` = **0** and `grep -c "40 刻 = 2 秒"` = **1**; the comment of `farmlandBonus` contains the word "额度" (visible in `git diff`).
  5. The Chinese and English language files each contain **1** `farmstorm.configuration.pumpkin_turret.bulletSpeed`, and the two key sets are still equal.
- **Rollback impact**: after a revert the three keys go back to 60 / 1 / no key; F-183 and F-184 fail to compile because they cannot read `pumpkinTurretBulletSpeed` — they are scheduled after this task and are "later tasks that depend on it".

#### F-183 · Delete `TurretBullet.SPEED` and change the two read points (M · 1.5)

- **Goal**: delete the hard-coded constant `TurretBullet.SPEED = 0.6`, and have both readers (the per-tick step and the muzzle speed) read `pumpkin_turret.bulletSpeed` (D-129 / A-30).
- **Files touched**: `entity/TurretBullet.java:28`, `:90-96`; `behavior/impl/PumpkinTurret.java:75`.
- **Implementation notes**:
  1. Delete the constant declaration at `:28`; both `steerTowards` and `PumpkinTurret`'s muzzle speed become `FarmstormConfig.pumpkinTurretBulletSpeed.get()` (read once per shot, not cached in a field).
  2. The two places **must be in the same commit**: this constant has two readers and changing only one fails to compile outright (A-30's original finding; row 7 of the fact card §3.2 named only one of them).
  3. This task **does not change the convergence logic** (F-184 does): `steerTowards` is still "normalise × speed", and `onHitEntity` is still where it was.
  4. The client prediction does not move: `FarmstormEntityTypes`'s `clientTrackingRange(4)` / `updateInterval(10)` are not changed by a single character.
- **Depends on**: F-182
- **Acceptance**:
  1. `grep -rn "TurretBullet.SPEED" src/main/java` has **0 hits**; `grep -c "SPEED" entity/TurretBullet.java` = **0**.
  2. `grep -c "pumpkinTurretBulletSpeed"` is **≥ 1 each** in `entity/TurretBullet.java` and `behavior/impl/PumpkinTurret.java`.
  3. Same source, provable: hand-edit `bulletSpeed` to `0.1` and restart; the magnitude of the three components of `/data get entity @e[type=farmstorm:turret_bullet,limit=1] Motion` is ≈ 0.1 (±0.01), and the muzzle instant agrees with the per-tick displacement.
  4. `.tools\gw.cmd build` exits with code 0; `FarmstormEntityTypes`'s `git diff` is empty.
- **Rollback impact**: a revert has to restore the constant along with it; F-184 reads the same key and is scheduled after this task, so it is a later task that depends on it.

#### F-184 · The bullet's close-range convergence and the shared settlement body (L · 3.0)

- **Goal**: eliminate the point-blank overshoot (converge by the remaining distance when `dist ≤ step`, and judge a hit directly when `dist ≤ MIN_STEP`), and factor `onHitEntity`'s settlement into a shared method with a `hitResolved` gate; **no soft tracking is introduced** (D-129).
- **Files touched**: `entity/TurretBullet.java:90-96` (`steerTowards`), `:98-119` (the settlement body).
- **Implementation notes**:
  1. `steerTowards` has three branches: `dist > step` is identical to before; when `MIN_STEP < dist ≤ step`, `setDeltaMovement(delta)` (one step covers the remaining distance without overshooting); when `dist ≤ MIN_STEP`, settle directly and `discard()`.
  2. `applyHit(Entity)` **moves house as-is** from `:98-119` (the damage type `farmstorm:void_bolt`, the advancement `turret_hit`, the knockback formula not one character changed), and both `onHitEntity` and the direct judgment go through it.
  3. The `hitResolved` gate prevents double settlement (after `discard()` the vanilla `move()` may still produce a second callback in the same tick).
  4. **Not done**: a turn-angle cap, a maximum pursuit duration, guidance parameters, swapping the entity superclass (item 3 of D-129; item 8 of the tombstone §6).
- **Depends on**: F-183
- **Acceptance**:
  1. The target stands **within 1 block** of the turret: 5 consecutive shots all hit (the target loses **15** health = 5 × 3), and no backward displacement of the bullet is observed within 5 seconds (document 30 §7 #35).
  2. The target is **beyond 3 blocks**: the trajectory shape is the same as before (direction reset every tick, constant speed magnitude), and the hit count is of the same order as before.
  3. A single shot settles once: one hit takes only **3** health; `latest.log` holds only one damage record for the same shot.
  4. Hitting a block still `discard()`s and does not blow the block up; the damage type, knockback level 1 and the advancement are still granted (it enters no new branch).
  5. `grep -c "applyHit"` ≥ **3** (the definition + two calls); `grep -c "MIN_STEP"` ≥ **2**.
- **Rollback impact**: after a revert the point-blank overshoot comes back; F-182 / F-183's numeric changes are unaffected, and no other task is invalidated.

### Phase 28 · Friend Chives' Persistent Field (F-185 ~ F-186)

Phase goal: replace "a flash on landing" with "a 10-second field after landing", and plug its two known crash faces (an entity leak, residue across an unload) once and for all.

#### F-185 · Friend Chives: from a one-shot on landing to a field lasting `effectTicks` ticks (L · 3.0)

- **Goal**: after the projectile lands it becomes a field that lasts `friend_chives.effectTicks` ticks: every tick it scans a sphere of radius 6 blocks around the landing point, applies Q-Bounce for `effectTicks − x` ticks, and self-destructs on tick `effectTicks`; after landing it zeroes its velocity, has no gravity, is collision-free and stays visible; the sound is still judged once only, at the instant of landing (D-131 / D-132 / D-133 / A-33).
- **Files touched**: `entity/thrown/FriendChivesProjectile.java` (`keepAliveAfterImpact` + the new `tick()` + the split of applying / playing at `:56-80`); `entity/ThrownCropEntity.java` (**checked only, not changed**, rows 18 / 42 of document 30 §3).
- **Implementation notes**:
  1. Rewrite `keepAliveAfterImpact()` to return `true`; the base class's default at `:116-118` is still `false`, and other subclasses are unaffected.
  2. `onImpact` does three things only: read `fieldLength = friend_chives.effectTicks` once (default 200, range 20 ~ 1200), apply the batch with `elapsed = 0`, and play the sound once by the existing criterion at `:71-73`; then `setDeltaMovement(Vec3.ZERO)` + `setNoGravity(true)` + `noPhysics = true`, `fieldTick = 0`.
  3. `tick()`: `super.tick()` → `fieldTick++` → if `>= fieldLength` then `discard()` → otherwise `applyField(level, position(), fieldTick)`.
  4. The duration written by `applyField` is always derived from `fieldLength` (`fieldLength − elapsed`), and **no second 200 appears in the code** (A-33); each write is 1 tick shorter than the previous one, so it neither extends nor ends early.
  5. No block entity is added to carry the field (that would add a `block_entity_type` for `friend_chives`, and the registry surface would no longer be +0); Q-Bounce's amplifier stays 0 and its `tick` is not rewritten.
- **Depends on**: —
- **Acceptance**:
  1. Timing (document 30 §7 #30): a mob standing inside the sphere sees the remaining Q-Bounce duration decrease tick by tick (tick 1: **200**, tick 2: **199**, … tick 199: **1**); on tick **200** the entity disappears (`/data get entity @e[type=farmstorm:friend_chives]` finds nothing).
  2. 200 is not a constant: change `friend_chives.effectTicks` to **100**, restart and re-test — the decrease ends at 100 and the entity disappears on tick **100** (A-33).
  3. Sound (D-132 / #31): an empty drop (0 mobs inside the sphere) does not play `farmstorm:friend_chives`; hitting ≥ 1 mob plays it **1** time (only one line in `latest.log` for the whole field).
  4. Landing physics: when the landing point is inside flowing water, the entity's position does not move within 30 seconds (±0 blocks); standing on it does not push the player.
  5. `git diff entity/ThrownCropEntity.java` is empty; the hits of `grep -c "200" entity/thrown/FriendChivesProjectile.java` contain no duration literal.
- **Rollback impact**: after a revert it goes back to a one-shot on landing; F-186 (end on unload) loses its meaning but does not fail to compile, and no other task is invalidated.

#### F-186 · End on chunk unload: self-destruct on the first tick after reading `Settled` (M · 1.5)

- **Goal**: plug the permanent leak in which "after landing the entity is stored in the chunk NBT and, once loaded back, neither self-destructs nor ticks its field" (RT-35 / D-133): the field does not extend its life through NBT and does not come back across dimensions.
- **Files touched**: `entity/thrown/FriendChivesProjectile.java` (`readAdditionalSaveData` + `tick()`'s first-tick criterion).
- **Implementation notes**:
  1. `readAdditionalSaveData` reads the `Settled` key written by the base class and sets `loadedSettled`.
  2. The first thing `tick()` does after `super.tick()`: if `loadedSettled` is true, `discard()`.
  3. `fieldTick` **is not written to NBT** (D-133: ending on chunk unload).
  4. It relies only on the base class's existing keys and adds no custom NBT key (the storage scope of `ThrownCropEntity:145-157` does not move).
- **Depends on**: F-185
- **Acceptance**:
  1. With a field alive, walk 200 blocks away to force an unload and come back: the entity does not exist and no residue is left; quitting and re-entering the world does not bring it back (document 30 §7 #32).
  2. Check before and after the unload: `/data get entity @e[type=farmstorm:friend_chives]` finds nothing **after the first tick** of being back.
  3. The NBT face: read the chunk the entity was in with an NBT viewer — the entity has only the base class's `Settled` and similar keys, and no self-invented key such as `fieldTick` / `FieldTick`.
  4. `grep -c '"Settled"' entity/thrown/FriendChivesProjectile.java` ≥ **1**.
- **Rollback impact**: after a revert an unload leaves a permanent entity behind (RT-35 reproduces); F-185's in-session behaviour is unaffected.

### Phase 29 · The Farmland–Core Binding Regime (F-187 ~ F-198)

Phase goal: this version's largest structural replacement — turn "in range means managed" into "explicit binding", and finish in one pass the six links it drags along (ally determination, saves, backfill, the index's comparison key, the quota, migration), the two deletions (the config group, the server-side command tree) and one addition (the chunk ticket).

#### F-187 · `TeamResolver`'s `Owner × Owner` overload (M · 1.5)

- **Goal**: let the "ally" determination be made from UUID + name alone (valid offline too): give `TeamResolver` an `Owner × Owner` overload and turn the `Player` version into a one-line delegation (D-138).
- **Files touched**: `team/TeamResolver.java`, `team/ScoreboardTeamResolver.java`.
- **Implementation notes**:
  1. A new overload `boolean isAlly(ServerLevel level, Owner owner, Owner target)`; the existing logic moves in as-is, and the `Player` version only does `new Owner(uuid, scoreboardName)` and then delegates.
  2. No third criterion is opened: the signature and call sites of `util/DeployedOwnership.isAlly(...)` do not move.
  3. `teamName(...)` already looks the name up in the scoreboard member table (usable offline), and this task does not change it.
  4. The two values of `untamedFallback` stay equivalent to the status quo item by item (under `"self"` a player with no team counts only as himself).
- **Depends on**: —
- **Acceptance**:
  1. `grep -c "isAlly(ServerLevel" team/TeamResolver.java` and the same in `team/ScoreboardTeamResolver.java` are **2 each** (the two overloads); the `Player` version's body is within 3 lines (pure delegation).
  2. Zero behaviour change: a same-team ally stepping on allied farmland is stopped, a non-ally stepping on the same plot turns it into dirt as usual — the two control groups give the same result before and after.
  3. Decidable offline: kick the farmland's owner B offline, and another online ally C jumps onto the same plot — the judgment still holds (the full observation surface is in F-195).
  4. `.tools\gw.cmd build` exits with code 0; `util/DeployedOwnership.java`'s `git diff` is empty.
- **Rollback impact**: after a revert F-191 (the quota counting allies by core count) and F-195 (offline protection) lose their offline criterion, but the old `Player` version still compiles; both are scheduled after this task.

#### F-188 · `FarmlandLedger` v2: the binding table, `orphans`, the backward-compatible read and the write entries (L · 3.0)

- **Goal**: turn the ledger from "player → a set of coordinates" into a binding table of "farmland → the one core", raise the on-disk format to `dataVersion: 2` with a backward-compatible read of v1; `byOwner` becomes a derived view and is no longer written to disk (D-134 / D-137 / A-47).
- **Files touched**: `farmland/FarmlandLedger.java` (`:51` the body, `:128-158` the bookkeeping, `:203-219` the lazy validation, `:228-264` the save / load; new `bind` / `unbind` / `migrate` / `bindingsOf` / `orphanCount` / `rebuildByCore`).
- **Implementation notes**:
  1. The body holds three things: `LinkedHashMap<BlockPos, Binding> bindings` (the single source of truth), `Map<CoreKey, LinkedHashSet<BlockPos>> byCore` (an in-memory reverse lookup, **not written to disk**), and `LinkedHashMap<BlockPos, UUID> orphans` (a legacy save that cannot be backfilled, **written to disk** — if it were not, the backfill could not be retried and "leave it alone" would degenerate into "forget it forever").
  2. Two records: `Binding(pos, owner, coreDimension, corePos)`, `CoreKey(dimension, pos)`; `binding.owner` and the core's owner **may differ** (D-136's migration judges the alliance by the farmland's owner).
  3. On disk: `dataVersion: 2` + `bindings` (4 keys) + `orphans` (2 keys); **`owners` is no longer written**; a missing `dataVersion` is read as 1.
  4. Backward-compatible read: v1's `owners` are **all** read as `orphans` (no binding is guessed); validation happens on read (an entry that is no longer `minecraft:farmland` and whose chunk is loaded is dropped on the spot); `rebuildByCore()` rebuilds the reverse lookup from `bindings`; `orphans` uses a `LinkedHashMap` only for a stable, readable on-disk form, and **its order takes part in no judgment** (A-47: the backfill in F-190 re-sorts by `BlockPos`'s single-level total order and then processes entry by entry).
  5. When `setDirty()` is called: yes for `bind` / `unbind` / `migrate` / `adoptOrphan`; no for `rebuildByCore`; for `validated` **only when an entry really was lost**.
  6. Lazy cleanup must **prune `byCore` as well** (item 3 of document 30 §9: pruning only `bindings` leaves ghost farmland occupying the 16-slot capacity).
- **Depends on**: —
- **Acceptance**:
  1. Hand-build a `.dat` with `dataVersion: 2` + 1 `bindings` entry and put it in `world/data/`: after startup the `latest.log` line "耕地绑定指纹 …（**1** 条）" is non-empty; start the same save once more and the fingerprint is identical.
  2. A 0.1.0 legacy save (only `owners`) starts: it does not crash, `latest.log` gets one line "旧格式：N 条读入 orphans", and not one block sampled with `/execute if block … minecraft:farmland` has moved.
  3. Shape after saving: after `/save-all` the NBT holds the three keys `dataVersion: 2`, `bindings`, `orphans`, and **no** `byCore` key.
  4. Downgrade direction: hand the v2 save to a 0.1.0 jar to read — it reads an empty ledger and does not shovel the world up (item 1 of document 30 §2.2.3; this is an acceptable downgrade direction).
  5. The write entries are all present: `grep -c "public void bind\|public void unbind\|public void migrate\|bindingsOf\|orphanCount" farmland/FarmlandLedger.java` ≥ **5**.
  6. Ghost farmland: after `/setblock air` on a bound coordinate and one lazy validation, the entry counts of `bindings` and `byCore` **decrease by 1 at the same time** (the two `latest.log` lines "绑定指纹（M 条）" and "反查集合 N 条" change together).
- **Rollback impact**: a revert puts the ledger back to v1 (the binding information is lost, but the world is not shovelled up); F-190, F-191, F-193, F-195 and F-196 all depend on it and are scheduled after it.

#### F-189 · `OutpostCoreIndex.Entry` gains `gameTime` and three read-only accessors (M · 1.5)

- **Goal**: land the first level of A-39's comparison key on the index side and write it to disk; at the same time add read-only accessors for `ownerNames` / `count` / entry iteration (A-39 / D-142 / D-138).
- **Files touched**: `outpost/OutpostCoreIndex.java` (`:50`'s `Entry`, `:209-238`'s `save`, `:240-266`'s `load`, `:56`'s `ownerNames`, `count`, the signature of `record(...)`).
- **Implementation notes**:
  1. `record Entry(ResourceLocation dimension, BlockPos pos, long gameTime)`; each on-disk entry gains one `gameTime` key.
  2. Write timing: in `OutpostCoreBlockEntity#setOwner` the **same** `placedGameTime` is written into the BE and the index at once (same method, same variable, same tick ⇒ the two copies cannot drift).
  3. Read: an entry in a legacy save missing `gameTime` takes `Long.MIN_VALUE` (treated as the earliest, without forging a time), and startup logs one line "核心比较键缺失（老档）：N 条"; **no** "backfill from the BE while loading" (that would make the value depend on the loading timing and break reproducibility).
  4. Read-only accessors: `entriesOf(...)` / `owners()` / `count(server, uuid)`; **`isOwnerOrAlly` is not revived** (it takes a `Player` and is not offline-safe).
  5. The comparison path only reads the index: no `getChunk`, no `getBlockEntity`, no `hasChunkAt` (A-39's structural guarantee, not a convention).
- **Depends on**: —
- **Acceptance**:
  1. Place 1 core, then read the save: the entry in `farmstorm_outpost_core.dat`'s `entries` contains `gameTime`, and its value equals the `placedGameTime` read back with `/data get block`.
  2. A legacy save (no `gameTime`) starts: the log shows "核心比较键缺失（老档）：N 条", N = the number of old cores; two startups give the same N (no drift with time).
  3. `grep -c "getChunk\|getBlockEntity\|hasChunkAt" outpost/OutpostCoreIndex.java` = **0**.
  4. The three accessors exist and are used from outside: `grep -rn "entriesOf\|\.owners()\|\.count(" outpost/OutpostCoreIndex.java` ≥ **3** (the definition + at least one call).
  5. `isOwnerOrAlly` still has only a definition and no caller: `grep -rn "isOwnerOrAlly" src/main/java` has exactly **1** hit.
- **Rollback impact**: after a revert the index lacks `gameTime`, F-193's tie criterion degenerates to two levels (A-39 lapses), and F-200's cross-dimension ordering can still use the dimension id; F-190 / F-193 / F-200 depend on it.

#### F-190 · The startup backfill: unbound farmland backfilled by the migration predicate (M · 1.5)

- **Goal**: run one backfill after the server starts, binding the farmland in `orphans` to cores by "covered + allied + a free slot, nearest first"; whatever cannot be backfilled stays farmland and stays unbound (D-137 / A-47).
- **Files touched**: `farmland/FarmlandLedger.java` (`backfill(server)`), `event/FarmstormGameEvents.java` (the one-shot startup gate).
- **Implementation notes**:
  1. Traversal order: **first sort the candidate farmland ascending by `BlockPos#compareTo` (y → z → x) in a single-level total order, then process it entry by entry** (A-47 rev.2); the persist order of `orphans`, the iteration order of `byOwner` and the order of any Map / List **take part in no judgment**. Why `BlockPos` single-level rather than "dimension + coordinates": on the farmland side the on-disk form has only `pos` and no dimension field (item 2 of document 30 §2.2.2), and only cores have a dimension.
  2. The predicate is **the same one** as the migration (covered + allied with the farmland's owner + the core has a free slot); among several candidates take the one with the nearest `distSqr`, and on a tie go by `placedGameTime` → dimension id → coordinate lexicographic order (A-39's three-level key governs **candidate cores only** and is not used for the traversal order).
  3. The whole segment **contains no `setBlock`**; it runs once on the first `ServerTickEvent.Post` (a one-shot boolean gate), not in a constructor (at constructor time `MinecraftServer` is not ready yet).
  4. It closes with one INFO line: "耕地补建：绑定 N 块 / 保持未绑定 M 块（老档共 T 块，按 `BlockPos` 全序）".
- **Depends on**: F-188, F-189
- **Acceptance**:
  1. Start with a 0.1.0 legacy save and the log shows "耕地补建：绑定 N 块 / 保持未绑定 M 块"; **run the same save twice and N and M are exactly the same** (document 30 §7 #19, A-47).
  2. **Order takes part in no judgment (A-47 rev.2)**: swap the two records of the `.dat`'s `orphans` segment and re-run — the bound coordinate set and N/M are **exactly the same** as before the swap; the log still says "按 `BlockPos` 全序".
  3. Farmland that cannot be backfilled is still `minecraft:farmland`: `/execute if block … minecraft:farmland` succeeds for each of them, **0** turned into dirt.
  4. The backfill runs once only: a second "耕地补建" log line does not appear within the same run; only a restart runs it again.
  5. `grep -c "setBlock" farmland/FarmlandLedger.java` = **0** (the whole backfill segment writes nothing to the world).
  6. Capacity is a hard cap: build 20 old fields + 1 core (capacity 16) — **N = 16, M = 4**, and the blocks from the 17th on do not move; **which 16 can be bound is decided by `BlockPos`'s total order** (the 16 smallest coordinates), and shuffling the `orphans` order changes nothing.
- **Rollback impact**: after a revert every old field stays unbound (unprotected, unshovelled); the binding chain from F-193 on still works, and no other task is invalidated.

#### F-191 · Quota and used change criterion, and the `base` trio deleted (M · 1.5)

- **Goal**: `capacity = farmlandBonus × the number of allied cores`, `used = the number of blocks under the ally's name in the binding table`; delete `baseOverride` / `base()` / `setBase()` / `personalCapacity`'s base parameter / `sumOverMembers`'s base parameter (D-139 / D-138).
- **Files touched**: `farmland/FarmlandLedger.java:57-58`, `:72-98`, `:101-121`, `:185-196`; `command/FarmstormCommands.java:50-57` (temporarily made a read-only echo so the commit still compiles).
- **Implementation notes**:
  1. `capacity(level, player)` goes through `OutpostCoreIndex.count(server, uuid)` summed over allies (other dimensions included, and available offline too).
  2. `used(...)` counts the `bindings` entries whose `owner` and the player are allied to each other; `orphans` **is not counted** (no binding, no slot taken).
  3. `members(...)`'s online traversal disappears; `farmlandLimitPerPlayer`'s **only reader**, `base()`, disappears with it (the key itself is deleted by F-192).
  4. The command side changes minimally: `FarmstormCommands`'s body becomes "echo `used / capacity`" and no longer calls `setBase`; the command tree stays for now and is deleted whole in F-197.
  5. The "one key, two meanings" comment is F-182's job; this task changes the formula only.
- **Depends on**: F-187, F-188, F-189
- **Acceptance**:
  1. The four worked examples check out item by item (document 29 §3.4): one player with 0 cores → **0**; one player with 1 core → **16**; one player with 2 cores whose land all lies inside A's 3×3 → quota **32** but only **16** can actually be tilled; the same cell covered by A and B with A full → B can still be bound (the last item is re-run in F-193).
  2. A teammate logging off does not change the quota: A and B are on the same team with 1 core each, and after B logs off A's quota is still **32** (it used to shrink).
  3. `grep -c "baseOverride\|personalCapacity\|sumOverMembers" farmland/FarmlandLedger.java` = **0**; `farmlandLimitPerPlayer` **still exists** in `FarmstormConfig.java` (this task does not delete the key).
  4. `used` and the bindings share one source: after tilling 1 cell `used = 1`; after `/setblock air` on that cell and one lazy validation `used = 0`.
  5. `/farmstorm farmlandlimit 8` still executes, but **does not change the quota** — it only echoes `used / capacity`; the quota reading is the same before and after.
  6. `.tools\gw.cmd build` exits with code 0.
- **Rollback impact**: a revert has to restore `setBase` and the command call together (the command currently only echoes, so it will not crash); F-192, F-193 and F-197 depend on it.

#### F-192 · Delete config's whole `farmland` group and 1 language key (S · 0.5)

- **Goal**: delete the `farmland` group (`limitPerPlayer` is one of its keys; field + comment + translation included), so that `farmstorm-server.toml` no longer has a `[farmland]` section and the group count goes **59 → 58** (D-139). **This action surface has no corresponding row among the 44 rows of document 30 §3 (A-42's supplementary row)**.
- **Files touched**: `config/FarmstormConfig.java:226` (the field declaration), `:904-908` (the whole group); `datagen/ModLangProvider.java:393-394` (the language-key generation segment).
- **Implementation notes**:
  1. Delete the whole segment from `b.push("farmland")` to `b.pop()`, the field declaration and its getter.
  2. Delete the generation segment of `farmstorm.configuration.farmland.limitPerPlayer`; after datagen is re-run the key disappears from both language files.
  3. The other 57 groups and the `outpost_core` group are not changed by a single character.
  4. This task is the **closing point** of A-40's intermediate state: after the old toml is deleted and regenerated, the tool's §1 section should turn green for the first time.
  5. **`base()` is `farmland.limitPerPlayer`'s only reader (`FarmlandLedger.java:72-74`), so F-191 must precede F-192; this is not a stylistic order but a compile order** (A-56): if F-192 were committed first, `base()` would reference a deleted field and that commit would not compile.
- **Depends on**: F-191
- **Acceptance**:
  1. Delete the old `run/config/farmstorm-server.toml` and start the server: the `[farmland]` section **does not exist** in either artifact, the section-line count (`^\s*\[`) = **58**, and the key count = **166** (document 30 §7 #2).
  2. `grep -c "farmland" config/FarmstormConfig.java` = **0**; `grep -c "farmland.limitPerPlayer"` is **0** in both lang JSONs.
  3. Keeping the old toml and starting without deleting it: the `[farmland]` section remains but **nobody reads it**; the tool reports STALE for the residual section and hints "regenerate to remove it" — one toml deletion plus a re-run makes it disappear (this is a known, recoverable intermediate state).
  4. After the toml is regenerated, `python -X utf8 tools/verify_project.py` gives: `结论：FAIL 0 项`, exit code 0, and a §1 section showing **166** (A-40's closing point; this is the first task in this file that may cite a tool conclusion).
- **Rollback impact**: after a revert the `[farmland]` section comes back into the toml and the group count returns to 59; F-197 (deleting the command) does not depend on it, so a revert does not invalidate F-197, but it turns the `[farmland]` assertions of F-208 / F-209 / F-212 red again.

#### F-193 · `FarmlandGuard.check`: core selection + a free slot on that core (L · 3.0)

- **Goal**: widen the till gate's three-state judgment into "range gate → candidate set (covered ∩ allied ∩ a free slot) → take the nearest → return the chosen core", and make `QUOTA_FULL` mean "the nearest allied core is full" (D-135 / A-39 / RT-33).
- **Files touched**: `farmland/FarmlandGuard.java:69-75` (`check`); a new `Decision` record.
- **Implementation notes**:
  1. A new `record Decision(Verdict verdict, @Nullable CoreKey core)` plus an overload carrying the result; the three-state structure (`ALLOW` / `NO_CORE` / `QUOTA_FULL`) does not change.
  2. Distance uses `BlockPos#distSqr` (three-dimensional Euclidean squared): the 3×3 judgment is already Chebyshev, and using Chebyshev for the distance too would produce a lot of ties along the diagonals.
  3. The three-level tie key: `placedGameTime` ascending → dimension id → `BlockPos#compareTo` (y → z → x); **it may not depend on any Map / List iteration order** (A-39).
  4. The candidate set is **filtered first and the nearest taken afterwards** (taking the nearest first and then testing the free slot would wrongly refuse "the nearest is full, another has a slot", contradicting the total quota of `16 × the number of cores`).
  5. `QUOTA_FULL`'s language key name does not change (item 4 of document 30 §9); only the text changes.
- **Depends on**: F-187, F-188, F-189
- **Acceptance**:
  1. The **17th** cell is refused with the message "耕地额度已满 16/16"; that core still has only **16** entries under its name (document 30 §7 #17).
  2. Change `outpost_core.farmlandBonus` to **2** and restart: the **3rd** cell is refused, and the team capacity changes to 2 at the same time (RT-33).
  3. The three-level key (①②③ of document 30 §7 #18): till 1 cell between two cores 40 blocks apart → bind the nearer one; equidistant → bind the one with the earlier `placedGameTime`; equidistant and at the same tick (offline, set both `gameTime`s to the same value) → bind the one whose coordinate is lexicographically earlier.
  4. "The nearest is full, the other has a slot" → bind to the one with a slot (document 29 B-17).
  5. A refusal costs no durability (document 29 B-16): compare the `ItemDamage` reading before and after the operation.
- **Rollback impact**: after a revert `check` goes back to the two-state judgment, and F-194 loses the chosen core and fails to compile (the two tasks are adjacent and dependent).

#### F-194 · Write the binding when tilling succeeds (M · 1.5)

- **Goal**: in `FarmstormGameEvents`'s till gate, replace `ledger.add(...)` with `ledger.bind(owner, coreKey, pos)` in the `ALLOW` branch, completing the binding write + bookkeeping + farmland placement within the same tick (D-135).
- **Files touched**: `event/FarmstormGameEvents.java:194-217` (`:206`'s `ledger.add(...)`).
- **Implementation notes**:
  1. Use the `Decision.core` returned by `check` as the binding target; when `Decision`'s `core` is empty, refuse by the `NO_CORE` / `QUOTA_FULL` path as before.
  2. `bind(player.getUUID(), new CoreKey(level.dimension().location(), decision.core()), pos)`.
  3. No new message text, no new language key, and the refusal path's no-durability-cost semantics are unchanged.
- **Depends on**: F-193
- **Acceptance**:
  1. Place 1 core and till 1 cell: 1 entry appears in `bindings`, with `coreDimension = "minecraft:overworld"` and `corePos` = that core's coordinates (document 30 §7 #16).
  2. The binding completes within the same tick: `/save-all` immediately after tilling and reading the NBT shows that entry (no waiting for the next autosave).
  3. Two-core overlap: till 1 cell at the 3×3 junction of cores A and B, and `bindings.corePos` falls on the nearer one.
  4. The two refusal cases produce **not one binding** (the `bindings` count does not change).
  5. `grep -c "ledger.add(" event/FarmstormGameEvents.java` = **0**.
- **Rollback impact**: after a revert tilling no longer writes a binding, and nobody consumes F-193's core selection; F-196 (migration) is unaffected (it reads `bindings`, which may be empty).

#### F-195 · Farmland protection's offline judgment and the trample face (M · 1.5)

- **Goal**: replace the farmland protection criterion "iterate the online players" with "farmland → binding → the core owner's UUID", valid even when the owner is offline; the trample face goes by "a binding means protected" (D-138 / A-45).
- **Files touched**: `farmland/FarmlandGuard.java:92-107` (`inOwnRange`), `:120-134` (`protectsFromTrample`).
- **Implementation notes**:
  1. `protectsFromTrample(level, pos)`: first test that the block is still `minecraft:farmland`, then look up whether `bindings` has this cell — **a binding means protected**.
  2. The reason (A-45): the acting party of a trample may be a `Monster` with no team; judged by "ally" live, the mob would be judged a non-ally and could trample allied farmland, directly conflicting with item 3 of 0.1.0's D-120 (both players and mobs must be stopped). A binding is produced only when an ally tills, so "has a binding" is the stored evidence that "the ally relationship once held".
  3. `inOwnRange(level, player, pos)` changes to reading the binding table + `isAlly(core owner, player)` (this one has an explicit enquirer and goes by the live ally judgment).
  4. After a team change: the **quota and the overlay** faces recompute at once by the new team (F-191 / F-200 each have an acceptance item for it), while **the trample face stays protected** (the binding has not changed) — the difference between the three faces is written into the narrowed version of document 29 B-18.
- **Depends on**: F-187, F-188
- **Acceptance**:
  1. After the core's owner logs off his farmland cannot be trampled: A places a core and tills 1 cell, A quits, B (not on the same team) jumps onto that cell → it is still `minecraft:farmland` (bug 2 of D-138 no longer reproduces).
  2. Control: an **unbound** plot of farmland at the same position is trampled into dirt as usual.
  3. A mob trampling: a zombie jumps onto bound farmland → it does not turn into dirt (item 3 of D-120, saved by A-45).
  4. The trample face does not change after a team change (A-45): move the core's owner to another team and the same cell still cannot be trampled; at the same moment the **quota** reading recomputes by the new team — the two readings taken side by side.
  5. `grep -c "getPlayerList().getPlayers()" farmland/FarmlandGuard.java` = **0**.
- **Rollback impact**: after a revert protection falls back to iterating online players (bug 2 reproduces); F-196 is unaffected, and no other task is invalidated.

#### F-196 · Core destroyed: migrate first, three downgrade branches (L · 3.0)

- **Goal**: `settleOnRemoval` handles only the farmland under this core's own name, migrating it to a successor core first; if it cannot migrate → it turns into dirt — **two levels: rebind / turn to dirt** (narrowed by A-60); "cannot migrate and the chunk is not loaded → unbind only" degrades to a **defensive branch**, unreachable in normal operation (D-136 / A-35 / A-60).
- **Files touched**: `blockentity/OutpostCoreBlockEntity.java:315-335`, `:353-366`, `:369-379`.
- **Implementation notes**:
  1. Keep the "really removed / unloaded" criterion at `:316-326` **unchanged by a single character**; take this core's own set from `bindingsOf(dead)`.
  2. Candidates: cover that farmland ∧ allied with **the farmland's owner** ∧ still has a free slot; among several candidates go by `distSqr` → the three-level key (the same one as tilling).
  3. Two exits plus one defensive branch: rebind (`migrate`) and `setBlock(DIRT, UPDATE_ALL)` + unbind; "unbind only" is the defensive branch (A-60: the ticket always covers the same 3×3, so it is unreachable in normal operation).
  4. **Compute the whole set first, then touch the world** (following the existing reason at `:312-313`, to avoid modifying the ledger while iterating it); **no chunk is loaded for the settlement** (no `getChunk`).
  5. The farmland's owner is not notified, nothing is dropped and no compensation is given (D-136's supplementary item).
- **Depends on**: F-188, F-189, F-195
- **Acceptance**:
  1. A successful migration (document 30 §7 #20): A has 3 fields under his name and B covers 2 of them; after destroying A those 2 fields' `bindings.corePos` become B and the 3rd becomes `minecraft:dirt`; B's total is ≤ **16**.
  2. Cannot migrate + the chunk is loaded (#21): A has 1 field under his name, no successor core around, and someone is standing at the field's edge; after destroying A the field becomes dirt and **there is no drop**.
  3. Walk 200 blocks away and destroy the core with `/execute in <dim> run setblock <core coords> air` (#22 / A-60) → that field turns into `minecraft:dirt` (the same as up close) with no drops; the `bindings` entry is removed; the log does **not** show "迁移失败且区块未加载：只摘绑定" (that string is at `OutpostCoreBlockEntity.java:374`, does not appear in normal operation, confirmed by code review).
  4. The two downgrades (document 29 B-19 / B-20): a successor core exists but is full → dirt; a successor core covers it but is not allied with the farmland's owner → dirt.
  5. The plot is no longer farmland (B-21): skip it, unbind, and do not touch the world (if the position now holds stone, the stone does not change).
  6. Only this core's own name is handled: another core's fields (`bindings.corePos` pointing at B) **do not move at all** after A is destroyed (bug 1's symptom does not reproduce).
- **Rollback impact**: after a revert destroying a core goes back to "shovel up everything in range" (bug 1 reproduces); F-198's ticket release hangs on `release()` and does not depend on this task's migration branches.

#### F-197 · Delete the whole `/farmstorm farmlandlimit` server-side command tree (M · 1.5)

- **Goal**: delete the whole `command/FarmstormCommands.java` file, 3 registrations, 1 language key and the corresponding segment of `ModLangProvider`; the server side no longer has `/farmstorm` (D-140 / RT-40).
- **Files touched**: `command/FarmstormCommands.java` (deleted), `event/FarmstormGameEvents.java:4`, `:62`, `:71-73`, `datagen/ModLangProvider.java:404-406`.
- **Implementation notes**:
  1. Delete the whole file; the import, `gameBus.addListener(...)` and the body of `onRegisterCommands` are deleted together — three places.
  2. Delete the generation segment of `command.farmstorm.farmlandlimit.set` and its output in both language files.
  3. **No empty shell is kept**: the `/farmstorm` root node is not registered.
  4. The client `/farmstorm hud` is a different registration (`RegisterClientCommandsEvent`); this task touches neither it nor the semantics of its root node.
- **Depends on**: F-191
- **Acceptance**:
  1. The dedicated server console running `/farmstorm` returns "未知的指令"; `/farmstorm farmlandlimit 5` fails on the server side (the server half of document 30 §7 #12).
  2. The single-player client running `/farmstorm farmlandlimit 5` fails as well; `farmlandlimit` does not appear in the client's completions (the full completion of the client `hud` subtree is in F-205).
  3. `grep -c "farmlandlimit"` is **0** in both lang JSONs and in `ModLangProvider.java`.
  4. `grep -rn "FarmstormCommands" src/main/java` has **0 hits**; `git status` shows the file deleted.
  5. The server startup log has no new exception, and `gw build` exits with code 0.
- **Rollback impact**: a revert puts the command tree back, but `setBase` was already deleted in F-191, so **this task and F-191 are one rollback chain**: reverting this task must revert F-191 too, otherwise it fails to compile.

#### F-198 · A runtime temporary ticket on the chunk the core is in (M · 1.5)

- **Goal**: every placed core adds a ticket of its own `TicketType` with `distance = 1` on the 1 chunk it occupies (the smallest ticket for block-entity ticking), released when the core is destroyed / the server stops; **`world/data/chunks.dat` is not written** (D-145 / A-38 / A-43 / RT-39).
- **Files touched**: `outpost/CoreChunkTickets.java` (new), `blockentity/OutpostCoreBlockEntity.java:95-103`, `:166-170`, `event/FarmstormGameEvents.java` (`ServerStoppingEvent` + the startup ticket re-issue).
- **Implementation notes**:
  1. `TicketType.create("farmstorm_core", Comparator.comparingLong(ChunkPos::toLong))`, `distance = 1`; the four arguments of `addRegionTicket` / `removeRegionTicket` are exactly identical.
  2. Three release points: `release()`, the `release()` at the end of `settleOnRemoval`, and the shutdown `releaseAll`; the release must be idempotent (one deletion too many is a no-op, one too few is a leak).
  3. **Ticket re-issue at startup**: tickets live in memory and are lost on shutdown, so after a restart one is requested again for each coordinate in `OutpostCoreIndex` (tickets first, bindings afterwards).
  4. **A bare `TicketType` only**: `ServerLevel#setChunkForced` and NeoForge's `TicketController#forceChunk` both write `chunks.dat`, and both are rejected (A-38).
  5. **Write the wording per A-43**: with `distance = 1` the centre chunk is level 32 (block-entity tick), the immediately adjacent ring is level 33 (FULL, loaded but not ticking), and the ring beyond that is no longer loaded. So the exact meaning of "loading only 1 chunk" is "**only 1 chunk is ticked**, and another 8 are loaded to FULL"; documents and acceptance always write the tick face and **never claim that only 1 chunk gets loaded** (that line is necessarily red).
- **Depends on**: F-189
- **Acceptance**:
  1. With no player near the core (walk 200 blocks away to unload the local chunks) the core's block entity still ticks: in `latest.log` its boundary-particle round counter keeps growing every **40** ticks; after destroying the core and walking 200 blocks away, F3's `Chunk` line shows that chunk **not loaded** (document 30 §7 #36).
  2. The `Forced` array of `world/data/chunks.dat` (including `DIM-1` / `DIM1`) **does not contain** that core's `ChunkPos.toLong()` (#36).
  3. Stop the server and start it again: the tickets are rebuilt (after the restart the core's boundary particles resume within 40 ticks); `grep -c "setChunkForced\|forceChunk" src/main/java` = **0**.
  4. Idempotent release: destroy two cores in a row and then stop the server — the release count = the request count; every log line carries the `farmstorm_core` type name.
  5. The tick-face wording (A-43): the centre chunk's F3 state is "block entity ticking", the adjacent ring is "loaded", and beyond the two rings it is "not loaded" — capture one frame of each of the three layers.
- **Rollback impact**: after a revert the core ticks only while the chunk is loaded (distant boundary particles do not refresh); F-199 / F-200 do not depend on it; `chunks.dat` is unaffected throughout (this task never writes it).

### Phase 30 · The Overlay and the Data Face (F-199 ~ F-205)

Phase goal: finish the only requirement of this version that "breaks the red line" — 1 S2C read-only packet + one self-drawn HUD overlay (the overlay) + 2 key bindings + 3-key CLIENT config + 1 client command.

#### F-199 · 1 S2C read-only packet: payload, codec, registration and the client snapshot (L · 3.0)

- **Goal**: the project's first custom network packet — the S2C payload of `farmstorm:core_list` + `StreamCodec` + `playToClient` registration + the client handler and snapshot; the client only reads, never replies and makes no judgment (D-142 / RT-31).
- **Files touched**: `network/CoreListPayload.java`, `network/ClientCoreListHandler.java`, `network/FarmstormNetwork.java` (registration), `client/ClientCoreList.java`, `Farmstorm.java` (the constructor subscribing to the mod bus).
- **Implementation notes**:
  1. The payload: `List<Core> cores` + `int total`; `Core(dimension, pos, ownerName, self)` with four fields; `TYPE = new Type<>(Farmstorm.id("core_list"))` — **not** `CustomPacketPayload.createType(String)` (it would land in the `minecraft:` namespace).
  2. The codec: a 4-element `StreamCodec.composite` + `ByteBufCodecs.list(MAX_CORES = 64)`; `total` is sent **separately** (the `+N` needs the true total, and a truncated `cores.size()` cannot answer it).
  3. Registration: `event.registrar("1").playToClient(...)`, on the mod bus, `play` phase only, CLIENTBOUND only.
  4. The handler has a single line, `context.enqueueWork(() -> ClientCoreList.set(...))`; the client class is resolved only when the lambda's synthetic method is called, so a dedicated server never hits `NoClassDefFoundError`.
  5. The client snapshot: `volatile List<Core>` + `volatile int total`, written only by the handler and read only by the rendering.
- **Depends on**: —
- **Acceptance**:
  1. Across the whole repository `grep -rln "CustomPacketPayload" src/main/java` finds exactly **1** file (`network/CoreListPayload.java`); `grep -rn "playToServer\|sendToServer" src/main/java` has **0 hits** (document 30 §7 #6).
  2. A dedicated server completes startup with no `NoClassDefFoundError`; the registration line in `latest.log` contains `farmstorm:core_list` (#6).
  3. In single-player, send one empty packet with `total=0`: the client log gets one line "收到核心列表 0 条（total=0）"; the overlay has not landed yet, so the evidence is the log.
  4. `grep -c "MAX_CORES" network/CoreListPayload.java` ≥ **2** (the constant + the codec's use).
  5. `python -X utf8 tools/verify_project.py` exits with code 0 and `结论：FAIL 0 项` (A-40 has closed).
- **Rollback impact**: after a revert the overlay has no data face (F-200 / F-203 lose their upstream); keeping the client snapshot class as an empty implementation does not crash either, and no other task is invalidated (apart from F-200 and F-203).

#### F-200 · The broadcaster: assembly, three-level ordering, debounce and 10 push timings (L · 3.0)

- **Goal**: assemble "the list of allied cores" on the server side and push it at 10 timings to the players who should receive it; nothing survives a disconnect / server switch (D-142 / A-39 / RT-31).
- **Files touched**: `network/CoreListBroadcaster.java`, `event/FarmstormGameEvents.java` (+4 player events + `ServerTickEvent.Post`), `blockentity/OutpostCoreBlockEntity.java:95-103`, `:166-170`.
- **Implementation notes**:
  1. Assembly: iterate every owner in the index → do the UUID-side ally judgment against the viewer (F-187's overload) → collect candidates → the comparison key reads `gameTime` **from the index only** (no BE read, no chunk load) → three-level ordering → `total` = the number of candidates, `cores` = the first 64.
  2. Debounce: `MIN_INTERVAL_TICKS = 10` with `lastSentTick` + `pending`, flushed uniformly on `ServerTickEvent.Post`; several requests in the same tick send once.
  3. Reconciliation: a fingerprint comparison every `RECONCILE_INTERVAL_TICKS = 100`, catching "a teammate switches teams / logs in / an event is missed".
  4. The 10 timings hang one by one per the table of document 30 §2.1.4; logging out clears the three records `lastSentTick` / `pending` / `fingerprint`; the client empties its snapshot in `ClientPlayerNetworkEvent.LoggingOut`.
  5. The recipients are decided by each player's own assembly result (a hostile core is never sent to a client).
- **Depends on**: F-199, F-189, F-187
- **Acceptance**:
  1. One player with 1 core: one log line "核心列表下发：<玩家> 1 条（total=1）" (the log half of document 30 §7 #7).
  2. 1 core in the overworld + 1 in the nether: standing in either dimension the packet received is `total=2`, the two dimension markers are `overworld` / `the_nether`, and teleporting changes nothing (#8).
  3. Ordering (①④ of #9): place A first (t=100) and B afterwards (t=200) → A comes first; restart the same save twice and the two "核心比较键指纹" lines of `latest.log` are exactly identical.
  4. Debounce: placing a core + destroying a core in the same tick (a chained trigger) produces **1** packet only (one dispatch record in the same tick).
  5. Nothing survives a server switch (#29): quit server X and enter server Y (Y has no cores) — the overlay disappears at once; back on X it reappears.
  6. Hostile cores are not sent out: the second client's core is in no packet (the log line "本包只含 N 条" against N's actual value).
- **Rollback impact**: after a revert the overlay has no data (F-203 draws an empty panel); F-199's packet still exists but nobody sends it, and no other task is invalidated.

#### F-201 · CLIENT config 3 keys and 3 language keys (M · 1.5)

- **Goal**: add `config/FarmstormClientConfig.java` (the first `ModConfig.Type.CLIENT`), writing the three keys of `config/farmstorm-client.toml`'s `[hud]` section (D-144).
- **Files touched**: `config/FarmstormClientConfig.java` (new), one line after `Farmstorm.java:56`, `datagen/ModLangProvider.java` (+3).
- **Implementation notes**:
  1. `enabled` (bool, **true**), `x` (int, **4**, 0 ~ 3840), `y` (int, **4**, 0 ~ 2160); all three carry `.translation(...)`.
  2. The registration point references `FarmstormClientConfig` only (it depends on `ModConfigSpec` alone and is a common class); it is **not put into `FarmstormConfig`**.
  3. Runtime persistence goes through `ConfigValue#set(T)` + `SPEC.save()`; hand-written `Files.write` is forbidden.
  4. The range only blocks obviously bad values; the real boundary is a second clamp by the screen size at drawing / dragging time.
- **Depends on**: —
- **Acceptance**:
  1. Starting the client for the first time generates `run/config/farmstorm-client.toml` with **3** keys in the `[hud]` section and the values `enabled = true`, `x = 4`, `y = 4`; a dedicated server **does not generate** this one (the carrier face of document 30 §7 #37).
  2. Hand-edit `x = 8` and restart the client: the in-memory value reads 8 (one log line "hud 位置读入 (8, 4)").
  3. The range takes effect: change `x` to `5000` and restart, and the loaded value is clamped to **3840**.
  4. No `hud` section appears in `farmstorm-server.toml`; the two tomls do not reference each other (copying the game to someone else does not carry the position over).
  5. **3** `farmstorm.configuration.hud.*` entries each in Chinese and English, and the two key sets are still equal.
- **Rollback impact**: after a revert the key and the command have no persistence carrier (F-202 / F-205 depend on it), and F-203's rendering cannot read the switch; all three are scheduled after it.

#### F-202 · 2 `KeyMapping`s and 2 description keys (M · 1.5)

- **Goal**: register `KeyMapping`s for the first time (toggle the overlay **H**, enter dragging **J**) on the mod bus + `Dist.CLIENT`, and consume them in the existing `ClientTickEvent.Post` (D-144).
- **Files touched**: `client/FarmstormKeys.java` (new), `client/FarmstormClient.java:78-81`, `datagen/ModLangProvider.java` (+2).
- **Implementation notes**:
  1. Two `KeyMapping`s, reusing `KeyMapping.CATEGORY_GAMEPLAY` as the category (a self-made category would need 1 more `key.categories.*`, which would not add up against the 323 ledger).
  2. `@EventBusSubscriber(modid, bus = MOD, value = Dist.CLIENT)` + `RegisterKeyMappingsEvent` for the registration.
  3. Consumption: `while (TOGGLE.consumeClick()) toggleHud();` / `while (DRAG.consumeClick()) openDragScreen();`; the toggle writes `enabled`, saves it and echoes in the action bar.
- **Depends on**: F-201
- **Acceptance**:
  1. The "gameplay" page of the key-binding screen shows the two key bindings of this mod, defaulting to **H / J** (half of document 30 §7 #11).
  2. Press H: `enabled` in `farmstorm-client.toml` flips between true / false; restarting the client keeps the last state (#11).
  3. Press J: a Screen opens (before F-204 lands it is a temporary empty Screen, the criterion being "a Screen is opened and single-player pauses").
  4. **2** `key.farmstorm.core_hud.*` entries each in Chinese and English, with equal key sets; `grep -c "key.categories.farmstorm"` = **0** (no self-made category).
  5. `grep -c "RegisterKeyMappingsEvent" client/FarmstormKeys.java` = **1**.
- **Rollback impact**: after a revert the switch is available only through the command (F-205) and dragging has no entry point (F-204's Screen can only be invoked by the command); no other task is invalidated.

#### F-203 · The overlay's rendering, `CoreHudRenderer` (L · 3.0)

- **Goal**: `RenderGuiEvent.Post` self-draws a list in the top-left corner: one core per line, **8 core lines + 1 `+N` line**, a panel **93 px** high, the layout per A-41's "line height 9 / line spacing 1 / inner padding 2", switchable, clamped by the measured width (D-143 / A-34 / A-41).
- **Files touched**: `client/CoreHudRenderer.java` (new), `datagen/ModLangProvider.java` (+1 `gui.farmstorm.core_hud.self`).
- **Implementation notes**:
  1. Four early exits: no player / the switch is off → return; `hideGui` and not dragging → return; `screen != null` and it is not `CoreHudDragScreen` → return; `ClientCoreList` empty → return (**0 cores draws nothing**).
  2. Line format `#%d [%s] (%d, %d, %d) %s`: the index is 1-based, the dimension id's **path** (no new language key), self takes `gui.farmstorm.core_hud.self`, a teammate takes `ownerName`.
  3. Panel height `10n + 3` (8 lines 83 px / 9 lines 93 px, the equivalent corollary of the three numbers of document 29 §5.6); width = the longest line's `font.width` + 4; the drawing origin `(hud.x, hud.y)` is first clamped to `[0, guiWidth − width] × [0, guiHeight − height]`.
  4. Colours: background `0x80000000`, border `0xFF000000`, self `0xFF55FF55`, teammate `0xFFFFFFFF`, `+N` grey `0xFFAAAAAA`.
  5. `+N` is the plain-digit concatenation of `"+" + (total − 8)` and **adds no language key**; the index is numbered by the full ordering before truncation (with `+N` the numbering stays continuous).
- **Depends on**: F-199, F-200, F-201
- **Acceptance**:
  1. One player places 1 core: the overlay shows 1 line shaped like `#1 [overworld] (x, y, z) 自己`; the panel is **13 px** high (document 30 §7 #7).
  2. 9 allied cores: **8 core lines + 1 `+1` line** = 9 lines, total panel height **93 px**; with 8 cores, 8 lines, **83 px**, no `+N` (#10, A-34).
  3. Cross-dimension: 1 core each in the overworld + the nether → 2 lines, starting with `overworld` / `the_nether`; unchanged after teleporting (#8).
  4. 0 cores draws nothing: after destroying the only core the whole panel disappears and `+N` does not appear either (document 29 B-01).
  5. The switch: after pressing H to turn it off nothing is drawn; with `hideGui` (F1) nothing is drawn either (half of B-10).
  6. No intersection with the charge bar: with `guiHeight = 240` the overlay's bottom edge is y = 97 and the charge bar band, measured from the bottom, is `[47, 53)`, so the two do not overlap (B-11).
  7. **1** `gui.farmstorm.core_hud.self` entry each in Chinese and English.
- **Rollback impact**: after a revert the screen goes back to having no overlay, and the interaction targets of F-204 / F-205 disappear; no other task is invalidated.

#### F-204 · Drag positioning, `CoreHudDragScreen` (M · 1.5)

- **Goal**: opened with J, dragging changes `hud.x` / `hud.y`, `ESC` saves and exits; the drag state is an "edit state" and the overlay keeps drawing (D-144).
- **Files touched**: `client/CoreHudDragScreen.java` (new), `client/CoreHudRenderer.java` (the edit-state exception), `client/FarmstormKeys.java` (the DRAG branch).
- **Implementation notes**:
  1. `isPauseScreen()` returns `true` (single-player pauses automatically).
  2. `mouseClicked` starts a drag only inside the panel rectangle and records the grab offset; `mouseDragged` writes the preview coordinates and clamps them; `mouseReleased` ends the drag but **writes nothing to disk**; `onClose()` writes the config + `SPEC.save()`.
  3. The edit-state exception: the two early exits for `hideGui` and `screen != null` do not take effect in the drag state (otherwise the player could not see what he is dragging).
  4. The panel body is not drawn here: the panel is still drawn by the renderer, guaranteeing that "what you see while dragging is the final result".
- **Depends on**: F-203, F-202, F-201
- **Acceptance**:
  1. Press J: single-player pauses (the picture freezes, the world does not tick) (document 30 §7 #14).
  2. Drag to the bottom-right corner of the picture and press `ESC`: `hud.x` / `hud.y` equal the drop coordinates; re-entering the game leaves the position unchanged (#14).
  3. Drag outside the screen and release: `hud.x ≤ guiWidth − panel width`, `hud.y ≤ guiHeight − panel height`, the out-of-bounds part **0 px** (#15, try each of the four corners).
  4. Change the window size while dragging: it clamps again and the panel stays fully visible (document 29 B-08).
  5. `ESC` only saves and exits: no pause menu is raised after the Screen closes (B-09).
- **Rollback impact**: after a revert the position can only be changed by hand-editing the toml or with `hud reset` (F-205); F-203's drawing clamp is still there, and no other task is invalidated.

#### F-205 · The client command `/farmstorm hud <on|off|reset>` and 3 feedback keys (M · 1.5)

- **Goal**: client command registration (`RegisterClientCommandsEvent`), the three actions writing `enabled` / resetting the position, with the feedback sent through `sendSuccess(..., false)` (shown locally, not broadcast) (D-144).
- **Files touched**: `client/CoreHudClientCommands.java` (new), `client/FarmstormClient.java` (the `RegisterClientCommandsEvent` subscription), `datagen/ModLangProvider.java` (+3).
- **Implementation notes**:
  1. `dispatcher.register(Commands.literal("farmstorm").then(Commands.literal("hud")...))`, with the three subcommands **not** carrying `requires(...)`.
  2. `reset` only resets the position (`x = y = 4` and `save()`), and **does not touch `enabled`**.
  3. The feedback is `sendSuccess(..., false)`, the opposite of the deleted server-side command (`true`).
  4. This command and the server registration point do not share the root node's semantics (the server side no longer has `/farmstorm`, F-197).
- **Depends on**: F-201
- **Acceptance**:
  1. `/farmstorm hud off` → `enabled = false` in `farmstorm-client.toml`; `on` → `true`; `reset` → `x = 4`, `y = 4` and `enabled` unchanged (document 30 §7 #13).
  2. In the single-player client, completing `/farmstorm ` offers only `hud`, under which there are only `on` / `off` / `reset`; `/farmstorm farmlandlimit 5` fails (the client half of #12).
  3. The feedback appears only in the local chat bar and does not enter the server broadcast (the second client cannot see it).
  4. **3** `command.farmstorm.hud.*` entries each in Chinese and English, with equal key sets.
  5. `grep -c "sendSuccess" client/CoreHudClientCommands.java` = **3**, and the second argument is `false` every time.
- **Rollback impact**: after a revert the switch is only the key binding (F-202) and `reset` loses its entry point; no other task is invalidated.

### Phase 31 · Closing and Full Acceptance (F-206 ~ F-212)

Phase goal: close in one pass the language-key ledger, the release artifact and the two criterion sets (the 38 acceptance entries of document 30 §7 + the 39 boundaries of document 29 §11), producing one re-checkable acceptance record.

#### F-206 · The language-key ledger 323 × 2 and the `ModLangProvider` comment (M · 1.5)

- **Goal**: reconcile this version's language-key additions and deletions in one pass: **315 − 2 + 10 = 323**, the Chinese and English key sets identical character for character, and the key-count narrative in `ModLangProvider`'s class comment consistent with the artifact (document 30 §4.3 / item 7 of §9 / RT-40).
- **Files touched**: `datagen/ModLangProvider.java` (`:15`, the comment at `:27-32`), `src/generated/resources/assets/farmstorm/lang/zh_cn.json` / `en_us.json` (the datagen artifacts).
- **Implementation notes**:
  1. Re-check class by class: `farmstorm.configuration` **169** (= 166 server + 3 client), `gui.farmstorm` **9**, `command.farmstorm` **3**, `key.farmstorm` **2**, and the other categories as before.
  2. The narrative of the "299 + 16 = 315" kind in the class comment becomes the post-change formula; no key is added or removed (the additions and deletions have already been done by F-182 / F-192 / F-197 / F-201 / F-202 / F-203 / F-205 individually).
  3. `en_us`'s wording style does not move; the two key sets must be identical character for character.
- **Depends on**: F-182, F-192, F-197, F-201, F-202, F-203, F-205
- **Acceptance**:
  1. **323** keys in each of the two lang files, with identical key sets (the tool's §3 section "lang 键集一致，共 323 键"; document 30 §7 #5).
  2. `grep -c '"farmstorm.configuration.'` is exactly **169**; `grep -c farmlandlimit` is **0** in both (#5).
  3. The prefix distribution adds up line by line to 323 (`169 + 36 + 32 + 26 + 16 + 13 + 9 + 6 + 4 + 3 + 2 + 2 + 3 + 1 + 1`).
  4. The class comment agrees with the artifact: the four numbers appearing in the comment (315, 323, 169, 2) each have a readable counterpart in the artifact; `grep -c "299 + 16 = 315"` = **0**.
- **Rollback impact**: a revert only puts the comment back to the old narrative and does not change the artifact; the key sets are decided by the individual tasks' artifacts, so reverting this task loses no key.

#### F-207 · The build artifact and the `dist/` release (S · 0.5)

- **Goal**: produce the releasable `farmstorm-0.1.1+mc1.21.1.jar` and the `dist/` copy, with the `jar tf` entries matching item by item (1 replaced PNG, no new PNG / JSON), and `chunks.dat` holding no core coordinate (D-124 / D-147 / A-38).
- **Files touched**: `build/libs/`, `dist/` (artifacts, not committed).
- **Implementation notes**: `.tools\gw.cmd build` → copy the jar and the `-sources.jar` into `dist/`; check `META-INF/neoforge.mods.toml`'s version, the size of `textures/item/wild_apple.png`, and that no image entry is new.
- **Depends on**: F-176, F-179, F-198, F-199, F-203, F-206
- **Acceptance**:
  1. `dist/farmstorm-0.1.1+mc1.21.1.jar` exists; `jar tf` holds **1** entry for `assets/farmstorm/textures/item/wild_apple.png`, and this version adds no PNG entry (compared with the pre-change entry list, only this one image's content differs).
  2. `jar tf` holds **1** entry each for `farmstorm/network/CoreListPayload.class`, `farmstorm/client/CoreHudRenderer.class` and `farmstorm/outpost/CoreChunkTickets.class`.
  3. `META-INF/neoforge.mods.toml`'s `version="0.1.1"`; `build/libs` holds no leftover 0.1.0 / 0.0.1 artifact.
  4. The `Forced` array of `world/data/chunks.dat` (including `DIM-1` / `DIM1`) contains no core's `ChunkPos.toLong()` (document 30 §7 #36).
  5. It starts as a pure server: a dedicated server loads this jar and completes startup, with no `NoClassDefFoundError` in `latest.log`.
- **Rollback impact**: affects only the release artifact, not the source; after a revert `dist/` goes back to the previous version's jar.

#### F-208 · Document 30 §7's #1 – #19 acceptance (M · 1.5)

- **Goal**: run the first 19 entries of `30-TECH-0.1.1.md` §7 one by one and produce a re-checkable acceptance record (no code change).
- **Files touched**: the acceptance record (new, written to `.work/` or the commit message); no source change.
- **Implementation notes**: each entry gets the four columns "action / expected / actual / conclusion"; the observation locations follow §0.3's seven kinds; the entries involving the tool (#1's registry face, #5) cite `FAIL 0` after A-40 has closed.
- **Depends on**: F-182, F-188, F-189, F-190, F-191, F-192, F-193, F-194, F-197, F-199, F-200, F-201, F-202, F-203, F-204, F-205, F-206
- **Acceptance**:
  1. #1 – #19 each have a measured reading and an observation location; **0** failing; any failure sends you back to the corresponding task to redo it, **never to a re-judging of the criterion on the spot**.
  2. #1's startup log contains verbatim "自检 注册对象 93 个：…" and the twelve classes are equal item by item.
  3. #5's two lang files hold **323** keys, `farmstorm.configuration.` has **169** entries, and `farmlandlimit` has **0** hits.
  4. #19's N and M are identical across two startups (A-47), and the blocks that cannot be backfilled are still `minecraft:farmland`.
  5. The record is committed as a table with all four columns per entry; "passed" alone is not allowed.
- **Rollback impact**: no code change, no rollback surface.

#### F-209 · Document 30 §7's #20 – #38 acceptance (M · 1.5)

- **Goal**: run the second 19 entries of §7 one by one (including the three migration branches, A-35's downgrades, the chives field's timing, the ticket release, the three Pumpkin Turret items, the red-line scan and the A-40 tool wording).
- **Files touched**: the acceptance record; no source change.
- **Implementation notes**: four columns per entry; #38's criterion is `FAIL 0` + exit code 0 + config 166 "after the tool is reworked" (A-40); #37's three counts are read in one go.
- **Depends on**: F-177, F-179, F-180, F-181, F-182, F-183, F-184, F-185, F-186, F-192, F-196, F-198, F-199, F-200, F-203
- **Acceptance**:
  1. #20 – #38 each have a measured reading; **0** failing.
  2. #22 turns into `minecraft:dirt` (the same as up close) with no drops, and the log does **not** show "迁移失败且区块未加载：只摘绑定"; that string exists at `OutpostCoreBlockEntity.java:374` and does not appear in normal operation, confirmed by code review.
  3. #30's two rounds (200 / 100) both hold; #32's unload-and-return leaves no residue.
  4. #33's F3 `Light` = **15**; #34's three readings (≈2000 ms / **3** points / ≈ **33** ticks).
  5. #36's `chunks.dat` holds no core coordinate; #37's `@Mixin` **0** hits, `CustomPacketPayload` exactly **1** file, and the tool `93 / 93`; #38's tool output `结论：FAIL 0 项`, exit code 0, config section **166**.
- **Rollback impact**: no code change, no rollback surface.

#### F-210 · Document 29 §11's B-01 – B-20 boundary re-run (M · 1.5)

- **Goal**: run the first **20 entries** of document 29 §11's boundaries (B-01 ~ B-20: 14 overlay entries + the first 6 farmland entries) as control experiments **one by one**, with one acceptance item per boundary; **14 of them** (B-01, B-02, B-03, B-05, B-08, B-10, B-11, B-13, B-14, B-15, B-17, B-18, **B-19, B-20**) have **no** corresponding entry in document 30 §7 (#20 writes only "migration succeeds / turns to dirt" and does not distinguish "the successor core is full" from "not allied"), so this task is their only criterion (A-46).
- **Files touched**: the boundary record `.work/v011-verify/F-210.md`; screenshots / screen recordings go into `.work/v011-verify/`; no source change.
- **Implementation notes**:
  1. One acceptance item per boundary, with the number appearing in the acceptance item; the criterion falls on one of §0.3's seven locations.
  2. **The purely visual ones** (B-01, B-02, B-10, B-11) are written as "human visual inspection + a screenshot saved to `.work/v011-verify/<B-xx>.png`"; **they may not be skipped and may not be written as just "passed"**.
  3. The two where readings are taken side by side (B-18's quota face and trample face) must be given in the same record segment, to avoid verifying only half.
  4. A-49 (farmland at the same coordinate across dimensions colliding) is a registered **known limitation**, is **not** among these 39, and this task sets no pass item for it — only one line at the end of the record.
- **Depends on**: F-189, F-191, F-193, F-194, F-195, F-196, F-199, F-200, F-201, F-203, F-204
- **Acceptance** (**20 items = B-01 ~ B-20 one-to-one**):
  1. **B-01** 0 cores draws nothing: after destroying the only core neither the panel nor `+N` appears (human visual inspection + screenshot `.work/v011-verify/B-01.png`).
  2. **B-02** the height with 1 core: place only 1 core, and the panel is **13 px** high (screenshot + pixel measurement, `.work/v011-verify/B-02.png`).
  3. **B-03** a teammate's core + the teammate offline: B places a core and then logs off; A's overlay still has that line and the name is B's `ownerName`.
  4. **B-04** switching dimension: overworld → nether → end, one switch each; the line count and order do not change, and the dimension marker at the start of a line is still the **core's own** dimension.
  5. **B-05** the first frame after reconnecting: after logging back in the first frame is 0 lines, and the next refresh brings the full list (two frames captured + the order of the log timestamps).
  6. **B-06** more than 8 cores: 8 core lines + 1 `+N` line, N = the total − 8.
  7. **B-07** dragging off-screen: try each of the four corners, `hud.x ≤ guiWidth − width`, `hud.y ≤ guiHeight − height`, the out-of-bounds part **0 px**.
  8. **B-08** changing the GUI scale while dragging: scale 2 → 4, and the panel is re-clamped and stays fully visible.
  9. **B-09** `ESC` only saves: no pause menu is raised after the Screen closes, and `hud.x` / `hud.y` are already the drop values.
  10. **B-10** F1 / the inventory: nothing is drawn with `F1` or with the inventory opened by `E`; in the drag state (J) it is still drawn (screenshots `.work/v011-verify/B-10a.png` / `B-10b.png`).
  11. **B-11** against the charge bar: with `guiHeight = 240` the overlay's bottom edge is y = 97 and the charge bar band, measured from the bottom, is `[47, 53)`, so the two elements do not overlap (measure pixels on the screenshot).
  12. **B-12** two cores at the same tick: offline, set both cores' `gameTime` to the same value → order by dimension id → coordinate lexicographic order; two restarts give the same order.
  13. **B-13** a hostile core / the core item: the second client's (different team's) core does not appear in the list; holding the core item produces no line.
  14. **B-14** the first frame has not received the list: treat it as 0 lines at the instant of a reconnect and do not draw the previous session's data.
  15. **B-15** tilling outside any covering core: stand outside the cores' 3×3 and till → it fails, the block does not change, the message is `no_core`, and the durability does not change.
  16. **B-16** all covering cores are full: after 16 cells the 17th is refused + `QUOTA_FULL`, and `ItemDamage` is the same before and after.
  17. **B-17** the nearest is full but another core has a slot: bind to the one with a slot (`bindings.corePos` = the other core's coordinates).
  18. **B-18** switching teams: the quota and the overlay recompute by the new team at once, **while the trample face is still protected** (three readings side by side, the wording per A-45).
  19. **B-19** core destroyed + the successor core is full: that field becomes `minecraft:dirt` (it does not wait for a slot).
  20. **B-20** core destroyed + the successor core is not allied: it becomes dirt, and **the farmland's owner is not notified** (no chat, no packet-change hint).
- **Rollback impact**: no code change, no rollback surface.

#### F-211 · Document 29 §11's B-21 – B-39 boundary re-run (M · 1.5)

- **Goal**: run the last **19 entries** of document 29 §11's boundaries (B-21 ~ B-39: the last 5 farmland entries + 3 Pumpkin Turret entries + 7 Friend Chives entries + 4 hay / core / apple entries) as control experiments **one by one**, with one acceptance item per boundary; **7 of them** (**B-21**, B-24, B-25, B-26, B-31, B-32, B-35) have **no** corresponding entry in document 30 §7 (#21 writes only "cannot migrate + the chunk is in memory → dirt" and does not cover the "the plot is no longer farmland" branch), so this task and F-210 together come to exactly 21 entries (14 + 7, A-46).
- **Files touched**: the boundary record `.work/v011-verify/F-211.md`; screenshots / screen recordings / the sha256 list go into `.work/v011-verify/`; no source change.
- **Implementation notes**:
  1. One acceptance item per boundary, with the number appearing in the acceptance item; the criterion falls on one of §0.3's seven locations.
  2. **The purely sensory ones** (B-36's five sound types, B-38's spot check) are written as "human visual / human auditory inspection + a screenshot or recording saved to `.work/v011-verify/<B-xx>-<sub-item>.*`"; **skipping is not allowed**.
  3. B-24 / B-25 depend on building "a legacy save + the capacity hard cap", so prepare a local save with 20 old fields + 1 core first and run it, writing the operation sequence into the record.
  4. A-49 (farmland at the same coordinate across dimensions colliding) is a registered **known limitation**, is **not** among these 39, and this task sets no pass item for it — only one line at the end of the record.
- **Depends on**: F-178, F-179, F-180, F-181, F-182, F-184, F-185, F-186, F-188, F-190, F-196
- **Acceptance** (**19 items = B-21 ~ B-39 one-to-one**):
  1. **B-21** the plot is no longer farmland when the core is destroyed: `/setblock stone` on that cell and then destroy the core → it is skipped, unbound, and the stone does not change.
  2. **B-22** the chunk boundary when a core is destroyed (A-60): walk 200 blocks away and destroy the core with `/execute in` → the field turns into `dirt` (the same as up close) with no drops; "unbind only" is an unreachable defensive branch, and the log string at `OutpostCoreBlockEntity.java:374` is confirmed by code review to exist and does not appear in normal operation.
  3. **B-23** a core is already full during the backfill: that land stays `farmland`, unbound, and **can be trampled** (control: the one bound in the same batch cannot be trampled).
  4. **B-24** a legacy save over capacity: 20 old fields + 1 core (capacity 16) → 16 are protected and 4 are not (trampling each as the control), **0** turn into dirt.
  5. **B-25** recovery after a failed backfill: if the next startup still has no free slot it is still unprotected; after digging it up and tilling once it regains protection (verify each of the two paths once).
  6. **B-26** the target dies in mid-flight: `target` is emptied, the bullet flies straight out and disappears on hitting a block, and `latest.log` has no NPE.
  7. **B-27** point-blank (`dist < 0.45`): it converges and hits, 5 shots out of 5.
  8. **B-28** an old toml is not migrated: keep 0.1.0's toml → `fireIntervalTicks = 60`, `projectileDamage = 1`, and `bulletSpeed` is automatically filled in as **0.45**.
  9. **B-29** an empty drop: 0 mobs inside the sphere means no sound is played, **but the field still lasts the full duration**, and the entity disappears on tick `effectTicks`.
  10. **B-30** entering midway: it gets `effectTicks − x` ticks and still expires on tick `effectTicks`.
  11. **B-31** leaving the sphere midway: the effect runs out with the player and **does not end early** (the remaining duration still decreases towards the original expiry instant).
  12. **B-32** the thrower himself standing inside the sphere: he is caught as well (the Q-Bounce `Duration > 0` of `/data get`).
  13. **B-33** chunk unload: the field ends and does not continue after a reload (the entity does not exist).
  14. **B-34** self-destruction on tick `effectTicks`: `/data get entity @e[type=farmstorm:friend_chives]` finds nothing.
  15. **B-35** two fields overlapping: take the stronger one (with the same amplifier, compare the duration), and the earlier victim's expiry instant is **not shortened**.
  16. **B-36** the five sound types: breaking / placing / stepping / mining / falling, one positive-negative control each (human listening + `/playsound` control; recordings saved to `.work/v011-verify/B-36-<type>.mp4`).
  17. **B-37** lighting and mob spawning: F3 `Light = 15`, there is light within 15 blocks; in the centre of the light **0** mobs in 100 seconds and ≥ 1 beyond 15 blocks.
  18. **B-38** the other 24 still carry the "+": the sha256 list of the 24 PNGs + 3 sampled visually confirmed to still have the green cross at the bottom-right corner (screenshots `.work/v011-verify/B-38-*.png`).
  19. **B-39** the generator and the release artifact (A-61): **this section does not re-run the generator** (not a release action); the committed `wild_apple.png` is **64×64 / 6,946 B with the source image's sha256**, and the other 24 are identical to the pre-commit ones image by image (by the bytes in git); a re-run strips the green cross off those 24, which is D-125's **expected outcome** and is not counted as a failure.
- **Rollback impact**: no code change, no rollback surface.

#### F-212 · The full summary and the upstream-consistency signature (L · 3.0)

- **Goal**: turn the 38 acceptance entries + 39 boundaries + the status of the 37 tasks into one conclusion, and re-check item by item the **27** adjudications of this file's §5 (A-30 ~ A-56, of which A-44 is void) and the seven tables of §6.
- **Files touched**: the summary record; no source change.
- **Implementation notes**: read the five counts in one go; re-check each of the seven tables' coverage counts; give each adjudication its implementing task and back-fill status; list the still-open disagreements as 0.
- **Depends on**: F-208, F-209, F-210, F-211
- **Acceptance**:
  1. 38 + 39 = **77 entries** each with a conclusion, **0** failing; all 37 tasks have a "done / not done" status.
  2. The five counts read in one go: registration **93**, server config **166**, client config **3**, language keys **323 × 2**, tasks **37**.
  3. Each of the seven tables holds: table A **44/44** (+ 2 supplementary rows), table B **20** items, table C **7** items, table D **5** items, table E **11/11**, table F **8/8**, table G **17** rows (of which the **8** new adjudications each have an implementing task, and A-44 is void).
  4. The red line keeps only "no Mixin": `grep -rn "@Mixin\|mixins.json" src/main/java` **0 hits**; `CustomPacketPayload` exactly **1** file; `GameRules.register` still **0 hits** (the engineering constraint is kept, D-146's supplementary item).
  5. `python -X utf8 tools/verify_project.py` → `结论：FAIL 0 项`, exit code 0, config section **166**, `93 / 93`, `323 键`.
- **Rollback impact**: no code change, no rollback surface.

---

## 3. Phases and Milestones

The six Phases map one-to-one onto the six milestones (M22 ~ M27). Each Phase gives one **exit criterion**: after all tasks of that Phase are committed, what observable state the system is in.

| Phase | Name | Tasks | Task count | Person-days | Milestone |
|---|---|---|---|---|---|
| 26 | Baseline and Tool Chain | F-176 ~ F-177 | 2 | 2.0 | **M22** |
| 27 | Textures, Values and Block Properties | F-178 ~ F-184 | 7 | 11.5 | **M23** |
| 28 | Friend Chives' Persistent Field | F-185 ~ F-186 | 2 | 4.5 | **M24** |
| 29 | The Farmland–Core Binding Regime | F-187 ~ F-198 | 12 | 21.5 | **M25** |
| 30 | The Overlay and the Data Face | F-199 ~ F-205 | 7 | 15.0 | **M26** |
| 31 | Closing and Full Acceptance | F-206 ~ F-212 | 7 | 11.0 | **M27** |
| **Total** | | **F-176 ~ F-212** | **37** | **65.5** | **M22 ~ M27** |

**Phase 26 · Baseline and Tool Chain → M22**
Exit criterion: `build/libs/farmstorm-0.1.1+mc1.21.1.jar` exists and `neoforge.mods.toml`'s `version=0.1.1`; `tools/verify_project.py`'s expected config set is computed from document 30 §4.1 (**58 groups / 166 keys**) with `farmland.limitPerPlayer` removed, and the intermediate state has only two key-level differences (`bulletSpeed` missing, `farmland.limitPerPlayer` extra).

**Phase 27 · Textures, Values and Block Properties → M23**
Exit criterion: `wild_apple.png` is **64×64 / 6,946 B**, and deleting the image and re-running the generator neither falls back nor reports a size assertion; `add_plus` / `build_marker` / `compose_icon` in the generator have **0 hits** and the other 24 PNGs' bytes are unchanged; the Compressed Hay Block's **five** sound types are all stone; the core's F3 `Light = 15`; after deleting the old toml and starting the server, `[pumpkin_turret]`'s 5 keys are 40 / 3 / 0.45 / 15 / 1; 5 consecutive shots at a target within 1 block all hit, 3 points each.

**Phase 28 · Friend Chives' Persistent Field → M24**
Exit criterion: after landing it is a field scanned every tick for `friend_chives.effectTicks` ticks (default 200, configurable 100 / 1000), and the entity disappears on tick `effectTicks`; an empty drop plays no sound and a hit plays it once only; a chunk unload and return leaves no residue and no revival.

**Phase 29 · The Farmland–Core Binding Regime → M25**
Exit criterion: the whole binding chain runs — tilling binds the nearest allied core, the 17th cell is refused, the core's owner offline is still protected, destroying a core migrates first (rebind / into dirt / unbind only, three branches), a legacy save's startup backfill gives reproducible N/M, `/farmstorm farmlandlimit` is gone, the `[farmland]` section is gone with the group count at **58**, and the core's chunk has a temporary ticket while `chunks.dat` holds no core coordinate; at the end of this Phase `verify_project.py` reaches `FAIL 0` for the first time (A-40 closes).

**Phase 30 · The Overlay and the Data Face → M26**
Exit criterion: with 1 core each in the overworld + the nether the overlay is **2 lines**, with 9 cores it is **9 lines 93 px** (8 + `+1`); **H** toggles and persists, **J** drags and `ESC` saves, and `/farmstorm hud on|off|reset` all three change `farmstorm-client.toml`; nothing survives a server switch; the whole repository has exactly 1 `CustomPacketPayload` file and the client never replies.

**Phase 31 · Closing and Full Acceptance → M27**
Exit criterion: **323** keys in each of the two language files with identical key sets; `dist/farmstorm-0.1.1+mc1.21.1.jar` is releasable and starts as a pure server; all **38 entries** of document 30 §7 and all **39 entries** of document 29 §11 pass (0 failing); the red line keeps only "no Mixin"; `verify_project.py` outputs `FAIL 0` / exit code 0 / `93 / 93` / `166` / `323 键`.

---

## 4. Workload Summary

### 4.1 By Phase

| Phase | Task count | S | M | L | Person-days |
|---|---|---|---|---|---|
| 26 Baseline and Tool Chain | 2 | 1 | 1 | 0 | `1 × 0.5 + 1 × 1.5` = **2.0** |
| 27 Textures, Values and Block Properties | 7 | 2 | 3 | 2 | `2 × 0.5 + 3 × 1.5 + 2 × 3.0` = **11.5** |
| 28 Friend Chives' Persistent Field | 2 | 0 | 1 | 1 | `1 × 1.5 + 1 × 3.0` = **4.5** |
| 29 The Farmland–Core Binding Regime | 12 | 1 | 8 | 3 | `1 × 0.5 + 8 × 1.5 + 3 × 3.0` = **21.5** |
| 30 The Overlay and the Data Face | 7 | 0 | 4 | 3 | `4 × 1.5 + 3 × 3.0` = **15.0** |
| 31 Closing and Full Acceptance | 7 | 1 | 5 | 1 | `1 × 0.5 + 5 × 1.5 + 1 × 3.0` = **11.0** |
| **Total** | **37** | **5** | **22** | **10** | **65.5** |

### 4.2 Totals (Two Numbers + the Arithmetic)

- **Raw person-days**: `5 × 0.5 + 22 × 1.5 + 10 × 3.0 = 2.5 + 33.0 + 30.0 = **65.5 person-days**`
- **With a 25% buffer**: `65.5 × 1.25 = **81.875 person-days**`

Three cross-checks:

1. Complexity summary: S 5 + M 22 + L 10 = **37** = the task count;
2. Person-day summary: `2.5 + 33.0 + 30.0 = 65.5` = the sum of the six Phase subtotals `2.0 + 11.5 + 4.5 + 21.5 + 15.0 + 11.0 = 65.5`;
3. Buffer: `65.5 × 1.25 = 81.875`, the same as the milestone table (§3) added row by row.

> The buffer absorbs rework and debugging only, not scope change. The 0.1.0 comparison: 40 tasks / 63.0 raw person-days / 78.75 with the buffer; this version's 37 tasks have slightly more raw person-days because the two schema changes and the four "firsts" (the first custom packet, the first CLIENT config, the first batch of `KeyMapping`s, the first client command) each count one tier up.

### 4.3 Critical Path (the Longest Predecessor per the Dependency Graph)

```
F-199 network packet 3.0                        (cumulative 3.0)
 └─ F-200 broadcaster 3.0                        (cumulative 6.0)   ← also needs F-189's comparison key landed first
     └─ F-203 overlay rendering 3.0               (cumulative 9.0)
         └─ F-206 language-key ledger 1.5         (cumulative 10.5)
             └─ F-208 §7's #1–#19 1.5             (cumulative 12.0)
                 └─ F-212 summary signature 3.0   (cumulative 15.0)
```

**Critical path = 15.0 person-days / 6 tasks**, **22.9 %** of the 65.5 total. This chain is the result of a pure dependency derivation (each node takes the maximum cumulative value of all its predecessors): the overlay's data face (F-199 → F-200) → rendering (F-203) → the language-key ledger (F-206) → the first acceptance batch (F-208) → the summary (F-212). **The overlay line is the longest one, not the binding line** — the binding line (F-188 → F-195 → F-196 → F-211) is only `3.0 + 1.5 + 3.0 + 1.5 = 9.0` person-days, because it has many branches and a high degree of parallelism.

**A tied second 15.0**: `F-199 → F-200 → F-203 → F-204 → F-210 → F-212`. The two chains fork after `F-203` and each converges back on F-212; when scheduling, treat F-204 and F-206 as the same layer (both wait only on F-203).

The next-longest chains (again taking the longest predecessor per dependency, not entering the critical path):

| Rank | Chain | Person-days |
|---|---|---|
| 3 | F-188 → F-195 → F-196 → F-211 → F-212 | 12.0 |
| 4 | F-182 → F-183 → F-184 → F-209 → F-212 | 10.5 |
| 5 | F-178 → F-179 → F-211 → F-212 | 9.0 |

**The critical path is less than a quarter of the total**, which says that this version's duration is decided by **total workload and parallelism**, not by the longest chain: to shorten the calendar duration, add parallelism first (4 of Phase 29's 12 tasks are parallelizable forks), and only then squeeze this overlay chain. Conversely, **F-199 → F-200 → F-203 (9.0 person-days) cannot be squeezed**: it is this version's only serial chain of the form "first the data face, then the picture, then the acceptance", and squeezing any link leaves the three later acceptance batches with no criterion at all.

### 4.4 Parallelism

| Cluster | Location | Parallelizable tasks | Parallelism | Notes |
|---|---|---|---|---|
| A | Phase 26 ~ 27 | F-178, F-180, F-181, F-182 | 4 | F-176 → F-177 is a serial chain (change the version first, then the tool); F-178 / F-180 / F-181 / F-182 wait only on F-177 (none of them changes the tool), and the four share no file |
| B | Phase 27 | F-183 → F-184 | 1 | The two tasks change the same class and are serial by nature; they can run parallel to cluster A |
| C | Phase 28 ~ 29 | F-185 → F-186 and F-187, F-188, F-189 | 4 | The chives-field line and the binding line do not depend on each other and share only the baseline after F-177 |
| D | Phase 29 | F-190, F-191, F-195 | 3 | All three fork after F-188; F-191 → F-192 → F-197 is one deletion chain (of which F-191 -> F-192 is a **compile order**, see A-56) and F-193 → F-194 → F-196 another |
| E | Phase 30 | F-201, F-199 | 2 | CLIENT config and the network packet do not depend on each other; F-202 ~ F-205 close in turn onto F-203 |
| F | Phase 31 | F-207, F-208, F-209, F-210, F-211 | 5 | After F-206 it forks into five: F-207 the release chain, F-208 the first acceptance batch, F-209 the second, F-210 / F-211 the boundaries; F-212 is the mandatory join point (it waits for all four acceptance production lines) |

**The parallelism ceiling is 4** (clusters A / C with 4 each). In practice it is constrained by **same-file conflicts**: F-178 and F-179 both change `tools/gen_textures.py` (already chained into 178 → 179); F-183 and F-184 both change `TurretBullet.java` (already chained into 183 → 184); F-188 / F-190 / F-191 all change `FarmlandLedger.java` (the three sit in different places of Phase 29, ordered 188 → 189 / 191); F-193 / F-195 both change `FarmlandGuard.java` (ordered 193 → 195); F-199 / F-200 both change `network/` (ordered 199 → 200). Under the convention "one task appends only its own segment", **3 lanes** can run in parallel reliably: `65.5 ÷ 3 ≈ 21.8 person-days`, ≈ **27.3 person-days** after the buffer (about 5.5 weeks @ 5 days/week); single-person serial work is `81.875` person-days.

---

## 5. Adjudications A-30 ~ A-56: Collection and Back-fill Status (27 Entries)

This section collects in three parts: the **12 rows in the table** reproduce A-30 ~ A-41 of `.work/v011-verdicts.md` as they stand; **§5.1** holds the 8 entries newly established / re-judged by this file (A-42 ~ A-49, of which **A-44 is void and superseded by A-47**); **§5.2** holds the other 7 (A-50 ~ A-56: A-50 / A-51 / A-53 / A-55 are registrations on the 28 / 30 side, A-52 lands in document 30, and A-54 / A-56 are document 31's own correction sheets). Total **27 entries (A-30 ~ A-56), contiguous with no gap**.

| ID | Conclusion (one sentence) | Landed on which task | Back-fill target document | Status |
|---|---|---|---|---|
| **A-30** | Once the Pumpkin Turret's bullet speed becomes config, the launch point at `behavior/impl/PumpkinTurret.java:75` **must read the config in the same change** — deleting the constant is not a single-point change | **F-183** (both read points in the same commit) | document 28 D-129 | ledger: **back-filled** ｜ review by this draft: back-filled |
| **A-31** | Changing the Compressed Hay Block's sound affects **five** sound types (breaking / placing / stepping / mining / falling), so acceptance has 2 items more than originally estimated | **F-180** (five positive-negative controls) | document 28 D-130 supplementary item | ledger: **back-filled** ｜ review by this draft: back-filled |
| **A-32** | `gen_textures.py`'s **size assertion and `expected_size()` are relaxed together**, otherwise the 64×64 passthrough output hits `SystemExit` | **F-179** | document 28 §3.2 row 14 + §3.3 row 8 | ledger: **back-filled** ｜ review by this draft: back-filled |
| **A-33** | The wording of Friend Chives' "200": **both the field length and the application base take `friend_chives.effectTicks`**, and the amount applied = `effectTicks − x` | **F-185** (only one source, `fieldLength`, is left in the code) | document 28 D-131 | ledger: **back-filled** ｜ review by this draft: back-filled |
| **A-34** | The overlay's "8 lines" means **8 core lines**, `+N` **adds 1 more line**, so the maximum is **9 lines** (panel height 83 px → 93 px) | **F-203** | item 4 of document 28 D-143's conclusion | ledger: **back-filled** ｜ review by this draft: back-filled |
| **A-35** | When a core is destroyed and "no successor is found + the chunk the farmland is in is not loaded" → **unbind only, the world does not move** | **F-196** (two exits plus one defensive branch) | document 28 D-136 supplementary item | ledger: **back-filled** ｜ review by this draft: back-filled; **narrowed by A-60: the third branch degrades to a defensive branch** (the ticket always covers the same 3×3 ⇒ unreachable in normal operation, so core removal is really two levels) |
| **A-36** | The legacy backfill's traversal order was **originally** set to the record order of `FarmlandLedger.byOwner` (`LinkedHashSet`, `FarmlandLedger.java:51`); **that wording has been superseded by A-47** (A-44 is void, see below) | **F-190** (changed to A-47's total-order sort), **F-188** (the v2 on-disk form attaches no semantics to the order any more) | document 28 D-137 supplementary item | ledger: **back-filled** ｜ review by this draft: back-filled; **the order part has been covered by A-47** |
| **A-37** | **An acceptance assertion may not say "the call-site count = 1"**; once the binding regime introduces "unbind", the write entries are no longer unique | **this file §0.1's three hard conventions** (all 37 tasks follow them) | document 31's acceptance chapter + document 30 §7 | ledger: **to be back-filled (closed when document 31 is drafted)** ｜ review by this draft: **this draft is the closing point**, landed in §0.1; the table header discipline ① of document 30 §7 is already written to the same wording |
| **A-38** | The on-disk file of forced loading is **`world/data/chunks.dat`** (not `level.dat`); `setChunkForced()` and `TicketController#forceChunk` **both write it ⇒ both are rejected** | **F-198** (a bare `TicketType` + `addRegionTicket` only) | document 28 D-145 + fact card §2 #24 + document 30 §2.5 | ledger: **back-filled in 28 / 30 pending** ｜ review by this draft: document 30 §2.5.1-2, §3 row 39 and §7 #36 are already written, **back-filled** |
| **A-39** | The implementation wording of "a tie goes to the earlier placement" = **`placedGameTime` ascending → dimension id → coordinate lexicographic order**; **it may not depend on any Map / List insertion order**; the carrier of the value loads no chunk | **F-189** (the carrier lands in the index), **F-193** (candidate comparison), **F-190** (the backfill uses it too), **F-200** (the overlay uses it too) | document 28 D-135 + document 29 §3.4 + document 30 §2.2 / §7 #9 | ledger: **28 and 29 back-filled / 30 pending** ｜ review by this draft: document 30 §2.2.10 is already written, **back-filled** |
| **A-40** | `verify_project.py`'s config expectation source must be re-pointed at v0.1.1; **before it is reworked, "the tool is all green" is unusable** | **F-177** (the rework), **F-192** (the closing point), F-208 / F-209 / F-212 (the citation face) | document 30 §3 + §7 + document 31's tasks | ledger: **30 and 31 pending** ｜ review by this draft: document 30 §3 row 43 + §7 #38 are already written; document 31 is this draft's §0.4, **back-filled** |
| **A-41** | The overlay's layout is unified to document 29's decomposition (**line height 9 + line spacing 1 + inner padding 2**), and document 30's `10n + 3` is an equivalent corollary | **F-203** (implemented per the three numbers of document 29 §5.6) | document 30 §2.4.4 + document 29 §5.6 | ledger: **to be back-filled** ｜ review by this draft: both document 29 §5.6's "single wording (A-41)" segment and document 30 §2.4.4 are already written per A-41, **back-filled** |

### 5.1 The 8 Entries Newly Established / Re-judged by This File (A-42 ~ A-49, of which A-44 is void)

**A-42 · The 44-row change surface of document 30 §3 omits the action surface "delete the config `farmland` group"**

- **Conclusion**: deleting the field at `FarmstormConfig.java:226` and the whole group at `:904-908`, and deleting the generation segment of `farmstorm.configuration.farmland.limitPerPlayer`, is still an action surface of this version; its authoritative source is row 3 of document 30 §2.3 and §4.1's "the deleted group registered separately" table, and it is **not among the 44 rows**. The 44 rows are not renumbered; this file registers it as a **supplementary row** at the end of table A, and the action-surface total is counted as **47 = 44 + 3**.
- **Basis**: row 18 of `BRIEF` §3.2 ("`FarmstormConfig.java:904,907` → delete the whole group") and row 1 of document 28 §3.3 both require the deletion; document 30 §2.3 / §4.1 also write it, and only the 44 rows of §3 lack this row — a downstream reader of §3 alone would miss the key deletion entirely.
- **Implementing task**: **F-192** (table A's supplementary row).
- **Back-fill target**: document 30 §3 (add a note under the table or a 45th row) + document 32's pitfalls chapter.
- **Status**: **to be back-filled** (newly established by this draft).

**A-43 · The wording of "loads only 1 chunk": the tick face is 1 chunk; the load face is necessarily 3×3**

- **Conclusion**: D-145's "that 1 chunk" exactly means "**only 1 chunk is ticked** (ticket level 32, block-entity tick)"; the immediately adjacent ring of 8 chunks is loaded to FULL by ticket-level propagation (level 33, loaded but not ticking), and the ring beyond that is no longer loaded. So "not 3×3" holds on the tick face and **does not hold on the load face**. Documents and acceptance always write the tick face: **forbidden** to assert that only 1 chunk gets loaded (that line is necessarily red).
- **Basis**: document 28 D-145's "load only that 1 chunk (not 3×3)" and document 30 §2.5.2's "an honest description of the load face" (`distance = 1` ⇒ the centre 32, the neighbours 33); the two conflict literally, and document 30 §8 ③ lists "whether the actual load face is 3×3" as to be verified — the wording has to be fixed first, otherwise the acceptance cannot be written.
- **Implementing task**: **F-198** (the implementation and the three-layer screenshots), **F-209** (#36's assertion rewritten for the tick face).
- **Back-fill target**: document 28 D-145 supplementary item + document 29 §11 (the boundary list) + document 30 §7 #36.
- **Status**: **to be back-filled** (newly established by this draft).

**A-44 · (Void) the legacy backfill's traversal order was once judged to take `orphans`' record order; superseded by A-47**

- **Original conclusion (no longer valid)**: the backfill's traversal order takes the record order frozen at the time `orphans` was migrated; A-39's three-level key governs candidate cores only and is not used for the traversal order.
- **Why it is void**: a 0.1.0 legacy save **has no `orphans` key** — `orphans` is an on-disk field newly added by document 30. When a legacy save is backfilled, the "candidate farmland" can only be derived from `FarmlandLedger.byOwner`, and **`byOwner` is a `HashMap`** (A-39 already confirms it), so the derived order is unstable across owners ⇒ it runs straight into document 30 §7 #19's "two startups of the same save give the same result". The original ruling treated "freeze the order at migration time" as a reproducible source, but the freezing action itself runs on `HashMap`'s iteration, which is no freezing point at all.
- **Status**: **void, superseded by A-47** (A-44's "take `orphans`' record order" is overturned; **A-47 itself then went through rev.2** — the sort key dropped from "dimension + coordinates" to `BlockPos`'s **single-level total order**, because the farmland side's on-disk form has only `pos` and no dimension). A-44 and A-36's order part are both kept as history and **their rows are not deleted** (deleting them would make later readers implement the old instruction).

**A-45 · The trample face's criterion is "a binding means protected", not "judge the ally live"**

- **Conclusion**: `protectsFromTrample` judges by "this cell has a binding ⇒ protected"; the **quota** and **overlay** faces still judge the live "ally". After a team change: the quota and the overlay recompute by the new team at once, while **the trample face stays protected** (the binding has not changed). Document 29 B-18's "protection may be lost at once after a team change" is narrowed to "the quota and overlay faces change immediately, the trample face does not".
- **Basis**: document 28 D-138 requires the three faces to "share this one set of judgments"; document 29 B-18 writes "protection may be lost at once after a team change" accordingly; and item 8 of document 30 §9 points out that `protectsFromTrample(level, pos)` has no enquirer parameter, so it is changed to "a binding means protected" and is raised to a technical ruling in appendix A #30. **Why D-138's literal reading cannot be taken**: the acting party of a trample may be a `Monster` with no team, and judging the ally live would judge the mob a non-ally, so the mob could trample allied farmland, directly conflicting with item 3 of 0.1.0's D-120 (both players and mobs must be stopped); a binding is produced only when an ally tills, so "has a binding" is exactly the stored evidence that the ally relationship once held.
- **Implementing task**: **F-195** (the implementation), **F-210** (B-18's narrowed readings).
- **Back-fill target**: document 28 D-138's supplementary item (add one sentence on the trample face's exception) + document 29 §3.10 and B-18 + document 30 §9 item 8 (raised from a "registration" to an adjudication).
- **Status**: **to be back-filled** (newly established by this draft).

**A-46 · The 38 acceptance entries of document 30 §7 do not cover the 39 boundaries of document 29 §11**

- **Conclusion**: the 38 acceptance entries and the 39 boundaries **are not one-to-one**: 13 match completely, 5 have only partial assertions, and **21 have no corresponding assertion at all** (B-01, B-02, B-03, B-05, B-08, B-10, B-11, B-13, B-14, B-15, B-17, B-18, B-19, B-20, B-21, B-24, B-25, B-26, B-31, B-32, B-35). The boundary list follows document 29 §11 and is accepted entry by entry by **F-210 / F-211**; document 30 §7 is **not required** to be expanded to one-to-one (that would renumber #1 ~ #38 wholesale and break #38's citation of A-40). Document 32's pitfalls chapter must state that "a boundary ≠ an acceptance entry".
- **Basis**: an entry-by-entry comparison of document 29 §11's and document 30 §7's tables (row 17 of this file's §6 table G gives the classification counts); a further 11 acceptance entries have no corresponding boundary (#1, #2, #3, #5, #6, #11, #13, #14, #16, #37, #38), belonging to the engineering face (counts / artifacts / tool / red line) by design.
- **Implementing task**: **F-210** (B-01 ~ B-20), **F-211** (B-21 ~ B-39), **F-209** (#37 / #38's closing).
- **Back-fill target**: document 30 §7's table header (add one sentence: "this table does not cover document 29 §11's boundaries; the latter are borne by document 31's F-210 / F-211") + document 32's pitfalls chapter.
- **Status**: **to be back-filled** (newly established by this draft).

**A-47 · A legacy backfill always sorts by `BlockPos`'s single-level total order first, then processes entry by entry (supersedes A-44; this file finalises it as rev.2)**

- **Conclusion (rev.2)**: a legacy backfill (including the first backfill of a 0.1.0 legacy save) always sorts the candidate farmland ascending by `BlockPos#compareTo` (`Vec3i`'s existing implementation, comparison order y → z → x) in a single-level total order, and then processes it entry by entry; the persist order of `orphans` **takes part in no judgment**. There are two reproducibility criteria: ① two consecutive startups of the same save give exactly the same N and M; ② swapping two records of the `orphans` segment in the `.dat` and re-running gives a bound coordinate set and N/M **exactly identical** to before the swap.
- **Why the sort key is `BlockPos` single-level rather than "dimension + coordinates"** (rev.2's correction): in `FarmlandLedger`'s NBT the farmland has only `pos` (one `long`) and **no dimension field** (item 2 of document 30 §2.2.2: v1 and v2 alike, farmland coordinates carry no dimension), whereas `OutpostCoreIndex.Entry` does carry `dimension`. **Cores have a dimension, farmland does not** — the inconsistency between the two keys is decided by the data, and forging a dimension key for farmland would conflict with the on-disk format and would also reintroduce the key-collision problem of "the same `(x,y,z)` in two dimensions" (item 5 of document 30 §9 has registered it as a known limitation not fixed in this version). `BlockPos`'s single-level total order is complete for the farmland side: the same ledger never holds two identical `pos`.
- **Basis**: a 0.1.0 legacy save has no `orphans` key (it is an on-disk field newly added by document 30), so the candidates can only be derived from `byOwner` (a `HashMap`; A-39 has confirmed that the order across owners is unstable); treating "freeze at migration time" as the source of the order still runs on `HashMap`'s iteration and runs into document 30 §7 #19's reproducibility requirement.
- **Implementing task**: **F-190** (the backfill sorts by `BlockPos`'s total order), **F-188** (v2 attaches no semantics to `orphans`' order), **F-208** (#19's N/M + the swap evidence).
- **Back-fill target**: document 28 D-137's supplementary item (change "by `byOwner`'s record order" to "by `BlockPos`'s total order") + document 29 §3.6 reading 2 (direction correct, the key narrowed from "dimension path + coordinates" to `BlockPos` single-level) + document 30 §2.2.8 (delete the `orphans` order segment) + one sentence added to A-36's record, "the order part has been superseded by A-47".
- **Status**: **to be back-filled** (re-judged by this draft; rev.2's sort key is already aligned with the author's wording).

**A-48 · The language-key arithmetic structurally missed 2 `KeyMapping` description keys (321 → 323)**

- **Conclusion**: the formula in document 28 §3.4, `315 − 2 + 1 + 3 + 1 + k`, **is structurally short of one term, `+2`** (the two `key.farmstorm.*` description keys); the complete formula is `315 − 2 + 10 = 323`, where 10 = 1 for `bulletSpeed` + 3 for CLIENT config + 2 for the `KeyMapping` description keys + 1 for the overlay's "self" + 3 for the command feedback. This version's language-key total is always written **323** (one copy each in Chinese and English), and any formula that yields 321 no longer holds.
- **Basis**: document 30 §4.3 names the 10 additions one by one (#1 ~ #10) and the 2 deletions; `BRIEF` §3.5 already carries the corrected formula; item 1 of document 30 §9 registers this as "the formula missed 2 entries".
- **Implementing task**: **F-206** (the ledger 323 × 2), **F-202** (those 2 `key.farmstorm.*`), **F-182 / F-201 / F-203 / F-205** (the other 8).
- **Back-fill target**: document 28 §3.4 (add `+2` to the formula) + document 32 (forbid the 321 wording).
- **Status**: **entered in the adjudication ledger** (A-48); document 28 §3.4 is to be back-filled.

**A-49 · Farmland at the same coordinate across dimensions colliding: not fixed in this version, listed as a known limitation**

- **Conclusion**: `FarmlandLedger` has no dimension field (it has been so since 0.1.0), so a farmland plot at `(0, 64, 0)` in the nether "borrows" the protection of the binding at the same coordinate in the overworld, and the quota counts the two dimensions' identical coordinates as one plot. **Not fixed in this version**: the plan does not ask for it; the legacy save's NBT holds only `pos` and the dimension cannot be recovered; and the farmland side has already stacked one save upgrade in this version (v1 → v2), so changing the dimension key would mean upgrading the same save twice in a row.
- **Basis**: item 5 of document 30 §9 (already registered as "a known limitation, not fixed in this version") + item 2 of document 30 §2.2.2 (farmland coordinates follow v1 and carry no dimension) + A-47 rev.2 (the sort key can therefore only be `BlockPos` single-level, since no dimension is available).
- **Implementing task**: **no implementation task is produced**; it is carried by **F-188** (the on-disk wording does not move, `pos` is still the only farmland key), **F-210 / F-211** (recorded as a known limitation, **not written as a pass / fail item**), and **F-212** (listed as a known limitation in the summary).
- **Back-fill target**: document 30 §9's known limitations + document 32's pitfalls chapter.
- **Status**: **entered in the adjudication ledger** (A-49); 30 / 31 / 32 are to be back-filled.

### 5.2 The Remaining 7 Entries (A-50 ~ A-56)

These 7 entries change none of this file's wording; they only register "who changed which upstream / which place this file handles itself", so that the ledger and document 31 can be reconciled.

| ID | Conclusion (one sentence) | Landing: task or document | Back-fill status |
|---|---|---|---|
| **A-50** | Document 28 D-131 **writes the projectile base class's path wrong**: the correct one is `entity/ThrownCropEntity.java` (the class sits at the top level of `entity/`, not under `entity/thrown/`, consistent with document 30 §1.2's "2 at the top level of `entity` = `ThrownCropEntity` + `TurretBullet`") | document 28 D-131 | **back-filled into 28**; this file's F-185 and row 18 of table A already cite `entity/ThrownCropEntity.java`, so no change is needed |
| **A-51** | The jar name in `一键构建.cmd` is at **`:11-12`**, not `:10-11` (`:10` is `if not exist "dist" mkdir "dist"`) | document 28 D-147 + fact card §1.10 | **back-filled into 28**; this file's F-176 and row 2 of table A are already written per `:11-12` |
| **A-52** | The toml section-count criterion `^\[` of document 30 §7 #2 cannot count **nested sections** (`[a.b]` is eaten by the first character after `\[`), so it must become `^\s*\[` | document 30 §7 #2 | **back-filled into 30**; acceptance item 1 of this file's F-192 is already written per `^\s*\[` |
| **A-53** | Adding `gameTime` to `OutpostCoreIndex.Entry` is this version's **second save-format change** and has to go into document 28 §3.2 (originally only `FarmlandLedger`'s rise to v2 was registered) | document 28 §3.2 | **back-filled into 28**; row B.2 no. 2 of this file's table B and F-189 are already written as "the second schema" |
| **A-54** | This file's dependency column and acceptance attribution were inconsistent in four places (F-208's dependency misassigned, three missing prerequisites, 21 vs 18, table A rows 23/24 attributed wrongly) | **document 31 (handled by this file itself)** | **handled by this file itself**: the dependency column recomputed mechanically as "the item taken over → the producing task", §1 and §2 identical character for character, the 21 boundaries distributed as 14 + 7, table A's rows 23/24 rewritten per the actual attribution, F-185's title unified to the long form |
| **A-55** | Document 30 §3's change surface is missing the two segments `FarmlandLedger.java:128-158` (the bookkeeping `add` / `remove`) and `:203-219` (the lazy validation `validated`) | document 30 §3 | **back-filled into 30**; this file covers it with **F-188** (both segments are in its "Files touched") and **F-191** (capacity / used consuming `validated`'s dropped result), and table A's supplementary segment has one more row explaining it |
| **A-56** | **F-192 must depend on F-191 (a compile order, not a stylistic order)**: `farmland.limitPerPlayer`'s only reader is `FarmlandLedger.base()` (`FarmlandLedger.java:72-74`), and deleting `base()` is F-191 | **document 31 (handled by this file itself)** | **handled by this file itself**: §1's and §2's dependencies are both `F-191`, and F-192's implementation notes state that this is a compile order |

> Numbering note: A-50 ~ A-53 and A-55 are recorded on the 28 / 30 side, A-54 / A-56 are document 31's correction sheets; none of the 7 **changes** any conclusion of this file's A-42 ~ A-49.

> Back-fill discipline (per item 4 of document 28 §0.3): A-42 ~ A-49 change **only the corresponding few places** and no decision's conclusion sentence; where they conflict with this file, the back-filled upstream text prevails. **A-44 and A-36's order part are kept as history** (A-44 is void and superseded by A-47; A-47 itself went through rev.2 and narrowed the sort key to `BlockPos` single-level), and their rows are not deleted — deleting a row in the ledger would make later readers implement the old instruction.

---

## 6. Seven Coverage Mapping Tables (the Most Important Part of This File)

Tables A ~ F are the six coverage tables, and table G is the upstream-consistency check (the seventh, specifically catching places where the three upstream documents contradict each other / omit something in one direction). The last row of each table gives "coverage count / total".

### Table A · Change-surface coverage (the 44 rows of document 30 §3 → tasks)

| §3 row | Path or class#method | Add / change / delete | Responsible tasks |
|---|---|---|---|
| 1 | `gradle.properties:11` | change | F-176 |
| 2 | `检查环境.cmd:14`, `一键构建.cmd:11-12`, the banner lines of the four `.cmd` scripts | change | F-176 (artifact-face check F-207) |
| 3 | `assets/farmstorm/textures/item/wild_apple.png` | change | F-179 |
| 4 | `tools/gen_textures.py`'s "+" overlay mechanism (`:274-275`, `:407`, `:546-560`, `:612-623`, `:974-975`) | change | F-178 |
| 5 | `tools/gen_textures.py:154`, `:849-881`, `:884-895` | change | F-179 |
| 6 | `tools/gen_textures.py:982-983`, `:1151-1154` | change | F-179 |
| 7 | `registry/FarmstormBlocks.java:79` | change | F-180 |
| 8 | `registry/FarmstormBlocks.java:82-89` | change | F-181 |
| 9 | `config/FarmstormConfig.java:425-427` | change | F-182 |
| 10 | `config/FarmstormConfig.java:431-433` | change | F-182 |
| 11 | `config/FarmstormConfig.java` (inside the `pumpkin_turret` group, after `:433`) | add | F-182 |
| 12 | `entity/TurretBullet.java:28` | delete | F-183 |
| 13 | `entity/TurretBullet.java:90-96` | change | F-184 |
| 14 | `entity/TurretBullet.java:98-119` | change | F-184 |
| 15 | `behavior/impl/PumpkinTurret.java:75` | change | F-183 |
| 16 | `entity/thrown/FriendChivesProjectile.java` | change | F-185 (the field itself), F-186 (the load branch) |
| 17 | `entity/thrown/FriendChivesProjectile.java:56-80` | change | F-185 |
| 18 | `entity/ThrownCropEntity.java:95-118` | check (no change) | F-185 (the check) |
| 19 | `farmland/FarmlandLedger.java:51` | change | F-188 |
| 20 | `farmland/FarmlandLedger.java:57-58, 72-79, 82-98` | delete | F-191 |
| 21 | `farmland/FarmlandLedger.java:101-121` | change | F-191 |
| 22 | `farmland/FarmlandLedger.java:228-264` | change | F-188 |
| 23 | `farmland/FarmlandLedger.java` (new methods) | add | F-188 (`bind` / `unbind` / `migrate` / `bindingsOf` / `orphanCount` / `rebuildByCore`), F-190 (`backfill`) |
| 24 | `farmland/FarmlandGuard.java:69-75` | change | F-193 (core selection + the free-slot judgment on that core); **F-194's write point is not in this class** — it is at `event/FarmstormGameEvents.java:206` (document 30 §3 lists no row for it; it is row 24's decision landing) |
| 25 | `farmland/FarmlandGuard.java:92-107` | change | F-195 |
| 26 | `farmland/FarmlandGuard.java:120-134` | change | F-195 |
| 27 | `blockentity/OutpostCoreBlockEntity.java:315-335` | change | F-196 |
| 28 | `blockentity/OutpostCoreBlockEntity.java:353-379` | change | F-196 |
| 29 | `blockentity/OutpostCoreBlockEntity.java:95-103` | change | F-198 (the ticket segment), F-200 (the broadcast segment) |
| 30 | `blockentity/OutpostCoreBlockEntity.java:166-170` | change | F-198 (the ticket segment), F-200 (the broadcast segment) |
| 31 | `team/TeamResolver.java`, `team/ScoreboardTeamResolver.java` | add | F-187 |
| 32 | `outpost/OutpostCoreIndex.java` | add / change | F-189 (`gameTime` / the on-disk form / the accessors), F-200 (the consumption) |
| 33 | `command/FarmstormCommands.java` | delete | F-197 |
| 34 | `event/FarmstormGameEvents.java:4`, `:62`, `:71-73` | delete | F-197 |
| 35 | `event/FarmstormGameEvents.java` (+6 listeners) | add | F-200 (4 packet-push listeners), F-198 (`ServerStoppingEvent`) |
| 36 | `network/CoreListPayload.java` and two others, three in all | add | F-199 (payload / handler), F-200 (broadcaster) |
| 37 | `config/FarmstormClientConfig.java` + the line after `Farmstorm.java:56` | add | F-201 |
| 38 | the 5 new classes under `client/` | add | F-202, F-203, F-204, F-205 (`ClientCoreList` comes from F-199) |
| 39 | `outpost/CoreChunkTickets.java` | add | F-198 |
| 40 | `datagen/ModLangProvider.java:393-394`, `:404-406`, the 10 new entries, the class comment | change | F-206 (the ledger / the comment) + F-182 / F-192 / F-197 / F-201 / F-202 / F-203 / F-205 (their own keys) |
| 41 | `Farmstorm.java:124-146`'s registry-surface self-check | check (no change) | F-212 |
| 42 | `entity/ThrownCropEntity.java` (`keepAliveAfterImpact`'s default) | check (no change) | F-185 |
| 43 | `tools/verify_project.py:222-237` | change | F-177 |
| 44 | the other three segments of `tools/verify_project.py` | check (no change) | F-212 |
| **Supplementary 1** | `config/FarmstormConfig.java:226`, `:904-908`, `ModLangProvider.java:393-394` (A-42, not among document 30's 44 rows) | delete | **F-192** |
| **Supplementary 2** | `farmland/FarmlandLedger.java:128-158` (the bookkeeping `add` / `remove`) and `:203-219` (the lazy validation `validated`) — the 44 rows of document 30 §3 list neither segment (A-55) | change | **F-188** (the rewrite of both segments and the double pruning of `byCore`), **F-191** (`capacity` / `used` consuming the validation result) |

**44/44 coverage** (+ 2 supplementary rows, A-42 / A-55; action-surface total 47 = 44 + 3, 0 uncovered).

### Table B · Registry and schema (+0 registered objects, 2 schema changes)

**B.1 The 12 registry classes, +0 one by one** (baseline 93, document 30 §1.2; this version adds not one registry entry, D-146 / §1.3)

| registry | Baseline | This version's increment | Checking task |
|---|---|---|---|
| `Registries.ITEM` | 32 | **+0** | F-212 (the twelve classes' readings of #1) |
| `Registries.BLOCK` | 26 | **+0** | as above |
| `Registries.BLOCK_ENTITY_TYPE` | 7 | **+0** | as above |
| `Registries.ENTITY_TYPE` | 13 | **+0** | as above |
| `Registries.MOB_EFFECT` | 4 | **+0** | as above |
| `Registries.SOUND_EVENT` | 3 | **+0** | as above |
| `Registries.RECIPE_TYPE` | 1 | **+0** | as above |
| `Registries.RECIPE_SERIALIZER` | 1 | **+0** | as above |
| `Registries.CREATIVE_MODE_TAB` | 1 | **+0** | as above |
| `Registries.DAMAGE_TYPE` | 3 | **+0** | as above |
| `Registries.DATA_COMPONENT_TYPE` | 1 | **+0** | as above |
| `Registries.MENU` | 1 | **+0** | as above |
| **Total** | **93** | **+0** | 32+26+7+13+4+3+1+1+1+3+1+1 = **93** |

**B.2 The two schema changes** (each committed and rolled back on its own, never merged)

| # | Save | Change | Backward-compatible read | Task |
|---|---|---|---|---|
| 1 | `world/data/farmstorm_farmland.dat` | rise to **v2**: `dataVersion` + `bindings` (`pos` / `owner` / `coreDimension` / `corePos`) + `orphans` (`pos` / `owner`); **`owners` is no longer written** | a missing `dataVersion` is read as v1, and all `owners` are read as `orphans` (no binding is guessed) | **F-188** |
| 2 | `world/data/farmstorm_outpost_core.dat` | `Entry` gains `long gameTime` and writes it to disk (A-39's comparison-key carrier) | a missing field takes `Long.MIN_VALUE`, and startup logs a count | **F-189** |

**B.3 The six components "added but not entering the registry surface"** (this is the whole reason for +0)

| # | Component | Why it does not enter the registry | Task |
|---|---|---|---|
| 1 | 1 custom network packet (`farmstorm:core_list`) | the payload goes through `RegisterPayloadHandlersEvent`, and the `Type` is only a `ResourceLocation` record | F-199 |
| 2 | 2 `KeyMapping`s | key bindings go into the client's `Options#keyMappings` array | F-202 |
| 3 | CLIENT config 3 keys | `ModConfigSpec` is a config-file model | F-201 |
| 4 | the client command `/farmstorm hud` | the Brigadier command tree is a runtime registration (`RegisterClientCommandsEvent`) | F-205 |
| 5 | the farmland binding table | `SavedData` on disk | F-188 |
| 6 | the chunk ticket | `TicketType.create(...)` is an ordinary object; 1.21.1 has no ticket registry | F-198 |

### Table C · Config coverage (of the server's 166 keys, "2 changed, 1 added, 1 deleted" + client 3 keys)

| # | Key | Type | Default | Change / add / delete | Key-landing task | Read-point task |
|---|---|---|---|---|---|---|
| 1 | `pumpkin_turret.fireIntervalTicks` | int | 60 → **40** | change | F-182 | F-184 (the interval is consumed by `TurretBehavior`'s existing read point, untouched) |
| 2 | `pumpkin_turret.projectileDamage` | int | 1 → **3** | change | F-182 | F-184 (the settlement body moves house, the read point does not) |
| 3 | `pumpkin_turret.bulletSpeed` | double | **0.45** (range 0.0 ~ 4.0) | **add** | F-182 | F-183 (the two read points) |
| 4 | `farmland.limitPerPlayer` | int | — | **delete** (the whole group disappears) | F-192 (the supplementary row, A-42) | no reader (`base()` was already deleted in F-191) |
| 5 | `hud.enabled` (client) | bool | **true** | add | F-201 | F-202 (the key binding), F-203 (the rendering), F-205 (the command) |
| 6 | `hud.x` (client) | int | **4** (0 ~ 3840) | add | F-201 | F-203 (the clamp), F-204 (dragging), F-205 (reset) |
| 7 | `hud.y` (client) | int | **4** (0 ~ 2160) | add | F-201 | as above |
| **Total** | server **166** + client **3** | | | 2 changed / 1 added / 1 deleted + client 3 | | |

The arithmetic and the checks:

1. **server**: `166 − 1 + 1 = 166` (deleting `farmland.limitPerPlayer`, adding `pumpkin_turret.bulletSpeed`) — the total is level but **the composition has changed**.
2. **group count**: `59 − 1 = 58` (the whole `farmland` group disappears, and the new key lands in the existing `pumpkin_turret` group).
3. **client**: `+3`, **not counted into** 166, in a separate `farmstorm-client.toml`; added to the server's 166 it is 169, exactly the 169 entries of `farmstorm.configuration` among the language keys (document 30 §4.3's strong check).
4. The new key's three properties (type / default / range) follow row by row the "three properties of a new key" table of document 30 §4.1, and this file does not recompute them.

### Table D · Resource coverage (0 added / 1 PNG replaced in place + three places in the generator + 2 lang files)

| # | File | Add / replace | Handling | Task |
|---|---|---|---|---|
| 1 | `assets/farmstorm/textures/item/wild_apple.png` | **1 replaced in place** | 16×16 / 571 B → **64×64 / 6,946 B**, a byte-for-byte copy of the source image | F-179 |
| 2 | `tools/gen_textures.py` (`NO_PLUS_ICONS` / `add_plus` / `build_marker` / `compose_icon` / the composition branch / the call sites / the dry-run text) | the generator's **place 1** | the "+" overlay mechanism deleted altogether | F-178 |
| 3 | `tools/gen_textures.py:154`, `:849-881`, `:884-895` | the generator's **place 2** | `wild_apple` passthrough + the default source image re-pointed at 64×64 | F-179 |
| 4 | `tools/gen_textures.py:982-983`, `:1151-1154` | the generator's **place 3** | `expected_size()` and the size assertion relaxed together (A-32) | F-179 |
| 5 | `assets/farmstorm/lang/zh_cn.json`, `en_us.json` | **2 replaced in place** (the file count unchanged) | regenerated by datagen, **315 → 323** keys | F-206 (the ledger) + the individual tasks |
| **Total** | **0 added**, **1 PNG + 2 lang files replaced in place**; **3 places** of the generator changed | | | **0 uncovered** |

Files that are not added (this must be written down, or someone will go and add them): **no** new PNG, **no** new JSON (models / blockstates / loot tables / recipes / advancements all 0), `sounds.json` untouched (document 30 §5 decides "do not add `attenuation_distance`"), ogg untouched.

### Table E · Risk coverage (RT-31 ~ RT-41, each with at least one task responsible for verifying it)

| ID | Risk | Task responsible for verification | How that task verifies it |
|---|---|---|---|
| **RT-31** | The first introduction of a custom network packet: miss any one of codec / registration / disconnect / cross-dimension / push timing, and the overlay shows wrong data | F-199, F-200, F-203 | F-199 verifies "exactly 1 payload file + 0 `playToServer` + a dedicated server can start"; F-200 verifies the 10 timings and the debounce (a chained event in the same tick produces only 1 packet) and that nothing survives a server switch; F-203 verifies the 9-line / 93 px display |
| **RT-32** | The binding-table rework + the save upgrade: a legacy save reads an empty ledger, the backfill shovels the old fields up, the migration computes wrong | F-188, F-190, F-196 | F-188 starts once each with a fixture and with an old save, and reads v2 and 0.1.0 in both directions; F-190 verifies "the same save started twice gives the same N/M" and that `setBlock` has 0 hits; F-196 verifies the three branches and "no drop" |
| **RT-33** | `outpost_core.farmlandBonus`'s one key, two meanings (capacity + the quota base) | F-182, F-191, F-193 | F-182 writes the double meaning into the comment (visible in `git diff`); F-191 verifies "after changing the key the quota = 16 × the number of cores"; F-193 verifies "after the key is changed to 2 the 3rd cell is refused and the quota changes to 2 at the same time" |
| **RT-34** | The cost of Friend Chives scanning its sphere every tick | F-185 | With 10 fields alive at once the server tick time does not degrade into visible stutter; with `effectTicks` changed to 1200 the field self-destructs on schedule and does not accumulate |
| **RT-35** | The landed entity's survival while visible: gravity / collisions / a leak across unloads | F-185, F-186 | F-185 verifies that a landing point inside flowing water does not move for 30 seconds and that it disappears on tick `effectTicks`; F-186 verifies that after an unload and return the entity does not exist and is not revived |
| **RT-36** | Light level 15: strong light propagates 15 blocks, and no mob ever spawns at that position | F-181 | F3's `Light` = 15; light within 15 blocks and none beyond 16; in the centre of the light 0 mobs in 100 seconds and ≥ 1 outside |
| **RT-37** | A 64×64 texture: the display area is still 16×16, and re-running the generator rolls it back | F-179 | Verify the PNG is 64×64 / 6,946 B / the same sha256; deleting the image and re-running still gives 64×64 with no assertion reported; the inventory is not a broken image |
| **RT-38** | The Pumpkin Turret's two defaults have no effect on an already-written toml | F-182 | Two controls: delete the old toml → 40 / 3 / 0.45; keep the old toml → 60 / 1 / 0.45 (`bulletSpeed` auto-filled) |
| **RT-39** | Chunk ticket leaks | F-198 | After destroying the core and walking 200 blocks away the chunk can unload; stopping and starting the server rebuilds the tickets; the release count = the request count; `chunks.dat` holds no core coordinate |
| **RT-40** | The knock-on effects of deleting the command tree (`baseOverride` / 2 language keys / comments / documents) | F-197, F-192, F-206 | F-197 verifies "the server's `/farmstorm` is an unknown command, `FarmstormCommands` has 0 hits"; F-192 verifies the `[farmland]` section disappears; F-206 verifies `farmlandlimit` has 0 hits in both lang files and that 323 keys reconcile |
| **RT-41** | After the red line is broken, the existing documents still say "0 network packets" | F-207, F-212 | F-207's `jar tf` and F-212's red-line scan: `@Mixin` 0 hits, `CustomPacketPayload` exactly 1 file; the document face is rewritten by document 32 per D-146 (outside this file's scope) |
| **Total** | **11 entries** | | **11/11, 0 uncovered** |

### Table F · API-to-verify coverage (document 30 §8's 8 entries → which task confirms it at which step + the fallback)

**This table is this version's only prevention against "discovering halfway through implementation that the interface does not exist"**: each entry must land on **a concrete step of a concrete task**, and the fallback must be concrete enough to execute directly.

| # | API / semantics to verify | Confirmed by which task at which step | Fallback when it cannot be confirmed |
|---|---|---|---|
| **①** | The **runtime** behaviour of `PayloadRegistrar#playToClient` / `StreamCodec` / `PacketDistributor#sendToPlayer`: the handshake version string `"1"`, whether a single-player in-memory connection still serialises, and whether a dedicated server loads the client handler class | **F-199's first step**: send only an empty packet with `total=0`, start single-player and a dedicated server once each, and see whether the handler is called (one log line) | If single-player does not serialise and the handler never fires ⇒ change to the client **requesting once** after `ClientPlayerNetworkEvent.LoggingIn`. That becomes **2 packets** and must be registered back into document 28 (D-146: no cap, but every packet must be registered); adding one on your own is not allowed |
| **②** | The registration shape of `RegisterClientCommandsEvent`: how the client dispatcher and the server-sent command tree merge, and where `sendSuccess(..., false)`'s echo lands for a client command | **F-205's first step**: register the `/farmstorm` root + one `hud` literal first, and see whether `hud` appears in the single-player completions | If the client command is overridden by the server tree ⇒ keep only the two carriers "key binding + CLIENT config" (items 1 and 3 of D-144 suffice; the command is only convenience), and F-205 shrinks to one "key-binding equivalent" |
| **③** | Whether a self-made `TicketType.create(String, Comparator)` type is accepted by `DistanceManager`; whether `removeRegionTicket` requires the four arguments to be exactly identical; the actual load face of `distance = 1` (A-43) | **F-198's first step**: place 1 core and check F3's three-layer chunk state, then destroy the core and walk 200 blocks away to see whether it unloads | If the self-made type is rejected ⇒ fall back to `TicketType.PORTAL` (`BlockPos` value, `timeout = 300`) and renew it every `serverTick` — **this changes the release semantics** and must be registered back into document 28; if `distance = 1` does not tick the block entity ⇒ change to `distance = 2` (a 5×5 load face, with A-43's wording changed along with it) |
| **④** | Whether `sounds.json`'s `attenuation_distance` and `playSound`'s volume stack (document 30 §5 has decided **multiplication**) | **F-185's sound acceptance**: stand 32 blocks and 33 blocks from the landing point and listen once each (`#31`) | If it cannot be heard at 32 blocks ⇒ adjust `friend_chives.soundRange` (a config key, default 32.0, range 0 ~ 64), and **do not change `sounds.json`** (changing it would break "0 new JSONs" and would turn the audible radius into 64) |
| **⑤** | The registration timing of `KeyMapping` and `RegisterKeyMappingsEvent` (after `Options` loads, before the key-binding screen opens); whether the default keys **H / J** collide; whether `consumeClick()` still counts while a Screen is open | **F-202's first step**: open the key-binding screen and confirm the two key bindings are visible, then press H/J repeatedly and confirm each fires once | If registration is too late (invisible in the screen) ⇒ move the registration into `FMLClientSetupEvent#enqueueWork`; if H/J collide ⇒ change only the default key codes, not the language keys or the design |
| **⑥** | The **exact per-packet size limit** of a vanilla custom packet (this file only needs the 3.1 KB order of magnitude) | **F-203's 9-line full scenario** (`#10`): with 64 candidates the packet still arrives normally | If the limit is lower than expected (it cannot be lower than 3 KB) ⇒ lower `MAX_CORES` from **64** to **32** (`total` can still compute `+N` correctly, and the client only draws 9 lines) |
| **⑦** | The drag `Screen`'s mouse-coordinate wording: whether `mouseClicked` / `mouseDragged`'s `(mx,my)` are already **GUI-scaled** coordinates | **F-204's first step**: drag the panel to a known `(100, 100)` and check whether `hud.x` / `hud.y` are 100 | If the coordinate systems differ ⇒ do `mx * guiWidth / windowWidth` in `mouseClicked` first (the factors being `Minecraft#getWindow`'s width and height) |
| **⑧** | Whether `ClientPlayerNetworkEvent.LoggingOut` fires on both paths, "single-player quitting to the main menu" and "being kicked" | **F-200's server-switch test** (`#29`): quit server X and enter server Y, and see whether the overlay disappears at once | If it does not fire ⇒ empty the snapshot in `ClientTickEvent.Post` when `Minecraft#level == null` (one more null check per tick, a negligible cost) |
| **Total** | **8 entries (①~⑧)** | **8/8 land on a concrete task and concrete step** | **8/8 give an executable fallback** |

### Table G · Upstream consistency check (among the three documents 28 / 29 / 30, and between the fact card and document 30 §3)

Count: **17 rows** — **6 real disagreements** (G-1, G-9, G-10, G-11, G-12, G-17; **all 6 have an adjudication**: G-1 → A-48, G-9 → A-47, G-10 → A-45, G-11 → A-43, G-12 → A-42, G-17 → A-46), **6 already adjudicated and consistent** (G-2 ~ G-7), **2 notational differences** (G-8, G-13), **3 one-way omissions** (G-14 ~ G-16).

| # | Disagreement | Document 28 says | Document 29 says | Document 30 says | Which prevails | Adjudicated yet |
|---|---|---|---|---|---|---|
| G-1 | **The language-key total** (321 vs 323) | §3.4's formula `315 − 2 + 1 + 3 + 1 + k`, `k` = the number of command-feedback entries (candidate 3), the final value "to be pinned down by document 30" ⇒ taking k = 3 gives **321** | says only that the overlay's "self" marker uses one key and the dimension path adds none | §4.3 names the 10 additions one by one, `315 − 2 + 10 = **323**`; the 2 extra are the `KeyMapping` description keys | **323** (document 30 §4.3's item-by-item list can be checked one by one; document 28's formula missed 2 key-binding description keys) | **A-48 established** (this document originally wrote "settled, no A number"; re-judged on review 2026-11-02; entered in the ledger) |
| G-2 | **8 lines vs 9 lines** | D-143: at most **8 core lines**, with 1 more `+N` line beyond that ⇒ at most **9 lines** | §5.5: 8 core lines, and `+N` does not take one of the 8 ⇒ at most 9 lines | §2.4.4: the maximum line count is **9** | **8 + 1 = 9** | **Adjudicated A-34** |
| G-3 | **83 px vs 93 px** | D-143 item 4 gives no height | §5.6: a full 9 lines is **93** px (`9×9 + 8×1 + 2×2`), 1 line is 13 px | §2.4.4: 8 lines **83** px, 9 lines **93** px | **8 lines 83 / 9 lines 93** (two values of the same formula `10n + 3`) | **Adjudicated A-34** (93 is the 9-line value) |
| G-4 | **The line-height decomposition** (9+1+2 vs `10n+3`) | — | §5.6: line height 9 / line spacing 1 / inner padding 2 | §2.4.4: panel height `10n + 3` | **take document 29 §5.6's three numbers**; `10n + 3` is an equivalent corollary (`9n + (n−1) + 4`) | **Adjudicated A-41** |
| G-5 | **Five sound types vs three** | D-130's supplementary text writes three: "breaking / placing / stepping" | §6.3 writes five (adding mining / falling) | §3 row 7 and §7 #24–#28 go by five | **five** | **Adjudicated A-31** |
| G-6 | **`effectTicks` vs 200** | D-131 (after the back-fill): both the field length and the application base take `friend_chives.effectTicks` | §8.2 the same wording | §2.7.2 only `fieldLength` is left in the code as the one source | **`effectTicks`**; 200 is only the default | **Adjudicated A-33** |
| G-7 | **A two-level vs three-level comparison key** | D-135's supplementary item: `placedGameTime` → dimension id → coordinate lexicographic order (three levels) | §3.3 / §5.4 three levels | §2.2.7 / §2.1.5 three levels (`BlockPos#compareTo` is y → z → x) | **three levels** | **Adjudicated A-39** |
| G-8 | **166 vs 165** | §3.4: `166 − 1 + 1 = 166`, groups 59 → 58 | §9 row 46: 166 / 58 groups | §4.1: 166 / 58 groups; a note in §4.3 says "`farmstorm.configuration` from 166 → 165" | **166 / 58**; 165 is the intermediate quantity "after the deletion, before adding `bulletSpeed`", not the total | a notational difference, no adjudication |
| G-9 | **The legacy backfill's traversal order** | D-137 (after A-36's back-fill): by `FarmlandLedger.byOwner`'s record order (`LinkedHashSet`) | §3.6 reading 2: **sort by (dimension path, x, y, z) first** and then backfill entry by entry, never relying on `byOwner`'s iteration | §2.2.8: by `orphans`' record order (= v1 `byOwner`'s record order, frozen at migration) | **sort by `BlockPos#compareTo`'s single-level total order (y → z → x) and then traverse (A-47 rev.2)** — the farmland's on-disk form has only `pos` and no dimension, so a three-dimensional key is unobtainable on the farmland side; both sides' "by record order" wordings are void; A-39's three-level key is used only for comparing candidate cores | **A-44 (void) → A-47 (rev.2)** |
| G-10 | **The trample face's criterion** | D-138: farmland protection / the quota / the overlay **share one and the same** ally judgment (live, valid offline) | B-18 / §3.10: after a team change protection recomputes by the live ally ⇒ it may **lose protection at once** | §9 item 8 + appendix A #30: the trample face goes by "**a binding means protected**" (there is no enquirer parameter, and mobs have no team) | **the trample face goes by "a binding means protected"**; the quota and overlay faces still judge the ally live; B-18 is narrowed | **A-45 newly established by this draft** |
| G-11 | **Forced loading "1 chunk"** | D-145: load only that **1 chunk** (**not** 3×3) | §3.10 does not expand it (only cites D-145) | §2.5.2: `distance = 1` ⇒ the centre ticks, the adjacent ring loads to FULL ⇒ **the load face is 3×3** | **the tick face is 1 chunk, the load face is necessarily 3×3**; documents and acceptance write the tick face, and asserting that only 1 chunk gets loaded is forbidden | **A-43 newly established by this draft** |
| G-12 | **44 rows vs the fact card's 27 entries** | §3.3 row 1 requires deleting `farmland.limitPerPlayer` | — (document 29 lists no change surface) | the 44 rows of §3 have **no** such row (only in §2.3 row 3 and §4.1's deletion table); the fact card §3.2 row 18 has it | **the deletion is mandatory**; the 44 rows are not renumbered, this file registers it as a supplementary row, and the action surface is 47 = 44 + 3 | **A-42 newly established by this draft** |
| G-13 | **The row numbers cited for the size assertion** (four variants) | §3.2 row 14 writes `:1152-1154` | — | §2.6 writes `:1153-1154`, §3 row 6 and §5 write `:1151-1154`, and §7 #23 writes `:1153` | **measured as `:982-983` + `:1151-1154`** (`image = compose` at 1151, the assertion at 1153); **acceptance uses no row number as a criterion** (row numbers drift with changes, the same spirit as A-37) | a notational difference, no adjudication |
| G-14 | **The config comment hard-codes "60 刻 = 3 秒"** | D-127 gives only the default | — | §3 row 10 changes only the default and does not mention the comment | changing a default **must change the comment along with it** (the same discipline as item 7 of document 30 §9) | a one-way omission, folded into **F-182**, no adjudication |
| G-15 | **The two `.cmd` scripts' banner `v0.0.1`** | D-147 names only the jar name (`:14` / `:11-12`) | — | §3 row 2 likewise names only the jar name | the banner changes to `v0.1.1` along with it (D-124: the version number has only one source) | a one-way omission, folded into **F-176**, no adjudication |
| G-16 | **`+N`'s colour** | — | §5.5: grey `0xFFAAAAAA` | §2.4.4 lists only self / teammate / background / border and does not list `+N` | **take document 29's `0xFFAAAAAA`**, and document 30 adds one row | a one-way omission, no adjudication (it does not affect the acceptance criteria) |
| G-17 | **38 acceptance entries vs 39 boundaries** | — | §11: **39 entries** (B-01 ~ B-39, **no B-40 added in this version**, see A-49) | §7: **38 entries** (#1 ~ #38) | **not one-to-one**: 13 match completely, 5 partially covered, **21 with no corresponding assertion** — `B-01`, `B-02`, `B-03`, `B-05`, `B-08`, `B-10`, `B-11`, `B-13`, `B-14`, `B-15`, `B-17`, `B-18`, `B-19`, `B-20`, `B-21`, `B-24`, `B-25`, `B-26`, `B-31`, `B-32`, `B-35` (**21 numbers named one by one**); a further 11 acceptance entries have no corresponding boundary (#1, #2, #3, #5, #6, #11, #13, #14, #16, #37, #38, the engineering face). The boundaries follow document 29 §11: **F-210 accepts B-01 ~ B-20 entry by entry (20 entries)**, **F-211 accepts B-21 ~ B-39 entry by entry (19 entries)** | **A-46 newly established by this draft** (already tightened: the 21 numbers named one by one + each landing on one acceptance item, see F-210 / F-211) |
| **Total** | **17 rows** | | | | real disagreements **6** (all adjudicated) / adjudicated **6** / notational differences **2** / one-way omissions **3** | table G derives **8 entries** (A-42 ~ A-49; of which A-44 is void); the other seven, A-50 ~ A-56, are in §5.2 and do not come from table G |

Three ways to read table G:

1. **G-2 ~ G-7 are "two ways of writing the same thing"**: already ruled by A-31 / A-33 / A-34 / A-39 / A-41, and this file only reproduces them without changing any conclusion; G-1 closes with **A-48** (total 323), and G-8 is a notational difference about an intermediate quantity (165 is the quantity "after the deletion, before adding `bulletSpeed`", not the total).
2. **G-9 ~ G-12 are real disagreements**: the three upstream documents give **different behavioural outcomes** for the same thing (the traversal order decides which old fields can be bound; the trample face decides whether a field still resists trampling after a team change; the load face decides how many chunks F3 shows; the action surface decides whether a config group is deleted). All four **have been adjudicated**: G-9 → **A-47 (rev.2, `BlockPos` single-level; A-44 void)**, G-10 → A-45, G-11 → A-43, G-12 → A-42.
3. **G-13 ~ G-16 are one-way omissions**: only one side wrote it and the other side missed it, and the treatment is "take the side that wrote it + add one sentence on the other side", changing no decision. G-17 is a disagreement about coverage, and the treatment is "the boundaries are borne by dedicated acceptance tasks, and document 30 §7 is not required to be one-to-one".

---

## 7. Coverage Self-Check (walk through entry by entry before submission)

| # | Check item | Criterion | Result |
|---|---|---|---|
| 1 | Task numbers contiguous, no gap | `grep -c "^#### F-"` = **37**; `grep -oE "F-2[0-9]{2}" \| sort -u` = 37 numbers; the range `F-176 ~ F-212` ⇒ `212 − 176 + 1 = 37` | **pass** |
| 2 | Phase numbers contiguous | 26 / 27 / 28 / 29 / 30 / 31, 6 of them, covering the task ranges one-to-one | **pass** |
| 3 | Milestone numbers contiguous | M22 ~ M27, 6 of them, one-to-one with the Phases | **pass** |
| 4 | Adjudication numbers | A-30 ~ A-41 reproduced as they stand, **12** entries (aligned item by item with `.work/v011-verdicts.md`); A-42 ~ A-49 newly established, **8** entries (A-44 void); a further A-50 ~ A-56, **7** entries; **27** in total (A-30 ~ A-56), contiguous with no gap | **pass** |
| 5 | Table A coverage | 44 rows without missing one + 2 supplementary rows (A-42 / A-55); **0** uncovered | **44/44 (+3)** |
| 6 | Table B coverage | the 12 registry classes +0 one by one (93 in total); the **2** schema changes each in a task of their own; the **6** components outside the registry surface each have a task | **20 items** |
| 7 | Table C coverage | server 2 changed / 1 added / 1 deleted + client 3; the arithmetic `166 − 1 + 1 = 166`, `59 − 1 = 58`, `+3` | **7 items** |
| 8 | Table D coverage | 0 added, 1 PNG + 2 lang files replaced in place, 3 places of the generator; `sounds.json` and ogg untouched | **5 items** |
| 9 | Table E coverage | RT-31 ~ RT-41, **11** entries, each with at least one task responsible for verification and with a "how to verify it" | **11/11** |
| 10 | Table F coverage | document 30 §8's **8** entries, each landing on **a concrete step of a concrete task** + **a concrete fallback**, with no "watch out during implementation" | **8/8** |
| 11 | Table G | **17** rows; 6 real disagreements (all adjudicated), 6 adjudicated, 2 notational differences, 3 one-way omissions; G-17's **21** boundary numbers are named one by one | **17 rows** |
| 12 | Every task traceable | all 37 tasks trace to one of "a decision number / a row of document 30 §3 / an acceptance item / a risk"; there is no task out of thin air | **pass** |
| 13 | Every task has ≥ 1 observable acceptance item | read entry by entry: all 37 tasks have ≥ 1 assertion falling on one of §0.3's seven locations; no "behaves normally" or "feels good" | **pass** |
| 14 | A-37 discipline | no "call-site count = N" criterion anywhere in the file; every statement about a write entry is turned into "the hit count of `grep -c`" or "the number of entries in the save" | **pass** |
| 15 | A-40 timing | F-177 is earlier than every task that cites a tool conclusion; F-177 ~ F-191 never cite `FAIL 0`; F-192 is the closing point | **pass** |
| 16 | No assertion about time-sensitive facts | nowhere in the file is there a criterion such as "N call sites before the change", which would expire; any citation of the pre-change state is used only for "the scope of a `git diff`" and "the two sides of a control experiment" | **pass** |
| 17 | Workload self-consistent | `5 × 0.5 + 22 × 1.5 + 10 × 3.0 = 65.5`; summed by Phase = 65.5; `65.5 × 1.25 = 81.875`; S+M+L = 37 | **pass** |
| 18 | No dependency cycles, numbers increase, the two places identical character for character (A-54); §5 completely covered | every task's "Depends on" numbers are smaller than its own number; ordering by Phase is a topological order with no back edge; **the dependency column of §1's table and the "Depends on" line of §2's detail are identical character for character** (37 / 37, a script comparison found 0 inconsistencies); **§5 covers A-30 ~ A-56, 27 entries, with no gap** (12 + 8 + 7) | **pass** |
| 19 | Forbidden words and tone | read through entry by entry: no undecided-item marker, no to-do placeholder marker, no vaguely conditional conclusion; no second-person honorific anywhere, always "the author" / "the implementer" | **pass** |
| 20 | Only one file created | `git status` shows only `docs/zh/31-TASKS-0.1.1.md` as new; 28 / 29 / 30, the fact card, the adjudication ledger and `src/` are not changed by a line | **pass** |
| 21 | A-47's sort-key wording (rev.2) | the backfill traversal is written only as `BlockPos#compareTo`'s single-level total order (y → z → x), **never** as a "dimension + coordinates" traversal key; the only remaining prohibition wording about ordering is A-39's one place (`grep` hits **1**) | **pass** |
| 22 | A-46's tightening | table G-17 names the 21 boundary numbers one by one; F-210 / F-211's 20 / 19 acceptance items correspond one-to-one with "B-01 ~ B-20" / "B-21 ~ B-39"; **the number of boundaries with no corresponding entry in §7 = F-210's 14 + F-211's 7 = 21** (matching table G-17's 21 numbers one by one); every purely visual one states "human visual / auditory inspection + a screenshot / recording saved to `.work/v011-verify/`" | **pass** |
| 23 | A-54's four corrections | ① the dependency columns of F-208 / F-209 / F-210 / F-211 / F-207 are recomputed as "the item taken over → the producing task", and §1 and §2 are identical character for character; ② the 21 boundaries are distributed as 14 + 7 between F-210 / F-211; ③ table A row 23's `orphanCount` belongs to F-188, and row 24 keeps only F-193's judgment (F-194's write point is in the events class); ④ F-185's title is unified to the long form in both places | **pass** |

---

## Appendix A Task ↔ Decision ↔ Acceptance Three-Way Index

The "document 30 §7" column gives the acceptance numbers this task is directly responsible for; the "document 29 §11" column gives the boundary numbers it is directly responsible for; `—` means the task has no direct counterpart (its verification comes from elsewhere, such as the scope of a `git diff` or a count).

| Task | Decisions | Document 30 §7 | Document 29 §11 | Risk |
|---|---|---|---|---|
| F-176 | D-124, D-147 | #2 (the artifact name), #3's carrier | — | RT-38 |
| F-177 | A-40 (document 30 §3 row 43) | #38 (the closing point) | — | RT-40 |
| F-178 | D-125 | — (the mechanism level, acceptance written inside the task) | B-38 | RT-37 |
| F-179 | D-126, A-32 | #23 | B-38, B-39 | RT-37 |
| F-180 | D-130, A-31 | #24, #25, #26, #27, #28 | B-36 | — |
| F-181 | D-141 | #33 | B-37 | RT-36 |
| F-182 | D-127, D-128, D-129 | #2, #3, #4 | B-28 | RT-33, RT-38 |
| F-183 | D-129, A-30 | #34 | — | — |
| F-184 | D-129 | #35 | B-26, B-27 | — |
| F-185 | D-131, D-132, D-133, A-33 | #30, #31 | B-29, B-30, B-31, B-32, B-34, B-35 | RT-34, RT-35 |
| F-186 | D-133 | #32 | B-33 | RT-35 |
| F-187 | D-138 | — | — | — |
| F-188 | D-134, D-137, A-47 | #16 | — | RT-32 |
| F-189 | A-39, D-142, D-138 | #9 (the comparison-key face) | B-12 | RT-31 |
| F-190 | D-137, A-47 | #19 | B-23, B-24, B-25 | RT-32 |
| F-191 | D-139, D-138 | #17 | — | RT-33 |
| F-192 | D-139, A-42 | #2, #5 | — | RT-40 |
| F-193 | D-135, A-39 | #17, #18 | B-15, B-16, B-17 | RT-33 |
| F-194 | D-135 | #16, #18 | — | — |
| F-195 | D-138, A-45 | — | B-15, B-18 | — |
| F-196 | D-136, A-35 | #20, #21, #22 | B-19, B-20, B-21, B-22 | RT-32 |
| F-197 | D-140 | #12 | — | RT-40 |
| F-198 | D-145, A-38, A-43 | #36 | — | RT-39 |
| F-199 | D-142 | #6 | B-14 | RT-31 |
| F-200 | D-142, A-39 | #7, #8, #9, #29 | B-04, B-05, B-12 | RT-31 |
| F-201 | D-144 | #11 (the switch's carrier), #13 | — | — |
| F-202 | D-144 | #11 | — | — |
| F-203 | D-143, A-34, A-41 | #7, #8, #10 | B-01, B-02, B-03, B-06, B-10, B-11, B-13 | RT-31 |
| F-204 | D-144 | #14, #15 | B-07, B-08, B-09 | — |
| F-205 | D-144 | #12, #13 | — | — |
| F-206 | document 30 §4.3, the knock-on of D-129 / D-144 | #5 | — | RT-40 |
| F-207 | D-124, D-147, A-38 | #36 | — | RT-41 |
| F-208 | document 30 §7 #1 – #19 | #1 – #19 | — | — |
| F-209 | document 30 §7 #20 – #38, A-40 | #20 – #38 | — | — |
| F-210 | document 29 §11 B-01 – B-20, A-46 | — | B-01 – B-20 (**20 entries, one-to-one**) | — |
| F-211 | document 29 §11 B-21 – B-39, A-46 | — | B-21 – B-39 (**19 entries, one-to-one**) | — |
| F-212 | all of D-124 ~ D-147, A-42 ~ A-49 | #37, #38 | all **39** entries (the known limitation A-49 has no pass item) | all 11 entries |

Three reconciliation notes:

1. **Decision coverage**: D-124 ~ D-147, 24 entries, each appearing at least once in some task's "Decisions" column; D-129 appears 4 times (F-182 / F-183 / F-184 / F-206), because it governs the values, the constant's deletion, the convergence and the language keys — four places at once.
2. **Acceptance coverage**: document 30 §7's #1 ~ #38 are fully covered by the two batches F-208 (#1–#19) and F-209 (#20–#38); this table marks only the ones a task is "directly responsible for", to avoid writing 37 rows as 38 rows.
3. **Boundary coverage**: document 29 §11's B-01 ~ B-39 are fully covered by the two batches F-210 (B-01–B-20) and F-211 (B-21–B-39); **36 of them** are already asserted at a single point in an implementation task's acceptance (marked one by one in this table), and the remaining 3 (B-23, B-24, B-25) are covered by F-211 alone — all three depend on building "a legacy save + the capacity hard cap", which does not suit hanging on some implementation task's acceptance.
