# 12 · v0.0.2 Technical Specification

> Subject: Farmstorm v0.0.2 (NeoForge 21.1.213 / Minecraft 1.21.1 / Java 21)
> Single source of truth: `.work/v002-BRIEF.md` (hereafter BRIEF). Every number, term and decision ID that appears in BRIEF is copied verbatim — not rewritten, not recomputed, not rephrased.
> Relation to `docs/zh/00-DECISIONS.md` (hereafter DEC): for what is new in v0.0.2, BRIEF §2 prevails; for what already existed in v0.0.1, DEC prevails and BRIEF does not override it (D-49).
> This document describes **architecture and contracts** only; it delivers no implementation. Class names, method signatures and skeleton fragments are permitted.
> **Added by this document** = content that BRIEF does not state, derived here from the project's existing conventions and explicitly marked as such. Any conclusion without that mark comes from BRIEF, DEC, or the actual source under `src/`.
> The whole text uses declarative sentences in Simplified Chinese, with no honorifics and no marketing language; every value carries a unit and a range.

---

## 0. Scope of This Document and Its Relation to 02-TECH-SPEC

### 0.1 What this document covers

| Item | Conclusion |
|---|---|
| Complexity tier | L2 (cross-registration layer / behavior layer / client rendering / media pipeline; multiple deliverables; 7 numbered risks) |
| In scope for this document | Build baseline changes, per-category verification of the 77 registry objects, class design for the 13 new objects, the technical plan for three client subsystems, phase synchronization of Zangief Cane, the audio and texture pipeline, the resource and data file inventory, the full list of new config keys, the proof that the 0-packet constraint holds, the risk register RT-10…RT-16, the acceptance self-check table |
| Out of scope for this document | Gameplay value statements (`11-DESIGN-0.0.2.md`), the itemization of D-49…D-78 (`10-DECISIONS-0.0.2.md`), task breakdown and dependency graph (`13-TASKS-0.0.2.md`), pixel drafts and the audio assets themselves (the `tools/` scripts that follow `06-TECH-SPEC.md`) |
| Readership | The people who will write `src/` straight from this document during implementation; and the people who verify item by item against §11 during acceptance |

### 0.2 Relation to `docs/zh/02-TECH-SPEC.md`

`02-TECH-SPEC.md` describes the 64 registry objects of v0.0.1 and every existing contract, and is **frozen by D-49 — not one character changes in v0.0.2**. This document is its incremental sequel: it does not recopy, rewrite or override it.

| Topic | Where it lives in v0.0.1 | How this document handles it |
|---|---|---|
| The 64 existing registry objects | `02-TECH-SPEC.md` §3.1–§3.11 | Appears in §2 only as "existing stock"; no per-item table is repeated |
| Existing class contracts (items, blocks, BE, behavior, projectiles, converter) | `02-TECH-SPEC.md` §4.1–§4.13 | Mostly referenced; rewritten only where v0.0.2 must change or extend it |
| Server-authoritative model and 0 packets (C-6) | `02-TECH-SPEC.md` §5 | §9 uses it for an **incremental proof**, and does not restate the principle |
| Resource and language key inventory | `02-TECH-SPEC.md` §6 / §7 | Lists only new files and new keys; existing counts come from BRIEF §4.1 |
| Risks RT-01…RT-09 | `02-TECH-SPEC.md` §9 | IDs are not reused; v0.0.2 risks are numbered from RT-10, in §10 here |
| Acceptance self-check | `02-TECH-SPEC.md` §11 | §11 here lists only the v0.0.2 incremental checks |

**One failed statement must be flagged up front**: `02-TECH-SPEC.md` §4.9 and `09-IMPL-VS-PLAN.md` §1 both say "Demolition always uses the cobblestone 6.0 and breaks nothing", which is not what the source does (only Melon Grenade and Melon Slice Bomb carry the 6.0 threshold; Ubi Server / Toxic Ubi Server use 3.6e6). The erratum itself is registered in `10-DECISIONS-0.0.2.md` (D-78); wherever this document touches a demolition threshold it follows D-78 and does not carry over the wrong sentence from `02-TECH-SPEC.md` §4.9.

### 0.3 Self-consistency commitment for the three inventories

BRIEF §4.2 requires this document to produce three mutually consistent numbers. The approach here is to **list every item first and count afterwards**, so that the number of rows in an inventory and the count written down are always exactly equal:

| Inventory | Subsection | Inventory rows | BRIEF §4.1 existing stock | New total |
|---|---|---|---|---|
| New resource files | §7.2 | **36** | 262 | **298** |
| New language file keys | §7.3 | **39** | 222 | **261** |
| New config keys | §8.1 | **22** | 117 | **139** |

Two hard checks (pinned from both sides by `runData` and by the startup log during implementation):

- The number of new `farmstorm.configuration.*` language keys, **22**, equals the number of table rows in §8.1, **22**; the key sets of `zh_cn` and `en_us` must be character-for-character equal (`222 + 39 = 261`, both files 261).
- Total resource files = 262 before the migration + 36 = **298**; split by category = JSON 169+24=193, PNG 91+8=99, OGG 0+2=2, GLSL 0+2=2, TOML 1, `pack.mcmeta` 1 → 193+99+2+2+1+1 = 298.

### 0.4 Numeric conventions (copied from BRIEF §4)

| Metric | v0.0.1 | v0.0.2 |
|---|---|---|
| `CropProfile` entries | 15 | **19** |
| Throwable / Deployable / Edible | 8 / 5 / 2 | **10 / 6 / 3** |
| Demolition | 4 | **4** (unchanged) |
| Trap | 2 | **3** |
| Turret | 3 | **3** |
| Buff | 7 | **8** |
| Construction | 1 | **3** |
| Damage | 8 | **9** |
| Total registry objects | 64 | **77** |
| items | 22 | **27** |
| blocks | 20 | **22** |
| block_entities | 6 | **6** |
| entity_types | 9 | **11** |
| mob_effects | 0 | **2** |
| sound_events | 0 | **2** |
| Converting recipes | 21 | **24** |
| Crafting recipes | 0 | **2** |
| Advancements | 12 | **15** |
| Lucky Berry pool | 14 | **16** |

The target text of the startup self-check (`Farmstorm#logSelfCheck` prints it; changing a number means changing it in step):

```
自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK
```

### 0.5 Registration rules for "pending verification"

Every preferred option in BRIEF §7 carries a "pending verification" mark. The handling here:

1. **Preferred options are not swapped** (precondition ① of BRIEF §7); pending items keep their mark verbatim, and "how to verify during implementation" is appended — a specific command, a specific log line, or a specific manual test.
2. Pending items come in two kinds: **behavior pending verification** is attached to the matching entry among RT-10…RT-16; **API existence / signature pending verification** is always attached to RT-01 ("patch-level API drift", `02-TECH-SPEC.md` §9) and opens no new ID.
3. Pending verification does not mean "do it later": every item is given a **current default implementation** and a **degradation path**, to be tried in order during implementation.

### 0.6 Binding of the three client subsystems to the two effects (added by this document)

D-50 only stipulates "route it through a hidden MobEffect" and does not name which effect carries which item. Following the two remarks in BRIEF §3.2 (`wild_hunter` = visible, with icon; `zangief_piledriver` = hidden, pure synchronization channel) and the source text of `plan0.0.2.txt` (line 40, "the view turns black and white, and all creatures in view are highlighted in red", written inside the Wild Hunter entry; line 29, "the view is flipped upside down… all keyboard and mouse input is disabled", written inside the Zangief Cane entry), this document settles it as follows:

| Intrusive effect | MobEffect that carries it | Criterion | Basis |
|---|---|---|---|
| Full-screen grayscale (including GUI) | `farmstorm:wild_hunter` | on whenever the effect is present | D-61 + `plan0.0.2.txt` line 40 |
| Red silhouette | `farmstorm:wild_hunter` | on whenever the effect is present | D-62 + same as above |
| Camera flip / spin / input lock | `farmstorm:zangief_piledriver` | amplifier 0/1/2, all three phases are inside the lock window | D-59 / D-60 / D-70 + `plan0.0.2.txt` line 29 |

This binding fixes the **source of state** for §4 and §5: the client never asks the server "do you want me gray", it only reads the effect on itself. That way "the server tells the client the state" lands entirely on vanilla effect synchronization and needs no new packet at all (§9).

---

## 1. Build Baseline Changes

### 1.1 `gradle.properties`: one line only

```diff
-mod_version=0.0.1
+mod_version=0.0.2
```

| Item | v0.0.1 | v0.0.2 | Note |
|---|---|---|---|
| `minecraft_version` | 1.21.1 | 1.21.1 | unchanged |
| `neo_version` | 21.1.213 | 21.1.213 | unchanged (no chasing new releases, see RT-01) |
| `mod_version` | 0.0.1 | **0.0.2** | the only change |
| `mod_license` | All Rights Reserved | unchanged | the D-66 non-commercial personal-use statement goes in the README and the Release; the license itself does not change |
| Java `toolchain` | 21 | 21 | unchanged |
| ModDevGradle | `net.neoforged.moddev` 2.0.148 | unchanged | unchanged |
| `build.gradle` | — | **untouched** | no new dependency: audio and GLSL are resource files, no runtime library is introduced |

### 1.2 New resource directories

Under `assets/farmstorm/` v0.0.1 has only `textures/{block,item,gui}`. v0.0.2 needs 5 new directories (under `src/main/resources/`):

| Directory | Purpose | Basis |
|---|---|---|
| `assets/farmstorm/sounds/` | 2 Ogg Vorbis audio files | D-53 / D-66 |
| `assets/farmstorm/textures/misc/` | 1×1 white texture (for the silhouette) | BRIEF §7.2 |
| `assets/farmstorm/textures/mob_effect/` | Wild Hunter effect icon, 18×18 | D-53 / D-66 |
| `assets/farmstorm/shaders/core/` | grayscale shader program (`.json` + `.vsh` + `.fsh`) | BRIEF §7.1 |
| `assets/farmstorm/post_effect/` | grayscale post-effect declaration (used by the RT-10 degradation branch) | BRIEF §3.2 |

`sounds.json` goes to `assets/farmstorm/sounds.json` (not inside the `sounds/` directory, isomorphic to vanilla).

### 1.3 Build and acceptance gates

| Gate | Command | Pass criterion |
|---|---|---|
| Compile + data generation | `.tools\gw.cmd build` | exit code 0; the files added under `src/generated/resources` match §7.2 item by item |
| Resource validation | the `processResources` phase of `.tools\gw.cmd build` | the phase fails outright when an `.ogg` referenced by `sounds.json` or a `.png` referenced by a model is missing |
| Startup self-check | the client startup log | all 6 "自检 …" lines of §11 are OK |
| Artifact | `dist/farmstorm-0.0.2+mc1.21.1.jar` | exists, and is larger than v0.0.1 (the two ogg files are packed in) |

**This document runs no build command**: this file is a specification, not a work log; the three gates above are implementation-phase acceptance actions.

---

## 2. Incremental Registry Object Summary (77-object verification)

### 2.1 Per-registry verification table

Counting rule: **77 registry objects** (across 12 registries), plus 1 `ModConfigSpec` (not a registry; the config file `config/farmstorm-server.toml`). `recipe` / `tag` / `advancement` / `post_effect` are data files, not registry objects, and are not counted in this table (see §7).

| # | registry | v0.0.1 | Delta | v0.0.2 | Delta detail |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 22 | +5 | **27** | `molotov`, `zangief_cane`, `wild_apple`, `compressed_puffed_wheat`, `compressed_hay_block` (BlockItem) |
| 2 | `Registries.BLOCK` | 20 | +2 | **22** | `compressed_hay_block` (plain `Block`), `zangief_cane` (`DeployedCropBlock`) |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 6 | +0 | **6** | reuses `DeployedCropBlockEntity`: `zangief_cane` is added to the legal block set of an existing type (§3.14) |
| 4 | `Registries.ENTITY_TYPE` | 9 | +2 | **11** | `molotov`, `compressed_puffed_wheat` |
| 5 | `Registries.MOB_EFFECT` | 0 | +2 | **2** | `wild_hunter`, `zangief_piledriver` |
| 6 | `Registries.SOUND_EVENT` | 0 | +2 | **2** | `wild_hunter_loop`, `zangief_piledriver` |
| 7 | `Registries.RECIPE_TYPE` | 1 | +0 | **1** | reuses `farmstorm:converting` |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | +0 | **1** | reuses `ConverterRecipeSerializer` |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | +0 | **1** | the 5 new items are appended to `farmstorm:main` |
| 10 | `Registries.DAMAGE_TYPE` (data-driven, code-side key) | 2 | +0 | **2** | Molotov deals no damage (§2.4) |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | +0 | **1** | — |
| 12 | `Registries.MENU` | 1 | +0 | **1** | — |
| | **Total** | **64** | **+13** | **77** | |

Per-category subtotals (for verification; must equal the table row by row):

```
items           27   （22 + 5）
blocks          22   （20 + 2）
block_entities   6   （ 6 + 0）
entity_types    11   （ 9 + 2）
mob_effects      2   （ 0 + 2）
sound_events     2   （ 0 + 2）
recipe types     1   （ 1 + 0）
recipe serializers 1 （ 1 + 0）
creative tabs    1   （ 1 + 0）
damage_types     2   （ 2 + 0）
data_components  1   （ 1 + 0）
menu types       1   （ 1 + 0）
--------------------------------
相加 = 27+22+6+11+2+2+1+1+1+2+1+1 = 77
```

**Verification conclusion**: `64 + 13 = 77`, and the 12 per-category subtotals also add up to `77`; the two computations cross-check each other.

### 2.2 The 13 new objects, named one by one

| # | registry | Full ID | Class / form | Basis |
|---|---|---|---|---|
| 1 | items | `farmstorm:molotov` | `ThrowableCropItem` | BRIEF §3.2 |
| 2 | items | `farmstorm:zangief_cane` | `DeployableCropItem` (BlockItem of the deployed block) | BRIEF §3.2 |
| 3 | items | `farmstorm:wild_apple` | `FoodCropItem` | BRIEF §3.2 |
| 4 | items | `farmstorm:compressed_puffed_wheat` | `ThrowableCropItem` | BRIEF §3.1 #19 |
| 5 | items | `farmstorm:compressed_hay_block` | `BlockItem` | BRIEF §3.2 |
| 6 | blocks | `farmstorm:compressed_hay_block` | `Block` (no custom subclass, see §3.9) | D-57 |
| 7 | blocks | `farmstorm:zangief_cane` | `DeployedCropBlock` | D-68 |
| 8 | entity_types | `farmstorm:molotov` | `MolotovProjectile` (extends `ThrownCropEntity`) | D-58 |
| 9 | entity_types | `farmstorm:compressed_puffed_wheat` | `CompressedPuffedWheatProjectile` (extends `ThrownCropEntity`) | D-77 |
| 10 | mob_effects | `farmstorm:wild_hunter` | `WildHunterEffect` (visible, with icon) | BRIEF §3.2 |
| 11 | mob_effects | `farmstorm:zangief_piledriver` | `ZangiefPiledriverEffect` (hidden, pure synchronization channel) | D-50 / BRIEF §7.5 |
| 12 | sound_events | `farmstorm:wild_hunter_loop` | `SoundEvent` (variable-range event) | D-76 |
| 13 | sound_events | `farmstorm:zangief_piledriver` | `SoundEvent` (variable-range event) | D-66 |

Both `entity_types` entries reuse the v0.0.1 `projectile(...)` factory: `MobCategory.MISC`, `sized(0.25F, 0.25F)`, `clientTrackingRange(4)`, `updateInterval(10)`, character for character the same as the 8 existing projectiles (source: `registry/FarmstormEntityTypes.java`).

### 2.3 Why 13 and not more

None of the three new crops has a planted form (D-51): the `cropBlock` and `seedItem` of `molotov` / `zangief_cane` / `wild_apple` are all `null`, so **no crop block is registered and no seed item is registered** — this single fact absorbs the 3-block + 3-item delta. The precedent is `melon_slice_bomb` (likewise `cropBlock = null`, `seedItem = null`; source `util/CropProfile.java`), so no new mechanism is needed for this.

### 2.4 Increments that are not registry objects (not part of the 77)

BRIEF §3.2 mixes registry objects and data files in one table. This document separates them by nature and puts every data file into the itemized inventory of §7.2:

| Increment | Count | Nature | Where in this document |
|---|---|---|---|
| Converting recipes | +3 (`molotov` / `zangief_cane` / `wild_apple`), 21+3=24 total | data file | §7.2 #27–#29 |
| Crafting recipes | +2 (converter 1 + compressed puffed wheat 1) | data file | §7.2 #25–#26 |
| item tag (new) | +1 `farmstorm:converter_fuel` | data file | §7.2 #35 |
| vanilla tag (append / new) | 2 places: `minecraft:mineable/pickaxe` appended, `minecraft:needs_diamond_tool` created | data file | §7.2 #36 (new) + the existing `data/minecraft/tags/block/mineable/pickaxe.json` appended (not a new file, so it does not enter the 36 rows) |
| advancements | +3 | data file (datapack registry, no `DeferredRegister`) | §7.2 #30–#32 |
| post_effect | +1 | data file | §7.2 #15 |
| damage_types | +0 | Molotov **deals no damage**: the landing explosion has power 0, and `BlastRule.NEVER_BREAK` breaks no block (D-58), so there is no damage source to attribute | this section |
| data components / recipe type / recipe serializer / creative tabs / menu types | +0 | all reused | this section |

**Why damage_types can be +0**: the `FarmstormExplosion` constructor takes a `DamageSource`, but when `damageCap <= 0` it skips the entire damage section (constructor comment in `explosion/FarmstormExplosion.java`: "<= 0 means pure destruction, skip the damage section"). Molotov uses `damageCap = 0` plus `BlastRule.NEVER_BREAK`, both channels are no-ops, so not even a damage source is needed and there is no reason to add a `farmstorm:molotov` damage type.

---

## 3. Design of the New Classes

Every class gets five items: **responsibility / key methods / which config keys it reads / which registry objects it writes / basis**. Every key name that appears under "which config keys it reads" can be looked up in the 22-row list of §8.1; "which registry objects it writes" lists registry objects only, data files are in §7.

### 3.0 Overview of additions and changes

| Class | New / changed | One-line responsibility | Which registry objects it writes |
|---|---|---|---|
| `CropProfile` | changed | 4 new profile constants + `ALL` 19 entries + `LUCKY_BERRY_POOL` 16 entries | none (input for the registration layer) |
| `DeployedKind` | changed | enum +1 value `ZANGIEF_CANE(10)` | none |
| `FarmstormMobEffects` | **new** | `DeferredRegister` carrying 2 `MobEffect` | mob_effects ×2 |
| `FarmstormSounds` | **new** | `DeferredRegister` carrying 2 `SoundEvent` | sound_events ×2 |
| `WildHunterEffect` | **new** | visible effect: attribute bonuses + the client reads it to decide grayscale and silhouette | via `FarmstormMobEffects` |
| `ZangiefPiledriverEffect` | **new** | hidden effect: carries nothing but the amplifier phase number | via `FarmstormMobEffects` |
| `MolotovProjectile` | **new** | the landing point triggers a power-0 explosion; inside the sphere, fire / magma block is rolled cell by cell | entity_types ×1 |
| `CompressedPuffedWheatProjectile` | **new** | Puffed Wheat's replacement block becomes compressed hay block | entity_types ×1 |
| `ZangiefCaneTrap` | **new** | `TrapBehavior` implementation: spherical 1.5-block trigger | none |
| `ZangiefPiledriverController` | **new** | server-side three-phase state machine | none |
| `WildHunterCombat` | **new** | server-side target re-check, damage and knockback, kill extension | none |
| `GrayscalePass` | **new** | client full-screen grayscale pass | none |
| `WildHunterOutline` | **new** | client red silhouette | none |
| `InputLockHandler` | **new** | client input lock (5 events) | none |
| `ZangiefCameraController` | **new** | client render-layer angles (roll / pitch / yaw) | none |
| `WildHunterLoopSound` | **new** | client looping `SoundInstance` | none |
| `WildHunterDashClient` | **new** | client dash prediction | none |
| `compressed_hay_block` | **no new class** | a pure-property block, using vanilla `Block` | blocks ×1 + items ×1 |

