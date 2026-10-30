# 18 · v0.0.3 Technical Specification

> This document is the technical specification for "Farmstorm" v0.0.3: the registry increment, the class designs, the two Q-Bounce branches, the three mechanism plans,
> the full config and resource inventories, the risk register RT-17 ~ RT-23, and the acceptance self-check table.
> The single source of truth is `docs/zh/16-DECISIONS-0.0.3.md` (the text of D-80 ~ D-97 plus the supplementary adjudications D-98 ~ D-102 in §3.6); where they conflict, that file prevails.
> §1.4 of this document once registered four baseline inconsistencies, and all four are now closed: three were corrected by D-98 in §3.6 of document 16 together with the author's adjudication, and one was this document's correct reading of D-87's family count.
> Every source-code fact is taken from `src/main/java/com/dashpig/farmstorm/`; line numbers follow the v0.0.2 release code.

---

## 0. Scope of This Document and Its Relation to 12-TECH-0.0.2.md

> The doubled 「与与」 in this section's heading is transcribed verbatim from §0 of `12-TECH-0.0.2.md` (that file writes it the same way) and is not changed.

### 0.1 What This Document Is Responsible For

| Item | Content |
|---|---|
| Version | 0.0.3 (an incremental release; the build baseline is unchanged: MC 1.21.1 / NeoForge / Java 21) |
| In scope | Naming the +13 registry increments item by item; the design of new and changed classes; the two Q-Bounce sitting branches; Confusion's reversed input; Point Defense Fungus's interception; Vibration Bean's unattached block; the full config inventory; the resource and data file inventory; RT-17 ~ RT-23; the acceptance self-check |
| Out of scope | The full narrative of numeric balance and state machines (`17-DESIGN-0.0.3.md`); task breakdown and effort (`19-TASKS-0.0.3.md`); prompts (`20` / `21`) |
| Files left untouched | 00 – 15 under `docs/zh` and 00 – 15 under `docs/en` (D-80) |

### 0.2 Relation to `docs/zh/12-TECH-0.0.2.md`

This document carries over every convention established by `12-TECH-0.0.2.md` and invents none:

| Convention | Where it lives in document 12 | How this document inherits it |
|---|---|---|
| The per-registry-object table | §2.1 / §2.2 | §2 keeps the four-column reading of "registry × v0.0.2 × increment × v0.0.3" |
| Per-category subtotals that agree | the code block under the §2.1 table | §1.2 / §2.4 use the same form; the two algorithms check each other |
| The items of a class design | the opening of §3 | §3 uses four items (responsibilities / key methods / which config keys it reads / which existing files it changes); the reason is at the opening of §3 |
| The per-key config inventory | §8.1 | §8.1 keeps the same column set; the row count is counted first and stated afterwards |
| The resource inventory | §7.2 / §7.3 | §9 splits into three tables — PNG / OGG / JSON — each with its own category subtotal |
| The risk register | §10 | §10 gives RT-17 ~ RT-23, with the fields changed to five (risk / trigger / primary mitigation / fallback / how to verify during implementation) |
| The "to be verified" register | §0.5 | §0.5 is carried over; every "to be verified" item here hangs off one specific RT |
| Numeric discipline | §0.4 | §0.4 is carried over, but this document **is allowed** to state registry totals — because they are already measured values (§1) |

**Relation to document 12 in one sentence**: document 12 is the specification that "adds 13 objects on top of 0.0.1 and moves 64 to 77"; this document is the specification that "adds another 13 objects on top of 0.0.2 and moves 77 to 90", and the addition is word for word the same. There is exactly one difference: document 12 could use 64 as the baseline because 0.0.1's archive was complete; this document can use 77 as the baseline because **one line of the source's startup self-check asserts these 12 numbers** (`Farmstorm.java` L138–L141), and this document's measured values equal that assertion item by item (§1.3).

### 0.3 Self-Consistency Commitment for the Three Inventories

This document's three inventories (§2 registry objects, §8 config keys, §9 resource files) each satisfy:

1. **Row count = the declared count**: every table states "this table has N rows", and N equals the data rows counted one by one — never write the number first and pad the rows afterwards.
2. **Subtotal = the sum of the rows**: all three tables print a category subtotal below the table, and the subtotals add up to the number of table rows.
3. **No overlap and no omission across tables**: an object appears exactly once, in one table. Registry objects go into §2; config keys go into §8; textures / audio / JSON go into §9. `DeployedKind`'s enum constants are not registry objects: they appear only in §3 and enter no count.

### 0.4 Numeric Conventions

| Category | Convention | Source |
|---|---|---|
| Measured | 77 registry objects (per category in §1.2) | Counted here by reading each registry class's source item by item, and equal item by item to the 12 assertions of `Farmstorm#logRegistrySelfCheck` |
| Measured | 139 config keys | `FarmstormConfig`'s 133 `defineInRange` + 4 `define` + 2 `defineEnum/defineList` = 139; `assets/farmstorm/lang/en_us.json`'s `farmstorm.configuration.*` is 139 as well |
| Measured | 263 language keys | the actual output of §3 of `tools/verify_project.py` (263 each in Chinese and English, identical key sets) |
| Measured | 19 crop profiles / 16 lucky berry pool entries | `util/CropProfile.java`'s `ALL` (19 entries) and `LUCKY_BERRY_POOL` (19 − 3 entries) |
| Increment | +13 (registry) / +16 (config keys) / +34 (language keys) / +8 PNG / +1 OGG | §2 / §8 / §9 of this document |
| Target | 90 registry objects, 155 config keys, 297 language keys | measured value + increment, itemized in §2.4 / §8.3 / §9.4 |
| Forbidden | any registry total not backed by this section's two pieces of evidence | D-80's numeric discipline; §1 of this document has already lifted that ban (both the tool gap and the measured values are given) |

### 0.5 Registration Rule for "To Be Verified"

This document invents no Minecraft / NeoForge API. Wherever this document cannot find a precedent for a call in the existing source, that place is marked "**to be verified**" and the bracketed text names the RT number it hangs off. There are 9 such places in this document, all concentrated on the four RTs in §10 (RT-17 four, RT-18 two, RT-20 one, RT-22 two — 9 in total). A call marked "to be verified" must first be proven with a minimal case during implementation before the real logic is written; if it cannot be made to work, switch to the alternative path written in that RT's "fallback" column.

---

## 1. Baseline Verification (measured existing registry objects + the verify_project.py gap)

### 1.1 Counting Method

**Do not count with grep.** `grep "register"` would count factory methods, comments and `DeferredRegister` field declarations together — this document deliberately avoids that road. The three evidence chains used are independent of one another:

1. **Read every construction site item by item**: open each `registry/*.java` and count only the calls that "actually produce one registry entry" — the `REGISTER.register(...)` / `REGISTER.registerItem(...)` / `REGISTER.registerBlock(...)` / `REGISTER.registerComponentType(...)` / `REGISTER.registerDataComponents(...)` series, plus every call site of the **factory methods** `cropItem(...)` / `seedItem(...)` / `crop(...)` / `deployed(...)` / `projectile(...)`. A factory method is not itself an entry: `FarmstormEntityTypes.projectile(String, EntityTypeFactory)` is one method, it is called 10 times, so it yields 10 entries and not 11. This one point is the whole of this counting method.
2. **Cross-check against the startup self-check**: `Farmstorm.java`'s `logRegistrySelfCheck()` (L123–L147) counts the 12 classes separately on every startup — it counts `DeferredRegister#getEntries().size()`, not a hard-coded constant — and asserts `total == 77` and that each class equals a concrete set of numbers (L138–L141). That is the second independent ledger, a **runtime** one.
3. **Cross-check against the existing document**: the 12-row table of `12-TECH-0.0.2.md` §2.1 (the v0.0.1 column + the increment column + the v0.0.2 column).

All three pieces of evidence agree ⇒ the baseline holds. If any one disagrees ⇒ record it in §1.4.

### 1.2 Per-Category Measured Values (v0.0.2 baseline, 77 in total)

This table has 12 rows and states "what was counted" per category. Column 4 is this document's measured value, column 5 is the v0.0.2 column of `12-TECH` §2.1, column 6 is the asserted value of `Farmstorm#logRegistrySelfCheck` — the three columns are equal item by item.

| # | registry | What was counted (the basis for naming them one by one) | Measured | Document 12 | Startup self-check |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 19 crop items (15 from 0.0.1 + 4 from 0.0.2: `molotov` / `zangief_cane` / `wild_apple` / `compressed_puffed_wheat`) + 6 standalone seed items + 2 block items (`converter` / `compressed_hay_block`) | **27** | 27 | 27 |
| 2 | `Registries.BLOCK` | 14 crop blocks (`crop(profile)` called 14 times) + 6 deployed blocks (`deployed(...)` called 6 times) + `converter` + `compressed_hay_block` | **22** | 22 | 22 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 5 deployed types (`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus`) + `converter` | **6** | 6 | 6 |
| 4 | `Registries.ENTITY_TYPE` | 10 calls to `projectile(id, factory)` (8 from 0.0.1 + `molotov` + `compressed_puffed_wheat`) + `turret_bullet` | **11** | 11 | 11 |
| 5 | `Registries.MOB_EFFECT` | `wild_hunter` + `zangief_piledriver` | **2** | 2 | 2 |
| 6 | `Registries.SOUND_EVENT` | `wild_hunter_loop` + `zangief_piledriver` | **2** | 2 | 2 |
| 7 | `Registries.RECIPE_TYPE` | `converting` | **1** | 1 | 1 |
| 8 | `Registries.RECIPE_SERIALIZER` | `converting` | **1** | 1 | 1 |
| 9 | `Registries.CREATIVE_MODE_TAB` | `main` | **1** | 1 | 1 |
| 10 | `Registries.DAMAGE_TYPE` | Data-driven; the code side holds only a `ResourceKey`: `void_bolt` + `crop_blast` | **2** | 2 | 2 |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `temporary_enchantments` | **1** | 1 | 1 |
| 12 | `Registries.MENU` | `converter` | **1** | 1 | 1 |
| | **Total** | | **77** | 77 | 77 |

Per-category subtotals (for cross-checking; must equal the table above row by row):

```
items                 27
blocks                22
block_entities         6
entity_types          11
mob_effects            2
sound_events           2
recipe types           1
recipe serializers     1
creative tabs          1
damage_types           2
data_components        1
menu types             1
--------------------------------
sum = 27+22+6+11+2+2+1+1+1+2+1+1 = 77
```

**Verification conclusion**: the per-category measured values add up to 77, the v0.0.2 column of `12-TECH` §2.1 is 77 as well, and the assertion of `Farmstorm#logRegistrySelfCheck` is likewise 77. The three independent algorithms cross-check one another, and **77 is this document's baseline**.

### 1.3 Cross-Check: 19 / 16 / 22 and the Registry Total

| Quantity being cross-checked | What the document claims | Measured here | Conclusion |
|---|---|---|---|
| Number of `CropProfile` entries | §4 of `10-DECISIONS-0.0.2.md` has 19 rows; §4 of `16-DECISIONS-0.0.3.md` states "that table has 19 rows" | `CropProfile` has 19 `public static final CropProfile` constants, and `ALL` has exactly 19 entries | Consistent |
| Lucky berry random pool | D-55 / D-97: 19 − 3 = 16, and from v0.0.3, 16 → 19 | `LUCKY_BERRY_POOL` = `ALL` with three entries filtered out = 19 − 3 = 16 | Consistent (v0.0.3 only adds profiles and does not change the derivation, so it becomes 19 automatically) |
| Crop type distribution | `Farmstorm`'s self-check asserts `throwable == 10 && deployable == 6 && edible == 3` | 19 = 10 + 6 + 3; Demolition `demolition == 4` | Consistent |
| Total registry objects | the title of §2 of `12-TECH-0.0.2.md`: "77 object verification" | 77 (§1.2) | Consistent |
| Number of `sounds.json` events | §8 of `16-DECISIONS`: "2 → 3" | 2 events in `sounds.json` (`wild_hunter_loop` / `zangief_piledriver`) | Consistent |
| config key count | §1 of `16-DECISIONS`: 139 (already verified) | 133 + 4 + 2 = 139 definitions; the `farmstorm.configuration.*` language keys number 139 as well | Consistent |
| language key count | §1 of `16-DECISIONS`: 263 | §3 of `verify_project.py` outputs 263 (identical key sets in Chinese and English) | Consistent |

### 1.4 The Four Inconsistencies Found by the Baseline Verification (**all four are closed**)

**Each one states which side may be wrong, and gives the implementation-phase decision at the same time, so that no uncertainty is left for later.**

**Status table (last updated at the end of 2026-02; all four were settled, no loose ends were left)**:

| Item | Matter | Status | Basis for the ruling |
|---|---|---|---|
| (a) | The `ThrownCropEntity` family: 9 vs 10 | **Closed** | D-98 in document 16 corrected it to 10 + 2 = **12**; "9" was the 0.0.1 total for `entity_types` (8 projectiles + `turret_bullet`), which is not the same thing as the number of family members |
| (b) | The `DeployedKind` new values: +1 vs +2 | **Closed, +2 adopted** | The author adjudicated +2; the enum goes 6 → **8**; enum constants are not registry elements, so the registry total is unchanged |
| (c) | `tools/verify_project.py` enumerates everything per 0.0.1 | **Closed, fix** | Author's adjudication: fix it to the 0.0.2 baseline **77** and, after v0.0.3, **90**; the criterion of §11 #1 may not be downgraded to a manual signature |
| (d) | PNG increment +7 vs +8 | **Closed, +8** | The author confirmed the fact card had been changed to +8, the 8th file being Vibration Bean's item icon (the convention under which each of 0.0.2's three invisible traps carries one item texture) |

#### (a) The Number of Members in the `ThrownCropEntity` Family: the Document Says 9, the Source Says 10

- The document: §3.3 D-87 of `16-DECISIONS-0.0.3.md` says in its original text "only intercepts the ThrownCropEntity family (0.0.2's **9** + the 2 new ones from 0.0.3)" — that entry's original text has not changed by a single character to this day (D-98's convention is "keep the original text, open a separate entry to correct it"), so the present tense is used here; for the correction see the last line of this block and D-98 in §3.6.
- The source: `entity/ThrownCropEntity.java` has **9 direct subclasses** (`BambooFirecrackerProjectile` / `BrownMushroomPuffProjectile` / `MelonGrenadeProjectile` / `MelonSliceProjectile` / `MolotovProjectile` / `RedMushroomPuffProjectile` / `PuffedWheatProjectile` / `ToxicUbiServerProjectile` / `UbiServerProjectile`), plus **1 indirect subclass**, `CompressedPuffedWheatProjectile` (it extends `PuffedWheatProjectile`, not the base class directly). All 10 `entity_type`s are created by `FarmstormEntityTypes.projectile(...)`, whose generic bound is `<T extends ThrownCropEntity>`, so all 10 are necessarily family members.
- **Which side may be wrong**: the document's "9" was counted by **direct subclass**; D-87's criterion is written as `instanceof`, and `instanceof` counts **family members**. Each side was right by its own count, but the two readings differed.
- **Implementation decision**: follow D-87's `instanceof` reading and take **10**. After v0.0.3 lands the family holds 10 + 2 = **12** members, and §11 #8 / #13 are both verified against that base of 12.
- **Closed (D-98)**: D-98 in §3.6 of `16-DECISIONS-0.0.3.md` already corrected the family size to **12** and lists it piece by piece (the list is the 12-row table in §6.1 of this document); D-98 also restored the "non-ally" qualifier that D-87 had missed (§6.1 / §6.2).

#### (b) `DeployedKind`'s New Values: the Document Says +1, the Source Constraint Requires +2

