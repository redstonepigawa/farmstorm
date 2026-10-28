# 02 · Technical Specification

> Subject: Farmstorm v0.0.1 (NeoForge 21.1.x / Minecraft 1.21.1 / Java 21)
> Single source of truth: `docs/zh/00-DECISIONS.md` (hereafter DEC). Where this document conflicts with DEC, DEC prevails.
> This document describes **architecture and contracts** only; it does not deliver a complete implementation. Skeleton fragments and signatures are permitted.
> Companion note: `01-DESIGN.md` §3.8 (the full configuration key list) is already written to disk; the key names / default values / value ranges in §4.11 of this document are aligned with it item by item (117 items in total). Where the two documents conflict, DEC arbitrates; for configuration key names, §3.8 prevails.

**Chinese → English name mapping (translation aid; the body of this document uses the English names only)**

| Chinese (original) | English |
|---|---|
| 膨胀麦 | Puffed Wheat |
| 膨胀麦种子 | Puffed Wheat Seeds |
| 胡罗贝 | Blast Carrot |
| 育碧服务器 | Ubi Server |
| 毒育碧服务器 / 毒育碧 | Toxic Ubi Server / Toxic Ubi |
| 甜菜！ | Power Beet |
| 甜菜种子（甜菜！） | Beet Seeds (Power Beet) |
| 瓜片子 | Melon Slice Bomb |
| 瓜！ | Melon Grenade |
| 瓜！种子 | Melon Grenade Seeds |
| man瓜 | Pumpkin Turret |
| man瓜种子 | Pumpkin Turret Seeds |
| 激活的火把花 | Awakened Torchflower |
| 激活火把花种子 | Awakened Torchflower Seeds |
| 激活的瓶子草 | Awakened Pitcher Plant |
| 激活瓶子草豆荚 | Awakened Pitcher Plant Pod |
| 火把花 | Torchflower |
| 瓶子草 | Pitcher Plant |
| 赢过输过没死过 | Won, Lost, Never Died |
| 爆竹 | Bamboo Firecracker |
| ？！菇菇！？ | Red Mushroom Puff |
| ？！棕色菇菇色棕！？ | Brown Mushroom Puff |
| 红菇 / 棕菇 | Red Mushroom / Brown Mushroom |
| 幸运浆果 | Lucky Berry |
| 农产品转换器 | Crop Converter |
| 作物 | crop |
| 播种物 | seeding item |
| 投掷型 / 部署型 / 食用型 | throwable / deployable / edible |
| 己方 | own side |
| 炮台 / 陷阱 / 光环 | turret / trap / aura |
| 硬挖 | forced mining |
| 临时附魔 | temporary enchantment |
| 部署型作物 | deployable crop |

---

## 0. Triage and Scope of This Document

| Item | Conclusion |
|---|---|
| Complexity tier | L2 (cross-module, multiple deliverables, implicit assumptions present) |
| In scope for this document | Build baseline, directory structure, registration summary, class contracts, server-authoritative model, resource inventory, performance and security, risks |
| Out of scope for this document | Design statements of gameplay values (see 01-DESIGN.md), task breakdown (see 03-TASKS.md), texture pixel drafts (see tools/gen_textures.py) |

### 0.1 Technical decisions introduced by this document (not covered by DEC)

DEC has already converged everything at the gameplay level, but a number of gaps remain at the engineering level; this document fills them with `T-xx` numbering. **These decisions do not change gameplay, they only fix the implementation shape.** If the author rejects one, the change surface is confined to a single point.

| ID | Technical decision | Rationale |
|---|---|---|
| T-01 | The "planted form" is implemented as 14 independent `FarmstormCropBlock` blocks (`<id>_crop`), with no corresponding BlockItem registered; sowing is performed directly by the item `useOn` via `level.setBlock` | DEC D-08 requires "on harvest, drop both the crop and the seeding item at vanilla ratios, a closed self-sustaining loop", which requires a harvestable custom crop block; a single block plus a BlockState enum would cram 15 vanilla planting rules (farmland / sand / mycelium, light level, growth conditions) into one class, which is not worth the cost |
| T-02 | For deployable crops (`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus`) the item is the **BlockItem of the deployed block**; the same item, when used on farmland, instead places its own `_crop` block | DEC §4 stipulates that deployable block IDs share the item IDs; one ItemStack can only be one BlockItem, so "deploy" and "sow" must be split inside `useOn` |
| T-03 | For torchflower / pitcher plant, "cannot be destroyed by non-own-side players" is implemented by having `Block#getDestroyProgress` return `0.0F` plus a second check in `playerWillDestroy`; it applies to creative-mode players as well | The client must also compute 0 progress, otherwise the player feels "mining for ages and then it suddenly snaps back"; the server still needs the second check to stop packet forgery |
| T-04 | DEC D-13 "turret-type blocks are fixed at 10 seconds of forced mining" applies only to `pumpkin_turret`; torchflower / pitcher plant follow D-14 "own side may mine normally" | D-14 is more specific than D-13 and names these two explicitly; applying both would make the own side mine for 10 seconds too, contradicting D-14 "mine normally". Arbitration order: a specific clause outranks a generalized clause. **Endorsed by DEC C-8** |
| T-05 | Puffed Wheat block replacement skips blocks with "blast resistance ≥ 3600000" (bedrock / barrier / command block / end portal frame, etc.) | `plan0.0.1.txt` only says "3×3×3 including air blocks"; a literal implementation would delete bedrock and barriers and destroy server protection structures. This is a destruction-prevention floor, not a balance adjustment (D-21 only locks gameplay values) |
| T-06 | Two custom damage types: `farmstorm:void_bolt` (turret missile) and `farmstorm:crop_blast` (crop explosion) | The "void damage" in `plan0.0.1.txt` cannot be expressed with vanilla `explosion` / `generic`; only an independent type lets the explosion produce the death message "blown up by <X> crop" and attribute the kill correctly |
| T-07 | No Mixin is introduced | Every requirement can be met through inheritance + the NeoForge event bus + native extension points; no Mixin means readable crash logs, the smallest conflict surface with other mods, and no need for `farmstorm.mixins.json` |
| T-08 | Temporary enchantments are recorded in a **DataComponent** `farmstorm:temporary_enchantments` (enchantment / granted level / original level / expiry tick) rather than in persistent player data | DEC D-15 requires "removed automatically on leaving the range or on expiry, and existing enchantments are not overwritten"; recording it on the item lets it travel with the item, drop, and enter chests, which stops players from stuffing the sword into a chest to bypass reclamation |

---

## 1. Build Baseline

### 1.1 gradle.properties (key entries)

```properties
# --- Platform ---
minecraft_version=1.21.1
neo_version=21.1.213
neo_version_range=[21.1.0,)
loader_version_range=[4,)

# --- Mod ---
mod_id=farmstorm
mod_name=Farmstorm
mod_license=All Rights Reserved
mod_version=0.0.1
mod_group_id=com.dashpig.farmstorm
mod_authors=dashpig
mod_description=Vanilla crops turned into PvP gadgets.

# --- Build ---
org.gradle.jvmargs=-Xmx3G
org.gradle.parallel=true
org.gradle.caching=true
```

Value discipline for `neo_version`: what is written here is the **latest 21.1.x artifact that could be verified**, and the official build must use the current highest stable 21.1.x version listed in `https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml` (rationale and sources in §10, version verification). **Do not** write dynamic versions such as `21.1.+` in `gradle.properties` — the build would not be reproducible.

### 1.2 build.gradle skeleton

```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.148'   // see §10: verifiable artifact on the 2.0.x line
}

version = mod_version + "+mc" + minecraft_version
group = mod_group_id

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)   // DEC: Java 21
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
}

neoForge {
    // target NeoForge version; download and mappings are handled by the plugin
    version = neo_version

    // MDG uses official (Mojang) mappings by default; DEC requires official, so parchment is not specified here
    // parchment { }  // add when parameter names are needed

    runs {
        client { client() }
        server { server() }
        data   { data() }   // DataGen: GatherDataEvent
    }

    mods {
        farmstorm {
            sourceSet sourceSets.main
        }
    }
}
```

**DSL name caveat**: the task document writes `neoforge { }`, but the actual ModDevGradle extension block is `neoForge { }` (case sensitive). If the build reports `Could not find method neoForge()` / `neoforge()`, follow the DSL hints shipped with the plugin and write the result back into this document — this falls under "subject to what is actually pulled at build time".

### 1.3 neoforge.mods.toml key points

```toml
modLoader = "javafml"
loaderVersion = "[4,)"
license = "All Rights Reserved"

[[mods]]
modId = "farmstorm"
version = "${file.jarVersion}"
displayName = "Farmstorm"
authors = "dashpig"
description = '''Vanilla crops, upgraded into PvP gadgets.'''

[[dependencies.farmstorm]]
modId = "neoforge"
type = "required"
versionRange = "[21.1.0,)"
ordering = "NONE"
side = "BOTH"

[[dependencies.farmstorm]]
modId = "minecraft"
type = "required"
versionRange = "[1.21.1]"
ordering = "NONE"
side = "BOTH"
```

### 1.4 Build artifacts and acceptance gates

| Command | Expectation |
|---|---|
| `./gradlew build` | Produces `build/libs/farmstorm-0.0.1+mc1.21.1.jar`, with no deprecation above warning level |
| `./gradlew runData` | Generates models / blockstates / recipes / loot tables / language files under `src/generated/resources` |
| `./gradlew runServer` | The dedicated server starts standalone (DEC D-23: the client installs only the resource pack) |
| `./gradlew runClient` | 22 items appear in the creative tab, the converter GUI opens |

---

## 2. Project Directory Structure

```
src/main/java/com/dashpig/farmstorm/
├── Farmstorm.java                      main class: MOD_ID, mod bus event registration, registry summary entry point
├── registry/                           registration layer: DeferredRegister and Supplier only, no logic
│   ├── FarmstormItems.java             22 Item (15 crops + 6 seeding items + 1 converter)
│   ├── FarmstormBlocks.java            20 Block (14 crops + 5 deployed + 1 converter)
│   ├── FarmstormBlockEntities.java     6 BlockEntityType
│   ├── FarmstormEntityTypes.java       9 EntityType (8 projectiles + 1 turret missile)
│   ├── FarmstormMenus.java             1 MenuType
│   ├── FarmstormRecipes.java           1 RecipeType + 1 RecipeSerializer
│   ├── FarmstormDataComponents.java    1 DataComponentType
│   ├── FarmstormCreativeTabs.java      1 CreativeModeTab
│   └── FarmstormDamageTypes.java       ResourceKey for 2 DamageType + accessor helpers
├── config/
│   └── FarmstormConfig.java            server-side ModConfigSpec (all values + baked cache)
├── item/                               item behavior
│   ├── ModifiedCropItem.java           base class: shared tooltip, CropProfile holder, sow split
│   ├── ThrowableCropItem.java          throwable: use() spawns the matching projectile entity
│   ├── DeployableCropItem.java         deployable: BlockItem subclass, useOn splits "deploy / sow"
│   ├── SeedItem.java                   standalone seeding item: ItemNameBlockItem subclass
│   └── FoodCropItem.java               edible: FoodProperties + finished-eating callback
├── block/
│   ├── FarmstormCropBlock.java         the single implementation of the 14 crop blocks (CropBlock subclass)
│   ├── DeployedCropBlock.java          the single implementation of the 5 deployed blocks (attachment / hovering / forced mining)
│   └── ConverterBlock.java             crop converter block (with the MenuProvider contract)
├── blockentity/
│   ├── DeployedCropBlockEntity.java    deployed-object state machine + behavior dispatch + sync
│   └── ConverterBlockEntity.java       two-slot container (input + catalyst) + conversion transaction
├── behavior/                           deployed behavior (strategy pattern)
│   ├── DeployedBehavior.java           top-level behavior interface: lifecycle + tick
│   ├── TrapBehavior.java               trap: trigger condition
│   ├── TurretBehavior.java             turret: target acquisition + firing
│   ├── AuraProvider.java               aura: radius / frequency / application (DEC C-4)
│   └── impl/
│       ├── BlastCarrotTrap.java        Blast Carrot: Chebyshev ≤1 self-detonation
│       ├── FangTrapCactusTrap.java     Fang Trap Cactus: Euclidean ≤3, two rings of fangs
│       ├── PumpkinTurret.java          Pumpkin Turret: Chebyshev ≤5 missile
│       ├── TorchflowerAura.java        Torchflower: radius 20 buff + temporary Fire Aspect
│       └── PitcherPlantAura.java       Pitcher Plant: radius 10 buff + purge of negative effects
├── entity/
│   ├── ThrownCropEntity.java           projectile base class (ThrowableItemProjectile subclass)
│   ├── TurretBullet.java               turret missile (custom, not the vanilla shulker bullet)
│   └── thrown/
│       ├── PuffedWheatProjectile.java  Puffed Wheat: hay bale fill
│       ├── UbiServerProjectile.java    Ubi Server: power 4 explosion
│       ├── ToxicUbiServerProjectile.java  Toxic Ubi: explosion + potion cloud
│       ├── MelonSliceProjectile.java   Melon Slice Bomb: power 2 and damage capped at 6
│       ├── MelonGrenadeProjectile.java  Melon Grenade: 4 melon slices + power 3
│       ├── BambooBamboo FirecrackerProjectile.java  Bamboo Firecracker: 140 tick wind-charge rain
│       ├── RedMushroomPuffProjectile.java  Red Mushroom: nausea / blindness
│       └── BrownMushroomPuffProjectile.java  Brown Mushroom: weakness / slowness
├── explosion/
│   ├── FarmstormExplosion.java         custom explosion: three phases (destruction / damage / presentation)
│   └── BlastRule.java                  destructibility predicate + resistance threshold + drop policy
├── team/
│   ├── TeamResolver.java               own-side determination abstraction (DEC D-06)
│   └── ScoreboardTeamResolver.java     v0.0.1 implementation: vanilla scoreboard teams
├── recipe/
│   ├── ConverterRecipe.java            Recipe<SingleRecipeInput>
│   └── ConverterRecipeSerializer.java  StreamCodec + MapCodec
├── menu/
│   └── ConverterMenu.java              1 input + 1 read-only output + convert button
├── client/                             client only: models / screens / rendering
│   ├── FarmstormClient.java            @EventBusSubscriber(Client): menu screen registration
│   ├── ConverterScreen.java            converter GUI
│   └── render/TurretBulletRenderer.java  reuses the shulker bullet model
├── util/
│   ├── TempEnchantHandler.java         grant / revoke of Fire Aspect (DEC D-15)
│   ├── LuckyBerryRoller.java           Lucky Berry 8-tier probability table (DEC D-17/D-18)
│   ├── DistanceMetrics.java            Chebyshev / Euclidean helpers (DEC D-20)
│   ├── DeployedOwnership.java          ownership read/write (UUID + name; team resolved live)
│   └── CropProfile.java                static profile of a single crop (ID / seeding item / behavior / deployed face)
├── event/
│   ├── FarmstormModEvents.java         mod bus: GatherData, BuildCreativeTabContents
│   └── FarmstormGameEvents.java        game bus: PlayerTick, PlayerLoggedIn/Out, explosion interception
└── datagen/                            DataGen providers (loaded only under runData)
    ├── FarmstormDataGen.java           GatherDataEvent aggregation
    ├── ModBlockStateProvider.java      20 blockstates + 62 block models
    ├── ModItemModelProvider.java       22 item models
    ├── ModRecipeProvider.java          20 bidirectional conversion recipes
    ├── ModLootTableProvider.java       20 loot tables (including vanilla-ratio drops)
    ├── ModBlockTagsProvider.java       deployed_crops / explosion_immune
    ├── ModItemTagsProvider.java        modified_crops / seeds
    ├── ModDamageTypeTagsProvider.java  merges the 2 damage types into the minecraft tags
    ├── ModLangProvider.java            zh_cn / en_us
    └── ModAdvancementProvider.java     12 advancements
```

**Package split principle**: `registry` only registers, `item/block/entity` only implement behavior, `util` does not register.

---

## 3. Registration Summary

Counting basis: **64 registry objects** (across 10 registries), plus 1 `ModConfigSpec` (not a registry; configuration file `config/farmstorm-server.toml`). Every entry is given with its full ID.

### 3.1 items — 22

Namespace is `farmstorm` throughout. Stack sizes follow DEC D-16 (throwable 16, everything else 64).

**A. 15 crop items (verbatim from DEC §4)**

| # | ID | Name | Class | Stack | Seeding item |
|---|---|---|---|---|---|
| 1 | `farmstorm:puffed_wheat` | Puffed Wheat | `ThrowableCropItem` | 16 | `puffed_wheat_seeds` |
| 2 | `farmstorm:blast_carrot` | Blast Carrot | `DeployableCropItem` (= BlockItem of the deployed block) | 64 | same name (itself) |
| 3 | `farmstorm:ubi_server` | Ubi Server | `ThrowableCropItem` | 16 | same name (itself) |
| 4 | `farmstorm:toxic_ubi_server` | Toxic Ubi Server | `ThrowableCropItem` | 16 | same name (itself; D-32: independent crop block + the item itself is the seeding item) |
| 5 | `farmstorm:power_beet` | Power Beet | `FoodCropItem` | 64 | `power_beet_seeds` |
| 6 | `farmstorm:melon_slice_bomb` | Melon Slice Bomb | `ThrowableCropItem` | 16 | none (not plantable) |
| 7 | `farmstorm:melon_grenade` | Melon Grenade | `ThrowableCropItem` | 16 | `melon_grenade_seeds` |
| 8 | `farmstorm:pumpkin_turret` | Pumpkin Turret | `DeployableCropItem` (= BlockItem of the deployed block) | 64 | `pumpkin_turret_seeds` |
| 9 | `farmstorm:awakened_torchflower` | Awakened Torchflower | `DeployableCropItem` (= BlockItem of the deployed block) | 64 | `awakened_torchflower_seeds` |
| 10 | `farmstorm:awakened_pitcher_plant` | Awakened Pitcher Plant | `DeployableCropItem` (= BlockItem of the deployed block) | 64 | `awakened_pitcher_plant_pod` |
| 11 | `farmstorm:fang_trap_cactus` | Fang Trap Cactus | `DeployableCropItem` (= BlockItem of the deployed block) | 64 | same name (itself) |
| 12 | `farmstorm:bamboo_firecracker` | Bamboo Firecracker | `ThrowableCropItem` | 16 | same name (itself) |
| 13 | `farmstorm:red_mushroom_puff` | Red Mushroom Puff | `ThrowableCropItem` | 16 | same name (itself) |
| 14 | `farmstorm:brown_mushroom_puff` | Brown Mushroom Puff | `ThrowableCropItem` | 16 | same name (itself) |
| 15 | `farmstorm:lucky_berry` | Lucky Berry | `FoodCropItem` | 64 | same name (itself) |

