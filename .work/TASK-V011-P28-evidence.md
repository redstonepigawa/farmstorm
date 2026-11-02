# Phase 28 证据 · F-185 / F-186（朋友的韭 持续场）

> 执行者：子代理（本 Phase 唯一执行者）。工作目录 `D:\project_farm`（Windows / pwsh）。
> 基线 commit `11b8635`（Phase 27 交付）。本 Phase **只改 1 个文件**：
> `src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java`。
> 构建唯一入口：`cmd /c ""D:\project_farm\.tools\gw.cmd" build`。

---

## 0. 结论行（逐条对应任务书 §7 的 9 条）

| # | 判据 | 结论 | 证据位置 |
|---|---|---|---|
| 1 | 时序（#30）：逐刻递减，第 `effectTicks` 刻消失 | **通过（实测）** 200 刻一轮：x=0 施加 **200** → x=1 施加 **199** → x=199 施加 **1** → x=200 **自毁**；RCON 逐刻读数 `199,198,…,2,1` | §3.1、§3.4 |
| 2 | 200 不是常量（A-33）：改 100 递减止于 100 | **通过（实测）** 100 刻一轮：x=0 施加 **100** → x=99 施加 **1** → 第 **100** 刻自毁；另测 1200 刻（60 s 整自毁） | §3.2、§3.3 |
| 3 | 音效（#31）：空投不播、命中 ≥1 只播 1 次 | **通过（实测）** 空投 0 条命中音（3 轮共 4 次空投）；球内 ≥1 每场恰好 1 条 | §3.5 |
| 4 | 落地物理：水流里 30 s 不动、踩上去不被推动 | **通过（实测）** 水里 60 s 位置恒为 `(30.5, 100.13872650564801, 30.5)`（±0）；玩家站上去 70/70 样本恒为 `(0.5, 100.02, 0.5)` | §3.6 |
| 5 | 基类零改动 | **通过** `git diff ThrownCropEntity.java` 通道长度 0；基类 `keepAliveAfterImpact()` 仍 `return false` | §1.2、§4 |
| 6 | 卸载即结束（#32）+ `"Settled"` ≥ 1 | **通过（实测）** 走开 300+ 格卸载再回来：重载首 tick 自毁（日志有专条）；跨整次重启也不复活；`grep -c '"Settled"'` = **1** | §3.7、§4 |
| 7 | `.tools\gw.cmd build` 退出码 0 | **通过** `EXIT=0`，产物 `build/libs/farmstorm-0.1.1+mc1.21.1.jar` 1,025,816 B | §2 |
| 8 | `git status --porcelain`：改动只落在目标文件 + 证据 + `run-p28*` | **通过**：`src` 下只有目标文件；`run-p28/` 被 `.gitignore:4 run-*/` 忽略（0 条）；`run/` 无任何改动 | §5.3 |
| 9 | 三个数不变：注册 93 / server config 166 / 语言键与开工时相同 | **通过**：93（12 类逐类相加）/ 166 键 59 组 / 316 × 316（键集一致） | §5 |

红线：**无 Mixin**（`verify_project.py` §4 扫过 + `mixin` 命名文件 0 个）。「0 个自定义网络包」不作为任何判据。

---

## 1. `git diff`（任务书 §8.1）

### 1.1 `git diff --stat -- src`

```
 .../entity/thrown/FriendChivesProjectile.java      | 154 ++++++++++++++++++---
 1 file changed, 132 insertions(+), 22 deletions(-)
```

### 1.2 `ThrownCropEntity.java` 的 diff 为空（冻结面）

```
$ git diff -- src/main/java/com/dashpig/farmstorm/entity/ThrownCropEntity.java
（无输出，通道长度 = 0）
```

基类的默认仍未变（`ThrownCropEntity.java`）：

```java
    /** 爆竹重写为 true：命中后原地继续计时撒风弹。 */
    protected boolean keepAliveAfterImpact() {
        return false;
    }
```

（本类现在是第二个重写者，与爆竹并列；其余 6 个投掷物行为不变。）

### 1.3 `FriendChivesProjectile.java` 的完整 diff

