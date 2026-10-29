# 10 · v0.0.2 Decision Record

> This file is the single source of truth for 'Farmstorm' v0.0.2: its numbering continues `00-DECISIONS.md` from D-48 and covers D-49 ~ D-79.
> The existing content of v0.0.1 still follows `00-DECISIONS.md`, and this file does not override it; the new content of v0.0.2 follows this file.
> Source: the author's v0.0.2 requirement text + the build fact card `.work/v002-BRIEF.md` (§2 decision summary, §3 registry object delta, §4 number-discipline table). The fact card is a flattened working draft of the build period and is not a deliverable.

---

## 1. Force and scope of this file

| Item | Value |
|---|---|
| File | `docs/zh/10-DECISIONS-0.0.2.md` (English counterpart `docs/en/10-DECISIONS-0.0.2.md`) |
| Version | 0.0.2 |
| Decision number range | D-49 – D-79, 31 entries, consecutive with no gaps |
| Subject matter covered | delivery and documentation, 4 new profiles and 5 new items, the three intrusive effects, the media pipeline, 4 new recipes, the Compressed Hay Block numbers, the Molotov landing semantics, one scope clarification on the attribution of the demolition threshold (D-78), and one revision of the input lock implementation (D-79) |
| Subject matter not covered | D-01 – D-48 and C-1 – C-10 (inheritance in §7); anything in `docs/zh/00`–`09` and `docs/en/00`–`09` (frozen by D-49) |
| Conflict ruling | existing v0.0.1 clauses → `00-DECISIONS.md`; new v0.0.2 clauses → this file |

- **Force**: once these 31 entries are registered they are the final scope for v0.0.2. `11-DESIGN-0.0.2.md`, `12-TECH-0.0.2.md`, `13-TASKS-0.0.2.md`, `14-PROMPT-MAIN-0.0.2.md`, `15-PROMPT-TASKS-0.0.2.md` and the code of the implementation phase must all agree with this file; where they conflict, this file wins.
- **Relation to v0.0.1**: none of v0.0.1's numbers, recipes, classification or engineering constraints is changed (the forbidden list is in §6), so that content still follows `00-DECISIONS.md`. This file only states, item by item in §7, how D-01 – D-48 are inherited, refined and extended.
- **Relation to the build fact card**: `.work/v002-BRIEF.md` is responsible for flattening the author's text into a fact card; this file is responsible for turning it into formal decision lines. The 'Decision' wording and numbers here are therefore transcribed verbatim from fact card §2, with no rewriting and no recalculation; the 'Rationale' gives numbers, source symbols or fact card sections.
- **Marking of additions**: content that is not in the fact card and is supplied by this file is marked 'supplemented by this document'. There are exactly three such places: the Chinese category names set against the enum constant names in §4, the way the scope clarification is expanded in §5, and the 48-row inheritance table in §7 — none of them introduces a new number.
- **Citation style**: references to existing documents are written with the file name, as in `01-DESIGN.md §3.4`; references to source code are written as paths relative to the repository root.
- **Forbidden wording**: the three kinds of uncertain statement forbidden by the build fact card (open items, to-do markers, conclusions with vague conditions) appear nowhere. When a judgement is needed and the fact card does not give it directly, it is derived from the existing decisions in §3 and marked 'supplemented by this document' at that place.

---

## 2. Deliverable structure (v0.0.2 documentation map)

Chinese and English correspond one to one with matching numbering; terminology follows C-10 of `00-DECISIONS.md` and column 6 of §4 of this file (English name).

```
D:\project_farm\docs\zh\
├── 00-DECISIONS.md              v0.0.1 decision record (frozen, D-49)
├── 01-DESIGN.md  …  09-IMPL-VS-PLAN.md
│                                 the other 9 v0.0.1 files (frozen, D-49)
├── 10-DECISIONS-0.0.2.md        this file: D-49 … D-79
├── 11-DESIGN-0.0.2.md           full design specs for the 7 new features
├── 12-TECH-0.0.2.md             new registry objects, class design, resource list, three client subsystems, risk register
├── 13-TASKS-0.0.2.md            task breakdown from F-60 + dependency graph + milestones + effort
├── 14-PROMPT-MAIN-0.0.2.md      one main prompt
└── 15-PROMPT-TASKS-0.0.2.md     one staged task list
```

`docs/en/` has the same structure: `00`–`06` and `09` are v0.0.1 frozen files, `10`–`15` are new this round. `07-PUBLISH.md` and `08-DEV-GUIDE.md` never had an English version in v0.0.1, and whether this round fills the gap follows D-49: the six files `10`–`15` must exist in both Chinese and English, while the two missing English v0.0.1 files are not filled in this round.

| File | Content it owns | Decisions in this file that define it |
|---|---|---|
| `10-DECISIONS-0.0.2.md` | D-49 … D-79 one line each; the scope clarification section; the 19-entry profile summary table; what is explicitly not done; the inheritance relation to v0.0.1 | all 31 |
| `11-DESIGN-0.0.2.md` | number tables, state machines, boundaries and anomalies, config references for the 7 new features | D-57 (the consequences of 6.0 must be written into the design document), D-58, D-59, D-64, D-65, D-70, D-73, D-74 |
| `12-TECH-0.0.2.md` | item-by-item table of new registry objects, class design, resource list, three client subsystem plans, risk register RT-10…RT-16, new config keys, acceptance self-check | D-50, D-53, D-60, D-61, D-62, D-66, D-68, D-69, D-76 |
| `13-TASKS-0.0.2.md` | task breakdown from F-60, dependency graph, outline, effort, critical path | D-51, D-52, D-63, D-67 (task ownership of the new advancements and the new recipes) |
| `14-PROMPT-MAIN-0.0.2.md` | one main prompt | all 31 |
| `15-PROMPT-TASKS-0.0.2.md` | one staged task list | all 31 |
| `README.md` index section | v0.0.2 documentation navigation | D-49, D-66 (asset provenance and the non-commercial personal use statement) |

---

## 3. Decision list

Each entry is one subsection with a fixed set of four fields: ID / Decision / Rationale / Where it lands. 'Decision' is transcribed verbatim from build fact card §2.

### 3.1 Delivery and documentation (D-49 ~ D-50)

#### D-49 · The v0.0.1 documents are frozen; v0.0.2 starts 6 new files

- **ID**: D-49
- **Decision**: `docs/zh/00`–`09` and `docs/en/00`–`09` are left untouched throughout. v0.0.2 adds six files `10`–`15` under each of `docs/zh/` and `docs/en/`, continuing the numbering. README gains an index section.
- **Rationale**: v0.0.1 has already been released and its documents and its jar correspond to the same version; editing old files would put the released content out of step with the documents and would make it impossible to answer 'which version said what'. Continuing the numbering instead of renumbering preserves the existing references to D-01 – D-48 in `00-DECISIONS.md`, in the v0.0.1 documents and in code comments.
- **Where it lands**: `docs/zh/10`–`15`, `docs/en/10`–`15`, the v0.0.2 index section of `README.md`; the division of labour is in §2 of this file.

#### D-50 · Still 0 custom network packets

- **ID**: D-50
- **Decision**: the 'the server tells the client the state' needed by the three intrusive effects (grayscale, red highlight, input lock) all goes through a **hidden MobEffect**: the presence or absence of the effect + the amplifier + the remaining duration make up the complete state, and the vanilla `ClientboundUpdateMobEffectPacket` synchronizes it for free. Adding a `CustomPacketPayload` for this is forbidden.
- **Rationale**: C-6 has already made 'no custom S2C network packet' a hard constraint and D-40 has ruled out Mixin, so a hidden `MobEffect` is the only ready-made channel that delivers 'phase / remaining duration' to the client and lets the client decide locally without adding a packet and without changing vanilla classes. Of the three effects, grayscale and red highlight need only 'on / off + remaining duration', the input lock needs only 'on / off', and the Zangief Cane inversion needs three phases, so the number of amplifiers covers all of it.
- **Where it lands**: `12-TECH-0.0.2.md` (the sync scheme and amplifier phase table for the three effects); `mob_effects` registration +2 (`wild_hunter`, `zangief_piledriver`); `client/FarmstormClient.java` (the client reads the effect state and drives post-processing and input interception).

### 3.2 Incremental content (D-51 ~ D-58)

#### D-51 · None of the three new crops has a planting form

