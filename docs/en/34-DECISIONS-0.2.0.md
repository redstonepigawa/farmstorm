# 34 · v0.2.0 Decision Record

> Project: Farmstorm · v0.2.0
> Version: **0.2.0**, baseline **the published 0.1.1 state** (`gradle.properties:11`, git tag `v0.1.1`)
> Basis: the 4 original requirements of `plan0.2.0.txt` —
> ① requirement 1, "changes to the crop behaviour of this mod": "tier the crops of this mod into Common, Rare and Epic" (`:9`), "no crop of this mod is any longer allowed to be planted on farmland" (`:13`), "and each trade can be made once only" (`:14`), "Lucky Berry no longer produces Rare or Epic crops" (`:15`)
> ② requirement 2, "new block": "block name: Crop Cultivator" (`:19`), "block hardness and blast resistance referencing the anvil" (`:20`), "after thirty seconds it produces 2 of the corresponding crop and 2 seeds and stores them automatically in the storage of the Outpost Core it is bound to" (`:21`)
> ③ requirement 3, "large rework of the Outpost Core block": "retire every change of this mod that concerns farmland" (`:26`), "the Outpost Core is no longer bound to farmland; it now owns a list of capacity 16" (`:27`), "right-clicking the Outpost Core opens its built-in storage (equivalent to a large chest)" (`:28`), "it produces a visible link with the Outpost Core (no entity, no effect on any block or entity, indicating the position only)" (`:29`), "when the Outpost Core is destroyed, all the items it stores drop" (`:30`)
> ④ requirement 4, "adjustment of the Product Converter": "remove its function for now, leaving it a decorative block only" (`:33`)
> Plus the **19 author rulings** of §2 of `.work/v020-BRIEF.md` (settled on the spot over three rounds of interviews, transcribed verbatim in §2 of this document) and `28-DECISIONS-0.1.1.md` (the 24 decisions of 0.1.1 and the source of the style of this document).
> Status: **the single source of truth**. Every convention of v0.2.0 follows this document; the downstream **35 (design) / 36 (technical) / 37 (tasks) / 38 (master prompt) / 39 (phase prompts) cite this document only and invent nothing themselves**. Where this document conflicts with the source code, it is reported to the author and no trade-off is made on one's own (the conflicts found while writing are listed item by item in §9).
> Coverage: **D-148 ~ D-172, 25 entries in total**, consecutive with no gaps. The additions / changes / deletions against 0.1.1 are registered in §3, the tier table in §4, the red-line convention and the new network packet registration in §5, and the supersession relation to the 0.1.1 decisions in §6.

---

## 0. Reading conventions

### 0.1 Numbering (from D-148)

The decision numbering of this document starts at **D-148** (28 of 0.1.1 goes up to D-147). The other numbering series are **only cited here, never opened anew**; the continuation table is copied verbatim from §0.1 of `BRIEF`:

| Series | 0.1.1 up to | **This document from** | Written by |
|---|---|---|---|
| Decisions D-xx | D-147 | **D-148** | this document (34) |
| Tasks F-xx | F-212 | **F-213** | 37 |
| Phase | 31 | **32** | 37 |
| Milestones M-xx | M27 | **M28** | 37 |
| Hard constraints HC-xx | HC-96 | **HC-97** | 38 |
| Prohibitions N-xx | N-92 | **N-93** | 38 |
| Risks RT-xx | RT-41 | **RT-42** | 36 |
| Adjudications A-xx | A-61 | **A-62** | 37 |
| Implementation verdicts V-xx | V-191 | **V-192** | `.work/PLAN.md` |
| Document number | 33 | **34** | this document |

Four numbering rules:

1. This document issues **D-xx** only (D-148 ~ D-172). When another series is cited the full prefix is written ("RT-42", "HC-97", "F-213", "A-62"); no number is invented here, and no downstream number is spent in advance.
2. The continuation table comes from §0.1 of `BRIEF`; this document neither recomputes it nor sets up a second one.
3. D-124 ~ D-147 of 0.1.1 still follow 28, **unless §6 names them as superseded**; every clause §6 does not name continues in force (typical case: the "ally" boundary definition of D-138 **is not superseded**, and the overlay and the cultivator binding of this release keep using it).
4. This document **opens no new adjudication**: the few places in the writing-time report (§9) that need the author's ruling are registered by 37 starting at **A-62**, and the corresponding decision here changes only its "Notes" field.

### 0.2 Glossary

This section gives each term its **decision boundary**, not a circle of synonyms. When the five downstream documents meet these words, they are used by the boundary of this section.

**Tier (`CropTier`)**
: a **static property** of the crop profile, with exactly three values: `COMMON` (Common) / `RARE` (Rare) / `EPIC` (Epic). Decision boundary: the tier belongs to the **profile / item id**, not to the item instance — any stack, any NBT state of the same id is the same tier, and it does not change with the player, the dimension or time; it is **not** a data component, **not** synchronised with the item stack, and **not** in `BuiltInRegistries`. The tier has **nothing to do** with the "stack tier" (`CropProfile.THROWABLE_STACK_TIER = 16` / `OTHER_STACK_TIER = 64`), and is not derived from `CropType` / `CropCategory` either (D-149, D-150).

**Cultivator**
: equal to **`farmstorm:cultivator` (the Crop Cultivator)**, this one block and its block entity. Decision boundary: "cultivator" in this document **never** means the cultivating action, never a UI (it has no UI, D-160), never the core, never the converter. "This cultivator" means that one block in the world; "the contents of this cultivator" means the one pending item inside it (only one at a time, D-162).