**B. 6 standalone seeding items**

| ID | Name | Class | Stack | Resulting block |
|---|---|---|---|---|
| `farmstorm:puffed_wheat_seeds` | Puffed Wheat Seeds | `SeedItem` | 64 | `farmstorm:puffed_wheat_crop` |
| `farmstorm:power_beet_seeds` | Beet Seeds (Power Beet) | `SeedItem` | 64 | `farmstorm:power_beet_crop` |
| `farmstorm:melon_grenade_seeds` | Melon Grenade Seeds | `SeedItem` | 64 | `farmstorm:melon_grenade_stem` |
| `farmstorm:pumpkin_turret_seeds` | Pumpkin Turret Seeds | `SeedItem` | 64 | `farmstorm:pumpkin_turret_stem` |
| `farmstorm:awakened_torchflower_seeds` | Awakened Torchflower Seeds | `SeedItem` | 64 | `farmstorm:awakened_torchflower_crop` |
| `farmstorm:awakened_pitcher_plant_pod` | Awakened Pitcher Plant Pod | `SeedItem` | 64 | `farmstorm:awakened_pitcher_plant_crop` |

**C. 1 block item**

| ID | Name | Class | Stack |
|---|---|---|---|
| `farmstorm:converter` | Crop Converter | `BlockItem` | 64 |

**D. Material items**: v0.0.1 **registers no new material items**. Conversion costs use vanilla resources (experience, nether star); DEC D-09 already states that modified crops do not take part in vanilla recipes, so no intermediate products are needed.

> Cross-check: all 15 crop items appear in the table above (§11 acceptance item 1).

### 3.2 blocks — 20

| Group | ID | Class | Note |
|---|---|---|---|
| Crops (14) | `farmstorm:puffed_wheat_crop` | `FarmstormCropBlock` | no BlockItem (T-01) |
| | `farmstorm:blast_carrot_crop` | `FarmstormCropBlock` | |
| | `farmstorm:ubi_server_crop` | `FarmstormCropBlock` | |
| | `farmstorm:toxic_ubi_server_crop` | `FarmstormCropBlock` | |
| | `farmstorm:power_beet_crop` | `FarmstormCropBlock` | |
| | `farmstorm:melon_grenade_stem` | `FarmstormCropBlock` | stem block (vanilla reference: melon stem) |
| | `farmstorm:pumpkin_turret_stem` | `FarmstormCropBlock` | stem block (vanilla reference: pumpkin stem) |
| | `farmstorm:awakened_torchflower_crop` | `FarmstormCropBlock` | |
| | `farmstorm:awakened_pitcher_plant_crop` | `FarmstormCropBlock` | |
| | `farmstorm:fang_trap_cactus_block` | `FarmstormCropBlock` | sand / red sand |
| | `farmstorm:bamboo_firecracker_stalk` | `FarmstormCropBlock` | |
| | `farmstorm:red_mushroom_puff_block` | `FarmstormCropBlock` | low light level |
| | `farmstorm:brown_mushroom_puff_block` | `FarmstormCropBlock` | low light level |
| | `farmstorm:lucky_berry_bush` | `FarmstormCropBlock` | grass block (same as sweet berry bush) |
| Deployed (5) | `farmstorm:blast_carrot` | `DeployedCropBlock` | top face only |
| | `farmstorm:pumpkin_turret` | `DeployedCropBlock` | top / bottom / side, may hover |
| | `farmstorm:awakened_torchflower` | `DeployedCropBlock` | top face only, may hover, explosion immune |
| | `farmstorm:awakened_pitcher_plant` | `DeployedCropBlock` | top face only, may hover, explosion immune |
| | `farmstorm:fang_trap_cactus` | `DeployedCropBlock` | top face only |
| Functional (1) | `farmstorm:converter` | `ConverterBlock` | three-slot GUI (catalyst + input + preview, D-47), with facing |

`melon_slice_bomb` has no crop block (DEC §4: not plantable), hence 14 crop blocks rather than 15.

### 3.3 block_entities — 6

| ID | Class | Note |
|---|---|---|
| `farmstorm:blast_carrot` | `DeployedCropBlockEntity` | the same class registered 5 times, distinguished by `BlockEntityType` |
| `farmstorm:pumpkin_turret` | `DeployedCropBlockEntity` | |
| `farmstorm:awakened_torchflower` | `DeployedCropBlockEntity` | |
| `farmstorm:awakened_pitcher_plant` | `DeployedCropBlockEntity` | |
| `farmstorm:fang_trap_cactus` | `DeployedCropBlockEntity` | |
| `farmstorm:converter` | `ConverterBlockEntity` | two-slot container (input + catalyst) |

> Crop blocks (`FarmstormCropBlock`) **do not** use a BlockEntity: all growth state lives in the BlockState, which saves memory and sync bandwidth.

### 3.4 entity_types — 9

All are `MobCategory.MISC`, `sized(0.25F, 0.25F)` (0.3F for the missile), `clientTrackingRange(4)`, `updateInterval(10)`.

| ID | Class | Corresponding item |
|---|---|---|
| `farmstorm:puffed_wheat` | `PuffedWheatProjectile` | Puffed Wheat |
| `farmstorm:ubi_server` | `UbiServerProjectile` | Ubi Server |
| `farmstorm:toxic_ubi_server` | `ToxicUbiServerProjectile` | Toxic Ubi Server |
| `farmstorm:melon_slice_bomb` | `MelonSliceProjectile` | Melon Slice Bomb (also used as the bullet of Melon Grenade) |
| `farmstorm:melon_grenade` | `MelonGrenadeProjectile` | Melon Grenade |
| `farmstorm:bamboo_firecracker` | `BambooFirecrackerProjectile` | Bamboo Firecracker |
| `farmstorm:red_mushroom_puff` | `RedMushroomPuffProjectile` | Red Mushroom Puff |
| `farmstorm:brown_mushroom_puff` | `BrownMushroomPuffProjectile` | Brown Mushroom Puff |
| `farmstorm:turret_bullet` | `TurretBullet` | none (fired by the Pumpkin Turret turret) |

The 4 bullets of Melon Grenade reuse the `farmstorm:melon_slice_bomb` entity type (carrying the `submunition` flag) instead of registering an extra type.

### 3.5 recipe types / serializers — 1 each

| registry | ID | Note |
|---|---|---|
| `Registries.RECIPE_TYPE` | `farmstorm:converting` | `RecipeType<ConverterRecipe>` |
| `Registries.RECIPE_SERIALIZER` | `farmstorm:converting` | `ConverterRecipeSerializer` |

### 3.6 creative tabs — 1

| ID | Title lang key | Contents |
|---|---|---|
| `farmstorm:main` | `itemGroup.farmstorm.main` | 22 items ordered as crop → seeding item → tool block |

### 3.7 damage types — 2

Data-driven: the instances of the `Registries.DAMAGE_TYPE` registry are supplied by datapack JSON, and the code side only holds `ResourceKey<DamageType>` constants (`DeferredRegister` takes no part in instance registration).

| ID | Use | Key attributes |
|---|---|---|
| `farmstorm:void_bolt` | Pumpkin Turret missile hit (the 1 point of void damage) | `exhaustion=0.0`, `scaling=when_caused_by_living_non_player`, `effects=hurt`; merged into `#minecraft:is_projectile` |
| `farmstorm:crop_blast` | damage source of every crop explosion | `exhaustion=0.1`, `effects=hurt`; merged into `#minecraft:is_explosion` (blast protection / resistance enchantments take effect on this basis) |

### 3.8 data components — 1

| ID | Type | Use |
|---|---|---|
| `farmstorm:temporary_enchantments` | `List<TempEnchantRecord>` (`Codec` + `StreamCodec`) | DEC D-15 temporary enchantment ledger |

`record TempEnchantRecord(Holder<Enchantment> enchantment, int granted, int previous, long expireAt)`.

### 3.9 menu types — 1

| ID | Class | Note |
|---|---|---|
| `farmstorm:converter` | `ConverterMenu` | client screen `ConverterScreen` (does not occupy a registry) |

### 3.10 config — 1

| Item | Value |
|---|---|
| File | `config/farmstorm-server.toml` |
| Type | `ModConfig.Type.SERVER` |
| Spec object | `FarmstormConfig.SPEC` |
| Registration point | `modContainer.registerConfig(...)` inside the mod constructor |
| Entry count | see §4.11 (117 keys in total) |

### 3.11 Count summary

| registry | Count |
|---|---|
| items | 22 |
| blocks | 20 |
| block_entity_type | 6 |
| entity_type | 9 |
| recipe_type | 1 |
| recipe_serializer | 1 |
| creative_mode_tab | 1 |
| damage_type (data-driven, 2 keys on the code side) | 2 |
| data_component_type | 1 |
| menu_type | 1 |
| **registry total** | **64** |
| config spec (not a registry) | 1 |
| **Grand total** | **65** |

---

## 4. Core Class Design

This section gives the **responsibilities / inheritance / key method signatures / state fields** of every key class. Signatures follow 1.21.1 + NeoForge 21.1.x; version-sensitive APIs (`FoodProperties.Builder`, `PotionContents`, `EnchantmentHelper`) are marked "subject to build time".

### 4.1 `Farmstorm` (main class)

- **Responsibility**: mod entry point; holds `MOD_ID` / `LOGGER` / the global `TeamResolver` singleton; attaches the 8 `DeferredRegister` instances to the mod event bus; registers the config; mounts the DataGen and creative tab events.
- **Inheritance**: none (`final class` annotated with `@Mod(Farmstorm.MOD_ID)`).
- **State fields**: all `static final`, no instance state.

```java
@Mod(Farmstorm.MOD_ID)
public final class Farmstorm {
    public static final String MOD_ID = "farmstorm";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final TeamResolver TEAMS = new ScoreboardTeamResolver();

    public Farmstorm(IEventBus modBus, ModContainer container) {
        FarmstormItems.REGISTER.register(modBus);
        FarmstormBlocks.REGISTER.register(modBus);
        FarmstormBlockEntities.REGISTER.register(modBus);
        FarmstormEntityTypes.REGISTER.register(modBus);
        FarmstormMenus.REGISTER.register(modBus);
        FarmstormRecipes.REGISTER.register(modBus);
        FarmstormDataComponents.REGISTER.register(modBus);
        FarmstormCreativeTabs.REGISTER.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, FarmstormConfig.SPEC);

        modBus.addListener(FarmstormModEvents::onGatherData);
        modBus.addListener(FarmstormModEvents::onBuildCreativeTab);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
```

> Note: since 1.21 the `ResourceLocation` constructor is private, so `fromNamespaceAndPath` must be used; NeoForge 1.20.5+ supports `@Mod` constructor injection of `IEventBus` and `ModContainer`, so `FMLJavaModLoadingContext` is not needed.

### 4.2 `TeamResolver` + `ScoreboardTeamResolver` (DEC D-06 / C-9)

- **Responsibility**: pulls "is this player on the deployer side" out of the gameplay code; v0.0.1 implements it with vanilla scoreboard teams, and FTB Teams can replace it later without touching the call sites.
- **Inheritance**: one interface + a single implementation.
- **Resolution timing (C-9)**: a deployed object records identity only, never a team; **at the instant of every trigger / target acquisition** the current team of the owner is resolved live. Team changes therefore take effect **immediately** — if A joins team X after deploying, teammate B is instantly immune; if A leaves the team, B instantly becomes hostile again.

```java
public interface TeamResolver {
    /** Ownership identity: UUID is mandatory; name is only used to look the owner up in the Scoreboard member table while offline (may be null). Carries no team information. */
    record Owner(UUID uuid, @Nullable String name) {}

    /** Own-side test: resolve the current team of owner at the trigger instant, then compare it with the current team of target. */
    boolean isAlly(ServerLevel level, Owner owner, Player target);

    /** Live team name (works online and offline); empty when there is no team. */
    Optional<String> teamName(ServerLevel level, Owner owner);
}
```

```java
public final class ScoreboardTeamResolver implements TeamResolver {
    @Override public Optional<String> teamName(ServerLevel level, Owner owner) {
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = owner.name() == null ? null : scoreboard.getPlayersTeam(owner.name());
        return team == null ? Optional.empty() : Optional.of(team.getName());
    }

    @Override public boolean isAlly(ServerLevel level, Owner owner, Player target) {
        if (owner.uuid().equals(target.getUUID())) return true;       // the deployer itself
        Optional<String> ownerTeamName = teamName(level, owner);      // resolved live
        if (ownerTeamName.isEmpty()) return false;                    // D-31 / G-8: no team → self convention
        PlayerTeam targetTeam = target.getTeam();                     // 1.21: Entity#getTeam()
        return targetTeam != null && ownerTeamName.get().equals(targetTeam.getName());
    }
}
```

**Key conventions**:

- A deployed block stores only `ownerUuid` + `ownerName` (C-9); it **stores no team** and holds no `PlayerTeam` reference (team objects become invalid after a save reload).
- `Scoreboard#getPlayersTeam(String)` is a hash lookup "name → team", O(1), and it **also works for offline players** — the member table is indexed by name and does not depend on the entity being online. This is the only reason `ownerName` must be stored.
- **Offline / deleted owner**: while offline, the owner is judged by the last known team as usual; if the name appears in no member table (entry deleted, renamed, or never joined a team) → treated as having no team → the `self` convention applies (D-31 / G-8): only the deployer counts as own side and every other player counts as non-own-side, that is, it takes effect against all online players (consistent with `01-DESIGN.md` §7.2).
- **Renaming**: it only affects how `ownerName` is displayed and the hit rate of the reverse lookup; the primary key for judgement remains `ownerUuid` (the deployer is always immune to the traps they deployed).
- **Caching**: within one tick the `teamName` result is cached with `ownerName` as the key and cleared on `LevelTickEvent.Post` — this avoids a 20-block aura repeatedly looking up the same owner within one tick.

### 4.3 Item Hierarchy

Inheritance tree:

```
Item
├── ModifiedCropItem                 (crop item base: sow split + tooltip + CropProfile)
│   ├── ThrowableCropItem            (throwable: 8)
│   └── FoodCropItem                 (edible: Power Beet / Lucky Berry)
├── DeployableCropItem extends BlockItem   (deployable: 5, also the item of the deployed block)
└── SeedItem extends ItemNameBlockItem     (6 standalone seeding items)
```

#### 4.3.1 `ModifiedCropItem`

- **Responsibility**: the common superclass of the 15 crop items. Holds the static profile `CropProfile`; provides the shared tooltip; provides the split "sow when aimed at a plantable face"; uniformly disables vanilla uses (DEC D-09).
- **Key methods**:

```java
public class ModifiedCropItem extends Item {
    private final CropProfile profile;

    public ModifiedCropItem(CropProfile profile, Item.Properties props) { super(props); this.profile = profile; }

    public CropProfile profile() { return this.profile; }

    /** Sow split: on hitting a plantable face, place profile.cropBlock(); otherwise fall back to the default behavior. */
    @Override public InteractionResult useOn(UseOnContext ctx);

    /** Whether sowing is allowed at this position (soil + light + space). */
    protected boolean canPlantAt(UseOnContext ctx, BlockState soil);

    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext ctx,
                                          List<Component> tips, TooltipFlag flag);
}
```

- **State fields**: `profile` (`CropProfile`: `ResourceLocation id`, `ResourceKey<Block> cropBlock`, `@Nullable ResourceKey<Item> seedItem`, `DeployedKind kind`, `Set<AttachFace> allowedFaces`, `int throwStack`). `CropProfile` is a `record`, and the 15 constants are collected in the `CropProfile.ALL` table of `util/CropProfile.java` (no new file is added).

#### 4.3.2 `ThrowableCropItem`

- **Responsibility**: right-click spawns the matching projectile entity, consumes 1 item, and enters a short cooldown; stack size 16 (D-16).
- **Key methods**:

```java
public class ThrowableCropItem extends ModifiedCropItem {
    private final Supplier<EntityType<? extends ThrownCropEntity>> projectileType;

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand);

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }

    /** Lets subclasses override initial velocity and spread (needed by the submunitions of Melon Grenade). */
    protected ThrownCropEntity createProjectile(ServerLevel level, Player player, ItemStack stack);

    protected float velocity() { return 1.5F; }
    protected float inaccuracy() { return 1.0F; }
}
```

Order of the `use()` implementation (server authoritative): `if (level.isClientSide) return InteractionResultHolder.success(stack)` → spawn the entity → `entity.setItem(stack)` → `entity.shootFromRotation(player, xRot, yRot, 0.0F, velocity(), inaccuracy())` → `level.addFreshEntity(entity)` → `stack.consume(1, player)` → `player.getCooldowns().addCooldown(this, cfg.throwCooldown)` → `player.awardStat(Stats.ITEM_USED.get(this))`.

#### 4.3.3 `SeedItem`

