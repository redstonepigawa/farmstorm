# 35 · v0.2.0 Design Delta

> Project: Farmstorm · Version: **v0.2.0** · Basis: `docs/zh/34-DECISIONS-0.2.0.md` (D-148 ~ D-172, 25 in total) + `.work/v020-BRIEF.md` (the fact card) + `plan0.2.0.txt` (35 lines, **4 original plan items**, split by this document into **11 requirement items R1~R11**; the split measure is in §1) · Relation to 0.1.1: a **delta**; it neither changes `docs/zh/29-DESIGN-0.1.1.md` nor restates it.

| Item | Value |
|---|---|
| Document | 35-DESIGN-0.2.0 |
| Status | Design final (the last version before the six Chinese documents are frozen) |
| Upstream (single source of truth) | `34-DECISIONS-0.2.0.md`: the measure is taken from it alone; this document is responsible only for "what the requirement looks like" |
| Upstream (the author's original text) | `plan0.2.0.txt`: the 4 original plan items are at `:7-34`; `:1-5` is the header, in which the version number is a stale value (see Appendix A row 1) |
| Upstream (factual base) | `.work/v020-BRIEF.md`: §0 baseline, §1 source-code facts (with `file:line`), §2 the author's 16 rulings, §3 the increment list, §4 risks RT-42~RT-50 |
| Downstream | `36-TECH-0.2.0.md` (class design, the full config table, packet codec, NBT structure), `37-TASKS-0.2.0.md` (tasks and scheduling) |
| Language | Body text in Chinese; code identifiers, registry names and config keys always in English |

---

## 0. Reading conventions

### 0.1 The division of labour between this document and 34

34 fixes the **measure** (a one-sentence conclusion + grounds + landing position + knock-on items); this document writes **what it looks like** (range, format, sequence, numbers, boundaries, and what the player sees). Four rules of the division:

1. **The measure is not invented here.** Every conclusion in this document has a decision number (`D-1xx`) behind it; if a reader finds a sentence that cannot be traced back to a decision number and is not a derivation marked [author's supplement], that is a defect of this document, not a blank that may be filled in by one's own interpretation.
2. **34's body text is not restated.** This document cites numbers rather than copying conclusion sentences; where the reader should "read item so-and-so of 34 first", the number is written out directly.
3. **What is not here**: the full config table (36), class design / packet codec / NBT structure / the save's binary format (36), task numbers and scheduling (37). This document names config keys and defaults only in the "Basis" column.
4. **Conflicts with the source are reported to the author.** Where, during writing, a place was found where 34 or the fact card does not match the source code, it is listed item by item in the hand-over notes and **the upstream is not changed**; this document is written per 34, and leaves a "per 34's measure" note in the corresponding subsection.

### 0.2 The test for "[author's supplement]"

| Type | May be marked "[author's supplement]" | Explanation |
|---|---|---|
| Arithmetic | yes | a third number computed from two given numbers; the expression must be written out (e.g. `19 − 8 = 11`, `11 + 7 + 4 = 22`) |
| Explicitly authorised by 34 | yes | 34 says "fixed by 35": how many a trade gives per deal, which one item a trade item sells, what a seed produces when put into the cultivator |
| A reading the upstream does not cover | yes | it must come with "why it can only be read this way", and be registered in §12's boundary list |
| Given by the fact card, not yet ruled on by the author | marked "[card supplement (vetoable)]" | e.g. the 600-tick cultivation cycle, the link being sent only inside the 3 × 3 chunks, the binding's tie-break key |
| Conflicting with 34 or the fact card | **not allowed** | a conflict can only be reported back; one may not pick one side and write it in |
| Aesthetic preference | **not allowed** | "I think it looks good" is not a reason; a decidable criterion must be given |

A number in this document has only four legitimate sources: it is written in 34, it is written in the fact card, it is arithmetic with the expression written out, or it is marked [author's supplement] with a reason given. Anything that is none of the four is a document defect.

### 0.3 Markup

| Markup | Meaning |
|---|---|
| [D-1xx] | the conclusion comes from the corresponding decision in 34 |
| [card §1.x] | the conclusion comes from the corresponding subsection of the fact card |
| [`path:line`] | a line taken directly from the source code; paths are relative to `src/main/java/com/dashpig/farmstorm/` (resources and tools are noted separately) |
| [author's supplement] | derived by this document under the six tests of 0.2 |
| [card supplement] | an item noted under the table in fact card §2 as "not the author's ruling, vetoable" |

**Deviation from the Chinese edition (intentional)**: the **English edition** writes the markers above as English bracketed tags (`[author's supplement]`, `[card §1.x]` and so on) and does **not** use `【】`; the Chinese edition keeps `【】`, the two editions' markers correspond one to one and mean the same thing, and this is not a translation omission.

### 0.4 Units and counting conventions

1. **Time** is measured in game ticks (tick), **1 second = 20 ticks**; **length** in blocks (block); **range** in chunks: the influence range of an allied core = the **3 × 3 chunks** centred on the chunk the core is in (Chebyshev distance ≤ 1, [`OutpostCoreBlockEntity.java:53,151-156`]), side length `16 × 3 = 48` blocks.
2. **"22"** always means the number of crop **profiles** (`CropProfile.ALL`), **"15"** the number of crop blocks, **"7"** the number of standalone seed items, and **"8"** the number of deployable blocks. The four numbers are never converted into one another, nor derived from one another.
3. **"Tier"** is a static, profile-level property (D-150), and **"stack tier"** is the existing constant of 16 for throwables / 64 for everything else ([`CropProfile.java:43,45`]); the two are unrelated (§2.6).
4. **"Allied"** follows 28's D-138 definition (the core owner + same-team teammates, judged by UUID / name and regardless of online status), which 34 §6 explicitly leaves **not superseded**; the three places this document uses it are cultivator binding, link dispatch, and the core storage's access check.

### 0.5 The 25 decisions → this document's sections

This table is a **reverse index**: from any one D number a reader can find "where it lands in this document, and as what". The 25 run consecutively with no gaps, and every one of them has a landing place.

| Decision | Topic | Where it lands here |
|---|---|---|
| D-148 | version 0.2.0 | header, the §1 table, Appendix A row 1 |
| D-149 | the three tiers and the 22 memberships | §2.2 (named one by one), §2.4 (the derived sets) |
| D-150 | the tier's carrier (a 9th field + an enum) | §2.1, the end of §2.2, §2.6 |
| D-151 | name color plus one tooltip line | §2.5, §2.6, §11 row 18 |
| D-152 | both sowing paths closed | §3.2, §3.4, §3.5 |
| D-153 | retiring farmland = commenting out call sites only | §7.2 (the seven landing points), §7.3, §7.4 |
| D-154 | the `.dat` is not read, written or deleted | §7.3, §7.5 |
| D-155 | blocks and items keep their registration | §3.3, §2.6, §3.5 |
| D-156 | the Lucky Berry pool 19 → 11 | §5.2, §5.3, §5.4 |
| D-157 | 3 trade items, unlocked in order | §4.2, §4.4 |
| D-158 | "once" = out of stock + restock | §4.5, §4.6 |
| D-159 | the price bands 2–5 / 10–32 / 40–64 | §4.2, §4.3 |
| D-160 | the cultivator block itself | §6.1, §6.5, §6.6 |
| D-161 | the animation plays only while working | §6.5 |
| D-162 | admission: Common + Rare, Epic excluded | §6.2, §6.8 |
| D-163 | 600 ticks, 2 crops + 2 seeds | §6.3, §6.4 |
| D-164 | pause when the core is full | §6.3 step 4b, §6.8 |
| D-165 | binding capacity 16, the single truth on the core side | §8.2, §6.8 |
| D-166 | a 54-slot container + allied ∧ within reach + legacy compatibility | §8.3 |
| D-167 | breaking drops | §8.4 |
| D-168 | cultivator migration when the core is broken | §8.5, §6.8 |
| D-169 | the link: a client wireframe + a new packet + range-filtered dispatch | §8.6 |
| D-170 | the converter's function deactivated | §9.2 |
| D-171 | the registry surface +4 ⇒ 97 | §9.3, §10 rows 50–51 |
| D-172 | red lines: no Mixin, one new packet | §9.3, §10 rows 56–57 |

One reading rule: **D numbers appear only in this table and in section heading lines; in body text they are always written out in full** ("D-163 supplementary item" rather than "item 163"), so that cross-document searching does not confuse them with task numbers or test numbers.

---

## 1. Overview of the requirements (11, one table)

| No. | Topic | Type | Decision | plan line | One-line outcome |
|---|---|---|---|---|---|
| R1 | tiering | addition | D-149, D-150, D-151 | `:9-12` | each of the 22 crops gets exactly one tier (Common 11 / Rare 7 / Epic 4), item names are colored by tier plus one tooltip line, and everything else is unchanged |
| R2 | crops are no longer planted | removal (deactivation) | D-152, D-155 | `:13` | both sowing paths are cut at once, the 15 crop blocks stay registered as before, but the player has no route left to put them into the world |
| R3 | farmer trading | addition | D-157, D-158, D-159 | `:14` | 3 trade items unlock in tier order, emeralds 2–5 / 10–32 / 40–64, `maxUses = 1` goes out of stock and waits for the vanilla restock |
| R4 | the Lucky Berry pool narrows | change to existing | D-156 | `:15` | the pool goes from 19 entries to 11, exactly the whole Common tier; the startup self-check's assertion value changes with it |
| R5 | the new cultivator | addition | D-160, D-161, D-162, D-163, D-164 | `:19-22` | a new block with an anvil's hardness, admission through four gates, 600 ticks yielding 2 crops (+2 seeds, only 5 crops have them), output into the core, pause when the core is full, animation only while working |
| R6 | retire the farmland system | removal (deactivation) | D-153, D-154 | `:26` | only 2 listeners and 1 startup backfill line are commented out, the two classes and that `.dat` are kept as they are, and no farmland rule is in force at runtime |
| R7 | the core binds cultivators (capacity 16) | change to existing | D-165 | `:27` | the binding target changes from farmland to cultivators, at most 16 per core, and 0 or 1 core per cultivator |
| R8 | core storage (54 slots) and breaking drops | change to existing | D-166, D-167 | `:28`, `:30` | the core becomes a 54-slot double-chest container that allies can open within the vanilla reach; when it is really broken the whole container drops |
| R9 | link rendering | addition | D-169 | `:29` | a new 2nd S2C read-only packet + a client wireframe, sent only while the player is inside that core's 3 × 3 chunks |
| R10 | the converter is deactivated | removal (deactivation) | D-170 | `:33` | right-click returns `PASS` (no reaction); the block, the 26 `converting` recipes and the creative-tab entry are all kept |
| R11 | the registry surface and the red lines | addition (close-out) | D-171, D-172 | — (a close-out item) | the registry surface `93 + 4 = 97`; Mixin is still 0, custom packets total 2 and are registered one by one |

**How 4 original items become 11 requirement items**: plan item 1 → **R1 / R2 / R3 / R4** (tiering, no planting, trading and the berry pool are four distinct acceptance faces); item 2 → **R5**; item 3 → **R6 / R7 / R8 / R9** (retiring farmland, the binding's new target, the container and its drops, and the link are four things that can be reproduced separately); item 4 → **R10**. R11 does not come from plan: it is the close-out ledger of the whole delta (the registry surface and the two red lines), which 34 makes separate entries of in D-171 / D-172. So `4 → 11` splits the **acceptance faces**; it does not add 7 requirements. Merged back, they are plan's 4 items.

Three points to take from the table:

1. **R2 and R6 are two different things and must not be merged.** R2 governs "crops cannot be planted" (the item side's sowing paths), and R6 governs "farmland is no longer managed" (the event listeners and the ledger). Only with both done does the player get the combination "hoe farmland wherever you like, but this mod's crops still cannot be planted"; do only one, and a working old route is left behind (RT-42, RT-48).
2. **R7 / R8 / R9 share one data structure** (the 16-entry binding list on the core side), but they are accepted in completely different ways: R7 looks at whether the binding is right, R8 at the container and its drops, R9 at whether there is a line on the screen. The three are listed separately in §8.
3. **R11 does not get a section of its own**; its close-out content is in §9.3: the two groups of numbers, registry surface `97` and red lines `2 / 0`, only line up after R1 ~ R10 have all landed, and they read more naturally at the end of the functional requirements.

---

## 2. Tiering (R1)

**Grounds for this section: D-149, D-150, D-151.**

### 2.1 One-line positioning

Each of the 22 crop profiles gets **one** static property `tier` (`COMMON` / `RARE` / `EPIC`), with the hard constraint that the three tiers hold **11 / 7 / 4** entries; the only changes a player sees are **the item name's color** and **one extra tooltip line**. The tier changes no stacking, no drops, no item id, no registry surface and no texture (D-150, D-151).

### 2.2 The three tiers and their membership (all 22 named one by one)

The table below is the **only list** of v0.2.0's tiering and is identical, character for character, to 34 §4.1; downstream documents may cite tiers only from this table.

| Tier | Entries | Crop ID (named one by one) | Chinese name (current language-key value) |
|---|---|---|---|
| **Common** `COMMON` | **11** | `puffed_wheat`, `blast_carrot`, `pumpkin_turret`, `melon_grenade`, `melon_slice_bomb`, `red_mushroom_puff`, `brown_mushroom_puff`, `molotov`, `fang_trap_cactus`, `bamboo_firecracker`, `vibration_bean` | 膨胀麦, 胡罗贝, man瓜, 瓜！, 瓜片子, ？！菇菇！？, ？！棕色菇菇色棕！？, 莫洛托夫！, 刺球, 爆竹, 震动豆 |
| **Rare** `RARE` | **7** | `zangief_cane`, `friend_chives`, `power_beet`, `compressed_puffed_wheat`, `ubi_server`, `toxic_ubi_server`, `point_defense_fungus` | 桑吉尔蔗, 朋友的韭, 甜菜！, 压缩膨胀麦, 育碧服务器, 毒育碧服务器, 近防菌 |
| **Epic** `EPIC` | **4** | `lucky_berry`, `awakened_torchflower`, `awakened_pitcher_plant`, `wild_apple` | 幸运浆果, 激活的火把花, 激活的瓶子草, 狂野苹果 |
| **Total** | **22** | **11 + 7 + 4 = 22** | 22 entries |

**Counting each tier with the expression** ([D-149 supplement] + [author's supplement: arithmetic]):

- Common `11`: counted item by item = 11. These 11 are **identical, item for item, to the common list in plan `:10`**, and v0.2.0 adds nothing to or removes nothing from this tier (Appendix A row 2).
- Rare `7` = the **4** plan lists (`zangief_cane`, `friend_chives`, `power_beet`, `compressed_puffed_wheat`) + the **3** added by author's ruling #2 (`ubi_server`, `toxic_ubi_server`, `point_defense_fungus`). Those 3 added entries are profiles introduced in v0.0.3, and plan's list was written before them (Appendix A row 2).
- Epic `4`: counted item by item = 4, the same as plan `:12`.
- Total `11 + 7 + 4 = 22`, the same count as `CropProfile.ALL`'s 22 entries and as the existing assertion `size() == 22` at `Farmstorm.java:112-113`.

**The carrier of the three tiers is the profile, not the instance** (D-150): any stack and any state of the same id is the same tier; it does not vary with the player, the dimension or time; it is **not** a data component, **does not** travel with the item stack, and **does not** enter `BuiltInRegistries`. So "the same id in two tiers" **does not exist** in this version, and must not be written as a state that needs NBT to tell apart.

### 2.3 Seeds follow their parent crop (7 standalone seeds)

A seed has no tier source of its own and always follows its parent crop ([card §3.2 last row], [D-151]). All 7, one by one:

| Seed item | Chinese name | Parent crop | Tier |
|---|---|---|---|
| `puffed_wheat_seeds` | 膨胀麦种子 | `puffed_wheat` | Common |
| `melon_grenade_seeds` | 瓜！种子 | `melon_grenade` | Common |
| `pumpkin_turret_seeds` | man瓜种子 | `pumpkin_turret` | Common |
| `chive_seeds` | 朋友的韭种子 | `friend_chives` | Rare |
| `power_beet_seeds` | 甜菜！种子 | `power_beet` | Rare |
| `awakened_torchflower_seeds` | 激活的火把花种子 | `awakened_torchflower` | Epic |
| `awakened_pitcher_plant_pod` | 激活的瓶子草豆荚 | `awakened_pitcher_plant` | Epic |
| **Total 7** | — | 7 parent crops | **3 + 2 + 2 = 7** |

The other 15 crops **have no standalone seed item**: 8 have a `seedItem` pointing at themselves (`blast_carrot`, `ubi_server`, `toxic_ubi_server`, `fang_trap_cactus`, `bamboo_firecracker`, `red_mushroom_puff`, `brown_mushroom_puff`, `lucky_berry`), and 7 have not even a `seedItem` (`melon_slice_bomb`, `molotov`, `zangief_cane`, `wild_apple`, `compressed_puffed_wheat`, `point_defense_fungus`, `vibration_bean`) [`CropProfile.java:47-128`] [34 §4.2].

### 2.4 The four sets derived from the tiering (cited downstream, never recomputed)

| Purpose | Set | Count | Expression | Decision |
|---|---|---|---|---|
| crops the cultivator **accepts** | Common + Rare | **18** | 11 + 7 | D-162 supplementary item |
| crops the cultivator **rejects** | Epic | **4** | 22 − 18 | D-162 |
| crops that yield **2 crops + 2 seeds** | inside the admission surface, with a standalone seed item | **5** | 3 Common + 2 Rare | D-163 supplementary item |
| crops that yield **2 crops only** | inside the admission surface, without a standalone seed | **13** | 18 − 5 | D-163 supplementary item |
| the Lucky Berry pool | the whole Common set | **11** | 19 − 8 | D-156 |

Two points to take from the table: ① **the Epic tier can only come from trading** (none of the 4 can enter the cultivator, D-162), so the third trade item can neither be dropped nor merged into the Rare item (D-157 supplementary item); ② **the Lucky Berry pool = the whole Common set**, not "a few picked out of Common": 11 entries, not one more and not one fewer.

### 2.5 How it looks: name color plus one tooltip line

**Only two things move; nothing else does** (D-151, author's ruling #14):

| Item | Rule | Basis |
|---|---|---|
| Name color | the names of 22 crop items + 7 seed items = **29** items are colored by tier: Common **white** / Rare **blue** / Epic **gold** | D-151; [author's supplement: arithmetic 22 + 7 = 29] |
| Color values | taken from vanilla `ChatFormatting`'s `WHITE` / `BLUE` / `GOLD` (`#FFFFFF` / `#5555FF` / `#FFAA00`); **no new palette is added** | [author's supplement] — 34 gives only "white / blue / gold"; what is added here is the layer "borrow the vanilla constants", so that 36 can align to it directly |
| Tooltip | **one line added** beyond the existing category line: `品质：普通` / `品质：稀有` / `品质：史诗`; the line's format is 1 language key + 3 language keys for the tier names (key names fixed by 36) | D-151; the wording is [author's supplement] |
| Style of the tooltip line | keeps the vanilla tooltip's default color and **does not follow the tier color** (color is for the item name only) | [author's supplement] — 34 does not give this layer; the grounds for not following it are "one piece of information uses one encoding only": if the name color and the line's color were both colored, the same line of information would compete with itself when 3 tiers are shown side by side |
| Texture / model / inventory size | **none of them changes** (all 22 crop textures as they are) | D-151; [card §3.5] |
| Carrier | it must go through `Component`'s style (`withStyle`); it **must not** be drawn a second time in a GUI | D-151 supplementary item |

Three hard requirements:

1. **The name color must sit on the item name's `Component`.** That way an anvil-renamed item, a sign, the chat column and a chest preview show the same item in the same color; drawing an extra layer inside a GUI would make the same item show two colors on two screens (D-151 supplementary item).
2. **The tooltip adds one line only** and is not merged with the existing `CropCategory` line — that is another set of semantics (category ≠ tier) [D-151 supplementary item; `CropCategory.java:15-27`].
3. **No resource beyond language keys is added**: the three tier names + 1 line format, 4 language keys in total (their key names and the Chinese / English wording are landed by 36 in the existing style).

### 2.6 What tier does **not** change (five that must be stated)

| Item | Conclusion | Basis |
|---|---|---|
| stacking limit | **not affected**. Throwables are still 16 and the rest still 64, decided by `CropType` / the profile's existing constants, and unrelated to tier | [`CropProfile.java:43,45`]; D-150 supplementary item |
| drops | **not affected**. All 26 loot tables (15 of them belonging to crop blocks) stay as they are, with not one word of their counts or probabilities changed | D-155; [card §3.3 row 3] |
| item id / NBT | **not affected**. No stack of the same id ever carries a tier field; an item read in from a legacy save is still its own tier | D-150 supplementary item |
| registry surface | **not affected**. `data_components` is still 1, and the tier adds no registry entry | D-150, D-171 |
| recipes / the creative tab's order | **not affected**. The 29 recipes stay put, and the order of the 22 crops in the creative tab stays put | D-155; [`FarmstormItems.java:122-129`] |

**The three places tier does affect** (for cross-checking, not a new measure): the trade price band and level band (§4), cultivator admission (§6), and the Lucky Berry pool (§5). All three judge by tier, not by an id whitelist — change one of the 22 profiles' `tier` and all three follow automatically, which is exactly why D-149 requires them to be matched one by one (RT-49).

### 2.7 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| right-clicking a cultivator with an Epic crop | no reaction, no consumption, no message (see §6.2) | D-162 |
| an item already present in a legacy save | it has no NBT tier field, yet the name color and the tooltip are still correct — the tier follows the id | D-150 supplementary item |
| renaming a crop on an anvil | the name color is kept (the style hangs on the name `Component`, not on a hard-coded display name) | D-151 supplementary item |
| two stacks of the same id | always the same tier; "one stack Rare, one stack Common" does not exist | D-150 |
| the author wants a fourth tier | add one enum value + fill the value in on all 22 profiles + recompute the 4 derived sets; **every related number in this document and in 34 has to be recomputed** | [author's supplement]; D-149 |

---

## 3. Cutting the planting chain (R2)

**Grounds for this section: D-152, D-155.**

### 3.1 One-line positioning

"This mod's crops cannot be planted" must hold on **two paths at once**: ① custom sowing (`ModifiedCropItem.useOn` → `plantCrop` → `isPlantableSoil`); ② the vanilla `ItemNameBlockItem` sowing that the 7 `SeedItem`s inherit. **The block side neither blocks nor deletes**: the 15 crop blocks stay registered (D-155), so "cannot be planted" can only be cut off from the item side.

### 3.2 The two paths compared item by item

| Path | Carrier | Current behaviour | This version's change | Basis |
|---|---|---|---|---|
| ① custom sowing | `ModifiedCropItem.useOn` (`:47-50`) → `plantCrop` (`:66-99`) → `isPlantableSoil` (`:101-107`), call site `:72` | if the block clicked is a `FarmBlock` the crop block is planted; otherwise `PASS` | `isPlantableSoil`'s farmland criterion **becomes a constant `false`** ⇒ `plantCrop` always returns `PASS` and places nothing | D-152 |
| ② vanilla BlockItem sowing | 7 `SeedItem extends ItemNameBlockItem` (`SeedItem.java:16`), overriding only the tooltip (`:28-31`) | inherits vanilla sowing: right-click a farmland block and the crop block is planted, **without going through** `isPlantableSoil` at all | `SeedItem` **overrides `useOn` to return `PASS`**, cutting vanilla sowing off | D-152 |
| ①' the deployable items' delegation branch | `DeployableCropItem.useOn` (`:37-43`): calls `plantCrop` first, and goes on to `super.useOn` only when that returns `PASS` | hitting farmland ⇒ plant a crop; not hitting ⇒ place the deployed form (turret / trap / fungus) | always goes through `super.useOn` ⇒ right-clicking farmland **places the deployed form** | D-152 supplementary item [author's supplement: the behavioural consequence] |

**Why ①② must both be closed** (RT-48): the 7 seeds do not go through `isPlantableSoil` at all, so changing the criterion alone still lets them plant crops on farmland; conversely, overriding only `SeedItem.useOn` without changing the criterion still lets the 15 crop items be planted. The two paths' classes, criteria and call sites are entirely different (`BRIEF` §4 RT-48).

**A note on ①'** ([author's supplement], a reading): `DeployableCropItem` is the class of the 7 deployable crop items, and its `useOn` is a choice between "sow" and "deploy". Once the criterion is a constant `false`, that choice always falls to the "deploy" branch — **this is not a missed closure**: the deployed form is not a crop, it grows no crop, and it cannot enter the "crop on farmland" chain either. It merely turns the result of right-clicking farmland from "plant a crop" into "place a turret", which is a visible behaviour change and is written into §11 row 2.

### 3.3 The 15 crop blocks keep their registration (named one by one)

The 15 crop blocks of `FarmstormBlocks.java:26-43` (all sharing `FarmstormCropBlock`, **with no BlockItem registered**, per the class comment at `:20`):

| # | Registry ID | Owning crop |
|---|---|---|
| 1 | `puffed_wheat_crop` | `puffed_wheat` |
| 2 | `blast_carrot_crop` | `blast_carrot` |
| 3 | `ubi_server_crop` | `ubi_server` |
| 4 | `toxic_ubi_server_crop` | `toxic_ubi_server` |
| 5 | `power_beet_crop` | `power_beet` |
| 6 | `melon_grenade_stem` | `melon_grenade` |
| 7 | `pumpkin_turret_stem` | `pumpkin_turret` |
| 8 | `awakened_torchflower_crop` | `awakened_torchflower` |
| 9 | `awakened_pitcher_plant_crop` | `awakened_pitcher_plant` |
| 10 | `fang_trap_cactus_block` | `fang_trap_cactus` |
| 11 | `bamboo_firecracker_stalk` | `bamboo_firecracker` |
| 12 | `red_mushroom_puff_block` | `red_mushroom_puff` |
| 13 | `brown_mushroom_puff_block` | `brown_mushroom_puff` |
| 14 | `lucky_berry_bush` | `lucky_berry` |
| 15 | `friend_chives_crop` | `friend_chives` |

**Three reasons for keeping the registration** (D-155 supplementary item): ① deleting the blocks would turn them into air in legacy saves, which is far worse than "cannot be planted"; ② a published 0.1.1 save would see "blocks vanishing" in 0.2.0; ③ the registry surface goes by `93 + 4 = 97`, and deleting blocks would knock that large block out. Once the blocks stay, their only remaining role in practice is "decoration + loot tables + the item source for the cultivator's output".

⚠️ Two places in the source comments disagree with this table's numbers, and **both are pre-existing blemishes this version does not fix**: the comment at `FarmstormBlocks.java:25` says "14 crop blocks" while the measured count is 15 (the 14 entries at `:26-39` + Friend Chives at `:43`); the comment at `SeedItem.java:13` says "6 standalone sowing items" while the measured count is 7. The same family of typos includes `LuckyBerryRoller.java:170`'s "14 kinds of pool" (measured 19) and `CropType.java:17,19,21`'s 8 / 5 / 2 (measured 12 / 7 / 3) [34 §8 item 4].

### 3.4 What the player sees

| Scenario | What 0.1.1 is | What v0.2.0 becomes |
|---|---|---|
| right-clicking farmland with a crop item in hand | the crop block is planted (1 consumed) | **no reaction** (the item is not consumed) |
| right-clicking farmland with one of the 7 seeds in hand | the crop block is planted (1 consumed) | **no reaction** |
| right-clicking farmland with one of the 7 deployable crops in hand | the crop block is planted | **the deployed form is placed** (turret / trap / fungus) |
| creative mode | plantable | **equally unplantable** — creative mode hands out items, and the item side has no placement path |
| a crop already growing in a legacy save | it is there | it is there and is harvested as usual (the loot tables are untouched); it simply can never be planted back once harvested |
| the player wants more crops | plant them | four routes only: **the cultivator** (18 entries, and you need 1 to start with), **trading** (22 entries, unlocked by tier), **Lucky Berry** (11 entries, Common only), **the existing recipes and drops** |

"Crops can never be planted again" has to be precise to this degree: **the player side has no route at all to put a crop block into the world**; it is not "crop blocks cannot exist" (legacy saves, commands and data-pack injection can still make them appear, D-155 supplementary item).

**The acquisition matrix by tier** (after R2 + R10 stack up, what each of the four routes can give):

| Tier | Trading (§4) | Cultivator (§6) | Lucky Berry (§5) | Existing recipes / drops | What the first acquisition relies on |
|---|---|---|---|---|---|
| Common 11 | the Novice item (1 random entry × 2) | possible (you need 1 to start with) | **yes**, the whole 11 | legacy saves, the creative tab | all four routes work |
| Rare 7 | the Journeyman item (1 random entry × 2) | possible (you need 1 to start with) | **no** | legacy saves, the creative tab | trading (among them `compressed_puffed_wheat` also has an unordered recipe, see below) |
| Epic 4 | the Master item (1 random entry × 2) | **no** (D-162) | **no** | legacy saves, the creative tab | **trading only** |

Two points to take from the table: ① **the Epic tier's only entrance is a Master-level farmer**, and trading is sampling (§4.4), so "this Epic cannot be bought" is normal on any single villager and is not a bug (B-34); ② **`compressed_puffed_wheat` is an exception inside the Rare tier** — it has an unordered recipe of "4 `puffed_wheat` → 1" [`ModRecipeProvider.java:131-136`], so it does not belong to "trading only", while the other 6 Rare and all 4 Epic do.

### 3.5 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| placing a crop block with the `/setblock` command | still possible (the block is kept; `FarmstormCropBlock` overrides neither `mayPlaceOn` nor `canSurvive`, so it is allowed by default) | D-155; [card §1.2] |
| naturally generated crops from data packs / structure injection | can still appear; this version does not handle them | D-155 supplementary item |
| the crop blocks of a legacy save | present as they are and harvested as usual | D-155 |
| right-clicking a crop item against non-farmland (dirt / grass block) | it never planted there (the criterion requires a `FarmBlock`), and after the change it equally does nothing | [`ModifiedCropItem.java:101-107`] |
| creative mode | equally unplantable | D-152, D-155 |
| someone changes `isPlantableSoil` back to `true` | the 15 crop items can be planted at once; but the 7 seeds are still held back by `useOn`'s `PASS` ⇒ back to the half-dead state of "only one path closed" (RT-48) | D-152 |

---

## 4. Farmer trading (R3)

**Grounds for this section: D-157, D-158, D-159.**

### 4.1 One-line positioning

**3 trade items** are added to villagers of the **farmer profession**, one per tier, unlocked at three successively higher level ranks; each item **takes emeralds only** and gives **one kind** of this mod's crop of that tier; one completed trade leaves it **out of stock**, and it recovers by itself once the vanilla restock arrives. The whole repository has 0 hits for `villager` / `trade`, so this feature is entirely new and has no existing pattern ([card §1.4], RT-46).

### 4.2 The trade table

| Item | Tier | Emerald band | Unlocked at | Quantity per deal | What it gives | Decision |
|---|---|---|---|---|---|---|
| 1 | Common (11 entries) | **2–5** | Novice (the lowest rank) | **2** | 2 of one crop of that tier | D-157, D-159 |
| 2 | Rare (7 entries) | **10–32** | Journeyman (a middle rank) | **2** | as above | D-157, D-159 |
| 3 | Epic (4 entries) | **40–64** | Master (the highest rank) | **2** | as above | D-157, D-159 |

Four points to take from the table:

1. **The 3 trade items are not 3 crops but 3 tiers.** Each covers the whole tier's 11 / 7 / 4 entries, and "which one this item sells this time" is fixed by §4.4 (D-157 supplementary item).
2. **The band constrains the price, not the quantity.** What the player pays is emeralds, and plan's original text is "each trade consumes N emeralds" (`plan0.2.0.txt:14`); how many crops are given per deal is not written in plan and is fixed by this section (§4.3).
3. **The levels give only three ranks.** Author's ruling #6's example measure is "Novice → Common, Journeyman → Rare, Master → Epic", while a vanilla villager's level ladder has more than 3 ranks ⇒ this version **adds no trade item** at the ranks in between (D-157 supplementary item, 34 §9 ⑪). The three names' **actual indices** in the 21.1.213 level table are landed by 36 through `VillagerTradesEvent` (RT-46); this document fixes only "the three tiers correspond to three successively higher ranks".
4. **`maxUses = 1` is the common value of all three trade items**, fixed by D-158 and not changed by this section.

### 4.3 Quantity per deal = 2 (this document's supplement)

| Item | Value | Reason |
|---|---|---|
| crops given per deal | **2**, the same for all three tiers | ① it equals the cultivator's crop output per cycle (D-163), so the player can compare the two acquisition routes with one ruler; ② `2 ≤ 16`, so it always fits even the strictest stack tier (throwables, 16) ([`CropProfile.java:43`]); ③ the quantity does not go into config, the `trade` group is still 6 keys (D-159), and the config total is not pushed from 175 to 176 |

Three notes:

- **No seeds are given.** D-157's wording is "gives this mod's crop of that tier", and plan's own words are "exchange this mod's crops" (`:14`); after R2 a seed has only two uses left (cultivator input and converter recipes, and the converter is deactivated in R10), so stuffing seeds into a trade is handing the player something he can only throw back into the cultivator.
- **The quantity does not vary with tier.** The Epic tier's scarcity is carried by **price** (40–64) and by **only once per tier** (out of stock), not by quantity; if the quantity varied, "how many do I get at a time" would have to be written in three rows for three tiers, and all it would buy is making Epic dearer.
- **The quantity does not conflict with "only one trade at a time"**: `maxUses = 1` counts **completed trades**, not item counts (D-158).

### 4.4 Which one an item sells: random by tier, drawn once when the offer is generated (this document's supplement)

| Item | Rule | Reason |
|---|---|---|
| when it is drawn | once, **when that trade item's offer is first generated**, and fixed afterwards | the same sequencing discipline as D-159's price "taken once when the offer is generated"; re-rolling on every trade would make the same offer jump id in the list |
| what is drawn from | the whole tier (11 / 7 / 4), **equal probability, no weights** | the same shape as the Lucky Berry pool's draw (D-156 supplementary item); no second random measure is added |
| the result | the same villager's item **sells one id for the whole save**, and different villagers draw independently | a fixed id would leave the 3 trade items covering only 3 crops, which contradicts plan's general "exchange this mod's crops" |

**The author has confirmed and accepted this cost** (registered as §12 B-34): trading is **sampling**, not a catalogue. Whether one particular crop can be bought depends on what the farmer you meet has drawn:

- the expected number of villagers to collect all **4 Epic** entries ≈ `4 × H₄ = 4 × 2.0833 ≈ 8.3` Master-level farmers (H being the harmonic number, [author's supplement: arithmetic]);
- the expected number of villagers to collect all **7 Rare** entries ≈ `7 × H₇ = 7 × 2.5929 ≈ 18.2` Journeyman-level farmers.

These two numbers **are not a design defect but an accepted cost**: when v0.2.0 was finalised the author confirmed **keeping 3 items, each random by tier**, with the sampling cost carried by the player's search for villagers. If determinism is wanted (a named id must be purchasable), the only way is to add trade items, and adding items means changing D-157 — **this version does not change it**.

### 4.5 The out-of-stock and restock sequence

| Step | Moment | What happens | What the player sees |
|---|---|---|---|
| 1 | the offer is generated | the crop id is drawn and the price is drawn inside the band; `maxUses = 1`, `uses = 0` | the item appears in the villager's list, showing "2 × price" |
| 2 | the player trades | emeralds −price, crops +2, `uses = 1` | the item turns grey and cannot be clicked |
| 3 | while out of stock | **the item is not deleted, not hidden, not re-idded, and no timer of our own runs** (D-158) | the item is still in the list, merely sold out |
| 4 | the vanilla restock arrives | vanilla resets `uses` to zero | the item is clickable again, **with the price and the crop id unchanged** (the offer is not re-rolled) |
| 5 | trading again | as step 2 | as step 2 |

Two disciplines: ① **no second timer, no NBT, no GUI** (D-158 supplementary item); ② **the restock period, the workstation and the level experience** are all decided by vanilla mechanics, and the exact period is checked by 36 under RT-46 and then written into the technical documents.

### 4.6 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| does it recover after a restock | **it does**. `maxUses = 1` + the vanilla restock ⇒ the item can be traded again; the price and the crop id are unchanged | D-158 |
| creative mode | this version **writes no creative-mode branch**. Whether it can be traded without limit depends on vanilla's existing handling of that mode, and is checked by 36 under RT-46 and then written into the technical documents; the out-of-stock count is still carried by vanilla `uses` | [author's supplement] |
| the trade is taken first (a teammate buys it) | out of stock belongs to the **villager**, not to the player: once a teammate has traded, what you see is a sold-out item. Out of stock is not "one each" | D-158; [author's supplement] |
| two players opening the same villager at once | the completed trade is whatever the server validates; the sold-out state on the client's screen arrives at the next synchronisation | [author's supplement] |
| a farmer already at max level in a legacy save | trade items are granted by vanilla on levelling up, and a max-level villager never levels up again ⇒ **it will not be granted them**; getting the new trade items needs a new villager, or a villager that is not yet max level levelling up to the corresponding rank | [author's supplement]; RT-46 |
| vanilla supply / demand drift and reputation discounts | this version defines the "band" on the offer's **base price**, and vanilla's drift and discounts act as usual with no new interception; whether to turn the drift off is decided by 36 at the technical layer | [author's supplement] |
| the trade item is refreshed away (a profession change) | vanilla behaviour is followed: a profession change rebuilds the offer list, and this version adds no extra lock | [author's supplement] |
| the payment being emeralds only | the three trade items' payment **is emeralds only**, taking none of this mod's items | D-157 |

---

## 5. Lucky Berry (R4)

**Grounds for this section: D-156.**

### 5.1 One-line positioning

`CropProfile.LUCKY_BERRY_POOL` goes from **19 entries to 11**, and the 11 are **exactly the whole Common tier**; the draw algorithm (equal probability, no weights) and the call site are all unchanged, only the pool's contents are swapped. One knock-on that cannot be avoided: the startup self-check assertion `poolSize == 19` at `Farmstorm.java:119`.

### 5.2 The pool 19 → 11: the 11 that can still be rolled (named one by one)

| # | Crop ID | Chinese name | Tier |
|---|---|---|---|
| 1 | `puffed_wheat` | 膨胀麦 | Common |
| 2 | `blast_carrot` | 胡罗贝 | Common |
| 3 | `pumpkin_turret` | man瓜 | Common |
| 4 | `melon_grenade` | 瓜！ | Common |
| 5 | `melon_slice_bomb` | 瓜片子 | Common |
| 6 | `red_mushroom_puff` | ？！菇菇！？ | Common |
| 7 | `brown_mushroom_puff` | ？！棕色菇菇色棕！？ | Common |
| 8 | `molotov` | 莫洛托夫！ | Common |
| 9 | `fang_trap_cactus` | 刺球 | Common |
| 10 | `bamboo_firecracker` | 爆竹 | Common |
| 11 | `vibration_bean` | 震动豆 | Common |
| **Total** | **11 entries** | **`19 − 8 = 11`** | all Common |

**Which crops can still come out of a berry**: only the 11 above. Not **one** of the 7 Rare or the 4 Epic can be rolled (plan `:15`: "no longer produce Rare-quality or Epic-quality crops"). The draw still goes through `pool.get(random.nextInt(pool.size()))` at `LuckyBerryRoller.java:172-180`, **equal probability, no weights**, and this version does not change the draw algorithm (D-156 supplementary item).

### 5.3 The 8 removed and the 3 that were never in the pool

| Category | Entries | Detail | Explanation |
|---|---|---|---|
| removed from the pool (Rare) | **6** | `zangief_cane`, `friend_chives`, `power_beet`, `ubi_server`, `toxic_ubi_server`, `point_defense_fungus` | the Rare tier of the old pool's 19 |
| removed from the pool (Epic) | **2** | `awakened_torchflower`, `awakened_pitcher_plant` | the Epic tier of the old pool's 19 (the other 2 Epic entries were never in the pool) |
| **Subtotal** | **8** | — | `19 − 8 = 11` |
| never in the pool to begin with | **3** | `compressed_puffed_wheat` (Rare), `lucky_berry` (Epic), `wild_apple` (Epic) | the explicit exclusions at [`CropProfile.java:139-143`], which the other half of the assertion at `Farmstorm.java:119-121` guards |

One easy misreading: **the Lucky Berry itself (`lucky_berry`) is Epic and not in the pool either**, so eating a berry can never roll another berry; `wild_apple` is the same. These two exclusions already existed in 0.1.1 and are not new in this version.

### 5.4 Knock-on: the startup self-check's assertion has to change with it

| Item | 0.1.1 | v0.2.0 | Basis |
|---|---|---|---|
| the assertion value | `poolSize == 19` | **`poolSize == 11`** | D-156; [`Farmstorm.java:119`] |
| the other half | the pool excludes `lucky_berry` / `wild_apple` / `compressed_puffed_wheat` | **kept untouched** | D-156 |
| the consequence of not changing it | — | every startup prints one `自检 幸运浆果池 … → FAIL` | RT-50 |

**The "consequence" cell must be written accurately against the source**: `poolOk` takes part only in one `LOGGER.info`'s `OK` / `FAIL` text ([`Farmstorm.java:118-123`]); there is **no `throw` and no `System.exit`**, so the server **starts as usual**. Not changing the assertion is not "it will not start" but "every startup has one self-check that is permanently red" — the self-check's use is "all green normally, and whichever one turns red means that item is genuinely wrong", and one permanently red assertion voids that signal, which is exactly what RT-50 watches.

### 5.5 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| eating a berry and hitting the "generate random crops" branch | `lucky_berry.randomCropCount` items (the config is unchanged), each drawn with equal probability from the 11 | D-156; [`LuckyBerryRoller.java:170-181`] |
| the berry's other branches (effects / lightning etc.) | **completely unaffected**; the pool acts only on the "generate random crops" branch | [`LuckyBerryRoller.java:170`] |
| the pool changed to 11 but the assertion still 19 | the startup log prints FAIL and the server still starts (§5.4) | D-156, RT-50 |
| an Epic entry slipped into the pool | acceptance fails: in this version Epic can only come from trading (D-162 keeps it out of the cultivator, and one leak in the pool would open a second entrance) | D-156, D-162 |
| the pool's count not equal to the Common tier's | acceptance fails: `19 − 8 = 11` and `Common = 11` must hold at the same time | D-156 |

---

## 6. The Crop Cultivator (R5)

**Grounds for this section: D-160, D-161, D-162, D-163, D-164.**

### 6.1 One-line positioning

A new block `farmstorm:cultivator` (the Crop Cultivator): its hardness and blast resistance follow the anvil's `strength(5.0F, 1200.0F)` (`Blocks.java:3053`), it has **no screen** (right-click puts the item straight in), it holds **one** cultivated item at a time, and after **600 ticks** it yields **2 crops** (plus **+2 seeds** when that profile has a standalone seed item), with the output **going automatically into the bound core's storage**; only in the **working state** does it play the 16-frame page-flip animation.

### 6.2 Conditions for use: four gates

All four gates must **pass** before an item goes in; failing any one of them is "no reaction to the right-click" — the held item is not consumed, no message pops up, no error is raised (D-162).

| # | Gate | Criterion | When it fails | Basis |
|---|---|---|---|---|
| 1 | range gate | the Chebyshev distance between the cultivator's chunk and **some allied core**'s centre chunk ≤ 1 (`RANGE_CHUNKS = 1` ⇒ 3 × 3 chunks) | no reaction (such a cultivator usually has no binding either) | [`OutpostCoreBlockEntity.java:53,151-156`]; D-165 |
| 2 | binding gate | a record pointing at it exists in the **core side's** 16-entry list | no reaction (an unbound cultivator still exists and is still visible, it simply cannot be used) | D-165 |
| 3 | hand gate | holding a **Common or Rare** tier crop, or its seed | no reaction: an empty hand, another item, and **the 4 Epic crops and the 2 Epic seeds** all fail | D-162 |
| 4 | empty-slot gate | the slot holds nothing (one at a time; no stacking, no replacing) | no reaction | D-162 supplementary item |

**Gate 3 accepts 23 items in all** ([author's supplement: arithmetic]):

| Category | Entries | Detail |
|---|---|---|
| Common crops | 11 | the whole Common tier of §2.2 |
| Rare crops | 7 | the whole Rare tier of §2.2 |
| seed items inside the admission surface | 5 | `puffed_wheat_seeds`, `melon_grenade_seeds`, `pumpkin_turret_seeds` (Common) + `chive_seeds`, `power_beet_seeds` (Rare) |
| **Total** | **23** | `11 + 7 + 5 = 23` |

The 6 items kept outside the door: the 4 Epic crops + the 2 Epic seeds (`awakened_torchflower_seeds`, `awakened_pitcher_plant_pod`). Of the 18 crops inside the admission surface, another **13** have no standalone seed item (7 of them "seed = itself" and 6 have no seed entry at all, §2.3), and their "seeds" do not make a 24th acceptable item: `18 + 5 = 23` is the whole of it.

**Gate 2's order of judgment must be stated clearly** ([author's supplement], a reading): the range is looked at first (gate 1) and the binding second (gate 2), because "inside the range" is the precondition for a binding to exist at all (D-165's candidate set is exactly "covers it + allied + has a free slot"); in practice both feel like "the right-click does nothing", and the player does not need to tell which gate refused.

### 6.3 The sequence: put in → 600 ticks → output → into the core

| Step | Moment | What happens | Data / world |
|---|---|---|---|
| 1 | `t = 0` | all four gates pass: the held item −1, the slot records "the cultivated item" = that item's **parent crop profile**; `working = true` | the slot is occupied; the block state switches; the animation starts |
| 2 | `t = 1 … 599` | only progress advances; **the core's capacity is not checked** | progress +1 / tick |
| 3 | `t = 600` | settle: compute this cycle's output list per §6.4 (2 crops, possibly +2 seeds), and ask the bound core to deliver the **whole batch** | decide "the whole batch fits" |
| 4a | it fits | the whole batch goes into the core's 54 slots; the slot is cleared; `working = false` | the slot is empty; the core's contents change; the animation returns to the static first frame |
| 4b | it does not fit | **pause**: progress stops at the instant of completion, the slot is not cleared, nothing drops, nothing is lost; `working = false` | none (waiting for the core to free space) |
| 5 | after the pause the core frees space | delivery continues from where it paused, **without restarting the timer** | back to 4a |

Four disciplines:

1. **600 ticks = 30 seconds × 20 ticks/second** ([`plan0.2.0.txt:21`]'s "thirty seconds" + MC's 20 ticks per second; [author's supplement: arithmetic]), going through `cultivator.cycleTicks`, default 600 ([card §3.4], [card supplement]).
2. **The output quantity goes through `cultivator.yield`, default 2** (D-163). The "2 seeds" half **gets no config key of its own** and follows §6.4's rule.
3. **"It fits" = judged on the whole batch, not item by item** (D-164 supplementary item): every item of this cycle's output must either merge into the remaining space of an existing stack of its kind or get an empty slot; if even one item does not fit, the whole batch is not delivered.
4. **It neither drops nor goes into the player's inventory** (plan `:21`'s "stored automatically in the storage space of the Outpost Core it is bound to").

**Four worked examples** (to nail the sequence down, [author's supplement]):

| Scenario | Result |
|---|---|
| one cultivator fed `puffed_wheat` (Common, with a standalone seed) | tick 600: +2 `puffed_wheat` and +2 `puffed_wheat_seeds` in the core; the slot is cleared and the 2nd can be fed at once |
| one cultivator fed `ubi_server` (Rare, seed = itself) | tick 600: +2 `ubi_server` in the core, **no seeds** (giving 2 more would be 2 items out of thin air) |
| one cultivator fed `chive_seeds` (a Rare seed) | tick 600: +2 `friend_chives` and +2 `chive_seeds` in the core — **exactly the same** result as feeding the parent crop |
| the core has only 1 empty slot left and this cycle's output is 4 different items | 3 have nowhere to go ⇒ **the whole batch is not delivered** ⇒ pause; not "shove 1 in first" |

### 6.4 The three classes of output (5 give 2 + 2, 13 give 2 only)

| Category | Entries among the 22 | Entries inside the admission surface (18) | Output | Example |
|---|---|---|---|---|
| with a standalone seed item | **7** | **5** | **2 crops + 2 seeds** | `puffed_wheat`, `friend_chives` |
| seed = itself | **8** | **7** | **2 crops** (giving 2 more would be 2 items out of thin air) | `blast_carrot`, `ubi_server` |
| no seed at all | **7** | **6** | **2 crops** (there is no registry entry to give) | `zangief_cane`, `vibration_bean` |
| **Total** | **22** | **5 + 7 + 6 = 18** | — | — |

**The 5 inside the admission surface that give 2 + 2, named one by one**: `puffed_wheat`, `melon_grenade`, `pumpkin_turret` (Common) + `friend_chives`, `power_beet` (Rare); the other **13 give 2 crops only** (`18 − 5 = 13`). This corresponds character for character to the derived table in 34 §4.3 and to author's ruling #15's "inside the admission surface 5 give 2+2 and 13 give 2 only".

**What putting a seed in produces** ([author's supplement], not covered by 34): the output is decided by the **parent crop profile** and has nothing to do with whether a crop or a seed went in — putting in `chive_seeds` gives the same 2 crops + 2 seeds as putting in `friend_chives`. The reason: gate 3 lets seeds in (plan `:21`'s own words, "or its seeds"), and if the output rule were "what goes in is what comes out", a seed would produce "seeds" instead of crops, the opposite of plan's output semantics.

**This is what stops "2 seeds" from being a dead item**: after R2 a seed can no longer be planted, but it can serve as cultivator input and be exchanged for 2 crops + 2 seeds ⇒ a self-sustaining loop of crop → 2 crops + 2 seeds. This is also the practical meaning of D-163 keeping "2 seeds".

### 6.5 The animation: played only in the working state (two textures switched)

| State | Definition | Display | Basis |
|---|---|---|---|
| working state | the slot holds a cultivated item **and** it is not paused because the core is full | **plays** the 16-frame animation (`frametime 2`, one cycle `32` ticks = 1.6 seconds) | D-161 |
| idle (empty slot) | the slot holds no cultivated item | **static first frame** | D-161 |
| paused (core full) | the slot holds a cultivated item, but the whole batch cannot be delivered | **static first frame** | D-161, D-164 |
| unbound | it is not in the core side's list | **static first frame** (and it produces nothing either) | D-165 |

**Why it has to be "two models + two texture sets"** (D-161 supplementary item): an mcmeta animation is **texture-level** — the same image moves wherever any model references it, and the code layer has no access to a "pause" switch. So "idle is static" can only be achieved by "the idle model references a static image": `working=true` mounts the frame strip carrying the mcmeta, and `working=false` mounts the static image taken from frame 0 of the strip. The resources are therefore **3 PNGs + 2 mcmeta + 2 static first-frame images** (1 each for side / top; bottom is already 32 × 32 static and needs no copy), while fact card §3.5 records only the first two of these (registered in 34 §9 ⑤).

**`working` must go through a blockstate property** and cannot rely on block-entity synchronisation alone, otherwise the client does not know it should swap models (D-161 supplementary item). The three faces are assembled with `cube_bottom_top` (top / side / bottom), and **the file names must not be changed** (the texture pack's README explicitly requires the three names `farmstorm:block/cultivator_a_top` / `_side` / `_bottom`) [card §1.8].

### 6.6 Block properties

| Property | Value | Basis |
|---|---|---|
| hardness / blast resistance | **`strength(5.0F, 1200.0F)`** (following the anvil) | D-160; `Blocks.java:3053` |
| the other properties | `MapColor.METAL` / `requiresCorrectToolForDrops` / `SoundType.ANVIL` / `PushReaction.BLOCK` (following the anvil's registration chain) | D-160 supplementary item; `Blocks.java:3051-3055` |
| screen | **none**. No menu is registered (`menu_types`'s +1 goes to core storage only) | D-160 supplementary item; D-171 |
| breaking drops | drops **itself + the cultivated item inside** (per `ConverterBlock.java:88-96`'s four-condition measure) | D-160 supplementary item |
| registry surface | `blocks +1`, `block_entities +1`, `items +1` | D-160, D-171 |

### 6.7 What the player sees

| Scenario | What the player sees |
|---|---|
| first sight | a new block with an anvil's hardness: a still image when it is quiet, and it flips pages while it works (one round in 1.6 seconds) |
| putting an item in | right-click with a Common / Rare crop in hand: the hand loses 1 and the block starts moving; **no screen, no sound cue, no text** |
| waiting | 30 seconds, during which the block keeps moving |
| output | the block stops and the core's chest has 2 more crops (2 more seeds too if it has a standalone seed); **nothing falls on the ground** |
| the core is full | the block stops dead (back to the static image) and nothing is lost; it carries on by itself once room is cleared |
| right-clicking with an Epic in hand | no reaction |

### 6.8 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| putting in an Epic crop / an Epic seed | no reaction, no consumption, no message | D-162 |
| the core is already full when an item goes in | **it goes in and the timer starts as usual**; at tick 600 the whole batch does not fit ⇒ pause (§6.3 step 4b) | D-164; [author's supplement] |
| right-clicking again while the slot is occupied | no reaction (no stacking, no replacing, no squeezing out the 1st) | D-162 supplementary item |
| right-clicking empty-handed | no reaction, and **nothing is taken out**; the only way to get the item back is to break the block (breaking drops the cultivated item inside) | [author's supplement]; D-160 supplementary item |
| the cultivator is broken / blown up | drops itself + the cultivated item inside (the four-condition measure; a piston push does not drop it a second time) | D-160 supplementary item |
| the cultivator is unbound (no allied core nearby when it was placed) | the block is there and visible, and **right-clicking does nothing** | D-165 |
| putting a core next to it afterwards | **no automatic claiming**: a binding is written only at two moments, "on placement" and "on migration when the core is broken" | D-165 supplementary item |
| a core already has 16 bound, and a 17th is placed | the 17th is unbound and unusable, and **does not squeeze out** any bound one | D-165 |
| the cultivator's chunk unloads / the server stops | progress is written to disk and continues from where it was after reload (no restarting the timer, no clearing the slot) | [author's supplement] |
| the player leaves the core's 3 × 3 range | what is already in **produces as usual** — the four gates are judged only at the instant of the right-click | [author's supplement] |
| the bound core is broken and a successor exists | it is rebound, and the slot's contents and progress are kept (§8.5) | D-168 |
| the bound core is broken and there is no successor | it stays where it is, contents kept, marked unbound, unusable, producing nothing — **it does not vanish, does not turn to dirt, does not drop** | D-168 |
| space is freed in the core mid-timer and then filled again | exactly the 4a / 4b loop of §6.3, no stopping and no queueing | D-164 |

---

## 7. Retiring the farmland system (R6)

**Grounds for this section: D-153, D-154.**

### 7.1 One-line positioning

"Retire the farmland system" = **only comment out the call sites**: the farmland system's **entrances** are commented out so that the rule set is no longer in force at runtime. It is **not deleting code** (`FarmlandLedger`'s 555 lines and `FarmlandGuard`'s 225 lines are not changed by a single line, still compile and are still in the source tree), **not deleting the save data** (`farmstorm_farmland.dat` is not read, written or deleted), and **not deleting blocks** (the 15 crop blocks stay registered, §3.3).

### 7.2 The seven landing points that are deactivated (named one by one)

| # | Landing point | What 0.1.1 does here | This version's action | Basis |
|---|---|---|---|---|
| 1 | `FarmstormGameEvents.java:75` | registers `onRightClickBlock` | **comment out the registration line** | D-153 |
| 2 | `:241-265` | the hoeing block + two action-bar messages (`NO_CORE` / `QUOTA_FULL`) + writing the binding when it is allowed | **comment out the implementation** | D-153 |
| 3 | `:76` | registers `onFarmlandTrample` | **comment out the registration line** | D-153 |
| 4 | `:277-281` | trample protection (cancelling `FarmlandTrampleEvent`) | **comment out the implementation** | D-153 |
| 5 | `:171` | `FarmlandLedger.get(server).backfill(server)` (the startup backfill) | **comment out this one line only** (`onServerTickPost` itself is kept) | D-153, D-154 |
| 6 | `FarmstormConfig.java:909-912` (**comment out the definition only**), `:227` (**the field declaration is kept**) | `outpost_core.farmlandBonus` (per-core capacity + the quota's base, one key with two meanings) | **comment out the definition only** `:909-912`, the slot going to `cultivatorCapacity`; **the field declaration `:227` is kept** | D-153, D-165; **A-62** |
| 7 | `ModLangProvider.java:403`, `:412-413` | 3 farmland language keys (`outpost_core.farmlandBonus`, `message.farmstorm.farmland.no_core`, `…quota_full`) | **comment out** | D-153 |

**Every commented-out place must carry a one-line reason** ("裁定 #3：v0.2.0 撤销耕地，勿恢复") — RT-42's whole risk rests on that line; without it, the next person will delete these comments as mistaken ones (D-153 supplementary item).

**Why landing point 6 comments out the definition only and keeps the field** (A-62): the two classes `FarmlandLedger` / `FarmlandGuard` are D-153's "not one line changed", and what they read is precisely `FarmstormConfig.outpostCoreFarmlandBonus` (`:227`, `ModConfigSpec.IntValue`) ⇒ **the field declaration must not be commented out, or those two dead classes would not compile**. So this version **comments out the definition `:909-912` only** and keeps the field declaration `:227` ⇒ the field is **permanently `null`** (nobody assigns to it any more). If someone restores the listener by mistake, the first read of that field is an **NPE** — a deliberate "it makes a noise" rather than a silent revival, and the mitigation still rests on the reason line at the comment (RT-42).

### 7.3 The four things that must be kept

| Item | Position | Why it is kept |
|---|---|---|
| the `onServerTickPost` listener itself | `:73` (registration), `:166-176` (implementation) | it still has to run `CoreListBroadcaster.tick` (`:175`), and the overlay's packet has to go out every tick; only the line at `:171` is commented out (34 §9 ⑦) |
| `CoreChunkTickets.restoreAll` | `:170` | it restores the cores' chunk tickets and has nothing to do with farmland (D-153 supplementary item) |
| the two classes `FarmlandLedger` / `FarmlandGuard` | the `farmland/` package, 555 + 225 lines | not one line changed, still compiling, and `grep` finds no caller — this is deliberate (§7.5) |
| `farmstorm_farmland.dat` | under the save's `world/data/` | not read, not written, **not deleted** (D-154) |

**The `.dat`'s three nos** have to be written out separately: ① once the call site is commented out this `SavedData` is no longer `get()`-ed; ② once it is `get()`-ed, saving would rewrite the file, and even with unchanged contents it would move the `dataVersion` and the timestamp, which amounts to "quietly changing a piece of data we have declared no longer maintained"; ③ not deleting it is a **data-safety** decision — delete it and there is no way back, whereas leaving the old saves' farmland-ownership information on disk keeps the raw data available should this system ever be restored or a migration tool written (D-154 supplementary item).

### 7.4 What the player sees

| Scenario | What 0.1.1 is | What v0.2.0 becomes |
|---|---|---|
| hoeing outside an allied core's range | **blocked**, with a `NO_CORE` message on the action bar | hoe as usual, no message |
| covered by an allied core but the quota is full | **blocked**, with a `QUOTA_FULL` message (carrying the two numbers "used / limit") | hoe as usual, no message |
| vanilla crops (wheat / carrots / beetroot…) | affected by the same gate — farmland could only be created inside an allied core's range ⇒ vanilla farming was restricted along with it | **plantable anywhere** (that gate was added by this mod in the first place) |
| trampling one's own field | bound farmland could not be trampled | **it turns to dirt when trampled** (the protection listener is commented out) |
| who the farmland "belongs to" | it belongs to some core, with a quota of `16 × number of allied cores` | **no ownership and no quota any more** |
| a field in a legacy save | protected and counted against the quota | still there and still plantable with vanilla crops, but **no longer protected and no longer counted against anything** |
| `/farmstorm farmlandlimit` | already removed in 0.1.1 | still does not exist (this version neither removes it again nor restores it) |

In one sentence: **farmland returns to vanilla semantics**. The three things the player notices are "no longer blocked", "no quota message", and "the field can be trampled".

### 7.5 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| someone restores the comments (by mistake) | the old rules **come back at once**: hoeing is blocked again, trampling is protected again, the startup backfill runs again. The reason line at each comment is the only line of defence | RT-42; D-153 |
| a legacy save's `farmstorm_farmland.dat` | the file stays on disk, not read, not written, not deleted | D-154 |
| already-bound farmland in a legacy save | the ledger is no longer `get()`-ed ⇒ the binding does not exist at runtime; the block is still farmland and works as vanilla does | D-154 |
| farmland drying out and reverting | 0.1.1's trample protection **never covered** the drying-out reversion anyway (`FarmBlock#randomTick` reverts it directly); with this version's listeners commented out, both the reversion and trampling return to vanilla | [`FarmstormGameEvents.java:275`] |
| the two classes still in the source tree with no caller | kept deliberately: delete them and there is no way back, and the history of "why farmland rules used to be here" disappears too | D-153 supplementary item; 34 §8 item 3 |
| the server's `/farmstorm` command tree | removed whole in 0.1.1; the `/farmstorm` that exists today is only the client's `CoreClientCommands` (`hud on / off / reset`) | [card §1.2] |

---

## 8. The Outpost Core rework (R7 / R8 / R9)

**Grounds for this section: D-165, D-166, D-167, D-168, D-169.**

### 8.1 One-line positioning

The Outpost Core turns from "storing only the owner and binding farmland" into the carrier of three things: **binding 16 cultivators** (R7), **a 54-slot storage container** (R8), and **the link's origin** (R9). The three numbers must always be spoken of separately: **16 is the allowance, 54 is the slot count, 3 × 3 is the range**.

### 8.2 R7: the binding target changes from farmland to cultivators (capacity 16)

| Relation | Cardinality | What is stored | Judgment boundary |
|---|---|---|---|
| player → core | 1 : 0..2 | the core's `ownerUuid` / `ownerName` | at most `outpost_core.maxPerPlayer` default 2 per player, untouched in this version [`FarmstormConfig.java:913-915`] |
| core → cultivator | 1 : 0..**16** | the core side's 16-entry binding list | capacity = `outpost_core.cultivatorCapacity` default 16 |
| cultivator → core | 1 : **0 or 1** | the reference cache on the cultivator side | the single truth is on the core side; when the two sides disagree it is treated as **unbound** |

**The four criteria for a binding** (D-165):

| # | Criterion | Explanation |
|---|---|---|
| 1 | covers it | the cultivator is inside the core's 3 × 3 chunks (`inRange`, `RANGE_CHUNKS = 1`) |
| 2 | allied | the core's owner himself or a same-team teammate (D-138) |
| 3 | still has a free slot | the core side's list < 16 |
| 4 | nearest first | distance taken as `BlockPos#distSqr` (3-D Euclidean squared); a tie is broken by `placedGameTime` ascending → the core coordinate in lexicographic order |

**"The single truth is on the core side" must be written as an executable criterion**: the core's 16-entry list is both the capacity carrier and the lookup surface; the "whom am I bound to" copy inside the cultivator's BE is a **reference cache**, validated against the core side's list before every use, and **when the two sides disagree it is treated as unbound**. Writing happens at only two moments — on placement, and when the bound core is broken (D-168); at runtime there is no "drift to the nearest / periodic rebinding", so a second kind of binding truth does not exist (D-165 supplementary item, 34 §9 ⑫).

**Two player consequences must go into the boundaries**: ① placing one when there is no allied core nearby ⇒ the block is there but unbound and unusable; ② placing a core next to it **afterwards** does **not** claim it automatically (§6.8). The second is the one players most easily read as a bug, which is why §12 gives it a row of its own.

**Five worked examples** (to nail "allowance 16" down, [author's supplement]):

| Scenario | The core's state | The result when a cultivator is placed |
|---|---|---|
| 1 | 1 core, 0 bindings | bound; the core side's list `0 → 1` |
| 2 | 1 core already with 16 bindings | the 17th is **unbound** and unusable; not one bound one is squeezed out |
| 3 | A is already full at 16, B still has free slots, both cores cover the point | binds to **B** (the candidate set is first filtered for "has a free slot", then the nearest is taken) |
| 4 | both A and B cover it, both have free slots, the distances are equal, placed in the same tick | ordered by `placedGameTime` ascending → the core coordinate in lexicographic order; any two runs over the same save give the same answer |
| 5 | A is broken (A has 3 bindings), B covers 1 of those cultivators | 1 is rebound to B; the other 2 find no successor core ⇒ they stay where they are, are marked unbound and are unusable |

### 8.3 R8: 54-slot storage (a double chest)

| Item | Rule | Basis |
|---|---|---|
| slots | **54** (a double chest), through `outpost_core.storageSlots` | D-166; author's ruling #8 |
| form | a container `implements Container`, with `getUpdateTag` / `getUpdatePacket` added | D-166 |
| how it is opened | right-click the core empty-handed (`useWithoutItem`) and the storage screen opens; `menu_types +1` | D-166; D-171 |
| who can open it | **two gates**: ① allied (the core owner + teammates, D-138); ② the player is within the vanilla container's **reach** (`AbstractContainerMenu#stillValid`'s default semantics, **no custom radius is introduced**). Failing either ⇒ no reaction to the right-click | D-166 supplementary item; ruled at the v0.2.0 finalisation |
| legacy compatibility | an old BE with only the three owner fields ⇒ read as **an empty container + 0 bindings**; a missing key counts as empty and is not inferred backwards | D-166 supplementary item |
| being broken | all items in the 54 slots **drop** | D-167 |
| who puts things in | the player by hand + the cultivator's automatic delivery (§6.3 step 4a) | D-164, D-166 |

**54 slots ≠ 16 allowance** (author's ruling #8, RT-44) — this table must be readable on its own:

| Dimension | **54** | **16** |
|---|---|---|
| what it is | the container's **slot count** (holding items) | the number of **cultivators** one core can bind |
| config key | `outpost_core.storageSlots` (default 54) | `outpost_core.cultivatorCapacity` (default 16) |
| who uses it | the player storing and taking by hand + the cultivator's delivery | the core → cultivator binding list |
| what happens when it is full | the player cannot put more in; **the cultivator pauses its output** (D-164) | a new cultivator is **unbound** and unusable (D-165) |
| the correspondence in plan | "equivalent to a large chest" (`:28`) | "a list of capacity 16" (`:27`) |
| what goes wrong if the two are swapped | 16 slots is not even a single chest, which flatly contradicts plan's analogy; nor could it carry the delivery volume of 16 cultivators | taking 16 as the slot count = tombstone §7 item 3 |

**The sequence of "opening"** ([author's supplement], adding only the action surface, not changing the criteria):

| Step | What happens |
|---|---|
| 1 | the player right-clicks the core **empty-handed** (with a block in hand, vanilla placement runs first and the chest does not open) |
| 2 | the server judges two gates: **allied ∧ the vanilla reach** (`stillValid`'s default semantics); both pass ⇒ the 54-slot screen opens; either fails ⇒ no reaction |
| 3 | the screen has the same shape as an ordinary double chest: 54 slots on top + the player's inventory |
| 4 | on closing it settles per the vanilla container measure; the server is authoritative and no client-side predictive write is done |

**Where the two gates come from** (ruled at the v0.2.0 finalisation): 34's D-166 originally wrote only "allies only", and the finalisation added the second one — **the player is within the container's reach** — and **reuses `AbstractContainerMenu#stillValid`'s default semantics directly**: vanilla decides it from the container's position and the player's position, and **no custom radius is introduced, no config key is added, no new distance constant is brought in**. So this version's opening criterion is the two of "**allied ∧ reach**", and **neither the 54 slots nor the 16 allowance has changed**.

**The two easily confused "ranges" must be kept apart**: ① **opening** = the vanilla reach (it has to do with the player's hand: standing beside the core is enough to reach it); ② **binding (D-165) and the link (D-169)** = the core's **3 × 3 chunk** range (it has to do with chunks and not at all with where the player stands). **The 3 × 3 chunks are not an opening condition** — standing beside one's own core, even outside the 3 × 3 boundary, still opens it; conversely, an allied player who has gone far away can no longer reach it (B-23).

### 8.4 R8: breaking drops (D-167)

| Scenario | Does it drop | Basis |
|---|---|---|
| mined by a player (**really removed**) | **drops**, all 54 slots | D-167 |
| pushed by a piston | **does not drop** | D-167; the four conditions of `ConverterBlock.java:88-96` |
| same-block replacement (`state.is(newState.getBlock())`) | **does not drop** | as above |
| chunk unload | **does not drop** | as above |
| nothing at all is in the core | only the block itself drops | D-167 |

Three notes: ① the core's blast resistance is **1200** [`FarmstormBlocks.java:86`], so an ordinary explosion cannot break it, which means "blown up" is not a realistic path under the default configuration — this section is written per "really removed" and sets up no explosion branch of its own; ② a drop and "settling when the core is broken" are **two things and two hooks** (D-167 supplementary item): the drop goes through `onRemove` (the block is gone, so it drops), while the index removal + cultivator migration go through `setRemoved` / `settleOnRemoval` (§8.5); ③ the criterion must follow the precedent's four conditions, otherwise there would be a duplication exploit that "spawns a fresh set of items in place when a piston pushes it".

### 8.5 R7: cultivator migration when the core is broken

| Step | Condition | What is done | Does the world change |
|---|---|---|---|
| 1 | the core block is **really** removed (not a chunk unload, not a server stop) | take out the core side's 16 binding records | no |
| 2 | record by record | look for other cores that "cover this cultivator + allied + still have a free slot", nearest first, the tie key as in §8.2 | no |
| 3a | one is found | **rebind**: write the records on both sides of the new core; the cultivator's slot contents and progress are kept | no |
| 3b | none is found | **stay where it is, contents kept, marked unbound, unusable** | no |
| 3c | that cultivator's chunk cannot be read (the exception path) | keep the NBT's original values, do not guess, do not delete the binding | no |
| 4 | everything is processed | remove the index, release the chunk ticket, update the overlay list | no |

**There is only one difference from 0.1.1, and it is this section's focus**: 0.1.1's D-136 **touched the world** when migration failed (`setBlock(DIRT)`, `OutpostCoreBlockEntity.java:387`); this version **does not touch the world** when migration fails — a cultivator is a block of its own and can simply stay. So the three exits are "rebind / keep and mark unbound / skip defensively", and **there is no "turn to dirt" branch** (D-168 supplementary item, RT-43). Rejected: "break the cultivator and drop its items if it cannot be migrated" — plan explicitly writes "cannot be used", not "is broken" (the same logic as 34 §7 item 1).

**No chunk is loaded for migration** (D-168 supplementary item): a bound cultivator must be inside the core's 3 × 3, and the chunk tickets pin that ring in memory ⇒ those chunks are in memory during the migration; should one still be unreadable, 3c applies.

### 8.6 R9: the link (a client wireframe + a new packet + dispatch inside the range only)

| Item | Rule | Basis |
|---|---|---|
| what it is | a **purely visual** wireframe between "core ↔ cultivator": no entity, no collision, no blocking, no glow, no particles, no registry surface | D-169; plan `:29` |
| data side | **a new 2nd custom packet** (S2C read-only), without widening `core_list`; the packet id, the codec, and the send / retract moments are listed by 36 | D-169, D-172 |
| when it is sent | sent only while **the player is inside that core's 3 × 3 chunk range** | D-169 supplementary item; [the note under the table in card §2] |
| client behaviour | **read-only**: draw what is received, do not compute bindings oneself, do not cache across dimensions; leftovers received after leaving the range must be cleared | D-169 supplementary item |
| server dependency | **no** server logic (binding, output, migration, protection) may depend on whether the link exists, nor on whether the client received the packet | D-169 supplementary item |
| relation to the boundary particles | mutually independent: the existing 48 particles per ring on the core are unchanged to the character [`OutpostCoreBlockEntity.java:56,274-289`] | — |

**Why a new packet is needed and `core_list` is not widened** (D-169 supplementary item): `core_list` is a **full snapshot** of "the allied core list" (`MAX_CORES = 64`, three-level sorting, fingerprint debouncing), while the link is another kind of data — **per player, range-filtered, following the bindings**; cramming it into the same `StreamCodec` would mix the two semantics together, and both the debouncing and the fingerprint would have to be rewritten.

**What the player sees**: standing next to one of his own cores, there is a line between the core and the cultivators bound to it; walk away and the lines go; break the core or a cultivator and the corresponding line goes. The line's thickness, color and whether it draws through blocks are decided by 36 at the `RenderType` layer (RT-45).

### 8.7 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| a non-allied player right-clicking the core | no reaction | D-166 supplementary item |
| an allied player right-clicking the core while outside the core's 3 × 3 chunks (still within reach) | **it opens** (the 3 × 3 chunks are not an opening condition; opening looks at the vanilla reach) | D-166 supplementary item; §8.3 |
| two allied players opening it at once | the same 54 slots, server-authoritative, no duplication | [author's supplement] |
| a legacy core with only the three owner fields | read as an empty container + 0 bindings, no error, no save wiped | D-166 supplementary item |
| when the core is broken none of the 16 cultivators finds a successor | all 16 stay where they are, all are marked unbound, all are unusable | D-168 |
| when the core is broken one cultivator has already been mined by another player | that record is skipped (the block is not there) and the rest migrate as usual | D-168 |
| a cultivator and a core in different dimensions | impossible: a binding only happens between cores of the same dimension (the candidate set comes from cores of the same dimension) | D-165 supplementary item |
| a player placing a 3rd core | stopped by `maxPerPlayer = 2` (untouched in this version) | [`FarmstormConfig.java:913-915`] |
| the link packet leaving leftovers after a disconnect | after reconnecting it is sent again by the current range; no previous session is cached | D-169 supplementary item |
| the core's 54 slots crammed with junk | all cultivators pause (§6.3 step 4b); nothing is lost, nothing drops | D-164 |

---

## 9. Deactivating the converter (R10)

**Grounds for this section: D-170.**

### 9.1 One-line positioning

`ConverterBlock.useWithoutItem` is changed to return **`PASS`**: the right-click does nothing and no screen opens. The block, the block entity, the menu class, the **26 `farmstorm:converting` recipes** and the crafting recipe are **all kept**; only the function is stopped. plan's own words are "temporarily remove its function, leaving it as a decorative block" (`:33`).

### 9.2 Where keeping ends and deactivation begins

| Item | This version | Basis |
|---|---|---|
| right-click | returns `PASS`: no reaction, no screen, and it does not swallow the right-click (it must not return `SUCCESS` / `CONSUME`, or it would block the held item's use) | D-170 supplementary item |
| the block itself | kept (the block properties, the texture and the drops are all untouched) | D-170 |
| block entity / menu class / screen class | kept (`ConverterMenu` / `ConverterScreen` are not deleted, so a future restoration need not be rewritten) | D-170 supplementary item |
| the 26 `converting` recipes | kept in the data, merely **with no entrance calling them** | D-170; [card §1.7] |
| the crafting recipe (the converter's own 1 shaped recipe) | kept, so the player can still craft it as decoration | [`ModRecipeProvider.java:111-120`] |
| the creative-tab entry | **kept** (a decorative block has to be obtainable) | D-170 supplementary item; [`FarmstormItems.java:121-129`] |
| the language key (the block's name) | **keeps its current value**: plan writes "农作物转换器" while the language key's current value is "农产品转换器", and **this version does not change the language key**; the body text uses "converter" to refer to it, **which is not a difference** | [`ModLangProvider.java:94`]; the v0.2.0 finalisation |

**The 26 `converting` recipes' coverage** (for 36 to check, not to change them): 20 crop items + 6 seed items; `compressed_puffed_wheat` goes through another shapeless recipe, and `friend_chives` and `chive_seeds` have no converting recipe [`ModRecipeProvider.java:67-99`]. Total `26 + 2 shaped + 1 shapeless = 29` recipes [card §1.7].

**One knock-on that must be stated** ([author's supplement]): the old route "vanilla crops → this mod's crops" is cut together with R10. In 0.1.1 a player used the converter to turn wheat into Puffed Wheat and carrots into Blast Carrots… and **R2** cuts planting off as well, so with the two stacked the crop entrances in v0.2.0 are down to four (§3.4 last row). This is not a defect but the result of plan's two requirements stacking; it must nevertheless appear in the table of player-visible changes (§11 row 3), otherwise an upgraded player will not be able to find any crops.

### 9.3 Close-out: the registry surface and the red lines (R11)

| Item | Value | Basis |
|---|---|---|
| registry surface | **`93 + 4 = 97`**: `blocks +1` (the cultivator), `block_entities +1` (the cultivator), `items +1` (the cultivator's BlockItem), `menu_types +1` (core storage); the other 8 classes `+0` | D-171; [card §3.1] |
| class by class | `items 33` / `blocks 27` / `block_entities 8` / `menu_types 2`; `entity_types 13` / `mob_effects 4` / `sound_events 3` / `recipe_types 1` / `recipe_serializers 1` / `creative_tabs 1` / `damage_types 3` / `data_components 1` | D-171 supplementary item |
| expression | `33 + 27 + 8 + 2 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 = 97` | [author's supplement: arithmetic; the 12 addends come one by one from 34's D-171 class-by-class breakdown] |
| Mixin | **0**, still forbidden (the only red line kept) | D-172; [card §0] |
| custom packets | **2** in total, **1 new** in this version (the link); no numerical cap, but every new packet needs one decision + one registration | D-172 |

⚠️ **These 97 have to be checked again by 36 itself, class by class from the source**, and must not be copied from this row (`BRIEF` §3.1's ⚠️, §6 hard prohibition item 1). What this document gives is 34's D-171 class-by-class breakdown and expression, not a check done in 36's place.

**Why R11 sits here**: the registry surface and the red lines only line up after R1 ~ R10 have all landed (`menu_types +1` goes to core storage only and the cultivator registers no menu; `data_components` is still 1 because tier is not a data component; packets do not enter `BuiltInRegistries`), so it is a close-out item and does not take a section of its own.

---

## 10. Master table of numbers (cross-requirement, with a "Basis" column)

Every number is given with its source. The source is of only four kinds: a decision number in 34, the fact card's / the source code's `file:line`, a config key name + default, or an [author's supplement] with the expression written out.

| # | Value | Meaning | Source |
|---|---|---|---|
| 1 | **22** | the number of crop profiles (`CropProfile.ALL`) | D-149; the existing assertion at `Farmstorm.java:112-113` |
| 2 | **11 / 7 / 4** | the three tiers' entry counts (Common / Rare / Epic) | D-149; `BRIEF` §3.2 |
| 3 | **`11 + 7 + 4 = 22`** | the tiering total | [author's supplement: arithmetic] |
| 4 | **18 = 11 + 7** | the crops the cultivator accepts | D-162 supplementary item |
| 5 | **4 = 22 − 18** | the Epic crops it rejects | D-162 |
| 6 | **7** | standalone seed items (3 Common + 2 Rare + 2 Epic) | `BRIEF` §1.2; `CropProfile.java:47-128` |
| 7 | **5 = 3 + 2** | crops inside the admission surface that yield "2 crops + 2 seeds" | D-163 supplementary item |
| 8 | **13 = 18 − 5** | crops inside the admission surface that yield 2 crops only | D-163 supplementary item |
| 9 | **23 = 18 + 5** | the total number of item ids the cultivator accepts (crops + seeds) | [author's supplement: arithmetic] |
| 10 | **29 = 22 + 7** | items that get a name color and a tooltip line | D-151; [author's supplement: arithmetic] |
| 11 | **white / blue / gold** | the three tiers' name colors | D-151; author's ruling #14 |
| 12 | **`#FFFFFF` / `#5555FF` / `#FFAA00`** | the three tiers' color values (taken from the vanilla `ChatFormatting` constants) | [author's supplement]; corresponding one to one with §10 row 11 |
| 13 | **15** | crop blocks | `FarmstormBlocks.java:26-43`; `BRIEF` §0 |
| 14 | **8** | deployable blocks | `FarmstormBlocks.java:46-66` |
| 15 | **16 / 64** | the stack tier (throwables / the rest); unrelated to tier | `CropProfile.java:43,45` (the existing constants of D-16) |
| 16 | **19 → 11** | the Lucky Berry pool's entry count | D-156 |
| 17 | **`19 − 8 = 11`** | the pool's expression | D-156 supplementary item |
| 18 | **8 = 6 + 2** | entries removed from the pool (6 Rare + 2 Epic) | D-156 supplementary item |
| 19 | **3** | exclusions that were never in the pool (`compressed_puffed_wheat` / `lucky_berry` / `wild_apple`) | `CropProfile.java:139-143` |
| 20 | **11** | the new assertion value in the startup self-check (the old one 19) | D-156; `Farmstorm.java:119` |
| 21 | **3** | the number of farmer trade items | D-157 |
| 22 | **2–5 / 10–32 / 40–64** | the three tiers' emerald bands (the defaults of 6 config keys) | D-159; `BRIEF` §3.4 |
| 23 | **1** | the trade items' `maxUses` | D-158 |
| 24 | **2** | crops given per completed trade | [author's supplement]; §4.3 |
| 25 | **600** | the cultivation cycle (ticks) = `cultivator.cycleTicks`'s default | D-163; `BRIEF` §3.4 (card supplement) |
| 26 | **30 seconds** | `600 ÷ 20` | D-163; plan `:21` |
| 27 | **2** | crops per cycle = `cultivator.yield`'s default | D-163 |
| 28 | **2 or 4** | the total number of items per cycle (2 crops; or 2 crops + 2 seeds) | D-163; §6.4 |
| 29 | **`5.0F / 1200.0F`** | the cultivator's hardness / blast resistance (following the anvil) | D-160; `Blocks.java:3053` |
| 30 | **16** | animation frames | D-161; `BRIEF` §1.8 |
| 31 | **2** | `frametime` (ticks / frame) | D-161; `BRIEF` §1.8 |
| 32 | **32 ticks / 1.6 seconds** | one animation cycle (`16 × 2`; `32 ÷ 20`) | D-161; `BRIEF` §1.8 (the README states the same value) |
| 33 | **32 × 512 / 32 × 512 / 32 × 32** | the three faces' texture sizes (side / top / bottom) | `BRIEF` §1.8 |
| 34 | **3 + 2 + 2** | the resource increment (3 PNGs + 2 mcmeta + 2 static first frames) | D-161 supplementary item; 34 §9 ⑤ |
| 35 | **16** | cultivators per core = `outpost_core.cultivatorCapacity`'s default | D-165; author's ruling #8 |
| 36 | **0 or 1** | one cultivator's binding count | D-165 |
| 37 | **54** | the core storage's slot count = `outpost_core.storageSlots`'s default (a double chest) | D-166; author's ruling #8 |
| 38 | **`27 × 2 = 54`** | the double chest's composition (a vanilla single chest is 27 slots) | [author's supplement: arithmetic + the vanilla chest's existing slot count] |
| 39 | **1** | `menu_types`'s increment (core storage only) | D-166, D-171 |
| 40 | **3 × 3 chunks** | the core's influence range (`RANGE_CHUNKS = 1`) | `OutpostCoreBlockEntity.java:53,151-156` |
| 41 | **48 blocks** | the side length of 3 × 3 chunks (`16 × 3`) | [author's supplement: arithmetic]; `OutpostCoreBlockEntity.java:56` has the same value |
| 42 | **48** | blocks per ring of boundary particles (unchanged) | `OutpostCoreBlockEntity.java:56` |
| 43 | **40 ticks** | the boundary particles' interval (unchanged) | `FarmstormConfig.java:916-918` |
| 44 | **2** | the per-player core cap (unchanged) | `FarmstormConfig.java:913-915` |
| 45 | **`30.0F / 1200.0F`** | the core's hardness / blast resistance (unchanged) | `FarmstormBlocks.java:86` |
| 46 | **2 + 1** | the number of deactivated listeners + the number of startup-backfill lines | D-153; author's ruling #3 |
| 47 | **7** | the farmland system's deactivation landing points (§7.2) | D-153; [author's supplement: counted point by point] |
| 48 | **3** | the deactivated farmland language keys | D-153; `ModLangProvider.java:403,412-413` |
| 49 | **555 / 225** | the line counts of the two classes kept without callers | `BRIEF` §0 |
| 50 | **`93 + 4 = 97`** | the registry surface total | D-171; `BRIEF` §3.1 (⚠️ 36 rechecks class by class) |
| 51 | **33 / 27 / 8 / 2** | the after-change values of `items` / `blocks` / `block_entities` / `menu_types` | D-171 |
| 52 | **`166 − 1 + 2 + 2 + 6 = 175`** | the config key count (delete `farmlandBonus`; add 2 + 2 + 6) | D-159, D-163, D-165, D-166; 34 §3.4 |
| 53 | **`58 + 2 = 60`** | the config group count | 34 §3.4 [author's supplement: arithmetic] |
| 54 | **`323 − 3`** | the language-key baseline minus the 3 deactivated (the new ones are named by 36) | `BRIEF` §0; D-153 |
| 55 | **26 / 29** | the `converting` recipe count / the total recipe count | `BRIEF` §1.7; `ModRecipeProvider.java:67-99` |
| 56 | **2 / 1** | packets in total / new in this version | D-169, D-172 |
| 57 | **0** | the number of Mixins | `BRIEF` §0 |
| 58 | **4** | the number of criteria for a breaking drop (really removed / not a piston / not the same block / an actual container) | D-167; `ConverterBlock.java:88-96` |

Three notes under the table:

1. **The 16 in row 35 and the 54 in row 37 are not the same thing** (author's ruling #8, RT-44): 16 counts cultivators, 54 counts slots; when the former is full nothing more can be bound, when the latter is full the cultivator pauses. In the documents they are always spoken of separately.
2. **The 175 in row 52 is a skeleton expression**, with the key-by-key naming done by 36; this document uses it only to show that "the quantity does not go into config" (§4.3 reason ③).
3. **This table contains no deleted keys**: `outpost_core.farmlandBonus` is deactivated in this version (D-153), so it appears only in the "deleted" explanations and never in any "current value / after" column.

---

## 11. Master table of player-visible behaviour changes (against 0.1.1)

| # | What 0.1.1 is | What v0.2.0 becomes | What the player will notice | Decision |
|---|---|---|---|---|
| 1 | this mod's crops can be planted on farmland (both paths work) | both paths are cut and **crops can never be planted again** | "it just will not go in"; right-clicking farmland does nothing | D-152 |
| 2 | right-clicking farmland with a deployable crop = plant a crop | the same action = **place a turret / trap / fungus** | right-clicking one's own field leaves a deployable block on the ground, and the field is still a field | D-152 supplementary item |
| 3 | crop sources: planting + the converter | only the cultivator / trading / Lucky Berry / the existing recipes and drops | after upgrading one has to find a villager first or build a cultivator first, or the crop supply dries up | D-152, D-170 |
| 4 | farmland can be hoed only inside an allied core's 3 × 3, and going beyond is blocked | **farmland can be hoed anywhere**, no longer blocked | those two action-bar messages never appear again | D-153 |
| 5 | "quota = 16 × number of allied cores", with a message when it is full | the concept of a quota disappears entirely | the overlay and the messages no longer have a quota item | D-153 |
| 6 | bound farmland cannot be trampled | **there is no trample protection any more** | running over one's own field turns it into dirt | D-153 |
| 7 | vanilla crops were restricted along with the same gate | vanilla crops can be planted anywhere | at last one can grow wheat outside the base | D-153 |
| 8 | a legacy save's `farmstorm_farmland.dat` was read and backfilled | not read, not written, not deleted | an unused `.dat` is left in the save directory | D-154 |
| 9 | right-clicking the Outpost Core did nothing | **right-clicking opens the 54-slot storage screen** | the core has become a large chest | D-166 |
| 10 | breaking the core dropped only the block itself | **all 54 slots drop** | a pile of things appears on the ground when a core is broken | D-167 |
| 11 | there is no such block as a cultivator | a new cultivator: 600 ticks yielding 2 crops (2 more seeds for 5 of the crops) | the base has a machine that flips pages, and its output goes straight into the core | D-160, D-163 |
| 12 | — | the cultivator takes Common / Rare only | right-clicking with an Epic crop does nothing and no error is shown | D-162 |
| 13 | — | the cultivator pauses when the core is full and nothing is lost | the machine stops dead (back to the static image) and carries on by itself once room is cleared | D-164 |
| 14 | the core bound farmland, 16 plots per core | the core binds cultivators, 16 per core | after a core is broken its cultivators either change core or stop working for good (they do not turn to dirt) | D-165, D-168 |
| 15 | the core had only a ring of boundary particles | a **visible link** is added between the core and the cultivators bound to it | standing beside the core shows the lines; walking away makes them go | D-169 |
| 16 | the Lucky Berry pool had 19 entries (Rare and Epic included) | the pool has 11 and **yields Common only** | Rare and Epic crops can no longer be eaten out of a berry | D-156 |
| 17 | right-clicking the converter opened a screen and it converted | **right-clicking does nothing** (the block is still there and still craftable) | "why does my converter not open any more" | D-170 |
| 18 | item names were all white | Common white / Rare blue / Epic gold + one tooltip line `品质：…` | crops in the inventory have a color grading | D-151 |
| 19 | farmers did not sell this mod's crops | 3 trade items unlocked by level, out of stock after one sale and restored by a restock | the farmer's list has 3 more items, and after one purchase it will not sell again | D-157, D-158, D-159 |

**The three headlines**: row 1 (crops can no longer be planted) and row 9 (the core has become a chest) are what every player will run into; row 3 is the one most in need of explanation at the moment of upgrading — **two old acquisition routes disappear at once** (planting + the converter), so the player has to be told where the new entrances are. Rows 17 (the converter will not open) and 16 (berries give Common only) are the "the thing is still there, the function is gone" kind, and are the ones most easily misreported as bugs.

---

## 12. Boundary list (table, from B-01)

Numbering starts at B-01. Each entry gives only "scenario / expected behaviour / basis / how it shows when not met"; the numbers are looked up in their own sections.

| No. | Scenario | Expected behaviour | Basis | How it shows when not met |
|---|---|---|---|---|
| B-01 | right-clicking farmland with one of the 15 `ModifiedCropItem`s | no reaction (`PASS`), the item is not consumed | D-152 | a crop can still be planted |
| B-02 | right-clicking farmland with one of the 7 deployable crops | the deployed form is placed and **no crop is planted** | D-152 supplementary item | either a crop is planted out of thin air or the right-click fails entirely |
| B-03 | right-clicking farmland with one of the 7 seeds | no reaction | D-152 | seeds can still be planted; `RT-48` is half missed |
| B-04 | wanting to plant a crop in creative mode | equally unplantable (the creative tab hands out items only, and the item side has no placement path) | D-152, D-155 | creative mode can plant ⇒ there is still an entrance on the block side that was not closed |
| B-05 | placing a crop block with the `/setblock` command | still possible (the block is kept; `mayPlaceOn` / `canSurvive` are not overridden) | D-155; `BRIEF` §1.2 | "cutting the player's route" is written as "the block cannot exist" |
| B-06 | a crop already growing in a legacy save | present and harvested as usual (the loot tables are untouched) | D-155 | the crops in old fields vanish after the upgrade |
| B-07 | naturally generated crops from data packs / structure injection | can still appear; this version does not handle them (registered as known) | D-155 supplementary item | this is reported as a defect |
| B-08 | right-clicking the cultivator with an Epic crop or an Epic seed | no reaction, no consumption, no message | D-162 | it can go in ⇒ Epic has a second entrance |
| B-09 | right-clicking the cultivator again while the slot is occupied | no reaction (no stacking, no replacing, no squeezing out) | D-162 supplementary item | the 1st is swallowed or replaced |
| B-10 | breaking the cultivator while the slot holds a cultivated item | drops itself + the cultivated item inside | D-160 supplementary item | the crop inside is permanently swallowed |
| B-11 | right-clicking the cultivator empty-handed | no reaction, **nothing is taken out** (getting it back means breaking the block) | [author's supplement] | the player thinks he can take it out, or the progress resets after taking it out |
| B-12 | no allied core nearby when the cultivator is placed | the block is there and visible, **unbound and unusable** | D-165 | an unbound one can be used ⇒ the binding gate is void |
| B-13 | placing a core next to an unbound cultivator **afterwards** | **no automatic claiming** (writing happens only on placement and on migration) | D-165 supplementary item | runtime automatic drift ⇒ a second binding truth appears |
| B-14 | a core already has 16 bound and a 17th is placed | the 17th is unbound and unusable, and **does not squeeze out** a bound one | D-165 | the 17th replaces one of the bound ones |
| B-15 | the core's 54 slots cannot hold the whole batch at output time | **pause**: progress stops, the slot is not cleared, nothing drops, nothing is lost | D-164 | the output drops on the ground or simply vanishes |
| B-16 | the core frees space after the pause | delivery continues from where it paused, **without restarting the timer** | D-164 | another 600 ticks are waited |
| B-17 | the cultivator's chunk unloads / the server stops | progress is written to disk and continues from where it was after reload | [author's supplement] | the progress resets on every reload |
| B-18 | the player leaves the core's 3 × 3 range | what is already in produces as usual (the four gates are judged only at the instant of the right-click) | [author's supplement] | it stops the moment one leaves ⇒ the farm would need a guard |
| B-19 | the bound core is broken and a successor exists | rebind; the slot's contents and progress are kept | D-168 | the migration clears the slot |
| B-20 | the bound core is broken and there is no successor | stay where it is, contents kept, marked unbound, **unusable, producing nothing** | D-168 | it turns to dirt / is broken / its contents vanish |
| B-21 | when the core is broken the cultivator's chunk cannot be read | keep the NBT's original values, no guessing, no deleting the binding | D-168 supplementary item | it is bound to the wrong core, or the binding is silently deleted |
| B-22 | a non-allied player right-clicking the core | no reaction | D-166 supplementary item | the enemy can open your warehouse |
| B-23 | an allied player right-clicking the core but already far away (beyond the vanilla container's reach) | **it does not open** (the second gate is exactly the vanilla `stillValid` default reach check; no new radius, no config read) | D-166 supplementary item; ruled at the v0.2.0 finalisation; §8.3 | the chest opens from half a map away ⇒ it conflicts with the semantics of "an allied outpost's storage" |
| B-24 | two allied players opening the same core at once | the same 54 slots, server-authoritative, no duplication | [author's supplement] | each gets a copy ⇒ item duplication |
| B-25 | a legacy core with only the three owner fields | read as an empty container + 0 bindings, no error, no save wiped | D-166 supplementary item | reading the save fails, or the surrounding blocks are cleared |
| B-26 | a piston pushing the core / same-block replacement / chunk unload | **none of them drops** | D-167 | item duplication |
| B-27 | the core is broken with nothing in it | only the block itself drops | D-167 | every core break spawns a pile of items |
| B-28 | the pool changed to 11 but the startup assertion still 19 | the startup log prints FAIL (the server still starts; the assertion only writes a log) | D-156; `Farmstorm.java:118-123` | the permanently red FAIL is taken as noise and a real fault gets buried |
| B-29 | eating a Lucky Berry and hitting the "generate random crops" branch | drawn with equal probability from the 11 Common only, no weights | D-156 | Rare / Epic still get rolled |
| B-30 | farmer trading: clicking the same item again after one trade | out of stock (the item is still there and cannot be clicked) and restored after a restock | D-158 | the trade item disappears, or it can be traded without limit |
| B-31 | farmer trading: after a restock | tradeable again, **with the price and the crop id unchanged** (the offer is not re-rolled) | D-158, D-159; [author's supplement] | a restock turns it into another offer and the player cannot plan around it |
| B-32 | farmer trading: a teammate buys it first | out of stock belongs to the villager, not to the player; what you see is a sold-out item | D-158; [author's supplement] | one each per player ⇒ conflicts with "once" |
| B-33 | farmer trading: a max-level farmer in a legacy save | it never levels up again ⇒ the new trade items are **not granted**; a new villager is needed, or a villager that is not yet max level levelling up | [author's supplement]; RT-46 | it is assumed that every old villager automatically gets 3 more items |
| B-34 | farmer trading: wanting to buy one particular Rare / Epic | trading is **sampling** (a cost the author has confirmed he accepts): whichever entry is drawn is the one sold, and a named id is not guaranteed; determinism can only come from adding trade items, and **this version adds none** | §4.4; confirmed at the v0.2.0 finalisation | trading is treated as a catalogue ⇒ the player goes back to the same villager again and again, or misreports "this villager is broken" |
| B-35 | right-clicking the converter | no reaction; the block, the 26 recipes and the creative-tab entry are kept | D-170 | the right-click is swallowed, or the recipes are deleted along with it |
| B-36 | renaming a crop on an anvil / reading a sign / the item name in the chat column | it still carries the tier color (the color hangs on the `Component`'s style) | D-151 supplementary item | it is colored only in the inventory |
| B-37 | tier versus stacking / drops | neither is affected (stacking is still 16 / 64 and the 26 loot tables are untouched) | D-150, D-155; §2.6 | the same id shows two different stacking limits |
| B-38 | the link: the player leaves the range | the client clears the leftovers and does not cache across dimensions | D-169 supplementary item | a distant line hangs there for good |
| B-39 | the link: the core or a cultivator is broken | the corresponding line is retracted | D-169 supplementary item | a line points at a block that no longer exists |
| B-40 | someone restores the comments on the farmland listeners | the old rules come back at once (the reason line at each comment is the only line of defence) | RT-42, D-153 | they come back silently and nobody knows why the field is blocked again |

40 entries. B-01 ~ B-07 belong to R2, B-08 ~ B-21 to R5 / R7 / R8, B-22 ~ B-27 to R8, B-28 ~ B-29 to R4, B-30 ~ B-34 to R3, B-35 to R10, B-36 ~ B-37 to R1, B-38 ~ B-39 to R9, and B-40 to R6.

---

## Appendix A · Register of differences between plan's original text and this version's measure

| # | plan line | plan's original text | This version's measure | Nature of the difference | Decision | Explanation |
|---|---|---|---|---|---|---|
| 1 | `:2`, `:5` | "Version: 0.1.1", "this version is based mainly on version 0.1.0" | **0.2.0**, with the baseline being **0.1.1's published state** | **stale value (changed per the ruling)** | D-148 | author's ruling #1 states clearly that these two places copied a stale value. Taking them literally would give this version the same name as the published 0.1.1, and the jars could not be told apart (34 §9 ⑬) |
| 2 | `:10-12` | the three-tier list totals **19** crops (Common 11 + Rare 4 + Epic 4) | **22** (Common 11 + Rare 7 + Epic 4), with `ubi_server`, `toxic_ubi_server` and `point_defense_fungus` added to Rare | addition (+3) | D-149 | plan's list was written before v0.0.3, and what is missing is exactly the 3 profiles added in v0.0.3; the difference between `11 + 4 + 4 = 19` and `11 + 7 + 4 = 22` is those 3 (34 §4.1) |
| 3 | `:14` | "each level stage unlocks one more… and only one trade is allowed each time" | only **3** trade items (one per tier), unlocked at three successively higher ranks only; the ranks in between **get none** | narrowing | D-157, D-158 | "each level stage" is narrowed by author's ruling #6 to three tiers at three ranks (a vanilla level ladder has more than 3 ranks, 34 §9 ⑪) |
| 4 | `:14` | "only one trade is allowed each time" | **out of stock + waiting for the vanilla restock** (`maxUses = 1`); the trade item is not deleted, not hidden, not re-idded | rewrite (ambiguous wording) | D-158 | translated literally as "disappears after a trade" it would leave the restock with nothing to carry (tombstone §7 item 4, 34 §7 item 4) |
| 5 | `:21` | "after thirty seconds it produces 2 of the corresponding crops and 2 seeds" | **2 crops + 2 seeds happens for only 5 crops** (those inside the admission surface with a standalone seed item); the other **13 give 2 crops only** | refinement | D-163 | the 22 profiles fall into three classes (7 with a standalone seed / 8 seed = itself / 7 no seed); giving 2 more to the 8 "seed = itself" ones would conjure items out of thin air (34 §9 ⑧) |
| 6 | `:21` | "usable if and only if the block is inside an allied Outpost Core's influence range and is bound by an allied Outpost Core" | an unbound cultivator **can still be placed and is visible**, it simply cannot be used; a binding is written only "on placement" and "when the core is broken" | refinement | D-165 | "if and only if… usable" speaks of use, not of existence; refusing even placement would leave the player with an inexplicable "it will not go down" |
| 7 | `:21` | "right-clicking with a Common- or Rare-quality crop of this mod, or its seeds, puts it in" | **23** acceptable items (18 crops + 5 seeds); the 4 Epic crops + 2 Epic seeds are **refused without exception** | refinement | D-162 | only 5 seed items fall inside the admission surface (3 Common + 2 Rare) and the other 2 are Epic (§6.2) |
| 8 | `:21` | "plays the preset page-flip animation while it is in the working state" | working state = the slot holds something and it is not paused because the core is full; idle and paused **show the static first frame** (two textures switched) | refinement | D-161 | an mcmeta animation is texture-level and the code cannot reach a pause switch ⇒ 2 static first-frame images are needed (34 §9 ⑤) |
| 9 | `:22` | the texture's absolute path `C:\Users\…\cultivator_a_animation_pack` | copy in **3 PNGs + 2 mcmeta + 2 static first-frame images**, with the file names unchanged and the model `cube_bottom_top` | refinement | D-160, D-161 | the path is an address on the author's machine; what the repository recognises is only the file names under `assets/farmstorm/textures/block/` (`BRIEF` §1.8) |
| 10 | `:26` | "revoke every change this mod made regarding farmland" | **comment out 7 call sites only**: no class deleted, no save data deleted, no block deleted | rewrite (narrowed scope) | D-153, D-154 | what author's ruling #3 chose is "comment out the call sites only"; "every change" taken literally would delete the 555 + 225 lines as well (tombstone §7 item 1) |
| 11 | `:27` | "has a list of capacity 16… used to store the information of the crop cultivators bound to it" | 16 = the **cultivator allowance**; storage is another matter, **54 slots** | refinement (the two numbers must be kept apart) | D-165, D-166 | taking 16 as the slot count would contradict the next line's "equivalent to a large chest" (tombstone §7 item 3, author's ruling #8) |
| 12 | `:27` | "the crop cultivators bound to it will look for other allied Outpost Cores they can bind to, otherwise that crop cultivator cannot be used" | three exits: rebind / keep and mark unbound / skip defensively; **there is no "turn to dirt" branch** | refinement | D-168 | 0.1.1's isomorphic logic (D-136) did `setBlock(DIRT)` when migration failed; this version does not touch the world when it fails (RT-43) |
| 13 | `:28` | "right-clicking the Outpost Core opens its built-in storage space (equivalent to a large chest)" | a 54-slot double chest; **allied ∧ the vanilla reach** as two gates; a legacy save is read as an empty container | refinement + [author's supplement] (who can open it) | D-166 | plan does not say who can open it: 34's original text wrote only "allies only", and the v0.2.0 finalisation added the second gate "the vanilla `stillValid` reach" (no new radius, no config key) |
| 14 | `:29` | "when the player is inside an allied Outpost Core's influence range… a visible link is produced" | the range = that core's **3 × 3 chunks**; the data goes through **a new 2nd packet**; the client is read-only and clears on leaving the range | refinement | D-169 | "influence range" has a single value in this project (`inRange`, `RANGE_CHUNKS = 1`) and is not a new range invented here |
| 15 | `:30` | "when the Outpost Core is destroyed, all the items it stores drop" | drops only on **real removal**; a piston push / same-block replacement / chunk unload **do not drop** | refinement | D-167 | the criterion follows `ConverterBlock.java:88-96`'s four conditions, otherwise a piston duplication exploit appears |
| 16 | `:33` | "temporarily remove its function, leaving it only as a decorative block" | right-click returns `PASS`; the block, the 26 `converting` recipes, the menu class and the creative-tab entry are **all kept** | refinement | D-170 | "temporarily" is the reason for keeping the menu class and the recipes; delete them and a future restoration would have to rebuild them |

Three notes under the table:

1. **Beyond the four rows whose differences must be named individually, the rest are refinements or narrowings**: row 1 is a stale value (changed per the ruling), row 4 is ambiguous wording (rewritten per the ruling), row 3 is a narrowed scope (per author's ruling #6) and row 10 is a narrowed scope (per author's ruling #3). These four rows must not be written as "by the way", "not implemented" or "relaxed".
2. **The two [author's supplement] entries appear only in the refinements of rows 13 and 14** (who can open the chest, and what the range's value is), and both can be traced back to a decision number in 34; not one of them is a new measure without grounds.
3. **Everywhere this document departs from plan, this table has named it**; a departure with no decision number behind it counts as a document defect.
