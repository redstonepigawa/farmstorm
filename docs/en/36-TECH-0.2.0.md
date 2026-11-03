# 36 · v0.2.0 Technical Specification

> Project: Farmstorm · version **0.2.0** (MC 1.21.1 / NeoForge 21.1.213 / Java 21).
> Single source of truth: `docs/zh/34-DECISIONS-0.2.0.md` (D-148 ~ D-172, 25 in total). This document **only cites** decision numbers: it opens no new decision and changes no measure; where it conflicts with document 34, document 34 prevails, and the conflict itself is registered in §10 of this document.
> Factual base: `.work/v020-BRIEF.md` (§0 baseline / §1 source facts / §2 the **19** rulings / §3 the increment list / §4 risks RT-42 ~ RT-50). Of these, **#17 ~ #19 in §2 are the three added during writing** (the core storage's two gates / the trade granularity staying at 3 items / the converter's Chinese name unchanged); document 34 has not absorbed them yet and the D numbers are to be back-filled by document 37 ⇒ this document is written against 19 and lands those three in §2.6.4 / §2.4.2 / §2.9 (§10 item 15).
> The two scans `.work/v020-scan-A.md` / `.work/v020-scan-B.md` serve only as side witnesses; **every count below is counted out of the source by this document itself**.
> Design measure: `docs/zh/35-DESIGN-0.2.0.md` (all 11 requirements, full specification). This document does not restate the design values; it writes only "how to implement it and how to verify it".
> Stylistic reference: `docs/zh/30-TECH-0.1.1.md` (**only** the section structure of §1 baseline check / §2 class design / §3 action surface table / §4 full config table / §7 acceptance / §8 to-verify / §9 difference register is learned; **not one of its values is cited**).
> Red line: **no Mixin (0, still prohibited)**; custom packets have **no cap**, this version adds **1** (the link), **2** in total (D-172, registered in §2.8).
> Writing discipline: the three numbers — registry surface 97, server config 175 keys / 60 groups, language keys 337 — are **all counted by this document itself** after naming every item one by one (the methods are written in §1.2 / §1.3 / §4.1 / §4.3), copied from neither document 34 nor `BRIEF`; every technical parameter this document fixes on its own (payload name, NBT key name, `Property` name, `RenderType`, tick carrier, level positions) is marked "**[this document's decision]**" with a reason (the summary is in the §2 items and the list of appendix A); **no task schedule and no effort** (those belong to document 37).

---

## 0. Reading conventions

### 0.1 What this document is responsible for

| Item | Content |
|---|---|
| Responsible for | the per-class independent naming of the registry surface **97** and the counting method (§1.2); the per-key naming of the language keys **337** and of the server config **175 keys / 60 groups** (§1.3, §4); the class design of the nine technical components (tier values, the two planting-chain paths, the farmland comment-out list, the villager trades, the cultivator, the core container, the three migration exits, the link rendering, retiring the converter) (§2, including where the three rulings #17 ~ #19 added during writing land); the master table of additions / changes / deletions against 0.1.1, **46 rows** (§3); the resource inventory (§5); save and compatibility **written as four separate items** (§6); how RT-42 ~ RT-50 are verified (§7); the **36** acceptance self-check items (§8); the **13** APIs to verify (§9); the register of differences from upstream, **16 items** (§10) |
| Not responsible for | the gameplay narrative, the timing tables, the player-visible changes (document 35); task breakdown, phases, milestones, effort (document 37); the master prompt and the phase prompts (documents 38 / 39); the design trade-offs of the decisions themselves (document 34) |
| Files left untouched | every **existing** file under `src/` is read-only (this document only describes "what it must become"; it lands no code); `docs/zh/00` ~ `35`, `docs/en/*` and `.work/*` are all read-only; this document creates exactly one file, `docs/zh/36-TECH-0.2.0.md` |

### 0.2 Counting discipline

1. **The three numbers — registry objects, config keys, language keys — are all counted by this document itself**, with a statement of "which lines of which files were counted". It copies neither `BRIEF` §0, nor document 34 §3.4, nor document 30 §1.2.
2. Each of the three tables satisfies "row count = the declared count", "subtotal = the sum of the rows" and "no overlap and no omission across tables".
3. Numbers may come from four sources only: document 34's decision numbers, `BRIEF` §1 / §2 / §3, arithmetic on two already-given numbers, and the technical parameters this document marks "**[this document's decision]**" (a reason for each). **No gameplay value is invented.**
4. Always read files with `[IO.File]::ReadAllLines($p,[Text.Encoding]::UTF8)` (on this machine `Get-Content` reads BOM-less UTF-8 Chinese as GBK mojibake).

### 0.3 The list of "[this document's decision]" (13 items, a reason in the body for each)

| # | Parameter | Value | Body |
|---|---|---|---|
| 1 | the enum values of `CropTier` and the form of `translationKey()` | `COMMON`/`RARE`/`EPIC`; `tooltip.farmstorm.tier.<lower-case name>` | §2.1 |
| 2 | the `ChatFormatting` used by the tier name color | `WHITE` / `BLUE` / `GOLD` | §2.1 |
| 3 | the language key of the tooltip tier line and its format argument | `tooltip.farmstorm.tier`, `%s` = the tier name | §2.1 |
| 4 | the cultivator block-state property name | `working` (`BooleanProperty`) | §2.5 |
| 5 | the three NBT keys of the cultivator BE | `Held` / `Progress` / `Core` (missing `Core` = unbound) | §2.5 |
| 6 | the cultivator BE's tick carrier | block side `getTicker` → BE static `serverTick` (server only) | §2.5 |
| 7 | the core container's NBT keys and binding-list key | `Items` (`ContainerHelper.TAG_ITEMS`) / `Cultivators` (a Long list) | §2.6 |
| 8 | the link payload name, `MAX_LINKS`, the broadcast constants | `farmstorm:cultivator_link`; `1024`; `10` / `20` ticks | §2.8 |
| 9 | the link's `RenderType` / stage / colour / line width | `RenderType.lines()`; `Stage.AFTER_PARTICLES`; RGB `0.30/0.85/0.90`, alpha `1.0`; line width as `RenderType`'s own `1.0` | §2.8 |
| 10 | the three level positions of the trades | **1 / 3 / 5** | §2.4 |
| 11 | the trades' `xp` and `priceMultiplier` | `2` / `0.0F` | §2.4 |
| 12 | the core storage screen's title key and screen texture | `container.farmstorm.outpost_core`; reuse vanilla `textures/gui/container/generic_54.png` | §2.6 |
| 13 | the file names of the static first-frame textures | `cultivator_a_side_still.png` / `cultivator_a_top_still.png` | §2.5 |

---

## 1. Baseline and the registry-surface check

### 1.1 Platform and build

| Item | Value | Evidence |
|---|---|---|
| Minecraft / NeoForge / Java | **1.21.1 / 21.1.213 / 21** | `gradle.properties:2-3`; Java 21 is pinned by the toolchain (`org.gradle.java.installations.paths=D:/project_farm/.tools/jdk21` at `gradle.properties:21-23`); this document attaches no line number (document 34 §9 ②) |
| `mod_version` | **0.2.0** (currently 0.1.1; changed by this version) | `gradle.properties:11` (`:10` is `mod_license`, document 34 §9 ①) |
| Build entry point | `.tools\gw.cmd` (the only entry point; no sub-command dispatch) | `gw.cmd:41` |
| Run directory | `run/` (isolatable with `-PfsRunDir=`) | the `gameDirectory` property of `build.gradle` |
| Java file count (current) | **134** (the whole `src/main/java` tree, counted with `ReadAllLines` file by file) | counted by this document itself; this version adds **11 classes** (§2 names them one by one: `CropTier`, `VillagerTradeEvents`, `TierTrade`, `CultivatorBlock`, `CultivatorBlockEntity`, `OutpostCoreMenu`, `OutpostCoreScreen`, `CultivatorLinkPayload`, `CultivatorLinkBroadcaster`, `ClientCultivatorLinks`, `CultivatorLinkRenderer`), **145** after the change |
| Baseline commit surface | 0 changes under `src/` since the `v0.1.1` tag | end of `BRIEF` §1.2 (`git diff --stat v0.1.1 -- src` is empty); D-148 |

### 1.2 Per-class naming of the registry surface (**counted out of the source by this document itself**, 97 after the change)

**Counting method (reproducible)**: open each of the **11** registry classes under `src/main/java/com/dashpig/farmstorm/registry/` and count item by item by "registration call + registered id literal", **never by back-deriving from `EXPECTED_COUNTS`, never by reading the assertion values in `Farmstorm.java`, never by copying `BRIEF` §0**. Three counting measures:

- `DeferredRegister#register(...)` / `registerItem` / `registerBlock` / `registerComponentType`: count **the number of calls**;
- registrations that go through a factory method (`cropItem` / `seedItem` / `crop` / `deployed` / `projectile`): count **the number of factory calls**, with the id given by `CropProfile.path()` or by the first argument literal;
- `damage_type` is **data-driven** (the comment at `FarmstormDamageTypes.java:14`: the instance is supplied by the datapack JSON, and the code side has only a `ResourceKey`): count **the number of static `ResourceKey` constants**.

| # | registry | What was counted now (file:line) | Ids one by one | Current count | v0.2.0 increment | After |
|---|---|---|---|---|---|---|
| 1 | `Registries.ITEM` | `FarmstormItems.java`: **22** × `cropItem(...)` (`:36,38,40,42,44,47,49,51,53,55,57,59,61,63,65,70,72,75,79,85,91,95`) + **7** × `seedItem(...)` (`:89,100,101,102,103,104,106`) + **3** × `registerItem(...)` (`:110,113,116`) | 22 crops: `puffed_wheat` `blast_carrot` `ubi_server` `toxic_ubi_server` `power_beet` `melon_slice_bomb` `melon_grenade` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `lucky_berry` `molotov` `zangief_cane` `wild_apple` `compressed_puffed_wheat` `friend_chives` `point_defense_fungus` `vibration_bean`; 7 seeds: `chive_seeds` `puffed_wheat_seeds` `power_beet_seeds` `melon_grenade_seeds` `pumpkin_turret_seeds` `awakened_torchflower_seeds` `awakened_pitcher_plant_pod`; 3 block items: `converter` `compressed_hay_block` `outpost_core` | **32** | **+1** (`cultivator`'s BlockItem) | **33** |
| 2 | `Registries.BLOCK` | `FarmstormBlocks.java`: **15** × `crop(...)` (`:26,27,28,29,30,31,32,33,34,35,36,37,38,39,43`) + **8** × `deployed(...)` (`:46,47,48,50,52,55,62,65`) + **3** × `registerBlock(...)` (`:69,73,82`) | 15 crop blocks: `puffed_wheat_crop` `blast_carrot_crop` `ubi_server_crop` `toxic_ubi_server_crop` `power_beet_crop` `melon_grenade_stem` `pumpkin_turret_stem` `awakened_torchflower_crop` `awakened_pitcher_plant_crop` `fang_trap_cactus_block` `bamboo_firecracker_stalk` `red_mushroom_puff_block` `brown_mushroom_puff_block` `lucky_berry_bush` `friend_chives_crop`; 8 deployed blocks: `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `zangief_cane` `point_defense_fungus` `vibration_bean`; 3 others: `converter` `compressed_hay_block` `outpost_core` | **26** | **+1** (`cultivator`) | **27** |
| 3 | `Registries.BLOCK_ENTITY_TYPE` | `FarmstormBlockEntities.java`: **7** × `REGISTER.register` (`:24,28,31,34,38,42,50`) | `blast_carrot` `pumpkin_turret` `awakened_torchflower` `awakened_pitcher_plant` `fang_trap_cactus` `converter` `outpost_core` | **7** | **+1** (`cultivator`) | **8** |
| 4 | `Registries.ENTITY_TYPE` | `FarmstormEntityTypes.java`: **12** × the factory `projectile(...)` (`:31,33,35,37,39,41,43,45,49,51,55,57`; factory body `:70-78`) + **1** × `REGISTER.register("turret_bullet", …)` (`:61`) | `puffed_wheat` `ubi_server` `toxic_ubi_server` `melon_slice_bomb` `melon_grenade` `bamboo_firecracker` `red_mushroom_puff` `brown_mushroom_puff` `molotov` `compressed_puffed_wheat` `friend_chives` `vibration_bean` `turret_bullet` | **13** | +0 | **13** |
| 5 | `Registries.MOB_EFFECT` | `FarmstormMobEffects.java:24,28,32,36` | `wild_hunter` `zangief_piledriver` `q_bounce` `confusion` | **4** | +0 | **4** |
| 6 | `Registries.SOUND_EVENT` | `FarmstormSounds.java:21,26,31` | `wild_hunter_loop` `zangief_piledriver` `friend_chives` | **3** | +0 | **3** |
| 7 | `Registries.RECIPE_TYPE` | `FarmstormRecipes.java:23` | `converting` | **1** | +0 | **1** |
| 8 | `Registries.RECIPE_SERIALIZER` | `FarmstormRecipes.java:25` | `converting` | **1** | +0 | **1** |
| 9 | `Registries.CREATIVE_MODE_TAB` | `FarmstormCreativeTabs.java:16` | `main` | **1** | +0 | **1** |
| 10 | `Registries.DAMAGE_TYPE` | `FarmstormDamageTypes.java:20,23,26` (3 `ResourceKey` constants) | `void_bolt` `crop_blast` `zangief_slam` | **3** | +0 | **3** |
| 11 | `Registries.DATA_COMPONENT_TYPE` | `FarmstormDataComponents.java:16` | `temporary_enchantments` | **1** | +0 (the tier runs on a static field, D-150) | **1** |
| 12 | `Registries.MENU` | `FarmstormMenus.java:17` | `converter` | **1** | **+1** (`outpost_core`'s storage screen) | **2** |
| | **Total** | | | **93** | **+4** | **97** |

Per-class subtotal (equal to the rows above, item by item):

```
items                 33   ← 32 + 1
blocks                27   ← 26 + 1
block_entity_type      8   ← 7 + 1
entity_type           13
mob_effect             4
sound_event            3
recipe_type            1
recipe_serializer      1
creative_mode_tab      1
damage_type            3
data_component_type    1
menu_type              2   ← 1 + 1
--------------------------------
sum = 33+27+8+13+4+3+1+1+1+3+1+2 = 97
```

**Three independent algorithms cross-check one another**: this document's item-by-item naming = **97**; the 12 assertions of `Farmstorm#logRegistrySelfCheck` (`Farmstorm.java:132-156`) added up (after the change) = **97**; the `EXPECTED_COUNTS` of `tools/verify_project.py` after the change = **97**. This document's number is **not** copied from the latter two — it was counted out again under the "number of calls" measure of the table above, and the two side witnesses are used only to confirm the measure has not drifted.

**The self-check assertions' old value → new value, item by item** (`Farmstorm.java:147-150`, which this version must change; document 34's D-171 says only "93 → 97, + on every item" and gives no per-item old values):

| Variable | Old value (`:148-150`) | New value |
|---|---|---|
| `total` | `93` | **97** |
| `items` | `32` | **33** |
| `blocks` | `26` | **27** |
| `blockEntities` | `7` | **8** |
| `menuTypes` | `1` | **2** |
| the other 8 classes | `13 / 4 / 3 / 1 / 1 / 1 / 3 / 1` | **unchanged** |

`damageTypeCount()` (`Farmstorm.java:163-166`) takes **no new item** in this version: the tier, the trades, the cultivator and the link are none of them damage types.

### 1.3 Current baseline of the language keys and the config keys (named by this document itself)

**SERVER config (currently 166 keys / 58 groups)**: the measure is to read the static block of `config/FarmstormConfig.java` (`static {` at `:267` through `SPEC = b.build();` at `:927`) and to land every key by the rule "`b.push("…")` / `b.pop()` maintain the group stack; `.define("…")` / `.defineInRange("…")` / `.defineInList("…")` count one key". This document ran a script over it and then re-checked every group by hand:

```
keys 166 / groups 58 = 34 top-level groups + 24 nested groups
```

> 📌 The **group-count reading** against document 30: "currently 166 keys / 59 groups (35 top-level + 24 nested)" in document 30 §1.4 is the **pre-change baseline** (0.1.0, when the `farmland` group still existed), while the **post-change** value in §4.1 of the same file is exactly **58 groups**; this document measures 58 groups (34 + 24), consistent with that and with `BRIEF` §0's 58. **It is not a deviation**, see §10 item 1. The per-group detail is in §4.1.

**CLIENT config (currently 3 keys)**: `hud.enabled` / `hud.x` / `hud.y` at `config/FarmstormClientConfig.java:29-43`, landing in `config/farmstorm-client.toml`. This version **leaves it alone** (§4.2).

