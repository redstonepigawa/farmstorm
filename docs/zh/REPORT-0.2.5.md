# v0.2.5 变更汇报（1.20.1 分支 → 1.21.1 主线）

> 写于 2026-11-07。目的：把 1.20.1 分支 v0.2.5 的改动交代给负责 1.21.1 实现的人。
> 结论先说：**只有「植物学家职业贴图」这一条需要 1.21.1 侧动手**；崩溃修复是 1.20.1 移植期引入的、1.21.1 本来就没有；其余是 1.20.1 独有的加载器兼容改动，仅供知悉。

---

## 0. 版本与产物

| 项 | 值 |
|---|---|
| 分支 | `1.20.1` |
| 版本 | **0.2.4 → 0.2.5**（`gradle.properties` 的 `mod_version`） |
| 产物 | `farmstorm-0.2.5+mc1.20.1.jar` — 1,140,833 B |
| SHA256 | `F085288C8AAF0EE6588CF9D7405E7117BC6A8BCA87732D0DF6B401303E057E8C` |
| 加载器 | Forge 47.x **与** NeoForge 47.x 共用同一个 jar（1.21.1 侧不受此影响） |

> 注意：0.2.5 **只存在于 1.20.1 分支**。1.21.1 主线当前仍是 0.2.4，两边版本号从此各自演进。

---

## 1. 【需要 1.21.1 跟进】植物学家职业贴图缺失

### 现象

真机日志每次渲染该职业村民都会刷一条：

```
WARN  Failed to load texture: farmstorm:textures/entity/villager/profession/botanist.png
java.io.FileNotFoundException: farmstorm:textures/entity/villager/profession/botanist.png
```

不崩溃，但植物学家村民的职业外观会退回缺失贴图（紫黑格）。

### 已核实的事实

- 这个文件在 **1.20.1 分支和 1.21.1 主线里都不存在** —— 是原模组就漏的资源，不是移植丢失。
- 因此 **1.21.1 主线同样有这个问题**，需要一并补。

### 补什么

两个文件，同名同目录：

```
src/main/resources/assets/farmstorm/textures/entity/villager/profession/botanist.png          (64×64 RGBA)
src/main/resources/assets/farmstorm/textures/entity/villager/profession/botanist.png.mcmeta   (42 B)
```

`.mcmeta` 内容（**是草帽渲染配置，不是动画配置**，不要当成动画帧表）：

```json
{ "villager": { "hat": "full" } }
```

- PNG 是**透明职业叠加层**：脸部与腿部区域保持透明，让原村民皮肤和群系服装继续显示。
- 路径规则：`assets/<modid>/textures/entity/villager/profession/<职业注册路径>.png`；职业 id 若是 `farmstorm:botanist` 就是上面这个路径。
- 贴图来源：本项目的材质包，64×64 原版村民 UV，UV 派生自原版 farmer 帽子 + leatherworker 围裙（受 Minecraft EULA / Brand and Asset Guidelines 约束，同现有「原版贴图派生图」一段）。

### 验收

起客户端后进入有该职业村民的场景，日志里**不再出现**这条 `FileNotFoundException`，且村民戴上草帽、围裙为森林绿。

---

## 2. 【仅 1.20.1，1.21.1 不需要动作】临时附魔列表崩溃修复

### 现象（真机游玩崩溃）

```
Description: Ticking block entity
java.lang.UnsupportedOperationException
    at com.google.common.collect.ImmutableList.set(ImmutableList.java:528)
    at com.dashpig.farmstorm.util.TempEnchantHandler.grant(TempEnchantHandler.java:58)
    at com.dashpig.farmstorm.behavior.impl.TorchflowerAura.grantFireAspect(TorchflowerAura.java:110)
    at com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity.onScan(...)
```

触发条件：站在**觉醒火把花**光环里给同一把剑反复授予火焰附加 —— 第一次正常，后续扫描必然崩溃。

### 根因（移植时引入）

1.20.1 把「临时附魔」从数据组件改成了物品 NBT，读取走 codec：

```java
// 有问题的写法（v0.2.4）
return TempEnchantRecord.LIST_CODEC.parse(NbtOps.INSTANCE, list)
        .result()
        .orElseGet(ArrayList::new);
```

DataFixerUpper 解出来的列表是 **ImmutableList**；而 `grant()` / `revokeExpired()` 会就地 `set` / `add`。
物品上还没有该 NBT 键时走 `new ArrayList<>(0)` 分支，所以第一次正常；一旦写进去过，第二次读回来就是不可变的 → `UnsupportedOperationException`。

### 修法（v0.2.5）

```java
// 读出来必须再包一层可变列表
return new ArrayList<>(TempEnchantRecord.LIST_CODEC.parse(NbtOps.INSTANCE, list)
        .result()
        .orElseGet(ArrayList::new));
```

