# 30 · v0.1.1 Technical Specification

> Project: Farmstorm · version **0.1.1** (MC 1.21.1 / NeoForge 21.1.213 / Java 21).
> Single source of truth: `docs/zh/28-DECISIONS-0.1.1.md` (D-124 ~ D-147, 24 in total). This document **only cites** decision numbers: it opens no new decision and changes no ruling; where it conflicts with document 28, document 28 prevails.
> Factual base: `.work/v011-BRIEF.md` (§1 source facts / §2 the 25 rulings / §3 the increment list / §4 risks RT-31 ~ RT-41 / the 93 ids of appendix A).
> Stylistic reference: `docs/zh/24-TECH-0.1.0.md` (**only its section structure and table conventions are learned; not one of its numbers is cited**).
> New red-line ruling: **only "no Mixin" is left** (D-146). "0 custom packets" is **abolished outright**; this version adds 1 (D-142) and **sets no cap**; each new packet must still be registered in the decision record.
> Writing discipline: every count in this document is counted out of the source by this document itself, item by item, and names file:line (§1.2 / §4.1 / §4.3); every technical parameter this document fixes on its own (payload name, NBT key name, ticket level, row spacing and so on) is marked "**fixed by this document**" and given a reason (the summary is appendix A); **no task schedule and no effort** (those belong to document 31); task numbers F-xx are issued by 31, and this document holds people to account only by §7's acceptance numbers (#1 ~ #38).

---

## 0. Reading conventions

### 0.1 What this document is responsible for

| Item | Content |
|---|---|
| Responsible for | the **per-class independent naming** of the 93 registry objects and the counting method (§1.2); the reason this version's registry increment is +0 (§1.3); the current baseline of the language keys and the server config keys (§1.4); the class design of the seven technical components (the packet, the binding table, the command deletion, the client components, the chunk ticket, the projectile convergence, the Friend Chives carrier) (§2); the master table of additions / changes / deletions against 0.1.0 (§3, 47 rows); the full config table and the language-key additions and deletions (§4); the resource inventory (§5); how RT-31 ~ RT-41 are verified (§6); the **38** acceptance self-check items (§7); the 8 APIs to verify (§8); the register of differences from upstream (§9, 8 rows; the already-adjudicated A-30 ~ A-49 are not registered twice, the only exception being A-49, whose landing point is exactly §9 item 5) |
| Not responsible for | the gameplay narrative, the timing tables and the value tables (document 29); task breakdown, phases, milestones and effort (document 31); the master prompt and the phase prompts (documents 32 / 33); the design trade-offs of the 21 fields other than "technical parameters" (document 28) |
| Files left untouched | `src/`, `docs/zh/00` ~ `29`, `docs/en/*`, `.work/v011-BRIEF.md`, `tools/verify_project.py` are all read-only; this document creates exactly one file, `docs/zh/30-TECH-0.1.1.md` |

### 0.2 Counting discipline

1. **The three numbers — registry objects, config keys, language keys — are all counted by this document itself**, with a statement of "which lines of which files were counted". It copies neither fact card §0 nor document 28 §3.4.
2. Each of the three tables satisfies "row count = the declared count", "subtotal = the sum of the rows" and "no overlap and no omission across tables".
3. Numbers may come from three sources only: fact card §1 / §2 / §3, two-number arithmetic, and the technical parameters this document marks "fixed by this document" (with a reason). config defaults **may only** be taken from fact card §3.4.
4. Always read files with `[IO.File]::ReadAllLines($p,[Text.Encoding]::UTF8)` (on this machine `Get-Content` reads BOM-less UTF-8 Chinese as GBK mojibake).

---

## 1. Baseline and the registry-surface check

### 1.1 Platform and build

| Item | Value | Evidence |
|---|---|---|
| Version number | 0.1.1 (`mod_version`) | `gradle.properties:11` currently reads `mod_version=0.1.0`; this version changes it to 0.1.1 (D-124) |
| Build baseline | MC 1.21.1 / NeoForge 21.1.213 / Java 21 | `gradle.properties:2-3`, `:16-17`, `.tools/jdk21` (21.0.2) |
| Build entry point | `.tools\gw.cmd` (the only entry point; arguments are forwarded to gradle unchanged) | `gw.cmd:41`; the tasks `runClient` / `runServer` / `runData` / `build` are declared by `build.gradle:28-47` |
| Run directory | `run/` (isolatable with `-PfsRunDir=`) | the `gameDirectory` property of `build.gradle` |
| Java file count | **125 files / 15,785 lines** (the whole `src/main/java` tree, counted with `ReadAllLines` file by file) | **counted by this document itself**: `block` 4 / `blockentity` 3 / `behavior` 18 (`behavior` 9 + `behavior/impl` 9) / `client` 20 + `client/render` 1 / `combat` 6 / `command` 1 / `config` 1 / `datagen` 10 / `effect` 4 / `entity` 2 + `entity/thrown` 12 / `event` 3 / `explosion` 3 / `farmland` 2 / `combo` none / `item` 5 / `menu` 1 / `outpost` 1 / `recipe` 2 / `registry` 11 / `team` 2 / `trap` 2 / `util` 10 / root 1 |
| `client` package class count | **21** (20 top-level + `client/render/TurretBulletRenderer`) | measured by this document; consistent with fact card §1.5's "21 classes" (§1.4 re-check) |

> Note: `behavior` 18 = the 9 top-level classes under `behavior` + the 9 under `behavior/impl`; `entity` 2 + `entity/thrown` 12 = 14. Both pairs are listed by directory separately, and added up they come to 125.

### 1.2 Per-class naming of the registry surface (**counted out of the source by this document itself**, 93 in total)

**Counting method (reproducible)**: open each of the 11 files under `src/main/java/com/dashpig/farmstorm/registry/` and count item by item by "registration call + registered id literal", never by back-deriving from `EXPECTED_COUNTS`. Three counting measures:

- `DeferredRegister#register(...)` / `registerItem` / `registerBlock` / `registerComponentType` calls: count **the number of calls**;
- registrations that go through a factory method (`cropItem` / `seedItem` / `crop` / `deployed` / `projectile`): count **the number of factory calls**, with the id given by `CropProfile.path()` or by the first argument literal;
- `damage_type` is **data-driven** (the comment at `FarmstormDamageTypes.java:14`: the instance is supplied by the datapack JSON, and the code side has only a `ResourceKey`): count **the number of static `ResourceKey` constants**.

| # | registry | What was counted (file:line) | Ids one by one | Count |
|---|---|---|---|---|
| 1 | `Registries.ITEM` | `FarmstormItems.java`: 22 × `cropItem(...)` (`:36,38,40,42,44,47,49,51,53,55,57,59,61,63,65,70,72,75,79,85,91,95`) + 7 × `seedItem(...)` (`:89,100,101,102,103,104,106`) + 3 × `registerItem(...)` (`:110,113,116`) | 22 crops/items: `puffed_wheat` `blast_carrot` `ubi_server` `toxic_ubi_server` `power_beet` `melon_slice_bomb` `melon_grenade` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `lucky_berry` `molotov` `zangief_cane` `wild_apple` `compressed_puffed_wheat` `friend_chives` `point_defense_fungus` `vibration_bean`; 7 seed items: `chive_seeds` `puffed_wheat_seeds` `power_beet_seeds` `melon_grenade_seeds` `pumpkin_turret_seeds` `awakened_torchflower_seeds` `awakened_pitcher_plant_pod`; 3 block items: `converter` `compressed_hay_block` `outpost_core` | **32** |
| 2 | `Registries.BLOCK` | `FarmstormBlocks.java`: 15 × `crop(...)` (`:26,27,28,29,30,31,32,33,34,35,36,37,38,39,43`) + 8 × `deployed(...)` (`:46,47,48,50,52,55,62,65`) + 3 × `registerBlock(...)` (`:69,73,82`) | 15 crop blocks: `puffed_wheat_crop` `blast_carrot_crop` `ubi_server_crop` `toxic_ubi_server_crop` `power_beet_crop` `melon_grenade_stem` `pumpkin_turret_stem` `awakened_torchflower_crop` `awakened_pitcher_plant_crop` `fang_trap_cactus_block` `bamboo_firecracker_stalk` `red_mushroom_puff_block` `brown_mushroom_puff_block` `lucky_berry_bush` `friend_chives_crop`; 8 deployed blocks: `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `zangief_cane` `point_defense_fungus` `vibration_bean`; 3 others: `converter` `compressed_hay_block` `outpost_core` | **26** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | `FarmstormBlockEntities.java:24,28,31,34,38,42,50` (7 × `REGISTER.register`) | `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `converter` `outpost_core` | **7** |
| 4 | `Registries.ENTITY_TYPE` | `FarmstormEntityTypes.java`: 12 × the factory `projectile(...)` (`:31,33,35,37,39,41,43,45,49,51,55,57`, factory body at `:72`) + 1 × `REGISTER.register("turret_bullet", ...)` (`:61`) | `puffed_wheat` `ubi_server` `toxic_ubi_server` `melon_slice_bomb` `melon_grenade` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `molotov` `compressed_puffed_wheat` `friend_chives` `vibration_bean` `turret_bullet` | **13** |
| 5 | `Registries.MOB_EFFECT` | `FarmstormMobEffects.java:24,28,32,36` | `wild_hunter` `zangief_piledriver` `q_bounce` `confusion` | **4** |
| 6 | `Registries.SOUND_EVENT` | `FarmstormSounds.java:21,26,31` | `wild_hunter_loop` `zangief_piledriver` `friend_chives` | **3** |
| 7 | `Registries.RECIPE_TYPE` | `FarmstormRecipes.java:23` | `converting` | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | `FarmstormRecipes.java:25` | `converting` | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | `FarmstormCreativeTabs.java:16` | `main` | **1** |
| 10 | `Registries.DAMAGE_TYPE` | `FarmstormDamageTypes.java:20,23,26` (3 `ResourceKey` constants, data-driven) | `void_bolt` `crop_blast` `zangief_slam` | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `FarmstormDataComponents.java:16` | `temporary_enchantments` | **1** |
| 12 | `Registries.MENU` | `FarmstormMenus.java:17` | `converter` | **1** |
| | **Total** | | | **93** |

Per-class subtotal (equal to the rows above, item by item):

```
items                 32
blocks                26
block_entity_type      7
entity_type           13
mob_effect             4
sound_event            3
recipe_type            1
recipe_serializer      1
creative_mode_tab      1
damage_type            3
data_component_type    1
menu_type              1
--------------------------------
sum = 32+26+7+13+4+3+1+1+1+3+1+1 = 93
```

**Three independent algorithms cross-check one another**: this document's item-by-item naming = **93**; the 12 assertions of `Farmstorm#logRegistrySelfCheck` (`Farmstorm.java:124-146`) add up to **93** (hard-coded per class at `:138-141`); the `EXPECTED_COUNTS` of `tools/verify_project.py` (`:197-210`) plus the id list of `EXPECTED` (`:157-194`) add up to **93**. This document's number is **not** copied from the latter two — it was counted out again under §1.2's "number of calls" measure, and the two side witnesses are used only to confirm the measure has not drifted.

### 1.3 This version's registry increment = +0

| What this version adds | Why it does not enter registry |
|---|---|
| 1 custom packet (D-142) | the payload is registered through `RegisterPayloadHandlersEvent` / `NetworkRegistry` and **does not go into `BuiltInRegistries`**; `CustomPacketPayload.Type` is only a `ResourceLocation` record (`record Type<T>(ResourceLocation id)` of `CustomPacketPayload.java`), not a registry entry |
| 2 `KeyMapping`s (D-144) | key registration goes into the client's `Options#keyMappings` array (the implementation of `RegisterKeyMappingsEvent#register` is exactly `ArrayUtils.add(options.keyMappings, key)`), and has nothing to do with registry |
| CLIENT config 3 keys (D-144) | `ModConfigSpec` is a config-file model and does not enter registry |
| the `/farmstorm hud …` client command (D-144) | the Brigadier command tree is a runtime registration (`RegisterClientCommandsEvent`) and enters no registry |
| the farmland binding table (D-134) | persisted as `SavedData`, enters no registry |
| the chunk ticket (D-145) | `TicketType.create(...)` is an ordinary object, and 1.21.1 **has no ticket registry at all** (see §2.5) |

⇒ **the registry surface is still 93, equal to §1.2 item by item**. This version's "+0" does not mean "nothing was added" but "nothing that was added sits on the registry surface", which is exactly the ruling of fact card §3.1 and D-146.

### 1.4 Current baseline of the language keys and the config keys (counted by this document itself)

**SERVER config (currently 166 keys / 59 groups)**: the measure is to read the static block of `config/FarmstormConfig.java` (`static {` at `:268` through `SPEC = b.build();` at `:928`) and to land every key by the rule "`b.push("…")` / `b.pop()` maintain the group stack; `.define("…")` / `.defineInRange("…")` / `.defineInList("…")` count one key". This document ran a script over it and then re-checked every group by hand. The result:

```
keys 166 / groups 59
groups = 35 top-level groups + 24 nested groups (blast_carrot.nausea 1; under awakened_torchflower 6;
     under awakened_pitcher_plant 3; under red_mushroom_puff 2; under brown_mushroom_puff 2;
     under lucky_berry 6; under power_beet 4)
```

**CLIENT config (currently 0 keys)**: `Farmstorm.java:56` is the only `registerConfig` in the whole repository, of type `ModConfig.Type.SERVER`; `ModConfig.Type.CLIENT` has 0 hits across the repository (fact card §1.5, re-checked by this document).