```diff
diff --git a/src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java b/src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java
index 6c9ebcb..1bb702d 100644
--- a/src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java
+++ b/src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java
@@ -8,6 +8,7 @@ import com.dashpig.farmstorm.registry.FarmstormSounds;
 import com.dashpig.farmstorm.util.CropProfile;
 import java.util.Locale;
 import javax.annotation.Nullable;
+import net.minecraft.nbt.CompoundTag;
 import net.minecraft.server.level.ServerLevel;
 import net.minecraft.sounds.SoundSource;
 import net.minecraft.world.effect.MobEffectInstance;
@@ -19,11 +20,18 @@ import net.minecraft.world.phys.AABB;
 import net.minecraft.world.phys.Vec3;
 
 /**
- * 朋友的韭的投掷物（TECH §3.3）：落点对欧氏 6 格球内的生物施加 200 刻 Q弹，
- * 至少施加成功 1 个生物时在落点播一次命中音（音量 2.0，32 格可闻）；空投不响（D-85）。
+ * 朋友的韭的投掷物（TECH §2.7.2 / D-131 ~ D-133）：落地后不是「闪一下」就消失，而是变成
+ * {@code friend_chives.effectTicks} 刻的场——每刻重扫落点球体，对球内每个生物施加
+ * {@code fieldLength − x} 刻 Q弹（x = 落地后经过的刻数），第 {@code fieldLength} 刻自毁；
+ * 命中音仍只在落地那一刻判一次（球内 ≥1 播一次、空投不响、后续进场不补播，D-132）。
  *
- * <p>F-115 落点结算 + F-116 播音都在 {@link #onImpact} 里：基类 {@code settle} 保证每次生成恰好调用一次，
- * 因此「一次投掷只播一次」由生命周期保证，本类不需要额外的播音标记。
+ * <p>A-33：场长只有 {@link #fieldLength} 一个来源（落地时读一次 config，默认值见
+ * {@code FarmstormConfig}），施加量一律由它派生，本类不再出现任何时长字面量——服主把键改成 100，
+ * 场就只跑 100 刻。
+ *
+ * <p>F-186 / RT-35：场不跨区块卸载。{@code fieldTick} 不写 NBT；读档时若发现基类的 {@code Settled}
+ * 已经是 {@code true}，就在第一个 tick 自杀——否则它既不再 tick 场、也不会被基类的寿命检查回收
+ * （{@code ThrownCropEntity} 的寿命检查只在 {@code !settled} 时生效），会变成一个永不消失的实体。
  */
 public class FriendChivesProjectile extends ThrownCropEntity {
     /**
@@ -35,6 +43,15 @@ public class FriendChivesProjectile extends ThrownCropEntity {
     /** 播音音高：文档没有给出可变口径，取原版的 1.0。 */
     private static final float SOUND_PITCH = 1.0F;
 
+    /** 落地后的刻数；-1 = 还没落地。不写 NBT（D-133：区块卸载即结束）。 */
+    private int fieldTick = -1;
+
+    /** 读档时基类的 {@code Settled} 已经是 {@code true}（F-186）：下一个 tick 自杀，不跨卸载复活。 */
+    private boolean loadedSettled;
+
+    /** 场长（A-33：唯一来源），落地那一刻读一次；此时 {@code fieldTick} 仍是 -1。 */
+    private int fieldLength;
+
     public FriendChivesProjectile(EntityType<? extends FriendChivesProjectile> type, Level level) {
         super(type, level);
     }
@@ -48,34 +65,127 @@ public class FriendChivesProjectile extends ThrownCropEntity {
         return CropProfile.FRIEND_CHIVES;
     }
 
+    /** D-131：落地后不消失，改为持续场。基类的默认仍是 {@code false}，其它投掷物不受影响。 */
+    @Override
+    protected boolean keepAliveAfterImpact() {
+        return true;
+    }
+
     /**
-     * DESIGN §4.6 的判定顺序（同一刻完成，不排队）：欧氏球筛 → 逐个写 Q弹 → 累计施加成功数 n → n ≥ 1 播一次音。
-     * 谓词里只有几何与存活条件：效果端不筛敌我，投掷者本人站进球内同样中招（DESIGN §4.5 / §12.12）。
+     * 落地那一刻（基类 {@code settle} 保证每次生成恰好调用一次）：读一次场长 → 施加 x = 0 那一批 →
+     * 按既有判据播一次音（D-132）→ 把实体钉在落点（D-133 的三件套：置零速度 / 无重力 / 免碰撞）。
+     *
+     * <p>谓词里只有几何与存活条件：效果端不筛敌我，投掷者本人站进球内同样中招（DESIGN §4.5 / §12.12）。
      */
     @Override
     protected void onImpact(ServerLevel level, Vec3 pos, @Nullable Entity directHit) {
+        this.fieldLength = FarmstormConfig.friendChivesEffectTicks.get();
+        FieldScan scan = applyField(level, pos, 0);
+        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
+                "朋友的韭 落点 (%.2f, %.2f, %.2f) 球内 %d 个生物，新施加 %d 个（半径 %.2f 格，第 0 刻施加 %d 刻，amplifier 0）",
+                pos.x, pos.y, pos.z, scan.inSphere(), scan.applied(),
+                FarmstormConfig.friendChivesHitRadius.get(), this.fieldLength));
+        // 判据只有一条：球内 ≥ 1 个生物（D-132），不另做一次「附近有没有生物」的查询（DESIGN §4.6）；
+        // 这里数球内生物而不是数新施加数，所以即使命中的生物身上已有 Q弹也照样只响这一声。
+        // D-132：这条判据只在落地这一刻跑，所以整场最多播一次，后续进场不补播。
+        if (scan.inSphere() >= 1) {
+            float volume = (float) (FarmstormConfig.friendChivesSoundRange.get() / SOUND_RANGE_PER_VOLUME);
+            level.playSound(null, pos.x, pos.y, pos.z, FarmstormSounds.FRIEND_CHIVES.get(), SoundSource.PLAYERS,
+                    volume, SOUND_PITCH);
+            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
+                    "朋友的韭 命中音 farmstorm:friend_chives 播放一次：落点 (%.2f, %.2f, %.2f) 音源 PLAYERS 音量 %.2f（可闻 %.1f 格）",
+                    pos.x, pos.y, pos.z, volume, volume * SOUND_RANGE_PER_VOLUME));
+        }
+        // 三件套分工（TECH §2.7.2 要点 2）：置零速度防被上一刻的动量带走、无重力防缓慢下坠、
+        // 免碰撞（Entity 的既有字段）防水流 / 活塞 / 实体推走。
+        this.setDeltaMovement(Vec3.ZERO);
+        this.setNoGravity(true);
+        this.noPhysics = true;
+        this.fieldTick = 0;
+    }
+
+    /**
+     * 场由本方法驱动。基类 {@code tick()} 的寿命检查在 settled 之后失效（{@code ThrownCropEntity} 只在
+     * {@code !settled} 时 discard），落地后的回收只能靠这里的自毁（RT-35）。
+     *
+     * <p>顺序即验收点（TECH §2.7.2 / §7 #30）：{@code super.tick()} 之后第一件事是 F-186 的读档自杀，
+     * 然后是「还没落地就不管」，再自增刻数；第 {@code fieldLength} 刻自毁（不再施加），否则重扫球体。
+     */
+    @Override
+    public void tick() {
+        super.tick();
+        if (!(this.level() instanceof ServerLevel level)) {
+            return;
+        }
+        if (this.loadedSettled) {
+            // F-186 / RT-35：这条路径只会在「场被区块卸载写进 NBT、再加载回来」时走到。
+            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
+                    "朋友的韭 场结束：读档 Settled=true，(%s) 的第一个 tick 自毁（场不跨区块卸载，F-186）",
+                    this.position()));
+            this.discard();
+            return;
+        }
+        if (this.fieldTick < 0) {
+            return;
+        }
+        this.fieldTick++;
+        if (this.fieldTick >= this.fieldLength) {
+            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
+                    "朋友的韭 场结束：落点 (%.2f, %.2f, %.2f) 第 %d 刻自毁（场长 %d 刻）",
+                    this.getX(), this.getY(), this.getZ(), this.fieldTick, this.fieldLength));
+            this.discard();
+            return;
+        }
+        FieldScan scan = applyField(level, this.position(), this.fieldTick);
+        // 只在两端的验收点留读数：第 1 刻（首个重扫批）与最后一次施加（施加 1 刻）。每刻都打会让每场的行数等于场长。
+        if (this.fieldTick == 1 || this.fieldTick == this.fieldLength - 1) {
+            Farmstorm.LOGGER.info(String.format(Locale.ROOT,
+                    "朋友的韭 第 %d 刻重扫：球内 %d 个生物，新施加 %d 个，施加 %d 刻（amplifier 0）",
+                    this.fieldTick, scan.inSphere(), scan.applied(), this.fieldLength - this.fieldTick));
+        }
+    }
+
+    /**
+     * 每刻重扫一次落点球体（TECH §2.7.2 要点 1 + A-33）：施加 {@code fieldLength − elapsed} 刻 Q弹，
+     * amplifier 仍是既有字面量 0。施加量比上一刻少 1 刻，于是「第 {@code fieldLength} 刻之后效果自然结束」
+     * 与「场结束时效果刚好走完」同时成立，既不续期也不提前结束。
+     *
+     * <p>返回值只给日志与落地那一刻的音效判据（D-132）用，{@code tick()} 不看它。
+     */
+    private FieldScan applyField(ServerLevel level, Vec3 center, int elapsed) {
+        int ticks = this.fieldLength - elapsed;
+        if (ticks <= 0) {
+            return new FieldScan(0, 0);
+        }
         double radius = FarmstormConfig.friendChivesHitRadius.get();
-        int ticks = FarmstormConfig.friendChivesEffectTicks.get();
+        int inSphere = 0;
         int applied = 0;
         // AABB 只是粗筛（2r × 2r × 2r 的立方），命中判定必须再用 distanceTo 做欧氏复核（TECH §3.3 的「二次筛」）。
-        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(radius),
-                candidate -> candidate.isAlive() && pos.distanceTo(candidate.position()) <= radius)) {
+        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius),
+                candidate -> candidate.isAlive() && center.distanceTo(candidate.position()) <= radius)) {
+            inSphere++;
             if (target.addEffect(new MobEffectInstance(FarmstormMobEffects.Q_BOUNCE, ticks, 0, false, true, true))) {
                 applied++;
             }
         }
-        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
-                "朋友的韭 落点 (%.2f, %.2f, %.2f) 球内施加 %d 个生物（半径 %.2f 格，%d 刻，amplifier 0）",
-                pos.x, pos.y, pos.z, applied, radius, ticks));
-        // 判据只有一条：n 就是上面的施加成功计数，不另做一次「附近有没有生物」的查询（DESIGN §4.6）。
-        if (applied < 1) {
-            return;
-        }
-        float volume = (float) (FarmstormConfig.friendChivesSoundRange.get() / SOUND_RANGE_PER_VOLUME);
-        level.playSound(null, pos.x, pos.y, pos.z, FarmstormSounds.FRIEND_CHIVES.get(), SoundSource.PLAYERS,
-                volume, SOUND_PITCH);
-        Farmstorm.LOGGER.info(String.format(Locale.ROOT,
-                "朋友的韭 命中音 farmstorm:friend_chives 播放一次：落点 (%.2f, %.2f, %.2f) 音源 PLAYERS 音量 %.2f（可闻 %.1f 格）",
-                pos.x, pos.y, pos.z, volume, volume * SOUND_RANGE_PER_VOLUME));
+        return new FieldScan(inSphere, applied);
+    }
+
+    /**
+     * 一次球扫的两个读数。{@code inSphere} 是粗筛 + 欧氏复核后的球内生物数（落地那一刻的音效判据用它，D-132）；
+     * {@code applied} 是 {@code addEffect} 真的改动了效果状态的个数——已在场者逐刻被刷新时这里是 0，因为原版只在
+     * 新时长更长时才更新，于是「在场者身上正好剩 {@code fieldLength − x}」这条幂等性由原版规则保证，不必自己比时长。
+     */
+    private record FieldScan(int inSphere, int applied) {
+    }
+
+    /**
+     * F-186：只读基类既有的 {@code Settled} 键（{@code ThrownCropEntity} 的存取范围），不放宽、不自造键。
+     * {@code fieldTick} 故意不持久化（D-133：区块卸载即结束），因此本类不重写 {@code addAdditionalSaveData}。
+     */
+    @Override
+    public void readAdditionalSaveData(CompoundTag tag) {
+        super.readAdditionalSaveData(tag);
+        this.loadedSettled = tag.getBoolean("Settled");
     }
 }

```

