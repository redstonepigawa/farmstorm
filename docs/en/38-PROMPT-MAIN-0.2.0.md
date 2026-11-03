# 38 · v0.2.0 Implementer's Master Prompt

> Feeding method: this file is fed **once only**, before any Phase. After that, feed per Phase following document 39.
> Reading order: **34 (decisions, the single source of truth) → 35 (design) → 36 (tech) → 37 (tasks and adjudications) → this file**. Where they conflict, document 34 wins; this file is harder than document 34 in exactly two places: the **red-line standard** (HC-115) and the **execution discipline** (§5 ~ §8).
> This file's standing: **a standing constraint** — read it before you start any work, then read the current Phase that document 39 gives you. This file does not answer "which thing to do this round" (that is documents 37 / 39's business); it only answers "which ways of doing it are definitely wrong".
> Do only what the task brief asks, one thing at a time: stop when done, report, wait for the next Phase. Do not implement the next stage in advance, and do not stuff a "fixed it on the way" into this round.

| Item | Value |
|---|---|
| Document ID | 38-PROMPT-MAIN-0.2.0 |
| Audience | the implementation agent (can read and write this repository, can run `.tools\gw.cmd`) |
| Upstream | `34-DECISIONS-0.2.0.md` (D-148 ~ D-172, 25 entries + 19 rulings) → `35-DESIGN-0.2.0.md` (11 requirements / 40 boundaries) → `36-TECH-0.2.0.md` (97 registrations / 46 action-surface rows / 175 config / 337 language keys / 36 acceptances / 13 to verify) → `37-TASKS-0.2.0.md` (34 tasks / 8 Phases / 8 adjudications) |
| Numbers this file issues | **HC-97 ~ HC-120 (24 entries)**, **N-93 ~ N-114 (22 entries)**, fact card **19 rows**, pitfall table **31 entries** (items 1 ~ 20 inherited from document 32, items 21 ~ 31 new in this version), known conflicts **10 rows**, pre-start confirmation **12 items** |
| What this file does not write | class design and algorithms (document 36), tasks and schedule (document 37), per-Phase content (document 39) |
| Numbering relay | decision **D-148** / task **F-213** / Phase **32** / milestone **M28** / hard constraint **HC-97** / prohibited behaviour **N-93** / risk **RT-42** / adjudication **A-62** |

## 0. What You Are Facing

v0.2.0 is not "a few more things piled on top of 0.1.1". It is **the first time the previous version overturns its own core mechanism**: 0.1.1's farmland binding regime (D-134 ~ D-140: one farmland block belongs to only one core, 16 blocks per core, quota = 16 × the number of allied cores) is retired wholesale; that "16" did not disappear, it **changed what it points at** — from "how many farmland blocks per core" to "how many cultivators per core" (D-165) — and the key changed from `outpost_core.farmlandBonus` to `outpost_core.cultivatorCapacity`. So what you have opened is a version like this: **the thing being bound has changed, the core has become a chest, two old acquisition paths (planting and the converter) are broken at the same time, and crops have tiers for the first time.**

