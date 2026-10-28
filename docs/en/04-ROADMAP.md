# 04 · Roadmap

> **Document status**: this file is a construction-period historical record of v0.0.1; its numbers and structural descriptions may have been corrected by the implementation. **The implementation baseline is `00-DECISIONS.md`** (see D-42 ~ D-48); the complete list of discrepancies is in `09-IMPL-VS-PLAN.md`.

> Project: Farmstorm · v0.0.1
> Upstream: `plan0.0.1.txt` + `docs/zh/00-DECISIONS.md` (D-01 ~ D-41) + `docs/zh/01-DESIGN.md` + `docs/zh/02-TECH-SPEC.md`
> Sibling: `docs/zh/03-TASKS.md` (59 tasks, 10 phases)
> This document answers four questions: how many milestones there are, how each milestone counts as complete, which chain determines the schedule, and which risks need a plan prepared in advance. **Phase numbering is fully consistent with `03-TASKS.md`** (Phase 1 ~ Phase 10 → M1 ~ M6).

> **Translation note · Chinese item names** — each Chinese item name is rendered as “English (中文原名)” on first use and in English only afterwards. The naming reference:
>
> | 中文原名 | English |
> |---|---|
> | 膨胀麦 | Puffed Wheat |
> | 胡罗贝 | Blast Carrot |
> | 育碧服务器 | Ubi Server |
> | 毒育碧服务器 | Toxic Ubi Server |
> | 瓜片子 | Melon Slice Bomb |
> | 瓜！子母弹 | Melon Grenade |
> | man瓜炮台 | Pumpkin Turret |
> | 火把花 | Torchflower |
> | 瓶子草 | Pitcher Plant |
> | 仙人掌尖牙陷阱 | Fang Trap Cactus |
> | 爆竹 | Bamboo Firecracker |
> | 红菇 | Red Mushroom Puff |
> | 棕菇 | Brown Mushroom Puff |
> | 幸运浆果 | Lucky Berry |
> | 甜菜！ | Power Beet |

---

## 0. Reading Conventions

- Workload is measured in person-days, with the conversion rule unified with `03-TASKS.md`: S = 0.5, M = 1.5, L = 3.0.
- Each milestone's person-days are given as two numbers: the **raw workload** and the workload **including the 25% buffer**. The buffer absorbs only rework and debugging, not scope changes.
- Exit criteria must be independently re-checkable by a third person: every one of them points at a command, a file or an in-game reading.
- A “playability checkpoint” describes what is achievable **from the player's point of view**; it maps one-to-one onto the exit criteria and adds no requirements of its own.

---

## 1. Milestone Overview

| Milestone | Name | Phases | Tasks | Raw workload | With 25% buffer | Cumulative (buffered) | One-line exit |
|---|---|---|---|---|---|---|---|
| M1 | Compilable Skeleton | Phase 1 + Phase 2 | 13 | 25.5 | 31.9 | 31.9 | 59 of the 64 registered objects are in the registry, and all four entry-point commands pass |
| M2 | Deployables Playable | Phase 3 + Phase 4 | 12 | 21.5 | 26.9 | 58.8 | The 5 deployables run per plan, and blow up nothing that should stay intact |
| M3 | Throwables Playable | Phase 5 | 8 | 11.5 | 14.4 | 73.2 | All 8 throwables' impact effects and damage caps line up |
| M4 | Conversion and Rare-Gameplay Loop | Phase 6 + Phase 7 | 10 | 17.0 | 21.3 | 94.5 | The vanilla ↔ modified loop holds, and all 8 Lucky Berry tiers are triggerable |
| M5 | Content and Resources Complete | Phase 8 + Phase 9 | 11 | 14.0 | 17.5 | 112.0 | Datapack and resource pack complete: no purple-black blocks, no missing glyphs |
| M6 | Acceptance and Release | Phase 10 | 5 | 6.5 | 8.1 | 120.1 | All 18 DoD items ticked, jar releasable |
| **Total** | | **10 phases** | **59** | **96.0** | **120.1** | | |

> 96.0 person-days is the **single-person serial** net workload; the buffered 120.1 person-days is roughly 24 weeks (5 days / week). Following the 3-lane schedule of `03-TASKS.md §13.3`, the calendar duration is about **40 person-days (about 8 weeks)**, with the milestone boundaries unchanged.

---

## 2. Milestone Details

### M1 · Compilable Skeleton (Phase 1 + Phase 2)

**Goal**: move from an empty repository to “the skeleton runs, the registry is complete”. What this phase produces is not gameplay but the foundation that all 7 later phases depend on: build baseline, registration skeleton, ally check, static profiles, config framework, and the 59 registered objects.

