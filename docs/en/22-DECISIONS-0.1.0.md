# 22 · v0.1.0 Decision Record

> This file is the single source of truth for 'Farmstorm' v0.1.0: the decision numbering continues `16-DECISIONS-0.0.3.md` from D-102, and this release covers **D-103 ~ D-123, 21 entries in total**, consecutive with no gaps.
> The baseline is the **implemented and released 0.0.4** (`dist/farmstorm-0.0.4+mc1.21.1.jar`, 959,376 B). The existing clauses of v0.0.1 / v0.0.2 / v0.0.3 still follow `00-DECISIONS.md` / `10-DECISIONS-0.0.2.md` / `16-DECISIONS-0.0.3.md`; this file covers only the new content of v0.1.0, plus the changes to 0.0.4 (§4).
> Source: the author's original text `plan0.1.0.txt` (51 lines, 9 requirements) + the build fact card `.work/v010-BRIEF.md` (**252 lines**, every item carrying `file:line` evidence). The build fact card is a flattened working draft of the build period and is not a deliverable.

---

## 0. Reading conventions

| Item | Convention |
|---|---|
| Force | Once these 21 entries are registered they are the final scope for v0.1.0. `23-DESIGN-0.1.0.md`, `24-TECH-0.1.0.md`, `25-TASKS-0.1.0.md`, `26-PROMPT-MAIN-0.1.0.md`, `27-PROMPT-TASKS-0.1.0.md` and the code of the implementation phase must all agree with this file; where they conflict, this file wins |
| Numbering continuation | Decisions start at **D-103**, tasks at **F-136**, Phases at **21**, milestones at **M17**, hard constraints at **HC-51**, forbidden behaviours at **N-51**, risks at **RT-24**, adjudications at **A-19**, implementation verdicts at **V-160** |
| The four-field body | Each decision has the four fixed items "**ID / Decision / Rationale / Where it lands**", plus a fifth item "**Supplement**" only when it is really needed (optional, and not used as a patch — a real patch gets an entry of its own, the same way as §3.6 of `16-DECISIONS-0.0.3.md`) |
| Marking of the evidence | For values and assertions that can be read straight off the build fact card, the citation is written "BRIEF §x" and the in-line evidence is transcribed; content that the build fact card does not give directly and that this file derives or reads out of the source code is marked "**supplemented by this document**" |
| Number discipline | The counts appearing in this file are of only three kinds: **verified** (117 / 90 / 155 / 299×2 / 22 / 18, from the 0.0.4 delivery record), **incremental** (+3 and its breakdown, config +11 and its breakdown), and **converted values obtained by subtracting or multiplying two verified numbers** (such as 52 − 39 = 13, 3 × 16 = 48), the conversion being written out place by place in §8 |
| Total number of registry objects | This file records only **90 + 3**. **The total of 93 is confirmed by `24-TECH-0.1.0.md` itself, which checks it against the source code class by class and names each one; this file records only 90 + 3.** (0.0.2 and 0.0.3 both came to grief in this very cell; see the note under the table of BRIEF §3.1) |
| Forbidden wording | No open items, no to-do markers, no conclusions with vague conditions; when a judgement is needed and the build fact card does not give it directly, it is derived from the existing decisions and marked "supplemented by this document" rather than written as a vague conclusion |
| What the "farmland rule" is called | In this release the farmland quota is the **`/farmstorm farmlandlimit <n>` subcommand + the config default**; it is not called a gamerule. The farmland rule is nowhere in this file written as a gamerule |
| Citation style | A reference to an existing document is written with the file name, in the form `16-DECISIONS-0.0.3.md` §5.3; a reference to source code is written as a path relative to the repository root + a line number; a reference to the build fact card is written in the form "BRIEF §2 item 14", with the item number |
| Relation to the source code | Where the build fact card conflicts with the source code, the source code wins and the author is reported to. The facts checked one by one while this file was written are listed in the "re-check" column of §8; no trade-off conflict that needed reporting was found |

---

## 1. Positioning and boundaries of this version

### 1.1 Version and baseline

| Item | Value | Evidence |
|---|---|---|
| Version | 0.1.0 | `plan0.1.0.txt:2` |
| Minecraft | 1.21.1 | `gradle.properties:2` |
| NeoForge | 21.1.213 | `gradle.properties:3` |
| Java | 21 | `gradle.properties` |
| Build entry point | `.tools\gw.cmd` (**the only entry point**; calling gradle directly is not allowed) | repository convention |
| Baseline | **0.0.4, implemented and released** (959,376 B) | `dist/farmstorm-0.0.4+mc1.21.1.jar` |
| Java files | 117 | recursive count under `src/main/java` |
| Registry objects | **90** | 0.0.4 delivery record + the self-check of `Farmstorm.java:103-104` |
| config keys | **155** | 0.0.4 delivery record |
| Language keys | **299 × 2** | 0.0.4 delivery record |
| Crop profiles | **22** | `CropProfile.ALL` (`Farmstorm.java:103-104` asserts 22) |
| Advancements | **18** | 0.0.4 delivery record |
| The two red lines | **0 custom network packets**, **no Mixin** | inherited since 0.0.1 and maintained in this release (§7) |

### 1.2 The 9 items this version does and the decisions that answer them

| # | Requirement (`plan0.1.0.txt` line number) | Decision | Nature |
|---|---|---|---|
| 1 | Raise the charge bar (:7-10) | D-103 | change one constant |
| 2 | Friend Chives planted-stage textures (:11-14) | D-104 | replace 4 images + change the generator |
| 3 | Puffed Wheat / Compressed Puffed Wheat no longer overwrite ordinary blocks on landing, and the Compressed Puffed Wheat model becomes 3D (:15-19) | D-106, D-107 | change the predicate + replace the model |
| 4 | Melon Grenade and Melon Slice Bomb lose Demolition, the landing points get denser (:20-24) | D-108, D-109 | change the category + change three numbers |
| 5 | Crop growth 20~45 seconds (:25-28) | D-110, D-111 | **mechanism replacement** |
| 6 | The four trap adjustments (:29-35) | D-112 ~ D-115 | extend the mechanism |
| 7 | Farmland quota and team summation (:36-40) | D-116 ~ D-118 | **open a new pipeline** |
| 8 | The new block "Outpost Core" (:41-47) | D-119 ~ D-122 (the body), D-118 (the range convention), D-123 (the assets) | new registries +3 |
| 9 | Wild Apple texture (:48-51) | D-105 | replace 1 image |

Of the 9 rows above, D-112 ~ D-115 belong to item 6, D-116 ~ D-118 to item 7 and D-119 ~ D-122 to item 8; item 8 depends at the same time on D-118 (the range convention) and D-123 (the assets).

### 1.3 Boundaries

- **There is only one new surface**: the Outpost Core (`blocks` +1, `block_entities` +1, `items` +1, +3 in total, 90 → 93). No crop profile is added, and no `entity_type` / `mob_effect` / `sound_event` / `damage_type` / `recipe_type` / `menu_type` is added.
- **The changed surface is registered item by item in §4.2** (the two tables total **19 rows**: 5 rows of numbers and assertions + 14 rows of other code surface): one startup self-check assertion, one hard-coded constant, two config defaults, the expected values of one static acceptance script, and the rest are the implementation landing points that the individual decisions bring along (growth advancement, the write predicate, trap scheduling, the two new classes of the Outpost Core, the command and the trample subscription, the generator).
- **The changed-profile surface has only two entries** (§3.1): `melon_grenade` and `melon_slice_bomb` lose `DEMOLITION`; the other 20 entries change by not one character.
- **The mechanism-replacement surface has only one entry**: crop growth goes from the vanilla random tick to deterministic advancement (D-110, D-111, RT-24). It acts on `FarmstormCropBlock` only; not one vanilla crop is changed.
- **The two red lines**: 0 custom network packets (boundary particles and dust go through the vanilla particle packet), no Mixin (the new command is registered with Brigadier, and no injection is introduced for a gamerule).
- **Delivery size**: 6 files each in Chinese and English (22 ~ 27) + README, 12 in total (BRIEF §2 item 12, §5). The order is 22 serial → 23 and 24 in parallel → 25 → 26 and 27 → the English side translated as a whole once the Chinese is frozen.

---

## 2. Decisions D-103 ~ D-1xx

The numbers this version actually uses are **D-103 ~ D-123, 21 entries in total**, consecutive with no gaps. Grouping: §2.1 visuals and textures 3 entries, §2.2 landing-block behaviour 2 entries, §2.3 demolition and explosions 2 entries, §2.4 growth 2 entries, §2.5 the trap system 4 entries, §2.6 farmland and the outpost 7 entries, §2.7 assets 1 entry.

### 2.1 Visuals and textures (D-103 ~ D-105)

#### D-103 · Wild Hunter charge bar raised: `BAR_Y_FROM_BOTTOM` 39 → 52

- **ID**: D-103
- **Decision**: `WildHunterHud.BAR_Y_FROM_BOTTOM` goes from **39** to **52**, that is, the whole bar moves up by 13 GUI pixels. The vertical coordinate is still `int y = screenHeight - BAR_Y_FROM_BOTTOM`, and **no** second, resolution-adaptive formula is introduced. The bar width 20, height 4, gap 4, total width 44, the centring algorithm, the four colours and the cell-by-cell fill order all stay as they are.
- **Rationale**: the current state is `y = screenHeight - 39` (`client/WildHunterHud.java:54`), which sits exactly on the **health / hunger rows** (the health / hunger band is roughly **[30,39)** from the bottom) and clear of the experience bar (**[24,29)**); it is 12 px away from the hotbar (**[0,22)**) and **does not sit on the hotbar**. The author's words are "raise the dash charge bar a little, so that the health bar and the hunger bar do not hide it" (`plan0.1.0.txt:9`); 52 is the author's adjudicated value (BRIEF §2 item 14). The numbers of the four bands, and "the charge bar including its outline occupies **[34,40)** (body [35,39), outline 1 px above and below)", come from the HUD measurement of `23-DESIGN-0.1.0.md`. This entry looks only at the vertical coordinate and has nothing to do with the drawing time (`RenderGuiEvent.Post`, `:39-40`), the scaling convention (`graphics.guiHeight()` / `guiWidth()`, `:49-50`), the number of bars or the source of the charge progress, so the changed surface is a single constant. 13 = 52 − 39 is the displacement this document obtains by subtracting the two numbers (supplemented by this document) and does not constitute a new constant.
- **Where it lands**: `client/WildHunterHud.java:26` (the constant); §4.2 row 4; §5.1 row 1.
- **Supplement**: `HOTBAR_WIDTH = 182` (`:21`) is still a constant that is declared and never used (BRIEF §1.1), and this release leaves it alone — it is unrelated to the observable effect of this entry, and deleting it would be a change unrelated to the requirement (§6 item 10).

#### D-104 · The four Friend Chives stages: 16×16 placeholders drawn by hand, and **the generator changed as well**

- **ID**: D-104
- **Decision**: the four `friend_chives_stage0..3.png` go from pure magenta placeholders to four-stage **16×16** textures drawn by hand in the style of the existing crops; **at the same time** the generator changes — in `tools/gen_textures.py` these 4 no longer go through the single-colour fill of `PLACEHOLDER_COLOR` but through a dedicated branch that draws stage by stage, so that the output of "re-running the generator" matches the checked-in files and cannot fall back to magenta. The stage shapes follow the four-level progression of `AGE_3` (`MAX_CUSTOM_AGE = 3`). The model and the render type are unchanged (`parent: minecraft:block/crop` + `render_type: cutout`), and the language key and the registry id are unchanged.
- **Rationale**: all four current images are 16×16 pure magenta `#D81E8A`, and the magenta is **something the generator wrote in** — `PLACEHOLDER_COLOR = (0xD8,0x1E,0x8A,0xFF)` at `tools/gen_textures.py:64`, `make_placeholder()` at `:512-514` and the loop at `:659-666` (BRIEF §1.2). Replacing only the PNGs without touching the generator means the next re-run overwrites them back to magenta, and that is the only reason this entry has to touch code. The style is modelled on two sets of existing crop textures in the same directory: `power_beet_stage0..3` (16×16, 4 colours, about 251/256 transparent) and `ubi_server_stage0..3` (9 colours, 175/256 transparent) — both use only single-digit colour counts within 16×16 and are largely fully transparent, which shows that the readability of this mod's crops rests on the outline rather than on gradients. This round is a hand-drawn placeholder, to be replaced by the author later (BRIEF §2 item 11).
- **Where it lands**: `tools/gen_textures.py` (the dedicated drawing branch for these 4; `PLACEHOLDER_COLOR` is kept for the other placeholder items); `assets/farmstorm/textures/block/friend_chives_stage0..3.png` (the 4 replaced in place, §3.3).
- **Supplement**: the dedicated branch decides the seedling height and the number of leaves from the **stage index** and **uses no random numbers** (supplemented by this document): randomness would make every re-run produce different output, and the criterion "re-running equals what is checked in" would stop holding.

#### D-105 · Wild Apple texture replaced: 1294×1216 → 16×16

