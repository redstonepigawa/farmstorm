# 16 · v0.0.3 Decision Record

> This file is the single source of truth for 'Farmstorm' v0.0.3: the numbering continues `10-DECISIONS-0.0.2.md` from D-79 and covers D-80 ~ D-102, 23 entries in total.
> The existing content of v0.0.1 still follows `00-DECISIONS.md` and the existing content of v0.0.2 still follows `10-DECISIONS-0.0.2.md`; this file covers only the new content of v0.0.3, plus the two authorized changes to v0.0.2 (§5).
> Source: the author's v0.0.3 requirement text + the build fact card `.work/v003-BRIEF.md` (§1 verified facts, §2 decision summary, §3 incremental list, §6 risk register). The build fact card is a flattened working draft of the build period and is not a deliverable.

---

## 1. Force and scope of this file

| Item | Value |
|---|---|
| File | `docs/zh/16-DECISIONS-0.0.3.md` (English counterpart `docs/en/16-DECISIONS-0.0.3.md`) |
| Version | 0.0.3 |
| Decision number range | D-80 – D-102, 23 entries, consecutive with no gaps |
| Subject matter covered | delivery and structure (D-80), the two status effects (D-81 ~ D-83), the 9 decisions of the three new crops (D-84 ~ D-92), the two small reworks (D-93 ~ D-96), the random pool expansion (D-97), the supplementary rulings on the preceding decisions (D-98 ~ D-102) |
| Subject matter not covered | D-01 – D-48 and C-1 – C-28 (inheritance in §7); the existing clauses of D-49 – D-79 (the way each is inherited is stated entry by entry in §7, and apart from the two places authorized by D-94 / D-96 nothing changes); the 16 Chinese files 00 – 15 of `docs/zh` and the 14 English files of `docs/en` (frozen by D-80) |
| Conflict ruling | existing v0.0.1 clauses → `00-DECISIONS.md`; existing v0.0.2 clauses → `10-DECISIONS-0.0.2.md`; new v0.0.3 clauses → this file |

- **Force**: once these 23 entries are registered they are the final scope for v0.0.3. `17-DESIGN-0.0.3.md`, `18-TECH-0.0.3.md`, `19-TASKS-0.0.3.md`, `20-PROMPT-MAIN-0.0.3.md`, `21-PROMPT-TASKS-0.0.3.md` and the code of the implementation phase must all agree with this file; where they conflict, this file wins.
- **Relation to v0.0.2**: both v0.0.1 and v0.0.2 have already been implemented and released (build fact card §1), and v0.0.3 is an increment on the implemented 0.0.2. Within that, **D-93 ~ D-96 change an existing implementation, they are not a pure increment**; apart from these two places (Fang Trap Cactus, Zangief Cane) everything in 0.0.2 stays as it is (§5.7). This section is also the only place where that is registered, and no separate list is set up elsewhere.
- **How the conclusions are transcribed**: the 'Decision' of each entry in §3.1 – §3.5 is transcribed verbatim from build fact card §2, with not one character of the numbers or the wording changed; the five entries of §3.6 (D-98 ~ D-102) are the author's supplementary rulings, that group does not exist in fact card §2, and what is transcribed is the text of the rulings themselves. The in-line identifiers of the fact card are written straight (no backticks); this file keeps them that way and does not rewrite them into `code` form, so as not to conflict with the 'not one character changed' requirement.
- **Marking of derivations**: content that the fact card does not give directly and that this file derives or reads out of the source code is marked 'supplemented by this document'. In this file the supplementary points are marked 'supplemented by this document' place by place and are not counted here — they grow and shrink with every rewrite, so a hard-coded count would necessarily fail to match (the D-20 row in §7.3 is one such later addition).
- **Number discipline**: the counts appearing in this file are of only three kinds — **verified** (config 139 keys, language keys 263, from the actual output of `tools/verify_project.py`), **incremental** (+13 and its breakdown, from build fact card §3), and the **to-be-measured** total number of registry objects. The total is never hard-coded: that tool still takes inventory against the 64 hard-coded in v0.0.1 and has already FAILed, so the tool has to be fixed before the number is taken (RT-23). This file gives the increment only.
- **Forbidden wording**: the three kinds of uncertain statement forbidden by the build fact card (open items, to-do markers, conclusions with vague conditions) appear nowhere. When a judgement is needed and the fact card does not give it directly, it is derived from the existing decisions and marked 'supplemented by this document' at that place.
- **Citation style**: references to existing documents are written with the file name, as in `10-DECISIONS-0.0.2.md §3.3`; references to source code are written as paths relative to the repository root; references to the build fact card are written 'build fact card §2 D-94'.

---

## 2. Deliverable structure (v0.0.3 documentation map)

v0.0.3 adds six files 16 – 21 under each of `docs/zh/` and `docs/en/`, continuing the numbering; 00 – 15 is left untouched throughout (D-80).

```
D:\project_farm\docs\zh\
├── 00-DECISIONS.md  …  09-IMPL-VS-PLAN.md      the 10 v0.0.1 files (frozen, D-80)
├── 10-DECISIONS-0.0.2.md  …  15-PROMPT-TASKS-0.0.2.md
│                                               the 6 v0.0.2 files (frozen, D-80)
├── 16-DECISIONS-0.0.3.md        this file: D-80 … D-102
├── 17-DESIGN-0.0.3.md           full design specs for the 7 entries
├── 18-TECH-0.0.3.md             registry increment, class design, resource and config list, RT-17 ~ RT-23, acceptance self-check
├── 19-TASKS-0.0.3.md            task breakdown from F-100 + dependency graph + milestones + effort
├── 20-PROMPT-MAIN-0.0.3.md      the main prompt, starting at HC-29 and N-29
└── 21-PROMPT-TASKS-0.0.3.md     the staged task list, starting at Phase 16
```

`docs/en/` has 16 – 21 one to one. On the English side terminology follows C-10 of `00-DECISIONS.md` and the English name column (column 9) of §4 of this file; the English name of the Produce Converter continues the **Produce Converter** fixed in v0.0.2. `07-PUBLISH.md` and `08-DEV-GUIDE.md`, which have no English version in v0.0.1, are still not filled in (D-80 freezes the existing numbering and adds no filling action).

| File | Content it owns | Decisions in this file that define it |
|---|---|---|
| `16-DECISIONS-0.0.3.md` | D-80 ~ D-102 one line each; the 22-entry profile summary table; the register of changes to v0.0.2; what is explicitly not done; the inheritance relation to v0.0.2; the supplementary rulings (D-98 ~ D-102); the provenance of the numbers | all 23 |
| `17-DESIGN-0.0.3.md` | the number tables, state machines, boundaries and anomalies of the 7 entries (Q-Bounce, Confusion, Friend Chives, Point Defense Fungus, Vibration Bean, Fang Trap Cactus, Zangief Cane) | D-81 ~ D-97, D-98 ~ D-102 |
| `18-TECH-0.0.3.md` | registry increment +13 item by item, class design, resource and media list, new config keys and the breakdown of changed defaults, RT-17 ~ RT-23, acceptance self-check (including 'fix `tools/verify_project.py` first, then take the numbers') | D-84 ~ D-92, D-94 ~ D-97, D-98 ~ D-102 |
| `19-TASKS-0.0.3.md` | task breakdown from F-100, dependency graph, milestones, effort, coverage cross-reference table | D-80 |
| `20-PROMPT-MAIN-0.0.3.md` | one main prompt, the HC and N numbers laid out after v0.0.2 | all 23 |
| `21-PROMPT-TASKS-0.0.3.md` | one staged task list, the Phase numbers laid out after v0.0.2 | all 23 |

---

## 3. Decision list

Each entry is one subsection with a fixed set of four fields: ID / Decision / Rationale / Where it lands. The 'Decision' of §3.1 – §3.5 is transcribed verbatim from build fact card §2. The group names are the same as the group headings of fact card §2: delivery and structure / two status effects / three new crops / two small reworks / random pool expansion. §3.6 is this round's new supplementary rulings (D-98 ~ D-102): it patches D-82 / D-87 / D-88 / D-89 / D-92, whose original text and conclusions stay untouched character for character, the patches being written only in §3.6; that group does not come from the groups of fact card §2 but from the author's review of plan0.0.3.txt.

### 3.1 Delivery and structure (D-80)

#### D-80 · The 0.0.2 documents are frozen; 0.0.3 starts 6 new files 16–21

- **ID**: D-80
- **Decision**: `docs/zh` and `docs/en` each gain 16…21; 00–15 is left untouched throughout. Decisions start at D-80, risks at RT-17, tasks at F-100.
- **Rationale**: v0.0.2 has been implemented and released, and the documents were edited back during the implementation period (build fact card §1); editing the old files back again would let 'which version said what' keep drifting and would also break the correspondence between the released jar and the documents. The numbering continues instead of being renumbered for the same reason as D-49: to preserve the existing references to D-01 – D-79 in `00-DECISIONS.md`, in `10-DECISIONS-0.0.2.md` and in code comments. The risk register starts a new paragraph (from RT-17) and the tasks start a new paragraph (from F-100) so that the exclusion list of v0.0.3 and that of v0.0.2 do not overwrite each other.
- **Where it lands**: `docs/zh/16` – `21`, `docs/en/16` – `21`, the v0.0.3 index section of `README.md`; the division of labour is in §2 of this file; risks in build fact card §6 (RT-17 ~ RT-23); the starting number of tasks in `19-TASKS-0.0.3.md`, from F-100.

### 3.2 The two status effects (D-81 ~ D-83)

#### D-81 · Q-Bounce (q_bounce)

