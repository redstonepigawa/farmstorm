# 11 · v0.0.2 Design Delta

| Item | Value |
|---|---|
| Document | 11-DESIGN-0.0.2 |
| Version | v0.0.2 |
| Status | Design final |
| Upstream | `plan0.0.2.txt` (the author's original idea), `.work/v002-BRIEF.md` (the single source of truth for v0.0.2), `docs/zh/01-DESIGN.md` (v0.0.1 design document, read-only) |
| Downstream | `12-TECH-0.0.2.md`, `13-TASKS-0.0.2.md` |
| Language | Body text in Chinese; code identifiers, registry names and config keys always in English |

---

## 0. Reading conventions

### 0.1 Relationship to 01-DESIGN.md

This document is a **delta**, not a rewrite. The v0.0.1 design (`01-DESIGN.md`) was frozen by D-49, and this document redefines none of its existing rules; wherever it says 'carried over', it points back to the corresponding subsection of `01-DESIGN.md`, and the two are read side by side.

v0.0.2 does exactly two things: ① give the converter the single crafting recipe it lacks (§2); ② append the 7 new features of §3–§8. The existing 21 conversion recipes, 12 advancements, every field of the 15 `CropProfile` entries and the default values of the 117 existing config keys stay untouched.

### 0.2 Markup

| Markup | Meaning |
|---|---|
| 【D-xx】 | The conclusion comes directly from the decision table in `.work/v002-BRIEF.md` §2 (D-49 ` D-78) |
| 【P-x】 | The conclusion comes directly from item x of the appended requirements in `plan0.0.2.txt` |
| 【本文补充】 | A value or convention that neither BRIEF nor plan gives, derived by this document from existing convention or vanilla behaviour. Implementation follows it as written; no second version is established anywhere else |

Every number has only two legitimate sources: it is written in BRIEF / plan, or it is marked 【本文补充】. A number that is neither is a document defect.

### 0.3 Units and rounding

1. Time: seconds / game ticks (tick), 1 second = 20 ticks.
2. Length: blocks.
3. Health and damage: points. Vanilla 2 points = half a heart, so 'restore 10 points of health' = 5 hearts.
4. Level and amplifier: when level N is stored, `amplifier = N − 1` (`01-DESIGN.md` §0 clause 3).
5. Angle: degrees (°). Rotational speed is written in degrees/tick.
6. Percentage: always an integer or one decimal carrying `%`, with no implicit rounding.

### 0.4 Distance measurement

Carried over from `01-DESIGN.md` §3.7 clause 4: 'radius N blocks' = Euclidean radius; 'Chebyshev distance N' = square neighbourhood. New checks default to Chebyshev; wherever this document switches to Euclidean (effect radius 4 in §5, trigger radius 1.5 in §6, target-acquisition radius 5 in §7), the reason is stated in that section.

### 0.5 Authority and synchronisation

1. **Server-authoritative**: landing settlement, block writes, effect application, damage, knockback and state transitions all happen on the server (D-23 wording unchanged).
2. **0 custom packets** (C-6, D-50): the 'server tells the client the state' that the three intrusive effects (greyscale, red silhouette, input lock) need always rides a hidden `MobEffect`, expressing the complete state through 'effect present + amplifier + remaining duration', and synchronises free of charge over the vanilla `ClientboundUpdateMobEffectPacket`. Adding a `CustomPacketPayload` for this is prohibited.
3. **The render layer never writes back**: every camera angle (§6.8) is changed on the render layer only, never written to `player.getYRot()` / `player.getXRot()`.

---

## 1. Positioning and boundaries of v0.0.2

### 1.1 One-line positioning

v0.0.2 is a pure delta on top of the frozen v0.0.1: it adds the single crafting recipe the converter lacks, then 4 `CropProfile` entries, 1 high-hardness block, **2 status effects** (1 visible `farmstorm:wild_hunter`, responsible for rewriting 'melee' as 'lock-on + dash'; 1 hidden `farmstorm:zangief_piledriver`, serving only as the phase-synchronisation channel of the Zangief Cane), and 3 intrusive client presentations (full-screen greyscale, red silhouette, input lock).

### 1.2 The 7 new features and their sections

| # | plan item | Section of this document | Delivered form |
|---|---|---|---|
| 1 | Produce Converter crafting recipe 【P-1】 | §2 | 1 shaped 3×3 crafting recipe + 1 item tag `farmstorm:converter_fuel` |
| 2 | Molotov 【P-2】 | §5 | 1 `CropProfile` + 1 projectile entity `farmstorm:molotov` |
| 3 | Zangief Cane 【P-3】 | §6 | 1 `CropProfile` + 1 deployed block + 1 hidden effect + 1 sound event |
| 4 | Compressed Hay Block 【P-4】 | §3 | 1 plain block + 1 `BlockItem` |
| 5 | Compressed Puffed Wheat 【P-5】 | §4 | 1 `CropProfile` + 1 projectile entity + 1 shapeless crafting recipe |
| 6 | Wild Hunter 【P-6】 | §7 | 1 visible status effect + 1 looping sound + 1 post_effect |
| 7 | Wild Apple 【P-7】 | §8 | 1 `CropProfile` + 1 bidirectional conversion recipe |

### 1.3 Number conventions

The table below copies `.work/v002-BRIEF.md` §4 verbatim; do not compute it a second time and report a different number.

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
| Total registered objects | 64 | **77** |
| items | 22 | **27** |
| blocks | 20 | **22** |
| block_entities | 6 | **6** |
| entity_types | 9 | **11** |
| mob_effects | 0 | **2** (1 visible + 1 hidden) |
| sound_events | 0 | **2** |
| Conversion recipes | 21 | **24** |
| Crafting recipes | 0 | **2** |
| Advancements | 12 | **15** |
| Lucky Berry pool | 14 | **16** |
| config keys | 117 (measured) | **139** (= 117 + 22, the 22-entry sole list of BRIEF §8) |

The startup self-check text is updated accordingly to:

    自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK

**Two easy mistakes** (copied from BRIEF; do not re-derive them during implementation):

1. Demolition is still 4. Molotov's categories are Damage and Construction and it carries **no Demolition tag**, so it breaks no block at all (§5.10).
2. The Lucky Berry pool is 16, not 17: from the 19 profiles, exclude `lucky_berry`, `wild_apple` (D-55) **and** `compressed_puffed_wheat` (D-56).

### 1.4 Non-goals for v0.0.2

1. No planting form for the three new crops (D-51): `cropBlock` and `seedItem` of `molotov` / `zangief_cane` / `wild_apple` are all `null`, and neither a crop block nor a seed is registered.
2. Compressed Puffed Wheat likewise has no crop block, no seed and no `kind` (D-56).
3. No new packet (D-50), no Mixin (D-40).
4. No new damage type: Molotov deals 0 damage, so the damage_types delta in BRIEF §3.2 is 0.
5. No runtime detection for shader compatibility (RT-16).
6. No change to the fields of the existing 15 `CropProfile` entries, to existing config defaults, to the 21 conversion recipes or to the 12 existing advancements (BRIEF §3.4).

### 1.5 Three seams with v0.0.1

| Seam | Boundary | Basis |
|---|---|---|
| Registry side | Only the 19 existing files listed in BRIEF §3.3 may be modified; everything else stays untouched | BRIEF §3.3 |
| Data side | Reuse the existing `ConverterRecipe`; no new recipe type / serializer; the three new conversion recipes are isomorphic to the existing 21 | BRIEF §3.2, D-54 |
| Sync side | All state synchronisation rides a hidden `MobEffect` or a vanilla packet | D-50 |

---

## 2. Crafting recipe of the Produce Converter

### 2.1 One-line positioning

Give the v0.0.1 converter — which has no recipe at all and can only be obtained in creative mode — its **first** crafting recipe, so that the 'farm → convert' chain actually runs in survival mode.

### 2.2 Type and category

| Item | Value |
|---|---|
| Object | A work block (`farmstorm:converter`), not a crop |
| `CropProfile`? | No. It does not enter the 19-profile table and carries no `CropCategory` |
| Recipe type | Vanilla shaped crafting `minecraft:crafting`, 3×3 ring |
| Classification | A 'recipe delta', not a 'registered object delta': the blocks / items counts are unchanged |

### 2.3 Acquisition

The only route is crafting at a crafting table (D-52). The result is a **block item**; only once placed is it the v0.0.1 three-slot GUI work block (`01-DESIGN.md` §3.1.2).

### 2.4 Numbers

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Recipe grid | 3×3, shaped | cells | D-52 |
| Centre cell | `minecraft:crafting_table` × 1 | item | D-52 |
| Eight outer cells | Item tag `farmstorm:converter_fuel`, 1 per cell | cell | D-52 |
| Output | `farmstorm:converter` × 1 | item | D-52 |
| Experience cost | Crafting deducts no experience | points | Neither 【P-1】 nor D-52 gives a crafting cost; the 3 points per conversion in D-12 apply only to 'conversion inside the converter' and have nothing to do with crafting |
| Fuel tag entries | 36 (18 vanilla + 18 improved) | kinds | 【本文补充】, the split is given in §2.5 |

### 2.5 Contents of the fuel tag `farmstorm:converter_fuel`

BRIEF gives only the rule 'vanilla crops + all improved crops of this mod (every item among the 19 profiles that can be treated as a crop)', not an item-by-item list. This document turns the rule into one executable statement:

> **Tag = the 18 improved crop items among the 19 profiles excluding `compressed_puffed_wheat`, plus their 18 vanilla reference items.**

The basis for excluding `compressed_puffed_wheat` is D-56: it is a 'tier-2 composite', not an 'improved crop'. The vanilla side likewise takes 18 items 'one to one': the 15 vanilla references of `01-DESIGN.md` §5, plus the vanilla references of the three entries added in §5–§8 (crimson fungus, sugar cane, golden apple).

**Vanilla side (18 items)**

| Group | Items |
|---|---|
| The 15 vanilla references of `01-DESIGN.md` §5 | `minecraft:wheat`, `minecraft:carrot`, `minecraft:potato`, `minecraft:poisonous_potato`, `minecraft:beetroot`, `minecraft:melon_slice`, `minecraft:melon`, `minecraft:pumpkin`, `minecraft:torchflower`, `minecraft:pitcher_plant`, `minecraft:cactus`, `minecraft:bamboo`, `minecraft:red_mushroom`, `minecraft:brown_mushroom`, `minecraft:sweet_berries` |
| Vanilla references of the three new entries | `minecraft:crimson_fungus` (Molotov, 【P-2】), `minecraft:sugar_cane` (Zangief Cane, 【P-3】), `minecraft:golden_apple` (Wild Apple, D-54) |

**Improved side (18 items)**

| Group | Items |
|---|---|
| The 15 profiles of v0.0.1 | `puffed_wheat`, `blast_carrot`, `ubi_server`, `toxic_ubi_server`, `power_beet`, `melon_slice_bomb`, `melon_grenade`, `pumpkin_turret`, `awakened_torchflower`, `awakened_pitcher_plant`, `fang_trap_cactus`, `bamboo_firecracker`, `red_mushroom_puff`, `brown_mushroom_puff`, `lucky_berry` |
| The 3 entries added by v0.0.2 | `molotov`, `zangief_cane`, `wild_apple` |

Every item on the improved side carries the `farmstorm:` prefix. **Three classes do not enter the tag** (【本文补充】, reason given per class): ① seed items (such as `puffed_wheat_seeds`), they are seeds, not crops; ② `compressed_puffed_wheat` (D-56); ③ `compressed_hay_block`, it is a block, not a crop.

### 2.6 Behaviour specification

1. Put `minecraft:crafting_table` in the centre and one item from the fuel tag in each of the eight cells; the output is 1 converter.
2. The eight cells **may be mixed**: they need not be the same crop, as long as every cell matches the `farmstorm:converter_fuel` tag (【本文补充】: D-52 only says 'the eight surrounding cells take this tag'; each cell is judged independently, with no 'all eight identical' constraint added).
3. The centre must be a vanilla crafting table, not the converter itself; putting a converter into one of the eight cells does not work (it is not in the tag).
4. Crafting consumes no experience and no Nether Star; the Nether Star is consumed only by the converter's forward conversion (D-12, `01-DESIGN.md` §3.1.2).
5. Crafting and conversion are two different things: crafting belongs to `minecraft:crafting`, conversion settles inside the GUI under `farmstorm:conversion`. Adding this recipe **touches** no existing conversion recipe.

### 2.7 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| A converter already placed in creative mode in a v0.0.1 save | Works normally, with behaviour identical to a newly crafted one; this recipe changes no block field |
| Another data pack defines a recipe of the same shape | Vanilla crafting tables resolve by first match; this mod does not intervene |
| The fuel tag is emptied by a data pack | The recipe becomes unreachable and the converter cannot be obtained in survival. That is a data-pack-level consequence; this mod changes no recipe logic and adds no fallback |
| Burning a new crop as fuel | Legitimate. `wild_apple` and others are both fuel and conversion output; mistaking one for fuel is the player's own loss, and no interception is added |
| Crafting output stacking | The output is 1, with no extra experience drop and no advancement triggered |

---

## 3. Compressed Hay Block

### 3.1 One-line positioning

A building block that is too hard to blow up and can only be chipped away slowly with a diamond pickaxe, existing as the landing product of Compressed Puffed Wheat.

### 3.2 Type and category

| Item | Value |
|---|---|
| Registry name | `farmstorm:compressed_hay_block` |
| Block superclass | Plain block (`Block`), not `DeployedCropBlock` |
| `CropProfile`? | No. It is a block and does not enter the 19-profile table |
| Category | Carries no `CropCategory`. 【本文补充】: the Construction criterion requires 'the effect writes **vanilla** blocks that have a real use' (`01-DESIGN.md` §2.3.5); this block belongs to this mod, so it may only be mentioned as the carrier of a Construction product and is not itself classified |
| Item form | `BlockItem` `farmstorm:compressed_hay_block` |

### 3.3 Acquisition

| Route | Description | Basis |
|---|---|---|
| Compressed Puffed Wheat landing | Writes the whole block volume inside the landing range as this block; the main mass-production route | D-77 |
| Mining it back | Requires a diamond pickaxe or better, and drops only with the correct tool (see §3.4) | Tool tags of BRIEF §3.2 |
| Crafting | **None**. The recipe delta of BRIEF §3.2 has only 5 entries (1 converter crafting + 1 compressed puffed wheat + 3 conversions) and does not include this block | BRIEF §3.2 |

### 3.4 Numbers

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Hardness | 30.0 | first field of vanilla `strength` | D-57 |
| Blast resistance | 6.0 | second field of vanilla `strength` | D-57 |
| Drop condition | `requiresCorrectToolForDrops()` is true, i.e. the correct tool is mandatory | — | D-57 |
| Mining tool tag | `minecraft:mineable/pickaxe` | — | BRIEF §3.2 |
| Minimum tool tier | `minecraft:needs_diamond_tool` (diamond pickaxe or better) | — | BRIEF §3.2 |
| Orientation | Same texture on all six faces, not rotatable (no pillar / axis) | — | D-57 |
| Texture | Greyscale version of the vanilla hay block side | — | D-57 |
| Item stack | 64 | items | 【本文补充】: a block item keeps the vanilla 64 limit and is not included in `stack.throwable` |
| Drops when broken | 1 of the block itself | items | 【本文补充】: vanilla block-breaking rule, no extra loot-table field |

**These two numbers are copied from a pair measured on ancient debris; they are not balanced values** (the measurement basis of D-57):

| Vanilla block | `strength` as measured | Note |
|---|---|---|
| `minecraft:ancient_debris` | 30.0 / 1200.0 | This block takes its **hardness** 30.0, not its 1200.0 resistance |
| `minecraft:hay_block` | 0.5 | The no-argument `strength(0.5F)` manages hardness and resistance together; this block does not use its resistance |
| `minecraft:cobblestone` | 2.0 / 6.0 | This block takes its **resistance** 6.0 — this is the very 6.0 of 'cobblestone 6.0' in D-19 (`Blocks.COBBLESTONE` is `strength(2.0F, 6.0F)`) |

One-line alignment: **30.0 comes from the hardness of ancient debris, 6.0 comes from the blast resistance of cobblestone**, and the two together make the pair D-57 records; 'the things the 6.0 resistance does not stop' mentioned in §3.5 and §11.2 all refer to this cobblestone reading.

### 3.5 Behaviour specification

**Blast-resistance matrix** — the one table this section most needs to make clear. The predicate is `resistance < maxResistance` (BRIEF §3.4 forbids changing it), so any 'equal' counts as **cannot be broken**:

| Destruction source | Effective threshold / rule | For this block | Basis |
|---|---|---|---|
| Melon Grenade | `BlastRule.grenade()`, threshold 6.0 | **Cannot be broken** (6.0 is not less than 6.0) | D-57, D-78 |
| Melon Slice Bomb | `BlastRule.sliceBomb()`, threshold 6.0 | **Cannot be broken** (same as above) | D-57, D-78 |
| Ubi Server | `BlastRule.UNBREAKABLE_SKIP`, floor 3.6e6 | **Can be broken** | D-78 |
| Toxic Ubi Server | `BlastRule.VANILLA`, floor 3.6e6 | **Can be broken** | D-78 |
| Vanilla explosion (incl. TNT) | The per-cell accumulation formula of vanilla `Explosion` | **Can be broken**: per cell `f -= (resistance + 0.3) * 0.3`, `f > 0` breaks it; 6.0 resistance does not stop cobblestone-grade blocks | D-57 |
| Player mining | Diamond pickaxe or better | Broken normally, drops the block itself | BRIEF §3.2 |

Everything else is the default behaviour of any plain block (pushable by pistons, pushable by fluids, participates in light occlusion, drops per vanilla rules when destroyed by an explosion).

### 3.6 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| Covered by another shot of Compressed Puffed Wheat | Idempotent: the block written is still this block, with no extra phenomenon |
| Covered by Puffed Wheat | Becomes `minecraft:hay_block`. The write outranks blast resistance (row 4 of the priority table in `01-DESIGN.md` §7.11); resistance does not take part |
| Pushed by a piston | Pushable. It is not a deployed block, so there is no owner or attachment coordinate to maintain |
| Wrong tool (wooden pickaxe / bare hand) | Can be mined but **does not drop**, and the time is computed at hardness 30.0, extremely long |
| Creative mode | Breaks instantly, unconstrained by hardness and tool tags (vanilla rule) |
| `mobGriefing=false` | Explosions destroy no block at all (`01-DESIGN.md` §7.18); this block is safer still |
| This block already inside the landing range of Compressed Puffed Wheat | Allowed; the result is that the same region is confirmed to the same block again and again, with no merging or de-duplication |

## 4. Compressed Puffed Wheat

### 4.1 One-line positioning

The 'heavy version' of Puffed Wheat: landing-write and wet-state expansion rules are exactly the same, only the block written changes from a hay block to the 30-hardness / 6.0-resistance compressed hay block.

### 4.2 Type and category

| Item | Value | Basis |
|---|---|---|
| Profile ordinal | entry 19 of the 19 profiles | BRIEF §3.1 |
| Registry name | `farmstorm:compressed_puffed_wheat` | BRIEF §3.1 |
| Type | `THROWABLE` (Throwable) | D-56 |
| Category | `CONSTRUCTION` (Construction) | D-56 |
| `cropBlock` / `seedItem` / `kind` | all `null` | D-56 |
| Positioning | a 'tier-2 composite', not an 'improved crop' | D-56 |

### 4.3 Acquisition

| Route | Description | Basis |
|---|---|---|
| Shapeless crafting | 4 `farmstorm:puffed_wheat` in any arrangement → 1 `farmstorm:compressed_puffed_wheat` | D-67 |
| Converter | **Does not enter** any converter recipe | D-56 |
| Lucky Berry pool | **Does not enter** the pool | D-56 |
| Planting | No crop block and no seed, so it cannot be planted and cannot be replenished by farming | D-56 |

### 4.4 Numbers

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Stack limit | 16 | items | D-16 (existing key `stack.throwable`) |
| Projectile lifetime | 100 | ticks | G-6 (existing key `throwable.lifetimeTicks`) |
| Dry-state replacement cube edge | 3 | blocks | Carried over from Puffed Wheat (existing key `puffed_wheat.dryEdge`) |
| Wet-state replacement cube edge | 5 | blocks | Carried over from Puffed Wheat (existing key `puffed_wheat.wetEdge`) |
| Block written | `farmstorm:compressed_hay_block` | — | D-77 |
| Resistance floor not written | 3.6e6 | resistance units | D-78 |
| Explosion power / damage cap | **no such parameter** (Puffed Wheat has no explosion stage and this block sets none either) | — | D-77 |
| Crafting ratio | 4 → 1 | items | D-67 |

### 4.5 Behaviour specification

Landing settlement is **carried over from Puffed Wheat item by item** (`01-DESIGN.md` §4.1); only the block written changes:

1. **Landing determination**: carry over the priority of `01-DESIGN.md` §3.4 (hit an entity → hit-block inner-face coordinate plus exact point → timing out / unloaded chunk voids it); an entity settles exactly once in its lifetime.
2. **Wet-state check**: first probe for a water source once inside the dry-state cube of edge 3 — the criterion is the fluid tag `FluidTags.WATER` with `fluidState.isSource()` true (`01-DESIGN.md` §4.1); on a hit the whole pass switches to wet edge 5, with **no second check**.
3. **Write volume**: a cube centred on the landing block, edge 3 (dry) or 5 (wet), equivalent to Chebyshev radius 1 / 2 (`01-DESIGN.md` §3.7). Air, fluids and solid blocks inside the range are all written over directly, and the replaced blocks settle no drops.
4. **Resistance floor**: if the original block at the target position has blast resistance ≥ 3.6e6 (`BlastRule.UNBREAKABLE_RESISTANCE`), **skip it and write nothing**. This floor — skip and write nothing at resistance ≥ 3.6e6 — already exists in the v0.0.1 implementation (`PuffedWheatProjectile`), and D-77 inherits it together with Puffed Wheat's landing write (same threshold attribution as BRIEF D-78).
5. **No explosion stage**: Puffed Wheat's landing settlement does only two things — write the cube as hay blocks and skip blocks with resistance ≥ 3.6e6 (`01-DESIGN.md` §4.1, the source file named in the previous entry) — and it triggers no `Explosion`. This block therefore **likewise has no** 'explosion power' or 'damage cap' parameter. D-77 has made this a hard requirement: no v0.0.2 document may say 'explosion power and damage cap carried over from Puffed Wheat'.
6. **No Demolition semantics**: the category is `CONSTRUCTION` only, without `DEMOLITION`, so it neither destroys blocks nor advances the existing 'unauthorised wall demolition' advancement (item 8 of `01-DESIGN.md` §6).

### 4.6 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| A deployed block already inside the landing range | It is overwritten, and the overwritten deployed block **drops no item** (`01-DESIGN.md` §7.11) |
| A bedrock-grade block (resistance ≥ 3.6e6) inside the landing range | Skipped and not written, while the rest of the range is written as usual; the result is 'one hole in a patch of compressed hay blocks' |
| Dry / wet boundary | The water probe runs once, inside the dry cube only; the wet cube is one ring larger, and whether that extra ring contains water **does not affect** the result |
| The landing spans a chunk boundary and the target chunk is unloaded | Voided outright, settlement is not deferred (`01-DESIGN.md` §7.10) |
| The projectile is deflected by a wind charge / another mod | The landing procedure runs again along the new trajectory, still settling exactly once |
| A player crafts 4 Puffed Wheat and throws at once | Normal. Crafting and throwing have no cooldown and no state |
| Overwriting the position the player stands in | Allowed; the player inside blocks is handled by vanilla rules (suffocation / pushed out), with no special exemption |

---

## 5. Molotov

### 5.1 One-line positioning

A thrown fire source that **only writes blocks and never destroys them**: within a landing radius of 4 blocks, every block whose top face touches air rolls its own die — 85% ignites the air position above it, 15% places a magma block.

### 5.2 Type and category

| Item | Value | Basis |
|---|---|---|
| Profile ordinal | entry 16 of the 19 profiles | BRIEF §3.1 |
| Registry name | `farmstorm:molotov` | BRIEF §3.1 |
| Type | `THROWABLE` (Throwable) | BRIEF §3.1 |
| Category | `DAMAGE` + `CONSTRUCTION` (Damage, Construction) | BRIEF §3.1 |
| Corresponding vanilla reference | crimson fungus `minecraft:crimson_fungus` | 【P-2】 |
| `cropBlock` / `seedItem` / `kind` | all `null` | D-51 |
| Demolition tag | **none**. This is the whole basis of §5.10 | BRIEF §3.1 |
| Chinese name | 莫洛托夫！ — **the exclamation mark from the original is kept** | BRIEF §3.1 |

### 5.3 Acquisition

The only route is the converter: `minecraft:crimson_fungus` ⇄ `farmstorm:molotov`, deducting 3 experience points each time, with no catalyst, bidirectional (the unified wording of D-12 + the isomorphic recipe of D-54). No crop block and no seed are registered (D-51), so there is no planting loop.

### 5.4 Numbers

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Trigger explosion power | 0 | power units | D-58; config key `molotov.triggerExplosionPower` defaults to 0; when raised it only adds entity damage and **never breaks blocks** (ruling A-10) |
| Entity damage of the trigger explosion | 0 | points | D-58 |
| Block destruction of the trigger explosion | 0 | blocks | D-58 |
| Effect radius | 4.0 | blocks (Euclidean) | 【P-2】 gives 'radius 4 blocks'; config key `molotov.effectRadius` defaults to 4.0; the reason for the Euclidean metric is given in §5.6 |
| Fire chance | 85 | % | 【P-2】; config key `molotov.fireChance` defaults to 85.0 |
| Magma block chance | 15 | % | 【P-2】; the **remainder**, with no separate key (ruling A-01) |
| Rolls per qualifying block | exactly 1 | rolls | 【本文补充】: 85 + 15 = 100, one roll is mutually exclusive, no two independent checks (same weighted-draw wording as `01-DESIGN.md` §3.8.5) |
| Block written (fire) | `minecraft:fire` | — | D-58 |
| Block written (magma) | `minecraft:magma_block` | — | D-58 |
| Stack limit | 16 | items | D-16 (existing key `stack.throwable`) |
| Projectile lifetime | 100 | ticks | G-6 (existing key `throwable.lifetimeTicks`) |
| Damage type delta | 0 | entries | BRIEF §3.2 |

### 5.5 Landing algorithm (six steps)

The trigger is issued by the projectile landing; the landing itself first follows the existing priority of `01-DESIGN.md` §3.4.

    settleLanding(level, landingVec, pos):
        1. Trigger       -- spawn one 'power 0' explosion at the landing point, only to obtain the landing semantics; settles no damage, touches no block
        2. Enumerate     -- cube of Chebyshev radius 4 centred on the block coordinate containing landingVec
        3. Euclidean cut -- keep blocks whose 'Euclidean distance from block centre to landingVec' is ≤ 4.0
        4. Top-face test -- per block, judge 'top face touches air' (§5.7); drop the ones that fail
        5. Roll          -- each passing block rolls once on its own (§5.8); write the result into the air position above it (§5.9)
        6. Finish        -- discard the projectile entity, set settled true, the same entity never settles again

Two hard constraints run between the six steps: ① at the default power of 0 the explosion of step 1 has **zero side effects**, it is only a 'landing trigger' (D-58); once the power is raised deliberately it **only adds damage to entities and never destroys blocks** — Molotov carries no Demolition tag, `BlastRule.forCrop` always returns `NEVER_BREAK` for it, and the destruction permission has nothing to do with power (ruling A-10); ② step 5 writes only positions that were air, and moves no original block by a single cell (D-58).

### 5.6 How the radius of 4 blocks is counted

1. **Candidate set**: with the block coordinate of the landing as origin, all block positions with `dx, dy, dz ∈ [−4, +4]`. That is 9×9×9 = 729 candidates (【本文补充】: 729 is the enumeration bound, not the number actually affected).
2. **Filtering**: cut the candidate set into a sphere by **Euclidean distance** — the judged point is the `block centre` (`pos.getCenter()`), the origin of the check is the **exact landing coordinate** `landingVec`, and the condition is written `distance ≤ 4.0`.
3. **Why Euclidean**: 【P-2】 says 'radius 4 blocks'. Under `01-DESIGN.md` §3.7 clause 4, 'radius N blocks' is always read as a Euclidean radius; clause 1 says the origin is by default the block centre or the deployment-point coordinate, and throwables follow the exact landing coordinate of §3.4. The two clauses together give item 2 above.
4. **Why not Chebyshev**: Chebyshev radius 4 is a 9×9×9 block box whose corners are 6.93 blocks away (【本文补充】: the geometric value of √(4²+4²+4²)), far from the intuition of 'radius 4 blocks'; the farthest point of the Euclidean sphere is exactly 4.0 blocks.
5. **Blocks not counted**: air, water and blocks whose top face is not solid are all eliminated in step 4, so 'how many blocks are within 4 blocks' is not 'how many rolls were made'.

### 5.7 How 'the top face touches air' is judged

There are only two conditions, both judged per block by coordinate on the server:

| Condition | Check | Note |
|---|---|---|
| A · Above is air | `level.getBlockState(pos.above()).isAir()` is true | Only vanilla `isAir` counts, which covers air, cave air and void air. Water, a snow layer, grass or a torch above is not air |
| B · It has a top face that can bear something | `level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP)` is true | This one naturally excludes air blocks, water, leaves and the like. Without it, 'igniting' a water surface would yield a fire that vanishes at once |

Supplementary wording (【本文补充】):

1. Conditions A and B are in an **and** relationship; without either one there is no roll.
2. Only the single cell directly above is examined, **no recursion**. 'Air above and air above that' does not earn an extra roll.
3. The check and the roll happen at the same instant, with no block update in between; the fire written therefore does not affect the criteria of the other blocks in the same batch (see §5.9 item 1).
4. There is no 'is it flammable' filter. Fire is written even on stone — such fire dies out naturally under vanilla rules, which is the expected result, not a missed check (see §11.3).

### 5.8 How the 85 / 15 is rolled

1. **Granularity**: every block that passes §5.7 **rolls on its own**, not the sphere as a whole and not once per layer.
2. **Random source**: the server-side world random (`RandomSource`), independent of the thrower's luck and reproducible from the world seed.
3. **Rolling**: take a real number `r` in [0, 100); `r < fireChance` → fire, otherwise → magma block. There is **only the one key `molotov.fireChance`** (§10.2); the magma block chance is its remainder and no second key is set (BRIEF §8.1 ruling A-01).
4. **Mutual exclusion**: one roll yields exactly one result, so the same air position never receives both fire and a magma block.
5. **The check 'the two must sum to 100' is gone**: only one chance key remains and the remainder goes to magma blocks automatically, so there is no illegal configuration in which two percentages fail to sum to 100 (BRIEF §8.1 ruling A-01). `molotov.fireChance` takes 0.0–100.0: at 100 everything is fire, at 0 everything is magma.
6. **No floor guarantee**: no compensation of the 'at least one fire' kind. In an extreme case, all magma blocks is a legitimate result.

### 5.9 Which coordinate fire and magma blocks land on

| Item | Coordinate | Note |
|---|---|---|
| Fire | `pos.above()` | the 'air position directly above that qualifying block', exact to the block coordinate, with no centre offset |
| Magma block | `pos.above()` | **the same position** as fire; the two are mutually exclusive |
| The original block `pos` | stays as it is | not replaced, not destroyed, produces no drop |

Three corollaries (【本文补充】):

1. **The mapping is a bijection**: different blocks necessarily have different positions above them, so one Molotov never produces a conflict in which 'two blocks fight over the same air position', and no de-duplication is needed.
2. **No second air check is needed before writing**: condition A already confirmed, at the same instant, that it is air. If implementation ever switches to asynchronous or multi-threaded writing, an `isAir` check must be added before the write; when the write fails, that cell is abandoned — no retry, no relocation.
3. **When the landing point itself is air** (for example after settling on an entity), the landing block is naturally eliminated in the filter while the layer below it still takes part — the observed effect is 'a patch of fire / magma appears below the landing point'.

### 5.10 Why there is no block destruction

This is the most important design constraint of the section; its basis is D-48, D-19 and D-58:

1. **Only one place decides the destruction permission**: permission to destroy blocks is decided by `CropCategory.DEMOLITION` alone, and there is no second such place in the repository (D-48 iron rule; the basis paragraph of D-58 names it again and states explicitly that it is 'not D-19'). D-19 governs the **resistance threshold** — it decides what can be blown up, not who is entitled to blow it up; the two are not the same thing: the threshold lives in `BlastRule`, not at the individual explosion call site.
2. **Molotov has no Demolition tag**: its categories are Damage and Construction (BRIEF §3.1). Carrying no `DEMOLITION`, it is not entitled to destroy blocks at the rules layer — however much the effect looks like an explosion.
3. **D-58 degrades the explosion into a trigger**: the power of that landing explosion is 0. A vanilla `Explosion` at radius 0 gives Infinity in the damage formula and is therefore naturally harmless; block destruction likewise does not happen. That leaves this explosion with exactly one piece of information: where the landing point is.
4. **The real effect is a write, not a delete**: fire and magma blocks are written into **air positions**, which is Construction semantics (writing new blocks) and interacts with the original block not at all.
5. **Proof by contradiction**: were Molotov allowed to break blocks, a second criterion would appear at once — 'for the same explosion, by what threshold does Molotov break while the Melon Grenade breaks nothing at threshold 6.0' — which violates D-19 and tears the destruction priority table of `01-DESIGN.md` §7.11 in half.
6. **Knock-on effects**: because it destroys nothing and damages nothing, the damage_types delta is 0 (BRIEF §3.2), and no capping logic such as a damage cap is needed either.

### 5.11 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| Landing point in an unloaded chunk | Voided outright, no settlement (`01-DESIGN.md` §7.10) |
| Landing point in water | A water surface fails condition B and takes no roll; inside a body of water there is no air position either, so no magma block appears out of nowhere under water. Qualifying blocks above the surface roll as usual |
| Landing point in a rain area | Fire is written as usual. It is then put out by the rain under vanilla rules; that is the expected behaviour and no compensation is made (§11.3) |
| No qualifying block inside the landing range | Settlement completes and nothing is written; no error, no particle compensation |
| Hitting the side of a block | The landing point takes the inner-face coordinate of the hit face (`01-DESIGN.md` §3.4), the sphere centre falls inside the pile of blocks, and the result is 'the ring of top faces around the sphere centre is ignited' |
| Blocks in range are changed by another effect at the same instant | This settlement does not retry and does not rescan; it finishes cutting with the state read at that moment |
| Thrown by a player in creative mode | Settles normally and consumes no item (the symmetric treatment of D-16) |

---

## 6. Zangief Cane

### 6.1 One-line positioning

A **one-shot trap** that turns the view of whoever steps on it upside down, locks their keyboard and mouse, then throws them into the air to spin and fall — a complete reproduction of the three moves of the Spinning Piledriver.

### 6.2 Type and category

| Item | Value | Basis |
|---|---|---|
| Profile ordinal | entry 17 of the 19 profiles | BRIEF §3.1 |
| Registry name | `farmstorm:zangief_cane` | BRIEF §3.1 |
| Type | `DEPLOYABLE` (Deployable) | BRIEF §3.1 |
| Category | `TRAP` (Trap) | BRIEF §3.1 |
| `DeployedKind` | `ZANGIEF_CANE`, scan interval 10 ticks | BRIEF §3.2 |
| Corresponding vanilla reference | sugar cane `minecraft:sugar_cane` | 【P-3】 |
| `cropBlock` / `seedItem` | both `null` | D-51 |
| What it looks like in the world | completely invisible (empty model, no elements), keeping only `textures.particle` | D-68 |
| Item icon | goes its own way through `item/generated` and does not turn invisible with the block | D-68 |

### 6.3 Acquisition

The only route is the converter: `minecraft:sugar_cane` ⇄ `farmstorm:zangief_cane`, deducting 3 experience points each time, with no catalyst, bidirectional (the unified wording of D-12 + the isomorphic recipe of D-54). No crop block and no seed are registered (D-51).

### 6.4 Numbers

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Trigger radius | 1.5 | blocks (spherical Euclidean) | 【P-3】 + D-73 |
| Scan interval | 10 | ticks | BRIEF §3.2 |
| Phase 0 duration (inverted, waiting) | 14 | ticks | D-69 |
| Levitation level | VII (`amplifier = 6`) | level | D-69 |
| Levitation duration | 20 | ticks (1 second) | D-69 |
| Phase 2 target duration (fall) | 40 | ticks (2.0 seconds) | 【P-3】 gives 1.7–2.0 seconds; 【本文补充】 takes the upper bound of the range, 40 ticks |
| Spin speed | 72 | degrees/tick | D-70; config key `zangief_cane.spinDegreesPerTick` defaults to 72.0 (ruling A-05) |
| Spin direction | counter-clockwise | — | D-70 |
| Input-lock window | trigger tick → end of phase 2 | — | D-60, 【P-3】 |
| Trigger targets | non-ally players, not creative, not spectator | — | D-71 |
| After the trigger | silently removed, no drop | — | D-72 |
| Attachment position | top face of a block only | — | 【本文补充】: consistent with the two existing traps (`01-DESIGN.md` §4.2 / §4.11) |
| Floating | may not float; removed as soon as the supporting block disappears, whether the item itself drops is decided by the existing key | — | 【本文补充】: Trap default (`01-DESIGN.md` §3.3.3) |
| Sound | `farmstorm:zangief_piledriver`, played once | — | D-66 |
| Client fallback | every tick it self-checks whether the stage effect is still there and resets itself if not | — | RT-15 |
| Total duration cap | 74 | ticks (3.7 seconds) | the sum 14 + 20 + 40, of which 40 is 【本文补充】 |

### 6.5 Trigger check

1. **Scan timing**: the deployed block's BlockEntity scans every 10 ticks (`DeployedKind.ZANGIEF_CANE(10)`).
2. **Metric**: with the **centre of the deployment point block** as sphere centre, take the Euclidean distance from the **centre of the player's bounding box** to the sphere centre ≤ 1.5 blocks (the spherical wording of D-73 + 【本文补充】's choice of the bounding-box centre as the judged point).
3. **Target filtering**: only non-ally players count (`TeamResolver.isFriendly` returns false, `01-DESIGN.md` §3.6); creative mode and spectators do not trigger (D-71); mobs do not trigger (D-71). This matches the exemptions of the three v0.0.1 traps.
4. **Several candidates in the same tick**: the nearest one settles, the others are unaffected — the block is silently removed at the first tick of settlement (D-72), so a second triggerer cannot exist.
5. **Trigger means settle**: `triggered` set true → block silently removed → `farmstorm:zangief_piledriver` at `amplifier 0` applied to the triggerer (14 ticks) → sound played. The whole process completes in the same tick, with no queueing.

### 6.6 Phase state machine (amplifier 0 / 1 / 2)

The **amplifier of the hidden effect `farmstorm:zangief_piledriver` is the stage number** (D-50). The client performs whatever stage it reads; no custom packet is needed (BRIEF §7.5).

| amplifier | Stage | Effect duration | What the server does in this stage | What the client does according to this stage | When this stage is left |
|---|---|---|---|---|---|
| 0 | Inverted, waiting for levitation | 14 ticks | Remembers the triggerer and the trigger tick; does not touch position; re-issues the same id at `amplifier 1` on tick 14 | Camera `roll = 180` plus inverted pitch (D-59); keyboard and mouse locked (D-60); plays the trigger sound once | Remaining duration reaches zero |
| 1 | Levitating | 20 ticks | Gives no extra velocity (the rise is left to vanilla `MobEffects.LEVITATION` itself; D-69 fixes only level and duration); on tick 20 re-issues `amplifier 2` and gives the first downward velocity | Keeps inversion and lock; does not spin; the view rises naturally with the body | Remaining duration reaches zero |
| 2 | Falling and spinning | 40 ticks | Every tick resets the downward velocity from 'remaining height ÷ remaining ticks' (§6.10); clears the effect as soon as landing is detected | Keeps inversion; every tick `yaw += 72°` (D-70); resets the camera angle to 0 on landing or when the effect disappears | Landing / effect expiry / player death / effect cleared from outside |

Three state-machine constraints (【本文补充】): ① the stage may only move forward 0 → 1 → 2, never back; ② when the effect is cleared from outside in any stage (`/effect clear`, the cleanup logic of another mod), the client resets straight to 'no effect' and no further stage is issued; ③ a phase switch is implemented by **re-issuing the effect with the same id and a different amplifier**, one re-issue refreshing amplifier and remaining duration together.

### 6.7 Timing table

Let the trigger tick be T+0. The whole chain is capped at 74 ticks (3.7 seconds) = 14 + 20 + 40.

| # | Moment | Duration | Client presentation | Server action | Synchronisation |
|---|---|---|---|---|---|
| 1 | T+0 | 1 tick | Flips the screen at once (roll 180 + inverted pitch); keyboard and mouse dead, only ESC works; the `zangief_piledriver` sound is heard | Validate the target → set `triggered` true → silently remove the block → apply `amplifier 0` (14 ticks) | Vanilla `ClientboundUpdateMobEffectPacket` + vanilla sound packet |
| 2 | T+0 … T+13 | 14 ticks | Inverted + locked, position does not move | Only counts down; does not touch position | The remaining duration of the hidden effect is the complete state |
| 3 | T+14 | 1 tick | The camera starts rising with the body | Re-issues `amplifier 1` (20 ticks) | Same as above (a different amplifier means a different stage) |
| 4 | T+14 … T+33 | 20 ticks | Stays inverted and locked; the player can see themselves being lifted by vanilla levitation | Does not intervene; records the lift height for §6.10 | Hidden effect + vanilla entity position sync |
| 5 | T+34 | 1 tick | Starts to fall; turns 72° in the first tick | Re-issues `amplifier 2` (40 ticks); sends the first downward velocity | Hidden effect + `ClientboundSetEntityMotionPacket` |
| 6 | T+34 … T+73 | ≤ 40 ticks | Every tick `yaw += 72°` (2880° over 40 ticks, i.e. 8 whole turns); inversion is kept | Tick by tick resets the downward velocity from 'remaining height ÷ remaining ticks'; detects landing | Same as above; the spin is a pure client-side accumulation and is not sent every tick |
| 7 | Landing tick (≤ T+74) | 1 tick | Camera angle reset; keyboard and mouse unlocked; screen upright again | Clears `farmstorm:zangief_piledriver` | Effect-removal packet |

This table has 7 rows. In it, '2880°' is the product 72 degrees/tick × 40 ticks and '8 whole turns' is the quotient 2880° ÷ 360° (【本文补充】: results of arithmetic, not independent values). **The '≤ 40 ticks' of stages 5 and 6 is not vagueness**: 40 ticks is the design target, and landing earlier ends the stage early (§6.10 recomputes the servo velocity every tick); landing later does not happen — unless there is no ground to land on beneath the player (§11.5).

### 6.8 Camera

1. **How it is layered**: `roll = 180°` **and** inverted pitch (D-59). Doing only one of the two is wrong; both must be applied together.
2. **Yaw spin**: during phase 2, `yaw += 72°` every tick, counter-clockwise (D-70), accumulated on the client per tick and not dependent on per-tick server sync (BRIEF §7.4).
3. **Render layer only**: the angle is changed through `ViewportEvent.ComputeCameraAngles` and is **never written back** to `player.getYRot()` / `player.getXRot()`. The player's real facing is unchanged from start to finish, and server authority is not broken (BRIEF §7.4).
4. **Reset moments**: when the effect disappears, when the phase returns to 'none', on player death and on any view switch that rebuilds the camera, the accumulated value must be zeroed. A missed reset leaves the camera permanently skewed, and that is the bug this section is most likely to produce.
5. **The GUI is unaffected**: roll / pitch act on the camera matrix, not on the HUD or container screens.
6. **Spin speed already ruled**: by BRIEF §8.1 ruling A-05, `zangief_cane.spinDegreesPerTick` defaults to **72.0 degrees/tick** (72 × 20 = 1440 degrees/second), which satisfies '1440 degrees per second' on line 29 of `plan0.0.2.txt`; 40 ticks accumulate 2880° = 8 whole turns, counter-clockwise (yaw increasing, D-70 has been amended accordingly). The old fact card wrote '1440 ÷ 20' as 24; that was a slip in its own arithmetic, corrected here, and the 'two landing conventions' wording is no longer kept.

### 6.9 Input lock

The lock scope copies D-60 and **keeps ESC only**:

| What is locked | How far it is locked |
|---|---|
| Movement keys (forward, back, left, right) | All zeroed; the player does not move during phases 0 / 1 |
| Jump, sneak | Same as above |
| Attack, use / place | Cancelled, no swing and no block placed |
| Hotbar switching (1–9 and the scroll wheel) | Cancelled, the slot cannot be switched |
| Opening the inventory (E), chat (T / slash) | Cancelled, the screen does not open |
| ESC | **Allowed through**. Locking ESC would make the player think the game has frozen (D-60) |

The implementation lands on five client events (the table of BRIEF §7.3). This section fixes only the window: **from the trigger tick to the end of phase 2**. The lock is released the moment the effect disappears, with no extra 'only after landing' criterion — otherwise a player who falls into the void would stay locked forever (§11.5).

### 6.10 Falling

1. **Why a single velocity is not enough**: vanilla gravity drops the y velocity by another 0.08 every tick and multiplies it by 0.98 (【本文补充】: vanilla gravity parameter −0.08 blocks/tick², drag 0.98). A one-off initial velocity makes the fall accelerate and the landing time drift with height, so '1.7–2.0 seconds' cannot be guaranteed.
2. **Servo rule** (【本文补充】): the downward velocity is recomputed every tick

    velocity = remaining height ÷ remaining ticks      // remaining ticks = 40 − elapsed, denominator clamped to at least 1

   so however high the player was lifted, they reach the ground on tick 40; if a block stops them earlier, the stage ends earlier.
3. **How the remaining height is taken**: search downward from the player's feet for the first standable surface or the world minimum y, whichever is nearer.
4. **End conditions**: the player's `onGround()` is true / a block stops them from below / 40 ticks are used up / the player dies / the effect is cleared. Any one of the five clears the effect, unlocks and resets the camera.
5. **No snapping, no ghosting through**: the fall is a velocity, not a teleport; hitting a block stops the player on that block (the same wording as the dash of D-65).

### 6.11 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| The triggerer dies on the spot | The effect disappears with the entity and the lock and camera are released with it; the block was already silently removed, so it cannot trigger twice (§11.4) |
| The triggerer quits the game | The server no longer ticks that player; after reconnecting the effect continues for its remaining duration (§11.4) |
| Only creative-mode players nearby | No trigger (D-71); the block stays on standby |
| The owner is offline | The trap works as usual; the friend-or-foe check uses the owner's last known team (`01-DESIGN.md` §7.2) |
| The triggerer is pushed away by another effect during phase 1 | The server does not correct the position; the remaining height of phase 2 is computed afresh from the new position |
| No ground beneath during phase 2 (void) | When the 40 ticks run out the lock is released and vanilla gravity is handed back, so the player can save themselves (§11.5) |
| The supporting block is broken | The deployed block is removed on the next tick; whether the item itself drops is decided by the existing key `deploy.dropOnSupportLost` (drops by default, `01-DESIGN.md` §3.3.3) |
| One player steps on two Zangief Canes in a row | If the second triggers during the first one's phase, the effect is re-issued as `amplifier 0`, equivalent to starting the round again (【本文补充】: a phase can only start from 0; phases do not stack) |

---

## 7. Wild Hunter

This is the longest section, because this single status effect rewrites four things at once: the attribute panel, screen post-processing, target acquisition and displacement, and audio. The eight sub-specifications correspond item by item to D-61 ` D-76, and a reader should be able to implement the behaviour from this section alone.

### 7.1 One-line positioning

A status effect that temporarily turns the player into a 'hunter': three attribute bonuses + full-screen black-and-white + red silhouette marking + aim-locked dash + kill-based extension — all five hold at the same time while the effect lasts.

### 7.2 Type and category

| Item | Value | Basis |
|---|---|---|
| Effect id | `farmstorm:wild_hunter` | BRIEF §3.2 |
| Type | A status effect (`MobEffect`), not a crop, not an item | BRIEF §3.2 |
| Visibility | **visible**, with an icon, appearing in the effect row of the player HUD | BRIEF §3.2 |
| Category attribution | carries no `CropCategory` itself; it is applied by Wild Apple as the carrier of the **Buff** category (`01-DESIGN.md` §2.3.4) | BRIEF §3.1 |
| Chinese name | 狂 野 猎 人 — **original layout kept: 4 characters with one space between each, 3 spaces in total** | 【P-6】 |
| Icon | 18×18 PNG converted from the desktop JPG (the conversion wording of D-53) | D-66 |

### 7.3 Acquisition

| Route | Description | Basis |
|---|---|---|
| Eating Wild Apple | the main route, granting 35 seconds (700 ticks) | 【P-7】 |
| Other sources | vanilla effect commands, effects granted by other mods. The effect does not distinguish sources; every source triggers all five behaviours | 【本文补充】 |

### 7.4 Numbers

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Effect duration (via Wild Apple) | 700 | ticks (35 seconds) | 【P-7】; config key `wild_hunter.durationTicks` defaults to 700 (ruling A-02) |
| Movement speed bonus | +30 | % | 【P-6】 |
| Attack speed bonus | +30 | % | 【P-6】 |
| Attack damage bonus | +40 | % | 【P-6】 |
| Target-acquisition radius | 5.0 | blocks (spherical Euclidean) | 【P-6】 |
| Target-acquisition angle cap | 30 | degrees | D-64 |
| Dash duration | 3 | ticks | D-65 |
| Red silhouette distance cap | 64 | blocks | D-62 |
| Kill extension duration | 160 | ticks (8 seconds) | D-74 |
| Health restored per kill | 10 | points (5 hearts) | D-74 |
| Extension count cap | none | times | D-74 |
| Looping sound audible distance | 16 | blocks | D-76 |
| Source of the looping sound volume | follows the player position | — | D-76 |
| Sound category | `SoundSource.PLAYERS` | — | D-76 |
| Audio format | mono, 64 kbps Ogg Vorbis | — | D-53 |
| Silhouette colour | RGBA(1, 0.15, 0.15, 0.45) | — | BRIEF §7.2 |
| Knockback strength | vanilla default | — | D-75, no separate number given |

### 7.5 The three attribute bonuses

All three bonuses are implemented as **attribute modifiers**, so they synchronise free of charge over the vanilla `ClientboundUpdateAttributesPacket` and need no custom packet (the wording of D-50).

| Attribute | Bonus | Modifier Operation | Example of the effect | Basis |
|---|---|---|---|---|
| `minecraft:generic.movement_speed` | +30% | `ADD_MULTIPLIED_TOTAL` | vanilla base 0.1 blocks/tick → 0.13 blocks/tick (【本文补充】: vanilla base value 0.1) | 【P-6】 fixes the percentage; the Operation choice is 【本文补充】 |
| `minecraft:generic.attack_speed` | +30% | `ADD_MULTIPLIED_TOTAL` | vanilla base 4.0 hits/second → 5.2 hits/second (【本文补充】: vanilla base value 4.0) | same as above |
| `minecraft:generic.attack_damage` | +40% | `ADD_MULTIPLIED_TOTAL` | bare hand 1.0 point → 1.4 points; with a weapon the total damage is scaled proportionally (【本文补充】: vanilla bare-hand base value 1.0) | same as above |

The three numbers 0.13 blocks/tick, 5.2 hits/second and 1.4 points in the table are illustrative values computed as 'vanilla base value × (1 + bonus)' (【本文补充】); they show the order of magnitude and are not a new source of numbers.

**Why a multiplicative modifier and not an additive one** (【本文补充】):

1. The original says 'raise by thirty / forty percent', i.e. a percentage **relative to the current value**. An additive modifier (`ADD_VALUE`) can only express a fixed number of points, and the moment the player's base value changes (swapping weapons, drinking a potion) the meaning 'raised by four tenths' breaks down.
2. Why not `ADD_MULTIPLIED_BASE`: it multiplies the **base value** only. The base value of attack damage is just 1.0 point, so +40% = +0.4 points, an order of magnitude away from what 'damage raised by four tenths' actually feels like; `ADD_MULTIPLIED_TOTAL` multiplies 'base value + all additive modifiers + weapon damage', which is what the original means.
3. **Stacking semantics** (the key point): the amounts of several `ADD_MULTIPLIED_TOTAL` modifiers on the same attribute are **summed** first and the total is multiplied once, not multiplied successively. So Speed II (+40%) and Wild Hunter (+30%) present together give ×1.70 (= 1 + 0.40 + 0.30), not ×1.82 (= 1.4 × 1.3) — both multipliers are arithmetic illustrations of 【本文补充】, used to tell 'summing' apart from 'successive multiplication'. This is expanded in §11.7.
4. **Exact removal**: each of the three modifiers carries a fixed identity (name + UUID). When the effect expires, is cleared or the player dies, they are removed one by one by identity, without deleting modifiers that other sources (potions, equipment, other mods) put on the same attribute.

### 7.6 Black-and-white view (including the GUI)

| Item | Specification | Basis |
|---|---|---|
| Scope | world rendering **and** the GUI layer: full-screen greyscale, not just a desaturated world | D-61 |
| Carrier | 1 post_effect JSON + 2 GLSL source files, packaged into the jar | BRIEF §3.2 |
| Mount point | preferably 'one full-screen pass after the GUI, before the screen'; the concrete event and the alternatives are in section 7 of `12-TECH-0.0.2.md` | BRIEF §7.1 |
| Activation condition | the local player themself carries `farmstorm:wild_hunter` | D-50 |
| Unmounting | post-processing stops in the same instant the effect disappears, leaving no afterimage | 【本文补充】 |
| Affected party | only the holder's own screen; other players on the same server see no greyscale | 【本文补充】 |

Three prohibitions (BRIEF §7.1, copied verbatim): ① faking greyscale with a change of fog colour is prohibited; ② faking greyscale by swapping textures is prohibited; ③ if the preferred mount point does not hold, the fallback must be 'one pass for the world + one pass for the GUI', not 'grey the world only' — the latter must be written into the Release notes and registered under RT-10 as a degradation.

**Why the GUI must be included**: D-61 asks for 'the whole picture the player sees losing its colour'. Greying only the world leaves a colourful ring of hotbar and health bar behind, and the look breaks at once.

### 7.7 Red highlight

| Item | Specification | Basis |
|---|---|---|
| Trigger condition | the local player themself carries `farmstorm:wild_hunter` | D-50 |
| Distance cap | ≤ 64 blocks | D-62 |
| Visibility | line of sight is required (no block in the way), **never through walls** | D-62 |
| Excluded targets | the player, teammates | D-62 |
| Drawing method | take the entity model's `renderToBuffer` and draw it again with the translucent self-illuminating render type | BRIEF §7.2 |
| Colour | shader colour set to RGBA(1, 0.15, 0.15, 0.45) | BRIEF §7.2 |
| Texture | 1×1 white texture `assets/farmstorm/textures/misc/white.png` | BRIEF §7.2 |

Three prohibitions (BRIEF §7.2, copied verbatim): ① calling the render dispatcher from an entity's render event to redraw the whole entity is prohibited — it recurses into its own event; ② substituting the vanilla glowing effect is prohibited — that is a white outline visible to the whole server, which contradicts D-62; ③ a 'highlight visible to everyone' is not to be built — it is a visual enhancement on the holder's side only.

**How visibility is judged** (【本文补充】): `player.hasLineOfSight(entity)` is true **and** the Euclidean distance from the player's eye to the entity's judged point is ≤ 64.0 blocks. The two conditions are in an and relationship; if either fails, nothing is drawn. The judged point is the centre of the entity's bounding box.

### 7.8 Target-acquisition algorithm

The candidate set and the ordering rules are all copied from D-64:

1. **Range**: a sphere (Euclidean) centred on the player with **radius 5.0 blocks**. 【P-6】 says 'within 5 blocks', which is read under `01-DESIGN.md` §3.7 clause 4 as a Euclidean radius.
2. **Angle**: the angle between the candidate and the player's **look direction** is **≤ 30°**.
3. **Type**: only `LivingEntity`.
4. **Exclusions**: the player themself, teammates (`TeamResolver.isFriendly`), spectators.
5. **Ordering**: the smallest angle wins; on equal angles the nearer one.
6. **Computability**: 'the one closest to the crosshair on screen' is equivalent to 'the smallest angle to the look direction', which the server can compute, **with no need to read the client's mouse position** (the wording of D-64).

    selectTarget(player):
        look = player.getLookAngle()                      // look unit vector
        eye  = player.getEyePosition()                     // origin of the check is the eye
        best = null
        for e in level.getEntitiesOfClass(LivingEntity, bounding box centred on player, radius 5.0):
            if e == player                       → skip   // itself
            if e is a spectator                  → skip   // D-64
            if TeamResolver.isFriendly(player, e) → skip   // same team
            v = e.getEyePosition() - eye                   // eye to eye
            d = |v|
            if d > 5.0                           → skip   // Euclidean sphere
            if v·look / d > cos(30°) is not true  → skip   // angle > 30°
            angle = acos(v·look / d)
            best = whichever is higher priority than best (smaller angle first; equal angle → smaller d)
        return best

Two additions (【本文补充】): ① the angle is computed from the **eye-to-eye** vector, not from the foot point — that way tall mobs are not wrongly excluded at small angles; ② the comparison only needs the cosine values, since they are monotonically related, so `acos` need not actually be computed.

### 7.9 Dash (how the snap is implemented)

'Snapping next to the target' is **not a teleport** but a 3-tick dash (D-65):

| Item | Specification | Basis |
|---|---|---|
| Displacement duration | 3 ticks | D-65 |
| Displacement speed | initial velocity computed from the current distance so that the player spans it within 3 ticks (【本文补充】: displacement per tick = distance ÷ 3) | D-65 |
| Delivery method | `setDeltaMovement`; the client simulates movement and collision as usual | D-65 |
| Hitting a wall | stops on the spot, **no snapping, no ghosting through** | D-65 |
| Landing point occupied by a block | that dash simply fails, with no snap this time | D-65 |
| Crosshair aimed at the target | the **client** turns the view towards the target locally when the dash starts, synchronised with the client's normal rotation packet; the server does not write `setYRot` / `setXRot` | 【本文补充】, in the spirit of 'the render layer never writes back' of BRIEF §7.4 |
| Driving party | client-predicted displacement + server adjudication (on receiving the swing packet the server independently computes again with the same algorithm as §7.8) | BRIEF §7.6 |
| New packets | **0**. No `CustomPacketPayload` may be added for this feature | D-50 |

**Three red lines** (copied from BRIEF §7.6): ① the preferred solution may not be swapped for 'per-tick server teleportation' unless the preferred one turns out unacceptable in play; ② if it really is swapped, a decision record must be left in `12-TECH-0.0.2.md`; ③ whichever is chosen, no packet may be added for it.

**Why not a teleport**: a teleport passes through walls, skips collision and gives the other side no reaction time at all; a dash uses vanilla movement and collision, stops on hitting a wall, and is right on both feel and fairness (D-65).

### 7.10 Knockback

| Item | Specification | Basis |
|---|---|---|
| Strength | the default strength of a vanilla melee attack, with no custom number and no level | D-75 |
| Trigger condition | **after damage was successfully dealt** | 【P-6】: 'if damage is successfully dealt, knock the target back' |
| Direction | decided by vanilla rules (the horizontal line from the attacker to the target) | D-75 |
| Immunity | when the target is itself immune to knockback (for example while riding, or when another mod takes over) no extra compensation is made | 【本文补充】 |

The criterion for 'damage successfully dealt' (【本文补充】): the **target actually lost more than 0 health**. When the target is in invulnerability frames, the damage is fully absorbed, or the damage is 0, knockback is **not** applied — otherwise the odd phenomenon of 'cannot hurt them yet kicks them flying' appears.

### 7.11 Kill extension

| Item | Specification | Basis |
|---|---|---|
| Trigger | **killing any mob** while the effect is active | 【P-6】 + D-74 |
| Effect duration | current remaining duration **+160 ticks (8 seconds)** | D-74 |
| Health restored | **+10 points** (5 hearts), not exceeding maximum health | D-74 |
| Count cap | **none**. The original gives no cap, so none is added | D-74 |
| Duration cap | none. 160 ticks may accumulate without limit | 【本文补充】, synonymous with 'no count cap' |
| Kill attribution | given by vanilla kill credit (the killer in `LivingEntity#die` is the player holding this effect) | 【本文补充】 |

Three details (【本文补充】): ① it is 'add 160 ticks', not 'set to 160 ticks' — a kill at 30 ticks remaining leaves 190 ticks; ② the duration is allowed to be pushed past the effect's own natural cap (this effect sets none); ③ summons, passive mobs and bosses are treated alike, with no whitelist.

### 7.12 Looping audio

| Item | Specification | Basis |
|---|---|---|
| Resource | `farmstorm:wild_hunter_loop` (declared as a looping event in `sounds.json`) | BRIEF §7.7 |
| Source file | `Animals.mp3` converted to mono / 64 kbps Ogg Vorbis | D-53, D-66 |
| Playing party | one looping `SoundInstance` on the client, not re-sent by the server every tick | D-76 |
| Position | the sound source is updated onto the player every tick and follows the player | D-76 |
| Audible distance | 16 blocks | D-76 |
| Sound category | `SoundSource.PLAYERS` | D-76 |
| Start condition | the client detects `farmstorm:wild_hunter` appearing on the local player | D-50 |
| Stop condition | effect disappears / player dies / quits the world, stop immediately | 【本文补充】 |

Three wordings (copied from D-76 and BRIEF §7.7): ① it does not occupy the `MUSIC` category and **does not interrupt vanilla background music**; ② the server does **not** re-send playback instructions every tick, it only synchronises on changes of the effect's own state; ③ whether the 16-block audible distance on 1.21.1 is achieved by clamping `getVolume` or by the audible-distance hook is an implementation-phase measurement item (the same class of risk as RT-14); at document level only the number '16 blocks' is fixed.

**Provenance register** (D-66, must appear in both the README and the Release notes): the `Animals.mp3` audio source is registered as non-commercial personal use, and is packaged into the jar after conversion.

### 7.13 The full melee attack flow

【P-6】 gives a flow sketch; this document turns it into a state machine — every cell points back to a decision:

    Attack key pressed (client)
      ├─ no wild_hunter effect locally
      │     └─ vanilla attack, nothing changed
      ├─ effect present locally, but §7.8 cannot produce a legal target
      │     └─ vanilla attack (no snap, no extra knockback)          // the empty-set branch of D-64
      └─ a legal target exists locally
            ├─ a 3-tick dash starts locally, the local view turns to the target   // D-65, client prediction
            └─ the server receives the swing packet
                  ├─ recomputes the target with the same algorithm       // D-64, deterministic
                  ├─ the target no longer exists / the landing point is occupied by a block
                  │     └─ no snap this time, settles as a normal attack   // D-65
                  └─ the target exists
                        ├─ settles damage
                        ├─ health lost > 0 → knockback at vanilla default strength   // D-75
                        └─ a kill was made → +160 ticks / +10 points      // D-74

Three facts bound to the flow: ① the client computes once for feel (no waiting for the reply) and the server computes once for authority — both sides use the **same deterministic algorithm**, so the conclusions agree (BRIEF §7.6); ② no step of the flow needs a new packet (D-50); ③ 'crosshair aimed at the target' rests on a local client-side turn; the server does not touch facing (the table of §7.9).

### 7.14 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| The player goes offline while the effect is active | The effect is kept with the save and continues for its remaining duration on reconnection; the looping sound is rebuilt by the client (§11.6) |
| Stacking with Speed / Strength-type effects | modifiers with the same Operation are summed first and then multiplied once, not multiplied together (§11.7) |
| The target dies mid-dash | The displacement has already happened; damage and knockback are not settled, and the target is not switched (§11.9) |
| An invisible target | The highlight is drawn all the same (invisibility does not affect `hasLineOfSight`), and target acquisition can still lock on (§11.8) |
| A target in creative mode | Can be locked and can be snapped to, but vanilla settles no damage against creative mode, so there is no knockback and no extension (【本文补充】) |
| Attacking while riding | The dash does not apply (vanilla riding velocity is decided by the vehicle) and degrades to a normal attack (【本文补充】) |
| The effect is removed by `/effect clear` | The three attribute modifiers are removed at once, greyscale and silhouette vanish at once, the looping sound stops at once; a dash already under way is not interrupted (it ends naturally within 3 ticks) |
| Several attack presses in the same tick | The first swing packet the server receives wins; the rest are handled as normal attacks (【本文补充】) |
| The player collides with another player during the dash | Vanilla collision handling; no extra damage, no extra knockback (【本文补充】) |

---

## 8. Wild Apple

### 8.1 One-line positioning

A food that sells Wild Hunter to the player: eat it and the hunter state is granted for 35 seconds, at the price of one conversion costing 3 experience points.

### 8.2 Type and category

| Item | Value | Basis |
|---|---|---|
| Profile ordinal | entry 18 of the 19 profiles | BRIEF §3.1 |
| Registry name | `farmstorm:wild_apple` | BRIEF §3.1 |
| Type | `EDIBLE` (Edible) | BRIEF §3.1 |
| Category | `BUFF` (Buff) | BRIEF §3.1 |
| Corresponding vanilla reference | golden apple `minecraft:golden_apple` | 【P-7】 + D-54 |
| `cropBlock` / `seedItem` / `kind` | all `null` | D-51 |
| Chinese name | 狂野苹果 | BRIEF §3.1 |

### 8.3 Acquisition

| Route | Description | Basis |
|---|---|---|
| Converter (forward) | golden apple → Wild Apple, deducting 3 experience points, **no catalyst** | D-54 |
| Converter (reverse) | Wild Apple → golden apple, likewise deducting 3 experience points | D-54 (`bidirectional = true`) |
| Planting | no crop block, no seed, cannot be planted | D-51 |
| Crafting | no crafting recipe | the recipe delta of BRIEF §3.2 |

### 8.4 Numbers

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Effect applied | `farmstorm:wild_hunter` | — | 【P-7】 |
| Effect duration | 700 | ticks (35 seconds) | 【P-7】; config key `wild_hunter.durationTicks` defaults to 700 (ruling A-02: the duration belongs to the effect and the apple sets no key of its own) |
| Eating time | 32 | ticks | D-29 (existing key `food.eatTicks`) |
| Nutrition | 4 | points | takes the same value as the vanilla golden apple; config key `wild_apple.nutrition` defaults to 4 |
| Saturation modifier | 1.2 | multiplier | takes the same value as the vanilla golden apple; config key `wild_apple.saturationModifier` defaults to 1.2 |
| Stack limit | 64 | items | D-16 (existing key `stack.other`) |
| Conversion experience cost | 3 | points | D-54 (same wording as the existing conversions) |
| Nether Star catalyst | 0 | items | D-54 (no catalyst) |
| Enters the Lucky Berry pool after eating | no | — | D-55 |

### 8.5 Behaviour specification

1. Hold right-click for 32 ticks to finish eating; the server branch of `finishUsingItem` applies `farmstorm:wild_hunter` (the behaviour contract of `01-DESIGN.md` §2.2.4 is unchanged).
2. **Eating again only refreshes the duration and never stacks a level**: this effect has no concept of level, so re-applying it is equivalent to resetting the remaining duration to 700 ticks (`01-DESIGN.md` §2.2.4 contract 3).
3. Eating in creative mode consumes no item (the symmetric treatment of D-16).
4. After eating, all five behaviours of §7 take effect immediately: attributes, greyscale, silhouette, dash, looping sound.
5. Nutrition and saturation act only on the player's hunger system and take no part in any combat calculation.

### 8.6 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| Eating another one before the effect ends | The duration resets to 700 ticks; the level is unchanged and does not stack |
| Giving yourself the effect with a command, then eating | Also resets to 700 ticks. The effect records no source; only the current valid period counts |
| Trying to craft a golden apple with it | Not allowed. Improved crops take no part in vanilla recipes (D-09); for a golden apple, use the reverse direction of the converter |
| The Lucky Berry rolling it | It will not. The pool excludes `wild_apple` explicitly (D-55), because the strongest item of this version should not be produced at random by 1 berry |
| Quitting the game right after eating | The effect is kept with the player save and continues for its remaining duration on reconnection (§11.6) |
| Eating in creative mode | The effect is applied and no item is consumed |

---

## 9. Advancement list

### 9.1 The three new advancements

3 entries are added in total, hung beneath the existing advancement tree; the existing 12 are **untouched** (BRIEF §3.4). The total afterwards is 15 (BRIEF §4).

| # | ID | Display name (zh / en) | Parent | Trigger condition | Basis |
|---|---|---|---|---|---|
| 13 | `farmstorm:first_molotov` | 火了！ / It's Lit! | `farmstorm:first_throw` | Throw one `farmstorm:molotov` (judged as soon as the projectile entity spawns; a hit is not required) | D-63; the display name follows the language-key table of §7.3 in `12-TECH-0.0.2.md`; the parent is 【本文补充】 |
| 14 | `farmstorm:zangief_hit` | 手摇重 360 / Hand-Cranked 360 | `farmstorm:first_deploy` | First hit by a Zangief Cane: some player is applied `farmstorm:zangief_piledriver` at `amplifier 0` | D-63; same as above |
| 15 | `farmstorm:first_wild_hunter` | 狂野猎人 / Wild Hunter | `farmstorm:root` | The player gains the `farmstorm:wild_hunter` effect for the first time (**source not restricted**) | D-63; same as above |

**Wording of the display names**: all three are short phrases (火了！ / 手摇重 360 / 狂野猎人), consistent with the language-key table of §7.3 in `12-TECH-0.0.2.md`. The reason: all 12 advancement names of v0.0.1 are short phrases (出手 / 落地 / 闭环自给 / 别站那儿 / 星之代价 …), whereas 'It's Lit! It's Lit!' (「火了！火了！」), 'Take my Hand-Cranked 360!' (「吃我一个手摇重360！」) and 'I Am Blot Handel!' (「我是布洛特·亨德尔！」) are the **item tooltips** in `plan0.0.2.txt`, belonging to the `item.farmstorm.*.tooltip` class, not to advancement titles. The two are different language-key namespaces and must not be mixed.

Carrying over the convention of `01-DESIGN.md` §6: all of them use `frame = task`, `show_toast = true`, `announce_to_chat = false`, the icon uses the corresponding item (`molotov` / `zangief_cane` / `wild_apple`); advancement checks all run on the server, with no custom command (D-24).

### 9.2 Three details of the trigger wording

1. **`first_molotov` does not require a hit**: the original only says 'thrown for the first time', so it holds as soon as the projectile entity spawns. Dropping it at your own feet right after throwing counts too.
2. **`zangief_hit` counts only a successful trigger**: it holds once `amplifier 0` is applied, regardless of whether the player later lands or is actually hurt. Creative mode and spectators do not trigger the trap (D-71), so they do not advance this advancement either.
3. **`first_wild_hunter` does not restrict the source**: eating a Wild Apple, using a command or being given the effect by another mod all advance the same advancement. 【P-7】 defines only the one source that is the Wild Apple, while the advancement is defined as 'first time the effect is gained', consistent with the wording of D-63.

### 9.3 Relationship to the existing 12

| Relationship | Description |
|---|---|
| Parent advancement | The three new advancements hang under `first_throw` (existing entry 6), `first_deploy` (existing entry 7) and `root` (existing entry 1) respectively; no new root node is created |
| Mutual independence | Not a single character of the existing 12 trigger conditions changes, and the new advancements share no criteria with the existing ones |
| Chinese names | All three use short phrases, consistent with the language-key table of §7.3 in `12-TECH-0.0.2.md`; the item tooltips (`item.farmstorm.*.tooltip`) are another set of keys and take no part in advancement titles |

---

## 10. Config reference table

**This section only references**: it lists the config key names and default values this design needs. The **sole source** for new keys is `.work/v002-BRIEF.md` §8; this document and `12-TECH-0.0.2.md` both copy it and neither counts it again. **The full definition is in section 8 of `12-TECH-0.0.2.md` (the list is its §8.1)**.

### 10.1 Existing keys carried over (referenced only, not one default value changed)

| Key | Default | Used by this document in | Basis |
|---|---|---|---|
| `converter.xpCost` | 3 | the conversions of §2, §5, §6, §8 | D-12, `01-DESIGN.md` §3.8.1 |
| `converter.netherStarForwardCost` | 1 | §2 (the three new conversions of this document do not consume it) | D-12 |
| `stack.throwable` | 16 | §4 Compressed Puffed Wheat, §5 Molotov | D-16 |
| `stack.other` | 64 | §8 Wild Apple | D-16 |
| `throwable.lifetimeTicks` | 100 | §4, §5 | G-6 |
| `puffed_wheat.dryEdge` | 3 | §4 dry-state replacement edge | `01-DESIGN.md` §3.8.3 |
| `puffed_wheat.wetEdge` | 5 | §4 wet-state replacement edge | `01-DESIGN.md` §3.8.3 |
| `food.eatTicks` | 32 | §8 eating time | D-29 |
| `explosion.grenadeResistThreshold` | 6.0 | §3 blast-resistance matrix (Melon Grenade side) | D-19 |
| `explosion.sliceResistThreshold` | 6.0 | §3 blast-resistance matrix (Melon Slice Bomb side) | D-19 |
| `deploy.dropOnSupportLost` | true | §6 whether the Zangief Cane drops its item when support is lost | G-9 |

Note: §3.8 of the v0.0.1 `01-DESIGN.md` writes these keys as `farmstorm.<group>.<key>`. By ruling A-04, this document and `12-TECH-0.0.2.md` write them uniformly as `<group>.<key>`, pointing at the same batch of keys (language keys remain `farmstorm.configuration.<group>.<key>`, corresponding one to one with the 117 entries of v0.0.1).

### 10.2 New keys (22 entries, the sole list of BRIEF §8)

Key names are written uniformly as `<group>.<key>` (ruling A-04); language keys are written `farmstorm.configuration.<group>.<key>`. The table below copies BRIEF §8 line by line; 11-DESIGN and 12-TECH may not rewrite it separately.

| # | Key (`<group>.<key>`) | Type | Default | Range | Purpose | Basis |
|---|---|---|---|---|---|---|
| 1 | `molotov.effectRadius` | double | 4.0 blocks | 0.0–16.0 | radius of the landing sphere: every block inside it whose 'top face touches air' rolls on its own | D-58 |
| 2 | `molotov.fireChance` | double | 85.0 % | 0.0–100.0 | chance of fire on each candidate block; **the remaining percentage places a magma block automatically, with no second key** | D-58 |
| 3 | `molotov.triggerExplosionPower` | int | 0 | 0–100 | power of the landing trigger explosion; 0 = a pure landing trigger | D-58 |
| 4 | `wild_apple.nutrition` | int | 4 points | 0–20 | Wild Apple nutrition (takes the same value as the vanilla golden apple) | D-54 + 【本文补充】 |
| 5 | `wild_apple.saturationModifier` | double | 1.2 | 0.0–10.0 | Wild Apple saturation modifier (takes the same value as the vanilla golden apple) | D-54 + 【本文补充】 |
| 6 | `wild_hunter.durationTicks` | int | 700 ticks (35 seconds) | 1–72000 | base duration of the effect; this is the duration Wild Apple grants | line 52 of `plan0.0.2.txt` |
| 7 | `wild_hunter.killExtensionTicks` | int | 160 ticks (8 seconds) | 0–72000 | extension per mob killed while the effect is active; no count cap | D-74 |
| 8 | `wild_hunter.killHealPoints` | double | 10.0 points | 0.0–40.0 | health points restored per kill | D-74 |
| 9 | `wild_hunter.searchRadius` | double | 5.0 blocks | 0.0–32.0 | radius of the target-acquisition sphere | D-64 |
| 10 | `wild_hunter.searchAngleDegrees` | double | 30.0 degrees | 0.0–180.0 | cap on the angle between a candidate and the look direction | D-64 |
| 11 | `wild_hunter.dashTicks` | int | 3 ticks | 1–20 | ticks the snap dash takes to span the distance | D-65 |
| 12 | `wild_hunter.speedBonusPercent` | double | 30.0 % | 0.0–1000.0 | movement speed bonus | line 40 of `plan0.0.2.txt` |
| 13 | `wild_hunter.attackSpeedBonusPercent` | double | 30.0 % | 0.0–1000.0 | attack speed bonus | line 40 of `plan0.0.2.txt` |
| 14 | `wild_hunter.attackDamageBonusPercent` | double | 40.0 % | 0.0–1000.0 | attack damage bonus | line 40 of `plan0.0.2.txt` |
| 15 | `wild_hunter.highlightRadius` | double | 64.0 blocks | 0.0–256.0 | maximum distance of the red silhouette | D-62 |
| 16 | `wild_hunter.loopSoundRange` | double | 16.0 blocks | 0.0–64.0 | audible distance of the looping sound | D-76 |
| 17 | `zangief_cane.triggerRadius` | double | 1.5 blocks | 0.0–8.0 | radius of the trigger sphere (spherical Euclidean distance) | D-73 |
| 18 | `zangief_cane.stageDelayTicks` | int | 14 ticks | 0–200 | duration of phase 0 (inverted, waiting for levitation) | D-69 |
| 19 | `zangief_cane.levitationLevel` | int | level 7 | 1–255 | levitation level; amplifier = level − 1 = 6 | D-69 |
| 20 | `zangief_cane.levitationDurationTicks` | int | 20 ticks (1 second) | 0–72000 | levitation duration = phase 1 duration | D-69 |
| 21 | `zangief_cane.fallTicks` | int | 40 ticks (2 seconds) | 1–200 | target landing duration of phase 2; the server servos the downward velocity every tick so that the landing falls within this duration | line 29 of `plan0.0.2.txt` + 【本文补充】 |
| 22 | `zangief_cane.spinDegreesPerTick` | double | **72.0** degrees/tick | 0.0–360.0 | horizontal spin of phase 2; 72 × 20 = 1440 degrees/second | D-70 |

**22 entries in total.** With the 117 entries of v0.0.1 as the base: `117 + 22 = 139`.

This table is line-by-line identical to §8.1 of `12-TECH-0.0.2.md`; cross-document differences are adjudicated by A-01 ` A-05 of BRIEF §8.1.

### 10.3 Four declarations

1. The 22 entries above are the **sole list** adjudicated by BRIEF §8, and §8.1 of `12-TECH-0.0.2.md` must be line-by-line identical to it (all four items — key name, type, default value, range — aligned); neither side may add or remove entries on its own. The total is 22, and with the 117 entries of v0.0.1 gives `117 + 22 = 139`.
2. The 'Range' column copies BRIEF §8; this document does not give ranges of its own. The comment text of the config entries and the `farmstorm.configuration.*` language keys are defined once, in §8.1 of `12-TECH-0.0.2.md` only.
3. **None** of the new keys changes an existing default of §10.1 (the prohibition of BRIEF §3.4).
4. Relative to the previous version of this document, four changes were made by ruling: `molotov.magmaChance` is withdrawn (the magma block chance becomes the remainder of `molotov.fireChance`, see §5.8); `wild_apple.hunterDurationTicks` is withdrawn (the duration belongs to `wild_hunter.durationTicks` and Wild Apple sets no key of its own, see §8.4); key names uniformly drop the `farmstorm.` prefix (ruling A-04, the existing keys of §10.1 are written the same way); the default of `zangief_cane.spinDegreesPerTick` changes from 24.0 to **72.0** (ruling A-05, see item 6 of §6.8).

---

## 11. Exception and boundary handling list

The numbering follows the style of §7 in `01-DESIGN.md`; this section is renumbered 11.1, 11.2… with 13 items in total. Each item gives only the rule and the basis, without repeating the numbers already given in the individual sections.

### 11.1 Coexistence of the converter recipe and the existing 21 conversion recipes

- **The two kinds of recipe are not the same thing**: crafting belongs to `minecraft:crafting` (outputs one converter block), conversion belongs to `farmstorm:conversion` (settles inside the converter GUI). The new crafting recipe **touches** no conversion recipe (item 5 of §2.6).
- **Counts**: not one of the existing 21 conversion recipes changes; 3 are appended (Molotov, Zangief Cane, Wild Apple) → 24 (BRIEF §4).
- **The fuel tag is not an input-slot whitelist**: the former decides what can craft a converter, the latter is decided by the conversion recipe table and decides what can go into the input slot (`01-DESIGN.md` §3.1.2). An item that is in the tag but not in the recipe table still cannot enter the input slot; conversely, an item in the recipe table but not in the tag is legitimate — the two criteria do not derive from each other.
- **Enchanted golden apples do not work**: `minecraft:enchanted_golden_apple` and `minecraft:golden_apple` are two item ids, the former is not in the `farmstorm:converter_fuel` tag, so putting it in one of the eight cells does not work (【本文补充】: the tag is judged by item id).
- **Burning your own product is legitimate**: `wild_apple` and others are both fuel and conversion output; mistaking one for fuel is the player's own loss, and no UI interception or second confirmation is added (§2.7).

### 11.2 The disagreement over the Melon Grenade failing to blow up a Compressed Hay Block

- **The predicate is nailed down**: `resistance < maxResistance` (BRIEF §3.4 explicitly forbids changing the comparison logic of `BlastRule.canBreak()`). So 'equal' is judged **not broken**.
- **Where the disagreement lies**: this block has resistance 6.0 and the thresholds of the Melon Grenade and the Melon Slice Bomb are also 6.0; 6.0 is not less than 6.0 → **cannot be broken** (D-57). This runs counter to the intuition that an explosion should blow up hay-type blocks; it is a deliberate design outcome.
- **The other side**: the Ubi Server (`UNBREAKABLE_SKIP`) and the Toxic Ubi Server (`VANILLA`) are bound only by the 3.6e6 floor and the two tags, and they **can break** this block (D-78). Vanilla explosions (incl. TNT) **can break it too**: vanilla `Explosion` accumulates per cell `f -= (resistance + 0.3) * 0.3` and breaks at `f > 0`; 6.0 resistance does not stop cobblestone-grade blocks (D-57).
- **The threshold may not be changed for 'feel'**: any change would also change the feel of the Melon Grenade, the Melon Slice Bomb and the existing 4 Demolition crops, which is inside the no-change zone of BRIEF §3.4.
- **A player who wants to dismantle their own foundation**: mine it normally with a diamond pickaxe, or blow it up with the Ubi Server. Both routes are inside the rules; no new recipe or new tool is needed for it.
- **No conflict with 'Construction writing'**: Compressed Puffed Wheat **writes** blocks (§4.5) while the Melon Grenade **blows up** blocks (threshold check); the two paths sit at different layers, and their coexistence creates no contradiction (rows 2 and 4 of the priority table in `01-DESIGN.md` §7.11).

### 11.3 Molotov in the rain / in water

- **Rain**: fire that was written is put out by the rain under vanilla rules; this mod **does no rain-proofing and makes no compensation**. D-58 only prescribes 'placing fire', not 'the fire has to keep burning'.
- **On a water surface**: a water surface fails condition B of §5.7 (`isFaceSturdy` is false), so that cell **takes no roll**; nor is there any 'air position above' inside water, so no magma block appears out of nowhere under water.
- **Landing point in water**: the projectile follows the landing procedure of `01-DESIGN.md` §3.4 and settles as usual, only fewer qualifying blocks are filtered out, possibly zero (§5.11).
- **Landing on the shore next to water**: the land side catches fire as usual; the same Molotov giving two results on different blocks is expected.
- **No 'disabled in rain' branch**: adding one would require reading the weather state and would turn a pure block check into three (rain / thunder / clear), for zero benefit.

### 11.4 The player dies or quits when a Zangief Cane triggers

- **Player death**: the effect hangs on the player entity and disappears with it → the input lock is released and the accumulated camera value is reset (item 4 of §6.8). The block was already silently removed at the trigger tick (D-72), so there is no second trigger. The new entity after respawn inherits no stage.
- **Quitting the game**: the server no longer ticks that player and the phase stays where it is. The whole chain is capped at 74 ticks (3.7 seconds), far shorter than a normal disconnect-reconnect, so **usually** the effect has already expired at reconnection and the client does nothing.
- **Fast reconnection** (【本文补充】): if the player reconnects within 74 ticks, they may come back carrying `amplifier 1` or `2`. The server keeps advancing the phase by the remaining duration; the 'remaining height' of phase 2 is computed afresh from the new position after reconnection (item 2 of §6.10). The client self-checks every tick whether the stage effect is still there and resets itself if not (RT-15).
- **Owner offline**: the Zangief Cane is a trap; the owner only affects the friend-or-foe check (by last known team, `01-DESIGN.md` §7.2) and has nothing to do with whether the owner is online.
- **The view stuck inverted on player death**: it will not be. Camera angles exist on the render layer only (§6.8), and the death screen goes through another rendering path.

### 11.5 A Zangief Cane throwing the player onto the bedrock layer or into the void

- **Rising part**: levitation is done by vanilla `MobEffects.LEVITATION` itself and this mod gives no velocity (phase 1 of §6.6). With a block overhead the displacement is eaten by collision and the actual rise may be 0.
- **Landing back on bedrock**: the bedrock top face is a standable surface, the servo velocity presses the player onto it before tick 40, phase 2 ends early and everything is normal.
- **Falling into the void (the player dug through the bedrock themselves)**: the servo velocity advances by 'remaining height ÷ remaining ticks', with the remaining height taken to the world minimum y. After the 40 ticks the phase ends, the lock is released and vanilla gravity is handed back, so the player can save themselves (ender pearl, water bucket, flight). **No patch that 'force-ends at a y coordinate' is added** — that would introduce a second termination condition unrelated to health.
- **Dying during phase 2** (from fall damage or any other source): it takes the death path of §11.4.
- **The lock cannot last indefinitely**: the end point of the input lock is 'end of phase 2', not 'actually landed'. A lock to the actual landing would leave a player who fell into the void unable to move forever, which must be avoided.

### 11.6 The player going offline during Wild Hunter

- **The effect itself**: the `MobEffectInstance` is kept with the player save and keeps counting down for its remaining duration after reconnection (vanilla behaviour). The count is neither paused nor accelerated by being offline.
- **Looping audio**: a client `SoundInstance`, destroyed with the session; on reconnection the client rebuilds it according to 'is the effect still there' (D-76; the server does not re-send).
- **Greyscale and red silhouette**: derived rendering, persisting no state; after reconnection they resume automatically according to whether the effect exists (D-50).
- **Interrupted dash**: a dash lasts only 3 ticks (D-65) and does not span a login; a dash unfinished at disconnection dies with the session, with no compensation.
- **No extension for being offline**: extension is triggered by **kills** only (D-74); there is no combat while offline, so the effect is not lengthened by it.
- **Attribute modifiers**: saved with the player data and restored automatically by vanilla attribute sync after reconnection; if lost through an anomaly, the cleanup logic at effect expiry still tries to remove them by identity (removing a modifier that does not exist is a no-op and raises no error).

### 11.7 Wild Hunter stacking with other speed-type effects

- **Same Operation: sum first, then multiply once**: the amounts of several `ADD_MULTIPLIED_TOTAL` modifiers on the same attribute are added up first and the total is multiplied once. So Speed II (+40%) and Wild Hunter (+30%) present together give **×1.70**, not ×1.82 (item 3 of §7.5).
- **Order across Operations**: the fixed points of `ADD_VALUE` are summed into the 'base value' side first, and `ADD_MULTIPLIED_TOTAL` multiplies last, uniformly. So when another mod gives a fixed +0.05 blocks/tick, addition comes before multiplication and the result differs from stacking pure percentages.
- **Haste does not clash**: haste acts on mining speed, not attack speed, and has no intersection with the three bonuses of this section.
- **Exact removal**: the three modifiers carry a fixed identity (name + UUID) and are removed by identity when the effect expires / is cleared / the player dies, **without wrongly deleting** modifiers that potions, equipment or other mods put on the same attribute (item 4 of §7.5).
- **Only one cleanup path**: all three ways of ending (duration exhausted, `/effect clear`, death) call the same piece of cleanup; a residue in which 'the attributes are still there after death' is not permitted — that is the hardest class of bug to track down.
- **Timing with food / potions**: the one eaten first settles first and the one eaten later settles later, but the result depends only on 'which modifiers exist at the same instant', not on the order.

### 11.8 The red highlight on invisible players

- **It is drawn**: invisibility (`MobEffects.INVISIBILITY`) affects rendering only and does not affect `hasLineOfSight`, so an invisible enemy player **is still** drawn as a red silhouette.
- **Reason**: D-62 gives only two filter conditions — 'visible within line of sight (no block in the way)' and 'distance ≤ 64 blocks' — plus excluding yourself and teammates. There is no invisibility exemption, and none is added unilaterally (the 'add no conditions of your own' wording of BRIEF §6).
- **It does not count as wallhacking**: the highlight still requires no block in the way, so hiding behind a wall means it is not drawn (D-62 states 'never through walls'). It therefore gives 'marked red when seen', not a 'whole-map radar'.
- **Spectators are not drawn**: spectator players do not enter the highlight set (aligned with the spectator exclusion of D-64 target acquisition), so spectator views are not flooded.
- **Teammates are not drawn**: the ally check goes through `TeamResolver` (`01-DESIGN.md` §3.6); when a team changes in combat, the next rendering decides immediately by the new team — no caching.
- **A different set from target acquisition**: target acquisition is a 5-block sphere + 30° cone (§7.8) while the highlight is 64 blocks omni-directional. The only thing the two share is 'exclude yourself and teammates'; do not merge them into one set.

### 11.9 The dash target dying mid-dash

- **The displacement is not reclaimed**: the 3-tick dash has happened and the player arrives where it arrives. Displacement is a velocity, not a teleport; reclaiming it would be a teleport and would violate D-65.
- **The target is re-resolved at settlement**: on the settlement tick the server computes the target once more with the algorithm of §7.8; if the target no longer exists (dead / removed / out of range) → **no damage and no knockback** are settled this time, and no further chase happens.
- **No target switching**: the ordering is computed once at the trigger tick and is not re-ordered on the way. So 'the enemy who was second in line' is not hit just because the first one died — that would make 'who gets hit' unpredictable.
- **A landing point occupied by the dead body is not a failure**: the failure condition of D-65 is 'the landing point is occupied by a **block**'; a corpse does not block a block position, so it does not affect the check.
- **Kill extension counts only a real kill**: +160 ticks / +10 points only when the killer really is this player (D-74); a target dying from another source (fall damage, a teammate finishing it off) does not extend.

### 11.10 The looping audio clashing with jukeboxes / other mods' sounds

- **Category isolation**: the looping sound goes through `SoundSource.PLAYERS` (D-76) and the jukebox through `SoundSource.RECORDS`; the two volume sliders in settings are independent and neither ducks the other.
- **It does not take MUSIC**: so it **does not interrupt** vanilla background music (the explicit requirement of D-76). That is the only reason `PLAYERS` was chosen over `MUSIC`.
- **Several instances coexist**: when two players hold Wild Hunter at the same time, both looping sounds exist, each following its own owner; they are not mutually exclusive, not merged and not noise-gated. 【本文补充】
- **16-block attenuation**: beyond 16 blocks the sound falls off linearly to inaudible under vanilla rules. Whether 1.21.1 achieves this by clamping `getVolume` or by the audible-distance hook is an implementation-phase measurement item (the open point of BRIEF §7.7); the document fixes only the number '16 blocks'.
- **Stop has exactly three triggers**: the effect disappears, the player dies, the player quits the world. The server does not re-send play / stop instructions every tick (D-76), so there is no state split in which 'the server and the client each own half'.
- **Conflicts with audio mods**: no runtime detection and no degradation (the same handling as RT-16); if a third-party mod has taken over sound categories, it wins and this mod does not fight for them.

### 11.11 How the Lucky Berry pool excludes Wild Apple

- **Size of the pool**: 19 profiles minus 3 = **16** (BRIEF §4). The three are `lucky_berry` (D-55), `wild_apple` (D-55) and `compressed_puffed_wheat` (D-56).
- **Two decisions must be read together**: D-55 names only two exclusions, and D-56 separately rules that Compressed Puffed Wheat does not enter the pool. Only the two together give 16; reading D-55 alone gives 17.
- **Why Wild Apple is excluded**: it is the strongest item of this version and should not be produced at random by 1 berry (the reasoning of D-55). This is what makes 'rolling a Wild Apple' impossible, as registered in §8.6.
- **Implementation wording**: the pool must be an **explicitly enumerated** constant list; it may not be written as the runtime derivation 'all profiles − Lucky Berry' — that would silently widen the pool with every profile added (D-55 explicitly replaces automatic derivation with explicit exclusion).
- **Effect on the existing 14**: all 14 entries of v0.0.1 are kept, not one missing; 2 are new (Molotov, Zangief Cane), so 14 → 16.

### 11.12 How the converter fuel tag interlocks with the 19 profiles

- **The tag is data**: `farmstorm:converter_fuel` lives in item tags, its contents are the 36 entries of §2.5. It is the **only** executable definition of 'what counts as a crop'.
- **Relation to the profile table**: when a `CropProfile` is added, it must be synchronised into the tag unless it is explicitly judged a 'tier-2 composite' like Compressed Puffed Wheat. What a missed entry looks like: 'the player cannot obtain a converter' — no error, no crash, the hardest kind to notice.
- **The three classes that stay out of the tag**: seed items, `compressed_puffed_wheat`, `compressed_hay_block` (last paragraph of §2.5).
- **Two different tables, alongside the conversion recipe table**: being in the tag ≠ being convertible. The tag governs crafting, the recipe table governs conversion (item 3 of §11.1).
- **Not synchronised with the Lucky Berry pool**: the pool excludes 3 entries and the tag excludes 3 entries, but not the same objects (the pool excludes the Lucky Berry itself, the tag excludes Compressed Puffed Wheat and the seed items). Do not derive one table from the other.

### 11.13 Full-screen greyscale and shader mods (RT-16)

- **No detection**: this mod does **no** runtime detection of shaders (Oculus / Iris and the like) and has no branch that 'turns greyscale off when one is detected'.
- **Registered as a known incompatibility**: full-screen greyscale runs one more full-screen pass at the end of the render pipeline, and shader mods occupy the same stretch of pipeline too; the possibility that the two overwrite each other is real. Under the wording of RT-16 this goes into the compatibility notes of the release and is not handled as a runtime issue.
- **Degradation path**: the fallback of RT-10 is 'one pass for the world + one pass for the GUI'; if that is unusable too, degrade one more step per RT-10 (grey the world only) and register the degradation in the Release notes. None of the three cases adds a packet.
- **D-61 is not changed because of compatibility**: 'including the GUI' is a hard requirement and does not fall back to 'grey the world only' just because some shader pack does not support it.

---

## Appendix A · Register of differences between plan0.0.2.txt and this document

### A.1 Item-by-item comparison (7 requirements + 1 supplement)

| # | Key point of the plan text | Treatment in this document | DEC ids | Nature of the difference |
|---|---|---|---|---|
| 1 | Converter crafting: the example is 'wheat ×8 surrounding a crafting table' | Generalised to the item tag `farmstorm:converter_fuel` (36 entries), the eight cells may be mixed, the centre must be a vanilla crafting table | D-52 | Refinement: from 'one particular crop' to 'one tag' |
| 2 | Molotov: a power-0 explosion at the landing point; within radius 4 blocks, every block whose top face touches air has 85% fire and 15% magma block | The explosion is degraded to a pure trigger (0 damage, 0 destruction); 85/15 rolls independently per block, and the result is written into the **air position above the block** rather than the block itself; the radius is cut as a Euclidean sphere | D-58, D-19, D-48 | Refinement + tightened wording (write position, roll granularity) |
| 3 | Zangief Cane: upside-down flip; after 14 ticks a levitation-7 effect for 1 second; counter-clockwise at 1440 degrees/second before landing; lands within 1.7–2 seconds; keyboard and mouse dead; play a specified mp3 | roll 180 + inverted pitch; amplifier 6 / 20 ticks / applied on tick 14; 72 degrees/tick (see A.4); servo velocity lands in 40 ticks; five-event input lock keeping only ESC; audio converted to ogg and packaged | D-59, D-69, D-70, D-60, D-72, D-73, D-66 | Refinement + wording supplement for implementation (how the landing happens) |
| 4 | Compressed Hay Block: values reference ancient debris, texture is a black-and-white version of the hay block | Takes 30.0 hardness / 6.0 resistance, `requiresCorrectToolForDrops`, six non-rotatable faces, diamond pickaxe, two vanilla tool tags | D-57, D-78 | Refinement: BRIEF fixes the values directly |
| 5 | Compressed Puffed Wheat: crafted from 4 Puffed Wheat, attributes identical to Puffed Wheat, generated block switched to the compressed hay block | Shapeless 4 → 1; landing-write rules carried over from Puffed Wheat item by item; **no explosion stage** (see A.5); does not enter converter recipes or the berry pool | D-67, D-77, D-56 | Refinement + wording adjustment |
| 6 | Wild Hunter: three attributes +30%/+30%/+40%, black-and-white view, red highlight, the mob within 5 blocks closest to the crosshair, snapping, knockback, a 16-block looping sound, +8 seconds and +10 health per kill | Each item landed as D-61 ` D-76; 'snapping' changed to a 3-tick dash (see A.6); the highlight gains 64 blocks and no going through walls; target acquisition gains a 30-degree angle and exclusion of spectators | D-61, D-62, D-64, D-65, D-74, D-75, D-76 | Refinement + change of implementation wording (snap → dash) |
| 7 | Wild Apple: eating grants 35 seconds of Wild Hunter | Bidirectional conversion (golden apple ⇄ Wild Apple, 3 experience, no catalyst); nutrition 4 / saturation 1.2; eating 32 ticks; does not enter the berry pool | D-54, D-55, D-51 | Refinement |
| 8 | Supplement: all audio and image files packaged into the mod itself | Audio converted to mono / 64 kbps Ogg Vorbis and icons to 18×18 PNGs, then into `src/main/resources`; the script errors out when `ffmpeg` is missing; provenance registered in the README and the Release notes | D-53, D-66 | Refinement |

### A.2 Details whose wording was explicitly changed

| Id | Point in the plan text | How this document lands it | DEC ids |
|---|---|---|---|
| A-1 | 'place a magma block' does not say where | Written at 'the air position above that block', `pos.above()`, the same position as fire, the two mutually exclusive; the original block is not replaced (§5.9) | D-58 |
| A-2 | 'place fire' likewise | Same as A-1. The landing rules of fire and magma blocks must agree, otherwise one settlement would carry two coordinate semantics | D-58 |
| A-3 | 'flip the view upside down' only says flipped | Fixed as **roll 180° and inverted pitch taking effect together**; doing only one of them is wrong (item 1 of §6.8) | D-59 |
| A-4 | '1440 degrees per second' was once written by the old fact card's parenthetical arithmetic as '24 degrees per tick' (1440 ÷ 20 miscomputed as 24; the correct value is 72) | Landed according to the plan text's 1440 degrees/second: `zangief_cane.spinDegreesPerTick` defaults to **72.0** degrees/tick, accumulating 2880° = 8 whole turns over 40 ticks (item 6 of §6.8, BRIEF §8.1 ruling A-05) | D-70 |
| A-5 | Compressed Puffed Wheat 'attributes identical to Puffed Wheat' — but Puffed Wheat has no explosion stage, and item 5 of the plan does not mention an explosion either | Landed by the two rules 'landing write + resistance floor (3.6e6)'; the explosion power and damage cap parameters are **not set** (item 5 of §4.5) | D-77 (amended to 'carries over the landing write, not the explosion'), D-78 |
| A-6 | 'snap next to the target' | Changed to a **3-tick dash**: initial velocity from the distance, stops on hitting a wall, no snapping, no ghosting through; if the landing point is occupied by a block there is no snap this time (§7.9) | D-65 |
| A-7 | the plan does not mention the Lucky Berry pool at all | The pool is fixed at 19 − 3 = **16**, excluding `lucky_berry`, `wild_apple` and `compressed_puffed_wheat` explicitly (§11.11) | D-55, D-56 |
| A-8 | 'within a radius of 1.5 blocks' does not say which metric | Fixed as **spherical Euclidean distance**, a departure from the convention that new checks default to Chebyshev, which is why it gets a row of its own (item 2 of §6.5) | D-73 |
| A-9 | the audio is given as a local absolute path `D:\dsh\default\sf6_audio\out\BV1qu411W7ha_846-850.mp3` | Converted and packaged into the jar; no external path is read at runtime; the Zangief Cane deployed block is **completely invisible** in the world and keeps only the particle texture (§6.2) | D-66, D-68, D-53 |
| A-10 | 'a non-ally player comes near' | Only non-ally players count, with creative mode and spectators explicitly excluded (item 3 of §6.5) | D-71 |
| A-11 | the plan does not say whether Compressed Puffed Wheat counts as a 'crop' | Fixed as a **tier-2 composite**: no converter recipe, no berry pool, no crop block and no seed (§4.3) | D-56 |
| A-12 | Molotov's 'corresponding vanilla crop: crimson fungus' — a crimson fungus is not strictly a crop | Accepted as its vanilla reference and written into the fuel tag so that both sides of 'vanilla ⇄ improved' can serve as fuel (§2.5) | D-52 |

### A.3 The parts left unchanged

Outside this table, the rest of plan0.0.2.txt is copied verbatim by this document: the Chinese names and exclamation marks (莫洛托夫！), the item tooltip originals (they go into `item.farmstorm.*.tooltip` and are not treated as advancement titles), the spaces in the effect name (狂 野 猎 人), the percentages of the three attribute bonuses, the 85 / 15 probabilities, levitation level 7, the 1.7–2 second landing window, the 16-block sound travel distance, the 8-second extension and 10-point restore, and the exclusion wording 'ignore ally and same-team players'.

Wherever this document departs from the plan, the departure is named in A.1 or A.2 with a DEC id; a departure without a DEC id is a document defect.

---

## Translator's notes

1. The metadata row 'Language' is carried over verbatim from the source (and matches `docs/en/01-DESIGN.md`), even though this body text is English.
2. The effect name 狂 野 猎 人 is reproduced with its original layout: 4 characters with one space between each, 3 spaces in total, as §7.2 states.
3. §1.2 itemises the two effects in separate rows (row 3 '1 hidden effect', row 6 '1 visible status effect'), while §1.3 writes '2 (1 visible + 1 hidden)' in a single cell; both are kept as written.
4. §5.4 and §5.5 cite 'ruling A-10' (BRIEF §9.1), while table A.2 also carries a row labelled A-10 (the non-ally player point). The two labels belong to different numbering namespaces and are kept as written.
5. Terminology: 农产品转换器 is rendered 'Produce Converter' per BRIEF §9.1, which matches `docs/en/01-DESIGN.md`.
6. 'Cobblestone-grade' in §3.5 and §11.2 refers to the 6.0 blast resistance explained in the §3.4 measurement table.
7. The three tooltip lines of §9.1 are translated by meaning with the Chinese original in parentheses, because they are the author's own words in `plan0.0.2.txt`: 'It's Lit! It's Lit!' (「火了！火了！」), 'Take my Hand-Cranked 360!' (「吃我一个手摇重360！」), 'I Am Blot Handel!' (「我是布洛特·亨德尔！」). The name in the third line is transliterated.
8. Cross-references 'A.4', 'A.5' and 'A.6' inside A.1 point at rows A-4, A-5 and A-6 of table A.2; both spellings are kept as in the source.
9. The `【D-xx】` / `【P-x】` / `【本文补充】` markers are kept verbatim: they are the document's source-attribution tokens, not prose.
10. The startup self-check line in §1.3 is reproduced verbatim in Chinese because it is a log line.