- **ID**: D-105
- **Decision**: `textures/item/wild_apple.png` is replaced by the **16×16** finished product obtained by scaling the 1294×1216 RGBA PNG from the desktop. The model's `layer0 = farmstorm:item/wild_apple` is unchanged, no second image is added, and the item name, the language key and the registry surface are not changed.
- **Rationale**: the current texture is 16×16, **11 colours**, 126/256 transparent (BRIEF §1.11; **12 is a common misremembering of 11 colours + the transparent entry**, and document 23 §9.4 lists them colour by colour as 11 as well); the new asset is 1294×1216 RGBA, 1,113,442 B, and it must be scaled before it enters the jar (BRIEF §2 item 21). The size is set by the current texture: 16×16 is the only reasonable specification for the `item/generated` model, and a 28-fold reduction cannot preserve, on a single 16×16, any detail beyond the 11 colours, so the finished product can only be resampled on the 16 grid and the original image cannot be put straight into the jar (1,113,442 B differs from the hundred-byte scale of a 16×16 by three orders of magnitude).
- **Where it lands**: `assets/farmstorm/textures/item/wild_apple.png` (replaced in place, §3.3); the asset source and the non-commercial personal-use registration follow D-66.
- **Supplement**: the concrete steps of scaling and quantisation (downsample first or hand-fix first, how many colours the palette takes) belong to the asset list of `24-TECH-0.1.0.md`; this document fixes only two points, the finished specification 16×16 and "the model reference must not be changed".

### 2.2 Landing-block behaviour (D-106 ~ D-107)

#### D-106 · The Puffed Wheat landing block replaces air and fluids only

- **ID**: D-106
- **Decision**: the landing write **layers a whitelist on top of** the existing "resistance ≥ 3.6e6 → skip": the target position must be **air** or **a block in the fluid state** for the write to happen; ordinary blocks, block entities and blocks that cannot be replaced are all skipped. The predicate becomes the config switch `compressed.replaceOnlyAirAndFluid` (boolean, default **true**): switched off, it returns to the 0.0.4 behaviour of "skipping only resistance ≥ 3.6e6", for comparison and for the server owner to choose. The existing resistance floor is kept, and the new predicate is **layered on** rather than a replacement. The two projectiles (Puffed Wheat and Compressed Puffed Wheat) share the same write implementation, so changing one place takes effect on both at once.
- **Rationale**: the only interception in the current loop is `getExplosionResistance() >= BlastRule.UNBREAKABLE_RESISTANCE` (`entity/thrown/PuffedWheatProjectile.java:106`, the 3.6e6 of `explosion/BlastRule.java:26`), and there is **no** `canBeReplaced` / `isAir` / fluid / block-entity judgement at all (BRIEF §1.3) — an ordinary block's resistance is far below 3.6e6, so it is overwritten whole, and that is the bug this entry fixes. The author's semantics are a single sentence: "it can no longer replace ordinary blocks, but it can still replace liquid blocks and air blocks" (`plan0.1.0.txt:17`); the whitelist holds only air and fluids, with no third kind. The resistance floor cannot be deleted: it is the anti-destruction floor of D-38 / T-05 (not a balance value), and deleting it would let Puffed Wheat overwrite bedrock-class blocks again. It is made a config switch rather than hard-coded because "what gets overwritten" is a gameplay-side predicate, of the same kind as the thresholds of the `BlastRule` family, and therefore a server-owner knob (supplemented by this document).
- **Where it lands**: `entity/thrown/PuffedWheatProjectile.java:99-113` (the pre-write judgement of `fillHay`); `entity/thrown/CompressedPuffedWheatProjectile.java:35-37` (it rewrites only `fillBlock()` and inherits the same judgement, so it is unchanged); `compressed.replaceOnlyAirAndFluid` of `config/FarmstormConfig.java` (§5.2 row 12).
- **Supplement**: the cube edge length of the write, the landing-point semantics and the "write rather than explode" nature all stay as they are (the boundary convention of D-58 remains in force); how the predicate is actually written (air judged before fluid or the other way round, `getFluidState().isEmpty()` or some other combination) is pinned down by `24-TECH-0.1.0.md`, and the semantic convention follows this entry.

#### D-107 · Compressed Puffed Wheat: item model becomes 3D, landing block distinguishes six faces, greyscale changes to yellow-brown

- **ID**: D-107
- **Decision**: four things —
  1. the item model goes from `item/generated` (a single `layer0`) to a **3D block model**, taking the landing block's model directly as its parent, so that it shows as a small block in the inventory;
  2. the landing block's model goes from `cube_all` to **six distinguished faces**: three textures for top / side / bottom, with the model using `cube_bottom_top`;
  3. the texture tint goes from **fully greyscale to yellow-brown**: the existing 1 greyscale texture is recoloured in place, and 2 more of the same 16×16 specification are added for top / bottom;
  4. the block properties change by not one character: `strength(30.0F, 6.0F)`, `requiresCorrectToolForDrops()`, `sound(SoundType.GRASS)`, `mapColor(MapColor.COLOR_BROWN)`.
  The block is no longer "the same material on all six faces" — this voids the second half of D-57, registered in the D-57 row of §7.2.
- **Rationale**: at present the item is flat 2D (`item/generated` + `layer0: farmstorm:item/compressed_puffed_wheat`) and the landing block is `cube_all` with a single texture and **fully greyscale** (9 colours: `#8D8D8D` / `#7B7B7B` / `#A3A3A3` / `#707070` / `#ADADAD` …), BRIEF §1.3. BRIEF §2 item 13 adjudicates "the item model becomes 3D + the landing block gets a redone model (new textures + six distinguished faces)". The 3D item model reuses the landing block's model instead of drawing a separate inventory-view image because the Compressed Puffed Wheat itself is that block, and a separate image would mean two sets of textures to maintain in step (supplemented by this document). The six-face distinction changes only the textures, not the shape or the collision box, so `cube_bottom_top` is enough and no custom uv is needed (the same direction as the conclusion of RT-29). The reason for greyscale → yellow-brown: within the same block family the `puffed_wheat` series is straw-yellow, yet Compressed Puffed Wheat, its second-stage form, is grey, and the colour itself hints at the wrong material (supplemented by this document; the hexadecimal values are given by the asset steps of `24-TECH-0.1.0.md`).
- **Where it lands**: `assets/farmstorm/models/item/compressed_puffed_wheat.json` (change the parent); `assets/farmstorm/models/block/compressed_hay_block.json` (`cube_all` → `cube_bottom_top`); `assets/farmstorm/textures/block/compressed_hay_block.png` (recoloured in place) + the newly added `_top` / `_bottom`; the resource table of §3.3.
- **Supplement**: the block id, the item id, the shapeless crafting recipe, the loot table and the property lines of `registry/FarmstormBlocks.java:71-78` all stay as they are; the old wording "the six faces cannot be rotated" in `16-DECISIONS-0.0.3.md` is registered as void per §7.2, but **document 16 is not edited back** (it is frozen, the D-80 convention).

### 2.3 Demolition and explosions (D-108 ~ D-109)

#### D-108 · Melon Grenade and Melon Slice Bomb lose `DEMOLITION`: they can break no block at all, the damage stays

- **ID**: D-108
- **Decision**:
  1. the category set of `melon_grenade` and `melon_slice_bomb` drops `DEMOLITION` and keeps the other categories — both are left with nothing but `DAMAGE`;
  2. they therefore **cannot break blocks at all**: `canBreakBlocks()` is the only predicate, both `BlastRule.forCrop` and `FarmstormExplosion.breakBlocks` read nothing else, and once the category is gone no path can break a block, so **there is no intermediate state such as "reduced demolition capability"**;
  3. the explosion itself is kept: the two projectiles still settle through `ExplosionQueue → FarmstormExplosion`, only `breakBlocks` no longer breaks blocks;
  4. the damage is kept: Melon Grenade `explosionPower` 3 / `maxDamage` 8, Melon Slice Bomb 2 / 6, none of them changed;
  5. the two resistance thresholds are unchanged (`explosion.sliceResistThreshold` / `grenadeResistThreshold` still 6.0) and D-19 remains in force — it now describes only "who blocks whom if demolition is ever restored" and no longer affects this release's behaviour.
- **Rationale**: across the whole repository there are only **3 places** that read `DEMOLITION` in code: `canBreakBlocks()` at `util/CropProfile.java:181-184`, `forCrop` at `explosion/BlastRule.java:78-80` and the startup self-check assertion at `Farmstorm.java:99-106`. There is no second demolition predicate — that is the entire technical basis for "removing it means it can break nothing at all" (BRIEF §1.4, and item 4 of the prohibitions in §6). The author's original text says only "remove the demolition-category entry of both, they can no longer blow blocks up" (`plan0.1.0.txt:22`) + BRIEF §2 item 19. Keeping the damage is the adjudication of BRIEF §2 item 9: what the author wants is "not blowing blocks up", not "downgrading these two into junk".
- **Where it lands**: `util/CropProfile.java:58-63` (the two `categorySet`); with it `Farmstorm.java:103-104` (§4.2 row 1); the profile-change table of §3.1.
- **Supplement**: the number of Demolition profiles drops from 4 to 2 (leaving `ubi_server` and `toxic_ubi_server`), and the `demolition` count in the startup self-check accordingly goes from 4 to 2 (§4.2 row 1). The category count of each of the two goes from 2 to 1, the field count of `CropProfile` (8) is unchanged, and the profile table is still 22 rows.

#### D-109 · Melon Slice Bomb landing points tightened: count unchanged, jitter 0.3 → 0.05, radial speed 0.3 → 0.1

- **ID**: D-109
- **Decision**:
  1. the count is unchanged: `melon_grenade.spawnCount` is still **4**;
  2. `melon_grenade.horizontalJitter` defaults to **0.3 → 0.05**;
  3. the radial initial velocity goes from a hard-coded constant to the config key `melon_grenade.submunitionSpeed`, default **0.1** (was `SUBMUNITION_SPEED = 0.3`);
  4. `spawnOffsetY` **1** is unchanged; the submunitions still do not settle immediately and each runs the Throwable landing-point flow again through its own ground collision.
- **Rationale**: BRIEF §2 item 10 (count unchanged, both the jitter and the speed changed, the speed turned into a config key). The current state is "landing-point jitter ±0.3 block + radial initial velocity 0.3": the jitter decides the **initial spread radius** of the four Melon Slice Bombs (a diameter of at most 0.6 blocks) and the initial velocity decides the **speed** of the spread, so pressing down only one of them still spreads (this document separates them by vector semantics), and therefore both numbers must be pressed down together. The author's wording is "the landing points more dense" (`plan0.1.0.txt:23`); turning the speed from a constant into config is the requirement of BRIEF §3.2: it is now a tunable feel parameter and no longer belongs to "implementation detail".
- **Where it lands**: `entity/thrown/MelonGrenadeProjectile.java:26` (delete the constant, read config instead), `:56` (the jitter goes through the existing key), `:66` (the speed reads config); `config/FarmstormConfig.java` (`horizontalJitter` gets a changed default, `submunitionSpeed` is added); §4.2 rows 2 and 3; §5.1 rows 2 and 3; §5.2 rows 10 and 11.
- **Supplement**: the source comment "the submunition's horizontal initial velocity (TECH §4.7), an implementation-detail constant that does not go into config" (`MelonGrenadeProjectile.java:25`) is voided along with this entry and is deleted together when the comment is reworked — leaving a sentence that says the opposite of the behaviour would lead the next reader to put the key back (supplemented by this document).

### 2.4 Growth (D-110 ~ D-111)

#### D-110 · Total growth time 20~45 seconds, **the vanilla random tick is abandoned**

- **ID**: D-110
- **Decision**:
  1. the total time from planting to maturity (level 3 of `AGE_3`) of all this mod's crops (the 15 `FarmstormCropBlock` registry entries) is random within **[20, 45] seconds**, in four stages and **three advancements**;
  2. **the vanilla random-tick mechanism is abandoned**: the crops no longer advance through `randomTick` but through deterministic advancement (the carrier is in D-111);
  3. the three intervals are **equal in length**: each is `d` ticks and the total time is `T = 3d` (the value of `d` is in "Supplement", its scheme having been fixed as three equal segments by **A-26**), and the sum of the three intervals always equals `T`;
  4. the brightness condition, the bone-meal behaviour, the four `AGE_3` stages and `MAX_CUSTOM_AGE = 3` are all unchanged.