- The document: D-86 says "`behavior/DeployedKind.java` (enum +1 value `POINT_DEFENSE_FUNGUS`)" (that entry's original text is unchanged); the "DeployedKind" column of row 22 (Vibration Bean) in §4 of `16-DECISIONS` **read "—" at the time** (no value), and has now been corrected to `VIBRATION_BEAN`.
- The source: `kindOf(BlockState)` at L107–L113 of `blockentity/DeployedCropBlockEntity.java` **throws `IllegalStateException` directly for any block that is not a `DeployedCropBlock`**, and the constructor's first line uses it to obtain the `BlockEntityType` (L103). That is to say: the path D-91 wants — "a floating trap block + reuse `DeployedCropBlockEntity`" — **can only** be a `DeployedCropBlock`; and `DeployedCropBlock`'s `kind` field is taken from `profile.kind()` in the constructor (L74), falling back to `BLAST_CARROT` when it is unavailable — which would make Vibration Bean run Blast Carrot's explosion behavior.
- **Which side may have been wrong**: that "DeployedKind —" cell in row 22 of `16-DECISIONS` §4 at the time. It was very likely filled in the context of "Vibration Bean is a Throwable and has no planted form", but in the other rows this column means the **deployment behavior dispatch key**, which is not the same as "does it have a planted form"; document 16 has now changed that cell to a value per this section's decision (`VIBRATION_BEAN`).
- **Implementation decision**: `DeployedKind` actually gains **+2 values** (`POINT_DEFENSE_FUNGUS(10)`, `VIBRATION_BEAN(10)`), and `CropProfile.VIBRATION_BEAN`'s `kind` field is filled with `VIBRATION_BEAN` rather than `null`. **This affects no registry count**: enum constants are not registry objects, and `block_entities` is still 6 (Vibration Bean joins the legal block set of the existing `BlockEntityType`, the same technique as 0.0.2 attaching `zangief_cane` to the `fang_trap_cactus` type). **Closed (author's adjudication, +2 adopted)**: row 22 of `16-DECISIONS` §4 **has been corrected to** `VIBRATION_BEAN` (with the reason "row 22's DeployedKind is mandatory" appended below the §4 table), and the fact card has been changed; the enum goes from 0.0.2's 6 → **8**. Enum constants are not registry elements, so no number in §1.2 / §2 is affected. RT-21 keeps tracking the fast-fail path of "throw when the block and the BE are mismatched".

#### (c) `tools/verify_project.py`: the Denominator 64 Disagrees with Its Own Enumeration of 62, and Everything Is Enumerated per 0.0.1

Run result (`python -X utf8 tools/verify_project.py`, exit code 1, 1 FAIL item):

| Section | Actual output | Reading |
|---|---|---|
| §1 config | document convention 117 keys, generated toml 139 keys, `[FAIL] toml 多 22 个键` | The error is in **where the expected value comes from**: the tool reads the 117 keys of `01-DESIGN.md` §3.8, whereas 0.0.2's 22 new keys live in `12-TECH-0.0.2.md` §8.1 (117 + 22 = 139, an exact match). `01-DESIGN.md` belongs to the frozen 00–15 zone and cannot be edited back, so the only fix is to have the tool read one more place |
| §2 registry | `items 22 / blocks 20 / block_entity_type 6 / entity_type 9`, `已核 registry 对象: 62 / 64` | The enumeration is 0.0.1's (22 + 20 + 6 + 9 = 57, plus 5 bare ID literals = 62); the denominator 64 disagrees with its own 62 |
| §3 language | `lang 键集一致，共 263 键` | The only section that was still correct |
| §4 prohibitions | `无 Mixin / 调试残留 / 空 catch` (not one of the four prohibited-pattern scan categories hit) | Pass |

**Which side may have been wrong**: the tool's expected values and its denominator. The implementation side was not wrong — the tool's own §3 section counts 263 and that matches the implementation.

