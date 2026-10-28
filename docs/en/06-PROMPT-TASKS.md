# 06 · Phased Task List (Task Prompt)

> **Document status**: this file is a construction-period historical record of v0.0.1; its numbers and structural descriptions may have been corrected by the implementation. **The implementation baseline is `00-DECISIONS.md`** (see D-42 ~ D-48); the complete list of discrepancies is in `09-IMPL-VS-PLAN.md`.

| Item | Value |
|---|---|
| Document ID | 06-PROMPT-TASKS |
| Version | v0.0.1 |
| Audience | The person feeding the coding agent + the CLI coding agent being fed (Claude Code / Codex class) |
| Upstream | `plan0.0.1.txt`, `docs/zh/00-DECISIONS.md` (single source of truth), `01-DESIGN.md`, `02-TECH-SPEC.md`, `03-TASKS.md` |
| Peer | `05-PROMPT-MAIN.md` (the resident main prompt; this file does **not** repeat its HC-01~HC-14, N-01~N-14) |
| Loading method | Feed once per phase: the full text of `05-PROMPT-MAIN.md` + the matching section of this file |
| Contents | 10 phases / 59 tasks (F-01 ~ F-59); task IDs and titles align with `03-TASKS.md` verbatim |

**Chinese name → English name (names used in this file)**

| Chinese | English |
|---|---|
| 农场风云 | Farmstorm |
| 农产品转换器 | Produce Converter |
| 膨胀麦 | Puffed Wheat |
| 胡罗贝 | Blast Carrot |
| 育碧服务器 | Ubi Server |
| 毒育碧服务器 | Toxic Ubi Server |
| man瓜 | Pumpkin Turret |
| 瓜片子 | Melon Slice Bomb |
| 瓜！ | Melon Grenade |
| 爆竹 | Bamboo Firecracker |
| 红菇 | Red Mushroom |
| 棕菇 | Brown Mushroom |
| 仙人掌尖牙陷阱 | Fang Trap Cactus |
| 火把花 | Torchflower |
| 瓶子草 | Pitcher Plant |
| 甜菜！ | Power Beet |
| 幸运浆果 | Lucky Berry |

---

## 0. How to Use This File (for humans)

### 0.1 Division of Labor Between This File and 05-PROMPT-MAIN.md

| File | What it governs | Feed frequency |
|---|---|---|
| `05-PROMPT-MAIN.md` | Global discipline and hard constraints (HC-01~HC-14, §4 code conventions, §5 execution discipline, §6 definition of done, §7 report format, §8 forbidden actions, §9 known traps) | Resident in every phase |
| `06-PROMPT-TASKS.md` (this file) | Which tasks to do this round, what counts as passing, and how to open the next phase after delivery | One section at a time |

Three rules:

1. This file does not restate the hard constraints; it cites their IDs only. When you see "HC-xx", "N-xx", or "RT-xx", go back to 05 or TECH for the original text — do not interpret from memory.
2. On conflict, 05's hard constraints win (05 §0's adjudication order: DEC > TECH > DESIGN > plan0.0.1.txt > this file).
3. This file's acceptance criteria only tighten within 05 §6's definition of done; they never invent new deliverables.

### 0.2 How to Feed It

1. Open this file, find the section for the current phase (`## Phase N · …`), and copy the whole section starting from the heading.
2. Feed it to the coding agent together with the **full text** of `05-PROMPT-MAIN.md` — **one phase at a time**.
3. Wait for it to hand back a report per 05 §7's report template, then accept item by item against this section's "Phase exit criteria".
4. Once acceptance passes, open the next phase with the hand-off wording in §12.

Two prohibitions:

- Do not feed the §1 overview table along with it — the agent will try to finish all ten phases at once (the same mistake as N-01).
- Do not skip any section: the phase numbers are the feed order, and the dependency graph in `03-TASKS.md §1.1` has already laid it out.

### 0.3 How to Judge That a Phase Is Complete

All three gates must be green to pass; missing one is handled as "not done":

1. **Exit criteria**: every item of this section's "Phase exit criteria" can be pointed to command output, a `/data get` read-back, file content, or a log line. "Implemented but not verified" does not pass.
2. **Task coverage**: every F-xx in the task list has identifiable evidence; when the agent skipped one, require it to be made up per 05 §5.5 — do not fill it in yourself.
3. **Repository state**: `./gradlew build` is green; `git status` shows only files inside this section's boundary; `plan0.0.1.txt` and the upstream documents are untouched (HC-06).

Order of handling when a phase fails: first have it self-check per 05 §5.4 → if that still fails, copy the failing acceptance items back into the task prompt and dispatch once more → only after two failures, redo it in a new session, naming in the hand-off which item it is stuck on.

### 0.4 The Four Latest Adjudications (they override older wording in `03-TASKS.md`; getting them wrong means rework)

| # | Adjudication | Correct wording | Common mistake |
|---|---|---|---|
| C-9 | A deployment block stores **only** `ownerUuid` + `ownerName` | The ally check **resolves the owner's current team in real time** at the trigger instant; when the owner is offline, look their last known team up in the scoreboard member list; when the owner has no team, use D-31's `self` rule | Storing a "team name snapshot" or a `PlayerTeam` object (the old wording of adjudication 2 in `03-TASKS.md §0.4` is superseded by C-9) |
| C-8 / D-37 | The fixed **10-second hard mining belongs only** to `pumpkin_turret` | Torchflower / Pitcher Plant (火把花 / 瓶子草): allies mine at normal vanilla speed; non-allies (creative mode included) always get progress 0; no explosion can destroy them | Applying the 10 seconds to `awakened_torchflower` / `awakened_pitcher_plant` |
| C-7 | **12** advancements, **257** resources, **216** language keys | 257 = JSON 169 + TOML 1 + PNG 87 (the extra 6 are advancement JSONs) | Reusing TECH §6.1's old figures 251 / 204 |
| C-6 | Particles and sounds **use vanilla packets** | `ClientboundLevelParticlesPacket` / `ClientboundSoundPacket` | Writing your own S2C packet (which also violates HC-03) |

### 0.5 Number Quick Reference (needed in every phase)

| Quantity | Value | Source |
|---|---|---|
| Crops | 15 (not 16, C-1) | DEC §4 |
| registry objects | 64 | TECH §3 |
| config keys | 117 (Phase 1 lands only 58; the rest are appended phase by phase) | TECH §4.11 |
| Resource files | 257 | TECH §6.1 + C-7 |
| Language keys | 216 | TECH §7 |
| Advancements | 12 (including 1 hidden) | C-7; DESIGN §6 |
| Tasks | 59 / 10 phases | `03-TASKS.md §2` |

---

## 1. Overview Table (for humans; do not feed it together with a phase)

| Phase | Title | Tasks | Task IDs | Prerequisite phases | Exit criteria (one sentence) |
|---|---|---|---|---|---|
| Phase 1 | Project Foundation | 5 | F-01 ~ F-05 | — | `build` produces a jar, `runServer` prints `Done`, `config/farmstorm-server.toml` generates 58 keys, `Farmstorm.id()` works |
| Phase 2 | Full Registry Layer | 8 | F-06 ~ F-13 | Phase 1 | all 22 creative-tab items present, 19 blocks `/setblock`-able, 9 entities `/summon`-able, 5 of the 6 BlockEntityTypes registered |
| Phase 3 | Explosions and Global Events | 5 | F-14 ~ F-18 | Phase 1 (in practice it needs F-09/F-12/F-13 from Phase 2) | one `FarmstormExplosion` can "destroy per the rules + cap per entity + report the right death cause", 12 explosions in the same tick are spread over 2 ticks, Torchflower survives TNT |
| Phase 4 | Deployment Behavior | 7 | F-19 ~ F-25 | Phase 2, Phase 3 | Blast Carrot (胡罗贝) self-destructs, the cactus emits two rings of fangs, Pumpkin Turret (man瓜) fires, both auras apply effects, the per-chunk and per-player caps take effect |
| Phase 5 | Throwables | 8 | F-26 ~ F-33 | Phase 2, Phase 3 | all 8 throwables can be thrown and produce the impact effects described in `plan0.0.1.txt`, damage stays within the caps, creative mode consumes nothing |
| Phase 6 | Converter | 6 | F-34 ~ F-39 | Phase 2 | one wheat conversion yields Puffed Wheat (膨胀麦) and deducts 3 XP points, the reverse conversion works, insufficient XP is rejected without deducting levels, all 5 converter objects are registered |
| Phase 7 | Edible Type and Lucky Berry | 4 | F-40 ~ F-43 | Phase 3, Phase 6 | Power Beet (甜菜！) grants 4 buffs; all 8 tiers of Lucky Berry (幸运浆果) can be force-triggered; the creative-mode branch leaves no residue after a crash |
| Phase 8 | Data-Driven Content | 5 | F-44 ~ F-48 | Phase 2, Phase 6 | 21 recipes convert both ways, 20 loot tables accept `/loot give`, 4 tags can be tested, 12 advancements unlock, `/reload` never hits a stale recipe |
| Phase 9 | Resources and DataGen | 6 | F-49 ~ F-54 | Phase 4, Phase 6, Phase 8 | `runData` passes in one go, 257 outputs (JSON 169 + TOML 1 + PNG 87), no purple-black blocks in game and no raw lang keys |
| Phase 10 | Acceptance and Release | 5 | F-55 ~ F-59 | Phase 4, Phase 5, Phase 7, Phase 9 | all 18 DoD items checked, each of RT-02/03/05/08/09 has one passing record, `build/libs` holds a releasable jar |
| **Total** | | **59** | F-01 ~ F-59 | | |