- **Rationale**: `FarmstormCropBlock` does **not override** `randomTick` / `isRandomlyTicking` / `getGrowthSpeed` and follows the vanilla `CropBlock` completely; the only growth-related code in the whole mod is `.randomTicks()` at `registry/FarmstormBlocks.java:88`, and **no config key** controls the growth speed (BRIEF §1.5). The vanilla predicate is "advance one level only when `nextInt(25/f + 1) == 0` on a random tick, and only when `getRawBrightness(pos,0) >= 9`", the random-tick rate being 3 times per chunk section per tick over 4096 blocks; on that basis the current fastest is about **10 minutes 14 seconds (614 seconds)**, about 24 minutes on fully dry farmland and about 44 minutes on densely planted dry ground. The target's upper bound of 45 seconds is an order of magnitude faster even than the current **fastest** value, so the random-tick road is unreachable in parameter terms — turning `randomTickSpeed` up far enough to mature in 45 seconds would send every vanilla crop on the whole server growing wildly, which is exactly the "global mechanism replacement" RT-24 talks about. BRIEF §2 item 1 gives the total-time interval and "the four stages divided evenly".
- **Where it lands**: `block/FarmstormCropBlock.java` (deterministic advancement); `registry/FarmstormBlocks.java:88` (whether `.randomTicks()` stays is in D-111); `growth.minSeconds` **20** / `growth.maxSeconds` **45** of `config/FarmstormConfig.java` (§5.2 rows 1 and 2).
- **Supplement**: **15** is the number of `crop(...)` calls obtained by counting `registry/FarmstormBlocks.java:25-38` (14 lines) and `:42` (1 line) line by line, not the profile count 22 — 7 entries in the profiles have no planted form (the explanation in §3.1 follows the convention of §4 of document 16). The rule for the three intervals is **supplemented by this document** and was re-adjudicated by **A-26**: `d = 134 + floorMod(pos.hashCode(), 167)`, hence `T = 3d ∈ [402, 900]` ticks = **20.1 ~ 45.0 seconds** — the lower bound is 402 (= 3 × 134) rather than 400 (= 20 seconds × 20 ticks) and is still inside the [20, 45] second interval; the three intervals are equal in length, so the patch that "absorbs the rounding remainder" is no longer needed. Starting from `pos.hashCode()` rather than drawing afresh every time is so that the same cell gets the same `d` after re-entering the world; otherwise an unload and reload would shuffle the progress.

#### D-111 · The carrier of the deterministic advancement: a scheduled-tick chain, **no block entity is added**

- **ID**: D-111
- **Decision**: it is driven by a **scheduled-tick (`scheduleTick`) chain**, with no block entity and no global polling:
  1. when the crop block is placed (`onPlace`) and after each completed advancement, the next one is queued with `level.scheduleTick(pos, this, delay)`; after the third advancement no further scheduling happens;
  2. the crop block therefore **needs no block entity** — the registry surface stays at +0, and the only +1 of `block_entities` is the Outpost Core (§3.2);
  3. `.randomTicks()` at `registry/FarmstormBlocks.java:88` is **removed** and `FarmstormCropBlock` does not override `randomTick`: keeping the random tick would let the vanilla `CropBlock.randomTick` keep advancing and form a **dual-source advancement** together with the scheduled tick, making the growth time uncontrollable;
  4. after bone meal advances one level the next scheduling is rearranged by the remaining stages (an early advancement does not interrupt the scheduled chain); the `tick` callback first checks `state.getBlock() == this`, so a scheduled tick lapses naturally once the block has been mined away.
- **Rationale**: the scheduled tick is the block's own capability (`Block` / `Level.scheduleTick`) and needs no registry object at all; the block-entity road would either give each of the 15 crops a type of its own (+15 registry objects) or share one type and change the definitions of 15 blocks, and either way it would break through this release's "registry surface +3" boundary (§3.2). The scheduled tick is carried by the chunk's own pending queue and is saved and loaded with the chunk data, so advancement continues after a chunk unload and reload or a server restart — which is exactly the semantics needed for "shut the server down for two days, come back, and the crops do not mature instantly". A global tick poll would have to walk every block on the server, with a cost that grows with the size of the world (supplemented by this document: the reasons the three carriers were weighed).
- **Where it lands**: `block/FarmstormCropBlock.java` (the `onPlace` scheduling, the `tick` advancement, the rearrangement after bone meal); `registry/FarmstormBlocks.java:82-91` (the one `.randomTicks()` of the `crop(...)` factory); the acceptance self-check of `24-TECH-0.1.0.md` needs one more item, "advancement is not lost after a chunk unload and reload".
- **Supplement**: this entry is one of the four items BRIEF §7 hands to the document author to derive ("which carrier the deterministic advancement of the growth time uses (scheduled-tick chain / block entity / something else), and why"), and the reasons for rejecting the other two carriers are written into "Rationale". "The scheduled tick is saved with the chunk" has to be confirmed by measurement in the implementation phase (save → restart → advancement continues); if the measurement does not hold, fall back to the patch "rearrange by the placement information on load", which **does not change the convention of this entry**, only the carrier detail.

---

### 2.5 The trap system (D-112 ~ D-115)

#### D-112 · 5 seconds of same-category (**major category**) immunity after a trigger

- **ID**: D-112
- **Decision**:
  1. after a player triggers any kind of trap, **no trap triggers on him for 5 seconds (100 ticks)** — the immunity is judged by the **trap major category** (the TRAP category as a whole), not by which concrete trap it was;
  2. the predicate sits before trigger scheduling: once `findTrigger` has returned candidates they pass one further immunity gate, and while immune the scan skips them outright (no `consume`, no effect applied);
  3. only players are remembered; **hostile mobs are not remembered**;
  4. the timer uses `gameTime` and writes no save file.
- **Rationale**: the author's adjudication is "after stepping on any kind of trap, no trap triggers for 5 seconds" (BRIEF §2 item 2, the original "same kind" being made explicit as the **major category**). The `findTrigger` of the four traps each walk the players independently (BRIEF §1.6); writing the immunity into the four implementations would mean four duplicated predicates and would necessarily produce the deviation "the four kinds of trap have different immunity-window start points", whereas putting it in the scheduling layer (the scan entry point at `blockentity/DeployedCropBlockEntity.java:136-153`) leaves only one place and the four categories are unified by themselves. It writes no save file because it only lives for 5 seconds: the worst consequence of clearing it on reconnect is "the same person can step on one more within 5 seconds", a price far smaller than designing an on-disk format for a 5-second table (supplemented by this document). Mobs are not remembered for the same reason: mobs have short life cycles and the bookkeeping earns nothing, while remembering them would produce the counter-intuitive result "one monster in a group has just triggered ⇒ the whole group is immune" (supplemented by this document).
- **Where it lands**: `blockentity/DeployedCropBlockEntity.java:136-161` (the immunity gate and the existing staggering at the scan entry point); the server-side bookkeeping container (on the same level as the existing `DEPLOY_LEDGER`, `:71-72`); `trap.immunityTicks` **100** of `config/FarmstormConfig.java` (§5.2 row 3).
- **Supplement**: the immunity takes effect "after the candidates have been screened and before the hit is decided", so when several traps reach the same player in the same moment only the one settled first takes effect and writes the immunity while the rest are gated — which is the same effect as "only one trigger per moment", with no extra de-duplication key needed (supplemented by this document).

#### D-113 · One dust particle every 5 seconds after deployment

- **ID**: D-113
- **Decision**:
  1. after each trap is deployed, every **5 seconds (100 ticks)** it produces **a small number of** dust particles at its own position, **2** of them each time, using the vanilla particle `minecraft:dust`;
  2. only the vanilla particle packet is used for the broadcast (`ClientboundLevelParticlesPacket`), no custom network packet is opened (C-6 / D-50);
  3. it shares the existing staggering `stagger = floorMod(pos.hashCode(), interval)` with the scan (`blockentity/DeployedCropBlockEntity.java:155-161`), so that several traps in the same chunk do not all fire in the same tick;
  4. only deployed **traps** (TRAP) emit it; turrets and construction items do not.
- **Rationale**: the author's original text is "every five seconds after all traps are deployed, produce a small amount of 'dust' particles" (`plan0.1.0.txt:32`), and the interval is pinned down by `trap.dustIntervalTicks` **100** of BRIEF §3.4. It hangs on the existing hook `TrapBehavior.tick(be)` (`behavior/TrapBehavior.java:11-26`) rather than opening a second kind of scheduling: that hook already carries the interval and the staggering semantics, and a second set would only produce the misalignment "the scan goes by 10 ticks and the particles by 100 ticks, but with a different phase" (supplemented by this document). The purpose of the particles is "to hint that there is something here", not a special effect: a larger number would expose the position of our own traps to the enemy and would also amplify the packet volume — the same concern as RT-30 has about boundary particles.
- **Where it lands**: `behavior/TrapBehavior.java` (the default implementation of `tick`, shared by the four impls); `blockentity/DeployedCropBlockEntity.java:136-161` (the scheduling is shared); `trap.dustIntervalTicks` **100** of `config/FarmstormConfig.java` (§5.2 row 4).
- **Supplement**: "a small number" taken as **2 per emission** is **supplemented by this document**: both the plan and BRIEF say only "a small number", and 2 is the smallest observable value that "can be seen at a glance while not being enough to mark the exact position"; the colour, scale and spread radius of the particles take the values of the existing particle constant table and are aligned with it by `23-DESIGN-0.1.0.md`, and this document lays down no separate values.

#### D-114 · Hostile mobs can trigger: the general effects apply, the player-only ones are skipped

- **ID**: D-114
- **Decision**:
  1. the triggering subject widens from "non-ally players" to "non-ally players + **hostile mobs**"; hostile mobs are taken as the vanilla `Monster` (zombie / skeleton / creeper and so on; **animals and villagers do not count**), the same convention as D-43's ruling on the Pumpkin Turret's targeting;
  2. the effects are split in two: the **general effects** (damage, slowness, knockback) apply to every triggerer, while the **player-only ones** (Q-Bounce, Confusion) are skipped for mobs;
  3. trap by trap, where each one lands is: the **Blast Carrot** only needs the trigger scan widened (its explosion damage already settles over `LivingEntity`); the **Fang Trap Cactus**'s fang damage applies to all mobs, and Slowness V is only applied when the injured one is a `ServerPlayer` (the current implementation already filters that way and is unchanged); the **Vibration Bean**'s Confusion is applied only to players within the radius (the effect side walks players and is unchanged), and when a mob triggers it the explosion sound is still played once and `consume` still runs; the **Zangief Cane** runs the phase chain for players (unchanged) and for mobs settles only the general part — a fixed **7.5** damage (following `zangief_cane.slamDamage`) plus knockback, settled at the trap's position, with no phase animation;
  4. creative and spectator players still do not trigger; mobs have no team affiliation and are therefore **always treated as non-ally** (the ally judgement only means anything for players);
  5. the target set of the turrets is unchanged: the Pumpkin Turret, per D-43, still locks only non-ally players + hostile mobs, and the other turrets still lock players only.
- **Rationale**: the author's original text "traps can be triggered by all hostile mobs" (`plan0.1.0.txt:33`) + BRIEF §2 item 17 (the general effects apply, the player-only ones are skipped). At present all four `findTrigger` walk `level.players()` only (BlastCarrot:44, FangTrap:39, Zangief:113, VibrationBean:49), and the signature of `TrapBehavior` is `Optional<Player> findTrigger(...)` / `trigger(be, Player)` (`behavior/TrapBehavior.java:18-24`) — so this entry is not "swap one collection" but a widening of the **type** at the triggering end, an interface-level change. The twofold split follows the implementation directly: the damage path is already general to mobs (`explosion/FarmstormExplosion.java:159-179` settles over `LivingEntity`), and the two player-only effects each already have a clear player filter (the Fang Trap Cactus filters `ServerPlayer` at `behavior/impl/FangTrapFangs.java:59-61`, and the Vibration Bean's effect side walks `level.players()`), so following the implementation is less likely to miss something than adding a second set of predicates.
- **Where it lands**: `behavior/TrapBehavior.java:18-24` (the triggerer type of `findTrigger` / `trigger` widened); the scan of the four impls (`behavior/impl/BlastCarrotTrap.java:44`, `FangTrapCactusTrap.java:39`, `ZangiefCaneTrap.java:113`, `VibrationBeanTrap.java:49`); the mob branch of the Zangief Cane (`behavior/impl/ZangiefCaneTrap.java:61-81` and the entry split of `behavior/ZangiefPiledriverController.java`); no new key in `config` (the existing damage and radius keys are reused).
- **Supplement**: the mob branch of the Zangief Cane is **supplemented by this document** — the twofold split of BRIEF §2 item 17 lists only "slowness / damage / knockback" and "Q-Bounce / Confusion" and does not name which of the two the Zangief Cane's phase chain belongs to; the plan's original text is "all traps", and excluding it would conflict with the author's literal wording, while the phase controller targets a `ServerPlayer` (`ZangiefPiledriverController.begin(player, deployer)`, `ZangiefCaneTrap.java:71`), so only the general part is settled. "Mobs are always non-ally" is likewise supplemented by this document, the reason being that C-9's ally judgement is based on the team member list and mobs are not in it.

#### D-115 · 16 traps per player, **FIFO squeezes out the earliest one**

- **ID**: D-115
- **Decision**:
  1. each player can have at most **16** traps at the same time; when the 17th is deployed, **the one deployed earliest** is removed silently (FIFO);
  2. the slot counts only deployed objects of the **TRAP category**; turrets and construction items take no slot;
  3. the squeezed-out trap goes through the existing `consume()` path (`removeBlock(pos,false)` + breaking particles, **no drops**), and the item is not refunded;
  4. a **new "player → the traps in that player's name" ordered index is added and persisted**: the existing ledger holds only counts (`Map<UUID, DeployCounter(int, long)>`, `blockentity/DeployedCropBlockEntity.java:65-72`) and cannot tell "which one is the earliest";
  5. the three existing safety valves **5 / 8 / 32** (`DEPLOY_COOLDOWN_TICKS` / `MAX_DEPLOYED_PER_CHUNK` / `MAX_DEPLOYED_PER_PLAYER`, `:48-50`) stay untouched — 16 is a gameplay slot, not a safety valve.