- **Responsibility**: the single implementation of the 6 standalone seeding items, equivalent to `ItemNameBlockItem`, except that the "target block" is swapped for the matching crop block (suffix one of `_crop` / `_stem` / `_stalk` / `_block` / `_bush`) and a `CropProfile` is added so the tooltip can be reused.
- **Key methods**: only `appendHoverText` is overridden; the sowing logic is inherited from `ItemNameBlockItem`.

```java
public class SeedItem extends ItemNameBlockItem {
    private final CropProfile profile;
    public SeedItem(CropProfile profile, Block cropBlock, Item.Properties props) { super(cropBlock, props); this.profile = profile; }
}
```

#### 4.3.4 `FoodCropItem`

- **Responsibility**: edible items (Power Beet, Lucky Berry). `FoodProperties` carries nutrition / saturation only; **the effects are applied in `finishUsingItem`**, because Lucky Berry needs server-side RNG and Power Beet needs to apply 4 effects at once.
- **Key methods**:

```java
public class FoodCropItem extends ModifiedCropItem {
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living);

    /** Implemented by subclasses: what happens after eating (called on the server only). */
    protected void onEaten(ItemStack stack, ServerLevel level, ServerPlayer player);
}
```

- **State fields**: no extra fields; `FoodProperties` is supplied at registration through `Item.Properties#food(...)`.

```java
// registration example (D-29: Power Beet and Lucky Berry both use 4 / 0.3, eating time 32 ticks)
ITEMS.registerItem("power_beet", p -> new FoodCropItem(CropProfile.POWER_BEET,
        p.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible().build())),
        new Item.Properties());
```

> The method names of `FoodProperties.Builder` changed during 1.21.x (`saturationModifier` / `saturation`, whether `alwaysEdible()` takes an argument), so the API actually available at build time prevails.

#### 4.3.5 `DeployableCropItem`

- **Responsibility**: the items of the 5 deployable crops, which are also the `BlockItem` of the deployed block (T-02). `useOn` performs the either-or split.
- **Key methods**:

```java
public class DeployableCropItem extends BlockItem {
    private final CropProfile profile;

    @Override public InteractionResult useOn(UseOnContext ctx) {
        // 1) the hit face is plantable (farmland / sand / grass underneath) → sow profile.cropBlock()
        // 2) otherwise super.useOn(ctx) → BlockItem placement flow → DeployedCropBlock#getStateForPlacement
        return super.useOn(ctx);
    }

    @Override protected boolean placeBlock(BlockPlaceContext ctx, BlockState state);
}
```

### 4.4 `DeployedCropBlock`

- **Responsibility**: the single implementation of the 5 deployed blocks. Handles: attachment face validation, floating state maintenance, ownership check, forced mining duration, drop policy, and tick dispatch (handing the tick to the BlockEntity).
- **Inheritance**: `BaseEntityBlock` (needs a BlockEntity + blockstate + codec).
- **State fields (BlockState properties)**:

| Property | Type | Note |
|---|---|---|
| `FACE` | `EnumProperty<AttachFace>` (`FLOOR` / `CEILING` / `WALL`) | attachment face; together with `FACING` it determines the orientation when `WALL` |
| `FACING` | `DirectionProperty` | used by `WALL` only; fixed to `NORTH` for `FLOOR` / `CEILING` |
| `FLOATING` | `BooleanProperty` | set to true after the supporting block disappears (Pumpkin Turret / Torchflower / Pitcher Plant, plan lines 93 / 101 / 109) |

- **Instance fields (block-level immutable configuration)**: `DeployedKind kind`, `EnumSet<AttachFace> allowedFaces`, `boolean supportsFloating`, `boolean turretHardMine` (`pumpkin_turret` only, T-04), `boolean explosionImmune` (Torchflower / Pitcher Plant), `boolean allyOnlyBreak` (the same two).
- **Key method signatures**:

```java
public class DeployedCropBlock extends BaseEntityBlock {
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FLOATING = BooleanProperty.create("floating");

    // placement
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx);
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos);

    // floating maintenance
    @Override protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                               LevelAccessor level, BlockPos pos, BlockPos neighborPos);

    // break rules (T-03 / T-04 / D-13 / D-14)
    @Override public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos);
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player);
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston);

    // interaction
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                        Player player, BlockHitResult hit);

    // BlockEntity wiring
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state);
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type);

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder);
    @Override protected MapCodec<? extends BaseEntityBlock> codec();
}
```

**Placement rules (`getStateForPlacement`)**:

1. `ctx.getClickedFace()` must belong to `allowedFaces` (Blast Carrot / cactus / Torchflower / Pitcher Plant = `FLOOR` only; Pumpkin Turret = all three faces).
2. The supporting block must be a **full opaque block**: `support.isCollisionShapeFullBlock(level, supportPos) && support.canOcclude()`. This is the only place where item 11 of the plan, "must be a full opaque block", is enforced; slabs / glass / leaves are rejected outright.
3. The target position must be a replaceable air-like block, and must not collide with entities.
4. `FLOATING` starts as `false`; blocks with `supportsFloating == false` never read the property (the property is registered uniformly and the semantics are decided by the field, which avoids writing a separate `createBlockStateDefinition` for each of the 5 blocks).

**Floating rules (`updateShape`)**: when the supporting block disappears —

- `supportsFloating == true` → `state.setValue(FLOATING, true)`, and mark the BE as `detached = true` (the behavior keeps running);
- otherwise → return `Blocks.AIR.defaultBlockState()` (the drop is handled by `onRemove` + the loot table).

**Block behavior (aligned with `01-DESIGN.md` §7.4 / §7.5)**: every deployed block sets `PistonBehavior.BLOCK` — it cannot be pushed or pulled by a piston, otherwise owner / attachment face / attachment position would all become inconsistent at once; deployed blocks in unloaded chunks do not tick (traps do not trigger, turrets do not fire, auras do not refresh) and are saved with the chunk as usual.

**Break rules**:

| Block | Own side / creative | Non-own-side |
|---|---|---|
| `pumpkin_turret` | `getDestroyProgress` returns `1.0F / 200.0F` (fixed 10 seconds, ignoring tool and enchantments) | same as the left cell (10 seconds does not stop it either, but the miner takes turret fire while mining) |
| `awakened_torchflower` / `awakened_pitcher_plant` | normal mining (returning `-1F` means "use the default formula") | returns `0.0F`, and `event.setCanceled(true)` in `playerWillDestroy` |
| `blast_carrot` / `fang_trap_cactus` | normal mining | normal mining (a one-shot trap; taking it apart is the same as defusing it, which is accepted) |

When `getDestroyProgress` returns `0.0F` the client also computes zero progress (the BE is already synced to the client, see §4.5), so the feel is consistent and no client-side prediction patch is needed.

> Decompiler-level detail: the return value of `getDestroyProgress` is the progress increment per tick, and `1.0F / 200` is exactly 200 ticks (10 seconds at 20 tps).

**`codec()`**: since 1.21 `Block` must implement `codec()`; this class needs parameters such as `DeployedKind`, so `RecordCodecBuilder.mapCodec` exposes `kind` (enum codec) and the three booleans, which makes `/setblock` and data generation reusable.

### 4.5 `DeployedCropBlockEntity`

- **Responsibility**: all runtime state and logic of a deployed object — ownership record, behavior instance, tick scheduling, sync, drop protection.
- **Inheritance**: `BlockEntity` (does not implement `Container`).
- **State fields**:

| Field | Type | Synced | Note |
|---|---|---|---|
| `ownerUuid` | `@Nullable UUID` | yes | deployer; the primary key for own-side determination (C-9) |
| `ownerName` | `String` | yes | deployer name; used while offline to look up the last known team in the Scoreboard member table (§4.2), and also used for the tooltip |
| `placedGameTime` | `long` | yes | deployment instant; used for the 10-second forced mining and cooldown advancement |
| `behavior` | `DeployedBehavior` | no (derived from `kind`) | strategy object, a static singleton, not serialized |
| `cooldown` | `int` | no | countdown until the turret fires again / the aura next takes effect |
| `pendingWaves` | `List<FangWave>` | yes | delayed queue for the two rings of fangs of the cactus trap |
| `fuseTicks` | `int` | yes | used by delayed-detonation types; 0 means none |
| `detached` | `boolean` | yes | whether it is already floating; affects particles and tooltip |

`record FangWave(long atTick, int count, double radius)`.

- **Key method signatures**:

```java
public class DeployedCropBlockEntity extends BlockEntity {
    public static final int HARD_MINE_TICKS = 200;

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeployedCropBlockEntity be);

    public void setOwner(Player placer);   // writes only ownerUuid + ownerName (C-9), never a team
    public boolean isAlly(Player player);                        // goes through Farmstorm.TEAMS internally
    public @Nullable ServerPlayer owner(ServerLevel level);       // resolve the UUID to an online player
    public DeployedBehavior behavior();

    /** Self-destruct of a one-shot deployed object (trap): run the behavior callback first, then remove the block and play particles. */
    public void consume();

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries);
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries);
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries);
    @Override public Packet<ClientGamePacketListener> getUpdatePacket();
}
```

**Tick dispatch (`serverTick`)**:

```java
public static void serverTick(Level level, BlockPos pos, BlockState state, DeployedCropBlockEntity be) {
    if (!(level instanceof ServerLevel server)) return;             // belt and braces: the client never ticks
    if (be.behavior == null) be.behavior = DeployedBehaviors.of(be.kind);

    long now = server.getGameTime();
    be.drainPendingWaves(server, pos, state, now);                  // 1) delayed fang waves
    if (be.cooldown > 0) { be.cooldown--; return; }                 // 2) cooldown
    if ((now + be.stagger) % be.behavior.scanInterval() == 0) {     // 3) staggered main scan
        be.behavior.tick(be);
    }
}
```

- `stagger = Math.floorMod(be.getBlockPos().hashCode(), be.behavior.scanInterval())`: spreads several deployed objects inside the same chunk onto different ticks, avoiding a same-tick spike (§8.1).

**Ownership and disassembly protection**: in `onRemove`, if `supportsFloating` is false the block drops according to the loot table; if `ownerUuid == null` because of `/setblock` or structure generation, that deployed object is "non-own-side" for every player (anyone may break it, and the trap triggers for everyone), which is an acceptable degradation.

### 4.6 The `DeployedBehavior` family (DEC C-4)

```java
public interface DeployedBehavior {
    DeployedKind kind();
    /** Main scan interval (ticks). Trap / turret 10, aura 100. */
    int scanInterval();
    void tick(DeployedCropBlockEntity be);
    default void onDeployed(DeployedCropBlockEntity be) {}
    default boolean canBeBrokenBy(Player player, DeployedCropBlockEntity be) { return true; }
}

public interface TrapBehavior extends DeployedBehavior {
    default int scanInterval() { return 10; }
    Optional<Player> findTrigger(DeployedCropBlockEntity be);
    void trigger(DeployedCropBlockEntity be, Player target);
    @Override default void tick(DeployedCropBlockEntity be) {
        findTrigger(be).ifPresent(t -> trigger(be, t));
    }
}

public interface TurretBehavior extends DeployedBehavior {
    default int scanInterval() { return 10; }
    Optional<Entity> findTarget(DeployedCropBlockEntity be);    // players first, projectiles optional (C-3)
    void fire(DeployedCropBlockEntity be, Entity target);
    int cooldownTicks();
}

public interface AuraProvider extends DeployedBehavior {
    default int scanInterval() { return 100; }                 // 5 seconds
    double radius();                                           // Euclidean (DEC D-20)
    int effectDurationTicks();                                 // 15 seconds = 300 ticks
    void apply(DeployedCropBlockEntity be, ServerPlayer player);
}
```

The default `tick` of `AuraProvider` iterates `ServerLevel#players()` (instead of scanning entities with an AABB) — within a 20-block radius the player list is usually in the single digits, which is an order of magnitude cheaper than `getEntitiesOfClass` (§8.1).

**Behavior table of the 5 implementations** (all values come from `plan0.0.1.txt`; per-item configurability in §4.11):

| Implementation | Interface | Judgement geometry | Scan | Behavior highlights |
|---|---|---|---|---|
| `BlastCarrotTrap` | `TrapBehavior` | Chebyshev ≤ 1 | 10 ticks | on trigger, `FarmstormExplosion(power=2, cap=7)` + nausea III for 60 ticks to **every player** inside the explosion range (radius = power × 2 = 4 blocks) (plan line 46; no own-side filtering, D-27); then `be.consume()` |
| `FangTrapCactusTrap` | `TrapBehavior` | Euclidean ≤ 3.0 (DEC C-5) | 10 ticks | after triggering, queues two waves: outer ring 8 fangs, delay 10 ticks (0.5 s); inner ring 5 fangs, delay 4 ticks (0.2 s). The fangs use vanilla `EntityType.EVOKER_FANGS` with owner = the deployer; the geometry takes outer radius 2.0 split evenly into 8 points and inner radius 1.0 split evenly into 5 points (the plan gives no radii); `be.consume()` once both waves are out |
| `PumpkinTurret` | `TurretBehavior` | Chebyshev ≤ 15 (`pumpkin_turret.targetRadius`) | 10 tick scan, 60 tick firing interval (G-2 / D-28) | target priority: non-own-side player → hostile mob (`Monster`) → (when `interceptProjectiles` is true) non-own-side projectile (DEC C-3, D-43). Firing spawns a `TurretBullet`; a hit deals 1.0 damage of `farmstorm:void_bolt` + knockback 1.0 along the direction "pumpkin → target", and **grants no levitation** (plan line 95) |
| `TorchflowerAura` | `AuraProvider` | Euclidean 20 | 100 ticks, effect 300 ticks | Strength III / Resistance II / Haste II / Speed I / Water Breathing I / Night Vision I; holding any `#minecraft:swords` item (main hand or off hand) → `TempEnchantHandler.grant(..., FIRE_ASPECT, 1, 300)`; every application plays `ParticleTypes.FLAME` ×8 (`particlesPerRefresh`) |
| `PitcherPlantAura` | `AuraProvider` | Euclidean 10 | 100 ticks, effect 300 ticks | Regeneration II / Saturation II / Absorption I; iterates `player.getActiveEffects()` and calls `removeEffect` on everything with `MobEffect#getCategory() == MobEffectCategory.HARMFUL`; plays `ParticleTypes.HEART` ×8 (`particlesPerRefresh`) |

**Where "buff-type deployed objects cannot be broken by the non-own-side" is implemented**: `DeployedCropBlock.allyOnlyBreak = true` (Torchflower / Pitcher Plant); the behavior interface keeps `canBeBrokenBy` only as an extension point. The break decision must live at the block layer — `getDestroyProgress` has no access to the behavior object.

**Roman numeral → amplifier conversion** (uniform across the document): "level N" in the plan means `amplifier = N - 1`. Strength 3 → 2, Resistance 2 → 1, Nausea 5 → 4, Speed 1 → 0.

### 4.7 `ThrownCropEntity` + 8 subclasses

- **Responsibility**: projectile base class with unified client rendering (using the item model), unified `onHit` dispatch, and unified "discard immediately after impact".
- **Inheritance**: `ThrowableItemProjectile` (which already provides `ItemStack` sync and `getDefaultItem`).
- **Key methods**:

```java
public abstract class ThrownCropEntity extends ThrowableItemProjectile {
    private boolean submunition;            // submunition flag of Melon Grenade

    protected ThrownCropEntity(EntityType<? extends ThrownCropEntity> type, Level level);
    protected ThrownCropEntity(EntityType<? extends ThrownCropEntity> type, LivingEntity shooter, Level level);

    @Override protected Item getDefaultItem();
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder);
    @Override protected void onHitEntity(EntityHitResult result);
    @Override protected void onHitBlock(BlockHitResult result);
    @Override protected void readAdditionalSaveData(CompoundTag tag);
    @Override protected void addAdditionalSaveData(CompoundTag tag);

    /** The single behavior entry point after impact; called on the server only. */
    protected abstract void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit);

    public void setSubmunition(boolean value) { this.submunition = value; }
    public boolean isSubmunition() { return this.submunition; }
}
```

The common part of `onHitBlock` / `onHitEntity` is implemented by the base class and must not be overridden by subclasses:

```java
@Override protected void onHitBlock(BlockHitResult r) {
    super.onHitBlock(r);
    if (level() instanceof ServerLevel sl) {
        onImpact(sl, r.getLocation().add(Vec3.atLowerCornerOf(r.getDirection().getNormal()).scale(0.05)), null);
        if (keepAliveAfterImpact()) return;   // Bamboo Firecracker overrides this to true
        discard();
    }
}

protected boolean keepAliveAfterImpact() { return false; }
```

**Subclass behavior table**:

| Subclass | Entity ID | `onImpact` behavior | Defense / limits |
|---|---|---|---|
| `PuffedWheatProjectile` | `farmstorm:puffed_wheat` | replaces a 3×3×3 volume centered on the hit block with `Blocks.HAY_BLOCK`; if any `#minecraft:water` block exists in that volume → uses 5×5×5 instead | skips blocks with blast resistance ≥ 3.6e6 (T-05); the replacement includes air (plan line 38) |
| `UbiServerProjectile` | `farmstorm:ubi_server` | `FarmstormExplosion(power=4.0, cap=2, rule=UNBREAKABLE_SKIP)` | obstruction-breaking types set no resistance threshold, but always skip unbreakable blocks |
| `ToxicUbiServerProjectile` | `farmstorm:toxic_ubi_server` | first `FarmstormExplosion(power=2.0, cap=2)`, then spawn an `AreaEffectCloud` at the hit point (poison, radius 3.0, 600 ticks, G-4), equivalent to a "lingering potion of poison" | `PotionContents` construction changed substantially in 1.21 → subject to build time; fallback: apply `MobEffects.POISON` directly to players inside the cloud |
| `MelonSliceProjectile` | `farmstorm:melon_slice_bomb` | `FarmstormExplosion(power=2.0, cap=6, rule=COBBLESTONE_6)` | resistance threshold 6.0 (DEC D-19 / C-2) |
| `MelonGrenadeProjectile` | `farmstorm:melon_grenade` | ① spawn 4 `MelonSliceProjectile` at `pos + (rand(-0.3,0.3), 1.0, rand(-0.3,0.3))` (`setSubmunition(true)`, random horizontal velocity 0.3); ② then `FarmstormExplosion(power=3.0, cap=8, rule=COBBLESTONE_6)` | each submunition still explodes again by itself (power 2), producing a secondary explosion (plan line 86) |
| `BambooFirecrackerProjectile` | `farmstorm:bamboo_firecracker` | `keepAliveAfterImpact() = true` after impact: sets `fuseTicks = 140`, `noPhysics = true`, `setDeltaMovement(ZERO)`; `tick()` spawns a vanilla `EntityType.WIND_CHARGE` within ±1.5 of itself (internal constant `FIRECRACKER_SCATTER`) every 5 ticks and immediately calls `windCharge.explode()` | `discard()` after 140 ticks; every wind charge explosion uses vanilla logic (no block destruction) |
| `RedMushroomPuffProjectile` | `farmstorm:red_mushroom_puff` | applies nausea V for 60 ticks + blindness I for 60 ticks to **every player** within radius 4.0 | no explosion, no block damage |
| `BrownMushroomPuffProjectile` | `farmstorm:brown_mushroom_puff` | applies weakness II for 200 ticks + slowness II for 100 ticks to every player within radius 4.0 | same as above |

**Why the mushroom types do not exclude the own side**: lines 135 / 144 of `plan0.0.1.txt` say only "every player within 4 blocks of the landing point", and DEC **D-27** states explicitly that the area effects of Blast Carrot and the red / brown mushroom types **do not filter by side, and teammates are hit as well**, while "targets only the non-own-side" from D-07 constrains only the **triggering** of traps and the **target acquisition** of turrets, not one-shot AoE. The implementation therefore calls `level.getEntitiesOfClass(Player.class, aabb, p -> true)` **without** an `isAlly` filter.

**`TurretBullet`** (the missile of the Pumpkin Turret turret):

```java
public class TurretBullet extends ThrowableItemProjectile {
    private @Nullable Entity target;
    public void setTarget(@Nullable Entity target);
    @Override protected void onHitEntity(EntityHitResult r);
    @Override protected void tick();                    // homing: recompute velocity toward target every tick
    @Override protected void onHitBlock(BlockHitResult r);   // vanishes on hitting a block, does not blow up blocks
}
```

Hit handling: `target.hurt(FarmstormDamageTypes.voidBolt(level, owner), 1.0F)` → apply `knockback(1.0, dx, dz)` along the direction "turret position → target position" → `discard()`. Rendering reuses the model and texture of the vanilla `ShulkerBulletRenderer` (`client/render/TurretBulletRenderer`).

### 4.8 The converter quartet

#### 4.8.1 `ConverterBlock`

```java
public class ConverterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override protected MapCodec<? extends BaseEntityBlock> codec();
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state);
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type);
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                        Player player, BlockHitResult hit);
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston);
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
```

- `useWithoutItem`: on the server, `player.openMenu(state.getMenuProvider(level, pos))`; `getMenuProvider` returns `new SimpleMenuProvider((id, inv, p) -> new ConverterMenu(id, inv, ContainerLevelAccess.create(level, pos)), Component.translatable("block.farmstorm.converter"))`.
- `onRemove`: `Containers.dropContents(level, pos, be)`, so that breaking the block does not swallow the items.
- Block properties: `.strength(3.5F)` (same tier as the cartography table), `.requiresCorrectToolForDrops()`, `#minecraft:mineable/pickaxe`.

#### 4.8.2 `ConverterBlockEntity`

```java
public class ConverterBlockEntity extends BlockEntity implements Container {
    private ItemStack input = ItemStack.EMPTY;
    private @Nullable ConverterRecipe cachedRecipe;      // cleared when RecipeManager reloads
    private int cachedXpCost;

    @Override public int getContainerSize() { return 2; }      // 0 = input, 1 = Nether Star catalyst
    @Override public ItemStack getItem(int slot);
    @Override public ItemStack removeItem(int slot, int amount);
    @Override public void setItem(int slot, ItemStack stack);
    @Override public boolean stillValid(Player player);
    @Override public void setChanged();

    /** Transactional conversion: validate everything first, and only if all checks pass consume materials and produce output. */
    public boolean tryConvert(ServerPlayer player);

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries);
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries);
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries);
}
```

Validation order of `tryConvert` (**the order is the safety property**, DEC D-12):

1. `input` is empty → false.
2. Forward recipe: `level.getRecipeManager().getRecipeFor(FarmstormRecipes.CONVERTING_TYPE.get(), new SingleRecipeInput(input), level)`; if nothing matches and reverse is allowed, iterate `getAllRecipesFor` and compare `result` against `input` (cached in `cachedRecipe`).
3. `player.totalExperience >= recipe.xpCost()` (if insufficient, reject outright, **never deduct levels**).
4. Catalyst: when `recipe.catalyst()` is non-empty, consume it from the **catalyst slot first** (slot 1, Nether Stars only) and, when the slot cannot cover `catalystCount`, fall back to scanning the player inventory (including the off hand), without consuming the item in the main hand itself.
5. Only after everything passes does state change: `input.shrink(1)` → `player.giveExperiencePoints(-xpCost)` → deduct the catalyst → hand the output to `player.getInventory().placeItemBackInInventory(...)`.
6. `setChanged()` + `level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)` to refresh the GUI.

> Steps 2–4 must complete within the same server tick, and no client insertion is accepted in between; the button packet only triggers `tryConvert` and carries no parameter that could be forged.

#### 4.8.3 `ConverterMenu`

```java
public class ConverterMenu extends AbstractContainerMenu {
    public static final int BUTTON_CONVERT = 0;

    private final Container container;
    private final ContainerLevelAccess access;
    private final ContainerData data;      // [0]=xpCost  [1]=canConvert(0/1)  [2]=reverse(0/1)

    public ConverterMenu(int id, Inventory inv, FriendlyByteBuf buf);                 // client constructor
    public ConverterMenu(int id, Inventory inv, ContainerLevelAccess access);         // server constructor

    @Override public void slotsChanged(Container c);
    @Override public boolean clickMenuButton(Player player, int id);
    @Override public ItemStack quickMoveStack(Player player, int index);
    @Override public boolean stillValid(Player player);
}
```

- Slots, by **menu index** (= block-entity slot for the first two): **0 = input** (`new Slot(container, 0, 80, 22)`), **1 = catalyst** (`new Slot(container, 1, 44, 22)`, `mayPlace` accepts Nether Stars only), **2 = read-only preview** (`new Slot(preview, 0, 116, 22)`, `mayPlace = false`, `mayPickup = false`); the player inventory starts at index **3** (3 rows + hotbar).
- Visual order on the texture is a different thing from the indices: left to right the three slots are catalyst (44) / input (80) / preview (116), all on the same row y = 22.
- `clickMenuButton(player, 0)` → `container instanceof ConverterBlockEntity be && be.tryConvert((ServerPlayer) player)`.
- `quickMoveStack`: the preview slot is skipped; shift-click moves between the player inventory and slots 0~1 (input + catalyst).

#### 4.8.4 `ConverterScreen`

```java
public class ConverterScreen extends AbstractContainerScreen<ConverterMenu> {
    private static final ResourceLocation BG = Farmstorm.id("textures/gui/converter.png");
    @Override protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY);
    @Override protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY);
}
```

Registration: on the client, inside `@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)`, listen for `RegisterMenuScreensEvent` → `e.register(FarmstormMenus.CONVERTER.get(), ConverterScreen::new)`. - The screen owns exactly one `Button` (**Convert**): clicking it calls `handleInventoryButtonClick(containerId, BUTTON_CONVERT)`; it is greyed out (`active = false`) whenever `canConvert` is false, and the experience cost is drawn at the right-hand end of the title row.
- The button only emits the native `ServerboundContainerButtonClickPacket` and carries no parameter that could be forged — **no custom network packet is added** (§5.4).

#### 4.8.5 `ConverterRecipe` + `ConverterRecipeSerializer`

```java
public class ConverterRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack result;
    private final int xpCost;
    private final Ingredient catalyst;          // Ingredient.EMPTY means no catalyst
    private final int catalystCount;
    private final boolean bidirectional;
    private final int reverseXpCost;

    @Override public boolean matches(SingleRecipeInput in, Level level);
    @Override public ItemStack assemble(SingleRecipeInput in, HolderLookup.Provider registries);
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result; }
    @Override public RecipeSerializer<?> getSerializer() { return FarmstormRecipes.CONVERTING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return FarmstormRecipes.CONVERTING_TYPE.get(); }

    /** Reverse view: not written to disk, generated at runtime, used by the converter for reverse matching. */
    public ConverterRecipe reverse();
}
```

```java
public class ConverterRecipeSerializer implements RecipeSerializer<ConverterRecipe> {
    public static final MapCodec<ConverterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("input").forGetter(ConverterRecipe::input),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ConverterRecipe::result),
            Codec.INT.optionalFieldOf("xp_cost", 3).forGetter(ConverterRecipe::xpCost),
            Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.EMPTY).forGetter(ConverterRecipe::catalyst),
            Codec.INT.optionalFieldOf("catalyst_count", 1).forGetter(ConverterRecipe::catalystCount),
            Codec.BOOL.optionalFieldOf("bidirectional", true).forGetter(ConverterRecipe::bidirectional),
            Codec.INT.optionalFieldOf("reverse_xp_cost", 0).forGetter(ConverterRecipe::reverseXpCost)
    ).apply(inst, ConverterRecipe::new));

    @Override public MapCodec<ConverterRecipe> codec() { return CODEC; }
    @Override public StreamCodec<RegistryFriendlyByteBuf, ConverterRecipe> streamCodec();
}
```

**JSON schema** (`data/farmstorm/recipe/*.json`):

```json
{
  "type": "farmstorm:converting",
  "input": { "item": "minecraft:wheat" },
  "result": { "id": "farmstorm:puffed_wheat", "count": 1 },
  "xp_cost": 3,
  "bidirectional": true,
  "reverse_xp_cost": 0
}
```

Torchflower / Pitcher Plant (DEC D-12: forward only, and additionally consumes 1 nether star):

```json
{
  "type": "farmstorm:converting",
  "input": { "item": "minecraft:torchflower" },
  "result": { "id": "farmstorm:awakened_torchflower", "count": 1 },
  "xp_cost": 3,
  "catalyst": { "item": "minecraft:nether_star" },
  "catalyst_count": 1,
  "bidirectional": false
}
```

Field semantics:

| Field | Type | Required | Default | Note |
|---|---|---|---|---|
| `type` | string | yes | — | fixed `farmstorm:converting` |
| `input` | Ingredient | yes | — | supports `{"item":...}` / `{"tag":...}` / arrays (any one of) |
| `result` | ItemStack | yes | — | `{"id":..., "count":1}` |
| `xp_cost` | int | no | 3 | overrides the global `converter.xpCost` value |
| `catalyst` | Ingredient | no | empty | additional ingredient (nether star) |
| `catalyst_count` | int | no | 1 | ingredient count |
| `bidirectional` | bool | no | true | whether "modified → vanilla" is allowed |
| `reverse_xp_cost` | int | no | 0 | experience cost of the reverse direction (D-12: reverse is free) |

### 4.9 `FarmstormExplosion` + `BlastRule`

- **Responsibility**: satisfy four constraints at once — ① destructibility predicate (which blocks may be destroyed); ② resistance threshold (DEC D-19: resistance ≥ cobblestone 6.0 is not destroyed); ③ **per-entity damage cap** (the "deals at most N damage" of the plan); ④ correct kill attribution and death message (T-06).
- **Inheritance**: `Explosion` (reuses the constructor, the `Level` registration flow and `ExplosionInteraction` semantics).
- **Why `finalizeExplosion` must be overridden**: vanilla `Explosion#finalizeExplosion(boolean)` hardcodes "particles / sound / entity damage / block destruction" into one method, and a damage cap cannot be inserted by a subclass. Overriding it in a subclass and doing it in three phases is the least invasive implementation of this requirement (no Mixin, T-07).

```java
public record BlastRule(Predicate<BlockState> extraBreakable,
                        float maxResistance,
                        boolean dropsBlocks) {
    public static final BlastRule VANILLA           = new BlastRule(s -> true, 3.6e6F, true);
    public static final BlastRule UNBREAKABLE_SKIP  = new BlastRule(s -> true, 3.6e6F, true);
    /** DEC D-19: blast resistance >= cobblestone (6.0) is not destroyed. */
    public static final BlastRule COBBLESTONE_6     = new BlastRule(s -> true, 6.0F, true);

    public boolean canBreak(BlockGetter level, BlockPos pos, BlockState state, Explosion exp) {
        if (!extraBreakable.test(state)) return false;
        return state.getExplosionResistance(level, pos, exp) < maxResistance;
    }
}
```

```java
public class FarmstormExplosion extends Explosion {
    private final BlastRule rule;
    private final float damageCap;
    private final DamageSource damageSource;

    public FarmstormExplosion(ServerLevel level, @Nullable Entity sourceEntity, DamageSource dmg,
                              double x, double y, double z, float radius,
                              BlastRule rule, float damageCap) {
        super(level, sourceEntity, dmg, null, x, y, z, radius, false, Explosion.BlockInteraction.DESTROY);
        this.rule = rule;
        this.damageCap = damageCap;
        this.damageSource = dmg;
    }

    @Override public void finalizeExplosion(boolean spawnParticles) {
        breakBlocks();            // 1) block destruction, constrained by BlastRule
        hurtCapped();             // 2) damage, constrained by damageCap
        spawnFx(spawnParticles);  // 3) particles and sound
    }

    private void breakBlocks();
    private void hurtCapped();
    private void spawnFx(boolean spawnParticles);

    /** Vanilla hook: makes other mods and structure checks follow our rule as well. */
    @Override public boolean shouldBlockExplode(Explosion e, BlockGetter level, BlockPos pos,
                                                BlockState state, float power) {
        return rule.canBreak(level, pos, state, e);
    }
}
```

**Damage cap algorithm** (`hurtCapped`, capped per entity independently):

```text
center c = (x, y, z); radius r
for each LivingEntity e in level.getEntitiesOfClass(LivingEntity.class, aabb(r*2)) where !e.ignoredByExplosion():
    dist = e.position().distanceTo(c)
    if dist > r * 2: continue
    exposure = Explosion.getSeenPercent(c, e)          // 0..1, line-of-sight exposure
    impact   = (1.0 - dist / (r * 2)) * exposure
    raw      = (impact * impact + impact) / 2.0 * 7.0 * r * 2.0 + 1.0      // same formula as vanilla
    finalDmg = min(raw, damageCap)
    e.hurt(damageSource, finalDmg)
    e.push(...)                                        // same knockback as vanilla
```

- **Does not exclude the source entity**: the user themselves standing inside the blast takes damage just the same (D-45, **deliberately different** from vanilla `Explosion`, which exempts the source entity). Apart from `Entity#ignoreExplosion` there is no filtering at all — teammates and villagers included.
- **Attribution**: `damageSource` must carry the `owner` (thrower / deployer), so that kill scoring and the death message (X was blown up by Y) are both correct.
- **`#farmstorm:explosion_immune`**: "no explosion can destroy it" for Torchflower / Pitcher Plant (D-14) is excluded through `BlastRule.extraBreakable`, implemented as `!state.is(FarmstormTags.EXPLOSION_IMMUNE)`; the tag file is in §6.
- **Destruction-only type**: when `damageCap <= 0`, `hurtCapped` is skipped (reserved for future destruction-only crops).

### 4.10 `TempEnchantHandler` (DEC D-15)

- **Responsibility**: attach a "temporary Fire Aspect" to the weapon of players inside the Torchflower aura, revoke it on expiry or when leaving the range, and **do not overwrite the existing enchantment level**.
- **Shape**: a purely static utility class (final + private ctor); all state lives in the DataComponent of the item (T-08).

```java
public final class TempEnchantHandler {
    public static void grant(ServerPlayer player, ItemStack stack, Holder<Enchantment> ench,
                             int level, int durationTicks);
    public static void tick(ServerPlayer player);        // PlayerTickEvent.Post, every 20 ticks
    public static void revokeExpired(ItemStack stack, long now);
    public static void revokeAll(ItemStack stack);        // leaving the aura / logout cleanup
    public static boolean hasTemporary(ItemStack stack, Holder<Enchantment> ench);
    private static void write(ItemStack stack, List<TempEnchantRecord> records);
    private static List<TempEnchantRecord> read(ItemStack stack);
}
```

**Grant flow**:

1. `int previous = EnchantmentHelper.getItemEnchantmentLevel(ench, stack)` (record the original level).
2. If a record for the same `ench` already exists and its `expireAt` has not passed → refresh only `expireAt`, do not push a new record.
3. `EnchantmentHelper.updateEnchantments(stack, m -> m.set(ench, Math.max(previous, level)))`.
4. Append `new TempEnchantRecord(ench, level, previous, now + durationTicks)` to the component.

**Revoke flow**:

1. `read(stack)` filters out the records with `expireAt <= now`.
2. For each one: `updateEnchantments` sets the level back to `previous`; if `previous == 0`, remove the enchantment.
3. Write the remaining records back; if the list is empty, remove the whole component.
4. **No-overwrite check**: if the current level > `granted` (the player upgraded it themselves with an anvil) → skip the restore and only remove the record, so that the temporary system never lowers the enchantments of the player.

**Trigger points**:

| Scenario | Trigger |
|---|---|
| every 100 ticks of the aura | `grant` on the sword inside `TorchflowerAura.apply` |
| periodic expiry | `PlayerTickEvent.Post`, `player.tickCount % 20 == 0` |
| leaving the range | during the aura tick, `revokeAll` for own-side players outside the radius (revokes only the record the aura granted) |
| logout | `PlayerEvent.PlayerLoggedOutEvent` performs no persisted cleanup; the next tick self-heals |
| item moved into a chest | the record travels with the item and is cleared by `revokeExpired` on the next `tick` after it is taken out |

> Boundary: `gameTime` is monotonically non-decreasing, so a record that never expires cannot hang; a save rollback may make a record expire early — acceptable (revoking a temporary enchantment does not harm the player).

### 4.11 `FarmstormConfig` (complete server config inventory)

> **Alignment statement**: the key names, default values and value ranges of this section **are aligned item by item with `01-DESIGN.md` §3.8** (**117** items in total, including the effect key templates of §3.8.2 and the probability table of §3.8.5). Inside the file the key path is written `<group>.<key>` (landing in `config/farmstorm-server.toml`), while the design document and `translation` write `farmstorm.<group>.<key>` and `farmstorm.configuration.<group>.<key>`. The earlier draft of this section, which used camelCase prefixes (`general` / `blastCarrot` / `torchflower` …), is **void**; the table below is authoritative.

- **Responsibility**: the single entry point for all gameplay values (DEC D-21 / D-22); rebuilds the derived caches after loading and reloading.
- **Registration**: `modContainer.registerConfig(ModConfig.Type.SERVER, FarmstormConfig.SPEC)` inside the mod constructor; `SPEC` is built by `ModConfigSpec.Builder` in a static block — the fields **must not be `final`** (the values are assigned in the static block).
- **Baking**: listen for `ModConfigEvent.Loading` / `ModConfigEvent.Reloading` → `bake()`, which rebuilds the probability table of `LuckyBerryRoller` and the threshold snapshot of `BlastRule`. The "the sum of the 8 entries must equal 100" check of §3.8.5 also runs in `bake()`: if it fails, **that section is rejected and falls back to its default values + `log.warn`**, without affecting the other groups.

```java
public final class FarmstormConfig {
    public static final ModConfigSpec SPEC;
    public static ModConfigSpec.IntValue deployTurretMiningTicks;
    public static ModConfigSpec.DoubleValue blastCarrotExplosionPower;
    // ... 117 items in total

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Deployment system").push("deploy");
        deployTurretMiningTicks = b
                .comment("Fixed forced-mining ticks of turret-type deployed blocks (200 ticks = 10 seconds)")
                .translation("farmstorm.configuration.deploy.turretMiningTicks")
                .defineInRange("turretMiningTicks", 200, 0, 72000);
        b.pop();
        // ... remaining groups
        SPEC = b.build();
    }

    public static void bake();
}
```

#### 4.11.1 General entries (14)

```toml
[converter]
xpCost = 3                     # D-12: each conversion costs 3 experience points
netherStarForwardCost = 1      # D-12: additional forward cost for torchflower / pitcher plant
netherStarOnReverse = false    # D-12: the reverse direction burns no star

[stack]
throwable = 16                 # D-16
other = 64                     # D-16

[deploy]
turretMiningTicks = 200        # D-13: 10 seconds
supportMustBeFullOpaque = true  # plan line 11: the supporting block must be full and opaque
dropOnSupportLost = true       # G-9: drop the block itself when support is lost and floating is not allowed

[teams]
resolver = "scoreboard"        # D-06: scoreboard / ftbteams
untamedFallback = "self"       # G-8: a player without a team is friendly only to themselves

[explosion]
sliceResistThreshold = 6.0     # C-2 / D-19: melon slice
grenadeResistThreshold = 6.0   # D-19: Melon Grenade

[throwable]
lifetimeTicks = 100            # G-6

[food]
eatTicks = 32                  # D-29
```

#### 4.11.2 Per-crop entries (51)

```toml
[puffed_wheat]
dryEdge = 3                    # plan line 38: 3x3x3
wetEdge = 5                    # expands to 5x5x5 when water is present

[blast_carrot]
triggerRadius = 1              # Chebyshev
explosionPower = 2
maxDamage = 7
nausea.level = 3               # nausea III
nausea.durationTicks = 60

[ubi_server]
explosionPower = 4
maxDamage = 2

[toxic_ubi_server]
explosionPower = 2
maxDamage = 2
cloudRadius = 3.0
cloudDurationTicks = 600       # G-4: follows the vanilla lingering potion

[melon_slice_bomb]
explosionPower = 2
maxDamage = 6

[melon_grenade]
spawnCount = 4                 # 4 melon slices
spawnOffsetY = 1
horizontalJitter = 0.3         # x/z jitter 0~0.3
explosionPower = 3
maxDamage = 8

[pumpkin_turret]
targetRadius = 15              # Chebyshev (D-43)
projectileDamage = 1           # void damage
knockbackLevel = 1
fireIntervalTicks = 60         # G-2 / D-28: one shot every 3 seconds

[awakened_torchflower]
auraRadius = 20.0              # Euclidean
refreshTicks = 100
fireAspectLevel = 1
particlesPerRefresh = 8        # G-7

[awakened_pitcher_plant]
auraRadius = 10.0
refreshTicks = 100
clearHarmful = true
particlesPerRefresh = 8

[fang_trap_cactus]
triggerRadius = 3.0            # C-5: Euclidean
outerFangCount = 8
outerDelayTicks = 10
outerRadius = 2.0              # G-3
innerFangCount = 5
innerDelayTicks = 4
innerRadius = 1.0

[bamboo_firecracker]
spawnIntervalTicks = 5
durationTicks = 140            # 140 / 5 = 28 times

[red_mushroom_puff]
radius = 4.0

[brown_mushroom_puff]
radius = 4.0

[lucky_berry]
nutrition = 4                  # D-29
saturationModifier = 0.3
randomCropCount = 16
lootChestCount = 2
smallExplosionPower = 1
creativeDurationTicks = 1800   # D-18: 90 seconds

[power_beet]
nutrition = 4                  # D-29
saturationModifier = 0.3
```

#### 4.11.3 Effect keys (44 = 22 groups × 2)

Template (§3.8.2): `<crop>.<effect>.level` (1 = level I; `amplifier = level - 1` when written) + `<crop>.<effect>.durationTicks` (0 = not applied).

| Crop group | effect group | `level` | `durationTicks` | Resulting amplifier |
|---|---|---|---|---|
| `power_beet` | `strength` | 2 | 600 | 1 |
| `power_beet` | `speed` | 2 | 600 | 1 |
| `power_beet` | `haste` | 2 | 600 | 1 |
| `power_beet` | `resistance` | 1 | 600 | 0 |
| `awakened_torchflower` | `strength` | 3 | 300 | 2 |
| `awakened_torchflower` | `resistance` | 2 | 300 | 1 |
| `awakened_torchflower` | `haste` | 2 | 300 | 1 |
| `awakened_torchflower` | `speed` | 1 | 300 | 0 |
| `awakened_torchflower` | `water_breathing` | 1 | 300 | 0 |
| `awakened_torchflower` | `night_vision` | 1 | 300 | 0 |
| `awakened_pitcher_plant` | `regeneration` | 2 | 300 | 1 |
| `awakened_pitcher_plant` | `saturation` | 2 | 300 | 1 |
| `awakened_pitcher_plant` | `absorption` | 1 | 300 | 0 |
| `red_mushroom_puff` | `nausea` | 5 | 60 | 4 |
| `red_mushroom_puff` | `blindness` | 1 | 60 | 0 |
| `brown_mushroom_puff` | `weakness` | 2 | 200 | 1 |
| `brown_mushroom_puff` | `slowness` | 2 | 100 | 1 |
| `lucky_berry` | `regeneration` (25% branch) | 5 | 100 | 4 |
| `lucky_berry` | `strength` (1.9% branch) | 5 | 12000 | 4 |
| `lucky_berry` | `resistance` (1.9% branch) | 3 | 12000 | 2 |
| `lucky_berry` | `jump_boost` (1.9% branch) | 5 | 12000 | 4 |
| `lucky_berry` | `speed` (1.9% branch) | 2 | 12000 | 1 |

#### 4.11.4 Lucky Berry probability table (8)

```toml
[lucky_berry.chance]
lightning = 15.0          # spawn a lightning bolt at the position of the eater
random_crops = 25.0       # spawn randomCropCount random modified crops
regeneration = 25.0       # regeneration branch
loot_chest = 25.0         # spawn lootChestCount end city loot chests
small_explosion = 5.0     # power smallExplosionPower
charged_creeper = 3.0     # charged creeper
godlike_buff = 1.9        # 600 seconds of godlike buff
creative_mode = 0.1       # creative mode creativeDurationTicks
```

**Validation**: the sum of the 8 entries must equal 100; otherwise that section is rejected and falls back to the default values + a warning (§3.8.5).

#### 4.11.5 Consumer cross-reference (key → the class that reads it)

| Group | Consumer |
|---|---|
| `converter.*` | `ConverterBlockEntity#tryConvert` (`netherStarForwardCost` / `netherStarOnReverse`), `ConverterMenu` (displays `xpCost`) |
| `stack.*` | the `.stacksTo(...)` registration in `FarmstormItems` |
| `deploy.*` | `DeployedCropBlock#getDestroyProgress` (`turretMiningTicks`), `#getStateForPlacement` (`supportMustBeFullOpaque`), `#updateShape` / `#onRemove` (`dropOnSupportLost`) |
| `teams.*` | the `TeamResolver` implementation selected when `Farmstorm` is constructed; `ScoreboardTeamResolver#isAlly` reads `untamedFallback` |
| `explosion.*` | `MelonSliceProjectile` / `MelonGrenadeProjectile` read the threshold when constructing `BlastRule` |
| `throwable.lifetimeTicks` | `ThrownCropEntity#tick` (`tickCount > lifetime` → `discard()`) |
| `food.eatTicks` | `FoodCropItem#getUseDuration` |
| `<crop>.explosionPower` / `.maxDamage` | read by each projectile / trap when constructing `FarmstormExplosion` |
| `<crop>.<effect>.*` | read by each `AuraProvider` / `ThrownCropEntity` / `LuckyBerryRoller` when applying effects |
| `puffed_wheat.dryEdge` / `.wetEdge` | `PuffedWheatProjectile#onImpact` |
| `awakened_torchflower.fireAspectLevel` | `TempEnchantHandler#grant` (passed in by `TorchflowerAura`) |
| `lucky_berry.*` | `LuckyBerryRoller#roll/apply` |

#### 4.11.6 Internal constants that stay out of the config (safety valves)

The following 6 values are **deliberately not exposed as config keys** (they are not registered in `01-DESIGN.md` §3.8, and they are safety valves rather than gameplay values): `DEPLOY_COOLDOWN_TICKS = 5`, `THROW_COOLDOWN_TICKS = 5`, `MAX_DEPLOYED_PER_CHUNK = 8`, `MAX_DEPLOYED_PER_PLAYER = 32`, `MAX_EXPLOSIONS_PER_TICK = 8`, `MAX_EFFECTS_PER_TICK = 64`. To expose one of them, §3.8 must be updated first and the code changed afterwards.

**Count check**: 14 (general) + 51 (per-crop) + 44 (effects) + 8 (probability) = **117 items**.

### 4.12 `LuckyBerryRoller` (DEC D-17 / D-18)

- **Responsibility**: the 8-tier probability table of Lucky Berry — data structure, roll, execution.
- **Shape**: a final utility class + an immutable weight table; the table is rebuilt from the config in `FarmstormConfig.bake()` (the defaults are the percentages of the plan).
- **Percentages in the config layer, permille integers internally**: the config keys are exposed as percentages per `01-DESIGN.md` §3.8.5 (15 / 25 / 25 / 25 / 5 / 3 / 1.9 / 0.1, sum = 100); `bake()` multiplies by 10 and rounds to the internal weights 150 / 250 / 250 / 250 / 50 / 30 / 19 / 1, whose sum is always 1000, so a single `nextInt(1000)` suffices and floating-point accumulation drift is avoided. The config layer validates "sum = 100"; if it fails, that section is rejected and falls back to the default values (§3.8.5).

```java
public final class LuckyBerryRoller {
    public enum Outcome {
        LIGHTNING, SPAWN_CROPS, REGEN, LOOT_CHESTS, SMALL_EXPLOSION,
        CHARGED_CREEPER, POWER, CREATIVE
    }

    /** weight is in permille: derived from the percentage config ×10 and rounded, the sum is always 1000. */
    public record Roll(Outcome outcome, int permille) {}

    /** Baked by FarmstormConfig; read-only at runtime. */
    private static final List<Roll> TABLE = List.copyOf(List.of(
            new Roll(Outcome.LIGHTNING,        150),
            new Roll(Outcome.SPAWN_CROPS,      250),
            new Roll(Outcome.REGEN,            250),
            new Roll(Outcome.LOOT_CHESTS,      250),
            new Roll(Outcome.SMALL_EXPLOSION,   50),
            new Roll(Outcome.CHARGED_CREEPER,   30),
            new Roll(Outcome.POWER,             19),
            new Roll(Outcome.CREATIVE,           1)));

    /** Cumulative weight upper bounds, same length as TABLE, the last element is always 1000. */
    private static final int[] CUMULATIVE = buildCumulative(TABLE);

    public static Outcome roll(RandomSource random) {
        int r = random.nextInt(1000);
        for (int i = 0; i < CUMULATIVE.length; i++) {
            if (r < CUMULATIVE[i]) return TABLE.get(i).outcome();
        }
        return Outcome.SPAWN_CROPS;   // fallback for a misconfigured table, should be unreachable
    }

    public static void apply(ServerLevel level, ServerPlayer player, Outcome outcome);
    private static int[] buildCumulative(List<Roll> table);   // validates sum == 1000; the config layer already guarantees the percentages sum to 100
}
```

**Execution details of the 8 outcomes**:

| Outcome | Probability | Execution (all server side) |
|---|---|---|
| `LIGHTNING` | 15% | `EntityType.LIGHTNING_BOLT` spawned at the player position with `setCause(null)` (no source damage) |
| `SPAWN_CROPS` | 25% | 16 times: uniformly pick 1 of the **14 crops other than Lucky Berry** (D-17) and spawn an `ItemEntity`; position = a random standable block within a 2-block radius of the player (D-33; nearest possible position if the exact one is not available) |
| `REGEN` | 25% | `MobEffects.REGENERATION`, amplifier 4 (level V), 100 ticks (5 seconds) |
| `LOOT_CHESTS` | 25% | spawn 2 vanilla chests and fill them from the `minecraft:chests/end_city_treasure` loot table (`ChestBlockEntity#setLootTable` + `setLootTableSeed`); chest positions follow D-33 as well (standable blocks within a 2-block radius, nearest if not available) |
| `SMALL_EXPLOSION` | 5% | `FarmstormExplosion(power=1.0, cap=999, rule=UNBREAKABLE_SKIP)` — **note**: the plan gives no cap, so "uncapped" is used to avoid damaging the design intent |
| `CHARGED_CREEPER` | 3% | spawn `EntityType.CREEPER` and set `setPowered(true)`; not ignited, left to the player |
| `POWER` | 1.9% | Strength V / Resistance III / Jump Boost V / Speed II, 12000 ticks each (600 seconds) |
| `CREATIVE` | 0.1% | **handled specifically by D-18**: record `player.gameMode.getGameModeForPlayer()` → `player.setGameMode(GameType.CREATIVE)` → store in a `Map<UUID, CreativeRestore>` (**memory only, never written to the save**); force a restore after 1800 ticks (90 seconds). Player logs out mid-way → restore immediately and drop the record in `PlayerLoggedOutEvent`; server restart → the record is lost, but `PlayerLoggedInEvent` performs one reconciliation for "a non-creative save that is in creative mode" (comparing `player.gameMode` against the default save game mode, see §9 risk 5) |

`LuckyBerryRoller.apply` must be called inside `FoodCropItem#onEaten`, and the whole method body must be wrapped in `if (level instanceof ServerLevel)` — the client must not take part in the roll (§5.3).

### 4.13 The two-dimensional Type / Category model (DEC D-48 / PLAN R-47)

`Type` and `Category` are **two independent fields, not one hierarchy** (DESIGN §2.4). `CropProfile` holds them side by side and derives neither from the other; `DeployedKind` (how a deployment behaviour is implemented) is a third thing and belongs to neither dimension.

```java
public record CropProfile(
        ResourceLocation id,
        CropType type,                    // single value      (§2.2 throwable / deployable / edible)
        Set<CropCategory> categories,     // multi-valued set  (§2.3 trap / turret / demolition / buff / construction / damage)
        @Nullable ResourceKey<Block> cropBlock,
        @Nullable ResourceKey<Item> seedItem,
        @Nullable DeployedKind kind,      // implementation detail, NOT a third dimension (C-4)
        Set<AttachFace> allowedFaces,
        int throwStack) {

    /** Can it destroy blocks: decided by the DEMOLITION tag alone, nowhere else. */
    public boolean canBreakBlocks() { return has(CropCategory.DEMOLITION); }
}
```

- `CropType` = `{THROWABLE, DEPLOYABLE, EDIBLE}`. The "plantable" type of DESIGN §2.2.3 deliberately has **no constant** (D-10): it is a cross-cutting capability that all 15 crops carry, so a constant would only create the pseudo-question "which crop is plantable".
- `CropCategory` = `{TRAP, TURRET, DEMOLITION, BUFF, CONSTRUCTION, DAMAGE}`, held as a `LinkedHashSet` so that the tooltip and the start-up self-check print in the order DEC §4 writes them.
- **"Can it destroy blocks" has exactly one criterion**: `BlastRule.forCrop(CropProfile profile, BlastRule whenDemolition)` returns `whenDemolition` only when the profile carries `DEMOLITION`, and `BlastRule.NEVER_BREAK` otherwise. The resistance threshold still comes from the individual call site (the Melon Slice Bomb and the Melon Grenade each read their own config key); the factory only switches destruction on and off (D-48).