**Phases included**: Phase 1 (Engineering Base, F-01 ~ F-05), Phase 2 (Full Registry Layer, F-06 ~ F-13).

**Exit criteria** (all observable):

1. `./gradlew build` produces `build/libs/farmstorm-0.0.1+mc1.21.1.jar`; the `./gradlew runServer` log contains `Done (` and no `ERROR` line; `./gradlew runData` exits with code 0.
2. The `build.gradle` extension block is named `neoForge` (case-sensitive), and `neo_version` carries a concrete patch number with no wildcard.
3. The creative tab `farmstorm:main` holds all 22 items, ordered 15 crops → 6 sowing items → converter.
4. `/setblock` succeeds for all 19 block IDs (14 crops + 5 deployables); `/summon` succeeds for all 9 entity IDs.
5. `config/farmstorm-server.toml` generates 58 keys (14 common + 44 effect templates).
6. In F3, the 14 crop blocks carry an `age` property of 0 ~ 3, and the 5 deployed blocks carry `face` / `facing` / `floating`.
7. `/damage @s 1 farmstorm:void_bolt` and `/damage @s 1 farmstorm:crop_blast` both succeed and show their respective death messages.
8. The data component `farmstorm:temporary_enchantments` round-trips consistently on set / get.

**Estimated workload**: 25.5 person-days (Phase 1 = 7.0, Phase 2 = 18.5); with the 25% buffer = **31.9 person-days**.

**Main risks**:

- **RT-01 Version drift** — this is the project's only “first dependency pull”, and the actually available versions of `neo_version` and MDG `2.0.x` are pinned down here. Mitigation: in the very first commit, write the actually resolved versions back into `gradle.properties` and spec §10.2; later upgrades go through a separate PR only.
- **RT-04 The shared BlockEntity class registered 5 times** — generic erasure means the error surfaces only at runtime. Mitigation: `getTicker` uses explicit type branches instead of `createTickerHelper`'s generic inference; F-10's acceptance includes “place 1 of each of the 5 deployables and run 600 ticks”.
- **Same-file write conflicts** — F-06 / F-08 / F-09 / F-38 all modify the two files under `registry/`. Mitigation: adopt the “one task appends only its own segment” convention, and merge these three spots serially within M1.

### M2 · Deployables Playable (Phase 3 + Phase 4)

**Goal**: turn deployables from “blocks that can be placed” into “devices that do things”. Lay the explosion and event foundation first (Phase 3), then implement the 5 behaviors (Phase 4) — the order cannot be reversed, otherwise each of the 5 behaviors would have to write the explosion once more.

**Phases included**: Phase 3 (Explosions and Global Events, F-14 ~ F-18), Phase 4 (Deployment Behavior, F-19 ~ F-25).

**Exit criteria**:

1. Blast Carrot (胡罗贝): a non-ally player entering Chebyshev 1 detonates it, damage per player ≤ 7, all players within explosion radius 4 (teammates included) get Nausea III for 60 ticks, and the block disappears after triggering.
2. Fang Trap Cactus (“赢过输过没死过” — won some, lost some, never died): triggers at Euclidean distance ≤ 3, the outer ring of 8 fangs appears after a 10-tick delay and the inner ring of 5 after a 4-tick delay, and the block disappears once both waves are out.
3. Pumpkin Turret (man瓜): Chebyshev 5-block targeting, one shot per 60 ticks; a hit deals 1 point of `farmstorm:void_bolt` damage plus knockback along the connecting line with no Levitation; it vanishes on hitting a block.
4. Torchflower (火把花): ally players within radius 20 gain 6 buffs of 300 ticks each every 100 ticks, sword holders get temporary Fire Aspect I, and each application emits 8 flame particles.
5. Pitcher Plant (瓶子草): ally players within radius 10 gain 3 buffs of 300 ticks each every 100 ticks and have all negative effects cleared, with 8 heart particles.
6. After one blast each from TNT / a creeper / an end crystal, the torchflower and pitcher plant are still there; `blast_carrot` at the same position is blown away.
7. Non-ally players (creative mode included) make exactly 0 mining progress on the torchflower / pitcher plant; `pumpkin_turret` takes a fixed 10 seconds for everyone.
8. A single player's 33rd and a single chunk's 9th deployable are rejected without consuming the item; “place → break → place again” is rejected inside the 5-tick cooldown.
9. When a temporary enchantment expires the component is removed, and a sword whose original level was 2 is not downgraded.
10. After a player joins or leaves their own team, already-deployed traps and turrets adjudicate against the **current** team with no redeployment (C-9: deployables store no team snapshot).

**Estimated workload**: 21.5 person-days (Phase 3 = 9.5, Phase 4 = 12.0); with the 25% buffer = **26.9 person-days**.