**Language keys (currently 323 × 2, the zh and en key sets exactly identical)**: read `src/generated/resources/assets/farmstorm/lang/zh_cn.json` and `en_us.json`, take the key set after `ConvertFrom-Json`: `zh=323`, `en=323`, zh-only = 0, en-only = 0. By prefix (the grouping is this document's own, adding up to 323):

```
farmstorm.configuration   169   ← = 166 server + 3 client (not 166! see §10 item 12)
advancements.farmstorm     36
item.farmstorm             32
block.farmstorm            26
tooltip.farmstorm          16
entity.farmstorm           13
gui.farmstorm               9
death.attack                6
effect.farmstorm            4
subtitles.farmstorm         3
command.farmstorm           3
message.farmstorm           2
key.farmstorm               2
itemGroup.farmstorm         1
container.farmstorm         1
--------------------------------
sum = 169+36+32+26+16+13+9+6+4+3+3+2+2+1+1 = 323
```

**Resource baseline (the base of §5)**: under `src/main/resources/assets/farmstorm`, **113 PNGs** = `textures/block` 79 + `textures/item` 29 + `textures/mob_effect` 2 + `textures/misc` 2 + `textures/gui` 1; `*.png.mcmeta` **0**; datagen artifacts: `blockstates` **26**, `models` **103**, `loot_table` **26**, `recipe` **29**.

### 1.4 The current state of the two red lines

| Item | Current state (re-checked by this document) | v0.2.0 | Evidence |
|---|---|---|---|
| Custom packets | **1**: `farmstorm:core_list` (S2C, read-only, `MAX_CORES = 64`), registered at exactly **one** `.playToClient(...)` in `FarmstormNetwork.java:29-31` | **2** (1 new link packet, D-169 / D-172) | `CoreListPayload.java:23,25,28`; across the repository `playToClient\|playToServer` hits 2 places (1 is the call, 1 is the import side of `RegisterPayloadHandlersEvent`), actually **1** registered |
| Mixin | **0** | **0**, still prohibited | across the repository the `Mixin`/`mixin` keyword has **0 hits** (`src/`, `build.gradle`, `gradle.properties`, `settings.gradle` all scanned); no `*.mixins.json`; no mixin section in `neoforge.mods.toml` |

Two readings (get either one wrong and #1 / #2 of §8 both go red):

1. Packets **do not enter the registry surface** — a payload goes through `RegisterPayloadHandlersEvent` and is not written into `BuiltInRegistries`, so the 97 of §1.2 does not include it (document 34 §5.3 item 4).
2. Mixin's 0 is a **prohibited item**, not "this version did not get round to it": all nine technical components of 0.2.0 are implemented with inheritance + the event bus + native extension points, and not one of them needs bytecode injection.

---

## 2. The technical components this version adds / rewrites

Nine components in this section, each giving "class / file → key signatures → data shape → correspondence to the decisions". **Every parameter not marked "[this document's decision]" comes from document 34 or from an existing precedent in the source.**

### 2.1 Tiering (`CropTier` enum + the 9th field of `CropProfile` + name color / tooltip landing)

**Decisions**: D-149 (11 / 7 / 4), D-150 (a static field), D-151 (name color + one tooltip line).

**New file `util/CropTier.java`** (the 1st new class):

```
enum CropTier implements StringRepresentable
  COMMON / RARE / EPIC
  getSerializedName()  → name().toLowerCase(Locale.ROOT)
  translationKey()     → "tooltip.farmstorm.tier." + getSerializedName()
  color()              → ChatFormatting.WHITE / BLUE / GOLD
  nameStyle(Component) → base.copy().withStyle(color())
```

**[this document's decision] (1) (2) (3)**, three reasons:

- the key-name form follows the existing `CropCategory.translationKey()` (`CropCategory.java:38-40`: `"tooltip.farmstorm.category." + getSerializedName()`); no second key-name convention is opened;
- `color()` takes `ChatFormatting.WHITE / BLUE / GOLD`, the white / blue / gold that D-151 names. ⚠️ This is **not** vanilla `Rarity`'s palette (`Rarity.java:16-19` is white / yellow / aqua / light purple), which is one more reason not to reuse `Rarity` — the colours do not match (§10 item 9);
- `nameStyle` sits inside the enum so that the 3 item classes share one implementation instead of writing `withStyle` three times.

**`CropProfile` gains a 9th field** (`util/CropProfile.java:32-40`, the record header):

```
record CropProfile(id, type, categories, cropBlock, seedItem, kind, allowedFaces, throwStack, CropTier tier)
```

Position 9 **goes after `throwStack`** ([this document's decision], reason: the line numbers and semantics of the first 8 fields are cited one by one in documents 34 and 35 and in both scans; appending at the end cannot shift any existing "field N" citation).

**The knock-on change surface of adding the field (named one by one by this document: 10 construction sites + 15 factory arguments)**:

| Site | Now | After |
|---|---|---|
| the three private factory signatures | `crop(...)` (`:191-195`), `throwable(...)` (`:197-201`), `deployed(...)` (`:203-207`) | each gains one `CropTier tier` parameter, forwarded to the record constructor |
| the 15 profiles that go through a factory | `:47,49,51,53,55,62,64,68,71,74,77,79,81,83,111` | each appends one `CropTier.X` argument |
| the 7 that call `new CropProfile(...)` directly | `:58` (`melon_slice_bomb`), `:87` (`molotov`), `:92` (`zangief_cane`), `:97` (`wild_apple`), `:102` (`compressed_puffed_wheat`), `:117` (`point_defense_fungus`), `:125` (`vibration_bean`) | each appends `CropTier.X` at the end of the argument list |

`15 + 7 = 22` matches the 22 rows of `CropProfile.ALL` (`:131-136`) and the existing assertion at `Farmstorm.java:112-115` — **this is RT-49's acceptance entry point**: miss one, and the compiler reports it on the argument count of `new CropProfile(...)` (a record's canonical constructor accepts no missing argument), so it cannot drift silently.

**Name-color landing (3 classes, covering 29 items)**:

| Class | Items covered | How it is changed |
|---|---|---|
| `item/ModifiedCropItem.java` | **15** (`ThrowableCropItem` 12 + `FoodCropItem` 3, both `extends ModifiedCropItem`) | override `Item#getName(ItemStack)`: `return this.profile.tier().nameStyle(super.getName(stack));` |
| `item/DeployableCropItem.java` | **7** (`extends net.minecraft.world.item.BlockItem`, **not** a subclass of `ModifiedCropItem`, so it must be overridden separately, `:18`) | as above, using the `profile` it holds itself |
| `item/SeedItem.java` | **7** (`extends ItemNameBlockItem`, `:16`) | as above |

`15 + 7 + 7 = 29 = 22 crops + 7 seeds` (22 = 12 throwable + 7 deployed + 3 food, the same number as the existing assertion at `Farmstorm.java:112-113`; note that **there are 8 deployed blocks but only 7 deployed items**: `vibration_bean`'s block comes from `deployed(...)` while its item is a `ThrowableCropItem`). Why go through `Item#getName` instead of painting it in a GUI: `ItemStack#getHoverName()` (`ItemStack.java:731-739`), when there is no `CUSTOM_NAME` / `ITEM_NAME` component, **is** calling `getItem().getName(this)`, and the tooltip's first line (`:755`), the item name inside a container, the chat item link and the anvil screen all go through it ⇒ one override, one colour on every interface, which is exactly the "go through the Component's style" that D-151 asks for.

**The one tooltip line (one implementation covering 29 items)**: the tooltip of all 29 items converges on `ModifiedCropItem.appendCropTooltip(profile, tips)` (`ModifiedCropItem.java:113`), with three call sites at `ModifiedCropItem.java:59`, `DeployableCropItem.java:33` and `SeedItem.java:30`. Append one line at the **end** of that method:

```
tips.add(Component.translatable("tooltip.farmstorm.tier",
        Component.translatable(profile.tier().translationKey())).withStyle(ChatFormatting.GRAY));
```

The tier-name child component therefore carries its own colour while the shell is grey — the same colour as the existing category line (`:126`'s `withStyle(GRAY)`), so it does not fight for attention. **It is not merged into the category line** (D-151 supplement: that is a different semantics).

**The "four don'ts" that make the tier precise** (get any one wrong and D-150 becomes a data component): no new `data_component_type` (`data_components` stays 1), no item NBT, no new registry object, no change to the 22 crop textures.

### 2.2 Cutting the planting chain (how each of the two paths is blocked)

**Decisions**: D-152 (two paths), D-155 (blocks and items keep their registration); risk RT-48.

#### Path ①: `ModifiedCropItem#isPlantableSoil` (custom sowing, carrying the 15 profiles that have a crop block)

Current state (`item/ModifiedCropItem.java:101-107`):

```
private static boolean isPlantableSoil(UseOnContext ctx, BlockState soil) {
    if (!(soil.getBlock() instanceof FarmBlock)) { return false; }
    return true;                 // ← the only pass-through point
}
```

The only call site is `:72` inside `plantCrop(...)`. **The change: the criterion becomes a constant `false`** (D-152 ①). Two equivalent forms work; this document takes "keep the method, make the body `return false;`, and keep the original test as a comment":

```
private static boolean isPlantableSoil(UseOnContext ctx, BlockState soil) {
    // 裁定 #4 / D-152：v0.2.0 起本模组作物不可种植。原判据「是 FarmBlock 才允许」保留在下一行注释里备查。
    // return soil.getBlock() instanceof FarmBlock;
    return false;
}
```

The knock-on behaviour (think it through together, or it will be misread as a bug):

| Caller | Now | After |
|---|---|---|
| `ModifiedCropItem#useOn` (`:47-50` → `plantCrop`) | sows on farmland | `plantCrop` returns `PASS` at `:72` ⇒ `useOn` returns `PASS` ⇒ **right-clicking farmland does nothing** |
| `DeployableCropItem#useOn` (`:36-43`) | tries sowing first, and on `PASS` goes to `super.useOn` and deploys | the sowing branch is a constant `PASS` ⇒ **the deploy branch is always taken**, and the placement of the **7 `DeployableCropItem`s is unaffected** (the 8th of the 8 deployed blocks, `vibration_bean`, has a throwable item and never went down this path anyway). This is the dividing line between "crops cannot be planted" and "deployed blocks cannot be placed" |

#### Path ②: vanilla sowing through the 7 `SeedItem`s (`SeedItem extends ItemNameBlockItem`)

Current state (`item/SeedItem.java:16-32`): only `appendHoverText` is overridden, and sowing is **fully inherited** from `BlockItem#useOn` (`BlockItem.java:44-51` → `place(new BlockPlaceContext(ctx))`). `FarmstormCropBlock` does **not** override `mayPlaceOn` / `canSurvive`; the block side never blocked it in the first place (`BRIEF` §1.2).

**The change: override `useOn` to return `PASS`** (D-152 ②):

```
@Override
public InteractionResult useOn(UseOnContext ctx) {
    // 裁定 #4 / D-152：v0.2.0 起 7 个独立种子不可播种（原版 BlockItem#useOn 这条路整条切断）。
    return InteractionResult.PASS;
}
```

Three semantics that must be spelled out:

1. `PASS` means "I do nothing here", **not** `FAIL` (`FAIL` would make the following logic believe it was refused); after `PASS` is returned, vanilla goes on to try the block's own `useWithoutItem` (`ServerPlayerGameMode.java:363` → `:370`, the existing order), so right-clicking a chest while holding a seed still opens the chest — that is vanilla behaviour and this version must not change it.
2. **The held item is not consumed**: `PASS` comes before `itemstack.consume(1, player)` in `BlockItem#place` (`BlockItem.java:95`), so a seed does not vanish into thin air.
3. **The dead code `canPlantAt` (`ModifiedCropItem.java:53-55`) is untouched**: it has 0 callers across the repository, and deleting it is pre-existing cleanup outside this version's requirement (D-152 supplement).

#### What is not touched (D-155, this item's "negative list")

| Object | Count | Why it is not touched |
|---|---|---|
| crop blocks | 15 | deleting a block turns it into air in an old save (D-155 supplement) |
| crop items | 22 | keeps their registration and their creative-tab order |
| seed items | 7 | as above; seeds are now consumed by the cultivator (D-163) and the trades (D-157) |
| block loot tables | 26 (15 of them are crop blocks) | they live and die with the registry surface |
| `FarmstormCropBlock` | 1 class | no refusal is added on the block side — "cannot be planted" is cut on the item side only |

**RT-48's acceptance entry point**: #6 and #7 of §8 assert the two paths separately (each verified on its own), and #8 asserts "deployed blocks can still be placed" — block only one of them and either #7 or #8 must go red.

### 2.3 Retiring the farmland system (comment out the call sites only)

**Decisions**: D-153 (comment out the call sites only), D-154 (do not read / write / delete the `.dat`), D-155 (do not delete the blocks); risk RT-42.

#### The comment-out list (7 sites, each with "why it cannot be deleted")

| # | file:line | What is commented out | Why it cannot be deleted / cannot be commented out as a whole block |
|---|---|---|---|
| 1 | `event/FarmstormGameEvents.java:75` | `gameBus.addListener(FarmstormGameEvents::onRightClickBlock);` | commenting out only the registration line is enough to make the listener **inactive**; deleting the declaration would take #3's method signature with it and lose the comparison |
| 2 | `event/FarmstormGameEvents.java:76` | `gameBus.addListener(FarmstormGameEvents::onFarmlandTrample);` | as above; this line is the only switch of "trample protection" |
| 3 | `event/FarmstormGameEvents.java:241-265` | the **whole body** of `onRightClickBlock` (including `ledger.bind(...)` at `:254` and the two action-bar messages at `:260-264`) | the body references `FarmlandGuard` / `FarmlandLedger` and 2 language keys; delete it and there is no traceable evidence that "this used to block hoeing here" (document 34 §8 item 3) |
| 4 | `event/FarmstormGameEvents.java:277-281` | the whole body of `onFarmlandTrample` | as above; the only call site of `FarmlandGuard.protectsFromTrample` (`:219`) |
| 5 | `event/FarmstormGameEvents.java:171` | `FarmlandLedger.get(server).backfill(server);` (inside `onServerTickPost` at `:166-176`) | this is the **only startup-time `get()`**; commenting it out = the `.dat` is no longer read (D-154) |
| 6 | `config/FarmstormConfig.java:909-912` | the `.defineInRange(...)` definition of `farmlandBonus` (the key disappears from `farmstorm-server.toml`) | leaving the key in the toml is the same as saying "this version still maintains a piece of data it has declared retired" |
| 7 | `datagen/ModLangProvider.java:403`, `:412`, `:413` | 3 language keys: `farmstorm.configuration.outpost_core.farmlandBonus`, `message.farmstorm.farmland.no_core`, `message.farmstorm.farmland.quota_full` | the key's definition is gone and the message's emitting point (#3) is gone too; keeping the language keys only makes the config screen show bare key names |

#### Three places that **must be kept** (commenting them out as a block breaks things)

| Object | Line | Why it is kept |
|---|---|---|
| `onServerTickPost` itself | `FarmstormGameEvents.java:166-176` | it still has to run `CoreListBroadcaster.tick(server)` (`:175`), the overlay packet's **only per-tick entry**. Comment out the whole block ⇒ the overlay never receives a packet again (document 34 §9 ⑦) |
| `CoreChunkTickets.restoreAll(server)` | `FarmstormGameEvents.java:170` | it serves the **core's chunk ticket**, and has nothing to do with farmland |
| `FarmlandLedger` (555 lines) / `FarmlandGuard` (225 lines) | two whole classes | ruling #3: not one line of the body changes, they still compile, and they stay in the source tree. Delete them and there is no way back (document 34 §7 item 1) |

#### ⚠️ One place where the **measure must change**: the field declaration at `FarmstormConfig.java:227` is **not commented out**

D-153's conclusion writes that the **reads** of `outpost_core.farmlandBonus` must be commented out too, but **those two reads sit inside the two classes that are required to change not one line**:

| Read site | Code |
|---|---|
| `farmland/FarmlandGuard.java:75` | `int bonus = FarmstormConfig.outpostCoreFarmlandBonus.get();` |
| `farmland/FarmlandLedger.java:305` | `return FarmstormConfig.outpostCoreFarmlandBonus.get() * allyCoreCount(level, player);` |

So "comment out the field declaration at `:227`" and "change not one line of the two classes" are mutually exclusive. **The only self-consistent measure = A-62** (the full adjudication is at the end of §10):

- the field declaration at `FarmstormConfig.java:227` **is kept as it is** (otherwise the two kept classes will not compile);
- only the **definition** at `:909-912` is commented out (the key disappears from the toml, and the arithmetic `166 − 1 = 165` holds);
- the two `.get()` calls inside the two classes **are kept as they are**, and are unreachable at runtime (the call sites of `onRightClickBlock` and `backfill` are already commented out);
- the cost must be written into the comment: **the field is `null` at this point**, and if someone later restores only the listeners and not the definition, they get a `NullPointerException` instead of the default value — that is exactly the concrete shape of RT-42's "accidental restore". **The comment site must carry this line**:

```
// 裁定 #3 / D-153：v0.2.0 撤销耕地，勿恢复。
// 注意：本键的 .defineInRange 在 :909-912 已注释，字段现为 null；
// 恢复监听器（:75/:76）前必须先恢复定义，否则 FarmlandGuard:75 抛 NPE。
```

#### The three don'ts for the `.dat` (D-154)

`farmstorm_farmland.dat` (the on-disk name is `"farmstorm_farmland"` at `FarmlandLedger.java:53`, the format is `dataVersion: 2` at `:55` and `:34`): **not read, not written, not deleted**. After comment point #5, `FarmlandLedger.get(...)` has 0 runtime calls; the last two call sites of `of(level)` (`onRightClickBlock:249` and `settleOnRemoval`) disappear together with #3's comment-out and §2.7's whole-section rewrite. The file stays in the save directory as it is.

### 2.4 Villager trades (`VillagerTradesEvent` + 3 trade items)

**Decisions**: D-157 (3 items, unlocked level by level), D-158 (`maxUses = 1` + out of stock), D-159 (the price ranges 2–5 / 10–32 / 40–64); risk RT-46.

#### The event signature (checked by this document, no longer "to verify")

`.tools/gradle-home/caches/modules-2/…/neoforge-21.1.213-sources.jar!/net/neoforged/neoforge/event/village/VillagerTradesEvent.java`:

| Item | Value |
|---|---|
| Type | `class VillagerTradesEvent extends Event` (**not cancellable**, no result) |
| Bus | **`NeoForge.EVENT_BUS`** (the game bus; the class comment reads "It is fired on the `NeoForge#EVENT_BUS`") |
| Timing | on every `TagsUpdatedEvent` reload, fired once for **each** `VillagerProfession` |
| `getTrades()` | `Int2ObjectMap<List<ItemListing>>`, **keys 1 ~ 5 are always non-null** (the class comment reads "populated for levels 1-5 (inclusive), so Map#get will never return null") |
| `getType()` | `VillagerProfession` |
| `getRegistryAccess()` | `RegistryAccess` |
| The trade-item interface | `VillagerTrades.ItemListing#getOffer(Entity, RandomSource)` (`VillagerTrades.java:1593-1595`) |

⇒ **one new class `event/VillagerTradeEvents.java`** (the 2nd new class), hung on `NeoForge.EVENT_BUS` in the `Farmstorm` constructor (the same place as `FarmstormGameEvents.register`, beside `:72`). The handler body:

```
if (event.getType() != VillagerProfession.FARMER) { return; }
event.getTrades().get(1).add(TierTrade.COMMON);
event.getTrades().get(3).add(TierTrade.RARE);
event.getTrades().get(5).add(TierTrade.EPIC);
```

#### The three level positions = **1 / 3 / 5** ([this document's decision] (10))

Reason: vanilla has only 5 level positions (`VillagerData.java:13-14`: `MIN_VILLAGER_LEVEL = 1`, `MAX_VILLAGER_LEVEL = 5`; the experience thresholds `{0,10,70,150,250}` at `:15`), and document 34 §9 ⑪ gives only three example labels "novice / journeyman / master". Three reasons for taking **1 / 3 / 5**:

1. the three positions are **spread as far apart as possible** (1 / 3 / 5), so the player meets one new trade at every perceptible stage (just hired / mid-game / max level) instead of being crowded into 1 / 2 / 3;
2. the positions in between, **2 and 4, are left empty**, so they neither take slots from vanilla's existing farmer trades nor change vanilla's "two items drawn per level" sampling-pool ratio;
3. `getTrades().get(3)` / `get(5)` are **always non-null** in this event (the class comment), so no null check is needed — which is what makes "leaving the positions empty" a zero-risk form.

#### The trade item's implementation (`event/TierTrade.java`, the 3rd new class)

`record TierTrade(CropTier tier, List<Item> pool, int minKey, int maxKey) implements VillagerTrades.ItemListing`:

| Step | What it does | Grounds |
|---|---|---|
| 1 | `Item item = pool.get(random.nextInt(pool.size()));` — draw 1 item uniformly from that tier's whole set | document 35 §4.4 (drawn once when the offer is first generated, fixed afterwards) |
| 2 | `int min = cfg(minKey), max = cfg(maxKey); if (max < min) { int t = min; min = max; max = t; }` — **swap automatically when the config is written the wrong way round**, no exception | [this document's decision] (no document 34 measure; the 6 keys of the `trade` group let the player write them the wrong way round, and swapping beats crashing the server) |
| 3 | `int price = (max == min) ? min : min + random.nextInt(max - min + 1);` — a **closed interval**; `nextInt(bound)` is open at the upper end, hence the `+1` | D-159 (a random value in the range) |
| 4 | `ItemCost cost = new ItemCost(Items.EMERALD, price);` | `ItemCost(ItemLike, int)` at `ItemCost.java:44` |
| 5 | `return new MerchantOffer(cost, Optional.empty(), new ItemStack(item, 2), 1, 2, 0.0F);` | `(ItemCost, Optional<ItemCost>, ItemStack, maxUses, xp, priceMultiplier)` at `MerchantOffer.java:70` |

Where the five values come from, one by one (**not one new value**):

| Value | Source |
|---|---|
| **2 crops** per deal | document 35 §4.3 (the 2 is this document's supplement; reason: the same number as the cultivator's single output, and `2 ≤ 16` is the strictest stack tier) |
| `maxUses = 1` | D-158; `MerchantOffer#isOutOfStock()` (`:181`) is true when `uses >= maxUses` ⇒ one deal and it is sold out |
| `xp = 2` | **[this document's decision] (11)**: take the `2` most common in vanilla farmer trades (`VillagerTrades.java:90,93,94` etc.), the same for all three tiers rather than per tier |
| `priceMultiplier = 0.0F` | **[this document's decision] (11)**: zeroes the `floor(i × demand × priceMultiplier)` term in `MerchantOffer#getModifiedCostCount` (`:107-111`) ⇒ **the price does not rise** after a deal (`demand` is 1 after the first deal under `maxUses = 1`), and the offer **equals** the integer drawn from the range |
| the three ranges 2–5 / 10–32 / 40–64 | D-159, entering the 6 keys of the `trade` group (§4.1) |

Three boundaries pinned down (so that the implementation period does not write a second set):

1. **`priceMultiplier = 0` does not affect the reputation discount**: `specialPriceDiff` at `:110` is an independent addend, and vanilla's discount applies as usual, so **the actual payment may fall below the range's lower bound**. What the acceptance assertion targets is the **base price** (`offer.getItemCostA().count()`, `:117-119`), which lies strictly inside the range (§8 #17).
2. **No seeds are given** (document 35 §4.3, note 1); **no vanilla trade item is changed** (only `add`, the list is never cleared).
3. **No second out-of-stock / restock logic is written**: out of stock is carried by `maxUses = 1`, restock by vanilla's `resetUses()` (`:141`). This version **stores no NBT, writes no timer, builds no GUI** (D-158).

#### 2.4.1 Price fluctuation: this document's conclusion is "lock the base price"

Document 35 §4.6 left "whether vanilla's supply-demand / reputation fluctuation breaks through the range" for document 36 to decide. **This document decides: lock the base price, accept the reputation discount**, expressed in code as follows:

| Fluctuation channel | Source location | This document's handling | How it is expressed in code |
|---|---|---|---|
| **supply and demand** (demand) | the `floor(i × demand × priceMultiplier)` of `MerchantOffer#getModifiedCostCount` (`:109`) | **switched off** | `priceMultiplier = 0.0F` (the constructor's 6th argument). Under `maxUses = 1` `demand` is 1 after the first deal, and 1 × 0 is always 0 ⇒ the offer equals the drawn integer |
| **reputation discount** (specialPriceDiff) | `+ this.specialPriceDiff` in the same method (`:110`) | **kept** (vanilla's positive feedback, explicable to the player) | no code is written: `Villager` calls `setSpecialPriceDiff` itself (`:169-171`) |
| "recompute the base price on every restock" | would need a custom restock hook | **rejected** | the offer is produced by `ItemListing#getOffer` as **one `MerchantOffer` at first generation** and stored in the villager's `Offers` NBT; "recompute on every restock" would need its own restock listener + a rewritten NBT, in direct conflict with D-158's "no second restock logic" |

⇒ In one sentence: **the base price of the first offer lies strictly ∈ the range, it does not rise after a deal, and with a discount it only goes lower**. §9 ⑪ records this dependence on `priceMultiplier`'s semantics.

#### 2.4.2 The trade granularity and its cost (ruling #18)

Ruling #18, newly added in `BRIEF` §2, decides: **keep 3 items, randomised per tier; no 22 items, no rotation**. This document complies, and writes the known cost here (not into the implementation):

- the expected number of villagers to collect all 4 epics ≈ `4 × H₄ ≈ 8.3` master-level farmers; to collect all 7 rares ≈ `7 × H₇ ≈ 18.2` journeyman-level farmers (document 35 §4.4's arithmetic; re-checked by this document: `H₄ = 2.0833`, `H₇ = 2.5929`).
- this cost **is accepted** (ruling #18). The only way to shorten it is to add trade items, and adding items means changing D-157.
- the implementation introduces no "per-tier rotation / pity" mechanism at all: `TierTrade` has exactly one `random.nextInt(pool.size())` and no other state.

⚠️ Farmers in an old save that are **already at max level (5) do not get the new trade items retroactively** — the trade list is rebuilt only when a villager levels up, and a max-level villager has no levelling path left. This is vanilla semantics, and **this version does no migration** (no retroactive-grant logic, no change to the saved `Offers`); the player either finds a farmer that has not reached max level and levels it up, or uses a new villager (document 35 §4.6, §7 RT-46, §10 item 16).

### 2.5 The Crop Cultivator (block / BE / item / no menu)

**Decisions**: D-160 (the block itself + anvil properties + no menu), D-161 (dual textures), D-162 (admission), D-163 (600 ticks + output), D-164 (pause when the core is full); risk RT-44 / RT-47.

#### 2.5.1 The block (`block/CultivatorBlock.java`, the 4th new class)

| Item | Value / form | Grounds |
|---|---|---|
| Superclass | `BaseEntityBlock` | the same shape as `ConverterBlock` / `OutpostCoreBlock` |
| `codec()` | `simpleCodec(CultivatorBlock::new)` | follows `OutpostCoreBlock.java:33` |
| `getRenderShape` | `RenderShape.MODEL` | `BaseEntityBlock` defaults to `INVISIBLE`, so it must be declared explicitly (the comment at `ConverterBlock.java:48-52` recorded this pitfall) |
| State property | `static final BooleanProperty WORKING = BooleanProperty.create("working")`, default `false` | **[this document's decision] (4)**: D-161 requires "`working` must go through a blockstate property" |
| `createBlockStateDefinition` | `builder.add(WORKING)` | — |
| `getTicker` | **returns on the server side only**, the client `return null` | word for word the same shape as `OutpostCoreBlock.java:56-67`; HC-03 |

The block's properties, one by one (registered in `FarmstormBlocks`):

| Property | Value | Evidence |
|---|---|---|
| hardness / blast resistance | **`strength(5.0F, 1200.0F)`** | D-160; anvil `Blocks.java:3053` |
| `mapColor` | `MapColor.METAL` | `Blocks.java:3051` |
| `requiresCorrectToolForDrops()` | yes | `Blocks.java:3052` |
| `sound` | `SoundType.ANVIL` | `Blocks.java:3054` |
| `pushReaction` | `PushReaction.BLOCK` | `Blocks.java:3055`; it also prevents "duplicating the contents when pushed by a piston" |
| `noOcclusion` | **not added** | the model is a full cube (`cube_bottom_top`); adding it buys nothing |

#### 2.5.2 The block entity (`blockentity/CultivatorBlockEntity.java`, the 5th new class)

| Item | Content |
|---|---|
| Superclass | `BlockEntity`. **Does not `implements Container`** ([this document's decision], reason: D-160 has no menu; once `Container` is implemented, a hopper / dropper can bypass `useWithoutItem` and stuff an epic crop in, and D-162's admission surface falls on the spot) |
| Fields | `ItemStack held = ItemStack.EMPTY`, `int progress`, `BlockPos corePos` (`null` = unbound; a **reference cache**, the single truth is on the core side, D-165) |
| NBT keys | `Held` (`ItemStack#save` / `ItemStack.parse`, following the form at `ConverterBlockEntity.java:396-416`), `Progress` (int), `Core` (long, `BlockPos#asLong`). **A missing `Core` key = unbound** ([this document's decision] (5): no sentinel value is introduced; a missing key is the semantics) |
| Sync | **no `getUpdateTag` needed**: the client knows only the blockstate's `working` and the two models and reads none of the BE's fields; the binding goes through §2.8's packet |
| tick carrier | **the block side's `getTicker` → the BE's static `serverTick(Level, BlockPos, BlockState, CultivatorBlockEntity)`** ([this document's decision] (6): the same shape as `OutpostCoreBlockEntity.serverTick`; no second tick registration surface) |

**The state machine (once per tick, the body of `serverTick`)**:

```
1. if (held.isEmpty())        → setWorking(false); progress = 0; return;
2. if (!isUsable())           → setWorking(false); return;        // unbound / the core does not recognise it / the core is gone
3. if (progress < cycleTicks) → progress++; setWorking(true); return;
4. // progress == cycleTicks: settle once this tick
   if (acceptAll(batch()))    → held = EMPTY; progress = 0; setWorking(false);
   else                       → setWorking(false);                // paused: progress stays at cycleTicks
```

After step 4 fails, **the next tick reaches step 4 again** (`progress` still equals `cycleTicks`), and delivery completes automatically on the tick the core frees up space — "resume from where it paused, without restarting the clock" (D-164) therefore holds naturally, with no extra state bit.

`setWorking(boolean)` writes the blockstate only when the value really changes (`level.setBlock(pos, state.setValue(WORKING, v), Block.UPDATE_CLIENTS)`), and sends no packet every tick.

#### 2.5.3 Admission (D-162, four gates merged into one test)

```
boolean isUsable():
    corePos != null
 && level.getBlockEntity(corePos) instanceof OutpostCoreBlockEntity core
 && core.hasCultivator(this.worldPosition)                       // a hit in the core-side 16-entry list
 && OutpostCoreBlockEntity.inRange(corePos, this.worldPosition)  // 3×3 chunk coverage
```

| Gate | Document 35's split | Where this document lands it |
|---|---|---|
| 1 range gate | the cultivator's chunk is at Chebyshev distance ≤ 1 from one of your own cores | folded into `isUsable()`: `inRange(corePos, worldPosition)` is true (**reusing** the pure-function form of `OutpostCoreBlockEntity.inRange`, `:151-157`; no second range formula is written) |
| 2 binding gate | it is in the core-side list | `core.hasCultivator(pos)` |
| 3 held-item gate | a Common / Rare crop or its seed | judged **by tier, not by an id whitelist** (D-162 supplement: change the tier of one of the 22 profiles and the admission surface follows automatically) |
| 4 empty-slot gate | nothing in the slot | `held.isEmpty()`; no stacking, no replacing |

The items that can be put in are **23** = 11 Common crops + 7 Rare crops + 5 seeds inside the admission surface (`puffed_wheat_seeds` / `melon_grenade_seeds` / `pumpkin_turret_seeds` / `chive_seeds` / `power_beet_seeds`). Kept outside are **6** = 4 Epic crops + 2 Epic seeds (`awakened_torchflower_seeds` / `awakened_pitcher_plant_pod`).

The order of `useWithoutItem` (**any step that does not pass `return PASS`**: no consumption, no message, no error):

```
if (level.isClientSide) return InteractionResult.SUCCESS;   // 照 ConverterBlock.java:81-83 的 HC-03 取舍
if (!isUsable() || !held.isEmpty() || !admissible(player 手持物)) return InteractionResult.PASS;
held = player 手持物.split(1); progress = 0; setChanged();
return InteractionResult.CONSUME;
```

⚠️ A player who is not an ally / is unbound sees one swing animation on the client (`SUCCESS` is the existing client-side trade-off, following `ConverterBlock`), but **the server consumes no held item and puts nothing into the slot** — "right-click does nothing" is defined on the behaviour surface, not on the animation surface.

#### 2.5.4 Output (D-163, two criteria pinned down)

| Criterion | Form | Why it is written this way |
|---|---|---|
| "has a separate seed item" | `profile.seedItem() != null && !profile.seedItem().location().equals(profile.id())` | the 22 profiles fall into three classes: 7 whose `seedItem` points at **another** item, 8 that point at **themselves**, 7 that are `null`. This boolean expression is true **exactly** for the first class (`seedItem().location()` for the 8 self-pointing ones is `profile.id()`) ⇒ no second table is needed, and "seed = itself" is not counted as having a seed (D-163's supplement rejects exactly this algorithm) |
| Amount | `cultivator.yield` (default 2) | D-163 |
| Cycle | `cultivator.cycleTicks` (default 600) | D-163 |

The output batch (`List<ItemStack>`, computed before delivery):

- with a separate seed: `[crop × yield, seed × yield]` — **5** inside the admission surface (`puffed_wheat` / `melon_grenade` / `pumpkin_turret` / `friend_chives` / `power_beet`);
- without a separate seed: `[crop × yield]` — **13** inside the admission surface (`18 − 5`).

When what is put in is a seed, the output is still decided by the **parent crop profile** (document 35 §6.4 supplement): `SeedItem#profile()` and `ModifiedCropItem#profile()` return the same `CropProfile`, so "putting `chive_seeds` in and putting `friend_chives` in give the same output" needs no extra branch.

#### 2.5.5 The dual-texture switch and the `working` property (D-161)

**5 PNGs + 2 `mcmeta` files** (3 PNGs + 2 `mcmeta` files copied in verbatim from the texture pack; the 2 static first frames taken from `editable_frames/`):

| File | Size | Purpose |
|---|---|---|
| `textures/block/cultivator_a_side.png` + `.png.mcmeta` | 32×512 (16 frames) | the working side's frame strip (`frametime 2`) |
| `textures/block/cultivator_a_top.png` + `.png.mcmeta` | 32×512 (16 frames) | the working top's frame strip (**only 9 of the 16 frames differ**, 1–7 and 9–15 repeat palindromically, see RT-47) |
| `textures/block/cultivator_a_bottom.png` | 32×32 | the bottom face (static, shared by both states) |
| `textures/block/cultivator_a_side_still.png` | 32×32 | the idle side's first frame (copied from `editable_frames/side_00.png`) |
| `textures/block/cultivator_a_top_still.png` | 32×32 | the idle top's first frame (copied from `editable_frames/top_00.png`) |

**The three `cultivator_a_*` names must not be changed**: the texture pack's README explicitly requires the model to reference `farmstorm:block/cultivator_a_top` / `_side` / `_bottom` (D-160 supplement). The two static copies are **new** files, and this document names them (**[this document's decision] (13)**: the `_still` suffix. Reason: `_a_top` / `_a_side` are taken by the frame strips, and the same path cannot point at two images).

| Model file | Parent model | `top` / `side` / `bottom` |
|---|---|---|
| `models/block/cultivator.json` (idle) | `minecraft:block/cube_bottom_top` | `cultivator_a_top_still` / `cultivator_a_side_still` / `cultivator_a_bottom` |
| `models/block/cultivator_working.json` (working) | `minecraft:block/cube_bottom_top` | `cultivator_a_top` / `cultivator_a_side` / `cultivator_a_bottom` |

`blockstates/cultivator.json` (**two states**, through datagen):

```
{"variants":{
  "working=false":{"model":"farmstorm:block/cultivator"},
  "working=true" :{"model":"farmstorm:block/cultivator_working"}}}
```

#### 2.5.6 Breaking drops (D-160 supplement)

`onRemove` follows the **four conditions** of `ConverterBlock.java:88-96` (`!state.is(newState.getBlock())` ∧ `!movedByPiston`) → drops the one cultivation item inside. Because the BE does not implement `Container`, use the `Containers.dropContents(Level, BlockPos, NonNullList<ItemStack>)` overload (`Containers.java:27`) rather than the `Container` overload. The block itself drops through the loot table (§5); the two do not overlap.

### 2.6 The core becomes a container (54 slots + 16 bindings + menu + screen)

**Decisions**: D-165 (16 bindings), D-166 (a 54-slot container + menu + old-save compatibility), D-167 (breaking drops); risk RT-44.

#### 2.6.1 `OutpostCoreBlockEntity` (rewritten)

| Item | Now (`:48`) | After |
|---|---|---|
| Class declaration | `class OutpostCoreBlockEntity extends BlockEntity` | `… extends BlockEntity implements Container` |
| Fields | `ownerUuid` / `ownerName` / `placedGameTime` (`:82-86`), `cleared` (`:92`) | append `NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY)` and `List<BlockPos> cultivators` |
| Container contract | none | `getContainerSize()` → `54`; the seven `isEmpty` / `getItem` / `removeItem` / `removeItemNoUpdate` / `setItem` / `clearContent` / `stillValid` (the per-method form of `ConverterBlockEntity.java:72-146`; `stillValid` uses `Container.stillValidBlockEntity`) |
| NBT | `saveAdditional` / `loadAdditional` write only 3 keys (`:411-424`) | append `Items` (`ContainerHelper.saveAllItems` / `loadAllItems`, `ContainerHelper.java:24-28`, key-name constant `TAG_ITEMS = "Items"`) and `Cultivators` (`ListTag<LongTag>`, `BlockPos#asLong`, ≤ 16 entries) |
| Sync | no `getUpdateTag` / `getUpdatePacket` | both are added (see below) |
| New methods | — | `canAccept(List<ItemStack>)` (a read-only simulation), `acceptAll(List<ItemStack>)` (a real insert), `cultivatorCount()`, `hasCultivator(BlockPos)`, `addCultivator(BlockPos)`, `removeCultivator(BlockPos)`, `cultivators()` |

**The three disciplines of `canAccept` / `acceptAll`**: ① **compute first, write afterwards** — `canAccept` simulates the merge and the occupancy on a copy, and `acceptAll` writes for real only when the simulation passes (otherwise half a batch lands); ② "it fits" is judged **per whole batch** (D-164 supplement); ③ after `acceptAll` succeeds it must `setChanged()` and `sendBlockUpdated` within the same tick (otherwise a player with the screen closed keeps seeing the old contents).

**The size plan for `getUpdateTag` / `getUpdatePacket` (RT-44's straight answer)**:

| Method | What it returns | Why |
|---|---|---|
| `getUpdateTag(HolderLookup.Provider)` | **only the three keys `ownerUuid` / `ownerName` / `placedGameTime`**, **no `Items`, no `Cultivators`** | the client **does not need** either: the 54 items are sent slot by slot by the vanilla menu when the screen opens (`addSlot` + `ClientboundContainerSetSlotPacket`), and the links are sent by §2.8's packet. Pushing 54 `ItemStack`s wholesale into `getUpdateTag` adds several KB every time a chunk is sent to the client for a benefit of 0 |
| `getUpdatePacket()` | `ClientboundBlockEntityDataPacket.create(this)` (the default implementation of `BlockEntity.java:213` is exactly this shape) | fired only at the three **binding-change** moments, by `level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)` (place / migrate / cultivator added or removed) |

Conclusion: **the sync-surface size stays level with 0.1.1**, and the save-surface increment is only the two blocks `Items` + `Cultivators`.

#### 2.6.2 The old-save compatible read (how to initialise when only three keys exist)

| Key in the old save | Read result |
|---|---|
| `ownerUuid` / `ownerName` / `placedGameTime` present (`DeployedOwnership.readUuid/readName` + `tag.getLong`, `:421-423`) | assigned as before |
| `Items` missing | `ContainerHelper.loadAllItems` reads an empty list ⇒ **54 empty slots**, no error, no save wipe |
| `Cultivators` missing | the binding list is empty (an old core bound farmland, not cultivators) |
| keys **extra** in the old save, such as `Bindings` / `ByCore` | not read, not deleted, left as they are inside the `CompoundTag` (this version neither recognises nor cleans them) |

⚠️ Do not read "the old save has no 54 slots" as "all 54 slots are occupied", and do not read the owner keys as items — this is the easiest place to get wrong in RT-44.

#### 2.6.3 Menu + screen (following the `ConverterMenu` / `ConverterScreen` paradigm)

| Component | File | Key points |
|---|---|---|
| Menu | `menu/OutpostCoreMenu.java` (the 6th new class, following the structure of `ConverterMenu`'s 216 lines) | `super(FarmstormMenus.OUTPOST_CORE.get(), id)`; **54 slots** (6 rows × 9 columns, x = 8 + 18×col, y = 18 + 18×row); the player inventory's 3 rows (y = 140) + the hotbar (y = 198); `quickMoveStack` both ways (0–53 ⇄ 54–89); `stillValid` uses `ContainerLevelAccess` |
| Client-side fallback container | same as above | the client constructor uses `new SimpleContainer(54)` (following `ConverterMenu.resolveContainer` `:100-104`) |
| Screen | `client/OutpostCoreScreen.java` (the 7th new class, following `ConverterScreen`'s 86 lines) | `imageWidth = 176`, `imageHeight = 222`; `renderBg` uses the **six-argument** `blit` (the comment at `ConverterScreen.java:69-74` states that the five-argument form computes UVs against 256×256 and shifts everything) |
| Texture | **reuse vanilla** `minecraft:textures/gui/container/generic_54.png` (**[this document's decision] (12)**) | 54 slots = vanilla's double chest, and the slot coordinates (8 / 18 / 140 / 198) with the 176×222 layout are vanilla constants ⇒ **0 new GUI textures** |
| Title key | `container.farmstorm.outpost_core` | following the form of `container.farmstorm.converter` (`ConverterBlock.java:75`) |
| Registration | `FarmstormMenus.java` (+1), the `client/FarmstormClient.java:52-54` area (+1 line `event.register(FarmstormMenus.OUTPOST_CORE.get(), OutpostCoreScreen::new)`) | `menu_types 1 → 2` |

The relation between `storageSlots` and the menu's slot count (D-166 supplement's technical landing point): **read exactly once, when the menu is created**, and that key's value range is pinned to `54..54` (§4.1), so "an already open menu" and "a newly opened menu" cannot disagree.

#### 2.6.4 Block interaction and drops (`block/OutpostCoreBlock.java`)

**Two gates (ruling #17, newly added in `BRIEF` §2: one's own side + within the player's reach)**. The reach **gets no custom constant**; it uses vanilla container-menu `stillValid` semantics directly:

| Gate | Criterion | Where it is written |
|---|---|---|
| A, one's own side | `core.isAlly(player)` (the core owner + teammates, the existing test of D-138) | `useWithoutItem` (before opening the screen) |
| B, reach | **vanilla `stillValid` semantics**: `AbstractContainerMenu.stillValid(access, player, block)` (`AbstractContainerMenu.java:70`) → `Container.stillValidBlockEntity(be, player)` (`Container.java:81-82`, the default distance argument `4.0F`) → `player.canInteractWithBlock(pos, 4.0)` (`Player.java:2229-2232`: `AABB(pos).distanceToSqr(eye) < (blockInteractionRange + 4.0)²`) | in the menu's `stillValid(Player)` override, **copying** `ConverterMenu`'s `Container.stillValidBlockEntity(this, player)` form |

Three readings:

1. **`useWithoutItem` tests A only** (one's own side). B is carried by the menu's per-tick `stillValid` — exactly as with a vanilla chest (a chest does not test distance in `use` either; once opened, `stillValid` closes the menu).
2. **Do not write a distance constant of one's own**: the existing `RANGE_CHUNKS = 1` (a 3×3 chunk area) is the range of **binding / linking**, which is a different thing from "whether the player's hand can reach this block". Using `stillValid` inherits vanilla's `blockInteractionRange()` (default 4.5) + `4.0` eye-to-block-AABB distance, plus "the block in that cell is still itself".
3. The actual feel: the screen can stay open within roughly 8.5 blocks (`4.5 + 4.0`); walk further away and vanilla closes the menu, and right-clicking again re-tests A + B.

| Hook | How it is changed |
|---|---|
| `useWithoutItem` (new) | server: `if (!core.isAlly(player)) return InteractionResult.PASS;` otherwise `player.openMenu(state.getMenuProvider(level, pos))` → `CONSUME`. Client: `return InteractionResult.SUCCESS` (following `ConverterBlock.java:81-83`, HC-03) ⇒ **a non-ally's right-click opens no screen** (D-166 supplement + gate A of ruling #17) |
| `getMenuProvider` (new) | `new SimpleMenuProvider((id, inv, player) -> new OutpostCoreMenu(id, inv, ContainerLevelAccess.create(level, pos)), Component.translatable("container.farmstorm.outpost_core"))` |
| The menu's `stillValid` (new) | `return Container.stillValidBlockEntity(this.core, player);` (if the menu's container is resolved from `ContainerLevelAccess`, use `AbstractContainerMenu.stillValid(this.access, player, FarmstormBlocks.OUTPOST_CORE.get())` — the two are equivalent; take the one consistent with `ConverterMenu`) ⇒ gate B of ruling #17 |
| `onRemove` (changed, `:101-107`) | keep `core.release()`; **append** the four-condition drop: `if (!state.is(newState.getBlock()) && !movedByPiston && level.getBlockEntity(pos) instanceof Container c) { Containers.dropContents(level, pos, c); }` (following `ConverterBlock.java:88-96`) |
| `getStateForPlacement` / `setPlacedBy` / `getTicker` | **untouched** (the placement cap of 2, the ownership write and the server-side ticker stay as they are) |

`PASS` rather than `FAIL` / `SUCCESS`: returning `PASS` means "I do not handle this", which hands the feel back to vanilla and does not swallow other interactions (the existing order of `ServerPlayerGameMode.java:363` → `:370`: `useWithoutItem` is called back only when the item interaction was not consumed). D-170 uses `PASS` for the converter for the same reason.

⚠️ One measure change against document 35: document 35 §8.3 writes "the storage access criterion has **one** gate (one's own side), and the range gate exists only for binding and linking", and §12 B-24 left the interface "if the author wants a range gate". Ruling #17 has brought the range gate back (through vanilla `stillValid`, **not** the 3×3 chunks) ⇒ **that ⚠️ paragraph in document 35 §8.3 is out of date**, and this document is written against two gates (§10 item 13).

### 2.7 Cultivator migration (the three exits when the core is broken)

**Decision**: D-168; risk RT-43. Location: `OutpostCoreBlockEntity#settleOnRemoval` (currently `:341-394`, rewritten as a whole). Keep the existing structure: first test "is it a real removal", then compute the set, and finally touch the data.

```
1. if (!(level instanceof ServerLevel) || ownerUuid == null) return;
2. chunk = getChunkNow(the chunk corePos is in); if (chunk == null) return;           // cannot prove the block is gone ⇒ touch nothing
3. if (chunk.getBlockState(corePos).getBlock() instanceof OutpostCoreBlock) return;   // the block is still there ⇒ unload / shutdown
4. List<BlockPos> bound = cultivators();          // copy before iterating (no modify-while-iterating)
5. for (BlockPos p : bound):
     successor = another core that "covers p ∧ is on your own side ∧ still has a free slot" (nearest first, tie-break key as in D-165)
     if (successor != null)                  → exit ① rebind: the new core's addCultivator(p) + rewrite the cultivator BE's cache
     else if (getChunkNow(the chunk p is in) == null) → exit ③ drop only this core's binding, keep the cultivator's NBT values
     else if (p is no longer a CultivatorBlock)       → exit ③ in the same shape (the block was mined by someone else)
     else                                    → exit ② keep it and mark it unbound (clear the `Core` key only if the BE exists)
6. clear the cultivators list only, **do not clear items** (the items are `onRemove`'s dropContents job; the two hooks divide the work)
7. this.release();                                 // remove the index + release the chunk ticket + request an overlay refresh (existing method, last line untouched)
```

| Exit | Condition | What it does to the cultivator | What it does to the world |
|---|---|---|---|
| ① rebind | a successor core is found | writes the new core's 16-entry list + refreshes the cultivator BE's `Core` cache | **nothing** |
| ② keep and mark unbound | no successor in range ∧ the cultivator's chunk is in memory ∧ that cell is still a cultivator | clears the `Core` key (`Held` / `Progress` kept as they are) ⇒ it becomes "unusable" | **nothing** |
| ③ defensive skip | no successor in range ∧ (the chunk is not in memory ∨ that cell is no longer a cultivator) | writes nothing (the NBT values are kept) | **nothing** |

**Three negative-list items that must be written into comments**:

1. **there is no more `setBlock(DIRT)`**: 0.1.1's `:387` disappears with the whole-section rewrite. The cultivator is an independent block, and "unusable" does not equal "torn down" (D-168 supplement).
2. **no chunk is loaded for the migration**: `getChunkNow` throughout (`ServerChunkCache#getChunkNow`, which only looks up what is loaded and triggers no load). A bound cultivator is necessarily inside the core's 3×3, and `CoreChunkTickets`' tickets hold that ring in memory; `release()` gives up the ticket only on the last line ⇒ those chunks are in memory during the migration.
3. **items do not drop here**: the 54 slots go through `onRemove`'s `Containers.dropContents` (§2.6.4); `settleOnRemoval` handles only the binding and the index.

**Lazy cleanup of the core-side list** ([this document's decision]): `cultivators()` does one `getChunkNow` check on the two low-frequency paths — **choosing a core / placing a binding** — and prunes the entries whose "cell is no longer a cultivator" (loading no chunk). The per-tick `isUsable()` at runtime does **not** do that check (every tick × 16 entries × 16 cultivators = 256 chunk lookups, a cost out of all proportion); it checks only the core-side list hit + `inRange`.

### 2.8 Link rendering (client-side wireframe)

**Decisions**: D-169 (a client-side wireframe + a new packet + sent only within range), D-172 (registered as the 2nd packet); risk RT-45.

#### 2.8.1 The data shape (`network/CultivatorLinkPayload.java`, the 8th new class)

```
record CultivatorLinkPayload(List<Link> links) implements CustomPacketPayload
record Link(BlockPos core, BlockPos cultivator) {}

TYPE      = new Type<>(Farmstorm.id("cultivator_link"))     // [this document's decision] (8)
MAX_LINKS = 1024                                            // [this document's decision] (8)
LINK_STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, Link::core,
        BlockPos.STREAM_CODEC, Link::cultivator, Link::new)
STREAM_CODEC = LINK_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINKS))
```

**Why it carries no `total` and no dimension**: `core_list`'s `total` is "the true total before truncation" (`CoreListPayload.java:20-22`); the links are **the complete set after filtering by player**, never truncated (`MAX_LINKS` is only the decode cap). The dimension is not needed either: a cultivator and its core are necessarily in the same dimension (D-165 supplement), and a cross-dimension link does not exist.

Where `MAX_LINKS = 1024` comes from (arithmetic, both multiplicands being config value-range bounds): the `maxPerPlayer` upper bound **64** × the `cultivatorCapacity` upper bound **16** = 1024. The realistic value is `2 × 16 = 32` (the default of 2 cores per player), and one packet of 32 × 16 bytes ≈ 512 bytes.

Registration: `FarmstormNetwork.onRegisterPayloads` appends a second `.optional().playToClient(...)`, and **the protocol version string must change from `"1"` to `"2"`** (the comment at `FarmstormNetwork.java:17`: it must move whenever the codec moves).

#### 2.8.2 When and to whom to send (`network/CultivatorLinkBroadcaster.java`, the 9th new class)

The structure is the same shape as `CoreListBroadcaster` (215 lines), and its debounce measure is reused (no second set is invented):

| Item | Value | Grounds |
|---|---|---|
| Debounce | `MIN_INTERVAL_TICKS = 10` | carried over from `CoreListBroadcaster.java:31` |
| Range rescan | `RESCAN_INTERVAL_TICKS = 20` (**[this document's decision] (8)**: once a second, covering the event-less path "walking into / out of range"; `core_list`'s reconciliation is 100 ticks, and the link is denser than it because the lines move with position) | — |
| Recipient test | `player.connection.hasChannel(CultivatorLinkPayload.TYPE)` (following `CoreListBroadcaster.java:143`) | an optional channel; a client without the mod should not be sent anything |

**The send / withdraw moments (five, each with its trigger point)**:

| # | Moment | Who triggers it |
|---|---|---|
| 1 | the player enters some core's 3×3 chunk area | the 20-tick rescan in `tick` |
| 2 | the player leaves the range | as above (after the rescan the assembled result is empty ⇒ send an **empty list** to clear the residue) |
| 3 | the binding changes (a cultivator is placed / broken / rebound by a migration) | `requestAll(server)`, the same place as `CoreListBroadcaster.requestAll` (`OutpostCoreBlockEntity.setOwner` / `release`) |
| 4 | the core is broken | as above (`release()` already ends with one `requestAll`, `:185`); the link broadcaster adds one request at the same place |
| 5 | login / dimension change / respawn / logout | the first three follow `FarmstormGameEvents`' existing request points (`:103,120,128`); logout clears the record (following `CoreListBroadcaster.onLogout`, `:114`) |

**The range criterion**: `OutpostCoreBlockEntity.inRange(corePos, playerBlockPos)` — **reuse that static method directly, write no second range formula** (D-169's "the player is inside that core's 3×3 chunk area" and D-165's binding coverage are the same `RANGE_CHUNKS = 1`).

#### 2.8.3 The client (the 10th / 11th new classes)

`client/ClientCultivatorLinks.java`: `volatile List<CultivatorLinkPayload.Link> links = List.of()`, and the handler does only `context.enqueueWork(...)` (following `ClientCoreList.java:24-26`); cleared on a server switch / disconnect (following `:38-41`).

`client/CultivatorLinkRenderer.java` (**the project's first line drawing in world space**; the API is checked, see §9 ②):

| Item | Value | Grounds |
|---|---|---|
| Event / bus | `RenderLevelStageEvent`, `NeoForge.EVENT_BUS`, client only | the class comment of `RenderLevelStageEvent.java:34-35` |
| Stage | **`Stage.AFTER_PARTICLES`** (**[this document's decision] (9)**) | `RenderType.lines()`'s composite state is `TRANSLUCENT_TRANSPARENCY` + `VIEW_OFFSET_Z_LAYERING` (`RenderType.java:604-612`), while `AFTER_TRANSLUCENT_BLOCKS`'s official comment states outright that translucency may be incorrect at that stage and that `AFTER_TRIPWIRE_BLOCKS` or `AFTER_PARTICLES` should be used instead |
| Geometry carrier | **create** a new `PoseStack`, `translate(-cam.x, -cam.y, -cam.z)` (**do not** modify the event's `getPoseStack()`, which would translate the later stages as well) | `RenderLevelStageEvent#getCamera()` (`:113`), `#getPoseStack()` (`:78`) |
| Vertex source | `Minecraft.getInstance().renderBuffers().bufferSource()` → `getBuffer(RenderType.lines())` | `WildHunterOutline.java:95` is an existing precedent of the same statement (it only takes the buffers and draws no line) |
| Drawing | **`LevelRenderer.renderLineBox(PoseStack, VertexConsumer, AABB, float, float, float, float)`** (`LevelRenderer.java:2252-2253`, public static) + `buffers.endBatch(RenderType.lines())` | vanilla itself uses it to draw wireframes (`LevelRenderer.java:2331-2354` is its vertex form); it is steadier than hand-written vertices: `POSITION_COLOR_NORMAL`'s three elements (`RenderType.java:601`) — miss one and `BufferBuilder.endLastVertex` throws `Missing elements in vertex` (`WildHunterOutline.java:54-57` recorded the same class of crash) |
| The two ends of a line | the core's centre `(x+0.5, y+0.5, z+0.5)` → the cultivator's centre `(x+0.5, y+0.5, z+0.5)`; a 0.25-block `AABB` wireframe at each end | [this document's decision] (9): the end boxes make "which end is the core" obvious at a glance, at the cost of 24 more vertices per pair |
| Colour | RGB `0.30F / 0.85F / 0.90F`, alpha `1.0F` | **the same set of values as the core boundary particles** (`DustParticleOptions(new Vector3f(0.30F, 0.85F, 0.90F), 1.0F)` at `OutpostCoreBlockEntity.java:63`) — the same visual semantics (the core's range) uses the same colour, and no new value is invented |
| Line width | `RenderType.lines()`'s own `1.0` (`RenderStateShard.LineStateShard(OptionalDouble.empty())`, `RenderType.java:606`) | [this document's decision] (9): changing the line width would require building one's own `RenderType`, and this version does not |
| Occlusion | decided by `RenderType.lines()`'s depth test (no seeing through terrain) | `COLOR_DEPTH_WRITE` at `RenderType.java:610` |

**The client's three read-only rules** (D-169 supplement): ① draw what is received, derive no binding; ② no cross-dimension cache (cleared by `clear()` after a dimension change); ③ an **empty list must really empty** the cache after leaving the range (not "ignore empty packets").

### 2.9 Retiring the converter (right-click does nothing, recipes kept)

**Decision**: D-170.

| Object | How it is changed | What is kept |
|---|---|---|
| `useWithoutItem` at `block/ConverterBlock.java:78-86` | replaced as a whole by `return InteractionResult.PASS;` (the same statement on client and server, no more branching) | — |
| `getMenuProvider` (`:70-76`) | **kept as it is** (nobody calls it) | no rewrite needed when it is restored later (the plan says "temporarily removed") |
| `ConverterMenu` / `ConverterScreen` / `FarmstormMenus.CONVERTER` | **untouched** | the menu chain and the screen registration keep compiling |
| the 26 `farmstorm:converting` recipes + 2 shaped + 1 shapeless (29 jsons in total) | **untouched** | the only carrier of the "vanilla crop ⇄ modified crop" mapping |
| the creative-tab entry (`FarmstormItems.creativeTabItems()` `:125`) | **untouched** | the plan: it serves only as a decorative block, and a decorative block must be obtainable |
| block / BE / textures / language keys | **untouched** | — |
| **The Chinese name (ruling #19)** | **keeps 「农产品转换器」**: not one character of the **values** of the two language keys `block.farmstorm.converter` and `item.farmstorm.converter` changes | `ModLangProvider.java:94`, `:128`. The plan's title says 「农作物转换器」, but that is the requirement's title, not the block name; renaming would make the item names in old saves and in the wiki screenshots all fail to match |

**Why `PASS` and not `FAIL` / `SUCCESS`**: `SUCCESS` / `CONSUME` swallow the right-click event (blocking the held item's use on that cell); `FAIL` makes the caller believe "it was explicitly refused". `PASS` = "I do nothing here" (the same reason as `SeedItem#useOn` in §2.2).

**One knock-on consequence that must be remembered** (already registered in document 35 §9.2): with the converter retired, the old path "vanilla crop → this mod's crop" is cut, and the player's remaining entries to obtain crops are only "villager trades (§2.4) + the cultivator (§2.5) + loot + the creative tab". That is not a defect; it is the result of R2 + R10 stacking.

---

## 3. Master table of the action surface (additions / changes / deletions against 0.1.1, 46 rows line by line)

Column notes: **Kind** = registry / class / data / config / language / retired. **Current** = the actual content of the released 0.1.1 state; **After** = v0.2.0's target state. Decision numbers always cite document 34, risk numbers always cite `BRIEF` §4.

| # | Kind | Path or class#method | Current | After | Decision | Risk |
|---|---|---|---|---|---|---|
| 1 | registry | `registry/FarmstormBlocks.java` | 26 | **27** (+`cultivator`) | D-171 | RT-44 |
| 2 | registry | `registry/FarmstormBlockEntities.java` | 7 | **8** (+`cultivator`) | D-171 | RT-44 |
| 3 | registry | `registry/FarmstormItems.java` | 32 | **33** (+`cultivator`'s BlockItem) | D-171 | — |
| 4 | registry | `registry/FarmstormMenus.java` | 1 (`converter`) | **2** (+`outpost_core`) | D-166, D-171 | RT-44 |
| 5 | registry | the other 8 classes (entity_types / mob_effects / sound_events / recipe_types / recipe_serializers / creative_tabs / damage_types / data_components) | 13 / 4 / 3 / 1 / 1 / 1 / 3 / 1 | **unchanged** | D-171 | — |
| 6 | class | `util/CropTier.java` | does not exist | **a new enum** (`COMMON`/`RARE`/`EPIC` + `translationKey()` + `color()` + `nameStyle`) | D-149, D-150 | RT-49 |
| 7 | class | `util/CropProfile.java:32-40` | an 8-field record | **9 fields** (+`CropTier tier`, positioned after `throwStack`) | D-150 | RT-49 |
| 8 | data | `util/CropProfile.java:47-128` | 22 profiles with no tier | **all 22 gain a tier** (11 / 7 / 4) | D-149 | RT-49 |
| 9 | data | `util/CropProfile.java:139-143` | a pool of 19 | **11** (the Common tier's whole set only) | D-156 | RT-50 |
| 10 | class | `item/ModifiedCropItem#getName(ItemStack)` | inherits `Item` (no override) | override → coloured by tier (covering 15 items) | D-151 | RT-49 |
| 11 | class | `item/DeployableCropItem#getName(ItemStack)` | inherits `BlockItem` (not `ModifiedCropItem`) | override → coloured by tier (7 items) | D-151 | RT-49 |
| 12 | class | `item/SeedItem#getName(ItemStack)` | inherits `ItemNameBlockItem` | override → follows the parent crop's tier colour (7) | D-151 | RT-49 |
| 13 | class | `item/ModifiedCropItem#appendCropTooltip` (`:113-127`) | adds the category line only | **append 1 tier line** at the end (3 call sites cover all 29 items) | D-151 | — |
| 14 | class | `item/ModifiedCropItem#isPlantableSoil` (`:101-107`) | allowed only on `FarmBlock` | **a constant `false`** (the original test stays as a comment) | D-152 | RT-48 |
| 15 | class | `item/SeedItem#useOn` | inherits vanilla sowing from `BlockItem#useOn` | **override to return `PASS`** | D-152 | RT-48 |
| 16 | class | `block/FarmstormCropBlock` | `mayPlaceOn` / `canSurvive` not overridden | **untouched** (no refusal on the block side) | D-152, D-155 | RT-48 |
| 17 | retired | `event/FarmstormGameEvents.java:75` | registers `onRightClickBlock` | **commented out** (with a reason line) | D-153 | RT-42 |
| 18 | retired | `event/FarmstormGameEvents.java:76` | registers `onFarmlandTrample` | **commented out** (with a reason line) | D-153 | RT-42 |
| 19 | retired | `event/FarmstormGameEvents.java:241-265` | hoeing interception + writing the binding + 2 action-bar messages | **the whole block commented out** | D-153 | RT-42 |
| 20 | retired | `event/FarmstormGameEvents.java:277-281` | trample protection | **the whole block commented out** | D-153 | RT-42 |
| 21 | retired | `event/FarmstormGameEvents.java:171` | `FarmlandLedger.get(server).backfill(server)` | **only this line commented out** (`:166-176`, `:170` and `:175` are kept) | D-153, D-154 | RT-42 |
| 22 | retired | `config/FarmstormConfig.java:909-912` | the `outpost_core.farmlandBonus` definition | **the definition is commented out**; the field declaration at `:227` **is kept** (§2.3's exception) | D-153 | RT-42 |
| 23 | retired | `datagen/ModLangProvider.java:403` | `farmstorm.configuration.outpost_core.farmlandBonus` | **commented out** | D-153 | RT-42 |
| 24 | retired | `datagen/ModLangProvider.java:412-413` | `message.farmstorm.farmland.no_core` / `quota_full` | **commented out** (2 entries) | D-153 | RT-42 |
| 25 | data | `farmland/FarmlandLedger.java` / `FarmlandGuard.java` | 555 + 225 lines, each with callers | **not one line changed**, 0 runtime callers | D-153 | RT-42 |
| 26 | data | `farmstorm_farmland.dat` | read and written by `get()` at startup | **not read, not written, not deleted** | D-154 | RT-42 |
| 27 | class | `event/VillagerTradeEvents.java` | does not exist | **new** (a `VillagerTradesEvent` subscription + 3 items) | D-157 | RT-46 |
| 28 | class | `event/TierTrade.java` | does not exist | **new** (an `ItemListing` implementation, the offer is drawn at generation) | D-157, D-159 | RT-46 |
| 29 | data | the 3 trade items | none | levels **1 / 3 / 5**; `maxUses = 1`; 2 crops per deal | D-157, D-158 | RT-46 |
| 30 | class | `block/CultivatorBlock.java` | does not exist | **new** (the `working` property + `getTicker` + `useWithoutItem` + `onRemove` drops) | D-160, D-162 | RT-44, RT-47 |
| 31 | class | `blockentity/CultivatorBlockEntity.java` | does not exist | **new** (does not implement `Container`; `Held` / `Progress` / `Core`) | D-160, D-163 | RT-44 |
| 32 | data | the cultivator's cycle / output | none | **600 ticks**; 2 crops (5 of them +2 seeds); pauses when the core is full | D-163, D-164 | RT-44 |
| 33 | class | `blockentity/OutpostCoreBlockEntity.java:48` | only `extends BlockEntity` | **`implements Container` (54 slots)** | D-166 | RT-44 |
| 34 | data | `OutpostCoreBlockEntity` NBT (`:411-424`) | 3 keys | **+`Items` (54 slots) +`Cultivators` (≤16 entries)** | D-165, D-166 | RT-44 |
| 35 | class | `OutpostCoreBlockEntity#getUpdateTag` | does not exist | **new** (writes the owner's three keys only, no items) | D-166 | RT-44 |
| 36 | class | `OutpostCoreBlockEntity#getUpdatePacket` | does not exist | **new** (`ClientboundBlockEntityDataPacket.create(this)`) | D-166 | RT-44 |
| 37 | class | `OutpostCoreBlockEntity#settleOnRemoval` (`:341-394`) | the farmland's three exits (including `setBlock(DIRT)` at `:387`) | **the cultivator's three exits** (rebind / mark unbound / skip), **no turning into dirt** | D-168 | RT-43 |
| 38 | class | `block/OutpostCoreBlock#useWithoutItem` | does not exist | **new** (allies open the screen, non-allies get `PASS`) | D-166 | RT-44 |
| 39 | class | `block/OutpostCoreBlock#onRemove` (`:101-107`) | only `release()` | **append** the four-condition `Containers.dropContents` | D-167 | RT-44 |
| 40 | class | `menu/OutpostCoreMenu.java` + `client/OutpostCoreScreen.java` | does not exist | **2 new classes** (54 slots + 176×222, reusing vanilla `generic_54.png`) | D-166 | RT-44 |
| 41 | class | `network/CultivatorLinkPayload.java` | does not exist | **new** (`farmstorm:cultivator_link`, `MAX_LINKS = 1024`) | D-169, D-172 | RT-45 |
| 42 | class | `network/CultivatorLinkBroadcaster.java` + `client/ClientCultivatorLinks.java` + `client/CultivatorLinkRenderer.java` | does not exist | **3 new classes** (debounce 10 / rescan 20 ticks; `RenderType.lines()`) | D-169 | RT-45 |
| 43 | class | `network/FarmstormNetwork.java:17,29-31` | 1 packet, protocol version `"1"` | **2 packets, protocol version `"2"`** | D-169, D-172 | RT-45 |
| 44 | retired | `block/ConverterBlock.java:78-86` | client `SUCCESS` + server `openMenu` | **returns `PASS`** (menu / recipes / creative tab kept) | D-170 | — |
| 45 | config | `config/FarmstormConfig.java` | 166 keys / 58 groups | **175 keys / 60 groups** (−1 +2 +2 +6) | D-159, D-163, D-165, D-166 | — |
| 46 | data | `Farmstorm.java:119` the pool assertion / `:147-150` the registry assertions | `19` / `93` + the 12 class breakdown | **`11`** / **`97`** + the breakdown (items 33 / blocks 27 / BE 8 / menu 2) | D-156, D-171 | RT-50 |

**The 46 rows' kind counts**: **registry 5** (#1~5) + **class 23** (#6,7,10~16,27,28,30,31,33,35~43) + **data 8** (#8,9,25,26,29,32,34,46) + **retired 9** (#17~24,44) + **config 1** (#45) = **46**. **Not one deletion / retirement is "deleting a file"**: `FarmlandLedger` / `FarmlandGuard` / `ConverterMenu` / `ConverterScreen` / the 26 recipes / the 15 crop blocks / the 22 crop items / the 7 seeds / the 26 loot tables **are all kept**.

---

## 4. The full config inventory

### 4.1 server (key count per group + the total; **named by this document itself**)

**The measure**: read the static block of `FarmstormConfig.java` from `:267` (`static {`) to `:927` (`SPEC = b.build();`), landing every key group by group. The result is **166 keys / 58 groups = 34 top-level groups + 24 nested groups**.

| Group | Keys | Nested detail | v0.2.0 |
|---|---|---|---|
| `converter` | 3 | — | untouched |
| `stack` | 2 | — | untouched |
| `deploy` | 3 | — | untouched |
| `teams` | 2 | — | untouched |
| `explosion` | 2 | — | untouched |
| `throwable` | 1 | — | untouched |
| `food` | 1 | — | untouched |
| `puffed_wheat` | 2 | — | untouched |
| `blast_carrot` | 5 | `nausea` 2 | untouched |
| `ubi_server` | 2 | — | untouched |
| `toxic_ubi_server` | 4 | — | untouched |
| `melon_slice_bomb` | 2 | — | untouched |
| `melon_grenade` | 6 | — | untouched |
| `pumpkin_turret` | 5 | — | untouched |
| `awakened_torchflower` | 16 | 6 nested groups × 2 (strength / resistance / haste / speed / water_breathing / night_vision) | untouched |
| `awakened_pitcher_plant` | 10 | 3 nested groups × 2 (regeneration / saturation / absorption) | untouched |
| `fang_trap_cactus` | 9 | — | untouched |
| `bamboo_firecracker` | 2 | — | untouched |
| `red_mushroom_puff` | 5 | `nausea` 2 + `blindness` 2 | untouched |
| `brown_mushroom_puff` | 5 | `weakness` 2 + `slowness` 2 | untouched |
| `lucky_berry` | 24 | 5 effect groups × 2 + `chance` 8 | untouched |
| `power_beet` | 10 | 4 nested groups × 2 (strength / speed / haste / resistance) | untouched |
| `molotov` | 3 | — | untouched |
| `wild_apple` | 2 | — | untouched |
| `wild_hunter` | 11 | — | untouched |
| `zangief_cane` | 8 | — | untouched |
| `qbounce` | 4 | — | untouched |
| `friend_chives` | 3 | — | untouched |
| `point_defense_fungus` | 3 | — | untouched |
| `vibration_bean` | 2 | — | untouched |
| `growth` | 2 | — | untouched |
| `trap` | 3 | — | untouched |
| `outpost_core` | **3** | — | **changed: −1 and +2 ⇒ 4** |
| `compressed` | 1 | — | untouched |
| `cultivator` | — | — | **new, 2** |
| `trade` | — | — | **new, 6** |
| **current total (34 groups)** | **166** | **24 nested groups** | — |
| **after the change (36 groups)** | **175** | **24** (unchanged) | **net +9** |

The per-group sum check (current): `3+2+3+2+2+1+1+2+5+2+4+2+6+5+16+10+9+2+5+5+24+10+3+2+11+8+4+3+3+2+2+3+3+1 = 166` ✓

#### 4.1.1 The 11 rows of additions / changes / deletions (named key by key)

| # | Full key path | Type | Default | Range | Action | Decision |
|---|---|---|---|---|---|---|
| 1 | `outpost_core.farmlandBonus` | int | ~~16~~ | ~~0–1024~~ | **delete** (the definition is commented out, the field is kept) | D-153 |
| 2 | `outpost_core.cultivatorCapacity` | int | **16** | **1–64** | **add** | D-165 |
| 3 | `outpost_core.storageSlots` | int | **54** | **54–54** | **add** | D-166 |
| 4 | `cultivator.cycleTicks` | int | **600** | **20–72000** | **add** (a new group) | D-163 |
| 5 | `cultivator.yield` | int | **2** | **1–64** | **add** (a new group) | D-163 |
| 6 | `trade.common.min` | int | **2** | **1–64** | **add** (a new group) | D-159 |
| 7 | `trade.common.max` | int | **5** | **1–64** | **add** | D-159 |
| 8 | `trade.rare.min` | int | **10** | **1–64** | **add** | D-159 |
| 9 | `trade.rare.max` | int | **32** | **1–64** | **add** | D-159 |
| 10 | `trade.epic.min` | int | **40** | **1–64** | **add** | D-159 |
| 11 | `trade.epic.max` | int | **64** | **1–64** | **add** | D-159 |

(11 rows = 1 deletion + 2 changed groups + 2 new groups 8; `166 − 1 + 2 + 2 + 6 = 175`.)

**The reasons for the five value ranges** (document 34 gives only the defaults; the ranges belong to the technical side):

| Key | Where the range comes from |
|---|---|
| `cultivatorCapacity` 1–64 | the lower bound 1 = "at least one can be bound"; the upper bound 64 = "the same order of magnitude as the stack limit, and multiplied by `maxPerPlayer`'s upper bound it is exactly §2.8's `MAX_LINKS = 1024`" |
| `storageSlots` **54–54** | **[this document's decision]**: D-166 states outright "54 must not become any other value", and 54 is at the same time the screen layout (6×9) and the return value of `getContainerSize()`; pinning it has one fewer inconsistency path than "read it once and clamp it". Whether `ModConfigSpec` accepts a degenerate range with `min == max` is in §9 ⑬ (the way out: change to `54..55` and clamp to 54 when creating the menu) |
| `cycleTicks` 20–72000 | the lower bound 20 = 1 second (any shorter is not "cultivating"); the upper bound 72000 follows the upper bound of the same file's existing `deploy.turretMiningTicks` (`FarmstormConfig.java:295`), and 72000 = 3600 × 20 |
| `yield` 1–64 | the upper bound 64 = vanilla's stack limit; the lower bound 1 = producing 0 is not allowed (otherwise the progress is wasted) |
| `trade.*` 1–64 | the emerald stack limit is 64; the lower bound 1 = giving it away is not allowed |

The `trade` group's three runtime disciplines (written where the value is read, not as a third mechanism): ① when `min > max` **swap** (§2.4 step 2); ② when `min == max` take that value (the range degenerates to a single value, no crash); ③ the three ranges are **not cross-checked** (a Rare range set lower than the Common one is allowed — that is the player's own world).

#### 4.1.2 The measure of groups and sections

- **Group** = the number of `b.push(...)` calls (58 → 60), the same number as the `[section]` lines in the on-disk toml.
- The new `cultivator` and `trade` are **top-level groups** (at the same level as `outpost_core`), so the top-level groups go 34 → **36** and the nested groups stay **24**.
- The `outpost_core` section still exists (3 keys → 4 keys); this is **not** "delete the group and rebuild it" — deleting the group would invalidate all three old values of that section in an old toml.

### 4.2 client (3 keys, unchanged)

| Group | Key | Type | Default | Range | Carrier |
|---|---|---|---|---|---|
| `hud` | `hud.enabled` | boolean | `true` | — | `config/farmstorm-client.toml` |
| `hud` | `hud.x` | int | `4` | 0–3840 | as above |
| `hud` | `hud.y` | int | `4` | 0–2160 | as above |

**Untouched by this version**: not one of §2's nine components is a client setting. These 3 keys are **not counted into** §4.1's 175 (the existing measure of `BRIEF` §0), but they **are counted into** the language keys' `farmstorm.configuration.*` (see §4.3).

### 4.3 Language-key additions and deletions (named one by one + the total counted out by this document itself)

**Currently 323** (method in §1.3). This version: **−3 + 17 = +14 ⇒ 337**.

#### 4.3.1 Deletions (3)

| # | Key | Current location | Why it is deleted |
|---|---|---|---|
| 1 | `farmstorm.configuration.outpost_core.farmlandBonus` | `ModLangProvider.java:403` | the key's definition is already commented out (§4.1 row 1) |
| 2 | `message.farmstorm.farmland.no_core` | `:412` | its only emitting point `FarmstormGameEvents:260-264` is commented out |
| 3 | `message.farmstorm.farmland.quota_full` | `:413` | as above |

After the deletion the whole `message.farmstorm` prefix bucket goes to **0** — the only prefix this version empties.

#### 4.3.2 Additions (17, named one by one)

| # | Key | Chinese | English | Count | Decision |
|---|---|---|---|---|---|
| 1 | `item.farmstorm.cultivator` | 农作物培养器 | Cultivator | 1 | D-160 |
| 2 | `block.farmstorm.cultivator` | 农作物培养器 | Cultivator | 1 | D-160 |
| 3 | `container.farmstorm.outpost_core` | 据点核心 | Outpost Core | 1 | D-166 |
| 4–6 | `tooltip.farmstorm.tier.common` / `.rare` / `.epic` | 普通 / 稀有 / 史诗 | Common / Rare / Epic | 3 | D-150, D-151 |
| 7 | `tooltip.farmstorm.tier` | 品质：%s | Tier: %s | 1 | D-151 |
| 8–9 | `farmstorm.configuration.cultivator.cycleTicks` / `.yield` | 培养周期（刻） / 单次产出数量 | Cultivation cycle in ticks / Items per cycle | 2 | D-163 |
| 10–11 | `farmstorm.configuration.outpost_core.cultivatorCapacity` / `.storageSlots` | 每个核心可绑定的培养器数量 / 核心存储的格子数 | Cultivators bound per core / Storage slots per core | 2 | D-165, D-166 |
| 12–17 | `farmstorm.configuration.trade.{common,rare,epic}.{min,max}` | 普通 / 稀有 / 史诗档的绿宝石价格下界与上界 | Common / Rare / Epic trade price lower / upper bound | 6 | D-159 |

`1 + 1 + 1 + 3 + 1 + 2 + 2 + 6 = 17`.

Two measures pinned down:

1. **the block name and the item name are 1 entry each** (not 1 shared entry): in 0.1.1 `block.farmstorm.outpost_core` and `item.farmstorm.outpost_core` are already two entries (the comments at `ModLangProvider.java:107` and `:37`); this follows that.
2. **config language keys and server config keys are 1:1**: 10 new config keys ⇒ 10 new `farmstorm.configuration.*` entries (`169 − 1 + 10 = 178`).

#### 4.3.3 The total after the change (337) and the per-prefix check

```
item.farmstorm             32 → 33    (+1)
block.farmstorm            26 → 27    (+1)
container.farmstorm         1 →  2    (+1)
tooltip.farmstorm          16 → 20    (+4)
farmstorm.configuration   169 → 178   (−1 deleted + 10 added)
message.farmstorm           2 →  0    (−2)
the other 9 prefix buckets             (unchanged)
--------------------------------------------
323 − 3 + 17 = 337
```

The per-prefix sum check (after the change): `178 + 36 + 33 + 27 + 20 + 13 + 9 + 6 + 4 + 3 + 3 + 0 + 2 + 1 + 2 = 337` ✓

**The zh and en key sets being identical** is guaranteed by how `ModLangProvider` is built (the same `ENTRIES` table written twice, column by column, `:64-68`), so `zh = en = 337`, zh-only = 0 and en-only = 0 are a structural conclusion.

**One comment that must be changed along the way**: the class comment at `ModLangProvider.java:13-16` writes out "32 item + 26 block + … + 169 config + … = 323" item by item, and after the datagen lands it must be brought into line as "33 / 27 / 178 / 20 / 0 / 337" — that passage itself says "this section must be updated whenever the table changes".

---

## 5. Resource inventory

**12 new files**: PNG **5** + mcmeta **2** + blockstate 1 + block model 2 + item model 1 + loot table 1. **No existing texture is changed.**

| # | File (relative to `src/main/resources` or `src/generated/resources`) | Produced by | Note |
|---|---|---|---|
| 1 | `assets/farmstorm/textures/block/cultivator_a_side.png` | copied by hand (texture pack) | 32×512, a 16-frame strip |
| 2 | `assets/farmstorm/textures/block/cultivator_a_side.png.mcmeta` | copied by hand | `frametime 2` / `interpolate false` / `frames 0..15` |
| 3 | `assets/farmstorm/textures/block/cultivator_a_top.png` | copied by hand | 32×512, a 16-frame strip (only 9 frames differ, RT-47) |
| 4 | `assets/farmstorm/textures/block/cultivator_a_top.png.mcmeta` | copied by hand | **byte for byte identical** to #2 |
| 5 | `assets/farmstorm/textures/block/cultivator_a_bottom.png` | copied by hand | 32×32, static |
| 6 | `assets/farmstorm/textures/block/cultivator_a_side_still.png` | copied by hand (`editable_frames/side_00.png`) | a 32×32 static first frame (the file name is **[this document's decision]**) |
| 7 | `assets/farmstorm/textures/block/cultivator_a_top_still.png` | copied by hand (`editable_frames/top_00.png`) | a 32×32 static first frame |
| 8 | `assets/farmstorm/blockstates/cultivator.json` | datagen (`ModBlockStateProvider`) | **two states**: `working=false` / `working=true` |
| 9 | `assets/farmstorm/models/block/cultivator.json` | datagen | idle: parent `cube_bottom_top`, three faces → `_still` / `_still` / `_bottom` |
| 10 | `assets/farmstorm/models/block/cultivator_working.json` | datagen | working: parent `cube_bottom_top`, three faces → `_top` / `_side` / `_bottom` |
| 11 | `assets/farmstorm/models/item/cultivator.json` | datagen | `withExistingParent("cultivator", "farmstorm:block/cultivator")` |
| 12 | `data/farmstorm/loot_table/blocks/cultivator.json` | datagen | drops itself (1 item table, following `outpost_core`'s `createSingleItemTable`) |

**Increment reconciliation**: PNG `113 → 118` (+5); `*.png.mcmeta` `0 → 2`; `blockstates` `26 → 27`; `models` (block + item together) `103 → 106` (+2 block models +1 item model); `loot_table` `26 → 27`; `recipe` still **29** (unchanged).

**0 new GUI textures**: the core storage screen reuses vanilla `minecraft:textures/gui/container/generic_54.png` (§2.6.3, [this document's decision] (12)).

**Three "do not touch" items written as a negative list** (otherwise the acceptance will go and change them):

| What is not touched | Count | Why |
|---|---|---|
| the 22 crop item textures | 22 | D-151: the tier affects only the name color and the tooltip (the 29 in `textures/item` = 22 crops + 7 seeds) |
| the 7 seed item textures | 7 | as above |
| the 15 crop block textures and the existing block textures | the rest of the 79 | this version changes no existing block appearance |

**Two knock-on datagen changes (`BRIEF` does not cover them, see §10 item 10)**:

1. `ModItemModelProvider.usesBlockModel(Block)` (`:57-59`) currently recognises only `ConverterBlock \|\| OutpostCoreBlock` ⇒ **`|| block instanceof CultivatorBlock` must be added**. Otherwise `registerModels` falls through to `basicItem(item)` (`:50-51`), which demands a **non-existent** `textures/item/cultivator.png`, and in game it shows as a purple-black cell.
2. `ModBlockStateProvider` gains `cultivatorStates(Block)`: the ready-made `simpleBlock(block, model)` cannot be used (it generates the single-state `"": {...}`); one must write `getVariantBuilder(block).partialState().with(CultivatorBlock.WORKING, false).modelFile(idle)` + `.partialState().with(WORKING, true).modelFile(working)`.

---

## 6. Save and compatibility

**Four items, which must be written separately** (document 34 §9 ⑨ lists this as the most easily confused place; this document gives a criterion for each of "not read, not written, not deleted / index unchanged / chunk NBT heavily changed / a new BE").

### ① `farmstorm_farmland.dat`: not read, not written, not deleted

| Item | Content |
|---|---|
| On-disk name | `farmstorm_farmland` (`FarmlandLedger.java:53`) ⇒ `world/data/farmstorm_farmland.dat` |
| Current format | `dataVersion: 2` (`:55`, `:34`), three tables `bindings` / `byCore` / `orphans` (`:89,92,95`) |
| v0.2.0 | **neither read, nor written, nor deleted** (D-154): once `FarmlandLedger.get(...)`'s (`:98-101`) runtime call sites are zeroed by §2.3's comment points, this `SavedData` is no longer `get()` ⇒ the file is not rewritten on save |
| Acceptance criterion | after an old save is upgraded: the file **is still there**, its **mtime is unchanged**, its content is **byte for byte unchanged**; `latest.log` carries 0 records related to reading or writing `farmstorm_farmland` |
| Why it cannot be deleted | delete it and there is no way back; "this version does not read it" is a temporary non-use, and if that system or a migration tool is ever restored the original data is still there (D-154 supplement) |

### ② `farmstorm_outpost_core.dat` (the index): entries unchanged

| Item | Content |
|---|---|
| Class | `outpost/OutpostCoreIndex.java`, `DATA_NAME = "farmstorm_outpost_core"` (`:50`) |
| Entry format | `record Entry(ResourceLocation dimension, BlockPos pos, long gameTime)` (`:67`), persisted as `owners → {uuid → {name, entries[]}}` |
| v0.2.0 | **neither the format nor the semantics changes**: this version adds no field to the index (the 16 cultivator bindings grow on the core BE, not in the index) |
| Acceptance criterion | after the upgrade the index's entry count and coordinate set are unchanged; the number of cores shown in the overlay list (`core_list`) matches the pre-upgrade one |

### ③ The core BE's NBT: in the **chunk save**, and the structure changes heavily

| Item | Content |
|---|---|
| Location | the **chunk save** (the BE's `saveAdditional` inside the region file), **not** in ②'s `.dat` |
| Current | 3 keys: `ownerUuid` / `ownerName` (`DeployedOwnership.java:18-19`) + `placedGameTime` (`OutpostCoreBlockEntity.java:50`) |
| v0.2.0 | **+`Items` (54 slots, `ContainerHelper`'s list) +`Cultivators` (≤16 Longs)** |
| Compatible read | missing `Items` ⇒ 54 empty slots; missing `Cultivators` ⇒ an empty list; the three keys present ⇒ assigned as before (§2.6.2) |
| Sync surface | `getUpdateTag` **writes only the three keys** (no items, no bindings) ⇒ the network size stays level with 0.1.1 (§2.6.1) |
| Acceptance criterion | place a core in an old save → it opens as 54 empty slots with no error; store 3 items → they are still there after a restart; `/data get block <pos>` shows the two keys `Items` and `Cultivators` |

### ④ The new `cultivator` BE's NBT

| Key | Type | Meaning when absent | Note |
|---|---|---|---|
| `Held` | `CompoundTag` (`ItemStack#save`) | an empty slot | at most 1 item inside |
| `Progress` | `int` (ticks) | 0 | the value range `0..cycleTicks`; when paused it stays at `cycleTicks` |
| `Core` | `long` (`BlockPos#asLong`) | **a missing key = unbound** | a reference cache; the single truth is on the core side, checked before use (D-165) |

**The rollback direction**: taking a v0.2.0 save back to 0.1.1, the `cultivator` block and its BE become "unknown block / unknown BE" and are dropped by vanilla; `outpost_core`'s two keys `Items` / `Cultivators` are ignored by 0.1.1's `loadAdditional` (it reads only its own 3 keys). **No downgrade code is written**: handling an unknown block belongs to vanilla, and this version does not take it over.

---

## 7. Risk register RT-42 ~ RT-50 (each refined down to "how to verify it")

| Id | Risk | Trigger | Impact | Mitigation | Which acceptance items cover it |
|---|---|---|---|---|---|
| **RT-42** | retiring the farmland system means "commenting out the call sites", not deleting the code: the two classes still exist, and an accidental restore revives the old rules | ① someone restores the comments at `:75`/`:76` as if they were a mistaken comment-out; ② someone "tidies up" the two classes with no callers | ① hoeing is blocked again, trampling is protected again, the startup backfill runs again, and the player sees 0.1.1's old behaviour; ② after the classes are deleted they can never be restored, and the `null` field at `FarmstormConfig.java:227` would make the very first hoe stroke after restoring the listeners throw an NPE | three lines of defence: (a) one reason line above each comment-out — "ruling #3: v0.2.0 retires the farmland system, do not restore"; (b) `FarmstormConfig.java:227` carries the extra line "this key's definition is already commented out ⇒ the field is null, restore the definition before restoring the listeners" (§2.3); (c) one line at the top of each of `FarmlandLedger` / `FarmlandGuard`'s class comment — "v0.2.0 has no runtime callers, kept deliberately (D-153 / RT-42)" | §8 #9 ~ #14 |
| **RT-43** | `settleOnRemoval` is rewritten as a whole, and the three exits' semantics are redefined | writing "no successor core found" as "turn into dirt" or "destroy the cultivator" | the cultivator and its contents vanish into thin air (data loss); or `setBlock(DIRT)` replaces the cultivator with dirt | (a) the three exits are pinned down as §2.7's table, **with no fourth branch**; (b) "do not touch the world" is written as a hard constraint in the comments; (c) whenever the chunk cannot be fetched, exit ③ is always taken (no guessing) | §8 #27 ~ #30 |
| **RT-44** | the core BE gains `Container` + 16 bindings + 54 slots, and the NBT size and the sync must be recomputed; the old-save compatible read | ① all 54 `ItemStack`s go wholesale into `getUpdateTag`; ② a missing `Items` in an old save is read as "full" or crashes; ③ the menu's slot count and `getContainerSize()` disagree | ① several KB more every time a chunk is sent to the client; ② the old save cannot be entered / errors; ③ the screen crashes the instant it opens (`IndexOutOfBoundsException`) | (a) `getUpdateTag` writes the three keys only (§2.6.1); (b) the old-save three-key compatibility table (§2.6.2); (c) `storageSlots` is pinned to 54 and the menu reads it only at creation; (d) the slot count and `getContainerSize()` share the constant `SLOT_COUNT = 54` | §8 #21 ~ #26 |
| **RT-45** | wireframe rendering for the first time in the project (0 precedents of `RenderType.lines`) | ① a `POSITION_COLOR_NORMAL` vertex missing an element ⇒ a `Missing elements in vertex` crash; ② the wrong stage ⇒ the lines are invisible; ③ a full rescan every 20 ticks × the online population | ① the client crashes; ② the lines are invisible or flicker; ③ server tick jitter | (a) use `LevelRenderer.renderLineBox` rather than hand-written vertices (§2.8.3); (b) the stage is `AFTER_PARTICLES`; (c) debounce 10 ticks + rescan 20 ticks; (d) the client is read-only and derives nothing | §8 #31 ~ #34 |
| **RT-46** | villager trades for the first time in the project (0 villager code anywhere) | ① the event signature / bus is wrong; ② the level positions are written wrong (put into 2 / 4); ③ the price is rerolled at the moment of the deal; ④ **max-level farmers in an old save cannot get the new items** | ① not one of the three trade items appears; ② the item appears at the wrong level and the player never sees it; ③ the same offer costs a different amount each time it is clicked; ④ players with an old save believe "it was updated but there is no content" | (a) the signature is checked (§2.4's table, `neoforge-21.1.213-sources.jar`); (b) the positions 1 / 3 / 5 are fixed by `VillagerData.java:13-15`'s 1~5; (c) the draw and the pricing both complete once inside `getOffer`; (d) ④ **no migration** (no retroactive-grant logic, no change to the `Offers` NBT), written into the player-visible note (§10 item 16) | §8 #15 ~ #17 |
| **RT-47** | animated textures for the first time in the project (0 mod `.png.mcmeta` in the repository) | ① the mcmeta and the PNG are not together / the JSON is wrong ⇒ the texture breaks into a missing texture; ② only 9 of the top's frames differ ⇒ it looks like stuttering; ③ the idle state forgot to switch to the static image ⇒ it animates while idle too | ① the block is purple-black; ② a visual blemish (not a bug); ③ a violation of D-161 | (a) the mcmeta sits in the same directory under the same name as the PNG; (b) both `mcmeta` files are copied in verbatim, not one character changed; (c) dual models + the `working` property (§2.5.5); (d) ② is registered as a known visual property (document 34 §8 item 2) and not fixed | §8 #18 ~ #20 |
| **RT-48** | cutting the planting has two paths, and blocking only one leaks | only `isPlantableSoil` is changed, and the 7 independent seeds can still grow a crop on farmland | the player can still plant 7 kinds of crop ⇒ entry control fails | one change point for each of the two paths (§2.2's ① and ②), and the acceptance is **two separate items**, not merged | §8 #6 ~ #8 |
| **RT-49** | tiering is a new concept, and the 22 tiers must agree item by item with document 35 §2.2 | 1 row wrong (for example writing `power_beet` as Common) | it affects three places at once: the trade sampling pool, the cultivator's admission and the Lucky Berry pool | (a) the 22 are defined in `CropProfile` only, and all three places read the same `tier()`; (b) a compile-time line of defence: after the field is added, the 10 construction sites fail to compile when an argument is missing (§2.1); (c) one more startup self-check line "Common 11 / Rare 7 / Epic 4" (following the existing shape of `Farmstorm.java:112-115`) | §8 #3 ~ #5, #35 |
| **RT-50** | the pool goes 19 → 11, and the assertion at `Farmstorm.java:119` goes red | the pool is changed without the assertion | every startup logs one `自检 幸运浆果池 … → FAIL`; the server **still starts normally** (`poolOk` affects only the log text, `:118-123`), and a real fault would be buried under this always-red noise | change the pool and the assertion **in the same commit**; keep the other half — "does not contain `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`" | §8 #36 |

**The nine items' common trait** (written for documents 37 / 38): every one of them mitigates with "**one definition + one comment + one independent acceptance item**", and not one of them relies on "remembering to be careful". RT-42 / RT-43 / RT-48 / RT-50's red lines all land on **specific line numbers**, so the acceptance can grep them line by line.

---

## 8. Acceptance self-check list (36 items)

Each gives: number / the assertion verbatim / where to observe / the decision it rests on. **"Where to observe" is actionable**: `V-xxx` means an observation point this document defines; no second numbering series is introduced.

| # | The assertion verbatim | Where to observe | Grounds |
|---|---|---|---|
| 1 | the server startup log shows `自检 注册对象 97 个：items 33 / blocks 27 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 2 → OK` | `run/logs/latest.log` (the format string at `Farmstorm.java:151-155`) | D-171 |
| 2 | across the repository `@Mixin` hits **0**, `*.mixins.json` files **0**; in `FarmstormNetwork.onRegisterPayloads` `.playToClient` appears exactly **2** times, the packet ids being `farmstorm:core_list` and `farmstorm:cultivator_link` | a source grep + the two `自检 网络包注册` lines in the startup log | D-172, D-169 |
| 3 | the 22 crop profiles' `tier()` match document 35 §2.2's table item by item: Common 11 (`puffed_wheat` … `vibration_bean`) / Rare 7 / Epic 4 | `CropProfile.java:47-128`; `/data` is not readable, so use the startup self-check line | D-149 |
| 4 | the startup self-check gains one line `自检 作物品质 普通 11 / 稀有 7 / 史诗 4 → OK` | `latest.log` | D-149, RT-49 |
| 5 | the two sets derived from the tiering: inside the admission surface "produces 2 + 2" **5** and "produces 2 only" **13** (`5 + 13 = 18`) | the self-check line + document 35 §6.4's named table | D-163 |
| 6 | hold one of the 22 crops and right-click **farmland**: no block appears, the held item does not decrease, no message | manual (or `V-01`) | D-152 ① |
| 7 | hold one of the 7 seeds and right-click **farmland**: as above (no block appears, the seed does not decrease) | manual (`V-02`) | D-152 ② |
| 8 | hold one of the 7 deployed crop items (`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` / `zangief_cane` / `point_defense_fungus`) and right-click the top face of a full **non-farmland** block: the block is placed normally (proving that only the planting path was cut) | manual (`V-03`) | D-152, D-155 |
| 9 | in `FarmstormGameEvents.java` the **registration lines** of `onRightClickBlock` / `onFarmlandTrample` (`:75`, `:76`) and the two method bodies (`:241-265`, `:277-281`) are all in a commented state; and the single line `FarmlandLedger.get(server).backfill(server)` at `:171` is commented out | a source grep: `^\\s*//.*onRightClickBlock` and so on, 5 hits | D-153 |
| 10 | `onServerTickPost` **is still there** (not commented out), `CoreChunkTickets.restoreAll(server)` (`:170`) and `CoreListBroadcaster.tick(server)` (`:175`) are both inside the body, and `CoreListBroadcaster.tick` is executed every tick | the source + the overlay's reconciliation log every 100 ticks still appearing | D-153, RT-42 |
| 11 | in `farmstorm-server.toml` the `outpost_core` section has **no** `farmlandBonus` key and does have `cultivatorCapacity = 16` and `storageSlots = 54` | `run/config/farmstorm-server.toml` | D-153, D-165, D-166 |
| 12 | the field declaration at `FarmstormConfig.java:227` **is still there** and the definition at `:909-912` **is commented out**, and the comment says "restore the definition before restoring the listeners, otherwise NPE" | the source | D-153, §2.3 |
| 13 | none of the 3 farmland keys **exists** in the language files: `farmstorm.configuration.outpost_core.farmlandBonus`, `message.farmstorm.farmland.no_core`, `message.farmstorm.farmland.quota_full` | `zh_cn.json` / `en_us.json` (the key set after `ConvertFrom-Json`) | D-153 |
| 14 | both classes `FarmlandLedger` (555 lines) / `FarmlandGuard` (225 lines) are still in the source tree, their line counts unchanged, and they compile; `farmstorm_farmland.dat` **still exists and its mtime is unchanged** in the upgraded save | the source + the `world/data/` directory | D-153, D-154 |
| 15 | the farmer's trade list shows **3** of this mod's trade items, at the level positions **1 / 3 / 5**; each one's `maxUses = 1` | open the farmer's trade screen in game; `/data get entity <villager> Offers.Recipes` for `maxUses` | D-157, D-158 |
| 16 | after one deal that item **is still shown**, greyed out and unclickable (it does not disappear and its id does not change); the price and the crop id are **unchanged** after a restock | two consecutive observations in game | D-158 |
| 17 | the first offer's **base price** falls inside the range: Common ∈ [2,5], Rare ∈ [10,32], Epic ∈ [40,64] (`offer.getItemCostA().count()`) | the `count` of `/data get entity <villager> Offers.Recipes[0].buy` | D-159 |
| 18 | the cultivator's blockstate has two states: `working=false` uses `farmstorm:block/cultivator` (referencing the two `_still` images) and `working=true` uses `farmstorm:block/cultivator_working` (referencing the two frame strips) | `blockstates/cultivator.json` + the two model jsons + `F3`'s targeted block state | D-161 |
| 19 | among the resources there are exactly **2** `*.png.mcmeta` for `cultivator_a_*` (side / top), containing `frametime 2` / `interpolate false` / `frames 0..15`; the two `_still` images have **no** mcmeta | the file system | D-161, RT-47 |
| 20 | with an empty slot the block **does not move** (the static first frame); after a Common crop is put in it starts moving (one lap every 1.6 s); after 600 ticks it stops and clears the slot | in-game observation + `V-04` (the timestamp) | D-161, D-163 |
| 21 | put a Common crop into the cultivator: after 600 ticks the core's 54 slots hold **2** more of that crop; if the crop has a separate seed (one of the 5) then **2** more seeds as well | in game + `/data get block <core>`'s `Items` | D-163 |
| 22 | once the core's 54 slots are **full**: the cultivator stops at the completed state (`progress == cycleTicks`) and the items **do not drop and are not lost**; free 2 slots and the delivery completes automatically | in game + `/data get block <cultivator>`'s `Progress` | D-164 |
| 23 | hold an Epic crop (`lucky_berry` / `wild_apple` / the 2 `awakened_*`) or the 2 Epic seeds and right-click the cultivator: **no reaction**, the held item does not decrease | manual (`V-05`) | D-162 |
| 24 | an ally right-clicks the core empty-handed: the **54-slot** screen opens (6 rows × 9 columns + the player inventory), titled 「据点核心」; after storing 3 items, close it and open it again and the items are still there | in game (`V-06`) | D-166 |
| 25 | the core storage's **two gates**: ① a non-ally right-clicks the core → **no screen opens** (`useWithoutItem` returns `PASS`); ② an ally right-clicks from **9 blocks** away → the client cannot send the interaction and no screen opens (vanilla interaction distance); an ally opens the screen within **8 blocks** and then walks to 9 blocks → the screen is closed by vanilla's `stillValid` | two-player client (`V-07`) | D-166 supplement, ruling #17 |
| 26 | mine the core away: **all the items** in the 54 slots drop; a piston push / a same-block replacement / a chunk unload **drop nothing** | four in-game scenarios (`V-08`) | D-167 |
| 27 | when the core is broken there is another core in the world that "covers that cultivator + is on your own side + still has a free slot": the cultivator is rebound successfully and the item in the slot and the progress are **kept** | in game + `/data get block <cultivator>`'s `Core` | D-168 exit ① |
| 28 | when the core is broken there is no successor core: the cultivator **stays where it is**, the `Core` key disappears, `Held` / `Progress` are kept, and right-clicking does nothing | in game + `/data get block` | D-168 exit ② |
| 29 | when the core is broken the cultivator's chunk is **not in memory**: the cultivator's NBT **does not change by one byte** (`Core` is still the old core's coordinate) | read the region file offline after stopping the server and compare (`V-09`) | D-168 exit ③ |
| 30 | inside the `settleOnRemoval` method body the `setBlock` hit count = **0** (0.1.1's `:387` has disappeared with the rewrite) | a source grep (within the method body) | D-168, RT-43 |
| 31 | a player standing beside his own core (inside the 3×3 chunks): there is a **visible wireframe** between the core and each bound cultivator; after walking out of range the line **disappears** (the client list is emptied) | in game (`V-10`) | D-169 |
| 32 | break the cultivator: the corresponding line disappears at once (with no need to wait for the 20-tick rescan) | in game | D-169 moment #3 |
| 33 | the client does not crash while drawing lines: 10 minutes online continuously with 0 `Missing elements in vertex` / `IllegalStateException` in `Latest.log` | the client log | RT-45 |
| 34 | the server logic **does not depend** on the links: temporarily change `ClientCultivatorLinks`' handler to an empty implementation, and the binding / output / migration / core-breaking drops **all work as usual** | breakpoint-style verification (`V-11`) | D-169 supplement |
| 35 | the startup self-check's pool assertion is `poolSize == 11` with `→ OK` in the log; at the same time the other half — "does not contain `lucky_berry` / `wild_apple` / `compressed_puffed_wheat`" — is still there | `latest.log` | D-156, RT-50 |
| 36 | hold a seed and right-click the **converter**: no screen, no message; but the converter's **block is still there**, it is **obtainable in the creative tab**, and the **26 `converting` recipe jsons are still there** (`data/farmstorm/recipe/` count = 29) | in game + a resource-directory count | D-170 |

**The 36 items' distribution**: registry and red lines 2 / tiering 3 / planting 3 / farmland 6 / trades 3 / cultivator 5 / core 5 / migration 4 / links 4 / wrap-up 1 = 36.

---

## 9. Register of APIs to verify (13 items)

**The criterion**: an API counts as "checked" only when "the type / the method signature / the number of arguments at the call site — all three are seen verbatim in source or decompiled source readable on this machine". This section lists the 13 items that have **no direct precedent even in the source readable on this machine**, or **only a signature and no usage precedent**.

| # | The API or semantics | Why it is uncertain | Which task confirms it | The way out |
|---|---|---|---|---|
| ① | `VillagerTradesEvent`'s signature and registration bus | **checked** (`net/neoforged/neoforge/event/village/VillagerTradesEvent.java` inside `neoforge-21.1.213-sources.jar`: `getTrades()` / `getType()` / `getRegistryAccess()`, `NeoForge#EVENT_BUS`, keys 1 ~ 5 always non-null). **What is still uncertain**: this mod hangs a "fires during a reload" listener on the game bus for the first time, and the event fires inside `TagsUpdatedEvent` ⇒ the ordering against a datapack reload, and whether the `List` inside the `Int2ObjectMap` is mutable — there is no runtime evidence on this machine | the implementation task (trade registration) | if `getTrades().get(1)`'s list is immutable ⇒ change to `event.getTrades().put(1, new ArrayList<>(old + new))` |
| ② | the usable form of drawing lines with `RenderType.lines()` / `VertexConsumer` | **signatures checked**: `RenderType.lines()` (`RenderType.java:1063-1065`), `LevelRenderer.renderLineBox(PoseStack, VertexConsumer, AABB, float×4)` (`:2252-2253`), and 1.21.1's vertex chain is `addVertex(...).setColor(...).setNormal(...)` (`VertexConsumer.java:16,18,26`). **What is still uncertain is the runtime behaviour**: whether a usable `bufferSource` can be obtained at the `AFTER_PARTICLES` stage, how a fixed 1.0 line width actually looks, and whether `VIEW_OFFSET_Z_LAYERING` conflicts with shader mods | the rendering task (the link client) | if `AFTER_PARTICLES` is unusable ⇒ fall back to `AFTER_TRIPWIRE_BLOCKS`; if the `bufferSource` has already been flushed ⇒ capture the matrix in `AFTER_ENTITIES` and redraw at the `RenderGuiEvent` stage (the existing trick of `WildHunterOutline.java:66-74`) |
| ③ | expressing `mcmeta` animation + a static-frame dual texture at the blockstate level | the repository has **0** mod `*.png.mcmeta` and **0** vertical frame strips, so this is a first for the whole project: ① whether putting an `mcmeta` beside a PNG of the same name in the same directory is enough; ② whether an animation times itself separately when the same texture is referenced by two models; ③ whether the frame where the `working` property switches models flashes one frame of the default image | the resource task (datagen + copying the textures in) | if the switch flashes ⇒ make both models' `textures` use static images and give the working state a single transitional image "containing only frame 0 of the 16"; if the animation only takes effect under `working=true` fails ⇒ change to two **independent blocks** (not done in this version; it needs to go back to document 34) |
| ④ | the NBT compatibility and `getUpdateTag` size after the core BE gains `Container` | there is a precedent for NBT compatibility (`ConverterBlockEntity.java:407-416` treats a missing key as empty), but **none** for "upgrading from a non-container to a container"; the trade-off that the client BE's contents are empty when `getUpdateTag` carries no `Items` also appears in this mod for the first time | the core container task | if some client place (a comparator / rendering) really needs the items ⇒ put only "the non-empty slot count and the first slot's item" into `getUpdateTag` (≤ 200 bytes), still not moving the 54 slots |
| ⑤ | the detection timing of the cultivator's "pause the output" | document 34 fixes only "pause when the core is full" and **does not fix** at which moment it is judged. **[this document's decision]** is "judge once on completion" (§2.5.2 step 4): judging every tick would scan the core's 54 slots × 16 cultivators = 864 times per tick; judging every 20 ticks would make "pause → resume" up to 1 second late | the cultivator task | if a measured "resume delay of 1 second" is unacceptable ⇒ add a "broadcast to the bound cultivators" callback in the core's `setItem` / `removeItem` (without moving the judgement timing, only waking it early) |
| ⑥ | how the `CropTier` name color is implemented (`Rarity` / the `tooltip` event / custom) | **decided**: override `Item#getName(ItemStack)` (`Item.java:299-300`) + `withStyle`. **What is still uncertain**: ① whether an anvil-renamed `CUSTOM_NAME` overrides our colour (`getHoverName` `:731-739` prefers `CUSTOM_NAME`) ⇒ a renamed item **loses** its tier colour; ② the rarity wrapping of the tooltip's first line (`ItemStack.java:755`) not overriding the child component's colour is only inferred from the source, with no runtime screenshot | the tier visual task | ① if "keep the tier colour after renaming" is required ⇒ it can only be done by wrapping once more in a tooltip event (`ItemTooltipEvent`), at the cost of one more event mount; ② if the rarity wrapping really does override ⇒ return `Component.empty().append(the coloured text)` from `getName` (one extra empty shell preserves the child style) |
| ⑦ | `ModConfigSpec` accepting a degenerate range with `min == max` (`storageSlots` 54–54) | there is no precedent of this kind on this machine (all 166 existing keys are `min < max`); `defineInRange`'s boundary validation only shows up at runtime when the spec is first built | the config task | change to `54..55` and clamp to 54 with `Math.min(54, the read value)` when creating the menu (the measure is unchanged; it only adds one clamp line) |
| ⑧ | `AbstractContainerMenu#quickMoveStack`'s boundaries with 54 + 36 slots | the precedent `ConverterMenu` has 3 + 36 slots (`:121-216`); the 54 + 36 partition is written for the first time | the core container task | write the two ranges the way vanilla `ChestMenu` does (0–53 / 54–89), and let `Slot#mayPlace` cover slot validity |
| ⑨ | the reachability of the cultivator's `useWithoutItem` **while holding a block** | the order in `ServerPlayerGameMode.java:363` → `:370` says "`useWithoutItem` is called back only when the item interaction is not consumed"; "right-clicking the cultivator while holding a block" goes through `BlockItem` placement first ⇒ the cultivator **does not take** that click (which is reasonable, but it must be written into the acceptance) | the cultivator task | none (this is vanilla's order and is not changed); write "wanting to put something in while holding a block" into the player-visible note (document 35 §6.7) |
| ⑩ | `Containers.dropContents(Level, BlockPos, NonNullList<ItemStack>)` working with the cultivator | all three of `Containers`' overloads are there (`Containers.java:13,17,27`), but "the BE is not a Container and only one slot drops" appears in this mod for the first time | the cultivator task | if the `NonNullList` overload drops in a poor position ⇒ switch to `Containers.dropItemStack(...)` (`:31`) and drop item by item |
| ⑪ | the effect of `MerchantOffer`'s `priceMultiplier = 0.0F` on vanilla's discount | this document has already chosen "lock the base price" (§2.4.1): the source is readable (`MerchantOffer.java:107-111`: `floor(i × demand × multiplier)` and `specialPriceDiff` are two independent addends), but there is **no runtime evidence** that 0.0F is not treated as an abnormal value somewhere in vanilla, nor evidence of the actual size of the reputation discount | the trade task | if 0.0F has a side effect ⇒ switch to vanilla's most common `0.05F` (`VillagerTrades.java:132` etc.), at the cost of the base price possibly going +1 after a deal (the range's upper bound is still held by `clamp(...,1,maxStack)`) |
| ⑫ | whether the `setBlockUpdated` trigger points of the cultivator and the core are enough for the overlay / links to refresh | the existing `ConverterBlockEntity` uses `sendBlockUpdated` for one thing only, "conversion finished" (`:261`), with no precedent such as "the binding changed" | the core + link task | if the client does not refresh ⇒ additionally call `CoreListBroadcaster.requestAll(server)` (an existing method) at the three binding-change moments |
| ⑬ | whether the self-check format string at `Farmstorm.java:151-155` still lines up after 97 | that line is 13 arguments hand-assembled from `{}` placeholders, and changing the values without changing the string is enough; but **adding one "tier self-check" line** changes the number of log lines, so an acceptance that takes them by line number goes out of step | the wrap-up task | the acceptance uses a **content grep** (`自检 注册对象 97 个`) rather than line numbers |

**Three records "already downgraded from to-verify to checked"** (written here so that documents 37 / 38 do not treat them as risks again): `VillagerTradesEvent`'s signature (the first half of ①), the signatures of `RenderType.lines` / `LevelRenderer.renderLineBox` (the first half of ②), and `ContainerHelper`'s `Items` key plus `saveAllItems` / `loadAllItems` (the first half of ④) — all three were read verbatim in this machine's `neoforge-21.1.213-sources.jar` and `sourcesAndCompiledWithNeoForge_…_output.jar`, and the `file:line` has already been given in the body.

---

## 10. Register of differences from upstream (16 items; **upstream is not changed on its own**)

Ordered by the four classes "document 34's clauses / `BRIEF`'s clauses / documents 30 and 35's clauses / missing items". **This document only registers; it changes no upstream file.**

| # | Involved | What upstream says | This document's handling | Nature |
|---|---|---|---|---|
| 1 | document 30 §1.4's "59 groups" vs this document's measurement | document 30 §1.4 writes "currently 166 keys / **59 groups** (35 top-level groups + 24 nested groups)" | **a reading clarification, not a deviation**: document 30's "current" always means the **pre-change baseline** (0.1.0, when the `farmland` group still existed ⇒ 59 groups); §4.1 of the same file (`:968`) states outright "this table is the **post-change** 58 groups: the `farmland` group disappears entirely (−1 group)", and the total rows at `:1032` and `:1063` are both 58. This document measures 58 groups (34 + 24), consistent with document 30's **post-change** value and with `BRIEF` §0's 58 | the measure agrees (document 30 frozen, not changed) |
| 2 | document 34 §9 ⑤ vs `BRIEF` §3.5 | document 34 says `BRIEF` §3.5 recorded only "3 PNGs + 2 mctetas" | the current `BRIEF` §3.5 (`:217`) **already** reads "5 new PNGs + 2 mctetas" and gives the reason for the dual textures ⇒ that item of document 34 has been absorbed by an upstream rewrite, and this document writes 5 + 2 | document 34's report is out of date |
| 3 | document 34 §9 ⑥ vs `BRIEF` §3.3 line 13 | document 34 says `BRIEF` writes "the `cultivator` block + BE + item + **menu**", conflicting with 97 | the current `BRIEF` §3.3 line 13 **already** reads "(**no menu**: right-click puts the item straight in, no screen opens)" ⇒ the conflict no longer exists, but "the cultivator has no menu" is still a premise of the 97, so this document keeps it | document 34's report is out of date |
| 4 | document 34 §9 ⑨ vs `BRIEF` §3.6 | document 34 says `BRIEF` puts the location of the core BE's NBT in the index `.dat` | the current `BRIEF` §3.6 has **split** "the index `.dat` is unchanged" and "the core BE's NBT is in the chunk save and changes heavily" into two items ⇒ already corrected; this document's §6 still writes them as separate items | document 34's report is out of date |
| 5 | document 34 §9 ⑧ vs `BRIEF` §2 ruling #15 | document 34 says ruling #15 covers only the two ends "has a separate seed / has no seed", and the 8 in between are not written | the current `BRIEF` §2 ruling #15 already contains "the 22 crops are really three classes (7 / 8 / 7); inside the admission surface 5 give 2+2 and 13 give 2 only" ⇒ the measure agrees | document 34's report is out of date |
| 6 | document 34 §2 note 3 vs `BRIEF` §2 ruling #3 | document 34 says "ruling #3's '3 listeners' is the fact card's wording, the reality being 2 + 1" | the current `BRIEF` §2 ruling #3 states outright "comment out **2 listeners** and **1 backfill line**; `onServerTickPost` must be kept" ⇒ both documents point at 2 + 1, and document 34's wording makes one think the fact card still says 3 | a distortion of expression (the conclusion is unaffected) |
| 7 | **D-153's internal contradiction (substantive)** | D-153 requires commenting out the **reads** of `outpost_core.farmlandBonus` while also requiring `FarmlandGuard` / `FarmlandLedger` to "change not one line of their bodies" | contradiction: the two reads sit inside those two classes (`FarmlandGuard.java:75`, `FarmlandLedger.java:305`). **Adjudicated by the author as A-62** (see the end of this section): comment out the **definition** only (`FarmstormConfig.java:909-912`), **the field declaration at `:227` must be kept**, and the two classes' read sites are kept as they are (unreachable at runtime). Cost: the field is `null`, and an accidental restore of the listeners throws an NPE — already written into the comment's reason and into RT-42 | **already adjudicated (A-62)** |
| 8 | document 34 §9 ⑪ | the trade levels' **positions are undecided**, handed to document 36 to check against the level table | this document has checked: `VillagerData`'s levels are **1~5** (`VillagerData.java:13-15`, with the experience thresholds `{0,10,70,150,250}`) ⇒ the positions are fixed at **1 / 3 / 5** (§2.4) | the work document 34 left behind; done here |
| 9 | D-151's parenthetical | "the three colours **borrow vanilla's rarity** white / blue / gold" | vanilla's `Rarity` is white / **yellow** / **aqua** / **light purple** (`Rarity.java:16-19`), with **no gold**. This document takes white / blue / gold as `ChatFormatting.WHITE / BLUE / GOLD`; that distortion only strengthens the reason "do not use `Rarity`" | a distortion of expression (the conclusion is unaffected) |
| 10 | `BRIEF` does not cover it (missing) | §3.3 line 19 says only "the new item model goes through datagen" | `ModItemModelProvider.usesBlockModel` (`:57-59`) recognises only `ConverterBlock \|\| OutpostCoreBlock`; without adding `CultivatorBlock` it falls into the `basicItem` branch and demands a non-existent `item/cultivator.png`. This document writes it as a must-change item (§5) | **missing; would crash straight into the wall** |
| 11 | `BRIEF` does not cover it (missing) | D-171 says only "the assertions at `Farmstorm.java:147-150` go 93 → 97, + on every item" | no per-item old values were given. This document adds the per-item old → new table (`total 93→97`, `items 32→33`, `blocks 26→27`, `blockEntities 7→8`, `menuTypes 1→2`, the other 8 classes unchanged) (§1.2) | missing (copying it would omit items) |
| 12 | **[withdrawn] this document's first draft's "three stale numbers in document 30"** | the first draft registered three items here: ① document 30 §1.4's "59 groups" overcounts by 1; ② document 30 §1.4's "315 language keys" is stale; ③ document 30's "`farmstorm.configuration` 166 is 1:1 with the server config" is stale | **all three are withdrawn**; after a line-by-line re-check document 30 is self-consistent: ① see row 1, "59" is the 0.1.0 baseline while §4.1 of the same file's 58 is post-change; ② `:117`'s "315 × 2" is likewise a pre-change value, the arithmetic `315 − 2 + 10 = 323` at `:1092` already lands on **323**, and `:1318` even explains "the arithmetic missed 2 key-description keys; with them added = 323"; ③ `:1116` states outright "deleting `farmland.limitPerPlayer` takes `farmstorm.configuration` from 166 → 165, and adding `bulletSpeed` and the 3 `hud.*` keys brings it back to **169** — 169 = 166 + 3", and the acceptance item at `:1254` also asserts 169. Conclusion: **document 30 is not stale; this document's first draft misread "the pre-change baseline" as "the current value"** | **a misreading, withdrawn** (document 30 frozen, not changed; this document's §1.3 / §4.1 measurements agree with document 30 §4.1, §4.3) |
| 13 | document 35 §8.3 vs document 34's D-166 vs **ruling #17** | document 35 states "the storage access criterion has **one** gate (one's own side) with no range gate", and §12 B-24 left the interface "if the author wants a range gate" | **ruling #17**, newly added in `BRIEF` §2, brings the range gate back (one's own side + within the player's reach) and requires "no custom constant; use vanilla `stillValid` semantics directly" ⇒ that ⚠️ paragraph in document 35 §8.3 and B-24 **are out of date**. This document writes two gates (§2.6.4), the reach being vanilla `Container.stillValidBlockEntity`'s `4.0F` (`Container.java:81-82`), **not** the 3×3 chunks | **upstream has changed; document 35 needs to be revised back** |
| 14 | the "section count" wording of document 34 §3.4 | document 34 writes "section count `58 + 2 = 60`" | this document treats "group = the number of `b.push` calls" and "section = a toml `[section]` line" as the same number (58 → 60), and states in §4.1.2 that "`outpost_core` gains keys, it is not deleted and rebuilt" | terminology unified (the measure is unchanged) |
| 15 | `BRIEF` §2's new rulings vs document 34 (missing) | `BRIEF` §2 now has **19** rulings (#17 the core storage's two gates / #18 the trade granularity staying at 3 items / #19 the converter's Chinese name unchanged), while document 34 still stops at 16 and the D numbers stop at D-172 | this document is written against 19: §2.6.4 (#17), §2.4.2 (#18), §2.9 (#19). **The D numbers are to be back-filled by document 37 after A-62** — this document opens no new D number (document 34 §0.1's numbering discipline) | **upstream has changed; document 34 needs to be revised back** |
| 16 | document 35 §4.6 / `BRIEF` does not cover the old-save migration (missing) | "max-level farmers in an old save do not get the trade items retroactively" was registered by document 35 only as a player-visible consequence; nobody said clearly **whether this version migrates** | this document makes it explicit: **no migration** (no retroactive-grant logic, no change to the villager `Offers` NBT, no data repair), the same discipline as D-158's "no second restock logic"; already written into §7 RT-46 and §2.4.2's ⚠️ | **missing; documents 35 / 38 need to land one line in the player note** |

**Two hints for document 37**:

1. item 7 **has already been adjudicated by the author as A-62** (the body is at the end of this section); document 37 **only needs to register it, no further ruling is needed**; the `175` keys and the `97` registry objects are both unaffected — A-62 answered exactly one question, "does 1 line of `FarmlandGuard.java:75` / `FarmlandLedger.java:305` need to change?", and the answer is **no change**.
2. what item 15 needs filled in is the **number** and what item 16 needs filled in is the **player note**: rulings #17 ~ #19 need a D number (this document does not open one itself, per discipline), and "max-level farmers in an old save do not get them retroactively" needs one sentence in the upgrade note, "this version does not migrate; please use new villagers". The former belongs to document 37 (the ruling register after A-62), the latter to document 35's revision / document 38's HC section.

---

### A-62 · Adjudication: `outpost_core.farmlandBonus` — comment out the definition only, keep the field declaration

| Item | Content |
|---|---|
| Number | **A-62** (adjudicated on the spot during writing; continuing A-61 of 0.1.1; this document opens no new D number, per document 34 §0.1's "no numbers issued outward" discipline) |
| Conclusion | comment out the **definition** of `outpost_core.farmlandBonus` in `config/FarmstormConfig.java` (`:909-912`), and **keep the field declaration** (`:227`). |
| Reason | D-153 requires both "comment out that key's reads" and "change not one line of `FarmlandGuard` / `FarmlandLedger`'s bodies", while the two reads sit inside those two classes (`FarmlandGuard.java:75`, `FarmlandLedger.java:305`) — the two cannot both be had; keeping the declaration is the only way to keep the two classes compiling. |
| Cost | the field is permanently `null`; if someone restores those two listeners by mistake (`FarmstormGameEvents.java:75` / `:76`) a **`NullPointerException`** is thrown. |
| Mitigation | RT-42's three lines of defence: both comments at `:227` and `:909` state "restore this definition before restoring the listeners, otherwise `FarmlandGuard:75` throws an NPE"; one line at the top of each of `FarmlandLedger` / `FarmlandGuard`'s class comment reads "v0.2.0 has no runtime callers, kept deliberately (D-153 / RT-42)". |
| Rejected | ① **changing the two classes as well** (violates D-153's "change not one line of the bodies"); ② **turning the field into the hard-coded constant 16** (that is secretly keeping the farmland quota, in direct conflict with "retiring the farmland system"); ③ **deleting the two classes** (document 34 §7 tombstone 1: delete them and there is no way back). |
| Blast radius | **zero**: the `175` config keys and the `97` registry objects are both unaffected; all that is affected is "does 1 line of those two `.get()` calls inside the two classes need to change", and A-62's answer is "no change". |
| Relation to §2.3 of this document | row 6 of §2.3's comment-out list and "the three places that must be kept" are written against A-62; the two documents' measures agree, and changing one means changing the other at the same time. |

---

## Appendix A — the 13 parameters fixed by this document (quick reference; a reason for each in the body)

| # | Parameter | Value | Body |
|---|---|---|---|
| 1 | `CropTier` values / `translationKey()` | `COMMON`/`RARE`/`EPIC`; `tooltip.farmstorm.tier.<lower-case name>` | §2.1 |
| 2 | the tier name color | `ChatFormatting.WHITE` / `BLUE` / `GOLD` | §2.1 |
| 3 | the tooltip tier line | `tooltip.farmstorm.tier`, `%s` = the tier name, grey shell | §2.1 |
| 4 | the cultivator's state property | `working` (`BooleanProperty`, default false) | §2.5.1 |
| 5 | the cultivator BE's NBT | `Held` / `Progress` / `Core` (missing `Core` = unbound) | §2.5.2 |
| 6 | the cultivator's tick carrier | block `getTicker` → BE static `serverTick` (server only) | §2.5.2 |
| 7 | the core container's NBT | `Items` (`ContainerHelper.TAG_ITEMS`) / `Cultivators` (a Long list) | §2.6.1 |
| 8 | the link payload / broadcast constants | `farmstorm:cultivator_link`; `MAX_LINKS = 1024`; debounce 10 ticks / rescan 20 ticks | §2.8.1, §2.8.2 |
| 9 | the link rendering | `RenderType.lines()`; `AFTER_PARTICLES`; RGB 0.30/0.85/0.90, alpha 1.0; line width 1.0; 0.25-block end boxes | §2.8.3 |
| 10 | the trade level positions | **1 / 3 / 5** | §2.4 |
| 11 | the trades' `xp` / `priceMultiplier` | `2` / `0.0F` | §2.4 |
| 12 | the core screen's title key and screen texture | `container.farmstorm.outpost_core`; reuse vanilla `generic_54.png` | §2.6.3 |
| 13 | the static first-frame texture names | `cultivator_a_side_still.png` / `cultivator_a_top_still.png` | §2.5.5 |