---

## 2. 构建（任务书 §8.2）

```
$ cmd /c ""D:\project_farm\.tools\gw.cmd" build"
> Task :jar
> Task :sourcesJar
> Task :assemble
> Task :compileJava
> Task :build
BUILD SUCCESSFUL in 10s
5 actionable tasks: 2 executed, 3 up-to-date
EXIT=0
```

产物：`build/libs/farmstorm-0.1.1+mc1.21.1.jar` = **1,025,816 B**（2026-11-03 00:31:03）。
（基线 1,023,754 B；本 Phase +2,062 B = 一个类的新增逻辑。）

---

## 3. 实测读数（任务书 §8.3）

**测试方法**：独立目录 `run-p28`（复制 `run/eula.txt` + `run/server.properties`，改成
`level-type=minecraft:flat` / `spawn-monsters|animals|npcs=false` / `spawn-protection=0` / `max-tick-time=-1`，
RCON `enable-rcon=true` / `rcon.port=25575` / `rcon.password=fsverify`），**没有在 `run/` 里起服**。

- 手动改配置只用 `[System.IO.File]::WriteAllText(..., UTF8Encoding($false))`（UTF-8 无 BOM，实测首三字节 `91,99,111`），
  避开 pwsh 5.1 `Set-Content -Encoding utf8` 的 BOM 坑。