**Binding (cultivator ↔ core)**
: the persistent relation between one cultivator and **exactly one** ally core. Decision boundary: the binding is **one-to-one**, its unit being "one cultivator"; one core binds **16** cultivators at most (`outpost_core.cultivatorCapacity`), and one cultivator has **0 or 1** core at any one moment. The **only truth of the binding is on the core side** (the core's 16-entry list); the copy in the cultivator's BE, "whom I am bound to", is a reference cache and must be validated against the core-side list before use (D-165). An unbound cultivator still exists and is still visible, it is simply **not usable**. The binding is **not** "automatic ownership inside a range" — it does not drift with distance.

**Storage**
: the **54-slot** container of the Outpost Core (a double chest). Decision boundary: it is **not** "capacity 16" — 16 is the **cultivator allowance**, 54 is the **number of slots**, the two are not synonymous and not interchangeable (ruling #8, RT-44). The storage is a **general container** (the player puts things in by hand, the cultivator delivers automatically), **not** the input slot of the cultivator (the cultivator has no slot UI), **not** the player's inventory, **not** a dropped item. When the core is destroyed the items inside **drop** (D-167).

**Link**
: **a line frame the client draws on the screen**, saying "this cultivator is bound to this core". Decision boundary: it is **purely visual** — no entity, no collision, no blocking, no light, no particles, no registry surface; no server-side logic (binding, production, migration, protection) **may** depend on whether the link exists, nor on whether the client received the link packet. The line frame is sent only while "the player is inside the 3×3 chunks of that core" (D-169). It is not a block, not an entity, not a particle.

**Retire the farmland system**
: "retire" in this release = **comment out the call sites only** — comment out the **entry points** of the farmland system (the registration and the implementation of the 2 event listeners, the 1 start-up backfill call, the reading and the definition of the farmland config, the 3 language keys), so that this set of rules **no longer takes effect** at run time. Decision boundary: it is **not deleting code** (`FarmlandLedger` with its 555 lines and `FarmlandGuard` with its 225 lines keep their class bodies unchanged, still compile and stay in the source tree); **not deleting the save** (`farmstorm_farmland.dat` is not read, not written, not deleted, D-154); **not deleting the blocks** (the 15 crop blocks are still registered, D-155). Any wording such as "delete the farmland code" or "clear the farmland data" is wrong (ruling #3, RT-42).

**Out of stock**
: the state in which a trade offer **cannot be traded again** after one deal and before the **vanilla restock** arrives. Decision boundary: what is out of stock is **this one offer**, not this villager, not all villagers, not this tier of crop; the offer **still shows in the list** (visible, clickable, the deal refused), **does not disappear, does not change id, is not hidden**; it recovers automatically once the restock arrives (`maxUses = 1` + the vanilla restock mechanism). Out of stock ≠ removed ≠ "gone for good after one purchase" (ruling #7, D-158).

**Ally**
: equal to **the core owner himself + same-team allies**; the judgement goes by **UUID / name** and **does not look at whether the target is online** — this follows **D-138** of 28 and is unchanged here. Decision boundary: the three places of this release that use it (choosing a core when binding a cultivator, sending the link, the access judgement of the core storage) **share this one set**; mobs are always treated as "non-ally" (mobs have no team affiliation).

**Migration**
: when a core is dismantled, **rebinding** the cultivators bound to it onto another core. Decision boundary: what migrates is the **binding relation**, not a block, not an item, not the save; when not a single one can be migrated, the cultivator **stays where it is and is marked unbound** (it does not disappear, does not turn to dirt, does not drop) — this is the biggest difference from the farmland migration of 0.1.1 (D-168).

**Working state**
: the state in which the cultivator **is producing** = "there is a crop in the slot" and "it is not paused because the core is full". Decision boundary: an empty slot is not the working state; being paused for fullness is not the working state either; only the working state plays the page-flip animation, everything else shows the static first frame (ruling #12, D-161, D-164).

### 0.3 The four-field body of this document

Every decision has four fixed items, **none may be missing**, in a fixed order:

| Field | What is written | What must not be written |
|---|---|---|
| **Conclusion** | one sentence, imperative, directly executable | conditional clauses, alternatives, "could consider" |
| **Basis** | the ruling number of §2 of `BRIEF`, or the `file:line` of §1 / the source code; giving both is best | "because it is reasonable", "intuitive", "industry convention" |
| **Landing site** | the `path` or `class#method` to change | code snippets, algorithm steps |
| **Notes** | **what this entry affects in passing** and **which alternatives it rejects** | implementation details (that is 36's job), task scheduling (that is 37's job) |

Four writing disciplines:

1. **The "supplemented by this document" marker**: every convention derived by this document rather than given directly by `BRIEF` is marked **[author's supplement]** in front of the sentence. An entry carrying this mark can be overruled by the author; an entry without it is a transcription of `BRIEF` / plan, and overruling it amounts to overruling an author ruling.
2. **Number discipline**: every value in this document traces back to one item of §1 / §2 / §3 of `BRIEF`, or is arithmetic on two given values (such as 19 − 8 = 11, 93 + 4 = 97). Where a **new value** is needed, it is written as "fixed by 35 / 36 within the framework of this entry", and this document does not invent one.
3. **Forbidden wording**: no open items, no to-do markers, no conclusions with vague conditions; no second-person honorific — always "the author".
4. **Relation to the source code**: where this document conflicts with the source code, it is reported to the author and no trade-off is made on one's own. The conflicts found while writing are listed item by item in §9, and `BRIEF` is **not** changed.

---

## 1. Decision list (D-148 ~ D-172)

The numbers this version uses are **D-148 ~ D-172, 25 entries in total**, consecutive with no gaps. Grouping: §1.1 version and tiering 4 entries, §1.2 the planting chain and farmland 4 entries, §1.3 Lucky Berry and trading 4 entries, §1.4 the cultivator 5 entries, §1.5 the core 4 entries, §1.6 the link / the converter / the registry surface / the red lines 4 entries (4 + 4 + 4 + 5 + 4 + 4 = 25).

### 1.1 Version and tiering (D-148 ~ D-151)

#### D-148 · Version number 0.2.0, baseline = the published 0.1.1 state

- **ID**: D-148
- **Conclusion**: change `mod_version` to **0.2.0** and take **the published 0.1.1 state** as the baseline; this release delivers **six Chinese documents, 34 ~ 39**, and the English ones are produced by the translation round.
- **Basis**: ruling #1; `gradle.properties:11` currently reads `mod_version=0.1.1` (§0 and row 1 of §3.3 of `BRIEF` record it as `:10`, see §9 ①); "version number: 0.1.1" at `plan0.2.0.txt:2` and "with 0.1.0 as the main version" at `:5` are copies of an old value (the parenthetical of ruling #1); the end of §1.2 of `BRIEF` (`git diff --stat v0.1.1 -- src` is empty); the six-way division-of-labour table of §5 of `BRIEF`.
- **Landing site**: `gradle.properties:11`; `docs/zh/34-DECISIONS-0.2.0.md` ~ `docs/zh/39-PROMPT-TASKS-0.2.0.md` (6 new); `docs/en/34` ~ `docs/en/39` (the translation round); `README.md` (the increment section).
- **Notes**: the version number has only one source, `gradle.properties:11`; the other documents cite it and do not each declare it. "Baseline = the published 0.1.1" is a hard precondition: `src/` has 0 changes since the v0.1.1 tag and every increment of this release is stacked on it, so any "while I am here, fix an old problem of 0.1.1" is out of scope (it is a pre-existing blemish, registered in §8). The 0.1.1 / 0.1.0 in the body of the plan are old values; writing them as the new version number would give this release the same name as the published 0.1.1 and make the jars indistinguishable. Rejected: "write 0.1.1 as the plan literally says" — ruling #1 has made clear that it is a copy of an old value.

#### D-149 · Three tiers and the assignment of the 22 crops: 11 / 7 / 4

- **ID**: D-149
- **Conclusion**: give each of the 22 crop profiles **exactly one tier**: **Common 11 / Rare 7 / Epic 4**; of these, `ubi_server`, `toxic_ubi_server` and `point_defense_fungus` are **added to Rare** (the plan's original text lists only 4 Rare). The item-by-item list is only in §4 of this document.
- **Basis**: ruling #2; the item-by-item naming table of §3.2 of `BRIEF`; `plan0.2.0.txt:10-12` (the plan has only 4 Rare entries); `CropProfile.java:32-40` (the carrier of the profiles), `Farmstorm.java:112-115` (the existing assertion of 22); §1.1 of `BRIEF` (`rarity` / `quality` / `品质` have 0 hits across the whole repository; the tier is a brand-new concept).
- **Landing site**: `util/CropProfile.java:47-128` (add a tier to each of the 22 profiles); `util/CropTier.java` (new enum); §4 of this document (the only list).
- **Notes**: **11 + 7 + 4 = 22 is a hard constraint**: one wrong entry changes trading (D-157), cultivator admission (D-162) and the Lucky Berry pool (D-156) at the same time (RT-49). The 3 supplementary tierings do not come from the plan but from the interview; the plan's "4 Rare" is an old convention and must not be rolled back. `compressed_puffed_wheat` is Rare while its ingredient `puffed_wheat` is Common, which means the two product levels of the same crop straddle tiers — the author decided that, it is not a slip. Rejected: "derive the tier from `CropType` / `CropCategory`" (say "edible = Epic") — among the edible ones `power_beet` is Rare and `lucky_berry` is Epic, so the derivation contradicts itself on the spot.

#### D-150 · The carrier of the tier = the 9th field of `CropProfile` + the `CropTier` enum

- **ID**: D-150
- **Conclusion**: add a **9th field** `CropTier tier` to the `CropProfile` record and create the `CropTier` enum (`COMMON` / `RARE` / `EPIC`); **add no** `data_component_type`, write no NBT to the item, register no new object.
- **Basis**: ruling #5; `CropProfile.java:32-40` (currently 8 fields); row 2 of §3.3 of `BRIEF`; §1.1 of `BRIEF` (the only data component is `farmstorm:temporary_enchantments`, `FarmstormDataComponents.java:15-18`; the 13 hits of `tier` are all the stack-tier constants).
- **Landing site**: `util/CropProfile.java:32-40` (add the field), `:47-128` (fill in the value for the 22 profiles); `util/CropTier.java` (new enum, with `translationKey()`). ⚠️ Adding a 9th record field touches the **7 profiles that call `new CropProfile(...)` directly** (`CropProfile.java:58,87,92,97,102,117,125`); miss one and it does not compile.
- **Notes**: a static field + an enum = **zero registry increment** (`data_components` is still 1 in the 97 arithmetic of §3.1, D-171), and **zero save change** as well: the tier goes with the id, and an item stack stores only the id. Made a data component, "the same item with two tiers" would have to be told apart by NBT, and the `BY_ID` reverse lookup of `CropProfile` (`:145-146`), the trade table and the cultivator admission would all have to change with it, plus a component default value generated for each of the 22 profiles (the rejection reasons are in §7 item 2). The key name of `translationKey()` and the three Chinese tier names are fixed by 36 following the existing convention of `CropCategory.translationKey()` (`CropCategory.java:38-40`).

#### D-151 · The visual presentation of the tier: item name color plus one tooltip line

- **ID**: D-151
- **Conclusion**: **color the names of the 22 crop items and the 7 seed items by tier** (Common white / Rare blue / Epic gold), add **one line** of tier description to the tooltip; **never change the textures**.
- **Basis**: ruling #14; row 14 of §3.3 of `BRIEF` (22 crops + 7 seeds); §3.5 of `BRIEF` (the textures of the 22 crop items unchanged); the existing tooltip entry `ModifiedCropItem.appendCropTooltip` (`ModifiedCropItem.java:57-60`, `SeedItem.java:28-31`).
- **Landing site**: `item/ModifiedCropItem.java` (name color + the tooltip line), `item/SeedItem.java` (the same entry, a seed following its parent crop); 3 language keys for the tier names + 1 for the line format (the key names fixed by 36).
- **Notes**: the three colors **borrow the white / blue / gold of the vanilla rarity**, and add no texture, change no model and change no inventory display size. The name color must go through the style of `Component` (`withStyle`) and must not be painted a second time in the GUI — otherwise an item of the same name would not change color in an anvil rename, on a sign, in the chat bar or in a chest preview, and the same item would show two colors in two interfaces. The tooltip gets **one line only** and is not merged with the existing `CropCategory` line (that is another set of semantics, `CropCategory.java:15-27`). Rejected: "switch to the vanilla `Rarity` enum" — its grades do not line up with the three tiers of this release, and it would take over the name color and the tooltip format at once, which amounts to handing the tier semantics to vanilla; adding a fourth tier later would then be a dead end.

### 1.2 The planting chain and farmland (D-152 ~ D-155)

#### D-152 · Crops are no longer planted: both paths are closed

- **ID**: D-152
- **Conclusion**: make "the crops of this mod cannot be planted" hold on **both paths** at once: ① **change the farmland criterion of `ModifiedCropItem.isPlantableSoil` to a constant `false`**; ② have the 7 `SeedItem`s **override `useOn` to return `PASS`**, cutting off the vanilla sowing of `ItemNameBlockItem` / `BlockItem`.
- **Basis**: ruling #4; rows 3 and 4 of §3.3 of `BRIEF`; RT-48 (closing only one path leaks); `ModifiedCropItem.java:101-107` (the criterion), `:72` (the only call site), `:53-55` (`canPlantAt` has 0 callers across the repository and is **not** the point to close here); `SeedItem.java:16` (`extends ItemNameBlockItem`, overriding the tooltip only), `:28-31`; §1.2 of `BRIEF` (`FarmstormCropBlock` overrides neither `mayPlaceOn` nor `canSurvive`, so the block side never blocked anything).
- **Landing site**: `item/ModifiedCropItem.java:101-107`; `item/SeedItem.java` (the new `useOn` override); `block/FarmstormCropBlock.java` (untouched).
- **Notes**: **the division of labour of the two paths** — `ModifiedCropItem` goes through the custom sowing of "placing the block itself" (carrying the 15 profiles that have a crop block, of which 8 have a seed = the crop itself), and the 7 standalone seeds go through the vanilla `BlockItem.useOn` (those 7 of `SeedItem`). Close only the former and the 7 standalone seeds can still grow crops on farmland, which is exactly what RT-48 says. `canPlantAt` is dead code: this release does not treat it as a point to close, and **does not delete** it either (deleting dead code is pre-existing cleanup and outside the requirements of this release). The crop blocks **stay registered** (D-155), so "cannot be planted" can only be cut on the item side — expecting the block side to refuse is wrong, as `FarmstormCropBlock` has no `mayPlaceOn` override. **The behaviour change of the third entry point**: `DeployableCropItem#useOn` (`:36-43`) calls `ModifiedCropItem.plantCrop` first and falls back to `super.useOn` once the criterion is a constant `false`, so the **7 deployable items** (the 7 profiles of `CropType.DEPLOYABLE`) right-clicked on farmland **place their deployed form** rather than "doing nothing" — this is the visible consequence of cutting off plantability, not a third path left unclosed (acceptance is written as "can be placed, cannot be planted"). Rejected: "change only `isPlantableSoil` to a constant false" (§7 item 5).

#### D-153 · Retire the farmland system = comment out the call sites only (the whole system disabled)

- **ID**: D-153
- **Conclusion**: **comment out every entry point** of the farmland system — the `:75` registration line and the `:76` registration line of `FarmstormGameEvents`, the `FarmlandLedger.backfill` call at `:171`, the implementations of the two listeners (`:241-265`, `:277-281`), and the **definition** of `outpost_core.farmlandBonus` (`:909-912`); the class bodies of `FarmlandLedger` and `FarmlandGuard` **keep not one changed line and stay in the source tree**.
- **Basis**: ruling #3 (**comment out the "call sites" only**); rows 5, 6 and 7 of §3.3 of `BRIEF`; RT-42; `FarmstormGameEvents.java:75,76` (registration), `:241-265` (the hoeing interception), `:277-281` (the trample protection), `:166-176` (the start-up gate, where `:171` is the backfill and `:170` is `restoreAll`); `FarmlandLedger.java` 555 lines / `FarmlandGuard.java` 225 lines (`BRIEF` §0).
- **Landing site**: `event/FarmstormGameEvents.java:75`, `:76`, `:171`, `:241-265`, `:277-281`; `config/FarmstormConfig.java:909-912` (**comment out the definition only**; the field declaration `:227` is **kept**, the reason being in A-62); the 3 language keys (`datagen/ModLangProvider.java:403`, `:412-413`).
- **Notes**: **three distinctions that must be spelled out** (getting any one of them wrong makes this release wrong): ① the retirement is **not deleting code** — the two classes still compile and can still be restored by mistake, which is deliberate (the mitigation of RT-42 is "leave one line of reason at the comment", not "delete it clean"); ② the retirement is **not deleting the save** (D-154); ③ the retirement is **not deleting the blocks** (D-155). `CoreChunkTickets.restoreAll` (`:170`) is **kept**; it serves the core's chunk tickets and has nothing to do with farmland. **The granularity of the commenting follows A-62**: **comment out the definition `:909-912` only and keep the field declaration `:227`** — keeping the declaration is what lets the two classes `FarmlandLedger` / `FarmlandGuard`, which no longer have any caller, still compile (they read this field directly); the cost is that **the field is permanently `null`** and that restoring a listener by mistake would NPE, the mitigation being in RT-42 (so the reason line at the comment must also say "the field is permanently null", otherwise whoever restores it will take it for a config entry that is simply missing). Every comment must carry one line of reason ("ruling #3: v0.2.0 retires the farmland system, do not restore") — the whole risk of RT-42 rests on that line, and without it the next person will delete these comments as mistaken ones. **Note that `onServerTickPost` itself is kept**: it still has to run `CoreListBroadcaster.tick` (`:175`), so only the `:171` line inside it is commented out (§9 ⑦). Rejected: "delete the two classes wholesale" (§7 item 1).

#### D-154 · `farmstorm_farmland.dat` is not read, not written, not deleted

- **ID**: D-154
- **Conclusion**: `farmstorm_farmland.dat` is **neither read nor written nor deleted** in this release; once the call sites are commented out this `SavedData` is no longer `get()`, and the file stays in the save as it is.
- **Basis**: item 1 of §3.6 of `BRIEF`; `FarmlandLedger.java:44` (the persisted name `farmstorm_farmland`), `:228-244` (`save`), `:246-264` (`load`); §1.2 of `BRIEF` (`dataVersion=2`, `bindings` / `byCore` / `orphans`); `FarmstormGameEvents.java:171` (the only start-up `get()` point).
- **Landing site**: `farmland/FarmlandLedger.java` (the class untouched, `get()` no longer called); `event/FarmstormGameEvents.java:171`.
- **Notes**: not deleting is a **data-safety** decision: once deleted there is no way back, while "not read in this release" is merely temporarily unused — the farmland ownership information of an old save stays on disk, so if this system is ever restored (or a migration tool written) the original data is still there. Not writing is hard too: once a `SavedData` is `get()`, saving rewrites the file, and even with unchanged content it touches `dataVersion` and the timestamp, which equals "quietly changing a piece of data we have declared unmaintained". **The division of labour with D-155**: D-154 governs the save file, D-155 governs the registered objects, and neither is "deleting code". Rejected: "clear the old .dat at start-up so it does not confuse anyone" — that would destroy a piece of data in the player's save without him noticing, a head-on conflict with "do not delete the save".

#### D-155 · The crop blocks and items stay registered, they simply cannot be planted

- **ID**: D-155
- **Conclusion**: the 15 crop blocks, the 22 crop items, the 7 seed items and the 26 block loot tables **all keep their registration and their output**; this release cuts only the one path "the player plants them in the ground".
- **Basis**: ruling #4 (**keep the registry**, cut the plantable path outright); row 3 of §3.3 of `BRIEF`; `FarmstormBlocks.java:26-43` (the 15 crop blocks), `FarmstormItems.java:36-97` (22 crops + 7 seeds); §1.2 of `BRIEF` (the 8 crops whose "seed = the crop itself"); scan A §2.7 C (15 of the 26 loot tables are crop blocks).
- **Landing site**: `registry/FarmstormBlocks.java`, `registry/FarmstormItems.java`, `datagen/ModLootTableProvider.java` (**none changed**; this entry is the register of "not touched").
- **Notes**: the reason for keeping them is **the stability of the registry surface and of old saves**: deleting a block would turn these blocks in an old save into air (with the drop judgement inside them), and would make a published 0.1.1 save "lose blocks" in 0.2.0 — far worse than "cannot be planted". With the block kept, its remaining role in the world is "decoration + loot table + the source of the items the cultivator produces"; the player's acquisition path moves to the cultivator (D-163) and trading (D-157) instead of planting. ⚠️ "Blocks kept + not plantable" means naturally generated crops injected by a data pack can still appear, which this release does not handle (it is outside the 4 requirements of the plan). Rejected: "delete the blocks together with the items" — that would cut a large chunk off the registry surface and conflict head-on with `93 + 4 = 97` of §3.1.

### 1.3 Lucky Berry and trading (D-156 ~ D-159)

#### D-156 · Lucky Berry pool 19 → 11 (Common only)

- **ID**: D-156
- **Conclusion**: change `CropProfile.LUCKY_BERRY_POOL` from **19 entries to 11**, and the 11 are **exactly the full Common set**; change the start-up assertion `poolSize == 19` at `Farmstorm.java:119` to `== 11` in the same pass, keeping the other half, "contains no `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`".
- **Basis**: `plan0.2.0.txt:15` ("Lucky Berry no longer produces Rare or Epic crops"); row 16 of §3.3 of `BRIEF`; RT-50 (the assertion goes red); `CropProfile.java:139-143` (currently 19), `LuckyBerryRoller.java:172` (the only place the pool is taken), `:175` (equal probability, `nextInt(pool.size())`), `Farmstorm.java:118-121` (the assertion).
- **Landing site**: `util/CropProfile.java:139-143`; `util/LuckyBerryRoller.java:172` (the code unchanged, only the content of the pool replaced); `Farmstorm.java:119` (the assertion value).
- **Notes**: the arithmetic is **19 − 8 = 11**. The 8 removed = the 6 Rare in the pool (`zangief_cane`, `friend_chives`, `power_beet`, `ubi_server`, `toxic_ubi_server`, `point_defense_fungus`) + the 2 Epic (`awakened_torchflower`, `awakened_pitcher_plant`); the other 3 (`compressed_puffed_wheat`, `lucky_berry`, `wild_apple`) **were never in the pool** (the explicit exclusion at `CropProfile.java:139-143`). The roll is still **equal probability, no weights**, and this release does not change the roll algorithm. RT-50 must be read on the **corrected convention**: that assertion only feeds `LOGGER.info`, **does not throw, does not exit**, so not syncing it does **not** keep the server from starting, it **prints one `FAIL` on every start-up and the log stops telling the truth** (the correction of RT-50 in §4 of `BRIEF`). Changing the assertion is still to be done, but it is "making the log tell the truth", not "saving the day". The plan's semantics is "no longer produces Rare and Epic", **not** "weight by tier" and **not** "keep only some besides Common" — the pool = the full Common set, no more, no less. Rejected: "turn the pool into a tier-weighted one" — the plan has no such requirement, and weighting would also tangle the word "lucky" with the probability table (the 8 grades of `lucky_berry.chance`).

#### D-157 · 3 farmer trade offers, unlocked by tier and by level in order

- **ID**: D-157
- **Conclusion**: add **3 trade offers** to villagers of the **farmer profession**, one per tier, unlocked in order by three level positions (novice → Common, journeyman → Rare, master → Epic, the example convention of ruling #6); each offer **takes emeralds only** and gives the crops of this mod of that tier.
- **Basis**: rulings #6 and #18; `plan0.2.0.txt:14` ("a villager with the farmer profession additionally unlocks one trade at every level stage, exchanging emeralds for the crops of this mod"); row 15 of §3.3 of `BRIEF`; §1.4 of `BRIEF` (`villager` / `trade` have 0 hits across the whole repository; trading is entirely new, with no existing pattern); RT-46 (the signature of `VillagerTradesEvent` in 21.1.213 has to be checked).
- **Landing site**: a new trade registration class under `event/` (subscribing to `VillagerTradesEvent`) + the 3 offer constructors; the event-bus mounting of `Farmstorm`; the `trade` group of `config/FarmstormConfig.java` (6 keys, see D-159).
- **Notes**: the 3 offers **are not 3 crops** but 3 **tiers** — each offer covers every crop of that tier (11 / 7 / 4); "which one a given offer sells" is fixed by ruling #18 as **one drawn at random by tier** (neither the 22 changed nor a rotation introduced), at a known and **accepted** cost: collecting all 4 Epic takes about **8.3 master farmers** in expectation (a harmonic number) and Rare about **18.2 journeymen** (both numbers are the original estimates of ruling #18; this document neither changes nor recomputes them); how many are given per deal is still fixed by 35 within the framework of D-157. The unlock levels are **1 / 3 / 5** (novice / journeyman / master, fixed by 36; vanilla has more than 3 level positions, and the ruling gives only three examples, see §9 ⑪). **The Epic tier can only be obtained through trading** (it does not enter the cultivator, D-162, and its crops have no cultivating path either), so the third offer cannot be omitted and cannot be merged into the Rare one. Rejected: "put all 3 offers on the lowest level" — a conflict with the "unlocked in order" of ruling #6, and it would leave high-level villagers with no new content at all.

#### D-158 · "Each trade can be made once only" = out of stock + wait for the restock (not the offer disappearing)

- **ID**: D-158
- **Conclusion**: `maxUses = 1` on every offer; after one deal the offer goes **out of stock** (still shown in the list, no longer tradeable) and recovers automatically once the **vanilla restock** arrives; **no offer is deleted, nothing is hidden, no id changes, no timer is written**.
- **Basis**: ruling #7; `plan0.2.0.txt:14` ("each trade can be made once only"); row 15 of §3.3 of `BRIEF` (`maxUses=1`); the parenthetical of item 7 of §2 of `BRIEF` ("equivalent to `maxUses=1` + the vanilla restock mechanism").
- **Landing site**: the `MerchantOffer` construction of the 3 offers (the `maxUses` parameter); no custom out-of-stock / restock logic is added.
- **Notes**: both out of stock and restock go through the **vanilla mechanism**: `maxUses = 1` makes vanilla count the deal as sold out, and the vanilla periodic restock resets the uses to zero — this release writes no second timer, stores no NBT and builds no GUI. The reverse of this entry is tombstone 4: rendering "each trade can be made once only" as "the offer disappears after one deal" is wrong (the player thinks he has been cheated, the list is one item shorter, and the restock has nowhere to happen). The restock period, the workstation, the level experience and other vanilla details are outside this document and are checked by 36 (RT-46). Rejected: "`maxUses = 1` + resetting the uses every tick" — that equals unlimited trades, a head-on conflict with "once".

#### D-159 · The trade prices are drawn at random: 2–5 / 10–32 / 40–64

- **ID**: D-159
- **Conclusion**: the price ranges of the three tiers are **Common 2–5 / Rare 10–32 / Epic 40–64** (unit: emeralds), and **a random value inside the range is drawn each time an offer is generated**; the 6 values go into the `trade` group, **6 config keys** in all (a min / max per tier).
- **Basis**: `plan0.2.0.txt:14` (the three ranges in the original text); the `trade` group of §3.4 of `BRIEF` (a min/max per tier, 6 keys in all, defaults 2/5, 10/32, 40/64); ruling #6 (3 offers).
- **Landing site**: the new `trade` group of `config/FarmstormConfig.java` (6 keys); the offer generation of the 3 offers.
- **Notes**: **the range constrains the price, not the quantity** — the plan writes "each trade consumes N emeralds", and what the player pays is emeralds; how many crops and seeds a deal gives is not given by the plan and is fixed by 35 within the framework of D-157 (subject to the stack tier of the items). The names and value ranges of the 6 keys are named item by item by 36 (§3.4 of `BRIEF` gives the defaults only; the ranges belong to the technical surface), the arithmetic is in §3.4. The random value is **drawn once when the offer is generated** and is not re-rolled on every deal: otherwise the same offer would jitter "one price per click" and the player could not decide on it. Rejected: "fix the price at the middle of the range" — the plan's original text is a range, and pinning it to a single value would delete the "random" layer.

### 1.4 The cultivator (D-160 ~ D-164)

#### D-160 · The new block `cultivator`: anvil hardness + three faces from the texture pack + no menu

- **ID**: D-160
- **Conclusion**: add a **`farmstorm:cultivator` (Crop Cultivator)** block + block entity + `BlockItem`: hardness / blast resistance following the anvil, **`strength(5.0F, 1200.0F)`**; the textures are the **3 PNGs + 2 mcmetas of the texture pack copied in as they are, not renamed**, and the model is assembled as **`cube_bottom_top`** (top / side / bottom); **register no menu** (it has no UI).
- **Basis**: `plan0.2.0.txt:19-22` (the block name, the hardness reference to the anvil, the texture path); §1.8 of `BRIEF` (the texture pack's 40 files = 37 PNG + 2 mcmeta + 1 README; side / top 32×512 each / 16 frames; bottom 32×32; the two mcmetas byte-for-byte identical); rows 18 of §3.3 and §3.5 of `BRIEF`; `Blocks.java:3053` (the anvil's `strength(5.0F, 1200.0F)`, decompiled from the jar cached in `.tools`; the loose-file copy `.scratch/p22c/src/Blocks.java:3053`); the model precedent `blockstates/outpost_core.json:1-7` + `models/block/outpost_core.json:1-8` (`cube_bottom_top`, `ModBlockStateProvider.java:187-194`).
- **Landing site**: `registry/FarmstormBlocks.java` (blocks +1), `registry/FarmstormItems.java` (items +1), `registry/FarmstormBlockEntities.java` (block_entities +1); `src/main/resources/assets/farmstorm/textures/block/` (5 files); `datagen/ModBlockStateProvider`, `ModItemModelProvider`, `ModLootTableProvider`.
- **Notes**: the README of the texture pack explicitly requires the model to reference the three names `farmstorm:block/cultivator_a_top` / `_side` / `_bottom`, so **renaming is forbidden** and the `_a_` must not be dropped either. The block properties beyond the two values (sound / map color / tool requirement / push reaction) follow the anvil's registration chain (`Blocks.java:3051-3055`: `MapColor.METAL` / `requiresCorrectToolForDrops` / `SoundType.ANVIL` / `PushReaction.BLOCK`) and are pinned item by item by 36 in the block property table. **No menu is this document's ruling**: row 13 of §3.3 of `BRIEF` writes "block + BE + item + menu", which conflicts with `menu_types +1` ⇒ 97 of §3.1 — if the cultivator registered a menu too, it would become 98; this document follows the +4 convention of §3.1, and the `menu_types +1` goes to the core storage only (§9 ⑥). When the cultivator is destroyed it **drops itself and the crop inside** (following the four-condition convention of `ConverterBlock.java:88-96`), otherwise the crop of "only one at a time" would be swallowed for good. Rejected: "one side texture on all six faces" — the texture pack gives three different images, and the scanning light of the top and the still face of the bottom must be visually separate.

#### D-161 · Page-flip animation: played in the working state only, the static first frame when idle / paused

- **ID**: D-161
- **Conclusion**: the cultivator plays the 16-frame / `frametime 2` looping animation **only in the working state**; when idle and when paused it shows the **static first frame**. The implementation is **two models**: `working=true` references the frame strip with the mcmeta, `working=false` references the static image taken from frame 0 of the strip.
- **Basis**: ruling #12; `plan0.2.0.txt:21` ("when it is in the working state, play the preset page-flip animation"); §1.8 of `BRIEF` (16 frames × 2 ticks/frame = 1.6 seconds per loop; the two mcmetas byte-for-byte identical; 0 mod `*.png.mcmeta` in the repository); RT-47 (an animated texture for the first time in the project).
- **Landing site**: `src/main/resources/assets/farmstorm/textures/block/` (3 frame strips + 2 mcmetas + **2 static first-frame images**: 1 each for side and top; bottom is 32×32 static already and needs no copy); `datagen/ModBlockStateProvider` (two variant sets for the `working` boolean property).
- **Notes**: **why a static copy is necessary**: the mcmeta animation is **texture-level** — any model referencing that image animates, and the code layer has no "pause" switch. So "idle static" can only be achieved by "the idle model referencing a static image", and the resource increment is therefore **3 PNG + 2 mcmeta + 2 static PNG**, whereas §3.5 of `BRIEF` records only the first two (**[author's supplement]**, §9 ⑤). The first-frame images are ready-made: the texture pack already carries `editable_frames/side_00.png` and `top_00.png` (1 frame each, 32×32) and they can be copied in directly (whether they are pixel-identical to frame 0 of the strip is checked by 36). **The definition of the working state**: there is a crop in the slot and it is not paused because the core is full; an empty slot, or a pause for fullness, both count as idle (the meaning of the animation is "producing"). `working` must go through a blockstate property and must not rest on BE sync alone, otherwise the client does not know to switch models. Rejected: "animate when idle as well" (a conflict with ruling #12), and rejected: "tile the six faces with the bottom image when idle" (the three-face textures would be flattened out).

#### D-162 · Cultivator admission: Common + Rare may go in, Epic may not

- **ID**: D-162
- **Conclusion**: only crops of the **Common** and **Rare** tiers and their seeds can go into the cultivator; **the 4 Epic crops and the 2 Epic seed items are refused outright** (right-click does nothing, the held item is not consumed, no error popup).
- **Basis**: ruling #16; `plan0.2.0.txt:21` ("right-click with a Common or Rare crop of this mod, or its seeds, in hand"); the last row of §3.2 of `BRIEF` (a seed follows its parent crop's tier); §3.2 of `BRIEF` (11 + 7 + 4).
- **Landing site**: `block/CultivatorBlock.java` (the right-click admission judgement, reading `CropProfile.tier()`); `blockentity/CultivatorBlockEntity.java` (the single slot).
- **Notes**: the admission is **judged by tier**, not by an id whitelist — change the tier of one of the 22 profiles and the admission surface follows automatically (which is exactly why D-149 must line up item by item, RT-49). The crops that may go in are **18 = 11 Common + 7 Rare**; the 4 Epic ones may not, and only 2 of them have a standalone seed item (`awakened_torchflower_seeds`, `awakened_pitcher_plant_pod`), while the other 2 Epic crops (`lucky_berry`, `wild_apple`) have no seed item. **Only one may be put in at a time**: while the slot is occupied a right-click does nothing, no stacking, no replacing. Rejected: "let it through by `CropType`" — the edible `wild_apple` is Epic and would be let through, a head-on conflict with ruling #16.

#### D-163 · Cultivator output: 600 ticks, 2 crops + 2 seeds

- **ID**: D-163
- **Conclusion**: the cycle is **600 ticks (30 seconds)**, through the config `cultivator.cycleTicks`; one output is **2 crops + 2 seeds** (when that profile has a **standalone seed item**), and a profile without one produces **2 crops only**; the quantity goes through `cultivator.yield`, default 2. The output is decided by the **crop profile** and is **independent** of whether a crop or its seeds was put in (putting in `chive_seeds` and putting in `friend_chives` give the same result, **[author's supplement]**).
- **Basis**: ruling #15; `plan0.2.0.txt:21` ("after thirty seconds it produces 2 of the corresponding crop and 2 seeds"); the note under the table of §2 of `BRIEF` (cycle default 600 ticks, through config, annotated "supplemented by that card, may be overruled"); the `cultivator` group of §3.4 of `BRIEF` (`cycleTicks` 600, `yield` 2); §1.2 of `BRIEF` (7 standalone seeds / 8 seeds = the crop itself / 7 without seed).
- **Landing site**: `blockentity/CultivatorBlockEntity.java` (the tick and the output); the `cultivator` group of `config/FarmstormConfig.java` (2 keys); the delivery target of the output is in D-166.
- **Notes**: **the criterion of "has seeds"** is the only thing this entry has to reason out. The 22 profiles fall into three classes: 7 with a standalone seed item, 8 whose `seedItem` points at themselves, 7 with no seed. This document takes "has a **standalone seed item**": the 7 give 2 + 2, and the other **15 give 2 crops only** (for the 8 whose seed = themselves the "seed" is the crop item itself, and giving 2 more would create 2 items out of nothing; the 7 without a seed have no registered item to give). **Inside the admission surface** (the 18 of D-162) this counts: **5 give 2 + 2** (`puffed_wheat`, `melon_grenade`, `pumpkin_turret`, `friend_chives`, `power_beet`), and **13 give 2 crops only**. 600 ticks = `30 × 20` (30 seconds × 20 ticks per second, **[author's supplement]**: arithmetic, the two numbers coming from the plan and the tick rate of MC respectively). The output **goes into the core's storage automatically**, it does not drop and does not enter the player's inventory (the plan's own words). Rejected: "judge by `seedItem` being non-null" — that would count the 8 whose seed = themselves as having seeds and double the output on the spot (§9 ⑧).

#### D-164 · Core full at output time → pause production

- **ID**: D-164
- **Conclusion**: when the 54 slots of the bound core **cannot hold this output**, the cultivator **pauses production** — the progress stops at the moment of completion, the slot is not cleared, nothing drops, nothing is lost; once the core frees up space it continues from where it paused.
- **Basis**: ruling #13; row 11 of §3.3 of `BRIEF` (the core's 54 slots); `plan0.2.0.txt:21` ("stores them automatically in the storage of the Outpost Core it is bound to").
- **Landing site**: `blockentity/CultivatorBlockEntity.java` (the completion judgement and the delivery); `blockentity/OutpostCoreBlockEntity.java` (the remaining-capacity query).
- **Notes**: the criterion of "full" is **whether the whole output of this one run can be put in as a batch** (2 + 2 or 2), not "1 slot left, send one" — judged per batch, the output is never split into several batches and the progress semantics do not get complicated. While paused **no animation is played** (the working-state definition of D-161). **Losing no item is hard**: no dropping as an `ItemEntity`, no putting into the player's inventory, no discarding — all three alternatives would turn "the core is full" into a net loss for the player. Rejected: "a queued buffer (queue up when full, spit out when empty)" — the plan's semantics is "pause", not "buffer", and a buffer would add another layer of NBT and sync. Rejected: "drop the output on the ground when the core is full" — the opposite of the plan's "store automatically in the core".

### 1.5 The core and migration (D-165 ~ D-168)

#### D-165 · Cultivator binding: on placement, bind to the nearest ally core, capacity 16

- **ID**: D-165
- **Conclusion**: **on placement** a cultivator binds automatically to the **nearest** core among those that "**cover it + are ally + still have a free slot**"; the distance convention is `BlockPos#distSqr` (three-dimensional Euclidean squared), ties by `placedGameTime` ascending and then by core coordinate lexicographic order; one core binds **16** at most (`outpost_core.cultivatorCapacity`, default 16).
- **Basis**: rulings #8 and #9; `plan0.2.0.txt:26-27`; the note under the table of §2 of `BRIEF` (the tie-break key follows 0.1.1 and **needs no** dimension level — a cultivator and its core are necessarily in the same dimension); §3.4 of `BRIEF` (`cultivatorCapacity` default 16, `storageSlots` default 54); the distance and tie-break precedent of 0.1.1, `FarmlandGuard.java:164`, `:179-189`; the range criterion `OutpostCoreBlockEntity.inRange` (`:146-157`, `RANGE_CHUNKS = 1` at `:53`); the cap of 2 cores per player unchanged (`FarmstormConfig.java:915`).
- **Landing site**: `blockentity/CultivatorBlockEntity.java` (choosing a core on placement + persisting the binding); `blockentity/OutpostCoreBlockEntity.java` (the 16-entry binding list + the free-slot judgement); `config/FarmstormConfig.java:227` (the field), `:909-912` (the definition, where `farmlandBonus` gives way to `cultivatorCapacity`).
- **Notes**: **the only truth of the binding is on the core side** (the 16-entry list is both the capacity carrier and the lookup surface); the copy in the cultivator's BE, "whom I am bound to", is a **reference cache** validated against the core-side list before every use, and an inconsistency between the two sides is handled as **unbound** (otherwise there would be a double source, "the core says unbound, the cultivator says bound", §9 ⑫). **When there is no core to bind to**: the block is placed all the same but sits in the unbound state and is not usable (the plan's "usable if and only if … bound"); the binding is written **at two moments only** — on placement, and when the bound core is dismantled (D-168). Nothing drifts to the nearest core or rebinds periodically at run time, so there is no second truth of the binding. **The consequence of not having bound on placement**: because the binding is written at those two moments only, a cultivator placed when no ally core was available **stays unbound for good** (not usable) **unless it is dismantled and placed again** — this is deliberate; do not quietly rebind it at run time (rebinding would make "was there a core when I placed it" meaningless). `cultivatorCapacity` is **not** the number of storage slots (54 is, ruling #8; §7 item 3). Rejected: "choose the bound core by hand after placement" — the plan has no selection UI, and the cultivator has no GUI (D-160).

#### D-166 · The core becomes a container: 54 slots (a double chest) + sync + legacy-save compatibility

- **ID**: D-166
- **Conclusion**: turn `OutpostCoreBlockEntity` from "stores the owner only" into a **54-slot container (a double chest) that `implements Container`** and add `getUpdateTag` / `getUpdatePacket`; add `useWithoutItem` to `OutpostCoreBlock` to open this storage, **the access condition being two gates: ally + inside the player's reach**; add an `outpost_core` menu (`menu_types` +1); a legacy save (with only the three owner fields) **reads compatibly as an empty container**.
- **Basis**: rulings #8 and #17; rows 11 and 12 of §3.3 of `BRIEF`; `plan0.2.0.txt:28` ("right-clicking the Outpost Core opens its built-in storage (equivalent to a large chest)"); §1.5 of `BRIEF` (the current class **only** `extends BlockEntity`, has no `getUpdateTag` / `getUpdatePacket`, and `setChanged` appears only at `:114`); the container precedent of §1.6 of `BRIEF` (`ConverterBlockEntity implements Container`, per-slot `ItemStack#save`, the shape of `getUpdateTag`, the drops of `ConverterBlock.java:88-96`); §3.6 of `BRIEF` (a legacy save has only the three owner fields).
- **Landing site**: `blockentity/OutpostCoreBlockEntity.java:48` (the class declaration), `:411-424` (NBT); the new `menu/OutpostCoreMenu` + `client/OutpostCoreScreen`; `registry/FarmstormMenus.java:16-17` (+1); `client/FarmstormClient.java:52-54` (the screen registration); `block/OutpostCoreBlock.java` (`useWithoutItem`); `config/FarmstormConfig.java` (`outpost_core.storageSlots` default 54).
- **Notes**: **54 is the number of slots, 16 is the cultivator allowance** — the two are not synonymous (ruling #8, RT-44, §7 item 3). Legacy-save compatibility can only go in the direction "a missing key reads as empty" and must not be inferred the other way (a legacy save has no 54 slots, so read 54 empty ones). **Who can open it**: the two gates **ally + inside the player's reach** (ruling #17) — a non-ally right-click does not open the interface, and being outside the reach does not open it either. **No new distance constant is set for the reach**: the `stillValid` semantics of the vanilla container menu is used directly (that is, the behaviour the `stillValid(access, player, block)` brings with it when `ConverterMenu` is copied), and what the author chose is "the standard reach", not a custom radius. The reason is that the core's output goes into this container automatically and the content drops when it is dismantled, so by its semantics it is "the ally outpost's storage" rather than a public chest. The config key `outpost_core.storageSlots` carries one technical risk: the slot count of a menu is fixed at construction, and changing it in config would make "a menu already open" and "a newly opened menu" disagree — 36 fixes "read it once when the menu is created", but the 54 **must not** become another value (the arithmetic of §3.4 goes by 54). The NBT size and the sync have to be recomputed too (RT-44): 54 `ItemStack`s going into `getUpdateTag` in full would grow noticeably, and 36 must provide a scheme that "sends only on change". Rejected: "reuse the vanilla `ChestBlockEntity`" — the core's ownership, placement cap, chunk tickets and cultivator bindings all live on its own BE, and changing the superclass would mean rewriting a whole set of existing logic.

#### D-167 · Items drop when the core is destroyed

- **ID**: D-167
- **Conclusion**: when the core is **genuinely destroyed**, every item in the 54 slots **drops**; being pushed by a piston, being replaced by the same block, and a chunk unload **drop nothing**.
- **Basis**: `plan0.2.0.txt:30` ("when the Outpost Core is destroyed, all the items it stores drop"); row 12 of §3.3 of `BRIEF`; the drop precedent `ConverterBlock.java:88-96` (`onRemove` → `!state.is(newState.getBlock()) && !movedByPiston` → `Containers.dropContents`).
- **Landing site**: `block/OutpostCoreBlock.java:101-107` (add the drop branch in `onRemove`).
- **Notes**: the drop and "the settlement of dismantling a core" are **two things with two hooks**: the drop goes through `onRemove` (the block is gone, so the items drop), while the index removal + the cultivator migration go through `setRemoved` / `settleOnRemoval` (D-168). The criterion follows the four conditions of the precedent, otherwise there would be a duplication slip of "a fresh copy of the items spawned in place when pushed by a piston". The drop **excludes** the cultivators: a cultivator is a separate block, and when the core is dismantled it does not disappear, it merely migrates (D-168). Rejected: "stuff the items straight into the player's inventory on destruction" — the core may be blown up by TNT or dismantled by someone else, and at that moment there is no "operating player".

#### D-168 · Cultivator migration: when the core is dismantled, look for a successor core; none found means not usable

- **ID**: D-168
- **Conclusion**: when a core is dismantled, rebind its **16 cultivators** one by one onto other cores that "**cover that cultivator + are ally + still have a free slot**" (nearest first, the tie-break keys as in D-165); **when there is not one**, that cultivator **stays where it is, keeps its contents, is marked unbound and is not usable** — it does not disappear, does not turn to dirt, spawns no drop.
- **Basis**: `plan0.2.0.txt:27` ("the Crop Cultivators bound to it look for another ally Outpost Core they can bind to, otherwise the Crop Cultivator cannot be used"); RT-43; row 10 of §3.3 of `BRIEF` (the three exits of `settleOnRemoval` rewritten as a whole); the isomorphic precedent of 0.1.1, the migration predicate of D-136 (coverage + alliance + a free slot, nearest first).
- **Landing site**: `blockentity/OutpostCoreBlockEntity.java:341-394` (`settleOnRemoval` rewritten as the cultivator migration); `blockentity/CultivatorBlockEntity.java` (receiving the rebind); `outpost/OutpostCoreIndex.java` (the candidate-core query, the same convention as 0.1.1).
- **Notes**: **the only difference from D-136 of 0.1.1**: 0.1.1 **touches the world** when it cannot migrate (`setBlock(DIRT)`), while this release **does not touch the world** (a cultivator is a separate block, leaving it there is enough) — so the new three exits are "rebind / keep and mark unbound / skip defensively", and **there is no "turn to dirt" branch** (RT-43). **No chunk is loaded for the migration**: a bound cultivator is necessarily inside the core's 3×3 chunks (the `RANGE_CHUNKS = 1` of `inRange` and `CoreChunkTickets.DISTANCE = 1` are the same range, §1.5 of `BRIEF`, `CoreChunkTickets.java:44`), and the tickets are released in `release()`, `release()` being the last line of the whole block ⇒ during the migration those chunks are in memory; if `getChunkNow` unexpectedly fails (an exceptional path), **keep the original NBT value, do not guess, do not delete the binding** (a defensive branch pointing the same way as A-60). Rejected: "destroy the cultivator and drop its items when it cannot migrate" — the plan says in so many words "cannot be used", not "is dismantled" — destroying it would make a destruction decision on the player's behalf.

### 1.6 The link / the converter / the registry surface / the red lines (D-169 ~ D-172)

#### D-169 · The link: a client-side line frame + a new network packet + sent inside the range only

- **ID**: D-169
- **Conclusion**: add **one purely visual link** for "core ↔ cultivator": the server sends the in-range binding relations with **the new 2nd custom network packet**, and the client **draws a line frame in world space**; the link is not an entity, not a particle, does not collide, does not emit light, and no server-side logic may depend on it.
- **Basis**: ruling #10; `plan0.2.0.txt:29` ("a visible link (no entity, no effect on any block or entity, indicating the position only)"); row 20 of §3.3 of `BRIEF`; the note under the table of §2 of `BRIEF` (sent only while **the player is inside the 3×3 chunks of an ally core**); §1.9 of `BRIEF` (world-space line drawing has 0 places across the repository; `RenderLevelStageEvent` appears only as the matrix capture of `WildHunterOutline:66-74`, drawing no geometry); RT-45 (line-frame rendering for the first time in the project).
- **Landing site**: the new `network/CultivatorLinkPayload` (the class name and the packet id fixed by 36) + the `FarmstormNetwork` registration + a client handler; a new line-frame renderer class under `client/` (hung on `RenderLevelStageEvent`); the sending points hooked to the binding changes of `OutpostCoreBlockEntity` / `CultivatorBlockEntity`.
- **Notes**: **why it must be a new packet and must not extend `core_list`**: `core_list` is a **full snapshot** of "the ally core list" (`MAX_CORES = 64`, three-level ordering, fingerprint de-bouncing, `CoreListPayload.java:23,25,28`), whereas the link is another kind of data — **per player, filtered by range, changing with the bindings**; putting it into the same packet would mix the two semantics, "full snapshot" and "range increment", into one `StreamCodec`, and both the de-bouncing and the fingerprint would have to be rewritten. The packet id, the `StreamCodec` structure and the list of send / withdraw timings (entering the range / leaving the range / a binding change / the core dismantled / the cultivator dismantled) are listed item by item by 36 (RT-45); the concrete choice of `RenderType` and the line width likewise belong to the technical surface and are fixed by 36. The client is **read-only**: it draws what it receives, computes no binding itself and caches nothing across dimensions; any residue received before leaving the range must be cleared. Rejected: "draw the line with particles" (§7 item 6) — a line a few dozen blocks long would need a few dozen particle packets, would miss the perspective and line width of `RenderType`, and could not move smoothly with the camera.

#### D-170 · The converter's function is disabled (right-click does nothing, the recipes kept)

- **ID**: D-170
- **Conclusion**: change `ConverterBlock.useWithoutItem` to return **`PASS`** (right-click does nothing, no interface opens); the block, the block entity, the menu class, the 26 `farmstorm:converting` recipes and the crafting recipes **all stay**, only the function is disabled.
- **Basis**: ruling #11; `plan0.2.0.txt:33` ("remove its function for now, leaving it a decorative block only"); row 17 of §3.3 of `BRIEF`; `ConverterBlock.java:78-86` (the current implementation: `SUCCESS` on the client, `openMenu` on the server); §1.7 of `BRIEF` (29 recipe jsons, 26 of them `converting`); §1.6 of `BRIEF` (the menu chain `ConverterMenu` → `ConverterScreen`, the only entry being the GUI button `ConverterMenu:141-150`).
- **Landing site**: `block/ConverterBlock.java:78-86`; `registry/FarmstormItems.java:121-129` (kept in the creative tab, still an obtainable decorative block); `ConverterMenu` / `ConverterScreen` (**not deleted**).
- **Notes**: "disabled" is on the **interaction surface**, not the data surface: the recipe jsons are still there, there is simply no entry calling them. The menu class is kept so that restoring it later needs no rewrite (the plan says "remove for now"). `getMenuProvider` may stay (nobody calls it), but `useWithoutItem` must return `PASS` — returning `SUCCESS` / `CONSUME` swallows the right-click event (for instance it blocks the use of the item in hand). Rejected: "take the block out of the creative tab too" — the plan says "a decorative block only", and a decorative block must be obtainable. Rejected: "delete the recipes with it" — the 26 recipes are the only carrier of this mod's "vanilla crop ⇄ modified crop" mapping (D-54 of 0.1.0), and deleting them would mean rebuilding it on restoration.

#### D-171 · Registry surface +4 ⇒ 97

- **ID**: D-171
- **Conclusion**: the registry surface of this release **grows by a net 4**: `blocks +1`, `block_entities +1`, `items +1`, `menu_types +1`; the other 8 classes **+0**. Total **93 + 4 = 97**.
- **Basis**: §3.1 of `BRIEF` (the class-by-class table of +4 ⇒ 97, together with the ⚠️ "36 must still name it class by class from the source code itself"); the 93 baseline of §0 of `BRIEF` (the 12-class breakdown); D-150 (the tier goes through a static field and is not registered), D-157 (trading goes through a NeoForge event and is not registered), D-169 (the network packet goes through `RegisterPayloadHandlersEvent` and does not enter `BuiltInRegistries`).
- **Landing site**: `registry/FarmstormBlocks.java`, `registry/FarmstormBlockEntities.java`, `registry/FarmstormItems.java`, `registry/FarmstormMenus.java`; `Farmstorm.java:147-150` (the registration self-check assertion, 93 → 97, the 12 classes item by item +).
- **Notes**: the arithmetic and the class-by-class breakdown — items `32 + 1 = 33`, blocks `26 + 1 = 27`, block_entities `7 + 1 = 8`, menu_types `1 + 1 = 2`, the other 8 classes as before (entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1) ⇒ **33 + 27 + 8 + 2 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 = 97** (**[author's supplement]**: arithmetic, each of the 12 addends coming from the baseline breakdown and the +4 table). `data_components` is still 1 — the tier is not a data component (D-150). ⚠️ This 97 must be checked again by 36 **naming it class by class from the source code itself**, and must not be copied from this line (the ⚠️ of §3.1 of `BRIEF`, item 1 of the hard prohibitions of §6). **The converter's Chinese name stays as it is, "农产品转换器"**: this release changes neither that language key nor the wording in documents 34 / 35 (ruling #19).

#### D-172 · Red lines: "no Mixin" kept, this release adds 1 network packet

- **ID**: D-172
- **Conclusion**: the project red line **is still the one "no Mixin"** (0 in this release, still forbidden); "custom network packets" has no numerical cap, this release **adds 1** (the link, D-169) for **2** in total, and it **must be registered in §5 of this document**.
- **Basis**: §0.2 of `BRIEF` (the packet-count restriction abolished outright from 0.1.1 on, this release expecting 1 new one that must be registered in 34); §0 of `BRIEF` (currently 1, `farmstorm:core_list`: `CoreListPayload.java:23,25,28`, `FarmstormNetwork.java:22-31`; Mixin 0); **D-146** of 28 (the red-line convention: only "no Mixin" left).
- **Landing site**: `network/FarmstormNetwork.java` (the registration of the 2nd packet); §5 of this document (the register table); the new HC section of `38-PROMPT-MAIN-0.2.0.md` (written by 38).
- **Notes**: "no cap" **does not equal** "add them freely": every new packet still needs a decision of its own + one registered row in §5 (the existing reading of D-146). **The 2 packets of this release are S2C read-only**; the client sends no reply and makes no judgement, and server authority is unchanged. The Mixin number is not recomputed here; the 0 of §0 of `BRIEF` is cited. Rejected: "the link reuses the channel of `core_list`" (D-169 Notes), and rejected: "turn the red line back to 0 packets" (that is the old convention of 0.1.0, abolished outright by D-146 of 28).

---

## 2. Author ruling index (19 rulings → decision numbers)

The table below transcribes item by item the 19 author rulings of §2 of `BRIEF` (three rounds of interviews, **the two columns "item" and "fixed as" verbatim identical to the fact card**, the bold being the emphasis the fact card already had), and the last column is the decision number of this document.

| Ruling # | Item | Fixed as | Decision |
|---|---|---|---|
| 1 | Version and baseline | **0.2.0**, baseline **the published 0.1.1 state** (the "0.1.1 / with 0.1.0 as the main version" in the body of the plan is a copy of an old value) | D-148 |
| 2 | Tiering | Common 11 / Rare 7 / Epic 4 (including putting `ubi_server`, `toxic_ubi_server`, `point_defense_fungus` **all into Rare**) | D-149 |
| 3 | Retire the farmland system | **comment out the "call sites" only**: comment out the 3 listeners and the related config reading; the class bodies of `FarmlandLedger` / `FarmlandGuard` kept untouched | D-153, D-154 |
| 4 | Crop blocks | **keep the registry**, but cut the "plantable" path outright | D-152, D-155 |
| 5 | The carrier of the tier | written into the **static field** of `CropProfile` (no new `data_component_type`) | D-150 |
| 6 | Farmer trading | **3 trade offers** by tier, unlocked in order by villager level (example: novice → Common, journeyman → Rare, master → Epic) | D-157 |
| 7 | "Each trade can be made once only" | after one deal it goes **out of stock** and **the restock** has to be waited for (equivalent to `maxUses=1` + the vanilla restock mechanism) | D-158 |
| 8 | Core storage | **54 slots** (a double chest); "capacity 16" is the **number of bound cultivators**, not the number of slots | D-166, D-165 |
| 9 | Cultivator binding | **on placement, bind automatically to the nearest ally core with a free slot** | D-165 |
| 10 | Link rendering | **client-side line-frame rendering, through a network packet** | D-169, D-172 |
| 11 | The converter | keep the block and the recipes, **right-click does nothing** (no GUI) | D-170 |
| 12 | Page-flip animation | **played only while working**, static when idle | D-161 |
| 13 | Core full at output time | **pause production**, store once the core has space | D-164 |
| 14 | The visual presentation of the tier | **item name color + one tooltip line** (tier colors: Common white / Rare blue / Epic gold) | D-151 |
| 15 | Cultivator output rule | **with a standalone seed, 2 crops + 2 seeds; without one, 2 crops only** | D-163 |
| 16 | Cultivator admission | **only Common and Rare may go in** (the 4 Epic crops may not) | D-162 |
| 17 | The access condition of the core storage | the two gates **ally + inside the player's reach**. **The reach goes through the vanilla container distance check** (the default semantics of `AbstractContainerMenu#stillValid`), **no custom distance gate is set up separately** | D-166 |
| 18 | The granularity of farmer trading | **keep 3 offers, each drawing one at random by tier** (neither the 22 changed nor a rotation introduced). Known cost: collecting all 4 Epic takes about 8.3 master farmers in expectation (a harmonic number) and Rare about 18.2 journeymen — **this is an accepted cost** | D-157 |
| 19 | The converter's Chinese name | **keep the current "农产品转换器"**, change neither the language key nor the wording in documents 34 / 35 | D-171 |

Three mapping notes (to be watched when reading the table):

1. The 19 rulings map onto **20 decisions** (23 mapping relations in all): rulings #3, #4, #8 and #10 are each split into two, because their landing sites and acceptance methods are completely different (#3 = the commenting action / the save file; #4 = cutting the path / keeping the registry; #8 = the container slots / the binding allowance; #10 = the data surface / the red-line register); rulings #17 and #18 land on the existing D-166 / D-157 (a second ruling face of the same decision), and #19 lands on D-171.
2. **5 decisions have no corresponding ruling**: D-156 (the Lucky Berry pool), D-159 (the price ranges), D-160 (the cultivator block itself), D-167 (the core's drops), D-168 (the cultivator migration). Their basis is only the plan's original text and §1 / §3 of `BRIEF`, but they are **not therefore weaker than the others** — most of the execution surface of requirements 2 and 3 of the plan lands on them. Should the author want to co-opt them into rulings, 37 starts at A-62, and this document changes only the "Notes" field of the corresponding decisions.
3. The bold and the wording of the "fixed as" column are copied verbatim from the fact card and must not be reworded when cited. **The "3 listeners" of ruling #3 is the fact card's wording**; in reality it is 2 listeners + 1 start-up backfill call (this document follows the latter, see §9 ⑦).

---

## 3. Register of changes to 0.1.1

The three tables register **additions / changes / retirements** respectively. Every row gives the original location in 0.1.1 — it is the evidence of "where this sentence is in 0.1.1", not a suggestion to change it (28 and the earlier documents are frozen). The tables contain no tasks and no scheduling.

### 3.1 Additions (things 0.1.1 does not have)

| # | Addition | Original location in 0.1.1 (proof it did not exist then) | Decision |
|---|---|---|---|
| 1 | **The `CropTier` enum** (Common / Rare / Epic) and the **9th field** `tier` of `CropProfile` | `CropProfile.java:32-40` is an 8-field record; `rarity` / `quality` / `品质` / `稀有` / `史诗` have 0 hits across the whole repository (`BRIEF` §1.1) | D-149, D-150 |
| 2 | **Tier visuals**: the item name in three tier colors + one tooltip line (22 crops + 7 seeds) | the existing tooltip has only the `CropCategory` line (`ModifiedCropItem.java:57-60`, `SeedItem.java:28-31`), no color, no tier line | D-151 |
| 3 | **Farmer trading**: 3 trade offers + the 6 keys of the `trade` group | `Villager` / `villager` / `trade` / `TradeOffers` / `VillagerTradesEvent` have 0 hits across the whole repository (`BRIEF` §1.4); `data/farmstorm/` has no villager-related directory | D-157, D-159 |
| 4 | **The `cultivator` block + `BlockItem`** | the +4 table of §3.1 of `BRIEF`; it is not among the 26 blocks of `FarmstormBlocks.java:26-90` | D-160 |
| 5 | **The `cultivator` block entity** | `FarmstormBlockEntities.java` has 7 at present, `BRIEF` §0; no `cultivator` | D-160, D-163 |
| 6 | **The cultivator binding list** (16 entries on the core side) | the current fields of `OutpostCoreBlockEntity` are only `ownerUuid` / `ownerName` / `placedGameTime` (`BRIEF` §1.5, `:82-86`) | D-165 |
| 7 | **The core container's 54 slots + the `outpost_core` menu** | `OutpostCoreBlockEntity` only `extends BlockEntity` and is not a container (`BRIEF` §1.5, `:48`); `menu_types` has 1 at present (`converter`, `FarmstormMenus.java:16-17`) | D-166 |
| 8 | **The 2nd custom network packet** (the link's data surface) + client-side line-frame rendering | currently only 1, `farmstorm:core_list` (`CoreListPayload.java:23,25,28`, `FarmstormNetwork.java:22-31`); world-space line drawing has 0 places across the whole repository (`BRIEF` §1.9) | D-169, D-172 |
| 9 | **The 5 texture files** (3 PNG + 2 mcmeta) + **2 static first-frame images** | 0 mod `*.png.mcmeta` and 0 vertical frame strips in the repository (`BRIEF` §1.8); no `cultivator_a_*` under `textures/block` | D-160, D-161 |
| 10 | **New config groups** `cultivator` (2 keys) + `trade` (6 keys) + `cultivatorCapacity` / `storageSlots` of `outpost_core` (2 keys) | `FarmstormConfig.java` currently has 58 groups / 166 keys (`BRIEF` §0); the `outpost_core` group has only 3 keys (`FarmstormConfig.java:909-918`) | D-159, D-163, D-165, D-166 |
| 11 | **The three exits of the cultivator migration** (when the core is dismantled) | the three exits of `settleOnRemoval` are the farmland exits at present (`OutpostCoreBlockEntity.java:341-394`, `:383-392`) | D-168 |

### 3.2 Changes (present in 0.1.1, semantics or values changed)

| # | Item | 0.1.1's value / semantics | v0.2.0 | Original location in 0.1.1 | Decision |
|---|---|---|---|---|---|
| 1 | Version number | 0.1.1 | **0.2.0** | `gradle.properties:11` | D-148 |
| 2 | The field count of `CropProfile` | 8 fields | **9 fields** (+ `CropTier tier`) | `CropProfile.java:32-40` | D-150 |
| 3 | The sowing criterion `isPlantableSoil` | "allowed only when the block clicked is a `FarmBlock`" | **a constant `false`** | `ModifiedCropItem.java:101-107`, the call site `:72` | D-152 |
| 4 | The sowing path of the 7 standalone seeds | the vanilla sowing inherited from `ItemNameBlockItem` / `BlockItem` | **`useOn` returns `PASS`** | `SeedItem.java:16`, `:28-31` | D-152 |
| 5 | The settlement when a core is dismantled | the three farmland exits: rebind / turn to dirt / remove the bookkeeping only | **the three cultivator exits**: rebind / keep and mark unbound / skip defensively | `OutpostCoreBlockEntity.java:341-394` | D-168 |
| 6 | The shape of `OutpostCoreBlockEntity` | "a BE storing the owner only", not a container, no `getUpdateTag` / `getUpdatePacket` | **a 54-slot `Container` + 16 bindings + the two sync methods** | `OutpostCoreBlockEntity.java:48`, `:411-424`; `BRIEF` §1.5 | D-166, D-165 |
| 7 | The interaction surface of `OutpostCoreBlock` | no `use` / `useWithoutItem` override in its 108 lines | **right-click opens the storage interface + drops on destruction** | `OutpostCoreBlock.java` (no interaction override), `:101-107` (`onRemove`) | D-166, D-167 |
| 8 | The Lucky Berry pool | 19 entries (3 excluded) | **11 entries (Common only)** | `CropProfile.java:139-143`; `LuckyBerryRoller.java:172` | D-156 |
| 9 | The pool assertion of the start-up self-check | `poolSize == 19` | **`== 11`** | `Farmstorm.java:118-121` | D-156 |
| 10 | The converter's interaction | right-click opens the GUI (`SUCCESS` on the client, `openMenu` on the server) | **right-click does nothing (`PASS`)** | `ConverterBlock.java:78-86` | D-170 |
| 11 | The `outpost_core` config group | 3 keys (`farmlandBonus` / `maxPerPlayer` / `boundaryParticleIntervalTicks`) | **4 keys** (delete `farmlandBonus`, add `cultivatorCapacity` / `storageSlots`; `maxPerPlayer` and the particle interval untouched) | `FarmstormConfig.java:909-918` | D-165, D-166 |
| 12 | The resource surface (new resources, not image changes) | no blockstate / model / item model / loot table / animated texture for `cultivator` | **1 of each + the 5 texture files + the 2 static images** (through datagen) | `BRIEF` §3.5; no `blockstates` / `models` under `src/main/resources/assets/farmstorm` | D-160, D-161 |
| 13 | The save surface: the core BE's NBT | only the three owner fields (in the chunk save) | **+ 54 slots of items + 16 bindings** (a legacy save reads a missing key as empty) | `OutpostCoreBlockEntity.java:411-424`; `BRIEF` §3.6 | D-166, D-165 |

### 3.3 Deletions / retirements (the most important section of this release: **the farmland system retired as a whole**)

| # | What is retired / deleted | Original location in 0.1.1 | Decision |
|---|---|---|---|
| 1 | **Every run-time effect of the farmland binding system**: core binding, the quota gate, the hoeing interception, the trample protection, the start-up backfill | `FarmlandLedger.java` (555 lines), `FarmlandGuard.java` (225 lines), `FarmstormGameEvents.java:241-265` / `:277-281` | D-153 |
| 2 | **The `onRightClickBlock` listener**: the registration line + the implementation (the hoeing interception, the action-bar hint) | `FarmstormGameEvents.java:75` (registration), `:241-265` (implementation) | D-153 |
| 3 | **The `onFarmlandTrample` listener**: the registration line + the implementation (the trample protection) | `FarmstormGameEvents.java:76` (registration), `:277-281` (implementation) | D-153 |
| 4 | **The start-up backfill call** `FarmlandLedger.get(server).backfill(server)` | `FarmstormGameEvents.java:171` (inside `onServerTickPost`; that listener itself is kept) | D-153, D-154 |
| 5 | **The definition of the config `outpost_core.farmlandBonus`** (including the dual semantics "per-core capacity + quota base") | `FarmstormConfig.java:909-912` (definition); the **field declaration `:227` is kept**, the reason being in A-62 | D-153 |
| 6 | **The 3 farmland language keys**: `outpost_core.farmlandBonus`, `message.farmstorm.farmland.no_core`, `message.farmstorm.farmland.quota_full` | `datagen/ModLangProvider.java:403`, `:412`, `:413` | D-153 |
| 7 | **The reading and writing of `farmstorm_farmland.dat`** (the file itself is not deleted) | `FarmlandLedger.java:44` (persisted name), `:228-244` (save), `:246-264` (load) | D-154 |
| 8 | **The concept of the farmland quota** (`capacity = farmlandBonus × number of ally cores`) | `FarmlandLedger.java:290-306`; D-139 of 28 | D-153 |
| 9 | **The sowing ability of the crops and the 7 seeds** | `ModifiedCropItem.java:101-107`, `SeedItem.java:16` | D-152 |
| 10 | **The converter's functional surface**: opening the GUI and the "convert" button settlement (the block, the recipes and the menu class kept) | `ConverterBlock.java:78-86`; `ConverterMenu.java:141-150` | D-170 |
| 11 | **The 8 non-Common crops in the Lucky Berry pool** (6 Rare + 2 Epic) | `CropProfile.java:139-143` | D-156 |

Five notes (this table is **the easiest to misread**):

- **Rows 1 ~ 8 are eight landing points of one and the same thing** (the retirement of the farmland system): the two listeners, the one backfill line, the one config key, the 3 language keys, the one save file and the one quota concept must be retired together; retiring half would leave half-dead states such as "reading a key that does not exist", "the implementation commented out but the registration not", or "the listener still there but referencing a commented-out method, so it does not compile".
- **The two classes `FarmlandLedger` / `FarmlandGuard` are not in this table**: they are **kept**, not one line changed (ruling #3, RT-42, §8 item 3). Row 7 retires the **reading and writing** of that `.dat`; **the file itself is not deleted** (D-154).
- **`/farmstorm farmlandlimit` is not in this table either**: it **was already deleted in 0.1.1** (D-140 of 28, §1.2 of `BRIEF`), and this release neither deletes it again nor restores it (§6 row 7).
- **The crop blocks, crop items, seed items and loot tables are not in this table**: they keep their registration (D-155); row 9 retires the "sowing ability", not the registered objects.
- **`CoreChunkTickets.restoreAll` is not in this table**: it serves the core's chunk tickets and has nothing to do with farmland, and `FarmstormGameEvents.java:170` is kept (D-153 Notes).

### 3.4 Count arithmetic

- **Registry surface**: `93 + 4 = 97` (blocks +1 / block_entities +1 / items +1 / menu_types +1, the other 8 classes +0), the class-by-class breakdown in D-171. ⚠️ 36 must still name it class by class from the source code itself (the ⚠️ of §3.1 of `BRIEF`).
- **config**: `166 − 1 + 2 + 2 + 6 = 175` (delete `farmlandBonus`; add `cultivatorCapacity`, `storageSlots`; the new group `cultivator` 2 keys; the new group `trade` 6 keys). Groups `58 + 2 = 60` (the two new groups `cultivator` and `trade`, the `outpost_core` group still there) — **[author's supplement]**: arithmetic, the 58 coming from §0 of `BRIEF`. 36 must name every key and check the skeleton of §3.4 of `BRIEF` again.
- **Language keys**: baseline **323** (§0 of `BRIEF`). This document registers only "**−3** (the 3 farmland keys of row 6)" and the existence of the additions (the three tier names, the tooltip line, the `cultivator` block name and the 10 config names, the core storage interface title, etc.); **the total is pinned down by 36 after naming every key**, and this document gives no total (to avoid inventing a count).
- **Save format**: this release has **three save-related registers** — ① **changed**: the core BE's NBT grows (+54 slots +16 bindings), its location is the **chunk save**, and a legacy save reads a missing key as empty; ② **unchanged**: `farmstorm_outpost_core.dat` (the index `OutpostCoreIndex`) keeps the entry shape `Entry(dimension, pos, gameTime)`; ③ **not read, not written, not deleted**: `farmstorm_farmland.dat`. **These three must be written apart**; doing the compatibility for one only misses the others (§3.6 of `BRIEF` wrote the location of ① as ②, see §9 ⑨).
- **Resources**: `3 PNG + 2 mcmeta + 2 static first-frame images` (the texture pack's 5 files + the side / top first frames taken from `editable_frames/`, 1 each); blockstate / block model / item model / loot table **1 of each**, through datagen (§3.5 of `BRIEF`).

---

## 4. Tier table (the 22 crops named item by item)

The table below is the **only list** of the v0.2.0 tiering. The downstream 35 ~ 39 may cite only this table when citing a tier, and must not add or remove anything themselves.

### 4.1 The three tiers and their membership

| Tier | Count | Crop id (named item by item) |
|---|---|---|
| **Common** | **11** | `puffed_wheat`, `blast_carrot`, `pumpkin_turret`, `melon_grenade`, `melon_slice_bomb`, `red_mushroom_puff`, `brown_mushroom_puff`, `molotov`, `fang_trap_cactus`, `bamboo_firecracker`, `vibration_bean` |
| **Rare** | **7** | `zangief_cane`, `friend_chives`, `power_beet`, `compressed_puffed_wheat`, `ubi_server`, `toxic_ubi_server`, `point_defense_fungus` |
| **Epic** | **4** | `lucky_berry`, `awakened_torchflower`, `awakened_pitcher_plant`, `wild_apple` |
| **Total** | **22** | **11 + 7 + 4 = 22** |

**Counting the arithmetic tier by tier**:

- Common **11**: counted item by item = 11 (11 ids in the row).
- Rare **7** = the plan's 4 (`zangief_cane`, `friend_chives`, `power_beet`, `compressed_puffed_wheat`) + the **3** added by the interview (`ubi_server`, `toxic_ubi_server`, `point_defense_fungus`). These 3 are **not in the plan's original text**; ruling #2 added them, and this is the only tiering of this release where the author raised the bar in the interview.
- Epic **4**: counted item by item = 4.
- Total **22**: the same number as the 22 of `CropProfile.ALL` and the existing assertion of `Farmstorm.java:112-115`.

### 4.2 Seeds follow their parent crop (7 standalone seeds)

| Seed item | Parent crop | Tier |
|---|---|---|
| `puffed_wheat_seeds` | `puffed_wheat` | Common |
| `melon_grenade_seeds` | `melon_grenade` | Common |
| `pumpkin_turret_seeds` | `pumpkin_turret` | Common |
| `chive_seeds` | `friend_chives` | Rare |
| `power_beet_seeds` | `power_beet` | Rare |
| `awakened_torchflower_seeds` | `awakened_torchflower` | Epic |
| `awakened_pitcher_plant_pod` | `awakened_pitcher_plant` | Epic |
| **Total 7** | 7 parent crops | **3 + 2 + 2 = 7** |

The other 15 crops **have no standalone seed item**: 8 have a `seedItem` pointing at themselves (`blast_carrot`, `ubi_server`, `toxic_ubi_server`, `fang_trap_cactus`, `bamboo_firecracker`, `red_mushroom_puff`, `brown_mushroom_puff`, `lucky_berry`), and 7 have an empty `seedItem` altogether (`melon_slice_bomb`, `molotov`, `zangief_cane`, `wild_apple`, `compressed_puffed_wheat`, `point_defense_fungus`, `vibration_bean`). A seed **has no tier source of its own** and always follows its parent crop (the last row of §3.2 of `BRIEF`).

### 4.3 The sets derived from the tiering (cited directly downstream, do not recompute)

| Purpose | Set | Count | Arithmetic |
|---|---|---|---|
| Cultivator **may take** (D-162) | Common + Rare | **18** | 11 + 7 = 18 |
| Cultivator **may not take** (D-162) | Epic | **4** | 22 − 18 = 4 |
| Cultivator output **2 crops + 2 seeds** (D-163) | inside the admission surface, with a standalone seed item | **5** | 3 Common (`puffed_wheat` / `melon_grenade` / `pumpkin_turret`) + 2 Rare (`friend_chives` / `power_beet`) |
| Cultivator output **2 crops only** (D-163) | inside the admission surface, no standalone seed | **13** | 18 − 5 = 13 |
| Lucky Berry pool (D-156) | the full Common set | **11** | 19 − 8 = 11 |
| Farmer trade offers (D-157) | one per tier | **3** | the number of tiers |

Two points to watch when reading the table: ① **the Epic tier can only be obtained through trading** (none of the 4 enters the cultivator, D-162), so the master-level offer is the **only entry** of Epic crops in this release; ② **the Lucky Berry pool = the full Common set**, not "a few picked out of Common" — 11, not one more, not one less (`19 − 8 = 11`; the 8 removed are in the D-156 Notes).

---

## 5. Red lines and the red-line change

### 5.1 Before and after of the two red lines

| Item | v0.0.1 ~ v0.1.0 | v0.1.1 | **v0.2.0 (this document)** | Source |
|---|---|---|---|---|
| Custom network packets | **red line: 0** | **that red line abolished outright** (no cap; this release happening to add 1) | **no cap**; this release **adds 1**, **2** in total, registered one by one (see 5.2) | `BRIEF` §0.2; D-146, D-142 of 28 |
| Mixin | none | **none (the only red line kept)** | **none (the only red line kept, 0)** | `BRIEF` §0; D-146 of 28 |

### 5.2 Registration of the network packets added by this release (only 1 is new here)

| # | Packet | Direction | Purpose | Decision | Status |
|---|---|---|---|---|---|
| 1 | `farmstorm:core_list` (`CoreListPayload`) | S2C read-only | the core list of the overlay | D-142 of 0.1.1; **kept untouched** by this release | existing |
| 2 | **The link data packet** (the packet id / `StreamCodec` fixed by 36 within the framework of D-169) | **S2C read-only** | the range data of the "core ↔ cultivator" link | **D-169** | **added by this release** |

### 5.3 Four readings (get any one wrong and all five downstream documents are wrong)

1. **"No cap" is not "add them freely"**: this release adds 1 only because the link really needs it (the four reasons of D-169). Every new packet from now on still needs a decision + one registered row in this table (the existing reading of D-146 of 28).
2. **Both packets are S2C read-only**: the client sends no reply and makes no judgement, and server authority is unchanged (the existing boundary of `core_list`; the handler of `ClientCoreList.java:24-26` does only `enqueueWork`).
3. **"No Mixin" is still zero**: `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor` are still forbidden, and this release has no exception (§0 of `BRIEF`).
4. **Network packets do not enter the registry surface**: a payload goes through `RegisterPayloadHandlersEvent` and does not enter `BuiltInRegistries`, so it is not among the `+4` of D-171 (the same convention as §3.4 of 28).

---

## 6. Supersession relation to the 0.1.1 decisions

The table below registers only the clauses where "0.1.1 says X and this release says Y"; it also keeps the comparison rows marked "**Kept**", to declare explicitly that "these were not changed". The clauses marked "Kept" in the table continue in force; **the 0.1.1 clauses not listed in this table** continue in force just the same.

| 0.1.1 clause (location) | Status | This release | Why |
|---|---|---|---|
| D-134 the farmland-core binding system (each plot belongs to 1 core only, 16 plots per core) | **retired (the whole entry)** | D-153 | ruling #3: retire every change of this mod that concerns farmland (`plan0.2.0.txt:26`) |
| D-135 the binding rule (nearest first, ties by earlier placement, must cover the 3×3) | **retired (the farmland side)** | D-153; **the convention inherited by D-165** | the cultivator binding uses the same "nearest + tie-break key", but omits the dimension level (a cultivator and its core are necessarily in the same dimension) |
| D-136 the farmland migration when a core is destroyed (migrate / turn to dirt / remove the bookkeeping only) | **retired (the farmland side)** | D-153; **the isomorphic semantics moved to D-168** | 0.1.1 touches the world when it cannot migrate (`setBlock(DIRT)`), while this release only marks the cultivator unbound (RT-43) |
| D-137 the start-up backfill of a legacy save (`FarmlandLedger.backfill`) | **retired** | D-153, D-154 | the backfill is one of the entry points of the farmland system; the call site is commented out and the ledger is no longer `get()` |
| D-138 the "ally" boundary and its validity offline | **Kept** (only the "farmland protection surface" retired with D-153) | continues in force | the overlay (D-143), the cultivator binding (D-165) and the link (D-169) keep using it; this document does not change the definition |
| D-139 quota = `16 × number of ally cores`, `farmland.limitPerPlayer` abandoned | **retired (the whole quota concept disappears)** | D-153 | no farmland, no quota; `base` was deleted in 0.1.1 long ago, and this release has no second kind of quota |
| D-140 delete the `/farmstorm farmlandlimit` command tree | **Kept (already deleted in 0.1.1; this release neither deletes again nor restores)** | unchanged | already deleted in 0.1.1 (§1.2 of `BRIEF`: 0 hits of `FarmstormCommands`); this release registers only "not restored" |
| the config `outpost_core.farmlandBonus` (one key, two meanings of D-134 / D-139 of 28) | **retired** | D-153; **the allowance semantics taken over by `outpost_core.cultivatorCapacity` (default 16)** | the number 16 goes from "the number of farmland blocks" to "the number of cultivators", and the key must be renamed, otherwise the semantics would follow the old name |
| the Lucky Berry pool of 19 entries (0.1.1 following the earlier derivation of D-55 / D-97) | **Superseded** | D-156 | `plan0.2.0.txt:15`: no more Rare and Epic; `19 − 8 = 11` |
| the converter's functional surface (the GUI + the "convert" button settlement, D-11 / D-47 of 0.1.0) | **functional surface retired** (the block / the recipes / the menu class kept) | D-170 | `plan0.2.0.txt:33`: remove the function for now, a decorative block only |
| the Outpost Core's "not a container, stores the three owner fields only" (the fact surface of 0.1.1) | **Extended** | D-166, D-165, D-167 | ruling #8: 54-slot storage + 16 bindings; drops on destruction (plan `:30`) |
| D-142 the only S2C packet `core_list` | **Kept**; this release sets up a 2nd packet and **does not extend it** | D-169, D-172 | a full snapshot and a range increment are two kinds of data (D-169 Notes); the red line no longer limits the count |
| D-146 the red-line convention (only "no Mixin" left, no cap on packets) | **Kept** | D-172 | this release adds 1 packet per it and registers it; it does not reopen the 0-packet red line |
| D-122 the 2 cores per player and D-123 the 32×32 core textures (existing clauses of 0.1.0) | **Kept** | unchanged | this release does not touch them (`maxPerPlayer` still defaults to 2, `FarmstormConfig.java:915`); listed here to keep downstream from misreading "the core has been reworked" as "these two have changed as well" |

Points to watch when reading the table: **five things must be named** — ① the farmland binding system (D-134 ~ D-140) retired as a whole; ② `outpost_core.farmlandBonus` retired; ③ `/farmstorm farmlandlimit` already deleted in 0.1.1 and not restored here; ④ the Lucky Berry pool 19 → 11; ⑤ the converter's function retired. Any downstream document that writes about one of these five must land on the corresponding D number per this table. **The reverse one must be remembered too**: the "ally" definition of D-138 **is not superseded** — do not read "retire the farmland system" as "retire the ally judgement".

---

## 7. Rejected options (tombstones)

| # | Rejected option | Reason for rejection | Rejected by whom, when |
|---|---|---|---|
| 1 | **deleting the farmland system's code wholesale** (`FarmlandLedger` / `FarmlandGuard` / the two listener implementations) | ① once deleted there is no way back: the farmland ledger, the binding information and the judgement logic would all have to be rewritten; ② deleting the code erases the history of "why there used to be farmland rules here", and the next version wanting to restore it would have nothing to compare against; ③ commenting out the call sites has already zeroed the run-time effect, and deleting the classes is an action **beyond the requirement** (the plan says "retire", it does not say "delete"). Keeping the two classes + writing one line of reason at the comment is the only way to get "revocable" and "traceable" at the same time | the author, v0.2.0 interview (ruling #3 chose "comment out the call sites only"; RT-42) |
| 2 | making the tier a **`data_component_type`** (a 2nd data component, the tier carried on the item stack) | ① the tier is a **profile-level** property, and made a component it becomes **instance-level**, so "the same id with two tiers" would immediately have to resolve the ambiguity in six places: merging, comparing, stacking, drops, trading and recipes; ② a component default value would have to be generated for each of the 22 profiles, and the registry surface would be +1 (a conflict with the `data_components` +0 of §3.4); ③ a component would need `persistent` + `networkSynchronized`, adding a layer of save and sync out of nothing; ④ a static field costs nothing, and the `BY_ID` reverse lookup table (`CropProfile.java:145-146`) is naturally queryable | the author, v0.2.0 interview (ruling #5 chose "the static field of `CropProfile`") |
| 3 | treating "**capacity 16**" as **the number of storage slots** (a 16-slot core) | ① the plan writes "a **list** of capacity 16, holding the information of the Crop Cultivators bound to it" — the 16 counts **cultivators**, not slots; ② the storage is "equivalent to a large chest", and 16 slots is not even a single chest, a direct contradiction of the plan's own analogy; ③ 16 slots could not hold the delivery volume of "one core with 16 cultivators", and the cultivators would jam on "the core is full → pause" at once (D-164). The two numbers **must be said apart** in this release: 54 = the number of slots, 16 = the cultivator allowance | the author, v0.2.0 interview (ruling #8 fixed "54 slots (a double chest); capacity 16 is the number of bound cultivators") |
| 4 | rendering "each trade can be made once only" as **the offer disappearing** (removed from the list after a deal) | ① once the offer disappears there is **no carrier for the restock** — the offer is no longer in the list, the vanilla restock logic has nowhere to take effect, which equals a permanent purchase limit; ② the player sees one item fewer in the list and cannot tell "sold out" from "this villager never sold it"; ③ the plan's own words are "each trade can be made once only", which is about **the count**, not **existence**; ④ `maxUses = 1` is a ready-made vanilla field, with zero custom code | the author, v0.2.0 interview (ruling #7 fixed "out of stock + wait for the restock", equivalent to `maxUses=1` + the vanilla restock) |
| 5 | closing **only the one `ModifiedCropItem` path** of the planting chain (change `isPlantableSoil` and call it done) | ① the 7 standalone seeds go through `SeedItem extends ItemNameBlockItem` → the vanilla `BlockItem.useOn` and **never pass** `isPlantableSoil` at all; ② `FarmstormCropBlock` overrides neither `mayPlaceOn` nor `canSurvive`, the block side does not block, so closing only the one item-side path leaves the 7 seeds still able to grow crops on farmland; ③ the two paths have completely different criteria, classes and call sites, and each has to be closed once | the author, v0.2.0 interview (ruling #4 requires "cut the plantable path outright"; RT-48 of §4 of `BRIEF` names the two paths) |
| 6 | drawing the link with **particles** (one dust particle every N blocks) | ① a line a few dozen blocks long needs a few dozen particle packets, re-sent every tick; ② it cannot use the perspective / line width / depth handling of `RenderType`, unreadable far away and blurry up close; ③ it cannot move smoothly with the camera (particles are discrete points in world coordinates); ④ the project already has the precedent of "48 particle packets per cycle" (`OutpostCoreBlockEntity.java:274-289`), and stacking a line on top would double the load on the same channel | the author, v0.2.0 interview (ruling #10 chose "client-side line-frame rendering, through a network packet"; RT-45) |
| 7 | carrying the link endpoints with **an invisible marker entity + `CustomName`** (the 0-packet hack) | ① every "core ↔ cultivator" pair would need one entity, so 16 cultivators means 16 entities per core; ② an entity is limited by the client's view distance / chunk loading just the same, and a line outside the range should not be drawn anyway; ③ the packet restriction was abolished outright in 0.1.1 (D-146), so the "0 packets" gain this hack buys equals 0 | the author, v0.2.0 interview (ruling #10; D-146 of 28 has abolished the packet-count restriction) |

---

## 8. Leftovers of this release (registered, not counted as defects)

The eight items below are **deliberately not done in this release / known to exist**, not omissions. Downstream documents may cite them, but must not write them as to-dos or defects.

1. **The texture pack's README mentions GIFs, but the pack contains none**. The README states of itself that "the GIF is an enlarged preview", while measured there are **0 `.gif`** among the 40 files (37 PNG + 2 mcmeta + 1 README). This release copies in only 5 files (3 PNG + 2 mcmeta) and the 2 static first-frame images; the GIFs have nothing to do with this release; §1.8 of `BRIEF` has recorded this as "item 2 of the unconfirmed list".
2. **The texture pack has not been tested in game**. The README states of itself "not yet run-tested in the game", and the repository holds neither a log nor a screenshot of the pack being loaded. So how the 32×512 frame strip + `frametime 2` actually looks, and whether "the two mcmetas are byte-for-byte identical but the top has only 9 distinct frames" (1–7 and 9–15 repeated as a palindrome) looks like a stutter, can only be seen in game (RT-47). This is a **verification gap**, not a design defect.
3. **`FarmlandLedger` / `FarmlandGuard` are kept without callers, deliberately**. What ruling #3 chose is exactly "comment out the call sites only": the two classes (555 + 225 lines) are still in the source tree, still compile, and `grep` finds no caller. This is the source of RT-42 and the place of this release most likely to be "cleaned up while passing by" by a later reader — cleaning it up would delete the possibility of restoring the farmland system later, **do not delete**.
4. **The `CropType` comment says 8 / 5 / 2, at odds with the measured 12 / 7 / 3**. This is a **pre-existing blemish** (the comments at `CropType.java:17,19,21`), earlier than this release, and this release does not change it (the baseline is the published 0.1.1 state, D-148). The same class of blemish also includes: **the class comment at `SeedItem.java:13` says "6 standalone sowers", measured 7** (§1.2 of `BRIEF`).
5. **The `farmstorm_farmland.dat` file of an old save is still on disk**, no longer read or written, and it will not be deleted either (D-154). A "nobody uses it .dat" will remain in the save directory for good — this is deliberate data retention, not leftover rubbish.
6. **The chapters about farmland binding in 29 ~ 33 of 0.1.1 lapse with the retirement of D-153**, but those five documents are frozen and this release does not change them. Typical places: the farmland-related passages of the design delta of 29, the technical chapters of 30, and the tasks and acceptance items of 31. Where a reader meets a conflict, this document prevails (the same handling as §4.5 / §6 of 28).
7. **A max-level farmer in an old save does not gain the 3 new trade offers from this release**. Vanilla trade offers are **issued only on a level-up**: a farmer already at max level in a 0.1.1 old save (or one whose offers of that level have already been generated) does not recompute its offer list because this release adds a `VillagerTradesEvent` subscription. To see the new trades, **a new farmer villager has to spawn**, or a villager not yet at max level has to keep levelling — this is the existing behaviour of the vanilla mechanism, not a defect (the same class of handling as "an old save is not backfilled" in 28).
8. **Two more slips in the source comments** (the same class as §8 item 4, not changed by this release): the section comment at `FarmstormBlocks.java:25` says "14 crop blocks", measured **15** (§0 of `BRIEF`, `FarmstormBlocks.java:26-43`); the javadoc at `LuckyBerryRoller.java:170` says "the 14 kinds of the D-17 pool", measured **19** (`CropProfile.java:139-143`).

---

## 9. Writing-time report (conflicts with `BRIEF`, and places `BRIEF` does not cover)

Per item 9 of §6 of `BRIEF` and item 4 of §0.3 of this document, listed item by item for the author, **without making a trade-off on one's own and without editing the fact card**. The four items marked "**substantive**" change what downstream does or its numbers.

1. **The line-number offset of `gradle.properties`** (formal, **partly resolved**): row 1 of §3.3 of `BRIEF` has been changed to `gradle.properties:11` (noting that "`:10` is `mod_license`"), but **the table of §0 still says `:10`**. This document writes `:11` throughout (28 uses `:11` as well).
2. **The evidence line number of "Java 21"** (formal, **resolved**): §0 of `BRIEF` has added "(Java 21 is pinned by the toolchain `:21-23`)". This document writes "Java 21" only, with no line reference attached.
3. **The line numbers of the 3 farmland language keys** (formal): row 9 of §3.3 of `BRIEF` writes `ModLangProvider.java:403,413`, but of the 3 keys `message.farmstorm.farmland.no_core` is at **`:412`** and `:413` is `quota_full`. This document writes `:403`, `:412-413`.
4. **The definition lines of `FarmstormConfig`** (formal): row 8 of §3.3 of `BRIEF` writes `FarmstormConfig.java:227,909`; the field is at `:227`, and the definition body is **`:909-912`** (the default value `16` falls on `:912`). This document writes `:909-912`.
5. **The texture resources under-counted by 2** (**substantive**, **resolved**): the original text recorded only "3 PNGs + 2 mcmetas", but the "idle static" of ruling #12 can only be done with a **static first-frame image** under an mcmeta animation (the animation is texture-level and the code has no pause switch). §3.5 of `BRIEF` has been changed to "**5 new PNGs + 2 mcmetas**" and names those 2 static first frames (`editable_frames/side_00.png` / `top_00.png` shipped with the texture pack). This document follows "the 3 faces of the block + the 2 static first frames + the 2 mcmetas" (§3.1 row 9, §3.4, D-161).
6. **A `cultivator` menu would make the registry surface 98** (**substantive**, **resolved**): row 13 of §3.3 of `BRIEF` has made it explicit — "(**no menu**: right-click puts the item in directly, no interface opens)", consistent with the `menu_types +1` of §3.1 (the core storage only) ⇒ 97. D-160 / D-171 of this document follow that convention.
7. **"3 listeners" is 2 + 1** (formal, **resolved**): item 3 of §2 of `BRIEF` has been changed to "**2 listeners** (`:75` / `:76`) and **1 backfill line** (inside `:166-176`), and `onServerTickPost` itself **must be kept**". D-153 of this document agrees with it.
8. **The three-way classification of ruling #15** (**substantive**, **resolved**): the third interview supplemented #15 into "**7 with standalone seeds** (producing 2 + 2), **8 seeds = the crop itself**, **7 with no seed at all** (the last two classes producing 2 crops only); of the 18 in the admission surface **5 give 2 + 2 and 13 give 2 only**", consistent with the reading of D-163 / §4.3 of this document. The earlier gap of "covering only the two ends" is resolved, and downstream cites #15 of §2 of `BRIEF` and D-163 of this document directly.
9. **The save location in §3.6 of `BRIEF`** (**substantive**, **resolved**): §3.6 of `BRIEF` has been split into three — ① `farmstorm_farmland.dat` not read, not written, not deleted; ② the entry shape of `farmstorm_outpost_core.dat` (the index) **unchanged**, with the note that "it is **two different things** from the NBT of the core block entity, do not mix them"; ③ the NBT of the core block entity is in the **chunk save**, and its structure has changed a lot. §3.4 / D-166 of this document follow these three.
10. **The comment slip of `SeedItem.java`** (a pre-existing blemish): `:13` says "6 standalone sowers", measured **7** (§1.2 of `BRIEF` + `FarmstormItems.java:89,100-107`). The same class as the 8/5/2 of `CropType`; this document does not change it, and it is registered in §8 item 4.
11. **The level positions of trading** (**settled: 1 / 3 / 5 (36)**): ruling #6 gives only the three **example positions** "novice → Common, journeyman → Rare, master → Epic"; 36 has fixed the unlock levels of the three offers as **1 / 3 / 5**, in one-to-one correspondence with those three positions (D-157 Notes). The state of the original text "left to 36 to check" ends here.
12. **The binding is stored twice** (an internal tension of `BRIEF`, **resolved**): §3.6 of `BRIEF` has made clear that "the copy in the cultivator's BE is a **reference cache of the bound core** — the only truth is the 16-entry list on the core side", consistent with "the only truth on the core side + validation before use" of D-165 of this document. 36 must still pin down the validation points (write both copies on placement, validate before use, change both copies on migration).
13. **The version numbers of `plan0.2.0.txt` are old values** (already declared by `BRIEF`, registered for the record): `:2` writes "version number: 0.1.1" and `:5` writes "with 0.1.0 as the main version", in conflict with the 0.2.0 / baseline 0.1.1 of ruling #1. This document follows ruling #1 (D-148) and does not align with the plan's original text.
14. **The access permission of the core storage** (`BRIEF` has been supplemented as ruling #17): the first draft had no such item, and the first version of this document wrote it as "ally only" (marked **[author's supplement]** at the time). The third interview settled it as **two gates: ally + inside the player's reach**, with **the reach going through the vanilla container distance check** (the default semantics of `AbstractContainerMenu#stillValid`) and **no custom distance gate set up separately**. The Decision and the Notes of D-166 have been rewritten per #17, and the sentence "ally only" of the first version **is void**.