Reading notes:

- Phase 3's official prerequisite is only Phase 1, but F-14 depends on F-13, F-17 on F-09, and F-18 on F-12 — all three are in Phase 2. **In practice, feed them in the order 1 → 2 → 3; do not skip Phase 2.**
- Phases 4 / 5 / 6 do not block each other after Phase 3, but this file still lists them in numeric order; reordering becomes meaningful only after F-44 (Phase 8).
- The whole project totals 96.0 person-days (excluding a 25% buffer); about 40 person-days with three people in parallel, about 120 person-days for one person working serially (`03-TASKS.md §13.3`).

---

## 2. Phase 1 · Project Foundation (F-01 ~ F-05)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 2.1 Must Already Be Ready Before Feeding

- The repository contains only `plan0.0.1.txt` and the `docs/zh/00~04` documents, with **no Java source at all** (05 §2.1, item 5).
- This machine can run `./gradlew`, JDK 21 is available, and it can reach the network to fetch ModDevGradle and NeoForge 21.1.213.
- It is confirmed that **no** `*.mixins.json` and no `@Mixin` exist in the repository (HC-04).

### 2.2 Phase Goal

Make an empty mod with nothing registered yet run on all four ends under ModDevGradle, and establish the registration skeleton, the ally check, the static profile, and the config framework firmly in one pass — every later phase only appends entries into them and never changes the shape of these four places again.

### 2.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-01 | Land the build baseline | Get the empty mod through its first successful build / runClient / runServer / runData under MDG | ① `./gradlew build` produces `build/libs/farmstorm-0.0.1+mc1.21.1.jar`; ② the `./gradlew runServer` log contains `Done (` and no `ERROR` line; ③ the `build.gradle` extension block is named `neoForge` (case-sensitive) and `neo_version=21.1.213` contains no wildcard; ④ `./gradlew runData` exits with code 0 |
| F-02 | Main class and registration skeleton | Establish the mod entry point and the 8 `DeferredRegister` skeletons so later registration tasks only append entries | ① the constructor hooks the 8 `REGISTER`s onto the mod bus and calls `container.registerConfig(ModConfig.Type.SERVER, …)`; ② `Farmstorm.id("converter").toString()` equals `farmstorm:converter`; ③ the `runClient` startup log has no `Missing registry` and no registration exception |
| F-03 | Ally check and ownership utilities | Extract "is this the deployer's ally" into an interface + a single implementation, and provide read/write utilities for ownership data | ① after `/team add red`, a same-team player has `isAlly` true, a different-team player false, and the deployer is always true; ② with `teams.untamedFallback=self`, a teamless player is friendly only to itself; ③ ownership stores only the two fields `ownerUuid` + `ownerName`, and `grep -rn "PlayerTeam" team/` hits only local variables inside the implementation class (C-9) |
| F-04 | Static profiles and distance utilities | Centralize the static profiles of the 15 crops and provide Chebyshev and Euclidean distance utilities | ① `CropProfile.ALL` holds exactly 15 entries with IDs identical to `00-DECISIONS.md §4` verbatim; ② `DistanceMetrics.chebyshev(3,1,3)==3`, `euclidean(3,4,0)==5.0` (asserted in the startup log); ③ `DeployedKind` contains 5 enum values |
| F-05 | Config general sections and effect template keys | Land the 7 general groups and 22 groups of effect keys together with the hook that rebuilds the cache after a reload | ① `config/farmstorm-server.toml` contains the seven sections `[converter] [stack] [deploy] [teams] [explosion] [throwable] [food]`; ② total keys in the file = 58 (14 general + 44 effect); ③ after changing `converter.xpCost` to 7 and running `/reload`, `FarmstormConfig.converterXpCost` reads 7 and the log prints one `bake()` line |

### 2.4 Phase Exit Criteria

- [ ] `./gradlew build` produces `farmstorm-0.0.1+mc1.21.1.jar`, `runServer` prints `Done (`, the log has no `ERROR`, and `runData` exits with code 0.
- [ ] `config/farmstorm-server.toml` is generated with exactly **58** keys (this phase requires only 58; the 51 crop-specific keys and the 8 probability keys are appended in Phases 4/5/6/7, and F-56 reviews the final 117).
- [ ] The assertion output of `Farmstorm.id("converter")` appears in the log.
- [ ] All 8 `DeferredRegister`s are registered on the mod bus, and the `registry/` package contains no logic code at all.
- [ ] `git status` shows only `build.gradle`, `gradle.properties`, `settings.gradle`, `gradle/wrapper/`, `src/main/resources/META-INF/neoforge.mods.toml`, `src/main/resources/pack.mcmeta`, and `src/main/java/com/dashpig/farmstorm/**`; the upstream documents and `plan0.0.1.txt` are unchanged.

### 2.5 Common Failure Points

1. **Writing the extension block as `neoforge { }`**: after the error, do not guess over and over — read the plugin's DSL hints first and then write what is true, and put the conclusion in the report (05 §9, item 1); at the same time, changing `neo_version` to `21.1.+` or "upgrading to try" is forbidden (N-14).
2. **Writing business content in F-02 along the way**: F-02 delivers only the skeleton and `Farmstorm.id()`; once you start registering concrete items/blocks, the phase boundary is gone, and the F-06/F-08 acceptance later cannot tell who introduced a defect.
3. **F-05 scattering the 58 keys across several classes**: the keys must be declared at a single point in `FarmstormConfig` (05 §4.4: a value may have only one source).

### 2.6 What to Report to the User on Completion

- The jar path and the conclusion line of `./gradlew build`; the `Done (` line of `runServer`; the exit code of `runData`.
- The key-count command and its result for `config/farmstorm-server.toml` (should be 58), plus the names of the seven sections.
- The assertion output line of `Farmstorm.id()`.
- The list of the 8 `DeferredRegister`s.
- Open questions (especially the conclusion of the §9 item 1 kind, namely "what the extension block is actually called").

---

## 3. Phase 2 · Full Registry Layer (F-06 ~ F-13)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 3.1 Must Already Be Ready Before Feeding

- Every exit criterion of Phase 1 passes: `Farmstorm`, the 8 `DeferredRegister`s, `CropProfile`, `DistanceMetrics`, and `FarmstormConfig` already compile.
- `./gradlew build` is green (it must go green again at the end of every task in this phase).
- You have read the matching registry tables in `02-TECH-SPEC.md §3` (items / blocks / entity_type / block_entity_type / damage_type / data_component_type).

### 3.2 Phase Goal

Land **59 of** the 64 registered objects into the registries (the 5 converter-related ones land in Phase 6) and make them placeable, summonable, and queryable.

### 3.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-06 | Crop blocks and 14 registrations | Instantiate 14 crop blocks from the single `FarmstormCropBlock` and register them, **without** registering BlockItems (D-34) | ① all 14 `/setblock ~ ~ ~ farmstorm:<id>` succeed; ② every block has an `age` property and all four values 0~3 are legal; ③ `melon_slice_bomb` has no crop block (`grep` hits 0); ④ under `/gamerule randomTickSpeed 100`, `age` climbs to 3 within 5 minutes |
| F-07 | Throwable base class and 9 entity types | Implement the `ThrownCropEntity` base class and register 8 throwables + 1 turret missile as EntityTypes | ① `FarmstormEntityTypes` contains exactly 9 `DeferredHolder`s; ② all 9 `/summon farmstorm:<id> ~ ~ ~` succeed without a crash; ③ the parameters are `sized(0.25,0.25)` (`turret_bullet` 0.3), `clientTrackingRange(4)`, `updateInterval(10)`; ④ `submunition` is synced through `SynchedEntityData` and the client log prints the value |
| F-08 | Item system and 22 item registrations | Implement 5 item classes and register 15 crops + 6 seed items + 1 converter item | ① all 22 creative-tab items are present; ② the server startup log prints the `stacksTo` of the 22 items: the 8 throwable ones are 16 and the other 14 are 64 (D-16); ③ right-clicking dirt with the 6 seed items places the matching `_crop` / `_stem` / `_stalk` / `_block` / `_bush` block; ④ right-clicking farmland with the 5 deployable items takes the planting branch (T-02) |
| F-09 | Deployment blocks and 5 registrations | Implement the attach / floating / break rules of deployment blocks and register 5 blocks | ① right-clicking glass / slabs / leaves is rejected without consuming the item; ② `pumpkin_turret` can be placed on the top / bottom / side faces, the other 4 only on the top face; ③ after the supporting block is mined, `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` become `floating=true` and do not fall, while `blast_carrot` / `fang_trap_cactus` vanish and drop themselves; ④ `/setblock ~ ~ ~ farmstorm:pumpkin_turret[face=wall,facing=east,floating=false]` succeeds |
| F-10 | Deployed-object BlockEntity and 5 types | Land the runtime state, persistence, and syncing of deployed objects, and register 5 BlockEntityTypes with the same class | ① the 5 IDs are independent but share one class and one factory; ② `/data get block` can read the 6 synced fields `ownerUuid` `ownerName` `placedGameTime` `detached` `pendingWaves` `fuseTicks` (the TECH §5.3 whitelist) and **cannot read** any team snapshot field (C-9); ③ the fields survive quitting and re-entering the world; ④ `getUpdateTag` contains neither `behavior` nor `cooldown`; ⑤ `getTicker` returns null on the client |
| F-11 | Creative mode tab | Register the custom tab and order it "crops → seed items → tool blocks" | ① `itemGroup.farmstorm.main` displays as “农场风云”; ② the 22 items inside the tab are ordered 15 crops → 6 seed items → converter; ③ `BuildCreativeModeTabContentsEvent` appends only to this tab (other tabs keep their entry counts) |
| F-12 | Data component temporary_enchantments | Register the temporary enchantment ledger component and settle its serialized form | ① the `DataComponentType` registers successfully with the ID `farmstorm:temporary_enchantments`; ② after `stack.set(...)`, the `stack.get(...)` round-trip is identical; ③ the startup log prints true for the equality comparison after `Codec` encode/decode; ④ the component is still there after the item is put into a chest and taken out |
| F-13 | Damage types void_bolt / crop_blast | Provide 2 damage types the data-driven way and fold them into the vanilla tags | ① both `/damage @s 1 farmstorm:void_bolt` and `farmstorm:crop_blast` succeed; ② the death messages display “被虚空撕碎” and “被作物炸飞了” (Chinese display names); ③ `/damage` autocompletion offers both IDs; ④ taking `crop_blast` damage in leather armor with Blast Protection IV is clearly lower than bare-skinned |