- **ID**: D-81
- **Decision**: the duration is decided by the applier (Friend Chives applies **10 seconds = 200 ticks**); the icon goes from preview.jpg to an 18×18 PNG; the icon is visible.
During the effect: the client poses the model of the affected mob in a **riding pose**, keeps rotating it and repeatedly squashing and stretching it; at the same time it **locks every input**.
The lock reuses the seven-segment input lock of 0.0.2 (D-79) and no second one is written.
- **Rationale**: the effect itself has no duration key, because the duration of Q-Bounce is decided by the source (D-85 gives 200 ticks) and hard-coding a key of the effect's own would create two sources for 'where the duration is set'; this is also the direct consequence of the v0.0.2 D-50 scheme 'effect present + amplifier + remaining duration is the complete state' — the applier writes the duration and the client only reads it. The 18×18 icon follows the effect icon spec already fixed by D-53; the asset is a 1024×1024 JPG (build fact card §1.3) and must be converted before it enters the jar. The lock reuses the seven-segment form of D-79 because a second lock would overlap with the existing seven-segment judgement (RT-19).
- **Where it lands**: `registry/FarmstormMobEffects.java` (one of `mob_effects` +2, `q_bounce`); the four `qbounce.*` keys of `config/FarmstormConfig.java` (`poseMode` / `rotateDegreesPerTick` / `squashAmplitude` / `squashPeriodTicks`, build fact card §3.3); the rendering-side pose driver in `client/`; the lock entry point of `client/InputLockHandler.java` (reusing the seven segments of D-79); `assets/farmstorm/textures/mob_effect/q_bounce.png`; the Q-Bounce section of `17-DESIGN-0.0.3.md`.

#### D-82 · Both implementation branches of the Q-Bounce riding pose coexist and are switched by config

- **ID**: D-82
- **Decision**: the config key qbounce.poseMode ∈ { render, mount }, default render.
- render branch: the client changes the riding flag of the model instance in RenderLivingEvent and lowers the model, **without touching the entity itself**, so it affects neither the AI nor the hitbox;
- mount branch: the server mounts an invisible mount on every affected mob, the pose is the vanilla real one, but it changes entity relations and collisions.
Both **must be implemented**; the author keeps one of them after comparing on a real client, and the one that is not kept must be switchable off by config rather than deleted from the code.
- **Rationale**: the two branches cost different things — the render branch changes only the rendering layer, at the cost that other mods' custom models may not honour the riding flag (RT-17); the mount branch gets the vanilla real pose, at the cost of changed entity relations and collisions (RT-18). This trade-off can only be settled by comparing on a real client and cannot be decided while writing documents, so both are implemented and switched by config, instead of cutting one and reworking later. render is the default because it does not change entity state, so the blast radius of a mistake is limited to what is on the screen.
- **Where it lands**: `config/FarmstormConfig.java` (`qbounce.poseMode`, enum render/mount, default render); the `RenderLivingEvent` branch in `client/`; the server-side branch that mounts the invisible mount; the two-branch class design of `18-TECH-0.0.3.md`; RT-17, RT-18.
- **Supplement**: This entry is supplemented by D-102 — the mount branch carries the mount with vanilla existing entity types only and adds no `entity_type`, so the `entity_types` increment stays +2; see §3.6.

#### D-83 · Confusion (confusion)

- **ID**: D-83
- **Decision**: **no icon is shown**; **it applies to players only**; only the movement input is inverted (W↔S, A↔D), while the view, the jump, the attack and the use are all left alone.
When it is present together with Q-Bounce, **the input lock takes priority**: while locked, 'the reversed direction' means nothing, so it is handled as locked.
- **Rationale**: the icon is not shown because it is not a collectible state and an icon slot would expose the position of the trap; applying to players only follows the effect standard of the trap category. Only the movement input is inverted and the view and the attack are left alone so that 'confusion' does not turn into 'loss of control' — once the view and the attack are reversed it overlaps with the lock of D-60 and the player can no longer save themselves. The criterion for the lock taking priority is that the lock is the stronger one: a reversed direction has no executable landing point while locked, so handling it as locked loses no information (RT-19).
- **Where it lands**: `registry/FarmstormMobEffects.java` (the hidden item of `mob_effects` +2, `confusion`, with no icon registered); the `MovementInputUpdateEvent` branch in `client/` (it changes the direction components only and touches neither speed nor view); `config/FarmstormConfig.java` gets no duration key (the 'no key' row of build fact card §3.3); the Confusion section of `17-DESIGN-0.0.3.md`.

### 3.3 The three new crops (D-84 ~ D-92)

The 9 decisions are grouped in three by crop and correspond one to one with rows 20 – 22 of §4.

| Crop | registry id | Decisions | Type + category |
|---|---|---|---|
| Friend Chives | `friend_chives` | D-84, D-85 | THROWABLE + BUFF |
| Point Defense Fungus | `point_defense_fungus` | D-86 ~ D-89 | DEPLOYABLE + TURRET |
| Vibration Bean | `vibration_bean` | D-90 ~ D-92 | THROWABLE + TRAP |

**Friend Chives (friend_chives): D-84 ~ D-85**

#### D-84 · Friend Chives (friend_chives) has no vanilla counterpart

- **ID**: D-84
- **Decision**: it therefore **has no converter recipe**. There is only one way to obtain it: Chive Seeds planted on farmland, harvested when mature.
It still joins the converter_fuel tag (it can serve as a crafting material of the converter).
- **Rationale**: conversion recipes serve only the reversible mapping 'vanilla crop ⇄ improved crop' (the tag standard of D-52, the bidirectional same shape of D-54); with no counterpart there is no reversible pair, and forcing a one-way recipe would give `ConverterRecipe` a second form. As a crop it must still be able to serve as a crafting material of the converter, so it joins the `farmstorm:converter_fuel` tag — tag membership and 'having a conversion recipe of its own' are two different things.
- **Where it lands**: `util/CropProfile.java` (the 20th profile `FRIEND_CHIVES`); `registry/FarmstormItems.java` (the two items `friend_chives` and `chive_seeds`); `util/FarmstormTags.java` and `datagen/ModItemTagsProvider.java` (`converter_fuel` gains one entry); `datagen/ModRecipeProvider.java` does not generate its conversion recipe.

#### D-85 · Friend Chives = Throwable + Buff

- **ID**: D-85
- **Decision**: after being thrown it applies **10 seconds of Q-Bounce** to the mobs inside a **Euclidean 6-block** sphere around the landing point.
**If this throw hit at least 1 mob**, a sound effect is played once at the landing point and is **audible to players within 32 blocks**; if nothing was hit it is not played.
- **Rationale**: the sphere uses Euclidean distance, consistent with the v0.0.1 standard 'the buff aura uses a Euclidean radius' (`00-DECISIONS.md` D-20). The sound is played on a hit only: playing it on an empty throw too would make the 'did I hit anything' feedback disappear, while the 32-block audible radius is what lets teammates of the same team hear the result of the hit judgement. 10 seconds and 200 ticks are two spellings of the same value (D-81), and this entry, as the applier, hard-codes it.
- **Where it lands**: the Friend Chives projectile in `entity/thrown/` (one of `entity_types` +2); `sound_events` +1 (the `friend_chives` hit sound, mono 64 kbps OGG); `friend_chives.hitRadius` 6.0, `effectTicks` 200, `soundRange` 32.0 of `config/FarmstormConfig.java` (build fact card §3.3); the Friend Chives section of `17-DESIGN-0.0.3.md`.

**Point Defense Fungus (point_defense_fungus): D-86 ~ D-89**

#### D-86 · Point Defense Fungus (point_defense_fungus) = Deployable + Turret, counterpart is the Warped Fungus

- **ID**: D-86
- **Decision**: conversion recipe: minecraft:warped_fungus ⇄ farmstorm:point_defense_fungus, bidirectional, 3 experience points, no catalyst (the same shape as D-54).
- **Rationale**: the counterpart is the Warped Fungus, exactly the same shape as the Wild Apple of D-54 — bidirectional, 3 experience points, no catalyst — so the normal form of `ConverterRecipe` is reused directly and no recipe type is added. The classification is Turret, but the objects it intercepts are narrowed by D-87 to this mod's projectiles, which is the same device as C-3's treatment of man瓜: the classification is what the player sees, the actual judgement goes by behaviour.
- **Where it lands**: `datagen/ModRecipeProvider.java` (conversion recipes +1, `bidirectional=true`); `util/CropProfile.java` (the 21st profile); `behavior/DeployedKind.java` (enum +1 value `POINT_DEFENSE_FUNGUS`); `registry/FarmstormBlocks.java` (the deployed block `point_defense_fungus`); the Point Defense Fungus section of `17-DESIGN-0.0.3.md`.

#### D-87 · The Point Defense Fungus intercepts this mod's projectiles only

- **ID**: D-87
- **Decision**: range: inside a **Euclidean 8-block** sphere centred on itself. Only the ThrownCropEntity family is intercepted (the 9 kinds of 0.0.2 + the 2 new kinds of 0.0.3).
**Vanilla arrows / snowballs / tridents / ender pearls are never intercepted.**
- **Rationale**: the judgement surface is limited to the single base class `entity/ThrownCropEntity.java` — v0.0.2 already has 9 `entity_type`s going through it and v0.0.3 adds 2 more (build fact card §1.2, §3.1), so one `instanceof` covers all of them and nothing has to be registered item by item. Vanilla projectiles are never intercepted because intercepting vanilla arrows would change vanilla combat and the projectile feel of other mods, for a gain far smaller than the risk; this entry is narrower than C-3 (man瓜 intercepts projectiles) and is a self-defence mechanism inside this mod.
- **Where it lands**: the scan and intercept judgement of the Point Defense Fungus in `behavior/` (dispatched by `DeployedKind.POINT_DEFENSE_FUNGUS`); `point_defense_fungus.interceptRadius` 8.0 of `config/FarmstormConfig.java` (build fact card §3.3); RT-20 (the cost of the per-tick scan and the staggering).
- **Supplement**: This entry is supplemented by D-98 — the interception applies to **non-ally** projectiles only (an ally passing through is neither intercepted nor charged), and the family size is corrected to 10 + 2 = 12; see §3.6.

#### D-88 · The interception count of the Point Defense Fungus

