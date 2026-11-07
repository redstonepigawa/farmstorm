# Farmstorm 农场风云 v0.2.6

> Minecraft **1.21.1** · NeoForge **21.1.x** · Java **21** · modid `farmstorm`

**崩溃修复版，零功能改动、零数值改动。** 修掉红色剪影的一个 `ClassCastException`：**附近有末影龙时客户端会崩**。

注册面 **92** / server config **175 键** / 语言键 **321 × 2** / 网络包 **2** / 进度 **16** / 转换配方 **21** 六组数字**一项未变**；协议串仍是 `3`，Mixin 仍 **0**。

## 本版改了什么

- **根因**：`WildHunterOutline` 重画红色剪影时，对每个目标的实体渲染器做了**无守卫强转** —— `(LivingEntityRenderer) getRenderer(entity)`。
- 但**并非所有 `LivingEntity` 的渲染器都是 `LivingEntityRenderer`**：**末影龙**用的是 `EnderDragonRenderer`（直接继承 `EntityRenderer`），于是附近有龙时抛 `ClassCastException`，客户端直接崩。
- **修法**：改成 `instanceof LivingEntityRenderer<?, ?>` 守卫，不满足的目标**跳过不画**；通配符→具体类型的那一步集中到一个显式 `@SuppressWarnings("unchecked")` 的私有方法里，不再散落在渲染循环。
- 其余生物的剪影表现**完全不变**（变换链、放大 `1.008`、重入守卫、顶点格式过滤都没动）。

## 从 v0.2.5 升级必看

| 项 | 会发生什么 | 怎么办 |
|---|---|---|
| **存档无损** | 本版不改注册 id、不动配置键、语言键不变、协议串仍是 `3` | 老存档直接进 |
| 配置不用删 | server config 仍 175 键 / 60 组；client config 仍 3 键 | 无需操作 |
| 行为变化 | 末影龙（以及任何把渲染器写成非 `LivingEntityRenderer` 的生物）**不再画红色剪影**，也不再崩 | 无需操作 |
| 客户端与服务端 | 协议串不变 ⇒ 可混版，但仍建议两端同版 | 同版最省事 |

## 已知限制

- **末影龙场景未实机复现**（验收环境没有图形客户端）：已验证的是「无守卫强转在全仓已归零、守卫覆盖该分支、`build` 与静态检查全绿」，实机复现请按 `吃狂野苹果 → 靠近末影龙` 走一遍。
- 连线线框与核心浮窗仍需要装本模组的客户端；纯原版客户端能进服但看不到。
- 老存档里已超过 4 个据点核心不会被回收；拆掉核之后绑不回去的机器留原地、永久不可用。

## 数字（v0.2.6）

注册对象 **92**（11 类 + 菜单 1）；server config **175 键 / 60 组**；client config **3 键**；语言键 **321 × 2**；Java 源文件 **151**
进度 **16** 条；转换配方 **21** 条；培养器投入物 **17** 个；交易 listing **26** 条；网络包 **2** 个（协议串 `"3"`）；Mixin **0**

## 文件校验（v0.2.6）

```
farmstorm-0.2.6+mc1.21.1.jar            1,127,673 B
  SHA256 53BA746F2179C89C1E6285D966DBA16C82A4C9CCA324FF9EFF0ACACBF19B1524
farmstorm-0.2.6+mc1.21.1-sources.jar    1,073,475 B
  SHA256 D01B0FC88BFB88616A0DB1A2E29C1E07450EDFCA0CD0B47C1AA18B5D465DF5AE
```

> `-sources.jar` 是源码包，给 IDE 看实现用，**不要**放进 `mods`。

---
## 许可 / License

本项目自 **2026-11-07** 起采用 **MIT 许可证**（根目录 [`LICENSE`](https://github.com/redstonepigawa/farmstorm/blob/main/LICENSE)，`Copyright (c) 2026 redstonepigawa`）。

> 第三方素材（三段音频）**不适用 MIT**，版权归各自权利人，见下一节。

---

## 素材来源与非商业声明（务必随 Release 一起保留）

本项目**不是全部原创**：下表里**只有三段音频**取自第三方（**版权归各自权利人**，本项目不对其主张任何权利）；**其余各行均为本项目自制**，适用 [MIT 许可证](https://github.com/redstonepigawa/farmstorm/blob/main/LICENSE)。

| 产物 | 处理 |
|---|---|
| `sounds/wild_hunter_loop.ogg` | 转 mono / 64 kbps Ogg Vorbis（保留 48 kHz），47.14 s（源音频取 3m04s 起的剪辑） |
| `sounds/zangief_piledriver.ogg` | 转 mono / 64 kbps Ogg Vorbis（保留 48 kHz），4 秒段 |
| `sounds/friend_chives.ogg` | 转 mono / 64 kbps Ogg Vorbis，取前 10 秒 |
| `textures/mob_effect/wild_hunter.png` | 本项目自制；居中裁切缩放为 18×18 |
| `textures/mob_effect/q_bounce.png` | 本项目自制；居中裁切缩放为 18×18 |
| `textures/item/friend_chives.png`、`plain_chives_seeds.png` | 本项目自制；缩放为 16×16 |
| `textures/block/friend_chives_stage0..3.png` | 本项目自绘占位（洋红 `#D81E8A`） |
| `textures/block/outpost_core_{top,side,bottom}.png` | 本项目自制；原样拷入（32×32） |
| `textures/item/wild_apple.png`（v0.1.1） | 本项目自制；原图直用，不缩放、不取色（64×64） |
| `textures/block/cultivator_a_{top,side,bottom}.png` + 两张静态首帧（v0.2.0） | 本项目自制；帧条原样拷入，静态首帧取 `editable_frames/{side,top}_00.png` |
| `textures/entity/villager/profession/botanist.png` + `.png.mcmeta`（v0.2.5） | 本项目自制；64×64 原版村民 UV 的透明职业叠加层（草帽由 mcmeta 的 `{"villager":{"hat":"full"}}` 打开，脸部与腿部保持透明） |

### 使用条件（务必照此分发）

1. **非商业**：不得将上述**三段音频**或其转换产物用于任何商业用途（表内其余自制贴图不受此限，按 MIT 分发）。
2. **署名**：转载、二次分发本模组时，必须连同本表一并保留。
3. **可移除**：若**上述三段音频**的权利人提出异议，维护者会移除对应音频并发布替代版本（替换为 CC0 / 自录音源）。

### 关于原版贴图派生图

`textures/item/*.png` 与 `textures/block/*.png` 里大部分是**从原版客户端 jar 读出来再变换**的派生图：受 **Minecraft EULA / Brand and Asset Guidelines** 约束，本项目不对其主张任何权利；仓库里只有变换后的小尺寸占位图，不含原版 jar 的原始文件。

### 已知风险（维护者注）

本仓库现在是**公开**仓库。三段音频属非商业自用，但**再分发**（尤其被第三方镜像/整合包收录）仍存在被投诉的可能：若权利人提出异议，维护者会按上面的「可移除」条款替换为 CC0 / 自录音源。想彻底规避风险的话，也欢迎优先换成 CC0 音源。