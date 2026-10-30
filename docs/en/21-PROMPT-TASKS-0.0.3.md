# 21 · v0.0.3 Phased Task List (Task Prompt)

| Item | Value |
|---|---|
| Document ID | 21-PROMPT-TASKS |
| Version | v0.0.3 |
| Audience | The person feeding the coding agent + the CLI coding agent being fed (Claude Code / Codex class) |
| Upstream | `16-DECISIONS-0.0.3.md` (D-80 ~ D-102, 23 entries, the single source of truth), `17-DESIGN-0.0.3.md`, `18-TECH-0.0.3.md`, `19-TASKS-0.0.3.md` (the single source for F-100 ~ F-135) |
| Peer | `20-PROMPT-MAIN-0.0.3.md` (the resident main prompt; this file does **not** repeat its hard constraints or its definition of done) |
| Loading method | Feed once per Phase: the full text of `20-PROMPT-MAIN-0.0.3.md` + the matching section of this file |
| Contents | 5 Phases / 36 tasks (F-100 ~ F-135); task IDs, titles, complexity, person-days and dependencies align verbatim with `19-TASKS-0.0.3.md` |
| Boundaries | Upstream is read-only: do not change any of 00 – 20 under `docs/zh`, do not change `docs/en/`, do not change `src/` (D-80) |

---

## 0. How to Use This File

### 0.1 Division of Labor Between This File and 20-PROMPT-MAIN-0.0.3.md

| File | What it governs | Feed frequency |
|---|---|---|
| `20-PROMPT-MAIN-0.0.3.md` | Global discipline and hard constraints (the engineering red lines, code conventions, execution discipline, definition of done, report format, prohibited actions, known traps; numbering starts at HC-29 and N-29) | Resident in every phase |
| `21-PROMPT-TASKS-0.0.3.md` (this file) | Which tasks to do this round, what counts as passing, and how to open the next phase after delivery | One section at a time |

Three rules:

1. This file does not rewrite the hard constraints; it cites their IDs only. When you see `D-xx` (a decision) go back to `16`, `RT-xx` (a risk) and `§11 #n` (an acceptance self-check) go back to `18`, `§12.x` (exceptions and boundaries) and the numeric tables go back to `17`, `A-10 ~ A-18` (adjudications) and `F-xxx` (tasks) go back to `19`, and HC-29 onwards and N-29 onwards go back to `20` — do not interpret from memory.
2. Adjudication order on conflict: `16-DECISIONS-0.0.3.md` (the single source of truth for v0.0.3) > `17` / `18` > `19` > this file. `19` §0.4 has already settled this round's disagreements of reading (A-10 ~ A-18); this file is written per those adjudications and does not reopen the discussion.
3. This file's acceptance criteria only tighten within the definition of done of `20`; they invent no new deliverables. Not one task, ID or numeric value that is absent from `19` is added here.

### 0.2 How to Feed It

1. Open this file, find the section for the current Phase (the heading looks like `## 2. Phase 16 · Baseline and Tool Fixes (F-100 ~ F-105)`), and copy the whole section starting from the heading.
2. Feed it to the coding agent together with the **full text** of `20-PROMPT-MAIN-0.0.3.md` — **one Phase at a time**.
3. Wait for it to hand back a report per `20`'s report format, then accept item by item against this section's §x.4 exit criteria.
4. Once acceptance passes, open the next phase with the hand-off wording in §7.

Three prohibitions:

- Do not feed the §1 overview table along with it — the agent will try to finish all five phases at once.
- Do not skip Phase 16's F-100 / F-101 and go straight to F-103: 0.0.2's **77** is a measured value that can only be recomputed once the tool is fixed, and skipping it puts you back on the old road of "announce 90 from +13" (RT-23 risk ④; A-14 forbids downgrading to a manual signature).
- Do not force these three places into numeric order (`19` §1.2's supplementary prerequisites): inside Phase 19, `F-123` before `F-122` (`F-122` depends on `F-105` and `F-123`); `F-126` has to wait for Phase 16's `F-104` (the two new `fang_trap_cactus` keys); `F-132` has to wait for `F-128` (the `damage_type` data json lands in the same batch).

### 0.3 How to Judge That a Phase Is Complete

All three gates must be green to pass; missing one is handled as "not done":

1. **Exit criteria**: every item of §x.4 can be pointed to an assertion output in `logs/latest.log`, the return value of `/execute` / `/data get` / `/damage` / `/advancement` / `/playsound` in game, an F3 / F3+B on-screen reading, the content of a file under `src/generated/resources` or of `run/config/farmstorm-server.toml`, or the artifact in `build/libs` / `dist`. Structural evidence (0 `grep` hits, an empty `git diff`, `ffprobe`'s encoding output, `jar tf`'s entry list) is used only where `19` §0.3 item 4 allows it. Saying "implemented but not verified" does not pass.
2. **Task coverage**: every F-xx in §x.3 has identifyable evidence; when the agent skipped one, require it to be made up per `20`'s definition of done — do not fill it in yourself.
3. **Repository state**: `.tools\gw.cmd build` exits 0; `git status` shows only files inside this phase's boundary; 00 – 20 under `docs/zh` and all of `docs/en/`, the fields of the existing 19 `CropProfile` entries, the defaults of the existing 139 config keys (except the 3 that F-104 authorises), the existing 15 advancements and the existing 21 converting recipes are untouched (D-80; `16` §5.7).

Order of handling when a phase fails: first have it self-check per `20`'s self-check section → if that still fails, copy the failing acceptance items back into the task prompt and dispatch once more → only after two failures, redo it in a new session, naming in the hand-off which item it is stuck on.

### 0.4 The Nine Latest Adjudications (A-10 ~ A-18)

The nine below are the adjudication record of `19` §0.4, numbered on from 0.0.2's A-09. **The three upstream disagreements that once existed in A-15 / A-16 / A-17 have been corrected upstream per these adjudications**: the "old wording" column is kept here for traceability and does not mean upstream still writes it that way. A-11 and A-18 are rulings on reading; follow the "correct wording".

| # | Adjudication (what is adopted) | Old wording (judged wrong) | Correct wording | Tasks that land it |
|---|---|---|---|---|
| **A-10** | The Fang Trap Cactus rename touches only the Chinese display name | "rename it to 刺球" was once read as changing the registry id, the block id, the `DeployedKind` constant and the English name along with it | **Only the value in `zh_cn.json` changes**: 「赢过输过没死过」→「刺球」; `fang_trap_cactus`, `fang_trap_cactus_block`, `FANG_TRAP_CACTUS` and `Fang Trap Cactus` change by not one character. This is C-10's first and only explicit exception, and no second one may be opened elsewhere | F-125 |
| **A-11** | What the Vibration Bean profile's `DeployedKind` is filled with | The column was once read as `null` following the "—" in row 22 of `16` §4; there was also a reading that counted the two new enum values as +1 | Fill `DeployedKind.VIBRATION_BEAN`; the enum goes 6 → **8** (`POINT_DEFENSE_FUNGUS`, `VIBRATION_BEAN`). Enum constants are not registry objects, so the registry total is unaffected; filling `null` falls back to `BLAST_CARROT` and makes the trap run as an explosion trap | F-103, F-105 |
| **A-12** | What carries the mount in Q-Bounce's `mount` branch | A new `entity_types` registry object was once planned for the mount (with the increment written as +3) | Use the vanilla `minecraft:armor_stand`, keeping the `entity_types` increment **at +2**; five property overrides `setInvisible` / `setNoGravity` / `setInvulnerable` / `setSilent` / `setMarker`, with an immediate `discard()` when the creature dies | F-109, F-112 |
| **A-13** | Whether the interception sound and the Vibration Bean explosion sound get new `sound_event`s | One `sound_event` was once planned for each of the interception and explosion sounds (with `sound_events` written as +3 and `sounds.json`'s event count as 5) | Both reuse vanilla events: `SoundEvents.SHIELD_BLOCK` and `SoundEvents.GENERIC_EXPLODE`, written in code as Java constants only, with no `minecraft:***` string; the `sound_events` increment is **+1** (only `friend_chives`), and `sounds.json`'s event count goes 2 → 3 | F-120, F-124, F-131 |
| **A-14** | Whether acceptance may be downgraded while the tool lags | A fallback once said "keep the tool red + a manual signature" | **No downgrade**: first fix it to the 0.0.2 baseline **77 / 77** (config 139, lang 263, `结论：FAIL 0 项`, exit code 0), and after v0.0.3 lands tighten it to **90 / 90** (config 155, lang 297). RT-23's fallback line "keep it red + a manual signature" applies only if the author explicitly rejects fixing the tool, and does not apply this round | F-100, F-135 |
| **A-15** | The defaults of `qbounce`'s three pose parameters | Two sets of readings once coexisted: 24.0 / 0.15 / 6 and 12.0 / 0.15 / 10 (240 degrees/second) | Take **24.0 degrees/tick, 0.15, 6 ticks** (= 480 degrees/second, one turn per 15 ticks, about 3.3 squash cycles per second); the range column takes `18` §8.1 as its only source (0.0–360.0 / 0.0–0.90 / 2–100). Corrected upstream | F-104, F-107, F-108 |
| **A-16** | The PNG increment | A reading of 7 once appeared (`16` §8 and `17` §1.3's "+7 / +1") | Take **8**: the 8th is Vibration Bean's item icon (`basicItem`'s fallback branch always references `farmstorm:item/<id>`, the same treatment as 0.0.2's three invisible traps); the resource increment is 8 PNG + 1 OGG + 22 JSON = **31**. Corrected upstream | F-130, F-135 |
| **A-17** | The IDs and file names of the three new advancements | Two readings once appeared (`first_intercept` / `first_vibration_bean`) | Take `farmstorm:first_q_bounce`, `farmstorm:point_defense_intercept`, `farmstorm:vibration_bean_trigger`, with the JSON file names equal to the IDs. Corrected upstream | F-133 |
| **A-18** | How §9's sub-table 3 (31 resource files) and sub-table 4 (9 data entries) are counted | 31 and 9 were once added together as a single total | The two tables use different conventions and **are not added**: sub-table 3 counts **files** (PNG 8 + OGG 1 + JSON 22 = 31), sub-table 4 counts **data entries** (2 recipes + 3 advancements + 1 item tag + 2 block-tag appends + 1 damage_type = 9), 6 of which already appear in sub-table 3 as files; the two "0 uncovered" claims each hold on their own | F-132, F-133 |

Of the nine, only A-14 affects "when a phase may be declared complete"; the other eight decide only the concrete readings of a few acceptance points. Not one of the nine changes the 36 tasks, the 60.0 person-days or the critical path of 18.0 person-days (the last paragraph of `19` §12.3).

### 0.5 Number Quick Reference

| # | Quantity | Value | Source |
|---|---|---|---|
| 1 | Registry objects (v0.0.2 baseline) | **77** (12 categories: 27 / 22 / 6 / 11 / 2 / 2 / 1 / 1 / 1 / 2 / 1 / 1) | `18` §1.2 (per-category measurement), the v0.0.2 column of §2.1, §11 #1; the numeric-discipline paragraph of `16` |
| 2 | Registry objects (v0.0.3) | **90** (77 + 13) | `18` §2.1 / §2.4 / §11 #2; `19` F-103, F-135 |
| 3 | config keys (v0.0.2) | **139** (133 `defineInRange` + 4 `define` + 2 `defineEnum` / `defineList`) | `18` §0.4 / §8.3; `16` §8 |
| 4 | config keys (v0.0.3) | **155** (139 + 16) | `18` §8.3; `16` §8 |
| 5 | Language keys (v0.0.2) | **263** (263 each in Chinese and English, identical key sets) | `18` §0.4 / §9.4; `16` §8 |
| 6 | Language keys (v0.0.3) | **297** (263 + 34) | the seven-row subtotal of `18` §9.4 |
| 7 | config changes | **19 rows** = **16** purely new + **3** changes to existing defaults; another 1 row, the note "«`confusion` has no key»", is not counted | `18` §8.1 (the 19-row item-by-item inventory), §8.3; the "19 / 16 / 3 / 1" row in `16` §5.4 / §8 |
| 8 | Resource files | **31** = PNG **8** + OGG **1** + JSON **22** | `18` §9.1 (8 rows) / §9.2 (1 row) / §9.3 (22 rows); A-16, A-18 |
| 9 | `ThrownCropEntity` family | **12** (0.0.2's 10 + 0.0.3's 2) | `16` §3.6 D-98; the 12-row table of `18` §6.1 |
| 10 | `DeployedKind` enum | **6 → 8** | `18` §1.4(b); the note under `16` §4 (rows 21, 22); A-11 |
| 11 | Decisions | **23** entries (D-80 ~ D-102, consecutive with no gaps) | the decision-number-range row in `16` §1 |
| 12 | Risks | **7** entries (RT-17 ~ RT-23) | `18` §10 (7 sections, five fields each) |
| 13 | Tasks | **36** / **5** Phases (6 / 8 / 8 / 8 / 6) | `19` §2 / §12.2 |
| 14 | Person-days (bare) | **60.0** (S 6 × 0.5 + M 22 × 1.5 + L 8 × 3.0 = 3.0 + 33.0 + 24.0) | `19` §0.2 / §2 / §10 |
| 15 | Person-days (with the 25 % buffer) | **75.0** (60.0 × 1.25) | the milestone table in `19` §2 / §10 |

Two reminders on how to read the numbers:

- **77 and 90 are "two measurements + one addition"**, not guesses: 77 comes from the per-category measurement in `18` §1.2 and equals the 12 runtime assertions of `Farmstorm#logRegistrySelfCheck` item by item, and 90 = 77 + 13. Any total written down before the tool is fixed (F-100) does not hold (`18` §0.4; RT-23 risk ④).
- **This release adds only three small accounts**: registry +13, config +16, language +34; resources are counted separately by file, 8 + 1 + 22 = 31. 0.0.2's 77 / 139 / 263 are the starting point of those three accounts and are not recomputed (`19` §12.1); sub-table 3 and sub-table 4 are not added together (A-18).

---

## 1. Overview Table (for humans; do not feed it together with a phase)

| Phase | Title | Tasks | Task IDs | Person-days | Preceding Phase | Exit criterion (one sentence) |
|---|---|---|---|---|---|---|
| Phase 16 | Baseline and tool fixes | 6 | F-100 ~ F-105 | 7.5 | — | First the tool produces 77 / 77 and `FAIL 0 项` on the 0.0.2 baseline; then 90 registry objects are established for good (per category 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1), 22 crop kinds, a pool of 19, and 155 leaf nodes in `farmstorm-server.toml` |
| Phase 17 | The two status effects | 8 | F-106 ~ F-113 | 14.0 | Phase 16 | `18` §11's #3 ~ #7 all pass: sitting pose + lowering + rotation + squash + the full lock, with Confusion reversing direction only and yielding while the lock is active; each of the two sitting branches passes one run; the `CustomPacketPayload` and `Mixin` match counts are both 0 |
| Phase 18 | Friend Chives and Point Defense Fungus | 8 | F-114 ~ F-121 | 12.5 | Phase 16 | `18` §11's #8 / #9 / #12 / #13 / #14 all pass: a Euclidean 6-block sphere + the hit sound at 32 blocks, four 8-block filters (including non-ally), silent removal after 6; ally projectiles and vanilla projectiles all pass through |
| Phase 19 | Vibration Bean and the two reworks | 8 | F-122 ~ F-129 | 14.0 | Phase 16 | `18` §11's #10 / #11 / #15 / #16 all pass; `16` §5.3's 8-row difference table shows 4 changed and 4 unchanged; Vibration Bean uses one ruler at both ends; the `damage_types` subtotal is 3 |
| Phase 20 | Resources, audio, advancements and acceptance | 6 | F-130 ~ F-135 | 12.0 | Phase 16 + 17 + 18 + 19 | the tool at 90 / 90, config 155, lang 297, `FAIL 0 项`; resources 8 PNG + 1 OGG + 22 JSON; 3 events in `sounds.json`; `dist/farmstorm-0.0.3+mc1.21.1.jar` in place |
| **Total** | **5 Phases** | **36** | **F-100 ~ F-135** | **60.0** | | plus the 25 % buffer, **75.0 person-days** |

How to read the table:

- In the official dependency graph Phase 17 / 18 / 19 **do not depend on one another**; all three content lines wait only on Phase 16 (`19` §1.1), so they can be dispatched to three people separately. This file still lays them out in numeric order, and reordering only makes sense while those three lines run in parallel.
- The critical path runs "tool → registry layer → Point Defense Fungus → advancements → language → acceptance": `F-100 → F-101 → F-102 → F-103 → F-105 → F-117 → F-118 → F-119 → F-133 → F-134 → F-135` = **18.0 person-days / 11 tasks**, only 30 % of the 60.0 (`19` §11.1). More than half of the schedule is held hostage by the total workload and the degree of parallelism — to shorten the schedule, add parallelism first and squeeze the critical path only second.
- Phase 20 is the only convergence point: `F-135` depends on F-113 / F-121 / F-129 / F-130 ~ F-134, and if any one item in the four preceding phases fails, F-135 may not start.
- Two places where the numeric order is not the execution order: inside Phase 19, `F-123` before `F-122`; inside Phase 20, `F-130 / F-131 → F-132 → F-133 → F-134 → F-135`. There are three further cross-phase prerequisites: `F-126` waits for F-104, `F-132` waits for F-128, `F-133` waits for F-115 / F-119 / F-124 (`19` §1.2's supplementary prerequisites).
- Complexity distribution **S 6 / M 22 / L 8**. The 8 L tasks are `F-103` (registry wiring), `F-108` (the render branch), `F-109` (the mount branch), `F-118` (the four targeting filters), `F-123` (the unattached block), `F-127` (the definition of a hit) and `F-132` (22 JSON files), `F-135` (full acceptance) — these eight are the "one mistake means redoing the whole stretch" items, so give them generous context when feeding.
- The real calendar bottleneck is M12 (Phase 16): of its 7.5 person-days, 4.5 are the sequential chain `F-100 → F-101 → F-102 → F-103`, and M12 takes no parallelism, so not one of the four later milestones can start (`19` §11.3).

---

## 2. Phase 16 · Baseline and Tool Fixes (F-100 ~ F-105)

> How to feed it: the full text of `20-PROMPT-MAIN-0.0.3.md` + this section, one Phase at a time.

### 2.1 Prerequisites

- v0.0.2 is already released: `dist/farmstorm-0.0.2+mc1.21.1.jar` is in place and `.tools\gw.cmd build` exits 0 (the existing facts of `16` §1).
- `18` §1.2 (the 77 per-category measurement), §1.4(c) (the tool's four changes), §2.1 / §2.2 (the +13 named one by one), §8.1 (the 19-row config) and §11 #1 / #2 have been read; `12-TECH-0.0.2.md` §2.1 (the 12-row table) and §8.1 (the existing 22 rows) are within reach, because F-101 needs them for the reconciliation.
- The tool's current state is known: it enumerates per 0.0.1 (items 22 / blocks 20 / entity 9), its denominator is 64, and its config expectation reads only the 117 keys of `01-DESIGN.md` — **it is red until F-100**, and its output may not be used as acceptance evidence (`18` §1.4(c); RT-23 risks ① / ②).
- The frozen zone is confirmed untouched: 00 – 15 under `docs/zh` and all of `docs/en`, `01-DESIGN.md`, the defaults of the existing 139 config keys, the existing 19 `CropProfile` entries, the existing 21 converting recipes and the existing 15 advancements (D-80; `16` §5.7).
- The build entry point is `.tools\gw.cmd` and nothing else; do not call gradle directly. F-102's artifact path and file name follow it.
- This phase produces no behaviour at all: the two new projectiles' `onImpact` are skeletons, the three new blocks have only an appearance and a block entity, and the two new effects are registered without behaviour; the behaviour lands in Phases 17 ~ 19 respectively. A `/summon`-ed entity that "does not throw on landing" counts as passing — do not settle the impact point early.
- Keep a copy of the expected 12-category subtotals within reach: 27 / 22 / 6 / 11 / 2 / 2 / 1 / 1 / 1 / 2 / 1 / 1 (summing to 77), and after v0.0.3 closes, 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1 (summing to 90).

### 2.2 Goal

First use F-100 / F-101 to get the "taking the numbers" part right: make 0.0.2's baseline **77** a measured value that `tools/verify_project.py` can recompute, rather than a bare assertion (RT-23; A-14). Then use F-102 ~ F-105 to establish v0.0.3's **13 registry objects**, **19 rows of config changes** (16 purely new + 3 changes to existing defaults) and **3 new crop profiles** in one go: the registry self-check moves from 77 to 90, `farmstorm-server.toml`'s leaf nodes from 139 to 155, the crop profiles from 19 to 22 and the lucky berry pool from 16 to 19. Once the four startup self-check lines (registry 90 / crops 22 / pool 19, plus F-101's reconciliation conclusion) and the number 155 have taken shape, the four later Phases only fill behaviour into them and never change their shape again.

### 2.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-100 | Fix `tools/verify_project.py` to the 0.0.2 baseline of 77 | S / 0.5 | — | land the four changes of `18` §1.4(c) one by one: the expected key set 117 → 139, `EXPECTED` to items 27 / blocks 22 / entity 11, the denominator 64 → 77, and the remaining 5 bare ID literals completed, so that all 12 categories go through the per-category subtotal |
| F-101 | Reconcile the 0.0.2 baseline's three ledgers and take the numbers | S / 0.5 | F-100 | a read-only recomputation: the startup log's self-check lines, the tool's four sections of output and the 12-row table of `12-TECH-0.0.2.md` §2.1 reconciled item by item, with the conclusion written into the commit message (no source changes) |
| F-102 | Version switch and build baseline | S / 0.5 | F-101 | `gradle.properties`' `mod_version` changes from 0.0.2 to 0.0.3 (line 11), with zero changes to `build.gradle`; the artifact becomes `farmstorm-0.0.3+mc1.21.1.jar` |
| F-103 | The 13 registry objects, `DeployedKind` +2 and 11 new class skeletons | L / 3.0 | F-102 | the +13 across the seven registry classes, the two new `DeployedKind` values (6 → 8), the two `block_entities` cases, compile-time skeletons for the 11 new classes, the self-check's 12 assertions changed to 90, and the tool's registry section synchronised to 90 |
| F-104 | The 19 rows of config changes and the 16 `configuration` language keys | M / 1.5 | F-103 | 12 keys in four new groups + 2 keys appended to `fang_trap_cactus` with 3 existing defaults changed + 2 keys appended to `zangief_cane` (19 rows in total); the 16 `farmstorm.configuration.*` entries; the tool's expected key set 139 → 155 |
| F-105 | The three new profiles, `ALL` at 22 and the lucky berry pool at 19 | M / 1.5 | F-103 | `CropProfile` gains the three constants `FRIEND_CHIVES` / `POINT_DEFENSE_FUNGUS` / `VIBRATION_BEAN` (the latter two with block and seed `null` and `kind` filled truthfully), `ALL` at 22 entries, the pool's derivation untouched line for line, and the two self-check texts synchronised |

Phase subtotal: **6 tasks, 7.5 person-days** (S 3 / M 2 / L 1).

### 2.4 Phase exit criteria

- [ ] **F-100: the tool is back on the 0.0.2 baseline**: how to verify: `python -X utf8 tools/verify_project.py`. What passing looks like: the output shows `已核 registry 对象: 77 / 77`, no FAIL in the config section's 139 keys, `lang 键集一致，共 263 键` and `结论：FAIL 0 项`, with exit code 0 (`18` §1.4(c)'s target table; §11 #1; A-14).
- [ ] **F-100: the 12 per-category subtotals**: how to verify: read the grouped lines of the same output and compare them item by item with `18` §1.2. What passing looks like: 27 / 22 / 6 / 11 / 2 / 2 / 1 / 1 / 1 / 2 / 1 / 1, summing to 77; all 12 categories go through the per-category subtotal, with no branch left on bare ID literals.
- [ ] **F-100: the frozen zone has not been edited back**: how to verify: `git diff --stat` and `grep -c "64" tools/verify_project.py`. What passing looks like: `docs/` empty and `01-DESIGN.md` untouched; 64 is no longer the denominator, and no second hard-coded total exists in the tool.
- [ ] **F-101: the three-ledger reconciliation**: how to verify: put the four startup self-check lines of `logs/latest.log`, the tool's four sections of output and the 12-row table of `12-TECH-0.0.2.md` §2.1 side by side and compare them item by item. What passing looks like: the three are equal item by item; the 7 cross-check rows of `18` §1.3 (19 profiles, pool of 16, types 10 / 6 / 3 and Demolition 4, registry 77, 2 events in `sounds.json`, config 139, language keys 263) each hold.
- [ ] **F-101: take the numbers only, change no code**: how to verify: `git diff --stat`. What passing looks like: it contains only `tools/` (F-100's changes), with `src/` and `docs/` empty.
- [ ] **F-102: build baseline**: how to verify: check the exit code and `build/libs` after `.tools\gw.cmd build`. What passing looks like: exit code 0, the artifact is `build/libs/farmstorm-0.0.3+mc1.21.1.jar`; `jar tf` contains `META-INF/neoforge.mods.toml` whose `version` is 0.0.3; `build.gradle`'s `git diff` is empty.
- [ ] **F-103: the registry self-check line**: how to verify: start the server and read `logs/latest.log`. What passing looks like: `自检 注册对象 90 个：items 31 / blocks 25 / block_entities 6 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK` appears word for word (the v0.0.3 column of `18` §2.1 + §11 #2).
- [ ] **F-103: the tool synchronised to 90**: how to verify: run the tool once more. What passing looks like: the registry section reads `90 / 90`; the config section is still 139 and the lang section still passes as before (those two only move after F-104 and F-134 respectively).
- [ ] **F-103: the 13 new IDs**: how to verify: `/give` each of the four items once, `/setblock` each of the three blocks once, `/summon` each of the two entities once, both effects in `/effect`'s completion, and `farmstorm:friend_chives` in `/playsound`'s completion. What passing looks like: every command returns success, and the IDs match the 13 rows of `18` §2.2 character for character; the client does not crash after `/summon` (both projectiles go through `ThrownItemRenderer`; the formal 13 / 13 renderer check is in F-135, `19`'s own note "supplied by this document").
- [ ] **F-103: enum and exhaustive switch**: how to verify: read the startup log's kind mapping line, the compile log and `git diff behavior/DeployedKind.java`. What passing looks like: all 8 values print, and `VIBRATION_BEAN`'s scan interval is 10 ticks; the compile log contains no `the switch expression does not cover all possible input values`; the `git diff` contains added lines only, with not one of the six existing constants changed (A-11).
- [ ] **F-104: config 155**: how to verify: after starting the server, count the leaf nodes of `run/config/farmstorm-server.toml` (abbreviated as `config/farmstorm-server.toml` in `19`). What passing looks like: **155**; the four new groups `qbounce` / `friend_chives` / `point_defense_fungus` / `vibration_bean` are appended at the end of the file in alphabetical order (`18` §8.2's TOML fragment).
- [ ] **F-104: the three changed values and the three pose parameters**: how to verify: read those six keys of the TOML. What passing looks like: `fang_trap_cactus.triggerRadius = 2.5`, `outerDelayTicks = 4`, `innerDelayTicks = 2`, `qbounce.rotateDegreesPerTick = 24.0`, `squashAmplitude = 0.15`, `squashPeriodTicks = 6`; the existing keys' `min` / `max` and language keys are kept character for character (A-15; the note under the table in `18` §8.1).
- [ ] **F-104: no `confusion` group**: how to verify: `grep -c "[confusion]" run/config/farmstorm-server.toml`. What passing looks like: **0** hits (D-83; `18` §8.1's "1 non-key note").
- [ ] **F-104: language keys equal to config**: how to verify: count the keys with the `farmstorm.configuration.*` prefix (once in Chinese and once in English). What passing looks like: **155** entries with identical key sets in Chinese and English. Note that the language files' total key count at this point is **279** (263 + this phase's 16), not 297 — 297 closes in F-134 (34 = this phase's 16 + the other 18).
- [ ] **F-104: hot reload**: how to verify: change `point_defense_fungus.maxIntercepts` to 3 → `/reload` → read it back; then change it back to 6 → `/reload` → read it back. What passing looks like: the two readings are 3 and 6 (the existing `bake()` convention).
- [ ] **F-105: the two self-check lines**: how to verify: read `logs/latest.log`. What passing looks like: `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 4 → OK` and `自检 幸运浆果池 19 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK` appear word for word.
- [ ] **F-105: the frozen items and the two assertions**: how to verify: `git diff util/CropProfile.java` plus one in-game reading. What passing looks like: not one character of the existing 19 profiles' eight fields has changed, and the diff for the `LUCKY_BERRY_POOL` line is empty; `CropProfile.VIBRATION_BEAN.kind() == DeployedKind.VIBRATION_BEAN` (not `null`, A-11); the two new deployment profiles' `block()` / `seed()` return `null`.
- [ ] **Phase-level**: how to verify: run everything above in the same session, plus `.tools\gw.cmd build`. What passing looks like: the build exits 0; the three startup self-check lines (registry 90 / crops 22 / pool 19) are all OK; `farmstorm-server.toml` has 155 leaf nodes (`19` §3's phase exit criteria).

### 2.5 Common failure modes

1. **Announcing 90 before the tool is fixed** (RT-23 risk ④): the symptom is a task marked done while the tool still outputs `62 / 64` or `FAIL 1 项`, so the number "90" has no evidence behind it. Catch it early: F-103's acceptance order must be "run the tool first, then read the log"; if the tool's denominator is still 64, or the §1 config section still compares against `01-DESIGN.md`'s 117, then F-100 has not landed.
2. **Editing a frozen document back to make the tool go green** (RT-23 risk ③; D-80): someone sees the §1 config section report "toml 多 22 个键" and goes and edits the 117 in `01-DESIGN.md`. The symptom is `docs/` showing up in `git diff --stat` with the frozen zone broken. Catch it early: F-100's third acceptance item is exactly this assertion; run `git diff --stat` once before committing.
3. **Changing the tool's expected key count from 139 straight to 155 instead of adding the 19 rows one by one** (the deliverable F-104 requires): changing one total is a one-line job, but then not a single key name has been checked; the symptom is a config section that "does not FAIL but has not checked any new key either". Catch it early: copy the 19 rows of `18` §8.1 into a checklist and run it against the TOML line by line, comparing key names character for character; then count the keys with the `farmstorm.configuration.` prefix — both numbers must be 155.
4. **`DeployedKind` gaining only one value** (A-11; `18` §1.4(b)): following the old reading of row 22 in `16` §4 and filling Vibration Bean with `null` still compiles and still places with `/setblock`, but `kindOf` cannot get a value and silently falls back to `BLAST_CARROT`, so the "trap" runs as an "explosion trap". Catch it early: F-105's acceptance asserts outright that `kind()` is not `null`; F-103's acceptance requires all 8 values to print.
5. **Completing the exhaustive switch in only one place** (F-103's deliverable): both `DeployedBehaviors` and `FarmstormBlockEntities.deployedOf` need the `case`. Missing one is a compile error, `the switch expression does not cover all possible input values`; worse is papering over it with `throw new UnsupportedOperationException()` — the build goes green and the server crashes when the chunk loads. Catch it early: build immediately after adding the two enum values; the registry self-check must still show `block_entities` at 6 (a 7 means a BE was registered along the way, which is the mistake).
6. **"Tidying up" `ALL` or the existing 19 profiles** (F-105 item 3; `16` §5.7): reordering the array while adding the three constants, or rewriting `LUCKY_BERRY_POOL`'s derivation on the side, both touch the fields of the existing 19 profiles. Catch it early: `git diff util/CropProfile.java` may contain added lines only — any `-` line is a failure; the diff for the pool's line must be empty.

### 2.6 What to report

- The tool's complete four sections of raw output (`77 / 77`, config 139, `lang 键集一致，共 263 键`, `结论：FAIL 0 项`) and the exit code, plus the 12-category subtotal line.
- The conclusion of the three-ledger reconciliation: the four startup log lines / the tool's four sections / the 12 rows of `12-TECH-0.0.2.md` §2.1, either equal item by item or with the differing item named.
- The artifact path under `build/libs`, the `neoforge.mods.toml` version line from `jar tf`, and whether `build.gradle`'s `git diff` is empty.
- The **raw text** of the registry self-check line (90 and the 12 per-category subtotals), the 22-crop line and the 19-pool line (paste the log, do not paraphrase).
- `farmstorm-server.toml`'s leaf-node count (155) + the four new group names + six sampled defaults (2.5 / 4 / 2 / 24.0 / 0.15 / 6).
- The 8 kind → singleton mappings of `DeployedKind`, and the reading of `VIBRATION_BEAN`'s scan interval.
- Open issues (for example whether a second hard-coded total still exists in the tool, and whether the two new projectiles really render through `ThrownItemRenderer` on the client).

---

## 3. Phase 17 · The Two Status Effects (F-106 ~ F-113)

> How to feed it: the full text of `20-PROMPT-MAIN-0.0.3.md` + this section, one Phase at a time.

### 3.1 Prerequisites

- All of Phase 16's exit criteria pass, especially `F-103` (the two `mob_effects` registrations + 11 new class skeletons) and `F-104` (the 4 `qbounce` keys, `poseMode` included) — 6 of this phase's 8 tasks directly modify what those two produced.
- `18` §3.1 / §3.2 (the two effect classes), §3.12 (the two client state machines), §4 (the two sitting branches), §5 (the Confusion reversal) and §11 #3 ~ #7 have been read; `17` §2 (Q-Bounce), §3 (Confusion) and §12.1 / §12.2 / §12.3 / §12.12 / §12.13 have been read.
- The to-be-verified APIs are already registered: how `RenderLivingEvent.Pre/Post` are obtained, `EntityModel#riding`, `com.mojang.math.Axis`, the constructor arguments of `ClientboundSoundPacket` (RT-17 risk ③), and `ArmorStand#setMarker` and `startRiding(Entity, boolean)` (RT-18 risk ③) — work in the order "write a minimal case and get it running first, then go into the main logic".
- The existing seven-stage input lock's current state is known: `InputLockHandler.locked()` recognises only `ZangiefClientState.active()`, and F-110 only adds one term to it; no second lock is written (D-79; RT-19).
- Red-line counts: no new `entity_types` (the increment stays +2, A-12), no network packets (`CustomPacketPayload` 0), no `Mixin` (0).
- Neither effect's body knows whether it lasts 10 seconds or 4: the duration is written by the applying side, and the effect classes define no duration key of their own and read no config key at all (`18` §3.1 / §3.2).

### 3.2 Goal

Finish the two status effects `q_bounce` and `confusion`: Q-Bounce has to deliver the four things "sitting pose + rotation + squash-and-stretch + the full lock", with both sitting branches implemented and switchable through the single value `qbounce.poseMode` (the losing one switched to off by default, without deleting a line of code); Confusion only reverses the two components of the movement direction and yields while the lock is active. At the end of the phase: throw Friend Chives at a zombie (or apply it directly with `/effect`) and it sits and spins, bobs up and down on a 6-tick cycle, and no key does anything (except ESC); with Confusion applied alone, W walks backwards and A strafes right, and everything else works as usual; with both stacked, it does not move at all.

### 3.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-106 | The two effect classes: `QbounceEffect` and `ConfusionEffect` | M / 1.5 | F-103 | two `MobEffect` subclasses: the constructor sets `MobEffectCategory.HARMFUL` and the colour, neither overrides `applyEffectTick` / `isDurationEffectTick`, neither defines a duration key, neither reads config; `confusion` is hidden and registers no icon |
| F-107 | The client's three-piece state machine and the compile-time mirror constants | M / 1.5 | F-106 | `QbounceClientState`: `t` accumulates tick by tick, `θ = 24.0 × t mod 360`, `s = 0.15 × sin(2π t / 6)`, `scale = (1 − s/2, 1 + s, 1 − s/2)`, reset to zero when the effect disappears; the three compile-time mirror constants with their comment about keeping them in step |
| F-108 | The `render` branch: model `riding`, lowering, rotation, squash | L / 3.0 | F-107 | `QbouncePoseRenderer`: in `Pre`, `pushPose` → `translate(0, −0.35, 0)` → `scale` → rotation about `Axis.YP`, with a paired `popPose` in `Post`; the only entry condition is `hasEffect(Q_BOUNCE)` |
| F-109 | The `mount` branch: the invisible `minecraft:armor_stand` mount and the ledger table | L / 3.0 | F-106 | `QbounceMountController`: `ARMOR_STAND.create(level)` + the five property overrides + `startRiding(stand, true)` + an in-memory ledger + three checks per tick + four exit paths, giving up after more than 20 re-mounts and printing one warning line |
| F-110 | Q-Bounce reuses the seven-stage input lock | M / 1.5 | F-107 | `InputLockHandler.locked()`'s criterion widens to "`ZangiefClientState.active()` or `QbounceClientState.active()`", with not one character of the seven stages or of "ESC is the only thing left" changed |
| F-111 | Confusion's movement reversal and "the lock wins" | M / 1.5 | F-106 | `ConfusionInputHandler`: subscribes to `MovementInputUpdateEvent`, first `if (InputLockHandler.isLocked()) return;`, then negates `forwardImpulse` and `leftImpulse` each; jump / sneak / camera / attack / use are never touched |
| F-112 | The real-client comparison of the two sitting branches and the conclusion on which to keep | S / 0.5 | F-108, F-109 | a record of one real-client comparison (look / AI behaviour / collision differences / whether third-party custom models honour `riding` / which one is finally kept), with the losing branch switched to off by default and its code kept |
| F-113 | Phase 17 exit regression | M / 1.5 | F-110, F-111, F-112 | one regression: the handling of `17` §12.1 / §12.2 / §12.3 / §12.12 / §12.13's five boundaries + a rerun of `18` §11 #3 ~ #7 |

Phase subtotal: **8 tasks, 14.0 person-days** (S 1 / M 5 / L 2).

### 3.4 Phase exit criteria

- [ ] **F-106: both effects can be applied**: how to verify: `/effect give @s farmstorm:q_bounce 200 0` and `/effect give @s farmstorm:confusion 80 0`, then `/data get entity @s active_effects`. What passing looks like: both commands succeed, two records appear in the effect list, both with `amplifier` 0 and `duration` 200 and 80 respectively.
- [ ] **F-106: the effect classes read no config and register no icon**: how to verify: `grep -c "FarmstormConfig" effect/` and `grep -rc "mob_effect/confusion" src/main/java/`. What passing looks like: both hits are **0**, and there is no `confusion.png` under `textures/mob_effect/` (D-83).
- [ ] **F-106: the common side does not crash**: how to verify: start a dedicated server once (not merely run the client). What passing looks like: no `NoClassDefFoundError` in the startup log (neither effect class depends on `Dist.CLIENT`).
- [ ] **F-107: the three mirror constants**: how to verify: compare the constants' values one by one with `qbounce.*` in `run/config/farmstorm-server.toml`. What passing looks like: 24.0 / 0.15 / 6 are equal value by value, and a single mismatch fails the item (A-15; `18` §4.2).
- [ ] **F-107: `t` accumulates only locally on the client**: how to verify: once the effect is active, change its remaining duration to 100 with `/effect` and watch the phase. What passing looks like: `t` does not jump phase (it is not derived by "total duration − remaining duration").
- [ ] **F-107: reset to zero and no log spam**: how to verify: read the log for the two seconds in which the effect disappears. What passing looks like: `t` returns to zero on the next tick, and exactly one `q_bounce t=0` line is printed.
- [ ] **F-107: no packets, no writes back to the entity**: how to verify: two greps. What passing looks like: `grep -c "CustomPacketPayload" src/main/java/` is 0 and `grep -c "setYRot" client/QbounceClientState.java` is 0.
- [ ] **F-108: the look of the `render` branch**: how to verify: with `qbounce.poseMode = render`, apply Q-Bounce to a zombie and watch the picture plus F3 / F3+B. What passing looks like: `18` §11 #3 all passes — riding pose (legs pressed down), lowered by 0.35 blocks, about 480 degrees/second of rotation (24.0 degrees/tick), squash-and-stretch on a 6-tick cycle; the AI is still walking (its position keeps changing) and the hitbox is unchanged.
- [ ] **F-108: `push` / `pop` are paired**: how to verify: watch the picture after 30 seconds of continuous rendering. What passing looks like: no overall drift (RT-17 verification ③).
- [ ] **F-108: three at once, and the degradation**: how to verify: look at one frame each for 3 affected creatures on the same screen; then apply the effect to a model that does not read `riding` (pick any vanilla creature whose model is not a `HumanoidModel`). What passing looks like: the three do not accumulate offsets on each other; the third-party model degrades to "rotation only, no sitting pose" without throwing (RT-17 fallback items ② / ③).
- [ ] **F-109: the mount and the cleanup**: how to verify: with `qbounce.poseMode = mount`, apply the effect and read `/data get entity`; then `kill`, disconnect and reconnect, and walk through a portal once each. What passing looks like: `RootVehicle` points at `minecraft:armor_stand`; after the creature dies that ArmorStand is no longer in the world; none of the three exit paths leaves a residue (`18` §11 #4; RT-18 verification ②).
- [ ] **F-109: switching and the red line**: how to verify: switch `poseMode` twice (render → mount → render) + read the registry self-check. What passing looks like: after each switch the ledger is cleared and every mount is `discard()`-ed; `entity_types` is still 13 and the increment still +2 (A-12).
- [ ] **F-110: the seven-stage lock**: how to verify: apply Q-Bounce to a player and press W / A / space / sneak / left click / right click / scroll wheel / a number key once each, then press ESC. What passing looks like: everything except ESC (which opens the pause menu) does nothing; the lock log prints exactly once (`18` §11 #5).
- [ ] **F-110: the lock has exactly one definition**: how to verify: `grep -rn "isLocked" src/main/java/com/dashpig/farmstorm/client/`. What passing looks like: every hit points at `InputLockHandler`; after the effect ends, all input returns on the next tick.
- [ ] **F-111: Confusion reverses direction only**: how to verify: apply Confusion alone, press W then A, and compute the speed amplitude from F3 coordinates tick by tick. What passing looks like: W walks backwards, A strafes right, and the amplitude matches normal; camera / jump / sneak / left click all work as usual (`18` §11 #6).
- [ ] **F-111: the lock wins**: how to verify: apply Q-Bounce and then Confusion (or the other way round) and press W. What passing looks like: displacement 0, the lock log has printed, and reversed movement returns on the next tick after the effects end (`18` §11 #7).
- [ ] **F-111: one subscription, no touching of speed or camera**: how to verify: three greps. What passing looks like: `MovementInputUpdateEvent` appears once in `ConfusionInputHandler`; `MOVEMENT_SPEED` and `Sensitivity` appear 0 times each.
- [ ] **F-112: both branches pass and both sets of code are present**: how to verify: switch the single value `poseMode` and run #3 / #4 once each; look at both class files with `git diff`. What passing looks like: each branch passes on its own; both `QbouncePoseRenderer` and `QbounceMountController` are present, with no deleted lines in the diff; `poseMode`'s default is still `render`; the record carries one conclusion each for RT-17 and RT-18.
- [ ] **F-113: boundary regression**: how to verify: death / logout / dimension change once each during the effect; coexistence with Zangief Cane's phase lock; a spectator and a creative player once each; being leashed and being ridden once each. What passing looks like: no mount residue, and the lock is released after respawning; only one lock log line prints and Confusion still returns early as usual; spectators are unaffected and creative players behave as usual; neither case rolls back or crashes (`17` §12.2 / §12.3 / §12.12 / §12.13).
- [ ] **Phase-level**: how to verify: rerun #3 ~ #7 + the two greps + the registry self-check. What passing looks like: all five pass; the `CustomPacketPayload` and `Mixin` match counts are both 0; the `entity_types` increment is still +2 and the registry self-check is still 90 (`19` §4's phase exit criteria).

### 3.5 Common failure modes

1. **The three mirror constants out of step with config** (F-107 item 1; A-15): the client constants are a second copy of the numbers, fixed at compile time, so copying them wrongly does not fail the build — it just "looks slow" or "has a long squash cycle". The symptom: about 240 degrees/second, or a 10-tick squash cycle — exactly the old readings A-15 ruled wrong. Catch it early: compare the three constants value by value against `qbounce.*` in `farmstorm-server.toml`; one mismatch fails the item.
2. **`pushPose` and `popPose` not paired** (RT-17 risk ②): `Post` returns early because of an exception, or only pops under some conditions; the symptom is that after 30 seconds of continuous rendering the whole picture accumulates the offset — the longer you look, the more crooked it gets, and quitting and re-entering fixes it, so it is easily mistaken for "a graphics card problem". Catch it early: RT-17 verification ③'s "render continuously for 30 seconds and check for drift" has to be done once on a real client.
3. **Deriving `t` from "total duration − remaining duration"** (F-107 item 2): the code is easier to write, but the phase jumps whenever `/effect` changes the remaining duration. The symptom: the rotation angle suddenly jumps a long way after the effect is extended. Catch it early: F-107 item 2 is exactly this control experiment — change the remaining duration to 100 and see whether `t` jumps.
4. **Writing a second lock for Confusion** (RT-19; D-79): the symptom is that with Q-Bounce + Confusion together, pressing W moves you backwards — the two pieces of logic override each other and the lock is beaten by execution order. Catch it early: every `grep -rn "isLocked"` hit must point at `InputLockHandler`; `ConfusionInputHandler`'s first statement must be `if (InputLockHandler.isLocked()) return;`.
5. **The `mount` branch missing one exit path** (RT-18 risk ②): miss any one of death / disconnect / dimension change / server stop and the invisible ArmorStand stays in the world, pinning the player in place. The symptom: after rejoining the server the character cannot move, and there is nothing to be found that is blocking it. Catch it early: do each of the three paths once + switch `poseMode` twice and check whether the ledger is cleared; after more than 20 re-mounts one warning line is printed, and seeing that line means an exit path was missed.
6. **Adding an `entity_type` for the mount** (A-12; D-102): the moment one is added, the registry self-check goes from 90 to 91 and `entity_types` from 13 to 14, violating A-12 and D-102 at the same time. Catch it early: check the `entity_types` subtotal in the registry self-check line after every build in this phase.

### 3.6 What to report

- The per-item readings of `18` §11's #3 ~ #7 (sitting pose / lowered by 0.35 / 480 degrees per second / the 6-tick cycle / the AI still walking / the hitbox unchanged; the result of each of the seven-stage keys once; Confusion's W and A displacements; displacement 0 when stacked).
- The real-client comparison record of the two sitting branches: look, AI behaviour, collision differences, whether third-party custom models honour `riding`, which one is finally kept, and how the losing one was turned off (default value versus configuration).
- The values of the three mirror constants with the corresponding TOML readings (24.0 / 0.15 / 6).
- The raw text of the lock log (there should be exactly one line) and when it was printed.
- The evidence of 0 hits for four greps: `CustomPacketPayload`, `Mixin`, `FarmstormConfig` inside the `effect/` directory, and `setYRot` inside `QbounceClientState`.
- The registry self-check line (still 90, `entity_types` 13, `block_entities` 6) and the artifact under `build/libs`.
- Open issues (especially the conclusion for each of RT-17's 4 to-be-verified APIs, the actual semantics of `setMarker(true)`, and which of the two branches is the better candidate for a future default).

---

## 4. Phase 18 · Friend Chives and Point Defense Fungus (F-114 ~ F-121)

> How to feed it: the full text of `20-PROMPT-MAIN-0.0.3.md` + this section, one Phase at a time.

### 4.1 Prerequisites

- All of Phase 16's exit criteria pass, especially `F-103` (`blocks` +3, `entity_types` +2, `sound_events` +1) and `F-105` (the `FRIEND_CHIVES` and `POINT_DEFENSE_FUNGUS` profiles).
- `18` §3.3 (Friend Chives' projectile), §3.4 (the Point Defense Fungus behaviour class), §6 (the four interception filters and the criterion), the appendix to §9.2 (the full sound-event table) and §11 #8 / #9 / #12 / #13 / #14 have been read; `17` §4 (Friend Chives), §5 (Point Defense Fungus) and §12.4 / §12.14 have been read.
- The family reading is known: `ThrownCropEntity` has **12** members in total (0.0.2's 10 + this release's 2), and the decision uses the base class's `instanceof`, not an ID whitelist (D-98; `18` §6.1).
- Three increment boundaries: the `sound_events` increment is **+1** (only `friend_chives`), `block_entities` is still **6** (both new blocks reuse `DeployedCropBlockEntity`), and `sounds.json`'s event count goes **2 → 3** (only `friend_chives`; the interception and explosion sounds reuse vanilla, so A-13 applies to those two only — **do not** read it as "no sound event is added in this phase", which was the old draft's over-generalisation).
- The ally criterion uses only the existing `be.isAlly(player)` chain and writes no new team comparison; when the owner is not a player it is treated as non-ally (`18` §6.1's ally-exclusion row).

### 4.2 Goal

Take the two new crops from profile to behaviour. Friend Chives runs the loop "seed → crop block (4 ages) → mature harvest → throw → a Euclidean 6-block sphere at the impact point → the hit sound within 32 blocks"; Point Defense Fungus runs the interception loop "deployed block → Euclidean 8-block targeting → four filters (including non-ally) → one interception per instant → a count of 6 → purely visual effects → `discard()` without settling the impact point". At the end of the phase: plant a patch of chives, harvest a round, throw one and bounce monsters and teammates up together (the sound plays only on a hit, audible within 32 blocks); place one Point Defense Fungus and your own side's projectiles pass straight through while the other side's are stopped with particles and a sound; after 6 interceptions in a row the fungus disappears and nothing is left on the ground.

### 4.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-114 | Friend Chives: the item, Chive Seeds, the crop block and its 4 ages | M / 1.5 | F-103, F-105 | `friend_chives` (`ThrowableCropItem`) and `chive_seeds` (`SeedItem`), `friend_chives_crop` (`FarmstormCropBlock`, 4 ages), the profile wiring, and the `converter_fuel` tag key (the tag file itself is in F-132) |
| F-115 | `FriendChivesProjectile`'s impact settlement: the Euclidean 6-block sphere and 200 ticks of Q-Bounce | M / 1.5 | F-114, F-106 | `onImpact` takes a Euclidean sphere of radius 6.0 centred on the impact point and writes 200 ticks of `q_bounce` (amplifier 0) into every `LivingEntity` inside it; the accumulated number of successful applications `n` feeds F-116's decision whether to play the sound |
| F-116 | Friend Chives' hit sound: played only on a hit, once, audible within 32 blocks | S / 0.5 | F-115 | plays only when `n ≥ 1`, once per throw, with `SoundSource.PLAYERS`, volume 2.0 (16 × 2.0 = 32 blocks) and the event `farmstorm:friend_chives` |
| F-117 | The Point Defense Fungus block, the `POINT_DEFENSE_FUNGUS` category name and the deployment entry | M / 1.5 | F-103, F-105 | `deployed(POINT_DEFENSE_FUNGUS, …)` (Turret appearance, borrowing vanilla `warped_fungus`), the dispatch in `DeployedBehaviors`, and the profile wiring (no crop block, no seed) |
| F-118 | Point Defense Fungus targeting and the four filters (including "non-ally") | L / 3.0 | F-117 | `PointDefenseFungusBehavior`: the 8-block AABB serves only as a coarse filter → `getEntitiesOfClass(ThrownCropEntity.class, box, predicate)` → the four filters in a fixed order → the nearest one handed to `fire` |
| F-119 | The interception count, same-tick de-duplication and the silent removal on the 6th | M / 1.5 | F-118 | `DeployedCropBlockEntity#interceptCount` + `recordIntercept(now)`, `ThrownCropEntity#lastInterceptTick` + `markIntercepted(now)` (not persisted); after the 6th, `consume()` → `removeBlock`, without touching the loot table |
| F-120 | The interception effects and `discard()` (no impact settlement) | M / 1.5 | F-119 | `fire`'s four steps in a fixed order: mark as intercepted → `sendParticles` (`ParticleTypes.CRIT`) → `playSound(SoundEvents.SHIELD_BLOCK)` → `recordIntercept` then `target.discard()`; no branch anywhere goes through `onImpact` |
| F-121 | Phase 18 exit regression | M / 1.5 | F-116, F-120 | one regression: `18` §11's #8 / #9 / #12 / #13 / #14 plus `17` §12.4's chunk-unload boundary |

Phase subtotal: **8 tasks, 12.5 person-days** (S 1 / M 6 / L 1).

### 4.4 Phase exit criteria

- [ ] **F-114: the two items and the four ages**: how to verify: `/give @s farmstorm:friend_chives` and `/give @s farmstorm:chive_seeds`; the creative tab `farmstorm:main`; plant the seeds on farmland and push `age` from 0 to 3 (`/setblock ... age=3` once and natural growth once). What passing looks like: both commands succeed, both items are visible, the crop block id is `farmstorm:friend_chives_crop` and all four stages are visible.
- [ ] **F-114: no converting recipe, farmland only**: how to verify: `grep -c "FRIEND_CHIVES" datagen/ModRecipeProvider.java`; `/setblock` a seed on non-farmland. What passing looks like: the grep is 0; no crop appears on non-farmland (D-84).
- [ ] **F-114: the throw entry exists**: how to verify: `/summon farmstorm:friend_chives ~ ~2 ~`. What passing looks like: the command succeeds and landing throws nothing (the impact settlement is F-115; this is the skeleton `onImpact`).
- [ ] **F-115: the Euclidean 6-block sphere**: how to verify: hit 1 creature and read `/data get entity`; then place creatures at 6.0 blocks inside the sphere and at a diagonal 8.4 blocks. What passing looks like: everything within 6 blocks gets Q-Bounce with `duration` 200 / `amplifier` 0; the diagonal 8.4-block creature does not (`18` §11 #12 first half; `17` §4.5).
- [ ] **F-115: no ally/enemy filtering, one criterion only**: how to verify: station one teammate and one opposing player; then read `n` from the log. What passing looks like: both are affected; the thrower themselves also gets it inside the sphere; `n` is the count of successful applications, with no second "is anything nearby" query.
- [ ] **F-116: the hit sound's distance and count**: how to verify: stand at 32 blocks and at 33 blocks and listen once each; hit 3 creatures in one throw; then throw one empty. What passing looks like: audible within 32 blocks and inaudible beyond 33; hitting 3 plays it only once; an empty throw (`n = 0`) plays nothing at all (`18` §11 #12 second half).
- [ ] **F-116: two places writing the same number**: how to verify: change `friend_chives.soundRange` to 16.0 and stand 16 blocks away to listen. What passing looks like: audible at 16 blocks (the volume is still 2.0); change it back to 32.0 after verifying; the sound source category is `PLAYERS`, so background music is not interrupted.
- [ ] **F-117: the block and its ownership**: how to verify: `/setblock ~ ~ ~ farmstorm:point_defense_fungus`, then `/data get block`. What passing looks like: the command succeeds, the geometry is visible in the world, and the `owner` field is returned (reusing `DeployedCropBlockEntity`, so `block_entities` is still 6).
- [ ] **F-117: enum dispatch and the caps**: how to verify: read the kind mapping line of the startup log; then fill one chunk with 8 of them. What passing looks like: all 8 values print and `POINT_DEFENSE_FUNGUS`'s scan interval is 10 ticks; the existing caps of 32 per player / 8 per chunk still apply.
- [ ] **F-117: zero new textures**: how to verify: check whether any file named `point_defense_fungus` exists under `textures/`. What passing looks like: none (both the block and the item borrow vanilla `warped_fungus`).
- [ ] **F-118: vanilla projectiles all pass through**: how to verify: throw a vanilla arrow / snowball / trident / ender pearl / `farmstorm:turret_bullet` at the fungus once each, then throw one Friend Chives. What passing looks like: all 5 pass through and `interceptCount` is unchanged; Friend Chives is intercepted and `interceptCount` +1 (`18` §11 #13).
- [ ] **F-118: Euclidean, not Chebyshev**: how to verify: throw once at a diagonal 7.9 blocks and once at 8.1 blocks. What passing looks like: 7.9 blocks is intercepted, 8.1 blocks is not.
- [ ] **F-118: non-ally and the three exceptions**: how to verify: have the deployer themselves and a teammate throw once each; then produce an ownerless projectile with a dispenser or `/summon`; then throw a wind charge whose `isSubmunition()` is true. What passing looks like: both ally throws pass through with no charge spent and no particles; an ownerless one is treated as non-ally and intercepted as usual; the wind charge is not intercepted (D-98).
- [ ] **F-119: the count of six**: how to verify: throw 6 Friend Chives in a row at a single fungus and read `interceptCount` in the log. What passing looks like: 1..6 in sequence, with the block gone at the instant the 6th ends and no dropped items.
- [ ] **F-119: same-tick and multi-fungus de-duplication**: how to verify: a single fungus facing 2 projectiles in the same instant; then 3 fungi around the same projectile. What passing looks like: the former intercepts only 1, with the count +1 and the log's "already intercepted this tick" branch taken at least once; the latter's `interceptCount` goes up by exactly 1 in total, and the other 2 fungi make no particles (`18` §11 #8 / #9).
- [ ] **F-119: the mark is not persisted, the cap is tunable**: how to verify: check whether `lastInterceptTick` appears near `addAdditionalSaveData`; change `maxIntercepts` to 2 and intercept twice. What passing looks like: the grep hit count is 0; the block disappears after the 2nd interception; change it back to 6 after verifying.
- [ ] **F-120: the interception effects are purely visual**: how to verify: a player at full health stands within 8 blocks and has 6 projectiles intercepted. What passing looks like: health / hunger / coordinates unchanged point by point, the block and its block state unchanged, and no `Explosion` construction line in the log (`18` §11 #14).
- [ ] **F-120: nothing follows an interception**: how to verify: look at where an intercepted projectile lands. What passing looks like: no explosion, no block written, no effect applied, no drops (D-99).
- [ ] **F-120: three 0-hits and the particle sync**: how to verify: `sounds.json`'s event count, `grep -c "hurt("` and `grep -c "new Explosion"`; then set `interceptParticles` to 0 and intercept once. What passing looks like: the event count is still 2; both greps are 0; with a particle count of 0 there are no particles, and setting it back to 8 restores them.
- [ ] **Phase-level**: how to verify: rerun the five self-checks + the registry self-check line. What passing looks like: `18` §11's #8 / #9 / #12 / #13 / #14 all pass; `interceptCount` is unchanged in both the ally-passes-through and the vanilla-projectile-passes-through cases; the three subtotals `sound_events` 3, `block_entities` 6 and `entity_types` 13 are unchanged.

### 4.5 Common failure modes

1. **Treating the AABB as the decision** (RT-20 risk ①): the 8-block AABB is a cubic coarse filter whose corners sit 13.86 blocks from the centre, and using it directly as the hit decision makes it Chebyshev. The symptom: a diagonal 8.4 blocks away is intercepted while 7.9 blocks is not. Catch it early: the `distanceTo(...) <= 8.0` Euclidean re-check must be inside the predicate; test the two points — a diagonal 7.9 must be intercepted and 8.1 must not — once each.
2. **The "non-ally" filter left out** (RT-20 risk ⑤; D-98): the symptom is that your own and your teammates' projectiles get eaten by your own fungus — the count still goes +1 and the particles still play, and the player thinks it is a bug. Catch it early: the third part of `18` §11 #8 is exactly this; throw once as an ally and see whether `interceptCount` moves and whether there are particles.
3. **Not really calling `discard()` after interception** (RT-20 risk ⑥; D-99): the symptom is that the intercepted chives settle their impact point as usual, the 6-interception cap is a dead letter, and the opponent gets Q-Bounce for free. Catch it early: an intercepted projectile's impact point must show all four absences — no explosion / no block written / no effect applied / no drops; `grep -c "new Explosion"` and `grep -c "hurt("` are both 0.
4. **The de-duplication mark on the wrong object** (RT-20 mitigation ②): when the mark sits on the block instead of the projectile, 3 fungi around the same projectile each spend a charge. The symptom: `interceptCount` goes +3 at once. Catch it early: `lastInterceptTick` must be written on `ThrownCropEntity` and must not enter `addAdditionalSaveData`; a three-fungus single-throw reading may only be +1.
5. **Tying the number of sounds to the number of hits** (F-116 item 3; D-85): written as "play once per affected creature", hitting 3 plays it 3 times, and an empty throw may still play it because "there was something in range". Catch it early: `n ≥ 1` is the only criterion, one throw plays once, and an empty throw plays nothing at all.
6. **`soundRange` and the volume 2.0 not lining up** (F-116 item 2): changing `soundRange` to 16.0 while forgetting the volume, or hard-coding the volume to 1.0, makes the "audible within 32 blocks" acceptance depend on feel instead of on a reading. Catch it early: with `soundRange` at 16.0 it is audible at 16 blocks, and after changing it back to 32.0 it is audible at 32; stand at 16 and at 32 blocks both times.
7. **Adding a `sound_event` for Point Defense Fungus** (A-13; D-100): once one is added, `sound_events` goes from 3 to 4 and `sounds.json` from 2 to 3, violating A-13 and misaligning F-131's acceptance. Catch it early: during this phase `sounds.json`'s event count must still be 2, and the interception sound must be written as the `SoundEvents.SHIELD_BLOCK` constant rather than a `minecraft:` string.

### 4.6 What to report

- The per-item readings of `18` §11's #8 / #9 / #12 / #13 / #14: the 6-interception count sequence, the two-fungus and three-fungus same-instant readings, the 32 / 33-block audio comparison, the pass-through results for the 5 kinds of vanilla projectile, and a full-health player's health and coordinates before and after 6 interceptions.
- The two pieces of evidence for ally pass-through: the deployer themselves and a teammate throw once each, with `interceptCount` unchanged and no particles.
- The two distance readings of the impact sphere (applied at 6.0 blocks / not applied at a diagonal 8.4 blocks) and the evidence that the thrower themselves also gets the effect.
- The two readings of `n`: one hit (`n ≥ 1`, played once) and one empty throw (`n = 0`, not played).
- The registry self-check line (90; `entity_types` 13 / `sound_events` 3 / `block_entities` 6) and `sounds.json`'s event count (**2 → 3**, i.e. 3 after F-116 adds `friend_chives`).
- Open issues (especially whether `getEntitiesOfClass`'s predicate overload is usable, how `interceptCount` behaves after a chunk unload, and what `isAlly` returns when there is no team).

---

## 5. Phase 19 · Vibration Bean and the Two Reworks (F-122 ~ F-129)

> How to feed it: the full text of `20-PROMPT-MAIN-0.0.3.md` + this section, one Phase at a time.

### 5.1 Prerequisites

- All of Phase 16's exit criteria pass, especially `F-104` (`fang_trap_cactus`'s two new keys and three changed values, `zangief_cane`'s two new keys) and `F-103` (`damage_types` +1).
- `18` §3.6 ~ §3.11 (Vibration Bean's trigger and settlement, the unattached block, the Fang Trap Cactus rework, Zangief Cane's fall damage), §7 and §11 #10 / #11 / #15 / #16 have been read; `16` §5.2 / §5.3 / §5.4 / §5.5 / §5.7 have been read; `17` §6.4 / §7 / §8 / §12.5 ~ §12.9 have been read.
- The authorisation boundary is confirmed: v0.0.3 has **only two** places that may touch 0.0.2's existing content — Fang Trap Cactus (D-93 ~ D-95) and Zangief Cane (D-96); everything else in 0.0.2 stays exactly as it is (`16` §5.1 / §5.7).
- The 8-row difference table has been copied into the task book: 4 rows change (trigger distance ≤ 2.5, outer ring 4 ticks, inner ring 2 ticks, hit effect Slowness V for 60 ticks) and 4 rows stay (8 outer fangs, outer radius 2.0, 5 inner fangs, inner radius 1.0) — "unchanged" is a decision boundary too, and may not be adjusted on the side (`16` §5.3; `17` §7.4).
- An ordering reminder: inside this phase, **do F-123 before F-122** (`F-122`'s dependency column names `F-123`).

### 5.2 Goal

Three things. Vibration Bean: throw → place an "unattached" invisible block at the impact point → a non-ally player triggering at strictly less than 4.0 → **every player** within the radius (with no ally/enemy filtering) gets 80 ticks of Confusion → one explosion sound plays at the trap → silent removal with no drops. Fang Trap Cactus: change only the Chinese name plus four numeric values (trigger ≤ 2.5, outer ring 4 ticks, inner ring 2 ticks, Slowness V for 60 ticks), and define "a hit" as "actually damaged by this trap's fangs". Zangief Cane: during phase 2, cancel the natural fall damage the target receives and deal a fixed 7.5 points on landing. The last two are the only places in v0.0.3 authorised to change an existing implementation.

### 5.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-122 | Vibration Bean's projectile and the placement at the impact point | M / 1.5 | F-105, F-123 | `VibrationBeanProjectile#onImpact` places `farmstorm:vibration_bean` at the impact point (unattached mode, with the block entity reusing `DeployedCropBlockEntity`); the item is a `ThrowableCropItem` that **does not extend** `BlockItem` |
| F-123 | `DeployedCropBlock`'s "unattached" mode | L / 3.0 | F-103 | a new 5th **independent flag** `unattached`, orthogonal to `supportsFloating`: `canSurvive` is always true, `updateShape` returns itself, the FACE support check returns early, and no block update removes it; `PushReaction.BLOCK` follows the existing line |
| F-124 | Vibration Bean's trigger and settlement (strictly less than 4.0 at both ends) | M / 1.5 | F-123, F-111 | `VibrationBeanTrap`: a non-ally player with `distanceTo < 4.0` triggers it; every player within the radius gets 80 ticks of `confusion`; one `SoundEvents.GENERIC_EXPLODE` plays at the trap; it is one-shot, with `consume()` removing it silently after the trigger and no drops |
| F-125 | The Fang Trap Cactus rename: exactly one value changes in `zh_cn.json` | S / 0.5 | F-103 | the Fang Trap Cactus display-name value in `zh_cn.json` goes 「赢过输过没死过」→「刺球」; `en_us.json` changes by not one character; the key count and key set are unchanged |
| F-126 | Fang Trap Cactus' four numeric values: trigger ≤ 2.5, outer ring 4 ticks, inner ring 2 ticks | M / 1.5 | F-125, F-104 | `FangTrapCactusTrap`'s trigger distance reads `fang_trap_cactus.triggerRadius` (2.5) and the operator is **≤**; the inner ring's `innerDelayTicks` is 2 and the outer ring's `outerDelayTicks` is 4; the counts 5 / 8 and the radii 1.0 / 2.0 do not move |
| F-127 | Fang Trap Cactus' definition of "a hit" and Slowness V | L / 3.0 | F-126 | `FangTrapFangs`: the fang ownership table + the damage-event subscription, applying Slowness only to a player "actually damaged by this trap's fangs", with the duration from `slownessDurationTicks` 60 and `amplifier = slownessLevel − 1` = 4; applied once per player |
| F-128 | Zangief Cane: cancel natural fall during phase 2, a fixed 7.5 on landing | M / 1.5 | F-104, F-103 | `ZangiefPiledriverController`: phase 2 cancels natural fall (the criterion is "effect present + `amplifier == 2`", not global); the `onGround()` branch first deals the fixed `slamDamage` (7.5) points of `farmstorm:zangief_slam` and then does the existing silent removal |
| F-129 | Phase 19 exit regression | M / 1.5 | F-124, F-127, F-128 | one regression: `16` §5.3's 8 rows item by item, Vibration Bean's 3.9 / 4.1 boundary, and Zangief Cane's four heights and two states side by side |

Phase subtotal: **8 tasks, 14.0 person-days** (S 1 / M 5 / L 2).

### 5.4 Phase exit criteria

- [ ] **F-122: the block at the impact point**: how to verify: after throwing one Vibration Bean, read the block state with `/data get block`. What passing looks like: `farmstorm:vibration_bean` block + block entity generated at the impact point, with `unattached` true and `FLOATING` false (D-91).
- [ ] **F-122: it stays floating**: how to verify: make the impact point an air cell. What passing looks like: the block still stays floating in place, neither dropping nor vanishing by itself (its position is revealed by particles only).
- [ ] **F-122: no BlockItem mapping**: how to verify: `Item.BY_BLOCK.get(FarmstormBlocks.VIBRATION_BEAN)`. What passing looks like: it returns `null` (`18` §2.2 note 2) — so the loot table must point explicitly at the item, an acceptance item that lands in F-132.
- [ ] **F-122: it reuses the existing keys**: how to verify: read `throwable.lifetimeTicks` and `stack.throwable`. What passing looks like: still 100 and 16; this release adds no key at all for Vibration Bean.
- [ ] **F-123: dig out one block on each of the six faces**: how to verify: after placing it, dig out one block from each of the 6 neighbours (north, south, east, west, up, down). What passing looks like: the block is still in place and `FLOATING` is always false (`18` §11 #10).
- [ ] **F-123: chunk reload and pistons**: how to verify: walk 200 blocks away and come back; then push once with a piston arm. What passing looks like: the block and the block entity are still there and `owner` is readable; the piston cannot push it (RT-21 verification ② / ③).
- [ ] **F-123: orthogonal to `supportsFloating`, and a mismatched pair fails**: how to verify: regress each existing `supportsFloating` block (such as `blast_carrot`); then deliberately mismatch a block with a block entity. What passing looks like: not one existing block's behaviour changes; `kindOf` throws `IllegalStateException` for a non-`DeployedCropBlock` (A-11's fast-fail path).
- [ ] **F-124: one ruler at both ends**: how to verify: have a player walk to 3.9 blocks and to 4.1 blocks from the trap. What passing looks like: 3.9 blocks triggers (every player within 4 blocks gets 80 ticks of Confusion plus one explosion sound at the trap) and 4.1 blocks does not; `grep -c "4.0" behavior/impl/VibrationBeanTrap.java` is **1** (D-101).
- [ ] **F-124: one-shot, and three 0-hits**: how to verify: step on it again after triggering; then look at the construction calls. What passing looks like: silently removed with no drops and no further trigger; `new Explosion` and `hurt(` are 0 times each.
- [ ] **F-124: the trigger side and the effect side are asymmetric**: how to verify: the deployer walks in to 3.9 blocks; then have another player trigger it with the deployer standing within 4 blocks. What passing looks like: the deployer walking in does not trigger it; when someone else triggers it, the deployer does get Confusion (the reasoning paragraph of D-92).
- [ ] **F-125: the Chinese and English names**: how to verify: look at the block's and the item's display names in game. What passing looks like: both are 「刺球」, and the English is still `Fang Trap Cactus` (A-10; D-93).
- [ ] **F-125: not one registration identity moves, and where the diff lands**: how to verify: `git diff --numstat assets/farmstorm/lang/`. What passing looks like: 1 line added and 1 deleted, landing in `zh_cn.json` only, with `en_us.json` untouched; the registry id, the block id, `DeployedKind.FANG_TRAP_CACTUS`, the loot table, the fang entity and the ownership are all unchanged.
- [ ] **F-126: the trigger distance and the operator**: how to verify: stand at 3.0 blocks and at 2.5 blocks once each. What passing looks like: 3.0 blocks does not trigger and 2.5 blocks does (the operator is ≤, `18` §11 #15 first half; `16` §5.3).
- [ ] **F-126: the order and interval of the two rings**: how to verify: watch on which ticks the two rings of fangs appear when it triggers. What passing looks like: the 5 inner fangs first on tick 2 and the 8 outer fangs after on tick 4, 2 ticks between the rings.
- [ ] **F-126: four rows unchanged and reversible**: how to verify: look at the four rows of counts and radii with `git diff`; then set `triggerRadius` back to 3.0 and trigger once. What passing looks like: 8 / 5 and 2.0 / 1.0 have no changed lines; with it back at 3.0 the trigger distance returns to 0.0.2's behaviour; set it back to 2.5 after verifying.
- [ ] **F-127: Slowness V only for those who actually lost health**: how to verify: read `active_effects` for a player actually damaged by the fangs; then find a player swept by the animation who lost no health (invulnerability frames swallowed the damage, or another fang hit in the same instant). What passing looks like: the former gets Slowness V (`amplifier` 4) for 60 ticks, applied exactly once; the latter gets **none** (D-95).
- [ ] **F-127: ownership and the level conversion**: how to verify: look at `Owner` with `/data get entity`; set `slownessLevel` to 1 and to 5 and land a hit each. What passing looks like: `Owner` points at the deployer and the kill is credited to the deployer; `amplifier` 0 at level 1 and 4 at level 5; set it back to 5 after verifying.
- [ ] **F-128: exactly 7.5 on landing**: how to verify: fall naturally after the default 16-block lift and watch the health and the log at the moment of landing. What passing looks like: health drops by exactly 7.5 points (= 3.75 hearts), and there is no vanilla fall-damage line in the log (`18` §11 #16).
- [ ] **F-128: the control experiment and the death message**: how to verify: turn `cancelFallDamage` off and land once more; then look at the death message. What passing looks like: the damage is > 7.5 and varies with height (set it back to `true` after verifying); the death message shows `death.attack.farmstorm.zangief_slam`, and the damage is still attributed to the trap's owner.
- [ ] **F-128: nothing global changes and everything else stays**: how to verify: test the slow-falling / immune / flying cases once each; then rerun the phase timings. What passing looks like: none of the three is affected; the phase timings of 14 / 15 / 22 ticks, the 14.86-block lift, the +72 degrees per tick of yaw spin, the world-invisible state and the silent removal are all unchanged (`16` §5.5's "unchanged" row).
- [ ] **Phase-level**: how to verify: rerun the 8-row difference table + Vibration Bean's two ends + Zangief Cane's two heights and two states + the registry self-check. What passing looks like: the 4 changed and 4 unchanged rows match line by line; the operators at both 3.9 and 4.1 blocks agree and the block is still there after a chunk reload; neither of the two heights, 16 blocks and 4 blocks, shows "0 damage" or the double damage of "7.5 + natural fall"; `18` §11's #10 / #11 / #15 / #16 all pass, the registry self-check reads 90, `damage_types` 3 and `block_entities` 6.

### 5.5 Common failure modes

1. **Passing `supportsFloating` off as "unattached"** (RT-21 risk ②): mixing the two sets `FLOATING` to true, adds the 90° rotation to the model and still leaves `canSurvive` returning false, so the block drops as soon as it is placed. Catch it early: `unattached` is the **5th independent flag**, orthogonal to `supportsFloating`; accept by copying both readings ( `unattached` true, `FLOATING` false).
2. **The `unattached` branch missing from `updateShape`** (RT-21 risk ①): the symptom is that digging or placing any block nearby makes the trap vanish into thin air — and it throws nothing, it just "is gone somehow". Catch it early: after placing it, dig out one block on each of the 6 faces (north, south, east, west, up, down) and confirm face by face that the block is still there.
3. **Two 4.0s or two operators at the two ends** (D-101; RT-21 risk ⑥): with `< 4.0` on the trigger side and `<= 4.0` on the effect side, the boundary shows "triggered but no effect applied" or the reverse. The symptom: the behaviour exactly at 4.0 agrees with neither 3.9 nor 4.1. Catch it early: `grep -c "4.0" behavior/impl/VibrationBeanTrap.java` must be 1, and both ends share one config key and one `<`.
4. **Fang Trap Cactus writing only 2.5 and losing the ≤** (`16` §5.3's "the operator is part of the decision"): written as `<`, exactly 2.5 blocks does not trigger — and that is precisely the point acceptance looks at. Catch it early: test both points, 3.0 blocks not triggering and 2.5 blocks triggering; missing either one fails the item.
5. **Also adjusting the four "unchanged" rows on the side** (`16` §5.3; `17` §7.4): changing the counts to 6 / 10 while touching the delays, or "aligning" the radii to 2.5 / 1.5, both cross D-94's boundary. Catch it early: the four rows — 8 outer fangs, outer radius 2.0, 5 inner fangs, inner radius 1.0 — may not show a changed line in `git diff`.
6. **Giving Slowness to a player "swept by the animation but losing no health"** (D-95; RT-22 risk ④): using "am I inside the ring" as the hit lets Slowness land even when invulnerability frames swallow the damage, and the more it is fixed the more random it looks. Catch it early: acceptance must prepare two players — the one who lost health gets the effect, the one who did not does not; if the fang instance cannot be obtained, take RT-22 fallback ③ with the `Set<Integer>` + `getPersistentData()` mark and write the conclusion into the record.
7. **Zangief Cane cancelling only, or settling only** (RT-22 risk ① / ②): cancel only → 0 damage on landing; settle only → "the fixed 7.5 + natural fall", with the damage drifting with height and conflicting with "fixed". Both mistakes are visible only on a real client. Catch it early: land once each from the two heights of 16 blocks and 4 blocks; both readings must be 7.5, and the log must carry no vanilla fall-damage line.
8. **The cancellation's scope written too wide** (RT-22 risk ③): turning falling into a global cancellation drags in all three kinds of player — slow falling / immune / flying. Catch it early: the criterion can only be "effect present + `amplifier == 2`"; test the three cases once each.
9. **Changing the registry id and the English name along with the rename** (A-10; `16` §5.2): the symptom is that the loot table, the fang entity and the ownership chain all have to change with it, turning one rename into one refactor. Catch it early: `git diff --numstat assets/farmstorm/lang/` must show 1 line added and 1 deleted, in `zh_cn.json` only.

### 5.6 What to report

- The line-by-line readings of the 8-row difference table (4 changed: trigger ≤ 2.5 / outer ring 4 ticks / inner ring 2 ticks / Slowness V for 60 ticks; 4 unchanged: 8 outer fangs and radius 2.0, 5 inner fangs and radius 1.0).
- Vibration Bean's three sets of readings: 3.9 blocks triggering / 4.1 blocks not; `unattached` true and `FLOATING` false; the block and `owner` still there after a chunk reload.
- Zangief Cane's four sets of readings: the two heights of 16 and 4 blocks, the two states of `cancelFallDamage` on / off, and the health difference after the two landings (7.5 versus > 7.5).
- Fang Trap Cactus' two pieces of evidence: 2.5 blocks triggering and 3.0 blocks not; the player who actually lost health gets Slowness V (`amplifier` 4) for 60 ticks while the one swept without losing health gets none.
- The result of `git diff --numstat assets/farmstorm/lang/` (1 line added, 1 deleted, in `zh_cn.json` only).
- The readings of four greps: `4.0` 1 hit, `new Explosion` 0, `hurt(` 0, and `Item.BY_BLOCK.get(...)` returning `null`.
- The registry self-check line (90; `damage_types` 3, `block_entities` 6) and the artifact under `build/libs`.
- Open issues (especially whether the fang ownership table can get the instance, the cancellation semantics of `LivingFallEvent`, and Vibration Bean's behaviour at the chunk-reload boundary).

---

## 6. Phase 20 · Resources, Audio, Advancements and Acceptance (F-130 ~ F-135)

> How to feed it: the full text of `20-PROMPT-MAIN-0.0.3.md` + this section, one Phase at a time.

### 6.1 Prerequisites

- All three lines of Phase 17 / 18 / 19 have closed: the regression records of `F-113` / `F-121` / `F-129` are in hand, and `F-128` is done (its `damage_type` data json lands in the same batch as `F-132`).
- `F-102`'s 0.0.3 build baseline is in place; `F-104` has closed the config section at 155; the tool's state right now is "registry section 90, config section 155", with only the lang section left to reach 297 after `F-134` (`F-135` closes it).
- `18` §9.1 (8 PNG rows), §9.2 (1 OGG row + the full sound-event table), §9.3 (22 JSON rows + 5 places where existing files are appended), §9.4 (the seven-row subtotal of the 34 language keys) and §11 #17 have been read; `17` §10 (the three advancements and their trigger readings) and §4.7 (the placeholder textures are not final art) have been read.
- The audio pipeline's prerequisites: the source file `pengyoudejiu.mp3` (7,450,588 B, its file name kept unchanged) is in place, and `ffmpeg` sits at `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`, **not on PATH** (the script points at it with `-Ffmpeg`).
- The 4 `friend_chives_stage0..3.png` files are magenta `#D81E8A` placeholders and **not final art**; this phase does not require them to be finished (`17` §4.7; A-16).
- The three increment conventions are confirmed: 34 language keys = `item` 4 + `block` 3 + `effect` 2 + `subtitles` 1 + `farmstorm.configuration.*` 16 + `advancements` 6 + `death.attack` 2 (16 of which landed in F-104, so this phase continues with the remaining 18 and verifies the whole at 297).

### 6.2 Goal

Deliver in one go the three kinds of item the four preceding Phases left behind: **8 PNGs** (4 of them magenta placeholders), **1 OGG** and the extended media pipeline, **22 new JSON files** plus the appended entries in existing files, **3 advancements** and **34 language keys**; then run `18` §11's 17 self-checks and the four counts, and close `tools/verify_project.py`'s three expected values at **90 / 155 / 297**. At the end of the phase: the whole Chinese interface has no missing glyphs (Fang Trap Cactus displays as 「刺球」); the 8 new images are all in place; the hit sound can be heard; each of the three advancements pops its toast once; `dist/farmstorm-0.0.3+mc1.21.1.jar` is ready to release.

### 6.3 Task list

| ID | Title | Complexity / person-days | Depends on | One-sentence deliverable |
|---|---|---|---|---|
| F-130 | The 8 PNGs (4 of them magenta placeholders) | M / 1.5 | F-102 | `mob_effect/q_bounce.png` (18×18) + three item icons (16×16 each) + `block/friend_chives_stage0..3.png` (4 files at 16×16, magenta `#D81E8A` placeholders, not final art) |
| F-131 | Extending the media pipeline and `friend_chives.ogg` | M / 1.5 | F-102 | `tools/convert_media.ps1` extended from 0.0.2's two sounds and one image to this round's material; it produces the mono 64 kbps `sounds/friend_chives.ogg`; `sounds.json` gains 1 event (2 → 3) |
| F-132 | DataGen: the 19 new JSON files and 3 of the 5 appends to existing files | L / 3.0 | F-103, F-123, F-128 | the 19 new JSON files #1–#19 of `18` §9.3 (blockstate 3 + block models 6 + item models 4 + damage_type 1 + recipe 2 + loot_table 3) + 3 appends to existing files (`converter_fuel` +1 entry, `deployed_crops.json` and `mineable/pickaxe.json` +2 entries each) |
| F-133 | The three advancements | M / 1.5 | F-115, F-119, F-124 | the 3 advancement JSON files (`first_q_bounce` / `point_defense_intercept` / `vibration_bean_trigger`) and the three server-side `award` trigger points |
| F-134 | The 34 language keys (297 each in Chinese and English) | M / 1.5 | F-104, F-132, F-133 | `zh_cn.json` and `en_us.json` gain 34 keys each (`item` +4 / `block` +3 / `effect` +2 / `subtitles` +1 / `farmstorm.configuration.*` +16 / `advancements` +6 / `death.attack` +2), with the other three categories staying at +0; the tool's lang section expected value closes at 297 |
| F-135 | Full acceptance: §11's 17 items and the four counts | L / 3.0 | F-113, F-121, F-129, F-130, F-131, F-132, F-133, F-134 | an execution record for each of `18` §11's 17 self-checks; the tool's three expected values closed at 90 / 155 / 297 with the final output; `dist/farmstorm-0.0.3+mc1.21.1.jar` |

Phase subtotal: **6 tasks, 12.0 person-days** (S 0 / M 4 / L 2).

### 6.4 Phase exit criteria

- [ ] **F-130: eight PNGs**: how to verify: count the new PNGs under `textures/` and check their names against the 8 rows of `18` §9.1 one by one. What passing looks like: exactly **8**, with file names matching one by one (A-16).
- [ ] **F-130: the placeholder textures' colour**: how to verify: sample the pixel colour of `friend_chives_stage0..3.png`. What passing looks like: the colour is `#D81E8A`, and it is annotated there as "placeholder, not final art" (`17` §4.7).
- [ ] **F-130: the two "no new texture" cases line up**: how to verify: check whether block textures for `point_defense_fungus` and `vibration_bean` exist under `textures/`. What passing looks like: neither exists (the former borrows `warped_fungus`; the latter is an empty model with `particle` borrowing `cocoa_stage2`).
- [ ] **F-130: the effect icon's specification**: how to verify: read `q_bounce.png`'s dimensions. What passing looks like: 18×18 (D-53's effect-icon specification).
- [ ] **F-131: the OGG specification**: how to verify: `ffprobe` that ogg. What passing looks like: mono, 64 kbps (`18` §9.2's specification row).
- [ ] **F-131: the event count and the event's content**: how to verify: count the events in `sounds.json`; read the new event's four fields. What passing looks like: the event count is **3** (2 → 3); the new event's `subtitle` is `subtitles.farmstorm.friend_chives`, with a single sound, `stream: false` and `category: player`.
- [ ] **F-131: the script is still the same one**: how to verify: `git diff tools/convert_media.ps1` + rerun 0.0.2's two sounds and one image. What passing looks like: the diff contains only the added branch, and the old three paths still run.
- [ ] **F-131: it plays**: how to verify: `/playsound farmstorm:friend_chives master @s`. What passing looks like: the command succeeds and the event is visible in the completion list.
- [ ] **F-132: nineteen new JSON files**: how to verify: check each file name against #1–#19 of `18` §9.3; then look at `friend_chives_stage0..3`'s parent model. What passing looks like: all 19 files exist with the right names; the four stage models' parent is `minecraft:block/crop` with `render_type: cutout`.
- [ ] **F-132: the two converting recipes**: how to verify: read the two files under `data/farmstorm/recipe/`. What passing looks like: both are `farmstorm:converting`, bidirectional, 3 experience, no catalyst; `vibration_bean`'s counterpart is `minecraft:cocoa_beans` and `point_defense_fungus`'s is `minecraft:warped_fungus`.
- [ ] **F-132: Friend Chives has no converting recipe**: how to verify: look for its file under `data/farmstorm/recipe/`; then look at the `farmstorm:converter_fuel` tag. What passing looks like: there is no recipe file; it appears only in the tag (D-84).
- [ ] **F-132: the loot table points at the item**: how to verify: read `loot_table/blocks/vibration_bean.json`. What passing looks like: an explicit `LootItem.lootTableItem(FarmstormItems.VIBRATION_BEAN)` (writing `lootTableItem(block)` would resolve to air).
- [ ] **F-132: `damage_type`'s four fields**: how to verify: read `data/farmstorm/damage_type/zangief_slam.json`. What passing looks like: `message_id` is `farmstorm.zangief_slam`, with `WHEN_CAUSED_BY_LIVING_NON_PLAYER`, `exhaustion 0.0` and `HURT` matching character for character.
- [ ] **F-133: each of the three advancements pops once**: how to verify: trigger each of the three separately, then query with `/advancement`. What passing looks like: each pops one toast; the shape is `frame = task`, `show_toast = true`, `announce_to_chat = false`.
- [ ] **F-133: the parent advancements and the trigger readings**: how to verify: read the two parent advancements; then test each of the three rules once. What passing looks like: the parents are `farmstorm:root` (Q-Bounce) and `farmstorm:first_deploy` (the other two), with no new root node; `first_q_bounce` also advances when applied with `/effect`; `point_defense_intercept` advances only on the 1st non-ally interception (an ally passing through does not advance); `vibration_bean_trigger` also advances when an ally steps on it.
- [ ] **F-133: not one existing advancement changes**: how to verify: `git diff`. What passing looks like: it contains only the 3 new files; the advancement total goes 15 → 18.
- [ ] **F-134: two sets of 297 keys**: how to verify: count the keys of `zh_cn.json` and `en_us.json` separately, then compare the key sets. What passing looks like: **297** keys each, with identical key sets character for character (`18` §11 #17 second half).
- [ ] **F-134: the seven categories and the three +0s**: how to verify: count by prefix. What passing looks like: `item` 4 / `block` 3 / `effect` 2 / `subtitles` 1 / `configuration` 16 / `advancements` 6 / `death.attack` 2 = 34; `farmstorm.configuration.*` totals 155 (equal to the config key count); `tooltip.farmstorm.*` is still 16 and `entity.farmstorm.*` still 11.
- [ ] **F-134: the death messages and the Fang Trap Cactus name**: how to verify: read the two `death.attack.farmstorm.*` entries; then look at the Fang Trap Cactus display name in game. What passing looks like: both `zangief_slam` and `zangief_slam.player` are there; the Chinese is still 「刺球」 and the English still `Fang Trap Cactus` (A-10; this phase may not change it back).
- [ ] **F-135: the tool closed**: how to verify: `python -X utf8 tools/verify_project.py`. What passing looks like: `已核 registry 对象: 90 / 90`, no FAIL in the config section's **155** keys, `lang 键集一致，共 297 键` and `结论：FAIL 0 项`, with exit code 0 (A-14; `18` §11 #1).
- [ ] **F-135: the four startup self-check lines**: how to verify: start the server and read the log. What passing looks like: registry **90** (per category 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1), crops **22** (Throwable 12 / Deployable 7 / Edible 3, Demolition 4), lucky berry pool **19**, entity renderers **13 / 13**, all four lines OK (`19` F-135's "supplied by this document").
- [ ] **F-135: the seventeen items are satisfied**: how to verify: run them one by one per `18` §11. What passing looks like: #3 – #17 each have an observable record (Q-Bounce 3, Confusion 2, Point Defense Fungus 4, Vibration Bean 2, Friend Chives 1, Fang Trap Cactus 1, Zangief Cane 1, resources and language keys 1).
- [ ] **F-135: the four counts and the jar**: how to verify: count the resources + `jar tf dist/farmstorm-0.0.3+mc1.21.1.jar`. What passing looks like: PNG +8, OGG +1, JSON +22, 3 events in `sounds.json`; the jar contains `assets/farmstorm/sounds/friend_chives.ogg` and `assets/farmstorm/textures/mob_effect/q_bounce.png`.
- [ ] **F-135: the red lines rerun**: how to verify: two greps + `git diff docs/zh/`. What passing looks like: 0 `CustomPacketPayload` and 0 `Mixin`; the diff for documents 00 – 18 under `docs/zh` is empty (D-80; `19` F-135 item 6).
- [ ] **Phase-level**: how to verify: put everything above into a single delivery. What passing looks like: the tool at 90 / 90 + 155 + 297 + `FAIL 0 项`; resources 8 PNG + 1 OGG + 22 JSON; the jar present in `dist` (`19` §7's phase exit criteria).

### 6.5 Common failure modes

1. **Copying `lootTableItem(block)` into `vibration_bean`'s loot table** (F-132 item 4; `18` §2.2 note 2): that block has no `BlockItem` mapping, so the line resolves to air and "digging the trap up drops nothing", while DataGen reports no error. Catch it early: F-122 has already asserted that `Item.BY_BLOCK.get(...)` is `null`; F-132's acceptance requires this line to name `FarmstormItems.VIBRATION_BEAN` explicitly.
2. **Counting the PNGs as 7** (A-16's old value): what gets missed is Vibration Bean's item icon — and this is not a matter of "good-looking or not", because `basicItem`'s fallback branch always references `farmstorm:item/vibration_bean`, and missing it means a purple-and-black block at runtime. Catch it early: check the 8 file names against the 8 rows of `18` §9.1 one by one; one mismatch means one file short.
3. **Supplying a block texture for Point Defense Fungus or Vibration Bean** (`18` §9.1's two "no new texture" entries): once one is added the PNGs become 10, the account no longer matches 8, and A-16's acceptance goes red outright. Catch it early: those two names must show 0 hits under `textures/`.
4. **Treating the 4 magenta placeholders as final art, or deleting them as defects** (F-130 item 2; `17` §4.7): a sampled colour other than `#D81E8A` fails the item; deleting them leaves only 7 PNGs. Catch it early: sample the colour image by image and count the files — do both.
5. **Hitting the total with the language keys but not the breakdown** (F-134's deliverable): the total reaches 297 while `configuration` is 154 or `advancements` has only 4 — "total right, structure wrong", and `farmstorm.configuration.*` must always equal the config key count of 155. Catch it early: count by the seven prefixes, then compare `configuration` against 155 once.
6. **Advancement IDs using short names** (A-17's old wording `first_intercept` / `first_vibration_bean`): when the JSON file name and the ID disagree, `/advancement` cannot find it and no toast pops. Catch it early: check the three IDs character for character against `17` §10.1, with file names equal to the IDs.
7. **`ffmpeg` not on PATH** (F-131's deliverable): the script simply reports "ffmpeg not found", or some transcoder is picked that produces 44.1 kHz stereo. Catch it early: `ffprobe` must read out both mono and 64 kbps; reporting only "there is sound" does not pass.
8. **Turning the interception or explosion sound into a new `sound_event`** (A-13; D-100): `sound_events` goes from 3 to 4 and `sounds.json` from 3 to 4, colliding with A-13 and F-131's acceptance at the same time. Catch it early: the code may contain only constants such as `SoundEvents.SHIELD_BLOCK` and `SoundEvents.GENERIC_E*`, and no `minecraft:` string.
9. **F-134 "unifying" Fang Trap Cactus' Chinese name back to 「赢过输过没死过」** (A-10; F-134 item 5): regenerating the whole language file from the old English table while adding keys overwrites the Chinese name back. Catch it early: read the display name once in F-125 and once in F-134; both must be 「刺球」.

### 6.6 What to report

- The list of the eight PNGs' file names and the sampled colour result for the 4 placeholder textures (`#D81E8A`); `ffprobe`'s two readings for the ogg (mono / 64 kbps).
- `sounds.json`'s event count (3) and the raw text of the new event's four fields.
- The list of the 19 new JSON files' names, the two converting recipes' counterpart items and parameters, the raw text of the `vibration_bean` loot-table line, and the four fields of `zangief_slam.json`.
- The three advancements' IDs and file names, screenshots or observation records of the three toasts, the parent advancements, and the test results for the three trigger readings.
- The two language files' key counts (297 each) and the per-prefix counts for the seven categories; `tooltip.farmstorm.*` at 16 entries and `entity.farmstorm.*` at 11.
- The tool's final output (`90 / 90`, 155, `共 297 键`, `结论：FAIL 0 项`) and the exit code; the raw text of the four startup self-check lines.
- A per-item conclusion table for `18` §11's 17 items (ID / conclusion / where the evidence is); if any one is missing, write "not verified".
- The path of `dist/farmstorm-0.0.3+mc1.21.1.jar` and the two lines for the ogg and `q_bounce.png` in `jar tf`.
- Open issues (especially when the 4 placeholder textures will be finished, and any item in §11 that only "looks right" without a reading to show for it).

---

## 7. The Verbal Hand-off Between Phases (hand-off)

Template (replace the square brackets with the real content; send one at a time):

~~~text
The previous phase (Phase N · [title]) has passed acceptance against the exit criteria of 21-PROMPT-TASKS-0.0.3.md: [one sentence of evidence, for example "tool 77 / 77 with FAIL 0 项, registry self-check 90, config 155"].
This phase begins: Phase N+1 · [title].
Please follow 21-PROMPT-TASKS-0.0.3.md's "Phase N+1" section exactly through its six sub-sections: first confirm "投喂前必须已就绪" item by item and report back, then touch the first line of code; do only the F-xx tasks in that section's task list, do not implement the next phase early, and hand back per the report format of 20-PROMPT-MAIN-0.0.3.md when done.
~~~

Five ready-to-paste instances:

| Hand-off | One sentence you can paste directly |
|---|---|
| Phase 16 → 17 | The previous phase (Phase 16 · Baseline and Tool Fixes) is done: `tools/verify_project.py` produces `77 / 77`, config 139, lang 263, `结论：FAIL 0 项` and exit code 0 on the 0.0.2 baseline; on the v0.0.3 code the registry section reads 90 / 90; all four startup self-check lines are OK (registry 90 with the per-category split 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1, crops 22, pool 19); `farmstorm-server.toml` has 155 leaf nodes; `build/libs/farmstorm-0.0.3+mc1.21.1.jar` is in place. This phase begins Phase 17 · The Two Status Effects; please follow 21-PROMPT-TASKS-0.0.3.md's "Phase 17" section, confirm "投喂前必须已就绪" item by item before touching the first line of code, do only F-106 ~ F-113, and hand back per the report format of 20-PROMPT-MAIN-0.0.3.md. |
| Phase 17 → 18 | The previous phase (Phase 17 · The Two Status Effects) is done: `18` §11's #3 ~ #7 all pass (sitting pose + lowered by 0.35 blocks + about 480 degrees/second of rotation + a 6-tick squash cycle + the full seven-stage lock with ESC excepted; Confusion reverses direction only, and displacement is 0 when stacked); both `qbounce.poseMode` branches pass one run each, the losing branch's code is still present and the default is still `render`; the `CustomPacketPayload` and `Mixin` match counts are both 0. This phase begins Phase 18 · Friend Chives and Point Defense Fungus; please follow 21-PROMPT-TASKS-0.0.3.md's "Phase 18" section, do only F-114 ~ F-121, and hand back per 20's report format. |
| Phase 18 → 19 | The previous phase (Phase 18 · Friend Chives and Point Defense Fungus) is done: `18` §11's #8 / #9 / #12 / #13 / #14 all pass (the 6-interception count reads 1..6 in sequence with no drops on the 6th, three fungi in the same instant give only +1, the 32-block / 33-block audio comparison holds, all five vanilla projectiles pass through, ally projectiles pass through without spending a charge, and nothing follows an interception); the three subtotals `sound_events` 3, `block_entities` 6 and `entity_types` 13 are unchanged. This phase begins Phase 19 · Vibration Bean and the Two Reworks; please follow 21-PROMPT-TASKS-0.0.3.md's "Phase 19" section, do F-123 before F-122 internally, do only F-122 ~ F-129, and hand back per 20's report format. |
| Phase 19 → 20 | The previous phase (Phase 19 · Vibration Bean and the Two Reworks) is done: `18` §11's #10 / #11 / #15 / #16 all pass (the floating block survives digging out one block on each of six faces and survives a chunk reload, and a piston cannot push it; Vibration Bean triggers at 3.9 blocks and not at 4.1, with both ends sharing the same `<`; Fang Trap Cactus triggers at 2.5 blocks and not at 3.0, and Slowness V for 60 ticks goes only to those who actually lost health; Zangief Cane deals exactly 7.5 points on landing, and with `cancelFallDamage` off the damage varies with height); `16` §5.3's 8-row difference table shows 4 changed and 4 unchanged; `damage_types` 3. This phase begins Phase 20 · Resources, Audio, Advancements and Acceptance; please follow 21-PROMPT-TASKS-0.0.3.md's "Phase 20" section, go internally in the order F-130 / F-131 → F-132 → F-133 → F-134 → F-135, and hand back per 20's report format. |
| Phase 20 → wrap-up | The previous phase (Phase 20 · Resources, Audio, Advancements and Acceptance) is done: tool 90 / 90, config 155, lang 297, `结论：FAIL 0 项`; resources 8 PNG + 1 OGG + 22 JSON, 3 events in `sounds.json`; both language files at 297 keys with identical key sets character for character; each of the three advancements popped its toast once; each of `18` §11's 17 items left a reading; `dist/farmstorm-0.0.3+mc1.21.1.jar` has been produced. Now run the "whole-project closing prompt" of 21-PROMPT-TASKS-0.0.3.md §8 once as a pre-release self-check: check and fix only, add no feature. |

---

## 8. The Whole-Project Closing Prompt (feed it after Phase 20 passes)

~~~text
Project wrap-up. Please do one complete delivery check on farmstorm v0.0.3: check and fix only, add no feature.

1. First run the fixed number-taking tool (RT-23's closing condition, A-14)
   Run python -X utf8 tools/verify_project.py; the three expected values must be 90 / 155 / 297 and must not fall back to 77 / 139 / 263:
   ① the registry section outputs "已核 registry 对象: 90 / 90", with the 12 per-category subtotals 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1;
   ② the config section has 155 keys with no FAIL, and its key set matches run/config/farmstorm-server.toml character for character;
   ③ the lang section outputs "lang 键集一致，共 297 键".
   It only passes when all three sections are green, "结论：FAIL 0 项" and the exit code is 0. If any section is red, stop and report — do not declare it releasable.

2. Build
   Run .tools\gw.cmd build (do not call gradle directly) and confirm exit code 0 and the artifact build/libs/farmstorm-0.0.3+mc1.21.1.jar;
   unpack it and confirm that the version resolves to 0.0.3 and the modId is farmstorm in META-INF/neoforge.mods.toml.

3. What the jar must contain
   jar tf dist/farmstorm-0.0.3+mc1.21.1.jar and confirm that all 8 PNGs and the 1 ogg are there:
   assets/farmstorm/textures/mob_effect/q_bounce.png,
   assets/farmstorm/textures/item/friend_chives.png, chive_seeds.png, vibration_bean.png,
   assets/farmstorm/textures/block/friend_chives_stage0.png ~ stage3.png;
   assets/farmstorm/sounds/friend_chives.ogg and assets/farmstorm/sounds.json.
   Name them one by one and stop and report if any one is missing. Do not judge the ogg by its file name alone: run ffprobe once and confirm vorbis, mono, about 64 kbps.
   Then confirm that the two "no new texture" ones really are absent: there is no block texture for point_defense_fungus or vibration_bean under textures/.

4. The four counts and the language keys
   PNG +8, OGG +1, JSON +22, sounds.json 3 events (check names one by one against 18-TECH-0.0.3.md §9.1 / §9.2 / §9.3);
   both language files at 297 keys each with set(zh) == set(en) being True; count by the seven prefixes:
   item 4 / block 3 / effect 2 / subtitles 1 / farmstorm.configuration.* 16 (totalling 155, equal to the config key count) / advancements 6 / death.attack 2 = 34;
   tooltip.farmstorm.* still 16 entries and entity.farmstorm.* still 11;
   the 4 friend_chives_stage*.png are #D81E8A placeholders, not final art — say so in the report and do not treat them as finished art.

5. Trigger each of Q-Bounce's two branches once
   With qbounce.poseMode = render, apply Q-Bounce to a zombie: it takes the riding pose, is lowered by 0.35 blocks, rotates at about 480 degrees/second on a 6-tick squash cycle, the AI is still walking and the hitbox is unchanged;
   then switch to mount and do it again: the look is the same, RootVehicle in the entity data points at minecraft:armor_stand, and after the creature dies that ArmorStand is no longer in the world;
   in both runs press W / A / space / sneak / left click / right click / scroll wheel / a number key once each and confirm that nothing responds, only ESC opens the pause menu, and the lock log prints exactly once.
   If either branch fails to run, stop and report — both branches must exist, and the losing one is only switched off, not deleted.

6. Point Defense Fungus' "allies are not intercepted"
   Have the deployer themselves and a teammate throw one Friend Chives each: both must pass through (their landing settles as usual), with interceptCount unchanged and no particles;
   then have the other side throw one: it is intercepted, interceptCount +1, particles appear, one sound plays;
   after 6 throws in a row the block disappears with no dropped items, and an intercepted projectile has no follow-up on landing (no explosion / no block written / no effect applied / no drops).
   Throw a vanilla arrow / snowball / trident / ender pearl / farmstorm:turret_bullet once each; they must all pass through too.

7. The 17 acceptance self-checks
   Open docs/zh/18-TECH-0.0.3.md §11 and run #1 ~ #17 one by one, outputting a 17-row check table,
   three columns per row: ID / conclusion / where the evidence is (a log line, a command return value or a file path).
   #1 and #2 are character-for-character comparisons against the tool and the startup log, #17 is the resource and language-key counting, and #3 ~ #16 are manual tests.

8. Three red lines
   grep -r "CustomPacketPayload" src/main/java and grep -rn "Mixin" src/main/java both return 0 places;
   git diff for docs/zh 00 ~ 18 and for docs/en is empty (D-80; 19 ~ 21 are new v0.0.3 files and are not in the frozen zone);
   give one piece of "unchanged" evidence each for the fields of the existing 19 CropProfile entries, the defaults of the existing 139 config keys (except the 3 that D-94 authorises),
   the existing 15 advancements and the existing 21 converting recipes (git diff shows added lines only, or the corresponding file's diff is empty).

9. Check the 36 tasks against the definition of done one by one
   Open docs/zh/19-TASKS-0.0.3.md and output a 36-row check table for F-100 ~ F-135,
   four columns per row: task ID / title / achieved or not / where the evidence is.
   Judgement rests on the definition of done of 20-PROMPT-MAIN-0.0.3.md and the three gates of 21-PROMPT-TASKS-0.0.3.md §0.3,
   and then the task's acceptance assertions in 19-TASKS are matched one by one; missing one means "not achieved".
   If any row is "not achieved", stop and report — do not declare it releasable.

10. Report format
   Output per the report format of 20-PROMPT-MAIN-0.0.3.md, plus four extra small tables: the resource inventory table, the four-count table, the 17-item self-check table and the 36-row task check table.
   Do not paste complete logs (for each command paste only the conclusion line and the first error); write "not verified" for anything that was not run or not verified,
   and never write it up as "passed".
~~~

Three criteria at closing time (reviewed by human eyes, not by the agent's self-report):

1. The tool's three expected values really are **90 / 155 / 297**, and the 12 per-category subtotals add up to 90 while `farmstorm.configuration.*`'s entry count and the config key count are both 155 (A-14; RT-23). If the tool is still red, it may not be released.
2. The resource inventory table's three categories add up to **8 + 1 + 22 = 31**, with every row pointing at a real file; the 4 placeholder textures are explicitly marked "not final art" instead of being mixed in with the final ones (A-16; A-18).
3. The task check table has **36 rows** with 0 "not achieved"; all **17** of `18` §11's items are green with an evidence location each — #5 (the full seven-stage lock) and #8 (allies are not intercepted) must be named explicitly.

---

## 9. Consistency Between This File and 19-TASKS-0.0.3.md (self-check record)

| Phase | `19-TASKS` task range | `19-TASKS` task count | Tasks carried into §x.3 of this file | Equal? |
|---|---|---|---|---|
| Phase 16 | F-100 ~ F-105 | 6 | 6 | Equal |
| Phase 17 | F-106 ~ F-113 | 8 | 8 | Equal |
| Phase 18 | F-114 ~ F-121 | 8 | 8 | Equal |
| Phase 19 | F-122 ~ F-129 | 8 | 8 | Equal |
| Phase 20 | F-130 ~ F-135 | 6 | 6 | Equal |
| **Total** | **F-100 ~ F-135** | **36** | **36** | **Equal (the difference set is empty)** |

The four other self-checks:

| Check | Method | Conclusion |
|---|---|---|
| Task IDs and titles | extract the 36 `#### F-xx · title` entries from `19-TASKS-0.0.3.md` and compare them line by line with the "ID + title" of §2.3 ~ §6.3 here | 36 / 36 identical character for character (the numbering is consecutive: `135 − 100 + 1 = 36`, with no gaps and no duplicates) |
| Complexity and person-days | check S / M / L and the converted person-days phase by phase, then add them up against the subtotals of `19` §2 | 7.5 + 14.0 + 12.5 + 14.0 + 12.0 = **60.0 person-days**; S 6 / M 22 / L 8 with identical counts; **75.0** with the 25 % buffer |
| The dependency column | compare line by line with each task block's "dependencies" field | 36 / 36 consistent, including the three cross-phase prerequisites (`F-126` waits for `F-104`, `F-132` waits for `F-128`, `F-133` waits for `F-115` / `F-119` / `F-124`) and the reversed numbering inside Phase 19 (`F-123` before `F-122`) |
| Citation of adjudications | this file cites only the adjudication IDs (A-10 ~ A-18) and does not copy the table text of `19` §0.4 | consistent with the single-source convention; the nine entries of §0.4 are rewritten as "old wording / correct wording" with not one character of the conclusions changed |

Maintenance agreement: if `19-TASKS-0.0.3.md` revises a task's title, dependencies or person-days, this section and §2.3 ~ §6.3 have to run the comparison again; this file does not change titles on its own, does not change IDs on its own, does not add tasks on its own, and does not change the 60.0 person-day total.

---

## Translator's notes

1. **The `~~~text` fences are kept exactly as in the source.** Both blocks (the hand-off template in §7 and the whole-project closing prompt in §8) use `~~~text` … `~~~`, not triple backticks. Only the prose inside them is translated; the fenced bodies remain plain-text prompts to be pasted verbatim.
2. **§2 ~ §6 section titles.** Every Phase has exactly six second-level subsections, titled uniformly "x.1 Prerequisites / x.2 Goal / x.3 Task list / x.4 Phase exit criteria / x.5 Common failure modes / x.6 What to report", mirroring 投喂前必须已就绪 / 本阶段目标 / 任务清单 / 本阶段出口判据 / 常见翻车点 / 完成后应向用户回报 in the source.
3. **Chinese strings that are program output or in-game display text stay in Chinese**, character for character: the four startup self-check lines, `已核 registry 对象: …`, `lang 键集一致，共 297 键`, `结论：FAIL 0 项`, and the display name 「刺球」 (whose rename is the whole subject of A-10). Translating them would make the acceptance criteria unverifiable — and 「刺球」 must literally be what a player sees.
4. **The task-book hand-off sentence keeps the Chinese requirement wording** inside quotation marks ("投喂前必须已就绪"), because it is the literal string the coding agent is told to look for in the document; the English heading §x.1 renders the same requirement as "Prerequisites".
5. **`man瓜` does not occur in this document**; where §7's hand-off text lists commands, the `farmstorm:turret_bullet` id is kept exactly as written rather than being given a display name.
6. **A-18's arithmetic is kept verbatim** — "31 and 9 are not added", "6 of which already appear in sub-table 3 as files", "the two '0 uncovered' claims each hold on their own" — with no attempt to make the two conventions comparable.
7. **Person-day values** 60.0, 75.0 and 18.0, and the task distribution 6 / 8 / 8 / 8 / 6, are copied unchanged in every place they appear (the header table, §0.5, §1, each phase subtotal and §9).