### 3.1 `CropProfile`: 4 new constants, `ALL` 19 entries, `LUCKY_BERRY_POOL` 16 entries

**Responsibility**: the static profile of a single improved crop. Items / blocks / entities / tooltips / self-check all take their data from here, so that the same registry ID is not spelled out as a string in several places (class comment of `util/CropProfile.java`).

**Key methods: unchanged**. Not one signature of the three private factories `crop()` / `throwable()` / `deployed()` is touched; each new constant is placed according to "use whichever factory the available arguments allow":

| # | Constant | Construction | type | categories | cropBlock | seedItem | kind | Basis |
|---|---|---|---|---|---|---|---|---|
| 16 | `MOLOTOV` | `throwable("molotov", …)` unusable → **`new CropProfile(...)`** | `THROWABLE` | `DAMAGE, CONSTRUCTION` | `null` | `null` | `null` | D-51 |
| 17 | `ZANGIEF_CANE` | `new CropProfile(...)` | `DEPLOYABLE` | `TRAP` | `null` | `null` | `ZANGIEF_CANE` | D-51 |
| 18 | `WILD_APPLE` | `new CropProfile(...)` | `EDIBLE` | `BUFF` | `null` | `null` | `null` | D-51 |
| 19 | `COMPRESSED_PUFFED_WHEAT` | `new CropProfile(...)` | `THROWABLE` | `CONSTRUCTION` | `null` | `null` | `null` | D-56 |

All four constants must go straight through the record constructor, because each of the three private factories calls `blockKey()` / `itemKey()` (`ResourceKey.create`) on `cropBlockPath` / `seedItemPath`, and passing `null` would NPE during class initialization. The precedent is `MELON_SLICE_BOMB`: it likewise has no crop block and is written as a bare `new CropProfile(...)`.

Stack tier: `MOLOTOV` and `COMPRESSED_PUFFED_WHEAT` take `THROWABLE_STACK_TIER` (16, D-16); `ZANGIEF_CANE` and `WILD_APPLE` take `OTHER_STACK_TIER` (64, D-16).
Attach face: `ZANGIEF_CANE` takes `Set.of(AttachFace.FLOOR)` (floor only, consistent with the two existing traps).

**The order of `ALL`** (= rows 1–19 of BRIEF §3.1):

```
1  PUFFED_WHEAT            11 FANG_TRAP_CACTUS
2  BLAST_CARROT            12 BAMBOO_FIRECRACKER
3  UBI_SERVER              13 RED_MUSHROOM_PUFF
4  TOXIC_UBI_SERVER        14 BROWN_MUSHROOM_PUFF
5  POWER_BEET              15 LUCKY_BERRY
6  MELON_SLICE_BOMB        16 MOLOTOV
7  MELON_GRENADE           17 ZANGIEF_CANE
8  PUMPKIN_TURRET          18 WILD_APPLE
9  AWAKENED_TORCHFLOWER    19 COMPRESSED_PUFFED_WHEAT
10 AWAKENED_PITCHER_PLANT
```

The order is not decoration: `BY_ID` builds a `LinkedHashMap` from it, and tooltips and the startup self-check print in this order (source comment: "the iteration order of `Set.of` is decided by the JVM hash salt, may differ run to run, and must not be used for display").

**`LUCKY_BERRY_POOL`: from automatic derivation to three explicit exclusions**. v0.0.1 is `ALL.stream().filter(p -> p != LUCKY_BERRY)`; v0.0.2 must be written as three explicit exclusions:

```java
/** D-55 + D-56: exclude lucky_berry, wild_apple, compressed_puffed_wheat from the 19 → 16 entries. */
public static final List<CropProfile> LUCKY_BERRY_POOL = ALL.stream()
        .filter(profile -> profile != LUCKY_BERRY)
        .filter(profile -> profile != WILD_APPLE)
        .filter(profile -> profile != COMPRESSED_PUFFED_WHEAT)
        .toList();
```

| Exclusion | Reason | Basis |
|---|---|---|
| `lucky_berry` | it cannot produce itself (the existing v0.0.1 rule) | D-17 |
| `wild_apple` | the strongest item of this version, it should not be produced at random by 1 berry | D-55 |
| `compressed_puffed_wheat` | a second-tier composite, not an "improved crop" | D-56 |

`19 - 3 = 16`, consistent with "Lucky Berry pool 16" in BRIEF §4. **Note that D-55's original text says "two explicit exclusions"**, which taken literally gives 17 entries; the number 16 comes from the third exclusion in D-56 and from the number table in BRIEF §4, and this document follows the number table (already aligned in the conventions of §0).

**Which config keys it reads**: this class reads no config; the stack counts other than `throwStack` are decided by `FarmstormItems` reading `stack.throwable` / `stack.other` (an existing v0.0.1 mechanism).
**Which registry objects it writes**: none. It is an input to the registration layer.

### 3.2 `DeployedKind`: +1 value `ZANGIEF_CANE(10)`

**Responsibility**: the **implementation-dimension** enum of deployed blocks (not `CropType`, not `CropCategory`). `scanInterval` lives on the enum because the staggered tick of `DeployedCropBlockEntity` needs it before the behavior object is ready (source comment, §4.5).

```java
public enum DeployedKind {
    BLAST_CARROT(10),
    FANG_TRAP_CACTUS(10),
    PUMPKIN_TURRET(10),
    AWAKENED_TORCHFLOWER(100),
    AWAKENED_PITCHER_PLANT(100),
    ZANGIEF_CANE(10);        // new: consistent with the other two traps
    …
}
```

**Key methods**: `scanInterval()` unchanged. The new value takes 10 ticks (0.5 seconds), consistent with `BLAST_CARROT` / `FANG_TRAP_CACTUS` — a trap is "settled on the first trigger", and a 10-tick scan delay at a 1.5-block radius misses nothing: the fastest player covers 0.28 blocks/tick (near the sprint + Speed II ceiling), 10 ticks is at most 2.8 blocks, so the 0.5-second judgement window is enough to catch "walking past" (**added by this document**: the convention follows the measured feel of the two existing traps; no new value is introduced).

**Adding one enum value breaks two exhaustive switches** (a compile-time error, which is a deliberate sentinel, not an accident):

| File | Location | What must be added | Is it on the BRIEF §3.3 whitelist |
|---|---|---|---|
| `behavior/DeployedBehaviors.java` | the `switch` expression of `of(DeployedKind)` | `case ZANGIEF_CANE -> ZANGIEF_CANE_TRAP;` + one static singleton field | **no** (see the contradiction recorded in §3.14) |
| `registry/FarmstormBlockEntities.java` | the `switch` expression of `deployedOf(DeployedKind)` | `case ZANGIEF_CANE -> FANG_TRAP_CACTUS.get();` | **no** (same as above) |

**Which config keys it reads**: none directly; the behavior object `ZangiefCaneTrap` reads `zangief_cane.triggerRadius`.
**Which registry objects it writes**: none.

### 3.3 `FarmstormMobEffects` (new `DeferredRegister`)

**Responsibility**: the first `MobEffect` registrar of this mod. `mob_effects` is 0 in v0.0.1 (BRIEF §4), so this class is entirely new and inherits no existing registrar.

```java
public final class FarmstormMobEffects {
    public static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(Registries.MOB_EFFECT, Farmstorm.MOD_ID);

    /** Visible, with icon: the carrier of grayscale + red silhouette + three attribute bonuses (D-61 / D-62). */
    public static final DeferredHolder<MobEffect, WildHunterEffect> WILD_HUNTER =
            REGISTER.register("wild_hunter", WildHunterEffect::new);

    /** Hidden, no icon: carries nothing but the amplifier phase number (D-50 / BRIEF §7.5). */
    public static final DeferredHolder<MobEffect, ZangiefPiledriverEffect> ZANGIEF_PILEDRIVER =
            REGISTER.register("zangief_piledriver", ZangiefPiledriverEffect::new);

    private FarmstormMobEffects() {}
}
```

**Key methods**: no business method, only two `Holder`s. The mount point is one line in the `Farmstorm` constructor, `FarmstormMobEffects.REGISTER.register(modBus);` (in parallel with the 9 existing registrars).

**Which config keys it reads**: none.
**Which registry objects it writes**: `farmstorm:wild_hunter`, `farmstorm:zangief_piledriver` (mob_effects ×2).

**Why not some other way**: D-50 already rules it out — the state of the three intrusive effects must not go through `CustomPacketPayload` and may only ride a hidden `MobEffect`. The three components of a `MobEffect` — present / absent + amplifier + remaining duration — happen to equal the entire state that needs synchronizing, and `ClientboundUpdateMobEffectPacket` delivers all three for free (§9.2).

### 3.4 `FarmstormSounds` (new `DeferredRegister`)

```java
public final class FarmstormSounds {
    public static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(Registries.SOUND_EVENT, Farmstorm.MOD_ID);

    /** D-76: looping sound; the 16-block audible range is decided jointly by the client SoundInstance and sounds.json. */
    public static final DeferredHolder<SoundEvent, SoundEvent> WILD_HUNTER_LOOP =
            REGISTER.register("wild_hunter_loop",
                    () -> SoundEvent.createVariableRangeEvent(Farmstorm.id("wild_hunter_loop")));

    /** D-66: Zangief Cane trigger sound, played once. */
    public static final DeferredHolder<SoundEvent, SoundEvent> ZANGIEF_PILEDRIVER =
            REGISTER.register("zangief_piledriver",
                    () -> SoundEvent.createVariableRangeEvent(Farmstorm.id("zangief_piledriver")));

    private FarmstormSounds() {}
}
```

**Key methods**: none. `createVariableRangeEvent` is used instead of `createFixedRangeEvent` because the audible range of the looping sound must be controlled by `sounds.json` (§6.3) together with the `SoundInstance` (16 blocks); a fixed-range event pins the radius on the server.

**Which config keys it reads**: none.
**Which registry objects it writes**: `farmstorm:wild_hunter_loop`, `farmstorm:zangief_piledriver` (sound_events ×2).

### 3.5 `WildHunterEffect` (the visible effect)

**Responsibility**: the state carrier of Wild Hunter. It **does no gameplay** itself (targeting / dash / kill extension are all in the server classes of §3.12); it owns three registration-layer things only: category and icon, mounting and unmounting the three attribute bonuses, and giving the client a criterion for "is the effect there".

| Shape item | Value | Basis |
|---|---|---|
| Class | `public class WildHunterEffect extends MobEffect` | in 1.21.1 `MobEffect` is a class, not an interface |
| Category | `MobEffectCategory.BENEFICIAL` | from the player's viewpoint it is a buff |
| Color (particles and icon background) | `0x8B1A1A` (dark red) **added by this document** | same color family as the red silhouette |
| Icon | `assets/farmstorm/textures/mob_effect/wild_hunter.png` (18×18, D-53) | D-66 |
| Visibility | `visible = true`, `showIcon = true` (passed at apply time) | BRIEF §3.2 "visible, with icon" |
| Instantaneous | `isInstantenous() = false` | it has a duration |

**Key methods**:

```java
@Override
public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
    attributes.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_ID,
            FarmstormConfig.wildHunterSpeedBonus() / 100.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    attributes.addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_ID,
            FarmstormConfig.wildHunterAttackSpeedBonus() / 100.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    attributes.addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID,
            FarmstormConfig.wildHunterAttackDamageBonus() / 100.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
}

@Override
public void removeAttributeModifiers(AttributeMap attributes, int amplifier) { …three removeAttributeModifier calls… }
```

The ids of the three `AttributeModifier`s must be fixed `ResourceLocation`s (`farmstorm:wild_hunter_speed` etc.) declared as static constants — creating a new id each time makes them impossible to remove. **Pending verification (attached to RT-01)**: whether the 1.21.1 `AttributeModifier` constructor is already `(ResourceLocation, double, Operation)` (the id type changed in 1.21); the compile-time signature decides.

**Which config keys it reads**: `wild_hunter.speedBonusPercent` (30.0 %, 0.0–1000.0), `wild_hunter.attackSpeedBonusPercent` (30.0 %, 0.0–1000.0), `wild_hunter.attackDamageBonusPercent` (40.0 %, 0.0–1000.0).
**Which registry objects it writes**: mob_effects ×1, via `FarmstormMobEffects.WILD_HUNTER`.

**Why the attribute bonuses sit on the `MobEffect` instead of in a tick**: vanilla applies an effect's attribute modifiers on **both sides** (`LivingEntity#onEffectAdded` is called on the client as well), so movement speed / attack speed / damage are felt immediately in client-side prediction with no extra synchronization; writing a manual `getAttribute().addTransientModifier` from `ServerPlayer` every tick instead produces a "one frame late on the client" and forces you to manage the unmount yourself.

### 3.6 `ZangiefPiledriverEffect` (hidden effect, pure synchronization channel)

**Responsibility**: **exactly one** — carry "the phase number of Zangief Cane's piledriver" from the server to the client. It adds no attribute, deals no damage and emits no particle.

| Shape item | Value | Basis |
|---|---|---|
| Class | `public class ZangiefPiledriverEffect extends MobEffect` | **pending verification (attached to RT-01)**: whether the 1.21.1 `MobEffect` is an extendable class (constructor `(MobEffectCategory, int color)`), and whether the six-argument constructor `MobEffectInstance(MobEffect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon)` exists; the compile-time result decides |
| Category | `MobEffectCategory.HARMFUL` | being piledriven is a negative experience |
| Color | `0x000000` (hidden effect, the color is never seen) | — |
| Visibility | `visible = false`, `showIcon = false` (passed at apply time) | BRIEF §3.2 "hidden, pure synchronization channel" |
| amplifier semantics | `0` = already flipped, waiting to levitate / `1` = levitating / `2` = falling and spinning | BRIEF §7.5 |
| duration semantics | remaining ticks of the current phase (reset on every switch) | §5.2 |

**Key methods**:

```java
@Override public boolean isDurationEffectTick(int duration, int amplifier) { return false; }  // nothing depends on the vanilla tick
@Override public boolean shouldRenderHUD() { return false; }        // hidden: draw no HUD icon
@Override public boolean shouldRenderInvText() { return false; }    // hidden: write nothing into the inventory description
/** addAttributeModifiers / removeAttributeModifiers are not overridden: this effect has no attribute impact. */
```

**Pending verification (attached to RT-15)**: whether 1.21.1's `MobEffect` still offers the two overridable methods `shouldRenderHUD()` / `shouldRenderInvText()`; if only `shouldRenderIcon()` exists, override that instead — the visibility of the hidden effect is ultimately guaranteed by the `MobEffectInstance` constructor arguments `(visible, showIcon)`, and the override on the class is only a second safety net.

**Which config keys it reads**: none. The duration is computed from the config by the applying side (`ZangiefCaneTrap` / `ZangiefPiledriverController`) and passed in.
**Which registry objects it writes**: mob_effects ×1, via `FarmstormMobEffects.ZANGIEF_PILEDRIVER`.

### 3.7 `MolotovProjectile` (projectile #9)

**Responsibility**: two things at the landing point — (1) trigger a **power-0** explosion that serves only as a landing-point trigger, (2) walk the blocks inside a sphere centered on the landing point with the sphere radius, and for every cell whose **top surface touches air** roll **independently**: 85 % place fire in the air position above it, 15 % place a magma block.

```java
public class MolotovProjectile extends ThrownCropEntity {
    @Override protected CropProfile profile() { return CropProfile.MOLOTOV; }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        BlockPos center = landingBlock(pos, directHit == null);   // same landing back-solving as Puffed Wheat
        trigger(pos);                                             // (1) power-0 explosion, a trigger only
        scatter(level, center);                                   // (2) independent roll per cell
    }
}
```

**Key methods**:

| Method | What it does | Key point |
|---|---|---|
| `profile()` | returns `CropProfile.MOLOTOV` | must be a **static constant**: `getDefaultItem()` of `ThrownCropEntity` is reached during `defineSynchedData`, before the subclass instance fields are initialized (base class comment) |
| `onImpact(ServerLevel, Vec3, Entity)` | the single behavior entry point, server only | the base class guarantees one settlement per spawn (`settled` re-entrancy guard) |
| `trigger(Vec3)` | `new FarmstormExplosion(level, owner, FarmstormDamageTypes.cropBlast(level, owner), x, y, z, power, BlastRule.NEVER_BREAK, 0F).detonate()` | `power = molotov.triggerExplosionPower` (default 0), `damageCap = 0` (damage section skipped), `NEVER_BREAK` (0 blocks destroyed) |
| `scatter(ServerLevel, BlockPos)` | walk the sphere + `nextFloat()` per cell | criteria below |
| `landingBlock(Vec3, boolean)` | the block on the inner side of the hit face | same algorithm as `PuffedWheatProjectile` (`LANDING_PROBE = 0.1` epsilon), see the duplicated-code note in §3.8 |

**The criteria of `scatter` (fixed one by one, so that implementation does not invent its own)**:

1. Candidate set = the cells of `BlockPos.betweenClosed(center.offset(-r,-r,-r), center.offset(r,r,r))` that satisfy `center.distSqr(pos) <= r*r` (`r = molotov.effectRadius`, default 4.0 blocks → about 268 cells inside the sphere).
2. "The top surface of the cell touches air" = `level.getBlockState(pos).isSolidRender(...)` is true **and** `level.getBlockState(pos.above()).isAir()` is true. **Added by this document**: only "solid + air above" counts; pass-through blocks (grass, snow layers) are not accepted, which avoids the visual error of "placing fire on the grass of a block" instead of "placing fire on top of the grass block".
3. Every candidate cell rolls **independently**: `float roll = level.random.nextFloat() * 100.0F; if (roll < fireChance) place fire else place magma block`. `fireChance = molotov.fireChance` (default 85.0 %) → the remaining 15 % is automatically magma block, a second key is not needed.
4. The write position = **the air cell above** (`pos.above()`), always `level.setBlock(target, state, Block.UPDATE_ALL)`.
5. Nothing is replaced and nothing is destroyed: not one byte of `pos` itself is touched; the original block drops nothing and emits no particle.
6. The write ceiling of a single throw = the number of candidate cells in the sphere (≈268), with no separate circuit breaker — 268 `setBlock` calls in the same tick is an acceptable magnitude (compare v0.0.1's Puffed Wheat wet state, 5×5×5 = 125 per throw).

**Why a power-0 explosion**: D-58's original text is "the landing point triggers a power-0 explosion, and that explosion serves only as the landing-point trigger". At power 0 the vanilla damage formula degenerates (radius 0, no entity enters), and `damageCap = 0` skips our damage section outright — **two** no-ops; at the same time the `ExplosionEvent.Start` hook is preserved, so land-claim / protection mods still get a chance to veto it (consistent with v0.0.1's `FarmstormExplosion#detonate`).

**Why not reuse `BlastRule.forCrop`**: `forCrop(MOLOTOV, …)` would resolve to `NEVER_BREAK` (`molotov` carries no `DEMOLITION` tag); the result is the same but it takes one more indirection, whereas writing `NEVER_BREAK` directly states "this explosion must not break blocks at all" more clearly. **Where D-19's iron rule lands**: what `scatter` places is fire and magma blocks, which is **addition**, not destruction — not one original block is lost, so it does not violate "the right to destroy is decided by `DEMOLITION` alone".

**Which config keys it reads**: `molotov.triggerExplosionPower` (0, 0–100), `molotov.effectRadius` (4.0 blocks, 0.0–16.0), `molotov.fireChance` (85.0 %, 0.0–100.0).
**Which registry objects it writes**: `farmstorm:molotov` (entity_types ×1, registered in `FarmstormEntityTypes` with the existing `projectile(...)` factory).

### 3.8 `CompressedPuffedWheatProjectile` (projectile #10)

**Responsibility**: **exactly the same** landing semantics as Puffed Wheat (water-level test, dry/wet edge length, write-style replacement, 3.6e6 destruction floor); the only difference is that the block written changes from `minecraft:hay_block` to `farmstorm:compressed_hay_block` (D-77).

```java
public class CompressedPuffedWheatProjectile extends PuffedWheatProjectile {
    @Override protected CropProfile profile() { return CropProfile.COMPRESSED_PUFFED_WHEAT; }

    /** D-77: the only difference — the block the parent writes changes from minecraft:hay_block to the compressed hay block. */
    @Override protected BlockState fillBlock() {
        return FarmstormBlocks.COMPRESSED_HAY_BLOCK.get().defaultBlockState();
    }
}
```

