# 42 · v0.2.1 Technical Specification

> Project: Farmstorm · version **0.2.1** (MC 1.21.1 / NeoForge 21.1.213 / Java 21), baseline **the released 0.2.0 state** (`gradle.properties:11` currently `mod_version=0.2.0`).
> Single source of truth: `docs/zh/40-DECISIONS-0.2.1.md` (D-173 ~ D-186, **14** in total) + the **17** author's rulings of `.work/v021-BRIEF.md` §2 (#1 ~ #12 have D-number mappings; #13 ~ #17 were added during writing and document 40 has not transcribed them yet, see §9 item 6). This document **only cites** decision numbers and ruling numbers: it opens no new decision and changes no measure; where it conflicts with document 40 the conflict is reported to the author, and the conflict itself is registered in §9 of this document.
> Factual base: `.work/v021-BRIEF.md` §1 (source facts, with line numbers), §3 (the increment and the 12 mandatory changes), §4 (RT-51 ~ RT-60). **Where this card conflicts with the source, the source prevails** — every line number in this document was **checked by this document itself** after re-reading the source with `[IO.File]::ReadAllLines($p, UTF8)` at writing time; wherever it disagrees with `BRIEF` the item is registered in §9.
> Scan side witness: `.work/v021-scan.md` is used for locating only and enters no count.
> Stylistic reference: `docs/zh/36-TECH-0.2.0.md` (**only** the section structure of §1 baseline check / §2 class design / §3 action surface table / §4 full config table / §6 save / §7 risk / §8 acceptance / §9 to-verify / §10 difference register is learned; **not one of its values is cited**).
> Red line (two, both unchanged): **no Mixin (0, still prohibited)**; custom packets have **no cap**, this version **adds none**, **2** in total (D-186).
> Writing discipline: the three numbers — registry surface **97**, server config **175 → 176 keys / 60 groups**, language keys **337 → 338 × 2** — are **all named item by item by this document itself** (the methods are written in §1.2 / §1.3 / §4.1 / §4.3), copied from neither `BRIEF` §0 nor document 40; every technical parameter this document fixes on its own is marked "**[this document's decision]**" with a reason (the summary is in §0.3); **no task breakdown and no effort** (those belong to document 43), **no open items and no placeholder markers**.

---

## 0. Reading conventions

### 0.1 What this document is responsible for

| Item | Content |
|---|---|
| Responsible for | the per-class independent naming of the registry surface **97** (§1.2) + the explicit registration of **the 13th category outside the count** (§1.2.1); the per-key naming of server config **175 → 176 keys / 60 groups** and of the language keys **337 → 338 × 2** (§1.3, §4); the class design and call surface of the five technical components (periodic rebind / tooltip / AI intervention and restore / hostility predicate / trap multiplier) (§2); the master table of additions / changes / deletions against 0.2.0, **31 rows** (§3); the five questions of save and compatibility (§5); how RT-51 ~ RT-60 are verified (§6); the **31** acceptance self-check items (§7); the **10** APIs to verify (§8); the register of differences from upstream, **16 items** (§9) |
| Not responsible for | why it was decided as X (document 40); the player-visible presentation and the timing tables (document 41); tasks, phases, milestones (document 43); the master prompt and the phase prompts (documents 44 / 45) |
| Files left untouched | every **existing** file under `src/` is read-only (this document only describes "what it must become"; it lands no code); `docs/zh/00` ~ `41`, `docs/en/*` and `.work/*` are all read-only; this document creates exactly one file, `docs/zh/42-TECH-0.2.1.md` |

### 0.2 Counting discipline

1. **The three numbers — registry objects, config keys, language keys — are all counted by this document itself**, with a statement of "which lines of which files were counted". It copies neither `BRIEF` §0, nor document 40 §3, nor the `EXPECTED_COUNTS` of `verify_project.py`.
2. Each of the three tables satisfies "row count = the declared count", "subtotal = the sum of the rows" and "no overlap and no omission across tables".
3. Numbers may come from four sources only: document 40's decision numbers, `BRIEF` §1 / §2 / §3, arithmetic on two already-given numbers (`175 + 1 = 176`, `337 + 1 = 338`, `97 + 0 = 97`, `LCM(40, 100) = 200`), and the technical parameters this document marks "[this document's decision]" (a reason for each). **No gameplay value is invented.**
4. Always read files as UTF-8 (on this machine `Get-Content` without `-Encoding` reads BOM-less UTF-8 Chinese as GBK mojibake and **shifts the line numbers**: the same `FarmstormConfig.java` is read as 1095 lines instead of 1131, and every line number is then wrong). Every line number in this document comes from the UTF-8 reading.

### 0.3 The list of "[this document's decision]" (15 items, a reason in the body for each)

