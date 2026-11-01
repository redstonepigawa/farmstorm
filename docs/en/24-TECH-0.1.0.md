# 24 · v0.1.0 Technical Specification

> This document is the technical specification for "Farmstorm" v0.1.0: the measured registry increment, the class designs, the deterministic advance of growth time,
> the four trap-system reworks, the farmland quota and the Outpost Core, command registration, the config and resource inventories, the risk register RT-24 ~ RT-30, and the acceptance self-check table.
> The single source of truth is `docs/zh/22-DECISIONS-0.1.0.md` (D-103 ~ D-123, 21 in total); where they conflict, that file prevails.
> The baseline is **the implemented and released 0.0.4** (`dist/farmstorm-0.0.4+mc1.21.1.jar`, 959,376 B).
> Every source-code fact is taken from `src/main/java/com/dashpig/farmstorm/`; line numbers follow the 0.0.4 release code. Every number is counted out by this document itself, naming each entry from the source (§1).

---

## 0. Scope of This Document and Its Relation to 18-TECH-0.0.3.md

### 0.1 What This Document Is Responsible For

| Item | Content |
|---|---|
| Version | 0.1.0 (an incremental release; the build baseline is unchanged: MC 1.21.1 / NeoForge 21.1.213 / Java 21) |
| In scope | the **measured verification and item-by-item naming** of the +3 registry increment; the design of the eight new classes and of the nineteen items' change surface (18 changed + 1 verified and left unchanged); growth time's scheduled-tick chain; the mount points of the four trap reworks; the three chains of the farmland quota and the Outpost Core; the registration and permission of `/farmstorm farmlandlimit`; the full config inventory (12 rows); the resource and language-key inventories; RT-24 ~ RT-30; the 18 acceptance self-check items |
| Out of scope | the gameplay narrative and timing tables of the 9 requirements (`23-DESIGN-0.1.0.md`); task breakdown and effort (`25-TASKS-0.1.0.md`); prompts (`26` / `27`) |
| Files left untouched | 00 – 23 under `docs/zh` and the whole of `docs/en` (frozen); `src/` is untouched by a single line during the writing of this document |

### 0.2 Relation to `docs/zh/18-TECH-0.0.3.md`

This document carries over every convention established by document 18 and invents none:

| Convention | Where it lives in document 18 | How this document inherits it |
|---|---|---|
| The three evidence chains of the baseline check | §1.1 | §1.1 keeps "read the construction sites item by item → cross-check against the startup self-check → cross-check against the existing document", and lists the call count of every factory method this document uses |
| The per-category measured table | §1.2 | §1.2 likewise gives a 12-row table + a per-category subtotal code block under the table + a verification conclusion under the table |
| Registering baseline inconsistencies | §1.4 | §1.4 registers **verify_project.py's actual run output and three gaps**, and states "which side may be wrong" |
| The per-registry-object table | §2.1 / §2.2 | §2 keeps the four-column measure "registry × baseline × increment × target" + the item-by-item naming table |
| The four items of a class design | the opening of §3 | §3 keeps four items (responsibilities / key methods / which config keys it reads / which existing files it changes) |
| The per-key config inventory | §8.1 | §8.1 keeps the same column set (group.key / type / default / range / use / D), and the row count is counted first and stated afterwards |
| The resource inventory | §9.1 / §9.3 / §9.4 | §9 splits into three tables — PNG / JSON / language keys — each with its own category subtotal |
| The risk register | §10 | §10 gives RT-24 ~ RT-30, with the fields also five (risk / trigger / primary mitigation / fallback / how to verify during implementation) |
| The "to be verified" register | §0.5 | §0.5 is carried over; this document's 8 places each hang off one specific RT |
| Numeric discipline | §0.4 | §0.4 is carried over; this document **is allowed** to state registry totals — because they are already measured values (§1.2) |

**Relation to document 18 in one sentence**: document 18 is the specification that "adds another 13 objects on top of 0.0.2 and moves 77 to 90"; this document is the specification that "adds another 3 objects on top of 0.0.4 and moves 90 to 93". The addition is word for word the same, with only one difference: document 18 could use 77 as the baseline because one line of the source's startup self-check asserts those 12 numbers; this document can use 90 as the baseline because the same assertion asserts exactly 90 in 0.0.4, and §1.2's per-category measurement equals it item by item.

### 0.3 Self-Consistency Commitment for the Three Inventories

This document's three inventories (§2 registry objects, §8 config keys, §9 resource files) each satisfy:

1. **Row count = the declared count**: every table states "this table has N rows", and N equals the data rows counted one by one — never write the number first and pad the rows afterwards.
2. **Subtotal = the sum of the rows**: all three tables print a category subtotal below the table, and the subtotals add up to the number of table rows.
3. **No overlap and no omission across tables**: registry objects go into §2 only; config keys into §8 only; PNG / JSON / language keys into §9 only. `DeployedKind`'s 8 enum constants and `CropProfile`'s 22 profiles are not registry objects: they appear only in §3 and §5 and enter no count.

### 0.4 Numeric Conventions