**Language keys (currently 315 × 2)**: read `src/generated/resources/assets/farmstorm/lang/zh_cn.json` and `en_us.json`, take the key set after `ConvertFrom-Json`: `zh=315`, `en=315`, the key sets exactly identical. By prefix (the grouping is this document's own, adding up to 315):

```
farmstorm.configuration   166   ← equal to the server config key count, key by key (1:1)
advancements.farmstorm     36
item.farmstorm             32
block.farmstorm            26
tooltip.farmstorm          16
entity.farmstorm           13
gui.farmstorm               8
death.attack                6
effect.farmstorm            4
subtitles.farmstorm         3
message.farmstorm           2
command.farmstorm           1
container.farmstorm         1
itemGroup.farmstorm         1
--------------------------------
166+36+32+26+16+13+8+6+4+3+2+1+1+1 = 315
```

**The conclusions of the two cross-checks**: ① the `farmstorm.configuration.*` keys inside the language keys are **exactly 166**, in one-to-one correspondence with the 166 `define*` calls of `FarmstormConfig` (`config(...)` at `ModLangProvider.java:438` is the only source); ② `block.farmstorm.*` = 26, `item.farmstorm.*` = 32 and `entity.farmstorm.*` = 13 inside the language keys are each equal to the three numbers blocks / items / entity_type of §1.2 — these three groups are the hard constraint between "the registry surface ↔ the resource surface", and renaming any one id blows up right here.

---

## 2. The technical components this version adds (a class design for each)

### 2.1 The packet (**the project's first custom packet**)

#### 2.1.1 Facts and boundary

This repository has **0** custom packets so far (all 17 keywords such as `CustomPacketPayload` / `RegisterPayloadHandlersEvent` / `PacketDistributor` / `SimpleChannel` have 0 hits, fact card §1.4). This section therefore **gives the signature evidence first and the design second**: for every signature, this document has checked it against the local source jars, and the place checked is written in parentheses; any semantics still uncertain goes into §8 without exception.

The sources checked (read-only, unmodified): `PayloadRegistrar.java` / `RegisterPayloadHandlersEvent.java` / `PacketDistributor.java` / `RegisterClientCommandsEvent.java` / `RegisterKeyMappingsEvent.java` inside `D:\project_farm\.tools\gradle-home\caches\modules-2\files-2.1\net.neoforged\neoforge\21.1.213\…\neoforge-21.1.213-sources.jar`, and `StreamCodec.java` / `ByteBufCodecs.java` / `CustomPacketPayload.java` / `TicketType.java` inside `…\neoformruntime\intermediate_results\sourcesAndCompiledWithNeoForge_…_output.jar`. **These signatures are not guessed**; what hangs in §8 is "runtime behaviour", not "whether this method exists".

The signatures already checked:

| API | Signature (verbatim) |
|---|---|
| `RegisterPayloadHandlersEvent` | `public PayloadRegistrar registrar(String version)`; the event implements `IModBusEvent` ⇒ it hangs on the **mod bus** |
| `PayloadRegistrar` | `public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> reader, IPayloadHandler<T> handler)` |
| `PayloadRegistrar` default thread | inside the private `register(...)`: `if (this.thread == HandlerThread.MAIN) handler = new MainThreadPayloadHandler<>(handler);` ⇒ **already on the main thread by default** |
| `PacketDistributor` | `public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads)` |
| `CustomPacketPayload.Type` | `public static record Type<T extends CustomPacketPayload>(ResourceLocation id)` ⇒ construct it with `new Type<>(Farmstorm.id("core_list"))` and **not** with `CustomPacketPayload.createType(String)` (that one goes through `ResourceLocation.withDefaultNamespace` and lands in the `minecraft:` namespace) |
| `StreamCodec#composite` | 6 overloads for 1 ~ 6 fields (checked one by one in `StreamCodec.java`) |
| `ByteBufCodecs` | the `BOOL` / `VAR_INT` / `STRING_UTF8` fields; `list()` and `list(int)` return a `CodecOperation` (to be used with `StreamCodec#apply`) |
| `ResourceLocation.STREAM_CODEC` / `BlockPos.STREAM_CODEC` | both exist (checked with `javap`) |
| `IPayloadContext` | `Player player()`, `CompletableFuture<Void> enqueueWork(Runnable)`, `PacketFlow flow()`, `default void reply(CustomPacketPayload)` |

#### 2.1.2 payload design (fixed by this document)

Package name `com.dashpig.farmstorm.network`; four classes (3 in `network/` + 1 **client-only** `client/network/ClientCoreListHandler`):

| Class | Responsibility |
|---|---|
| `CoreListPayload` (`network/`) | the payload itself; `implements CustomPacketPayload`; `public static final Type<CoreListPayload> TYPE = new Type<>(Farmstorm.id("core_list"))`; the fields `List<Core> cores` and `int total`; the nested `public record Core(ResourceLocation dimension, BlockPos pos, String ownerName, boolean self)` (fixed by this document: the 4 fields are exactly D-143's "dimension + position + whose it is + whether it is one's own") |
| `CoreListBroadcaster` (`network/`, server side only) | assembly, debounce, dispatch per recipient; the single sending entry point calls `PacketDistributor.sendToPlayer` |
| `ClientCoreList` / `ClientCoreListHandler` (`client/` and `client/network/`, client side only) | the client-side snapshot holder (`volatile List<Core> cores` + `volatile int total`, written only by the handler and read only by rendering) / the S2C handler (`enqueueWork` writes the snapshot) |

codec (fixed by this document: 4 fields use the 4-argument `composite`, and the list is capped with `list(64)`):

```java
// Core's codec: 4 fields → the 4-argument overload of StreamCodec.composite
public static final StreamCodec<ByteBuf, Core> CORE_STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, Core::dimension,
        BlockPos.STREAM_CODEC,         Core::pos,
        ByteBufCodecs.STRING_UTF8,     Core::ownerName,
        ByteBufCodecs.BOOL,            Core::self,
        Core::new);

// the whole payload: the list + the true total (total and cores.size() may differ, see 2.1.6)
public static final StreamCodec<RegistryFriendlyByteBuf, CoreListPayload> STREAM_CODEC = StreamCodec.composite(
        CORE_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_CORES)), CoreListPayload::cores,
        ByteBufCodecs.VAR_INT,                                  CoreListPayload::total,
        CoreListPayload::new);
```

**Why the dimension is not written as a `String`**: `ResourceLocation.STREAM_CODEC` has the same origin as `writeResourceLocation`, so the length prefix and the validation are both vanilla's responsibility; hand-assembling a string would be rewriting serialization once (rejected).
**Why `total` is sent separately**: D-143's "at most 8 core rows + 1 `+N` row" (A-34) requires the client to know **the true total**; if only the truncated list were sent, `+N` could only show `64 − 8`, which is wrong (fixed by this document).

Registration (fixed by this document: version string `"1"`, hung on the mod bus, `play` phase only, CLIENTBOUND only):

```java
// in the Farmstorm constructor: modBus.addListener(FarmstormNetwork::onRegisterPayloads);
public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
    event.registrar("1")                       // fixed by this document: the handshake version string; it must move whenever the codec moves
         .playToClient(CoreListPayload.TYPE, CoreListPayload.STREAM_CODEC,
                       (payload, context) -> ClientCoreListHandler.handle(payload, context));
}
```

`ClientCoreListHandler` contains **only one statement**, `context.enqueueWork(() -> ClientCoreList.set(payload.cores(), payload.total()))`, for three reasons:

1. `PayloadRegistrar` has already wrapped it with `MainThreadPayloadHandler` by default (see the table in 2.1.1), and `enqueueWork` on the main thread **executes immediately** (the `IPayloadContext` javadoc verbatim: "For handlers running on the main thread, immediately executes the task"), so there is no extra queueing delay;
2. should someone later use `executesOn(HandlerThread.NETWORK)`, this line is still safe — it writes "must be the main thread" at the call site instead of relying on the registrar's default;
3. the client handler **reads no world, decides nothing and replies to nothing** (D-142's three boundaries); it only replaces one reference, so there is no lock and no race.

**A dedicated server must not load the client class**: the target class inside `(payload, context) -> ClientCoreListHandler.handle(...)` is resolved only when that lambda's synthetic method is invoked, and a CLIENTBOUND handler never runs on the server ⇒ a dedicated server does not hit `NoClassDefFoundError`. This is a design premise fixed by this document, and §7 #6 carries one "a dedicated server can start" assertion to catch it.

#### 2.1.3 The server-side sending entry points and the debounce (fixed by this document)

The four entry points of `CoreListBroadcaster` plus two time constants:

| Item | Value | Reason |
|---|---|---|
| `MIN_INTERVAL_TICKS` | **10** (0.5 seconds) | the same chain of events (place a core → book it → rebuild boundary particles) may fire several requests in the same tick; 10 ticks is a visible delay of at most 0.5 seconds, still inside the "immediate" band for the human eye |
| `RECONCILE_INTERVAL_TICKS` | **100** (5 seconds) | catches the "no event but the list changed" path (an ally switches teams, goes offline / comes online, an event is missed); on a 20-player server that is 20 fingerprint comparisons every 5 seconds, a negligible cost |
| `MAX_CORES` | **64** | the per-packet cap; works together with the size estimate of §2.1.6 |

Request merging rule: `Map<UUID, Long> lastSentTick` + `Set<UUID> pending`; `request(player)` only puts the UUID into `pending`; `ServerTickEvent.Post` flushes them all — a packet really goes out only if `now − lastSentTick ≥ 10`, otherwise it waits for the next tick. **Several requests in the same tick produce one send** (the unit of debounce is "player × packet", not event).

#### 2.1.4 List of push moments (RT-31 requires each one to be verified)

| # | Moment | Mount point (all server-side, game bus) | Recipient | Debounce / note |
|---|---|---|---|---|
| 1 | **Login** | `PlayerEvent.PlayerLoggedInEvent` (`FarmstormGameEvents:58` already has a registration slot for a listener of that name) | that player | request once immediately; if the client has not built its world yet, the reconciliation of #10 catches it |
| 2 | **Reconnect** | same as #1 (the server does not distinguish a first join from a reconnect) | that player | the same path as #1; `PlayerLoggedOutEvent` clears `lastSentTick` and `pending`, so a stale tick number cannot misjudge the debounce |
| 3 | **First join** (a new player entering the server for the first time) | same as #1 | that player | send once even when the list is empty (`total=0`), so the client can tell "no core" from "no packet received" |
| 4 | **Place a core** | the end of `OutpostCoreBlockEntity#setOwner` (`:95-103`): `CoreListBroadcaster.requestAll(server)` | all online players | only the players whose "own list" really changes are affected, but the recipient set is decided by each player's own assembly result, so the request is made uniformly |
| 5 | **Destroy a core** | the end of `OutpostCoreBlockEntity#release` (`:166-170`) + `settleOnRemoval` (`:315-335`) | all online players | same as #4; migration (D-136) has already finished rebinding inside `settleOnRemoval`, so one broadcast is enough |
| 6 | **Migration finished** | same as #5 (migration is a branch of core removal and opens no separate event) | all online players | `release()` and `settleOnRemoval` may each request once; the 10-tick debounce merges them into one send |
| 7 | **Dimension change** | `PlayerEvent.PlayerChangedDimensionEvent` | that player | the list content does not change (cross-dimension and exhaustive, D-143); sending it covers the tiny window in which "the client lost it once while switching worlds"; cost 1 packet |
| 8 | **Respawn** | `PlayerEvent.PlayerRespawnEvent` | that player | same as #7, through the 10-tick debounce |
| 9 | **Ally change** | no event is available (vanilla `Scoreboard` throws no team-change event) ⇒ discovered by the **fingerprint reconciliation** of `RECONCILE_INTERVAL_TICKS = 100` | the players who changed | a delay of at most 5 seconds; the fingerprint = the `hashCode()` of the sorted `(dimension, pos, ownerName, self)` list plus `total`, and a packet goes out only when they differ |
| 10 | **Reconciliation / catch-all** | `ServerTickEvent.Post`, every 100 ticks | the players whose fingerprint changed | catches all three at once: "a missed event", "a dropped packet", "the client was not ready yet at #1" |

**Disconnect catch-all**: on `PlayerLoggedOutEvent` the server clears that player's three records in `lastSentTick` / `pending` / `fingerprint` (not cleaning them makes the first request after a reconnect be judged "just sent" and skipped). On the client, `ClientPlayerNetworkEvent.LoggingOut` clears `ClientCoreList` to an empty list — otherwise, after **switching servers**, one would see the previous server's cores (this is the only client-side state in this version that survives across sessions).

#### 2.1.5 Assembly (server-authoritative)

`CoreListBroadcaster.build(ServerPlayer viewer)`:

1. take `OutpostCoreIndex.of(level)`; iterate over **all owner UUIDs** of that index (a read-only accessor must be added, see §3 row 32);
2. for each owner, compare with the viewer using §2.2.5's **UUID-side ally test**; if not allied, skip the whole block (hostile cores are not sent to the client, D-143);
3. if allied, collect every `(dimension, pos)` of that owner as a candidate; `ownerName` is taken from the index's `ownerNames` (present even when offline, `:55-56`); `self = owner.equals(viewer.getUUID())`;
4. take each candidate core's comparison key: **read it from the index only**, `Entry#gameTime` (the `gameTime` persisted on the index side, see §2.2.10) — **no block-entity read, no chunk load**. When a legacy-save entry lacks the field, take `Long.MIN_VALUE` (treated as the earliest, without fabricating a time) and print one count line at startup;
5. sort: **A-39's three-level comparison key** — `gameTime` ascending → **dimension id** (`dimension.toString()`) → core position lexicographic (`BlockPos#compareTo`: y → z → x); a full three-level tie can only occur in the same dimension at the same cell, which is physically impossible;
6. `total = the number of candidates`; `cores = the first MAX_CORES entries`; send `new CoreListPayload(cores, total)`.

#### 2.1.6 Packet size estimate (fixed by this document)

| Field | Encoding | Bytes |
|---|---|---|
| `dimension` | `ResourceLocation.STREAM_CODEC` (a string, 2 B length prefix + UTF-8) | `2 + len`, `minecraft:overworld` = 19 → **21** |
| `pos` | `BlockPos.STREAM_CODEC` (1 long) | **8** |
| `ownerName` | `STRING_UTF8` (2 + UTF-8) | the name cap is 16 → **≤ 18** |
| `self` | `BOOL` | **1** |
| per-core subtotal | | **≈ 40 B (typical) / ≤ 48 B (upper bound)** |
| `total` | `VAR_INT` | ≤ 5 |
| list length prefix | the count of `ByteBufCodecs.list` | ≤ 3 |

⇒ worst case `64 × 48 + 8 ≈ 3.1 KB`, typical (8 cores) `≈ 0.4 KB`. That is 2 ~ 3 orders of magnitude below vanilla's cap on a custom packet (of the order of 1 MiB, the exact value in §8 ⑥), so neither fragmentation nor compression is needed.

### 2.2 The farmland-core binding table (**a save-format change**)

#### 2.2.1 Current state (checked line by line)

`farmland/FarmlandLedger.java` (275 lines) has this structure today:

| Item | Current state | Lines |
|---|---|---|
| Persisted name | `farmstorm_farmland` → `world/data/farmstorm_farmland.dat` | `:44` |
| The body | `Map<UUID, LinkedHashSet<BlockPos>> byOwner` (**the outer map is a `HashMap`**) | `:51` |
| Runtime override | `@Nullable Integer baseOverride` (**not persisted**) | `:57-58` |
| Quota | `base()` (`:72-74`) / `setBase(int)` (`:76-79`) / `personalCapacity(int,int,int)` (`:82-84`) / `sumOverMembers(int,int,int...)` (`:92-98`) / `capacity(level,player)` (`:101-108`) | as above |
| Used | `used(level,player)` (`:111-121`, the cardinality of the union of coordinates) | as above |
| Bookkeeping | `add` (`:128-135`) / `remove(BlockPos)` (`:141-158`; in the 1.21.1 code the only write point is the core-removal path `OutpostCoreBlockEntity.java:332`, and **after the binding system it is no longer the only write entry**, see §2.2.4 and A-37) | as above |
| Reads | `ownedBy(level, player)` / `ownedBy(level, UUID)` (`:161-177`, the latter resolvable offline) | as above |
| Member set | `members(...)` (`:185-196`, **online players only**) | as above |
| Lazy validation | `validated(...)` (`:203-219`): the block at the coordinate must still be `minecraft:farmland`; an unloaded chunk counts as "valid" | as above |
| Save / load | `save` (`:228-244`): `owners → {uuid → {positions → [{pos: long}]}}`; `load` (`:246-264`) | as above |

**The key fact (it decides how this section is written)**: `byOwner` is a `LinkedHashSet` inside and a `HashMap` outside — "insertion order is record order" holds **inside one UUID only**. Besides that, nobody calls a subtract entry when a player digs out his own farmland (in the 1.21.1 code the write point is on the core-removal path); every subtraction relies on `validated()`'s lazy discard.

#### 2.2.2 The v2 structure (fixed by this document)

```java
// binding: one farmland block ↔ exactly one core
public record Binding(BlockPos pos, UUID owner, ResourceLocation coreDimension, BlockPos corePos) {}

// the core's key (unique across dimensions)
public record CoreKey(ResourceLocation dimension, BlockPos pos) {}

// a leftover entry with no core: a farmland entry has only pos (§2.2.2 point 2), so it carries no dimension either
public record Orphan(BlockPos pos, UUID owner) {}

// three bodies; bindings is the single truth, byCore is the in-memory reverse index, orphans are the leftovers a legacy save could not match
private final LinkedHashMap<BlockPos, Binding> bindings = new LinkedHashMap<>();
private final Map<CoreKey, LinkedHashSet<BlockPos>> byCore = new HashMap<>();     // derived from bindings, not persisted
private final LinkedHashMap<BlockPos, Orphan> orphans = new LinkedHashMap<>();     // leftovers with no core; the persisted order is diagnostic only
```

Four design trade-offs (all fixed by this document):

1. **`byOwner` is no longer persisted** (D-134 requires it): `binding.owner` already says "whose field it is", and `byOwner` is only derived by filtering on `owner` at query time — two sources drift.
2. **A farmland coordinate still carries no dimension** (as in v1): in a 1.21.1 legacy `.dat` `pos` is a single long, and suddenly adding a dimension field would fork the migration; besides, "farmland is in the same dimension as its core" is already guaranteed at bind time by `inOwnRange`'s same-dimension filter (`FarmlandGuard.java:92-107`, the same-dimension filter at `:94-96`), so the dimension can be carried by `coreDimension`. **Known boundary**: when the same `(x,y,z)` has one farmland block in the overworld and one in the nether, both v1 and v2 collide (§9 item 5 registers it; it is not fixed in this version).
3. **`orphans` is persisted** (fixed by this document; reason: D-137 says "what cannot be matched keeps its farmland untouched", and if their accounts were dropped there would be no chance to backfill on the next startup, so "keep untouched" would degrade into "forgotten forever"; persisting also makes the backfill algorithm naturally idempotent). `orphans` uses a `LinkedHashMap`, but **its persisted order is diagnostic / display only and takes part in no decision** — the backfill order is sorted by A-47's total order (§2.2.8) and has nothing to do with the insertion order here.
4. **`binding.owner` and the core's owner may differ**: D-136's migration criterion is "allied with the **farmland's owner**", while the farmland's owner is the tiller and the core's owner is the placer, and the two may be different people in a team ⇒ the owner must be stored per entry and cannot be back-derived from the core.

The NBT shape (fixed by this document: all key names in lower camel case, in the same style as `OutpostCoreIndex`'s `owners/entries/dimension/pos`):

```
dataVersion: 2                       // int; missing means 1 (= the 0.1.0 format)
bindings: [                          // ListTag<TAG_COMPOUND>, the order is insertion order
  { pos: long, owner: "uuid-string", coreDimension: "minecraft:overworld", corePos: long }
]
orphans: [                           // ListTag<TAG_COMPOUND>; the batch a legacy save failed to backfill (persisted order is diagnostic only)
  { pos: long, owner: "uuid-string" }
]
```

#### 2.2.3 The version number and the backward-compatible read (fixed by this document)

```java
private static FarmlandLedger load(CompoundTag tag, HolderLookup.Provider registries) {
    FarmlandLedger ledger = new FarmlandLedger();
    int version = tag.contains("dataVersion") ? tag.getInt("dataVersion") : 1;   // missing = v1
    if (version >= 2) {
        readBindings(tag, ledger);        // validate item by item: an entry whose coordinate is no longer farmland is dropped outright (the same measure as validated)
        readOrphans(tag, ledger);
        // if a legacy v1 section coexists as well (a hand-assembled save), merge it all into orphans, never into bindings (do not guess bindings)
        readLegacyOwners(tag, ledger);
    } else {
        readLegacyOwners(tag, ledger);    // v1: owners → {uuid → {positions:[{pos}]}} all go into orphans
    }
    ledger.rebuildByCore();               // rebuild the reverse index from bindings
    return ledger;
}
```

Three disciplines:

1. **Only up, never down**: v2's `save` always writes `dataVersion: 2` and **no longer writes** the `owners` section. An old build (0.1.0) reading a v2 save gets an empty ledger — in a downgrade that is equivalent to "every piece of farmland becomes unbound", which shovels no world and is an acceptable direction for a downgrade (the alternative "write both formats" is rejected: writing both returns to two sources).
2. **Every v1 record is read as `orphans`** (D-137's measure): `owners` carries no core information, so the binding cannot be guessed; taking it as a binding would be inventing data.
3. **Validate on read**: an entry whose `pos` block is not `minecraft:farmland` and whose chunk is loaded is dropped on the spot (the same criterion as `validated`), so coordinates dug out long ago do not enter the capacity statistics.

#### 2.2.4 When `setDirty()` is called (named one by one)

| Action | `setDirty()`? | Reason |
|---|---|---|
| `bind(...)` (bind one new entry) | yes | the persisted content changed |
| `unbind(pos)` (remove one entry) | yes | as above |
| `migrate(...)` (rebind) | yes | one binding's `corePos` changed |
| `adoptOrphan(...)` (a legacy entry backfilled into a binding) | yes | one fewer entry in `orphans`, one more in `bindings` |
| `rebuildByCore()` | **no** | purely an in-memory derivation, not persisted |
| `validated()` dropping a bad entry | yes (**only if an entry was really dropped**) | the same discipline as the current `:214-217`: no change, no write |
| `get` / `of` / any read | no | — |

> The `setDirty()` inside the current `remove(BlockPos)` (`:154-156`) and the one inside `add` (`:133`) keep their present semantics: v2's `remove` = `unbind` first, then clear `byCore`, with a single `setDirty`.

#### 2.2.5 The ally test (UUID side, valid offline)

D-138 requires "look it up from the UUID index instead". The only existing chain of the ally test is `DeployedOwnership.isAlly(level, ownerUuid, ownerName, Player target)` (`util/DeployedOwnership.java:48`) → `TeamResolver#isAlly(level, Owner, Player)`, and **target must be a `Player` entity**, which an offline player cannot supply. This document therefore fixes: add an Owner ↔ Owner overload to `TeamResolver`, let the `Player` version delegate to it, and **open no third criterion**:

```java
// team/TeamResolver.java, new (fixed by this document)
boolean isAlly(ServerLevel level, Owner owner, Owner target);

// team/ScoreboardTeamResolver.java: move the existing logic into the new overload verbatim; the Player version becomes a one-line delegation
@Override public boolean isAlly(ServerLevel level, Owner owner, Player target) {
    return isAlly(level, owner, new Owner(target.getUUID(), target.getScoreboardName()));
}
@Override public boolean isAlly(ServerLevel level, Owner owner, Owner target) {
    if (owner.uuid().equals(target.uuid())) return true;              // oneself is always true (currently :29-31)
    Optional<String> a = teamName(level, owner);                      // resolvable offline: the scoreboard member list is looked up by name
    if (a.isEmpty()) return FALLBACK_PUBLIC.equals(FarmstormConfig.untamedFallback());  // currently :33-37
    Optional<String> b = teamName(level, target);
    return b.isPresent() && a.get().equals(b.get());
}
```

`teamName(...)`'s implementation already looks up `Scoreboard#getPlayersTeam` by `ownerName` (`:21-25`) and **works offline too** — this is the technical ground on which D-138 stands, not a new capability.

**A known semantic change (fixed by this document)**: for a player with no team, under `untamedFallback = "self"` (the default), `isAlly(self, self) = true` and `false` towards anyone else; set to `"public"` and everyone is allied to everyone. This is equivalent item by item to the current `Player` version (exactly the same block of logic moved over) and changes no existing gameplay.

**Where the trample surface is closed off (A-45)**: the "a binding exists ⇒ protected" rule above **applies to the trample surface only** (`protectsFromTrample`, `:120-134`). **The quota gate (tilling) and the HUD overlay (the overlay) still use the live ally test**: tilling goes through `FarmlandGuard.check`'s UUID-side criterion (§2.2.6 / §2.2.7), and the overlay goes through the live `isAlly` at each send (§2.1.5) — after a team change both change **immediately**, and only the trample surface keeps "a binding exists ⇒ protected", because the trample event itself carries no inquirer (document 29's B-18 has already been narrowed per A-45).

#### 2.2.6 Capacity and used (D-139)

| Quantity | Formula | Note |
|---|---|---|
| per-core capacity | `outpost_core.farmlandBonus` (default 16, range 0 ~ 1024) | **no new key** (D-134); at `0` the core can bind no farmland at all |
| a core's free slots | `farmlandBonus − byCore.get(key).size()` | the cardinality of the reverse set; `byCore` is derived from `bindings` and **not persisted** |
| the team capacity `capacity(level, player)` | `Σ over the allied cores: farmlandBonus` = `bonus × the number of allied cores` | "an allied core" = every entry of `OutpostCoreIndex` whose owner and player are allied (**including other dimensions**, and offline too); the index needs a public `count(MinecraftServer, UUID)` overload (§3 row 32) |
| used `used(level, player)` | the number of entries in `bindings` whose `owner` and player are allied | the same source as capacity (D-139); `orphans` **do not count** (no binding, no slot — D-137's "merely unprotected") |
| a single core's occupancy | `byCore.get(key).size()` | used by tilling to judge "there is still a free slot"; **not** a filter over the whole `bindings` table (that degrades into an O(n) full scan) |

**One key, two meanings** (RT-33): `outpost_core.farmlandBonus` is at once "the per-core capacity" and "the quota base". This document fixes: write it into the `comment(...)` of the line at `FarmstormConfig.java:911` as "this key decides both the number of farmland blocks each core can manage and the team quota base (capacity = this key × the number of allied cores)", **without changing the key name and without adding a key**.

#### 2.2.7 Tilling binds a core (D-135)

```
The state machine of FarmlandGuard.check(level, player, pos) (still three states):
  1) range gate: does pos fall inside any allied core's 3×3 chunks? no → NO_CORE
  2) candidate set: among the allied cores above that cover pos, filter( byCore free slots > 0 )
  3) the candidates are empty → QUOTA_FULL (the meaning moves from "the team quota is full" to "the nearest allied core is full"; the key name does not change, see §9 item 4)
  4) take the smallest distSqr(core.pos, pos) among the candidates; on a tie use A-39's three-level key: placedGameTime ascending → dimension id → core position lexicographic (BlockPos#compareTo)
  5) return ALLOW + the chosen core (add an overload carrying a result, e.g. record Decision(Verdict verdict, @Nullable CoreKey core))
```

`FarmstormGameEvents.onRightClickBlock` (`:194-217`) replaces `ledger.add(level, player, pos)` in the `ALLOW` branch with `ledger.bind(player.getUUID(), new CoreKey(level.dimension().location(), decision.core()), pos)`.

**The distance measure (fixed by this document)**: `BlockPos#distSqr(BlockPos)` (the squared three-dimensional Euclidean distance between the two block centres). Reason: D-135 says only "the nearest", while the 3×3 chunk test is itself Chebyshev (`:139-145`); if the distance were Chebyshev too, the same "nearest" would give several ties along the diagonal and the bound core would be unpredictable. In one dimension the Euclidean distance almost never ties, and the remaining ties go to the ruling below.

**The tie criterion (A-39, pinned down in this section and used by both migration and backfill)**:

1. first level, `placedGameTime` **ascending** (the earlier-placed core first);
2. second level, **dimension id**: `coreKey.dimension().toString()` ascending (**the third level A-39's final text added**: two cores in different dimensions may be placed in the same tick at exactly the same coordinate, and the two levels tie at this step, so one more level is needed to separate them);
3. third level, **core position lexicographic**: `BlockPos#compareTo` (the existing implementation of `Vec3i`: y first, then z, then x at the end);
4. **no reliance on the insertion order of any `Map` / `List`**: `OutpostCoreIndex.byOwner` is a `HashMap` (`:53`) and the outer map of `FarmlandLedger.byOwner` is a `HashMap` too (`:51`), so the traversal order across owners is unstable; hanging "placed earlier" on the insertion order would make the same save give different bound cores on two startups.

> **The division of labour between the three criteria (A-47 replaces A-36, do not mix them up)**: **A-47 governs "in what order the backfill processes entries"** — a `BlockPos` single-level total order (y → z → x, see §2.2.8), because a ledger entry has only `pos`; **A-39 governs "how candidate cores are compared with one another"** — every decision of the form "pick one among several candidate cores" (tilling binds a core, migration, backfill) uses the **three-level key** above; **the two key lengths are deliberately different, not a slip of the pen**: a core has a dimension (`OutpostCoreIndex.Entry` carries `dimension`) and farmland does not (`FarmlandLedger`'s entries have only `pos`, `:46-48`). Neither place may look at the iteration order of any `Map` / `List`.

#### 2.2.8 The startup backfill (D-137, pseudo-code)

```
backfill(server):                                  // runs once only, after the server has started
  total = orphans.size(); bound = 0; kept = 0
  work = orphans.values().sortedBy(BlockPos#compareTo)   // ★ A-47 rev.2: farmland has only pos, so one level is already a total order (y → z → x)
                                                        //   unlike the "candidate-core three-level key" of §2.2.7: a core has a dimension, farmland does not
  for (item in work):
      if (chunk loaded && block is no longer farmland)  → orphans.remove(item.pos) ; continue   // the block is gone, the entry is void
      candidates = the cores allied to item.owner
                   where inRange(core, item.pos) && byCore(core).size() < bonus
      if (candidates.isEmpty()) → kept++ ; continue      // ★ cannot be matched: keep the block untouched, and do not touch the world either
      core = candidates.minBy(distSqr, then A39KEY)       // A39KEY = (gameTime, dimension id, BlockPos#compareTo)
      bind(item.owner, core, item.pos); orphans.remove(item.pos); bound++
  LOGGER.info("耕地补建：绑定 {} 块 / 保持未绑定 {} 块（老档共 {} 块，按 BlockPos 全序）", bound, kept, total)
```

Three hard constraints:

1. **The backfill never writes a block**: the whole passage has no `setBlock`, which is how "what cannot be matched keeps its farmland untouched" holds at the code level (D-137).
2. **The backfill runs once**: it is called by a server startup hook (on the same layer as `Farmstorm`'s existing initialization: after `FarmstormGameEvents.register(...)` and before the first `ServerTickEvent.Post`, behind a one-shot `boolean` gate; this document fixes "run once inside the first `ServerTickEvent.Post`" rather than in the constructor, because `MinecraftServer` is not ready at constructor time).
3. **The backfill's processing order = the `BlockPos` single-level total order** (A-47 rev.2): `work` is first sorted by `BlockPos#compareTo` (y → z → x) and then processed entry by entry; neither `orphans`' persisted order nor `byOwner`'s `HashMap` iteration order **takes part in any decision**. A ledger entry has only `pos`, and one `pos` appears at most once in the ledger ⇒ a single-level comparison is already a total order; it is **deliberately different** from the candidate-core three-level key of §2.2.7 (a core has a dimension, farmland does not). Capacity 16 is a hard cap, and changing the order changes the result ⇒ the order is part of the acceptance surface (§7 #19's N and M must be reproducible).

#### 2.2.9 Migration (D-136, pseudo-code)

```
settleOnRemoval()   // OutpostCoreBlockEntity:315-335, after the change
  ...(keep the "really removed / unloaded" criterion of :316-326, not one character changed)...
  CoreKey dead = new CoreKey(level.dimension().location(), corePos)
  List<Binding> mine = ledger.bindingsOf(dead)               // process only what stands under this core
  Map<BlockPos, CoreKey> rebind = new LinkedHashMap<>()
  List<BlockPos> toDirt = new ArrayList<>()
  List<BlockPos> unbindOnly = new ArrayList<>()
  for (b in mine):
      candidates = the allied cores (allied to b.owner)
                   where core != dead
                     && inRange(candidate, b.pos)
                     && byCore(candidate).size() < bonus
      if (!candidates.isEmpty()):
          rebind.put(b.pos, nearest(candidates))   // nearest = distSqr → A39KEY (three levels)
      else if (chunk already in memory && block is still farmland):
          toDirt.add(b.pos)                                  // cannot migrate + can see it => turns to dirt (D-136)
      else:
          unbindOnly.add(b.pos)                              // defensive branch: only possible with a missing ticket / anomaly; unreachable normally (A-60)
                                                             //   => remove the binding only, do not touch the world (no setBlock, no chunk load)
  // compute everything first, then touch the world (following the existing reason of :312-313: avoid changing the ledger while iterating it)
  for ((pos, core) in rebind): ledger.migrate(pos, core)     // rebind + keep byCore in sync
  for (pos in toDirt):    server.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL)
                          ledger.remove(pos)
  for (pos in unbindOnly): ledger.remove(pos)                // remove the account only, do not touch the block
  this.release()                                             // already includes ticket release + broadcast (§2.5)
```

**A-35's branch is the one most easily written wrong in this version**: when a core is destroyed, if the chunk of some piece of farmland is **not in memory at all**, the implementer cannot confirm whether that cell is still farmland; at that point **degrade to "loses protection" rather than "becomes dirt"**. The reason runs the same way as the existing trade-off at `:304-310`: the cost of a misjudged direction is asymmetric — shovelling one block too few merely loses protection, shovelling one block too many is data loss. On the implementation side it is **forbidden** to force-load a chunk with `getChunk(...)` just to shovel that block (that would load a batch of chunks in a chain, heavier than the semantics of a "temporary ticket").

**"Nearest first" follows the same order as tilling** (`distSqr` nearest → the three-level comparison key `placedGameTime` → dimension id → coordinate lexicographic, A-39), for the reason above: two distance measures would make "bound to the nearer one" and "migrated to the nearer one" give different answers (fixed by this document).

**Note that "the farmland's owner is not notified"** (a D-136 supplement): migration writes no chat line and sends no packet-change notice; the rebinding shows up only in the overlay's next refresh.

#### 2.2.10 The carrier of the comparison key: `placedGameTime` lands on the index side (A-39)

**Conclusion**: the first level of the comparison key, `placedGameTime`, is **carried by an `OutpostCoreIndex` entry and persisted there**; the comparison path reads the index only, with **no block-entity read and no chunk load**.

**Data structure**: `OutpostCoreIndex.Entry` gains a `long gameTime` — `record Entry(ResourceLocation dimension, BlockPos pos, long gameTime)`; each entry is persisted as `{dimension, pos, gameTime}` (one more key inside the existing `entries` list at `OutpostCoreIndex:209-238`).

**Write moments (four, named one by one)**:

| # | Moment | What is written | Landing point |
|---|---|---|---|
| 1 | **Placement** | write the **same** `this.placedGameTime` of `OutpostCoreBlockEntity#setOwner` (`:95-103`) into both the BE and the index: `record(server, placer, pos, placedGameTime)` | the same method, the same variable, the same tick ⇒ the two copies cannot drift |
| 2 | **Destruction** | the entry disappears together with `release()` → `index.remove(...)`, and `gameTime` needs no separate maintenance | `OutpostCoreBlockEntity:166-170` |
| 3 | **Loading a save** | `load` reads `gameTime`; when it is **missing** (the 0.1.0 index has no such field) take `Long.MIN_VALUE` | `OutpostCoreIndex:240-266` |
| 4 | **Backfill** | **writes nothing** — the backfill touches farmland bindings only, produces no core and changes no core comparison key | §2.2.8 |

**How the comparison value is obtained when a candidate core's chunk is not loaded**: **from the index**. The index is a `SavedData` (`farmstorm_outpost_core` at `OutpostCoreIndex:41`), and reading it requires no chunk in memory; therefore the value path contains no `getChunk` / `getBlockEntity` / `hasChunkAt` call — "**never load a candidate core's chunk for the sake of comparison**" is structural (not a convention), and that is how this hard constraint of A-39 holds.

**Why not read it from the block entity (rejected)**: a BE exists only while its chunk is in memory (`level.getBlockEntity` returns `null`). Even though D-145's ticket will eventually load every core's chunk, **loading happens in some order** (the world has just opened, the ticket has not taken effect yet, the dimension is not ticking), so the comparison value would drift with the load timing, and the same save could give a different order on two startups — exactly the irreproducibility A-39 bans.

**The boundary for a legacy save (a 0.1.0 index)**: entries without `gameTime` all take `Long.MIN_VALUE` (treated as "earlier than every known time"), and legacy cores are separated by the second and third levels; **there is no "back-write it from the BE while loading"** — that would make the value depend on "whether the chunk happened to be loaded at the time" and destroy reproducibility instead. One count line is printed at startup: `核心比较键缺失（老档）：N 条`.

**Observability (A-39's acceptance surface; §7 #9 / #18 assert exactly these lines)**:

1. after startup and after the backfill, one INFO line each: `核心比较键指纹 <hex16>（N 条）`; the content = the first 16 digits of the SHA-256 over the lines `uuid gameTime dimension pos` after sorting by `uuid` (the per-item content at DEBUG: `核心比较键 <uuid> <gameTime> <dimension> <pos>`).
2. at the same moment one INFO line: `耕地绑定指纹 <hex16>（M 条）`; the content = the first 16 digits of the SHA-256 over the lines `pos|coreDimension|corePos` after sorting by `(pos, coreDimension, corePos)`.
3. both fingerprints depend only on the save content and the comparison key, not on the traversal order or the load timing ⇒ "the same save started twice with the same candidate set ⇒ the same set of bindings" can be checked by comparing two log lines.

### 2.3 Deleting the server-side command tree

| # | What is deleted | Where | Consequence after deletion |
|---|---|---|---|
| 1 | `command/FarmstormCommands.java`, **the whole 58-line file** | the whole file | `/farmstorm farmlandlimit` disappears; `base` loses its only write entry (D-139) |
| 2 | the server-side command registration, **3 places** | `event/FarmstormGameEvents.java:4` (the import), `:62` (`gameBus.addListener(FarmstormGameEvents::onRegisterCommands)`), `:71-73` (the body of `onRegisterCommands`, `:72` calls `FarmstormCommands.register`) | the server's `RegisterCommandsEvent` no longer has a listener from this mod |
| 3 | config, **the whole `farmland` group** (1 key) | `config/FarmstormConfig.java:904-908` (`push` / `comment` / `translation` / `defineInRange` / `pop`) + the field declaration `:226` | `farmstorm-server.toml` no longer has a `[farmland]` section; the group count goes 59 → 58 |
| 4 | language keys, **2 entries** | `command.farmstorm.farmlandlimit.set` (`zh_cn.json:64`), `farmstorm.configuration.farmland.limitPerPlayer` (`zh_cn.json:145`); the generating blocks `datagen/ModLangProvider.java:393-394` and `:404-406` are each deleted | language keys 315 → 323 (a net +8, see §4.3) |
| 5 | `FarmlandLedger`'s base trio | `:57-58` (the `baseOverride` field), `:72-74` (`base()`), `:76-79` (`setBase`), `:82-84` (`personalCapacity`), `:92-98` (`sumOverMembers`) | `capacity` becomes "bonus × the number of allied cores", with no base parameter any more |

**After the deletion `/farmstorm` does not exist on the server**: running `/farmstorm` in a dedicated server's console gives "unknown command". **But in single-player / on the client `/farmstorm` still exists** — that is D-144's client command `/farmstorm hud …` (registered into the client dispatcher by `RegisterClientCommandsEvent`), and it **shares no registration point and no root-node semantics** with the server-side command deleted in §2.3. §7 #12's assertion is written on that distinction (fixed by this document: the distinction must go into acceptance, or "single-player still types `/farmstorm`" would be misjudged as a failed deletion).

### 2.4 The client components (**the project's first batch**)

Current state: the `client` package has 21 classes (§1.1), `KeyMapping` registrations **0**, CLIENT-type config **0**, client commands **0** (fact card §1.5, re-checked by this document). This version adds **6** classes (row 2 of the table merges 2: `ClientCoreList` in `client/`, `ClientCoreListHandler` in `client/network/`):

| New class | Location | Responsibility |
|---|---|---|
| `FarmstormKeys` | `client/` | the 2 `KeyMapping` constants + `RegisterKeyMappingsEvent` registration |
| `ClientCoreList` / `ClientCoreListHandler` | `client/` / `client/network/` | the payload snapshot / the S2C handler (`enqueueWork` writes the snapshot; client-only, see §2.1.2) |
| `CoreHudRenderer` | `client/` | overlay drawing (`RenderGuiEvent.Post`) |
| `CoreHudDragScreen` | `client/` | the `Screen` for drag positioning |
| `CoreHudClientCommands` | `client/` | registration and execution of `/farmstorm hud <on\|off\|reset>` |

#### 2.4.1 The 2 `KeyMapping`s (fixed by this document)

```java
public static final KeyMapping TOGGLE = new KeyMapping(
        "key.farmstorm.core_hud.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H,
        KeyMapping.CATEGORY_GAMEPLAY);
public static final KeyMapping DRAG = new KeyMapping(
        "key.farmstorm.core_hud.drag",   InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J,
        KeyMapping.CATEGORY_GAMEPLAY);
```

- the constructor used is the already-checked `KeyMapping(String, InputConstants.Type, int, String)` (checked with `javap`: 1.21.1 has 6 constructors, and this version takes the "string + type + keycode + category" one);
- **the category reuses `KeyMapping.CATEGORY_GAMEPLAY`** (fixed by this document): a category of one's own would need one more `key.categories.*` language key, while reusing the vanilla category costs only "the keys show up on vanilla's Controls page" — 1 language key fewer, and 1.21.1's key screen is paginated by category anyway (a self-made category is rejected: one more language key, and it would not square with §4.3's 323);
- **the default keys are H / J** (fixed by this document): in 1.21.1's vanilla default key table neither H nor J is bound (vanilla occupies W/A/S/D, space, Shift, Ctrl, E, Q, F, T, Tab, the middle mouse button, `/`, P, F5, F11, L, 1~9 and so on); two adjacent non-function letter keys make "open the overlay + enter drag" easy to press in sequence. §8 ⑤ keeps one runtime confirmation of "a key clash with another mod".

Registration (**mod bus**; `RegisterKeyMappingsEvent` has been checked to implement `IModBusEvent`, and `@EventBusSubscriber` has a `bus()` attribute):

```java
@EventBusSubscriber(modid = Farmstorm.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FarmstormKeys {
    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
        event.register(DRAG);
    }
}
```

Consumption happens in the existing client tick hook (`ClientTickEvent.Post` at `client/FarmstormClient.java:78-81`):

```java
while (FarmstormKeys.TOGGLE.consumeClick()) { toggleHud(); }         // toggle hud.enabled + write to disk + action-bar echo
while (FarmstormKeys.DRAG.consumeClick())   { openDragScreen(); }    // Minecraft#setScreen(new CoreHudDragScreen())
```

#### 2.4.2 CLIENT config, 3 keys (**the first `ModConfig.Type.CLIENT`**)

Create `config/FarmstormClientConfig.java` (**not inside `FarmstormConfig`**: the two specs differ in lifecycle and in loading side, and mixing them in one class would complicate the `bake()` semantics of the existing 166 keys):

```java
public static final ModConfigSpec SPEC;
public static ModConfigSpec.BooleanValue hudEnabled;
public static ModConfigSpec.IntValue hudX;
public static ModConfigSpec.IntValue hudY;

static {
    ModConfigSpec.Builder b = new ModConfigSpec.Builder();
    b.push("hud");
    hudEnabled = b.comment("浮窗开关（客户端本地设置；按键与 /farmstorm hud 都会改它）")
                  .translation("farmstorm.configuration.hud.enabled")
                  .define("enabled", true);
    hudX = b.comment("浮窗左上角的 GUI 像素 x（绘制时还会按屏幕宽二次钳制）")
            .translation("farmstorm.configuration.hud.x")
            .defineInRange("x", 4, 0, 3840);
    hudY = b.comment("浮窗左上角的 GUI 像素 y（绘制时还会按屏幕高二次钳制）")
            .translation("farmstorm.configuration.hud.y")
            .defineInRange("y", 4, 0, 2160);
    b.pop();
    SPEC = b.build();
}
```

- the registration point: one line after `Farmstorm.java:56` in the `Farmstorm` constructor, `container.registerConfig(ModConfig.Type.CLIENT, FarmstormClientConfig.SPEC);` (**no client-only class reference may appear in `Farmstorm.java`**, so this line references `FarmstormClientConfig` only — that class depends on `ModConfigSpec` alone and is a common class);
- **where it is generated**: `<gameDir>/config/farmstorm-client.toml` (`gameDir` = `run/` or the directory given by `-PfsRunDir=`). It sits beside `farmstorm-server.toml` and **does not go into the server one**;
- **the 3 defaults**: `enabled = true`, `x = 4`, `y = 4` (D-144 item 3 + the `(4,4)` noted under the table in fact card §2);
- **the value ranges of `x` / `y`** (fixed by this document): `0 ~ 3840` / `0 ~ 2160` (the usual upper bound of GUI pixels on a 4K landscape display). The real boundary is a second clamp to `guiWidth − the panel width` / `guiHeight − the panel height` at drawing and dragging time; the config's int range only blocks obviously bad values (the alternative "use `0 ~ 1920`" is rejected: on a 4K screen it would pin the overlay to the left half forever);
- **writing to disk at runtime**: `FarmstormClientConfig.HUD_ENABLED.set(v); FarmstormClientConfig.SPEC.save();` (`ConfigValue#set(T)` and `ModConfigSpec#save()` have both been checked with `javap` to exist). **Do not** hand-write `Files.write`.

#### 2.4.3 The client command `/farmstorm hud …`

Registration: `RegisterClientCommandsEvent` (checked to exist, `net.neoforged.neoforge.client.event`, **game bus, client only**, `getDispatcher()` / `getBuildContext()`). Hanging it on `client/FarmstormClient.java`'s `@EventBusSubscriber(Dist.CLIENT)` is enough (that class is already a game-bus subscriber):

```java
@SubscribeEvent
public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
    CoreHudClientCommands.register(event.getDispatcher());
}

// CoreHudClientCommands
dispatcher.register(Commands.literal("farmstorm").then(Commands.literal("hud")
        .then(Commands.literal("on")   .executes(ctx -> set(ctx.getSource(), true)))   // feedback: command.farmstorm.hud.on
        .then(Commands.literal("off")  .executes(ctx -> set(ctx.getSource(), false)))  // feedback: command.farmstorm.hud.off
        .then(Commands.literal("reset").executes(ctx -> reset(ctx.getSource())))));    // feedback: command.farmstorm.hud.reset
```

- **permission**: no `requires(...)` (a client command changes only the local display, and a permission would shut out everyone but single-player creative players);
- **feedback**: `source.sendSuccess(() -> Component.translatable("command.farmstorm.hud.on"), false)` — `false` = not broadcast to chat, shown locally only (fixed by this document: the opposite of the `true` at the current `FarmstormCommands:53`, because this is a local switch and there is no need for "others must know");
- **`reset`**: writes `x` / `y` back to `4` / `4` and calls `save()`; `enabled` is untouched (fixed by this document: `reset` means "reset the position", not "restore the default switch"; D-144 gives only the three action names, and its boundary is fixed here).

#### 2.4.4 Overlay rendering (`CoreHudRenderer`)

The registration shape follows `client/WildHunterHud.java`: `@EventBusSubscriber(modid, value = Dist.CLIENT)` + `@SubscribeEvent public static void onRenderGuiPost(RenderGuiEvent.Post event)` (`:19`, `:40-41`), with all drawing going through `GuiGraphics#fill` (`:60-67`) and `#drawString` (signature checked: `drawString(Font, String, int, int, int, boolean)`).

The preconditions (**only one point differs from `WildHunterHud:46`**, fixed by this document):

```java
Minecraft mc = Minecraft.getInstance();
if (mc.player == null || !FarmstormClientConfig.HUD_ENABLED.get()) return;      // no player / switch off
if (mc.options.hideGui && !isDragging) return;                                   // hideGui has the highest priority, the drag state is the exception
if (mc.screen != null && !(mc.screen instanceof CoreHudDragScreen)) return;      // draw nothing while another Screen is open
if (ClientCoreList.cores().isEmpty()) return;                                    // 0 cores, no panel
```

**Why the drag state is the exception**: `WildHunterHud:46`'s `minecraft.screen != null` makes the HUD disappear entirely while a Screen is open, but drag mode must let the player **see what he is dragging** (a D-144 supplement has already made this a technical necessity). In the same way `hideGui` (F1) is released in the drag state too — the player pressed J to come in and see it.

Layout (**A-41 prevails**: row height **9** / row spacing **1** / padding **2**, panel height **10n + 3** ⇒ 8 rows 83 px, 9 rows 93 px; A-34's 93 px is its **equivalent corollary**, and the two decompositions must not coexist. The colours are taken from D-143 and the note under the fact card's table; document 29 fixes the glyph and colour details only and **may not change the row count, the row height or the panel height**):

| Item | Value | Reason |
|---|---|---|
| row height / row spacing / padding | **9 px / 1 px / 2 px** (`Font#lineHeight` = 9, checked) | **A-41**: the same measure as document 29 §5.6; the decomposition of +3 follows document 29 §5.6, and this document does not define it independently |
| panel width | `max(font.width(row)) + 4` (computed per frame, not cached; 2 px on each side) | the name length varies; `Font#width(String)` has been checked |
| panel height | `10n + 3` (= `n × (9 + 1) + 3`) ⇒ **8 rows = 83 px, 9 rows = 93 px** | A-41's equivalent corollary; 8 core rows + 1 overflow row = at most 9 rows = **93 px** (A-34) |
| maximum row count | **9 rows** (8 core rows + 1 `+N` row) | A-34 |
| background | `0x80000000` (translucent black) | the same family as `WildHunterHud`'s `COLOR_EMPTY = 0x60101010` but blacker, so white text stays legible |
| border | `0xFF000000`, 1 px (`fill` four times) | the same method as `WildHunterHud:60` |
| oneself | `0xFF55FF55` (green) | D-143 / the note under the fact card's §2 table |
| allies | `0xFFFFFFFF` (white) | as above |
| row format | `#%d [%s] (%d, %d, %d) %s` | item 1 = the 1-based index (**numbered over the full sort before truncation**, so the numbers stay contiguous when there is a `+N`), item 2 = the **path** of the dimension id (`overworld` / `the_nether` / `the_end`, no new language key), item 5 = `gui.farmstorm.core_hud.self` for oneself and `ownerName` for an ally |
| overflow | row 9 draws `"+" + (total − 8)`, **taking one row** (A-34: it does not crowd into the first 8 rows) | D-143 item 4 + A-34; `+N` is a pure number concatenation, **no new language key** (fixed by this document: the language keys already add up to 323, and `+N` is the same symbol in every language) |
| drawing origin | `(hudX, hudY)`, clamped to `[0, guiWidth − width] × [0, guiHeight − height]` before drawing | after a screen-size change (window resize / GUI scale) the old coordinates may be out of bounds |

#### 2.4.5 The drag `Screen` (`CoreHudDragScreen`)

| Item | Design (fixed by this document) |
|---|---|
| opening | `Minecraft#setScreen(new CoreHudDragScreen())` (the world is not paused, but `isPauseScreen()` returns `true` ⇒ single-player pauses by itself, D-144 item 4) |
| hit test | `mouseClicked(mx, my, button)`: if `(mx,my)` falls inside the panel rectangle → `dragging = true`, recording `grabDx = mx − hudX` and `grabDy = my − hudY`; otherwise ignored (the screen is not closed) |
| dragging | `mouseDragged(mx, my, button, dx, dy)`: `hudX = clamp(mx − grabDx)`, `hudY = clamp(my − grabDy)`; the clamp range is the same expression as §2.4.4's drawing clamp |
| release | `mouseReleased(...)` → `dragging = false` (**no disk write**: one drag triggers hundreds of position changes before the release, and writing every frame is pointless) |
| saving | `onClose()` (ESC / close) → write the current `hudX` / `hudY` into config and `SPEC.save()`; **no** cancel path exists (D-144 gives only "ESC saves") |
| background | `renderBackground`'s default implementation is enough (1.21.1's `Screen#renderBackground` draws the blur / darkening); the panel body is **not** drawn — the panel is the responsibility of `CoreHudRenderer`'s drag exception, which guarantees "what you see while dragging is the final result" |
| relation to `hideGui` | the drag state ignores `hideGui` (F1): entering drag is entering edit state (§2.4.4 precondition 2) |
| relation to `screen != null` | only `CoreHudDragScreen` itself is the exception; while any other Screen is open the overlay is not drawn (including `ConverterScreen` and the chat bar) |
| effect of the single-player pause | `ClientTickEvent` keeps running while paused (the client tick is unaffected by pausing), but dragging depends on mouse events only and not on ticks, so pausing does not affect the feel |

#### 2.4.6 Where `farmstorm-client.toml` is generated and its lifecycle

- on the client's first start it is created by the `ModConfig.Type.CLIENT` loader: `run/config/farmstorm-client.toml`; a dedicated server **does not** generate this one (the CLIENT type is loaded on the client only);
- the content = the 3 keys of the `[hud]` section (§4.2);
- relation to `farmstorm-server.toml`: **neither references the other**. Copying a server toml onto a USB stick and giving it to someone does not carry the overlay position, and the other way round;
- after a manual edit of the toml, restarting the client makes it effective (the same rule as the server config: `ConfigValue#get()` reads the loaded value).

### 2.5 Temporary runtime loading of the chunk a core sits in (D-145)

#### 2.5.1 The already-checked 1.21.1 ticket shape

| Fact | Evidence (the local source jar) |
|---|---|
| `TicketType` can be **built by oneself**: `public static <T> TicketType<T> create(String name, Comparator<T> comparator)` plus the overload taking an `int timeout` | `net/minecraft/server/level/TicketType.java` |
| 1.21.1 **has no ticket persistence**: under `net/minecraft/server/level/` there are only `TicketType` / `Ticket` and no `TicketStorage`; the only "forced-load persistence" is `world/data/chunks.dat` (`ForcedChunksSavedData.FILE_ID = "chunks"`) | enumerating the classes of that package one by one; `ForcedChunksSavedData.java` |
| adding / removing a ticket: `ServerChunkCache#addRegionTicket(TicketType<T>, ChunkPos, int distance, T value)` / `removeRegionTicket(the same arguments)` | `ServerChunkCache.java` |
| ticket level = `33 − distance` (`ChunkLevel.byStatus(FullChunkStatus.FULL) − distance`) | `DistanceManager#addRegionTicket` |
| `level ≤ 31` = `ENTITY_TICKING`, `≤ 32` = `BLOCK_TICKING`, `≤ 33` = `FULL` | `ChunkLevel.fullStatus` / `isBlockTicking` |
| the gate of the block-entity tick is `Level#shouldTickBlocksAt` → `ServerLevel#shouldTickBlocksAt` → `inBlockTickingRange` (that is, **level ≤ 32**) | the `else if (flag && this.shouldTickBlocksAt(...))` inside `Level.tickBlockEntities`; `ServerLevel#shouldTickBlocksAt` |

#### 2.5.2 The choice and the level (fixed by this document)

```java
public final class CoreChunkTickets {
    /** fixed by this document: a ticket type of our own, timeout = 0 (no timeout); the name goes into the log, for /debug and troubleshooting. */
    private static final TicketType<ChunkPos> CORE = TicketType.create("farmstorm_core",
            Comparator.comparingLong(ChunkPos::toLong));
    /** fixed by this document: distance = 1 ⇒ ticket level 32 = BLOCK_TICKING. */
    private static final int DISTANCE = 1;

    public static void acquire(ServerLevel level, BlockPos core) {
        ChunkPos cp = new ChunkPos(core);
        level.getChunkSource().addRegionTicket(CORE, cp, DISTANCE, cp);
    }
    public static void release(ServerLevel level, BlockPos core) {
        ChunkPos cp = new ChunkPos(core);
        level.getChunkSource().removeRegionTicket(CORE, cp, DISTANCE, cp);
    }
}
```

**Why `distance = 1` (fixed by this document, with the full reason)**: D-145 wants "that 1 chunk". `distance = 0` would give level 33 (`FULL`) — the chunk is loaded but **the block entity does not tick**, and a core's boundary particles hang off `serverTick` (`OutpostCoreBlockEntity:179-192`, registered by `getTicker` at `OutpostCoreBlock:58-67`) ⇒ `level ≤ 32` is required. `distance = 1` gives exactly 32: **the smallest ticket at which a block entity ticks**. `distance = 2` gives 31 (`ENTITY_TICKING`) at the price of widening the load face from 3×3 to 5×5 (every ring outwards is +1 level, which is why the 31/32/33 three-step exists), and that is unnecessary.

**An honest description of the load face (fixed by this document / A-43, and it must be written into document 29's boundary list)**: a ticket's level propagates outwards, +1 per chunk. At `distance = 1` the centre chunk = 32 (block-ticking, **the block entity ticks**), the ring immediately around it = 33 (`FULL`: loaded, not ticking), and the next ring = 34 (no longer loaded). So the accurate statement of "**the tick face is 1 chunk; the load face is necessarily 3×3**" is "**only 1 chunk is ticked and the other 8 are loaded to FULL level**"; D-145's "not 3×3" holds on the **tick face**, and on the **load face** it is 3×3 — that is the inherent propagation of the ticket mechanism, not an implementation choice. **A-43 therefore rules: acceptance writes the tick face only, and no assertion may claim that a single chunk is loaded.**

**A-38: down to which file D-145's "no persistence" is precise**. D-145's text says "**writes no `level.dat`**", and that landing point is wrong — 1.21.1's forced loading has nothing to do with `level.dat`. This document unifies the measure per A-38 to: **writes no `world/data/chunks.dat`** (`ForcedChunksSavedData`, `FILE_ID = "chunks"`, key `Forced`; `ServerChunkCache`'s data directory is split by dimension, the nether / the end each being `world/DIM-1/data/chunks.dat` / `world/DIM1/data/chunks.dat`).

**The two rejected paths (both write this file, so neither can be used)**:

| The rejected form | Where it lands | Why it is rejected |
|---|---|---|
| `ServerLevel#setChunkForced(cx, cz, true)` | `getDataStorage().computeIfAbsent(ForcedChunksSavedData.factory(), "chunks")` ⇒ `<the dimension's data directory>/chunks.dat` | **persistent**: after a restart that chunk is still force-loaded, which directly conflicts with D-145's "temporary at runtime"; besides, it is a global force (the forced set of `ChunkMap`) with no paired "released with the core" semantics |
| NeoForge's `TicketController#forceChunk(level, owner, cx, cz, add, ticking)` | it lands in `ForcedChunksSavedData::getBlockForcedChunks` all the same (the third actual argument of `forceChunk` in `TicketController.java` is exactly that tracker) | the same reason: it is a "ticket controller" layer NeoForge puts over `ForcedChunksSavedData`; the name looks like a ticket, and the persistence semantics are exactly those of `setChunkForced` |

⇒ the only option is a **bare `TicketType` + `addRegionTicket`**: the `server/level` package of 1.21.1 has no ticket persistence at all (item 2 of 2.5.1 enumerated it class by class), a ticket is pure in-memory state that vanishes with the server, which is the same thing as D-145's "temporary at runtime". **A-38's landing point is this paragraph and item 2 of §2.5.1.**

#### 2.5.3 Acquiring and releasing (D-145 + RT-39)

| Moment | Action | Mount point |
|---|---|---|
| placing a core | `acquire(level, pos)` | the end of `OutpostCoreBlockEntity#setOwner` (`:95-103`) |
| the core destroyed (really removed) | `release(level, pos)` | inside `OutpostCoreBlockEntity#release` (`:166-170`), at the same place as the index removal |
| migration / settlement branch | same as "the core destroyed" | the `this.release()` at the end of `settleOnRemoval` (`:315-335`) already covers it |
| server stopping | `releaseAll(server)` walks the loaded dimensions and releases | a new `ServerStoppingEvent` listener (checked to exist), the RT-39 catch-all |
| the startup backfill | walk every `(dimension, pos)` of `OutpostCoreIndex` → `acquire` | the same startup passage as §2.2.8's backfill (tickets first, bindings second: a ticket depends only on the coordinate, not on chunk loading) |
| observability (A-43) | subscribe to `ChunkTicketLevelUpdatedEvent` (checked: `getLevel()` / `getChunkPos()` / `getOldTicketLevel()` / `getNewTicketLevel()` / `getChunkHolder()`), and log at DEBUG only for this mod's core coordinates: `核心区块票 <dim> <chunk> old->new` | a new debug listener; §7 #36 uses it to prove the "centre 32 / neighbours 33" tick face |

**Why tickets must be re-acquired at startup**: a ticket is in-memory state and is lost when the server stops. If they were acquired only on placement, then after a restart no legacy core's chunk would be ticked any more — the boundary particles and `settleOnRemoval`'s criterion would both fail silently (this is the other half of RT-39; the fact card wrote only "leak").
**Why releasing must be idempotent**: `release()` has three call sites (`OutpostCoreBlock:101-107`, `settleOnRemoval:334`, the server-stopping catch-all), and `removeRegionTicket`'s arguments must be **exactly identical** to `addRegionTicket`'s (the same type, position, distance and value); one removal too many is a no-op, one too few is a leak.

### 2.6 Short-range convergence of the Pumpkin Turret (man瓜) projectile (D-129)

#### 2.6.1 A line-by-line reading of the current state and the cause of the overshoot

| Line | Current state | Consequence |
|---|---|---|
| `TurretBullet.java:28` | `public static final double SPEED = 0.6;` (hard-coded) | the speed cannot be tuned; and **it has two readers** (`:95` and `PumpkinTurret.java:75`, A-30 has been added as a ruling) |
| `:73-87` `tick()` | server side and `target.isAlive()` → `steerTowards(target)`, then `super.tick()` | the speed is reset every tick |
| `:90-96` `steerTowards` | `delta = the target hitbox centre − itself`; when `delta.lengthSqr() < 1e-4` it **returns outright (keeping the previous tick's velocity)**; otherwise `setDeltaMovement(delta.normalize().scale(SPEED))` | ★ the cause of the overshoot lies in exactly these two lines |
| `:98-119` `onHitEntity` | damage + advancement + knockback + `discard()` | the hit test relies entirely on vanilla `move()` producing a collision |
| `:121-128` `onHitBlock` | `discard()`, no block explosion | it disappears on hitting a wall |

**The three-step chain of the overshoot** (line by line):
1. when `dist < SPEED` (point blank, typically 0.3 blocks), `:95` still gives a velocity of **length 0.6** ⇒ that tick's displacement is 0.6 blocks, overshooting the target centre by 0.3;
2. on the next tick `delta`'s direction turns 180° and the length is still 0.6 ⇒ it overshoots by 0.3 again; so the projectile **reverses every tick** between two points 0.3 blocks either side of the target (this is the "circling / jitter" the author saw);
3. worse still is the early return at `:92-94`: when the displacement brings the projectile almost on top of the target centre (`lengthSqr < 1e-4`) it **no longer changes the velocity** and flies straight on at the previous tick's 0.6-block velocity — at point blank it may either never hit or occasionally graze the hitbox on some tick. Vanilla `move()`'s collision detection is swept (`Entity#move` uses `getBoundingBox().expandTowards(delta)`), so **a hit counts only if "this step's swept volume intersects the target's hitbox"**; reversing every tick means the swept volume jumps back and forth and does not intersect when they cross.

#### 2.6.2 The fix (fixed by this document)

```java
/** fixed by this document: the minimum step of the hit test. Below it the projectile is taken to have "reached the target centre" and settles directly, no longer relying on move()'s sweep. */
private static final double MIN_STEP = 0.05;
/** fixed by this document: keeps the settlement body of :98-119 from being run once by each of the two paths. */
private boolean hitResolved;

private void steerTowards(Entity aim) {
    double step = FarmstormConfig.pumpkinTurretBulletSpeed.get();      // ← the read path of the speed now that it lives in config
    Vec3 delta = aim.getBoundingBox().getCenter().subtract(this.position());
    double dist = delta.length();
    if (dist <= MIN_STEP) {
        this.resolveHit(aim);                                          // judge a hit directly (no longer waiting for move)
        return;
    }
    if (dist <= step) {
        this.setDeltaMovement(delta);                                   // cover the remaining distance in one step: no overshoot, no jitter
        return;
    }
    this.setDeltaMovement(delta.normalize().scale(step));               // normal distance: the original behaviour is unchanged
}

private void resolveHit(Entity hit) {
    if (this.hitResolved) return;
    this.hitResolved = true;
    applyHit(hit);                                                      // the settlement body extracted from :98-119
    this.discard();
}

@Override
protected void onHitEntity(EntityHitResult result) {
    super.onHitEntity(result);
    if (!(this.level() instanceof ServerLevel)) return;
    this.resolveHit(result.getEntity());                                // the same settlement body, guarding against a double settlement
}
```

**Both "converge on the remaining distance" and "judge the hit directly" are done, each with its own job** (fixed by this document):
- `MIN_STEP < dist ≤ step`: **converge** (`setDeltaMovement(delta)`). This step's displacement equals the remaining distance exactly, so the swept volume necessarily covers the target centre, vanilla `move()` produces an `EntityHitResult` and the normal `onHitEntity` path runs;
- `dist ≤ MIN_STEP`: **judge the hit directly**. The displacement is now close to 0, `move()`'s swept volume degenerates into a point, and whether it hits depends on the target's hitbox happening to contain that point — not a behaviour to rely on, so the direct judgement is required. Without it, "point blank" would degrade into step 3 of 2.6.1.
- the `hitResolved` gate guarantees that the two paths never settle the same projectile twice (after `resolveHit`'s `discard()`, vanilla `move()` may still produce a second hit callback in the same tick).

**How it joins up with `onHitEntity`**: `applyHit(Entity)` is `:98-119` moved verbatim (`hurt(void_bolt, projectileDamage)` → a player's `turret_hit` advancement → knockback → no `discard()`; `discard` moves into `resolveHit`), and **not one character of the damage type, the advancement id or the knockback formula changes**. That way "hitting a block" is still `onHitBlock`'s `discard()`, while "hitting an entity" and "the point-blank direct judgement" share one settlement.

**The two read points after the speed moves to config** (fixed by this document: both read the same key, and the muzzle velocity gets no key of its own):

| Location | Change |
|---|---|
| `TurretBullet.java:28` | delete the constant; `:95` becomes `FarmstormConfig.pumpkinTurretBulletSpeed.get()` |
| `behavior/impl/PumpkinTurret.java:75` | `bullet.setDeltaMovement(direction.scale(TurretBullet.SPEED))` → `…direction.scale(FarmstormConfig.pumpkinTurretBulletSpeed.get())` (**the fact card §3.2 row 7 does not mention this place; A-30 has added it as a ruling**) |

**Why it is not cached in a field**: `ModConfigSpec.DoubleValue#get()` reads the loaded in-memory value with no IO; `TurretBullet:106` already reads `projectileDamage` once per shot (an existing precedent). A third snapshot would only add one more trap of "forgot to bake after a reload" (rejected).

**Why the client prediction is not changed** (a D-129 supplement + HC-03): tracking happens on the server only (the class comment at `TurretBullet.java:23-25`), and the client receives the entity's synchronised position and interpolates it; the `clientTrackingRange(4)` / `updateInterval(10)` at `FarmstormEntityTypes.java:60-66` are unchanged character for character. Dropping the speed from 0.6 to 0.45 makes "one position update every 10 ticks" look smoother (the displacement per update interval falls from 6 blocks to 4.5), and no client-side compensation is needed.

### 2.7 The carrier of Friend Chives' persistent field (D-131 ~ D-133)

#### 2.7.1 The current state and the three traps

| Fact | Lines | Constraint on this section |
|---|---|---|
| `settle()` → `onImpact()` → **`discard()` only if `!keepAliveAfterImpact()`** | `ThrownCropEntity.java:95-104` | overriding `keepAliveAfterImpact()` to return `true` is enough to survive |
| the default `keepAliveAfterImpact()` returns `false`, and the only overrider is the firecracker | `:116-118` | this version is the second overrider |
| the lifetime check takes effect only while `!settled` | `:65-72` | ★ **after landing nothing recycles it**: a missed self-destruct is a permanent entity leak (RT-35) |
| `settled` is `private` with no accessor | `:33`, `:99` | ★ a subclass cannot read "have I landed" — it must keep its own record |
| `onImpact` is called exactly once per spawn | `:95-104` | it can be used as the anchor for "the moment of landing" |
| `Settled` / `Submunition` are written to and read from NBT | `:145-157` | ★ after **a chunk unload and reload** `settled` is still `true`, while the subclass's "ticks since landing" is reset if it was not persisted ⇒ a permanent leak |

#### 2.7.2 The carrier design (fixed by this document)

```java
public class FriendChivesProjectile extends ThrownCropEntity {
    /** fixed by this document: the tick count after landing; -1 = not landed yet. Not written to NBT (D-133: the field ends at chunk unload). */
    private int fieldTick = -1;
    /** fixed by this document: if NBT says settled already when the save is read, wait for the first tick and kill itself. */
    private boolean loadedSettled;
    /** A-33: the field length is read from config; no second "200" appears in the code. */
    private int fieldLength;

    @Override protected boolean keepAliveAfterImpact() { return true; }

    @Override
    protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
        this.fieldLength = FarmstormConfig.friendChivesEffectTicks.get();  // A-33: default 200, range 20 ~ 1200
        applyField(level, pos, 0);                 // the existing sphere scan + application of :56-69, changed to take an elapsed parameter
        playHitSoundOnce(level, pos, applied);     // the existing sound logic of :70-79, run only at the moment of landing (D-132)
        this.setDeltaMovement(Vec3.ZERO);          // zero the velocity on landing (D-133)
        this.setNoGravity(true);                   // already set by the constructor (:43-51); explicit here once more to guard against subclass changes
        this.noPhysics = true;                     // no collision: water / pistons / entities cannot push it (D-133)
        this.fieldTick = 0;
    }

    @Override
    public void tick() {
        super.tick();                                        // the base class's lifetime check is already ineffective because of settled (:65-72)
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.loadedSettled) { this.discard(); return; }  // already landed when the save was read => this field ended at the unload
        if (this.fieldTick < 0) return;                      // not landed yet
        this.fieldTick++;
        if (this.fieldTick >= this.fieldLength) { this.discard(); return; }  // self-destruct on the effectTicks-th tick
        applyField(level, this.position(), this.fieldTick);  // rescan the sphere every tick
    }

    /** fixed by this document + A-33: apply (effectTicks − elapsed) ticks, with amplifier still 0 (the existing literals are unchanged). */
    private void applyField(ServerLevel level, Vec3 center, int elapsed) {
        int ticks = this.fieldLength - elapsed;              // at elapsed = 0 this is exactly effectTicks (default 200)
        if (ticks <= 0) return;
        int radius = ...;                                    // friend_chives.hitRadius (default 6.0, the literal 6 is unchanged)
        for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(radius),
                c -> c.isAlive() && center.distanceTo(c.position()) <= radius)) {
            t.addEffect(new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadedSettled = tag.getBoolean("Settled");      // the key the base class writes (:149)
    }
}
```

Four points:

1. **The `effectTicks − elapsed` computation** (A-33): the field length `L = friend_chives.effectTicks` (default **200**, range **20 ~ 1200**); at `elapsed = 0` (the moment of landing) `L` ticks are applied; after that it is +1 every tick, and the `k`-th tick applies `L − k`. **The duration written every tick is 1 tick shorter than the previous tick's**, so "the effect ends by itself after tick `L`" and "the effect runs out exactly when the field ends" both hold at once. Writing 200 as a literal would produce the contradiction "the field runs 200 ticks while everyone gets `100 − x`" once a server owner changes the key to 100; writing a fixed 200-tick duration would renew the effect forever (a D-131 supplement has already pointed that out). So the code has **exactly one source, `this.fieldLength`**, and the literal `200` does not appear a second time.
2. **The physical convergence after landing**: the trio `setDeltaMovement(Vec3.ZERO)` + `setNoGravity(true)` + `noPhysics = true` (`noPhysics` is an existing `Entity` field and is effective in 1.21.1). The three divide the work: zeroing the velocity prevents "being carried off by the last tick's momentum", no gravity prevents "drifting slowly downwards", and no collision prevents "water / pistons / entities pushing it away".
3. **What is visible**: `getDefaultItem()` follows the base class (`ThrownCropEntity:52-56`, taking the `farmstorm:friend_chives` item by `profile().id()`), and rendering follows `ThrownItemRenderer` (already registered at `FarmstormClient:105-117`), so **no model, texture or renderer is added**; after landing it is simply a "chive" item shape hanging at the landing point (D-133).
4. **Chunk unload ends it**: `loadedSettled` makes it kill itself on the first tick. **One may not** rely on "the entity disappears with the chunk": a 1.21.1 chunk unload **stores the entity in the chunk NBT**, and when the chunk is loaded back `settled` is still true while `fieldTick` is -1, so it neither ticks the field nor self-destructs (`super.tick()`'s lifetime check is blocked by `settled`) — that is an entity that never disappears.

#### 2.7.3 The performance estimate (RT-34)

| Quantity | Estimate | Grounds |
|---|---|---|
| per tick per field | 1 `AABB` coarse filter (`inflate(6.0)` = 13×13×13 blocks) + one `distanceTo` per coarse-filter result | `getEntitiesOfClass` walks the per-chunk entity lists internally and does not scan the whole world |
| field length | `friend_chives.effectTicks` (default 200 ticks = 10 seconds; range 20 ~ 1200 ⇒ 60 seconds at most) | A-33; `throwable.lifetimeTicks` (default 100, `FarmstormConfig:328`) constrains only the **pre-landing** phase, and after landing the field is constrained by this class's `fieldLength` |
| fields alive at the same time | at most 1 field per throw; the field count cap = the player's throw rate × the field length | the projectile itself has a cooldown and a lifetime, so fields do not pile up without bound |
| worst-case order of magnitude | 10 fields at once ⇒ 10 AABB per tick + about 10×n distance comparisons (n = the entities inside a field, usually < 20) | the same order of magnitude as the existing `QbounceObserverSync` / trap scans |

**Conclusion**: no spatial index and no every-other-tick scan is needed (D-131 says "every tick" explicitly). What really needs guarding against is "an unbounded number of fields" — `fieldLength`'s self-destruct and the projectile's own cooldown hold that; §7 #30 carries one assertion of "the entity disappears on the `effectTicks`-th tick".

---

## 3. Master table of the change surface (additions / changes / deletions against 0.1.0, line by line)

The table has **47 rows**. The "after the change" column states **the target state** only, never implementation steps (the implementation is in §2). Risk numbers are in §6; `A-xx` means a ruling in `.work/v011-verdicts.md` (an entry already established as a ruling is written here as a ruling and is not registered again in §9).

| # | Kind | Path or class#method | Current value | After the change | Decision | Risk |
|---|---|---|---|---|---|---|
| 1 | change | `gradle.properties:11` | `mod_version=0.1.0` | `0.1.1` | D-124 | — |
| 2 | change | `检查环境.cmd:14`, `一键构建.cmd:10-12` | `farmstorm-0.0.1+mc1.21.1.jar` (and `-sources.jar`) | `farmstorm-0.1.1+mc1.21.1.jar` / `-sources.jar` | D-147 | RT-38 |
| 3 | change | `assets/farmstorm/textures/item/wild_apple.png` | 16×16 / 571 B / 88 colours | **the 64×64 source image as it is** (the source image is 6,946 B) | D-126 | RT-37 |
| 4 | change | the "+" overlay mechanism of `tools/gen_textures.py` (`:274-275`, `:407`, `:546-560`, `:612-623`, `:974-975`) | 25 textures carrying a "+" | the mechanism is deleted (this version regenerates `wild_apple` only, 1 texture); a re-run strips the green cross off the other 24 as well, which is expected (A-61) | D-125 / **A-61** | RT-37 |
| 5 | change | `tools/gen_textures.py:154`, `:849-881`, `:884-895` | the old desktop image + `wild_apple_grid()`'s 16-grid modal colour pick | read the 64×64 source image directly and output it as it is (`wild_apple` takes the pass-through branch) | D-126 | RT-37 |
| 6 | **change** | **`tools/gen_textures.py:982-983` (`expected_size()` always returns 16×16) and `:1151-1154` (`image.size != expected_size(spec)` → `SystemExit`)** | the size assertion accepts 16×16 only | **both places are opened up together**: `expected_size()` returns the source image's actual size for `wild_apple`, and the assertion at `:1153` then holds | D-126 / **A-32** | RT-37 |
| 7 | change | `registry/FarmstormBlocks.java:79` | `SoundType.GRASS` | `SoundType.STONE` (**all 5 sound kinds move together**: break / place / step / hit / fall) | D-130 / **A-31** | — |
| 8 | change | `registry/FarmstormBlocks.java:82-89` | no `lightLevel` | `.lightLevel(state -> 15)` | D-141 | RT-36 |
| 9 | change | `config/FarmstormConfig.java:425-427` | `projectileDamage` default 1 | default **3** | D-128 | RT-38 |
| 10 | change | `config/FarmstormConfig.java:431-433` | `fireIntervalTicks` default 60 | default **40** | D-127 | RT-38 |
| 11 | add | `config/FarmstormConfig.java` (inside the `pumpkin_turret` group, after `:433`) | — | `bulletSpeed` (double, `0.45`, range `0.0 ~ 4.0`) | D-129 | RT-33 |
| 12 | delete | `entity/TurretBullet.java:28` | `SPEED = 0.6` | the constant is deleted | D-129 | — |
| 13 | change | `entity/TurretBullet.java:90-96` | `normalize × SPEED` every tick | short-range convergence (converge when `dist ≤ step` / judge directly when `dist ≤ 0.05`) | D-129 | — |
| 14 | change | `entity/TurretBullet.java:98-119` | the settlement sits in `onHitEntity` | extract `applyHit`, shared by `onHitEntity` and the direct judgement, plus a `hitResolved` gate | D-129 | — |
| 15 | **change** | **`behavior/impl/PumpkinTurret.java:75`** | `bullet.setDeltaMovement(direction.scale(TurretBullet.SPEED))` (**the second reader of `TurretBullet.SPEED`**) | `direction.scale(FarmstormConfig.pumpkinTurretBulletSpeed.get())` | D-129 / **A-30** | — |
| 16 | change | `entity/thrown/FriendChivesProjectile.java` | one-shot on landing, no `tick()` | `keepAliveAfterImpact()=true` + `tick()` + the landing trio + self-destruct on the `effectTicks`-th tick + `loadedSettled` | D-131 / D-133 / **A-33** | RT-34 / RT-35 |
| 17 | change | `entity/thrown/FriendChivesProjectile.java:56-80` | application and sound in the same tick; the constant 200 hidden in `effectTicks`'s default | the sound stays in `onImpact` (once only), the application moves to `tick()`, the duration is **`effectTicks − elapsed`** (the field length reads config, and no second 200 appears in the code) | D-131 / D-132 / **A-33** | RT-34 |
| 18 | verify | `entity/ThrownCropEntity.java:95-118` | the existing hook | **unchanged** (this version only overrides the hook and opens no new lifecycle) | D-131 | — |
| 19 | change | `farmland/FarmlandLedger.java:51` | `byOwner` only | `bindings` + the derived `byCore` + `orphans` (`LinkedHashMap`; the persisted order is diagnostic only and takes part in no decision) | D-134 / **A-47** | RT-32 |
| 20 | delete | `farmland/FarmlandLedger.java:57-58, 72-79, 82-98` | `baseOverride` / `base()` / `setBase` / `personalCapacity` / `sumOverMembers` | all deleted; `capacity = bonus × the number of allied cores` | D-139 | RT-40 |
| 21 | change | `farmland/FarmlandLedger.java:101-121` | `capacity` walks the online members and `used` takes a union | `capacity` from the number of cores on the UUID side, `used` from the number of binding entries | D-139 / D-138 | RT-33 |
| 22 | change | `farmland/FarmlandLedger.java:228-264` | v1 persists `owners` | v2 `dataVersion/bindings/orphans` + the backward-compatible read + `rebuildByCore` | D-137 | RT-32 |
| 23 | add | `farmland/FarmlandLedger.java` (new methods) | — | `bind` / `migrate` / `unbind` / `bindingsOf(CoreKey)` / `backfill(server)` / `orphanCount()`; the backfill processes in the `BlockPos` single-level total order | D-134 / D-136 / D-137 / **A-47** | RT-32 |
| 24 | change | `farmland/FarmlandGuard.java:69-75` | the three states hold only range + quota | add "pick the core + a single core's free slots", refine the meaning of `QUOTA_FULL` | D-135 | RT-33 |
| 25 | change | `farmland/FarmlandGuard.java:92-107` | `inOwnRange` walks the online players | switch to a binding-table / index lookup (offline-safe) | D-138 | — |
| 26 | change | `farmland/FarmlandGuard.java:120-134` | `protectsFromTrample` walks the online players | switch to "a binding exists ⇒ protected" | D-138 | — |
| 27 | change | `blockentity/OutpostCoreBlockEntity.java:315-335` | settle directly | migrate first; **a failed migration has two levels (rebind / turn to dirt); the "not loaded" branch is defensive and unreachable in normal operation (A-60)**; compute first, write afterwards | D-136 / **A-35** / **A-60** | RT-32 |
| 28 | change | `blockentity/OutpostCoreBlockEntity.java:353-379` | two online traversals + 3×3 | process only what stands under this core (`bindingsOf`) | D-136 | — |
| 29 | change | `blockentity/OutpostCoreBlockEntity.java:95-103` | `setOwner` writes only ownership + the index | append: acquire the ticket, request the broadcast | D-145 / D-142 | RT-39 |
| 30 | change | `blockentity/OutpostCoreBlockEntity.java:166-170` | `release` removes only the index entry | append: release the ticket, request the broadcast | D-145 / D-142 | RT-39 |
| 31 | add | `team/TeamResolver.java`, `team/ScoreboardTeamResolver.java` | only `Owner × Player` | add an `Owner × Owner` overload, the `Player` version delegates | D-138 | — |
| 32 | add / change | `outpost/OutpostCoreIndex.java` | `ownerNames` / `count` are `private`, `isOwnerOrAlly` takes a `Player`; `Entry` has only `(dimension, pos)` | add the read-only accessors `entriesOf(...)` / `owners()` / `count(server, uuid)`; **`Entry` gains a `long gameTime` and persists it**, `record(...)` gains a `gameTime` parameter (A-39's comparison-key carrier, see §2.2.10) | D-142 / D-138 / **A-39** | RT-31 |
| 33 | delete | `command/FarmstormCommands.java` | the whole 58-line file | deleted | D-140 | RT-40 |
| 34 | delete | `event/FarmstormGameEvents.java:4`, `:62`, `:71-73` | 3 command registrations | deleted | D-140 | RT-40 |
| 35 | add | `event/FarmstormGameEvents.java` | 0 overlay-related listeners | add 6: login / logout / dimension change / respawn / `ServerTickEvent.Post` / `ServerStoppingEvent` | D-142 / D-145 | RT-31 / RT-39 |
| 36 | add | `network/CoreListPayload.java`, `network/CoreListBroadcaster.java`, `client/network/ClientCoreListHandler.java` | 0 custom packets | 1 S2C payload + assembly + handler | D-142 | RT-31 |
| 37 | add | `config/FarmstormClientConfig.java` + one line after `Farmstorm.java:56` | no CLIENT config | `ModConfig.Type.CLIENT` with 3 keys | D-144 | — |
| 38 | add | `client/FarmstormKeys.java`, `CoreHudRenderer`, `CoreHudDragScreen`, `CoreHudClientCommands`, `ClientCoreList` | `client` has 21 classes | +5 classes; a full overlay is **9 rows / 93 px** (8 cores + 1 overflow) | D-143 / D-144 / **A-34** | — |
| 39 | add | `outpost/CoreChunkTickets.java` | no ticket | a `TicketType` of our own + `distance = 1` | D-145 | RT-39 |
| 40 | change | `datagen/ModLangProvider.java:393-394, 404-406` (deleted) + 10 new entries + the key-count prose of the class comment at `:15`, `:27-32` | 315 keys | 323 keys (§4.3) | D-127 ~ D-144 | — |
| 41 | verify | `Farmstorm.java:124-146`, the registry-surface self-check | asserts 93 / 12 classes | **unchanged** (+0, the assertions still hold) | — | — |
| 42 | verify | `entity/ThrownCropEntity.java` (the existing default of `keepAliveAfterImpact`) | default `false` | **unchanged** (overridden in the subclass only) | D-131 | — |
| 43 | **change** | the expectation source at `tools/verify_project.py:222-237`: the four functions `expected_config_keys` / `_v002` / `_v003` / `_v010` + the union at `:244` | the expected set = the union of the four **old documents** `01-DESIGN §3.8` + `12-TECH §8.1` + `18-TECH §8.1` + `24-TECH §8.1` | add a parser for this version's measure (a new `expected_config_keys_v011(...)`, its table shape matching the 58 groups of §4.1 here) and fold it into the expected set; at the same time **drop** the deleted `farmland.limitPerPlayer` from the union. The criterion is still "expected set == the toml key set" | **A-40** | RT-40 |
| 44 | verify | the other three sections of `tools/verify_project.py` (the 93 registry ids / the lang key sets agree / the prohibition scan) | the existing assertions | **unchanged**: the registry surface is still 93 (§1.2), the lang section reads the artifacts (323 × 2 matches up by itself), and the prohibition scan agrees with D-146's red line | A-40 | RT-40 |
| 45 | delete (supplement) | `config/FarmstormConfig.java:904-908` (`push` / `comment` / `translation` / `defineInRange` / `pop`) + the field declaration `:226`; `run/config/farmstorm-server.toml` | the `[farmland]` section and the `limitPerPlayer` key exist (default 0, range 0 ~ 4096) | **the whole group is deleted**: `farmstorm-server.toml`'s `[farmland]` section disappears with it (groups 59 → 58; keys 166 − 1 + 1 = 166) | D-139 / **A-42** (a supplement row; the action itself, see §2.3 item 3) | RT-40 |
| 46 | change (supplement) | `farmland/FarmlandLedger.java:128-158` (the two write entries `add` / `remove`) | maintains only `byOwner`'s coordinate set; `remove(BlockPos)` removes by coordinate across owners | add "the core-binding parameters" (bookkeeping becomes `bind(owner, coreKey, pos)`) and "keeping `byCore` in sync" (every write entry must change `bindings` and the reverse index together) | D-134 / D-135 / **A-55** | RT-32 |
| 47 | change (supplement) | `farmland/FarmlandLedger.java:203-219` (the lazy validation `validated`) | drops only the coordinates in `byOwner` that are "no longer farmland" | on a hit, **prune `byCore` and the binding table together** (clearing one side only lets ghost farmland hold a core's 16 slots) | D-134 / **A-55** | RT-32 (ghost farmland) |

**Action surface 47 = 44 (original) + 1 (A-42: deleting the whole `farmland` group) + 2 (A-55: the bookkeeping write entries + the lazy validation)**: row 45 is **A-42**'s supplement (the action itself, see §2.3 item 3: row 45 is there for document 31's table A and §4.1 to cross-reference); rows 46 / 47 are **A-55**'s supplements — `FarmlandLedger.java:128-158` (the `add` / `remove` write entries) and `:203-219` (`validated`'s lazy validation) were **not in a single one of the original 44 rows**, while F-188's "files involved" names both passages, and the binding system turns `remove` from one entry into several and requires `byCore`'s reverse lookup to be written and pruned together with `bindings`. **1 ~ 44 are not renumbered**, and the supplement rows follow the same criterion as the existing ones (row 45 ≡ §2.3 item 3; rows 46 / 47 are different methods of the same files as rows 19 and 23, so there is no duplication).

---

## 4. The full config inventory

### 4.1 server (counted into the 166)

**Counting method**: read the static block of `config/FarmstormConfig.java` (`:268-928`), maintain the group stack with `push`/`pop`, and count `.define(...)` / `.defineInRange(...)` / `.defineInList(...)`; **166 keys / 59 groups** in total (§1.4). **The section-counting measure (A-52)**: to count a toml's sections one must use `^\s*\[` — a nested section is **indented** (`\t[blast_carrot.nausea]`), and with `^\[` one can only count **35** top-level sections and never reach 58. This table is the **after** state, 58 groups: the `farmland` group is gone entirely (−1 group), the `pumpkin_turret` group goes from 4 to 5 keys (+1 key), and the other 57 groups list only key names and counts.

**This table has 58 rows (data rows), the key counts adding up to 166**. The "Mark" column: `change` = the default changed; `new` = added in this version; empty = untouched.

| # | Group | Keys | Keys (named one by one) | Mark |
|---|---|---|---|---|
| 1 | `converter` | 3 | `xpCost` `netherStarForwardCost` `netherStarOnReverse` | |
| 2 | `stack` | 2 | `throwable` `other` | |
| 3 | `deploy` | 3 | `turretMiningTicks` `supportMustBeFullOpaque` `dropOnSupportLost` | |
| 4 | `teams` | 2 | `resolver` `untamedFallback` | |
| 5 | `explosion` | 2 | `sliceResistThreshold` `grenadeResistThreshold` | |
| 6 | `throwable` | 1 | `lifetimeTicks` | |
| 7 | `food` | 1 | `eatTicks` | |
| 8 | `puffed_wheat` | 2 | `dryEdge` `wetEdge` | |
| 9 | `blast_carrot` | 3 | `triggerRadius` `explosionPower` `maxDamage` | |
| 10 | `blast_carrot.nausea` | 2 | `level` `durationTicks` | |
| 11 | `ubi_server` | 2 | `explosionPower` `maxDamage` | |
| 12 | `toxic_ubi_server` | 4 | `explosionPower` `maxDamage` `cloudRadius` `cloudDurationTicks` | |
| 13 | `melon_slice_bomb` | 2 | `explosionPower` `maxDamage` | |
| 14 | `melon_grenade` | 6 | `spawnCount` `spawnOffsetY` `horizontalJitter` `explosionPower` `maxDamage` `submunitionSpeed` | |
| 15 | `pumpkin_turret` | **5** | `targetRadius` `projectileDamage` (**change** 1 → 3) `knockbackLevel` `fireIntervalTicks` (**change** 60 → 40) `bulletSpeed` (**new** 0.45) | change / change / **new** |
| 16 | `awakened_torchflower` | 4 | `auraRadius` `refreshTicks` `fireAspectLevel` `particlesPerRefresh` | |
| 17 | `awakened_torchflower.strength` | 2 | `level` `durationTicks` | |
| 18 | `awakened_torchflower.resistance` | 2 | `level` `durationTicks` | |
| 19 | `awakened_torchflower.haste` | 2 | `level` `durationTicks` | |
| 20 | `awakened_torchflower.speed` | 2 | `level` `durationTicks` | |
| 21 | `awakened_torchflower.water_breathing` | 2 | `level` `durationTicks` | |
| 22 | `awakened_torchflower.night_vision` | 2 | `level` `durationTicks` | |
| 23 | `awakened_pitcher_plant` | 4 | `auraRadius` `refreshTicks` `clearHarmful` `particlesPerRefresh` | |
| 24 | `awakened_pitcher_plant.regeneration` | 2 | `level` `durationTicks` | |
| 25 | `awakened_pitcher_plant.saturation` | 2 | `level` `durationTicks` | |
| 26 | `awakened_pitcher_plant.absorption` | 2 | `level` `durationTicks` | |
| 27 | `fang_trap_cactus` | 9 | `triggerRadius` `outerFangCount` `outerDelayTicks` `outerRadius` `innerFangCount` `innerDelayTicks` `innerRadius` `slownessDurationTicks` `slownessLevel` | |
| 28 | `bamboo_firecracker` | 2 | `spawnIntervalTicks` `durationTicks` | |
| 29 | `red_mushroom_puff` | 1 | `radius` | |
| 30 | `red_mushroom_puff.nausea` | 2 | `level` `durationTicks` | |
| 31 | `red_mushroom_puff.blindness` | 2 | `level` `durationTicks` | |
| 32 | `brown_mushroom_puff` | 1 | `radius` | |
| 33 | `brown_mushroom_puff.weakness` | 2 | `level` `durationTicks` | |
| 34 | `brown_mushroom_puff.slowness` | 2 | `level` `durationTicks` | |
| 35 | `lucky_berry` | 6 | `nutrition` `saturationModifier` `randomCropCount` `lootChestCount` `smallExplosionPower` `creativeDurationTicks` | |
| 36 | `lucky_berry.regeneration` | 2 | `level` `durationTicks` | |
| 37 | `lucky_berry.strength` | 2 | `level` `durationTicks` | |
| 38 | `lucky_berry.resistance` | 2 | `level` `durationTicks` | |
| 39 | `lucky_berry.jump_boost` | 2 | `level` `durationTicks` | |
| 40 | `lucky_berry.speed` | 2 | `level` `durationTicks` | |
| 41 | `lucky_berry.chance` | 8 | `lightning` `random_crops` `regeneration` `loot_chest` `small_explosion` `charged_creeper` `godlike_buff` `creative_mode` | |
| 42 | `power_beet` | 2 | `nutrition` `saturationModifier` | |
| 43 | `power_beet.strength` | 2 | `level` `durationTicks` | |
| 44 | `power_beet.speed` | 2 | `level` `durationTicks` | |
| 45 | `power_beet.haste` | 2 | `level` `durationTicks` | |
| 46 | `power_beet.resistance` | 2 | `level` `durationTicks` | |
| 47 | `molotov` | 3 | `effectRadius` `fireChance` `triggerExplosionPower` | |
| 48 | `wild_apple` | 2 | `nutrition` `saturationModifier` | |
| 49 | `wild_hunter` | 11 | `durationTicks` `killExtensionTicks` `killHealPoints` `searchRadius` `searchAngleDegrees` `dashTicks` `speedBonusPercent` `attackSpeedBonusPercent` `attackDamageBonusPercent` `highlightRadius` `loopSoundRange` | |
| 50 | `zangief_cane` | 8 | `triggerRadius` `stageDelayTicks` `riseHeightBlocks` `riseTicks` `spinDelayTicks` `spinDegreesPerTick` `slamDamage` `cancelFallDamage` | |
| 51 | `qbounce` | 4 | `poseMode` `rotateDegreesPerTick` `squashAmplitude` `squashPeriodTicks` | |
| 52 | `friend_chives` | 3 | `hitRadius` `effectTicks` `soundRange` | |
| 53 | `point_defense_fungus` | 3 | `interceptRadius` `maxIntercepts` `interceptParticles` | |
| 54 | `vibration_bean` | 2 | `triggerRadius` `confusionTicks` | |
| 55 | `growth` | 2 | `minSeconds` `maxSeconds` | |
| 56 | `trap` | 3 | `immunityTicks` `dustIntervalTicks` `maxTrapsPerPlayer` | |
| 57 | `outpost_core` | 3 | `farmlandBonus` `maxPerPlayer` `boundaryParticleIntervalTicks` | |
| 58 | `compressed` | 1 | `replaceOnlyAirAndFluid` | |
| | **Total** | **166** | **58 groups** | change 2 / new 1 / delete 1 |

**The arithmetic and the checks** (all three directions must agree):

1. after = before 166 − 1 (delete `farmland.limitPerPlayer`) + 1 (add `pumpkin_turret.bulletSpeed`) = **166**;
2. group count = before 59 − 1 (the whole `farmland` group disappears) + 0 (the new key lands in the existing `pumpkin_turret` group) = **58**;
3. the key counts of this table's 58 rows add up row by row to **166** (`pumpkin_turret` counted as 5).

**The deleted group, registered on its own** (not among this table's 58 rows):

| Group | Key | Type | Default | Range | Action |
|---|---|---|---|---|---|
| `farmland` | `limitPerPlayer` | int | 0 | 0 ~ 4096 | **delete** (`FarmstormConfig.java:904-908` + the field `:226`; `farmstorm-server.toml`'s `[farmland]` section disappears with it) |

**The new key's three attributes** (fixed by this document; the value is taken from fact card §3.4 only):

| Key | Type | Default | Range (fixed by this document) | Reason |
|---|---|---|---|---|
| `pumpkin_turret.bulletSpeed` | double | **0.45** | `0.0 ~ 4.0` | the default comes from ruling #7 (0.45 blocks/tick = 9 blocks/second); the range is the same tier as the existing `melon_grenade.submunitionSpeed` (`0.0 ~ 4.0`, `:418`) — both are speed keys in "blocks/tick" semantics, and reusing the existing dimensional scale beats opening a second one |

**Upgrade notes (D-147, they must go into the delivery document)**: ① in an already-persisted `farmstorm-server.toml`, `fireIntervalTicks` is still 60 and `projectileDamage` still 1; to make them effective one must delete the old toml or hand-edit these two keys; ② `bulletSpeed` is added to an old toml automatically by the config system and **needs no hand-editing**; ③ the `[farmland]` section is not deleted automatically from an old toml (the config system only adds, never deletes), so it becomes "a section nobody reads", and deleting it by hand is recommended (that does not affect running).

### 4.2 client (3 keys, **not counted into the 166**)

| Key | Type | Default | Range (fixed by this document) | Use | Decision |
|---|---|---|---|---|---|
| `hud.enabled` | bool | **true** | — | the overlay switch; both key H and `/farmstorm hud on\|off` write it | D-144 |
| `hud.x` | int | **4** | `0 ~ 3840` | the GUI-pixel x of the overlay's top-left corner; clamped again by `guiWidth − the panel width` when drawing | D-144 |
| `hud.y` | int | **4** | `0 ~ 2160` | the GUI-pixel y of the overlay's top-left corner; clamped again by `guiHeight − the panel height` when drawing | D-144 |

- the persisted file: `<gameDir>/config/farmstorm-client.toml` (§2.4.2), **a separate file** that is never mixed into the server one;
- subtotal: **3 keys / 1 group (`hud`)**; not counted into §4.1's 166 and not counted into the 58 groups;
- all three keys **have** a `translation` (fixed by this document), hence language keys +3: `farmstorm.configuration.hud.enabled` / `.x` / `.y`. Reason: all 166 existing server keys have translations and the config screen displays by key; giving translations to the server keys only would expose bare key names on the client config page (§9 item 1 pins the conditional sentence "whether to add them" down to "add them").

### 4.3 Language-key additions and deletions (named one by one + the total counted out here)

**Before: 315 keys (the key sets of Chinese and English are identical, §1.4).**

Two entries deleted (one block each in `datagen/ModLangProvider.java`; after the deletion a datagen re-run makes them vanish from the artifacts):

| # | Key | Current location | Current text |
|---|---|---|---|
| 1 | `farmstorm.configuration.farmland.limitPerPlayer` | `zh_cn.json:145`; the generating source `ModLangProvider.java:393-394` | Server-side default farmland quota (0 = nobody can till at first) |
| 2 | `command.farmstorm.farmlandlimit.set` | `zh_cn.json:64`; the generating source `ModLangProvider.java:404-406` | Farmland quota set to %s (effective in this process only; a restart falls back to the config default) |

**10** entries added (each with its key name, source and use; **all 10** additions are named one by one by this document, and `k` is no longer left blank):

| # | Key | Use | Decision |
|---|---|---|---|
| 1 | `farmstorm.configuration.pumpkin_turret.bulletSpeed` | the translation of the new config key (the same source and shape as the other 165 config keys) | D-129 |
| 2 | `farmstorm.configuration.hud.enabled` | CLIENT config key 1 | D-144 |
| 3 | `farmstorm.configuration.hud.x` | CLIENT config key 2 | D-144 |
| 4 | `farmstorm.configuration.hud.y` | CLIENT config key 3 | D-144 |
| 5 | `key.farmstorm.core_hud.toggle` | the key description that toggles the overlay (H) | D-144 |
| 6 | `key.farmstorm.core_hud.drag` | the key description that enters drag mode (J) | D-144 |
| 7 | `command.farmstorm.hud.on` | client command feedback: enabled | D-144 |
| 8 | `command.farmstorm.hud.off` | client command feedback: disabled | D-144 |
| 9 | `command.farmstorm.hud.reset` | client command feedback: the position has been reset | D-144 |
| 10 | `gui.farmstorm.core_hud.self` | the "self" marker inside the overlay (allies show their real names and need no key) | D-143 |

**The arithmetic**: `315 − 2 + 10 = 323`. **After: 323 keys × 2 (the Chinese and English key sets exactly identical)**.

Re-checked by prefix (after the change; the sum must be 323):

```
farmstorm.configuration   166 − 1 + 1 + 3 = 169
advancements.farmstorm     36
item.farmstorm             32
block.farmstorm            26
tooltip.farmstorm          16
entity.farmstorm           13
gui.farmstorm               8 + 1 = 9
death.attack                6
effect.farmstorm            4
subtitles.farmstorm         3
key.farmstorm               2      ← the new key prefix
message.farmstorm           2
command.farmstorm           1 − 1 + 3 = 3
container.farmstorm         1
itemGroup.farmstorm         1
--------------------------------
169+36+32+26+16+13+9+6+4+3+2+2+3+1+1 = 323
```

> Note: deleting `farmland.limitPerPlayer` takes `farmstorm.configuration` from 166 to 165, and adding `bulletSpeed` and the 3 `hud.*` keys brings it back to 169 — exactly consistent with §4.1's "166 server keys + 3 client keys" (169 = 166 + 3). That equation is the strong check between "config keys ↔ translation keys", and §7 #5 uses it as an assertion.

---

## 5. Resource inventory

| Category | New | Replaced in place | Note |
|---|---|---|---|
| PNG | **0** | **1**: `assets/farmstorm/textures/item/wild_apple.png` (16×16 / 571 B → **64×64**, the source image 6,946 B / 1814 deduplicated RGBA / 1471 fully transparent) | D-126; the `layer0` of the model `models/item/wild_apple.json` is unchanged, and **no second texture is added** |
| JSON (models / blockstates / loot tables / recipes / advancements) | **0** | **0** | this version touches no JSON resource |
| `sounds.json` | **0** | **0** | **this document rules that `attenuation_distance` is not added**, see the reason below |
| ogg | **0** | **0** | `sounds/friend_chives.ogg` 83,932 B / 48 kHz mono / 10.000 s, untouched |
| language files | **0** (the file count is unchanged) | **2**: `zh_cn.json` / `en_us.json` regenerated by datagen (315 → 323 keys) | §4.3 |

**Whether `sounds.json` should add `attenuation_distance`: the conclusion is "do not add" (fixed by this document, already verified against the source; it is no longer a to-be-verified item)**

- current state: `friend_chives` at `sounds.json:16-22` does not set that key ⇒ it falls back to `Sound`'s default **16**;
- on the playing side: the volume at `FriendChivesProjectile.java:74-76` = `friend_chives.soundRange / 16 = 2.0`;
- the client's actual attenuation-radius formula (`SoundEngine.java`, checked verbatim):

  ```java
  float f  = soundInstance.getVolume();                                    // 2.0
  float f1 = Math.max(f, 1.0F) * (float) sound.getAttenuationDistance();   // max(2,1) × 16 = 32
  ```

  that is, **`audible radius = max(volume, 1) × attenuation_distance`**, and the two quantities are **multiplied**, not added.
- so: **the current state is already 32 blocks** (`2.0 × 16 = 32`), and adding `"attenuation_distance": 32` would turn it into `2.0 × 32 = 64` blocks — which **does not agree** with D-132's "audible at 32 blocks", and would change the meaning of the config key `friend_chives.soundRange` from "blocks" to "half a block" (one key changed, two quantities jumping).
- the precedent for that key at `sounds.json:6` (`wild_hunter_loop` uses 32) is for a sound source of **volume 1.0** (1.0 × 32 = 32) and must not be copied over.
- ⇒ the resource side's "0 new JSON" holds; the item handed over by document 28 §7 item 5 **is closed**, see §9 item 2.

---

## 6. Risk register RT-31 ~ RT-41

Fields: risk / trigger condition / impact surface / mitigation / how to verify (**verification always means the acceptance number in §7**; F-xx task numbers are issued by 31 and this document does not spend them in advance).

### RT-31 · Introducing a custom packet for the first time (S2C)

- **Risk**: if any one of codec, registration, disconnect-and-reconnect, cross-dimension or push timing (login / placement / destruction / dimension change) is missed, the overlay shows wrong data or does not show at all.
- **Trigger condition**: the registration misses `playToClient` (the packet cannot go out), the handler makes a client-side decision (overreach), `total` and `cores` are used with mixed meanings (`+N` computed wrongly), the snapshot is not cleared on logout (left over after a server switch).
- **Impact surface**: the client display (no effect on world state, no effect on server authority).
- **Mitigation**: the payload is read-only (the client does not reply; `playToServer` and `sendToServer` have 0 hits across the repository); assembly is all server-side; `total` and `cores` are two separate fields; `ClientPlayerNetworkEvent.LoggingOut` clears the snapshot; a 10-tick debounce + a 100-tick fingerprint reconciliation.
- **Verification**: #6 (only one packet, the client does not reply, a dedicated server can start), #7 (single player, 1 core → the overlay shows 1 row), #8 (cross-dimension, 2 rows), #9 (row content and ordering), #10 (9 rows = 93 px, 8 cores + 1 overflow), #29 (no leftovers after a server switch).

### RT-32 · Rebuilding the farmland-core binding table (a save-format version bump)

- **Risk**: `farmstorm_farmland.dat` goes from v1 to v2 and a legacy save is read as an empty ledger; the backfill shovels legacy fields; migration miscalculates and turns farmland that should not change into dirt.
- **Trigger condition**: `load` does not check `dataVersion`; `orphans` is lost from persistence; the migration candidates' criterion lacks "has a free slot"; the ledger is changed while it is being iterated.
- **Impact surface**: **world blocks** (the only item that touches the world: `setBlock(DIRT)` on the migration-failure branch) + the fields the player has already tilled.
- **Mitigation**: all of v1 is read as `orphans` (no guessing at bindings); the whole backfill passage has no `setBlock`; migration computes everything before touching the world (following the existing reason of `:312-313`); persisting `orphans` makes the backfill idempotent.
- **Verification**: #16 (the v2 persisted fields), #18 (legacy backfill, what cannot be matched does not turn to dirt, the same N/M on two startups), #19 (migration succeeds), #20 (migration fails with the chunk loaded → turns to dirt), #21 (A-60: the two levels "migration succeeds / turns to dirt" only; "not loaded → the binding only is removed" is a defensive branch, unreachable at runtime, confirmed by code review).

### RT-33 · One key, two meanings: `outpost_core.farmlandBonus` is both the capacity and the quota base

- **Risk**: changing this key changes "the per-core capacity" and "the team quota" at once, and a server owner changes one place believing it affects one place.
- **Trigger condition**: the documents do not state it clearly; `verify_project`'s 166 check is unaffected (the key count does not change), so nobody would raise an alarm about it.
- **Impact surface**: gameplay values (capacity / quota) + the configuration documentation.
- **Mitigation**: state the double meaning in the `comment(...)` at `FarmstormConfig.java:911` (§2.2.6); document 29's value table must put "capacity / quota" on two parallel rows; the meaning of `QUOTA_FULL`'s message becomes "the nearest core is full" (§9 item 4).
- **Verification**: #17 (16 cells per core, the 17th refused; after `farmlandBonus` is set to 2 the 3rd cell is refused and the quota drops to 2 in step).

### RT-34 · Friend Chives scans a sphere every tick

- **Risk**: `effectTicks` ticks × N entities × each field = one AABB per tick; with an unbounded number of fields the cost adds up linearly.
- **Trigger condition**: several fields overlapping, many entities inside a field (near a mob farm), someone throwing wildly.
- **Impact surface**: server tick time (not crash level).
- **Mitigation**: scan `LivingEntity` only; an `AABB` coarse filter + a `distanceTo` re-check (following the two-stage filter of `:60-62`); the field converges through `effectTicks`'s self-destruct; `hitRadius` caps at 16 (the existing config range).
- **Verification**: #30 (the timing: `effectTicks − x` decreasing; after it is changed to 100 the entity disappears on the 100th tick), #31 (the sound plays once only).

### RT-35 · A landed entity living `effectTicks` ticks and staying visible

- **Risk**: pushed by gravity / water / pistons; after a chunk unload it **leaks permanently** (`settled` blocks the lifetime check and `fieldTick` is not persisted).
- **Trigger condition**: the landing point is in water / next to a piston; the player leaves, the chunk unloads and comes back.
- **Impact surface**: entity leaks (world file growth, tick time), plus "the field has ended but the entity is still there".
- **Mitigation**: `setDeltaMovement(ZERO)` + `setNoGravity(true)` + `noPhysics = true`; `readAdditionalSaveData` reads `Settled` → kills itself on the first tick (a chunk unload ends it, D-133); self-destruct on the `effectTicks`-th tick.
- **Verification**: #30 (it disappears on the `effectTicks`-th tick), #32 (a chunk unload ends it, and coming back leaves no residue).

### RT-36 · The Outpost Core's light level 15

- **Risk**: strong light propagates 15 blocks and that position never spawns mobs (`lightLevel` affects the spawn criterion).
- **Trigger condition**: placing a core in the middle of a mob platform / a farm.
- **Impact surface**: gameplay (light / mob spawning) + performance (light updates).
- **Mitigation**: write it into document 29's "known effects"; do not make the light level a config (a D-141 supplement).
- **Verification**: #33 (F3's `Light` value = 15, visible light at night).

### RT-37 · The 64×64 item texture

- **Risk**: the display area in the inventory is still 16×16, so details 4 times as fine are averaged away by the display size; re-running `gen_textures.py` puts the texture back to 16×16.
- **Trigger condition**: re-running datagen / the generator; or expecting "bigger in the inventory".
- **Impact surface**: visuals; it affects neither the model nor the registry surface.
- **Mitigation**: the generator's default source image and `add_plus` are changed in the same version (§3 rows 4 and 5); **the size assertions `:982-983` and `:1151-1154` must be opened up together** (A-32), or a 64×64 output hits `SystemExit`; document 29 states "the display area is unchanged".
- **Verification**: #23 (the PNG is 64×64, still 64×64 after re-running the generator, and the inventory is not broken).

### RT-38 · The Pumpkin Turret's two defaults having no effect on an already-persisted toml

- **Risk**: an old server still runs 60 ticks / 1 damage after the upgrade, and the author believes the change did not take effect.
- **Trigger condition**: `run/config/farmstorm-server.toml` already exists.
- **Impact surface**: the "actually effective value" of gameplay numbers.
- **Mitigation**: the code **does no migration** (D-147 does this on purpose); the delivery document lists the upgrade notes separately (end of §4.1); `bulletSpeed` is added automatically by the config system.
- **Verification**: #3 (after deleting the old toml the defaults are 40 / 3 / 0.45), #4 (without deleting the old toml the old values remain, and `bulletSpeed` is added automatically).

### RT-39 · Chunk ticket leaks

- **Risk**: a destroyed core is not released ⇒ the chunk stays loaded forever; or the tickets are not cleaned up after the server stops.
- **Trigger condition**: `release()` runs with inconsistent arguments (`removeRegionTicket` fails silently); the startup re-acquisition misses a legacy core; the server-stopping path has no catch-all.
- **Impact surface**: memory / the world's load surface (not save corruption).
- **Mitigation**: the release point is the same place as `release()`; `ServerStoppingEvent`'s `releaseAll` is the catch-all; the startup and shutdown acquisitions both read coordinates from `OutpostCoreIndex` (not relying on chunk loading); a ticket is in-memory state (1.21.1 has no ticket persistence).
- **Verification**: #36 (the chunk can unload after the core is removed), #36 (no core coordinate in `chunks.dat` ⇒ nothing was written to disk).

### RT-40 · The knock-on effects of deleting the `/farmstorm` command tree

- **Risk**: `baseOverride` / the 2 language keys / `ModLangProvider`'s comment / the command description in the existing documents — miss one and there are two sources.
- **Trigger condition**: deleting only the command class and not the config key; deleting only the language key and not the generating source.
- **Impact surface**: compilation (`base()` still has a writer after losing its readers) and document consistency.
- **Mitigation**: the five deletions of §2.3 + §3's 47-row master table; `FarmlandLedger`'s base trio deleted in one go; `ModLangProvider`'s two blocks and the class comment changed together (§9 item 7).
- **Verification**: #12 (no `/farmstorm` on the server, only `hud` on the client), #5 (323 language keys and no `farmlandlimit`).

### RT-41 · Existing documents still saying "0 packets" after the red line was broken

- **Risk**: HC-73 of document 26, §1.1/§6 item 4 of document 22, the red-line section of the README and this version's state conflict, and an implementer reviewing against the old measure would judge the new packet a violation.
- **Trigger condition**: anyone re-running the 0.1.0 documents as the red line.
- **Impact surface**: document consistency (no effect on the code).
- **Mitigation**: document 28 §4.5 has already listed the six files to change; §0 and §2.1 of this document use the new measure; document 32, when writing HC-77, states explicitly that it "supersedes half of 0.1.0's HC-73 (the packet half)".
- **Verification**: #37 (`@Mixin` with 0 hits across the repository, exactly 1 file with `CustomPacketPayload`, verify_project's 93/93), #6.

---

## 7. Acceptance self-check list

**38 items (#1 ~ #38)**. Each = one concrete action + **one assertion carrying a concrete value / key name / path** + where to observe it + the grounds. Writing "behaves normally" is banned.

**Three disciplines**: ① this table does not use criteria of the form "the number of call points of some method = N" (A-37: the binding system introduces "remove a binding", after which the write entry is no longer unique; such an assertion is bound to go red once implemented, and one cannot tell a defect from the expectation); ② wherever the text would say "there is only one place in the whole repository", this table always replaces it with a countable **file count / key count / hit count** (for example "`CustomPacketPayload` is in exactly 1 file"), never with "the only call point"; ③ **A-40**: before `tools/verify_project.py` has been changed to this version's measure, its "FAIL 0" is not a pass criterion (see #38).

| # | Assertion (with a concrete value / key name / path) | Where to observe | Grounds |
|---|---|---|---|
| 1 | the startup log contains, verbatim, `自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK` | `run/logs/latest.log` | §1.2, D-146 |
| 2 | in `run/config/farmstorm-server.toml` the `[farmland]` section **does not exist**; the number of section lines (`^\s*\[`; a nested section is indented, see §4.1) = **58**; the `pumpkin_turret` section has exactly **5** keys and `fireIntervalTicks = 40`, `projectileDamage = 3`, `bulletSpeed = 0.45`, `targetRadius = 15`, `knockbackLevel = 1` | `run/config/farmstorm-server.toml` | §4.1, D-127 ~ D-139 |
| 3 | first start after deleting the old `farmstorm-server.toml`: `bulletSpeed` appears in the `[pumpkin_turret]` section with the value `0.45`; `fireIntervalTicks` is `40` and `projectileDamage` is `3` | as above | §4.1, D-147, RT-38 |
| 4 | upgrading directly while keeping the old toml (a 0.1.0 artifact): `fireIntervalTicks` is still `60` and `projectileDamage` still `1`, but `bulletSpeed` is added automatically as `0.45` | as above | D-147, RT-38 |
| 5 | `src/generated/resources/assets/farmstorm/lang/zh_cn.json` and `en_us.json` have **323** keys each and exactly the same key set; `grep -c farmlandlimit` is **0** in both files; `grep -c '"farmstorm.configuration.'` is exactly **169** | the two lang JSONs; section 3 of `tools/verify_project.py` | §4.3, RT-40 |
| 6 | `grep -rln "CustomPacketPayload" src/main/java` across the repository gives exactly **1** file (`network/CoreListPayload.java`); `grep -rn "playToServer\|sendToServer" src/main/java` gives **0 hits**; starting a **dedicated server** completes its startup normally (no `NoClassDefFoundError`) | `src/main/java` + the dedicated server's `latest.log` | §2.1, D-142, RT-31 |
| 7 | after a single player places 1 core, the overlay shows **1 row** shaped like `#1 [overworld] (x, y, z) 自己`; `latest.log` carries one line `核心列表下发：<玩家> 1 条（total=1）` | the in-game overlay + `latest.log` | §2.1.4, D-143 |
| 8 | 1 core in the overworld and 1 in the nether, standing in either dimension: the overlay shows **2 rows**, with `overworld` and `the_nether` as the leading dimension marks; after teleporting it does not change | the in-game overlay | D-143, RT-31 |
| 9 | ordering determinism and reproducibility (A-39): ① place A first (t=100) and then B (t=200) ⇒ A is above B in the overlay; ② **change the two cores' `gameTime` in the index to the same value offline** (by editing `world/data/farmstorm_outpost_core.dat`; the two cores are in different dimensions at exactly the same coordinate) ⇒ after a restart the order follows the **dimension id ascending** (`minecraft:overworld` before `the_nether`); ③ change `gameTime` the same way, same dimension, different coordinates ⇒ the order follows `BlockPos#compareTo` (y → z → x); ④ **start the same save twice and the two "core comparison-key fingerprint" lines in the log are exactly identical** (`核心比较键指纹 <hex16>（N 条）`) | the in-game overlay + the save NBT + the two fingerprint lines in `latest.log` | A-39, §2.1.5, §2.2.10, D-143 |
| 10 | construct 9 allied cores: the overlay draws **8 core rows + 1 `+1` row = 9 rows**, with a total panel height of **93 px** (with 8 cores: 8 rows, 83 px, no `+N`) | the in-game overlay (measure the panel height from a screenshot) | D-143, A-34, §2.4.4 |
| 11 | press **H** to switch the overlay off: `config/farmstorm-client.toml` reads `[hud] enabled = false`; the overlay still does not show after a client restart; pressing H again restores it | `config/farmstorm-client.toml` + in game | D-144, §2.4.2 |
| 12 | run `/farmstorm` in a dedicated server's console: it returns "unknown command"; the single-player client's `/farmstorm` completes only to `hud` (`on` / `off` / `reset`); `/farmstorm farmlandlimit 5` fails in both environments | the server console + the single-player chat bar | D-140, §2.3, RT-40 |
| 13 | `/farmstorm hud off` → `enabled = false`; `/farmstorm hud on` → `true`; `/farmstorm hud reset` → `x = 4`, `y = 4`, and `enabled` unchanged | `config/farmstorm-client.toml` | D-144, §2.4.3 |
| 14 | press **J** to open dragging: single-player **pauses** (the picture freezes, the world does not tick); drag the overlay to the bottom-right corner and press ESC: `hud.x` / `hud.y` become the drop coordinates; re-entering the game leaves the position unchanged | in game + `config/farmstorm-client.toml` | D-144, §2.4.5 |
| 15 | drag the overlay off-screen and release: `hud.x ≤ guiWidth − the panel width`, `hud.y ≤ guiHeight − the panel height`, and the panel is fully visible (0 px out of bounds) | in game (try all four corners) | §2.4.4, §2.4.5 |
| 16 | after tilling 1 cell, look at the save: `world/data/farmstorm_farmland.dat` contains `dataVersion: 2` and 1 entry in `bindings`, with the keys `pos` / `owner` / `coreDimension` / `corePos`, where `coreDimension = "minecraft:overworld"` | the save NBT (read offline with an NBT viewer) | §2.2.2, D-134, RT-32 |
| 17 | till the **17th** cell under the same core: it is refused with the message `Farmland quota full: 16/16`; `bindings` still holds only **16** entries for that core. After changing `outpost_core.farmlandBonus` to **2** and restarting: the **3rd** cell is refused at once, and the team capacity changes to 2 in step | the in-game action bar + the save NBT + the toml | D-134, §2.2.6, RT-33 |
| 18 | the three-level comparison key (A-39): ① between two allied cores 40 blocks apart, till 1 cell at the 3×3 junction ⇒ it is bound to the **nearer** one (`bindings.corePos` = the near core's coordinate); ② make the two cores equidistant ⇒ it is bound to the one with the earlier `placedGameTime`; ③ equally distant again, and change both cores' `gameTime` to the same value offline ⇒ it is bound to the one **earlier in coordinate lexicographic order** (`BlockPos#compareTo`); ④ start the same save twice and the two "farmland binding fingerprint" lines in the log are exactly identical. **Note**: the second level (dimension id) cannot trigger on the binding surface — a candidate core must cover the same piece of farmland ⇒ it is necessarily the same dimension; it only acts in #9's cross-dimension overlay ordering | the save NBT + the two fingerprint lines in `latest.log` | D-135, A-39, §2.2.7, §2.2.10 |
| 19 | start with the 0.1.0 `farmstorm_farmland.dat` (an `owners` section only, no `dataVersion`): the log reads `耕地补建：绑定 N 块 / 保持未绑定 M 块`; **run the same save twice in a row and N and M are exactly identical** (A-47: processed in the `BlockPos#compareTo` single-level total order, independent of `HashMap` iteration order); **the blocks that cannot be matched are still `minecraft:farmland`** (`execute if block` verified one by one, 0 turned to dirt) | `latest.log` + `/execute if block … minecraft:farmland` | D-137, A-47, §2.2.8, RT-32 |
| 20 | 3 fields under core A, of which core B covers 2: after removing A, those 2 fields' `bindings.corePos` becomes B and the 3rd becomes `minecraft:dirt`; B's total stays ≤ 16 | the save NBT + in-game blocks | D-136, §2.2.9, RT-32 |
| 21 | 1 field under core A, no replacement core around, and **that chunk is in memory** (standing right beside the field): after removing A the field becomes `minecraft:dirt`, and there are **no** drops | in-game blocks | D-136, §2.2.9, RT-32 |
| 22 | core A has 1 field under his name and there is no successor core: first walk 200 blocks away, then destroy the core with `/execute in <dim> run setblock <core coords> air` — that field **turns into `minecraft:dirt`** (exactly as when standing next to it), with **no drops**; the `bindings` entry is removed; the log does **not** show "迁移失败且区块未加载：只摘绑定" (A-60; that branch does not log, so the block reading is the criterion). The string "迁移失败且区块未加载：只摘绑定" sits at `OutpostCoreBlockEntity.java:374` and **does not appear in normal operation**, confirmed by code review | the in-game block + the save NBT + `latest.log` | D-136, A-60, §2.2.9, RT-32 |
| 23 | `textures/item/wild_apple.png` is **64×64** (`identify` or PIL reads the size); after re-running `python -X utf8 tools/gen_textures.py` it is **still 64×64** and no "size assertion failed" is reported (`expected_size()` and `:1153` have been opened up per A-32); the inventory shows an unbroken image | the PNG file + the generator's output + the in-game inventory | D-126, A-32, RT-37 |
| 24 | Compressed Hay Block **breaking**: the timbre matches `/playsound minecraft:block.stone.break block @s` and clearly differs from `minecraft:block.grass.break` | in game (play the positive and the negative control once each) | D-130, A-31, §3 row 7 |
| 25 | Compressed Hay Block **placing**: the timbre matches `/playsound minecraft:block.stone.place block @s` and differs from `minecraft:block.grass.place` | as above | D-130, A-31 |
| 26 | Compressed Hay Block **stepping** (walking onto it): the timbre matches `/playsound minecraft:block.stone.step block @s` and differs from `minecraft:block.grass.step` | as above | D-130, A-31 |
| 27 | Compressed Hay Block **mining** (hold the left button without breaking it): the hit sound matches `/playsound minecraft:block.stone.hit block @s` and differs from `minecraft:block.grass.hit` | as above | D-130, A-31 |
| 28 | Compressed Hay Block **falling** (dropping onto it from 3 blocks up): the landing sound matches `/playsound minecraft:block.stone.fall block @s` and differs from `minecraft:block.grass.fall` | as above | D-130, A-31 |
| 29 | leave server X and join server Y (which has no core at all): the overlay **disappears at once** (rather than keeping X's list); back on X the list reappears | the in-game overlay | §2.1.4, RT-31 |
| 30 | after Friend Chives lands (default `friend_chives.effectTicks = 200`): a creature standing inside the field has its remaining Q-Bounce duration refreshed every tick and decreasing tick by tick (200 on tick 1, 199 on tick 2 … 1 on tick 199); the entity disappears on tick **200** (`/data get entity @e[type=farmstorm:friend_chives]` finds nothing). After changing `effectTicks` to **100** and restarting, re-test: the decrement stops at 100 and the entity disappears on tick **100** (proving that 200 is not a constant) | in game + `/data get` + the toml | D-131 / D-133, A-33, §2.7, RT-34 |
| 31 | a drop into empty space (no creature inside the sphere) plays no `friend_chives`; hitting ≥ 1 creature plays it **once only** (exactly 1 "hit sound" line in `latest.log`, with no repeat within the field's 10 seconds) | `latest.log` + listening | D-132, §2.7 |
| 32 | while a field exists, walk 200 blocks away to force an unload and come back: the field has ended (the entity does not exist) with no residue; re-entering the world does not revive it | in game + `latest.log` | D-133, RT-35 |
| 33 | the block the Outpost Core sits on shows `15` on F3's `Light` line; at night a light patch is visible within 15 blocks of the core and there is no light beyond 15 blocks | F3 + in game | D-141, RT-36 |
| 34 | the Pumpkin Turret, three checks in a row: with `fireIntervalTicks = 40` the time stamps of two shots differ by ≈ 2000 ms; one hit deals **3** points (1.5 hearts) of damage; with `bulletSpeed = 0.45` a 15-block flight takes ≈ **33 ticks** (1.65 s) | `latest.log`'s time stamps + the target's health | D-127 ~ D-129, RT-38 |
| 35 | put the target **within 1 block** of the turret: 5 shots in a row all hit (the target loses 15 health) and the projectile no longer circles / jitters; at 3 blocks away the behaviour is the same as before the change | in game (`/summon` a 20-health creature and count) | D-129, A-30, §2.6 |
| 36 | the ticket's **tick face** (A-43): ① after placing a core, the debug log of `ChunkTicketLevelUpdatedEvent` shows **`newTicketLevel = 32` for the core's own chunk** (inside the ticking set, and the block entity ticks as usual — the boundary particles run one lap every 40 ticks), **the immediately surrounding ring `= 33` (border level, outside the ticking set)**: put a `farmland` block into a neighbouring chunk, and after 200 ticks its `moisture` is unchanged; ② after removing the core there are no particles at the same position any more; ③ the `Forced` array of `world/data/chunks.dat` **does not contain** that core's `ChunkPos.toLong()`. **This item's assertion writes the tick face only**: the load face is necessarily 3×3 (§2.5.2), and it is **forbidden** to write as an assertion that a single chunk gets loaded | the `ChunkTicketLevelUpdatedEvent` log + in game + `chunks.dat` | D-145, A-38, A-43, §2.5, RT-39 |
| 37 | `grep -rn "@Mixin\|mixins.json" src/main/java` across the repository gives **0 hits**; `grep -rln "CustomPacketPayload" src/main/java` gives exactly **1** file; the registry section of `python -X utf8 tools/verify_project.py` prints `已核 registry 对象: 93 / 93` | `src/main/java` + the tool's output | D-146, §1.2, RT-41 |
| 38 | before `tools/verify_project.py` is changed per **A-40** (`:222-237` gaining a parser for "§4.1 of this document" and dropping `farmland.limitPerPlayer` from the union), no acceptance record **may** take the tool's `结论：FAIL 0 项` as a pass criterion; after the change, one run must print `结论：FAIL 0 项`, exit with code 0, and show **166** keys in the config section | the tool's output + the acceptance record | A-40, §3 row 43, RT-40 |

> **Coverage cross-reference**: the registry surface 93 → #1 / #37; config 166 + 3 → #2 / #3 / #4; language keys 323 → #5; exactly 1 packet and the client does not reply → #6 / #37; the binding table → #16 / #17 / #18; migration (including A-35's degraded branch) → #20 / #21 / #22; the legacy-save backfill (A-47's reproducible `BlockPos` single-level total order) → #19; the 64×64 texture and the generator's size assertion (A-32) → #23; the **5** sound kinds (A-31) → #24 ~ #28; a full overlay at 9 rows / 93 px (A-34) → #10; the overlay across dimensions → #8; drag persistence → #14 / #15; no leftovers after a server switch → #29; Friend Chives' timing (A-33) → #30 / #31; the end at a chunk unload → #32; light level 15 → #33; the Pumpkin Turret's three items → #34; the Pumpkin Turret at close range (A-30) → #35; ticket release → #36; the three-level comparison key and reproducibility (A-39) → #9 / #18; tickets not hitting the disk (A-38) → #36; the layout decomposition (A-41) → #10; the tool's measure (A-40) → #38; the ticket's tick face (A-43) → #36; the trample surface closed off (A-45) → §2.2.5; farmland's cross-dimension collision (A-49) → §9 item 5 (a known limitation that enters no assertion).

---

## 8. Register of APIs to verify

**8 items**. "The signature has been checked" and "runtime confirmation is still needed" are written separately: every signature names its source in §2 (the local source jar), and this table keeps only **runtime behaviour and semantics**. Each item gives "the way out if it is uncertain"; during implementation, first make a minimal case work and only then write the real logic.

| # | The API / semantics to verify | Why it is uncertain | When to confirm | The way out if it is uncertain |
|---|---|---|---|---|
| ① | the **runtime** behaviour of `PayloadRegistrar#playToClient` / `StreamCodec` / `PacketDistributor#sendToPlayer`: whether the handshake version string `"1"` affects the connection, whether a single-player (in-memory) connection still serializes, whether a dedicated server goes and loads the client handler class | the signatures are checked, but this repository had **0 custom packets** before and has no runtime record at all; `RegisterPayloadHandlersEvent`'s javadoc says only "payloads are always serialized, including memory connections" | the first moment the packet lands (§7 #6 / #7) | if a single-player connection does not serialize so the handler never fires ⇒ change "send in single-player too" to "the `Dist.CLIENT` side actively requests once after `ClientPlayerNetworkEvent.LoggingIn`" (that needs a new `playToServer` request packet — **which would make 2 packets**, and it must be registered back in document 28; adding it on one's own is not allowed) |
| ② | the **registration shape** of `RegisterClientCommandsEvent`: how the client dispatcher and the command tree sent down by the server merge, and where `sendSuccess(..., false)`'s echo inside a client command goes (the chat bar / the action bar) | the event class is checked (game bus, client only, `getDispatcher()` / `getBuildContext()` both present), but this repository has **0 client commands**; the merge logic lives in `ClientPacketListener` and was not read line by line | §7 #12 / #13 | if the client command is overridden by the server's tree ⇒ fall back to the two carriers "the key + CLIENT config" (D-144 items 1 and 3 are enough; the command is only convenience) |
| ③ | the usable shape of `TicketType` and how a non-persistent ticket is added: whether a self-built type from `TicketType.create(String, Comparator)` is fully accepted by `DistanceManager`; whether `removeRegionTicket` requires all four arguments to be identical; whether `distance = 1`'s actual load face is exactly 3×3 (§2.5.2) | the sources of `TicketType` / `ServerChunkCache` / `ChunkLevel` are checked, but this repository has **0 tickets**; NeoForge also has a separate `TicketController` (which goes through `ForcedChunksSavedData` and is **not used** in this version) | the first moment a ticket lands (§7 #36) | if a self-built type is refused ⇒ fall back to `TicketType.PORTAL` (a `BlockPos` value, `timeout = 300`) and renew it on every `serverTick` (**which changes §2.5's release semantics**, and it must be registered back in document 28); if `distance = 1` does not tick the block entity ⇒ change to `distance = 2` (a 5×5 load face, and §9 will be amended in step) |
| ④ | whether `sounds.json`'s `attenuation_distance` and `playSound`'s volume add up | **settled: they multiply** (`Math.max(volume, 1.0F) * attenuationDistance` in `SoundEngine`), so 32 blocks is already reached by `2.0 × 16` and **that key is not added**. What remains uncertain is only "whether the client volume slider for `SoundSource.PLAYERS` scales `calculateVolume` further" | §7 #31 (listening + the logged volume) | if 32 blocks proves inaudible in practice ⇒ adjust `friend_chives.soundRange` (a config key, default 32.0, range 0 ~ 64) and **do not touch `sounds.json`** (touching it would change one resource file outside the 165 keys and conflict with "0 new JSON") |
| ⑤ | the registration timing of a client `KeyMapping` with `RegisterKeyMappingsEvent`: whether the mod bus + `Dist.CLIENT` is early enough (after `Options` loads and before the key screen opens); whether the default keys **H / J** clash with other mods; whether `consumeClick()` still counts while a Screen is open | the event and `register(KeyMapping)` are checked (the implementation is exactly `ArrayUtils.add(options.keyMappings, key)`), but this repository has **0 keys**; a clash depends on which mods the player has installed | §7 #11 / #14 | if registration is too late (the keys do not show on the key screen) ⇒ move registration into `FMLClientSetupEvent#enqueueWork`; if H/J clash ⇒ change only the default key codes, without touching the language keys or the design |
| ⑥ | the concrete value of vanilla's per-packet size cap for a custom packet | `ClientboundCustomPayloadPacket`'s `STREAM_CODEC` is checked, but the cap constant was not found verbatim; this document uses only the 3.1 KB order of magnitude | §7 #10 (still normal with 9 full rows) | if the cap is lower than expected (it cannot be below 3 KB) ⇒ drop `MAX_CORES` from 64 to 32 (`total` still computes `+N` correctly) |
| ⑦ | the mouse-coordinate measure of the drag `Screen`: whether `mouseClicked` / `mouseDragged`'s `(mx,my)` are already coordinates **after GUI scaling** (the same coordinate system as `hud.x` / `hud.y`) | `Screen`'s mouse methods come from `GuiEventListener`'s default implementation, and this repository has no usable precedent (`ConverterScreen:86` has only `render`, no mouse logic) | §7 #14 / #15 | if they are not in the same coordinate system ⇒ do one `mx * guiWidth / windowWidth` conversion first inside `mouseClicked` (using `Minecraft#getWindow`'s width and height as the factors) |
| ⑧ | whether `ClientPlayerNetworkEvent.LoggingOut` fires on both paths — "single player quitting to the main menu" and "being kicked" (the catch-all for clearing the snapshot) | the event class is checked (`LoggingIn` / `LoggingOut` / `Clone`), but this repository uses it 0 times; the single-player quit path was not read line by line | §7 #29 | if it does not fire ⇒ clear in `ClientTickEvent.Post` when `Minecraft#level == null` (one more null check per tick, a negligible cost) |

---

## 9. Register of differences from upstream

**This section only registers; it does not change upstream on its own** (documents 28 / 29 / the fact card / `tools/verify_project.py` are all untouched). Each item gives "what upstream says → what the source / the measurement says → what this document does".

**Exclusions**: **A-30 ~ A-49, already established as rulings in `.work/v011-verdicts.md`, are not registered again in this table** — they are already pinned down as rulings in §2 / §3 / §7. **The only exception is A-49**: its landing point is exactly item 5 of this table (a known limitation), because it produces no action at all (it changes neither §3's change surface, nor §4's config, nor §7's acceptance). The other items of this table are **newly found while writing this document and not yet ruled by upstream**; among them items 3 and 5 are written as "implementation note / known limitation" at the author's request and enter no acceptance assertion.

| # | Upstream location | What upstream says | Measurement / derivation | What this document does |
|---|---|---|---|---|
| 1 | fact card §3.5, document 28 §3.4 | the language-key formula `315 − 2 + 1 + 3 + 1 + k`, where `k` = the number of client-command feedback entries (3 candidates), with the final value "pinned down by document 30" | the formula **misses 2 key-description keys**: each of D-144's 2 `KeyMapping`s needs one `key.farmstorm.*` translation, or the key screen shows a bare key name. With those 2 added: `315 − 2 + 1 + 3 + 1 + 3 + 2 = 323` | this document pins down **323** (§4.3 names the 10 additions one by one); `k = 3` is pinned down; the 2 missing key-description keys are registered here — **documents 29 / 32 are asked to cite 323** |
| 2 | fact card §3.3, document 28 §7 item 5 | whether `sounds.json` should add `"attenuation_distance": 32` for `friend_chives` "is a technical matter, handed to document 30 to check" | the client's audible radius = `max(volume, 1) × attenuation_distance` (`SoundEngine`, verbatim), the two quantities **multiplied**. The current `2.0 × 16 = 32` is already 32 blocks; adding 32 would make it **64** blocks | this document rules **not to add it** (§5), so the resource side's "0 new JSON" holds. The handover item is closed |
| 3 | fact card §1.3, `FarmlandLedger:203-219` | the ledger's lazy validation clears only the coordinates in `byOwner` that are "no longer farmland" | after the binding table is introduced, `bindings` and the derived `byCore` drift together: if a vanished piece of farmland is cleared from `bindings` but not from `byCore`, the capacity is held by **ghost farmland**, and "the core is obviously not full yet reports full" appears | this document fixes: the lazy clearing must **prune `byCore` at the same time** (§2.2.4's `setDirty` table); the item is not written as an acceptance criterion (A-37's discipline) but only as an implementation requirement |
| 4 | document 28, D-135's "supplement" | "whether `Verdict.QUOTA_FULL`'s message language key is renamed is up to document 30 (not renaming costs less)" | after the binding system `QUOTA_FULL` means "the nearest allied core in range is full" instead of "the team quota is used up"; but the existing text "Farmland quota full: %s/%s" holds under both meanings (`%s/%s` = used / cap) | this document rules **not to change the key** (both `message.farmstorm.farmland.quota_full` and `.no_core` are kept); the semantic change is registered here, and document 29's boundary list must say "the cap inside the message is the team capacity, not a single core's capacity" |
| 5 | fact card §1.3, `FarmlandLedger:46-51` | the ledger is "player UUID → a coordinate set", persisted as `farmstorm_farmland.dat`, **with no dimension field** (so since 0.1.0) | two consequences: ① a nether farmland block at `(0,64,0)` "borrows" the **binding protection** of the same coordinate in the overworld (`protectsFromTrample(level, pos)` checks the coordinate only); ② the **quota count** takes the same coordinate in two dimensions as one block | **not fixed in this version (A-49), listed as a known limitation**. The three reasons copied verbatim: **the plan did not require it**; **a legacy save's NBT has only `pos`, so the dimension cannot be traced back**, and adding one would require a default value or a lazy back-fill, which introduces a new **irreproducible surface**; the farmland side **has already taken one save-format version bump (v1→v2)** in this version, and stacking a second one would be more risk than gain. This version keeps `coreDimension` in the binding record only (that is the **core**'s dimension) and never back-derives the farmland's dimension from it |
| 6 | the "Java file count" of fact card §0 | "document 30 names and counts them itself and writes them into the document" | this document counts **125 files / 15,785 lines** (`src/main/java`, `ReadAllLines` file by file); the `client` package has **21** classes (20 top-level + `client/render/TurretBulletRenderer`) | written into §1.1; consistent with fact card §1.5's "21 classes", no conflict |
| 7 | fact card §3.2 row 26 | "`ModLangProvider.java`: 315 keys → delete 2, add N" | that class's **class comment** (`:15`, `:27-32`) writes 315 and the per-section key counts as prose ("299 + 16 = 315"), and after keys are deleted / added the comment no longer matches the artifacts | this document fixes: changing keys must **change the comment too** (§3 row 40's "change" scope includes the comment); the fact card named the keys but not the comment |
| 8 | fact card §1.2, `OutpostCoreIndex` | `ownerNames` (`:55-56`) and `count` (`:162`) are relied on by D-142 / D-138 | both are `private`; `isOwnerOrAlly` (`:99-113`) exists but takes a `Player` (not offline-safe), and this version **does not use it** | this document fixes: add three read-only accessors to the index (§3 row 32) and do not revive `isOwnerOrAlly` (leaving it in its present state of having no caller) |

---

## Appendix A — the parameters this document fixes (a reason for each)

| # | Parameter | Value | Reason | Section |
|---|---|---|---|---|
| 1 | payload package name / class name | `com.dashpig.farmstorm.network.CoreListPayload` | consistent with the existing package style (`outpost/`, `farmland/`, `command/`); `CoreList` describes the content directly | §2.1.2 |
| 2 | payload id | `farmstorm:core_list` | the namespace must be this mod's (do not use `CustomPacketPayload.createType`, which would land in `minecraft:`) | §2.1.2 |
| 3 | handshake version string | `"1"` | the first packet; it must move whenever the codec moves, and starting from 1 makes comparison easy | §2.1.2 |
| 4 | per-packet core cap | `MAX_CORES = 64` | 2 cores per person × 32 people = 64, covering a common server; anything beyond is expressed by `total` | §2.1.6 |
| 5 | send debounce | `MIN_INTERVAL_TICKS = 10` | chain events in the same tick are merged; 0.5 seconds is still inside the "immediate" band | §2.1.3 |
| 6 | reconciliation period | `RECONCILE_INTERVAL_TICKS = 100` | catches "an ally change with no event"; a 5-second delay is acceptable and the cost is negligible | §2.1.4 |
| 7 | client handler thread handling | `context.enqueueWork(...)` | the registrar has already wrapped it with `MainThreadPayloadHandler`; writing it at the call site is more robust against future changes | §2.1.2 |
| 8 | sort key (three levels) | `gameTime` → `dimension.toString()` → `BlockPos#compareTo` (y → z → x) | A-39's final three levels; the third level is used only for "same tick + same dimension + same coordinate" (physically impossible) | §2.1.5 |
| 9 | binding-table NBT key names | `dataVersion` / `bindings` / `orphans`; inside a record `pos` / `owner` / `coreDimension` / `corePos` | the same style as `OutpostCoreIndex`'s `owners` / `entries` / `dimension` / `pos` | §2.2.2 |
| 10 | format version | `2` (missing = 1) | missing means v1, the only reading that is compatible with a 0.1.0 product save | §2.2.3 |
| 11 | binding distance measure | `BlockPos#distSqr` (three-dimensional Euclidean square) | the 3×3 test is already Chebyshev; using Chebyshev for the distance too would produce many ties along the diagonal | §2.2.7 |
| 12 | the candidate order for migration | `distSqr` → `placedGameTime` → coordinate lexicographic (A-39) | **the same** as tilling's core binding, so "bound to the near one, migrated to another" cannot happen | §2.2.9 |
| 13 | when the backfill runs | once, inside the first `ServerTickEvent.Post` | `MinecraftServer` is not ready at constructor time; a one-shot boolean gate guarantees it runs once | §2.2.8 |
| 14 | whether `orphans` is persisted | **persisted** | not persisting ⇒ the backfill cannot be retried, and "keep untouched" degrades into "forgotten forever" | §2.2.2 |
| 15 | ticket type name / distance | `"farmstorm_core"` / `distance = 1` (level 32) | the name in the log helps troubleshooting; 32 is the smallest level at which "the block entity ticks" | §2.5.2 |
| 16 | ticket release points | `release()` + `releaseAll` at server stop | three release paths share one place; the server-stopping catch-all guards RT-39 | §2.5.3 |
| 17 | the projectile's minimum hit step | `MIN_STEP = 0.05` | below it `move()`'s swept volume degenerates into a point and the hit cannot be relied on | §2.6.2 |
| 18 | the projectile's speed range | `0.0 ~ 4.0` | the same dimension and tier as `melon_grenade.submunitionSpeed` | §4.1 |
| 19 | the chive field's self-destruct tick | the **`effectTicks`**-th tick (`fieldTick >= fieldLength` ⇒ `discard()`) | the measure is set by ruling **A-33** (the field length reads config); what this document fixes is "the code keeps exactly one source, `fieldLength`, and no second 200 appears" | §2.7.2 |
| 20 | the chive field's physical convergence | `Vec3.ZERO` + `setNoGravity(true)` + `noPhysics = true` | the trio guards against "carried off by momentum / falling / being pushed" respectively | §2.7.2 |
| 21 | already landed when the save is read | `readAdditionalSaveData` reads `Settled` → kills itself on the first tick | the base class's `Settled` blocks the lifetime check, so not killing itself means a permanent leak | §2.7.2 |
| 22 | the 2 keys' defaults | H (toggle) / J (drag) | in 1.21.1's vanilla default key table neither is taken | §2.4.1 |
| 23 | key category | reuse `KeyMapping.CATEGORY_GAMEPLAY` | a category of one's own would need 1 more language key, which would not square with the 323 account | §2.4.1 |
| 24 | CLIENT config ranges | `x: 0 ~ 3840`, `y: 0 ~ 2160` | the upper bound of 4K GUI pixels; the real boundary is a second clamp by the screen when drawing | §4.2 |
| 25 | the meaning of `hud reset` | reset the position only (`x/y = 4/4`), leaving `enabled` alone | D-144 gives only the action names; "reset" in an overlay context means the position | §2.4.3 |
| 26 | whether command feedback is broadcast | `sendSuccess(..., false)` | a local switch has no need to let other players know (the opposite of the deleted server-side command) | §2.4.3 |
| 27 | the overlay layout | row height **9 px** / row spacing **1 px** / padding **2 px** (A-41, the same measure as document 29 §5.6) / panel height `10n + 3` (**8 rows 83 px / 9 rows 93 px**, the equivalent corollary) / background `0x80000000` / border `0xFF000000` / oneself `0xFF55FF55` / allies `0xFFFFFFFF` / row format `#%d [%s] (%d, %d, %d) %s` / `+N` as a pure number concatenation | **the decomposition A-41 fixed** (row height 9 + row spacing 1 + padding 2); the colours are taken from D-143 and the note under the fact card's table; `+N` adds no language key | §2.4.4 |
| 28 | the overlay's drawing clamp | clamp to `[0, guiWidth − width] × [0, guiHeight − height]` before drawing | after a window resize / GUI scale change the old coordinates may be out of bounds | §2.4.4 |
| 29 | `hideGui` and the drag state | the drag state ignores `hideGui` and `screen != null` | edit state must let the player see what he is dragging (the technical necessity of a D-144 supplement) | §2.4.4 |
| 30 | the criterion of trample protection | **a binding exists ⇒ protected** (A-45) | the trample event has no inquirer; a binding's existence is the evidence of an allied relation. **It applies to the trample surface only**: the quota gate (tilling) and the overlay still use the **live** ally test (changing immediately after a team change); document 29's B-18 has already been narrowed accordingly | §2.2.5, §2.5 |
| 31 | the comparison carrier of `placedGameTime` | **persisted on the index side**: `OutpostCoreIndex.Entry.gameTime` (not a block-entity read) | A-39 requires "never load a candidate core's chunk for the sake of comparison"; the index is a `SavedData` whose read needs no chunk, whereas a BE read is affected by the load timing and is not reproducible (§2.2.10) | §2.2.10 |