- **Rationale**: the author's FIFO metaphor in the original text (`plan0.1.0.txt:34`) + BRIEF §2 item 3 (only deployed objects of the TRAP tag are counted). "Earliest" is by nature already an ordered fact on a single trap: `placedGameTime` is already written into the block entity's NBT (`TAG_PLACED_GAME_TIME`, `:53`); what is missing is the **reverse index** "which traps there are in that player's name" (the ⚠️ of BRIEF §1.6, RT-26). The criterion for persisting it is: the index must be able to rebuild, after a server restart, the same order as at the first deployment, otherwise "squeeze out the earliest" degenerates into "squeeze out the last loaded" (supplemented by this document). The safety valves are left alone because the comment at `DeployedCropBlockEntity.java:47` fixes the three as the server protection floor (HC-13 / TECH §4.11.6), and touching them would mean melting the protection line and the gameplay ceiling into one knob.
- **Where it lands**: `blockentity/DeployedCropBlockEntity.java` (writing / reading / squeezing out of the index); the server-side `SavedData` (the persistence carrier of the index); `trap.maxTrapsPerPlayer` **16** of `config/FarmstormConfig.java` (§5.2 row 5). The decision entry point is `getStateForPlacement` at `block/DeployedCropBlock.java:163` (choose the head of the queue and decide the eviction), and the removal itself runs through `consume()` at `blockentity/DeployedCropBlockEntity.java:322-330`.
- **Supplement**: the on-disk format is taken as "UUID → a list of coordinates ascending by `placedGameTime`", with a **lazy check on load** (the block at the coordinate must still be a TRAP deployment in that player's name, otherwise the entry is dropped) — this is the second item BRIEF §7 hands to the document author to derive, and it is chosen because "the data can validate itself": a bad entry expires by itself and no extra reconciliation task is needed. The eviction must happen **before** the new block is written, otherwise the 17th would take a slot first and squeeze out one that has just been placed (supplemented by this document).

### 2.6 Farmland and the outpost (D-116 ~ D-122)

#### D-116 · The carrier of the farmland quota: `/farmstorm farmlandlimit <n>` + a config default of 0

- **ID**: D-116
- **Decision**:
  1. the farmland quota **is not a gamerule**: this release registers no `GameRule`; the quota has two parts — the server-side config `farmland.limitPerPlayer` (default **0**) supplies the default value, and at run time it is changed with the custom subcommand `/farmstorm farmlandlimit <n>`;
  2. the meaning of the default 0: at the start nobody can till any land and a player must first place an Outpost Core (the +16 of D-120);
  3. only **new tilling** is limited, nothing is retroactive: existing farmland does not disappear or degrade when the quota changes;
  4. the occupancy check intercepts at the moment of tilling (on the vanilla path that turns dirt / grass into farmland); when refused, no hoe durability is consumed and no farmland is produced;
  5. the command's value acts only on the current server process, and a restart returns to the config default; no second piece of persistent data is written.
- **Rationale**: the vanilla `GameRules.register` is `private static` (decompiled `GameRules.java:207`), and the sources of NeoForge 21.1.213 contain **no entry point at all for registering a custom gamerule** — inside the "no Mixin" red line (D-40) a real gamerule cannot be registered (BRIEF §1.8), which is why the `/gamerule` in the author's original text (`plan0.1.0.txt:39`) was re-adjudicated by BRIEF §2 item 18 into a `/farmstorm` subcommand. The default 0 is BRIEF §2 item 4 and non-retroactivity is item 5. The command's value is not persisted: config is already the only persistent convention, and storing a second copy would immediately produce a dual source "both places can define the quota, and one wins over the other", the same kind of anti-pattern as the "the duration is written only by the applier" established by D-81 (supplemented by this document). Not deducting durability when refused is supplemented by this document: the quota is a rule, not a failure verdict, and a player should not pay a price for hoeing once in a field where no core has been placed.
- **Where it lands**: the new `/farmstorm` command tree under `command/` (across the whole repository, `RegisterCommandsEvent` / `CommandSourceStack` / `LiteralArgumentBuilder` all have 0 hits, measured); `farmland.limitPerPlayer` **0** of `config/FarmstormConfig.java` (§5.2 row 6); the tilling interception entry point (`FARMLAND` / `FarmlandBlock` / `turnToDirt` / `HoeItem` / `TillableBlock` / `BlockEvent` have 0 hits under `src/` and are all built from nothing, BRIEF §1.10).
- **Supplement**: the permission level and the feedback wording of the command are pinned down by `24-TECH-0.1.0.md`; the language keys are appended on the base of 299 and their number is counted by 24-TECH once it has listed them item by item; this document writes no fixed number.

#### D-117 · The quota is summed within a team, and the accounting uses a set of coordinates

- **ID**: D-117
- **Decision**:
  1. when there is a team, the tilling ceiling is **the sum of the personal quotas of all members of the team**, not each member's own;
  2. the team's tilled count is the total number of farmland blocks in the team's name (with members counted together); the range constraint is guaranteed by D-118, so "the farmland that can be tilled is necessarily inside an ally core's range" does not have to be judged a second time;
  3. with no team it looks only at the player himself (the quota = his own personal quota);
  4. the accounting uses a **set of coordinates** plus a count cache rather than a bare count: a coordinate set can be audited, can be decremented, and is also the data source for item 4 of D-120, "after the core is destroyed, turn only the ally blocks".
- **Rationale**: the author's original text "when several players are in the same team, their farmland count ceiling is the sum of the farmland ceilings of all players in the team" (`plan0.1.0.txt:38`). The team convention follows the vanilla scoreboard team and `TeamResolver` of D-06, no second system is wired in, and the ally judgement is resolved in real time at read time per C-9. The coordinate set is chosen as the third item BRIEF §7 hands to the document author to derive: a bare count has nowhere to be decremented when "a player digs up one farmland block himself" and the imbalance is only discovered at the next tilling; a coordinate set carries the decrement naturally, at the price of one copy in memory and one write to disk (supplemented by this document).
- **Where it lands**: the server-side farmland accounting (a coordinate set + a count cache, on the same level as the interception entry point of D-116); `TeamResolver` (D-06) resolves the team; `/farmstorm farmlandlimit` changes only the base quota, not the tilled count.
- **Supplement**: a farmland block is removed from the set on the spot when it is dug into dirt or destroyed by an explosion; the material check is done at read time (the block at the coordinate must still be `minecraft:farmland`), so that the world being changed by external tools leaves no dirty entries (supplemented by this document).

#### D-118 · Farmland can be tilled only inside the 3×3 chunks of an ally Outpost Core

- **ID**: D-118
- **Decision**:
  1. farmland may be tilled only within **the chunk of an ally Outpost Core and the 8 chunks around it** (3 × 3 = 9 chunks, 48 × 48 blocks);
  2. "ally" = the core's placer himself and his teammates (the real-time resolution convention of D-06 / C-9);
  3. tilling outside the range: no farmland is produced, no durability is deducted, and nothing but the hint wording happens;
  4. the ranges of several cores **are unioned**; the same farmland block lying inside two cores' ranges counts as one block.
- **Rationale**: the author's original text "a player may till only when inside the chunk of an ally Outpost Core or the eight chunks around it" (`plan0.1.0.txt:38`) + BRIEF §2 item 7 (the placer's ownership, the effect shared with the team). Pinning the range to "chunks" rather than to "a sphere centred on the core" is the author's literal wording: a chunk is a multiple of 16, 3 × 3 chunks = 48 × 48 blocks, and this shares the same set of numbers as the boundary particles of D-121 (this document converts literally: 16 × 3 = 48). Not deducting durability outside the range is the same predicate as item 4 of D-116: a refusal is the enforcement of a rule, not a mistake by the player.
- **Where it lands**: the tilling interception entry point (the same place as D-116, range judged first and quota second); the range query of the Outpost Core block entity; no new key in `config`.
- **Supplement**: the 3 × 3 is not made tunable — the author gave no such knob, and adding a key would amount to inventing a requirement (§6 item 9). "Several cores are unioned" is supplemented by this document: the author described only a single core's range, while D-122 allows 2 cores per player, and if two adjacent ranges were not unioned there would be the hole "standing between two cores and unable to till".
#### D-119 · The numbers and the immovable property of the Outpost Core

- **ID**: D-119
- **Decision**:
  1. hardness **30** (`strength(30.0F, …)`), blast resistance **1200** (`strength(…, 1200.0F)`);
  2. **a pickaxe of any level mines out a drop**: `requiresCorrectToolForDrops()` + membership of `minecraft:mineable/pickaxe`, but **not** of any level tag among `needs_stone_tool` / `needs_iron_tool` / `needs_diamond_tool`;
  3. it cannot be pushed or pulled by a piston: `pushReaction(PushReaction.BLOCK)`, following the existing precedent of the deployed block;
  4. it can be placed at any position (no attachment required, no support required); the placer is written into `ownerUuid` in `setPlacedBy`, and the effect is shared with teammates by real-time team resolution per D-06;
  5. mining goes through the block's own loot table and yields the block itself (the same shape as the existing functional blocks, with no special drop).