| Category | Convention | Source |
|---|---|---|
| Measured | 90 registry objects (per category in §1.2) | counted by reading the 11 `registry/*.java` item by item, and equal item by item to the 13 assertions of `Farmstorm#logRegistrySelfCheck` (`Farmstorm.java:138-141`) |
| Measured | 155 config keys | 155 `define*` calls in `FarmstormConfig`; `farmstorm.configuration.*` language keys are 155 as well (measured) |
| Measured | 299 language keys | 299 each in Chinese and English, identical key sets (measured here, the same value as `tools/verify_project.py` §3's output) |
| Measured | 22 crop profiles / 19 lucky berry pool entries | `util/CropProfile.java:131-143`; the startup self-check asserts 22 / 19 (`Farmstorm.java:103-113`) |
| Measured | resources: blockstates **25**, `models/block` **70**, `models/item` **31**, `loot_table/blocks` **25**, `recipe` **28**, `advancement` **18** | counted directory by directory under `src/generated/resources` |
| Increment | +3 (registry) / +11 (config keys) / +16 (language keys) / +5 new PNG + 6 replaced in place / +5 new JSON + 2 replaced in place | §2 / §8 / §9 of this document |
| Target | 93 registry objects, 166 config keys, 315 language keys | measured value + increment, itemized in §2.4 / §8.3 / §9.4 |
| Forbidden | any registry total not backed by this section's §1.2 measurement; any number that appears out of thin air (one with no source and no "this document's supplement" mark is a defect) | §0.3 of this document + construction fact card §6 item 2 |

### 0.5 Registration Rule for "To Be Verified"

This document invents no Minecraft / NeoForge API. Wherever this document cannot find a precedent for a call in the existing source of this repository, that place is marked "**to be verified**" and the bracketed text names the RT number it hangs off. There are **8 such places** in this document (① – ⑧), distributed as RT-24 three, RT-25 one, RT-26 one, RT-28 two, RT-30 one (8 in total). The bracketed references in the body text number **9** — ① is cited once each in §4.3 and §4.4, and ⑤ once each in §3.3 and §3.4, so the same to-be-verified item is cited in two places. A call marked to be verified must first be proven with a minimal case during implementation before the real logic is written; if it cannot be made to work, switch to the alternative path written in that RT's "fallback" column and record the conclusion in the acceptance record.

| # | Call to be verified | Hangs off | Why this repository has no precedent |
|---|---|---|---|
| ① | the 1.21.1 signatures and call timing of `Level#scheduleTick(BlockPos, Block, int)`, `Block#tick(BlockState, ServerLevel, BlockPos, RandomSource)`, `Block#onPlace(BlockState, Level, BlockPos, BlockState, boolean)` and `isRandomlyTicking(BlockState)` | RT-24 | `scheduleTick` / `onPlace` / `randomTick` have **0 hits** across the repository |
| ② | whether repeating `scheduleTick` for the same `(BlockPos, Block)` overwrites or coexists (`SavedTick`'s equality measure) | RT-24 | as above |
| ③ | whether a scheduled tick is written into `block_ticks` with the chunk and keeps advancing after a server restart | RT-24 | as above; D-111 gives the semantics only, with no measurement record |
| ④ | how to subscribe to `RegisterCommandsEvent`, and how `Commands.literal` / `IntegerArgumentType.integer` / `requires` are registered | RT-25 | `RegisterCommandsEvent` / `CommandSourceStack` / `LiteralArgumentBuilder` have **0 hits** across the repository |
| ⑤ | the 1.21.1 factory signature and save form of `SavedData` / `DimensionDataStorage#computeIfAbsent` | RT-26 | `SavedData` / `DimensionDataStorage` / `getDataStorage` have **0 hits** across the repository |
| ⑥ | the interception point for dirt → farmland: the form of `HoeItem` in 1.21.1 (a static mapping table or data-driven), and whether `PlayerInteractEvent.RightClickBlock`'s cancel semantics suffice for "produces no farmland, consumes no durability" | RT-28 | `HoeItem` / `Tillable` / `FarmlandBlock` have **0 hits** across the repository |
| ⑦ | the interception point for farmland being trampled: whether NeoForge 21.1.213 provides a cancellable farmland-trample event (the `FarmlandTrampleEvent` family) | RT-28 | `FarmlandBlock` / `turnToDirt` have **0 hits** across the repository |
| ⑧ | `ClientboundLevelParticlesPacket`'s constructor arguments, and how to "send only to a named player" (`ServerPlayer#connection.send`) | RT-30 | this repository has a precedent only for `ClientboundSoundEntityPacket` (`combat/WildHunterAura.java:211`), none for the particle packet |

---

## 1. Baseline Check (measured existing registry objects + the verify_project.py gap)

### 1.1 Counting Method

**Do not count with grep.** `grep "register"` would count factory method bodies, comments and `DeferredRegister` field declarations together. This document uses three evidence chains that are independent of one another:

1. **Read every construction site item by item**: open the 11 `registry/*.java` and count only the calls that "actually produce one registry entry" — the `REGISTER.register(...)` / `registerItem(...)` / `registerBlock(...)` / `registerComponentType(...)` series, plus **every call site of the factory methods**. A factory method is not itself an entry. The factory methods this document uses, with their call counts, are listed one by one:

| Factory method | Definition site | Call count | Entries produced |
|---|---|---|---|
| `FarmstormBlocks.crop(CropProfile)` | `registry/FarmstormBlocks.java:82-91` | **15** (`:25-38` 14 lines + `:42` 1 line) | 15 crop blocks |
| `FarmstormBlocks.deployed(...)` | `:93-111` | **8** (`:45`, `:46`, `:47-48`, `:49-50`, `:51-52`, `:54-55`, `:61-62`, `:64-65`) | 8 deployed blocks |
| direct `REGISTER.registerBlock(...)` | `:68`, `:72` | **2** | `converter`, `compressed_hay_block` |
| `FarmstormItems.cropItem(...)` | `registry/FarmstormItems.java:136-147` | **22** (`:36`–`:95`, counted line by line) | 22 crop items |
| `FarmstormItems.seedItem(...)` | `:149-154` | **7** (`:89`, `:100`–`:107`) | 7 seed items |
| direct `REGISTER.registerItem(...)` | `:110`, `:113` | **2** | the two block items |
| `FarmstormEntityTypes.projectile(...)` | `registry/FarmstormEntityTypes.java:70-78` | **12** (`:31`–`:58`, counted line by line) | 12 projectile types |
| direct `REGISTER.register(...)` | `:60-66` | **1** | `turret_bullet` |
| `FarmstormBlockEntities.REGISTER.register(...)` | `registry/FarmstormBlockEntities.java:22-42` | **6** | 6 BlockEntityTypes |
| `FarmstormDamageTypes.key(String)` | `registry/FarmstormDamageTypes.java:53-55` | **3** | 3 damage-type `ResourceKey`s |

  `projectile(...)` is called **12** times ⇒ 12 entries, not 13 — this one sentence is the whole point of this counting method, and it is where 0.0.2 and 0.0.3 both crashed.
2. **Cross-check against the startup self-check**: `Farmstorm#logRegistrySelfCheck()` (`Farmstorm.java:123-147`) counts the 12 classes separately on every startup — it counts `DeferredRegister#getEntries().size()` rather than a hard-coded constant — and asserts `total == 90` and that each class equals a concrete set of numbers (`:138-141`). That is the second independent ledger, a **runtime** one.
3. **Cross-check against the existing document and the static verifier**: the +3 itemisation in `22-DECISIONS-0.1.0.md` §3.2, and the hard-coded per-category subtotals at `tools/verify_project.py:150-163` (this document's measurement equals them item by item, see §1.4).

All three pieces of evidence agree ⇒ the baseline holds. If any one disagrees ⇒ record it in §1.4.

### 1.2 Per-Category Measured Values (v0.0.4 baseline, 90 in total)

This table has 12 rows. Column 3 says "what was counted"; column 4 is this document's measurement; column 5 is the asserted value of `Farmstorm#logRegistrySelfCheck`. **The two columns are equal item by item**.

| # | registry | What was counted (the item-by-item grounds) | Measured | Startup self-check |
|---|---|---|---|---|
| 1 | `Registries.ITEM` | 22 `cropItem(...)` + 7 `seedItem(...)` + 2 `registerItem(...)` | **31** | 31 |
| 2 | `Registries.BLOCK` | 15 `crop(...)` (`FarmstormCropBlock`) + 8 `deployed(...)` (`DeployedCropBlock`) + `converter` + `compressed_hay_block` | **25** | 25 |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 5 deployed types (`blast_carrot` covers 2 blocks / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` covers 3 blocks) + `converter` | **6** | 6 |
| 4 | `Registries.ENTITY_TYPE` | 12 × `projectile(id, factory)` + `turret_bullet` | **13** | 13 |
| 5 | `Registries.MOB_EFFECT` | `wild_hunter` / `zangief_piledriver` / `q_bounce` / `confusion` | **4** | 4 |
| 6 | `Registries.SOUND_EVENT` | `wild_hunter_loop` / `zangief_piledriver` / `friend_chives` | **3** | 3 |
| 7 | `Registries.RECIPE_TYPE` | `converting` | **1** | 1 |
| 8 | `Registries.RECIPE_SERIALIZER` | `converting` | **1** | 1 |
| 9 | `Registries.CREATIVE_MODE_TAB` | `main` | **1** | 1 |
| 10 | `Registries.DAMAGE_TYPE` | data-driven; only `ResourceKey`s on the code side: `void_bolt` / `crop_blast` / `zangief_slam` | **3** | 3 |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `temporary_enchantments` | **1** | 1 |
| 12 | `Registries.MENU` | `converter` | **1** | 1 |
| | **Total** | | **90** | 90 |

Per-category subtotals (for verification; they must equal the table above row for row):

```
items                 31
blocks                25
block_entities         6
entity_types          13
mob_effects            4
sound_events           3
recipe types           1
recipe serializers     1
creative tabs          1
damage_types           3
data_components        1
menu types             1
--------------------------------
相加 = 31+25+6+13+4+3+1+1+1+3+1+1 = 90
```

**Verification conclusion**: the per-category measurements add up to **90**, `Farmstorm#logRegistrySelfCheck` asserts **90**, and the 12 per-category subtotals at `tools/verify_project.py:150-163` also add up to **90**. Three independent algorithms check one another ⇒ **90 is this document's baseline**, the construction fact card's 90 stands, and there is nothing to report upwards.

### 1.3 Cross-check: 22 / 19 / 15 / 4 and the registry totals

| Number being checked | What the documents claim | This document's measurement | Conclusion |
|---|---|---|---|
| the number of `CropProfile` entries | 22 (22 §1.1) | `ALL` (`util/CropProfile.java:131-136`) has exactly 22 entries, and there are exactly 22 `public static final CropProfile` constants | agrees |
| the lucky berry random pool | 19 (D-55 / D-97) | `LUCKY_BERRY_POOL` = `ALL` with `LUCKY_BERRY` / `WILD_APPLE` / `COMPRESSED_PUFFED_WHEAT` filtered out = 19 | agrees (this version does not change the derivation, still 19) |
| the distribution of crop types | Throwable 12 / Deployable 7 / Edible 3 | `Farmstorm.java:104`'s assertion equals it value by value; Demolition 4 | agrees |
| the number of crop blocks | D-110 says "15 `FarmstormCropBlock` registrations" | `crop(...)` is called 15 times; 22 − 15 = 7 profiles have no planted form | agrees |
| the profiles carrying the TRAP category | the four traps of 22 §2.5 | `BLAST_CARROT` / `FANG_TRAP_CACTUS` / `ZANGIEF_CANE` / `VIBRATION_BEAN`, **4** in total, corresponding to 4 `DeployedKind`s | agrees (§5.4's allowance of 16 counts only deployed objects of these 4 kinds) |
| the correspondence of resources to the registry surface | — | blockstates 25 = blocks 25; `models/item` 31 = items 31; `loot_table/blocks` 25 = blocks 25 | agrees (the Outpost Core must be added to all of them, see §9.2) |
| the config key count | 155 (construction fact card §0) | 155 `define*` calls; 155 `farmstorm.configuration.*` language keys | agrees |
| the language key count | 299 × 2 | 299 each in Chinese and English, identical key sets: `configuration` 155 + `advancements` 36 + `item` 31 + `block` 25 + `tooltip` 16 + `entity` 13 + `gui` 8 + `death.attack` 6 + `effect` 4 + `subtitles` 3 + `container` 1 + `itemGroup` 1 | agrees |
| advancements | 18 | 18 JSON files under `src/generated/resources/data/farmstorm/advancement`; this version +0 | agrees |

### 1.4 `tools/verify_project.py`'s actual run output and the three gaps

**The conclusion first**: when work on this version began the tool was **green** (exit code 0, FAIL 0, WARN 0), not red. The task book's premise that "it is red right now" does not match the measurement, and 22 §4.2 row 5's wording "leave it and it stays red forever" does not hold either — both are registered as measured, and this document does not reason backwards from the premise.

The command run and its output (`python -X utf8 tools/verify_project.py`, working directory `D:\project_farm`); the block below is the tool's actual output and is kept in Chinese character for character:

```
== 1. config：文档键集 vs 生成的 toml ==
  文档口径键数: 155（01-DESIGN §3.8 117 + 12-TECH §8.1 22 + 18-TECH §8.1 19 行；其中 3 行是改既有默认值、
  键名已在上两份清单里，按并集去重后 155 = 139 + 16）
  生成 toml 键数: 155
  [OK]   155 个配置键与文档逐字一致（toml 155 键）
== 2. 90 个注册 ID 逐字核对（全源码树） ==
  [OK]   items: 31 个 ID 齐全
  [OK]   blocks: 25 个 ID 齐全
  [OK]   block_entity_type: 6 个 ID 齐全
  [OK]   entity_type: 13 个 ID 齐全
  [OK]   mob_effect: 4 个 ID 齐全
  [OK]   sound_event: 3 个 ID 齐全
  [OK]   recipe_type: 1 个 ID 齐全
  [OK]   recipe_serializer: 1 个 ID 齐全
  [OK]   creative_mode_tab: 1 个 ID 齐全
  [OK]   damage_type: 3 个 ID 齐全
  [OK]   data_component_type: 1 个 ID 齐全
  [OK]   menu_type: 1 个 ID 齐全
  已核 registry 对象: 90 / 90
== 3. 语言文件键集一致 ==
  [OK]   lang 键集一致，共 299 键
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch

结论：FAIL 0 项，WARN 0 项
```

The exit code is **0**. All three gaps have to do with "after v0.1.0 has landed"; each is given with its location, consequence and fix:

| # | Gap | Location (source) | Current consequence | Consequence after v0.1.0 lands | Fix (written into §11 #1's order of actions) |
|---|---|---|---|---|---|
| G1 | `outpost_core` is in none of the three `EXPECTED` lists | `verify_project.py:120-146` | the tool **cannot see** these 3 new objects; after adding them it stays 90 / 90, all green | **worse than red**: after the registry surface goes +3 the tool is still green, which amounts to these 3 objects never being reviewed by anyone | append `"outpost_core"` to each of `EXPECTED["items"]` / `["blocks"]` / `["block_entity_type"]` |
| G2 | `EXPECTED_COUNTS`'s 12 per-category subtotals are hard-coded on the 90 baseline | `verify_project.py:150-163` | equal item by item to §1.2's measurement, currently self-consistent | adding G1 without changing this ⇒ red at once (`清单 26 个 ID，但 §2.1 的 v0.0.3 列是 25 个`). **This is what 22 §4.2 row 5 means**: not "leave it and it stays red forever", but "add the list without changing the counts and it goes red" | `items` 31→**32**, `blocks` 25→**26**, `block_entity_type` 6→**7**; the `90` in the note under the table becomes **93** |
| G3 | the config expectation set reads only the three frozen documents | `verify_project.py:174-189` | 155 agrees character for character with the source's 155 | once the new toml becomes 166, the 11 extra keys do exist in the source ⇒ they hit `real_extra` ⇒ the tool prints `toml 多 11 个键且源码里存在（文档漏记 MISMATCH）` and goes red | add a fourth source: parse the 12 rows of `docs/zh/24-TECH-0.1.0.md` §8.1, take the 11 key names in the "new" column and union them in; the expected key count then goes 155 → **166**. **Documents 01 / 12 / 18 must not be edited back** (those three files are in the frozen zone) |

One thing that is **not a gap** but must be booked: the language-file section (`verify_project.py:239-249`) only compares whether the Chinese and English key sets are equal and hard-codes no count, so the +16 keys neither turn it red nor get caught by it — **the number 315 rests entirely on §9.4 and §11 #1's re-check**.

**Which side may be wrong**: 22 §4.2 row 5 states as fact that "the tool is red right now", while the measurement is green; the error is in 22's description of the tool's current state, not in its remedial action ("recompute for +3" must still be done). This document rewrites that row's causal claim in line with the measurement, without changing its action.

### 1.5 Consistency with the construction fact card

`BRIEF §0`'s 90, the 13 assertions at `Farmstorm.java:138-141`, the 12 per-category subtotals at `verify_project.py:150-163` and 22 §3.2's "+3 ⇒ 93" all agree, and **there is nothing to report upwards**. The two places BRIEF §3.2 does not list but 22 §4.3 has registered as supplements (the widening of `TrapBehavior`'s signature, and `.randomTicks()` at `FarmstormBlocks.java:88`) are each given their full treatment in §3.5 / §4.4 of this document.

---

## 2. Master Table of Incremental Registry Objects

### 2.1 The Per-Registry Verification Table

The counting measure is word for word the same as document 18's §2.1: **registry object counts**; the JSONs of `recipe` / `loot_table` / `advancement` / `tag` and the blockstate files are data files and do not enter the registry count (§2.3 lists them separately). This table has 12 rows.

| # | registry | 0.0.4 | Increment | v0.1.0 | Increment detail |
|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | 31 | **+1** | **32** | `outpost_core` (a `BlockItem`) |
| 2 | `Registries.BLOCK` | 25 | **+1** | **26** | `outpost_core` (`OutpostCoreBlock`) |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | 6 | **+1** | **7** | `outpost_core` (`OutpostCoreBlockEntity`, a **new type**, see note 1) |
| 4 | `Registries.ENTITY_TYPE` | 13 | +0 | **13** | this version adds no projectile (the same direction as "0 custom packets") |
| 5 | `Registries.MOB_EFFECT` | 4 | +0 | **4** | immunity is not an effect; Confusion already exists |
| 6 | `Registries.SOUND_EVENT` | 3 | +0 | **3** | this version reuses vanilla sounds throughout (a trap's dust is particles, with no sound) |
| 7 | `Registries.RECIPE_TYPE` | 1 | +0 | **1** | the Outpost Core uses vanilla `minecraft:crafting_shaped` and does not enter this mod's `converting` |
| 8 | `Registries.RECIPE_SERIALIZER` | 1 | +0 | **1** | as above |
| 9 | `Registries.CREATIVE_MODE_TAB` | 1 | +0 | **1** | `outpost_core` is appended to the existing `farmstorm:main` |
| 10 | `Registries.DAMAGE_TYPE` | 3 | +0 | **3** | mobs triggering traps reuse the existing `crop_blast` (Blast Carrot) and `zangief_slam` (Zangief Cane) |
| 11 | `Registries.DATA_COMPONENT_TYPE` | 1 | +0 | **1** | — |
| 12 | `Registries.MENU` | 1 | +0 | **1** | the Outpost Core has no screen |
| | **Total** | **90** | **+3** | **93** | |

### 2.2 The 3 New Objects, Named One by One

**3 rows**. Each gives the complete registry id, the carrying class and the grounds for the writing.

| # | registry | Complete id | Carrying class / form | Grounds |
|---|---|---|---|---|
| 1 | blocks | `farmstorm:outpost_core` | `OutpostCoreBlock extends BaseEntityBlock`; registered in the same shape as `converter`: `FarmstormBlocks.REGISTER.registerBlock("outpost_core", OutpostCoreBlock::new, properties)` (precedent `registry/FarmstormBlocks.java:68-69`) | D-119, D-122 |
| 2 | block_entities | `farmstorm:outpost_core` | `OutpostCoreBlockEntity extends BlockEntity`; `REGISTER.register("outpost_core", () -> BlockEntityType.Builder.of(OutpostCoreBlockEntity::new, FarmstormBlocks.OUTPOST_CORE.get()).build(null))` (precedent: the single-block case at `:40-42`) | D-119 item 4 (the placer's ownership), D-120 item 4 (conversion to dirt after destruction), D-121 (tick emits particles), D-122 item 1 (2 per person) |
| 3 | items | `farmstorm:outpost_core` | `BlockItem`; `REGISTER.registerItem("outpost_core", props -> new BlockItem(FarmstormBlocks.OUTPOST_CORE.get(), props), otherProperties())` (precedent `:113-114`) | D-119 item 5 (drops itself), D-122 item 3 (craftable ⇒ there must be an item) |

**Note 1 (why row 3 must create a new type and cannot be hung on the existing 6)**: `DeployedCropBlockEntity`'s constructor looks `DeployedKind` up from `kindOf(state)`, and a block that is not a `DeployedCropBlock` is thrown at directly with `IllegalStateException` (`blockentity/DeployedCropBlockEntity.java:112-118`); the Outpost Core is neither a deployed block nor one of the 8 `DeployedKind`s, so stuffing it into an existing type would crash at block-entity construction time. `ConverterBlockEntity`'s type accepts only `converter` (`registry/FarmstormBlockEntities.java:40-42`), and its BE class has no ownership field. So **+1 type is the inevitable consequence of this +1 block**, not a choice that can be economised.

**Note 2 (no new `DeployedKind`, no new `CropProfile`)**: `DeployedKind` stays at 8 (`behavior/DeployedKind.java:10-20`); `CropProfile.ALL` stays at 22 (`util/CropProfile.java:131-136`). The Outpost Core is not a crop and not a deployed object, and takes no part in `deployedOf(DeployedKind)`'s switch (`registry/FarmstormBlockEntities.java:47-58`).

**Note 3 (`deployedOf` needs no change)**: none of the 8 `DeployedKind`s is added, and the compiler errors out when a switch is missing a branch, so zero change here is the correct outcome.

### 2.3 Increments That Are Not Registry Objects (they do not enter the 93)

| Increment | Count | Nature | Location |
|---|---|---|---|
| Crafting recipe | **+1** | data file | `data/farmstorm/recipe/outpost_core.json` (3×3: `minecraft:stone` in the four corners, `minecraft:diamond` in the centre, `minecraft:redstone` in the remaining four cells, producing 1, D-122 item 3) |
| Loot table | **+1** | data file | `data/farmstorm/loot_table/blocks/outpost_core.json` (drops itself, D-119 item 5) |
| Blockstate / models | **+3** | resource files | `blockstates/outpost_core.json`, `models/block/outpost_core.json`, `models/item/outpost_core.json` (§9.2) |
| Block tag addition | **+1 entry** | data file | `minecraft:mineable/pickaxe` gets `farmstorm:outpost_core` added; it does **not** enter `needs_stone_tool` / `needs_iron_tool` / `needs_diamond_tool` (D-119 item 2) |
| Command tree | **+1 tree** | runtime registration (Brigadier), **not a registry object** | the `/farmstorm` root + `/farmstorm farmlandlimit <int>` (§7) |
| Language keys | **+16** | data files | §9.4 |
| Advancements | **+0** | — | still 18 (22 §3.2's footnote, §6 item 14) |
| The other 9 categories | **+0** | — | `entity_type` / `mob_effect` / `sound_event` / `damage_type` / `recipe_type` / `recipe_serializer` / `creative_tab` / `data_component_type` / `menu_type` |

### 2.4 Verification of the Sum

| Algorithm | Arithmetic | Result |
|---|---|---|
| baseline + increment | 90 + 3 | **93** |
| per-category addition | 32 + 26 + 7 + 13 + 4 + 3 + 1 + 1 + 1 + 3 + 1 + 1 | **93** |
| itemised addition | items 1 + blocks 1 + block_entities 1 | **3** |

The three arithmetics check one another, and **§2's sum equals §1's measured 90 + 3 = 93**. A further 6 data-file entries and 1 command tree do not enter this sum and are listed separately in §2.3.

---

## 3. Design of the New and Changed Classes

Each class gets four items: **responsibilities / key methods / which config keys it reads / which existing files it changes**. Every config key name can be looked up in §8.1's 12 rows.

### 3.0 Overview

| Category | File | New / changed |
|---|---|---|
| 1 block | `block/OutpostCoreBlock.java` | new |
| 1 block entity | `blockentity/OutpostCoreBlockEntity.java` | new |
| 1 command | `command/FarmstormCommands.java` | new |
| 2 farmland classes | `farmland/FarmlandLedger.java` (the quota and the tilled coordinate set), `farmland/FarmlandGuard.java` (tilling interception + 3×3 + trample protection + conversion to dirt) | new |
| 1 Outpost Core index | `outpost/OutpostCoreIndex.java` (player → core coordinates, server-side `SavedData`) | new |
| 2 trap classes | `trap/TrapFifoIndex.java` (player → trap coordinates, `SavedData`), `trap/TrapImmunity.java` (the in-memory 5-second immunity table) | new |
| The trap interface | `behavior/TrapBehavior.java` (signature widened + immunity gate + dust) | changed |
| Growth | `block/FarmstormCropBlock.java` (`onPlace` / `tick` / `isRandomlyTicking`) | changed |
| 9 existing files | `registry/FarmstormBlocks.java`, `registry/FarmstormBlockEntities.java`, `registry/FarmstormItems.java`, `Farmstorm.java`, `config/FarmstormConfig.java`, `blockentity/DeployedCropBlockEntity.java`, `util/CropProfile.java`, `entity/thrown/MelonGrenadeProjectile.java`, `entity/thrown/PuffedWheatProjectile.java` | changed |
| The four trap implementations | `behavior/impl/{BlastCarrotTrap,FangTrapCactusTrap,ZangiefCaneTrap,VibrationBeanTrap}.java` | changed |
| Wiring | `event/FarmstormGameEvents.java`, the 6 datagen Providers, `tools/verify_project.py`, `tools/gen_textures.py` | changed |

**8 new files** (1 block + 1 block entity + 1 command + 2 farmland + 1 core index + 2 trap). **18 changed files** (plus one more file verified and confirmed unchanged, listed in §3.7 as a trace), itemised in §3.7.

### 3.1 `OutpostCoreBlock` (new, `block/`)

| Item | Content |
|---|---|
| Responsibilities | the block side of the Outpost Core: registration properties (hardness 30 / blast resistance 1200 / mineable by any pickaxe / not pushable), writing the placer's ownership, the 2-per-player placement cap, and handing the block entity's ownership to the release flow on destruction. The gameplay effects (+16 quota, range check, boundary particles) live in the block entity and the index |
| Key methods | `newBlockEntity(pos, state)` → `new OutpostCoreBlockEntity(pos, state)`; `getTicker(level, state, type)` returns the ticker on the server (precedent `block/DeployedCropBlock.java:146-158`, including the `level.isClientSide` early return); `getStateForPlacement(ctx)` first checks `OutpostCoreIndex.countOwned(player) >= outpost_core.maxPerPlayer` ⇒ returns `null` (placement does not take place, the item is not consumed, nothing drops, D-122 item 2); `setPlacedBy(level, pos, state, placer, stack)` writes `ownerUuid` and registers the index (precedent `:292-297`); `getDestroyProgress` follows the vanilla formula (**no** ally instant-break, D-119 supplement); `onRemove(...)` does one thing only — hands the block entity's ownership to `OutpostCoreBlockEntity#release()` |
| Registration properties | `BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(30.0F, 1200.0F).requiresCorrectToolForDrops().sound(SoundType.STONE).pushReaction(PushReaction.BLOCK)`. `requiresCorrectToolForDrops()` requires only that "the tool is right", and the concrete tier is decided by `needs_*_tool`; this version **adds no** tier tag ⇒ a wooden pickaxe is already the right tool (D-119 item 2), and the tag enters only `mineable/pickaxe`; not being pushable is done with a registration property rather than a piston-event listener (precedent `registry/FarmstormBlocks.java:101-102`, whose comment states exactly this reason) |
| Which config keys it reads | `outpost_core.maxPerPlayer` (2); `outpost_core.farmlandBonus` (16) is read on the index side and is not read by this class |
| Which existing files it changes | `registry/FarmstormBlocks.java` (+1 registration line); `registry/FarmstormBlockEntities.java` (+1 type); `registry/FarmstormItems.java` (+1 `BlockItem`); `datagen/ModBlockStateProvider.java`, `ModItemModelProvider.java`, `ModLootTableProvider.java`, `ModRecipeProvider.java`, `ModBlockTagsProvider.java`, `ModLangProvider.java` (+1 item each); `Farmstorm.java` (the self-check assertion) |

### 3.2 `OutpostCoreBlockEntity` (new, `blockentity/`)

| Item | Content |
|---|---|
| Responsibilities | three fields and one tick chain: `ownerUuid` / `ownerName` / `placedGameTime` (**no** team snapshot, per C-9's measure); every `boundaryParticleIntervalTicks` ticks it sends one round of boundary particles to players who are "allies and inside the visible range"; on `setRemoved` it releases the allowance and turns the ally farmland in range to dirt |
| Key methods | `saveAdditional` / `loadAdditional` read and write the three fields, **reusing `util/DeployedOwnership`'s** `write(tag, uuid, name)` / `readUuid` / `readName` (`util/DeployedOwnership.java:24-45`, the NBT key names `ownerUuid` / `ownerName` unprefixed, the same shape as the deployed blocks); `owner()` obtains the online entity with `level.getServer().getPlayerList().getPlayer(uuid)` (precedent `blockentity/DeployedCropBlockEntity.java:293-299`); `isAlly(Player)` goes through `DeployedOwnership.isAlly(server, ownerUuid, ownerName, player)` (the same shape at `:285-290`); `serverTick(...)`, a static ticker: when `floorMod(now, outpost_core.boundaryParticleIntervalTicks) == 0` it runs one round, and several cores in one chunk are staggered with `floorMod(pos.hashCode(), interval)` (precedent `:155-161`); in `setRemoved()`, `OutpostCoreIndex.remove(...)` + `FarmlandGuard.turnOwnedToDirt(server, this)` |
| Which config keys it reads | `outpost_core.boundaryParticleIntervalTicks` (40), `outpost_core.farmlandBonus` (16), `outpost_core.maxPerPlayer` (2, for the index's count) |
| Which existing files it changes | `block/OutpostCoreBlock.java` (created in the same batch); `blockentity/DeployedCropBlockEntity.java` is **not changed** (the two are parallel BEs and share no class); `event/FarmstormGameEvents.java` is **not changed** (the core's tick goes through the block's own ticker and does not enter the global tick) |

**Why not extend `DeployedCropBlockEntity` with one more kind**: that would force `DeployedKind` +1 and `CropProfile` +1 profile, and would immediately break this version's two boundaries, "registry surface +3" and "profiles still 22" (§2.1 note 2).

### 3.3 The FIFO index `trap/TrapFifoIndex.java` (new)

| Item | Content |
|---|---|
| Responsibilities | "player → the coordinates of that player's TRAP deployed objects", ascending by `placedGameTime`; it answers three questions: how many traps this player has now, which is the earliest, and whether a given coordinate is still on the books. Persisted to the server-side `SavedData` |
| Key methods | `static TrapFifoIndex get(MinecraftServer)`: `server.overworld().getDataStorage().computeIfAbsent(...)` [to be verified ⑤, RT-26]; `void record(UUID owner, BlockPos pos, long placedGameTime)`; `int count(UUID owner)` (**counts TRAP only**, the criterion being `CropProfile.byKind(kind).has(CropCategory.TRAP)`); `Optional<BlockPos> oldest(UUID owner)`; `void remove(UUID owner, BlockPos pos)`; `List<BlockPos> owned(UUID owner)`; after loading, **lazy validation** — the block at the position must still be a TRAP deployed object under that player, otherwise the entry is discarded (D-115 supplement) |
| Which config keys it reads | `trap.maxTrapsPerPlayer` (16) |
| Which existing files it changes | `blockentity/DeployedCropBlockEntity.java` (`record` in `setOwner`, `remove` in `setRemoved`); `block/DeployedCropBlock.java` (squeeze out first, then allow, in `getStateForPlacement`, see §5.4) |

### 3.4 The farmland quota holder `farmland/FarmlandLedger.java` and the interceptor `farmland/FarmlandGuard.java` (new)

| Item | Content |
|---|---|
| Responsibilities (Ledger) | the **coordinate set** of tilled farmland plus a count cache (D-117 item 4): `Map<UUID, LinkedHashSet<BlockPos>>`, persisted to `SavedData`; `capacity(player)` computes the quota (§6.1); `used(player/team)` computes the tilled count; when a piece of farmland is dug out or blown up it is deleted from the set **on the spot** |
| Responsibilities (Guard) | three interception surfaces: tilling (range → quota), trample protection, and conversion to dirt after the core is destroyed. The order of checks is fixed as "range → quota", and either refusal means **no durability consumed, no farmland produced** (D-116 item 4, D-118 item 3) |
| Key methods (Ledger) | `static FarmlandLedger get(MinecraftServer)` (the same `SavedData` shape as §3.3) [to be verified ⑤, RT-26]; `int capacity(ServerLevel, Player)`; `int used(ServerLevel, Player)`; `boolean add(ServerLevel, Player, BlockPos)`; `boolean remove(BlockPos)`; `List<BlockPos> ownedBy(ServerLevel, ServerPlayer)` (used by D-120 item 4's conversion to dirt) |
| Key methods (Guard) | `static boolean canTill(ServerLevel, Player, BlockPos)` = `inOwnRange(...) && used < capacity`; `static boolean inOwnRange(ServerLevel, Player, BlockPos)` = the Chebyshev distance between the target's chunk and the chunk of "any core under this player or his teammates" ≤ 1; `static boolean protectFromTrample(ServerLevel, BlockPos)`; `static void turnOwnedToDirt(ServerLevel, OutpostCoreBlockEntity)` |
| Which config keys it reads | `farmland.limitPerPlayer` (0, the base quota), `outpost_core.farmlandBonus` (16), `outpost_core.maxPerPlayer` (2, used only for the placement cap) |
| Which existing files it changes | `command/FarmstormCommands.java` (writes the base quota); `block/OutpostCoreBlock.java`, `blockentity/OutpostCoreBlockEntity.java` (ownership and release); `event/FarmstormGameEvents.java` (+1 line subscribing to the trample event, see §6.4) |

### 3.5 The signature change of `behavior/TrapBehavior.java` (changed) — the concrete form

**The change is a widening of types, not a new branch.** Today (`behavior/TrapBehavior.java:5`, `:18-20`):

```java
import net.minecraft.world.entity.player.Player;
...
Optional<Player> findTrigger(DeployedCropBlockEntity be);

void trigger(DeployedCropBlockEntity be, Player target);
```

After the change (D-114):

```java
import net.minecraft.world.entity.LivingEntity;
import java.util.Optional;
...
Optional<LivingEntity> findTrigger(DeployedCropBlockEntity be);

void trigger(DeployedCropBlockEntity be, LivingEntity target);
```

Five knock-on changes, each with its landing point:

| # | Landing point | What changes |
|---|---|---|
| 1 | `behavior/TrapBehavior.java:22-25` | the default `tick` gains the immunity gate and dust (one implementation for §5.1 / §5.2) |
| 2 | `BlastCarrotTrap` (`:38`, `:58`) | both method signatures become `LivingEntity`; the method bodies need no change (the explosion damage already traverses `LivingEntity` to settle, `explosion/FarmstormExplosion.java:159`) |
| 3 | `FangTrapCactusTrap` (`:33`, `:53`) | signatures become `LivingEntity`; the existing `target instanceof ServerPlayer serverPlayer` (`:66`) inside `trigger` stays as it is — the advancement is granted to players only |
| 4 | `ZangiefCaneTrap` (`:48`, `:61`, `:108`) | signatures become `LivingEntity`; `nearestCandidate`'s return type `Player` → `LivingEntity` (`:108`); the existing `!(target instanceof ServerPlayer player)` (`:62`) inside `trigger` becomes two branches: `ServerPlayer` takes the phase chain, `Monster` takes the mob branch (§5.3) |
| 5 | `VibrationBeanTrap` (`:43`, `:66`, `:88`) | signatures become `LivingEntity`; **`:88`'s `target.getGameProfile().getName()` must be replaced** — `LivingEntity` has no such method, so use `target.getName().getString()` instead (otherwise it will not even compile; this is the place most easily missed in this item) |

### 3.6 The growth rework of `block/FarmstormCropBlock.java` (changed)

| Item | Content |
|---|---|
| Responsibilities | the single implementation shared by the 15 crop blocks (`block/FarmstormCropBlock.java:29`) moves from "advanced by vanilla random ticks" to "advanced by a scheduled-tick chain"; everything else (`AGE_3`, `MAX_CUSTOM_AGE = 3`, the four `VoxelShape`s, the `getBaseSeedId` fallback) is untouched |
| Key methods | add `onPlace(state, level, pos, oldState, movedByPiston)` (schedule the first segment when first formed), `tick(state, serverLevel, pos, random)` (brightness gate → advance one level → schedule the next segment), and `isRandomlyTicking(state)` returning `false`; add the private `scheduleNext(ServerLevel, BlockPos, BlockState)` and `segmentTicks(BlockPos)` (§4.2's arithmetic). **Do not override** `randomTick`; **do not override** `performBonemeal` (bone meal goes through the vanilla `CropBlock`'s `setBlock` to raise the age, and the rescheduling is handled by the one `scheduleNext` at the end of `tick`, §4.3 step 4) |
| Which config keys it reads | `growth.minSeconds` (20), `growth.maxSeconds` (45) |
| Which existing files it changes | `registry/FarmstormBlocks.java:88` (delete `.randomTicks()`); `config/FarmstormConfig.java` (+2 keys); `Farmstorm.java` is **not changed** (growth does not enter the registry self-check) |

### 3.7 The Complete List of the Change Surface (19 items = 18 changed + 1 verified unchanged)

| # | File | What changes | Grounds |
|---|---|---|---|
| 1 | `registry/FarmstormBlocks.java` | +1 `outpost_core` registration line; `:88` deletes `.randomTicks()` | D-119, D-111 |
| 2 | `registry/FarmstormBlockEntities.java` | +1 `outpost_core` type | D-119 |
| 3 | `registry/FarmstormItems.java` | +1 `BlockItem` for `outpost_core` | D-119 item 5 |
| 4 | `Farmstorm.java` | `logSelfCheck`'s `demolition == 4` → **2** (`:104`); `logRegistrySelfCheck`'s `total == 90` → **93**, `items == 31` → **32**, `blocks == 25` → **26**, `blockEntities == 6` → **7** (`:138-141`) | D-108, §2.4 |
| 5 | `config/FarmstormConfig.java` | +11 keys, 1 default changed (`horizontalJitter` 0.3 → 0.05) | §8.1 |
| 6 | `util/CropProfile.java` | the `categorySet`s of `MELON_SLICE_BOMB` (`:58-61`) and `MELON_GRENADE` (`:62-63`) lose `DEMOLITION` | D-108 |
| 7 | `entity/thrown/MelonGrenadeProjectile.java` | delete `SUBMUNITION_SPEED` (`:26`), `:66` reads `melon_grenade.submunitionSpeed` instead; the comment at `:25` is deleted | D-109 |
| 8 | `entity/thrown/PuffedWheatProjectile.java` | `fillHay` (`:99-113`) layers the allow-list on top of the resistance baseline | D-106 |
| 9 | `entity/thrown/CompressedPuffedWheatProjectile.java` | **not changed** (it overrides `fillBlock()` only and inherits the check, `:35-37`) | D-106's second sentence |
| 10 | `behavior/TrapBehavior.java` | signature widened + immunity gate + dust | §3.5, §5.1, §5.2 |
| 11-14 | the four traps under `behavior/impl/` | the scans widen to `Monster`; Zangief Cane gains a mob branch | §5.3 |
| 15 | `block/FarmstormCropBlock.java` | the scheduled-tick chain | §4 |
| 16 | `blockentity/DeployedCropBlockEntity.java` | the trap allowance's "squeeze out first, then allow" | §5.4 |
| 17 | `event/FarmstormGameEvents.java` | in `register(...)`, +1 line subscribing to `RegisterCommandsEvent` and +1 line of trample-protection subscription | §7.1, §6.4 |
| 18 | `tools/verify_project.py` | §1.4's G1 / G2 / G3 | §1.4 |
| 19 | `tools/gen_textures.py` | the dedicated branch for the 4 Friend Chives images, the yellow-browning of Compressed Puffed Wheat and its two top / bottom images | §9.3 |

The table above has **16 rows covering 19 items** (rows 11–14 merge the four trap implementations, counting as 4 files), of which row 9 is "not changed (with the reason stated)" — it stays in the table because it had to be verified item by item; what actually changes is **18**. The 6 datagen Providers (`ModBlockStateProvider` / `ModItemModelProvider` / `ModBlockTagsProvider` / `ModLootTableProvider` / `ModRecipeProvider` / `ModLangProvider`) each gain +1 item, counted inside rows 1–3's wiring and not counted twice.

---

## 4. Deterministic Advance of Growth Time (technical plan)

### 4.1 The Random-Tick Road Is Unreachable Parameter-Wise

D-110's quantification is settled: the current fastest is about **614 seconds** (10 min 14 s), fully dry farmland about 24 minutes, dense planting on dry ground about 44 minutes; the target upper bound of **45 seconds** is an order of magnitude faster than even the current **fastest** value. The vanilla criterion is "advance one level only when `nextInt(25/f + 1) == 0` on each random tick, and `getRawBrightness(pos,0) >= 9`", and the random-tick rate is 3 times per chunk section per tick / 4096 blocks; tuning `randomTickSpeed` enough for a 45-second maturity would make every vanilla crop on the whole server shoot up (22 §6 item 2 forbids it outright). So this version switches to a **scheduled-tick chain**, affecting `FarmstormCropBlock` only.

### 4.2 Converting Three Equal Segments

D-110 item 3 requires "draw the total tick count T at random first, then take `d1 = d2 = floor(T/3)`, `d3 = T − 2×d1`". Without a block entity (D-111 excludes block entities) this requirement must land in a form that can be **recomputed statelessly**, otherwise the third segment cannot obtain `d`. The method is to make T a multiple of 3, and every quantity in the table below is **computed live from `growth.minSeconds` / `growth.maxSeconds`** — 134 and 167 are the results under the default configuration, not hard-coded constants (hard-coding them would demote those two config keys to decoration: changing them would change no behaviour at all):

| Quantity | Value | Arithmetic |
|---|---|---|
| Lower bound in ticks, `min` | **400** ticks (default) | `growth.minSeconds × 20` = 20 × 20 |
| Upper bound in ticks, `max` | **900** ticks (default) | `growth.maxSeconds × 20` = 45 × 20 |
| Per-segment lower bound, `dMin` | **134** (default) | `dMin = ceil(min ÷ 3)` = ceil(400 ÷ 3) = 134 |
| Per-segment upper bound, `dMax` | **300** (default) | `dMax = max ÷ 3` (integer division) = 900 ÷ 3 = 300; **constraint ①**: if the computed `dMax < dMin`, clamp it with `dMax = dMin` (so `span` is always ≥ 1) |
| The segment-length pool, `span` | **167** (default) | `span = dMax − dMin + 1` = 300 − 134 + 1 |
| Ticks in one segment, `d` | `dMin + floorMod(pos.hashCode(), span)` ∈ [dMin, dMax] (default [134, 300]) | this document's supplement: stateless recomputation, never persisted |
| Total duration, `T` | `3 × d` ∈ [3·dMin, 3·dMax] (default [402, 900] ticks = **20.1 – 45.0 seconds**) | 3 × 134 = 402, 3 × 300 = 900 |
| Three segments | `d` ticks each, the sum always equal to T | `d1 = d2 = d3 = floor(T/3)`, remainder `r = T − 3d = 0` |

Two reasons, written out one by one:

1. **Why T must be a multiple of 3**: if `r ∈ {1,2}` were allowed, the third segment would be `d + r`, and "what r is" is known only at the moment the first segment is scheduled — with block entities excluded there is nowhere to carry it to the third segment; D-111 has already excluded block entities. Taking `r = 0` makes the three segments always equal in length, and `d3 = T − 2d1 = d` still holds exactly (D-110 item 3's arithmetic is unchanged); the cost is that T takes only the `span` values {3·dMin, 3·dMin+3, …, 3·dMax} (167 by default), which fill [min, max] without a visible gap. **"T is a multiple of 3" is this document's supplement**: it implements D-110 item 3 rather than rewriting it.
2. **Why `d` is decided by the coordinate rather than drawn from `level.random`**: each of the three segments is scheduled separately and each needs `d`; drawing from `level.random` would require storing the first result, which comes straight back to "needs a carrier that can store something". `BlockPos#hashCode()` is pure arithmetic (`(y + z*31)*31 + x`), stable across saves and restarts, and this repository already uses the same form for staggering (`blockentity/DeployedCropBlockEntity.java:155-161`'s `floorMod(pos.hashCode(), interval)`). Re-planting the same plot yields the same `d`: that is reproducible determinism, the same direction as "deterministic advance". **"d is decided by the coordinate" is this document's supplement**.

### 4.3 The Complete Plan of the Scheduled-Tick Chain

The life cycle of one scheduling (six steps, whose order cannot be changed):

1. **There is only one scheduling entry**: the private method `scheduleNext(ServerLevel level, BlockPos pos, BlockState state)`. It first checks `state.getBlock() != this` and `state.getValue(AGE_3) >= MAX_CUSTOM_AGE`, and **returns outright** if either holds, scheduling no tick at all; otherwise `level.scheduleTick(pos, this, segmentTicks(pos))` — `segmentTicks` recomputes `dMin` / `dMax` / `span` every time, and **constraint ②**: `d` is computed at the moment of scheduling and written into the scheduled tick as its delay, so changing the config affects only segments scheduled afterwards and crops already planted are unaffected (raising `maxSeconds` does not make them mature early) [to be verified ①, RT-24].
2. **The first formation is in `onPlace`**: inside `onPlace(state, level, pos, oldState, movedByPiston)`, `scheduleNext` is called only when `oldState.getBlock() != this` (the block came into being). It returns outright when `level` is not a `ServerLevel` (the client schedules nothing, HC-03).
3. **The advance is in `tick`**: `tick(state, serverLevel, pos, random)`'s first line is `if (state.getBlock() != this) return;` (the block has been dug out and the scheduled tick lapses naturally, D-111 item 4), and its second line returns if `age >= MAX_CUSTOM_AGE`; then the brightness gate is passed (`serverLevel.getRawBrightness(pos, 0) >= 9`, D-110 item 4's "the brightness condition is untouched"); if it passes, `serverLevel.setBlock(pos, state.setValue(AGE_3, age + 1), Block.UPDATE_ALL)` (precedent: `Block.UPDATE_ALL` at `entity/thrown/PuffedWheatProjectile.java:109`), and after the advance `scheduleNext` is called once (the `state` has then changed and the age is +1, so `scheduleNext` reads the new age).
4. **Bone meal needs no dedicated code**: the vanilla `CropBlock`'s `performBonemeal` raises the age and calls `setBlock`, which triggers `onPlace`; because `onPlace` schedules only when `oldState.getBlock() != this`, the "same block, new age" case bone meal produces is **not** scheduled by `onPlace`, but by the one `scheduleNext` at the end of step 3's `tick` — that is, "advance early, then reschedule for the new age" (D-111 item 4). This keeps the scheduling entry single, and no second chain appears.
5. **When brightness is insufficient, do not advance and retry 1 second later**: when the brightness gate fails in `tick`, `level.scheduleTick(pos, this, 20)`, with no log line and no age change. 20 ticks is this document's supplement: it keeps the existing semantics "no growth in the dark" exactly as it was while not breaking the scheduled chain; the promise "20 ~ 45 seconds" presupposes light ≥ 9 (the same precondition as vanilla), and acceptance measures it in a field with light 15 per §11 #3.
6. **Saved with the chunk**: the scheduled tick is carried by the chunk's own pending queue (`block_ticks`), so it keeps advancing after a chunk unload/reload and a server restart [to be verified ③, RT-24]. D-111 item 2 already requires implementation to measure "save → restart → keeps advancing"; if the measurement does not bear it out, fall back to the "reschedule from the block state at load time" patch (call `scheduleNext` once for crops with age < 3 in `ChunkEvent.Load`), without changing D-111's measure.

### 4.4 The Three-Part Kit Against Two-Source Advance

If random ticks and scheduled ticks advance at the same time, the 20 ~ 45 seconds promise does not hold (D-111 item 3). All three must be done together; leave one out and there is a hole:

| # | Action | Landing point | Why this one cannot be skipped |
|---|---|---|---|
| 1 | **delete `.randomTicks()`** | `registry/FarmstormBlocks.java:88` (`crop(...)` factory's Properties) | this is the mod's only Properties declaration related to growth; leaving it in is opening the door to vanilla random ticks |
| 2 | **do not override `randomTick`** | `block/FarmstormCropBlock.java` (keep it as it is, add no method) | 1.21.1's `CropBlock.randomTick` has its own "advance only when `nextInt(25/f+1)==0`"; overriding it would take over random ticks and collide with the scheduled tick |
| 3 | **override `isRandomlyTicking(state)` to return `false`** | a new method in `block/FarmstormCropBlock.java` | item 1 deletes the switch on Properties, but whether vanilla `CropBlock` itself overrides `isRandomlyTicking` depends on 1.21.1's decompiled result [to be verified ①, RT-24]; one explicit `return false` on our side is a **locally controllable** criterion that does not depend on that conclusion |

Item 3 is one step this version takes beyond D-111 (D-111 requires only deleting `.randomTicks()` and not overriding `randomTick`), and the reason is in the right-hand column: it turns "has the two-source advance been eliminated" from "depends on vanilla implementation details" into "there is one line returning false in our own code".

### 4.5 Relation to the Vanilla Criteria (not one is changed)

| Vanilla criterion | This version's treatment |
|---|---|
| the brightness threshold `getRawBrightness(pos, 0) >= 9` | kept (§4.3 step 5) |
| bone meal behaviour | kept (§4.3 step 4, going through vanilla `performBonemeal`) |
| `AGE_3`'s four stages / `MAX_CUSTOM_AGE = 3` | kept (`block/FarmstormCropBlock.java:31`, `:63-70`) |
| the four `VoxelShape`s | kept (`:38-43`) |
| vanilla crops (wheat / carrot / potato / beetroot / melon stem / pumpkin stem) | **not one is changed** (22 §6 item 2) |
| the global `randomTickSpeed` | **not changed** (as above) |

### 4.6 The Signature Change of `behavior/TrapBehavior.java:18-24`

See §3.5 — it gives the before / after code and the five knock-on changes and is not repeated here. In one sentence: **the type widens from `Player` to `LivingEntity`**, and the only place among the four implementations that will not compile is `VibrationBeanTrap:88`'s `getGameProfile()`, which becomes `getName().getString()`.

---

## 5. The Four Trap-System Reworks (technical plan)

### 5.1 5-Second Broad-Category Immunity After a Trigger (D-112) — where it is recorded, where the gate is

| Item | Plan |
|---|---|
| Recording container | a new class `trap/TrapImmunity.java`: `static Map<UUID, Long> UNTIL` (the value being the `gameTime` at which immunity expires), with a `MinecraftServer` instance guard — a different server instance empties the whole table, in the same shape as `ledgerOf` at `blockentity/DeployedCropBlockEntity.java:237-244` |
| Where the gate is | `TrapBehavior#tick`'s **default implementation** (`behavior/TrapBehavior.java:22-25`). The four traps share it; although `ZangiefCaneTrap` overrides `tick` (`:37-40`), its first statement is `TrapBehavior.super.tick(be)`, so the gate applies automatically — this is the evidence for "written once, effective in four places" |
| Criterion | `static boolean passGate(ServerLevel level, LivingEntity target)`: for anything that is not a `Player` it returns `true` outright (mobs are not recorded for immunity, D-112 item 3); for a `Player`, `now < UNTIL.getOrDefault(uuid, 0L)` ⇒ returns `false` (gated), otherwise it **writes first** `now + trap.immunityTicks` and then returns `true` |
| Call order | `findTrigger` → `passGate` → `trigger`. When gated, there is **no `consume`, no effect** (D-112 item 2), and when `findTrigger` returns empty no immunity is written either (no trigger, so no immunity) |
| Clock | `level.getGameTime()`, not written to the save (D-112 item 4); the worst consequence of clearing on reconnect is "the same person can step on one again within 5 seconds" |
| Several in the same tick | because `passGate` writes before `trigger`, a second trap reaching the same player in the same tick is necessarily gated ⇒ "triggers once per tick" holds automatically, with no extra deduplication key |
| config | `trap.immunityTicks` (100) |

### 5.2 dust Particles Every 5 Seconds After Deployment (D-113) — who emits, how the phases align

| Item | Plan |
|---|---|
| Who emits | in `TrapBehavior#tick`'s default implementation, a `default void emitDust(DeployedCropBlockEntity be)` is called **before** the immunity gate. The reason: dust is the hint "there is something here", independent of whether anyone steps on it, so it cannot hang off `trigger` |
| For whom only | only the 4 kinds implementing `TrapBehavior` (`BLAST_CARROT` / `FANG_TRAP_CACTUS` / `ZANGIEF_CANE` / `VIBRATION_BEAN`). Turrets go through `TurretBehavior` and auras through `AuraProvider`, which are **two other, parallel interfaces**, so they are naturally never emitted to (D-113 item 4) |
| Emission condition | `Math.floorMod(now + Math.floorMod(pos.hashCode(), dustInterval), dustInterval) == 0`, with `now = level.getGameTime()` |
| Why the phase cannot be missed | `dustInterval = 100` is an integer multiple of `scanInterval = 10`, and the two phases have the same source (`phase100 = hash mod 100`, `stagger10 = hash mod 10`, hence `phase100 mod 10 = stagger10`). So at the moment "(`now + phase100) % 100 == 0`" holds, `serverTick`'s 10-tick door "(`now + stagger10) % 10 == 0`" **necessarily holds at the same time** (the door at `blockentity/DeployedCropBlockEntity.java:149-152`) — dust is never skipped because of staggering, and no second scheduler is needed |
| Particle form | 2 per emission; type `DustParticleOptions(new Vector3f(1.0F, 0.15F, 0.15F), 1.0F)`, character for character the same value as `combat/WildHunterAura.java:47`'s `SMOKE_RED` (the repository's only dust colour value; no new RGB is invented); position `Vec3.atCenterOf(pos)`, offset `(0.2, 0.1, 0.2)`, velocity `0.0`; call `level.sendParticles(...)` (precedent: the 8-argument form at `:170-172`) |
| Why "2 per emission" and the offsets | 22's D-113 supplement fixes 2 as the smallest observable value that is "visible at a glance yet insufficient to mark the exact position"; the offsets (0.2, 0.1, 0.2) are **this document's supplement**, so that the two do not coincide while staying within 0.3 blocks of the block centre |
| Packet volume | 1 `sendParticles` = 1 vanilla particle packet / trap / 5 seconds. 10 traps = 2 packets/second, no burden |
| 0 custom packets | goes over vanilla `ClientboundLevelParticlesPacket` (C-6 / D-50; 22 §6 item 4) |

### 5.3 Hostile Mobs Can Trigger (D-114) — how the mob pass is added

**The two-pass measure follows the implementation**, with no separate criterion invented:

| Step | What is done | Landing point |
|---|---|---|
| 1 | the player pass is kept exactly as it is (the three exemptions for creative / spectator / ally are untouched character for character) | the existing `for (ServerPlayer player : level.players())` in the four `findTrigger`s |
| 2 | **a second pass is added**: `level.getEntitiesOfClass(Monster.class, box)`, with `box` an `AABB` built from the same radius as the player pass. Precedent `behavior/impl/PumpkinTurret.java:109` (the Pumpkin Turret's targeting locks players and `Monster` at the same time) | +1 block each in Blast Carrot / Fang Trap Cactus / Zangief Cane / Vibration Bean |
| 3 | the distance criterion reuses **the same** metric and the same config key (Chebyshev ≤ 1 / Euclidean ≤ 2.5 / spherical ≤ 1.5 / strictly less than 4.0), with no second number written | as above |
| 4 | the ally judgment applies to players only: a `Monster` has no team affiliation and is **always treated as non-ally** (D-114 item 4); `be.isAlly(...)` accepts only a `Player`, so the mob pass does not call it | as above |
| 5 | the effect split: **generic** (damage / slowness / knockback) applies to every triggerer; **player-only** (Confusion) is skipped for mobs | Confusion already traverses `level.players()` only on the effect side (`behavior/impl/VibrationBeanTrap.java:75-83`), **not changed**; slowness already filters `ServerPlayer` on the damage-event side (`behavior/impl/FangTrapFangs.java:57-77`), **not changed**; Blast Carrot's explosion damage already traverses `LivingEntity` (`explosion/FarmstormExplosion.java:159`), **not changed** |
| 6 | Zangief Cane gains a **mob branch** | `behavior/impl/ZangiefCaneTrap.java:61-81`: `target instanceof ServerPlayer` takes the existing `ZangiefPiledriverController.begin(...)` phase chain; otherwise only the generic part settles — `target.hurt(FarmstormDamageTypes.zangiefSlam(level, deployer), (float) zangief_cane.slamDamage)` (default 7.5) plus one `target.knockback(0.4, -dx, -dz)` (strength 0.4 taken from this repository's `combat/WildHunterCombat.java:27`'s `KNOCKBACK_STRENGTH`, no new number invented), **without entering the phase chain** |

**One terminology convention (an author's correction; this document and 22 are unified to this measure)**: Zangief Cane's phase 1 is a **servo lift straight up to a fixed height** (gravity off + per tick "remaining height ÷ remaining ticks", capped by `MAX_RISE_SPEED = 4.0`; `riseHeightBlocks` defaults to 16 with a measured ≈ 15 blocks, `riseTicks` defaults to 15 ticks), phase 2 is a servo descent + yaw +72° self-rotation per tick + a landing settlement of a fixed 7.5 points. The effect class `ZangiefPiledriverEffect` is only the phase channel (`amplifier` as the phase number); it adds no attribute, deals no damage and produces no particles; `MobEffects.LEVITATION` at `behavior/ZangiefPiledriverController.java:390`, `:421` is **only ever removed** (an old-save fallback), not a mechanism. The phase constant being named `PHASE_LEVITATING` is a historical misnomer. **This section therefore does not say "levitate", and does not conflate phase 1 with phase 2.**

### 5.4 16 Traps per Player and the FIFO Squeeze-Out (D-115) — how the squeeze works

| Step | What is done | Why it has to be this way |
|---|---|---|
| 1 | the allowance counts **TRAP** only: `CropProfile.byKind(kind).has(CropCategory.TRAP)` | turrets (`PUMPKIN_TURRET` / `AWAKENED_*` / `POINT_DEFENSE_FUNGUS`) and Construction take up no allowance (D-115 item 2); among the 22 profiles only 4 carry `TRAP` (§1.3) |
| 2 | the squeeze happens **before the new block is written**: inside `block/DeployedCropBlock.java:163-184`'s `getStateForPlacement`, `TrapFifoIndex.enforce(server, player, kind)` is called after `canDeploy(...)` and before `return defaultBlockState()` | 22's D-115 supplement: if the squeeze came after the write, the 17th would take its place first and then squeeze out the one just placed. `getStateForPlacement` is the only place where "the check precedes the item deduction" (the comment at `:178-181` already states this property) |
| 3 | `enforce`'s semantics: while `count(owner) >= trap.maxTrapsPerPlayer`, take `oldest(owner)` and call `DeployedCropBlockEntity#consume()` on that coordinate (`removeBlock(pos,false)` + break particles, **no drops**, the item is not refunded, D-115 item 3) until `count < 16`; at most 1 squeeze per call | reuse the existing `consume` path (`blockentity/DeployedCropBlockEntity.java:322-330`) rather than writing a second removal |
| 4 | a new entry is `record(owner, pos, placedGameTime)` in `setOwner`, and `remove(owner, pos)` in `setRemoved` | these correspond one to one with the existing throttling ledger's addition / subtraction points (`:267-279`, `:333-346`), so both books go in and out together |
| 5 | the three safety valves **5 / 8 / 32 are untouched** | they are the server's protection baseline (HC-13); 16 is a gameplay allowance (D-115 item 5) |
| 6 | the on-disk format: `UUID → a list of positions ascending by placedGameTime`; lazy validation at load time (the position must still hold a TRAP deployed object under that player) | D-115's supplement gives two reasons: the order consistent with first deployment can be rebuilt after a restart; a bad entry lapses by itself, with no reconciliation job needed |

### 5.5 Master Table of the Four Items' Mount Points

| Item | Mount point | Where it is recorded | Persisted | config key | Acceptance |
|---|---|---|---|---|---|
| 5-second broad-category immunity | `TrapBehavior#tick` default implementation (the immunity gate) | `trap/TrapImmunity.UNTIL` (`Map<UUID, Long>`, in memory + a server-instance guard) | not persisted | `trap.immunityTicks` 100 | §11 #6 |
| dust every 5 seconds | `TrapBehavior#emitDust` (default implementation, called first in `tick`) | none (the phase is decided by `pos.hashCode()`) | not persisted | `trap.dustIntervalTicks` 100 | §11 #7 |
| mobs can trigger | the second pass `getEntitiesOfClass(Monster.class, box)` in the four `findTrigger`s; Zangief Cane's two branches in `trigger` | none (a one-shot settlement) | not persisted | no new key (the four existing radius keys are reused) | §11 #8 |
| FIFO 16 | `DeployedCropBlock#getStateForPlacement` (the squeeze) + `setOwner` / `setRemoved` (the accounting) | `trap/TrapFifoIndex` (`SavedData`) | **persisted** | `trap.maxTrapsPerPlayer` 16 | §11 #9 |

---

## 6. Farmland Quota and the Outpost Core (technical plan)

### 6.1 The Quota Model

```
base            = 运行期覆盖值（默认取 farmland.limitPerPlayer = 0）
capacity(玩家)  = base + 16 × （该玩家名下已放置的核心数）
capacity(队伍)  = Σ over 队伍成员 m of [ base + 16 × （m 名下已放置的核心数） ]
used(玩家/队伍) = 账本里该口径下的坐标集合大小（同一坐标只算一次）
```

(In English: base = the runtime override (defaulting to `farmland.limitPerPlayer` = 0); capacity(player) = base + 16 × the number of cores placed under that player; capacity(team) = Σ over team members m of [base + 16 × the number of cores placed under m]; used(player/team) = the size of the coordinate set under that measure in the ledger (the same coordinate counts once).)

| Situation | Arithmetic | Quota | Note |
|---|---|---|---|
| Single player, no team, no core, config default | 0 + 16×0 | **0** | at the start nobody can till (D-116 item 2) |
| Single player, 1 core | 0 + 16×1 | **16** | D-120 item 1 |
| A 2-person team, 1 core each | (0+16) + (0+16) | **32** | summed within the team (D-117 item 1), not 16 computed per head |
| After `/farmstorm farmlandlimit 8`, a 2-person team with 1 core each | (8+16) + (8+16) | **48** | the command changes `base` only (§7.6) |
| No team | base + 16 × one's own core count | — | D-117 item 3 |

Three hard measures: **only new tilling is limited, never retroactively** (D-116 item 3); **accounting uses a coordinate set** (the same piece of farmland inside two cores' united range counts once, D-118 item 4 / D-117 item 4); **destroying a core releases +16 immediately** (D-122 supplement).

### 6.2 The 3×3 Check

```
核心区块   cx = corePos.getX() >> 4,  cz = corePos.getZ() >> 4
目标区块   tx = pos.getX()     >> 4,  tz = pos.getZ()     >> 4
在范围内 ⇔ max(|tx − cx|, |tz − cz|) ≤ 1
```

(In English: the core's chunk cx = `corePos.getX() >> 4`, cz = `corePos.getZ() >> 4`; the target's chunk tx = `pos.getX() >> 4`, tz = `pos.getZ() >> 4`; in range ⇔ max(|tx − cx|, |tz − cz|) ≤ 1.)

- 3 × 3 chunks = 9 chunks = 48 × 48 blocks (16 × 3 = 48), sharing the same set of numbers as D-118, D-120 item 2 and D-121's boundary particles.
- **Multiple cores unite**: check once per core under "the player himself + his teammates", and any hit puts the position in range (D-118 supplement).
- The 3 × 3 is **not made a config key** (D-118 supplement, 22 §6 item 9).

### 6.3 Tilling Interception (order fixed: range → quota)

| Step | Check | Effect when refused |
|---|---|---|
| 1 | is the action "dirt / grass block → farmland" | — |
| 2 | is the target position inside an **ally** core's 3×3 (§6.2) | no farmland produced, **no durability consumed**, message `message.farmstorm.farmland.no_core` sent |
| 3 | `used < capacity` (§6.1) | no farmland produced, **no durability consumed**, message `message.farmstorm.farmland.quota_full` sent (with `%s/%s`) |
| 4 | passes ⇒ the vanilla logic is allowed to run, and the coordinate is written into the ledger in the same tick | — |

Choice of interception point: [to be verified ⑥, RT-28]. The primary plan is NeoForge's `PlayerInteractEvent.RightClickBlock` (fired before the item's `useOn`, cancellable), with `setUseBlock(Result.DENY)` at the same time when cancelled; this repository has 0 precedents, so a minimal case is written first to confirm "after cancelling, neither farmland is produced nor durability consumed". For the fallback see RT-28.

The ledger's write measure: after step 4 passes the coordinate is recorded **immediately**, without waiting for the next read; if for some other reason the vanilla logic ultimately produces no farmland, the lazy validation (the position must still be `minecraft:farmland`) discards that entry at the next read.

### 6.4 Trample Protection (where it hangs)

| Item | Content |
|---|---|
| What is protected | **ally** farmland inside a core's 3×3 (D-120 item 3); only "trampling into dirt" is intercepted, **dehydration degradation is not** (D-120 supplement) |
| Mount point | +1 subscribing line in `event/FarmstormGameEvents.register(...)`; the handler decides "is the trampled coordinate inside an ally core's range and is it ally farmland", and cancels if so [to be verified ⑦, RT-28] |
| Why not a block property | the farmland block is vanilla `minecraft:farmland`; this mod has no class for it and cannot replace it (replacing it would mean swapping every piece of farmland in the world for a home-grown block, breaking §2's registry surface); under the no-Mixin red line (D-40) the only viable place is the event layer |
| Fallback plan | if 21.1.213 has no cancellable trample event, degrade to "reconcile every 40 ticks + restore": `OutpostCoreBlockEntity` checks, every 40 ticks (the same round as the boundary particles), the ledger's coordinates inside the range and, finding that the block has become `minecraft:dirt`, `setBlock`s it back to `minecraft:farmland` (`moisture` taken as the default 0). The cost goes into the acceptance record: the trampling instant flashes one frame of dirt |

### 6.5 Destroying a Core → Ally Farmland in Range Turns to Dirt

| Item | Content |
|---|---|
| What is traversed | **only the coordinates under that owner in the ledger**; no whole-range scan (D-120 supplement: a scan would shovel up "farmland inside the range but not on the books" as well, conflicting with item 4's "ally" restriction) |
| Trigger point | `OutpostCoreBlockEntity#setRemoved()` (precedent `blockentity/DeployedCropBlockEntity.java:333-346`: `setRemoved` is a reliable hook for "the block has been removed" and chunk unloading calls it too, hence a one-shot flag guarantees it runs only once) |
| Criterion | for each coordinate on the books: ① it is still inside this core's 3×3; ② the block is still `minecraft:farmland`; ③ the ledger's owner of that coordinate is the core's owner himself or a teammate — only when all three hold does it `server.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL)` and delete it from the ledger |
| Order | work out "which coordinates are to be converted" first, then touch the world, so the ledger is not changed while it is being traversed |
| No residue | after the conversion the core is deleted from `OutpostCoreIndex` and the `+16` lapses on the spot (D-122 supplement) |

### 6.6 Boundary Particles and the Packet-Volume Estimate (D-121)

| Item | Value / plan |
|---|---|
| Geometry | the ground rectangle's **boundary** 48 × 48 blocks (not a filled face). Perimeter = 4 × 48 = 192 blocks; one point every 4 blocks ⇒ **48 points / loop** |
| How packets are sent | one `level.sendParticles(...)` per edge: the centre is that edge's midpoint, `count = 12`, the `dx` (or `dz`) along the edge takes half the edge length and the other horizontal axis and `dy` take 0 ⇒ 12 points land at random on that edge, with an average spacing of 48 ÷ 12 = **4 blocks**. Four edges = **4 packets / loop** (**this document's supplement**: one `sendParticles` emits one packet only, so sending point by point would be 48 packets; sending per edge presses it down to 4 without changing the "one point every 4 blocks" look) |
| Frequency | one round every 40 ticks (2 seconds) (`outpost_core.boundaryParticleIntervalTicks`) |
| Distance falloff | three tiers (**this document's supplement**): player within ≤ 48 blocks of the core centre ⇒ 48 points (4 packets); 48 < d ≤ 64 blocks (RT-30's "margin", taken as one chunk width of 16 blocks) ⇒ 24 points (4 packets, `count = 6` per edge); d > 64 ⇒ not sent |
| Visibility | sent only to "ally players who are also inside the table's ranges" (D-121 items 1 and 4); creative / survival is not distinguished |
| Particle type | `ParticleTypes.END_ROD` — a vanilla constant, weightless, needing no colour argument, so this version produces no RGB constant at all and does not conflict with `23-DESIGN-0.1.0.md`'s colour table |
| Packet volume (one player, inside the range) | 4 packets / 2 seconds = **2 packets/second**, 48 particles / 2 seconds = 24/second |
| Packet volume (10 ally players all inside the range) | **20 packets/second**, 240 particles/second |
| Comparison with "sending per cell" | 48 × 48 = 2304 cells / 2 seconds = 1152 particles/second/player ⇒ after sampling it is **1/48** of that (1152 ÷ 24 = 48) |
| 0 custom packets | if "send only to qualifying players" is needed, use the vanilla `ClientboundLevelParticlesPacket` + `ServerPlayer#connection.send` (the same shape as `combat/WildHunterAura.java:211`'s `ClientboundSoundEntityPacket`) [to be verified ⑧, RT-30]; the fallback is `ServerLevel#sendParticles` (broadcast to everyone within the radius, still a vanilla packet) |

---

## 7. Command Registration and Permission

### 7.1 The Registration Entry

| Item | Plan |
|---|---|
| Event | `RegisterCommandsEvent`, +1 line `gameBus.addListener(FarmstormGameEvents::onRegisterCommands)` inside `event/FarmstormGameEvents.register(IEventBus gameBus)` (`:46-52`) [to be verified ④, RT-25] |
| Handler | `onRegisterCommands(RegisterCommandsEvent event)` → `FarmstormCommands.register(event.getDispatcher())` |
| Carrying class | the new class `command/FarmstormCommands.java` (the `command/` directory does not exist in the repository today; this version creates it) |
| Registry surface | **+0 registry objects**: a Brigadier command tree is registered at runtime and enters no category of §2 (§2.3 lists it separately) |
| Server-side only? | yes. The command is registered on the server only and the client gains no code at all (the same direction as C-6 / HC-03) |

### 7.2 The Command Tree

```
/farmstorm
    farmlandlimit <value>        权限等级 2（与 /gamerule 同档）
```

(In English: `/farmstorm` → `farmlandlimit <value>`, permission level 2 (the same tier as `/gamerule`).)

| Item | Value | Note |
|---|---|---|
| Root literal | `farmstorm` | the same as the mod id, avoiding a name clash with someone else's `/farm` |
| Sub-command | `farmlandlimit` | the literal name D-116 item 1 gives, copied character for character (not `farmland_limit`, not `farmlandLimit`) |
| Argument | `IntegerArgumentType.integer(0, 4096)` | the lower bound 0 matches config `farmland.limitPerPlayer`'s lower bound; the upper bound matches §8.1's range column |
| Permission | `.requires(src -> src.hasPermission(2))` | 2 = the same tier as `/gamerule` (this document's supplement: the quota is a global gameplay rule, and letting players change it themselves means there is no rule) |
| Execution | write `FarmlandLedger.setBase(value)`, then `src.sendSuccess(...)` sends back one piece of feedback carrying the new value | changes base only, **not the tilled count** (D-117's closing clause) |
| Return | success 1 | Brigadier's `Command.SINGLE_SUCCESS` |

### 7.3 The Points of the Feedback Texts

| Key | Trigger | Points |
|---|---|---|
| `command.farmstorm.farmlandlimit.set` | the command executed successfully | must echo back **the new quota** and the fact "in force within this process, returning to the config default on restart"; carries no player name (it is a global value) |
| `message.farmstorm.farmland.no_core` | tilling refused by the range | one sentence making clear "there is no ally Outpost Core nearby", reporting neither coordinates nor distance (so as not to become a probe) |
| `message.farmstorm.farmland.quota_full` | tilling refused by the quota | must carry `%s/%s` (used / cap), so the player can judge for himself whether to demolish a core or dig up an old field |

All three texts use `Component.translatable(...)`, with the language keys going into §9.4; **no sound effect is attached to a refusal text** (vanilla tilling has its own sound, and a second layer would cover it).

### 7.4 This Is Not a Gamerule (stated explicitly and pinned down)

**1.21.1 cannot register a `GameRule` without Mixin.** Three grounds:

1. vanilla `GameRules.register` is `private static` (the decompiled line `GameRules.java:207` in construction fact card §1.8), and NeoForge 21.1.213's source has no entry point for registering a custom gamerule;
2. this mod has kept the "no Mixin" red line since 0.0.1 (D-40), and this version does not introduce bytecode injection for the sake of one rule (22 §6 item 1);
3. the repository's only read of `GameRule` is `explosion/FarmstormExplosion.java:109` reading `RULE_MOBGRIEFING` — that is **reading a vanilla rule**, not registering one of our own, and this version does not touch it.

So the farmland rule has exactly two carriers: **the server-side config `farmland.limitPerPlayer` (default 0)** + **the `/farmstorm farmlandlimit <n>` sub-command**. Neither this document nor any deliverable calls this arrangement a "gamerule" (22 §0's row on "what the farmland rule is called").

### 7.5 Scope and Persistence

| Item | Conclusion |
|---|---|
| The command value's scope | the current server process |
| After a restart | back to the config default (D-116 item 5) |
| Persistence | **no second copy of persistent data is written**. Config is already the only persistent measure, and storing another copy would immediately create a two-source with "the quota defined in two places, and an order between them" |
| `/reload` | `FarmstormConfig.onReloading` will `bake()` derived snapshots; the base quota is **not** reset in `bake()` (the command value takes precedence over config until the next restart) |

### 7.6 The Relation Between the Command and the Quota

The command changes §6.1's `base` only; neither `+16 × core count` nor `used` is affected by it. Hence "turning the quota down" neither makes already tilled farmland disappear nor degrades it (D-116 item 3); it only makes the next tilling refused.

---

## 8. The Complete Config Change Inventory

### 8.1 The Per-Key Inventory (**12 rows**)

The column set is the same as document 18's §8.1: group.key / type / default / range / use / D number. **This table has 12 rows = 11 new rows + 1 default changed**; the row count does not equal the number of new keys (row 10 changes an existing key's default, the key itself having existed since 0.0.4). **The whole "range" column is this document's supplement** — the construction fact card gives only the types and defaults, and the ranges are chosen in line with the order of magnitude of the existing keys in the same group.

| # | group.key | Type | Default | Range | Use | New / changed | D |
|---|---|---|---|---|---|---|---|
| 1 | `growth.minSeconds` | int | **20** | 1 – 600 seconds | lower bound of the total time to maturity | new | D-110 |
| 2 | `growth.maxSeconds` | int | **45** | 1 – 600 seconds | upper bound of the total time to maturity | new | D-110 |
| 3 | `trap.immunityTicks` | int | **100** | 0 – 1200 ticks | ticks of broad-category trap immunity after a trigger (5 seconds) | new | D-112 |
| 4 | `trap.dustIntervalTicks` | int | **100** | 20 – 1200 ticks | the interval of the traps' dust particles (5 seconds) | new | D-113 |
| 5 | `trap.maxTrapsPerPlayer` | int | **16** | 1 – 128 | the per-player trap allowance (FIFO) | new | D-115 |
| 6 | `farmland.limitPerPlayer` | int | **0** | 0 – 4096 blocks | the server-side default of the farmland quota | new | D-116 |
| 7 | `outpost_core.farmlandBonus` | int | **16** | 0 – 1024 blocks | the farmland cap added by each core | new | D-120 |
| 8 | `outpost_core.maxPerPlayer` | int | **2** | 1 – 64 | cores a player may place | new | D-122 |
| 9 | `outpost_core.boundaryParticleIntervalTicks` | int | **40** | 20 – 1200 ticks | the refresh interval of the boundary particles (2 seconds) | new | D-121 |
| 10 | `melon_grenade.horizontalJitter` | double | **0.05** (was 0.3) | 0.0 – 8.0 blocks | Melon Slice Bomb's landing-jitter upper bound | **changed** | D-109 |
| 11 | `melon_grenade.submunitionSpeed` | double | **0.1** | 0.0 – 4.0 blocks/tick | Melon Slice Bomb's radial initial speed (previously the hard-coded 0.3) | new | D-109 |
| 12 | `compressed.replaceOnlyAirAndFluid` | boolean | **true** | `true` / `false` | Puffed Wheat's landed block replaces air and fluids only | new | D-106 |

**Itemised subtotals**: `growth` **2** + `trap` **3** + `farmland` **1** + `outpost_core` **3** + `melon_grenade` **1** (row 11; row 10 changes a default and adds no key) + `compressed` **1** = **11** new; **1** default changed (row 10); the table has **12** rows.

**Row-by-row verification conclusion** (the type / range / default columns have all been checked against construction fact card §3.4 and the source):

- The types and defaults of rows 1–9, 11 and 12 are identical character for character to construction fact card §3.4 (int / int / int / int / int / int / int / int / int / double / boolean; 20 / 45 / 100 / 100 / 16 / 0 / 16 / 2 / 40 / 0.1 / true).
- Row 10's **current value** measures as `defineInRange("horizontalJitter", 0.3, 0.0, 8.0)` (`config/FarmstormConfig.java:389-391`): the default goes from 0.3 to 0.05, and `min` / `max` and the `translation` key are kept character for character.
- Rows 1 and 2 must constrain each other: the code takes `Math.min(minSeconds, maxSeconds)` as the lower bound and `Math.max(...)` as the upper, so filling the two values in reverse still cannot make `T` come out negative or an empty interval (an implementation-stage defence, with no change of measure).
- This version adds **no** key at all for the 3 × 3 range, the 48 × 48 boundary, the boundary granularity or Zangief Cane's mob settlement (22 §6 item 9).

### 8.2 TOML Fragments (5 new groups)

The 5 new groups are appended after `vibration_bean` (the current end of the file), with the keys inside each group in §8.1's row order. The comments below are the generated config file's own text and are kept in Chinese character for character:

```toml
[growth]
	# 成熟总时长下界（秒），范围 1 ~ 600
	minSeconds = 20
	# 成熟总时长上界（秒），范围 1 ~ 600
	maxSeconds = 45

[trap]
	# 触发后陷阱大类免疫的刻数，100 刻 = 5 秒；范围 0 ~ 1200
	immunityTicks = 100
	# 陷阱 dust 粒子的间隔（刻），100 刻 = 5 秒；范围 20 ~ 1200
	dustIntervalTicks = 100
	# 每玩家陷阱名额，第 17 个挤掉最早的一个；范围 1 ~ 128
	maxTrapsPerPlayer = 16

[farmland]
	# 耕地额度的服务端默认值，0 = 开局谁也耕不了地；范围 0 ~ 4096
	limitPerPlayer = 0

[outpost_core]
	# 每个据点核心增加的耕地上限；范围 0 ~ 1024
	farmlandBonus = 16
	# 每玩家可放置的核心数；范围 1 ~ 64
	maxPerPlayer = 2
	# 边界粒子刷新间隔（刻），40 刻 = 2 秒；范围 20 ~ 1200
	boundaryParticleIntervalTicks = 40

[compressed]
	# 落地方块只替换空气与流体；false = 回到 0.0.4 的「只跳过抗性 ≥ 3.6e6」
	replaceOnlyAirAndFluid = true
```

The `melon_grenade` group gets no new group of its own: row 11 appends one new key after the existing 5, and row 10 changes the existing key's default argument (`config/FarmstormConfig.java:382-398`).

### 8.3 The Equality of the Key Count and the Language Key Count

| Item | 0.0.4 | Increment | v0.1.0 |
|---|---|---|---|
| config keys (the `define*` call count) | 155 | +11 | **166** |
| `farmstorm.configuration.*` language keys | 155 | +11 | **166** |
| number of top-level groups (top-level `b.push("...")` calls) | 30 | +5 | **35** |

The three are always equal (the latter two are each determined by the former): every key has one language key generated by `FarmstormConfig.tr(path)` (`"farmstorm.configuration." + path`, `:867-869`), and `ModLangProvider.config(path)` writes one corresponding entry; every new key falls in one of the 5 new groups, hence the group count goes 30 → 35.

The verification arithmetic: `155 + 11 = 166`; `30 + 5 = 35`; the config and configuration-language-key columns both hold at the same time.

### 8.4 Constants Explicitly Kept Out of config (with a reason for each)

| Constant | Value | Reason |
|---|---|---|
| the 3 × 3 core range | 9 chunks / 48 × 48 blocks | the author gave no knob for this, and adding a key would amount to inventing a requirement (D-118 supplement, 22 §6 item 9) |
| the boundary particles' sampling interval | one point every 4 blocks, 48 points per loop | an appearance parameter, the same set of numbers as the 48 × 48; aligned by document 23 rather than tuned by the server owner (D-121 supplement) |
| the boundary particles' visible margin | 16 blocks (one chunk width) | RT-30 says only "falls off with distance", and this document takes one chunk width as the margin; it is a performance parameter, not a gameplay one |
| the safety valves beyond the trap allowance | `DEPLOY_COOLDOWN_TICKS = 5` / `MAX_DEPLOYED_PER_CHUNK = 8` / `MAX_DEPLOYED_PER_PLAYER = 32` | the server's protection baseline (HC-13), which must not be moved into config (the comment at `blockentity/DeployedCropBlockEntity.java:47-50` has it pinned down) |
| the permission level of the farmland-quota command | 2 | a permission is an operations measure, not a gameplay number |
| the brightness retry interval of the scheduled-tick chain | 20 ticks | an implementation detail (the retry cadence for "no growth in the dark"), unrelated to D-113's 5 seconds |
| the pool of growth segment lengths | `span = dMax − dMin + 1` (default 167) | a derived quantity: computed every time from `growth.minSeconds` / `growth.maxSeconds` (§4.2), **not a config key of its own**; hard-coding it would make those two keys pointless |
| `HOTBAR_WIDTH` | 182 | a constant declared but unused; this version does not touch it (D-103 supplement, 22 §6 item 10) |

---

## 9. Inventory of Resource and Data Files

Measure: only the files v0.1.0 **adds or needs replaced in place / appended to** are listed; the 200-odd files 0.0.4 already has are not. The three tables (PNG / JSON / language keys) each give a category subtotal; existing files that get "an appended entry" are listed separately and are not counted as new files.

### 9.1 PNG (**11 rows**: 5 new + 6 replaced in place)

| # | File | Size | Source / treatment | Grounds |
|---|---|---|---|---|
| 1 | `assets/farmstorm/textures/block/outpost_core_top.png` | 32 × 32 | `_top` of `32x32/a_runic` in the new material `outpost_core_all_textures` (2,745 B), committed as it is | D-123 item 1 |
| 2 | `assets/farmstorm/textures/block/outpost_core_side.png` | 32 × 32 | `_side` in the same directory (2,534 B), committed as it is | D-123 item 1 |
| 3 | `assets/farmstorm/textures/block/outpost_core_bottom.png` | 32 × 32 | `_bottom` in the same directory (2,513 B), committed as it is | D-123 item 1 |
| 4 | `assets/farmstorm/textures/block/compressed_hay_block_top.png` | 16 × 16 | new top face: redrawn from the existing `compressed_hay_block.png`'s yellow-brown base with a "bundle mouth", its tone in the same family as the side | D-107 item 3 |
| 5 | `assets/farmstorm/textures/block/compressed_hay_block_bottom.png` | 16 × 16 | new bottom face: the same specification as the top, with the bundle-mouth pattern removed | D-107 item 3 |
| 6 | `assets/farmstorm/textures/block/friend_chives_stage0.png` | 16 × 16 | **replaced in place**: the magenta `#D81E8A` placeholder → a hand-drawn stage 0 | D-104 |
| 7 | `assets/farmstorm/textures/block/friend_chives_stage1.png` | 16 × 16 | **replaced in place**: stage 1 (one cell of leaves above stage 0) | D-104 |
| 8 | `assets/farmstorm/textures/block/friend_chives_stage2.png` | 16 × 16 | **replaced in place**: stage 2 | D-104 |
| 9 | `assets/farmstorm/textures/block/friend_chives_stage3.png` | 16 × 16 | **replaced in place**: stage 3 (the mature plant height) | D-104 |
| 10 | `assets/farmstorm/textures/item/wild_apple.png` | 16 × 16 | **replaced in place**: the desktop material 1,294 × 1,216 RGBA (1,113,442 B) resampled to the 16 grid; the model's `layer0` untouched | D-105 |
| 11 | `assets/farmstorm/textures/block/compressed_hay_block.png` | 16 × 16 | **replaced in place**: a full grey scale (9 colours) → yellow-brown at the same specification | D-107 item 3 |

**Category subtotal**: new **5** (3 Outpost Core + 2 Compressed Puffed Wheat top and bottom) + replaced in place **6** (4 Friend Chives + 1 Wild Apple + 1 Compressed Puffed Wheat side) = **11**.

**Two places where no PNG is added (this must be stated, or someone will go and fill in an image)**: the Outpost Core **needs** no separate item icon — the item model takes the block model as its parent directly (D-123 item 2), so `outpost_core.png` **does not appear** under `textures/item/`; Compressed Puffed Wheat's item is a 3D model, so **no** second item texture is added (D-107 item 1).

### 9.2 JSON (**7 rows**: 5 new + 2 replaced in place)

| # | File (under `src/generated/resources/`) | Nature |
|---|---|---|
| 1 | `assets/farmstorm/blockstates/outpost_core.json` | new: a single variant pointing at `farmstorm:block/outpost_core` |
| 2 | `assets/farmstorm/models/block/outpost_core.json` | new: parent `minecraft:block/cube_bottom_top`, the three `textures` keys `top` / `side` / `bottom` pointing at §9.1's #1–#3 |
| 3 | `assets/farmstorm/models/item/outpost_core.json` | new: parent `farmstorm:block/outpost_core` (a 3D item, no `layer0` written) |
| 4 | `data/farmstorm/recipe/outpost_core.json` | new: vanilla `minecraft:crafting_shaped`, 3 × 3 with `stone` in the four corners / `diamond` in the centre / `redstone` in the remaining four cells, producing 1 |
| 5 | `data/farmstorm/loot_table/blocks/outpost_core.json` | new: drops itself (`LootItem.lootTableItem(block)`; this block **does** have a `BlockItem`, unlike `vibration_bean`, which has none) |
| 6 | `assets/farmstorm/models/block/compressed_hay_block.json` | **replaced in place**: `minecraft:block/cube_all` → `minecraft:block/cube_bottom_top`, with `textures`' `side` pointing at `compressed_hay_block` and `top` / `bottom` at §9.1's #4 / #5 |
| 7 | `assets/farmstorm/models/item/compressed_puffed_wheat.json` | **replaced in place**: `item/generated` + `layer0` → parent `farmstorm:block/compressed_hay_block` (a small 3D block) |

**Category subtotal**: blockstate **1** + block models **2** (#2 new, #6 replaced) + item models **2** (#3 new, #7 replaced) + recipe **1** + loot_table **1** = **7**; of which **5** are new and **2** replaced in place.

**Three appended entries in existing files** (not new files, but the feature is incomplete without them):

| # | File | What is appended |
|---|---|---|
| A1 | `data/minecraft/tags/block/mineable/pickaxe.json` | +1 entry `farmstorm:outpost_core`; it does **not** enter the three `needs_*_tool` (D-119 item 2) |
| A2 | `assets/farmstorm/lang/zh_cn.json` | +16 keys (§9.4) |
| A3 | `assets/farmstorm/lang/en_us.json` | +16 keys (a key set identical character for character to A2) |

**The difference from 22 §3.3 (must be registered)**: 22 records the JSON additions as 4 (blockstate / block model / item model / recipe), **missing row 5's loot table**. D-119 item 5 states explicitly that "mining drops go through the block's own loot table, producing the block itself", and without `loot_table/blocks/outpost_core.json` that cannot land (`ModLootTableProvider` currently builds one per block for 25 blocks, see §1.3). This document records **5 new**, and that cell of 22 needs an incremental correction — it is not a new requirement but a file required to implement D-119.

### 9.3 Changes to the Generator (`tools/gen_textures.py`, 3 places)

| # | Location | What changes | Why the generator must change |
|---|---|---|---|
| 1 | the `for index in range(ADDITION_PLACEHOLDER_STAGES)` branch at `:659-666` | these 4 no longer go through `make_placeholder()` (the solid fill at `:512-514`, colour value `PLACEHOLDER_COLOR = (0xD8,0x1E,0x8A,0xFF)`, `:64`) but through a dedicated stage-drawing branch: fill a key into `Spec.drawer` (the `drawer` field already exists, `:148-149`), add `STAGE_DRAWERS` beside `ITEM_DRAWERS` (`:632-634`), and decide the seedling height and leaf count by `index` | replacing the PNGs without touching the generator means the next re-run overwrites them back to magenta (D-104's only reason) |
| 2 | the `("compressed_hay_block", ...)` entry at `:284-286` | the 4th element (`monochrome`) changes from `True` to `False`, and `tint` is added to make it yellow-brown | this entry is currently `monochrome=True` (going through `convert("L")`), and a re-run would overwrite the yellow-brown back to grey scale |
| 3 | `BLOCK_SPECS` (the same place as the tuple block at `:275-291`) | append `compressed_hay_block_top` / `compressed_hay_block_bottom` (16 × 16, same source as `hay_block_side`, likewise `tint`ed) | the top / bottom images are both generator products, and leaving them out of the generator would disagree with §9.1 #4 / #5 |

**The dedicated branch uses no random numbers** (D-104 supplement): randomness would make every re-run produce different output and void the criterion "re-running equals what is committed". The stage shapes progress through `AGE_3`'s four levels, with the art style referring to `power_beet_stage0..3` (4 colours, about 251/256 transparent) and `ubi_server_stage0..3` (9 colours, 175/256 transparent) in the same directory.

### 9.4 Language Key Increment (**16 keys**)

| Prefix | 0.0.4 | Increment | v0.1.0 | Source |
|---|---|---|---|---|
| `block.farmstorm.*` | 25 | **+1** | 26 | `outpost_core`'s block name |
| `item.farmstorm.*` | 31 | **+1** | 32 | `outpost_core`'s item name |
| `farmstorm.configuration.*` | 155 | **+11** | 166 | §8.1's 11 new keys (row 10 changes a default, the key name unchanged) |
| `command.farmstorm.*` | 0 (a new prefix) | **+1** | 1 | `command.farmstorm.farmlandlimit.set` (§7.3) |
| `message.farmstorm.*` | 0 (a new prefix) | **+2** | 2 | `message.farmstorm.farmland.no_core`, `message.farmstorm.farmland.quota_full` (§7.3) |
| the rest (`advancements` 36 / `tooltip` 16 / `entity` 13 / `gui` 8 / `death.attack` 6 / `effect` 4 / `subtitles` 3 / `container` 1 / `itemGroup` 1) | 88 | **+0** | 88 | this version adds no advancement, no effect, no entity and no screen; `tooltip`'s 6 category rows reuse existing entries |
| **Total** | **299** | **+16** | **315** |

**The verification arithmetic of the two totals**: the 0.0.4 column `25 + 31 + 155 + 0 + 0 + 88 = 299`; the v0.1.0 column `26 + 32 + 166 + 1 + 2 + 88 = 315`; the increment column `1 + 1 + 11 + 1 + 2 + 0 = 16`. All three hold at the same time.

**Why the other `+0` categories do not move**: the 36 advancement keys correspond to 18 advancements (a title + a description each) and this version adds +0 advancements; the 16 `tooltip` keys are organised by category and by "no vanilla counterpart", and the Outpost Core reuses an existing category row rather than adding a category; the registry surfaces behind `entity` / `effect` / `subtitles` / `gui` / `container` / `itemGroup` are all +0 in this version (§2.1).

---

## 10. Risk Register (RT-24 ~ RT-30)

The numbering is fixed at 7 entries, with no additions, deletions or merges. Each has five items: risk / trigger condition / primary mitigation / fallback / how to verify during implementation.

### RT-24 · Growth time pressed from 10~44 minutes to 20~45 seconds: random tick → scheduled-tick chain

| Item | Content |
|---|---|
| Risk | ① this is a **global mechanism replacement**: the 15 crop blocks move from "advanced by vanilla random ticks" to "a scheduled-tick chain", and if `.randomTicks()` is missed or `isRandomlyTicking` is not blocked, random ticks advance alongside the scheduled tick and the 20~45 seconds becomes uncontrollable; ② whether `scheduleTick` overwrites or coexists for the same `(pos, type)` is unknown (to be verified ②), and coexistence would have the same plant scheduled several times over and mature early; ③ if a scheduled tick is not persisted with the chunk (to be verified ③), a crop would stall in place after a chunk unload / restart, or even be stretched by "scheduling d once more"; ④ bone meal goes through vanilla `setBlock` to raise the age, so more than one scheduling entry would already mean a second chain; ⑤ `d` is decided by `pos.hashCode()`, the same plot re-planted gets the same `d`, which looks like "this plot always grows at the same speed" |
| Trigger condition | someone adds `.randomTicks()` back to make a crop somewhere ripen faster; `scheduleTick`'s overload signatures differ from this document's assumption (to be verified ①); a chunk unloads and the player walks 200 blocks away and comes back; `performBonemeal` is overridden by another mod |
| Primary mitigation | the three-part kit (§4.4) is done together: delete `.randomTicks()`, do not override `randomTick`, explicitly `isRandomlyTicking → false`; there is only one scheduling entry, the private `scheduleNext`, and both `onPlace` and `tick` call it; `tick`'s first line checks `state.getBlock() != this` and the second `age >= MAX`, so any invalid state exits outright |
| Fallback | ① if measurement shows "the same `(pos,type)` coexists" and it is observable (the same plant advances twice within a second), switch to "read `age` before scheduling to derive the remaining segment count and use only one `scheduleTick`", with `d`'s recomputation inside the same call; ② if a scheduled tick is not persisted, add a reschedule in `ChunkEvent.Load` for crops with `age < 3` (D-111 has authorised this patch); ③ if the author judges `d`'s being decided by the coordinate "too regular", have `d` take `level.getSeed() ^ pos.hashCode()` as input (still with no block entity and no storage) |
| How to verify during implementation | ① 4 crops at different coordinates under light 15, recording each one's "planted → `AGE 3`" tick count, all falling within 402 – 900 ticks; ② temporarily raise `randomTickSpeed` to 1000 and measure again, with the time unchanged (§11 #4); ③ the same plant's log shows only 3 advances (not 6, not 30); ④ after planting, walk 200 blocks away and come back: the maturity time still falls within 402 – 900 ticks and there is no instant maturity (§11 #5) |

### RT-25 · A custom `GameRule` cannot be registered in 1.21.1 (no Mixin): go through `/farmstorm farmlandlimit`

| Item | Content |
|---|---|
| Risk | ① someone digs into `GameRules` to "give it a try", and introducing Mixin breaks two red lines at once; ② the command tree is registered at runtime, so a mistake (the literal spelled `farmland_limit`, the argument type given as `StringArgumentType`) produces no compile error and only "the command does not exist" in game; ③ a permission level of 0 would let any player change the quota himself, which means there is no rule; ④ the command value is mistakenly taken as a persistent value (it returns to the config default on restart, and players conclude "setting it does nothing"); ⑤ `RegisterCommandsEvent` is subscribed on the wrong bus (the mod bus rather than the game bus), so it **never fires** |
| Trigger condition | nobody can find `/farmstorm` in the server's startup log; after a restart a player runs the command and does not see the old value; someone copies the way `RULE_MOBGRIEFING` is read to write a rule of their own |
| Primary mitigation | the carriers are only config + sub-command (§7.4's three grounds are written into the document), and `GameRules.register` **does not appear** in the implementation; the command tree's literals and argument types follow §7.2 character for character; permission level 2; the feedback text must contain "in force within this process" (§7.3) |
| Fallback | ① if `RegisterCommandsEvent`'s (to be verified ④) registration differs from this document, first hang it on the game bus in the `Farmstorm` constructor and print one line "command tree registered", taking the log rather than a guess as the criterion; ② if the command ultimately cannot be registered, the degraded plan is "the quota is decided by config alone, with operations editing the toml + `/reload`" — that loses runtime adjustment and must be written into the acceptance record |
| How to verify during implementation | ① after the server starts, `/farmstorm` completes to `farmlandlimit`; ② a player with permission 1 is refused (the vanilla permission message); ③ after running `farmlandlimit 8`, a player with no core has tilling refused by the quota, and after a core gives +16 he can till 24 blocks (§11 #11's quota section); ④ after a restart the quota is back to the config default 0 (§11 #10) |

### RT-26 · FIFO and the three server-side indexes: the existing ledger has counts only and is not persisted

| Item | Content |
|---|---|
| Risk | ① the existing "player → deployed objects" ledger has only `Map<UUID, DeployCounter(int, long)>` (`blockentity/DeployedCropBlockEntity.java:65-72`), which cannot tell "which one is earliest"; ② if the index is not persisted, `oldest`'s order degrades to "the last one loaded" after a restart and the FIFO becomes a LIFO; ③ the squeeze in the wrong place (written in `setOwner`) would let the 17th take its place first and then squeeze out the one just placed; ④ writing the allowance measure as "all deployed objects" would count turrets and Construction in; ⑤ `SavedData`'s factory signature in 21.1.213 is unknown (to be verified ⑤), and all three indexes (trap / core / farmland) need it; ⑥ if a bad entry (its coordinate already turned to air by `/setblock`) is not validated, `oldest` keeps pointing at an empty position and the squeeze spins idly |
| Trigger condition | the same player places 17 traps in a row; the server restarts and the 17th is placed again; 16 traps + 8 turrets are placed; a coordinate is cleared by `/setblock` or an explosion; a chunk unloads and is reloaded |
| Primary mitigation | all three persisted indexes are `SavedData` with a unified format "a list of `UUID → positions ascending by placedGameTime`"; after loading, **lazy validation** (the block at the position must still be the same kind of block under that player), so bad entries lapse by themselves; the squeeze acts inside `getStateForPlacement` (the only place where "the check precedes the item deduction", `block/DeployedCropBlock.java:178-181`); the allowance counts only the 4 kinds for which `CropProfile.byKind(kind).has(CropCategory.TRAP)` |
| Fallback | ① if `SavedData`'s API form differs from this document's assumption, degrade to "write the three indexes into one self-managed `CompoundTag` file" — the same format and the same lazy validation, only the carrier changes; ② if the reordering cannot be guaranteed, store `placedGameTime`'s **absolute tick number** in the index and sort by it (not depending on load order); ③ if a "squeezed the wrong object" alarm persists, add one log line printing the squeezed coordinate and its `placedGameTime`, gather evidence from the log and then change the criterion |
| How to verify during implementation | ① the same player places 17 traps: after the 17th is placed the earliest disappears, with **no drops**, and the total is still 16 (§11 #9); ② `stop` the server and `start` it, then place the 17th again: what is squeezed is still the earliest (not the last); ③ 16 traps + 8 turrets mixed, not one turret is squeezed (§11 #17); ④ `/setblock air` one trap's coordinate and place the 17th: the index skips that empty position by itself |

### RT-27 · Three count assertions: `demolition == 4`, `total == 90`, and `verify_project.py`'s hard-coded subtotals

| Item | Content |
|---|---|
| Risk | ① `Farmstorm.java:104` asserts `demolition == 4`, and leaving it unchanged after removing the two profiles' `DEMOLITION` means **FAIL at startup**; ② the same line also asserts `CropProfile.ALL.size() == 22 && throwable == 12 && deployable == 7 && edible == 3` — these four do not change in this version, and changing them by mistake would only create a second error; ③ `:138-141` asserts `total == 90` plus the 12 per-category counts, which FAILS as well after +3 if left unchanged; ④ among `verify_project.py`'s three gaps (G1 / G2 / G3), G1 is "silent failure" — without the list the tool stays green, which amounts to the new objects never being reviewed |
| Trigger condition | anyone runs the server once; anyone runs `verify_project.py` once; anyone changes G1 without changing G2; someone edits `01-DESIGN.md` (the frozen zone) to make things go green |
| Primary mitigation | the three changes are written in one batch (§3.7 rows 4 and 18): `demolition` 4 → 2, `total` 90 → 93 with the three per-category counts +1, and the tool's three fixes; the tool's order of actions is fixed as "add the 3 ids first → then change the 3 counts → then add the fourth config source", and reversing it goes red once first (this is exactly what §1.4 means by "add the list without changing the counts and it goes red") |
| Fallback | ① if the tool's fourth source proves too hard to parse, the expected config key count is allowed to be "counted live from the `define*` call count in `FarmstormConfig.java`" (`grep` counting has been proven wrong in this repository, so this is used only if parsing the document fails, and it must be written into the acceptance record); ② if `demolition`'s assertion is mistakenly changed to another value, the sole criterion is §11 #2's logged behaviour (it must be `破障类 2 → OK`) |
| How to verify during implementation | ① start the server and read `latest.log`: `自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / ... → OK` and `自检 作物 22 种：投掷型 12 / 部署型 7 / 食用型 3；破障类 2 → OK` (§11 #2); ② run `python -X utf8 tools/verify_project.py`: `已核 registry 对象: 93 / 93`, a config section of 166 keys with no FAIL, `lang 键集一致，共 315 键`, `结论：FAIL 0 项`, exit code 0 (§11 #1) |

### RT-28 · Farmland intervention from zero: the tilling interception + accounting + trample protection chains

| Item | Content |
|---|---|
| Risk | ① this mod has **zero intervention** on farmland / tilling (`FARMLAND` / `FarmlandBlock` / `turnToDirt` / `HoeItem` / `Tillable` / `BlockEvent` have 0 hits under `src/`), so all three chains start from nothing; ② the tilling interception point is unknown (to be verified ⑥): whether `HoeItem` in 1.21.1 is a static mapping table or data-driven decides whether "cancelling the interaction" really produces no farmland; ③ the trample interception point is unknown (to be verified ⑦): with no cancellable event there is only after-the-fact reconciliation, which flashes one frame of dirt; ④ if the ledger is in memory only, `used` resets to zero after a restart ⇒ the quota is bypassed by "restarting once"; ⑤ if accounting used a bare count (already rejected by D-117), a player digging out one plot himself would have nowhere to subtract one; ⑥ if destroying a core converts to dirt with a whole-range scan, it would shovel up farmland someone else tilled (D-120's supplement forbids it explicitly) |
| Trigger condition | a player tills where there is no core; a team reaches its quota and keeps tilling; someone jumps on farmland inside a core's range; `/setblock` turns a bookkept piece of farmland to air; a non-ally blows up the core |
| Primary mitigation | the order of checks is fixed as "range → quota", and both refusals **consume no durability** (§6.3); the ledger is a **coordinate set + a count cache** persisted to disk (D-117 item 4), with the subtraction happening on the spot; conversion to dirt traverses only coordinates on the books (§6.5's three conditions holding together); trampling goes through a cancellable event, degrading to a 40-tick reconciliation when unavailable |
| Fallback | ① if the tilling interception cannot achieve "no durability consumed", degrade to "cancel the event + allow farmland to be produced but not bookkept": the quota and range semantics still hold (unbookkept farmland is not counted in `used` and takes no part in conversion to dirt), and the acceptance record states "visually tillable, ruled invalid"; ② if the trample event is unavailable, use §6.4's 40-tick reconciliation (cost: one frame of dirt); ③ if persisting the ledger fails, the degraded plan is to hang the ledger on "write once when the player logs out + read once when he logs in", coarse-grained but not bypassing the quota |
| How to verify during implementation | ① default quota 0, no core: right-click a grass block with a hoe → no farmland produced, durability unchanged, a message appears (§11 #10); ② after placing a core, the same position → farmland produced; one cell outside the 3×3 → not produced (§11 #11); ③ jumping on ally farmland inside the range → still farmland; the same action outside the range → turns to dirt (§11 #12); ④ destroying the core → ally farmland in range all becomes dirt, farmland outside the range / belonging to others untouched (§11 #13); ⑤ till again after a restart: `used` is identical to before the restart (not reset) |

### RT-29 · The Outpost Core's 32 × 32 versus other blocks' 16 × 16, plus the six-face distinction

| Item | Content |
|---|---|
| Risk | ① 32 × 32 textures mixed with the 16 × 16 textures of the same atlas may bleed at the edges if mipmapping is on; ② `cube_bottom_top` has only the three top / side / bottom textures, and mistyping the `textures` key names (`top` / `side` / `bottom`) produces no compile error, only a purple-black checker render; ③ if the item model copies `item/generated`, the inventory shows a flat image instead of a small block; ④ after Compressed Puffed Wheat goes from `cube_all` to `cube_bottom_top`, changing the model without changing the generator (§9.3 item 2) means one re-run puts it back to a grey-scale single texture |
| Trigger condition | someone writes `block/cube_all` instead of `cube_bottom_top` in `outpost_core.json`; someone keeps the `basicItem` path in `ModItemModelProvider`; someone re-runs `gen_textures.py` without comparing the PNGs |
| Primary mitigation | 32 × 32 needs **no** special uv handling: vanilla samples by the 16 × 16 logical grid, and the texture's resolution is only pixel density (BRIEF §2 item 20, D-123's grounds); the model's parent is fixed at `cube_bottom_top` (the material already provides three different faces, and using `cube_all` would waste two of them); the item model is fixed at "take the block model as parent", with no `layer0` |
| Fallback | ① if mipmap bleeding is visible, shrink `outpost_core_*.png`'s edge inward by 1 pixel (without changing size or model); ② if `cube_bottom_top`'s three keys cannot produce the expected look, degrade to `cube_all` + only the `_side` texture (losing the difference between top and bottom; decide after writing that into the acceptance record) |
| How to verify during implementation | ① place the core and take one frame each of the four sides / top / bottom: the three faces differ and none is a purple-black checker; ② in the inventory it is a small block (the top face visibly differs from the side), not a flat image; ③ after Compressed Puffed Wheat lands, its top / side / bottom differ, and re-running `gen_textures.py` leaves the byte sizes of the three PNGs unchanged |

### RT-30 · Boundary particles: the volume of a 48 × 48 block area

| Item | Content |
|---|---|
| Risk | ① sending per cell is hundreds of packets a second (2304 cells / 2 seconds = 1152 particles/second/player); ② "send to allies only" requires per-player packets, and this repository has a precedent only for `ClientboundSoundEntityPacket`, none for the particle packet (to be verified ⑧); ③ if the distance falloff reduces only the point count and not the frequency, a distant player still receives one round of empty packets every second; ④ with several cores' ranges overlapping, one player may receive two rings of particles; ⑤ if the boundary is computed hard as "core centre ± 24 blocks", a core at a chunk edge makes the boundary cross chunks and visibly break at the seam |
| Trigger condition | 10 ally players stand inside a core's range at once; two cores' ranges are adjacent or overlapping; a core sits in a chunk corner; `boundaryParticleIntervalTicks` is tuned to 20 |
| Primary mitigation | sampling is pressed down to "one point every 4 blocks, 48 points per loop" and sent per edge (4 packets / loop); three-tier distance falloff (≤ 48 blocks 48 points / 48–64 blocks 24 points / > 64 blocks not sent); the geometry is computed on the **chunk grid** (the core's chunk 3 × 3 → 48 × 48 blocks), so the boundary is always aligned with chunk lines and never breaks at a seam; when one player is covered by two cores, each round is taken once only (deduplicated by player UUID, the same shape as the existing `AppliedAura`, `blockentity/DeployedCropBlockEntity.java:68-75`) |
| Fallback | ① if per-player sending (to be verified ⑧) is infeasible, degrade to `ServerLevel#sendParticles` (broadcast to everyone within the radius, still a vanilla packet, at the cost of enemies seeing the boundary too); ② if the packet volume is still high, change the interval from 40 ticks to 60 (a config value only, no code change); ③ if the overlap deduplication must be precise to "one ring per player per round", clear a `Set<UUID>` at the start of each round (the same shape as `claimAura`) |
| How to verify during implementation | ① an ally player enters the range: a ring of points appears on the four edges of the ground rectangle every 2 seconds, about 4 blocks apart; after walking 48 blocks out, half density remains; after 64 blocks it disappears (§11 #14); ② a second client (non-ally) stands at the same point: it sees no boundary particles at all; ③ with two adjacent core ranges, there is only one ring at the junction rather than two overlapping (§11 #15's control item); ④ count the particle packets over 10 seconds ≈ 20 per player (4 packets × 5 rounds) |

---

## 11. Acceptance Self-Check Table

Each entry gives one concrete action + one decidable pass criterion. **18 entries** (#1 – #18).

| # | Action | Pass criterion | Coverage |
|---|---|---|---|
| 1 | change `tools/verify_project.py` per §1.4's three fixes (G1 adds the 3 ids, G2 changes the 3 counts, G3 adds the fourth config source), then run `python -X utf8 tools/verify_project.py` | the output shows `已核 registry 对象: 93 / 93`, a config section reading `166 个配置键与文档逐字一致`, `lang 键集一致，共 315 键` and `结论：FAIL 0 项`, with exit code 0 | §1.4, §2.4, §9.4, RT-27 |
| 2 | start the server and read the two self-check lines in `latest.log` | registry objects **93** with the per-category counts `items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1`, the line ending in `OK`; the crop self-check reading `22 种、投掷型 12 / 部署型 7 / 食用型 3；破障类 2`, the line ending in `OK` | D-108, §2.4, RT-27 |
| 3 | in a field with light 15, plant 4 crops at four different coordinates and record each one's "planted → `AGE 3`"; then set `growth.minSeconds` / `growth.maxSeconds` to **10 / 20** and plant 4 more, measuring again | default configuration: all four fall within **402 – 900 ticks** (20.1 – 45.0 seconds) and are not all identical; after the change to 10 / 20: all four fall within **201 – 399 ticks** (10.05 – 19.95 seconds, from `3 × ceil(200/3) = 201` to `3 × (400/3 integer division) = 399`) — proving that the two keys really take part in computing `d` and are not decoration | D-110, §4.2, §8.1 |
| 4 | temporarily raise `randomTickSpeed` to 1000 and repeat #3 | the maturity durations equal #3's per plant (difference ≤ 20 ticks); the same plant's advance log shows only **3** entries | D-111, §4.4, RT-24 |
| 5 | right after planting, walk 200 blocks away (forcing a chunk unload), then come back and wait for maturity | the total duration still falls within 402 – 900 ticks; nothing "matures instantly" the moment you return | D-111 supplement, §4.3 step 6, RT-24 |
| 6 | the same player steps on a Blast Carrot, then on a Fang Trap Cactus within 2 seconds; after waiting 6 seconds, steps on the same cactus once more | the second one (within 2 seconds) **does not trigger**: no explosion, no sound, the cactus block still in place; the third one 6 seconds later triggers normally | D-112, §5.1 |
| 7 | deploy 1 trap (and stay away from it), recording the particles over 10 seconds | dust appears at the block centre every **5 seconds** (2 at a time), exactly 2 times in 10 seconds; turrets and aura blocks produce none at all | D-113, §5.2 |
| 8 | push a zombie into a Blast Carrot's / Fang Trap Cactus's / Vibration Bean's trigger range once each; then push a zombie into a Zangief Cane | all three traps trigger (the carrot explodes, the cactus produces fangs, the bean plays its explosion sound and disappears); what the zombie receives is the generic effects — damage / slowness (if hit) / knockback, **Confusion is not applied** (no `confusion` in `/data get entity`); Zangief Cane: the zombie takes **7.5 points** and is knocked back, and **does not enter the phase chain** (no `farmstorm:zangief_piledriver` effect on the zombie) | D-114, §5.3 |
| 9 | the same player places 17 traps in a row (every 5 ticks, or at different placement points to dodge the cooldown), recording each coordinate | after the 17th is placed, the **earliest** one's coordinate has become air, **with no drop items**; the player's trap total is still 16 | D-115, §5.4, RT-26 |
| 10 | config default (quota 0), no core: right-click a grass block with a hoe | no farmland produced; the hoe's durability **unchanged**; the message "there is no ally Outpost Core nearby" appears; after a restart, the behaviour is the same | D-116 items 2 and 4, §6.3, RT-28 |
| 11 | place 1 core and till one cell each inside the core's chunk and the 8 surrounding chunks; then till one cell in the 9th chunk outside the 3 × 3 | the first batch (the sample points inside the 9 chunks) all produce farmland; that cell in the 9th chunk produces none; the quota the core supplies is **16** (the 17th cell is refused with the used / cap message) | D-118, D-120 item 1, §6.1 / §6.2 |
| 12 | jump on ally farmland inside the core's range once; perform the same action on another piece of ally farmland outside the core's range | the one inside the range is still `minecraft:farmland`; the one outside becomes `minecraft:dirt` | D-120 item 3, §6.4 |
| 13 | note one piece of farmland inside the range, one outside, and one belonging to someone else, then dig out the core | the ally one inside the range becomes dirt; the one outside and the other player's are unchanged; that core's `+16` lapses on the spot (the quota returns to `base`) | D-120 item 4, D-122 supplement, §6.5 |
| 14 | an ally player stands within 48 blocks of the core and looks at the ground boundary; then a non-ally player stands at the same point | ally: a ring of points appears on the four edges of the rectangle every 2 seconds (about 48 points, about 4 blocks apart); non-ally: sees no particles at all | D-121, §6.6, RT-30 |
| 15 | place 1 core in each of two adjacent chunks (the two 3 × 3 ranges partly overlapping), till at the junction of the two ranges and observe the boundary particles | the cell at the junction can be tilled normally (the union holds, no hole appears); only one ring of boundary particles appears there, not two overlapping | D-118 supplement, §6.2, RT-30 |
| 16 | the same player places a 3rd core | placement does not take place: no block appears, the item is **not consumed**, nothing drops; the 2 cores already there are unaffected | D-122 items 1 and 2, §3.1 |
| 17 | 16 traps + 8 turrets mixed (same player), then a 17th trap | what is squeezed out is the **earliest trap**; not one of the 8 turrets is squeezed and their positions are unchanged | D-115 item 2, §5.4, RT-26 |
| 18 | scan the whole repository: `@Mixin`, `CustomPacketPayload`, `GameRules.register` | all three counts are **0**; the only permitted `GameRule` use is the one **read** of `RULE_MOBGRIEFING` at `explosion/FarmstormExplosion.java:109` | D-40, C-6, §7.4, 22 §6 items 1 and 4 |

**The correspondence of the numbering to the requirements**: #1 the tool and the three counts (RT-27), #2 the startup self-check (D-108), #3–#5 growth (D-110 / D-111 / RT-24), #6–#9 the four trap items (D-112 ~ D-115 / RT-26), #10–#13 farmland and the core (D-116 ~ D-120 / RT-28), #14–#15 boundary particles (D-121 / RT-30), #16 the core cap (D-122), #17 the allowance measure (D-115), #18 the red lines (D-40 / C-6).

**Row-count re-check of the four tables**: §2's item-by-item naming **3** rows, §8's per-key inventory **12** rows, §9 (PNG 11 + JSON 7 + language keys 6 rows), §11's self-check **18** rows — each equal to §0.4's counts.
## Translator's notes

1. **Chinese strings that the tools actually print stay in Chinese, character for character**, following the convention already set by `docs/en/18-TECH-0.0.3.md` (its note 6): the whole `tools/verify_project.py` output block in §1.4, the two `latest.log` self-check lines in RT-27 and §11 #2 (`… → OK`, `破障类 2 → OK`), the `[OK] …` and `结论：FAIL 0 项` lines, and the comments inside the generated `toml` in §8.2. They are what the tool, the server and the config file actually show, so translating them would make the acceptance criteria unverifiable.
2. **Registry ids, class and method names, package paths, config keys, language keys, file paths, commands and code fragments are reproduced unchanged**, including `待验证`'s replacements `[to be verified ①…⑧, RT-24…RT-30]`, which keep the ① – ⑧ number and the RT number intact so that §0.5's 8-entry register and its 9 bracketed citations still line up.
3. **Re-mirrored from the A-26 ruling (the derivation is computed from config)**: §4.2's every quantity — `min`, `max`, `dMin = ceil(min ÷ 3)`, `dMax = max ÷ 3` with **constraint ①** (clamp `dMax = dMin` when `dMax < dMin`), `span = dMax − dMin + 1`, `d = dMin + floorMod(pos.hashCode(), span)`, `T = 3d` — is written as **computed live from `growth.minSeconds` / `growth.maxSeconds`**; 400 / 900 / 134 / 300 / 167 / [402, 900] / 20.1–45.0 s are marked "(default)" as the results under the default configuration, never as hard-coded constants. §4.3 step 1 carries **constraint ②** (d is fixed at the moment of scheduling and written into the scheduled tick as its delay, so a config change affects only later segments). §8.4's span row and §11 #3 follow the same measure; §11 #3's 10 / 20 re-measurement uses **201 – 399 ticks (10.05 – 19.95 s)**, i.e. `3 × ceil(200/3) = 201` up to `3 × (400/3 integer division) = 399`.
4. **The JSON count now agrees with 23-DESIGN-0.1.0.md** after the upstream A-27 correction: §9.2's **5 new** (blockstate → block model → item model → loot table → recipe) matches `23` §1.3 / §10.3, so the conflict this translation originally recorded in both files is closed.
5. **Particle specifications that still disagree with 23-DESIGN-0.1.0.md, left as written**: this document's §5.2 gives the trap dust as `DustParticleOptions(new Vector3f(1.0F, 0.15F, 0.15F), 1.0F)` (the red of `SMOKE_RED`) with position `Vec3.atCenterOf(pos)` and offset `(0.2, 0.1, 0.2)`, while 23 §7.4 gives a neutral earth tone `(0.62, 0.56, 0.44)` with position `(x + 0.5, y + 0.15, z + 0.5)` and spread `0.25 / 0.10 / 0.25`; this document's §6.6 uses `ParticleTypes.END_ROD` with a three-tier falloff (48 / 24 / none), while 23 §8.7 uses `minecraft:dust` coloured `(0.30, 0.85, 0.90)` with a two-tier falloff. Each document marks its own version 【本文补充】, and neither cross-references the other on these points.
6. **§6.1's pseudo-code writes the bonus as a literal 16** (`base + 16 × …`) while §8.1 makes it the config key `outpost_core.farmlandBonus` (default 16); 23 §8.1 states the same rule with the config key. Translated as written, with no harmonisation.
7. **Carried-over conventions**: the markup tag 【本文补充】 is left untranslated (as in `docs/en/17-DESIGN-0.0.3.md` and `docs/en/18-TECH-0.0.3.md`); `man瓜` is rendered "Pumpkin Turret", following the note in `docs/en/17-DESIGN-0.0.3.md`; Chinese 「」 quotation marks are rendered as English double quotes.
8. **Levitation wording**: following §5.3's own terminology paragraph, "levitation" appears only in the negation ("not a mechanism", "not the vanilla levitation effect") and in the historical constant name `PHASE_LEVITATING`; the mechanism itself is written as "phase 1 is a **servo lift straight up to a fixed height**".