并全项目扫描了同类写法：只有这一处受影响（另一处 codec 解析返回单条记录，无可变操作）。

### 为什么 1.21.1 不受影响

1.21.1 主线 `TempEnchantHandler.read()` 本来就是防御性拷贝：

```java
return new ArrayList<>(stack.getOrDefault(FarmstormDataComponents.TEMPORARY_ENCHANTMENTS.get(), List.of()));
```

**这条不用改，也不要「顺手对齐」**。写进来只是为了让两边记录一致：1.20.1 的 codec 路径丢了这层拷贝，1.21.1 的组件路径没丢。

---

## 3. 仅供知悉：1.20.1 独有的加载器兼容改动

下面这些是「1.20.1 上要同时兼容 Forge 47 与 NeoForge 47」带来的，**1.21.1（NeoForge 21.1）不适用**，列出来只为避免以后有人照抄到主线：

| 项 | 1.20.1 的做法 | 为什么 1.21.1 不需要 |
|---|---|---|
| mod 类构造器 | **必须无参**，总线用 `FMLJavaModLoadingContext.get().getModEventBus()` 取 | Forge 47 只认 `(FMLJavaModLoadingContext)` 或无参；NeoForge 21.1 支持 `(IEventBus)` 等多参 |
| 步高属性 | `ForgeMod.STEP_HEIGHT_ADDITION`（Forge 只有这个名，NeoForge 里它是 `STEP_HEIGHT` 的别名） | 1.21.1 有内建 `Attributes.STEP_HEIGHT` |
| 数据包目录 | `recipes/` `advancements/` `loot_tables/` `tags/blocks/` `tags/items/` | 1.21 用单数：`recipe/` `advancement/` `loot_table/` `tags/block/` |
| 产物重映射 | 自建 SRG 重映射链路（NG7 无 reobf 任务） | moddev 产出的 jar 已是可分发型 |
| 世界渲染 | 相机变换只存在于 `event.getPoseStack()`，`RenderSystem` 的 ModelViewMat 是 GUI 残留 | 1.21.1 里 `getModelViewMatrix()` 就是相机矩阵 |
| 相机事件定序 | 桑吉尔蔗必须 `EventPriority.LOW`，排在输入锁冻结之后 | 1.21.1 用 `CalculatePlayerTurnEvent`，不存在这个冲突 |

---

## 4. 1.21.1 侧待办清单

- [ ] 补 `botanist.png` + `botanist.png.mcmeta` 到 `src/main/resources/assets/farmstorm/textures/entity/villager/profession/`
- [ ] 起客户端确认日志不再出现该 `FileNotFoundException`，村民外观正确
- [ ] （可选）若决定给 1.21.1 也发一版，版本号自行决定；两边版本号已经解耦

---

## 5. 1.20.1 侧的完整改动清单（v0.2.4 → v0.2.5）

| # | 改动 | 类型 |
|---|---|---|
| 1 | `TempEnchantHandler.read()` 加 `new ArrayList<>()` 包装 | 崩溃修复（1.20.1 独有） |
| 2 | 新增 `botanist.png` + `.mcmeta` | 资源补齐（两端共有缺口） |
| 3 | `mod_version` 0.2.4 → 0.2.5 | 版本 |
| 4 | README 更新版本号、双加载器说明、素材表新增 botanist 行 | 文档 |

构建与发布链路（1.20.1 专用，1.21.1 不适用）：`build-1.20.1.cmd` → `remap-1.20.1.cmd` → `dist/` → GitHub Release。

---

## 6. v0.2.6 追加：末影龙剪影崩溃（**1.21.1 主线同样存在**）

### 现象（真机游玩崩溃）

```
Description: Unexpected error
java.lang.ClassCastException: class net.minecraft.client.renderer.entity.EnderDragonRenderer
  cannot be cast to class net.minecraft.client.renderer.entity.LivingEntityRenderer
    at com.dashpig.farmstorm.client.WildHunterOutline.onRenderGuiPre(WildHunterOutline.java:126)
```

触发条件：吃**狂野苹果** → 获得狂野猎人效果 → 红色剪影开始渲染 → 附近存在**末影龙**时崩溃。

### 根因

剪影的循环里直接强转：

```java
LivingEntityRenderer renderer =
        (LivingEntityRenderer) minecraft.getEntityRenderDispatcher().getRenderer(entity);
```

**并非所有 `LivingEntity` 的渲染器都是 `LivingEntityRenderer`**。末影龙就是反例：`EnderDragonRenderer` 直接继承 `EntityRenderer`。
对象本身仍是 `LivingEntity`（所以能进候选集），但强转必然失败。

### 修法（v0.2.6）

```java
if (!(minecraft.getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer<?, ?> living)) {
    continue;   // 末影龙等非 LivingEntityRenderer 的目标跳过，不画剪影
}
((LivingEntityRenderer) living).render(entity, yaw, capturedPartialTick, poseStack, shell, LightTexture.FULL_BRIGHT);
```