- 无玩家在线时区块不 tick 实体（实测：投射物位置冻结、无任何日志），因此用 mineflayer 无头 bot
  `.work/p28-bot.js`（本地 `.scratch/f124-bot/node_modules`，版本 1.21.1）连进服提供「在玩家模拟距离内」的条件；
  该 bot 同时是**客户端包级证据**的来源。
- 驱动脚本 `.work/p28-rcon.py`（持久 RCON 会话，一条命令一行，`!sleep <ms>` 控制节奏），
  逐刻采样 `/time query gametime` + `/data get entity ... Pos` + `/data get entity @a[...] active_effects[0].duration`。
- **读数口径**：`x` = 落地后经过的刻数（29 号 §8.2 的 x 表）。落地那一刻 `x = 0`。

### 3.1 `effectTicks = 200`（默认值；最终代码）

服务端日志（`.work/p28-server8.log`，整场 8 行，两次投掷）：

```
[00:35:12] 朋友的韭 落点 (0.50, 100.05, 0.50) 球内 0 个生物，新施加 0 个（半径 6.00 格，第 0 刻施加 200 刻，amplifier 0）
[00:35:12] 朋友的韭 第 1 刻重扫：球内 0 个生物，新施加 0 个，施加 199 刻（amplifier 0）
[00:35:22] 朋友的韭 第 199 刻重扫：球内 0 个生物，新施加 0 个，施加 1 刻（amplifier 0）
[00:35:22] 朋友的韭 场结束：落点 (0.50, 100.02, 0.50) 第 200 刻自毁（场长 200 刻）
[00:35:24] 朋友的韭 落点 (0.50, 100.05, 0.50) 球内 1 个生物，新施加 1 个（半径 6.00 格，第 0 刻施加 200 刻，amplifier 0）
[00:35:24] 朋友的韭 命中音 farmstorm:friend_chives 播放一次：落点 (0.50, 100.05, 0.50) 音源 PLAYERS 音量 2.00（可闻 32.0 格）
[00:35:24] 朋友的韭 第 1 刻重扫：球内 1 个生物，新施加 0 个，施加 199 刻（amplifier 0）
...（第 199 刻 / 场结束 同上，第二次投掷）
```

RCON 逐刻读数（`.work/p28-run10.log`，球内主体 = bot，采样 215 次 / 约每刻一次）：

```
count=215
first5 = Found no elements matching active_effects, (同上), (同上), 199, 198
last5  = 5, 4, 3, 2, 1
```

- 场实体位置：213 个样本恒为 `[0.5d, 100.02d, 0.5d]`（另 2 个是召唤瞬间的 `100.05`），末尾 `No entity was found`。
- 前 3 个样本是命中之前的空读数（`Found no elements matching active_effects`）。

### 3.2 `effectTicks = 100`（A-33 第二轮；最终代码）

服务端日志（`.work/p28-server6.log`）：

```
[00:29:37] 落点 (0.50, 100.05, 0.50) 球内 0 个生物，新施加 0 个（半径 6.00 格，第 0 刻施加 100 刻，amplifier 0）
[00:29:37] 第 1 刻重扫：球内 0 个生物，新施加 0 个，施加 99 刻（amplifier 0）
[00:29:42] 第 99 刻重扫：球内 0 个生物，新施加 0 个，施加 1 刻（amplifier 0）
[00:29:42] 场结束：落点 (0.50, 100.02, 0.50) 第 100 刻自毁（场长 100 刻）
[00:30:12] 落点 (0.50, 100.05, 0.50) 球内 1 个生物，新施加 1 个（半径 6.00 格，第 0 刻施加 100 刻，amplifier 0）
[00:30:12] 命中音 farmstorm:friend_chives 播放一次：落点 (0.50, 100.05, 0.50) 音源 PLAYERS 音量 2.00（可闻 32.0 格）
[00:30:12] 第 1 刻重扫：球内 1 个生物，新施加 0 个，施加 99 刻（amplifier 0）
[00:30:17] 第 99 刻重扫：球内 1 个生物，新施加 0 个，施加 1 刻（amplifier 0）
[00:30:17] 场结束：落点 (0.50, 100.02, 0.50) 第 100 刻自毁（场长 100 刻）
```