| # | Parameter | Value | Body |
|---|---|---|---|
| 1 | the rebind interval constant and the phase formula | `REBIND_INTERVAL_TICKS = 100`; `floorMod(now + floorMod(pos.hashCode(), 100), 100) == 0` | §2.1 |
| 2 | where the rebind gate is inserted | **before** the particle gate, a standalone `if`, **no** `return` | §2.1 |
| 3 | the shape of "enumerate block entities over an area" | `util/AreaBlockEntities#collect(ServerLevel, BlockPos, int, Class<T>) → List<T>` (a new list, going only through `getChunkNow`) | §2.1 |
| 4 | the carrier of the original AI flag and the runtime index | persisted: `AttachmentType<Suspend>` (`registry/FarmstormAttachments`); in memory only a `Set<UUID>` observation index + a `Map<UUID, Long>` of vibration-bean deadlines (**holding no original value**) | §2.3 |
| 5 | the attachment's data shape | `Suspend(boolean originalNoAi, int sources)` + the `NONE` sentinel; `Codec` serialization, `sources == 0` is not persisted; **no timestamp of any kind is stored** (ruling #16) | §2.3 |
| 6 | the constant of the vibration bean's AI suspension | `AI_SUSPEND_TICKS = 80` (**no** new config key) | §2.3 |
| 7 | the mount query entry point | `QbounceMountController#isMountMode` goes from `private` (`:199`) to `public`, **no second method is added** | §2.3 |
| 8 | the hostility predicate's class and method | `util/Hostility#isHostile(@Nullable Entity)` | §2.4 |
| 9 | the explosion's source mark | `FarmstormExplosion`'s 9th constructor parameter `boolean trapSourced`; the 8-param overload is kept and delegates `false` | §2.5 |
| 10 | the multiplier key's field name / range / comment | `trapDamageMultiplier`; `defineInRange("damageMultiplier", 3.0, 1.0, 10.0)` | §2.5, §4.1.2 |
| 11 | where the AI reconciliation is mounted | `ServerTickEvent.Post` + `EventPriority.HIGHEST` (it must run before `QbounceMountController.onServerTick`'s default priority) | §2.3 |
| 12 | `verify_project.py`'s new parsing contract | a new `expected_config_keys_v021`, bounded by `#### 4.1.1` ~ `#### 4.1.2`, 7 columns, row-count assertion `1` | §4.1.1 |
| 13 | the criterion for an ownerless core | the first line of `rebindNearby`: `if (this.ownerUuid == null) return;` (existing shape `emitBoundaryRing:535-537`) | §2.1.3 |
| 14 | the candidates' sort keys | `distSqr` ascending → `x` → `y` → `z` lexicographic (isomorphic to `selectCore:310-315`) | §2.1.3 |
| 15 | the registration shape of the 13th category outside the count | the attachment type `farmstorm:ai_suspend` listed on its own in §1.2.1: name / registry / the reason it is not counted in 97 / the tool does not check it | §1.2.1 |

---

## 1. Baseline and the registry-surface check

### 1.1 Platform and build (the released 0.2.0 state)

| Item | Value | Evidence |
|---|---|---|
| Minecraft / NeoForge / Java | **1.21.1 / 21.1.213 / 21** | `gradle.properties:2,3`; toolchain `:21-23` (`org.gradle.java.installations.paths=D:/project_farm/.tools/jdk21`) |
| `mod_version` | **0.2.0** (this version changes it to 0.2.1) | `gradle.properties:11` |
| Build entry point | `.tools\gw.cmd` (the only entry point; no sub-command dispatch) | `一键构建.cmd:8` only `call`s it |
| Run directory | `run/` | where the two tomls actually sit under `run/config/` |
| `src/main/java` Java file count (current) | **145** | counted by this document itself (`Get-ChildItem -Recurse -Filter *.java` over the whole tree); this version adds **4 classes** (§2 names them one by one: `util/AreaBlockEntities`, `combat/MobAiSuspension`, `util/Hostility`, `registry/FarmstormAttachments`), **149** after the change |
| Registry self-check entry point | `Farmstorm#logRegistrySelfCheck` (`:154-176`), called at `Farmstorm.java:80` | `:167-168` the sum, `:169-172` the per-class assertions, `:173` the log format string |
| server config generation site | the `FarmstormConfig` static block `:289` ~ `:995` (`SPEC = b.build()` at `:995`) | the counting range of §1.3 |
| Language generation site | `datagen/ModLangProvider` (`config(entries, …)` **178** times) | §1.3 / §4.3 |

### 1.2 Per-class naming of the registry surface (**counted out of the source by this document itself**, confirming **97 / +0**)

**Counting method (reproducible)**: open each of the **11** registry classes under `src/main/java/com/dashpig/farmstorm/registry/` and count item by item by "registration call + registered id literal", **never back-deriving from `EXPECTED_COUNTS`, never reading the assertion values in `Farmstorm.java`, never copying `BRIEF` §0**. Three counting measures:

- `DeferredRegister#register` / `registerItem` / `registerBlock` / `registerComponentType`: count **the number of calls** (the one inside a factory body counts once only);
- registrations that go through a factory method (`cropItem` / `seedItem` / `crop` / `deployed` / `projectile`): count **the number of factory calls**, with the id given by `CropProfile.path()` or by the first argument literal;
- `damage_type` is **data-driven** (the comment at `FarmstormDamageTypes.java:14`: the instance is supplied by the datapack JSON, the code side has only a `ResourceKey`): count **the number of static `ResourceKey` constants**.

| # | registry | What was counted now (file:line, **UTF-8 line numbers**) | Ids one by one | Current | v0.2.1 | After |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | `FarmstormItems.java`: **22** × `cropItem(...)` (`:37,39,41,43,45,48,50,52,54,56,58,60,62,64,66,71,73,76,80,86,92,96`) + **7** × `seedItem(...)` (`:90,101,102,103,104,105,107`) + **4** × `registerItem(...)` (`:111,114,117,121`) | 22 crops: `puffed_wheat` `blast_carrot` `ubi_server` `toxic_ubi_server` `power_beet` `melon_slice_bomb` `melon_grenade` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `lucky_berry` `molotov` `zangief_cane` `wild_apple` `compressed_puffed_wheat` `friend_chives` `point_defense_fungus` `vibration_bean`; 7 seeds: `chive_seeds` `puffed_wheat_seeds` `power_beet_seeds` `melon_grenade_seeds` `pumpkin_turret_seeds` `awakened_torchflower_seeds` `awakened_pitcher_plant_pod`; 4 block items: `converter` `compressed_hay_block` `outpost_core` `cultivator` | **33** | **+0** | **33** |
| 2 | `Registries.BLOCK` | `FarmstormBlocks.java`: **15** × `crop(...)` (`:28,29,30,31,32,33,34,35,36,37,38,39,40,41,45`) + **8** × `deployed(...)` (`:48,49,50,52,54,57,64,67`) + **4** × `registerBlock(...)` (`:71,75,84,95`) | 15 crop blocks: `puffed_wheat_crop` `blast_carrot_crop` `ubi_server_crop` `toxic_ubi_server_crop` `power_beet_crop` `melon_grenade_stem` `pumpkin_turret_stem` `awakened_torchflower_crop` `awakened_pitcher_plant_crop` `fang_trap_cactus_block` `bamboo_firecracker_stalk` `red_mushroom_puff_block` `brown_mushroom_puff_block` `lucky_berry_bush` `friend_chives_crop`; 8 deployed blocks: `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `zangief_cane` `point_defense_fungus` `vibration_bean`; 4 others: `converter` `compressed_hay_block` `outpost_core` `cultivator` | **27** | **+0** | **27** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | `FarmstormBlockEntities.java`: **8** × `REGISTER.register` (`:25,29,32,35,39,43,51,56`) | `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `converter` `outpost_core` `cultivator` | **8** | **+0** | **8** |
| 4 | `Registries.ENTITY_TYPE` | `FarmstormEntityTypes.java`: **12** × the factory `projectile(...)` (`:31,33,35,37,39,41,43,45,49,51,55,57`, factory body `:70-78`) + **1** × `REGISTER.register("turret_bullet", …)` (`:61`) | `puffed_wheat` `ubi_server` `toxic_ubi_server` `melon_slice_bomb` `melon_grenade` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `molotov` `compressed_puffed_wheat` `friend_chives` `vibration_bean` `turret_bullet` | **13** | **+0** | **13** |
| 5 | `Registries.MOB_EFFECT` | `FarmstormMobEffects.java:23,27,31,35` | `wild_hunter` `zangief_piledriver` `q_bounce` `confusion` | **4** | **+0** | **4** |
| 6 | `Registries.SOUND_EVENT` | `FarmstormSounds.java:20,25,30` | `wild_hunter_loop` `zangief_piledriver` `friend_chives` | **3** | **+0** | **3** |
| 7 | `Registries.RECIPE_TYPE` | `FarmstormRecipes.java:22` | `converting` | **1** | +0 | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | `FarmstormRecipes.java:24` | `converting` | **1** | +0 | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | `FarmstormCreativeTabs.java:16` | `main` | **1** | +0 | **1** |
| 10 | `Registries.DAMAGE_TYPE` | `FarmstormDamageTypes.java:20,23,26` (3 `ResourceKey` constants) | `void_bolt` `crop_blast` `zangief_slam` | **3** | +0 | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `FarmstormDataComponents.java:15` | `temporary_enchantments` | **1** | +0 | **1** |
| 12 | `Registries.MENU` | `FarmstormMenus.java:17,21` | `converter` `outpost_core` | **2** | +0 | **2** |
| | **Total** | | | **97** | **+0** | **97** |

Per-class subtotal (equal to the rows above, item by item):

```
items                 33
blocks                27
block_entity_type      8
entity_type           13
mob_effect             4
sound_event            3
recipe_type            1
recipe_serializer      1
creative_mode_tab      1
damage_type            3
data_component_type    1
menu_type              2
--------------------------------
sum = 33+27+8+13+4+3+1+1+1+3+1+2 = 97
```

**Three independent algorithms cross-check one another**: this document's item-by-item naming = **97**; the 12 runtime assertions of `Farmstorm#logRegistrySelfCheck` (`Farmstorm.java:154-176`) added up = **97** (`:167-168` the sum, `:169-172` per class); the `EXPECTED_COUNTS` of `tools/verify_project.py` (`:325-338`) = **97**. This document's number is **not** copied from the latter two — it was counted out again under the "number of calls" measure of the table above, and the two side witnesses are used only to confirm the measure has not drifted.

**The four reasons this version is +0 (consistent with D-175; only the technical side is written here)**: the rebind adds no block / block entity (it reuses the binding list that already exists between the two existing BEs `outpost_core` and `cultivator`); the tooltip runs on the vanilla `AbstractContainerScreen` chain (§2.2) and adds no screen class; the hostility predicate is a static method in `util/` (§2.4); the multiplier is a config key (§2.5), not a registry object. The carrier of the original AI flag is an **`AttachmentType`** (§2.3), which **does not enter the 12 classes of the table above** — the reason is in the next paragraph.

> ⚠️ **One measure that would make "+0" be misjudged**: this version adds 4 Java classes (§1.1), and 3 of them **contain not one registration call** — `AreaBlockEntities` / `Hostility` are pure functions, and `MobAiSuspension` holds only two in-memory containers and one `@EventBusSubscriber`; the 4th, `FarmstormAttachments`, **does** have 1 registration call, but it lands in a NeoForge registry. Inferring the registry surface from "the number of new classes" would yield the wrong answer of 101; the registry surface is counted only by the **registration calls** of the 12 classes above.

#### 1.2.1 The 13th category outside the count: `farmstorm:ai_suspend` (`ATTACHMENT_TYPES`, **not counted in 97**)

Ruling #17 requires this matter to be **written out explicitly, never invisibly** — "what the tool does not count is the same as not registered", which is the same nature as A-66's false green. So besides the 12 classes above, this version **registers 1 more attachment type**, named item by item:

| Item | Content |
|---|---|
| Registered name | `farmstorm:ai_suspend` |
| Type | `AttachmentType<Suspend>` (`Suspend(boolean originalNoAi, int sources)`, see §2.3.1) |
| Registry | **`NeoForgeRegistries.Keys.ATTACHMENT_TYPES`** (`AttachmentType.java:35`: "Data attachment types must be registered to the registry") |
| Registration site | the new file `registry/FarmstormAttachments.java`: `DeferredRegister<AttachmentType<?>>` + 1 × `REGISTER.register("ai_suspend", …)`; and in `Farmstorm.java`'s constructor it is `.register(modBus)` together with the other registers (**this is the only place this version changes `Farmstorm.java`**, see §3 #29) |
| Why it does not enter 97 | 97's measure is **this project's 12 classes** (§1.2's table: items / blocks / block_entities / entity_types / mob_effects / sound_events / recipe_types / recipe_serializers / creative_tabs / damage_types / data_components / menu_types), all of which land in `BuiltInRegistries` (the damage types are data-driven, so `ResourceKey`s are counted). `ATTACHMENT_TYPES` is **a registry NeoForge itself adds** and is not among these 12 ⇒ **97 unchanged**, `data_components` still **1** |
| Why it is not "a way around D-175" | **An attachment ≠ a data component**: what D-175 rejected is `data_components` (the 11th of the 12 classes, currently the single `farmstorm:temporary_enchantments`); adding one makes it 2 ⇒ 97 → 98. This version uses **NeoForge's entity attachment registry**, which is outside the measure, so 97 is unchanged. The two statements must be read together, or the reader will think "it went around its own rejection by changing the name" |
| Will `verify_project.py` check it | **No.** It does exactly two things for the registry surface: ① grep the source for the literals of `EXPECTED`'s (`:281-321`) 12 id groups one by one; ② add up the 12 counts of `EXPECTED_COUNTS` (`:325-338`) = 97. The attachment type **is in none of the groups** ⇒ it **checks neither its existence nor its count**. This is a known coverage gap, and **this version does not extend the tool** (D-174; the same class of pre-existing problem as A-66); it is covered by three manual registrations: this subsection + §3 #29 + §7 #29 |
| Is it synced to the client | **No.** There is no `.sync(...)` (not one of `AttachmentType.Builder`'s `sync` overloads is used); the attachment lives only on the server and in the save |
| Is it copied on death | **No.** `copyOnDeath()` is not called (the default is `false`, `AttachmentType.java:43`), which is the premise of §2.3.3 path ④ |

### 1.3 config and language-key baseline (named by this document itself)

**SERVER config (currently 175 keys / 60 groups)**: the measure is to read the static block of `config/FarmstormConfig.java` (from `static {` at `:289` through `SPEC = b.build();` at `:995`) and to land every key by the rule "`b.push("…")` / `b.pop()` maintain the group stack; a line-initial `.define(...)` / `.defineInRange(...)` counts one key". This document ran it twice and re-checked every group by hand:

```
line-initial .define* lines    = 175   ← all of them live definitions
commented-out definitions      =   1   ← // .defineInRange("farmlandBonus", 16, 0, 1024); at :935
b.push(...) count              =  60
b.pop() count                  =  60
                                    ⇒ 175 keys / 60 groups
```

Three reproducible side witnesses: ① the literal `.define` hit **177** lines = 175 live + 1 commented-out line (`:935`) + 1 comment line that merely mentions `.defineInRange` (`:229`, about the history of `:909-912`); ② the section count of `run/config/farmstorm-server.toml` is **untrustworthy right now** (see §9 item 2), so only the source count may be used; ③ the `trap` section runs from `:917` (`push`) to `:927` (`pop`), and its three keys are at `:920` / `:923` / `:926`, consistent with `BRIEF` §1.6.

**CLIENT config (currently 3 keys)**: `config/FarmstormClientConfig`'s `hud.enabled` / `hud.x` / `hud.y` (one `builder.push("hud")`). This version **leaves it alone**, and it **is not counted** in the 175 (the existing measure of `BRIEF` §0), but it **is counted** in §4.3's language keys.

**Language keys (currently 337 × 2, the zh and en key sets exactly identical)**: read `src/generated/resources/assets/farmstorm/lang/zh_cn.json` and `en_us.json`, take the key set after `ConvertFrom-Json`: `zh = 337`, `en = 337`, zh-only = **0**, en-only = **0**. Two reproducible side witnesses: `farmstorm.configuration.*` has **178** entries (= 175 server + 3 client), `tooltip.farmstorm.*` has **20** entries (consistent with `BRIEF` §0); and `datagen/ModLangProvider` contains exactly **178** `config(entries, "…")` calls, in one-to-one correspondence with the 178 language entries.

### 1.4 The two red lines (packets still 2, Mixin 0)

| Item | Current state (re-checked by this document) | v0.2.1 | Evidence |
|---|---|---|---|
| Custom packets | **2**: `farmstorm:core_list` (`FarmstormNetwork.java:38`), `farmstorm:cultivator_link` (`:43`), protocol version `"2"` | **2, none added** | `FarmstormNetwork.java:25` (`PROTOCOL_VERSION = "2"`), the two `.playToClient` at `:38` / `:43`; across the repository `playToClient\|playToServer` hits **2** registration calls |
| Mixin | **0** | **0**, still prohibited | across the whole `src/` tree `@Mixin` hits **0**; `build.gradle` / `gradle.properties` / `neoforge.mods.toml` have no mixin section; `*.mixins.json` under `src/` **0** (⚠️ the whole repository has 1 at `.scratch/p25a/src/neoforge.mixins.json` — an experimental leftover, not under `src/`, not part of the build, see §9 item 10) |

Two readings (getting either one wrong turns §7's #2 red):

1. Packets **do not enter the registry surface** — payloads go through `RegisterPayloadHandlersEvent` and write no `BuiltInRegistries`, so they are not among §1.2's 97.
2. Mixin's 0 is a **prohibition**, not "this version did not get round to it": all five components of this version are implemented with inheritance + the event bus + native extension points (in particular, **no** Mixin is used to change vanilla's `EvokerFangs` implementation to get the fangs ×3, ruling #11 + D-186).

---

## 2. The five technical components

### 2.1 Periodic rebind (an independent phase gate / enumerating the loaded chunks of the 3×3 / <16 / mutual exclusion with the placement binding)

#### 2.1.1 The two gates must each be independent (this version's biggest implementation trap)

`serverTick` (`OutpostCoreBlockEntity:507-525`) does **three things** today, and they are **sequential early returns**:

```
:508-510  not a ServerLevel          → return
:511-513  interval <= 0              → return        (interval = outpost_core.boundaryParticleIntervalTicks, default 40, range 20~1200, :939-941)
:515      now = getGameTime()
:516-518  the particle phase gate fails → return     (floorMod(now + floorMod(pos.hashCode(), interval), interval) != 0)
:519      emitBoundaryRing
:521-524  one INFO per TICK_LOG_ROUNDS(=30) rounds
```

**The trap**: hanging the rebind before or after `:519` (that is, downstream of the particle gate) makes the rebind run only on the ticks where "the particle gate happens to let it through" ⇒ the actual period = `LCM(40, 100) = 200` ticks, and changing `boundaryParticleIntervalTicks` (20 / 60 / 1200) drifts the period with it: set it to 1200 and the rebind becomes once per 1200 ticks, and the requirement "every 100 ticks" fails on the spot.

**[this document's decision] (1)**: the rebind gets **its own gate**, and the gate goes **before** the particle gate, uses a standalone `if`, and does **not** `return`. After the change `serverTick` does four things:

```
public static void serverTick(Level level, BlockPos pos, BlockState state, OutpostCoreBlockEntity core) {
    if (!(level instanceof ServerLevel server)) return;              // originally :508-510, untouched
    long now = server.getGameTime();                                  // moves up one line from :515 (a pure read, no side effect)
    if (rebindDue(pos, now)) {                                        // new: a standalone gate
        core.rebindNearby(server);                                    // new: no return
    }
    int interval = FarmstormConfig.outpostCoreBoundaryParticleIntervalTicks.get();  // originally :511
    if (interval <= 0) return;                                        // originally :512-513, untouched
    if (Math.floorMod(now + Math.floorMod(pos.hashCode(), interval), interval) != 0) return;  // originally :516-518, untouched
    core.emitBoundaryRing(server, pos, now, interval);                 // originally :519, untouched
    // the log block of :521-524, untouched
}
```

- **The only structural change is moving `now` up one line from `:515`** (`emitBoundaryRing` and the log block still use the same `now`, with no second read). Every other existing line is unchanged character for character — this satisfies D-174's "do not change what 0.2.0 already had in passing": moving it up is a **necessary condition** of the rebind gate, not an opportunistic optimisation.
- The two gates **do not block each other**: when the rebind is true, the particle gate still judges its own phase once, and there is no overlapping `return` between them.
- Phase staggering: `floorMod(pos.hashCode(), 100)` falls in `0~99`, so several cores inside the same 3×3 each occupy one tick and do not sweep at the same instant (ruling #2).

**[this document's decision] (1, continued)**: the constant and the gate method (written in `OutpostCoreBlockEntity`'s constant area, next to `TICK_LOG_ROUNDS` at `:91`):

```
private static final int REBIND_INTERVAL_TICKS = 100;      // the "every 100 ticks" of plan:9, not a config key

private static boolean rebindDue(BlockPos pos, long now) {
    return Math.floorMod(now + Math.floorMod(pos.hashCode(), REBIND_INTERVAL_TICKS),
                         REBIND_INTERVAL_TICKS) == 0;
}
```

**The reason there is no `interval <= 0` guard**: `REBIND_INTERVAL_TICKS` is the compile-time constant 100, and `floorMod(x, 100)` never throws; the particle gate's guard (`:512-513`) exists to prevent a division by zero when `interval` is configured as 0 (the config range cannot reach it, but the compiler cannot prove that). The two gates' guards **must not be borrowed from each other** — this is exactly the second place where "reusing the particle gate" goes wrong.

#### 2.1.2 "Enumerating block entities over an area": 0 precedents across the repository

**What already exists**: `ChunkMap` 0; `LevelChunk#getBlockEntities()` only **1** place (`DeployedCropBlockEntity:227-236`'s `countDeployedInChunk`, **a single chunk, counting only itself**, `:230` iterating `.values()`); `level.getAllEntities()` 2 places (`QbounceMountController:220`, `QbounceObserverSync:164`, both entities, not block entities). ⇒ it must be written from scratch.

**[this document's decision] (3)**: add `util/AreaBlockEntities.java`, with the single method:

```
public static <T extends BlockEntity> List<T> collect(ServerLevel level, BlockPos center,
                                                      int chunkRadius, Class<T> type)
```

Four implementation disciplines (each one corresponds to a place that will go wrong):

1. **Go only through `ServerChunkCache#getChunkNow(int,int)`; `Level#getChunk` / `getChunkAt` are forbidden**: when `getChunkNow` cannot get a loaded chunk it returns `null` ⇒ **skip that chunk** (no forced load, ruling #2). Existing shape: `OutpostCoreBlockEntity:296-300` (core selection) and `:384-387` (lazy cleanup) are both written this way, and this version does not break the convention. `Level#getBlockEntity(pos)` is also unusable — internally it goes through `getChunkAt` and **would trigger a load** (the comment at `:264-267` has already pinned this down).
2. **The chunk range = `RANGE_CHUNKS` (`:75`, value 1)**: `cx-1..cx+1` × `cz-1..cz+1` = **9 chunks**, the same set of numbers as `inRange`'s (`:454-465`) Chebyshev ≤ 1. **No second radius constant is introduced** — the comments at `:74` and `:77` have already made "the same set of numbers as D-118 and D-121" a discipline, and this version makes no exception.
3. **Return a new `ArrayList`, not the view of `chunk.getBlockEntities()`**: `LevelChunk#getBlockEntities()` returns **that chunk's own live `blockEntities` map** (`LevelChunk.java:528-530`, `return this.blockEntities;`), and adding or removing a block entity during iteration throws `ConcurrentModificationException`. This version follows `QbounceMountController.adoptExisting`'s **two-phase** form (the comment at `:213`: "collect first, then mount, so as not to addFreshEntity while iterating the entity list"): collect into a new list first, then write the bindings in the second phase.
4. **Filter by `Class<T>`, not by `BlockEntityType`**: the rebind candidate is exactly `CultivatorBlockEntity` (the `instanceof` semantics are the same shape as `DeployedCropBlockEntity:231`, `OutpostCoreBlockEntity:301`, `:689`). `type.isInstance(be)` is enough; the kind dispatch of `FarmstormBlockEntities.deployedOf` is not needed.

> 📌 **"Skipping the unloaded" is a defensive branch, not the norm**: the core's own tickets `CoreChunkTickets` give the centre chunk `distance = 1` (the `DISTANCE = 1` of `CoreChunkTickets.java:17-18`, `:43-44`), under the measure of `:25-29` ⇒ **the centre chunk is entity-ticking (block entities tick as usual) and the 8 surrounding chunks are loaded to FULL** ⇒ **in the normal 3×3 case all 9 chunks are in memory**. So the rebind is not a low-frequency behaviour that "finds nothing in most rounds"; it **steadily sweeps all 9 chunks every 100 ticks**. "Skipping the unloaded" takes effect in only three cases: ① the tickets are not in force yet (just after the core is placed / before the tickets are restored at server start, `restoreAll` at `:64-68`); ② the tickets were released by some other path; ③ an ownerless core (but such a core is already stopped by §2.1.3's first line and never reaches the enumeration). **Both the logging and the acceptance are written on the premise "normally sweeps all 9 chunks"**, not "sweeps occasionally".

**Cost**: per core, sweeping the `blockEntities` maps of 9 chunks every 100 ticks. The block-entity count of 9 chunks is of the order of "tens", once per 100 ticks ⇒ less than one map traversal per tick when amortised. This is the positive answer to the sentence at `:326` ("every tick × 16 entries × the number of cores, a disproportionate cost"): **it is not every tick, and it does not sweep 16 entries**.

#### 2.1.3 The write boundary: only the unbound are rebound, a bound one is never taken over, rebind only below 16, stop when full

**[this document's decision] (1, continued)**: the criteria and their order in `rebindNearby`:

```
private void rebindNearby(ServerLevel server) {
    if (this.ownerUuid == null) return;                                 // ruling #13: an ownerless core does not rebind
    int cap = FarmstormConfig.outpostCoreCultivatorCapacity.get();      // :949-951, default 16
    if (cultivatorCount() >= cap) return;                               // full ⇒ skip the whole round (no enumeration)
    List<CultivatorBlockEntity> candidates =
            AreaBlockEntities.collect(server, getBlockPos(), RANGE_CHUNKS, CultivatorBlockEntity.class);
    candidates.sort(Comparator                                          // ruling #14: distance ascending → coordinate lexicographic
            .comparingDouble((CultivatorBlockEntity c) -> c.getBlockPos().distSqr(this.getBlockPos()))
            .thenComparingInt(c -> c.getBlockPos().getX())
            .thenComparingInt(c -> c.getBlockPos().getY())
            .thenComparingInt(c -> c.getBlockPos().getZ()));
    for (CultivatorBlockEntity cultivator : candidates) {
        if (cultivatorCount() >= cap) break;                            // re-read after every binding; stop when full
        if (cultivator.corePos() != null) continue;                      // already bound (including to another core) ⇒ skip, do not take over
        if (hasCultivator(cultivator.getBlockPos())) continue;            // conservative: the core side already has it ⇒ skip
        if (addCultivator(cultivator.getBlockPos())) {                    // write the core side first
            cultivator.setCore(this.getBlockPos());                       // then the cultivator-side cache (both sides are written)
        }
    }
}
```

Seven hard constraints (a reason for each):

1. **Capacity is read via `cultivatorCount()` / `isCultivatorListFull()` (`:334`, `:343`), not via `cultivators()` (`:328`)**: the latter first calls `pruneCultivators()` (`:329`, `:380-391`) and may `setChanged`, and interleaving it with the rebind in the same loop makes "the coordinates bound this round" counted twice or missed in the later decisions (D-176 supplement).
2. **`addCultivator` refuses a rebind and a full list itself** (`:352`), but the rebind loop **must still** `break` at `cap` — otherwise, once full, it would call a method that is bound to return false on each of the remaining candidates, running the enumeration once for nothing (RT-53).
3. **`removeCultivator` (`:366-377`) is not called**: the rebind **only adds, never removes** (D-177). A candidate whose "two sides disagree" is always **skipped** rather than "bound first and removed afterwards" — D-165's handling of "a disagreement is treated as unbound (unusable)" means that following it in the rebind would create a second owner. The method's call count therefore **stays 0**; this version neither enables nor deletes it (document 40 §7 ① left that question to document 42, and the conclusion is exactly this item).
4. **No extra broadcast**: `addCultivator` already does `setChanged() + syncBindings() + CultivatorLinkBroadcaster.requestAll(...)` (`:356-362`). One round has at most 16 `requestAll` calls, and it has a 10-tick debounce (`CultivatorLinkBroadcaster.MIN_INTERVAL_TICKS = 10`, `:54`), so at most one extra packet is sent after merging. **Another** request from the rebind would only add noise.
5. **At most one fill-up per round**: bind up to `cap` in the same round and `break`; if another slot frees up, the next round (100 ticks later) fills it again. There is no two-pass loop of "sweep every free slot once and then come back to fill" — that would blur the boundary of "stop when full".

**Where the two new rulings land (#13 / #14, a reason for each)**:

6. **An ownerless core does not rebind (ruling #13)**: a core placed by `/setblock` has `ownerUuid == null` (`setOwner` is the only write point, `:410-419`); at that point "our side" cannot be decided and the 16 slots have no owner, so rebinding it amounts to recording someone else's cultivators under a core with no owner. The criterion goes on the method's **first line** (before the capacity and the enumeration); the existing shape is `emitBoundaryRing`'s `:535-537` (`if (this.ownerUuid == null) return;`, with the comment "a core that came out of `/setblock` has no owner"). ⚠️ This item is added **only** to the rebind: the one in `emitBoundaryRing` is existing code and this version changes not a character of it.
7. **Deterministic candidate ordering (ruling #14)**: a round usually finds more candidates than free slots, and the `List`'s enumeration order comes from the `HashMap` iteration order of `chunk.getBlockEntities()` ⇒ **without sorting there is no guarantee that two runs of the same save bind the same batch** (the `HashMap` order is stable within one JVM version, but changes with a version change or a different resize history). The sort keys are **isomorphic** to the placement-time `selectCore:310-315` of 0.2.0: `distSqr` ascending → `x` → `y` → `z`. Note that there is **no** `placedGameTime` level here — a cultivator has no "placement time" field and no UUID to compare; the coordinate triple is **unique** for the same block in the same world, and that is enough to order them (`selectCore` has the extra `placedGameTime` level because it sorts **cores**, and a core has that field).

#### 2.1.4 Mutual exclusion with "the placement binding" (RT-51)

The two binding write points (placement: `CultivatorBlock:86-102`'s `:99` + `:100`; migration: `OutpostCoreBlockEntity:685-691`) and the third one (this section's rebind) are **all on the server main thread**, and all obey the same order: **write the core side `addCultivator` first, and write the cultivator side `setCore` only after it succeeds**. So "the same block written once by each of two cores" leaves exactly one window: **this round's sweep and a placement write happen on the same tick**.

- Within the same tick, the player interaction (`setPlacedBy`) completes before the entity tick phase, and the core's `serverTick` runs after it; the two cores' `serverTick`s run one after the other on a single thread, in chunk-tick order.
- After the first writer has written `setCore`, the later arrival reads `cultivator.corePos() != null` ⇒ skips.
- Therefore **no extra lock is needed, no `removeCultivator` fallback is needed, and no "re-check once after the sweep" is needed** — the synchronous write of `setCore` is the mutual-exclusion surface itself.
- The candidate list holds **live `CultivatorBlockEntity` references** (instances from `getBlockEntities()`), so `setCore` cannot possibly meet a `null` on this path; the half-written path of "the core side succeeded, the cultivator side failed" is unreachable in the rebind (the migration path has its own exit ③, `:695-704`).

### 2.2 The core storage tooltip (why the default chain already suffices / what breaks it / whether `renderTooltip` must be added explicitly)

**Conclusion ([this document's decision])**: **add nothing and override nothing** of the rendering methods. `OutpostCoreScreen` (42 lines) keeps "overriding only `renderBg` (`:37-41`)"; the deliverable is **an explicit acceptance item** (§7 #11 ~ #13), not a piece of new code. This is D-178's "prefer not to change", and the positive answer to RT-59.

**Why the default chain already suffices (four pieces of evidence)**:

1. `OutpostCoreScreen` overrides only `renderBg` (`:37-41`), and has **no** `render` override ⇒ the most common way of breaking it, "overriding `render` and forgetting to call `super`", does not exist.
2. **In that class** the `render` / `renderTooltip` / `renderLabels` / `init` overrides are **all 0** (the whole class has `renderBg` as its only override); **across the repository** `renderTooltip` hits **0** places ⇒ no layer intercepts the tooltip or replaces it with something else. (For comparison: `ConverterScreen:77` overrides `renderLabels`, `CoreHudDragScreen:66` overrides `render` — they are not the core storage screen, have nothing to do with this item, and must not be changed in passing.)
3. The 54 storage slots + 36 player slots are all **vanilla `net.minecraft.world.inventory.Slot` instances** (`OutpostCoreMenu:68`, `:79`, `:84`), with no custom `Slot` subclass changing behaviour such as `isActive()` / `getItem()` that decides "whether there is something under the cursor".
4. The layout is vanilla's double chest: `IMAGE 176×222`, `TEXTURE 256×256` (`:24-28`), `generic_54.png` ⇒ the slot coordinates line up with the texture, and `hoveredSlot` does not stay `null` because of misaligned coordinates.

The vanilla chain itself is: at the end of `AbstractContainerScreen#render` it calls `renderTooltip(guiGraphics, mouseX, mouseY)`, which draws only when `hoveredSlot != null && hoveredSlot.hasItem()`. The four pieces of evidence correspond to its four preconditions, all of which are satisfied. ⚠️ The line number of the vanilla call site cannot be checked on this machine (`BRIEF` §1.2 gives only in-repository evidence), so **the acceptance can only be written as "visible on hover", never as "`render` calls it on line N"**.

**What breaks it (five items, each with its current state and criterion)**:

| # | How it breaks | Current state | Criterion (written into §7) |
|---|---|---|---|
| 1 | someone overrides `render` and does not call `super` | 0 places | `OutpostCoreScreen`'s `render` override count = 0 |
| 2 | someone overrides `renderTooltip` and draws it themselves | 0 places (0 hits across the repository) | across the repository `renderTooltip` hits = 0 |
| 3 | the slot coordinates / `imageWidth` are misaligned with the texture ⇒ `hoveredSlot` is always `null` | the current layout is correct | hovering slot 1 / 28 / 54 all show a tooltip |
| 4 | a custom `Slot#isActive()` returns `false` | all vanilla `Slot`s | inside `OutpostCoreMenu` there is no `Slot` subclass other than `new Slot(` |
| 5 | `stillValid` always `false` ⇒ the screen is closed | follows the vanilla semantics (`:133-134`) | after opening, standing within 8 blocks does not close the screen |

**Whether `renderTooltip` must be added explicitly**: **no**. Adding it produces one of exactly two outcomes — either the tooltip is drawn twice, duplicating vanilla, or vanilla's handling of positioning / scaling / background / delayed display is masked (D-178 has already rejected "rewriting `render` to draw the tooltip oneself"). **The fallback (only enabled if §7 #11 fails)**: in that change, add one `super.render(...)` to the `render` override (or call `renderTooltip` explicitly and **not** call `super.render` twice); the change is ≤ 2 lines and still adds no rendering code.

### 2.3 AI intervention and restore (the carrier choice / recording the original value / the **five** cleanup paths / RT-54's residue risk)

#### 2.3.1 The carrier: an `AttachmentType` persisted on the entity (ruling #15); memory holds only the index

Ruling #15 pins down two things: **the original value is recorded on the entity (a DataAttachment of some kind)**, and **that piece of data must be deleted immediately after the restore**. Four candidates are compared one by one (`BRIEF` §7 authorises this document to fix the technical shape):

| Candidate | Survives a server stop / a crash | Relation to ruling #15 / D-175 | Verdict |
|---|---|---|---|
| a pure in-memory `Map<UUID, Boolean>` table | a server stop, yes (if a give-back on stop is added); **a crash, no**: the entity NBT keeps `NoAI:1b` while the table is gone ⇒ the original value is lost | ruling #15 states explicitly "not an in-memory `Map<UUID,Boolean>`" | **rejected** |
| `DataComponentType` (the `temporary_enchantments` kind that 0.2.0 already has) | yes | it is **the 11th class** of §1.2 (currently 1) and adding one makes it 2 ⇒ **97 becomes 98**; D-175 rejected exactly this | **rejected** (D-175 stays in force) |
| change the restore criterion to "infer it from the effect / a timer" and store no original value | Q-Bounce, yes, but **the original value cannot be inferred** | conflicts with ruling #8's "restore the **original value**" (always restoring to "has AI" is `WildHunterAura:119`'s old measure) | **rejected** |
| **`AttachmentType<Suspend>`** | yes: `serialize(Codec)` persists with the entity (`AttachmentHolder.serializeAttachments:122-145` / `deserializeAttachments:151-172`), so the next load after a crash can read it; after the restore, `removeData` clears it at once | it is registered in `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` (`AttachmentType.java:35`) and is **not among the 12 classes of §1.2** ⇒ **97 unchanged**; it is not the same kind of thing as the "data component" D-175 rejected (the next paragraph of §1.2 + §9 item 14) | **adopted** |

**[this document's decision] (4 / 5)**: the data class `combat/MobAiSuspension.java` (in the same package as the only precedent `WildHunterAura`, for side-by-side reading; it is not a stateless utility, so it does not go into `util/`) + the registry class `registry/FarmstormAttachments.java`. The shape:

```
public record Suspend(boolean originalNoAi, int sources) {          // only two fields: no timestamp of any kind (ruling #16)
    public static final Suspend NONE = new Suspend(false, 0);
    public static final int SRC_VIBRATION_BEAN = 1, SRC_Q_BOUNCE = 2;      // source bit mask
    // Codec: record → CompoundTag written by hand (two fields, no Codec combinator, the same style as TempEnchantRecord)
}

public static final DeferredHolder<AttachmentType<?>, AttachmentType<Suspend>> AI_SUSPEND =
        REGISTER.register("ai_suspend", () -> AttachmentType.builder(() -> Suspend.NONE)
                .serialize(Suspend.CODEC, s -> s.sources() != 0)          // NONE is not persisted
                .build());
```

Four design trade-offs:

1. **Why a "source bit mask" rather than `refCount` + `EnumSet`**: an `int` serialises better than an `EnumSet` (`Codec.INT` is enough); "how many sources have not exited yet" = `sources != 0`, so no separate count is needed. **A repeated entry from the same source** (the same mob hit by two vibration beans one after the other) **does not double it**: the `sources` bit is unchanged and the deadline takes `max` in memory (extended to the later expiry) — the semantics are "held until the latest expiry".
2. **`sources == 0` is the only existence criterion**: `getData(TYPE)` (`AttachmentHolder.java:74`) returns the default `NONE` when nothing has ever been attached, so "is it suspended" looks only at `sources != 0` and **not at `hasData`** (`:68`) — after the default value has been taken, `hasData` may also be true, and mixing the two measures creates an "empty suspension record" (§8 ⑩).
3. **What is persisted is only "the original value + the sources", with no timestamp at all (ruling #16)**: the vibration bean's 80-tick deadline is **runtime** data and lives in memory (`Map<UUID, Long>`), not in the attachment — this way "the window does not cross a save" holds **structurally** (there is no deadline in the save to read), rather than by a piece of cleanup code. Ruling #16 also rejects "storing the expiry instant in the attachment": that is exactly the "record a point in time" shape document 40's D-179 criticised (`WildHunterAura:52`'s `AI_RESTORE_AT`).
4. **Restore means delete** (ruling #15's hard requirement): `release()` is the only exit and always ends with `mob.removeData(AI_SUSPEND)` (`AttachmentHolder.java:107`) — not `setData` to `NONE`, because after `removeData` a `getData` falls back to `NONE`, which is exactly the same semantics as "never attached". **The persistence surface** is `AttachmentHolder.ATTACHMENTS_NBT_KEY = "neoforge:attachments"` (`:27`), and `Entity extends AttachmentHolder` (`Entity.java:131`) ⇒ the key sits in this mod's namespace, travels with the entity, and pollutes no other mod.

#### 2.3.2 Recording the original value: only the one before the first entry

`WildHunterAura`'s precedent **does not meet this definition** (`AI_RESTORE_AT:52` stores "the expiry instant" and the restore hard-codes `setNoAi(false)`, `:119`) — it would **wrongly release** an entity that was "already `noAi` before the entry". This version changes it to:

```
suspend(mob, sourceBit, vbDeadline):        // vbDeadline is given only when sourceBit == SRC_VIBRATION_BEAN
    Suspend cur = mob.getData(AI_SUSPEND);              // never attached ⇒ NONE
    if (cur.sources() == 0):
        boolean original = mob.isNoAi();                // read first, then change (the order must not be reversed)
        mob.setData(AI_SUSPEND, new Suspend(original, sourceBit));   // persist first: original value + source
        mob.setNoAi(true);                              // then change the state
    else:
        // already suspended: the original value is never touched, only the source bit is added
        mob.setData(AI_SUSPEND, new Suspend(cur.originalNoAi(), cur.sources() | sourceBit));
    if (sourceBit == SRC_VIBRATION_BEAN):
        VB_DEADLINE.merge(mob.getUUID(), vbDeadline, Math::max);     // the deadline stays in memory only, never persisted
    OBSERVED.add(mob.getUUID());                        // observation index (holding no original value)

release(mob, sourceBit):
    Suspend cur = mob.getData(AI_SUSPEND);
    int left = cur.sources() & ~sourceBit;
    if (left == 0):
        mob.setNoAi(cur.originalNoAi());                // restore from the original value
        mob.removeData(AI_SUSPEND);                     // delete at once (ruling #15)
        VB_DEADLINE.remove(mob.getUUID());              // clear the two in-memory pieces together
        OBSERVED.remove(mob.getUUID());
    else:
        mob.setData(AI_SUSPEND, new Suspend(cur.originalNoAi(), left));
        if (sourceBit == SRC_VIBRATION_BEAN) VB_DEADLINE.remove(mob.getUUID());
```

- **The order is pinned down**: `getData` to read the original value first → write the attachment → then `setNoAi(true)`. Reversed, a crash between `setNoAi` and the attachment write leaves a permanent dirty state of "the entity has no AI and there is no original value" (the first entry point of RT-54's residue risk).
- **The same entity suspended by two places at once** (hit by the vibration bean first, then by Q-Bounce): the original value records only the reading **before the first** entry (D-179 supplement). If a later entrant overwrote the original value, it would take the reading that this version already changed to `true` as the original and lock the entity for good on restore.
- **The `sources` bit mask** replaces "the form of the entry count" that D-179 left to this document: the entity is truly restored only when `sources` reaches zero, and "the vibration bean expired but Q-Bounce is still there" does not restore early (`left != 0` takes the else branch).
- **Only `SRC_VIBRATION_BEAN` has an in-memory deadline** (`VB_DEADLINE`): 80 ticks is a runtime duration and, per ruling #16, does not cross a save; `SRC_Q_BOUNCE`'s "expiry" needs no record at all — the criterion is "is the effect still there" (§2.3.4).
- **`Mob` only**: the predicate has the same shape as `WildHunterAura:88` (`instanceof Mob`), and a player is never in the table (`Player` is not a `Mob`).
- **The relation of `OBSERVED` / `VB_DEADLINE` to the attachment**: the two in-memory pieces may be lost and may go dirty, **the original value is never lost** — it lives only in the attachment. This is ruling #15's key gain over a pure in-memory table.

#### 2.3.3 The five cleanup paths (ruling #15's "must be deleted immediately after the restore, no residue allowed")

| Path | Trigger | Who cleans up | Cleanup action |
|---|---|---|---|
| ① Q-Bounce expires naturally | `LivingEntity.java:790-792` removes the effect | the per-tick reconciliation (§2.3.4): `!hasEffect` | `release(entity, SRC_Q_BOUNCE)` → restore from the original value → `removeData` |
| ② `/effect clear` | `EffectCommands` → `removeAllEffects()` (`LivingEntity.java:911-921`) | as above | as above |
| ③ milk | `MilkBucketItem.java:27` → `removeEffectsCuredBy(MILK)` (`LivingEntity.java:3517-3525`) | as above | as above |
| ④ the entity dies / is removed | `LivingDeathEvent`, or the reconciliation reads `isDeadOrDying()/isRemoved()` | the reconciliation | **do not write noAi** (it is already dead), only `removeData` + drop the index. ⚠️ An entity's serialisable attachments are **not copied on death by default** (`AttachmentType.java:43`) ⇒ no dropped item / respawned entity is polluted, and this item cannot create a second host |
| ⑤ a chunk is reloaded after being unloaded | the entity is persisted with the chunk (`neoforge:attachments`) and read on the next load | `EntityJoinLevelEvent` → `adopt` | after reading `sources`, **settle the vibration-bean branch first** (ruling #16: the window does not cross a save ⇒ `release(SRC_VIBRATION_BEAN)`, keeping only the original-value semantics); the Q-Bounce branch continues on "is the effect still on the body" (§2.3.4) |

- **Why "a chunk is unloaded" is also a path**: on unload the entity is written into the chunk NBT (`NoAI:1b` together with the attachment), and at that instant it **must not** be restored (the entity is not in the world and `setNoAi` has no object to write to). So this version handles it as "**settle on load**": the vibration-bean branch is **always treated as already expired** on load (an early restore is harmless, see the next item); the Q-Bounce branch relies on the effect itself being in the save, so it crosses a save naturally.
- **The vibration bean's window does not cross a save (ruling #16)**: the 80-tick deadline lives only in memory (`VB_DEADLINE`), and **the attachment simply does not have it**. So "crossing a save" is structurally impossible: after one save / load, the loading side's `adopt` can only read "there is an `SRC_VIBRATION_BEAN` source", never "how many ticks are left", and per ruling #16 it always **restores from the original value + deletes that source's record**. The cost: a mob may stop for only 2 seconds instead of 4 — rare (it requires the mob to be unloaded exactly inside the 80-tick window), and **an early restore is harmless**. Ruling #16 also **rejects** "storing the expiry instant in the attachment too": that is the "record a point in time" shape document 40's D-179 has already criticised (`WildHunterAura:52`'s `AI_RESTORE_AT` is exactly it).
- **A server stop is a special case of ⑤** (every entity has "unloaded"): this version still mounts `ServerStoppingEvent` (precedents `QbounceMountController:157`, `FarmstormGameEvents:191`) for one **best-effort settlement** (write what can be written: the vibration bean is always restored, Q-Bounce is judged by the effect), purely so that the save is clean and one correction on load is saved; **correctness does not depend on it** — even if that round missed something because the chunk was already unloaded, the attachment is caught by `adopt` on the next load.

**The persisted shape ([this document's decision] (5))**: the attachment `farmstorm:ai_suspend`, whose value is `Suspend(originalNoAi, sources)`, is written with the entity into `neoforge:attachments` (the key name at `AttachmentHolder.java:27`, the write at `:122-145`, the read-back at `:151-172`). `sources == 0` (`NONE`) is **not written** (`serialize(codec, s -> s.sources() != 0)`) ⇒ in the normal state there is **not a single byte** of it in the save.

**`adopt`'s three criteria** (the settlement measure on load: rather restore early than lock forever):

1. **Settle the vibration bean first**: if `sources` contains `SRC_VIBRATION_BEAN` ⇒ always `release(SRC_VIBRATION_BEAN)` (no timestamp is read, and the attachment has none). If it was the only source ⇒ restore from the original value + delete the attachment; if it also contained Q-Bounce ⇒ clear only this bit and stay suspended.
2. **Then judge Q-Bounce**: if `sources` contains `SRC_Q_BOUNCE` and `!mob.hasEffect(Q_BOUNCE)` ⇒ `release(SRC_Q_BOUNCE)` (which may likewise trigger a restore + delete the attachment); if the effect is still there ⇒ add it back to `OBSERVED` and leave it to the reconciliation to converge.
3. **An entity with `originalNoAi == true`** (no AI before the entry to begin with): on restore it **still writes back `true`**, not `false` — this is exactly the dividing line between ruling #8 and the old precedent `WildHunterAura:119`.

> 📌 Ruling #16 also **removes a candidate design**: had "persisting the deadline too" been adopted, a "time-went-backwards guard" would have had to come with it (`deadline - now > 80` is treated as already expired, precedent `OutpostCoreBlockEntity:540-541`'s `LAST_RING_TICK`). Now the persisted record has **no timestamp at all**, so neither a rollback nor a world copy needs a special case — this is the direct gain of "store no point in time" over "store a point in time + all sorts of fallbacks", registered in §9 item 16.

#### 2.3.4 Q-Bounce: the restore criterion is "whether the effect is still on the body"; no one-shot timer (RT-56)

**Why a timer cannot be used**: `FriendChivesProjectile` reads `friend_chives.effectTicks` as the **field duration** when it lands (`:82`), then **re-scans every tick** and writes "field duration − elapsed ticks" (`:155-172`, `:156` computes `ticks`, `:167` applies it). Within one field the effect is **applied repeatedly** and the duration counts down tick by tick. Any "arm an N-tick timer on the first application" would either ① be reset by the repeated application every tick (never restores), or ② restore early while the effect is still there (the suspension is lifted mid-way).

**[this document's decision] (4, continued)**: the entry side and the exit side are **split into two places**, because the hooks available at the two are asymmetric (checked in §8 ③):

- **Entry**: `QbounceEffect` overrides `MobEffect#onEffectAdded(LivingEntity, int)` (`MobEffect.java:86`) and forwards to `MobAiSuspension`. This method **is called only in the branch where "there was no such effect before"**: `LivingEntity.addEffect` calls `p_147208_.onEffectAdded(this)` only when it judges `mobeffectinstance == null` at `:957-961`, and a rescan refresh goes through `:962`'s `update` and **does not trigger it**. ⇒ Repeated application does not rewrite the attachment (the `sources` bit mask is unchanged).
- **Exit**: **not done in the effect class** — 21.1.213's `MobEffect` **has no `onEffectRemoved`** (only `:86`'s `onEffectAdded`). The exit is decided by **the per-tick reconciliation** reading `hasEffect`:

```
sweep(server):                                      // every tick, O(index size)
  for id in copyOf(OBSERVED):                       // iterate a copy: release mutates the index
      Mob mob = resolve(server, id)                 // getEntity dimension by dimension
      if (mob == null):
          OBSERVED.remove(id); VB_DEADLINE.remove(id)   // not observable ⇒ drop the two in-memory pieces only, always keep the attachment
          continue                                      // settled by adopt on load (§2.3.3 criteria 1/2)
      if (mob.isRemoved() || mob.isDeadOrDying()):  release all sources (delete the attachment, do not write noAi)   // path ④
      else:
          Suspend s = mob.getData(AI_SUSPEND)
          if (s.sources() contains Q_BOUNCE && !mob.hasEffect(Q_BOUNCE)) release(SRC_Q_BOUNCE)     // paths ①②③
          if (s.sources() contains Q_BOUNCE && QbounceMountController.isMountMode()) release(SRC_Q_BOUNCE)  // §2.3.5's second layer
          if (s.sources() contains VIBRATION_BEAN && now >= VB_DEADLINE.getOrDefault(id, now)) release(SRC_VIBRATION_BEAN)
```

> Implementation note: one reconciliation pass may fire `release` twice in a row (Q-Bounce and the vibration bean expiring on the same tick). Before the second one, `mob.getData(AI_SUSPEND)` must be **re-read** — the first may already have deleted the attachment and zeroed `sources`; calling `release` again with the stale `s` is idempotent, but the log doubles and `setNoAi` is written one extra time.

- **All three automatic removal paths are covered, and the evidence is in the 21.1.213 source (not inferred)**:
  - **natural expiry** → `LivingEntity.java:790-792` posts `MobEffectEvent.Expired` first, then goes through `onEffectRemoved`; once the effect is removed from `activeEffects`, `hasEffect` becomes false.
  - **`/effect clear`** → `EffectCommands` calls `removeAllEffects()` → `LivingEntity.java:911-921` (`:920` goes through `EventHooks.onEffectRemoved`) → `hasEffect` becomes false.
  - **milk** → `MilkBucketItem.java:27` calls `removeEffectsCuredBy(EffectCures.MILK)` → `LivingEntity.java:3517-3525` (`:3524` the same hook) → `hasEffect` becomes false.
  - The three paths' **common property**: whichever is taken, what the next reconciliation reads is "the effect is gone". That is the implementation of "judge by whether the effect is still on the body" — it **depends on the delivery of no removal event whatsoever**, so the subtle differences of event semantics in 21.1.213 cannot affect correctness (the reconciliation is authoritative, and **this version mounts not one removal event**, one fewer thing to depend on).
- **"Not observable" is not "the entity is gone"**: after a chunk unload `level.getEntity(uuid)` returns `null`, and at that moment it **must never** be treated as death and have the attachment deleted — deleting it loses the original value (RT-54's second entry point). This document fixes it as: when not observable, **drop only the two in-memory pieces** (the index + the deadline) and keep the attachment; on load it is settled by `adopt`'s criteria 1/2 (ruling #16: the vibration-bean branch is always treated as expired on load). Therefore **no** upper-bound constant such as "give up after not being observable for N ticks" is needed — **memory may be dropped freely; the attachment is the truth**.
- **No event is mounted for the death path**: `LivingDeathEvent` (precedent `QbounceMountController:124-130`) **need not** be mounted — the reconciliation reads `isRemoved()/isDeadOrDying()` and takes path ④. One fewer event mounted is one fewer failure surface of "the event was not delivered".

**The mount state is skipped (ruling #7) and the second layer (restore first on a switch)** (§2.3.5).

#### 2.3.5 Mount mutual exclusion: one entry point only, and it must not parse the config itself

`isMountMode()` is `private` today (`QbounceMountController:199-201`), and neither the effect class nor the AI table can reach it. **[this document's decision] (7)**: change `private static boolean isMountMode()` at `:199` to **`public static boolean isMountMode()`**, and **add no second method** — one thing must not have two implementations (this is how document 40's glossary "the truth about mount lives in exactly one place" lands; the change is one modifier).

- **Neither the effect class nor the AI table may parse `qbounce.poseMode`** (`QbounceMountController:50`'s `POSE_MODE_MOUNT` is the only literal and `:200` is the only read point). `MobAiSuspension` only calls `isMountMode()`.
- **Two layers of semantics**:
  - the first layer (it is mount at entry): in `suspend()`, `if (sourceBit == SRC_Q_BOUNCE && QbounceMountController.isMountMode()) return;` ⇒ do not write the attachment, do not suspend the AI, and the entity moves as usual.
  - the second layer (it switches to mount after the suspension): the line `if (contains Q_BOUNCE && isMountMode()) release(SRC_Q_BOUNCE)` in the reconciliation ⇒ as soon as it switches to mount, put the AI back from the original value and delete the attachment first, **and on the same tick hand it to `QbounceMountController` to mount**, or the mount would run 0 movement with a `noAi` entity on its back.
- **The ordering guarantee ([this document's decision] (11))**: the reconciliation is mounted on `ServerTickEvent.Post` with `@SubscribeEvent(priority = EventPriority.HIGHEST)`; `QbounceMountController.onServerTick` (`:173-196`) writes no priority ⇒ NORMAL. HIGHEST runs first ⇒ the second layer restores within the same tick, and the mounting happens afterwards.
- **`MobAiSuspension` must not change the mounting logic**: it only reads `isMountMode()`; it does not touch `ACTIVE`, does not discard the mount and does not `startRiding`.

#### 2.3.6 The vibration bean's hookup (D-180)

`VibrationBeanTrap#trigger` (`:74-105`) today applies `CONFUSION` only to the **players** inside the sphere (`:83-91`), with 0 damage / 0 effect / 0 displacement for mobs. This version **appends a block** after `:91` and before `pos` is taken at `:92` (the existing four steps — player confusion / explosion sound / `consume` / grant the advancement — are unchanged character for character):

```
for (Mob mob : level.getEntitiesOfClass(Mob.class, TrapBehavior.searchBox(pos, radius))) {
    if (!mob.isAlive()) continue;
    if (center.distanceTo(mob.position()) >= radius) continue;   // the same strict < measure as the trigger side
    MobAiSuspension.suspend(mob, MobAiSuspension.SRC_VIBRATION_BEAN, level.getGameTime() + AI_SUSPEND_TICKS);
}
```

- **The radius introduces no second number**: it reuses `vibration_bean.triggerRadius` (`:900-902`, default 4.0), the same key and the same "strictly less than" as `findTrigger` (`:50`) and the confusion side (`:79`).
- **`AI_SUSPEND_TICKS = 80` is a constant, not a config key** (ruling #5; D-180 has already rejected "writing the 80 ticks as a new config key" — that would make it 177).
- **"80 ticks" is how long the AI is suspended, not how long the confusion lasts**: the latter is `vibration_bean.confusionTicks` (`:903-905`, default also 80). The two **share a value but not a meaning** and must not be merged.
- **The range is a one-off snapshot at the instant of the trigger**: the trap is `consume`d at `:97`, so mobs that enter the radius afterwards are not suspended, and those that leave early are not restored early (document 40 §7 ⑥).
- **`Mob` already excludes players** (`Player` is not a `Mob`), so the code writes no redundant branch such as `!(mob instanceof Player)`; the same-shaped precedent `WildHunterAura:88` writes it because its parameter is a `LivingEntity` (this method's collection generic is already `Mob`).
- **The immunity gate does not take part**: `TrapImmunity.passGate` (`TrapImmunity.java:42-45`) always lets non-players through, so a mob always reaches `trigger`; this version does not change it.
- ⚠️ **One pre-existing inconsistency (reported by document 41; this version does not fix it, but the acceptance must avoid it)**: `findTrigger` filters out creative / spectator / own-side players (`:51-54`), while `trigger`'s confusion loop iterates **all** of `level.players()` and **does not filter** (`:83-91`) ⇒ creative / spectator players **do not** trigger a vibration bean, yet are stunned by a vibration bean triggered by **someone else**. This is a measure that has existed since 0.2.0 and has nothing to do with the 4 requirements (D-174 keeps it out of scope), and this version changes **not one line** of it. How to handle it: registered in §9 item 13; §7 #14 / #15 always test with **survival-mode, non-own-side, non-spectator** players and mobs, and no creative account is used for the reverse assertion "does not trigger" (that test will go red, but what is red is the pre-existing measure, not a regression of this version).

#### 2.3.7 One cross-consequence that must be spelled out (vibration bean × mount)

If a mob **is being carried along by the mount branch** and is then hit by a vibration bean: per ruling #4 (all non-player mobs in range) this version **grants no exemption** — it has its AI suspended for 80 ticks, so "has a mount + does not move" lasts 80 ticks, after which the AI is restored from the original value and the mount carries on as before (`QbounceMountController.validate:307-316`'s three checks do not look at `noAi`). Ruling #7's mount exemption belongs to the Q-Bounce chain only and must not be extended sideways to the vibration bean (that would be an RT-57-style "changing the measure in passing"). This item is registered as a known consequence of RT-55, and the acceptance tests §7 #14 / #15 (vibration bean) and #18 (mount) separately, without merging them.

### 2.4 The hostility predicate (where `Enemy` ∪ `MobCategory.MONSTER` is implemented and how it is named)

**[this document's decision] (8)**: add `util/Hostility.java` (a stateless pure function, the same kind as `util/DeployedOwnership` and `util/DistanceMetrics`):

```
public static boolean isHostile(@Nullable Entity entity) {
    if (!(entity instanceof LivingEntity)) return false;
    if (entity instanceof Player) return false;                     // belt and braces: a player is neither Enemy, nor of category MONSTER
    return entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER;
}
```

Four facts (all checked in the 21.1.213 source):

1. `Enemy` is a **pure marker interface**: `net/minecraft/world/entity/monster/Enemy.java` is 11 lines in total, with only the 6 `XP_REWARD_*` constants and **no method at all** ⇒ `instanceof Enemy` is the whole test.
2. `Monster extends PathfinderMob implements Enemy` (`Monster.java:29`) ⇒ vanilla's `Monster` is **already** inside `Enemy`, and the union's increment comes only from two kinds: modded mobs whose category is `MONSTER` but which do not implement `Enemy`, and mobs that implement `Enemy` but are not in the `monster` package.
3. The category value goes through `EntityType#getCategory()` (`EntityType.java:1043`), not reflection and not `instanceof Monster`.
4. A player is never included: `Player` is neither `Enemy` nor of category `MONSTER`; the explicit line is there for the reader, not to plug a gap.

**There are only 2 use points** (D-182): `explosion/FarmstormExplosion#hurtCapped` (inside the loop at `:159`) and `behavior/impl/ZangiefCaneTrap#monsterBranch` (`:111-118`). The 5 existing `getEntitiesOfClass(Monster.class, …)` sites are **not changed back** (`BlastCarrotTrap:59`, `FangTrapCactusTrap:54`, `VibrationBeanTrap:59`, `ZangiefCaneTrap:174`, `PumpkinTurret:109`) — what they change is a trap's **trigger surface**, not its damage surface (RT-57).

### 2.5 The trap multiplier (`trap.damageMultiplier`'s type and range / `FarmstormExplosion`'s source mark / the two multiplication points)

#### 2.5.1 The key and the field

**[this document's decision] (10)**: insert into `FarmstormConfig`'s `trap` section (`:917` push ~ `:927` pop), after `trapMaxTrapsPerPlayer` (`:924-926`) and before `b.pop()` (`:927`):

```
trapDamageMultiplier = b.comment("陷阱对敌对生物的伤害倍率（1.0 = 不改；只作用于 blast_carrot 与 zangief_cane）；范围 1.0 ~ 10.0")
        .translation(tr("trap.damageMultiplier"))
        .defineInRange("damageMultiplier", 3.0, 1.0, 10.0);
```

- The type is **double** (D-183); the default is **3.0** (ruling #10 / D-183).
- **The range `1.0 ~ 10.0` ([this document's decision])**: the lower bound is 1.0 = "do not change the damage", not 0.0 — 0.0 would create a third semantics (damage reduced to zero) that is neither "×3" nor "switch the multiplier off", and with `blast_carrot`'s `min(raw, cap)` formula a result of 0 turns "a hit that deals no damage" into a silent abnormal state. The upper bound 10.0 is a threefold margin over 3.0, and `cap 7 × 10 = 70` still lies in float's exact-integer range (≤ 2^24), so readings such as 69.99999 cannot appear.
- The field declaration goes after the three `trap*` fields at `:223-225` (`public static ModConfigSpec.DoubleValue trapDamageMultiplier;`). `tr()`'s prefix is `farmstorm.configuration.` (`:1000-1002`) ⇒ the language key = **`farmstorm.configuration.trap.damageMultiplier`**.

#### 2.5.2 `FarmstormExplosion`'s source mark (the minimum)

`FarmstormExplosion` has **7** `new` sites (this document grepped the whole repository itself):

| # | Call site | Nature | ×3? |
|---|---|---|---|
| 1 | `behavior/impl/BlastCarrotTrap.java:84` | **trap** (`blast_carrot`) | **yes** |
| 2 | `entity/thrown/MelonGrenadeProjectile.java:42` | projectile (Melon Grenade) | no |
| 3 | `entity/thrown/MelonSliceProjectile.java:40` | projectile (Melon Slice Bomb) | no |
| 4 | `entity/thrown/MolotovProjectile.java:94` | projectile (Molotov) | no |
| 5 | `entity/thrown/ToxicUbiServerProjectile.java:43` | projectile (Toxic Ubi Server) | no |
| 6 | `entity/thrown/UbiServerProjectile.java:41` | projectile (Ubi Server) | no |
| 7 | `util/LuckyBerryRoller.java:212` | the Lucky Berry's small explosion | no |

**All seven use the same damage source** `farmstorm:crop_blast` (`FarmstormDamageTypes:23`, built by `cropBlast` at `:36-38`) ⇒ multiplying blindly at `:179` would make 2 ~ 7 ×3 as well.

**[this document's decision] (9)**: add a 9th constructor parameter `boolean trapSourced`, **and keep the original 8-param signature as an overload that delegates `false`**:

```
public FarmstormExplosion(ServerLevel level, @Nullable Entity sourceEntity, DamageSource damageSource,
                          double x, double y, double z, float radius, BlastRule rule, float damageCap) {
    this(level, sourceEntity, damageSource, x, y, z, radius, rule, damageCap, false);   // the 6 non-trap call sites change nothing
}

public FarmstormExplosion(…, float damageCap, boolean trapSourced) {
    super(…); this.trapSourced = trapSourced;   // the other four lines as before
}
```

Only `BlastCarrotTrap:84-86` is changed to pass `true` (one more parameter after `damageCap`). **Why not the other two routes**:

- **Do not change the damage source**: a new `farmstorm:trap_blast` would move the `damage_type` registry surface (§1.2's `:10` row, 3 → 4, 97 → 98) and the semantics of the `#minecraft:is_explosion` tag (blast protection / resistance enchantments / death messages all change with it). D-186 has already rejected "changing the fangs with a Mixin"; by the same token a new damage type must not be opened just for one multiplier.
- **Do not look the crop up from the `BlastRule`**: `BlastRule.forCrop(profile, whenDemolition)` (`BlastRule.java:78-80`) **returns only the rule and throws the profile away** — the return value carries no crop identity, and `BlastRule`'s fields are only `extraBreakable` / `maxResistance` / `dropsBlocks`. A lookup would need a "rule → crop" mapping, and different crops may produce the same rule (`NEVER_BREAK` is shared by every non-demolition crop), so the lookup would misjudge. Besides, some of the 6 non-trap call sites also go through `forCrop` (such as `MelonGrenadeProjectile`'s `BlastRule.grenade()` family), so judging by rule means judging "is it a trap" by "destructive capability" — two unrelated things.
- **Do not judge by `damageSource.getEntity()`**: that is "who threw it", not "which chain"; a player-thrown Melon Grenade and a player-deployed Blast Carrot cannot be told apart by it.

#### 2.5.3 The two multiplication points

**Multiplication point ①: `FarmstormExplosion#hurtCapped` (`:179`)**

```
double raw = (impact * impact + impact) / 2.0 * 7.0 * reach + 1.0;         // originally :178, untouched
float dealt = (float) Math.min(raw, this.damageCap);                       // the cap of :179, as before
if (this.trapSourced && Hostility.isHostile(entity)) {
    dealt *= (float) FarmstormConfig.trapDamageMultiplier.get();
}
entity.hurt(this.damageSource, dealt);                                     // originally :179
```

- **What is multiplied is the final damage after the cap**, not `damageCap` and not `radius`: `blast_carrot`'s formula is `min(raw, cap)`, and `raw` is below 7 on most hits ⇒ "multiplying the cap" has no effect at all when `raw < cap`; "multiplying `explosionPower`" would change the radius and the formula's `reach` (that is, change the trap's physical surface).
- **The hostility gate**: `entity` is a `LivingEntity` (the loop type at `:159`). Players and non-hostile mobs are not multiplied.
- **The old measure of "it blasts its own side too" is unchanged**: the comment at `:144-147` states explicitly "no friend-or-foe distinction, the user is not spared", and this version does not touch it.

**Multiplication point ②: `ZangiefCaneTrap#monsterBranch` (`:111-118`)**

```
double amount = FarmstormConfig.zangiefCaneSlamDamage.get();               // originally :111, default 7.5
if (Hostility.isHostile(target)) {
    amount *= FarmstormConfig.trapDamageMultiplier.get();
}
DamageSource source = FarmstormDamageTypes.zangiefSlam(level, deployer);   // originally :112
…
} else if (amount > 0.0) {
    target.hurt(source, (float) amount);                                   // originally :118
}
```

- Putting the multiplication **before** the two tests `source == null` / `amount > 0`: with `slamDamage` configured to 0, the product is still 0 and the behaviour is unchanged.
- **The existing INFO log (`:126-128`) prints `amount`** ⇒ it must print after the multiplication, so that the acceptance reading (§7 #19) matches what `hurt` actually received. The log format string is not changed; only the value becomes the multiplied one.
- **The player branch is untouched**: the damage of `ZangiefPiledriverController:303`/`:311` is outside the multiplier surface (D-184).

**The effective surface (2 of the 4 `TRAP` crops take part, RT-58)**: `blast_carrot` ✅ / `zangief_cane` ✅ / `fang_trap_cactus` ❌ (the damage is inside vanilla's `EvokerFangs`, `FangTrapCactusTrap:87-109`, with 0 `hurt` in the repository) / `vibration_bean` ❌ (0 damage). Any sentence that says "all traps ×3" is wrong.

---

## 3. Master table of the action surface (additions / changes / deletions against 0.2.0, 31 rows line by line)

Column notes: **Kind** = release / class / comment / data / config / language / tool / acceptance / unchanged. **Current** = the actual content of the released 0.2.0 state; **After** = v0.2.1's target state. Decision numbers always cite document 40, risk numbers always cite `BRIEF` §4.

| # | Kind | Path or class#method | Current | After | Decision | Risk |
|---|---|---|---|---|---|---|
| 1 | release | `gradle.properties:11` | `mod_version=0.2.0` | **`0.2.1`** | D-173 | — |
| 2 | release | `一键构建.cmd:5` | the header `Farmstorm v0.2.0` | **`v0.2.1`** | D-173 | — |
| 3 | release | `一键构建.cmd:11,12` | the jar / sources-jar names each contain `0.2.0` | **`0.2.1`** (two places) | D-173 | — |
| 4 | release | `检查环境.cmd:5` | the header `Farmstorm v0.2.0` | **`v0.2.1`** | D-173 | — |
| 5 | release | `检查环境.cmd:14` | `farmstorm-0.2.0+mc1.21.1.jar` | **`0.2.1`** | D-173 | — |
| 6 | class | `blockentity/OutpostCoreBlockEntity#serverTick` (`:507-525`) | three things (early return → particle gate → particles + a log every 30 rounds) | **four things**: `now` moves up one line → the rebind gate (a standalone `if`, no return) → the particle gate → particles + the log | D-176 | RT-51, RT-53 |
| 7 | class | `OutpostCoreBlockEntity` constant area (next to `:91`) | none | **new** `REBIND_INTERVAL_TICKS = 100` | D-176 | RT-52 |
| 8 | class | `OutpostCoreBlockEntity#rebindDue(BlockPos, long)` | does not exist | **new** (an independent phase formula, modulus 100) | D-176 | RT-51 |
| 9 | class | `OutpostCoreBlockEntity#rebindNearby(ServerLevel)` | does not exist | **new** (two phases: collect → write the bindings; `break` at `cap`) | D-177 | RT-53 |
| 10 | class | `util/AreaBlockEntities.java` | does not exist | **new** (`collect(ServerLevel, BlockPos, int, Class<T>)`, going only through `getChunkNow`) | D-177 | RT-52 |
| 11 | comment | `block/CultivatorBlock.java:81-83` | "**does not claim automatically**……placing a core next to it afterwards **will not bind it** either" | **must change**: "every 100 ticks at runtime, rebind the unbound ones (D-176)" | D-176 | RT-51 |
| 12 | class | `client/OutpostCoreScreen.java` (42 lines) | overrides `renderBg` only (`:37-41`) | **not one line changed** (criteria and fallback in §2.2) | D-178 | RT-59 |
| 13 | acceptance | §7 of this document | tooltip / hovering: **0 items** | **+3 items** (#11 ~ #13) | D-178 | RT-59 |
| 14 | class | `behavior/impl/VibrationBeanTrap#trigger` (`:74-105`) | `CONFUSION` to the players inside the sphere only (`:83-91`), 0 effect on mobs | **append**: suspend the AI of every non-player mob inside the sphere for **80 ticks** (recording the original value) | D-180 | RT-54, RT-56 |
| 15 | class | `VibrationBeanTrap` constant area | none | **new** `AI_SUSPEND_TICKS = 80` (no new config key) | D-180 | RT-56 |
| 16 | class | `effect/QbounceEffect` (20 lines, 0 overrides) | a pure state carrier, `applyEffectTick` is an empty implementation | **override `onEffectAdded(LivingEntity, int)`**, one method, forwarding to the AI table (**do not** override `applyEffectTick`) | D-181 | RT-55, RT-56 |
| 17 | class | `behavior/QbounceMountController#isMountMode` (`:199`) | `private static` | **`public static`** (the same implementation, no second method added) | D-181 | RT-55 |
| 18 | class | `combat/MobAiSuspension.java` | does not exist | **new** (`sources` bit mask + observation index + in-memory deadline + the five cleanup paths + per-tick reconciliation) | D-179 | RT-54 |
| 19 | class | `registry/FarmstormAttachments.java` | does not exist | **new** (`AttachmentType<Suspend>`, `farmstorm:ai_suspend`, registered in `ATTACHMENT_TYPES`) | D-179 | RT-54 |
| 20 | class | `util/Hostility.java` | does not exist | **new** (`isHostile(Entity)`, `Enemy` ∪ `MONSTER`) | D-182 | RT-57 |
| 21 | class | `explosion/FarmstormExplosion` constructor (`:69-78`) | 8 params | **9 params** (+`boolean trapSourced`), **the 8-param overload is kept and delegates `false`** | D-184 | RT-57, RT-58 |
| 22 | class | `FarmstormExplosion#hurtCapped` (`:178-179`) | `entity.hurt(src, min(raw, cap))` | × the multiplier when hostile ∧ `trapSourced` | D-184 | RT-58 |
| 23 | class | `ZangiefCaneTrap#monsterBranch` (`:111-118`, log `:126-128`) | `hurt(src, amount)`, the log prints the original value | × the multiplier when hostile, **the log prints the multiplied value** | D-184 | RT-58 |
| 24 | config | `config/FarmstormConfig.java` (the `trap` section `:917-927`) | 3 keys / 60 groups / 175 keys | **4 keys** (+`damageMultiplier` 3.0, 1.0~10.0) / 60 groups / **176 keys** | D-183, D-185 | RT-60, RT-60b |
| 25 | language | `datagen/ModLangProvider.java` (after `:412-413`) | 178 `config(entries, …)` / 337 keys | **+1 entry** (`trap.damageMultiplier`) / **338 keys × 2** | D-185 | RT-60 |
| 26 | tool | `tools/verify_project.py` (`main()` `:349-402`) | the expected key set = the fixed sections of 6 documents; no document-42 parsing | **new** `expected_config_keys_v021` (reads §4.1.1 of this document, 1 row) + expect 176 | D-185 | RT-60, RT-60b |
| 27 | unchanged | `Farmstorm.java:167-172` (the 12 class assertions of 97) / `network/FarmstormNetwork.java` (2 packets, protocol `"2"`) | 97 / 2 packets | **as is** (this version is +0) | D-175, D-186 | — |
| 28 | unchanged | `FangTrapCactusTrap:87-109` (vanilla `EvokerFangs`) / `vibration_bean` (0 damage) / the 5 `Monster` filters | as is | **as is** (the fangs and the vibration bean do not take part in ×3; the trigger surface is untouched) | D-184, D-182 | RT-58, RT-57 |
| 29 | class | `Farmstorm.java` constructor (the `.register(modBus)` area of the 12 registers) | 12 registers | **+1 line** (`FarmstormAttachments.REGISTER.register(modBus)`); the 12 class assertions at `:167-172` **change not one line** | D-179, D-175 | RT-54, RT-60 |
| 30 | class | the **pre-gates and the sort** of `OutpostCoreBlockEntity#rebindNearby` | do not exist | **+ the ownerless-core gate** (`ownerUuid == null` returns at once, ruling #13) + **the candidate sort** (`distSqr` → `x` → `y` → `z`, ruling #14) | D-177 | RT-51, RT-53 |
| 31 | data | the vibration bean's 80-tick deadline | none | **in memory only** (`VB_DEADLINE`), **not persisted** (ruling #16: the window does not cross a save) | D-180 | RT-54 |

**The 31 rows' kind counts**: release **5** (#1~5) + class **18** (#6~10, 12, 14~23, 29, 30) + comment **1** (#11) + acceptance **1** (#13) + data **1** (#31) + config **1** (#24) + language **1** (#25) + tool **1** (#26) + unchanged **2** (#27, #28) = **31**.

**Where each of `BRIEF` §3.4's 12 mandatory changes lands**: ① `gradle.properties` → #1; ② the two `.cmd` files' jar names + header lines → #2~#5 (**4 rows**; the header lines were already listed in `BRIEF`'s own text, see §9 item 1); ③ the rebind in `serverTick` → #6~#9; ④ the area enumeration utility → #10; ⑤ the tooltip → #12, #13; ⑥ the vibration bean → #14, #15; ⑦ `QbounceEffect` → #16 (+ #18 carries the restore criteria); ⑧ the two multiplication points + the source mark → #21~#23; ⑨ the hostility utility → #20; ⑩ `verify_project.py` → #26; ⑪ the `CultivatorBlock` class comment → #11; ⑫ the visibility of `isMountMode` → #17. **All 12 items land, and not one of them lands on an "unchanged" row**.

**The 4 items that `BRIEF` §3.4 does not list but this version must do**: #18, #19, #29 (the carrier of the original AI flag, its registry class, and that one registration line in the main class) and #31 (the deadline is not persisted). They are not among the 12 because none of them lands on a line of an existing file — #19 / #29 are the direct product of rulings #15 / #17, and #31 is the direct product of ruling #16.

---

## 4. The full config table (only the groups that change + the total check)

### 4.1 The changes, key by key

#### 4.1.1 Additions / changes / deletions (**1 row**: +1 / −0 / changed 0)

| # | Full key path | Type | Default | Range | Action | Decision |
|---|---|---|---|---|---|---|
| 1 | `trap.damageMultiplier` | double | **3.0** | **1.0–10.0** | **add** | D-183 |

(The `trap` group **3 → 4** keys; the group count is unchanged ⇒ **175 + 1 = 176 keys / 60 groups**. This version deletes **no** key and changes **no** existing default.)

**The parsing contract ([this document's decision] (12), written for the next maintainer of `tools/verify_project.py`)**: the table in §4.1.1 of this document has **the same format** as §4.1.1 of `36-TECH-0.2.0.md` (7 columns: number / the `group.key` inside backticks / type / default / range / action / decision). The new `expected_config_keys_v021` must:

1. **Bound the range** with `^#### 4\.1\.1\b` and the following `^#### 4\.1\.2\b` (it must not scan the whole text for `^\|\s*\d+\s*\|` — the tables in §3 and §7 of this document also start with `| 1 |`);
2. require exactly **7** columns row by row, column 2 matching `` ^`([A-Za-z0-9_]+\.[A-Za-z0-9_.]+)`$ ``, column 6 starting with the add / delete marker (「增」 / 「删」 in the Chinese original, rendered as **add** / **delete** in this translation);
3. assert the row count **== 1** (1 addition / 0 deletions), the same shape as `expected_config_keys_v012`'s 11-row assertion (`verify_project.py:172-178`);
4. merge the result into `main()`'s `union` (`:382-391`), and change the expected number from 175 to 176.

**⚠️ Writing the document without changing the parser = the false green stays**: the current `verify_project.py` reads only the fixed sections of 6 fixed documents (`main():349-355`'s `design` / `tech` / `tech3` / `tech4` / `tech5` / `tech6`), and **it does not read this document today**. So "writing `trap.damageMultiplier` into the document" is a **necessary but not sufficient** condition — the two must be in the same commit (§7 #22 is the acceptance of exactly this).

#### 4.1.2 The reasons for the range and the field name

| Item | Value | Reason |
|---|---|---|
| Field name | `trapDamageMultiplier` | the same naming as the three fields of the same section, `trapImmunityTicks` / `trapDustIntervalTicks` / `trapMaxTrapsPerPlayer` (`:223-225`): `group name + the key name with its first letter capitalised` |
| Type | `ModConfigSpec.DoubleValue` | D-183 fixes double; the three in the same section are all `IntValue`, and this is the only one that takes the `defineInRange(String, double, double, double)` overload |
| Lower bound | **1.0** | 1.0 = "do not change the damage". Taking 0.0 would produce a third state (a hit that deals no damage), and `blast_carrot`'s `min(raw, cap)` behaves as a silent abnormality at a 0 multiplier |
| Upper bound | **10.0** | a threefold margin over 3.0; `cap 7 × 10 = 70` lies inside float's exact-integer range (≤ 2^24), so the acceptance reading cannot show 69.99999 |
| Default | **3.0** | ruling #10 / D-183; this document does not change it |
| Translation key | `farmstorm.configuration.trap.damageMultiplier` | `tr()`'s prefix is `farmstorm.configuration.` (`FarmstormConfig:1000-1002`) |
| On-disk key | `damageMultiplier` in the `[trap]` section | the same section as the three existing keys of `trap`; `b.push("trap")` (`:917`) / `b.pop()` (`:927`) are untouched |

**Three things that do not change**: `blast_carrot.maxDamage` (7), `zangief_cane.slamDamage` (7.5), and the default values of the 3 existing keys of the `trap` section — the multiplier is a **new key** and is applied at the settlement point (ruling #10).

### 4.2 The total check (176 / 60)

| Item | 0.2.0 | This version | v0.2.1 | Method |
|---|---|---|---|---|
| server config keys | **175** | **+1** | **176** | line-initial `.define*` count: `175 + 1 = 176` (§1.3's method, re-checked key by key) |
| server config groups | **60** | **+0** | **60** | `b.push` count 60; `trap` is an existing group, and only a key is added, no group is built |
| client config keys | 3 | +0 | **3** | `hud.enabled/x/y`, not counted in 176 |
| `.define` line total (comments included) | 177 | +1 | **178** | 175 live + 1 comment (`:935`) + 1 mention (`:229`), 176 + 2 after the change |

**Three independent checks**: ① the line-initial `.define*` in the source = 176; ② the keys of a regenerated `run/config/farmstorm-server.toml` = 176 (⚠️ the copy on disk right now is a leftover of an old version, see §9 item 2; run `runServer` once to let it rewrite before verifying); ③ the union of `verify_project.py` = 176 (the parser must be added per §4.1.1's contract first).

**Why the group count is still 60**: the `trap` group has existed for a long time (`b.push("trap")` at `:917`), and this version only adds one key inside the group ⇒ the split between "top-level groups / nested groups" does not move at all, and the number of `[section]` lines in the on-disk toml does not change either.

### 4.3 Language-key additions and deletions (+1 → 338)

**Currently 337 × 2** (the method is in §1.3). This version is **+1** ⇒ **338 × 2**.

| # | Key | Language | Position | Text ([this document's decision]) |
|---|---|---|---|---|
| 1 | `farmstorm.configuration.trap.damageMultiplier` | zh_cn | after `ModLangProvider`'s `:412-413` | 「陷阱对敌对生物的伤害倍率（1.0 = 不改；只作用于 blast_carrot 与 zangief_cane）」 |
| 2 | as above | en_us | the fourth argument of the same line | `"Damage multiplier of traps against hostile mobs (1.0 = unchanged; blast_carrot and zangief_cane only)"` |

**How to count it (reproducible)**:

```
ConvertFrom-Json key sets on both sides: zh = 338, en = 338, zh-only = 0, en-only = 0
farmstorm.configuration.* : 179 (= 176 server + 3 client)
the other prefixes unchanged (tooltip.farmstorm still 20, advancements 36, item 32, block 26 …… adding up to 338)
ModLangProvider's config(entries, …) count: 178 → 179
```

**Two disciplines**: ① Chinese and English **same key, same count** (0.2.0's baseline is 337 × 2, with zh-only 0 / en-only 0); adding only the Chinese one would turn 338 × 2 into 338 / 337; ② the language keys are **not generated automatically** — `ModLangProvider` writes `config(entries, "group.key", Chinese, English)` by hand entry by entry (178 of them now), so "adding a config key" does not grow a translation by itself and this 1 line must be written by hand.

---

## 5. Save and compatibility

Five questions answered one by one (each gives "is it persisted" and "what a new process sees").

### 5.1 Is the original AI flag persisted?

**Yes, in the entity's own attachment (ruling #15), and it is deleted on restore.** Four layers of fact:

1. Where it is persisted: `AttachmentType<Suspend>` (`farmstorm:ai_suspend`), serialised into the entity's `neoforge:attachments` (the key name at `AttachmentHolder.java:27`, written at `:122-145`, read back at `:151-172`); `Entity extends AttachmentHolder` (`Entity.java:131`). **No `SavedData` is added**, no `.dat` is written, and it does not enter `getUpdateTag` (the client cannot see it).
2. Lifetime: it lives only "for the stretch in which the AI is suspended". The only exit, `release()`, ends with `removeData(AI_SUSPEND)` (`AttachmentHolder.java:107`) ⇒ on the normal path **not one byte is left in the save**; and `NONE` (`sources == 0`) is not written in the first place (`serialize(codec, s -> s.sources() != 0)`).
3. Server stop: `ServerStoppingEvent` performs one **best-effort give-back** (restore + delete the attachment) to leave the save clean; but it is **not the source of correctness** — whatever is missed because the chunk is already unloaded is caught by the attachment on the next load (§2.3.3 path ⑤).
4. It is left behind only on an abnormal shutdown: then, at the next server start, `EntityJoinLevelEvent` reads `sources != 0` and restores by the criterion "both sources have ended" (§2.3.3's four criteria). This is exactly the slot that `Map<UUID, Boolean>` cannot solve, and it is this version's **only** new persistence surface.

**Why not `SavedData`**: `SavedData` is a "per-dimension / global" save object, whereas the key here is an entity UUID and it must be **copied and deleted together with the entity** (when an entity is `/kill`ed or its chunk is unloaded for good, the entry in `SavedData` becomes garbage that is never cleaned up). The three existing `SavedData`s (`FarmlandLedger` / `OutpostCoreIndex` / `TrapFifoIndex`) are all "coordinate index" data of the **cross-entity, needs active expiry** kind, and are not isomorphic to this purpose.

### 5.2 How Q-Bounce's / the vibration bean's temporary state is handled on a server stop

| State | Carrier | Handling on a server stop | What a new process sees |
|---|---|---|---|
| the Q-Bounce effect itself | entity NBT (vanilla `active_effects`) | **untouched** (it is vanilla state, and this version adds no marker) | the effect is still there ⇒ if that entity's attachment contains `SRC_Q_BOUNCE`, it is caught on load by "the effect is still there ⇒ stay suspended" (§2.3.3 criterion 2); **if the attachment does not exist** (for example it already carried the effect before the restart and this version never took it over), then **the AI is not suspended** — this version only takes over entities **it suspended itself** |
| the vibration bean's 80 ticks | **memory** (`VB_DEADLINE`) — ruling #16: not persisted | best-effort settlement (restore from the original value + delete the source bit where it can be written); where it cannot, the attachment stays | on load, `adopt`'s criterion 1: always treated as expired ⇒ restore from the original value (**an early restore is harmless**) |
| the mount ledger `ACTIVE` | memory (`QbounceMountController:73`) | `releaseAll` (`:157` / `:164`) discards the mounts + clears the table | an empty table + no mount residue in the save |
| the AI observation index `OBSERVED` | memory (`MobAiSuspension`) | cleared (the original value is in the attachment, the index may be dropped freely) | an empty index + entities with an attachment re-enter the index automatically on load |

**One trade-off that must be spelled out**: **only an entity that has had `SRC_Q_BOUNCE` in its attachment is still suspended by this version after a restart**. An entity that "already carried Q-Bounce before the restart but was never taken over by this version" (for example a chives field still hovering in a 0.2.0 world) does not have its AI suspended — "remaining duration → should it be held" is not a decidable criterion, and forcing an inference from the effect would wrongly release an entity that was "already `noAi` before the entry" (exactly the kind ruling #8 wants to avoid). This boundary is registered in RT-55.

### 5.3 No new `SavedData` (the three existing ones named one by one, all 0 changes)

| # | Class | On-disk file | This version |
|---|---|---|---|
| 1 | `farmland/FarmlandLedger` | `farmstorm_farmland.dat` | 0 changes (0 runtime callers, already so in 0.2.0) |
| 2 | `outpost/OutpostCoreIndex` | `farmstorm_outpost_core.dat` | **0 changes** — the rebind writes no index: `setOwner` is the only `record` call site (`:410-419`), and the rebind does not touch the "player → core position" mapping |
| 3 | `trap/TrapFifoIndex` | the trap FIFO index | 0 changes |

**The core BE's NBT**: the `Cultivators` key (`:72` / `:757-761` / `:774-780`) keeps its **format** (a `ListTag<LongTag>`, each entry a `BlockPos.asLong()`). The rebind only `add`s entries to the same list, with **no new key, no type change and no version bump** ⇒ an old save can be read directly, and an old version can read a new save too (it just cannot see the extra bindings and will run the lazy cleanup of `pruneCultivators`, `:380-391`). **No migration marker such as `DataVersion` is added.**

### 5.4 Old-save compatibility

| Scenario | This version's behaviour |
|---|---|
| an entity in an old save with `NoAI:1b` (locked by another mod / a command) | **not touched**. It has no `farmstorm:ai_suspend` attachment ⇒ it does not enter the index, does not enter `adopt`, and is never "restored to having AI" |
| a core in an old save (`Cultivators` empty or ≤ 16 entries) | the rebind starts on the next round (≤ 100 ticks), with no migration action needed |
| an old save that already has 17+ `Cultivators` entries (theoretically impossible; 0.2.0's `addCultivator` already stops it) | the rebind's `if (cultivatorCount() >= cap) return;` skips it outright and **prunes nothing** (only add, never remove) |
| a `Core` cache in an old save pointing at a dead core (left by migration exit ②) | the rebind skips it (`corePos() != null`), consistent with 0.2.0's `isUsable()` criterion |

### 5.5 "The little bit extra" on the save surface

The only increment: every entity that **is currently suspended** carries one compound of ≤ 40 bytes in `neoforge:attachments` (not written while `sources == 0`), deleted on restore. **No new file, no new `.dat`, no new `DataVersion`, no change to the chunk format, no new `SavedData`.**

---

## 6. Risk register RT-51 ~ RT-60 (each: risk / trigger / impact / mitigation / which acceptance covers it)

| Id | Risk | Trigger | Impact | Mitigation | Which acceptance items cover it |
|---|---|---|---|---|---|
| **RT-51** | the periodic rebind coexists with "binding at placement", and the same cultivator may be claimed by two cores at once | ① the rebind sweep and a placement write on the same tick; ② someone writes the rebind as "write the cultivator side first, then the core side"; ③ someone calls `removeCultivator` to "fix a disagreement" | one cultivator enters two cores' lists ⇒ violating 0.2.0 D-165's "one-to-one" invariant; migration / capacity / links are all distorted at once | (a) the write order is pinned to "core first, cultivator second" (`setCore` only after `addCultivator` succeeds); (b) the candidate criterion is `corePos() != null ⇒ skip`, so a later arrival sees the earlier one by reading the cache; (c) the rebind does **not enable** `removeCultivator` (§2.1.3 item 3), and a half-written state is always skipped rather than "bound first and removed afterwards"; (d) the class comment at `CultivatorBlock:81-83` is changed with it (#11), leaving no "the comment says no, the code does it anyway" | §7 #6 ~ #10 |
| **RT-52** | the area enumeration has no precedent: the return semantics of `LevelChunk#getBlockEntities()`, and unloaded chunks must be skipped | ① using `Level#getChunk` / `getBlockEntity` instead of `getChunkNow` ⇒ triggering a chunk load; ② iterating the live map of `chunk.getBlockEntities()` directly ⇒ `ConcurrentModificationException`; ③ taking "not loaded" as "no cultivator" when logging / recording state; ④ conversely believing "in the normal case it sweeps hardly any chunks" (the fact is that in the normal 3×3 case everything is loaded, see the 📌 in §2.1.2) | ① a core pulls 9 chunks into memory every 100 ticks; ② the server tick throws; ③ log noise + misjudgement; ④ the acceptance is written as "low frequency" ⇒ "rebound within 100 ticks" is misjudged as occasional | (a) go only through `ServerChunkCache#getChunkNow`, and skip when it cannot be obtained (the existing convention of `OutpostCoreBlockEntity:296-300` / `:384-387`); (b) return a **new `ArrayList`** (two phases, the form of `QbounceMountController:213`); (c) "skipping" writes no state and logs nothing; (d) the grounds for "normally fully loaded" are written into §2.1.2: the core's own tickets have `distance = 1` (`CoreChunkTickets.java:17-18`, `:43-44`) ⇒ the centre chunk is entity-ticking and the 8 surrounding chunks are FULL | §7 #6, #9 |
| **RT-53** | a core's cap of 16: the rebind must stop once full; `pruneCultivators` and the rebind running at the same time may "remove what was just bound" | ① the loop does not `break`; ② the rebind calls `cultivators()` (which contains `pruneCultivators`) in passing; ③ capacity is read from something other than `cultivatorCount()` | ① once full it still runs through dozens of candidates for nothing; ② `pruneCultivators`'s `removeIf` changes the list and `setChanged`, and interleaving it with the rebind makes "the coordinates bound this round" counted twice or missed; ③ the capacity and the `isCultivatorListFull` criterion disagree | (a) `if (cultivatorCount() >= cap) break;` re-read after every binding; (b) read only `cultivatorCount()` / `isCultivatorListFull()` (`:334` / `:343`), **do not call** `cultivators()`; (c) `pruneCultivators` / `removeCultivator` must not be called on the rebind path | §7 #10 |
| **RT-54** | the **residue risk after the original AI flag is persisted**: miss one of the five cleanup paths and the save keeps an "expired suspension record", and the next load touches an entity it should not touch (ruling #15's new shape; the old shape, "the table is lost on a server stop ⇒ permanently no AI", has been removed by the attachment) | ① forgetting `removeData` after the restore; ② treating "not observable (chunk unloaded)" as "the entity is gone" and deleting the attachment; ③ `setNoAi` before the attachment write (a crash in between); ④ missing the death / unload path among the five; ⑤ "always restore to having AI" | ① the attachment never disappears ⇒ every load runs for nothing, and `originalNoAi` becomes ever less trustworthy; ② the original value is lost + the entity is persisted with `NoAI:1b` ⇒ permanently no AI (old RT-54's consequence returns); ③ as above; ④ a dead entity's attachment is copied elsewhere (already blocked by `copyOnDeath` defaulting to false, `AttachmentType.java:43`); ⑤ an entity that "had no AI to begin with" is released | (a) `release()` is the only write exit and always ends with `removeData` (§2.3.2's two code blocks); (b) the write order is pinned to "read the original value → write the attachment → `setNoAi(true)`"; (c) "not observable" **drops the index only and keeps the attachment**, 1200-tick upper bound (§2.3.4); (d) the attachment can express two sources at once, so the restore criterion is "both sources have ended"; (e) a time-went-backwards guard; (f) being caught on load (path ⑤) means "the round missed on a server stop" is no longer permanent damage | §7 #23, #24, #28 |
| **RT-55** | the Q-Bounce effect's lifetime: the effect is `/effect clear`ed, cured by milk, or expires, **and also death and a chunk unload** — all five paths must clean up | ① using a one-shot timer; ② judging only inside `applyEffectTick`; ③ the mounted events miss one path; ④ a chunk unload is misjudged as death; ⑤ the vibration bean's suspension and a mount carry hit on the same tick | ① the per-tick rescan resets the timer for ever ⇒ it never restores; or conversely it restores early while the effect is still there; ② `QbounceEffect` has 0 overrides today and `applyEffectTick` is an empty implementation (`:8`) ⇒ it is never called at all; ③ the attachment is left behind on some path; ④ the original value is lost; ⑤ "has a mount + does not move" for 80 ticks (§2.3.7; this version **grants no exemption, per ruling #4**, and registers it as a known consequence) | (a) the restore criterion = a live reading of `hasEffect` (read every tick by the reconciliation, not one removal event mounted); (b) the common convergence point of the three automatic removal paths is checked: expiry `LivingEntity.java:790`, `/effect clear` `:920`, milk `:3524` (`MilkBucketItem.java:27` → `removeEffectsCuredBy`); (c) death and unload take paths ④ / ⑤ respectively, and an unload **does not delete the attachment**; (d) the reconciliation is authoritative, and the event surface has **zero dependencies** | §7 #17, #18, #28 |
| **RT-56** | Q-Bounce's effect is "applied repeatedly, with a decreasing duration", so the AI suspension's duration cannot use a one-shot timer | `FriendChivesProjectile` reads the field duration on landing (`:82`) and re-scans every tick writing "field duration − elapsed ticks" (`:155-172`) | the same as above (RT-55's ①) | (a) the entry side mounts `MobEffect#onEffectAdded` — in 21.1.213 it is called **only in the branch where "there was no such effect before"** (`LivingEntity.java:957-961`), and a rescan refresh goes through `:962`'s `update` without triggering it ⇒ that bit of `sources` is never set twice (one source occupies one bit); (b) the exit side looks only at `hasEffect`; (c) **no** constant of the "restore after N ticks" kind is set | §7 #16, #17 |
| **RT-57** | ×3 applies to hostile mobs only: none of the 5 existing mob-handling sites distinguishes friend from foe, and the new judgement must not be changed onto another path in passing | ① replacing the 5 `getEntitiesOfClass(Monster.class, …)` sites with the new predicate; ② multiplying uniformly at the damage-event layer; ③ using `Hostility` on the trigger surface | ① the trap's **trigger surface** is widened (hostile mobs that are not `Monster` start triggering traps) ⇒ a large behavioural change, conflicting with D-174's "do not change what 0.2.0 already had"; ② non-trap damage is affected; ③ as above | (a) `Hostility`'s **use points are only 2** (`FarmstormExplosion`, `ZangiefCaneTrap#monsterBranch`), written into §2.4 and §3 #22/#23; (b) the 5 filter sites are listed on §3 #28's "unchanged" row; (c) no multiplication is mounted at any event layer | §7 #19, #20, #27 |
| **RT-58** | the fangs do not take part: an acceptance written as "all traps" must misjudge | the 4 `TRAP` crops written as "all ×3" | with the acceptance written as "all", the fangs (vanilla damage) and the vibration bean (0 damage) can never measure ×3, and the acceptance must go red; or the implementer changes vanilla's fangs "to make the acceptance pass" (stepping on the red line) | (a) the four-item named table at the end of §2.5.3 (2 yes / 2 no); (b) §3 #28's "unchanged" row; (c) §7 splits it into #19 (the two that really do ×3) and #21 (the fangs unchanged), two independent acceptance items | §7 #19, #21 |
| **RT-59** | the tooltip may already be fine: the task then becomes "add an acceptance", which is easily written up as "a pile of changes" | the implementer does not believe "the default chain works", so rewrites `render` to draw the tooltip itself / adds a `Slot` subclass | a 42-line class becomes a hand-drawn screen that bypasses vanilla's handling of positioning / scaling / background / delayed display (already rejected by D-178) | (a) §2.2's four pieces of evidence + the five ways of breaking it, each with "current state 0"; (b) §3 #12's "not one line changed" row; (c) the fallback is written as "add ≤ 2 lines only if the acceptance fails", with the concrete patch given | §7 #11 ~ #13 |
| **RT-60** | `verify_project.py`'s expected surface (A-66): config 175→176 and lang 337→338 out of step means **silently checking less** | ① changing only the source, not the document; ② changing only the document, not the parser; ③ believing "if the document says it, the tool will check it" | the tool stays green, and one config key too many or too few cannot be detected (A-66's false green) | (a) §4.1.1 of this document pins the contract with a **parseable 7-column table** (the four items of §4.1.1's "parsing contract"); (b) §3 #26 lists "add the parser" as a mandatory change; (c) the expected number 176 and the source's 176 are checked both ways by §7 #2; (d) the extraction logic itself is **not fixed** (a pre-existing 0.2.0 problem, kept out of scope by D-174) | §7 #2, #22 |
| **RT-60b** | `trap.damageMultiplier` must **really be written into the document**: the expected key set is extracted from the document, and a key missing there is a key not added | the document only says "a new multiplier key" without the full key path, or writes it in prose rather than in a table | "a config key too many / a key missing from the document", silently in both directions (the same mechanism as RT-60; listed separately because it is this version's **only** new key) | (a) the key path appears in backticks in column 2 of §4.1.1's table (so a regex can hit it); (b) §4.1.2 names the field name and the on-disk key once more; (c) §7 #22 verifies specifically that "the key really is in the document" | §7 #22 |

**The eleven items' common trait** (written for documents 43 / 44): every one of them mitigates with "**one definition + one comment + one independent acceptance item**", and not one of them relies on "remembering to be careful". RT-51 / RT-53's landing points are concrete methods (`rebindNearby`'s three criteria), RT-55 / RT-56's landing point is a concrete criterion (`hasEffect` rather than a timer), and RT-60 / RT-60b's landing point is **the table in §4.1.1 of this document itself**.

---

## 7. Acceptance self-check list (31 items)

Each gives: number / the assertion verbatim / where to observe / the grounds. **"Where to observe" must be actionable**: `V-nn` means an observation point this document defines; no second numbering series is introduced.

| # | The assertion verbatim | Where to observe | Grounds |
|---|---|---|---|
| 1 | the startup log shows `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK`; and the `src/main/java` Java file count = **148** | `run/logs/latest.log` (the format string at `Farmstorm.java:173`); a file count | §1.1, §1.2 |
| 2 | section 2 of `verify_project.py` prints `已核 registry 对象: 97 / 97`; section 1 prints `文档口径键数: 176` and `生成 toml 键数: 176` | run `tools/verify_project.py` with the python under `.tools` | §4.2, D-185 |
| 3 | across the repository `@Mixin` = **0** and `*.mixins.json` under `src/` = **0**; `FarmstormNetwork`'s `.playToClient` is exactly **2** places (`farmstorm:core_list` / `farmstorm:cultivator_link`), `PROTOCOL_VERSION = "2"` | a source grep + `FarmstormNetwork.java:25,38,43` | D-186, §1.4 |
| 4 | the `trap` section in `run/config/farmstorm-server.toml` has **4** keys (`immunityTicks` / `dustIntervalTicks` / `maxTrapsPerPlayer` / **`damageMultiplier = 3.0`**), **60** sections, **176** keys in total (run `runServer` once to regenerate the toml before counting) | `run/config/farmstorm-server.toml` | §4.1, §4.2 |
| 5 | the language files: `zh=338` / `en=338` / zh-only **0** / en-only **0**; `farmstorm.configuration.*` = **179** (= 176 + 3) | `zh_cn.json` / `en_us.json`, the key set after `ConvertFrom-Json` | §4.3, D-185 |
| 6 | the core has 0 cultivators bound, **1** unbound cultivator is placed inside the 3×3, `boundaryParticleIntervalTicks = 40` (the default) ⇒ that cultivator appears in `Cultivators` **within 100 ticks** (not 200) | `/data get block <core> Cultivators`; a timestamp (`V-01`) | §2.1.1, D-176 |
| 7 | **the same scenario with `boundaryParticleIntervalTicks` changed to 1200** ⇒ it is still rebound **within 100 ticks**. This is the decisive acceptance that the rebind **is not** filtered out by the particle gate: had the particle gate been reused, this would have to wait 1200 ticks | as above (`V-02`) | §2.1.1, RT-51 |
| 8 | phase staggering: **2** cores in the same 3×3, each with 1 unbound cultivator ⇒ the two cores' rebinds happen on **different ticks** (the `gameTime` in the log differs by ≠ 0) | the rebind INFO line (`V-03`) | §2.1.1, ruling #2 |
| 9 | the unloaded-chunk skip: put a cultivator into a chunk at the edge of the 3×3 and let it unload (walk away / `forceload remove`) ⇒ it is not rebound this round; the server log has **no** load record for that chunk, and `ChunkMap` does not grow because of the rebind | the chunk-load lines of `latest.log` (`V-04`) | §2.1.2, RT-52 |
| 10 | stop when full: **20** unbound cultivators inside the 3×3, `cultivatorCapacity = 16` ⇒ `Cultivators` has exactly **16** entries and **does not grow further**; after 300 more ticks it is still 16; the 4 new ones are not taken in, and **not one of the 16 existing entries changes its order or coordinates** | `/data get block <core> Cultivators` (`V-05`) | §2.1.3, RT-53 |
| 11 | hovering the item in **any one** of the 54 slots (slots 1 / 28 / 54 taken as samples) ⇒ a vanilla tooltip appears: the item name + detail lines (≥ 2 lines), and a count of 1 shows no "1" | in game (`V-06`) | §2.2, D-178 |
| 12 | hovering an **empty slot** ⇒ no tooltip; hovering a **player-inventory slot** ⇒ a tooltip likewise | in game (`V-06`) | §2.2 |
| 13 | the tooltip chain is not bypassed: `OutpostCoreScreen`'s `render` / `renderTooltip` / `renderLabels` / `init` override count = **0**; across the repository `renderTooltip` hits = **0**; `new Slot(` inside `OutpostCoreMenu` = 54 + 36 times, with no `Slot` subclass | a source grep | §2.2, RT-59 |
| 14 | the vibration bean: 1 zombie in the radius (`NoAI:0b`) ⇒ after the trigger `NoAI:1b`; **80 ticks later** `NoAI` is back to **0b**; the 2nd and 3rd mobs inside the radius on the same tick **also** have their AI suspended (the triggerer is not the only one). ⚠️ Trigger with a **survival-mode, non-own-side** player (pre-existing measure: creative / spectator players do not trigger but are stunned, §2.3.6's last item, §9 item 13) | `/data get entity <mob> NoAI` (`V-07`) | §2.3.6, D-180 |
| 15 | the original-value semantics: first use `/data merge entity <mob> {NoAI:1b}` to make a mob that "already has `noAi`", then trigger the vibration bean ⇒ 80 ticks later it **is still** `NoAI:1b` (not wrongly released) | as above (`V-08`) | §2.3.2, ruling #8 |
| 16 | repeated Q-Bounce application does not rewrite the attachment: after `friend_chives` lands on a mob, the attachment's `sources` stays `SRC_Q_BOUNCE` (not set twice), `originalNoAi` does not change with the later refreshes; the log prints only 1 `suspend` line per application | the server log + `/data get entity` (`V-09`) | §2.3.4, RT-56 |
| 17 | all three Q-Bounce paths restore the AI: ① wait for the effect to expire naturally (`friend_chives.effectTicks = 200`); ② `/effect clear <mob> farmstorm:q_bounce` mid-way; ③ have the mob drink milk mid-way (`removeEffectsCuredBy(MILK)`) ⇒ all three restore `NoAI` to the original value **within 1 tick** | `/data get entity <mob> NoAI` (`V-10`) | §2.3.4, RT-55 |
| 18 | the mount state does not suspend: while `qbounce.poseMode = mount`, a mob with Q-Bounce has `NoAI` constantly **0b** (never suspended throughout); after switching back to `render`, only the next application suspends it | the server log + `NoAI` (`V-11`) | §2.3.5, ruling #7 |
| 19 | ×3 only against hostiles and only on 2 traps: `blast_carrot` against a zombie = **21.0** (`min(raw,7) × 3`), against a player = **7.0**; `zangief_cane` against a zombie = **22.5** (7.5 × 3), against a player it takes the phase chain (no multiplier) | the server log before `hurt` + the health reading (`V-12`) | §2.5.3, D-184 |
| 20 | non-trap explosions are not ×3: `melon_grenade` / `molotov` deal **exactly the same** damage to a zombie as 0.2.0 (set `damageMultiplier` from 3.0 to 1.0 and to 10.0, two runs, and the reading does not change) | as above (`V-13`) | §2.5.2, RT-57 |
| 21 | the fangs are unchanged: `fang_trap_cactus` deals exactly the same damage to a zombie as 0.2.0 (vanilla `EvokerFangs`, `FangTrapCactusTrap:87-109`, 0 `hurt` in the repository) | as above (`V-14`) | §2.5.3, RT-58 |
| 22 | `trap.damageMultiplier` really appears in **this document** and is machine-readable: `grep -c 'trap\.damageMultiplier' docs/zh/42-TECH-0.2.1.md` ≥ 2, and §4.1.1's table row is hit once by the contract regex; `verify_project.py`'s new parsing function returns `["trap.damageMultiplier"]` | the document + the tool (`V-15`) | §4.1.1, RT-60b |
| 23 | a server stop does not carry the window into the next save (ruling #16): 1 mob is suspended by a vibration bean and **`/stop` is issued before the 80 ticks are up** ⇒ after the restart that mob's `NoAI` is **0b** (restored early), and the entity's `neoforge:attachments` has **no** `farmstorm:ai_suspend` | `latest.log` after the stop + `/data get` after the restart (`V-16`) | §2.3.3, §5.1 |
| 24 | crash residue is settled from the attachment: start the server → trigger a suspension → **kill the process** (no `/stop`) → restart ⇒ on load the mob reads `sources`, the vibration-bean branch is taken as expired ⇒ `NoAI` returns to the original value (≤ 1 tick) and the attachment is deleted; if the Q-Bounce branch's effect is still there, the mob **stays** suspended | `/data get entity` + the server log (`V-17`) | §2.3.3, RT-54 |
| 25 | no new `SavedData`: the number of `.dat` files under `world/data/` is **the same** as before this version (3 now: farmland / outpost_core / the trap FIFO), and their mtime changes only with a normal save | the `world/data/` directory | §5.3 |
| 26 | the rebind path touches neither of the two forbidden methods: inside `OutpostCoreBlockEntity` the call count of `removeCultivator(` = **0** and of `pruneCultivators(` = **1** (only inside `cultivators()`, `:329`) | a source grep (within the method bodies) | §2.1.3, §5.4 |
| 27 | `Hostility` has only 2 use points; all 5 `getEntitiesOfClass(Monster.class, …)` sites are **untouched** (the line numbers are still `BlastCarrotTrap:59` / `FangTrapCactusTrap:54` / `VibrationBeanTrap:59` / `ZangiefCaneTrap:174` / `PumpkinTurret:109`) | a source grep | §2.4, RT-57 |
| 28 | restore means delete the attachment: after any `release`, the entity shows **no** `neoforge:attachments.farmstorm:ai_suspend` in `/data get entity <mob>`; an entity with `sources == 0` **never has** that key (`NONE` is not persisted) | `/data get entity` (`V-18`) | §2.3.2, ruling #15 |
| 29 | the 13th category is explicitly registered and 97 is unchanged: `registry/FarmstormAttachments.java` has **1** `AttachmentType` registration; `Farmstorm.java`'s constructor has **1** more line of `.register(modBus)`; the 12 class assertions at `Farmstorm.java:167-172` **change not one character**; the startup log is still `自检 注册对象 97 个`; §1.2.1 exists and states "not counted in 97 / the tool does not check it" | the source + `latest.log` + §1.2.1 of this document | ruling #17, D-175 |
| 30 | an ownerless core does not rebind: place a core with `/setblock` (`ownerUuid` empty) + 1 unbound cultivator inside the 3×3 ⇒ after **300 ticks** that core's `Cultivators` is still **empty** | `/data get block <core> Cultivators` (`V-19`) | §2.1.3, ruling #13 |
| 31 | the candidate ordering is deterministic: **20** unbound cultivators inside the 3×3, capacity 16 ⇒ the 16 bound entries follow `distSqr` ascending, with ties broken by `x`→`y`→`z`; **running the same save twice gives 16 character-for-character identical entries in `Cultivators`** | `/data get block <core> Cultivators` (`V-20`) | §2.1.3, ruling #14 |

**The 31 items' distribution**: release and counts **5** (#1~#5) / rebind **7** (#6~#10, #30, #31) / tooltip **3** (#11~#13) / AI **7** (#14~#18, #23, #24) / multiplier **3** (#19~#21) / document and tool **1** (#22) / save and attachment **3** (#25, #28, #29) / structure and prohibitions **2** (#26, #27) = **31**.

**Four combinations that must be tested together (written for whoever runs the acceptance)**: ① #6 and #7 **must be run as a pair** — #6 alone cannot reveal "filtered down to 200 ticks by the particle gate", because at the default 40 even 200 ticks is "within 300 ticks"; #7 drags the particle interval to 1200, and any implementation that reuses the particle gate exposes itself on the spot. ② #14 and #15 **must be run as a pair** — testing only an ordinary mob cannot tell "restored to having AI" from "restored to the original value". ③ #19 and #20 **must be run as a pair** — testing only traps cannot tell "multiplied on the trap's own call" from "multiplied on every explosion". ④ #23 and #24 **must be run as a pair** — the former verifies the best-effort path of `ServerStoppingEvent`, the latter verifies the fallback path of "attachment + settlement on load"; running only #23 would solidify the misunderstanding "correctness depends on the server-stop event" (which it precisely does not).

---

## 8. Register of APIs to verify (10 items)

**The criterion**: an API counts as "checked" only when "the type / the method signature / the number of arguments at the call site — all three are seen verbatim in source readable on this machine". The NeoForge source on this machine comes from `.tools/gradle-home/caches/neoformruntime/intermediate_results/sourcesAndCompiledWithNeoForge_c95866ec…_output.jar` (17459 entries, containing the `.java` files of both MC and NeoForge); the line numbers below are **the line numbers of the files inside that jar**, not repository line numbers, and each is marked as such.

| # | The API or semantics | How far it has been checked | Why it is still to verify | Who confirms it | The way out |
|---|---|---|---|---|---|
| ① | the return semantics of `LevelChunk#getBlockEntities()` and the unloaded test | **signature checked**: `public Map<BlockPos, BlockEntity> getBlockEntities()` with `return this.blockEntities;` (`LevelChunk.java:528-530`) — what is returned is the **live map**, not a copy. `ServerChunkCache#getChunkNow` returns `@Nullable LevelChunk` (already in use at `OutpostCoreBlockEntity:296-300`, `:384-387` in this repository) | ① whether the "live map" throws a CME when changed during iteration: this machine has only `DeployedCropBlockEntity:230`'s **single-chunk, no-write** precedent; ② what `getChunkNow` returns for a chunk that is "loaded but not ticked this tick": no runtime evidence; ③ whether `blockEntities` contains a BE that is "created but not yet ticked" (this version needs only `CultivatorBlockEntity`, which must already be created) | the rebind task (run §7 #6~#10 after implementation) | if a CME really appears ⇒ in `collect`, do `new ArrayList<>(chunk.getBlockEntities().values())` first and then filter (the same code as now, only making the "copy" explicit and wrapping a try/catch around it) |
| ② | `Mob#setNoAi` and how "the original value" is read back | **checked**: `public void setNoAi(boolean)` (`Mob.java:1399`), `public boolean isNoAi()` (`:1414`); `NoAI` is persisted at `Mob.java:437-438` (written only when true) and read at `:501`; `isEffectiveAi()` contains `&& !this.isNoAi()` (`:1396`) | ① whether `/data get entity <mob> NoAI` reads stably given that "the key does not exist when the value is false" (`NoAI:0b` and "no such key" are two different things at the NBT layer); ② which branches of `Mob#tick` are skipped once the AI is suspended (the one at `:437`) — the measured conclusion on "does the mount still move" comes only from a comment | the AI task (§7 #14 / #15) | change the acceptance reading to "the key exists and is 1b" = suspended, "no key" = has AI (treating `false` and "the key is missing" as the same state, consistent with the form at `Mob.java:437`) |
| ③ | the availability of `MobEffect`'s `onEffectAdded` / `onEffectRemoved` in 21.1.213 (`QbounceEffect` currently has 0 overrides) | **checked (the conclusion is "one exists, one does not")**: `MobEffect#onEffectAdded(LivingEntity, int)` **exists** (`MobEffect.java:86`), is forwarded by `MobEffectInstance#onEffectAdded` (`MobEffectInstance.java:341-343`), and is called only in `LivingEntity.addEffect`'s "there was no such effect before" branch (`:957-961`; a refresh goes through `:962`'s `update`). **`onEffectRemoved` does not exist** (0 hits in `MobEffect.java`'s 218 lines). The exit side is covered by NeoForge events, and this repository already has precedents: `MobEffectEvent.Remove` (`QbounceObserverSync:130`), `MobEffectEvent.Expired` (`:136`), `MobEffectEvent.Added` (`QbounceMountController:109`) | ① `onEffectAdded` is **not** called when "the effect is read back from NBT" (`readAdditionalSaveData` fills `activeEffects` directly) — this version's design requires exactly that (no takeover after a restart), but it has not been measured; ② on `LivingDeathEvent` the effect is not removed (this repository's `QbounceObserverSync:142` comment records this), so the reconciliation must handle "the entity is already dead" itself | the AI task (the implementation of §2.3.4) | if `onEffectAdded` does not fire on some path ⇒ additionally mount `MobEffectEvent.Added` (filtered by `getOldEffectInstance() == null`, as in `QbounceMountController:117-119`); mounting both at once does not double up (the `sources` bits are idempotent) |
| ④ | the `Enemy` interface and how `MobCategory.MONSTER` is obtained | **checked**: `Enemy` is a pure marker interface (`Enemy.java:1-11`, with only the 6 `XP_REWARD_*` constants); `Monster extends PathfinderMob implements Enemy` (`Monster.java:29`); the category is obtained by `public MobCategory getCategory()` (`EntityType.java:1043`) | ① the coverage gap for modded mobs (a "pseudo-hostile" that is neither `Enemy` nor of category `MONSTER`) cannot be enumerated; ② `entity.getType().getCategory()` returns `MISC` for a non-`Mob` entity (no side effect, but no runtime evidence) | the multiplier task (§7 #19) | if a modded mob is really missed ⇒ register it only (document 40 §7 ③ has already fixed this item as "do not widen the criterion"), and do not add a whitelist on your own |
| ⑤ | the specific functions by which `verify_project.py` extracts the key set from the documents | **checked**: `main()` `:349-355` reads the 6 fixed documents (`01-DESIGN` / `12` / `18` / `24` / `30` / `36`); `expected_config_keys_v012` (`:137-179`) is bounded by `#### 4.1.1` ~ `#### 4.1.2`, 7 columns, with a row-count assertion of 11; the union and the deletion check are at `:382-391`; **it does not read `docs/zh/42-*` today** | ① after the new parser is added, `main()`'s expected number goes from 175 to 176, and the 58-group assertion of `30-TECH §4.1` (`:228-230`) is unaffected, but the toml under `run/config` must be **regenerated first** (the one on disk now is an old version, §9 item 2); ② the "language keys" section (`:464-474`) compares **the key sets for equality only** and **does not check the number 338** — "language keys 338" has no automatic gate | the tool task (§7 #2 / #5 / #22) | if the table format of "reading document 42 §4.1.1" is broken by a later change ⇒ the parser should **throw with the file name + the line number**, in the form of `:172-178` (better red than silently checking less) |
| ⑥ | the trigger surface of `EntityJoinLevelEvent` (settlement on load) | **not checked**: 0 uses in this repository; there is no runtime evidence for it on this machine | ① whether it covers the case "a chunk load puts an entity back into the world" (and not only `/summon` and the first join); ② the order between the trigger timing and the readability of `getData(AI_SUSPEND)` (the attachment is already deserialised when the entity is loaded, so in theory earlier, but it has not been measured) | the AI task (§7 #23 / #24) | a **bounded rescan at server start**: follow `QbounceMountController`'s `ADOPT_RESCAN_INTERVAL_TICKS = 20` / `MAX_ADOPT_RESCANS = 10` (`:63-70`, `:190-194`) for 10 rounds; known gap: entities in chunks that load later than 10 seconds are not settled (registered as an RT-54 residue) |
| ⑦ | the ordering of `EventPriority.HIGHEST` on `ServerTickEvent.Post` | **not checked**: none of this repository's three `ServerTickEvent.Post` subscribers (`QbounceMountController:174`, `QbounceObserverSync:120`, `FarmstormGameEvents`) has ever written a priority | "the AI reconciliation must precede the mount" holds only logically (HIGHEST < NORMAL), with no runtime evidence | the AI task (§7 #18) | mount `ServerTickEvent.Pre` instead (the event phase is naturally earlier than every `Post` subscriber, so it does not depend on priority); the cost is "an effect just removed in the same tick is restored on the next tick" (a 1-tick movement loss, acceptable) |
| ⑧ | when `AttachmentType` is written / read back, and its size | **the read/write surface is checked**: `AttachmentHolder.serializeAttachments` (`:122-145`), `deserializeAttachments` (`:151-172`), the key name `neoforge:attachments` (`:27`), `Entity extends AttachmentHolder` (`Entity.java:131`); `AttachmentType.builder(Supplier)` (`:95`), `.serialize(Codec, Predicate)` (`:196`), `.build()` (`:294`) | ① when an entity attachment is written on the **chunk save** path (`serializeAttachments` is called by `Entity`'s save hook; on this machine only the signature was read, not the call chain); ② the actual effect of one ≤ 40-byte compound per entity on the chunk size; ③ whether `.serialize(Codec, Predicate)` **writes no key at all** (rather than an empty one) when `shouldSerialize` is false — that decides whether the sentence "zero bytes in the save in the normal state" holds | the AI task (§7 #28) | if `shouldSerialize` still writes an empty compound ⇒ change to "`removeData` on `release`, with the default still NONE"; the size impact is ≤ 40 bytes and the conclusion is unchanged (only "zero bytes" has to become "≤ 40 bytes") |
| ⑨ | the interaction between an `ArmorStand` mount and `noAi` (vibration bean × mount on the same tick) | **not checked**: the three checks of `QbounceMountController:307-316` do not include `noAi`, so all that can be inferred is "the mount carries an entity that does not move" | the actual behaviour of "has a mount + `noAi`" for 80 ticks (whether vanilla `Mob#tick`'s `:437` branch interrupts the riding) has not been measured | the acceptance task (§7 #14's and #18's neighbouring scenario; this version only registers it, with no exemption) | if the measurement shows "the mount is removed / the entity is thrown off" ⇒ register it per RT-55 and do not change ruling #4's effective surface |
| ⑩ | the default-value semantics of `getData` / `setData` / `removeData` / `hasData` | **signatures checked**: `hasData` (`IAttachmentHolder.java:24`), `getData` (`:39`), `setData` (`:90`), `removeData` (`:106`); `AttachmentHolder`'s implementations at `:68` / `:74` / `:97` / `:107` | ① whether `getData` **writes the default value into the map** when taking it (which decides whether `hasData` can serve as the "is it suspended" criterion — this document therefore **does not use** `hasData`, only `sources != 0`); ② whether a `getData` immediately after `removeData` reliably falls back to `NONE` | the AI task (§7 #28) | if `getData` does write the map ⇒ it **still does not affect** this document (the existence criterion is `sources != 0`); if `getData` does not fall back after `removeData` ⇒ add one layer of `sources == 0 ? NONE : the stored value` on the default-value provider |

**Three records of items "downgraded from to-verify to checked"** (written here so that documents 43 / 44 do not treat them as risks again): ① the existence and the trigger branch of `MobEffect#onEffectAdded` (`MobEffect.java:86` + `LivingEntity.java:957-961`); ② the convergence point of the three effect-removal paths (expiry `LivingEntity.java:790`, `/effect clear` `:911-921`, milk `MilkBucketItem.java:27` → `LivingEntity.java:3517-3525`, all three going through `EventHooks.onEffectRemoved`, `EventHooks.java:1050-1056`); ③ `AttachmentType`'s registry and serialisation surface (`AttachmentType.java:35` + `AttachmentHolder.java:27/122-145/151-172` + `Entity.java:131`).

---

## 9. Register of differences from upstream (16 items, **upstream is not changed on its own**)

Ordered by four kinds: "a contradiction with document 40 / a contradiction with `BRIEF` / a deviation from the tool's actual behaviour / source-comment drift". **This document only registers; it changes no upstream file.**

| # | Involves | What upstream says | How this document handles it | Nature |
|---|---|---|---|---|
| 1 | document 40's D-173 supplement + §7 ⑤ vs `BRIEF` §3.4's 2nd row | document 40 says "the **header lines** of the two `.cmd` files (each `:5`) also hard-code `Farmstorm v0.2.0`, and `BRIEF` §3.4's 2nd row **does not list them** (registered in §7 ⑤)"; §7 ⑤ says it again ("`BRIEF` §3.4's 2nd row lists only the three jar names") and leaves "whether the header lines change too" to document 42 | **verified: `BRIEF`'s line 183 (§3.4's 2nd row) already says "and the header lines of the two scripts (`一键构建.cmd:5`, `检查环境.cmd:5`, hard-coded `Farmstorm v0.2.0`)"** ⇒ those two passages of document 40 do not match the fact card. This document follows `BRIEF` (**the header lines change too**, see §3 #2 and #4) and removes the question "whether they change too" (`BRIEF` has already answered it) | document 40's description is inaccurate (the conclusion is unaffected) |
| 2 | `BRIEF` §0's toml side witness | `BRIEF` §0 writes that the config is "(consistent with `run/config/farmstorm-server.toml`'s **175 keys / 60 sections**) \| measured" | **holds, re-checked**: `run/config/farmstorm-server.toml` measures **801 lines / 60 sections / 175 keys** (mtime `2026-11-04 11:29`, keys identified by a leading Tab), consistent with the current count of `FarmstormConfig.java`. ⚠️ But the repository holds many old copies under `run-*/config/` (`run-verify`, `run-f*`, etc., all 12,238 B, about **121 keys / 36 sections**) — **when counting keys, `run/config/` is the only copy that counts**. Regenerate with one `runServer` before the acceptance (`verify_project.py:437-440` has a STALE hint for this case, with the same criterion) | this document's first draft took an old copy by mistake; corrected |
| 3 | `BRIEF` §0.2 / D-175 / D-185's description of the tool mechanism | all three say "`verify_project.py`'s expected key set is **extracted from `docs/zh/*`**" | **inaccurate**: it reads only **the fixed sections of 6 fixed documents** (`main():349-355`'s `01-DESIGN` / `12` / `18` / `24` / `30` / `36`), neither globbing `docs/zh/*` nor reading documents 40 ~ 45. ⇒ "writing the new key into this document" is **necessary but not sufficient**: `expected_config_keys_v021` must also be added per §4.1.1's contract, in the same commit. This item is the concrete shape of A-66's false green **in this version** | upstream's description is imprecise (the mechanism itself is still A-66) |
| 4 | `BRIEF` §0 / D-185 on the language keys' automatic gate | both list "language keys 337 → 338" alongside the config keys as "the expected surface must be kept in step" | `verify_project.py`'s section 3 (`:464-474`) **checks only that the zh and en key sets are equal** and **does not check the number 338**. ⇒ "language keys 338" has **no automatic gate** in this version; it can only be counted by hand (§4.3's `ConvertFrom-Json` method) and written into §7 #5 | missing (fails silently) |
| 5 | `BRIEF` §4's risk numbering | the table inserts an **`RT-60b`** between RT-56 and RT-57, whereas the RT series should be consecutive (document 40 §0.1's continuation table says "risks from RT-51" are written by document 42) | this document **opens no new RT number**: RT-51 ~ RT-60 stand one by one (§6), and `RT-60b` is listed as a **sub-item of RT-60** on its own row (the second entry of the same mechanism: the single key `trap.damageMultiplier`); the number follows `BRIEF`'s form and does not start a 61. If document 43 wants strict consecutiveness, merge §6's last row into RT-60; this document's acceptance coverage is unchanged (§7 #22) | numbering is not consecutive (copied from `BRIEF`, not changed) |
| 6 | `BRIEF` §2's item count and document 40's transcription | `BRIEF`'s line 5 says "§2's **9 items** were decided on the spot in the author interview", and §2's heading once said "author's rulings (**11 items**)"; **during writing the author added #13 ~ #17, so `BRIEF` §2 now has 17 items** (this item rests on the author's message and the current version of `BRIEF`) | this document cites **17 items** (#1~#12, #13 ownerless core, #14 sorting, #15 attachment persistence, #16 the window does not cross a save, #17 explicit registration of the 13th category). **Document 40 still transcribes only 11** (the mapping table of D-173 ~ D-186 and §2 also covers #1~#11 only) ⇒ these 6 items (#12 ~ #17) **have no D number** and must be back-filled by document 43 starting from A-70 (by discipline this document opens no D number itself). Document 40 §2 note 3 has already registered the "9 vs 11" discrepancy; this document only updates it to 17 | upstream has changed, document 40 needs a back-fix (**the conclusion is unchanged**: none of the three numbers 97 / 176 / 338 is affected) |
| 7 | the question document 40 §7 ① left to document 42 | "whether the rebind makes `removeCultivator` necessary (for example, to undo a wrong rebind) is to be decided by **document 42** within D-177's framework" | **this document decides "not needed, not enabled"**: the rebind only adds, never removes (D-177), and a candidate whose "two sides disagree" is always **skipped** rather than "bound first and removed afterwards" ⇒ there is no "undo" scenario. `removeCultivator`'s (`:366-377`) call count **is still 0**; this version neither enables nor deletes it (deleting dead code is pre-existing cleanup, kept out by D-174). §7 #26 is this item's acceptance | work left by upstream, done here |
| 8 | document 40 §7 ⑥ and §2.3.7's cross-consequence | document 40 says only "the vibration bean's AI deprivation is a one-off snapshot" and does not say what happens when "the suspended mob is being carried along by the mount branch" | **this document adds one cross-rule** (§2.3.7): the vibration bean **grants no exemption for a mount** (ruling #4 has no such exception); the consequence is "has a mount + does not move" for at most 80 ticks, after which the AI is restored from the original value and the mount carries on as before. This item does not go into document 40 ([author's supplement]), but it is something document 41 must know when writing the player-visible notes | missing ([author's supplement]) |
| 9 | document 40 §0.2's glossary entry "multiplier" and its range | "the range and the field name are to be fixed by document 42 within this item's framework" | this document fixes **1.0 ~ 10.0** (§4.1.2 gives four reasons). It does not conflict with document 40's "default 3.0, double" | work left by upstream, done here |
| 10 | this repository's Mixin counting measure | `BRIEF` §0 writes "Mixin **0** \| measured" | **holds under the `src/` measure**: `@Mixin` 0 and `*.mixins.json` 0 across the `src/` tree. But **the whole repository** (including `.scratch/`) has **1** `neoforge.mixins.json` under `.scratch/p25a/src/` — an experimental leftover, not under `src/`, not part of the build. An acceptance using "the whole repository `Get-ChildItem -Recurse -Filter *.mixins.json`" would hit 1 ⇒ §7 #3 of this document writes the measure as "under `src/` = 0" and names that one file here | counting measure (not a defect) |
| 11 | source-comment drift (does not affect any count) | — | two places: ① the comment at `FarmstormBlocks.java:43` says "the **3 blocks** added in v0.0.3", whereas that section has only **1** (`FRIEND_CHIVES_CROP`, `:45`); ② the comment at `ModLangProvider.java:237` says "**139 config entries**", whereas that file's `config(entries, …)` is in fact called **178** times (the comment at `:14` says 178 and is correct). Neither affects this document's blocks **27** / language keys **338** (those are counted from the registration call sites and the key set), and this version does not change the comments (D-174) | source-comment drift (registered) |
| 12 | the reading difference on this machine (which makes every line number wrong) | both `BRIEF` and document 36 give line numbers under the UTF-8 reading | but **the default `Get-Content` (without `-Encoding`) reads the two files with a different line count**: `FarmstormConfig.java` = 1131 lines (UTF-8) vs 1095 lines (default), and `OutpostCoreBlockEntity` and others are offset the same way; the `trap` section's `:917` / `:920` in `FarmstormConfig` become `:906`-ish under the default reading. ⇒ every line number in this document is UTF-8 (§0.2 item 4), and it is stated here that "document 40's and `BRIEF`'s line numbers are on the UTF-8 measure and reproducible on this machine" | reading measure (written down now so that the next author is not out of step) |
| 13 | the **pre-existing inconsistency** reported by document 41 (outside this version's scope) | the vibration bean's `findTrigger` filters out creative / spectator / own-side (`VibrationBeanTrap:51-54`), while `trigger`'s confusion loop iterates **all** of `level.players()` and **does not filter** (`:83-91`) | this version **does not fix it** (D-174's scope gate). Consequence: creative / spectator players do not trigger a vibration bean, yet are stunned by a bean **someone else** triggered. Handling: §2.3.6's last item states this pre-existing measure, and §7 #14 / #15 always test with **survival-mode, non-own-side** players, so that pre-existing behaviour is not recorded as a regression of this version | pre-existing defect (registered, not touched by this version) |
| 14 | ruling #17 vs D-175's wording | D-175 rejected "carrying the original AI flag in a data component" on the grounds that it "**would make the registry surface 98**"; this version switches to an `AttachmentType`, and the reader may easily think "it went around its own rejection by changing the name" | **an attachment ≠ a data component**: `DataComponentType` is the 11th of this project's 12 classes (currently 1, `farmstorm:temporary_enchantments`; adding one ⇒ 2 ⇒ 98), and that is what D-175 rejected; `AttachmentType` is registered in `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` (outside the measure) ⇒ **97 unchanged**. Ruling #17 has stated explicitly "97 unchanged, but it must be explicitly registered, never invisible" ⇒ this document therefore lists **§1.2.1, "the 13th category outside the count"**, on its own, and registers it in three places: §3 #19 / #29 and §7 #29. **It is suggested that document 40 add one sentence to D-175's supplement**: "what is rejected is `data_components`, not attachments" | already ruled (#17); it is suggested that document 40 add one sentence of wording |
| 15 | the relation between ruling #16 and D-179's criticism of "recording a point in time" | D-179 criticises `WildHunterAura:52`'s `AI_RESTORE_AT` (which stores "the expiry instant"); then "storing the expiry instant in the attachment too" would be a recurrence of the same shape | ruling #16 explicitly **rejects** "storing the expiry instant in the attachment" ⇒ this document's attachment holds only `(originalNoAi, sources)`, and the vibration bean's expiry instant lives only in memory (`VB_DEADLINE`). **This is not a contradiction but an extension of the same criticism**; it is registered here so that the reader understands "why there is no timestamp in the attachment", and to stop a later hand from "adding a deadline field while passing by" | the measure is consistent (registered to prevent a recurrence) |
| 16 | a guard that ruling #16 **removes** in passing (design convergence) | had "the window does not cross a save" not been adopted and the expiry instant been persisted instead, a "time-went-backwards guard" would have had to come with it (`deadline - now > 80` is treated as already expired, precedent `OutpostCoreBlockEntity:540-541`'s `LAST_RING_TICK`) | the attachment stores no timestamp ⇒ a rollback / a world copy needs no special case, and that guard **need not exist**. `OutpostCoreBlockEntity:540-541` is still kept as a side witness that "time going backwards really does happen", but this version has nowhere that needs it. This item is registered so that document 43 knows: **no "time goes backwards after a rollback" item should appear in the acceptance**, and nobody should add a `deadline` field to the attachment | design convergence (registered) |

**Four hints for document 43**:

1. Item 3 is **the mandatory change this version is most likely to miss**: unless `verify_project.py`'s parser is changed, `trap.damageMultiplier` being written into the document or not makes no difference to the green. It must be in **the same commit** as that line of `FarmstormConfig`, and tied to the table format of document 42 §4.1.1.
2. Item 2 affects the acceptance's **first step**: let `runServer` regenerate `run/config/farmstorm-server.toml` first, then count 176 / 60; but the repository has old copies such as `run-verify`, `run-f*` (about 121 keys / 36 sections) — **`run/config/` is the only copy that counts**, so do not take an old copy as the baseline.
3. Items 1 and 6 are both **inaccurate upstream descriptions with unchanged conclusions**, and need no further ruling from the author; items 7 / 8 / 9 / 13 are gaps that documents 40 or 41 explicitly left to document 42, and this document has given a conclusion for each (not enabling `removeCultivator` / the vibration bean is not exempted from a mount / the range 1.0~10.0 / the pre-existing inconsistency is not fixed).
4. **Items 6 and 14 need a back-fix in document 40**: the former is "`BRIEF` §2 already has 17 items while document 40 transcribes only 11" — #12 ~ #17 need document 43 to back-fill D numbers from A-70; the latter is D-175's wording ("rejecting a data component" ≠ "rejecting an attachment"). Neither of them **affects** 97 / 176 / 338, nor any acceptance item of this version.
