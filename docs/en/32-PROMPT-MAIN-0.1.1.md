# 32 · v0.1.1 Master Prompt

> Feeding method: this file is fed **once only**, before any Phase. After that, feed per Phase following document 33.
> Applies to: v0.1.1 (the baseline v0.1.0 is already implemented and released). Where they conflict, the decision record of document 28 wins.

| Item | Value |
|---|---|
| Document ID | 32-PROMPT-MAIN-0.1.1 |
| Audience | the implementation agent (can read and write this repository, can run `.tools\gw.cmd`) |
| Upstream | `28-DECISIONS-0.1.1.md` (the single source of truth, D-124 ~ D-147) → `30-TECH-0.1.1.md` → `29-DESIGN-0.1.1.md` → `31-TASKS-0.1.1.md` |
| Numbers this file issues | **HC-77 ~ HC-96 (20 entries)**, **N-75 ~ N-92 (18 entries)**, 19 fact-card rows, 20 traps (including 1 meta-rule for self-checking the criterion); risks continue **RT-31 ~ RT-41**, adjudications are cited as **A-30 ~ A-49** |
| What this file does not write | the per-Phase content (document 33), the tasks and the schedule (document 31), the class design and algorithms (document 30) |

## 0. Who You Are and What You Do

You are this repository's **implementation agent** for v0.1.1. The baseline is the **already released 0.1.0** (`gradle.properties:11` currently reads `mod_version=0.1.0`, and `dist/` holds the 0.1.0 artifacts); this version is only an increment: 24 decisions (D-124 ~ D-147) land on 0.1.0's existing registration surface, config, ledger, client, generators and build scripts — **do not rewrite what 0.1.0 has already implemented**, and do not open a second framework. **(Read document 28 before writing a line)** — it is the single source of truth, and where it conflicts with this file or with document 30 it wins; this file is harder than document 28 in exactly two things: the **new red-line criteria** (§1) and the **execution discipline** (from §2 on). You only ever see this file plus the current Phase's task brief from document 33, so **do only what the brief asks**: stop when done, report, wait for the next Phase, do not implement the next stage in advance, and do not stuff a "fixed it on the way" into this round. Never invent a number: copy verbatim whatever traces to a D number / RT number / a `file:line` in the source, and put what does not trace into the report's "open questions" — you may not decide it yourself. The behaviour 0.1.0 has already implemented — tracking, traps, growth, deployment, sounds, particles, indexes, menus — is **not changed by a single line** in this version, unless a decision in document 28 names it for change. Every "done" in the report must land on one observable piece of evidence (a log line, a command reading, a `git diff`, a `jar tf`, save NBT, an in-game reading); "it behaves normally" is not accepted. Also record one **pre-existing defect**: `检查环境.cmd:14` and `一键构建.cmd:11`/`:12` still hard-code the `0.0.1` jar name, so running `检查环境.cmd:14` today reports the jar missing — this was carried over from 0.1.0, is not a new requirement of this version, and this version fixes it in passing (A-51).

## 1. Red Lines (**the criteria have changed in this version — follow this section**)

### 1.1 Retained: No Mixin

The only project-level red line retained. **Do not** write `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor`, and do not add the mixin plugin to `build.gradle`.

- Criterion: `grep -rn "@Mixin\|mixins.json" src/main/java` gives **0 hits**; `git diff build.gradle` is **empty**; there is no `*.mixins.json` under `src/main/resources`.
- Source: D-146; `BRIEF` §1.4 (`@Mixin` measured at 0, `build.gradle` has no mixin plugin).

### 1.2 **Abolished outright: 0 Custom Network Packets**

v0.0.1 ~ v0.1.0's red line was "**0** custom network packets". **From v0.1.1 on, this red line is abolished outright.** This version happens to add **1** S2C read-only packet (D-142), but **sets no numeric ceiling**.

Four readings; get any one of them wrong and all six downstream documents are wrong:

