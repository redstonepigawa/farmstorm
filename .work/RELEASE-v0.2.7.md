# Farmstorm 农场风云 v0.2.7

> Minecraft **1.21.1** · NeoForge **21.1.x** · Java **21** · modid `farmstorm`

三处改动：**撤除部署数量上限**（玩法）、**灰度改自持 `PostChain`**（渲染，光影模组下也能灰）、**莫洛托夫落点爆炸威力默认 0 → 1**（数值）。存档格式不变。

注册面 **92** / server config **175 键** / 语言键 **321 × 2** / 网络包 **2** / 进度 **16** / 转换配方 **21** 六组数字**一项未变**；协议串仍是 `3`，Mixin 仍 **0**。

## 本版改了什么

### ① 撤除部署数量上限

- 原来放置部署物走三道闸：5 刻冷却 → **单玩家 32 个** → **单区块 8 个**。后两道撤除，**只保留 5 刻冷却**。
- 冷却挡的是「放 → 立刻拆 → 再放」的抖动，与数量无关，因此保留。
- 陷阱的总量仍由 FIFO 名额兜底（每玩家 16 条，超出挤掉最先放的），与这道闸无关。
- 判定仍发生在扣物品之前：被拒绝时物品不消耗、不产生掉落。

### ② 灰度改自持 `PostChain`

- 根因：原来走 `GameRenderer#loadEffect`。光影模组（Oculus / Iris 之类）接管后处理后，会周期性清掉外部装上的链 —— 实机日志表现为「加载」与「被外部关闭」交替出现、一个会话刷 700 余次装载，而画面从未灰过一帧。
- 现在自己持有 `PostChain`，在 `AFTER_LEVEL`（地形 / 实体 / 粒子 / 天气都画完、GUI 还没开始）直接 `process()`。仍是「**只灰世界、不灰 GUI**」（V-56 / V-57）。
- Windows 尺寸变化时 `resize`；连续失败 20 次本会话放弃并只留一条 error 日志；退出世界时释放 GL 资源。

### ③ 莫洛托夫落点爆炸威力默认 0 → 1

- `molotov.triggerExplosionPower` 默认值改为 **1**。原来 0 时那次爆炸只当触发器，没有击退与伤害衰减半径，只剩放火 / 岩浆。
- 方块破坏恒为 `NEVER_BREAK`（A-10），与威力无关 —— 改成 1 **不会**让莫洛托夫开始拆方块。

## 从 v0.2.6 升级必看

| 项 | 会发生什么 | 怎么办 |
|---|---|---|
| **存档无损** | 本版不改注册 id、不删物品、不改语言键集、协议串仍是 `3` | 老存档直接进 |
| **已有 toml 的 `triggerExplosionPower` 不会自动变 1** | NeoForge 只补键 / 删键，不改已存在键的值 | 想要新默认值：删掉 `config/farmstorm-server.toml` 让它重生成，或手改那一行成 `1` |
| 已放置的部署物 | 不受影响；上限只在「放置时」判 | 无需操作 |
| 灰度 | 装 / 不装光影都应能灰世界；若失败会在日志里留 `灰度 PostChain ... 失败` | 有异常请把日志发出来 |

## 已知限制

- **灰度的观感未实机验证**：本版只做到「自持链 + 无 `loadEffect` 残留 + 编译与静态检查全绿」，验收环境没有图形客户端；请在**有光影**与**无光影**两种环境下各看一眼。
- 撤除上限后，单玩家部署物数量**只受自身建造速度约束**（只剩 5 刻冷却）；服务端性能未做大密度压测。
- 连线线框与核心浮窗仍需要装本模组的客户端；纯原版客户端能进服但看不到。
- 末影龙那类「渲染器不是 `LivingEntityRenderer`」的生物**不画剪影**（v0.2.6 起，属于预期行为）。

## 数字（v0.2.7）

注册对象 **92**（11 类 + 菜单 1）；server config **175 键 / 60 组**；client config **3 键**；语言键 **321 × 2**；Java 源文件 **151**
进度 **16** 条；转换配方 **21** 条；培养器投入物 **17** 个；交易 listing **26** 条；网络包 **2** 个（协议串 `"3"`）；Mixin **0**

## 文件校验（v0.2.7）

```
farmstorm-0.2.7+mc1.21.1.jar            1,128,686 B
  SHA256 482817FF37A20FD6B7E1ADB0213867A751352A3C4B89840E29DFD6612E6A6AFE
farmstorm-0.2.7+mc1.21.1-sources.jar    1,074,835 B
  SHA256 5E4CFF2A1243354C8B90223C20B2CD99786D9DD488B4C6EFC98CE2A565E68BF0
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