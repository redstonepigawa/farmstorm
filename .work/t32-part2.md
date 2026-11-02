
- **HC-84 · Tilling's three states do not change, and the order is fixed**
  - Constraint: `Verdict`'s three states `ALLOW` / `NO_CORE` / `QUOTA_FULL` do not change; the order is fixed: ① range gate (an allied core's 3×3) → ② a free slot in the candidate core (`farmlandBonus − that core's bound count > 0`) → ③ nearest (`BlockPos#distSqr`) + the three-level tie-breaker; neither refusal **produces farmland or spends durability**.
  - Criterion: hoeing outside the range returns `NO_CORE` with the block and the durability unchanged; when the nearest core is full it returns `QUOTA_FULL` (even if a farther one has a free slot); `bindings.corePos` equals the nearer core's coordinates.
  - Source: D-135; document 30 §2.2.7.

- **HC-85 · The three faces split apart: the trample face is "bound means protected", quota and overlay are "allied in real time"**
  - Constraint: **the trample face** is decided by "**bound means protected**" (`protectsFromTrample` has no asker parameter and cannot answer "who stepped on it"); **the quota gate (tilling) and the HUD overlay** are decided by **real-time** alliance (the core owner themself + same-team allies, via UUID / name, regardless of whether they are online).
  - Criterion: with every teammate offline, allied bound farmland still does not get trampled; after the same plot changes team it is trample-protected **still**, but the next block's tilling capacity is computed for the new team and the overlay's next packet shows the new team; `protectsFromTrample`'s signature has **no** `Player` parameter.
  - Source: D-138; A-45; document 30 §2.2.5 and appendix A #30.