1. **It is abolished outright, not "relaxed to 1".** There is no ceiling, and no "apply for an exception" action surface either.
2. **It is not a "this-version exception".** The original structure was "prohibited by default + one waiver", and that "prohibited by default" no longer exists; the word "exception" implies the ban is still there — do not use it.
3. **Abolishing it does not mean a packet must be added.** This version adds exactly 1, because the HUD overlay (the overlay) genuinely needs it (the client cannot reach the server's `SavedData`); every future packet still has to be **registered by a separate decision** and may not be added on the way — the one this version registers is D-142.
4. **"No Mixin" is unaffected**: it is still zero and still prohibited (§1.1).

**The list of old red-line statements** (every place below says "0", and document 28 §5 has **abolished them outright**):

| Document | Location | Old statement | Status now |
|---|---|---|---|
| `00-DECISIONS.md` | C-6 | "0 custom S2C packets" | **abolished outright**; the rest of C-6 (sounds / particles go through vanilla packets) is still an implementation fact, but no longer a prohibition |
| `10-DECISIONS-0.0.2.md` | `:73-76` (D-50) | "the three invasive effects add no `CustomPacketPayload`" | **the project-level prohibition is abolished**; the implementation facts of those three effects do not change, and this version does not touch them |
| `22-DECISIONS-0.1.0.md` | `:44`, `:68`, `:480` | "0 custom network packets" written as one of the two red lines | **abolished outright** |
| `26-PROMPT-MAIN-0.1.0.md` | `:183-185` (HC-73), `:402` | the same, as a hard constraint and as a cross-reference-table row | **the half of HC-73 about network packets** is void (not "half a ban is still in force"); the two sentences on Mixin and on `GameRules.register` are still in force (§1.3) |
| `27-PROMPT-TASKS-0.1.0.md` | the acceptance criterion | "`grep CustomPacketPayload` is 0" used as the pass criterion | **the criterion is void**, replaced by "exactly 1 file, and the client does not send back" |
| `README.md` | `:71`, `:148`, `:191`, `:419`, `:423` | the red-line section says "0 custom network packets" | belongs to README's increment scope and changes along with D-146 |

**Why this one packet cannot be anything else** (four reasons; any one of them alone is enough to reject the "0 packets" route, and they are also why you **may not** delete it to save trouble):

1. the core list is the **server-side `SavedData`** (`world/data/farmstorm_outpost_core.dat`), and the client **has no copy**;
2. "which cores are mine" must be **complete**: the client only loads chunks within view distance, and knows nothing about a core outside it;
3. the owner **may be offline**, and "teammate relations" can only be resolved on the server;
4. the list needs a **stable order** and the "8 rows + `+N`" overflow, which can only be drawn from a complete snapshot.

**When implementing, this file is authoritative**: do not delete this packet because some old document says "0", and conversely do not read "abolished" as "this version may add packets freely". The packet's three boundaries (read-only, no send-back, server-authoritative) are written in HC-87.

### 1.3 Engineering Constraints (not red lines, but equally not to be broken)

Document 28 §4 lists only one red line, "no Mixin"; the two below are not on the red-line list, but breaking one is on a par with breaking a red line — break one and this round is judged unfinished.

- **Do not try to register a real gamerule**: on 1.21.1 `GameRules#register` is `private static`, and there is no registration entry inside "no Mixin". Criterion: `grep -rn "GameRules.register" src/main/java` gives **0 hits**; the repository's only read point for `GameRule` is `explosion/FarmstormExplosion.java:109` reading the vanilla `RULE_MOBGRIEFING` (that one place must not be changed).
- **Server authority**: hit determination, farmland binding, ledger clearing and packet sending all live on the server; the client only renders — it does not determine, does not send back and does not read the world. Criterion: the client handler has exactly one reference swap; `grep -rn "playToServer\|sendToServer" src/main/java` gives **0 hits**; a dedicated server starts with no `NoClassDefFoundError`.

## 2. Hard Constraints (HC-77 ~ HC-96)

The HCs constrain only this version's increment surface. 0.1.0's HC-51 ~ HC-76 remain in force, but 8 of them are explicitly superseded in §6.2 (HC-61 / HC-62 merge into one row, so 8 entries over 7 rows) — **where they conflict, this file and document 28 win**. Each entry gives three things: the **constraint** (what counts as right), the **criterion** (what counts as passed) and the **source** (decision / tech / risk number). The criterion column is always executable: one command, one `git diff`, one log line, one block state.

- **HC-77 · The registration total is still 93, and it is checked class by class by name**
  - Constraint: the 12 class subtotals must still be `items 32 / blocks 26 / block_entity_type 7 / entity_type 13 / mob_effect 4 / sound_event 3 / recipe_type 1 / recipe_serializer 1 / creative_mode_tab 1 / damage_type 3 / data_component_type 1 / menu_type 1`, totalling **93**. **Not one** of the things this version adds enters the registry: payloads go through `RegisterPayloadHandlersEvent`, keys into the client's `Options#keyMappings`, CLIENT config through `ModConfigSpec`, client commands through Brigadier, the binding table is `SavedData`, and tickets are plain objects. The 12 assertions of `Farmstorm.java:138-141` are **not changed**.
  - Criterion: the startup log contains "自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK"; `Farmstorm.java:138-141` shows 0 changed lines in `git diff`; all 12 classes have been checked **class by class by name** (do not change only the total without the list, and do not change only the list without the total). **The numbers must match the log, and the class names follow the log string**: the constraint's `block_entity_type` is written `block_entities` in the log, and the two can never be character-for-character identical.
  - Source: D-142 / D-144 / D-145; document 28 §3.4 "registration surface +0"; document 30 §1.2, §1.3.

- **HC-78 · The server config is 166 keys / 58 groups, and `farmland.limitPerPlayer` does not exist**
  - Constraint: after the change the server config = **166 keys / 58 groups** (delete `farmland.limitPerPlayer` and its whole group, add `pumpkin_turret.bulletSpeed`); that key **does not exist** in the source, the generated source, the language keys or the default toml; the `pumpkin_turret` group holds **5 keys**.
  - Criterion: **first delete** `run/config/farmstorm-server.toml` and then start the server; in the regenerated file the `^\s*\[` lines = **58**, the key lines = **166**, and there is no `[farmland]` section; `grep -rn "limitPerPlayer\|farmlandLimitPerPlayer" src/` gives **0 hits**. A `[farmland]` section left over in an old toml **does not count as a failure** (the config system only adds, never deletes), but no code may read it.
  - Source: D-139; A-42; document 30 §4.1; RT-33.

- **HC-79 · CLIENT config: 3 keys, a file of its own, not counted in the 166**
  - Constraint: add one `ModConfig.Type.CLIENT` spec with 3 keys `hud.enabled` (bool, **true**) / `hud.x` (int, **4**) / `hud.y` (int, **4**), landing in `config/farmstorm-client.toml` (in a dev environment it actually lands in `run/config/farmstorm-client.toml`, side by side with the server one); it is **not counted in the 166** and is never written mixed with the server's.
  - Criterion: after the client's first start `run/config/farmstorm-client.toml` exists with exactly 3 keys / 1 group; `farmstorm-server.toml` has no `hud` section; the server's group count is still 58.
  - Source: D-144; document 30 §4.2, §2.4.2.

- **HC-80 · Language keys 323 × 2, with the Chinese and English key sets exactly equal**
  - Constraint: after the change each of the two language files holds **323** keys (`315 − 2 + 10`), and `set(zh) == set(en)`; `farmstorm.configuration.*` is exactly **169** (= 166 server + 3 client); the new `key.farmstorm.*` **2** entries, `command.farmstorm.hud.*` **3** entries and `gui.farmstorm.core_hud.self` **1** entry; the 2 deleted entries include `farmlandlimit`. `ModLangProvider`'s class comment — stating the same key counts as in the change comments — is also part of the artifact.
  - Criterion: `zh_cn.json` and `en_us.json` each have **323** keys and equal key sets; `grep -c farmlandlimit` is **0** in both; `grep -c '"farmstorm.configuration\.'` is exactly **169**.
  - Source: D-129 / D-140 / D-144; A-48; document 30 §4.3; RT-40.

- **HC-81 · One farmland block belongs to exactly 1 core, and the per-core capacity = `outpost_core.farmlandBonus`**
  - Constraint: each farmland block has only **0 or 1** binding at any one moment; the per-core capacity = `outpost_core.farmlandBonus` (default **16**, range 0 ~ 1024), with **no new key added**; the team capacity = that key × the number of allied cores; "used" = the number of binding entries; legacy entries that cannot be backfilled (`orphans`) **count neither towards capacity nor towards used**.
  - Criterion: the **17th** block hoed under the same core is refused, and the save's `bindings` still holds **16** entries for that core; after changing `farmlandBonus` to **2** the **3rd** block is refused and the team capacity becomes `2 × the number of cores`; the same `pos` appears only once in `bindings`; the capacity log does not show the count of `orphans`.
  - Source: D-134 / D-139; RT-33; document 30 §2.2.6.

- **HC-82 · The comparison key has three levels and reads only the index and the coordinates**
  - Constraint: every "pick one of several candidate cores" determination (till binding, migration, backfill) uses **the same** three-level key: `placedGameTime` ascending → **dimension id** ascending → `BlockPos#compareTo` (y → z → x). The comparison values are **read only from `OutpostCoreIndex` (a `SavedData`)** — no block entity is read and no chunk is loaded; **no reliance on the iteration order of any `Map` / `List`**.
  - Criterion: after offline-changing two cores' `gameTime` to the same value and restarting, the order follows dimension id ascending; within one dimension and the same `gameTime` it follows `BlockPos#compareTo`; running the same save twice, the two `核心比较键指纹 <hex16>` log lines are completely identical; in the comparison path `getBlockEntity` / `getChunk` / `hasChunkAt` have **0 hits**.
  - Source: D-135; A-39; document 30 §2.2.7, §2.2.10.

- **HC-83 · Legacy-save backfill uses a `BlockPos` single-level total order, and the whole stretch writes no world**
  - Constraint: the startup backfill (including the first backfill of a 0.1.0 legacy save) first sorts the candidate farmland blocks by `BlockPos#compareTo` (y → z → x) as a **single-level total order**, then processes them one by one; the insertion order of `byOwner` / `orphans` must not be used; what cannot be backfilled **stays as farmland, untouched**; the whole stretch has no `setBlock`.
  - Criterion: running the same 0.1.0 legacy save twice, the `耕地补建：绑定 N 块 / 保持未绑定 M 块` log's N and M are completely identical; `setBlock` has 0 hits in the backfill stretch; for every coordinate that cannot be backfilled, `/execute if block … minecraft:farmland` is true.
  - Source: D-137; A-47 (rev.2, superseding A-36 / A-44); document 30 §2.2.8; RT-32.

- **HC-84 · Tilling's three verdicts are unchanged, and the order is fixed**
  - Constraint: `Verdict`'s three states `ALLOW` / `NO_CORE` / `QUOTA_FULL` do not change; the order is fixed: ① the range gate (an allied core's 3×3) → ② a candidate core's free slot (`farmlandBonus − that core's bound count > 0`) → ③ nearest (`BlockPos#distSqr`) plus the three-level tie-break keys; neither refusal **produces farmland or costs durability**.
  - Criterion: tilling outside the range returns `NO_CORE`, with the block unchanged and durability unchanged; when the nearest core is full it returns `QUOTA_FULL` (even if a farther core has a free slot); `bindings.corePos` equals the nearer core's coordinates.
  - Source: D-135; document 30 §2.2.7.