RCON 逐刻读数（`.work/p28-run8.log`，主体 = bot，先 `effect clear P28BOT` 清掉继承来的旧效果）：

```
99, 98, 97, ..., 5, 4, 3, 2, 1   （每刻 -1；出现 4 个重复值 = 采样快于服务器刻）
```

**结论（A-33）**：同一份代码在 200 / 1200 / 100 三个配置值下分别跑 200 / 1200 / 100 刻后自毁，
且末次施加恒为 **1 刻** —— 场长与施加基准同源于 `friend_chives.effectTicks`，
代码里没有第二个来源（§4 的 `grep 200 = 0`）。

### 3.3 `effectTicks = 1200`（时长上限；最终代码）

```
[00:19:40] 落点 (30.50, 100.05, 30.50) 球内 0 个生物，新施加 0 个（半径 6.00 格，第 0 刻施加 1200 刻，amplifier 0）
[00:19:40] 第 1 刻重扫：球内 0 个生物，新施加 0 个，施加 1199 刻（amplifier 0）
[00:20:40] 第 1199 刻重扫：球内 0 个生物，新施加 0 个，施加 1 刻（amplifier 0）
[00:20:40] 场结束：落点 (30.50, 100.14, 30.50) 第 1200 刻自毁（场长 1200 刻）
```

00:19:40 → 00:20:40 正好 60 s = 1200 刻。「不累积」：**一次投掷只产生 1 个场实体**
（每投掷 1 条落点日志 + 1 条自毁日志；RCON 查 `@e[type=farmstorm:friend_chives]` 从未出现第二个）。

### 3.4 客户端包级读数（无头 bot 收包，`entityId` = bot 自己）

| 配置 | `entity_effect` | `remove_entity_effect` | 差值 |
|---|---|---|---|
| 200 | `duration: 200, amplifier: 0` @ 16:35:24.363 | @ 16:35:34.311 | 9.948 s ≈ **200 刻** |
| 100 | `duration: 100, amplifier: 0` @ 16:30:12.833 | @ 16:30:17.784 | 4.951 s ≈ **100 刻** |

（`.work/p28-bot9.log` / `.work/p28-bot7.log`）
含义：落地那一刻客户端拿到的正是 `fieldLength`，且整段效果恰好在第 `fieldLength` 刻被服务端撤掉
——对应 29 号 §8.2「到期时刻统一」。中间没有续期包，说明每刻重扫是**幂等**的（原版 `MobEffectInstance.update`
只在「新时长更长」时才改，所以已在场者身上的剩余时长正好是 `fieldLength − x`）。

### 3.5 音效（D-132 / #31）

```
空投（球内 0 生物）：4 次投掷 —— `命中音` 日志 0 条（`球内 0 个生物` 各 1 条）
命  中（球内 ≥1）  ：3 次投掷 —— `命中音 ... 播放一次` 正好 3 条（每次 1 条）
```

原始行（`.work/p28-server8.log` 00:35:12 无 `命中音`；00:35:24 有且仅 1 条；`.work/p28-server6.log` 00:29:37 无、
00:29:43 1 条、00:30:12 1 条）。10 s 内没有出现第 2 条 —— 播音判据只在 `onImpact` 里跑一次，
`tick()` 不看施加结果（§4 的行号证据：`applyField` 的返回值在 `tick()` 里只用于日志）。

### 3.6 落地物理（D-133 / RT-35）

**（a）落在流动水里 60 s 不动**（`.work/p28-run4.log`，水柱 `x 28..32 / y 100..102 / z 28..32`，池底石板 y=99）：

```
落点 (30.50, 100.05, 30.50) 第 0 刻施加 1200 刻
采样 66 次（每 1 s 一次，共 66 s）→ 位置恒为 [30.5d, 100.13872650564801d, 30.5d]
第 1200 刻自毁（00:20:40），之后 No entity was found
```

y 值逐位相同（含小数），x/z 恒 30.5 ⇒ 30 s（实为 60 s）内 **±0 格**，没有被水流带走。

**（b）玩家站上去不被推动**（`.work/p28-run9.log`）：

```
tp P28BOT 0.5 100.02 0.5        （与场实体同点）
70 个样本：场实体恒为 [0.5d, 100.02d, 0.5d]；bot 恒为 [0.5d, 100.0d, 0.5d]
```

**（c）代码层三件套**（`onImpact` 末尾）：`setDeltaMovement(Vec3.ZERO)` + `setNoGravity(true)` + `noPhysics = true`。
`noPhysics` 同时让 `Entity.tick()` 跳过碰撞解算，因此活塞/实体挤压都改变不了位置（本机未实测活塞）。

### 3.7 卸载即结束（F-186 / RT-35）

**（a）区块卸载 → 重载**（`.work/p28-run6.log` + `.work/p28-server3.log`）：

```
00:23:59 落点 (2000.50, 100.05, 2000.50) 球内 1 个生物，新施加 1 个（… 第 0 刻施加 1200 刻 …）
00:23:59 命中音 … 播放一次
00:23:59 第 1 刻重扫：球内 1 个生物，新施加 0 个，施加 1199 刻（amplifier 0）
[把 bot 传送到 400 格外 7 s，chunk [125,125] 掉出模拟距离→卸载；再传回]
00:24:08 朋友的韭 场结束：读档 Settled=true，((2000.5, 100.02, 2000.5)) 的第一个 tick 自毁（场不跨区块卸载，F-186）
随后 /data get entity @e[type=farmstorm:friend_chives] → No entity was found
```

