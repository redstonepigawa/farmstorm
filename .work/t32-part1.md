# 32 · v0.1.1 Master Prompt

> How it is fed: this file is fed **once only**, before any Phase. After that feed document 33 one Phase at a time.
> Applies to: v0.1.1 (the baseline v0.1.0 has been implemented and released). On conflict the decision record of document 28 wins.

| Item | Value |
|---|---|
| Document ID | 32-PROMPT-MAIN-0.1.1 |
| Audience | the implementation agent (can read and write this repository, can run `.tools\gw.cmd`) |
| Upstream | `28-DECISIONS-0.1.1.md` (the single source of truth, D-124 ~ D-147) → `30-TECH-0.1.1.md` → `29-DESIGN-0.1.1.md` → `31-TASKS-0.1.1.md` |
| Numbers this file issues | **HC-77 ~ HC-96 (20 clauses)**, **N-75 ~ N-92 (18 clauses)**, the 19 rows of the fact card, 20 traps (including 1 meta-rule that self-checks the criteria); risks continue **RT-31 ~ RT-41**, rulings cite **A-30 ~ A-49** |
| What this file does not write | the per-phase content (33), tasks and scheduling (31), class design and algorithms (30) |

## 0. Who you are, and what you are to do

You are this repository's **implementation agent** for v0.1.1. The baseline is **0.1.0, already released** (`gradle.properties:11` now reads `mod_version=0.1.0`, and `dist/` holds the 0.1.0 artifacts), and this version is an increment only: 24 decisions (D-124 ~ D-147) land on the registration surface, config, ledger, client, generators and build scripts that 0.1.0 already has — **do not rewrite what 0.1.0 already implements**, and do not open a second framework. **Read document 28 before you write a line** — it is the single source of truth, and where it conflicts with this file or with document 30 it wins; this file is harder than document 28 in exactly two things: the **new red-line wording** (§1) and the **execution discipline** (§2 onwards). You see only this file plus the current Phase's task order from document 33, so **do only what the task order asks**: stop when done, report, and wait for the next Phase; do not implement the next stage in advance, and do not stuff "I fixed that on the way" into this round. Never invent a value: copy what you can trace to a D number / an RT number / a `file:line` in the source, and put what you cannot trace under "open questions" in the report — do not decide it yourself. The tracking, traps, growth, deployment, sounds, particles, index, menus and other behaviour that 0.1.0 already implements **do not change by a single line** in this version, unless a decision in document 28 names it. Every sentence of "done" in the report must land on one observable piece of evidence (a log line, a command reading, `git diff`, `jar tf`, the save NBT, an in-game reading); "it behaves normally" is not accepted. Also record one **pre-existing defect**: `检查环境.cmd:14` and `一键构建.cmd:11`/`:12` still hard-code the `0.0.1` jar name, so running as far as `检查环境.cmd:14` today reports the jar as missing — this was left over at 0.1.0, is not a new requirement of this version, and this version fixes it along the way (A-51).

## 1. Red Lines (**this version's wording has changed — you must follow this section**)

### 1.1 Kept: no Mixin

The only project-level red line that is kept. **You must not** have `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor`, and you must not add the mixin plugin to `build.gradle`.

- Criterion: `grep -rn "@Mixin\|mixins.json" src/main/java` has **0 hits**; `git diff build.gradle` **is empty**; there is no `*.mixins.json` under `src/main/resources`.
- Source: D-146; `BRIEF` §1.4 (`@Mixin` measured 0, `build.gradle` has no mixin plugin).

### 1.2 **Abolished: 0 custom network packets**

The red line of v0.0.1 ~ v0.1.0 was "custom network packets, **0** of them". **From v0.1.1 on, this red line is abolished outright.** This version happens to add **1** S2C read-only packet (D-142), but **sets no cap on the count**.

Four ways to read that; get any one of them wrong and all six downstream documents are wrong:

1. **It is abolished outright, not "relaxed to 1"**. There is no cap, and there is no action space called "applying for an exception".
2. **It is not "an exception for this version"**. The original structure was "banned by default + exempted this once"; that "banned by default" no longer exists. The word "exception" implies the ban is still there and is banned vocabulary.
3. **Abolition does not mean packets must be added.** This version adds exactly 1, because the HUD overlay (the overlay) really needs it (the client cannot get the server's `SavedData`); each further packet must still be **registered by a decision of its own** and must not be added on the side — the one this version registers is D-142.
4. **"No Mixin" is unaffected**: still zero, still banned (§1.1).

**List of the old red-line wording** (every place below says "0", and they have **all been abolished outright by document 28 §5**):

| Document | Location | Old wording | Current status |
|---|---|---|---|
| `00-DECISIONS.md` | C-6 | "0 custom S2C packets" | **Abolished outright**; the rest of C-6 (sounds / particles go through vanilla packets) is still an implementation fact, but no longer a prohibition |
| `10-DECISIONS-0.0.2.md` | `:73-76` (D-50) | "the three intrusive effects add no `CustomPacketPayload`" | **The project-level prohibition is abolished**; those three effects' implementation facts do not change, and this version does not touch them |
| `22-DECISIONS-0.1.0.md` | `:44`, `:68`, `:480` | "0 custom network packets" written as one of the two red lines | **Abolished outright** |
| `26-PROMPT-MAIN-0.1.0.md` | `:183-185` (HC-73), `:402` | same as above, as a hard constraint and as a cross-reference row | **The half-sentence in HC-73 about network packets** is void (not "half the ban is still in force"); the Mixin sentence and the `GameRules.register` sentence remain valid (§1.3) |
| `27-PROMPT-TASKS-0.1.0.md` | acceptance criterion | "`grep CustomPacketPayload` is 0" used as the pass criterion | **The criterion is void**; it becomes "exactly 1 file, and the client does not send back" |
| `README.md` | `:71`, `:148`, `:191`, `:419`, `:423` | the red-line section says "0 custom network packets" | inside the README's increment scope, changed with D-146 |

**Why this 1 packet is indispensable** (four points; any one of them alone is enough to reject the "0 packets" path, and they are also your reason that you **must not** delete it to save trouble):

1. The core list is the **server-side `SavedData`** (`world/data/farmstorm_outpost_core.dat`), and the client **has no copy**;
2. "which cores do I have" must be **complete**: the client loads only the chunks inside view distance, and a core outside view distance is one it does not even know exists;
3. The owner **may be offline**, and only the server can resolve "teammate relationships";
4. The list needs a **stable order** and the "8 rows + `+N`" overflow, which can only be drawn with a complete snapshot in hand.

**When implementing, this file wins**: do not delete this packet because some old document says "0", and in the other direction do not read "abolished" as "this version may add packets freely". This packet's three boundaries (read-only, no reply, server-authoritative) are written in HC-87.

### 1.3 Engineering constraints (not red lines, but just as unbreakable)

Document 28 §4 lists only one red line, "no Mixin"; the two below are not on the red-line list, but breaking them is as bad as breaking a red line — break one and this round is judged incomplete.

- **Do not try to register a real gamerule**: on 1.21.1 `GameRules#register` is `private static`, and inside "no Mixin" there is no registration entry. Criterion: `grep -rn "GameRules.register" src/main/java` has **0 hits**; the only read point of `GameRule` in the repository is `explosion/FarmstormExplosion.java:109` reading the vanilla `RULE_MOBGRIEFING` (this one place must not be changed).
- **Server authority**: hit determination, farmland binding, ledger settlement and packet sending all live on the server; the client only renders — it does not determine, does not send back and does not read the world. Criterion: the client handler does one reference replacement only; `grep -rn "playToServer\|sendToServer" src/main/java` has **0 hits**; a dedicated server starts with no `NoClassDefFoundError`.

## 2. Hard Constraints (HC-77 ~ HC-96)

The HC clauses constrain only this version's increment surface. 0.1.0's HC-51 ~ HC-76 remain in force, but 8 of them are explicitly superseded by §6.2 (HC-61 / HC-62 are merged into one row, so 8 clauses correspond to 7 rows) — **on conflict, this file and document 28 win**. Each clause gives three things: the **constraint** (what written counts as right), the **criterion** (what read counts as passing) and the **source** (decision / tech / risk numbers). The criterion column is always executable: one command, one place in `git diff`, one log line, one block state.

- **HC-77 · Registration is still 93, checked class by class by name**
  - Constraint: the 12 class subtotals must still be `items 32 / blocks 26 / block_entity_type 7 / entity_type 13 / mob_effect 4 / sound_event 3 / recipe_type 1 / recipe_serializer 1 / creative_mode_tab 1 / damage_type 3 / data_component_type 1 / menu_type 1`, **93** in all. **None** of this version's additions goes into the registry: the payload goes through `RegisterPayloadHandlersEvent`, the keys go into the client's `Options#keyMappings`, the CLIENT config goes through `ModConfigSpec`, the client command goes through Brigadier, the binding table is a `SavedData`, and the ticket is a plain object. The 12 assertions at `Farmstorm.java:138-141` **do not change**.
  - Criterion: the startup log contains `自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK`; `Farmstorm.java:138-141` shows 0 changed lines in `git diff`; all 12 classes are checked **one by one by name** (do not change the total without the list, and do not change the list without the total). **The numbers must match the log, and the class names follow the log string**: the constraint's `block_entity_type` is written `block_entities` in the log, so the two can never be identical character for character.
  - Source: D-142 / D-144 / D-145; document 28 §3.4 "registration surface +0"; document 30 §1.2, §1.3.

- **HC-78 · server config is 166 keys / 58 groups, and `farmland.limitPerPlayer` does not exist**
  - Constraint: after the change the server config is **166 keys / 58 groups** (delete `farmland.limitPerPlayer` and its whole group, add `pumpkin_turret.bulletSpeed`); that key **does not exist** in the source, in the generated source, in the language keys or in the default toml; the `pumpkin_turret` group has **5 keys**.
  - Criterion: **delete** `run/config/farmstorm-server.toml` first and then start the server; in the regenerated file the `^\s*\[` line count is **58**, key lines **166**, and there is no `[farmland]` section; `grep -rn "limitPerPlayer\|farmlandLimitPerPlayer" src/` **0 hits**. A `[farmland]` section left behind in an old toml **does not count as a failure** (the config system only adds, never deletes), but no code may read it.
  - Source: D-139; A-42; document 30 §4.1; RT-33.

- **HC-79 · CLIENT config is 3 keys, in a separate file, not counted in 166**
  - Constraint: add one `ModConfig.Type.CLIENT` spec with the 3 keys `hud.enabled` (bool, **true**) / `hud.x` (int, **4**) / `hud.y` (int, **4**), landing in `config/farmstorm-client.toml` (in a dev environment it actually lands in `run/config/farmstorm-client.toml`, alongside the server one); it is **not counted in 166** and is not written into the server file.
  - Criterion: after the client's first start `run/config/farmstorm-client.toml` exists with exactly 3 keys / 1 group; `farmstorm-server.toml` has no `hud` section; the server's group count is still 58.
  - Source: D-144; document 30 §4.2, §2.4.2.

- **HC-80 · Language keys 323 × 2, and the Chinese and English key sets are identical**
  - Constraint: after the change each of the two language files has **323** keys (`315 − 2 + 10`), and `set(zh) == set(en)`; `farmstorm.configuration.*` is exactly **169** (= 166 server + 3 client); the additions are **2** `key.farmstorm.*`, **3** `command.farmstorm.hud.*` and **1** `gui.farmstorm.core_hud.self`; the 2 deleted keys include `farmlandlimit`. `ModLangProvider`'s class comment is changed to match (the key counts in the comment are part of the artifact too).
  - Criterion: `zh_cn.json` and `en_us.json` each have **323** keys and their key sets are equal; `grep -c farmlandlimit` is **0** in both; `grep -c '"farmstorm.configuration\.'` is exactly **169**.
  - Source: D-129 / D-140 / D-144; A-48; document 30 §4.3; RT-40.

- **HC-81 · One farmland block belongs to 1 core, and per-core capacity = `outpost_core.farmlandBonus`**
  - Constraint: at any one moment a farmland block has **0 or 1** bindings; per-core capacity = `outpost_core.farmlandBonus` (default **16**, range 0 ~ 1024), with **no new key**; team capacity = that key × the number of allied cores; "used" = the number of binding entries; old entries that cannot be backfilled (`orphans`) **count towards neither capacity nor used**.
  - Criterion: under the same core, hoeing the **17th** block is refused and the save's `bindings` still holds **16** entries for that core; after changing `farmlandBonus` to **2** the **3rd** block is refused and the team capacity becomes `2 × cores`; the same `pos` appears exactly 1 time in `bindings`; the count of `orphans` does not appear in the capacity log.
  - Source: D-134 / D-139; RT-33; document 30 §2.2.6.

- **HC-82 · The comparison key is three-level, and it reads the index and the coordinates only**
  - Constraint: every determination that "picks one among several candidate cores" (tilling's core binding, migration, backfill) uses **the same** three-level key: `placedGameTime` ascending → **dimension id** ascending → `BlockPos#compareTo` (y → z → x). The comparison values are **read only from `OutpostCoreIndex` (a `SavedData`)**, not from block entities, and no chunk is loaded; **do not depend on the iteration order of any `Map` / `List`**.
  - Criterion: after offline-editing two cores' `gameTime` to the same value and restarting, the order is by dimension id ascending; with the same dimension and the same `gameTime` it is by `BlockPos#compareTo`; running the same save twice gives two identical `核心比较键指纹 <hex16>` log lines; `getBlockEntity` / `getChunk` / `hasChunkAt` have **0 hits** on the comparison path.
  - Source: D-135; A-39; document 30 §2.2.7, §2.2.10.

- **HC-83 · Legacy-save backfill uses `BlockPos`'s single-level total order, and the whole segment writes nothing to the world**
  - Constraint: startup backfill (including the first backfill of a 0.1.0 legacy save) first sorts the candidate farmland blocks by `BlockPos#compareTo` (y → z → x) as a **single-level total order**, then processes them one by one; do not use `byOwner` / `orphans` insertion order; where backfill fails, **leave the farmland untouched**; the whole segment has no `setBlock`.
  - Criterion: running the same 0.1.0 legacy save twice gives exactly the same N and M in the log line `耕地补建：绑定 N 块 / 保持未绑定 M 块`; `setBlock` has 0 hits in the backfill segment; every coordinate that could not be backfilled returns true for `/execute if block … minecraft:farmland`.
  - Source: D-137; A-47 (rev.2, superseding A-36 / A-44); document 30 §2.2.8; RT-32.
