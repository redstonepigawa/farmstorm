# F-64 证据（v0.0.2 · 两个效果 + 两个注册器 + sounds.json）

HEAD: ab33fff ｜ **未 commit** ｜ 改动：新增 5 个文件、改 1 个既有文件（`Farmstorm.java` +5 行）

## 0. 交付物与 diff

    $ git diff --stat
     src/main/java/com/dashpig/farmstorm/Farmstorm.java | 5 +++++
     1 file changed, 5 insertions(+)

新增（未跟踪）：`effect/WildHunterEffect.java`、`effect/ZangiefPiledriverEffect.java`、
`registry/FarmstormMobEffects.java`、`registry/FarmstormSounds.java`、
`src/main/resources/assets/farmstorm/sounds.json`。

`Farmstorm.java` 只加：2 行 import、1 行注释、2 行 `REGISTER.register(modBus)`；自检文案一字未动。

## 1. 四条验收（专用服务器，`-PfsRunDir=run-f64`）

命令源：`FakePlayerFactory.getMinecraft(overworld)` 作玩家命令源（RCON 控制台没有 `@s`，见 F-65 §8），
权限用 `.withPermission(4)` 提升，命令走 `CommandDispatcher.parse/execute`，回调记 success/result。
`.work/f64-server-d.log`（含探针的构建）。

### 验收 1：两个效果都能施加

    [探针] /effect give @s farmstorm:wild_hunter 10 → 返回值 1，回调 success=1 result=1
    [探针]   → ActiveEffects=[{id=farmstorm:wild_hunter, amplifier=0, duration=200, visible=true, show_icon=true}]
             | NBT=[{duration:200,id:"farmstorm:wild_hunter","neoforge:cures":["protected_by_totem","milk"],show_icon:1b}]
    [探针] /effect give @s farmstorm:zangief_piledriver 10 2 → 返回值 1，回调 success=1 result=1
    [探针]   → ActiveEffects=[… {id=farmstorm:zangief_piledriver, amplifier=2, duration=200, visible=true, show_icon=true}]
             | NBT=[…,{amplifier:2b,duration:200,id:"farmstorm:zangief_piledriver","neoforge:cures":[…],show_icon:1b}]

`wild_hunter`：可见 + 带图标（`show_icon:1b`），图标文件 `textures/mob_effect/wild_hunter.png` = **18×18** 已进 jar。
`zangief_piledriver`：**amplifier=2 成立**；但裸命令下 `show_icon:1b`（见 §4 的坑）。

### 验收 2：相位通道的 NBT

    [探针] /data get entity @s active_effects → 返回值 2，回调 success=1 result=2
    [探针] /data get entity @s ActiveEffects → 失败 CommandSyntaxException: Found no elements matching ActiveEffects

**1.21.1 的键名是 `active_effects`（小写）**：任务书写的 `ActiveEffects` 在 1.21.1 上按该路径取不到元素
（`LivingEntity.TAG_ACTIVE_EFFECTS = "active_effects"`），改成 `/data get entity @s active_effects` 才返回。
探针打印的就是该路径的同一份 NBT（`saveWithoutId().get("active_effects")`），内容见验收 1 第二段。

### 验收 3：两个 sound_event

    [探针] 注册表 sound_event: wild_hunter_loop=farmstorm:wild_hunter_loop / zangief_piledriver=farmstorm:zangief_piledriver
    [探针]   /playsound 补全数据源（BuiltInRegistries.SOUND_EVENT 键集）含两个键=true
    [探针] /playsound farmstorm:zangief_piledriver master @s ~ ~ ~ 1 1 → 返回值 1，回调 success=1 result=1
    [探针] /playsound farmstorm:wild_hunter_loop master @s ~ ~ ~ 1 1 → 返回值 1，回调 success=1 result=1

`/playsound` 的参数解析就是走 `BuiltInRegistries.SOUND_EVENT`，两条命令返回 1 即证明两个事件在补全源里。