- **HC-85 · The three surfaces' criteria are separated: the trample surface is "bound means protected", the quota and the HUD overlay judge real-time allied status**
  - Constraint: the **trample surface** is judged by "**bound means protected**" (`protectsFromTrample` has no querier parameter and cannot answer "who stepped on it"); the **quota gate (tilling) and the HUD overlay** are judged by **real-time** allied status (the core owner himself + same-team allies, by UUID / name, regardless of whether they are online).
  - Criterion: with every teammate offline, allied bound farmland is still not trampled; after the same block changes team, trampling is **still** protected, but the next block's tilling capacity is computed by the new team and the overlay's next packet is displayed by the new team; `protectsFromTrample`'s signature has **no** `Player` parameter.
  - Source: D-138; A-45; document 30 §2.2.5 and appendix A #30.

- **HC-86 · Removing a core: compute first, then write, in three branches**
  - Constraint: destroying a core processes **only the farmland under that core**; compute all three sets first, then touch the world; **two levels plus one defensive branch** = ① a successor is found (coverage + allied with the farmland's owner + a free slot, nearest + three-level key) → rebind; ② none found + the block is still farmland → `setBlock(DIRT)`; "none found + the chunk is **not loaded** → detach the binding only" is the **defensive branch** (A-60: the ticket always covers the same 3×3, so it is unreachable in normal operation). Do not load chunks for the settlement.
  - Criterion: **with no successor core the field turns into `minecraft:dirt` with no drop whether the player is near or far** (destroying a core remotely shovels the farmland up too, and that is the intent); the log does **not** contain "迁移失败且区块未加载：只摘绑定"; in the settlement path `getChunk` / `hasChunkAt` have 0 hits.
  - Source: D-136; A-35; document 30 §2.2.9; RT-32.

- **HC-87 · Exactly 1 custom packet, one-way and read-only**
  - Constraint: the whole repository has **exactly 1** custom payload, direction S2C; the client handler does one reference swap only — **no send-back, no determination, no world read**; `playToServer` / `sendToServer` have **0 hits**; the packet does not enter the registration surface; the client must clear the snapshot on logout / server switch.
  - Criterion: `grep -rln "CustomPacketPayload" src/main/java` gives exactly **1** file; `grep -rn "playToServer\|sendToServer" src/main/java` gives **0**; a dedicated server starts with no `NoClassDefFoundError`; after leaving server X for a server Y with no cores, the HUD overlay **disappears immediately**.
  - Source: D-142 / D-146; RT-31; document 30 §2.1.

- **HC-88 · The HUD overlay's scope and ordering**
  - Constraint: list **all** of your own and your teammates' cores (**including unloaded chunks and other dimensions**), not enemy cores and not the core item; one row per core `#n [dimension path] (x, y, z) who owns it` (yourself by a marker, teammates by name; the dimension takes the id's **path**, with no new language key); ordered by **`placedGameTime` ascending**, uniformly across dimensions.
  - Criterion: with 1 core in the overworld + 1 in the nether, standing in either dimension shows **2 rows** whose prefixes are `overworld` / `the_nether`, unchanged after teleporting; placing A first (t=100) then B (t=200) ⇒ A on top; setting both cores' `gameTime` to the same value with the same coordinates across dimensions ⇒ the order is decided by dimension id.
  - Source: D-143; A-39; document 30 §2.1.5, §2.4.4.

- **HC-89 · The HUD overlay's layout: 8 core rows + 1 `+N` row, panel height `10n + 3`**
  - Constraint: at most **8 core rows**; when exceeded, **add 1 more row** `+N` (`N = total − 8`) ⇒ at most **9 rows**; panel height = `10n + 3` (8 rows **83 px** / 9 rows **93 px**); `+N` adds no language key; `#n` is numbered by the full order **before truncation**.
  - Criterion: with 9 allied cores, draw 8 core rows + 1 `+1` row = **9 rows / 93 px**; with 8 cores, 8 rows / 83 px / no `+N`; the layout decomposition **admits only** row height 9 / row gap 1 / padding 2, and two decompositions may not coexist.
  - Source: D-143 item 4; A-34 / A-41; document 30 §2.4.4.

- **HC-90 · Friend Chives: the field length reads `friend_chives.effectTicks`, not a constant**
  - Constraint: after landing it lasts **`friend_chives.effectTicks`** ticks (default **200**, range 20 ~ 1200); **every tick** it scans the `LivingEntity` set inside the landing sphere (radius from `friend_chives.hitRadius`, default 6 blocks, Euclidean distance) and applies **`effectTicks − x`** ticks of Q-Bounce (x = ticks elapsed since landing); it self-destructs on tick `effectTicks`. In the code the field length has only one source, `fieldLength`. The sound is judged **only once, at the moment of landing**: with ≥1 entity inside the sphere it plays once, an empty drop plays nothing, and anything walking in later gets no late play.
  - Criterion: after changing `effectTicks` to **100** and re-testing, the countdown stops at 100 and the entity disappears on tick **100**; the hits of `grep -n "200" src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java` are allowed only inside comments (0 code hits); an empty drop has no `friend_chives` sound, a hit has exactly 1.
  - Source: D-131 / D-132 / D-133; A-33; RT-34 / RT-35; document 30 §2.7.