- **ID**: D-51
- **Decision**: `CropProfile.cropBlock` and `seedItem` of `molotov` / `zangief_cane` / `wild_apple` are all `null`; no crop block is registered and no seed is registered. Precedent: `melon_slice_bomb` (likewise no crop block). All three are produced by the converter only.
- **Rationale**: the precedent `melon_slice_bomb` likewise had both `cropBlock` and `seedItem` empty in v0.0.1, which shows the profile model already allows 'no planting form', so this entry needs no new interface. These three are obtained through the converter (D-54 and the three conversion recipes in build fact card §3.2), and giving them a planting form would instead make the design 'the converter is this version's entry point' meaningless.
- **Where it lands**: `util/CropProfile.java` (`cropBlock` and `seedItem` of the three profiles set to `null`); `registry/FarmstormItems.java` (no seed item registered); `datagen/ModItemModelProvider.java` (the item icon goes through `item/generated`); the profile table in `12-TECH-0.0.2.md`.

#### D-52 · Converter crafting recipe

- **ID**: D-52
- **Decision**: shaped 3×3: `minecraft:crafting_table` in the centre, the eight surrounding slots take the item tag `farmstorm:converter_fuel`, producing 1 `farmstorm:converter`. The content of that tag = vanilla crops + every improved crop of this mod (every item across the 19 profiles that can be treated as a 'crop'). The v0.0.1 converter previously had no recipe at all; this recipe is the **first** one.
- **Rationale**: v0.0.1 has 0 crafting recipes, and the converter has only an item and a GUI with no way to obtain it; for the conversion recipes to be usable, the converter must first get a crafting path. A tag instead of a hard-coded item list is used so that the set 'vanilla crops + improved crops' is maintained in one place only, and so that adding content later needs no recipe edit.
- **Where it lands**: `datagen/ModRecipeProvider.java` (shaped 3×3 crafting, crafting recipes +1); `util/FarmstormTags.java` + `datagen/ModItemTagsProvider.java` (new `farmstorm:converter_fuel`, item tags new +1).

#### D-53 · Media pipeline scope

- **ID**: D-53
- **Decision**: the Minecraft sound engine decodes Ogg Vorbis only. All three source files are converted before entering `src/main/resources`:
  - audio → **mono, 64 kbps Ogg Vorbis**
  - effect icon JPG → **18×18 PNG**, no alpha-channel requirement
  The conversion script ships with the documents; when `ffmpeg` is missing it **exits with an error immediately and prints installation instructions** — silently skipping is not allowed, and falling back to the original extension is not allowed.
  (Measured on this machine: **a usable ffmpeg is present** — a 7.1 essentials build carrying libvorbis at `D:\dsh\default\sf6_audio\tools\ffmpeg.exe`, but it is **not on PATH**, so the script needs `-Ffmpeg <absolute path>`. Note that the 6.1.1 build bundled with Trae CN is trimmed and has **no libvorbis**, so it is unusable. npm / maven / mirrors are all unreachable, which is why the script downloads nothing.)
- **Rationale**: both audio sources are MP3 and the icon source is JPG, so renaming them and dropping them into the jar fails at load time instead of degrading silently. The script chooses 'missing ffmpeg exits with an error' because a silent skip would produce a false pass — 'two effects have no sound and one effect has no icon, yet the build reports success' — which conflicts with D-66's delivery requirement.
- **Where it lands**: the resource list and conversion script description in `12-TECH-0.0.2.md`; the conversion script ships with the documents; the 3 Ogg files referenced by `assets/farmstorm/sounds.json` and the effect icon enter `src/main/resources`.

#### D-54 · The Wild Apple conversion recipe is bidirectional

- **ID**: D-54
- **Decision**: `minecraft:golden_apple` ⇄ `farmstorm:wild_apple`, experience cost 3, no catalyst. Same shape as the 13 bidirectional recipes of v0.0.1 (`ConverterRecipe`, `bidirectional=true`).
- **Rationale**: D-12 fixes the conversion cost at 3 experience points each and requires a catalyst only for the Torchflower and the Pitcher Plant; this entry follows that, so the Wild Apple does not use the Nether Star slot. The reason for bidirectional is the same as for the 13 recipes of v0.0.1: `ConverterRecipe` has only one implementation and bidirectional is its normal shape, while one-way would need extra configuration. The conversion recipe total therefore goes from 21 to 24 (the number-discipline table of build fact card §4).
- **Where it lands**: `datagen/ModRecipeProvider.java` (conversion recipes +1, `bidirectional=true`); the cost key of `config/FarmstormConfig.java` reuses the existing conversion cost; the conversion recipe list in `12-TECH-0.0.2.md`.

#### D-55 · The Lucky Berry random pool is fixed at 16 kinds

- **ID**: D-55
- **Decision**: `LUCKY_BERRY_POOL` = the 19 profiles minus `lucky_berry`, `wild_apple` and `compressed_puffed_wheat` — **16 entries as the result**. Reason for excluding the Wild Apple: it is the strongest item of this version and should not come out of a single berry at random; reason for excluding the Compressed Puffed Wheat: it is a 'second-tier composite', not an improved crop (D-56). The pool changes from the automatic derivation '`ALL` minus Lucky Berry' to an **explicit exclusion of three entries**.
- **Rationale**: the automatic derivation holds while every profile is an 'improved crop'; with `wild_apple` and `compressed_puffed_wheat` added this round, the derivation would automatically pull both into the random pool, where the Wild Apple would break the exchange strength and the Compressed Puffed Wheat is not a crop at all (D-56). The derivation therefore has to become an explicit list. On pool contents: 19 profiles minus the three (`lucky_berry`, `wild_apple`, `compressed_puffed_wheat`) gives 16, which matches 'Lucky Berry pool 14 → 16' in build fact card §4 and matches the self-check wording of `12-TECH-0.0.2.md` §11 #3 and F-62 of `13-TASKS-0.0.2.md`.
- **Where it lands**: `util/CropProfile.java` (`LUCKY_BERRY_POOL` changed to an explicit exclusion of three — the constant is defined in `CropProfile` and `util/LuckyBerryRoller.java` only consumes it, the logic is unchanged); `config/FarmstormConfig.java` gains no new key (the pool contents are fixed and do not go into config).

#### D-56 · The Compressed Puffed Wheat joins the profile table

- **ID**: D-56
- **Decision**: `compressed_puffed_wheat` is the 19th `CropProfile`, type `THROWABLE`, category `CONSTRUCTION`, but `cropBlock` / `seedItem` / `kind` are all empty, and it **enters neither the converter recipes nor the Lucky Berry pool**. It belongs to the 'second-tier composites', not to the 'improved crops'.
- **Rationale**: it is a second-tier composite (4 `puffed_wheat` craft 1, D-67) whose input is itself an improved crop. If it took part in the conversion recipes and the random pool, it would form an 'improved crop → improved crop' shortcut and would make the random pool self-multiply. §4: `CropProfile` 15 → 19, Throwable 8 → 10, Construction 1 → 3.
- **Where it lands**: `util/CropProfile.java` (the 19th profile); `datagen/ModRecipeProvider.java` (not in the conversion recipes); the startup self-check message of `Farmstorm.java` (19 crops); the profile table in `12-TECH-0.0.2.md`.

#### D-57 · Compressed Hay Block numbers

- **ID**: D-57
- **Decision**: `BlockBehaviour.Properties.of().strength(30.0F, 6.0F).requiresCorrectToolForDrops()`. A six-sided block, not rotatable (no pillar/axis). Texture = the grayscale version of the vanilla hay block's side.
- **Rationale**: measured results — `ANCIENT_DEBRIS` is `strength(30.0F, 1200.0F)`, `HAY_BLOCK` is `strength(0.5F)` (the same pair of numbers governs hardness and resistance together). The consequences of taking 6.0 (**must be written into the design document**): the Melon Grenade and the Melon Slice Bomb (threshold 6.0) **cannot break it** (the predicate is `resistance < maxResistance`, and 6.0 is not less than 6.0); the Ubi Server and the Toxic Ubi Server (threshold 3.6e6) **can break it**; **vanilla explosions (TNT included) can break it too** — the vanilla formula accumulates cell by cell as `f -= (resistance + 0.3) * 0.3` and breaks when `f > 0`; with TNT power 4 the initial f range is [2.8, 5.2], and 6.0 resistance cannot hold off a cobblestone-grade block.
- **Where it lands**: `registry/FarmstormBlocks.java` (`strength(30.0F, 6.0F).requiresCorrectToolForDrops()`, an ordinary `Block`); `datagen/ModBlockStateProvider.java` (same texture on six faces, no axis variant); `datagen/ModLootTableProvider.java`; `datagen/ModBlockTagsProvider.java` (one addition each to `minecraft:mineable/pickaxe` and `minecraft:needs_diamond_tool`); `11-DESIGN-0.0.2.md` must state the consequences of 6.0 for the Melon Grenade / Melon Slice Bomb, the Ubi Server / Toxic Ubi Server and vanilla explosions (TNT included).