**Four details that must be carried over** (aligned one by one with `entity/thrown/PuffedWheatProjectile.java`; "roughly the same" is not accepted):

| Detail | Value / algorithm | Source basis |
|---|---|---|
| Landing back-solving step | `LANDING_PROBE = 0.1`, probe 0.1 blocks inward from the hit point along the flight direction | class comment: the base class pushes the hit point 0.05 blocks along the outward normal, so the hit point lies outside the hit block |
| Water source test | the fluid is `#minecraft:water` and `isSource()`, tested once inside the **dry** cube only | "the water source is tested once inside the dry cube; if it hits, the wet edge length is used and no second test is made" |
| Edge length | dry `puffed_wheat.dryEdge` (3, 1–15), wet `puffed_wheat.wetEdge` (5, 1–15) | the source reads these two keys |
| Destruction floor | `getBlock().getExplosionResistance() >= BlastRule.UNBREAKABLE_RESISTANCE` (3.6e6) → skip that cell | D-38 / T-05 |

**Which config keys it reads**: `puffed_wheat.dryEdge`, `puffed_wheat.wetEdge` (**no new keys**; "fully carried over" in D-77 here literally means "reads the same set of keys").
**Which registry objects it writes**: `farmstorm:compressed_puffed_wheat` (entity_types ×1).

**Inheritance rather than copy**: `entity/thrown/PuffedWheatProjectile.java` **is inside the editable-file whitelist of BRIEF §3.3** (the v0.0.2 ruling added it together with `DeployedBehaviors.java` / `FarmstormBlockEntities.java`). So the approach is to extract an extension point in the parent and have the subclass override only that:

| Change point | Parent `PuffedWheatProjectile` | Subclass `CompressedPuffedWheatProjectile` |
|---|---|---|
| The block written | add `protected BlockState fillBlock()`, whose body returns `Blocks.HAY_BLOCK.defaultBlockState()` | override to `FarmstormBlocks.COMPRESSED_HAY_BLOCK.get().defaultBlockState()` |
| The write loop | in `fillHay(...)`, replace the hard-coded `BlockState hay = Blocks.HAY_BLOCK.defaultBlockState()` with `BlockState hay = this.fillBlock()` (turn the method from `private static` into an instance method) | do not rewrite the loop |
| Landing back-solving `landingBlock` | untouched | not overridden |
| Water source test `hasWaterSource` | untouched | not overridden |
| `onImpact` | untouched | not overridden |

Four lines change in the parent (1 new method + 1 line changed to use it + the signature moving from `private static` to instance). That makes D-77's "fully carried over" **literally true**: the subclass still runs the same landing back-solving, water source test and write loop, the only variable in the code path is "the block", and there is no second implementation that could drift on its own. F-68 of 13-TASKS adopts exactly this approach, so the two documents agree.

### 3.9 `compressed_hay_block`: no new block class

**Responsibility**: a pure-property block — same texture on all six faces, not rotatable, no `BlockEntity`, no behavior. **Conclusion: no new class**, use vanilla `net.minecraft.world.level.block.Block`.

```java
// registry/FarmstormBlocks.java
public static final DeferredBlock<Block> COMPRESSED_HAY_BLOCK = REGISTER.registerBlock("compressed_hay_block",
        Block::new,
        BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(30.0F, 6.0F)                 // D-57: hardness 30.0F, resistance 6.0F
                .requiresCorrectToolForDrops()
                .sound(SoundType.GRASS));              // same sound as the hay block
```

| Shape requirement of D-57 | How it is met |
|---|---|
| `strength(30.0F, 6.0F)` | one method, two arguments, controlling hardness and resistance at once (`ANCIENT_DEBRIS` is `strength(30.0F, 1200.0F)`, `HAY_BLOCK` is `strength(0.5F)`) |
| `requiresCorrectToolForDrops()` | without it the block does not drop itself; paired with the `minecraft:mineable/pickaxe` + `minecraft:needs_diamond_tool` tags |
| Six faces, not rotatable | `RotatedPillarBlock` is not introduced; the blockstate uses `cube_all` with exactly 1 variant |
| Texture = a grayscale version of the hay block side | `assets/farmstorm/textures/block/compressed_hay_block.png` (§6.5) |

**The consequence of taking 6.0 resistance must be written into the document and the tooltip** (D-57 explicitly requires it):

| Explosion source | Threshold | Against this block | Reason |
|---|---|---|---|
| Melon Grenade `MelonGrenadeProjectile` | `explosion.grenadeResistThreshold` = 6.0 | **cannot break it** | the test is `resistance < maxResistance`, and 6.0 is not less than 6.0 |
| Melon Slice Bomb `MelonSliceProjectile` | `explosion.sliceResistThreshold` = 6.0 | **cannot break it** | same as above |
| Ubi Server `UbiServerProjectile` | `UNBREAKABLE_SKIP`, 3.6e6 | **can break it** | 6.0 < 3.6e6 |
| Toxic Ubi Server `ToxicUbiServerProjectile` | `VANILLA`, 3.6e6 | **can break it** | same as above |
| Vanilla explosion (TNT included) | vanilla `Explosion` rules | **can break it** | vanilla `Explosion` accumulates `f -= (resistance + 0.3) * 0.3` per cell and breaks while `f > 0`: at TNT power 4, `f` starts in `[2.8, 5.2]`, and after subtracting the 1.89 that 6.0 resistance corresponds to it is still usually positive, so 6.0 resistance cannot stop a cobblestone-tier block |

**Which config keys it reads**: **no key at all**. D-57 nails down three numbers (30.0F / 6.0F / `requiresCorrectToolForDrops`), and these three are inherent block properties, not tunable gameplay values; turning them into config would violate the spirit of "the default values of existing config keys must not change" (adding one tunable key is a back door for balancing) and would also be inconsistent with v0.0.1, which hard-codes `CONVERTER`'s `strength(3.5F)` at the same place.
**Which registry objects it writes**: `farmstorm:compressed_hay_block` (blocks ×1) + the same-named BlockItem (items ×1, via `REGISTER.registerItem("compressed_hay_block", …)` in `FarmstormItems`).

### 3.10 `ZangiefCaneTrap` (a `TrapBehavior` implementation)

**Responsibility**: the **trigger test** and every action **at the instant of trigger** of Zangief Cane. Phase advancement is not here (§3.11), because phases must follow the player tick while the trap block is gone the moment it triggers (D-72).

```java
public final class ZangiefCaneTrap implements TrapBehavior {
    @Override public DeployedKind kind() { return DeployedKind.ZANGIEF_CANE; }

    @Override public Optional<Player> findTrigger(DeployedCropBlockEntity be) { … }
    @Override public void trigger(DeployedCropBlockEntity be, Player target) { … }
}
```

**Key methods**:

| Method | Rule | Basis |
|---|---|---|
| `scanInterval()` | uses the `TrapBehavior` default of 10 ticks | `DeployedKind.ZANGIEF_CANE(10)` |
| `findTrigger(be)` | iterate `level.players()`; skip `isSpectator()` / `isCreative()` / `be.isAlly(player)`; the test is **spherical Euclidean distance** `pos.distSqr(player.position()) <= r*r` with `r = zangief_cane.triggerRadius` (1.5 blocks) | D-71 (non-ally players only, creative and spectators exempt), D-73 (spherical, not Chebyshev) |
| `trigger(be, target)` | ① apply `zangief_piledriver` amplifier 0 to the target; ② `level.playSound(null, pos, FarmstormSounds.ZANGIEF_PILEDRIVER.get(), SoundSource.PLAYERS, 1.0F, 1.0F)`; ③ record `(playerId → trigger tick)`; ④ `be.consume()` (silent removal + destroy particles) | D-72 (vanishes silently, drops nothing), D-66 (play the trigger sound) |

**Why the distance test uses `player.position()` and not `player.blockPosition()`**: `BLAST_CARROT` uses `blockPosition()` because it is a **Chebyshev** test (comparing integers between cells); `FANG_TRAP_CACTUS` uses Euclidean distance but compares the block's `Vec3` center with the entity's exact position (source `FangTrapCactusTrap`). Zangief Cane belongs to the latter family: `pos.getCenter().distanceTo(player.position()) <= 1.5`. **Added by this document**: 1.5 blocks is measured "from the block center", not "from the block boundary", so a player standing right on top of the trap (center distance about 0.5) still counts as hit, and a player standing at the center of an adjacent block (distance 1.0 or 1.41) also counts — which is exactly the effect this number 1.5 is meant to produce.

**The order in which the trap disappears after triggering**: apply the effect and the sound first, then `consume()`. `consume()` internally does `level.removeBlock(pos, false)` + `levelEvent(PARTICLES_DESTROY_BLOCK)`, and once the block is gone the `DeployedCropBlockEntity` is removed right after; doing it the other way round invalidates `be.getBlockPos()` and `be.getLevel()` first (the `BlastCarrotTrap` comment in v0.0.1 records the same lesson: the block must be detached before detonating, otherwise the sampling ray is blocked by itself).

**Which config keys it reads**: `zangief_cane.triggerRadius` (1.5 blocks, 0.0–8.0), `zangief_cane.stageDelayTicks` (14 ticks, 0–200), `zangief_cane.levitationLevel` (level 7, 1–255), `zangief_cane.levitationDurationTicks` (20 ticks, 0–72000), `zangief_cane.fallTicks` (40 ticks = 2 seconds, 1–200), `zangief_cane.spinDegreesPerTick` (72.0 degrees/tick, 0.0–360.0).
**Which registry objects it writes**: none (behavior objects are static singletons and are not registered).


### 3.11 `ZangiefPiledriverController` (server-side three-phase state machine)

**Responsibility**: every piece of **timing** after the trigger belongs here: grant levitation after 14 ticks, apply downward velocity when levitation ends, strip the effect on landing or on timeout. It hangs on `PlayerTickEvent.Post` and runs on the server only.

```java
public final class ZangiefPiledriverController {
    /** Safety valve: the longest duration of phase 2 in ticks, preventing the effect from never ending while the player is stuck in mid-air. Not in config (HC-13 family). */
    public static final int MAX_FALL_TICKS = 100;
    /** Memory only: UUID → phase state. Cleared on server restart, never written to a save (precedent D-18). */
    private static final Map<UUID, PhaseState> ACTIVE = new HashMap<>();
    public static void onPlayerTick(ServerPlayer player) { … }
}
```

**Phase table** (the single truth of amplifier and time):

| Phase | amplifier | Entry moment | Duration | Server action | Semantics read by the client |
|---|---|---|---|---|---|
| 0 flipped, waiting to levitate | 0 | the instant of `trigger` (t=0) | `stageDelayTicks` = 14 ticks | apply the effect (`visible=false, showIcon=false`), play the trigger sound | grayscale off / flip on / input lock on / spin off |
| 1 levitating | 1 | t = 14 | `levitationDurationTicks` = 20 ticks | hard-switch the effect; at the same time `addEffect(LEVITATION, 20, 6)` (amplifier 6 = level VII) | grayscale off / flip on / input lock on / spin off |
| 2 falling and spinning | 2 | t = 34 | until landing, target `zangief_cane.fallTicks` = 40 ticks, safety cap `MAX_FALL_TICKS` = 100 ticks | hard-switch the effect; from then on **servo the downward velocity every tick** = remaining height ÷ remaining ticks (§5.2) | grayscale off / flip on / input lock on / spin on |
| (end) | — | landing or timeout | — | `removeEffect` + remove from `ACTIVE` | everything reset |

**The definition of "hard switch"** (not optional): when advancing a phase, first `player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER)`, then on the next line `addEffect(new MobEffectInstance(…, amplifier = new phase, …))`. The reason is that vanilla `MobEffectInstance#update` has its own merge rules (comparing amplifier and duration), and simply stacking an instance with "the same id, a different amplifier" does not guarantee a replacement.
**Pending verification (attached to RT-15)**: the exact comparison order of 1.21.1's `MobEffectInstance#update`; this design **does not rely** on its behavior and sidesteps it with "remove first, then add". The risk is that a hard switch costs one extra `ClientboundRemoveMobEffectPacket` plus one `ClientboundUpdateMobEffectPacket`, so the client has a 1-tick window in which "the effect is absent" — the client self-check of §5.6 must therefore not reset immediately when the effect is absent, but wait 2 ticks.

**Key methods**:

| Method | What it does | Key point |
|---|---|---|
| `begin(ServerPlayer, long tick)` | lands phase 0 | called by `ZangiefCaneTrap#trigger` |
| `onPlayerTick(ServerPlayer)` | reads `ACTIVE`, advances by phase; landing tested by `player.onGround()`; timeout tested by `now - enteredAt >= MAX_FALL_TICKS` | `ServerLevel` side only; once per tick, constant cost |
| `setPhase(ServerPlayer, int phase, int durationTicks)` | hard switch + update `ACTIVE` | the only place that changes the amplifier |
| `finish(ServerPlayer)` | `removeEffect` + drop the record | idempotent |
| `isActive(UUID)` | for `WildHunterCombat` and the like to test on (no dash attraction during a phase) | read only |

**Which config keys it reads**: `zangief_cane.stageDelayTicks`, `zangief_cane.levitationLevel`, `zangief_cane.levitationDurationTicks`, `zangief_cane.fallTicks`.
**Which registry objects it writes**: none.

**How the landing stays inside 1.7–2 seconds** (`plan0.0.2.txt` line 29, verbatim: "确保其可以在 1.7-2 秒内落地"): **no constant speed, use a per-tick servo** (ruling A-03). Every tick of phase 2 measures the remaining height `h` below the player and the remaining ticks `n = fallTicks - elapsed`, then sets the downward velocity to `h / n`:

```java
double h = player.getY() - groundYBelow(player);          // remaining fall height (blocks)
int n = Math.max(1, fallTicks - elapsedInPhase2);         // remaining ticks
player.setDeltaMovement(0.0, -Math.min(h / n, MAX_SERVO_SPEED), 0.0);
```

Why a constant speed does not work: the plan's constraint is **time** (1.7–2 seconds), whereas under a constant speed the landing time is decided by the starting height, and the height to which the player is lifted by levitation differs from round to round; once `v` is hard-coded it must either overshoot or land early at some heights. The servo pins the time with "remaining height ÷ remaining ticks", and height is absorbed automatically.

| Quantity | Value | Note |
|---|---|---|
| `fallTicks` | `zangief_cane.fallTicks`, default **40 ticks = 2.0 seconds**, range 1–200 | the plan's "1.7–2 seconds" takes the upper bound 2.0 seconds as the default; set 34 for 1.7 seconds |
| Servo speed ceiling `MAX_SERVO_SPEED` | `3.0` blocks/tick (**added by this document**, safety valve) | prevents an absurd teleport speed when the remaining ticks are 1 and the remaining height is 20 blocks; after hitting the ceiling it finishes as free fall and logs one line `桑吉尔蔗 伺服触顶 h={} n={}` |
| Safety cap `MAX_FALL_TICKS` | `100` ticks | the servo alone lands the player within `fallTicks`; this cap only backstops "the landing test failed" (stuck in a block seam) |

How to verify during implementation: the log line `桑吉尔蔗 落地耗时 {} 刻（目标 {fallTicks}）`, where the deviation of the measured value from `fallTicks` must be ≤ 2 ticks; run it 5 times in a row (different heights: flat ground, a two-block platform, a four-block platform) and all runs must land near the same duration — which is exactly what a constant-speed approach cannot do.

### 3.12 `WildHunterCombat` (server-side decisions: target re-check / damage / knockback / kill extension)

**Responsibility**: **every computation in Wild Hunter that needs authority**. The client-side prediction (`WildHunterDashClient` in §3.13) is feel only; this is what settles it.

**Key methods and rules**:

| Method | Rule | Basis |
|---|---|---|
| `selectTarget(ServerPlayer)` | candidates = `LivingEntity` within `wild_hunter.searchRadius` (5.0 blocks) of the player as sphere center, with an angle of at most `wild_hunter.searchAngleDegrees` (30.0°) to the view direction; exclude self, same-team players (`player.isAlliedTo(other)`), spectators | D-64 |
| Ordering | smallest angle first; on equal angles the nearer one (`Comparator.comparingDouble(angle).thenComparingDouble(distSqr)`) | D-64 |
| `onSwing(ServerPlayer)` | hooking `PlayerInteractEvent.LeftClickEmpty` is not enough (that covers empty swings only); the real entry is the server-side handling path of `ServerboundSwingPacket` ⇒ re-check the target at the **server-side settlement point** of `PlayerEvent` / the vanilla attack flow; on a hit, settle damage with the player's own attack power | D-65 + BRIEF §7.6 |
| Knockback | on a successful hit, call `target.knockback(0.4, dx, dz)` with the vanilla default strength; no custom value | D-75 |
| `onKill(ServerPlayer)` | in `LivingDeathEvent`, test that the killer is a player carrying the effect ⇒ remaining duration + `killExtensionTicks` (160 ticks = 8 seconds), restore `killHealPoints` (10 health points); **no cap on the number of kills** | D-74 |
| Mutual exclusion with phases | no attraction while `ZangiefPiledriverController.isActive(uuid)` is true | added by this document: all input is locked during a piledriver, so attracting on top of that would be self-contradictory |

**How the "attack damage" item lands**: D-75 only says knockback uses the vanilla default strength and gives no extra damage number. The rule here: **the attraction itself deals no damage**; damage comes from the player's own ordinary attack (through vanilla `Player#attack`), so no key such as `wild_hunter.attackDamage` is needed. The `attackDamageBonusPercent` attribute (+40 %) is provided by the `MobEffect` of §3.5 and is a bonus of the effect itself, unrelated to the attraction.

**Key point: the server computes independently and does not listen to the client**. BRIEF §7.6's original text is "the client, when 'the attack key is pressed locally + the player has the effect + a legal target is computed locally', immediately gives velocity toward the target, and the server, on receiving `ServerboundSwingPacket`, independently runs the same deterministic algorithm to compute the target and settle damage and knockback". "The same" means: there is **only one copy** of the `selectTarget` code, and both client and server call it, except that the client copy runs on `ClientLevel` (it must be generic over `Level` or take a `LivingEntity` list; `ServerLevel` must not be hard-coded).

**Which config keys it reads**: `wild_hunter.searchRadius`, `wild_hunter.searchAngleDegrees`, `wild_hunter.killExtensionTicks`, `wild_hunter.killHealPoints`, `wild_hunter.dashTicks`.
**Which registry objects it writes**: none.

### 3.13 The 6 client classes

Client code lives only under the `client/` package and **must** enter through the single boundary `FarmstormClient` (`@EventBusSubscriber(modid, value = Dist.CLIENT)`) — a dedicated server never loads this class, so it cannot crash from a missing client class (class comment of `client/FarmstormClient.java`).

| Class | Responsibility | Key methods / mount point | Which config keys it reads | Which registry objects it writes |
|---|---|---|---|---|
| `GrayscalePass` | full-screen grayscale (including GUI) | `RegisterShadersEvent` registers the `ShaderInstance`; `RenderGuiEvent.Post` runs the pass | none | none |
| `WildHunterOutline` | red silhouette | `RenderLivingEvent.Post` → `getModel().renderToBuffer(...)` | none | none |
| `InputLockHandler` | keyboard and mouse lock (ESC only) | 5 events, see §4.3 | none | none |
| `ZangiefCameraController` | render-layer roll / pitch / yaw | `ViewportEvent.ComputeCameraAngles` | `zangief_cane.spinDegreesPerTick` (used only for the spin accumulator; **the client cannot read the server config, see the degradation rule in §4.4**) | none |
| `WildHunterLoopSound` | looping sound that follows the player | a hand-written `SoundInstance` with `isLooping() = true` | none | none |
| `WildHunterDashClient` | dash prediction | gives velocity locally when the attack key is pressed | a **mirror of the default values** of the same 5 keys as `WildHunterCombat` (§4.5) | none |

The source of state for all six classes is **the effect on the player itself**, not a message pushed by the server: `GrayscalePass` / `WildHunterOutline` / `WildHunterLoopSound` read whether `wild_hunter` is present; `InputLockHandler` / `ZangiefCameraController` read the amplifier of `zangief_piledriver`. This constraint is the entire reason §9 holds.

