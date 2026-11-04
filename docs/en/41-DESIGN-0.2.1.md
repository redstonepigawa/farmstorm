# 41 · v0.2.1 Design Delta

> Project: Farmstorm · Version **v0.2.1**, baseline **v0.2.0's published state** · Basis: `docs/zh/40-DECISIONS-0.2.1.md` (D-173 ~ D-186, **14** in total) + `.work/v021-BRIEF.md` (the fact card) + `plan0.2.1.txt` (23 lines, **4 appended original items**, split by this document into **5 requirement items R1 ~ R5**; the split measure is in §1) · Relation to 0.2.0: a **delta**; it neither changes `docs/zh/35-DESIGN-0.2.0.md` nor restates it (it only borrows its format).

| Item | Value |
|---|---|
| Document | 41-DESIGN-0.2.1 |
| Status | Design final (the last version before the six Chinese documents are frozen) |
| Upstream (single source of truth) | `40-DECISIONS-0.2.1.md`: the measure is taken from it alone; this document is responsible only for "what the requirement looks like" |
| Upstream (the author's original text) | `plan0.2.1.txt`: the 4 appended items are at `:9`, `:10`, `:14`, `:18`, `:22` (item 1 occupies two lines, inside `:7-11`) |
| Upstream (factual base) | `.work/v021-BRIEF.md`: §0 baseline, §1 source-code facts (with `file:line`), §2 the author's rulings, §3 the increment list, §4 risks RT-51 ~ RT-60b |
| Downstream | `42-TECH-0.2.1.md` (registry-surface recheck, class design, config 176, acceptance), `43-TASKS-0.2.1.md` (tasks and scheduling) |
| Language | Body text in Chinese; code identifiers, registry names and config keys always in English |

---

## 0. Reading conventions

### 0.1 The division of labour between this document and 40

40 fixes the **measure** (a one-sentence conclusion + grounds + landing position + knock-on items); this document writes **what it looks like** (trigger conditions, period and phase, sequence, range, participation surface, what the player sees, boundaries). Four rules of the division:

1. **The measure is not invented here.** Every conclusion in this document has a decision number (`D-17x`) hung behind it; if a reader finds a sentence that cannot be traced back to a decision number and is not a derivation marked [author's supplement], that is a defect of this document, not a blank that may be filled in by one's own interpretation.
2. **40's body text is not restated.** This document cites numbers rather than copying conclusion sentences; where the reader should "read item so-and-so of 40 first", the number is written out directly.
3. **What is not here**: the full config table and the list of key names (42), class design and method signatures (42), the **NBT structure** (42), task numbers and scheduling (43), the acceptance table itself (42). This document names config keys and defaults only in the "Basis" column.
4. **Conflicts with the source are reported to the author.** Where, during writing, a place was found where 40 or the fact card does not match the source code, it is listed item by item in the hand-over notes, and **the upstream is not changed**; this document is written per 40, and leaves a "per 40's measure" note in the corresponding subsection.

### 0.2 The test for "[author's supplement]"

| Type | May be marked "[author's supplement]" | Explanation |
|---|---|---|
| Arithmetic | yes | a third number computed from two given numbers; the expression must be written out (e.g. `100 ÷ 20 = 5 seconds`, `7 × 3 = 21`, `16 × 3 = 48`) |
| Explicitly authorised by 40 | yes | for items 40 writes as "fixed by 42 within the framework", this document may first pin down the **observation surface**, while the implementation form is still left to 42 |
| A reading the upstream does not cover | yes | it must come with "why it can only be read this way", and be registered in §9's boundary list |
| Given by the fact card, not yet ruled on by the author | marked "[card supplement (vetoable)]" | e.g. `friend_chives.effectTicks` defaults to 200 ticks, `qbounce.poseMode` defaults to `render` |
| Conflicting with 40 or the fact card | **not allowed** | a conflict can only be reported back; one may not pick one side and write it in |
| Aesthetic preference | **not allowed** | "I think it looks good" is not a reason; a decidable criterion must be given |

A number in this document has only four legitimate sources: it is written in 40, it is written in the fact card, it is arithmetic with the expression written out, or it is marked [author's supplement] with a reason given. Anything that is none of the four is a document defect.

### 0.3 Markup

| Markup | Meaning |
|---|---|
| [D-17x] | the conclusion comes from the corresponding decision in 40 |
| [card §1.x] | the conclusion comes from the corresponding subsection of the fact card |
| [`path:line`] | a line taken directly from the source code; paths are relative to `src/main/java/com/dashpig/farmstorm/` (resources and tools are noted separately) |
| [author's supplement] | derived by this document under the tests of §0.2 |
| [card supplement] | a reading the fact card already gives but the author has not ruled on separately (vetoable) |

**The convention for ruling numbers**: `BRIEF` §2 and 40's §2 index table both register **#1 ~ #12**; this document additionally cites **#13 ~ #15** (the three the author fixed during the writing of 41: an ownerless core does not rebind / the preference order for rebinding / the original AI flag persisted with the entity). A cited ruling quotes only the original text of the "ruled as" column and does **not** change 40's decision numbers.

### 0.4 Units and counting conventions

1. **Time** is measured in game ticks (tick), **1 second = 20 ticks**. Two words in this document must be kept apart: **period** (how often an attempt is made, 100 ticks) and **window** (how long a state lasts, 80 ticks / the effect's lifetime). The two are not converted into one another and are not derived from one another.
2. **Range** is measured in chunks: the core's influence range = the **3 × 3 chunks** centred on the chunk the core is in (Chebyshev distance ≤ 1, `RANGE_CHUNKS = 1`, [`OutpostCoreBlockEntity:75,454-465`]), side length `16 × 3 = 48` blocks, **9** chunks in total.
3. **"16" is an allowance** (`outpost_core.cultivatorCapacity`, default 16), **"54" is slots** (`outpost_core.storageSlots`, default 54); neither number changes in this version, and confusing the two would write "rebind" as "stuff something into the chest".
4. **"Non-player mob" and "hostile mob" are not synonyms**: the former = `Mob` ∧ ¬`Player` (the object of R3 / R4), the latter = `Enemy` ∪ `MobCategory.MONSTER` (the object of R5, D-182). Any place that swaps the two words is wrong.
5. **"The AI is deleted" = `Mob#setNoAi(true)`**, not a loose phrase for "make the mob stop moving"; it does not change damage, health, drops, collision or knockback, and **it has restore semantics** (D-179, author's ruling #8).

### 0.5 The 14 decisions → this document's sections

This table is a **reverse index**: from any one D number a reader can find "where it lands in this document, and as what". The 14 run consecutively with no gaps, and every one of them has a landing place.

| Decision | Topic | Where it lands here |
|---|---|---|
| D-173 | version 0.2.1, baseline 0.2.0 | header, §0.4 |
| D-174 | the scope gate: only the 4 appended items | §1 the split measure, §8 the reading, Appendix A |
| D-175 | registry surface +0 ⇒ 97 | the note under the §1 table, §7 row 32 |
| D-176 | rebind: 100 ticks / phase spread / scan loaded chunks only / fill only when not full | §2.2, §2.3, §2.4 |
| D-177 | the rebind's scan surface and write boundaries | §2.5, §2.6, §2.7, §9 |
| D-178 | tooltip: guarantee the default chain + acceptance | the whole of §3 |
| D-179 | the original-AI-flag table: record the original value on entry, restore it on exit | §4.3, §5.3, §9 |
| D-180 | Vibration Bean: every non-player mob in range for 80 ticks | the whole of §4 |
| D-181 | Q-Bounce: delete the AI for the effect's lifetime; skip in mount mode | the whole of §5 |
| D-182 | the hostile predicate `Enemy` ∪ `MobCategory.MONSTER` | §6.3 |
| D-183 | `trap.damageMultiplier` defaults to 3.0 | §6.2, §7 |
| D-184 | the ×3's effect surface: 2 out of 4 | §6.4, §6.6, Appendix A row 1 |
| D-185 | the numbers surface 176 / 338 / 97 | the end of §6.2, §7 |
| D-186 | red lines: 0 Mixin, 2 packets and no new one | the note under the §1 table, §7 row 33 |

One reading rule: **D numbers appear only in this table and in the "Grounds for this section" line of each section; in body text they are always written out in full** ("D-180 supplementary item" rather than "item 180"), so that cross-document searching does not confuse them with task numbers or test numbers.

---

## 1. Overview of the requirements (5, one table)

| No. | Topic | Type | Decision | plan line | One-line outcome |
|---|---|---|---|---|---|
| R1 | automatic rebinding by the core | addition | D-176, D-177 | `:9` | every 100 ticks at runtime, staggered by a coordinate hash, the core fills its 16-entry allowance with the "wanted by nobody" cultivators of the loaded 3 × 3 chunks; it stops when full and takes no bound one |
| R2 | the core storage shows item details on hover | guarantee + acceptance | D-178 | `:10` | in the 54-slot screen, hovering the mouse over an item shows its name and the enchantment / effect lines; the default chain should show it anyway, and this version writes it into the requirement and the acceptance |
| R3 | the Vibration Bean deletes the AI of non-player mobs | addition | D-179, D-180 | `:14` | at the instant it triggers, **every non-player mob** in the radius stops for **80 ticks (4 seconds)**, then is restored to the value it had before entry; players still get only CONFUSION |
| R4 | Q-Bounce deletes the AI of non-player mobs | addition | D-179, D-181 | `:18` | a non-player mob carrying Q-Bounce stops for the **whole lifetime of the effect** and is restored to the original value the instant the effect ends; mount mode skips |
| R5 | traps deal ×3 damage to hostile mobs | numeric adjustment | D-182, D-183, D-184 | `:22` | of the 4 trap crops **only 2** (`blast_carrot`, `zangief_cane`) multiply by `trap.damageMultiplier` (default **3.0**) against **hostile mobs**; players and non-hostile mobs are unchanged |

**How 4 original items become 5 requirement items**: plan item 1 at `:9` (rebinding) and `:10` (tooltip) is **two distinct acceptance faces** — one inside a server-side block entity, one on a client-side screen — so it is split into **R1 / R2**; item 2 → **R3**; item 3 → **R4**; item 4 → **R5**. So `4 → 5` separates the two acceptance faces of item 1; it does not add one more requirement. Merged back, they are plan's 4 items (D-174's scope gate).

Three points to take from the table:

1. **R3 and R4 are both "delete the AI", but they are not one thing**: R3 is **a one-shot trigger + a fixed 80-tick window** (it restores when the time is up); R4 is **state-driven + with no fixed duration** (delete while the effect is there, restore the moment it is gone). The two restore criteria are completely different and must not be merged into one table (D-180 supplementary item, D-181 supplementary item).
2. **R1 and R2 both live on the "Outpost Core", but one is server-side and the other client-side**: R1 changes when a binding is written, R2 may not need a single line of rendering code changed. The two have no overlap in how they are accepted (§2.7, §3.4).
3. **R5's "participation surface" is the place in this version most easily written wrong**: plan's original text is "all trap crops", narrowed by author's ruling #11 to **2**. Any downstream sentence saying "all traps ×3" is wrong (RT-58, Appendix A row 1).

**None of the five requirements lives on the registry surface**: rebinding uses the existing `OutpostCoreBlockEntity` + `CultivatorBlockEntity`, the tooltip goes through the vanilla chain, the original-AI-flag table and the hostile predicate are runtime data / utility classes, and the multiplier is a config key ⇒ the registered objects are still **97**, custom packets are still **2**, Mixin is still **0** (D-175, D-186; [card §3.1, §0.2]).

---

## 2. R1 Automatic rebinding by the core

**Grounds for this section: D-176, D-177 (author's rulings #2, #12).**

### 2.1 One-line positioning

At **runtime**, every **100 ticks**, the core looks inside its own loaded 3 × 3 chunks for one "wanted by nobody" cultivator and fills it into its 16-entry allowance — this is the **third binding write point** (the first two: cultivator placement `CultivatorBlock:85-102`, migration when the core is broken `OutpostCoreBlockEntity:686-690`). Rebinding **only adds, never takes away**: it removes no bound entry, changes none and moves none, and does not re-select the core either.

### 2.2 Trigger condition: rebind only while the binding count < 16

| Item | Rule | Basis |
|---|---|---|
| Source of the allowance | `outpost_core.cultivatorCapacity`, default **16**, range 1 ~ 64; the only read site `OutpostCoreBlockEntity:344` | [`FarmstormConfig:949-951`]; D-177 |
| Trigger condition | scanning starts only while the current binding count is **< the allowance**; already full ⇒ this round **returns immediately**, not one is rebound | D-176, D-177 |
| How the capacity check is read | use `cultivatorCount()` (`:334`) or `isCultivatorListFull()` (`:343`); do **not** use `cultivators()` (`:328`) — it calls `pruneCultivators()` (`:380-391`) first and may `setChanged`, turning "just asking whether there is room left" into a write operation | D-176 supplementary item |
| How "16" is written | the body text writes "default 16", the implementation reads config; **16 is never hard-coded**, otherwise when config is changed to 8 or 64 rebinding and the placement check would diverge | D-177; [`FarmstormConfig:951`] |
| At most how many per round | from 0 up to the allowance, **no per-round cap**: `16 − 0 = 16` is this version's theoretical maximum increment per round [author's supplement: arithmetic] | D-177 |

### 2.3 Period and phase: 100 ticks, an **independent phase gate**

| Item | Rule | Basis |
|---|---|---|
| Period | **100 ticks** (= 5 seconds, `100 ÷ 20`) [author's supplement: arithmetic] | `plan0.2.1.txt:9`; D-176 |
| Phase | spread over **0 ~ 99** ticks by the **core's coordinate hash** (`100 − 1 = 0~99`) [author's supplement: arithmetic] | author's ruling #2; D-176 |
| Reference form | **the same shape** as the existing particle gate: `floorMod(now + floorMod(pos.hashCode(), 100), 100) == 0` (the expression at [`OutpostCoreBlockEntity:516-518`], with the modulus changed to 100); the constant name and whether `pos.hashCode()` is reused are fixed by 42 | D-176; [card §7 item 1] |
| Source of the period | **a constant 100**, not a config key. This version's config adds only +1 key (`trap.damageMultiplier`, D-183); opening another key for the rebind period would make it **177**, in direct conflict with D-185's 176 | D-183, D-185; [card §3.2] [author's supplement: the reason no key is added] |

**⚠️ The implementation trap in this item, and also the most important sentence in this section**: `serverTick` (`:507-525`) does only three things today — return if not a `ServerLevel` → return if `interval <= 0` (`:511-513`) → **`return` if the particle phase gate at `:516-518` is not met** → boundary particles + one log line every 30 rounds. If rebinding is attached **after** that `return`, the real period gets filtered into `LCM(40, 100) = 200` ticks (40 is the default of `outpost_core.boundaryParticleIntervalTicks`, [`FarmstormConfig:939-941`], and config can change it to 20 ~ 1200), which means "every 100 ticks" fails on the spot and then drifts with the configuration.

⇒ **The two phase gates must be evaluated independently** (42 orders them, but "100 ticks does not depend on the particle interval" is a hard constraint, D-176 supplementary item).

| Item | Particle gate (existing, unchanged) | Rebinding gate (this version) |
|---|---|---|
| Position | `OutpostCoreBlockEntity:516-518` | inside `serverTick`, **side by side** with this gate, and not after its `return` |
| Source of the period | `outpost_core.boundaryParticleIntervalTicks`, default 40, configurable 20 ~ 1200 (`:939-941`) | **a constant 100 ticks** |
| Modulus | `interval` (configurable) | **100** (fixed) |
| Phase | the coordinate hash modulo `interval` | the coordinate hash modulo **100** |
| Result | one ring of boundary particles every 40 ticks | one rebind attempt every 100 ticks |

**The sense of time on the player's side** (all three numbers are arithmetic, no further number is set): after the core is placed, the first rebind attempt happens **within 100 ticks (5 seconds) at worst** (phase offset 0 ~ 99 ⇒ the first trigger falls within 1 ~ 100 ticks of placement); afterwards once every 100 ticks.

### 2.4 Range and scan surface: 3 × 3, loaded chunks only

| Item | Rule | Basis |
|---|---|---|
| Range | Chebyshev distance ≤ 1 from the core's chunk ⇒ **3 × 3 chunks**, side length 48 blocks, **9** chunks in total | [`OutpostCoreBlockEntity:75,454-465`]; D-176 |
| Loaded chunks only | chunks are taken with `getChunkNow`; if none can be got ⇒ **this round skips that chunk**, no loading, no error, no failure counted | author's ruling #2; D-177 supplementary item; the existing form `selectCore:296-300`, `pruneCultivators:384-387` |
| How to enumerate | walk `LevelChunk#getBlockEntities()` and accept only `CultivatorBlockEntity` | [card §1.1]; the only existing precedent `DeployedCropBlockEntity:230-234` |
| The precedent can only be copied in half | that place uses `level.getChunk(...)` (`:228`, which **does load**); rebinding requires no loading ⇒ copy only the walk at `:230-234`, not the chunk fetch at `:228` | [`DeployedCropBlockEntity:227-236`]; author's ruling #2 |
| No precedent | "enumerate block entities by area" has **0** occurrences in the whole repository: `ChunkMap` 0, `getBlockEntities()` only the 1 above; `getAllEntities()` only 2 and neither is a block entity | [card §1.1] |

**"Loaded chunks only" rarely skips anything in normal operation** ([author's supplement], grounded in the source, not a new measure): the 1 chunk the core itself is in holds a ticket of its own, `distance = 1` ⇒ **the centre chunk ticks and the 8 adjacent chunks are loaded to FULL** ([`outpost/CoreChunkTickets:17-30,43-44`]). That is, the 3 × 3 and the ticket's coverage **coincide exactly**, so "skip the unloaded ones" is a **defensive branch**, and there are only three real sources:

1. the core has just been placed / just restarted and the ticket has not taken effect yet (only `setOwner:418` requests a ticket; after a restart `restoreAll` makes it up);
2. the block entity itself is ticking, but an adjacent chunk has been demoted because of memory pressure;
3. a core produced by `/setblock` (no `setOwner`, no ticket).

**Conclusion**: the rebind's scan surface is "3 × 3 minus the chunks unreadable this round", **not** "3 × 3 minus 8 chunks that can never be read".

### 2.5 Candidate criteria and the two-side write

**The executable criterion for "unbound" in this version** (D-177 supplementary item gives the only form; this document copies it and sets up no second one):

| Form | How it is judged | Adopted in this version |
|---|---|---|
| Strict form (global, on the core side) | walk **all** 16 entries of every allied core and confirm that none points at it | **No**: the repository has no "cultivator → core" global index; for the cost of a full scan per round over cores × 16, `OutpostCoreBlockEntity:326` has already recorded "per tick × 16 entries × the number of cores, the cost is out of proportion" for a judgement of the same kind |
| **This version's form** | the cultivator-side cache is **empty** (`corePos == null`; an NBT without the `Core` key is unbound, [`CultivatorBlockEntity:41,53,318`], [`CultivatorBlock:97`]) **∧** the target core has not bound it itself (`hasCultivator`, `:338`) | **Yes** (D-177 supplementary item) |
| Inconsistent form | the cache points at a core, but that core's list does not contain it | **skip, do not rebind** (D-177 supplementary item) |

Only when **all three** criteria pass is anything written:

| # | Criterion | Form of the criterion | When it is not met |
|---|---|---|---|
| 1 | it is covered | `inRange` (`:454-465`, chunk Chebyshev ≤ 1) | not in the candidate set, never rebound |
| 2 | it is unbound | the cultivator-side cache is empty ∧ this core has not bound it | skip (a bound one is **not taken**) |
| 3 | there is room left | `count < capacity` | stop enumerating at once (§2.6) |

**Write order and "both sides are written"**: first `addCultivator` (`:351`, which returns `false` on its own when full) → only on success `setCore` ([`CultivatorBlockEntity:79`]) → **the core side first, then the cultivator side**, word for word the same order as on placement (`CultivatorBlock:99-100`) and on migration (`OutpostCoreBlockEntity:686-690`). Writing only the core side leaves the cultivator-side reference cache empty for good (the next round refuses the duplicate write because of `hasCultivator`, so **the cache can never be filled**); writing only the cultivator side leaves one entry missing from the core's list, and capacity, migration and the link all fail to see it.

**A core with no owner does not rebind** (**author's ruling #13**): a core with `ownerUuid == null` (one produced by `/setblock`) **does not rebind**. The reason matches the existing form: `emitBoundaryRing:535-537` "returns directly" for an ownerless core; with no owner there is no "allied" to speak of, and no way to tell whom those 16 entries belong to. It is not in `OutpostCoreIndex` either (the index is written only in `setOwner:410-423`).

**What to do when there is not enough room** (**author's ruling #14**): when one round finds more unbound cultivators than the room left, take the first N by "ascending `distSqr` to this core → lexicographic order of coordinates", isomorphic to the tie-break key at `selectCore:310-315` — the same save gives the same binding result on two runs, rather than depending on the enumeration order of chunks and block entities.

**The cache points at a core that no longer exists** ([author's supplement]): such a "stale cache" is **skipped** per §2.5's inconsistent form — rebinding must not treat it as unbound and rebind it, because it may be pointing at a core unreadable this round (chunk not loaded); the cost is that the cultivator stays unusable until that core-breaking run reaches migration exit ② (clear the cache) or someone deals with it by hand.

### 2.6 One-to-one: one cultivator must not be claimed by two cores at once

This is 0.2.0's D-165 invariant, **kept** in this version (RT-51). After rebinding introduces a second writer, three layers defend it:

| Layer | Mechanism | Explanation |
|---|---|---|
| 1 | **the candidate surface**: only a cultivator whose cache is empty enters the candidates | any cultivator already written by a core has a non-empty cache ⇒ excluded naturally |
| 2 | **writing is claiming**: after the core-side write succeeds, the cultivator-side cache is written **immediately** | the core processed later in the same tick reads an already-updated cache ⇒ it skips (D-177 supplementary item) |
| 3 | **serial**: several cores in the same tick run `serverTick` **one after another** on the main thread | therefore "collect candidates first, write them together across ticks" is not allowed: it would let two cores both see "the cache is empty" at the same instant [author's supplement: a coding constraint derived from layer 2] |

**Stop when full**: `addCultivator` returns `false` on its own when full (`:352`), so the rebind loop need not check again, but it **must stop enumerating when the allowance is reached** (RT-53) — otherwise "keep reading every block entity after the 16th is reached" burns CPU for nothing and leaves a window for "just bound and then removed".

**Rebinding does not call `pruneCultivators`** (D-177 rejects it): it changes the list and calls `setChanged` (`:384-389`), and interleaving it with rebinding in the same loop would make "the coordinates bound this time" be counted twice or missed in later judgements. The cost is: if the core's list holds a **dead entry** (the block has been removed), it still occupies a place in the allowance, rebinding still treats the list as "full", until something else calls `cultivators()` and triggers the lazy prune (§9 B-10).

### 2.7 The relation to "binding on placement": a **partial supersession** of D-165, not an addition

The original text of author's ruling #12 is "**partial supersession**"; plan `:5`'s "0.2.0's existing content may not be changed unless **this document explicitly requires it**" plus plan `:9`'s "every 100 ticks it will try to bind ..." together are that explicit requirement. Item by item:

| D-165 (0.2.0)'s clause | Status | This version |
|---|---|---|
| "no 'nearby drift / periodic rebinding' at runtime" | **superseded** | the periodic **rebinding** every 100 ticks is explicitly required by this version (D-176) |
| "**do not secretly rebind at runtime**" (D-165 supplementary item) | **superseded** | as above; and rebinding is not "secret" — it has an explicit requirement (the whole of §2 here) and is written into the acceptance table by 42 |
| the core-selection criteria (covered + allied + not full, nearest first, tie-break key `distSqr` → `placedGameTime` → coordinates) | **kept** | rebinding reuses the same criteria (`selectCore:276-317`) and **sets up no second ordering**; rebinding does not change the "on placement" judgement either |
| the 16 cap (`outpost_core.cultivatorCapacity`) | **kept** | rebinding's boundary is exactly "stop at 16" (§2.2, §2.6) |
| one-to-one (a cultivator has 0 or 1 core) | **kept** | what §2.6's three layers defend is exactly this |
| "the single truth of a binding is on the core side" | **kept** | rebinding still writes the core side first, then the cache; the cache is **not** the truth (D-165 supplementary item, 40's §5 reading note) |

**"Filling a place" is not "drifting"**: drifting is "move a bound one to a nearer core"; rebinding is "fill in an unbound one". Not one bound record is moved in this version.

**One comment must change** ([card §3.4 row 11]): `CultivatorBlock:81-83` currently says "没有可用核时……**永久**处于未绑定态……**不自动认领**——事后在旁边补一个核也**不会**绑上" (with no usable core ... **permanently** in the unbound state ... **no automatic claiming** — adding a core next to it afterwards **will not** bind it either). That is exactly the opposite of this version; leaving it unchanged would be "the comment says no, the code does it anyway".

### 2.8 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| it reaches 16 | stop enumerating at once; the 16 bound entries **do not move at all**, and the 17th is still unbound | D-177; RT-53 |
| a chunk in the 3 × 3 is unreadable this round | skip that chunk, no cultivator inside it is rebound this round; try again next round (100 ticks later), no chunk loading | author's ruling #2; D-177 supplementary item |
| rebinding and core-breaking migration happen at the same time | **no double claiming**: the migrated cultivator's cache points at the core being broken (non-empty) ⇒ it is not among the rebind candidates; after migration exit ② clears the cache, the **next round** of rebinding takes over (≤ 100 ticks) | D-177 supplementary item; D-168 |
| the cultivator-side cache is empty while the core side actually holds a record (abnormal state) | rebinding **skips** it (D-177: any inconsistency between the two sides is skipped). It must not be "treated as unbound" — that would create a second owner | D-177 supplementary item |
| the core-side write succeeds and the block entity is already gone when the cultivator side is written | only the core side is written; the next round will not count it twice because `hasCultivator` returns `false` (idempotent), and the cache stays empty | [author's supplement] |
| the core has no owner (`/setblock`) | no rebinding, return directly | [author's supplement]; the existing form `emitBoundaryRing:535-537` |
| two allied cores both cover the same batch of unbound cultivators | each fills its own room; one cell only ever lands on one core (the first writer claims the cache, see §2.6 layer 2) | D-177 supplementary item |
| the core's list holds a dead entry for a removed block | still treated as "full", nothing is rebound this round; the dead entry is removed by the lazy prune of the next `cultivators()`, and **rebinding does not prune on its own** | D-176 supplementary item, D-177 rejection |

---

## 3. R2 The core storage shows item details on hover

**Grounds for this section: D-178 (author's ruling #3).**

### 3.1 One-line positioning

In the Outpost Core's 54-slot screen, hovering the mouse over any item slot shows the **item name + the enchantment / effect lines**, exactly as when a vanilla chest is opened. **This is not "building a tooltip from scratch"**: the default chain is supposed to show it anyway, and this version writes it down as this version's **explicit requirement + explicit acceptance**, with the implementation surface **preferably not one line changed**.

### 3.2 Factual premise: the default chain has not been bypassed

| # | Evidence | Where from |
|---|---|---|
| 1 | `OutpostCoreScreen` is **42 lines** in total and **overrides only `renderBg`** (`:37-41`); the overrides of `render` / `renderTooltip` / `renderLabels` / `init` are **all 0** | [card §1.2]; [`client/OutpostCoreScreen:21-42`] |
| 2 | all 54 slots are vanilla `net.minecraft.world.inventory.Slot` instances (`:68`, `:79`, `:84`), with no custom `Slot` subclass changing the hover behaviour | [card §1.2]; [`menu/OutpostCoreMenu:66-86`] |
| 3 | `renderTooltip` has **0** hits in the whole repository | [card §1.2] (rechecked here and consistent) |

⇒ vanilla's `AbstractContainerScreen#render` calls `renderTooltip` itself, and **nobody intercepts it**. So what this version must deliver is "a requirement + acceptance", not "a piece of new rendering code".

### 3.3 What "guarantee" means

"Guarantee" is an **acceptable behavioural promise**, not "probably fine". Four must-nots and two musts:

| # | Must not / must | Why |
|---|---|---|
| 1 | **must not** override `render` without calling `super` | that is the only change that makes the whole tooltip chain disappear (it does not exist in 0.2.0, and this version must not introduce it either) |
| 2 | **must not** draw slots by hand and bypass `renderSlot` | hand-drawn slots do not go through vanilla's hover test, which turns "is there a tooltip" into hand-written logic |
| 3 | **must not** add a custom `Slot` subclass to "provide the tooltip" | vanilla's `Slot` is not responsible for tooltips; a subclass solves nothing and only adds a layer nobody reads (D-178 rejection) |
| 4 | **must not** take over the tooltip's positioning / scaling / background / delayed display | that is vanilla's handling surface; taking over one place means taking over all of them, and a 42-line class would become a hand-drawn screen (D-178 rejection) |
| 5 | **must** make the 54 slots and the player-inventory slots behave **the same** | both kinds of slot are in the same `AbstractContainerScreen`, and vanilla treats them alike |
| 6 | **must** cover this mod's own tooltip lines | this mod has **20** language keys for `tooltip.farmstorm` ([card §0]), and they are part of the item tooltip already; "show item details" includes them, it is not just the name |

### 3.4 How the acceptance is observed (on screen, not in the code)

| Step | Action | What must be seen |
|---|---|---|
| 1 | an allied player right-clicks the core empty-handed and opens the 54-slot screen | the screen opens normally, the layout identical to a vanilla double chest (54 slots + the player inventory) |
| 2 | move the mouse onto a slot **with an item** and hold it there | a tooltip appears: **the first line is the item name** |
| 3 | switch to an item **with an enchantment / with an effect of this mod** (e.g. a crop carrying the temporary-enchantment component) | the tooltip gains **the enchantment line / effect line**, styled as in a vanilla chest |
| 4 | move the mouse onto an **empty slot** | **no** tooltip (not an empty frame, not a blank line) |
| 5 | move the mouse onto an item in a **player-inventory slot** | exactly as in step 2 |
| 6 | move the mouse to the rightmost column of the screen | the tooltip is still fully visible (vanilla flips it to the left; the position is computed by vanilla and not taken over here) |

**The acceptance can only be written as "visible on hover"**: the line number of vanilla's `renderTooltip` call site cannot be verified on this machine ([card §1.2] gives only in-repository evidence), so the acceptance **must not** be written as "line N of `render` calls it".

### 3.5 What does not change + boundaries

| Item | Conclusion | Basis |
|---|---|---|
| 54 slots | **unchanged** (`outpost_core.storageSlots` defaults to 54, [`FarmstormConfig:944-946`]) | D-166 kept; 40's §5 |
| the two access gates | **unchanged** (allied ∧ the vanilla reach) | D-166; 40's §5 |
| the menu structure / the three `addSlot` | **unchanged** (`:68`, `:79`, `:84`) | [card §1.2] |
| the background texture | **unchanged** (`generic_54.png`, 256 × 256, `:22-28`) | [card §1.2] |
| the registry surface | **unchanged**: no new `Slot`, no new Screen, `menu_types` still 2 | D-175 |

**Boundaries**: hovering an empty slot gives no hint; when the tooltip goes beyond the screen's right edge vanilla flips it to the left; a non-allied player cannot open the screen at all (D-166 supplementary item), so "a non-allied player sees no tooltip" is not a tooltip problem; after the screen closes the tooltip state leaves no residue. That the acceptance passes with not one line of code changed is the **correct result**, not "nothing was done" (RT-59, 40's §7 item 4).

---

## 4. R3 The Vibration Bean deletes the AI of non-player mobs

**Grounds for this section: D-179, D-180 (author's rulings #4, #5, #8).**

### 4.1 One-line positioning

**At the instant the Vibration Bean triggers**, it deletes the AI of **every non-player mob** inside the trigger radius for **80 ticks (4 seconds)**, then restores the **original value** they had before entry. Who triggered it **does not enter the filter**: the monster that stepped on it, the cow standing beside it, the villager passing by — as long as they are in the radius and are not players, they all stop.

In 0.2.0 its effect on mobs is **0** (mobs are merely the trigger, [`behavior/impl/VibrationBeanTrap:59-64`]), so this is **entirely new behaviour**, not "a duration was changed".

### 4.2 The object and the radius

| Item | Rule | Basis |
|---|---|---|
| Object | **every non-player mob in range** = `Mob` ∧ ¬`Player`; **not** "the one that triggered the trap", and **not** "hostile mobs" | author's ruling #4; D-180; the existing predicate form `combat/WildHunterAura:88` |
| Radius | reuses the **same single key** shared by the trigger end and the CONFUSION end, `vibration_bean.triggerRadius` (default **4.0** blocks, strictly less than), **no second number is set up** | [`FarmstormConfig:900-902`]; D-180 supplementary item; the comment in [`VibrationBeanTrap:41-42`] |
| Players | **never in the object set** (a player is not a `Mob`); how players are handled is **unchanged word for word**: every player in the radius still gets `CONFUSION` | D-180; [`VibrationBeanTrap:83-91`] |
| Snapshot | "in range" is **a one-off snapshot at the instant of the trigger**: a mob entering the radius afterwards is not deleted later, and one leaving early is not restored early | 40's §7 ⑥; D-180 supplementary item |
| Counting | how many mobs were in the radius and how many were deleted goes into the log only and does not affect the result | [author's supplement] |

### 4.3 Duration and restore: 80 ticks, record the original value

| Item | Rule | Basis |
|---|---|---|
| Duration | **80 ticks** = `80 ÷ 20 = 4 seconds` [author's supplement: arithmetic] | author's ruling #5; `plan0.2.1.txt:14` ("4 seconds") |
| Window | the half-open interval `[t0, t0 + 80)`; the restore starts at tick `t0 + 80` (the same shape as the window in `trap/TrapImmunity:16`) | [author's supplement]; D-180 |
| Where the duration comes from | **a constant 80**, which **does not read** `vibration_bean.confusionTicks` and **adds no** config key: author's ruling #5 already gives a fixed number, and one more key would take 175 to 177 (conflicting with D-185) | author's ruling #5; D-180 supplementary item; D-185 |
| "Same value, different meaning" | `vibration_bean.confusionTicks` also defaults to 80 ticks ([`FarmstormConfig:903-905`]), but that is the **players' CONFUSION duration**; the two have **the same value and different meanings**, must not be merged into one key, and "change the CONFUSION duration" must not be taken for "change the AI duration" | D-180 supplementary item |
| Original value | before entry, read `mob.isNoAi()` and record this reading into that entity's **persisted** original-value record (author's ruling #15, see §5.5); on exit write back **that reading** (one whose original value was `true` stays `noAi`) | author's rulings #8, #15; D-179 |

### 4.4 Coexisting with "CONFUSION for players only": one trigger, two kinds of result

| Step | Moment | What happens | Who is affected |
|---|---|---|---|
| 1 | `t0` | `findTrigger` hits (a non-allied player **or** a `Monster`, `:45-66`) | — |
| 2 | `t0` | **every player** in the radius gets one `CONFUSION` (`confusionTicks` ticks), with no friend-or-foe filter | players (allied ones included, creative / spectator included: that is the status quo, unchanged here) |
| 3 | `t0` | one explosion sound (`SoundEvents.GENERIC_EXPLODE`, volume 1.0, pitch 0.9) | audible near the radius |
| 4 | `t0` | **new in this version**: **every non-player mob** in the radius goes into the original-AI-flag table + `setNoAi(true)` | mobs |
| 5 | `t0` | `consume()` removes it silently without touching the loot table; then the advancement is granted to the players inside | — |
| 6 | `t0 + 80` | restore the AI to the original value; a further trigger outside the window is handled per §9 B-19 | the same batch of mobs |

Four rules: ① **settled exactly once per tick** (the trap is `consume`d at `:97`, and `TrapImmunity:42-45` always lets mobs through ⇒ the same bean cannot trigger twice); ② **players get CONFUSION, mobs get the stop**, the two lines do not replace each other; ③ **the object of CONFUSION is still "every player"** (the existing one-shot AoE measure of D-27), and this version does **not** add a friend-or-foe filter along the way; ④ **mobs do not get CONFUSION** (the `CONFUSION` loop iterates `level.players()` only).

### 4.5 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| there are mobs and players in the radius | players get CONFUSION + mobs stop, the two results happen at the same time | author's ruling #4; D-180 |
| there are only players and no mobs in the radius | the behaviour is **exactly** as in 0.2.0 (CONFUSION only + the sound + removal) | D-180; §4.4 |
| the same mob is covered again by another Vibration Bean within the 80 ticks | **refresh** the window to "now + 80", do **not** stack it into 160; the original value records only the one from **the first** entry | D-179 supplementary item (the original value records the first only); D-180 |
| the mob dies / is removed while stopped | the persisted original-value record is deleted with the entity, leaving no residue (author's ruling #15's cleanup path 4); UUIDs are not reused | author's ruling #15 |
| the mob was already `noAi` (locked by a command / another mod) | on entry the original value is recorded as `true`, and on exit it is **still** `noAi` (it must not be "restored to having an AI" along the way) | author's ruling #8; D-179 |
| the mob is knocked back / loses health / is hit while stopped | as usual: deleting the AI changes no damage, health, drops, collision or knockback | §0.4 item 5; D-179 |
| the chunk the mob is in unloads while it is stopped | **keep the attachment and settle it on load** (cleanup path 5): once unloaded the entity is no longer in the world, so its `noAi` cannot be changed on the spot; on load, restore from the attachment and delete the record | author's ruling #15; **A-80** |
| creative / spectator players | they do not trigger it themselves (`findTrigger:51-53` filters them out), but they **are still confused** when someone else triggers it (`:83-91` filters by distance only, not by friend or foe); this version **does not fix it** and registers it as 0.2.0's existing measure | [`VibrationBeanTrap:51-53,83-91`] |

---

## 5. R4 Q-Bounce deletes the AI of non-player mobs

**Grounds for this section: D-179, D-181 (author's rulings #6, #7, #8).**

### 5.1 One-line positioning

A **non-player mob** carrying the Q-Bounce effect stays at `noAi` **for the whole lifetime of the effect**; the instant the effect ends it is restored immediately to the value it had before entry. **No fixed number of seconds is set** (author's ruling #6) — "how long" is decided by the duration the applier writes into the effect. While in **mount mode** it is **not deleted**, and one already riding **is not touched** (author's ruling #7).

### 5.2 "The duration" = the lifetime of the effect

| Item | Rule | Basis |
|---|---|---|
| Duration | **it stops as long as the effect is on it**: not 80 ticks, not 10 seconds, and no "stop duration" key is read | author's ruling #6; D-181 |
| Where the effect's duration comes from | the applier `FriendChivesProjectile` reads `friend_chives.effectTicks` (**default 200 ticks = 10 seconds**, [`FarmstormConfig:879-881`]) as the field's duration and writes it into the effect | [card §1.4]; D-181 supplementary item |
| Numbers added by this version | **0**: no duration key, no new registered object, no change to `friend_chives.effectTicks` | author's ruling #6; D-174; D-183 |
| Restore criterion | **"is the effect still on it"** (`hasEffect(Q_BOUNCE)`), not "how many ticks have passed since entry" | D-181 supplementary item; RT-56 |
| Object | `Mob` ∧ ¬`Player`: a player carrying Q-Bounce **is not affected** (a player is not a `Mob`), and the mount / render looks are unchanged | D-181; §0.4 item 4 |

### 5.3 The fact of "repeated application" must be written clearly

| Fact | Where from | Consequence for the requirement |
|---|---|---|
| every tick the field re-scans the target sphere and rewrites the effect once for **every** `LivingEntity` inside it | [`entity/thrown/FriendChivesProjectile:155-171`] | the decision point of "delete the AI on entry" **may be reached again every tick** |
| the duration written is "the field's duration − ticks already elapsed" | [`FriendChivesProjectile:156`] | the effect's remaining duration decreases tick by tick; it is **not a fixed value** |
| vanilla updates only when "the new duration is longer" (`addEffect` returns `false` when a mob already inside is refreshed tick by tick) | [`FriendChivesProjectile:176-177`] | the same effect instance runs out naturally from the duration of the first application |
| therefore: **one must not** pair a one-shot timer with the first application | — | the per-tick re-scan would reset the timer (→ never restore), or, the other way round, restore early while the effect is still there |

**Two hard requirements** (both derived from the table above, marked [author's supplement]):

1. **The entry point must be idempotent**: for an entity that already has an original-value record, do not record the original value again and do not reset the window. Recording the original value again would take "the reading this version has already changed to `true`" as the original, and on restore the mob would be **locked for good** (this is exactly the pit 40's D-179 supplementary item names).
2. **The restore must be driven by "is the effect still there"**, and be judgeable **every tick**: restore the moment the effect is gone, no matter whether it expired, was cleared with `/effect clear`, or was cleared by milk.

### 5.4 Skipping the mount state

| Layer | Rule | Basis |
|---|---|---|
| on entry | currently in mount mode ⇒ **the AI is not deleted**, and one already riding is not touched | author's ruling #7; D-181 |
| switching to mount after deletion | **restore to the original value first, then mount**; a mount must not end up running 0 movement while carrying a `noAi` entity | D-181 supplementary item (the second layer is landed by 42 at the "mode switch" layer) |
| the entry for the judgement | "is it mount now" **may only be asked of** `QbounceMountController` (`isMountMode()` is currently `private`, [`behavior/QbounceMountController:199`]); the effect class **must not** parse `qbounce.poseMode` a second time of its own | author's ruling #7; D-181 supplementary item; [card §3.4 row 12] |
| default | `qbounce.poseMode` defaults to **`render`** ([`FarmstormConfig:861-863`]) ⇒ **the mount branch does not take effect under the default configuration**, and a mob carrying Q-Bounce **always stops**; "skipping" exists only when the configuration is changed to `mount` | [card supplement]; D-181 |

### 5.5 The five cleanup paths + the original value persisted

| Path | Expected result | Basis |
|---|---|---|
| the effect **expires** | restore to the original value from the same tick it expires, **and delete that entity's original-value record** | RT-55; D-181; author's ruling #15 |
| `/effect clear` | restore to the original value from the instant it is cleared by hand, and delete the record | RT-55; author's ruling #15 |
| **drinking milk** (`MilkBucket`) | as above | RT-55; author's ruling #15 |
| **the entity dies / is removed** | the original-value record is deleted together with the entity, leaving no residue that a later read could pick up | author's ruling #15 |
| **the chunk unloads** | **keep the attachment and settle it on load**: the entity is not in the world when the chunk unloads, so `noAi` cannot be changed on the spot; on load, restore from the attachment and delete the record — keeping the attachment is **not** "leaving it in the save" | author's ruling #15; **A-80** |
| beyond the five: the effect is **applied again** (the field is still scanning) | the effect is still there ⇒ **no restore**; only the entry point is idempotent and does not record the original value again | §5.3; D-179 supplementary item |
| the field writes a longer duration when the effect has 1 tick left | the effect is still there ⇒ it keeps stopping, no false restore | §5.3; RT-56 |
| **before and after reloading the save** (RT-54) | see below (the original value is persisted with the entity and no longer depends on an in-memory table) | author's ruling #15; RT-54 |

**The measure for persisting the original value** (**author's ruling #15**):

1. The original AI flag is **written onto the entity** (a persistence surface of the `DataAttachment` kind) and persisted with the entity ⇒ **kept across a save reload**. This version no longer needs an "in-memory table", nor fallbacks such as "clear the whole table when the server instance changes" (the form at `trap/TrapImmunity:27-31` suits only state that does not cross a save).
2. Therefore author's ruling #8's "remember the original value from before entry, restore it at the end" **holds across a save reload as well**: after a reload, the mob keeps stopping while the effect is still there, and the moment the effect is gone it is restored to the **persisted original value** — an entity that "was already `noAi` before entry" goes back to `noAi` as well, with no fallback of "restore to having an AI when the record is missing".
3. **Invariant**: "the original-value record exists" ⇔ "this version currently holds that entity's AI". The record still there while the effect has ended ⇒ dirty data (a cleanup was missed); the record gone while `noAi` is still set with no external cause ⇒ a lost restore. Neither direction is allowed.
4. **The new risk form is "a persisted residue"**: what is persisted does not vanish by itself, and **missing any one cleanup path in the table above** leaves a dirty record in the save; the next time that entity enters the stopped state it reads that old record, and the restore writes the wrong value. ⇒ The five paths must be exhaustive, and "restore to the original value + delete the record" is **one step**, not to be split into two.
5. The counts do not change: no new `data_components` (still 1), registered objects still **97** (the attachment is not one of this version's 12 counted categories, and 42 rechecks it category by category in the end); the exact form of the persistence carrier is fixed by 42.

### 5.6 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| the effect expires / `/effect clear` / milk | all three go through "restore the original value + delete the record" | RT-55; author's ruling #15 |
| the effect is rewritten every tick | the entry point is idempotent: no second original-value record, no window reset; **no early restore just because "it was applied once more"** | §5.3; D-179 supplementary item |
| the mob dies / is removed while stopped | the original-value record is deleted with the entity (cleanup path 4) | author's ruling #15 |
| the chunk unloads | **keep the attachment and settle it on load** (cleanup path 5) | author's ruling #15; **A-80** |
| it was already `noAi` before entry | the restore writes `true` back, it is not released | author's ruling #8; author's ruling #15 |
| `poseMode=mount` and the mob carries the effect | the AI is not deleted (not touched) | author's ruling #7 |
| the AI has already been deleted and the configuration then switches to `mount` | restore first, then mount (§5.4 layer 2) | D-181 supplementary item |
| a player carries Q-Bounce | the AI is untouched; the mount / render looks are unchanged | §5.2 |
| the effect is still on it after reloading the save | the original value is persisted with the entity ⇒ it keeps stopping, and at the end it is restored to the **persisted original value** and the record is deleted; there is no fallback that "writes `false`" | author's ruling #15; §5.5 |

---

## 6. R5 Traps deal ×3 damage to hostile mobs

**Grounds for this section: D-182, D-183, D-184 (author's rulings #9, #10, #11).**

### 6.1 One-line positioning

The `trap` group gains one **global multiplier key** `trap.damageMultiplier` (double, default **3.0**): **the one damage instance a trap crop settles for itself**, when **the target is a hostile mob**, is multiplied by it. Of the 4 trap crops **only 2 really participate**; players and non-hostile mobs **always take the original value**; **not one** of the crops' damage defaults changes.

### 6.2 The multiplier key

| Item | Value | Basis |
|---|---|---|
| Key name | `trap.damageMultiplier` | D-183 |
| Group | `trap` (currently 3 keys, [`FarmstormConfig:917-927`]) ⇒ **4 keys** | D-183; [card §3.2] |
| Type | double | D-183 |
| Default | **3.0** | D-183; [card §3.2] |
| Range | **fixed by 42 within D-183's framework** (this document writes no concrete bounds) | D-183; [card §7 item 5] |
| Effect surface | applies only to the one combination "the damage instance the trap settles itself" × "the target is a hostile mob" | D-184 |
| Surfaces it does not affect | players, non-hostile mobs, non-trap explosions (the throwables' small explosions, the Lucky Berry), the fangs, the Vibration Bean | D-184; author's ruling #11 |
| Knock-on counts | config **175 → 176** keys, groups still **60**; language keys **337 → 338** (the new key's translation, the same key and the same count in Chinese and English) | D-183, D-185; [card §3.2, §3.3] |
| Documentation duty | this key **must really be written into the documents**: `tools/verify_project.py`'s expected key set is extracted from `docs/zh/*`, and a key absent from the documents is the same as a key not added (A-66's false green) | RT-60b; D-185; 40's §7 ⑦ |

**The multiplier is not "a change of the defaults"**: `blast_carrot.maxDamage` is still **7** ([`FarmstormConfig:375-377`]), `zangief_cane.slamDamage` is still **7.5** ([`FarmstormConfig:851-853`]). What the player sees in config is still the original value; the multiplier is applied at the settle site (author's ruling #10 rejects "change each crop's default").

**The multiplier is not "one multiplication at the damage-event layer" either**: that layer cannot get a reliable identity of "did this hit come from a trap" — `blast_carrot` and the 5 throwables use the **same** `farmstorm:crop_blast` damage source (`registry/FarmstormDamageTypes:36`), and judging by source would multiply the Melon Grenade, the Molotov and the rest as well (D-184 supplementary item; 40's §6 item 2).

### 6.3 The hostile criterion

| Item | Rule | Basis |
|---|---|---|
| Criterion | **an instance of `net.minecraft.world.entity.monster.Enemy` ∪ `MobCategory.MONSTER`** (a union) | author's ruling #9; D-182 |
| Why a union | vanilla's `Monster` already implements `Enemy`, so the union's **actual increment** is two kinds: "a modded mob whose category is `MONSTER` but which does not implement `Enemy`" and "a mob that implements `Enemy` but is outside the `MONSTER` category" | D-182 supplementary item |
| Known gaps | a "pseudo-hostile" that is neither an `Enemy` nor of the `MONSTER` category does not get the multiplier; this version **does not widen the criterion and adds no whitelist** | 40's §7 ③ |
| Used in one place only | it serves the ×3 gate only; it **does not go back and change** the existing 5 `Monster` filters (`BlastCarrotTrap:59`, `FangTrapCactusTrap:54`, `VibrationBeanTrap:59`, `ZangiefCaneTrap:174`, `PumpkinTurret:109`) — changing them changes the **trigger surface** | D-182; RT-57; [card §1.5] |
| Players | **never** hostile mobs | D-182 supplementary item |
| Status quo | `Enemy` / `isHostile` / `MobCategory.MONSTER` all have **0** hits in the whole repository (rechecked here: `MobCategory` has only 2 occurrences, both `MISC`) | [card §1.5] |

### 6.4 Participation surface: 4 trap crops, 2 participate

| Crop | Damage implementation | Participates in ×3 | Reason |
|---|---|---|---|
| `blast_carrot` | goes through `explosion/FarmstormExplosion`, `entity.hurt` at **`:179`**, damage = `min(raw, cap)` (formula `:178`, cap = `blast_carrot.maxDamage`, default 7) | **participates** | the damage is settled in this repository, and the target side is already a full walk over `LivingEntity` (`:159`), so only a hostile gate has to be added |
| `zangief_cane` | the mob branch `behavior/impl/ZangiefCaneTrap:109-129`, `hurt` at **`:118`**, a fixed 7.5 points | **participates** | the mob branch settles `hurt` exactly once; the player branch (`ZangiefPiledriverController:311`) is not on the multiplier surface |
| `fang_trap_cactus` | spawns **vanilla `EvokerFangs`** (`FangTrapCactusTrap:87-109`), the damage lives in the vanilla class | **does not participate** | **0** `hurt` in the repository; changing it would mean rewriting vanilla fangs' spawning and settling (author's ruling #11; 40's §6 item 1) |
| `vibration_bean` | **0 damage** | **does not participate** | there is no damage to multiply (author's ruling #11) |
| **Total** | `CropCategory.TRAP` has **4** | **2 participate / 2 do not** | plan `:22`'s "all" is narrowed per author's ruling #11 |

**⚠️ One sentence that must be nailed down**: plan's original `:22` is "**all** trap crops' damage to hostile mobs becomes three times the original"; per author's ruling #11, this version's measure is "**2 of the 4 trap crops**". **A downstream document that writes "all traps ×3" is wrong** — the acceptance will be written per "all" and will therefore necessarily misjudge (RT-58, Appendix A row 1).

### 6.5 How it is computed: multiply the final damage

| Crop | Expression | Result under the default configuration | Basis |
|---|---|---|---|
| `blast_carrot` | `3 × min(raw, cap)`, equivalent to `min(3 × raw, 3 × cap)` | capped `7 × 3 = 21`; when not capped it is also ×3 (e.g. `raw = 6` ⇒ `6 × 3 = 18`) | `FarmstormExplosion:178-179`; D-184; [author's supplement: arithmetic] |
| `zangief_cane` | `3 × slamDamage` | `7.5 × 3 = 22.5` | `ZangiefCaneTrap:118`; [`FarmstormConfig:851-853`]; [author's supplement: arithmetic] |

Three counter-examples (all of which must be computed correctly):

| Scenario | Result | Explanation |
|---|---|---|
| a blast carrot explosion with `raw = 4.0` hits a hostile mob | `4 × 3 = 12` | **not** "it is multiplied only at 7": what is multiplied is the final damage |
| the same explosion hits a player or a neutral mob | `4` (the original value) | the multiplier applies to hostile mobs only |
| the small explosions of the Melon Grenade / Melon Slice Bomb / Molotov / Ubi Server / Toxic Ubi Server / Lucky Berry hit a hostile mob | the original value | these 6 are not the trap's one explosion (`FarmstormExplosion` has **7** construction call sites: `BlastCarrotTrap:84` + `MelonGrenadeProjectile:42` + `MelonSliceProjectile:40` + `MolotovProjectile:94` + `ToxicUbiServerProjectile:43` + `UbiServerProjectile:41` + `LuckyBerryRoller:212`) |

**Two implementation choices are not written here** (they belong to 42): whether the multiplier lands as "pass a flag at construction" or "judge the source once before settling", and `trap.damageMultiplier`'s value range and field name. This document pins down only the **observation surface**: a non-trap explosion's damage against a hostile mob **must not move by a single point**.

### 6.6 The narrowing statement and "what does not change"

| Item | This version | Basis |
|---|---|---|
| participants among the 4 traps | **2** (not 4, not "all") | author's ruling #11; D-184 |
| the trigger surface (the 5 `Monster` filters) | **unchanged** | D-182; RT-57 |
| each crop's damage default | **unchanged** (7 / 7.5 as they are) | author's ruling #10; D-183 |
| the blast carrot's explosion power and range | **unchanged** (`explosionPower` defaults to 2, `reach = power × 2`) | [`FarmstormConfig:372-374`]; `FarmstormExplosion:154` |
| the blast carrot's nausea for players | **unchanged** (it iterates players only, `:98-103`) | [card §1.6] |
| the Zangief Cane's knockback / sound | **unchanged** (the knockback is still at `:124`) | [`ZangiefCaneTrap:118-125`] |
| the Zangief Cane's player branch | **unchanged** (`ZangiefPiledriverController:303,311`) | D-184 supplementary item |
| the fangs | **unchanged, no new damage source, no Mixin** | author's ruling #11; D-186 |
| the Vibration Bean | **0 damage, nothing to multiply** | author's ruling #11 |

### 6.7 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| a hostile mob is hit by the blast carrot | `3 × min(raw, 7)`, at most 21 | D-184 |
| a player is hit by the same explosion | the original value (`min(raw, 7)`), at most 7 | D-184 |
| a neutral / passive mob is hit by the same explosion | the original value | D-182 |
| a hostile mob is caught in one of the 6 non-trap explosions (the Melon Grenade etc.) | the original value | D-184 supplementary item |
| a hostile mob is pricked by the fangs | vanilla damage (not touched here) | author's ruling #11 |
| a hostile mob steps on the Vibration Bean | 0 damage (it was 0 already) | author's ruling #11 |
| the multiplier is set to 1.0 | the behaviour is **exactly** as in 0.2.0 (this is the value used for alignment) | [author's supplement] |

---

## 7. Master table of numbers (with a "Basis" column)

Every number is given with its source. The source is of only four kinds: a decision number in 40, the fact card's / the source code's `file:line`, a config key name + default, or an [author's supplement] with the expression written out. "Rechecked here" means this document counted it again from the source and it agrees with the fact card.

| # | Value | Meaning | Basis |
|---|---|---|---|
| 1 | **100 ticks** | the rebind period (a constant, not a config key) | `plan0.2.1.txt:9`; D-176 |
| 2 | **0 ~ 99 ticks** | the rebind phase offset interval (by the core's coordinate hash) | author's ruling #2; D-176; [author's supplement: arithmetic `100 − 1`] |
| 3 | **40 ticks** | the boundary particles' interval default (unchanged here) | [`FarmstormConfig:939-941`] |
| 4 | **20 ~ 1200** | the configurable particle interval range (the source of the wrong-period drift) | [`FarmstormConfig:941`] |
| 5 | **200 ticks** | `LCM(40, 100)`: the wrong period if rebinding is attached after the particle gate | [card §1.1]; D-176 supplementary item |
| 6 | **5 seconds** | the rebind period converted (`100 ÷ 20`) | [author's supplement: arithmetic] |
| 7 | **16** | the per-core cultivator allowance default = `outpost_core.cultivatorCapacity` | [`FarmstormConfig:949-951`]; D-177 |
| 8 | **1 ~ 64** | the allowance's configurable range (rebinding must read config, not hard-code 16) | [`FarmstormConfig:951`] |
| 9 | **17 = 16 + 1** | the 17th cultivator stays unbound and squeezes no bound one out | D-177; [author's supplement: arithmetic] |
| 10 | **3 × 3 chunks / 9** | the core's influence range (`RANGE_CHUNKS = 1`; the chunk count `3 × 3`) | [`OutpostCoreBlockEntity:75,454-465`]; [author's supplement: arithmetic] |
| 11 | **48 blocks** | the range's side length (`16 × 3`) | [author's supplement: arithmetic]; [`OutpostCoreBlockEntity:78`] |
| 12 | **3** | the number of binding write points (placement / migration / rebinding) | D-165; D-176; author's ruling #12 |
| 13 | **16** (`16 − 0`) | the theoretical maximum increment of one rebind round | D-177; [author's supplement: arithmetic] |
| 14 | **80 ticks** | the Vibration Bean's AI stop duration (a constant, reads no key) | author's ruling #5; D-180 |
| 15 | **4 seconds** | as above (`80 ÷ 20`) | author's ruling #5; [author's supplement: arithmetic] |
| 16 | **80 ticks** | `vibration_bean.confusionTicks`'s default (**the players' CONFUSION duration**, same value, different meaning, unchanged here) | [`FarmstormConfig:903-905`]; D-180 supplementary item |
| 17 | **4.0 blocks** | the Vibration Bean's trigger radius default (shared by the trigger end and the effect end; no second number is set up) | [`FarmstormConfig:900-902`]; D-180 supplementary item |
| 18 | **200 ticks** | `friend_chives.effectTicks`'s default field duration (unchanged here, no new duration key) | [`FarmstormConfig:879-881`]; author's ruling #6 |
| 19 | **10 seconds** | the longest Q-Bounce stop under the default field duration (`200 ÷ 20`) | [author's supplement: arithmetic] |
| 20 | **`render`** | `qbounce.poseMode`'s default (under the default configuration the mount branch does not take effect) | [`FarmstormConfig:861-863`]; [card supplement] |
| 21 | **0 overrides / 20 lines** | `QbounceEffect`'s status quo (this version adds overrides) | [card §1.4]; [`effect/QbounceEffect:15-20`] (the line count rechecked here and consistent) |
| 22 | **3.0** | `trap.damageMultiplier`'s default (double) | D-183; [card §3.2] |
| 23 | **7** | `blast_carrot.maxDamage`'s default (the damage cap, unchanged here) | [`FarmstormConfig:375-377`]; author's ruling #10 |
| 24 | **21 = 7 × 3** | the capped damage a hostile mob takes from the blast carrot | D-184; [author's supplement: arithmetic] |
| 25 | **7.5** | `zangief_cane.slamDamage`'s default (unchanged here) | [`FarmstormConfig:851-853`]; author's ruling #10 |
| 26 | **22.5 = 7.5 × 3** | the damage a hostile mob takes from the Zangief Cane | D-184; [author's supplement: arithmetic] |
| 27 | **4 → 2** | `CropCategory.TRAP`'s crop count → the number that really takes the multiplier | [card §1.6]; D-184; author's ruling #11 |
| 28 | **3 → 4 keys** | the `trap` group's key count | [`FarmstormConfig:917-927`]; D-183 |
| 29 | **175 → 176** | the config key count (`175 + 1`) | [card §0, §3.2]; D-185 |
| 30 | **337 → 338 (× 2)** | the language key count (the same key and the same count in Chinese and English) | [card §0, §3.3]; D-185 |
| 31 | **60 groups** | the config group count (unchanged, no new group) | [card §3.2]; D-185 |
| 32 | **97 = 97 + 0** | the registered object count (+0 in this version) | [card §0, §3.1]; D-175 |
| 33 | **0 / 2** | the Mixin count / the custom packets in total (this version adds no packet) | [card §0, §0.2]; D-186 |
| 34 | **54** | the core storage's slot count (unchanged here, unrelated to the 16 allowance) | [`FarmstormConfig:944-946`]; D-166 kept |
| 35 | **7** | `FarmstormExplosion`'s construction call sites (1 trap + 5 throwables + 1 Lucky Berry), all using `farmstorm:crop_blast` | rechecked here: `BlastCarrotTrap:84`, `MelonGrenadeProjectile:42`, `MelonSliceProjectile:40`, `MolotovProjectile:94`, `ToxicUbiServerProjectile:43`, `UbiServerProjectile:41`, `LuckyBerryRoller:212`; D-184 supplementary item |
| 36 | **0** | the `hurt` count of `fang_trap_cactus` in the repository | [card §1.6] |
| 37 | **0** | the hit count of `Enemy` / `MobCategory.MONSTER` / `isHostile` | [card §1.5] (rechecked here: `MobCategory` has only 2 occurrences, both `MISC`) |
| 38 | **0** | the hit count of `renderTooltip` in the whole repository | [card §1.2] (rechecked here and consistent) |
| 39 | **42 lines / 140 lines / 106 lines / 20 lines** | the line counts of `OutpostCoreScreen` / `OutpostCoreMenu` / `VibrationBeanTrap` / `QbounceEffect` | [card §1.2, §1.3, §1.4] (rechecked here with `ReadAllLines` and consistent) |
| 40 | **20** | the number of `tooltip.farmstorm` language keys (R2's coverage includes them) | [card §0] |
| 41 | **5** | the number of original-value cleanup paths (expiry / `/effect clear` / milk / entity death / chunk unload) | RT-55; author's ruling #15 |
| 42 | **2 layers** | the two directions of the mount judgement (skip on entry / restore before switching to mount) | D-181 supplementary item |

Three notes under the table:

1. **The 16 in row 7 and the 54 in row 34 are not the same thing** (the same discipline as author's ruling #8): 16 counts the **cultivator allowance**, 54 counts the **container slots**; rebinding recognises only 16, the tooltip only 54.
2. **Rows 1 and 14 are both "ticks", but they are not the same kind of number**: 100 is a **period** (how often an attempt is made), 80 is a **window** (how long the AI is deleted). R4 does not even have a window; its length is decided by the effect (row 18).
3. **Rows 29, 30 and 32 are not derived from one another**: 176 = 175 + 1 (the multiplier key), 338 = 337 + 1 (the translation of that same key), 97 = 97 + 0 (no registry increment here). Explaining any two of them by each other is a miscalculation.

---

## 8. Master table of player-visible behaviour changes (against 0.2.0)

| # | What 0.2.0 is | What v0.2.1 becomes | What the player will notice | Decision |
|---|---|---|---|---|
| 1 | no allied core nearby when the cultivator is placed ⇒ **permanently** unbound and unusable; placing a core next to it afterwards **does not** bind it either | once a core appears next to it, it is taken in automatically **within ≤ 100 ticks (5 seconds)** | "that machine that never worked hooked itself up" | D-176, D-177 |
| 2 | the 17th one placed while the core already has 16 **can never** be bound | it is taken in within ≤ 100 ticks after the core frees a place | "I cleared one place and it filled itself in" | D-177 |
| 3 | the core's 54-slot screen: the default chain should show a tooltip anyway, but there is **no acceptance item at all** (0 of 0.2.0's 36 items cover a tooltip / hover) | written as an explicit requirement + explicit acceptance: hovering must show the name + the enchantment / effect lines | no difference in normal use; if it ever regressed, this time it will be caught | D-178 |
| 4 | the Vibration Bean has **0 effect** on mobs (mobs are merely the trigger) | **every non-player mob** in the radius stops for **80 ticks (4 seconds)** | "the bean pops, and a ring of mobs stands still for 4 seconds" | author's rulings #4, #5; D-180 |
| 5 | the Vibration Bean only confuses players | players are confused **as before**, and mobs **additionally** stop | "I'm dizzy, and I can watch the mobs not moving" | D-180 |
| 6 | Q-Bounce only changes a mob's pose / displacement (the effect class has 0 overrides) | a non-player mob carrying the effect stops **the whole time**, at most **10 seconds** under the default field duration | "the chives pop and the mob stops; the instant the effect is gone it is back" | author's ruling #6; D-181 |
| 7 | a player carrying Q-Bounce | **unchanged** (a player is not a `Mob`; the mount / render looks are unchanged) | I still get bounced and I am not held in place | D-181 |
| 8 | trap damage to hostile mobs = the original value (the blast carrot at most 7, the Zangief Cane 7.5) | hostile mobs ×3 (at most **21 / 22.5**), players and non-hostiles **unchanged** | "mobs lose health faster, and being blasted hurts just as much as before" | D-184 |
| 9 | all 4 trap crops settle at their own original value | only **2** (the blast carrot, the Zangief Cane) take the multiplier | "why don't the fangs hurt more", "the Vibration Bean never had damage anyway" | author's ruling #11; D-184 |
| 10 | a mob locked to `noAi` by a command / another mod | after entering and leaving the stopped state it is **still** `noAi` | "the mob the command locked was not released" | author's ruling #8; D-179 |
| 11 | a cultivator in an unloaded chunk of the 3 × 3 | **not loaded, not rebound**; wait for the next round after the chunk loads | "the machine I built far away only hooked up after I came back" | author's ruling #2; D-177 |
| 12 | "non-player mob" is easily read as "hostile mob" | villagers, pets and animals **stop as well** | "my cow got frozen by the bean" | author's ruling #4; D-180, D-181 |
| 13 | the Vibration Bean's CONFUSION **does not filter friend or foe** (every player in the radius) | **unchanged**: teammates are confused as well, and a teammate's AI is not deleted | "my teammate got dizzy too, but he can still run" | D-180; §4.4 |
| 14 | after reloading the save / restarting, this version's state | the original value is persisted with the entity: while the effect is there it keeps stopping, and once it is gone it is restored to the original value; it is never permanently without an AI, and no dirty record is left | "after the restart the mobs that should move still move, and the locked ones were not released" | author's ruling #15; RT-54; §5.5 |

**Three headline rows**: row 1 (the core takes cultivators in automatically) and row 4 (mobs are held for 4 seconds) will be hit by every player; row 8 is the only change on the numbers surface, **but it holds only against hostile mobs**. The ones most easily misreported as bugs are row 9 (the fangs do not hurt more), row 12 (neutral mobs stop as well) and row 11 (a distant machine does not hook up).

---

## 9. Boundary list (table, from B-01)

Numbering starts at B-01. Each entry gives only "scenario / expected behaviour / basis / how it shows when not met"; the numbers are looked up in their own sections.

| No. | Scenario | Expected behaviour | Basis | How it shows when not met |
|---|---|---|---|---|
| B-01 | the core has 10 bound and there are still 5 unbound cultivators inside the 3 × 3 | filled to 15 within 100 ticks (fill while there is room, never beyond the allowance) | D-176, D-177 | it does not fill up, or fills past 16 in one go |
| B-02 | the core has 16 bound and there are still unbound ones inside the 3 × 3 | **not one is rebound**; the 16 bound entries do not move | D-177; RT-53 | the 17th squeezes one bound one out |
| B-03 | a cultivator's chunk inside the 3 × 3 is unreadable this round | skip that chunk, **no loading**, no error; try again next round | author's ruling #2; D-177 supplementary item | the core pulls 9 chunks into memory every 100 ticks |
| B-04 | a cultivator outside the 3 × 3 (one chunk away) | **never** rebound (criterion 1 is not met) | [`OutpostCoreBlockEntity:454-465`] | the range is void and cores snatch across chunks |
| B-05 | a cultivator inside the 3 × 3 but already bound by another allied core | not rebound, not taken (it is not "unbound") | D-177 | one cultivator with two owners |
| B-06 | the cultivator-side cache is empty while some core's list actually holds it (abnormal state) | rebinding **skips** it (any inconsistency between the two sides is skipped) | D-177 supplementary item | a second owner is created |
| B-07 | the cultivator's block entity has already been removed when the rebind writes | only the core side is written; the next round refuses to count it twice because of `hasCultivator` (idempotent) | [author's supplement] | the same coordinate appears twice in the list |
| B-08 | rebinding and core-breaking migration happen in the same tick | no double claiming: the migrated cultivator's cache is non-empty ⇒ not among the candidates; after exit ② clears the cache the next round takes over (≤ 100 ticks) | D-177 supplementary item; D-168 | the migrated cultivator hangs on two cores at once |
| B-09 | the core has no owner (produced by `/setblock`) | **no rebinding**, return directly | author's ruling #13; `emitBoundaryRing:535-537` | an ownerless core swallows every cultivator around it |
| B-10 | the core's list holds a dead entry (the block has been removed) and only 14 are really alive | still treated as "full" ⇒ nothing is rebound this round; the dead entry is removed by the lazy prune of the next `cultivators()` | D-176 supplementary item, D-177 rejection | rebinding prunes along the way and miscounts the coordinates bound this round |
| B-11 | two allied cores cover the same batch of unbound cultivators at once | each fills its own room; **one cell only ever lands on one core** (the first writer claims the cache) | D-177 supplementary item; §2.6 | two cores each write a copy in the same tick |
| B-12 | after a server restart (before the tickets are restored) the chunk is not loaded | rebinding skips it; it continues once the tickets are restored | author's ruling #2; [`outpost/CoreChunkTickets:31`] | 9 chunks are loaded all at once at startup |
| B-13 | hovering an **empty slot** among the 54 | **no** tooltip (not an empty frame) | D-178; vanilla behaviour | an empty slot pops an empty hint as well |
| B-14 | hovering an item with an enchantment / with one of this mod's effect components | the name + the enchantment line / effect line are all shown | D-178; §3.3 item 6 | only the name shows, the detail lines are lost |
| B-15 | hovering an item in a player-inventory slot | the same as in the 54 slots | D-178; §3.3 item 5 | there is a tooltip in the upper half only |
| B-16 | the tooltip goes beyond the screen's right edge | vanilla flips it to the left; this version does not take over the positioning | D-178 rejection item 4 | the text is clipped by the screen |
| B-17 | a non-allied player right-clicks the core | the screen does not open, so there is no tooltip to speak of; the access gates are unchanged | D-166 kept | the tooltip requirement is taken for a change of "who can open it" |
| B-18 | when the Vibration Bean triggers there are 3 mobs + 1 player in the radius | the player gets CONFUSION; all 3 mobs stop for 80 ticks | author's ruling #4; D-180 | only the one that triggered it stops |
| B-19 | the same mob is covered by another Vibration Bean within the 80 ticks | the window is **refreshed** to "now + 80", not stacked into 160; the original value records the first one only | D-179 supplementary item; D-180 | it stops for 160 ticks, or the original value is overwritten as `true` |
| B-20 | the mob dies / is removed while stopped | the persisted original-value record is deleted with the entity, no residue is left | author's ruling #15 | a dirty record stays in the save and affects that entity's next entry into the stopped state |
| B-21 | there are only players and no mobs in the radius | exactly as in 0.2.0 (CONFUSION only + the sound + removal) | §4.4 | the players' CONFUSION was broken for the sake of "deleting the AI" |
| B-22 | the mob was already `noAi` (locked by a command / another mod) | the restore writes `true` back, it is not released | author's ruling #8; D-179 | a mob the command locked gets "unsealed" |
| B-23 | the mob is knocked back / loses health / is pushed while stopped | as usual (deleting the AI changes no damage, health, drops, collision or knockback) | §0.4 item 5 | a mob whose AI was deleted becomes invulnerable / immovable |
| B-24 | the mob carries Q-Bounce and `poseMode = render` (the default) | it stops the whole time and is restored when the effect ends | author's ruling #6; [`FarmstormConfig:863`] | a "skip" loophole exists even under the default configuration |
| B-25 | `poseMode = mount` and the mob carries Q-Bounce | **the AI is not deleted**, and one already riding is not touched | author's ruling #7; D-181 | a mount runs 0 movement while carrying a `noAi` entity (a shell frozen in mid-air) |
| B-26 | the AI has already been deleted and the configuration then switches to `mount` | restore to the original value first, then mount | D-181 supplementary item | as above: a shell |
| B-27 | the effect expires / `/effect clear` / drinking milk / the entity dies / the chunk unloads | the first four go through "restore the original value + delete the record"; **the fifth (chunk unload) keeps the attachment and settles on load** | RT-55; author's ruling #15 | miss one ⇒ a persisted residue is left in the save (B-30) |
| B-28 | the effect is rewritten by the field every tick (the duration decreasing) | the entry point is idempotent: no second original-value record, no window reset | §5.3; D-179 supplementary item | the original value is recorded as `true` and the mob is locked for good |
| B-29 | the field writes a longer duration when the effect has 1 tick left | the effect is still there ⇒ it keeps stopping, no false restore | §5.3; RT-56 | the effect is still there but the AI has already come back |
| B-30 | before and after reloading the save (the original value is already persisted with the entity) | the effect is still there ⇒ it keeps stopping; it is not ⇒ restore to the **persisted original value** and delete the record; a record never remains at any moment | author's ruling #15; RT-54 | a persisted residue: the next entry into the stopped state reads the old record and the restore writes the wrong value |
| B-31 | a hostile mob is hit by the blast carrot with `raw = 6` | it takes `6 × 3 = 18` | D-184; §6.5 | "only multiplied at 7" ⇒ 18 becomes 6 |
| B-32 | a player is hit by the same explosion | the original value (`min(raw, 7)`, at most 7) | D-184; author's ruling #10 | the player is ×3 as well |
| B-33 | a neutral / passive mob is hit by the same explosion | the original value | D-182 | neutral mobs are ×3 as well (the criterion was widened to "non-player") |
| B-34 | a hostile mob is caught in the small explosions of the Melon Grenade / Molotov / Ubi Server / Lucky Berry | the original value (**not** the trap's one explosion) | D-184 supplementary item; §7 row 35 | the 6 non-trap explosions are ×3 as well |
| B-35 | a hostile mob is pricked by the fangs | vanilla damage (not touched here) | author's ruling #11 | vanilla `EvokerFangs` is changed for the sake of "all traps" |
| B-36 | a hostile mob steps on the Vibration Bean | 0 damage (it was 0 already) | author's ruling #11 | damage is conjured for the Vibration Bean for the sake of "all traps" |

36 entries. B-01 ~ B-12 belong to R1, B-13 ~ B-17 to R2, B-18 ~ B-23 to R3, B-24 ~ B-30 to R4, and B-31 ~ B-36 to R5.

---

## Appendix A · Register of differences between plan's original text and this version's measure

| # | plan line | plan's original text | This version's measure | Nature of the difference | Decision | Explanation |
|---|---|---|---|---|---|---|
| 1 | `:22` | "**all** trap crops' damage to hostile mobs becomes three times the original" | **only 2 of the 4 trap crops** participate (`blast_carrot`, `zangief_cane`); `fang_trap_cactus` goes through vanilla `EvokerFangs` (0 `hurt` in the repository) and `vibration_bean` has 0 damage | **narrowing** | author's ruling #11; D-184 | writing "all" literally would necessarily misjudge the acceptance (RT-58); this is not a slip of the pen but the measure, and 40's §7 ② has registered it |
| 2 | `:9` | "every 100 ticks it will try to bind ..." | the period is **100 ticks**, but it **must go through an independent phase gate**: the particle early-return gate of the existing `serverTick` (`:516-518`) would filter the period attached behind it into `LCM(40, 100) = 200` ticks, and it drifts with `boundaryParticleIntervalTicks` (default 40, configurable 20 ~ 1200) | **refinement (+ [author's supplement]: the gate must be independent)** | D-176 | this is the version's biggest implementation trap (§2.3); "100 ticks" is a requirement in plan, and in the code it gets changed by the gate in front of it |
| 3 | `:10` | "when opening the Outpost Core's storage space, hovering the mouse cursor over an item shows the item details (just like opening a normal chest)" | the default chain **should show it anyway** (`OutpostCoreScreen` overrides `renderBg` only, `renderTooltip` has 0 hits in the repository); this version is "**guarantee + acceptance**", not "implement from scratch" | **rewrite (wording: from "implement" to "guarantee")** | D-178 | the three pieces of evidence are in §3.2; the acceptance passing with not one line changed is the correct result (RT-59) |
| 4 | `:9` | "when the number of bound crop cultivators is less than 16" | **16 is read from config** `outpost_core.cultivatorCapacity` (default 16, range 1 ~ 64); the body text writes "default 16" and does not hard-code it | refinement | D-177; [`FarmstormConfig:949-951`] | hard-coding 16 makes rebinding and the placement check diverge when "config is changed to 8" |
| 5 | `:9` | "within its influence range" | the range = **that core's 3 × 3 chunks** (`inRange`, `RANGE_CHUNKS = 1`), and it **scans loaded chunks only and forces no loading** | refinement | author's ruling #2; D-176 | "influence range" has a single value in this project and is not a new range set up here (0.2.0's 35 already uses the same reading) |
| 6 | `:9` | "unbound crop cultivators" | the judgement form = "the cultivator-side cache is empty ∧ the target core has not bound it itself"; if the two sides are inconsistent (the cache points at a core but it is not in that core's list) ⇒ **skip** | refinement | D-177 supplementary item | the strict form of "unbound" needs a global index, which this version has not; the three forms are in §2.5 |
| 7 | `:9` | "every 100 ticks it will try to bind" | the period is **a constant 100**, and **no config key is added**: this version's config adds only +1 key and it is already taken by `trap.damageMultiplier` (175 → 176) | refinement (+ [author's supplement]: why it is not a key) | D-183, D-185 | one more key would make it 177, in conflict with D-185's 176 |
| 8 | `:14` | "when a non-player mob is affected by the Vibration Bean, its AI is deleted for 4 seconds" | the object = **every** non-player mob in range (**not** the one that triggered it); the window is **80 ticks** (a half-open interval); the original value is recorded and restored per the original value | **clarification + refinement** | author's rulings #4, #5, #8; D-179, D-180 | "affected by" has no counterpart in 0.2.0 (mobs are merely the trigger); this version defines its range surface for the first time |
| 9 | `:18` | "when a non-player mob gains the Q-Bounce effect, its AI is deleted" | the duration = **the lifetime of the effect** (no fixed number of seconds); **mount mode skips**; the restore follows the original value; the restore criterion is "is the effect still there" | refinement (two narrowings) | author's rulings #6, #7, #8; D-181 | plan writes neither "for how long it is deleted" nor mount; both are fixed by rulings |
| 10 | `:22` | "damage to hostile mobs" | hostile = **`Enemy` ∪ `MobCategory.MONSTER`** (a union, covering other mods); players are never included | refinement | author's ruling #9; D-182 | the repository's current measure has only `Monster`; the new criterion is **only** used for the ×3 gate and does not touch the trigger surface (RT-57) |
| 11 | `:5` | "0.2.0's existing content may not be changed unless **this document explicitly requires it**" | `:9` is exactly that "explicit requirement" ⇒ D-165's two sentences "no periodic rebinding at runtime" and "do not secretly rebind at runtime" are **partially superseded by this version**; the core-selection criteria / the 16 cap / one-to-one / the single truth on the core side are **all kept** | **authorisation (local supersession)** | author's ruling #12; D-165; D-176 | this is **supersession**, not "addition": the binding write points go from 2 to 3 (§2.7) |

Three notes under the table:

1. **Row 1 is the only difference that "must contradict plan word for word"**: `plan:22` says "all", and this version has only 2. Any downstream document that copies "all traps ×3" is wrong (RT-58).
2. **Rows 2 and 7 are "two landings of the same sentence"**: 100 ticks can neither be filtered into 200 ticks by the particle gate (row 2), nor become a config key (row 7). Only when both are done is it "every 100 ticks".
3. **Row 3 is this version's only difference where "the conclusion differs on the surface from plan but is in substance stricter"**: plan wants "show", this version wants "guarantee that it shows + that it can be accepted". It does not reduce the work; it only moves the work from "writing code" to "writing the acceptance + proving that the chain was not bypassed".