- **ID**: D-88
- **Decision**: every interception consumes 1 charge, and **the block is silently removed after the 6th interception** (it does not go through a loot table, the same trigger-path standard as D-72).
When the same projectile is reached by N Point Defense Fungi at the same instant, **only 1 of them is charged** — the method is to record an 'already intercepted this tick' mark on the projectile, which the later fungi skip and then clear.
- **Rationale**: silent removal follows the trigger-path standard of D-72 directly, does not set up a new 'disappear when used up' semantics, and could not be achieved by deleting the loot table anyway (that way a player who mines it himself would get no drop either). The count lives on the block entity and the de-duplication lives on the projectile: the block entity only knows how many times it was charged and cannot know who else is charging it at the same instant; the projectile is the only shared object in this judgement, so a tick mark only works on it (RT-20). The staggered scan (every 10 ticks) avoids per-tick duplicate charging and pointless cost.
- **Where it lands**: the counting logic of the Point Defense Fungus in `behavior/` (the interception counter of `DeployedCropBlockEntity`); the tick mark field on the projectile entity; `point_defense_fungus.maxIntercepts` 6 of `config/FarmstormConfig.java` (build fact card §3.3); silent removal goes through `level.removeBlock` / `consume()` and does not touch the loot table; RT-20.
- **Supplement**: This entry is supplemented by D-99 — an intercepted projectile is discarded on the spot and its landing point is not settled; see §3.6.

#### D-89 · The interception graphic of the Point Defense Fungus is purely visual