### 3.14 Change points in existing files (file by file)

```diff
  change only these, touch nothing else (BRIEF §3.3 + the three whitelist additions of this section)
```

| File | Change | Basis |
|---|---|---|
| `util/CropProfile.java` | +4 constants; `ALL` becomes 19 entries; `LUCKY_BERRY_POOL` becomes three exclusions | §3.1 / D-55 / D-56 |
| `behavior/DeployedKind.java` | +1 enum value `ZANGIEF_CANE(10)` | §3.2 |
| `registry/FarmstormItems.java` | +5 `DeferredItem`s; append `compressed_hay_block` in `creativeTabItems()` (the 4 crops enter automatically through `CROP_ITEMS`) | §2.2 |
| `registry/FarmstormBlocks.java` | +2 blocks: `deployed(ZANGIEF_CANE, false, false, false)` and `compressed_hay_block` | §3.9 / D-68 |
| `registry/FarmstormEntityTypes.java` | +2 `projectile(...)` calls | §2.2 |
| `registry/FarmstormCreativeTabs.java` | **unchanged** (the content comes from `FarmstormItems.creativeTabItems()`) | BRIEF §3.2 "creative tabs +0" |
| `Farmstorm.java` | register `FarmstormMobEffects.REGISTER` and `FarmstormSounds.REGISTER`; change the self-check text to 19 / 10 / 6 / 3 / 4; add 2 self-check lines | §0.4 |
| `client/FarmstormClient.java` | 2 new `ThrownItemRenderer`s; `ALL_ENTITY_TYPES` grows from 9 to 11 entries | §2.2 (the renderer self-check of `FIX-1`) |
| `config/FarmstormConfig.java` | +4 groups with 22 keys in total (`molotov` / `wild_apple` / `wild_hunter` / `zangief_cane`) + the baked snapshot | §8 |
| `datagen/ModRecipeProvider.java` | +5 recipes: 1 shaped crafting, 1 shapeless crafting, 3 converting | §7.2 |
| `datagen/ModItemModelProvider.java` | 4 `basicItem` + 1 `withExistingParent(compressed_hay_block → block/compressed_hay_block)` | §7.2 |
| `datagen/ModBlockStateProvider.java` | add `"zangief_cane"` to `DEPLOYED_INVISIBLE`; add the `cubeAll` compressed hay block | D-68 |
| `datagen/ModLootTableProvider.java` | +2 block loot tables (deployed-block conventions + ordinary-block conventions) | §7.2 |
| `datagen/ModLangProvider.java` | +39 entries (see §7.3) | §7.3 |
| `datagen/ModAdvancementProvider.java` | +3 advancements; append `first_molotov` / `zangief_hit` / `first_wild_hunter` to `AWARD_REQUIRED` | D-63 |
| `datagen/ModBlockTagsProvider.java` | append `zangief_cane` to `deployed_crops`; append `compressed_hay_block` to `mineable/pickaxe`; create `needs_diamond_tool` | D-57 |
| `datagen/ModItemTagsProvider.java` | append 4 entries to `modified_crops` (the three new crops + compressed puffed wheat); create `converter_fuel` (the items of the 19 profiles + vanilla crops) | D-52 / D-55 / D-56 |
| `util/FarmstormTags.java` | +1 `TagKey<Item> CONVERTER_FUEL` | D-52 |
| `gradle.properties` | `mod_version=0.0.2` | §1.1 |

**The three compile-enforced places (must change, or the build fails)**

The editable-file list of BRIEF §3.3 already includes the three files below (`DeployedBehaviors.java` / `FarmstormBlockEntities.java` / `PuffedWheatProjectile.java`), so no file outside the whitelist remains beyond the table above. The changes these three make to `DeployedKind` / `PuffedWheatProjectile` are **compile-enforced** and cannot be avoided:

| File | Why it must change | Minimal change | Risk |
|---|---|---|---|
| `behavior/DeployedBehaviors.java` | `of(DeployedKind)` is an **exhaustive `switch` expression** over the enum with no `default`; adding one enum value ⇒ the compiler reports "the switch expression does not cover all possible input values" | +1 static singleton field + 1 `case` | none |
| `registry/FarmstormBlockEntities.java` | same as above: `deployedOf(DeployedKind)` is also an exhaustive `switch` | +1 `case ZANGIEF_CANE -> FANG_TRAP_CACTUS.get();` + add `FarmstormBlocks.ZANGIEF_CANE.get()` to the block list of the `BlockEntityType.Builder.of(…)` of `FANG_TRAP_CACTUS` | the `block_entities` count stays 6 (one type takes two blocks); **pending verification (attached to RT-04)**: whether `BlockEntityType.Builder#of` accepts `Block...` varargs |
| `entity/thrown/PuffedWheatProjectile.java` | see §3.8: extract the written block into an extension point, which is the only way D-77's "fully carried over" is one code path instead of two copies | +1 `protected BlockState fillBlock()` + 1 line changed to use it (`fillHay` turns from `private static` into an instance method) | none |

None of the three changes produces a new registry object, a new resource file or a new config key: the 77 of §2, the 36 of §7 and the 22 of §8 all stay the same.

---

## 4. Technical Plan for the Three Client Subsystems

This section expands BRIEF §7.1–§7.3 into an implementable plan. **Not a single word of the preferred options is changed** (precondition ① of BRIEF §7), and no risk ID is renumbered (precondition ②). The three pending items keep their marks per the rules of §0.5, with "how to verify" appended.

The shared premise of all three (not written in BRIEF, derived in §0.6): **the source of state is always the effect on the player itself**. The client does not ask the server, does not cache server messages and adds no packet.

### 4.1 Grayscale (D-61, full screen including GUI) → RT-10

**Trigger condition**: `farmstorm:wild_hunter` is present on the local player (§0.6). The "present / absent" of the effect is decided by the `MobEffectInstance` delivered by vanilla `ClientboundUpdateMobEffectPacket`, read once per tick by the client.

**Preferred option (not to be swapped)**:

1. In `RegisterShadersEvent` (mod bus), register a custom `ShaderInstance` whose program lives in `assets/farmstorm/shaders/core/grayscale.json + grayscale.vsh + grayscale.fsh`.
2. In `RenderGuiEvent.Post`, run one full-screen pass against `Minecraft.getInstance().getMainRenderTarget()`.

Why `RenderGuiEvent.Post` (BRIEF §7.1, verbatim): it fires after the GUI and before the frame goes to the screen, so one pass covers both the world and the GUI at once, satisfying D-61's "full-screen grayscale, not just a faded world".

```java
// client/render/GrayscalePass.java (skeleton; signatures per 1.21.1)
private static ShaderInstance shader;                       // assigned in RegisterShadersEvent

@SubscribeEvent
public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
    event.registerShader(new ShaderInstance(event.getResourceProvider(),
            Farmstorm.id("grayscale"), DefaultVertexFormat.POSITION_TEX), s -> shader = s);
}

@SubscribeEvent
public static void onRenderGuiPost(RenderGuiEvent.Post event) {
    if (!WildHunterClientState.active() || shader == null) return;   // outside the effect: zero cost, not even the target is touched
    Minecraft mc = Minecraft.getInstance();
    RenderTarget main = mc.getMainRenderTarget();
    main.bindWrite(true);                                            // switch to the main target
    RenderSystem.setShader(() -> shader);
    RenderSystem.setShaderTexture(0, main.getColorTextureId());      // sample the previous frame's own result
    blitFullScreenQuad();                                            // draw one screen-filling quad
    mc.getMainRenderTarget().bindWrite(true);                        // restore
}
```

**Why the pass must return on the very first line when `active()` is false**: grayscale is an "abnormal state", not a permanent post-process. In the normal state it must cost nothing; otherwise every frame of every player runs a wasted full-screen pass, which is a server-wide frame-rate tax. `active()` is a client snapshot (see below), not a live effect query.

**What `WildHunterClientState` is**: `client/WildHunterClientState.java`, the client's state cache class. It has exactly one job — compute the few `wild_hunter` quantities that are "shared across subsystems and unreadable from the server config" once, so that the three hooks of §4.1 / §4.2 / §4.6 can read them directly:

| Method | Returns | Who uses it |
|---|---|---|
| `active()` | `boolean`: whether the local player carries `wild_hunter` at this moment | the first-line switch of grayscale in §4.1 and of the silhouette in §4.2; computed once in `ClientTickEvent.Post`, the render thread only reads the snapshot |
| `highlightRadius()` | `double`: maximum distance of the red silhouette (blocks), default `64.0` | the distance filter of §4.2; the value comes from the mirror constant `DEFAULT_HIGHLIGHT_RADIUS` of §8.4 |
| `loopRange()` | `double`: audible range of the looping sound (blocks), default `16.0` | the attenuation distance of §4.6; the value comes from the mirror constant `DEFAULT_LOOP_SOUND_RANGE` of §8.4 |