- **HC-91 · Pumpkin Turret (man瓜) (`farmstorm:pumpkin_turret`): three values and two readers**
  - Constraint: `pumpkin_turret.fireIntervalTicks` defaults to **40**, `projectileDamage` to **3**, `bulletSpeed` to **0.45** (a new key, range 0.0 ~ 4.0); the `TurretBullet.SPEED` constant is deleted and **both readers read that same key** (`entity/TurretBullet.java:95` and `behavior/impl/PumpkinTurret.java:75`); no soft tracking is introduced, only short-range convergence.
  - Criterion: after deleting the old toml the `[pumpkin_turret]` group has exactly **5** keys, with the three values `40` / `3` / `0.45`; `grep -rn "TurretBullet.SPEED" src/` gives **0 hits**; the interval between two shots is ≈ **2000 ms**; a target within 1 block is hit by 5 consecutive shots.
  - Source: D-127 / D-128 / D-129; A-30; document 30 §2.6; RT-38.

- **HC-92 · Compressed Hay Block: all five sound categories become stone**
  - Constraint: `registry/FarmstormBlocks.java:79`'s `SoundType.GRASS` → **`STONE`**; this affects the five categories **break / place / step / mine / land** (`getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound`); hardness 30 / blast resistance 6 / the tool requirement for drops / `MapColor.COLOR_BROWN` all stay untouched.
  - Criterion: five positive-and-negative `/playsound` comparisons, one per category (`block.stone.*` matches, `block.grass.*` differs); `grep -c "SoundType.GRASS" src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java` is **0**; that line's `git diff` has exactly one change.
  - Source: D-130; A-31; document 30 §3 row 7, §7 #24 ~ #28.

- **HC-93 · The Outpost Core's light level 15**
  - Constraint: add `.lightLevel(state -> 15)` to `outpost_core`'s registration chain, leaving the other properties untouched by a character (`MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `SoundType.STONE` / `pushReaction(BLOCK)`).
  - Criterion: F3's `Light` line = **15**; `grep -c "lightLevel" src/main/java/com/dashpig/farmstorm/registry/FarmstormBlocks.java` = **1** and only on the core's chain; that file's `git diff` contains no other property line.
  - Source: D-141; RT-36; document 30 §3 row 8.

- **HC-94 · Forced loading uses a bare `TicketType` only**
  - Constraint: the runtime temporary loading of the core's chunk **uses only** `TicketType` + `addRegionTicket` / `removeRegionTicket`, with the request and the release **in pairs**, the release point at the same place as `release()` and a stop-server fallback; **no** `setChunkForced` and **no** NeoForge `TicketController#forceChunk`; the persisted `world/data/chunks.dat` must not contain the core's `ChunkPos`; the acceptance **covers the tick face only**.
  - Criterion: `grep -rn "setChunkForced\|forceChunk" src/main/java` gives **0 hits**; `chunks.dat`'s `Forced` array contains no core coordinate; the ticket-level log shows the centre `newTicketLevel = 32` and the immediately adjacent ring `33`; after removing the core, boundary particles no longer appear at the same place.
  - Source: D-145; A-38 / A-43; RT-39; document 30 §2.5.

- **HC-95 · The version number has only one source**
  - Constraint: `mod_version` is changed once only, in `gradle.properties:11` (`0.1.0` → `0.1.1`); the jar name hard-coded in `检查环境.cmd` and `一键构建.cmd` is changed **in the same pass** to `farmstorm-0.1.1+mc1.21.1.jar` (including `-sources`); other documents only reference it and do not each declare a version.
  - Criterion: `grep -c "0\.1\.0" gradle.properties` is **0**; in the two `.cmd` files `farmstorm-0.1.1+mc1.21.1` has **3** hits (`检查环境.cmd:14`, `一键构建.cmd:11`, `:12`), and `0.0.1` has **0 hits**; the artifact names in `build/libs/` and `dist/` agree.
  - Source: D-124 / D-147; document 30 §3 rows 1, 2.

- **HC-96 · The code does no config migration**
  - Constraint: write no migration logic for an already-persisted `farmstorm-server.toml` (do not change or delete the server owner's key values); the "upgrade notes" go into the delivery document only.
  - Criterion: `grep -rn "farmstorm-server.toml" src/main/java` gives **0 hits**; `git diff` contains no code that reads or writes toml; when upgrading directly with the old toml kept, `fireIntervalTicks` is still **60** and `projectileDamage` still **1**, while `bulletSpeed` is automatically backfilled as **0.45**.
  - Source: D-147; RT-38; document 30 §4.1 upgrade notes.

## 3. Prohibited Actions (N-75 ~ N-92)

Each one can be decided by one command, one place in `git diff` or one reading; these are not attitude requirements. The "why" is one sentence only, enough for you to judge the boundary.

- **N-75** Never write "abolishing the network-packet red line" as "relaxed", "exception" or "this version allows 1".
  - Criterion: in documents, comments and reports, for sentences about network packets, `grep -rn "放宽\|例外\|允许 1 个"` is 0.
  - Why: structurally that ban no longer exists, and writing it as an "exception" makes the next reader delete the packet (document 28 §4.2).

- **N-76** Never write farmland protection as "all farmland inside the range" or "automatically protected inside a core's range".
  - Criterion: when documents and comments describe the protection surface, `grep -rn "范围内所有耕地\|范围覆盖"` is 0; the code criterion must come from the binding table.
  - Why: that was 0.1.0's model, and it is the bug `plan0.1.1.txt:14` is to fix (document 28 §6's first tombstone).

- **N-77** Never write the "+" mark's repeal as "all icons were removed in this version".
  - Criterion: in `git status`, `textures/item/` has only `wild_apple.png` changed; `NO_PLUS_ICONS` has been deleted from the generator.
  - Why: what was repealed is the **rule**; this version regenerates only 1 image and the other 24 keep their old look (D-125). **Do not re-run the generator** — a re-run strips those 24 as well (A-61).

- **N-78** Never write `man瓜` as an alias without its id.
  - Criterion: in the Chinese version `man瓜（farmstorm:pumpkin_turret）` appears at least once, and the **first** occurrence is that form; in the English version `Pumpkin Turret (man瓜)` appears at least once, and the **first** occurrence is that form; `grep -rn "南瓜炮台"` is 0 in both documents.
  - Why: 4 objects contain "瓜" (`pumpkin_turret` / `melon_grenade` / `melon_slice_bomb` / `melon_grenade_stem`), and an alias points at the wrong one (document 28 §0.2).

- **N-79** Never write a "point-in-time fact" as an acceptance assertion.
  - Criterion: in acceptance and self-checks `grep -rn "调用点\|唯一调用\|只有 1 个调用"` is 0; every "there is only one place in the whole repository" becomes a countable **file count / key count / hit count**.
  - Why: A-37 — the binding regime introduced "detach binding", so the write entry is no longer unique; such an assertion always goes red after implementation, and there is no telling whether it is a defect or the expectation.

- **N-80** Never cite `verify_project.py`'s "FAIL 0" before it has been changed to this version's criteria.
  - Criterion: when "FAIL 0" appears in an acceptance record, that tool's diff must be given alongside (v0.1.1's expectation source added, `farmland.limitPerPlayer` removed from the union); before it is changed, cite only the sectional readings (`93 / 93`, `323 keys`).
  - Why: A-40 — its config expectation set comes from four old documents; after deleting 1 and adding 1, a green light can only come from a missed check.

