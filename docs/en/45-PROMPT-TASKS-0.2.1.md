# 45 · v0.2.1 Phase-by-Phase Task List (Phase Prompts)

> How to feed it: **feed the master prompt (document 44) once first**, then **when a Phase starts, feed that Phase's section only**; feed only one section at a time. Every section is executable on its own from beginning to end — hand one section to the implementation agent alone, and it needs neither the previous section nor any other section of this file.

| Item | Value |
|---|---|
| Document number | 45-PROMPT-TASKS |
| Version | v0.2.1 (MC 1.21.1 / NeoForge 21.1.213 / Java 21) |
| Readers | the person feeding a coding agent + the CLI coding agent being fed |
| Upstream | `43-TASKS-0.2.1.md` (**the only source of F-247 ~ F-266**: numbers, titles, Phase ownership, complexity, person-days, dependencies), `42-TECH-0.2.1.md` (the registry surface 97 in §1.2 and the 13th category in §1.2.1, the five building blocks of §2, the 31-row action surface of §3, the config and the language keys of §4, the risks RT-51 ~ RT-60b of §6, the 31 acceptance entries of §7, the 10 to-be-verified items of §8, the 16 deltas of §9), `41-DESIGN-0.2.1.md` (the 5 requirements R1 ~ R5, B-01 ~ B-36), `.work/v021-BRIEF.md` (17 rulings) |
| Peer | `44-PROMPT-MAIN-0.2.1.md` (the resident master prompt, fed once only, produced in the same batch as this file). **This file does not repeat** its hard constraints, red-line wording and execution discipline; every section restates only the five lines of §0.2, and every section is executable without document 44 |
| Content | **6 Phases / 20 tasks (F-247 ~ F-266)**; every section has the same six subsections: Preconditions / Goal of this phase / Task list / Exit criteria / Pitfalls / Reporting format |
| Exit criteria | **45 entries** across the 6 sections (4 ~ 10 per section), all starting with `- [ ]`; preconditions use an ordinary list, so `grep -c '^- \[ \]'` counts exactly the criteria. A criterion only proves "this section's own tasks" and adds no new acceptance dimension; the full wording is in Phase 45: **31** entries of document 42 §7 + **36** entries of document 41 §9 = **67** entries (F-264 / F-265 of document 43 take them over one by one) |
| Boundary | read the upstream only: change none of `docs/zh/40` ~ `docs/zh/43`, nothing under `docs/en/`, nothing in `src/`, nothing in `tools/`, nothing under `.work/`; create this file only |

---

## 0. Feeding order and constraints

### 0.1 The feed order of the six sections (Phase 40 → 45) and each section's preconditions

| Order | Which section to feed | Phase · milestone | Tasks | Person-days | Preconditions of this section (do not open unless every one holds) |
|---|---|---|---|---|---|
| 1 | §1 | 40 · **M36** | F-247 | 0.5 | the work tree is clean (`git status` shows only the **3** files this section changes); `.tools\gw.cmd build` exits 0; the baseline is **the released 0.2.0 state** (`mod_version=0.2.0` at `gradle.properties:11`). **This section is the first Phase and has no previous section** |
| 2 | §2 | 41 · **M37** | F-248 ~ F-250 | 4.5 | **entries 1 / 2 / 3** of §1.4 are green (a new jar produced, `0.2.1` hits 6 places, `git diff gradle.properties` only 1 line); `OutpostCoreBlockEntity` has **782** UTF-8 lines; `serverTick` still does three things |
| 3 | §3 | 42 · **M38** | F-251 ~ F-256 | 9.5 | **entries 6 / 7 / 8** of §2.4 are green (filled in within 100 ticks at both particle intervals 40 and 1200, stopped at 16, a core with no owner fills in nothing); `Cultivators` is readable and the rebind INFO line is in place |
| 4 | §4 | 43 · **M39** | F-257 ~ F-260 | 3.0 | **entries 1 / 2 / 3** of §3.4 are green (the mount entrance is unique, the 13th category is registered explicitly, the startup log is still 97); the three readings of `NoAI` (missing key / `0b` / `1b`) have been recorded once |
| 5 | §5 | 44 · **M40** | F-261 ~ F-263 | 2.5 | **entries 3 / 4 / 5 / 6** of §4.4 are green (4 keys inside `[trap]`, the group count unchanged, 1 key each in zh / en, only 1 of the 7 call sites passes the 9th argument) — F-258 has opened the window and this section closes it in three places |
| 6 | §6 | 45 · **M41** | F-264 ~ F-266 | 7.5 | **all 7 entries** of §5.4 are green (the three closes are done); `.work/v021-verify/` is writable; **17** tasks of F-247 ~ F-263 have landed |
| 7 | close-out | not inside any Phase | — | — | §6.4 all pass: **67** entries (31 + 36) and all **20** tasks have a conclusion |

Three notes on reading the table:

1. **The order is the feed order, not the only executable order.** Among the six dependency chains of document 43 §3.1, chain 2 (`F-248 → F-249 → F-250`) and chain 3 (`F-251 / F-252 → F-253 → F-254 / F-255`) **do not intersect** and may go to two people in parallel; but **feed one section at a time**. In parallel, the preconditions of §3 count only its own chain (F-251 / F-252) and need not wait for the rebind acceptance of §2; every "previous section" in the table is read as a **process gate**, not as a code dependency.
2. **§5 must not be moved before §4**: the parser of F-261 has to read that one row of document 42 §4.1.1, and that row is written by F-258; the third reading of F-262 is again the tool union of F-261. The three closes are a one-way chain §4 → §5 in dependency, and feeding them the other way round is "taking a document that has no key yet and adding a parser to it".
3. **F-264 of §6 waits for all 17 tasks of §1 ~ §5 to land**, and the three closes (F-261 / F-262 / F-263) must come **before** acceptance — otherwise acceptance checks against a spec whose "document comment still says the old number while the actual key set is already the new one".

### 0.2 The five lines that must be restated at the head of every section (context-independent)

After the master prompt (document 44) has been fed once, paste the five lines below **verbatim** at the head of every feed (after document 44, before the body of this section). These five lines are the only wording of this section that may be "executed without explanation"; the section does not repeat them:

```text
① Two red lines: Mixin 0 (@Mixin and every *.mixins.json under src/ at 0 hits); custom network packets 2 in total
   (farmstorm:core_list / farmstorm:cultivator_link, PROTOCOL_VERSION = "2"), and this version adds none.
② The in-between window = F-258 (opens) → F-263 (closes). Inside the window write only "this key exists / does not exist" + "4 keys inside [trap]"
   + "the interval reading of the whole-file key count (one value before the old toml is regenerated, one after)"; each of the three totals is
   asserted once, in its own close-out task only: tool expectation surface = F-261, config side = F-262, language side = F-263.
③ A toml reading recognises only run/config/farmstorm-server.toml, and run one runServer to regenerate it before taking the reading;
   run-*/config/ is an old copy of about 121 keys / 36 sections — take the wrong one and the baseline is wrong.
④ The false green of A-66: tools/verify_project.py reads only the fixed subsections of 6 fixed documents (01 / 12 / 18 / 24 / 30 / 36),
   it does not read document 42 ⇒ "writing trap.damageMultiplier into document 42" is necessary but not sufficient, and it must land with F-261's
   expected_config_keys_v021 parser in the same commit.
⑤ The 13th category farmstorm:ai_suspend (ATTACHMENT_TYPES) does not count towards 97; the tool checks neither its existence nor its count
   ⇒ a criterion may only write "explicitly registered in the document" + "the startup log is still 97", never "the tool passes".
```

### 0.3 Quick reference (one screen: Phase ↔ milestone ↔ task IDs ↔ person-days ↔ exit criterion in one sentence)

| Phase | Milestone | Task IDs | Count | Person-days | Exit criterion (one sentence) |
|---|---|---|---|---|---|
| 40 Version and baseline | M36 | F-247 | 1 | 0.5 | the artifact `farmstorm-0.2.1+mc1.21.1.jar`; `0.2.0` hits **0**, `0.2.1` hits **6** |
| 41 R1 periodic rebind | M37 | F-248 ~ F-250 | 3 | 4.5 | only `getChunkNow` is used; both particle intervals **40** and **1200** fill in within **100** ticks; it stops at **16**; a core with no owner fills in nothing |
| 42 R2 tooltip and the AI intervention of R3 / R4 | M38 | F-251 ~ F-256 | 6 | 9.5 | `farmstorm:ai_suspend` explicitly registered and 97 unchanged; the Vibration Bean returns `NoAI` to the original value after **80** ticks; all three Q-Bounce paths restore within **1** tick; mount is never deleted throughout; the tooltip is visible when hovering any of the **90** slots |
| 43 R5 trap ×3 and the config window | M39 | F-257 ~ F-260 | 4 | 3.0 | Blast Carrot **21.0** against a zombie / **7.0** against a player; Zangief Cane **22.5**; the 6 non-trap readings unchanged at multipliers **1.0** and **10.0**; **4** keys inside `[trap]` |
| 44 The numeric surface and the tool close-out | M40 | F-261 ~ F-263 | 3 | 2.5 | each of the three closes asserts its own total once only; the toml readings come from the regenerated `run/config/` |
| 45 Full acceptance and signature | M41 | F-264 ~ F-266 | 3 | 7.5 | 31 + 36 = **67** entries, not one missing; the signature carries **8** readings and the 13th-category registration |
| **Total** | **M36 ~ M41** | **F-247 ~ F-266** | **20** | **27.5** | bare person-days 27.5 (`7 × 0.5 + 10 × 1.5 + 3 × 3.0`), with the 25 % buffer **34.375**; critical path **13.5** person-days / 6 tasks |