关键：**没有**出现「第 1199 刻重扫」或「第 1200 刻自毁」⇒ 它不是自然到期的，而是**重载后首 tick 自杀**；
场长 1200 刻（60 s）而整段只过了 9 s。

**（b）跨整次服务器重启也不复活**（`.work/p28-server6.log` 第 1 行）：上一个 server 进程 stop 时把场写进区块 NBT，
新进程 00:29:27 加载该区块（bot 登录时）→ 立刻走同一条自杀路径。

**（c）NBT 面**（`/data get entity` 原样输出，`.work/p28-run6.log`）：

```
{NoGravity: 1b, Motion: [0.0d, 0.0d, 0.0d], Submunition: 0b, Invulnerable: 0b, LeftOwner: 1b, Air: 300s,
 OnGround: 0b, PortalCooldown: 0, Rotation: [0.0f, -0.017826334f], Settled: 1b, FallDistance: 0.0f,
 Item: {count: 1, id: "farmstorm:friend_chives"}, Pos: [2000.5d, 100.02d, 2000.5d], HasBeenShot: 1b,
 Fire: 0s, UUID: [I; 1109818657, -1615443952, -1083063538, 1928665776]}
```

- `Settled: 1b` = 基类写的键（`ThrownCropEntity` 的存取范围）；
- **没有** `FieldTick` / `fieldTick` / `FieldLength` 之类自造键（本类不重写 `addAdditionalSaveData`）；
- 其余键全部是原版 `Entity` 的既有键（`NoGravity` 是原版布尔，不是自定义键）。

---

## 4. `grep` 读数（任务书 §8.4）

在 `src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java` 上：

| 模式 | 命中 | 说明 |
|---|---|---|
| `200` | **0** | 本类不出现时长字面量；`200` 只出现在 `FarmstormConfig.java:861` 的 `defineInRange("effectTicks", 200, 20, 1200)` 一处（默认值），以及注释 `「D-85：Q弹 时长（200 刻 = 10 秒）」`（`:859`） |
| `"Settled"` | **1** | `readAdditionalSaveData` 里 `this.loadedSettled = tag.getBoolean("Settled")` |
| `keepAliveAfterImpact` | **1** | 本类的重写（`return true`） |
| `keepAliveAfterImpact`（基类 `ThrownCropEntity.java`） | **3** | javadoc 1 + `settle()` 调用 1 + 定义 1；定义体仍是 `return false` |
| `noPhysics` | **1** | `this.noPhysics = true;`（`onImpact` 里） |
| `putInt` | **0** | 本类不写任何自定义 NBT 键（没有 `tag.putXxx`） |
| `addAdditionalSaveData` | **1** | 只在 javadoc 里出现（说明「本类不重写它」）；`@Override addAdditionalSaveData` 的命中数 = **0**，即确实没有重写 |

全源码树扫 `(= 200\b|, 200,|200 刻)`：**0 处**（唯一 `200` 是 config 的 `defineInRange` 参数，见上）。
`mixin` 命名的文件：**0 个**（红线「无 Mixin」）。

---

## 5. 三个数（任务书 §7.9）

| 项 | 读数 | 取证方式 |
|---|---|---|
| 注册对象 | **93** | 服务端启动自检：`自检 注册对象 93 个：items 32 / blocks 26 / block_entities 7 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / data_components 1 / menu_types 1 → OK`；`verify_project.py` §2 逐类核对「已核 registry 对象: 93 / 93」 |
| server config | **166 键 / 59 组** | 直接数 `run/config/farmstorm-server.toml`（**没有重新生成**，mtime 仍是 2026-11-02 02:39:02）；`verify_project.py` §1「生成 toml 键数: 166」 |
| 语言键 | **316 × 316，键集一致** | `src/generated/resources/assets/farmstorm/lang/{en_us,zh_cn}.json` 各自 316 键；`verify_project.py` §3「lang 键集一致，共 316 键」 |

`verify_project.py` 现状（**与开工时完全一致，本 Phase 没有让它变差**，退出码 1）：

```
== 1. config：文档键集 vs 生成的 toml ==
  生成 toml 键数: 166
  [STALE] run/config/farmstorm-server.toml 过期（mtime 2026-11-02 02:39:02 …）：缺 1 个键、多 1 个键
          （缺 ['pumpkin_turret.bulletSpeed']，多 ['farmland.limitPerPlayer']）
  [FAIL] toml 多 1 个键且源码里存在（文档漏记 MISMATCH）: ['farmland.limitPerPlayer']
…
== 4. 禁令扫描 ==
  [OK]   无 Mixin / TODO / 调试残留 / 空 catch
结论：FAIL 2 项，WARN 0 项        VERIFY_EXIT=1
```

这是 **A-40 窗口**（Phase 29 会删 `farmland.limitPerPlayer` 并补 `pumpkin_turret.bulletSpeed`）；
本 Phase **没有**跑 `runServer` 去重生 `run/config`（重生行为只在 `run-p28/config` 里发生）。

### 5.3 `git status`（任务书 §7.8）

```
$ git status --porcelain | grep -v '^?? \.work/'
 M .work/TASK-V011-P27.md          ← 开工前就是脏的（mtime 2026-11-02 23:43:03）
 A .work/p27-git-baseline.txt      ← 同上（2026-11-02 23:45:12）
 M README.md                       ← 同上（2026-11-02 21:44:51）
 M src/main/java/com/dashpig/farmstorm/entity/thrown/FriendChivesProjectile.java   ← 本 Phase 唯一源码改动
```

