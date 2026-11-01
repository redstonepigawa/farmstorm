# 23 · v0.1.0 Design Delta

| Item | Value |
|---|---|
| Document | 23-DESIGN-0.1.0 |
| Version | v0.1.0 |
| Status | Design final |
| Upstream | `docs/zh/22-DECISIONS-0.1.0.md` (the single source of truth, D-103 ~ D-123, 21 in total), `plan0.1.0.txt` (the author's original text, 51 lines, 9 requirements), `.work/v010-BRIEF.md` (construction fact card, 252 lines, read-only), `docs/zh/17-DESIGN-0.0.3.md` (the source of this document's style and structure) |
| Downstream | `24-TECH-0.1.0.md`, `25-TASKS-0.1.0.md` |
| Language | Body text in Chinese; code identifiers, registry names, config keys and registry ids always in English |

---

## 0. Reading conventions

### 0.1 Relationship to 17-DESIGN-0.0.3.md

This document is a **delta**, not a rewrite. The designs and decisions of v0.0.1 ~ v0.0.4 are all final, and of them 0.0.4 is already implemented and released (`dist/farmstorm-0.0.4+mc1.21.1.jar`, 959,376 B). This document redefines none of the existing rules; wherever it says 'carried over', it points back to the corresponding subsection of `01-DESIGN.md`, `11-DESIGN-0.0.2.md`, `17-DESIGN-0.0.3.md` or `docs/zh/00` / `10` / `16`, and the two are read side by side.

v0.1.0 does exactly three things: ① append 1 block (Outpost Core) and 1 custom command; ② change **four classes of existing behaviour** (the charging bar's y coordinate, Puffed Wheat's landing write check, the category and two numbers of Melon Grenade and Melon Slice Bomb, and the crop-growth carrier); ③ replace / add a number of textures. Apart from these three things, everything in 0.0.4 stays as it is (`22-DECISIONS-0.1.0.md` §4 is the sole register).

### 0.2 Markup

| Markup | Meaning |
|---|---|
| 【D-1xx】 | The conclusion comes directly from the corresponding decision in `22-DECISIONS-0.1.0.md` §2 (D-103 ~ D-123) |
| 【P-x】 | The conclusion comes directly from item x of the functional requirements in `plan0.1.0.txt` (items 1 ~ 9, `:7-51`) |
| 【BRIEF §x】 | The conclusion comes directly from the corresponding subsection of `.work/v010-BRIEF.md`. It is the flattened construction-period draft, not a deliverable, and serves only as a source of grounds |
| 【本文补充】 | A value or convention that neither 22 nor BRIEF gives, derived by this document from existing decisions or source-code facts. Each carries one sentence of 'why'; implementation follows it as written, and no second version is established anywhere else |

Every number in this document has only three legitimate sources: it is written in 22, it is written in BRIEF, or it is marked 【本文补充】. A number that is none of the three is a document defect.

### 0.3 Units, rounding and the "bottom-offset" measure

1. Time: seconds / game ticks (tick), **1 second = 20 ticks**.
2. Length: blocks (block). Pixels are used only for the GUI and for textures, written 'GUI px' and 'px' respectively; the two are different dimensions and are never converted into one another.
3. Health and damage: points. Vanilla 2 points = half a heart, so 7.5 points = 3.75 hearts.
4. Dimensions and chunks: 1 chunk = 16 × 16 blocks; 3 × 3 chunks = 48 × 48 blocks (16 × 3 = 48).
5. **The "bottom-offset" measure**: bottom-offset = `guiHeight() − y`, that is, the number of GUI pixels from the screen's bottom edge up to the element's top edge. An element of height h whose top edge sits at bottom-offset a occupies the **half-open interval [a − h, a)**. Every HUD row band in this document is written as such a half-open interval, so that the number 39 does not come out one bar-height different under different notations (§2.3 gives them one by one). Read in the closed-interval notation of 'anchor + height, expanding upward', the same set of pixels is written 'a ~ a + h': the health / hunger row is **39 ~ 48** and the charging bar is **39 ~ 43**; the two notations describe the same set of pixels and reach the same conclusion.
6. Rounding: growth duration **involves no rounding and has no remainder** — the per-segment tick count `d` comes out of `floorMod` as an integer directly (§6.3's A-26 measure); the only place in this document that needs rounding is §8.2's chunk coordinates (rounded down, which applies to negative coordinates too). Nothing is relaxed to floating point.

### 0.4 Distance and range

Carried over from `16-DECISIONS-0.0.3.md` §7.3's refinement of D-20: **traps and turrets use Chebyshev, auras / effect radii use Euclidean**. Among the checks added by this version, mob triggering follows each trap's existing metric (Hu Luobei Chebyshev ≤ 1, Fang Trap Cactus Euclidean ≤ 2.5, Zangief Cane spherical ≤ 1.5, Vibration Bean strictly less than 4.0, 【BRIEF §1.6】); the Outpost Core's range and boundary are a **chunk-aligned rectangle**, use no distance metric at all, and are compared directly by chunk coordinate (§8.2).

### 0.5 Authority and synchronisation

1. **Server-authoritative**: landing writes, block placement / removal, quota accounting, trap triggering, damage settlement and scheduled ticks all happen on the server.
2. **0 custom packets** (D-50, C-6 fully in force): both the dust particles (§7.4) and the boundary particles (§8.7) go over the vanilla `ClientboundLevelParticlesPacket`, with zero new client-side code.
3. **No further vanilla rule object is registered**: the carrier of the farmland quota is the `/farmstorm farmlandlimit <n>` sub-command plus a config default, not a vanilla rule (D-116, RT-25). This version's change count against vanilla rule objects is **0**.

### 0.6 Number discipline of this document

1. **Give only the increment, never the registered-object total.** The registry surface gains +3 in this version (90 → 93), where **the total 93 is confirmed by `24-TECH-0.1.0.md` itself after counting the source class by class and naming every entry**; this document records only 90 + 3 (`22-DECISIONS-0.1.0.md` §0 says in its body text that this is the one cell that went wrong twice, in 0.0.2 and 0.0.3).
2. A count has only three legitimate sources: a table in 22, a list in BRIEF, or a derivation this document counts out row by row and marks as such (the last always comes with its arithmetic).
3. **An increment is not a total**: +3, +11, the 22 profiles and the 18 advancements are four numbers of different dimensions and do not stand in for one another.
4. Texture colour values are always taken as **measured**: this document's palettes were obtained by Pillow decoding the committed PNGs pixel by pixel and tallying them (`Counter`), never by human eyeballing; wherever they differ from BRIEF's notation, the conversion is written out in place (§3.3, §9.1).

---

## 1. Positioning and boundaries of v0.1.0

### 1.1 One-line positioning

v0.1.0 makes increments on top of the released 0.0.4: **players can claim land now** — a new "Outpost Core" block at hardness 30 / blast resistance 1200 ties "how far you may till" and "the farmland quota" together onto itself (tilling is only allowed within a 3 × 3 chunk area, each core gives +16 quota, farmland in range is not trampled, destroying the core turns the ally's farmland to dirt, and allies can see boundary particles); one `/farmstorm farmlandlimit <n>` sub-command supplies the server-side default of the quota (default 0). The other 8 items are **fixing visible defects and retuning numbers**: the charging bar moves up 13 px, 4 growth-stage textures for Friend Chives, Puffed Wheat landing replaces only air and fluids, Compressed Puffed Wheat becomes 3D with its six faces distinguished, the two melons lose Demolition, the Melon Slice Bomb's landing spread is tightened, this mod's crops grow in 20 ~ 45 seconds, four trap adjustments, and Wild Apple gets a new texture.

### 1.2 The 9 items and their sections in this document

| # | plan item (line numbers) | Section of this document | Decision | Delivered form |
|---|---|---|---|---|
| 1 | Charging bar moves up (`:7-10`) | §2 | D-103 | change 1 constant, 39 → 52 |
| 2 | Friend Chives growth-stage material (`:11-14`) | §3 | D-104 | replace 4 × 16 × 16 + change the generator |
| 3 | Puffed Wheat / Compressed Puffed Wheat bug fixes (`:15-19`) | §4 | D-106, D-107 | landing allow-list + item model to 3D + block model with six faces distinguished + 2 new textures |
| 4 | Melon Grenade and Melon Slice Bomb adjustments (`:20-24`) | §5 | D-108, D-109 | remove the category from 2 profiles + change 1 existing default + add 1 new key |
| 5 | Crop growth in 20 ~ 45 seconds (`:25-28`) | §6 | D-110, D-111 | **mechanism replacement**: random tick → scheduled-tick chain |
| 6 | Four trap adjustments (`:29-35`) | §7 | D-112 ~ D-115 | widen the trigger set + immunity gate + dust particles + FIFO index |
| 7 | Farmland quota (`:36-40`) | §8.1, §8.2 | D-116 ~ D-118 | open a new tilling interception + accounting chain |
| 8 | New block "Outpost Core" (`:41-47`) | §8.3 ~ §8.8 | D-119 ~ D-122, D-118, D-123 | registry surface +3, 1 command, 3 × 32 × 32 textures |
| 9 | Wild Apple material (`:48-51`) | §9 | D-105 | replace 1 × 16 × 16 |

Items 7 and 8 are merged into a single body (§8) in this document: the author's original text puts "quota", "range" and "core" in the same paragraph, and splitting them into two sections would break the causal chain "the core is the single source of quota and range".

### 1.3 Number conventions (increments only)

| Metric | This version's increment | Detail |
|---|---|---|
| `blocks` | **+1** | `outpost_core` (Outpost Core) |
| `block_entities` | **+1** | `outpost_core`'s `BlockEntityType` |
| `items` | **+1** | `outpost_core`'s `BlockItem` |
| The other 9 categories of registry object | **+0** | `entity_type` / `mob_effect` / `sound_event` / `damage_type` / `recipe_type` / `recipe_serializer` / `creative_mode_tab` / `data_component_type` / `menu_type` are all +0 |
| Registry objects, total | **+3** (90 → 93) | 93 is confirmed by `24-TECH-0.1.0.md` after counting class by class and naming every entry; this document records only 90 + 3 |
| Crop profiles | **±0** (still 22) | §5.2 changes only the category set of 2 profiles, adding and removing nothing |
| Advancements | **+0** (still 18) | none of the 9 requirements asks for an advancement |
| config | **12 rows** = **11** new + **1** default changed | 155 → 166 (§10.2) |
| PNG | **5** new / **6** replaced in place | new: 3 Outpost Core + 2 Compressed Puffed Wheat top / bottom; replaced: 4 Friend Chives + 1 Wild Apple + 1 Compressed Puffed Wheat side (§10.3) |
| JSON | **5** new / **2** replaced in place | 3 models for the Outpost Core + 1 loot table + 1 crafting recipe (A-27); 1 Compressed Puffed Wheat block model and 1 item model each (§10.3) |
| Language keys | **not fixed here** | the base is 299 × 2; the row count is counted by `24-TECH-0.1.0.md` after it lists every key (D-116 supplementary item) |
| Custom commands | **+1** | `/farmstorm farmlandlimit <n>` |
| Custom packets / Mixins | **0 / 0** | two red lines, upheld in this version |

### 1.4 Non-goals of this version

The following 10 items are transcribed from the design-relevant part of `22-DECISIONS-0.1.0.md` §6 (originally 15 items); encountering any one of them during implementation is no licence for an exception; if an exception is needed, change 22 first, then change the code.

1. **Register no vanilla rule object**: the farmland quota goes only through `/farmstorm farmlandlimit <n>` + the config default (D-116, 22 §6 item 1).
2. **Do not change vanilla crops**: only `FarmstormCropBlock` changes; the growth of wheat / carrot / potato / beetroot / melon and pumpkin stems is not touched at all, and neither is the global random-tick speed (D-110, 22 §6 item 2, RT-24).
3. **Add no crop profile**: `CropProfile.ALL` stays at 22, the 23rd entry is not added, and the enum values of `CropType` / `CropCategory` do not change (22 §6 item 3).
4. **No custom packets for the Outpost Core or farmland**: `CustomPacketPayload` is still 0 and `@Mixin` is still 0 (22 §6 item 4).
5. **No 512 × 512 assets**: only the three `a_runic` textures of the `32x32` set are used; the other three style sets do not enter this version (D-123, 22 §6 item 5).
6. **Do not touch the three deployment safety valves**: `DEPLOY_COOLDOWN_TICKS` 5 / `MAX_DEPLOYED_PER_CHUNK` 8 / `MAX_DEPLOYED_PER_PLAYER` 32 — not one of them changes; 16 is a gameplay allowance, not a safety valve (D-115, 22 §6 item 7).
7. **No "ally instant-break" and no custom mining speed for the Outpost Core**: any tier of pickaxe + hardness 30 is the final feel (D-119, 22 §6 item 8).
8. **Add no config key for the 3 × 3 range, the 48 × 48 boundary, the boundary granularity or Zangief Cane's mob settlement** (D-118, D-121, D-114, 22 §6 item 9).
9. **Do not write "remove DEMOLITION" as "reduce demolition capability"**: it is the only criterion; removing it means it simply cannot demolish any more (D-108, BRIEF §6 item 4).
10. **Do not retroactively touch existing farmland**: however the quota changes, farmland that is already tilled neither disappears nor degrades (D-116 item 3, BRIEF §2 item 5).

### 1.5 The three seams with 0.0.4

| Seam | Boundary | Grounds |
|---|---|---|
| Registry side | only new entries are added; existing entries are neither reordered nor removed; of the existing config defaults exactly one is changed, `melon_grenade.horizontalJitter` (0.3 → 0.05), and the other 154 are untouched character for character | `22-DECISIONS-0.1.0.md` §5.2, §5.3 |
| Data side | 1 new shaped recipe (four corners stone / centre diamond / four redstone → 1 core); the Outpost Core's block entity stores only the placer's UUID and the placement tick, and the team relation is resolved live at read time (an extension of C-9, 22 §7.2) | BRIEF §2 items 7, 15, 22; D-119, D-122 |
| Presentation side | everything goes over vanilla packets: the dust and boundary particles are both `ClientboundLevelParticlesPacket`; the charging bar is still drawn in `RenderGuiEvent.Post`; the three 32 × 32 textures do not change the sampling mode (RT-29) | D-113, D-121, D-103, D-123 |

---

## 2. Wild Hunter charging bar position

### 2.1 One-line positioning

Lift "Wild Hunter"'s two sprint charge bars from bottom-offset **39** to bottom-offset **52** (13 px upward), because the anchor 39 is exactly the anchor of the vanilla **health / hunger row**, and the 4 px-high charge bar (6 px including its 1 px outline) lies squarely on the heart and drumstick cells. This item changes one constant only: bar width / bar height / spacing / centring algorithm / the four colours / the per-cell fill order are all untouched.

### 2.2 Current geometry

| Quantity | Value | Source |
|---|---|---|
| Number of bars | 2 (`MAX_CHARGES`) | `client/WildHunterDashClient.java:25`; `12-TECH-0.0.2.md` §3.12 row 4 says "2 charges" |
| Size of one bar | 20 × 4 GUI px | `client/WildHunterHud.java:23-24`【BRIEF §1.1】 |
| Gap between the two bars | 4 GUI px | `WildHunterHud.java:25`【BRIEF §1.1】 |
| Total width | 44 = 20 × 2 + 4 | the comment at `:22` and the arithmetic at `:51-52` of the same file (2 × 20 + 1 × 4 = 44) |
| y coordinate | `int y = screenHeight - BAR_Y_FROM_BOTTOM`, constant = **39** | `WildHunterHud.java:54`, `:26`【BRIEF §1.1】 |
| Outline | 1 px outward on each side (`fill(x-1, y-1, x+BAR_WIDTH+1, y+BAR_HEIGHT+1)`) | `WildHunterHud.java:59` |
| Draw timing | `RenderGuiEvent.Post` (after the whole vanilla HUD has been drawn) | `WildHunterHud.java:39-40`【BRIEF §1.1】 |
| Scaling measure | `graphics.guiHeight()` / `guiWidth()` (already-scaled GUI pixels) | `WildHunterHud.java:49-50`【BRIEF §1.1】 |

### 2.3 The four row bands of the vanilla HUD

The vanilla `Gui`'s row positions are determined by four fixed anchors, all converted by "bottom-offset" (half-open intervals, see §0.3 item 5). The anchor expressions in the four rows below are copied directly from the decompiled source (`net/minecraft/client/gui/Gui.java` in NeoForge 21.1.213's `neoforge-21.1.213-sources.jar`); the interval conversions are marked 【本文补充】.

| Row | Anchor expression (source) | Top edge, bottom-offset | Height | Occupied interval (bottom-offset, px) |
|---|---|---|---|---|
| Hotbar | `blitSprite(HOTBAR_SPRITE, i - 91, guiHeight() - 22, 182, 22)` (`Gui.java:608`) | 22 | 22 | **[0, 22)** |
| Experience bar | `int l = guiHeight() - 32 + 3`, then `blitSprite(..., 182, 5)` (`Gui.java:681`, `:683`) | 29 | 5 | **[24, 29)** |
| Health + hunger | `int j1 = guiHeight() - leftHeight` (`Gui.java:904`), `leftHeight` / `rightHeight` initialised to **39** in the same method (`:232-233`) | 39 | 9 | **[30, 39)** |
| Charging bar (0.0.4) | `int y = screenHeight - BAR_Y_FROM_BOTTOM` (39) | 39 | 4 (6 including the outline) | **[35, 39)** (with the outline **[34, 40)**) |

Two points to take from the table:

1. **The health / hunger row's anchor is exactly 39**, **the same number** as `BAR_Y_FROM_BOTTOM`'s current value. This is no coincidence: 0.0.4 placed the charging bar "above the hotbar, above the experience bar" but picked the health / hunger row's anchor to do it.
2. The experience bar occupies **[24, 29)**, which is to say there is still a 6 px gap between the original position and the experience bar (29 → 35) — 0.0.4 did avoid the experience bar; what it collided with is **the row above that one**.

### 2.4 Why the original position was obstructed: the arithmetic

Put the three things on the same bottom-offset coordinate and the conflict surfaces (all converted per §0.3 item 5, marked 【本文补充】):

1. **Vertically**: the charging bar's fill occupies **[35, 39)**, the health / hunger row occupies **[30, 39)**. The former is a **proper subset** of the latter — the 4 px-high bar falls without the least deviation inside the 9 px-high band of heart and drumstick cells. Counting the 1 px outline, the bar occupies **[34, 40)**, the overlap [34, 39) is **5 px** in total, and only the topmost 1 px (bottom-offset 39 ~ 40) passes the band's upper edge. Read in closed-interval notation this is: band **39 ~ 48**, bar **39 ~ 43**, the bar fully enclosed by this row.
2. **Horizontally**: the hearts start at `guiWidth()/2 - 91` and run rightwards, the hunger cells run out to `guiWidth()/2 + 91` (`Gui.java:902-903`), so only **20 px** in the middle is blank (10 icons 9 px wide on each side ⇒ 91 − 10 = 81 px of icon area). The 44 px charging bar, centred, occupies `[−22, +22]` ⇒ it presses inward on each side by 22 − 10 = **12 px**, that is the innermost 9 px icon plus 3 px. This is exactly what the author meant by "blocked by the health bar and the hunger bar" — **one icon covered on each side**.
3. **Draw order cannot save it**: `RenderGuiEvent.Post` runs after the vanilla HUD, so the bar is **drawn on top of** the hearts rather than being covered by them. Both covering and being covered look the same on screen anyway — "the same band of pixels stuffed with two things" — and the author's impression matches, so this item changes the coordinate only, not the draw timing (D-103 says explicitly "independent of draw timing").

### 2.5 Geometry after the change and the three clearances

After `BAR_Y_FROM_BOTTOM` becomes **52** (D-103; 52 is the value the author adjudicated 【BRIEF §2 item 14】):

| Item | Value (px) | Arithmetic |
|---|---|---|
| Charging bar, fill interval | **[48, 52)** | 52 − 4 = 48 |
| Charging bar, interval including the outline | **[47, 53)** | 52 − 5 = 47; 52 + 1 = 53 (1 px outward at the bottom edge and at the top edge) |
| Amount lifted | **13** | 52 − 39 = 13【D-103】 |
| Clearance to the health / hunger row | **9** (fill) / 8 (outline) | 48 − 39 = 9; 47 − 39 = 8 |
| Clearance to the experience bar | **19** (fill) / 18 (outline) | 48 − 29 = 19; 47 − 29 = 18 |
| Clearance to the hotbar | **26** (fill) / 25 (outline) | 48 − 22 = 26; 47 − 22 = 25 |
| Bar's top edge from the screen bottom | **52** | the constant value itself |

Three concluding sentences: ① the bar no longer shares a pixel row with **any row of the vanilla HUD**; ② all three clearances (9 / 19 / 26 px) are greater than 0, and the smallest, 9 px, is exactly the height of the health / hunger row — that is, the bar has been moved to "precisely one whole row above"; ③ 52 is a **constant** and does not vary with resolution (D-103 item 1 explicitly introduces no second adaptive formula), so the same code is "bottom-offset 52" under any GUI scale.

### 2.6 Do-not-touch list

| Item | Why it is not touched |
|---|---|
| `BAR_WIDTH` 20 / `BAR_HEIGHT` 4 / `BAR_GAP` 4 / total width 44 | unrelated to the y coordinate; touching them equals changing things outside requirement 1 (the same-direction measure of 22 §6 item 10) |
| The 2 bars, the four colours (`COLOR_FULL` / `COLOR_CHARGING` / `COLOR_EMPTY` / `COLOR_BORDER`), the per-cell fill order | as above |
| The line `y = screenHeight - BAR_Y_FROM_BOTTOM` | only the constant changes, not the formula (D-103 item 1) |
| The draw timing `RenderGuiEvent.Post`, the two early returns on `hideGui` / `screen != null` | D-103 says explicitly that it has nothing to do with this item |
| `HOTBAR_WIDTH = 182` (declared but unused) | 22 §6 item 10: deleting it is a change unrelated to the requirement |

### 2.7 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| Maximum GUI scale, minimum window (`guiHeight()` = 240) | the bar occupies [48, 52), its top edge is 240 − 52 = 188 px from the screen top, so it cannot go out of bounds; the outline's top edge leaves the screen only when `guiHeight() < 53`, which the vanilla minimum GUI height of 240 never triggers (this document's supplement: 52 + 1 = 53) |
| Player has a screen open (inventory / chat) or has hidden the HUD with F1 | the two existing early returns apply, nothing is drawn (`WildHunterHud.java:45-47`) |
| Player does not have the `wild_hunter` effect | `WildHunterClientState.active()` is false, nothing is drawn (`:41-43`) |
| Death screen / spectator | the death screen is stopped by the `screen != null` early return; a spectator cannot obtain the effect, so `active()` is false |
| Player wearing armour / having absorption | vanilla accumulates `leftHeight` and moves the health row upward, and the charging bar **does not follow** (D-103 introduces no adaptivity). The bar and the newly added row may then overlap; this is the known boundary drawn by this item (introducing no adaptive formula means accepting it) and is registered in this section |

---

## 3. Friend Chives growth-stage material

### 3.1 One-line positioning

The four `friend_chives_stage0..3.png` go from "16 × 16 solid magenta squares" to **plant silhouettes that progress by stage**: the height aligns with the collision box (4 / 8 / 12 / 16 px), the leaf count runs 2 → 3 → 5 → 7, and the palette **reuses the 4 greens of `power_beet` already in the repository**. **At the same time** the generator changes, so that re-running `tools/gen_textures.py` produces output identical to the committed files — this is the only reason this item must touch code.

### 3.2 Current state: four solid magenta squares

| Item | Measurement / fact | Source |
|---|---|---|
| Size | all 4 are 16 × 16 | 【BRIEF §1.2】 |
| Colour | all 4 are **monochrome** `#D81E8A`, 256 / 256 pixels opaque (decoded here, `Counter` has 1 key) | 【BRIEF §1.2】's "pure magenta" + this document's measurement |
| Who writes them | the generator: `PLACEHOLDER_COLOR = (0xD8, 0x1E, 0x8A, 0xFF)` (`tools/gen_textures.py:64`), `make_placeholder()` (`:512-514`), the loop over the 4 (`:659-666`) | 【BRIEF §1.2】 |
| Model | `parent: minecraft:block/crop` + `render_type: cutout`, with the blockstate's four variants pointing at `farmstorm:block/friend_chives_stage0..3` | 【BRIEF §1.2】; `src/generated/resources/assets/farmstorm/blockstates/friend_chives_crop.json` |
| Collision box | `SHAPE_BY_AGE` four heights: **4 / 8 / 12 / 16 px** | `block/FarmstormCropBlock.java:38-43` |

Two consequences must be stated up front: ① monochrome + fully opaque ⇒ under `cutout` the picture is a **solid magenta square**, not a plant; ② the texture is a full 16 px tall while age 0's collision box is only 4 px ⇒ even with a plant drawing, if all four stages are painted across the full 16 px the picture will still be **misaligned** with the collision box. So this item's shape specification takes `SHAPE_BY_AGE` as its skeleton (§3.4).

### 3.3 Art-style baseline (measured)

Two sets of crop textures in the same directory are this mod's existing art-style baseline. The measured conclusions are as follows (this document's supplement: Pillow's per-pixel `Counter`; where this differs from BRIEF's notation, the conversion is given):