### 0.4 The reading ledger of the in-between window

| Window | Opens | Closes | What may be written mid-window only | Changed all at once when the window closes |
|---|---|---|---|---|
| config side | **F-258** | **F-262** | "this key exists" (**4** keys inside `[trap]`, `damageMultiplier = 3.0` appears **1** time) + the **interval reading** of the whole-file key count | the three readings — the line-leading `.define*` of the source, `b.push(` / `b.pop()`, and the regenerated `run/config/farmstorm-server.toml` — written into the criteria of F-262 |
| tool expectation surface | **F-258** | **F-261** | "the parser returns **1** key" + `grep -c 'trap\.damageMultiplier'` ≥ **2** | the expected count and the union, written into the criteria of F-261 |
| language side | **F-258** | **F-263** | "this key appears **1** time each in zh / en" + zh-only / en-only both **0** | the key sets of the two lang files and the class comment at `ModLangProvider.java:13-16`, written into the criteria of F-263 |

Three notes on reading it:

1. **The three closes do not sit in the same task**: the tool expectation surface (F-261) comes before the config ledger (F-262), because the third reading of F-262 is the tool's union; the language side (F-263) depends on F-258 alone and may run in parallel with F-261 / F-262.
2. **No exit criterion of any section inside the window cites a total**: writing a total means one count per section, and inside the window one count is bound to land on the half-done state "key added, translation added, comment not changed".
3. **The closes must come before F-264**: acceptance has to check against a spec that is already aligned, not against a document whose comment still sits at the previous version's number.

---

## 1. Phase 40 · Version and baseline (M36)

### 1.1 Preconditions

- The work tree is clean: `git status` shows only the **3** files this section changes (`gradle.properties`, `一键构建.cmd`, `检查环境.cmd`).
- `.tools\gw.cmd build` currently exits **0**; the only recognised build entry point is it — do not call gradle directly.
- The baseline is **the released 0.2.0 state**: `gradle.properties:11` is `mod_version=0.2.0`; `build/libs` holds `farmstorm-0.2.0+mc1.21.1.jar` at this moment.
- These three at hand: the F-247 row of the document 43 §1 table and A-70 of §5.1; document 42 §3 #1 ~ #5; rows 1 and 2 of `BRIEF` §3.4.
- By **line** the version string sits in **6** places (A-70): 1 at `gradle.properties:11` + 3 at `一键构建.cmd:5,11,12` + 2 at `检查环境.cmd:5,14`; the "4 places" recorded in document 40 §3.2 is corrected by A-70, and this section writes 6 throughout.
- This section is **before the in-between window** (the window opens only at F-258) and touches no numeric surface: the diff of `src/` and `run/` must be empty.
- Previous section: **this section is the first one** and has no previous section; its exit criteria are exactly §2's preconditions.

### 1.2 Goal of this phase

Nail the version number and the artifact surface of the two build scripts — it is the only source of every jar name and of the environment-check (`检查环境`) criteria.

At the end of this section: `build/libs` holds only the 0.2.1 artifacts, `0.2.0` falls to 0 among the 6 literals, and the three platform lines (`minecraft_version` / `neo_version` / toolchain) have not moved by one byte.

### 1.3 Task list (F-247)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-247 | Version number 0.2.1 and the 6 version strings of the two `.cmd` scripts | S | 0.5 | — | `mod_version` at `gradle.properties:11` becomes `0.2.1`; the banner lines / jar names of `一键构建.cmd:5,11,12` and `检查环境.cmd:5,14` follow (including the `-sources.jar` line) |

Phase subtotal: **1 task, 0.5 person-days** (S 1 / M 0 / L 0).

### 1.4 Exit criteria

- [ ] **Artifact name and version line (F-247)**: How to verify: after `.tools\gw.cmd build`, read `META-INF/neoforge.mods.toml` through `jar tf build/libs/farmstorm-0.2.1+mc1.21.1.jar`. What passing looks like: exit code **0**; the main jar and `farmstorm-0.2.1+mc1.21.1-sources.jar` both in place; **0** leftovers of `farmstorm-0.2.0*` in `build/libs`; the `version` line is `0.2.1`.
- [ ] **6 literals changed in one go (F-247 / A-70)**: How to verify: count the hit lines of `0.2.0` and `0.2.1` in the three files one by one. What passing looks like: `0.2.0` hits **0**; `0.2.1` hits = **1** (`gradle.properties`) + **3** (`一键构建.cmd`) + **2** (`检查环境.cmd`) = **6**; both banner lines (each at `:5`) and the `-sources.jar` of `一键构建.cmd:12` are among them.
- [ ] **The three platform lines untouched (negative)**: How to verify: the hunk count of `git diff gradle.properties`. What passing looks like: only line **11** changes; `minecraft_version` / `neo_version` / toolchain are unchanged character for character.
- [ ] **This section does not touch the numeric surface (negative)**: How to verify: `git status` and `git diff --stat src/`. What passing looks like: the diff of `src/` and `run/` is empty; no criterion of this section cites the key count or the group count of `run/config/farmstorm-server.toml`.

### 1.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Only the main jar changed, `-sources.jar` missed | `一键构建.cmd:12` still copies by the old name and sources are silently not produced | name the 6 places line by line; the total hits of `0.2.1` must = **6** |
| Only the jar name changed, the banner line missed | the artifact is right while the banner still says `Farmstorm v0.2.0` | each script's `:5` counts as one place of its own; count the banner lines and the jar names separately |
| `检查环境.cmd:14` missed | after the rename the jar is not found and the environment check reports "jar: 缺失" (which is really the rename) | that line sits in the same paragraph as the prompt text; run `检查环境.cmd` for real once afterwards |
| The version number written as a second source | documents and scripts each declare a version and the two drift | the version number has one source only, `gradle.properties:11`; everywhere else references it |
| The platform parameters changed in passing | `neo_version` or the toolchain is moved, the whole build cache is void and nothing is comparable to the baseline | `git diff gradle.properties` allows **1** line only |

### 1.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the "file:line" of each of the 6 literals named one by one.
- **Commands run**: the exit code of `.tools\gw.cmd build`; the `neoforge.mods.toml` entry of `jar tf`; the two rounds of `grep -c` on the three files; the changed-line count of `git diff gradle.properties`.
- **What was observed**: the complete listing of `build/libs` (including 0 leftovers of `0.2.0*`); the verbatim `version` line; the output of one real run of `检查环境.cmd`.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example a `.cmd` cannot run outside Windows; state the substitute reading).
- **Open questions**: for example a side artifact such as `-dev.jar` in `build/libs` — register the phenomenon only; this section does not change the output surface of the build scripts.

---

## 2. Phase 41 · R1 periodic rebind (M37)

### 2.1 Preconditions

- **entries 1 / 2 / 3** of the previous section §1.4 are green: `farmstorm-0.2.1+mc1.21.1.jar` produced, `0.2.1` hits 6 places, `0.2.0` hits 0, `git diff gradle.properties` only 1 line.
- Baseline readings (UTF-8): `blockentity/OutpostCoreBlockEntity.java` **782** lines; `serverTick` still does three things (`:508-510` / `:511-513` / `:516-518` / `:519` / `:521-524`); `RANGE_CHUNKS = 1` (`:75`), `TICK_LOG_ROUNDS = 30` (`:91`).
- These four at hand: F-248 ~ F-250 of document 43 and A-71 of §5.2; document 42 §2.1.1 ~ §2.1.4; document 41 §2.2 ~ §2.7 and B-01 ~ B-12 of §9; `BRIEF` §1.1 (including the 📌 about `CoreChunkTickets`).
- Frozen surface: `FarmstormConfig` does not move by one line (`b.push(` **60**, `b.pop()` **60**, line-leading `.define*` **175**); `setPlacedBy` at `CultivatorBlock:85-102` does not move by one line; `removeCultivator` still has **0** call sites.
- Line numbers are UTF-8 throughout (`[IO.File]::ReadAllLines($p, UTF8)`): the default `Get-Content` reads the same file as **668** lines and every line number shifts afterwards.
- This section is **before the in-between window**, so the baseline total **175** may be asserted; but this section **adds no config key and no language key**, and once the window opens only "this key exists" is left to write.
- Readings come from four kinds only: file counts and key counts, log lines, `/data get block`, source `grep` counts; never "it feels right", "normal" or "about right".

### 2.2 Goal of this phase

Let the core fill its 16 places at runtime every 100 ticks with the "nobody wants it" cultivators inside the 3×3 loaded chunks, and not be filtered out by the existing particle phase gate. Three things in three commits: the area enumeration tool (F-248), the two methods (F-249), the wiring + the comment + one INFO (F-250).

At the end of this section: with `boundaryParticleIntervalTicks` set to 40 and to 1200, the rebind both happens within **100** ticks; it stops at 16; a core with no owner fills in nothing.