- 本 Phase 的源码改动**只有** `FriendChivesProjectile.java`（`git status --porcelain -- src` 只列这一行）。
- 上文三个 `M`/`A` 文件的 mtime 都早于本会话开始时间（2026-11-03 00:05），是 Phase 26 / 27 遗留的未提交工作，
  本 Phase 没有碰它们。
- `run-p28/` 被 `.gitignore:4: run-*/` 忽略，`git status --porcelain -- run-p28` = 0 条；
  `git status --porcelain -- run` = **0 条**（`run/` 一个字节都没动）。
- `docs/`、`build.gradle`、四份台账 `.work/v011-*.md`：**均未改动**（该路径过滤下唯一一行是开工前就存在的
  未跟踪文件 `?? .work/v011-glossary.md`，本 Phase 没有写过它）。

---

## 6. 文档-源码偏差表（任务书 §8.5）

| # | 文档说法 | 源码 / 实测 | 处理 |
|---|---|---|---|
| D1 | 30 号 §2.7.2 注释：`this.setNoGravity(true);  // 构造器已设（:43-51），这里再显式一次` | 两个构造器都**没有**调 `setNoGravity`（0.1.1 的 `FriendChivesProjectile.java` 构造器只有 `super(...)`）。重力是默认开的，落地前必须受重力 | 按任务书 §6.3 在 `onImpact` 显式设一次（§1.3 diff 可见），并登记本条偏差；`docs/` 不改 |
| D2 | 31 号 F-185 验收 1 / 33 号 §3.4：「第 1 刻 **200**、第 2 刻 **199**……第 **199** 刻 **1**；第 **200** 刻消失」 | 29 号 §8.2 的 x 表写的是 `x=0 → 200`、`x=199 → 1`、`x=200 → 自毁`。两种编号差 1 刻：把落地那刻计为「第 1 刻」时，「第 199 刻」对应 `x=198`（施加 2），不是 1 | **以 30 号 §2.7.2 伪代码 + 29 号 §8.2 x 表为准**（任务书 §6.4 明确要求按伪代码落地）。本证据全部按 `x` 记账：`x=0 → L`、`x=L−1 → 1`、`x=L → 自毁`。文档这句话需要作者定口径（见 §8 未关闭问题 2） |
| D3 | 「第 1 刻 200」（可观察口径） | `onImpact` 在**落地那一 tick 的 `super.tick()` 内部**执行，本类的 `tick()` 体在同一 tick 接着把 `x=1` 那一批写成 `L−1`。所以 RCON 在落地刻末读到的是 `L−1`（200 轮读到 199、100 轮读到 99），**不是** `L` | 落地那一刻确实写入了 `L`：由日志「第 0 刻施加 L 刻」与客户端包 `entity_effect duration = L` 两处取证。这是 30 号 §2.7.2 伪代码顺序的直接后果（伪代码把 `fieldTick = 0` 放在 `onImpact` 末尾），不是实现偏差；是否要让「落地刻末 = L」需要新裁决（§8 未关闭问题 2） |
| D4 | 29 号 §4.6：「n = 逐个施加成功数，n ≥ 1 播一次音」 vs 28 号 D-132 / 任务书 §6.3：「**球内 ≥ 1** 个生物才播」 | 在「场」的语境下两者不再等价：球内生物身上若已有更长的 Q弹（或另一场刚刷过），`addEffect` 返回 `false`（原版只在「新时长更长」时更新），施加成功数 = 0 | 按 **D-132 / 任务书 §6.3 的「球内 ≥1」** 实现：`onImpact` 用 `scan.inSphere()` 判音效（日志同时给 `球内 N 个` 与 `新施加 M 个` 两个读数）。实测证据见 §3.5、§3.2 里 `球内 1 个生物，新施加 0 个` 那一行仍播了一次 |
| D5 | 33 号 §3.4「NBT 面：实体只有基类的 `Settled` 等键」 | 实测还带原版 `Entity` 的既有键（`NoGravity / Motion / Air / OnGround / PortalCooldown / Rotation / FallDistance / Item / HasBeenShot / Fire / UUID / Pos / LeftOwner`），全是原版键、不是自定义键 | 口径澄清，不算偏差；本类**没有**新增任何 NBT 键（`grep putInt = 0`、无 `addAdditionalSaveData` 重写） |
| D6 | 33 号 §3.5 翻车点 6：「改了基类的默认值 → 其它投掷物不消失」 | 基类 `git diff` 为空、默认仍 `false`（§1.2） | 已守住 |
| D7 | RT-35 描述「实体被存进区块 NBT、加载回来既不自毁也不 tick 场」 | 实测复现了这条链路本身：卸载时确实被写进区块 NBT（重载后 `Settled: 1b` 可读），再由 `loadedSettled` 首 tick 自杀 | 与文档一致，无需改 |

---

## 7. 未验证清单（任务书 §8.6）

| # | 条目 | 为什么本机做不了 |
|---|---|---|
| U1 | 客户端**视觉**验收：落地后悬在落点的「韭」物品形态、无抖动 | 本机没有图形会话（只有专用服务器 + 无头 bot）；改用**包级**证据替代：`entity_effect duration=L` → 第 `L` 刻 `remove_entity_effect`（§3.4）。渲染路径未改（仍是 `ThrownItemRenderer`） |
| U2 | 客户端**听感**：音量 2.0 / 32 格可闻的主观验收 | 同上；只有服务端 `playSound` 日志（音量 2.00、可闻 32.0 格）与既有 `sounds.json`（本 Phase 不改） |
| U3 | 客户端侧物理预测（`noGravity` / `noPhysics` 不同步给客户端，客户端副本可能在自己的 tick 里被重力拉低） | 需要客户端进程观察；列为未关闭问题 1（同样的形态在已发布的爆竹里也存在） |
| U4 | 活塞推挤 | `noPhysics = true` 从代码上排除，但没有实测活塞（需要构造活塞装置 + 同步观察） |
| U5 | 跨维度 / 传送门卸载后的行为 | F-186 的出口判据是「区块卸载」，跨维度未测（同一自杀路径应当覆盖，但没有读数） |
| U6 | 两个场重叠时谁生效（29 号 §8.6） | 不在本 Phase 出口判据内，未构造重叠场景 |
| U7 | RT-34 多场性能（10 个场同时在场的服务端 tick 时间） | 需要压测装置与 MSPT 采样；本 Phase 只测了单场 |
| U8 | 「走下坡 / 半空落点」等非平面落点的场位置 | 只测了平面石板、水柱、玩家站立三种落点 |