### 验收 4：专用服务器无 NoClassDefFoundError

`.work/f64-server-e.log`（**无探针的最终构建**）：

    [modloading-worker-0/INFO] [farmstorm/]: 自检 作物 19 种：投掷型 10 / 部署型 6 / 食用型 3；破障类 4 → OK
    [modloading-worker-0/INFO] [farmstorm/]: 自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK
    [Server thread/INFO] [minecraft/DedicatedServer]: Done (5.328s)! For help, type "help"
    BUILD SUCCESSFUL in 46s
    NoClassDefFoundError 计数 = 0    （Exception|ERROR 3 行全是环境的 WMI COM 警告）

收尾走 RCON `stop`（`run-f64/server.properties` 里 enable-rcon/25586），**gradle 退出码 0**，不是 kill。

## 2. 关键代码

`WildHunterEffect`（挂 / 卸 / 幂等）：

    public static final ResourceLocation SPEED_ID = Farmstorm.id("wild_hunter_speed");
    public static final ResourceLocation ATTACK_SPEED_ID = Farmstorm.id("wild_hunter_attack_speed");
    public static final ResourceLocation ATTACK_DAMAGE_ID = Farmstorm.id("wild_hunter_attack_damage");

    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        apply(attributes, Attributes.MOVEMENT_SPEED, SPEED_ID, SPEED_BONUS_PERCENT, amplifier);
        …
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {   // 1.21.1 是单参，没有 amplifier
        clear(attributes, Attributes.MOVEMENT_SPEED, SPEED_ID);
        …
    }

    private static void apply(AttributeMap attributes, Holder<Attribute> attribute, ResourceLocation id,
                              double percentPerLevel, int amplifier) {
        AttributeInstance instance = attributes.getInstance(attribute);
        if (instance == null) { return; }
        instance.removeModifier(id);
        instance.addPermanentModifier(new AttributeModifier(id, percentPerLevel * (amplifier + 1) / 100.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

`ZangiefPiledriverEffect`：只有 `super(MobEffectCategory.HARMFUL, 0x000000)` + 三个相位常量
（`PHASE_REVERSED=0 / PHASE_LEVITATING=1 / PHASE_FALLING=2`），无属性、无 tick、不读 config。

`FarmstormSounds`：

    REGISTER.register("wild_hunter_loop",
            () -> SoundEvent.createVariableRangeEvent(Farmstorm.id("wild_hunter_loop")));

## 3. `sounds.json`（完整）

    {
      "wild_hunter_loop": {
        "category": "player",
        "subtitle": "subtitles.farmstorm.wild_hunter_loop",
        "sounds": [
          { "name": "farmstorm:wild_hunter_loop", "stream": false, "attenuation_distance": 16 }
        ]
      },
      "zangief_piledriver": {
        "category": "player",
        "subtitle": "subtitles.farmstorm.zangief_piledriver",
        "sounds": [
          { "name": "farmstorm:zangief_piledriver", "attenuation_distance": 16 }
        ]
      }
    }

jar 内实测：`assets/farmstorm/sounds.json` + `sounds/wild_hunter_loop.ogg`(1782670 B) + `sounds/zangief_piledriver.ogg`(33568 B)。
**RT-14 关闭**：`SoundDefinition.Sound` 确有 `attenuation_distance`（int，默认 16）字段。

## 4. 两个必须下游收口的坑

### 4.1 「隐藏」的类级钩子在 1.21.1 不存在（RT-15 关闭）

`MobEffect`（1.21.1）只有 `applyEffectTick` / `shouldApplyEffectTickThisTick` / `addAttributeModifiers` / `removeAttributeModifiers`，
**没有** `shouldRenderHUD()` / `shouldRenderInvText()` / `shouldRenderIcon()`。隐藏只能由**施加方**传
`MobEffectInstance(…, ambient, visible=false, showIcon=false)`（TECH §5.2 t=0 行已是这样写）。

裸命令的实测差异（同一次运行，只差一个 `hideParticles` 参数）：

    /effect give @s farmstorm:zangief_piledriver 10 2       → …{amplifier:2b,…,show_icon:1b}
    /effect give @s farmstorm:zangief_piledriver 10 2 true  → …{amplifier:2b,…,show_icon:0b,show_particles:0b}

原因：`/effect give` 用五参构造 `MobEffectInstance(holder, duration, amplifier, false, !hideParticles)`，
而五参构造把 `showIcon` 直接取成 `visible`（`MobEffectInstance.java:65`）→ 默认 `show_icon=1b`。

**影响**：任务书验收 1 的「HUD 无图标」与验收 2 的 `show_icon=false`，按字面命令（不带 `true`）**过不了**，
与 F-64 代码无关，是原版命令的默认参数。两条可选收口（都不是本任务能改的）：
① 验收命令补 `true`；② 由**客户端**用 NeoForge 的 `RegisterClientExtensionsEvent` +
`IClientMobEffectExtensions#isVisibleInGui/isVisibleInInventory` 返回 false 作第二道保险（只挡画面，不改 NBT）。

### 4.2 属性修饰器的残余问题：**不会留残余**（F-77 / F-76 可放心）

三条路径全部实测（`.work/f64-server-d.log`）：

    玩家属性基线（无效果） speed=0.10000000149011612 attack_speed=4.0 attack_damage=1.0
    玩家属性 施加后（应 0.13 / 5.2 / 1.4） speed=0.13000000193715097 attack_speed=5.2 attack_damage=1.4
    玩家属性 命令清除后（应回基线） speed=0.10000000149011612 attack_speed=4.0 attack_damage=1.0
    僵尸 属性基线 speed=0.23 attack_damage=3.0
    僵尸属性 施加 1 秒后 speed=0.299 attack_damage=4.199999999999999 | ActiveEffects=[{… duration=20 …}]
    僵尸 第 19 刻（效果应仍在） speed=0.299 attack_damage=4.199999999999999 | duration=1
    僵尸 第 24 刻（应已自然过期并卸下） speed=0.23 attack_damage=3.0 | ActiveEffects=[]

对应源码：`LivingEntity.onEffectAdded`（服务端）→ `addAttributeModifiers`；
`onEffectUpdated(…, true, …)` → 先 `remove` 再 `add`（相位重设 / 续时不叠加）；
`onEffectRemoved` ← `removeEffect`（`/effect clear`）与 `tickEffects` 的自然过期（`LivingEntity.java:792`）同一个出口。
另外 `ServerPlayer.restoreFrom`（换维度 / 重生）对每条效果 `addEffect` 重建，因此也不丢。

## 5. 硬门禁

    $ .tools\gw.cmd build -PfsRunDir=run-f64
    > Task :build
    BUILD SUCCESSFUL in 10s
    exit=0 ；warning/WARN/deprecat 行数 = 0
    产物 build/libs/farmstorm-0.0.2+mc1.21.1.jar（2161852 B）

`verify_project.py`：与 F-65 同形（子集检查，本轮新增 4 个注册对象它看不见）：

    == 2. 64 个注册 ID 逐字核对（全源码树） ==
      已核 registry 对象: 62 / 64
    结论：FAIL 0 项，WARN 0 项（exit 0）

探针删净：`Probe` 计数 src=0 / build\classes=0 / jar=0；`f64-server-e.log` 里「探针」行数 = 0。

## 6. 运行过程里的两次非生产性收尾

- run A：探针命令源权限是 0，所有命令静默失败（`success=-1`、ActiveEffects 一直是空）→ 改 `.withPermission(4)` 后 run B 正常。
- run C：僵尸没有 `ATTACK_SPEED` 属性，`getAttributeValue` 抛 `IllegalArgumentException`，探针中途停下、java 被手动 kill，
  gradle 报 `exit code: 1`（人为终止，不是缺陷）；run D 改为缺属性打印 `n/a` 后正常收尾。