**Main risks**:

- **RT-08 Compatibility with explosions from other mods** — “no explosion can break it” must rely on `ExplosionEvent.Detonate` filtering, not on resistance alone. Mitigation: F-17 lands both the 3600000F resistance and the tag filter, and the acceptance fires each of the three explosion sources once.
- **RT-06 20-block aura stacking** — a multi-player team stacking more than 10 auras amplifies effect refresh and particle broadcasts. Mitigation: a `MAX_EFFECTS_PER_TICK` circuit breaker plus same-tick, same-player deduplication; the M2 exit verifies functionality only, and performance is quantified in M6's F-57.
- **RT-02 Hard-mining progress rebound** — if `getDestroyProgress` reads a field that only exists on the server, the client computes a different result. Mitigation: write it as a pure function that returns the default speed when `BE == null`; do a single-player self-test within M2 and leave the formal three-mode regression to M6's F-55.

### M3 · Throwables Playable (Phase 5)

**Goal**: all 8 throwable crops can be thrown, and their impact effects match `plan0.0.1.txt` item by item. This is the phase that “delivers 8 behavior implementations in one go”, and the one with the highest parallel gain (6 tasks can proceed at the same time).

**Phases included**: Phase 5 (Throwables, F-26 ~ F-33).

**Exit criteria**:

1. All 8 throwables can be thrown and hit, and their impact effects match the descriptions on lines 38 / 56 / 72 / 79 / 86 / 93 / 121 / 130 / 139 of `plan0.0.1.txt`.
2. Damage caps verified one by one: Blast Carrot 7, Ubi Server (育碧服务器) 2, Toxic Ubi Server (毒育碧服务器) 2, Melon Slice Bomb (瓜片子) 6, Melon Grenade (瓜！) 8.
3. Puffed Wheat (膨胀麦): a dry impact point replaces 27 blocks with hay blocks, and 125 when water is inside the area; bedrock / barrier / command block are never replaced.
4. Melon Grenade: after spawning 4 submunitions (`submunition=true`) it explodes immediately, for a total of 5 explosions in the scene.
5. Bamboo Firecracker (爆竹): after impact `noPhysics=true` and the velocity is zero, one wind charge every 5 ticks for 28 times in total, and it disappears after 140 ticks.
6. Mushrooms: all players within radius 4 (teammates included) get the corresponding effects; no blocks are broken and no damage is dealt.
7. A throwable living longer than `throwable.lifetimeTicks` (default 100) disappears automatically; throwing in creative mode consumes no item.
8. Cobblestone (6.0) is not blown up by the Melon Slice Bomb or the Melon Grenade, while dirt is.

**Estimated workload**: 11.5 person-days; with the 25% buffer = **14.4 person-days**.

**Main risks**:

- **Whether `Explosion#finalizeExplosion` is still overridable** (the “build-time decides” item of spec §10.2) — this is the only hook of the three-stage explosion. Mitigation: M2's F-14 exposes the issue already; a degradation plan is ready (do not extend `Explosion`, use a standalone executor with the interface unchanged), and the impact is confined to the `explosion/` package.
- **The block write volume of Puffed Wheat** — stacked at 16 the worst case is 2000 `setBlock` calls. Mitigation: skip blocks already in the target state and send only `Block.UPDATE_CLIENTS`; run one same-tick stress check in F-57.
- **Changes to how `PotionContents` / `AreaEffectCloud` are constructed** — the Toxic Ubi Server depends on them. Mitigation: the degradation plan applies `MobEffects.POISON` directly to entities inside the cloud, which is behaviorally equivalent.

### M4 · Conversion and Rare-Gameplay Loop (Phase 6 + Phase 7)

**Goal**: make the “vanilla crop → modified crop → vanilla crop” loop actually hold, and deliver the two edible crops. When this phase ends the player's full cycle is closed: plant → harvest → convert → use.

**Phases included**: Phase 6 (Converter, F-34 ~ F-39), Phase 7 (Food Crops and Lucky Berry, F-40 ~ F-43).

**Exit criteria**:

1. Put in wheat and click convert once to get puffed wheat and lose 3 XP; convert it back to wheat.
2. With insufficient XP it is rejected and **no level is deducted**; clicking 10 times in a row leaves item and XP unchanged.
3. The torchflower / pitcher plant are forward-only and additionally cost 1 nether star; reverse is free.
4. The converter GUI shows a 176×166 background, the title “Farm Produce Converter”, the convert button and the XP cost; a button click goes only through the vanilla `ServerboundContainerButtonClickPacket` (0 custom network packets in the whole project).
5. Power Beet: 32 ticks after eating, gain Strength II / Speed II / Haste II / Resistance I for 600 ticks each.
6. Lucky Berry: each of the 8 probability tiers is force-triggered by setting the config to 100 / 0, and each tier's result matches spec §4.12.
7. Creative branch: restored automatically after 1800 ticks; restored immediately on disconnecting midway; after `kill -9` on the server and a reconnect, non-OP players are restored to survival and OP players are left untouched.
8. All 64 registered objects are in place (the converter's block, BE, Menu, RecipeType and Serializer are completed in this phase).

**Estimated workload**: 17.0 person-days (Phase 6 = 10.5, Phase 7 = 6.5); with the 25% buffer = **21.3 person-days**.

**Main risks**:

- **RT-05 Creative-mode rollback failing** — the spec marks it as the “highest-severity gameplay hole”. Mitigation: three-tier fallback (in-memory table + immediate restore on disconnect + login reconciliation), and login reconciliation applies only to non-OP players so that a creative server is never hit by mistake; M6's F-58 reproduces it once with `kill -9`.
- **Atomicity of the conversion transaction** — the validation order is the security property, and any step that consumes materials early becomes an item-duplication hole. Mitigation: `tryConvert` strictly “validates everything first, touches state afterwards”, and the acceptance includes “click 10 times for no effect”.
- **RT-07 Recipe cache** — stale recipes are hit after `/reload`. The code lands in this phase (F-39 builds the cache); the regression acceptance is completed in M5 (F-48).

### M5 · Content and Resources Complete (Phase 8 + Phase 9)

**Goal**: fill in the pure data and pure resources so the mod goes from “playable” to “complete” — 21 recipes, 20 loot tables, 4 tags, 12 advancements, 20 blockstates, 62 + 22 models, 87 textures, 216 language keys.

**Phases included**: Phase 8 (Data-Driven Content, F-44 ~ F-48), Phase 9 (Resources and DataGen, F-49 ~ F-54).

**Exit criteria**:

1. After `/reload`, 5 consecutive conversions succeed and the log has no `Recipe not found`; delete one recipe JSON and `/reload`, and the corresponding item is immediately non-convertible (RT-07 closed).
2. The 14 crop loot tables drop both the crop and the sowing item at vanilla proportions at `age=3`, and only 1 sowing item when immature; the 5 deployable tables drop themselves by default and 0 items when destroyed by an explosion.
3. The 4 tags have 5 / 15 / 6 / 6 entries; `/execute if block ~ ~ ~ #farmstorm:deployed_crops` returns 1 for all 5 deployed blocks.
4. All 12 advancements load and unlock, and `lucky_jackpot` is hidden.
5. One `./gradlew runData` generates the full artifact set, **257** in total (JSON 169 + TOML 1 + PNG 87); this is +6 over the pre-C-7 figure of 251 (`02-TECH-SPEC.md §6.1` has been synced to 257), the difference coming from 12 advancements replacing 6 (see `03-TASKS.md §0.4` ruling 1).
6. The key-set diff between `zh_cn.json` and `en_us.json` is empty and the key count is 216; no `item.farmstorm.*` is displayed raw in game.
7. All 87 textures are in place, the 15 crop item icons carry a green plus in the bottom-right corner, and there are no purple-black blocks in the creative tab or in the world.
8. The turret missile is visible on the client and reuses the shulker bullet model.

**Estimated workload**: 14.0 person-days (Phase 8 = 5.5, Phase 9 = 8.5); with the 25% buffer = **17.5 person-days**.

**Main risks**:

- **PM-05 Asset review bottleneck** — the 87 PNGs are script-produced placeholders, and the final pixels are reviewed by the author (DEC D-25). Mitigation: ship the script first and swap the pixels later; texture replacement blocks no code task, only the 14th DoD item of M6.
- **Drift in the resource count figures** — the two deltas 251 → 257 and 204 → 216 must be written into the delivery notes. Mitigation: F-53's acceptance reconciles the total directly instead of relying on human memory.
- **DataGen reset risk** — hand edits to `src/generated` are overwritten by the next `runData`. Mitigation: the acceptance requires “an empty git diff after running runData again”.

### M6 · Acceptance and Release (Phase 10)

**Goal**: run through the 9 technical risks and the DoD item by item, and produce a releasable v0.0.1.

**Phases included**: Phase 10 (Acceptance and Release, F-55 ~ F-59).

**Exit criteria**:

1. All 18 DoD items (see §6) are ticked, with the command output or screenshot location recorded next to each tick.
2. Each of the four acceptance lines has a record: server regression (F-55), numeric cross-check (F-56), performance (F-57), failures and crashes (F-58).
3. The file name `build/libs/farmstorm-0.0.1+mc1.21.1.jar` is correct, and the version inside the jar's `neoforge.mods.toml` resolves to 0.0.1.
4. The repository has no mixin config and no placeholder marker (grep hits 0).
5. Each of RT-02 / RT-03 / RT-05 / RT-08 / RT-09 in the risk register has one passing record.

**Estimated workload**: 6.5 person-days; with the 25% buffer = **8.1 person-days**.

**Main risks**:

- **The acceptance environment is not reproducible** — crash regression (`kill -9`) and multi-player scenes need a dedicated server that can be restarted. Mitigation: have that environment ready before M5 ends, and do not push environment setup into M6.
- **The numeric cross-check degenerates into a formality** — checking 117 keys item by item is tedious and easy to miss entries in. Mitigation: F-56 exports the toml with a script and diffs it against the spec table, leaving humans to re-check only the differing entries.

---

## 3. Critical Path Analysis

### 3.1 Critical Path (determines the shortest calendar duration)

```text
F-01 Build Baseline 1.5
 └─ F-02 Main Class and Registration Skeleton 1.5          (cumulative 3.0)
     └─ F-05 Config 58 Keys 3.0          (cumulative 6.0)
         └─ F-34 ConverterRecipe 1.5 (cumulative 7.5)
             └─ F-35 ConverterBlockEntity 1.5 (cumulative 9.0)
                 └─ F-36 ConverterMenu 1.5    (cumulative 10.5)
                     └─ F-38 ConverterBlock 1.5 (cumulative 12.0)
                         └─ F-49 blockstates and models 3.0 (cumulative 15.0)
                             └─ F-53 DataGen assembly and artifact check 1.5 (cumulative 16.5)
                                 └─ F-55 Server and three-mode regression 1.5 (cumulative 18.0)
                                     └─ F-59 Packaging and release 0.5 (cumulative 18.5)
```

**Critical path length = 18.5 person-days / 11 tasks.**

### 3.2 Second-Longest Chain

`F-01 → F-02 → F-05 → F-09 → F-10 → F-19 → F-22 → F-54`, totalling **17.0 person-days** (the turret chain, ending in client rendering).

### 3.3 Conclusion and Compressible Points

- The 18.5-person-day critical path is only **19%** of the 96.0 total. In other words, **the schedule is not held back by the longest chain but by the total workload and the degree of parallelism**: 96.0 person-days serially by one person, about 32 person-days across 3 lanes. To shorten the schedule, add parallelism first rather than compressing the critical path.
- Three genuinely compressible points:
  1. **Split F-05**: split “14 common keys” and “44 effect keys” into two tasks, and F-34 can start about 2 person-days earlier, lowering the critical path to about 16.5 person-days. The price is one extra commit.
  2. **Split F-49**: split blockstates and block models in half; the model half does not block F-53's artifact check, saving about 1.5 person-days.
  3. **Do not compress F-35 → F-36 → F-38**: the converter container and menu are a security-critical path (anti item-duplication); compressing them carries RT-07 and an item-duplication hole straight into M5.
- The real calendar bottleneck is **M1**: of the 25.5 person-days of raw workload, 4 of the 13 tasks are L-sized and all wait on each other's files. If M1 is not parallelised, no later phase can start.

---

## 4. Per-Phase Playability Checkpoints

| Milestone | What the player can actually do | How it is verified |
|---|---|---|
| **M1** | Take all 22 items from the creative tab; place the 14 crop blocks and the 5 deployed blocks (the look is there, the behavior is an empty shell); try the two custom damage types' death messages with `/damage` | Visual check of the creative tab + `/setblock` one by one + `/damage` returning success |
| **M2** | Plant a blast carrot and blow a teammate into the air; place a pumpkin turret and watch it fire on its own and knock people flying; step on the cactus and eat two rings of fangs; stand under the torchflower for 6 buffs and notice the extra Fire Aspect on the sword; blow the torchflower up with TNT and find it will not break | Real test on a two-player server + count fangs with F3+B + `/effect` readings + `/data get` on the component |
| **M3** | Throw the 8 throwables like grenades: stack a hay wall, blow up stone, drop a poison cloud, rain 140 ticks of wind charges, and feed a whole area's worth of players Nausea and Slowness together | One round each single-player and two-player, compared value by value with `plan0.0.1.txt`; damage caps checked against health readings |
| **M4** | Take a backpack of wheat to the converter, convert it to puffed wheat and back again; eat a Power Beet and be buffed for 30 seconds; chew a Lucky Berry and gamble on the 0.1% creative branch, then kill the game process and come back to find the mode already restored | Converter GUI test + `/effect` readings + force-triggering each config tier + `kill -9` reproduction |
| **M5** | Play a complete session: plant the modified crops, get both the crop and the seeds at harvest, unlock the 12 advancements one by one (the hidden one included), a fully Chinese UI with no missing glyphs, and a green plus in the bottom-right corner of the item icons | One complete save playthrough + `/advancement` checks + language file diff + visual check of the icons |
| **M6** | The server owner drops the jar into a dedicated server and players can join with only the mod itself installed; ticks do not collapse in a base with 10 auras + 8 players; nobody is stuck in creative mode after a crash and restart | Dedicated server deployment + `/debug profile` + crash-and-restart regression |

---

## 5. Risk Register

### 5.1 Technical Risks (continuing RT-01 ~ RT-09 of `02-TECH-SPEC.md §9`)

| ID | Risk | Trigger signal | Impact | Mitigation | Owning milestone | Verification |
|---|---|---|---|---|---|---|
| RT-01 | Patch-level API drift in NeoForge 21.1.x / MDG 2.0.x | Compilation reports signature errors after upgrading `neo_version` or `net.neoforged.moddev` | The four most fragile spots: `finalizeExplosion`, `RecipeSerializer#streamCodec`, `FoodProperties.Builder`, `PotionContents` | Pin the version numbers, never write dynamic versions; upgrades go through a separate PR that changes only the version; add compile-level smoke checks at the four spots | Pinned in M1, re-checked throughout | Two consecutive successful `build`s on the pinned version |
| RT-02 | The fixed 10-second hard-mining disagreeing with client / server prediction | The progress bar rebounds, or mining at 99% suddenly drops to zero | Players report bugs; the feel cannot be trusted | Write `getDestroyProgress` as a pure function; return the default speed when `BE == null`; owner adjudication goes through the already-synced BE | Implemented in M2, re-verified in M6 | Mine once each in creative / survival / spectator; retest after a `/tp` round trip far away and back |
| RT-03 | Temporary enchantment state leaking | Leftover enchantments after the item is destroyed by lava / put into a chest / dropped on death | Extra Fire Aspect, or a real enchantment lowered on revoke | Record the `previous` level; restore only when “current level == granted”; `expireAt` uses absolute gameTime | Implemented in M2, re-verified in M6 | Four scenario hand tests + assert the component is cleared |
| RT-04 | The shared `DeployedCropBlockEntity` class registered 5 times | `ClassCastException` when a specific deployable is ticked | Surfaced only at runtime, invisible at compile time | `getTicker` uses explicit type branches rather than generic inference; the 5 `BlockEntityType.Builder#of` each get their own block list | Pinned in M1, re-verified in M2 | Place 1 of each of the 5 deployables and run 600 ticks with no exception |
| RT-05 | Creative-mode rollback failing on failure paths | After a server crash / force quit / `/stop` the player is left in creative mode | Highest-severity gameplay hole | Three-tier fallback: in-memory table + immediate restore on disconnect + login reconciliation; reconciliation applies to non-OP only | Implemented in M4, re-verified in M6 | Reconnect after `kill -9`; one regression on a creative server confirming OP is not modified |
| RT-06 | Performance and stacking of the 20-block aura | A multi-player base stacking more than 10 torchflowers raises tick time | Effect refresh storm; particle broadcasts amplify bandwidth | `MAX_EFFECTS_PER_TICK` circuit breaker; same-tick, same-player deduplication; play particles only when an effect actually applied | Implemented in M2, quantified in M6 | 10 auras + 8 players, `/debug profile` single tick < 1 ms |
| RT-07 | Converter recipe cache and `RecipeManager` reload | Deleted recipes can still be converted after `/reload` | Players can bypass datapack updates | Listen to the recipe reload event and clear the cache; do one `byKey` validation of the cache inside `tryConvert` | Cache built in M4, regressed in M5 | 5 consecutive conversions after `/reload`; immediate invalidation after deleting a recipe |
| RT-08 | Compatibility with explosions from other mods | Vanilla TNT / creepers / end crystals blow up the aura plants | D-14 “no explosion can break it” fails | `ExplosionEvent.Detonate` filters `#farmstorm:explosion_immune`; plus a 3600000F resistance as a second line of defence | Implemented in M2, re-verified in M6 | Fire each of the three explosion sources once; the blocks are still there |
| RT-09 | Ambiguity of the “cobblestone 6.0” threshold for modded blocks | Other mods add blocks with resistance 5.9 ~ 6.1 | The break boundary does not match player intuition | Thresholds go into config (`explosion.sliceResistThreshold` / `grenadeResistThreshold`) | Implemented in M3, re-verified in M6 | One block each at resistance 6.0 and 5.99; the former is not broken, the latter is |

### 5.2 Project Management Risks (newly added here)

| ID | Risk | Trigger signal | Impact | Mitigation | Owning milestone |
|---|---|---|---|---|---|
| PM-01 | Version drift (three “build-time decides” spots: `neo_version` / MDG `2.0.x` / the `FoodProperties` and `PotionContents` APIs) | The versions resolved at build time differ from the spec §10.2 table | The spec and the implementation diverge, and later tasks copy the old API | Write back into `gradle.properties` and spec §10.2 right after the first `build`; list the write-back as an implicit M1 exit item | M1 |
| PM-02 | Documentation–implementation drift (the three upstream documents already disagree in 3 places, adjudicated by C-7 / C-8 / C-9 in `00-DECISIONS.md`, see `03-TASKS.md §0.4`) | The same thing has two different descriptions during implementation | Rework; arguments at acceptance | Adjudication records come first; the three numbers 117 config keys, 64 registered objects, 12 advancements must be script-checkable in every document | Throughout |
| PM-03 | Cross-module rework (a change to the `DeployedCropBlock` / `DeployedCropBlockEntity` interface ripples into the 5 behavior implementations) | BE fields change frequently during Phase 4 | All 5 behavior tasks rework at once and M2 slips | Lock down the BE's public surface in F-10's exit (the 6 synced fields `ownerUuid` / `ownerName` / `placedGameTime` / `detached` / `pendingWaves` / `fuseTicks`, with no team snapshot per C-9; + `consume()` + `setOwner()`); behaviors call interfaces only and never touch fields | M2 |
| PM-04 | Context loss (the 59 tasks are executed by different sessions, and the previous session's conventions do not carry over automatically) | A later task reimplements an existing utility class, or bypasses an existing interface | Duplicate code; style divergence | Each task brief is self-contained (the three columns “Files / Classes”, “Depends on”, “Acceptance criteria” of `03-TASKS.md` are the brief skeleton); the public surface of `util/` and `registry/` is extended only when necessary after M1 is pinned | Throughout |
| PM-05 | Asset review bottleneck (the 87 PNGs are first produced as placeholders by script, and the final pixels need author review) | At the end of M5 the textures are still placeholder colour blocks | Blocks only DoD item 14, not the code | Texture replacement is decoupled from code and can happen at any time; the review is scheduled into the last 3 person-days of M5 | M5 |
| PM-06 | Task granularity off (an L-sized task actually exceeds 3.0 person-days) | An L task has produced no committable intermediate artifact for two consecutive days | Milestone estimates distort and the buffer is drained | The first day of every L task must produce a compilable skeleton commit; if it overruns twice in a row, split it into two S/M tasks and correct the total workload in step | Throughout |
| PM-07 | Build environment differences (Windows paths, Chinese file-name encoding, Gradle cache and JDK version) | The same commit fails to `build` on another machine | Acceptance cannot be reproduced | All paths in task briefs use repository-relative paths; `sourceEncoding = UTF-8` is written explicitly into `build.gradle`; the JDK and Gradle versions are recorded in the delivery notes | Pinned in M1 |
| PM-08 | Scope creep (Farmer's Delight compatibility, FTB Teams integration, new planting-type designs, number balancing) | A task contains an item from the “explicitly deferred” list in section 6 of `00-DECISIONS.md` | The schedule runs out of control and the DECs are bypassed | The deferral list goes into every task brief's boundaries; `TeamResolver` keeps only the interface and gets no second implementation; all numbers are copied verbatim (D-21) | Throughout |
| PM-09 | Vagueness in acceptance criteria (phrasing like “playable” or “feels right” entering acceptance) | The acceptance column of a task brief contains unobservable wording | Completion cannot be judged and acceptance turns into subjective argument | All acceptance items must land on one of log / `/execute` / `/data get` / file content / F3 reading; `03-TASKS.md §0.3` already states this prohibition | Throughout |

---

## 6. v0.0.1 Definition of Done (DoD)

Tick each item before release; if any single item is unmet, the version number stays at 0.0.1-rc.

- [ ] **DoD-01 Build**: `./gradlew build` passes, the artifact is `build/libs/farmstorm-0.0.1+mc1.21.1.jar`, and the version inside the jar's `neoforge.mods.toml` resolves to 0.0.1.
- [ ] **DoD-02 Dedicated server**: `./gradlew runServer` starts standalone and prints `Done` in ≤ 120 s, with no `ERROR` in the log.
- [ ] **DoD-03 Registry completeness**: all 64 registered objects are in the registry, the mapping table in `03-TASKS.md §14` corresponds one-to-one with the implementation, 0 uncovered.
- [ ] **DoD-04 Creative tab**: the `farmstorm:main` tab holds all 22 items in the correct order.
- [ ] **DoD-05 Planting loop**: all 14 crops can be planted, and a mature harvest drops both the crop and the sowing item (the melon slice is unplantable by design).
- [ ] **DoD-06 Conversion loop**: the 21 recipes work in both directions; insufficient XP is rejected without deducting levels; the torchflower / pitcher plant are forward-only and consume 1 nether star.
- [ ] **DoD-07 Throwable behavior**: the 8 throwables' impact effects and damage caps match `plan0.0.1.txt` item by item.
- [ ] **DoD-08 Deployable behavior**: the 5 deployables' trigger / targeting / aura behavior matches item by item, and creative-mode players are not targeted by traps.
- [ ] **DoD-09 Breaking rules**: `pumpkin_turret` takes a fixed 10 seconds; the torchflower / pitcher plant give non-allies zero progress, cannot be destroyed by any explosion, and are recoverable by allies at normal speed (D-14 / D-37 / C-8 rulings).
- [ ] **DoD-10 Temporary enchantments**: granted on entering an aura, revoked on leaving or expiry, the original level is never downgraded, and nothing remains after the item is destroyed.
- [ ] **DoD-11 Lucky Berry**: each of the 8 probability tiers can be force-triggered; the creative branch restores after 90 seconds and leaves no residue after a crash.
- [ ] **DoD-12 Advancements**: all 12 advancements unlock, 1 of them hidden (C-7).
- [ ] **DoD-13 Language**: `zh_cn.json` and `en_us.json` have identical key sets, 216 keys in total; no key is displayed raw in game.
- [ ] **DoD-14 Resources**: 257 `runData` artifacts (JSON 169 + TOML 1 + PNG 87); the 15 crop icons carry a green plus in the bottom-right corner; no purple-black blocks.
- [ ] **DoD-15 Config**: all 117 keys are generated in `config/farmstorm-server.toml`, with default values matching the originals in `plan0.0.1.txt` item by item.
- [ ] **DoD-16 Server authority**: 0 custom network packets; the owner is read only from server context; the client takes part in no adjudication.
- [ ] **DoD-17 Performance**: single tick < 1 ms in the 10-auras + 8-players scene; no spike with 8 deployables in one chunk.
- [ ] **DoD-18 Delivery cleanliness**: the repository has no mixin config and no placeholder marker (grep hits 0); the Phase numbering, task counts and workload numbers in `03-TASKS.md` and `04-ROADMAP.md` agree with each other.

---

## 7. Scheduling and Mitigation

### 7.1 Recommended Schedule (3 lanes)

```text
              M1 Skeleton (31.9)       M2 Deploy (26.9)    M3 Throw (14.4)  M4 Loop (21.3)   M5 Content (17.5) M6 Release (8.1)
Lead    F-01 F-02 F-05 F-09 F-10  F-14 F-19 F-22      F-27 F-29        F-34 F-35 F-36 F-38 F-49 F-53  F-55 F-59
Par-A   F-04 F-06 F-08 F-11       F-15 F-20 F-21      F-26 F-28 F-31   F-39 F-40        F-44 F-45 F-50  F-56
Par-B   F-03 F-12 F-13            F-16 F-17 F-18 F-23 F-30 F-32 F-33   F-37 F-41 F-42   F-46 F-47 F-51  F-57 F-58
                                  F-24 F-25                              F-43             F-52 F-54
```

- Calendar duration ≈ the **actual stage-overlapped occupancy** of 32 + 27 + 15 + 21.3 + 17.5 + 8.1, about **40 person-days (8 weeks)**; strictly serial by phase (each milestone waiting for the whole previous one) it is about **120 person-days (24 weeks)**.
- When executed by one person, advance serially phase by phase and **do not skip phases**: M1's registry layer is the prerequisite for everything that compiles later (spec §11.2 gives the same phase-order advice).

### 7.2 Buffer Rules

- Reserve a 25% buffer inside each milestone, used only for rework and debugging; once the buffer is drawn on, record the reason at that milestone's exit.
- If two milestones in a row draw more than half the buffer, pause new tasks and first re-check the workload conversion in `03-TASKS.md` (PM-06).
- Scope changes (new crops, integration with other mods) always go through an appended decision in `00-DECISIONS.md` and are not absorbed in place at the task level (PM-08).

### 7.3 Freeze Points

1. **M1 exit**: the `neo_version` and MDG versions in `gradle.properties` are frozen; afterwards only a separate PR may upgrade them (RT-01).
2. **M2 exit**: the public surface of `DeployedCropBlockEntity` is frozen (PM-03).
3. **M4 exit**: the key names and default values of the 117 config keys are frozen (D-21 / D-22); a change requires changing all three documents at the same time.
4. **M5 exit**: registry IDs and language keys are frozen; afterwards only defect fixes are allowed.