### ⚠️ 1.21.1 侧需要同样修

我核对过 1.21.1 主线 `src/main/java/com/dashpig/farmstorm/client/WildHunterOutline.java` 第 113~114 行，**是同一行同样的强转，同样会崩**。建议按上面的写法加 `instanceof` 守卫。

同类候选（渲染器不是 `LivingEntityRenderer` 的 LivingEntity）：末影龙；若以后接了别的 mod 的非标准生物渲染器也可能踩到——用 `instanceof` 守卫一次覆盖。

### 验收

在末影龙附近吃狂野苹果（或直接 `/effect give` 给狂野猎人），剪影正常显示其他生物、不崩溃、末影龙不画剪影。

---

## 7. v0.2.6 变更清单

| # | 改动 | 类型 |
|---|---|---|
| 1 | `WildHunterOutline` 剪影渲染加 `instanceof LivingEntityRenderer` 守卫 | 崩溃修复（**两端共有**） |
| 2 | `mod_version` 0.2.5 → 0.2.6 | 版本 |

产物：`farmstorm-0.2.6+mc1.20.1.jar` — 1,140,861 B — SHA256 `B395C8524A74F0CA59C4AB0E1EAA6BA6237DDA4D6A6E55D9ECBF0401DC6A7FE6`

---

## 8. v0.2.7：伤害来源的访问权限崩溃（**仅 1.20.1 双加载器场景**）

### 现象（用户反馈，胡萝卜陷阱引爆时崩溃）

```
Description: Ticking block entity
java.lang.IllegalAccessError: class com.dashpig.farmstorm.registry.FarmstormDamageTypes
  tried to access private method
  'DamageSource DamageSources.m_269298_(ResourceKey, Entity)'
    at com.dashpig.farmstorm.registry.FarmstormDamageTypes.cropBlast(FarmstormDamageTypes.java:37)
    at com.dashpig.farmstorm.behavior.impl.BlastCarrotTrap.trigger(BlastCarrotTrap.java:84)
```

### 根因：访问转换器（AT）差异

代码调的是 `level.damageSources().source(KEY, owner)`。这个**两参重载在原版里不是 public**，靠 access transformer 放宽。

我把两个加载器的 `META-INF/accesstransformer.cfg` 做了逐行差分，**只有 7 条差异，全部在 `world.damagesource`**：

| 成员 | NeoForge 47.1.106 | Forge 47.4.28 |
|---|---|---|
| `DamageSources.m_269079_(ResourceKey)` | 放宽为 public | **没有** |
| `DamageSources.m_269298_(ResourceKey, Entity)` | 放宽为 public | **没有** |
| `DamageSources.m_268998_(ResourceKey, Entity, Entity)` | 放宽为 public | **没有** |
| `DamageSources.f_268645_` | 放宽为 public | **没有** |
| `CombatTracker.f_268553_ / m_19298_ / m_289215_` | 放宽为 public | **没有** |

所以这段代码在 NeoForge 上编译通过、运行正常，在 Forge 上一跑就 `IllegalAccessError`。

### 修法（v0.2.7）

绕开 AT，直接用在 **vanilla 里本就是 public** 的 `DamageSource` 构造器（语义完全相同——原重载的实现就是这一行）：

```java
private static DamageSource build(Level level, ResourceKey<DamageType> key, @Nullable Entity owner) {
    Holder<DamageType> type = level.registryAccess()
            .registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(key);
    return new DamageSource(type, owner);
}
```

三个伤害类型（`void_bolt` / `crop_blast` / `zangief_slam`）统一走这个 `build()`。

### 同类风险已被扫描干净

7 条 AT 差异里有 3 条属于 `CombatTracker`，项目里 **0 处引用**；剩余 `player.damageSources().playerAttack(player)` 不在差异表内（两边都公开）。**全项目再无依赖 NeoForge 放宽的调用。**

### 1.21.1 侧：不需要改

主线 `FarmstormDamageTypes` 第 32 / 37 / 50 行同样是 `damageSources().source(KEY, owner)`，但 1.21.1 只有 NeoForge 一个加载器，AT 由 NeoForge 提供 → 正常工作。
这条**只是双加载器场景**（Forge 47 + NeoForge 47 共用一个 jar）才会踩到。

---

## 9. v0.2.7 变更清单

| # | 改动 | 类型 |
|---|---|---|
| 1 | `FarmstormDamageTypes` 改用 `new DamageSource(Holder, Entity)` | 崩溃修复（**1.20.1 双加载器独有**） |
| 2 | `mod_version` 0.2.6 → 0.2.7 | 版本 |

产物：`farmstorm-0.2.7+mc1.20.1.jar` — 1,141,065 B — SHA256 `B64EAE95797807B6E3E220204EE36538953A579E1E347BB940A66F578FC140E2`
