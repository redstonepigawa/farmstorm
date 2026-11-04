# 44 · v0.2.1 Implementer's Master Prompt

> Feeding method: this file is fed **once only**, before any Phase. After that, feed per Phase following document 45.
> Reading order: **40 (decisions, the single source of truth) → 41 (design) → 42 (tech) → 43 (tasks and adjudications) → this file**. Where they conflict, document 40 wins; this file is harder than document 40 in exactly two places: the **red-line standard** (HC-137) and the **execution discipline** (§5 ~ §8).
> This file's standing: **a standing constraint** — read it before you start any work, then read the current Phase that document 45 gives you. This file does not answer "which thing to do this round" (that is documents 43 / 45's business); it only answers "which ways of doing it are definitely wrong".
> Do only what the task brief asks, one thing at a time: stop when done, report, wait for the next Phase. Do not implement the next stage in advance, and do not stuff a "fixed it on the way" into this round.

| Item | Value |
|---|---|
| Document ID | 44-PROMPT-MAIN-0.2.1 |
| Audience | the implementation agent (can read and write this repository, can run `.tools\gw.cmd`) |
| Upstream | `40-DECISIONS-0.2.1.md` (D-173 ~ D-186, 14 entries + 17 rulings) → `41-DESIGN-0.2.1.md` (5 requirements R1 ~ R5 / 36 boundaries B-01 ~ B-36) → `42-TECH-0.2.1.md` (97 registrations / 31 action-surface rows / 176 config / 338 language keys / 31 acceptances / 10 to verify) → `43-TASKS-0.2.1.md` (from F-247 / from Phase 40 / adjudications A-70 ~ A-81) |
| Numbers this file issues | **HC-121 ~ HC-137 (17 entries)**, **N-115 ~ N-126 (12 entries)**, fact card **19 rows**, pitfall table **41 entries** (items 1 ~ 31 inherited from document 38, items 32 ~ 41 new in this version), known conflicts **17 rows**, pre-start confirmation **12 items** |
| What this file does not write | class design and algorithms (document 42), tasks and schedule (document 43), per-Phase content (document 45). This file writes only "what is definitely wrong" and "how to decide it" |
| Numbering relay | decision **D-173** / task **F-247** / Phase **40** / milestone **M36** / hard constraint **HC-121** / prohibited behaviour **N-115** / risk **RT-51** / adjudication **A-70** / implementation verdict **V-193** |

## 0. What You Are Facing

v0.2.1 is a **small increment**: the 4 additions in `plan0.2.1.txt` (landing on the five line numbers `:9` / `:10` / `:14` / `:18` / `:22`, with the 1st taking up both `:9` and `:10`) are split into 5 requirements R1 ~ R5 (41 §1), with **+0** registered objects, **+1 config key** and **+1 language key**. It does not swap out a whole mechanism the way 0.2.0 did, **but it has two "firsts for the whole project" and one "overturning the previous version's own promise"** — none of the three can be passed by "copying a class as it stands".

| # | Category | What it is | Why there is no precedent | Where it lands |
|---|---|---|---|---|
| 1 | A first for the whole project | **Enumerating block entities by area** | `ChunkMap` **0** hits; `LevelChunk#getBlockEntities()` only **1** hit (`DeployedCropBlockEntity:227-236`, a single chunk, counting only itself), and the line that gets the chunk there, `:228`, uses the chunk-loading `getChunk` ⇒ **only half of it can be copied** | R1; 42 §2.1.2; F-248 |
| 2 | A first for the whole project | **Judging hostility against the vanilla template** | the whole repository's hostility standard is only `net.minecraft.world.entity.monster.Monster` (5 `getEntitiesOfClass` sites); `Enemy` / `isHostile` / `MobCategory.MONSTER` all have **0** hits | R5; 42 §2.4; F-257 |
| 3 | **Overturning the previous version** | Rebind **partially supersedes** the two self-promises of 0.2.0's D-165 | D-165's supplement writes "no nearest-drift / periodic rebind at runtime" and "do not quietly rebind at runtime"; ruling #12 **supersedes** those two sentences, and the binding's **write points go from 2 to 3** (placement / migration / periodic rebind) | R1; 40 §5; ruling #12 |

The third one is the easiest place to misread: **"partial supersession" is not "overturning the whole entry"**. D-165's core-selection criterion, the **16 ceiling**, the one-to-one invariant and **"the single truth of the binding is on the core side"** are all kept (40 §5, reading notes). Reading "rebind" as "switch to using the cultivator-side cache as the truth" amounts to tearing down the previous version's skeleton along with it.