### 3.4 Phase Exit Criteria

- [ ] The creative tab shows **22** items in the correct order.
- [ ] All 14 crop blocks and 5 deployment blocks accept `/setblock`; `melon_slice_bomb` has no crop block.
- [ ] All 9 entities accept `/summon` without a crash.
- [ ] **5** of the 6 BlockEntityTypes are registered (the 6th, `farmstorm:converter`, belongs to F-35).
- [ ] 2 damage types work with `/damage`, and the death messages are Chinese display names rather than key names.
- [ ] Count item by item against `03-TASKS.md §14`: this phase should cover items 22 + blocks 20 + entity_type 9 + block_entity_type 5 + damage_type 2 + data_component_type 1 + creative_mode_tab 1 = **59 of the 60** (recipe_type / recipe_serializer / menu_type and the converter's block_entity_type are left to Phase 6); put the counting conclusion in the report.
- [ ] `./gradlew build` is green; `git status` shows only files within this phase's scope under `registry/`, `block/`, `entity/`, `item/`, `blockentity/`, `datagen/` (if involved), and `data/`.

### 3.5 Common Failure Points

1. **`registry/FarmstormBlocks.java` overwritten three times by three writers**: F-06 / F-09 / F-38 all modify this file. Agree that "a task appends only its own segment", count the entries immediately after every append, and state clearly in the report what this round added.
2. **Registering BlockItems for the 14 crop blocks along the way** (violating D-34), or writing the `Properties` literal twice in two places (05 §4.3). Crop blocks have no BlockItem; their planted form is the 6 seed items in F-08.
3. **Writing a team name into `getUpdateTag` or NBT**: violates C-9 and server authority (HC-03). The sync whitelist is exactly the 6 fields of TECH §5.3 (`ownerUuid` / `ownerName` / `placedGameTime` / `detached` / `pendingWaves` / `fuseTicks`), and not one team field is among them.

### 3.6 What to Report to the User on Completion

- The entry count of each registry and the total (against `03-TASKS.md §14`), naming which items are left to Phase 6.
- The `stacksTo` print lines for the 22 items (8×16 / 14×64).
- One `/data get block` read-back sample with the field names listed explicitly (proving there is no team snapshot).
- The verbatim death message of one `/damage`.
- Open questions (especially the actual signature of `SynchedEntityData` in F-07 and the Codec shape in F-12).

## 4. Phase 3 · Explosions and Global Events (F-14 ~ F-18)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 4.1 Must Already Be Ready Before Feeding

- Phase 1 fully passes; **F-09 / F-12 / F-13 of Phase 2 are complete** (they are the hard prerequisites of the three tasks here — miss one and do not open this phase).
- You have read `02-TECH-SPEC.md §5` (server authority) and §8 (performance ceilings), and know that particles and sounds use vanilla packets (C-6).
- The game-bus event class that `FarmstormGameEvents` needs does not exist yet (F-15 creates it in this phase).

### 4.2 Phase Goal

Establish the three-stage "destroy / damage / present" explosion, explosion batching, explosion immunity, the temporary enchantment ledger, and the player event bus first — every later crop that involves an explosion or an aura reuses them, which avoids writing crops first and then going back to change the explosion.

### 4.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-14 | Three-stage explosion and damage capping | Implement `FarmstormExplosion` and `BlastRule`, splitting block destruction, per-entity capped damage, and particles/sounds into three stages | ① an explosion with power 2 / cap 7 deals 0 < damage ≤ 7 to a player in no armor; ② under the `COBBLESTONE_6` rule cobblestone is not destroyed and dirt is; ③ the death cause shows `death.attack.farmstorm.crop_blast` and the kill is attributed to the deployer / thrower; ④ with `power=4, cap=2` a player standing at the center loses only 2 health points |
| F-15 | Event bus and player events | Establish the game-bus event class that carries one unified entry for player tick, login / logout, and explosion events | ① `PlayerTickEvent.Post` prints a heartbeat log once every 20 ticks (switchable); ② `PlayerLoggedInEvent` / `PlayerLoggedOutEvent` each print one line containing the player name and UUID; ③ the event class registers no block entity tick |
| F-16 | Explosion batching queue | Use the internal constant `MAX_EXPLOSIONS_PER_TICK = 8` to defer same-tick overflow explosions to the next tick | ① throw 12 explosions in one tick: the first 8 execute immediately and the last 4 on the next tick, with the log printing `deferred=4`; ② the total number of blocks destroyed matches one-by-one execution; ③ `/debug` shows single-tick time < 5 ms |
| F-17 | Explosion immunity and Detonate interception | Make Torchflower / Pitcher Plant immune to any explosion (vanilla TNT and other mods included) | ① detonate TNT / a creeper / an end crystal once each and both blocks are still there; ② `blast_carrot` at the same position is destroyed; ③ the `ExplosionEvent.Detonate` log prints a removal count from `getAffectedBlocks()` > 0; ④ `getExplosionResistance` returning 3600000F and the tag together form a double safeguard |
| F-18 | Temporary enchantment ledger | Implement the grant / revoke / expiry of `TempEnchantHandler`, and never overwrite the player's real enchantment on revoke | ① grant Fire Aspect I to a wooden sword and the tooltip shows the `tooltip.farmstorm.temporary_enchant` line; ② on expiry the enchantment disappears and the component is removed (`/data get entity @s SelectedItem` has no such key); ③ an original level of 0 restores to no enchantment, and an original level of 2 is not downgraded; ④ a sword put into a chest and taken out is cleaned up on the next tick |

### 4.4 Phase Exit Criteria

- [ ] One `FarmstormExplosion` can be constructed and the three facts "destroy per the rules + per-entity capped damage + correct death cause" hold at the same time (all four assertions of F-14 pass).
- [ ] 12 explosions in the same tick are spread over **2 ticks**, and the log contains `deferred=4`.
- [ ] Torchflower survives TNT / a creeper / an end crystal, and `blast_carrot` at the same position is destroyed.
- [ ] Temporary enchantments can be granted, expire and be revoked, and clean up bypass cases such as "the sword goes into a chest", while never downgrading the player's real enchantment.
- [ ] The whole repository has no custom network packet (`grep` hits 0 for `ClientboundCustomPayloadPacket` and any self-made S2C class, HC-03 / C-6).
- [ ] `./gradlew build` is green; the `runServer` log has no `ERROR`; `git status` is limited to `explosion/`, `event/`, `util/`, `block/`, `data/farmstorm/tags/block/`, and the main class.

### 4.5 Common Failure Points

1. **Writing the damage cap as a "total damage cap"**: it must be **per-entity** (computed independently for each player); otherwise, when one explosion hits several people, the first player eats the whole budget and the rest take zero damage. At the same time, do not write a custom S2C packet to send particles and sounds along the way (C-6 requires vanilla packets).
2. **Relying on `getExplosionResistance` alone for explosion immunity**: other mods and vanilla paths can still remove the block, so it must also be removed from `getAffectedBlocks()` in `ExplosionEvent.Detonate` — both lines at once (RT-08).
3. **F-18 writing the record into playerdata**: violates D-41 and makes the residue and downgrade exploits of RT-03 impossible to close — the record may only hang on the item's `farmstorm:temporary_enchantments` component.

### 4.6 What to Report to the User on Completion

- A log fragment of one three-stage explosion (destruction count / damage per player / death cause line).
- The verbatim log line containing `deferred=4`.
- The removal count of `Detonate` and the survival conclusions for the three explosion sources.
- Evidence for the four steps of temporary enchantments: grant → expire and revoke → original level 2 not downgraded → chest bypass cleaned up.
- Open questions (especially the actual signature of `Explosion#finalizeExplosion` under 21.1.213).

---

## 5. Phase 4 · Deployment Behavior (F-19 ~ F-25)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 5.1 Must Already Be Ready Before Feeding

- F-09 (deployment blocks) and F-10 (deployed-object BlockEntity) of Phase 2 are complete: the 5 blocks can be placed and the 5 BlockEntityTypes can load from disk.
- F-14 (explosion) and F-18 (temporary enchantment) of Phase 3 are complete: Blast Carrot needs the explosion and Torchflower needs the enchantment ledger.
- F-19 is the sole prerequisite of the other 6 tasks in this phase — deliver the framework first, then the 5 behaviors, and finally the rate limiting.

### 5.2 Phase Goal

Turn the 5 deployed objects from "blocks you can place" into "devices that do things": behavior interfaces + 5 implementations + rate limiting.

### 5.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-19 | Behavior family and tick dispatch | Define `DeployedBehavior` / `TrapBehavior` / `TurretBehavior` / `AuraProvider` and their factories, and dispatch ticks to the behavior objects | ① place one of each of the 5 deployed objects and run 600 ticks with no exception; ② 8 deployed objects in the same chunk are spread across different ticks by `stagger` (the log prints ≤ 2 triggers per tick); ③ no scan happens inside the cooldown window (the turret logs target acquisition only once per 60 ticks); ④ the client does not tick |
| F-20 | Blast Carrot trap | After deployment, self-destruct on non-ally players at Chebyshev ≤ 1, with power 2 / cap 7 / Nausea III | ① an enemy player entering Chebyshev 1 triggers it; ② damage per player ≤ 7; ③ **all** players within explosion radius 4 (teammates included) get Nausea III for 60 ticks (D-27); ④ the block disappears after triggering (`/execute if block` returns 0) |
| F-21 | Fang Trap Cactus | After triggering at Euclidean ≤ 3, spawn two waves of 8 + 5 fangs that appear with a delay | ① triggers at Euclidean distance ≤ 3 (4 blocks does not trigger); ② the outer 8 `minecraft:evoker_fangs` appear after 10 ticks and the inner 5 after 4 ticks (count the hitboxes and their order with F3+B); ③ outer radius 2.0, inner radius 1.0; ④ the block disappears once both waves are out |
| F-22 | Pumpkin Turret turret and missile | Implement Chebyshev-5 target acquisition, firing every 60 ticks, and the behavior of the custom missile | ① it fires at 60-tick intervals once a non-ally player is in range; ② a hit deals 1 point of `farmstorm:void_bolt` damage; ③ the knockback direction is the "pumpkin → target" line and no Levitation is applied; ④ it disappears on hitting a block and destroys no blocks; ⑤ ally players never become targets; ⑥ when interception is enabled in config it also fires at non-ally projectiles (C-3) |
| F-23 | Torchflower aura | Allies within radius 20 gain 6 buffs every 100 ticks, and sword holders are granted temporary Fire Aspect | ① the 6 effect values and the 300-tick durations are correct (Strength III / Resistance II / Haste II / Speed I / Water Breathing I / Night Vision I); ② holding any `#minecraft:swords` item grants temporary Fire Aspect I; ③ each application spawns 8 flame particles; ④ neither non-ally players nor creative-mode players can mine it (progress always 0); ⑤ the temporary enchantment is revoked within 100 ticks after leaving the radius |
| F-24 | Pitcher Plant aura | Allies within radius 10 gain 3 buffs every 100 ticks and have all negative effects purged | ① Regeneration II / Saturation II / Absorption I for 300 ticks each; ② every effect in `MobEffectCategory.HARMFUL` on the player is removed (verify with `/effect give @s poison`); ③ 8 heart particles; ④ non-ally players cannot mine it |
| F-25 | Deployment rate limiting and anti-farming | Use 3 internal constants to close off placement farming and per-chunk pile-up | ① the same player's 33rd deployed object is rejected and the item is not consumed; ② the 9th in the same chunk is rejected; ③ "place → break immediately → place again" is rejected within the 5-tick cooldown; ④ a rejection produces no drops at all |

### 5.4 Phase Exit Criteria

- [ ] Blast Carrot self-destructs: triggers at Chebyshev 1, damage capped at 7, Nausea III covers all players within radius 4, and it vanishes on the spot after triggering.
- [ ] The cactus emits two rings of fangs: waves of 8 + 5, delays 10 / 4 ticks, radii 2.0 / 1.0.
- [ ] Pumpkin Turret fires: Chebyshev-5 acquisition, 60-tick interval, 1 point of `void_bolt`, knockback along the connecting line, allies never hit.
- [ ] Both auras apply effects: the 6 / 3 effect values and the 300-tick durations are correct, the particle count is 8, and the temporary enchantment is revoked on leaving.
- [ ] The per-chunk and per-player caps take effect, and a rejected placement consumes no item and leaves no drop (HC-13: these 6 internal constants must not be raised for testing).
- [ ] The mining rule for Torchflower / Pitcher Plant is correct: vanilla speed for allies, always 0 for non-allies and creative mode, immune to any explosion (C-8 / D-37).
- [ ] `./gradlew build` is green; `git status` is limited to `behavior/`, `blockentity/`, `entity/`, `block/`, and `config/` (the phase-specific keys added here).

### 5.5 Common Failure Points

1. **The ally check reading a team snapshot taken at placement**: C-9 requires **resolving in real time** the owner's current team at the trigger instant; writing a snapshot taken at placement makes "deployed before teaming, triggered after teaming" completely wrong. When the owner is offline, look up their last known team in the scoreboard member list.
2. **Applying the 10-second hard mining to Torchflower / Pitcher Plant**: only `pumpkin_turret` is fixed at 10 seconds; for Torchflower / Pitcher Plant allies mine at vanilla speed and non-allies always get progress 0 (C-8 / D-37). Also, do not let each aura pull all entities with `getEntitiesOfClass` every tick — accept only one application per player per tick (RT-06).

### 5.6 What to Report to the User on Completion

- The trigger log of each of the 5 behaviors (who triggered it, how many times, which effect was applied).
- The three counting evidences of rate limiting (the 33rd / the 9th / a replay within 5 ticks).
- One measured conclusion of "deployed before teaming → triggered after teaming" (proving real-time resolution).
- The mining triple: ally speed, always 0 for non-allies, TNT cannot destroy it.
- Open questions (especially the spawn timing of `evoker_fangs` and the de-duplication rule of `AuraProvider`).

---

## 6. Phase 5 · Throwables (F-26 ~ F-33)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 6.1 Must Already Be Ready Before Feeding

- F-07 (`ThrownCropEntity` + 9 entity types) and F-08 (the 8 throwable items) of Phase 2 are complete.
- F-14 (three-stage explosion and capping) of Phase 3 is complete: all 5 throwables in this phase use it.
- F-27 must come before F-29 (Melon Grenade reuses `MelonSliceProjectile`).

### 6.2 Phase Goal

Implement the impact effects of the 8 throwable crops one by one, and unify the lifecycle, the impact branching, and the creative-mode rule.

### 6.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-26 | Puffed Wheat | Replace a 3×3×3 volume at the landing point with hay blocks; with water it expands to 5×5×5 | ① a dry landing point replaces 27 blocks with `minecraft:hay_block`; ② after one water block is placed in range, 125 blocks are replaced; ③ bedrock / barrier / command block are not replaced (T-05 / D-38); ④ air blocks are replaced |
| F-27 | Ubi Server and Melon Slice Bomb | Implement the two obstacle-breaking / damage throwables and their different resistance thresholds | ① Ubi Server (育碧服务器) has power 4, ≤ 2 damage per player, and can destroy stone; ② Melon Slice Bomb (瓜片子) has power 2 and ≤ 6 damage; ③ cobblestone (6.0) is not destroyed and dirt is (D-19 / C-2) |
| F-28 | Toxic Ubi Server | Spawn a cloud of strong poison at the spot after the explosion | ① explode first (power 2 / damage ≤ 2); ② then spawn an `AreaEffectCloud` with radius 3.0 lasting 600 ticks; ③ players inside the cloud get Poison; ④ the cloud disperses after 60 seconds |
| F-29 | Melon Grenade Cluster Bomb | Spawn 4 melon slices one block above the landing point and then immediately produce a power-3 explosion | ① spawn 4 `melon_slice_bomb` at `pos + (Δx, +1.0, Δz)`, Δ ∈ [-0.3, 0.3]; ② `/data get entity` reads `submunition=true`; ③ a power-3 / damage ≤ 8 explosion happens immediately; ④ each of the 4 submunitions explodes once more when it lands, for 5 explosions in total |
| F-30 | Bamboo Firecracker | Survive 140 ticks after impact, spawning and detonating one wind charge every 5 ticks | ① it does not disappear on impact, has `noPhysics=true` and zero velocity; ② every 5 ticks it spawns a `minecraft:wind_charge` and detonates it immediately, 28 times in total; ③ it disappears after 140 ticks; ④ wind charges destroy no blocks |
| F-31 | Red Mushroom / Brown Mushroom throwables | Implement two AoE buff throwables whose area effect does not filter friend from foe | ① Red Mushroom (红菇): all players within radius 4 (teammates included) get Nausea V for 60 ticks + Blindness I for 60 ticks; ② Brown Mushroom (棕菇): Weakness II for 200 ticks + Slowness II for 100 ticks; ③ neither destroys blocks nor deals damage |
| F-32 | Lifecycle and impact branching | Unify the survival cap and the post-impact behavior of throwables, locking the shared part into the base class | ① surviving longer than `throwable.lifetimeTicks` (default 100) triggers an automatic `discard`; ② `onHitBlock` / `onHitEntity` are implemented only in the base class (`grep -c "onHitBlock" entity/thrown/` returns 0); ③ it disappears immediately on impact (Bamboo Firecracker being the exception via `keepAliveAfterImpact()`) |
| F-33 | Creative-mode consumption rule | Unify "creative mode consumes no items and is not locked by traps" | ① the item count is unchanged after a creative-mode throw; ② the item count is unchanged after a creative-mode deployment; ③ a creative-mode player standing next to a trap does not trigger it; ④ mining `pumpkin_turret` in creative mode still takes 10 seconds |

### 6.4 Phase Exit Criteria

- [ ] All 8 throwables can be thrown, and their impact effects match `plan0.0.1.txt` item by item (lines 33~156 of `plan0.0.1.txt` are the only original text).
- [ ] Every damage cap holds: Ubi Server ≤ 2, Melon Slice Bomb ≤ 6, Melon Grenade ≤ 8, Blast Carrot ≤ 7.
- [ ] The resistance threshold is unified as "≥ cobblestone 6.0 is not destroyed" (D-19 / C-2), the thresholds are read from `explosion.sliceResistThreshold` / `explosion.grenadeResistThreshold`, and `6.0F` is not hard-coded (RT-09).
- [ ] Impact branching exists only in the base class: no subclass under `entity/thrown/` implements `onHitBlock` / `onHitEntity` itself.
- [ ] Creative-mode throwing / deployment both consume no items, and creative-mode players are not locked by traps.
- [ ] Throwables never pile up forever because "discard was forgotten": the 100-tick cap takes effect.
- [ ] `./gradlew build` is green; `git status` is limited to `entity/thrown/`, `entity/ThrownCropEntity.java`, `item/`, `behavior/impl/BlastCarrotTrap.java`, and `config/`.

### 6.5 Common Failure Points

1. **Copying the original's "≥ -1" for the Melon Slice Bomb threshold**: that is a typo; the correct rule is "≥ cobblestone 6.0" (C-2 / D-19), and it must share the same threshold tier as Melon Grenade.
2. **Letting impact logic scatter into the subclasses**: the acceptance of F-32 is reverse evidence — the `grep` result is 0. A subclass writes only "what to do on impact", never "whether to disappear on impact".
3. **Treating Bamboo Firecracker as an ordinary throwable**: after impact it must survive 140 ticks with `noPhysics=true` and spawn one vanilla `wind_charge` every 5 ticks, 28 times in total; using the wrong entity or building the explosion yourself both break the "destroy no blocks" criterion.

### 6.6 What to Report to the User on Completion

- One piece of impact-effect evidence per throwable, for all 8 (block changes / damage numbers / effect lists).
- The measured numbers for the 4 damage caps.
- The result line of `grep -c "onHitBlock" entity/thrown/`.
- The before/after item counts for creative-mode throwing and deployment.
- Open questions (especially the parameter differences between `AreaEffectCloud` and the vanilla lingering potion).

---

## 7. Phase 6 · Converter (F-34 ~ F-39)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 7.1 Must Already Be Ready Before Feeding

- F-05 (the `[converter]` config section) and F-08 (the `farmstorm:converter` item) of Phase 2 are complete.
- The item IDs of the 15 crops and 6 seed items are settled (DEC §4) — the recipes must be written against these IDs.
- The converter is the only block in the whole project that has a GUI; client classes may appear only in the `client/` package (HC-03 / 05 §4.7, N-12).

### 7.2 Phase Goal

Build the complete closed-loop "vanilla ↔ improved" block: recipe type, container, menu, screen, and transactional conversion. At the end of this phase all 64 registered objects should be registered.

### 7.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-34 | Recipe type and serializer | Define `ConverterRecipe` and its serializer, and register 1 RecipeType + 1 Serializer | ① `farmstorm:converting` has exactly 1 RecipeType and 1 Serializer; ② after adding a `data/farmstorm/recipe/puffed_wheat.json`, `runServer` shows no `Parsing error`; ③ `reverse()` returns a view with input / result swapped and writes nothing to disk |
| F-35 | Converter container and persistence | Implement a single-slot container BlockEntity with content persistence and break-on-drop | ① insert / extract / `stillValid` behave correctly; ② the slot keeps its item after quitting and re-entering; ③ breaking the block drops the contents and never swallows items |
| F-36 | Converter menu | Implement a menu with 1 input + 1 read-only preview + 3 `ContainerData`, and register the menu type | ① the `farmstorm:converter` menu type registers successfully; ② `clickMenuButton(player, 0)` accepts only 0; ③ the `ContainerData` entries `xpCost` / `canConvert` / `reverse` change with the input item; ④ `quickMoveStack` handles only the input slot and the player inventory |
| F-37 | Converter screen and client registration | Implement the GUI background / title / button, and register the screen through a client event | ① opening the converter under `runClient` shows a 176×166 background and the title “农产品转换器” (Produce Converter); ② clicking the convert button emits `ServerboundContainerButtonClickPacket` (visible in the log, with no custom packet); ③ closing and reopening 10 times does not crash |
| F-38 | Converter block and break drops | Register the converter block and hook up the menu provider and the break drops | ① the `farmstorm:converter` block ID registers successfully (the 20th block); ② it has a 4-way `facing` and `getRenderShape` is `MODEL`; ③ mining it drops the block itself + the contents; ④ `#minecraft:mineable/pickaxe` takes effect (a pickaxe is faster than an empty hand) |
| F-39 | Conversion transaction | Implement `tryConvert` as "validate everything first, then touch state", covering XP, catalysts, and both directions | ① forward and reverse 1:1 conversion both succeed; ② insufficient XP is rejected and **no levels are deducted**; ③ Torchflower / Pitcher Plant are forward-only, cost 1 nether star, and the reverse is free; ④ when validation fails, 10 rapid clicks change neither items nor XP |

### 7.4 Phase Exit Criteria

- [ ] Putting in wheat and converting once yields Puffed Wheat, with **3 XP points** deducted (points, not levels, D-12).
- [ ] The reverse conversion works back, so the conversion is 1:1 and reversible both ways.
- [ ] Insufficient XP is rejected and **never deducts levels**; when validation fails, 10 rapid clicks change neither items nor XP.
- [ ] Torchflower / Pitcher Plant run only in the "vanilla → awakened" direction, cost 1 extra nether star each time, and the reverse is free.
- [ ] All 5 converter registration objects (`recipe_type` / `recipe_serializer` / `menu_type` / the `converter` block / the `converter` block_entity_type) are registered — at this point it is 64 / 64.
- [ ] The GUI button goes through the vanilla `ServerboundContainerButtonClickPacket`, and the repository still has 0 custom network packets.
- [ ] `./gradlew build` is green; `runClient` can open the screen without crashing; `git status` is limited to `recipe/`, `blockentity/`, `menu/`, `client/`, `block/`, `registry/`, `config/`.

### 7.5 Common Failure Points

1. **Writing the transaction order as "deduct materials first, validate later"**: this is the sole source of item-duplication exploits. `tryConvert` must "validate everything first (input convertible / XP sufficient / catalyst sufficient / output fits) → then touch state"; any step that deducts early is a defect (`04-ROADMAP.md` already calls this out).
2. **Deducting XP as levels**: D-12 deducts **3 XP points** per conversion, rejects when there is not enough, and **never deducts levels** — writing `addExperienceLevels(-1)` directly violates the adjudication.
3. **Forgetting the cache after `/reload`**: this phase builds the cache and only F-48 in Phase 8 does the full regression; but the cache must carry the `RecipeManager#byKey` validation from day one (RT-07), otherwise Phase 8 means rework.

### 7.6 What to Report to the User on Completion

- The before/after XP values and item values for one forward conversion and one reverse conversion.
- The rejection log for insufficient XP (including the evidence that no levels were deducted: level and XP identical before and after).
- The evidence for the catalyst cost of a forward Torchflower conversion (1 nether star deducted) and the evidence that the reverse is free.
- The conclusion of 10 rapid clicks producing no change.
- The 64 / 64 counting conclusion.
- Open questions (especially the actual signature of `RecipeSerializer#streamCodec`).

## 8. Phase 7 · Edible Type and Lucky Berry (F-40 ~ F-43)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 8.1 Must Already Be Ready Before Feeding

- F-14 (explosion) and F-15 (player events) of Phase 3 are complete; the F-05 config section of Phase 6 can already hot-reload.
- F-08 of Phase 2 has delivered the two items `power_beet` and `lucky_berry` (this phase adds behavior only, not registration).
- The 8 probability keys in `config` have not landed yet — F-41 in this phase is responsible for adding them to the 117-key ledger.

### 8.2 Phase Goal

Complete the two edible crops, with the focus on the **8-tier probability table** and the **three-level fallback of the creative-mode branch** — these two places are the only ones in the whole project that touch a player's game mode.

### 8.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-40 | Power Beet eating effect | Apply 4 buffs of 600 ticks each in one go on eating | ① 32 ticks after eating you have Strength II / Speed II / Haste II / Resistance I for 600 ticks each; ② nutrition 4, saturation 0.3 (`/data get` reads the `food` component); ③ it can still be eaten with a full hunger bar |
| F-41 | Probability table and drawing | Implement the per-mille weight table and a single `nextInt(1000)` draw, rebuilt on reload | ① the internal weight sum is always 1000 (asserted in the startup log); ② setting each of the 8 config entries to 100 / 0 / … in turn and running 100 draws each gives a 100% hit rate; ③ when the config sum ≠ 100, `bake()` rejects that section, falls back to the defaults, and prints a warn |
| F-42 | The 6 regular Lucky Berry branches | Implement the 6 outcomes: lightning, random crops, regeneration, treasure chests, a small explosion, and a charged creeper | ① lightning strikes at the player's position and deals damage with no source; ② spawn 16 random crops (14 kinds, self excluded, D-17) landing on standable blocks within a radius of 2 (D-33); ③ Regeneration V for 100 ticks; ④ 2 `end_city_treasure` chests; ⑤ a power-1 small explosion with no cap; ⑥ a creeper with `powered=true` that is not ignited |
| F-43 | Creative-mode branch and three-level fallback | Implement the 0.1% branch: 90-second rollback, restore on logout, and login reconciliation | ① after drawing it the player switches to creative and is restored automatically after 1800 ticks; ② logging out midway restores immediately and writes nothing to the save; ③ after `kill -9` on the server and reconnecting, a non-OP player is restored to survival and an OP player is left untouched (RT-05); ④ a player already in creative who draws this branch only gets the sound effect and no mode change |

### 8.4 Phase Exit Criteria

- [ ] Power Beet grants 4 buffs of 600 ticks each on eating, nutrition 4 / saturation 0.3, and it can still be eaten with a full hunger bar.
- [ ] All 8 Lucky Berry tiers **can be force-triggered** (set each tier's probability to 100 in turn and draw 100 times, hitting 100%).
- [ ] The internal weight sum is always 1000; when the config sum ≠ 100, `bake()` rejects it, falls back to the defaults, and prints a warn — it never swallows it silently.
- [ ] The creative-mode branch restores after 1800 ticks, restores immediately on logout, and is rescued by reconciliation after `kill -9`; and it **never writes to the save** (RT-05).
- [ ] The whole project still has 0 custom network packets; sounds and particles use vanilla packets (C-6).
- [ ] `./gradlew build` is green; `git status` is limited to `util/`, `item/`, `event/`, `config/`.

### 8.5 Common Failure Points

1. **Creative-mode state landing in playerdata**: RT-05 is the highest-severity gameplay exploit. The record goes only into an in-memory `Map<UUID, CreativeRestore>`, `PlayerLoggedOutEvent` restores immediately and `PlayerLoggedInEvent` reconciles; the "accidental downgrade" protection runs only for non-OP players. Any "just store it in the save, it is more stable" approach breaks the three-level fallback.
2. **Using floating-point accumulation or a separate `nextDouble()` per tier**: it must be a single `nextInt(1000)` hitting exactly one tier, with the weight sum always 1000; otherwise the "100% hit rate over 8 tiers" acceptance can never pass.
3. **Counting Lucky Berry itself in the random crop pool**: D-17 states the pool is the **14 kinds** other than itself; a wrong count also breaks F-42's "spawn 16" criterion.

### 8.6 What to Report to the User on Completion

- Power Beet's 4 effects and their durations, plus the `food` component read-back line.
- The result table of force-triggering all 8 tiers (the hit rate of 100 draws per tier).
- The verbatim warn log of one `bake()` fallback.
- Three pieces of evidence for the creative-mode branch: restore after 1800 ticks / restore on logout / reconciliation after `kill -9`, and state how "no write to the save" was checked.
- Open questions (especially the signature of `FoodProperties.Builder` under 21.1.213).

---

## 9. Phase 8 · Data-Driven Content (F-44 ~ F-48)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 9.1 Must Already Be Ready Before Feeding

- F-34 (`ConverterRecipe`) of Phase 6 is complete: without a recipe type, the 21 JSONs have nowhere to go.
- F-06 / F-09 of Phase 2 are complete: the loot tables hang on the 14 crop blocks and the 5 deployment blocks.
- Phase 8 consists entirely of **data file** tasks: whatever DataGen can generate goes through a Provider; whatever it cannot (recipes, the loot table bodies) may be hand-written JSON and still counts as data, but it **must not** contend with DataGen for the same file (05 §4.7).

### 9.2 Phase Goal

Complete the purely data content — recipes, loot tables, tags, advancements — so that the gameplay loop holds at the data layer. By the end of this phase the game is already "playable" and only resources and acceptance remain.

### 9.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-44 | 21 conversion recipes | Write one conversion recipe for each of the 15 crops and 6 seed items | ① `runServer` shows no recipe parsing error; ② all 15 vanilla crops + 6 seeds are convertible in the converter; ③ 15 are reversible, while the 2 for Torchflower / Pitcher Plant are not; ④ the above still holds after `/reload` |
| F-45 | 20 loot tables | Write loot tables for the 14 crop blocks, the 5 deployment blocks, and the 1 converter block | ① when mature (`age=3`), crop and seed item both drop at the vanilla ratio, while immature plants drop only 1 seed item; ② the 5 deployment tables drop the block itself by default and 0 when destroyed by an explosion; ③ `/loot give @s loot farmstorm:blocks/puffed_wheat_crop` executes per table with no error |
| F-46 | 4 block / item tags | Provide the deployed-object set, the improved-crop set, the seed-item set, and the pickaxe mining tag | ① the four tags hold 5 / 15 / 6 / 6 entries; ② `/execute if block` returns 1 for all 5 deployment blocks (`#farmstorm:deployed_crops`); ③ `#farmstorm:modified_crops` holds exactly 15 entries and no seed items |
| F-47 | 12 advancements | Deliver the 12 advancements per `01-DESIGN.md §6`, including 1 hidden one | ① all 12, including `/advancement grant @s only farmstorm:root`, succeed; ② each trigger condition unlocks; ③ `lucky_jackpot` is `hidden: true`; ④ all are `frame=task`, `announce_to_chat=false`; ⑤ the `root` background is `minecraft:textures/block/hay_block_side.png` |
| F-48 | Recipe cache and reload regression | Make the converter stop hitting stale recipes after `/reload` | ① after `/reload`, 5 conversions in a row succeed with no `Recipe not found` in the log; ② delete one recipe JSON and run `/reload`, and the matching item immediately becomes unconvertible; ③ restore the JSON and it converts again |

### 9.4 Phase Exit Criteria

- [ ] The 21 recipes convert both ways; the 2 for Torchflower / Pitcher Plant do not reverse.
- [ ] The 20 loot tables each accept `/loot give`, and the mature and immature drops follow the vanilla ratios.
- [ ] The 4 tags work, with entry counts 5 / 15 / 6 / 6.
- [ ] All **12 advancements** unlock (including 1 `hidden`), at the path `data/farmstorm/advancement/*.json` (C-7: not 6).
- [ ] After `/reload` the converter hits no stale recipe: delete the table and it stops working, restore it and it works again (RT-07 closed).
- [ ] `./gradlew build` is green; `git status` is limited to `data/` plus `blockentity/` and `event/` (F-48's cache validation).

### 9.5 Common Failure Points

1. **Writing only 6 advancements**: the old wording in `02-TECH-SPEC.md §6.7` is overruled by C-7; the correct number is **12** (the 24 lang keys follow from it, and F-52's 216 keys depend on it). Writing 6 fewer also breaks F-53's 257 and F-52's 216.
2. **Getting the loot table path wrong**: it must be `data/farmstorm/loot_table/blocks/*.json` with the table ID `farmstorm:blocks/<block_id>`; with a wrong path `/loot give` fails silently and you only find out at acceptance.
3. **F-48 closing the issue by "restarting the server"**: the cache must carry the `RecipeManager#byKey` validation and listen to `RecipesUpdatedEvent` to clear itself; `/reload` must be correct on the spot.

### 9.6 What to Report to the User on Completion

- The commands and results of the four counts 21 / 20 / 4 / 12.
- The verbatim response of one `/loot give` and one `/advancement grant`.
- The conversion conclusion before and after `/reload` (including the controlled experiment "delete the table and it stops working").
- Open questions (especially how the advancement JSON `requirements` structure is written under 1.21.1).

---

## 10. Phase 9 · Resources and DataGen (F-49 ~ F-54)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 10.1 Must Already Be Ready Before Feeding

- F-22 of Phase 4 is complete (F-54 must write a renderer for the missile).
- F-38 of Phase 6 is complete (F-49 must generate the converter's blockstate and models).
- F-44 / F-45 / F-46 / F-47 of Phase 8 are complete (F-53 must hook the 7 providers onto `GatherDataEvent`).
- `tools/gen_textures.py` does not exist yet (F-51 creates it).

### 10.2 Phase Goal

Fill in the 20 blockstates, 62 block models, 22 item models, 87 textures, 216 language keys, and client rendering in one pass, with **everything generated by DataGen / scripts**.

### 10.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-49 | Blockstates and block models | Generate 20 blockstates and 62 block models with DataGen | ① `runData` generates 20 `blockstates/*.json` and 62 `models/block/*.json`; ② each of the 14 crops has all 4 stage variants (`age` 0 ~ 3); ③ the blockstates of the 5 deployment blocks cover `face × facing × floating` and the floating variant reuses the same model |
| F-50 | Item models | Generate 22 item models with DataGen | ① 22 `models/item/*.json`; ② the 5 deployable item models point their `parent` at `farmstorm:block/<id>`; ③ none of the 22 creative-tab icons is a purple-black block |
| F-51 | Texture generation script | Produce the 87 placeholder PNGs in bulk with a script, and add a green plus in the bottom-right corner of the 15 crop icons | ① one run produces 22 `textures/item`, 64 `textures/block`, and 1 `textures/gui`; ② the 15 crop item icons have a green plus in the bottom-right corner (visual check per image + a 16×16 size assertion); ③ repeated runs are idempotent (identical md5 across two runs) |
| F-52 | Language files, 216 keys | Generate language files whose key sets are identical across Chinese and English | ① the key set diff between `zh_cn.json` and `en_us.json` is empty; ② the key count is 216 (22 item + 20 block + 9 entity + 1 itemGroup + 1 container + 8 gui + 10 tooltip + 4 death + 24 advancement + 117 config); ③ the Chinese names match `plan0.0.1.txt` verbatim (full-width punctuation included); ④ no `item.farmstorm.*` key displays raw in game |
| F-53 | DataGen assembly and output verification | Hook the 7 providers onto `GatherDataEvent` and verify the total output count and its breakdown | ① `./gradlew runData` generates the full set of outputs under `src/generated/resources` in one go; ② the total is 257 (JSON 169 + TOML 1 + PNG 87), which is +6 over the old figure 251; ③ there is no trace of hand edits to `src/generated` (a second run produces an empty diff) |
| F-54 | Turret missile rendering | Make the missile visible on the client, reusing the shulker bullet model | ① the client can see the missile and its shape matches the shulker bullet; ② its orientation follows its velocity; ③ `runServer` loads no client rendering class (no `NoClassDefFoundError`) |

### 10.4 Phase Exit Criteria

- [ ] `./gradlew runData` passes in one go, the total output is **257** (JSON 169 + TOML 1 + PNG 87), and **running it again produces an empty diff**.
- [ ] The per-class counts match `02-TECH-SPEC.md §6.1`: blockstates 20 / models-block 62 / models-item 22 / lang 2 / textures-item 22 / textures-block 64 / textures-gui 1 / damage_type 2 / minecraft tags-damage_type 2 / recipe 21 / loot_table-blocks 20 / advancement **12** / farmstorm tags-block 2 / farmstorm tags-item 2 / minecraft tags-block-mineable-pickaxe 1 / root 2.
- [ ] **216** language keys, with an empty diff between the Chinese and English key sets; no key displays raw in game.
- [ ] The 15 crop icons have a green plus in the bottom-right corner; there are no purple-black blocks in the creative tab or in the world.
- [ ] The script is idempotent: two consecutive runs produce identical md5.
- [ ] `runServer` does not crash because of a client class (no `NoClassDefFoundError`, HC-03 / 05 §4.7, N-12).
- [ ] `./gradlew build` is green; `git status` is limited to `datagen/`, `client/`, `tools/`, `src/generated/`.

### 10.5 Common Failure Points

1. **Hand-editing the JSON generated under `src/generated/resources`**: the next `runData` overwrites it, so F-53's "running it again produces an empty diff" simply fails. All generated output belongs to a Provider — to change it, change the Provider.
2. **Checking against the old figures 251 / 204**: 257 and 216 are the latest adjudication (C-7); the difference is exactly those 6 advancement JSONs and 12 text keys. The check script must count class by class over this section's 15 classes, not just look at the total.
3. **Client classes leaking into the server path**: `TurretBulletRenderer` and `ConverterScreen` may appear only in the `client/` package and are registered through client events; otherwise the dedicated server will not start (05 §4.7, N-12).

### 10.6 What to Report to the User on Completion

- The output lines of `runData` + the 15-class per-class count + the verified total of 257.
- An empty `git diff --stat` after a second `runData`.
- The conclusion of an empty 216-key diff and the key count.
- The visual conclusion for the 15 crop icons (or one contact sheet).
- The `runServer` log line showing no client class loading error.
- Open questions (especially the provider signatures of `ParticleProvider` / BlockState variants under 1.21.1).

---

## 11. Phase 10 · Acceptance and Release (F-55 ~ F-59)

> Feed method: the full text of `05-PROMPT-MAIN.md` + this section; feed one phase at a time.

### 11.1 Must Already Be Ready Before Feeding

- Phases 4, 5, 7, and 9 all pass (each of the four acceptance lines in this phase depends on one of them).
- A dedicated server environment you can restart is available (needed for the `kill -9` regression and the multiplayer scenarios; `04-ROADMAP.md` has scheduled it before M5).
- The DoD-01 ~ DoD-18 checklist from `04-ROADMAP.md §6` is ready.

### 11.2 Phase Goal

Run all 9 technical risks and 18 DoD items once each and produce a releasable v0.0.1 — **this phase adds no feature code**; only fixes for acceptance defects are allowed.

### 11.3 Task List

| ID | Title | One-line description | Observable acceptance criteria |
|---|---|---|---|
| F-55 | Dedicated server and three-mode regression | Verify server authority and the stability of the hard-mining progress | ① `runServer` starts standalone to `Done` in ≤ 120 s; ② with two clients connected, traps / turrets / auras all take effect; ③ mining `pumpkin_turret` once each in creative / survival / spectator, the progress bar does not bounce back over 10 seconds (RT-02); ④ it stays stable after `/tp`-ing far away and coming back to trigger a chunk reload |
| F-56 | Numeric cross-check acceptance | Check the 117 config keys against the spec item by item | ① exporting the toml's 117 keys and comparing item by item with `02-TECH-SPEC.md §4.11`, difference 0; ② changing 5 randomly picked keys changes behavior accordingly; ③ the check that the 8 probabilities always sum to 100 rejects and reverts a wrong edit |
| F-57 | Performance acceptance | Verify the worst case of aura stacking and deployment density | ① in a scenario of 10 auras + 8 players, `/debug profile` shows single-tick < 1 ms (RT-06); ② 8 deployed objects in one chunk hold steady with no tick spike; ③ 12 throwable impacts in the same tick cause no stutter (batching works) |
| F-58 | Exceptional paths and crash regression | Run each verification method of RT-03 / RT-05 / RT-08 / RT-09 once | ① after each of the four scenarios — lava / chest / death drop / logout — the item component is cleared; ② after `kill -9` and reconnecting, the game mode is correct; ③ detonate TNT / a creeper / an end crystal once each and Torchflower is still there; ④ one block with resistance 6.0 and one with 5.99, the former not destroyed and the latter destroyed |
| F-59 | Packaging and v0.0.1 release | Produce a releasable jar and complete the DoD sign-off | ① the `./gradlew build` output is named `farmstorm-0.0.1+mc1.21.1.jar`; ② `META-INF/neoforge.mods.toml` inside the jar resolves to version 0.0.1; ③ the repository has no mixin configuration and no placeholder markers (`grep` hits 0); ④ all 18 DoD items are ticked one by one |

### 11.4 Phase Exit Criteria

- [ ] All **18 DoD items** in `04-ROADMAP.md` are ticked, each with command output or a screenshot location.
- [ ] **RT-02 / RT-03 / RT-05 / RT-08 / RT-09** in the risk register each have one passing record.
- [ ] Each of the four acceptance lines has a record: server regression (F-55), numeric cross-check (F-56), performance (F-57), exceptions and crashes (F-58).
- [ ] The 117 config keys are compared **item by item** with difference 0 (not a spot check).
- [ ] `build/libs/farmstorm-0.0.1+mc1.21.1.jar` is produced and the version inside the jar resolves to 0.0.1.
- [ ] Repository-wide `grep` hits 0: no mixin configuration, no placeholder markers other than `TODO(api-unverified)`, no commented-out code, no `System.out.println` (05 §6 item 4, N-11).
- [ ] `git status` is clean; the Phase numbers / task counts / workload figures in `03-TASKS.md` agree with each other (DoD-18).

### 11.5 Common Failure Points

1. **Using "it looks normal" as evidence**: `03-TASKS.md §0.3` forbids expressions such as "behaves normally" and "feels good". Every acceptance item must land on a log line, a `/execute` / `/data get` response, an F3 reading, or file content.
2. **Making the config comparison a spot check**: F-56 requires all 117 keys compared with difference 0; sampling 5 keys is the method for item ② "changing a value changes behavior" and cannot replace item ①.
3. **Doing only half of the `kill -9` regression**: the RT-05 reconciliation must cover both sides — "a non-OP player is restored to survival" and "an OP player is left untouched"; doing only one side misses the accidental-downgrade protection.

### 11.6 What to Report to the User on Completion

- The 18-item DoD checklist (one line each: DoD ID / conclusion / evidence location).
- The passing record for each of the 5 risks (RT-02/03/05/08/09).
- The record paths of the four acceptance lines.
- The jar path and the version resolution line inside the jar.
- The four cleanup conclusions where `grep` hits 0.
- The list of open questions that are still unresolved (this one decides whether release can be signed).

---

## 12. Verbal Hand-off Between Phases (hand-off)

Template (replace the square brackets with actual content; send only one at a time):

~~~text
The previous phase (Phase N · [title]) has passed acceptance against the exit criteria of 06-PROMPT-TASKS.md: [one sentence of evidence, for example "build is green, all 22 items are present, 5 BlockEntityTypes are registered"].
This phase begins: Phase N+1 · [title].
Execute strictly the six items of the "Phase N+1" section of 06-PROMPT-TASKS.md: first confirm the "must already be ready before feeding" list item by item and report, then touch the first line of code; do only the F-xx entries in that section's task list, do not implement the next phase in advance, and report per 05-PROMPT-MAIN.md §7 when done.
~~~

Nine ready-to-paste instances:

| Hand-off | Paste-ready sentence |
|---|---|
| P1 → P2 | The previous phase (Phase 1 · Project Foundation) is complete: `build` produces a jar, `runServer` prints `Done`, and `config/farmstorm-server.toml` has all 58 keys. This phase begins Phase 2 · Full Registry Layer; execute the "Phase 2" section of 06-PROMPT-TASKS.md, confirm readiness before touching code, and report per 05 §7 when done. |
| P2 → P3 | The previous phase (Phase 2 · Full Registry Layer) is complete: 22 items / 19 blocks / 9 entities / 5 BE types are all landed and the damage types work. This phase begins Phase 3 · Explosions and Global Events; execute the "Phase 3" section of 06-PROMPT-TASKS.md and report per 05 §7 when done. |
| P3 → P4 | The previous phase (Phase 3 · Explosions and Global Events) is complete: the three-stage explosion works, `deferred=4` takes effect, and temporary enchantments can be granted and revoked. This phase begins Phase 4 · Deployment Behavior; execute the "Phase 4" section of 06-PROMPT-TASKS.md and report per 05 §7 when done. |
| P4 → P5 | The previous phase (Phase 4 · Deployment Behavior) is complete: all 5 deployed-object behaviors take effect and the three rate-limit caps hold. This phase begins Phase 5 · Throwables; execute the "Phase 5" section of 06-PROMPT-TASKS.md and report per 05 §7 when done. |
| P5 → P6 | The previous phase (Phase 5 · Throwables) is complete: all 8 throwables can be thrown and the damage caps and resistance thresholds all line up. This phase begins Phase 6 · Converter; execute the "Phase 6" section of 06-PROMPT-TASKS.md and report per 05 §7 when done. |
| P6 → P7 | The previous phase (Phase 6 · Converter) is complete: two-way conversion, XP point deduction, and the catalyst rules all hold, and 64 / 64 registered objects are registered. This phase begins Phase 7 · Edible Type and Lucky Berry; execute the "Phase 7" section of 06-PROMPT-TASKS.md and report per 05 §7 when done. |
| P7 → P8 | The previous phase (Phase 7 · Edible Type and Lucky Berry) is complete: Power Beet's 4 buffs, all 8 probability tiers force-triggerable, and the creative-mode three-level fallback holds. This phase begins Phase 8 · Data-Driven Content; execute the "Phase 8" section of 06-PROMPT-TASKS.md and report per 05 §7 when done. |
| P8 → P9 | The previous phase (Phase 8 · Data-Driven Content) is complete: 21 recipes / 20 loot tables / 4 tags / 12 advancements are all verifiable and the `/reload` regression passes. This phase begins Phase 9 · Resources and DataGen; execute the "Phase 9" section of 06-PROMPT-TASKS.md and report per 05 §7 when done. |
| P9 → P10 | The previous phase (Phase 9 · Resources and DataGen) is complete: `runData` produces 257 files in one go, the 216 keys match across Chinese and English, and there are no purple-black blocks. This phase begins Phase 10 · Acceptance and Release; execute the "Phase 10" section of 06-PROMPT-TASKS.md and report per 05 §7 when done. |

---

## 13. Whole-Project Wrap-Up Prompt (feed after Phase 10 passes)

~~~text
Project wrap-up. Perform one complete delivery check of farmstorm v0.0.1: do checks and fixes only, add no features.

1. Build
   Run ./gradlew clean build and confirm the artifact is build/libs/farmstorm-0.0.1+mc1.21.1.jar;
   unpack it and confirm that META-INF/neoforge.mods.toml resolves the version to 0.0.1 and the modId is farmstorm.

2. Resource inventory (you must count class by class; reporting only the total is invalid)
   Re-run ./gradlew runData, then count the following 16 classes one by one; the total must be 257 (JSON 169 + TOML 1 + PNG 87):
   blockstates 20 / models/block 62 / models/item 22 / lang 2 / textures/item 22 / textures/block 64 /
   textures/gui 1 / data/farmstorm/damage_type 2 / data/minecraft/tags/damage_type 2 /
   data/farmstorm/recipe 21 / data/farmstorm/loot_table/blocks 20 / data/farmstorm/advancement 12 /
   data/farmstorm/tags/block 2 / data/farmstorm/tags/item 2 /
   data/minecraft/tags/block/mineable/pickaxe 1 / root files pack.mcmeta + META-INF/neoforge.mods.toml 2.
   Run runData once more; git diff --stat must be empty (no trace of hand edits to generated output).
   Also verify the language file's 216 keys and that the zh_cn and en_us key set diff is empty (not 204, see C-7).

3. Advancement verification (12, not 6)
   Execute /advancement grant @s only farmstorm:<id> for each one; all 12 succeed;
   the data/farmstorm/advancement/*.json produced by ./gradlew runData number exactly 12;
   1 of them is hidden (lucky_jackpot), and the 24 advancement text keys in lang are all present;
   in game, the /advancement screen shows Chinese names rather than key names for all 12.

4. Check the 59 tasks one by one against the definition of done
   Open docs/zh/03-TASKS.md and output a 59-line checklist in the order F-01 ~ F-59,
   four columns per line: task ID / title / achieved or not / evidence location (command output line, file path, or /data get read-back).
   Judge against the seven definition-of-done items of 05-PROMPT-MAIN.md §6 plus that task's acceptance criteria; missing any one means "not achieved".
   If any line reads "not achieved", stop and report; do not announce that it can be released.

5. DoD sign-off
   Tick DoD-01 ~ DoD-18 per docs/zh/04-ROADMAP.md §6, each with one line of evidence.
   At minimum these four must be evidenced separately: DoD-03 (all 64 registered objects covered), DoD-13 (216 keys),
   DoD-14 (257 resources + 15 green plus signs), DoD-18 (no mixin, no placeholder markers, grep hits 0).

6. Report format
   Output per 05-PROMPT-MAIN.md §7, and append three small tables: the resource inventory table, the 12-advancement table, and the 59-line task checklist.
   Do not paste complete logs (only the conclusion line and the first error per command); anything you did not run or verify must be written as "not verified",
   never as "passed" (N-13 in 05 §8).
~~~

Three criteria at wrap-up (human review, not the agent's self-report):

1. The 16 rows of the resource inventory table sum to **257**, and every row can be pointed to a directory.
2. The advancement table has **12 rows**, all green, and the hidden one is really hidden.
3. The task checklist has **59 rows**, with 0 "not achieved"; if there is any, make it up and re-run this section — no make-up, no release.

---

## 14. Consistency Between This File and 03-TASKS.md (Self-Check Record)

| Check | Method | Conclusion |
|---|---|---|
| Task IDs and titles | Extract `F-xx + title` from the `03-TASKS.md` tables and compare one by one with this file (scripted comparison, not eyeballing) | 59 / 59 identical verbatim, empty difference set |
| Phase count | Count the `## Phase N · …` sections of this file | 10 (Phase 1 ~ Phase 10) |
| Task distribution | Count per phase | 5 / 8 / 5 / 7 / 8 / 6 / 4 / 5 / 6 / 5 = 59 |
| Hard-constraint references | `grep` HC-01 ~ HC-14 | IDs are cited only, the clauses are not copied (consistent with 05 §3's single source) |

Maintenance convention: if `03-TASKS.md` revises any task title, re-run the comparison in this section; this file never changes titles on its own — it only follows upstream.