#### D-58 · Molotov landing semantics

- **ID**: D-58
- **Decision**: the landing point triggers one explosion of **power 0**, and that explosion acts **only as the landing trigger**: 0 entity damage, 0 block destruction (with the vanilla `Explosion` radius at 0 the damage formula yields Infinity, so there is no damage by construction). The real effect is: with the landing point as the sphere centre and within a radius of 4 blocks, **every block whose top surface touches air** is rolled independently — 85% places fire in **the air position above it**, 15% places a magma block in **the air position above it**. Neither replaces nor destroys the original block.
- **Rationale**: the source text gives the trigger basis as 'destruction permission is decided only by the Demolition category (the D-19 iron rule), while the Molotov's categories are Damage and Construction and it has no Demolition tag'. According to `00-DECISIONS.md` §3, 'block destruction permission is decided only by `CropCategory.DEMOLITION`' is **D-48**, while D-19 governs the resistance threshold; the conclusion is unchanged and this rationale should be read with D-48 (supplemented by this document: a correction of the number it points at, not a change of scope).
- **Where it lands**: the Molotov projectile under `entity/thrown/` (one of the `entity_types` +2); `config/FarmstormConfig.java` (three keys: `molotov.effectRadius` 4.0 blocks, `molotov.fireChance` 85.0 %, `molotov.triggerExplosionPower` 0; the remaining percentage places magma blocks automatically, with no second probability key — ruling A-01 of build fact card §8); the Molotov section of `11-DESIGN-0.0.2.md`.

### 3.3 The three intrusive effects (D-59 ~ D-77)

The number range of this group is D-59 ~ D-77. The main line is the three intrusive effects — grayscale (D-61), red silhouette (D-62) and input lock (D-60) — together with the presentation, control, sync and content layers that must be solved in the same batch for them to land: camera (D-59 / D-70), advancements (D-63), target acquisition (D-64), pull dash (D-65), audio (D-66 / D-76), the Zangief Cane itself (D-68 / D-69 / D-71 / D-72 / D-73), the Wild Hunter itself (D-74 / D-75), and the Compressed Puffed Wheat (D-67 / D-77). The grouping is only so that it reads in blocks; the numbering and the order still follow the fact card.

| Subsystem | Decisions involved | Technical landing point |
|---|---|---|
| Screen takeover (camera) | D-59, D-70 | `ViewportEvent.ComputeCameraAngles`, render-layer angles only |
| Full-screen grayscale | D-61 | one full-screen pass in `RenderGuiEvent.Post` |
| Red silhouette | D-62 | redraw of the model in `RenderLivingEvent.Post` |
| Input lock | D-60 | five events, each covering one segment (**revised by D-79 to seven mechanisms**) |
| Wild Hunter target acquisition and displacement | D-64, D-65 | client-side prediction + server-side ruling |
| State sync | D-50, D-69 | the hidden `MobEffect` amplifier used as the phase number |
| The Zangief Cane itself | D-68, D-71, D-72, D-73 | deployment block and spherical trigger check |
| Audio | D-66, D-76 | `sounds.json` + client-side looping `SoundInstance` |
| Advancements | D-63 | `ModAdvancementProvider` +3 |
| Compressed Puffed Wheat | D-67, D-77 | shapeless crafting + reuse of the Puffed Wheat landing write |

#### D-59 · The Zangief Cane's 'upside-down flip' = roll + pitch superimposed

- **ID**: D-59
- **Decision**: the screen rolls 180° around the view axis (roll = 180) **and** pitch is negated; both take effect together. Doing only one of the two is wrong.
- **Rationale**: roll alone looks like 'the screen tipped sideways', inverted pitch alone looks like 'looking up at the ceiling', and only the two superimposed give the 'upside-down flip' the source text asks for. Iron rule: change render-layer angles only, never write back to `player.getYRot()` / `getXRot()`; the player's real facing is unchanged and server authority is not broken.
- **Where it lands**: `client/FarmstormClient.java` (`ViewportEvent.ComputeCameraAngles`); the camera section of `12-TECH-0.0.2.md`; the effect section of `11-DESIGN-0.0.2.md`.

#### D-60 · Keyboard and mouse lock scope

- **ID**: D-60
- **Decision**: locked: movement keys, jump, attack, use/place, hotbar switching (1–9 and the scroll wheel), opening the inventory (E), chat (T / slash). **ESC alone is left open.** Rationale: locking ESC out would make players think the game has frozen.
- **Rationale**: the only reason given in the source text is 'locking ESC out would make players think the game has frozen', so ESC must always pass. Technically five events each cover one segment (movement/jump/sneak, attack/use, hotbar 1–9, scroll wheel, inventory/chat), and missing one leaks. This entry is the review object of RT-13.
- **Where it lands**: `client/FarmstormClient.java` (`MovementInputUpdateEvent`, `InputEvent.InteractionKeyMappingTriggered`, `InputEvent.Key`, `InputEvent.MouseScrollingEvent`, `ScreenEvent.Opening`); the input lock section and RT-13 of `12-TECH-0.0.2.md`.

#### D-61 · Black and white includes the GUI

- **ID**: D-61
- **Decision**: the grayscale post-processing acts on the whole-world rendering **and** on the GUI layer; it is a full-screen grayscale, not a fade of the world alone.
- **Rationale**: fading the world alone would leave a coloured inventory bar and health bar, and the player would see at a glance that 'only the world changed colour', a look too far from the vanilla Blindness effect. The preferred mount point `RenderGuiEvent.Post` sits after the GUI and before the screen, so a single pass naturally covers world and GUI at once, which satisfies this entry exactly.
- **Where it lands**: `assets/farmstorm/shaders/core/grayscale.fsh` + `assets/farmstorm/post_effect/grayscale.json` (post_effect +1, plus 2 GLSL); the grayscale section and RT-10 of `12-TECH-0.0.2.md` (faking grayscale with fog colour or a texture swap is forbidden).

#### D-62 · The red highlight draws only what is visible

- **ID**: D-62
- **Decision**: a red silhouette is drawn only for mobs that are **visible in line of sight** (not occluded by a block) and at a distance of **≤ 64 blocks**. The player itself and same-team players are excluded. No through-wall drawing.
- **Rationale**: a through-wall highlight marks 'enemies you cannot see' as well, which is far stronger than the vanilla Glowing effect; 64 blocks is the distance cap given by the source text, and together they form the strength cap. Excluding the player itself and same-team players follows D-06's ally scope. Technically the redraw must use `getModel().renderToBuffer` and not `EntityRenderDispatcher#render`, otherwise it recursively fires its own event.
- **Where it lands**: `client/FarmstormClient.java` (`RenderLivingEvent.Post` + `player.hasLineOfSight` + distance check); `assets/farmstorm/textures/misc/white.png`; the silhouette section and RT-11 of `12-TECH-0.0.2.md`.

#### D-63 · 3 new advancements