- **HC-86 · Core removal: compute first, write later + three branches**
  - Constraint: a destroyed core **handles only the farmland under its own name**; compute all three sets first, then write to the world; the three branches = ① a successor is found (covers + is allied with the farmland's owner + has a free slot, nearest + three-level key) → rebind; ② none found + the chunk is in memory + the block is still farmland → `setBlock(DIRT)`; ③ none found + the chunk is **not loaded** → **drop the binding only, leave the world alone**. Do not load a chunk in order to settle.
  - Criterion: with no successor and the player standing far away (the chunk unloaded), after removing the core the field is still `minecraft:farmland` and the log contains `迁移失败且区块未加载`; standing at the field's edge and removing the core turns it into `minecraft:dirt` with no drop; `getChunk` / `hasChunkAt` have 0 hits on the settlement path.
  - Source: D-136; A-35; document 30 §2.2.9; RT-32.

- **HC-87 · Exactly 1 custom packet, one-way, read-only**
  - Constraint: the repository has **exactly 1** custom payload, direction S2C; the client handler does one reference replacement only — **no reply, no determination, no world read**; `playToServer` / `sendToServer` have **0 hits**; the packet does not enter the registration surface; the client must clear the snapshot on logout / server switch.
  - Criterion: `grep -rln "CustomPacketPayload" src/main/java` is exactly **1** file; `grep -rn "playToServer\|sendToServer" src/main/java` is **0**; a dedicated server starts with no `NoClassDefFoundError`; after leaving server X for server Y, which has no core, the overlay **disappears immediately**.
  - Source: D-142 / D-146; RT-31; document 30 §2.1.

- **HC-88 · The overlay's scope and ordering**
  - Constraint: list **all** cores of yourself + your teammates (**including unloaded chunks and other dimensions**), list no enemy core and no core item; one row per core, `#n [维度 path] (x, y, z) 归谁` (your own uses the marker, a teammate uses the name; the dimension is the id's **path**, with no new language key); ordered by **`placedGameTime` ascending**, uniformly across dimensions.
  - Criterion: 1 core in the overworld + 1 in the nether shows **2 rows** in either dimension, with the rows headed `overworld` / `the_nether`, unchanged after a teleport; placing A first (t=100) and B second (t=200) ⇒ A is on top; with both cores' `gameTime` set equal and the same coordinates across dimensions ⇒ ordered by dimension id.
  - Source: D-143; A-39; document 30 §2.1.5, §2.4.4.

- **HC-89 · Overlay layout: 8 core rows + 1 `+N` row, panel height `10n + 3`**
  - Constraint: at most **8 core rows**; beyond that add **1 more row**, `+N` (`N = total − 8`) ⇒ at most **9 rows**; panel height = `10n + 3` (8 rows **83 px** / 9 rows **93 px**); `+N` adds no language key; `#n` numbers by the full order **before truncation**.
  - Criterion: with 9 allied cores it draws 8 core rows + 1 `+1` row = **9 rows / 93 px**; with 8 cores, 8 rows / 83 px / no `+N`; the layout decomposition **recognises only** row height 9 / row gap 1 / padding 2 — two decompositions must not coexist.
  - Source: D-143 item 4; A-34 / A-41; document 30 §2.4.4.

- **HC-90 · Friend Chives: the field length reads `friend_chives.effectTicks`, not a constant**
  - Constraint: after landing it lasts **`friend_chives.effectTicks`** ticks (default **200**, range 20 ~ 1200); **every tick** it scans the `LivingEntity`s inside the sphere at the landing point (radius from `friend_chives.hitRadius`, default 6 blocks, Euclidean distance) and applies **`effectTicks − x`** ticks of Q-Bounce (x = ticks elapsed since landing); it destroys itself on tick `effectTicks`. In code the field length has only one source, `fieldLength`. The sound is judged **once only, at the moment of landing**: with ≥1 entity inside the sphere it plays once, an airdrop plays nothing, and one that walks into the field later is not replayed.
  - Criterion: after changing `effectTicks` to **100** and re-measuring, the countdown stops at 100 and the entity disappears on tick **100**; the hits of `grep -n "200" src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java` may only be inside comments (0 code lines); an airdrop has no `friend_chives` sound and a hit plays exactly 1.
  - Source: D-131 / D-132 / D-133; A-33; RT-34 / RT-35; document 30 §2.7.

- **HC-91 · Pumpkin Turret (man瓜) (`farmstorm:pumpkin_turret`): three values + two readers**
  - Constraint: `pumpkin_turret.fireIntervalTicks` defaults to **40**, `projectileDamage` to **3**, `bulletSpeed` to **0.45** (a new key, range 0.0 ~ 4.0); the `TurretBullet.SPEED` constant is deleted and **both readers read that key** (`entity/TurretBullet.java:95` and `behavior/impl/PumpkinTurret.java:75`); introduce no soft tracking, only close-range convergence.
  - Criterion: after deleting the old toml, `[pumpkin_turret]` has exactly **5** keys with the three values `40` / `3` / `0.45`; `grep -rn "TurretBullet.SPEED" src/` has **0 hits**; the gap between two shots is ≈ **2000 ms**; a target within 1 block is hit by 5 consecutive shots.
  - Source: D-127 / D-128 / D-129; A-30; document 30 §2.6; RT-38.

- **HC-92 · Compressed Hay Block: all five sound categories become stone**
  - Constraint: `registry/FarmstormBlocks.java:79`'s `SoundType.GRASS` → **`STONE`**; this affects all **five** sound categories — **break / place / step / hit / fall** (`getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound`); hardness 30 / blast resistance 6 / the tool requirement for drops / `MapColor.COLOR_BROWN` all stay untouched.
  - Criterion: five `/playsound` A/B pairs, one each (`block.stone.*` matches, `block.grass.*` differs); `grep -c "SoundType.GRASS" src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java` is **0**; that line's `git diff` has exactly one change.
  - Source: D-130; A-31; document 30 §3 row 7, §7 #24 ~ #28.

- **HC-93 · Outpost Core light level 15**
  - Constraint: add `.lightLevel(state -> 15)` to `outpost_core`'s registration chain; every other property stays unchanged character for character (`MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `SoundType.STONE` / `pushReaction(BLOCK)`).
  - Criterion: F3's `Light` line = **15**; `grep -c "lightLevel" src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java` = **1** and only on the core's chain; that file's `git diff` contains no other property line.
  - Source: D-141; RT-36; document 30 §3 row 8.

- **HC-94 · Forced loading uses a bare `TicketType` only**
  - Constraint: the runtime temporary load of the core's chunk **only** uses `TicketType` + `addRegionTicket` / `removeRegionTicket`, with acquisition and release **paired**, the release point at the same place as `release()`, and a shutdown fallback; **no** `setChunkForced`, **no** NeoForge `TicketController#forceChunk`; the core's `ChunkPos` must not appear in the persisted `world/data/chunks.dat`; acceptance writes **the tick face only**.
  - Criterion: `grep -rn "setChunkForced\|forceChunk" src/main/java` has **0 hits**; `chunks.dat`'s `Forced` array does not contain the core coordinates; the ticket-level log shows the centre's `newTicketLevel = 32` and the immediately neighbouring ring `33`; after removing the core there are no boundary particles at the same place.
  - Source: D-145; A-38 / A-43; RT-39; document 30 §2.5.

- **HC-95 · The version number has only one source**
  - Constraint: change `mod_version` exactly once, in `gradle.properties:11` (`0.1.0` → `0.1.1`); the jar names hard-coded in `检查环境.cmd` and `一键构建.cmd` change to `farmstorm-0.1.1+mc1.21.1.jar` **in the same pass** (`-sources` included); the other documents only cite it and do not each declare a version.
  - Criterion: `grep -c "0\.1\.0" gradle.properties` is **0**; `farmstorm-0.1.1+mc1.21.1` hits **3** places in the two `.cmd` files (`检查环境.cmd:14`, `一键构建.cmd:11`, `:12`), and `0.0.1` has **0 hits**; the artifact names under `build/libs/` and `dist/` agree.
  - Source: D-124 / D-147; document 30 §3 rows 1, 2.

- **HC-96 · The code does no config migration**
  - Constraint: write no migration logic for a `farmstorm-server.toml` already on disk (do not change or delete the server owner's values); write the "upgrade notes" into the delivery document only.
  - Criterion: `grep -rn "farmstorm-server.toml" src/main/java` has **0 hits**; `git diff` contains no code that reads or writes a toml; when upgrading in place with the old toml kept, `fireIntervalTicks` is still **60** and `projectileDamage` still **1**, while `bulletSpeed` is filled in automatically as **0.45**.
  - Source: D-147; RT-38; document 30 §4.1 upgrade notes.

## 3. Prohibited Actions (N-75 ~ N-92)

Each one can be decided by a single command, a single place in `git diff` or a single reading; these are not attitude requirements. The "why" is given in one sentence — enough for you to judge the boundary.

- **N-75** Never write "the packet red line is abolished" as "relaxed", "exception" or "this version allows 1".
  - Criterion: in documents, comments and the report, `grep -rn "放宽\|例外\|允许 1 个"` is 0 in sentences about network packets.
  - Why: structurally that ban no longer exists, and writing "exception" makes the next reader delete the packet (document 28 §4.2).

- **N-76** Never write farmland protection as "all farmland inside the range" or "automatically protected inside the core's range".
  - Criterion: when documents and comments describe the protected surface, `grep -rn "范围内所有耕地\|范围覆盖"` is 0; the code's criterion must come from the binding table.
  - Why: that is 0.1.0's model, and it is the bug `plan0.1.1.txt:14` sets out to fix (document 28 §6 tombstone 1).

- **N-77** Never write the retirement of the "+" sign as "all icons are gone in this version".
  - Criterion: in `git status`, `textures/item/` has only `wild_apple.png` changed; `NO_PLUS_ICONS` has been deleted from the generator.
  - Why: what is retired is the **rule**; this version regenerates 1 image only, and the other 24 stay as they were (D-125).

- **N-78** Never write `Pumpkin Turret` (man瓜) as a bare alias without its id.
  - Criterion: in the Chinese document `man瓜（farmstorm:pumpkin_turret）` appears at least once, and **its first occurrence is that form**; in the English document `Pumpkin Turret (man瓜)` appears at least once, and **its first occurrence is that form**; `grep -rn "南瓜炮台"` is 0 in both documents.
  - Why: 4 objects contain "melon" (`pumpkin_turret` / `melon_grenade` / `melon_slice_bomb` / `melon_grenade_stem`), and an alias points at the wrong one (document 28 §0.2).

- **N-79** Never write a **point-in-time fact** as an acceptance assertion.
  - Criterion: in acceptance and self-check, `grep -rn "调用点\|唯一调用\|只有 1 个调用"` is 0; every "there is only one place in the repository" is replaced by a **countable number of files / keys / hits**.
  - Why: A-37 — once the binding scheme introduces "dropping a binding", the write entry point is no longer unique; such an assertion is red the moment it is implemented, and you cannot tell a defect from what is expected.

- **N-80** Never cite `verify_project.py`'s "FAIL 0" before the tool has been changed to this version's wording.
  - Criterion: whenever "FAIL 0" appears in an acceptance record, that tool's diff must be given with it (v0.1.1's expectation sources added, `farmland.limitPerPlayer` removed from the union); before the change, cite only the per-section readings (`93 / 93`, `323 键`).
  - Why: A-40 — its config expectation set comes from four old documents, so after deleting 1 and adding 1 a green light can only come from a missed check.

- **N-81** Never load a chunk in order to settle, compare or display.
  - Criterion: `getChunk` / `hasChunkAt` / `getBlockEntity` are all 0 at the three places — core-removal settlement, the comparison-key path and overlay assembly.
  - Why: A-35 and A-39 — one forced load drags in a batch of chunks and makes the result drift with load timing.

- **N-82** Never fix A-49's cross-dimension same-coordinate collision.
  - Criterion: the ledger's farmland record still has only `pos` (no dimension field), `dataVersion` is still **2** (not raised to 3), and there is no migration code that "adds a dimension to legacy saves".
  - Why: A-49 has ruled that this version does not fix it, because a legacy save cannot recover the dimension and the farmland side has already taken one save-format bump.

- **N-83** Never skip confirmation of an API to verify and assume the signature.
  - Criterion: each of document 30 §8's 8 items has a record of "the minimal case's command + result"; before it is confirmed you must not use an empty implementation, an `UnsupportedOperationException` or commented-out functionality to "make it compile".
  - Why: this version's 6 technical constructs (packet / keys / CLIENT config / client command / ticket / binding table) are all firsts in this repository.

- **N-84** Never change a value already fixed by documents 28 / 29 / 30 / 31.
  - Criterion: `docs/` shows 0 changes in `git status`; a new value in the report must be traceable to a D number, or marked "measured during implementation".
  - Why: the six documents are the frozen upstream; changing a convention means changing document 28 first and registering it.

- **N-85** Never revive `farmland.limitPerPlayer` in any form.
  - Criterion: `grep -rn "limitPerPlayer\|baseOverride\|setBase" src/ tools/` is 0; `farmstorm-server.toml`'s `[farmland]` section is read by no code any more; those 2 language keys are deleted.
  - Why: D-139 / D-140 — it would immediately create the double source "base + capacity".

- **N-86** Never stuff this version's new constructs into the registry.
  - Criterion: 93 and the 12 class subtotals do not change; `git diff src/main/java/com/dashpig/farmstorm/registry/` holds only the two places in `FarmstormBlocks.java` (`:79`, `:82-89`).
  - Why: none of the new pieces goes into `BuiltInRegistries` (document 30 §1.3), and stuffing one in breaks 93 and "+0" at once.

- **N-87** Never open a second knob for capacity / quota.
  - Criterion: in `FarmstormConfig`'s diff `pumpkin_turret.bulletSpeed` is the only new key, and `outpost_core.farmlandBonus` keeps its key name and default; there is no new key such as `farmlandCapacity` / `outpost_core.capacity`.
  - Why: RT-33 — once the two knobs disagree, nobody can answer "does this plot count as over quota".

- **N-88** Never read the comparison key from a block entity.
  - Criterion: `OutpostCoreIndex.Entry` carries `gameTime` and the comparison path reads the index only; `getBlockEntity` has 0 hits in overlay assembly and in `FarmlandGuard.check`.
  - Why: A-39 / document 30 §2.2.10 — whether a block entity exists depends on load timing, so the value drifts.

- **N-89** Never send back from the client, and never do the allied determination on the client.
  - Criterion: `playToServer` / `sendToServer` 0 hits; the client handler does one reference replacement only; there is no `isAlly` call in client code.
  - Why: D-142's three boundaries — a client that can determine can forge "allied".
