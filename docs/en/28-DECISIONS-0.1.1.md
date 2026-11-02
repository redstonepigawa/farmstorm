# 28 · v0.1.1 Decision Record

> Project: Farmstorm · v0.1.1
> Basis: `plan0.1.1.txt` (5 original requirements) + the 25 author rulings of §2 of `.work/v011-BRIEF.md`
> Status: **the single source of truth**. Where anything conflicts with this document, this document wins; where this document conflicts with the source code, it is reported to the author and no trade-off is made on one's own.
> Coverage: **D-124 ~ D-147, 24 entries in total**, consecutive with no gaps. The additions / changes / deletions against 0.1.0 are registered in §3, the change of the red-line convention in §4, and the comparison with the superseded 0.1.0 clauses in §5.

---

## 0. Reading conventions

### 0.1 Numbering

The decision numbering of this document starts at **D-124** (`22-DECISIONS-0.1.0.md` of 0.1.0 goes up to D-123). The other numbering series are **only cited here, never opened anew**; the continuation table is copied verbatim from §0.2 of `BRIEF`:

| Series | 0.1.0 up to | **This document from** | Written by |
|---|---|---|---|
| Decisions D-xx | D-123 | **D-124** | this document (28) |
| Tasks F-xx | F-175 | **F-176** | 31 |
| Phase | 25 | **26** | 31 |
| Milestones M-xx | M21 | **M22** | 31 |
| Hard constraints HC-xx | HC-76 | **HC-77** | 32 |
| Prohibitions N-xx | N-74 | **N-75** | 32 |
| Risks RT-xx | RT-30 | **RT-31** | 30 |
| Adjudications A-xx | A-29 | **A-30** | 31 |
| Implementation verdicts V-xx | V-190 (`.work/PLAN.md`) | **V-191** | `.work/PLAN.md` |
| Document number | 27 | **28** | this document |

Three numbering rules:

1. This document issues **D-xx** only. When another series is cited the full prefix is written ("RT-33", "HC-77", "F-176"); no number is invented here, and no downstream number is spent in advance.
2. The continuation table comes from §0.2 of `BRIEF`; this document neither recomputes it nor sets up a second one.
3. This document covers **D-124 ~ D-147**. D-103 ~ D-123 of 0.1.0 still follow `22-DECISIONS-0.1.0.md`, **unless §5 names them as superseded**; every clause §5 does not name continues in force.

### 0.2 Glossary

This section gives each term its **decision boundary**, not a circle of synonyms. When the six downstream documents meet these words, they are used by the boundary of this section.