- **ID**: D-63
- **Decision**: `first_molotov` (first Molotov thrown), `zangief_hit` (the Zangief Cane's first hit), `first_wild_hunter` (first time the Wild Hunter effect is obtained). The existing 12 are untouched.
- **Rationale**: all three new gameplay loops need a 'first achieved' receipt, otherwise players cannot confirm they triggered it and there is no collectible to show. The existing advancements stay untouched because C-7 has already pinned the advancement count and the lang key count into a one-to-one correspondence, so changing an existing entry would drag the language files along. Build fact card §4: advancements 12 → 15.
- **Where it lands**: `datagen/ModAdvancementProvider.java` (+3); `datagen/ModLangProvider.java` (advancement lang keys generated in step with C-7's scope; the Chinese and English key sets must be equal); the advancement task in `13-TASKS-0.0.2.md`.

#### D-64 · Wild Hunter target acquisition scope

- **ID**: D-64
- **Decision**: candidates = `LivingEntity` within **5 blocks** of the player as centre and at an angle of **≤ 30°** to the player's view direction, excluding the player itself, same-team players and spectators. Ordering: smallest angle first; equal angles take the nearer one. 'Closest to the crosshair on screen' is 'smallest angle to the view direction'; the server can compute it and needs no reading of the client's mouse position.
- **Rationale**: once 'closest to the crosshair' is made equivalent to 'smallest angle to the view direction', client and server can run the same deterministic algorithm, which needs neither the mouse position nor a new sync packet for it (D-50). The exclusions agree with D-06's ally scope, and the spectator exclusion agrees with the `isSpectator()` exemption of the three v0.0.1 traps.
- **Where it lands**: `client/FarmstormClient.java` (local prediction of the same target); the server-side adjudication entry running the same algorithm (a new class, name set by `12-TECH-0.0.2.md`); `config/FarmstormConfig.java` (one key each for the 5 blocks and the 30°); the Wild Hunter section of `11-DESIGN-0.0.2.md`.

#### D-65 · The pull uses a dash, not a teleport

- **ID**: D-65
- **Decision**: the server computes an initial velocity from the current distance that carries the player into place within **3 ticks**; the velocity is sent through `setDeltaMovement` and **the client simulates movement and collision as usual**. Hitting a wall stops it, no gap-filling, no pass-through. If the landing spot is occupied by a block, that dash simply fails (no pull this time).
- **Rationale**: a teleport skips collision detection and position interpolation, so hitting a wall either passes through it or wedges the player into a block; a dash lets the client run vanilla collision as usual, and 'hitting a wall stops it' is the natural result of vanilla feel. RT-12 has already registered the risk of 'judging the driving side wrongly', and the preferred approach is client-side position prediction + a server-side ruling; the red line remains no new network packets (D-50).
- **Where it lands**: the dash section and RT-12 of `12-TECH-0.0.2.md`; `client/FarmstormClient.java` (predicted displacement when the attack key is pressed locally); the server's adjudication entry on receiving `ServerboundSwingPacket`; `config/FarmstormConfig.java` (`wild_hunter.dashTicks` 3 ticks; for the list see build fact card §8).

#### D-66 · Audio packaging and provenance

- **ID**: D-66
- **Decision**: the three source files are converted and packaged into the jar. README and the Release notes **must** state the provenance and the non-commercial personal use:
  - `Animals.mp3` → `wild_hunter` loop sound
  - the SF6 clip `BV1qu411W7ha_846-850.mp3` → Zangief Cane trigger sound
  - the desktop JPG → Wild Hunter effect icon
- **Rationale**: none of the three assets is an original sound source, so a public release must state provenance and non-commercial personal use, otherwise the Release carries copyright risk. It is written as 'must' rather than 'recommended' because a miss cannot be corrected later in the description of an already distributed jar.
- **Where it lands**: the provenance statement section of `README.md`; the Release notes template in `docs/zh/07-PUBLISH.md` (that file is a v0.0.1 frozen file: fill it in following its template only, do not change the file itself); the resource list of `12-TECH-0.0.2.md`.

#### D-67 · The Compressed Puffed Wheat recipe is shapeless

- **ID**: D-67
- **Decision**: shapeless crafting: **4** `farmstorm:puffed_wheat` in any positions → **1** `farmstorm:compressed_puffed_wheat`.
- **Rationale**: 4 for 1 is a shapeless equivalence, and making it shaped would only force players to memorise the arrangement without producing any benefit; it is also this version's only 'improved crop → second-tier composite' recipe. The recipe counts have to follow it as well (build fact card §4: crafting recipes 0 → 2, this being the 2nd).
- **Where it lands**: `datagen/ModRecipeProvider.java` (shapeless crafting); the recipe task in `13-TASKS-0.0.2.md`; the recipe list of `12-TECH-0.0.2.md`.

#### D-68 · The Zangief Cane is invisible in the world

- **ID**: D-68
- **Decision**: the deployment block follows the v0.0.1 trap scope (D-48): **completely invisible** in the world (empty model, no elements), keeping only `textures.particle`; the item icon goes independently through `item/generated`.
- **Rationale**: D-48 has already fixed 'trap deployment blocks are completely invisible in the world' as the appearance scope, and the Zangief Cane is a Trap (§4 row 17: category `TRAP`), so it simply follows. The item icon must go independently through `item/generated`, otherwise the inventory icon would turn blank along with it.
- **Where it lands**: `datagen/ModBlockStateProvider.java` (empty model + `textures.particle`); `datagen/ModItemModelProvider.java` (`item/generated`); `behavior/DeployedKind.java` (`DeployedKind` enum +1 value `ZANGIEF_CANE`); `registry/FarmstormBlocks.java`.

#### D-69 · The Zangief Cane's Levitation level

- **ID**: D-69
- **Decision**: 'Levitation 7 for 1 second' = `MobEffects.LEVITATION`, amplifier **6** (= level VII), lasting **20 ticks**. Applied on the **14th tick** after the trigger.
- **Rationale**: vanilla effect levels start at amplifier 0 (level I), so 'Levitation 7' corresponds to amplifier 6; 20 ticks is exactly 1 second. It is applied on the 14th tick rather than immediately in order to leave D-59's flip and D-70's spin a stretch to play first; the timing is given by that entry. The phase switch is done by re-applying an effect with the same id and a different amplifier (D-50).
- **Where it lands**: the Zangief Cane's trigger and phase logic (the phase table of `DeployedKind.ZANGIEF_CANE`); the amplifier phase table of `12-TECH-0.0.2.md`; the phase timing table of `11-DESIGN-0.0.2.md`.

#### D-70 · Horizontal spin rate

- **ID**: D-70
- **Decision**: before landing, yaw keeps increasing by **+72° per tick** (1440°/s ÷ 20 ticks/s = 72°/tick; 40 ticks accumulate 2880° = 8 full turns), the direction being counter-clockwise (yaw increasing).
- **Rationale**: the source text (`plan0.0.2.txt` line 29) says '1440 degrees per second'; dividing by 20 ticks per second gives 72°/tick, 40 ticks accumulate 2880° = 8 full turns, and the direction is given as counter-clockwise (yaw increasing). The spin accumulates per tick on the client and does not depend on per-tick server sync, so it produces no extra packets (D-50); the write point is still `setYaw(yaw + accumulated)` in `ComputeCameraAngles`, not written back to the player's real facing (D-59's iron rule).
- **Where it lands**: `client/FarmstormClient.java` (yaw accumulation in `ViewportEvent.ComputeCameraAngles`); `config/FarmstormConfig.java` (`zangief_cane.spinDegreesPerTick`, default 72.0, range 0.0–360.0; for the list see build fact card §8); the camera section of `12-TECH-0.0.2.md`.

#### D-71 · The Zangief Cane affects players only

- **ID**: D-71
- **Decision**: the trigger recognizes **non-ally players** only, not mobs. Creative-mode players and spectators do not trigger it (consistent with the `isCreative()` / `isSpectator()` exemptions of the three v0.0.1 traps).
- **Rationale**: D-07 states that Trap and Turret categories target non-ally players only and do not handle mobs (the Pumpkin Turret exception is given separately by D-43 and does not apply to this entry). The creative and spectator exemptions stay consistent with the existing checks of the three v0.0.1 traps, avoiding two sets of exemptions inside one category.
- **Where it lands**: the Zangief Cane's trigger check entry; the Zangief Cane section of `11-DESIGN-0.0.2.md`; the trap check table of `12-TECH-0.0.2.md`.

#### D-72 · Silent disappearance after the trigger

- **ID**: D-72
- **Decision**: one-shot trap: after the trigger the block is **silently removed and produces no drop** (precedent: Blast Carrot D-46). This entry constrains only the **trigger disappearance** path and not the loot table; the two paths stay separate:
  - the trap silently removed after the trigger → goes through `level.removeBlock` / `consume()`, **passes through no loot table**, hence no drop;
  - the player mining the deployment block itself → **goes through the loot table normally and drops itself once** (consistent with the five deployment blocks of v0.0.1).
- **Rationale**: D-46 has already fixed 'silently vanishes after the trigger, produces no drop' as the **trigger path** behaviour of a one-shot trap, and explains that the Blast Carrot's earlier drop came from its explosion destroying its own block and then running the loot table and has been removed at the rule layer — it constrains the instant of triggering, not the loot table itself. The five v0.0.1 deployment blocks (`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus`) already had loot tables and drop themselves when the player mines them, and the Zangief Cane does not change that; nor may the implementation delete the table to make the trigger produce no drop.
- **Where it lands**: the Zangief Cane trigger logic (silent `removeBlock` / `consume()`, no loot table); `datagen/ModLootTableProvider.java` (keeps the 'drops itself once' loot table, same shape as the five v0.0.1 deployment blocks); the Zangief Cane section of `11-DESIGN-0.0.2.md`; the loot table entries of `12-TECH-0.0.2.md` §3.14 and §7.2 aligned to this scope.

#### D-73 · The trigger radius is spherical

- **ID**: D-73
- **Decision**: 1.5 blocks is judged by **spherical Euclidean distance** (not Chebyshev).
- **Rationale**: D-20 says the trigger checks of traps and turrets use Chebyshev distance, but Chebyshev enlarges the diagonal trigger range into a square, which does not match the circular impression the player sees. This entry is an explicit exception to D-20, acting on the Zangief Cane only and not changing D-20 itself (see the D-20 row in §7).
- **Where it lands**: the Zangief Cane's trigger check (spherical Euclidean distance); `config/FarmstormConfig.java` (the 1.5-block key); the Zangief Cane section of `11-DESIGN-0.0.2.md`.

#### D-74 · No cap on kill-based extension

- **ID**: D-74
- **Decision**: during Wild Hunter, each mob killed: remaining duration **+8 seconds**, restore **10 health points**. No count cap (the source text states no cap, so none is added unilaterally).
- **Rationale**: the source text gives only the per-kill increment and no count cap; capping it myself would change the strength curve the author gave, which is an unauthorised balance adjustment (D-21 has made 'no balance adjustment' a principle). The implementation must therefore not add its own counter.
- **Where it lands**: the Wild Hunter kill settlement entry; `config/FarmstormConfig.java` (one key each for the +8 seconds and the 10 points, key names set by `12-TECH-0.0.2.md`, supplemented by this document); the Wild Hunter section of `11-DESIGN-0.0.2.md`.

#### D-75 · Knockback uses the vanilla default strength

- **ID**: D-75
- **Decision**: 'if damage is successfully dealt, knock the target back' = call vanilla `LivingEntity#knockback` at its default strength; no custom number.
- **Rationale**: a custom knockback strength would be inconsistent in feel with the other damage sources of this version and would also bypass vanilla's knockback resistance and attribute bonuses. Calling the vanilla method inherits those rules naturally and needs no config key for it.
- **Where it lands**: the Wild Hunter damage settlement entry; the Wild Hunter section of `11-DESIGN-0.0.2.md`; the damage settlement table of `12-TECH-0.0.2.md`.

#### D-76 · The loop sound uses a client-side SoundInstance

- **ID**: D-76
- **Decision**: Animals is played continuously by a single looping `SoundInstance` on the client; the sound source follows the player, attenuation distance 16 blocks, sound category `SoundSource.PLAYERS` (does not occupy `MUSIC` and does not interrupt vanilla background music). The server does not resend a play instruction every tick.
- **Rationale**: a per-tick play instruction from the server would produce a large number of vanilla packets and the sound would stutter while the player moves; a client-side looping `SoundInstance` follows the player by itself and is the only approach that neither adds packets (D-50) nor stutters. Using `PLAYERS` as the category is what keeps it out of the `MUSIC` channel and from interrupting vanilla background music.
- **Where it lands**: `client/FarmstormClient.java` (a `SoundInstance` subclass, `isLooping() = true`, position updated every tick); `assets/farmstorm/sounds.json` (`wild_hunter_loop`); the audio section and RT-14 of `12-TECH-0.0.2.md`.

#### D-77 · The Compressed Puffed Wheat reuses the Puffed Wheat's landing write

- **ID**: D-77
- **Decision**: first, one factual correction: **the Puffed Wheat has no explosion stage**. `src/main/java/com/dashpig/farmstorm/entity/thrown/PuffedWheatProjectile.java` does only two things — ① replace a cubic volume centred on the hit block with `minecraft:hay_block` (dry edge length 3; edge length 5 if `#minecraft:water` exists inside the volume); ② skip blocks with explosion resistance ≥ 3.6e6 (`BlastRule.UNBREAKABLE_RESISTANCE`). It triggers no `Explosion` at all, so neither 'explosion power' nor 'damage cap' exists.
  So what `compressed_puffed_wheat` reuses from the Puffed Wheat is exactly these two, **changing only the written block from `minecraft:hay_block` to `farmstorm:compressed_hay_block`**:
  - cubic write volume: dry edge length 3, edge length 5 in water, writing directly over air / fluid / solid blocks, the original block producing no drop;
  - resistance floor: skip without writing when the original block at the target position has resistance ≥ 3.6e6.
- **Rationale**: the Compressed Puffed Wheat's input is the Puffed Wheat (D-67), and the semantics of the landing write should not mutate just because one more crafting step was added; reusing the Puffed Wheat's two rules also inherits D-38's 3.6e6 floor, with no separate rule needed. `PuffedWheatProjectile` contains no `Explosion` call at all, so 'explosion power' and 'damage cap' do not exist on either of the two projectiles, and there is nothing to reuse.
- **Where it lands**: the Compressed Puffed Wheat projectile under `entity/thrown/` (one of the `entity_types` +2); the projectile table and landing write logic of `12-TECH-0.0.2.md`; no new explosion-related config keys (the single list of build fact card §8 contains no explosion power / damage cap key, supplemented by this document).

### 3.4 Errata (D-78)

> This section registers D-78, and its content is a **scope clarification**, not an error correction of the v0.0.1 documents: that version's documents and implementation have always taken the same position on threshold attribution. The three deviations registered here all sit at the symbol and coverage level, and the behaviour is right in all three.

#### D-78 · Clarification of demolition threshold attribution (scope clarification)

- **ID**: D-78
- **Decision**: first, correct a former misreading: the projectile table of `02-TECH-SPEC.md §4.9` does not unify the Demolition category onto one threshold — what it labels `UbiServerProjectile` with is `BlastRule.UNBREAKABLE_SKIP`, and it notes that 'the Demolition category sets no resistance threshold'; the original text of D-19 in `00-DECISIONS.md` likewise constrains only **the Melon Slice Bomb and the Melon Grenade**. The v0.0.1 documents have no behavioural error in threshold attribution. What really needs registering are three deviations at the **symbol and coverage** level (the behaviour is right in all three, they are simply not written strictly enough):
  1. The projectile table of `02-TECH-SPEC.md §4.9` writes the rules of `MelonSliceProjectile` and `MelonGrenadeProjectile` as the constant `BlastRule.COBBLESTONE_6`; the implementation uses the config-reading factories `BlastRule.sliceBomb()` / `BlastRule.grenade()` (default values likewise 6.0, behaviour equivalent, symbol not matching). And §4.11 of the same document lists the two keys `sliceResistThreshold` / `grenadeResistThreshold`, so §4.9 and §4.11 do not agree.
  2. The same table does not state which rule `ToxicUbiServerProjectile` uses; the implementation is `BlastRule.VANILLA`, threshold 3.6e6.
  3. The body text of `02-TECH-SPEC.md §4.9` writes 'explosion resistance ≥ cobblestone 6.0 is not broken' as a general responsibility of `BlastRule`, which is easily read as a unified threshold for the Demolition category; the accurate statement is '**this is the threshold D-19 sets for the two: the Melon Slice Bomb and the Melon Grenade**, while the other Demolition projectiles follow the 3.6e6 floor'.

  The item-by-item table and the consequences are in §5.3 and §5.5 of this file. The v0.0.1 documents are frozen by D-49 and are not changed; this entry is registered only inside the v0.0.2 documents.
- **Rationale**: measured against the four projectile classes under `src/main/java/com/dashpig/farmstorm/entity/thrown/` and `explosion/BlastRule.java`: the predicate is `resistance < UNBREAKABLE_RESISTANCE && resistance < this.maxResistance`, `UNBREAKABLE_RESISTANCE = 3.6e6F`, both `VANILLA` and `UNBREAKABLE_SKIP` take it, and only `grenade()` and `sliceBomb()` read the two 6.0 keys from config; those two keys really do exist in `config/FarmstormConfig.java` (default 6.0), i.e. §4.11 records the implementation and the constant name in §4.9 is only a simplified way of writing it. For the vanilla-explosion side, the formula basis is in D-57.
- **Where it lands**: §5 of this file (expansion and blast radius); the demolition scope of `11-DESIGN-0.0.2.md` and `12-TECH-0.0.2.md` written item by item per §5.3; `02-TECH-SPEC.md` unchanged per D-49.

---

### 3.5 Revision of the input lock implementation (D-79)

> This section registers D-79: `12-TECH-0.0.2.md` §4.3 originally specified "cancel `InputEvent.Key`", which the sources show is **not implementable**. Itemized evidence in `.work/FINDING-RT13-INPUT-LOCK.md`.

#### D-79 · The input lock steals clicks instead of cancelling, and gains a view lock

- **ID**: D-79
- **Decision**: D-60's "five events, one segment each (**revised by D-79 to seven mechanisms**, see §3.5; original wording: " becomes **seven mechanisms**, and three concrete mechanisms change:
  1. **Keyboard (hotbar 1–9 / Q / E / F / F5 etc.)**: `InputEvent.Key` is **not cancellable** (`InputEvent.java:214` does not implement `ICancellableEvent`), and `ClientHooks.onKeyInput` is the **last statement** of `KeyboardHandler#keyPress` (`:485`), i.e. after `KeyMapping.set/click` (`:483-484`). → drain `consumeClick()` inside that event instead: `handleKeybinds()` (`Minecraft.java:1837`) only consumes in the tick (`:1944`), so draining first is equivalent to blocking.
  2. **Mouse left/right**: use `InputEvent.MouseButton.Pre` (cancellable and runs first, `MouseHandler.java:82`); keep `InteractionKeyMappingTriggered` as the second net, covering `startAttack` / `startUseItem` / `continueAttack`.
  3. **View (missing from the draft)**: add `CalculatePlayerTurnEvent#setMouseSensitivity(0)` (`ClientHooks.java:368`) — the **only Mixin-free view lock**.
- **Known limitations**: F11 fullscreen, F2 screenshot, F3 debug and ESC pause are handled inside `keyPress` **before** `onKeyInput`, unreachable without Mixin; the draft's "let ESC through" is a non-requirement (it takes effect either way).
- **State reset**: if the left button is already held when the lock engages, `keyAttack.isDown()` stays true (`Minecraft.java:2035`) and mining never stops; the down-state must be cleared explicitly.
- **Basis**: `.work/FINDING-RT13-INPUT-LOCK.md` (sources from `neoforge-21.1.213-sources.jar`, every conclusion citing `file:line`).
- **Where it lands**: the event table and explanatory paragraphs of `12-TECH-0.0.2.md` §4.3.

## 4. Crop registry name summary (19 entries)

The Chinese names, registry ids, types and categories of the first 15 rows follow `00-DECISIONS.md` §4; the English names take the Title Case of the registry id per C-10; the last 4 rows follow build fact card §3.1. Chinese names always keep the exclamation marks from the source text.

| # | registry id | Type | Category | Chinese name | English name | New in v0.0.2 |
|---|---|---|---|---|---|---|
| 1 | `puffed_wheat` | THROWABLE | CONSTRUCTION | 膨胀麦 | Puffed Wheat | No |
| 2 | `blast_carrot` | DEPLOYABLE | TRAP, DAMAGE | 胡罗贝 | Blast Carrot | No |
| 3 | `ubi_server` | THROWABLE | DEMOLITION, DAMAGE | 育碧服务器 | Ubi Server | No |
| 4 | `toxic_ubi_server` | THROWABLE | BUFF, DEMOLITION, DAMAGE | 毒育碧服务器 | Toxic Ubi Server | No |
| 5 | `power_beet` | EDIBLE | BUFF | 甜菜！ | Power Beet | No |
| 6 | `melon_slice_bomb` | THROWABLE | DAMAGE, DEMOLITION | 瓜片子 | Melon Slice Bomb | No |
| 7 | `melon_grenade` | THROWABLE | DEMOLITION, DAMAGE | 瓜！ | Melon Grenade | No |
| 8 | `pumpkin_turret` | DEPLOYABLE | TURRET, DAMAGE | man瓜 | Pumpkin Turret | No |
| 9 | `awakened_torchflower` | DEPLOYABLE | TURRET, BUFF | 激活的火把花 | Awakened Torchflower | No |
| 10 | `awakened_pitcher_plant` | DEPLOYABLE | TURRET, BUFF | 激活的瓶子草 | Awakened Pitcher Plant | No |
| 11 | `fang_trap_cactus` | DEPLOYABLE | TRAP, DAMAGE | 赢过输过没死过 | Fang Trap Cactus | No |
| 12 | `bamboo_firecracker` | THROWABLE | DAMAGE | 爆竹 | Bamboo Firecracker | No |
| 13 | `red_mushroom_puff` | THROWABLE | BUFF | ？！菇菇！？ | Red Mushroom Puff | No |
| 14 | `brown_mushroom_puff` | THROWABLE | BUFF | ？！棕色菇菇色棕！？ | Brown Mushroom Puff | No |
| 15 | `lucky_berry` | EDIBLE | BUFF | 幸运浆果 | Lucky Berry | No |
| 16 | `molotov` | THROWABLE | DAMAGE, CONSTRUCTION | 莫洛托夫！ | Molotov | Yes |
| 17 | `zangief_cane` | DEPLOYABLE | TRAP | 桑吉尔蔗 | Zangief Cane | Yes |
| 18 | `wild_apple` | EDIBLE | BUFF | 狂野苹果 | Wild Apple | Yes |
| 19 | `compressed_puffed_wheat` | THROWABLE | CONSTRUCTION | 压缩膨胀麦 | Compressed Puffed Wheat | Yes |

Notes:

- The registry id is `CropProfile.id()` and also the registration ID of the item / block / entity; for deployable crops the block and the item share the same name.
- Chinese ↔ English mapping of type and category (supplemented by this document: the enum constant names come from `util/CropType.java` and `util/CropCategory.java`):

  | Type (Chinese) | `CropType` | Category (Chinese) | `CropCategory` |
  |---|---|---|---|
  | 投掷型 | `THROWABLE` | 陷阱类 | `TRAP` |
  | 部署型 | `DEPLOYABLE` | 炮台类 | `TURRET` |
  | 食用型 | `EDIBLE` | 破障类 | `DEMOLITION` |
  | — | — | buff类 | `BUFF` |
  | — | — | 建造类 | `CONSTRUCTION` |
  | — | — | 伤害类 | `DAMAGE` |

- The categories of `molotov` are DAMAGE + CONSTRUCTION (spreading fire and spreading magma blocks both count as writing blocks into the world), `compressed_puffed_wheat` is pure CONSTRUCTION; `zangief_cane` is pure TRAP with no DAMAGE tag, unlike `blast_carrot` and `fang_trap_cactus`.
- 'New in v0.0.2' is No for the first 15 rows and Yes for the last 4: 15 + 4 = 19.
- The block new in this version, `compressed_hay_block`, occupies no row of this table: it is the carrier block of the second-tier composite (D-57, D-77), its item form is its BlockItem, its English display name is Compressed Hay Block per C-10 and its Chinese name is 压缩干草块.

Counting is self-consistent (the row numbers are the serial numbers of the table above, aligned item by item with the number-discipline table of build fact card §4):

| Dimension | Value | Row numbers |
|---|---|---|
| Type · Throwable | 10 | 1, 3, 4, 6, 7, 12, 13, 14, 16, 19 |
| Type · Deployable | 6 | 2, 8, 9, 10, 11, 17 |
| Type · Edible | 3 | 5, 15, 18 |
| Category · Demolition | 4 | 3, 4, 6, 7 |
| Category · Trap | 3 | 2, 11, 17 |
| Category · Turret | 3 | 8, 9, 10 |
| Category · Buff | 8 | 4, 5, 9, 10, 13, 14, 15, 18 |
| Category · Construction | 3 | 1, 16, 19 |
| Category · Damage | 9 | 2, 3, 4, 6, 7, 8, 11, 12, 16 |
| Total | 19 | 1 – 19 |

Startup self-check message (printed by `Farmstorm#logSelfCheck`; change the numbers here together with it; the 19 / 10 / 6 / 3 / 4 of this table must agree with it):

```
自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK
```

---

## 5. Errata for the v0.0.1 documents

This section expands D-78. All three deviations are looseness at the **symbol and coverage** level, and all of them sit in the same section of the same document — `02-TECH-SPEC.md` §4.9: implementation and documents agree on threshold attribution (the Melon Grenade and the Melon Slice Bomb 6.0, the Ubi Server and the Toxic Ubi Server 3.6e6), and what is missing is only the way it is written, the coverage and the phrasing. This section restates none of the statements that were corrected; it gives only the location, the implementation and the correct scope.

### 5.1 Which places are not written strictly enough

| Place | Location | Where it is loose | What the implementation is |
|---|---|---|---|
| 1 | the projectile table of `02-TECH-SPEC.md §4.9` | the rules of the two melons are written as the constant `BlastRule.COBBLESTONE_6`, while the implementation is the config-reading factory (default likewise 6.0, behaviour equivalent, symbol not matching); and it does not agree with the two keys `sliceResistThreshold` / `grenadeResistThreshold` listed in §4.11 of the same document. | `MelonSliceProjectile` → `BlastRule.sliceBomb()`; `MelonGrenadeProjectile` → `BlastRule.grenade()`. |
| 2 | the projectile table of `02-TECH-SPEC.md §4.9` | the same table does not state which rule `ToxicUbiServerProjectile` uses, so a reader would assume it sits in the same tier as the two melons. | `BlastRule.VANILLA`, threshold 3.6e6 (the same 3.6e6 floor as `UbiServerProjectile`'s `UNBREAKABLE_SKIP`). |
| 3 | the body text of `02-TECH-SPEC.md §4.9` | writes 'explosion resistance ≥ cobblestone 6.0 is not broken' as a general responsibility of `BlastRule`, easily read as a unified threshold for the Demolition category. | 6.0 is the threshold D-19 sets for the two: the Melon Slice Bomb and the Melon Grenade; the other Demolition projectiles follow the 3.6e6 floor. |

The document involved is one (`02-TECH-SPEC.md`) and the deviations are three — two in that projectile table and the third in the body text, and the two statements do not contradict each other. All three are handled the same way: none of them changes the v0.0.1 documents (D-49); they are written into the v0.0.2 documents only, with the item-by-item scope of §5.3.

### 5.2 Measured evidence

Source paths measured (v0.0.1 released code, read only this round and not changed):

| File | Key symbols |
|---|---|
| `src/main/java/com/dashpig/farmstorm/entity/thrown/UbiServerProjectile.java` | `BlastRule.UNBREAKABLE_SKIP` |
| `src/main/java/com/dashpig/farmstorm/entity/thrown/ToxicUbiServerProjectile.java` | `BlastRule.VANILLA` |
| `src/main/java/com/dashpig/farmstorm/entity/thrown/MelonGrenadeProjectile.java` | `BlastRule.grenade()` |
| `src/main/java/com/dashpig/farmstorm/entity/thrown/MelonSliceProjectile.java` | `BlastRule.sliceBomb()` |
| `src/main/java/com/dashpig/farmstorm/explosion/BlastRule.java` | `UNBREAKABLE_RESISTANCE = 3.6e6F`; the predicate of `canBreak()` is `resistance < UNBREAKABLE_RESISTANCE && resistance < this.maxResistance`; both `VANILLA` and `UNBREAKABLE_SKIP` take `UNBREAKABLE_RESISTANCE` |
| `src/main/java/com/dashpig/farmstorm/config/FarmstormConfig.java` | `grenadeResistThreshold`, `sliceResistThreshold` (default 6.0, read only by `grenade()` / `sliceBomb()`) |
| decompiled source `net/minecraft/world/level/Explosion` | `f` starts at `radius * (0.7 + rand * 0.6)`, drops `0.225` per block along the ray, drops a further `(resistance + 0.3) * 0.3` on hitting a block, and breaks when `f > 0` |

### 5.3 Correct scope

| Projectile | Profile | Rule | Effective threshold | Against the Compressed Hay Block (resistance 6.0) |
|---|---|---|---|---|
| `UbiServerProjectile` | `ubi_server` | `BlastRule.UNBREAKABLE_SKIP` | 3.6e6 | **breaks** |
| `ToxicUbiServerProjectile` | `toxic_ubi_server` | `BlastRule.VANILLA` | 3.6e6 | **breaks** |
| `MelonGrenadeProjectile` | `melon_grenade` | `BlastRule.grenade()` | 6.0 | does not break (`6.0 < 6.0` is false) |
| `MelonSliceProjectile` | `melon_slice_bomb` | `BlastRule.sliceBomb()` | 6.0 | does not break (same as above) |
| Vanilla explosion (TNT etc.) | — | vanilla `Explosion` | per-cell accumulation formula | **breaks** (see D-57 rationale) |

- The 6.0 threshold belongs **only to the Melon Grenade and the Melon Slice Bomb**; the Ubi Server and the Toxic Ubi Server are constrained only by the 3.6e6 floor plus the two tags `#farmstorm:blast_proof` and `#farmstorm:explosion_immune`.
- The predicate runs as 'only resistance strictly less than the threshold breaks': the Compressed Hay Block, whose resistance is exactly 6.0, does not break against the two melons, and does break against the Ubi Server, the Toxic Ubi Server and vanilla explosions.
- The classification scope of the Demolition category is unchanged: still the 4 fixed by D-42 (`ubi_server` / `toxic_ubi_server` / `melon_grenade` / `melon_slice_bomb`); what is clarified is threshold attribution and the implementation symbols, not the classification.

### 5.4 Why the v0.0.1 documents are not changed and this is registered only in this file

1. All three are looseness at the symbol and coverage level and affect no behaviour: documents and implementation agree on threshold attribution, and the conclusions — 6.0 for the Melon Grenade and the Melon Slice Bomb, 3.6e6 for the Ubi Server and the Toxic Ubi Server — are the same on both sides.
2. D-49 freezes them: the v0.0.1 documents correspond one to one to the released jar, so the benefit of editing them is only the phrasing, while the cost is touching the cross-references of both the Chinese and the English sets and the consistency of the released version.
3. The implementation phase reads only the v0.0.2 documents: `11-DESIGN-0.0.2.md` and `12-TECH-0.0.2.md` are written item by item per §5.3, so the implementation receives the correct scope and there is no need to edit the old files.
4. The traceability path is intact: the D-19 row in §7 notes that D-19 remains in force and that its scope of application is clarified by D-78, so a reader of the v0.0.1 documents can follow the version number and find this.

### 5.5 Knock-on effects for v0.0.2

- D-57's Compressed Hay Block values are read per §5.3: resistance 6.0 is not broken by the Melon Grenade and the Melon Slice Bomb, is broken by the Ubi Server and the Toxic Ubi Server, and **vanilla explosions (TNT included) can break it too** — this is a consequence that `11-DESIGN-0.0.2.md` must state.
- This round changes neither the comparison logic of `BlastRule.canBreak()` nor the default values of `grenadeResistThreshold` / `sliceResistThreshold` (§6).
- The projectile table of `12-TECH-0.0.2.md` is written item by item against `BlastRule`'s constants and factories, and must add the two rows for `ToxicUbiServerProjectile` and vanilla explosions; the merged wording may no longer be used.

---

## 6. What v0.0.2 explicitly does not do

The following 11 items are this version's exclusion list; encountering any one of them during the implementation phase is no licence for an exception, and if an exception is needed, change this file first and then the code.

1. **No planting form for the three new crops**: `molotov` / `zangief_cane` / `wild_apple` register no crop block and no seed, and `CropType` gains no 'planting' constant either (D-51, D-10).
2. **No custom network packets**: no `CustomPacketPayload` is added; the three intrusive effects and the Zangief Cane's phases rely entirely on the vanilla sync of hidden `MobEffect`s (D-50, C-6).
3. **No Mixin**: all three intrusive effects go through NeoForge events and native extension points (D-40).
4. **No change to any existing v0.0.1 number or recipe**: any field of the 15 existing `CropProfile`s, the default value of any existing config key, the existing 21 conversion recipes and the existing 12 advancements are untouched throughout (build fact card §3.4).
5. **No change to the comparison logic of the demolition threshold**: `resistance < maxResistance` in `BlastRule.canBreak()` is not touched, and neither are the default values of `grenadeResistThreshold` and `sliceResistThreshold`; D-78 clarifies threshold attribution and the wording of the documents, not a change of implementation (§5.3, §5.5).
6. **No shader-mod compatibility detection**: the conflict of full-screen grayscale with post-processing mods such as Oculus / Iris is registered only as a known incompatibility; no runtime probing is written and no degradation branch is added (RT-16).
7. **No rotatable pillar for the Compressed Hay Block**: the same texture on six faces, no axis variant (D-57).
8. **No new damage types**: `damage_types` +0, still D-39's two, `farmstorm:void_bolt` and `farmstorm:crop_blast`; the Molotov deals no damage (D-58).
9. **No new registration categories**: `block_entities`, `menu types`, `recipe type` / `serializer`, `data components` and the creative tab are all +0 in build fact card §3.2 (`zangief_cane` reuses the existing `DeployedCropBlockEntity`, the conversion recipes reuse `ConverterRecipe`).
10. **No new custom commands**: this round adds only 3 advancements (D-24, D-63).
11. **No editing of the v0.0.1 documents**: not one of `docs/zh/00`–`09` and `docs/en/00`–`09` is changed; the scope clarification is registered only in §5 of this file (D-49).

---

## 7. Inheritance relation to the v0.0.1 decisions

| Marker | Meaning |
|---|---|
| Full continuation | neither the conclusion nor the scope of application changes |
| Refined this time | the conclusion is unchanged; this version pins down one of its parameters, exceptions or landing points |
| Extended this time | the conclusion is unchanged; this version adds entries to the list it governs |
| Scope changed | this version rewrote part of its scope (D-17 only) |

| ID | Status | Note |
|---|---|---|
| D-01 | Full continuation | v0.0.2 likewise produces documents and prompts only. |
| D-02 | Extended this time | On top of the full v0.0.1 set, the deliverables gain 6 files each in Chinese and English (D-49). |
| D-03 | Extended this time | This round's prompts are `14-PROMPT-MAIN-0.0.2.md` and `15-PROMPT-TASKS-0.0.2.md` (D-49). |
| D-04 | Full continuation | The target readers are unchanged. |
| D-05 | Extended this time | The Chinese + English requirement is unchanged, and the coverage extends to `10`–`15` (D-49). |
| D-06 | Full continuation | Ally determination still uses vanilla scoreboard teams; the exclusions of D-64 and D-71 are both filtered by it. |
| D-07 | Full continuation | Trap and Turret still target non-ally players only; per this entry the Zangief Cane does not handle mobs (D-71). |
| D-08 | Refined this time | 'Every improved crop has a seed item' holds only for the 15 existing profiles; the three new profiles have no planting form, like `melon_slice_bomb` (D-51). |
| D-09 | Full continuation | The Wild Apple is produced by the converter and given no vanilla use (D-54). |
| D-10 | Full continuation | 'Planting type' still adds no new design; D-51 goes the same way. |
| D-11 | Extended this time | The GUI description of the body has been superseded by D-47; the conversion recipe list gains 1 bidirectional recipe this version (D-54), conversion recipes 21 → 24. |
| D-12 | Full continuation | The 3-experience-point cost and the 'no catalyst' of D-54 both follow this entry. |
| D-13 | Full continuation | Already narrowed by D-37; this version does not touch it. |
| D-14 | Full continuation | The recovery scope of the Torchflower and the Pitcher Plant is unchanged. |
| D-15 | Full continuation | The granting and removal of temporary enchantments is unchanged. |
| D-16 | Full continuation | The stacking scope is unchanged; the new items of this version are filed under the same scope. |
| D-17 | Scope changed | D-55: the random pool changes from the automatic derivation '`ALL` minus Lucky Berry' to an explicit exclusion of three (`lucky_berry` / `wild_apple` / `compressed_puffed_wheat`), pool 14 → 16. |
| D-18 | Full continuation | The 90-second creative-mode restore is unchanged. |
| D-19 | Full continuation | It constrains only the two, the Melon Slice Bomb and the Melon Grenade; the threshold itself is unchanged. What D-78 clarifies is its scope of application (the other Demolition projectiles follow the 3.6e6 floor) and the implementation symbols (factory vs constant); the expansion is in §5. |
| D-20 | Refined this time | D-73 adds one explicit exception for the Trap category: the Zangief Cane's 1.5-block trigger radius uses spherical Euclidean distance. |
| D-21 | Full continuation | The new numbers of this version likewise go into `FarmstormConfig`, with no balance adjustment. |
| D-22 | Extended this time | New keys are appended to `FarmstormConfig`, and the default values of the existing 117 keys are unchanged; the full list of new keys is produced by `12-TECH-0.0.2.md` (D-53 / D-58 / D-64 / D-74 each bring new keys). |
| D-23 | Full continuation | A dedicated server still needs only the resource pack. |
| D-24 | Extended this time | Advancements 12 → 15 (D-63), still no custom commands. |
| D-25 | Refined this time | D-53 fixes the conversion specs for audio and icons, D-66 fixes the provenance statement; the pixels are still reviewed by the author. |
| D-26 | Full continuation | This version's English names are taken by C-10's Title Case rule (the last 4 rows of §4). |
| D-27 | Full continuation | The friendly-fire scope is unchanged. |
| D-28 | Full continuation | The Pumpkin Turret's fire interval is not inside this version's change surface. |
| D-29 | Full continuation | The food values are unchanged. |
| D-30 | Full continuation | The fang radii are unchanged. |
| D-31 | Full continuation | The default values of the completion items are unchanged. |
| D-32 | Full continuation | The Toxic Ubi Server's crop block and seed item are registered separately; this version does not touch it. |
| D-33 | Full continuation | The landing offset of what the Lucky Berry spawns is unchanged. |
| D-34 | Full continuation | The three new profiles register no crop block (D-51), so this entry's list does not grow. |
| D-35 | Extended this time | `zangief_cane` is a Deployable, so its item is the BlockItem of the deployment block and it reuses the `useOn` split (D-35); its appearance follows the trap scope (D-68). |
| D-36 | Full continuation | The implementation of indestructibility is unchanged. |
| D-37 | Full continuation | The ruling principle 'a specific clause outranks a general clause' applies equally in this version (both D-57 and D-78 are read by it). |
| D-38 | Full continuation | D-77 inherits the 3.6e6 floor along with the Puffed Wheat landing write. |
| D-39 | Full continuation | This version adds no damage type (`damage_types` +0), still 2. |
| D-40 | Full continuation | All three intrusive effects go through NeoForge events and hidden `MobEffect`s (D-50, D-60 ~ D-65, D-76); not one Mixin. |
| D-41 | Full continuation | Temporary enchantments still use the DataComponent on the item; this version does not touch it. |
| D-42 | Full continuation | The Demolition category is still 4 (`ubi_server` / `toxic_ubi_server` / `melon_grenade` / `melon_slice_bomb`); threshold attribution is clarified by D-78 (§5). |
| D-43 | Full continuation | The Pumpkin Turret's acquisition radius and target range are unchanged. |
| D-44 | Full continuation | The Pumpkin Turret's damage attribution is unchanged. |
| D-45 | Full continuation | Crop explosions still exclude no entity; the Molotov's landing explosion is 0 damage (D-58) and does not conflict with this entry. |
| D-46 | Full continuation | D-72 reuses 'silently removed, produces no drop', with the scope limited to the trigger path (mining still goes through the loot table). |
| D-47 | Full continuation | The converter is still three slots + an explicit button; what D-52 adds is its crafting recipe, not a GUI change. |
| D-48 | Full continuation | Type and Category being orthogonal is still the only modelling approach; D-68 reuses the trap category's in-world invisibility scope, and D-58 reuses 'block destruction permission is decided only by the Demolition category'. |

Of the 48, 37 are full continuation, 10 are refined or extended this time (3 refined: D-08, D-20, D-25; 7 extended: D-02, D-03, D-05, D-11, D-22, D-24, D-35), and 1 has its scope changed (D-17), 48 in total.

The five most worth watching: the conversion recipe list of **D-11** is extended by D-54 (conversion recipes 21 → 24); the Lucky Berry pool of **D-17** has its scope changed by D-55 (pool 14 → 16); **D-19** remains in force and constrains only the two, the Melon Slice Bomb and the Melon Grenade, with its scope of application clarified by D-78 (§5); the trap invisibility of **D-48** is reused by D-68, and at the same time its 'block destruction permission is decided only by the Demolition category' is the basis of D-58. **D-40**'s 'no Mixin' likewise remains in force — all three intrusive effects of this version are implemented through NeoForge events and hidden `MobEffect`s, without a single exception.

## Translator's notes

1. The headings `### 3.4 Errata (D-78)` and `## 5. Errata for the v0.0.1 documents` translate the source's 勘误. Their own body text (the blockquote opening §3.4 and the opening paragraph of §5) states that what they register is a **scope clarification** (口径澄清), not a correction of errors in the v0.0.1 documents. The heading vocabulary is kept verbatim from the source; the clarifications are carried by the body text.
2. D-55's rationale carries the parenthetical 「两人一物」 after 'explicit exclusion of three entries'. It is left out of the English sentence because its meaning is not recoverable from the context (the three excluded entries are `lucky_berry`, `wild_apple`, `compressed_puffed_wheat`); nothing is reconciled here.
3. The §4 startup self-check block is kept verbatim in Chinese: it reproduces a log string printed by `Farmstorm#logSelfCheck`, and its numbers (19 / 10 / 6 / 3 / 4) are what the surrounding text requires to match.