**The three most expensive readings** (memorise them; they decide half of this version's rework):

1. **100 ticks are not "there as soon as it is wired in"**: `serverTick`'s existing particle phase gate already `return`s at `:516-518`. Wire the rebind downstream of it and the actual period = `LCM(40, 100) = 200` ticks, drifting with `boundaryParticleIntervalTicks` (default 40, configurable 20 ~ 1200) — "every 100 ticks" fails on the spot (42 §2.1.1).
2. **Only 2 of the 4 trap crops take part in ×3**: plan's original text says "**all** trap crops"; ruling #11 narrows it to `blast_carrot` and `zangief_cane`; `fang_trap_cactus` (its damage lives in the vanilla `EvokerFangs`, with **0** `hurt` in the repository) and `vibration_bean` (0 damage) **do not take part**. Writing "all traps ×3" is wrong, and the acceptance goes wrong with it (RT-58, 41 §6.4).
3. **The attachment is not in the 97**: `farmstorm:ai_suspend` is attached to `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` and is **the 13th category outside this project's 12-category count**; the 97 does not change, but it **must be registered explicitly** — the tool neither checks its existence nor counts it (ruling #17, 42 §1.2.1).

**None of the five requirements grows on the registration surface**: the rebind uses the existing `OutpostCoreBlockEntity` + `CultivatorBlockEntity`; the tooltip goes through the vanilla `AbstractContainerScreen` chain; area enumeration and the hostility predicate are both plain static utility classes; the original AI flag rides in an **out-of-count** entity attachment; the multiplier is a config key ⇒ the 97 is unchanged, packets are still 2, Mixin is still 0 (the note under 42 §1.2's table). The four components that are "added but do not enter the registration surface" are named one by one: `util/AreaBlockEntities`, `util/Hostility`, `combat/MobAiSuspension` (including `Suspend`), `trap.damageMultiplier` — **do not infer the registration surface from "the number of new classes"**; that lands you on the wrong conclusion of 101.

Tasks, Phases, milestones and estimates are all in document 43 (from F-247 / from Phase 40 / from M36 / from A-70); this file cites their numbers only and **does not repeat the schedule**.

## 1. Hard Constraints HC-121 ~ HC-137 (17 entries)

The HCs constrain only this version's increment surface. Among document 38's HC-97 ~ HC-120, **6 HCs** are superseded by this version, and one more **N** (N-106) is void as a whole (the list is in §6.3); the rest remain in force; **where they conflict, this file and document 40 win**. Each entry gives three things: the **constraint** (what counts as right), the **criterion** (what counts as passed) and the **source** (decision / tech / task number). The criterion column is always executable: one command, one `git diff`, one log line, one reading.

- **HC-121 · The registration surface is 97 (+0), and the 13th category outside the count must be registered explicitly**
  - Constraint: the 12 class subtotals `items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2` = **97**, and this version adds **+0** class by class; `data_components` is still **1**; packets are still **2** and the protocol version is still `"2"`. One **attachment type** is noted separately: `farmstorm:ai_suspend` (`AttachmentType<Suspend>`, registry `ATTACHMENT_TYPES`), **not counted in the 97**, but it must be written into the documents and into the sign-off.
  - Criterion: the server-start log still reads `自检 注册对象 97 个：items 33 / … / menu_types 2 → OK`; the 12-class assertion at `Farmstorm.java:167-172` is unchanged word for word; the constructor's `.register(modBus)` lines in `Farmstorm.java` go **11 → 12** (`damage_type` is data-driven and has no register call in the first place); the sign-off carries a row "attachment types **1** (outside the count)".
  - Source: D-175; ruling #17; 42 §1.2 / §1.2.1; A-73; F-252, F-266.

- **HC-122 · config 176 keys / 60 groups, with the single-key standard pinned**
  - Constraint: the `trap` group goes **3 → 4** keys (new `damageMultiplier`: double, default **3.0**, range **1.0 ~ 10.0**); the group count is still **60** (`trap` is an existing group; add a key, build no group); total keys `175 + 1 = 176`; neither `blast_carrot.maxDamage` (7) nor `zangief_cane.slamDamage` (7.5) changes.
  - Criterion: line-initial `.define*` in `FarmstormConfig.java` = **176** (the comment line at `:935` does not count); `b.push(` = `b.pop()` = **60**; `run/config/farmstorm-server.toml` (run `runServer` once first to regenerate it) = **60 sections / 176 keys**, with the `[trap]` section at **4 keys**.
  - Source: D-183, D-185; 42 §4.1 / §4.2; F-258, F-262.

- **HC-123 · Language keys 338 × 2, the same keys and the same count in Chinese and English, and there is no automatic gate**
  - Constraint: add **1** key `farmstorm.configuration.trap.damageMultiplier` (one entry each in zh_cn / en_us, placed after `ModLangProvider:412-413`); `farmstorm.configuration.*` goes **178 → 179** (= 176 server + 3 client); `tooltip.farmstorm` is still **20**. The class comment at `ModLangProvider.java:13-16` (`337` / `178`) is **changed in one pass in the window-closing task only**.
  - Criterion: taking the key sets with `ConvertFrom-Json`: `zh = 338`, `en = 338`, zh-only **0**, en-only **0**; `config(entries, …)` count **179**; in `ModLangProvider.java:13-16`, `337` has **0** hits and `338` ≥ 1 hit; the "139 个配置项" at `:237` is unchanged word for word.
  - Source: D-185; 42 §4.3, §9 item 4; A-74; F-258, F-263.

- **HC-124 · The rebind period is 100 ticks, through a phase gate of its own**
  - Constraint: `REBIND_INTERVAL_TICKS = 100` is a **compile-time constant, not a config key**; the rebind gate is an **independent `if` in `serverTick`, placed before the particle gate and not `return`ing**; the phase formula's modulus is fixed at **100**; it **does not borrow** the particle gate's `interval <= 0` guard; `now` moves up one line from `:515` (this task's only structural change).
  - Criterion: the constant's declared value = **100**; run `boundaryParticleIntervalTicks` at **40** and at **1200**, and the rebind happens within **100 ticks** both times (`#6` and `#7` are run **as a pair**; running `#6` alone does not reveal that it has been filtered to 200 ticks); after this task is committed `b.push(` is still 60 and line-initial `.define*` is still 175.
  - Source: D-176; 42 §2.1.1; RT-51; F-249, F-250; pitfall 32.

- **HC-125 · The rebind's scan surface: `getChunkNow` only, 3×3 only, return a new list**
  - Constraint: a single method `AreaBlockEntities.collect(ServerLevel, BlockPos, int, Class<T>)`; **forbidden**: `Level#getChunk` / `getChunkAt` / `Level#getBlockEntity` (the last one loads internally); the radius reuses `RANGE_CHUNKS = 1` (**no second constant is introduced**); return a **new `ArrayList`**, do not hand out the live map from `chunk.getBlockEntities()`; filter by `Class<T>`, not by `BlockEntityType`.
  - Criterion: **1** `getChunkNow` call in that file; **0** hits for the three loading APIs; `new ArrayList` ≥ 1; `BlockEntityType` **0** hits; Java file count 145 → **146**.
  - Source: D-177; 42 §2.1.2; RT-52; F-248; pitfalls 13 / 33.

- **HC-126 · The rebind's write boundary: no core owner ⇒ no rebind, core before cultivator, <16, stop when full, four-level sort**
  - Constraint: `rebindNearby`'s **first line** is the no-owner gate (`ownerUuid == null` returns immediately, ruling #13); capacity is read only through `cultivatorCount()` / `isCultivatorListFull()`, **not `cultivators()`** (which contains `pruneCultivators`); the write order is pinned to "`addCultivator` succeeds first, then `setCore`"; `break` at `cap`; **do not call** `removeCultivator` / `pruneCultivators`; candidates sort by `distSqr` ascending → `x` → `y` → `z` (**no** `placedGameTime` level).
  - Criterion: inside the method body `pruneCultivators(` **0**, `removeCultivator(` **0**, `cultivators()` **0**; `comparingDouble` **1** time + `thenComparingInt` **3** times; repository-wide `removeCultivator(` call sites still **0** and `pruneCultivators(` call sites still **1** (`OutpostCoreBlockEntity:329`); 20 candidates with capacity 16 ⇒ `Cultivators` holds exactly **16** entries, still 16 after 300 more ticks, and the order of the existing 16 is unchanged word for word.
  - Source: D-177; ruling #13 / #14; 42 §2.1.3; RT-51, RT-53; F-249.

- **HC-127 · The original AI flag must be persisted in the entity attachment, and removed as soon as it is released**
  - Constraint: data shape `Suspend(boolean originalNoAi, int sources)` + a `NONE` sentinel, **storing no timestamp of any kind** (ruling #16); `serialize(Codec, s -> s.sources() != 0)` (`NONE` is not persisted); **do not use** `.sync(...)`, **do not call** `copyOnDeath()`; `release` is the only write exit and must end with `removeData`; the criterion for "is it suspended" is **`sources != 0`**, not `hasData`.
  - Criterion: `removeData(` exactly **1** site; in that file `deadline` / `expire` / `timestamp` have **0** hits; `/data get entity` **shows no** `neoforge:attachments.farmstorm:ai_suspend`; `Suspend` has exactly **2** fields; `registry/FarmstormAttachments.java` has exactly **1** `AttachmentType` registration, with the id literal `ai_suspend`.
  - Source: ruling #15 / #16 / #17; D-179; 42 §2.3.1 / §2.3.2; RT-54; F-252, F-253; pitfalls 35 / 36 / 39.

- **HC-128 · Five cleanup paths + the write order + "cannot observe it" does not delete the attachment**
  - Constraint: the write order is pinned to "read the original flag → write the attachment → `setNoAi(true)`"; the five paths = expiry / `/effect clear` / milk / death / chunk unload; the first three **all converge through a per-tick reconciliation reading `hasEffect`** (not one removal event is hooked); the death path **writes no `noAi`**, it only calls `removeData`; chunk unload **keeps the attachment and settles on load** (`adopt` settles the Vibration Bean first, then judges Q-Bounce); **"cannot observe it" drops only the two in-memory pieces and keeps the attachment**; **no** cap constant of "give up after N unobservable ticks" is **set**.
  - Criterion: `LivingDeathEvent` / `MobEffectEvent` **0** hits; the cap constant **0** hits; `/stop` at 80 ticks not yet elapsed ⇒ after the restart that mob's `NoAI` is **0b** and the attachment holds no `farmstorm:ai_suspend`; force-killing the process and restarting ⇒ it settles from `sources` on load (≤ **1** tick) and the attachment is deleted; packets are still **2**.
  - Source: 42 §2.3.3 / §2.3.4; A-79, A-80; RT-54, RT-55; F-253; pitfall 36.

- **HC-129 · Vibration Bean: every non-player mob in range for 80 ticks, and the constant is not a config key**
  - Constraint: `AI_SUSPEND_TICKS = 80` is a constant (making it a key would turn 175 into 177); the radius reuses **the one key shared by the trigger side and the confusion side**, `vibration_bean.triggerRadius` (default 4.0, the same strict less-than); `Mob` already excludes players, so **do not write** `!(mob instanceof Player)`; the existing four steps (player confusion / explosion sound / `consume` / grant advancement) are **unchanged word for word**; 80 ticks ≠ `vibration_bean.confusionTicks` (same value, different meaning; they may not be merged).
  - Criterion: **3** mobs inside the radius ⇒ all 3 have their AI suspended and the log's count for the round is **3**; after 80 ticks `NoAI` is back to **0b**; players only inside the radius ⇒ the behaviour is exactly the same as 0.2.0 (log count **0**); `AI_SUSPEND_TICKS = 80`; this task does not touch config (`b.push(` still 60, `.define*` still 175).
  - Source: D-180; ruling #4 / #5 / #8; 42 §2.3.6; F-254.

- **HC-130 · Q-Bounce: override `onEffectAdded` only, and the recovery criterion is `hasEffect`**
  - Constraint: `QbounceEffect` adds **one** override only, `onEffectAdded(LivingEntity, int)`; **do not override** `applyEffectTick` / `isDurationEffectTick`; the exit surface **is not done in the effect class** (21.1.213's `MobEffect` has no `onEffectRemoved`); the entry point is **idempotent** (the same source does not set it twice, and the original flag does not change when the effect is refreshed); **no** one-shot timer is **fitted** (RT-56: the effect field rescans every tick and the duration decrements).
  - Criterion: the override count = **1**; `applyEffectTick` / `isDurationEffectTick` **0** hits; run each of the three removal paths (natural expiry / `/effect clear` / milk) once, and all three restore the original flag **within 1 tick**; `sources` is always `SRC_Q_BOUNCE` (it does not carry twice).
  - Source: D-181; ruling #6 / #8; RT-55, RT-56; 42 §2.3.4; F-255; pitfall 37.

- **HC-131 · mount has exactly one truth**
  - Constraint: `QbounceMountController#isMountMode` (`:199`) only changes `private` to `public static`, **1 modifier**; **no second method**, no wrapper method; `POSE_MODE_MOUNT` (`:50`) is still the only literal and `:200` is still the only read point; the effect class and the AI table **must never** resolve `qbounce.poseMode` themselves; **do not change** the mount logic (do not touch `ACTIVE`, do not discard the mount, do not `startRiding`).
  - Criterion: the class still has **376** lines in total; `isMountMode`'s **declaration** appears exactly **1** time and its modifier is `public static`; `qbouncePoseMode` still has only **1** read point, at `:200`; with `qbounce.poseMode = mount`, a mob carrying Q-Bounce has `NoAI` constantly **0b**.
  - Source: D-181; ruling #7; 40 §0.2 term "mount mode"; 42 §2.3.5; F-251; pitfall 38.

- **HC-132 · The hostility criterion is `Enemy` ∪ `MobCategory.MONSTER`, with exactly 2 use sites**
  - Constraint: `util/Hostility#isHostile(@Nullable Entity)`, four branches (not a `LivingEntity` → false; player → false; `instanceof Enemy` → true; `getType().getCategory() == MobCategory.MONSTER` → true); **only 2 use sites** (`FarmstormExplosion#hurtCapped` and `ZangiefCaneTrap#monsterBranch`); **do not change back** the existing 5 `getEntitiesOfClass(Monster.class, …)` sites; **add no whitelist**, widen no criterion.
  - Criterion: repository-wide `Enemy` hits go from **0** to **1 file**; `isHostile` is defined **1** time; `MobCategory` hits go **2 → 3**; the 5 filter line numbers are unchanged word for word (`BlastCarrotTrap:59` / `FangTrapCactusTrap:54` / `VibrationBeanTrap:59` / `ZangiefCaneTrap:174` / `PumpkinTurret:109`); Java file count 146 → **147**.
  - Source: D-182; ruling #9; 42 §2.4; RT-57; F-257.

- **HC-133 · The multiplier applies to the final damage only, and only 2 traps take part**
  - Constraint: `blast_carrot` multiplies **the capped final damage** (after `min(raw, cap)`), not `cap`, not `explosionPower`; `zangief_cane`'s multiply point is after `amount` has been read and **before both** the `source == null` and the `amount > 0.0` checks; the log's **format string does not change**, only the value is swapped for the multiplied one; `fang_trap_cactus` and `vibration_bean` **do not take part**; players and non-hostile mobs **always get the original value**.
  - Criterion: Blast Carrot vs. a zombie **21.0**, vs. a player **7.0**; Zangief Cane vs. a zombie **22.5** (`7.5 × 3`, **not 11.25**, A-76), vs. a cow / villager **7.5**; `slamDamage` set to **0** ⇒ no `hurt`, logged damage **0.0**; the fang damage is point by point identical to 0.2.0.
  - Source: D-184; ruling #10 / #11; 42 §2.5.3; A-76; RT-58; F-259, F-260.

- **HC-134 · The explosion's 9th parameter `trapSourced`, the 8-parameter overload delegating `false`, 6 non-trap sites with zero changes**
  - Constraint: `FarmstormExplosion` gains `boolean trapSourced` (after `damageCap`) + **keeps the 8-parameter signature as an overload** (whose body is one line delegating `false`); **only** `BlastCarrotTrap:84-86` is changed to pass `true`; **open no new damage source** (`trap_blast` would take `damage_type` 3 → 4 and 97 → 98, and would also change the semantics of `#minecraft:is_explosion`); **do not look the crop up from `BlastRule`**; **do not judge from `damageSource.getEntity()`**.
  - Criterion: repository-wide `new FarmstormExplosion` is still **7** sites, **only 1** passes the 9th parameter, and the other **6** argument lists still stop at `damageCap` (`MelonGrenadeProjectile:42` / `MelonSliceProjectile:40` / `MolotovProjectile:94` / `ToxicUbiServerProjectile:43` / `UbiServerProjectile:41` / `LuckyBerryRoller:212`); constructor declarations are exactly **2**; `damage_type` is still **3**; run the multiplier at **1.0** and at **10.0**, and the 6 readings are **identical**.
  - Source: D-184; 42 §2.5.2; A-72; RT-57, RT-58; F-259; pitfalls 34 / 24 / 25.

- **HC-135 · The tool must actually read the new key: documents and parser in the same commit**
  - Constraint: `tools/verify_project.py` gains `expected_config_keys_v021`, with the four contracts per 42 §4.1.1 (bound the range with `^#### 4\.1\.1\b` ~ `^#### 4\.1\.2\b`; exactly **7** columns per row; column 2 matching `` ^`([A-Za-z0-9_]+\.[A-Za-z0-9_.]+)`$ ``; column 6 starting with "增" / "删"; the row-count assertion **== 1**; joined into `main()`'s `union`); the expected number goes **175 → 176**; **`EXPECTED_COUNTS` does not move** (the registration surface is still 97).
  - Criterion: both lines of the config section of `python tools/verify_project.py` read **176**; the new parser function returns **exactly 1** key for `docs/zh/42-TECH-0.2.1.md` §4.1.1, `["trap.damageMultiplier"]`; a counter-example (a temporary copy in `$env:TEMP` edited to 2 rows) ⇒ the tool **goes red and carries the file name + line number**, not a silent pass.
  - Source: D-185; A-75; 42 §4.1.1; RT-60, RT-60b; F-261; pitfall 41.

- **HC-136 · The version string is 6 sites by line, not 4**
  - Constraint: `0.2.0` → `0.2.1` in **6 literals** in all — 1 at `gradle.properties:11` + **5 lines** in the two `.cmd` files (`一键构建.cmd:5` (header) / `:11` / `:12`; `检查环境.cmd:5` (header) / `:14`); the three lines `minecraft_version` / `neo_version` / toolchain **do not move by a character**; the version number has only one source, `gradle.properties:11`.
  - Criterion: `0.2.0` has **0** hits across `gradle.properties` + the two `.cmd` files; `0.2.1` has **1 + 3 + 2 = 6** hits; `build/libs/` holds `farmstorm-0.2.1+mc1.21.1.jar` and `-sources.jar`, with **0** `farmstorm-0.2.0*` leftovers.
  - Source: D-173; A-70; 42 §3 #1 ~ #5; F-247; pitfall 31.

- **HC-137 · Red lines and the "do-not-touch surface": not one place may move**
  - Constraint: **Mixin 0** (`src/` standard: no `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor`, and no mixin plugin added to `build.gradle`); packets **2**, protocol `"2"`, nothing new in this version; the 12-class assertion at `Farmstorm.java:167-172`; the 5 `Monster` filters (the trigger surface); the vanilla fang spawning in `FangTrapCactusTrap:87-109`; `vibration_bean`'s 0 damage; `blast_carrot.maxDamage` (7) and `zangief_cane.slamDamage` (7.5); `removeCultivator` (0 call sites); the 3 `.dat` files under `world/data/`; `docs/zh/00` ~ `43`, `docs/en/*`, `.work/*`.
  - Criterion: `@Mixin` and `*.mixins.json` each **0** under `src/`; `.playToClient` **2**, `PROTOCOL_VERSION = "2"`; the line numbers above show **0 changed lines** in `git diff`; `git status` shows 0 changes under `docs/` and `.work/`. ⚠️ Repository-wide there is **1** `*.mixins.json` (`.scratch/p25a/src/neoforge.mixins.json`, an experiment leftover, outside `src/`, not part of the build) — the standard says "under `src/` = 0".
  - Source: D-174, D-186; 42 §1.4, §3 #27 / #28, §9 item 10; 43 §0.4 item 2; F-266.

## 2. Prohibited Behaviour N-115 ~ N-126 (12 entries)

Each one can be decided by one command, one place in `git diff` or one reading; these are not attitude requirements. The "why" is one sentence only, enough for you to judge the boundary.

- **N-115** Never wire the rebind after the particle gate, and never reuse the particle gate's `interval` guard or period.
  - Criterion: the rebind gate is an independent `if`, textually **before** `:516-518`; `interval` does not appear in the rebind method; with `boundaryParticleIntervalTicks` set to 1200 the rebind still happens within 100 ticks.
  - Why: wired behind it, the actual period is `LCM(40, 100) = 200` ticks and drifts with 20 ~ 1200 (D-176 supplement, 42 §2.1.1).

- **N-116** Never add a config key for the rebind (period / scan radius / entries per round / candidate cap).
  - Criterion: `b.push(` is still **60**, line-initial `.define*` is still **175** (before the window closes) / **176** (after it closes); `REBIND_INTERVAL_TICKS` is a `private static final int`, not a `ModConfigSpec`.
  - Why: this version's config adds exactly +1 key and it is already taken by `trap.damageMultiplier`; one more makes **177**, in direct conflict with D-185's 176 (41 appendix A row 7).

- **N-117** Never call `removeCultivator` / `pruneCultivators` / `cultivators()` on the rebind path.
  - Criterion: inside `rebindNearby`'s method body all three have **0** hits; repository-wide `removeCultivator(` call sites are still **0** and `pruneCultivators(` call sites still **1** (`:329`).
  - Why: "bind first, unlink after" creates a second core; `pruneCultivators`'s `setChanged` interleaved with the rebind makes this round's already-bound coordinates duplicate or go uncounted (D-177, 42 §2.1.3 item 3).

- **N-118** Never enumerate with `getChunk` / `getChunkAt` / `Level#getBlockEntity`, and never iterate the live map directly.
  - Criterion: **1** `getChunkNow` in `AreaBlockEntities` and **0** hits for the other three; `new ArrayList` ≥ 1.
  - Why: one loads chunks (a core drags 9 chunks into memory every 100 ticks), the other throws `ConcurrentModificationException` (RT-52, 42 §2.1.2).

- **N-119** Never store the original AI flag in an in-memory table, a data component or an "expiry instant".
  - Criterion: `Suspend` has exactly **2** fields (`originalNoAi` / `sources`); `deadline` / `expire` / `timestamp` **0** hits; `data_components` is still **1**.
  - Why: an in-memory table does not survive a crash, a data component takes 97 to 98, and a timestamp makes "the window never spans a save" need a whole extra layer of fallback (ruling #15 / #16, D-175, 42 §2.3.1).

- **N-120** Never leave the attachment undeleted after recovery, and never treat "cannot observe it (chunk unloaded)" as the entity being dead.
  - Criterion: `release` ends with `removeData` (exactly 1 `removeData(` in the whole file); `sweep`'s "cannot observe it" branch drops only the two in-memory pieces and does not touch the attachment.
  - Why: delete the attachment and the original flag is gone; an entity persisted with `NoAI:1b` has no AI for good (RT-54's two entry points).

- **N-121** Never fit Q-Bounce with a one-shot timer, and never resolve `qbounce.poseMode` yourself.
  - Criterion: `QbounceEffect` has **1** override; `applyEffectTick` / `isDurationEffectTick` **0** hits; repository-wide `qbouncePoseMode` still has only **1** read point, at `:200`.
  - Why: the field rescans every tick and writes "field length − ticks elapsed", so a timer gets reset or fires early; mount has exactly one truth (RT-56, ruling #7, 42 §2.3.5).

- **N-122** Never multiply blindly at `FarmstormExplosion:179`, and never touch those 6 non-trap call sites.
  - Criterion: `new FarmstormExplosion` is still **7** sites, only 1 passes the 9th parameter; the 6 argument lists are unchanged word for word; the readings at multiplier 1.0 / 10.0 are identical.
  - Why: all 7 share the one `farmstorm:crop_blast`; multiplying indiscriminately puts Melon Grenade, Molotov and the rest at ×3 as well (RT-57, 42 §2.5.2).

- **N-123** Never touch the 5 `Monster` trigger sites, never change the vanilla fangs with Mixin, and never give Vibration Bean damage.
  - Criterion: the 5 line numbers are unchanged word for word; `@Mixin` 0; `FangTrapCactusTrap:87-109` and `vibration_bean`'s 0 damage are unchanged word for word.
  - Why: touching the trigger surface is exactly the knock-on that RT-57 forbids; giving the fangs a different damage source steps on the red line and ruling #11 at once (D-182, D-186).

- **N-124** Never write "all traps ×3" or "Zangief Cane 11.25" into any document, comment or acceptance.
  - Criterion: in this version's output `所有陷阱` has **0** hits; every Zangief Cane reading is **22.5**; `4 个里 2 个参与` appears in the acceptance record.
  - Why: plan's "all" has been narrowed by ruling #11, and an acceptance written to "all" necessarily misjudges; 11.25 = `7.5 × 1.5`, which contradicts the `7.5 × 3` arithmetic (RT-58, A-76).

- **N-125** Never write the key into the documents without adding the parser, and never write the two totals 176 / 338 inside the window.
  - Criterion: acceptance mentions of `176` appear only in the window-closing task (F-262) and the tool task (F-261); mentions of `338` appear only in F-263; `expected_config_keys_v021` and the document are in the same commit.
  - Why: the tool does not read document 42 today, so a key written into a document is as good as not added (A-66 / RT-60b's false green); writing a total inside the window writes a criterion that is forever red (43 §0.3, A-74).

- **N-126** Never "fix" a registered pre-existing defect on the way.
  - Criterion: the "139 个配置项" at `ModLangProvider:237`, the "3 个方块" at `FarmstormBlocks:43`, `removeCultivator`'s 0 call sites, `.scratch/`'s `neoforge.mixins.json`, Vibration Bean's "does not trigger in creative / spectator yet still gets stunned", the tooltip's urge to draw itself, and `verify_project.py`'s extraction logic — all seven show an empty `git diff`.
  - Why: A-66, A-73 and document 42 §9's 16 differences all hold because of "register only, do not fix"; fixing on the way mixes out-of-scope changes into this version (D-174, the relay of N-111).

## 3. Fact Card

Only source facts that "get it wrong if not written" are collected. **A name without a `file:line` is never written** — inventing a class name from memory and putting it into code is a compile error.

| # | Fact | Value / current state (baseline = 0.2.0 released state) | Evidence |
|---|---|---|---|
| 1 | The 97-object registration surface's 12-class assertions are hard-coded in the startup self-check | `33 / 27 / 8 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 2 = 97` | `Farmstorm.java:167-172`; the log string `:173`; 42 §1.2 |
| 2 | The 13th category outside the count: attachment types, **1** | `farmstorm:ai_suspend` (`AttachmentType<Suspend>`), baseline 0 → 1, **not counted in the 97**; the tool neither checks its existence nor counts it | 42 §1.2.1; ruling #17; A-73 |
| 3 | server config currently **175 keys / 60 groups** | line-initial `.define*` = 175 live (plus 1 comment line at `:935` and 1 mention at `:229`); `b.push` = `b.pop` = 60 | `FarmstormConfig.java:289-995`; 42 §1.3 |
| 4 | The on-disk `run/config/farmstorm-server.toml` holds **stale values** | 801 lines / 60 sections / **175 keys**; the repository also holds dozens of old `run-*/config/` copies (about 121 keys / 36 sections) | 42 §9 item 2; 43 §0.1 |
| 5 | CLIENT config: **3 keys**, in a file of its own | `hud.enabled` / `hud.x` / `hud.y`; **not counted in** 176, but **counted in** `farmstorm.configuration.*` | `FarmstormClientConfig:29-43`; 42 §1.3 |
| 6 | Language keys **337 × 2**, with equal Chinese and English key sets | `farmstorm.configuration.*` = **178** (= 175 + 3), `tooltip.farmstorm` = **20**, `config(entries, …)` **178** times | the two `lang/*.json` files; 42 §1.3 |
| 7 | Language keys have **no automatic gate** | the tool's section 3 compares the zh / en key sets for equality only; it **does not verify the number 338** | 42 §9 item 4; 43 §0.3 |
| 8 | `src/main/java` has **145** Java files; this version adds +4 = **149** | new: `util/AreaBlockEntities`, `util/Hostility`, `combat/MobAiSuspension`, `registry/FarmstormAttachments` | 42 §1.1; A-78 |
| 9 | `Farmstorm.java`'s constructor currently has **11** `.register(modBus)` lines | `:46,47,48,49,50,52,53,54,55,57,58`; `damage_type` is data-driven and has no register ⇒ **12** after the attachment | 42 §3 #29; A-78 |
| 10 | `serverTick` currently does only three things | early exit → the particle gate (`:516-518`, `interval` default **40**, range **20 ~ 1200**, `:939-941`) → boundary particles (`:519`) + a log every 30 rounds (`:521-524`, `TICK_LOG_ROUNDS = 30` at `:91`) | `OutpostCoreBlockEntity:507-525` |
| 11 | The affected area = **3×3 chunks**, and in normal operation **all of them are loaded** | `RANGE_CHUNKS = 1` (`:75`) ⇒ 9 chunks, 48 blocks a side; the core's own ticket `DISTANCE = 1` ⇒ the centre ticks, the 8 around it are FULL | `:454-465`; `CoreChunkTickets:17-18,43-44`; 42 §2.1.2 |
| 12 | Binding currently has **2** write points | placement `CultivatorBlock:99-100`; migration `OutpostCoreBlockEntity:686-690`. `removeCultivator` (`:366`) has **0** call sites; `pruneCultivators` has **1** (`:329`) | BRIEF §1.1; 42 §2.1.3 |
| 13 | Repository-wide `setNoAi` appears **2** times | `WildHunterAura:92` (sets true) / `:119` (sets false); `:91` is the `isNoAi()` test, **not** a write point | 42 §2.3.2; A-81 |
| 14 | `MobEffect`: **one present, one absent** | `onEffectAdded` exists (`MobEffect.java:86`, called only in the "no such effect before" branch, `LivingEntity.java:957-961`); **`onEffectRemoved` does not exist** | 42 §8 ③ |
| 15 | `QbounceEffect` is 20 lines with **0** overrides; `isMountMode` is `private` | `QbounceEffect:15-19`; `QbounceMountController:199` (`:200` is the only read point, `POSE_MODE_MOUNT` at `:50`) | BRIEF §1.4; 42 §2.3.5 |
| 16 | The current hostility standard is **only** `Monster` (5 sites) | `BlastCarrotTrap:59` / `FangTrapCactusTrap:54` / `VibrationBeanTrap:59` / `ZangiefCaneTrap:174` / `PumpkinTurret:109`; `Enemy` / `isHostile` / `MobCategory.MONSTER` have **0** hits (`MobCategory` appears only twice, both `MISC`) | BRIEF §1.5; 42 §2.4 |
| 17 | `FarmstormExplosion` has **7** constructor calls, all using `farmstorm:crop_blast` | 1 trap (`BlastCarrotTrap:84`) + 5 projectiles + `LuckyBerryRoller:212`; the formula at `:178`, `hurt` at `:179` | 42 §2.5.2 |
| 18 | Two red lines | Mixin **0** (**the `src/` standard**; `.scratch/p25a/src/neoforge.mixins.json` is an experiment leftover); packets **2**, protocol `"2"`, nothing new in this version | 42 §1.4, §9 item 10; D-186 |
| 19 | The save surface barely moves in this version | the **3** `.dat` files under `world/data/` show 0 changes; the only addition is a compound of ≤ 40 bytes under the entity's `neoforge:attachments`, not written when `sources == 0` | 42 §5.1, §5.3, §5.5 |

> Two footnotes: ① `run/config/farmstorm-server.toml`'s nested sections are **indented**, so counting sections must use `^\s*\[`; `^\[` only counts the top level. ② the default `Get-Content` on this machine reads BOM-less UTF-8 Chinese as GBK and **swallows line breaks** (`FarmstormConfig.java` reads as 1095 lines instead of 1131, `OutpostCoreBlockEntity` as 668 instead of 782), and every line number goes wrong with it — always use `[IO.File]::ReadAllLines($p, UTF8)`.

## 4. The Pitfall Table (41 entries)

Items **1 ~ 31** are **inherited from document 38** (the 0.2.0 version), not one deleted, numbered one-to-one and traceable; where their numbers have been rewritten by this version (action surface 31 rows, acceptances 31 entries, boundaries 36 entries, config 175 → 176, language keys 337 → 338, etc.) they are written to this version's standard. **Item 20 is a meta-rule** — what it constrains is not the code but the criterion itself, and its standing differs from every other item. Items **32 ~ 41** are new in this version, and each traces to a ruling (A-70 ~ A-81) or to a technical fact in document 42.

| # | Pitfall (how the implementer steps on it) | What happens if stepped on | Correct approach | Basis |
|---|---|---|---|---|
| 1 | Changing a constant at only one reader | the two behaviours fork, or compilation fails | `grep -rn` the whole repository for readers before touching a constant; this version's three constants each have their own reader: `REBIND_INTERVAL_TICKS` (`rebindDue`), `AI_SUSPEND_TICKS` (Vibration Bean), `trapDamageMultiplier` (the two multiply points) | the relay of document 38 pitfall 1 (A-30); 42 §2.1.1, §2.5.3 |
| 2 | Changing a texture only halfway, or re-running the icon generator | silently rewrites historical artifacts | this version has **0 changes to existing textures and 0 new PNGs**; the generator must not be re-run even once | document 38 pitfall 2 (A-32); D-174 |
| 3 | Using `setChunkForced` / `TicketController#forceChunk` to save trouble | writes `chunks.dat`, so the chunk stays force-loaded after the server stops | this version changes the ticket layer by **0**; the rebind uses `getChunkNow` only and skips what it cannot get | document 38 pitfall 3 (A-38); 42 §2.1.2 |
| 4 | Taking the iteration order of a `HashMap` / `List` as the order | the same save binds different batches on two runs (`getBlockEntities()` returns a live `HashMap`) | sort the candidates explicitly `distSqr` → `x` → `y` → `z`; there is **no** `placedGameTime` level | document 38 pitfall 4 (A-39 / A-47); ruling #14; 42 §2.1.3 |
| 5 | Hard-coding a config default as a constant | after the config changes, the behaviour contradicts the configuration | among this version's **new and affected numbers** only two read config: `cultivatorCapacity` (16) and `trap.damageMultiplier` (3.0); 100 / 80 are constants, and 7 / 7.5 still read their own original keys | document 38 pitfall 5 (A-33); 41 appendix A rows 4, 7 |
| 6 | Writing only the tick face, yet asserting the load face | the assertion is necessarily false | the rebind asserts only "no new chunk has been pulled into memory" (no loading line in the log); it asserts nothing about ticket level / load count | document 38 pitfall 6 (A-43); 42 §7 #9 |
| 7 | Adding to a determination function a parameter its signature does not have | it ripples into existing call sites | `isHostile(Entity)` is fixed; `isMountMode()` changes 1 modifier only and adds no wrapper method | document 38 pitfall 7 (A-45); 42 §2.4, §2.3.5 |
| 8 | Trusting the tool's green light right after the change | the expectation set comes from the old standard, so a green light can only come from a missed check | add the parser per HC-135 first, then cite the tool's conclusion; attach its `git diff` when citing | document 38 pitfall 8 (A-40 / A-66); 42 §4.1.1 |
| 9 | Treating one group of behaviours as two categories | the acceptance is short of items and two criteria push against each other | this version's three pairs may not be mixed: "suspend AI" ≠ "stop moving"; "non-player mob" ≠ "hostile mob"; "mount skip" belongs to Q-Bounce only (Vibration Bean is not exempt) | document 38 pitfall 9 (A-31); 40 §0.2; 42 §2.3.7 |
| 10 | Squeezing an overflow item into a fixed row count | the layout and the count no longer agree | language keys: 15 prefix buckets, +1 allowed but **no new bucket opened**; items outside the action surface's 31 rows are **recorded separately** and not squeezed into the 31 | document 38 pitfall 10 (A-34); the note under 42 §3's table; 43 table D |
| 11 | Writing to a clause that is void / superseded | the standard and the acceptance no longer agree, and it cannot be looked up | check the status before citing: D-165's two sentences are superseded by ruling #12; document 38's N-106 "never rebind at runtime" is **void as a whole**; A-70 ~ A-81 are all current | document 38 pitfall 11 (A-36 / A-44 / A-47); 40 §5; 43 §5 |
| 12 | Writing "this method has only one call site in the whole repository" into the acceptance | necessarily red after implementation, and you cannot tell a defect from the expectation | switch to a countable quantity: file count / key count / hit count / log line / `/data get` reading | document 38 pitfall 12 (A-37); 43 §0.1 |
| 13 | Writing a separate branch for an "unloaded block" and calling `getChunk` on the way | dead code + a chain-load of a batch of chunks | use `getChunkNow` only; when it cannot be obtained, skip — **write no state, print no log** | document 38 pitfall 13 (A-35 / A-60); RT-52 |
| 14 | Letting two sets of layouts / two decompositions coexist | two people compute two different numbers and acceptance has to pick one | recognise one set each: 54 slots / `IMAGE 176×222` / `generic_54.png`; **16 is the quota, 54 is the slots** | document 38 pitfall 14 (A-41); 41 §0.4 |
| 15 | Stopping at the previous version's action-surface row count | a whole row of changes is missed | this version is document 42 §3's **31 rows** + **2 items outside the table** (the tool's expected number, the two ledgers); match row by row | document 38 pitfall 15 (A-42); 42 §3; 43 table A |
| 16 | Taking the "number of acceptances" and the "number of boundaries" as a one-to-one correspondence | wrongly assuming "no assertion = no boundary", so the boundary list is quietly cut | **31** acceptances (42 §7) and **36** boundaries (41 §9 B-01 ~ B-36) are **not required** to correspond one-to-one | document 38 pitfall 16 (A-46); 41 §9; 42 §7 |
| 17 | Missing one category when counting language keys | the total is off by N keys, and the Chinese and English key sets end up unequal | add and subtract by the 15 prefix buckets: `179 + 36 + 33 + 27 + 20 + 13 + 9 + 6 + 4 + 3 + 3 + 0 + 2 + 1 + 2 = 338` | document 38 pitfall 17 (A-48); 42 §4.3; 43 table D |
| 18 | "Fixing" a registered known limitation on the way | out-of-scope changes get mixed into this version | document 42 §9's **16** differences, 40 §7's **7** leftovers and pre-existing defects outside 42 §7 are all left unfixed | document 38 pitfall 18 (A-49); D-174; N-126 |
| 19 | Counting the toml's sections with `^\[` | nested sections are indented ⇒ only the top level is counted | count sections with `^\s*\[` (**60**), and count key lines separately with `^\s*[A-Za-z0-9_]+\s*=` (**176**) | document 38 pitfall 19; 42 §4.2; 43 §0.1 |
| 20 | **The criterion's own failure (meta-rule)** | `^\[` missing the indented sections, a grep missing the prefix hitting 0 forever, a wrong toml copy reading 121 forever — all belong to "the criterion always passes or always fails" | run every criterion once **before** the change and report the measured value, then once after; only when both can be computed does the criterion stand. Three instances this version is bound to step on: `grep -c 'trap\.damageMultiplier' docs/zh/42-TECH-0.2.1.md` hits **0** forever before the document is written; a grep for `getChunkNow` without the path prefix hits **0** forever; counting an old copy under `run-*/config/` gives **121** keys instead of 175. **This item outranks every other row of this table**: every other row is an instance of it | A-52 / A-57 / A-58; 42 §9 item 2; 43 §0.1 |
| 21 | Tool false green | `97` objects, but the tool looks at 12 groups of ids only; the attachment type it **does not look at at all** | the sign-off must say "attachment types **1**, not counted in the 97, and the tool neither checks its existence nor counts it" | document 38 pitfall 21 (A-66); ruling #17; 42 §1.2.1 |
| 22 | Writing a total as an assertion inside the in-between window | inside the window the actual values are 176 / 338 while the assertion still wants the "window-closed value" ⇒ every one has to be recomputed | the window = **F-258 opens → F-263 closes**; inside it write only "this key exists" + "the `[trap]` section has 4 keys" + "the whole file is 175 or 176 (an interval reading)" | document 38 pitfall 22; 43 §0.3; A-74 |
| 23 | Blocking / changing only one of two paired paths | half the change looks green | this version's three pairs must go in pairs: **key ∧ translation** (F-258), **document ∧ parser** (F-261), **wiring ∧ the `CultivatorBlock:73-84` comment** (F-250) | document 38 pitfall 23; A-71 / A-75; 42 §4.1.1 |
| 24 | The third entry: counting the "non-trap" sites wrong | writing 6 as 5 loses the core of the acceptance on the spot | `new FarmstormExplosion` is **7** sites: 1 trap + **6** non-trap (`MelonGrenadeProjectile:42` / `MelonSliceProjectile:40` / `MolotovProjectile:94` / `ToxicUbiServerProjectile:43` / `UbiServerProjectile:41` / `LuckyBerryRoller:212`) | document 38 pitfall 24; 42 §2.5.2 |
| 25 | Missing a construction point (nothing warns at compile time once the 9th parameter is added) | the 6 sites pass silently through the overload, so you cannot see whether they are ×3'd | prove it with readings: the 6 argument lists still stop at `damageCap`, and the readings at multiplier **1.0** and **10.0** are identical | document 38 pitfall 25; A-72; 42 §7 #20 |
| 26 | The `usesBlockModel` kind of "something was added and the wiring forgotten" | purple-black squares / a wrong assertion | this version has no new block and no new item; the counterpart is **4 new classes that do not change the registration surface** — `FarmstormAttachments` has 1 registration call but it lands outside the count, so do not touch `Farmstorm.java:167-172` | document 38 pitfall 26; 42 §1.2's ⚠️; ruling #17 |
| 27 | Thinking code can "pause" an mcmeta animation | it cannot; players see it flipping while idle | this version has no animated texture; the counterpart is **not using Mixin / events to change the vanilla `EvokerFangs`** to get ×3 fangs | document 38 pitfall 27; ruling #11; D-186 |
| 28 | Writing a timing criterion early | necessarily red | this version has two timings: `run/config/farmstorm-server.toml` must be **regenerated by running `runServer` once first** before counting 176; "the attachment is deleted" can only be read after `release` | document 38 pitfall 28; 42 §4.2, §7 #28 |
| 29 | A hand-written list / using enumeration order as the criterion | two sources of truth, and results are not reproducible | the candidate order may not be written as "chunk traversal order / random"; the 6 non-trap sites must be **named one by one**, never "the rest" | document 38 pitfall 29; ruling #14; 42 §2.5.2 |
| 30 | Added but unobtainable (invisible registration) | "what the tool does not count is as good as not registered" | the 13th category must be registered explicitly in **three places**: 42 §1.2.1 + F-252 acceptance 5 + F-266 sign-off | document 38 pitfall 30; ruling #17; A-73 |
| 31 | The two `.cmd` files' jar names and header lines not changed in sync | "一键构建" copies a **non-existent file**, and "检查环境" reports `jar: 缺失` outright | `0.2.0` → `0.2.1` in **6 places**: `gradle.properties:11` + `一键构建.cmd:5,11,12` + `检查环境.cmd:5,14` | document 38 pitfall 31; A-70; D-173 |
| 32 | **Reusing the particle gate for the rebind gate** (wired after `:519`, or borrowing its `interval` guard) | the actual period becomes `LCM(40, 100) = 200` ticks, drifting with `boundaryParticleIntervalTicks` (20 ~ 1200) — "every 100 ticks" fails on the spot | an independent `if`, placed **before** the particle gate, **not** `return`ing; modulus fixed at 100; do not borrow the `interval <= 0` guard. Criterion: run `#6` and **`#7` as a pair**; with the particle interval pulled to 1200 the rebind must still happen within 100 ticks | 42 §2.1.1; D-176; RT-51; HC-124 |
| 33 | Thinking "most of the 3×3 is not loaded" | writing "scan it once in a while" into logs and acceptance ⇒ "rebind within 100 ticks" is taken as a fluke, or conversely "skip" gets a state and a log line | the core's own ticket `DISTANCE = 1` ⇒ **all 9 chunks of the 3×3 are loaded in normal operation**; skipping an unloaded chunk is only a **defensive branch** (ticket not yet effective / restart recovery window / no-owner core), and on a skip you **write no state and print no log** | 42 §2.1.2's 📌; RT-52; `CoreChunkTickets:17-18,43-44` |
| 34 | Multiplying blindly at `FarmstormExplosion:179` | **the 6 non-trap explosions get ×3 as well** (Melon Grenade / Melon Slice Bomb / Molotov / Ubi Server / Toxic Ubi Server / Lucky Berry) — all 7 share the one `crop_blast` | add the 9th parameter `trapSourced` + keep the 8-parameter overload delegating `false`; only `BlastCarrotTrap:84-86` passes `true`; use "the 6 readings are identical at multiplier 1.0 and 10.0" as the decisive reading | 42 §2.5.2; A-72; RT-57; HC-134 |
| 35 | Storing the original AI flag in an **in-memory table** | a server stop / crash loses the original flag ⇒ the entity persists with `NoAI:1b` and has no AI for good (the old RT-54 shape coming back) | persist it in the entity attachment `farmstorm:ai_suspend` (`Suspend(originalNoAi, sources)`), and `removeData` on recovery; memory keeps only the `Set<UUID>` index and Vibration Bean's expiry tick | ruling #15; 42 §2.3.1; HC-127 |
| 36 | **Forgetting to delete the attachment** after recovery | the attachment never disappears ⇒ every load runs a wasted pass and `originalNoAi` becomes less and less trustworthy; the next suspension reads a stale record and recovery writes the wrong value | `release` is the only write exit and must end with `removeData`; the existence criterion looks at `sources != 0` only (not `hasData`); a `NONE` with `sources == 0` is not persisted | ruling #15; 42 §2.3.2; RT-54; HC-127 / HC-128 |
| 37 | Fitting Q-Bounce with a **one-shot timer** | the field rescans every tick and the duration decrements tick by tick ⇒ the timer gets reset (never recovers) or recovers while the effect is still on | the entry surface hooks `onEffectAdded` only (called only in the "no such effect before" branch); the exit surface reads `hasEffect` every tick; **set no** "recover after N ticks" constant of any kind | RT-56; 42 §2.3.4; HC-130 |
| 38 | Resolving `qbounce.poseMode` yourself | one thing with two truths: change the config and the two criteria fork; you may also touch the mount logic on the way | call `QbounceMountController#isMountMode()` only (`:199` changed to `public`, 1 modifier); `POSE_MODE_MOUNT` is still the only literal and `:200` still the only read point | ruling #7; 40 §0.2 term; 42 §2.3.5; HC-131 |
| 39 | Treating the attachment as `data_components` | misjudging the registration surface 97 → 98 (or the reverse: thinking "it got around D-175 under another name") | the attachment is attached to `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` (**the 13th category outside the count**, 97 unchanged); `data_components` is still **1**; both sentences must be written together | D-175; ruling #17; 42 §1.2.1; HC-121 |
| 40 | Changing the rebind but **not** the comment at `CultivatorBlock:73-84` | leaves a window of "the comment says it cannot, the code does it anyway" (that sentence says "no automatic claiming ... placing a core afterwards will not bind it") | comment and wiring in **the same commit** (A-71); `:81-83` are rewritten to agree with the periodic rebind, the three-criterion note at `:74-79` is kept; the `setPlacedBy` line does not change | D-177; A-71; 42 §3 #11; F-250 |
| 41 | Writing the key into the documents only, **without** the `expected_config_keys_v021` parser | the tool **can never read** that key (it reads 6 fixed documents today and does not read document 42) ⇒ the false green stays, and neither an extra key nor a missing one is ever noticed | the document's §4.1.1 table and the parser go in **the same commit**; the parser follows the four contracts, and a counter-example must go red with a line number | 42 §4.1.1; A-75; RT-60b; HC-135 |

> Landing method: write each criterion's "measured value before the change" into the task report, and report once more after the change; a "passed" without a number is treated as unverified (item 20).

## 5. The Boundary Between Fact and Inference

**What must be checked against the source** (if you cannot find it, you may not write it into code, comments or a report's assertion):

| Category | Where to look | If you cannot find it |
|---|---|---|
| Any `file:line` | confirm on the spot with `Select-String` / by reading the file what that line **is now** (the UTF-8 reading) | do not write a comment from a citation in 40 / 42 — line numbers drift (42 §9 item 12) |
| API signatures and runtime behaviour | `neoforge-21.1.213-sources.jar` in the `.tools` cache; each of document 42 §8's **10 items** has "how far it has been verified + the way out" | write the minimal case and get it running first, then touch the main line (the relay of N-114) |
| Current counts | count them yourself: `run/config/farmstorm-server.toml`, the two `lang/*.json` files, the Java file count under `src/main/java`, `Farmstorm.java`'s assertions | do not copy a number from `BRIEF` / 42 §1 as "the value now" |
| Save structure | after stopping the server, read the region / `world/data/`, or `/data get block <pos>` / `/data get entity <mob>` | do not guess field names or key names |

**What may be cited from documents**: the conclusions of D numbers and A numbers, the "set as" column verbatim of rulings #1 ~ #17, document 41 §9's B numbers and behaviour standards, document 42's class design and fallbacks, document 43's F numbers and dependencies. Every citation carries a number (`D-176`, `A-72`, `ruling #12`, `42 §2.5.2`, `F-259`, `B-19`).

**Three disciplines**:

1. **A number may come from only four places**: document 40's D entries and 17 rulings, `BRIEF`'s facts, a measurement at a `file:line`, or arithmetic on two values already given (`175 + 1 = 176`, `337 + 1 = 338`, `97 + 0 = 97`, `LCM(40, 100) = 200`, `7.5 × 3 = 22.5`, `7 × 3 = 21`). If you need a new value, put it into "open questions" and do not decide it yourself.
2. **When unsure, write it into "open questions"**; do not fill the gap with "probably / usually / how the industry does it", and do not use "leave it for now, change it later" as a transition (it leaves an irreversible trace in the save).
3. **Inference must be marked**: in code comments, anything "not written in the documents but inferred by me" starts with "推断：" (Inference:) and gives the reasoning basis; the same in reports. An unmarked inference will be cited as fact by the next person.

## 6. Handling Conflicts

**Three iron rules**: ① a conflict between documents ⇒ **document 40 wins** (it describes itself as the single source of truth; 41 / 42 / 43 only refine it); ② a conflict between source and document ⇒ **report it**, write it into the report's "open questions", with the `file:line` and the measured value; ③ **do not pick a winner yourself** — do not implement whichever version you like, and do not write back into 40 / 41 / 42 / 43 (they are frozen).

### 6.1 Known Conflicts (17 rows; read this table before anything else)

| # | Conflict | Each side's original wording / location | Handling |
|---|---|---|---|
| 1 | The number of version-string sites | 40 §3.2 row 2 and §7 ⑤ write "**4** version strings in all" | measured **6** (gradle 1 + the two `.cmd` files' 5 lines); write **6** (A-70) |
| 2 | The header line's registration source and its line number | 42 §9 item 1 says "document 40 says `BRIEF` §3.4 row 2 **is not listed** in the header line", and says that line sits at "`BRIEF` **line 183**" | 40's D-173 supplement and §7 ⑤ both write "**already** listed together with `BRIEF` §3.4 row 2 as must-change"; that line is measured at **line 190** ⇒ the header line **changes too**; 42's two sentences are doubly distorted (the conclusion stands) |
| 3 | The Java file count | 42 §1.1's `145 + 4 = 149` against 42 §7 #1's "**148**" | write **149** (A-78) |
| 4 | The number of `register(modBus)` | 42 §3 #29 writes "the **12** register block"; measured, the constructor has only **11** lines (`damage_type` has no register) | 11 → **12** (A-78) |
| 5 | The measure word for `new Slot(` | 42 §7 #13 writes "**54 + 36 times**" | measured **3 call sites / 90 slots**; write "3 call sites / 90 slots" (A-77) |
| 6 | Zangief Cane's reading | 42 §7 #19 writes "**11.25** (7.5 × 3)"; 41 §6.5 and §7 row 26 write **22.5** | write **22.5** (the arithmetic in the parentheses contradicts the value, A-76) |
| 7 | How to clean up on chunk unload | 41 §4.5 / §5.5 write "restore to the original flag first + delete the record"; 42 §2.3.3 path ⑤ writes "**must not** restore on unload ... this version handles it as 'settle on load'" | follow document 42 (A-80): "keep the attachment, settle on load" |
| 8 | Whether "cannot observe it" needs a cap | 42 §6's RT-54 mitigation (c) writes "**a 1200-tick cap**"; 42 §2.3.4 states outright "**no** such cap constant is **needed**" | follow §2.3.4: **set no** cap (A-79) |
| 9 | The number of `setNoAi` sites | 40 D-179's basis writes "repository-wide `setNoAi` has only **3** sites"; `BRIEF` §1.5 and the source both have **2** (`:91` is an `isNoAi()` test) | write **2** sites (A-81's conclusion is right, but it also lists `BRIEF` as a side writing "3", so it is registered here) |
| 10 | The carrier of the original flag | 40 D-179's body and §0.2's term "original flag" write "the table is an **in-memory table**: not persisted" | ruling #15 changes it to **persisted**; those two sentences of document 40 are superseded (A-81) |
| 11 | The number of rulings and their description | 40 §2 note 3 says `BRIEF` §2's title "still says 12"; 42 §9 item 6 says row 5 says "9" and the title "once said 11" — both are the state of an **older `BRIEF`** | the current `BRIEF`'s title, row 5 and the note under the table **all already say 17** (rows #1 ~ #17 = 17 rows) ⇒ cite **17 rulings**; the D total is still the 14 entries D-173 ~ D-186 |
| 12 | The tool's extraction surface | `BRIEF` §0.2 / D-175 / D-185 all say it "extracts from `docs/zh/*`" | it reads only **6 fixed documents** (`main():349-355`) and does not read 40 ~ 45 ⇒ a **new parser must be added** (42 §9 item 3) |
| 13 | Whether language keys have an automatic gate | `BRIEF` §0 / D-185 put "language keys 337 → 338" alongside config as a "synchronised expectation surface" | the tool only compares zh / en **key-set equality** and does not verify 338 ⇒ **there is no automatic gate**, it can only be counted by hand (42 §9 item 4) |
| 14 | How ruling numbers are registered | 42 §9 item 6 requires "document 43 to **newly register D numbers** from A-70"; what document 43 actually registers is **A-70 ~ A-81** (adjudications), with no new D number | always write "**ruling #13 ~ #17**" (consistent with 43 §5's way of citing A-73 / A-81), and **do not use A numbers as D numbers** |
| 15 | The continuity of the RT numbers | `BRIEF` §4 inserts **RT-60b** between RT-56 and RT-57, whereas the RT series ought to be contiguous | cite it as **a sub-item of RT-60** and **do not open RT-61** (42 §9 item 5) |
| 16 | The start line of `CultivatorBlock`'s placement block | 41 §2.1 writes `:85-102`; `BRIEF` §1.1 and 42 §2.1.4 write `:86-102` | cite **the two write lines** `:99-100` (all three agree) and not the block's start line; no assertion is affected |
| 17 | Document 41 §0.3's ruling-registration standard | 41 §0.3 writes "`BRIEF` §2 and 40 §2 both register as **#1 ~ #12**; this document cites #13 ~ #15 separately" | the current state is **17 rulings** (#1 ~ #17) and 40 §2 has transcribed them one by one; cite **17 rulings**, and that passage of document 41 is the old standard (42 §9 item 6) |

### 6.2 What to Do When a New Conflict Appears

Write it into the report's "open questions", with all four parts: ① each side's `file:line` or number; ② each side's exact words; ③ which side you are **temporarily** writing to (you may only choose the "document 40 / an existing adjudication" side, or leave it untouched for now); ④ who has to decide. **Do not** change a document before implementing, and do not implement both sides.

### 6.3 Document 38's Entries Superseded by This Version (6 rows, declared explicitly so you do not copy the old ones)

| Document 38's entry | What it said at the time | This version |
|---|---|---|
| HC-98 | three in-between windows (registration F-224 → F-228, etc.), inside which "self-check OK" is not written | the window becomes **F-258 (opens) → F-263 (closes)**, and only for config keys and language keys (HC-122 / HC-123, 43 §0.3) |
| HC-99 / HC-100 | config **175 keys / 60 groups**; CLIENT **3 keys** | **176 / 60**; CLIENT 3 keys unchanged (HC-122) |
| HC-101 | language keys **337**, `farmstorm.configuration` 178 | **338 × 2**, that prefix **179** (HC-123) |
| HC-112 | "binding has **only two** write points" and "no nearest-drift / periodic rebind / auto-claim at runtime" | write points **2 → 3** (placement / migration / periodic rebind); the two self-promises are **partially superseded** by ruling #12 (HC-124 / HC-126, 40 §5) |
| HC-120 | the tool synced in four places: 97 / 175 / 337 | 97 **unchanged**; config **176**; language keys **338** (no automatic gate); plus the **new** `expected_config_keys_v021` (HC-135) |
| **N-106** | "**never rebind, drift or auto-claim at runtime**" | **void as a whole**: rebinding is an explicit requirement of this version (D-176 / D-177). The corresponding places in this version are N-117 (do not call `removeCultivator` / `pruneCultivators`) and N-116 (do not open new keys for the rebind) |

## 7. Report Format

Every report has four fixed sections, **in this order; a missing section means the task is unfinished**:

```
## Conclusion
One sentence: what you did, and which numbers it maps to (F number / D number / document 45's task section / document 42 §3's action-surface row number /
document 42 §7's acceptance number / document 41 §9's B number). Writing "done", "normal" or "no problem" is forbidden — that is not a conclusion.

## Evidence
Three lines per criterion: the command as written → the key output (one to three lines verbatim) → the number (HC-xx / N-xx / 42 §7 #n / 41 §9 B-nn).
For counting criteria give the two numbers "measured before the change / measured after the change" (pitfall 20).
For the change surface give the file list from `git diff --stat` (one task, one commit; one extra file means out of scope).
For save-related criteria give the key excerpt from `/data get block|entity <pos|mob>`, or the file reading after the server stops.

## Open Questions
What you are unsure about, what conflicts between documents, what needs the author to decide. If there is none, write "none" — this section may not be omitted.

## Next Step
Per document 43's dependency graph, the number of the next task to do; plus problems you have **not** fixed this round but have already seen (register only, do not act).
```

Three constraints:

1. **Evidence must be observable**: log lines, command readings, `git diff`, `jar tf`, save NBT, in-game readings. For a log longer than 20 lines keep only the **first error** and the **summary line**.
2. **Every assertion must land on a number**: an assertion without a number is treated as "unverified"; one that has a number but no reading is treated as "not run".
3. **Do not paste file contents only**: `git diff` shows only the changed lines, not the whole file; a log shows only the matching lines. The report is for the next person to make a decision, not an archive.

## 8. Pre-Start Confirmation Checklist

Tick every line before starting a task; if any line cannot be ticked, resolve it first and do not start:

- [ ] **1** I confirm the baseline is **0.2.0's released state** (`gradle.properties:11` currently reads `mod_version=0.2.0`), that this version changes it to **0.2.1**, that after the change this is the only place declaring the version, and that `0.2.0` comes to **0** across `gradle.properties` and the two `.cmd` files (6 literals, A-70).
- [ ] **2** I can say which numbers this task lands on: the F number, the D number, the ruling #, document 42 §3's action-surface row number, document 42 §7's acceptance number / document 41 §9's B number.
- [ ] **3** I know **which window** I am in (**F-258 opens → F-263 closes**), and inside it I write neither `176 / 60` nor `338 × 2`, only "this key exists" and section / interval readings (HC-122, 43 §0.3).
- [ ] **4** Every API I am going to touch — has it appeared among document 42 §8's **10 items**? If so, run the minimal case first and record the way out in the report (the relay of N-114).
- [ ] **5** For every criterion this round I have run it once **before** the change and recorded the measured value (pitfall 20, the meta-rule).
- [ ] **6** I confirm this round **does not touch**: `docs/zh/00` ~ `43`, `docs/en/*`, `.work/*`, `Farmstorm.java:167-172`, the 5 `Monster` trigger sites, `FangTrapCactusTrap:87-109`, `vibration_bean`'s 0 damage, `removeCultivator`, the three `.dat` files under `world/data/` (HC-137, N-123, N-126).
- [ ] **7** I confirm the run directories are isolated (`-PfsRunDir=`); when counting config I **recognise only `run/config/farmstorm-server.toml`**, and **run `runServer` once first to regenerate it** (the repository holds another old copy of about 121 keys).
- [ ] **8** I confirm whether `tools/verify_project.py` has been synced: is `expected_config_keys_v021` added, is the expected number 176; if it is not synced I **do not cite** `FAIL 0` (HC-135, RT-60).
- [ ] **9** I have read the twelve adjudications A-70 ~ A-81 and can say which ones constrain this task (A-70 version string in 6 places / A-71 comment changed together / A-72 the 9th parameter and the multiply point in one commit / A-73 the 13th category registered in three places / A-74 window open and close / A-75 the parser's four contracts / A-76 22.5 / A-77 3 call sites / A-78 149 and 12 / A-79 set no cap / A-80 settle on load / A-81 2 places and persisted).
- [ ] **10** I confirm this round commits the changes of **one** task only, and the commit message starts with the task number (`F-249: …`); rolling back this one commit does not invalidate other tasks.
- [ ] **11** I have ready the two numbers "measured before the change / measured after the change", plus at least one log line or reading that can go into the report.
- [ ] **12** I know where the open questions that may arise this round should be registered (the report's 3rd section), and which kinds I may not decide myself (new numbers, standard conflicts, upstream contradictions).