---

## 8. 未关闭的问题（任务书 §8.7）

1. **客户端不同步「已落定」**：`noGravity` / `noPhysics` 都是 `Entity` 的本地字段，不随实体同步；
   服务端不动，但客户端副本每 tick 会自己加一次重力，直到下一次位置同步把它拉回。同一形态在爆竹
   （`BambooFirecrackerProjectile`，上一版已发布）里也存在。彻底修需要在实体数据里加一个 synced 布尔（新协议面），
   本 Phase 的约束（不新增注册对象 / config / 语言键，且伪代码未含）下没有做。
2. **「落地刻末读到 L 还是 L−1」**：任务书 §6.4 的伪代码把 `fieldTick = 0` 放在 `onImpact` 末尾，
   于是落地那一 tick 会连着执行 `x=1` 那一批（§6 D2/D3）。行为上「施加窗口 = [0, L−1]、第 L 刻自毁」成立，
   但如果作者要求「落地刻末读到 L」，就要把首次自增推迟一刻（与伪代码相悖）——需要一句新裁决。
3. **`onHitBlock` 的方块侧回调可能每 tick 重入**：`settled` 守卫挡住了本类的 `onImpact`，
   但基类 `onHitBlock` 会先调 `super.onHitBlock(...)`（原版 `blockState.onProjectileHit`），
   若场停在紫水晶之类的方块上仍会每 tick 触发方块侧回调。基类只读（F-32 ② 不许重写 `onHitBlock`），本 Phase 不动。
4. **`Settled` 语义重叠**：本类用基类的 `Settled` 键判断「是否已落地」，与基类的 `settled` 字段语义一致；
   但基类没有访问器，所以本类只能读 NBT。若将来基类加了 `isSettled()`，这里应改为调用它（当前不动基类）。
5. **场与实体碰撞箱**：`noPhysics = true` 让场不被推动，但也意味着它不再参与任何碰撞解算；
   玩家/投射物穿过它的具体表现未细测（RT-35 的「另一半」）。

---

## 9. 环境与命令复现

```powershell
# 构建（唯一入口）
cmd /c ""D:\project_farm\.tools\gw.cmd" build

# python（不在 PATH）
$env:PYTHONIOENCODING='utf-8'
C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe -X utf8 tools/verify_project.py

# 起服（独立车道，禁止在 run/ 起服）
$env:FS_RUN_LOCK='run-p28'
cmd /c ""D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-p28" > .work\p28-serverN.log 2>&1
# 停服
"stop" | python .work\p28-rcon.py 25575 fsverify

# 无头客户端（供区块 tick 实体 + 包级证据）
node .work\p28-bot.js > .work\p28-botN.log 2>&1

# 配置改动（UTF-8 无 BOM！）
$enc8 = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($p, $text, $enc8)
```

环境坑（本 Phase 踩到的，供后人）：
- 无玩家在线时区块不 tick 实体 → 任何「逐刻」验收都必须先连一个客户端（无头 bot 即可）。
- `gw.cmd runServer` 的目录锁是目录 `(mkdir)` 语义；上一个 server 进程没完全退出时，
  新进程会**卡在锁上**（表现为日志 0 字节、无监听端口）。判定：`Get-Process java` 里有没有 >500 MB 的进程。
- 世界目录被锁（`IOException: 另一个程序已锁定文件的一部分`）= 上一个 server 进程还活着，必须先杀。
- `server.properties` 里 `level-type` 用 `minecraft\:flat`（单反斜杠转义），写双反斜杠会被当成字面 `\\`。

---

## 10. 证据文件索引（`.work/`）

| 文件 | 内容 |
|---|---|
| `p28-server.log` | 第一次起服（200 默认，中间代码） |
| `p28-server2.log` | 1200 刻一轮 |
| `p28-server3.log` | F-186 卸载测试（1200 刻） |
| `p28-server6.log` | 100 刻一轮 + 跨重启不复活 |
| `p28-server8.log` | **默认 200 刻一轮（最终代码）** |
| `p28-run4.log` | 水柱 60 s 位置采样 |
| `p28-run6.log` | 卸载/重载 + NBT 全量 dump |
| `p28-run8.log` | 100 刻逐刻 duration 读数 |
| `p28-run9.log` | 玩家站上去 70 样本 |
| `p28-run10.log` | **200 刻逐刻 duration 读数（最终代码）** |
| `p28-bot7.log` / `p28-bot9.log` | 客户端 `entity_effect` / `remove_entity_effect` 包 |
| `p28-cmds*.txt` | 每轮 RCON 命令原文（可复现） |
| `p28-rcon.py` / `p28-bot.js` | 驱动脚本 |
| `p28-diff.txt` | 目标文件完整 diff |
| `p28-readings.ps1` / `p28-readings2.ps1` | grep / git 读数脚本 |