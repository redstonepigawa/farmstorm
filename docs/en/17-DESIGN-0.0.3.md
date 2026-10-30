# 17 · v0.0.3 Design Delta

| Item | Value |
|---|---|
| Document | 17-DESIGN-0.0.3 |
| Version | v0.0.3 |
| Status | Design final |
| Upstream | `docs/zh/16-DECISIONS-0.0.3.md` (the single source of truth, D-80 ~ D-102; D-98 ~ D-102 are the supplementary rulings of §3.6), `plan0.0.3.txt` (the author's original requirements), `docs/zh/11-DESIGN-0.0.2.md` (the v0.0.2 design delta, the source of this document's style and structure), `.work/v003-BRIEF.md` (construction fact card, read-only) |
| Downstream | `18-TECH-0.0.3.md`, `19-TASKS-0.0.3.md` |
| Language | Body text in Chinese; code identifiers, registry names and config keys always in English |

---

## 0. Reading conventions

### 0.1 Relationship to 11-DESIGN-0.0.2.md

This document is a **delta**, not a rewrite. The v0.0.1 design (`01-DESIGN.md`) and the v0.0.2 design (`11-DESIGN-0.0.2.md`) are both final; of the two, v0.0.2 is already implemented and released (`dist/farmstorm-0.0.2+mc1.21.1.jar`, 814,935 B). This document redefines none of the existing rules; wherever it says 'carried over', it points back to the corresponding subsection of `01-DESIGN.md` or `11-DESIGN-0.0.2.md`, and the two are read side by side.

v0.0.3 does exactly two things: ① append the 5 new entries of §2 ~ §6 (2 status effects + 3 new profiles); ② change **two places** of existing implementation (§7 Fang Trap Cactus, §8 Zangief Cane). Apart from these two, everything in 0.0.2 stays as it is (`16-DECISIONS-0.0.3.md` §5.7 gives the boundaries class by class).

### 0.2 Markup

| Markup | Meaning |
|---|---|
| 【D-xx】 | The conclusion comes directly from the corresponding decision in `16-DECISIONS-0.0.3.md` §3 (D-80 ~ D-102; of these, D-98 ~ D-102 are in the supplementary rulings of §3.6) |
| 【P-x】 | The conclusion comes directly from item x of the functional requirements in `plan0.0.3.txt` (items 1 ~ 7) |
| 【本文补充】 | A value or convention that neither 16 nor plan gives, derived by this document from existing decisions or source-code facts. Implementation follows it as written; no second version is established anywhere else |
| Fact card §x | The corresponding subsection of `.work/v003-BRIEF.md`. It is the flattened construction-period draft, not a deliverable, and serves only as a source of grounds |

Every number in this document has only three legitimate sources: it is written in 16, it is written in plan, or it is marked 【本文补充】. A number that is none of the three is a document defect.

### 0.3 Units and rounding

1. Time: seconds / game ticks (tick), 1 second = 20 ticks.
2. Length: blocks (block).
3. Health and damage: points. Vanilla 2 points = half a heart, so 7.5 points = 3.75 hearts.
4. Level and amplifier: when level N is stored, `amplifier = N − 1`. So 'Slowness V' = `amplifier 4` (v0.0.2 already fixed this conversion in `10-DECISIONS-0.0.2.md` D-69).
5. Angle: degrees (°). Rotational speed is written in degrees/tick.
6. Percentage: always a number carrying `%`, with no implicit rounding.

### 0.4 Distance measurement

Carried over from 16 §7.3's refinement of D-20: the main rule 'traps and turrets use Chebyshev' is unchanged, while **auras / effect radii use Euclidean**. This version's three new checks and one changed value all take the Euclidean route: Friend Chives 6.0 blocks (§4.5), Point Defense Fungus 8.0 blocks (§5.3), Vibration Bean strictly less than 4.0 blocks (§6.5), Fang Trap Cactus 2.5 blocks (§7.3). The reason for each is written in its own section.

### 0.5 Authority and synchronisation

1. **Server-authoritative**: landing settlement, block writes, effect application, damage and block removal all happen on the server (D-23 wording unchanged).
2. **0 custom packets** (D-50, still fully in force in this version): Q-Bounce and Confusion are both `MobEffect`s, expressing the complete state through 'the effect exists + amplifier + remaining duration', and synchronising free of charge over the vanilla `ClientboundUpdateMobEffectPacket`. All three presentations of Q-Bounce (riding pose, rotation, squash-stretch) derive from 'the effect exists' and are computed on the client; no synchronisation field is added.
3. **The render layer never writes back**: Q-Bounce's rotation and squash act only on the render layer's `PoseStack`, never written to `entity.setYRot()` / `entity.setXRot()`, and neither `Pose` nor the bounding box is changed (the same wording as 11 §0.5 clause 3).

### 0.6 Number discipline of this document

1. **Give only the increment, never the registered-object total.** This version's registered-object increment is +13 (construction fact card §3.1), itemised in §1.3. The total is never written down: `tools/verify_project.py` still hard-codes v0.0.1's tally of 64 and is already FAIL, so no total assertion holds until it is fixed (RT-23).
2. A count has only three legitimate sources: a table in 16, construction fact card §3, or an item-by-item count of the source code (the last is always marked 【本文补充】 and carries the file path).
3. **An increment is not a total**: +13, the 22 profiles and the 19-entry pool are three numbers of different dimensions and do not stand in for one another.

---

## 1. Positioning and boundaries of v0.0.3

### 1.1 One-line positioning

v0.0.3 makes increments on top of the implemented and released 0.0.2: **2 status effects** (the visible `farmstorm:q_bounce`, which seats affected mobs, rotates them, squashes and stretches them and locks out every action; the hidden `farmstorm:confusion`, which only inverts the player's movement-direction components), **3 new profiles** (Friend Chives / Point Defense Fungus / Vibration Bean), plus **two small reworks** (the Fang Trap Cactus rename and four values, and Zangief Cane's landing damage fixed at 7.5 points).

### 1.2 The 7 entries and their sections in this document

| # | plan item | Section of this document | Decision | Delivered form |
|---|---|---|---|---|
| 1 | Status effect Q-Bounce 【P-1】 | §2 | D-81, D-82, D-102 | 1 visible `MobEffect` + 3 `qbounce.*` keys + two pose branches (the mount adds no `entity_type`) |
| 2 | Status effect Confusion 【P-4】 | §3 | D-83 | 1 hidden `MobEffect` (no icon registered, no keys) |
| 3 | Friend Chives + Chive Seeds 【P-2】 | §4 | D-84, D-85 | 1 profile + 1 projectile `entity_type` + 1 crop block + 1 seed item + 1 sound event |
| 4 | Point Defense Fungus 【P-3】 | §5 | D-86 ~ D-89, D-98 ~ D-100 | 1 profile + 1 deployed block + 1 bidirectional conversion recipe |
| 5 | Vibration Bean 【P-5】 | §6 | D-90 ~ D-92, D-100, D-101 | 1 profile + 1 projectile `entity_type` + 1 unattached trap block + 1 bidirectional conversion recipe (the block entity reuses the existing one) |
| 6 | Fang Trap Cactus rework 【P-6】 | §7 | D-93 ~ D-95 | 1 Chinese display name + 4 values + 2 new `fang_trap_cactus.*` keys |
| 7 | Zangief Cane rework 【P-7】 | §8 | D-96 | 1 new damage type + 2 new `zangief_cane.*` keys |

### 1.3 Number conventions (increments only)

| Metric | This version's increment | Detail |
|---|---|---|
| `items` | **+4** | `friend_chives`, `chive_seeds`, `point_defense_fungus`, `vibration_bean` |
| `blocks` | **+3** | `friend_chives_crop`, `point_defense_fungus` (deployed block), `vibration_bean` (unattached trap block) |
| `entity_types` | **+2** | `friend_chives` (projectile), `vibration_bean` (projectile) |
| `mob_effects` | **+2** | `q_bounce` (visible), `confusion` (hidden) |
| `sound_events` | **+1** | `friend_chives` (hit sound) |
| `damage_types` | **+1** | `zangief_slam` |
| Conversion recipes | **+2** | Point Defense Fungus, Vibration Bean (1 bidirectional recipe each) |
| Advancements | **+3** (3 new this round, hard count 15 → 18) | First Q-Bounce applied / first interception by Point Defense Fungus / first Vibration Bean trigger (§10) |
| `block_entities` | **+0** | Both Point Defense Fungus and Vibration Bean reuse the existing `DeployedCropBlockEntity` |
| Total | **+13** | The sum of the rows above (construction fact card §3.1) |

Three further cross-cutting conventions:

| Metric | Value | Source |
|---|---|---|
| `CropProfile` entries | 19 → **22** | Construction fact card §3.2; the 22-row table of `16-DECISIONS-0.0.3.md` §4 |
| Lucky Berry random pool | 16 → **19** | D-97; §9 |
| config | **19 keys** (16 purely new + 3 changed existing defaults), plus 1 'Confusion has no keys' line that does not count towards the key count | Construction fact card §3.3; §11 |
| PNG / OGG | **+8 / +1** | Construction fact card §3.4; the 8th is the Vibration Bean item icon, see `18-TECH-0.0.3.md` §1.4(d) |
| `sounds.json` event count | 2 → **3** | Construction fact card §3.4 |

**The registered-object total is not asserted in this document**: `tools/verify_project.py` is still stuck on v0.0.1's hard-coded tally of 64 and is already FAIL (RT-23). The total will be filled in by `18-TECH-0.0.3.md` from a measurement once the tool is fixed; before that no total holds (16 §6 clause 10).

### 1.4 Non-goals of v0.0.3

The 9 items below are transcribed from the parts of 16 §6 that bear on this version's design; none of them may be given an exception during implementation (to open an exception, change 16 first, then change the code):

1. **Change none of documents 00 – 15** (16 §6 clause 1, D-80).
2. **Change neither the Fang Trap Cactus registry id, crop block id, `DeployedKind` constant nor English name** (§6 clause 2, D-93).
3. **Add no converter recipe for Friend Chives** (§6 clause 3, D-84).
4. **Never let Point Defense Fungus intercept vanilla throwables or other mods' projectiles** (§6 clause 4, D-87).
5. **Never let interception deal damage, break blocks or construct an `Explosion`** (§6 clause 5, D-89).
6. **Add no 'the effect's own duration' key to Q-Bounce or Confusion** (§6 clause 6, D-81, D-83).
7. **Build no second input lock** (§6 clause 7, the seven segments of D-79).
8. **Do not delete the losing Q-Bounce pose branch** (§6 clause 8, D-82).
9. **Do not reuse `supportsFloating` for the Vibration Bean's unattached block**, and add no new exclusion to the Lucky Berry pool (§6 clauses 9, 11, D-91, D-97).

### 1.5 The three seams with v0.0.2

| Seam | Boundary | Basis |
|---|---|---|
| Registry side | New entries only; no reordering and no deletion of existing entries. Of the existing config defaults only the 3 keys of `fang_trap_cactus` are changed (§7.6), not one other | 16 §5.4, §5.7 |
| Data side | The 2 new conversion recipes reuse the existing `ConverterRecipe`'s ordinary form (bidirectional, 3 XP, no catalyst); the 2 new `entity_type`s ride the existing `entity/ThrownCropEntity.java` base class | D-86, D-90, D-87 |
| Presentation side | Q-Bounce reuses the existing seven-segment input lock (D-79); Confusion only adds one inversion step on the same movement-input chain; 0 custom packets | D-81, D-83, D-50 |

---

## 2. Status effect: Q-Bounce

### 2.1 One-line positioning

