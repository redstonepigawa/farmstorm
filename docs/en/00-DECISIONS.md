# 00 · Decision Record

> This file is the single source of truth for "Farmstorm" v0.0.1. The design document, technical specification, task breakdown and prompts must all agree with this file; where they conflict, this file wins.
> Source: `plan.txt` (the author's original idea) + 5 rounds of clarification interviews (converged, frontier empty).

**Crop name mapping (Chinese → English)**

| Chinese | English |
|---|---|
| 膨胀麦 | Puffed Wheat |
| 胡罗贝 | Blast Carrot |
| 育碧服务器 | Ubi Server |
| 毒育碧服务器 | Toxic Ubi Server |
| 甜菜！ | Power Beet |
| 瓜片子 | Melon Slice Bomb |
| 瓜！ | Melon Grenade |
| man瓜 | Pumpkin Turret |
| 激活的火把花 | Awakened Torchflower |
| 激活的瓶子草 | Awakened Pitcher Plant |
| 赢过输过没死过 | Fang Trap Cactus |
| 爆竹 | Bamboo Firecracker |
| ？！菇菇！？ | Red Mushroom Puff |
| ？！棕色菇菇色棕！？ | Brown Mushroom Puff |
| 幸运浆果 | Lucky Berry |

The short forms 火把花 / 瓶子草 used in this document denote the awakened crops; vanilla crops are always marked "vanilla".

---

## 1. Project metadata

| Item | Value |
|---|---|
| Chinese name | Farmstorm (农场风云) |
| English name | Farmstorm |
| Version | 0.0.1 |
| modid | `farmstorm` |
| Java package | `com.dashpig.farmstorm` |
| Author | dashpig |
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.x (the latest stable version actually pulled at build time is authoritative) |
| Java | 21 |
| Build tool | ModDevGradle |
| Mappings | Mojang official mappings (official) |
| Runtime side | Server-authoritative; the client only needs the resource pack |

---

## 2. Deliverable structure

```
D:\project_farm\
├── plan.txt                  作者原始设想（只读，不修改）
├── README.md                 总索引（中英双语导航）
├── docs\zh\                 中文全套
│   ├── 00-DECISIONS.md       本文件
│   ├── 01-DESIGN.md          完整策划文档（15 作物全规格）
│   ├── 02-TECH-SPEC.md       技术架构与类设计
│   ├── 03-TASKS.md           任务拆分
│   ├── 04-ROADMAP.md         开发大纲
│   ├── 05-PROMPT-MAIN.md     主提示词
│   └── 06-PROMPT-TASKS.md    分阶段任务清单提示词
├── docs\en\                  英文全套（同结构）
└── tools\gen_textures.py     材质批量生成脚本
```

---

## 3. Decision list

### Delivery and scope

| ID | Decision |
|---|---|
| D-01 | This round produces only documents and prompts, no Java code. Code implementation is carried out by coding agents driven by the prompts. |
| D-02 | All deliverables are written to disk as Markdown, one set each in Chinese and English (`docs/zh`, `docs/en`), plus a `README.md` index. |
| D-03 | There are 2 prompts in total: `05-PROMPT-MAIN.md` (main prompt) + `06-PROMPT-TASKS.md` (staged task list). |
| D-04 | Target readers: CLI coding agents such as Claude Code / Codex that can read and write the repository and run Gradle. |
| D-05 | Both documents and prompts come as a full Chinese set + a full English set. |

### Gameplay rules

| ID | Decision |
|---|---|
| D-06 | Friendly determination: v0.0.1 uses vanilla scoreboard teams (Scoreboard Team); at the same time a `TeamResolver` abstraction is extracted so FTB Teams can be plugged in later. |
| D-07 | Affected targets: trap and turret categories target **non-friendly players** only and do not handle mobs. Sole exception: the Pumpkin Turret keeps interception of projectiles in flight. |
| D-08 | Every improved crop has a seed item, in one-to-one correspondence with the vanilla seed form; on harvest the crop and the seed item drop together at vanilla ratios, a self-sufficient loop. |
| D-09 | Improved crops **no longer have their vanilla uses** (cannot be made into bread, cannot feed animals, do not take part in vanilla recipes). |
| D-10 | "Plantable" adds no new design in v0.0.1, only the interface and the specification; all 15 crops are naturally plantable because all have a seed form. |
| D-11 | Converter: single-slot GUI + data-driven conversion recipes (custom RecipeType), bidirectional and reversible, 1:1. |
| D-12 | Conversion cost: each conversion deducts **3 experience points** (if insufficient the conversion is refused, levels are never deducted); Torchflower and Pitcher Plant **in the "vanilla → awakened" direction only** additionally consume 1 Nether Star each time, the reverse direction is free. |
| D-13 | Deployment block breaking: turret-category blocks are fixed at a hard-mine of **10 seconds**; other deployments use normal mining speed. |
| D-14 | Torchflower / Pitcher Plant: the friendly side can mine and recover them normally, no explosion can destroy them; non-friendly players cannot mine them at all. |
| D-15 | Fire Aspect: a **temporary enchantment** for the duration of the grant, removed automatically when leaving the area or on expiry, existing enchantments are not overwritten. |
| D-16 | Stacking: throwables **16**, everything else **64**. Each throw consumes 1. |
| D-17 | Lucky Berry random pool: the other **14 kinds** excluding itself (the entry count is actually 15). |
| D-18 | Lucky Berry creative mode: the original game mode is recorded and forcibly restored after 90 seconds; if the player logs out midway the effect is cancelled and not written to the save. |
| D-19 | The explosion resistance threshold of the Melon Slice Bomb and the Melon Grenade is unified as "cannot destroy blocks with explosion resistance ≥ cobblestone (6.0)". |
| D-20 | Distance metric: traps and turrets use **Chebyshev distance**; aura buffs use **Euclidean radius**. Sole exception: the Fang Trap Cactus trigger uses **Euclidean distance ≤ 3**. |
| D-21 | All numbers are copied verbatim from `plan.txt`; v0.0.1 does no balance adjustment, but every number must be exposed in config so server owners can tune it. |

### Engineering

| ID | Decision |
|---|---|
| D-22 | Every key number goes into NeoForge config (server-authoritative). |
| D-23 | Dedicated servers are supported; the client only needs the resource pack installed. |
| D-24 | A custom creative mode tab + one set of advancements are added; no custom commands. |
| D-25 | Textures: only the requirements are written down + a batch generation script is provided; the actual pixels are reviewed by the author. |
| D-26 | Naming: semantically English names; jokes are kept only in the Chinese display names. |
| D-27 | Friendly-fire ruling: the area effects of the Blast Carrot and the red/brown mushroom puffs **do not filter friend or foe**; teammates are hit just the same. Copied verbatim from the original wording. |
| D-28 | The Pumpkin Turret fire interval is **60 ticks (3 seconds)**, adjustable through config. |
| D-29 | Food values: Power Beet and Lucky Berry are both **4 / 0.3**, eating time 32 ticks. |
| D-30 | Fang Trap Cactus fang radii: **outer ring 2.0 blocks, inner ring 1.0 blocks**. |
| D-31 | The remaining completion items take their default values: the poison potion cloud follows vanilla at 3.0 / 600 ticks; projectile lifetime 100 ticks; "a few particles" is taken as 8; the friendly-side rule for teamless players is `self`; a non-floating deployment drops its own item after losing its support. |
| D-32 | Poisonous potatoes are not plantable in vanilla, so the Toxic Ubi Server needs its **own crop block and seed item registered separately**, not shared with the other crops. |
| D-33 | Offset of the things the Lucky Berry spawns: a random standable block within a **radius of 2 blocks** of the eater; if none fits, the nearest one. |
| D-34 | T-01 adopted: the planting form is implemented as **14 independent `FarmstormCropBlock`s** (`<id>_crop` etc.), with no additional BlockItem registered. |
| D-35 | T-02 adopted: the deployable crops' item is the BlockItem of the deployment block, and `useOn` splits into "on farmland = plant / on a block = deploy". |
| D-36 | T-03 adopted: the indestructibility of the Torchflower / Pitcher Plant is implemented by `getDestroyProgress` returning 0 + a second check in `playerWillDestroy`, which also takes effect in creative mode. |
| D-37 | T-04 adopted, **narrowing the scope of D-13**: D-13's "turrets hard-mine for 10 seconds" applies only to `pumpkin_turret`; the Torchflower / Pitcher Plant follow D-14. Ruling principle: a specific clause outranks a general clause. |
| D-38 | T-05 adopted: when the Puffed Wheat replaces blocks it skips blocks with explosion resistance ≥ 3600000 (bedrock / barrier etc.). This is a destruction-prevention floor, not a balance adjustment. |
| D-39 | T-06 adopted: 2 custom damage types — `farmstorm:void_bolt` (turret missile) and `farmstorm:crop_blast` (crop explosion). |
| D-40 | T-07 adopted: **no Mixin is introduced**. All requirements are met with inheritance + the NeoForge event bus + native extension points. |
| D-41 | T-08 adopted: temporary enchantments are recorded **on the item** as the DataComponent `farmstorm:temporary_enchantments`, not in persistent player data. |

---

## 4. Crop registry name summary (15 entries)

| # | Name | Vanilla crop | Item ID | Seed item ID | Type | Category |
|---|---|---|---|---|---|---|
| 1 | Puffed Wheat | Wheat | `puffed_wheat` | `puffed_wheat_seeds` | Throwable | Construction |
| 2 | Blast Carrot | Carrot | `blast_carrot` | Same name (itself plantable) | Deployable (top) | Trap, Damage |
| 3 | Ubi Server | Potato | `ubi_server` | Same name (itself plantable) | Throwable | Demolition, Damage |
| 4 | Toxic Ubi Server | Poisonous Potato | `toxic_ubi_server` | Same name (itself plantable) | Throwable | Buff, Demolition, Damage |
| 5 | Power Beet | Beetroot | `power_beet` | `power_beet_seeds` | Edible | Buff |
| 6 | Melon Slice Bomb | Melon Slice | `melon_slice_bomb` | None (not plantable) | Throwable | Damage |
| 7 | Melon Grenade | Melon | `melon_grenade` | `melon_grenade_seeds` | Throwable | Demolition, Damage |
| 8 | Pumpkin Turret | Pumpkin | `pumpkin_turret` | `pumpkin_turret_seeds` | Deployable (top / bottom / side) | Turret, Damage |
| 9 | Awakened Torchflower | Torchflower | `awakened_torchflower` | `awakened_torchflower_seeds` | Deployable (top) | Turret, Buff |
| 10 | Awakened Pitcher Plant | Pitcher Plant | `awakened_pitcher_plant` | `awakened_pitcher_plant_pod` | Deployable (top) | Turret, Buff |
| 11 | Fang Trap Cactus | Cactus | `fang_trap_cactus` | Same name (itself plantable) | Deployable (top) | Trap, Damage |
| 12 | Bamboo Firecracker | Bamboo | `bamboo_firecracker` | Same name (itself plantable) | Throwable | Damage |
| 13 | Red Mushroom Puff | Red Mushroom | `red_mushroom_puff` | Same name (itself plantable) | Throwable | Buff |
| 14 | Brown Mushroom Puff | Brown Mushroom | `brown_mushroom_puff` | Same name (itself plantable) | Throwable | Buff |
| 15 | Lucky Berry | Sweet Berries | `lucky_berry` | Same name (itself plantable) | Edible | Buff |

Notes:
- For deployables the block ID and the item ID have the **same name** (items and blocks live in different registries, so identical names are allowed).
- "Same name (itself plantable)" means the crop's seed form is the crop itself; no separate seed item is registered.
- The Melon Slice Bomb is not plantable (a vanilla melon slice is not a seed item), so it has no seed form and takes no part in the planting loop.

---

## 5. Corrections to the text of plan.txt

| ID | Correction | Reason |
|---|---|---|
| C-1 | The entry count is **15**, not 16. The Lucky Berry random pool is therefore 14 kinds. | Checked entry by entry against lines 33~156 of plan.txt. |
| C-2 | The Melon Slice Bomb's "cannot destroy blocks with explosion resistance ≥ -1" is changed to "≥ cobblestone (6.0)". | The author confirmed it was a typo; aligned with the Melon Grenade. |
| C-3 | The Pumpkin Turret keeps projectile interception; the other turrets only lock onto players. | Author confirmed; narrows the scope. |
| C-4 | The Torchflower / Pitcher Plant are classified as "turret" in the plan but have no attack behaviour. The category label is kept; technically they are implemented as an "aura provider (AuraProvider)". | The classification is for players to read; the implementation follows behaviour. |
| C-5 | The Fang Trap Cactus trigger distance uses Euclidean distance ≤ 3. | The plan does not state the metric; the author confirmed Euclidean. |
| C-6 | Particle and sound broadcasting **uses the vanilla packets** (`ClientboundLevelParticlesPacket` / `ClientboundSoundPacket`); no custom S2C packet is introduced. | Cross-document conflict: DESIGN §3.5 / §7.16 conflict with TECH §5.5; TECH wins. Custom packets only add synchronization surface and version fragility. |
| C-7 | The number of advancements is **12**, corresponding to 12 `data/farmstorm/advancement/*.json` files and 24 lang keys. | Cross-document conflict: DESIGN §6 lists 12, TECH §6.1 counts only 6; DESIGN's 12 wins. |
| C-8 | When mined by the friendly side, the Torchflower / Pitcher Plant use **vanilla normal mining speed**; D-13's "fixed hard-mine 10 seconds" **applies only to `pumpkin_turret`**. | Cross-document conflict: DESIGN §3.3/§4.9/§4.10/§7.1 also write them as 10 seconds, which conflicts with D-14's original wording "the friendly side can mine and recover them (normal mining speed)". D-14 is the interview's original wording and has the highest priority; D-37 already narrowed D-13. |
| C-9 | Deployment blocks **store only `ownerUuid` + `ownerName`, no team snapshot**; the friendly check resolves the owner's current team in real time at the instant of triggering; when the owner is offline their last known team is looked up in the Scoreboard member table. | Cross-document conflict: DESIGN §7.2 conflicts with TECH §4.2/§4.5. The DESIGN wording is adopted: one fewer synchronized field, and team changes take effect immediately. When the owner has no team, the `self` rule of D-31 applies. |
| C-10 | **The single rule for English display names: display name = the Title Case of the registry ID.** The 15 crops' English names are fixed as Puffed Wheat / Blast Carrot / Ubi Server / Toxic Ubi Server / Power Beet / Melon Slice Bomb / Melon Grenade / Pumpkin Turret / Awakened Torchflower / Awakened Pitcher Plant / Fang Trap Cactus / Bamboo Firecracker / Red Mushroom Puff / Brown Mushroom Puff / Lucky Berry. | Four parallel translators each coined their own English names, so five of the 15 entries (`ubi_server`, `power_beet`, `melon_slice_bomb`, `melon_grenade`, `pumpkin_turret`) carried two competing display names each: a vendor-style name vs. the short form, an exclamation-mark form vs. the plain one, and a reordered name. Binding to the registry ID leaves no room for interpretation. |

---

## 6. Explicitly deferred topics (not implemented in v0.0.1)

1. Farmer's Delight crop compatibility.
2. FTB Teams integration (the interface is already reserved).
3. New "plantable" crop design (`plan.txt` item 12).
4. Numerical balance tuning (D-21 already locks the original values).
5. Custom commands.