**Type / category table of the 15 crops** (copied from DEC §4, with D-42 applied to the Melon Slice Bomb):

| # | Crop | Type | Category |
|---|---|---|---|
| 1 | Puffed Wheat | Throwable | Construction |
| 2 | Blast Carrot | Deployable | Trap, Damage |
| 3 | Ubi Server | Throwable | Demolition, Damage |
| 4 | Toxic Ubi Server | Throwable | Buff, Demolition, Damage |
| 5 | Power Beet | Edible | Buff |
| 6 | Melon Slice Bomb | Throwable | Damage, Demolition |
| 7 | Melon Grenade | Throwable | Demolition, Damage |
| 8 | Pumpkin Turret | Deployable | Turret, Damage |
| 9 | Awakened Torchflower | Deployable | Turret, Buff |
| 10 | Awakened Pitcher Plant | Deployable | Turret, Buff |
| 11 | Fang Trap Cactus | Deployable | Trap, Damage |
| 12 | Bamboo Firecracker | Throwable | Damage |
| 13 | Red Mushroom Puff | Throwable | Buff |
| 14 | Brown Mushroom Puff | Throwable | Buff |
| 15 | Lucky Berry | Edible | Buff |

Demolition therefore has **4** entries (Ubi Server / Toxic Ubi Server / Melon Grenade / Melon Slice Bomb), and the start-up self-check prints `15 crops: throwable 8 / deployable 5 / edible 2; demolition 4 → OK` (PLAN R-51). A change to this table changes behaviour without touching a single explosion call site; FIX-7's acceptance test showed exactly that (Melon Slice Bomb with the tag: 26 blocks destroyed, without it: 0, with the same six call sites and the same `BlastRule` bytes).

---

## 5. Server-Authoritative Architecture

### 5.1 General principles

Every judgement in this mod that "changes world state" is made on the server; the client is responsible for **presentation** only. This is not only a security requirement but also a compatibility requirement: DEC D-23 states that "the client only needs the resource pack", which means a dedicated server can run without anything beyond the client logic of `farmstorm` (the client still needs the mod itself to render blocks / items / entities, but it **does not need** any authoritative logic).

Three hard rules:

1. **No custom C2S packet** (0 of them). Every client → server intention goes through vanilla packets: `ServerboundUseItemPacket`, `ServerboundUseItemOnPacket`, `ServerboundContainerButtonClickPacket`, `ServerboundPlayerActionPacket`.
2. **The client sends no numeric value.** The experience cost of the converter, the damage of the turret and the power of the explosion are all read by the server from the config or the recipe.
3. **Ownership (owner) can only come from a server-side context**: `BlockPlaceContext#getPlayer()` / `Projectile#getOwner()`, never from an item component or a packet field.

### 5.2 List of logic that must run on the server

| Logic | Location | Consequence if moved to the client |
|---|---|---|
| Writing the ownership of a deployed object | `DeployedCropBlock#setPlacedBy` → `BE.setOwner` | the client could forge the owner and turn an enemy deployed object into an own-side one |
| Trap trigger decision | `TrapBehavior#findTrigger` | the client would decide who steps on the trap and could farm explosions without limit |
| Turret targeting and firing | `TurretBehavior#findTarget/fire` | the client could make the turret hit only enemies and never itself |
| Aura effect application / temporary enchantment | `AuraProvider#apply` / `TempEnchantHandler` | the client could buff the whole team |
| Three-phase explosion | `FarmstormExplosion#finalizeExplosion` | the client could bypass the damage cap and the resistance threshold |
| Projectile impact effect | `ThrownCropEntity#onImpact` | the client would decide the position and the number of explosions |
| Conversion transaction | `ConverterBlockEntity#tryConvert` | the client could convert for free and dupe items |
| Lucky Berry roll | `LuckyBerryRoller#roll` | the client could pick the outcome itself (straight to creative mode) |
| Creative-mode rollback timer | the `LuckyBerryRoller` memory table + player tick | the client could stay in creative forever |

### 5.3 Data synchronization

| Object | Mechanism | Synced fields | Frequency |
|---|---|---|---|
| `DeployedCropBlockEntity` | `getUpdateTag` (full state on chunk load) + `getUpdatePacket` → `ClientboundBlockEntityDataPacket.create(this)` (targeted delta) | `ownerUuid` / `ownerName` / `placedGameTime` / `detached` / `pendingWaves` / `fuseTicks` | once on placement; `setChanged()` + `level.sendBlockUpdated` on state change |
| `ConverterBlockEntity` | container slots are synced by the `ContainerSynchronizer` built into `AbstractContainerMenu`; extra state uses `ContainerData` (`xpCost` / `canConvert` / `reverse`) | the item in slot 0, 3 ints | on every `slotsChanged` |
| `ThrownCropEntity` | the entity spawn packet is broadcast automatically by `ServerLevel#addFreshEntity`; `submunition` uses `SynchedEntityData` (`defineSynchedData`) | item rendering, `submunition`, `fuseTicks` (if needed) | on spawn + on change |
| `TurretBullet` | same as above; should the tracked target use `SynchedEntityData<Optional<Entity>>`? → **no**: the target is needed by the server only, and the client can interpolate from velocity | item / velocity only | on spawn |
| temporary enchantment | `DataComponent` travels with the `ItemStack` (`ClientboundContainerSetSlotPacket` / `ClientboundSetEquipmentPacket`) | the whole `List<TempEnchantRecord>` | when the item changes |
| crop growth | `BlockState` (`age`) → the regular block update path | `age` | random tick |

**Team information takes no part in synchronization**: the team of the owner is **always** resolved live by the server at the trigger instant (C-9) — `getUpdateTag` carries no team field and should not. Even if the client knows who a block belongs to, it cannot determine who may trigger it, and this is part of the privilege protection (§8.4).

**Sync whitelist principle**: `getUpdateTag` writes only what the client truly needs. The client does not actually need `pendingWaves` (the fangs are server-spawned entities); the only reason to write it into `getUpdateTag` is to let the client `getDestroyProgress` and the tooltip show "already triggered" — if measured bandwidth becomes a problem, this is the first thing to cut.

### 5.4 Boundaries of client prediction

| Behavior | Client can predict | Note |
|---|---|---|
| mining progress bar | yes | `getDestroyProgress` is a pure function (`1/200` or `0`), identical on both sides, no snap-back |
| block placement preview (translucent block ghost) | yes | computed on the client from `getStateForPlacement` as well |
| ability cooldown UI | yes | `ItemCooldowns` is pushed to the client by a cooldown packet |
| projectile flight path | yes (visual) | the client interpolates from velocity; the impact point is decided on the server |
| block destruction of the explosion | no | the server destroys first and then pushes it through block updates |
| damage numbers / knockback of the explosion | no | `ClientboundSetHealthPacket` / `ClientboundSetEntityMotionPacket` |
| whether a trap triggers | no | the client does not know the owner (unless the BE has synced) and should not |
| whether a conversion succeeds | no | after the button click it waits for the `ContainerSetSlot` response from the server |

**Known and acceptable inconsistency**: when a non-own-side player presses the left button on Torchflower / Pitcher Plant, the client may compute "there is progress" because it cannot obtain the owner (if the BE has not synced yet), and after the server rejects it the client is reset by `ClientboundBlockUpdatePacket`. To eliminate this one-frame jitter, the client calls the same `isAlly` logic after the `BE` has synced (`DeployedCropBlock#getDestroyProgress` reads the BE straight from the `BlockGetter`), and in practice no visible snap-back should be observed.

### 5.5 Why 0 custom packets is a hard requirement

| Option | Conclusion |
|---|---|
| custom C2S "request conversion" packet | not needed — `ServerboundContainerButtonClickPacket` is already validated against the open menu in `AbstractContainerMenu#clickMenuButton` |
| custom C2S "deploy request" packet | not needed — `ServerboundUseItemOnPacket` goes through vanilla `BlockItem#useOn` |
| custom S2C "turret fire VFX" packet | not needed — `ServerLevel#sendParticles` + `level.playSound` already suffice |
| custom S2C "Lucky Berry result" packet | not needed — the result itself shows up through world changes and chat messages |

The cost is that purely decorative requests such as "the client can show a countdown ring on the turret" cannot be done; v0.0.1 does not do it and lists it as a deferred item.

---

## 6. Resource and Data File Inventory

### 6.1 Summary

| Category | Count |
|---|---|
| `assets/farmstorm/blockstates/*.json` | 20 |
| `assets/farmstorm/models/block/*.json` | 62 |
| `assets/farmstorm/models/item/*.json` | 22 |
| `assets/farmstorm/lang/*.json` | 2 |
| `assets/farmstorm/textures/item/*.png` | 22 |
| `assets/farmstorm/textures/block/*.png` | 68 |
| `assets/farmstorm/textures/gui/*.png` | 1 |
| `data/farmstorm/damage_type/*.json` | 2 |
| `data/minecraft/tags/damage_type/*.json` | 2 |
| `data/farmstorm/recipe/*.json` | 21 |
| `data/farmstorm/loot_table/blocks/*.json` | 20 |
| `data/farmstorm/advancement/*.json` | 12 |
| `data/farmstorm/tags/block/*.json` | 2 |
| `data/farmstorm/tags/item/*.json` | 2 |
| `data/minecraft/tags/block/mineable/pickaxe.json` | 1 |
| root: `pack.mcmeta` / `META-INF/neoforge.mods.toml` | 2 |
| **Total** | **261** (JSON 169 + TOML 1 + PNG 91) |

### 6.2 assets/farmstorm/blockstates (20)

```
blockstates/puffed_wheat_crop.json               crop (age 0-3 → 4 variants)
blockstates/blast_carrot_crop.json
blockstates/ubi_server_crop.json
blockstates/toxic_ubi_server_crop.json
blockstates/power_beet_crop.json
blockstates/melon_grenade_stem.json
blockstates/pumpkin_turret_stem.json
blockstates/awakened_torchflower_crop.json
blockstates/awakened_pitcher_plant_crop.json
blockstates/fang_trap_cactus_block.json
blockstates/bamboo_firecracker_stalk.json
blockstates/red_mushroom_puff_block.json
blockstates/brown_mushroom_puff_block.json
blockstates/lucky_berry_bush.json
blockstates/blast_carrot.json                     deployed (face × facing × floating)
blockstates/pumpkin_turret.json
blockstates/awakened_torchflower.json
blockstates/awakened_pitcher_plant.json
blockstates/fang_trap_cactus.json
blockstates/converter.json                        converter (4 facings)
```

### 6.3 assets/farmstorm/models/block (62)

- 56 crop stage models: `models/block/<base>_stage0.json` … `_stage3.json`, parent model `minecraft:block/crop`. `<base>` is derived by removing the trailing `_crop` / `_stem` / `_stalk` / `_block` / `_bush` from the crop block ID (for example `puffed_wheat_crop` → `puffed_wheat`, `lucky_berry_bush` → `lucky_berry`, `melon_grenade_stem` → `melon_grenade`).
- 5 deployed models: `models/block/blast_carrot.json` / `pumpkin_turret.json` / `awakened_torchflower.json` / `awakened_pitcher_plant.json` / `fang_trap_cactus.json`. The floating state **reuses the same model** and only changes the `y` rotation and `uvlock` in the blockstate.
- 1 converter: `models/block/converter.json` (`cube_bottom_top` + a separate front texture).

### 6.4 assets/farmstorm/models/item (22)

```
models/item/puffed_wheat.json            } 15 crop item models
models/item/blast_carrot.json            }
models/item/ubi_server.json              }
models/item/toxic_ubi_server.json        }
models/item/power_beet.json              }
models/item/melon_slice_bomb.json        }
models/item/melon_grenade.json           }
models/item/pumpkin_turret.json          }
models/item/awakened_torchflower.json    }
models/item/awakened_pitcher_plant.json  }
models/item/fang_trap_cactus.json        }
models/item/bamboo_firecracker.json      }
models/item/red_mushroom_puff.json       }
models/item/brown_mushroom_puff.json     }
models/item/lucky_berry.json             }
models/item/puffed_wheat_seeds.json      } 6 seeding items
models/item/power_beet_seeds.json        }
models/item/melon_grenade_seeds.json     }
models/item/pumpkin_turret_seeds.json    }
models/item/awakened_torchflower_seeds.json
models/item/awakened_pitcher_plant_pod.json
models/item/converter.json               converter (parent = models/block/converter.json)
```

The item models of the 5 deployable crops **reference the block model directly** (`{"parent": "farmstorm:block/blast_carrot"}`); no extra artwork is drawn.

### 6.5 assets/farmstorm/textures (91 PNG)

| Path | Count | Use |
|---|---|---|
| `textures/item/<22 ids>.png` | 22 | item icons; the 15 crop icons must carry a **green + in the bottom-right corner** (plan item 8) |
| `textures/block/<base>_stage{0..3}.png` | 56 | crop textures for the 4 stages (14 crops × 4; the `<base>` rule is in §6.3) |
| `textures/block/blast_carrot.png` and the other deployed block textures (5 in total) | 5 | appearance of the deployed objects |
| `textures/block/converter_top.png` / `converter_side.png` / `converter_front.png` | 3 | kept as-is; the last two are no longer referenced by the block model |
| `textures/block/converter_side_{wheat,carrot,potato,beetroot}.png` | 4 | working-table side as the base + the matching crop icon; these are the four side faces of the converter model |
| `textures/gui/converter.png` | 1 | 176×166 GUI background |

> The converter block model gives each of the four sides its own texture: north = wheat / east = carrot / south = potato / west = beetroot, and both up and down use `converter_top`.

> All PNGs are batch-generated as placeholders by `tools/gen_textures.py` and replaced by the author after review (DEC D-25).

### 6.6 assets/farmstorm/lang (2)

`lang/zh_cn.json`, `lang/en_us.json` (identical key sets, see §7).

### 6.7 data (62 JSON files)

**Damage types (2)**

| Path | Content |
|---|---|
| `data/farmstorm/damage_type/void_bolt.json` | `{"exhaustion":0.0,"message_id":"farmstorm.void_bolt","scaling":"when_caused_by_living_non_player","effects":"hurt"}` |
| `data/farmstorm/damage_type/crop_blast.json` | `{"exhaustion":0.1,"message_id":"farmstorm.crop_blast","scaling":"when_caused_by_living_non_player","effects":"hurt"}` |

**Merged into minecraft tags (2)**

| Path | Content |
|---|---|
| `data/minecraft/tags/damage_type/is_projectile.json` | `{"values":["farmstorm:void_bolt"]}` |
| `data/minecraft/tags/damage_type/is_explosion.json` | `{"values":["farmstorm:crop_blast"]}` |

**Block tags (3)**

| Path | Content |
|---|---|
| `data/farmstorm/tags/block/deployed_crops.json` | the 5 deployed blocks |
| `data/farmstorm/tags/block/explosion_immune.json` | `awakened_torchflower` / `awakened_pitcher_plant` |
| `data/minecraft/tags/block/mineable/pickaxe.json` | `converter` + the 5 deployed blocks (deployed objects can be mined by tools other than pickaxe-speed ones, but the mining speed is fixed, see §4.4) |

**Item tags (2)**

| Path | Content |
|---|---|
| `data/farmstorm/tags/item/modified_crops.json` | the 15 crop items (reused by the Lucky Berry random pool and by the recipes) |
| `data/farmstorm/tags/item/seeds.json` | the 6 seeding items |

**Conversion recipes (21; each with `bidirectional:true` covers both directions)**

| # | File | input → result |
|---|---|---|
| 1 | `recipe/puffed_wheat.json` | `minecraft:wheat` → `farmstorm:puffed_wheat` |
| 2 | `recipe/blast_carrot.json` | `minecraft:carrot` → `farmstorm:blast_carrot` |
| 3 | `recipe/ubi_server.json` | `minecraft:potato` → `farmstorm:ubi_server` |
| 4 | `recipe/toxic_ubi_server.json` | `minecraft:poisonous_potato` → `farmstorm:toxic_ubi_server` |
| 5 | `recipe/power_beet.json` | `minecraft:beetroot` → `farmstorm:power_beet` |
| 6 | `recipe/melon_slice_bomb.json` | `minecraft:melon_slice` → `farmstorm:melon_slice_bomb` |
| 7 | `recipe/melon_grenade.json` | `minecraft:melon` → `farmstorm:melon_grenade` |
| 8 | `recipe/pumpkin_turret.json` | `minecraft:pumpkin` → `farmstorm:pumpkin_turret` |
| 9 | `recipe/awakened_torchflower.json` | `minecraft:torchflower` → `farmstorm:awakened_torchflower` (+ nether star, **one way**) |
| 10 | `recipe/awakened_pitcher_plant.json` | `minecraft:pitcher_plant` → `farmstorm:awakened_pitcher_plant` (+ nether star, **one way**) |
| 11 | `recipe/fang_trap_cactus.json` | `minecraft:cactus` → `farmstorm:fang_trap_cactus` |
| 12 | `recipe/bamboo_firecracker.json` | `minecraft:bamboo` → `farmstorm:bamboo_firecracker` |
| 13 | `recipe/red_mushroom_puff.json` | `minecraft:red_mushroom` → `farmstorm:red_mushroom_puff` |
| 14 | `recipe/brown_mushroom_puff.json` | `minecraft:brown_mushroom` → `farmstorm:brown_mushroom_puff` |
| 15 | `recipe/lucky_berry.json` | `minecraft:sweet_berries` → `farmstorm:lucky_berry` |
| 16 | `recipe/puffed_wheat_seeds.json` | `minecraft:wheat_seeds` → `farmstorm:puffed_wheat_seeds` |
| 17 | `recipe/power_beet_seeds.json` | `minecraft:beetroot_seeds` → `farmstorm:power_beet_seeds` |
| 18 | `recipe/melon_grenade_seeds.json` | `minecraft:melon_seeds` → `farmstorm:melon_grenade_seeds` |
| 19 | `recipe/pumpkin_turret_seeds.json` | `minecraft:pumpkin_seeds` → `farmstorm:pumpkin_turret_seeds` |
| 20 | `recipe/awakened_torchflower_seeds.json` | `minecraft:torchflower_seeds` → `farmstorm:awakened_torchflower_seeds` |
| 21 | `recipe/awakened_pitcher_plant_pod.json` | `minecraft:pitcher_pod` → `farmstorm:awakened_pitcher_plant_pod` |