A status effect that seats the affected mob in a **riding pose**, **keeps rotating** it about the vertical axis, and **squashes and stretches** it over and over as it rotates; for as long as the effect lasts, **every action** of the affected player **is locked out**. The duration is not set by the effect itself but written by the applier (this version's only applier is Friend Chives, which writes 200 ticks).

### 2.2 Type and category

| Item | Value | Basis |
|---|---|---|
| Registry id | `farmstorm:q_bounce` | D-81 |
| Carrier | Status effect (`MobEffect`), one of this version's `mob_effects` +2 | D-81, construction fact card §3.1 |
| Visibility | **Visible**: this entry shows in the HUD icon row | D-81 |
| Icon | `assets/farmstorm/textures/mob_effect/q_bounce.png`, 18×18 PNG | D-81, D-53 |
| Icon asset | The Steam Workshop `preview.jpg` (1024×1024 RGB, 622 KB) converted to 18×18; the conversion goes through the existing `tools/convert_media.ps1`, with no separate script | D-81, D-53, construction fact card §1.3 |
| Duration | **The effect sets no duration key of its own**; the applier writes it (Friend Chives writes 200 ticks) | D-81, D-85, 16 §6 clause 6 |
| Applies to | Mobs (`LivingEntity`): both players and non-player mobs count | D-85's 'the mobs inside the sphere' |
| config group | `qbounce.*`, 4 keys (`poseMode` / `rotateDegreesPerTick` / `squashAmplitude` / `squashPeriodTicks`) | D-81, D-82; construction fact card §3.3; §11 |
| Synchronisation | Vanilla effect packet, 0 custom packets | D-50 |

**Note on non-player mobs** (【本文补充】): all three presentations play for mobs too (pose, rotation and squash all go through the render layer, regardless of whether the entity is a player); the 'cannot perform any action' part is unobservable for mobs — non-player mobs have no client input, and D-81's lock was always only on the player's input.

### 2.3 Value table

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Effect duration (written by the applier) | 200 (= 10 seconds) | tick | D-81, D-85; config `friend_chives.effectTicks` |
| Rotation speed | 24.0 (= 480 degrees/second) | degree/tick | D-81; config `qbounce.rotateDegreesPerTick`. 【本文补充】: 480 = 24 × 20; one turn = 360 ÷ 24 = 15 ticks = 0.75 seconds |
| Squash-stretch amplitude | 0.15 | fraction of the model's original size | D-81; config `qbounce.squashAmplitude` |
| Squash-stretch period | 6 (= 0.3 seconds) | tick | D-81; config `qbounce.squashPeriodTicks`. 【本文补充】: 0.3 seconds = 6 ÷ 20; about 3.3 periods within 1 second |
| Pose branch | `render` (default) / `mount` | enum | D-82; config `qbounce.poseMode`; §2.4 |
| Icon size | 18×18 | pixel | D-81, D-53 |
| Input-lock coverage | The whole time the effect lasts (no 'lock only the first N ticks' predicate) | — | D-81, D-79 |

### 2.4 The two pose implementation branches (both must be implemented)

D-82 requires **both `render` and `mount` to be implemented**, switched by `qbounce.poseMode`, with `render` as the default; after comparing them on a real machine the author **keeps one of them**, and the losing one is switched off through config with **its code kept** (16 §6 clause 8). This document does not predict which one wins — the choice can only be made on a real machine (D-82's basis).

#### 2.4.1 Branch A: `render` (default)

| Item | Content |
|---|---|
| Presentation | On the client, `RenderLivingEvent` yields the renderer and the model instance; the model's `riding` flag is set true and a downward translation is applied to the `PoseStack` (pressing the model down), so the model shows the vanilla riding pose. Each client renders the entities it can see itself, so 'others see me sitting' and 'I see others sitting' are the same piece of logic |
| What changes | Only the model instance and the pose matrix of the **render phase** |
| What does not change | Nothing about the entity itself: the ``vehicle`` relation is untouched, the bounding box is untouched, `Pose` is untouched, AI and pathfinding are unaffected |
| Cost | ① It works only for custom models extending the vanilla `HumanoidModel` / `EntityModel`; a renderer another mod built for itself may not honour the `riding` flag (RT-17); the degraded path is '**rotate only, do not press into a seat**' — the rotation and squash of the trio still play, and the pose falls back to the original. ② The pose is an **approximation**: the model looks seated, but the entity actually has no vehicle |
| Implementation layer | Pure client render layer, zero server involvement |
| Basis | D-82; RT-17 |

#### 2.4.2 Branch B: `mount`

| Item | Content |
|---|---|
| Presentation | The server puts an **invisible seat** on every affected mob (`startRiding`); vanilla computes the pose itself, so what you get is vanilla's real riding pose |
| What changes | Server-side entity relations: the mob's vehicle field is taken up, and the client's `rideTick` / render pose change with it |
| Cost | ① The mob's `isPassenger()` is true, so most AI goals (melee, pathfinding, jumping) stop running while it is a passenger. ② An entity has only one vehicle; once a player is put on a seat it can no longer board a boat or ride a horse. ③ The bounding box changes with the vehicle. ④ The seat entity itself has to tick and has to be removed when the mob dies; a missed cleanup leaves an invisible entity drifting in the world for ever (RT-18) |
| Implementation layer | Server-side entity-relation layer; zero client changes |
| Default | **Off** |
| Basis | D-82; RT-18 |

**One hard boundary** (D-102): the `mount` branch **may not add an `entity_type`** — the seat is carried by a vanilla entity type that already exists, and this version's `entity_types` increment stays +2 (the two projectiles, construction fact card §3.1); which vanilla type exactly is pinned by the entity table of `18-TECH-0.0.3.md` §4 (RT-18). The use of this boundary is that even if this branch wins, the registration increment table of this version (§1.3) does not change.

#### 2.4.3 Comparing the two branches and how to switch

| Dimension | `render` (default) | `mount` |
|---|---|---|
| Pose source | The client sets the model instance's `riding` flag + presses the model down | The vanilla vehicle relation (vanilla computes the pose itself) |
| Who changes it | The client's `RenderLivingEvent` | The server mounts an invisible seat |
| Affects AI / bounding box | No | Yes |
| Third-party custom models | May ignore the flag → degrades to 'rotate only, no seat' (RT-17) | Unaffected (vanilla pose) |
| Blast radius when it goes wrong | The picture on screen | Entity state |
| How to switch | config `qbounce.poseMode = render` (the default value) | config `qbounce.poseMode = mount` |
| When it takes effect | Read at startup, effective after a restart once the config changes (【本文补充】: consistent with how the existing config is read in, no hot switching) | Same |
| May it be deleted | No; even the loser keeps its code (16 §6 clause 8) | Same |

Both branches share the same set of 'trio' parameters (§2.5): `poseMode` decides only **how the pose segment** comes about; rotation and squash have nothing to do with the branch.

### 2.5 The trio while the effect lasts

| Presentation | Parameter | Implementation layer | Basis |
|---|---|---|---|
| ① Riding pose | `qbounce.poseMode`: `render` = the model's `riding` flag + pressing the model down; `mount` = the server mounts an invisible seat | `render` on the **client render layer**; `mount` on the **server-side entity-relation layer** | D-82 |
| ② Continuous rotation | 24.0 degrees/tick about the vertical axis (yaw direction); the direction and the accumulated angle only affect how it looks | **Client render layer**: rotate about the y axis on the `PoseStack`, accumulated per tick | D-81, construction fact card §3.3 |
| ③ Repeated squash-stretch | Amplitude 0.15, period 6 ticks | **Client render layer**: a three-axis scale on the same `PoseStack` | D-81, construction fact card §3.3 |

The formulas for the three (【本文补充】; every parameter value comes from the table above):

```
t   = the number of ticks this entity has been 'continuously under the effect' on this client (0 on first sight, +1 per tick, back to 0 when the effect disappears)
θ   = (24.0 × t) modulo 360                                   // unit: degrees
s   = 0.15 × sin(2π × t / 6)                                  // the amplitude comes from squashAmplitude
scale = (1 − s ÷ 2,  1 + s,  1 − s ÷ 2)                       // y stretched apart, xz narrowed, volume approximately conserved
```

Three implementation constraints:

1. **Fixed order**: rotate first, scale second. Reversing it makes the scale axes rotate along too, and the look turns from 'bouncing up and down' into 'squeezing diagonally along the body' (【本文补充】).
2. **The time base accumulates locally on the client**, never back-derived from 'total duration − remaining duration'. The state is still expressed by the `MobEffect` (D-50) and the client uses it only for an existence check; back-deriving would jump phase in the two cases of 'reconnecting' and 'being applied again by another source with a different duration' (【本文补充】).
3. **Never write back to the entity**: no `setYRot`, no `setPose`, no bounding-box change, no packet (D-50, §0.5 clause 3). The moment the effect disappears the trio's accumulated values all drop to zero together, and counting starts over only when the effect is seen again on a later tick (【本文补充】).

### 2.6 'Cannot perform any action' = reusing the existing seven-segment input lock

D-81 says outright that locking actions **reuses 0.0.2's seven-segment input lock (D-79) and writes no second set**. This document lands it that way: not one of the seven segments changes, only the predicate is widened.

| # | Segment | Hooked to which event | How it locks | Inputs covered | Basis |
|---|---|---|---|---|---|
| 1 | Movement | `MovementInputUpdateEvent` | `forwardImpulse` / `leftImpulse` zeroed, `jumping` / `shiftKeyDown` set false | Forward/back/left/right, jump, sneak | D-79 |
| 2 | Mouse button | `InputEvent.MouseButton.Pre` | Cancel the event (cancellable, and the earliest entry point) | The first gate for left-click attack and right-click use | D-79 |
| 3 | Interaction, second net | `InputEvent.InteractionKeyMappingTriggered` | Cancel the event | `startAttack` / `startUseItem` / `continueAttack` (including 'hold the left button to keep mining') | D-79 |
| 4 | Keyboard actions | `InputEvent.Key` | **Drain** `consumeClick()` (the event is not cancellable, so draining is all there is) | Hotbar 1–9, drop Q, inventory E, swap offhand F, toggle perspective F5 | D-79 |
| 5 | Mouse wheel | `InputEvent.MouseScrollingEvent` | Cancel the event | Wheel hotbar switching | D-79 |
| 6 | Camera | `CalculatePlayerTurnEvent` | `setMouseSensitivity(0)` + cinematic camera off | Mouse turning (a fixed `0 × 0.6 + 0.2` coefficient remains, i.e. extremely slow but still nudgeable) | D-79 |
| 7 | GUI | `ScreenEvent.Opening` | Cancel, letting through only the pause menu and the death screen | Inventory, chat, containers, any third-party screen | D-79, source `client/InputLockHandler.java` |

Three accompanying conventions:

- **Two things are let through**: the pause menu (ESC) and the death screen. The death screen is a deliberate exception — `/kill` and fall damage can both happen during a lock, and blocking the death screen would leave the player stuck with no UI at all (the source comment agrees with D-79's 'leave only ESC').
- **Three things cannot be blocked** (unreachable without a Mixin, and not counted as a defect at acceptance): F11 fullscreen, F2 screenshot, F3 debug.
- **The tick the lock comes on must reset the key state**: if the left button happens to be held at the moment of locking, `keyAttack` stays `isDown` and the mining never stops; so at the instant the lock comes on, the pressed state of `keyAttack` / `keyUse` is cleared on purpose (D-79's state-reset clause).

**How the reuse is done** (【本文补充】; this is the only place in this codebase where D-81's 'reuse' lands): the seven-segment implementation in `client/InputLockHandler.java` is unchanged in every segment, and only the lock predicate is widened from 'the Zangief Cane phase effect exists' to 'the Zangief Cane phase effect exists **or** `farmstorm:q_bounce` exists'.

- Hence D-81's 'cannot perform any action' = the union of the seven segments in the table above; this document lists no separate action inventory, and the table above is the authority on what an action is.
- When Q-Bounce and the Zangief Cane phase lock are present at the same time, all seven segments lock; unlocking follows 'neither effect is present' (【本文补充】).
- Confusion does not take part in this predicate (§3.5).

### 2.7 Boundaries and anomalies

| Scenario | Rule |
|---|---|
| The player dies while the effect is on | The death screen is let through as usual (the exception in segment 7); the effect disappears with the entity's removal, the lock is released at the same time and the trio's accumulated values drop to zero (details in §12.2) |
| The player quits the game while the effect is on | The effect is saved with the player data and continues for its remaining duration on reconnect; the client's tick count restarts from zero and the phase is rearranged — rotation and squash are both periodic functions, so nothing visible is affected (【本文补充】, details in §12.2) |
| The effect is removed by `/effect clear` or another mod's cleanup logic | The trio stops at once and the lock is released at once; nothing is re-issued and nothing is left behind (the same treatment as the phase constraint of 11 §6.6) |
| Under the `mount` branch the mob is already riding or is pulling a lead | Skip the mount and fall back to the `render` presentation (details in §12.3) |
| The same mob is applied over and over by several sources | For effects with the same id and the same amplifier, vanilla's merge rule takes the one with the longer remaining duration; no stacking and no doubling (【本文补充】) |
| A third-party mod's custom model ignores the `riding` flag | Degrade to 'rotate only, no seat'; no error, no crash (RT-17) |

---

## 3. Status effect: Confusion

### 3.1 One-line positioning

A status effect that **shows no icon**, **works only on players** and **inverts only the movement-direction components**: pressing W walks you backwards, pressing A walks you right; view, jump, attack and use do not move at all. It is Vibration Bean's settlement result, and this version's only applier writes 80 ticks.

### 3.2 Type and category

| Item | Value | Basis |
|---|---|---|
| Registry id | `farmstorm:confusion` | D-83 |
| Carrier | Status effect (`MobEffect`), the second of this version's `mob_effects` +2 | D-83, construction fact card §3.1 |
| Visibility | **Invisible**: this entry does not appear in the HUD icon row | D-83 |
| Icon | **No icon** texture is registered | D-83 |
| Duration | No duration key; the applier writes it (Vibration Bean writes 80 ticks = 4 seconds) | D-83, D-92 |
| Applies to | **Players only** | D-83 |
| config | No keys (not one line is set for the `confusion` group) | D-83, construction fact card §3.3 |
| Synchronisation | Vanilla effect packet, 0 custom packets | D-50 |

### 3.3 Landing the three restrictions one by one

| # | Restriction (D-83's wording) | How this document lands it | Basis |
|---|---|---|---|
| 1 | Shows no icon | No icon texture at registration; on application both the instance's 'visible' and 'show icon' flags are set false, so the whole entry stays out of the HUD row. The reason is still D-83's: it is not a collectable state, and appearing in the icon row would expose the trap's position. 【本文补充】: the concrete values of the two flags (D-83 gives only the conclusion 'shows no icon' and does not say whether it is 'no entry shown' or 'entry shown but no icon drawn'; to avoid exposing the position, the former is taken) | D-83 |
| 2 | Works only on players | Two sieves: the **applying side** writes the effect only on `Player`; the **consuming side** is hooked only to the local player's movement input (`MovementInputUpdateEvent` only ever fires for the local player anyway). Even if a mob has this effect stuffed into it by outside logic, nothing is presented | D-83; 【本文补充】: the division of labour between the two sieves |
| 3 | Inverts movement input only | Only `Input`'s `forwardImpulse` (W↔S) and `leftImpulse` (A↔D) are inverted; `jumping`, `shiftKeyDown`, view, attack and use do not move at all. Speed is not touched: `Attributes.MOVEMENT_SPEED` is not changed, and nothing outside `Input` is changed | D-83 |

### 3.4 Value table

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Components inverted | `forwardImpulse`, `leftImpulse` (each of the two inverted separately) | count | D-83 |
| Components not inverted | `jumping`, `shiftKeyDown`, view, attack, use | — | D-83 |
| Speed and view | Never changed (no `MOVEMENT_SPEED` write, no `MouseSensitivity` squeeze) | — | D-83 |
| Effect duration (written by the applier) | 80 (= 4 seconds) | tick | D-92; config `vibration_bean.confusionTicks` |
| The effect's own config keys | 0 | — | D-83, construction fact card §3.3 |

### 3.5 Stacking with Q-Bounce: the input lock wins

**Rule** (D-83's wording): when present at the same time as Q-Bounce, **the input lock wins** — while you are locked, 'the opposite direction' means nothing, so it is handled as locked.

Three landing points (【本文补充】):

1. **Explicit early return**: the Confusion segment and segment 1 of the seven-segment lock hang on the same event (`MovementInputUpdateEvent`), and this mod does not guarantee the execution order of the two listeners. So the Confusion segment first checks 'is the lock in force' and `return`s if it is, without inverting — relying not on the coincidence that 'the inverse of 0 is still 0'.
2. **Judged every tick, never cached**: the moment the lock is released while Confusion is still on, the inversion comes back on the next tick.
3. **The view is never inverted** (D-83), so 'the lock wins' does not affect segment 6 of the seven; Confusion does not touch that segment at all.

### 3.6 Boundaries and anomalies

| Scenario | Rule |
|---|---|
| Carrying both Q-Bounce and the Zangief Cane phase lock | All three sources lock, and the lock check is still a single one (segment 1); Confusion takes the early return of §3.5 (details in §12.1, §12.13) |
| Other mods are also changing movement input | This mod does exactly one thing, 'invert'; it does not recompute or overwrite (details in §12.11, RT-19) |
| A spectator / creative player is applied | The applying side sieves only on `Player`, so both are inside; but a spectator has no displacement, so there is nothing to lock (§12.12) |
| The effect is removed early by `/effect clear` | The inversion stops at once and the direction components return to the player's own input on the next tick |

---

## 4. Friend Chives (crop + projectile + seed)

### 4.1 One-line positioning

A Throwable crop with **no vanilla counterpart**: the seed is planted on farmland and ripens into the crop itself, the crop is thrown, and every mob inside a Euclidean sphere of **6.0 blocks** around the landing point gets **10 seconds of Q-Bounce**; as long as this throw hit at least 1 mob, one **audible-at-32-blocks** sound is played at the landing point.

### 4.2 Type and category

| Item | Value | Basis |
|---|---|---|
| Profile number | The 20th of the 22 profiles | `16-DECISIONS-0.0.3.md` §4 |
| Registry id | `farmstorm:friend_chives` | D-84 |
| Type | `THROWABLE` (Throwable) | D-85 |
| Category | `BUFF` (Buff) | D-85 |
| Crop block | `farmstorm:friend_chives_crop` (4 growth stages) | 16 §4 row 20; §4.4 |
| Seed | `farmstorm:chive_seeds` | Same as above |
| `DeployedKind` | None (`null`) | 16 §4 row 20 |
| Projectile entity | `farmstorm:friend_chives` (one of `entity_types` +2, riding `ThrownCropEntity`) | Construction fact card §3.1, D-87 |
| Sound event | `farmstorm:friend_chives` (`sound_events` +1) | D-85, construction fact card §3.1 |
| Chinese name / English name | 朋友的韭 / Friend Chives | 16 §4, §4.1 |
| Vanilla crop counterpart | **None** | D-84 |

### 4.3 Obtaining: seed planting is the only route

- **There is no converter recipe** (D-84). Conversion recipes serve only the reversible mapping 'vanilla crop ⇄ improved crop' (D-52's tag convention, D-54's bidirectional same-shape rule); this crop has no vanilla counterpart, and forcing a one-way recipe would give `ConverterRecipe` a second form.
- **The obtaining route is unique**: `chive_seeds` planted on farmland → ripens → harvested as `friend_chives`. There is no second one (it does not drop from mobs, does not enter loot chests, and enters no output other than the Lucky Berry pool).
- **It still joins the `farmstorm:converter_fuel` tag** (D-84): it can serve as a crafting material for the converter. Tag membership and 'has its own conversion recipe' are two different things — being in the tag ≠ being convertible (11 §11.1 clause 3).
- **`chive_seeds` does not join `converter_fuel`**: it is a sowing item, and belongs to category ① of the 'three categories that stay out of the tag' listed explicitly in 11 §2.5 (【本文补充】: D-84 names only `friend_chives` as joining the tag).

### 4.4 Closed loop: seed → crop block → mature harvest

| Step | Rule | Basis |
|---|---|---|
| Seed source | Harvesting a mature crop (the only source, §4.3) | D-84 |
| Sowing surface | Farmland `minecraft:farmland` (plan line 26, 'planted in the farmland') | 【P-2】 |
| Crop block id | `farmstorm:friend_chives_crop` | 16 §4 row 20 |
| Growth stages | **4 levels** (`age 0 – 3`, property `BlockStateProperties.AGE_3`) | D-84; 【本文补充】: `MAX_CUSTOM_AGE = 3` in the source `block/FarmstormCropBlock.java`, consistent with the '4 placeholder PNGs' of construction fact card §3.4 |
| Maturity check | `age == 3` | D-84; 【本文补充】: the maturity condition in the source `datagen/ModLootTableProvider.java` (`hasProperty(AGE_3, MATURE_AGE)`) |
| Ripening | Bone meal works, carried over from the existing crop-block convention (`FarmstormCropBlock extends CropBlock`) | D-84; 【本文补充】: the same implementation path as the existing 15 planting forms |
| Mature harvest | **1 `friend_chives` + 1 `chive_seeds`** | D-84; 【本文补充】: carried over from v0.0.2's 1 + 1 convention for 'the sowing item and the crop are not the same item' (torchflower / pitcher plant have the same shape) |
| Breaking while immature | Drops only **1 `chive_seeds`** | D-84; 【本文补充】: as above, the existing immature fallback convention |
| Fortune | The relation between crop count and Fortune is carried over from the existing `ApplyBonusCount` convention | D-84; 【本文补充】: no new probability number is added |

An executable state description (【本文补充】): the player right-clicks farmland with `chive_seeds` → an `age 0` block is placed → it grows to `age 3` over random ticks → the mature block is broken → 1 crop + 1 seed come out → the seed returns to the starting point. The whole chain is self-supplying in survival mode; it needs neither a converter nor creative mode.

### 4.5 Projectile value table

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Landing-sphere radius | 6.0 (Euclidean) | block | D-85; config `friend_chives.hitRadius` |
| Q-Bounce duration per mob inside the sphere | 200 (= 10 seconds) | tick | D-85, D-81; config `friend_chives.effectTicks` |
| Audible radius of the hit sound | 32.0 | block | D-85; config `friend_chives.soundRange` |
| Sound event | `farmstorm:friend_chives` | — | D-85, construction fact card §3.1 |
| Sound asset | `pengyoudejiu.mp3` (7,450,588 B) → `friend_chives.ogg` (mono 64 kbps) | — | D-53, D-66, construction fact card §1.3 |
| Projectile lifetime | 100 | tick | existing key `throwable.lifetimeTicks` (11 §10.1) |
| Stack limit | 16 | item | D-16; existing key `stack.throwable` (11 §10.1) |
| Entities affected inside the sphere | Mobs (`LivingEntity`), players and non-player mobs alike, **with no ally/enemy sieve** | — | 【本文补充】: D-85 says only 'mobs' and adds no ally/enemy sieve; consistent with D-27's one-shot AoE convention. This is the **effect side** not sieving, which is a different matter from Point Defense Fungus's interception sieve (D-98) |
| The thrower himself inside the sphere | Gets Q-Bounce too | — | D-85; 【本文补充】: the direct consequence of the row above |

### 4.6 Hit determination and the sound

**Order of determination (all in the same tick, not queued)**:

1. The projectile arrives, and the landing point is taken as the centre of a **Euclidean sphere** of radius 6.0 blocks;
2. every mob inside the sphere is written 200 ticks of `farmstorm:q_bounce`, one by one;
3. count the mobs this throw **actually applied the effect to successfully**, call it n;
4. `n ≥ 1` → play the sound **once** at the landing point; `n = 0` → play nothing (D-85).

Four conventions:

- **There is only one criterion**: n is the applied-successfully count, and no separate 'is there a mob nearby' query is made — two criteria would fight each other (【本文补充】).
- **Played once only**: one throw, one sound, regardless of how many mobs were hit (D-85's 'play the sound once').
- **Where the 32 blocks come from**: vanilla `playSound`'s broadcast radius = 16 blocks × volume, so the volume is taken as **2.0** to reach a radius of 32.0 blocks (【本文补充】: the conversion; the only source of the number 32.0 is `friend_chives.soundRange`, and the key value is not changed).
- **Sound source category**: `SoundSource.PLAYERS`, the same category-choice convention as D-76's looping sound — it does not take up MUSIC and does not cut off the background music.

The sound asset keeps the file name `pengyoudejiu.mp3` unchanged, and the event id is `friend_chives` (16 §4.1 point 3); the video asset's provenance and the non-commercial private-use statement go into the README and the Release per D-66.

### 4.7 Crop block texture: a placeholder, not the final version

- All 4 growth stages of the crop block **use a flat magenta `#D81E8A` placeholder** this round, and **are not final** (construction fact card §3.4, the note under the table in 16 §4). The author says outright 'do not draw the block texture for now, implement the function first' (plan line 27).
- This placeholder **affects textures only**: the registry id, block id, 4 growth stages, drops, tag and `CropProfile` fields are all final; swapping the images changes no value and does not change the blockstate or model structure.
- The item textures (`friend_chives` and `chive_seeds`) already have final assets (1254×1254 RGBA) and are converted to 16×16 into the jar along the existing pipeline (construction fact card §1.3, D-53) — **the placeholder happens in this one place only, the crop block**.
- If the final artwork arrives before release, replace these 4 PNGs directly; if it does not, ship with the placeholder and say so in the Release notes (【本文补充】: consistent with D-53's media-pipeline convention, with no separate branch opened for the placeholder).

### 4.8 Boundaries and anomalies

| Scenario | Rule |
|---|---|
| A miss (no mob inside the sphere) | No sound is played (D-85); the rest of the settlement proceeds as usual, and the projectile is destroyed as usual |
| Only the thrower himself inside the sphere | Counts as a hit (n = 1); the sound plays, and he takes 200 ticks of Q-Bounce and is locked for 10 seconds (D-85's effect side treats every mob inside the sphere alike, which is a different matter from Point Defense Fungus's interception sieve D-98) |
| The landing point is in water / in the air | The same landing procedure as the existing projectiles; no separate landing rule is opened for Friend Chives (the landing algorithm of 11 §5) |
| The crop block is washed away by water / pushed away by a piston | The same rules as the existing crop blocks; no separate protection is set up |
| Breaking an immature crop block | Drops only 1 `chive_seeds`, no crop (§4.4) |
| Burning `friend_chives` as fuel | Legitimate: it is both a member of the fuel tag and a crop in its own right, and burning it by mistake is the player's own loss; nothing intercepts it (the same convention as 11 §2.7) |

---

## 5. Point Defense Fungus (deployable turret)

### 5.1 One-line positioning

A defensive block that intercepts only **this mod's own crops thrown by non-allies**: any non-ally projectile inside a Euclidean sphere of **8.0 blocks** centred on it is shot down in mid-air, each interception using up 1 of its lives, and **after the 6th interception it silently removes itself**; an interception produces particles and a sound only, and **deals no damage, breaks no block and constructs no `Explosion`**.

### 5.2 Type, category and obtaining

| Item | Value | Basis |
|---|---|---|
| Profile number | The 21st of the 22 profiles | 16 §4 |
| Registry id | `farmstorm:point_defense_fungus` (the block and the item share the name) | D-86, the note under the table in 16 §4 |
| Type | `DEPLOYABLE` (Deployable) | D-86 |
| Category | `TURRET` (Turret) | D-86 |
| `DeployedKind` | `POINT_DEFENSE_FUNGUS` (an enum value +1, not a registered object). 0.0.3's `DeployedKind` gains **+2 values** in all (`POINT_DEFENSE_FUNGUS`, `VIBRATION_BEAN`), enum 6 → 8; enum constants are not registered objects, so the registration total is unaffected (the correction is in `18-TECH-0.0.3.md` §1.4(b)) | D-86, the note under the table in 16 §4 |
| Crop block / seed | None / None (all three columns empty) | 16 §4 row 21 |
| Vanilla counterpart | Warped Fungus `minecraft:warped_fungus` | D-86 |
| Conversion recipe | `minecraft:warped_fungus` ⇄ `farmstorm:point_defense_fungus`, **bidirectional, 3 XP, no catalyst** | D-86, D-54 |
| Attachment | On top of a deployed block, carried over from the existing deployed blocks' attachment rule | 16 §5.7 (D-91's new mode applies to Vibration Bean only) |

Filing it under the Turret category is a classification for the player's benefit only; what it actually intercepts is narrowed by §5.4 to this mod's projectiles thrown by non-allies (D-98) — the same technique as C-3's handling of Pumpkin Turret (the category is for the player to see, the determination follows behaviour, D-86's basis).

### 5.3 Value table

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Interception sphere radius | 8.0 (Euclidean) | block | D-87; config `point_defense_fungus.interceptRadius` |
| What it intercepts | **Non-ally** projectiles (the owner is not allied with the placer); allies pass through, neither intercepted nor charged a life | — | D-98 |
| Scan interval | 10 | tick | D-88's basis (a 10-tick staggered scan, not a per-tick scan); RT-20 |
| Interception ceiling | **6** (the block is silently removed after the 6th interception) | time | D-88; config `point_defense_fungus.maxIntercepts` |
| Per-tick deduction limit | Each projectile can be charged at most **1** time per tick | time | D-88 |
| Particles per interception | 8 | count | D-89; config `point_defense_fungus.interceptParticles` |
| Damage dealt by interception | 0 | point | D-89 |
| Blocks broken by interception | 0 | block | D-89 |
| Is an `Explosion` constructed | No | — | D-89 |
| How it is removed when used up | Silent removal, not through the loot table | — | D-88, D-72 |

### 5.4 The interception check: the grounds for 'this mod's non-ally projectiles only'

**What counts**: the surface of determination is a single base class — `entity/ThrownCropEntity.java`. The code that lands it is one line, `instanceof ThrownCropEntity` (D-87's basis: a single `instanceof` covers the whole family, with no item-by-item registration). One **ally/enemy sieve** is laid on top: a projectile is a target only when its owner is not allied with this fungus's placer (D-98); an ally projectile passes through, is not intercepted, and **is not charged a life either**.

Family size (D-98's item-by-item list; this section has checked it one by one against `registry/FarmstormEntityTypes.java`):

| Group | Count | Detail |
|---|---|---|
| Already in 0.0.2 | **10** | `puffed_wheat`, `ubi_server`, `toxic_ubi_server`, `melon_slice_bomb`, `melon_grenade`, `bamboo_firecracker`, `red_mushroom_puff`, `brown_mushroom_puff`, `molotov`, `compressed_puffed_wheat` |
| New in 0.0.3 | **2** | `friend_chives`, `vibration_bean` |
| Total | **12** | The total number of `entity_type`s riding `ThrownCropEntity` |

**The '9 or 10' account has been settled by D-98**: the 'v0.0.2 already has 9 `entity_type`s riding it' in D-87's grounds is a slip of the pen — 9 is **v0.0.1's `entity_types` total** (8 projectiles + 1 `turret_bullet`, which lines up with the '9 → 11' of 11 §1.3), not the number riding that base class. D-98 has corrected it item by item from the source to 0.0.2's 10 + 0.0.3's 2 = **12**, and §5.4 of this document writes 12. **The determination is unaffected**: it is still `instanceof` first, ally/enemy second.

**Why vanilla arrows do not count**: the three product lines are parallel to each other and `instanceof` excludes them by nature, so no 'exclusion list' is needed:

| Vanilla projectile | Its base-class chain | Relation to `ThrownCropEntity` |
|---|---|---|
| Arrow `Arrow`, trident `ThrownTrident` | `AbstractArrow` → `Entity` | A different branch from `ThrownCropEntity` (→ `ThrowableItemProjectile` → `ThrowableProjectile`) |
| Snowball `Snowball`, ender pearl `EnderPearl`, egg, potion | `ThrowableProjectile` → `Entity` | One level above `ThrownCropEntity`, but **not** a subclass of it, so `instanceof` says no |
| Other mods' projectiles | Their own base classes | As above; this mod does not enumerate, register or whitelist them |

Intercepting vanilla arrows would change vanilla combat and the feel of other mods' projectiles, with benefits far below the risk (D-87's basis); this clause is narrower than C-3 (Pumpkin Turret intercepting throwables) and is an internal self-protection mechanism of this mod.

**The ally/enemy sieve** (D-98): only **non-ally** projectiles are intercepted — a projectile is a target only when its owner is not allied with this fungus's placer; the determination is carried over from `TeamResolver` (the vanilla scoreboard-team convention of D-06 in `00-DECISIONS.md`), the same set as the Trap and Turret categories, with no second predicate written. When an ally's projectile (the placer himself, a player on the same team) passes through, it is **not intercepted and no life is charged**, and the 6-life count is unaffected. The 'non-ally projectiles' of plan item 3's original text is made whole by this, and the correction is registered in row 3 of Appendix A.

**Where the projectile goes after being intercepted** (D-99): when the interception holds, the projectile is `discard()`ed at once and **its `onImpact` is not triggered** — no explosion, no block write, no effect application, no drop, which is to say no landing settlement. Otherwise a projectile that is 'intercepted' and still settles its landing as usual is as good as not intercepted, and D-88's 6-time ceiling loses its meaning. Note that this is **entity removal**, not the same path as D-72's 'the block is silently removed after triggering': block removal still goes through D-72 (D-88), entity removal is given by D-99.

### 5.5 How the 6 charges and 'only one deduction per tick' are determined

The two counters sit on two different objects (D-88's basis: the block entity knows only how many times it has been charged itself and cannot know who else is charging in the same tick; the projectile is the only shared object in this determination):

| Data | Where it lives | Type | Role |
|---|---|---|---|
| Interception count | `interceptCount` on the block entity `DeployedCropBlockEntity` | integer, counted from 0 | +1 per successful interception; at 6 it is silently removed (D-88) |
| The 'already intercepted this tick' mark | `lastInterceptTick` on the projectile entity | game-tick number, initially -1 | Same-tick de-duplication (D-88) |

The four steps of the once-per-tick determination (scan interval 10 ticks, RT-20):

1. The block entity itself scans the `ThrownCropEntity`s within a Euclidean 8.0 blocks every 10 ticks (covering loaded chunks only);
2. for each candidate it first checks `instanceof ThrownCropEntity`, then the **ally/enemy** check — if the owner is on the same team as this fungus's placer (or is the placer himself), skip it and **charge no life** (D-98);
3. for a candidate that passes the first two steps, look at `lastInterceptTick == level.getGameTime()`: equal → another fungus already charged it this tick, skip it; not equal → charge 1 of its own, and set the projectile's `lastInterceptTick` to the current tick number;
4. after the charge, immediately play the particles and the sound and `discard()` the projectile (D-99); if this charge brings `interceptCount` to 6, complete this interception first, then `level.removeBlock` to remove itself silently (the D-72 path, not through the loot table).

Three implementation constraints:

- **How 'later fungi skip it and then it is cleared' lands**: the mark stores a tick number rather than a boolean, so it lapses by itself as soon as the next tick's number differs; no extra clearing code is needed (【本文补充】).
- **Order**: 'charge the count → set the mark → play the particles → discard the projectile' completes within the same tick; the mark is set after the charge is taken so that no second fungus can charge in the same tick (【本文补充】).
- **No rollback**: a failure at any step does not roll back a charge already taken — 'intercepted' is an accomplished fact, and rolling it back would let the same projectile be charged twice (【本文补充】).

### 5.6 The interception effects are purely visual

D-89's three prohibitions landed one by one:

| Prohibition | How it lands | Basis |
|---|---|---|
| Deals no damage | `hurt` is not called: an interception raises no damage event at all, so the player loses no health, gains no invulnerability frames and produces no death message | D-89 |
| Breaks no block | No block state is touched; removing itself when used up goes through `level.removeBlock`, which is 'removing itself', not 'breaking someone else' | D-89, D-88 |
| Constructs no `Explosion` | Not even D-58's 'power-0 trigger' is used — no trigger is needed here at all, because the landing point is the projectile's own position | D-89 |

Only two things are left: `interceptParticles` (8) particles + one sound. The sound **adds no `sound_events`** (this version's `sound_events` increment is only +1, Friend Chives's hit sound, construction fact card §3.1) and uses a vanilla sound event that already exists instead (D-100); which event exactly is reused, and its volume and pitch, are pinned once by the sound table of `18-TECH-0.0.3.md`.

### 5.7 Boundaries and anomalies

| Scenario | Rule |
|---|---|
| No non-ally projectile of this mod inside the sphere | Nothing happens, no life is charged, and the block stays idle |
| Reached by N Point Defense Fungi in the same tick | Only **1** of them has a life charged (§5.5 step 2), and the others can only charge another projectile on a later tick |
| A vanilla arrow / trident / snowball / ender pearl passes through | Never intercepted (D-87), and no particles or sound are triggered |
| A crop thrown by an ally player | **Not intercepted**, and no life is charged (D-98); when the sphere holds only ally projectiles, the block stays idle |
| The 6th interception | Complete this interception first, then remove itself; the removal produces no drops (D-88, D-72) |
| The player mines the Point Defense Fungus himself | Goes through the ordinary loot table (drops 1 of itself), which is a different path from 'disappears when used up' |
| The chunk unloads in the middle of an interception | A life already charged is not rolled back; the scan covers loaded chunks only (details in §12.4) |
| The attachment block is destroyed | The existing deployed blocks' `supportsFloating` convention applies as before (kept and marked after the attachment is lost), because D-91's new mode applies to Vibration Bean only (16 §5.7) |

---

## 6. Vibration Bean (Throwable trap)

### 6.1 One-line positioning

A bean that turns into an **unattached invisible trap** when thrown: it **attaches to no block at all** and exposes its position through particles alone; a non-ally player who comes **strictly within less than 4.0 blocks** triggers it, **all players** within a 4.0 radius (allies included, see §6.5) get **4 seconds of Confusion**, one explosion sound is played at the trap, and then it is silently removed once, with no drop.

### 6.2 Type, category and obtaining

| Item | Value | Basis |
|---|---|---|
| Profile number | The 22nd of the 22 profiles | 16 §4 |
| Registry id | `farmstorm:vibration_bean` (the projectile entity and the trap block share the name) | D-90, D-91 |
| Type | `THROWABLE` (Throwable) | D-90 |
| Category | `TRAP` (Trap) | D-90 |
| `DeployedKind` | **`VIBRATION_BEAN`** (a new enum value; the block is still placed at the landing point by the projectile itself, but kind must be supplied, otherwise `kindOf()` throws) | Construction fact card §3.2 point 2; the correction in `18-TECH-0.0.3.md` §1.4(b) |
| Crop block / seed | None / None (all three columns empty) | 16 §4 row 22 |
| Vanilla counterpart | Cocoa Beans `minecraft:cocoa_beans` | D-90 |
| Conversion recipe | `minecraft:cocoa_beans` ⇄ `farmstorm:vibration_bean`, **bidirectional, 3 XP, no catalyst** | D-90 |
| Trap block | `farmstorm:vibration_bean` (one of `blocks` +3) | D-91, construction fact card §3.3 |
| Block entity | Reuses the existing `DeployedCropBlockEntity` (`block_entities` +0) | D-91, construction fact card §3.1 |

### 6.3 Value table

| Parameter | Value | Unit | Basis |
|---|---|---|---|
| Trigger distance | 4.0, **strictly less than** | block (Euclidean) | D-92, D-101; config `vibration_bean.triggerRadius` |
| Confusion radius | 4.0, **likewise strictly less than** | block | D-92, D-101; the '4 / 4' row of 16 §8 |
| Confusion duration | 80 (= 4 seconds) | tick | D-92; config `vibration_bean.confusionTicks` |
| Explosion sound count | 1 (reuses a vanilla sound event that already exists, adds no `sound_events`) | time | D-92, D-100 |
| After triggering | Silently removed, 0 drops | — | D-92, D-72 |
| Attachment | Attaches to no block at all | — | D-91 |
| How its position is exposed | Particles only | — | D-91 |
| Particle cadence | Once every 10 ticks | tick | D-91; 【本文补充】: the same order of magnitude as the deployed blocks' scan interval; the `vibration_bean` group has no particle-related key (construction fact card §3.3) |
| Projectile lifetime | 100 | tick | existing key `throwable.lifetimeTicks` (11 §10.1) |

### 6.4 How the 'unattached' mode differs from the existing `supportsFloating`

D-91 says outright that `supportsFloating` **may not** be reused — that one is 'tolerate the loss of the attachment', a different meaning. The differences between the two are listed one by one:

| Dimension | The existing `supportsFloating` (0.0.2) | Vibration Bean's 'unattached' mode (new in 0.0.3) |
|---|---|---|
| Premise | The block first **attaches legally** to a supporting face | From the moment of landing there **is no** supporting face |
| `canSurvive` | Still runs the support check; only a lost attachment turns `FLOATING` on | Always true: no support check, no FACE-direction check |
| `updateShape` | The supporting side disappears → set `FLOATING` and `markDetached()` | Always returns itself: a neighbour change in any direction changes no state |
| FACE support check | Takes part (`supportPos` / `supportDirection`) | Does not take part — there is no notion of an 'attachment face' |
| Removal condition | The attachment is lost and `supportsFloating` is off → removed, with `deploy.dropOnSupportLost` deciding whether it drops | Only when the player mines it, when a trigger settles it, or when `/setblock` overwrites it |
| Meaning | '**Tolerate** the loss of the attachment' | '**Not attached** from the very start' |
| Basis | 0.0.2's `block/DeployedCropBlock.java`; 16 §5.7 | D-91, RT-21 |

Three landing constraints:

1. **They may not be merged** (D-91, 16 §6 clause 9): merging them would change the attachment semantics of the existing 5 deployed blocks along with it, while 16 §5.7 states outright that 'the behaviour and appearance of the existing deployed blocks do not change'.
2. **Three branch points** (D-91): `canSurvive` always true, `updateShape` returns itself, the FACE support check short-circuits. All three are switched by one 'mode flag' and **act on this one block, `farmstorm:vibration_bean`, only**.
3. **Block entity reused**: no new `block_entities`; the existing `DeployedCropBlockEntity` is still used (construction fact card §3.1).

### 6.5 Trigger and settlement

**Trigger condition**: the **Euclidean distance from a non-ally player to the centre of the trap block is strictly less than 4.0 blocks** (D-92). 'Strictly less than' is hard wording — exactly 4.0 blocks does not trigger (plan line 50's original text is also 'less than 4').

**Division of labour between the two sides**:

| Side | Who it recognises | Basis |
|---|---|---|
| Trigger side | Recognises **non-ally players** only (following D-07's ally/enemy convention); creative and spectator players carry over the existing traps' exemption and do not trigger it | D-92; 【本文补充】: the creative/spectator exemption (D-92 did not restate it item by item, so D-71's existing convention is followed) |
| Effect side | **All players** within a radius of 4.0 blocks, **with no ally/enemy sieve** — stepping on it as an ally still confuses you. This is the effect side D-101 governs (following D-27's one-shot AoE convention), a different matter from Point Defense Fungus's interception sieve (D-98) | D-92, D-101 |

**The endpoint convention** (D-101): the effect side and the trigger side share the same 4.0 ruler (D-92's original grounds read 'two uses of the same ruler'), and both sides use **strictly less than 4.0**; one side may not write '< 4' while the other writes '≤ 4'. If the endpoints disagree, you get the self-contradictory result 'standing at exactly 4.0 blocks did not trigger, yet you were confused anyway'. There is only one config key, `vibration_bean.triggerRadius` (4.0), and both sides share it.

**The four steps within one tick** (not queued, not split over frames):

1. test the trigger (a non-ally player, Euclidean distance < 4.0);
2. apply 80 ticks of `farmstorm:confusion` to every player within a radius of 4.0;
3. play the explosion sound **once** at the trap (reusing a vanilla sound event that already exists, D-100; the sound only, with no `Explosion` constructed, to avoid confusion with D-58's power-0 trigger);
4. `level.removeBlock` to remove the block silently, producing no drops and not touching the loot table (D-72).

**One-shot**: once the block is removed a second trigger is impossible; when several players satisfy the condition in the same tick, it settles only once (the block is already gone) (【本文补充】).

### 6.6 Boundaries and anomalies

| Scenario | Rule |
|---|---|
| The projectile lands in the air (there is no landing point to stand on) | The unattached trap block is placed as usual — this mode does not attach in the first place (D-91) |
| The unattached block meets a block update / water / a piston | It is not removed (details in §12.5, RT-21) |
| The chunk holding the block entity is unloaded when the trap is triggered | No tick, no determination; it works as usual once reloaded (the same treatment as §12.4) |
| An ally player triggers it | It triggers all the same and he is confused all the same (§6.5's effect side includes allies, D-101) |
| A creative / spectator player comes near | No trigger (§6.5's trigger-side exemption) |
| Two traps are both stepped on in the same tick | Each settles on its own; the two 80-tick Confusions follow vanilla's same-id merge rule and the longer one is taken, with no doubling (details in §12.6) |
| The player mines the trap | Goes through the ordinary loot table (drops 1 of itself); a different path from 'disappears after triggering' |

---

## 7. Fang Trap Cactus (a rework of existing content)

### 7.1 One-line positioning

**The first of v0.0.3's two changes to existing implementation.** The Chinese name changes from 赢过输过没死过 to 刺球, the trigger distance tightens to **≤ 2.5 blocks** (the comparison operator is part of the decision), the delays of the two rings of fangs are pressed from 10 / 4 ticks to 4 / 2 ticks, and players hit by fang damage are given **Slowness V for 60 ticks**. The registry id, the English name, the crop block id, `DeployedKind`, the loot table, both ring counts and both distribution radii **all stay put**.

### 7.2 Scope of the rename (D-93)

| Item | 0.0.2 current value | 0.0.3 | Changed |
|---|---|---|---|
| registry id | `fang_trap_cactus` | `fang_trap_cactus` | No |
| Crop block id | `fang_trap_cactus_block` | Same | No |
| `DeployedKind` constant | `FANG_TRAP_CACTUS` | Same | No |
| English display name (C-10) | Fang Trap Cactus | Fang Trap Cactus | No |
| Chinese display name (the value in `zh_cn.json`) | 赢过输过没死过 | **刺球** | **Yes (the only one)** |
| Language key count and key set | Same as English | Same as English | No (only the value changes; no key added or removed) |
| Loot table | Mined, drops 1 of itself | Same | No |
| Fang entity and its ownership | Vanilla `EvokerFangs`, owned by the deployer | Same | No |

Three conventions:

1. **This is one explicit exception registered against C-10** (Chinese and English no longer translate into each other). C-10's 'display name = the registry id in Title Case' meets its first exception on the Chinese side, and it must be registered, otherwise a later hand will take the Chinese name for a typo and change it back (D-93).
2. **The id may not be changed on the side** (16 §6 clause 2): changing the id drags the block id, the `DeployedKind` constant, the loot table, the recipe and tag references along with it, and it would also leave the block in existing saves without its counterpart.
3. **What changes is not a profile field**: the Chinese name lives in the language file, and not one of `CropProfile`'s eight fields is touched (the note under the table in 16 §5.2).

### 7.3 Value-difference table (D-94's original table, its 8 rows moved in as they are)

| Item | Current value | 0.0.3 |
|---|---|---|
| Trigger distance (Euclidean) | 3.0 | **≤ 2.5** |
| Outer-ring fang count | 8 | 8 (unchanged) |
| Outer-ring delay | 10 ticks | **4 ticks (0.2 seconds)** |
| Outer-ring distribution radius | 2.0 | 2.0 (unchanged) |
| Inner-ring fang count | 5 | 5 (unchanged) |
| Inner-ring delay | 4 ticks | **2 ticks (0.1 seconds)** |
| Inner-ring distribution radius | 1.0 | 1.0 (unchanged) |
| On-hit effect | None | **Slowness V (amplifier 4), 60 ticks** |

Five points:

- This table is **word-for-word identical** to the table in `16-DECISIONS-0.0.3.md` §5.3 and is this section's only value convention; it has **8 data rows** in all, the same as D-94's original table in construction fact card §2, with no row added or removed.
- Of the 8 rows, **4 change** (trigger distance, outer-ring delay, inner-ring delay, on-hit effect) and **4 are marked unchanged** (outer-ring count, outer-ring radius, inner-ring count, inner-ring radius).
- **The inner ring still comes out first and the outer ring still after** (the order is unchanged): the outer ring is pressed from 10 ticks to 4 ticks and the inner ring from 4 ticks to 2 ticks, and the two rings are still separated by **2 ticks**.
- Both the unit word and the comparison operator of the trigger distance are written out in full: 0.0.2's C-5 records '≤ 3', and this clause changes only the **value** to 2.5, leaving the **distance measure** (Euclidean) and the **comparison operator** (≤) unchanged (the C-5 row of 16 §7.3); the note in 16 §5.3 says outright that 'the operator is part of the decision, do not write the number alone', so the 0.0.3 column of this table reads **≤ 2.5** rather than 2.5.
- Slowness V's amplifier is **4**, because vanilla levels count from amplifier 0 (level I) (D-69's conversion); 60 ticks = 3 seconds.

### 7.4 The four 'unchanged' rows are decision boundaries too

| Unchanged item | Value | Why it has to stay in the table |
|---|---|---|
| Outer-ring fang count | 8 | D-94 authorises only 4 changes. Changing the count amounts to changing the trap's shape and is not authorised (D-94's original grounds: 'the counts and the two distribution radii stay put without exception') |
| Outer-ring distribution radius | 2.0 | The radius is the visual parameter of 'how far the fangs spread', and changing it amounts to changing the whole trap's shape (D-94's grounds) |
| Inner-ring fang count | 5 | As above, not authorised |
| Inner-ring distribution radius | 1.0 | As above; D-30 is fully in force and the fangs' distribution radius still does not change (the D-30 row of 16 §7.3) |

In one sentence: **these 4 rows marked unchanged are themselves decision boundaries and may not be changed on the side**. Writing 'unchanged' into the table as well is so that a later hand sees at a glance that 'these four were pinned in writing', rather than 'nobody ever mentioned these four'.

### 7.5 The definition of 'hit' (D-95)

- **Definition**: it turns on the vanilla `EvokerFangs` **dealing damage** to the player. A player settled by that damage gets Slowness V for 60 ticks; a player not hit by the damage **is not given it**.
- **The entry point of the determination is not a geometric recomputation**: 'is he inside the Euclidean 2.5 circle' is not checked a second time, and no bounding box is used. The determination falls on the damage-settlement chain, and all three must hold at once: ① the entity the damage came from is an `EvokerFangs`; ② that fang's owner is the deployer of this trap; ③ the wounded party is a player.
- **Why**: the fangs are vanilla entities, and their damage determination already carries invulnerability frames, armour and resistance settlement; a second 'inside the circle or not' system would conflict with it and produce the case where 'the animation bit you but no health was actually lost' and you were slowed anyway (D-95's grounds).
- **The ownership does not change**: the kill credit is still the deployer's (carried by `EvokerFangs`'s owner field), and this version makes no change to it (D-95).
- **One corollary** (【本文补充】): creative-mode players and spectators cannot be damaged by `EvokerFangs`, and therefore cannot be slowed either — this is the direct consequence of 'damage is the authority', and no separate exemption branch is opened for it (§12.12 has the same origin).

### 7.6 Knock-on changes

| Location | 0.0.2 | 0.0.3 | Basis |
|---|---|---|---|
| config `fang_trap_cactus.triggerRadius` | 3.0 | **2.5** | D-94 (changes an existing key's default) |
| config `fang_trap_cactus.outerDelayTicks` | 10 | **4** | D-94 (changes an existing key's default) |
| config `fang_trap_cactus.innerDelayTicks` | 4 | **2** | D-94 (changes an existing key's default) |
| config `fang_trap_cactus.slownessDurationTicks` | Does not exist | **60** (new key) | D-94, construction fact card §3.3 |
| config `fang_trap_cactus.slownessLevel` | Does not exist | **5** (new key) | D-94, construction fact card §3.3 |
| On-hit effect | None (no potion effect is applied at all) | Slowness V (amplifier 4), 60 ticks | D-94, D-95 |
| Trigger-check entry point | Euclidean 3.0 (≤) | Euclidean **≤ 2.5**, every other part of the check unchanged | D-94 |
| Language files | Chinese value = 赢过输过没死过 | Chinese value = 刺球; key set and key count unchanged | D-93 |

**The 4 existing keys that do not move** (【本文补充】: checked one by one against `config/FarmstormConfig.java`; not one of these four keys has its default changed): `fang_trap_cactus.outerFangCount` (8), `fang_trap_cactus.outerRadius` (2.0), `fang_trap_cactus.innerFangCount` (5), `fang_trap_cactus.innerRadius` (1.0).

### 7.7 Boundaries and anomalies

| Scenario | Rule |
|---|---|
| The fangs' landing point has no ground | Generation is not skipped for 'no support' (details in §12.7) |
| The player does not enter even the inner ring | No fang is generated at all, and the block stays idle |
| The player runs out of the ring in the 2-tick interval | The fangs' coordinates were already computed from the deployment point and do not chase; run out and you are not hit (【本文补充】: the fangs are a positional effect, not a homing one) |
| The target is inside an invulnerability window | No damage → no Slowness (the direct consequence of D-95) |
| A creative / spectator player steps on it | No trigger (carried over from the existing trap exemption); even if it did trigger they would get no Slowness, because they can get no damage (§7.5) |
| The fangs' owner is offline | The fangs are generated as usual, and the ownership is determined by the last known team (the same convention as 11 §6.11) |

---

## 8. Zangief Cane (a rework of existing content)

### 8.1 One-line positioning

**The second of v0.0.3's two changes to existing implementation.** Every existing feature of 0.0.2 is kept (phase timing, rise, float, spin, invisibility, trigger check, silent removal, sound, input lock, camera), and only one segment changes: **during phase 2's fall the target's natural fall damage is cancelled, and on landing a flat 7.5 points is applied**, with a 3rd damage type added for it.

### 8.2 What changed

| Item | 0.0.2 | 0.0.3 | Basis |
|---|---|---|---|
| Natural fall damage during phase 2's fall | In effect (the controller has no damage or fall hook of any kind) | **Cancelled**, only during this trap's phase 2 | D-96 |
| Landing damage | None | **A flat 7.5 points** | D-96 |
| damage type | `farmstorm:crop_blast`, `farmstorm:void_bolt` (2 in all) | **`farmstorm:zangief_slam`** added (the 3rd) | D-96 |
| config `zangief_cane.slamDamage` | Does not exist | **7.5** (new key) | D-96; construction fact card §3.3 |
| config `zangief_cane.cancelFallDamage` | Does not exist | **true** (new key, boolean) | D-96; construction fact card §3.3 |
| Damage ownership | The trap's owner | **Same** | D-96 |

7.5 points = **3.75 hearts** (§0.3's unit conversion). This change is an **addition**, not a refit: in 0.0.2's `behavior/ZangiefPiledriverController.java` all three of `causeFallDamage` / `LivingFallEvent` / `hurt` have 0 hits (construction fact card §1.2) — there was no fall hook at all — so both the 'cancel' and the 'settle' segments are new code.

### 8.3 The do-not-touch list (the rest of 0.0.2's features, not one of which may move)

| # | Item | Current value | Basis |
|---|---|---|---|
| 1 | Deployment form | Deployed block, top face only | D-96 (the do-not-touch list); 11 §6.4, §6.2 |
| 2 | Invisible in the world | Empty model + only `textures.particle` kept; the item icon goes through `item/generated` separately | D-68 |
| 3 | Phase timing | Phase 0 = 14 ticks, phase 1 = 15 ticks, phase 2 starts spinning after a 4-tick delay | D-69, 11 §6.6 |
| 4 | Rise | A servo lift of 16 blocks / 15 ticks (`MAX_RISE_SPEED = 4.0`) | D-96 (the do-not-touch list); 11 §6.4 |
| 5 | Float | `amplifier 6`, 20 ticks, applied on tick 14 | D-69 |
| 6 | Spin | 72 degrees/tick, counter-clockwise | D-70 |
| 7 | Trigger check and radius | Recognises non-ally players only (no mobs), Euclidean 1.5 blocks | D-71, D-73 |
| 8 | Silent removal | Removed the moment it triggers, producing no drops | D-72 |
| 9 | Sound | `farmstorm:zangief_piledriver`, played once | D-66 |
| 10 | Input lock | From the trigger tick → the end of phase 2, seven-segment | D-60, D-79 |
| 11 | Camera | `roll 180°` + inverted pitch + phase 2's yaw at 72 degrees/tick | D-59, D-70 |
| 12 | Scan interval | Every 10 ticks (`DeployedKind.ZANGIEF_CANE(10)`) | D-96 (the do-not-touch list); 11 §6.4 |

This table serves the same purpose as §7.4: to prove that **only the one segment in §8.2 changed**. 0.0.2 is an implemented and released version (the 814,935 B jar), and changing its existing behaviour amounts to changing the actual player-side experience (16 §5.6).

### 8.4 The paired constraints on cancelling and settling

1. **The scope of the cancellation**: it takes effect only when 'the target is in phase 2 **and** carries this trap's `farmstorm:zangief_piledriver` (amplifier 2)'; it uses an **effect-existence** check, does not change the global fall logic, and does not touch other mods' behaviours such as slow falling, immunity or flight (D-96, RT-22).
2. **When it settles**: at the moment of landing (when phase 2's landing check succeeds) apply **7.5 points** of `farmstorm:zangief_slam`, with the source credited to the **trap's owner** (D-96).
3. **The two must come as a pair**: cancel first, settle second, both steps in the same phase state machine. A missing cancellation = 'flat 7.5 + natural fall' stacked, damage drifting with height, which contradicts the word 'flat'; a missing settlement = 0 damage (the closing paragraph of 16 §5.5).
4. **Of the four ways of ending, only 'landing' settles** (【本文补充】): phase 2 has four termination conditions — landing, `MAX_FALL_TICKS` (100 ticks) running out, the target dying, and the effect being cleared; the other three **do not apply** the 7.5, because D-96's original text ties the damage to the one event 'on landing'.
5. **The damage-type trio**: register `damage_types` +1, the data pack file `data/farmstorm/damage_type/zangief_slam.json`, and the death message and language keys following the language-key table of `18-TECH-0.0.3.md` (D-96). The purpose of a separate new type is to make this blow distinguishable in damage source (death message, resistance, statistics); reusing either of the existing two would record it as blast or void damage.

### 8.5 Boundaries and anomalies

| Scenario | Rule |
|---|---|
| The target dies in phase 2 | The 7.5 is not settled, and the fall-damage cancellation lapses along with the effect (details in §12.8) |
| Another mod also cancels fall damage during phase 2 | This mod's 7.5 still settles only once (details in §12.9, RT-22) |
| The target gains flight / levitation during phase 2 | No landing → it ends through `MAX_FALL_TICKS`, and the 7.5 is not settled (§12.9) |
| The target falls into the void | It is unlocked after `MAX_FALL_TICKS` and the 7.5 is not settled; no 'end it forcibly by y coordinate' patch is made (11 §11.5) |
| The target is re-applied by another Zangief Cane during phase 2 | The effect is re-issued as `amplifier 0`, which amounts to starting a fresh round; this round's fall-damage cancellation is void with it (the same convention as 11 §6.11) |

---

## 9. Extending the Lucky Berry random pool

### 9.1 Conclusion

| Item | Value | Basis |
|---|---|---|
| The v0.0.2 pool | 19 profiles − 3 = **16** | D-55, 11 §11.11 |
| The v0.0.3 pool | 22 profiles − 3 = **19** | D-97 |
| Added this time | **+3**: `friend_chives`, `point_defense_fungus`, `vibration_bean`, **all of them included** | D-97 |
| Exclusions | Still `lucky_berry`, `wild_apple`, `compressed_puffed_wheat`, **three items, not one more** | D-97, 16 §6 clause 11 |
| Derivation | **Unchanged**: the pool = `CropProfile.ALL` minus those three (a streaming derivation, not a list) | D-97, D-55 |
| Code | The pool constant in `util/CropProfile.java` is not changed; `util/LuckyBerryRoller.java` is not changed | D-97 |
| config | **No new keys** (the pool's contents do not go into config) | D-97, D-55 |

### 9.2 Where the pool's members come from

- The pool is implemented as a **derivation**, '`CropProfile.ALL` minus three', not a hand-written list. As the profiles go from 19 to 22, the pool naturally goes from 16 to 19 — the inevitable result of having chosen a derivation in the first place, and precisely what it is there for (D-97's grounds).
- None of the three new profiles has the power standing of Wild Apple: Friend Chives is a projectile + a buff, Point Defense Fungus is a defensive block, and Vibration Bean is a one-shot trap. Going into the pool does not create the imbalance of '1 berry buys this version's strongest item' (D-97's grounds).
- Conversely, adding exclusions for them one by one would degrade the derivation into a list that has to be maintained version by version, the opposite of D-55's intent (D-97).

### 9.3 The consequence: the pool now contains Friend Chives (deliberate, not a hole)

**This subsection is the body of this section, not a footnote.**

1. **Friend Chives can come out**: eating one Lucky Berry has a chance of yielding 1 `friend_chives` projectile outright.
2. **Throw it and it is 10 seconds of full lock**: mobs inside the landing sphere (the thrower himself included) get 200 ticks of Q-Bounce and, at the same time, **every action is locked by the seven-segment lock** (§2.6). So there is a complete, reproducible path: **eat one berry → throw it at yourself → be locked for 10 seconds**.
3. **This is a consequence D-97 explicitly requires to be written out**, and it is **deliberate**, not a hole: the author asked this time for all three new profiles to be included; the landing point of D-97 reads literally 'the consequence must be written out: a Lucky Berry can produce Friend Chives, which on landing is 10 seconds of Q-Bounce and locks every action'.
4. **The other two consequences** (【本文补充】): getting Point Defense Fungus = 1 defensive block, with no side effect; getting Vibration Bean = 1 one-shot trap that only catches you if you step on it yourself.

**Three remedies that are not made**: no exemption for 'the player locking himself', no new exclusion for the pool, and no change to Q-Bounce's duration (16 §6 clauses 6, 11; D-81, D-97). To change any one of them, change 16 first, then change the code.

### 9.4 One easy mistake

The pool is **19**, not 22 (22 is the number of rows in the profile table), and not 16 (16 is 0.0.2's pool). The three numbers belong to three different objects: the number of profiles, the number of pool members, and the number of pool members in the previous version. `18-TECH-0.0.3.md`'s startup self-check text is written from the pool's measured member count (D-97's landing point).

---

## 10. Advancement list

### 10.1 The three new advancements

All 3 are new and hang under the existing advancement tree; the existing **15** **do not move** (16 §5.7). After adding them the total is **18** (15 + 3; 15 comes from 11 §9.1, 3 from this table).

| # | ID | Display name (zh / en, final values) | Parent advancement | Trigger condition | Basis |
|---|---|---|---|---|---|
| 16 | `farmstorm:first_q_bounce` | 弹起来了 / Q-Bounce | `farmstorm:root` | Any player first gains `farmstorm:q_bounce`, **with the source unconstrained** (a Friend Chives throw, a Lucky Berry's Friend Chives thrown again, `/effect`, all count) | D-63 (extended this time); the parent advancement and the display name are 【本文补充】 |
| 17 | `farmstorm:point_defense_intercept` | 一颗都不放 / Not One Through | `farmstorm:first_deploy` | Any one Point Defense Fungus first intercepts a **non-ally** `ThrownCropEntity` successfully (it holds as soon as the 1st deduction takes effect; an ally passing through charges no life and does not advance this advancement) | D-63, D-87, D-98; the parent advancement and the display name are 【本文补充】 |
| 18 | `farmstorm:vibration_bean_trigger` | 留个纪念 / A Souvenir | `farmstorm:first_deploy` | A Vibration Bean is first stepped on by a player and completes its settlement (both applying Confusion and the silent removal are carried out) | D-63, D-92; the parent advancement and the display name are 【本文补充】 |

Carried over from `01-DESIGN.md` §6: `frame = task`, `show_toast = true`, `announce_to_chat = false`, and the icon uses the corresponding item (`friend_chives` / `point_defense_fungus` / `vibration_bean`); the advancement determination is entirely on the server, with no custom command (D-24). The final value of the display names follows the language-key table of `18-TECH-0.0.3.md` §7.3.

### 10.2 Three details of the trigger conventions

1. **`first_q_bounce` counts only 'gaining the effect', not 'who applied it'**: the same convention as 0.0.2's `first_wild_hunter` (11 §9.2 clause 3). So Friend Chives produced by a Lucky Berry and then thrown also advances this advancement (【本文补充】).
2. **`point_defense_intercept` counts only 'the 1st successful interception'**: which kind of projectile it intercepted is irrelevant, and disappearing when used up after the 6th interception does not affect this advancement. Because an interception `discard()`s the projectile (§5.4), this advancement is at the same time the only observable evidence that 'the Point Defense Fungus really did work' (【本文补充】).
3. **`vibration_bean_trigger` counts only 'the settlement completed', not 'who stepped on it'**: an ally player triggering it advances it too — the effect side treats every player inside the radius alike (§6.5, D-101).

### 10.3 Relation to the existing 15

| Relation | Explanation |
|---|---|
| Parent advancements | The three new advancements hang under `farmstorm:root` (the existing 1st) and `farmstorm:first_deploy` (the existing 7th) respectively, with no new root node |
| No mutual effect | Not a word of the existing 15's trigger conditions changes, and the new advancements share no criteria with the existing ones |
| Count | 15 → 18 (3 added, none deleted or changed); 16 §5.7 and D-63 record these three as additions rather than changes, and F-133 of `19-TASKS-0.0.3.md` enforces the same count 15 → 18 as a hard task |

---

## 11. config reference table

**This section only references**: it lists key names and default values, and nothing else. **The full definitions and ranges are in Section 8 of `18-TECH-0.0.3.md`** — the range column, the comment text and the `farmstorm.configuration.*` language keys are defined once in that document, and this one does not repeat them.

### 11.1 Purely new keys (16)

Key names are always written `<group>.<key>` (the notation of ruling A-04), and the language key is written `farmstorm.configuration.<group>.<key>`.

| # | Key (`<group>.<key>`) | Default | Basis |
|---|---|---|---|
| 1 | `qbounce.poseMode` | `render` | D-82, D-102 |
| 2 | `qbounce.rotateDegreesPerTick` | 24.0 | D-81 |
| 3 | `qbounce.squashAmplitude` | 0.15 | D-81 |
| 4 | `qbounce.squashPeriodTicks` | 6 | D-81 |
| 5 | `friend_chives.hitRadius` | 6.0 | D-85 |
| 6 | `friend_chives.effectTicks` | 200 | D-81, D-85 |
| 7 | `friend_chives.soundRange` | 32.0 | D-85 |
| 8 | `point_defense_fungus.interceptRadius` | 8.0 | D-87, D-98 |
| 9 | `point_defense_fungus.maxIntercepts` | 6 | D-88 |
| 10 | `point_defense_fungus.interceptParticles` | 8 | D-89, D-100 |
| 11 | `vibration_bean.triggerRadius` | 4.0 | D-92, D-101 |
| 12 | `vibration_bean.confusionTicks` | 80 | D-92, D-101 |
| 13 | `fang_trap_cactus.slownessDurationTicks` | 60 | D-94 |
| 14 | `fang_trap_cactus.slownessLevel` | 5 | D-94 |
| 15 | `zangief_cane.slamDamage` | 7.5 | D-96 |
| 16 | `zangief_cane.cancelFallDamage` | `true` | D-96 |

**16 rows in all.** There is no 'Confusion' key in the table: the `confusion` group sets no key at all, and the duration is given by the applier (D-83, the 'no keys' row of construction fact card §3.3) — that row is not a key and does not count towards this table.

### 11.2 Keys whose existing default changes (3)

| # | Key (`<group>.<key>`) | 0.0.2 | 0.0.3 default | Basis |
|---|---|---|---|---|
| 1 | `fang_trap_cactus.triggerRadius` | 3.0 | **2.5** (the check's operator is ≤, see §7.3) | D-94 |
| 2 | `fang_trap_cactus.outerDelayTicks` | 10 | **4** | D-94 |
| 3 | `fang_trap_cactus.innerDelayTicks` | 4 | **2** | D-94 |

These three are the **only** existing keys whose default v0.0.3 changes (16 §5.4); not one other existing key's default moves.

### 11.3 Existing keys reused (reference only, not one default changes)

| Key (`<group>.<key>`) | Default | Where this document uses it | Basis |
|---|---|---|---|
| `stack.throwable` | 16 | §4.5 Friend Chives stacking | D-16; 11 §10.1 |
| `throwable.lifetimeTicks` | 100 | §4.5, §6.3 projectile lifetime | 11 §10.1 |
| `converter.xpCost` | 3 | §5.2, §6.2 the two conversion recipes | D-12, D-86, D-90 |
| `converter.netherStarForwardCost` | 1 | As above (neither conversion this round consumes it) | D-12 |
| `deploy.supportMustBeFullOpaque` | `true` | §5.2 Point Defense Fungus's attachment check | source `config/FarmstormConfig.java` |
| `deploy.dropOnSupportLost` | `true` | §5.7 whether Point Defense Fungus drops itself when it loses its attachment | G-9, 11 §10.1 |

**Three numbers**: 16 new + 3 changed = **19 keys**; the 1 extra 'Confusion has no keys' line does not count (construction fact card §3.3). The range column and the comment text are defined once, all of them in Section 8 of `18-TECH-0.0.3.md`.

---

## 12. Anomaly and boundary handling list

The numbering follows the style of 11 §11, and there are **14 entries** in all. Each writes only the rule and its grounds; the numbers are looked up in the individual sections.

### 12.1 Q-Bounce and Confusion stacked

- **Rule**: **the input lock wins** (D-83). With both present the movement components are 0 and no inversion is settled; the Confusion segment checks the lock first and `return`s if it is in force (§3.5).
- **Not relying on coincidence**: 'the inverse of 0 is still 0' is only a mathematical coincidence, and the implementation must return early explicitly — the two listeners hang on the same event and this mod does not guarantee their execution order (【本文补充】).
- **Released means restored**: the moment the lock is released while Confusion is still on, the inversion is back on the next tick (judged every tick, not cached).
- **The view is unaffected**: Confusion never touches segment 6, so 'the lock wins' does not affect the view lock (D-83).
- **Basis**: D-81, D-83, RT-19.

### 12.2 A player dying / quitting while Q-Bounce is on

- **Death**: the death screen is the **deliberate exception** of segment 7 and opens as usual (otherwise the player is stuck with no UI); the effect disappears with the entity's removal → the seven-segment lock is released at once and the trio's accumulated values drop to zero; the new entity after respawn inherits no state (D-79, D-81).
- **Quitting**: the effect is saved with the player data and continues for its remaining duration on reconnect (vanilla `MobEffectInstance` behaviour); the client's tick count restarts from 0 and the rotation and squash phases are rearranged — both are periodic functions, so nothing visible is affected (【本文补充】).
- **Fast reconnect**: if the player reconnects within 200 ticks he comes back with the effect's remaining duration, the lock is in force as before and the trio plays as before; no stage is re-issued (isomorphic to the same-kind treatment of Zangief Cane in 11 §11.4).
- **Basis**: D-81, D-79, D-50.

### 12.3 Under the mount branch, the mob is ridden / on a lead

- **Skip the mount**: if the affected mob already has a vehicle (`isPassenger()` is true) or is on a lead (`isLeashed()` is true), **no invisible seat is mounted**, and the behaviour falls back to the `render` branch (【本文补充】).
- **Why**: an entity has only one vehicle. Forcing a mount would kick the player off his boat or his horse, or pluck the mob off the entity carrying it — precisely the 'changing entity relations and collision' cost D-82 lists (RT-18).
- **The lead is not taken over**: a lead is another set of entity relations; the `mount` branch neither removes nor overwrites it, and the lead is in force as before once the effect ends.
- **Cleanup**: the seat is removed together with the effect when it ends; if the seat no longer exists (chunk unloaded, `/kill`ed), it is treated as a no-op and no error is raised.
- **Basis**: D-82, RT-18.

### 12.4 Point Defense Fungus: the chunk unloads halfway through an interception

- **The count lives in the block entity**: it is saved and restored across chunk unload and reload; **a charge already taken is not rolled back** — 'intercepted' is an accomplished fact, and rolling it back would let the same projectile be charged twice (【本文补充】).
- **The mark does not go into the save**: 'already intercepted this tick' is a runtime field (a tick number) on the projectile entity and does not take part in the chunk save; a projectile in an unloaded chunk does not tick anyway, so no 'half interception across ticks' can arise (【本文补充】).
- **The scan covers loaded chunks only**: when the fungus's chunk is loaded but the projectile's chunk is not, this projectile is not scanned; the projectile's lifetime is 100 ticks (§6.3), so by the time it is reloaded it has usually expired, and the practical result is 'not intercepted'.
- **Granularity of rollback**: if a chunk unload happens after the charge is taken but before the particles are played, the particles are lost and the count is already charged, so it counts as 'intercepted' (【本文补充】).
- **Basis**: D-88, RT-20, D-72.

### 12.5 Vibration Bean: the unattached block meets a piston / water / a block update

- **Block update**: `updateShape` always returns itself, and a neighbour change in any direction neither changes the state nor removes it (D-91's 'not removed by block updates').
- **Water**: the existing deployed blocks' `pistonPushReaction` is not overridden (default `NORMAL`, `block/DeployedCropBlock.java` does not declare the property), so flowing water does not wash it away (【本文补充】).
- **Piston**: judged pushable → the block moves together with its block entity, the trap changes coordinates with it and can still be triggered; judged not pushable → it stays put. Neither result **goes through** the 'attachment lost → removed' path, because this mode has no attachment from the start (D-91's grounds).
- **Only two routes can remove it**: the player mining it, or `/setblock` overwriting it (the table in §6.4).
- **Basis**: D-91, RT-21.

### 12.6 Vibration Bean triggering at the same time as another trap

- **Independent of each other**: every trap is a one-shot block that looks only at the distance between itself and the player; they do not query each other and do not queue (D-92).
- **Both settle in the same tick**: when two traps both satisfy the condition in the same tick, the two `removeBlock` calls each delete their own block; the two 80-tick Confusions have the same id and the same amplifier, so vanilla's merge rule takes the one with the longer remaining duration, with **no stacking and no doubling** (【本文补充】).
- **Isomorphic to the existing one-shot traps**: like Zangief Cane and Fang Trap Cactus it takes the 'one-shot + silent removal' route; whichever settles first disappears first, and there is no rollback (D-72).
- **Basis**: D-92, D-72.

### 12.7 Fang Trap Cactus: a fang lands where there is no ground

- **No ground check**: the fangs' coordinates are computed from the deployment point + the two distribution radii and generated directly, and not one fang is skipped because the target position has no support (【本文补充】).
- **Why it may not be checked**: the counts and the two distribution radii are D-94's decision boundaries (§7.4). Adding a 'generate one fewer if there is no ground' predicate would be secretly changing those two, and 16 §5.3 states outright that they are not authorised.
- **Landing where no entity should be**: what happens is vanilla's own entity behaviour (vanilla fangs do no support check when spawned); this mod does no extra clearing and no fallback (【本文补充】).
- **Basis**: D-94, D-30.

### 12.8 Zangief Cane: the target dies in phase 2

- **The cancellation lapses with it**: the effect disappears with the entity's removal, and the cancellation of natural fall damage goes with it (it turns on an effect-existence check, §8.4 clause 1).
- **The settlement is not carried out**: the target is already dead, so the landing 7.5 points is not applied; no `farmstorm:zangief_slam` damage record is produced, and no second kill credit is produced either (D-96, D-72).
- **Order within the same tick**: if death and landing fall in the same tick, the server's settlement order is the authority — landing first means 7.5 is settled once, death first means only the death path runs. Either way it settles only once (【本文补充】).
- **No second trigger**: the block was already silently removed on the trigger tick (D-72).
- **Basis**: D-96, D-72.

### 12.9 Zangief Cane conflicting with another mod's fall immunity

- **It cancels only its own segment** (RT-22): it uses an effect-existence check only and cancels fall damage only 'during this trap's phase 2'; slow falling, immunity and flight are decided by their own owners, and this mod neither reads, overwrites nor detects them.
- **The result once stacked**: if another mod also cancels fall damage during phase 2, the result is 'they cancel + this mod settles 7.5', and the 7.5 still settles only once — it is applied by the landing event and does not go through the fall-damage channel (【本文补充】).
- **Never landing**: when the target gains flight or levitation during phase 2, it ends through `MAX_FALL_TICKS` (100 ticks) and the 7.5 is not settled (§8.4 clause 4).
- **No runtime detection**: consistent with the same-kind treatment of RT-16, no compatibility branch is made.
- **Basis**: D-96, RT-22.

### 12.10 A Lucky Berry producing Friend Chives

- **It will happen**: the pool contains Friend Chives (D-97). Eating a berry yields 1 projectile; thrown, it gives every mob inside the landing sphere (yourself included) 200 ticks of Q-Bounce and **locks every action** (D-81). **Deliberate, not a hole** (§9.3).
- **No remedy is added**: no exemption for 'the player locking himself', no exclusion added to the pool, and no change to Q-Bounce's duration (16 §6 clauses 6, 11).
- **Dying during the lock**: takes the death path of §12.2; the death screen is let through and the UI does not stick.
- **Basis**: D-97, D-81, D-55.

### 12.11 Confusion and another mod's movement changes

- **It only inverts** (RT-19): inside `MovementInputUpdateEvent` it inverts the two components; it does not recompute, overwrite or read other mods' intermediate values.
- **Present at the same time as the lock**: the lock wins (§3.5, §12.1).
- **The order carries no meaning**: if another mod normalises or caps speed in the same event, whether the inversion happens before or after it is decided by the event registration order; in either order the result is only 'the opposite direction, the same strength', which does not affect playability (【本文补充】).
- **No detection, no degradation**: no runtime detection and no compatibility branch.
- **Basis**: D-83, RT-19.

### 12.12 Exemptions for creative / spectator players

Written separately in three places (none of D-81, D-83 and D-92 restates the exemptions item by item; what follows are this document's own derivations; the Point Defense Fungus row gets its ally/enemy convention from D-98):

| Place | Rule | Basis |
|---|---|---|
| Trap trigger side (Vibration Bean, Fang Trap Cactus, Zangief Cane) | Creative and spectator players **do not trigger** it, carried over from D-71 and the existing exemptions of the three v0.0.1 traps | D-71, D-92 |
| Effect-applying side (Friend Chives's Q-Bounce, Vibration Bean's Confusion, Fang Trap Cactus's Slowness) | **No exemption is set** — neither D-81 nor D-83 has such a clause. A creative player hit by Friend Chives is locked for 200 ticks; a spectator cannot take `EvokerFangs` damage and therefore cannot take Fang Trap Cactus's Slowness either | D-81, D-83, D-95 |
| Point Defense Fungus | Intercepts **non-ally** projectiles only (D-98); what it intercepts is a projectile, not a player, so a player's creative/spectator status plays no part in the determination here | D-87, D-98 |

### 12.13 Q-Bounce and the Zangief Cane phase lock coexisting

- **One and the same lock**: both effects turn the seven-segment lock on (segment 1's predicate looks at 'either effect is present'), and unlocking follows 'neither effect is present' (§2.6).
- **They do not read each other**: the Zangief Cane phase machine re-issues an effect with the same id and a different amplifier on a phase change; Q-Bounce's presence does not alter the phase advance, and vice versa (D-50).
- **Ending one after the other**: Q-Bounce ends first → the lock is held until phase 2 ends; Zangief Cane ends first → the lock is held until Q-Bounce ends. Neither case produces a 'half unlock' (【本文补充】).
- **Basis**: D-79, D-81, D-50.

### 12.14 Ally-triggered traps and ally-thrown projectiles

- **Vibration Bean**: the effect side treats every player inside the radius alike, and an ally player stepping on it gets 80 ticks of Confusion all the same (D-92, D-101). It is the **effect side** that does not sieve allies from enemies, which is a different matter from Point Defense Fungus's interception sieve (D-98).
- **Point Defense Fungus**: intercepts **non-ally** projectiles only (D-98); a crop thrown by an ally player passes through **without being intercepted and without a life being charged**, consistent with plan item 3's original text.
- **Fang Trap Cactus / Zangief Cane**: the trigger side recognises non-ally players only (D-71 and the existing convention), and an ally stepping on it does not trigger it.
- **These three are not the same ruler**: the trigger side and the interception side govern 'who steps in / who throws it over' (D-07's ally/enemy convention, of which D-98 comes from the same source), and the effect side governs 'who gets caught in it' (D-27's one-shot AoE convention) — the division of labour differs, so do not read the effect side's lack of a sieve as the interception side having none either (D-92's grounds).
- **Basis**: D-92, D-98, D-101, D-71.

---

## Appendix A · Register of differences between plan0.0.3.txt's original text and this document

| # | Key point of plan0.0.3.txt's original text | How this document handles it | Decision | Nature of the difference |
|---|---|---|---|---|
| 1 | Q-Bounce: the model is forced to stay in the riding state, rotates continuously, is squashed and stretched repeatedly, and cannot perform any action ('for the specific action restrictions, refer to Zangief Cane'); the icon is the `preview.jpg` under the Steam Workshop path | The pose is split into **two branches that coexist**, `render` / `mount` (default `render`; the author picks which one to keep on a real machine); rotation 24.0 degrees/tick, squash amplitude 0.15 / period 6 ticks, all of it falling on the client render layer; 'cannot act' **reuses the seven-segment lock** instead of being written anew; the icon is converted into an 18×18 PNG; the `mount` branch's seat uses only a vanilla entity type that already exists and adds no `entity_type` | D-81, D-82, D-102, D-79, D-53 | Refinement + the implementation convention split into two branches (the registration surface does not grow) |
| 2 | Friend Chives + Chive Seeds: mobs inside a sphere of radius 6 metres around the landing point get 10 seconds of Q-Bounce; if any mob is hit, audio plays at the landing point and is audible within 32 blocks around; the seed is planted in farmland and harvested when mature; the block texture is not drawn for now | 6.0 blocks **Euclidean**; 200 ticks; with 'the number actually applied successfully ≥ 1' as the **only** play criterion; volume 2.0 traded for 32.0 blocks; a 4-stage crop + mature drops 1 crop and 1 seed, immature drops 1 seed; **no converter recipe**, only membership of the `converter_fuel` tag; the block texture is a magenta `#D81E8A` placeholder (not final) | D-84, D-85, D-53 | Refinement + the new 'no conversion recipe' convention |
| 3 | Point Defense Fungus: intercepts **non-ally** projectiles within an 8-block radius; on interception it produces one explosion effect at the intercepted projectile; a single fungus disappears after 6 interceptions; when several fungi reach the same projectile only one of them has a life consumed | Determination surface = the single base class `ThrownCropEntity` (vanilla arrow / snowball / trident / ender pearl excluded by nature) **+ the non-ally sieve**: the 'non-ally' qualifier had originally been lost in D-87, and **D-98 has restored that qualifier**; §5.4 of this document is written as non-ally (allies pass through, neither intercepted nor charged a life), and the family size is corrected by D-98 to 10 + 2 = 12; an intercepted projectile is discarded on the spot per D-99 and does not settle its landing; 'explosion effect' = purely visual particles + a vanilla sound (D-100), **with no `Explosion` constructed**; 6 charges + the tick-number mark on the projectile for de-duplication | D-86 ~ D-89, D-98 ~ D-100 | Refinement + **convention correction** (D-98 restores the original text's 'non-ally' and corrects the family size; D-99 supplies where the projectile goes after interception; D-100 closes off the sound) |
| 4 | Confusion: shows no status icon, works only on players, inverts the movement direction | Two conventions plan did not write are added: only the two components `forwardImpulse` and `leftImpulse` are inverted (view / jump / attack / use do not move); when present at the same time as Q-Bounce, **the input lock wins**; no icon texture is registered, and the instance's two display flags are set false | D-83 | Refinement |
| 5 | Vibration Bean: the landing point generates a trap that **attaches to no block**; it triggers when a non-ally player's 'expected distance is less than 4' (a slip of the pen in the original text); it applies 4 seconds of Confusion to every player within a distance of less than 4; it plays one explosion sound | A separate 'unattached' mode is added (**it does not reuse** `supportsFloating`: `canSurvive` always true, `updateShape` returns itself, no FACE check); the trigger side uses **strictly less than 4.0 blocks**, the effect side 80 ticks and treats every player inside the radius alike (both sides share the same ruler and the same endpoint, D-101; this is the **effect side** not sieving, which is a different matter from Point Defense Fungus's interception sieve D-98); the explosion sound reuses a vanilla event that already exists (D-100); after triggering it is silently removed with no drop | D-90 ~ D-92, D-100, D-101 | Refinement + clarification of the convention (`supportsFloating` may not be reused) |
| 6 | Fang Trap Cactus: '赢过输过没死过' is renamed to '刺球'; it triggers at a distance ≤ 2.5; the 8 outer-ring fangs have a 0.2-second delay and the 5 inner-ring fangs 0.1 seconds; players hit get 3 seconds of Slowness 5 | The rename **changes the Chinese side only** (the id, the crop block id, `DeployedKind` and the English name do not move; C-10's explicit exception); the trigger distance is **≤ 2.5** (Euclidean, keeping C-5's ≤ operator, with the value changing accordingly); delays 4 ticks / 2 ticks; Slowness V = `amplifier 4`, 60 ticks; the 4 items plan does not mention (the two ring counts and the two distribution radii) are written into the table marked **unchanged** | D-93 ~ D-95 | Refinement + the decision boundaries made explicit |
| 7 | Zangief Cane: keep the existing features, remove the fall damage of the last segment and replace it with a flat 7.5 damage | Only the natural fall damage **during phase 2's fall** is cancelled (an effect-existence check, with no change to the global logic); landing settles a **flat 7.5 points**; a 3rd damage type, `farmstorm:zangief_slam`, is added; the ownership is still the trap's owner; the rest of 0.0.2's features are listed as the 12-row do-not-touch list of §8.3 | D-96 | Refinement + a new damage type (which plan does not mention) |

Three notes under the table:

1. **The 7 rows correspond to the 7 functional requirements of plan0.0.3.txt** (items 1 ~ 7); the first few lines of plan are version and platform information and contain no functional requirement.
2. **What is unchanged and copied as it stands**: the original text of the four item descriptions ('保持Q弹' / '子弹我都抓得住' / '留个纪念' / '最扎人的一集') goes into `item.farmstorm.*.tooltip`; the Chinese names and the spellings in plan (including the history of the old name '赢过输过没死过'); and the order itself of 'implement the function first, do not draw the block texture for now'.
3. **Everywhere this document departs from plan, the table above has named it and given a D number**; a departure with no D number behind it counts as a document defect. All three corrections of this round have been turned into decisions: row 3 had originally dropped the 'non-ally' of D-87 (this document at the time recorded it as a difference from plan), and **D-98 has restored that qualifier**, so §5.4 and row 3 of this section are both written as **non-ally** now; row 3's 'explosion effect → purely visual' still follows D-89, and the reuse of a vanilla event for the sound is closed off by D-100; row 5's 'the unattached mode may not reuse `supportsFloating`' still follows D-91, and the endpoints of the two sides are unified to strictly less than 4.0 by D-101. One further correction that is not part of plan's wording is registered in §6.2: Vibration Bean's `DeployedKind` changes from 'none' to `VIBRATION_BEAN` (the source fact that `kindOf()` throws, see `18-TECH-0.0.3.md` §1.4(b)).

## Translator's notes

1. **Resolved in the upstream source**: the three discrepancies this translation originally recorded for this document have been corrected, and this copy follows the corrected text — §8.4 now reads 'of the **four** ways of ending' (consistent with the four phase-2 termination conditions it enumerates, landing plus the other three), §9.3's truncated 动豆 now reads 震动豆 (still translated as Vibration Bean), and §5.5's projectile-side mark is `lastInterceptTick` throughout, matching `20-PROMPT-MAIN-0.0.3.md`.
2. **刺球 is rendered as 'Fang Trap Cactus'** in running prose, per the fixed terminology of this version (registry id `fang_trap_cactus`, D-93's explicit exception to C-10, English name unchanged). Where the text discusses the **Chinese display-name value itself** (§7.1, §7.2, §7.6, Appendix A row 6), the literal strings 赢过输过没死过 and 刺球 are kept in source form, because they are the values written into `zh_cn.json`. The three §10.1 advancements' Chinese display names are kept in source form for the same reason.
3. **Carried-over conventions from the v0.0.2 English documents**: the markup tag 【本文补充】 is left untranslated (as in `11-DESIGN-0.0.2.md`), and `man瓜` (§5.2, §5.4) is rendered 'Pumpkin Turret', following `docs/en/03-TASKS.md` and `docs/en/06-PROMPT-TASKS.md`. All registry ids, class and method names, package paths, config keys, language keys, file paths, commands and code fragments are reproduced unchanged.