The way it was retired is "comment out the call sites only" (ruling #3): the two classes (`FarmlandLedger` 555 lines / `FarmlandGuard` 225 lines) are not changed by a single line, are still in the source tree and still compile; `farmstorm_farmland.dat` is not read, not written and not deleted. **Understanding that "commenting out ≠ deleting" is this version's first threshold**; whoever reads it wrong will go and delete code, delete the save or delete blocks, and none of those three is in this version's scope (HC-116 ~ HC-118 in §1).

**Four firsts for the whole project** (each is costed at L, and none may "just copy an existing class"):

| # | First | Why there is no precedent | Where it lands |
|---|---|---|---|
| 1 | **Villager trading** | the whole repository has **0 hits** for `villager` / `trade` / `VillagerTradesEvent`, and there is no villager directory under `data/farmstorm/` | D-157 / D-158 / D-159; F-222 / F-223; RT-46 |
| 2 | **Drawing lines in world space** | the whole repository has 0 occurrences of `RenderType.lines()`; `RenderLevelStageEvent` appears only as the matrix capture in `WildHunterOutline:66-74` and **draws no geometry** | D-169; F-238 / F-240; RT-45 |
| 3 | **mcmeta animated textures** | the repository's mod has **0** `*.png.mcmeta` and **0** vertical frame strips; the two mcmeta files must also be byte-for-byte identical | D-161; F-225; RT-47 |
| 4 | **The core becomes a container** | the current class **only** `extends BlockEntity` and has no `getUpdateTag` / `getUpdatePacket`; this repository has no precedent for "a non-container BE upgraded to `Container`" | D-166 / D-167; F-226 / F-227; RT-44 |

**The three most expensive facts** (memorise them; they decide half of this version's rework):

1. **54 and 16 are not the same thing**: 54 is the core's **slot count** (a double chest = 27 × 2), and 16 is the **cultivator capacity** per core. Mix the two and the menu breaks, the documents go wrong, and `RT-44` hits in two places at once (ruling #8, document 34 §7 tombstone 3).
2. **"Cannot plant" must be cut once on each of two paths**: `ModifiedCropItem#isPlantableSoil` and the 7 `SeedItem#useOn`. Block only the former and the 7 standalone seeds can still grow crops on farmland (D-152, RT-48).
3. **Tier is defined in exactly one place**: the 22 profiles' `tier` feeds trade sampling, cultivator admission and the Lucky Berry pool at once; get one wrong and all three go wrong together (RT-49).

## 1. Hard Constraints HC-97 ~ HC-120

The HCs constrain only this version's increment surface. **11 entries** of document 32's HC-77 ~ HC-96 are superseded by this version (8 rows; the list is in §6.3), and the rest remain in force; **where they conflict, this file and document 34 win**. Each entry gives three things: the **constraint** (what counts as right), the **criterion** (what counts as passed) and the **source** (decision / tech / task number). The criterion column is always executable: one command, one `git diff`, one log line, one reading.

- **HC-97 · The registration surface is 97, all 12 classes named one by one, and the five constants changed together**
  - Constraint: the 12 class subtotals = `items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2` = **97** (`93 + 4`). Four new ids: `farmstorm:cultivator` (one entry each on the block / block_entity_type / item registration surfaces) + `farmstorm:outpost_core` (menu_type). The five numbers at `Farmstorm.java:147-150` go from `93 / 32 / 26 / 7 / 1` to `97 / 33 / 27 / 8 / 2`; the other 8 classes are not changed by a character.
  - Criterion: the startup log contains `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / … / menu_types 2 → OK` (the format string is at `Farmstorm.java:151-155`); **the closing task F-228's** `git diff` touches only these 5 numbers and the method comment (the tier self-check line belongs to F-214 and the pool assertion to F-216, each committed separately); report a reading for each of the twelve classes one by one, never just the total.
  - Source: document 36 §1.2 (the item-by-item table); D-171; document 37 table B.

- **HC-98 · Three in-between windows: never write "self-check OK" inside a window**
  - Constraint: the registration window = **F-224 → F-228** (inside it the actual number is **96** first and **97** later, while the assertion constant is still 93 ⇒ the log suffix is `→ FAIL`; only the closing task F-228 writes "it turns OK when done"); the language-key window = **F-215 → F-241**, the config window = **F-220 → F-242** (tasks in between never touch those two total-count comments; their criteria only say "this key exists / does not exist"). Inside a window the four negative self-check lines (`作物 22 种` / `作物品质 普通 11 / 稀有 7 / 史诗 4` / `幸运浆果池 11 种` / `网络包注册`) **must all be green**; any red is a real fault.
  - Criterion: the task reports for F-224 ~ F-227 give the verbatim FAIL line from `latest.log` plus the four subtotal numbers (state A: `96`, `items 33 / blocks 27 / block_entities 8 / menu_types 1`; state B: `97`, `menu_types 2`); in the assertion section of any report inside the window `grep "→ OK"` hits 0 (except F-228).
  - Source: document 37 §0.3; document 36 §1.2.

- **HC-99 · The server config is 175 keys / 60 groups (delete 1, add 10)**
  - Constraint: `166 − 1 + 10 = 175`, group count `58 + 2 = 60` (two new top-level groups: `cultivator` with 2 keys, `trade` with 6); `outpost_core` is **adding keys, not deleting the group and rebuilding it** (3 → 4 keys: delete `farmlandBonus`, add `cultivatorCapacity` default 16 / range 1–64 and `storageSlots` default 54 / range 54–54). Defaults of the 10 new keys: `16 / 54 / 600 / 2 / 2 / 5 / 10 / 32 / 40 / 64`.
  - Criterion: **first delete** `run/config/farmstorm-server.toml` and then start the server; in the regenerated file the `^\s*\[` lines = **60** and the key lines `^\s*[A-Za-z_].*=` = **175**; `[outpost_core]` has exactly 4 keys and `grep -c farmlandBonus` = 0; `[cultivator]` has 2 keys (600 / 2) and `[trade]` 6 keys. In-between readings (not assertions; only to prove you have not drifted): after F-220 165/58 → after F-222 171/59 → after F-226 172/59 → after F-231 173/59 → after F-234 175/60.
  - Source: document 36 §4.1, §4.1.1, §4.1.2; D-159 / D-163 / D-165 / D-166; document 37 table C.

- **HC-100 · CLIENT config: 3 keys, a file of its own, not counted in the 175**
  - Constraint: `hud.enabled` (bool, **true**) / `hud.x` (int, **4**) / `hud.y` (int, **4**), landing in `config/farmstorm-client.toml`; this version **does not touch it**; it is not counted in the 175 and is never written mixed with the server's; but it **does count** towards the language keys' `farmstorm.configuration.*`.
  - Criterion: `run/config/farmstorm-client.toml` has exactly 3 keys / 1 group; `farmstorm-server.toml` has `grep -c hud` = 0; the server's group count is still 60.
  - Source: document 36 §4.2; D-144 (already in 0.1.1).

- **HC-101 · Language keys 337 × 2, with the Chinese and English key sets exactly equal**
  - Constraint: `323 − 3 + 17 = 337`; each of the two files holds 337 and `set(zh) == set(en)`; after the change, prefix by prefix: `item 33 / block 27 / container 2 / tooltip 20 / farmstorm.configuration 178 / message 0`, with the other 9 buckets unchanged; the class comment at `ModLangProvider.java:13-16` is brought into line as "33 / 27 / 178 / 20 / 0 / 337".
  - Criterion: `zh_cn.json` and `en_us.json` each have 337 keys (`ConvertFrom-Json` to take the key sets), zh-only = 0 and en-only = 0; `grep -c '"message\.farmstorm'` is 0 in both; `grep -c '"farmstorm.configuration\.'` is exactly **178**; `grep -c 'tooltip\.farmstorm'` is exactly **20**.
  - Source: document 36 §4.3.1 ~ §4.3.3; D-153 / D-159 ~ D-166; document 37 table D.

- **HC-102 · The 46-row action surface covered row by row, plus 4 items outside the table**
  - Constraint: **not one row of document 36 §3's 46 rows may be missed** (registration 5 / class 23 / data 8 / retirement 9 / config 1); the **4 items outside the table** must be done too, and each is backed by an adjudication: ① the version number at `gradle.properties:11`; ② the two `.cmd` files' jar names (A-65); ③ adding `CULTIVATOR` to `FarmstormItems.creativeTabItems()` (A-64); ④ the expectation surface of `tools/verify_project.py` (A-66). Items outside the table **do not enlarge the 46 rows**, and you may not add line numbers into document 36 §3.
  - Criterion: each report's "Conclusion" section names the action-surface row numbers this task owns (e.g. `#17~#22, #25, #26`); all 46 rows of document 36 §3 have an owner (document 37 table A already gives them); each of the 4 items outside the table has its own independent reading (the jar name in 3 places, one more item at the end of the creative tab, the tool's three-section output, `mod_version`).
  - Source: document 36 §3; document 37 table A + §5.3 ~ §5.5.

- **HC-103 · The version number has only one source, and the two `.cmd` files must be kept in sync**
  - Constraint: `mod_version` is changed once only, at `gradle.properties:11` (`0.1.1` → **`0.2.0`**); the hard-coded `farmstorm-0.1.1+mc1.21.1` (including `-sources`) in `一键构建.cmd:11`, `:12` and `检查环境.cmd:14` is changed to `0.2.0` **in the same pass**; other documents only reference it and do not each declare a version.
  - Criterion: `grep -c "0\.1\.1" gradle.properties` = 0; in the two `.cmd` files `farmstorm-0.2.0+mc1.21.1` has **3** hits and `0.1.1` has 0; the artifacts in `build/libs/` and `dist/` have the same name.
  - Source: D-148; A-65; document 36 §1.1.

- **HC-104 · Tiers 11 / 7 / 4 = 22, carried by `CropProfile`'s 9th field**
  - Constraint: add a 9th field `CropTier tier` to `CropProfile` (**after** `throwStack`), and create a new `CropTier` enum (`COMMON` / `RARE` / `EPIC` + `translationKey()` + `color()` + `nameStyle`); fill in the value of all 22 profiles, Common **11** / Rare **7** / Epic **4**; **add no** `data_component_type` (still 1), write no item NBT, open no new registration object, and **change no texture**. Construction surface = **10 construction points** (7 direct `new CropProfile(...)`: `:58,87,92,97,102,117,125`; 3 private factories) + **15 factory arguments**.
  - Criterion: the startup log gains `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK`; all 7 hits of `grep -c "new CropProfile("` carry a `CropTier.` argument, one by one; `data_components` is still 1; `textures/item/` shows **0** changes in `git status`.
  - Source: D-149 / D-150; document 36 §2.1; document 35 §2.2; RT-49.

- **HC-105 · Name color plus one tooltip line: 29 items, 3 classes, 0 texture changes**
  - Constraint: `ModifiedCropItem` (15 items), `DeployableCropItem` (7, which **does not extend** `ModifiedCropItem` and must be overridden separately) and `SeedItem` (7) each override `getName(ItemStack)` to go through `CropTier.nameStyle` (`WHITE` / `BLUE` / `GOLD`); the tooltip **appends 1 line** at the end of `appendCropTooltip` (`tooltip.farmstorm.tier`), **not merged with the category line**; **do not reuse the vanilla `Rarity`** (the colors do not match); 4 language keys are added.
  - Criterion: `15 + 7 + 7 = 29` item names are colored by tier, and the same item keeps the same color in an anvil rename, on a sign and in chat; the tooltip has exactly 1 more line ("Tier: Common / Rare / Epic"); `grep -rn "Rarity" src/main/java` = 0; the 29 images under `textures/item` show 0 changes.
  - Source: D-151; document 36 §2.1, appendix A #2 ~ #3.

- **HC-106 · "Cannot plant" must be blocked once on each of two paths**
  - Constraint: ① `ModifiedCropItem#isPlantableSoil` (`:101-107`) is **always `false`** (leave the original criterion as a comment; do not delete it); ② the `SeedItem#useOn` override returns **`PASS`**. `FarmstormCropBlock` **is not touched** (it does not override `mayPlaceOn` / `canSurvive`; the block side never blocked planting anyway). All 15 crop blocks, 22 crop items, 7 seeds and 26 loot tables **stay registered**.
  - Criterion: right-clicking **farmland** with any of the 22 crops or 7 seeds: no block appears, the held item is not consumed, and there is no message; right-clicking **farmland** with the 7 deployable items places the deployed form (D-152 supplement; see pitfall 24), while right-clicking the top face of **non-farmland** places normally; `grep -c "mayPlaceOn\|canSurvive" block/FarmstormCropBlock.java` = 0.
  - Source: D-152 / D-155; document 36 §2.2; RT-48.

- **HC-107 · The Lucky Berry pool is 11 entries, derived from `tier()`**
  - Constraint: `LUCKY_BERRY_POOL` goes from "excluding 3 entries" to a **filter construction** of `tier() == COMMON`; a hand-written list of 11 ids is **not allowed** (A-63); the assertion at `Farmstorm.java:119` goes from `19` to **`11`**, keeping the other half "excludes `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`"; the pool and the assertion go into **one and the same commit** (RT-50).
  - Criterion: the startup log has `自检 幸运浆果池 11 种… → OK`; inside the pool section (`CropProfile.java:139-143`) `grep -c "CropProfile\."` = 0 (no second id list); within that section `19` has 0 hits.
  - Source: D-156; A-63; document 36 §2.1; document 35 §5.2; RT-50.

- **HC-108 · Trading: 3 entries, levels 1 / 3 / 5, `maxUses = 1`**
  - Constraint: add **3** entries to **farmers only** (`VillagerProfession.FARMER`), at levels **1 / 3 / 5**; each entry has `maxUses = 1` and gives **2** crops at a time, **sampled once** by tier when the offer is generated (not re-rolled on the trade); the price comes from `trade.<tier>.min~max` (2–5 / 10–32 / 40–64, key defaults 2/5, 10/32, 40/64), with `xp = 2` and `priceMultiplier = 0.0F` (locking the base price); **write no** second restock and no back-fill for old saves.
  - Criterion: the farmer trade screen shows exactly 3 entries, and all three have `maxUses` = 1 in `/data get entity <villager> Offers.Recipes`; the first offer's `buy.count` falls inside the range; after one trade the entry is **still there**, greyed out and unclickable; after a restock the price and the crop id are unchanged.
  - Source: D-157 / D-158 / D-159; document 36 §2.4, appendix A #10 ~ #11; RT-46.

- **HC-109 · Cultivator: four gates, 600 ticks, 5 give 2+2, 13 give 2 only**
  - Constraint: admission = range gate (the Chebyshev distance between the cultivator's chunk and the allied core's centre chunk ≤ 1 ⇒ 3 × 3) + binding gate (it is in the core's 16-entry list) + held-item gate (Common / Rare) + empty-slot gate; failing any one of them means "right-click does nothing"; the crops that may be inserted are **18 = 11 + 7**, while the 4 Epic crops and the 2 Epic seeds are always refused; the cycle is **600 ticks** (through `cultivator.cycleTicks`) and a single yield goes through `cultivator.yield`, default **2**; **5 entries inside the admission surface give 2 crops + 2 seeds** (`puffed_wheat` / `melon_grenade` / `pumpkin_turret` / `friend_chives` / `power_beet`), and the other **13 give 2 crops only**; when the core is full the whole batch pauses (progress stays at the completed state, no slot is cleared, nothing is dropped, nothing is lost, nothing is queued).
  - Criterion: on tick 600 the core's 54 slots gain +2 (the 5 entries also add 2 seeds); once the core is full, `Progress` from `/data get block <cultivator>` = 600 and does not move, and there is no `ItemEntity` on the ground; after space is freed it delivers automatically and **does not restart the timer**; an Epic right-click does nothing and the held item is not consumed.
  - Source: D-160 / D-162 / D-163 / D-164; document 35 §6.2 ~ §6.4; document 36 §2.5; RT-44.

- **HC-110 · Cultivator: no menu registered, two textures, anvil properties**
  - Constraint: the `menu_types` +1 **goes to the core's storage only**; the cultivator **registers no menu** (registering one more makes 98, a direct conflict with HC-97); the block properties follow the anvil: `strength(5.0F, 1200.0F)` + `MapColor.METAL` + `requiresCorrectToolForDrops()` + `SoundType.ANVIL` + `PushReaction.BLOCK`; `working` is a **boolean blockstate property** with two models (`working=false` references the `_still` static image, `working=true` references the frame strip); assets = **5 PNG + 2 mcmeta = 7 files** (3 frame strips / bottom face + 2 static first frames + 2 mcmeta), and the two `_still` images **carry no** mcmeta.
  - Criterion: `blockstates/cultivator.json` has exactly **2** variants; there are exactly **2** `*.png.mcmeta` (side / top, with `frametime 2` / `interpolate false` / `frames 0..15`); with an empty slot the block is still; after a Common crop is inserted it runs one round per 1.6 seconds (`16 × 2 = 32` ticks); after 600 ticks it stops and clears the slot; `menu_types` = 2.
  - Source: D-160 / D-161 / D-171; document 36 §5, §2.5.5; RT-47.

- **HC-111 · The core: a 54-slot container, two gates, four drop conditions**
  - Constraint: `OutpostCoreBlockEntity implements Container` with `SLOT_COUNT = 54` (6 × 9 double chest = 27 × 2), and `outpost_core.storageSlots` is pinned to **54–54**; the access condition in `useWithoutItem` is **two gates = allied ∧ the vanilla `stillValid` reach** (**set no new radius, add no config key, introduce no distance constant**); `onRemove` drops by four conditions (a real removal / not pushed by a piston / not replaced by the same block / the container is not empty); `getUpdateTag` **writes the three owner keys only** (no `Items`, no `Cultivators`); an old save missing the keys reads as **54 empty slots**.
  - Criterion: an allied right-click opens the 54-slot screen (title "Outpost Core"); a non-allied right-click returns `PASS` and opens nothing; 9 blocks away from the core it does not open, and after it is opened walking away closes it through the vanilla `stillValid`; breaking the core drops the whole chest, while a piston push / same-block replacement / chunk unload **drop nothing**; an old-save core opens as 54 empty slots with no error.
  - Source: D-166 / D-167; ruling #17; document 36 §2.6, §6③; RT-44.

- **HC-112 · Binding 16: the single source of truth is on the core side, and writes happen at exactly two moments**
  - Constraint: `outpost_core.cultivatorCapacity` defaults to **16** (1–64); one core has ≤ 16 cultivators and one cultivator has **0 or 1** core; the **single source of truth for a binding is the core's 16-entry list**; the cultivator BE's `Core` is a **reference cache**, checked against the core-side list before every use, and any disagreement between the two sides is treated as **unbound**; writes happen only at **placement** and at **migration when a core is broken**, and at runtime there is **no nearest-drift / no periodic rebind / no auto-claim**; when there is no core to bind to the block is still placed, but it is **permanently unbound and unusable** (the only way out is to break and replace it).
  - Criterion: placing the 17th binds nothing and **evicts none** of the already-bound ones; `/data get block <cultivator>` has a `Core` key; placing a core **afterwards** next to an unbound cultivator → it is not auto-claimed; the two numbers 16 and 54 are **read separately** in the report.
  - Source: D-165; document 35 §8.2, §6.8; document 36 §6④; ruling #8.

- **HC-113 · Migration's three exits: rebind / stay in place marked unbound / defensive skip**
  - Constraint: `settleOnRemoval` (`OutpostCoreBlockEntity.java:341-394`) is rewritten as a whole into **cultivator migration**; the three exits are "rebind to a successor core (coverage + allied + a free slot, nearest first)" / "none at all ⇒ stay in place, keep the contents, mark unbound, unusable" / "the chunk cannot be obtained ⇒ keep the NBT's original values, do not guess, do not delete the binding"; **there is no fourth branch and no "turn to dirt"**; `setBlock` has **0** hits inside the method body; the migration stretch **must not load chunks with `getChunk`** (`getChunkNow` only).
  - Criterion: with a successor core ⇒ `Core` becomes the new core's coordinates and `Held` / `Progress` are unchanged; with no successor ⇒ the cultivator is still in place, the `Core` key is gone, right-click does nothing and nothing drops; when the chunk is not in memory ⇒ stop the server and read the region, and that BE's NBT is **byte-for-byte unchanged**; `grep -c setBlock` in the method body = 0.
  - Source: D-168; A-69; document 36 §2.7; RT-43.

- **HC-114 · The link is the 2nd packet: send only, draw lines only**
  - Constraint: add a 2nd custom packet `farmstorm:cultivator_link` (**S2C read-only**, `MAX_LINKS = 1024`), and change the protocol version string from `"1"` to **`"2"`**; **do not extend `core_list`** (a full snapshot and a range delta are two kinds of data); send it only while **the player is inside that core's 3 × 3 chunk range**; the client is **read-only**: draw exactly what it receives, do not compute bindings itself, do not cache across dimensions, and clear the leftovers on leaving the range; **no server logic may depend on the link** (binding / yield / migration / drops have nothing to do with it).
  - Criterion: `grep -rln "CustomPacketPayload" src/main/java` gives exactly **2** files; `playToClient` has exactly **2** occurrences and `playToServer|sendToServer` **0**; after temporarily making the client handler an empty implementation, binding / yield / migration / core-break drops **all behave as usual**; after walking out of range the lines disappear immediately.
  - Source: D-169 / D-172; document 34 §5.2; document 36 §2.8; RT-45.

- **HC-115 · Red line: Mixin is still 0, and the packet total is 2 with each one registered**
  - Constraint: **No Mixin** is the only project-level red line retained: no `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor`, and no mixin plugin added to `build.gradle`; network packets have **no numeric ceiling**, but this version **adds only this 1** (a total of **2**), both S2C read-only and both registered in document 34 §5.2; payloads go through `RegisterPayloadHandlersEvent` and **do not enter the registration surface** (which is why it is not among HC-97's +4).
  - Criterion: `grep -rn "@Mixin\|mixins.json" src/` gives **0** hits; `git diff build.gradle` is empty; there is no `*.mixins.json` under `src/main/resources`; the startup log has two `自检 网络包注册` lines; `playToClient` appears 2 times, and the packet ids are `farmstorm:core_list` and `farmstorm:cultivator_link`.
  - Source: D-172; document 34 §5; D-146 of document 28; document 36 §1.4.

- **HC-116 · Retiring the farmland system = comment out the call sites only, and three things must be kept**
  - Constraint: **5 comments on the event surface** — `FarmstormGameEvents.java:75`, `:76` (two registration lines), `:241-265` (the whole hoeing interception block, including `ledger.bind` at `:254`), `:277-281` (the whole trample protection block, including the `protectsFromTrample` call at `:278`), `:171` (the single line `FarmlandLedger.get(server).backfill(server)`); above each of these 5, write one line of reason "ruling #3: v0.2.0 retires the farmland system, do not restore". **1 comment on the key definition**: the `.defineInRange` definition at `FarmstormConfig.java:909-912` (the field declaration `:227` is **kept**, see HC-117 / A-62). **3 language keys stop being emitted**: the 3 farmland keys at `ModLangProvider.java:403`, `:412`, `:413` **are no longer produced** — this is "no longer produced", not the act of "commenting out"; the criterion looks at the artifact. **Three things must be kept**: `onServerTickPost` itself (`:166-176`, which still has to run `CoreListBroadcaster.tick(server)` (at `:175`), so commenting out the whole block means the overlay never receives a packet again), `CoreChunkTickets.restoreAll(server)` (`:170`, serving the core chunk ticket), and the two classes' bodies `FarmlandLedger` (555 lines) / `FarmlandGuard` (225 lines) (**not one line changed**).
  - Criterion (**three, verified separately, not merged**):
    1. **Event surface**: the three calls in `FarmstormGameEvents.java` — `ledger.bind` (`:254`) / `protectsFromTrample` (`:278`) / `backfill` (`:171`) — are **all commented out**, and the **registration line** of `onServerTickPost` **is still there** (`:73`).
    2. **Key definition**: `:909-912` in `FarmstormConfig.java` **is commented out**, and the field declaration at `:227` **is still there** (A-62).
    3. **Language keys**: the 3 farmland keys **disappear from the artifact** (they are not commented out) — in both `zh_cn.json` / `en_us.json`, `outpost_core.farmlandBonus`, `message.farmstorm.farmland.no_core` and `message.farmstorm.farmland.quota_full` each have **0** hits, and the whole `message.farmstorm` bucket goes to **0**.
  - Source: D-153; document 36 §2.3 (the 5 + 1 + 3 composition); document 37 F-220 ("5 comments" counts the event surface only); RT-42; document 35 §7.2.

- **HC-117 · A-62: keep the field declaration, comment out the definition only**
  - Constraint: the field declaration at `FarmstormConfig.java:227` is **kept as it is** (delete it and the retained classes no longer compile, which is a roundabout way of changing them); comment out only the `.defineInRange(...)` definition at `:909-912`; the two `.get()` calls at `FarmlandGuard.java:75` and `FarmlandLedger.java:305` are **kept as they are** (unreachable at runtime); next to `:227` write two comment lines: the definition is commented out ⇒ the field is `null` ⇒ the definition must be restored before the listeners are restored, otherwise `FarmlandGuard:75` throws `NullPointerException`. **Do not** turn that key into a hard-coded constant `16`.
  - Criterion: `grep -n "outpostCoreFarmlandBonus" config/FarmstormConfig.java` gives exactly 2 hits (1 declaration + 1 commented-out definition); `grep -c farmlandBonus run/config/farmstorm-server.toml` = 0; the two `.get()` calls show 0 changed lines in `git diff`.
  - Source: A-62 (the full ruling is at the end of document 36 §10); document 36 §2.3; D-153.

- **HC-118 · The three don'ts for `.dat`, and its criterion only after F-233**
  - Constraint: `farmstorm_farmland.dat` (whose on-disk name is `DATA_NAME` at `FarmlandLedger.java:53`, **not** `:44`) is **not read, not written and not deleted**; the file stays in the save as it is. Its **last runtime reader** is `FarmlandLedger.of(server)` at `OutpostCoreBlockEntity.java:353` (inside `settleOnRemoval`), and only **F-233** rewrites that whole block ⇒ the criterion "the file is still there and its mtime is unchanged" **may only be run after F-233**.
  - Criterion: before F-233, the report explicitly says "breaking a core still reads and writes that file; this item is not an assertion"; after F-233, upgrade an old save → the file is still there, its mtime is unchanged and its contents are byte-for-byte unchanged; `latest.log` has 0 read/write records for `farmstorm_farmland`.
  - Source: D-154; A-68 (line number); A-69 (timing); document 36 §6①, §8 #14.

- **HC-119 · No save migration, no data back-fill, no downgrade code**
  - Constraint: write no toml migration, no `.dat` migration, no villager `Offers` back-fill and no version-downgrade branch; old-save compatibility allows only "missing key means empty" (a core missing `Items` ⇒ 54 empty slots, missing `Cultivators` ⇒ an empty list); "a max-level farmer in an old save will not gain 3 trade entries" goes into the **delivery notes**, not into code.
  - Criterion: the hits of `grep -rn "farmstorm-server.toml\|farmstorm_farmland" src/main/java` appear only in comments (0 code hits); `git diff` contains no migration / back-fill class; the report's "upgrade notes" contain the three sentences "a max-level farmer in an old save gets no back-fill", "`.dat` is not read, not written and not deleted" and "an old-save core reads as an empty container".
  - Source: D-154 / D-158; document 36 §6, §10 #16; D-147 (0.1.1's "the code does no migration" remains in force).

- **HC-120 · The tool must be brought into sync: 97 / 175 / 337 in four places**
  - Constraint: change four places in `tools/verify_project.py` in the same pass — add the 4 new ids to `EXPECTED` (the 12-class id whitelist), change `EXPECTED_COUNTS`' 93 to **97**, sync the config expectation set's five-document union to 175, and change the print string `已核 registry 对象: {} / 93` to `/ 97`. **The consequence of not syncing is silently checking 4 fewer objects and exiting 0**, which is more dangerous than going red.
  - Criterion: `python -X utf8 tools/verify_project.py` outputs `97 / 97`, config **175**, lang **337** and `FAIL 0`, and **the tool's `git diff` is given first**; before the change you may cite only the sectional readings (`93 / 93`, `323 keys`), never `FAIL 0`.
  - Source: A-66; document 36 §1.2; document 37 §5.5.

## 2. Prohibited Behaviour N-93 ~ N-114

Each one can be decided by one command, one place in `git diff` or one reading; these are not attitude requirements. The "why" is one sentence only, enough for you to judge the boundary.

- **N-93** Never write "retiring the farmland system" as deleting code / deleting the save / deleting blocks.
  - Criterion: `grep -rn "删除耕地\|删掉两个类\|清除耕地数据"` in reports and comments = 0; the two classes under `farmland/` show 0 changes in `git status`; the 15 crop blocks are still registered.
  - Why: the definition of retirement is "comment out the call sites only"; those three would make the runtime effect and the traceability disappear together (document 34 §0.2's term "retire the farmland system", D-153, D-154, D-155).

- **N-94** Never change a single character in `FarmlandLedger` / `FarmlandGuard`.
  - Criterion: both files' `git diff` is empty and their line counts are still 555 / 225; those two `.get()` calls (`FarmlandGuard:75`, `FarmlandLedger:305`) are there as they were.
  - Why: ruling #3 and A-62 both rest on "not one line changed in the bodies"; change 1 line and you have to rewrite the definition and the `null` semantics along with it (RT-42).

- **N-95** Never comment out `onServerTickPost` as a whole block.
  - Criterion: `:166-176` is still there and `CoreListBroadcaster.tick(server)` at `:175` runs every tick; only the `:171` line is in a commented state.
  - Why: it is the overlay packet's only per-tick entry; comment out the whole block and the overlay never receives a packet again (document 34 §9 ⑦, document 36 §2.3).

- **N-96** Never turn `farmlandBonus` into a hard-coded constant.
  - Criterion: `git diff src/main/java/com/dashpig/farmstorm/farmland/` is empty (not one character in the two classes); `FarmstormConfig.java:227` is still the field declaration; `grep -rn "outpostCoreFarmlandBonus" src/main/java` gives exactly **4** hits (1 declaration at `:227` + 1 commented-out definition at `:909` + 2 `.get()` calls in the retained classes), with no fifth new read.
  - Why: that amounts to quietly keeping the farmland quota, in direct conflict with "the quota concept disappears as a whole" (A-62 rejection item ②).

- **N-97** Never write "the `.dat` file's mtime is unchanged" as an assertion before F-233.
  - Criterion: in reports between F-220 and F-233, `grep "mtime"` appears only in the explanatory sentence "this item is not an assertion".
  - Why: `settleOnRemoval` only stops reading it at F-233; asserting it early is necessarily red (A-69).

- **N-98** Never make the tier a data component, NBT or the vanilla `Rarity`.
  - Criterion: `data_components` is still 1; `grep -rn "DataComponent" src/main/java/com/dashpig/farmstorm/util/CropProfile.java` = 0; `grep -rn "Rarity"` = 0.
  - Why: tier is a profile-level static property; making it instance-level would require resolving the ambiguity in six places — merge / compare / stack / trade — and would turn `data_components` into 2 (D-150, document 34 §7 tombstone 2).

- **N-99** Never write a second 11-entry pool id list.
  - Criterion: inside the pool section `grep -c "CropProfile\."` = 0; after changing one `tier`, the pool's entry count changes with it automatically.
  - Why: a hand-written list would make "tier" and "pool" two sources of truth, and RT-49's defence line fails on the spot (A-63).

- **N-100** Never stop after blocking only one planting path.
  - Criterion: `ModifiedCropItem#isPlantableSoil` and `SeedItem#useOn` **each have their own independent acceptance** (not merged); the two acceptances appear in the report as a pair.
  - Why: the 7 standalone seeds go through the vanilla `BlockItem.useOn` and never pass through `isPlantableSoil` at all (D-152, RT-48, document 34 §7 tombstone 5).

- **N-101** Never register a menu for the cultivator.
  - Criterion: `FarmstormMenus` gets +1 only (`outpost_core`); `registry/FarmstormMenus.java`'s diff has only one line; `menu_types` = 2.
  - Why: one more registration makes 98, in direct conflict with `93 + 4 = 97` (D-160 supplement, D-171, document 34 §9 ⑥).

- **N-102** Never take 16 as the slot count, and never change 54 to anything else.
  - Criterion: `storageSlots` range `54–54`, `SLOT_COUNT = 54`; the menu's slot count and `getContainerSize()` come from the same source; in the report 16 and 54 each carry a qualifier (cultivator capacity / storage slot count).
  - Why: mixing the two numbers breaks the menu, the `getUpdateTag` size and the "core full ⇒ pause" criterion all at once (ruling #8, RT-44, document 34 §7 tombstone 3).

- **N-103** Never stuff the link into `core_list`.
  - Criterion: `CoreListPayload.java`'s diff is empty; the new packet is its own file + its own single `.playToClient`; the protocol version string is already `"2"`.
  - Why: a full snapshot and a range delta are two kinds of data; stuffing them together forces the debounce and the fingerprint to be rewritten entirely (D-169 supplement, document 34 §5.2).

- **N-104** Never draw the links with particles or entities.
  - Criterion: `grep -rn "DustParticleOptions\|ParticleTypes" client/CultivatorLinkRenderer.java` = 0; the renderer uses `LevelRenderer.renderLineBox`.
  - Why: a line dozens of blocks long would need dozens of particle packets and gets none of `RenderType`'s line width and depth handling; 0.1.1 already has the "48 particle packets per ring" precedent (document 34 §7 tombstones 6 / 7, RT-45).

- **N-105** Never let server logic depend on the link.
  - Criterion: binding / yield / migration / drops — all four places `grep -rn "CultivatorLink\|ClientCultivatorLinks"` = 0.
  - Why: the link is purely visual; the client not receiving a packet does not mean the binding does not exist (D-169 supplement).

- **N-106** Never rebind, drift or auto-claim at runtime.
  - Criterion: binding write points `grep -rn "setCore\|bind("` appear only in the two places, placement and migration; placing a core afterwards next to an unbound cultivator produces no binding.
  - Why: that writes happen at exactly two moments is the whole meaning of "is there a core when it is placed" (D-165 supplement, document 35 §6.8).

- **N-107** Never `setBlock` in migration, and never load a chunk for migration.
  - Criterion: inside `settleOnRemoval`'s method body `setBlock` = 0 and `getChunk(` = 0 (only `getChunkNow`); `minecraft:dirt` has 0 hits in that method body.
  - Why: the cultivator is a block of its own; when it cannot move it only needs to be marked unbound — touching the world is data loss (D-168, RT-43, document 35 B-20).

- **N-108** Never delete blocks, items, loot tables, recipes or menu classes.
  - Criterion: the 15 crop blocks / 22 crop items / 7 seeds / 26 loot tables / 29 recipes (26 `converting`) / `ConverterMenu` / `ConverterScreen` are all there; the `recipe` count is still 29.
  - Why: deleting a block turns it into air in an old save; recipes are the only carrier of the "vanilla crop ⇄ modified crop" mapping (D-155, D-170, the note under document 36 §3's table).

- **N-109** Never write trade back-fill / out-of-stock recalculation logic.
  - Criterion: `grep -rn "Offers\|repairOffers\|resendOffers" src/main/java` gives 0 hits (except comments); the 3 trade entries have only `maxUses` as their one count knob.
  - Why: going out of stock and restocking both go through the vanilla mechanic; a second timer is a second source of truth (D-158, document 36 §10 #16).

- **N-110** Never change an existing texture, and never re-run the icon generator.
  - Criterion: the 113 existing PNGs under `textures/item/` and `textures/block/` show 0 changes in `git status`; the only additions are the 5 `cultivator_a_*` PNGs + 2 mcmeta.
  - Why: this version touches no existing appearance; re-running the generator silently rewrites historical artifacts (pitfall 2, D-151, document 36 §5).

- **N-111** Never touch `docs/` or the 8 pre-existing defects registered in document 34 §8.
  - Criterion: `docs/` shows 0 changes in `git status`; the 8/5/2 at `CropType.java:17,19,21`, the "6 个" at `SeedItem.java:13`, the "14 个" at `FarmstormBlocks.java:25` and the "14 种池" at `LuckyBerryRoller.java:170` are not changed by a character.
  - Why: the baseline is 0.1.1's released state, and "fixing it on the way" mixes out-of-scope changes into this version (D-148, document 37 §0.4 item 2).

- **N-112** Never write "self-check OK" as an acceptance assertion inside a window.
  - Criterion: in the reports for F-224 ~ F-227 `grep "→ OK"` gives 0 hits (except F-228); inside a window every assertion gives the actual subtotal numbers.
  - Why: inside the window the assertion constant is still 93 while the actual value is already 96 / 97; writing OK is writing a criterion that is forever red (document 37 §0.3, HC-98).

- **N-113** Never cite the tool's `FAIL 0` before the tool has been synced.
  - Criterion: the report that contains "FAIL 0" also attaches `tools/verify_project.py`'s `git diff` (the four places: `EXPECTED` / `EXPECTED_COUNTS` / the config union / the print string).
  - Why: its expectation set comes from old documents and it checks only 93 objects; the extra 4 it does not look at at all (A-66, HC-120).

- **N-114** Never assume a signature on an API to verify, and never write an empty implementation "to make it compile".
  - Criterion: each of document 36 §9's 13 items has a "minimal-case command + result" record; before it is confirmed, do not fake the implementation with `return null`, do not comment the functionality out, and do not use `UnsupportedOperationException`.
  - Why: 4 of this version's 9 technical components are firsts for the whole project, and both the signature and the runtime behaviour have to be measured (document 36 §9, RT-45 ~ RT-47).

## 3. Fact Card

Only source facts that "get it wrong if not written" are collected. **A name without a `file:line` is never written** — inventing a class name from memory and putting it into code is a compile error.

| # | Fact | Value / current state (baseline = 0.1.1 released state) | Evidence |
|---|---|---|---|
| 1 | The 93-object registration surface's 12-class assertions are hard-coded in the startup self-check | `32 / 26 / 7 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1` = **93**; the five constants are in `total == 93`, `items == 32`, `blocks == 26`, `blockEntities == 7`, `menuTypes == 1` | `Farmstorm.java:147-150`; the log format string `:151-155` |
| 2 | The server config currently has **166 keys / 58 groups** (34 top-level + 24 nested) | counting method: `^\s*\[` counts sections, `^\s*[A-Za-z_].*=` counts keys | measured in `run/config/farmstorm-server.toml`; document 36 §1.3 / §4.1 |
| 3 | CLIENT config: **3 keys**, in another file | `hud.enabled = true` / `hud.x = 4` / `hud.y = 4`; **not counted in** 166 | `config/FarmstormClientConfig.java:29-43`; document 36 §4.2 |
| 4 | Language keys **323 × 2**, with equal Chinese and English key sets | zh = 323 / en = 323, zh-only = 0; of which `farmstorm.configuration` = 169 (= 166 server + 3 client) | `src/generated/resources/assets/farmstorm/lang/*.json`; document 36 §1.3 |
| 5 | Crop profiles: **22** entries, split into three tiers 11 / 7 / 4 in this version | `CropProfile.ALL` has 22 entries | D-149; `Farmstorm.java:112-115`; document 34 §4.1 |
| 6 | Blocks **26** = 15 crop blocks + 8 deployed blocks + 3 others | crops `:26-43`, deployed `:46-66`, others (`converter` / `compressed_hay_block` / `outpost_core`) `:69,73,82` | `registry/FarmstormBlocks.java` |
| 7 | Deployed **blocks**: 8; deployed **items**: only 7 | `vibration_bean`'s block goes through `deployed(...)`, but its item is a `ThrowableCropItem` ⇒ do not count 8 when counting items | document 36 §2.1 |
| 8 | The two farmland classes are **retained and have no runtime caller** | `FarmlandLedger.java` **555** lines / `FarmlandGuard.java` **225** lines (counted by line breaks) | note 2 under document 34 §3.3's table; document 36 §2.3 |
| 9 | The core BE is currently **not a container** | `extends BlockEntity`; its NBT has only the three keys `ownerUuid` / `ownerName` / `placedGameTime`; no `getUpdateTag` / `getUpdatePacket` | `OutpostCoreBlockEntity.java:48`, `:411-424` |
| 10 | The converter BE is a **2-slot** container (input + catalyst), while the menu has 3 slots (one more for the preview) | `SLOT_INPUT = 0` / `SLOT_CATALYST = 1` / `SLOT_COUNT = 2` | `ConverterBlockEntity.java:39-42`; `ConverterMenu.java:72-84` |
| 11 | The anvil's property lines | `.strength(5.0F, 1200.0F)` at `:3053`; `MapColor.METAL` / `requiresCorrectToolForDrops` / `SoundType.ANVIL` / `PushReaction.BLOCK` at `:3051-3055` | `.scratch/p22c/src/Blocks.java:3047-3057` |
| 12 | The texture is a **32×512 strip of 16 frames** + a 32×32 bottom face, with `frametime 2` | one cycle `16 × 2 = 32` ticks = 1.6 seconds; only 9 frames differ on top (1–7 and 9–15 repeat as a palindrome) | `BRIEF` §1.8; document 36 §5; RT-47 |
| 13 | The texture pack is **outside the repository** | `C:\Users\DASHpig\Downloads\cultivator_a_animation_pack`: 40 files = 37 PNG + 2 mcmeta + 1 README; the static first frames come from `editable_frames/side_00.png` / `top_00.png` | `BRIEF` §1.8; document 34 §8 item 1 |
| 14 | Network packets: **1** at present | `farmstorm:core_list` (S2C read-only, `MAX_CORES = 64`), with its only registration point at `FarmstormNetwork.java:29-31` | `CoreListPayload.java:23,25,28`; document 36 §1.4 |
| 15 | Mixin: **0** | the whole repository has 0 hits for `Mixin` / `mixin`; no `*.mixins.json`; `build.gradle` has no mixin plugin | document 36 §1.4; `BRIEF` §0 |
| 16 | The vanilla / NeoForge sources are in `.tools`' gradle cache | `neoforge-21.1.213-sources.jar` (1.7 MB) and `sourcesAndCompiledWithNeoForge_..._output.jar` (30.9 MB); loose file copies are under `.scratch/p22c/src/` | `.tools\gradle-home\caches\` (measured) |
| 17 | Assets and datagen as they stand | PNG **113** (block 79 / item 29 / mob_effect 2 / misc 2 / gui 1); `*.png.mcmeta` **0**; blockstates 26 / models 103 / loot_table 26 / recipe **29** (26 `converting`) | measured; document 36 §1.3 / §5 |
| 18 | `CropProfile`'s construction surface | **7** `new CropProfile(...)` (`:58,87,92,97,102,117,125`) + **3** private factories (`:191-207`) + 15 profiles built through the factories = 22; 15 factory arguments | document 36 §2.1; `CropProfile.java` |
| 19 | The farmland `.dat` file's on-disk name and format | `DATA_NAME = "farmstorm_farmland"` at **`:53`** (not `:44`); `dataVersion: 2`; three tables `bindings` / `byCore` / `orphans` | `FarmlandLedger.java:53,55`; A-68; document 36 §6① |

> Two footnotes: ① `run/config/farmstorm-server.toml`'s nested sections are **indented**, so counting sections must use `^\s*\[`; `^\[` only counts the top level (36 sections after the change). ② the core BE's NBT lives in the **chunk save**, not in `farmstorm_outpost_core.dat`; the two names look alike but their locations are entirely different (document 36 §6 ②③).

## 4. The Pitfall Table (31 entries)

Items **1 ~ 20** are inherited from document 32 (the 0.1.1 version), not one deleted; where their numbers have been rewritten by this version (action surface 44 → 46, acceptances 38 → 36, language keys 323 → 337, etc.) they are written to this version's standard. **Item 20 is a meta-rule** — what it constrains is not the code but the criterion itself, and its standing differs from every other item. Items **21 ~ 31** are new in this version, and each traces to an adjudication (A-62 ~ A-69) or to a technical fact in document 36.

| # | Pitfall (how the implementer steps on it) | What happens if stepped on | Correct approach | Basis |
|---|---|---|---|---|
| 1 | Changing a constant at only one reader (0.1.1's `TurretBullet.SPEED` has two readers) | compile failure, or two speeds coexisting, muzzle and flight | `grep -rn` the whole repository for readers before touching a constant; this version's counterpart is the field at `FarmstormConfig.java:227` (its two readers are in the retained classes, and A-62 has ruled they do not move) | A-30; document 36 §2.3 |
| 2 | Changing a texture only halfway (the output changed, the generator or the assertion did not) | a re-run of the generator silently restores it; or the generator throws `SystemExit` and the whole round aborts | if you change it, change the three places in the same version; **this version changes no existing texture ⇒ do not re-run the icon generator** | A-32 / D-126; document 36 §5 |
| 3 | Using `setChunkForced` or `TicketController#forceChunk` to save trouble | both write `world/data/chunks.dat`, so the chunk is still force-loaded after the server stops | a bare `TicketType` + `addRegionTicket`, with the release in the same place as `release()`; this version does not touch the ticket layer | A-38; document 36 §6② |
| 4 | Taking the iteration order of `byOwner` / the binding Map / List as the order | the same save started twice gives different results (the outer layer is a `HashMap`) | sort the candidate set explicitly: `distSqr` → `placedGameTime` → coordinates; before migrating, `sort(comparing(Binding::pos))` | A-39 / A-47; `OutpostCoreBlockEntity.java:356` |
| 5 | Hard-coding a config default as a constant (0.1.1's was a 200-tick field length) | after changing the config, the behaviour contradicts the configuration | this version's 600 / 54 / 16 / 2 **all come from the config only**, and no second default appears in the code | A-33; D-163 / D-165 / D-166 |
| 6 | Writing only the tick face, but asserting the load face in acceptance ("only 1 chunk is loaded") | the ticket level spills over, so a ring of border loading is unavoidable ⇒ the assertion is necessarily false | do not assert the load face; this version's migration stretch asserts only "no loading with `getChunk`" (`getChunkNow` only) | A-43; document 36 §2.7 |
| 7 | Adding a parameter the signature does not have to a determination function (0.1.1's `protectsFromTrample`) | it ripples into existing call sites, and the event itself carries no player | do not add parameters to a criterion whose signature does not move; this version's counterpart is the gate for opening the core — **use the vanilla `stillValid` semantics, set no new radius / add no parameter** | A-45; ruling #17 |
| 8 | Trusting the tool's green light right after the change | the expectation set comes from old documents ⇒ a green light can only come from a missed check | sync the tool first, then cite `FAIL 0`; before that, report only the sectional readings | A-40; A-66 |
| 9 | Treating one group of behaviours as two categories (0.1.1 treated `SoundType` as break + place) | the acceptance is two or three items short, and players notice immediately | for each property change, enumerate **all** its readers (five getters / five entry points) and verify them one by one | A-31 |
| 10 | Squeezing an overflow item into a fixed row count (`+N` squeezed into 8 rows) | the layout and the acceptance count no longer agree, and you have to pick one | add an **extra** row for the overflow, do not squeeze; this version's fixed-count surface is the screen's 54 + 36 = 90 slots, and `quickMoveStack` is written in two stretches | A-34; document 36 §9 ⑧ |
| 11 | Writing to an adjudication that is void / superseded | the standard you write does not match the acceptance, and cannot be looked up | check the status column before citing: A-36 / A-44 are void and A-47 is rev.2; this version's new A-62 ~ A-69 are all current | A-36 / A-44 / A-47; document 37 §5 |
| 12 | Writing "this method has only one call site in the whole repository" into the acceptance | it is necessarily red after implementation, and you cannot tell a defect from the expectation | switch to a countable quantity: file count / key count / hit count / log line / save field | A-37; document 37 §0.4 item 4 |
| 13 | Writing a separate branch for an "unloaded block" and calling `getChunk` on the way | dead code + a forced load that cascades a batch of chunks | use `getChunkNow` only; when it cannot be obtained, take the defensive exit (keep the original values, do not guess, do not delete) | A-35 / A-60; D-168 exit ③ |
| 14 | Letting two layouts / two decompositions coexist | two people compute two different numbers and acceptance has to pick one | recognise only one set of dimensions: 54 = 6 × 9, screen 176 × 222, reuse the vanilla `generic_54.png` | A-41; document 36 §2.6.3 |
| 15 | Stopping at the previous version's action-surface row count (0.1.1 was 44 rows) | a whole row of changes is missed (what the previous version missed was "delete the whole config group") | this version is **46 rows** (document 36 §3) **+ 4 items outside the table**; match row by row, do not count only your own task | A-42; document 36 §3; document 37 table A |
| 16 | Taking the "number of acceptances" and the "number of boundaries" as a one-to-one correspondence (36 vs 40) | wrongly assuming "no assertion = no boundary", so the boundary list is quietly cut | boundaries follow document 35 §12 (**40** entries); document 36 §8's **36** acceptances are **not required** to correspond one-to-one; F-245 covers all 40 | A-46; document 37 appendix A |
| 17 | Missing one category when counting language keys (0.1.1 missed 2 `key.farmstorm.*` entries) | the total is off by N keys, and the Chinese and English key sets end up unequal | add and subtract bucket by bucket by prefix (`323 − 3 + 17 = 337`), and after datagen use `ConvertFrom-Json` to count the key set | A-48; document 36 §4.3.3 |
| 18 | "Fixing" a registered known limitation on the way | triggers a second save-format bump and introduces a non-reproducible surface | A-49's cross-dimension collision and document 34 §8's 8 items are **all left unfixed**; cite "known limitations" in the report | A-49; document 34 §8 |
| 19 | Counting the toml's sections with `^\[` | nested sections are indented ⇒ it only counts the top level, misjudged as "the group count is wrong" | count sections with `^\s*\[` (**60** after the change), and count key lines separately with `^\s*[A-Za-z_].*=` (**175**) | document 36 §4.1.2 |
| 20 | **The criterion's own failure (meta-rule)**: before any grep / `git diff` / counting criterion goes live, **run it against the current state first** — it should already be 0 or some number now; first confirm that this number can be computed | `^\[` missing the indented sections, a grep missing the `src/main/java/` prefix hitting 0 forever, a `git diff` with a wrong pathspec always being empty: all three are "the criterion always passes or always fails", more insidious than a wrong number | run every criterion once **before** the change and report the measured value, then once after; only when both can be computed does the criterion stand. **This item outranks every other row of this table**: every other row is an instance of it | A-52 / A-57 / A-58 and the other rows of this table |
| 21 | **Tool false green**: 97 objects, but the tool checks only 93 and exits 0 | `已核 registry 对象: 93 / 93`, exit code 0, `FAIL 0` — everything looks right, and the 4 extra objects it **does not look at at all** | first do F-243's four-place sync (`EXPECTED` / `EXPECTED_COUNTS` / the config union / the print string), then cite the tool's conclusion; the citation must attach its `git diff` | A-66; HC-120; document 36 §1.2 |
| 22 | **In-between window**: writing "self-check OK" as an assertion between F-224 and F-228 | inside the window the actual value is 96 / 97 while the assertion constant is still 93 ⇒ that assertion is red the moment it is written; writing "OK" means writing a criterion that always fails | inside a window assert only the **actual subtotal numbers in the FAIL line** (96 / 97 and `menu_types 1 → 2`); the only one that writes OK is the window-closing task F-228. The language-key window F-215 ~ F-241 and the config window F-220 ~ F-242 work the same way, with totals asserted only in the closing task | the same spirit as A-69; document 37 §0.3; HC-98 |
| 23 | Blocking only one of the **two planting paths** (changing only `isPlantableSoil`) | the 7 standalone seeds can still grow crops on farmland, and players can keep planting 7 kinds | `ModifiedCropItem#isPlantableSoil` always `false` **and** `SeedItem#useOn` returning `PASS`, each done once, with the acceptances **split into two and not merged** | D-152; RT-48; document 36 §2.2 |
| 24 | **The third entry**: thinking a deployable right-click on farmland will "do nothing" | in fact it **places the turret / trap / fungus** (`DeployableCropItem#useOn` falls back to `super.useOn` once its criterion is always `false`) ⇒ writing "can place, cannot plant" as "right-click is entirely dead", and the acceptance is necessarily wrong | write the acceptance as "deployable right-click on farmland = places the deployed form, plants no crop; right-click on the top face of non-farmland = places normally" | D-152 supplement; document 35 §12 B-02 |
| 25 | Missing a construction point: `new CropProfile(...)` has 7 direct constructions | the record gained a 9th field, and missing one point means **it will not compile** (which is good: it cannot drift silently); what gets missed is a place where a `CropTier.` argument "should have been added but was not" | go through document 36 §2.1's list point by point: 7 direct constructions (`:58,87,92,97,102,117,125`) + 3 private factories + 15 factory arguments | D-150; document 36 §2.1; `CropProfile.java` |
| 26 | `usesBlockModel` does not add `CultivatorBlock` | `ModItemModelProvider` falls into the `basicItem(item)` branch and asks for a **non-existent** `textures/item/cultivator.png` ⇒ a purple-black square in game | `ModItemModelProvider.usesBlockModel` (`:57-59`) must add `\|\| block instanceof CultivatorBlock` | document 36 §5 connected change 1; document 36 §10 #10 |
| 27 | Thinking code can "pause" an mcmeta animation | the animation is **texture-level**: the same image animates wherever any model references it, and the code layer has no pause switch ⇒ "idle static" can never be done, and players see it flipping while idle | two models + two textures: `working=false` references the `_still` static first frame, `working=true` references the frame strip; `working` goes through a blockstate property, not BE sync | D-161 supplement; document 35 §6.5; RT-47 |
| 28 | **D-154's timing**: thinking that after commenting out those call sites the `.dat` "is no longer read or written" | before F-233 `settleOnRemoval` (`FarmlandLedger.of` at `OutpostCoreBlockEntity.java:353`) still reads and rewrites that file ⇒ the "mtime unchanged" assertion is necessarily red if written early | split the criterion in two: before F-233 write only "the call sites are commented out; this item is not an assertion", and only after F-233 verify the file is still there / the mtime is unchanged | A-69; D-154; HC-118 |
| 29 | Writing the pool's 11 entries as a hand-written id list | changing one `tier` does not change the pool ⇒ "tier" and "pool" become two sources of truth, and RT-49's defence line fails | the pool = a `tier() == COMMON` filter, and a second 11-entry id list **must not** appear | A-63; D-156; HC-107 |
| 30 | The cultivator does not go into the creative tab | this version has **no** cultivator recipe, and `creativeTabItems()` is an explicit list ⇒ it is registered but **nobody can obtain it** (survival has no acquisition path either) | `FarmstormItems.creativeTabItems()` must add `CULTIVATOR`; the acceptance looks for one more item at the end of the creative tab | A-64; the note under document 36 §5's table |
| 31 | The two `.cmd` files' jar names are not changed in sync | "一键构建"'s `copy` copies a **non-existent file**, and "检查环境" directly reports `jar: 缺失` — the build looks successful while the artifact never reaches `dist/` | change `一键构建.cmd:11-12` and `检查环境.cmd:14` to `farmstorm-0.2.0+mc1.21.1[.jar\|-sources.jar]` (3 places) in the same pass | A-65; D-148; HC-103 |

> Landing method: write each criterion's "measured value before the change" into the task report, and report again after the change; a "passed" without a number is treated as unverified (item 20).

## 5. The Boundary Between Fact and Inference

**What must be checked against the source** (if you cannot find it, you may not write it into code, comments or a report's assertion):

| Category | Where to look | If you cannot find it |
|---|---|---|
| Any `file:line` | use `Select-String` / read the file on the spot to confirm what that line **is now** | do not write a comment from a citation in document 34 / 36 — line numbers drift (that is exactly how A-68 happened) |
| API signatures and runtime behaviour | `neoforge-21.1.213-sources.jar` / `sourcesAndCompiledWithNeoForge_..._output.jar` in the `.tools` cache; each of document 36 §9's 13 items has "minimal case + way out" | write the minimal case and get it running first, then touch the main line (N-114) |
| Current counts | count them yourself: `run/config/*.toml`, the two lang files, `src/generated`, `Farmstorm.java`'s assertions | do not copy a number from `BRIEF` / document 36 §1 as "the value now" |
| Save structure | after stopping the server, read the region / `world/data/`, or `/data get block <pos>` | do not guess field names |

**What may be cited from documents**: the conclusions of D numbers and A numbers, document 35 §6 / §12's behaviour standards, document 36's class design and fallbacks, document 37's F numbers and dependencies. Every citation carries a number (`D-165`, `A-66`, `36 §2.7`, `F-233`).

**Three disciplines**:

1. **A number may come from only three places**: `BRIEF` / document 34's D entries, a measurement at a `file:line`, or arithmetic on two values already given (e.g. `93 + 4 = 97`). If you need a new value, put it into "open questions"; you may not decide it yourself.
2. **When unsure, write it into "open questions"**; do not fill the gap with "probably / usually / how the industry does it", and do not use "leave it for now, change it later" as a transition (it leaves an irreversible trace in the save).
3. **Inference must be marked**: in code comments, anything "not written in the documents but inferred by me" starts with "推断：" (Inference:) and gives the reasoning basis; the same in reports. An unmarked inference will be cited as fact by the next person.

## 6. Handling Conflicts

**Three iron rules**: ① a conflict between documents ⇒ **document 34 wins** (it describes itself as the single source of truth; 35 / 36 / 37 only refine it); ② a conflict between source and document ⇒ **report it**, write it into the report's "open questions", with the `file:line` and the measured value; ③ **do not pick a winner yourself** — do not implement whichever version you like, and do not write back into 34 / 35 / 36 / 37 (they are frozen).

### 6.1 Known Conflicts (10 rows; read this table before anything else)

| # | Conflict | Handling |
|---|---|---|
| 1 | document 34's D-154 and §3.3 row 7 write the `.dat` on-disk name as `FarmlandLedger.java:44` | it is actually **`:53`** (`:44` is the javadoc about dimensions); write `:53` per A-68 |
| 2 | document 34's §2 note 3, transcribing ruling #3, writes "**3 listeners**" | it is actually **2 listeners + 1 backfill line**; `onServerTickPost` itself must be kept (document 36 §10 #6, document 34 §9 ⑦) |
| 3 | document 34's D-153 requires commenting out the **read** of `farmlandBonus`, and at the same time requires the two classes **not to change by a single line** | the two reads are inside those two classes ⇒ mutually exclusive; **A-62** has ruled: comment out the definition only, keep the field declaration, leave the two `.get()` calls alone |
| 4 | document 34's D-160 writes the landing location as "textures/block/ (**5 files**)" | the texture pack's 5 files = 3 PNG + 2 mcmeta; this version's **addition** is **5 PNG + 2 mcmeta = 7 files** (plus the 2 static first frames, D-161). The two "5"s are not the same thing |
| 5 | document 34's §9 ⑤ ~ ⑨ say `BRIEF` §3.3 / §3.5 / §3.6 / §2 have gaps | `BRIEF` has been revised on those reports ⇒ those few **reports are now out of date**; write per the current `BRIEF` and document 36 (document 36 §10 #2 ~ #5) |
| 6 | document 36's §10 #13 says "document 35 §8.3 has only one gate and B-24 is out of date" | document 35 §8.3 has been changed to **two gates** (allied ∧ the vanilla `stillValid`), and B-23 is written to match ⇒ that item in document 36 is stale; write per document 35's current text and ruling #17 |
| 7 | document 36's §10 #15 says "the D number is to be registered later by document 37 after A-62" | document 37's **A-67** rules that no D number is registered: always write the double citation "ruling #17 + D-166", the "ruling + decision" form |
| 8 | `plan0.2.0.txt:10-12` writes 4 Rare; document 34 §4.1 has **7** Rare | document 34 wins (the extra 3 were added in the interview by ruling #2); the plan's 4 is the old standard and must not be rolled back |
| 9 | document 35 §6.2 says **23** items can be inserted; D-162 says **18** crops | both standards are right: **18 crops + 5 standalone seeds inside the admission surface = 23 items**; when writing "18" the qualifier is "crops" |
| 10 | document 30 §1.4 writes "166 keys / 59 groups"; document 36 measures 58 groups | "59" is the **pre-change baseline** (0.1.0, when the `farmland` group still existed), and the same file's post-change value is 58; this version's post-change value is **60** (document 36 §10 #1) |

### 6.2 What to Do When a New Conflict Appears

Write it into the report's "open questions", with all four parts: ① each side's `file:line` or number; ② each side's exact words; ③ which side you are **temporarily** writing to (you may only choose the "document 34 / an existing adjudication" side, or leave it untouched for now); ④ who has to decide. **Do not** change a document before implementing, and do not implement both sides.

### 6.3 The 0.1.1 Hard Constraints Superseded by This Version (8 rows, declared explicitly so you do not copy the old ones)

| Document 32's entry | What it said at the time | This version |
|---|---|---|
| HC-77 | registration surface 93, 12 class subtotals | **97** (`33/27/8/…/2`), see HC-97 |
| HC-78 / HC-79 | config 166 keys / 58 groups; CLIENT 3 keys | **175 / 60**; CLIENT 3 keys unchanged (HC-99 / HC-100) |
| HC-80 | language keys 323 | **337** (HC-101) |
| HC-81 / HC-84 / HC-85 | farmland bound to a core, 16 blocks per core, quota gate, trample protection | **retired as a whole** (D-153); "16" now means the cultivator capacity (D-165) |
| HC-83 | startup backfill of old saves (`backfill`) | **retired** (comment `:171`; the ledger no longer calls `get()`) |
| HC-86 | the three branches on core removal (including `setBlock(DIRT)`) | **changed to the cultivator's three exits**, with no turning to dirt (HC-113) |
| HC-87 | exactly 1 custom packet | **2** (`core_list` + the link packet), with no ceiling (HC-115) |
| HC-95 | version number 0.1.1, both `.cmd` files write 0.1.1 | **0.2.0**, changed in three places together (HC-103) |

## 7. Report Format

Every report has four fixed sections, **in this order; a missing section means the task is unfinished**:

```
## Conclusion
One sentence: what you did, and which numbers it maps to (F number / D number / document 36 §3 row number / acceptance number).
Writing "done", "normal" or "no problem" is forbidden — that is not a conclusion.

## Evidence
Three lines per criterion: the command as written → the key output (one to three lines verbatim) → the number (HC-xx / N-xx / 36 §8 #n / 35 §12 B-nn).
For counting criteria give both numbers, "measured before the change / measured after the change" (pitfall 20).
For the change surface give the file list from `git diff --stat` (one task, one commit; one extra file means out of scope).
For save-related criteria give the key excerpt from `/data get block <pos>` or the file reading after the server stops.

## Open Questions
What you are unsure about, what conflicts between documents, what needs the author to decide. If there is none, write "none" — this section may not be omitted.

## Next Step
Per document 37's dependency graph, the number of the next task to do; plus problems you have **not** fixed this round but have already seen (register only, do not act).
```

Three constraints:

1. **Evidence must be observable**: log lines, command readings, `git diff`, `jar tf`, save NBT, in-game readings. For a log longer than 20 lines keep only the **first error** and the **summary line**.
2. **Every assertion must land on a number**: an assertion without a number is treated as "unverified"; one that has a number but no reading is treated as "not run".
3. **Do not paste file contents only**: `git diff` shows only the changed lines, not the whole file; a log shows only the matching lines. The report is for the next person to make a decision, not an archive.

## 8. Pre-Start Confirmation Checklist

Tick every line before starting a task; if any line cannot be ticked, resolve it first and do not start:

- [ ] **1** I confirm the baseline is **0.1.1's released state** (`gradle.properties:11` currently reads `mod_version=0.1.1`), and this version changes it to **0.2.0**; after the change this is the only place that declares the version.
- [ ] **2** I can say which numbers this task lands on: the F number, the D number, document 36 §3's action-surface row number, document 36 §8's acceptance number / document 35 §12's boundary number.
- [ ] **3** I know **which window** I am in (registration F-224 → F-228 / language keys F-215 → F-241 / config F-220 → F-242), and inside a window I write neither "self-check OK" nor a total (HC-98).
- [ ] **4** Every API I am going to touch — has it appeared among document 36 §9's 13 items? If so, run the minimal case first and record the way out in the report (N-114).
- [ ] **5** For every criterion this round I have run it once **before** the change and recorded the measured value (pitfall 20, the meta-rule).
- [ ] **6** I confirm this round **does not touch**: `docs/`, document 34 §8's 8 pre-existing defects, `FarmlandLedger` / `FarmlandGuard` (not one line), the 113 existing textures (N-110 / N-111 / N-94).
- [ ] **7** I confirm the run directories are isolated (`-PfsRunDir=`); this version runs `runServer` (verifying binding / the container) and `runClient` (verifying the overlay / the link) **at the same time**, and colliding directories overwrite each other's logs.
- [ ] **8** I confirm whether `tools/verify_project.py` has been synced to 97 / 175 / 337; if not, I **do not cite** `FAIL 0` (A-66, HC-120).
- [ ] **9** I have read the eight adjudications A-62 ~ A-69 and can say which ones constrain this task (A-62 keep the field, A-63 derive the pool, A-64 the creative tab, A-65 the script names, A-66 the tool, A-67 register no D number, A-68 `:53`, A-69 the timing).
- [ ] **10** I confirm this round commits the changes of **one** task only, and the commit message starts with the task number (`F-224: ...`); rolling back this one commit does not invalidate other tasks.
- [ ] **11** I have ready the two numbers "measured before the change / measured after the change", plus at least one log line or reading that can go into the report.
- [ ] **12** I know where the open questions that may arise this round should be registered (the report's 3rd section), and which kinds I may not decide myself (new numbers, standard conflicts, upstream contradictions).