### 2.3 Task list (F-248 ~ F-250)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-248 | `util/AreaBlockEntities#collect` (area enumeration of block entities, 0 precedent) | M | 1.5 | — | a new `util/AreaBlockEntities.java` (the only method `collect`, goes through `getChunkNow` only, returns a new `ArrayList`) |
| F-249 | The phase gate and the write boundary of rebind: `rebindDue` + `rebindNearby` | M | 1.5 | F-248 | the constant region of `OutpostCoreBlockEntity` gains `REBIND_INTERVAL_TICKS = 100`; the method region gains `rebindDue` and `rebindNearby` (no wiring in this task) |
| F-250 | `serverTick` wiring + the comment at `CultivatorBlock:73-84` changed in the same pass | M | 1.5 | F-249 | `OutpostCoreBlockEntity:507-525` (`now` moves up one line + an independent gate + one rebind INFO), the class comment at `CultivatorBlock:73-84` |

Phase subtotal: **3 tasks, 4.5 person-days** (S 0 / M 3 / L 0).

### 2.4 Exit criteria

- [ ] **Area enumeration goes through loaded chunks only (F-248 / RT-52)**: How to verify: read `util/AreaBlockEntities.java` and count the call sites. What passing looks like: `getChunkNow` has **1** call site; `getChunk(` / `getChunkAt(` / `getBlockEntity(` have **0** hits; `new ArrayList` ≥ **1** (the live map is copied before iterating); `type.isInstance` ≥ **1** and `BlockEntityType` **0** hits.
- [ ] **It compiles and the Java file count is +1 (F-248)**: How to verify: `.tools\gw.cmd build` + count the `.java` files under `src/main/java`. What passing looks like: exit code **0**; the file count **145 → 146** (UTF-8 reading).
- [ ] **The phase gate is independent and the modulus fixed (F-249 / document 42 §2.1.1)**: How to verify: read the constant region and the body of `rebindDue`. What passing looks like: the declared value of `REBIND_INTERVAL_TICKS` = **100**; the modulus in `rebindDue` is **100** and the word `interval` does not appear in the method body; there is no `interval <= 0` guard borrowed from the particle gate.
- [ ] **The three prohibitions of the write boundary (F-249 / ruling #13 / #14)**: How to verify: `grep` inside the body of `rebindNearby` + count the comparison chain. What passing looks like: the first line is the `ownerUuid == null` return; `pruneCultivators(` / `removeCultivator(` / `cultivators()` have **0** hits each; `addCultivator(` and `setCore(` have ≥ **1** each and the former appears first in the text; `comparingDouble` **1** time + `thenComparingInt` **3** times (`x` → `y` → `z`).
- [ ] **The two call sites in the whole repo were not switched on in passing (F-249 / negative)**: How to verify: `grep` the two method names repo-wide. What passing looks like: `pruneCultivators(` still has **1** call site (`OutpostCoreBlockEntity:329`, inside `cultivators()`); `removeCultivator(` still has **0** call sites (only the declaration at `:366` and the javadoc mention at `CultivatorLinkBroadcaster:35`).
- [ ] **Filled in within 100 ticks at particle interval 40 (F-250 / document 42 §7 #6)**: How to verify: a core bound to 0 + **1** unbound cultivator placed inside the 3×3 + `boundaryParticleIntervalTicks = 40`; wait **100** ticks and run `/data get block <core> Cultivators`. What passing looks like: that coordinate appears in `Cultivators`; one rebind INFO appears in the log (core coordinate + the number added this round + cumulative / cap).
- [ ] **Still filled in within 100 ticks at particle interval 1200 (F-250 / decisive)**: How to verify: the same scenario with `boundaryParticleIntervalTicks` changed to **1200**. What passing looks like: it is still filled in within **100** ticks — an implementation reusing the particle gate would wait **1200** ticks here; this entry and the previous one **appear as a pair**, and one of them missing means not passed.
- [ ] **Full stops, no owner no fill, comment changed in the same pass (F-250 / RT-53 / A-71)**: How to verify: three sets of readings + read the comment. What passing looks like: **20** unbound cultivators inside the 3×3 + `cultivatorCapacity = 16` ⇒ `Cultivators` holds exactly **16** entries and still 16 after **300** more ticks; a `/setblock` core with no owner + **1** unbound ⇒ still empty after **300** ticks; in `CultivatorBlock.java` the hits of "不自动认领" and "不会绑上" are **0**, `:99` / `:100` of `setPlacedBy` are unchanged character for character, and the class is still **162** lines.

### 2.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Wiring the rebind after the particle gate | the real period is filtered into `LCM(40, 100) = 200` ticks and drifts with `boundaryParticleIntervalTicks` between 20 and 1200; the 1200 test goes red at once | an independent `if`, placed **before** the particle gate, with **no** `return`; criterion 7 exists for exactly this |
| Borrowing the `interval <= 0` guard of the particle gate | `floorMod(x, 100)` never throws, so one more guard is one more misreading | `REBIND_INTERVAL_TICKS` is the compile-time constant 100; do not set that guard |
| Using `Level#getChunk` / `getBlockEntity` to save trouble | 9 chunks are pulled into memory every 100 ticks (RT-52) | `getChunkNow` only; the three loading APIs must have **0** hits |
| Iterating the live map of `chunk.getBlockEntities()` directly | adding or removing during iteration ⇒ `ConcurrentModificationException` | copy with `new ArrayList` first, then filter (two-phase write) |
| Calling `removeCultivator` in passing as a fallback for "the two sides disagree" | a second owner is created; the call count of that method goes from 0 to 1, which conflicts with D-177 | any disagreement between the two sides is **skipped**; `removeCultivator(` stays **0** |
| Writing `REBIND_INTERVAL_TICKS` as a config key | the config of this version becomes +2 keys, which directly conflicts with the "+1 only" of D-185 | a constant is not a key; `b.push(` stays **60** and line-leading `.define*` stays **175** |
| The comment and the code committed in two goes | a window is left where "the comment says no while the code does it" (A-71) | the wiring, the comment at `CultivatorBlock:81-83` and the rebind INFO in **the same commit** |
| The `y` level missed in the ordering | two cultivators with the same x/z and different y order unstably, and two runs of the same save differ | `thenComparingInt` exactly **3** times; the comparison chain named level by level |

### 2.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the new file of F-248 and the line numbers of the four kinds of call sites, the constant and the two methods of F-249, the `:515` shift + the independent gate + the INFO line + `CultivatorBlock:81-83` of F-250.
- **Commands run**: the exit code of `.tools\gw.cmd build`; the file count of `src/main/java`; the repo-wide `grep -c` of the two method names; three server starts (default 40 / changed to 1200 / the full-16 scenario).
- **What was observed**: the **complete verbatim text** of the rebind INFO (core coordinate + the number added this round + cumulative / cap); the three sets of readings of `/data get block <core> Cultivators` (1 / 16 / empty); the line count of `CultivatorBlock.java` and the zeroing result of the two old comments.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example "chunk not loaded" needs walking away or `forceload remove`; state the operation and the number of ticks waited).
- **Open questions**: for example when two cores in the same 3×3 each hold 1 unbound cultivator, whether the `gameTime` of the two INFO lines is equal (the wording of document 42 §7 #8); this section records the phenomenon and draws no conclusion.

---

## 3. Phase 42 · R2 tooltip and the AI intervention of R3 / R4 (M38)

### 3.1 Preconditions

- **entries 6 / 7 / 8** of the previous section §2.4 are green: filled in within 100 ticks at both intervals 40 and 1200, stopped at 16, no owner no fill; the structural readings of entries 1 ~ 5 and the two prohibitions are green as well.
- These four at hand: F-251 ~ F-256 of document 43 and A-77 / A-79 / A-80 / A-81 of §5.8 ~ §5.12; document 42 §1.2.1, §2.2, §2.3.1 ~ §2.3.7; document 41 §3, §4, §5 and B-13 ~ B-30 of §9; rulings #3 ~ #8 and #15 ~ #17 of `BRIEF` §2.
- Frozen surface: `OutpostCoreScreen` (**42** lines) is expected to change **zero**, overriding `renderBg:37-41` only; `QbounceMountController:199` changes **1** modifier only; the 12-category assertion at `Farmstorm.java:169-172` does not move by one line.
- This section is still **before the in-between window**: `FarmstormConfig` does not move by one line (line-leading `.define*` still **175**, `b.push(` / `b.pop()` still **60**), and the two language-key baselines are still **337 × 2**; this section **adds no config key and no language key**.
- **A-80's wording**: when the chunk holding the entity is unloaded it does **not** "restore by the original value + delete the record" (at that moment the entity is not in the world and `setNoAi` has nothing to write), but **keeps the attachment and settles on load**; the opposite wording of document 41 §4.5 / §5.5 is not adopted per A-80.
- **A-81's wording**: repo-wide the writes of `setNoAi` number **2** (`WildHunterAura:92` / `:119`); `:91` is an `isNoAi()` test, not a write point; the original value is **persisted to disk** (ruling #15), not a memory table.
- Line numbers are UTF-8 throughout; `OutpostCoreMenu` **140** lines, `QbounceEffect` **20** lines and `VibrationBeanTrap` **106** lines are the comparison base of this section.
- Known wording: `qbounce.poseMode` defaults to `render` ⇒ under the default config the mount branch does not take effect; when the Vibration Bean and mount hit on the same tick there is **no exemption** (document 42 §2.3.7); test the two separately, never merged.

### 3.2 Goal of this phase

Build a mechanism for "suspend AI" that can be persisted, restored, and covers the five cleanup paths, wire it into the Vibration Bean and Q-Bounce, and at the same time turn the core storage tooltip from "it was always there" into "it has an acceptance".

At the end of this section: `NoAI` of the Vibration Bean is back to the original value after 80 ticks, all three removal paths of Q-Bounce restore within 1 tick, mount is never deleted throughout, hovering the 54 slots shows the tooltip, and not one line of rendering code or one container slot has been added.

### 3.3 Task list (F-251 ~ F-256)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-251 | `isMountMode` visibility (`private` → `public`) | S | 0.5 | — | the **1** modifier at `behavior/QbounceMountController:199` (the method body does not move by one line) |
| F-252 | The attachment carrier of the original AI flag: `Suspend` + `farmstorm:ai_suspend` (the 13th category) | M | 1.5 | — | a new `registry/FarmstormAttachments.java` + the data-class part of `combat/MobAiSuspension.java` + **+1** line in `Farmstorm.java` |
| F-253 | `MobAiSuspension`: record the original value on entry / restore by it on exit / the five cleanup paths | L | 3.0 | F-251, F-252 | the behaviour part of `combat/MobAiSuspension.java`: `suspend` / `release` / `adopt` / `sweep` + two in-memory containers |
| F-254 | Vibration Bean: suspend AI on every non-player mob in range for 80 ticks | M | 1.5 | F-253 | a block appended to `behavior/impl/VibrationBeanTrap:74-105` + `AI_SUSPEND_TICKS = 80` + one INFO |
| F-255 | Q-Bounce: suspend AI for the duration of the effect, skip mount | M | 1.5 | F-251, F-253 | `effect/QbounceEffect.java` overrides **1** method; the two-level mount test of `MobAiSuspension` |
| F-256 | The core storage tooltip: guarantee the default path + 3 acceptance entries | M | 1.5 | — | `client/OutpostCoreScreen.java` (expected to change **zero**) + 3 acceptance records (re-run at F-264) |

Phase subtotal: **6 tasks, 9.5 person-days** (S 1 / M 4 / L 1).

### 3.4 Exit criteria

- [ ] **The mount entrance is unique (F-251 / ruling #7)**: How to verify: count the declaration and the read points + read the line count. What passing looks like: `QbounceMountController.java` is still **376** lines; repo-wide `isMountMode` has exactly **1** **declaration** whose modifier is `public static` and whose body is still the one line `POSE_MODE_MOUNT.equalsIgnoreCase(...)`; repo-wide `qbouncePoseMode` still has only **1** read point, `:200`.
- [ ] **The 13th category explicitly registered (F-252 / A-73)**: How to verify: read `registry/FarmstormAttachments.java` and `combat/MobAiSuspension.java`. What passing looks like: exactly **1** `AttachmentType` registered, the id literal `ai_suspend`, the namespace `farmstorm`; `Suspend` has exactly **2** fields; `deadline` / `expire` / `timestamp` **0** hits.
- [ ] **The registration line +1 while 97 unchanged (F-252)**: How to verify: count the `.register(modBus)` of the `Farmstorm.java` constructor + read the startup log. What passing looks like: **11 → 12** register calls; the 12-category assertion at `:169-172` unchanged character for character; `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`.
- [ ] **The tool's coverage gap written into the record (F-252 / A-73)**: How to verify: read the acceptance record of this task. What passing looks like: it states that the 12 groups of ids in `EXPECTED` contain no `ai_suspend` and that `EXPECTED_COUNTS` still adds up to 97 ⇒ the tool **checks neither the existence nor the count**; no criterion of this section writes "the tool passes".
- [ ] **The semantics of the original value (F-253 / ruling #8)**: How to verify: `/data merge entity <mob> {NoAI:0b}`, trigger one suspension, then `/data get entity`. What passing looks like: `NoAI:1b`; after the restore `NoAI` is back to **0b** and `neoforge:attachments.farmstorm:ai_suspend` is **not visible** in the entity; a mob that already had `NoAI:1b` **stays** at `NoAI:1b` after the same flow.
- [ ] **Server stop and crash recorded separately (F-253 / ruling #16 / A-80)**: How to verify: `/stop` once and a hard kill of the process once. What passing looks like: at `/stop` the 80 ticks are not up ⇒ after the restart `NoAI` is **0b** (restored early) and the attachment holds no `farmstorm:ai_suspend`; after a hard kill and a restart ⇒ settled from `sources` on load (restored in **≤ 1** tick) and the attachment is gone.
- [ ] **The single exit and zero event subscriptions (F-253 / A-79)**: How to verify: three `grep` runs + read the packet surface. What passing looks like: `removeData(` has exactly **1** hit (the only write exit); `LivingDeathEvent` / `MobEffectEvent` **0** hits; upper-bound constants of the kind "give up after how many ticks it cannot be observed" **0** hits; `.playToClient` still **2**, `PROTOCOL_VERSION = "2"`, the attachment carries no `.sync(...)`.
- [ ] **The Vibration Bean's range and duration (F-254 / ruling #4, #5)**: How to verify: two scenarios + count the constants. What passing looks like: **1** zombie in range (`NoAI:0b`) + 1 player in survival mode who is not the owner ⇒ after the trigger `NoAI:1b`, **0b** again after **80** ticks, and the player still gets `CONFUSION`; **3** mobs in range ⇒ all 3 are suspended and the round count in the log is **3**; `AI_SUSPEND_TICKS = 80`, `b.push(` still **60**, line-leading `.define*` still **175** (this task touches no config).
- [ ] **The three removal paths of Q-Bounce and mount (F-255 / RT-56)**: How to verify: one run of each of the three paths + switch poseMode. What passing looks like: while the effect is refreshed repeatedly the `sources` in the attachment stays `SRC_Q_BOUNCE` and `originalNoAi` does not change with later refreshes; natural expiry (`friend_chives.effectTicks = 200`) / `/effect clear` / milk all restore the original value within **1** tick; with `qbounce.poseMode = mount` ⇒ the `NoAI` of a mob carrying Q-Bounce stays **0b** and is suspended only at the **next application** after switching back to `render`; the override count of `QbounceEffect.java` = **1**, `applyEffectTick` / `isDurationEffectTick` **0** hits.
- [ ] **The tooltip's 4 overrides and 90 slots (F-256 / A-77)**: How to verify: hover in game + count the source. What passing looks like: hovering slot **1 / 28 / 54** ⇒ item name + detail lines ≥ **2** lines, and "1" is not shown when the count is 1; hovering an empty slot shows **no** tooltip; hovering a player-inventory slot agrees with the 54 slots; the override count of `render` / `renderTooltip` / `renderLabels` / `init` in `OutpostCoreScreen` = **0**; repo-wide `renderTooltip` = **0** hits; in `OutpostCoreMenu` `new Slot(` is **3** calls / **90** slots (54 + 27 + 9, not "54 + 36 times") and there is no `Slot` subclass.

### 3.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| `setNoAi` before the attachment is written | a crash in between leaves "no AI, no original value", the first entrance of RT-54 | fix the order of the three steps: read the original value → write the attachment → `setNoAi(true)` |
| Treating "cannot be observed (chunk unloaded)" as the entity being dead and deleting the attachment | the original value is lost and the entity is persisted with `NoAI:1b` (the second entrance of RT-54) | when it cannot be observed drop only the two in-memory pieces (the index + the expiry tick) and always keep the attachment; the upper-bound constant has **0** hits (A-79) |
| Adding a `deadline` / `expireAt` field to the attachment | back to the "record a point in time" shape criticised by D-179, and a time-rewind guard is needed on top | `Suspend` has exactly **2** fields; the three banned words have **0** hits |
| Using `hasData` to judge "is it suspended" | `hasData` may also be true after the default value has been read out, creating empty suspension records | the criterion recognises `sources != 0` only |
| Carrying the original value with `DataComponentType` | `data_components` 1 → 2, 97 → 98 (steps on D-175) | use `ATTACHMENT_TYPES`; write the 13th category into the record and **do not** count it in 97 |
| Configuring a one-shot timer in the effect class | the per-tick rescan of the level resets the timer (never restores) or restores early (RT-56) | the restore criterion = `hasEffect`; override `onEffectAdded` only |
| Taking "the tool passes" as the criterion of the 13th category | the tool checks neither the existence nor the count, so the criterion is as good as unwritten (A-73) | write "the registration name + the field count + the startup log 97" + "explicitly registered in the document" |
| Rewriting `render` in passing to draw the tooltip by hand | the 42-line class becomes a hand-drawn screen that bypasses the vanilla positioning / scaling / background (RT-59) | keep the 4 overrides at **0**; only when the hover fails enable a fallback of ≤ 2 lines |
| Using a creative / spectator account for the "does not trigger" reverse assertion of the Vibration Bean | that test goes red, but what is red is the existing wording (document 42 §9 entry 13) | always trigger with a player in survival mode who is not the owner and not a spectator |
| Adding the key or the translations of `trap.damageMultiplier` into this section in passing | the window opens early and the **175 / 337 × 2** baseline this section may assert is void on the spot (F-258 is the task that opens the window) | this section adds no config key and no language key; the key and the translations appear only in the same commit of F-258 |

### 3.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the 1 modifier of F-251, the two classes and the one line of `Farmstorm.java` of F-252, the four methods + two in-memory containers of F-253, the appended block and the constant of F-254, the 1 override of F-255, and the "zero change" evidence of F-256 (the 4 override counts).
- **Commands run**: the exit code of `.tools\gw.cmd build`; three server starts (normal / `/stop` / hard kill); `/data get entity <mob>` several times; four `grep -c` runs (`isMountMode`, `qbouncePoseMode`, `removeData(`, `renderTooltip`).
- **What was observed**: the three readings of `NoAI` (missing key / `0b` / `1b`) one line per scenario; the verbatim `/data get entity` of the attachment (key present / key absent); the round count of the Vibration Bean INFO; the restore ticks of the three removal paths; the comparison of the three hovered slots and the empty slot.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example a hard kill needs an on-site operation; state how it was killed and the ticks waited after the restart).
- **Open questions**: for example whether `EntityJoinLevelEvent` covers the "a chunk load puts an entity back into the world" case (document 42 §8 ⑥); this section records the reading only and does not characterise it; the fallback is a bounded rescan of 10 rounds.

---

## 4. Phase 43 · R5 trap ×3 and the config window (M39)

### 4.1 Preconditions

- **entries 1 / 2 / 3** of the previous section §3.4 are green: the mount entrance is unique, the 13th category is registered explicitly, the startup log is still 97; entries 5 / 8 / 9 (the original-value semantics, the Vibration Bean's 80 ticks, the three Q-Bounce paths) are green too — the readings of the multiplier surface rely on them to tell "the damage changed" from "the AI was suspended".
- These four at hand: F-257 ~ F-260 of document 43 and A-72 / A-76 of §5.3 / §5.7; document 42 §2.4, §2.5.1 ~ §2.5.3, §4.1.1; document 41 §6.2 ~ §6.7 and B-31 ~ B-36 of §9; rulings #9 ~ #11 of `BRIEF` §2.
- **The in-between window opens in this section (F-258)**: the opening action = writing the key `trap.damageMultiplier` and its Chinese and English translations in **the same commit**. Inside the window this section writes only "this key exists / does not exist", "**4** keys inside `[trap]`" and "175 or 176 in the whole file (interval reading)"; it **does not write** the totals of config and of the language keys.
- **A toml reading recognises `run/config/farmstorm-server.toml` only, and run one `runServer` to regenerate it before taking the reading**; the old copies under `run-*/config/` (about **121** keys / **36** sections) are never a baseline.
- Frozen surface: the 5 `getEntitiesOfClass(Monster.class, …)` unchanged character for character (`BlastCarrotTrap:59`, `FangTrapCactusTrap:54`, `VibrationBeanTrap:59`, `ZangiefCaneTrap:174`, `PumpkinTurret:109`); `blast_carrot.maxDamage` still **7**, `zangief_cane.slamDamage` still **7.5**; `FarmstormDamageTypes` still holds **3** `ResourceKey` of `damage_type`.
- The 6 non-trap call sites must be **zero change**: `MelonGrenadeProjectile:42`, `MelonSliceProjectile:40`, `MolotovProjectile:94`, `ToxicUbiServerProjectile:43`, `UbiServerProjectile:41`, `LuckyBerryRoller:212`.
- The class comment at `ModLangProvider.java:13-16` **does not move by one character** — it is changed in one go by F-263 (window discipline item 2).
- Line numbers are UTF-8 throughout; `ZangiefCaneTrap` **183** lines is the comparison base of this section.

### 4.2 Goal of this phase

Build a gate for ×3 that acts on **hostile mobs only**, and let the multiplier land only on **the one damage the trap settles itself** — only 2 of the 4 trap crops take part.

At the end of this section: Blast Carrot 21.0 against a zombie and 7.0 against a player; Zangief Cane 22.5 against a zombie; the 6 non-trap explosions (Melon Grenade / Molotov and the rest) read the same at multipliers 1.0 and 10.0; `[trap]` gains 1 key and the group count is unchanged.

### 4.3 Task list (F-257 ~ F-260)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-257 | The hostile predicate `util/Hostility#isHostile` (0 precedent) | S | 0.5 | — | a new `util/Hostility.java` (four branches, `Enemy` ∪ `MobCategory.MONSTER`) |
| F-258 | The `trap.damageMultiplier` key and its translations (**the window opens**) | S | 0.5 | — | +1 key in the `trap` group of `FarmstormConfig` (double, 3.0, 1.0 ~ 10.0); +1 Chinese and English translation in `ModLangProvider` |
| F-259 | The 9th argument `trapSourced` of `FarmstormExplosion` + multiplication point ① | M | 1.5 | F-257, F-258 | `FarmstormExplosion:69-78` (9 arguments + the 8-argument overload), the multiplication point at `:178-179`; `BlastCarrotTrap:84-86` passes `true` |
| F-260 | The multiplication point ② of `ZangiefCaneTrap#monsterBranch` + the log prints the multiplied value | S | 0.5 | F-257, F-258 | the multiplication point at `ZangiefCaneTrap:111-118`, the log value at `:126-128` |

Phase subtotal: **4 tasks, 3.0 person-days** (S 3 / M 1 / L 0).

### 4.4 Exit criteria

- [ ] **The hostile predicate's four branches (F-257 / ruling #9)**: How to verify: read `util/Hostility.java` + repo-wide `grep`. What passing looks like: the four branches exist in order (not a `LivingEntity` → false; a player → false; `instanceof Enemy` → true; `getType().getCategory() == MobCategory.MONSTER` → true); repo-wide the hits of `Enemy` go from **0** to **1** file; `isHostile` has **1** definition; the hits of `MobCategory` go from **2** (two `MISC`) to **3** (the new one is `MONSTER`).
- [ ] **The 5 trigger points unchanged character for character (F-257 / RT-57)**: How to verify: compare the five line numbers and their content character for character. What passing looks like: the `getEntitiesOfClass(Monster.class, …)` line at `BlastCarrotTrap:59`, `FangTrapCactusTrap:54`, `VibrationBeanTrap:59`, `ZangiefCaneTrap:174` and `PumpkinTurret:109` has not moved at all.
- [ ] **The first item of the opening: this key exists (F-258)**: How to verify: run `runServer` once to regenerate `run/config/farmstorm-server.toml`, then read the `[trap]` section and the source. What passing looks like: `[trap]` holds exactly **4** keys and `damageMultiplier = 3.0` appears **1** time; among the line-leading `.define*` of `FarmstormConfig.java`, `"damageMultiplier"` has **1** hit.
- [ ] **The second item of the opening: the group count unchanged, the whole file written as an interval only (F-258 / A-74)**: How to verify: count `b.push(` / `b.pop()`, then read the whole-file key count. What passing looks like: `b.push(` still **60**, `b.pop()` still **60** (`trap` is an existing group: only a key is added, no group is created); the whole-file key count is written only as "**175** or **176** (interval reading: the former when the old toml has not been regenerated, the latter after regeneration)" and **not** as a conclusion.
- [ ] **The translations and the key sets (F-258)**: How to verify: take the key sets of the two lang files with `ConvertFrom-Json` + read the class comment. What passing looks like: `zh_cn.json` and `en_us.json` each hold `farmstorm.configuration.trap.damageMultiplier` **1** time; zh-only = **0**, en-only = **0**; the class comment at `ModLangProvider.java:13-16` has not moved by one character.
- [ ] **The 9-argument constructor and the 6 zero changes (F-259 / A-72)**: How to verify: count `new FarmstormExplosion` and the two constructor declarations. What passing looks like: repo-wide `new FarmstormExplosion` is still **7**, **only** `BlastCarrotTrap:84-86` passes the 9th argument and the other 6 argument lists still stop at `damageCap`; there are exactly **2** constructor declarations (8 and 9 arguments) and the body of the 8-argument one is a single line delegating with `false`.
- [ ] **The two readings of the multiplier (F-259 / F-260 / A-76)**: How to verify: the in-game damage readings + the INFO line. What passing looks like: the capped reading of Blast Carrot against a zombie `min(raw, 7) × 3` is **21.0**, the same explosion against a player **7.0**; Zangief Cane against a zombie **22.5** (`7.5 × 3`, the "damage" field of the INFO line the same value; the 11.25 of document 42 §7 #19 is not adopted per A-76); with `damageMultiplier` at **1.0** and at **10.0** the readings of `melon_grenade` / `molotov` are **identical**.
- [ ] **The player branch, zero damage and the fangs not taking part (F-260 / RT-58)**: How to verify: two sets of readings + three character-for-character comparisons. What passing looks like: with `slamDamage` set to **0** ⇒ no `hurt` and the log damage is still **0.0**; a player stepping on it goes through the phase chain (`ZangiefPiledriverController`) and does not enter the multiplier surface; the line numbers of `ZangiefCaneTrap:174` are unchanged character for character and the class is still **183** lines; `FarmstormDamageTypes` still holds **3** `ResourceKey` of `damage_type`, and **only 2** of the 4 trap crops take part.

### 4.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Writing a total as a conclusion inside the window | every later section has to follow, and one of them will get it wrong (A-74) | write only "this key exists" + "4 keys inside `[trap]`" + the whole-file interval reading |
| Changing the class comment at `ModLangProvider.java:13-16` in passing | the comment no longer matches the key set and there is no baseline at the close (window discipline item 2) | the class comment does not move by one character; the close happens at F-263 |
| Counting keys on the toml that has not been regenerated | the copy on disk holds the old value and the reading is wrong outright | run `runServer` to regenerate first, then read `run/config/farmstorm-server.toml` |
| Taking an old copy under `run-*/config/` as the baseline | the conclusion "only 121 keys" comes out and the whole set of readings is void | hard-code the path as `run/config/`; old copies are only named in the record, never read |
| Writing the multiplication on the 8-argument constructor path | that equals ×3 on every explosion (exactly the mistake RT-57 / RT-58 are there to stop) | the multiplication point sits on the 9-argument path + the `trapSourced` gate only; the 6 non-trap readings do not change |
| Multiplying `damageCap` instead of the final damage | completely ineffective when `raw < cap` (`raw = 4` should give 12 and still gives 4) | multiply the `dealt` that comes after `min(raw, cap)` |
| Judging "is it a trap" by `damageSource.getEntity()` alone | a player-thrown Melon Grenade cannot be told from a player-deployed Blast Carrot | add a source flag; do not open a new damage type (otherwise 97 → 98) |
| Replacing the 5 `Monster` filters with the new predicate in passing | that changes the trigger surface, not the damage surface (RT-57) | the 5 line numbers unchanged character for character; `Hostility` has only **2** use sites |
| Forgetting that the log prints the multiplied value | the log and `hurt` do not agree and the multiplier readings of this phase are void | keep the log format string and replace the value with the multiplied one only |

### 4.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the new file and the four branch lines of F-257, the two places of F-258 (the `trap` group + the translation line) and the field declaration, the two constructors and the multiplication point of F-259, the multiplication point and the log value of F-260.
- **Commands run**: the exit code of `.tools\gw.cmd build`; one `runServer` to regenerate the toml; five `grep -c` runs (`Enemy`, `MobCategory`, `new FarmstormExplosion`, `damageMultiplier`, `getEntitiesOfClass(Monster`).
- **What was observed**: the key names and values of `[trap]`; the readings of the same key in the two lang files; the three readings `21.0` / `7.0` / `22.5` and the matching log verbatim; the comparison table of the 6 readings with `damageMultiplier` at 1.0 and at 10.0.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example a "pseudo-hostile" miss on a modded mob cannot be enumerated; record the phenomenon only and add no whitelist).
- **Open questions**: for example whether the reading of Zangief Cane against a non-hostile mob (a cow / a villager) is **point by point** equal to 7.5; this section records one set only and leaves it to the B-33 re-run.

---

## 5. Phase 44 · The numeric surface and the tool close-out (M40)

### 5.1 Preconditions

- **entries 3 / 4 / 5** of the previous section §4.4 are green: 4 keys inside `[trap]`, `b.push(` / `b.pop()` still 60, 1 `farmstorm.configuration.trap.damageMultiplier` each in zh and en, and `ModLangProvider.java:13-16` has not moved by one character.
- **entries 6 / 7** of §4.4 (2 constructors / only 1 of the 7 call sites passes the 9th argument, the two readings 21.0 and 22.5) are green — the parser of F-261 checks keys only, not damage; the two kinds of readings are recorded separately.
- These four at hand: F-261 ~ F-263 of document 43 and A-74 / A-75 of §5.5 / §5.6; document 42 §4.1.1 (the four parsing contracts), §4.1.2, §4.2, §4.3; row 10 of `BRIEF` §3.4; `expected_config_keys_v012` (`:137-179`) and `main()` (`:341-402`) of `tools/verify_project.py`.
- **This section is the close-out section and closes the three places once each**: the tool expectation surface = **F-261**, the config side = **F-262**, the language side = **F-263**; after the close, any task that adds or removes a key again is out of scope.
- Run `runServer` once to regenerate `run/config/farmstorm-server.toml` before starting; the repo also holds old copies under `run-verify`, `run-f*` and the like (`run-*/config/`, about **121** keys / **36** sections) — take the wrong one and the baseline is wrong.
- **A-66's wording**: the current tool reads only the fixed subsections of **6** fixed documents (`01-DESIGN` / `12` / `18` / `24` / `30` / `36` at `main():349-355`) and **does not read document 42 today** ⇒ "the document writes the key" is necessary but not sufficient, and the parser must land in the same commit (RT-60b).
- **The language keys have no automatic gate**: section 3 of `verify_project.py` only compares that the zh / en key sets are equal, it does not verify the total (document 42 §9 entry 4) ⇒ the close can only count by hand with `ConvertFrom-Json`.
- `EXPECTED_COUNTS` does not move by one line (the registry surface is still **97**, +0 in this version); the 13th category does not enter it either.

### 5.2 Goal of this phase

Turn "the document writes the key" into "the tool really expects it", and close each of the two totals into **its own close-out task** and **its own single reading**, so that a false close of "three numbers changed and not one of them has a reading" cannot happen.

At the end of this section: the two config lines of `verify_project.py` agree, the class comment at `ModLangProvider.java:13-16` matches the key set, and the close-out record states that no total was written as a conclusion inside the window.

### 5.3 Task list (F-261 ~ F-263)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-261 | The new parser `expected_config_keys_v021` of `verify_project.py` | M | 1.5 | F-258 | a new `expected_config_keys_v021` (shaped after `expected_config_keys_v012` at `:137-179`); joined into the `union` of `main()` (`:382-391`); the expected count 175 → **176** |
| F-262 | The config ledger **176** keys / 60 groups (**the close**) | S | 0.5 | F-258, F-261 | three readings recorded: the line-leading `.define*` of the source, the regenerated `run/config/farmstorm-server.toml`, the tool's union |
| F-263 | The language-key ledger **338** × 2 (**the close**) | S | 0.5 | F-258 | the key sets of the two lang files + the class comment at `ModLangProvider.java:13-16` (`337 → 338`, `178 → 179`) |

Phase subtotal: **3 tasks, 2.5 person-days** (S 2 / M 1 / L 0).

### 5.4 Exit criteria

- [ ] **The tool's two lines of readings (F-261)**: How to verify: `python tools/verify_project.py` (`-X utf8`). What passing looks like: exit code **0**; the config section prints both `文档口径键数: 176` and `生成 toml 键数: 176`; the registry-surface section still prints `已核 registry 对象: 97 / 97`.
- [ ] **The parser returns 1 key (F-261 / A-75)**: How to verify: call the function directly and take the return value + three `grep` runs. What passing looks like: the new parsing function returns **exactly 1** key `["trap.damageMultiplier"]` for §4.1.1 of `docs/zh/42-TECH-0.2.1.md`; `grep -c 'trap\.damageMultiplier' docs/zh/42-TECH-0.2.1.md` ≥ **2**; the table row of §4.1.1 is hit by the contract regex **1** row; the four contract items land one by one (`^#### 4\.1\.1\b` and `^#### 4\.1\.2\b` bound the scope, exactly **7** columns per row, column 2 fully matched, column 6 starting with 增 / 删, the row-count assertion **== 1**, the result joined into `union`).
- [ ] **The counter-example must go red (F-261 / RT-60b)**: How to verify: change §4.1.1 to **2** rows on a temporary copy under `$env:TEMP` and run it. What passing looks like: the tool **reports red with the file name + line number** (not a silent pass); document 42 itself has not moved by one character (`git diff --stat docs/zh/42-TECH-0.2.1.md` is empty).
- [ ] **The three config readings (F-262)**: How to verify: the source count + the regenerated toml + the tool's union, one reading each. What passing looks like: line-leading `.define*` in `FarmstormConfig.java` = **176** (the comment line at `:935` does not count); `b.push(` = `b.pop()` = **60**; `run/config/farmstorm-server.toml` has **60** sections, **176** keys and **4** keys in `[trap]` (including `damageMultiplier = 3.0`); the tool's union **176**.
- [ ] **The toml take-reading discipline (F-262 / A-70)**: How to verify: read the record of this task and check the take-reading path and timing. What passing looks like: the readings come from `run/config/farmstorm-server.toml` only, and `runServer` has been run once before taking them; the record names the old copies under `run-*/config/` (about **121** keys / **36** sections) as not used as a baseline; the **3** client keys (`hud.*`) are not counted in.
- [ ] **The language keys and the class comment (F-263)**: How to verify: take both key sets with `ConvertFrom-Json` + count prefix by prefix + read the comment. What passing looks like: `zh = 338`, `en = 338`, zh-only = **0**, en-only = **0**; `farmstorm.configuration.*` = **179** (= the server key count of F-262 + **3** client); `tooltip.farmstorm.*` still **20**; the number of `config(entries, …)` calls in `ModLangProvider` = **179**; in `ModLangProvider.java:13-16` `337` has **0** hits and `338` ≥ **1**; the "139 个配置项" at `:237` is unchanged character for character.
- [ ] **The close review (F-262 / A-74)**: How to verify: read the close-out record of this section through. What passing looks like: the record states that the acceptance of F-258 ~ F-261 did **not** write the totals of config and of the language keys as conclusions (the review of window discipline item 1); and that from then on any task adding or removing a key is out of scope.

### 5.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Only the expected count changed, no parser added | the tool stays green and nobody checks the key (the false green of A-66 as before, RT-60b) | the parser and the table of document 42 §4.1.1 in **the same commit**; the counter-example must report red |
| Forgetting to add document 42 to the read surface | the parser never sees its input while `grep` is all green | criterion 2 calls the function and takes the return value; it does not look at "the tool exits 0" |
| Scanning tables with a whole-file regex | it picks up rows of document 42 §3 and §7 as well (the first column of those two tables is also the ordinal 1, 2, 3 …) | bound the scope with `^#### 4\.1\.1\b` and `^#### 4\.1\.2\b` |
| Changing `EXPECTED_COUNTS` | the registry surface is still 97 and +0 in this version; changing it would get 97 wrong | this section leaves it alone; the 13th category does not enter it either |
| Counting keys on an old toml or an old copy | both the key count and the group count are off, and where the error is cannot be seen | `runServer` to regenerate first, and recognise `run/config/farmstorm-server.toml` only |
| Counting the 3 client keys into the config total | the key count and the group count each gain one and the reading of F-262 is void | the 3 `hud.*` keys are not counted; they only count into `farmstorm.configuration.*` of the language keys |
| Adding Chinese only, not English | the key set goes from "× 2" to more zh than en | same keys, same counts in both; zh-only / en-only both **0** |
| Changing the "139 个配置项" at `ModLangProvider.java:237` in passing | existing drift is smuggled in (document 42 §9 entry 11) and the diff goes out of bounds | that line is unchanged character for character; this section touches `:13-16` and the two lang files only |
| Three closes squeezed into one commit | the close and the tool change mix together and the rollback granularity is lost | one task, one commit: F-261 / F-262 / F-263 are independently verifiable |

### 5.6 Reporting format

- **Files changed**: the verbatim text of `git diff --stat`; the added function and the union block of `main()` of F-261, the three readings of F-262, the two lang files and the `:13-16` comment of F-263.
- **Commands run**: the exit code of `.tools\gw.cmd build`; one `runServer`; the **complete output** and exit code of `python -X utf8 tools/verify_project.py`; two `ConvertFrom-Json` runs; `git diff --stat docs/`.
- **What was observed**: the verbatim text of the tool's two config lines and of the registry-surface line; the verbatim 4 keys of `[trap]`; the key sets of the two lang files and zh-only / en-only; the before-and-after comparison of the two comments in `ModLangProvider.java:13-16` (the key total and the config entry count).
- **Not done and not doable**: one line each stating "not verified" and the reason (for example `runServer` needs a usable dedicated-server environment; state the substitute reading).
- **Open questions**: for example if `shouldSerialize` still writes an empty compound, the size wording has to change from "zero bytes" to "≤ 40 bytes" (document 42 §8 ⑧); this section registers it only and does not change the implementation.

---

## 6. Phase 45 · Full acceptance and signature (M41)

### 6.1 Preconditions

- **all 7 entries** of the previous section §5.4 are green: the tool's two lines of readings, the parser returning 1 key, the counter-example going red, the three config readings, the toml take-reading discipline, the language keys and the class comment, the close review.
- **The three closes are done**: the tool expectation surface (F-261), the config side (F-262), the language side (F-263); from then on any task adding or removing a key is out of scope.
- **17** tasks of F-247 ~ F-263 have landed; `.work/v021-verify/` is writable (`acceptance.md` / `boundaries.md` / `signoff.md`).
- Environment: a single-player client and a dedicated server can be started at the same time; a second client serves the "not the owner" comparison and the mount comparison; the Mixin wording counts `src/` only (`.scratch/p25a/src/neoforge.mixins.json` is experimental residue, document 42 §9 entry 10).
- These four at hand: F-264 ~ F-266 of document 43 and A-70 ~ A-81 of §5; the **31** entries of document 42 §7, the **10** to-be-verified entries of §8, the **16** deltas of §9; the **36** entries of document 41 §9 (B-01 ~ B-36); the **11** risks of `BRIEF` §4 (RT-51 ~ RT-60 + RT-60b).
- Of the three tasks of this section **F-264 / F-265 change no code at all** (they produce records only); if acceptance fails, go back to the corresponding task and redo it — never rewrite the criterion on the spot.
- The upstream is read-only: `docs/zh/40` ~ `docs/zh/43` do not move by one character; the dispositions of A-70 ~ A-81 are registered only and not fed back.
- This section **recomputes neither of the two totals**: the config key / group count cites the reading of F-262, the language key set cites the reading of F-263; this section cites them only and asserts nothing again.

### 6.2 Goal of this phase

Run the 31 entries of document 42 §7 and the 36 entries of document 41 §9 one by one, and sign the red lines, the registry surface, the numeric surface and the upstream consistency into one reviewable record.

At the end of this section: the two records miss nothing, the signature carries eight readings and the 13th-category registration, the upstream files have not moved by one character — ready for release.

### 6.3 Task list (F-264 ~ F-266)

| Task ID | Title | Complexity | Person-days | Depends on | Deliverable of this phase |
|---|---|---|---|---|---|
| F-264 | Full execution of the 31 acceptance entries of document 42 §7 | L | 3.0 | F-247 ~ F-263 | `.work/v021-verify/acceptance.md` (a reading for each of the 31 entries) |
| F-265 | Re-run of the 36 boundaries of document 41 §9 | L | 3.0 | F-264 | `.work/v021-verify/boundaries.md` (B-01 ~ B-36 in five groups) |
| F-266 | Signature of the red lines, the registry surface and the upstream consistency | M | 1.5 | F-261, F-262, F-263, F-264, F-265 | `.work/v021-verify/signoff.md` (eight readings + the 13th-category registration) |

Phase subtotal: **3 tasks, 7.5 person-days** (S 0 / M 1 / L 2).

### 6.4 Exit criteria

- [ ] **31 entries, not one missing (F-264)**: How to verify: read `.work/v021-verify/acceptance.md`. What passing looks like: `#1` ~ `#31`, **31** entries in all, each with an action, a reading, a conclusion and evidence (not the two words "已验"); `#25` (the file count of `.dat` under `world/data/` unchanged) and `#28` (restored means the attachment is deleted) pass.
- [ ] **The four mandatory pairs run as pairs (F-264)**: How to verify: check adjacent entries in the record. What passing looks like: `#6 + #7`, `#14 + #15`, `#19 + #20`, `#23 + #24` — the four pairs **appear as pairs** and all pass; `#7` (still within 100 ticks at particle interval 1200) and `#20` (the 6 non-trap explosions unchanged at multipliers 1.0 / 10.0) are the decisive readings of RT-51 and RT-57.
- [ ] **The participation surface and the file count (F-264 / RT-58 / A-78)**: How to verify: read the two rows `#21` and `#1`. What passing looks like: `#21` (the fang damage identical to 0.2.0 point by point) passes and the record states "**2** of the 4 trap crops take part"; the Java file count of `#1` is written as **149** (145 + 4 new classes).
- [ ] **The 36 entries in five groups (F-265)**: How to verify: read `.work/v021-verify/boundaries.md`. What passing looks like: `B-01` ~ `B-36`, **36** entries not one missing, split into five groups by ownership (B-01 ~ B-12 belong to R1, B-13 ~ B-17 to R2, B-18 ~ B-23 to R3, B-24 ~ B-30 to R4, B-31 ~ B-36 to R5), one summary per group; the six negative criteria (B-04 / B-06 / B-16 / B-25 / B-26 / B-33) all pass.
- [ ] **The two boundaries built separately (F-265 / A-80)**: How to verify: read B-19 / B-27 / B-30. What passing looks like: B-19 (a second overwrite within 80 ticks ⇒ refreshed, not stacked, the original value is still the first) passes; B-27 (all five cleanup paths go "restore by the original value + delete the record") passes; B-30 and `#24` are recorded **separately** (the former verifies the persisted original value, the latter the settlement on load), not the same entry re-run.
- [ ] **The signature's eight readings (F-266)**: How to verify: read `.work/v021-verify/signoff.md`. What passing looks like: eight items, each with a reading — Mixin **0** (`src/` wording), `*.mixins.json` **0** (`src/`), `.playToClient` **2**, the registry surface **97**, `data_components` **1**, attachment types **1** (outside the count), config keys / groups (citing the reading of F-262), language key sets × 2 (citing the reading of F-263).
- [ ] **The 13th category explicitly registered (F-266 / A-73)**: How to verify: read the one line in the signature + compare the 12-category assertion. What passing looks like: the signature states "**1** further attachment type `farmstorm:ai_suspend`, not counted in **97**, and `verify_project.py` checks neither its existence nor its count"; the 12-category assertion at `Farmstorm.java:169-172` is unchanged character for character.
- [ ] **The no-change list agrees with the upstream (F-266 / RT-57 / RT-58)**: How to verify: compare character for character + `git diff`. What passing looks like: the 5 `Monster` filter line numbers unchanged character for character; the vanilla fang generation at `FangTrapCactusTrap:87-109` and the 0 damage of `vibration_bean` unchanged character for character; `git diff --stat docs/zh/40-DECISIONS-0.2.1.md docs/zh/41-DESIGN-0.2.1.md docs/zh/42-TECH-0.2.1.md` is empty.

### 6.5 Pitfalls

| Pitfall | Symptom | Prevention |
|---|---|---|
| Running only `#6` and not `#7` | at the default 40-tick interval even 200 ticks looks "within 300 ticks" and being filtered by the particle gate cannot be seen | run the four mandatory pairs as pairs; one missing counts as not run |
| Using a creative / spectator account for the "the Vibration Bean does not trigger" reverse assertion | that test goes red, but what is red is the existing wording (document 42 §9 entry 13) | `#14` / `#15` always with a player in survival mode who is not the owner and not a spectator |
| Writing `#11` as "there is `renderTooltip` in the code" | the line numbers of the vanilla call sites cannot be checked and the criterion is not executable | write "visible on hover" only; on the source side count the 4 overrides = 0 only |
| Copying the Zangief Cane reading of `#19` from document 42 as 11.25 | it contradicts the arithmetic of `7.5 × 3` (A-76) | always write **22.5** |
| Treating B-30 as `#24` re-run | the former verifies the persisted original value and the latter the settlement on load; run them together and both are missing | keep the two records apart: `acceptance.md` 31 rows, `boundaries.md` 36 rows |
| Counting the attachment type into 97 | the wrong conclusion of 98 comes out (ruling #17) | write in the signature "1 attachment type, outside the count, not counted in 97, the tool does not check it" |
| Counting `*.mixins.json` repo-wide | `.scratch/p25a/src/neoforge.mixins.json` turns the reading into **1** | the Mixin wording counts `src/`: `@Mixin` 0, `*.mixins.json` 0 |
| Rewriting the criterion when acceptance fails | three rounds of acceptance become void and `src/` changes appear in `git status` | F-264 / F-265 change no code; `git status` may show `.work/v021-verify/` only |
| Recomputing the two totals in the signature | it clashes with the readings of the close-out tasks and violates "assert once, in the close-out task only" | the signature cites the readings of F-262 / F-263 only and recomputes nothing |

### 6.6 Reporting format

- **Files changed**: the verbatim text of `git status`; this section adds three records only (`acceptance.md` / `boundaries.md` / `signoff.md`), and the diff of `src/` and `docs/` must be empty.
- **Commands run**: the exit code of `.tools\gw.cmd build`; the three verbatim sections of `python -X utf8 tools/verify_project.py` (the two config lines / the registry surface / the conclusion); three `grep` runs (`@Mixin`, `playToClient`, `PROTOCOL_VERSION`); `git diff --stat docs/`.
- **What was observed**: the entry-by-entry conclusions of the 31 entries (`acceptance.md` 31 rows); the entry-by-entry conclusions of the 36 entries in five groups (`boundaries.md` 36 rows); the eight readings (`signoff.md`); the verbatim text of the 13th-category line.
- **Not done and not doable**: one line each stating "not verified" and the reason (for example a feel-based entry has no screen recording, or the second client was not available).
- **Open questions**: A-70 ~ A-81 ask for a feed-back into several places of documents 40 / 41 / 42 (the 6 version strings, 22.5, 3 calls / 90 slots, 149, the upper-bound constant, the settlement on load, the 2 `setNoAi`) — all of them are upstream feed-backs; this section registers them only and does none of them.

---

## 7. Where the cross-Phase disciplines land (ledger)

| # | Discipline | Which sections' "Preconditions / Pitfalls / criteria" it lands in | What you see on site once it is violated |
|---|---|---|---|
| 1 | The in-between window **F-258 → F-263** (config side F-262, tool expectation surface F-261, language side F-263) | preconditions: §4 (the window opens here), §5 (the three closes), §6 (already closed, cites only); pitfalls: §3 (writing the key into this section early), §4 (writing a total inside the window), §5 (recomputing a total); criteria: §4 entries 3 / 4 (this key exists + the interval reading), §5 entries 1 / 4 / 6 (the three closes once each) | a total appears inside the window ⇒ every later section has to follow and one of them will get it wrong |
| 2 | A toml reading recognises `run/config/farmstorm-server.toml` only, and `runServer` regenerates it first | preconditions: §4, §5; pitfalls: §4, §5; criteria: §4 entry 3, §5 entries 4 / 5 | taking an old copy under `run-*/config/` (about 121 keys / 36 sections) as the baseline ⇒ the conclusion "only 121 keys" comes out and the whole set of readings is void |
| 3 | The false green of A-66 + the parser of F-261 | preconditions: §5; pitfalls: §5 (only the expected count changed / the read surface forgotten / a whole-file regex); criteria: §5 entries 2 / 3 (returns 1 key, the counter-example goes red) | the document writes the key and the tool stays green ⇒ that key was never checked |
| 4 | The 13th category `farmstorm:ai_suspend` not counted in 97, not checked by the tool | preconditions: §3 (registered here), §6 (cited by the signature); pitfalls: §3, §6 (counted into 97); criteria: §3 entries 2 / 3 / 4, §6 entry 7 | counting the attachment into 97 ⇒ the wrong conclusion of 98 comes out |
| 5 | No upstream change (40 ~ 43 unchanged, A-70 ~ A-81 registered only) | preconditions: §6; pitfalls: §5 (changing `:237` in passing), §6 (rewriting a criterion); criteria: §6 entry 8 (`git diff --stat docs/` empty) | the upstream gets changed ⇒ the delta register and the feed-back chain break and the next version has no baseline |
| 6 | The corrections of six readings (A-70's 6 places / A-76's 22.5 / A-77's 3 calls 90 slots / A-78's 149 and 11 → 12 / A-79's upper-bound constant 0 / A-80's settlement on load / A-81's 2 `setNoAi`) | preconditions: §1 (A-70), §3 (A-80 / A-81), §4 (A-76); pitfalls: §3, §4, §5; criteria: §3 entries 7 / 10, §4 entry 7, §5 entry 4, §6 entry 3 | copying 11.25 / "54 + 36 times" / 148 / "the 1200-tick cap" straight from document 42 ⇒ the criterion contradicts itself or must misjudge |

## 8. Consistency self-check of this file

| # | Check | Criterion | Result |
|---|---|---|---|
| 1 | Task numbers consecutive, no gap, no duplicate | **20** rows across the six task tables; the range `F-247 ~ F-266` ⇒ `266 − 247 + 1 = 20` | pass |
| 2 | Phase and milestone correspond one to one | Phase **40 ~ 45**, **6** in all; milestones **M36 ~ M41**, **6** in all; every section heading carries its own milestone number | pass |
| 3 | Six subsections per section, fixed order | every Phase is "Preconditions / Goal of this phase / Task list / Exit criteria / Pitfalls / Reporting format"; **6** subsections × **6** Phases = **36** third-level headings inside the Phases (plus the 4 of §0), not one of them out of order | pass |
| 4 | Task number / title / complexity / person-days / dependency agree with document 43 | all 20 rows compared row by row against the §1 table of document 43 and the task title lines: numbers **20/20**, titles **20/20**, S/M/L **20/20**, person-days **20/20**, dependencies **20/20** | pass |
| 5 | Person-days self-consistent | the six subtotals `0.5 + 4.5 + 9.5 + 3.0 + 2.5 + 7.5 = 27.5`; complexity `S 7 / M 10 / L 3`; `27.5 × 1.25 = 34.375` | pass |
| 6 | Exit-criteria total and distribution | **45** entries in all: §1 **4** / §2 **8** / §3 **10** / §4 **8** / §5 **7** / §6 **8**; every entry starts with `- [ ]` and every entry carries a number, a key name or a path | pass |
| 7 | The in-between window written right | the window falls only in §4 (opens at F-258) and §5 (closes at F-261 / F-262 / F-263): §4 writes only "this key exists" + "4 keys inside `[trap]`" + the interval reading; §5 asserts each of the three totals once; §6 recomputes nothing | pass |
| 8 | The toml take-reading discipline | `run/config/farmstorm-server.toml` appears ≥ 1 time in the preconditions and the criteria of §4 / §5, and both require `runServer` to regenerate first; the old copies under `run-*/config/` appear only in the "not a baseline" sentences | pass |
| 9 | A-66 and the 13th category | A-66 in the preconditions / pitfalls / criteria 2·3 of §5; the 13th category in criteria 2 (registered) / 3 (97 unchanged) / 4 (not checked by the tool) of §3 and criterion 7 (registered in the signature) of §6, four places, each writing "not counted in 97" | pass |
| 10 | No invented numbers | 97 / 175 / 337 / 31 / 36 / 67 / 20 / 6 / 45 / 149 / 145 / 22.5 / 21.0 / 7.0 / 7.5 / 80 / 100 / 1200 / 300 / 16 / 3 / 2 / 1 / 0 — every one traces back to document 43 / 42 / 41 or to arithmetic on them (`145 + 4`, `175 + 1`, `337 + 1`, `7.5 × 3`, `31 + 36`) | pass |
| 11 | Banned words and the change surface | no undecided-item marker and no placeholder marker anywhere; the person is "the author / the implementer", never a second-person honorific; only this file is created, `docs/zh/40` ~ `43`, `docs/en`, `src`, `tools`, `.work` are unchanged by one character | pass |