- **Rationale**: BRIEF §2 item 8 (hardness 30 / blast resistance 1200 / any pickaxe), item 16 (placeable anywhere, not pushable) and item 7 (the placer's ownership, the effect shared with the team). The blast resistance of 1200 does not conflict with "with reference to ancient debris": the author gives hardness and resistance as two independent numbers and BRIEF §2 item 8 pins both down, so the numbers are taken from BRIEF. Any pickaxe is landed through tags: `requiresCorrectToolForDrops` requires only "the right tool", which level exactly being decided by `needs_*_tool`, and with no level tag added ⇒ a wooden pickaxe is the right tool (supplemented by this document). Immovability is done with a registration property rather than by listening for piston events: an event cannot stop a push that has already happened, and the registration property is the only predicate — which is exactly the reason written in the comment at `registry/FarmstormBlocks.java:101-102`.
- **Where it lands**: `registry/FarmstormBlocks.java` (the property lines of `outpost_core`); `datagen/ModBlockTagsProvider.java` (`mineable/pickaxe` +1, without the three `needs_*`); `block/OutpostCoreBlock.java` (`setPlacedBy` writes the ownership); the loot table `loot_table/blocks/outpost_core.json`; the registry surface of §3.2.
- **Supplement**: the feel of "any pickaxe" is "it can be mined, but it takes a long time" (hardness 30 × 1.5 = 45 seconds of base mining time), so no special case is added to `destroyTime` and no "allies break it instantly" bypass is written (§6 item 8).

#### D-120 · The three range effects of the Outpost Core: +16 quota / farmland not trampled / turns to dirt when destroyed

- **ID**: D-120
- **Decision**:
  1. each core adds **+16** farmland quota to its placer and that player's team (`outpost_core.farmlandBonus`);
  2. the range of effect = the chunk it is in + the 8 chunks around it (3 × 3 = 48 × 48 blocks), the same set of numbers as D-118;
  3. **ally** farmland inside the range cannot be trampled: neither players nor mobs turning it into dirt;
  4. when the core is destroyed, all **ally** farmland inside the range turns into dirt;
  5. "ally" in items 3 and 4 = the core's placer and his teammates.
- **Rationale**: one line of the author's original text gives all four items (`plan0.1.0.txt:45`): +16 quota, the 3 × 3 range, farmland inside the range not being trampled, and turning to dirt when destroyed. The range of item 4 is restricted to "ally" because the original text says exactly "all **ally** farmland inside the range of effect turns into dirt" — flattening the enemy's fields along with it would amount to granting a remote right of "blow up the core, demolish the neighbourhood", which goes beyond the original text (supplemented by this document: the original text already carries the restriction, and this only reads the qualifier in full). Item 3 blocking trampling only and not the dehydration degradation is supplemented by this document: dehydration degradation is a state change of the farmland itself, and blocking that too would turn the farmland inside the range into permanent fields that never degrade, which goes beyond what the author asked for.
- **Where it lands**: `block/OutpostCoreBlock.java` and `blockentity/OutpostCoreBlockEntity.java` (`blocks` +1 / `block_entities` +1); the farmland interception entry point (the same place as D-116 / D-118, with two more trigger surfaces, "trampling" and "core destroyed"); `outpost_core.farmlandBonus` **16** of `config/FarmstormConfig.java` (§5.2 row 7).
- **Supplement**: when the core is destroyed only the **booked ally farmland** is handled (the coordinate set of D-117) and no whole-range scan is done — a scan would also flatten "farmland inside the range that someone else tilled and that is not on the books", which directly conflicts with the restriction of item 4 (supplemented by this document).

#### D-121 · Boundary particles visible to allies, server side only

- **ID**: D-121
- **Decision**:
  1. ally players can see the core's **boundary particle outline** (the rectangular ground boundary of 48 × 48 blocks);
  2. a pure server-side implementation: the server sends **vanilla** particle packets, on demand, to the players that satisfy the conditions, with zero new client code and zero custom network packets (C-6 / D-50);
  3. it is refreshed once every **40 ticks** (2 seconds) (`outpost_core.boundaryParticleIntervalTicks`);
  4. it is sent only when a player enters the range (including a 16-block margin), and the number of points and the frequency decay with distance (RT-30);
  5. one particle point every 4 blocks, **48** points in a single ring (48 × 4 ÷ 4 = 48).
- **Rationale**: BRIEF §2 item 6 "a particle outline at the boundary (pure server side)" + RT-30 (the volume of a 48 × 48 boundary is large ⇒ send only when a player enters the range, and decay with distance). One point per block would be hundreds of packets per second, hence the thinning out: one point every 4 blocks is supplemented by this document, the reason being that it presses the points of a single ring down to 48 (the same order as the 48-block perimeter), so that at a frequency of once every 2 seconds no more than 48 particles per second are sent; the distance decay reuses the radius and the `longDistance` parameter that the vanilla particle packet already carries, with no custom field needed.
- **Where it lands**: the tick of `blockentity/OutpostCoreBlockEntity` (one round every 40 ticks); the server-side particle broadcast (`ServerLevel.sendParticles` / `ClientboundLevelParticlesPacket`); `outpost_core.boundaryParticleIntervalTicks` **40** of `config/FarmstormConfig.java` (§5.2 row 9).
- **Supplement**: the visibility predicate is "ally + inside the range", without distinguishing creative from survival (the author requires only "a player can see the boundary of an ally Outpost Core"); "one point every 4 blocks / 48 points to a ring" is supplemented by this document, and the measured packet volume and the feel are checked against the timing table of `23-DESIGN-0.1.0.md`.

#### D-122 · 2 Outpost Cores per player, stone in the four corners + diamond in the centre + redstone in the four remaining cells

- **ID**: D-122
- **Decision**:
  1. each player can have at most **2** placed Outpost Cores at the same time (`outpost_core.maxPerPlayer`);
  2. above the limit the placement is **refused** (the place action does not happen, no item is dropped, no item is consumed) — no FIFO eviction as with traps;
  3. crafting: a 3 × 3 crafting-table recipe with **stone in the four corners** (`minecraft:stone`), **a diamond in the exact centre** (`minecraft:diamond`) and **redstone in the remaining four cells** (`minecraft:redstone`), yielding 1 core;
  4. the limit of 2 counts **per player**, not per team; only the effects (+16 quota, the range, the protection) are shared with the team.
- **Rationale**: BRIEF §2 items 15 and 22 (the recipe's four corner stones / centre diamond / four redstone cells) and item 7 (the limit of 2 per player, the effects shared with the team). Refusal instead of FIFO: the plan writes a FIFO only for traps (`plan0.1.0.txt:34`) and says for cores "each player may by default have at most 2 placed Outpost Cores" (`:45`), with no authorisation to evict — mixing the two semantics would make a player randomly lose an **already effective** outpost when placing the third (supplemented by this document).
- **Where it lands**: `datagen/ModRecipeProvider.java` (+1 shaped recipe); `block/OutpostCoreBlock.java` (the limit check before placement); `outpost_core.maxPerPlayer` **2** of `config/FarmstormConfig.java` (§5.2 row 8).
- **Supplement**: the counting source of the limit shares the same "cores in this player's name" query as the boundary particles of D-121; the slot is released immediately when a core is destroyed or mined by an ally, with no delayed release (supplemented by this document).

### 2.7 Assets (D-123)

#### D-123 · Outpost Core textures: three from `32x32\a_runic`

- **ID**: D-123
- **Decision**:
  1. the three textures of the Outpost Core are the `_top` / `_side` / `_bottom` of `outpost_core_all_textures\32x32\a_runic\`, all three **32 × 32**, landing at `textures/block/outpost_core_top.png` / `_side` / `_bottom`;
  2. the block model uses `cube_bottom_top` (one texture each for top / side / bottom); the item model is derived from the block model (`models/item/outpost_core.json` points at `block/outpost_core`);
  3. **the 512 × 512 version is not used**: the directory the author's original text gives is `512x512` (`plan0.1.0.txt:46`), BRIEF §2 item 20 re-adjudicates it to `32x32`, and §1.11 states in so many words that 512 × 512 is not an asset of this release;
  4. the other three style sets in the same directory (`b_industrial` / `c_obsidian` / `d_royal`) do not enter this release.
- **Rationale**: the measurement of BRIEF §1.11 (three 32 × 32, 2745 / 2534 / 2513 bytes) + §2 item 20. 32 × 32 does not change the sampling on a block texture (vanilla samples on a 16 × 16 logical grid and the texture resolution is only pixel density), so no special uv handling is needed (RT-29). `cube_bottom_top` rather than `cube_all`: the assets already give three different faces, and `cube_all` would put the side texture on all six faces, which amounts to wasting the other two the author provided.
- **Where it lands**: `assets/farmstorm/textures/block/outpost_core_{top,side,bottom}.png` (3 newly added, §3.3); `assets/farmstorm/blockstates/outpost_core.json`; `assets/farmstorm/models/block/outpost_core.json` (`cube_bottom_top`); `assets/farmstorm/models/item/outpost_core.json`.
- **Supplement**: the registration of the asset source and the non-commercial personal use follows D-66 (this entry adds 3 PNGs); if the author later wants another style, only these 3 same-named PNGs are replaced, without touching the models or the code (supplemented by this document).

---

## 3. Profiles and the registry surface

### 3.1 The profiles that change this time (only 2)

| # (document 16 §4) | registry id | category before | category after | change |
|---|---|---|---|---|
| 6 | `melon_slice_bomb` | DAMAGE, DEMOLITION | **DAMAGE** | `DEMOLITION` removed |
| 7 | `melon_grenade` | DEMOLITION, DAMAGE | **DAMAGE** | `DEMOLITION` removed |

Apart from the two rows above, **the other 20 profiles change by not one character**: the id, the type, the categories, the crop block, the seeds, the `DeployedKind`, the attachment face and the stack size are all unchanged, and no 23rd profile is added (`CropProfile.ALL` is still **22**). The category count of each of the two goes from 2 to 1, and the profile field count (8) is unchanged.

There are two consequences, both recorded in this section:

- **Demolition profiles 4 → 2**: the remaining ones are `ubi_server` in row 3 and `toxic_ubi_server` in row 4; the `demolition` count in the startup self-check accordingly goes from 4 to 2 (§4.2 row 1).
- **The Lucky Berry random pool is still 19**: the pool is the derivation "all of `ALL` minus `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`" (D-55 / D-97), and the two profiles only change category, entering and leaving the pool neither way, while the profile total has not changed either.

### 3.2 Registry surface increment: +3, **90 → 93**

| registry | increment | detail |
|---|---|---|
| `blocks` | **+1** | `outpost_core` (the Outpost Core) |
| `block_entities` | **+1** | the `BlockEntityType` of `outpost_core` |
| `items` | **+1** | the `BlockItem` of `outpost_core` |
| the other 9 categories | **+0** | `entity_type` / `mob_effect` / `sound_event` / `damage_type` / `recipe_type` / `recipe_serializer` / `creative_mode_tab` / `data_component_type` / `menu_type` are all +0; no crop profile is added (`CropProfile` is still 22) |
| total | **+3** | **90 → 93** |

**The total of 93 is confirmed by `24-TECH-0.1.0.md` itself, which checks it against the source code class by class and names each one; this file records only 90 + 3.**

Two notes:

- **Advancements +0 (still 18)**: not one of the 9 requirements of this release asks for an advancement, and it is handled by the same convention as "the rest +0" of BRIEF §3.1 (supplemented by this document).
- **Crop blocks +0**: D-110 / D-111 change only the behaviour of `FarmstormCropBlock` and add no block and no block entity; the scheduled-tick chain needs no registry object (the rationale of D-111).

### 3.3 Resource increment

| Category | Added | Replaced in place |
|---|---|---|
| PNG | **5**: 3 for `outpost_core` (32 × 32) + 2 for Compressed Puffed Wheat top / bottom (16 × 16) | **6**: `friend_chives_stage0..3` (4, D-104), `wild_apple.png` (D-105), `compressed_hay_block.png` (greyscale to yellow-brown, D-107) |
| JSON | **5**: `blockstates/outpost_core.json`, `models/block/outpost_core.json`, `models/item/outpost_core.json`, `data/farmstorm/loot_table/blocks/outpost_core.json` (the mining drop of item 5 of D-119), 1 crafting recipe for the Outpost Core | **2**: the Compressed Puffed Wheat block model (`cube_all` → `cube_bottom_top`), the Compressed Puffed Wheat item model (`generated` → 3D) |
| Language keys | block name / hints / command feedback / config comments; **no fixed number** | — |

The "added / replaced" breakdown of the PNGs and the JSONs is obtained by merging and counting the items of BRIEF §3.3 (supplemented by this document): anything for which a file of the same name already exists in the repository counts as "replaced in place", anything else as "added". The number of language keys is counted by `24-TECH-0.1.0.md` once it has listed them all item by item, and this document records only the base **299 × 2**.

---

## 4. Register of changes to the existing content of 0.0.4

### 4.1 Scope of the authorisation

v0.1.0 is an incremental version: one sentence of the author's original text says "this version is based on 0.0.4 and keeps adding new items or features; unless this document explicitly requires it, the existing content of 0.0.4 must not be modified" (`plan0.1.0.txt:5`). This section is the only register of "which existing content was changed", and also the proof that "only this much was changed".

The changes fall into three kinds: **19 rows of code-surface registration** (§4.2: 5 rows of numbers and assertions + 14 rows of other code surface), **the category of 2 profiles** (§3.1), and **2 existing wordings plus 2 knock-on statements about mechanisms** (§4.3). The datagen increment and the additions and changes of config keys are registered in §3.3 and §5.2 respectively. Beyond that everything in 0.0.4 stays as it is (§6).

### 4.2 The existing code and assertions that must be changed in step (two tables: 5 rows + 14 rows = 19 rows)

**Table A · Numbers and assertions** (the five items of BRIEF §3.2, each landed on its line number)

| # | Location | Current value | After | Reason | What happens if it is not changed |
|---|---|---|---|---|---|
| 1 | `src/main/java/com/dashpig/farmstorm/Farmstorm.java:103-104` | `demolition == 4` | **`demolition == 2`** | D-108 removes `DEMOLITION` from two profiles | **the startup self-check fails if it is not changed**: `taxonomyOk` becomes `false` and the log prints "破障类 2 → FAIL" |
| 2 | `entity/thrown/MelonGrenadeProjectile.java:26` | `SUBMUNITION_SPEED = 0.3` (hard-coded) | delete the constant, read `melon_grenade.submunitionSpeed` instead (default **0.1**) | D-109 | the speed is still 0.3: once the jitter is pressed to 0.05 the spread speed is still 6 times the initial radius, and the landing points still do not come together |
| 3 | the default of `melon_grenade.horizontalJitter` | 0.3 | **0.05** | D-109 | the two numbers are one convention in two places, and changing only half still spreads |
| 4 | `client/WildHunterHud.java:26` | `BAR_Y_FROM_BOTTOM = 39` | **52** | D-103 | the charge bar still sits on the health / hunger cells, so requirement 1 is as good as not done |
| 5 | the `EXPECTED` / `EXPECTED_COUNTS` of `tools/verify_project.py` (`:150-163` and the ID list) | 90 / 155 / 299, **measured FAIL 0 / exit code 0** (not broken) | **add the 3 IDs to the list and change the counts to 93 / 166 / 315 in step** | the newly added `outpost_core` (§3.2); language keys 299 → 315 (+16, measured by 24-TECH) | **a silent blind spot, not a red light**: with both sides untouched it stays all green while missing the new objects; only adding the list without changing the counts turns it red. For the order of actions see `24-TECH-0.1.0.md` §1.4 (add the list first → then change the counts → then add the fourth config source) |

**Table B · The rest of the code surface that must be changed in step** (14 rows; each row names "what is changed / what changes / which decision it rests on")

| # | File | What changes | Basis |
|---|---|---|---|
| 6 | `util/CropProfile.java:58-63` | the two profiles drop `DEMOLITION` (the category goes from 2 items to 1) | D-108, §3.1 |
| 7 | `entity/thrown/PuffedWheatProjectile.java:99-113` | a "write only into air and fluids" predicate is added before the `fillHay` write (shared by the two projectiles) | D-106 |
| 8 | `entity/thrown/MelonGrenadeProjectile.java:56`, `:66` | the jitter goes through the existing key and the radial speed reads the new key (row 2 covers only the constant deletion at `:26`) | D-109 |
| 9 | `block/FarmstormCropBlock.java` | advancement by the scheduled-tick chain (`onPlace` scheduling, `tick` advancement, rearrangement after bone meal) | D-110, D-111 |
| 10 | `registry/FarmstormBlocks.java:82-91` | the `crop(...)` factory drops `.randomTicks()` | D-111 |
| 11 | `registry/FarmstormBlocks.java` (one new registration line) | register `outpost_core`: `strength(30.0F, 1200.0F)`, `requiresCorrectToolForDrops()`, `pushReaction(BLOCK)` | D-119, §3.2 |
| 12 | `registry/FarmstormBlockEntities.java` | `block_entities` +1: the `BlockEntityType` of `outpost_core` | D-120, §3.2 |
| 13 | `block/OutpostCoreBlock.java` (new) | `setPlacedBy` writes the ownership, the placement limit of 2 is enforced, and ally farmland turns to dirt when it is destroyed | D-119, D-120, D-122 |
| 14 | `blockentity/OutpostCoreBlockEntity.java` (new) | the 3 × 3 chunk range query and the boundary-particle tick (every 40 ticks) | D-120, D-121 |
| 15 | `blockentity/DeployedCropBlockEntity.java` | the 5-second major-category immunity gate, the dust scheduling, and the reading, writing and eviction of the trap FIFO index | D-112, D-113, D-115 |
| 16 | `blockentity/DeployedCropBlockEntity.java:322-330` | the FIFO eviction reuses `consume()` (`removeBlock` + breaking particles, no drops), and the eviction is ordered before the new block is written; the **decision entry point** is `getStateForPlacement` at `block/DeployedCropBlock.java:163` | D-115 |
| 17 | `behavior/TrapBehavior.java:18-24` and the four `behavior/impl/*Trap` | the triggerer type is widened to `LivingEntity`; the Zangief Cane gains the branch "for mobs settle only the general part" | D-114 |
| 18 | `event/FarmstormGameEvents.java` | register the `/farmstorm farmlandlimit` subcommand; subscribe to the trample event for in-range farmland protection | D-116, D-118, D-120 |
| 19 | `tools/gen_textures.py` | `friend_chives_stage0..3` go through the dedicated drawing branch (a re-run must not fall back to magenta) | D-104 |

The two tables total **19 rows** and cover the **code surface** of 0.0.4 that is changed; `24-TECH-0.1.0.md` §1.4 gives the implementation-level list of the same set of changes (19 items: 18 changes + 1 checked and not changed), and document 25 indexes the two tables against each other when it lays out the tasks. datagen (recipes / loot tables / tags / language keys) and the additions and changes of config keys are not in this section; see §3.3 and §5.2 respectively.

### 4.3 Two knock-on changes of existing wordings and mechanisms

- **The second half of D-57 is void**: the D-57 row of `16-DECISIONS-0.0.3.md` §7.2 says that the six faces of the compressed hay block "cannot be rotated" and leaves them alone, while this release turns it into three textures for top / side / bottom per D-107. The property part of D-57 (`strength(30.0F, 6.0F)`, `sound(GRASS)`, normal drops) stays unchanged, with the registration in the D-57 row of §7.2. **Document 16 is not edited back** (it is frozen).
- **A comment is void**: the sentence "an implementation-detail constant that does not go into config" at `entity/thrown/MelonGrenadeProjectile.java:25` is voided along with D-109 and is deleted together when the comment is reworked.
- **Two places that BRIEF §3.2 does not list but that must be changed all the same** (registered here by this document after checking the source code; they are already rows 10 and 17 of Table B and are not new requirements):
  1. `findTrigger` / `trigger` at `behavior/TrapBehavior.java:18-24` currently take `Player` as the triggerer type; D-114 wants hostile mobs to trigger, so the signature must be widened, otherwise "mobs can trigger" has nowhere to land;
  2. `.randomTicks()` at `registry/FarmstormBlocks.java:88` must be removed (D-111), otherwise the vanilla `CropBlock.randomTick` and the scheduled tick advance from two sources and the 20~45 seconds become uncontrollable.

---

## 5. Summary of the number changes

### 5.1 The four number changes (4 rows)

| # | Item | 0.0.4 | v0.1.0 | Carrier | Decision |
|---|---|---|---|---|---|
| 1 | Charge bar distance from the bottom edge of the screen (GUI px) | 39 | **52** | the constant at `client/WildHunterHud.java:26` | D-103 |
| 2 | Upper bound of the Melon Slice Bomb horizontal jitter | 0.3 | **0.05** | config `melon_grenade.horizontalJitter` (the default of an existing key changed) | D-109 |
| 3 | Melon Slice Bomb radial initial velocity | 0.3 (the hard-coded constant `SUBMUNITION_SPEED`) | **0.1** | config `melon_grenade.submunitionSpeed` (a new key replacing the constant) | D-109 |
| 4 | Number of demolition profiles (the startup self-check's `demolition`) | 4 | **2** | the assertion at `Farmstorm.java:103-104` (§4.2 row 1) | D-108 |

Rows 1 and 4 are not config defaults (one is a client constant and one is a startup assertion), so they stay in this section rather than in the next one; rows 2 and 3 are config-side changes and take one row each in §5.2.

### 5.2 The config changes line by line (12 rows = 11 new + 1 changed default)

| # | Group | Key | Type | Default | New / Changed | Purpose | Decision |
|---|---|---|---|---|---|---|---|
| 1 | `growth` | `minSeconds` | int | **20** | new | lower bound of the total time to maturity (seconds) | D-110 |
| 2 | `growth` | `maxSeconds` | int | **45** | new | upper bound of the total time to maturity (seconds) | D-110 |
| 3 | `trap` | `immunityTicks` | int | **100** | new | ticks of same-category (major category) immunity after a trigger (5 seconds) | D-112 |
| 4 | `trap` | `dustIntervalTicks` | int | **100** | new | interval in ticks of the trap dust particle (5 seconds) | D-113 |
| 5 | `trap` | `maxTrapsPerPlayer` | int | **16** | new | per-player trap slot (FIFO) | D-115 |
| 6 | `farmland` | `limitPerPlayer` | int | **0** | new | server-side default of the farmland quota | D-116 |
| 7 | `outpost_core` | `farmlandBonus` | int | **16** | new | farmland ceiling added by each core | D-120 |
| 8 | `outpost_core` | `maxPerPlayer` | int | **2** | new | cores a player may place | D-122 |
| 9 | `outpost_core` | `boundaryParticleIntervalTicks` | int | **40** | new | refresh interval of the boundary particles (ticks) | D-121 |
| 10 | `melon_grenade` | `horizontalJitter` | double | 0.3 → **0.05** | **changed** | landing points tightened | D-109 |
| 11 | `melon_grenade` | `submunitionSpeed` | double | **0.1** | new | radial speed (was hard-coded 0.3) | D-109 |
| 12 | `compressed` | `replaceOnlyAirAndFluid` | boolean | **true** | new | the landing block replaces only air and fluids | D-106 |

### 5.3 Notes on the counts

- The table has **12 rows**, of which **11 are new** (rows 1 – 9, 11, 12) and **1 changes a default** (row 10). **The row count is not the new-key count**: row 10 changes the default of an existing key from 0.3 to 0.05, and the key itself already existed in 0.0.4.
- config keys: **155 + 11 = 166**. 155 is the verified value of 0.0.4 (the BRIEF §0 table) and +11 is the number of purely new keys in this section.
- The defaults of three existing keys **are unchanged**: `melon_grenade.spawnCount` 4, `melon_grenade.spawnOffsetY` 1, `melon_grenade.explosionPower` 3 / `maxDamage` 8, `explosion.sliceResistThreshold` 6.0 / `grenadeResistThreshold` 6.0 (item 5 of D-108, items 1 and 4 of D-109).
- This release adds **no** key at all for the 3 × 3 range, the 48 × 48 boundary, the boundary granularity or the Zangief Cane's mob settlement (§6 item 9).

---

## 6. What is explicitly not done

The 15 entries below are this version's exclusion list; no exception may be opened for any of them during the implementation phase, and when an exception is needed, this document is changed first and the code second.

1. **No gamerule is registered**: this release does not touch `GameRules` and does not introduce a Mixin for it; the farmland rule goes only through the `/farmstorm farmlandlimit <n>` subcommand + the config default (D-116, RT-25).
2. **No vanilla crop is changed**: only `FarmstormCropBlock` is changed; the growth of vanilla wheat / carrots / potatoes / beetroot / melon and pumpkin stems is not touched at all, and the global `randomTickSpeed` is not changed either (D-110, RT-24).
3. **No crop profile is added**: `CropProfile.ALL` is still 22, no 23rd entry is added, and the enum values of `CropType` / `CropCategory` are not changed (§3.1).
4. **No custom network packet is written for the Outpost Core or for farmland**: boundary particles and dust go through the vanilla particle packet; `CustomPacketPayload` is still **0** and `@Mixin` is still **0** (D-121, D-113, §7.2).
5. **No 512 × 512 asset is used**: only the three from `32x32\a_runic`; the other three style sets (`b_industrial` / `c_obsidian` / `d_royal`) do not enter this release (D-123).
6. **The Zangief Cane's seven-segment lock and phase chain are not changed**: the `fullyLocked()` / `movementLocked()` split out in 0.0.4 is unchanged, and the Zangief Cane still fully locks players; the only change this release makes to it is "a mob trigger settles only the general part" (D-114, §7.2).
7. **The three deployment safety valves are not touched**: `DEPLOY_COOLDOWN_TICKS` 5 / `MAX_DEPLOYED_PER_CHUNK` 8 / `MAX_DEPLOYED_PER_PLAYER` 32, not one of them is changed; 16 is a gameplay slot, not a safety valve (D-115).
8. **No "allies break it instantly" or custom mining speed is given to the Outpost Core**: a pickaxe of any level + hardness 30 is the final feel (D-119).
9. **No config key is added for the 3 × 3 range, the 48 × 48 boundary, the boundary granularity or the Zangief Cane's mob settlement**: the author gave no such knobs, and adding one would amount to inventing a requirement (D-118, D-121, D-114).
10. **`WildHunterHud.HOTBAR_WIDTH` is not cleaned up**: this constant, declared and never used, is left where it is, and deleting it would be a change unrelated to the requirement (D-103, BRIEF §1.1).
11. **"Removing DEMOLITION" is not written as "reducing the demolition capability", and the two melons get no substitute entry** (such as "breaks only a certain kind of block"): `DEMOLITION` is the only predicate, and removing it means it can break nothing at all (D-108, BRIEF §6 item 4).
12. **The explosion parameters and resistance thresholds of Melon Grenade and Melon Slice Bomb are not changed**: power 3 / 2, damage caps 8 / 6 and both thresholds 6.0 are all unchanged (D-108).
13. **Existing farmland is not handled retroactively**: however the quota changes, farmland that has already been tilled neither disappears nor degrades (D-116, BRIEF §2 item 5).
14. **No advancement is added**: still 18 (+0); this release hangs no advancement at all (§3.2).
15. **No file of `docs/zh/00` – `21` or of `docs/en` is changed**: the existing documents stay frozen and clarifications of the convention are registered in this document only (the same direction of handling as D-80).

---

## 7. Inheritance relation to existing decisions

### 7.1 Markings

| Marking | Meaning |
|---|---|
| Full continuation | neither the conclusion nor the scope of application changes |
| Extended this time | the conclusion is unchanged; this version adds entries to the list it governs or to the objects it applies to |
| Refined this time | the conclusion is unchanged; this version pins down part of its means or landing points |
| Scope changed | this version rewrote part of its wording |

### 7.2 The 18 entries this version touches

| ID / source | Status | Explanation |
|---|---|---|
| D-06 (the ally judgement uses the vanilla scoreboard team) | Full continuation | the ownership of the Outpost Core, the farmland range and the trap-immunity inheritance all go through `TeamResolver`, and FTB Teams is not wired in |
| C-9 (only `ownerUuid` is stored, the team is resolved in real time at the moment of the trigger) | Extended this time | the convention is unchanged and the objects it applies to extend from the deployed block to **functional blocks**: the Outpost Core likewise stores only the placer's UUID and resolves the teammate relation in real time (D-119) |
| D-07 (traps and turrets target **non-ally players** only and do not handle mobs) | **Scope changed** | D-114 widens the triggering subject of **traps** to hostile mobs (the vanilla `Monster`, following the convention of D-43); the turret set is unchanged; "non-ally" and "creative and spectator excluded" both stay |
| D-19 (the resistance thresholds of Melon Slice Bomb and Melon Grenade are unified at 6.0) | Full continuation | not one threshold is changed; what this release changes is the category (item 5 of D-108) |
| D-38 (the write skips resistance ≥ 3.6e6) | Full continuation | the whitelist of D-106 is **layered on top of** this floor and does not replace it |
| D-40 (no Mixin is introduced) | Full continuation | the new command is registered with Brigadier and the Outpost Core and the farmland interception go through blocks and event extension points; there is not one Mixin anywhere |
| D-50 (state sync adds no custom packet) | Full continuation | this release adds no new sync surface: the quota, the immunity and the FIFO are all accounted on the server side |
| C-6 (particles and sounds go through vanilla packets, 0 custom S2C packets) | Full continuation | boundary particles and dust both use the vanilla particle packet (item 2 of D-121, item 2 of D-113) |
| D-55 (the Lucky Berry pool follows the derivation and adds no exclusion) | Full continuation | the pool is still 19: the two profiles only change category and enter and leave the pool neither way (§3.1) |
| D-57 (the compressed hay block 30 / 6, the same material on all six faces and not rotatable) | **Scope changed** | `strength(30.0F, 6.0F)`, `sound(GRASS)` and normal drops are unchanged; the half-sentence "the same material on all six faces" is voided by D-107 (changed to three textures for top / side / bottom) |
| D-63 (advancements 15 → 18) | Full continuation | this release is +0 (still 18) and adds no new entry (§3.2) |
| D-71 (the Zangief Cane recognises non-ally players only and does not include mobs) | **Scope changed** | the triggering end takes in hostile mobs; the phase chain for players (**phase 1 servo rise to a fixed height**, with the amplifier acting only as the phase number; the meaning and provenance of the four existing numbers 14 / 72° / 20 / 6 are in row 91 of §8) stays exactly as it is, and for mobs only the general part is settled (item 3 of D-114) |
| D-77 (Compressed Puffed Wheat follows the two conventions of the Puffed Wheat landing write) | Extended this time | the structure "reuses the same write implementation" is unchanged, and this release adds a third predicate to that implementation (the air and fluid whitelist, D-106), taking effect on both projectiles at once |
| D-83 (Confusion shows no icon, applies to players only and reverses only the movement input) | Refined this time | the conclusion is unchanged; D-114 makes clear that the landing point of "the player-only ones are skipped" is exactly this one — when a mob triggers a trap, Confusion is not applied, and the effect side still walks players only |
| D-93 (the Fang Trap Cactus changes only its Chinese name; the id and the English name stay) | Full continuation | this release changes no display name, and the single exception of C-10 is still this one place |
| D-98 (the Point Defense Fungus intercepts non-ally projectiles only) | Full continuation | this release's ally judgement comes from the same source (the real-time resolution of C-9) and lays down no second predicate |
| D-101 (the Vibration Bean uses strictly less than 4.0 at both ends) | Full continuation | D-114 widens only the **triggering subject**; the comparison operator and the two radii stay untouched |
| 0.0.4 / Q-Bounce locks movement only (not a D number; registered as V-158 in `README.md:145` and `.work/PLAN.md`) | Full continuation | the segment split of `fullyLocked()` / `movementLocked()` is not touched; "the player-only ones are skipped" of D-114 follows it |

Adding row by row: **Full continuation 12 entries** (D-06, D-19, D-38, D-40, D-50, C-6, D-55, D-63, D-93, D-98, D-101, 0.0.4 Q-Bounce), **Extended this time 2 entries** (C-9, D-77), **Refined this time 1 entry** (D-83), **Scope changed 3 entries** (D-07, D-57, D-71) (supplemented by this document: added row by row from this table).

### 7.3 Why the three "Scope changed" entries have to be registered

1. **D-07 and D-71 are conventions the author's requirement changes directly**: the plan's original text "traps can be triggered by all hostile mobs" (`plan0.1.0.txt:33`) directly conflicts with D-07's "does not handle mobs". The conflict can only be settled at the decision layer — written as "traps may have mob triggering subjects, turrets may not", rather than quietly settled inside a design document.
2. **D-57 is voided in passing by the requirement**: the author requires "the model of Compressed Puffed Wheat changed into a three-dimensional hay block" (`plan0.1.0.txt:18`) and gives the six-face adjudication (BRIEF §2 item 13); "the same material on all six faces" cannot coexist with that. It is registered so that later readers know: what fails in D-57 is **the half-sentence about the appearance**, while the half-sentence about the properties still holds.
3. **Apart from these three, not one has been changed**: the remaining clauses of D-49 ~ D-102 and D-01 ~ D-48 and C-1 ~ C-28 of `00-DECISIONS.md` all continue in force as they are. Not one of this release's 21 decisions rewrites their conclusions; only D-108 touches "the category of two profiles" and D-110 touches "the growth carrier of the crops", those two places of existing implementation.

Two further existing items **not listed in the table above but sidestepped by this version** also need attention (supplemented by this document):

- the comment at `DeployedCropBlockEntity.java:71-72` records "the deployment throttle ledger does not write a save file" under the name of D-18, while D-18 of `00-DECISIONS.md` is about the creative-mode restoration of the Lucky Berry (forced restoration after 90 seconds, no write to the save file) and has nothing to do with the deployment ledger. The FIFO index of D-115 in this release has to be persisted, which is the opposite of that comment and does not conflict with D-18 itself — only the FIFO index is persisted, and the throttle ledger of the three safety valves is still not written to the save file.
- "the only existing config keys whose default changes are the 3 listed in §5.4" in `16-DECISIONS-0.0.3.md` §5.7 is the boundary of 0.0.3, and this release changes one existing key in §5.2 row 10 (`horizontalJitter`), which belongs to **this release's** scope of authorisation and does not edit document 16 back.

---

## 8. Provenance of every number

This section lists one by one every number that this document uses as a quantity, a size, a distance, a duration, a damage, a probability or a ratio. The identifiers themselves (the D-, C-, RT-, F-, HC-, N-, Phase, M, A and V prefixes) are not listed as counts. The "supplemented by this document" entries are numbers that this document derives, reads out of the source code or converts from two verified numbers, and that the build fact card does not give directly.

| # | Number | Meaning (section it is in) | Source |
|---|---|---|---|
| 1 | 1.21.1 | Minecraft version (§1.1) | `gradle.properties:2` |
| 2 | 21.1.213 | NeoForge version (§1.1) | `gradle.properties:3` |
| 3 | 21 | Java version (§1.1) | `gradle.properties` |
| 4 | 0.0.4 | baseline version (§1.1) | `dist/farmstorm-0.0.4+mc1.21.1.jar`; the BRIEF §0 table |
| 5 | 959,376 B | byte count of the baseline jar (§1.1) | the BRIEF §0 table; `dist/` |
| 6 | 117 | number of Java files (§1.1) | the BRIEF §0 table (recursive count under `src/main/java`) |
| 7 | 90 | 0.0.4 registry objects (§1.1, §3.2) | the BRIEF §0 table; the 12 category subtotals of `tools/verify_project.py:150-163` add up to 90 as well (re-checked by this document) |
| 8 | 155 | 0.0.4 config keys (§1.1, §5.3) | the BRIEF §0 table |
| 9 | 299 × 2 · 315 | language keys (§1.1, §3.3, §4.2 row 5): the 0.0.4 base 299 × 2; this release's target 315 (+16) | 299 comes from the BRIEF §0 table; 315 and +16 come from the measurement in `24-TECH-0.1.0.md` §1.4 |
| 10 | 22 | number of crop profiles (§1.1, §3.1) | the BRIEF §0 table; `CropProfile.ALL` (`Farmstorm.java:103-104` asserts 22) |
| 11 | 18 | number of advancements (§1.1, §3.2, §6 item 14) | the BRIEF §0 table |
| 12 | 9 | number of requirements of this release (§1.2) | `plan0.1.0.txt:7-51` counted item by item |
| 13 | 21 | number of decisions of this release (§0, §1.2, §2) | §2 of this document; the closed interval D-103 – D-123 |
| 14 | 3 / 2 / 2 / 2 / 4 / 7 / 1 | the number of decisions in the seven groups (§2 headings) | §2.1 – §2.7 of this document added section by section = 21 (supplemented by this document) |
| 15 | 6 | number of documents added by this release (6 each in Chinese and English) (§1.3) | BRIEF §5; §2 item 12 |
| 16 | +3 | registry object increment (§1.3, §3.2) | BRIEF §3.1 |
| 17 | +1 / +1 / +1 | the breakdown of `blocks` / `block_entities` / `items` (§3.2) | BRIEF §3.1 |
| 18 | 93 | 90 + 3 (§3.2) | the addition of this document; **the final confirmation is the named check by `24-TECH-0.1.0.md`** |
| 19 | 5 / 6 | PNGs added / replaced in place (§3.3, supplemented by this document) | the items of BRIEF §3.3 added up: added 3 + 2, replaced 4 + 1 + 1 |
| 20 | 5 / 2 | JSONs added / replaced in place (§3.3, supplemented by this document) | the items of BRIEF §3.3 merged by "is there already a file of the same name", plus the 1 loot table of item 5 of D-119 (the omission was found by `24-TECH-0.1.0.md`) |
| 21 | 39 → 52 | charge bar distance from the bottom edge of the screen, GUI px (§2.1, §5.1) | BRIEF §2 item 14; `client/WildHunterHud.java:26` |
| 22 | 13 | upward displacement = 52 − 39 (§2.1, supplemented by this document) | subtracted by this document |
| 23 | 20 / 4 / 4 / 44 · 12 · [0,22) · [24,29) · [30,39) · [34,40) | bar width / bar height / gap / total width, GUI px (§2.1); the bottom-offset bands of the vanilla HUD: hotbar / experience bar / health · hunger / charge bar including outline (§2.1); 12 = 34 − 22 is the gap between the lower edge of the charge bar's outline and the upper edge of the hotbar (supplemented by this document) | the four items bar width and so on come from `client/WildHunterHud.java:22-25`, `:51-52`; the four bands come from the HUD measurement of `23-DESIGN-0.1.0.md` (consistent with the author's convention: 39 is 12 px away from the hotbar and does not overlap it) |
| 24 | 182 | `HOTBAR_WIDTH`, declared and unused (§2.1, §6 item 10) | `client/WildHunterHud.java:21`; BRIEF §1.1 |
| 25 | 4 | number of Friend Chives stages (§2.1) | `block/FarmstormCropBlock.java:31`, `:63-70` (`MAX_CUSTOM_AGE = 3` ⇒ AGE_3 has four levels) |
| 26 | 16 × 16 | size of the crop and item textures (§2.1) | BRIEF §1.2, §1.11 (PNG decode measurement) |
| 27 | 4 / 9 | colour counts of the reference styles (`power_beet` / `ubi_server`) (§2.1) | BRIEF §1.2 |
| 28 | 251/256, 175/256 | transparent pixel counts of the reference styles (§2.1) | BRIEF §1.2 |
| 29 | `#D81E8A` | the magenta placeholder colour (§2.1) | `tools/gen_textures.py:64`; BRIEF §1.2 |
| 30 | 1294 × 1216 | size of the new Wild Apple asset (§2.1) | BRIEF §1.11 |
| 31 | 1,113,442 B | byte count of the new asset (§2.1) | BRIEF §1.11 |
| 32 | 11 / 126 | colour count / transparent pixel count of the current `wild_apple.png` (§2.1) | BRIEF §1.11 (states **11 colours** explicitly; 12 is a misremembering of 11 colours + the transparent entry, and the colour-by-colour check of document 23 §9.4 gives 11 as well) |
| 33 | 3.6e6 | the anti-destruction floor resistance (§2.2, §7.2) | `explosion/BlastRule.java:26`; BRIEF §1.3 |
| 34 | 30.0 / 6.0 | hardness / resistance of the compressed hay block (§2.2, §7.2) | `registry/FarmstormBlocks.java:72-78`; BRIEF §1.3 |
| 35 | 9 | colour count of the current Compressed Puffed Wheat texture (fully greyscale) (§2.2) | BRIEF §1.3 |
| 36 | 2 | number of top / bottom textures added for Compressed Puffed Wheat (§2.2, §3.3) | BRIEF §3.3 |
| 37 | 4 → 2 | number of demolition profiles (§2.3, §3.1) | "category · demolition 4" of the counting self-consistency table in `16-DECISIONS-0.0.3.md` §4; §3.1 of this document |
| 38 | 3 | number of code read points of `DEMOLITION` (§2.3) | BRIEF §1.4 |
| 39 | 2 / 6 | `explosionPower` / `maxDamage` of Melon Slice Bomb (§2.3, §5.3) | BRIEF §1.4 |
| 40 | 3 / 8 | `explosionPower` / `maxDamage` of Melon Grenade (§2.3, §5.3) | BRIEF §1.4 |
| 41 | 6.0 / 6.0 | the two resistance thresholds (`slice` / `grenade`) (§2.3, §5.3) | BRIEF §1.4 |
| 42 | 4 | the Melon Slice Bomb count `spawnCount` (§2.3, §5.3) | BRIEF §2 item 10; `config/FarmstormConfig.java:385` |
| 43 | 1 | `spawnOffsetY` (§2.3, §5.3) | BRIEF §1.4; `config/FarmstormConfig.java:388` |
| 44 | 0.3 → 0.05 | upper bound of the horizontal jitter (§2.3, §5.1, §5.2) | BRIEF §2 item 10, §3.2; `config/FarmstormConfig.java:391` (current value 0.3) |
| 45 | 0.3 → 0.1 | radial initial velocity (§2.3, §5.1, §5.2) | BRIEF §2 item 10, §3.2; `entity/thrown/MelonGrenadeProjectile.java:26`, `:66` |
| 46 | 8 | `MAX_EXPLOSIONS_PER_TICK` (§2.3) | `explosion/ExplosionQueue.java:20`; BRIEF §1.4 |
| 47 | 20 / 45 | the total-time-to-maturity interval, seconds (§2.4, §5.2) | BRIEF §2 item 1, §3.4 |
| 48 | 402 / 900 | **20.1** seconds / 45.0 seconds converted to ticks (§2.4, supplemented by this document; the lower bound was re-adjudicated to the three-equal-segments scheme by **A-26**) | 1 second = 20 ticks: `d = 134 + floorMod(pos.hashCode(), 167)` ⇒ `T = 3d ∈ [402, 900]` ticks; **402 = 3 × 134** (20.1 seconds, which keeps the lower bound inside [20, 45] seconds) and 900 = 3 × 300 (45.0 seconds). 402 is not 400: A-26 takes document 24's three equal segments and no longer rounds to a whole 20 seconds. **Where 134 and 167 come from** (the arithmetic of this document's supplement): 134 = ⌈400 / 3⌉ (20 seconds, 400 ticks, split into three and rounded up), 300 = 900 / 3, and 167 = 300 − 134 + 1 (so that the upper bound of `d` lands exactly on 3 × 300 = 900); whether these two constants are written in directly or derived from the seconds of §5.2's `growth.minSeconds` 20 / `growth.maxSeconds` 45 is pinned down by `24-TECH-0.1.0.md`, and the defaults of the two keys in the table are still 20 / 45 |
| 49 | 3 | three advancements (four stages) (§2.4) | BRIEF §2 item 1; `MAX_CUSTOM_AGE = 3` |
| 50 | 614 | the current fastest 10 minutes 14 seconds converted to seconds (§2.4, supplemented by this document) | "10 minutes 14 seconds" of BRIEF §1.5 = 10 × 60 + 14 |
| 51 | 24 / 44 | current time to maturity on fully dry farmland / on densely planted dry ground, minutes (§2.4) | BRIEF §1.5 |
| 52 | 3 / 4096 | random tick rate (3 times per section per tick / 4096 blocks) (§2.4) | BRIEF §1.5 |
| 53 | 9 | the vanilla growth brightness threshold (§2.4) | BRIEF §1.5 |
| 54 | 15 | number of registry entries of this mod's crops (§2.4, supplemented by this document) | `registry/FarmstormBlocks.java:25-38` (14 lines) + `:42` (1 line) counted line by line |
| 55 | 100 | 5 seconds converted to ticks (immunity / dust) (§2.5, §5.2) | BRIEF §3.4; 5 × 20 = 100 |
| 56 | 10 | the existing trap scan interval (the default of `scanInterval()`) (§2.5) | `behavior/TrapBehavior.java:11-26`; BRIEF §1.6 |
| 57 | 1 / 2.5 / 1.5 / 4.0 | the existing trigger radii of the four traps (Blast Carrot / Fang Trap Cactus / Zangief Cane / Vibration Bean) (§2.5, §7.2) | BRIEF §1.6 |
| 58 | 16 | the per-player trap slot (§2.5, §5.2, §6 item 7) | BRIEF §3.4; `plan0.1.0.txt:34` |
| 59 | 5 / 8 / 32 | the three deployment safety valves (§2.5, §7.2, §6 item 7) | `blockentity/DeployedCropBlockEntity.java:48-50`; BRIEF §1.6 |
| 60 | 2 | number of dust particles per emission (§2.5, supplemented by this document) | the reason for the value in §2.5 of this document (BRIEF gives only "a small number") |
| 61 | 0 | the default farmland quota (§2.6, §5.2) | BRIEF §2 item 4, §3.4 |
| 62 | 30 / 1200 | Outpost Core hardness / blast resistance (§2.6) | BRIEF §2 item 8 |
| 63 | 45 seconds | the base mining time at hardness 30 (§2.6 supplement) | vanilla mining time = hardness × 1.5 (30 × 1.5 = 45) |
| 64 | 3 × 3 / 9 / 48 × 48 | the core's range of effect (chunks / number of chunks / blocks) (§2.6, §7.2) | `plan0.1.0.txt:45`; RT-30 of BRIEF §4 (a 48 × 48 block boundary) |
| 65 | 16 | the farmland ceiling added by each core (§2.6, §5.2) | `plan0.1.0.txt:45`; BRIEF §3.4 |
| 66 | 2 | the per-player core limit (§2.6, §5.2, §6 item 7) | BRIEF §2 item 7, §3.4 |
| 67 | 40 | the boundary particle refresh interval, ticks (§2.6, §5.2) | BRIEF §3.4 |
| 68 | 4 / 48 | boundary particle point spacing / points in a single ring (§2.6, supplemented by this document) | 48 × 4 ÷ 4 = 48 (edge length 48 blocks, one point every 4 blocks) |
| 69 | 16 | chunk width, blocks (the basis of the conversion in §2.6 and §2.7) | the vanilla chunk constant; 48 = 3 × 16 and 3 × 3 chunks = 48 × 48 blocks in this document |
| 70 | 4 / 1 / 4 | recipe: stone in the four corners / diamond in the centre / redstone in the four cells (§2.6) | BRIEF §2 items 15 and 22 |
| 71 | 1 | the recipe yields 1 core (§2.6, supplemented by this document) | BRIEF §2 item 22 gives only the recipe slots; the yield of 1 follows the shape of the existing block recipes |
| 72 | 32 × 32 | Outpost Core texture size (§2.7, §3.3) | BRIEF §1.11, §2 item 20 |
| 73 | 3 | number of Outpost Core textures (§2.7, §3.3) | BRIEF §1.11 (`_top` / `_side` / `_bottom`) |
| 74 | 2745 / 2534 / 2513 B | byte counts of the three textures (§2.7) | BRIEF §1.11 |
| 75 | 11 / 12 | number of new keys / number of table rows in §5.2 (§5.2, §5.3) | the note under the table of BRIEF §3.4 (12 rows = 11 new + 1 changed default) |
| 76 | 155 / 166 | config key count before and after (§1.1, §5.3) | the BRIEF §0 table (155); 155 + 11 = 166 |
| 77 | 19 = 5 + 14 | total data rows of the two tables of §4.2 (§4.2, supplemented by this document) | the 5 rows of Table A come from BRIEF §3.2; the 14 rows of Table B are named row by row by this document from the landing points of the individual D entries and are indexed against the 19 items of `24-TECH-0.1.0.md` §1.4 |
| 78 | 4 | number of table rows in §5.1 (§5.1, supplemented by this document) | counted row by row in §5.1 of this document |
| 79 | 15 | number of entries in §6 (§6, supplemented by this document) | counted item by item in §6 of this document |
| 80 | 18 / 12 / 2 / 1 / 3 | the table rows of §7.2 and the number of entries in each of its four statuses (§7.2, supplemented by this document) | added row by row in §7.2 of this document |
| 81 | 2 | number of profile changes in §3.1 (§3.1, supplemented by this document) | counted row by row in §3.1 of this document |
| 82 | 20 | number of profiles that do not change (§3.1, supplemented by this document) | 22 − 2 |
| 83 | 8 | the field count of `CropProfile` (§3.1 supplement) | the 8 fields of `util/CropProfile.java`; the same convention as §4 of `16-DECISIONS-0.0.3.md` |
| 84 | 19 | number of entries in the Lucky Berry random pool (§3.1) | `Farmstorm.java:108-111` (asserts 19); D-55 / D-97 |
| 85 | 3 | the change of the category count of the two "profiles" (2 → 1) (§3.1) | `util/CropProfile.java:58-63`; §3.1 of this document |
| 86 | 16 | the margin of the boundary particles' visible range, blocks (item 4 of §2.6, supplemented by this document) | RT-30 of BRIEF §4 says only "decay with distance"; this document takes one chunk width (16 blocks) as the margin |
| 87 | 6 | the ratio of the radial initial velocity to the jitter (§2.3, §4.2 row 2, supplemented by this document) | 0.3 ÷ 0.05 = 6 (both numbers come from BRIEF §2 item 10) |
| 88 | 7.5 | the fixed landing damage of the Zangief Cane (D-114 of §2.5, the D-71 row of §7.2) | D-96 of `16-DECISIONS-0.0.3.md`; the `config` key `zangief_cane.slamDamage` |
| 89 | 3 | the three spare styles in the same directory (§2.7, §6 item 5) | BRIEF §1.11 (`b_industrial` / `c_obsidian` / `d_royal`) |
| 90 | 9 | the number of registry categories under "the rest +0" (§3.2) | the 12 categories of `tools/verify_project.py:150-163` minus `items` / `blocks` / `block_entity_type` = 9 |
| 91 | 0 / 1 / 2 · 14 · 16 / 15 · 72° · 20 · 6 | The Zangief Cane phase chain: **the amplifier is the phase number (`PHASE_REVERSED` 0 / `PHASE_LEVITATING` 1 / `PHASE_FALLING` 2), not a levitation level**; phase 0 waits **14** ticks; phase 1 rises **16** blocks / **15** ticks (measured about 15 blocks, the velocity difference of the last tick being a fixed loss); the yaw goes +**72**° per tick. `20` and `6` appear only in the description of the old 0.0.1 scheme (levitation) in the D-69 row of `16-DECISIONS-0.0.3.md` §7.2, and from 0.0.2 on they are superseded by the real-client convention — **a historical statement, and the frozen document is not edited back**; the D-71 row of §7.2 cites them only for the record | `effect/ZangiefPiledriverEffect.java:6-16` (it only sends the phase, adds no attribute, deals no damage, produces no particle and reads no config), `:19-25` (the three phase constants; keeping the constant name `PHASE_LEVITATING` is a historical misnomer, and the values 0/1/2 are the protocol to the client), `behavior/ZangiefPiledriverController.java:30` ("the author's v0.0.2 real-client convention, superseding the levitation version of TECH §3.11"), `:36-44` (the three phase definitions), `:212-244` (gravity off + servo rise, `MAX_RISE_SPEED` 4.0), `config/FarmstormConfig.java:792` (`stageDelayTicks` 14), `:795` (`riseHeightBlocks` 16), `:798` (`riseTicks` 15), `:804` (`spinDegreesPerTick` 72.0), `client/ZangiefClientState.java:27` (the client-side 72.0 mirror); `20` / `6` come from the D-69 row of `16-DECISIONS-0.0.3.md` §7.2. The writing convention and the full evidence are in BRIEF §1.12 (the dedicated section "the actual mechanism of the Zangief Cane (**not** levitation)"): always write "phase 1 servo rise to a fixed height", and never write it as levitation / floating / "applying `LEVITATION`" |

Restatement of the number discipline: this document contains no assertion at all about the total number of registry objects — **the total of 93 is confirmed by `24-TECH-0.1.0.md` itself, which checks it against the source code class by class and names each one; this file records only 90 + 3**. +3 and +11 are increments, 21 is the number of decisions, 22 is the number of profiles, and 155 / 90 / 22 / 18 / 117 / 299 are the verified values of 0.0.4; every count beyond those points item by item in the table above to the build fact card, the source code or an existing decision, with the conversion written on the same line.

---

## Translator's notes

1. `plan0.1.0.txt:5` and §1.1 of the source write "已实现并发版的 0.0.4" / "0.0.4 已实现并发版"; "并发" (concurrency) reads like a typo for "发布" (released), and `27-PROMPT-TASKS-0.1.0.md` §2.1 says "v0.0.4 已发版". Rendered here as "implemented and released" in both places; the numbers and the structure are untouched.
2. Four source statements are kept as written and are, by nature, negative or meta statements about wording; they are not descriptions of behaviour: the "what the farmland rule is called" row of §0 (the farmland quota is not called a gamerule), item 1 of §6 (no gamerule is registered), the "is not a gamerule" half of item 1 of D-116, and the wording prohibition in row 91 of §8 (never write the Zangief Cane as levitation). Row 91 of §8 is also the only place in this file that mentions levitation, and it does so as a historical statement about superseded 0.0.1 wording or as an explicit prohibition.
3. `蓄力条` and `充能条` are used interchangeably in the source for the same Wild Hunter HUD element; both are rendered "charge bar" here, so a sentence that counts "two" bars still counts two.
4. Row 23 and row 91 of §8, and several rows of §4.2 / §7.2, quote log lines and section titles of the tooling and of the Chinese documents in Chinese (`破障类 2 → FAIL`, `「类别 · 破障类 4」`, and so on). They are left in the original Chinese on purpose: they are quoted output or quoted titles, and rule "log lines word for word" applies to them.
5. The source's §4.3 third bullet says "two places" and then lists them as 1 and 2, which matches; no contradiction found there.
6. Two statements of this document were re-adjudicated in `27-PROMPT-TASKS-0.1.0.md` §0.4 and have been mirrored into this file from the Chinese original: A-26 replaced the "draw T at random, then d1 = d2 = floor(T/3)" of item 3 of D-110 with three equal segments, `d = 134 + floorMod(pos.hashCode(), 167)` and `T = 3d ∈ [402, 900]`, and A-28 corrected the "12 colours" of D-105's rationale and of §8 row 32 to **11 colours** (12 being 11 colours + the transparent entry). "400" now stands only as a contrast or an arithmetic input (402 is not 400; 134 = ⌈400 / 3⌉), and "12 colours" only as the misremembering being corrected.