Why mirror instead of reading the config: `FarmstormConfig` is `ModConfig.Type.SERVER`, readable by the client in single-player but not on a dedicated server. So `wild_hunter.highlightRadius` (§8.1 #15) and `wild_hunter.loopSoundRange` (§8.1 #16) are always returned by this class as **mirror constants** on the client, the same rule as the degradation convention of §4.4: changing a config default requires changing the mirror in step, otherwise the client lags (registered as a known deviation, not fixed — fixing it needs a new packet, violating D-50).

**Resources and the two mounting forms**:

| File | Form | Who uses it | Note |
|---|---|---|---|
| `shaders/core/grayscale.json` | program declaration of the `ShaderInstance` | **preferred** (this section) | the json named by BRIEF §7.1 |
| `shaders/core/grayscale.vsh` / `grayscale.fsh` | GLSL source | shared by both forms | BRIEF §3.2's "2 GLSL" are exactly these two files |
| `post_effect/grayscale.json` | `PostChain` declaration | **the RT-10 degradation branch** | the +1 registered by BRIEF §3.2; uses the same pair of GLSL files, only the mounting differs |

**Pending verification (BRIEF §7.1, verbatim)**: whether switching the render target during `RenderGuiEvent.Post` conflicts with other mods' GUI post-processing.

- **How to verify**: install a mod that does GUI post-processing (or temporarily run your own pass a second time inside `RenderGuiEvent.Post`) → enter the effect → watch for "screen fully black / only half the screen gray / another mod's HUD disappears". At the same time look for GL errors such as `GL_INVALID_OPERATION` in the log (unrelated to `-Dmixin.debug`; read the `[Render thread/ERROR]` lines of `latest.log` directly).
- **Verdict**: any of the above symptoms means "they fight"; take the degradation branch below.
- **Degradation branch (given by BRIEF §7.1)**: run one world grayscale pass in the AFTER stage of `RenderLevelStageEvent` (after the world stage, before the GUI; the exact constant name is **pending verification, attached to RT-10**) plus one pass over the GUI region only in `RenderGuiEvent.Post`. The GLSL of the two passes is identical; only the mount point and the viewport change. If the world stage step does not hold either, fade the world only and register "the GUI is not grayed" as a degradation in the Release notes.
- **RT attribution**: this pending item is attached to **RT-10** (the mount point of the full-screen grayscale post-process does not hold).
- **There is one more API pending verification (attached to RT-01)**: whether the signature `RegisterShadersEvent#registerShader(ShaderInstance, Consumer<ShaderInstance>)` and the three-argument constructor `ShaderInstance(ResourceManager, ResourceLocation, VertexFormat)` still hold on 1.21.1 (1.21 changed the shader loading path, and `shaders/core/*.json` and `post_effect/*.json` now coexist). If the three-argument constructor does not hold, use a utility method of `Shaders` or switch to `PostChain` (§7.2 #15 in §6 already lists that resource, so no file has to be added when degrading).

**Forbidden (BRIEF §7.1, verbatim)**: counterfeiting grayscale with fog color or texture replacement. D-61 wants true grayscale — "fog color" only fades distance and "texture replacement" only changes block faces; neither can change the GUI, and neither can change entities or particles.

### 4.2 Red silhouette (D-62, only what is visible, ≤64 blocks) → RT-11

**Trigger condition**: same as §4.1 (`wild_hunter` present).

**Preferred option (not to be swapped)**:

1. Subscribe to `RenderLivingEvent.Post`.
2. Take `LivingEntityRenderer#getModel()`.
3. Draw it a second time with `Model#renderToBuffer` using `RenderType.entityTranslucentEmissive(whiteTexture)`.
4. With `RenderSystem.setShaderColor(1.0F, 0.15F, 0.15F, 0.45F)`.
5. The white 1×1 texture lives at `assets/farmstorm/textures/misc/white.png`.

```java
@SubscribeEvent
public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
    if (!WildHunterClientState.active()) return;
    LivingEntity entity = event.getEntity();
    LocalPlayer self = Minecraft.getInstance().player;
    if (entity == self || entity.isSpectator() || self.isAlliedTo(entity)) return;
    if (self.distanceToSqr(entity) > sq(WildHunterClientState.highlightRadius())) return;  // D-62: highlightRadius, default 64.0 blocks
    if (!self.hasLineOfSight(entity)) return;                        // D-62: no block occlusion
    var model = event.getRenderer().getModel();
    var buffer = event.getMultiBufferSource().getBuffer(
            RenderType.entityTranslucentEmissive(WHITE_TEXTURE));
    RenderSystem.setShaderColor(1.0F, 0.15F, 0.15F, 0.45F);
    model.renderToBuffer(event.getPoseStack(), buffer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, -1);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);            // must be reset, or later entities are tinted red
}
```

**Filter rules one by one** (D-62 + the exclusions of D-64):

| Filter | Criterion | Basis |
|---|---|---|
| Exclude self | `entity == Minecraft.getInstance().player` | D-62 |
| Exclude same-team players | `self.isAlliedTo(entity)` (the client only has scoreboard teams and friend info, which is enough) | D-62 |
| Exclude spectators | `entity.isSpectator()` | the same exemption as D-64 |
| Distance ≤ `wild_hunter.highlightRadius` (default 64.0 blocks, range 0.0–256.0) | `distanceToSqr <= r * r` (compare squares, no square root); `r` comes from the client mirror constant of §4.4 | D-62 |
| Line of sight visible | `self.hasLineOfSight(entity)`; **no drawing through walls** | D-62 |

**Forbidden (BRIEF §7.2, verbatim)**:

- Do not call `EntityRenderDispatcher#render` inside `RenderLivingEvent` to redraw the whole entity — that recursively fires the event itself and overflows the stack outright.
- Do not substitute the vanilla glow effect (`Glowing`): that draws a **white** outline and is **visible to the whole server**, contradicting all three of D-62's conditions ("red, only for me, only what is visible").

**Pending verification (attached to RT-11)**:

| Pending item | How to verify | Verdict |
|---|---|---|
| availability and cost of `hasLineOfSight` on a client-side entity | stand once behind a wall and once in front of it, and check that silhouettes are drawn only for creatures in front; also run `/debug profile` to read tick and frame times | a silhouette behind the wall means unusable, and it must fall back to "distance + coarse line-of-sight sampling" |
| whether the alpha of `setShaderColor` (0.45) takes effect on `entityTranslucentEmissive` | set the alpha to 0.0 once and to 1.0 once and compare | no change at either extreme means the alpha does not take effect; use the `RenderType`'s own translucency or a vertex color instead |
| frame time with many entities | 30 creatures on screen with the effect on | a frame-time increase above 3 ms counts as unacceptable and requires a "at most 32 per frame" circuit breaker |

**Performance guardrail (added by this document)**: draw at most **32** entities per frame (nearest first); the rest are not drawn. Reason: `renderToBuffer` redraws the whole model, it is not a cheap outline pass, and a large brawl inside the default 64-block radius can hold hundreds of creatures.

### 4.3 Input lock (D-60, only ESC survives) → RT-13

**Trigger condition**: `farmstorm:zangief_piledriver` is present on the local player, **and the amplifier is any of 0 / 1 / 2** (all three phases are inside the lock window; D-60's range is "from the trigger to just before landing").

**Five events, one segment each — omitting one leaks** (the original table of BRIEF §7.3, kept word for word):

| What to lock | Which event | How exactly |
|---|---|---|
| Movement / jump / sneak | `MovementInputUpdateEvent` | zero out `forwardImpulse` / `leftImpulse` / `jumping` / `shiftKeyDown` |
| Attack / use | `InputEvent.InteractionKeyMappingTriggered` | `event.setCanceled(true)` |
| Hotbar 1–9 | `InputEvent.Key` | let `GLFW_KEY_ESCAPE` through, cancel the rest |
| Scroll-wheel hotbar switch | `InputEvent.MouseScrollingEvent` | cancel |
| Inventory / chat | `ScreenEvent.Opening` | let only the ESC path through (`PauseScreen`); for the rest `setNewScreen(null)` or cancel |

```java
// client/input/InputLockHandler.java (skeleton)
private static boolean locked() {
    return Minecraft.getInstance().player != null
            && Minecraft.getInstance().player.hasEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
}

@SubscribeEvent
public static void onMovementInput(MovementInputUpdateEvent event) {
    if (!locked()) return;
    Input input = event.getInput();
    input.forwardImpulse = 0.0F;
    input.leftImpulse = 0.0F;
    input.jumping = false;
    input.shiftKeyDown = false;
}
```

**Why the lock test is "is the effect present" and not "what is the amplifier"**: all three phases must be locked, so the single condition "present" is enough and there is one fewer number that can be written wrong. The phase number is read only where §4.1 / §4.4 need to distinguish it.

**The full meaning of "ESC only"** (D-60): ESC must stay usable, because "locking ESC dead makes the player think the game has frozen". Concretely in code: in `InputEvent.Key`, `keyCode == GLFW.GLFW_KEY_ESCAPE` returns without cancelling; in `ScreenEvent.Opening`, `event.getNewScreen() instanceof PauseScreen` is let through.

**Pending verification (BRIEF §7.3, verbatim)**: the relative order of `InputEvent.Key` and `KeyMapping` decides whether 1–9 can really be blocked.

- **How to verify**: after entering the effect, **test key by key** and record: W / A / S / D / space / Shift / left button / right button / the nine digit keys 1–9 / wheel up / wheel down / E / T / slash / T / ESC. Write one line "key → blocked or not" per press.
- **Verdict**: any key still taking effect (the character moves, the item is used, the hotbar switches, a GUI opens) means that event is hooked at the wrong level, and that path must be re-hooked to an earlier hook (for example `ScreenEvent.KeyPressed` before `InputEvent.Key`).
- **RT attribution**: attached to **RT-13** (input interception has a gap and some key still penetrates).

**Keys outside the lock scope (stated explicitly, so they are not mistaken for a missed test)**: the F1 / F2 / F3 function keys, F5 view, F11 fullscreen, and the operations inside the Esc menu. D-60's list has exactly 5 groups, and nothing outside these 5 groups is covered.

**Release condition**: released on the **same tick** `zangief_piledriver` disappears (`locked()` queries live, there is no cache). Writing "wait for the next tick to unlock" is not allowed, otherwise the player stays locked 1 extra tick after landing, which feels like "a hitch".

### 4.4 Camera (D-59 / D-70, no risk ID assigned by BRIEF §7.4)

**Trigger condition**: `zangief_piledriver` present (all three phases apply).

**Implementation**: `ViewportEvent.ComputeCameraAngles`:

```java
@SubscribeEvent
public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
    var player = Minecraft.getInstance().player;
    if (player == null || !player.hasEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER)) return;
    event.setRoll(180.0F);                              // D-59: roll 180° about the view axis
    event.setPitch(-event.getPitch());                  // D-59: negate pitch, both apply at once
    if (ZangiefClientState.spinning()) {                // only phase 2 spins (D-70)
        event.setYaw(event.getYaw() + ZangiefClientState.accumulatedDegrees());
    }
}
```

**Iron rule (BRIEF §7.4, verbatim)**: change only the **render-layer** angles and **never write back** to `player.getYRot()` / `player.getXRot()`. The player's real facing is unchanged and server authority is intact — writing back would make the client send the "flipped facing" to the server, tampering with the player's actual facing, which opens a hole for hitting through walls.

**How the spin accumulates**: the client accumulates per tick, `accumulated += spinDegreesPerTick` each tick (default 72.0 degrees = 1440 degrees/second), **relying on no per-tick server synchronization** (BRIEF §7.4, verbatim). It is zeroed when phase 2 starts and zeroed when phase 2 ends.

**Degradation rule for the client's inability to read the server config (added by this document)**: `FarmstormConfig` is `ModConfig.Type.SERVER`, readable by the client in single-player but not on a dedicated server. Therefore `ZangiefCameraController` and `WildHunterDashClient` of §3.13 always use **compile-time constants equal to the config defaults** (`DEFAULT_SPIN_DEGREES_PER_TICK = 72.0`, `DEFAULT_SEARCH_RADIUS = 5.0`, `DEFAULT_HIGHLIGHT_RADIUS = 64.0`, `DEFAULT_LOOP_SOUND_RANGE = 16.0`, etc.), and name it in the class comment: "this mirrors the config default; changing the config default requires changing this in step". Consequences and handling:

| Scenario | Symptom | Handling |
|---|---|---|
| The server uses the defaults | fully consistent | — |
| The server changed `spinDegreesPerTick` | the client spin speed does not match the config (purely visual, authority unaffected) | registered as a known deviation, not fixed (fixing it needs a new packet, violating D-50) |
| The server changed `searchRadius` / `searchAngleDegrees` | the client prediction may not fire → "no attraction this time", while damage and knockback are still settled by the server with the real config | same as above |

### 4.5 Wild Hunter dash (D-65) → RT-12

**Preferred option (not to be swapped)**: **client-side predicted displacement + server-side decision**.

In MC the player position is client-authoritative to begin with (the server only validates speed). The full flow:

| # | Where | What happens | Basis |
|---|---|---|---|
| 1 | client | attack key pressed locally + `wild_hunter` on the player + local `selectTarget` yields a legal target | BRIEF §7.6 |
| 2 | client | immediately give velocity toward the target: `setDeltaMovement(dir.normalize().scale(dist / dashTicks))`, crossing in `dashTicks = 3` ticks | D-65 |
| 3 | client | send `ServerboundSwingPacket` as usual (the vanilla attack flow already does, no extra packet needed) | §9 |
| 4 | server | on receiving the swing, run the **same** `selectTarget` to compute the target independently; do nothing if it is not legal | BRIEF §7.6 |
| 5 | server | if legal, settle damage (the player's own attack power) and knockback (D-75's vanilla default strength) | D-75 |
| 6 | server | **send no displacement** (do not send `ClientboundSetEntityMotionPacket` to correct the player) | avoid "being pulled back" |

**Why step 6 must be "send nothing"**: BRIEF §7.6's pending item says it plainly — `ServerPlayer#setDeltaMovement` does not move the client-side player (player movement is client-simulated), so "the server gives velocity" must land on client driving in implementation. If the server forces a velocity packet out, the client gets overwritten by its own position validation on the next tick, which shows up as "pulled back halfway through the dash". **Red line**: no new network packet for this (D-50).

**Pending verification (BRIEF §7.6, verbatim)**: velocity given by the server does not take effect on players.

- **How to verify**: on the server call `player.setDeltaMovement(0, 0, -2)` and print a log line, then observe whether the client is displaced (expectation: no displacement, or only a slight jitter); at the same time check whether the server prints `moved too quickly!`.
- **RT attribution**: attached to **RT-12** (the driver of the dash displacement is misjudged, the player does not move or is pulled back).

**Two known side effects and their handling (added by this document)**:

| Side effect | Cause | Handling |
|---|---|---|
| client displacement trips the server's "moved too quickly" validation | 5 blocks in 3 ticks = 1.67 blocks/tick, above vanilla's usual expectation of 0.0625 blocks/tick | watch `latest.log` for `moved too quickly!` / `moved wrongly!`; if they appear, raise `dashTicks` to 4–5, or switch to the alternative |
| dashing into a wall | the client simulates collision as usual (D-65: stop at the wall, no position correction, no phasing through) | nothing to handle — this is exactly what D-65 wants |

**Alternative option (given by BRIEF §7.6; switching requires a decision record)**: a server-side per-tick short-step `teleportTo` (≤ 1 block per tick, with collision detection). Switch condition: the preferred option's measured feel is unacceptable (for example "it dashes over but the attack does not follow", "displacement and damage are out of sync"). On switching, append a paragraph to this section — "decision record: YYYY-MM-DD, switched from A to B, because …" — and change RT-12's primary mitigation to "per-tick teleport".

### 4.6 Looping sound (D-76 / D-53 / D-66) → RT-14

**Trigger condition**: `farmstorm:wild_hunter` present on the local player.

**Implementation**: one hand-written `SoundInstance` subclass on the client, `isLooping() = true`, updating its position to the player every tick, taking the audible range from `WildHunterClientState.loopRange()` (= `wild_hunter.loopSoundRange`, default 16.0 blocks, range 0.0–64.0), with the sound category `SoundSource.PLAYERS`.

```java
// client/audio/WildHunterLoopSound.java
public final class WildHunterLoopSound extends SoundInstance {
    @Override public boolean isLooping() { return true; }
    @Override public boolean canPlaySound() { return true; }
    @Override public SoundSource getSource() { return SoundSource.PLAYERS; }   // does not take MUSIC, does not interrupt background music

    @Override
    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { stop(); return; }
        this.x = player.getX(); this.y = player.getY(); this.z = player.getZ();   // the source follows the player
        if (!player.hasEffect(FarmstormMobEffects.WILD_HUNTER)) stop();          // stop at once when the effect is gone
    }
}
```

**Why not re-send a play instruction from the server every tick** (D-76, verbatim): sending `ClientboundSoundPacket` every tick from the server is 20 packets/second/player, whereas the lifetime of the looping sound is exactly the lifetime of the effect — the presence of the effect already tells the client "play it". A client-side `SoundInstance` is 0 packets.

**Pending verification (BRIEF §7.7, verbatim)**: whether the 16-block attenuation on 1.21.1 should be driven through `SoundInstance#getVolume` or through its audible-range hook.

- **How to verify**: stand at 8 / 16 / 17 / 32 blocks and listen once at each, recording "audible / not audible / does the volume fall with distance". Also raise `getVolume()` from 1.0 to 4.0 and check whether it gets louder at distance (whether a larger volume is treated as "carrying farther").
- **Verdict**: still clearly audible at 17 blocks means the attenuation radius is not taking effect, and it must instead drive `attenuation_distance` of that event in `sounds.json` (if the field exists, **pending verification, attached to RT-14**) or switch to a manual distance attenuation through `getVolume()`.
- **RT attribution**: attached to **RT-14** (audio cannot be produced / the audio parameters are wrong).

**The two events of sounds.json** are in §6.3; the conversion parameters of the two `.ogg` files are in §6.2.

---

## 5. Phase Synchronization Plan for Zangief Cane

This section expands BRIEF §7.5 into an implementable timing. The core constraint is D-50: **no new `CustomPacketPayload` is allowed**, so "the server tells the client which phase it is in" can only ride the amplifier of `farmstorm:zangief_piledriver`.

### 5.1 State definition

| State quantity | Carrier | Value range | Who writes | Who reads |
|---|---|---|---|---|
| Phase number | the amplifier of `ZangiefPiledriverEffect` | 0 / 1 / 2 | server, `ZangiefPiledriverController#setPhase` | client, `ZangiefClientState` (once per tick) |
| Remaining phase duration | the duration of the same `MobEffectInstance` | 14 / 20 / ≤100 ticks | same as above | the client uses it for logs and debugging only, not for logic |
| "Present or not" | whether the effect instance exists | true / false | the server's `addEffect` / `removeEffect` | all 6 client classes (`locked()` in §4.3 is exactly this) |

The three quantities together are the complete state, and no fourth quantity needs synchronizing — this is D-50's sentence "presence of the effect + amplifier + remaining duration is the complete state" landed literally.

### 5.2 Server timing (the single truth)

| Moment | Event | Server action | Vanilla packets emitted |
|---|---|---|---|
| t = 0 | the trap triggers | `addEffect(zangief_piledriver, 14 ticks, amplifier 0, ambient=false, visible=false, showIcon=false)`; `playSound(zangief_piledriver)`; `be.consume()` | `ClientboundUpdateMobEffectPacket` ×1, `ClientboundSoundPacket` ×1, `ClientboundBlockUpdatePacket` ×1, `ClientboundLevelEventPacket` ×1 |
| t = 14 | phase 0 → 1 | `removeEffect`, then immediately `addEffect(…, 20 ticks, amplifier 1)`; at the same time `addEffect(LEVITATION, 20 ticks, amplifier 6)` | `ClientboundRemoveMobEffectPacket` ×1, `ClientboundUpdateMobEffectPacket` ×2 |
| t = 34 | phase 1 → 2 | `removeEffect`, then `addEffect(…, 100 ticks, amplifier 2)`; from then on servo every tick: `setDeltaMovement(0, -remainingHeight / remainingTicks, 0)` | same as above ×1 + one `ClientboundSetEntityMotionPacket` per tick |
| t = landing | end | `removeEffect`; remove from `ACTIVE` | `ClientboundRemoveMobEffectPacket` ×1 |
| t = 34+100 | timeout backstop | same as "end", plus one `warn` line: `桑吉尔蔗 相位 2 超时（玩家 {} 未落地）` | same as above |

**Why t = 14 does not reuse the same instance and just change the amplifier**: see the "hard switch" of §3.11 — the merge rules of `MobEffectInstance#update` do not guarantee a replacement, and it does not reset the duration. "Remove + add" pins all three fields at once (amplifier / duration / visibility).

**The 1-tick gap**: a hard switch emits both the "remove" and the "add" packet within the same tick, and while processing the two the client **may** have one frame without the effect. That is why the self-check of §5.6 must tolerate 2 ticks. **Pending verification (attached to RT-15)**: when both packets arrive in the same tick, whether the client's `getEffect` returns `null` between two reads; the verification is the log line `桑吉尔蔗 相位切换 t={} 客户端读到 amplifier={}`, checking whether `null` ever appears.

### 5.3 Full call sequence of apply / remove

```java
// ZangiefPiledriverController#setPhase —— the only place that changes the amplifier
private static void setPhase(ServerPlayer player, int phase, int durationTicks) {
    player.removeEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);                 // hard switch, step 1
    player.addEffect(new MobEffectInstance(FarmstormMobEffects.ZANGIEF_PILEDRIVER,
            durationTicks, phase, false /*ambient*/, false /*visible*/, false /*showIcon*/));
    ACTIVE.put(player.getUUID(), new PhaseState(phase, player.level().getGameTime()));
}
```

| Concern | Rule |
|---|---|
| `visible` / `showIcon` | both passed `false`. This is the first safety net for "hidden"; overriding `shouldRenderHUD()` on the class is the second (§3.6) |
| `ambient` | `false`. Neither particles nor an icon appear, consistent with hidden |
| Player death / logout | clear `ACTIVE` in `PlayerLoggedOutEvent` and `LivingDeathEvent` (the effect itself is cleared on death, but `ACTIVE` is an in-memory table and would otherwise survive into the next life) |
| Server restart | `ACTIVE` is an in-memory table, cleared inherently (precedent D-18: the creative-mode timer is also memory only) |
| Repeated trigger | a player already being piledriven stepping on another trap: `begin` first tests `ACTIVE.containsKey(uuid)` and **returns directly** if true (no stacking, no reset) |

### 5.4 Client read timing

The **only read point** is `ClientTickEvent.Post` (client, once per tick); the result is written into a snapshot, and the render thread only reads the snapshot.

```java
// client/ZangiefClientState.java
private static int phase = -1;             // -1 = no effect
private static int missingTicks;           // consecutive ticks without the effect (the tolerance window of RT-15)
private static float accumulated;

@SubscribeEvent
public static void onClientTick(ClientTickEvent.Post event) {
    LocalPlayer player = Minecraft.getInstance().player;
    MobEffectInstance inst = player == null ? null
            : player.getEffect(FarmstormMobEffects.ZANGIEF_PILEDRIVER);
    if (inst == null) {
        if (++missingTicks >= 2) { phase = -1; accumulated = 0.0F; }   // tolerate the 1-tick gap of the hard switch
        return;
    }
    missingTicks = 0;
    int next = inst.getAmplifier();
    if (next != phase) { phase = next; accumulated = 0.0F; }           // a phase change resets the spin
    if (phase == 2) accumulated += (float) DEFAULT_SPIN_DEGREES_PER_TICK;  // only phase 2 spins (D-70)
}
```

| Why read in the tick and not in rendering | rendering may run several times per frame (high-refresh displays), and reading `MobEffectInstance` on the render thread couples "logical state" to "frame rate"; the snapshot approach lets all three render hooks of §4.1 / §4.2 / §4.4 read just two ints and one float |
|---|---|
| Why tolerate 2 ticks | on the hard-switch tick the client may read `null` (the pending item of §5.2); without tolerance the screen would flicker one frame of "back to normal" |
| Is there a visual lag | at most 1 tick (50 ms). For a state like "being piledriven" that is imperceptible, and what is bought is zero packets and zero render-thread locks |

### 5.5 Matrix of three phases × five client subsystems

| Subsystem | Phase 0 (0–14 ticks) | Phase 1 (14–34 ticks) | Phase 2 (34 ticks–landing) | After the effect is gone |
|---|---|---|---|---|
| Camera roll = 180° (D-59) | on | on | on | off |
| Camera pitch negated (D-59) | on | on | on | off |
| Camera yaw spin (D-70) | off | off | **on** (+72° per tick, counter-clockwise; 72 × 20 = 1440 degrees/second) | off |
| Input lock (D-60) | on | on | on | off |
| Looping sound (D-76) | not applicable (that is `wild_hunter`) | — | — | — |
| Grayscale (D-61) | not applicable (that is `wild_hunter`) | — | — | — |
| Red silhouette (D-62) | not applicable (that is `wild_hunter`) | — | — | — |

**The count inside the table**: 5 subsystems = camera (roll / pitch / yaw, three rows counting as one), input lock, looping sound, grayscale, red silhouette; the table has **7 rows** in total. The three "not applicable" rows (looping sound, grayscale, silhouette) have nothing to do with the three phases of `zangief_piledriver` — their switch is whether `wild_hunter` is present (the binding table of §0.6), and they are the responsibility of §4.6, §4.1 and §4.2 respectively.

**The spin is phase 2 only**: line 29 of `plan0.0.2.txt` reads "after the levitation effect ends… and continues until just before landing… rotate the horizontal view", i.e. the spin interval = from the end of levitation to landing. D-70's "before landing" takes the same stretch following that word order. Phases 0 / 1 flip but do not spin.

**The flip starts already in phase 0**: the first sentence of the same source line is "flip its view upside down", and only then comes "grant levitation after 14 game ticks", so the flip (roll + pitch) covers all three phases.

### 5.6 Self-check and reset (RT-15)

RT-15's risk is "the hidden MobEffect is disturbed by other mods' effect-clearing logic" — for example a mod that "clears all negative effects" clears `zangief_piledriver` as well, while the client is still at `phase = 2`, leaving the screen flipped forever and the keys locked forever.

**Primary mitigation: the client self-checks every tick whether "the phase effect is still there" and resets itself if not** (BRIEF §7.8, verbatim). Landed as three assertions:

| Assertion | Location | When it does not hold |
|---|---|---|
| The phase number must be within `{-1, 0, 1, 2}` | `ZangiefClientState#onClientTick` | log a `warn` and force `-1` (any other amplifier counts as invalid) |
| When `phase >= 0` the effect must exist | same as above | 2 consecutive ticks without it ⇒ reset (the `missingTicks` of §5.4) |
| When `phase == -1` the three render hooks must do nothing | `GrayscalePass` / `ZangiefCameraController` / `InputLockHandler` | each hook tests `phase` / `hasEffect` on its first line and returns without resetting |

**The symmetric self-check on the server side**: in `ZangiefPiledriverController#onPlayerTick`, if `ACTIVE` has a record but `player.getEffect(...)` is empty (cleared by another mod), call `finish(player)` directly and log a `warn`; do not try to re-apply — the effect being cleared by an outside force means third-party interference, and re-applying would turn it into a fight between the two sides.

**How to verify (implementation phase)**: `/effect give @s farmstorm:zangief_piledriver 10 2 false` (granting an amplifier-2 effect directly) → the screen is flipped + input is locked + it spins; `/effect clear @s` → the screen and input must return to normal **within 100 ms**, and the log shows `桑吉尔蔗 状态复位（效果被外部清除）`.

### 5.7 Why this whole design needs no new packet

Because only three scalars need synchronizing, and those three scalars happen to be exactly the mutable fields of `MobEffectInstance` (id / amplifier / duration), which vanilla `ClientboundUpdateMobEffectPacket` sends verbatim. See rows 1–3 of §9.2 for the full proof. In particular:

- The **phase number** needs no packet: it is the amplifier.
- The **instant of a phase switch** needs no packet: `removeEffect` + `addEffect` each trigger one vanilla packet.
- The boolean **"being piledriven"** needs no packet: it is "is the effect instance present".
- The **downward velocity** needs no packet: `setDeltaMovement` on `ServerPlayer` goes through `ClientboundSetEntityMotionPacket` (broadcast by the server to all trackers, including the player itself).

---

## 6. Audio and Texture Pipeline

All three source files must be converted before they enter `src/main/resources` (D-53). Minecraft's sound engine decodes **Ogg Vorbis** only, and the effect icon must be a **PNG** — copying a file in under its original extension is the same as shipping a file that will never be played or displayed.

### 6.1 The three source files (verified to exist on this machine)

| # | Source path (absolute) | Size / form | Purpose | Target path |
|---|---|---|---|---|
| 1 | `C:\Users\DASHpig\Downloads\Animals.mp3` | mp3 audio | Wild Hunter looping sound | `assets/farmstorm/sounds/wild_hunter_loop.ogg` |
| 2 | `D:\dsh\default\sf6_audio\out\BV1qu411W7ha_846-850.mp3` | mp3 audio (SF6 excerpt) | Zangief Cane trigger sound | `assets/farmstorm/sounds/zangief_piledriver.ogg` |
| 3 | `C:\Users\DASHpig\Desktop\e91bb6997d8e24cda1f596267c41da97500924726.jpg` | desktop JPG | Wild Hunter effect icon | `assets/farmstorm/textures/mob_effect/wild_hunter.png` |

Path provenance: lines 29 / 39 / 40 of `plan0.0.2.txt`; the `Test-Path` of all three files was `True` when this document was written. **They live outside the repository**, so the conversion script must **check existence before doing anything**, and exit with an error when a file is missing as well (same rule as a missing ffmpeg).

Provenance and the non-commercial personal-use statement go into the README and the Release notes per D-66, and into no file inside the jar.

### 6.2 Conversion parameters

| Target | ffmpeg arguments | Criterion |
|---|---|---|
| audio → Ogg Vorbis | `-ac 1 -ar 44100 -c:a libvorbis -b:a 64k` | **mono** (1 channel), **64 kbps** (D-53) |
| icon → PNG | `-vf scale=18:18:flags=lanczos -pix_fmt rgba` | **18×18**, no alpha-channel requirement (D-53) |

```powershell
ffmpeg -y -i "<source mp3>" -vn -ac 1 -ar 44100 -c:a libvorbis -b:a 64k "<target>.ogg"
ffmpeg -y -i "<source jpg>" -vf "scale=18:18:flags=lanczos" -pix_fmt rgba "<target>.png"
```

- `-vn` must be given: if the source mp3 carries cover art, omitting it writes the cover into the ogg as a video stream, and Minecraft refuses to load it outright.
- `-ar 44100` fixes the sample rate: mixing sample rates from different sources makes the volume sound alternately loud and soft.
- 18×18 rather than 16×16: the effect icon is drawn at 18×18 in the vanilla HUD (the icon area of `MobEffectInstance`), and 16×16 would be stretched 1.125×, blurring the edges.

### 6.3 `assets/farmstorm/sounds.json` (1 file, 2 events)

BRIEF §7.7 requires "two events in `sounds.json`: `wild_hunter_loop` (looping), `zangief_piledriver` (one-shot)":

```json
{
  "wild_hunter_loop": {
    "category": "player",
    "subtitle": "subtitles.farmstorm.wild_hunter_loop",
    "sounds": [
      { "name": "farmstorm:wild_hunter_loop", "stream": false, "attenuation_distance": 16 }
    ]
  },
  "zangief_piledriver": {
    "category": "player",
    "subtitle": "subtitles.farmstorm.zangief_piledriver",
    "sounds": [
      { "name": "farmstorm:zangief_piledriver", "attenuation_distance": 16 }
    ]
  }
}
```

| Field | Value | Reason |
|---|---|---|
| `category` | `player` | corresponds to `SoundSource.PLAYERS` (D-76: does not take `MUSIC`, does not interrupt vanilla background music) |
| `stream` | `false` | a looping sound is re-read on every loop, and `stream` leaves a seam at the joint. `Animals` converts to low-bitrate mono and holds entirely in memory without pressure |
| `attenuation_distance` | `16` | D-76's "attenuation distance 16 blocks" = a static mirror of the **default value** of `wild_hunter.loopSoundRange` (json cannot read the config; when the config is set to something else, the program-side attenuation of §4.6 prevails); **pending verification (attached to RT-14)**: whether the key name and semantics of this field in 1.21.1's `SoundDefinition.Sound` (the `sounds.json` entry) really are "audible distance" — see §4.6 for the verification |
| `subtitle` | `subtitles.farmstorm.<event name>` | corresponds to the two subtitle keys of §7.3 (shown when subtitles are on) |

### 6.4 Conversion script `tools/convert_media.ps1`

**Toolchain requirements**:

| Item | Requirement | Current state on this machine |
|---|---|---|
| ffmpeg | on `PATH`, or an absolute path given by the `-Ffmpeg` argument; must carry the `libvorbis` encoder (`ffmpeg -encoders \| Select-String libvorbis` produces output) | **installed but not on PATH**: the usable build on this machine is `D:\dsh\default\sf6_audio\tools\ffmpeg.exe` (ffmpeg 7.1 essentials, carries `libvorbis`); the 6.1.1 bundled with Trae CN is trimmed and has no `libvorbis`, so it is unusable → the real-world basis of RT-14 |
| PowerShell | 5.1 or 7.x | satisfied |
| network | **not needed**. The script must not download ffmpeg itself | all three sources on this machine (npm / maven / mirrors) are unreachable (the measurement recorded in D-53) |

**Parameters**: three invocation forms — no argument (convert everything when ffmpeg is on `PATH`), `-Ffmpeg <absolute path>` (ffmpeg not on `PATH`), `-CheckOnly` (validate only the environment and the three source files, not writing a single byte).

```powershell
pwsh -File tools/convert_media.ps1
pwsh -File tools/convert_media.ps1 -Ffmpeg "D:\dsh\default\sf6_audio\tools\ffmpeg.exe"
pwsh -File tools/convert_media.ps1 -CheckOnly
```

**Failure behavior (a hard requirement of D-53, not relaxable item by item)**:

| Situation | Behavior | Exit code |
|---|---|---|
| `ffmpeg` not found | print installation guidance (`winget install Gyan.FFmpeg` / unzip the official zip and add it to PATH / drop it into the project's `tools/ffmpeg/bin`), **exit directly**, writing not one file | `1` |
| source file missing | list which absolute path is missing, **exit directly** | `1` |
| ffmpeg returns non-zero | print its full stderr, **exit directly**, do not continue to the next file | `1` |
| conversion succeeded | print `source → target (size, channels, bitrate/dimensions)` per item, and read back every target once at the end | `0` |

**Forbidden (D-53, verbatim: "no silent skipping, no falling back to the original extension")**:

- Do not continue after `try { … } catch { Write-Warning }` — a failure must terminate the whole pipeline.
- Do not "rename the `.mp3` into `sounds/` when it cannot be converted" — Minecraft decodes Ogg Vorbis only, and a renamed file plays nothing.
- Do not download ffmpeg inside the script (all three sources on this machine are unreachable, so it would be dead code).

**Idempotence**: when the target exists and is newer than the source, skip that file and print `skip`, so the script can be run repeatedly.

**Read-back validation** (the criterion for a successful conversion, printed item by item):

```powershell
ffprobe -v error -show_entries stream=codec_name,channels,sample_rate,bit_rate -of json "<target>.ogg"
ffprobe -v error -show_entries stream=width,height,pix_fmt -of json "<target>.png"
```

The first expects `codec_name=vorbis`, `channels=1`, `bit_rate` ≈ 64000; the second expects `width=18`, `height=18`. Any mismatch exits with code 1.

### 6.5 Compressed hay block texture: a grayscale version generated from the vanilla hay block side

D-57 requires "texture = a grayscale version of the vanilla hay block side". This step **needs no ffmpeg**: it uses the same toolchain as the existing `tools/gen_textures.py` (Python 3.9+ / Pillow 9.1+, reading the client jar directly with `zipfile`, no extraction).

**Source**: `assets/minecraft/textures/block/hay_block_side.png` inside the client jar. The file was verified to exist on this machine:

| Item | Value |
|---|---|
| client jar | `D:\project_farm\.tools\gradle-home\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar` |
| entry | `assets/minecraft/textures/block/hay_block_side.png` (202 bytes, 16×16) |
| extraction | `zipfile.ZipFile(jar).read(entry)` → `PIL.Image.open(BytesIO(...))`, writing no temporary file (consistent with the existing approach of `gen_textures.py`) |

**Conversion** (three steps, written into `tools/gen_textures_002.py`):

```python
src = Image.open(BytesIO(zipfile.ZipFile(jar).read("assets/minecraft/textures/block/hay_block_side.png")))
rgba = src.convert("RGBA")
gray = rgba.convert("L").convert("RGBA")      # luminance only, alpha preserved (the vanilla texture is opaque)
gray.save(out / "assets/farmstorm/textures/block/compressed_hay_block.png")
```

| Requirement | How it is met |
|---|---|
| must be grayscale | `convert("L")` and then `convert("RGBA")`: R=G=B=L. Do not substitute "reduce saturation" (that leaves a color cast) |
| size unchanged | 16×16, the same size as vanilla; no noise and no blur is added |
| re-runnable | the script re-reads the source image from the jar every time and caches no intermediate product |

**The same script also produces 4 16×16 item icons** (`molotov` / `zangief_cane` / `wild_apple` / `compressed_puffed_wheat`) and 1 `textures/block/zangief_cane.png` (used only as the trap block's `particle` texture, D-68). The placeholder rules follow `gen_textures.py`: the bottom-right corner of a crop icon gets a green "+" overlay (`plan0.0.1.txt` item 8 distinguishes vanilla from improved), and the four new crops get the "+" as well.

### 6.6 The 1×1 white texture

`assets/farmstorm/textures/misc/white.png`: 1×1, all white, RGBA (255,255,255,255). Specified by BRIEF §7.2. The silhouette takes its red from `setShaderColor`, so the texture itself must be pure white — otherwise the colors multiply and go muddy.

### 6.7 Pipeline order and evidence

| Order | Step | Command | Product | Stop on failure |
|---|---|---|---|---|
| 1 | audio + icon | `pwsh -File tools/convert_media.ps1` | 2 `.ogg` + 1 18×18 `.png` | yes (exit 1 when ffmpeg is missing) |
| 2 | 7 textures (4 item icons + compressed hay block + Zangief Cane particle + 1×1 white) | `python -X utf8 tools/gen_textures_002.py --vanilla-assets "<client jar>" --out src/main/resources` | 7 `.png` (6 × 16×16 + 1 × 1×1) | yes |
| 3 | hand-written resources | write `sounds.json` / `shaders/core/*` / `post_effect/grayscale.json` in an editor | 4 files | — |
| 4 | DataGen | `.tools\gw.cmd build` | the 24 JSONs marked "DataGen" in §7.2 | yes |

**PNG tally (aligned row by row with §7.2)**: step 1 produces **1** (#8, the 18×18 effect icon) + step 2 produces **7** (#1–#4 item icons, #5 compressed hay block, #6 Zangief Cane particle, #7 white) = **8**, exactly §7.2's #1–#8, not one more and not one less. OGG: step 1 produces **2** (#9–#10).

**Evidence must be kept** (the acceptance material for RT-14): the full stdout of step 1 (including the read-back validation line of every target), the "vanilla hit / fell back to placeholder" list of step 2, and the 2 `.ogg` entries under `assets/farmstorm/sounds/` inside `dist/farmstorm-0.0.2+mc1.21.1.jar` (via `jar tf` or by extracting).

**The two scripts do not enter the jar and are not counted in the 36 of §7.2**: they live under the repository's `tools/` and are build-time tools, not mod resources.

---

## 7. Resource and Data File Inventory

### 7.1 Summary

| Category | v0.0.1 | Delta | v0.0.2 |
|---|---|---|---|
| `.json` | 169 | +24 | **193** |
| `.png` | 91 | +8 | **99** |
| `.ogg` | 0 | +2 | **2** |
| `.glsl` (`.vsh` + `.fsh`) | 0 | +2 | **2** |
| `.toml` (`neoforge.mods.toml`) | 1 | +0 | **1** |
| `pack.mcmeta` | 1 | +0 | **1** |
| **Total** | **262** | **+36** | **298** |

Counting reminder (BRIEF §4.1): the 11 extension-less files under `src/generated/resources/.cache` are DataGen caches — **they do not enter the jar and are not counted**. The 2 language-file `.json`s are already included in the 169 (their content changes, their file count does not).

### 7.2 Itemized inventory of new resource files (**36 rows**)

The "Line" column: `hand-written` = written by hand into `src/main/resources` with an editor; `DataGen` = generated by a Provider into `src/generated/resources`; `script` = produced by the conversion scripts of §6.

| # | Path (relative to `src/{main,generated}/resources`) | Category | Line | Purpose | Basis |
|---|---|---|---|---|---|
| 1 | `assets/farmstorm/textures/item/molotov.png` | PNG 16×16 | script | Molotov item icon | §2.2 |
| 2 | `assets/farmstorm/textures/item/zangief_cane.png` | PNG 16×16 | script | Zangief Cane item icon (invisible in the world, the icon is independent, D-68) | D-68 |
| 3 | `assets/farmstorm/textures/item/wild_apple.png` | PNG 16×16 | script | Wild Apple item icon | §2.2 |
| 4 | `assets/farmstorm/textures/item/compressed_puffed_wheat.png` | PNG 16×16 | script | Compressed Puffed Wheat item icon | D-56 |
| 5 | `assets/farmstorm/textures/block/compressed_hay_block.png` | PNG 16×16 grayscale | script | Compressed hay block six-face texture (grayscale hay block side) | D-57 / §6.5 |
| 6 | `assets/farmstorm/textures/block/zangief_cane.png` | PNG 16×16 | script | The trap block's `textures.particle` (invisible in the world, D-68) | D-68 |
| 7 | `assets/farmstorm/textures/misc/white.png` | PNG 1×1 | script | base texture of the red silhouette | BRIEF §7.2 |
| 8 | `assets/farmstorm/textures/mob_effect/wild_hunter.png` | PNG 18×18 | script | Wild Hunter effect icon | D-53 / D-66 |
| 9 | `assets/farmstorm/sounds/wild_hunter_loop.ogg` | OGG mono 64k | script | the looping sound itself | D-53 / D-76 |
| 10 | `assets/farmstorm/sounds/zangief_piledriver.ogg` | OGG mono 64k | script | the Zangief Cane trigger sound itself | D-53 / D-66 |
| 11 | `assets/farmstorm/sounds.json` | JSON | hand-written | 2 sound event definitions | D-76 / §6.3 |
| 12 | `assets/farmstorm/shaders/core/grayscale.json` | JSON | hand-written | program declaration of the grayscale `ShaderInstance` | BRIEF §7.1 |
| 13 | `assets/farmstorm/shaders/core/grayscale.vsh` | GLSL | hand-written | full-screen quad vertex shader | BRIEF §7.1 / §3.2 |
| 14 | `assets/farmstorm/shaders/core/grayscale.fsh` | GLSL | hand-written | grayscale fragment shader (`dot(rgb, luma coefficients)`) | BRIEF §7.1 / §3.2 |
| 15 | `assets/farmstorm/post_effect/grayscale.json` | JSON | hand-written | grayscale post-effect declaration (the `PostChain` mount of the RT-10 degradation branch) | BRIEF §3.2 |
| 16 | `assets/farmstorm/blockstates/compressed_hay_block.json` | JSON | DataGen | single-variant `cube_all` | D-57 |
| 17 | `assets/farmstorm/blockstates/zangief_cane.json` | JSON | DataGen | all variants of `face`×`facing`×`floating` → empty model | D-68 |
| 18 | `assets/farmstorm/models/block/compressed_hay_block.json` | JSON | DataGen | six-face single-texture model | D-57 |
| 19 | `assets/farmstorm/models/block/zangief_cane.json` | JSON | DataGen | empty model (only `textures.particle`) | D-68 |
| 20 | `assets/farmstorm/models/item/molotov.json` | JSON | DataGen | `item/generated` + layer0 | §2.2 |
| 21 | `assets/farmstorm/models/item/zangief_cane.json` | JSON | DataGen | `item/generated` (does not reference the block model) | D-68 |
| 22 | `assets/farmstorm/models/item/wild_apple.json` | JSON | DataGen | `item/generated` | §2.2 |
| 23 | `assets/farmstorm/models/item/compressed_puffed_wheat.json` | JSON | DataGen | `item/generated` | D-56 |
| 24 | `assets/farmstorm/models/item/compressed_hay_block.json` | JSON | DataGen | `parent = farmstorm:block/compressed_hay_block` | D-57 |
| 25 | `data/farmstorm/recipe/converter.json` | JSON | DataGen | converter crafting: center `minecraft:crafting_table` + eight `#farmstorm:converter_fuel` | D-52 |
| 26 | `data/farmstorm/recipe/compressed_puffed_wheat.json` | JSON | DataGen | shapeless: 4 `puffed_wheat` → 1 `compressed_puffed_wheat` | D-67 |
| 27 | `data/farmstorm/recipe/molotov.json` | JSON | DataGen | `ConverterRecipe`: `minecraft:crimson_fungus` ⇄ `molotov`, bidirectional, experience 3, no catalyst | same shape as D-54 |
| 28 | `data/farmstorm/recipe/zangief_cane.json` | JSON | DataGen | `ConverterRecipe`: `minecraft:sugar_cane` ⇄ `zangief_cane`, bidirectional, experience 3 | same shape as D-54 |
| 29 | `data/farmstorm/recipe/wild_apple.json` | JSON | DataGen | `ConverterRecipe`: `minecraft:golden_apple` ⇄ `wild_apple`, bidirectional, experience 3 | D-54 |
| 30 | `data/farmstorm/advancement/first_molotov.json` | JSON | DataGen | advancement: throw a Molotov for the first time | D-63 |
| 31 | `data/farmstorm/advancement/zangief_hit.json` | JSON | DataGen | advancement: Zangief Cane hits for the first time | D-63 |
| 32 | `data/farmstorm/advancement/first_wild_hunter.json` | JSON | DataGen | advancement: obtain the Wild Hunter effect for the first time | D-63 |
| 33 | `data/farmstorm/loot_table/blocks/compressed_hay_block.json` | JSON | DataGen | drops itself (`requiresCorrectToolForDrops` makes it drop nothing bare-handed) | D-57 |
| 34 | `data/farmstorm/loot_table/blocks/zangief_cane.json` | JSON | DataGen | deployed-block conventions: `survives_explosion` + drops itself | same shape as the 5 existing ones |
| 35 | `data/farmstorm/tags/item/converter_fuel.json` | JSON | DataGen | new item tag: vanilla crops + the items of the 19 profiles that can act as crops | D-52 |
| 36 | `data/minecraft/tags/block/needs_diamond_tool.json` | JSON | DataGen | new: `compressed_hay_block` needs a diamond pickaxe | D-57 |

Column mapping (this section is a file inventory, not a key table): **Path** ↔ key name; **Category** ↔ default value (including file dimensions, e.g. `PNG 16×16`); **Line** ↔ range (who produces the file, can it be re-run); **Purpose** ↔ purpose; **Basis** ↔ the D number it rests on.

**Row count check**: `36` rows; by category, PNG 8 (#1–#8) + OGG 2 (#9–#10) + JSON 24 (#11–#12, #15–#36) + GLSL 2 (#13–#14) → see the table below. #11–#36 is 26 slots = 24 JSON + 2 GLSL, consistent with the category counts.

| Category | Row numbers | Count |
|---|---|---|
| PNG | #1–#8 | 8 |
| OGG | #9–#10 | 2 |
| JSON | #11–#12, #15–#36 | 24 |
| GLSL | #13–#14 | 2 |
| **Total** | — | **36** |

**Append-only changes (produce no new file and are not counted in the 36)**: `data/minecraft/tags/block/mineable/pickaxe.json` gains `compressed_hay_block`; `data/farmstorm/tags/block/deployed_crops.json` gains `zangief_cane`; `data/farmstorm/tags/item/modified_crops.json` gains 4 entries; `assets/farmstorm/lang/{zh_cn,en_us}.json` gain 39 entries (§7.3); the 12 existing advancement files are untouched.

### 7.3 Itemized inventory of new language file keys (**39 rows**)

The key set is written out column by column from the same `ENTRIES` table of `ModLangProvider`, so the keys of `zh_cn` and `en_us` are equal by construction. `222 + 39 = 261`, and both must be **261**.

| # | Key | `zh_cn` | `en_us` | Basis |
|---|---|---|---|---|
| 1 | `item.farmstorm.molotov` | 莫洛托夫！ | Molotov | §2.2 / exclamation mark kept from the source |
| 2 | `item.farmstorm.zangief_cane` | 桑吉尔蔗 | Zangief Cane | §2.2 |
| 3 | `item.farmstorm.wild_apple` | 狂野苹果 | Wild Apple | §2.2 |
| 4 | `item.farmstorm.compressed_puffed_wheat` | 压缩膨胀麦 | Compressed Puffed Wheat | D-56 |
| 5 | `item.farmstorm.compressed_hay_block` | 压缩干草块 | Compressed Hay Block | D-57 |
| 6 | `block.farmstorm.zangief_cane` | 桑吉尔蔗 | Zangief Cane | §2.2 |
| 7 | `block.farmstorm.compressed_hay_block` | 压缩干草块 | Compressed Hay Block | D-57 |
| 8 | `effect.farmstorm.wild_hunter` | 狂 野 猎 人 | Wild Hunter | `plan0.0.2.txt` line 37 |
| 9 | `effect.farmstorm.zangief_piledriver` | 桑吉尔蔗打桩 | Zangief Piledriver | §3.6 (hidden effect, the name only serves `/effect` completion) |
| 10 | `subtitles.farmstorm.wild_hunter_loop` | 狩猎主题响起 | Hunting theme plays | §6.3 |
| 11 | `subtitles.farmstorm.zangief_piledriver` | 打桩机启动 | Piledriver hits | §6.3 |
| 12 | `advancements.farmstorm.first_molotov.title` | 火了！ | It's Lit! | D-63 |
| 13 | `advancements.farmstorm.first_molotov.description` | 初次投出莫洛托夫！ | Throw a Molotov for the first time | D-63 |
| 14 | `advancements.farmstorm.zangief_hit.title` | 手摇重 360 | Hand-Cranked 360 | D-63 |
| 15 | `advancements.farmstorm.zangief_hit.description` | 桑吉尔蔗首次命中一名玩家 | Land a Zangief Cane on a player for the first time | D-63 |
| 16 | `advancements.farmstorm.first_wild_hunter.title` | 狂野猎人 | Wild Hunter | D-63 |
| 17 | `advancements.farmstorm.first_wild_hunter.description` | 首次获得「狂 野 猎 人」效果 | Obtain the Wild Hunter effect for the first time | D-63 |
| 18 | `farmstorm.configuration.molotov.effectRadius` | 落点球半径（格） | Impact sphere radius (blocks) | §8.1 #1 |
| 19 | `farmstorm.configuration.molotov.fireChance` | 每个候选方块被点火的概率（%） | Chance (%) to place fire on each candidate block | §8.1 #2 |
| 20 | `farmstorm.configuration.molotov.triggerExplosionPower` | 落点触发爆炸的威力（0 = 只当触发器） | Power of the landing trigger explosion (0 = trigger only) | §8.1 #3 |
| 21 | `farmstorm.configuration.wild_apple.nutrition` | 营养值 | Nutrition | §8.1 #4 |
| 22 | `farmstorm.configuration.wild_apple.saturationModifier` | 饱和度修正 | Saturation modifier | §8.1 #5 |
| 23 | `farmstorm.configuration.wild_hunter.durationTicks` | 效果基础时长（tick） | Base effect duration (ticks) | §8.1 #6 |
| 24 | `farmstorm.configuration.wild_hunter.killExtensionTicks` | 每次击杀延长的时长（tick） | Duration added per kill (ticks) | §8.1 #7 |
| 25 | `farmstorm.configuration.wild_hunter.killHealPoints` | 每次击杀回复的生命点数 | Health restored per kill | §8.1 #8 |
| 26 | `farmstorm.configuration.wild_hunter.searchRadius` | 索敌球半径（格） | Target search radius (blocks) | §8.1 #9 |
| 27 | `farmstorm.configuration.wild_hunter.searchAngleDegrees` | 索敌视线夹角上限（度） | Target search view angle limit (degrees) | §8.1 #10 |
| 28 | `farmstorm.configuration.wild_hunter.dashTicks` | 吸附冲刺跨越的刻数 | Ticks the dash takes | §8.1 #11 |
| 29 | `farmstorm.configuration.wild_hunter.speedBonusPercent` | 移动速度加成（%） | Movement speed bonus (%) | §8.1 #12 |
| 30 | `farmstorm.configuration.wild_hunter.attackSpeedBonusPercent` | 攻击速度加成（%） | Attack speed bonus (%) | §8.1 #13 |
| 31 | `farmstorm.configuration.wild_hunter.attackDamageBonusPercent` | 攻击伤害加成（%） | Attack damage bonus (%) | §8.1 #14 |
| 32 | `farmstorm.configuration.wild_hunter.highlightRadius` | 红色剪影的最大距离（格） | Maximum silhouette distance (blocks) | §8.1 #15 |
| 33 | `farmstorm.configuration.wild_hunter.loopSoundRange` | 循环音的可听距离（格） | Looping sound audible range (blocks) | §8.1 #16 |
| 34 | `farmstorm.configuration.zangief_cane.triggerRadius` | 触发球半径（格） | Trigger sphere radius (blocks) | §8.1 #17 |
| 35 | `farmstorm.configuration.zangief_cane.stageDelayTicks` | 相位 0 的等待刻数 | Ticks spent in phase 0 | §8.1 #18 |
| 36 | `farmstorm.configuration.zangief_cane.levitationLevel` | 漂浮等级（1 = I 级） | Levitation level (1 = level I) | §8.1 #19 |
| 37 | `farmstorm.configuration.zangief_cane.levitationDurationTicks` | 漂浮时长（tick） | Levitation duration (ticks) | §8.1 #20 |
| 38 | `farmstorm.configuration.zangief_cane.fallTicks` | 相位 2 的目标落地时长（tick） | Target landing duration of phase 2 (ticks) | §8.1 #21 |
| 39 | `farmstorm.configuration.zangief_cane.spinDegreesPerTick` | 水平自转速度（度/刻） | Horizontal spin speed (degrees/tick) | §8.1 #22 |

Column mapping: **Key** ↔ key name; **the `zh_cn` / `en_us` columns** ↔ values (a language file has no "range"; the two texts cross-check each other); **Category** is given by the key prefix (`item.` / `block.` / `effect.` / `subtitles.` / `advancements.` / `farmstorm.configuration.`); **Basis** is the last column.

**Row count check**: `39` rows = items 5 (#1–#5) + blocks 2 (#6–#7) + effects 2 (#8–#9) + subtitles 2 (#10–#11) + advancements 6 (#12–#17) + config 22 (#18–#39). `222 + 39 = 261`.

**The two categories that are not added, with the reasons stated**:

| Not added | Why |
|---|---|
| `tooltip.farmstorm.*` | among the 16 tooltip keys of v0.0.1, the category keys are **per category** rather than per crop (`category.trap` / `category.damage` / `category.construction` / `category.buff` all already exist), and the category sets of the 4 new crops are subsets of these four, so 0 entries are added. The total stays 16 |
| `death.attack.*` | Molotov deals no damage (§2.4: `damageCap = 0` + `NEVER_BREAK`), there is no new damage type, so there is no new death message |

### 7.4 How to verify

| Number | How to check | Expectation |
|---|---|---|
| resources 298 | `Get-ChildItem -Recurse -File src\main\resources, src\generated\resources \| Where-Object Name -ne '.cache' \| Measure-Object`, then subtract the 11 files under `.cache` | 298 |
| JSON 193 | same, but count `.json` only | 193 |
| PNG 99 | count `.png` only | 99 |
| language keys 261 | `python -c "import json;print(len(json.load(open('…/zh_cn.json'))))"`, and the same for `en_us` | both 261 |
| the two key sets are equal | same, but compare `set(a) == set(b)` | `True` |

---

## 8. Full Inventory of New Config Keys

### 8.1 Itemized inventory (**22 rows**)

This table **copies `.work/v002-BRIEF.md` §8 "sole inventory of new config keys (22 entries)" column by column**; the key / type / default value / range columns match that table character for character — the rulings A-01…A-05 of BRIEF §8 have already fixed the conventions between `11-DESIGN-0.0.2.md` and this document, and neither document may count them again on its own. All keys go into `config/farmstorm-server.toml` (`ModConfig.Type.SERVER`). The key path is written `<group>.<key>` (ruling A-04), the language key is written `farmstorm.configuration.<group>.<key>`, fully isomorphic to the 117 of v0.0.1.

| # | Key | Type | Default | Range | Purpose | Basis |
|---|---|---|---|---|---|---|
| 1 | `molotov.effectRadius` | double | 4.0 blocks | 0.0–16.0 | landing sphere radius: every block inside the sphere whose top surface touches air rolls independently | D-58 |
| 2 | `molotov.fireChance` | double | 85.0 % | 0.0–100.0 | probability that fire is placed on each candidate block; **the remaining percentage automatically places a magma block, a second key is not set** | D-58 |
| 3 | `molotov.triggerExplosionPower` | int | 0 | 0–100 | power of the landing trigger explosion; 0 = trigger only | D-58 |
| 4 | `wild_apple.nutrition` | int | 4 points | 0–20 | Wild Apple nutrition value (same as the vanilla golden apple) | D-54 + added by this document |
| 5 | `wild_apple.saturationModifier` | double | 1.2 | 0.0–10.0 | Wild Apple saturation modifier (same as the vanilla golden apple) | D-54 + added by this document |
| 6 | `wild_hunter.durationTicks` | int | 700 ticks (35 seconds) | 1–72000 | base effect duration; this is exactly the duration the Wild Apple grants | plan0.0.2.txt line 52 |
| 7 | `wild_hunter.killExtensionTicks` | int | 160 ticks (8 seconds) | 0–72000 | extension per kill while the effect is active; no cap on the number of kills | D-74 |
| 8 | `wild_hunter.killHealPoints` | double | 10.0 points | 0.0–40.0 | health points restored per kill | D-74 |
| 9 | `wild_hunter.searchRadius` | double | 5.0 blocks | 0.0–32.0 | target search sphere radius | D-64 |
| 10 | `wild_hunter.searchAngleDegrees` | double | 30.0 degrees | 0.0–180.0 | upper limit of the angle between a candidate and the view direction | D-64 |
| 11 | `wild_hunter.dashTicks` | int | 3 ticks | 1–20 | ticks the attraction dash takes to cross to the target | D-65 |
| 12 | `wild_hunter.speedBonusPercent` | double | 30.0 % | 0.0–1000.0 | movement speed bonus | plan0.0.2.txt line 40 |
| 13 | `wild_hunter.attackSpeedBonusPercent` | double | 30.0 % | 0.0–1000.0 | attack speed bonus | plan0.0.2.txt line 40 |
| 14 | `wild_hunter.attackDamageBonusPercent` | double | 40.0 % | 0.0–1000.0 | attack damage bonus | plan0.0.2.txt line 40 |
| 15 | `wild_hunter.highlightRadius` | double | 64.0 blocks | 0.0–256.0 | maximum distance of the red silhouette | D-62 |
| 16 | `wild_hunter.loopSoundRange` | double | 16.0 blocks | 0.0–64.0 | audible range of the looping sound | D-76 |
| 17 | `zangief_cane.triggerRadius` | double | 1.5 blocks | 0.0–8.0 | trigger sphere radius (spherical Euclidean distance) | D-73 |
| 18 | `zangief_cane.stageDelayTicks` | int | 14 ticks | 0–200 | duration of phase 0 (flipped, waiting to levitate) | D-69 |
| 19 | `zangief_cane.levitationLevel` | int | level 7 | 1–255 | levitation level; amplifier = level − 1 = 6 | D-69 |
| 20 | `zangief_cane.levitationDurationTicks` | int | 20 ticks (1 second) | 0–72000 | levitation duration = duration of phase 1 | D-69 |
| 21 | `zangief_cane.fallTicks` | int | 40 ticks (2 seconds) | 1–200 | target landing duration of phase 2; the server servos the downward velocity every tick so that landing falls inside this duration | plan0.0.2.txt line 29 + added by this document |
| 22 | `zangief_cane.spinDegreesPerTick` | double | **72.0** degrees/tick | 0.0–360.0 | horizontal spin in phase 2; 72 × 20 = 1440 degrees/second | D-70 |

**Row count check**: `22` rows = `molotov` 3 (#1–#3) + `wild_apple` 2 (#4–#5) + `wild_hunter` 11 (#6–#16) + `zangief_cane` 6 (#17–#22). `117 + 22 = 139`.

**The four rulings against the 23 entries of the first 11-DESIGN version, and how this document lands them** (the ruling record is in BRIEF §8.1):

| Ruling | Conclusion | Where it lands here |
|---|---|---|
| A-01 | keep only `molotov.fireChance`, no `magmaChance` | the purpose cell of #2 states "the remaining percentage automatically places a magma block" |
| A-02 | the duration belongs to `wild_hunter.durationTicks`, the Wild Apple does not get its own `hunterDurationTicks` | the purpose cell of #6 states "this is exactly the duration the Wild Apple grants" |
| A-03 | use `zangief_cane.fallTicks` + per-tick servo, delete the "constant speed" option | #21; the mechanism is in §3.11 and §5.2 (that constant-speed key has been deleted and is not in the current version) |
| A-04 | the body writes `<group>.<key>`; only the language key carries the `farmstorm.configuration.` prefix | the first column of this table and #18–#39 of §7.3 |
| A-05 | `spinDegreesPerTick` defaults to **72.0** degrees/tick | #22 |

> **The default of #22 matches the plan source**: line 29 of `plan0.0.2.txt` requires "1440 degrees per second", and 1440 ÷ 20 ticks/second = **72.0 degrees/tick**. An older fact card once mis-wrote that arithmetic as 24, and BRIEF §8.1 ruling A-05 corrected it to 72.0. The direction is fixed to counter-clockwise and is not made a config option.

**Where the six defaults / conventions come from**: the ones BRIEF does not give and this document derives from convention are #4, #5 (same as the vanilla golden apple, because the correspondence in D-54 is exactly the golden apple) and the default of #21, 40 ticks (the plan only gives the "1.7–2 seconds" interval, so the upper bound 2.0 seconds is taken); #6 has a source (line 52 of the plan, "35 seconds"), #12–#14 have a source (the three percentages on line 40 of the plan), #22 has a source (1440 degrees/second on line 29 of the plan).

### 8.2 TOML fragment (the 4 new groups, appended at the end of the file in alphabetical order)

```toml
[molotov]
effectRadius = 4.0                 # D-58: landing sphere radius (blocks)
fireChance = 85.0                  # D-58: chance of fire per block (%), the rest places magma blocks
triggerExplosionPower = 0          # D-58: landing explosion power, 0 = trigger only

[wild_apple]
nutrition = 4                      # D-54: same as the golden apple
saturationModifier = 1.2           # D-54: same as the golden apple

[wild_hunter]
durationTicks = 700                # 35 seconds
killExtensionTicks = 160           # D-74: +8 seconds, no cap on the number of kills
killHealPoints = 10.0              # D-74: +10 health points
searchRadius = 5.0                 # D-64: 5 blocks
searchAngleDegrees = 30.0          # D-64: 30 degrees
dashTicks = 3                      # D-65: 3 ticks to cross
speedBonusPercent = 30.0           # +30% movement speed
attackSpeedBonusPercent = 30.0     # +30% attack speed
attackDamageBonusPercent = 40.0    # +40% attack damage
highlightRadius = 64.0             # D-62: max red silhouette distance (blocks)
loopSoundRange = 16.0              # D-76: looping sound audible range (blocks)

[zangief_cane]
triggerRadius = 1.5                # D-73: spherical 1.5 blocks
stageDelayTicks = 14               # D-69: levitation after 14 ticks
levitationLevel = 7                # D-69: level VII → amplifier 6
levitationDurationTicks = 20       # D-69: 1 second
fallTicks = 40                     # phase 2 target landing duration (ticks) = 2 seconds, per-tick servo
spinDegreesPerTick = 72.0          # D-70: 1440 degrees/second ÷ 20
```

**Why the new groups are appended in alphabetical order**: the group order of v0.0.1 follows the writing order of DEC, and the four new groups of v0.0.2 have no DEC line numbers to rest on, so appending them alphabetically (`molotov` < `wild_apple` < `wild_hunter` < `zangief_cane`) keeps every future version's new groups at the end without disturbing the diff of the existing toml (added by this document).

### 8.3 Equality of the key count and the language keys

| Assertion | Value | How to check |
|---|---|---|
| new `FarmstormConfig` keys | **22** | the row count of §8.1 |
| new `farmstorm.configuration.*` language keys | **22** | #18–#39 of §7.3, corresponding one by one to #1–#22 of §8.1 |
| the two are equal | `22 == 22` | `ModLangProvider` writes both files column by column from the same `ENTRIES` table, so the key sets are equal by construction |
| new total | **139** | `117 + 22` |
| new total of language keys | **261** | `222 + 39` (22 of them config) |

The mechanical comparison during implementation: the number of leaf nodes in `run/config/farmstorm-server.toml` must be **139**, and the number of keys in `zh_cn.json` carrying the `farmstorm.configuration.` prefix must also be **139**. If the two numbers differ, some keys were not generated.

### 8.4 Constants explicitly kept out of the config (reason given for each)

"One source per value" does not mean "every number goes into config". The following are **deliberately** left in code. **Reverse check**: every key that appears in the 22 rows of §8.1 (including `fallTicks`, `highlightRadius`, `loopSoundRange`, `spinDegreesPerTick`) is **not** in this table, and no constant of the same name may appear here either.

| Constant | Value | Reason for keeping it in code | Basis |
|---|---|---|---|
| `MAX_FALL_TICKS` | `100` ticks | backstop cap, **not** the target duration: the target duration is `zangief_cane.fallTicks` from the config (default 40 ticks), and this cap only backstops "the landing test failed (stuck in a block seam)". Safety valves staying out of config is the existing v0.0.1 convention | HC-13 family |
| white texture path / 18×18 size / red `(1.0, 0.15, 0.15, 0.45)` / the 32-per-frame cap | — | pure render parameters that change no gameplay outcome and are not "range"-type values. **The two distance-type values are already in config** (`wild_hunter.highlightRadius` #15, `wild_hunter.loopSoundRange` #16), and the client reads the same default values through the mirror constants of §4.4 | added by this document |
| client mirror constants such as `DEFAULT_SPIN_DEGREES_PER_TICK` / `DEFAULT_SEARCH_RADIUS` / `DEFAULT_SEARCH_ANGLE` / `DEFAULT_HIGHLIGHT_RADIUS` / `DEFAULT_LOOP_SOUND_RANGE` | the same values as the config defaults: 72.0 / 5.0 / 30.0 / 64.0 / 16.0 | the client cannot read the SERVER config (the degradation rule of §4.4); the keys behind these 5 mirror constants are all in §8.1 (#22 / #9 / #10 / #15 / #16), and the comments name them as requiring synchronization | added by this document |
| `strength(30.0F, 6.0F)` and `requiresCorrectToolForDrops()` of `compressed_hay_block` | — | D-57 has nailed down the three numbers and they are inherent block properties; making them tunable keys is a back door for balancing | D-57 |
| `THROW_COOLDOWN_TICKS` / `DEPLOY_COOLDOWN_TICKS` / `MAX_DEPLOYED_PER_*` | existing values | existing v0.0.1 safety valves, untouched | HC-13 |
| `LANDING_PROBE` (0.1 blocks) | `0.1` | a sampling epsilon, not a gameplay value (already explained in the source comment) | existing |
| volume / pitch of the trigger sound and the looping sound | `1.0F` / `1.0F` | vanilla defaults; changing them has no gameplay consequence. The looping sound's **audible range** is `wild_hunter.loopSoundRange` (#16), and is not here | added by this document |
---

## 9. Proof That Server Authority and the 0-Packet Constraint Hold

### 9.1 Criterion

D-50's criterion is one sentence: **the "server tells the client the state" needed by the three intrusive effects always rides a hidden `MobEffect`; presence of the effect + amplifier + remaining duration is the complete state; adding a `CustomPacketPayload` for this is forbidden.**

v0.0.1's C-6 is "0 custom network packets"; among the 13 new registry objects of v0.0.2 **not one belongs to the network layer** (no packet class appears in the 13 rows of §2.2), and that is the first layer of evidence. The second layer is this section: every piece of "server → client" information v0.0.2 needs is mapped one by one onto a vanilla packet.

### 9.2 Item-by-item mapping (19 entries, none needs a new packet)

| # | Information that must reach the client | Carrier / trigger point | Which vanilla packet | Why it suffices |
|---|---|---|---|---|
| 1 | the boolean "you are being piledriven" | server `addEffect(zangief_piledriver, …)` | `ClientboundUpdateMobEffectPacket` | the packet itself carries effect holder + amplifier + duration + flags (ambient/visible/showIcon) |
| 2 | which phase it is in now (0/1/2) | the amplifier field of the same | same as #1 | the amplifier is the phase number, no extra field needed |
| 3 | how many ticks the phase has left | the duration field of the same | same as #1 | the vanilla effect timer counts down by itself, and what the client reads is the authoritative remaining duration |
| 4 | the phase ends (landing / timeout) | server `removeEffect` | `ClientboundRemoveMobEffectPacket` | "the effect is gone" is "it is time to reset" |
| 5 | the downward velocity of phase 2 every tick (per-tick servo, `fallTicks` target duration) | server `ServerPlayer#setDeltaMovement(0, -h / n, 0)` every tick | `ClientboundSetEntityMotionPacket` (about one per tick inside phase 2, roughly 40 in 40 ticks; still a vanilla packet) | a `ServerPlayer` velocity change is broadcast by `ServerEntity` to all trackers (including the player itself); 40 velocity packets spread over 2 seconds is a bandwidth magnitude comparable to one explosion (`ClientboundExplodePacket` with a block list) |
| 6 | "you entered the Wild Hunter state" | server `addEffect(wild_hunter, …)` | `ClientboundUpdateMobEffectPacket` | same as #1; grayscale (D-61) and the red silhouette (D-62) both use it as their switch |
| 7 | the remaining duration of Wild Hunter (grows on a kill extension) | server re-`addEffect` with a longer instance | `ClientboundUpdateMobEffectPacket` | a duration change = one new effect packet |
| 8 | the three attribute bonuses (+30 % movement speed / +30 % attack speed / +40 % damage) | `MobEffect#addAttributeModifiers` | no packet needed | vanilla also runs that method on the client (`LivingEntity#onEffectAdded` is symmetric on both sides), so the client attribute table matches the server |
| 9 | the Zangief Cane trigger sound | `level.playSound(null, pos, …)` | `ClientboundSoundPacket` | position + event + volume + pitch, one-shot |
| 10 | the Wild Hunter looping sound | client-side hand-written `SoundInstance` (D-76) | **zero packets** | the lifetime of the sound == the lifetime of the effect, and the client can infer from #6 whether it should play |
| 11 | the fire / magma blocks at the Molotov landing point | `level.setBlock(target, state, UPDATE_ALL)` | `ClientboundBlockUpdatePacket` | one chunk-level update packet per changed cell (vanilla merges consecutive updates within a chunk automatically) |
| 12 | the visual of the Molotov explosion | `finalizeExplosion` of `FarmstormExplosion#detonate` | `ClientboundExplodePacket` + `ClientboundLevelParticlesPacket` | the two-piece vanilla explosion set; at power 0 the former carries only position and sound |
| 13 | the projectile entity appearing in the world | `level.addFreshEntity(projectile)` | `ClientboundAddEntityPacket` | the vanilla entity spawn flow |
| 14 | the trap block vanishing silently | `server.removeBlock(pos, false)` + `levelEvent(PARTICLES_DESTROY_BLOCK)` inside `be.consume()` | `ClientboundBlockUpdatePacket` + `ClientboundLevelEventPacket` | the latter is exactly vanilla's block-destroy particle event, so no particle packet has to be sent by hand |
| 15 | 10 health restored after a kill extension | `player.heal(10.0F)` | `ClientboundSetHealthPacket` | a server-side health change goes through vanilla synchronization |
| 16 | the three new advancements being granted | `PlayerAdvancements#award` | `ClientboundUpdateAdvancementsPacket` | the vanilla advancement system |
| 17 | the deployed block and its owner | `DeployedCropBlockEntity#getUpdatePacket` | `ClientboundBlockEntityDataPacket` | an existing mechanism (6 synchronized fields), v0.0.2 adds no field |
| 18 | camera flip / spin | the client reads the amplifier of #2 + local tick accumulation | **zero packets** | render layer only; D-70 states explicitly "no reliance on per-tick server synchronization" |
| 19 | input lock | the client reads the "present or not" of #1 | **zero packets** | pure client behavior |

Of the 19 entries, 4 are zero-packet (the attribute bonuses of #8, the looping sound of #10, the camera of #18, the input lock of #19), and the remaining 15 all land on **12 kinds** of vanilla packets: `ClientboundUpdateMobEffectPacket`, `ClientboundRemoveMobEffectPacket`, `ClientboundSetEntityMotionPacket`, `ClientboundSoundPacket`, `ClientboundBlockUpdatePacket`, `ClientboundLevelEventPacket`, `ClientboundExplodePacket` / `ClientboundLevelParticlesPacket`, `ClientboundAddEntityPacket`, `ClientboundSetHealthPacket`, `ClientboundUpdateAdvancementsPacket`, `ClientboundBlockEntityDataPacket`.

### 9.3 Why no `CustomPacketPayload` is added

Proof by contradiction: to add a packet there must first be a state that "a vanilla packet cannot express". Eliminating them one by one:

| Candidate reason | Why it does not hold |
|---|---|
| "the grayscale switch" | it is whether `wild_hunter` is present, and the vanilla effect packet already delivers it |
| "the target list of the red silhouette" | no list has to be sent: the client itself walks the `LivingEntity`s in view, and the criteria (distance / line of sight / team) are all locally computable on the client; besides, D-62 only asks for "what is visible, shown to me" |
| "the phase number" | it is the amplifier (#2) |
| "the dash target" | the client computes it itself (the server re-checks with the same algorithm), and no step of BRIEF §7.6's flow needs the server to tell the client who the target is |
| "the 16-block audible range" | `attenuation_distance` of `sounds.json` + `SoundInstance`, both local to the client |
| "the exact instant of a phase switch" | every phase gets a new effect packet, the packet itself carries a duration, and the client can derive the switch by counting the duration down |

The cost (compared with v0.0.1's choice, already listed once in `02-TECH-SPEC.md` §5.5): adding a packet means writing `CustomPacketPayload` + a `PayloadType` registration + handlers on both ends + version compatibility, plus a separate error-correction design for "the client and server phase numbers disagree" — and the entire benefit is saving the parsing of a few vanilla packets. With D-50 already ruling it out, the cost-benefit of that road is negative.

**The only thing sacrificed**: the client cannot display a precise "how many ticks are left in this phase" countdown ring (a purely decorative requirement). It is listed as deferred and not done.

### 9.4 New server responsibilities of v0.0.2 (the increment of the server-authority list)

| Responsibility | Where | Why it must be on the server |
|---|---|---|
| the fire / magma block writes of Molotov | `MolotovProjectile#scatter` | world writes can only be done by the server (HC-03) |
| the block writes of Compressed Puffed Wheat | `CompressedPuffedWheatProjectile#fill` | same as above |
| the trigger test of Zangief Cane | `ZangiefCaneTrap#findTrigger` | the ally test must read the team (the client cannot see complete team information; comment on `DeployedCropBlockEntity#isAlly`) |
| phase advancement and levitation application | `ZangiefPiledriverController` | time and state must have a single authority |
| target re-check / damage / knockback of Wild Hunter | `WildHunterCombat` | damage can only be settled by the server |
| kill extension and healing | `WildHunterCombat#onKill` | health and effect duration can only be changed by the server |
| granting the three new advancements | `FarmstormAdvancements#award` | advancement evaluation is server-side (the existing convention of `ModAdvancementProvider#AWARD_REQUIRED`) |

### 9.5 The boundaries of client prediction (the two new ones in v0.0.2)

| Prediction | Boundary | Consequence of crossing it | Backstop |
|---|---|---|---|
| dash displacement (§4.5) | changes only **its own** velocity, not another player's position and not damage | a wrong prediction ⇒ the player dashes but deals no damage, or deals damage without dashing (the latter is impossible, damage is computed by the server alone) | server re-check; do nothing if illegal |
| camera angles (§4.4) | changes only **render-layer** angles, not writing back to `getYRot()` / `getXRot()` | writing back ⇒ the player's real facing is tampered with and walls can be punched through | iron rule: only call `event.setXxx`, never touch `player` |

---

## 10. Risk Register (RT-10 ~ RT-16)

The numbering continues from RT-09 of `02-TECH-SPEC.md` §9. **Seven entries, consecutively numbered, none added, removed or merged** (precondition ② of BRIEF §7.8). The "Risk" and "Primary mitigation" columns are copied from BRIEF §7.8; the other three columns are filled in here.

| ID | Risk | Trigger condition | Primary mitigation | Backstop | How to verify during implementation |
|---|---|---|---|---|---|
| **RT-10** | the mount point of the full-screen grayscale post-process does not hold | switching the render target in `RenderGuiEvent.Post` fights other mods' GUI post-processing: a fully black screen, only half the screen grayed, another mod's HUD gone; or the `ShaderInstance` registered in `RegisterShadersEvent` fails to compile | split into two passes, world + GUI (the AFTER stage of `RenderLevelStageEvent` + `RenderGuiEvent.Post`); if that still fails, fade the world only and register the degradation in the Release notes | world only + state "the GUI is not grayed" in the Release notes; if even the world pass does not hold, D-61 degrades to "fade the world only" and is registered as a known defect | ① after entering the effect, check whether the whole screen is gray (the title bar and the inventory must be gray too); ② install another mod that does GUI post-processing and check whether the two break each other; ③ check whether `[Render thread/ERROR]` lines in `latest.log` show GL or shader compile errors; ④ record the frame-time difference before and after (F3) |
| **RT-11** | silhouette redraw recursion or frame drops | calling `EntityRenderDispatcher#render` by mistake inside `RenderLivingEvent`, causing recursion; or too many entities on screen blowing up the frame time; or `hasLineOfSight` being unavailable on the client | use `getModel().renderToBuffer` rather than a dispatcher redraw; draw only within ≤64 blocks and with line of sight | at most 32 per frame (nearest first); if that still fails, draw only the subset "distance ≤ 16 blocks" and state it in the Release notes | ① 30 creatures on screen with the effect on, check for a freeze or a frame drop > 3 ms; ② stand once in front of and once behind a wall and confirm no silhouette behind the wall; ③ confirm no silhouette beyond 65 blocks; ④ check the log for `StackOverflowError` |
| **RT-12** | the driver of the dash displacement is misjudged, the player does not move or is pulled back | `setDeltaMovement` on the server does not produce a client displacement on `ServerPlayer`; or the client-predicted displacement is judged "moving too fast" by the server's movement validation and pulled back | client prediction + server decision; the alternative is a per-tick teleport | switch to the alternative: the server `teleportTo` ≤ 1 block per tick with collision detection; on switching, append a decision record to §4.5 | ① print a `setDeltaMovement` log line and see whether the client is displaced; ② while dashing, check `latest.log` for `moved too quickly!` / `moved wrongly!`; ③ dash at a wall and confirm the player stops in front of it without phasing through; ④ when the target is fully occupied by blocks, confirm no attraction happens |
| **RT-13** | input interception has a gap and some key still penetrates | the relative order of `InputEvent.Key` and `KeyMapping` lets 1–9 through; or some GUI opening path does not go through `ScreenEvent.Opening` | all five events; during implementation test key by key once and keep the record | re-hook the leaking path to an earlier hook (e.g. add `ScreenEvent.KeyPressed`); if it still leaks, register "this key cannot be locked" in the Release notes | test key by key and fill the table: W/A/S/D/space/Shift/left button/right button/1–9/wheel up/wheel down/E/T/`/`/ESC, 21 items in total, recording "blocked / penetrated" for each; ESC must stay usable |
| **RT-14** | ffmpeg is not on PATH, or another machine / build has no ffmpeg with libvorbis, so the audio cannot be produced | `Get-Command ffmpeg` the script finds no usable ffmpeg — on this machine the 7.1 essentials build at `D:\dsh\default\sf6_audio\tools\ffmpeg.exe` is **usable**, while Trae CN's 6.1.1 has **no libvorbis** and is not; or the `attenuation_distance` of `sounds.json` does not mean "audible distance" on 1.21.1 | the conversion script errors out and exits when ffmpeg is missing or has no libvorbis; the audio task is scheduled first in the resource phase, so it fails early and is found early; the tool is in place and measured working (`tools/convert_media.ps1`) | pass the correct absolute `-Ffmpeg` path; on another machine, follow the guidance the script prints and use a build carrying libvorbis | ① without `-Ffmpeg` and with nothing on PATH, `-CheckOnly` must print the installation guidance and exit 1, while a usable path must report the environment as usable and exit 0; ② after a real run, check with `ffprobe` that channels = 1 and bitrate ≈ 64 kbps; ③ listen once each at 8 / 16 / 17 / 32 blocks in game; ④ use `jar tf` to confirm both `.ogg` files are inside the jar |
| **RT-15** | the hidden MobEffect is disturbed by other mods' effect-clearing logic | a third-party mod's "clear all negative effects" (`/effect clear`, a potion, a command) strips `zangief_piledriver` early while the client stays at phase 2: the screen stays flipped forever and the keys stay locked forever | the client self-checks every tick whether "the phase effect is still there" and resets itself if not | the symmetric self-check on the server: `ACTIVE` has a record but `getEffect` is empty ⇒ `finish` + `warn`, no re-apply | ① `/effect give @s farmstorm:zangief_piledriver 10 2 false` and watch the screen flip + input lock + spin; ② after `/effect clear @s` everything must reset within 100 ms, with the log showing "state reset (effect cleared externally)"; ③ log on every phase switch and check whether the client reads `null` (the 1-tick gap of the hard switch) |
| **RT-16** | full-screen grayscale is incompatible with shader mods (Oculus/Iris etc.) | with a shader mod installed, the grayscale pass conflicts with the shader's framebuffer chain: a fully black screen, shaders stop working, or grayscale only affects one layer before the final composite | registered in the documentation as a known incompatibility; no runtime detection | list Oculus / Iris in the "known incompatibilities" section of the Release notes and the README; no version probing, no automatic degradation | ① install Iris + any shader pack, enter the effect and check whether the screen goes black or the shaders break; ② with no shader pack installed, go back to step ① of RT-10 and confirm it is normal; ③ register it once in `12-TECH` and in this entry, and never re-check |

**The list of seven IDs**: RT-10, RT-11, RT-12, RT-13, RT-14, RT-15, RT-16 (consecutive, no gap, no merge).

### 10.1 Cross-reference between risks and RT-10…RT-16 (for looking them up by ID during acceptance)

| RT | Where this document references it |
|---|---|
| RT-10 | §4.1, `post_effect/grayscale.json` in §6, §10 |
| RT-11 | §4.2, §6.6 (white texture), §3.13 (`WildHunterOutline`) |
| RT-12 | §4.5, §3.13 (`WildHunterDashClient`), §9.5 |
| RT-13 | §4.3, §3.13 (`InputLockHandler`) |
| RT-14 | §4.6, §6.1–§6.4, §7.2 (#9/#10) |
| RT-15 | §3.6, §3.11, §5.2, §5.6 |
| RT-16 | §4.1 (grayscale and shaders), §10 |

---

## 11. Acceptance Self-Check Table

Every item can be decided by one **concrete action**; "looks fine" is not accepted. The first 6 items are the startup log (once on the server and once on the client), the last 8 are manual tests or commands.

| # | Self-check item | Check action | Pass criterion |
|---|---|---|---|
| 1 | the types and categories of the 19 crop profiles are not scrambled | start either instance and read the first screen of the server log | the line `自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK` appears (BRIEF §4, verbatim) |
| 2 | all 77 registry objects are present | in the same startup, read the self-check line for the new registry objects | `自检 注册对象 77 个：items 27 / blocks 22 / block_entities 6 / entity_types 11 / mob_effects 2 / sound_events 2 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 2 / data_components 1 / menu_types 1 → OK` |
| 3 | the Lucky Berry pool is 16 entries, not 17 or 18 | in the same startup, read the pool self-check line | `自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK` |
| 4 | all 11 entities have a renderer (the existing sentinel of FIX-1) | client startup log | `自检 实体渲染器 11/11 → OK`, and the line right after it lists the 11 entity IDs (including `farmstorm:molotov` and `farmstorm:compressed_puffed_wheat`) |
| 5 | the grayscale shader compiles | client startup log | no `Shader … could not be loaded` / `GL_INVALID` lines; after entering the effect the F3 frame rate shows no cliff (a drop < 3 ms compared with not entering the effect) |
| 6 | both sound events are picked up by the resource manager | after the client starts, run `/playsound farmstorm:wild_hunter_loop master @s ~ ~ ~ 1 1` and the same for `zangief_piledriver` once each | both commands succeed and both sounds are audible; if either is missing the command reports `Unknown sound event` |
| 7 | the landing semantics of Molotov | throw one on flat ground, count the fire and magma blocks and compare with D-58 | inside the 4-block sphere, fire/magma blocks have been added only above blocks whose top surface touches air; not one original block is lost; no explosion damage, no block destruction; fire : magma ≈ 85 : 15 (a rough check with 20 throws) |
| 8 | Compressed Puffed Wheat fully carries over Puffed Wheat | throw one Puffed Wheat and one Compressed Puffed Wheat at the same position, recording each | the cube edge length matches (dry 3 / wet 5), the landing block matches, and the cells blocked by 3.6e6 blocks match; the only difference is the block written (hay block vs compressed hay block) |
| 9 | the resistance boundary of the compressed hay block (D-57) | place one with `/setblock` and a cobblestone block next to it; explode each with Melon Grenade, Ubi Server and vanilla TNT | Melon Grenade cannot break the compressed hay block but breaks the cobblestone; Ubi Server breaks both; **vanilla TNT also breaks the compressed hay block** (vanilla `Explosion` subtracts `(resistance + 0.3) * 0.3` per cell, and 6.0 resistance cannot stop a cobblestone-tier block — cobblestone itself is destroyed by TNT) |
| 10 | the three-phase timing and landing duration of Zangief Cane | stand inside 1.5 blocks, time it with a stopwatch and read the log; test once on flat ground, once on a two-block platform and once on a four-block platform | flipped immediately; levitation starts after 14 ticks (level VII, 20 ticks); falling + spinning starts after 34 ticks (72.0 degrees/tick = 1440 degrees/second); **the landing time measured at all three heights falls within `fallTicks` (default 40 ticks) ± 2 ticks** (this is the dividing line between the per-tick servo and the constant-speed option); within 100 ms of landing the screen and input are fully reset; the log shows the four lines `桑吉尔蔗 相位 0→1→2→结束` and `落地耗时 {N} 刻` |
| 11 | Zangief Cane only affects non-ally players | teammate A, hostile player B, creative-mode player C and spectator D each step on one | only B is piledriven; A (same team) does not trigger; C and D do not trigger; after triggering, **the block disappears and drops no item at all** |
| 12 | the input lock leaks nothing but ESC | test key by key with the 21 items of RT-13 in §10 and fill the table | everything except ESC is blocked; ESC opens the pause menu; all keys are restored at once when the effect disappears |
| 13 | the three conditions of the red silhouette | stand within 5 blocks / beyond 65 blocks; in front of / behind a wall; a teammate / a hostile player | only creatures that are "≤64 blocks + unobstructed + non-ally" get a red silhouette; self and teammates are not drawn; nothing is drawn beyond 65 blocks |
| 14 | the Wild Hunter dash goes through the server re-check | with the effect on, attack a creature within 5 blocks and 30° once; then attack one outside 30° once | the former is attracted and takes damage and knockback; the latter is a normal attack with no attraction; `latest.log` shows no `moved too quickly!` (if it appears, tune `dashTicks` per RT-12) |
| 15 | kill extension has no cap | with the effect on, kill 6 creatures in a row, reading the remaining effect duration each time | every "expiry moment" moves +8 seconds and health +10 points; it is still active after 6 kills (no cap on the number of kills) |
| 16 | 139 config keys, equal on both sides | count the leaf nodes of `run/config/farmstorm-server.toml`; count the keys with the `farmstorm.configuration.` prefix in `zh_cn.json` | both numbers are **139** (117 + 22); the 22 new key names match §8.1 character for character, and match the sole inventory of BRIEF §8 column by column |
| 17 | 298 resource files, categories consistent | `Get-ChildItem -Recurse -File src\main\resources, src\generated\resources` minus the 11 under `.cache` | total **298**; JSON **193**, PNG **99**, OGG **2**, GLSL **2**, TOML 1, `pack.mcmeta` 1 |
| 18 | 261 language keys, Chinese and English equal | after `ModLangProvider` runs, compare the key sets of the two `.json` files | both **261** keys (222 + 39); `set(zh) == set(en)` is `True`; among them `tooltip.farmstorm.*` is still **16** entries (nothing wrongly added) |
| 19 | the three new advancements unlock | respectively: throw a Molotov / land a Zangief Cane on a player / obtain the Wild Hunter effect | each advancement pops a toast once; `/advancement` shows `farmstorm:first_molotov`, `farmstorm:zangief_hit`, `farmstorm:first_wild_hunter` |
| 20 | the shape and count of the 5 new recipes | craft the converter at the crafting table (center crafting table + eight crops); place 4 Puffed Wheat shapeless; convert molotov / zangief_cane / wild_apple once each in the converter | the converter yields 1; 4 Puffed Wheat yield 1 Compressed Puffed Wheat; each of the three conversions costs 3 experience points and works in both directions (golden apple ⇄ Wild Apple, once each way) |
| 21 | 0 custom network packets | `grep -r "CustomPacketPayload" src/main/java` | **0 matches** (v0.0.1 was also 0; v0.0.2 must not introduce the first one) |
| 22 | the client does not crash on a dedicated server | start the server with `启动服务器.cmd` and connect with the client | server startup shows no `NoClassDefFoundError` (all 6 client classes are isolated by `Dist.CLIENT`); after connecting, the effect-related features work |

**22 entries, all decidable with one concrete action.** Of these, #1–#6 are startup log lines (the most automatable), #21/#22 are structural assertions (a single command can disprove them), and the rest are manual tests.

---

## Translator's notes

1. §6.7 step 3 says the hand-written resources are "4 files", while §7.2 marks 5 rows as hand-written (#11–#15: `sounds.json`, `grayscale.json`, `grayscale.vsh`, `grayscale.fsh`, `post_effect/grayscale.json`).
2. §11's introduction says "the first 6 items are the startup log … the last 8 are manual tests or commands" (6 + 8 = 14), while the table has 22 rows and the closing line counts 22.
3. The header block states "the whole text uses declarative sentences in Simplified Chinese"; in the source that sentence describes the original language style, and it is kept verbatim here, where it is no longer literally true of the English text.