- **N-81** Never load a chunk for settlement / comparison / display.
  - Criterion: in all three places — core-removal settlement, the comparison-key path, HUD overlay assembly — `getChunk` / `hasChunkAt` / `getBlockEntity` are all 0.
  - Why: A-35 and A-39 — one forced load cascades a batch of chunks and lets the result drift with loading timing.

- **N-82** Never fix A-49's cross-dimension same-coordinate collision.
  - Criterion: the ledger's farmland record still has only `pos` (no dimension field), `dataVersion` is still **2** (not raised to 3), and there is no migration code "adding a dimension to legacy saves".
  - Why: A-49 has ruled that this version does not fix it, because a legacy save cannot recover the dimension retroactively and the farmland side has already taken one save-format bump.

- **N-83** Never skip confirming an API to verify and just assume the signature.
  - Criterion: each of document 30 §8's 8 items has a record of "the minimal-case command + result"; before confirmation, do not "make it compile" with an empty implementation, an `UnsupportedOperationException` or commented-out functionality.
  - Why: this version's 6 technical components (packet / key / CLIENT config / client command / ticket / binding table) are all firsts in this repository.

- **N-84** Never change the values already fixed in documents 28 / 29 / 30 / 31.
  - Criterion: in `git status`, `docs/` shows 0 changes; any new number in the report must trace to a D number, or be marked "measured implementation value".
  - Why: the six documents are the frozen upstream; changing a criterion means changing document 28 first and registering it.

- **N-85** Never revive `farmland.limitPerPlayer` in any form.
  - Criterion: `grep -rn "limitPerPlayer\|baseOverride\|setBase" src/ tools/` is 0; the `[farmland]` section of `farmstorm-server.toml` is no longer read by any code; those 2 language keys have been deleted.
  - Why: D-139 / D-140 — it would instantly create a double source of "base + capacity".

- **N-86** Never stuff this version's new components into the registry.
  - Criterion: 93 and the 12 class subtotals are unchanged; `git diff src/main/java/com/dashpig/farmstorm/registry/` has only `FarmstormBlocks.java`'s two places (`:79`, `:82-89`).
  - Why: none of the new components enters `BuiltInRegistries` (document 30 §1.3); stuffing it in breaks both 93 and "+0" at once.

- **N-87** Never open a second knob for capacity / quota.
  - Criterion: in `FarmstormConfig`'s diff `pumpkin_turret.bulletSpeed` is the only new key, and `outpost_core.farmlandBonus`'s key name and default are unchanged; there is no new key such as `farmlandCapacity` / `outpost_core.capacity`.
  - Why: RT-33 — once the two knobs differ, nobody can answer "does this block count as over quota".

- **N-88** Never read the comparison key from a block entity.
  - Criterion: `OutpostCoreIndex.Entry` carries `gameTime` and the comparison path reads the index only; `getBlockEntity` has 0 hits in HUD overlay assembly and in `FarmlandGuard.check`.
  - Why: A-39 / document 30 §2.2.10 — whether a block entity exists depends on loading timing, so the value drifts.

- **N-89** Never let the client send back, and never make the allied determination on the client.
  - Criterion: `playToServer` / `sendToServer` have 0 hits; the client handler does one reference swap only; there is no `isAlly` call in client code.
  - Why: D-142's three boundaries — a client able to decide equals a client able to forge "allied".

- **N-90** Never change `sounds.json`.
  - Criterion: `git diff src/main/resources/assets/farmstorm/sounds.json` is empty; the `friend_chives` section still sets no `attenuation_distance`; `friend_chives.soundRange` still defaults to 32.0.
  - Why: the audible radius = `max(volume, 1) × attenuationDistance` is a **multiplication**; the current `2.0 × 16 = 32` already meets the target, and adding 32 would make it **64** blocks (document 30 §5).

- **N-91** Never let "200" appear a second time in the code.
  - Criterion: the hits of `grep -n "200" src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java` are allowed only inside comments (0 code hits); the field length comes only from `fieldLength` (reading `friend_chives.effectTicks`).
  - Why: A-33 — when the config is changed to 100 you get the contradiction "the field runs 200 ticks while each player only gets `100 − x`".

- **N-92** Never change `ThrownCropEntity`'s existing lifecycle hooks.
  - Criterion: `git diff src/main/java/com/dashpig/farmstorm/entity/ThrownCropEntity.java` is empty (this version only overrides the subclass's `keepAliveAfterImpact` and adds the subclass's `tick`); the base class's `keepAliveAfterImpact` still defaults to `false`, and `settle` still settles once only; **this file must not be changed in this version** (`keepAliveAfterImpact`'s default stays `false`, overridden by the subclass itself).
  - Why: 8 projectiles share this base class, and changing it equals changing all the projectiles at once (document 30 §3 rows 18, 42 "not changed").

## 4. Fact Card

Only source facts that "get it wrong if not written" are collected. **A name without a `file:line` is never written** — this repository has no class such as `Conprint`, and inventing a name from memory and putting it into code is a compile error.