**Loot tables (20, `data/farmstorm/loot_table/blocks/*.json`)**

- 14 crop tables: at maturity (`age=3`) they drop the crop + the seeding item **at the vanilla ratio of that crop**, and while immature they drop only 1 seeding item.
  - Puffed Wheat: 1 crop + 0–3 seeds (Fortune bonus); Blast Carrot / Ubi Server / Toxic Ubi: 1–4 crops; Power Beet: 1 crop + 0–1 seeds; Melon Grenade: 1–4 crops; Pumpkin Turret: 1 crop; Torchflower / Pitcher Plant: 1 crop; cactus: 1–3 crops; Bamboo Firecracker: 1 crop; red / brown mushroom: 1–2 crops; Lucky Berry: 1–3 crops.
- 5 deployed block tables: by default they drop themselves (1 item), and **drop 0 when destroyed by an explosion** (using the `survives_explosion` condition, as in vanilla); Torchflower / Pitcher Plant never enter the explosion path (`explosion_immune`).
- 1 converter table: drops itself + the items inside (`copy_components`, or `dropContents` in `onRemove`, either one — this specification picks the latter, so the table drops the block itself only).

**Advancements (12, DEC C-7; ID / parent / trigger condition aligned item by item with `01-DESIGN.md` §6)**

| # | File | Display name | Parent | Trigger condition |
|---|---|---|---|---|
| 1 | `advancement/root.json` | Farmstorm | — (root) | obtain any modified crop item (`#farmstorm:modified_crops`) |
| 2 | `advancement/converter.json` | Install a Converter | `farmstorm:root` | place a crop converter |
| 3 | `advancement/first_conversion.json` | Two-Way Street | `farmstorm:converter` | complete one conversion inside the converter (either direction) |
| 4 | `advancement/nether_star_toll.json` | Price of a Star | `farmstorm:first_conversion` | complete one "vanilla → awakened" torchflower / pitcher plant conversion (consuming a nether star) |
| 5 | `advancement/seed_cycle.json` | Closed Self-Supply | `farmstorm:first_conversion` | harvest one mature modified crop and pick up both the crop and the seeding item |
| 6 | `advancement/first_throw.json` | Opening Move | `farmstorm:root` | throw any throwable modified crop |
| 7 | `advancement/first_deploy.json` | Placing a Stone | `farmstorm:root` | deploy any deployable modified crop |
| 8 | `advancement/demolition.json` | Unauthorized Wall Removal | `farmstorm:first_throw` | destroy at least 1 block with the explosion of a **Demolition**-category crop |
| 9 | `advancement/turret_hit.json` | Do Not Stand There | `farmstorm:first_deploy` | be hit once by a Pumpkin Turret missile |
| 10 | `advancement/aura_bath.json` | Aura Bath | `farmstorm:first_deploy` | receive an aura effect at least once from an Awakened Torchflower / Pitcher Plant |
| 11 | `advancement/fang_dance.json` | The Spikiest Episode | `farmstorm:first_deploy` | trigger Fang Trap Cactus once |
| 12 | `advancement/lucky_jackpot.json` | Playing It Safe Is Boring, Right | `farmstorm:root` | eat a Lucky Berry and roll the 0.1% creative-mode branch |

> The Demolition category has **4** entries: Ubi Server / Toxic Ubi Server / Melon Grenade / Melon Slice Bomb (the last one added by D-42).

Conventions (copied from `01-DESIGN.md` §6): all of them use `"frame": "task"`, `"show_toast": true`, `"announce_to_chat": false`; the `background` of `root` is `minecraft:textures/block/hay_block_side.png`.

**Implementation supplement (technical side)**: the advancement tree has two levels (`root` → 6 children; `converter → first_conversion → nether_star_toll / seed_cycle`, `first_throw → demolition`, `first_deploy → turret_hit / aura_bath / fang_dance`). Of these, #1 / #2 / #3 / #5 / #6 / #7 can be expressed directly with vanilla `criteria` (`inventory_changed` / `placed_block` / an `impossible` + `recipe` combination); #4 / #8 / #9 / #10 / #11 have no corresponding vanilla statistic, so they uniformly use the `minecraft:impossible` trigger + a server-side grant through `ServerPlayer#getAdvancements().award(...)` — **the decision stays on the server** (DESIGN §6 convention), and no command is added (D-24).

---

## 7. Language File Key Naming Convention

### 7.1 Rules

| Prefix | Format | Count | Note |
|---|---|---|---|
| `item.farmstorm.<id>` | all-lowercase snake case, identical to the registration ID | 22 | when one is missing, the raw key is displayed as `item.farmstorm.puffed_wheat.name` |
| `block.farmstorm.<id>` | same as above | 20 | the display name of a crop block takes the crop name (Puffed Wheat rather than Puffed Wheat Crop) |
| `entity.farmstorm.<id>` | same as above | 9 | the projectile names appear only in debug output and shulker-style fall messages |
| `itemGroup.farmstorm.main` | fixed | 1 | creative tab title |
| `container.farmstorm.converter` | fixed | 1 | GUI title |
| `gui.farmstorm.converter.*` | `.convert` / `.xp_cost` / `.no_recipe` / `.need_catalyst` / `.not_enough_xp` / `.reverse` / `.forward` / `.tooltip` | 8 | button and status texts |
| `tooltip.farmstorm.*` | `.deployed_by` / `.attached_face` / `.floating` / `.team` / `.turret_range` / `.aura_radius` / `.temporary_enchant` / `.cannot_break` / `.trap_armed` / `.category.{trap,turret,demolition,buff,construction,damage}` / `.category_separator` | 16 | additional information of deployed objects and items; the category label is a multi-valued set, so each category has its own key, and the separator can only come from the language file (D-48) |
| `death.attack.farmstorm.<message_id>` | matches the `message_id` of the `damage_type` | 4 | `farmstorm.void_bolt` and `farmstorm.crop_blast`, each with a `.player` variant |
| `advancements.farmstorm.<path>.title` / `.description` | 12 advancements × 2 | 24 | generated by DataGen |
| `farmstorm.configuration.<group>.<key>` | exactly identical to the TOML key path | 117 | specified via `ModConfigSpec.Builder#translation` |
| **Total** | | **222** | one copy for Chinese and one for English; the key sets must be exactly identical |

### 7.2 Example entries (zh_cn.json)

```json
{
  "item.farmstorm.puffed_wheat": "膨胀麦",
  "item.farmstorm.melon_grenade": "瓜！",
  "item.farmstorm.awakened_pitcher_plant_pod": "激活的瓶子草豆荚",
  "item.farmstorm.lucky_berry": "幸运浆果",

  "block.farmstorm.converter": "农产品转换器",
  "block.farmstorm.pumpkin_turret": "man瓜",
  "block.farmstorm.puffed_wheat_crop": "膨胀麦",

  "entity.farmstorm.turret_bullet": "炮台导弹",
  "entity.farmstorm.melon_grenade": "瓜！",

  "itemGroup.farmstorm.main": "农场风云",
  "container.farmstorm.converter": "农产品转换器",

  "gui.farmstorm.converter.convert": "转换",
  "gui.farmstorm.converter.xp_cost": "消耗经验：%s",
  "gui.farmstorm.converter.no_recipe": "无法转换",
  "gui.farmstorm.converter.need_catalyst": "缺少催化剂：%s",
  "gui.farmstorm.converter.not_enough_xp": "经验不足",
  "gui.farmstorm.converter.forward": "原版 → 改良",
  "gui.farmstorm.converter.reverse": "改良 → 原版",
  "gui.farmstorm.converter.tooltip": "放入原版或改良作物进行 1:1 转换",

  "tooltip.farmstorm.deployed_by": "部署者：%s",
  "tooltip.farmstorm.attached_face": "附着面：%s",
  "tooltip.farmstorm.floating": "悬空",
  "tooltip.farmstorm.team": "队伍：%s",
  "tooltip.farmstorm.turret_range": "索敌范围：%s 格（切比雪夫）",
  "tooltip.farmstorm.aura_radius": "光环半径：%s 格",
  "tooltip.farmstorm.temporary_enchant": "临时附魔（%s 秒后消失）",
  "tooltip.farmstorm.cannot_break": "非己方玩家无法破坏",
  "tooltip.farmstorm.trap_armed": "已布置",
  "tooltip.farmstorm.category.trap": "陷阱类",
  "tooltip.farmstorm.category.turret": "炮台类",
  "tooltip.farmstorm.category.demolition": "破障类",
  "tooltip.farmstorm.category.buff": "buff类",
  "tooltip.farmstorm.category.construction": "建造类",
  "tooltip.farmstorm.category.damage": "伤害类",
  "tooltip.farmstorm.category_separator": "、",

  "death.attack.farmstorm.void_bolt": "%1$s 被虚空撕碎",
  "death.attack.farmstorm.void_bolt.player": "%1$s 被 %2$s 的 man瓜 击落",
  "death.attack.farmstorm.crop_blast": "%1$s 被作物炸飞了",
  "death.attack.farmstorm.crop_blast.player": "%1$s 被 %2$s 的作物炸飞了",

  "farmstorm.configuration.deploy.turretMiningTicks": "炮台硬挖时长（tick）",
  "farmstorm.configuration.blast_carrot.maxDamage": "胡罗贝爆炸的伤害上限"
}
```

### 7.3 Detailed rules

1. **The Chinese name is copied verbatim from `plan0.0.1.txt`**, including punctuation and full-width characters such as "？！菇菇！？"; the English name is a semantic naming (DEC D-26).
2. Crop blocks and crop items **share the same name** (blocks and items belong to different registries, and their lang keys have different prefixes, so there is no conflict).
3. Death messages use the positional placeholders `%1$s` / `%2$s` (the `.player` variant is selected automatically by the vanilla damage system).
4. Config keys always go through `farmstorm.configuration.*` and map segment by segment onto the TOML path (`deploy.turretMiningTicks` → `farmstorm.configuration.deploy.turretMiningTicks`), so the automatic GUI of `ModConfigSpec` needs no extra mapping.
5. The language files are generated by `ModLangProvider`; hand-editing `src/generated` is forbidden (it would be overwritten by `runData`).

---

## 8. Performance and Security

### 8.1 Controlling the cost of per-tick scanning

| Measure | Concrete practice | Magnitude |
|---|---|---|
| scan interval | traps / turrets once every 10 ticks, auras once every 100 ticks | steady-state cost ÷ 10 or ÷ 100 |
| staggering | `stagger = floorMod(pos.hashCode(), interval)` | N deployed objects in the same chunk spread over `interval` different ticks |
| player list first | auras use `ServerLevel#players()` (single digits) instead of `getEntitiesOfClass` (possibly dozens) | about an order of magnitude cheaper at a radius of 20 |
| AABB narrowing | the turret targeting AABB is `AABB.ofSize(center, 2r+1, 2r+1, 2r+1)`, r ≤ 5 → 11×11×11 | the voxel clipping of vanilla `getEntitiesOfClass` is sufficient |
| early exit | own-side players take no part in targeting → check `isAlly` before computing distance (`isAlly` is an O(1) string comparison) | removes the vast majority of distance computations |
| crops without a BlockEntity | the 14 crop blocks have no BE and grow through `randomTick` | 0 steady-state cost |
| server-only | `getTicker` returns `level.isClientSide ? null : DeployedCropBlockEntity::serverTick` | 0 cost on the client |
| early return during cooldown | `if (cooldown > 0) { cooldown--; return; }` placed before all scanning | with a 60 tick firing interval a turret performs target acquisition only once per 60 ticks |

**Worst-case estimate**: 8 deployed objects per chunk at most × once per 10 ticks × on average 3 candidate entities inside the targeting AABB ≈ 2.4 distance computations per tick — negligible. What actually needs watching is **aura stacking**: when 20 Torchflowers cover the same player area, each player receives 20 effect applications every 100 ticks (20 `addEffect` calls + 20 particle broadcasts). On a 100+ player server this produces a stagger effect, so the internal constant `MAX_EFFECTS_PER_TICK` (64) acts as a circuit breaker: once exceeded, the remaining purges / applications of this tick are dropped and resumed on the next tick.

### 8.2 Explosion batching

| Problem | Solution |
|---|---|
| the 28 wind charge explosions of the Bamboo Firecracker burst at once | one every 5 ticks flattens them naturally; no extra throttling is needed |
| several projectiles hit in the same tick | internal constant `MAX_EXPLOSIONS_PER_TICK = 8`: the excess `FarmstormExplosion` instances enter a `Deque` and run one by one on `LevelTickEvent.Post` (delay ≤ 1 tick, no perceptible difference) |
| Puffed Wheat replaces 125 blocks | hard cap of 5×5×5 = 125 per use; stack size 16 → at most 2000 `setBlock` calls. Uses `level.setBlock(pos, state, Block.UPDATE_CLIENTS)` and skips blocks already in the same state, avoiding pointless updates |
| too many dropped entities from explosions | `BlastRule.dropsBlocks` works together with `survives_explosion`; crop blocks (of no drop value) use `dropsBlocks = false` |
| beam / particle storm | each explosion plays `Level#explode`-style particles only once; aura particles are capped by `particleCount` |

### 8.3 Anti-farming

| Attack surface | Defense line |
|---|---|
| place / break cycles to farm deployed objects | internal constants `DEPLOY_COOLDOWN_TICKS = 5` + `MAX_DEPLOYED_PER_CHUNK = 8` + `MAX_DEPLOYED_PER_PLAYER = 32`; the cooldown resets only after removal, preventing a "place → break immediately → place again" loop from repeatedly triggering drops |
| farming time through the 10-second turret forced mining | the forced mining progress cannot be accelerated (fixed `1F/200F`) and does not change with tool enchantments |
| item duping in the converter | **two slots (input + catalyst)** + transactional validation (§4.8.2); the input slot accepts no container (`Slot#mayPlace` accepts only items matching a recipe); `quickMoveStack` performs no bulk conversion |
| the converter swallowing items | `onRemove` → `Containers.dropContents`; nothing drops when the block is moved by a piston (the `movedByPiston` branch) |
| farming the Lucky Berry RNG | the roll happens only inside `finishUsingItem` on the server; one item can be eaten once (`stack.shrink(1)` is handled by the vanilla food flow) |
| making creative mode permanent | the 90-second forced restore of D-18 + restore on logout + reconciliation on login (§4.12) |
| farming entities with projectiles | `getUseDuration` + a 5 tick cooldown; projectile entities use `clientTrackingRange(4)` and are not broadcast server-wide |
| farming loot through chest generation | `chestCount = 2` is fixed; `setLootTable` is used instead of pre-generating contents, so a single roll cannot fill the whole inventory |

### 8.4 Privilege protection

| Risk | Protection |
|---|---|
| the client decides damage values | every `hurt()` call is on a server-side path; the client has no way to construct a `FarmstormExplosion` |
| the client forges the owner | the owner is read only from `BlockPlaceContext#getPlayer()` / `Projectile#getOwner()`; `getUpdateTag` only pushes data down and never accepts a write |
| the client forges the conversion cost | `clickMenuButton(id)` has exactly one legal value, 0; the cost is computed by the server from the recipe and the config |
| the client skips the 10-second forced mining | mining progress is accumulated independently by `ServerPlayerGameMode`; `playerWillDestroy` performs a second own-side check |
| the client sends a fake `pendingWaves` | BE data is written by the server only; `loadAdditional` is called only during deserialization |
| the client triggers a trap | the trigger condition lives in `TrapBehavior#findTrigger` (server tick) and cannot be called by the client |
| OP abuse | a deployed object created by `/setblock` has no owner → it is "non-own-side" for everyone and cannot be used as an own-side turret; this degradation is deliberate |

### 8.5 World protection and compatibility

1. **The T-05 floor**: every block replacement / destruction skips blocks with blast resistance ≥ 3.6e6 (bedrock, barrier, command block, end portal frame, structure block).
2. **`#farmstorm:explosion_immune`**: Torchflower / Pitcher Plant are in the tag, and it **blocks more than its own explosions** — in `ExplosionEvent.Detonate` these blocks are removed from `getAffectedBlocks()`, so vanilla TNT and explosions from other mods cannot destroy them either (DEC D-14, "no explosion can destroy it").
3. **Claim compatibility**: v0.0.1 does not integrate claim plugins; the destruction of deployed objects is constrained by `maxDeployedPerChunk`, preventing a single chunk from being filled with turrets as a lag machine. Claim integration is a deferred item.
4. **BlockEntity NBT size**: `getUpdateTag` writes only 5 fields (2 longs of the UUID + 2 strings + 1 boolean), so a single packet is < 200 bytes.
5. **Entity count**: one deployed object has at most 1 bullet in flight at any moment (it is 1 when the cooldown exceeds the flight time); the Bamboo Firecracker has at most 1 "fuse entity" alive at a time.

---

## 9. Known Technical Risks and Mitigations

Numbered `RT-xx` (Risk Technical). Each entry gives: trigger condition → impact → mitigation → verification method.

### RT-01 Patch-level API drift in NeoForge 21.1.x