**Pumpkin Turret** (man瓜)
: equal to **`farmstorm:pumpkin_turret`** (the same id for the item / block / block entity). Decision boundary: it means this one registry object only. The other three objects containing "melon" — `melon_grenade` (Melon Grenade), `melon_slice_bomb` (Melon Slice Bomb), `melon_grenade_stem` (瓜！茎) — are **not** the Pumpkin Turret. In documents it is always written "Pumpkin Turret (`farmstorm:pumpkin_turret`)", and no alias such as 南瓜炮台 may replace the id (`BRIEF` §1.7, §6 item 5; ruling #4).

**Outpost Core**
: equal to the **placed block instance** of **`farmstorm:outpost_core`** and its block entity. Decision boundary: when "the core" is said it means that one block in the world, not the item form (the item form is always written "the core item"); "the core owner" means the placer's UUID, which is not the same thing as "the players currently online" (the core owner may be offline). Basis: `BRIEF` §1.2.

**Ally**
: equal to "**the core owner himself + same-team allies**", the team convention following the vanilla scoreboard team. Decision boundary: it is judged by **UUID / name**, **regardless of whether the target is online**; mobs are always treated as "non-ally" (mobs have no team affiliation); creative / spectator have their own existing exclusions on the targeting and triggering surfaces, but those exclusions **are not part of** the definition of "ally". Basis: ruling #17, the bug 2 scene of `BRIEF` §1.3.

**Per-core capacity**
: the number of farmland blocks one core can manage at most; the value = `outpost_core.farmlandBonus` (default **16**). Decision boundary: capacity is a **binding allowance**, its unit being "a plot"; it is not equal to "the quota" — the quota is "the total number of plots the allies as a whole may till", and in this release it is obtained by summing the capacities (D-139). The two share one source (the same config key) but are not the same meaning, and documents must keep them apart. Basis: ruling #12, `BRIEF` §3.4, RT-33.

**Farmland binding**
: the persistent relation between one farmland block and **exactly one** core. Decision boundary: what is bound is "the core's position + dimension", not the core owner; one plot has only **0 or 1** binding at any one moment, **never 2** — this is the fundamental difference from the "range coverage" of 0.1.0 (under 0.1.0 one plot could fall inside the 3×3 of several cores at once). A farmland block with no binding still exists and can still be planted, it is simply **not protected**. Basis: rulings #12, #15.

**Migration**
: when a core is destroyed, **rebinding** the farmland in its name to another core. Decision boundary: what migrates is the **binding relation**, not the block itself; migration happens only when all three hold at once — "the successor core's influence range covers that farmland + it is allied with the farmland's owner + the successor core still has a free slot"; if one does not hold it **turns to dirt** (this is the only branch that touches the world). Basis: ruling #14.

**Q-Bounce**
: equal to the status effect **`farmstorm:q_bounce`** (`HARMFUL`, `0xFF6EC7`). Decision boundary: it means this MobEffect only; it does not mean any visual presentation, not the bounce height, not the "seven-segment input lock"; this release does not change its amplifier (still 0), does not change the existing fact that it does not rewrite `tick`, and does not read config. Basis: `BRIEF` §1.8.

**HUD overlay** (the overlay)
: equal to the one element "**the self-drawn core list in the top-left corner of the screen**". Decision boundary: it is drawn by this mod itself (`RenderGuiEvent.Post`) and can be switched on and off and dragged; it is **not** the vanilla boss bar, **not** the scoreboard sidebar, **not** the chat box, **not** the charge bar of `WildHunterHud`. It shows **all** the cores of oneself + teammates — **including unloaded chunks and other dimensions** (D-143); it does not show enemy cores and does not show core items. Basis: rulings #19 ~ #21.

### 0.3 The four-field body of this document

Every decision has four fixed items, **none may be missing**, in a fixed order:

| Field | What is written | What must not be written |
|---|---|---|
| **Conclusion** | one sentence, imperative, directly executable | conditional clauses, alternatives, "could consider" |
| **Basis** | the ruling number of §2 of `BRIEF`, or the `file:line` of §1; giving both is best | "because it is reasonable", "intuitive", "industry convention" |
| **Landing site** | the `path` or `class#method` to change | code snippets, algorithm steps |
| **Notes** | **what this entry affects in passing** and **which alternatives it rejects** | implementation details (that is 30's job), task scheduling (that is 31's job) |

Four writing disciplines:

1. **The "supplemented by this document" marker**: every convention derived by this document rather than given directly by `BRIEF` is marked **supplemented by this document** in front of the sentence. An entry carrying this mark can be overruled by the author; an entry without it is a transcription of `BRIEF`, and overruling it amounts to overruling an author ruling.
2. **Number discipline**: every value in this document traces back to one item of §1 / §2 / §3 of `BRIEF`, or is arithmetic on two given values (such as 25 − 1 = 24, 0.45 × 20 = 9). Where a **new value** is needed, it is written as "fixed by 29 within the framework of D-xxx of this document, subject to the int / double value range of the config"; this document does not invent one.
3. **Forbidden wording**: no open items, no to-do markers, no conclusions with vague conditions; no second-person honorific — always "the author".
4. **Relation to the source code**: where this document conflicts with the source code, it is reported to the author and no trade-off is made on one's own. The conflicts found while writing, and the places `BRIEF` does not cover, are listed item by item in the "writing-time report" at the end (see the end of §7).

---

## 1. Decision list (D-124 ~ D-147)

The numbers this version actually uses are **D-124 ~ D-147, 24 entries in total**, consecutive with no gaps. Grouping: §1.1 version and textures 3 entries, §1.2 Pumpkin Turret 3 entries, §1.3 Compressed Hay Block 1 entry, §1.4 Friend Chives 3 entries, §1.5 farmland and the Outpost Core 8 entries, §1.6 HUD overlay and run time 4 entries, §1.7 red lines and the release surface 2 entries.

### 1.1 Version and textures (D-124 ~ D-126)

#### D-124 · Version number 0.1.1 and the delivery form: six Chinese + six English + a README increment

- **ID**: D-124
- **Conclusion**: change `mod_version` to **0.1.1**; this release delivers **six Chinese documents (28 ~ 33) + six English ones (the corresponding 28 ~ 33) + a README increment**, opens no seventh document, and does not touch 22 ~ 27.
- **Basis**: §2 item 1 of `BRIEF` (0.1.1 / document numbers 28~33 / numbering continuation see §0.2); `gradle.properties:11` currently reads `mod_version=0.1.0` (`BRIEF` §1.10); the six-way division-of-labour table of `BRIEF` §5 and the order "Chinese fully frozen → translated into English as a whole".
- **Landing site**: `gradle.properties:11`; `docs/zh/28-DECISIONS-0.1.1.md` ~ `docs/zh/33-PROMPT-TASKS-0.1.1.md` (6 new); `docs/en/28-DECISIONS-0.1.1.md` ~ `docs/en/33-PROMPT-TASKS-0.1.1.md` (6 new); `README.md` (the increment section, including the red-line section, see D-146).
- **Notes**: the six English documents are translations of the Chinese, **not a second source of truth** — where they conflict with the Chinese, Chinese document 28 wins. The version number has only one source, `gradle.properties:11`; the other documents cite it and do not each declare it (otherwise the jar name of D-147 would come adrift from the version number). Documents 22 ~ 27 of 0.1.0 stay frozen (`BRIEF` §6 item 3), and this release patches none of them. Rejected: "this release merges the six into one big document" — `BRIEF` §5 has already cut the division of labour of 28 ~ 33 apart (source of truth / design / technical / tasks / master prompt / phase prompts), and merging would void the constraint "cite only 28, invent nothing yourself".

#### D-125 · The "green +" rule is voided globally, but this release regenerates only 1 image

- **ID**: D-125
- **Conclusion**: **void globally** the rule "stack a green `+` in the bottom-right corner of an item texture", and delete the stacking mechanism from the generator; **this release regenerates `wild_apple` only, a single image**, and the other 24 keep their current images untouched.
- **Basis**: §2 item 2 of `BRIEF`; the rule's original text `plan0.0.1.txt:8`, the voiding statement `plan0.1.1.txt:10`; the carriers `tools/gen_textures.py:274-275` (`NO_PLUS_ICONS = {"converter"}`, the only one of the 26 exempt from stacking), `:407` (`add_plus`), `:546-560` (`build_marker()` draws a 7×7 cross, 33 px in total), `:612-623` (stamped into the bottom-right corner at `left=9 / top=9`), `:974-975` (the composition branch); on the Java side **0 places** (no `ItemDecorator` / `overrides` / texture overlay, `BRIEF` §1.1); the current images measured already carry the 33 px `#3FA34D` cross (`BRIEF` §1.1).
- **Landing site**: `tools/gen_textures.py` (`NO_PLUS_ICONS` / `add_plus` / `build_marker` and the composition branch); `assets/farmstorm/textures/item/wild_apple.png`.
- **Notes**: the voiding is at the **rule level**, not "no icon of this release carries a +". Of the 26, 25 carry the `+` (only `converter` is exempt from stacking), so after this release's handling **24 are still the old look** (25 − 1 = 24), registered in §7 item 1. The generator is the only carrier, so re-running after deleting the mechanism will not put the `+` back — which is also why this entry must touch code and cannot just swap PNGs. Not chosen: "this release regenerates all 25 at once" — ruling #2 promises 1 only, and batch regeneration belongs to the next release; changing 25 images at once would make "the visual change of this release" impossible to accept image by image.

#### D-126 · Wild Apple switched to the 64×64 original image, used as is

- **ID**: D-126
- **Conclusion**: `textures/item/wild_apple.png` is replaced outright by the output of that **64×64** source image as it is (**no downsampling, no more 16-grid modal-colour picking**); the generator's default source-image path is changed to that file.
- **Basis**: §2 item 3 of `BRIEF`; `plan0.1.1.txt:9-10`; the §1.1 measurements — the source image `wild_apple_64x64.png` is **6,946 B / 64×64 / 1814 deduplicated RGBA / 1471 fully transparent / 2625 opaque / 0 semi-transparent / alpha bbox x∈[2,60] y∈[0,63]**, and is **not** the 4× nearest-neighbour upscale of any 16×16 image (204 of the 256 4×4 blocks are uneven); the current image is **571 B / 16×16 / 88 deduplicated RGBA / 107 fully transparent**; the generator `tools/gen_textures.py:59` (`TEXTURE_SIZE = 16`), `:154` (`WILD_APPLE_SOURCE` defaults to the old desktop image), `:849-881` (`wild_apple_grid()` 16-grid modal colour picking), `:884-895` (`draw_wild_apple()`); the model `models/item/wild_apple.json:1-6` (`parent=minecraft:item/generated`, `layer0=farmstorm:item/wild_apple`, no overrides).
- **Landing site**: `assets/farmstorm/textures/item/wild_apple.png` (replaced in place, §3.2 row 4); `tools/gen_textures.py:154` (the default source image) + `:884-895` / `:849-881` (`wild_apple` takes the pass-through branch and no longer goes through 16-grid colour picking).
- **Notes**: the model's `layer0` reference, the item id, the language keys and the registry surface are all untouched, and no second texture is added. It must go into the rendering facts of 29 / 30: **the displayed area in the inventory is still 16×16**; 64×64 is only pixel density, and the 4× finer detail is averaged away by the display size (RT-37); re-running the generator must not put this image back to 16×16, which is why the generator and the image have to change in the same release. Rejected: "keep 16-grid colour picking + hand-fix the details" — the 88 colours of the current image are exactly the product of that picking, and what the author wants is the original image; also rejected: "scale down to 32×32 as a compromise" — the author's words in `plan0.1.1.txt:10` are "use the original image directly as the texture".

### 1.2 Pumpkin Turret (D-127 ~ D-129)

#### D-127 · The Pumpkin Turret's identity is fixed as `farmstorm:pumpkin_turret`; fire interval 60 → 40

- **ID**: D-127
- **Conclusion**: in documents the Pumpkin Turret (man瓜) always means **`farmstorm:pumpkin_turret`**; `pumpkin_turret.fireIntervalTicks` default **60 → 40 ticks** (2 seconds).
- **Basis**: §2 items 4 and 5 of `BRIEF`; `plan0.1.1.txt:26` ("the default attack interval shortened to 2 seconds"); §1.7 — the Chinese literals at `zh_cn.json:57/58/287/288` are exactly "man瓜", there are 4 objects containing "melon" in all, and the original requirement of `plan0.0.1.txt:91-97` also writes "man瓜 / corresponding vanilla crop: pumpkin"; `config/FarmstormConfig.java:431-433` (default 60, range 1–72000); read by `PumpkinTurret.java:80-82`; effective at `TurretBehavior.java:26-32` and `DeployedCropBlockEntity.java:146-149`; the scan interval `TurretBehavior.java:13-16` defaults to 10 ticks.
- **Landing site**: `config/FarmstormConfig.java:431-433` (the default value); `behavior/impl/PumpkinTurret.java:80-82` (reads config, untouched); the document surface: the identity wording throughout 28 ~ 33.
- **Notes**: the interval is **cooldown** semantics: during the cooldown not even targeting is done (`DeployedCropBlockEntity.java:146-149`), so 40 ticks also raises the targeting frequency and the total number of projectiles — which is exactly why D-128 and D-129 must be tuned together (with the interval shortened and neither the damage nor the projectile speed changed, it would amount to a one-sided buff). 40 is a **default value**, not a hard one: an already-persisted `farmstorm-server.toml` still holds 60, see D-147 for the upgrade note. Rejected: "write the Pumpkin Turret as 南瓜炮台 without the id" — `BRIEF` §6 item 5 forbids it outright, and the mod has more than one object named "pumpkin".

#### D-128 · Pumpkin Turret damage 1 → 3

- **ID**: D-128
- **Conclusion**: `pumpkin_turret.projectileDamage` default **1 → 3**; the damage type is still `farmstorm:void_bolt`, and the knockback level `knockbackLevel` default 1 stays as it is.
- **Basis**: §2 item 6 of `BRIEF`; `plan0.1.1.txt:26` ("the damage is 3 points"); §1.7 — `config/FarmstormConfig.java:425-427` (default 1, range 0–100), `FarmstormDamageTypes.java:20` (`void_bolt`), the application point `TurretBullet.java:106`, knockback `:110-117` (`knockbackLevel` default 1, `FarmstormConfig.java:428-430`).
- **Landing site**: `config/FarmstormConfig.java:425-427` (the default value).
- **Notes**: 3 is the damage of **each shot**, not the DPS. Read together with the interval of D-127: 1 point / 60 ticks before, 3 points / 40 ticks after, and the theoretical output converted per tick is **4.5 times** ((3 ÷ 40) ÷ (1 ÷ 60) = 4.5, **supplemented by this document**: arithmetic, both numbers coming from rulings #5 and #6). So the number table of 29 must put the three rows "interval + damage + projectile speed" together and must not write the damage alone, otherwise the reader would think it was buffed only 3-fold. Rejected: "change the damage to 3 but keep the 60-tick interval" — a direct conflict with the 2 seconds of ruling #5.

#### D-129 · Missile speed 0.6 → 0.45 promoted to config, and the close-range circling fixed (**no soft homing introduced**)

- **ID**: D-129
- **Conclusion**:
  1. delete the hard-coded constant `TurretBullet.SPEED = 0.6` and read the new config key `pumpkin_turret.bulletSpeed` instead (double, default **0.45**, that is 9 blocks per second);
  2. homing gets **only the close-range convergence** fix: when the target is closer than one step (`dist < SPEED`), converge by the remaining distance or settle a hit directly, eliminating the circling / jitter caused by turning around every tick at point-blank range;
  3. **no soft homing is introduced**: no turn-rate cap, no maximum chase duration, no guidance parameters, no change of the entity superclass.
- **Basis**: §2 items 7 and 8 of `BRIEF`; `plan0.1.1.txt:27`; §1.7 — `TurretBullet.java:28` (`SPEED = 0.6` hard-coded, no config), `:73-87` (`steerTowards` every tick on the server while the target is alive), `:90-96` (normalized × `SPEED`, **reset directly every tick**, no turn-rate cap, no maximum chase duration), `:43-51` (both constructors call `setNoGravity(true)`), `FarmstormEntityTypes.java:60-66` (registered with `clientTrackingRange(4)` / `updateInterval(10)`); `BRIEF` §3.4 (the new key `bulletSpeed` default 0.45); `homing` has 0 hits in the same repository and no entity extends `ShulkerBullet` / `AbstractHurtingProjectile` (the last paragraph of §1.7).
- **Landing site**: `entity/TurretBullet.java:28` (delete the constant), `:90-96` (close-range convergence); **`behavior/impl/PumpkinTurret.java:75`** (the launch point: `bullet.setDeltaMovement(direction.scale(TurretBullet.SPEED))` changed to read the same config key); the `pumpkin_turret.bulletSpeed` of `config/FarmstormConfig.java` (`BRIEF` §3.4, a new key); the language key `farmstorm.configuration.pumpkin_turret.bulletSpeed` (`BRIEF` §3.5).
- **Notes**: **this is a head-on conflict with the plan's original text** — the plan wants "reference the homing effect of the vanilla shulker missile, preferably reusable directly in code" (`plan0.1.1.txt:27`), and ruling #8 re-adjudicated it into "introduce no soft homing, fix only the close-range circling / jitter". So "reuse `ShulkerBullet`" is rejected (§6 item 8). The two changes must be made together: lowering only the speed turns "cannot hit" into "cannot hit more slowly"; fixing only the convergence without changing the speed leaves the original hard-coded step at normal ranges. **Deleting the constant is not a single-point change**: this constant has two readers — the per-tick reset of `TurretBullet.java:90-96` and the muzzle velocity of `behavior/impl/PumpkinTurret.java:75` — and changing only the former fails to compile outright; both must read `pumpkin_turret.bulletSpeed` (A-30). Promoting the speed to config does not change server authority — the homing path is still server-side only (the class comment at `TurretBullet.java:23-25`, HC-03). The value range of the speed is pinned down by 30 within the double range of `config`; this document gives only the default 0.45.

### 1.3 Compressed Hay Block (D-130)

#### D-130 · Compressed Hay Block sound GRASS → STONE

- **ID**: D-130
- **Conclusion**: the `SoundType` of `compressed_hay_block` goes from `GRASS` to **`STONE`**; hardness 30 / blast resistance 6 / `requiresCorrectToolForDrops()` / `MapColor.COLOR_BROWN` all stay as they are.
- **Basis**: `plan0.1.1.txt:20-23` (the original text of requirement 3: "change the sound effect of the compressed hay block to a stone-like one"); `BRIEF` §3.2 row 3 (`FarmstormBlocks.java:79` `SoundType.GRASS` → `STONE`); §1.6 — across the whole repository `SoundType` appears with only 3 values (`GRASS` `:79` / `STONE` `:88`, `CROP` `:100,:110`), and the only precedent for `STONE` is `outpost_core`.
- **Landing site**: `registry/FarmstormBlocks.java:79`.
- **Notes**: the sound is a **block property** affecting the five kinds of sound **break / place / step / mine / land** (the five getters of `SoundType`: `getBreakSound` / `getPlaceSound` / `getStepSound` / `getHitSound` / `getFallSound`), and it affects neither **mining time** nor the drops or the hardness; **so the acceptance items are 2 more than the original estimate** (29 has already raised the acceptance items from 3 to 5, and the two sides must agree) (A-31). Knock-on: the D-57 row of §7.2 of document `22` says "`sound(GRASS)` untouched", and this release voids that half-sentence (§3.2 row 11), but the original text of 22 stays frozen and unedited. ⚠️ **This one is not among the 25 rulings** — its basis is only the plan's original text and `BRIEF` §1.6 / §3.2, and this document fills it in as an independent decision, otherwise the six downstream documents would collectively miss requirement 3 of the plan (see item 1 of the writing-time report at the end of §7). Rejected: "change it to `CROP`" — `CROP` is the sound of crop blocks, the exact opposite of "stone-like".

### 1.4 Friend Chives (D-131 ~ D-133)

#### D-131 · Friend Chives goes from "one-shot on landing" to a field lasting `effectTicks` ticks (default 200)

- **ID**: D-131
- **Conclusion**: after landing, `FriendChivesProjectile` becomes **a field that lasts `friend_chives.effectTicks` ticks** (default **200**, range 20–1200): `keepAliveAfterImpact()` is overridden to return `true`; a new `tick()` is added that **every tick** scans the `LivingEntity` inside a sphere of radius 6 blocks around the landing point (Euclidean distance, height difference included) and applies **`effectTicks − x`** ticks of Q-Bounce to each target (x = the ticks elapsed since landing); at the end of tick `effectTicks` the entity `discard()`s itself.
- **Basis**: §2 item 9 of `BRIEF`; `plan0.1.1.txt:31`; §1.8 — `ThrownCropEntity.java:95-104` (`settle()` → `onImpact()` → **`discard()` if `!keepAliveAfterImpact()`**), `:116-118` (default `false`, the only overrider being the firecracker), `:65-72` (**once settled, the lifetime cap no longer applies**); `FriendChivesProjectile.java:56-80` is currently one-shot on landing and the whole class **overrides no `tick()`**; `friend_chives.hitRadius` default **6.0** (0–16, `FarmstormConfig.java:851-853`), `effectTicks` default **200** (20–1200, `:854-856`); Q-Bounce `effect/QbounceEffect.java` (`farmstorm:q_bounce`, the amplifier literal 0, `FriendChivesProjectile.java:63`).
- **Landing site**: `entity/thrown/FriendChivesProjectile.java` (`keepAliveAfterImpact` + the new `tick()`); `entity/ThrownCropEntity.java` (only cooperating on the existing hook, no second lifecycle opened).
- **Notes**: **it must end by itself** — the lifetime check at `:65-72` carries the `!settled` condition, and after landing no mechanism reclaims this entity, so missing it is an entity leak (RT-35); the `discard()` at tick `effectTicks` is a hard part of this entry, not an option. The field does not filter friend from foe: the thrower himself standing inside also gets hit (the existing comment at `FriendChivesProjectile.java:51-53`, unchanged in this release), and 29 must write it into the boundary list. Applying every tick does not extend it indefinitely: each write is `effectTicks − x`, 1 tick less than the previous one, so the effect ends naturally as the field runs out — which is also the meaning of this way of writing it (if `effectTicks` were written in full every tick, it would be extended forever from tick 2 on). **200 is only a default value, not a constant**: both the field length and the application base take the same `friend_chives.effectTicks` (range 20–1200, `FarmstormConfig.java:854-856`), otherwise setting the config to 100 would produce the contradiction "the field runs 200 ticks while each person gets only `100 − x`" (A-33). One key, two meanings; 30 must state it in the config comment. The number of fields alive at once is limited by the projectile's lifetime (RT-34). Rejected: "carry the field with a block entity" — that would add a `block_entity_type` to `friend_chives` and the registry surface would no longer be +0 (§3.4).

#### D-132 · The Friend Chives sound is still decided at the moment of landing only

- **ID**: D-132
- **Conclusion**: the sound decision stays at **the moment of landing**: if there is ≥1 creature inside the sphere, `friend_chives` is played **once**, otherwise **never** (an empty throw plays nothing); creatures walking into the sphere within the 200 ticks **do not get a catch-up play**.
- **Basis**: §2 item 10 of `BRIEF`; `plan0.1.1.txt:31` ("play once only, an empty throw plays nothing"); §1.8 — `FriendChivesProjectile.java:71-73` (`if (applied < 1) return;` ⇒ an empty throw plays nothing, ≥1 hit plays, only once), `:74-76` (`volume = friend_chives.soundRange / 16.0` = 32.0 ÷ 16 = **2.0**, `SOUND_PITCH = 1.0F`, `playSound(null, x, y, z, …, PLAYERS, volume, pitch)`); `friend_chives.soundRange` default **32.0** (0–64, `FarmstormConfig.java:857-859`).
- **Landing site**: `entity/thrown/FriendChivesProjectile.java` (the playback branch in the landing callback is kept, and the application logic moves to `tick()` at the same time; the two are separated from then on).
- **Notes**: the relation between volume 2.0 and "audible within 32 blocks" is unchanged: 32 ÷ 16 = 2.0 (§1.8). Whether `sounds.json` should additionally carry `"attenuation_distance": 32` is a technical-surface question (`sounds.json:16-22` does not set the key and takes the vanilla default of 16, while `:6` of the same file already has the precedent of 32 for `wild_hunter_loop`); it is checked by 30 and **does not change the conclusion of this entry**; the hand-over is registered in §7 item 5. Rejected: "repeat the play at intervals while the field lasts" — a direct conflict with "play once only", and it would also turn the 10-second field into a noise source (200 ticks = 10 seconds, **supplemented by this document**: 200 ÷ 20).

#### D-133 · The landed Friend Chives entity lives 200 ticks and stays visible

- **ID**: D-133
- **Conclusion**: the landed entity **lives a full 200 ticks and stays visible**: after landing the velocity is zeroed, gravity stays off and collision is off; on tick 200 it destroys itself; a chunk unload ends that field at once (no NBT life extension, no cross-dimension restoration).
- **Basis**: §2 item 11 of `BRIEF`; §1.8 (the `setNoGravity(true)` of `ThrownCropEntity` is at `:43-51`); RT-35 (the three risks of gravity / collision / chunk unload and their mitigation).
- **Landing site**: `entity/thrown/FriendChivesProjectile.java` (the physical convergence after settling and the self-destruct timing); rendering reuses the existing projectile renderer, the client is unchanged.
- **Notes**: visibility is a by-product of "the entity exists + the existing renderer", with no new model, texture or renderer. Collision off + velocity zeroed is so that the visible landing point is not pushed away by water or a piston — it is the visual anchor of this field, and moving it would contradict "at the landing point" in the plan's original text. A chunk unload ends that field (the mitigation of RT-35), so chasing people across chunks is not the semantics of this release, and 29 must write it as a boundary rather than a defect. Rejected: "invisible on landing, only the effect left" — ruling #11 explicitly requires "stay visible", and if it cannot be seen there is no feedback that "the field is still lasting".

### 1.5 Farmland and the Outpost Core (D-134 ~ D-141)

#### D-134 · Farmland-core becomes a binding system; the per-core capacity reuses `farmlandBonus`

- **ID**: D-134
- **Conclusion**: the relation between farmland and a core goes from "range coverage" to **binding** — each farmland block belongs to **1** core at any one moment; one core manages **16 blocks** at most; this 16 **directly reuses** `outpost_core.farmlandBonus` (default 16) and **no config key is added**.
- **Basis**: §2 item 12 of `BRIEF`; `plan0.1.1.txt:14` (the bug of mis-shovelling when cores overlap); the bug 1 scene of §1.3 — `OutpostCoreBlockEntity.setRemoved` (`:391-399`) → `settleOnRemoval` (`:315-335`) → `doomedFarmland` (`:353-366`) → `collectFarmland` (`:369-379`), the three criteria being "the player himself is on the books" (`:355`) + "going along the teammates" iterating `server.players()` (`:356-364`) + a 3×3 Chebyshev (`:371-373`), with **no check at all of "is this plot still covered by another core"**, and the only world write being the one line `setBlock(DIRT)` at `:331`; `outpost_core.farmlandBonus` default 16 (§1.2 / §3.4); RT-32 (save-format upgrade), RT-33 (one key, two meanings).
- **Landing site**: `farmland/FarmlandLedger.java` (the new "farmland → core" binding table and its reverse lookup; `byOwner` becomes derived from it); `blockentity/OutpostCoreBlockEntity.java` (the capacity check); `farmland/FarmlandGuard.java` (the criterion reads the binding table instead).
- **Notes**: `byOwner` **is no longer persisted separately** — if "whose field" and "which core a plot belongs to" were each stored once there would be two sources; the binding table is the only truth and `byOwner` is derived in memory only (**supplemented by this document**). `farmlandBonus` therefore **has one key and two meanings**: it is both the per-core capacity and the base of the quota (the 16 × number of cores of D-139); changing it changes both places at once, and 30 must say so in the config comment (RT-33). Rejected: "add `outpost_core.farmlandCapacity` as a separate key" — that would give "16 blocks" and "+16 quota" a knob each, and once the two values differ nobody could answer "does this plot count as over-quota" — ruling #12 explicitly requires reuse. For the save format and legacy-save compatibility see D-137 (RT-32); for the settlement when a core is destroyed see D-136.

#### D-135 · The binding rule: nearest first, ties by placement order

- **ID**: D-135
- **Conclusion**: at the moment the hoeing succeeds, bind this farmland block to **the nearest ally core**; equal distance is settled by **earlier placement**; a candidate core must **already have a 3×3 influence range covering this farmland** and **still have capacity**.
- **Basis**: §2 item 13 of `BRIEF`; `plan0.1.1.txt:14`; §1.3 — the hoeing gate `event/FarmstormGameEvents.java:194-217` (`:206` `ledger.add(...)`), the three-state `Verdict` of `FarmlandGuard.check` (`:69-75`) (`ALLOW` / `NO_CORE` / `QUOTA_FULL`, `NO_CORE` being by definition "not inside the 3×3 of an ally core"); §1.2 (the 3×3 influence range and `inRange`).
- **Landing site**: `farmland/FarmlandGuard.java:69-75` (`check` gains the "choose a core + capacity" decision); `event/FarmstormGameEvents.java:194-217` (write the binding after the pass); `blockentity/OutpostCoreBlockEntity.java` (the 3×3 and capacity queries).
- **Notes**: **the range gate is kept** — D-118 of 0.1.0, "farmland can be tilled only inside an ally core's 3×3", has not been voided by any ruling; ruling #13 answers "which core to bind to", not "may one till at all"; this "two gates stacked" is **supplemented by this document**, the joint reading of the two source texts (§5 row 3 registers it; the conflict registration is in item 2 of the "writing-time report"). **The implementation convention of "ties by placement order"**: always by **`placedGameTime` ascending** (that field is already persisted, `OutpostCoreBlockEntity.java:79`, `:405`), and **at the same instant by core coordinate lexicographic order** (the full comparison key: `placedGameTime` → dimension id → coordinates (A-39); the last segment serves only the extreme tie of the same coordinates across dimensions). `OutpostCoreIndex.byOwner` is a **`HashMap`** (`:53`), the iteration order across owners is not stable, so **no reliance on the insertion order of any Map / list** (A-39). The carrier of `placedGameTime`'s value (a cache on the index side or an equivalent) is pinned down by 30, on the precondition that **no chunk containing a candidate core is loaded for the comparison** (the same convention as `isFarmlandLoaded`, `OutpostCoreBlockEntity.java:382-385`). The meaning of `Verdict.QUOTA_FULL` goes from "the team quota is used up" to "the nearest ally core in range is already at capacity"; the three-state structure is unchanged; whether the hint language key is renamed is decided by 30 (keeping the key costs less). Rejected: "bind to the earliest-placed core" — the first criterion of ruling #13 is distance, and earlier placement is only the tie-break.

#### D-136 · The migration rule when a core is destroyed

- **ID**: D-136
- **Conclusion**: when a core is destroyed, first **migrate** the farmland in its name to a successor, and **only when it cannot migrate does it turn to dirt**; the successor must satisfy "its influence range covers that farmland + it is allied with the farmland's owner + it still has capacity" all at once, and among several candidates **the nearest first**; only when there is none does it `setBlock(DIRT)`.
- **Basis**: §2 item 14 of `BRIEF`; the bug 1 scene of §1.3 (the current implementation settles directly, `OutpostCoreBlockEntity.java:315-335`); §1.2 (the only break callback is `onRemove → core.release()` at `OutpostCoreBlock.java:101-107`); `plan0.1.1.txt:14`.
- **Landing site**: `blockentity/OutpostCoreBlockEntity.java:315-335` (`settleOnRemoval` becomes "migrate first, dirt after"), `:353-366` (handle only the farmland of this core in the binding table); `farmland/FarmlandLedger.java` (rebind + remove).
- **Notes**: the order is fixed as "work out the whole set of coordinates to migrate / to turn to dirt first, then touch the world", following the existing reason at `:312-313` (avoid changing the ledger while iterating it). Only the farmland **in this core's name** is handled, and the online players are no longer iterated — that pass at `:356-364` is exactly the other half of bug 2 (the owner going offline invalidates it), and after the binding system it has no reason to exist (D-137 solves it as well). "Turns to dirt" is still the only world write (`:331`); no drop is added, no compensation is given, and the farmland's owner is not notified. **"No successor found + the chunk holding that farmland is not loaded" is a separate branch**: `setBlock` cannot write there, and a chunk must not be loaded just to shovel a plot, so it degrades to **removing the binding only, the world untouched** (the farmland is still there, it has merely lost its protection); the reason is "never load a chunk because of a settlement, and never lose a block update" (A-35, the same direction as the degradation convention of RT-32). Rejected: "turn all the farmland in its name to dirt when the core is destroyed (keeping the 0.1.0 semantics)" — that would leave the binding system with nothing but billing, while the bug the author wants fixed is exactly this one.

#### D-137 · Backfill the bindings when a legacy save starts

- **ID**: D-137
- **Conclusion**: on server start, backfill bindings for the **unbound** farmland by the new rule; those that cannot be matched **stay farmland untouched**, merely unprotected; **never shovel a legacy save**.
- **Basis**: §2 item 15 of `BRIEF`; `plan0.1.1.txt:14-15`; §1.3 (`farmland/FarmlandLedger.java:44`, the persisted name `farmstorm_farmland`, `:228-244` `save`, `:246-264` `load`); RT-32 (`farmstorm_farmland.dat` format v1 → v2 backward-compatible read + start-up backfill).
- **Landing site**: `farmland/FarmlandLedger.java` (the old-format compatibility branch of `load` + the start-up backfill entry point); `blockentity/OutpostCoreBlockEntity` (the candidate-core query reuses `inRange`); a server start-up hook (on the same level as the existing initialisation of `Farmstorm`).
- **Notes**: the matching predicate of the backfill is **the same one** as the migration of D-136 (coverage + alliance + a free slot, nearest first) — ruling #15 says only "by the new rule", and the only ruling in the new rule that gives the complete predicate is #14, so reusing it is the only way not to invent a third criterion (**supplemented by this document**, see item 3 of the "writing-time report"). The `byOwner` records of the old format are turned into "unbound farmland" on load, so old fields become "awaiting backfill" in place, which is exactly the semantics of "leave them alone if they cannot be matched". The backfill runs once, at start-up only; farmland newly tilled at run time goes through D-135. **The traversal order of the backfill is pinned down**: **legacy-save backfill (including the first backfill of a 0.1.0 legacy save) always sorts the candidate farmland by the `BlockPos` single-level total order first (`BlockPos#compareTo`: y → z → x), and then handles them one by one**; "by `byOwner` insertion order" is **void** — `FarmlandLedger.byOwner` is a `HashMap` (confirmed by A-39), the derived order across owners is not stable, and a 0.1.0 legacy save has **no `orphans` key** to rely on, so the derived order can only come from the ledger, and without sorting "the same save gives different results on two starts", which directly conflicts with the ordering determinism of §7 #19 of 30 (A-47). **No dimension level is set**: the NBT of `FarmlandLedger` has only `pos` and no dimension field (`FarmlandLedger.java:46-48`, `:228-244`), and the same `pos` is one record only in the ledger anyway, so the single level is the total order; if the ledger ever gains a dimension, one more level is appended on this entry then. The capacity of 16 is a hard ceiling, and the order directly decides which fields of a legacy save get bound, so pinning it down is what makes it reproducible. Rejected: "turn the farmland that cannot be matched to dirt at start-up" — a direct conflict with "never shovel a legacy save" of ruling #15; also rejected: "recompute by the teams of all currently online players during the backfill" — that would wash away the ownership relations of the legacy save.

#### D-138 · The "ally" boundary and offline validity

- **ID**: D-138
- **Conclusion**: "ally" = **the core owner himself + same-team allies**; the judgement always goes by **UUID / name**, **regardless of whether the target is online**; farmland protection, the quota and the HUD overlay display **share this one set of judgements**.
- **Basis**: §2 item 17 of `BRIEF` (valid offline too, queried from the UUID index instead); `plan0.1.1.txt:15`; the bug 2 scene of §1.3 — `:125` of `FarmlandGuard.protectsFromTrample` (`:120-134`) iterates `getPlayerList().getPlayers()` (online only), `inOwnRange` (`:92-107`) likewise starts from online players, and the class comment `:114-115` admits "nothing is protected when all the ledger's owners are offline"; `OutpostCoreIndex.owned(Player)` (`:85-90`) also takes a live entity; `util/DeployedOwnership.isAlly(level, ownerUuid, ownerName, target)` (`:48`) — the owner side is UUID + name already.
- **Landing site**: `farmland/FarmlandGuard.java:92-107`, `:120-134` (query the binding table → the core owner's UUID → `DeployedOwnership.isAlly`); `farmland/FarmlandLedger.java:101-108`, `:111-121` (the member sets of `capacity` / `used`); `blockentity/OutpostCoreBlockEntity.java:353-366` (the online iteration goes away together with D-136).
- **Notes**: the gap is not "how to judge alliance" — the owner side of `DeployedOwnership.isAlly` is already UUID + `scoreboardName` (the comment at `OutpostCoreIndex.java:121-123` states that this is for the offline reverse lookup of teams); what is missing is "getting from this farmland back to the core owner", and that is exactly what the binding table of D-134 provides. The quota must change as well: `capacity()` currently iterates the online members (the `members(...)` of `:102`, `FarmlandLedger.java:185-196`), so it shrinks on the spot when a teammate goes offline — without changing it together with this entry there would be the new dual source "already-tilled land over quota once a teammate logs off". Mobs are always non-ally (no team affiliation); the existing exclusions of creative / spectator on the targeting and triggering surfaces are not part of the definition of "ally". **The trample protection surface goes by "protected as long as bound"**: `FarmlandGuard.protectsFromTrample` has no inquirer parameter and can only answer "is this plot bound", not "who stepped on it", so the trample surface **has nothing to do with a team change**; **the quota gate and the HUD overlay still go by the live ally judgement** — B-18 of 29, "protection is lost immediately after a team change", is therefore narrowed to hold only for the quota / the HUD overlay (A-45). Rejected: "cache a team snapshot for offline resolution" — that would turn a team change into an event that needs a refresh, the opposite of the live resolution of C-9.

#### D-139 · The quota comes from per-core capacity; `farmland.limitPerPlayer` is abandoned

- **ID**: D-139
- **Conclusion**: the quota becomes **`capacity = 16 × number of ally cores`** (16 being `outpost_core.farmlandBonus`); `farmland.limitPerPlayer` and the concept of "a personal base quota (base)" are **deleted wholesale**.
- **Basis**: §2 item 16 of `BRIEF`; §1.3 — `FarmlandLedger.base()` (`:72-74`) reads `farmlandLimitPerPlayer` or `baseOverride`, the `capacity = base + bonus × number of cores` of `:82-108`, `:185-196` `members(...)`; §1.9 (the push `:904` / define `:907` of `farmland.limitPerPlayer`, default 0, range 0–4096, the **only reader** being `base()`); `BRIEF` §3.4 (delete 1 add 1 ⇒ config still 166, groups 59 → 58).
- **Landing site**: `farmland/FarmlandLedger.java:72-74` (delete `base()`), `:76-79` (delete `setBase`), `:82-84` (`personalCapacity` loses the base parameter), `:92-98` (`sumOverMembers` loses base), `:101-108` (`capacity`); `config/FarmstormConfig.java:904,907` (delete the keys).
- **Notes**: the "default 0 ⇒ nobody can till any land at the start and a core must be placed first" of 0.1.0 (item 2 of D-116 of 22) still holds arithmetically, but the reason it holds has changed: it is no longer base = 0 but "no core means no capacity". The "used" of `used()` becomes "the number of blocks in the binding table under an ally's name", the same source as the capacity. The config total stays level (166 = 166 − 1 + 1), but the number of groups drops from 59 to 58 (§3.4). Rejected: "keep `limitPerPlayer` as a team-wide base quota" — that would go back to the dual source "base + capacity", a direct conflict with the `16 × number of cores` of ruling #16, and would turn "one key with two meanings" into "two keys with three meanings".

#### D-140 · Delete the `/farmstorm farmlandlimit` command tree

- **ID**: D-140
- **Conclusion**: delete the `/farmstorm farmlandlimit` subcommand and the whole server-side command registration chain; the `/farmstorm` root node **keeps no empty shell**.
- **Basis**: §2 item 16 of `BRIEF`; `plan0.1.1.txt:14-15`; §1.9 — the whole file `command/FarmstormCommands.java` is 58 lines with **only one** subcommand at `:37-41` (permission 2, range 0..4096, `:27-31`), the only action being `ledger.setBase(value)` (`:50-57`); the registration points `event/FarmstormGameEvents.java:62`, `:71-72`, `:4`; 2 related language keys (`zh:145`, `zh:64`); RT-40 (the knock-on list).
- **Landing site**: `command/FarmstormCommands.java` (the whole file deleted); `event/FarmstormGameEvents.java:4`, `:62`, `:71-72`; the language key `command.farmstorm.farmlandlimit.set`.
- **Notes**: the command is the only write entry point of `base`; with `base` dead it has no semantics (D-139). Four knock-ons: the `baseOverride` field, the two language keys, the command description in the 0.1.0 documents (§3.3 rows 1 ~ 5), and the sentence in `README`, "the farmland rule goes through `/farmstorm farmlandlimit`, not a gamerule" (`README.md:191`, which falls in the README increment). After the deletion the **server side** no longer has `/farmstorm`; the client's `/farmstorm hud …` (D-144) is another registration, and the two do not share the same registration point. Rejected: "keep the command but turn it into a read-only query" — what the author wants is the deletion of the quota carrier, and leaving a shell that can only be looked at would merely make people think the quota is still there.

#### D-141 · Outpost Core light level 15

- **ID**: D-141
- **Conclusion**: `outpost_core` gets `.lightLevel(state -> 15)` on registration; every other block property stays as it is.
- **Basis**: §2 item 18 of `BRIEF`; `plan0.1.1.txt:16`; §1.2 — `registry/FarmstormBlocks.java:82-89` has `MapColor.STONE` / `strength(30.0F, 1200.0F)` / `requiresCorrectToolForDrops()` / `SoundType.STONE` / `pushReaction(BLOCK)`, and **no `.lightLevel(...)`** (measured by grep, the value being 0).
- **Landing site**: `registry/FarmstormBlocks.java:82-89`.
- **Notes**: the light level is **block light**: it propagates a strong light over 15 blocks and keeps that position from ever spawning mobs (RT-36) — 29 must write it into "known effects" and 30 into the block property table. The light level affects neither the block entity, nor the boundary particles, nor the farmland decisions. Of the 25 rulings only this one is a pure block-property change, so it brings no number table along. Rejected: "make the light level a config key" — the author gives one fixed property, and adding a key would amount to inventing a requirement.

### 1.6 HUD overlay and run time (D-142 ~ D-145)

#### D-142 · The data surface of the HUD overlay: 1 new custom network packet (S2C, read-only)

- **ID**: D-142
- **Conclusion**: add **exactly one** custom network packet for the HUD overlay: a **one-way server → client** push of "the ally core list"; the client only reads, **sends no reply** and makes no decision.
- **Basis**: §2 item 22 of `BRIEF` (the 0-packet red line abolished, a custom packet added to push the core list); §1.4 — custom packets are currently **0**, there are 2 BE-sync precedents (`DeployedCropBlockEntity.java:455-460` / `:462-466`, `ConverterBlockEntity.java:418-428`) and 5 classes / 8 places of vanilla packet usage, and the conclusion is that "the server → client push of a custom list **has no precedent at all in this repository**, this release being the first"; §1.2 (`OutpostCoreIndex` is a `SavedData` the client does not have); §1.5 (`WildHunterHud` has no data surface, and none of the six listeners of `FarmstormClient.java:46-96` receives a packet); RT-31.
- **Landing site**: a new `network/` package (an S2C payload + `RegisterPayloadHandlersEvent` registration + a client handler); the data source `outpost/OutpostCoreIndex`; the push is hung on the placement / destruction / migration of `blockentity/OutpostCoreBlockEntity`, and on player login / dimension change.
- **Notes**: the payload's name, the shape of the `StreamCodec`, the set of push timings and the de-bouncing are **decided by 30** (RT-31 requires the timings to be listed and accepted item by item). Content boundary: for each core "dimension + coordinates + the owner's display name + whether it is oneself", the order being carried by the list order (D-143); the owner's name is taken from the server-side `ownerNames` (`OutpostCoreIndex.java:55-56`, present offline as well). **Why nothing else will do**: the client has only the chunks within view distance and the entities already tracked, and cannot answer "which cores do I have"; the owner may be offline (`owned(Player)` is usable only while online, §1.2), and the teammate relation can only be resolved by the server; `OutpostCoreIndex` is itself a server-side `SavedData` with no copy on the client. This packet **does not enter the registry surface** (+0, §3.4: a payload goes through `RegisterPayloadHandlersEvent` and does not enter `BuiltInRegistries`). The five rejected alternatives are in §6 items 2 ~ 6 and item 9; for the red-line side see §4.

#### D-143 · The display range and content of the HUD overlay

- **ID**: D-143
- **Conclusion**:
  1. list **all** the cores of **oneself + teammates** — **including unloaded chunks and other dimensions** (the **same** ally judgement as farmland protection, that is D-138);
  2. one line per core: `#n [dimension path] (x, y, z) owner` (oneself marked with "oneself", a teammate shown by the real name);
  3. ordered by **`placedGameTime` ascending**, **one ordering across dimensions**;
  4. display **8 core lines** at most, adding **1 more line** "+N" when there are more — that is **9 lines** at most.
- **Basis**: §2 items 19 and 20 of `BRIEF`; the note under the table of §2 of `BRIEF` (the 8-line cap, the default position top-left at `(4,4)`, oneself in green / teammates in white, the dimension marker taking the path of the dimension id with no new language key, annotated "supplemented by that card, may be overruled"); §1.5 (the registration and drawing shape of `WildHunterHud` `:19`, `:40-41`, `:46`, `:60-67`); §1.2 and `blockentity/OutpostCoreBlockEntity.java:79`, `:405` (`placedGameTime` persisted, the basis of this entry's ordering).
- **Landing site**: a new overlay renderer class under `client/` (`RenderGuiEvent.Post`, registration shaped after `client/WildHunterHud.java`); the data comes from the packet of D-142; the ordering information comes from the list order.
- **Notes**: **precisely because the display has to cross dimensions, the dimension marker must be at the head of the line** — the **path** of the dimension id (`overworld` / `the_nether` / `the_end`), with **no new language key** (the convention of the note under the table of §2 of `BRIEF`); when the same coordinates have a core in each of different dimensions it is what tells them apart, and without a dimension marker they would necessarily collide (**supplemented by this document**, see item 4 of the "writing-time report"). "Owner" uses one language key for the oneself line, while a teammate shows the name from `ownerNames` (present offline as well). The line spacing / font size / padding / the glyph of "+N" are **fixed by 29 within the framework of this entry**, subject to the int range of `hud.x` / `hud.y` and the screen size. Enemy cores are not shown, core items are not shown, and no occlusion detection is done (occlusion detection is the precondition of automatic avoidance and is rejected along with §6 item 7). The preconditions `options.hideGui` and `screen != null` follow `WildHunterHud.java:46`, the only exception being drag mode (D-144 Notes).

#### D-144 · The switch and position carriers of the HUD overlay: a client key + a client command + CLIENT config + dragging

- **ID**: D-144
- **Conclusion**:
  1. **2 `KeyMapping`**: switch the overlay on and off, and enter drag mode;
  2. **the client command** `/farmstorm hud <on|off|reset>`;
  3. **CLIENT-type config**, 3 keys persisted: `hud.enabled` (bool, default **true**), `hud.x` (int, default **4**), `hud.y` (int, default **4**);
  4. the position can **only** be adjusted through **drag mode**: open a Screen, drag to position, `ESC` saves (single-player pauses automatically);
  5. **no automatic avoidance**.
- **Basis**: §2 item 21 of `BRIEF`; the note under the table of §2 of `BRIEF` (default `(4,4)`, dragging opens a Screen, `ESC` saves); §1.5 — of the 21 classes under `client/`, **0** register a `KeyMapping` (0 hits of `RegisterKeyMappingsEvent`) and **0** are a CLIENT-type config (the only `registerConfig` in the whole repository is at `Farmstorm.java:56` and is `ModConfig.Type.SERVER`); `BRIEF` §3.4 (the 3 CLIENT keys **do not count** towards 166, they are a separate `farmstorm-client.toml`); `plan0.1.1.txt:17`.
- **Landing site**: `client/FarmstormClient.java` (the first `RegisterKeyMappingsEvent` registration) + a new client key class; a new drag `Screen`; `config/FarmstormConfig.java` (the first `ModConfig.Type.CLIENT`, 3 keys); the client command registration.
- **Notes**: **the overlay must stay visible while dragging** — the `minecraft.screen != null` precondition of `WildHunterHud.java:46` makes the HUD disappear wholesale when a Screen is open, and drag mode must be handled as an "edit state" exception, otherwise the player cannot see what he is dragging (**supplemented by this document**: a technical necessity, not a design preference). The position is clamped inside the screen (`0 ≤ x ≤ guiWidth − width`, the expression fixed by 29). **This is the second head-on conflict with the plan's original text**: `plan0.1.1.txt:17` requires "when occluded it may move automatically to the position closest to the top-left corner that is not occluded", and ruling #21 re-adjudicated it into dragging + **no automatic avoidance** (§6 item 7). "Driving the client HUD switch with server config" does not work technically (§6 item 2). The default key bindings, the command feedback wording and the language keys are named item by item by 30 (the `k` of §3.4).

#### D-145 · Run-time temporary force-loading of the chunk the core is in

- **ID**: D-145
- **Conclusion**: every placed core **temporarily** force-loads **the 1 chunk** it is in **during run time** (**not** the 3×3); **nothing is written** to `world/data/chunks.dat`; the ticket is released when the core is destroyed or the server stops.
- **Basis**: §2 item 24 of `BRIEF`; RT-39 (ticket leak: the release point in the same place as `release()`, the server-stop fallback); §1.2 (the boundary particles of `OutpostCoreBlockEntity.serverTick` (`:179-192`) depend on the block entity ticking inside a loaded chunk; `release()` is triggered by `OutpostCoreBlock.java:101-107`).
- **Landing site**: `blockentity/OutpostCoreBlockEntity` (requested on placement, released in `release()`); the region-ticket registration on the `outpost/` side; a server-stop hook as the fallback.
- **Notes**: the ticket's type and level are **decided by 30 according to the forms available in 21.1.213** (the shape of `TicketType` is a technical-surface matter). Its force-loading covers **1 chunk**, not the 3×3 — the farmland criterion reads the binding table and the coordinates, and does not need the farmland's chunk in memory (`isFarmlandLoaded` reads in-memory chunks only, `OutpostCoreBlockEntity.java:382-385`). It **does not solve** "the client cannot see a core outside view distance"; that is what the packet of D-142 is for, and the two are not interchangeable (§6 item 4). **The persistence convention (an erratum)**: the force-loaded-chunk data of 1.21.1 is **not in `level.dat`** but in **`world/data/chunks.dat`** (`ForcedChunksSavedData.FILE_ID = "chunks"`, persisted per dimension); the `level.dat` in the early version of ruling #24 is a relocation typo (the fact card has been corrected per A-38), and the implementation convention prevails. Both implementation paths are therefore **rejected together**: ① `ServerLevel#setChunkForced()`; ② NeoForge's `TicketController#forceChunk` — both would write the core coordinates into `chunks.dat`, a direct conflict with "temporary at run time", and both would linger after the server stops. **Only a bare `TicketType` + `addRegionTicket`**.

### 1.7 Red lines and the release surface (D-146 ~ D-147)

#### D-146 · Change of the red-line convention: only "no Mixin" is kept

- **ID**: D-146
- **Conclusion**: from v0.1.1 on, the project-level red lines are **left with "no Mixin" alone**; "**0 custom network packets**" is **abolished outright** — not relaxed, not "allowing 1", not "an exception".
- **Basis**: §2 item 23 of `BRIEF`; the two-red-line comparison table of §0.1 of `BRIEF` (v0.0.1 ~ v0.1.0 = 0; v0.1.1 = the restriction abolished, this release happening to add 1 but **setting no ceiling**; Mixin none throughout); §1.4 (`@Mixin` measured 0, `build.gradle` has no mixin plugin).
- **Landing site**: the six documents 28 ~ 33 (every statement of the red lines); the red-line section of `README.md` (`:71`, `:148`, `:191`, the English section `:419`, `:423`); the HC-77 / N-75 red-line section of `32-PROMPT-MAIN-0.1.1.md`.
- **Notes**: 22 ~ 27 **stay frozen and unedited** (the ⚠️ of §0.1 of `BRIEF`, §6 item 3); where they conflict with this document, this document wins (§4's document list). After the abolition there is **no numerical ceiling**, but every new packet must still be registered in the decision record and must not be added casually — what this release registers is the one of D-142. The third item of HC-73 of document 26, "`GameRules.register` is 0", **is not one of** the two red lines (`BRIEF` §0.1 lists two only), and it continues in force as an engineering constraint; 32 must keep it verbatim in HC-77. Rejected: "write it as 'relaxed to allow 1' or 'an exception for this release'" (`BRIEF` §6 item 4 forbids it outright). For the detailed change note see §4.

#### D-147 · Release surface: config defaults are not migrated, the script jar names change to 0.1.1 in passing

- **ID**: D-147
- **Conclusion**: the two default-value changes of the Pumpkin Turret (60 → 40, 1 → 3) are **written into the documents only; the code does no migration**; the jar name hard-coded in `检查环境.cmd` and `一键构建.cmd` is changed to `farmstorm-0.1.1+mc1.21.1.jar` in passing.
- **Basis**: §2 item 25 of `BRIEF`; §1.10 — the jar names at `检查环境.cmd:14` and `一键构建.cmd:11-12` are still `farmstorm-0.0.1+mc1.21.1.jar` (they had not kept up even at 0.1.0, measured); RT-38 (an already-persisted toml does not follow a default change).
- **Landing site**: `检查环境.cmd:14`, `一键构建.cmd:11-12`; the "upgrade note" section of the delivery documents (`README.md` increment + `30-TECH-0.1.1.md`).
- **Notes**: the upgrade note must state two things clearly — ① an already-persisted `farmstorm-server.toml` still holds the old values, and to make them take effect the old toml must be deleted or `pumpkin_turret.fireIntervalTicks` / `projectileDamage` edited by hand; ② the new `pumpkin_turret.bulletSpeed` is filled into the old toml automatically by the config system and does not have to be added by hand. The code refraining from migration is deliberate: to "help players get the defaults changed" by writing into someone else's config file amounts to changing the server settings without permission (the same direction as RT-38). The jar name and `mod_version` must be changed in the same pass, otherwise the build artefact's name comes adrift from the actual version (D-124). The division of labour with D-124 is: the version number lives at `gradle.properties:11`, the jar name in the two `.cmd` scripts.

---

## 2. Author ruling index (25 rulings → decision numbers)

The table below transcribes item by item the 25 author rulings of §2 of `BRIEF` (**the two columns "item" and "fixed as" are verbatim identical to the fact card**), and the last column is the decision number of this document.

| Ruling # | Item | Fixed as | Decision |
|---|---|---|---|
| 1 | Version number | **0.1.1**; document numbers **28~33**; numbering continuation see §0.2 | D-124 |
| 2 | The "green +" rule | **voided globally** (abolished at the rule level), but **this release regenerates `wild_apple` only, a single image**, the other 24 being left to the next release for batch regeneration | D-125 |
| 3 | Wild Apple texture | **the 64×64 original image used directly** (no downsampling, no more 16-grid modal colour picking) | D-126 |
| 4 | Pumpkin Turret identity | = `farmstorm:pumpkin_turret` | D-127 |
| 5 | Pumpkin Turret fire interval | `pumpkin_turret.fireIntervalTicks` default **60 → 40 ticks** (2 seconds) | D-127 |
| 6 | Pumpkin Turret damage | `pumpkin_turret.projectileDamage` default **1 → 3** | D-128 |
| 7 | Pumpkin Turret missile speed | `TurretBullet.SPEED` **0.6 → 0.45** (9 blocks per second), and **promoted to a config key** | D-129 |
| 8 | Pumpkin Turret homing | **no soft homing introduced**; only the "**close-range circling / jitter**" fixed (turning around again and again at point-blank range without hitting) | D-129 |
| 9 | Friend Chives window | lasts **200 ticks** after landing; scans the creatures inside the sphere **every tick**; applies **`200 − x`** ticks of Q-Bounce (x = the ticks elapsed since landing) | D-131 |
| 10 | Friend Chives sound | decided **at the moment of landing**: ≥1 creature inside the sphere ⇒ played **once**, otherwise never (an empty throw plays nothing) | D-132 |
| 11 | Friend Chives entity | after landing the entity **lives 200 ticks and stays visible** | D-133 |
| 12 | Farmland-core relation | **each farmland block belongs to 1 core only**; **one core manages 16 blocks at most**; the capacity **directly reuses** `outpost_core.farmlandBonus` (default 16), no new key | D-134 |
| 13 | Binding rule | on a successful hoeing, bind to the **nearest** ally core (ties by earlier placement) | D-135 |
| 14 | Migration rule | when a core is destroyed, look for a successor only among the cores satisfying "**the influence range covers that farmland** + **allied with the farmland's owner** + **a free slot left**", **nearest first**; none found → turns to dirt | D-136 |
| 15 | Legacy saves | **backfill the bindings at start-up by the new rule**; those that cannot be matched **stay farmland untouched**, merely unprotected (**never shovel a legacy save**) | D-137 |
| 16 | Quota | **`farmland.limitPerPlayer` abandoned**, `capacity = 16 × number of cores`; the `/farmstorm farmlandlimit` command **deleted with it** | D-139, D-140 |
| 17 | The "ally" boundary | a core manages only the farmland of "**the core owner himself + same-team allies**"; **valid offline too** (queried from the UUID index instead) | D-138 |
| 18 | Outpost Core light level | `.lightLevel(state -> 15)` | D-141 |
| 19 | Whom the overlay shows | **all** the cores of **oneself + teammates** — **including unloaded chunks and other dimensions** (not "only the current dimension"); the ally judgement is the same one as farmland protection | D-143 |
| 20 | Overlay content | one line per core: **dimension marker + coordinates + owner (oneself / a teammate's name)**, by placement order (`placedGameTime` ascending, one ordering across dimensions) | D-143 |
| 21 | Overlay switch / position | **client keys** (switch + enter dragging) + **a client command** + **CLIENT config persistence**; the position is adjusted with **drag mode**, and **no automatic avoidance** is done | D-144 |
| 22 | Overlay data surface | **abolish the "0 custom network packets" red line**, add a **custom network packet** pushing the core list | D-142 |
| 23 | The new red-line convention | the packet red line **abolished outright**; **only "no Mixin" kept** | D-146 |
| 24 | Force-loading | **that 1 chunk** the core is in, **temporary at run time**, not persisted (**nothing written to `world/data/chunks.dat`** — the `level.dat` written in the early version of ruling #24 is a slip (**the fact card has been corrected per A-38**)) | D-145 |
| 25 | config migration & scripts | the Pumpkin Turret default changes are **written into the documents only** and the code does no migration; the jar names of `检查环境.cmd` / `一键构建.cmd` **changed to 0.1.1 in passing** | D-147 |

Three mapping notes (to be watched when reading the table):

1. The 25 rulings map onto **24 decisions**: rulings #16 and #25 are each split into two (quota / command, config / script), because their landing sites and acceptance methods are completely different; rulings #4 + #5, #7 + #8 and #19 + #20 are each merged into one (Pumpkin Turret identity and interval, missile speed and close-range convergence, overlay range and content), and the reason for each merge is written in the "Notes" of those entries.
2. **There is no "Compressed Hay Block sound" among the 25 rulings** — requirement 3 of the plan (`plan0.1.1.txt:20-23`) did not enter the interview, and the basis of D-130 is only the plan's original text and `BRIEF` §1.6 / §3.2 row 3. This document fills it in as an independent decision, registered in item 1 of the "writing-time report".
3. The bold in the "fixed as" column of the table is the emphasis the fact card already had, not a judgement newly added here; when citing, copy it verbatim and do not reword it.

---

## 3. Register of changes to 0.1.0

The three tables register **additions / changes / deletions** respectively. Every row gives the original location in 0.1.0 — it is the evidence of "where this sentence is in 0.1.0", not a suggestion to change it (22 ~ 27 are frozen). The tables contain no tasks and no scheduling.

### 3.1 Additions (things 0.1.0 does not have)

| # | Addition | Original location in 0.1.0 (proof it did not exist then) | Decision |
|---|---|---|---|
| 1 | **1 custom network packet** (an S2C push of the ally core list + registration + a client handler) | `22-DECISIONS-0.1.0.md:44` ("0 custom network packets"), `:68`, `:480` (§6 item 4 "`CustomPacketPayload` is still 0"); `26-PROMPT-MAIN-0.1.0.md:183-185` (HC-73) | D-142 |
| 2 | **2 `KeyMapping`** + the first `RegisterKeyMappingsEvent` registration | `BRIEF` §1.5: of the 21 classes under `client/`, 0 register a `KeyMapping` and `RegisterKeyMappingsEvent` has 0 hits; none of the six listeners of `client/FarmstormClient.java:46-96` has a key registration | D-144 |
| 3 | **CLIENT-type config** (`farmstorm-client.toml`, 3 keys `hud.enabled` / `hud.x` / `hud.y`) | `Farmstorm.java:56` is the only `registerConfig` in the whole repository and its type is `ModConfig.Type.SERVER` (`BRIEF` §1.5) | D-144 |
| 4 | **The client command** `/farmstorm hud <on\|off\|reset>` | `command/FarmstormCommands.java:37-41` has only `/farmstorm farmlandlimit`, registered on the server side at `FarmstormGameEvents.java:71-72` (`BRIEF` §1.9) | D-144 |
| 5 | **Overlay rendering + a drag Screen** | `BRIEF` §1.5: the only client HUD is `WildHunterHud` (77 lines, no occlusion intersection, no switch, no config) | D-143, D-144 |
| 6 | **The farmland → core binding table** (with its reverse lookup) and **the start-up backfill** | `farmland/FarmlandLedger.java:51` has only `byOwner`; `save` / `load` (`:228-244` / `:246-264`) have no binding field (`BRIEF` §1.3) | D-134, D-137 |
| 7 | **A run-time temporary ticket for the chunk the core is in** | listed as an addition on row 25 of `BRIEF` §3.2; 0.1.0 has no chunk ticket at all | D-145 |
| 8 | **The config key `pumpkin_turret.bulletSpeed`** (double, 0.45) | `entity/TurretBullet.java:28` is the hard-coded `SPEED = 0.6`, with no config (`BRIEF` §1.7) | D-129 |
| 9 | **Language keys**: `farmstorm.configuration.pumpkin_turret.bulletSpeed`, the overlay's "oneself" marker, and the client command feedback (on / off / reset) | the addition list of `BRIEF` §3.5; none of these keys is among the 315 base keys of 0.1.0 | D-129, D-143, D-144 |

### 3.2 Changes (present in 0.1.0, semantics or values changed)

| # | Item | 0.1.0's value / semantics | v0.1.1 | Original location in 0.1.0 | Decision |
|---|---|---|---|---|---|
| 1 | Version number | 0.1.0 | **0.1.1** | `gradle.properties:11` | D-124 |
| 2 | `pumpkin_turret.fireIntervalTicks` | 60 | **40** | `config/FarmstormConfig.java:431-433` | D-127 |
| 3 | `pumpkin_turret.projectileDamage` | 1 | **3** | `config/FarmstormConfig.java:425-427` | D-128 |
| 4 | `textures/item/wild_apple.png` | 16×16 / 571 B / 88 colours / 107 transparent | **64×64** (source image 6,946 B) | `22-DECISIONS-0.1.0.md:581` (the finished specification of D-105), `assets/farmstorm/textures/item/wild_apple.png` | D-126 |
| 5 | The "green +" rule | in force (25 carry a +) | **void** (the generator's mechanism deleted) | the rule's original text `plan0.0.1.txt:8`; the carriers `tools/gen_textures.py:274-275,407,974-975` | D-125 |
| 6 | The farmland protection criterion | "the ally farmland inside the range" | **the farmland bound to a core** | `farmland/FarmlandGuard.java:120-134`; item 3 of D-120 of `22` | D-134, D-138 |
| 7 | The quota formula | `base + bonus × number of cores` | **`16 × number of cores`** | `farmland/FarmlandLedger.java:82-108`; D-116, D-117 of `22` | D-139 |
| 8 | The resolution of "ally" | starting from the list of online players | **on the UUID / name side** (valid offline) | `farmland/FarmlandGuard.java:92-107`, `:120-134`; `outpost/OutpostCoreIndex.java:85-90` | D-138 |
| 9 | The settlement when a core is destroyed | **all** the ally farmland in range turns to dirt | **the farmland in this core's name migrates first, and only what cannot migrate turns to dirt** | `blockentity/OutpostCoreBlockEntity.java:315-335`, `:353-366`; item 4 of D-120 of `22` | D-136 |
| 10 | Outpost Core light level | 0 (no `lightLevel`) | **15** | `registry/FarmstormBlocks.java:82-89`; D-119 of `22` | D-141 |
| 11 | Compressed Hay Block sound | `SoundType.GRASS` | **`STONE`** | `registry/FarmstormBlocks.java:79`; the D-57 row of §7.2 of `22` (recording "`sound(GRASS)` untouched") | D-130 |
| 12 | Red lines | **0 custom network packets** + no Mixin | **only no Mixin left** | `22-DECISIONS-0.1.0.md:44`, `:68`, `:480`; `26-PROMPT-MAIN-0.1.0.md:183-185` | D-146 |
| 13 | The convention "zero new client code" | holds in this release | **no longer holds** | item 2 of D-121 of `22` (`:302`), `23-DESIGN-0.1.0.md:49` | D-142, D-143 |
| 14 | The output-size assertion of `tools/gen_textures.py` | `:1152-1154` asserts `image.size == expected_size(spec)`, while `expected_size()` (`:982-983`) always returns `(TEXTURE_SIZE, TEXTURE_SIZE)` = 16×16 for non-GUI | **relaxed to the actual size per spec** (`wild_apple` being 64×64), the assertion itself kept | `tools/gen_textures.py:982-983`, `:1151-1154` | D-126 |
| 15 | The entry structure of `outpost/OutpostCoreIndex` (`Entry`) | two fields `(dimension, pos)`, **no `gameTime`** | **`(dimension, pos, gameTime)`**, `gameTime` persisted; an old entry missing the field falls back to `Long.MIN_VALUE` | `outpost/OutpostCoreIndex.java:50` (`record Entry`), `:43-47` (the NBT key names), `:209-238` (the persisted structure) | D-135 (A-39); A-53; RT-32 |

### 3.3 Deletions (present in 0.1.0, removed by this release — **the section most easily missed**)

| # | What is deleted | Original location in 0.1.0 | Decision |
|---|---|---|---|
| 1 | The config key **`farmland.limitPerPlayer`** (the whole `farmland` group goes to zero) | `config/FarmstormConfig.java:904` (push), `:907` (define); row 6 of §5.2 of `22` | D-139 |
| 2 | **The `/farmstorm farmlandlimit` subcommand** — the whole `command/FarmstormCommands.java` (58 lines) | `command/FarmstormCommands.java:37-41`; D-116 and row 18 of §4.2 of `22` | D-140 |
| 3 | 3 server-side command registrations | `event/FarmstormGameEvents.java:4` (import), `:62` (listener), `:71-72` (the registration call) | D-140 |
| 4 | **`FarmlandLedger.baseOverride` / `base()` / `setBase()`** and the base parameter of `personalCapacity` | `farmland/FarmlandLedger.java:57-58`, `:72-74`, `:76-79`, `:82-84` | D-139 |
| 5 | 2 language keys: `farmstorm.configuration.farmland.limitPerPlayer`, `command.farmstorm.farmlandlimit.set` | `zh_cn.json:145`, `zh_cn.json:64` (`BRIEF` §1.9) | D-139, D-140 |
| 6 | The generator's **"+" stacking mechanism** (`NO_PLUS_ICONS` / `add_plus` / `build_marker` / the bottom-right stamp) | `tools/gen_textures.py:274-275`, `:407`, `:546-560`, `:612-623`, `:974-975` | D-125 |
| 7 | **The `TurretBullet.SPEED` constant** | `entity/TurretBullet.java:28` | D-129 |
| 8 | The "non-GUI is always 16×16" hard-coding of `expected_size()` (replacement: the actual size per spec, see §3.2 row 14) | `tools/gen_textures.py:982-983` | D-126 |

Four notes on the deletions:

- Rows 1, 2, 4 and 5 are **four landing points of one and the same thing** (the quota carrier disappearing): the config key, the command, the `base` field and the two language keys must be deleted together, and deleting half would leave "reading a key that does not exist" or "a command changing a field that does not exist".
- Row 6 deletes a **mechanism**, not the 25 images. The other 24 old images are still there (§7 item 1), but re-running the generator will no longer put the `+` back.
- After deleting the constant in row 7 the config key must be added at the same time (§3.1 row 8); deleting without adding amounts to zeroing the speed.
- Row 8 and row 14 of §3.2 are **two sides of one and the same change**: what is deleted is the default "non-GUI is always 16×16", and what replaces it is "the actual size per spec + the assertion kept" (A-32).

### 3.4 Count arithmetic

- **config**: `166 − 1 + 1 = 166` (delete `farmland.limitPerPlayer`, add `pumpkin_turret.bulletSpeed`); the total stays level but **the composition changes**, the group count going `59 → 58`. The 3 CLIENT keys **do not count** towards 166 and are a separate `farmstorm-client.toml`.
- **Language keys**: `315 − 2 + 1 + 3 + 1 + 2 + 3 = 323`, every term named as follows — **− 2** = delete `farmland.limitPerPlayer` and `farmlandlimit.set`; **+ 1** = `pumpkin_turret.bulletSpeed`; **+ 3** = the CLIENT config `hud.enabled` / `hud.x` / `hud.y`; **+ 1** = the overlay's "oneself" marker; **+ 2** = the two description keys of the `KeyMapping`s (`key.farmstorm.*`); **+ 3** = the client command feedback (on / off / reset). A net gain of 10 entries (1 + 3 + 1 + 2 + 3 = 10), the result agreeing with the 323 named item by item by 30 (A-48; the client command feedback that A-48 records as `k` is this `+ 3`).
- **Registry surface**: **+0** (the packet payload goes through `RegisterPayloadHandlersEvent`, the keys through `KeyMapping`, the CLIENT config through `ModConfigSpec`, and none of the three enters `BuiltInRegistries`). The baseline 93 comes from the class-by-class naming of `BRIEF` §0 and appendix A; **30 must still do the class-by-class naming check against the source code itself** and must not copy this line (the ⚠️ of `BRIEF` §3.1).
- **Save format**: this release has **two** changes, **not one** — ① `farmland/FarmlandLedger`'s `farmstorm_farmland.dat` goes to v2 (the new "farmland → core" binding table, a legacy v1 read compatibly, RT-32); ② the entries of `outpost/OutpostCoreIndex`'s `farmstorm_outpost_core.dat` go from `(dimension, pos)` to `(dimension, pos, gameTime)` (A-53, a legacy entry missing the field falling back to `Long.MIN_VALUE`). **Doing the compatible read for only one of them misses the other**; both must go into the save-compatibility manifest of 30.

---

## 4. Change of the red-line convention (the single most important change of this release)

### 4.1 What changed

| Item | v0.0.1 ~ v0.1.0 | **v0.1.1** | Source |
|---|---|---|---|
| Custom network packets | **red line: 0** | **that red line abolished outright**; this release adds 1, **with no ceiling** | ruling #23; `BRIEF` §0.1 |
| Mixin | none | **none (the only red line kept)** | ruling #23; `BRIEF` §1.4 (`@Mixin` 0, `build.gradle` has no mixin plugin) |

The old convention of 0.1.0 is written in four places, and they are the comparison surface of this change: `22-DECISIONS-0.1.0.md:44` (the "two red lines" row of §1.1), `:68` (§1.3 boundaries), `:480` (§6 item 4); `26-PROMPT-MAIN-0.1.0.md:183-185` (HC-73) and `:402` (the HC comparison table); `README.md:71` (the Chinese red-line section), `:148`, `:191`; the English section `:419`, `:423`.

### 4.2 Four readings (get any one of them wrong and all six downstream documents are wrong)

1. **This is an outright abolition, not "allowing 1"**. There is no numerical ceiling, and no action space such as "applying for an exception" — §0.1 of `BRIEF` says "abolish the restriction (this release happening to add 1, but setting no ceiling)". Any statement written as "this release allows 1 custom packet" is wrong.
2. **This is not an "exception"**. The original structure was "prohibited by default + an exemption this time"; there is no such "prohibited by default" any more, and the word "exception" implies the ban is still there, so it is forbidden.
3. **Abolition does not mean "a packet must be added"**. This release adds 1 only because the HUD overlay really needs it (§4.3); every new packet in future still needs a decision of its own, registered, and must not be added casually.
4. **"No Mixin" is unaffected**: it is still zero, and `@Mixin` / `mixins.json` / `@Inject` / `@Redirect` / `@Accessor` are still forbidden. The third item of HC-73 of document 26, `GameRules.register` being 0, is not one of these two red lines either, and continues in force as an engineering constraint (D-146 Notes).

### 4.3 What the 1 network packet this release actually adds is, and why nothing else will do

**What it is**: a **one-way server → client** payload whose content is a copy of "the ally core list" — for each core "dimension + coordinates + the owner's display name + whether it is oneself", the order being carried by the list order (D-142 / D-143). The client only reads, sends no reply and makes no decision.

**Why nothing else will do** (four reasons, any one of which alone would be enough to reject the "0 packets" road):

1. The core index is a **server-side `SavedData`** (`outpost/OutpostCoreIndex.java`, persisted as `world/data/farmstorm_outpost_core.dat`) and the client has no copy (`BRIEF` §1.2).
2. "Which cores do I have" must be **complete**: the client loads only the chunks within view distance, and of a core beyond it, it does not even know that it exists.
3. The owner **may be offline**: `OutpostCoreIndex.owned(Player)` of 0.1.0 is usable only while online (`:85-90`), and "the teammate relation" can only be resolved by the server (the owner side of `DeployedOwnership.isAlly` is UUID + `scoreboardName`).
4. The list needs a **stable order** (by placement order) and a **line cap / +N overflow**, which is something only a snapshot composed on the client can do; pushing line by line cannot (§6 item 9).

**Three boundaries**: ① read-only, no write — the client sends no reply and server authority is unchanged (the existing convention of HC-03 / F-25); ② no judgement — the client uses the list only for drawing, and any decision of "is it an ally" still rests with the server; ③ **it does not enter the registry surface** (+0), a payload going through `RegisterPayloadHandlersEvent` and not into `BuiltInRegistries`.

### 4.4 Rejected alternatives

Six alternatives are all recorded in the tombstones of §6 (the number column gives the tombstone number, so later readers can look it up directly):

| Alternative | One-sentence rejection | Tombstone |
|---|---|---|
| Driving the client HUD switch with server config | server config cannot change client rendering | §6 item 2 |
| Showing only the cores "inside loaded chunks" | a core outside view distance is never shown, and "which cores do I have" cannot be answered | §6 item 3 |
| Force-loading chunks so the client can see them | the client only receives chunks within view distance, and loading one does not send it | §6 item 4 |
| The vanilla text UI (boss bar / scoreboard) | fixed position, not draggable, cannot be split line by line per core | §6 item 5 |
| An invisible marker entity + `CustomName` carrying the coordinates | a string is not a data channel, and it is bound by view distance just the same | §6 item 6 |
| Block-entity sync (`getUpdateTag` / `ClientboundBlockEntityDataPacket`) | covers only the blocks the client already knows, and cannot answer "which cores there are" | §6 item 9 |

### 4.5 Documents that have to carry the new convention

| Document | What it has to say | What it says now (the old convention) |
|---|---|---|
| **28 (this document)** | D-146 is the new convention; §4 is the explanation | — (newly written) |
| **29 design delta** | the sync surface written per D-142 (1 read-only S2C packet); "0 custom network packets" must no longer appear | `23-DESIGN-0.1.0.md:49`, `:99`, `:108` are the old convention (frozen, not edited) |
| **30 technical spec** | the packet chapter written per D-142; the red-line chapter saying "only no Mixin left"; the push-timing manifest of RT-31 | `24-TECH-0.1.0.md:497`, `:614`, `:951` |
| **31 task breakdown** | the acceptance criterion must no longer be "`grep CustomPacketPayload` is 0"; change to "the whole repository has only the 1 S2C packet registered by D-142, and the client sends no reply" | `25-TASKS-0.1.0.md:270`, `:326`, `:428`, `:585`, `:624` |
| **32 master prompt** | HC-77 / N-75 written with the new red-line section, and **explicitly declaring that they supersede the HC of 0.1.0** | `26-PROMPT-MAIN-0.1.0.md:183-185` (HC-73), `:289`, `:402` |
| **33 phase prompts** | the "red-line re-run" items of every phase changed to the new convention in step | `27-PROMPT-TASKS-0.1.0.md:212`, `:251`, `:259`, `:299`, `:467`, `:500`, `:519`, `:549`, `:552` |
| **README** | the red-line section changed to "no Mixin", with a note that the packet restriction has been abolished outright | `README.md:71`, `:148`, `:191`, `:419`, `:423` |

**22 ~ 27 are frozen, not a single character is changed**: they still contain the statement "0 custom network packets", which is the historical convention of 0.1.0. When readers and implementers meet a conflict, **this document wins** (the ⚠️ of §0.1 of `BRIEF`, §6 item 3, RT-41).

---

## 5. Supersession relation to the 0.1.0 decisions

The table below registers only the clauses where "0.1.0 says X and this release says Y"; it also keeps the comparison rows marked "**Kept**" (D-40, D-122 / D-123), to declare explicitly that "these were not changed". The clauses marked "Kept" in the table continue in force; **the 0.1.0 clauses not listed in this table** continue in force just the same (for example the live team resolution of D-06 / C-9).

| 0.1.0 clause (location) | Status | This release | Why |
|---|---|---|---|
| D-116's quota carrier = `/farmstorm farmlandlimit` + config default 0 (D-116 of `22`) | **Superseded** | D-139, D-140 | ruling #16: the quota becomes `16 × number of cores`, and the command is deleted with it |
| D-117's team quota summation + coordinate-set accounting (D-117 of `22`) | **Superseded** (the summation formula) | D-139; the accounting carrier moved to D-134 | what is summed goes from "personal base × head count" to "the sum of core capacities"; `byOwner` becomes a derived view of the binding table |
| D-118's "tillable only inside an ally core's 3×3, the ranges of several cores unioned" (D-118 of `22`) | **Partly superseded** | D-134, D-135 | the range gate is kept (still a necessary condition for tilling), and **ownership** goes from "the union" to "the one core it is bound to". This stacked reading is **supplemented by this document** (see item 2 of the "writing-time report") |
| Item 1 of D-120: +16 quota per core (D-120 of `22`) | **Superseded** | D-134, D-139 | +16 is now the **per-core capacity** and no longer a quota added on top of base |
| Item 3 of D-120: ally farmland in range is not trampled (D-120 of `22`) | **Superseded** (the criterion) | D-138 | from "iterating the online players to look it up" to "the binding table + the UUID index", valid offline too |
| Item 4 of D-120: when a core is destroyed all the ally farmland in range turns to dirt (D-120 of `22`) | **Superseded** | D-136 | migrate first, and only what cannot migrate turns to dirt; only the farmland in this core's name is handled |
| Item 2 of D-121: zero new client code, zero custom network packets (D-121 of `22`, `:302`) | **Superseded** (the first half) | D-142, D-143 | the HUD overlay is new client code + 1 new packet; the boundary particles themselves still go through the vanilla particle packet, unchanged |
| D-119's block properties of the Outpost Core (D-119 of `22`) | **Extended** | D-141 | `.lightLevel(15)` is added, every other property untouched by a single character |
| The **0 custom network packets** red line (§1.1 `:44`, §1.3 `:68`, §6 item 4 `:480` of `22`) | **Abolished outright** | D-146 | ruling #23; not relaxed, not an exception (§4.2) |
| C-6 "0 custom S2C packets" (`00-DECISIONS.md`) | **Abolished outright** | D-146 | the same; the rest of C-6 (sounds / particles going through vanilla packets) is still an implementation fact, merely no longer a prohibition |
| D-50 "the three invasive effects add no `CustomPacketPayload`" (`10-DECISIONS-0.0.2.md:73-76`) | **Project-level prohibition abolished** | D-146 | the prohibition is gone; the fact it describes (the state of those three effects is still synchronised through a hidden `MobEffect`) is unchanged, and this release does not touch those three |
| The packet half of HC-73 of document 26 (`26-PROMPT-MAIN-0.1.0.md:183-185`) | **Abolished** | D-146, D-142 | 32 rewrites it as HC-77; the Mixin half and the `GameRules.register` half are kept |
| D-40 "no Mixin introduced" | **Kept (not superseded)** | D-146 | the only red line ruling #23 keeps |
| D-122's 2 cores per player and D-123's 32×32 core textures | **Kept** | — | this release does not touch them; they are listed here to keep downstream from misreading "the Outpost Core has changed" as "these two have changed as well" |

Points to watch when reading the table: **four things must be named** — ① the farmland model (range coverage → binding); ② the quota carrier (base + command → per-core capacity, no base); ③ the command carrier (`/farmstorm farmlandlimit` → deleted, only the client's `/farmstorm hud` left); ④ the red lines (two → one). Any downstream document that writes about one of these four must land on the corresponding D number per this table.

---

## 6. Rejected options (tombstones)

| # | Rejected option | Reason for rejection | Rejected by whom, when |
|---|---|---|---|
| 1 | replacing the binding system with "**every farmland block in range is protected**" | ① one plot protected by several cores at once means dismantling any one core drags the other core's land with it — exactly the bug `plan0.1.1.txt:14` is there to fix; ② it cannot answer "whose plot is this", and neither capacity nor quota has anywhere to be computed; ③ a direct conflict with "one core manages 16 blocks at most" | the author, v0.1.1 interview (rulings #12, #17) |
| 2 | driving the client overlay switch with **server config** | server config cannot change client rendering: a `ModConfig.Type.SERVER` value takes effect in the server process only and the client render thread cannot read it; a client can only persist its own settings through CLIENT config. The only `registerConfig` in this repository is SERVER (`Farmstorm.java:56`), and 0 of the 21 classes under `client/` read config (`BRIEF` §1.5) | the author, v0.1.1 interview (ruling #21 chose client keys + a client command + CLIENT config); the technical side is recorded by `BRIEF` §1.4 / §1.5 |
| 3 | showing, instead of everything, only "**the cores inside loaded chunks**" | a core outside view distance is never shown and the requirement "which cores do I have" simply fails; when the player is far away the overlay silently drops lines, and which lines it drops depends on the loading state, so it is not reproducible | the author, v0.1.1 interview (ruling #22 chose pushing the full list) |
| 4 | **force-loading chunks** so the client can see them (instead of a network packet) | technically unsound: the client only receives chunks within view distance, and a chunk loaded by a ticket on the server is not sent to the client by that; it and the ticket of D-145 are two different things and not interchangeable | the author, v0.1.1 interview (the ticket of ruling #24 serves the server run time only; ruling #22 chose the network packet) |
| 5 | the **vanilla text UI** (boss bar / scoreboard) instead of a top-left overlay | both are vanilla global display channels with a position the vanilla game fixes (the boss bar centred at the top, the scoreboard on the right); neither can reach a designated top-left coordinate, nor be dragged, nor be split line by line per core; squeezing it in would also collide with the existing uses of the boss bar / sidebar | the author, v0.1.1 interview (ruling #21 requires draggable + hideable) |
| 6 | the 0-packet hack of "**an invisible marker entity + `CustomName` carrying the coordinates**" | ① `CustomName` is a display string, not a data channel, and "dimension + coordinates + ownership + order" would have to be encoded in a one-dimensional string; ② an entity is bound by "the client only knows the chunks it has loaded" just the same, so the problem of tombstone 3 comes back as it was; ③ one entity per core = one entity sync and one unload handling per core; ④ after D-146 abolishes the red line, the "0 packets" gain of this hack equals 0 | the author, v0.1.1 interview (ruling #22 chose the cleaner option) |
| 7 | "**when occluded, move automatically to the nearest unoccluded spot to the top-left corner**" instead of drag positioning | ① it needs an occlusion intersection of the overlay with every vanilla UI / other mods' HUD every frame, with both cost and uncertainty high; ② "closest to the top-left corner and unoccluded" may have no solution when the overlay itself fills the top-left corner; ③ dragging is deterministic and the player can place it himself. **`plan0.1.1.txt:17` requires automatic avoidance in the original text, and ruling #21 re-adjudicated it into dragging + no automatic avoidance** | the author, v0.1.1 interview (ruling #21 states "no automatic avoidance" in so many words) |
| 8 | giving the Pumpkin Turret **soft homing** (reusing the shulker's `ShulkerBullet` homing) | ① soft homing would introduce two new parameters, a turn-rate cap and a maximum chase duration, which the author did not give; ② reusing the vanilla homing means changing the entity superclass (currently `extends ThrowableItemProjectile`), which amounts to rewriting the missile; ③ the vanilla homing circles at point-blank range just the same and does not solve the phenomenon the author actually sees; ④ homing would make the missile "always hit", the opposite of the straight line + server authority design of `TurretBullet`. **`plan0.1.1.txt:27` requires homing in the original text, and ruling #8 re-adjudicated it into fixing only the close-range circling / jitter** | the author, v0.1.1 interview (ruling #8) |
| 9 | **block-entity sync** (`getUpdateTag` / `ClientboundBlockEntityDataPacket`) instead of a custom packet | ① there is data only when the client has loaded the chunk the block is in, so "which cores do I have" it cannot answer; ② it covers only "the blocks the client already knows", the same class as tombstone 3; ③ an ordered list of 8 lines needs a complete snapshot in one go, while BE sync is sent block by block and the client would have to assemble the order itself | the author, v0.1.1 interview (ruling #22); the technical side is checked against the BE-sync precedents (2) of `BRIEF` §1.4 |

---

## 7. Leftovers of this release (registered, not counted as defects)

The five items below are **deliberately not done in this release**, not omissions. Downstream documents may cite them, but must not write them as to-dos or defects.

1. **The "green +" rule is voided globally, but this release regenerates `wild_apple` only, a single image, and the other 24 are left to the next release for batch regeneration**. 25 carry the `+` (only `converter` of the 26 is exempt from stacking), and after this release's handling 24 are still the old look (25 − 1 = 24). The rule is already abolished at the code level (the generator's mechanism deleted), so the next release's regeneration is pure execution and needs no new decision (ruling #2).
2. **The "11 colours / 126 transparent" of `wild_apple.png` in the existing documents 22 / 26 / 27 is wrong**; measured it is **88 deduplicated RGBA / 107 fully transparent** (`22-DECISIONS-0.1.0.md:581`, `26-PROMPT-MAIN-0.1.0.md:199`, `27-PROMPT-TASKS-0.1.0.md:405`). Those three are frozen and this release **does not change them**; new documents must never cite the old values, and where they cite, they cite the measured values of D-126 (88 colours / 107 transparent / 571 B).
3. **The explanations of `/farmstorm farmlandlimit` in documents 22 / 24 / 25 / 26 / 27 lapse with the deletion of the command** (typical places: D-116 and row 18 of §4.2 of `22`; `README.md:191`). Those documents are frozen and not edited, and D-140 prevails; the README falls in the increment range, and its `:191` sentence is to be changed together with the upgrade note of D-147 (RT-40).
4. **Item 4 of D-118 of document 22, "the same farmland block inside two cores' ranges counts as one block", still holds under the binding system**, but the reason it holds has changed from "counting de-duplication" to "the unique binding" (D-134). The original text of 22 is not edited; **the basis** of that sentence has changed, and downstream citations must explain it per D-134.
5. **The carrier of the Friend Chives' "audible within 32 blocks"**: it is currently carried by the volume `friend_chives.soundRange / 16 = 2.0` (`BRIEF` §1.8); whether `sounds.json` should additionally carry `"attenuation_distance": 32` is a technical-surface question (the key is currently unset and the vanilla default of 16 applies, while `wild_hunter_loop` in the same file already has the precedent of 32). Handed over to document **30** for a check at the technical layer, and it **does not change the conclusion of D-132**.

### Writing-time report (conflicts with `BRIEF`, and places `BRIEF` does not cover)

Per §6 item 9 of `BRIEF` and §0.3 item 4 of this document, listed item by item for the author, without making a trade-off on one's own and without editing the fact card:

1. **Requirement 3 of the plan has no corresponding author ruling**: the 25 items of §2 of `BRIEF` contain no "Compressed Hay Block sound GRASS → STONE"; its basis is only `plan0.1.1.txt:20-23` and `BRIEF` §1.6 / §3.2 row 3. This document fills it in as D-130; if the author thinks it should not be an entry of its own, deleting D-130 would require D-131 ~ D-147 to be renumbered wholesale (which would affect the mapping table of §2).
2. **Whether hoeing must first pass the 3×3 range gate is not stated by any ruling**: ruling #13 writes only "on a successful hoeing bind to the nearest ally core", with no distance condition; D-118 of 0.1.0 requires "tillable only inside an ally core's 3×3". This document treats it as "two gates stacked" (the range gate still stands, D-135 Notes), the reason being `plan0.1.1.txt:5`'s "unless this document explicitly requires it, the existing content of 0.1.0 must not be modified" — the plan asks only that the multi-core mis-shovelling be fixed, not that the farmland range be opened up; and the existing `NO_CORE` state of `FarmlandGuard` exists exactly for this range gate. **If the author's intent was "the range gate is cancelled and any core is enough to till", then the conclusion of D-135 and row 3 of §5 have to change.**
3. **The matching predicate of the legacy-save backfill is not given by any ruling**: ruling #15 says only "backfill by the new rule", and the only ruling in the new rule that gives a complete predicate is #14 (coverage + alliance + a free slot, nearest first). This document has D-137 reuse the predicate of #14 (marked "supplemented by this document"). If the author wants the backfill looser (say alliance + proximity only, with no range check), D-137 has to change.
4. **The HUD overlay must show all cores (including unloaded chunks and other dimensions) — corrected per the author's ruling**. The first draft of this document, with ruling #20 writing only "coordinates + owner" and the coordinates carrying no dimension, defaulted to "filter by dimension and list only the dimension one is in", **the wrong direction**. The revised rulings #19 / #20 explicitly require **a full cross-dimension display**; what the author chose in the interview is "everything must be shown (including unloaded, crossing dimensions)", and that is exactly the requirement that brought about ruling #22 (breaking the red line to add the network packet). **This item is kept on record because it is the evidence that this round of "cross-checking caught an upstream omission" worked**: the cross-dimension constraint of the overlay was lost when the fact card was relocated, the default inference of document 28 solidified the wrong direction, and only a line-by-line cross-check of 28 against the fact card found it. D-143 has been rewritten per the revised #19 / #20 (the dimension marker at the head of the line, one ordering across dimensions by `placedGameTime`).
5. **A small slip in `BRIEF`**: §1.4 says "0 custom network packets (measured, 17 keywords all with 0 hits)", while §0.1 and §2 item 23 have already abolished that red line — that sentence of §1.4 is the measurement **before** the change and must be read as "0 at that time", not "still 0 in this release". Every reference to "0" in this document means the pre-change state.
6. **The 8-line cap, the default coordinates `(4,4)` and the colours** of the note under the table of §2 of `BRIEF` are explicitly marked "supplemented by that card (not an author ruling, may be overruled)"; this document writes them into D-143 / D-144 with the source marked. If the author wants to change them, only these three values change, and neither the data surface of D-142 nor the choice of carrier of D-144 is affected.

---

## 8. Verdict back-fill record (the 15 of A-30 ~ A-53 that belong to this document, 2026-11-02)

After the delivery of 29 (design delta), 30 (technical spec), 31 (task breakdown) and 32 (master prompt), **15 upstream-defect adjudications relating to this document** were raised in all (7 from 29 + 2 from 30 + 3 from 31 + 3 from 32), and **all of them were back-filled into this document**. The formal registration of the adjudications is in the adjudication chapter of 31 (the internal ledger `.work/v011-verdicts.md`); this section records only "which decision each one changed", to make the reconciliation item by item easy.

| Adjudication | In one sentence | Which decision it changed | Date |
|---|---|---|---|
| A-30 | after the Pumpkin Turret's missile speed is promoted to config, **the launch point `behavior/impl/PumpkinTurret.java:75` must also read that key** — deleting the constant is not a single-point change | D-129 (Landing site + Notes) | 2026-11-02 |
| A-31 | the Compressed Hay Block sound change actually affects **five** kinds of sound (break / place / step / mine / land); the acceptance items are therefore **2 more** than the original estimate (3 → 5) | D-130 (Notes) | 2026-11-02 |
| A-32 | the generator's size assertion and the "non-GUI is always 16×16" of `expected_size()` must be **opened up together**, otherwise a 64×64 pass-through output goes straight to `SystemExit` | §3.2 row 14, §3.3 row 8 (resting on D-126) | 2026-11-02 |
| A-33 | the convention of the Friend Chives' "200": **both the field length and the application base take `friend_chives.effectTicks`**, the amount applied being `effectTicks − x`; 200 is only the default value | D-131 (Conclusion + Notes) | 2026-11-02 |
| A-34 | the overlay's "8 lines" is **8 core lines**, and `+N` **adds 1 more line** — **9 lines** at most | D-143 (Conclusion item 4) | 2026-11-02 |
| A-35 | when a core is destroyed, "no successor found + the chunk holding that farmland is not loaded" → **remove the binding only, the world untouched** (no chunk loading for a settlement) | D-136 (Notes) | 2026-11-02 |
| A-36 | the traversal order of the legacy-save backfill pinned down as **the insertion order of `FarmlandLedger.byOwner` (`LinkedHashSet`)** — **overturned by A-47** (changed to the `BlockPos` single-level total order: y → z → x) | D-137 (Notes) | 2026-11-02 |
| A-38 | the persisted file of force-loading is not `level.dat` but **`world/data/chunks.dat`** (`ForcedChunksSavedData.FILE_ID = "chunks"`); `setChunkForced()` and `TicketController#forceChunk` **both write it ⇒ both are rejected**, only a bare `TicketType` + `addRegionTicket` | D-145 (Conclusion + Notes), §2 row 24 | 2026-11-02 |
| A-39 | "ties by placement order" goes by **`placedGameTime` ascending**, and by coordinate lexicographic order at the same instant; `byOwner` is a `HashMap`, and **no reliance may be placed on the insertion order of any Map / list** | D-135 (Notes) | 2026-11-02 |
| A-45 | the trample protection surface goes by "**protected as long as bound**" (`protectsFromTrample` has no inquirer parameter and cannot answer "who stepped on it"); the quota gate and the overlay still go by the live ally judgement | D-138 (Notes) | 2026-11-02 |
| A-47 | the legacy-save backfill traversal order **overturns A-36**: sort by the **`BlockPos` single-level total order (`BlockPos#compareTo`: y → z → x)** first and then handle them one by one; "by `byOwner` insertion order" is void (`byOwner` is a `HashMap`, and a 0.1.0 legacy save has no `orphans` to rely on; the ledger has no dimension field ⇒ no dimension level is set) | D-137 (Notes) | 2026-11-02 |
| A-48 | the language-key arithmetic gains `+2` (the two description keys of the `KeyMapping`s); `k = 3` ⇒ **323** | §3.4 count arithmetic | 2026-11-02 |
| A-50 | the projectile base class is at **`entity/ThrownCropEntity.java`** and is not under `entity/thrown/` (measured by `Test-Path`) | D-131 (Landing site) | 2026-11-02 |
| A-51 | the jar name in `一键构建.cmd` is at **`:11` (release) and `:12` (sources)**, not `:10-11` (`:10` being `mkdir "dist"`) | D-147 (Basis + Landing site) | 2026-11-02 |
| A-53 | adding `gameTime` to `OutpostCoreIndex.Entry` and persisting it = the **second** save-format change of this release (the first being the farmland ledger v1 → v2) | §3.2 row 15, §3.4 save format | 2026-11-02 |

Three notes on the convention:

1. This section collects only the adjudications **back-filled into this document**. Adjudications landing in the acceptance chapter of 31 or the technical chapter of 30 are out of this section's scope — they change not one character of any conclusion of this document.
2. Each adjudication touches only **the few fields of the corresponding decision** (named in the table) and the necessary cross-indexes; the other decisions, the ruling rows of §2, the existing rows of §3, §5 and §6 are unchanged by not one character (§2 only gained an erratum note under its table; the ruling rows themselves were not changed).
3. Where an adjudication conflicts with the body of this document, **the body after the back-fill** prevails; this section is only an index; the date is uniformly recorded as the writing day of this round, 2026-11-02.