| Reference | Colours per stage (stage0 → 3) | Union of the four stages | Opaque pixels (stage0 → 3) | BRIEF §1.2's notation | Conversion |
|---|---|---|---|---|---|
| `power_beet` | 3 / 4 / 6 / 7 | **7** (4 green + 3 red) | 5 / 13 / 66 / 116 | "4 colours, about 251 / 256 transparent" | 4 = the 4 colours of the green family (`#266325` `#2B702A` `#4A8F28` `#55AB2D`); the 3 reds appear only at stage2 / 3, so only a per-stage count matches; 251 transparent = stage0's 5 opaque pixels |
| `ubi_server` | 4 / 5 / 7 / 8 | **8** (4 green + 4 brown-gold) | 8 / 13 / 38 / 81 | "9 colours, 175 / 256 transparent" | 9 = the 8 colours + the transparent entry; 175 transparent = stage3's 81 opaque pixels |

Two art-style criteria (both references satisfy them, and the new textures must):

1. **Single-digit colour steps**: at most 9 colours in one stage, at most 8 in the union of the four stages; no gradients and no anti-aliasing (anti-aliasing on 16 × 16 only smears the silhouette).
2. **Largely transparent**: the opaque pixel count grows monotonically from single digits to slightly over a hundred across the four stages (5 → 116 / 8 → 81), that is, "the bigger it grows, the more pixels show"; silhouette readability rests on **shape**, not on area.

`#D81E8A` **may not remain in a single pixel** of the new textures: it is the signal colour for "the generator found no asset", and leaving it in the finished product would make acceptance unable to tell "drawn" from "not drawn" (this document's supplement).

### 3.4 Shape specification for the four stages

| Stage | age | Texture height | Leaves | Stem | Colours | Target opaque pixels |
|---|---|---|---|---|---|---|
| 0 | 0 | **4 px** (the bottom 4 rows) | 2 (one single-pixel leaf left and right) | 1 px wide in the centre, 2 px tall | `#266325` + `#2B702A` | **6 / 256** |
| 1 | 1 | **8 px** | 3 (1 left, 2 right, offset by 1 px vertically) | 1 px wide in the centre, 4 px tall | adds `#4A8F28` | **14 / 256** |
| 2 | 2 | **12 px** | 5 (3 left, 2 right, interleaved) | 1 px wide in the centre, 6 px tall | adds `#55AB2D` | **40 / 256** |
| 3 | 3 | **16 px** (full height) | 7 (4 left, 3 right) + 2 px of bolting at the top | 1 px wide in the centre, 8 px tall | all 4 colours used | **90 / 256** |

**Why the heights are 4 / 8 / 12 / 16**: the four values are copied directly from `SHAPE_BY_AGE`'s four collision-box heights (`block/FarmstormCropBlock.java:38-43`), so that "the height you see" and "the height you stand on" correspond one to one. This is not an artistic preference; it is the only thing on screen that lets the player judge which stage the crop is in (this document's supplement).

**Why the palette is exactly these 4 greens**: `#266325` `#2B702A` `#4A8F28` `#55AB2D` are the green family already committed in `power_beet`, and in ascending brightness they fall in exactly this order (relative luminance 74 < 83 < 111 < 131). **Reusing the other's colour values without introducing a new hue** is so that Friend Chives and the existing crops look like one and the same art style in the field; a newly introduced hue would make it look like a block from another mod (this document's supplement).

**Colour progression (bottom to top)**: the stem uses the darkest `#266325`, the leaf base `#2B702A`, the leaf middle `#4A8F28`, and the leaf tips and the bolting at the top the brightest `#55AB2D`. All four stages share the same brightness gradient, and advancing a stage **only adds pixels, never changes hue**, so that from a distance it looks like "the same plant has grown" rather than "a different plant has been swapped in".