- **ID**: D-89
- **Decision**: particles are produced at the position of the intercepted projectile and a sound is played, and it **deals no damage, breaks no block and constructs no Explosion**.
- **Rationale**: interception is 'defence' and not a second damage source; with damage, the Point Defense Fungus would become an invisible automatic turret, which conflicts with the explicit design the Turret category already has (the target standard of D-07). Not constructing an `Explosion` goes one step further than D-58's 'a 0-power explosion is only a trigger' — here not even a trigger is needed, because the landing point is the projectile's own position.
- **Where it lands**: the interception particles and sound playback in `behavior/`; `point_defense_fungus.interceptParticles` 8 of `config/FarmstormConfig.java` (build fact card §3.3); the interception section of `18-TECH-0.0.3.md` (stating explicitly that `hurt` is not called, no block is written and no `Explosion` is new'ed).
- **Supplement**: This entry is supplemented by D-100 — the interception sound reuses a vanilla `sound_event` and adds no registry object, so the `sound_events` increment is still only the +1 of `friend_chives`; see §3.6.

**Vibration Bean (vibration_bean): D-90 ~ D-92**

#### D-90 · Vibration Bean (vibration_bean) = Throwable + Trap, counterpart is cocoa beans

- **ID**: D-90
- **Decision**: conversion recipe: minecraft:cocoa_beans ⇄ farmstorm:vibration_bean, bidirectional, 3 experience points, no catalyst.
- **Rationale**: the counterpart is the cocoa bean, the kind of 'crop harvested from a vanilla tree', and the shape is exactly the same as D-54 and D-86 (bidirectional, 3 experience points, no catalyst), likewise reusing the normal form of `ConverterRecipe`. The classification is Trap, consistent with the one-shot trap its landing point spawns.
- **Where it lands**: `datagen/ModRecipeProvider.java` (conversion recipes +1, `bidirectional=true`); `util/CropProfile.java` (the 22nd profile); `registry/FarmstormItems.java` (the `vibration_bean` item); the Vibration Bean section of `17-DESIGN-0.0.3.md`.

#### D-91 · The Vibration Bean landing point spawns a floating invisible trap

- **ID**: D-91
- **Decision**: the landing point spawns an invisible block + block entity that **is attached to no block at all**, and only particles expose its position.
This needs a new 'unattached' mode added to the block: canSurvive always true, no FACE support check, not removed by block updates.
**It is forbidden** to reuse supportsFloating — that is 'tolerate after the attachment is lost', a different semantics.
- **Rationale**: the `block/DeployedCropBlock.java` of v0.0.2 has only `supportsFloating` (tolerate after the attachment is lost) and no 'never attached from the start' mode (build fact card §1.2). The two cannot be merged: tolerance means the block first attaches legally and still survives after the support disappears; the Vibration Bean has no support from the start, so the semantics of `canSurvive` and `updateShape` are completely different. The biggest risk of a floating block is block updates and chunk unloading (RT-21), so the criteria are written as always true and the update callback returns the block itself.
- **Where it lands**: `block/DeployedCropBlock.java` (the new 'unattached' mode; the three branches `canSurvive` / `updateShape` / FACE support check); `registry/FarmstormBlocks.java` (one of `blocks` +3: the `vibration_bean` floating trap block); `block_entities` +0 (reusing `DeployedCropBlockEntity`); RT-21.

#### D-92 · The trigger and the effect of the Vibration Bean

- **ID**: D-92
- **Decision**: trigger: the **Euclidean distance** between a non-ally player and the trap is **< 4** (strictly less than).
Effect: applies **4 seconds of Confusion** to **all players** within radius 4 (no friend-or-foe filter, following the one-shot AoE standard of D-27 in 0.0.2);
at the same time it plays **one explosion sound** at the trap. One-shot: after the trigger it is silently removed and drops nothing.
- **Rationale**: strictly less than 4 and 'all players within radius 4' are two uses of the same ruler: the trigger end recognises non-ally players only (D-07), the effect end does not filter friend or foe (the one-shot AoE standard of D-27), so an ally stepping on it gets confused too, in line with the existing strength of 'a one-shot trap recognises nobody'. Silent removal after the trigger with no drop follows D-72 directly. The explosion sound uses the sound only and constructs no explosion, to avoid being confused with the 0-power trigger of D-58.
- **Where it lands**: the trigger and settlement of the Vibration Bean in `behavior/`; `vibration_bean.triggerRadius` 4.0 and `confusionTicks` 80 of `config/FarmstormConfig.java` (build fact card §3.3); the Vibration Bean projectile in `entity/thrown/` (one of `entity_types` +2, places the block at the landing point); the Vibration Bean section of `17-DESIGN-0.0.3.md`.
- **Supplement**: This entry is supplemented by D-101 — both the trigger end and the effect end use **strictly less than 4.0**, and one end must not be written < while the other is written ≤; see §3.6.

### 3.4 The two small reworks (D-93 ~ D-96)

This group holds two things only: the Fang Trap Cactus (D-93 ~ D-95) and the Zangief Cane (D-96). They are the only places in v0.0.3 where existing v0.0.2 content is authorized to change; the item-by-item register is in §5.

**The first place: the Fang Trap Cactus (fang_trap_cactus): D-93 ~ D-95**

#### D-93 · The Fang Trap Cactus changes its Chinese name only; the id and the English name stay

- **ID**: D-93
- **Decision**: the Chinese display name changes from '赢过输过没死过' to '刺球'; the registry id is still fang_trap_cactus; the English name still follows C-10 and is **Fang Trap Cactus**.
This is one explicit exception registered against C-10 (Chinese and English no longer translate each other), and the id must not be changed along with it.
- **Rationale**: what changes is the display name, not the registration identity. Once the id changes, the block id, the `DeployedKind` constant, the loot table, the recipes and the tag references all have to change with it, and the block in existing saves and world data would lose its counterpart; the English name follows the id, so it does not move either. Chinese and English no longer translating each other means C-10's 'display name = Title Case of the registry ID' gets its first exception on the Chinese side — that exception must be registered, otherwise a later reader would take the Chinese name for a typo and change it back.
- **Where it lands**: `assets/farmstorm/lang/zh_cn.json` (the value of that key; the number and the set of keys are unchanged); `assets/farmstorm/lang/en_us.json` unchanged; the item-by-item comparison table of §5.2; the Fang Trap Cactus section of `17-DESIGN-0.0.3.md`.

#### D-94 · Fang Trap Cactus number adjustment (a per-item delta against the current state)

- **ID**: D-94
- **Decision**:

| Item | Current value | 0.0.3 |
|---|---|---|
| Trigger distance (Euclidean) | 3.0 | **≤ 2.5** |
| Outer ring fang count | 8 | 8 (unchanged) |
| Outer ring delay | 10 ticks | **4 ticks (0.2 s)** |
| Outer ring distribution radius | 2.0 | 2.0 (unchanged) |
| Inner ring fang count | 5 | 5 (unchanged) |
| Inner ring delay | 4 ticks | **2 ticks (0.1 s)** |
| Inner ring distribution radius | 1.0 | 1.0 (unchanged) |
| Hit effect | none | **Slowness V (amplifier 4) 60 ticks** |

The inner ring still comes out first and the outer ring still after it (the order is unchanged).

**This table has 8 data rows**, of which 4 rows change and 4 rows are marked unchanged; the 'unchanged' ones must stay in the table, they are the boundary of this decision.

The plan says 'less than or equal to 2.5'; the comparator is part of the decision, and writing the number alone is not allowed.

- **Rationale**: the current values are verified from source and config (build fact card §1.2: trigger 3.0 Euclidean; inner ring 5, delay 4 ticks, radius 1.0; outer ring 8, delay 10 ticks, radius 2.0; vanilla `EvokerFangs`, owned by the deployer, applying no potion effect). This entry changes only four of them: the trigger distance, the outer delay, the inner delay and the new hit effect; the counts and the two distribution radii are all left alone — the radius is the perceptual parameter of 'how far the fangs spread out', and changing it would change the shape of the whole trap, which is beyond this entry's authority. The delays go from 10/4 to 4/2, keeping the 2-tick gap that makes 'the inner ring comes out first', so the order is unchanged.
- **Where it lands**: `behavior/impl/FangTrapCactusTrap.java` (the three places: the trigger distance, the two ring delays and the slowness applied after a hit); the `fang_trap_cactus` group of `config/FarmstormConfig.java` (3 keys with changed defaults: `triggerRadius` 2.5, `outerDelayTicks` 4, `innerDelayTicks` 2; 2 new keys: `slownessDurationTicks` 60, `slownessLevel` 5, build fact card §3.3); the item-by-item register of §5.3 and §5.4.

#### D-95 · The definition of 'hit' for the Fang Trap Cactus

- **ID**: D-95
- **Decision**: a hit is when vanilla EvokerFangs deal damage to a player; the player that was hit is given Slowness V for 60 ticks; a player that took no damage is not given it.
The owner of the fangs is still the deployer (kill attribution does not take the change).
- **Rationale**: the fangs are a vanilla entity and their damage judgement carries the invulnerability frames, armour and resistance settlement; building another 'am I inside the ring' judgement would conflict with it and produce the case 'bitten in the animation, lost no health' yet slowed. Taking the actual damage as the criterion hands the definition of 'hit' to the only party that can change health. The attribution does not change because it is already carried by the owner field of `EvokerFangs`, which is how v0.0.2 already ran (build fact card §1.2).
- **Where it lands**: the hit callback of `behavior/impl/FangTrapCactusTrap.java` (applying the effect where the fang damage is settled successfully); the Fang Trap Cactus section of `17-DESIGN-0.0.3.md` (stating that the judgement entry is not a bounding-box test); the knock-on change table of §5.4.

**The second place: the Zangief Cane (zangief_cane): D-96**

#### D-96 · The Zangief Cane fall damage becomes a fixed 7.5

- **ID**: D-96
- **Decision**: every existing feature of 0.0.2 is kept; only one passage changes: during the phase 2 fall the **natural fall damage taken by the target is cancelled**, and on landing a **fixed 7.5 points** of damage is applied.
This needs a 3rd damage type: farmstorm:zangief_slam (0.0.2 has only crop_blast and void_bolt).
Damage attribution is still recorded to the trap owner.
- **Rationale**: the `behavior/ZangiefPiledriverController.java` of v0.0.2 has no damage or fall hook at all (`causeFallDamage` / `LivingFallEvent` / `hurt` all 0 hits, build fact card §1.2), so this passage is an addition rather than a rework, and every other phase, the floating, the spin, the trigger judgement and the silent removal are all kept as they are. Cancelling the natural fall damage is necessary: without cancelling it, it would become 'fixed 7.5 + natural fall' stacked together, the damage would drift with height and would conflict with the word 'fixed'. A separate new damage type is needed so that this blow can be told apart in the damage source (death message, resistance, statistics); reusing either of the two existing ones would record it as explosion or void damage. The cancellation applies only 'during phase 2 of this trap', judged by the presence of the effect, and does not change the global behaviour (RT-22).
- **Where it lands**: `behavior/ZangiefPiledriverController.java` (the fall damage cancellation of phase 2 + the landing settlement); `zangief_cane.slamDamage` 7.5 and `zangief_cane.cancelFallDamage` true of `config/FarmstormConfig.java` (build fact card §3.3); `registry/FarmstormDamageTypes.java` (`damage_types` +1: `farmstorm:zangief_slam`); the data pack `data/farmstorm/damage_type/zangief_slam.json`; the item-by-item register of §5.5; RT-22.

### 3.5 Random pool expansion (D-97)

#### D-97 · The Lucky Berry random pool grows to 19, the derivation is unchanged

- **ID**: D-97
- **Decision**: D-55 of 0.0.2 wrote the pool as 'ALL minus lucky_berry / wild_apple / compressed_puffed_wheat'. With the profiles growing from 19 to 22, the three new entries (friend_chives / point_defense_fungus / vibration_bean) are **all included**, and the pool naturally goes from 16 to **19**.
**That derivation line is not changed and no new exclusion is added.** Reason: the author explicitly asked this time for all of them to be included; and none of the three is 'the strongest item of this version' the way the Wild Apple is.
The consequence must be written into the design document: the Lucky Berry can now roll Friend Chives (10 seconds of Q-Bounce); this is intentional, not a bug.
- **Rationale**: the pool is implemented as a stream derivation of `CropProfile.ALL` minus the three (D-55), so once the profiles grow the pool grows with them — that is the necessary result of 'a derivation rather than a list', and it is exactly why the derivation was chosen in the first place. None of the three new profiles has the strength standing of the Wild Apple: Friend Chives is a projectile + buff, Point Defense Fungus is a defensive block, Vibration Bean is a one-shot trap, so entering the pool causes no imbalance of the '1 berry buys the strongest item of the version' kind. Conversely, adding an exclusion for each of them would degrade the derivation into a list that has to be maintained version after version, the opposite of D-55's intent.
- **Where it lands**: `LUCKY_BERRY_POOL` of `util/CropProfile.java` (**the code is not changed**, it is still `ALL` minus the three, and the members change as the profiles grow); `util/LuckyBerryRoller.java` only consumes that constant and is not changed; `config/FarmstormConfig.java` gains no key (the pool contents do not go into config, consistent with D-55); `17-DESIGN-0.0.3.md` must state the consequence: the Lucky Berry can roll Friend Chives, which on landing is 10 seconds of Q-Bounce **and locks every input** (D-81), and this is intentional, not a bug; the startup self-check text of `18-TECH-0.0.3.md` is written against the measured member count of the pool.

### 3.6 Supplementary rulings on the preceding decisions (D-98 ~ D-102)

This section is a patch, not a rewrite: the text and the conclusions of D-82 / D-87 / D-88 / D-89 / D-92 stay untouched character for character, the gaps are filled only in the five entries below, and each of those five ends with a line pointing back. The method is the same as the D-78 clarification of D-19 in `10-DECISIONS-0.0.2.md` — keep the original text and register the correction as a separate entry.

#### D-98 · The Point Defense Fungus intercepts 'non-ally' projectiles only, family size corrected to 12

- **ID**: D-98
- **Decision**: interception applies to **non-ally** projectiles only — a projectile counts as a target only when its owner is not allied with the placer of this Point Defense Fungus. An ally's projectile (the placer himself, players of the same team) passes through without being intercepted and **without charging a count**.
Family size corrected: 0.0.2 has **10** that go through ThrownCropEntity (puffed_wheat / ubi_server / toxic_ubi_server / melon_slice_bomb / melon_grenade / bamboo_firecracker / red_mushroom_puff / brown_mushroom_puff / molotov / compressed_puffed_wheat), plus the friend_chives and vibration_bean added in 0.0.3 = **12**.
- **Rationale**: line 35 of plan0.0.3.txt reads 'intercept the **non-ally** projectiles inside the sphere', and D-87 dropped that qualifier, which is a deviation from the author's text that can only be fixed at the decision level, not settled privately in a design document. The '9' in D-87's rationale is a slip of the pen: 9 is the total `entity_types` of **v0.0.1** (8 projectiles + `turret_bullet`), which is not the same thing as 'the number that go through `ThrownCropEntity`'; the family members of 0.0.2 are exactly the 10 listed above (the same row of build fact card §1.2 still says 9 and has not been corrected either; when taking numbers, go by the item-by-item list of 10 given in this entry). Ally determination follows D-06 of `00-DECISIONS.md` (vanilla scoreboard teams) and `TeamResolver` of 0.0.2, the same set as the existing ally standard of the Trap and Turret categories, and no separate criterion is written.
- **Where it lands**: the interception judgement in `behavior/` (first `instanceof ThrownCropEntity`, then whether the owner and the placer are allied, and only then the charge); the interception judgement table and the projectile family table (12 listed item by item) of `18-TECH-0.0.3.md`; the Point Defense Fungus section of `17-DESIGN-0.0.3.md`; the patch line at the end of D-87 of this file.

#### D-99 · An intercepted projectile is removed on the spot and its landing point is not settled

- **ID**: D-99
- **Decision**: when the interception succeeds the projectile is discarded immediately and **its onImpact is not triggered** (no explosion, no block written, no effect applied, no drop). Otherwise 'interception' would have no observable semantics, and the limit of 6 would lose its meaning too.
- **Rationale**: D-88 wrote only the charge and the graphic, not where the projectile goes — a projectile that is 'intercepted' and still settles its landing point as usual is as good as not intercepted. What is removed here is the **projectile entity**, not a block, so it is not the same path as D-72's 'the block is silently removed after the trigger' and the two must not be mixed: block removal still follows the D-72 standard (D-88), entity removal is given by this entry.
- **Where it lands**: the projectile base class in `entity/thrown/` (`discard()` is placed before `onImpact` and returns as soon as the interception succeeds); the projectile life-cycle table of `18-TECH-0.0.3.md`; the patch line at the end of D-88 of this file.

#### D-100 · The interception sound and the Vibration Bean explosion sound reuse vanilla sound events

- **ID**: D-100
- **Decision**: the interception sound of the Point Defense Fungus and the explosion sound of the Vibration Bean both **reuse existing Minecraft sound_event**s and add no registry object. The `sound_events` increment of 0.0.3 is therefore **still only the +1 of friend_chives**. Which exact events are reused is fixed by the sound table of 18-TECH-0.0.3.
- **Rationale**: the author provides only one audio asset this time (source file `pengyoudejiu.mp3`, build fact card §1.3) and the other two places have no asset at all; a new event would also need an ogg, which would be invention out of nothing. D-89 says 'play a sound' while the increment table of build fact card §3.1 gives only `sound_events` +1, so the two originally clashed, and this entry closes it by 'do not widen the registration surface'.
- **Where it lands**: `assets/farmstorm/sounds.json` (still only 1 new event, `friend_chives`); the sound table of `18-TECH-0.0.3.md` (stating place by place which vanilla event id is reused); the patch line at the end of D-89 of this file.

#### D-101 · The Vibration Bean uses the same ruler at both ends: strictly less than 4.0

- **ID**: D-101
- **Decision**: the trigger end (a non-ally player entering) and the effect end (applying to all players inside the radius) **both use strictly less than 4.0**. One end must not be written '< 4' with the other written '≤ 4', otherwise the self-contradiction 'not triggered yet applied' appears.
- **Rationale**: D-92 wrote only 'Euclidean distance < 4' and 'all players within radius 4'; if the two ends took different comparators, the boundary would produce the state 'the trap did not trigger, yet the player has already been given Confusion'. This is the same in nature as the comparator correction of D-94: **the comparator is part of the decision, not typography**.
- **Where it lands**: the trigger and settlement of the Vibration Bean in `behavior/` (the same constant, the same comparison); `vibration_bean.triggerRadius` 4.0 of `config/FarmstormConfig.java` is given as a single key shared by both ends; the judgement table of `18-TECH-0.0.3.md`; the patch line at the end of D-92 of this file.

#### D-102 · The mount branch of Q-Bounce must not add an entity_type

- **ID**: D-102
- **Decision**: the mount is carried by a **vanilla existing entity type** and no new entity_type is registered; the entity_types increment stays **+2** (the two projectiles). Which exact vanilla type is used is fixed by §4 of 18-TECH-0.0.3.
- **Rationale**: D-82 requires only 'both branches implemented' and does not authorize widening the registration surface. The mount entity must be invisible, collision-free and removed together with the mob's death (RT-18), and a vanilla type is already enough; adding an entity type that exists only to serve a pose would be adding an `entity_types` entry that nobody would ever use on purpose. In the same direction as D-100: both places must use something that already exists and must not widen the registration surface along the way.
- **Where it lands**: the entity table of §4 of `18-TECH-0.0.3.md` (stating the vanilla type reused and its attribute overrides); the `qbounce.poseMode = mount` branch of Q-Bounce; the patch line at the end of D-82 of this file.

---

## 4. Crop profile summary (22 entries)

The registry ids, types, categories, Chinese names and English names of the first 19 rows follow §4 of `10-DECISIONS-0.0.2.md` (that table has 19 rows); the three columns 'Crop block', 'Seed' and 'DeployedKind' are taken from the 19 constants of the source profile `src/main/java/com/dashpig/farmstorm/util/CropProfile.java` (supplemented by this document: the profile table of 0.0.2 has no such three columns, so they are filled in here from the implementation, without changing any existing value). The last 3 rows follow build fact card §3.2.

| # | registry id | Type | Category | Crop block | Seed | DeployedKind | Chinese name | English name | New in v0.0.3 |
|---|---|---|---|---|---|---|---|---|---|
| 1 | `puffed_wheat` | THROWABLE | CONSTRUCTION | `puffed_wheat_crop` | `puffed_wheat_seeds` | — | 膨胀麦 | Puffed Wheat | No |
| 2 | `blast_carrot` | DEPLOYABLE | TRAP, DAMAGE | `blast_carrot_crop` | `blast_carrot` | BLAST_CARROT | 胡罗贝 | Blast Carrot | No |
| 3 | `ubi_server` | THROWABLE | DEMOLITION, DAMAGE | `ubi_server_crop` | `ubi_server` | — | 育碧服务器 | Ubi Server | No |
| 4 | `toxic_ubi_server` | THROWABLE | BUFF, DEMOLITION, DAMAGE | `toxic_ubi_server_crop` | `toxic_ubi_server` | — | 毒育碧服务器 | Toxic Ubi Server | No |
| 5 | `power_beet` | EDIBLE | BUFF | `power_beet_crop` | `power_beet_seeds` | — | 甜菜！ | Power Beet | No |
| 6 | `melon_slice_bomb` | THROWABLE | DAMAGE, DEMOLITION | — | — | — | 瓜片子 | Melon Slice Bomb | No |
| 7 | `melon_grenade` | THROWABLE | DEMOLITION, DAMAGE | `melon_grenade_stem` | `melon_grenade_seeds` | — | 瓜！ | Melon Grenade | No |
| 8 | `pumpkin_turret` | DEPLOYABLE | TURRET, DAMAGE | `pumpkin_turret_stem` | `pumpkin_turret_seeds` | PUMPKIN_TURRET | man瓜 | Pumpkin Turret | No |
| 9 | `awakened_torchflower` | DEPLOYABLE | TURRET, BUFF | `awakened_torchflower_crop` | `awakened_torchflower_seeds` | AWAKENED_TORCHFLOWER | 激活的火把花 | Awakened Torchflower | No |
| 10 | `awakened_pitcher_plant` | DEPLOYABLE | TURRET, BUFF | `awakened_pitcher_plant_crop` | `awakened_pitcher_plant_pod` | AWAKENED_PITCHER_PLANT | 激活的瓶子草 | Awakened Pitcher Plant | No |
| 11 | `fang_trap_cactus` | DEPLOYABLE | TRAP, DAMAGE | `fang_trap_cactus_block` | `fang_trap_cactus` | FANG_TRAP_CACTUS | 刺球 | Fang Trap Cactus | No |
| 12 | `bamboo_firecracker` | THROWABLE | DAMAGE | `bamboo_firecracker_stalk` | `bamboo_firecracker` | — | 爆竹 | Bamboo Firecracker | No |
| 13 | `red_mushroom_puff` | THROWABLE | BUFF | `red_mushroom_puff_block` | `red_mushroom_puff` | — | ？！菇菇！？ | Red Mushroom Puff | No |
| 14 | `brown_mushroom_puff` | THROWABLE | BUFF | `brown_mushroom_puff_block` | `brown_mushroom_puff` | — | ？！棕色菇菇色棕！？ | Brown Mushroom Puff | No |
| 15 | `lucky_berry` | EDIBLE | BUFF | `lucky_berry_bush` | `lucky_berry` | — | 幸运浆果 | Lucky Berry | No |
| 16 | `molotov` | THROWABLE | DAMAGE, CONSTRUCTION | — | — | — | 莫洛托夫！ | Molotov | No |
| 17 | `zangief_cane` | DEPLOYABLE | TRAP | — | — | ZANGIEF_CANE | 桑吉尔蔗 | Zangief Cane | No |
| 18 | `wild_apple` | EDIBLE | BUFF | — | — | — | 狂野苹果 | Wild Apple | No |
| 19 | `compressed_puffed_wheat` | THROWABLE | CONSTRUCTION | — | — | — | 压缩膨胀麦 | Compressed Puffed Wheat | No |
| 20 | `friend_chives` | THROWABLE | BUFF | `friend_chives_crop` | `chive_seeds` | — | 朋友的韭 | Friend Chives | Yes |
| 21 | `point_defense_fungus` | DEPLOYABLE | TURRET | — | — | POINT_DEFENSE_FUNGUS | 近防菌 | Point Defense Fungus | Yes |
| 22 | `vibration_bean` | THROWABLE | TRAP | — | — | VIBRATION_BEAN | 震动豆 | Vibration Bean | Yes |

Notes:

- '—' in the table = no value (`null`). The three empty columns of rows 6, 16, 18 and 19 are the existing state of v0.0.2 (D-51, D-56); the all-empty crop block and seed columns of rows 21 and 22 are the new state of v0.0.3 (**no planting form**, they all go the deploy / throw path; of the three only Friend Chives has no vanilla counterpart at all (D-84), and the vanilla counterparts of Point Defense Fungus and Vibration Bean are used only for the conversion recipes (D-86, D-90); build fact card §3.2 point 1); the `DeployedKind` of row 22 does have a value, for the reason given in the entry 'The `DeployedKind` of row 22 is mandatory' below in this section.
- **The Chinese name of row 11 is the only Chinese-name change in the whole table**: the display name in 0.0.2 is '赢过输过没死过' and from 0.0.3 it is '刺球' (D-93). The registry id, the crop block id, the `DeployedKind` constant and the English name are all unchanged; the item-by-item comparison is in §5.2.
- The `DeployedKind` of rows 21 and 22 are the two enum values new in v0.0.3: `POINT_DEFENSE_FUNGUS` (D-86) and `VIBRATION_BEAN` (the landing block of D-91). v0.0.3 is **+2 values** in total, enum 6 → 8; enum constants are dispatch keys in code, not registry objects, and do not affect the total number of registry objects.
- **The `DeployedKind` of row 22 is mandatory**: the block the Vibration Bean lands in the world is still `DeployedCropBlock` (reusing the same block entity), and `DeployedCropBlockEntity.kindOf()` throws directly for anything that is not a `DeployedCropBlock`, while the kind is taken from `profile.kind()`; if none is found it **silently falls back to `BLAST_CARROT`** — which would make the Vibration Bean run as an explosion trap. Consistent with this table: both row 22 of build fact card §3.2 and note 2 there state that `VIBRATION_BEAN` must be filled in; the mechanism and the implementation are in §1.4(b) of `18-TECH-0.0.3.md`.
- The `friend_chives_crop` and `chive_seeds` of row 20 are the two registry objects new in v0.0.3 (part of `blocks` +3 and `items` +4 of build fact card §3.1); the block texture of the crop is a magenta #D81E8A placeholder this round and is not final (build fact card §3.4).
- registry id is `CropProfile.id()`, and is also the registration ID of the item / block / entity; the block and the item of a Deployable crop have the same name (following the standard of §4 of `10-DECISIONS-0.0.2.md`).
- The enum constant names of the types and categories are exactly the same as in v0.0.2 (THROWABLE / DEPLOYABLE / EDIBLE of `util/CropType.java`, TRAP / TURRET / DEMOLITION / BUFF / CONSTRUCTION / DAMAGE of `util/CropCategory.java`), and no enum value is added this round.

Counts are self-consistent (**supplemented by this document**: obtained by adding up the rows of this table, not the measured output of any tool):

| Dimension | Value | Rows |
|---|---|---|
| Type · Throwable | 12 | 1, 3, 4, 6, 7, 12, 13, 14, 16, 19, 20, 22 |
| Type · Deployable | 7 | 2, 8, 9, 10, 11, 17, 21 |
| Type · Edible | 3 | 5, 15, 18 |
| Category · Trap | 4 | 2, 11, 17, 22 |
| Category · Turret | 4 | 8, 9, 10, 21 |
| Category · Buff | 9 | 4, 5, 9, 10, 13, 14, 15, 18, 20 |
| Category · Demolition | 4 | 3, 4, 6, 7 |
| Category · Damage | 9 | 2, 3, 4, 6, 7, 8, 11, 12, 16 |
| Category · Construction | 3 | 1, 16, 19 |
| Total | 22 | 1 – 22 |

The profile increment this round is +3 (rows 20 – 22); 22 is the row count of the profile table, not the total number of registry objects. The log string of the startup self-check can only be given as a measured value once the implementation is finished, and this file does not hard-code it in advance (RT-23).

### 4.1 The standard for English names (no judgement table needed any more)

This version meant at first to judge an English name for each of the two new crops (one for the possessive-with-apostrophe form, one for the acronym-uppercase form). The conclusion is to **not judge at all**: the author picked both registry ids directly in the form 'Title Case is itself a good name', so the English names fall back to the mechanical derivation of C-10 and no hand-written override table is introduced.

| Object | id before the change (first-draft form) | id after the change | English name (= Title Case of the id) | Chinese name |
|---|---|---|---|---|
| Friend Chives | `pengyoudejiu` (the pinyin of the Chinese name written straight, which does not look like English after Title Case) | `friend_chives` | Friend Chives | 朋友的韭 |
| Point Defense Fungus | `ciws_fungus` (close-in weapon system acronym + genus name, awkward capitalisation after Title Case) | `point_defense_fungus` | Point Defense Fungus | 近防菌 |
| the `DeployedKind` constant of the Point Defense Fungus | `CIWS_FUNGUS` (follows the old id) | `POINT_DEFENSE_FUNGUS` | — | — |

The old ids in the table serve as provenance only and **are no longer used from v0.0.3 on**; a grep for them across the whole file can only hit this table, the single exception being the source file name of the audio asset (the `.mp3` file name is kept unchanged, see the D-66 row of §7.2).

Both are objects new this round, so changing the id carries no compatibility burden: no released save, recipe, tag or language key references them, and `chive_seeds` already had the same shape and is not affected. Three consequences:

1. **The English name is still a purely mechanical derivation from the id**: not one line of C-10's rule changes and DataGen needs no display-name override table.
2. **D-93 is still the only exception to C-10 for now** (the Chinese name no longer translates), and no second one is opened; point 3 of build fact card §3.2 says in so many words 'do not open a second one'.
3. **The apostrophe problem disappears**: Friend Chives contains no apostrophe; the source file name of the audio asset is kept unchanged (`pengyoudejiu.mp3`) while the sound event id is `friend_chives`.

The two changed ids are used consistently throughout this file: the titles and parentheses of the entries of §3.3, rows 20 and 21 of §4 and the references of §7.2 are all written with the new ids; the old ids appear only in the register table of this section, and the rest of the body always uses the new ids, so that they are not taken for current registration names.

---

## 5. Register of changes to the existing content of v0.0.2

This is the key section of this file: v0.0.3 is authorized to change the existing content of v0.0.2 in two places only, and **apart from these two, everything in 0.0.2 stays as it is** (§5.7 gives the boundaries by kind).

### 5.1 Scope of the authorization: two places only

| Place | Object | Decisions | What changed | What did not change |
|---|---|---|---|---|
| 1 | Fang Trap Cactus `fang_trap_cactus` | D-93, D-94, D-95 | the Chinese display name; four numeric items (trigger distance, outer delay, inner delay, hit effect) | registry id, English name, crop block id, `DeployedKind` constant, the fang entity and its attribution, the loot table, the two ring counts and the two distribution radii |
| 2 | Zangief Cane `zangief_cane` | D-96 | the natural fall damage during the phase 2 fall (cancelled); the landing damage (fixed 7.5 points); 1 new damage type | the form, world invisibility, phase timing, floating level, spin, trigger judgement and radius, silent removal, damage attribution |

### 5.2 Fang Trap Cactus: register of the rename (D-93)

| Item | Current value in 0.0.2 | 0.0.3 | Changed |
|---|---|---|---|
| registry id | `fang_trap_cactus` | `fang_trap_cactus` | no |
| Crop block id | `fang_trap_cactus_block` | same as the left | no |
| `DeployedKind` constant | `FANG_TRAP_CACTUS` | same as the left | no |
| English display name (C-10) | Fang Trap Cactus | Fang Trap Cactus | no |
| Chinese display name (the value in `zh_cn.json`) | 赢过输过没死过 | 刺球 | **yes (the only item)** |
| Number and set of language keys | the same as English | the same as English | no (only values change, no key added or removed) |
| Loot table | drops itself ×1 when mined | same as the left | no |
| Fang entity and attribution | vanilla `EvokerFangs`, attribution to the deployer | same as the left | no |

The rename is not 'changing a profile field': the Chinese name lives in the language file and none of the eight fields of `CropProfile` moves. That is also the basis for row 1 of §5.7.

### 5.3 Fang Trap Cactus: number delta table (the original D-94 table, carried over verbatim)

| Item | Current value | 0.0.3 |
|---|---|---|
| Trigger distance (Euclidean) | 3.0 | **≤ 2.5** |
| Outer ring fang count | 8 | 8 (unchanged) |
| Outer ring delay | 10 ticks | **4 ticks (0.2 s)** |
| Outer ring distribution radius | 2.0 | 2.0 (unchanged) |
| Inner ring fang count | 5 | 5 (unchanged) |
| Inner ring delay | 4 ticks | **2 ticks (0.1 s)** |
| Inner ring distribution radius | 1.0 | 1.0 (unchanged) |
| Hit effect | none | **Slowness V (amplifier 4) 60 ticks** |

- This table is the **only numeric standard** of this file and is identical character for character with the 'Decision' of D-94 in §3.4; it has **8 data rows**, the same row count as the original D-94 table of build fact card §2, with no row added or removed.
- Of the 8 rows **4 change** (trigger distance, outer delay, inner delay, hit effect) and **4 are marked unchanged** (outer count, outer radius, inner count, inner radius). The 'unchanged' ones must be written into the table too, because they are the boundary of this decision: changing these three values does not mean the radii and the counts may be changed along the way.
- The inner ring still comes out first and the outer ring still after it (the order is unchanged). The outer ring goes from 10 ticks to 4 and the inner ring from 4 ticks to 2, and the 2-tick gap between the two rings is still there.
- The plan says 'less than or equal to 2.5'; the comparator is part of the decision and the number alone must not be written; so the 0.0.3 column says **≤ 2.5**, not 2.5.

### 5.4 Fang Trap Cactus: knock-on changes

| Position | 0.0.2 | 0.0.3 | Basis |
|---|---|---|---|
| config `fang_trap_cactus.triggerRadius` | 3.0 | 2.5 | D-94 (the default of an existing key is changed) |
| config `fang_trap_cactus.outerDelayTicks` | 10 | 4 | D-94 (the default of an existing key is changed) |
| config `fang_trap_cactus.innerDelayTicks` | 4 | 2 | D-94 (the default of an existing key is changed) |
| config `fang_trap_cactus.slownessDurationTicks` | does not exist | 60 (new key) | D-94, build fact card §3.3 |
| config `fang_trap_cactus.slownessLevel` | does not exist | 5 (new key) | D-94, build fact card §3.3 |
| Hit effect | none (no potion effect applied) | Slowness V (amplifier 4) 60 ticks | D-94, D-95 |
| Trigger judgement entry | Euclidean 3.0 | Euclidean 2.5, every other judgement unchanged | D-94 |
| Language file | Chinese value = 赢过输过没死过 | Chinese value = 刺球; the key set and the key count are unchanged | D-93 |

The amplifier of Slowness V is **4**, because vanilla effect levels count from amplifier 0 (level I) (that conversion was already fixed by v0.0.2 in D-69 of `10-DECISIONS-0.0.2.md`); 60 ticks is 3 seconds. The breakdown of the config change over the whole repository is given by build fact card §3.3: 19 keys (16 pure additions + 3 changed existing defaults), plus 1 row that is the note 'Confusion has no key' and does not count as a key (§8 has the provenance).

### 5.5 Zangief Cane: cancel the natural fall damage + fixed 7.5 on landing (D-96)

| Item | 0.0.2 | 0.0.3 | Basis |
|---|---|---|---|
| natural fall damage during the phase 2 fall | takes effect (the controller has no fall hook at all) | **cancelled**, only during phase 2 of this trap | D-96 |
| Landing damage | none | **fixed 7.5 points** | D-96 |
| damage type | `farmstorm:crop_blast`, `farmstorm:void_bolt` (2 in total) | adds `farmstorm:zangief_slam` (the 3rd) | D-96 |
| config `zangief_cane.slamDamage` | does not exist | 7.5 (new key) | build fact card §3.3 |
| config `zangief_cane.cancelFallDamage` | does not exist | true (new key, boolean) | build fact card §3.3 |
| Damage attribution | the trap owner | same as the left | D-96 |
| Form and world invisibility, phase timing, floating level, spin, trigger judgement and radius, silent removal | the v0.0.2 standard | as it is | 'keeps every existing feature of 0.0.2' of D-96 |

The way the fall damage is cancelled is constrained by RT-22: only the fall damage 'during phase 2 of this trap' is cancelled, judged by the presence of the effect, the global fall logic is not changed, and the behaviour of other mods such as slow falling, immunity and flight is not touched either. The cancellation and the settlement must appear as a pair — first cancel the natural fall, then apply the fixed 7.5 points at the instant of landing, both steps in the same phase state machine, and leaving out either one turns it into 0 damage or double damage.

### 5.6 Why only these two places

1. **0.0.2 is implemented and released** (build fact card §1, `dist/farmstorm-0.0.2+mc1.21.1.jar`). Changing its existing numbers means changing a behaviour that has already been handed out, and the cost is a real change in the player's experience, not a change of wording on the document side.
2. **Items 6 and 7 of the author's requirement text are the only change authorizations this round**: one asks for the display name and the numbers of the Fang Trap Cactus, one asks for the landing damage of the Zangief Cane. Build fact card §2 turns these two into the four decisions D-93 ~ D-96 (3 for the Fang Trap Cactus, 1 for the Zangief Cane), and no other decision changes existing content.
3. **The last item of §7 of the build fact card states it as a prohibition**: 'do not treat the existing numbers of 0.0.2 as something free to change: D-93 ~ D-96 is the only authorized scope of change (Fang Trap Cactus and Zangief Cane). D-97 is a new standard and changes no existing code.' Half of the reason this section exists is to register what changed, the other half is to prove that **only these changed**.
4. **Once one more place changes, the knock-on cost far exceeds the gain**: as soon as an existing number changes, `10-DECISIONS-0.0.2.md` and `docs/en/10-DECISIONS-0.0.2.md` fall out of step with the implementation, while 00 – 15 is frozen by D-80 and cannot be edited back; v0.0.1 also stepped into this pit of 'editing the old files back' (the freeze reason of D-49). Two places are the exception, editing back is the rule.

### 5.7 Apart from these two places, everything in 0.0.2 stays as it is

| Object | Treatment |
|---|---|
| the fields of the 19 existing `CropProfile`s (id, type, category, crop block, seed, kind, attachment face, stack) | untouched. The Chinese name of row 11 is not in the profile, it is the value of the language file (§5.2) |
| the defaults of the existing config keys | only the 3 keys listed in §5.4 change their default; the defaults of every other existing key are untouched |
| existing conversion recipes | untouched. This round only adds 2 new bidirectional recipes (D-86, D-90); Friend Chives gets no recipe (D-84) |
| existing advancements | untouched. This round adds 3 (advancements +3 of build fact card §3.1), which is an addition rather than a change; hard count 15 → 18 (the 15 comes from D-63 of `10-DECISIONS-0.0.2.md`) |
| existing `mob_effects` / `sound_events` / `damage_types` entries | untouched. Only new entries are appended (D-81, D-83, D-85, D-96); this round adds only 1 sound event, `friend_chives`, and the interception and explosion sounds reuse vanilla events (D-100) |
| the behaviour and appearance of the existing deployed blocks (including the world invisibility of the Trap category) | untouched (D-68 stays in force); the Point Defense Fungus is a new block and the Vibration Bean is a new block (the new mode of D-91 applies to it alone) |
| the three client subsystems (world grayscale, red silhouette, input lock) | untouched. Q-Bounce reuses the input lock (D-79, D-81), and Confusion changes only the direction components of the movement input (D-83) |
| the existing behaviour of the projectile base class `entity/ThrownCropEntity.java` | untouched. The 2 new `entity_type`s go through it and do not change the behaviour of the existing 10 (the family size is corrected by D-98 to 10 + 2 = 12) |
| all the documents 00 – 15 (16 under `docs/zh`, 14 under `docs/en`) | untouched (D-80) |

---

## 6. What v0.0.3 explicitly does not do

The 12 entries below are the exclusion list of this version; no exception may be opened for any of them during the implementation phase: when an exception is needed, change this file first, then the code.

1. **Do not change any one of 00 – 15**: not one of the 16 under `docs/zh` and the 14 under `docs/en` is changed, and `.work/v003-BRIEF.md` is read only, never written; standard clarifications are registered in this file only (D-80).
2. **Do not change the registry id or the English name of the Fang Trap Cactus**: the id is still `fang_trap_cactus`, the crop block is still `fang_trap_cactus_block`, the `DeployedKind` constant is still `FANG_TRAP_CACTUS` and the English name is still Fang Trap Cactus; the Chinese name is the only renamed item (D-93, §5.2).
3. **Do not give Friend Chives a converter recipe**: it has no vanilla counterpart, and conversion recipes serve only the bidirectional mapping 'vanilla crop ⇄ improved crop'; it takes part in converter crafting only as a member of the `farmstorm:converter_fuel` tag (D-84).
4. **Do not let the Point Defense Fungus intercept vanilla projectiles**: arrows, snowballs, tridents and ender pearls are never intercepted, and neither are the projectiles of other mods; the judgement surface is only the 12 non-ally members of the `ThrownCropEntity` family (D-87, D-98).
5. **Do not let interception by the Point Defense Fungus deal damage or break blocks**: `hurt` is not called, no block is written, no `Explosion` is constructed; interception has particles and a sound only (D-89).
6. **Do not give Q-Bounce or Confusion an independent duration key**: the duration is decided by the applier (D-85 gives 200 ticks, D-92 gives 80 ticks), config has no 'duration of the effect itself' tier, and the Confusion group sets no key at all (D-81, D-83, build fact card §3.3).
7. **Do not build a second input lock**: Q-Bounce reuses the seven-segment input lock of D-79 directly, and Confusion changes only the direction components in `MovementInputUpdateEvent`; neither adds a locking mechanism (D-81, D-83, RT-19).
8. **Do not delete the Q-Bounce branch that was not selected**: both render and mount must be implemented, the author keeps one after comparing on a real client, and the one that loses is switched off through `qbounce.poseMode` rather than deleted from the code (D-82).
9. **Do not reuse `supportsFloating` for the floating block of the Vibration Bean**: that is 'tolerate after the attachment is lost', a different semantics from 'never attached from the start', so a separate 'unattached' mode must be added (D-91, RT-21).
10. **Do not hard-code the total number of registry objects**: this file gives only the increment +13 and its breakdown, and the total is filled in by `18-TECH-0.0.3.md` as a measurement after `tools/verify_project.py` is fixed; before the tool is fixed no assertion about the total holds (the note under the table of build fact card §3.1, item 1 of §4 number discipline, RT-23).
11. **Do not add new exclusions to the Lucky Berry pool**: the pool is still '`ALL` minus `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`', and the three new profiles are all included; to exclude something, the decision must be changed first and then the derivation (D-97, D-55).
12. **Do not add sound events for the interception sound or the explosion sound, and do not add an entity_type for the mount branch of Q-Bounce**: the interception sound and the Vibration Bean explosion sound reuse vanilla `sound_event`s (D-100) and the mount is carried by a vanilla existing entity type (D-102); the `sound_events` increment is +1 only and the `entity_types` increment is +2 only.

---

## 7. Inheritance relation to the v0.0.2 decisions

### 7.1 Marker legend

| Marker | Meaning |
|---|---|
| Full continuation | neither the conclusion nor the scope of application changes |
| Extended this time | the conclusion is unchanged; this version adds entries to the list it governs or to the objects it applies to |
| Refined this time | the conclusion is unchanged; this version pins down part of its means or landing points |
| Scope changed | this version rewrote part of its wording |

### 7.2 D-49 ~ D-79 (31 entries)

| ID | Status | Note |
|---|---|---|
| D-49 | Full continuation | the v0.0.1 documents stay frozen; D-80 does the same thing to v0.0.2. |
| D-50 | Full continuation | no `CustomPacketPayload` is added; the state sync of Q-Bounce and Confusion continues to use the amplifier + remaining duration of the hidden `MobEffect`. |
| D-51 | Extended this time | the list of 'the three new crops have no planting form' gains two: the crop block and seed columns of `point_defense_fungus` and `vibration_bean` are all empty (rows 21 and 22 of §4); the `DeployedKind` of row 22 is the exception and has a value, see the note under the §4 table. Friend Chives does have a planting form and is not in this list. |
| D-52 | Full continuation | the converter crafting recipe and the `farmstorm:converter_fuel` tag standard are unchanged; per D-84, Friend Chives only joins the tag and does not want a conversion recipe of its own. |
| D-53 | Extended this time | the media pipeline is still the same single script (no second one is written) and its processing surface extends to the assets of 0.0.3; the icon is still an 18×18 PNG and the audio still mono 64 kbps OGG. |
| D-54 | Full continuation | the shape of 3 experience points, no catalyst and bidirectional is reused verbatim by D-86 and D-90. |
| D-55 | Extended this time | the conclusion continues in full and is extended by D-97: the pool goes 16 → 19, the three new profiles (`friend_chives` / `point_defense_fungus` / `vibration_bean`) are all included, the derivation is unchanged and no new exclusion is added. |
| D-56 | Full continuation | the Compressed Puffed Wheat is still a second-tier composite; it does not enter conversion recipes and does not enter the random pool. |
| D-57 | Full continuation | `strength(30.0F, 6.0F)` of the Compressed Hay Block and its non-rotatability on all six faces are both unchanged. |
| D-58 | Full continuation | the landing semantics of the Molotov and the 0-power trigger are unchanged; the 'not even an `Explosion` is constructed' of D-89 is the narrower wording and does not change this entry. |
| D-59 | Full continuation | the roll + pitch stacking of the Zangief Cane is unchanged (D-96 touches the fall damage only). |
| D-60 | Extended this time | the lock range and 'only ESC is left' are unchanged, and Q-Bounce is added to the objects it applies to (D-81); its means have already been fixed by D-79 as the seven-segment form. |
| D-61 | Full continuation | world grayscale and the original colours of the HUD are unchanged. |
| D-62 | Full continuation | the visibility of the red silhouette and the 64-block cap are unchanged. |
| D-63 | Extended this time | existing advancements are untouched and this version adds 3 (first Q-Bounce applied / first intercepted by a Point Defense Fungus / first Vibration Bean triggered, advancements +3 of build fact card §3.1); hard count 15 → 18, and F-133 of `19-TASKS-0.0.3.md` is executed as a hard task with the same count. |
| D-64 | Full continuation | the target acquisition standard of the Wild Hunter is unchanged. |
| D-65 | Full continuation | the standard of pulling with a dash rather than teleporting is unchanged. |
| D-66 | Extended this time | the rule 'the asset provenance and the non-commercial personal use statement must be written' is unchanged, and 1 sound event is added to the objects it applies to: the event id is `friend_chives` and the asset is the source file `pengyoudejiu.mp3` (7,450,588 B, the file name is kept unchanged, build fact card §1.3). |
| D-67 | Full continuation | the shapeless crafting of the Compressed Puffed Wheat is unchanged. |
| D-68 | Full continuation | the world invisibility of the Zangief Cane (empty model + `textures.particle`) is unchanged. |
| D-69 | Full continuation | floating amplifier 6, 20 ticks and the timing of applying it on tick 14 are unchanged. |
| D-70 | Full continuation | the spin of yaw +72° per tick is unchanged. |
| D-71 | Full continuation | the standard that the Zangief Cane recognises non-ally players only and no mobs is unchanged. |
| D-72 | Extended this time | the path standard 'silently removed after the trigger, no drop produced' is unchanged and one object is added: the removal after the 6th interception of the Point Defense Fungus goes the same path (D-88). |
| D-73 | Full continuation | the 1.5-block spherical Euclidean judgement of the Zangief Cane is unchanged; the Vibration Bean of D-92 brings its own Euclidean standard of < 4 and does not change this entry. |
| D-74 | Full continuation | the kill-time extension of the Wild Hunter with no cap is unchanged. |
| D-75 | Full continuation | knockback with the vanilla default strength is unchanged. |
| D-76 | Full continuation | the looping sound with the client `SoundInstance` is unchanged. |
| D-77 | Full continuation | the Compressed Puffed Wheat keeps the two standards of the Puffed Wheat landing write unchanged. |
| D-78 | Full continuation | the clarification of the demolition threshold attribution is unchanged, and this version does not touch `BlastRule`. |
| D-79 | Refined this time | the conclusion of the seven-segment input lock is unchanged; D-81 makes it clear that Q-Bounce reuses it instead of writing a new one, and D-83 makes it clear that Confusion builds no lock and only changes the direction components of the movement input. |

Of the 31 entries 23 are full continuation and 8 are extended or refined this time (7 extended: D-51, D-53, D-55, D-60, D-63, D-66, D-72; 1 refined: D-79), and **0 are revised this time**. This last item is the most important conclusion of this section: no entry among D-49 ~ D-79 defines the numbers or the Chinese name of `fang_trap_cactus`, or the fall damage of `zangief_cane` — `fang_trap_cactus` appears in D-49 ~ D-79 only as one of the 'five existing deployed blocks' in the rationale of D-72, and its numbers have always been recorded in `00-DECISIONS.md` and in the defaults of `config/FarmstormConfig.java`. So the changes of D-93 ~ D-96 land in two places outside that list: one is the 4 entries on the v0.0.1 side (see 7.3), the other is the code and the config defaults (§5.4, §5.5).

### 7.3 Knock-on register on the v0.0.1 side (4 entries)

| ID | Status | Note |
|---|---|---|
| C-5 | Scope changed | the value of '赢过输过没死过 trigger distance uses Euclidean distance ≤ 3' is changed by D-94 to 2.5; the distance metric (Euclidean) is unchanged. |
| C-10 | Extended this time | 'display name = Title Case of the registry ID' is still the only rule, and D-93 adds the first explicit exception on the Chinese side (Chinese and English no longer translate each other); no exception is added on the English-name side (§4.1). |
| D-20 | Refined this time | the value of the exception follows D-94 from 3 to 2.5; the main rule 'traps and turrets use Chebyshev, buff auras use Euclidean' is unchanged. **Supplemented by this document**: the Euclidean cases added in this version (Friend Chives 6 blocks, Point Defense Fungus 8 blocks, Vibration Bean < 4) are judged one by one under this entry's standard 'auras / effect radii use Euclidean', and are written into the 'Rationale' of the respective decisions, without changing the wording of this entry. |
| D-30 | Full continuation | the distribution radius of the fangs is still unchanged: D-94 only changes **trigger distance, the two delays and the on-hit effect**; both ring radii 2.0 and 1.0 and the counts 8 and 5 stay as they are. |

The one to watch most is **D-72**: it is the only 'disappearance path' standard reused by v0.0.3, and both the Point Defense Fungus disappearing when used up (D-88) and the Vibration Bean disappearing on trigger (D-92) are written after it, neither of them going through a loot table. The second most important is **D-50**: the two status effects of this version still add not a single custom network packet. The third is **D-55**: the random pool grows from 16 to 19 per D-97, not one line of the derivation changes, and the three new profiles are all included.

---

## 8. The numbers cited in this file and their provenance

This section lists one by one the numbers this file uses as a quantity, size, distance, duration, damage or probability. The numbers themselves (the D-, C-, RT-, F-, HC-, N- and Phase prefixes) do not count as quantities. The 'supplemented by this document' entries are numbers derived by this file or read out of the source code that the build fact card does not give directly.

| Number | Meaning (section) | Provenance |
|---|---|---|
| 139 | current number of config keys (§1, verified) | the §1 table of the build fact card; obtained by `tools/verify_project.py` reading the generated toml |
| 263 | current number of language keys, the same in Chinese and English (§1, verified) | the §1 table of the build fact card; §3 of `tools/verify_project.py` |
| to be measured | total number of registry objects (§1, item 10 of §6) | the §1 table of the build fact card (that row says in so many words 'to be measured, do not guess'), the note under the table of §3.1, §6 RT-23 |
| 23 | number of decisions in this file (§1, §3) | §3 of this file; the closed interval D-80 – D-102 |
| 5 | number of supplementary rulings (D-98 ~ D-102) (§3.6) | §3.6 of this file |
| 16 / 14 | number of frozen Chinese files and English files (§1, §5.7, item 1 of §6) | build fact card §1.1; the actual file counts of `docs/zh/*.md` and `docs/en/*.md` |
| 6 | number of documents added this round (§2) | D-80; build fact card §5 |
| 7 | number of entries document 17 has to write (2 effects + 3 crops + 2 reworks) (§2) | build fact card §5; the grouping of §3 of this file |
| +13 | total registry object increment (§1, item 10 of §6) | build fact card §3.1 |
| +4 / +3 / +2 / +2 / +1 / +1 / +2 / +3 / +0 | the breakdown of that increment (items / blocks / entity_types / mob_effects / sound_events / damage_types / converter recipes / advancements / block_entities); of which the +1 of `sound_events` counts only `friend_chives` (D-100) and the +2 of `entity_types` counts only the two projectiles (D-102) | build fact card §3.1 |
| +8 / +1 | the PNG and OGG increment: **8** PNGs (Q-Bounce icon 1 + Friend Chives item 1 + Chive Seeds item 1 + crop placeholders 4 + Vibration Bean item icon 1), 1 OGG (the note of §4, §5.7) | build fact card §3.4 (already corrected to 8) |
| 2 → 3 | number of events in `sounds.json` (§5.7) | build fact card §3.4 |
| 19 → 22 | number of profiles (§4) | build fact card §3.2; §4 of `10-DECISIONS-0.0.2.md` (19 rows) |
| 15 → 18 | number of advancements (§5.7, the D-63 row of §7.2) | 15 comes from the D-24 row of §7 of `10-DECISIONS-0.0.2.md` (advancements 12 → 15); +3 comes from the advancements increment of build fact card §3.1 |
| 16 → 19 | number of entries in the Lucky Berry random pool (§3.5, §7.2) | build fact card §2 D-97 |
| 19 / 16 / 3 / 1 | breakdown of the config change: 19 keys (16 pure additions + 3 changed existing defaults), plus 1 non-key note row (§5.4) | the title and the note under the table of build fact card §3.3 |
| 22 | number of data rows of the §4 table (§4) | §4 of this file |
| 12 / 7 / 3 | type distribution of §4 (Throwable / Deployable / Edible) (§4, supplemented by this document) | adding up the rows of §4 of this file |
| 4 / 4 / 9 / 4 / 9 / 3 | category distribution of §4 (Trap / Turret / Buff / Demolition / Damage / Construction) (§4, supplemented by this document) | as above |
| 3.0 → ≤ 2.5 | trigger distance and comparator of the Fang Trap Cactus (§3.4, §5.3) | build fact card §1.2 (current value), §2 D-94 (new value); the comparator comes from the text of plan0.0.3.txt 'less than or equal to 2.5' |
| 8 → 8 | outer ring fang count (§5.3) | build fact card §1.2, §2 D-94 |
| 10 → 4 | outer ring delay, ticks (§5.3) | as above |
| 2.0 → 2.0 | outer ring distribution radius (§5.3) | as above |
| 5 → 5 | inner ring fang count (§5.3) | as above |
| 4 → 2 | inner ring delay, ticks (§5.3) | as above |
| 1.0 → 1.0 | inner ring distribution radius (§5.3) | as above |
| 8 / 4 | number of changed rows and number of rows marked unchanged in the §5.3 table (§5.3, supplemented by this document) | comparing the rows of §5.3 of this file |
| 5 / 4 | level and amplifier of Slowness V (§5.4) | build fact card §2 D-94; the amplifier counting standard (0 = level I) of D-69 of `10-DECISIONS-0.0.2.md` |
| 60 | slowness duration in ticks (§5.3, §5.4) | build fact card §2 D-94 |
| 2 | gap in ticks between the two rings (§5.3) | build fact card §2 D-94 (the difference between 4 ticks and 2 ticks) |
| 200 | Q-Bounce 10 seconds = 200 ticks (§3.2, item 6 of §6) | build fact card §2 D-81 |
| 18×18 | effect icon size (§3.2) | build fact card §2 D-81; the effect icon spec of D-53 |
| 6 / 32 | Friend Chives landing sphere radius / audible distance of the sound, blocks (§3.3) | build fact card §2 D-85 |
| 3 | conversion experience cost, points (§3.3, 3 each for the Point Defense Fungus and the Vibration Bean) | build fact card §2 D-86, D-90 (the same shape as D-54) |
| 8 | interception radius of the Point Defense Fungus, blocks (§3.3, item 4 of §6) | build fact card §2 D-87 |
| 9 + 2 | the original way the family size was written in the D-87 conclusion (already corrected by D-98) (§3.3) | build fact card §1.2, §3.1 |
| 10 + 2 = 12 | actual number of projectiles of the `ThrownCropEntity` family (the 10 of 0.0.2 + the 2 of 0.0.3) (§3.3, §3.6) | the item-by-item list of D-98; build fact card §3.1 (+2) |
| 6 → 8 | number of `DeployedKind` enum values (0.0.3 adds the two values `POINT_DEFENSE_FUNGUS` and `VIBRATION_BEAN`) (the note of §4) | rows 21 and 22 of §4 of this file; the existing 6 values are measured from `behavior/DeployedKind.java` |
| 6 | upper limit of the interception count of the Point Defense Fungus (removed after the 6th) (§3.3, §7.2) | build fact card §2 D-88 |
| 1 | number of charges several fungi take at the same instant (§3.3) | build fact card §2 D-88 |
| 4 / 4 | Vibration Bean trigger distance / Confusion effect radius, blocks; both ends use strictly less than (§3.3, §3.6) | build fact card §2 D-92; D-101 unifies the comparator |
| 80 | Confusion 4 seconds = 80 ticks (§3.3, item 6 of §6) | build fact card §2 D-92 |
| 7.5 | fixed landing damage of the Zangief Cane (§3.4, §5.5) | build fact card §2 D-96 |
| 3 | ordinal of the damage type (the new one is the 3rd, 0.0.2 has only 2) (§5.5) | build fact card §2 D-96 |
| 2 places | number of places authorized to change existing content (§1, §5.1, §5.6) | the last item of §7 of the build fact card; §5 of this file |
| 8 rows | number of data rows of the D-94 delta table (§5.3) | the original D-94 table of build fact card §2 |
| 31 | number of entries of D-49 ~ D-79 (§7.2) | §1 of `10-DECISIONS-0.0.2.md` (the decision number range holds 31 entries) |
| 23 / 8 / 0 | number of entries of §7.2 that are full continuation / extended or refined / revised (§7.2, supplemented by this document) | adding up the rows of §7.2 of this file |
| 4 | number of knock-on entries of §7.3 (§7.3, supplemented by this document) | §7.3 of this file |
| 7 | number of entries RT-17 ~ RT-23 (§1, §3) | build fact card §6 |
| 12 | number of 'explicitly does not do' entries of §6 (§6, supplemented by this document) | §6 of this file |

Restatement of the number discipline: this file contains no assertion about the total number of registry objects. +13 is the increment, 22 is the row count of the profile table, 139 and 263 are the actual output of `tools/verify_project.py`; every other count is pointed at the build fact card or an existing decision, item by item, in the table above.

## Translator's notes

None.
