# 29 · v0.1.1 Design Delta

> Project: Farmstorm · Version: v0.1.1 · Basis: `docs/zh/28-DECISIONS-0.1.1.md` (D-124 ~ D-147, 24 in total) + `plan0.1.1.txt` (33 lines, **5 original plan items**, split by this document into **10 requirement items R1~R10**; the split measure is in §1) + `.work/v011-BRIEF.md` (the fact card) · Relation to 0.1.0: a **delta**; it neither changes `docs/zh/23-DESIGN-0.1.0.md` nor restates it.

| Item | Value |
|---|---|
| Document | 29-DESIGN-0.1.1 |
| Status | Design final (the last version before the Chinese text is frozen) |
| Upstream (single source of truth) | `28-DECISIONS-0.1.1.md`: the measure is taken from it alone; this document is responsible only for "what the requirement looks like" |
| Upstream (the author's original text) | `plan0.1.1.txt`: the **5 original plan items** are at `:7-33` (this document splits them by acceptance face into 10 requirement items R1~R10; the split measure is in §1); `:5` is the authorisation boundary of the whole document |
| Upstream (factual base) | `.work/v011-BRIEF.md`: §1 source-code facts (with `file:line`), §2 the author's 25 adjudications, §3 the increment list |
| Downstream | `30-TECH-0.1.1.md` (class design, the full config table, save format, packet codec), `31-TASKS-0.1.1.md` (tasks and scheduling) |
| Language | Body text in Chinese; code identifiers, registry names and config keys always in English |

---

## 0. Reading conventions

### 0.1 Division of labour between this document and 28

28 fixes the **measure** (a one-sentence conclusion + grounds + landing position + knock-on items); this document writes **what it looks like** (range, format, sequence, numbers, boundaries, and behaviour that only becomes visible at acceptance). Three rules of the division:

1. **The measure is not invented here.** Every conclusion in this document has a decision number (`D-1xx`) behind it; if a reader finds a conclusion that cannot be traced back to a decision number, that is a defect of this document, not a blank that may be filled in by one's own interpretation.
2. **28's body text is not restated.** This document cites numbers rather than copying conclusion sentences; where the reader should "read item so-and-so of 28 first", the number is written out directly.
3. **What is not here**: the full config table (30), class design / packet codec / the save's binary format (30), task numbers and man-hours (31). This document names config keys and defaults only in the "Basis" column.

### 0.2 The test for "[author's supplement]"

| Type | May be marked "[author's supplement]" | Explanation |
|---|---|---|
| Arithmetic | yes | a third number computed from two given numbers; the expression must be written out (e.g. `15 − 0 = 15`) |
| Explicitly authorised by 28 | yes | 28 says "fixed by 29" (line spacing, font size, padding, the "+N" glyph, the clamp expression, colours) |
| A reading the upstream does not cover | yes | it must come with "why it can only be read this way", and be registered in §11's boundary list |
| Given by the fact card, not yet ruled on by the author | marked "[card supplement (vetoable)]" | e.g. the 8-row cap, the default `(4,4)`, self green / allies white, the dimension path. **Not this document's own invention**; the author may veto it |
| Conflicting with 28 or the fact card | **not allowed** | a conflict can only be reported back to the author; one may not pick one side and write it in |
| Aesthetic preference | **not allowed** | "I think it looks good" is not a reason; a decidable criterion must be given (legibility, distinguishability from existing elements, the pixel account) |

A number in this document has only four legitimate sources: it is written in 28, it is written in the fact card, it is arithmetic with the expression written out, or it is marked "[author's supplement]" with a reason given. Anything that is none of the four is a document defect.

### 0.3 Markup

| Markup | Meaning |
|---|---|
| [D-1xx] | the conclusion comes from the corresponding decision in 28 |
| [card §1.x] | the conclusion comes from the corresponding subsection of the fact card |
| [`path:line`] | a line taken directly from the source code; paths are relative to `src/main/java/com/dashpig/farmstorm/` (resources and tools are noted separately) |
| [author's supplement] | derived by this document under the six tests of 0.2 |
| [card supplement] | an item noted under the table in fact card §2 as "not the author's adjudication, vetoable" |

**Deviation from the English set of 23 (intentional)**: the **English edition** of 29 writes the markers above as English brackets (`[author's supplement]` / `[card]` etc.) and does **not** use `【】` — Chinese punctuation in an English document is a leftover from the previous round, and is not reused in this version. The Chinese edition keeps using `【】`; the two editions' markers correspond one to one and mean the same thing, and this is not a translation omission.

### 0.4 Units

Time is measured in game ticks (tick), **1 second = 20 ticks**; length in blocks (block); interface dimensions in GUI px (already-scaled pixels), which are a different dimension from texture pixels and are never converted into one another. §9 is the single place where all the numbers in this document are collected.

---

## 1. Overview of the requirements (10)

| No. | Topic | Type | Decision | plan line | One-line outcome |
|---|---|---|---|---|---|
| R1 | Wild Apple texture redo | change to existing | D-125, D-126 | `:7-11` | the 16 × 16 finished image with the green "+" is replaced by the 64 × 64 original used directly, and the generator's six rollback points are changed in the same version |
| R2 | Wrong settlement where several cores overlap | change to existing (structural) | D-134, D-136 | `:14` | "settle whatever is in range" becomes "settle by the binding table", and farmland in the overlap is no longer shovelled up incidentally by whichever core |
| R3 | Owner going offline loses protection | change to existing (structural) | D-134, D-138 | `:15` | the protection criterion moves from "traverse the online players" to "look the core owner's UUID up from the farmland", and the owner stays protected while offline |
| R4 | Outpost Core light level 15 | addition | D-141 | `:16` | one line of block light is added at registration; every other property is untouched character for character |
| R5 | Top-left HUD overlay | addition | D-142, D-143, D-144 | `:17` | 1 S2C read-only packet + a self-drawn overlay + 2 key mappings + a 3-key CLIENT config + drag positioning |
| R6 | Compressed Hay Block sound | change to existing | D-130 | `:20-23` | `SoundType.GRASS` → `STONE`, and five classes of sound change together |
| R7 | Pumpkin Turret attack interval and damage | change to existing | D-127, D-128 | `:26` | firing interval 60 → 40 ticks, damage per shot 1 → 3 |
| R8 | Pumpkin Turret bullet speed and tracking fix | change to existing | D-129 | `:27` | bullet speed 0.6 → 0.45, promoted to a config key; only the point-blank overshoot is fixed, no soft tracking is added |
| R9 | Friend Chives 200-tick persistent field | change to existing | D-131, D-133 | `:31` | the one-shot landing becomes a field scanned every tick for 200 ticks; the entity survives the full 200 ticks and stays visible |
| R10 | Friend Chives sound judgment | change to existing | D-132 | `:31` | the sound is still judged once, at the moment of landing: no sound on an empty drop, no catch-up sound for later entrants |

**How 5 original items become 10 requirement items**: plan item 1 → **R1**; item 2 → **R2 / R3 / R4 / R5** (the two bugs, the light level and the overlay are four distinct acceptance faces); item 3 → **R6**; item 4 → **R7 / R8** (numbers and the tracking fix are accepted in different ways); item 5 → **R9 / R10** (the field and the sound are two independent criteria). So 5 → 10 splits the **acceptance faces**; it does not add 5 requirements. Merged back, they are plan's 5 items.

Three points to take from the table:

1. **R2 and R3 are not two independent bugs.** In this version they are merged into one structural change, the "farmland–core binding" (§3); this version does not fix one if each. The table still gives them two numbers because they correspond to two sentences inside plan's item 2, and acceptance has to reproduce each one on its own.
2. **R2's "type" reads "change to existing (structural)" rather than "addition"**: the binding table itself is new persistent data (registered as new in 28 §3.1 item 6), but the **requirement's** nature is fixing an existing bug, and the reporting measure follows the requirement's nature.
3. **R1 and R5 are the only two changes "a player can see at a glance"** (the icon, and something extra on the screen); the other eight are changes at the behaviour or number level, and §10 is the comparison table.

---

## 2. Wild Apple texture (R1)

**Grounds for this section: D-125, D-126.**

### 2.1 One-line positioning

`textures/item/wild_apple.png` is replaced by the **output as-is** of `wild_apple_64x64.png`: no downscaling, no more 16-cell modal colour picking, no more green "+" overlaid. **At the same time** the generator's six rollback points are changed (§2.6), otherwise the next re-run puts this image back to 16 × 16. The model, the item id, the language keys and the registry surface are all untouched.

### 2.2 What each of the two images is (measured)

| Item | Current image `textures/item/wild_apple.png` | Source image `wild_apple_64x64.png` |
|---|---|---|
| Size | 16 × 16 | 64 × 64 |
| File size | **571 B** | **6,946 B** |
| Distinct RGBA values | **88** | **1,814** |
| Fully transparent pixels | **107** / 256 | **1,471** / 4,096 |
| Opaque pixels | **149** (= 256 − 107, [author's supplement] arithmetic) | **2,625** |
| Semi-transparent pixels | 0 | 0 |
| alpha bounding box | — | x ∈ [2, 60], y ∈ [0, 63] |
| Green "+" | **present**: 33 px of `#3FA34D`, a 7 × 7 cross at x ∈ [9, 15] × y ∈ [9, 15] | none |
| A 4 × nearest-neighbour upscale | — | **no**: of the 256 4 × 4 blocks, **204** are non-uniform |

[card §1.1] gives every pixel statistic; "the current image has 149 opaque" is the arithmetic 256 − 107, [author's supplement].

That 33 px cross is **self-consistent**: `7 × 3 + 3 × 7 − 3 × 3 = 21 + 21 − 9 = 33`, that is, it is a **3 px wide** cross and it lands exactly at the bottom-right corner `left = 9 / top = 9` [`tools/gen_textures.py:612-623`] [author's supplement: the expression]. This explains why "the current texture already carries a `+`" — it was not drawn by an artist; the generator pasted it on.

### 2.3 What "the original image directly" means (three things)

| # | Meaning | Counter-example (not done in this version) |
|---|---|---|
| 1 | the committed PNG is a byte-for-byte copy of the source image: size unchanged, no quantisation, alpha untouched | 0.1.0's "shrink to 16 × 16 + modal colour picking" |
| 2 | the generator **no longer runs** `wild_apple` through `wild_apple_grid()`'s 16-cell colour picking [`tools/gen_textures.py:849-881`, `:884-895`] | keep the colour picking and only "add detail" after it |
| 3 | the generator **no longer overlays** the marker on `wild_apple` [`tools/gen_textures.py:407`, `:974-975`] | keep the overlay and merely make the marker transparent |

The list of what is not changed: `models/item/wild_apple.json`'s `parent = minecraft:item/generated` and `layer0 = farmstorm:item/wild_apple` [`models/item/wild_apple.json:1-6`], the item id, the language keys, the registry surface, and the `ICONS` table's entry itself (`zh_name` and the other metadata stay as they are). No second texture is added either.

### 2.4 The rendering fact: the 4× detail cannot be seen (RT-37)

This section is **expectation management** for the author, not implementation detail.

1. `item/generated`'s item model is a **1 × 1 model-unit quad**, drawn in the inventory into a **16 × 16 GUI px** item cell. So **the display area is always 16 × 16**, whatever the texture resolution.
2. A 64 × 64 texture pushed into a 16 × 16 display area means an average of `64² ÷ 16² = **16**` source pixels (a 4 × 4 block) per display pixel [author's supplement: arithmetic]. Filtering and mipmapping flatten that area into one colour.
3. So what the player still sees is "a 16 × 16 icon", except that each of its pixels comes from a real pixel of the original rather than from the mode among 88 discrete colour steps. **This is not 4× sharpness; it is a more accurate hue and a more natural transition.**
4. Counter-check: if the source image were merely a 4 × enlargement of a 16 × 16 image, then all 256 4 × 4 blocks should be uniform; measured, 204 blocks are not uniform [card §1.1] ⇒ the detail in this image is real, it simply **cannot be shown**.
5. One concluding sentence: the value of the swap lies in "the original's composition and colour gradation", not in "resolution". Any statement of the form "once it is 64 × 64 the detail becomes visible" is wrong (D-126's supplementary item lists this among the items 29 / 30 must state).

### 2.5 The scope of the abolished "+" rule and the landing rhythm

| Aspect | What this version does | Basis |
|---|---|---|
| Rule | **abolished globally**: `NO_PLUS_ICONS` / `add_plus` / `build_marker` / `compose_icon` and the composition branch are deleted together | D-125 |
| Images | **only `wild_apple` is regenerated** | D-125 |
| The rest | of the 26, 25 carry a `+` and only `converter` is exempt from the overlay [`tools/gen_textures.py:274-275`] ⇒ after this version's change **24 are still in the old form** (25 − 1 = 24) | D-125 supplementary item |
| Next version | regenerate those 24 in a batch; no decision needs changing any more (the rule is already out of the code) | 28 §7 item 1 |
| Judgment boundary | "the rule is abolished" ≠ "no icon in this version carries a `+`" | fact card §6 item 7 |

### 2.6 The traps the generator will spring (six, named one by one)

**This section is the acceptance focus of this item**: swapping the PNG alone is certain to fail, because there are three independent rollback paths.

| # | Carrier | What happens if only the PNG is swapped | Evidence |
|---|---|---|---|
| 1 | `wild_apple_grid()`'s 16-cell modal colour picking | a re-run picks colours by the 16 grid again and the output goes back to 16 × 16 | `tools/gen_textures.py:849-881`, `:884-895` |
| 2 | `TEXTURE_SIZE = 16` and `compose()`'s normalisation branch | even if the pass-through branch returns 64 × 64, it is resized back to 16 × 16 | `:59`, `:976-978` |
| 3 | `expected_size()` and the size assertion in `main()` | the pass-through branch changes `compose()` but not this ⇒ the build goes straight to `SystemExit` (`尺寸断言失败`) | `:982-983`, `:1153-1154` |
| 4 | `WILD_APPLE_SOURCE` defaults to the old image on the desktop | a re-run picks the wrong material (old image → mode → 16 × 16) | `:154`, `:155` (overridable by an environment variable) |
| 5 | `build_marker()`'s call site and the dry-run text | delete the function but not the call site ⇒ the name does not exist at runtime; do not delete the text ⇒ the dry-run still prints the "+ 绿色「+」" line | `:1127`, `:1141-1142` |
| 6 | `compose_icon()` | delete the overlay mechanism but not the call site ⇒ the function referenced at `:975` does not exist | `:612-623`, `:974-975` |

Three criteria (gone through one by one at acceptance):

1. Delete the committed `wild_apple.png` and re-run the generator: the file produced must be **64 × 64** and byte-for-byte identical to the source image.
2. In the same re-run, the other 24 old images carrying a `+` **must not** be touched (this version does not re-run the others).
3. Two re-runs produce byte-for-byte identical output (the generator has no dependence on random numbers or timestamps).

### 2.7 Consequences for how it looks

| Item | Fact | What the player sees |
|---|---|---|
| The icon sits "flush to the edge" | the source image's alpha bounding box is y ∈ [0, 63], x ∈ [2, 60] ⇒ the content almost fills the canvas | after the swap this item's icon is **larger and fuller** than the other items in the same row, with none of the usual 1 px of padding [author's supplement: derived from the bounding box] |
| Edges | both images' alpha takes only the values 0 / 255 | intermediate alpha when shrinking is produced only by sampling, and no semi-transparent seam comes with the source image [author's supplement] |
| Detail | 16 source pixels pressed into 1 display pixel | the 4× sharpness is not visible; what is visible is a more natural gradation (§2.4) |
| The "+" | the old image's 33 px green cross disappears with the swap | the bottom-right corner no longer has a green cross; the other 24 still do |

The "flush to the edge" item is not a defect: D-126 requires "output as-is", which rules out both cropping and scaling, so the look can only be decided by the source image. Tightening the padding would be an asset question for the next version, not an implementation question for this one.

### 2.8 Do-not-touch list

| Item | Basis |
|---|---|
| `models/item/wild_apple.json`'s `parent` and `layer0` | D-126 supplementary item |
| the item id `farmstorm:wild_apple`, the language keys, the `creative_mode_tab` entry | D-126 supplementary item |
| the registry surface (no second texture, no new model) | D-126 supplementary item |
| `wild_apple`'s entry in the `ICONS` table and the position it holds | only "how it is produced" changes, not "which ones are produced" |

### 2.9 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| The author swaps in an image of his own later | swapping in a PNG of the same name is enough; but **the generator's pass-through must be kept**, otherwise the next re-run puts it back to 16 × 16 (§2.6 row 1) |
| The source image's path moves | override it with the environment variable `FARMSTORM_WILD_APPLE_SOURCE`; no code change is needed (`:155`) |
| Someone brings a 32 × 32 or 128 × 128 image | allowed: after the pass-through the output size is decided by the source image; but **the display area is still 16 × 16**, so the gain is not proportional to the size (§2.4) |
| Atlasing | the model knows only `layer0`'s resource path; a change of texture size does not change the reference |
| The other 24 images are rewritten incidentally during a generator re-run | treated as an acceptance failure: this version may touch `wild_apple` only (§2.5) |

---

## 3. The farmland–core binding (R2 / R3)

**Grounds for this section: D-134, D-135, D-136, D-137, D-138, D-139, D-140.**

This is the largest design change of this version, and the only change that "replaces a whole set of data relations". Both bugs, R2 and R3, are cured at the root by this structural change, **not by fixing one criterion each**.

### 3.1 One-line positioning

The relation between farmland and cores moves from "**in range means managed**" to "**an explicit binding**": a plot belongs to **exactly 1** core at any one moment, and a core manages at most **16 plots**; "whose this plot is" is from now on a **stored fact**, no longer a runtime computation. The two bugs' symptoms are "demolition shovels it up along with the core" and "going offline loses protection"; their common cause is that 0.1.0 answered "whose this plot is" with "range + online players" — once the answer changes carrier, both symptoms lose their cause at the same time.

### 3.2 Entity relations (in words)

```
Player (UUID, may be offline)
  │  placement: writes ownerUuid + ownerName + placedGameTime
  ▼
Outpost Core (dimension + coordinate, one core per block entity)
  │  ── capacity 16 plots (outpost_core.farmlandBonus)
  │  ── 1 core : 0..16 plots of farmland
  ▼
Farmland (dimension + coordinate)
  · binding: only 0 or 1 core at any one moment (2 never exists; this is the fundamental difference from 0.1.0's range coverage)
  · owner: the person who hoed it (UUID); may be offline
  · no binding ≠ nonexistent: it can still be planted, it is simply unprotected
```

| Relation | Cardinality | What is stored | Judgment boundary |
|---|---|---|---|
| Player → core | 1 : 0..2 | the core's `ownerUuid` + `ownerName` | at most `outpost_core.maxPerPlayer` per player (default 2); 0.1.0's D-122 stays in force [28 §5] |
| Core → farmland | 1 : 0..16 | the binding table | the capacity is `outpost_core.farmlandBonus` (default 16); **no config key is added** (D-134) |
| Farmland → core | 1 : 0..1 | the binding table (looked up backwards) | 2 never exists (D-134) |
| Farmland → its owner | 1 : 1 | the ledger's `byOwner` | `byOwner` is no longer written to disk on its own; it is a derived view of the binding table (D-134 supplementary item) |
| Core → dimension | 1 : 1 | the coordinate carries its dimension | the overworld's `(0,0)` and the nether's `(0,0)` are two cores and do not overwrite each other (D-143, same direction) |

### 3.3 When a binding happens (the moment of hoeing, four steps)

| Step | Criterion | When it does not hold | Basis |
|---|---|---|---|
| 1 | the target cell can become farmland (grass block / dirt path / dirt + air above) | nothing happens (existing 0.1.0 behaviour, unchanged in this version) | `farmland/FarmlandGuard.java:55-63` |
| 2 | the target cell falls inside some **allied** core's 3 × 3 influence range (the range gate is kept) | a `NO_CORE` message, and **no durability is consumed** | 0.1.0's D-118 has not been revoked; D-135 supplementary item |
| 3 | candidate set = "the cores covering this cell" ∩ "allied" ∩ "still has a free slot"; take the **nearest** within the candidate set; a tie is broken by `placedGameTime` **ascending** → **dimension id** → the core coordinate `(x, y, z)` in **lexicographic order** (the three-level comparison key) | a `QUOTA_FULL` message (its meaning has changed, see §3.4) | D-135; the tie half-sentence is revised by **adjudication A-39** (three-level key) |
| 4 | write the binding + book it + place the farmland (all within the same tick) | — | D-135's landing position |

**The candidate set is filtered first, then the nearest is taken** ([author's supplement], a note on how to read it): D-135 writes "must cover" and "must have capacity" as preconditions on a candidate core, and writes "nearest" as the ordering rule; if the nearest were taken first and then checked for a free slot, "the nearest core is full, but another core covering this cell still has a free slot" would be rejected wrongly — and that directly contradicts D-139's total quota of `16 × number of cores` (the quota allows 32 plots while only 16 are actually granted). D-136's migration uses "migrate only when the three conditions hold at once + nearest first among several candidates", which is isomorphic to this.

**The tie criterion (adjudication A-39 revises D-135's "a tie goes by the earlier placement" half-sentence; the comparison key is three-level, 28's original text is not changed and A-39 prevails)**: compare in turn by `placedGameTime` ascending → **dimension id** (the string order of `minecraft:overworld` / `minecraft:the_nether` / `minecraft:the_end`) → the core coordinate `(x, y, z)` lexicographically. **Why three levels are necessary**: two cores in different dimensions may be placed in the same tick with exactly the same coordinate (the overworld's `(0, 64, 0)` and the nether's `(0, 64, 0)`), and a two-level key ties on exactly this cell; with the dimension id added, any two different cores can be ordered, the comparison key becomes a **total order**, and "a tie" never degenerates in the implementation into "pick one at random". **No Map's or list's insertion order may be relied on** — `OutpostCoreIndex.byOwner` is a `HashMap` (`outpost/OutpostCoreIndex.java:53`), and iteration order across owners is unstable; using it as the order would make "the same two cores, judged twice, bind to different cores". The three-level key gives the same answer on any two runs over the same save, and **that is the precondition of reproducibility**: legacy backfill, migration when a core is destroyed, and reproduction tests all require "the same world state ⇒ the same binding result", otherwise one save could end up with two different sets of ownership after two starts. `placedGameTime` is already on disk and can be re-checked; **no chunk is loaded just to compare candidate cores** (D-135 supplementary item).

### 3.4 The semantics of capacity 16

**Capacity and quota are two words, one source, different meanings** (D-134 supplementary item, RT-33):

| Word | What it is | What it is not | Basis |
|---|---|---|---|
| per-core capacity | the upper bound on the number of farmland plots one core can manage, the unit being "a plot of land"; it is a **binding allowance** | not a player quota, not an influence radius, not the old semantics of "+16 quota" | D-134 |
| quota | `16 × number of allied cores` | not `base + 16 × number of cores` (the concept of base has been deleted outright) | D-139 |
| used | the number of plots under the allied name in the binding table | not "all farmland in range" | D-139 supplementary item |
| the relation between the two | the quota is always equal to the sum of the cores' capacities | **not two independent gates** | [author's supplement: arithmetic + reasoning] |

**Why they are not two gates** ([author's supplement]): every plot must be bound to **one** core, and every core takes at most 16 ⇒ at any moment "the number of plots already bound under the allied name" ≤ Σ capacity = quota. So the "total quota" can never trigger before "per-core capacity"; the wall the player actually hits is always "every allied core covering this cell is full". D-139's expression still has to be written into the documents, because it is the **definition** of the quota and also the measure by which the overlay and the message text take their numbers, but it does not constitute a second runtime criterion.

Five worked examples (to nail the semantics down):

| Scenario | Cores | Quota | Per-core capacity | Actually tillable | Expression / note |
|---|---|---|---|---|---|
| single player, no core | 0 | **0** | — | **0** | 16 × 0; nobody can till at the start of a game, but the reason is no longer base = 0 (D-139 supplementary item) |
| single player, 1 core | 1 | **16** | 16 | **16** | 16 × 1 |
| single player, 2 cores whose ranges do not overlap | 2 | **32** | 16 each | **32** | 16 × 2 |
| single player, 2 cores but all the land is inside A's 3 × 3 | 2 | **32** | A 16 / B 16 | **16** | at the 17th plot B does not cover it ⇒ no candidate ⇒ rejected |
| the same cell covered by both A and B, with A full | 2 | 32 | — | still tillable | binds to B (§3.3 step 3's filter-then-take-nearest) |

### 3.5 Migration sequence (core destroyed → find a successor → turn to dirt if none)

| Step | Condition / moment | What is done | Does the world change |
|---|---|---|---|
| 1 | the core block is **really** removed (not a chunk unload, not a server stop) | take out the set of binding records under that core | no |
| 2 | for each record | decide whether the block is still farmland (when it cannot be read, degrade per §3.10, do not "skip") | no |
| 3 | search for a successor | three conditions hold at once: the influence range covers this farmland ∧ it is allied with the **farmland's owner** ∧ it still has a free slot; among several candidates **the nearest goes first**, and a tie is broken by `placedGameTime` ascending → dimension id → coordinate lexicographic order (three-level key, adjudication A-39) | no |
| 4a | there is a successor | **rebind**: write the new core; the block does not move, nothing drops, the farmland's owner is not notified | no |
| 4b | there is no successor and the block can be read | `setBlock(DIRT)` + remove the binding | **yes** (the only branch that touches the world) |
| 4c | there is no successor and the block cannot be read | remove the binding only; the world is untouched (degraded, see §3.10) | no |
| 5 | everything is processed | release that core's chunk ticket (D-145), remove the index entry, update the overlay list | no |

Three disciplines:

1. **Settle the whole set first, then touch the world**: following 0.1.0's existing reason (never modify the ledger while traversing it), which D-136's supplementary item explicitly retains.
2. **Only the farmland under this core is processed**, and the online players are no longer traversed — that traversal in 0.1.0 is exactly the other half of bug 2 (§3.7.2).
3. **"Turning to dirt" is the only world write**: no new drops, no compensation, no notification. When a migration succeeds, what the player sees is "the field changed owner", not "the field is gone".

### 3.6 Legacy-save backfill sequence (run once at startup)

| Step | Moment | What is done |
|---|---|---|
| 1 | server startup | read `world/data/farmstorm_farmland.dat`; the old format (only `byOwner`) goes through a backward-compatible read |
| 2 | loading complete | every old record becomes farmland that "has an owner and **no binding**" ⇒ semantically it is "awaiting backfill" |
| 3 | backfill | run **exactly the same** predicate as §3.5 step 3 on every unbound record (covers + allied + has a free slot, nearest first) |
| 4 | bound | write the binding; the world is untouched |
| 5 | cannot be bound | **stay farmland, stay unbound** (unprotected) — a legacy save is never shovelled up |
| 6 | backfill finished | no more backfill in this start; farmland tilled during the run goes through §3.3 |

Two readings that must be stated:

1. **The predicate reuse is this document's supplement**: adjudication #15 says only "backfill by the new rules", and of the new rules only #14 (D-136) gives a complete predicate. Reusing it is the only way to avoid inventing a third set of criteria; if the author wants the backfill to be looser (for example by alliance + proximity only, ignoring the range), what changes is D-137, not this document (already registered as item 3 of the reports made while 28 was being written).
2. **The backfill's traversal order decides the outcome on a legacy save, and therefore has to be a determinate order** ([author's supplement], tightened by adjudication A-39's prohibition; the sort key is `BlockPos`'s single-level total order per **A-47 rev.2**): capacity 16 is a hard cap, so "which old fields can be bound" is decided by the traversal order. The ledger's `HashMap` iteration order must not be used (unstable across owners, see §3.3): instead **first sort all unbound farmland by `BlockPos`'s total order (y → z → x), then process them one by one** — a ledger entry has only `pos` and no dimension field (`farmland/FarmlandLedger.java:46-48`), and one `pos` has only one entry in the ledger, so a single level is already a total order (A-47 rev.2). **This is not the same set as §3.3's three-level candidate-core key; the two are written differently on purpose, and it is not a typo**: a core's index entry carries a dimension (the `record Entry(ResourceLocation dimension, BlockPos pos)` at `outpost/OutpostCoreIndex.java:50`), and two cores in different dimensions can share a coordinate, so cores must first be ordered by dimension id; farmland in the ledger has only `pos`, and forcing a dimension level in would contradict the save's facts. The same save gives the same set of bindings on any two starts; and this order looks like a detail but in fact decides which fields on a legacy save **appear** unchanged after the upgrade and which become unprotected. The ledger's lack of a dimension field brings one more pre-existing boundary: **farmland at the same `BlockPos` in different dimensions cannot be distinguished**, and this version **does not fix it** (A-49), on the grounds that plan does not ask for it, a legacy save cannot retroactively recover the dimension, and the farmland side has already taken one save-version bump; where it lands is 30 §9's known limitations and 32's trap chapter.

### 3.7 Why the two bugs are cured at the root

#### 3.7.1 bug 1 (wrong settlement where several cores overlap): the causal chain

0.1.0's settlement criterion has three parts: ① the target cell is inside that core's 3 × 3 chunks; ② the block is still `minecraft:farmland`; ③ the owner is the core's owner himself **or his online teammate** (`blockentity/OutpostCoreBlockEntity.java:353-366`, `:369-379`).

1. **Not one of the three** asks "does this plot belong to **only** this core".
2. So when a plot lies inside two cores' 3 × 3 at once, A and B **each** satisfy the three.
3. When A is demolished, A counts this plot into its to-do set by its own 3 × 3 and `setBlock(DIRT)` lands once — under 0.1.0's criterion this is not "overreach"; every step A takes is legitimate.
4. Symptom: demolishing any one core shovels up the fields **belonging to the other core** in the overlap as well.

Under the binding regime, the settlement object = **the records under this core in the binding table**:

1. A plot has only 0 or 1 record, and the record says "this core".
2. When A is demolished, the set read out holds only the plots really bound to A; the plots bound to B **are not in A's records at all**.
3. So A has no legitimate path to touch B's plots. **It is not "an extra if checking the other cores" but "the source of the settlement object has changed from a range query to an ownership query"** — that gap is not filled; it no longer exists.
4. Symptom gone.

#### 3.7.2 bug 2 (the owner going offline loses protection): the causal chain

In 0.1.0 the entry point of the protection criterion is "**traverse the online players**": for every online player in `getPlayerList().getPlayers()` it asks "is this plot inside your (or your team's) core range and on your books" (`:125` of `farmland/FarmlandGuard.java:120-134`; `inOwnRange` at `:92-107` likewise starts from the online players).

1. The traversed set is **online players only**.
2. When the owners are all offline, he is not in the set, so nobody can answer "this is an allied field".
3. It returns "unprotected" — the class comment at `:114-115` states this known boundary itself.
4. Symptom: when the master goes offline, his fields get trampled; and when a core is demolished, those plots of his have no master to claim them.

Under the binding regime, the entry point of the **trample protection face** (A-45) becomes "**does this plot of farmland have a binding**":

1. Farmland → binding table: **a binding ⇒ protected**; no binding ⇒ unprotected. **There is no player link in this judgment chain** — the signature `protectsFromTrample(level, pos)` at `farmland/FarmlandGuard.java:120-134` never had an enquirer in it, and the trample event carries no "whose land" information either.
2. So it needs neither online players nor alliance resolution: the owner offline, the core's master offline, both having switched teams — this field still cannot be trampled.
3. **The other two faces** (the quota gate, the overlay) still go by "the core owner's UUID + **live** alliance" (D-138, C-9): they have an enquirer (who is hoeing, whose client this is), and a team switch changes their answers at once. The three faces no longer share one criterion, and A-45 is the narrowing of D-138's "the three faces share one" half-sentence.
4. Symptom gone.

**Two boundaries of this causal chain** (they must be written together, otherwise the reader will think "everything can be judged offline too"):

- **Trample protection has no "acting party" requirement**: whoever tramples, the fact "this plot has a binding" does not change, so whether he is allied and whether he is online do not affect the judgment; the quota gate and the overlay do have an acting party — the person hoeing, the owner of the client.
- Team resolution needs a name, while the ledger holds only UUIDs. This **affects the quota gate and the overlay only**: from which offline source the farmland owner's name is taken is not named by D-138 and is not invented here (the way of taking it is pinned down at the technical layer by 30); this document fixes only the criterion of those two faces: **the owner does not need to be online**.

#### 3.7.3 Why it is not "one if fixed each"

| Compared item | One patch each | This version's binding regime |
|---|---|---|
| how bug 1 is fixed | when demolishing a core, traverse the other cores and decide "is this plot still covered by someone else" | not needed: the settlement object does not contain other people's plots |
| how bug 2 is fixed | cache offline players + a team snapshot | not needed: the judgment starts from one stored fact |
| cost | O(number of cores × number of plots) per demolition; the team snapshot conflicts with C-9's live resolution | one backward lookup |
| how many copies the answer to "whose this plot is" has | two (the range computes one, the ledger computes one) | one (the binding table) |
| result | the two patches are unrelated, and either one left unwritten makes the bug recur | both symptoms lose their cause at once |

One concluding sentence: what this version replaces is **the carrier of the answer**, not **the number of criteria**.

### 3.8 Player-visible behaviour changes (against 0.1.0)

| # | Scenario | 0.1.0 | v0.1.1 | Basis |
|---|---|---|---|---|
| 1 | a plot in the 3 × 3 overlap of cores A and B, with A demolished | **turns to dirt** (bug) | if it is bound to A and B has a free slot ⇒ **rebound to B, still farmland**; if it was bound to B all along ⇒ completely untouched | D-134, D-136 |
| 2 | demolishing the only core covering it | turns to dirt | turns to dirt (the behaviour is unchanged when no successor is found) | D-136 |
| 3 | the core's master goes offline, the field is trampled | **trampled** (bug) | cannot be trampled | D-138 |
| 4 | a teammate online / offline | protection changes with online state | protection does not change with online state | D-138 |
| 5 | single player, default config, 0 cores | cannot till (reason: base = 0) | cannot till (reason: no core, no capacity) | D-139 supplementary item |
| 6 | single player, default config, 2 cores | at most 32 plots | at most 32 plots (the two cores added), but **16 each per core** | D-134, D-139 |
| 7 | 2 cores, but all the land is crowded into A's 3 × 3 | all 32 plots tillable | **only 16 plots tillable** (the 17th has no core to bind to) | D-134; §3.4 |
| 8 | wanting more quota when the quota is short | `/farmstorm farmlandlimit <n>` | the command is deleted; the only way is to place more cores | D-139, D-140 |
| 9 | the first start after upgrading | — | old fields get bindings backfilled per §3.6; the unbound ones stay where they are and are **merely unprotected** | D-137 |
| 10 | a legacy save with more fields than "16 per core × number of cores" (e.g. base had been raised by the command) | all protected | only those that can be bound are protected; the rest degrade to unprotected and are **not shovelled up** | D-137; §3.10 |

Row 7 is the one most easily taken by players as a bug in this change: the quota is enough, the land is inside an allied core's range, but **that core is full**. The message goes through the `QUOTA_FULL` one (language key `message.farmstorm.farmland.quota_full`), and whether the wording is renamed is decided by 30 (D-135 supplementary item).

### 3.9 Do-not-touch list

| Item | Basis |
|---|---|
| the 3 × 3 influence range and the gate "tilling is allowed only inside an allied core's range" | D-118 has not been revoked; D-135 supplementary item (the two gates stack) |
| the number +16 per core (only its semantics change, from "quota bonus" to "binding allowance") | D-134, D-139 |
| at most 2 cores per player | 28 §5 (D-122 retained) |
| no durability is consumed when tilling is refused | the part of 0.1.0's D-116 and D-118 that has not been superseded (28 §5 does not touch it; it stays in force) |
| the core's block properties (hardness 30 / blast resistance 1200 / sound STONE / not pushable) | D-141 adds light only; the rest is untouched |
| farmland already tilled is not retroactive | the part of 0.1.0's D-116 that has not been superseded (28 §5 superseded only the quota's carrier) |

### 3.10 Boundaries and exceptions

| Scenario | Rule | Basis |
|---|---|---|
| when a core is destroyed, the chunk holding one of its bound plots of farmland is **not in memory** | migration changes the ledger only and does not need to read the world, so **the rebind still works**; "turning to dirt" needs to read and write the block, so this one **degrades to removing the binding only** (the world is untouched, and that plot is unprotected from then on). The criterion is "do not touch the world when it cannot be proved", the same direction as 0.1.0's `isFarmlandLoaded` | D-136; [author's supplement] |
| when a core is destroyed that plot is no longer farmland (dug out / washed away / blown up) | skip it, do not touch the world, remove the binding (0.1.0's existing criterion is retained) | D-136; 0.1.0's existing criterion |
| a successor core exists but is already full | **turns to dirt**, not "wait until it has a free slot and then migrate" | D-136 |
| the successor core covers the plot but is not allied with the farmland's owner | **turns to dirt** | D-136 |
| after the binding, the core's master switches teams / the farmland's owner switches teams | the binding **does not migrate** (what is bound is the core's coordinate); **trample protection is unchanged** — it depends only on whether that plot of farmland has a binding, and has nothing to do with teams; **the quota gate and the overlay** are recomputed by live allied status ⇒ a team switch changes those two faces' answers at once, but the block itself does not move | D-138; A-45; C-9 |
| a field whose backfill failed on a legacy save and which has not been dug out since | it does not recover by itself: the backfill runs once at startup only, and it is already farmland and cannot be "hoed once more" ⇒ restoring protection can only wait for the next startup (or dig it out and till it again, going through §3.3) | D-137; [author's supplement] |
| two teammates each hoe their own land inside the same core's range | the two plots are each bound independently, but **share the same core's 16 slots** ⇒ the 17th combined is rejected | D-134 |

---

## 4. Outpost Core light level 15 (R4)

**Grounds for this section: D-141.**

### 4.1 One-line positioning

At registration `outpost_core` gains `.lightLevel(state -> 15)`. This is the only pure block-property change in this version: it touches no number table, carries no config key, and does not change the block entity.

### 4.2 The change itself

| Property | Before | After | Basis |
|---|---|---|---|
| block light | no `lightLevel` (value 0) | **15** | D-141 |
| `MapColor` / hardness / blast resistance / sound / `pushReaction` / `requiresCorrectToolForDrops` | `MapColor.STONE` / 30.0F / 1200.0F / `STONE` / `BLOCK` / yes | **untouched character for character** | `registry/FarmstormBlocks.java:82-89` |

### 4.3 The knock-on list

| # | Effect | How it shows | Basis |
|---|---|---|---|
| 1 | **no mob can ever spawn** at that position | the core's own cell and its neighbourhood where the light level has not decayed to 0 do not satisfy the darkness condition for hostile mobs | RT-36; D-141 supplementary item |
| 2 | **the light level propagates 15 blocks** | the level drops by 1 per block, and going from 15 to 0 takes **15** blocks (15 − 0 = 15, [author's supplement] arithmetic); horizontal and vertical distances are the same; an opaque block in the way blocks it (the vanilla lighting rule, unchanged in this version) | author's supplement |
| 3 | the **look** of the boundary particles | the boundary particles are `minecraft:dust`, whose colour is the final colour and which takes no part in block-light illumination; once the ground around the core is lit up, the brightness contrast between the particles and the ground drops ⇒ the boundary line is fainter in daylight or in a bright place | [author's supplement: an inference about the look]; the particle parameters are unchanged (D-141 states explicitly that only the light level changes) |
| 4 | farm lighting | the brightness of the ground around the core is maxed out ⇒ the light threshold this mod's crops need in order to grow (0.1.0's existing criterion, unchanged in this version) is automatically met inside the radius, and the ground beside the core can grow crops at night too | D-141 supplementary item |
| 5 | snow layers and ice | by the vanilla rule, snow layers and ice in a brightly lit place melt. This is the vanilla block's pre-existing reaction to a light source, **not a rule this mod adds** | [author's supplement] |
| 6 | not affected | block entity behaviour, the boundary particles' count / interval / colour, the farmland judgment, the overlay's data, mining time | D-141 supplementary item |

Items 1 and 2 are two faces of the same thing: never spawning comes from "a high light level", and propagating 15 blocks comes from "the level decays per block". Both are written because the player meets them separately ("no mobs beside the core" and "the core lights up a cave for 15 blocks").

### 4.4 Why it is not made a config

What the author gives is a **fixed block property**, not a tunable gameplay value. Making it a config would equal inventing a requirement, and would also turn "the same core is 15 on someone else's server and 0 here" into a table that needs explaining (D-141's supplementary item has already rejected that option). By the same token there is no state machine such as "redstone can switch the light off" — that would require changing block states and light updates, two orders of magnitude away from "one line of properties".

### 4.5 Boundaries and exceptions

| Scenario | Rule | Basis / supplement |
|---|---|---|
| two cores next to each other (gap < 15 blocks) | the light takes the maximum and **does not add up**: both places are 15, and the region between them still follows the "drops 1 per block" curve | [author's supplement: vanilla light propagates by maximum level] |
| the core sits right against a natural cave | the cave brightens within 15 blocks, and mobs move to spawn in the dark beyond 15 blocks — this is a spatial change the player will see, not a defect | D-141 notes; [author's supplement] |
| the core is demolished | the light falls back as the block disappears (the vanilla light update); nothing is left behind | vanilla light-update behaviour |
| the chunk holding the core unloads | the light data is saved with the chunk's data, and reloading does not need a recomputation (vanilla lighting-system behaviour) | vanilla lighting-system behaviour |

---

## 5. The top-left HUD overlay (R5)

**Grounds for this section: D-142, D-143, D-144.**

This is the second-largest item of the version, and **the only requirement that drove a red line to be broken**: showing "all of one's own and one's teammates' cores, including unloaded chunks and other dimensions" needs data the client does not hold, so 1 server → client read-only packet has to be added (D-142, D-146).

### 5.1 One-line positioning

A self-drawn list in the screen's **top-left** corner, one core per line, ordered by placement, at most 8 core lines + 1 overflow line; it can be switched on and off, positioned by dragging, its position is written into the CLIENT config and survives rejoining the game; the display scope is **all of one's own and one's teammates' cores** — including unloaded chunks and other dimensions.

### 5.2 Display scope

| Item | Conclusion | Basis |
|---|---|---|
| who is shown | all of one's own and one's teammates' cores | D-143 item 1 |
| ally judgment | **live allied status** (the core owner himself + same-team allies, judged by UUID / name, regardless of whether they are online) — the same set as the quota gate; **the trample protection face ignores teams** (A-45) | D-143 item 1; D-138; A-45 |
| includes unloaded chunks | **yes**: the list comes from a full server-side snapshot and has nothing to do with whether a chunk is loaded | D-143 |
| includes other dimensions | **yes**: full across dimensions, without filtering by dimension | D-143 |
| what is not shown | enemy cores, core items, cores already demolished | D-143 supplementary item |

The "cross-dimension" item is not "by the way": it is precisely the reason D-142's packet exists. If only the current dimension were shown, the client could assemble it from loaded chunks and no packet would be needed; it is exactly because cores **beyond view distance and across dimensions** must be shown that the server has to push a full list (tombstone §6 items 3 and 4).

### 5.3 Content format

One core per line, in a fixed format:

```
#n [dimension path] (x, y, z) owner
```

| Field | Value | Explanation |
|---|---|---|
| `#n` | a decimal sequence number starting from 1 | n = the core's position in the **sorted result** (§5.4); players can use it to call out a target ("that #2 core") [author's supplement] |
| `[dimension path]` | the dimension id's **path**: `overworld` / `the_nether` / `the_end` | **no language key is added** (card supplement); it distinguishes the case where the same coordinate holds one core in each of two dimensions (D-143 supplementary item) |
| `(x, y, z)` | block coordinates as integers, with a minus sign for negatives | shown as-is, without abbreviation and without conversion to chunks |
| owner | oneself → one language key (displayed as "self"); a teammate → the real name from `ownerNames` | a teammate has a name while offline too (D-142 supplementary item) |

Example lines (three ownership cases, three dimensions):

```
#1 [overworld] (-128, 64, 1024) self
#2 [the_nether] (32, 70, -8) Amai
#3 [the_end] (0, 65, 0) Amai
```

### 5.4 Ordering

| Item | Conclusion | Basis |
|---|---|---|
| primary key | `placedGameTime` **ascending** (the earlier placed is above) | D-143 item 3 |
| across dimensions | **one unified ordering**: the overworld's and the nether's cores compare their times on the same axis, not grouped, not segmented by dimension | D-143 item 3 |
| a tie (placed in the same tick) | two more levels after the primary key: **dimension id** → the core coordinate `(x, y, z)` in lexicographic order (three levels in total) — the same criterion as §3.3's tie among candidates | adjudication A-39 |
| the player's own dimension | **does not affect the ordering**, nor has it anything to do with the order in which the lines are shown | D-143 |

The ordering is carried by **the list's order**: the list pushed from the server is already sorted, and the client does not sort again (D-142 supplementary item). That way the "ordering rule" has one implementation only, and the client is responsible only for drawing.

### 5.5 The 8-line cap and "+N"

| Item | Conclusion | Basis |
|---|---|---|
| cap on core lines | **8 lines** | [card supplement] (noted under the table in fact card §2, vetoable) |
| overflow line | line 9 draws `+N`, N = the total − 8 | [card supplement] |
| the `+N` text | the literal `+` immediately followed by a decimal number (e.g. `+3`), with no unit and no counter word | [author's supplement: 28's literal is "+N"; adding a word would equal adding a language key, the same direction as D-143's "no language key is added"] |
| whether `+N` counts inside the 8 lines | **no**, it is an overflow line drawn outside the 8 core lines ⇒ the overlay is at most **9** lines high | [author's supplement: D-143 item 4's "show at most 8 lines" and item 2's "one line per core" stand in the same sentence, and 8 is the cap on core lines; otherwise with exactly 9 cores the `+N` would be invisible] |
| the colour of `+N` | grey (to tell it apart from "self" green and "teammate" white) | [author's supplement] |
| interaction | not clickable, not scrollable, not pageable, not expandable | [author's supplement: D-143 gives no interaction at all; the overlay is a read-only list] |

### 5.6 Geometry and colours

| Item | Value | Basis |
|---|---|---|
| default coordinate | top-left `(4, 4)` (`hud.x` = 4, `hud.y` = 4) | [card supplement]; D-144 item 3 |
| row height | **9** GUI px (the vanilla font's row height) | [author's supplement: D-143's supplementary item hands font size / line spacing to 29] |
| row spacing | **1** GUI px (drawn between rows only) | [author's supplement] |
| padding | **2** GUI px on each of the four sides | [author's supplement] |
| total height (full 9 rows) | **93** GUI px = `9 × 9 + 8 × 1 + 2 × 2` | [author's supplement: arithmetic] |
| total height (1 row only) | **13** GUI px = `9 + 2 × 2` | [author's supplement: arithmetic] |
| width | the measured `font.width` of the longest line + `2 × 2` padding; no hard-coded constant | [author's supplement] |
| order of magnitude of the width | a line of common content (e.g. `#1 [overworld] (-128, 64, 1024) 自己`) is about **214** px (estimated at 6 px per ASCII character and 9 px per han character: 32 ASCII × 6 + 2 han × 9 = 192 + 18 = 210, plus 4 for padding = 214) | [author's supplement: an estimate, used only to show that the width is controllable; the clamp uses the measured value] |
| text colour (self) | **green**, `0xFF55FF55` | [card supplement: self green, teammates white] + [author's supplement: the concrete ARGB, the same technique as `WildHunterHud`'s self-drawn `fill`] |
| text colour (teammates) | **white**, `0xFFFFFFFF` | as above |
| text colour (`+N`) | grey, `0xFFAAAAAA` | [author's supplement] |
| background | semi-transparent black `0x80000000` (so that it stays legible on grass, sky and water) | [author's supplement] |
| border | 1 px black `0xFF000000` | [author's supplement: the same technique as the charging bar's outline] |

**The single measure (adjudication A-41)**: this section's **row height 9 / row spacing 1 / padding 2** is the single measure; the panel height `10n + 3` in 30's technical spec (n = the number of rows, 83 px for 8 rows, 93 px for 9 rows) is its **equivalent corollary**, not a second set of parameters. If downstream sees both spellings at once, the three-number decomposition in this section prevails — 9 is the vanilla font's row height, 1 is the row spacing, 2 is the padding, and each of the three can be verified on its own; `10n + 3` merely folds them into one expression: `9n + (n − 1) + 4 = 10n + 3`.

Three notes: ① the semi-transparent background is not an aesthetic choice but a **legibility criterion** — white text laid straight onto snow or clouds disappears; ② the semantics of green / white are "self / teammates", independent of order and independent of dimension; ③ the width is computed from measured values, so the same line can be a different width on different players' screens (the names differ in length), and **the clamp must use the currently measured width**.

### 5.7 The interaction sequence for switching and positioning

| Step | Player action | Client state | Persistence | Basis |
|---|---|---|---|---|
| 1 | press the "toggle overlay" key | `hud.enabled` is inverted and takes effect on the next draw | writes `farmstorm-client.toml` | D-144 items 1 and 3 |
| 2 | press the "drag" key | enters drag mode: a Screen opens (single-player pauses automatically) and the overlay keeps drawing in its **edit state** | nothing written | D-144 item 4; D-143 supplementary item |
| 3 | press the left button | record the grab offset = cursor − (`hud.x`, `hud.y`) and start dragging | nothing written | [author's supplement] |
| 4 | drag | preview coordinate = clamp(cursor − offset); the clamp expression is below | nothing written (preview in memory only) | [author's supplement: D-144's supplementary item hands the expression to 29] |
| 5 | release | stop at the preview coordinate, **without leaving** drag mode (dragging can continue) | nothing written | [author's supplement] |
| 6 | press `ESC` | write the preview coordinate into `hud.x` / `hud.y`, close the Screen and return to the normal state | writes the CLIENT config | D-144 item 4 |
| 7 | quit the game → rejoin | the client reads the CLIENT config at startup and the overlay appears at the saved position | reads | D-144 item 3 |

Two details on the clamp and the exception:

1. **The clamp expression** ([author's supplement]): `x ∈ [0, guiWidth − width]`, `y ∈ [0, guiHeight − height]`, where "width / height" are the currently measured overlay dimensions. Going partly off screen is not allowed, and neither are negative coordinates (`hud.x` / `hud.y`'s int range holds negatives, but both the preview and the save must be pressed inside the screen).
2. **In drag mode `ESC` only saves and exits**: the save hangs on the Screen's close flow, so "save" and "leave drag mode" are the same action; while a Screen is open the vanilla game would not bring up the pause menu anyway (the pause menu is opened only when there is no Screen), so no extra interception is needed. Otherwise the save and another action would fight over the same keypress, and the player would think the save had failed.

**The overlay must still be visible while dragging** (D-144 supplementary item): in the preconditions of `WildHunterHud.java:46`, a `screen != null` suppresses the whole draw, and drag mode itself is a Screen. So **drag mode is the only exception**: the two early returns on `options.hideGui` and `screen != null` do not take effect in the edit state, otherwise the player could not see what he is dragging. This is a technical necessity, not a design preference.

The client command and the keys coexist (D-144 items 1 and 2):

| Entry point | Effect | Basis |
|---|---|---|
| the "toggle overlay" key | equivalent to `/farmstorm hud on` / `off` | D-144 items 1 and 2 |
| the "drag" key | equivalent to "enter drag mode" | D-144 items 1 and 4 |
| `/farmstorm hud on` / `off` | the same switch | D-144 item 2 |
| `/farmstorm hud reset` | the position returns to the default `(4, 4)`; **the on/off state is unchanged** | [author's supplement: D-144 gives only three sub-commands and does not say whether reset also changes enabled; if reset turned the overlay on along the way, "I switched it off, a reset and it is back" would become a surprise] |

### 5.8 Relation to the existing HUD

| Element | Position | Would it collide | Basis |
|---|---|---|---|
| the overlay | expands downwards from `(4, 4)`; with full rows its bottom edge is at y = 97 | — | [author's supplement: 4 + 93] |
| `WildHunterHud`'s charging bar | anchored at the bottom: `y = screenHeight − 52`, occupying the 4 px band at bottom-offset **[48, 52)**, including the outline **[47, 53)**; horizontally centred, total width 44 | **no**: the vertical clearance = `(guiHeight − 53) − 97 = guiHeight − 150`; at `guiHeight` = 240 the clearance is **90** px | `client/WildHunterHud.java:27`, `:55`; [author's supplement: arithmetic] |
| the vanilla hotbar / health / hunger / experience | 0 ~ 39 from the bottom (the class comment at `client/WildHunterHud.java:14-16` records these three row bands one by one) | no (the overlay is at the top) | `client/WildHunterHud.java:14-16` |
| the vanilla boss bar / scoreboard | top centre / right side | **no avoidance is done** (D-144 item 5); when they overlap the player drags it away | D-144 |

**Conclusion**: one is anchored at the top and the other at the bottom, and as long as `guiHeight ≥ 150` they **never** intersect (`97 + 53 = 150`, [author's supplement] arithmetic); common resolutions are far above that line (1080p at GUI scale 4 ⇒ `guiHeight` = 270, a clearance of 120 px). So the overlay's default position **does not need** to give way to the charging bar, and no cross-check is needed at draw time either.

### 5.9 The data face (read-only, used for drawing only)

| Item | Conclusion | Basis |
|---|---|---|
| number of packets | **1**, server → client, one-way | D-142 |
| client behaviour | read-only, sends nothing back, makes no "is this allied" judgment at all | D-142 |
| list content | for each core: "dimension + coordinate + owner's display name + whether it is oneself", in the ordering result's order | D-142 supplementary item |
| registry surface | **does not enter** the registry surface (+0): the payload goes through `RegisterPayloadHandlersEvent` and not into `BuiltInRegistries` | fact card §3.1 |
| red line | from this version on, "0 custom packets" is **abolished outright**, and only "no Mixin" is kept; this version happens to add 1, but **sets no upper limit** | D-146 |
| push timing | placement / destruction / migration, and player login / dimension change; the exact set of occasions and the debouncing are decided by 30 | D-142 supplementary item |
| difference from forced loading | forced loading (D-145) solves the **server-side** need for a block entity to tick; it **does not** solve the client's inability to see cores beyond view distance. The two are not interchangeable | D-145 supplementary item; tombstone §6 item 4 |

### 5.10 Boundary list (R5 from the local point of view)

| Scenario | What the player sees | Basis |
|---|---|---|
| no core at all | **not drawn**: 0 rows ⇒ the whole overlay does not appear, and neither does `+N`. Even then the toggle key still flips `hud.enabled`; there is simply no change on screen | [author's supplement: an empty frame carries no information, and the default position would occupy the top-left corner for a long time] |
| only 1 core | 1 line, no `+N`, the overlay is 13 px high | §5.5, §5.6 |
| a teammate's core, the teammate offline | shown as usual (the name comes from `ownerNames` and exists offline too) | D-142 supplementary item |
| switching dimension | the list does not change, the order does not change; the dimension tag at the front of each line is still **that core's own** dimension | D-143 |
| reconnecting / rejoining the server | the server pushes the full list at login; the client does not cache the previous session's list ⇒ the first frame just after joining is empty | D-142 supplementary item + [author's supplement] |
| more than 8 cores | 8 core lines + 1 `+N` line | [card supplement] |
| dragged off screen | clamped inside the screen; "dragged it away and cannot find it again" is impossible | [author's supplement: the clamp expression] |
| window size / GUI scale changes | clamp once more and press the coordinate back inside the bounds | [author's supplement] |
| opening the inventory / chat / pressing F1 | not drawn; **drag mode is the exception** (in the edit state neither early return takes effect) | `WildHunterHud.java:46`; D-144 supplementary item |

### 5.11 Do-not-touch list

| Item | Basis |
|---|---|
| any parameter of the charging bar `WildHunterHud` | this version does not touch 0.1.0's HUD (D-103's 52 is kept) |
| the vanilla boss bar / scoreboard / chat column | not taken over, not hidden, not moved |
| the boundary particles' count / interval / colour | D-141 changes the light level only; D-121's particle parameters are untouched |
| the client's `/farmstorm hud` and the server's commands | the server's `/farmstorm farmlandlimit` is deleted with its whole tree (D-140); the client's is a separate registration, and the two do not share a registration point |

---

## 6. Compressed Hay Block sound (R6)

**Grounds for this section: D-130.**

### 6.1 One-line positioning

`compressed_hay_block`'s `SoundType` changes from `GRASS` to **`STONE`**. One line of properties, but five classes of sound change together, and it is the only landing point of plan's item 3.

### 6.2 What changes

| Item | Before | After | Basis |
|---|---|---|---|
| `SoundType` | `GRASS` | **`STONE`** | D-130; `registry/FarmstormBlocks.java:79` |
| hardness / blast resistance | 30.0F / 6.0F | untouched | `:77` |
| `requiresCorrectToolForDrops()` | yes | untouched | `:78` |
| `mapColor` | `COLOR_BROWN` | untouched | `:76` |

### 6.3 Knock-on: five classes of sound change together

`SoundType` is a **block property**, and one replacement changes the whole set:

| # | Sound | When it plays | After the change |
|---|---|---|---|
| 1 | break sound | the block is dug out | the stone family |
| 2 | place sound | this block is placed | the stone family |
| 3 | step sound | a player / mob steps on it | the stone family |
| 4 | hit sound | every blow while mining | the stone family |
| 5 | fall sound | an entity lands on it | the stone family |

**28's D-130 supplementary item says "three classes" (break / place / step)**, while `SoundType` actually provides five; this document writes five, and the acceptance checklist goes by five. The difference does not change the conclusion (all of them swap the sound only), but it does change the number of acceptance items.

### 6.4 The four things not affected

| Item | Why it is not affected |
|---|---|
| mining time | decided by hardness 30, unrelated to `SoundType` |
| drops | decided by the loot table and `requiresCorrectToolForDrops()` |
| blast resistance 6 | an independent property |
| the block's feel (apart from sound) | the sound affects hearing only; every source that generates the block in the world (a player placing it, a projectile landing into it) shares the same set of sounds and is not distinguished by source |

### 6.5 Relation to the 0.1.0 documents

The D-57 row in `22-DECISIONS-0.1.0.md` §7.2 says "`sound(GRASS)` unchanged". This version **revokes that half-sentence** (registered in 28 §3.2 item 11), and 22's original text is frozen and not changed. The rest of D-57 (hardness following ancient debris 30.0, resistance following cobblestone 6.0, the same material on all six faces) stays in force.

### 6.6 Why not `CROP`

`CROP` is the sound of crop blocks (`registry/FarmstormBlocks.java:100`, `:110`), the exact opposite of "the stone family". Across the whole repository `SoundType` takes only three values: `GRASS`, `STONE`, `CROP`; the only precedent for `STONE` is `outpost_core` (`:88`), so choosing it both meets the requirement and has a precedent.

### 6.7 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| the sound changed — does the feel become "lighter" | no: hardness 30 is unchanged, the mining time and the "will not budge" impression stay consistent; only what is heard changes |
| confusion with `outpost_core` | both blocks now have a stone sound; the differences are blast resistance (6 versus 1200) and light level (0 versus 15), so players will not take them for the same material |
| blocks in an old save | the sound is a block property and is not written into the save ⇒ it takes effect in place after the upgrade, with no migration needed |

---

## 7. Pumpkin Turret numbers and tracking (R7 / R8)

**Grounds for this section: D-127, D-128, D-129.**

In this document `man瓜` always means **`farmstorm:pumpkin_turret`** (the item / block / block entity share one id). The other three objects whose names contain "melon" — `melon_grenade` (Melon Grenade), `melon_slice_bomb` (Melon Slice Bomb), `melon_grenade_stem` (the Melon Grenade stem) — are not the Pumpkin Turret (D-127).

### 7.1 One-line positioning

Three lines of numbers change together: firing interval **60 → 40 ticks**, damage per shot **1 → 3**, bullet speed **0.6 → 0.45** (promoted to a config key). R8's "tracking" adds no tracking mechanism at all — **0.1.0's bullet already re-aims every tick**; what has to be fixed is its overshoot jitter at point-blank range.

### 7.2 The number table

| # | Key / position | Before | After | Basis |
|---|---|---|---|---|
| 1 | `pumpkin_turret.fireIntervalTicks` | 60 ticks (3 seconds) | **40 ticks (2 seconds)** | D-127; `config/FarmstormConfig.java:431-433` |
| 2 | `pumpkin_turret.projectileDamage` | 1 point | **3 points** | D-128; `:425-427` |
| 3 | `pumpkin_turret.bulletSpeed` (**new key**, double) | 0.6 (the hard-coded constant `TurretBullet.SPEED`) | **0.45** (= 9 blocks per second) | D-129; `entity/TurretBullet.java:28` |
| 4 | `pumpkin_turret.knockbackLevel` | 1 | **untouched** | D-128; `:428-430` |
| 5 | `pumpkin_turret.targetRadius` | 15 (Chebyshev) | **untouched** | `:422-424` |
| 6 | damage type | `farmstorm:void_bolt` | **untouched** | `registry/FarmstormDamageTypes.java:20` |

The three numbers have to be read together (D-128 supplementary item): the theoretical output converted per tick is **4.5 times** (`(3 ÷ 40) ÷ (1 ÷ 60) = 4.5`). Changing only the damage would make people think it had been strengthened 3 times, and changing only the interval would make them think 1.5 times.

### 7.3 Where the close-range circling / jitter comes from

0.1.0's tracking implementation is "reset the velocity vector every tick to point at the target's centre × a fixed step" (`entity/TurretBullet.java:73-87`, `:90-96`):

1. **Look straight at it every tick**: `steerTowards()` recomputes the direction each tick from `target hitbox centre − own position`, with no turn-angle cap and no maximum chase duration.
2. **A fixed step**: the speed's magnitude is always `SPEED` (0.6 blocks/tick), regardless of the remaining distance.
3. **Overshoot**: when `dist < step` (0.6 blocks), the distance covered in one step is **greater than** the remaining distance ⇒ the bullet lands on the target's other side.
4. **Turning round again and again**: on the next tick the target is behind it ⇒ the direction reverses ⇒ it overshoots again ⇒ back and forth every tick, and the picture is "circling / jittering at point-blank range".
5. **Never hits**: the hit decision is left to the vanilla `move` flow (this class does no ray casting); if a per-tick displacement crosses the target's hitbox and both samples land outside the box, `onHitEntity` never fires ⇒ the closer it is, the more certainly it misses.

This is also why "slowing it down" and "converging" have to be done together: slowing down alone moves the overshoot threshold from 0.6 to 0.45 and the problem remains; fixing the convergence alone without changing the speed leaves the same step at normal distances.

**One thing worth noting in passing** (Appendix A expands it): `plan0.1.1.txt:27` asks for "add a tracking feature", and that capability **already exists** in 0.1.0's code — `PumpkinTurret` hands the target to the bullet at launch (`behavior/impl/PumpkinTurret.java:74`), and the bullet re-aims at the target every tick from then on. What is really missing is not tracking but "converging as it gets close".

### 7.4 Behaviour after the fix

| Case | Behaviour after the change |
|---|---|
| `dist ≥ step` | exactly as now: re-aim every tick, constant speed magnitude |
| `dist < step` | **converges by the remaining distance**: this step's displacement is the remaining distance (or, equivalently, a hit is declared directly), and the target is no longer crossed |
| the target dies / disappears | as now: `target` is nulled, the bullet flies straight on at its last velocity and disappears when it hits a block |
| hitting a block | as now: `discard()` straight away, the block is not blown up |

Three "unchanged" points must be stated clearly, otherwise it will be read as "a new mechanism was added":

1. **It still corrects its direction every tick** — not soft tracking (no turn-angle cap, no maximum chase duration, no guidance parameters, no change of entity superclass, D-129 item 3).
2. **It is still server-authoritative** — tracking is done on the server only, and the client relies on entity position synchronisation (HC-03).
3. **It still flies in a straight line** — convergence happens only in the last short stretch (`dist < step`), and the trajectory's shape at medium and long range is unchanged.

### 7.5 A feel comparison

| Item | 0.1.0 | v0.1.1 | Expression / basis |
|---|---|---|---|
| firing interval | 60 ticks (3 seconds) | **40 ticks (2 seconds)** | D-127; 40 ÷ 20 = 2 seconds ([author's supplement] arithmetic) |
| damage per shot | 1 point | **3 points** | D-128 |
| bullet speed | 0.6 blocks/tick (12 blocks/second) | **0.45 blocks/tick (9 blocks/second)** | D-129; 0.45 × 20 = 9 (given by D-129) |
| shortest flight time at the maximum lock distance | 25 ticks (15 ÷ 0.6) | **33.3 ticks** (15 ÷ 0.45) | [author's supplement] arithmetic; the 15 comes from `targetRadius`'s default |
| theoretical output | 1 ÷ 60 points/tick | 3 ÷ 40 points/tick | **4.5 times** (D-128 supplementary item) |
| point-blank hit | overshoot, jitter, usually misses | converges, hits | D-129 |

"The flight time becomes longer" is not a side effect but the requirement: what the author wants is **a lower bullet speed**, so that the player can see the bullet coming. 33.3 ticks is the shortest path of an ideal straight line (in practice the direction is corrected every tick and the path is slightly longer); what the magnitude means is "from 1.25 seconds to 1.7 seconds".

### 7.6 Does it still always hit

**It does not always hit, but the hit rate can only go up, not down.** Four counter-examples still hold:

| Counter-example | Why it still misses |
|---|---|
| the target dies / disappears while the bullet is in flight | `target` is nulled and the bullet flies straight on in its last direction |
| blocks stand between the target and the bullet | hitting a block means `discard()` |
| the target teleports (enderman, teleport, `/tp`) | the direction is recomputed every tick, but one step is only 0.45 blocks and cannot keep up with a teleport |
| the chunk the bullet is in unloads | the entity does not tick |

Conversely, **the point-blank class of "should have hit but did not" is eliminated** — not by raising the hit rate but by removing the overshoot. So this version's attitude to "always hits" is: **it does not promise to always hit, it promises only to stop always missing because of overshoot** (D-129 supplementary item).

### 7.7 Do-not-touch list

| Item | Basis |
|---|---|
| the targeting rules (players first, excluding spectator / creative / allied, `Monster` taken when near, intercepting projectiles) | D-127 supplementary item; `behavior/impl/PumpkinTurret.java:85-118` |
| the damage type `farmstorm:void_bolt`, knockback level 1, the advancement `turret_hit` | D-128 |
| the scan interval of 10 ticks | `behavior/TurretBehavior.java:13-16` |
| the bullet's `setNoGravity(true)`, its disappearance on hitting a block, its clearing of the target on load | `entity/TurretBullet.java:43-51`, `:121-128`, `:138-143` |
| the four things **not introduced** with soft tracking: a turn-angle cap, a maximum chase duration, guidance parameters, a change of entity superclass | D-129 item 3; tombstone §6 item 8 |

### 7.8 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| the server owner already has a `farmstorm-server.toml` on disk | it still holds the old values 60 / 1: **the code does no migration**, and to take effect the old toml must be deleted or edited by hand (D-147) |
| the new key `bulletSpeed` | the config system adds it to the old toml automatically; no hand-adding is needed (D-147 supplementary item) |
| the target is at point-blank range from the turret (`dist < 0.45`) | converges and hits (§7.4) |
| the target is at the edge of the range and walks out halfway through the bullet's flight | tracking does not look at the range; `targetRadius` is judged only at the moment of firing, and the bullet keeps chasing |
| two turrets lock the same target at once | the two shots settle independently and do not affect each other |
| the speed is set to 0 by the server owner | the bullet hovers in place and no longer advances — this is the extreme the config's value range allows; the range is decided by 30, and this version designs no gameplay branch for it |

---

## 8. The Friend Chives persistent field (R9 / R10)

**Grounds for this section: D-131, D-132, D-133.**

### 8.1 One-line positioning

After landing, the projectile is no longer "settle once and disappear" but becomes a **field lasting 200 ticks**: every tick it scans a sphere of radius 6 blocks at the landing point and applies **`200 − x`** ticks of Q-Bounce to every `LivingEntity` inside (x = the number of ticks elapsed since landing); on tick 200 it `discard()`s itself. The sound is **still judged once, at the moment of landing** (R10).

### 8.2 The field's timeline

"The field's length" = `friend_chives.effectTicks` (default **200**, value range 20 ~ 1200); `x` counts from the instant of landing, and at that moment **x = 0**.

| x (ticks after landing) | What the field is doing | How long this batch of new victims gets | What those already inside have left |
|---|---|---|---|
| **0** | landing: judge the sound (played once if there is ≥ 1 in the sphere), and apply `200 − 0` ticks to every `LivingEntity` in the sphere | **200 ticks** | 200 |
| 1 | the 1st `tick()` rescans the sphere | 199 ticks | 199 |
| k | the k-th scan (1 ≤ k ≤ 199) | `200 − k` ticks | `200 − k` |
| **199** | the last scan | **1 tick** | 1 |
| **200** | no more application; the entity `discard()`s | not applied | **0: everything expires, in the same tick** |

Four points to take from the table:

1. **The expiry moment is uniform**: every victim receives "the whole window minus the part already elapsed", so no matter which tick he enters on, **he expires at x = 200**. That is the entire meaning of the `200 − x` spelling.
2. **Idempotence**: those already inside are rewritten to `200 − k` every tick, and they have exactly `200 − k` left ⇒ the value does not change, neither extended nor shortened (a vanilla effect decreases by 1 every tick).
3. **Not one batch is missed**: the batch at x = 0 is handled by the landing callback, and `tick()` takes over from x = 1; the two do not overlap and do not apply twice.
4. **No application on tick 200**: applying 0 ticks is meaningless (vanilla refuses it). So the field's application window is **[0, 199]**, 200 ticks in total, and tick 200 is responsible only for self-destruction.

If the server owner sets `effectTicks` to 100, the field lasts 100 ticks and applies `100 − x` every tick ([author's supplement]: the field's length and the amount applied share one source, and `200` is the default rather than a new constant; otherwise setting the config to 100 would produce the contradiction "the field lasts 200 ticks, but everyone gets only 100 − x ticks").

### 8.3 Why it is `200 − x`

| If it were written as | What would happen |
|---|---|
| a fixed 200 ticks | from tick 2 on, every application pushes the remaining duration back to 200 ⇒ **perpetual renewal**, and a person standing in the sphere bounces for ever |
| a fixed 200 ticks, applied only at landing | back to 0.1.0's behaviour, and those who enter midway are unaffected |
| `200 − the accumulated count` each time | equivalent to `200 − x` (the accumulated count is x) |
| `200 − x` | each person's remaining duration differs, but **the expiry moment is the same** |

### 8.4 Sound judgment (R10)

| Item | Conclusion | Basis |
|---|---|---|
| when it is judged | **the moment of landing** (x = 0); not judged afterwards | D-132 |
| how many times it plays | **once** | D-132 |
| the condition for playing it | ≥ 1 living entity inside the sphere at that moment | D-132 |
| an empty drop | **never plays** | D-132 |
| later entrants | **no catch-up play** | D-132 |
| the sound | `farmstorm:friend_chives`, `SoundSource.PLAYERS`, volume **2.0**, pitch 1.0 | `entity/thrown/FriendChivesProjectile.java:74-76` |
| audible radius | 32 blocks (= `friend_chives.soundRange` default 32.0, volume = 32.0 ÷ 16 = 2.0) | fact card §1.8 |
| the audio asset | the current ogg is exactly the first **10.000 seconds** of the original asset (aligned with 200 ticks = 10 seconds, but not the same timer) | fact card §1.8 |

One hand-over: whether `sounds.json` should additionally carry `"attenuation_distance": 32` is a technical-face question (the key is currently unset and the vanilla default of 16 applies; the same file's `wild_hunter_loop` already sets 32 as a precedent), and 30 checks it; it **does not change this section's conclusion** (D-132 supplementary item, 28 §7 item 5).

### 8.5 Comparison with the old behaviour (one-shot at landing)

| Dimension | 0.1.0 | v0.1.1 |
|---|---|---|
| number of applications | **1** (the instant of landing) | **1 per tick × at most 200 ticks** |
| who is affected | the living entities inside the sphere at the instant of landing | those inside at the instant of landing + those entering the sphere during the following 200 ticks |
| duration per person | 200 ticks (all counted from the same moment) | `200 − x` ticks (x being the moment of entry) |
| expiry moment | 200 ticks after landing | **200 ticks after landing (the same moment)** |
| later entrants | unaffected | affected, with a remaining duration of exactly `200 − x` |
| leaving the sphere | irrelevant to the effect (the effect is already on the entity) | the effect runs its course with the entity and **does not end early** |
| the sound | played 1 time at landing only when there is ≥ 1 inside | **exactly the same** |
| the entity | disappears on landing | survives 200 ticks, stays visible, self-destructs on tick 200 |
| throwing again | the two throws are independent of each other | the two fields each time themselves independently (overlap in §8.6) |

The net effect of "how long the area is controlled": **the same spot goes from "a flash" to "10 seconds you cannot stand in"**. That is exactly what R9 asks for, and exactly the cost source RT-34 has to watch.

### 8.6 Boundaries and exceptions

| Scenario | Rule |
|---|---|
| 0 living entities in the sphere (an empty drop) | no sound is played; **the field still lasts 200 ticks** (D-131's field does not presuppose a hit) ⇒ a living entity walking into the sphere later is affected, but there is no sound cue at any point |
| entering midway (x = k) | gets `200 − k` ticks; still expires at x = 200 |
| leaving midway | the effect is already on the entity and runs out its remaining ticks ⇒ it still expires at x = 200 (he finishes his last few seconds bouncing outside the field) |
| the thrower himself standing inside the sphere | **affected just the same**: the effect side does not filter friend from foe (the existing comment states it, and this version does not change it) |
| a spectator / creative player | the predicate is only "a `LivingEntity` inside the sphere + alive" ⇒ **game mode is not filtered**. A creative player is bounced but takes no damage, and a spectator has no collision so nothing visibly changes — the judgment is the same, what is seen differs |
| the chunk unloads | **the field ends**: no NBT is written to keep it alive and no cross-dimension resumption (D-133). Walking away and coming back does not resume the remaining ticks |
| `discard()` forgotten on tick 200 | **an entity leak**: the lifetime cap no longer applies after landing (the base class's lifetime check carries a "not yet settled" condition), and no mechanism will reclaim it (RT-35) |
| two fields overlap | each field settles independently; for the same effect the vanilla rule takes "the stronger one" (same amplifier, compare duration) ⇒ a short duration arriving later **does not** shorten an existing long one, and the expiry moment is decided by the last, longer field [author's supplement] |
| the projectile is destroyed before landing (lifetime exceeded) | no settling, no field (the base class is still bound by the lifetime cap before settling) |
| the number of fields existing at once | limited by the projectile's lifetime and the throwing rate (RT-34); this version adds no extra cap |

### 8.7 Performance and do-not-touch list

| Item | Conclusion | Basis |
|---|---|---|
| scan cost | 1 per tick × per field; a coarse AABB filter plus a distance re-check, scanning `LivingEntity` only | RT-34; fact card §1.8 |
| what is not introduced | no block entity to carry the field (that would add a `block_entity_type` for `friend_chives`, and the registry surface would no longer be +0) | D-131 supplementary item |
| what is not changed (the effect) | Q-Bounce `farmstorm:q_bounce`'s amplifier (still 0), its `tick` is not rewritten, no config is read | fact card §1.8; D-131 |
| what is not changed (rendering) | the existing projectile renderer is reused, with zero client-side changes | D-133 supplementary item |

---

## 9. Master table of numbers

Every number is given with its source. The source is of only four kinds: a decision number in 28, the fact card's `file:line`, a config key name + default, or an [author's supplement] with the expression written out.

| # | Value | Meaning | Source |
|---|---|---|---|
| 1 | **64 × 64** | the size of the new `wild_apple.png` | D-126; fact card §1.1 |
| 2 | **16 × 16** | the current texture's size, and also the **display area** (independent of the texture resolution) | fact card §1.1; §2.4 [author's supplement] |
| 3 | **571 B** | the current texture's file size | fact card §1.1 |
| 4 | **6,946 B** | the source image's file size | fact card §1.1 |
| 5 | **88** | the current image's number of distinct RGBA values | fact card §1.1 |
| 6 | **1,814** | the source image's number of distinct RGBA values | fact card §1.1 |
| 7 | **107** | the current image's number of fully transparent pixels | fact card §1.1 |
| 8 | **149** | the current image's number of opaque pixels = 256 − 107 | [author's supplement] arithmetic |
| 9 | **1,471 / 2,625 / 0** | the source image's transparent / opaque / semi-transparent pixel counts | fact card §1.1 |
| 10 | **x ∈ [2, 60] / y ∈ [0, 63]** | the source image's alpha bounding box | fact card §1.1 |
| 11 | **33 px** | the pixel count of the current image's green "+"; `7 × 3 + 3 × 7 − 3 × 3 = 33` (a 3 px wide cross) | fact card §1.1; [author's supplement] arithmetic |
| 12 | **25 / 24** | the number of icons carrying a "+" / the number still in the old form after this version (25 − 1) | D-125 |
| 13 | **256 / 204** | the source image's number of 4 × 4 blocks / the number of non-uniform ones among them | fact card §1.1 |
| 14 | **16** | the average number of source pixels per display pixel (`64² ÷ 16²`) | [author's supplement] arithmetic |
| 15 | **16** | the per-core capacity (`outpost_core.farmlandBonus` default 16) | D-134 |
| 16 | **2** | the per-player core cap (`outpost_core.maxPerPlayer` default 2, untouched) | fact card §1.2; 28 §5 (D-122 retained) |
| 17 | **3 × 3** | the cores' influence range (chunks) | D-135 |
| 18 | **200** | the field's length (ticks) = `friend_chives.effectTicks`'s default | D-131 |
| 19 | **6.0** | the effect sphere's radius (blocks, Euclidean) = `friend_chives.hitRadius`'s default | D-131; `config/FarmstormConfig.java:851-853` |
| 20 | **`200 − x`** | every victim's remaining ticks (x = the moment of entry) | D-131 |
| 21 | **10 seconds** | 200 ÷ 20 | [author's supplement] arithmetic (D-132's supplementary item gives the same expression) |
| 22 | **32.0 / 2.0 / 16.0** | the audible radius (blocks) / volume / the per-block volume divisor (32 ÷ 16 = 2.0) | D-132; fact card §1.8 |
| 23 | **15** | the Outpost Core's block light | D-141 |
| 24 | **15 blocks** | the distance over which the light level decays from 15 to 0 (15 − 0) | [author's supplement] arithmetic |
| 25 | **60 → 40 ticks** | the Pumpkin Turret's firing interval (`pumpkin_turret.fireIntervalTicks`) | D-127 |
| 26 | **2 seconds** | 40 ÷ 20 | [author's supplement] arithmetic; D-127 already gives "2 seconds" |
| 27 | **1 → 3 points** | the Pumpkin Turret's damage per shot (`pumpkin_turret.projectileDamage`) | D-128 |
| 28 | **4.5 times** | the theoretical output ratio: `(3 ÷ 40) ÷ (1 ÷ 60)` | D-128 supplementary item |
| 29 | **0.6 → 0.45** | the bullet speed (blocks/tick) = the new key `pumpkin_turret.bulletSpeed` | D-129 |
| 30 | **9 blocks/second** | 0.45 × 20 | D-129 |
| 31 | **15** | the targeting radius (Chebyshev, `pumpkin_turret.targetRadius` default 15, untouched) | `config/FarmstormConfig.java:422-424` |
| 32 | **25 / 33.3 ticks** | the shortest flight time over 15 blocks: 15 ÷ 0.6, 15 ÷ 0.45 | [author's supplement] arithmetic |
| 33 | **10 ticks** | the turrets' scan interval (untouched) | `behavior/TurretBehavior.java:13-16` |
| 34 | **1** | the knockback level (`pumpkin_turret.knockbackLevel` default 1, untouched) | D-128; `:428-430` |
| 35 | **8 lines + 1 line** | the overlay's core-line cap + the overflow line | [card supplement]; "'+N' is a line of its own" is [author's supplement] |
| 36 | **`(4, 4)`** | the overlay's default coordinate (`hud.x` / `hud.y` default 4) | [card supplement]; D-144 item 3 |
| 37 | **3** | the number of CLIENT config keys (`hud.enabled` / `hud.x` / `hud.y`) | D-144 item 3 |
| 38 | **2** | the number of new `KeyMapping`s (toggle / drag) | D-144 item 1 |
| 39 | **1** | the number of new custom packets (S2C read-only) | D-142 |
| 40 | **9 / 1 / 2 / 93 px** | row height / row spacing / padding / total height with 9 full rows (`9×9 + 8×1 + 2×2`) | [author's supplement] |
| 41 | **13 px** | the total height with 1 row only (`9 + 2 × 2`) | [author's supplement] arithmetic |
| 42 | **~214 px** | the order of magnitude of the overlay's width (an estimate; the clamp uses the measured value) | [author's supplement] estimate; §5.6 |
| 43 | **`guiHeight ≥ 150`** | the condition for the overlay and the charging bar not to intersect vertically (`97 + 53`) | [author's supplement] arithmetic; `client/WildHunterHud.java:27`, `:55` |
| 44 | **30 / 6** | the Compressed Hay Block's hardness / blast resistance (untouched) | fact card §1.6; `registry/FarmstormBlocks.java:77` |
| 45 | **30 / 1200** | the Outpost Core's hardness / blast resistance (untouched) | fact card §1.2; `:86` |
| 46 | **166 / 58 groups** | the number of config keys (this version deletes 1 and adds 1, the total standing still) / the number of groups | 28 §3.4; the key-by-key naming is done by 30 |

Two notes under the table:

1. **The 16 in §3.4 (the quota's base number) and the 16 in row 15 (the per-core capacity) are two layers of meaning of the same config key** (`outpost_core.farmlandBonus`); see D-134 and RT-33. In the documents they are always spoken of separately.
2. **This table does not contain deleted keys**: `farmland.limitPerPlayer` is deleted in this version (D-139), and it appears only in the "deleted" explanations, never in any "current value / after" column.

---

## 10. Master table of player-visible behaviour changes (against 0.1.0)

| # | What 0.1.0 is | What v0.1.1 becomes | What the player will notice | Decision |
|---|---|---|---|---|
| 1 | the Wild Apple icon is a 16 × 16, 88-colour modal-picked finished image | the 64 × 64 original used directly | the icon looks different: larger and fuller, with a more natural gradation (the detail is still invisible, §2.4) | D-126 |
| 2 | 25 item icons carry a green "+" in the bottom-right corner | the rule is abolished and the mechanism deleted; **in this version only Wild Apple does not carry one**, and the other 24 are still in the old form | only Wild Apple's "+" disappears; the other items stay as they were | D-125 |
| 3 | farmland is protected as long as it falls inside an allied core's 3 × 3 | only farmland **bound to a core** is protected | fields in the overlap are no longer shovelled up incidentally by whichever core | D-134, D-136 |
| 4 | the fields a team can manage are limited by the quota only | a single core manages at most 16 plots | the quota is still enough, but the core covering this plot is full ⇒ cannot till (the message says the quota is full) | D-134, D-139 |
| 5 | quota = `base + 16 × number of cores`, `base` defaulting to 0 | quota = `16 × number of allied cores`, the concept of `base` deleted | a new game still cannot till, but the reason changes from "base is 0" to "there is no core" | D-139 |
| 6 | `/farmstorm farmlandlimit <n>` can raise the quota | the command and the whole server command tree are deleted | typing this command gives "unknown command" | D-140 |
| 7 | after the core's master goes offline his fields are unprotected | protected while offline just the same | the master can keep his own fields safe while away | D-138 |
| 8 | when a core is demolished, the allied farmland in range **all** turns to dirt | it is migrated to a successor core first, and turns to dirt only when it cannot be | demolishing a core usually does not clear a field but "changes its owner" | D-136 |
| 9 | the Outpost Core emits no light | block light 15 | it is bright around the core, no mobs spawn beside it, and crops grow at night | D-141 |
| 10 | the Pumpkin Turret fires one shot every 3 seconds, 1 point each | one shot every 2 seconds, 3 points each | the firepower is obviously stronger (4.5 times when converted per tick) | D-127, D-128 |
| 11 | the bullet goes 0.6 blocks/tick and almost never hits at point-blank range | 0.45 blocks/tick, converging and hitting at point-blank range | the bullet is slower and visible, but no longer jitters at close range | D-129 |
| 12 | the impression that the bullet "always hits" | still not always, but no longer always missing through overshoot | what should hit does hit, what should be dodged can still be dodged (blocks, teleport, the target dying) | D-129 |
| 13 | Friend Chives flashes once on landing | a persistent field of 10 seconds after landing | the same spot cannot be stood in for ten seconds; later entrants bounce too | D-131, D-133 |
| 14 | there is no core list on screen | a top-left overlay (switchable, draggable, cross-dimension) | an information panel has been added whose position one can set oneself | D-142, D-143, D-144 |
| 15 | a legacy save behaves the same after upgrading | the first start after upgrading backfills bindings; fields that cannot be bound are unprotected but **are not shovelled up** | the old fields are still there; a few of them may suddenly stop resisting trampling (depending on whether there is a core nearby that can take them) | D-137 |

**The two headlines**: row 2 (the icon changed) and row 14 (something was added to the screen) are what a player sees at a glance; rows 3 ~ 8 are rule changes the player only learns "when he runs into them"; row 15 is the only thing that needs explaining at the moment of upgrading.

---

## 11. Boundary list

Numbering starts at B-01. Each entry gives only "scenario / expected behaviour / basis / how it shows when not met"; the numbers are looked up in their own sections.

| No. | Scenario | Expected behaviour | Basis | How it shows when not met |
|---|---|---|---|---|
| B-01 | overlay: no core at all | not drawn, and `+N` not drawn either | [author's supplement]; §5.10 | an empty frame hangs in the top-left corner for good, and the player thinks the mod is broken |
| B-02 | overlay: only 1 core | 1 line, 13 px high, no `+N` | §5.5, §5.6 | the overlay is one blank line too tall |
| B-03 | overlay: a teammate's core, the teammate offline | shown as usual, the name taken from `ownerNames` | D-142 supplementary item | the teammate's core disappears from the list once he logs off |
| B-04 | overlay: the player switches dimension | the list and the order do not change, and the dimension at the head of the line is still the core's own | D-143 | the list is filtered after the switch and cores beyond view distance disappear |
| B-05 | overlay: reconnecting / rejoining the server | the full list is pushed at login; the client caches no previous session | D-142 supplementary item | the first frame shows the previous session's old data and cannot be taken back |
| B-06 | overlay: more than 8 cores | 8 core lines + 1 `+N` line (N = the total − 8) | [card supplement]; §5.5 | the 9th core is silently not shown, with no hint at all |
| B-07 | overlay: dragged off screen | clamped to `[0, guiWidth − width] × [0, guiHeight − height]` | [author's supplement]; §5.7 | once dragged away it cannot be found again, and only the reset command helps |
| B-08 | overlay: the window size / GUI scale changes while dragging | clamp again and press the coordinate back inside the bounds | [author's supplement] | the overlay stays off screen or is half cut off |
| B-09 | overlay: `ESC` in drag mode | save and exit only, with no pause menu | D-144 item 4; [author's supplement] | the save and the pause happen together and the player thinks the save failed |
| B-10 | overlay: pressing F1 or opening the inventory | not drawn; **drag mode is the exception** (neither early return takes effect) | `WildHunterHud.java:46`; D-144 supplementary item | while dragging the player cannot see what he is dragging |
| B-11 | overlay: on the same screen as the charging bar | no vertical intersection (always true while `guiHeight ≥ 150`) | [author's supplement] arithmetic; `WildHunterHud.java:27` | the two elements are drawn on top of each other on the same pixels |
| B-12 | overlay: two cores placed in the same tick (same `placedGameTime`) | ordered by **dimension id** → the core coordinate `(x, y, z)` lexicographically (the same three-level key as §3.3's tie criterion among candidates) | adjudication A-39; §5.4 | the two lines swap order on every refresh and players cannot agree on a callout |
| B-13 | overlay: enemy cores / core items | not shown | D-143 supplementary item | the enemy can see where your core is, or an item is shown as a core |
| B-14 | overlay: the client has not received the list yet (first frame / the instant of a reconnect) | treated as 0 rows (the same as no core, not drawn) | [author's supplement] | the previous data is drawn, or half a list is drawn |
| B-15 | farmland: no allied core covers this plot | new tilling is impossible; existing plots in a legacy save stay farmland and unprotected | D-135, D-137 | tilling works without a core and the quota loses its meaning |
| B-16 | farmland: every allied core covering it is full | reject, message `QUOTA_FULL`, and **no durability consumed** | D-135; the part of 0.1.0's D-116 that has not been superseded | durability is consumed and no farmland is placed |
| B-17 | farmland: the nearest core is full, but another core covering it has a free slot | bind to the core that has a free slot | [author's supplement] reading; §3.3 | the quota is enough and yet it is rejected, contradicting `16 × number of cores` |
| B-18 | farmland: after binding, the core's master or the farmland's owner switches teams | **the quota gate and the overlay recompute at once by the new team; trample protection is unchanged** (it depends only on whether that plot has a binding, not on teams) | adjudication A-45; D-138, C-9 | writing trample protection as "a team switch invalidates it", or the quota / overlay as "a team switch changes nothing" — both treat the three faces as one |
| B-19 | farmland: the core is destroyed and a successor exists but is full | turns to dirt (it does not wait for a free slot) | D-136 | the plot hangs unprocessed and the player thinks it still counts |
| B-20 | farmland: the core is destroyed and a successor covers it but is not allied with its master | turns to dirt | D-136 | after the enemy demolishes the core the field falls to him automatically |
| B-21 | farmland: when the core is destroyed that plot is no longer farmland | skip, remove the binding, do not touch the world | 0.1.0's existing criterion | dirt is `setBlock(DIRT)`ed once more, or some other block is shovelled up |
| B-22 | farmland: when the core is destroyed the chunk holding the farmland is not loaded | rebind if it can be rebound; with no successor, remove the binding only and leave the world alone | [author's supplement]; §3.10 | a chunk is force-loaded just to shovel one plot (a stutter), or the whole record is treated as nonexistent |
| B-23 | legacy save: a core is full during the backfill | that plot stays unbound, stays farmland, stays unprotected | D-137 | old fields that cannot be bound are shovelled up by force at startup |
| B-24 | legacy save: more fields than `16 × number of cores` (base was once raised by the command) | only those that can be bound are protected, the rest degrade | D-137; §3.8 row 10 | all are protected (the old behaviour) or all turn to dirt |
| B-25 | legacy save: a field whose backfill failed wants its protection back | the next startup's backfill is needed, or it must be dug out and tilled again | [author's supplement]; D-137 step 6 | the player tramples the same plot again and again expecting it to recover by itself |
| B-26 | Pumpkin Turret: the target dies while the bullet is in flight | `target` is nulled, the bullet flies straight on and disappears on hitting a block | `entity/TurretBullet.java:79-84`, `:121-128` | the bullet keeps chasing a dead entity, or a null pointer is thrown |
| B-27 | Pumpkin Turret: the target is at point-blank range from the turret (`dist < step`) | converges and hits | D-129 | circling in place and jittering (0.1.0's symptom) |
| B-28 | Pumpkin Turret: the old toml still holds 60 / 1 | no migration; the new key `bulletSpeed` is added automatically | D-147 | the player thinks the update did not take effect; or the code changes someone else's config file on its own |
| B-29 | Friend Chives: an empty drop (0 entities in the sphere) | no sound at all; the field still lasts its full length | D-131, D-132; §8.6 | an empty bang is played; or no field is created at all (which would contradict "a 200-tick field") |
| B-30 | Friend Chives: entering midway | gets `200 − x` ticks and still expires at x = 200 | D-131 | a full 200 ticks are given ⇒ that entity finishes later than those who came first |
| B-31 | Friend Chives: leaving the sphere midway | the effect runs its course with the entity and does not end early | D-131; §8.2 point 1 | the effect is cleared on leaving, which equals "no price to pay outside the field" |
| B-32 | Friend Chives: the thrower himself stands inside the sphere | affected just the same | the existing comment in fact card §1.8 | giving oneself an implicit exemption (the author did not ask for one) |
| B-33 | Friend Chives: the chunk unloads | the field ends and is not resumed | D-133 | after reloading, the application continues with a wrong x (someone gets his duration extended) |
| B-34 | Friend Chives: tick 200 | it `discard()`s itself | D-131, D-133; RT-35 | the entity stays in the world for ever (the lifetime cap no longer applies after landing) |
| B-35 | Friend Chives: two fields overlap | each field settles independently, and the effect takes the stronger one | [author's supplement]; §8.6 | a short duration arriving later overwrites a long one, and those affected first finish early |
| B-36 | Compressed Hay Block: break / place / step / hit / fall | all five classes of sound change to the stone family | D-130; §6.3 | only the break sound is changed and the other four are still grass sounds |
| B-37 | Outpost Core: mob spawning around it | no mobs spawn in the neighbourhood where the light level > 0; it brightens within 15 blocks | D-141; RT-36 | zombies still spawn beside the core, or the light reaches only 1 block |
| B-38 | Wild Apple: the "+" | removed from this one image only; the other 24 keep it | D-125 | "the rule is abolished" is read as "all of them were removed", or a generator re-run puts the "+" back |
| B-39 | Wild Apple: re-running the generator | the output is still 64 × 64 and byte-for-byte identical to the source image; the other 24 do not change | D-126; §2.6 | it is resized back to 16 × 16; or the size assertion makes the build fail outright |

39 entries. The first 14 belong to R5, B-15 ~ B-25 belong to R2 / R3, B-26 ~ B-28 belong to R7 / R8, B-29 ~ B-35 belong to R9 / R10, and B-36 ~ B-39 are the acceptance lines of R1 / R4 / R6.

---

## Appendix A · Register of differences between plan's original text and this version's measure

| # | plan line | plan's original text | This version's measure | Nature of the difference | Decision | Explanation |
|---|---|---|---|---|---|---|
| 1 | `:5` | "This version is based on the 0.1.0 version and keeps appending new items or features; unless this document explicitly requires it, the existing content of 0.1.0 must not be modified" | this version **deletes** the config key `farmland.limitPerPlayer`, **deletes the whole** `/farmstorm farmlandlimit` command tree and its registration point, deletes the concepts of `base` / `baseOverride` / `setBase`, and deletes the `TurretBullet.SPEED` constant | **overturned (authorised)** | D-139, D-140, D-129 | the authorisation comes from adjudication #16 ("deprecate `limitPerPlayer` and delete the command along with it"), and the deleted items are registered one by one in 28 §3.3 and §5. plan's "must not be modified" binds the part that **was not asked for**; things explicitly asked to be deleted are not in that set. A reader who looks only at `:5` would think this version only adds, and this table is the correction |
| 2 | `:9-10` | "change it to `wild_apple_64x64.png`", "modern minecraft supports 64*64 texture images, so the original image can simply be used as the texture image", "the previously required rule 'add a green + sign in the bottom-right corner of the mod's item textures' has been abolished" | the original image used directly (no downscaling, no 16-cell grid, no marker overlay) + the generator's six rollback points changed in the same version + **this version regenerates only 1 image** (the other 24 stay in the old form) | refinement | D-125, D-126 | plan says only "use the original image directly" and does not say the generator will overwrite it, nor what happens to the other icons after the abolition. Four of the six rollback points come from 28 §3.3 item 6 and fact card §3.2 items 19 and 20; the other two (`expected_size()` and the size assertion, `build_marker()`'s call site and the dry-run text) were added by this document reading the source (§2.6) |
| 3 | `:14` | "fix the bug where, when a piece of farmland exists inside the influence range of several Outpost Cores, destroying any one Outpost Core destroys that farmland" | not "fix one criterion" but replace "in range means managed" with "each plot is bound to 1 core only", and change the settlement object to the records under this core in the binding table | rewrite (structural replacement) | D-134, D-136 | plan describes the symptom; this version changes the carrier of the answer to "whose this plot is" (§3.7.1). Adding one criterion "does another core cover it too" would also stop the bleeding, but that would conflict with the capacity semantics of "16 plots per core" (tombstone §6 item 1) |
| 4 | `:15` | "fix the bug where the Outpost Core's owner going offline causes the Outpost Cores he owns to lose their farmland protection effect" | the protection criterion changes from "traverse the online players" to "farmland → binding → the core owner's UUID", regardless of online status | rewrite (structural replacement) | D-134, D-138 | it is one of the two faces of the same structural change as row 3 (§3.7.2, §3.7.3). Fixed on its own it would need either caching offline players or snapshotting the team, both of which run counter to C-9's live resolution |
| 5 | `:16` | "change the Outpost Core block's block light level to 15" | add `.lightLevel(state -> 15)` and leave every other block property untouched character for character; the knock-on effects (no mob spawning, propagation over 15 blocks, how the particles look, crop lighting) are written into §4.3 | refinement | D-141 | plan gives one number only; "everything else untouched" and the knock-on list were added by 28 |
| 6 | `:17` | "add a HUD overlay in the top-left corner of the game screen to show the positions of the Outpost Cores one owns; when the overlay is occluded it can **move automatically to the position closest to the top-left corner that is not occluded**, and one can choose whether to hide the overlay" | a top-left overlay, switchable, **draggable for positioning**; **no automatic avoidance**; the display scope widened to "all of one's own and one's teammates' cores, including unloaded chunks and other dimensions" | **overturned (the automatic-avoidance part)** + refinement | D-142, D-143, D-144 | the grounds on which automatic avoidance was rejected are registered in 28 §6 item 7 (a per-frame occlusion intersection against every vanilla UI and other mods' HUDs, possibly no solution when the overlay fills the top-left corner, and dragging being deterministic). The widening of "the ones one owns" to "all of one's own and one's teammates'" is adjudication #19, and widening the scope is exactly what necessitates D-142's packet |
| 7 | `:20-23` | "change the Compressed Hay Block's sound to a stone-family sound" | `SoundType.GRASS` → `STONE`; five classes of sound change together; hardness / blast resistance / tool / colour untouched | not mentioned (meaning: this one is **not** among the 25 adjudications) | D-130 | **this row has no adjudication by the author**: its grounds are only plan's original text itself and fact card §1.6 / §3.2 item 3. 28 turned it into a decision of its own (D-130), otherwise the six downstream documents would have missed plan's item 3 wholesale. If the author thinks it should not be a separate item, deleting D-130 would require renumbering D-131 ~ D-147 as a whole |
| 8 | `:26` | "shorten the Pumpkin Turret's default attack interval to 2 seconds, with damage of 3 points" | landed as two config defaults: `fireIntervalTicks` 60 → 40 and `projectileDamage` 1 → 3; and it explains that the theoretical output converted per tick is 4.5 times | refinement | D-127, D-128 | plan's "2 seconds" is 40 ticks (40 ÷ 20); the two numbers must be read together with the bullet speed, otherwise it will be read as "only strengthened 3 times" |
| 9 | `:27` | "lower the flight speed of the bullets the Pumpkin Turret fires, **add a tracking feature (refer to the tracking effect of the vanilla shulker's missile; ideally the code can be reused directly)**" | lower the speed 0.6 → 0.45 and promote it to a config key; **introduce no soft tracking**, fixing only "the point-blank overshoot"; **do not reuse** `ShulkerBullet` | **overturned (the tracking part)** | D-129 | two reasons: ① 0.1.0's bullet already re-aims every tick (`entity/TurretBullet.java:73-96`), so "adding tracking" is already true functionally and what is really missing is convergence at close range; ② reusing the vanilla tracking would mean changing the entity superclass and introducing two parameters the author did not give (a turn-angle cap and a maximum chase duration), while the vanilla tracking circles at point-blank range just the same (tombstone §6 item 8). This row's nature is **rewriting**, not "not implemented" |
| 10 | `:31` | "after landing, apply `200-x` ticks of Q-Bounce to all living entities inside a sphere of radius 6 blocks at the landing point over the following 200 ticks", "only when at least 1 living entity inside the sphere is hit does it play the audio once at the landing point (audible within 32 blocks, volume 2.0); it plays only once and does not play on an empty drop" | the field's length = `friend_chives.effectTicks` (default 200), a sphere scan every tick, applying `effectTicks − x`, self-destruction on tick 200, visible throughout the entity's life; the sound is still judged once, at the moment of landing | refinement | D-131, D-132, D-133 | plan gives the full numbers and judgments but does not give: where x starts, the field's self-destruction, the entity's visibility, the relation between the field's length and the config, or which value to take when two fields overlap. All of these are settled in §8 |

Three notes under the table:

1. **The three head-on conflicts** are all in this table: row 6 (automatic avoidance → dragging, tombstone §6 item 7), row 9 (tracking → fixing the overshoot only, tombstone §6 item 8), and row 1 ("must not modify the existing content" → deleting keys and commands, authorised by adjudication #16). None of the three may be written as "by the way", "not implemented" or "relaxed".
2. **One requirement without an adjudication** is row 7 (the Compressed Hay Block's sound): its grounds are plan's original text itself, not an interview conclusion. It is the only requirement in this round that "plan has and the adjudications do not".
3. The other six rows (2, 3, 4, 5, 8, 10, plus row 7's implementation detail) are all **refinements or rewrites**, and not one of them lacks a decision number; wherever this document departs from plan, this table has named it, and a departure with no D number behind it counts as a document defect.

---

## Appendix B · Term correspondence (the word used in this document → 28's glossary)

| Term in this document | 28's term | Judgment boundary (per 28 §0.2, not rewritten) |
|---|---|---|
| HUD overlay | HUD overlay | the self-drawn core list in the screen's top-left corner; not the boss bar, not the scoreboard, not the chat column, not `WildHunterHud`'s charging bar |
| core / Outpost Core | Outpost Core | the one block in the world and its block entity; in item form it is written "core item" |
| core owner | core owner (inside the "Outpost Core" entry) | the placer's UUID, **which may be offline** |
| allied | allied | the core owner himself + same-team allies, judged by UUID / name and regardless of online status; a mob is always treated as non-allied |
| capacity / 16 per core | per-core capacity | a binding allowance whose unit is "a plot of land"; **not the quota** |
| quota | (the `16 × number of allied cores` of 28's D-139) | the total number of plots all allies together can till; the same source as capacity (the same config key) but not the same meaning |
| binding | farmland binding | the persistent relation between a plot of farmland and **exactly one** core; what is bound is "the core's coordinate + dimension", not the core's owner; a plot has 0 or 1 of them |
| migration | migration | when a core is destroyed, the binding **relation** is redirected to another core and the block does not move |
| field | (28 has no separate entry) | this document's short name for "Friend Chives' 200-tick window after landing": the judgment boundary runs from the instant of landing (x = 0) to self-destruction on tick 200, and the application window is [0, 199] |
| Q-Bounce | Q-Bounce | the status effect `farmstorm:q_bounce` (`HARMFUL`, `0xFF6EC7`); this version changes neither its amplifier nor its `tick` |
| Pumpkin Turret | Pumpkin Turret | means `farmstorm:pumpkin_turret` only; Melon Grenade / Melon Slice Bomb / the Melon Grenade stem are none of them |
| the "+" | the "green +" rule | the **globally abolished** rule of overlaying a green cross in the bottom-right corner of item textures; it is not "no icon carries a +" |
| pass-through | (coined by this document) | the branch in which the generator no longer runs `wild_apple` through 16-cell colour picking and marker overlay (§2.3) |
| overflow line / `+N` | (the literal of 28's D-143) | the `+number` on line 9; not counted among the 8 core lines (§5.5) |
| core line | (28's D-143 "one line per core") | the line one core occupies, at most 8 lines |
| binding table | (28's D-134 "the farmland → core binding table") | the single source of truth for both directions, "farmland → core" and "core → farmland"; `byOwner` is derived from it and is not written to disk on its own |

## Translator's notes

1. **Markup tags are rendered as English bracketed tags**, one for one with the Chinese source: 【D-1xx】 → `[D-1xx]`, 【卡 §1.x】 → `[card §1.x]`, 【`路径:行号`】 → `[`path:line`]`, 【本文补充】 → `[author's supplement]`, 【卡补充】 → `[card supplement]`, and the qualified forms such as 【本文补充：算术】 → `[author's supplement: arithmetic]`. The markers themselves are never dropped.
2. **Chinese strings that are program output stay in Chinese, character for character**: `尺寸断言失败` (§2.6 row 3) and the generator's dry-run text `+ 绿色「+」` (§2.6 row 5). Likewise, wherever this document discusses `man瓜` as a word (the backticked token in §7's opening line and in Appendix B), the token is kept.
3. **The plan text quoted in Appendix A is translated**, because those are the author's request sentences in `plan0.1.1.txt` rather than string values written into any file; the plan's line references (`:5`, `:9-10`, `:17`, `:20-23`, `:31` and so on) and every number are reproduced unchanged, including the plan's own `64*64` and `200-x` spellings.
4. **Example strings in §5.3**: 自己 is the Chinese client's display value for the "self" language key and is rendered "self"; the sample player name 阿麦 is rendered "Amai". Every coordinate, number and dimension id in the example is unchanged. One exception: the sample line inside §5.6's width row keeps the source's `自己`, because that row's estimate counts "2 han characters × 9 = 18" and would not add up with a 4-character English word.
5. **The declaration that the two orderings differ on purpose** (§3.6 item 2: three-level key for candidate cores, single-level `BlockPos` order for ledger entries, "the two are written differently on purpose, and it is not a typo") is retained in full, together with its grounds, because without it a reader would take one of the two for a slip of the pen.
6. **Six defects this translation reported in the Chinese source were accepted and fixed upstream (A-59); this copy follows the corrected text.** They were: (a) §3.10's table had a 2-column header while its team-switch row carried a third cell — the table is now **3 columns** (`Scenario | Rule | Basis`) with all 7 rows filled; (b) §4.5's table had the same mismatch — it is now **3 columns** (`Scenario | Rule | Basis / supplement`) with all 4 rows filled; (c) the blockquote said `plan0.1.1.txt` had "5 requirements" while §1 tabulated 10 — the blockquote and the metadata row now say "**5 original plan items**, split by this document into **10 requirement items R1~R10**", and §1 carries the 5 → 10 split measure; (d) §3.4 said "four worked examples" under a five-row table — now "**five worked examples**"; (e) §5.6's width estimate counted "33 ASCII characters" for a 32-character sample — now **32 ASCII × 6 + 2 han × 9 = 192 + 18 = 210, plus 4 = 214**, and §9's row 42 follows as **~214 px**; (f) §9's note 1 attributed two meanings to row 15 — it now names both sides explicitly (the 16 of §3.4 as the quota's base number, the 16 of row 15 as the per-core capacity, one config key `outpost_core.farmlandBonus`).