**Closed (author's adjudication, fix)**: execute the four sections' changes in the table below; the target is the 0.0.2 baseline **77 / 77** and `结论：FAIL 0 项`; after v0.0.3 lands, tighten it to **90 / 90**, config **155**, lang **297**. The criterion of §11 #1 is exactly this output, and it **may not be downgraded to a manual signature**.

**The target of fixing it to the 0.0.2 baseline** (as an acceptance item of §11 #1):

| Section | Now | Change to |
|---|---|---|
| §1 expected key set | 117 (reads only `01-DESIGN.md` §3.8) | 139 (117 + the 22 rows of `12-TECH-0.0.2.md` §8.1) |
| §2 `EXPECTED` | items 22 / blocks 20 / entity 9 | items 27 / blocks 22 / entity 11 (add `compressed_hay_block`, `zangief_cane`, `molotov`, `compressed_puffed_wheat`) |
| §2 denominator | 64 | 77, and the per-category subtotals equal §1.2 (12 categories, see §2.1) |
| §2 bare ID literals | 5 (`converting` / `main` / `void_bolt` / `crop_blast` / `temporary_enchantments`) | Complete the remaining 5 (including the recipe serializer and the menu) so that all 12 categories go through "per-category subtotal" instead of bare literals |
| §3 language | 263 | Unchanged (after v0.0.3 lands, change to 297, see §9.4) |

#### (d) The PNG Increment: 8 and the Gap in "the Sum of the Items"

§8 of `16-DECISIONS-0.0.3.md` **once recorded** "+7 / +1 (the PNG and OGG increments)"; it has been corrected to +8 / +1. After listing every item, this document arrives at **8 PNG files**, one more than 7: the 8th is **Vibration Bean's item icon**. The reason is a source constraint and not a preference — `datagen/ModItemModelProvider.java` L46–L48 routes every item without a vanilla parent model through `basicItem(item)`'s fallback branch, and the model `basicItem` generates always references `farmstorm:item/<id>`; that is exactly why 0.0.2's three invisible traps (`blast_carrot` / `fang_trap_cactus` / `zangief_cane`) each carry a `textures/item/*.png` (all three files really exist in the repository). Vibration Bean is the fourth invisible trap and gets the same treatment. Point Defense Fungus is different: it is a Turret, and both its block and its item borrow the vanilla `warped_fungus` (D-86's counterpart is the warped fungus), so it brings 0 new textures. The itemized list is in §9.1.

**Closed (author confirmed, +8)**: the fact card has been changed from +7 to **+8**, noting that the 8th file is Vibration Bean's item icon. The 8 rows of §9.1 of this document are the itemized expansion of exactly that reading (1 + 3 + 4 = 8).

---

## 2. Total Table of Incremental Registry Objects

### 2.1 Per-Registry Verification Table

The counting convention is word for word that of `12-TECH-0.0.2.md` §2.1: a **registry object count**; `recipe` / `advancement` / `tag` / `damage_type's JSON` are data files and do not enter the registry count (§2.3 lists them separately).

This table has 12 rows.

| # | registry | v0.0.2 | Increment | v0.0.3 | Increment detail |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 27 | **+4** | **31** | `friend_chives`, `chive_seeds`, `point_defense_fungus`, `vibration_bean` |
| 2 | `Registries.BLOCK` | 22 | **+3** | **25** | `friend_chives_crop`, `point_defense_fungus`, `vibration_bean` |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 6 | +0 | **6** | the two new blocks join the legal block set of an existing type (note 3 of §2.2) |
| 4 | `Registries.ENTITY_TYPE` | 11 | **+2** | **13** | `friend_chives`, `vibration_bean` |
| 5 | `Registries.MOB_EFFECT` | 2 | **+2** | **4** | `q_bounce`, `confusion` |
| 6 | `Registries.SOUND_EVENT` | 2 | **+1** | **3** | `friend_chives` |
| 7 | `Registries.RECIPE_TYPE` | 1 | +0 | **1** | reuses `farmstorm:converting` |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | +0 | **1** | reuses `ConverterRecipeSerializer` |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | +0 | **1** | the 4 new items are appended to `farmstorm:main` |
| 10 | `Registries.DAMAGE_TYPE` | 2 | **+1** | **3** | `zangief_slam` |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | +0 | **1** | — |
| 12 | `Registries.MENU` | 1 | +0 | **1** | — |
| | **Total** | **77** | **+13** | **90** | |

### 2.2 The 13 New Objects Named One by One

Each row gives the full registry ID and the class that carries it. **13 rows**.

| # | registry | Full ID | Carrying class / form | Basis |
|---|---|---|---|---|
| 1 | items | `farmstorm:friend_chives` | `ThrowableCropItem` (`CropProfile.FRIEND_CHIVES`) | D-84, D-85 |
| 2 | items | `farmstorm:chive_seeds` | `SeedItem` (the seed item of `CropProfile.FRIEND_CHIVES`) | D-84 |
| 3 | items | `farmstorm:point_defense_fungus` | `DeployableCropItem` (`extends BlockItem`) | D-86 |
| 4 | items | `farmstorm:vibration_bean` | `ThrowableCropItem` (**not** a BlockItem, see note 2) | D-90 |
| 5 | blocks | `farmstorm:friend_chives_crop` | `FarmstormCropBlock` (the planted form with 4 ages) | D-84 |
| 6 | blocks | `farmstorm:point_defense_fungus` | `DeployedCropBlock` (Turret appearance, not invisible) | D-86 |
| 7 | blocks | `farmstorm:vibration_bean` | `DeployedCropBlock` (invisible empty model + unattached mode) | D-91 |
| 8 | entity_types | `farmstorm:friend_chives` | `FriendChivesProjectile` (`extends ThrownCropEntity`) | D-85 |
| 9 | entity_types | `farmstorm:vibration_bean` | `VibrationBeanProjectile` (`extends ThrownCropEntity`) | D-92 |
| 10 | mob_effects | `farmstorm:q_bounce` | `QbounceEffect` (visible, with an 18×18 icon) | D-81 |
| 11 | mob_effects | `farmstorm:confusion` | `ConfusionEffect` (hidden, no icon) | D-83 |
| 12 | sound_events | `farmstorm:friend_chives` | `SoundEvent.createVariableRangeEvent` (written the same way as the existing 2) | D-85 |
| 13 | damage_types | `farmstorm:zangief_slam` | Data-driven: the `ResourceKey` of `FarmstormDamageTypes.ZANGIEF_SLAM` + `data/farmstorm/damage_type/zangief_slam.json` | D-96 |

**Note 1 (the same-ID relation of rows 3 and 6)**: `point_defense_fungus`'s item and block share one ID; the item is carried by `DeployableCropItem`, which is a `BlockItem` subclass (`item/DeployableCropItem.java` L18), so `Item.BY_BLOCK` has a mapping and the loot table's `lootTableItem(block)` can land on the item. This is the same form as 0.0.2's 6 deployed blocks.

**Note 2 (the non-isomorphic relation of rows 4 and 7)**: `vibration_bean`'s item is a `ThrowableCropItem` and **does not extend `BlockItem`** (it has to be throwable), so the block of the same name has no BlockItem mapping. Two consequences, written into §3.5 and §9.3: ① the block loot table must explicitly write `LootItem.lootTableItem(FarmstormItems.VIBRATION_BEAN)` — writing `lootTableItem(block)` would resolve to `Items.AIR`; ② the item model goes through `basicItem`, hence the need for the 8th PNG (§1.4(d)).

**Note 3 (the block_entities +0 of rows 3, 6 and 7)**: the switch in `FarmstormBlockEntities.deployedOf(DeployedKind)` gains two branches, but **must not** create new `BlockEntityType`s for them (that would become `block_entities +1`). The correct handling is to append the two new blocks to the block sets of two existing types respectively: the `fang_trap_cactus` type takes `vibration_bean`, and the `blast_carrot` type takes `point_defense_fungus`; `getEntries().size()` is still 6.

### 2.3 Increments That Are Not Registry Objects (they do not enter the 90)

Following the existing convention of `12-TECH-0.0.2.md` §2.4, converting recipes and advancements are data files and do not enter the registry count. **5 rows.**

| Increment | Count | Nature | Location |
|---|---|---|---|
| Converting recipes | **+2** | data files (`data/farmstorm/recipe/*.json`) | `point_defense_fungus` (`minecraft:warped_fungus` ⇄ this item, bidirectional, 3 experience, no catalyst, D-86), `vibration_bean` (`minecraft:cocoa_beans` ⇄ this item, same parameters, D-90) |
| advancements | **+3** (3 added this round; the hard count goes 15 → 18) | data files (a datapack registry, no `DeferredRegister`) | `first_q_bounce` (first time Q-Bounce is applied), `point_defense_intercept` (first time intercepted by Point Defense Fungus), `vibration_bean_trigger` (first time Vibration Bean is triggered) — an extension of D-63 |
| item tag (append) | +1 entry | data file | `friend_chives` is appended to `farmstorm:converter_fuel` (D-84: it has no converting recipe but is still a crafting material of the converter) |
| Existing advancements untouched | +0 | — | not one of 0.0.2's 15 advancements is changed (D-63) |
| The other 6 categories | +0 | — | `block_entities` / `recipe types` / `recipe serializers` / `creative tabs` / `data_components` / `menu types` are all reused |

**Friend Chives has no converting recipe** (D-84): it has no vanilla counterpart, and `ConverterRecipe`'s bidirectional form requires a pair of reversible mappings.

### 2.4 Sum Verification

| Algorithm | Expression | Result |
|---|---|---|
| Baseline + increment | 77 + 13 | **90** |
| Per-category addition | 31 + 25 + 6 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 1 | **90** |
| Item-by-item addition | items 4 + blocks 3 + entity_types 2 + mob_effects 2 + sound_events 1 + damage_types 1 | **13** |

The two algorithms check each other, and **the total of §2 equals the baseline 77 + 13 = 90**. Another 5 data-file entries (2 converting recipes + 3 advancements) do not enter this sum and are listed separately in §2.3.

**The impact of D-98 ~ D-102 on this chapter is zero**: all five supplementary adjudications land on "criteria and semantics inside the existing implementation" — D-98 adds the ally filter, D-99 settles where a thrown crop goes, D-100 reuses a vanilla sound, D-101 unifies the comparison operator, D-102 uses a vanilla type for the mount. Not one of them adds or removes a registry object, so **the three numbers 77 / +13 / 90 do not move by a single character** (D-100 and D-102 especially: the former pins sound_events at +1, the latter pins entity_types at +2).

---

## 3. Design of the New and Changed Classes

Each class gets four items: **responsibilities / key methods / which config keys it reads / which existing files it changes**. That is one fewer than `12-TECH` §3's five — "which registry objects it writes" is dropped, because §2.2 has already named the 13 objects and their carrying classes one by one, and writing them again would leave two ledgers that drift apart. Every config key name can be looked up in the 19 rows of §8.1.

### 3.0 Overview

| Category | File | New / changed |
|---|---|---|
| Effects, 2 | `effect/QbounceEffect.java`, `effect/ConfusionEffect.java` | New |
| Client, 3 | `client/QbounceClientState.java`, `client/QbouncePoseRenderer.java`, `client/ConfusionInputHandler.java` | New |
| Server, 1 | `behavior/QbounceMountController.java` (the mount branch) | New |
| Projectiles, 2 | `entity/thrown/FriendChivesProjectile.java`, `entity/thrown/VibrationBeanProjectile.java` | New |
| Behaviors, 2 | `behavior/impl/PointDefenseFungusBehavior.java`, `behavior/impl/VibrationBeanTrap.java` | New |
| Fang Trap Cactus hits, 1 | `behavior/impl/FangTrapFangs.java` (the fang ownership table + the damage-event subscription) | New |
| 6 existing files | `util/CropProfile.java`, `behavior/DeployedKind.java`, `block/DeployedCropBlock.java`, `blockentity/DeployedCropBlockEntity.java`, `behavior/impl/FangTrapCactusTrap.java`, `behavior/ZangiefPiledriverController.java` | Changed |
| Wiring | the 7 registry classes under `registry/`, `config/FarmstormConfig.java`, `Farmstorm.java`, `client/InputLockHandler.java`, the 7 datagen Providers, `behavior/DeployedBehaviors.java`, `behavior/impl/ZangiefCaneTrap.java` | Changed |

### 3.1 `QbounceEffect` (new, `effect/`)

| Item | Content |
|---|---|
| Responsibilities | The state carrier for Q-Bounce. **Its only responsibility is "is the effect present + how much time is left"**; it defines no duration key of its own, produces no displacement, changes no attribute and sends no packet (D-50 / D-81) |
| Key methods | constructor `super(MobEffectCategory.HARMFUL, 0xFF6EC7)`; does not override `applyEffectTick` (an empty implementation means no side effects); does not override `isDurationEffectTick` |
| Icon | Visible, with an icon: `assets/farmstorm/textures/mob_effect/q_bounce.png` (18×18). Whether it is hidden does not rely on a class-level hook but on the six-argument constructor used when applying it, `MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon)` — `q_bounce` uses `(…, false, true)` (show the icon), `confusion` uses `(…, false, false)` |
| Config keys read | None. The duration and amplifier are written by the applying side, `FriendChivesProjectile`: `friend_chives.effectTicks` 200 ticks, amplifier 0 |
| Existing files changed | `registry/FarmstormMobEffects.java` (+1 `REGISTER.register("q_bounce", QbounceEffect::new)`); `Farmstorm.java` (the `mobEffects` assertion in `logRegistrySelfCheck` goes 2 → 4) |

**One division of labour that has to be written down**: the effect itself does not know whether it lasts 10 seconds or 4. Whoever applies it sets the duration — `friend_chives.effectTicks` is the sole source of Q-Bounce's duration, and `vibration_bean.confusionTicks` is the sole source of Confusion's duration (D-81 / D-83 / D-92).

### 3.2 `ConfusionEffect` (new, `effect/`)

| Item | Content |
|---|---|
| Responsibilities | The state carrier for Confusion. **It registers no icon** (which is why there is no PNG for it under `textures/mob_effect/`, and why it is not among the 8 of §9.1); it affects players only |
| Key methods | constructor `super(MobEffectCategory.HARMFUL, 0x6A0DAD)`; no tick behaviour. "Players only" is guaranteed on both sides: the applying side (`VibrationBeanTrap`) iterates `level.players()` only, and the consuming side (`ConfusionInputHandler`) runs on the client input chain, which mobs simply do not have |
| Config keys read | None (D-83 states plainly that the Confusion group sets not even one key). The duration is written by `vibration_bean.confusionTicks`, 80 ticks |
| Existing files changed | `registry/FarmstormMobEffects.java` (+1 registration); `Farmstorm.java` (as in 3.1) |

### 3.3 `FriendChivesProjectile` (new, `entity/thrown/`)

| Item | Content |
|---|---|
| Responsibilities | Friend Chives' projectile. At the impact point it applies 200 ticks of Q-Bounce to creatures inside a **Euclidean 6-block** sphere; when it hits **at least 1 creature** it plays one hit sound at the impact point (audible within 32 blocks), and an empty throw makes no sound (D-85) |
| Key methods | `protected CropProfile profile()` returns `CropProfile.FRIEND_CHIVES` (it must be a static constant: the base class calls it during `defineSynchedData`); overrides `onImpact(ServerLevel, Vec3, @Nullable Entity)` — ① `level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(radius), e -> e != shooter && e.isAlive() && pos.distanceTo(e.position()) <= radius)`; ② for each hit, `addEffect(new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true))`; ③ when the hit count is > 0, play the hit sound at the impact point |
| Second filter | The AABB is only a coarse filter (`inflate(6.0)` gives a 12×12×12 cube); the hit decision must be re-checked in the Euclidean metric with `distanceTo` — otherwise it becomes Chebyshev (D-20's Euclidean convention, D-85's sphere) |
| Config keys read | `friend_chives.hitRadius` (6.0 blocks), `friend_chives.effectTicks` (200 ticks), `friend_chives.soundRange` (32.0 blocks) |
| Existing files changed | `registry/FarmstormEntityTypes.java` (+1 `projectile("friend_chives", FriendChivesProjectile::new)`, with arguments identical to the existing 10: `MISC` / `0.25×0.25` / tracking 4 / interval 10); `registry/FarmstormItems.java` (`FRIEND_CHIVES`'s `cropItem` points at it); `registry/FarmstormSounds.java` (+1 variable-range event) |

**How `soundRange` lands on 32 blocks**: `SoundEvent.createVariableRangeEvent` sets no distance by itself (both existing events use it), so the audible radius is decided by the playing side. This document's approach is **per-player playback**: iterate `level.players()` and send a separate `ClientboundSoundPacket` to each player whose `distanceToSqr <= 32²` (**to be verified** [RT-17]: the constructor argument order in 1.21.1), which makes 32 blocks a hard cap rather than an estimate. If the real constructor does not match, degrade to `level.playSound(null, pos, sound, SoundSource.PLAYERS, 1.0F, 1.0F)` and accept the server's default attenuation; the criterion of §11 #12 then becomes a measured record of "audible within 32 blocks, inaudible beyond 33".

### 3.4 `PointDefenseFungusBehavior` (new, `behavior/impl/`) — it is a `TurretBehavior`, not a `TrapBehavior`

**The basis for the ruling (read the interface, do not guess)**:

| Interface | Method signature | Can it stop a projectile? |
|---|---|---|
| `behavior/TrapBehavior.java` | `Optional<Player> findTrigger(DeployedCropBlockEntity)`, `void trigger(DeployedCropBlockEntity, Player)` | **No.** Both the return value and the parameter are pinned to `Player`, and a projectile is an `Entity` and not a `Player`. Using it would mean changing the interface signature — that is changing existing 0.0.2 content, which D-80 §5.7 forbids |
| `behavior/TurretBehavior.java` | `Optional<Entity> findTarget(DeployedCropBlockEntity)`, `void fire(DeployedCropBlockEntity, Entity)`, `int cooldownTicks()`, with `default tick` = acquire a target → fire → `setCooldown(cooldownTicks())` | **Yes.** The target type is `Entity`, and `default tick` is exactly the "find it and shoot it" form |

**Conclusion**: `PointDefenseFungusBehavior implements TurretBehavior`.

| Item | Content |
|---|---|
| Responsibilities | Intercepts **non-ally** projectiles of this mod inside a Euclidean 8-block sphere centred on itself; each interception consumes 1 charge, and after the 6th it is silently removed; interception produces only particles and a sound effect and deals no damage (D-87 / D-88 / D-89; the ally filter was restored by **D-98**) |
| `kind()` | `DeployedKind.POINT_DEFENSE_FUNGUS` |
| `cooldownTicks()` | **0**. Returning 0 makes the `setCooldown(0)` inside `default tick` a no-op, so every staggered scan can intercept. `cooldown` cannot express the 6-interception cap here — the cap is **block lifetime**, not rhythm, and the two have different semantics |
| `findTarget` | `level.getEntitiesOfClass(ThrownCropEntity.class, box, predicate)` takes the nearest one; the predicate's five filters (not removed / not a cluster sub-projectile / inside the Euclidean 8 blocks / **non-ally**) are in step 4 of §6.2 |
| `fire` | **Four steps in a fixed order** (V-113: the old draft wrote "particles → sound → mark", contradicting §6.2/§6.3; the mark must come first or same-tick dedupe breaks): ① `if (target.markIntercepted(now)) return;`; ② at the **projectile position**, `server.sendParticles(ParticleTypes.CRIT, x, y, z, interceptParticles, 0.3, 0.3, 0.3, 0.05)`; ③ at the **block position**, `level.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, …)` (sound from the block, particles from the projectile — **deliberately different sources**, not a typo); ④ `be.recordIntercept(now)` then `target.discard()`. **No branch runs `onImpact`** |
| Non-ally filter (D-98) | `final Entity thrower = target.getOwner()`; when `thrower instanceof Player p && be.isAlly(p)` is true ⇒ **an ally, skipped outright** (no charge consumed, no particles, no discard); everything else counts as non-ally (including a dispenser or a command spawn with `owner == null`, following the existing convention of `DeployedOwnership#isAlly` L47–L52, "with no owner it is non-ally to everyone"). The criterion is that one call, `be.isAlly(Player)`; no new team comparison is written |
| The 6-interception cap | `recordIntercept` checks `count >= maxIntercepts` internally and then calls `be.consume()` (`removeBlock(pos, false)` + break particles, bypassing the loot table — the path of D-72) |
| Config keys read | `point_defense_fungus.interceptRadius` (8.0 blocks), `point_defense_fungus.maxIntercepts` (6), `point_defense_fungus.interceptParticles` (8) |
| Existing files changed | `behavior/DeployedKind.java` (+1 value `POINT_DEFENSE_FUNGUS(10)`); `behavior/DeployedBehaviors.java` (+1 static singleton + the switch's `case`; a switch expression that misses a branch fails at compile time, so nothing is silently left unregistered); `blockentity/DeployedCropBlockEntity.java` (the `interceptCount` field and `recordIntercept(long)`); `entity/ThrownCropEntity.java` (the tick stamp) |

**Behaviour boundary (a cap given by the interface, which has to be written down)**: `TurretBehavior#findTarget` returns only one target, so **one Point Defense Fungus intercepts at most 1 projectile per scan**. With a scan interval of 10 ticks (0.5 seconds) ⇒ a single fungus intercepts at most **2 times per second**; the cap of 6 means that, counted from the first interception, it takes roughly **3 seconds** to finish 6 interceptions. This is the shape of the interface, not a trade-off made by this document; changing it would mean changing `TurretBehavior`, and this document does not touch it.

**`turret_bullet` is outside the scope of the decision**: `entity/TurretBullet.java` is not a subclass of `ThrownCropEntity`, so Pumpkin Turret's missile is not intercepted by Point Defense Fungus. This is a necessary consequence of "deciding by base class" and matches D-87's "only intercept thrown crops"; §11 #13 verifies it once as a boundary item.

### 3.5 `VibrationBeanProjectile` (new, `entity/thrown/`)

| Item | Content |
|---|---|
| Responsibilities | Vibration Bean's projectile. At the impact point it places an invisible trap block + block entity that is attached to no block at all, and writes the deployer into it (D-91) |
| Key methods | `profile()` returns `CropProfile.VIBRATION_BEAN`; overrides `onImpact(ServerLevel, Vec3, @Nullable Entity)` — ① take the impact point's `BlockPos` (`BlockPos.containing(pos)`); if that position is not replaceable, take the cell above it; if that is still not replaceable, place nothing and the projectile vanishes as usual; ② when `DeployedCropBlockEntity.canDeploy(level, target, shooter)` is false, likewise place nothing (the existing two gates of 8 per chunk and 32 per player are kept; the throw path may not bypass them); ③ `level.setBlock(target, block.defaultBlockState().setValue(FACE, FLOOR).setValue(FACING, NORTH).setValue(FLOATING, false), 3)`; ④ `if (level.getBlockEntity(target) instanceof DeployedCropBlockEntity be) be.setOwner(shooter)` |
| Why the owner must be written explicitly | The throw path does not go through `BlockItem`, so `DeployedCropBlock#setPlacedBy` (the only place that writes the owner, `block/DeployedCropBlock.java` L270–L275) is never called. Without an owner, `be.isAlly()` is always false and the trap would judge its own side as "non-ally" — D-92's trigger side recognises non-ally players only, so that rule would be falsified |
| Config keys read | None (the placement itself is not parameterized; the trigger and Confusion live in `VibrationBeanTrap`) |
| Existing files changed | `registry/FarmstormEntityTypes.java` (+1 `projectile(...)`); `registry/FarmstormItems.java` (`VIBRATION_BEAN`'s `cropItem` points at it) |

### 3.6 `VibrationBeanTrap` (new, `behavior/impl/`) — the trigger and settlement of the Trap category

| Item | Content |
|---|---|
| Responsibilities | Triggers when a non-ally player comes within a Euclidean distance of **strictly less than 4 blocks**: it applies 80 ticks of Confusion to **every player** within radius 4 and plays one explosion sound at the trap, then removes itself silently with no drops (D-92) |
| Interface | `implements TrapBehavior` (the trigger target is a player, so `Optional<Player>` is entirely sufficient) |
| `findTrigger` | iterate `level.players()`, skipping spectators / creative players / `be.isAlly(player)` (following the exemption convention of HC-09 and D-07), and test `center.distanceTo(player.position()) < triggerRadius` — **strictly less than**, unlike `FangTrapCactusTrap`'s `<=`; both inequalities are decisions (D-92 / C-5) |
| `trigger` | ① iterate `level.players()` and apply `addEffect(new MobEffectInstance(FarmstormMobEffects.CONFUSION, confusionTicks, 0, false, false, false))` to **all** players with `center.distanceTo(p.position()) < triggerRadius` (strictly less than, sharing the same key and the same operator as the trigger side, **D-101**) (no ally/enemy filtering, D-27's one-shot AoE convention); ② `level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 0.9F)` (sound effect only, no explosion constructed — D-58's boundary; that event is the **vanilla existing** constant `SoundEvents.GENERIC_EXPLODE`, so no `sound_event` is added, **D-100**); ③ `be.consume()` |
| Config keys read | `vibration_bean.triggerRadius` (4.0 blocks), `vibration_bean.confusionTicks` (80 ticks) |
| Existing files changed | `behavior/DeployedKind.java` (+1 value `VIBRATION_BEAN(10)`); `behavior/DeployedBehaviors.java` (+1 singleton + switch case); `registry/FarmstormBlocks.java` (+1 `deployed(...)` with `unattached = true`) |

**D-101 put into practice: one ruler at both ends**

| Side | Criterion (same constant, same operator) | Target | Basis |
|---|---|---|---|
| Trigger side | `center.distanceTo(player.position()) < FarmstormConfig.vibrationBeanTriggerRadius.get()` (4.0 blocks) | non-ally players, with the triple exemption of spectator / creative / ally | D-92, D-101 |
| Effect side | `center.distanceTo(p.position()) < FarmstormConfig.vibrationBeanTriggerRadius.get()` (**the same key, the same 4.0, the same `<`**) | **all** players within the radius, with no ally/enemy filtering | D-27, D-101 |

Both ends read the same config key and write the same operator, so no boundary state exists in which "the effect was applied without a trigger". The moment a second `4.0` literal or a second operator appears in the implementation, that is an implementation error — §11 #11's boundary case is exactly one attempt at 3.9 blocks and one at 4.0 blocks.

### 3.7 Vibration Bean's Unattached Block Mode (changes `block/DeployedCropBlock.java`)

A new flag `unattached` (`private final boolean`), **a sibling of `supportsFloating` but semantically orthogonal to it**; the difference table is §7.1. There are four change points, and all of them are "add a branch" rather than "change an existing branch":

| Location | Now | Change |
|---|---|---|
| Field + constructor (L62–L85) | 4 external boolean parameters (`supportsFloating` / `turretHardMine` / `explosionImmune`, with `allyOnlyBreak` derived) | add a 5th parameter `boolean unattached`, store it in the field and add a `public boolean unattached()` accessor. The existing 6 call sites (`FarmstormBlocks`' 6 `deployed` calls) get the added argument `false` |
| `CODEC` (L53–L60) | 3 fields (`kind` / `supports_floating` / `turret_hard_mine` / `explosion_immune`, 4 components in total) | add a 5th, `Codec.BOOL.optionalFieldOf("unattached", false)`, placed after `explosion_immune` and before `propertiesCodec()` |
| `canSurvive` (L176–L180) | always checks the support block's `isFaceSturdy` | first line `if (this.unattached) return true;` |
| `updateShape` (L183–L221) | the support block disappears → those that may float turn `FLOATING`; the rest drop and destroy themselves | first line `if (this.unattached) return state;` (returns itself for **any direction**, earlier than the `direction != supportDirection(state)` check) |

`getStateForPlacement` gains no branch: the throw path never goes through it (it is `setBlock` straight from `VibrationBeanProjectile`), and a floating block need not support hand placement either (its only source is the projectile).

### 3.8 The Redo of Fang Trap Cactus (changes `behavior/impl/FangTrapCactusTrap.java` + adds `FangTrapFangs`)

Of the 8 rows of D-94's numeric table, 4 change and 4 stay. **For 3 of them the implementation change lands on a config default and not one line of code moves** — this has to be written down clearly, or the implementer will go and change the literals in the code:

| D-94 item | Current state (source) | 0.0.3 | Where the change is |
|---|---|---|---|
| Trigger distance 3.0 → **2.5** | L38 `FarmstormConfig.fangTrapTriggerRadius.get()` | 2.5 | the default of `FarmstormConfig`'s `defineInRange("triggerRadius", 3.0, 0.0, 32.0)` becomes 2.5 (the range does not move). L38 **not changed** |
| Outer ring count 8 → 8 | L61 `fangTrapOuterFangCount` | 8 | unchanged |
| Outer ring delay 10 → **4** | L61 `fangTrapOuterDelayTicks` | 4 | config default 10 → 4. L61 **not changed** |
| Outer ring radius 2.0 → 2.0 | L61 `fangTrapOuterRadius` | 2.0 | unchanged |
| Inner ring count 5 → 5 | L59 `fangTrapInnerFangCount` | 5 | unchanged |
| Inner ring delay 4 → **2** | L59 `fangTrapInnerDelayTicks` | 2 | config default 4 → 2. L59 **not changed** |
| Inner ring radius 1.0 → 1.0 | L59 `fangTrapInnerRadius` | 1.0 | unchanged |
| Hit effect none → **Slowness V for 60 ticks** | none | Slowness amplifier 4, 60 ticks | one registration line in `spawnWave` + the new `FangTrapFangs` subscription |

**At the code level there are really 2 changes plus 1 new class**:

1. `spawnWave` (L72–L93) adds one line, `FangTrapFangs.mark(fangs, be.getBlockPos())`, **before** `level.addFreshEntity(fangs)`. The ownership line L87–L89 (`fangs.setOwner(owner)`) does not move — D-95 states explicitly that "the fangs' owner is still the deployer".
2. `FangTrapFangs` (a new class, `behavior/impl/`): one `Map<Integer, Long>` (fang entity id → expiry gameTime); `mark` writes `gameTime + 40` (twice the 20-tick lifetime of a vanilla fang, as margin), and `consume(Entity)` deletes on read and returns whether it hit. **It writes no NBT and registers no object** — it is a bounded in-memory table, with an upper bound of ≈ 13 fangs per trigger (8 outer + 5 inner).
3. `FangTrapFangs` also subscribes to `LivingDamageEvent.Post` (**to be verified** [RT-22]): when `event.getSource().getDirectEntity() instanceof EvokerFangs fangs` and `FangTrapFangs.consume(fangs)` is true → apply `new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDurationTicks, slownessLevel - 1)` to `event.getEntity()`.

**Why no bounding-box decision** (D-95): the fangs' damage brings its own invulnerability frames, armour and resistance settlement; building a second "am I inside the ring" test would create false hits — slowed even though "the animation bit me and no health was actually lost". Taking the real damage event as the criterion hands the definition of "hit" to the only side that can change health.

**Why no Mixin**: both `@Mixin` and `mixins.json` are on the prohibited list of §4 of `verify_project.py` (D-40 / HC-04). `EvokerFangs`' damage method is private and out of reach, so the only place to hook is the damage-event side.

### 3.9 One Addition to the Projectile Base Class (changes `entity/ThrownCropEntity.java`)

Only one field and two methods are added, and **no existing behaviour changes** (the execution path of the existing 9 direct subclasses + 1 indirect subclass stays identical):

| New | Signature | Description |
|---|---|---|
| Field | `private long lastInterceptTick = Long.MIN_VALUE;` | server-side in-memory state |
| Method | `public boolean markIntercepted(long now)` | returns `true` when `lastInterceptTick == now` (already intercepted this tick, skip); otherwise writes `now` and returns `false` |
| Method | `public boolean wasInterceptedAt(long now)` | a read-only test, for logs and self-checks |

**It does not go into NBT**: `addAdditionalSaveData` / `readAdditionalSaveData` do not move. Reason: the mark is valid for exactly 1 tick, so carrying it across a save means nothing; writing it would instead make "the first interception after loading" be misjudged as "already intercepted this tick".

### 3.10 Interception Counting (changes `blockentity/DeployedCropBlockEntity.java`)

| Item | Content |
|---|---|
| New field | `private int interceptCount;` (default 0) |
| New methods | `public int interceptCount()`, `public void recordIntercept(long now)` — the latter does `interceptCount++`, `setChanged()`, and calls `consume()` when `interceptCount >= FarmstormConfig.pointDefenseFungusMaxIntercepts.get()` |
| NBT | `saveAdditional` / `loadAdditional` each gain one line (key name `interceptCount`), written the same way as the existing `placedGameTime` / `detached`. **This one is the opposite of §3.9**: the count has to survive a save, because "it disappears after the 6th" is the block's lifetime and not single-tick semantics |
| Where the cap comes from | `point_defense_fungus.maxIntercepts` (6). **No hard-coded constant**: 0.0.2's `HARD_MINE_TICKS` / `DEPLOY_COOLDOWN_TICKS` are constants because they have no property "the author might want to tune"; the 6 is a tunable value written plainly in D-88 |
| Config keys read | `point_defense_fungus.maxIntercepts` |
| Existing files changed | this file; `registry/FarmstormBlockEntities.java` (the two new blocks join the block sets of existing types, see note 3 of §2.2) |

### 3.11 Zangief Cane's Fall Damage (changes `behavior/ZangiefPiledriverController.java` + a new subscription in the same file)

**Which event it hangs off**: natural fall damage uses `net.neoforged.neoforge.event.entity.living.LivingFallEvent` (**to be verified** [RT-22]), and the landing settlement hangs off the `player.onGround()` branch inside the existing `tickFalling` (L233–L246) — that is already the single entry point of "the landing decision", and no second one is built.

| Item | Content |
|---|---|
| Cancelling natural fall (during phase 2) | add a static `@SubscribeEvent` method: `LivingFallEvent` → test whether `event.getEntity()` carries `farmstorm:zangief_piledriver` and `getAmplifier() == ZangiefPiledriverEffect.PHASE_FALLING` (= 2) → `event.setCanceled(true)`; also gated by the `zangief_cane.cancelFallDamage` switch. The criterion is **effect presence + amplifier**, not global fall logic, so it acts only on this trap's phase 2 (RT-22) |
| Landing settlement | inside `tickFalling`'s `player.onGround()` branch and **before** `finish(player)`: resolve `ACTIVE.get(uuid).ownerId` into an `Entity` (pass `null` if it cannot be resolved), then `player.hurt(level.damageSources().source(FarmstormDamageTypes.ZANGIEF_SLAM, ownerEntity), (float) FarmstormConfig.zangiefCaneSlamDamage.get())`, then `finish(player)` |
| Why the order is fixed | **settle first, then wind down** — `finish` strips the effect, and once it is stripped the criterion of `cancelFallDamage` no longer holds; the reverse order ("`finish` first, then `hurt`") would create a half-finished state of "damage dealt but the phase has already ended" |
| Ownership passing | `begin(ServerPlayer)` gains a second parameter `@Nullable UUID ownerId`, and `PhaseState` gains an `ownerId` field. The call site `ZangiefCaneTrap#trigger` (L67) already has `be.ownerUuid()` available, so it becomes `begin(player, be.ownerUuid())` |
| What `cancelFallDamage` does | a boolean switch. When `false`, natural fall is not cancelled, so "natural fall + the fixed 7.5" stack — this is the opening left for the author's controlled experiments, default `true` |
| Config keys read | `zangief_cane.slamDamage` (7.5 points), `zangief_cane.cancelFallDamage` (true) |
| Existing files changed | this file (the `begin` signature, `PhaseState`, `tickFalling`, the new subscription); `behavior/impl/ZangiefCaneTrap.java` (L67 passes the owner); `registry/FarmstormDamageTypes.java` (+1 `ResourceKey` and a `zangiefSlam(...)` accessor); `datagen/ModDamageTypeProvider.java` (+1 `context.register`, `WHEN_CAUSED_BY_LIVING_NON_PLAYER`, `exhaustion 0.0F`, `DamageEffects.HURT`); `Farmstorm.java` (+1 in the `damageTypeCount()` list) |

### 3.12 Two Client State Machines (new, `client/`)

| Class | Responsibilities | Key methods | config |
|---|---|---|---|
| `QbounceClientState` | reads `farmstorm:q_bounce` on itself once per tick and flattens "present or not / ticks remaining / entry tick" into static scalars. Written in the shape of `ZangiefClientState` (`ClientTickEvent.Post` + three static fields), adding no network packet | `active()`, `remainingTicks()`, `phaseTicks()` | none read |
| `ConfusionInputHandler` | Confusion's reversed input (§5 in full) | `onMovementInput(MovementInputUpdateEvent)` | none read |

**How the three parameters of the `render` branch reach the client**: `farmstorm-server.toml` cannot be read by the client on a dedicated server, so 0.0.2 handles this class of parameter with "a compile-time mirror constant plus a comment saying it must be changed in step" (`client/ZangiefClientState.java` L27–L34's `DEFAULT_SPIN_DEGREES_PER_TICK`). This document follows suit: the three constants are defined in **`client/QbounceClientState.java`** (the state machine that owns them; the renderer only reads its `rotateDegrees()` / `squashScaleX|Y|Z()`), `DEFAULT_ROTATE_DEGREES_PER_TICK = 24.0F` / `DEFAULT_SQUASH_AMPLITUDE = 0.15F` / `DEFAULT_SQUASH_PERIOD_TICKS = 6`, whose values must be character-for-character equal to the defaults in `FarmstormConfig`. **`qbounce.poseMode` is the exception**: it decides whether the server attaches a mount, so it is read on the server only and the client needs no mirror.

## 4. The Two Branches of Q-Bounce's Sitting Pose (technical plan)

Both are implemented, switched by `qbounce.poseMode`, default `render` (D-82). The losing one is turned off by configuration, and **its code is not deleted**.

### 4.1 How the Switch Works

| Item | Content |
|---|---|
| Key | `qbounce.poseMode`, type `enum { render, mount }`, default `render`, the range being exactly those two values (written as a string in TOML) |
| Who reads it | the `mount` branch reads it on the server (`QbounceMountController` tests `FarmstormConfig.qbouncePoseMode.get() == PoseMode.MOUNT` every tick); the `render` branch does not read this key — the client only looks at whether it carries the `q_bounce` effect |
| When it can change | it can be changed at runtime (taking effect after `/reload`). Switching to `mount`, creatures already under the effect get a mount from the next tick on; switching back to `render`, `QbounceMountController` calls `discard()` on every mount and clears the table on the next tick |

### 4.2 The render Branch

| Item | Content |
|---|---|
| Which events it hangs off | `net.neoforged.neoforge.client.event.RenderLivingEvent.Pre` and `Post` (**to be verified** [RT-17]: whether 1.21.1's `Pre` provides `getPoseStack()` / `getRenderer()` / `getPartialTick()`) |
| Entry condition | `event.getEntity().hasEffect(FarmstormMobEffects.Q_BOUNCE)` |
| Pose | `event.getRenderer().getModel().riding = true` (**to be verified** [RT-17]: whether `EntityModel#riding` is public and read by the corresponding renderer). This is the only non-invasive way to "put it into the riding pose" — it changes only this frame's model instance and **never touches the entity**, so AI, hitbox and pathfinding are all unaffected |
| Lowering and scaling | in `Pre`, in order: ① `poseStack.pushPose()`; ② `poseStack.translate(0.0, -0.35, 0.0)` to lower the model (in blocks; a value supplied by this document, tunable over 0–1); ③ a squash factor `s = 1.0 - amplitude * sin(2π * t / period)`, where `t = entity.tickCount + partialTick`, `amplitude` comes from `qbounce.squashAmplitude` (0.15 = 15 %) and `period` from `qbounce.squashPeriodTicks` (6 ticks); `poseStack.scale(1/s, s, 1/s)` keeps the volume constant; ④ `poseStack.mulPose(Axis.YP.rotationDegrees(rotateDegreesPerTick * t % 360.0F))` for continuous rotation (**to be verified** [RT-17]: whether 1.21.1 uses `com.mojang.math.Axis` or a static helper on `org.joml`) |
| Exit | in `Post`, `poseStack.popPose()`, paired with `Pre`'s `pushPose` |
| Config keys read | `qbounce.rotateDegreesPerTick` (24.0 degrees/tick), `qbounce.squashAmplitude` (0.15), `qbounce.squashPeriodTicks` (6 ticks) — all three go through the compile-time mirror constants of §3.12 |
| Failure modes | ① **a third-party model's `riding` has no effect**: another mod's custom `EntityModel` may not read it at all, leaving only the lowering and the rotation and no sitting pose. This is RT-17's main risk, and the mitigation is to accept it — the render branch is defined as "does not change entity state"; ② **unbalanced `push` / `pop`**: if `Pre` pushes and `Post` never runs because of an exception, every later render accumulates the offset (the whole picture sinks). For the mitigation see RT-17's fallback column; ③ **the lowering does not match the look**: 0.35 blocks is a value supplied by this document, and the author can tune it on a real client |

### 4.3 The mount Branch

| Item | Content |
|---|---|
| What is attached | the server gives every creature under Q-Bounce an **invisible mount**, whose type **D-102** pins down as `minecraft:armor_stand` (an existing vanilla type, adding no `entity_type`): `EntityType.ARMOR_STAND.create(level)` → `setInvisible(true)` / `setNoGravity(true)` / `setInvulnerable(true)` / `setSilent(true)` / `setMarker(true)` (**to be verified** [RT-18]: `setMarker`'s semantics are "drop the hitbox and the rendering"; if it also drops the passenger's sitting pose, fall back to `setMarker(false) + setSmall(true)`) |
| How it is attached | `stand.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), 0.0F)` → `level.addFreshEntity(stand)` → `entity.startRiding(stand, true)` (**to be verified** [RT-18]: the second `boolean` parameter is "forced mounting") |
| Bookkeeping | `private static final Map<UUID, MountRecord> ACTIVE` (passenger UUID → mount entity id + expiry tick + re-mount count), in the shape of `ZangiefPiledriverController.ACTIVE`: in memory only, never written to a save |
| Per-tick validation (`ServerTickEvent.Post`) | the mount is kept only while three conditions hold at once, otherwise it is `discard()`-ed and the record removed: ① the passenger is still alive and `getEffect(Q_BOUNCE) != null`; ② `passenger.getVehicle() != null` and `getVehicle().getId() == record.mountId`; ③ the mount still exists (`level.getEntity(mountId) != null`) |
| Cleanup on creature death | `LivingDeathEvent` triggers an immediate `discard()` (without waiting for the next tick) and removal from the table; there are three further exit paths, isomorphic to Zangief Cane's: `PlayerEvent.PlayerLoggedOutEvent`, `PlayerEvent.PlayerChangedDimensionEvent` and `ServerStoppedEvent` (the last clears the whole table) |
| Config keys read | `qbounce.poseMode` (read on this branch only) |
| Failure modes | ① **it changes entity relations and collision**: the mount is a real entity, it lifts the passenger (the `armor_stand` passenger offset) and changes what other mods decide about "what the player is riding" — RT-18's main risk; ② **mount residue**: any cleanup path written incompletely leaves an invisible entity pinning the player down, and the fallback is the three per-tick checks (which depend on no event); ③ **being force-dismounted by a third party**: if another mod calls `stopRiding`, the re-mount on the next tick causes jitter; the mitigation is to give up once `MountRecord.reMountCount` exceeds 20 and print one warning line (without flooding the log) |

**D-102 put into practice: the mount is pinned to `minecraft:armor_stand`**

| Item | Content |
|---|---|
| The pinned type | `net.minecraft.world.entity.decoration.ArmorStand` (registry id `minecraft:armor_stand`). **An existing vanilla entity type; no `entity_type` is added**, so the `entity_types` increment stays **+2** (the two projectiles, row 4 of §2.1) |
| Why it is that one | ① it is the only vanilla kind that satisfies all four conditions at once: "can be made completely invisible + can drop its hitbox + has no AI + can serve as a `Vehicle` for `startRiding`". ② `setInvisible(true)` stops the body rendering; `setMarker(true)` drops the hitbox and the rendering (one of RT-18's to-be-verified items: `setMarker`'s semantics); the passenger's sitting pose comes from `LivingEntity`'s riding channel, so the pose is the **genuine vanilla** one — which is exactly the only gain the mount branch has over the render branch (D-82). ③ it has no AI goals and takes no part in pathfinding, with only `tick()` and the passenger sync per tick, so its cost is on the order of a static entity. ④ it is itself a `LivingEntity`, so `startRiding` goes through the vanilla channel; `setNoGravity(true)` keeps gravity from dragging it off, `setInvulnerable(true)` keeps stray damage from killing it, `setSilent(true)` keeps it quiet. ⑤ the passenger offset is fixed, so the coordinates are predictable |
| Why not another vanilla type | `minecraft:boat` and `minecraft:minecart` bring their own hitbox and interaction (right-click to board a boat / a minecart) and change movement logic, which conflicts with the "invisible seat" semantics; `minecraft:item` / `minecraft:experience_orb` / `minecraft:area_effect_cloud` are not carriers that can hold a passenger (`startRiding` fails); `minecraft:bat` / `minecraft:silverfish` have AI, move on their own and leave their position. Only `armor_stand` falls in the intersection of "no AI + invisible + usable as a carrier" |
| Why nothing new is added (D-102's reading) | a custom entity type that exists only to serve a pose would become a registry entry nobody ever uses on purpose; the vanilla type is already enough. This points the same way as D-100 "reuse a vanilla sound": both use only what already exists and do not widen the registry surface |
| Failure modes | ① if `setMarker(true)` also cancels the passenger's sitting pose, fall back to `setMarker(false) + setSmall(true) + setInvisible(true)` (the hitbox comes back and the passenger is lifted by less); ② if some mod treats `armor_stand` specially (forbidding riding, say), the whole branch degrades to render — which is exactly what `qbounce.poseMode` exists for |
| Cleanup on creature death | see the "Cleanup on creature death" row above: `LivingDeathEvent` discards immediately, and the three per-tick checks are the fallback (depending on no event). The death paths of zombies / skeletons / players all pass through `LivingDeathEvent`, so no corpse leaves a mount behind |

### 4.4 The Two Branches Side by Side

| Dimension | render | mount |
|---|---|---|
| What it changes | the render layer's `PoseStack` and model instance fields | server-side entity relations + hitbox |
| Is it a "genuine vanilla pose" | no (approximated with `riding`) | yes |
| Impact on AI / pathfinding | none | yes (the passenger is lifted and judged as "riding" by other mods) |
| Impact on collision | none | yes |
| Blast radius when it fails | the picture only | entity state, which can freeze a player |
| Default | **yes** | no |
| Risk ID | RT-17 | RT-18 |
| Registry increment | 0 | 0 (the mount uses vanilla `minecraft:armor_stand`, D-102) |

---

## 5. Confusion's Movement Reversal (technical plan)

### 5.1 Only the Direction Components Change

| Item | Content |
|---|---|
| Which event it hangs off | `net.neoforged.neoforge.client.event.MovementInputUpdateEvent` (0.0.2's `client/InputLockHandler.java` L61–L71 already uses the same event, so the API is proven to exist and needs no verification) |
| What changes | `input.forwardImpulse = -input.forwardImpulse;` and `input.leftImpulse = -input.leftImpulse;` — the positive direction of `forwardImpulse` is forward, so negating it maps W↔S; the positive direction of `leftImpulse` is left, so negating it maps A↔D (D-83) |
| What is left alone | `input.jumping`, `input.shiftKeyDown` (jump and sneak are not reversed); the **magnitude** of `forwardImpulse` / `leftImpulse` (multiplied by −1 only, so the amplitude is unchanged character for character, hence "speed is not touched"); the player's `Attributes.MOVEMENT_SPEED` (not one attribute changes); the camera (no `CalculatePlayerTurnEvent` subscription, no sensitivity or camera touched); attack and use (no `InputEvent.MouseButton` / `InteractionKeyMappingTriggered`) |
| Entry condition | `Minecraft.getInstance().player.hasEffect(FarmstormMobEffects.CONFUSION)` |

### 5.2 Priority Against `InputLockHandler`: the Lock Wins

Three layers of guarantee, from strongest to weakest:

1. **An explicit criterion (primary)**: the first line of `ConfusionInputHandler` is `if (InputLockHandler.isLocked()) return;`. To that end `client/InputLockHandler.java` gains a `public static boolean isLocked()` (wrapping the private `locked()` at L39–L41 in a public accessor, **without changing one line of the existing seven-stage logic**).
2. **A natural degeneracy in the numbers (secondary)**: even if the execution order of the two pieces is rearranged, the result is the same — with the lock first, `forwardImpulse` is zeroed and Confusion's negation then yields `−0.0F`, and `−0.0F == 0.0F` is `true`, so the movement consumer still gets zero; with Confusion first, the negation is followed by the zeroing. Under either order the player does not move.
3. **Event order is not used as a criterion**: when two `@EventBusSubscriber`s subscribe to the same event there is **no agreed execution order**, so "the lock runs first" cannot be guaranteed by registration order. That is exactly why the first layer has to be written explicitly instead of relying on the second.

**`locked()`'s criterion gains one term**: 0.0.2's `locked()` is `ZangiefClientState.active() && player != null`; Q-Bounce has to lock as well (D-81), so it becomes `(ZangiefClientState.active() || QbounceClientState.active()) && Minecraft.getInstance().player != null`. The segmentation and technique of the seven-stage lock stay identical (D-79 reused; no second set is written).

### 5.3 Boundaries and Failure Modes

| Boundary | Handling |
|---|---|
| Jitter when the effect has 1 tick left | the client reads the effect once per tick, and normal service resumes on the next tick after it disappears (every criterion is "is the effect present at this instant", with no caching) |
| Confusion and Q-Bounce at the same time | the lock wins and there is no reversal (D-83's explicit conclusion). While locked, the player is not affected by the "opposite direction" |
| Confusion applied to a mob | ineffective but harmless: mobs have no client input chain, so `MovementInputUpdateEvent` is never fired for them. D-83's "players only" is therefore structural, not something blocked out with `instanceof Player` |
| Reversal and sprinting | `Input` has no sprint field; sprinting goes through `player.isSprinting()`, which this plan does not touch, so the sprint direction follows the reversed non-zero component — consistent with "only the movement input is reversed" |

---

## 6. Point Defense Fungus's Projectile Interception (technical plan)

### 6.1 What Counts as This Mod's Projectile — a Base-Class Criterion, Not an ID Whitelist

| Item | Content |
|---|---|
| Criterion | `target instanceof ThrownCropEntity` (`entity/ThrownCropEntity.java`). One `instanceof` covers every member |
| Why that covers everything | the generic bound of the `projectile(String, EntityTypeFactory)` factory in `registry/FarmstormEntityTypes.java` is `<T extends ThrownCropEntity>` (L62), which guarantees **at compile time** that the 10 existing `entity_type`s + the 2 new ones all sit on this base class. A whitelist would miss entries and drift; a base-class criterion is maintained by the compiler |
| Coverage | **12 members** (D-98's corrected reading, listed piece by piece under the table in §6.1): 0.0.2's 10 + `friend_chives` + `vibration_bean` |
| **Ally exclusion (D-98)** | the interception surface is **non-ally** projectiles: when the projectile's owner is allied with the fungus's deployer, it **passes through, is not intercepted, and consumes no charge**. The criterion uses exactly one existing call — `be.isAlly(throwerPlayer)` (`DeployedCropBlockEntity` L280–L285 → `DeployedOwnership#isAlly` → `Farmstorm.TEAMS`' `TeamResolver`, i.e. D-06's vanilla scoreboard-team reading) — and writes no new team comparison. When the owner is not a player (a dispenser / a command spawn / `getOwner() == null`) it is treated as non-ally, following the existing comment in `DeployedOwnership` L47–L52, "with no owner it is non-ally to everyone" |
| Explicitly not intercepted | `minecraft:arrow` / `minecraft:snowball` / `minecraft:trident` / `minecraft:ender_pearl` (none of them a `ThrownCropEntity` subclass); `farmstorm:turret_bullet` (`entity/TurretBullet.java` is not on the base class); any projectile of any other mod |
| Further exclusion | entities whose `isSubmunition()` is `true` (the wind charges a firecracker scatters — 0.0.2 already has this sync flag): it is a **fragment** of a projectile, and intercepting it would turn one firecracker into 6 interceptions |

The projectile family table (**12 rows**, D-98's delivery list):

| # | entity_type id | Carrying class | Owner source | New this round? |
|---|---|---|---|---|
| 1 | `farmstorm:puffed_wheat` | `PuffedWheatProjectile` | `getOwner()` (thrower / dispenser) | No |
| 2 | `farmstorm:ubi_server` | `UbiServerProjectile` | same as above | No |
| 3 | `farmstorm:toxic_ubi_server` | `ToxicUbiServerProjectile` | same as above | No |
| 4 | `farmstorm:melon_slice_bomb` | `MelonSliceProjectile` | same as above | No |
| 5 | `farmstorm:melon_grenade` | `MelonGrenadeProjectile` | same as above | No |
| 6 | `farmstorm:bamboo_firecracker` | `BambooFirecrackerProjectile` | same as above | No |
| 7 | `farmstorm:red_mushroom_puff` | `RedMushroomPuffProjectile` | same as above | No |
| 8 | `farmstorm:brown_mushroom_puff` | `BrownMushroomPuffProjectile` | same as above | No |
| 9 | `farmstorm:molotov` | `MolotovProjectile` | same as above | No |
| 10 | `farmstorm:compressed_puffed_wheat` | `CompressedPuffedWheatProjectile` (**indirect**: `extends PuffedWheatProjectile`) | same as above | No |
| 11 | `farmstorm:friend_chives` | `FriendChivesProjectile` (`extends ThrownCropEntity`) | same as above | **Yes** |
| 12 | `farmstorm:vibration_bean` | `VibrationBeanProjectile` (`extends ThrownCropEntity`) | same as above | **Yes** |

Category subtotal: 0.0.2's members **10** (rows 1–10) + **2** new this round (rows 11–12) = **12**. Row 10 is the "indirect inheritance" case, and it is also where the "document 9 / source 10" gap in §1.4(a) comes from.

### 6.2 The Per-Tick Decision Flow

1. `DeployedCropBlockEntity.serverTick` (L131–L148) → early return on cooldown → staggering: it scans only when `(now + stagger(interval)) % interval == 0`, with `stagger = floorMod(pos.hashCode(), 10)` (L151–L156).
2. `onScan` → `behavior().tick(be)` → `TurretBehavior`'s default `tick` → `findTarget`.
3. `findTarget`: `AABB box = new AABB(center, center).inflate(8.0)`, `level.getEntitiesOfClass(ThrownCropEntity.class, box, predicate)` (**to be verified** [RT-20]: whether this overload exists on 1.21.1's `Level`; degrade to the overload without a predicate and filter by hand).
4. The predicate has **four filters**, in a fixed order (D-98's "test `instanceof` first, then alliance, and only then spend a charge"): ① `!e.isRemoved()`; ② `!e.isSubmunition()`; ③ `center.distanceTo(e.position()) <= 8.0` (**the Euclidean re-check** — the AABB is a 16×16×16 cubic coarse filter and cannot serve as the decision, or it would be Chebyshev: a cube corner sits 8√3 ≈ 13.86 blocks from the centre); ④ **non-ally** — `!(e.getOwner() instanceof Player p && be.isAlly(p))`. Filter ④ rules ally projectiles out **at the targeting stage**, so they are never passed to `fire` and never reach the step that spends a charge.
5. The nearest one is handed to `fire`.
6. The four steps of `fire`: ① `if (target.markIntercepted(now)) return;` (same-tick de-duplication, see §6.3) → ② `sendParticles` → ③ `playSound` → ④ `be.recordIntercept(now)` and then `target.discard()`. The order contains **no `onImpact`**: a successful interception returns immediately and the impact point is never settled (D-99).

### 6.3 The "Only One Charge Per Tick" Mark: Where It Lives and When It Is Cleared

| Item | Content |
|---|---|
| Where it lives | **on the projectile**, not on the block (D-88's reasoning: a block entity only knows how many times it has been charged and cannot know who else is charging at the same instant; the projectile is the only shared object in this decision): `entity/ThrownCropEntity.java` gains `private long lastInterceptTick` (§3.9) |
| When it is written | as the very first thing in `fire`: `if (target.markIntercepted(now)) return;` — returning `true` means "already intercepted by someone this tick", so this pass is skipped outright, **without consuming a charge and without particles** |
| When it is cleared | three ways: ① **the normal path: it expires automatically at the next gameTime.** The test is the equality `lastInterceptTick == now`, so as soon as `now` changes the two are unequal and no cleanup code is needed; ② **after being intercepted**: the projectile is `discard()`-ed straight away and the mark dies with the entity; ③ **it is never persisted**: `addAdditionalSaveData` does not include this field, so no entity exists after loading that "carries last tick's mark" |
| Why not a static `Set<Entity>` table | a table needs a cleanup hook (removing the entity when it is gone), and one missed spot is a memory leak; a `long` field has no lifetime |

### 6.4 Scan Rhythm and Performance

| Item | Value and reason |
|---|---|
| Scan interval | 10 ticks (0.5 seconds): `DeployedKind.POINT_DEFENSE_FUNGUS(10)` + `TurretBehavior#scanInterval()` defaulting to 10 |
| Staggering | `floorMod(pos.hashCode(), 10)`: at most 8 Point Defense Fungi per chunk (`MAX_DEPLOYED_PER_CHUNK = 8`) are spread across different ticks, so in the worst case only 1 is scanning at any instant |
| Cost of one scan | 1 AABB entity query (bucketed per chunk section, not a sweep of every entity on the server) + n distance computations, where n is the number of projectiles inside the 8-block cube (in practice usually 0–3) |
| Total cost per second | 2 per second for a single fungus; a cluster of 8 fungi within 8 blocks ⇒ 16 per second per chunk, the same order of magnitude as the existing turrets' scan load (`PumpkinTurret` also scans on a 10-tick interval) |
| Caps | `MAX_DEPLOYED_PER_PLAYER = 32`, `MAX_DEPLOYED_PER_CHUNK = 8` (two existing constants, untouched in 0.0.3), so the worst case is "32 fungi per player, 8 fungi per chunk", which after staggering is at most 1 query per chunk per tick |
| What is not done | no "projectiles broadcast their own position" (that needs a new packet, which D-50 forbids); no "fungi notify each other" (that needs shared state, which RT-20 judges not worth it) |

### 6.5 Visuals and the Damage Boundary (D-89)

`fire` does exactly three things: `sendParticles`, `playSound`, `discard()`. **It does not call `hurt`, does not write a block, does not `new Explosion`, and does not change any entity attribute**. The criterion of §11 #14 is therefore "a player at full health stands within 8 blocks and has 6 projectiles intercepted, and their health does not change by a single point".

**D-99 put into practice (where an intercepted projectile goes)**: an interception that succeeds is `discard()`-ed at once, and **returns before `onImpact`** — the intercepted projectile does not explode, write a block, apply an effect or drop anything. What is deleted is the **projectile entity** (D-99), not the block; the block side's silent removal on the 6th interception still follows D-72's `consume()` reading (D-88), and the two paths are never mixed.

**D-100 put into practice (a reused sound, no new registration)**: the interception sound uses the vanilla `SoundEvents.SHIELD_BLOCK` (`SoundSource.BLOCKS`, volume 0.8, pitch 1.2, the server's default audible radius) and adds no `sound_event`. So this release's `sound_events` increment is only Friend Chives' **+1** (row 12 of §2.2); the full sound-event table is §9.2.

---

## 7. Vibration Bean's Unattached Block (technical plan)

### 7.1 How the New Flag `unattached` Differs from the Existing `supportsFloating`

| Dimension | `supportsFloating` (present in 0.0.2) | `unattached` (new in 0.0.3) |
|---|---|---|
| Semantics | **tolerating the loss of support**: the block attaches legally first, and is kept when the support disappears afterwards | **never attached from the start**: the block never had support |
| Initial state | placement requires a whitelisted face + a support block that is complete and opaque | no placement rule (its only source is the projectile) |
| `canSurvive` | checks the support block's `isFaceSturdy` (L176–L180) | always `true` |
| `updateShape` | the support block disappears → those that may float get `FLOATING = true`; the rest drop and destroy themselves | returns itself for any direction |
| The `FLOATING` property | read and written (the model adds a 90° rotation by it, `ModBlockStateProvider` L97–L99) | always `false`, never set |
| Users | `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` (3 pass `true`) | `vibration_bean` only |
| Can they be merged | **No**: merging would push a block that "had no support from the start" through a state machine of "attach first, lose it later", and the semantics of `canSurvive` and `updateShape` are entirely different (D-91's reasoning) |

### 7.2 Handling of the Four Scenarios

| Scenario | Handling | Basis |
|---|---|---|
| `canSurvive` | `if (this.unattached) return true;` on the method's first line. Every caller (`BlockItem`'s `canPlace`, `updateOrDestroy`, `FallingBlock`'s check) gets `true` | D-91 "canSurvive is always true" |
| `updateShape` | `if (this.unattached) return state;` on the method's first line, **earlier than** the `direction != supportDirection(state)` test. Returning the same `BlockState` instance ⇒ `Block.updateOrDestroy`'s `newState != state` test is false ⇒ neither `destroyBlock` nor `setBlock` fires | D-91 "not removed by a block update" |
| Piston pushing | **no new test needed**: `FarmstormBlocks.deployed(...)` sets `PushReaction.BLOCK` on every deployed block (L86–L87), and `BLOCK` means "cannot be pushed and blocks the piston". Vibration Bean goes through the same `deployed(...)` factory, so it is immovable from birth | existing source |
| Chunk unload | **no new test needed**: both the block state and the block entity go into the save (`DeployedCropBlockEntity`'s `saveAdditional` / `loadAdditional`), and `getTicker` returns as usual after reloading (L136–L148: null on the client, a ticker on the server). What has to be guaranteed is "do nothing extra in `setRemoved`" — the existing implementation only releases the deployment quota (L307–L321) and does not touch the block | existing source |
| How it is broken | the player can hit it out (default hardness 0.5, `PushReaction.BLOCK`, no `requiresCorrectToolForDrops`); what it drops is decided by the loot table, see the note on #19 of §9.3 | note 2 of §2.2 |
| Silent removal | after triggering it goes through `be.consume()`: `removeBlock(pos, false)` + break particles, no loot table and no drops (D-92 follows D-72) | `DeployedCropBlockEntity#consume` L296–L305 |
| One ruler at both ends (**D-101**) | the trigger side and the effect side read the **same** `vibration_bean.triggerRadius` (4.0 blocks) and write the **same** `<`. This section is responsible only for the block and its updates; the decision is the two-ended table at the close of §3.6, and this table's four world-maintenance handlings are independent of the operator | D-101 |

### 7.3 Failure Modes

| Mode | Symptom | Fallback |
|---|---|---|
| A missing branch in `updateShape` | the trap vanishes into thin air as soon as a neighbouring block changes (the player sees "the bean disappeared as soon as it was placed") | §11 #10's criterion: after placing it, break the 6 surrounding blocks face by face and the block must still be there |
| Mixing it up with `supportsFloating` | if `vibration_bean` were mistakenly passed `supportsFloating = true`: `updateShape` would set `FLOATING = true` and the model would pick up a 90° rotation (invisible on an invisible block), yet `canSurvive` would still return `isSupportValid(...)` = false, so every path that reads `canSurvive` would decide it ought to disappear | registered in RT-21; §11 #10's second criterion is "**`FLOATING` must always be false**" |

## 8. The Full Inventory of config Changes

### 8.1 Item-by-Item Inventory (**19 rows**)

The column set is the same as `12-TECH-0.0.2.md` §8.1: group.key / type / default / range / purpose / D number. **This table's 19 rows = 19 keys** (16 purely new + 3 changes to existing defaults). Below the table sits 1 non-key note (`confusion` has no key), which is **not counted in the 19**.

| # | group.key | Type | Default | Range | Purpose | D |
|---|---|---|---|---|---|---|
| 1 | `qbounce.poseMode` | enum | `render` | `render` / `mount` | the sitting-branch switch (§4.1) | D-82 |
| 2 | `qbounce.rotateDegreesPerTick` | double | 24.0 | 0.0 – 360.0 degrees/tick | the angular velocity of the continuous rotation | D-81 (the default follows §3.3 of the construction fact card) |
| 3 | `qbounce.squashAmplitude` | double | 0.15 | 0.0 – 0.90 | squash-and-stretch amplitude (a ratio, 0.15 = 15 %) | D-81 (the default follows §3.3 of the construction fact card) |
| 4 | `qbounce.squashPeriodTicks` | int | 6 | 2 – 100 ticks | the number of ticks in one full squash-and-stretch cycle | D-81 (the default follows §3.3 of the construction fact card) |
| 5 | `friend_chives.hitRadius` | double | 6.0 | 0.0 – 16.0 blocks | the radius of the impact sphere (Euclidean) | D-85 |
| 6 | `friend_chives.effectTicks` | int | 200 | 20 – 1200 ticks | Q-Bounce duration (200 ticks = 10 seconds) | D-85 |
| 7 | `friend_chives.soundRange` | double | 32.0 | 0.0 – 64.0 blocks | the audible radius of the hit sound | D-85 |
| 8 | `point_defense_fungus.interceptRadius` | double | 8.0 | 0.0 – 16.0 blocks | the radius of the interception sphere (Euclidean) | D-87 |
| 9 | `point_defense_fungus.maxIntercepts` | int | 6 | 1 – 64 times | the interception cap (silently removed after the Nth) | D-88 |
| 10 | `point_defense_fungus.interceptParticles` | int | 8 | 0 – 64 particles | the particle count of the interception effect | D-89 |
| 11 | `vibration_bean.triggerRadius` | double | 4.0 | 0.0 – 16.0 blocks | trigger distance (**strictly less than**) | D-92 |
| 12 | `vibration_bean.confusionTicks` | int | 80 | 20 – 600 ticks | Confusion duration (80 ticks = 4 seconds) | D-92 |
| 13 | `fang_trap_cactus.triggerRadius` | double | **2.5** (was 3.0) | 0.0 – 32.0 blocks | trigger distance (Euclidean) | D-94 (changes an existing default) |
| 14 | `fang_trap_cactus.outerDelayTicks` | int | **4** (was 10) | 0 – 200 ticks | outer-ring fang delay (0.2 seconds) | D-94 (changes an existing default) |
| 15 | `fang_trap_cactus.innerDelayTicks` | int | **2** (was 4) | 0 – 200 ticks | inner-ring fang delay (0.1 seconds) | D-94 (changes an existing default) |
| 16 | `fang_trap_cactus.slownessDurationTicks` | int | 60 | 0 – 600 ticks | Slowness duration in ticks | D-94 |
| 17 | `fang_trap_cactus.slownessLevel` | int | 5 | 1 – 10 levels | Slowness level (written as amplifier = level − 1 = 4) | D-94 |
| 18 | `zangief_cane.slamDamage` | double | 7.5 | 0.0 – 40.0 points | fixed landing damage (points = half a heart) | D-96 |
| 19 | `zangief_cane.cancelFallDamage` | boolean | `true` | `true` / `false` | cancels natural fall damage during phase 2 | D-96 |

**The 1 non-key note**: the `confusion` group **defines no key at all** — Confusion has no "the effect's own duration" tier: the duration is written by the applying side through `vibration_bean.confusionTicks` (80 ticks) (D-83, D-92). `q_bounce` is the same (its duration is written by `friend_chives.effectTicks`), but the `qbounce.*` group exists because it carries the pose and control parameters, which have nothing to do with "effect duration".

**Row count and category subtotal**: this table's 19 rows = 16 purely new (#1–#12, #16–#19) + 3 changes to existing defaults (#13–#15). Category subtotal: `qbounce` 4 + `friend_chives` 3 + `point_defense_fungus` 3 + `vibration_bean` 2 + `fang_trap_cactus` 5 + `zangief_cane` 2 = 19.

**Not a single existing key's range moves**: #13 / #14 / #15 change only the default argument of `defineInRange`; `min` / `max` and the `translation` key are kept character for character (current values `0.0–32.0` / `0–200` / `0–200`, see `FarmstormConfig` L494–L511).

### 8.2 The TOML Fragment (the 4 new groups, appended in alphabetical order)

```toml
[qbounce]
	# sitting branch: render = changes the render layer only (default), mount = the server attaches an invisible mount
	poseMode = "render"
	# angular velocity of the continuous rotation (degrees/tick), range 0.0 ~ 360.0
	rotateDegreesPerTick = 24.0
	# squash-and-stretch amplitude (a ratio), range 0.0 ~ 0.90
	squashAmplitude = 0.15
	# ticks in one full squash-and-stretch cycle, range 2 ~ 100
	squashPeriodTicks = 6

[friend_chives]
	# radius of the impact sphere (blocks, Euclidean), range 0.0 ~ 16.0
	hitRadius = 6.0
	# Q-Bounce duration (ticks), 200 ticks = 10 seconds; range 20 ~ 1200
	effectTicks = 200
	# audible radius of the hit sound (blocks), range 0.0 ~ 64.0
	soundRange = 32.0

[point_defense_fungus]
	# radius of the interception sphere (blocks, Euclidean), range 0.0 ~ 16.0
	interceptRadius = 8.0
	# interception cap, silently removed after the Nth interception; range 1 ~ 64
	maxIntercepts = 6
	# particle count of the interception effect, range 0 ~ 64
	interceptParticles = 8

[vibration_bean]
	# trigger distance (blocks, strictly less than), range 0.0 ~ 16.0
	triggerRadius = 4.0
	# Confusion duration (ticks), 80 ticks = 4 seconds; range 20 ~ 600
	confusionTicks = 80
```

Within `fang_trap_cactus`'s existing 7 keys, 3 defaults change and 2 new keys are appended; within `zangief_cane`'s existing 6 keys, 2 new keys are appended — both groups are appended in place in the file, and no new group is created.

### 8.3 Equality of the Key Count and the Language Keys

| Item | 0.0.2 | Increment | 0.0.3 |
|---|---|---|---|
| config keys | 139 | +16 | **155** |
| `farmstorm.configuration.*` language keys | 139 | +16 | **155** |

> Note: rows #13/#14/#15 of the 19 rows in §8.1 change the defaults of existing keys (the key names do not change), so the key count is 139 + 16 = **155**; the old draft computed 139 + 19 = 158 and double-counted these three keys.

The two are always equal, because every key generates one language key through `FarmstormConfig.tr(path)` (`"farmstorm.configuration." + path`), and `ModLangProvider.config(path)` writes one entry for it, one to one. For the fix to §1 of `verify_project.py` see §1.4(c): expected key set = 117 from §3.8 of `01-DESIGN.md` + 22 from §8.1 of `12-TECH-0.0.2.md` = 139, and from v0.0.3 on, + this table's 16 new keys = 155.

### 8.4 Constants Explicitly Kept Out of config (a reason for each)

| Constant | Value | Reason |
|---|---|---|
| Q-Bounce / Confusion amplifier | 0 | these two effects do not stack, so a level means nothing (in D-50's "effect present + amplifier + remaining duration is the complete state", amplifier is used here only for phase-type effects) |
| the interception sound | `SoundEvents.SHIELD_BLOCK` | a vanilla sound whose audible distance is decided by vanilla; no `sound_event` is added (otherwise §2's +13 breaks) |
| the interception particle type | `ParticleTypes.CRIT` | only the count is parameterized (`interceptParticles`); the type is not a tunable |
| Zangief Cane's `MAX_FALL_TICKS` | 100 ticks | an existing 0.0.2 constant with the nature of a safety valve; it stays out of config (the same class as HC-13) and this release does not touch it |
| `FangTrapFangs`' TTL | 40 ticks | an implementation detail (twice the 20-tick fang lifetime, as margin) with no bearing on gameplay |

---

## 9. Inventory of Resource and Data Files

Convention: only files that are **new in 0.0.3 or need entries appended** are listed; 0.0.2's 200-odd existing files are not. Each of the three tables (PNG / OGG / JSON) gives its own category subtotal; existing files that get "appended entries" are listed separately and do not count towards the number of new files.

### 9.1 PNG (**8 rows**)

| # | File | Size | Source / handling | Basis |
|---|---|---|---|---|
| 1 | `assets/farmstorm/textures/mob_effect/q_bounce.png` | 18×18 | converted from `preview.jpg` (1024×1024) and then scaled; a visible icon | D-81, D-53 |
| 2 | `assets/farmstorm/textures/item/friend_chives.png` | 16×16 | a newly drawn item icon | D-84 |
| 3 | `assets/farmstorm/textures/item/chive_seeds.png` | 16×16 | a newly drawn item icon | D-84 |
| 4 | `assets/farmstorm/textures/item/vibration_bean.png` | 16×16 | a newly drawn item icon (an invisible trap has no block appearance to borrow and must supply its own; the same as 0.0.2's `blast_carrot` / `fang_trap_cactus` / `zangief_cane`) | D-90, §1.4(d) |
| 5 | `assets/farmstorm/textures/block/friend_chives_stage0.png` | 16×16 | **magenta #D81E8A placeholder, not final art** | D-84 |
| 6 | `assets/farmstorm/textures/block/friend_chives_stage1.png` | 16×16 | **magenta #D81E8A placeholder, not final art** | D-84 |
| 7 | `assets/farmstorm/textures/block/friend_chives_stage2.png` | 16×16 | **magenta #D81E8A placeholder, not final art** | D-84 |
| 8 | `assets/farmstorm/textures/block/friend_chives_stage3.png` | 16×16 | **magenta #D81E8A placeholder, not final art** | D-84 |

Category subtotal: effect icon **1** (#1) + item icons **3** (#2 / #3 / #4) + block textures **4** (#5–#8) = **8**.

The two objects that add no PNG (this has to be written down, or someone will go and supply two more):

- both the block and the item of `point_defense_fungus` borrow the vanilla `minecraft:block/warped_fungus` / `minecraft:item/warped_fungus` — D-86's counterpart is the warped fungus, the same technique as 0.0.2's `pumpkin_turret` borrowing `block/pumpkin` (one item each in `ModBlockStateProvider`'s `DEPLOYED_VANILLA_PARENT` and `ModItemModelProvider`'s `VANILLA_BLOCK_PARENT`);
- `vibration_bean`'s block is an invisible empty model, and its `particle` texture borrows the vanilla `minecraft:block/cocoa_stage2` (D-90's counterpart is the cocoa bean), so 0 textures.

### 9.2 OGG (**1 row**)

| # | File | Specification | Source |
|---|---|---|---|
| 1 | `assets/farmstorm/sounds/friend_chives.ogg` | mono 64 kbps OGG | the source file `pengyoudejiu.mp3` (7,450,588 B; the file name is kept unchanged — an extension of D-66) |

Category subtotal: **1**.

**Appendix to §9.2: the full sound-event table (D-100 put into practice, 5 rows)**

This table is the "write out the reused vanilla event id for each site" that D-100 requires. **Only row 1 is a registry object new in this release**; the last four all reuse vanilla, so the `sound_events` increment is always **+1** (row 6 of §2.1).

| Site | Playback point | Event id | Registration | Basis |
|---|---|---|---|---|
| Friend Chives hit | `FriendChivesProjectile#onImpact`, at the impact point, played only when ≥ 1 creature is hit | `farmstorm:friend_chives` | **new in this mod** (+1) | D-85 |
| Point Defense Fungus interception | `PointDefenseFungusBehavior#fire`, at the position of the intercepted projectile | the constant `SoundEvents.SHIELD_BLOCK` | reuses vanilla | D-89, **D-100** |
| Vibration Bean explosion | `VibrationBeanTrap#trigger`, at the trap's position | the constant `SoundEvents.GENERIC_EXPLODE` | reuses vanilla | D-92, **D-100** |
| Zangief Cane trigger | `ZangiefCaneTrap#trigger` (existing in 0.0.2, untouched in this release) | `farmstorm:zangief_piledriver` | existing in 0.0.2 | D-66 |
| Wild Hunter loop | the client `SoundInstance` (existing in 0.0.2, untouched in this release) | `farmstorm:wild_hunter_loop` | existing in 0.0.2 | D-76 |

Why the two reuses may reach outside this mod's namespace: sound events are not part of this mod's registry surface, and `level.playSound` accepts any registered `Holder<SoundEvent>`; D-100's motive is exactly "do not widen the registry surface", so the closer these two stay to vanilla, the better they fit the convention.

**No id strings are written in the table**: both reuses obtain their `Holder<SoundEvent>` through the Java constants `SoundEvents.*`, and no hand-written `minecraft:***` string appears in the code. That way there is no silent-audio trap buried by misremembering a 1.21 event id, and this document does not have to freeze a string it cannot prove from the source.

### 9.3 JSON (**22 new rows** + **5 places** where existing files are appended)

| # | File (under `src/generated/resources/` or `src/main/resources/`) | Nature |
|---|---|---|
| 1 | `assets/farmstorm/blockstates/friend_chives_crop.json` | 4 age variants pointing at `friend_chives_stage0..3` |
| 2 | `assets/farmstorm/models/block/friend_chives_stage0.json` | parent `minecraft:block/crop`, `render_type: cutout` |
| 3 | `assets/farmstorm/models/block/friend_chives_stage1.json` | as above |
| 4 | `assets/farmstorm/models/block/friend_chives_stage2.json` | as above |
| 5 | `assets/farmstorm/models/block/friend_chives_stage3.json` | as above |
| 6 | `assets/farmstorm/blockstates/point_defense_fungus.json` | full coverage of face × facing × floating, all pointing at the same model |
| 7 | `assets/farmstorm/models/block/point_defense_fungus.json` | parent `minecraft:block/warped_fungus` + `render_type: cutout` |
| 8 | `assets/farmstorm/blockstates/vibration_bean.json` | as above (empty model + particle) |
| 9 | `assets/farmstorm/models/block/vibration_bean.json` | an empty model holding only `particle` (isomorphic to `blast_carrot.json`) |
| 10 | `assets/farmstorm/models/item/friend_chives.json` | `basicItem` |
| 11 | `assets/farmstorm/models/item/chive_seeds.json` | `basicItem` |
| 12 | `assets/farmstorm/models/item/point_defense_fungus.json` | parent `minecraft:block/warped_fungus` |
| 13 | `assets/farmstorm/models/item/vibration_bean.json` | `basicItem` (references `farmstorm:item/vibration_bean`, §9.1 #4) |
| 14 | `data/farmstorm/damage_type/zangief_slam.json` | `message_id: farmstorm.zangief_slam`, `WHEN_CAUSED_BY_LIVING_NON_PLAYER`, `exhaustion 0.0`, `HURT` |
| 15 | `data/farmstorm/recipe/point_defense_fungus.json` | `farmstorm:converting`, `minecraft:warped_fungus` ⇄ this item, 3 experience, no catalyst, `bidirectional: true` |
| 16 | `data/farmstorm/recipe/vibration_bean.json` | as above, `minecraft:cocoa_beans` ⇄ this item |
| 17 | `data/farmstorm/loot_table/blocks/friend_chives_crop.json` | 1 crop when mature + 0–3 seeds (following the existing crops' binomial parameters) |
| 18 | `data/farmstorm/loot_table/blocks/point_defense_fungus.json` | drops itself (the `deployed(block)` path; the block has a BlockItem) |
| 19 | `data/farmstorm/loot_table/blocks/vibration_bean.json` | **must explicitly write `LootItem.lootTableItem(FarmstormItems.VIBRATION_BEAN)`**; `lootTableItem(block)` must not be used (that block has no BlockItem, so it would resolve to `Items.AIR`, note 2 of §2.2) |
| 20 | `data/farmstorm/advancement/first_q_bounce.json` | `minecraft:impossible` + a server-side `award` (entering `AWARD_REQUIRED`) |
| 21 | `data/farmstorm/advancement/point_defense_intercept.json` | as above |
| 22 | `data/farmstorm/advancement/vibration_bean_trigger.json` | as above |

Category subtotal: blockstate **3** (#1 / #6 / #8) + block models **6** (#2–#5 / #7 / #9) + item models **4** (#10–#13) + damage_type **1** (#14) + recipe **2** (#15 / #16) + loot_table **3** (#17–#19) + advancement **3** (#20–#22) = **22**.

**5 places where existing files are appended** (not counted as new files, but missing any one of them leaves the feature incomplete):

| # | File | What is appended |
|---|---|---|
| A1 | `assets/farmstorm/sounds.json` | +1 event `friend_chives` (`category: player`, `subtitle: subtitles.farmstorm.friend_chives`, a single sound, `stream: false`); the event count goes 2 → 3 |
| A2 | `assets/farmstorm/lang/zh_cn.json` | +34 keys (see §9.4) |
| A3 | `assets/farmstorm/lang/en_us.json` | +34 keys (the key set identical character for character to A2) |
| A4 | `data/farmstorm/tags/item/converter_fuel.json` | +1 entry `farmstorm:friend_chives` (D-84) |
| A5 | `data/farmstorm/tags/block/deployed_crops.json` and `data/minecraft/tags/block/mineable/pickaxe.json` | +2 entries each (`point_defense_fungus`, `vibration_bean`) |

The other half of A5's story: 0.0.2's `MINEABLE_WITH_PICKAXE` does not carry `zangief_cane` (an existing state), and this release does not add it back; all that is guaranteed is that the two new blocks match the existing deployed blocks.

### 9.4 Language Key Increment (**34 keys**)

| Prefix | 0.0.2 | Increment | 0.0.3 | Source |
|---|---|---|---|---|
| `item.farmstorm.*` | 27 | +4 | 31 | `friend_chives` / `chive_seeds` / `point_defense_fungus` / `vibration_bean` |
| `block.farmstorm.*` | 22 | +3 | 25 | `friend_chives_crop` / `point_defense_fungus` / `vibration_bean` |
| `effect.farmstorm.*` | 2 | +2 | 4 | `q_bounce` / `confusion` |
| `subtitles.farmstorm.*` | 2 | +1 | 3 | `friend_chives` |
| `farmstorm.configuration.*` | 139 | +16 | 155 | the 16 new keys of §8.1 |
| `advancements.farmstorm.*` | 30 | +6 | 36 | 3 advancements × 2 (title + description) |
| `death.attack.farmstorm.*` | 4 | +2 | 6 | `zangief_slam` + `zangief_slam.player` (following the shape of the existing 2 damage types, 2 keys each) |
| the rest (`tooltip` 16 / `entity` 11 / `gui` 8 / `container` 1 / `itemGroup` 1) | 37 | +0 | 37 | unchanged (16 + 11 + 8 + 1 + 1 = 37) |
| **Total** | **263** | **+34** | **297** | |

**Check equation for the three columns** (added row by row; both columns land on the Total row): `0.0.2 column: 27+22+2+2+139+30+4+37 = 263; 0.0.3 column: 31+25+4+3+155+36+6+37 = 297.` Increment column: 4+3+2+1+16+6+2+0 = +34. All three hold at once.

Why the three `+0` categories do not move: the 16 keys of `tooltip.farmstorm.*` are organised by category (Trap / Turret / Buff / Demolition / Damage / Construction) plus fixed entries such as "no vanilla counterpart", and the two new deployables reuse existing category rows; the relation of `entity.farmstorm.*`'s 11 keys to a new `entity_type` is "one entry only if it has a display name of its own", and the two new projectiles use their item names as display names (`ThrownCropEntity#getDefaultItem` takes the item from the crop profile, so the name comes from `item.farmstorm.*` naturally).

---

## 10. Risk Register (RT-17 ~ RT-23)

The numbering is fixed at 7 entries; none is added, removed or merged. Each has five fields: risk / trigger condition / primary mitigation / fallback / how to verify during implementation.

### RT-17 · Q-Bounce render branch: the model flag and the PoseStack lifetime

| Item | Content |
|---|---|
| Risk | ① a third-party mod's custom `EntityModel` does not read the `riding` field ⇒ only the lowering and the rotation, no sitting pose; ② the `pushPose` in `RenderLivingEvent.Pre` and the `popPose` in `Post` do not pair up ⇒ every later render accumulates the offset; ③ this document hangs 4 to-be-verified APIs here (how `RenderLivingEvent.Pre/Post` are obtained, `EntityModel#riding`, `com.mojang.math.Axis`, the constructor arguments of `ClientboundSoundPacket`) |
| Trigger condition | any mod that replaces creature models is installed; or `Post` returns early because of an exception; or the build fails with "cannot find symbol" |
| Primary mitigation | only the render layer changes and the entity is never touched (AI / hitbox / pathfinding are entirely unaffected), so the blast radius of a failure is limited to the picture; the `riding` assignment is the first action at the start of `Pre`, so a failure at any step leaves entity state untouched |
| Fallback | ① when a to-be-verified API is missing, switch to the degraded form "only `translate` + `scale` + `mulPose`, no `push`" (no push means no imbalance); ② when `riding` has no effect, accept that form, write the conclusion into the acceptance record, and let D-82's real-client comparison decide whether to switch to `mount`; ③ when the sitting pose cannot be obtained at all, set `poseMode` to `mount` |
| How to verify during implementation | ① at compile time: get each of the 4 to-be-verified APIs working in a minimal case first; ② at runtime: look at one frame each for 3 zombies / players under Q-Bounce on the same screen and confirm "lowered by 0.35 blocks + about 480 degrees/second of rotation (24 degrees/tick × 20 ticks/second) + one squash cycle every 6 ticks"; ③ after 30 seconds of continuous rendering, check whether the whole picture drifts (proving push/pop balance) |

### RT-18 · Q-Bounce mount branch: entity relations and mount residue

| Item | Content |
|---|---|
| Risk | ① the mount is a real entity: it lifts the passenger, changes collision and what is decided about "what the player is riding", and may fight other mods' riding logic; ② any cleanup path written incompletely ⇒ an invisible entity is left behind, pinning the player in place; ③ two to-be-verified APIs (`ArmorStand#setMarker`, `Entity#startRiding(Entity, boolean)`) whose semantics do not match |
| Trigger condition | the same creature has Q-Bounce applied repeatedly; the player dies / disconnects / changes dimension while under the effect; another mod calls `stopRiding`; `setMarker(true)` cancels the passenger's sitting pose as well |
| Primary mitigation | the ledger `Map<UUID, MountRecord>` + three checks every tick (is the effect there / is the passenger still it / does the mount still exist), **depending on no event**; four exit paths (death / disconnect / dimension change / server stop) plus the per-tick backstop |
| Fallback | ① if `setMarker(true)` also cancels the sitting pose, fall back to `setMarker(false) + setSmall(true) + setInvisible(true)`; ② give up after more than 20 re-mounts and print one warning line (without flooding the log); ③ the branch can be switched off entirely at any time with `qbounce.poseMode = render`, without deleting code |
| How to verify during implementation | ① apply Q-Bounce to a zombie and verify "sitting pose + the mount disappears when the creature dies"; ② during the effect, `kill` the creature, disconnect and reconnect, and walk through a portal once each, verifying that no ArmorStand is left in the world (with a log line or `/data get entity`); ③ switch `poseMode` twice (render → mount → render) and verify that the table is cleared after each switch |

### RT-19 · Confusion coexisting with the input lock: two pieces of logic driving the same `Input`

| Item | Content |
|---|---|
| Risk | Confusion and Q-Bounce / Zangief Cane's seven-stage lock both manipulate `Input`: under an unfavourable order a "should be locked but moves backwards" case can appear; and writing a second lock for Confusion would override the existing seven-stage decision (D-79 explicitly forbids a second set) |
| Trigger condition | the player holds `q_bounce` and `confusion` at the same time; or someone later gives Confusion "lock" semantics; or the runtime reorders the two subscribers |
| Primary mitigation | the three-layer guarantee (§5.2): the explicit `if (InputLockHandler.isLocked()) return;` + the natural numeric degeneracy of `−0.0F == 0.0F` + the explicit rule "event order is not used as a criterion". Confusion subscribes to one event only and writes no second lock |
| Fallback | `locked()`'s criterion is widened to `ZangiefClientState.active() or QbounceClientState.active()`; if a third "needs a lock" source ever appears, one term is added inside `locked()` and the seven stages are not touched |
| How to verify during implementation | ① apply Confusion alone: W walks backwards, A strafes right, with the amplitude unchanged (§11 #6); ② apply Confusion + Q-Bounce together: pressing W does not move at all, and the lock's log line has been printed (§11 #7); ③ the direction returns to normal on the next tick after the effect ends |

### RT-20 · Point Defense Fungus: per-tick scanning, counting and "only one charge per tick"

| Item | Content |
|---|---|
| Risk | ① the AABB is a cubic coarse filter, and treating it as the decision makes it Chebyshev (a corner of the 8-block cube sits 13.86 blocks from the centre); ② several fungi reach the same projectile in the same instant ⇒ the charge is spent more than once; ③ the state split between the count and the 6-interception cap (the block counts, the projectile de-duplicates) produces "charged but not intercepted"; ⑤ **a missing ally filter** (D-98) ⇒ your own and your teammates' projectiles get eaten by your own fungus, turning into "friendly infighting"; ⑥ **not actually deleting after interception** (D-99) ⇒ the projectile settles its impact point as usual and the 6-interception cap loses its meaning; ④ one to-be-verified API (the overload `Level#getEntitiesOfClass(Class, AABB, Predicate)`) |
| Trigger condition | 2 or more Point Defense Fungi and 1 projectile within 8 blocks at the same time; a projectile arriving from a diagonal; `interceptRadius` tuned to 16 blocks; two fungi whose `stagger` happens to be identical |
| Primary mitigation | ① the hit decision must be re-checked with `distanceTo(...) <= radius`, with the AABB serving only as a coarse filter (step 4 of §6.2); ② the de-duplication mark lives on the projectile (`lastInterceptTick`), not on the block; ③ staggering via `floorMod(pos.hashCode(), 10)` spreads the 8 fungi of a chunk across different ticks; ④ `TurretBehavior#findTarget` returns one target only ⇒ a single fungus intercepts at most 1 per scan (§3.4 boundary); ⑤ the ally filter sits in the predicate's ④th filter (the targeting stage), so ally projectiles never reach `fire` and can never have a charge spent (D-98); ⑥ step four of `fire` is fixed as `recordIntercept` followed by `discard()`, with no branch anywhere that goes through `onImpact` (D-99) |
| Fallback | ① if the to-be-verified overload does not exist, degrade to the overload without a predicate plus manual filtering; ② if double charging is still measured in the same tick, upgrade the de-duplication from "a projectile field" to "a server-tick-level `Set<Integer>`, cleared in `ServerTickEvent.Post`" — but implement the field plan first, because a field has no cleanup hook and cannot leak; ③ the 6-interception cap cannot split as long as `recordIntercept` is the only entry point |
| How to verify during implementation | ① place 3 Point Defense Fungi facing the same projectile and read `interceptCount` each time, verifying the total goes up by exactly 1 (§11 #8); ② throw 6 in a row at a single fungus and verify the block is gone after the 6th with no drops; ③ a diagonal 7.9 blocks away must be intercepted and 8.1 blocks must not, proving Euclidean rather than Chebyshev; ④ throw a vanilla arrow / snowball / trident / ender pearl once each and verify they all pass through (§11 #13); ⑤ **throw one each as yourself and as a teammate**, verifying they pass through with `interceptCount` unchanged and no particles (§11 #8's third part, D-98); ⑥ for an intercepted projectile verify that "nothing follows the landing" (no explosion / no block written / no effect applied / no drops, D-99) |

### RT-21 · Vibration Bean: the unattached block and world maintenance

| Item | Content |
|---|---|
| Risk | ① a missing `unattached` branch in `updateShape` ⇒ the trap vanishes into thin air as soon as a neighbouring block changes; ② mixing it up with `supportsFloating` ⇒ `FLOATING = true` is set, the model picks up a 90° rotation and `canSurvive` still returns false; ③ the block or the block entity is lost after a chunk unload / reload; ④ piston pushing; ⑤ the DeployedKind column of row 22 in `16-DECISIONS` §4 conflicts with the source constraint (§1.4(b), closed: +2 adopted); ⑥ the trigger side and the effect side carry two operators or two 4.0s (D-101) |
| Trigger condition | digging / placing a block next to the trap; a chunk unloaded and then reloaded; a piston arm pushing into the trap; an implementer following §4 row 22 and filling `kind` with `null` (which falls back to `BLAST_CARROT` and makes the trap run as an explosion trap) |
| Primary mitigation | ① `unattached` is the **5th independent flag**, orthogonal to `supportsFloating`, with one early-return line at each of the four points (§3.7); ② pistons are covered by the existing `PushReaction.BLOCK` (`FarmstormBlocks` L86–L87), so no new code is needed; ③ `kind` must be filled with `DeployedKind.VIBRATION_BEAN`, and `DeployedCropBlockEntity#kindOf` throws when the block and the BE are mismatched (failing fast instead of silently running the wrong behaviour); ④ both ends share one config key and one `<` (the two-ended table at the close of §3.6), and a second literal in the implementation is an error (D-101) |
| Fallback | ① if the early returns in `updateShape` still leave a path uncovered, write both independent criteria (`canSurvive` and `updateShape`) as always-true (which is already the case); ② if the block really is lost after a chunk unload, hang a rebuild on `ChunkEvent.Load` keyed on `placedGameTime` — but that must first be proven to happen, and is not written in advance |
| How to verify during implementation | ① after placing it, dig out one block on each of the 6 surrounding faces and verify the block is still there and `FLOATING` is false (§11 #10); ② walk 200 blocks away and come back (forcing a chunk unload / reload) and verify the block and the block entity are still there; ③ push it once with a piston arm and verify it does not move; ④ trigger it once and verify "silent removal + no drops" (§11 #11) |

### RT-22 · Zangief Cane and Fang Trap Cactus: hooks on the damage-event chain

| Item | Content |
|---|---|
| Risk | ① cancelling natural fall only and forgetting the landing settlement ⇒ 0 damage; ② settling without cancelling ⇒ "the fixed 7.5 + natural fall", with damage drifting with height and conflicting with "fixed"; ③ the scope of the cancellation written too wide (changing falling globally) ⇒ affecting slow falling / immunity / other mods; ④ if Fang Trap Cactus's hit callback cannot get the fang instance, it degrades into the false hit of "am I inside the ring"; ⑤ two to-be-verified APIs (the cancellation semantics of `LivingFallEvent`, and `LivingDamageEvent.Post` versus `Entity#getPersistentData()`) |
| Trigger condition | the player falls 16 blocks during phase 2; the player dies during phase 2; a third-party mod clears negative effects; the `cancelFallDamage = false` control experiment; a fang hits but the damage is swallowed by invulnerability frames |
| Primary mitigation | ① cancellation and settlement are written inside the same phase state machine (the `onGround()` branch of `tickFalling`), so any change sees both at once; ② the cancellation's criterion is "effect present + `amplifier == 2`", touching neither global fall logic nor slow falling / immunity / flight; ③ the order is fixed: settle first, `finish` afterwards (`finish` strips the effect, and once stripped the criterion no longer holds); ④ the Fang Trap Cactus side takes the "actual damage event" as the criterion (D-95) |
| Fallback | ① if `LivingFallEvent.setCanceled(true)` only affects the distance and does not stop the damage, hang `LivingIncomingDamageEvent` instead and cancel when the damage type is fall damage; ② if the damage-event side also cannot get it, degrade to "no cancellation, and change 7.5 to 7.5 minus the natural fall damage" — which breaks the "fixed" semantics and is used only when both primary roads fail, and must be written into the acceptance record; ③ on the Fang Trap Cactus side, if the fang instance cannot be obtained, switch to a `Set<Integer>` + `getPersistentData()` mark (to be verified) |
| How to verify during implementation | ① one run from a height of 16 blocks: health drops by exactly 7.5 points (= 3.75 hearts) at the moment of landing, and there is no vanilla fall-damage line in the log (§11 #16); ② turn `cancelFallDamage` off and land once more: the damage is > 7.5 and varies with height; ③ one Fang Trap Cactus hit on a player: Slowness V for 60 ticks applied exactly once, and a player who was swept by the animation but lost no health gets no Slowness (§11 #15) |

### RT-23 · Numeric discipline and the gap in `tools/verify_project.py`

| Item | Content |
|---|---|
| Risk | ① the tool still enumerates per 0.0.1 (items 22 / blocks 20 / entity 9), and neither its denominator 64 nor its own count of 62 equals 77 ⇒ any "total" assertion is void until the tool is fixed; ② the config expectation still reads the 117 keys of `01-DESIGN.md`, and that file belongs to the frozen 00–15 zone and cannot be edited back ⇒ the tool stays red forever; ③ someone edits `01-DESIGN.md` to make the tool go green, breaking D-80; ④ someone announces 90 straight from "+13" instead of counting |
| Trigger condition | anyone running the tool right now; anyone following §4 row 22 and treating `DeployedKind` as +1; anyone copy-pasting "77" without looking at §1.2 |
| Primary mitigation | §1.2 of this document has already counted the 77 by category and aligned it with the 12 runtime assertions of `Farmstorm#logRegistrySelfCheck`, so 90 is the result of **two independent pieces of evidence plus one addition**, not a guess; the fix to the tool is written section by section in the table of §1.4(c) |
| Fallback | if the author's intent is "the tool is never changed and just stays red", then §11 #1 becomes "the tool stays red, but its output is compared section by section against this table and signed off in the acceptance record" — which loses the automation value, so it is used only if the author explicitly rejects fixing the tool |
| How to verify during implementation | ① run it once after the fix: `已核 registry 对象: 77 / 77` appears along with `结论：FAIL 0 项` and exit code 0; ② run it once more after v0.0.3 lands: `90 / 90`, config `139` (`155` after v0.0.3), lang `263` (`297` after v0.0.3); ③ re-run after every change to a registry class, and any subtotal that disagrees goes red immediately |

---

## 11. Acceptance Self-Check Table

One concrete action plus one decidable pass criterion per entry. **17 entries** (#1 – #17).

| # | Action | Pass criterion | Covers |
|---|---|---|---|
| 1 | fix `tools/verify_project.py` to the 0.0.2 baseline (per the four changes in §1.4(c)), then run `python -X utf8 tools/verify_project.py` | the output contains `已核 registry 对象: 77 / 77`, no FAIL in the config section's 139 keys, `lang 键集一致，共 263 键` and `结论：FAIL 0 项`, with exit code 0 | D-80, RT-23 |
| 2 | start the server and read the registry self-check line and the crop self-check line of `latest.log` | registry objects **90** with the per-category split 31 / 25 / 6 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1; crops **22** kinds, Throwable 12 / Deployable 7 / Edible 3, Demolition 4; lucky berry pool **19**; all three lines end with `OK` | §2.4, D-97 |
| 3 | with `qbounce.poseMode = render`, throw Friend Chives at a zombie | the zombie takes the riding pose (legs pressed down), is lowered by 0.35 blocks, rotates continuously (about 480 degrees/second, i.e. 24 degrees/tick × 20 ticks/second; one turn per 15 ticks) and squashes and stretches on a 6-tick cycle; the AI is still walking (its position keeps changing) and the hitbox is unchanged | D-81, D-82, RT-17 |
| 4 | with `qbounce.poseMode = mount`, the same zombie | the same look as above; its entity data shows it riding an ArmorStand; after the zombie dies that ArmorStand is no longer in the world | D-82, RT-18 |
| 5 | apply Q-Bounce to a player and press W / A / space / sneak / left click / right click / scroll wheel / a number key once each | nothing responds at all; ESC still opens the pause menu; the lock log prints once (Q-Bounce reuses the same seven-stage lock and adds no second set) | D-60, D-79, D-81 |
| 6 | apply Confusion to a player and press W, then A | W walks backwards, A strafes right, and **the speed amplitude matches normal** (computed from F3 coordinates tick by tick); the mouse still turns the view normally, jump and sneak work, and a left click can still hit a creature | D-83 |
| 7 | apply Q-Bounce first and then Confusion (or the other way round) and press W | the player does not move at all (displacement 0); the lock's log line has been printed; reversed movement returns on the next tick after the effects end | D-83, RT-19 |
| 8 | do all three parts: ① place 1 Point Defense Fungus and throw 6 Friend Chives in a row; ② place 3 Point Defense Fungi around the same projectile and throw 1; ③ have **the deployer themselves** and **a teammate** throw 1 each | ① the block disappears at the instant the 6th interception ends and there are **no dropped items**; the log's `interceptCount` reads 1..6 in sequence, and the intercepted projectiles have no follow-up on landing (no explosion / no block written / no effect applied, D-99); ② `interceptCount` goes up by exactly 1 in total and the other 2 fungi make no particles; ③ both throws **pass through** (their landing settles as usual), `interceptCount` is **unchanged** and there are no particles — allies are neither intercepted nor charged (D-98) | D-87, D-88, D-98, D-99, RT-20 |
| 9 | a single fungus facing 2 projectiles in the same instant | only 1 is intercepted and `interceptCount` goes up by exactly 1. **The "already intercepted this tick" branch is a fallback**: with the same-tick screen in `findTarget`, the normal flow never reaches it; evidence comes from a probe calling `fire` directly (V-112/V-115) | D-88, RT-20 |
| 10 | after placing the Vibration Bean trap, dig out one block from each of the 6 neighbours (north, south, east, west, up, down) | the block is still in place; `FLOATING` in the block state is always false; after walking 200 blocks away and coming back (forcing a chunk reload) it is still there | D-91, RT-21 |
| 11 | a (non-ally) player walks to 3.9 blocks and then to 4.1 blocks from a Vibration Bean | 3.9 blocks triggers (all players within 4 blocks get 80 ticks of Confusion plus one explosion sound effect); 4.1 blocks does not trigger; after triggering the block disappears silently with no drops | D-92 |
| 12 | Friend Chives: one hit on 1 creature, one empty throw | hit: every creature within 6 blocks gets 200 ticks of Q-Bounce, one sound plays at the impact point, audible within 32 blocks and inaudible beyond 33; empty throw: creatures outside the range get no effect, and **no sound plays** | D-85 |
| 13 | throw a vanilla arrow / snowball / trident / ender pearl / Pumpkin Turret missile at a Point Defense Fungus once each, then throw one Friend Chives | all 5 pass through and `interceptCount` is unchanged; Friend Chives is intercepted and `interceptCount` +1 (proving the decision surface is the base class and not "nothing is intercepted") | D-87, §6.1, §3.4 boundary |
| 14 | a player at full health stands within 8 blocks and has 6 projectiles intercepted | health, hunger and coordinates are unchanged point by point; the block and its block state do not change; there is no `Explosion` construction line in the log | D-89 |
| 15 | Fang Trap Cactus: verify item by item against D-94's 8 rows | 3.0 blocks does not trigger and 2.5 blocks triggers; the 5 inner-ring fangs come first (on tick 2) and the 8 outer-ring fangs after (on tick 4), 2 ticks between the two rings; a player who is **actually damaged** by the fangs gets Slowness V (amplifier 4) for 60 ticks, applied exactly once; a player swept by the animation but losing no health gets **no** Slowness | D-93 ~ D-95, RT-22 |
| 16 | Zangief Cane: fall naturally after the default 16-block lift; then land once with `cancelFallDamage` turned off | default: health drops by exactly 7.5 points at the moment of landing, with no vanilla fall damage in the log; with it off: the damage is > 7.5 and varies with height; the death message shows `death.attack.farmstorm.zangief_slam`, attributed to the trap's owner | D-96, RT-22 |
| 17 | the resource and language-key counts are self-consistent | exactly 8 new PNGs under `textures/`, each named as in §9.1 (4 of them `friend_chives_stage0..3`, **magenta #D81E8A placeholders, not final art**); 1 new OGG under `sounds/`; 3 events in `sounds.json`; `zh_cn.json` and `en_us.json` with 297 keys each and identical key sets character for character; 22 new JSON files, each named as in §9.3 | §9.1 – §9.4 |

**Coverage statement**: of the 17 entries, 2 cover the registry and config numbers (#1 / #2), 3 cover the two Q-Bounce branches (#3 / #4 / #5), 2 cover Confusion (#6 / #7), 4 cover Point Defense Fungus (#8 / #9 / #13 / #14), 2 cover Vibration Bean (#10 / #11), 1 covers Friend Chives (#12), 1 covers Fang Trap Cactus (#15), 1 covers Zangief Cane (#16) and 1 covers the resources and language keys (#17). All seven RTs are covered by at least one entry: RT-17 → #3; RT-18 → #4; RT-19 → #7; RT-20 → #8 / #9; RT-21 → #10; RT-22 → #15 / #16; RT-23 → #1 / #2.

---

## Translator's notes

1. **Code fences.** The Chinese source opens the two blocks in §1.2 (the per-category subtotals) and §8.2 (the TOML fragment) with a single backtick on its own line. Those are rendered here as ``` and ```toml fences, matching the English convention already used in `docs/en/12-TECH-0.0.2.md`. The block content itself is unchanged apart from the translation of the TOML comments.
2. **The doubled 「与与」 in §0's heading.** It cannot be reproduced in English. The heading is translated naturally, and the note under it, which explains that the doubling is transcribed verbatim from `12-TECH-0.0.2.md` §0, is kept with the Chinese characters in place so the reference stays findable.
3. **Proper names.** man瓜 is rendered **Pumpkin Turret** and 胡罗贝 **Blast Carrot**, following the glossary already in `docs/en` (`00-DECISIONS.md`, `01-DESIGN.md`, `02-TECH-SPEC.md`). 刺球 is rendered **Fang Trap Cactus** per the fixed terminology, keeping the registry id `fang_trap_cactus`.
4. **§3.4 counts "five filters" but enumerates four** (not removed / not a cluster sub-projectile / inside the Euclidean 8 blocks / non-ally). §6.2 calls the same predicate a set of **four** filters. Both are translated as written; they are not reconciled here.
5. **§3.7's `CODEC` row says "3 fields" and then lists four components.** Kept as written.
6. **Chinese strings that are program output stay in Chinese**, character for character: `[FAIL] toml 多 22 个键`, `已核 registry 对象: 77 / 77`, `结论：FAIL 0 项`, `lang 键集一致，共 263 键`, `无 Mixin / 调试残留 / 空 catch`. They are what the tool and the server actually print, so translating them would make the acceptance criteria unverifiable.
7. **TOML/config comments.** As in `docs/en/12-TECH-0.0.2.md`, the comments inside the TOML fragment are translated while every key name, value and range stays byte-identical.