**Contiguity rule**: a leaf must **share at least 1 pixel edge** with the stem (4-adjacency); isolated diagonal pixels are not allowed — under `cutout` an isolated diagonal point has half of it eaten by neighbour filtering and a jagged notch appears at the edge (this document's supplement).

### 3.5 How the generator changes

Only one file changes, `tools/gen_textures.py`, in three places (each named down to the line):

| # | Location | Change | Criterion |
|---|---|---|---|
| 1 | `ITEM_DRAWERS` (the drawing-method registry starting at `tools/gen_textures.py:632`) | add 4 keys: `friend_chives_stage0` / `..._stage1` / `..._stage2` / `..._stage3`, each returning the 16 × 16 image corresponding to §3.4's shape table | `compose()` (`:670-673`) already has the branch "if `drawer` is non-null, prefer the drawing method", so the composition flow needs no change |
| 2 | the 4-image loop in `build_additions_specs()` (`:659-666`) | add `drawer=f"friend_chives_stage{index}"` to the `Spec(...)`, and change the "magenta `#D81E8A` placeholder, not final" wording in `zh_name` | after the change these 4 no longer fall through to `make_placeholder()` (the `resolved.image is None` branch at `:677`) |
| 3 | the drawing implementation | add a function that draws the plant per stage: the pixel table is looked up by (stage, dx, dy), and **random numbers are forbidden** | two re-runs must produce byte-identical output — the criterion "re-running equals what is committed" rests on determinism (D-104 supplementary item) |

**`PLACEHOLDER_COLOR` and `make_placeholder()` are kept** (`tools/gen_textures.py:64`, `:512-514`): they still cover other items whose "source material is missing", and deleting them would leave those items with no image at all. This item only **moves these 4 out of** the placeholder branch; it does not delete the placeholder mechanism (this document's supplement).

**Acceptance criterion (the only one)**: delete the 4 PNGs, re-run the generator, and the 4 produced must be **byte-for-byte identical** to the committed files; failure means the drawing method has picked up a time stamp, a random number or a dependency on iteration order.

### 3.6 Do-not-touch list

| Item | Why it is not touched |
|---|---|
| The blockstate's four variants and the four model jsons | only the texture content changes; the model stays `parent: minecraft:block/crop` + `cutout` (D-104) |
| The registry id `farmstorm:friend_chives_crop`, the language keys, the crop profile | D-104 says explicitly they are not touched |
| `SHAPE_BY_AGE`'s 4 / 8 / 12 / 16 | the four heights are **the target the textures align to**, not the object of change (§3.4) |
| Crop growth behaviour | this item's 20 ~ 45 seconds is handled uniformly by §6 and has nothing to do with this item |

### 3.7 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| The author later swaps in the final hand-drawn art | replace the 4 same-named PNGs only; but the generator's corresponding drawing method **must be changed at the same time**, otherwise the next re-run overwrites them with this round's images (the same mechanism as the magenta overwrite, the ⚠️ of 【BRIEF §1.2】) |
| The generator re-runs on other items | the 4 outputs must remain unchanged — this is §3.5's acceptance criterion, not a coincidence |
| Semi-transparent pixels appear at a texture edge | not allowed: `cutout` clips by an alpha threshold, and a semi-transparent edge produces a black fringe. Alpha may only be 0 or 255 (this document's supplement) |
| The age 3 texture Z-fights with neighbouring blocks | the crop is `noCollission` + `cutout`, so there is no depth-write problem; a full 16 px height also stays inside the block bounds (`SHAPE_BY_AGE[3]` is a full 16 × 16 × 16 box) |
| The four stage heights are 4 / 8 / 12 / 16, but the `block/crop` template scales again | `minecraft:block/crop`'s parent model `block/cross` does not scale the texture, it only lays the 16 × 16 texture on a single plane; hence the texture's pixel row count = the visible height in the world (this document's supplement: the template carries no `display` scaling, and `crop`'s `rotation` variants do not take part in ordinary block states either) |

---

## 4. Puffed Wheat and Compressed Puffed Wheat

### 4.1 One-line positioning

Two bug fixes: ① the block the two projectiles generate on landing **replaces air and fluids only** and never touches an ordinary block (layered on top of the existing "skip if resistance ≥ 3.6e6", not replacing it); ② Compressed Puffed Wheat's **item model becomes a solid hay block**, and its landed block becomes **top / side / bottom distinguished**, with the texture's tone changed from a full grey scale to yellow-brown.

### 4.2 The order of the landing write check (D-106)

Today there is a single interception: `level.getBlockState(pos).getBlock().getExplosionResistance() >= BlastRule.UNBREAKABLE_RESISTANCE` (`entity/thrown/PuffedWheatProjectile.java:106`, the threshold 3.6e6 is at `explosion/BlastRule.java:26`). **The loop contains no `canBeReplaced` / `isAir` / fluid / block-entity check at all** (【BRIEF §1.3】), and an ordinary block's blast resistance is far below 3.6e6 ⇒ the whole block gets covered. This is the bug requirement 3 has to fix.

After the change there are **five gates, evaluated in order**:

| Step | Criterion | When it holds | Grounds |
|---|---|---|---|
| 1 | the target block's blast resistance `>= 3.6e6` | **skip (do not write)** | D-38 / T-05, `BlastRule.java:26`, the existing baseline **retained** |
| 2 | `compressed.replaceOnlyAirAndFluid == false` | **write** (back to 0.0.4's behaviour) | the second half of D-106 item 1 |
| 3 | `state.isAir()` | **write** | D-106 item 1; plan `:17` |
| 4 | the target block **is itself a liquid** (water / lava) | **write** | D-106 item 1; plan `:17` |
| 5 | everything else (ordinary blocks, waterlogged blocks, non-replaceable blocks, positions with a block entity) | **skip (do not write)** | D-106 item 1; plan `:17`'s "can no longer replace an ordinary block" |

Four landing points:

1. **Step 1 must come first**: the resistance baseline is an anti-griefing baseline (D-38 / T-05), not a balance value; putting it behind the allow-list would mean that with the switch off even bedrock-level blocks can be covered (D-106 item 1 states explicitly that "the new criterion is an addition, not a replacement").
2. **Step 4's "liquid" means the block itself**, not `getFluidState().isEmpty()`: a waterlogged block (waterlogged stairs, waterlogged fence, kelp) has a **non-empty** fluid state, so judging by fluid state would cover them wholesale as liquids, which directly conflicts with "no longer replaces ordinary blocks". This item settles the semantic measure; the concrete writing is pinned down by `24-TECH-0.1.0.md` (D-106's supplementary item hands the writing to 24).
3. **Positions with a block entity fall to step 5 and are skipped**: `level.setBlock(..., UPDATE_ALL)` would also remove the original block entity and drop its contents, which is a different kind of destruction and not within the semantics of "writing hay" (this document's supplement; D-106 item 1 lists "block entity" and "ordinary block" side by side as not written).
4. **The two projectiles share one implementation**: `CompressedPuffedWheatProjectile` overrides only `fillBlock()` (`entity/thrown/CompressedPuffedWheatProjectile.java:35-37`), and the write loop lives in the parent's `fillHay` (`PuffedWheatProjectile.java:99-113`), so one change takes effect on both sides (D-106's last sentence).

### 4.3 The two-state behaviour of the config switch

`compressed.replaceOnlyAirAndFluid` (boolean, default **true**) decides which branch step 2 takes. The actual outcomes of the two states:

| Target position | `true` (default) | `false` (back to 0.0.4) |
|---|---|---|
| Air | write | write |
| Water / lava | write | write |
| Ordinary block (stone, dirt, wood...) | **do not write** | **write (cover)** |
| Block with a block entity (chest, furnace, deployed block) | **do not write** | **write (cover, the old block entity is removed)** |
| Resistance ≥ 3.6e6 (bedrock, barrier, reinforced block) | do not write | do not write (step 1 intercepts) |

Why the switch exists (D-106 supplementary item): **"who gets covered" is a gameplay-facing criterion**, the same class as the `BlastRule` family's thresholds, and therefore a server-owner adjustable item. Making it a switch also gives the construction period a control instrument: with the switch off, behaviour should match 0.0.4 cell for cell.

### 4.4 Compressed Puffed Wheat's item model becomes 3D (D-107 item 1)

| Item | 0.0.4 | v0.1.0 |
|---|---|---|
| `parent` of `models/item/compressed_puffed_wheat.json` | `minecraft:item/generated` | **`farmstorm:block/compressed_hay_block`** |
| `textures` | `layer0: farmstorm:item/compressed_puffed_wheat` | none (inherits the block model's three-face textures) |
| Appearance in the inventory | a flat 2D image | a small block (isometric 3D) |
| Generation site | the `basicItem(item)` fallback branch at `datagen/ModItemModelProvider.java:48-50` | the special-case form used at `:45-47` of the same file: `withExistingParent(path, modLoc("block/compressed_hay_block"))` |

**Why take the block model as the parent directly instead of drawing a separate inventory-view image**: Compressed Puffed Wheat's object itself is that block; drawing another item image would mean two sets of textures to keep in sync, and the block model already carries the `display` transforms the inventory view needs (`cube_bottom_top`'s parent is `minecraft:block/block`, which supplies `display`) — so the item model needs only one `parent` line and no `display` (this document's supplement; the same form as `compressed_hay_block`'s own item model, whose comment at `ModItemModelProvider.java:46-47` calls this "an ordinary block item reuses the block model directly").

**One resource consequence**: `textures/item/compressed_puffed_wheat.png` is from now on **referenced by no model at all**. This document's measure is "keep the image, do not delete it" — deleting it is a change unrelated to the requirement (the same treatment 22 §6 item 10 gives `HOTBAR_WIDTH`), and the author may at any moment want to fall back to a 2D icon. Registered in the note under §10.3's table.

### 4.5 The landed block distinguishes its six faces (D-107 item 2)

| Item | 0.0.4 | v0.1.0 |
|---|---|---|
| Block model `parent` | `minecraft:block/cube_all` | **`minecraft:block/cube_bottom_top`** |
| Texture slots | one `all` | three: `top` / `side` / `bottom` |
| Generation site | `simpleBlock(block)` at `datagen/ModBlockStateProvider.java:76-78` | a new dedicated branch in the same file, using `models().cubeBottomTop(name, side, bottom, top)` |
| Number of faces | six faces, one material | one each for top / bottom, one shared by the four sides |

Shape, collision box, orientation and rotation are all unchanged: `cube_bottom_top` and `cube_all` have exactly the same element set (a single 16 × 16 × 16 cube), only the texture assignment changes ⇒ **no custom uv and no `uvLock` needed** (the same direction as RT-29's conclusion). The "six faces, one material" half of D-57 is voided by this item and registered in the D-57 row of `22-DECISIONS-0.1.0.md` §7.2; the properties half remains in force.

### 4.6 The three textures' palette (D-107 item 3)

Measured on the current texture `textures/block/compressed_hay_block.png`: 16 × 16, 256 / 256 opaque, **9 grey levels**, pixel distribution `#ADADAD` 29, `#A3A3A3` 40, `#8D8D8D` 65, `#7B7B7B` 54, `#707070` 36, `#656565` 15, `#616161` 4, `#565656` 12, `#4B4B4B` 1 (256 in total; 【BRIEF §1.3】 gives the first five values, the rest are completed by this document's measurement).

**The recolouring rule (this document's supplement)**: keep each pixel's **relative luminance** unchanged and turn the hue only to hay yellow (HSL's H = 42°, S = 0.55, L taking the grey level's L). That way the side texture's pixel layout can be **kept exactly as it is** and only the palette changes — satisfying "grey scale to yellow-brown" without redrawing a 16 × 16.

| Current grey | Pixel count | After (H 42° / S 0.55 / L unchanged) |
|---|---|---|
| `#ADADAD` | 29 | **`#DABF80`** |
| `#A3A3A3` | 40 | **`#D6B770`** |
| `#8D8D8D` | 65 | **`#CCA64E`** |
| `#7B7B7B` | 54 | **`#BF9637`** |
| `#707070` | 36 | **`#AE8932`** |
| `#656565` | 15 | **`#9D7B2D`** |
| `#616161` | 4 | **`#96762C`** |
| `#565656` | 12 | **`#856927`** |
| `#4B4B4B` | 1 | **`#745B22`** |

**Six-face allocation**:

| Texture | Faces used | Pattern | Colour steps used |
|---|---|---|---|
| `compressed_hay_block.png` (**recoloured in place**, D-107 item 3) | the four sides | keeps the current image's pixel layout, only the palette changes | all 9 steps |
| `compressed_hay_block_top.png` (**new**) | top face | stalk cross-section: bundles of horizontal strands, one bundle per 2 rows (bright / dark alternating), with a 1 px outline in the darkest step around the edge | 5 steps: `#9D7B2D` ~ `#DABF80` |
| `compressed_hay_block_bottom.png` (**new**) | bottom face | the same pattern as top but darkened one step overall, with a 2 px-wide cross-lashing in the central 4 × 4 area | 5 steps: `#745B22` ~ `#BF9637` |

Three constraints: ① all three are **16 × 16** (consistent with the block texture's logical grid); ② the side keeps the current image's 9 steps while top / bottom use only 5 of them — top and bottom are "cut faces", and one brightness level less than the side is the common-sense look of a hay bale (this document's supplement); ③ top and bottom **must be distinguishable at a glance** (one step darker + the cross lashing), otherwise the six-face distinction amounts to nothing on screen.

### 4.7 Do-not-touch list

| Item | Grounds |
|---|---|
| Block id `farmstorm:compressed_hay_block`, item id `farmstorm:compressed_puffed_wheat` | D-107 supplementary item |
| `strength(30.0F, 6.0F)`, `requiresCorrectToolForDrops()`, `sound(SoundType.GRASS)`, `mapColor(MapColor.COLOR_BROWN)` | `registry/FarmstormBlocks.java:72-78`; D-107 item 4 |
| The shapeless recipe, the loot table | D-107 supplementary item |
| The landing cube's edge length, the property of writing rather than exploding, the projectile parameters | D-58's boundary measure remains in force |

### 4.8 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| The target position is waterlogged stairs / a waterlogged fence | **do not write** (step 5 skips): it is not air, and the block itself is not a liquid (§4.2 point 2) |
| The target position is underwater kelp / seagrass | kelp is a block whose **block itself is not a liquid** but whose fluid state is non-empty ⇒ do not write; only plain water / plain lava are written |
| The target position is a chest / furnace / deployed block (has a block entity) | do not write (`true` state); the `false` state covers it and removes its block entity, which is 0.0.4's existing behaviour, and one of the points of the switch is that people can reproduce it |
| Writing triggers a block-update cascade (landed hay pressing on sand) | follows vanilla `setBlock(..., UPDATE_ALL)`'s existing behaviour, with no suppression |
| The landing point crosses chunks and some positions are unloaded | the projectile's chunk is necessarily loaded, the landing cube's edge length is bounded (existing parameters), and no extra load-state check is made (this document's supplement: all written positions lie inside the entity's own chunk or an immediately adjacent one) |
| Whether behaviour with the switch off matches 0.0.4 cell for cell | yes — only step 1's resistance baseline remains, exactly equivalent to the existing criterion at `PuffedWheatProjectile.java:106`. This is the control experiment for acceptance |

---

## 5. Melon Grenade and Melon Slice Bomb

### 5.1 One-line positioning

The two melons **can no longer break blocks at all**: the category sets of `melon_grenade` and `melon_slice_bomb` lose `DEMOLITION` and each keeps only `DAMAGE`. The explosion itself, the damage and the knockback are all retained; Melon Slice Bomb's **count is unchanged (4 slices)**, but its horizontal jitter goes 0.3 → **0.05** and its radial initial speed 0.3 → **0.1** (the latter becomes a config key instead of a hard-coded constant), tightening the landing spread to between one third and one sixth of the original magnitude.

### 5.2 Category changes (D-108)

| Profile (index per `16-DECISIONS-0.0.3.md` §4) | Categories before | Categories after | Change |
|---|---|---|---|
| 6 · `melon_slice_bomb` | `DAMAGE`, `DEMOLITION` | **`DAMAGE`** | `DEMOLITION` removed |
| 7 · `melon_grenade` | `DEMOLITION`, `DAMAGE` | **`DAMAGE`** | `DEMOLITION` removed |

The two `categorySet`s are at `util/CropProfile.java:58-61` (Melon Slice Bomb) and `:62-63` (Melon Grenade). The other 20 profiles are untouched character for character, `CropProfile.ALL` stays at **22**, and the Demolition profiles drop **from 4 to 2** (`ubi_server` and `toxic_ubi_server` remain).

**The assertion this forces to change**: the startup self-check at `Farmstorm.java:103-104` writes the `demolition` count as 4, which becomes **2** after the change; leaving it alone makes the startup self-check fail (`taxonomyOk` false, the log prints "破障类 2 → FAIL", 【BRIEF §1.4】's ⚠️). This is registered in row 1 of `22-DECISIONS-0.1.0.md` §4.2.

### 5.3 The criterion chain of "can no longer break blocks at all"

`DEMOLITION` has only 3 code read points in the whole repository (【BRIEF §1.4】), and once the category is removed no path can break a block:

| # | Read point | Role | Behaviour after the category is removed |
|---|---|---|---|
| 1 | `canBreakBlocks()` at `util/CropProfile.java:181-184` | the only category → boolean conversion | always `false` |
| 2 | `forCrop` at `explosion/BlastRule.java:78-80` | derives the demolition policy from the profile | `canBreak` always `false` |
| 3 | the startup self-check assertion at `Farmstorm.java:99-106` | a count check only, takes no part in running | count 4 → 2 (§5.2) |

The runtime chain is `FarmstormExplosion.breakBlocks()` (`explosion/FarmstormExplosion.java:103`), which calls `rule.canBreak(...)` per block (`:121`) ⇒ `canBreak` is always false ⇒ **not one block is broken**. **There is no intermediate state of "reduced demolition capability"** (22 §6 item 11, BRIEF §6 item 4).

One piece of corroborating evidence: `FarmstormExplosion.java:109` also reads vanilla's `GameRules.RULE_MOBGRIEFING` (a vanilla rule, not any rule added by this version), but once the category is removed `getToBlow()` is empty and `:112-115` returns directly — this read point from then on **has no effect whatsoever** on this mod's melons, and the vanilla rule itself is unchanged ( `22-DECISIONS-0.1.0.md` §6 item 1).

### 5.4 The retained explosion, damage and knockback

| Item | Value | Source |
|---|---|---|
| Melon Grenade `explosionPower` / `maxDamage` | **3** / **8** | 【BRIEF §1.4】; `config/FarmstormConfig.java:392-397` |
| Melon Slice Bomb `explosionPower` / `maxDamage` | **2** / **6** | 【BRIEF §1.4】; `:374-379` of the same file |
| Resistance thresholds `grenadeResistThreshold` / `sliceResistThreshold` | **6.0** / **6.0**, unchanged | 【BRIEF §1.4】; D-19 fully in force |
| Explosions settled per tick at most, `MAX_EXPLOSIONS_PER_TICK` | **8**, unchanged | `explosion/ExplosionQueue.java:20`; 【BRIEF §1.4】 |
| Damage channel | the superclass `Explosion` + `hurtCapped()` (the cap is `maxDamage`) | `FarmstormExplosion.java:95-100` |
| Knockback | **retained**: applied by the superclass `Explosion.explode()`, not one line changed in this version | as above (the explosion is still constructed with `Explosion.BlockInteraction.DESTROY`, `:69-73`) |

So "does not blow up blocks" is not "nerfed into a dud": damage, caps, knockback, smoke and sound all remain, **only the block-breaking item has gone** (BRIEF §2 item 9).

### 5.5 Melon Slice Bomb's three numbers (D-109)

| Item | 0.0.4 | v0.1.0 | Carrier |
|---|---|---|---|
| Count, `melon_grenade.spawnCount` | 4 | **4 (unchanged)** | existing key, `config:385` |
| Horizontal jitter upper bound, `melon_grenade.horizontalJitter` | 0.3 | **0.05** | existing key with its **default changed**, `config:391` |
| Radial initial speed | 0.3 (hard-coded `SUBMUNITION_SPEED`, `entity/thrown/MelonGrenadeProjectile.java:26`, `:66`) | **0.1** | **new key**, `melon_grenade.submunitionSpeed` |
| Spawn height offset, `spawnOffsetY` | 1 | **1 (unchanged)** | existing key, `config:388` |

**Why both numbers must be pressed down at once**: the jitter decides the four slices' **initial scatter radius**, the initial speed decides **how fast they spread**; press only one and the other still spreads the landing points out (the vector decomposition in D-109's grounds).

### 5.6 Scatter comparison before and after the tightening

The four quantities are converted directly from §5.5's two items (all marked 【本文补充】, the arithmetic is in the table):

| Quantity | 0.0.4 | v0.1.0 | Ratio | Arithmetic |
|---|---|---|---|---|
| Single-axis jitter upper bound | 0.300 blocks | 0.050 blocks | ÷ 6 | "upper bound of the random x / z offset" (the comment at `config:389-391`) |
| Planar resultant upper bound | **0.424 blocks** | **0.0707 blocks** | ÷ 6 | 0.3 × √2 = 0.424; 0.05 × √2 = 0.0707 |
| Diameter of the four slices' initial ring | **0.85 blocks** | **0.14 blocks** | ÷ 6 | 2 × 0.424; 2 × 0.0707 |
| Horizontal initial speed | 0.30 blocks/tick | 0.10 blocks/tick | ÷ 3 | 0.3 ÷ 0.1 = 3 |
| Maximum horizontal displacement over 10 ticks of flight | 3.00 blocks | 1.00 blocks | ÷ 3 | speed × ticks (no drag or gravity modelled, upper bound only) |
| Maximum horizontal displacement over 20 ticks of flight | 6.00 blocks | 2.00 blocks | ÷ 3 | as above |
| Order of magnitude of the farthest landing point (t = 20 ticks) | **≈ 6.4 blocks** | **≈ 2.1 blocks** | ÷ 3.1 | initial ring + displacement: 0.424 + 6.0; 0.0707 + 2.0 |

In one sentence: **the initial scatter tightens 6-fold, the dispersal speed 3-fold, the farthest landing point about 3-fold**; what the author called "more concentrated landing points" (plan `:23`) is numerically this table. The four slices still exist and still each run the Throwable landing flow again on their own ground collision, they are merely packed into one small cluster.

### 5.7 Do-not-touch list

| Item | Grounds |
|---|---|
| `spawnCount` 4, `spawnOffsetY` 1 | D-109 items 1 and 4 |
| The four slices do not settle immediately and each runs the landing flow again on its own ground collision | D-109 item 4 |
| The two resistance thresholds 6.0 / 6.0 | D-19 fully in force (D-108 item 5) |
| `MAX_EXPLOSIONS_PER_TICK` 8 | 【BRIEF §1.4】 |
| The two items' explosion power / damage caps | D-108 item 4 |
| The sentence at `MelonGrenadeProjectile.java:25` saying "an implementation-detail constant, not in config" | **voided by D-109, deleted together with the comment change** (22 §4.3 item 2); leaving it would contradict the behaviour |

### 5.8 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| The four slices land in water | each one's landing flow is as before (water takes no part in the demolition check and does not affect the category) |
| 0 of the 4 slices land (all blocked / flew out of the world) | no re-issue, no retry — `spawnCount` is the **spawn** count, not the **settle** count (D-109 item 1 guarantees the count only) |
| `spawnCount` is tuned to 0 by the server owner | allowed: the lower bound at `config:385` is 0, and then only Melon Grenade itself settles |
| Several explosions in the same tick | queued by `ExplosionQueue`'s 8 / tick cap; changing the category does not change the queueing semantics |
| The landing chunk unloads mid-flight | the submunitions are entities, entities in an unloaded chunk do not tick, and they continue after reload (existing behaviour, untouched by this version) |

---

## 6. Crop growth time

### 6.1 One-line positioning

The total time from planting to maturity (level 3 of `AGE_3`) for this mod's 15 crop blocks falls in **20.1 ~ 45.0 seconds** (`[402, 900]` ticks), across four stages and **three advances**. The method is to **abandon vanilla random ticks** and drive the crop with a scheduled-tick (`scheduleTick`) chain instead: the three segments are **equal in length**, `d` ticks each, and the total duration = 3 × `d`; `d` is computed on the spot from the coordinate and nothing is stored (A-26).

### 6.2 Current state and target: why the mechanism must change

| Item | Value | Source |
|---|---|---|
| Current fastest (optimal light, hydrated farmland, single plant) | 10 min 14 s = **614 seconds** | 【BRIEF §1.5】 |
| Current state, fully dry farmland | about **1,440 seconds** (24 minutes) | as above |
| Current state, dense planting on dry ground | about **2,640 seconds** (44 minutes) | as above |
| Target interval | **20 ~ 45 seconds** | D-110 item 1; plan `:27` |
| The target is faster than even the current fastest | 614 ÷ 45 ≈ **13.6-fold** | 【BRIEF §1.5】 |
| The target is faster than even the current slowest | 2,640 ÷ 20 = **132-fold** | as above |

The current mechanism is "advance one level with probability `nextInt((int)(25/f) + 1) == 0` on every random tick, provided `getRawBrightness(pos, 0) >= 9`", and the random-tick rate is 3 times per chunk section per tick / 4096 blocks (【BRIEF §1.5】). Tuning the global random-tick speed so that a crop matures in 45 seconds would make every vanilla crop on the whole server shoot up — this is the "global mechanism replacement" RT-24 talks about, and **that road is unreachable parameter-wise** (D-110's grounds).

### 6.3 The duration model and the three-way split

| Step | Rule | Grounds |
|---|---|---|
| 1 | take config `growth.minSeconds` **20** and `growth.maxSeconds` **45** (seconds) and convert them to ticks at 1 second = 20 ticks; the two converted endpoints only frame the interval — the real lower bound is decided by step 2's minimum value of `d` | D-110 supplementary item (the × 20 seconds → ticks conversion) |
| 2 | compute the **per-segment tick count** first: `d = 134 + floorMod(pos.hashCode(), 167)` ⇒ **`d ∈ [134, 300]`** | A-26 (taking document 24's scheme); 167 = 300 − 134 + 1 (the number of values in the closed interval) |
| 3 | the three segments are **equal**, `d` ticks each ⇒ **`T = 3d`**, that is **`T ∈ [402, 900]`** | A-26 |
| 4 | converted to seconds: 6.7 ~ 15.0 seconds per segment, total **20.1 ~ 45.0 seconds** | A-26: 402 ÷ 20 = 20.1; 900 ÷ 20 = 45.0 |
| 5 | **there is no remainder**: the three segments always sum to `3d`, and "round first, then absorb the remainder" is not needed | A-26 |

**Registration of the change of measure**: A-26 **supersedes D-110 item 3 and its supplementary item**, the three-segment split rule ("draw the total tick count `T` at random first, then take `d1 = d2 = floor(T/3)`, `d3 = T − 2·d1`"); D-110's other three items remain fully in force — item 1 (a total duration of 20 ~ 45 seconds), item 2 (abandoning vanilla random ticks) and item 4 (the brightness condition, bone meal behaviour and the four stages untouched). Document 22 registers this under A-26, and the whole of §6 of this document is written to the new measure.

**Why the lower bound is 402**: splitting into three equal parts requires a segment of at least `ceil(20 × 20 ÷ 3) = ceil(133.33) = 134` ticks, and `3 × 134 = 402` — 402 is the lower bound of 3 × 134, which keeps **20.1 seconds inside [20, 45] seconds**; the lower bound is decided by the minimum of `d` alone, not by splitting the 20 seconds into three and rounding each part down. The upper bound lands exactly on `3 × 300 = 900` ticks, that is 45.0 seconds, no more and no less.

Three worked examples (which pin the value rule down):

| `d` (ticks, per segment) | `T = 3d` | Total duration (seconds) | Duration per segment (seconds) | Note |
|---|---|---|---|---|
| **134** (lower bound) | **402** | **20.10** | 6.70 | `floorMod(...) = 0` |
| 213 (a value drawn) | 639 | 31.95 | 10.65 | a middle value, only to show that "the three segments are the same length" |
| **300** (upper bound) | **900** | **45.00** | 15.00 | `floorMod(...) = 166` |

**Why `d` is computed from the coordinate rather than "draw T first, then split into three"** (A-26's grounds, in one sentence): `d1 / d2 / d3` are all decided by `T`, and `T` is a random number drawn per plant while `age` knows only which segment has been reached and cannot derive `T`; executing the third segment therefore requires storing `T` (with that remainder) somewhere, and the carrier is fixed by D-111 to **no new block entity** ⇒ **the remainder has no carrier**. The equal-length scheme needs only one `d`, and `d` can be **recomputed on the spot** from `pos.hashCode()` (the same coordinate always yields the same `d`), so the chain needs no memory at all.

**The rejected scheme (registered here to stop later comers from re-proposing it)**: the old measure was "draw an integer uniformly from `T ∈ [400, 900]`, then take `d1 = d2 = floor(T / 3)`, `d3 = T − 2 × d1`", whose lower-bound worked example is 133 / 133 / 134 ticks (400 ticks in total = 20.0 seconds). The reason for rejection: the remainder has no carrier (expanded in the paragraph above), and moreover "the three segments are unequal" is known only when the third segment settles — the scheduled-tick chain carries exactly one piece of information, "when the next due time is", and cannot make up that difference. Once the three segments are equal, the second problem disappears by itself: the second and the third segment are both just "schedule `d` ticks once more".

### 6.4 The carrier: a scheduled-tick chain (D-111)

| Moment | What is scheduled | Note |
|---|---|---|
| On block placement (`onPlace`) | `level.scheduleTick(pos, this, d)`, with `d` computed on the spot from `pos` | a plant queues only on itself and occupies no block entity |
| Scheduled-tick callback | check `state.getBlock() == this` → check brightness → `age + 1` → if `age < 3` schedule **`d`** again (the same `d`, not the next array element) | **scheduling stops** after the third advance |
| Bone meal hit | reschedule **`d`** (overriding the old queue entry); `d` does not depend on `age`, so nothing else needs recomputing | §6.6 |
| Block broken / replaced | nothing to do: the callback's first statement `state.getBlock() == this` is false and it lapses by itself | a scheduled tick is **one per position**, so no cleanup is needed |

**Why no block entity** (D-111's grounds, the trade-off among three carriers): giving each of the 15 crops its own block-entity type = registry surface +15, which immediately breaks this version's "registry surface +3" boundary; sharing one type would mean changing 15 block definitions and the crop would then cost memory and synchronisation bandwidth; a global tick poll would have to walk the whole server's blocks, with a cost that grows with world size. A scheduled tick is **a capability of the block itself**, `+0` registry objects, and is saved and loaded with the chunk data — which is exactly the semantics needed so that "shut the server down for two days, bring it back, and the crops do not mature instantly".

### 6.5 Timeline table

The absolute tick axis from placement to maturity for two plants (`d = 134` (the lower bound, `T = 402`) and `d = 300` (the upper bound, `T = 900`)):

| Tick | Plant with d = 134 (T = 402) | Plant with d = 300 (T = 900) |
|---|---|---|
| 0 | placed, age 0, schedules `d` = 134 | placed, age 0, schedules `d` = 300 |
| 134 | callback: age 0 → 1, schedules `d` = 134 again | — |
| 268 | callback: age 1 → 2, schedules `d` = 134 again | — |
| 300 | — | callback: age 0 → 1, schedules `d` = 300 again |
| 402 | callback: age 2 → **3 (mature)**, schedules nothing more (20.1 seconds) | — |
| 600 | — | callback: age 1 → 2, schedules `d` = 300 again |
| 900 | — | callback: age 2 → **3 (mature)**, schedules nothing more (45.0 seconds) |

Two points to take from the table: ① the moment of maturity is decided solely by "placement tick + T" and is **independent** of whether the player is nearby and whether the chunk is loaded (a scheduled tick travels with the chunk data); ② `MAX_CUSTOM_AGE = 3` is a hard cap, and the callback checks `age < 3` before `age + 1`, otherwise it would write the illegal `age=4` (`block/FarmstormCropBlock.java:31`, `:63-70`).

### 6.6 Bone meal

**Still works**, and **the advance amount does not change**: bone meal goes through the vanilla `CropBlock`'s `BonemealableBlock` implementation, and this version rewrites neither `isValidBonemealTarget` nor `performBonemeal` nor `getBonemealAgeIncrease` (D-110 item 4, "bone meal behaviour untouched"). This item adds only one **rescheduling rule** (D-111 item 4):

1. After bone meal pushes the age to `a′`, **reschedule `d` ticks** (not "the sum of the remaining segments"): under the equal-length scheme `d` does not depend on `age`, so there is no need to know how many segments were crossed; segments already crossed are void and are neither re-issued nor deferred.
2. The semantics of rescheduling is **overwrite**, not **append**: `Level.scheduleTick` deduplicates and overwrites for "the same block at the same position" (vanilla `LevelChunkTicks` uses (block, pos) as the key for deduplication). If measurement shows it appends, two scheduled ticks would exist at one position ⇒ double advance, and that must be asserted separately in `24-TECH-0.1.0.md`'s acceptance (registered in §11.8).
3. If `age = 3` has been reached when the bone meal runs out, neither the callback nor a reschedule happens again.

### 6.7 `.randomTicks()` must be deleted

`.randomTicks()` at `registry/FarmstormBlocks.java:88` is deleted, and `FarmstormCropBlock` does not override `randomTick` (D-111 item 3). Three reasons:

1. **Two sources advancing**: `FarmstormCropBlock` today follows the vanilla `CropBlock` entirely, and the vanilla `randomTick` would still advance it (【BRIEF §1.5】). A scheduled tick plus a random tick at the same time ⇒ the growth time is uncontrollable, and it is the hardest kind of bug to find (the same plant is sometimes fast, sometimes slow).
2. `.randomTicks()`'s only purpose is to put the block into the chunk's random-tick sampling set. Delete it and these 15 blocks are no longer sampled, so the random-tick cost actually goes down.
3. Keep it and "20 ~ 45 seconds" will always be exceeded now and then during acceptance, and the overshoot is a probabilistic event — impossible to reproduce reliably.

### 6.8 Light remains a precondition

| Item | Conclusion | Grounds |
|---|---|---|
| Brightness threshold | still required: `getRawBrightness(pos, 0) >= 9` | 【BRIEF §1.5】's vanilla criterion; D-110 item 4, "the brightness condition is untouched" |
| Where it is checked | **moved from inside** the vanilla `CropBlock.randomTick` **into our own tick callback** (the callback's first statement checks the block, the second checks brightness) | this document's supplement: the carrier changed, but the criterion must not be dropped along with it |
| Behaviour in darkness | do not advance, and reschedule the next check after **`d`** (a scheduled tick has no "pause" semantics, only rescheduling) | this document's supplement |
| After light returns | continue advancing on the entry that was scheduled; the skipped ones are not made up | this document's supplement: vanilla in darkness also "does not attempt" rather than "owes a few tries" |

### 6.9 Do-not-touch list

| Item | Grounds |
|---|---|
| The four stages `AGE_3`, `MAX_CUSTOM_AGE = 3`, `SHAPE_BY_AGE`'s 4 / 8 / 12 / 16 | D-110 item 4 |
| The existing rules for the seed item, the harvest item and the mature drop | existing design, untouched by this version |
| The 22 crop profiles, the 15 crop blocks | §1.3; D-110 supplementary item |
| Vanilla wheat / carrot / potato / beetroot / melon and pumpkin stems, the global random-tick speed | `22-DECISIONS-0.1.0.md` §6 item 2 |

### 6.10 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| The chunk unloads halfway through an advance | the scheduled tick is saved with the chunk data and continues on time after reload (D-111's grounds); **must be confirmed by measurement** (save → restart → keep advancing); if measurement does not bear it out, fall back to the "reschedule from the placement information at load time" patch, without changing D-111's measure |
| Unloaded immediately after placement (same tick) | as above: at placement it has already been put into the chunk's pending queue, and unloading saves it along with the chunk |
| The plant is dug out before its scheduled tick is due | the callback's first statement `state.getBlock() == this` is false and it does nothing |
| Dig, then plant again at the same position | the new plant's `onPlace` schedules a new entry; even if the old one is still in the queue, at callback time the block id is the same but the age differs ⇒ only `this` matches, and the old entry is treated as "due early" (one advance) — this is the existing semantics of a scheduled tick, registered in §11.7 |
| Bone meal and a scheduled tick come due in the same tick | whichever executes first advances first; the extreme case where both advance is covered by the rule "segments already crossed are void" (§6.6 item 1), registered in §11.8 |
| Brightness exactly at the boundary of 9 | written per vanilla's ">= 9" (9 included) |

---

## 7. The trap system (immunity / particles / mob triggering / cap)

### 7.1 One-line positioning

Four mutually independent changes, all landing on the existing "deployed object + `TrapBehavior`" chain: **①** after a player triggers any trap, for 5 seconds **all** traps fail to trigger on him (immunity judged by broad category, recorded on the player, timed with `gameTime`); **②** every deployed trap emits 2 dust particles at its own position every 5 seconds; **③** the trigger set widens from "non-ally players" to "non-ally players + hostile mobs", with the generic effects applying and the player-only ones skipped; **④** at most 16 traps per player, with the 17th squeezing out the **earliest deployed** one (FIFO).

### 7.2 Overview of the four changes

| # | Change | New config key | Landing point | Decision |
|---|---|---|---|---|
| 1 | 5-second broad-category immunity | `trap.immunityTicks` = **100** | the immunity gate at the scan entry | D-112 |
| 2 | dust particles every 5 seconds | `trap.dustIntervalTicks` = **100** | `TrapBehavior.tick`'s default implementation | D-113 |
| 3 | hostile mobs can trigger | none (existing damage and radius keys are reused) | `TrapBehavior`'s signature and the four impls' scans | D-114 |
| 4 | 16 in FIFO order | `trap.maxTrapsPerPlayer` = **16** | the deployment registry + a server-side index on disk | D-115 |

### 7.3 5-second same-category (broad-category) immunity after a trigger (D-112)

| Item | Conclusion | Grounds |
|---|---|---|
| Immunity granularity | the **trap broad category** (the TRAP category as a whole), not "the same kind of trap" | D-112 item 1; BRIEF §2 item 2 |
| Duration | **100 ticks = 5 seconds** | D-112; `trap.immunityTicks` |
| Recorded on whom | **players only** (`Map<UUID, Long>`, the value being the `gameTime` at which immunity expires); **hostile mobs are not recorded** | D-112 item 3 |
| Timing | `gameTime`, **not written to the save** | D-112 item 4 |
| Check point | the scan entry: one more gate after `findTrigger` returns a candidate and before `trigger`; when immune, this scan is skipped outright (**no consume, no effect**) | D-112 item 2; `blockentity/DeployedCropBlockEntity.java:136-161` |
| Endpoints | the half-open interval `[t0, t0 + 100)`: from tick 100 it can trigger again | this document's supplement (consistent with §0.3 item 5's interval measure) |
| Where it is recorded | a server-side static container at the same level as the existing `DEPLOY_LEDGER` (the level of `DeployedCropBlockEntity.java:71-72`) | D-112's landing position |

**Why put it in the scheduling layer rather than in the four `findTrigger`s**: the four traps each traverse players independently (BlastCarrot `:44`, FangTrap `:39`, Zangief `:113`, VibrationBean `:49`, 【BRIEF §1.6】). Written into the four implementations it would be the same criterion repeated four times, and it would inevitably drift so that "the four traps' immunity windows start at different points"; put at the deployed object's scan entry there is one place only, and the four categories unify naturally.

**A timing example** (player P steps on a Blast Carrot A, then walks into Fang Trap Cactus B's range):

| Tick | Event | Result |
|---|---|---|
| 100 | P triggers Blast Carrot A | A settles and `consume`s; writes `immune[P] = 100 + 100 = 200` |
| 105 | P walks within 2.5 blocks of Fang Trap Cactus B | `findTrigger` hits B, the immunity gate blocks it ⇒ **no consume, no fangs** |
| 150 | P steps on a Vibration Bean | as above, blocked |
| 199 | still inside the immunity window | blocked |
| 200 | the window expires (the right endpoint of the half-open interval) | it can trigger again: if B is still there and P is still in range, this round's scan hits |

### 7.4 dust particles every 5 seconds after deployment (D-113)

| Item | Value | Grounds |
|---|---|---|
| Interval | **100 ticks = 5 seconds** | D-113; `trap.dustIntervalTicks` |
| Count | **2 per emission** | D-113 supplementary item (the smallest observable value of "a small amount") |
| Particle | `minecraft:dust` (`new DustParticleOptions(colour, scale)`) | D-113 items 1 and 2 |
| Colour | **(0.62, 0.56, 0.44)** — a neutral earth tone | this document's supplement (see below) |
| scale | **1.0** | this document's supplement (the same value as the existing call at `combat/WildHunterAura.java:170`) |
| Position | the block centre `(x + 0.5, y + 0.15, z + 0.5)` | this document's supplement: on the ground but not sunk into the block |
| Spread offset | `xDist` 0.25 / `yDist` 0.10 / `zDist` 0.25 | this document's supplement: so that the 2 particles do not exactly coincide |
| Velocity | **0** (spreads at rest) | this document's supplement |
| Broadcast | the vanilla particle packet, 32 blocks | this document's supplement: the same order as the visible distance of the existing `sendParticles` calls |
| First moment | tick **100** after deployment (not tick 0) | this document's supplement: the disturbance at the instant of deployment would expose it first |
| Mount point | `TrapBehavior.tick(be)`'s default implementation (shared by the four impls, `behavior/TrapBehavior.java:22-25`) | D-113's landing position |
| Staggering | reuse `stagger = floorMod(pos.hashCode(), interval)` (`DeployedCropBlockEntity.java:155-161`) | D-113 item 3 |
| Objects | only deployed **traps** (TRAP) emit; turrets and Construction do not | D-113 item 4 |

**Why the colour is a neutral earth tone**: the only existing dust constant is Wild Hunter's red smoke `(1, 0.15, 0.15)` (`combat/WildHunterAura.java:170`'s `SMOKE_RED`). A trap's dust is the hint "there is something here", not a special effect: red would be confused with Wild Hunter's red smoke, pure white with snow particles, while a neutral earth tone is neither eye-catching nor in conflict with existing marker colours (this document's supplement; D-113's supplementary item states explicitly that "the colour, scale and spread radius are aligned by `23-DESIGN-0.1.0.md`", and this section is that place).

**Why the count is 2**: D-113's supplementary item has already fixed 2 per emission. More would mark our own traps' positions too precisely and would also multiply the packet volume (1 `ClientboundLevelParticlesPacket` each time; 16 traps × every 5 seconds = at most 16 packets every 5 seconds, which is already plenty).

### 7.5 Hostile mobs can trigger (D-114)

| Item | Conclusion | Grounds |
|---|---|---|
| Trigger set | non-ally players + **hostile mobs** (vanilla `Monster`: zombie / skeleton / creeper and the like; **animals and villagers do not count**), the same measure as D-43's rule for the Pumpkin Turret's target set | D-114 item 1 |
| Interface | `Optional<Player> findTrigger(...)` / `trigger(be, Player)` widened to `LivingEntity` (`behavior/TrapBehavior.java:18-20`) | D-114's landing position; 【BRIEF §3.2】 row 6 |
| Generic effects (for every triggerer) | damage, slowness, knockback | D-114 item 2 |
| Player-only (skipped for mobs) | Q-Bounce, Confusion | as above; `16-DECISIONS-0.0.3.md` D-83 |
| Creative / spectator players | still do not trigger | D-114 item 4 |
| Ally judgment for mobs | **always treated as non-ally** (a mob is not in the team member table) | D-114 item 4 |
| Turrets | the target set is unchanged: the Pumpkin Turret still locks only non-ally players + hostile mobs, the other turrets still lock only players | D-114 item 5 |

**The landing point of each of the four traps**:

| Trap | Widening of the trigger side | What settles when a mob triggers | What is skipped for mobs |
|---|---|---|---|
| Blast Carrot `blast_carrot` | `Monster` added to the scan traversal, Chebyshev ≤ 1 | explosion damage (which already traverses `LivingEntity` to settle) | — |
| Fang Trap Cactus `fang_trap_cactus` | Euclidean ≤ 2.5 | fang damage | Slowness V (`behavior/impl/FangTrapFangs.java:59-61` already filters `ServerPlayer`, unchanged) |
| Vibration Bean `vibration_bean` | strictly less than 4.0 | still plays the explosion sound once and `consume`s | Confusion (the effect side already traverses players only, unchanged) |
| Zangief Cane `zangief_cane` | spherical ≤ 1.5 | **a fixed 7.5 points of damage + knockback**, settled at the trap's position | the phase chain (see below) |

**Zangief Cane's mob branch** (D-114 supplementary item): for a player it runs the existing phase chain — the phase number is sent from the server to the client by `effect/ZangiefPiledriverEffect.java:6-16` (that effect **carries only the phase number**; it adds no attribute, deals no damage, produces no particles and reads no config); phase 1 is "gravity off + servo speed computed from the remaining height and the remaining tick count, lifting to a fixed height" (`behavior/ZangiefPiledriverController.java:210-242`, lift height defaulting to 16 blocks with the config comment recording a measured ≈ 15 blocks, covered in 15 ticks, the per-tick cap `MAX_RISE_SPEED = 4.0` at `:56`); phase 2 is a servo descent + yaw **+72°** per tick; landing settles a fixed **7.5** points. Of the three phase constants, `PHASE_LEVITATING` is merely a **historical misnomer** (`effect/ZangiefPiledriverEffect.java:18-25`); it is not the vanilla levitation effect — `MobEffects.LEVITATION` is used at the controller's `:390` and `:421` only as a fallback removal for old saves. For a mob **only the generic part settles**: a fixed 7.5 points (reusing the existing landing-damage key) + knockback, settled at the trap's position, with no phase animation. The reason is that the phase chain serves the player's camera and input (`ZangiefPiledriverController.begin(player, deployer)`, `behavior/impl/ZangiefCaneTrap.java:71`), and a mob has no receiving end.

### 7.6 The 16-traps-per-player FIFO (D-115)

| Item | Conclusion | Grounds |
|---|---|---|
| Allowance | **16** | D-115 item 1; `trap.maxTrapsPerPlayer` |
| What counts | only deployed objects of the **TRAP category**; turrets and Construction do not take up an allowance | D-115 item 2; BRIEF §2 item 3 |
| What happens beyond the cap | on deploying the 17th, the **earliest deployed** one is **silently removed** (FIFO) | D-115 item 1 |
| Removal path | through the existing `consume()`: `removeBlock(pos, false)` + break particles, **no drops**, the item is not refunded | D-115 item 3; `block/DeployedCropBlock.java:322-330` |
| Index | add an **ordered index, persisted to disk, of "player → that player's trap positions"** (the existing ledger has counts only and cannot tell "which one is earliest") | D-115 item 4; 【BRIEF §1.6】's ⚠️ |
| On-disk format | `UUID → a list of positions ascending by placedGameTime`, with **lazy validation** at load time (the position must still hold a TRAP deployed object under that player, otherwise the entry is discarded) | D-115 supplementary item |
| Timing of the squeeze-out | must be completed **before** the new block is written | D-115 supplementary item |
| The three safety valves | `DEPLOY_COOLDOWN_TICKS` **5** / `MAX_DEPLOYED_PER_CHUNK` **8** / `MAX_DEPLOYED_PER_PLAYER` **32**, not one of them changes | `DeployedCropBlockEntity.java:48-50`; 22 §6 item 7 |

**A FIFO timing example** (player P places one every 2 ticks, all of the TRAP category):

| Tick | Action | Queue (#1 = earliest) | Result |
|---|---|---|---|
| 0 | place #1 | 1 | 1 / 16 |
| 2 ~ 30 | place #2 ~ #16 | 1 … 16 | the 16th exactly fills the allowance |
| 32 | place #17 | 2 … 17 | **#1 is squeezed out** (the smallest `placedGameTime`), through `consume()`, no drops |
| 40 | P digs out #5 himself | 2, 3, 4, 6 … 17 (15) | digging out is the normal path, dropping 1 of itself |
| 50 | place #18 | 2, 3, 4, 6 … 18 (16) | not beyond 16 ⇒ **no squeeze** |
| 60 | place #19 | 3, 4, 6 … 19 (16) | **#2 is squeezed out** |
| 80 | a trap is destroyed by a mob triggering it | … one fewer | the allowance is released **immediately**, with no delay |

Two points to take from the table: ① the queue length **counts only the TRAP deployed objects currently under that player**; the player digging one out, a trap being destroyed by a trigger, or one being broken by an explosion all reduce it at once; ② "earliest" is decided by `placedGameTime`, and for several in the same tick (two placed in one tick) **the order of the written index** is the secondary key — otherwise two from the same tick have no definite order and the FIFO degenerates into "squeeze out one at random" (this document's supplement).

### 7.7 How the four interact

| Combination | Result | Grounds |
|---|---|---|
| immunity × dust particles | immunity **does not stop** particles: particles are decoration, not a trigger | D-113 concerns the deployed state only |
| immunity × mob triggering | mobs are not recorded for immunity ⇒ a pack can trigger the same trap in succession (but a trap is one-shot and is destroyed on triggering) | D-112 item 3 |
| mob triggering × FIFO | a mob trigger destroys the trap ⇒ the queue shrinks and the allowance is released at once | §7.6, point ① of the table's notes |
| FIFO × turrets | turrets take up no allowance and do not enter the index | D-115 item 2 |
| immunity × allies | ally players never trigger in the first place (D-07's ally/enemy measure); immunity only means anything for "those who would trigger" | D-114 item 4 |

### 7.8 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| The player logs out | the immunity table uses `gameTime` and is not written to the save ⇒ it resets on reconnecting, and the worst consequence is that the same person can step on one again within 5 seconds (D-112's grounds already accept this cost) |
| The server restarts | as above; the FIFO index **must be persisted** (D-115); the two measures differ, do not mix them up |
| Two traps could both trigger on the same player in the same tick | only the one that settles first takes effect and writes immunity, the rest are gated — the same outcome as "triggers once in a tick", with no extra deduplication key needed |
| The chunk the trap is in unloads | the block entity does not tick ⇒ no check, no particles; as before after reload (registered in §11.5) |
| 1 of a player's 16 sits in an unloaded chunk | it still takes up an allowance (it is a fact that it is "placed"); index entries are only lazily validated at load time and the world is not actively scanned |
| The trap is overwritten by /setblock or falls into the void | the index entry is discarded at the next validation (§7.6's lazy validation) |

---

## 8. Farmland quota and the Outpost Core

This version's largest section. The "Outpost Core" is the **single source** of quota and range: without a core you cannot till land (default quota 0), with a core you have quota and range, and if the core is demolished the ally's farmland in range reverts to dirt. So the six things — quota (§8.1, §8.4), range (§8.2, §8.8), the core itself (§8.3), protection and settlement (§8.5, §8.6) and visualisation (§8.7) — must be read together.

### 8.1 The quota model

**Two layers of quota, added in one place**:

| Layer | Carrier | Default | How it changes | Grounds |
|---|---|---|---|---|
| Personal base quota | config `farmland.limitPerPlayer` | **0** | edit the config file | D-116 items 1 and 2; BRIEF §2 item 4 |
| Runtime override | the sub-command `/farmstorm farmlandlimit <n>` | none (the config value is used when unset) | the server owner issues one command at runtime | D-116 item 1 |
| Core bonus | config `outpost_core.farmlandBonus` × the number of cores placed | **16 per core** | place / demolish a core | D-120 item 1 |

**The summation rule (D-117)**:

```
个人额度  = 指令值（若设置过）否则 config 默认值
队伍上限  = Σ(队伍内每个成员的个人额度) + 16 × 队伍内已放置的核心数
无队伍时  = 个人额度 + 16 × 自己已放置的核心数
已开垦数  = 队伍账本（坐标集合）的基数
占用判定  = 已开垦数 < 队伍上限
```

(In English: personal quota = the command value (if it has been set), otherwise the config default; team cap = Σ(each member's personal quota) + 16 × the number of cores placed within the team; with no team = personal quota + 16 × the number of cores you have placed yourself; tilled count = the cardinality of the team ledger (a set of coordinates); occupancy test = tilled count < team cap.)

Four landing points:

1. **It is not computed per head**: with a team the cap is **the sum of every member's personal quota** (plan `:38`'s own wording), so a 3-person team, each with a command value of 5, that has placed 2 cores ⇒ 30 + 32 = 62 blocks; not 62 blocks per head (D-117 items 1 and 2).
2. **The core count has a cap, so the bonus has a cap too**: at most 2 cores per person (§8.3) ⇒ a team's core count ≤ 2 × the number of members ⇒ a two-person team's bonus cap is 4 × 16 = **64** (this document's supplement: D-122's 2 per person multiplied by D-120's 16 per core). (Note: the 3-person example above with 2 cores is 32 points of bonus, which together with the 30 points of base quota makes 62.)
3. **Accounting uses a set of coordinates, not a bare count**: when a player digs out a piece of farmland himself, a bare count has nowhere to subtract one; the coordinate set is likewise the data source for §8.6's "shovel up only your own few" (D-117 item 4).
4. **The command value is not persisted**: it applies only to the current server process and returns to the config default on restart; no second copy of persistent data is written (D-116 item 5) — allowing the quota to be defined in two places would immediately create a two-source "who overrides whom".

**Five worked examples** (which pin the summation rule down):

| Scenario | Personal quota | Cores placed | Team cap | Arithmetic |
|---|---|---|---|---|
| Single player, default config | 0 | 0 | **0** | at the start nobody can till |
| Single player, default config + 2 cores | 0 | 2 | **32** | 0 + 16 × 2 |
| Single player, command 10 + 1 core | 10 | 1 | **26** | 10 + 16 × 1 |
| 3-person team, default config + 3 cores | 0 each | 3 | **48** | 0 + 16 × 3 |
| 3-person team, command 5 + 2 cores | 5 each | 2 | **62** | 3 × 5 + 16 × 2 |

### 8.2 The tillable range

| Item | Conclusion | Grounds |
|---|---|---|
| Range | the chunk **the ally core is in + the 8 surrounding chunks** (3 × 3 = 9 chunks, **48 × 48 blocks**, an area of 2,304 blocks) | D-118 item 1; plan `:38` |
| Criterion | the target block's chunk `(cx, cz)` satisfies `abs(cx − coreX) ≤ 1` and `abs(cz − coreZ) ≤ 1` | this document's supplement (D-118's "chunk-aligned rectangle" wording) |
| Ally | the core's placer himself and his teammates (resolved live by `TeamResolver`) | D-118 item 2; D-06; C-9 |
| Tilling outside the range | **produces no farmland, consumes no durability**, and has no effect beyond the message text | D-118 item 3 |
| Existing farmland | **not retroactive**: existing farmland outside the range is still farmland and neither disappears nor degrades | D-116 item 3; BRIEF §2 item 5 |
| Multiple cores | the ranges **unite** | D-118 item 4; §8.8 |
| config key | **none** (the 3 × 3 is not made adjustable) | D-118 supplementary item; 22 §6 item 9 |

**A chunk-boundary worked example** (the core placed in chunk (0, 0)):

| Target block coordinate (x, z) | The block's chunk | Chunk difference from the core | Tillable? |
|---|---|---|---|
| (7, 7) | (0, 0) | (0, 0) | yes |
| (15, 15) | (0, 0) | (0, 0) | yes |
| (16, 16) | (1, 1) | (1, 1) | yes |
| (31, 31) | (1, 1) | (1, 1) | yes (the range's farthest corner) |
| (32, 32) | (2, 2) | (2, 2) | **no** |
| (−16, 0) | (−1, 0) | (−1, 0) | yes |
| (−17, 0) | (−2, 0) | (−2, 0) | **no** |

Two points to take from the table: ① negative coordinates must be converted to a chunk by **rounding down** (`floor(−17 / 16) = −2`, not −1), otherwise the range goes one cell askew near the world origin (this document's supplement); ② the range is a **rectangle**, not a circle — 3 × 3 chunks is a square 48 blocks on a side, not a circle 48 blocks in radius: with the core placed on block coordinate (7, 7) of chunk (0, 0), due east reaches at most x = 31 (+24 blocks) and due north at most z = −16 (−23 blocks), while the opposite corner's (31, 31) is equally reachable — the boundary is decided by chunks, not by distance (this document's supplement: all three offsets are obtained by subtracting the core's coordinates from 31 / −16).

### 8.3 The Outpost Core (D-119, D-122)

| Property | Value | Source |
|---|---|---|
| Hardness | **30.0F** | D-119 item 1; BRIEF §2 item 8 |
| Blast resistance | **1200.0F** | as above |
| Tool | needs a pickaxe and **drops at any tier**: `requiresCorrectToolForDrops()` + membership of `minecraft:mineable/pickaxe`, and **not** of any tier among `needs_stone_tool` / `needs_iron_tool` / `needs_diamond_tool` | D-119 item 2 |
| Base mining time | **45 seconds** (30 × 1.5), from a wooden pickaxe down | D-119 supplementary item |
| Piston | **not pushable**: `pushReaction(PushReaction.BLOCK)` | D-119 item 3; precedent `registry/FarmstormBlocks.java:101-102` |
| Placement position | placeable anywhere, with no attachment or support required | D-119 item 4; BRIEF §2 item 16 |
| Ownership | `setPlacedBy` writes `ownerUuid`; the effect is resolved live by team and shared with teammates | D-119 item 4; C-9 |
| Drops | the block's own loot table, producing the block itself | D-119 item 5 |
| Per-person cap | **2** (`outpost_core.maxPerPlayer`), counted **per player**, not per team | D-122 items 1 and 4 |
| Beyond the cap | **placement refused**: the placement action does not take place, no item is dropped and no item is consumed (no FIFO squeeze-out) | D-122 item 2 |

**The crafting recipe (D-122 item 3)**: 3 × 3 crafting table, stone in the four corners, a diamond in the centre and redstone in the remaining four cells, producing **1** core.

```
[ 石头 ] [ 红石 ] [ 石头 ]
[ 红石 ] [ 钻石 ] [ 红石 ]
[ 石头 ] [ 红石 ] [ 石头 ]
```

The nine cells: `minecraft:stone` × **4** (the four corners), `minecraft:diamond` × **1** (the centre), `minecraft:redstone` × **4** (the remaining four cells), outputting `farmstorm:outpost_core` × **1** — matching D-122 item 3's "stone in the four corners / diamond in the centre / redstone in the remaining four cells" cell for cell. The recipe goes through the existing shaped form in `datagen/ModRecipeProvider.java` (+1 entry).

**Why refuse beyond the cap rather than use FIFO**: plan writes FIFO only for traps (`:34`), and for the core it writes "each player may by default own at most 2 placed Outpost Cores" (`:45`), which authorises no squeeze-out; mixing the two semantics would mean a player who places a 3rd **randomly** loses an already-effective outpost (D-122's grounds).

### 8.4 +16 quota per core

| Item | Conclusion | Grounds |
|---|---|---|
| Bonus | **+16** farmland quota per core placed | D-120 item 1; config `outpost_core.farmlandBonus` |
| On whom it accrues | the core's **placer and his team** (taking effect together with §8.1's summation rule) | D-120 item 1; D-117 |
| When it takes effect | **the moment** placement succeeds (16 more blocks can be tilled within the same tick) | this document's supplement: the quota is summed at read time, not granted as an event |
| When it is reclaimed | dug out by a player, broken by an explosion, overwritten by `/setblock` ⇒ reclaimed **immediately** (the very next block of farmland may be refused) | D-122 supplementary item; §8.6 |
| When already over the limit | only **new tilling** is restricted; not one piece of existing farmland is withdrawn (not retroactive) | D-116 item 3 |

One inference that must be spelled out: the quota is a "cap", not a "ration". The core is placed, the player opens 10 plots, the core is demolished ⇒ the cap falls back in the direction of 10 − 16 < 0; at that point the player **still owns those 10 plots** (not retroactive) but **cannot open a single new one** until the cap becomes positive again. This is the combined result of the two measures "not retroactive" and "reclaimed immediately" (this document's supplement).

### 8.5 Farmland in range is not trampled

| Item | Conclusion | Grounds |
|---|---|---|
| What is intercepted | the path by which players and mobs **trample** farmland into dirt (`FarmlandBlock`'s `turnToDirt`) | D-120 item 3 |
| What is not intercepted | **dehydration degradation** (farmland turning to dirt by itself when there is no water nearby) — that is the farmland's own state change, and intercepting it too would turn farmland in range into permanent fields that never degrade | D-120 supplementary item |
| Criterion | both conditions hold at once: the coordinate falls inside an **ally** core's 3 × 3 chunk range, and that farmland is on the **ally ledger** | this document's supplement ("ally farmland" = tilled by an ally, not "any farmland inside an ally's range") |
| Where the criterion comes from | the same entry point as §8.2's tilling interception (range first, then ledger) | D-120's landing position |
| Range measure | with multiple cores, the union (§8.8) | D-118 item 4 |

**Why range and ledger must be judged together**: judging the range alone would protect the fields an enemy tills inside your core's range (he can still come in and till, because the range only constrains whether he himself is an ally) — that is handing protection to someone else; judging the ledger alone would amount to "an ally's field cannot be trampled anywhere in the world", which conflicts with "the affected range is the chunk it is in and the eight surrounding chunks" (plan `:45`).

### 8.6 Ally farmland turns to dirt after destruction

| Item | Conclusion | Grounds |
|---|---|---|
| Trigger | the core block is removed (dug out by a player / broken by an explosion / overwritten by `/setblock`) | D-120 item 4 |
| What turns | every piece of farmland **within that core's 3 × 3 range** and **on the ally ledger** becomes `minecraft:dirt` | D-120 item 4; plan `:45` |
| Data source | D-117's coordinate set, with **no whole-range scan** | D-120 supplementary item |
| Ledger sync | each converted piece of farmland is deleted from the set on the spot (§8.1 point 3), so the books can be seen immediately at the next tilling | this document's supplement |
| Drops | no drop from the farmland itself is produced and no loot table is triggered | this document's supplement: the same technique as `consume()`'s "silent removal" (D-115 item 3) |
| Time window | **settled within a single tick** (no batching, no deferred queue) | this document's supplement: the ledger's size is capped by the quota, so one traversal is on the same order as the "team cap" |

**Why not scan the whole range**: a scan would also shovel up the farmland "inside the range but tilled by someone else and not on the ledger", which directly conflicts with the original wording's restriction, "all **ally** farmland within the affected range becomes dirt" (plan `:45`; D-120 supplementary item).

### 8.7 Boundary particles (D-121)

| Item | Value | Grounds |
|---|---|---|
| Shape | the rectangular outline (its four edges) of a 48 × 48 block ground area | D-121 item 1 |
| Sampling | one point every **4 blocks** | D-121 item 5 |
| Points per loop | **48** points | 48 × 4 ÷ 4 = 48 (perimeter 192 blocks ÷ 4) |
| Refresh interval | **40 ticks = 2 seconds** (`outpost_core.boundaryParticleIntervalTicks`) | D-121 item 3 |
| Implementation | purely server-side: `ServerLevel.sendParticles` ⇒ vanilla `ClientboundLevelParticlesPacket`, zero client code | D-121 item 2; C-6; D-50 |
| Send condition | sent only when a player **enters the range** (including the 16-block margin) | D-121 item 4; RT-30 |
| Distance falloff (two tiers) | inside the range (48 × 48) ⇒ all 48 points every round; in the margin band (16 blocks outward) ⇒ 1 emission every **4** rounds | this document's supplement (D-121 item 4 says only "falls off with distance"; the two tiers are the concretisation) |
| Particle | `minecraft:dust`, colour **(0.30, 0.85, 0.90)**, scale 1.0 | this document's supplement (see below) |
| Height | core block y + 0.1 (on the ground) | this document's supplement: the outline is drawn on the ground, not at the core's height |
| Visibility | allies (placer + teammates) and inside the range / margin band; creative and survival are not distinguished | D-121 supplementary item |

**The packet account (this document's supplement; this is exactly the cell RT-30 cares about)**: 1 core, 1 visible player ⇒ **1 packet / 48 particles** per round, one round every 2 seconds ⇒ **0.5 packets/second**. Sending cell by cell would make one loop 192 points ⇒ 192 particles every 2 seconds, quadrupling the packet volume with no additional information — that is the entire reason for "one point every 4 blocks".

**Why the colour is cyan-blue**: it cannot be confused with the traps' earth-tone dust `(0.62, 0.56, 0.44)` (§7.4) or Wild Hunter's red smoke `(1, 0.15, 0.15)`; the boundary line carries the semantics of "territory" and must be distinguishable at a glance from "there is a trap here" (this document's supplement).

### 8.8 Multiple cores' ranges unite

| Item | Conclusion | Grounds |
|---|---|---|
| Range | if any **ally** core's 3 × 3 chunks covers the coordinate, it is tillable / protected | D-118 item 4 |
| One piece of farmland covered by two ranges | counted as **one** (the ledger is a set of coordinates, deduplicated by nature) | D-118 item 4; D-117 item 4 |
| Why not take the intersection or the nearest core | with two adjacent cores the intersection would open a hole where "you stand between two cores yet cannot till" (D-118 supplementary item) | as above |
| When a core is demolished | only the ally farmland inside **the demolished core's own** 3 × 3 is shovelled up; fields in the overlap that belong to another core **are shovelled up all the same** | this document's supplement: the union serves only "can you till", not "protection when a core is demolished" (registered in §11.10) |

**Is the union continuous** (one core in chunk coordinate (0, 0), another at (d, 0)):

| Chunk distance d between the two cores | the first covers x ∈ | the second covers x ∈ | union | any hole |
|---|---|---|---|---|
| 1 | [−1, 1] | [0, 2] | [−1, 2] | none (2 chunks of overlap) |
| 2 | [−1, 1] | [1, 3] | [−1, 3] | none (1 chunk of overlap) |
| 3 | [−1, 1] | [2, 4] | [−1, 4] | none (edge to edge) |
| 4 | [−1, 1] | [3, 5] | [−1, 1] ∪ [3, 5] | **yes** (chunk x = 2 is missing, 16 blocks wide) |

`d − 1 ≤ 1 + 1 ⇒ d ≤ 3` is the necessary and sufficient condition for "the union is continuous", that is, when the two cores' chunk distance is ≤ 3 (≤ 48 blocks) there is no untillable seam between them (this document's supplement). This affects appearance and siting strategy only; it constitutes no new mechanism.

---

## 9. Wild Apple material

### 9.1 One-line positioning

`textures/item/wild_apple.png` is replaced by a **16 × 16** finished image downscaled from the **1294 × 1216** RGBA PNG on the author's desktop; the model, the item id, the language keys and the registry surface are all untouched.

### 9.2 Current state and source material

| Item | Value | Source |
|---|---|---|
| Current texture size | 16 × 16 | 【BRIEF §1.11】 |
| Current texture's colour count (measured) | **11 colours** + transparent | this document's measurement (Pillow `Counter`); 【BRIEF §1.11】 records "12 colours", and the one that differs is the **transparent entry** (12 = 11 + transparent) |
| Current texture's opaque pixels | **130 / 256** | this document's measurement; 【BRIEF §1.11】's "126 / 256 transparent" = 256 − 126 = 130, the two agree |
| Current model | `item/generated`, `layer0 = farmstorm:item/wild_apple` | 【BRIEF §1.11】 |
| New source material | desktop `2797B56FEF56AF13125F333A6989632A.png`, **1294 × 1216** RGBA, **1,113,442 B** | 【BRIEF §1.11】; D-105 |
| Downscale ratio | horizontally 1294 ÷ 16 ≈ **81**, vertically 1216 ÷ 16 = **76** ⇒ each target pixel corresponds to about 81 × 76 source pixels | this document's supplement (the two sizes divided) |

### 9.3 Specification of the finished texture

| Item | Specification | Reason |
|---|---|---|
| Size | **16 × 16** | the only sensible specification for `item/generated`; a 28-fold downscale cannot preserve detail on a 16 grid (D-105) |
| Colour count | **≤ 11** (reusing the current image's palette, see §9.4) | the same order as the current image; introducing a new hue would make the icon a different art style from the other items in the field (this document's supplement) |
| Opaque pixels | **120 ~ 140 / 256** (the current image has 130) | a pixel density of the same order, guaranteeing "neither empty nor smudged" (this document's supplement) |
| Outline | a 1 px dark outline around the edge, with alpha taking only the two values **0 / 255** | a semi-transparent edge produces a black or grey fringe when the inventory scales it (this document's supplement) |
| Background | fully transparent (alpha 0) | `item/generated`'s default rendering cannot have a background patch |
| Scaling algorithm | NEAREST or LANCZOS downsampling followed by **manual correction quantised to 11 colours** | plain average downsampling produces hundreds of intermediate colours (this document's supplement) |

### 9.4 Palette and form

The current image's 11 colours fall into 5 groups by use, and all are retained (pixel counts and colours below — changing the image **without** changing the palette is this item's **core constraint**, otherwise "changing the material" would change the art style along with it):

| Group | Colour value (pixel count) | Where it is used |
|---|---|---|
| Leaf | `#3FA34D` (33) | the top leaf (the largest colour block) |
| Fruit · dark | `#54090E` (5), `#9C1017` (11) | the fruit's bottom shadow and outline |
| Fruit · mid | `#B4131E` (28) | the fruit's body |
| Fruit · bright | `#DD1725` (14), `#FF1C2B` (12) | the fruit's lit side |
| Highlight | `#FF969D` (4), `#FF5E69` (2) | the two highlight points at the fruit's upper left |
| Stalk / veins | `#752802` (17), `#7E370E` (1), `#542409` (3) | the stalk and where the leaf joins the fruit |

**Form requirements (this document's supplement)**: ① the fruit sits at the centre-lower part of the image with the leaf above and to the right, keeping the current image's compositional direction (on 16 × 16 composition matters more than detail); ② the highlights concentrate on the **upper left**, matching the lighting direction the mod's other items already use; ③ a 1 px dark separation (`#542409`) must be left between fruit and leaf, otherwise the two smear into one blob at 16 × 16.

### 9.5 Do-not-touch list

| Item | Grounds |
|---|---|
| The model's `layer0 = farmstorm:item/wild_apple` | D-105 |
| The item id, the language keys, the registry surface (no second image is added) | D-105 |
| Registration of the source material and its non-commercial personal use (per D-66's measure) | D-105's landing position |

### 9.6 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| Throwing the original image straight into the jar | **not allowed**: 1,113,442 B differs from a 16 × 16's hundred-byte order of magnitude by three orders of magnitude (D-105's grounds), and the GPU samples by the 16 grid anyway, so all that extra resolution is invisible |
| New colours appear after downsampling | they must be quantised back to 11 colours (§9.3); otherwise dozens of near-colours appear on 16 × 16 and look like noise |
| The highlights bunch into one blob | on 16 × 16 the highlight keeps only 4 + 2 = 6 pixels (reusing the current image's two-tier pixel counts); any more and there is no distinction between "highlight" and "bright face" |
| The author later wants a different image | replace the same-named PNG only, touching neither model nor code (the same mechanism as §3.7) |

---

## 10. Advancements and numbers summary

### 10.1 Registry surface and advancements

| registry | Increment | Detail | Grounds |
|---|---|---|---|
| `blocks` | **+1** | `outpost_core` (Outpost Core) | D-119; BRIEF §3.1 |
| `block_entities` | **+1** | `outpost_core`'s `BlockEntityType` | D-120; BRIEF §3.1 |
| `items` | **+1** | `outpost_core`'s `BlockItem` | D-122; BRIEF §3.1 |
| the other 9 categories | **+0** | `entity_type` / `mob_effect` / `sound_event` / `damage_type` / `recipe_type` / `recipe_serializer` / `creative_mode_tab` / `data_component_type` / `menu_type` | BRIEF §3.1 |
| Total | **+3** (90 → 93) | — | 22 §3.2 |
| Advancements | **+0** (still **18**) | none of the 9 requirements asks for an advancement | 22 §3.2's footnote; BRIEF §0 |
| Crop profiles | **±0** (still **22**) | only the category of 2 profiles changes | §5.2 |
| Crop blocks | **±0** (still **15**) | the carrier of growth changes; no block and no block entity is added | D-110 supplementary item; D-111 item 2 |
| Custom commands | **+1** | `/farmstorm farmlandlimit <n>` | D-116 |

**Said once more (both 22 §0 and BRIEF §3.1 point at this cell)**: **the total 93 is confirmed by `24-TECH-0.1.0.md` itself after counting the source class by class and naming every entry; this document records only 90 + 3.** Both 0.0.2 and 0.0.3 crashed on exactly this cell.

### 10.2 The 12 config rows (11 new + 1 default changed)

| # | Group | Key | Type | Default | New / changed | Use | Decision |
|---|---|---|---|---|---|---|---|
| 1 | `growth` | `minSeconds` | int | **20** | new | lower bound of the total time to maturity (seconds) | D-110 |
| 2 | `growth` | `maxSeconds` | int | **45** | new | upper bound of the total time to maturity (seconds) | D-110 |
| 3 | `trap` | `immunityTicks` | int | **100** | new | ticks of broad-category immunity after a trigger (5 seconds) | D-112 |
| 4 | `trap` | `dustIntervalTicks` | int | **100** | new | interval of the traps' dust particles (5 seconds) | D-113 |
| 5 | `trap` | `maxTrapsPerPlayer` | int | **16** | new | per-player trap allowance (FIFO) | D-115 |
| 6 | `farmland` | `limitPerPlayer` | int | **0** | new | the server-side default of the farmland quota | D-116 |
| 7 | `outpost_core` | `farmlandBonus` | int | **16** | new | the farmland cap added by each core | D-120 |
| 8 | `outpost_core` | `maxPerPlayer` | int | **2** | new | cores a player may place | D-122 |
| 9 | `outpost_core` | `boundaryParticleIntervalTicks` | int | **40** | new | refresh interval of the boundary particles (ticks) | D-121 |
| 10 | `melon_grenade` | `horizontalJitter` | double | 0.3 → **0.05** | **changed** | tightening of Melon Slice Bomb's landing spread | D-109 |
| 11 | `melon_grenade` | `submunitionSpeed` | double | **0.1** | new | radial speed (previously the hard-coded 0.3) | D-109 |
| 12 | `compressed` | `replaceOnlyAirAndFluid` | boolean | **true** | new | the landed block replaces only air and fluids | D-106 |

**12 rows ≠ 12 new keys**: row 10 changes an existing key's default from 0.3 to 0.05 (the key has existed since 0.0.4), so the new keys are **11**, and the config key count goes **155 → 166** (155 + 11). This version adds no key at all for the 3 × 3 range, the 48 × 48 boundary, the boundary granularity or Zangief Cane's mob settlement (22 §5.3, §6 item 9).

### 10.3 Assets

| Category | New | Replaced in place |
|---|---|---|
| PNG | **5**: `outpost_core_top` / `_side` / `_bottom` (32 × 32 each, D-123) + `compressed_hay_block_top` / `_bottom` (16 × 16 each, §4.6) | **6**: `friend_chives_stage0..3` (**4**, D-104) + `wild_apple.png` (D-105) + `compressed_hay_block.png` (grey scale to yellow-brown, D-107) |
| JSON | **5**: `blockstates/outpost_core.json`, `models/block/outpost_core.json`, `models/item/outpost_core.json`, `data/farmstorm/loot_table/blocks/outpost_core.json` (D-119 item 5: mining drops go through the block's own loot table; `ModLootTableProvider` currently 25 tables → **26**, A-27), and 1 Outpost Core crafting recipe | **2**: the Compressed Puffed Wheat block model (`cube_all` → `cube_bottom_top`), the Compressed Puffed Wheat item model (`generated` → pointing at the block model) |
| Language keys | block name / tooltip / command feedback / config comments; **the count is not fixed here** | — |

Two notes under the table:

1. **Which four are "the four"**: "Friend Chives stage0..3" is this version's only **whole-group replacement** of textures (the whole of requirement 2, D-104); the other two replacements (`wild_apple.png`, `compressed_hay_block.png`) serve requirement 9 and one sub-item of requirement 3 respectively. The replacement total is 4 + 1 + 1 = **6** and the new total 3 + 2 = **5** (both agree with `22-DECISIONS-0.1.0.md` §3.3).
2. **One image becomes an orphan**: `textures/item/compressed_puffed_wheat.png` is referenced by no model after the item model becomes 3D (§4.4). This document's measure is to keep it and not delete it; it bears on neither the "new" nor the "replaced" count.

### 10.4 Other numbers at a glance

| # | Number | Meaning | Source |
|---|---|---|---|
| 1 | 39 → **52** | the charging bar's distance from the screen's bottom edge (GUI px) | D-103; §2.5 |
| 2 | **13** | amount lifted = 52 − 39 | D-103; §2.5 |
| 3 | 0.3 → **0.05** | Melon Slice Bomb's horizontal jitter upper bound | D-109; §5.5 |
| 4 | 0.3 → **0.1** | Melon Slice Bomb's radial initial speed (new key) | D-109; §5.5 |
| 5 | 4 → **2** | the startup self-check's Demolition-profile count | D-108; §5.2 |
| 6 | **4** | the number of Melon Slice Bomb slices (unchanged) | D-109; §5.5 |
| 7 | **20 / 45** | the interval of a crop's total time to maturity (seconds) | D-110; §6.3 |
| 8 | **402 / 900** | the lower / upper bound of the total tick count: `3 × 134` and `3 × 300` (402 is the lower bound of 3 × 134, keeping 20.1 seconds inside [20, 45] seconds) | §6.3 (A-26) |
| 9 | **3** | three advances (four stages) | D-110; §6.5 |
| 10 | **100** | the duration of trap immunity (ticks) = 5 seconds | D-112; §7.3 |
| 11 | **100** | the dust-particle interval (ticks) = 5 seconds | D-113; §7.4 |
| 12 | **2** | dust particles per emission | D-113 supplementary item; §7.4 |
| 13 | **16** | the per-player trap allowance | D-115; §7.6 |
| 14 | **5 / 8 / 32** | the three deployment safety valves (untouched) | §7.6 |
| 15 | **0** | the default farmland quota | D-116; §8.1 |
| 16 | **16** | the quota added by each core | D-120; §8.4 |
| 17 | **2** | the per-person core cap | D-122; §8.3 |
| 18 | **30 / 1200** | the core's hardness / blast resistance | D-119; §8.3 |
| 19 | **3 × 3 / 9 / 48 × 48** | the core's affected range (chunks / number of chunks / blocks) | D-118, D-120; §8.2 |
| 20 | **40** | the boundary-particle refresh interval (ticks) | D-121; §8.7 |
| 21 | **4 / 48** | the boundary sampling interval (blocks) / points per loop | D-121 item 5; §8.7 |
| 22 | **7.5** | Zangief Cane's generic-part damage against mobs | D-114 item 3; §7.5 |
| 23 | **16 × 16** | the texture size of Friend Chives / Wild Apple / Compressed Puffed Wheat | D-104, D-105, §4.6 |
| 24 | **32 × 32** | the size of the Outpost Core's three textures | D-123; §8.3 |

---

## 11. Register of exceptions and boundary handling

The numbering follows the style of `17-DESIGN-0.0.3.md` §12; there are **13 entries** in total (11.1 ~ 11.13). Each gives the rule and the grounds only; the numbers are looked up in their own sections.

### 11.1 The core against pistons / explosions / the void

- **Pistons**: it can neither be pushed nor pulled — `pushReaction(PushReaction.BLOCK)` is written in the registration `Properties` (D-119 item 3; precedent `registry/FarmstormBlocks.java:101-102`). A registration property rather than a piston-event listener: an event cannot stop a push that has already happened, so the property is the only criterion.
- **Explosions**: blast resistance 1200 (D-119). Vanilla's maximum explosion power is far below that, so an explosion does not demolish the core; `/kill` kills entities, not blocks, and is beside the point.
- **The void**: the core is a block and there is no such thing as "falling"; if it is overwritten by `/setblock air` or by world generation ⇒ the "core removed" path applies (§8.6's conversion to dirt triggers as usual).
- **Grounds**: D-119, D-120 item 4.

### 11.2 The core's chunk unloads

- **No tick, no particles**: the boundary particles are the block entity's tick behaviour (§8.7) and are not emitted after the chunk unloads; they resume after reload, counting the phase from 0 again — visually it is only "two seconds missing", and no decision is affected.
- **The range check does not depend on loading**: §8.2's range criterion is a **chunk-coordinate comparison**, computable even when the core's block entity is not in memory (as long as it is not "reading the core's NBT"). Tilling happens near the player, and the player's chunk is always loaded.
- **Quota summation does not depend on loading either**: the quota is summed as "the number of cores placed × 16", and the count comes from a **server-side index** of the same kind as D-115's (it shares the same "cores under this player" query as the boundary particles, D-122 supplementary item), not from walking the world for blocks.
- **A known cost**: after the core is moved away by an external tool (a clipboard mod, a chunk editor), a stale entry pointing at air remains in the index ⇒ discarded by **lazy validation** as in D-115 (the same mechanism as D-115's supplementary item).

### 11.3 Quota reclamation when a player logs out

- **Already tilled farmland is not reclaimed**: the quota is a "cap", not a "ration"; a player going offline does not turn the farmland in his fields to dirt (not retroactive, D-116 item 3).
- **Whose the quota itself is**: with a team the cap is the team's sum, so when one member logs out the team cap loses one personal quota ⇒ teammates cannot open new land when the quota is short, but not one plot already opened changes (§8.4's inference, the same measure).
- **A teamless player logs out**: his quota becomes unreachable while he is offline (nobody is reading it), and the farmland stays in the world as before.
- **Grounds**: D-116 item 3, D-117 items 1 and 3.

### 11.4 The team disbands or a player switches teams

- **Resolved live, no snapshot kept**: both the ally judgment and the team summation are computed by `TeamResolver` (D-06) **at the moment of reading** (C-9), so disbanding / switching teams / renaming need no data migration.
- **The books after a disband**: the old team's coordinate set has no owner ⇒ those pieces of farmland no longer count under anyone's name, but **the blocks are still farmland** (not retroactive). Former members then count their tilled number from 0 in a new team.
- **Switching is double-edged**: joining someone else's team merges one's own quota with theirs (summation), but also brings their tilled count in to be shared with you; conversely, switching out immediately loses the quota. Neither outcome needs extra code; both are direct consequences of the summation rule (this document's supplement).
- **A core's ownership does not change when teams change**: the core stores the placer's UUID, so after a team switch it takes effect for the **new teammates** and stops taking effect for the **old ones** (D-119 item 4 + C-9's live resolution).
- **Grounds**: D-06, C-9, D-117, D-119 item 4.

### 11.5 FIFO and chunk unloading

- **The index does not lapse when a chunk unloads**: an entry is "UUID → a list of coordinates", which is the same whether the coordinates are in memory or not; the lazy validation is done **only at load time** (the position must still hold a TRAP deployed object under that player).
- **A trap in an unloaded chunk still takes up an allowance**: it is a fact that it is "placed"; the world is not actively scanned to find out whether it is still there — that would turn one deployment into a server-wide scan.
- **Squeezing out a trap that is in an unloaded chunk**: removal goes through `level.removeBlock(pos, false)`, which fails silently for an unloaded chunk ⇒ **no delay, no queueing**: the entry stays in the index and is judged at the next validation. This is this item's known boundary — the earliest one far away may not be squeezable, and in that case **the 17th is still placed as usual** (better one trap too many than refusing a legitimate deployment because of an old trap that cannot be found) (this document's supplement).
- **Grounds**: D-115 item 4 and its supplementary item.

### 11.6 Ally judgment when a mob triggers

- **Always non-ally**: a mob is not in the team member table and `TeamResolver` has no answer for it ⇒ treated as a non-ally (D-114 item 4).
- **Consequence**: a zombie running through an ally's territory will trigger the ally's traps — this is intended: a trap defends against "whoever steps in", and a mob stepping in has stepped in (plan `:33`'s own wording is "all hostile mobs").
- **Creative / spectator players are unaffected**: they still do not trigger (D-114 item 4), and that does not interfere with the mob clause.
- **Grounds**: D-114 items 1 and 4.

### 11.7 Growth advance at a chunk boundary

- **A crop itself never crosses chunks**: a crop occupies a single block position, so a "cross-chunk crop" does not exist.
- **The scheduled tick travels with the chunk**: the pending queue is carried by the chunk itself, so "the crop is in chunk A and the player in chunk B" only means "you cannot see it growing", not "it is not growing".
- **Batch advances across chunks within one tick**: one chunk load may bring dozens of plants due at once; they are all settled one by one in the main thread's block-tick stage, in an order given by the queue's `(due tick → insertion order)`, which this mod does not interfere with (this document's supplement).
- **Dig, then plant**: a new crop at the same position schedules a new entry; if the old one is still in the queue it is treated as "due early" when its moment comes — it advances once and does not skip a level (§6.10).
- **Grounds**: D-111 items 1 and 2.

### 11.8 The race between bone meal and a scheduled tick

- **Rescheduling overwrites, it does not append**: `Level.scheduleTick` deduplicates and overwrites for the same block at the same position. So rescheduling after a bone-meal advance cannot produce "two scheduled ticks ⇒ jumping two levels at once".
- **Both due in the same tick**: whichever executes first advances first; if bone meal settles in exactly the same tick as the scheduled tick, the outcome is "one advance + one reschedule", with no double advance (the reschedule overwrites the same entry).
- **The one that must be measured**: if the overwrite semantics do not hold in the target version (a real double advance appears), the fallback is "carry a generation number in the callback and discard outright any old entry whose `age` does not match", **without changing this item's measure** (this document's supplement, registered in `24-TECH-0.1.0.md`'s acceptance self-check).
- **Grounds**: D-111 item 4.

### 11.9 Does a refused tilling consume durability

- **No**: with an insufficient quota, or outside an ally core's range, neither kind of refusal **consumes hoe durability or produces farmland** (D-116 item 4, D-118 item 3).
- **Why**: the quota and the range are **rules**, not player mistakes; charging durability for one hoe swing in a field with no core is punishment in place of information.
- **Message text**: a refusal gives one action-bar message (the text and language keys are pinned down by `24-TECH-0.1.0.md`).
- **Grounds**: D-116 item 4, D-118 item 3.

### 11.10 Two cores' ranges overlap

- **Can it be tilled**: the union — any ally core covering it is enough (D-118 item 4).
- **One piece of farmland covered by two ranges**: the ledger records one (the coordinate set deduplicates) and the quota is charged for one (D-118 item 4, D-117 item 4).
- **Demolish one of them**: **only the ally farmland inside the demolished core's own 3 × 3 is shovelled up**; fields in the overlap are shovelled up along with it (even though they are also inside the other core's range), because the original wording is "all ally farmland within the affected range becomes dirt" (plan `:45`, D-120 item 4). This is a **known consequence**, not a bug: the union serves only "can you till", not "protection when a core is demolished" (§8.8).
- **Grounds**: D-118 item 4, D-120 item 4.

### 11.11 The core is broken while farmland is being trampled

- **Order within one tick**: on the server's main thread "an entity tramples → farmland turns to dirt" and "a block is removed → the ledger's farmland turns to dirt" are two independent paths, and whichever gets there first takes effect; both end in "this plot turns to dirt", and **there is no double settlement of "turned to dirt and then converted again"** (the conversion to dirt presupposes "is the current block farmland" and skips otherwise).
- **Ledger consistency**: both paths delete that coordinate from the ledger (§8.6's ledger sync), so no dirty entry "already dirt but still on the books" is left; and even if one were left, the existence check at read time (is the current block still `minecraft:farmland`) would discard it (D-117 supplementary item).
- **Grounds**: D-120 items 3 and 4; D-117 supplementary item.

### 11.12 Creative / spectator exemption

Written separately in four places (the first three are given by existing decisions; the fourth is an inference added by this document):

| Place | Rule | Grounds |
|---|---|---|
| The trap trigger side | creative and spectator **do not trigger** (the existing exemption is carried over) | D-114 item 4 |
| The Outpost Core's boundary particles | **creative and survival are not distinguished**; only "is he an ally" matters | D-121 supplementary item |
| The farmland quota and range | **no exemption**: creative players are bound by the quota and range just the same | this document's supplement: neither D-116 nor D-118 has an exemption clause; the quota is a gameplay rule, not a damage check |
| The core's per-person cap | **no exemption**: a creative player also places only 2 | this document's supplement: D-122 says "each player may own at most 2 at the same time" |

Also: a spectator takes no damage, so he takes no trap damage segment either; but does he **still trigger traps** — no, the trigger side carries over the existing exemption (isomorphic to the same table in `17-DESIGN-0.0.3.md` §12.12).

### 11.13 Waterlogged blocks and the landing write allow-list

- **Rule**: positions such as waterlogged stairs / waterlogged fences / kelp, where "the block itself is not a liquid but the fluid state is non-empty", **get no hay written** (§4.2 step 5).
- **Why it deserves its own entry**: this is the cell most easily got wrong in an allow-list implementation — judging by `getFluidState().isEmpty()` would cover them wholesale as liquids, directly violating "no longer replaces ordinary blocks" (plan `:17`).
- **Control experiment**: with `compressed.replaceOnlyAirAndFluid` off these positions are covered, which is exactly 0.0.4's behaviour and can be used for reproduction and acceptance.
- **Grounds**: D-106 item 1 (the semantics) and its supplementary item (the writing is handed to 24).

---

## Appendix A · Register of differences between plan0.1.0.txt and this document

| # | The point of `plan0.1.0.txt`'s original text (line numbers) | How this document handles it | Decision | Nature of the difference |
|---|---|---|---|---|
| 1 | "Move the sprint charging bar up slightly so the health bar and hunger bar do not block it" (`:7-10`) | fixed as the constant `BAR_Y_FROM_BOTTOM` **39 → 52** (up 13 px); no second resolution-adaptive formula introduced; bar width / bar height / spacing / centring / colours / fill order all untouched | D-103 | refinement (turning "slightly" into one number) |
| 2 | "Add the material for Friend Chives in the growth state" (`:11-14`) | 4 stage-progressive 16 × 16 plant images (heights 4 / 8 / 12 / 16 aligned with the collision box, leaf counts 2 / 3 / 5 / 7, palette reusing `power_beet`'s 4 greens); **the generator changes at the same time**, otherwise a re-run overwrites them back to magenta | D-104 | refinement + the new hard constraint "the generator must change" (BRIEF §1.2's ⚠️) |
| 3 | "The block generated on landing can no longer replace ordinary blocks, but can still replace liquid blocks and air blocks" (`:17`); "change Compressed Puffed Wheat's model to a solid hay block" (`:18`) | landing becomes **five gates** (resistance baseline → switch → air → liquid → skip everything else), and the blank intermediate state (waterlogged blocks) is treated as "do not write"; Compressed Puffed Wheat's item model becomes 3D, its landed block becomes `cube_bottom_top` with six faces distinguished, the grey scale becomes yellow-brown and two new top / bottom textures are added | D-106, D-107 | refinement + a **widened measure**: plan says only "a solid hay block"; the six-face distinction and the tone are adjudicated by BRIEF §2 item 13 (D-107) |
| 4 | "Remove the Demolition entry from both, so they can no longer blow up blocks" (`:22`); "the Melon Slice Bomb slices produced by Melon Grenade have more concentrated landing points" (`:23`) | `DEMOLITION` is removed from the two profiles ⇒ **can no longer break blocks at all** (the only criterion, with no intermediate state); damage, knockback, power and resistance thresholds are all retained; the slice count stays 4, the jitter goes 0.3 → **0.05** and the radial initial speed 0.3 → **0.1** and from a hard-coded constant to a config key | D-108, D-109 | refinement + the new item "the speed goes into config" (BRIEF §3.2's requirement) |
| 5 | "Change the growth time of all this mod's crops to [20, 45] seconds" (`:25-28`) | **abandons random ticks** and switches to a scheduled-tick chain; the per-segment tick count `d = 134 + floorMod(pos.hashCode(), 167) ∈ [134, 300]`, the three segments are **equal in length**, and the total duration `3d ∈ [402, 900]` ticks = **20.1 ~ 45.0 seconds** (A-26 takes document 24's scheme, superseding the old "draw T first, then absorb the remainder"); brightness ≥ 9 remains a precondition; bone meal still works and reschedules by `d` | D-110, D-111, A-26 | **mechanism replacement** (RT-24); the three-segment split is superseded by A-26 in place of D-110 item 3 |
| 6 | "5-second same-kind trap immunity", "a small amount of dust particles every five seconds", "traps can be triggered by all hostile mobs", "each player may deploy at most 16 traps at the same time (FIFO)" (`:29-35`) | immunity is by **broad category** (not same kind); dust interval 100 ticks, **2** per emission, neutral earth tone; the trigger set = non-ally players + vanilla `Monster` (animals and villagers do not count), generic effects apply and player-only ones are skipped, Zangief Cane settles only 7.5 + knockback against mobs; FIFO **counts the TRAP category only**, allowance 16, the earliest deployed is squeezed out, the index is persisted to disk | D-112 ~ D-115 | refinement + **two measure corrections**: "same kind" → "broad category" (BRIEF §2 item 2); "all hostile mobs" → vanilla `Monster` (D-114 item 1) |
| 7 | "Limit the amount of farmland a player may till, default 0", "summed within the team", "tilling is allowed only in the chunk the ally Outpost Core is in or the eight surrounding chunks" (`:38`); "the cap may be changed with the `/gamerule` command" (`:39`) | the quota is carried by **the `/farmstorm farmlandlimit <n>` sub-command + a config default of 0** instead (this version **registers no vanilla rule object at all**, and cannot register one within the no-Mixin red line either); summed within the team, single-player when there is no team; the range is pinned to **the ally core's 3 × 3 chunks** (48 × 48 blocks); existing farmland is not retroactive; a refusal consumes no durability | D-116 ~ D-118 | **carrier replacement**: `/gamerule` → a sub-command (D-116, RT-25); **range refinement**: a chunk-aligned rectangle |
| 8 | "Outpost Core": modelled on ancient debris; +16 quota; its own chunk + the eight surrounding chunks; farmland in range is not trampled; after it is destroyed the ally farmland in range turns to dirt; at most 2 per person (`:41-47`); material directory `512x512\a_runic` (`:46`) | hardness **30** / blast resistance **1200** / **drops at any pickaxe tier** / not pushable by pistons / placeable anywhere / `setPlacedBy` writes ownership / drops itself; recipe stone in the four corners + diamond in the centre + redstone in four cells → 1; +16 each; 3 × 3 chunks and their union; trample protection and core-demolition conversion to dirt; boundary particles one point every 4 blocks, 48 points per loop, one round every 40 ticks, sent only when a player enters the range; textures use **the three `a_runic` of the `32x32`** set (32 × 32) | D-119 ~ D-122, D-118, D-123 | **material-specification replacement**: `512x512` → `32x32` (D-123; the other three style sets do not enter this version); **number split**: "modelled on ancient debris" takes hardness 30 and resistance 1200 (BRIEF §2 item 8 pins both numbers down) |
| 9 | Wild Apple's material changes to the desktop `2797B56FEF56AF13125F333A6989632A.png` (`:48-51`) | 1294 × 1216 RGBA (1,113,442 B) downscaled to a **16 × 16** finished image, quantised back to the current image's **11 colours**; model, id, language keys and registry surface untouched | D-105 | refinement (the size, colour count and quantisation steps are given by this document and by 24) |

Three notes under the table:

1. **The 9 rows correspond to plan's 9 functional requirements** (items 1 ~ 9, `:7-51`). plan's first 6 lines are version and platform information (mod name / version number / loader / MC version / "base on 0.0.4, and unless explicitly required do not change existing content") and contain no functional requirement; the authorisation scope in line 5 is transcribed verbatim into §1.4 of this document.
2. **What is unchanged and copied as it stands**: "base on version 0.0.4 and keep appending new items or features; unless this document explicitly requires it, do not modify 0.0.4's existing content" (`:5`) as the whole document's authorisation boundary; the **intent** of "refer to ancient debris for the block's concrete values" (`:44`) is retained (hardness copied as 30), with resistance taken as 1200, each number taking its own route. Both 【BRIEF §1.3】 and the comment at `registry/FarmstormBlocks.java:71` record "hardness as ancient debris, 30.0".
3. **Everywhere this document departs from plan, the table above has named it and given a D number**; a departure with no D number behind it counts as a document defect. This round's two **hard departures** are row 7's `/gamerule` → `/farmstorm farmlandlimit` (D-116, RT-25) and row 8's `512x512` → `32x32` (D-123); one **wording correction** is row 6's "same kind" → broad category (BRIEF §2 item 2, D-112); one **object correction** is row 6's "all hostile mobs" → vanilla `Monster` (D-114 item 1); one **number split** is row 8's "refer to ancient debris" (BRIEF §2 item 8).
## Translator's notes

1. **Re-mirrored from the A-26 ruling (equal-length segments)**: §6.1, §6.3, §6.4, §6.5, §6.6, §6.8, §0.3 item 6, §10.4 row 8 and Appendix A row 5 now read the equal-segment scheme — `d = 134 + floorMod(pos.hashCode(), 167)` ⇒ `d ∈ [134, 300]`, three segments of `d` ticks each, `T = 3d ∈ [402, 900]` ticks = **20.1 ~ 45.0 seconds**, **no remainder**. The superseded "draw `T` first, then absorb the remainder" scheme survives only as the deliberately registered **rejected scheme** paragraph in §6.3, together with its `T ∈ [400, 900]` / 133 / 133 / 134 ticks = 400 ticks = 20.0 seconds worked example; the Chinese source keeps it on purpose as a guard against later re-proposal, so it is translated in full rather than trimmed.
2. **Resolved in the upstream source (A-27)**: the JSON increment conflict this translation originally recorded is closed. Both `23` §1.3 / §10.3 and `24-TECH-0.1.0.md` §9.2 now record **5 new + 2 replaced in place**, in the order blockstate → block model → item model → **loot table** → crafting recipe, the fifth being `data/farmstorm/loot_table/blocks/outpost_core.json` (D-119 item 5; `ModLootTableProvider` 25 tables → 26). This copy follows the corrected text.
3. **A particle specification that still disagrees with 24-TECH-0.1.0.md, left as written**: this document's §7.4 gives the trap dust as a neutral earth tone `(0.62, 0.56, 0.44)` at `(x + 0.5, y + 0.15, z + 0.5)` with spread `0.25 / 0.10 / 0.25`, while 24 §5.2 uses `DustParticleOptions(new Vector3f(1.0F, 0.15F, 0.15F), 1.0F)` (the red of `SMOKE_RED`) at `Vec3.atCenterOf(pos)` with offset `(0.2, 0.1, 0.2)`; §8.7 gives the boundary particles as `minecraft:dust` coloured `(0.30, 0.85, 0.90)` with a two-tier falloff (full rate inside 48 × 48, one emission in four in the 16-block margin band), while 24 §6.6 uses `ParticleTypes.END_ROD` with a three-tier falloff (48 / 24 / none). Each document marks its own version 【本文补充】, and neither cross-references the other on these points.
4. **Chinese strings that are program output stay in Chinese, character for character**, following the convention of `docs/en/18-TECH-0.0.3.md` and `docs/en/17-DESIGN-0.0.3.md`: the startup self-check log fragment `破障类 2 → FAIL` in §5.2 and `/farmstorm farmlandlimit`'s literal `farmlandlimit` are reproduced unchanged.
5. **The requirements text quoted in Appendix A is translated**, because those are the author's request sentences in `plan0.1.0.txt` rather than string values written into any file; the plan's line references (`:7-10`, `:17`, `:45` and so on) and every number are reproduced unchanged. Wherever this document discusses a literal that goes into `zh_cn.json` or a config key, the literal is kept in source form.
6. **Carried-over conventions**: the markup tag 【本文补充】 / 【BRIEF §x】 / 【D-1xx】 / 【P-x】 is left untranslated (as in `docs/en/17-DESIGN-0.0.3.md`); `man瓜` is rendered "Pumpkin Turret", following that document's note; Chinese 「」 quotation marks are rendered as English double quotes.
7. **A translation term, defined once and used throughout**: 距底 is rendered "bottom-offset" and defined in §0.3 item 5 (`guiHeight() − y`, with the half-open interval `[a − h, a)`); the 半开区间 (half-open interval) notation and every arithmetic expression in §2 are reproduced as written.
8. **Levitation wording**: where "levitation" appears it is either a negation or a history — §7.5 writes that `PHASE_LEVITATING` "is merely a **historical misnomer**" and "is not the vanilla levitation effect", with `MobEffects.LEVITATION` used only as an old-save fallback removal; the mechanism itself is written as "phase 1 is 'gravity off + servo speed computed from the remaining height and the remaining tick count, lifting to a fixed height'". No mechanism sentence in this document says "floats" or "levitates".
