# Document 43 · v0.2.1 Task Breakdown

> Basis: document 40 (14 decisions + 17 rulings) / document 41 (5 requirements + 36 boundaries) / document 42 (97 registry objects / 31 action-surface rows / 176 config keys / 31 acceptance entries), tasks from F-247, Phases from 40, milestones from M36, adjudications from A-70
>
> This document breaks the three specs 40 / 41 / 42 into **20 tasks that can each be "committed and verified independently"**, numbered **F-247 ~ F-266**, with Phase **40 ~ 45** mapping one-to-one onto milestones **M36 ~ M41**, and adjudications **A-70 ~ A-81** (12 entries, all newly established while this document was written).
> Coverage: the **31 action-surface rows** of document 42 §3 (table A, 31/31), the registry surface **97 (+0) + 1 row of the 13th category outside the count** (table B), config **176 keys / 60 groups** (table C, +1), language keys **338 × 2** (table D, +1), risks **11 entries** (table E: RT-51 ~ RT-60 + RT-60b), APIs to verify **10 entries** (table F); 0 uncovered.
> This document only reads upstream: it does **not** modify `docs/zh/40` ~ `docs/zh/42`, does not modify `docs/en`, does not modify `src/`, does not modify `tools/`, does not modify `.work/`; it only creates this file.

---

## 0. Reading Conventions

### 0.1 Task Granularity (five items) + Three Reading Disciplines

One task = one change that can be committed and verified independently. The five criteria follow document 37 §0.1 (whose source is document 31 / document 25); this version does not rewrite them, only restates them:

1. **One task = one change that can be committed and verified independently.**
2. **After the commit, `.tools\gw.cmd build` exits with code 0**, and the task does not depend on a task of the same batch that is not yet committed.
3. **Each task carries at least one observable assertion**, and the assertion must contain a concrete value, key name, path or log fragment.
4. **Reverting that commit does not invalidate other completed tasks** (it affects only later tasks that depend on it; the dependency is written in the "Depends on" column of §1 and on the task's title line).
5. **No Phase boundary is crossed**: all deliverables of a task fall inside the name scope of the Phase it belongs to.

Three reading disciplines **specific to this version** (written here, not repeated in the tasks):

- **All line numbers use the UTF-8 basis**: the local default `Get-Content` reads BOM-less UTF-8 Chinese as GBK and **swallows the line breaks** (the same `OutpostCoreBlockEntity.java` reads as 668 lines instead of 782, `FarmstormConfig.java` as 1095 instead of 1131), so every line number goes wrong with it (document 42 §0.2 item 4 / §9 item 12). Every line number in this document has been re-checked with `[IO.File]::ReadAllLines($p, UTF8)`.
- **Counting the toml always points at `run/config/farmstorm-server.toml`**: the repository still holds dozens of old `run-*/config/` copies (`run-verify` and the like are 12,238-byte old files with 121 keys); take the wrong one and the conclusion becomes "only 121 keys" (a pitfall outside A-70, already stepped into by document 42 §9 item 2).
- **Do not write "feel" or "normal"**: a criterion may only be a count (file count / key count / hit count), a log line, a `/data get` reading, or a key-set comparison — those four kinds.

### 0.2 Complexity and Workload Conversion (S/M/L → 0.5/1.5/3.0)

The conversion basis is exactly that of document 37 §0.2 and is not adjusted in this version:

| Complexity | Meaning | Converted person-days |
|---|---|---|
| S | Single file or pure data; ≤ 1 class, or literal replacement in ≤ 3 files | **0.5** |
| M | 2 ~ 4 files, across 1 ~ 2 packages, or 1 new complete behaviour implementation | **1.5** |
| L | ≥ 5 files or across 3 packages, including a runtime state machine / serialization / container contract / render pipeline / cross-class interface rework | **3.0** |

Distributed under this rule, this version's 20 tasks are **S 7 / M 10 / L 3**, converting to `7 × 0.5 + 10 × 1.5 + 3 × 3.0 = 3.5 + 15.0 + 9.0 = **27.5 person-days**` (buffer excluded).

**Three estimation rules**: anything with "0 precedents across the repository" (F-248 / F-257) counts one tier above "add one class"; any "one-off contract change" (6 of F-259's 7 call sites must stay untouched) counts as M; any acceptance task (F-264 / F-265) is not estimated as "running it once" but counts as L.

### 0.3 The **in-between window** (this version's 6th granularity rule)

**Fact**: this version's two numeric faces each grow by only **+1**, and both increments land in **one and the same commit** (F-258: the config key `trap.damageMultiplier` and its Chinese / English translations written in one go). But "the key is in" does not mean "the total can be asserted" — the four total texts are synced only in their own tasks:

| Total text | Current value | Synced by | Evidence |
|---|---|---|---|
| The **count** of line-leading `.define*` in `FarmstormConfig.java` | 175 | F-262 | the three independent checks of document 42 §4.2 |
| The keys / sections of `run/config/farmstorm-server.toml` | 175 / 60 | F-262 (run `runServer` once first to regenerate) | measured 801 lines / 60 sections / 175 keys |
| The class comment at `ModLangProvider.java:13-16` (`**337**` / `**178** config`) | 337 / 178 | F-263 | the two disciplines of document 42 §4.3 |
| The expected number (175) and the union in `verify_project.py` | 175 | F-261 | the four contracts of document 42 §4.1.1 |

**Window = F-258 (opens) → F-263 (closes)**, of which the config side closes at **F-262** and the tool's expectation face at **F-261**. Four segments of state:

| Segment | Task boundary | config keys | language keys | What can be asserted inside the segment |
|---|---|---|---|---|
| Before the window | F-247 ~ F-257 | **175** | **337** | the totals (175 / 337) may be asserted, because no key is added or removed in between |
| Inside the window A | F-258 ~ F-260 | actually **176** | actually **338** | **only** "this key exists" may be written: `damageMultiplier` appears once in the `[trap]` section, `farmstorm.configuration.trap.damageMultiplier` once in each language; plus **4 keys inside** `[trap]`, and **175 or 176 across the whole file (an interval reading; 175 while the old toml has not been regenerated)** |
| Inside the window B | F-261 ~ F-262 | actually **176** | actually **338** | the tool segment may write 176 (F-261 closes the tool's expectation face); the language-key total **is still not allowed** |
| Window closed | after F-263 | **176** | **338** | F-262 writes 176 / 60, F-263 writes 338 × 2; from then on any task that adds or removes a key is out of scope |

**Three window disciplines** (they apply to every task from F-258 to F-262; F-263 is the closing task, so item 1 does not apply to it):

1. **Do not** write "176 / 60" or "338 × 2" as an acceptance assertion; inside the window write only **this key's existence** and the **in-segment / interval readings** (see the third row of the table above). There are only two exceptions: F-261 may write the **176** printed by the tool (what it closes is the tool's expectation face), F-262 may write **176 / 60** (what it closes is the source and the toml); **338** appears exactly once, in F-263.
2. **The class comment at `ModLangProvider.java:13-16` is not touched at all** until F-263 changes it in one go (`337 → 338`, `178 → 179`). Changing that comment midway would mean every task has to recompute the total, and one of them would necessarily get it wrong.
3. **The reading of `run/config/farmstorm-server.toml` must be taken after `runServer` regenerates it** (check ② of document 42 §4.2); inside the window the interval wording "175 or 176" is allowed, but a file that has not been regenerated may not be taken as the true value; **the language keys have no automatic gate** (`verify_project.py` §3 only compares the zh / en key sets for equality and does not check the number 338, document 42 §9 item 4) ⇒ closing the window can only rely on F-263's manual `ConvertFrom-Json` count.

**Why the window opens at F-258 but closes in three places**: one config key and its translation are **one atomic change** (add the config without the translation and the player sees a bare key string), so they open together; whereas the three total texts "source count / tool expectation / document comment" have **mutually different observation faces** (`.define*` lines, the python union, the java comment), so each sync action must land on the task that can verify it — otherwise the closing task degenerates into a false closure that "changes three numbers and has a reading for none".

### 0.4 One Task = One Commit

1. **One task, one commit**; the commit message starts with the task number (`F-248: …`); the commit content lines up with the task's "Files touched" item by item — one extra file means out of scope.
2. **No drive-by fixes of existing blemishes**: the 16 differences registered in document 42 §9 (the "139 个配置项" comment at `ModLangProvider.java:237`, the "3 个方块" comment at `FarmstormBlocks.java:43`, the `neoforge.mixins.json` in `.scratch/`, `removeCultivator`'s 0 call sites, etc.) — this version **changes none of them**; they appear in "Easy-to-miss points" to mean "do not fix them in passing", not as to-dos.
3. **Rollback granularity = commit granularity**: every task's acceptance assertions must still describe the system after **reverting only this one commit** (without relying on another commit of the same batch).
4. **Upstream is not changed by a single character**: every difference found in 40 / 41 / 42 while writing is registered in A-70 ~ A-81 of §5, and the upstream files are **registered only, never modified**; when a task cites upstream it writes the full section number (`document 42 §2.3.3`), not a paraphrase of the details.

---

## 1. Task Overview

Columns: task number / title / Phase / complexity / person-days / depends on / deliverables / decisions / covered action-surface row numbers of document 42 §3 (`表外` = not among the 31 rows of document 42 §3, the reason being in §5).

| Task | Title | Phase | Complexity | Person-days | Depends on | Deliverables | Decisions | Action-surface rows |
|---|---|---|---|---|---|---|---|---|
| F-247 | Version 0.2.1 and the 6 version strings in the two `.cmd` scripts | 40 | S | 0.5 | — | `gradle.properties`, the two `.cmd` scripts | D-173 | #1, #2, #3, #4, #5 |
| F-248 | `util/AreaBlockEntities#collect` (region enumeration of block entities, 0 precedents) | 41 | M | 1.5 | — | new `util/AreaBlockEntities.java` | D-177, RT-52 | #10 |
| F-249 | The rebind phase gate and write boundary: `rebindDue` + `rebindNearby` | 41 | M | 1.5 | F-248 | the constants and the method area of `OutpostCoreBlockEntity` | D-176, D-177, author's ruling #13, #14 | #7, #8, #9, #30 |
| F-250 | Wiring `serverTick` + the comment at `CultivatorBlock:73-84` changed in the same commit | 41 | M | 1.5 | F-249 | `OutpostCoreBlockEntity:507-525`, `CultivatorBlock:73-84` | D-176, D-177 | #6, #11 |
| F-251 | `isMountMode` visibility (`private` → `public`) | 42 | S | 0.5 | — | `QbounceMountController:199` | D-181 | #17 |
| F-252 | The original-AI-flag attachment carrier: `Suspend` + `farmstorm:ai_suspend` (the 13th category) | 42 | M | 1.5 | — | new `registry/FarmstormAttachments.java`, `Farmstorm.java` +1 line | D-179, author's ruling #15, #17 | #19, #29 |
| F-253 | `MobAiSuspension`: record the original on entry / restore it on exit / five cleanup paths | 42 | L | 3.0 | F-251, F-252 | `combat/MobAiSuspension.java` | D-179, author's ruling #15, #16 | #18, #31 |
| F-254 | Vibration Bean: suspend the AI of every non-player mob in range for 80 ticks | 42 | M | 1.5 | F-253 | `VibrationBeanTrap:74-105` | D-180, author's ruling #4, #5 | #14, #15 |
| F-255 | Q-Bounce: suspend the AI while the effect lasts, skip in mount mode | 42 | M | 1.5 | F-251, F-253 | `effect/QbounceEffect.java` | D-181, author's ruling #6, #7 | #16 |
| F-256 | The core storage tooltip: guarantee the default chain + 3 acceptance entries | 42 | M | 1.5 | — | `OutpostCoreScreen` (expected zero change) + the acceptance record | D-178, RT-59 | #12, #13 |
| F-257 | The hostility predicate `util/Hostility#isHostile` (0 precedents) | 43 | S | 0.5 | — | new `util/Hostility.java` | D-182, author's ruling #9, RT-57 | #20 |
| F-258 | The `trap.damageMultiplier` key and its translation (**opens the window**) | 43 | S | 0.5 | — | `FarmstormConfig`, `ModLangProvider` | D-183, D-185 | #24, #25 |
| F-259 | `FarmstormExplosion`'s 9th parameter `trapSourced` + multiplication point ① | 43 | M | 1.5 | F-257, F-258 | `FarmstormExplosion:69-78,178-179`, `BlastCarrotTrap:84-86` | D-184, RT-58 | #21, #22 |
| F-260 | `ZangiefCaneTrap#monsterBranch` multiplication point ② + the log printing the multiplied value | 43 | S | 0.5 | F-257, F-258 | `ZangiefCaneTrap:111-118`, `:126-128` | D-184 | #23 |
| F-261 | `verify_project.py`'s new parser `expected_config_keys_v021` | 44 | M | 1.5 | F-258 | `tools/verify_project.py` | D-185, RT-60b | #26 |
| F-262 | The config ledger at 176 keys / 60 groups (**closes the window**) | 44 | S | 0.5 | F-258, F-261 | the three reading records | D-185, RT-60 | 表外 (document 42 §4.2) |
| F-263 | The language-key ledger at 338 × 2 (**closes the window**) | 44 | S | 0.5 | F-258 | the two lang files + the `ModLangProvider` comment | D-185 | 表外 (document 42 §4.3) |
| F-264 | Full execution of document 42 §7's 31 acceptance entries | 45 | L | 3.0 | F-247 ~ F-263 | `.work/v021-verify/acceptance.md` | document 42 §7, all 31 entries | — (the acceptance face) |
| F-265 | Re-run of document 41 §9's 36 boundary entries | 45 | L | 3.0 | F-264 | `.work/v021-verify/boundaries.md` | document 41 §9, all 36 entries | — (the boundary face) |
| F-266 | Sign-off on the red line, the registry surface and upstream consistency | 45 | M | 1.5 | F-261, F-262, F-263, F-264, F-265 | `.work/v021-verify/signoff.md` | D-175, D-186, A-73 | #27, #28 (negative check) |

Task count: `266 − 247 + 1 = 20`, contiguous with no gap; **6** Phases; **6** milestones.

---

## 2. Task Details, Phase 40 ~ 45

Grouped by Phase. Each task has exactly six items: **Goal / Files touched (with line numbers) / Concrete changes / Implementation notes / Easy-to-miss points / Acceptance assertions**; the dependency is on the task's title line (`Depends on: —` means no predecessor).

### Phase 40 · Version and Baseline (F-247)

Phase goal: nail down the version number and the artifact face of the two build scripts — it is the only source of every jar name and of the "检查环境" criterion. Exit criterion: `farmstorm-0.2.1+mc1.21.1.jar` is produced, and `0.2.0` falls to 0 across the 6 literals.

#### F-247 · Version 0.2.1 and the 6 version strings in the two `.cmd` scripts (S · 0.5 person-days | Depends on: —)

- **Goal**: change `mod_version` to **0.2.1** (D-173) and let the jar names and banner lines hard-coded in the two `.cmd` scripts follow, so that the copy step of "一键构建" and the existence check of "检查环境" still point at real artifacts (document 42 §3 #1 ~ #5; A-70).
- **Files touched**: `gradle.properties:11`; `一键构建.cmd:5` (the banner), `:11`, `:12` (jar / sources-jar); `检查环境.cmd:5` (the banner), `:14` (jar).
- **Concrete changes**: the 6 literal `0.2.0` → `0.2.1` (`gradle.properties` 1 place + the two `.cmd` scripts 5 places). The three lines `minecraft_version` / `neo_version` / toolchain are not changed by a single character (document 42 §1.1).
- **Implementation notes**: the version number has only one source, `gradle.properties:11`, and the other documents only cite it without declaring it themselves; the jar name is assembled from `mod_version`, so after the change one **real build** must confirm the new name (document 42 §3 #1~#5; the same discipline as F-213 of document 37).
- **Easy-to-miss points**: ① the `-sources.jar` at `一键构建.cmd:12` is the easiest place to leave only the main jar changed; ② the **banner lines** of the two scripts (each at `:5`) hard-code `Farmstorm v0.2.0`, and missing them means "the artifact is right while the banner still shows the old version"; ③ the existence check at `检查环境.cmd:14` is bound to its prompt text, and missing it makes the environment check report "jar: 缺失" (the file really is not found after the rename).
- **Acceptance assertions**:
  1. `.tools\gw.cmd build` exits with code 0; `build/libs/` holds `farmstorm-0.2.1+mc1.21.1.jar` and `farmstorm-0.2.1+mc1.21.1-sources.jar`, with **0** `farmstorm-0.2.0*` leftovers.
  2. The hit count of `0.2.0` in `gradle.properties` + the two `.cmd` scripts = **0**; the hit count of `0.2.1` = **1 (gradle) + 3 (一键构建) + 2 (检查环境) = 6**.
  3. `jar tf build/libs/farmstorm-0.2.1+mc1.21.1.jar` contains `META-INF/neoforge.mods.toml`, whose `version` parses as `0.2.1`.

### Phase 41 · R1 Periodic Rebind (F-248 ~ F-250)

Phase goal: make the core, at runtime, rebind every 100 ticks the "unwanted" cultivators inside the 3×3 loaded chunks into its own 16 slots, without being filtered out by the existing particle phase gate. Exit criterion: with `boundaryParticleIntervalTicks` at 40 and at 1200, the rebind happens within **100 ticks** in both; it stops at 16; a core with no owner does not rebind.

#### F-248 · `util/AreaBlockEntities#collect` (region enumeration of block entities, 0 precedents) (M · 1.5 person-days | Depends on: —)

- **Goal**: add a utility that enumerates block entities by region, only inside **loaded** chunks, as the rebind's only scan face (document 42 §2.1.2's "[this document's decision] (3)"; document 42 §3 #10). 0 precedents across the repository: 0 hits for `ChunkMap`, `getBlockEntities()` only at `DeployedCropBlockEntity:230` (a single chunk, counting only itself).
- **Files touched**: new `util/AreaBlockEntities.java`; read-only references `blockentity/OutpostCoreBlockEntity.java:75` (`RANGE_CHUNKS = 1`), `:296-300` (the existing shape of `getChunkNow`), `:384-387`, `blockentity/DeployedCropBlockEntity.java:227-236` (the only single-chunk precedent; `:228` uses `getChunk`, which **does load**, so only the loop at `:230-234` may be copied).
- **Concrete changes**: one method only, `collect(ServerLevel level, BlockPos center, int chunkRadius, Class<T> type) → List<T>`, with the four implementation disciplines applied item by item (go only through `ServerChunkCache#getChunkNow`; reuse `RANGE_CHUNKS` for the radius rather than introducing a second constant; return a **new `ArrayList`**; filter by `Class<T>`) — details in document 42 §2.1.2.
- **Implementation notes**: **two prohibitions** — no `Level#getChunk` / `getChunkAt` / `Level#getBlockEntity` (they trigger chunk loading; the comment at `OutpostCoreBlockEntity:264-267` already says so); and the **live map** of `LevelChunk#getBlockEntities()` (`LevelChunk.java:528-530`) must not be handed out for iteration directly (an insertion or removal during iteration throws `ConcurrentModificationException`).
- **Easy-to-miss points**: ① treating "not loaded" as "this chunk has no cultivator" in a log line or a state (document 42 §2.1.2's 📌: the core's own tickets make the 3×3 normally "all 9 chunks in memory", so the skip is a **defensive branch**, not the norm); ② swapping `Class<T>` for `BlockEntityType` to "save one filter"; ③ adding sorting or caching to the utility in passing (sorting is F-249's job; a cache would invalidate "writing is what claims the slot").
- **Acceptance assertions**:
  1. `gw.cmd build` exits with code 0; the Java file count under `src/main/java` goes 145 → **146** (UTF-8 reading).
  2. In that file, `getChunkNow` has **1** call site; `getChunk(` / `getChunkAt(` / `getBlockEntity(` have **0** hits.
  3. In that file, `new ArrayList` has ≥ 1 hit, and the result of `getBlockEntities()` is copied before iteration (two phases, document 42 §2.1.2 discipline 3).
  4. Filtering by `Class<T>` (`type.isInstance`) has ≥ 1 hit, `BlockEntityType` has **0** hits.
  5. Runtime readings are **not in this task**: `collect`'s first caller is F-249, and the runtime readings of document 42 §7 #6 ~ #10 belong to F-250 (document 42 §8 ① already hands the verification of "live-map semantics" to the rebind tasks). This task's independently executable criterion is "it compiles + the four structural readings above". The old toml files under `run-*/config/` have nothing to do with this task.

#### F-249 · The rebind phase gate and write boundary: `rebindDue` + `rebindNearby` (M · 1.5 person-days | Depends on: F-248)

- **Goal**: turn the phase gate "every 100 ticks, staggered by a coordinate hash into ticks 0~99" and the write boundary "only fill unbound ones, never steal a bound one, only while < 16, stop when full" into two methods (document 42 §2.1.1, §2.1.3; document 42 §3 #7 ~ #9, #30). This task **does not wire them in**; the two methods have no caller for now.
- **Files touched**: `blockentity/OutpostCoreBlockEntity.java` (in the constants, next to `TICK_LOG_ROUNDS` at `:91`, add `REBIND_INTERVAL_TICKS = 100`; in the methods, add `rebindDue(BlockPos, long)` and `rebindNearby(ServerLevel)`).
- **Concrete changes**: the phase formula's modulus is **fixed at 100** (separate from the particle gate's `interval` modulus, document 42 §2.1.1's "[this document's decision] (1), continued"); the constant is **not** a config key; the write order is fixed as "`addCultivator` succeeds first, then `setCore`"; the first line is the ownerless-core gate (author's ruling #13); the candidate ordering is `distSqr` ascending → `x` → `y` → `z` (author's ruling #14, isomorphic to `selectCore:310-315`); capacity is read through `cultivatorCount()` / `isCultivatorListFull()`.
- **Implementation notes**: inside `rebindNearby`'s body, `pruneCultivators` / `removeCultivator` / `cultivators()` **must not** appear (document 42 §2.1.3 items 1 and 3: the first calls `setChanged` and interleaves with the rebind, the second is "bind first, then strip" and creates a second owner); do not call the broadcast additionally (`addCultivator` already carries `setChanged` + `syncBindings` + `requestAll`, document 42 §2.1.3 item 4).
- **Easy-to-miss points**: ① making `REBIND_INTERVAL_TICKS` a config key (175 would become **177**, directly conflicting with D-185's 176); ② dropping the `y` level from the ordering (two cultivators with the same x/z and different y would order unstably, so the same save gives different results on two runs); ③ forgetting the `ownerUuid == null` gate on the first line (an ownerless core would take in every surrounding cultivator, author's ruling #13); ④ calling `removeCultivator` in passing as a fallback for "the two sides disagree" (that method's call sites must still be 0).
- **Acceptance assertions**:
  1. `REBIND_INTERVAL_TICKS` is declared as **100**; `FarmstormConfig`'s `b.push(` is still **60**, `b.pop()` still **60**, and the line-leading `.define*` still **175** (this task adds no config key at all).
  2. Inside `rebindNearby`'s body: `pruneCultivators(` has **0** hits, `removeCultivator(` **0**, `cultivators()` **0**; `addCultivator(` and `setCore(` have ≥ 1 hit each, and the former appears **first** in the text.
  3. The sort key has exactly four levels: `comparingDouble` **1** time (`distSqr`) + `thenComparingInt` **3** times (`x` → `y` → `z`).
  4. Across the repository, `pruneCultivators(` still has **1** call site (`OutpostCoreBlockEntity:329`, inside `cultivators()`); `removeCultivator(` still has **0** call sites (only the declaration at `:366` and the javadoc mention at `CultivatorLinkBroadcaster:35`).
  5. `gw.cmd build` exits with code 0.

#### F-250 · Wiring `serverTick` + the comment at `CultivatorBlock:73-84` changed in the same commit (M · 1.5 person-days | Depends on: F-249)

- **Goal**: wire the rebind gate into `serverTick` (its own `if`, **before** the particle gate, **no `return`**), and in **the same commit** change the class comment at `CultivatorBlock:73-84`, which says exactly the opposite of this version, plus add one rebind INFO line as the observation face (document 42 §2.1.1, §3 #6, #11; BRIEF §3.4 items 3 and 11; A-71).
- **Files touched**: `blockentity/OutpostCoreBlockEntity.java:507-525` (`:515`'s `now` moved up one line + one independent gate + one call line + one INFO); `block/CultivatorBlock.java:73-84` (the class comment block).
- **Concrete changes**: `serverTick` goes from three things to four (document 42 §2.1.1's post-change shape); the existing `:508-510`, `:511-513`, `:516-518`, `:519` and `:521-524` are not changed by a single character; the two sentences at `CultivatorBlock:81-83` — "**不自动认领**" / "事后在旁边补一个核也**不会绑上**" — become wording consistent with the periodic rebind (at runtime, unbound ones are rebound every 100 ticks, D-176), and the three-criteria explanation at `:74-79` is kept.
- **Implementation notes**: **the only structural change is moving `:515`'s `now` up one line** (a precondition of the rebind gate, not a drive-by optimization; `emitBoundaryRing` and the log segment still use the same `now`); the two gates do not block each other and no `return` overlaps them; `CultivatorBlock` changes comments only, and `setPlacedBy` (`:85-102`, including `addCultivator` at `:99` and `setCore` at `:100`) is not changed by a line.
- **Easy-to-miss points**: ① hooking the rebind gate after `:519` (the effective period gets filtered to `LCM(40, 100) = 200` ticks, and drifts with `boundaryParticleIntervalTicks` over 20 ~ 1200, document 42 §2.1.1); ② borrowing the particle gate's `interval <= 0` guard (`floorMod(x, 100)` never throws, so the two guard sets cannot be borrowed from each other); ③ committing the comment and the code separately (a window in which "the comment says it cannot, the code does it" should not exist even for a day, A-71); ④ changing only `:81-83` without aligning the `*/` boundary with `:84`, leaving half a sentence of the old comment.
- **Acceptance assertions**:
  1. `boundaryParticleIntervalTicks = 40` (the default) + a core bound to 0 cultivators + 1 unbound cultivator inside the 3×3 ⇒ within **100 ticks** that coordinate appears in the core's `Cultivators` (`/data get block <core> Cultivators`); at the same time one rebind INFO appears in the log (core coordinate + this round's new entries + cumulative / cap) (document 42 §7 #6).
  2. **In the same scenario, change `boundaryParticleIntervalTicks` to 1200** ⇒ the rebind still lands within **100 ticks** (document 42 §7 #7). This is the decisive reading for "the rebind was not filtered out by the particle gate": an implementation that reuses the particle gate waits 1200 ticks here.
  3. Stop when full: 20 unbound cultivators inside the 3×3 and `cultivatorCapacity = 16` ⇒ `Cultivators` has exactly **16** entries, still 16 after another 300 ticks, and the coordinates and order of the existing 16 are unchanged character for character (document 42 §7 #10).
  4. Ownerless core: `/setblock` a core (`ownerUuid` empty) + 1 unbound cultivator inside the 3×3 ⇒ after **300 ticks** that core's `Cultivators` is still **empty** (document 42 §7 #30).
  5. Phase stagger: **2** cores in the same 3×3, each with 1 unbound cultivator ⇒ the two rebind INFO lines' `gameTime` are not equal (document 42 §7 #8).
  6. In `CultivatorBlock.java`, `不自动认领` and `不会绑上` have **0** hits; the two lines `:99` / `:100` of `setPlacedBy` are unchanged character for character; the class's total line count is still **162**.

### Phase 42 · R2 Tooltip and R3 / R4's AI Intervention (F-251 ~ F-256)

Phase goal: build a mechanism for "suspend AI" that is persistable, restorable and covers the five cleanup paths, and hook it into the Vibration Bean and Q-Bounce; at the same time turn the core storage tooltip from "it was already there" into "it has an acceptance". Exit criterion: after 80 ticks the Vibration Bean's `NoAI` returns to the original value, Q-Bounce's three removal paths restore within 1 tick, mount mode never suspends, and hovering the 54 slots shows a tooltip.

#### F-251 · `isMountMode` visibility (`private` → `public`) (S · 0.5 person-days | Depends on: —)

- **Goal**: give the AI intervention **the only read-only entry** to the mount state (document 42 §2.3.5's "[this document's decision] (7)"; document 42 §3 #17; BRIEF §3.4 item 12).
- **Files touched**: `behavior/QbounceMountController.java:199` (one modifier only; the body at `:200` is not changed by a line).
- **Concrete changes**: `private static boolean isMountMode()` → `public static boolean isMountMode()`; `POSE_MODE_MOUNT` is still the only literal (`:50`) and `:200` is still the only read point.
- **Implementation notes**: **add no second method** (one thing may not have two implementations); both `MobAiSuspension` and `QbounceEffect` call this only and **may not** parse `qbounce.poseMode` themselves (document 40's glossary: "the truth of mount exists in one place"); do not touch `ACTIVE`, do not discard the mount, and do not change the mounting logic (document 42 §2.3.5).
- **Easy-to-miss points**: ① adding a wrapper such as `isMountModeForEntity(...)` in passing (that equals a second truth); ② moving the `POSE_MODE_MOUNT` comparison into the caller; ③ changing the tick order at `:173-196` (this task changes visibility only).
- **Acceptance assertions**:
  1. `QbounceMountController.java`'s total line count is still **376** (the diff holds only 1 modifier).
  2. Across the repository, `isMountMode` has exactly **1** **declaration**, with the modifier `public static`; the body is still the single line `POSE_MODE_MOUNT.equalsIgnoreCase(...)`.
  3. Across the repository, `qbouncePoseMode` still has only **1** read point, at `:200` (a new caller may only call the method).
  4. `gw.cmd build` exits with code 0.

#### F-252 · The original-AI-flag attachment carrier: `Suspend` + `farmstorm:ai_suspend` (the 13th category) (M · 1.5 person-days | Depends on: —)

- **Goal**: build the persistable carrier for "recording the original flag", and register **the 13th category outside the count** explicitly (document 42 §2.3.1's "[this document's decision] (4) (5)" and §1.2.1; document 42 §3 #19, #29; author's ruling #15 / #17; A-73).
- **Files touched**: new `combat/MobAiSuspension.java` (this task puts in it only the `Suspend` record + the `NONE` sentinel + the two source bits + the Codec) and `registry/FarmstormAttachments.java`; `Farmstorm.java:45-58` (the region of the constructor holding the **11** `.register(modBus)` calls, plus 1 line).
- **Concrete changes**: `Suspend(boolean originalNoAi, int sources)` with its two fields, `NONE` when `sources == 0` is not persisted, and **no timestamp is stored at all** (author's ruling #16); the `AttachmentType`'s registration name `ai_suspend` (full name `farmstorm:ai_suspend`), `.serialize(Codec, s -> s.sources() != 0)`; no `.sync(...)` and no `copyOnDeath()` (document 42 §1.2.1's two "will not"s); `Farmstorm.java` gains one line, `FarmstormAttachments.REGISTER.register(modBus)`.
- **Implementation notes**: the registry surface **stays 97** — `ATTACHMENT_TYPES` is NeoForge's own registry and is outside this project's 12-class count (author's ruling #17); the 12-class assertion at `Farmstorm.java:167-172` is **not changed by a character** (document 42 §3 #29); an attachment is **not** a data component (what D-175 rejected is `data_components`; one more would take 97 → 98).
- **Easy-to-miss points**: ① carrying it in a `DataComponentType` (walks into D-175); ② adding `deadline` / `expireAt` fields to the record (the "record a time point" shape that author's ruling #16 rejected); ③ forgetting to hang the register call in `Farmstorm.java` (an unregistered attachment type ⇒ the loading side cannot read it, document 42 §8 ⑧); ④ adding `.sync(...)` in passing (it would put the attachment on the client, which this version does not need).
- **Acceptance assertions**:
  1. `registry/FarmstormAttachments.java` holds exactly **1** `AttachmentType` registration, with the id literal `ai_suspend` in the namespace `farmstorm`.
  2. `Farmstorm.java`'s constructor has **1** more `.register(modBus)` line (**11 → 12** register calls; `damage_type` is data-driven and never had a register call, see A-78); the 12-class assertion at `:169-172` is unchanged character for character.
  3. The server-start log is still `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK` (document 42 §7 #1, #29).
  4. `Suspend` has exactly **2** fields; in that file `deadline` / `expire` / `timestamp` have **0** hits.
  5. **Acknowledge the coverage gap**: none of `verify_project.py`'s 12 `EXPECTED` id groups contains `ai_suspend`, and the `EXPECTED_COUNTS` sum is still 97 ⇒ the tool **checks neither its existence nor its count** (document 42 §1.2.1 line 5). This gap is held up by three manual registrations — document 42 §1.2.1 + this task + F-266's sign-off (A-73).

#### F-253 · `MobAiSuspension`: record the original on entry / restore it on exit / five cleanup paths (L · 3.0 person-days | Depends on: F-251, F-252)

- **Goal**: give "suspend AI" a mechanism: on entry **read the original first, then** `setNoAi(true)`; on exit restore by the original value and **remove the attachment immediately**; the restore criterion is "is the effect still on the entity" + the Vibration Bean's in-memory deadline tick (document 42 §2.3.2 ~ §2.3.4; document 42 §3 #18, #31).
- **Files touched**: `combat/MobAiSuspension.java` (`suspend` / `release` / `adopt` / `sweep` + the two in-memory containers); `Farmstorm.java:74-77` (the game-bus region, hanging `ServerTickEvent.Post` and `EntityJoinLevelEvent`).
- **Concrete changes**: the three steps are fixed in order (read the original → write the attachment → `setNoAi(true)`); `release` is the **only** write exit and must end with `removeData`; `sources` is a bit mask (entering twice from the same source does not double it, and `left != 0` does not restore); `VB_DEADLINE` lives in memory only (author's ruling #16: the window does not cross a save); `sweep` runs every tick over a copy; the reconciliation hangs on `ServerTickEvent.Post` + `EventPriority.HIGHEST` (document 42 §2.3.5's "[this document's decision] (11)", which must precede the mount's NORMAL); the load settlement hangs on `EntityJoinLevelEvent` (document 42 §8 ⑥ unverified → fallback in table F of §6).
- **Implementation notes**: **`Mob` only** (a player is not a `Mob`; the predicate shape follows `WildHunterAura:88`); "cannot observe (chunk unloaded)" drops only the two in-memory items and **keeps the attachment** (document 42 §2.3.4: deleting it loses the original); the death path does **not** hang `LivingDeathEvent`, and the three effect-removal paths hang **no event at all** (reading `hasEffect` in the reconciliation is the authority, which removes one "the event never arrived" failure surface).
- **Easy-to-miss points**: ① calling `setNoAi` before writing the attachment (a crash in between leaves "no AI, no original", RT-54's first entry); ② treating "cannot observe" as the entity being dead and deleting the attachment (loses the original, RT-54's second entry); ③ recording a deadline tick for Q-Bounce too (the "record a time point" shape author's ruling #16 opposes); ④ judging "is it suspended" with `hasData` (after the default value has been read, `hasData` may also be true; the criterion recognises only `sources != 0`); ⑤ an upper-bound constant for "give up after how many ticks" when unobservable (document 42 §2.3.4 says explicitly that it is **not needed**, A-79).
- **Acceptance assertions**:
  1. After `/data merge entity <mob> {NoAI:0b}`, trigger one suspension ⇒ `NoAI:1b`; after the restore `NoAI` is back to **0b**, and `/data get entity` **does not show** `neoforge:attachments.farmstorm:ai_suspend` (document 42 §7 #28).
  2. A mob that was already `noAi` (`/data merge … {NoAI:1b}`) goes through the same flow ⇒ after the restore it is **still** `NoAI:1b` (document 42 §7 #15, author's ruling #8).
  3. `/stop` before the 80 ticks are up ⇒ after the restart that mob's `NoAI` is **0b** (restored early), and the attachment holds no `farmstorm:ai_suspend` (document 42 §7 #23, author's ruling #16).
  4. Hard-kill the process (no `/stop`) and restart ⇒ the load settles by `sources`: the Vibration Bean path restores as already expired (≤ **1** tick), and the attachment is deleted (document 42 §7 #24).
  5. Source face: `removeData(` has exactly **1** occurrence (the only exit); `LivingDeathEvent` / `MobEffectEvent` have **0** hits; upper-bound constants such as "give up after how many ticks when unobservable" have **0** hits.
  6. This version still has **2** network packets (`.playToClient` = 2, `PROTOCOL_VERSION = "2"`); the attachment carries no `.sync(...)` and does not enter `getUpdateTag` (document 42 §1.2.1, §5.1).

#### F-254 · Vibration Bean: suspend the AI of every non-player mob in range for 80 ticks (M · 1.5 person-days | Depends on: F-253)

- **Goal**: **the instant the Vibration Bean triggers**, suspend the AI of **every non-player mob** inside the trigger radius for **80 ticks (4 seconds)**, recording the value from before entry (document 42 §2.3.6; document 42 §3 #14, #15; author's ruling #4, #5, #8).
- **Files touched**: `behavior/impl/VibrationBeanTrap.java:74-105` (**append one block** after the player confusion loop at `:91` and before `pos` is taken at `:92`; add `AI_SUSPEND_TICKS = 80` in the constants).
- **Concrete changes**: a full sweep of the `Mob`s inside the sphere + the **same** `vibration_bean.triggerRadius` as the trigger side and the same strict less-than (`:900-902`, default 4.0); an `isAlive()` filter; a per-mob call `MobAiSuspension.suspend(mob, SRC_VIBRATION_BEAN, now + 80)` (skeleton in document 42 §2.3.6); one INFO line printing "this round's mob count / suspend count" (document 41 §4.2 allows logging); the existing four steps (player confusion / explosion sound / `consume` / advancement award) are **not changed by a character**.
- **Implementation notes**: `Mob` already excludes players, so a redundant branch such as `!(mob instanceof Player)` is **not written** (`WildHunterAura:88` writes it because its parameter is a `LivingEntity`); **80 ticks is a constant, not a config key** (making it a key would take 175 to 177); 80 ticks ≠ `vibration_bean.confusionTicks` (the latter is the **player confusion duration**, the same value with a different meaning; they may not be merged, and "changing the confusion duration" may not be treated as "changing the AI duration").
- **Easy-to-miss points**: ① suspending only the **triggering** mob's AI (author's ruling #4 says every mob in range); ② introducing a second radius key (the trigger side / the confusion side / this block must use the same key); ③ applying confusion to mobs as well in passing (`CONFUSION`'s loop iterates only `level.players()`, untouched in this version); ④ using a creative / spectator account for the "does not trigger" negative assertion (the existing reading: creative / spectator **do not trigger** but are still stunned by a bean triggered by someone else, document 42 §2.3.6's last item / §9 item 13 — that test goes red, but what is red is the existing reading).
- **Acceptance assertions**:
  1. 1 zombie (`NoAI:0b`) + 1 non-ally player in survival mode inside the radius ⇒ after the trigger `NoAI:1b`; **80 ticks later** `NoAI` is back to **0b**; the player still gets `CONFUSION` (document 42 §7 #14).
  2. **3** mobs inside the radius ⇒ all 3 have their AI suspended (not only the triggering one), and the round's count in the log is **3** (document 42 §7 #14's 2nd and 3rd mob).
  3. Only players and no mobs inside the radius ⇒ behaviour identical to 0.2.0 (the round's count in the log is **0**, the four steps as usual) (document 41 §9 B-21).
  4. Covered by a second bean within the 80 ticks ⇒ the window refreshes to "now + 80" and does not stack to 160, and the original value is still the reading from before the **first** entry (document 41 §9 B-19).
  5. `AI_SUSPEND_TICKS = 80`; `FarmstormConfig`'s `b.push(` is still **60** and the line-leading `.define*` still **175** (this task does not touch config).

#### F-255 · Q-Bounce: suspend the AI while the effect lasts, skip in mount mode (M · 1.5 person-days | Depends on: F-251, F-253)

- **Goal**: a **non-player mob** carrying Q-Bounce stays `noAi` for as long as the effect lasts, and is restored by the original value the moment the effect ends; **mount mode is skipped**, and one that was already suspended and then switches to mount is **restored first and mounted after** (document 42 §2.3.4, §2.3.5; document 42 §3 #16; author's ruling #6, #7).
- **Files touched**: `effect/QbounceEffect.java:15-19` (currently 20 lines with 0 overrides ⇒ add **one** override, `onEffectAdded(LivingEntity, int)`); `combat/MobAiSuspension.java` (the entry-face forwarding + the two mount decisions inside the reconciliation).
- **Concrete changes**: override `onEffectAdded` only (in 21.1.213 it is called **only on the "there was no such effect before" branch**: `MobEffect.java:86` + `LivingEntity.java:957-961`); **do not override** `applyEffectTick` / `isDurationEffectTick`; the exit face **is not done in the effect class** (21.1.213's `MobEffect` has no `onEffectRemoved`) but is left to F-253's per-tick reconciliation reading `hasEffect`; mount's first layer (mount on entry ⇒ `return`, write no attachment) and second layer (the reconciliation reads mount ⇒ `release(SRC_Q_BOUNCE)` first) both live in `MobAiSuspension`.
- **Implementation notes**: the restore criterion is "**is the effect still on the entity**", **not** "how many ticks have passed since entry" (RT-56: `FriendChivesProjectile` rescans every tick and writes "field length − elapsed ticks", so a one-shot timer would be reset or would restore early); the entry point is **idempotent** (the `sources` bit is not set twice); the two source bits may coexist ("the Vibration Bean's time is up but Q-Bounce is still there" does not restore early).
- **Easy-to-miss points**: ① arming a one-shot timer on the first application (the two ways RT-56 goes wrong); ② parsing `qbounce.poseMode` inside the effect class (the truth of mount exists in one place; call F-251's entry only); ③ recording a "deadline tick" for Q-Bounce too (author's ruling #16); ④ overriding `applyEffectTick` to "check every tick" (it currently runs the empty implementation; overriding turns the effect into a class with side effects, which document 42 §2.3.4 explicitly does not do).
- **Acceptance assertions**:
  1. `friend_chives` landing on a mob ⇒ `NoAI:1b`; when the effect is refreshed repeatedly while it lasts, `sources` in the attachment is always `SRC_Q_BOUNCE` (it does not carry twice) and `originalNoAi` does not change with later refreshes (document 42 §7 #16).
  2. Run each of the three paths once — let the effect expire naturally (`friend_chives.effectTicks = 200`), `/effect clear <mob> farmstorm:q_bounce`, and make the mob drink milk ⇒ all three restore `NoAI` to the original value within **1 tick** (document 42 §7 #17).
  3. `qbounce.poseMode = mount` ⇒ a mob carrying Q-Bounce has `NoAI` always **0b** (never suspended for the whole run); after switching back to `render`, the suspension happens only on the **next application** (document 42 §7 #18).
  4. A mob already suspended, then the config switched to `mount` ⇒ it is restored by the original value first (`NoAI` returns to the original) and mounted after (document 42 §2.3.5's second layer; document 41 §9 B-26).
  5. `QbounceEffect.java` has **1** override (`onEffectAdded`); `applyEffectTick` / `isDurationEffectTick` have **0** hits; the class's total line count is ≥ 20 (it only grows).

#### F-256 · The core storage tooltip: guarantee the default chain + 3 acceptance entries (M · 1.5 person-days | Depends on: —)

- **Goal**: write "hovering an item shows the details" as an **explicit requirement + explicit acceptance**, with the implementation face **preferably changed by not one line** (document 42 §2.2; document 42 §3 #12, #13; D-178; author's ruling #3; RT-59).
- **Files touched**: `client/OutpostCoreScreen.java` (**42 lines**, overriding only `renderBg:37-41` — expected **zero change**); `menu/OutpostCoreMenu.java` (**140 lines**, 54 storage slots + 36 player slots, with the three `addSlot` calls at `:68` / `:79` / `:84`); the acceptance record entry is handed to F-264.
- **Concrete changes**: first confirm with document 42 §2.2's four pieces of evidence that the chain is not bypassed (the `render` / `renderTooltip` / `renderLabels` / `init` overrides are **0**; `renderTooltip` across the repository is **0**; all three `new Slot(` sites use vanilla classes); **only if the acceptance fails**, enable the fallback (add one `super.render(...)` to the `render` override, ≤ 2 lines of change, still adding no rendering code).
- **Implementation notes**: "guarantee" is an **acceptable-behaviour promise**, not "probably fine"; **add no** Screen / Slot / rendering code; take over none of the tooltip's positioning / scaling / background / delayed display (taking one over means taking all over, and a 42-line class would become a hand-drawn screen); this mod has **20** `tooltip.farmstorm` language keys, and they are inside the "details" scope — it is not just the name shown.
- **Easy-to-miss points**: ① rewriting `render` to draw the tooltip by hand (D-178 already rejected it); ② adding a custom `Slot` subclass to "provide the tooltip" (vanilla `Slot` is not responsible for tooltips; a subclass solves nothing); ③ using "is there a `renderTooltip` line in the code" as the criterion (the vanilla call site's line number cannot be checked on this machine, so the acceptance **may only** say "visible on hover"); ④ changing `ConverterScreen:77`'s `renderLabels` in passing (unrelated to this entry).
- **Acceptance assertions**:
  1. On one's own side, right-click the core with an empty hand to open the 54-slot screen, then hover the items in **slots 1 / 28 / 54** ⇒ the vanilla tooltip appears: the first line is the item name, the detail lines are ≥ 2 lines, and "1" is not shown when the count is 1 (document 42 §7 #11).
  2. Hovering an **empty slot** ⇒ **no** tooltip (not an empty box); hovering a **player-inventory slot** ⇒ the same as the 54 slots (document 42 §7 #12).
  3. Hovering an item with enchantments / with the `temporary_enchantments` component ⇒ both the enchantment lines and the effect lines show (document 41 §3.4 step 3).
  4. Source face: `OutpostCoreScreen`'s `render` / `renderTooltip` / `renderLabels` / `init` override count = **0**; `renderTooltip` across the repository has **0** hits; in `OutpostCoreMenu`, `new Slot(` = **3 call sites / 90 slots** (54 + 27 + 9), with no `Slot` subclass (document 42 §7 #13; for the call-count-versus-slot-count distinction see A-77).
  5. If item 1 fails and the fallback is enabled ⇒ the record states the number of changed lines and "still adds no rendering code", and updates item 4's override count from 0 to the actual value.

### Phase 43 · R5 Trap ×3 and the Config Window Opening (F-257 ~ F-260)

Phase goal: build a gate for ×3 that **applies to hostile mobs only**, and make the multiplier land only on **the one damage instance the trap settles itself** — of the 4 trap crops only 2 take part. Exit criterion: Blast Carrot against a zombie 21.0, against a player 7.0; Zangief Cane against a zombie 22.5; the 6 non-trap explosions such as Melon Grenade / Molotov read the same at a multiplier of 1.0 and of 10.0.

#### F-257 · The hostility predicate `util/Hostility#isHostile` (0 precedents) (S · 0.5 person-days | Depends on: —)

- **Goal**: add the union test **`net.minecraft.world.entity.monster.Enemy` instance ∪ `MobCategory.MONSTER`**, serving **only** the one gate of ×3 (document 42 §2.4; document 42 §3 #20; author's ruling #9; RT-57).
- **Files touched**: new `util/Hostility.java` (a stateless pure function, the same kind as `util/DeployedOwnership`, `util/DistanceMetrics`); read-only reference `combat/WildHunterAura:88` (the existing shape of a `Mob` predicate).
- **Concrete changes**: `isHostile(@Nullable Entity)` with four branches (not a `LivingEntity` → false; a player → false; `instanceof Enemy` → true; `getType().getCategory() == MobCategory.MONSTER` → true), in the order of document 42 §2.4's skeleton.
- **Implementation notes**: `Enemy` is a **pure marker interface** (`Enemy.java:1-11`, 11 lines, 0 methods) ⇒ `instanceof` is the whole test; `Monster implements Enemy` ⇒ the union's actual increment is only two classes (modded mobs whose category is `MONSTER` but which do not implement `Enemy`, and mobs that implement `Enemy` but are not in the `monster` package); **there are only 2 use sites** (F-259, F-260).
- **Easy-to-miss points**: ① swapping the 5 `getEntitiesOfClass(Monster.class, …)` sites for the new predicate in passing (that changes the trap's **trigger face**, which RT-57 forbids outright and D-174 keeps out as well); ② using `instanceof Monster` instead of `Enemy` (author's ruling #9 wants other mods' hostile mobs covered); ③ looking at `MobCategory` only (narrower than `Enemy`, and inconsistent with the union wording); ④ adding a whitelist in passing (document 40 §7 ③ fixes it as "do not widen the criterion").
- **Acceptance assertions**:
  1. Across the repository, `Enemy` goes from **0** hits to **1 file** (`util/Hostility.java`); `isHostile` has **1** definition (the 2 use sites appear only after F-259 / F-260 land).
  2. The line numbers of the 5 existing `Monster` filters are **unchanged character for character**: `BlastCarrotTrap:59`, `FangTrapCactusTrap:54`, `VibrationBeanTrap:59`, `ZangiefCaneTrap:174`, `PumpkinTurret:109` (document 42 §7 #27).
  3. `MobCategory`'s hit count goes from **2** (two `MISC`) to **3** (the new one is `MONSTER`).
  4. `gw.cmd build` exits with code 0; the Java file count goes **148 → 149** (F-248 already reached 146; F-252's two new classes take it to 148). The runtime reading belongs to F-259 (document 42 §8 ④ already hands the confirmation of "the modded-mob coverage gap" to the multiplier tasks).

#### F-258 · The `trap.damageMultiplier` key and its translation (**opens the window**) (S · 0.5 person-days | Depends on: —)

- **Goal**: the `trap` group gains 1 key (double, default **3.0**, range **1.0 ~ 10.0**) + 1 Chinese / English translation; this commit **opens both windows at once** (document 42 §2.5.1, §4.1.2, §4.3; document 42 §3 #24, #25; D-183, D-185; A-74).
- **Files touched**: `config/FarmstormConfig.java` (inside the `trap` section at `:917-927`, after `trapMaxTrapsPerPlayer` (`:924-926`) and before `b.pop()` at `:927`; in the fields, after `:223-225`); `datagen/ModLangProvider.java:412-413` onwards (+1 `config(entries, "trap.damageMultiplier", 中文, 英文)`).
- **Concrete changes**: `defineInRange("damageMultiplier", 3.0, 1.0, 10.0)` + comment + `translation(tr("trap.damageMultiplier"))`; the field declaration `public static ModConfigSpec.DoubleValue trapDamageMultiplier;`; the full language key `farmstorm.configuration.trap.damageMultiplier` (the `tr()` prefix `farmstorm.configuration.`, `FarmstormConfig:1001`). The copy follows document 42 §4.3's two lines.
- **Implementation notes**: **change no existing default** (`blast_carrot.maxDamage` stays 7, `zangief_cane.slamDamage` stays 7.5); the `trap` group goes 3 → 4 keys and the group count stays **60** (the `b.push` count does not move); the lower bound is **1.0** (= no change to the damage), not 0.0 (which would create a third state of "it hits but loses no health", document 42 §4.1.2); the upper bound is 10.0 (`cap 7 × 10 = 70` lies inside float's exact-integer range).
- **Easy-to-miss points**: ① adding the config without the translation (338 × 2 would become 338 / 337); ② writing the lower bound as 0.0; ③ writing the on-disk key name as `trapDamageMultiplier` (the on-disk key must be `damageMultiplier` inside the `[trap]` section); ④ changing the total in the class comment at `ModLangProvider.java:13-16` in passing (that is F-263's job, window discipline item 2).
- **Acceptance assertions**:
  1. **This key exists**: in `run/config/farmstorm-server.toml` (run `runServer` once first to regenerate), the `[trap]` section has exactly **4** keys and `damageMultiplier = 3.0` appears **1** time.
  2. **This key exists**: in `FarmstormConfig.java`'s line-leading `.define*`, `"damageMultiplier"` has **1** hit; `b.push(` is still **60** and `b.pop()` still **60**.
  3. **This key exists**: `zh_cn.json` and `en_us.json` each contain `farmstorm.configuration.trap.damageMultiplier` **1** time; after `ConvertFrom-Json`, zh-only = **0** and en-only = **0**.
  4. This task **does not write** the two totals `176` and `338` (§0.3 window discipline item 1); what the window allows is "this key exists / does not exist" + "4 keys inside the `[trap]` section" + "175 or 176 across the whole file (an interval reading)".

#### F-259 · `FarmstormExplosion`'s 9th parameter `trapSourced` + multiplication point ① (M · 1.5 person-days | Depends on: F-257, F-258)

- **Goal**: give the explosion a **minimal source marker**, so that the multiplier lands only on `blast_carrot`'s one explosion; **keep the 8-parameter overload delegating `false`**, leaving **the 6 non-trap call sites untouched** (document 42 §2.5.2, §2.5.3 multiplication point ①; document 42 §3 #21, #22; A-72).
- **Files touched**: `explosion/FarmstormExplosion.java:69-78` (the 8-parameter constructor → 9 parameters + an 8-parameter overload), `:178-179` (`hurtCapped`'s multiplication point); `behavior/impl/BlastCarrotTrap.java:84-86` (the **only** call site changed to pass `true`).
- **Concrete changes**: the new 9-parameter constructor takes `boolean trapSourced` (after `damageCap`); the original 8-parameter signature is kept as an overload whose only statement delegates `false`; the multiplication point: when `this.trapSourced && Hostility.isHostile(entity)`, `dealt *= FarmstormConfig.trapDamageMultiplier.get()`, multiplying **the final damage after the cap** (after `min(raw, cap)`).
- **Implementation notes**: **add no new damage source** (opening a `farmstorm:trap_blast` would take `damage_type` from 3 to 4 and 97 to 98, and would also change the tag semantics of `#minecraft:is_explosion`); **do not look the crop up through `BlastRule`** (`BlastRule.forCrop` returns the rule only and throws the profile away); **do not judge by `damageSource.getEntity()`** (that is "who set it off", not "which chain"); `:160`'s `ignoreExplosion` and the old reading of "do not spare the user" (`:144-147`) both stay.
- **Easy-to-miss points**: ① keeping both constructors but writing the multiplication on the 8-parameter path (that is ×3 for every explosion, exactly the error RT-58 / RT-57 are there to block); ② multiplying `damageCap` instead of the final damage (completely ineffective when `raw < cap`; `raw = 4` should give 12 and stays 4); ③ forgetting to have `BlastCarrotTrap:84-86` pass `true` (the trap then does not take part at all); ④ changing the 6 non-trap call sites in passing (they **must stay untouched** — that is precisely the core of this entry's acceptance).
- **Acceptance assertions**:
  1. Across the repository, `new FarmstormExplosion` still has **7** sites; **only** `BlastCarrotTrap:84-86` passes the 9th argument, and the other **6** argument lists still stop at `damageCap`: `MelonGrenadeProjectile:42`, `MelonSliceProjectile:40`, `MolotovProjectile:94`, `ToxicUbiServerProjectile:43`, `UbiServerProjectile:41`, `LuckyBerryRoller:212`.
  2. There are exactly **2** constructor declarations (8-parameter and 9-parameter); the 8-parameter one's body is a single line `this(…, false)`.
  3. Blast Carrot on a zombie: `min(raw, 7) × 3`, the capped reading **21.0**; the same explosion on a player: **7.0** (the first half of document 42 §7 #19).
  4. **The 6 non-trap explosions are unchanged point by point**: `melon_grenade` / `molotov`'s damage to a zombie reads **the same** with `damageMultiplier` at **1.0** and at **10.0** (document 42 §7 #20). This is the decisive reading for "the 6 were not ×3'd".
  5. `FarmstormDamageTypes` still has **3** `damage_type` `ResourceKey`s (`:20,23,26`); `farmstorm:crop_blast` is still the one shared by the 7 sites.

#### F-260 · `ZangiefCaneTrap#monsterBranch` multiplication point ② + the log printing the multiplied value (S · 0.5 person-days | Depends on: F-257, F-258)

- **Goal**: the mob branch's `hurt` multiplies by the multiplier against hostile mobs, and **the log prints the multiplied value**; the player branch and the knockback / sound do not move (document 42 §2.5.3 multiplication point ②; document 42 §3 #23; RT-58).
- **Files touched**: `behavior/impl/ZangiefCaneTrap.java:111-118` (the multiplication point after `amount` is read and before the two checks), `:126-128` (the INFO's damage field).
- **Concrete changes**: multiply after `amount` is read and **before** the two checks `source == null` and `amount > 0.0` (with `slamDamage` at 0 the product is still 0, so the behaviour does not change); the log's **format string is not changed**; only the value is swapped for the multiplied one (otherwise the log and `hurt`'s actual value disagree and the acceptance reading is void).
- **Implementation notes**: the player branch (`ZangiefPiledriverController:303` / `:311`) is **not** on the multiplier face; the knockback (`:124`) and the sound (`:125`) do not move; the hostility test calls only `Hostility.isHostile(target)` and never touches the trigger face's 5 `Monster` filters (including this class's `:174`).
- **Easy-to-miss points**: ① putting the multiplication after `amount > 0` (the log and `hurt` then fall out of step); ② swapping the `getEntitiesOfClass(Monster.class, …)` at `:174` too in passing (the trigger face, RT-57); ③ forgetting the log (half of this entry's acceptance reading depends on it).
- **Acceptance assertions**:
  1. Zangief Cane on a zombie: the damage reading is **22.5** (`7.5 × 3`), and the INFO line's "damage" field is also **22.5** (the second half of document 42 §7 #19; document 42 writes 11.25 there, and per A-76 22.5 prevails).
  2. Against a cow / a villager (non-hostile): **7.5**, and the log shows the same value (§6 table A names the participants one by one).
  3. A player stepping on it: goes through the phase chain (`ZangiefPiledriverController`), does not enter the multiplier face, and reads the same as 0.2.0.
  4. `slamDamage` at **0** ⇒ no `hurt`, and the log's damage is still **0.0** (the product is still 0).
  5. The line number of `ZangiefCaneTrap:174`'s `getEntitiesOfClass(Monster.class, …)` is unchanged character for character; the class's total line count is still **183**.

### Phase 44 · The Numeric Faces and the Tool Closure (F-261 ~ F-263)

Phase goal: turn "the document writes the key" into "the tool really expects it", and close the two totals at 176 / 60 and 338 × 2. Exit criterion: both lines of `verify_project.py`'s config section read 176; the comment at `ModLangProvider.java:13-16` reads 338 / 179; after the window closes, any task that adds or removes a key is out of scope.

#### F-261 · `verify_project.py`'s new parser `expected_config_keys_v021` (M · 1.5 person-days | Depends on: F-258)

- **Goal**: make the tool **really expect** `trap.damageMultiplier` and sync the expected number from 175 to 176; the four contracts land item by item per document 42 §4.1.1 (document 42 §3 #26; RT-60, RT-60b; A-75).
- **Files touched**: `tools/verify_project.py` (add `expected_config_keys_v021` shaped after `expected_config_keys_v012` at `:137-179`; the union region of `main()` at `:382-391`; the expected number 175 → 176).
- **Concrete changes**: four contracts — ① bound the range with `^#### 4\.1\.1\b` and the following `^#### 4\.1\.2\b` (do not scan the whole file with `^\|\s*\d+\s*\|`: the tables of document 42 §3 and §7 also start with `| 1 |`); ② exactly **7** columns per line, with column 2 matching `` ^`([A-Za-z0-9_]+\.[A-Za-z0-9_.]+)`$ `` and column 6 starting with "增" / "删"; ③ the row-count assertion **== 1** (the same shape as the 11-row assertion at `:172-178`); ④ the result is merged into `main()`'s `union` (`:382-391`), and the expected number goes 175 → 176.
- **Implementation notes**: the current tool reads only **6 fixed documents** (`main():349-355`) and **does not read document 42 today** ⇒ "writing the key into the document" is a **necessary but not sufficient** condition; the parser is **bound** to the table format of document 42 §4.1.1, and when the format is broken it must **throw with the file name + line number** in the shape of `:172-178` (better red than silently checking less).
- **Easy-to-miss points**: ① changing the expected number without adding the parser (the tool stays green and nobody checks the key, so A-66's false green continues); ② scanning the tables with a whole-file regex (it would over-capture rows from §3 / §7); ③ forgetting to add document 42 to `main()`'s document list (then the parser never sees its input); ④ touching `EXPECTED_COUNTS` (the registry surface is still 97; this task does not move it).
- **Acceptance assertions**:
  1. `python tools/verify_project.py`'s config section prints "文档口径键数: **176**" and "生成 toml 键数: 176" (document 42 §7 #2).
  2. The new parsing function returns **exactly 1** key for `docs/zh/42-TECH-0.2.1.md` §4.1.1: `["trap.damageMultiplier"]`.
  3. `grep -c 'trap\.damageMultiplier' docs/zh/42-TECH-0.2.1.md` ≥ **2**, and the contract regex hits **1** row of §4.1.1's table (document 42 §7 #22).
  4. Counter-example: on a **temporary copy** (put in `$env:TEMP`, document 42 itself untouched by a character), change §4.1.1 to 2 rows ⇒ the tool **goes red with the file name + line number**, not a silent pass.

#### F-262 · The config ledger at 176 keys / 60 groups (**closes the window**) (S · 0.5 person-days | Depends on: F-258, F-261)

- **Goal**: close the config totals at **176 keys / 60 groups**, giving three mutually distinct readings (document 42 §4.2's three independent checks; D-185; A-74).
- **Files touched**: `config/FarmstormConfig.java` (a key-by-key count of the static block at `:289-995`); `run/config/farmstorm-server.toml` (the reading after regeneration); `tools/verify_project.py` (the union = 176).
- **Concrete changes**: **verification and recording only**, no source change; three checks — ① the line-leading `.define*` = **176**; ② the `[trap]` section has **4** keys, the section count is **60**, the total key count is **176** (run `runServer` once first to regenerate); ③ the tool's union = **176**.
- **Implementation notes**: **for key paths, recognise only `run/config/farmstorm-server.toml`** (measured 801 lines / 60 sections / 175 keys); the dozens of `run-*/config/` copies in the repository are old ones (`run-verify` and the like, 12,238 bytes and about 121 keys), and taking the wrong one yields the wrong conclusion "only 121 keys" (already stepped into by document 42 §9 item 2, a reading pitfall outside A-70). This task **closes the window**; from then on any task that adds or removes a config key is out of scope.
- **Easy-to-miss points**: ① taking an old `run-*/config/` copy as the baseline; ② adding the 3 client keys (`hud.*`) into 176 (they are not counted, document 42 §4.2); ③ computing the group count as 61 (`trap` is an existing group; this adds a key, not a group); ④ forgetting the basis "175 live line-leading `.define*` + 1 comment line (`:935`)" (comment lines are not counted).
- **Acceptance assertions**:
  1. `FarmstormConfig.java`'s line-leading `.define*` = **176** (176 live; the comment line at `:935` is not counted); `b.push(` = `b.pop()` = **60**.
  2. `run/config/farmstorm-server.toml`: sections (`^\s*\[`) = **60**, keys (`^\s*[A-Za-z0-9_]+\s*=`) = **176**, and the `[trap]` section has **4** keys (including `damageMultiplier = 3.0`).
  3. Both lines of `verify_project.py`'s config section read **176** (document 42 §7 #2).
  4. The closing record states that the acceptances of F-258 ~ F-261 contain **neither** of the two numbers "176 / 60" (the re-check of §0.3 discipline item 1).

#### F-263 · The language-key ledger at 338 × 2 (**closes the window**) (S · 0.5 person-days | Depends on: F-258)

- **Goal**: close the language keys at **338 × 2** (the same key set in Chinese and English), and sync `ModLangProvider`'s class comment (document 42 §4.3; D-185; A-74).
- **Files touched**: `src/generated/resources/assets/farmstorm/lang/zh_cn.json` / `en_us.json` (**338** each); `datagen/ModLangProvider.java:13-16` (the class comment's `**337**` → `338`, `**178** config` → `179`).
- **Concrete changes**: verification + comment sync only: `:14`'s `**178** config (= 175 server + 3 client)` → `**179** config (= 176 server + 3 client)`; `:16`'s `= **337**` → `= **338**`.
- **Implementation notes**: **the language keys have no automatic gate** — `verify_project.py` §3 only compares the zh / en **key sets for equality** and **does not check the number 338** (document 42 §9 item 4) ⇒ the key set can only be counted by hand with `ConvertFrom-Json`; Chinese and English must have **the same keys and the same count** (adding only the Chinese one would turn 338 × 2 into 338 / 337).
- **Easy-to-miss points**: ① changing the json and forgetting the class comment (the comment says 337 while the reality is 338, and the next author will check against the comment); ② adding only the Chinese without the English; ③ counting the 20 `tooltip.farmstorm` entries into `farmstorm.configuration.*`'s 179; ④ changing the "139 个配置项" comment at `:237` in passing (that is the existing drift registered by document 42 §9 item 11 and is **out of this version's scope**).
- **Acceptance assertions**:
  1. `ConvertFrom-Json` over the key sets: `zh = 338`, `en = 338`, zh-only = **0**, en-only = **0** (document 42 §7 #5).
  2. `farmstorm.configuration.*` = **179** (= 176 server + 3 client); `tooltip.farmstorm.*` is still **20**.
  3. `ModLangProvider`'s `config(entries, …)` count = **179** (178 before the change).
  4. In `ModLangProvider.java:13-16`, `337` has **0** hits and `338` has ≥ 1; the "139 个配置项" at `:237` is unchanged character for character.

### Phase 45 · Full Acceptance and Sign-off (F-264 ~ F-266)

Phase goal: run document 42 §7's 31 entries and document 41 §9's 36 entries one by one, and sign the red line, the registry surface, the numeric faces and upstream consistency into one reviewable record. Exit criterion: neither record misses an entry; the sign-off carries seven readings; the upstream files are unchanged by a character.

#### F-264 · Full execution of document 42 §7's 31 acceptance entries (L · 3.0 person-days | Depends on: F-247 ~ F-263)

- **Goal**: execute document 42 §7's **31** acceptance entries one by one and record them in `.work/v021-verify/acceptance.md` (document 42 §7 plus the "four must-test combinations" below the table).
- **Files touched**: the acceptance record `.work/v021-verify/acceptance.md` (new); read-only `run/logs/latest.log`, `run/config/farmstorm-server.toml`, `src/generated/.../lang/*.json`, `/data get`.
- **Concrete changes**: for each of the 31, "set up → read → record", each giving: the number, the action performed, the reading, pass / fail, and the evidence (the log line verbatim or the `/data get` output).
- **Implementation notes**: the four **must-test combinations** cannot be split — `#6 + #7` (the rebind was not filtered by the particle gate), `#14 + #15` (restoring the original vs restoring to having AI), `#19 + #20` (trap vs non-trap), `#23 + #24` (server stop vs crash); `#1`'s Java file count is written as **149** (145 + the 4 new classes, A-78).
- **Easy-to-miss points**: ① running `#6` without `#7` (at the default 40 ticks even 200 ticks is "within 300 ticks", so the filtering is invisible); ② using a creative / spectator account for the Vibration Bean's "does not trigger" negative assertion (the existing reading goes red, document 42 §2.3.6's last item); ③ writing `#11`'s criterion as "there is a `renderTooltip` in the code" (only "visible on hover" may be written); ④ copying document 42's 11.25 for `#19`'s Zangief Cane reading (A-76).
- **Acceptance assertions**:
  1. The record holds **31** entries, `#1` ~ `#31`, none missing, each with a reading (not the two words "already verified"); `#25` (the `.dat` file count under `world/data/` unchanged) and `#28` (restoring deletes the attachment) pass.
  2. `#7` (still rebound within 100 ticks at a particle interval of 1200) and `#20` (the 6 non-trap explosions read the same at a multiplier of 1.0 / 10.0) pass — these two are RT-51's and RT-57's decisive readings respectively.
  3. `#21` (the fang damage identical to 0.2.0 point by point) passes, and the record states "**2** of the 4 trap crops take part" (RT-58).
  4. `#22`'s `grep -c 'trap\.damageMultiplier'` ≥ 2 and the parser returns 1 key; `#29`'s startup log is still `自检 注册对象 97 个 … → OK`.

#### F-265 · Re-run of document 41 §9's 36 boundary entries (L · 3.0 person-days | Depends on: F-264)

- **Goal**: run document 41 §9's **36** boundary entries (B-01 ~ B-36) one by one and record them in `.work/v021-verify/boundaries.md` (document 41 §9; kept separate from document 42 §7 because document 42's acceptance table does not include the B face).
- **Files touched**: the boundary record `.work/v021-verify/boundaries.md` (new); read-only source and in-game.
- **Concrete changes**: execute B-01 ~ B-36 one by one, grouped into five by document 41 §9's attribution (B-01 ~ B-12 belong to R1, B-13 ~ B-17 to R2, B-18 ~ B-23 to R3, B-24 ~ B-30 to R4, B-31 ~ B-36 to R5), with one summary per group.
- **Implementation notes**: **6 of them are negative criteria** (B-04 / B-06 / B-16 / B-25 / B-26 / B-33), where the criterion is "it did not happen" rather than "what happened"; B-19 (a second cover refreshes without stacking) and B-27 (the five cleanup paths) must be set up separately; B-30 (before and after a save load) and F-264's `#24` (crash residue) are **not the same entry** — the former verifies "the persisted original", the latter "the load settlement".
- **Easy-to-miss points**: ① re-running B-30 as `#24`; ② missing B-11 (two cores covering the same batch of unbound cultivators) — it is RT-51's boundary face; ③ missing B-03 / B-12 (unloaded chunks / after a restart before the tickets are restored) — both must prove "no new chunk was loaded".
- **Acceptance assertions**:
  1. The record holds **36** entries, `B-01` ~ `B-36`, none missing, labelled in five groups.
  2. All six negative criteria (B-04 / B-06 / B-16 / B-25 / B-26 / B-33) pass.
  3. B-19 (a second cover within 80 ticks ⇒ the refresh does not stack and the original is still the first) and B-27 (all five cleanup paths go through "restore by the original + delete the record") pass.
  4. B-11 passes: when two cores of one's own side cover the same batch of unbound cultivators, the same cell lands on one core only (the first writer claims the cache).

#### F-266 · Sign-off on the red line, the registry surface and upstream consistency (M · 1.5 person-days | Depends on: F-261 ~ F-265)

- **Goal**: sign off on five things — the red line (0 Mixin / 2 packets), the registry surface **97 (+0)** and **the 13th category outside the count**, the numeric faces **176 / 60** and **338 × 2**, the **do-not-change list** (document 42 §3 #27, #28 and the 5 `Monster` filters), and upstream consistency (document 42 §9's 16 differences and this document's handling under A-70 ~ A-81) (A-73).
- **Files touched**: the sign-off record `.work/v021-verify/signoff.md` (new); read-only source and logs.
- **Concrete changes**: give a reading item by item: `@Mixin` = 0, `*.mixins.json` under `src/` = 0, `.playToClient` = 2, `PROTOCOL_VERSION = "2"`, the 12 classes summing to 97, `data_components` = 1, **attachment types = 1 (outside the count)**, config 176 / 60, lang 338 × 2.
- **Implementation notes**: **the 13th category must be written into the sign-off explicitly** — "plus 1 attachment type `farmstorm:ai_suspend`, not counted into 97, and `verify_project.py` checks neither its existence nor its count" (author's ruling #17; A-73); the two "do not change" lines (document 42 §3 #27 / #28) need evidence of being **unchanged character for character** (line number + fragment), not "it should not have moved".
- **Easy-to-miss points**: ① counting the attachment type into 97 (which yields the wrong conclusion 98); ② counting "`*.mixins.json` across the repository" (only the `src/` basis is 0; `.scratch/p25a/src/neoforge.mixins.json` is experimental residue, document 42 §9 item 10); ③ signing off with "passed" and no reading; ④ changing any sentence of document 42 §9 in passing (upstream is unchanged by a character; differences are registered only).
- **Acceptance assertions**:
  1. The sign-off record carries eight readings: Mixin **0** (the `src/` basis), `*.mixins.json` **0** (`src/`), `.playToClient` **2**, registry surface **97**, `data_components` **1**, attachment types **1** (outside the count), config **176 / 60**, lang **338 × 2**.
  2. The 12-class assertion at `Farmstorm.java:169-172` is unchanged character for character; the line numbers of the 5 `Monster` filters are unchanged character for character (document 42 §7 #27); `FangTrapCactusTrap:87-109`'s vanilla fang spawning and `vibration_bean`'s 0 damage are unchanged character for character (document 42 §3 #28).
  3. The sign-off names the coverage gap: "the attachment type is in no `EXPECTED` group and in no `EXPECTED_COUNTS` sum" (document 42 §1.2.1; A-73).
  4. The sign-off cites document 42 §9's 16 registered differences and states how this version handles them under A-70 ~ A-81; the mtimes of `.work/` and `docs/zh/40` ~ `42` are unchanged.

---

## 3. Dependency Graph and Parallelism

### 3.1 Six Dependency Chains (sketch)

The diagram draws only the **predecessor → successor** trunk and does not try to draw every edge (F-264 actually waits for 17 predecessors):

```
Chain 1 (release):     F-247
Chain 2 (rebind):      F-248 → F-249 → F-250
Chain 3 (AI carrier):   F-252 ─┐
                        F-251 ─┴→ F-253 ─┬→ F-254
                                        └→ F-255
Chain 4 (tooltip):      F-256
Chain 5 (hostility and multiplier): F-257 → F-259 / F-258 → F-260 (also needs F-257) / F-258 → F-261 → F-262 / F-258 → F-263
Closing:               F-264 (waits for F-247 ~ F-263, all of them) → F-265 → F-266
```

**Three ways to read the diagram**:

1. **The two mutually independent starting points are symmetric**: F-248 (region enumeration) and F-257 (the hostility predicate) are both "0 precedents across the repository" pure utilities, each separately committable and separately revertible; they feed the rebind chain and the multiplier chain respectively, and they must **not** be merged into their callers to "save one task" (merged in, they would have no independently runnable acceptance).
2. **F-252 and F-251 are the AI chain's two parallel predecessors**: the carrier (the attachment) and the mount entry do not depend on each other, and F-253 waits for both; folding the visibility change into F-253 would mix a "one modifier" change into an L task and distort the rollback granularity.
3. **F-264 is the only big convergence point in the whole graph**: it waits for all of F-247 ~ F-263 to land (including the two window-closing tasks), so the window must close before the acceptance — otherwise the acceptance would check against a document whose "comment says 337 while the reality is 338".

### 3.2 Critical Path

Taking the longest predecessor per dependency (each node takes the maximum cumulative completion time of all its predecessors):

```
F-252 attachment carrier   1.5   (cumulative 1.5)
 └─ F-253 MobAiSuspension  3.0   (cumulative 4.5, also needs F-251's 0.5; take the larger, 4.5)
     └─ F-255 Q-Bounce hookup  1.5   (cumulative 6.0)
         └─ F-264 acceptance  3.0   (cumulative 9.0)
             └─ F-265 boundaries  3.0  (cumulative 12.0)
                 └─ F-266 sign-off  1.5 (cumulative 13.5)
```

**Critical path = 13.5 person-days / 6 tasks**, **49.1%** of the 27.5 total. This chain is the result of a pure dependency derivation.

**A tied second 13.5**: `F-252 → F-253 → F-254 → F-264 → F-265 → F-266` (`1.5 + 3.0 + 1.5 + 3.0 + 3.0 + 1.5 = 13.5`). It forks after F-253 into "Vibration Bean" rather than "Q-Bounce", and both reach 6.0 before F-264 — when scheduling, treat F-254 and F-255 as the same layer.

The next-longest chains (again taking the longest predecessor per dependency, not entering the critical path):

| Rank | Chain | Person-days |
|---|---|---|
| 3 | F-248 → F-249 → F-250 → F-264 → F-265 → F-266 | 1.5 + 1.5 + 1.5 + 3.0 + 3.0 + 1.5 = **12.0** |
| 4 | F-257 → F-259 → F-264 → F-265 → F-266 | 0.5 + 1.5 + 3.0 + 3.0 + 1.5 = **9.5** |
| 5 | F-256 → F-264 → F-265 → F-266 | 1.5 + 3.0 + 3.0 + 1.5 = **9.0** |
| 6 | F-258 → F-260 → F-264 → F-265 → F-266 | 0.5 + 0.5 + 3.0 + 3.0 + 1.5 = **8.5** (F-258 → F-261 → F-262 → F-266 is tied with it, `0.5 + 1.5 + 0.5 + 1.5 = 4.0`) |

> **How to read it**: apart from `F-258 → F-261 → F-262 → F-266` (4.0), every chain converges on F-264, so **every M / L before F-264 is parallel**; to shorten the calendar duration, squeeze F-253's 3.0 person-days first (it is the only L on the 13.5 critical path), not the acceptance (the acceptance is 6.0 person-days at the chain's tail, and squeezing it only thins the readings).

### 3.3 Parallel Clusters

| Cluster | Phase | Parallelizable tasks | Parallelism | Notes |
|---|---|---|---|---|
| A | 40 ~ 41 | F-247, F-248 | 2 | the version number and the region enumeration share no file; F-249 / F-250 are serial with F-248 |
| B | 42 | F-251, F-252, F-256 | 3 | the mount entry, the attachment carrier and the tooltip share no file |
| C | 43 | F-257, F-258 | 2 | the predicate and the config key do not depend on each other; F-259 / F-260 each waits for them |
| D | 43 | F-259 and F-260 | 2 | the two multiplication points belong to two classes and can run two people in parallel |
| E | 44 ~ 45 | F-261, F-263 | 2 | the tool and the language ledger share no file; F-262 waits for F-261, and after F-264 everything is serial level by level |

**The parallelism ceiling is 6** (clusters A + B + C open together), in practice constrained by **same-file conflicts**: F-249 / F-250 both change `OutpostCoreBlockEntity.java` (serial); F-252 / F-253 / F-255 all change `MobAiSuspension.java` (serial in the order 252 → 253 → 255); F-257 / F-260 both change `ZangiefCaneTrap`'s call face (`Hostility` must land first); F-258 / F-261 / F-262 / F-263 share the one reading face "config and language" (serial in the order 258 → 261 / 262 / 263). Under the convention "one task touches only its own segment", **3 lanes** can run in parallel reliably.

---

## 4. Workload Summary

### 4.1 By Phase and Milestone

| Phase | Name | Tasks | Task count | S | M | L | Person-days | Milestone |
|---|---|---|---|---|---|---|---|---|
| 40 | Version and Baseline | F-247 | 1 | 1 | 0 | 0 | `0.5` = **0.5** | **M36** |
| 41 | R1 Periodic Rebind | F-248 ~ F-250 | 3 | 0 | 3 | 0 | `3 × 1.5` = **4.5** | **M37** |
| 42 | R2 Tooltip and R3/R4 AI Intervention | F-251 ~ F-256 | 6 | 1 | 4 | 1 | `0.5 + 4 × 1.5 + 3.0` = **9.5** | **M38** |
| 43 | R5 Trap ×3 and Config Window Opening | F-257 ~ F-260 | 4 | 3 | 1 | 0 | `3 × 0.5 + 1.5` = **3.0** | **M39** |
| 44 | Numeric Faces and Tool Closure | F-261 ~ F-263 | 3 | 2 | 1 | 0 | `2 × 0.5 + 1.5` = **2.5** | **M40** |
| 45 | Full Acceptance and Sign-off | F-264 ~ F-266 | 3 | 0 | 1 | 2 | `1.5 + 2 × 3.0` = **7.5** | **M41** |
| **Total** | | **F-247 ~ F-266** | **20** | **7** | **10** | **3** | **27.5** | **M36 ~ M41** |

### 4.2 Totals (Two Numbers + the Arithmetic)

- **Raw person-days**: `7 × 0.5 + 10 × 1.5 + 3 × 3.0 = 3.5 + 15.0 + 9.0 = **27.5 person-days**`
- **With a 25% buffer**: `27.5 × 1.25 = **34.375 person-days**`

Three cross-checks:

1. Complexity summary: `7 + 10 + 3 = 20` = the task count;
2. Person-day summary: `3.5 + 15.0 + 9.0 = 27.5` = the sum of the six Phase subtotals `0.5 + 4.5 + 9.5 + 3.0 + 2.5 + 7.5 = 27.5`;
3. Buffer: `27.5 × 1.25 = 34.375`.

> The buffer absorbs rework and debugging only, not scope change. Compared with 0.2.0 (34 tasks / 61.0 raw person-days), this version has 14 fewer tasks and 33.5 fewer raw person-days: the change comes from three places — 0.2.0 had 14 L tasks (container, menu, migration, state machine, resources, rendering, two acceptance batches) while this version has only 3; this version has 7 S tasks (the version number, the visibility, the predicate, the config key, multiplication point ②, the two ledgers); and the two "written from scratch" components (F-248 / F-257), though counted one tier up, are only one class in size.

### 4.3 Calendar Duration After Parallelism

- **Single serial**: `34.375 person-days`, about **6.9 weeks** at 5 days/week.
- **A stable 3 lanes** (§3.3's last paragraph): `27.5 ÷ 3 ≈ 9.2` raw person-days, `9.2 × 1.25 ≈ **11.5 person-days** ≈ **2.3 weeks**` after the buffer (@ 5 days/week).
- **The critical path's lower bound**: `13.5 person-days` (§3.2) — **it is longer than the 3-lane 11.5**, so the real lower bound is **13.5 person-days ≈ 2.7 weeks**, and no amount of added people goes below it; the gap to the 3 lanes is `13.5 − 11.5 = 2.0 person-days`, and to go faster one can only squeeze F-253 (the only L on the critical path) or split F-264 / F-265 into two batches (**not recommended**: the 31 and the 36 each have must-test combinations, and splitting them scatters the paired readings).

---

## 5. Adjudication Ledger (from A-70)

This section collects **12 entries**: all newly established while this document was written, all with the status **landed**; each gives "conclusion / basis / landing place", and the upstream files are unchanged by a character (differences are registered here and in the delivery note only).

### 5.1 A-70 · The version strings are **6** by line, not 4

- **Conclusion**: 1 place in `gradle.properties:11` + **5 lines** in the two `.cmd` scripts (`一键构建.cmd:5,11,12`, `检查环境.cmd:5,14`) = **6 literal occurrences**; the acceptance is written as 6 (`0.2.1` hits 6, `0.2.0` hits 0).
- **Basis**: document 40 §3.2 line 2 says "the jar name + the two banner lines, **4 occurrences** of the version string", and §7 ⑤ says it again: "the version string occurs 4 times, 2 of them banner lines … the other 2 are jar names (`一键构建.cmd:11,12` and `检查环境.cmd:14`)" — **it lists 5 line numbers but records 4 occurrences** (counting "the two jar-name lines of 一键构建" as one). Measured, the two `.cmd` scripts hold 5 lines containing `0.2.0`, and with `gradle.properties` there are 6 occurrences.
- **Landing place**: **F-247** (the 6 literals), **F-264** (the artifact-face acceptance), **F-266** (the `.jar` name in the sign-off).

### 5.2 A-71 · The rebind wiring, the comment at `CultivatorBlock:73-84` and **one rebind INFO line** must be one commit

- **Conclusion**: the `serverTick` wiring (F-250) and the class-comment change at `CultivatorBlock:73-84` go into **one commit**; that same commit also adds **one rebind INFO line** (core coordinate + this round's new entries + cumulative / cap).
- **Basis**: ① item 11 of BRIEF §3.4 requires the comment to "change together with the periodic rebind" — the comment currently says "不自动认领……事后补一个核也不会绑上" (`CultivatorBlock:81-83`, measured), exactly the opposite of D-176, and committing the two separately leaves a window in which "the comment says it cannot, the code does it"; ② document 42 §7 #8's observation position is "the rebind INFO line (`V-03`)", while `rebindNearby`'s skeleton in document 42 §2.1.3 **has no logging call** ⇒ the observation face is missing from document 42's skeleton, so this document adds the line per document 42 §7 #8's need (otherwise the "phase stagger" acceptance has no reading).
- **Landing place**: **F-250** (the wiring + the comment + the INFO), **F-264** (`#6` ~ `#8`).

### 5.3 A-72 · `FarmstormExplosion`'s 9th parameter and multiplication point ① must be one commit

- **Conclusion**: adding `boolean trapSourced` and using it in `hurtCapped` go into **one commit**; "add the parameter first (unused), multiply later" is not allowed.
- **Basis**: with the parameter added and the multiplication not, the 9th argument is a purely dead parameter; with the multiplication added first, one would have to decide "is this explosion a trap's" in the intermediate state — both routes create a window in which "the 6 non-trap call sites may be ×3'd", and those 6 sites' **zero change** is precisely this entry's acceptance core (document 42 §2.5.2's seven-site table, §2.5.3 multiplication point ①).
- **Landing place**: **F-259** (one commit), **F-264** (`#19` / `#20` run as a pair).

### 5.4 A-73 · The 13th-category attachment: register it explicitly + admit the tool does not check it

- **Conclusion**: `farmstorm:ai_suspend` is registered as "the 13th category outside the count", **written down in three places** — F-252's acceptance item 5, F-266's sign-off ("attachment types 1, not counted into 97, the tool checks neither its existence nor its count"), and document 42 §1.2.1 itself; no task **may** describe the attachment as "+1 registry object".
- **Basis**: author's ruling #17 (97 does not move, but the type must be registered explicitly and may not be invisible); document 42 §1.2.1 line 5 (neither `EXPECTED` nor `EXPECTED_COUNTS` contains an attachment type); document 42 §9 item 14 (an attachment ≠ a data component; what D-175 rejected is the latter). "What the tool does not count equals not registered" is the same in nature as A-66's false green.
- **Landing place**: **F-252** (the registration + acceptance 5), **F-266** (sign-off 3), **F-261** (leaves `EXPECTED_COUNTS` alone; still 97).

### 5.5 A-74 · The opening and closing tasks of the two numeric windows pinned down

- **Conclusion**: the window **opens at F-258** (the config key + its Chinese / English translations written in one go); the config side **closes at F-262**, the language side **at F-263**, the tool's expectation face **at F-261**. Inside the window write only "this key exists / does not exist" and "4 keys in the `[trap]` section / 175 or 176 across the whole file (an interval reading)", and **do not write** `176` / `338`.
- **Basis**: the three window disciplines of §0.3; document 42 §4.2's three independent checks (source lines / toml / the tool's union) and §4.3's two disciplines (Chinese and English with the same keys and the same count; language keys are not generated automatically); document 42 §9 item 4 (the language keys have **no automatic gate**; `verify_project.py` only compares the key sets for equality).
- **Landing place**: **F-258** (opens), **F-261 / F-262 / F-263** (the three closes), **F-264** (`#2` / `#4` / `#5` re-run).

### 5.6 A-75 · The new parser's four contracts + "document and parser in one commit"

- **Conclusion**: `expected_config_keys_v021` is implemented strictly per document 42 §4.1.1's four contracts (range bounding / 7 columns + the column-2 regex + column 6's "add / delete" / row count == 1 / merged into `union` with an expectation of 176); together with "document 42 §4.1.1 really holds one `trap.damageMultiplier` row" it counts as **one commit**'s deliverable.
- **Basis**: document 42 §4.1.1's four "parsing contracts" and its "⚠️ writing the document without changing the parser = the false green continues" (the tool reads only 6 fixed documents and does not read document 42 today); RT-60 / RT-60b; document 42 §9 item 3 and item 1 of "the four hints to document 43" below the table.
- **Landing place**: **F-261** (the parser + the expected number), **F-264** (`#22`), **F-266** (the sign-off cites the tool's three-section output).

### 5.7 A-76 · Zangief Cane's ×3 reading is **22.5** (document 42 §7 #19's 11.25 is a typo)

- **Conclusion**: `zangief_cane`'s damage against hostile mobs = `7.5 × 3 = **22.5**`; F-260 and F-264's `#19` are always written as 22.5.
- **Basis**: both the worked-example table of document 41 §6.5 and line 26 of §7 read `7.5 × 3 = 22.5`; the formula of document 42 §2.5.3 multiplication point ② is also `amount × multiplier`; document 42 §7 #19 writes "`zangief_cane` against a zombie = **11.25** (7.5 × 3)", where the arithmetic inside the parentheses contradicts the value (11.25 = 7.5 × 1.5). Following the arithmetic `7.5 × 3`, take **22.5**.
- **Landing place**: **F-260**'s acceptance 1, **F-264**'s `#19`; back-fill target: document 42 §7 #19.

### 5.8 A-77 · The criterion for `new Slot(` is **3 call sites / 90 slots** (not "54 + 36 times")

- **Conclusion**: in `OutpostCoreMenu`, `new Slot(` is **3 call sites** (`:68` the storage loop, `:79` the main-inventory loop, `:84` the hotbar loop), producing **90** slots at runtime (54 + 27 + 9); the acceptance is written as "3 call sites / 90 slots / no `Slot` subclass".
- **Basis**: document 42 §7 #13 writes "`new Slot(` in `OutpostCoreMenu` = **54 + 36 times**" — 54 + 36 = 90 is the **slot count**, not the call count; measured, `new Slot(` appears only 3 times (all inside loops). The slot total 90 agrees with document 42; only the measure word "times" is wrong.
- **Landing place**: **F-256**'s acceptance 4, **F-264**'s `#13`.

### 5.9 A-78 · Two counts corrected against measurement: the Java file count **149**, the constructor's registers **11**

- **Conclusion**: ① this version adds **4 classes** (`util/AreaBlockEntities`, `combat/MobAiSuspension`, `util/Hostility`, `registry/FarmstormAttachments`), so the file count is `145 + 4 = **149**`; ② `Farmstorm.java`'s constructor currently holds **11** `.register(modBus)` calls (`damage_type` is data-driven with only 3 `ResourceKey`s and never had a register call), which becomes **12** after the attachment.
- **Basis**: ① document 42 §1.1 says "currently 145 … 4 classes added … **149** after the change", while document 42 §7 #1 says "the Java file count = **148**", a difference of 1; ② document 42 §3 #29 says "the `.register(modBus)` region of the 12 registers", while the constructor body has only **11** `.register(modBus)` lines (`:46,47,48,49,50,52,53,54,55,57,58`), the 10th of the 12 classes (`damage_type`) having no register call.
- **Landing place**: **F-248** (145 → **146**, `AreaBlockEntities`), **F-252** (146 → **148**, `MobAiSuspension` + `FarmstormAttachments`), **F-257** (148 → **149**, `Hostility`); **F-252**'s acceptance 2 (registers 11 → 12), **F-264**'s `#1` (149).

### 5.10 A-79 · "Cannot observe" gets no upper-bound constant (document 42 §2.3.4 prevails)

- **Conclusion**: when `sweep` cannot read the entity it **drops only the two in-memory items and keeps the attachment**, and sets **no** upper-bound constant for "give up after how many ticks of not observing".
- **Basis**: document 42 §2.3.4 says explicitly "therefore **no** upper-bound constant such as 'give up after how many ticks of not observing' is needed — memory may be dropped freely, the attachment is the truth"; whereas RT-54 mitigation (c) in document 42 §6 says "delete the index only, keep the attachment, with a **1200-tick upper bound** (§2.3.4)" — §2.3.4 contains no such bound, and the two contradict each other. Execute per §2.3.4's body text (a bound would create the third semantics "give up on expiry", and the persisted original does not need it anyway).
- **Landing place**: **F-253**'s acceptance 5 (0 hits for an upper-bound constant), **F-264**'s `#23` / `#24`.

### 5.11 A-80 · The cleanup timing for a chunk unload follows document 42 §2.3.3 path ⑤ (document 41 §4.5 / §5.5 are not adopted)

- **Conclusion**: when the entity's chunk unloads it does **not** "restore by the original + delete the record" (the entity is not in the world at that moment, so `setNoAi` has no object to write to); instead the attachment is **kept and settled on load** (the Vibration Bean path is always treated as already expired, the Q-Bounce path is judged by whether the effect is still there).
- **Basis**: document 41 §4.5 and §5.5 both write "chunk unload" as "restore by the original first, then delete the record (the 5th cleanup path)"; document 42 §2.3.3 path ⑤ says explicitly "restoring on unload is **not possible** … so this version's handling is '**settle on load**'", and RT-55 mitigation (c) also says "unloading **does not delete the attachment**". The two contradict each other; document 42 prevails (it is the technical face and it gives the reason why nothing may be written on unload). Document 41's intent ("`noAi` must not be left in the save") is satisfied by "settle on load" and is not lost.
- **Landing place**: **F-253** (`adopt`'s three criteria), **F-264**'s `#23` / `#24`, **F-265**'s B-27 / B-30.

### 5.12 A-81 · Two bases of D-179 need back-fixing: `setNoAi` is really 2 sites, and the original-value table has become **persisted**

- **Conclusion**: ① across the repository `setNoAi` has **2** call sites (`WildHunterAura:92` / `:119`), while `BRIEF` §1.5 and the basis of D-179 in document 40 write "3 sites (`:91` / `:92` / `:119`)", counting `isNoAi()` (`:91`) in; ② the original AI flag is **not an in-memory table** — author's ruling #15 fixes it as **persisted** (document 42 §2.3.1's `AttachmentType<Suspend>`), and "the table is an **in-memory table**: not persisted, not in `getUpdateTag`" in document 40 D-179's body and in §0.2's glossary has been superseded by author's ruling #15.
- **Basis**: the source `WildHunterAura:87-95` / `:98-123` (`:91` is `isNoAi()`, `:92` and `:119` are the `setNoAi`s; measured grep hits 2); `BRIEF` §2 author's ruling #15 (persisted + deleted after use); the four-candidate comparison of document 42 §2.3.1 (the pure in-memory table was rejected by "a crash breaks it").
- **Landing place**: **F-252 / F-253** (implemented as persisted), **F-264**'s `#2` / `#23` / `#28`; back-fill targets: D-179 of document 40 and the §0.2 glossary term "原值".

---

## 6. Coverage Mapping Tables (six)

Tables A ~ F correspond to document 42 §3 / §1.2 / §4 / §4.3 / §6 / §8. The last row of each table gives "covered / total".

### Table A · The 31 action-surface rows of document 42 §3 → tasks

| Row | Category | Path or class#method | Now → after | Responsible tasks |
|---|---|---|---|---|
| 1 | release | `gradle.properties:11` | `0.2.0` → **`0.2.1`** | F-247 |
| 2 | release | `一键构建.cmd:5` | the banner `Farmstorm v0.2.0` → `v0.2.1` | F-247 |
| 3 | release | `一键构建.cmd:11,12` | the jar / sources-jar names → `0.2.1` (two lines) | F-247 |
| 4 | release | `检查环境.cmd:5` | the banner `Farmstorm v0.2.0` → `v0.2.1` | F-247 |
| 5 | release | `检查环境.cmd:14` | `farmstorm-0.2.0+mc1.21.1.jar` → `0.2.1` | F-247 |
| 6 | class | `OutpostCoreBlockEntity#serverTick` (`:507-525`) | three things → **four things** (the rebind gate before the particle gate, no return) | F-250 |
| 7 | class | `OutpostCoreBlockEntity`'s constants (next to `:91`) | none → **`REBIND_INTERVAL_TICKS = 100`** | F-249 |
| 8 | class | `OutpostCoreBlockEntity#rebindDue(BlockPos, long)` | does not exist → **new** (its own phase with modulus 100) | F-249 |
| 9 | class | `OutpostCoreBlockEntity#rebindNearby(ServerLevel)` | does not exist → **new** (two phases: collect → write the binding; `break` at the `cap`) | F-249 |
| 10 | class | `util/AreaBlockEntities.java` | does not exist → **new** (`collect(…, Class<T>)`, going only through `getChunkNow`) | F-248 |
| 11 | comment | `block/CultivatorBlock.java:73-84` (the sentence is at `:81-83`) | "不自动认领……不会绑上" → **consistent with the periodic rebind** | F-250 |
| 12 | class | `client/OutpostCoreScreen.java` (42 lines) | overrides `renderBg` only → **not one line changed** (the criterion is in document 42 §2.2) | F-256 |
| 13 | acceptance | document 42 §7 | tooltip / hover **0** entries → **+3 entries** (`#11` ~ `#13`) | F-256 (re-run in F-264) |
| 14 | class | `VibrationBeanTrap#trigger` (`:74-105`) | gives `CONFUSION` to the players in the sphere only → **append** suspending the AI of every non-player mob in the sphere for 80 ticks | F-254 |
| 15 | class | `VibrationBeanTrap`'s constants | none → **`AI_SUSPEND_TICKS = 80`** (no new config key) | F-254 |
| 16 | class | `effect/QbounceEffect.java` (20 lines, 0 overrides) | a pure state carrier → **override one method, `onEffectAdded`** (no `applyEffectTick` override) | F-255 |
| 17 | class | `behavior/QbounceMountController#isMountMode` (`:199`) | `private static` → **`public static`** (no second method added) | F-251 |
| 18 | class | `combat/MobAiSuspension.java` | does not exist → **new** (the `sources` bit mask + the observation index + the in-memory deadline tick + the five cleanup paths + per-tick reconciliation) | F-253 (`Suspend` is in F-252) |
| 19 | class | `registry/FarmstormAttachments.java` | does not exist → **new** (`AttachmentType<Suspend>`, `farmstorm:ai_suspend`, registered in `ATTACHMENT_TYPES`) | F-252 |
| 20 | class | `util/Hostility.java` | does not exist → **new** (`isHostile(Entity)`, `Enemy` ∪ `MONSTER`) | F-257 |
| 21 | class | `explosion/FarmstormExplosion`'s constructor (`:69-78`) | 8 parameters → **9 parameters** (+`boolean trapSourced`) + **the 8-parameter overload kept, delegating `false`** | F-259 |
| 22 | class | `FarmstormExplosion#hurtCapped` (`:178-179`) | `hurt(src, min(raw, cap))` → × the multiplier when hostile ∧ `trapSourced` | F-259 |
| 23 | class | `ZangiefCaneTrap#monsterBranch` (`:111-118`, log `:126-128`) | `hurt(src, amount)`, the log prints the original → × the multiplier when hostile, **the log prints the multiplied value** | F-260 |
| 24 | config | `config/FarmstormConfig.java` (the `trap` section `:917-927`) | 3 keys / **175** → **4 keys / 176** (+`damageMultiplier` 3.0, 1.0 ~ 10.0) | F-258 (the ledger is F-262) |
| 25 | language | `datagen/ModLangProvider.java` (after `:412-413`) | 178 entries / **337** → **+1 entry / 338** | F-258 (the ledger is F-263) |
| 26 | tool | `tools/verify_project.py` (`main()` `:349-402`) | no document-42 parsing → **add `expected_config_keys_v021`** + an expectation of 176 | F-261 |
| 27 | do not change | `Farmstorm.java:167-172` (the 12-class assertion) / `FarmstormNetwork` (2 packets, protocol `"2"`) | as is | F-266 (negative check) |
| 28 | do not change | `FangTrapCactusTrap:87-109` (the vanilla `EvokerFangs`) / `vibration_bean` (0 damage) / the 5 `Monster` filters | as is | F-266 (negative check) |
| 29 | class | `Farmstorm.java`'s constructor (the region of the **11** `.register(modBus)` calls, `:45-58`) | 11 registers → **+1 line** (`FarmstormAttachments`); `:169-172` untouched | F-252 |
| 30 | class | `#rebindNearby`'s precondition gate and ordering | does not exist → the **ownerless-core gate** (author's ruling #13) + a **three-level ordering** `distSqr` → `x` → `y` → `z` (author's ruling #14) | F-249 |
| 31 | data | the Vibration Bean's 80-tick deadline tick | none → **memory only** (`VB_DEADLINE`), **not persisted** (author's ruling #16) | F-253 |

**31/31 covered, 0 uncovered**. Category counts: release **5** rows (#1 ~ #5), class **18** rows (#6 ~ #10, #12, #14 ~ #23, #29, #30), comment **1** row (#11), acceptance **1** row (#13), data **1** row (#31), config **1** row (#24), language **1** row (#25), tool **1** row (#26), do-not-change **2** rows (#27, #28) — identical item by item to the category counts below the table of document 42 §3 (release 5 / class 18 / comment 1 / acceptance 1 / data 1 / config 1 / language 1 / tool 1 / do-not-change 2 = 31).

**Note below the table**: **2 off-table actions** (**not counted into the 31 rows**) — ① the sync of `tools/verify_project.py`'s **expected number** (F-261's `union` and 175 → 176; document 42 §3 #26 registered only "add the parser", the expected number being in the same row); ② the two ledgers (F-262 / F-263, document 42 §4.2 / §4.3). Both have their own acceptance and do not stretch table A to 33 rows. **The only rows spanning several tasks** are #18 (`MobAiSuspension`'s data class is in F-252, its behaviour in F-253) and #24 / #25 (the key and the translation in one commit, the ledgers independent) — every segment has its own acceptance, so reverting any one of those tasks does not affect the other segments.

### Table B · Registry surface **97 (+0)** + the 13th category outside the count → tasks

**B.1 The 12 classes one by one** (baseline 97, named from the source by document 42 §1.2 itself)

| # | registry | Baseline | This version's increment | After | Landing task | Checking task |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 33 | **+0** | **33** | — | F-266 |
| 2 | `Registries.BLOCK` | 27 | **+0** | **27** | — | F-266 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 8 | **+0** | **8** | — | F-266 |
| 4 | `Registries.ENTITY_TYPE` | 13 | **+0** | **13** | — | F-266 |
| 5 | `Registries.MOB_EFFECT` | 4 | **+0** | **4** | — | F-266 |
| 6 | `Registries.SOUND_EVENT` | 3 | **+0** | **3** | — | F-266 |
| 7 | `Registries.RECIPE_TYPE` | 1 | **+0** | **1** | — | F-266 |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | **+0** | **1** | — | F-266 |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | **+0** | **1** | — | F-266 |
| 10 | `Registries.DAMAGE_TYPE` | 3 | **+0** (no new `trap_blast`; rejected by document 42 §2.5.2) | **3** | — | F-266 |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | **+0** (the original AI flag goes through an attachment, not a data component, D-175 / author's ruling #17) | **1** | — | F-266 |
| 12 | `Registries.MENU` | 2 | **+0** | **2** | — | F-266 |
| **Total** | | **97** | **+0** | **97** | | `33+27+8+13+4+3+1+1+1+3+1+2 = 97` |

**B.2 The 13th category outside the count (1 row, not counted into 97)**

| # | registry | Baseline | This version's increment | After | Landing task | Checking task |
|---|---|---|---|---|---|---|
| 13 | `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` | 0 | **+1** (`farmstorm:ai_suspend`) | **1** (**outside the count**, not counted into 97) | F-252 | F-266 (manual registration; `verify_project.py` **checks neither its existence nor its count**, document 42 §1.2.1) |

**B.3 The four components "added but not entering the registry surface"**

| # | Component | Why it does not enter the registry | Task |
|---|---|---|---|
| 1 | `util/AreaBlockEntities` | a purely static utility class, 0 registration calls | F-248 |
| 2 | `util/Hostility` | a purely static predicate, 0 registration calls | F-257 |
| 3 | `combat/MobAiSuspension` + `Suspend` | holds only two in-memory containers + the event subscription; `Suspend` is the attachment's value type; neither is registered | F-252, F-253 |
| 4 | `trap.damageMultiplier` | a config key, not registered (D-183) | F-258 |

**Coverage: 12 classes + 1 class outside the count + 4 components that do not enter the registry surface = 17 items, 0 uncovered** (the total row's 97 does not move).

### Table C · Config **176** (+1) → tasks

| # | Key | Type | Default | Range | Action | Key-landing task | Language-key task |
|---|---|---|---|---|---|---|---|
| 1 | `trap.damageMultiplier` | double | **3.0** | **1.0–10.0** | **add** | F-258 | F-258 |
| **Total** | **1 row = 1 added / 0 deleted / 0 changed** | | | | | **`175 + 1 = 176`** | 1 language key |

**Two checks**: ① the `trap` group goes **3 → 4** keys and the group count stays **60** (`b.push` / `b.pop` 60 times each; no new group); ② the intermediate key counts (each segment is countable from `run/config/farmstorm-server.toml`) — before F-257 **175 / 60** → after F-258 **176 / 60** (the interval wording "175 or 176" is allowed; the total is asserted only in F-262); the 3 CLIENT keys (`hud.enabled` / `hud.x` / `hud.y`) **do not move and are not counted into** 176, but they do count into the language keys' `farmstorm.configuration.*` (document 42 §4.2 / §4.3).

**Three things that do not change** (document 42 §4.1.1): `blast_carrot.maxDamage` (7), `zangief_cane.slamDamage` (7.5), the defaults of the `trap` section's existing 3 keys — the multiplier is a **new key** multiplied at settlement (author's ruling #10).

### Table D · Language keys **338** (+1) → tasks

| # | Key | Language | Action | Landing task |
|---|---|---|---|---|
| 1 | `farmstorm.configuration.trap.damageMultiplier` | zh_cn | **add** | F-258 |
| 2 | `farmstorm.configuration.trap.damageMultiplier` | en_us | **add** | F-258 |
| **Total** | **1 key × 2 languages** | | **`337 + 1 = 338` (× 2)** | the ledger is in F-263 |

**Prefix-by-prefix check** (after the change, document 42 §4.3's basis): `farmstorm.configuration 178 → 179` (= 176 server + 3 client), `tooltip.farmstorm` still **20**, and the other 13 buckets unchanged; `179 + 36 + 33 + 27 + 20 + 13 + 9 + 6 + 4 + 3 + 3 + 0 + 2 + 1 + 2 = 338`. **Chinese and English with the same keys and the same count** (zh-only 0 / en-only 0); **the language keys have no automatic gate**, so they can only be counted by hand with `ConvertFrom-Json` (document 42 §9 item 4).

### Table E · Risks, **11 entries** (RT-51 ~ RT-60 + RT-60b) → tasks

The numbering follows document 42 §6: **RT-51 ~ RT-60, ten contiguous**, and the last row is **RT-60's sub-entry `RT-60b`** (the same mechanism's second entry, document 42 §9 item 5: no RT-61 is opened).

| ID | Risk (document 42 §6) | Task responsible for verification | How that task verifies it |
|---|---|---|---|
| **RT-51** | the periodic rebind coexisting with binding on placement ⇒ the same cultivator may be claimed by two cores at once | F-249, F-250, F-264 | F-249 checks the write order of "core first, cultivator second" + no `removeCultivator` call; F-250 checks the comment changed in the same commit + the 100-tick reading; F-264 runs `#6` ~ `#10` |
| **RT-52** | region enumeration without precedent: the return semantics of `getBlockEntities()`, and unloaded chunks must be skipped | F-248, F-264 | F-248 checks that it goes only through `getChunkNow` (0 hits for the three load-triggering APIs) + that it returns a new list; F-264's `#9` checks that an unloaded chunk is skipped and that the log holds no load line for that chunk |
| **RT-53** | one core's cap of 16: it must stop when full; `pruneCultivators` running at the same time as the rebind would "strip a binding just written" | F-249, F-264 | F-249 checks the `break` at the `cap` + that the capacity reading does not go through `cultivators()`; F-264's `#10` checks that of 20 candidates only 16 are bound and that the count stops growing |
| **RT-54** | the **residue risk once the original AI flag is persisted**: missing one of the five cleanup paths leaves a stale record | F-252, F-253, F-264 | F-252 checks that `sources == 0` is not persisted + that there is no timestamp field; F-253 checks that `release` must end with `removeData`; F-264's `#23` / `#24` / `#28` |
| **RT-55** | Q-Bounce's effect lifetime: all five paths — expiry / `/effect clear` / milk / death / unload — must clean up | F-253, F-255, F-264 | F-253 checks that the reconciliation is the only authority (0 effect-event subscriptions); F-255 checks that the three removal paths restore within **1 tick**; F-264's `#17` / `#18` |
| **RT-56** | Q-Bounce's effect is "applied repeatedly with a decreasing duration", so the lifetime cannot be driven by a one-shot timer | F-255, F-264 | F-255 checks that the entry face hangs only `onEffectAdded` (called only on the "no such effect before" branch) + that the `sources` bit is not set twice; F-264's `#16` |
| **RT-57** | ×3 applies to hostile mobs only: it must not creep onto other paths (especially the 5 trigger faces) | F-257, F-259, F-260, F-264 | F-257 checks that the 5 `Monster` filters are unchanged character for character; F-259 / F-260 check that there are only 2 multiplication points; F-264's `#19` / `#20` / `#27` |
| **RT-58** | the fangs do not take part: writing the acceptance as "all traps" necessarily misjudges | F-259, F-260, F-264 | neither multiplication point touches `EvokerFangs`; F-264's `#21` checks that the fangs are unchanged point by point, and the record states "2 of the 4 take part" |
| **RT-59** | the tooltip may already be fine: the task then becomes "add the acceptance", and it is easy to write "a pile of changes" | F-256, F-264 | F-256 checks the 4 overrides = 0 + all 90 slots vanilla + the fallback ≤ 2 lines; F-264's `#11` ~ `#13` |
| **RT-60** | `verify_project.py`'s expectation face (A-66): not syncing 175 → 176 **silently checks less** | F-261, F-262, F-266 | F-261 checks that the parser returns 1 key + the expectation of 176; F-262 checks both directions (source 176 / toml 176 / the tool's union 176); F-266's sign-off cites the tool's three-section output |
| **RT-60b** | `trap.damageMultiplier` must be **really written into the document** (the expected key set is extracted from the document) | F-261, F-264 | F-261 checks `grep -c` ≥ 2 and that the regex hits 1 row of §4.1.1; F-264's `#22` |
| **Total** | **11 entries** | | **11/11, 0 uncovered** |

### Table F · APIs to verify, **10 entries** → tasks

| # | API / semantics to verify (document 42 §8) | Confirmed by which task at which step | Fallback when it cannot be confirmed (already given by document 42 §8) |
|---|---|---|---|
| ① | the return semantics of `LevelChunk#getBlockEntities()` and the unloaded test | **F-248**: after `collect` lands, run F-250's `#6` ~ `#10`; whether a CME appears is covered by the two readings `#9` (unloaded chunks skipped) and `#10` (20 candidates) | if a CME really appears ⇒ in `collect`, explicitly do `new ArrayList<>(chunk.getBlockEntities().values())` before filtering, and add a `try/catch` layer |
| ② | `Mob#setNoAi` and how to read "the original" back (the difference between `NoAI:0b` and "the key is missing") | **F-253**'s `#1` / `#2`: the three readings of `/data get entity <mob> NoAI` (key missing / `0b` / `1b`) | change the reading to "the key exists and is 1b" = suspended, "no key" = has AI (consistent with `Mob.java:437`, which writes only when true) |
| ③ | the availability of `MobEffect#onEffectAdded` / `onEffectRemoved` in 21.1.213 | **F-255**'s `#1`: first apply the effect once to one mob and see whether the attachment is written once only; "no `onEffectRemoved`" has already been checked in document 42 §8 ③ | if `onEffectAdded` does not fire on some path ⇒ additionally hang `MobEffectEvent.Added` (filtered by `getOldEffectInstance() == null`); the `sources` bit being idempotent means nothing is duplicated |
| ④ | the `Enemy` interface and how to obtain `MobCategory.MONSTER` | **F-257**'s 4 source branches + **F-259**'s `#3` / `#4` runtime readings | if measurement shows some modded mob is missed ⇒ **register it only** (document 40 §7 ③ has fixed "do not widen the criterion"); add no whitelist |
| ⑤ | the concrete function in `verify_project.py` that extracts the key set from the document | **F-261**'s `#1` ~ `#3`: the new parser returns 1 key and both lines read 176 | if the table format is broken ⇒ the parser **throws with the file name + line number** in the shape of `:172-178` (better red) |
| ⑥ | the trigger face of `EntityJoinLevelEvent` (settlement on load) | **F-253**'s `#4`: hard-kill the process, restart, and see whether the attachment is settled and deleted | a **bounded rescan** at server start: following `QbounceMountController:63-70` / `:190-194`'s `ADOPT_RESCAN_INTERVAL_TICKS = 20` / `MAX_ADOPT_RESCANS = 10`, do 10 rounds (a chunk that loads only after 10 seconds is a known residue, registered as RT-54) |
| ⑦ | the ordering of `EventPriority.HIGHEST` on `ServerTickEvent.Post` | before **F-253**'s `#5` + **F-255**'s `#3`: in mount mode the restore must precede the mounting | hang `ServerTickEvent.Pre` instead (an event phase naturally earlier than every `Post` subscriber), at the cost of losing 1 tick of displacement |
| ⑧ | the persist / read-back timing and the size of `AttachmentType` | **F-252**'s `#1` / `#4` + **F-253**'s `#1` / `#3` | if `shouldSerialize` still writes an empty compound ⇒ change "zero bytes in the save" to "≤ 40 bytes"; the conclusion does not change |
| ⑨ | the interaction between the `ArmorStand` mount and `noAi` (Vibration Bean × mount on the same tick) | **F-254**'s `#1` and **F-255**'s `#3` are **tested separately, never merged** (document 42 §2.3.7) | if measurement shows "the mount is stripped / the entity is thrown off", register it per RT-55 and **do not change** author's ruling #4's effective surface |
| ⑩ | the default-value semantics of `getData` / `setData` / `removeData` / `hasData` | **F-253**'s `#1` (after `removeData`, `getData` falls back to `NONE`) + `#5` (the existence criterion uses `sources != 0`) | if `getData` writes the default value into the map ⇒ it does not affect this version (the criterion is `sources != 0`); if it does not fall back after `removeData` ⇒ add a `sources == 0 ? NONE : 已存值` fallback layer to the default-value provider |
| **Total** | **10 entries** | **10/10 land on a concrete task and a concrete step** | **10/10 give an executable fallback** |

---

## 7. Coverage Self-Check (this document's own acceptance)

| # | Check item | Criterion | Result |
|---|---|---|---|
| 1 | Task numbers contiguous, no gap | `grep -c "^#### F-"` = **20**; the range `F-247 ~ F-266` ⇒ `266 − 247 + 1 = 20` | pass |
| 2 | Phase and milestone contiguous | Phase `40 ~ 45`, **6** in all; milestones `M36 ~ M41`, **6** in all, one-to-one (§4.1's table) | pass |
| 3 | Adjudication numbers | A-70 ~ A-81, **12** in all, contiguous with no gap; A-70 is the first and sits at §5.1 | pass |
| 4 | Table A coverage | the **31 rows** of document 42 §3, not one missing, **0** uncovered; the category counts (release 5 / class 18 / comment 1 / acceptance 1 / data 1 / config 1 / language 1 / tool 1 / do-not-change 2) identical item by item to the counts below document 42 §3's table | **31/31** |
| 5 | Table B coverage | the 12 classes one by one (**97 = 97 + 0**, the total row's arithmetic `33+27+8+13+4+3+1+1+1+3+1+2 = 97`); **1 row of the 13th category outside the count** (`ATTACHMENT_TYPES` +1, not counted into 97, not checked by the tool); the 4 components that do not enter the registry surface each have a task | **17 items** (12 classes + 1 outside the count + 4 components) |
| 6 | Tables C and D coverage | table C **1 row** (1 added / 0 deleted / 0 changed, `175 + 1 = 176`, the group count `60` unchanged); table D **2 rows** (zh_cn / en_us one each, `337 + 1 = 338` × 2, the prefix-by-prefix sum = 338); both tables' intermediate states and window disciplines are self-consistent (§0.3) | **1 row + 2 rows** |
| 7 | Table E coverage | **11 entries**: RT-51 ~ RT-60, ten, + **RT-60b**, one sub-entry (the last row); every entry has at least one task responsible for verification and is given a "how to verify" | **11/11** |
| 8 | Table F coverage | document 42 §8's **10** entries, each landing on "a concrete step of a concrete task" + "a concrete fallback", with no "watch out during implementation" | **10/10** |
| 9 | Every task has ≥ 1 observable acceptance item | read the 20 tasks one by one: all have assertions falling on `latest.log` / `farmstorm-server.toml` / `lang/*.json` / `/data get` / a source grep count / in-game hovering; none says "behaves normally" or "feels good" | pass |
| 10 | Every task has a dependency, and the dependencies have no cycle | §1's "Depends on" column + the tasks' title lines' `Depends on`, 20 / 20 consistent; of which **7 have no predecessor (`—`)**: F-247, F-248, F-251, F-252, F-256, F-257, F-258; every dependency number is smaller than its own, ordering by Phase is a topological order, with no back edge | pass |
| 11 | Workload self-consistent | `7 × 0.5 + 10 × 1.5 + 3 × 3.0 = 27.5`; the six Phase subtotals sum to 27.5; `27.5 × 1.25 = 34.375`; `S + M + L = 7 + 10 + 3 = 20` | pass |
| 12 | In-between window self-consistent | window = **F-258 (opens) → F-263 (closes)**, the config side closing at **F-262**, the language side at **F-263**, the tool's expectation face at **F-261**; the acceptances of the five tasks F-258 ~ F-262 **all exclude** "176 / 60" and "338 × 2"; each of the two totals is asserted exactly once, in its closing task | pass |
| 13 | The two "written from scratch" components are each a task of their own | `util/AreaBlockEntities#collect` (F-248) and `util/Hostility#isHostile` (F-257) are both independent tasks with separately executable acceptances (the former = compiling + the four structural readings; the latter = 4 branches + the 5 filters unchanged + `MobCategory` 2 → 3); their runtime readings are taken up by F-250 / F-259 respectively | pass |
| 14 | The acceptance core of the 9th parameter and the 8-parameter overload | F-259's acceptance 1 (7 call sites, only 1 passing the 9th argument) and acceptance 4 (the 6 sites reading the same at a multiplier of 1.0 / 10.0), plus F-264's `#20`, together prove "the 6 non-trap call sites are untouched and were not ×3'd" | pass |
| 15 | The 13th category's three registrations | F-252's acceptance 5 (the tool does not check it) + F-266's sign-off 3 + document 42 §1.2.1; all three state "not counted into 97" | pass |
| 16 | "No invention" | 97 / 176 / 338 / 31 rows / 31 entries / 10 entries / 11 entries all trace back to document 42 §1.2, §3, §4, §6, §7, §8 or their arithmetic (`175 + 1`, `337 + 1`, `97 + 0`, `145 + 4`, `7.5 × 3`); what is added is only the 12 adjudications and the task numbers, with no new gameplay value | pass |
| 17 | Forbidden words, tone and change surface | no undecided-item marker and no placeholder anywhere; the person is always "the author" / "the implementer", never a second-person honorific; conclusion sentences are always imperative; beyond this document no file changes (`docs/zh/40` ~ `42`, `docs/en`, `src/`, `tools/`, `.work/` unchanged by a character) | pass |
