# Farmstorm · Design Document

| Item | Value |
|---|---|
| Document | 01-DESIGN |
| Version | v0.0.1 |
| Status | Final |
| Upstream | `plan.txt` (the author's original idea), `docs/zh/00-DECISIONS.md` (single source of truth) |
| Downstream | `02-TECH-SPEC.md`, `03-TASKS.md`, `05-PROMPT-MAIN.md` |
| Language | Body text in Chinese; code identifiers, registry names and config keys always in English |

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

## 0. Reading conventions

1. **Number sources**: every gameplay number in this document is copied verbatim from §plan.txt§ and is not balanced in any way (D-21). Any implementation-required parameter not given by §plan.txt§ is registered uniformly in §3.8.6 "numerical completion register" with an identifier. Where the wording of §plan.txt§ conflicts with the decisions, §00-DECISIONS.md§ wins (see appendix A, correction entries C-1~C-5).
2. **Markup**: §[D-xx]§ means the conclusion comes directly from the decision record; §[C-x]§ means the conclusion comes from a correction of the original §plan.txt§ wording; §[G-x]§ means the value is a completion item.
3. **Units**: time is always labelled in "seconds" or "game ticks", 1 second = 20 game ticks. Levels are always written "level N"; when stored, `amplifier = N − 1`.
4. **Server authority**: every check, settlement and state transition happens on the server. The client only renders, draws particles and forwards input (D-23).

---

## 1. Mod positioning and design goals

### 1.1 One-line positioning

Farmstorm re-arms Minecraft's **farm produce** as **PvP items**: the same crop can be planted in the ground, thrown, mounted on a wall, or eaten, turning the originally peaceful "farm and grow" route into an equipment route that takes part in combat directly.

### 1.2 Design goals

| # | Goal | Means |
|---|---|---|
| G1 | Give agricultural output PvP value | All 15 improved crops carry at least one combat-facing category (Damage / Demolition / Trap / Turret / Buff) |
| G2 | Keep the original "farming" growth curve | Every crop has a seed form; harvesting drops crop and seed together at vanilla ratios, a self-sufficient loop (D-08) |
| G3 | Let players tell vanilla from improved at a glance | The "Produce Converter" work block + a green "+" badge in the bottom-right corner of the texture (§plan.txt§ lines 6, 8) |
| G4 | Make improved crops "no longer vanilla crops" | Improved crops do not inherit vanilla uses: no bread, no animal feed, no vanilla recipes (D-09) |
| G5 | A readable, extensible team dimension | Friendly determination goes through the `TeamResolver` abstraction; v0.0.1 implements it with scoreboard teams (D-06) |
| G6 | Server-owner tunable | Every key number goes into NeoForge server config (D-22) |

### 1.3 How the "vanilla crop / improved crop" boundary is drawn

The boundary must be expressed by two mutually independent means **at the same time**; neither alone suffices:

**(1) Gameplay layer: the Produce Converter**

- The only acquisition path is the converter, not a crafting table. A player's first step with an improved crop is necessarily "build a converter → feed it a vanilla crop → take the improved crop".
- The converter is **bidirectional 1:1 and reversible** (D-11). This means an improved crop is not a one-way dead end; a player can always turn an unneeded improved crop back into the vanilla crop and cut the loss.
- The converter has an **experience cost** (3 experience points each, D-12), so "bulk conversion" is a costly act rather than zero-cost duplication.

**(2) Visual layer: a green "+" in the bottom-right corner of the texture**

- Each improved crop's item texture carries a **green** "+" badge overlaid on the **bottom-right corner** (§plan.txt§ line 8).
- The badge is supplied by the resource pack; size and opacity are generated uniformly by the texture script, and the author reviews the final pixels (D-25).
- The badge is **added to item textures only**; block forms (deployment blocks, crop blocks) keep the appearance the crop body ought to have and carry no badge — when a player sees a crop in the field they see a crop, not a label.
- Seed items carry the badge as well, so that "this is an improved seed" is readable in the inventory.

### 1.4 Non-goals for v0.0.1

1. No numerical balancing (D-21).
2. No Farmer's Delight compatibility (deferred item 1).
3. No FTB Teams integration, only the interface (deferred item 2).
4. No new "plantable" crop design, only the interface and the specification (D-10, deferred item 3).
5. No custom commands (D-24, deferred item 5).

---

## 2. Glossary and classification system

### 2.1 Glossary

| Term | Definition |
|---|---|
| Vanilla crop | The Minecraft vanilla item/block named in the "corresponding vanilla crop" column of the 15 entries in §plan.txt§ |
| Improved crop | The 15 items added by this mod, one-to-one with the 15 vanilla crops |
| Seed item | The seed form of an improved crop. Some are separate items (e.g. `farmstorm:puffed_wheat_seeds`), some are the crop itself (e.g. `farmstorm:blast_carrot`) |
| Crop block | The ground block an improved crop grows into after planting (isomorphic to `CropBlock`) |
| Produce Converter | The only work block that converts vanilla crops ⇄ improved crops |
| Conversion recipe | A data-driven custom `RecipeType`; one row = one "vanilla ⇄ improved" mapping |
| Deployment block | The block entity formed after a deployable crop is placed (`Block` + `BlockEntity`) |
| Deployment point | The block position of the deployment block (`BlockPos`), the origin of most distance checks |
| Support block | The block a deployment block is attached to, determined by the "attachment position" |
| Attachment position | The deployment block's orientation relative to the support block: top / bottom / side |
| Floating | Whether a deployment block is retained and keeps running after its support block disappears |
| Owner | The player who deployed the block, recorded by UUID |
| Friendly | The set of players for whom `TeamResolver.isFriendly` holds against the owner (including the owner) |
| Non-friendly | The complement of friendly |
| One-shot deployment | A deployment block that settles and self-destructs on trigger and drops nothing |
| Persistent deployment | A deployment block that persists after placement and executes its behaviour periodically |
| Aura | The set of effects a persistent deployment applies periodically to friendly players in an area |
| Impact point | The block position and the exact position where a projectile terminates |
| Hard-mine | Mining whose break time is fixed and unaffected by tools and enchantments; in v0.0.1 only `pumpkin_turret` is subject to it (D-13, D-37) |

### 2.2 Dimension one: Type

Type answers "**how is this item used**". Type is a **single-valued enum** — an item belongs to exactly one type.

#### 2.2.1 Throwable

- **Formal definition**: an item a player can right-click to throw, which spawns an independent projectile entity and settles one effect at the impact point.
- **Behaviour contract**:
  1. Stack limit **16** (D-16).
  2. Each throw consumes **1**; creative mode consumes nothing (D-16).
  3. The projectile's initial velocity and gravity follow vanilla snowball-type projectiles (completion G-6 only adds a lifetime, it does not change the motion).
  4. Settlement at the impact point is **idempotent**: a projectile entity settles exactly once in its lifetime.
  5. Settlement must happen on the server main thread; the client only plays particles and sounds.
  6. Throwable items are **not edible**, even if the corresponding vanilla crop is (D-09).
- **Lifecycle**:
  `right-click throw → item count −1 → projectile entity spawned and flies → hits a block / hits an entity / times out (100 ticks, G-6) / falls into an unloaded chunk → impact point settles once → entity discard()`

#### 2.2.2 Deployable

- **Formal definition**: an item a player can right-click a face of a block with, thereby spawning a deployment block in the position adjacent to that face. The deployment block carries owner information and runtime state.
- **Behaviour contract**:
  1. There are three attachment positions: **top** (against the upper face of the support block), **bottom** (against the lower face) and **side** (against the four horizontal faces). Each crop declares which of them it supports.
  2. The support block must be a **full opaque block** (criteria in §3.3.2).
  3. The deployment block must record the owner's UUID.
  4. The breaking rule of a deployment block is decided **per block**: **only `pumpkin_turret` hard-mines for a fixed 10 seconds** (D-13, D-37); every other deployment block (including `awakened_torchflower` / `awakened_pitcher_plant`) is mined by the friendly side at **vanilla normal speed** (D-14, C-8). The Torchflower and the Pitcher Plant additionally have "non-friendly players cannot mine it + no explosion can destroy it" (D-14).
  5. Whether the block survives the disappearance of its support block is decided by the entry's "floating" declaration, defaulting to **not floating** (it drops as its own item).
  6. One-shot / persistent is specified by the entry, not uniformly by type.
- **Lifecycle**:
  `hold in hand, right-click a block face → validate attachment position and support block → count −1 → spawn deployment block + BlockEntity (write ownerUuid / supportPos / supportFace) → enter the tick loop → settle on trigger or execute periodically → broken (drops per the breaking rule) / one-shot self-destruct (drops nothing)`

#### 2.2.3 Plantable

- **Formal definition**: a type that grows as a crop block, with the item as the seed, and can only be planted on dirt / grass block / farmland.
- **v0.0.1 scope**: **no new design**, only the interface and the specification are kept (D-10). Because all 15 crops come with a seed form (D-08) and are therefore all naturally plantable, "plantable" in v0.0.1 manifests as a **cross-cutting capability** rather than a standalone item.
- **Behaviour contract**:
  1. The seed can only be placed on dirt, grass block, farmland (and their mud variants); other blocks are refused with failure feedback.
  2. When broken after maturing: **crop and seed drop together**, with ratios following the structure of the vanilla crop's drop table (D-08).
  3. Bone meal acceleration, farmland wetness, light requirements and other growth semantics follow the vanilla crop block.
  4. Improved crop blocks **take no part in vanilla recipes** (D-09).
- **Lifecycle**:
  `seed right-click on a block → crop block spawned (age=0) → random tick growth → mature (age=max) → broken → crop + seed dropped → back to the seed`

#### 2.2.4 Edible

- **Formal definition**: an item that right-click activates an eating animation, after which a set of effects is applied.
- **Behaviour contract**:
  1. Eating time 32 ticks (D-29).
  2. Effects are applied in the server branch of `finishUsingItem`.
  3. Repeated eating refreshes the **duration**, it does not stack the **level**.
  4. Eating in creative mode consumes no item (the symmetric treatment of D-16).
  5. Edible crops take no part in vanilla recipes and are not animal feed (D-09).
- **Lifecycle**:
  `press right-click → 32 ticks of eating animation → progress complete → server applies effects and consumes 1 → item count −1`

### 2.3 Dimension two: Category

Category answers "**what does this item do in combat**". Category is a **multi-valued set** — an item can belong to several categories at once, and each category has its own **criteria** (satisfying them is enough to carry the tag).

#### 2.3.1 Trap

- **Formal definition**: a deployment that stands by silently after deployment until its trigger condition is met, whereupon it produces a one-shot effect.
- **Criteria** (all must hold): ① it produces no effect on its own after deployment; ② it is triggered by "a non-friendly player entering the specified distance"; ③ it settles and self-destructs immediately on trigger.
- **Affected targets**: **non-friendly players** only, mobs are not handled (D-07).
- **Distance metric**: Chebyshev distance (D-20); the sole exception is in §3.7.

#### 2.3.2 Turret

- **Formal definition**: a persistent deployment that after placement actively searches for targets periodically and acts on them.
- **Criteria** (satisfying either is enough): ① it has an explicit attack behaviour (fires an entity, spawns a damage source); ② §plan.txt§ explicitly classifies it as a turret.
- **Two implementation paths**:
  - **Attack turret** (AttackTurret): actively acquires targets and attacks, e.g. the Pumpkin Turret.
  - **Aura turret** (AuraProvider): classified as a turret by §plan.txt§ but with **no attack behaviour whatsoever**; the actual behaviour is periodically granting buffs to the friendly side, e.g. the Awakened Torchflower and the Awakened Pitcher Plant. The category label is kept (for players to read), the implementation goes through `AuraProvider` (C-4).
- **Hard-mine**: **only the single block `pumpkin_turret`** requires a fixed **10 seconds** (200 ticks) to drop when mined (D-13, D-37, §plan.txt§ line 16); the two aura turrets (Torchflower / Pitcher Plant) are not subject to it and are recovered by the friendly side at vanilla normal speed (D-14, C-8).
- **Distance metric**: Chebyshev for target acquisition (D-20); Euclidean radius for the aura.

#### 2.3.3 Demolition

- **Formal definition**: a crop capable of **destroying blocks**.
- **Criteria**: its main effect (e.g. an explosion) can destroy at least one kind of vanilla block under normal game rules.
- **Note**: demolition does not require "always breaks"; it only requires "has the ability to destroy blocks". For example the Melon Grenade's explosion has an explosion resistance threshold (blocks ≥ cobblestone 6.0 cannot be destroyed), yet it can destroy a large number of blocks and still belongs to demolition.

#### 2.3.4 Buff

- **Formal definition**: a crop that brings buff / debuff effects to the user or to players in range.
- **Criteria**: its main effect is "applying a status effect", not dealing direct damage or destroying blocks.
- **Carriers**: edible, throwable and deployable types can all carry this category.
- **Name normalization**: line 18 of §plan.txt§ writes this category as "buff类" in its defining text, but the category list on lines 14~20 has no "增益类"; the Lucky Berry entry says "增益类". The whole document is unified as **buff类** (see appendix A, A-3).

#### 2.3.5 Construction

- **Formal definition**: a crop capable of **spawning blocks**.
- **Criteria**: its effect writes new blocks into the world, and those blocks are **vanilla blocks with a substantive use** (not this mod's deployment blocks).
- **Difference from demolition**: construction "writes blocks", demolition "deletes blocks". When both appear on the same item exclusively, the entry must state it explicitly.

#### 2.3.6 Damage

- **Formal definition**: a crop that can deal damage to players.
- **Criteria**: there is an explicit damage source somewhere in its effect chain (explosion, projectile, summoned entity, void damage, etc.).
- **Note**: damage entries in §plan.txt§ usually also give a **damage cap**; the cap is a hard constraint on "how much health this effect can take at most" and outranks the theoretical damage derived from the explosion power.

### 2.4 Type and category are two orthogonal dimensions

| | Throwable | Deployable | Plantable | Edible |
|---|---|---|---|---|
| **Trap** | — | Blast Carrot, Fang Trap Cactus | — | — |
| **Turret** | — | Pumpkin Turret, Awakened Torchflower, Awakened Pitcher Plant | — | — |
| **Demolition** | Ubi Server, Toxic Ubi Server, Melon Grenade | — | — | — |
| **Buff** | Toxic Ubi Server, Red Mushroom Puff, Brown Mushroom Puff | Awakened Torchflower, Awakened Pitcher Plant | — | Power Beet, Lucky Berry |
| **Construction** | Puffed Wheat | — | — | — |
| **Damage** | Ubi Server, Toxic Ubi Server, Melon Slice Bomb, Melon Grenade, Bamboo Firecracker | Blast Carrot, Pumpkin Turret, Fang Trap Cactus | — | — |

Notes:

1. **Type is "how it is used", category is "what it does in combat"**; neither implies the other: among throwables there is the pure-damage Bamboo Firecracker and the pure-construction Puffed Wheat; among deployables there is the attacking Pumpkin Turret and the Torchflower that only grants buffs.
2. **13 entries span 2 or more categories** (Toxic Ubi Server 3, Melon Grenade 2, Torchflower 2, ...), so category must be a multiset; in implementation it is expressed as `Set<Category>`.
3. **The plantable column being entirely empty is a consequence, not a design omission**: planting is a cross-cutting capability (§2.2.3) and does not occupy a type slot.
4. **All 15 entries hit at least one category**, so §1.2 G1 is achieved.

---

## 3. Core system rules

### 3.1 Produce Converter

#### 3.1.1 Positioning

The converter is the **only** channel between vanilla crops and improved crops. It is not a crafting recipe but a **work block + single-slot GUI**.

#### 3.1.2 Structure

| Item | Specification |
|---|---|
| Block ID | `farmstorm:converter` |
| GUI slot | **Single slot** input, no output slot (the conversion settles on take-out) |
| Slot constraint | Only accepts items that appear in the "conversion recipe table"; other items are bounced straight back |
| Recipe carrier | Custom `RecipeType` (`farmstorm:conversion`), JSON data-driven |
| Recipe direction | Bidirectional 1:1; one recipe declares both A→B and B→A (D-11) |
| Experience cost | Each conversion deducts **3 experience points** (D-12) |
| Nether Star cost | Torchflower / Pitcher Plant **forward only** (vanilla → awakened) consume **1** Nether Star extra each time; the reverse direction is free (D-12) |

#### 3.1.3 Conversion recipe data format (abridged)

    {
      "type": "farmstorm:conversion",
      "vanilla":  "minecraft:wheat",
      "improved": "farmstorm:puffed_wheat",
      "nether_star": 0        // 仅正向消耗，0 表示不消耗
    }

- One recipe registers two directions.
- `nether_star` only takes effect for the "vanilla → improved" direction; the reverse is always 0.
- v0.0.1 has 15 recipes in total (one per entry; although the Melon Slice Bomb is not plantable, the "Melon Slice → Melon Slice Bomb" path must exist).

#### 3.1.4 Settlement order (pseudocode)

    onTakeFromSlot(player):
        recipe = lookup(slot.item)
        if recipe == null: return 拒绝并弹回
        if player.experiencePoints < 3: return 拒绝（不扣等级，不扣经验）
        if recipe.netherStar > 0 and player 未提供下界之星: return 拒绝
        player.experiencePoints -= 3
        if recipe.netherStar > 0: 消耗 1 个下界之星
        result = recipe.otherSide(slot.item)      // 1:1，数量不变
        give(result, player)

#### 3.1.5 Key constraints

1. **Refuse when experience is insufficient, never deduct a level** (D-12). The criterion is `player.experiencePoints` (experience points within the current level), not the level.
2. **3 points are deducted per conversion**, independent of the converted amount — one conversion converts 1 item. If a player puts in a whole stack, it settles one item at a time as "3 points per 1 converted", stopping as soon as the points run out and leaving the remaining items in place.
3. **Bidirectional 1:1**: no loss and no by-product.
4. **The Nether Star only applies to two crops**, and only in the forward direction; this is the entry fee set for "ancient plants".
5. Conversion is **data-driven**; adding a crop does not require Java changes.

### 3.2 Planting and harvesting loop

#### 3.2.1 Rules

1. **Every crop has a seed form**, in one-to-one correspondence with the vanilla seed form (D-08).
2. **Harvesting drops the crop and the seed item together at vanilla ratios**, forming a self-sufficient loop (D-08).
3. Seed forms fall into two classes:
   - **Separate seed items**: `farmstorm:puffed_wheat_seeds`, `farmstorm:power_beet_seeds`, `farmstorm:melon_grenade_seeds`, `farmstorm:pumpkin_turret_seeds`, `farmstorm:awakened_torchflower_seeds`, `farmstorm:awakened_pitcher_plant_pod`.
   - **The crop itself is the seed**: the remaining 8, marked "same name (itself plantable)".

#### 3.2.2 Correspondence table

| # | Improved crop | Crop block | Seed item | Vanilla reference |
|---|---|---|---|---|
| 1 | Puffed Wheat | `farmstorm:puffed_wheat_crop` | `puffed_wheat_seeds` | Wheat crop |
| 2 | Blast Carrot | `farmstorm:blast_carrot_crop` | Itself | Carrot crop |
| 3 | Ubi Server | `farmstorm:ubi_server_crop` | Itself | Potato crop |
| 4 | Toxic Ubi Server | `farmstorm:toxic_ubi_server_crop` | Itself | Potato crop (see §3.2.3) |
| 5 | Power Beet | `farmstorm:power_beet_crop` | `power_beet_seeds` | Beetroot crop |
| 6 | Melon Slice Bomb | — | None | Not plantable |
| 7 | Melon Grenade | `farmstorm:melon_grenade_stem` | `melon_grenade_seeds` | Melon stem |
| 8 | Pumpkin Turret | `farmstorm:pumpkin_turret_stem` | `pumpkin_turret_seeds` | Pumpkin stem |
| 9 | Awakened Torchflower | `farmstorm:awakened_torchflower_crop` | `awakened_torchflower_seeds` | Torchflower crop |
| 10 | Awakened Pitcher Plant | `farmstorm:awakened_pitcher_plant_crop` | `awakened_pitcher_plant_pod` | Pitcher plant crop |
| 11 | Fang Trap Cactus | `farmstorm:fang_trap_cactus_block` | Itself | Cactus |
| 12 | Bamboo Firecracker | `farmstorm:bamboo_firecracker_stalk` | Itself | Bamboo |
| 13 | Red Mushroom Puff | `farmstorm:red_mushroom_puff_block` | Itself | Red Mushroom |
| 14 | Brown Mushroom Puff | `farmstorm:brown_mushroom_puff_block` | Itself | Brown Mushroom |
| 15 | Lucky Berry | `farmstorm:lucky_berry_bush` | Itself | Sweet berry bush |

#### 3.2.3 Two special mappings

1. **Toxic Ubi Server**: a vanilla poisonous potato is **not a standalone crop block**, it is a random product of the potato crop. This mod therefore registers a crop block and a seed item for it separately ("itself plantable"), detached from the vanilla potato's drop logic. This is the explicit landfall of the "one-to-one" principle where the vanilla structure leaves a gap. **Backed by decision: D-32.**
2. **The Melon Slice Bomb is not plantable**: a vanilla melon slice is not a seed item, so the Melon Slice Bomb has no seed form and takes no part in the planting loop. Players can only obtain it through the Melon Grenade's impact effect, or through the converter (Melon Slice → Melon Slice Bomb). It is the only one of the 15 entries that does not close the loop; this follows from the vanilla structure, it is not a gap.

#### 3.2.4 Drop implementation

- Drops reuse the **structure** of the vanilla crop block's loot table directly (count ranges, fortune modifiers, special tool checks such as shears), replacing only the `item` with the improved crop and the seed item.
- "At vanilla ratios" is therefore a **structural reference**, not a hardcoded copy of the vanilla numbers: when vanilla adjusts its drops, this mod needs no code change.
- The reference vanilla ratios (count ranges) follow the vanilla data pack at implementation time; this mod does not copy those constants.

### 3.3 Deployment system

#### 3.3.1 The three attachment positions

| Attachment position | Attached face | Deployment block position | Note |
|---|---|---|---|
| Top | Upper face of the support block | `supportPos.above()` | Stands on top of the block |
| Bottom | Lower face of the support block | `supportPos.below()` | Hangs from the ceiling |
| Side | The four horizontal faces | The block adjacent along the hit-face normal | Sticks to a wall |

- The destination block must be **replaceable** (air, water, grass, snow layer, etc.), otherwise placement fails.
- Each crop declares a `Set<Attachment>`; attachment positions outside the set are refused outright with a failure sound and no item consumed.

#### 3.3.2 The "full opaque block" criterion

The support block must satisfy all of:

1. `state.isSolidRender(level, pos)` is true (a full solid cube that can occlude light).
2. `state.getFluidState().isEmpty()` is true (no water / lava mixed in).
3. Not in the disabled set: replaceable blocks, leaves in `BlockTags`, slabs, fences, walls, glass, trapdoors, carpets, snow layers.

Otherwise deployment is refused with failure feedback and **no item consumed**.

#### 3.3.3 Floating rules after the support block is destroyed

| Floating declaration | Behaviour | Applicable entries |
|---|---|---|
| **Floating** (`floatOnSupportLost = true`) | After the support block disappears the deployment block stays in place, keeps ticking and its runtime state is not interrupted | Pumpkin Turret, Awakened Torchflower, Awakened Pitcher Plant |
| **Non-floating** (default, `floatOnSupportLost = false`) | On the tick after the support block disappears the deployment block is removed; if config allows, it drops its own item (allowed by default) | Blast Carrot, Fang Trap Cactus |

- The floating check runs on every `neighborChanged` and `onRemove`, **at most once per tick**, to avoid a block update storm.
- The three "floating" entries are written out word for word in §plan.txt§ as "may float after the support block is destroyed", so this is an entry-level attribute, not a global rule.

#### 3.3.4 Owner record

The `BlockEntity` behind a deployment block persists the following fields:

    ownerUuid        : UUID          // 归属者，必填
    ownerName        : String        // 仅用于日志与调试
    supportPos       : BlockPos      // 依附方块坐标
    supportFace      : Direction     // 依附面
    placedGameTime   : long          // 部署时刻
    triggered        : boolean       // 一次性部署物是否已结算（幂等标志）

- **No team snapshot is recorded**. The friendly check resolves the owner's current team with `TeamResolver` in real time **at the instant of triggering**; the reasoning is in §7.2.
- The `BlockEntity` must implement `saveAdditional` / `loadAdditional` so that owner and state survive a server restart.

#### 3.3.5 Breaking rules

| Category | Breaking method | Basis |
|---|---|---|
| `pumpkin_turret` (attack turret) | Fixed hard-mine **10 seconds = 200 ticks**, unaffected by tools, enchantments or Haste | D-13, D-37 |
| `awakened_torchflower` / `awakened_pitcher_plant` (aura turrets) | Friendly: **vanilla normal mining speed**; non-friendly: **break progress always 0**; no explosion can destroy them | D-14, C-8, D-36 |
| Trap category (`blast_carrot` / `fang_trap_cactus`) | Vanilla mining speed | D-13 by reverse inference |
| Torchflower / Pitcher Plant addendum | **Non-friendly players cannot break them**; the friendly side can mine and recover them normally; **no explosion can destroy them** | D-14 |

- The hard-mine of `pumpkin_turret` is implemented by overriding `getDestroyProgress` to return a fixed value, not by relying on block hardness: hardness is affected by tool enchantments and cannot guarantee "a constant 10 seconds". The "non-friendly players cannot mine it" of the Torchflower / Pitcher Plant is likewise implemented by `getDestroyProgress` returning 0, which also takes effect in creative mode (D-36).
- When broken, **1 unit of its own item** drops outward, aligned with the deployment cost, forming a recovery loop.

### 3.4 Throwing system

| Item | Rule | Basis |
|---|---|---|
| Stacking | **16** | D-16 |
| Consumption per throw | **1** | D-16 |
| Creative mode | No consumption | Symmetric treatment of D-16 |
| Projectile entity | Custom `ThrowableProjectile` subclass carrying an effect set per crop ID | Implementation requirement |
| Motion parameters | Follows the vanilla snowball: initial speed 1.5 m/t, gravity 0.03, drag 0.99 | Vanilla behaviour |
| Lifetime | 100 ticks (G-6) | Completion |
| Settlement count | **Exactly once** per spawn | Contract |

**Impact point resolution priority** (high to low):

1. **Hit an entity**: the hit point of `Projectile#onHitEntity` is the impact point, and the hit entity is recorded at the same time.
2. **Hit a block**: the block position inside the hit face of `Projectile#onHitBlock` is the "impact block", and the exact hit coordinates are the "impact exact position".
3. **Timeout / falling out of the world / falling into an unloaded chunk**: the projectile's last known position is the impact point; if the chunk at that position is not loaded the settlement is **discarded outright** (see §7.10).

**Two forms of the impact point**:

- `BlockPos`: used for "ranges measured in blocks" (the 3×3×3 / 5×5×5 of the Puffed Wheat).
- `Vec3`: used for "ranges measured in radii" (radius 4 of the mushroom puffs, the offset jitter of the Melon Grenade).

All throwable settlement runs on the server and is guarded for idempotency by a `settled` boolean.

### 3.5 Eating system

| Item | Rule |
|---|---|
| Food properties | Nutrition / saturation modifier / eating time, entry by entry in §3.8 and in each crop's specification |
| Eating time | 32 ticks (D-29) |
| Effect application | `Item#finishUsingItem` → server applies the debuff / buff |
| Repeated eating | Refreshes the duration, does not stack levels |
| Creative mode | Applies effects normally, consumes no item |
| Particles | If the entry specifies particles, they spawn around the **affected player** when the effect is applied |

**Area effects**: the area effects of the Blast Carrot, the two mushroom puffs and the Lucky Berry act on "**every player in range**" and **do not filter friend or foe** — teammates and the thrower / deployer themselves are hit just the same as long as they stand in range. This is **intentional design, not an omission** (D-27). The key criteria are in §3.6.3.

**Random-type eating**: the Lucky Berry is a **single weighted draw** whose 8 outcomes sum to exactly 100%, so each eating **hits exactly one outcome** and no multiple independent checks are made. The draw happens on the server, and the result is broadcast by the server as **vanilla particle packets and sound packets** (`ClientboundLevelParticlesPacket` / `ClientboundSoundPacket`) to drive the client's presentation; this project adds no network channel of its own (C-6).

### 3.6 Friendly determination

#### 3.6.1 Abstract interface

    interface TeamResolver {
        /* 返回玩家的队伍标识；空表示该玩家不属于任何队伍 */
        Optional<TeamId> teamOf(ServerPlayer player);

        /* 双方是否为「己方关系」，必须对称且自反 */
        boolean isFriendly(ServerPlayer a, ServerPlayer b);
    }

**Interface contracts** (an implementation must satisfy all of them):

| ID | Contract |
|---|---|
| T1 | **Reflexive**: `isFriendly(x, x) == true` always holds |
| T2 | **Symmetric**: `isFriendly(x, y) == isFriendly(y, x)` always holds |
| T3 | **Not required to be transitive**: teams may be disbanded or reorganised, so caches must be short-lived (valid within one check is enough) |
| T4 | **Read-only**: an implementation must not modify the scoreboard or any external state |
| T5 | **Main thread**: called only on the server main thread, the result is stable within one tick |
| T6 | **Empty team**: when `teamOf` returns empty, `isFriendly` is true only for itself (degradation rule, see §7.7) |
| T7 | **Replaceable**: the v0.0.1 implementation can be replaced wholesale by an FTB Teams implementation; callers must not depend on the concrete type |

#### 3.6.2 The v0.0.1 implementation: `ScoreboardTeamResolver`

    teamOf(player):
        team = player.getTeam()             // 原版计分板队伍，可能为 null
        return team == null ? empty : TeamId.of(team.getName())

    isFriendly(a, b):
        if a.getUUID() == b.getUUID(): return true          // T1
        ta = teamOf(a); tb = teamOf(b)
        if ta.isPresent() && tb.isPresent() && ta.get() == tb.get(): return true
        return false

- The team identifier is taken from `team.getName()` and mapped straight to `TeamId`.
- A player belonging to no team produces **no error and no exception**; the T6 degradation applies.
- This implementation is the **only** v0.0.1 implementation of `TeamResolver`, selected through `farmstorm.teams.resolver = "scoreboard"`.

#### 3.6.3 Caller conventions

| Caller | When it checks | Checked pair |
|---|---|---|
| Trap trigger | The tick a player enters range | Owner vs. entrant |
| Turret target acquisition | Every acquisition sweep | Owner vs. candidate player |
| Aura (AuraProvider) | Every refresh | Owner vs. players in range |
| Deployment block breaking | Break attempt event | Owner vs. breaker |

- The checked pair is always "**the owner** and the target player", never "the deployment block and the target player".
- When the owner is offline, their **last known team** still takes part in the check (see §7.2).
- **Friend/foe filtering applies only to the trigger condition, not to the effect area**: `TeamResolver` decides "whether the trap detonates", "whether the turret opens fire" and "whether the aura covers that player"; once an effect happens its area applies to all players with **no second friend/foe filter** (D-27, see §3.5).

### 3.7 Distance metric specification

| Purpose | Metric | Formula | Basis |
|---|---|---|---|
| Trap trigger | **Chebyshev** | `max(|dx|, |dy|, |dz|) <= r` | D-20 |
| Turret target acquisition (Pumpkin Turret) | **Chebyshev** | `max(|dx|, |dy|, |dz|) <= 5` | D-20 |
| Aura (Torchflower / Pitcher Plant) | **Euclidean radius** | `sqrt(dx² + dy² + dz²) <= r` | D-20 |
| Throwable area effect (mushroom puffs) | **Euclidean radius** | `sqrt(dx² + dy² + dz²) <= 4` | Original wording "radius" |
| Construction replacement range (Puffed Wheat) | **Cube edge length** | Edge 3 / 5, equivalent to Chebyshev radius 1 / 2 | Original wording "3*3*3 / 5*5*5" |
| **Sole exception** | **Euclidean distance** | Fang Trap Cactus trigger: `sqrt(dx² + dy² + dz²) <= 3` | D-20, C-5 |

**Specification clauses**:

1. The origin of a distance check is by default the **centre of the deployment point block** (`pos.getCenter()`) or **the block position the device occupies**; see each crop's specification.
2. Comparisons always use `<=`, never `<`.
3. Except where this section's table explicitly says "Euclidean", any new distance check **defaults to Chebyshev** and its exception must be registered in the decision record.
4. Wording such as "radius 20 blocks", "radius 10 blocks" and "radius 4 blocks" is always interpreted as a **Euclidean radius**; "Chebyshev distance N" is always interpreted as **the square neighbourhood of Chebyshev radius N**.

### 3.8 Config exposure list

All configuration entries live in `farmstorm-server.toml` (server-authoritative, D-22); hot reload only takes effect for entries marked "hot-loadable".

#### 3.8.1 General entries

| Config key | Default | Range | Effect | Source |
|---|---|---|---|---|
| `farmstorm.converter.xpCost` | 3 | 0 ~ 1000 | Number of experience points deducted per conversion | D-12 |
| `farmstorm.converter.netherStarForwardCost` | 1 | 0 ~ 64 | Nether Stars additionally consumed by forward Torchflower / Pitcher Plant conversion | D-12 |
| `farmstorm.converter.netherStarOnReverse` | false | true / false | Whether reverse conversion also consumes a Nether Star | D-12 |
| `farmstorm.stack.throwable` | 16 | 1 ~ 64 | Stack limit of throwable crops | D-16 |
| `farmstorm.stack.other` | 64 | 1 ~ 64 | Stack limit of deployable / edible / seed items | D-16 |
| `farmstorm.deploy.turretMiningTicks` | 200 | 0 ~ 72000 | Fixed hard-mine ticks of **`pumpkin_turret` only** (200 ticks = 10 seconds) | D-13, D-37 |
| `farmstorm.deploy.supportMustBeFullOpaque` | true | true / false | Whether the support block is forced to be a full opaque block | plan line 11 |
| `farmstorm.deploy.dropOnSupportLost` | true | true / false | Whether to drop the item itself when the support block disappears and the block is non-floating | G-9 |
| `farmstorm.teams.resolver` | `scoreboard` | `scoreboard` / `ftbteams` | Chooses the `TeamResolver` implementation | D-06 |
| `farmstorm.teams.untamedFallback` | `self` | `self` / `public` | Degradation rule for teamless players (`self` = friendly to itself only) | G-8 |
| `farmstorm.explosion.sliceResistThreshold` | 6.0 | 0.0 ~ 100.0 | Resistance threshold above which the Melon Slice Bomb explosion cannot destroy (6.0 = cobblestone) | C-2, D-19 |
| `farmstorm.explosion.grenadeResistThreshold` | 6.0 | 0.0 ~ 100.0 | Resistance threshold above which the Melon Grenade explosion cannot destroy (6.0 = cobblestone) | D-19 |
| `farmstorm.throwable.lifetimeTicks` | 100 | 1 ~ 1200 | Lifetime limit of projectile entities | G-6 |
| `farmstorm.food.eatTicks` | 32 | 1 ~ 200 | Eating time of edible crops | D-29 |

#### 3.8.2 Unified template for effect keys

For every crop that "applies a status effect", its effect parameters are always exposed through the following two keys:

    farmstorm.<crop>.<effect>.level          等级（1 = I 级，写入时 amplifier = 等级 - 1）
    farmstorm.<crop>.<effect>.durationTicks  时长（游戏刻）

The level range is §1 ~ 255§ and the duration range is §0 ~ 72000§ (0 = not applied).

#### 3.8.3 Per-crop entries

| Config key | Default | Range | Effect | Source |
|---|---|---|---|---|
| `farmstorm.puffed_wheat.dryEdge` | 3 | 1 ~ 15 | Cube edge length replaced in the dry state | plan line 38 |
| `farmstorm.puffed_wheat.wetEdge` | 5 | 1 ~ 15 | Cube edge length after expanding on contact with water | plan line 38 |
| `farmstorm.blast_carrot.triggerRadius` | 1 | 0 ~ 16 | Chebyshev radius of the trap trigger | plan line 46 |
| `farmstorm.blast_carrot.explosionPower` | 2 | 0 ~ 100 | Explosion power | plan line 46 |
| `farmstorm.blast_carrot.maxDamage` | 7 | 0 ~ 1000 | Damage cap this effect may cause with a single explosion | plan line 46 |
| `farmstorm.blast_carrot.nausea.level` | 3 | 1 ~ 255 | Nausea level | plan line 46 |
| `farmstorm.blast_carrot.nausea.durationTicks` | 60 | 0 ~ 72000 | Nausea duration (3 seconds) | plan line 46 |
| `farmstorm.ubi_server.explosionPower` | 4 | 0 ~ 100 | Explosion power | plan line 54 |
| `farmstorm.ubi_server.maxDamage` | 2 | 0 ~ 1000 | Damage cap | plan line 54 |
| `farmstorm.toxic_ubi_server.explosionPower` | 2 | 0 ~ 100 | Explosion power | plan line 62 |
| `farmstorm.toxic_ubi_server.maxDamage` | 2 | 0 ~ 1000 | Damage cap | plan line 62 |
| `farmstorm.toxic_ubi_server.cloudRadius` | 3.0 | 0.0 ~ 16.0 | Poison potion cloud radius (follows the vanilla lingering potion) | Vanilla behaviour |
| `farmstorm.toxic_ubi_server.cloudDurationTicks` | 600 | 0 ~ 72000 | Poison potion cloud duration (follows the vanilla lingering potion) | Vanilla behaviour |
| `farmstorm.melon_slice_bomb.explosionPower` | 2 | 0 ~ 100 | Explosion power | plan line 78 |
| `farmstorm.melon_slice_bomb.maxDamage` | 6 | 0 ~ 1000 | Damage cap | plan line 78 |
| `farmstorm.melon_grenade.spawnCount` | 4 | 0 ~ 64 | Number of melon slice projectiles spawned above the impact point | plan line 86 |
| `farmstorm.melon_grenade.spawnOffsetY` | 1 | 0 ~ 8 | Vertical offset of the spawn position relative to the impact point | plan line 86 |
| `farmstorm.melon_grenade.horizontalJitter` | 0.3 | 0.0 ~ 8.0 | Upper bound of the random offset on the x/z axes | plan line 86 |
| `farmstorm.melon_grenade.explosionPower` | 3 | 0 ~ 100 | Explosion power | plan line 86 |
| `farmstorm.melon_grenade.maxDamage` | 8 | 0 ~ 1000 | Damage cap | plan line 86 |
| `farmstorm.pumpkin_turret.targetRadius` | 5 | 0 ~ 64 | Chebyshev radius for target acquisition | plan line 95 |
| `farmstorm.pumpkin_turret.projectileDamage` | 1 | 0 ~ 100 | Void damage points caused by a missile hit | plan line 95 |
| `farmstorm.pumpkin_turret.knockbackLevel` | 1 | 0 ~ 10 | Knockback level applied on hit | plan line 95 |
| `farmstorm.pumpkin_turret.fireIntervalTicks` | 60 | 1 ~ 72000 | Fire interval (60 ticks = 3 seconds) | D-28 |
| `farmstorm.awakened_torchflower.auraRadius` | 20.0 | 0.0 ~ 128.0 | Aura Euclidean radius | plan line 103 |
| `farmstorm.awakened_torchflower.refreshTicks` | 100 | 1 ~ 72000 | Aura refresh interval (5 seconds) | plan line 103 |
| `farmstorm.awakened_torchflower.fireAspectLevel` | 1 | 0 ~ 10 | Fire Aspect level granted when a sword is held | plan line 103 |
| `farmstorm.awakened_torchflower.particlesPerRefresh` | 8 | 0 ~ 64 | Number of flame particles spawned around the player per refresh | G-7 |
| `farmstorm.awakened_pitcher_plant.auraRadius` | 10.0 | 0.0 ~ 128.0 | Aura Euclidean radius | plan line 111 |
| `farmstorm.awakened_pitcher_plant.refreshTicks` | 100 | 1 ~ 72000 | Aura refresh interval (5 seconds) | plan line 111 |
| `farmstorm.awakened_pitcher_plant.clearHarmful` | true | true / false | Whether to remove all harmful effects from players in range | plan line 111 |
| `farmstorm.awakened_pitcher_plant.particlesPerRefresh` | 8 | 0 ~ 64 | Number of heart particles spawned around the player per refresh | G-7 |
| `farmstorm.fang_trap_cactus.triggerRadius` | 3.0 | 0.0 ~ 32.0 | Euclidean distance used for the trigger | C-5, D-20 |
| `farmstorm.fang_trap_cactus.outerFangCount` | 8 | 1 ~ 64 | Number of outer-ring fangs | plan line 119 |
| `farmstorm.fang_trap_cactus.outerDelayTicks` | 10 | 0 ~ 200 | Outer-ring fang delay (0.5 seconds) | plan line 119 |
| `farmstorm.fang_trap_cactus.outerRadius` | 2.0 | 0.0 ~ 16.0 | Distribution radius of the outer-ring fangs | G-3 |
| `farmstorm.fang_trap_cactus.innerFangCount` | 5 | 1 ~ 64 | Number of inner-ring fangs | plan line 119 |
| `farmstorm.fang_trap_cactus.innerDelayTicks` | 4 | 0 ~ 200 | Inner-ring fang delay (0.2 seconds) | plan line 119 |
| `farmstorm.fang_trap_cactus.innerRadius` | 1.0 | 0.0 ~ 16.0 | Distribution radius of the inner-ring fangs | G-3 |
| `farmstorm.bamboo_firecracker.spawnIntervalTicks` | 5 | 1 ~ 200 | Interval between spawning one wind charge | plan line 127 |
| `farmstorm.bamboo_firecracker.durationTicks` | 140 | 1 ~ 7200 | Total spawning duration (140 / 5 = 28 times) | plan line 127 |
| `farmstorm.red_mushroom_puff.radius` | 4.0 | 0.0 ~ 32.0 | Euclidean radius of the area effect | plan line 135 |
| `farmstorm.brown_mushroom_puff.radius` | 4.0 | 0.0 ~ 32.0 | Euclidean radius of the area effect | plan line 144 |
| `farmstorm.lucky_berry.nutrition` | 4 | 0 ~ 20 | Nutrition | D-29 |
| `farmstorm.lucky_berry.saturationModifier` | 0.3 | 0.0 ~ 10.0 | Saturation modifier | D-29 |
| `farmstorm.lucky_berry.randomCropCount` | 16 | 0 ~ 64 | Number of random improved crops spawned | plan line 153 |
| `farmstorm.lucky_berry.lootChestCount` | 2 | 0 ~ 16 | Number of End City loot chests spawned | plan line 153 |
| `farmstorm.lucky_berry.smallExplosionPower` | 1 | 0 ~ 100 | Power of the small explosion | plan line 153 |
| `farmstorm.lucky_berry.creativeDurationTicks` | 1800 | 1 ~ 72000 | Creative mode duration (90 seconds) | plan line 153 |
| `farmstorm.power_beet.nutrition` | 4 | 0 ~ 20 | Nutrition | D-29 |
| `farmstorm.power_beet.saturationModifier` | 0.3 | 0.0 ~ 10.0 | Saturation modifier | D-29 |

#### 3.8.4 Effect table (together with the §3.8.2 template)

| Crop | effect key segment | level default | durationTicks default | Source |
|---|---|---|---|---|
| `power_beet` | `strength` | 2 | 600 | plan line 70 |
| `power_beet` | `speed` | 2 | 600 | plan line 70 |
| `power_beet` | `haste` | 2 | 600 | plan line 70 |
| `power_beet` | `resistance` | 1 | 600 | plan line 70 |
| `awakened_torchflower` | `strength` | 3 | 300 | plan line 103 |
| `awakened_torchflower` | `resistance` | 2 | 300 | plan line 103 |
| `awakened_torchflower` | `haste` | 2 | 300 | plan line 103 |
| `awakened_torchflower` | `speed` | 1 | 300 | plan line 103 |
| `awakened_torchflower` | `water_breathing` | 1 | 300 | plan line 103 |
| `awakened_torchflower` | `night_vision` | 1 | 300 | plan line 103 |
| `awakened_pitcher_plant` | `regeneration` | 2 | 300 | plan line 111 |
| `awakened_pitcher_plant` | `saturation` | 2 | 300 | plan line 111 |
| `awakened_pitcher_plant` | `absorption` | 1 | 300 | plan line 111 |
| `red_mushroom_puff` | `nausea` | 5 | 60 | plan line 135 |
| `red_mushroom_puff` | `blindness` | 1 | 60 | plan line 135 |
| `brown_mushroom_puff` | `weakness` | 2 | 200 | plan line 144 |
| `brown_mushroom_puff` | `slowness` | 2 | 100 | plan line 144 |
| `lucky_berry` | `regeneration` (25% branch) | 5 | 100 | plan line 153 |
| `lucky_berry` | `strength` (1.9% branch) | 5 | 12000 | plan line 153 |
| `lucky_berry` | `resistance` (1.9% branch) | 3 | 12000 | plan line 153 |
| `lucky_berry` | `jump_boost` (1.9% branch) | 5 | 12000 | plan line 153 |
| `lucky_berry` | `speed` (1.9% branch) | 2 | 12000 | plan line 153 |

#### 3.8.5 Lucky Berry probability table

| Key | Default (%) | Range | Outcome |
|---|---|---|---|
| `farmstorm.lucky_berry.chance.lightning` | 15 | 0 ~ 100 | Spawn a lightning bolt at the eater's position |
| `farmstorm.lucky_berry.chance.random_crops` | 25 | 0 ~ 100 | Spawn 16 random improved crops around the eater |
| `farmstorm.lucky_berry.chance.regeneration` | 25 | 0 ~ 100 | Gain Regeneration 5 for 5 seconds |
| `farmstorm.lucky_berry.chance.loot_chest` | 25 | 0 ~ 100 | Spawn 2 End City loot chests around the eater |
| `farmstorm.lucky_berry.chance.small_explosion` | 5 | 0 ~ 100 | Cause one explosion of power 1 |
| `farmstorm.lucky_berry.chance.charged_creeper` | 3 | 0 ~ 100 | Spawn a charged creeper next to the eater |
| `farmstorm.lucky_berry.chance.godlike_buff` | 1.9 | 0 ~ 100 | Gain Strength 5 / Resistance 3 / Jump Boost 5 / Speed 2 for 600 seconds |
| `farmstorm.lucky_berry.chance.creative_mode` | 0.1 | 0 ~ 100 | Switch the game mode to creative, restored after 90 seconds |

**Validation rule**: the sum of the 8 entries must equal 100. If the sum is not 100 when loading the config, **that config section is refused and falls back to the defaults**, and a warning is written to the server log (the loading of other entries is unaffected).

#### 3.8.6 Numerical completion register

Parameters that §plan.txt§ does not give but the implementation must have are all registered here; no already-given number is changed. D-28 ~ D-31 confirmed these completion items one by one, and the confirmed results are written straight back into the corresponding rows below.

| ID | Gap | Chosen value | Note |
|---|---|---|---|
| G-1 | Nutrition / saturation modifier / eating time of edible crops | Power Beet and Lucky Berry **both** 4 / 0.3; eating time 32 ticks | Vanilla food baseline. D-29 has confirmed the two crops share these values; all of them go into config |
| G-2 | Fire interval of the Pumpkin Turret | 60 ticks (3 seconds) | The original only says "fires", no rate is given. D-28 has confirmed 3 seconds, exposed as config |
| G-3 | Distribution radii of the two fang rings of the Fang Trap Cactus | Outer ring 2.0 blocks, inner ring 1.0 blocks | The original gives the ring count, the count per ring and the delay, no radius; the outer ring must fully enclose the inner ring. D-30 has confirmed |
| G-4 | Volume and duration of the poison potion cloud | Follows the vanilla lingering Potion of Poison (radius 3.0 / 600 ticks) | The original says "equivalent to throwing a lingering Potion of Poison", an explicit reference to the vanilla parameters. D-31 has confirmed |
| G-5 | Impact offset of the things the Lucky Berry spawns | A random standable block within 2 blocks of the eater, the nearest if none fits | The original says "next to the eater" without an offset amount. D-33 has confirmed |
| G-6 | Projectile lifetime | 100 ticks | Follows the vanilla projectile lifetime. D-31 has confirmed |
| G-7 | The concrete number behind "a few particles" | 8 per refresh per player | The original says "a few", a configurable base count is needed. D-31 has confirmed |
| G-8 | Friendly rule for teamless players | Friendly to itself only (`self`) | §plan.txt§ only defines the case "the deployer belongs to a team". D-31 has confirmed |
| G-9 | Drop rule for a non-floating deployment after it loses its support | Drops its own item | Avoids punishment loss for the player. D-31 has confirmed |

---

## 4. Full specification of the 15 improved crops

The template fields match §plan.txt§ lines 22~31, with two rows added per the project specification: "English ID" and "Implementation notes". **The "Function" field copies the wording of §plan.txt§ verbatim, with nothing cut.**

---

### 4.1 Puffed Wheat

Item name: Puffed Wheat
English ID: `farmstorm:puffed_wheat` (seed item `farmstorm:puffed_wheat_seeds`)
Corresponding vanilla crop: Wheat
Type: Throwable
Category: Construction
Function: throw it out and at the impact point replace the surrounding 3*3*3 blocks (air blocks included) with hay blocks; when a water source block exists within the 3*3*3 area around the impact point, the replacement area expands to 5*5*5
Item description: What? This isn't a cookie, this is our specially made compressed wheat; add water and it grows bigger and taller, and without water it still grows

**Implementation notes**

- **Attachment position**: not applicable (throwable, produces no deployment block).
- **One-shot / persistent**: one-shot. Settles on hit and the entity is destroyed immediately afterwards.
- **Trigger distance and metric**: a cube centred on the impact block, edge 3 (dry state) / edge 5 (wet state), equivalent to Chebyshev radius 1 / 2. The check "does a water source block exist" is performed once **inside the original 3×3×3 cube**; on a hit the range switches to 5×5×5 with no second check. The water criterion is `FluidTags.WATER` and `fluidState.isSource()`.
- **Breaking conditions**: destroys no blocks. The replacement range covers air, fluids and solid blocks alike, writing `minecraft:hay_block` directly with no drop settlement for the original block. Deployment blocks (this mod's) are overwritten too, see §7.11.
- **Stacking**: 16 (throwable, D-16).
- **Notes**: the range includes air, so the effect presents as "conjuring" a solid hay cube out of nothing; if the impact point sits at the edge of an unloaded chunk it is handled per §7.10.

---

### 4.2 Blast Carrot

Item name: Blast Carrot
English ID: `farmstorm:blast_carrot` (seed item of the same name, itself plantable)
Corresponding vanilla crop: Carrot
Type: Deployable (block top)
Category: Trap, Damage
Function: a one-shot crop; after deployment, when a non-friendly player is at a Chebyshev distance of 1 or less from the deployment point it explodes with a power of 2, but the explosion deals at most 7 damage and gives every player in range Nausea 3 for three seconds
Item description: Get any closer and it blows up!

**Implementation notes**

- **Attachment position**: **block top** only. Bottom and side are refused outright.
- **One-shot / persistent**: one-shot. Settles and removes the block on trigger, **dropping no item** (so a one-shot trap cannot be recycled over and over).
- **Trigger distance and metric**: Chebyshev distance ≤ 1, origin the deployment point block position, the checked object being the player's block position. The scan runs on every `BlockEntity#tick` and triggers on the first non-friendly player found.
- **Breaking conditions**: not a turret, so it breaks at **vanilla mining speed** and drops its own item when broken (D-13). It can be destroyed by explosions.
- **Stacking**: 64.
- **Notes**: ① The damage cap of 7 applies to all damage caused by that one explosion and must be clipped per victim after `Explosion` settles. ② Nausea 3 (60 ticks) applies to "every player in range" and **does not filter friend or foe**: teammates and the deployer themselves are hit just the same as long as they are inside the blast. This is **intentional design, not an omission** (D-27). ③ The "non-friendly" check is made at the instant of triggering, see §3.6.

---

### 4.3 Ubi Server

Item name: Ubi Server
English ID: `farmstorm:ubi_server` (seed item of the same name, itself plantable)
Corresponding vanilla crop: Potato
Type: Throwable
Category: Demolition, Damage
Function: throw it out and at the impact point produce an explosion with a power of 4 that deals no more than 2 damage
Item description: When you meet a Blitz, try throwing one at him

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot. The entity is destroyed after settling once at the impact point.
- **Trigger distance and metric**: a spherical explosion centred on the impact point (vanilla `Explosion` semantics), no additional metric is defined.
- **Breaking conditions**: the explosion destroys blocks per vanilla rules with no resistance threshold — this is the demolition workhorse of the set.
- **Stacking**: 16.
- **Notes**: power 4 and "damage no more than 2" are two independent constraints — the former decides the **destruction radius and knockback strength**, the latter only clips the **damage dealt to entities**. The damage cap must not be implemented by lowering the power.

---

### 4.4 Toxic Ubi Server

Item name: Toxic Ubi Server
English ID: `farmstorm:toxic_ubi_server` (seed item of the same name, itself plantable)
Corresponding vanilla crop: Poisonous Potato
Type: Throwable
Category: Buff, Demolition, Damage
Function: throw it out and at the impact point produce an explosion with a power of 2 that deals no more than 2 damage, and after the explosion generate a poison potion cloud at the impact point (equivalent to throwing a bottle of lingering Potion of Poison)
Item description: This one should really be able to solo a Blitz now

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot. The two stages (explosion + potion cloud) settle in order within the same tick.
- **Trigger distance and metric**: the potion cloud is a vanilla `AreaEffectCloud` with a Euclidean range of radius 3.0 (G-4).
- **Breaking conditions**: explosion power 2, blocks destroyed per vanilla rules.
- **Stacking**: 16.
- **Notes**: ① Potion cloud and explosion have a **fixed order**: explosion first, cloud second, so the cloud is not scattered by its own explosion. ② The cloud's effect particles and colour follow the Potion of Poison. ③ The cloud does not distinguish friend from foe and applies to every entrant (the original says "poison potion cloud" with no filtering wording). ④ Poisonous potatoes cannot be planted on their own in vanilla, so this crop **registers its own crop block and seed item** separately and does not share them with the potato (D-32).

---

### 4.5 Power Beet

Item name: Power Beet
English ID: `farmstorm:power_beet` (seed item `farmstorm:power_beet_seeds`)
Corresponding vanilla crop: Beetroot
Type: Edible
Category: Buff
Function: after eating, grants the user 30 seconds of Strength 2, Speed 2, Haste 2 and Resistance 1
Item description: Can you really become a genius after eating this

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot, consumes 1; the effect lasts 600 ticks.
- **Trigger distance and metric**: affects only the eater, no range check.
- **Breaking conditions**: not applicable.
- **Stacking**: 64.
- **Notes**: ① The four effects are applied at once, at Strength 2 / Speed 2 / Haste 2 / Resistance 1 respectively. ② Repeated eating refreshes the duration to 600 ticks, levels do not stack. ③ Nutrition 4 / saturation modifier 0.3, the same values as the Lucky Berry (D-29). ④ Usable neither in vanilla recipes nor as animal feed (D-09).

---

### 4.6 Melon Slice Bomb

Item name: Melon Slice Bomb
English ID: `farmstorm:melon_slice_bomb` (no seed item)
Corresponding vanilla crop: Melon Slice
Type: Throwable
Category: Damage
Function: throw it out and at the impact point produce an explosion with a power of 2 that cannot destroy blocks with an explosion resistance greater than or equal to -1, dealing at most 6 damage
Item description: Delicious to the point of exploding

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot.
- **Trigger distance and metric**: spherical explosion, no additional metric.
- **Breaking conditions**: **the original wording "blocks with explosion resistance greater than or equal to -1" is a typo**; per C-2 it is corrected to "blocks with explosion resistance ≥ cobblestone (6.0) cannot be destroyed", aligned with the Melon Grenade (D-19). The corrected threshold is exposed as `farmstorm.explosion.sliceResistThreshold`, default 6.0.
- **Stacking**: 16.
- **Notes**: ① The only entry outside the planting loop — a vanilla melon slice is not a seed item, so there is no seed form (D-09, §3.2.3). ② Obtainable through the converter from `minecraft:melon_slice`, and also generated by the Melon Grenade's impact effect. ③ The improved version is not edible (D-09).

---

### 4.7 Melon Grenade

Item name: Melon Grenade
English ID: `farmstorm:melon_grenade` (seed item `farmstorm:melon_grenade_seeds`)
Corresponding vanilla crop: Melon
Type: Throwable
Category: Demolition, Damage
Function: throw it out and one block directly above the impact point (with an x-axis and z-axis coordinate error of 0~0.3) spawn 4 melon slices in projectile state, then immediately produce an explosion with a power of 3 that cannot destroy blocks whose explosion resistance is greater than or equal to that of cobblestone, dealing at most 8 damage
Item description: Heavyweight damn-bomb

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot. Spawning and explosion complete within the same tick, in the fixed order "spawn first, explode second".
- **Trigger distance and metric**: spawn position = impact block position + `(Δx, +1, Δz)`, where `Δx` and `Δz` each independently take a positive value in `[0, 0.3]` (the original says "error of 0~0.3").
- **Breaking conditions**: blocks with explosion resistance ≥ cobblestone (6.0) cannot be destroyed (D-19); the threshold is exposed as `farmstorm.explosion.grenadeResistThreshold`.
- **Stacking**: 16.
- **Notes**: ① "Melon slices in projectile state" is implemented as 4 `farmstorm:melon_slice_bomb` projectile entities (registered in §7.12); these 4 entities **do not settle immediately** and must run the impact-point flow again according to their own ground collision. ② The damage cap of 8 only clips the damage of the main explosion, not the explosions of the subsequent melon slices. ③ The seed form is the melon stem structure, which produces improved melon blocks once planted.

---

### 4.8 Pumpkin Turret

Item name: Pumpkin Turret
English ID: `farmstorm:pumpkin_turret` (seed item `farmstorm:pumpkin_turret_seeds`)
Corresponding vanilla crop: Pumpkin
Type: Deployable (block top, block bottom. Block side, may float after the support block is destroyed)
Category: Turret, Damage
Function: fires shulker missiles at non-friendly players at a Chebyshev distance of 5 from it; on hit the missile deals 1 point of void damage and applies one Knockback 1 effect to that player, directed along the line from that pumpkin to the hit player (do not apply Levitation)
Item description: Kids, I can elbow-launch people now

**Implementation notes**

- **Attachment position**: **all three of top / bottom / side are supported** (all six faces).
- **One-shot / persistent**: persistent. Acquires a target and fires once every 60 ticks (3 seconds) (D-28).
- **Trigger distance and metric**: Chebyshev distance ≤ 5, origin the deployment point block position (D-20). Candidates are all non-friendly players in range.
- **Breaking conditions**: **fixed hard-mine of 10 seconds (200 ticks)** (D-13, D-37; the only block in the whole mod subject to hard-mine). Drops its own item when broken.
- **Stacking**: 64.
- **Notes**: ① The missile entity uses the vanilla `ShulkerBullet`; on hit it applies **1 point of void damage** (a custom damage source with `DamageSource#fellOutOfWorld` semantics) and **Knockback 1**, and **never applies Levitation** — this is the original's explicit negative requirement. ② The knockback direction is the horizontally normalized vector of "the line from the pumpkin to the hit player". ③ Floating: after the support block is destroyed it stays in place and keeps running. ④ **The only turret that keeps projectile interception**: it interacts with **projectiles in flight** crossing its range (C-3); all other turrets lock onto players only (D-07).

---

### 4.9 Awakened Torchflower

Item name: Awakened Torchflower
English ID: `farmstorm:awakened_torchflower` (seed item `farmstorm:awakened_torchflower_seeds`)
Corresponding vanilla crop: Torchflower
Type: Deployable (block top, may float after the support block is destroyed)
Category: Turret, Buff
Function: every 5 seconds grants friendly players within a radius of 20 blocks of the deployment point 15 seconds of Strength 3, Resistance 2, Haste 2, Speed 1, Water Breathing 1 and Night Vision 1; when a friendly player in range holds any sword, enchants that sword with Fire Aspect 1; every time the effect is granted, spawns a few flame particles around the player receiving it; this crop cannot be destroyed by non-friendly players
Item description: An ancient plant that gives people strength

**Implementation notes**

- **Attachment position**: block top only.
- **One-shot / persistent**: persistent. Refreshes every 100 ticks (5 seconds).
- **Trigger distance and metric**: **Euclidean radius 20**, origin the centre of the deployment point block (D-20). Friendly players are filtered within range.
- **Breaking conditions**: **the friendly side recovers it at vanilla normal mining speed** (D-14, C-8), **non-friendly players cannot mine it** (break progress always 0, which also takes effect in creative mode, D-36), **no explosion can destroy it** (D-14). This block is **not subject** to D-13's hard-mine rule (D-37).
- **Stacking**: 64.
- **Notes**: ① The six effects are applied at once, all for 300 ticks (15 seconds). ② Holding **any sword** additionally grants **Fire Aspect 1**, which is a **temporary enchantment**: removed automatically on leaving the range or on expiry, existing enchantments are not overwritten (D-15). ③ Each refresh spawns 8 flame particles around the affected player (G-7). ④ The category keeps "turret" (the original says so), but the implementation goes through `AuraProvider` and fires no projectile at all (C-4). ⑤ Floating: it stays in place after the support block is destroyed. ⑥ Forward conversion additionally consumes 1 Nether Star each time (D-12).

---

### 4.10 Awakened Pitcher Plant

Item name: Awakened Pitcher Plant
English ID: `farmstorm:awakened_pitcher_plant` (seed item `farmstorm:awakened_pitcher_plant_pod`)
Corresponding vanilla crop: Pitcher Plant
Type: Deployable (block top, may float after the support block is destroyed)
Category: Turret, Buff
Function: every 5 seconds grants friendly players within 10 blocks of the deployment point 15 seconds of Regeneration 2, Saturation 2 and Absorption 1, and removes all harmful effects from that player; every time the effect is granted, spawns a few heart particles around the player receiving it; this crop cannot be destroyed by non-friendly players
Item description: An ancient plant, said to heal everything

**Implementation notes**

- **Attachment position**: block top only.
- **One-shot / persistent**: persistent. Refreshes every 100 ticks (5 seconds).
- **Trigger distance and metric**: **Euclidean radius 10**, origin the centre of the deployment point block (D-20).
- **Breaking conditions**: the friendly side recovers it at vanilla normal mining speed (D-14, C-8), non-friendly players cannot mine it (break progress always 0, which also takes effect in creative mode, D-36), no explosion can destroy it (D-14). This block is **not subject** to D-13's hard-mine rule (D-37).
- **Stacking**: 64.
- **Notes**: ① The three positive effects all last 300 ticks (15 seconds). ② "Removes all harmful effects" covers every instance in `MobEffects` with `category() == HARMFUL`, and the clearing is limited to friendly players inside this aura's radius. ③ Each refresh spawns 8 heart particles (G-7). ④ The category keeps turret, the implementation goes through `AuraProvider` (C-4). ⑤ Floating: it stays in place after the support block is destroyed. ⑥ Forward conversion additionally consumes 1 Nether Star each time (D-12).

---

### 4.11 Fang Trap Cactus

Item name: Fang Trap Cactus
English ID: `farmstorm:fang_trap_cactus` (seed item of the same name, itself plantable)
Corresponding vanilla crop: Cactus
Type: Deployable (block top)
Category: Trap, Damage
Function: a one-shot crop; after deployment, when a non-friendly player is at a distance of 3 or less from the deployment point, generates two rings of fangs centred on the position of the deployment point. The two rings appear at different times: the outer ring is 8 fangs appearing together after a delay of 0.5 seconds; the inner ring is 5 fangs appearing together after a delay of 0.2 seconds
Item description: The prickliest episode

**Implementation notes**

- **Attachment position**: block top only.
- **One-shot / persistent**: one-shot. Triggering starts a timer; the block is removed once both fang rings have spawned, dropping no item.
- **Trigger distance and metric**: **Euclidean distance ≤ 3** — the only exception where §plan.txt§ gives no metric and the decision confirms one (C-5, D-20). Origin the deployment point block position.
- **Breaking conditions**: not a turret, so it breaks at vanilla mining speed and drops its own item (D-13).
- **Stacking**: 64.
- **Notes**: ① The **time gap between the two rings is deliberately inverted**: the inner ring appears first at 0.2 seconds, the outer ring follows at 0.5 seconds — copied from the original, not "corrected". ② Outer ring radius 2.0 blocks, inner ring radius 1.0 blocks (G-3); the points of each ring are spread evenly around the circle (outer ring quartered, inner ring fifths). ③ Fang entities use the vanilla `EvokerFangs`; damage and knockback follow vanilla. ④ The `triggered` flag is consumed immediately on trigger to prevent repeat triggering.

---

### 4.12 Bamboo Firecracker

Item name: Bamboo Firecracker
English ID: `farmstorm:bamboo_firecracker` (seed item of the same name, itself plantable)
Corresponding vanilla crop: Bamboo
Type: Throwable
Category: Damage
Function: throw this crop out; after it lands, at the impact point it spawns one wind charge every 5 game ticks and detonates it immediately, for 140 game ticks
Item description: Why is this fuse white — not good!

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot. Landing starts a 140-game-tick countdown; spawning stops when the countdown ends.
- **Trigger distance and metric**: the spawn point is the centre of the impact block; the wind charge's destruction and damage range are decided by the vanilla wind charge, no additional metric is defined.
- **Breaking conditions**: not applicable (produces no deployment block); the wind charge acts on blocks per vanilla rules.
- **Stacking**: 16.
- **Notes**: ① Converted directly: 140 ÷ 5 = **28 spawns**, and each wind charge **detonates immediately** after spawning without entering a flying state. ② The timer hangs on the "impact marker entity", not on the projectile entity — the projectile is destroyed once it settles. ③ If the chunk containing the impact point unloads during the countdown, the countdown pauses and resumes once the chunk is loaded (§7.5).

---

### 4.13 Red Mushroom Puff

Item name: Red Mushroom Puff
English ID: `farmstorm:red_mushroom_puff` (seed item of the same name, itself plantable)
Corresponding vanilla crop: Red Mushroom
Type: Throwable
Category: Buff
Function: throw this crop out; after it lands, gives every player within a radius of 4 blocks of the impact point Nausea 5 and Blindness 1 for 3 seconds
Item description: Eat it and you'll be lying on a plank

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot. Destroyed after settling once at the impact point.
- **Trigger distance and metric**: **Euclidean radius 4**, origin the impact point's exact position.
- **Breaking conditions**: not applicable.
- **Stacking**: 16.
- **Notes**: ① Nausea 5 (60 ticks) + Blindness 1 (60 ticks), both applied at once. ② The affected objects are "all players" and **friend or foe is not filtered**: teammates and the thrower themselves are hit just the same as long as they stand within 4 blocks. This is **intentional design, not an omission** (D-27). ③ It only applies effects and deals no damage, so it does not carry the damage category.

---

### 4.14 Brown Mushroom Puff

Item name: Brown Mushroom Puff
English ID: `farmstorm:brown_mushroom_puff` (seed item of the same name, itself plantable)
Corresponding vanilla crop: Brown Mushroom
Type: Throwable
Category: Buff
Function: throw this crop out; after it lands, gives every player within a radius of 4 blocks of the impact point Weakness 2 for 10 seconds and Slowness 2 for 5 seconds
Item description: This mushroom has no strength

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot. Destroyed after settling once at the impact point.
- **Trigger distance and metric**: **Euclidean radius 4**, origin the impact point's exact position.
- **Breaking conditions**: not applicable.
- **Stacking**: 16.
- **Notes**: ① Weakness 2 lasts 200 ticks (10 seconds) and Slowness 2 lasts 100 ticks (5 seconds); the two durations **differ** and must not be merged into one. ② The affected objects are "all players" and **friend or foe is not filtered**: teammates and the thrower themselves are hit just the same as long as they stand within 4 blocks. This is **intentional design, not an omission** (D-27). ③ It mirrors entry 13; both share one `RangeEffectThrowable` base class, with the parameters decided by the crop ID.

---

### 4.15 Lucky Berry

Item name: Lucky Berry
English ID: `farmstorm:lucky_berry` (seed item of the same name, itself plantable)
Corresponding vanilla crop: Sweet Berries
Type: Edible
Category: Buff
Function: after eating, there is a 15% chance to spawn a lightning bolt at the eater's position, a 25% chance to spawn 16 random improved crops around them, a 25% chance to gain Regeneration 5 for 5 seconds, a 25% chance to spawn 2 End City loot chests around the eater, a 5% chance of an explosion with a power of 1, a 3% chance to spawn a charged creeper around the eater, a 1.9% chance to give the eater 600 seconds of Strength 5, Resistance 3, Jump Boost 5 and Speed 2, and a 0.1% chance to switch the eater's game mode to creative mode, returning to the original game mode after 90 seconds
Item description: Playing it safe and just farming is boring, isn't it

**Implementation notes**

- **Attachment position**: not applicable.
- **One-shot / persistent**: one-shot, consumes 1; the result takes effect immediately, the only branch with a lasting state being the 0.1% one (90 seconds).
- **Trigger distance and metric**: spawned objects are placed in a random standable block within a radius of 2 blocks centred on the eater (G-5 / D-33); no hostile target filtering is involved.
- **Breaking conditions**: not applicable.
- **Stacking**: 64.
- **Notes**:
  1. **The probabilities of the 8 outcomes sum to exactly 100%** (15 + 25 + 25 + 25 + 5 + 3 + 1.9 + 0.1 = 100), so the check is a **single weighted draw with mutually exclusive outcomes**: each eating hits exactly one branch.
  2. The random pool of "16 random improved crops" is **the other 14 kinds, excluding the Lucky Berry itself** (D-17).
  3. The 0.1% creative mode branch: record the original game mode → switch to creative → **forcibly restore after 90 seconds (1800 ticks)**; if the player logs out midway the **effect is cancelled and not written to the save** (D-18).
  4. The four effects of the 1.9% branch all last 12000 ticks (600 seconds), at Strength 5 / Resistance 3 / Jump Boost 5 / Speed 2 respectively.
  5. The 3% branch spawns a **charged creeper** (a creeper empowered by lightning), not a normal creeper.
  6. §plan.txt§ writes this category as "增益类"; it is unified into **buff类** (§2.3.4, appendix A A-3).
  7. Nutrition 4 / saturation modifier 0.3, the same values as the Power Beet (D-29).

---

## 5. Crop registry name summary

Copied from §00-DECISIONS.md§ section 4.

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

1. For deployables the block ID and the item ID have the **same name** (items and blocks live in different registries, so identical names are allowed).
2. "Same name (itself plantable)" means the crop's seed form is the crop itself; no separate seed item is registered.
3. The Melon Slice Bomb is not plantable (a vanilla melon slice is not a seed item), so it has no seed form and takes no part in the planting loop.
4. Every item and block's fully qualified name uses `farmstorm:` as the namespace prefix; the table above lists short names only, for readability.
5. Per D-32 the Toxic Ubi Server registers its crop block and seed item separately and does not share them with the potato.

---

## 6. Advancement list draft

Every advancement hangs under `farmstorm:root`, forming a two-level tree. Icons use the corresponding improved crop items.

| # | ID | Display name | Parent | Trigger condition |
|---|---|---|---|---|
| 1 | `farmstorm:root` | Farmstorm (农场风云) | None (root advancement) | Obtain any one improved crop (a `farmstorm` namespace item) |
| 2 | `farmstorm:converter` | Install a Converter (装个转换器) | `farmstorm:root` | Place a Produce Converter |
| 3 | `farmstorm:first_conversion` | Two-Way (双向奔赴) | `farmstorm:converter` | Complete one conversion in the converter (either direction) |
| 4 | `farmstorm:nether_star_toll` | Price of a Star (星之代价) | `farmstorm:first_conversion` | Complete one "vanilla → awakened" Torchflower or Pitcher Plant conversion (consuming a Nether Star) |
| 5 | `farmstorm:seed_cycle` | Self-Sustaining Loop (闭环自给) | `farmstorm:first_conversion` | Harvest one matured improved crop and pick up the crop and the seed item at the same time |
| 6 | `farmstorm:first_throw` | Making a Move (出手) | `farmstorm:root` | Throw any one throwable improved crop |
| 7 | `farmstorm:first_deploy` | Placing the Piece (落子) | `farmstorm:root` | Deploy any one deployable improved crop |
| 8 | `farmstorm:demolition` | Unauthorized Demolition (未授权拆墙) | `farmstorm:first_throw` | Destroy at least 1 block with the explosion of a demolition crop |
| 9 | `farmstorm:turret_hit` | Don't Stand There (别站那儿) | `farmstorm:first_deploy` | Be hit once by a Pumpkin Turret missile |
| 10 | `farmstorm:aura_bath` | Aura Bath (光环洗浴) | `farmstorm:first_deploy` | Receive at least one aura effect from an Awakened Torchflower or an Awakened Pitcher Plant |
| 11 | `farmstorm:fang_dance` | The Prickliest Episode (最扎人的一集) | `farmstorm:first_deploy` | Trigger the Fang Trap Cactus once |
| 12 | `farmstorm:lucky_jackpot` | Playing It Safe Is Boring (老实发育，很枯燥吧) | `farmstorm:root` | Eat a Lucky Berry and draw the 0.1% creative mode branch |

Conventions:

- Every advancement is `"frame": "task"`, `"show_toast": true`, `"announce_to_chat": false` (to avoid chat spam).
- All advancement checks complete on the server, triggered through vanilla `criteria`; no custom commands are used (D-24).
- The `background` of `farmstorm:root` uses `minecraft:textures/block/hay_block_side.png`, consistent with the "farm" theme.

---

## 7. Exception and edge case handling list

### 7.1 Explosion protection (D-14)

- **Protected objects**: the two deployment blocks Awakened Torchflower and Awakened Pitcher Plant.
- **Rule**: no explosion (including this mod's demolition crops, vanilla creepers, TNT) can destroy them; non-friendly players cannot break them; the friendly side can mine and recover them normally.
- **Implementation**: return a value on `Block#getExplosionResistance` far above any explosion power, and cancel the event on `BlockEvent.BreakEvent` for non-friendly players.
- **Exception**: these two blocks are **not subject** to D-13's hard-mine constraint (D-37) — the friendly side mines at vanilla normal speed (C-8); for the non-friendly side they cannot be mined no matter what.

### 7.2 Ownership of already-deployed traps after a team change

- **Rule**: a deployment block **records only `ownerUuid`, not a team snapshot**. The friendly check resolves the owner's current team in real time at the instant of triggering.
- **Consequence**: after player A deploys a trap and then joins team X, teammate B walking into range **will not** trigger the trap; once A leaves the team, B triggers it again. This is the expected behaviour.
- **When the owner is offline**: their **last known team** takes part in the check — looked up from the `Scoreboard` team member table, without depending on whether the player entity is online.
- **The owner is completely deleted** (UUID absent from the save): treated as having no team.
- **The owner has no team at all**: per D-31's `self` rule, the friendly set = {the owner}, and every other player is treated as non-friendly (C-9).

### 7.3 One-shot traps when the player logs out / dies

- **Rule**: a deployment block is a **world object** and does not disappear when its owner logs out or dies. Player death does not drop already-deployed devices, which keep running according to their own state.
- **One-shot deployments**: while the owner is offline, if the trigger condition is met they **trigger as usual** — the criterion is "the players in range", independent of whether the owner is online.
- **Owner renames**: only affects the display of `ownerName`; the check goes through the UUID and is unaffected.
- **Sole exception**: the Lucky Berry's 0.1% creative mode effect is **cancelled and not written to the save** when the player logs out midway (D-18).

### 7.4 Blocks pushed by pistons

- **Rule**: every deployment block declares `PistonBehavior.BLOCK` and **cannot be pushed or pulled by a piston**.
- **Reason**: a deployment block carries the owner UUID, the support block position and the support face; once pushed, those three fields are all mismatched at once, and the correct semantics cannot be rebuilt after the push.
- **Reverse direction**: once the support block is pushed by a piston it is **no longer in its original position**, which is handled as "the support block was destroyed" (§7.9).

### 7.5 Chunk unloading

- **Rule**: deployment blocks and their `BlockEntity` are saved / loaded together with the chunk data and are not lost because of chunk unloading.
- **No ticking**: deployment blocks in unloaded chunks do not tick, so traps do not trigger, turrets do not fire and auras do not refresh.
- **Timers**: aura refresh timers, turret fire timers and the Bamboo Firecracker countdown **pause** when the chunk unloads and continue from the pause point once loaded (recorded as an "accumulated tick count", not an "absolute game time").
- **First tick after reloading**: missed refresh counts during the pause are not caught up; exactly one refresh is performed.

### 7.6 Creative mode players

| Scenario | Rule |
|---|---|
| A creative player triggers a trap | **Does not trigger**. Traps and turrets lock only onto non-friendly players who are **not in creative mode**, to avoid runaway spam |
| A creative player is covered by an aura | Receives the effects normally |
| A creative player throws | Spawns the projectile entity normally, **consumes no item** |
| A creative player deploys | Deploys normally, **consumes no item** |
| A creative player mines `pumpkin_turret` | Still subject to the 10-second hard-mine (D-13 has no exception clause) |
| A creative player mines the Torchflower / Pitcher Plant | Cannot mine it: `getDestroyProgress` returning 0 also takes effect in creative mode (D-36) |
| The 0.1% branch of the Lucky Berry | If the eater **was already in creative mode**, the branch becomes "no mode switch, only play the sound effect", and it still consumes this draw's outcome |
| Spectators | Do not trigger traps, are not locked onto, are not covered by auras |

### 7.7 Degraded behaviour without a team in single player

- **Rule**: when `farmstorm.teams.untamedFallback = "self"`, the "friendly" set of a teamless player = {itself}.
- **Consequence**: the traps and turrets they deploy take effect against **every player other than themselves**, including other players in a single-player world and friends temporarily invited into it.
- **The other rule**: setting the entry to `"public"` makes teamless players "on the same team as all teamless players", i.e. teamless players no longer trigger each other. Both rules must be selectable in config; the default is `self` (G-8).
- **Zero-player scenario**: `TeamResolver` never returns true for an empty set, any sweep yields 0 targets, and no exception is raised.
- **Single-player save**: with no other players present, traps and turrets short-circuit immediately at the sweep stage without computing any distance.

### 7.8 Invalid deployment point / illegal support block

| Scenario | Rule |
|---|---|
| The support block is not a full opaque block | Refuse deployment, play the failure sound, **consume no item** |
| The deployment point is not replaceable (a block is already there) | Refuse deployment, consume no item |
| The attachment position is not supported by that crop | Refuse deployment, consume no item |
| The support block is water / a waterlogged block | Refuse deployment |
| The support block is destroyed during this tick | Deployment fails and rolls back, consume no item |

### 7.9 Floating after the support block is destroyed

- **The three floating ones** (Pumpkin Turret, Torchflower, Pitcher Plant): after the support block disappears they **stay in place and keep running**, producing no drop at all.
- **The non-floating ones** (Blast Carrot, Fang Trap Cactus): the block is removed on the next tick, and `farmstorm.deploy.dropOnSupportLost` decides whether the item itself drops (drops by default, G-9).
- **The support block is replaced by another full block** (e.g. another block is placed on it): the support is considered still present and removal is not triggered.

### 7.10 A projectile crossing a chunk boundary / falling into an unloaded chunk

- The projectile entity itself lives in the entity chunk and advances with chunk loading; if the chunk the projectile is in unloads, the entity pauses along with it.
- If the chunk containing the impact point is **not loaded** at settlement time, the settlement is **discarded outright** with no deferred settlement — so that an explosion does not happen somewhere the player cannot see when the chunk loads.
- If the projectile flies out of the world border (`y < -64` or beyond the world height), it is handled as a timeout and settles once at the last known position.

### 7.11 Priority of block replacement and destruction

When several effects act on the same block at once, the priority from high to low is:

1. **Explosion immunity** (Torchflower / Pitcher Plant, D-14) — highest, no effect can destroy them.
2. **Explosion resistance threshold** (Melon Slice Bomb 6.0, Melon Grenade 6.0) — blocks with resistance ≥ the threshold are immune to that explosion.
3. **Vanilla explosion rules** (Ubi Server power 4, Toxic Ubi Server power 2, Lucky Berry power 1).
4. **Block replacement** (the Puffed Wheat's hay block write) — **checks nothing**, neither explosion resistance nor immunity, and overwrites the target block directly.
5. **Wind charge destruction** (Bamboo Firecracker) — per vanilla wind charge rules.

**Conflict between the Puffed Wheat and deployment blocks**: the Puffed Wheat's replacement is a "write", not an "explosion", so it **does overwrite** deployment blocks in range (including the Torchflower / Pitcher Plant). An overwritten deployment block **drops no item**.

### 7.12 Ownership of the melon slices spawned by the Melon Grenade

- The impact point spawns 4 `farmstorm:melon_slice_bomb` projectile entities (not vanilla melon slices, per D-09's "improved crops no longer have their vanilla uses").
- The owner = the Melon Grenade's thrower, used for later kill attribution and team checks.
- These 4 entities **do not settle immediately**: each lands according to physics and then goes through the standard throwable impact-point flow.
- If all 4 entities spawn inside the same block and overlap, overlapping is allowed and no dispersal is applied — the original only constrains the horizontal offset range.

### 7.13 Special handling of the Lucky Berry creative mode branch

| Scenario | Rule |
|---|---|
| Normal flow | Record `previousGameMode` → switch to `CREATIVE` → forcibly restore after 1800 ticks |
| Player logs out midway | **Cancel the effect, do not write it to the save** (D-18); the original game mode is kept on the next login |
| Player dies midway | Stay in creative mode until the timer ends (creative mode cannot die) |
| Player is already a spectator when the timer ends | Forcibly restore to the recorded original game mode |
| Server restart | The timer record is **not persisted**; after a restart it counts as cancelled and the player keeps their current mode |
| The same player hits the branch again | Reset the timer to 1800 ticks, no stacking |

### 7.14 Converter block destroyed / residue in the input slot

- When the converter is destroyed, every item in the input slot **drops** as an item entity (no item is lost).
- Destroying it does not refund already-spent experience points.
- An interrupted conversion (the player closes the GUI) produces no partial conversion: conversion is an atomic "settles on take-out" operation.

### 7.15 Multiple auras stacking and temporary enchantment conflicts

- **Multiple auras stacking**: when one player is inside several auras at once, **each refreshes independently**. For effects of the same name the level takes the **highest** and the duration the **longest**; levels do not stack.
- **Temporary enchantment**: the Torchflower's Fire Aspect 1 is a temporary enchantment. If a player is inside two Torchflowers' ranges at once it is applied only once. It is removed on leaving all ranges or on expiry, **keeping the existing enchantments** (D-15).
- **Existing Fire Aspect**: if the sword already has Fire Aspect II, the temporary enchantment neither downgrades nor overwrites it; the original value is kept.
- **Switching the held item**: the aura re-checks the held item on every refresh; switching away from the sword removes the temporary enchantment.

### 7.16 Server authority and client prediction

- **No prediction**: all deployment, triggering, explosions, effect application and experience deduction are decided by the server; the client does not predict any of them.
- **Particles and sounds**: throwable settlement, aura refresh and turret fire notify the client through **vanilla** particle packets and sound packets (C-6); the client only plays the presentation.
- **Packet loss tolerance**: at worst a lost client packet means one missing presentation; it affects no state.

### 7.17 Boundaries of stacking and consumption

| Scenario | Rule |
|---|---|
| Throwing the last one | Throws normally, the item slot is emptied |
| Creative mode throwing / deploying / eating | Consumes no item |
| Deployment fails | Consumes no item |
| Insufficient experience for conversion | No conversion, no experience deducted, no level deducted |
| Experience runs out during a bulk conversion | Results already converted are granted, the remaining items stay in the slot |
| Stacking above the limit | When config lowers the stack limit, the excess overflows into the inventory at the next stack merge; no forced splitting |

### 7.18 Game rules and event cancellation

- Explosions that affect blocks all go through the vanilla `Explosion`, and therefore obey `mobGriefing`, `doTileDrops` and other game rules automatically.
- With `mobGriefing = false`, no block-destroying explosion destroys blocks, but **damage still applies as usual**.
- `keepInventory` does not affect deployment blocks (they are not player drops).
- Breaking a deployment block goes through `BlockEvent.BreakEvent` and can be cancelled by other mods; when cancelled the deployment is treated as still present.

---

## Appendix A · Register of contradictions and gaps in §plan.txt§ and their resolutions

| # | Contradiction / gap in §plan.txt§ | Resolution | Basis |
|---|---|---|---|
| A-1 | The total entry count is not stated; it is actually **15** entries (lines 33~156), and was once understood as 16 | Fixed at 15; the Lucky Berry random pool is 14 kinds accordingly | C-1, D-17 |
| A-2 | The Melon Slice Bomb "cannot destroy blocks with explosion resistance greater than or equal to **-1**", inconsistent with the Melon Grenade's "≥ cobblestone" | Corrected to "≥ cobblestone (6.0)", the two are unified | C-2, D-19 |
| A-3 | The Lucky Berry's category is written "**增益类**", while the six-category list on lines 14~20 has no such category | Unified into **buff类** | §2.3.4 |
| A-4 | The Torchflower / Pitcher Plant are classified as "**turret**", yet no attack behaviour appears anywhere in the text | Keep the category label (player-facing readability), implement through `AuraProvider` | C-4 |
| A-5 | The Fang Trap Cactus trigger distance gives no metric | Fixed as **Euclidean distance ≤ 3**, the only exception to the Chebyshev specification | C-5, D-20 |
| A-6 | The category definition text mentions "增益类", but the category list has only 6 items and does not include that name | Same as A-3 | §2.3.4 |
| A-7 | The "Type" field mixes in the attachment position (e.g. "Deployable (block top)"), while type ought to be a single-valued enum | Type keeps only the four-value enum; the attachment position moves into the entry attributes and the "implementation notes" | §2.2, §3.3 |
| A-8 | The field name of entry 15 is written "**物品名**:" while the other entries write "物品名称:" | Unified as "Item name:" throughout | §4.15 |
| A-9 | The template has **no "English ID" row** | Added per the project specification, mapping in §5 | §5 |
| A-10 | Poisonous potato and potato share the same vanilla crop block, so the one-to-one correspondence of "each crop corresponds to one improved crop" does not hold | Register a crop block and a seed item separately for the Toxic Ubi Server | §3.2.3 · **confirmed by D-32** |
| A-11 | Melon slice has no seed form, conflicting with the general statement "improved crops all have a seed form" | The Melon Slice Bomb is the sole exception and takes no part in the planting loop | §3.2.3 |
| A-12 | "Plantable" (item 12) is **used by none** of the 15 entries | v0.0.1 keeps only the interface and the specification, as a cross-cutting capability | D-10 |
| A-13 | The Pumpkin Turret says "Chebyshev distance **is 5**" without stating whether it means "equal to" or "not greater than" | Interpreted as the square neighbourhood of Chebyshev radius 5 (≤ 5) | §3.7 clause 4 |
| A-14 | The Melon Grenade says "spawn 4 **melon slices** in projectile state" without stating vanilla or improved | Improved, `farmstorm:melon_slice_bomb` | D-09, §7.12 |
| A-15 | The Blast Carrot and the two mushroom puffs say "**every player** in range", differing from the trap category's "non-friendly only" | Copy the original wording: the effect range **does not filter friend or foe**; friend/foe filtering applies only to the trigger condition | §3.5, §3.6 · **confirmed by D-27** |
| A-16 | The 8 outcomes of the Lucky Berry sum to exactly 100%; the original does not state whether they are exclusive or independent checks | Decided as a single weighted draw with mutually exclusive outcomes | §4.15 |
| A-17 | The whole text gives no **conversion cost** | Filled in by decision: 3 experience points each, and 1 Nether Star extra for forward Torchflower / Pitcher Plant conversion | D-12 |
| A-18 | The whole text gives no **stack limit** | Filled in by decision: throwables 16, everything else 64 | D-16 |
| A-19 | The whole text gives no breaking rules other than **turret hard-mine** | Filled in by decision: only `pumpkin_turret` has a fixed hard-mine of 10 seconds (D-37); the Torchflower / Pitcher Plant are vanilla speed for the friendly side + explosion immunity + unbreakable for the non-friendly side | D-13, D-14 · **corrected by C-8** |
| A-20 | Line 5 mentions "consider Farmer's Delight compatibility in later development" | Explicitly deferred, not implemented in v0.0.1 | Deferred item 1 |