| # | Fact | Value / current state | Evidence |
|---|---|---|---|
| 1 | The 93-object registration surface's 12-class assertions are hard-coded in the startup self-check | `32 / 26 / 7 / 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 / 1`, totalling 93 | `Farmstorm.java:138-141` |
| 2 | The server config currently has 166 keys / **59 groups**, of which 24 are **nested** sections (indented in the toml) | 35 top-level sections + 24 nested sections | `FarmstormConfig.java:268-928`; `run/config/farmstorm-server.toml:85` (`\t[blast_carrot.nausea]`) |
| 3 | `outpost_core.farmlandBonus` defaults to **16**, range 0 ~ 1024 | one key, two meanings: per-core capacity + the quota base | `FarmstormConfig.java:911-913` |
| 4 | `friend_chives.hitRadius` 6.0 (0 ~ 16), `effectTicks` **200** (20 ~ 1200) | both the field length and the application base take the latter | `FarmstormConfig.java:851-853`, `:854-856` |
| 5 | The `pumpkin_turret` group currently has 4 keys | `targetRadius 15` / `projectileDamage 1` / `knockbackLevel 1` / `fireIntervalTicks 60` | `FarmstormConfig.java:422-433` |
| 6 | `TurretBullet.SPEED` has **two** readers | deleting the constant requires changing both places | `entity/TurretBullet.java:95`, `behavior/impl/PumpkinTurret.java:75` |
| 7 | The whole repository has only 3 `SoundType` values, and `STONE`'s only precedent is `outpost_core` | `GRASS :79` / `STONE :88` / `CROP :100,:110` | `registry/FarmstormBlocks.java` |
| 8 | `SoundType` has five sound-category getters | `getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound`; changing the type changes all five together | the vanilla `SoundType`; the change point `FarmstormBlocks.java:79` |
| 9 | `outpost_core`'s registration chain has **no** `.lightLevel` | current value 0 | `FarmstormBlocks.java:82-89` |
| 10 | `FarmlandLedger.byOwner`'s outer layer is a **`HashMap`**, the inner layer a `LinkedHashSet` | "insertion order" only holds inside one and the same UUID | `FarmlandLedger.java:51` |
| 11 | `OutpostCoreIndex.byOwner` is a **`HashMap`**; `Entry` has only `(dimension, pos)` | this version adds `gameTime` to `Entry` and persists it | `OutpostCoreIndex.java:50`, `:53` |
| 12 | `FarmlandLedger`'s NBT has **only** `owners → positions → pos` (long), **with no dimension field** | a legacy save cannot recover the farmland dimension | `FarmlandLedger.java:46-48`, `:228-244` |
| 13 | `protectsFromTrample(ServerLevel, BlockPos)` has **no querier parameter**, and starts from online players | it can only answer "does this block have a binding" | `farmland/FarmlandGuard.java:120-134` (`:125`) |
| 14 | `keepAliveAfterImpact()` defaults to `false`, and **its only existing overrider is the firecracker** | this version is the second overrider | `entity/ThrownCropEntity.java:116-118`; `entity/thrown/BambooFirecrackerProjectile.java:42` |
| 15 | `settle()` is the single settlement point on a hit; only `!keepAliveAfterImpact()` leads to `discard()`; after `settled` the lifetime check no longer takes effect | `settled` is `private`, with no accessor | `entity/ThrownCropEntity.java:95-104`, `:65-72`, `:33` |
| 16 | `placedGameTime` is already persisted in the block entity | written at `:405`, read at `:413`, field at `:79` | `blockentity/OutpostCoreBlockEntity.java` |
| 17 | 1.21.1 **has no ticket persistence**; forced loading's only persistence is `world/data/chunks.dat` | `ForcedChunksSavedData.FILE_ID = "chunks"` | document 30 §2.5.1 (enumerating `net/minecraft/server/level/` one by one) |
| 18 | `verify_project.py`'s config expectation set is the union of **four old documents** | `01-DESIGN` + `12-TECH-0.0.2` + `18-TECH-0.0.3` + `24-TECH-0.1.0` | `tools/verify_project.py:222-237` |
| 19 | The version number has only one source | `mod_version=0.1.0` as it stands | `gradle.properties:11` |

> Two footnotes: ① the base-class file is **`entity/ThrownCropEntity.java`**, not under `entity/thrown/` (that layer holds the 12 projectile subclasses); ② `farmstorm-server.toml`'s nested sections are **indented**, so counting sections must use `^\s*\[` — `^\[` only counts the 35 top-level sections.

## 5. Known Traps (**the most important chapter of this version**)