- **Trigger**: `neo_version` is raised from 21.1.213 to a higher 21.1.x, or downgraded.
- **Impact**: less commonly used APIs such as `Explosion#finalizeExplosion`, `RecipeSerializer#streamCodec`, `FoodProperties.Builder` and `PotionContents` occasionally change signature between patch releases, producing compile errors.
- **Mitigation**: pin an exact version in `gradle.properties` and never write a dynamic version; upgrade in a separate PR that changes only the version number and then runs `build`; add a unit-level smoke test for the four APIs above (compiling counts as passing).
- **Verification**: `./gradlew build` succeeds twice in a row on the pinned version.

### RT-02 The 10-second forced mining disagrees with client / server prediction

- **Trigger**: the client and the server resolve the inputs of `getDestroyProgress` (`BlockGetter`, `BlockPos`) differently; or the BE has not synced to the client yet.
- **Impact**: the progress bar jumps back and forth and the player sees "mining at 99% and it suddenly snaps back"; players report bugs.
- **Mitigation**: `getDestroyProgress` must be a **pure function** — it does not read `level.getGameTime()` and does not read server-only fields; the owner check goes through the synced BE; for `BE == null` (client chunk just loaded) return "default mining speed" rather than 0.
- **Verification**: mine once each in creative / survival / spectator mode and record the screen; teleport away with `/tp` and return (triggering a chunk reload) and retest.

### RT-03 Temporary enchantment state leaks

- **Trigger**: a player is granted the enchantment inside the aura → throws the sword into lava / a chest / an ender chest → the temporary record disappears with the item or lingers.
- **Impact**: a lingering record produces "an inexplicable Fire Aspect", or the revoke lowers the real enchantments of the player.
- **Mitigation**: `grant` records the `previous` level; the revoke restores only when "current level == granted" (the no-overwrite check of §4.10); `expireAt` uses the absolute `gameTime`, so an item expires as soon as it is taken out of the chest; container scenarios are covered by `PlayerTickEvent`.
- **Verification**: manually test 4 scenarios (thrown into lava / put in a chest / dropped on death / straight logout); assert that the component is cleared from the item NBT.

### RT-04 The shared `DeployedCropBlockEntity` class is registered 5 times

- **Trigger**: the 5 `BlockEntityType` instances use the same class + the same `BlockEntityTicker`, and the generics require a cast at `getTicker`.
- **Impact**: `ClassCastException` surfaces only at runtime (compile-time erasure), and only when a particular block is ticked.
- **Mitigation**: in `getTicker` use an explicit branch `type == FarmstormBlockEntities.PUMPKIN_TURRET.get() ? (BlockEntityTicker<T>) DeployedCropBlockEntity::serverTick : null` instead of the generic magic of `createTickerHelper`; the 5 types share one factory lambda, but each `BlockEntityType.Builder#of` is given its own `Block` list.
- **Verification**: place 1 of each of the 5 deployed types, run 600 ticks and observe no exception; the `validate` phase of `runData` confirms the registry has no duplicate IDs.

### RT-05 The creative-mode rollback (D-18) fails on exceptional paths

- **Trigger**: server crash / forced player quit / `/stop` after rolling the 0.1%.
- **Impact**: a player may stay in creative mode for a long time (**the highest-severity gameplay exploit**).
- **Mitigation**: three-level fallback — ① the record lives in memory only (`Map<UUID, CreativeRestore>`) and is naturally cleared by a server restart; ② `PlayerLoggedOutEvent` restores immediately; ③ `PlayerLoggedInEvent` reconciles: if the current `GameType` of the player is `CREATIVE` and the game mode recorded in the save `playerdata` is not creative (compared against the initial value of `ServerPlayer#gameMode` plus the persisted field of `ServerPlayerGameMode`), restore to `SURVIVAL`. The third level carries the risk of "demoting a player on a creative server", so it **runs only for players where `level.getServer().getPlayerList().isOp(...)` is false**.
- **Verification**: simulate a crash (`kill -9` the server), reconnect and confirm the mode is correct; run one regression on a creative server (OP players must not be touched).

### RT-06 Performance and stacking of the 20-block aura

- **Trigger**: a large team stacks 10+ Torchflowers in the same base.
- **Impact**: every 100 ticks each player receives 6 effects × N auras = an effect refresh storm; particle broadcasts inflate bandwidth.
- **Mitigation**: the `MAX_EFFECTS_PER_TICK` circuit breaker; `AuraProvider` accepts only one application per player per tick (deduplicated with a `Set<UUID>`); particles are played only when "this application actually succeeded".
- **Verification**: in a scenario with 10 auras and 8 players, use `/debug profile` and observe a tick time < 1 ms.

### RT-07 Converter recipe cache versus `RecipeManager` reload

- **Trigger**: after `/reload` or a datapack update, `ConverterBlockEntity.cachedRecipe` still points at the old object.
- **Impact**: players can convert recipes that were deleted, or new recipes do not take effect.
- **Mitigation**: listen for `RecipesUpdatedEvent` (or after `AddReloadListenerEvent`) and iterate the loaded `ConverterBlockEntity` instances to clear the cache; also validate the cache once inside `tryConvert` with `level.getRecipeManager().byKey(cached.getId())` and re-query if it is invalid.
- **Verification**: after `/reload`, convert 5 times in a row and confirm the log has no `Recipe not found` exception.

### RT-08 Compatibility with explosions from other mods

- **Trigger**: vanilla TNT or another mod explosion hits Torchflower / Pitcher Plant.
- **Impact**: D-14 "no explosion can destroy it" stops holding.
- **Mitigation**: filter `#farmstorm:explosion_immune` in `ExplosionEvent.Detonate`; additionally give these two blocks a logic-level protection beyond `blastResistance` (`getExplosionResistance` returns `3600000F`), as a second safeguard.
- **Verification**: detonate TNT / a creeper / an end crystal once each and confirm the blocks are still there.

### RT-09 Module block ambiguity around the "cobblestone 6.0" threshold

- **Trigger**: another mod adds blocks with a resistance between 5.9 and 6.1.
- **Impact**: the destruction boundary of Melon Slice Bomb / Melon Grenade does not match player intuition.
- **Mitigation**: expose the threshold in the config (`explosion.sliceResistThreshold` / `explosion.grenadeResistThreshold`) and provide a `#farmstorm:blast_proof` block tag so modpack authors can exempt blocks explicitly.
- **Verification**: place one block with resistance 6.0 and one with 5.99 using `/setblock` and confirm the former survives and the latter breaks.

---

## 10. Version Verification

> **Note on the verification method (important)**: actual HTTP fetching from this machine to the internet was blocked (direct connections to `docs.neoforged.net`, `raw.githubusercontent.com` and `maven.neoforged.net` all timed out), so the conclusions below come from **official documentation URLs and Maven artifact paths that matched in search**, not from page bodies. For any entry marked with "medium" / "low" confidence, **what is actually pulled at build time prevails**; it must not be hardcoded on the basis of this document.

### 10.1 Item-by-item conclusions

| # | Item to verify | Verified conclusion | Source | Confidence |
|---|---|---|---|---|
| 1 | the concrete NeoForge version for MC 1.21.1 | the version line is **21.1.x**; artifacts that verifiably exist include **21.1.213** (the changelog artifact for this version is already in the Maven repository) and **21.1.209** (a third-party host reseller page states MC 1.21.1). **It cannot be confirmed that 21.1.213 is the current absolute latest**; this document takes 21.1.213 as a lower bound, and the build must use the largest 21.1.x in `maven-metadata.xml` | `https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.213/neoforge-21.1.213-changelog.txt`; `https://scalacube.com/store/minecraft/neoforge-21.1.209` | medium-high |
| 2 | the plugin id and version of ModDevGradle | the plugin id is **`net.neoforged.moddev`**; the Gradle Plugin Portal carries 2.0.x artifacts such as **2.0.140 / 2.0.148**, and this document takes **2.0.148** | `https://plugins.gradle.org/plugin/net.neoforged.moddev/2.0.148`; `https://plugins.gradle.org/plugin/net.neoforged.moddev.repositories/2.0.140` | high (id) / medium-high (version) |
| 3 | the `neoforge { }` configuration block and runs syntax of MDG | the extension block in the official docs is named **`neoForge { }` (case sensitive)** and contains `version`, an optional `parchment`, `runs { client() / server() / data() }` and `mods { <modid> { sourceSet sourceSets.main } }`. **The task document writes `neoforge { }`, which disagrees with the actual DSL**; the implementation must follow the hints of the plugin | `https://docs.neoforged.net/toolchain/docs/plugins/mdg/`; `https://raw.githubusercontent.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/main/build.gradle`; `https://github.com/neoforged/MDK/blob/archive/1.21-mdg/build.gradle` | high (block name) / medium (field details) |
| 4 | how `DeferredRegister` is used in 1.21.1 | the 1.21.1 documentation still has a Registries chapter, and `DeferredRegister` is the standard registration mechanism; besides `DeferredRegister.create(Registry, modid)` it also offers the convenience subclasses `DeferredRegister.Items` (`createItems` / `registerItem`), `DeferredRegister.Blocks` (`registerBlock`), `DeferredRegister.DataComponents` and so on; after registering on the mod event bus (`register(IEventBus)`) the entries are filled in uniformly by `RegisterEvent` | `https://docs.neoforged.net/docs/1.21.1/concepts/registries/`; `https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/registries/DeferredRegister.html`; `DeferredRegister.DataComponents.html` in the same directory | high |
| 5 | `DataComponents` (replacing item NBT since 1.20.5) | since **1.20.5** the custom data of an item stack is carried by **data components** instead of the former `ItemStack` NBT; a custom component requires registering a `DataComponentType` (`DataComponentType.Builder` + Codec/StreamCodec) and is read and written through `stack.set/get/remove`. 1.21.1 continues this model | `https://minecraft.wiki/w/Data_component_format`; `https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/registries/DeferredRegister.DataComponents.html` | high |
| 6 | the data-driven way to define a custom damage type in 1.21.1 | 1.21.1 has a standalone server resource document "Damage Types", with the path `data/<namespace>/damage_type/<id>.json` and the fields `exhaustion` / `message_id` / `scaling` / `effects`; to merge it into vanilla behavior it must be appended in `data/minecraft/tags/damage_type/*.json` (for example `is_explosion`, `is_projectile`) | `https://docs.neoforged.net/docs/1.21.1/resources/server/damagetypes/`; `https://minecraft.wiki/w/Damage_type` | high |
| 7 | how NeoForge custom configuration (`ModConfigSpec`) is registered in 1.21.1 | 1.21.1 has a "Configuration" document; build `SPEC` with `ModConfigSpec.Builder` and call `modContainer.registerConfig(ModConfig.Type.SERVER, SPEC)` in the mod constructor; value changes are observed through `ModConfigEvent.Loading` / `ModConfigEvent.Reloading` (used to rebuild the caches) | `https://docs.neoforged.net/docs/1.21.1/misc/config/` | high |

### 10.2 Items explicitly marked as "subject to what is actually pulled at build time"

| Item | Why it is uncertain | How it is handled |
|---|---|---|
| the concrete `neo_version` patch number | the largest 21.1.x currently on Maven cannot be confirmed; and the newest is not necessarily the most stable | after the first `./gradlew build`, write the version that was actually resolved back into `gradle.properties` and this table |
| the concrete ModDevGradle `2.0.x` | only 2.0.140/148 can be confirmed to exist; whether a higher one exists cannot be confirmed | the latest 2.0.x currently on the Plugin Portal prevails |
| the method names of `FoodProperties.Builder` | within 1.21.x there are differences in `saturation` / `saturationModifier` and in whether `alwaysEdible()` takes an argument | IDE completion prevails; it is caught at compile time |
| how `PotionContents` / `AreaEffectCloud` are constructed | the component-based refactoring since 1.21 | confirm at compile time; a fallback is already given (apply `MobEffects.POISON` directly) |
| whether `Explosion#finalizeExplosion` is still public | this method is the key hook of the whole approach | confirm with `javap` or the source attachment; if it is closed, the fallback is not to extend `Explosion` but to use an independent executor (destruction / damage / presentation written by hand in three phases), with an unchanged interface |
| the JSON shape of `Ingredient.CODEC` | the shorthand `{"item":...}` and `{"tag":...}` works in 1.21.1, but the details of the array / nested shapes were not checked word by word | regress with a recipe generated by `runData` (if it generates, it parses) |

### 10.3 Rejected claims (statements **not** accepted in this round)

- "The latest version of NeoForge 21.1.x is 21.1.72 / 21.1.100" and similar remembered numbers are not accepted — they appear in old tutorials and contradict the 21.1.209/21.1.213 found in this round of search.
- "`neoforge { }`" as the extension block name (the task document wording) is not accepted; the reason is in 10.1 #3.
- "After 1.20.5, `ItemStack` can still store and read custom data with NBT" is not accepted — data components have replaced that path.

---

## 11. Acceptance Self-Check

| Acceptance item | Result | Evidence |
|---|---|---|
| all 15 crops appear in the registration summary | pass | §3.1-A table, 15 rows, IDs identical to DEC §4 word for word: `puffed_wheat` / `blast_carrot` / `ubi_server` / `toxic_ubi_server` / `power_beet` / `melon_slice_bomb` / `melon_grenade` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus` / `bamboo_firecracker` / `red_mushroom_puff` / `brown_mushroom_puff` / `lucky_berry` |
| every registered object has an explicit ID | pass | §3.1–§3.10 list **64 registry objects** in total, each with `farmstorm:<id>`; §3.11 gives the per-registry counts |
| a "Version Verification" subsection exists at the end with conclusions stated item by item | pass | §10 (7 verified conclusions + 6 "subject to build time" items + 3 rejected claims) |
| only one file is created | pass | only `docs/zh/02-TECH-SPEC.md` is written; `plan0.0.1.txt` and `00-DECISIONS.md` are untouched |
| no complete implementation | pass | the document contains class skeletons and signatures only, with no complete business implementation |
| written in Chinese, code identifiers in English | pass | — |

### 11.1 Item-by-item cross-reference with DEC

| DEC | Where it is addressed here |
|---|---|
| D-06 `TeamResolver` | §4.2 |
| D-07 targets only the non-own-side (traps / turrets) | §4.6 behavior table + the mushroom note in §4.7 |
| D-08 closed sowing loop | T-01 + the loot tables in §6.7 |
| D-09 takes no part in vanilla uses | §4.3.1 (no vanilla recipe, satisfied inherently) |
| D-10 the planted type keeps an interface only | T-01 (14 crop blocks, no new gameplay) |
| D-11 + D-47 (D-47 supersedes the single-slot part): data-driven + bidirectional, three slots + button | §4.8.2 ~ §4.8.5 |
| D-12 3 experience points, nether star one way | §4.11 `[converter]` + the JSON example in §4.8.5 |
| D-13 turret forced mining 10 seconds | §4.4 break rules table (narrowed to `pumpkin_turret` per T-04) |
| D-14 Torchflower / Pitcher Plant rules | §4.4 + §8.5 + RT-08 |
| D-15 temporary enchantment | §4.10 + T-08 |
| D-16 stack size 16 / 64 | the Stack column of §3.1 |
| D-17 random pool of 14 kinds | the `SPAWN_CROPS` row of §4.12 |
| D-18 creative mode 90 seconds + cancel on logout | §4.12 + RT-05 |
| D-19 cobblestone 6.0 threshold | §4.9 `BlastRule.COBBLESTONE_6` + §4.11 |
| D-20 distance metrics | the §4.6 table (Chebyshev / Euclidean) + `DistanceMetrics` |
| D-21 values unchanged + moved into the config | §4.11 (117 keys; default values aligned item by item with `01-DESIGN.md` §3.8 and equal to the original plan values) |
| D-22 the config is server authoritative | §3.10 + §4.11 |
| D-23 supports dedicated servers | §5 + §1.4 (`runServer` gate) |
| D-24 creative tab + advancements | §3.6 + the 12 advancements in §6.7 (DEC C-7) |
| D-25 textures only as requirements + script | the counts in §6.5 |
| D-26 English semantic naming | §7.3 |
| D-27 Blast Carrot / red and brown mushrooms do not filter by side | §4.6 behavior table + the note in §4.7 |
| D-28 Pumpkin Turret firing interval 60 ticks | §4.11.2 `pumpkin_turret.fireIntervalTicks` |
| D-29 food values 4 / 0.3, 32 ticks | §4.3.4 + §4.11.1 `food.eatTicks` |
| D-30 fang radii 2.0 / 1.0 | §4.11.2 `fang_trap_cactus.outerRadius` / `.innerRadius` |
| D-31 the remaining completed items (cloud 3.0/600, lifetime 100, particles 8, `self`, drop self on losing support) | §4.11.1 + §4.11.2 + §4.11.6 |
| D-32 Toxic Ubi Server has an independent crop block and seeding item | §3.1-A #4 + §3.2 |
| D-33 Lucky Berry landing radius 2 blocks | §4.12 `SPAWN_CROPS` / `LOOT_CHESTS` |
| C-6 particles / sound go through vanilla packets, 0 custom S2C packets | §5.5 (no custom packet is added) |
| C-7 12 advancements / 24 lang keys | §6.1 + §6.7 + §7.1 |
| C-8 own side mines Torchflower / Pitcher Plant at normal vanilla speed | §0.1 T-04 + the §4.4 break rules table |
| C-9 ownership stores only `ownerUuid` + `ownerName`, team resolved live | §4.2 + §4.5 + §5.3 |

### 11.2 Interface constraints for the follow-up documents

- Every task in `03-TASKS.md` must map onto **one class or one group of registration entries** of this document; a change spanning 4 packages must be split into several tasks.
- The phase split of `04-ROADMAP.md` should proceed as "registration layer → blocks / entities → behavior → explosion / config → data and resources", because the registration layer is the prerequisite for everything that compiles afterwards.
- `01-DESIGN.md` §3.8 is the **single source of naming and values** for the configuration; §4.11 of this document only restates it from the implementation viewpoint and adds the "key → consumer" mapping. If either side changes a config key, the other side must be updated in step; otherwise it counts as a documentation defect.