Translate A-30 ~ A-49's 20 adjudications into "how you will step on it". Items 1 ~ 8 are this version's eight most expensive pits, so read these eight first; item 19 is not an adjudication but a counting pit measured while this chapter was being written (document 30's counting criterion is corrected per it); **item 20 is a meta-rule** — what it constrains is not the code but the criterion itself.

| # | Trap (how the implementer steps on it) | What happens if stepped on | Correct approach | Basis |
|---|---|---|---|---|
| 1 | Deleting `TurretBullet.SPEED` in one place only, assuming the constant has one reader | `behavior/impl/PumpkinTurret.java:75` still reads it ⇒ **compile failure**; if only the bullet's flight reads config while the launch speed stays old, you get two speeds, muzzle and flight | both places read `pumpkin_turret.bulletSpeed`; `grep -rn "TurretBullet.SPEED" src/` is 0 | A-30 |
| 2 | Replacing `wild_apple.png` with a 64×64 image but not bringing the generator to the same version | ① the generator unchanged ⇒ the next re-run silently swaps the image back to 16×16; ② the output becomes 64×64 while `expected_size()` (`:982-983`) still always returns 16×16 ⇒ `:1153-1154` throws `SystemExit` and **the whole generation round aborts** (the other icons stop too) | change the pass-through branch, `expected_size()` and the assertion together in the same version; after the change re-run once and it is still 64×64 | A-32 / D-126 |
| 3 | Using `setChunkForced` or `TicketController#forceChunk` to save trouble | both write `world/data/chunks.dat`, so after the server stops that chunk is still force-loaded, directly contradicting "runtime temporary" | bare `TicketType` + `addRegionTicket`, the release at the same place as `release()`, with a stop-server fallback | A-38 |
| 4 | Taking the `byOwner` / `orphans` Map / List iteration order as the order | the same save started twice gives different core bindings and different backfill results (`byOwner` is a `HashMap`) | candidate cores use the three-level key; backfill uses a `BlockPos` single-level total order; compare the two "fingerprint" lines across two startups | A-39 / A-47 |
| 5 | Writing `effectTicks` into the code as the constant 200 | with the config changed to 100 you get "the field runs 200 ticks while each player only gets `100 − x`"; filling the whole `effectTicks` renews the effect every tick | the field length reads `friend_chives.effectTicks` and applies `effectTicks − x`; there is no second 200 in the code | A-33 |
| 6 | Covering only the tick face but asserting "only 1 chunk is loaded" in acceptance | the ticket level propagates outwards, so the immediately adjacent ring is necessarily border-loaded (3×3) ⇒ the assertion is necessarily false and the acceptance is forever red | the acceptance covers the tick face only (centre `32` / adjacent ring `33`); the load face is not asserted | A-43 |
| 7 | Thinking the trample surface also has to judge the team, and adding a "querier" parameter to `protectsFromTrample` on the way | that function has no querier parameter, and the trample event itself carries no player; changing the signature ripples into existing call sites | the trample surface is "bound means protected"; the quota gate and the HUD overlay still judge real-time allied status | A-45 |
| 8 | Trusting `verify_project.py`'s green light right after the change | its expectation set comes from four old documents; after deleting 1 and adding 1 it necessarily mismatches ⇒ a green light can only come from a missed check | change the tool per A-40 first; before that, do not cite `FAIL 0`, cite only the sectional readings | A-40 |
| 9 | Treating the Compressed Hay Block's sound as "break + place, two categories" | step / mine / land are still grass sounds, the acceptance is 2 items short, and players hear it the moment they walk on it | change all five categories together; compare `/playsound` positively and negatively, category by category | A-31 |
| 10 | Squeezing `+N` into the 8 rows | the 9th core eats the `+N`, the panel is 83 px, and it does not match the 93 px acceptance | 8 core rows + **1 additional** `+N` row = 9 rows / 93 px | A-34 |
| 11 | Writing the backfill per A-36 (`byOwner` insertion order) or A-44 (`orphans` insertion order) | A-36 has been superseded by A-47 and A-44 is void; a 0.1.0 legacy save has no `orphans` key, so the order is still not reproducible | recognise only A-47: a `BlockPos` single-level total order (y → z → x) | A-36 / A-44 / A-47 |
| 12 | Writing "this method has only one call site in the whole repository" into the acceptance | the binding regime's "detach binding" makes the write entry non-unique ⇒ the assertion necessarily goes red after implementation, with no telling whether it is a defect or the expectation | replace every such point-in-time fact with a countable file count / key count / hit count | A-37 |
| 13 | Calling `getChunk` on an unloaded chunk just to turn an unmigratable field into dirt | one forced load cascades a batch of chunks; and "the farmland under a core sits in an unloaded chunk" cannot happen at all (the ticket covers the same 3×3, A-60) ⇒ writing a branch for "not loaded" is dead code | do not write a separate branch for "not loaded" — the ticket already guarantees the chunk of the farmland under a core is in memory; the real red line is **never load a chunk for the settlement** (`getChunkNow` throughout) | A-35 / A-60 |
| 14 | Letting two HUD overlay layouts coexist (row height 10 / row height 9) | two people compute 83 px and 93 px respectively, and the acceptance has to pick one | recognise only row height 9 / row gap 1 / padding 2, panel height `10n + 3` | A-41 |
| 15 | Stopping at document 30 §3's 44 rows | the row "delete the whole `farmland` config group" is missed (the action surface should be **47 = 44 + 3**: 44 rows + A-42's one row + A-55's two rows), and `[farmland]` remains in the source | delete the whole group, and add that row to the change-surface list | A-42 |
| 16 | Taking document 30 §7's 38 acceptances and document 29 §11's 39 boundaries as a one-to-one correspondence | 21 boundaries have no corresponding assertion, and "no assertion = no boundary" is wrongly assumed, so the boundary list is quietly cut | boundaries follow document 29; the 38 acceptances and the 39 boundaries are **not required to correspond one-to-one** (A-46); **21** of the boundaries have no corresponding assertion in §7, covered by **F-210 / F-211** with 14 / 7 each, and must be named item by item | A-46 |
| 17 | Missing the 2 `key.farmstorm.*` entries when counting language keys | it comes out as **321**, the lang section is forever 2 keys short, and the Chinese and English key sets end up unequal | `315 − 2 + 10 = 323`; 323 each and `set(zh) == set(en)` | A-48 |
| 18 | "Fixing" farmland's cross-dimension same-coordinate collision on the way (adding a dimension field to the ledger) | triggers this version's second save-format bump; a legacy save cannot recover the dimension and can only be defaulted or lazily backfilled, introducing a new non-reproducible surface | this version **does not fix it**, only registers it as a known limitation; the ledger still has only `pos` | A-49 |
| 19 | Counting the toml's sections with `^\[` | nested sections are **indented** ⇒ it only counts the 35 top-level sections (34 after the change), mismatching 58 and being misjudged as "the group count is wrong" | count with `^\s*\[` (59 before, 58 after); count key lines separately with `^\s*[A-Za-z_].*=` | document 30 §7 #2's counting criterion (corrected by measurement in this chapter) |
| 20 | **The criterion's own failure (meta-rule)**: before any grep / `git diff` / counting criterion goes live, **run it on the current state first** — it should already be 0 or some number now; first confirm that number can be computed | `^\[` missing the indented sections, a grep missing the `src/main/java/` prefix hitting 0 forever, a `git diff` with a wrong pathspec always being empty: all three are "the criterion always passes or always fails", more insidious than a wrong number | run every criterion once **before** the change and report the measured value, then once after; only when both can be computed does the criterion stand | A-52 / A-57 / A-58 and the other rows of this table |

> Landing method: write each criterion's "measured value before the change" into the task report, and report again after the change; a "passed" without a number is treated as unverified.

## 6. Handling Conflicts with the 0.1.0 Documents

Documents 22 ~ 27 are frozen — **not one character changes** — and they still contain old criteria (document 28 §4.5 lists the six documents that must carry the new criteria). When you hit a conflict, read it through the two tables below; do not change the frozen documents back, and do not write an old criterion into a code comment as a "known fact".

### 6.1 Criterion conflicts

| What the 0.1.0 document says (location) | What this version goes by | Decision |
|---|---|---|
| `22-DECISIONS-0.1.0.md:44`, `:68`, `:480` and `26-PROMPT-MAIN-0.1.0.md:183-185` (HC-73): "0 custom network packets" is a red line | the red line is **abolished outright**, leaving only "no Mixin"; this version has 1 S2C read-only packet, **with no ceiling** (§1.2) | D-142 / D-146 |
| document `22` D-118: "farmland only inside an allied core's 3×3, multi-core ranges take the union", the protection surface = allied farmland inside the range | the range gate is kept; **ownership** becomes a unique binding: one block belongs to 1 core only, per-core capacity 16 | D-134 / D-135 |
| document `22` D-116 / D-117: quota = `base + bonus × the number of cores`, carried by `farmland.limitPerPlayer` + `/farmstorm farmlandlimit` | the quota = **`16 × the number of allied cores`**; `base`, that config key and the whole subcommand are deleted together | D-139 / D-140 |
| document `22` D-116, document `25` and `README.md:191`: the farmland rule goes through `/farmstorm farmlandlimit` | the server-side `/farmstorm` **no longer exists**; only the client-side `/farmstorm hud <on\|off\|reset>` remains (a separate registration, not sharing the root node's semantics) | D-140 / D-144 |
| `22-DECISIONS-0.1.0.md:581`, `26-PROMPT-MAIN-0.1.0.md:199`, `27-PROMPT-TASKS-0.1.0.md:405`: `wild_apple.png` is "11 colours / 126 transparent" | measured **88 distinct RGBA / 107 fully transparent / 571 B**; this version switches to the **64×64** original image (source image 6,946 B) | D-126; document 28 §7 item 2 |
| document `22` §6 item 4, `10-DECISIONS-0.0.2.md:73-76` (D-50), `00-DECISIONS.md` (C-6): "no custom packets / `CustomPacketPayload` still 0" | this version has **exactly 1** S2C read-only packet (`farmstorm:core_list`); the old prohibition is only a historical criterion and is no longer re-checked | D-142 / D-146 |
| document `22` §7.2's D-57 row: the Compressed Hay Block's `sound(GRASS)` does not move | change it to `SoundType.STONE`, and **all five** sound categories change | D-130 |
| document `22` D-120 item 4: when a core is destroyed, **all** allied farmland inside the range turns to dirt | migrate under that core first, and turn to dirt only what cannot migrate (**two levels: rebind / turn to dirt**, A-60) | D-136 / A-60 |
| document `22` D-121 item 2, `23-DESIGN-0.1.0.md:49`: zero new client code | client **+5 classes** (keys / HUD overlay rendering / drag Screen / client commands / snapshot) | D-142 / D-143 / D-144 |

### 6.2 The Superseded 0.1.0 Hard Constraints (explicitly declared)

| The 0.1.0 master prompt's entry | What it said at the time | This version | Basis |
|---|---|---|---|
| HC-53 | config 166, language keys **315** | config is still 166 (composition changed), language keys **323** | D-139 / D-144; A-48 |
| HC-60 | farmland rule = config default + `/farmstorm farmlandlimit` | both deleted; the quota is the sum of the per-core capacities | D-139 / D-140 |
| HC-61 / HC-62 | interception order "range → quota"; quota = `base + 16 × the number of cores`, bookkept by a coordinate set | the order is kept, the second gate becomes "single-core free slot"; quota = `16 × the number of allied cores`, and the bookkeeping carrier becomes the binding table | D-134 / D-135 / D-139 |
| HC-65 | when removing a core, iterate only the ledger's coordinates and turn them straight to dirt | migrate first, then the three branches; process only what is under that core | D-136 |
| HC-69 | the boundary-particle clause says "0 custom network packets" | delete that sentence; particles still go through vanilla packets | D-146 |
| HC-73 | 0 custom network packets, no Mixin, `GameRules.register` 0 | **the half of HC-73 about network packets** is void (this version has 1 S2C); the two sentences on no Mixin and on `GameRules.register` are kept (§1.1, §1.3) | D-146 |
| HC-76 | `wild_apple` 16×16 / 11 colours / 126 transparent | **64×64 original image**; the model's `layer0`, the item name and the language keys do not move | D-126 |

## 7. Self-Check at the End of Every Round

Run the table below when a task or a Phase closes; what was not run does not count as done. All commands run from the repository root, with paths in local form.

| # | Check item | Command / observation point | Pass criterion |
|---|---|---|---|
| 1 | Compile and build | `.tools\gw.cmd compileJava`, `.tools\gw.cmd build` | exit code 0; the artifact `build/libs/farmstorm-0.1.1+mc1.21.1.jar`, with `dist/` kept in sync under the same name |
| 2 | DataGen | `.tools\gw.cmd runData` | both lang files land in `src/generated/resources`, **323** keys each |
| 3 | Registration surface | the startup log's first line | `自检 注册对象 93 个：… → OK`; the 12 classes' **numbers** match HC-77, and the class names follow the log string |
| 4 | server config | delete the old toml → start the server → read `run/config/farmstorm-server.toml` | `^\s*\[` lines **58**, key lines **166**, no `[farmland]`; `[pumpkin_turret]` **5** keys with `40` / `3` / `0.45` |
| 5 | client config | after starting the client, read `run/config/farmstorm-client.toml` | exactly **3** keys: `true` / `4` / `4` |
| 6 | Language keys | both lang files + grep | **323** each, equal key sets, `farmlandlimit` 0, `"farmstorm.configuration.` exactly **169** |
| 7 | Red-line grep | under `src/main/java`: `@Mixin\|mixins.json`; `CustomPacketPayload`; `playToServer\|sendToServer`; `GameRules.register` | in order **0** / **exactly 1 file** / **0** / **0** |
| 8 | Prohibited-item grep | under `src/` and `tools/`: `limitPerPlayer\|baseOverride\|setBase`; `setChunkForced\|forceChunk`; `TurretBullet.SPEED` | all **0** |
| 9 | Log assertions | start the server, remove a core, run the legacy save once each | `耕地补建：绑定 N 块 / 保持未绑定 M 块`; `核心比较键指纹 <hex16>` and `耕地绑定指纹 <hex16>` **completely identical across two consecutive runs** |
| 10 | jar count | `jar tf build/libs/farmstorm-0.1.1+mc1.21.1.jar` | `network/CoreListPayload.class` **1**; `FarmstormKeys` / `CoreHudRenderer` / `CoreHudDragScreen` / `CoreHudClientCommands` / `ClientCoreList` **1** each; `command/FarmstormCommands.class` **0** |
| 11 | Persistence check | `world/data/chunks.dat`, `world/data/farmstorm_farmland.dat` | the `Forced` array contains no core coordinate; the ledger contains `dataVersion: 2` |
| 12 | Out-of-bounds check | `git status --short` | only new files and the existing files named by document 28; `docs/` **0** changes |
| 13 | Tool applicability | `python -X utf8 tools/verify_project.py` | after changing it per A-40: `93 / 93`, config **166**, lang **323**, `FAIL 0`; **before that, cite only the sectional readings** |

The report's three evidence lines are fixed: **the command as written + the key output lines + this table's item number** (or an HC / N number). For a log longer than 20 lines keep only the first error and the summary line. This file does not answer "which thing to do this round" — that is document 33's current Phase's business.

## 8. Pre-Start Confirmation and the Numbering Relay

### 8.1 Four Things That Must Be Confirmed Before Starting

| # | What to confirm | Where to look | What to do if it does not hold |
|---|---|---|---|
| 1 | Which decision this task falls under, and which numbers are already fixed | document 28 §1's D entries (four fields: conclusion / basis / landing site / supplement) | no matching D entry ⇒ stop and ask the author, do not decide the criterion yourself |
| 2 | Whether the APIs to be used have been verified | document 30 §8's 8 items (each gives "the way out when unsure") | write the minimal case and get it running first, then put the conclusion into the report (N-83) |
| 3 | Whether `verify_project.py` has been changed to this version's criteria | `tools/verify_project.py:222-237` and document 30 §3 row 43 | if not, do the tool step first; before that, do not cite `FAIL 0` (N-80) |
| 4 | Whether the run directories are isolated | `-PfsRunDir=` and `gw.cmd`'s `runServer` directory lock | this version runs `runServer` (verifying bindings / tickets) and `runClient` (verifying the HUD overlay) **at the same time**; colliding directories overwrite each other's logs |

The four old pits that run through the whole repository still hold: `neoForge { }` is case-sensitive; `.tools\gw.cmd` is the only build entry (do not call gradle directly); two `runServer`s must be separated with `-PfsRunDir=`; ffmpeg is not on PATH (this version changes no audio — do not go change `tools/convert_media.ps1`).

### 8.2 The Numbering Relay (reference only, do not open new numbers)

| Series | Previous version reached | This version starts at | How this file uses it |
|---|---|---|---|
| Decision D-xx | D-123 | **D-124** | appears in references only, no new ones opened |
| Task F-xx | F-175 | **F-176** | issued by document 31, this file does not advance it |
| Phase | 25 | **26** | issued by documents 31 / 33 |
| Milestone M-xx | M21 | **M22** | issued by document 31 |
| Hard constraint HC-xx | HC-76 | **HC-77** | this file issues HC-77 ~ HC-96 |
| Prohibition N-xx | N-74 | **N-75** | this file issues N-75 ~ N-92 |
| Risk RT-xx | RT-30 | **RT-31** | issued by document 30 §6, this file only references it |
| Adjudication A-xx | A-29 | **A-30** | issued by document 31's adjudication chapter; **A-44 is void, A-47 is rev.2, A-49 is a known limitation** |
| Document number | 27 | **28** | this version is 28 ~ 33 |

Two disciplines: ① code comments carry their numbers (for example `// D-131：场长读 config，不写死 200`), and a guessed sentence without a number is not written; ② numbers are referenced only, never opened — writing a number this version does not have, such as `RT-42` or `A-50`, leaves the next reader a reference that cannot be found.
