# 05 号 1.20.1 移植分阶段提示词

> 投喂方式：**先投 04 号一次**，此后每个 Phase 开工时只投对应的一节；投一节、做一节、按该节 §6 回报后再投下一节。
> 只读上游：00（决策）/ 01（设计）/ 02（技术）/ 03（任务）。本文件**不改口径**，只给「这一阶段做到什么程度算完」。

---

## 0. 固定口径（每节开头照此执行）

```text
① 红线：Mixin 0；网络包 2 个；不改 1.21.1 主线；不留 net.neoforged 兼容层；不动玩法数值。
② 本移植无中间态窗口：任一阶段启动自检变红即真 bug，不许记“期望红”。
③ 命名空间目标 = net.minecraftforge.*；协议串 = "1"；注册面 = 91（11 类）。
④ 判据只认五类读数：计数 / 日志整行 / /data get / 源码 grep / 像素或字节。
⑤ 构建必须走 D:/project_farm/.tools（Gradle 8.10 + JDK 21 + 镜像代理 + java.base 补丁）。
```

---

## Phase 1 · 构建链（M1，F-001 ~ F-003）

### 1.1 本阶段要做什么

把工程从 moddev 换到 NG7，让 `build` 与 `runServer` 都能跑；**不碰任何玩法代码**。

### 1.2 前置条件

- 镜像代理在 `127.0.0.1:8899` 监听（P-04）。
- `D:/project_farm/.tools` 可用（Gradle 8.10 / jdk21 / jdk-patch）。
- JDK 17 路径存在：`%APPDATA%/.minecraft/runtime/java-runtime-beta`（P-05）。

### 1.3 任务清单

| F 号 | 内容 | 人日 |
|---|---|---|
| F-001 | 骨架换 NG7 + `mods.toml` + `pack_format 15` | 2.0 |
| F-002 | 构建与运行脚本适配、jar 名 | 0.5 |
| F-003 | `runServer` 打通（`DownloadAssets` 卡点） | 1.5 |

### 1.4 出口判据

- **E-1-1** `build` 退出码 0，末尾 `BUILD SUCCESSFUL`。
- **E-1-2** `build/libs` 里有 `farmstorm-0.2.4+mc1.20.1.jar`。
- **E-1-3** `gradle :dependencies` 输出里出现 `net.neoforged:forge:1.20.1-47.1.106`。
- **E-1-4** `pack.mcmeta` 的 `pack_format` = 15；`src/main/resources/META-INF/mods.toml` 存在且 `loaderVersion="[47,)"`。
- **E-1-5** `runServer` 日志出现 `Done (` 与 `自检 注册对象` 行。**数量不作断言**：本分支无中间态窗口（03 号 §0.3 / A-001），F-009 与 Phase 2 同批落地，实测 Phase 1~2 收尾时自检已是 **91（11 类）**。
- **E-1-6** 构建期间 `8899` 一直处于监听（可在构建前、中、后各查一次）。

### 1.5 常见翻车点

- 用 moddev 试 1.20.1 → 必然 `Could not find net.neoforged:neoforge`（P-01）。
- `neo_version` 漏 MC 前缀 → 同上（P-02）。
- `installations.paths` 用分号 → 报目录不存在（P-06）。
- 代理中途被回收 → 构建立刻 `Connection refused`。
- `runServer` 的服端配置放进 `config/forge-server.toml` → Forge 不读：1.20.1 的服端配置**按存档**存放在 `<gamedir>/world/serverconfig/forge-server.toml`。
- `advertiseDedicatedServerToLan` 保持默认 `true` → dev 环境 `Done (` 之后必崩（`DedicatedServer` 引用客户端类 `LanServerPinger`）；置 `false`。

### 1.6 完成后必须回报什么

`build` 退出码与末尾三行；产物名与字节数；`mods.toml` 与 `pack.mcmeta` 原文；`runServer` 的 `Done (` 与自检整行；代理监听的三次检查结果；没做到的事逐条列。

---

## Phase 2 · 命名空间搬迁（M2，F-004 ~ F-008）

### 2.1 本阶段要做什么

把 189 行 `net.neoforged.*` import / 65 个符号按 02 号 §3 搬到 `net.minecraftforge.*`；**只求能编译**，行为留到 Phase 3。

### 2.2 前置条件

- Phase 1 的 E-1-1 ~ E-1-5 全部成立。
- 手边有 `HANDOVER-1.20.1.md` §4 的 65 符号表。

### 2.3 任务清单

| F 号 | 组 | 覆盖符号 | 人日 |
|---|---|---|---|
| F-004 | A 事件总线 / 加载器 / 配置 / 侧标记 | 6 | 2.0 |
| F-005 | B 注册面 | 5 | 2.5 |
| F-006 | C 客户端事件 + datagen | 23 | 2.5 |
| F-007 | D 通用事件 / tick / 村庄 / 爆炸 | 25 | 2.0 |
| F-008 | E 网络骨架 | 3 | 1.5 |

### 2.4 出口判据

- **E-2-1** `grep -c "net.neoforged." src/main/java` = **0**。
- **E-2-2** `grep -c DeferredHolder` = 0；`grep -c "DeferredBlock\|DeferredItem"` = 0。
- **E-2-3** `grep -c ModConfigSpec` = 0（应全部是 `ForgeConfigSpec`）。
- **E-2-4** `grep -c CustomPacketPayload` = 0。
- **E-2-5** 每组一次 `build`，五次全部退出码 0。
- **E-2-6** 启动自检整行为 `自检 注册对象 91 个：… → OK`（**91 / 11 类**）。本分支没有「先 92 再改」的中间态（03 号 §0.3）：`data_component_type` 在 1.20.1 不存在，F-009 与 Phase 2 同批落地，实测收尾即为 91。

### 2.5 常见翻车点

- 只改一半就构建 → 26 个「包不存在」（P-09），按组改、按组验。
- 造兼容包过关 → 违反 N-002。
- `TickEvent` 类事件忘记加 `phase == END` 判断 → 逻辑每刻跑两次。

### 2.6 完成后必须回报什么

每组改了哪些文件（按包分组计数）；五条 grep 的命中数（改前 / 改后）；五次 build 的退出码；自检整行；仍未解决的符号（若有）。

---

## Phase 3 · 五个子系统重写（M3，F-009 ~ F-014）

### 3.1 本阶段要做什么

把不能靠改 import 完成的部分按 02 号 §4 重写：组件 → 物品 NBT、附件 → 实体持久 NBT、网络 → SimpleChannel、配方与进度 → 1.20.1 接口、效果与投射物 → 新钩子名。

### 3.2 前置条件

- Phase 2 的 E-2-1 ~ E-2-5 全部成立。
- 已确认 RT-003 / RT-004 / RT-005 三个「1.20.1 是否存在」的 API 结论。

### 3.3 任务清单

| F 号 | 子系统 | 人日 |
|---|---|---|
| F-009 | 数据组件 → 物品 NBT | 3.0 |
| F-010 | 实体附件 → 实体持久 NBT | 3.0 |
| F-011 | 网络 SimpleChannel + 协议串 "1" | 3.0 |
| F-012 | 配方 | 2.5 |
| F-013 | 进度 | 2.0 |
| F-014 | 效果与投射物 | 1.5 |

### 3.4 出口判据

- **E-3-1** `grep -c DataComponent` = 0；注册面自检变 **91 … → OK**。
- **E-3-2** 物品 NBT 键 `FarmstormTempEnchants` 可在 `/data get entity <玩家> Inventory` 中读到，且到期消失。
- **E-3-3** `grep -c AttachmentType` = 0；实体 NBT 键 `FarmstormAiSuspend` 存在；停手恢复写回原值（抽验一只 `noAi=true` 的怪）。
- **E-3-4** 信道版本串 "1" 命中 1、"3" 命中 0；发送点 2 处；客户端 `ClientCultivatorLinks` 有内容。
- **E-3-5** `data/farmstorm/recipe` 里 21 份 `converting`；日志解析报错 0 条。
- **E-3-6** `data/farmstorm/advancement` 16 份；`converter` / `first_conversion` 在。
- **E-3-7** `grep -c shouldApplyEffectTickThisTick` = 0；`grep -c hitTargetOrDeflectSelf` = 0。
- **E-3-8** 炮台炮弹第 300 刻消失（`/data get entity` 或实体计数）。

### 3.5 常见翻车点

- 先删组件注册再补 NBT → 中间态编译/运行双红（按 A-003 同批做）。
- `ai_suspend` 存在内存字段而不是 `getPersistentData()` → 重启 / 重登后原值丢失，怪恢复不回原 `noAi`。
- 网络消息字段序与客户端解码不一致 → 收包乱码或崩（先骨架 F-008 再填）。
- 配方 JSON 直接沿用 1.21.1 的字段名但 `Ingredient` 写法不同 → 解析报错。

### 3.6 完成后必须回报什么

六个子系统的落点与判据读数（E-3-1 ~ E-3-8 逐条）；三个 RT 的核实结论；解析报错条数；混装抽验结果；没做到的事逐条列。

---

## Phase 4 · 存档签名与行为回归（M4，F-015 ~ F-016）

### 4.1 本阶段要做什么

清掉 `RegistryFriendlyByteBuf` / `RecipeInput` **两类**残留（`HolderLookup` 不作判据：1.20.1 的 `ForgeAdvancementProvider` / `DatapackBuiltinEntriesProvider` 本来就收 `HolderLookup.Provider`，当前 23 处全是合法用法），然后按 02 号 §7 跑 25 条验收、按 v0.2.4 跑 31 条边界。

### 4.2 前置条件

- Phase 3 的 E-3-1 ~ E-3-8 全部成立。
- 服务端能起、能 `stop`；客户端可用（观感类允许标未验证）。

### 4.3 任务清单

| F 号 | 内容 | 人日 |
|---|---|---|
| F-015 | 存档签名统一（两类残留清零；`HolderLookup` 不作判据） | 2.0 |
| F-016 | 25 条验收 + 31 条边界 | 4.0 |

### 4.4 出口判据

- **E-4-1** `grep -c RegistryFriendlyByteBuf` = 0；`grep -c RecipeInput` = 0；`HolderLookup` **不作判据**（1.20.1 的 `ForgeAdvancementProvider` / `DatapackBuiltinEntriesProvider` 本来就收 `HolderLookup.Provider`，23 处全合法）。
- **E-4-2** 02 号 §7 的 25 条逐条有读数（通过 / 不通过 / 未验证，后者必须给原因与替代读数）。
- **E-4-3** 31 条边界逐条成立或不成立，负向判据（B-09 / B-10 / B-11 / B-12）给「没有发生的证据」。
- **E-4-4** 不通过项 0；若有，逐条登记并回到对应任务重做（不许就地改判据）。

### 4.5 常见翻车点

- 把「未验证」写成「通过」。
- 验收不过就改判据。
- 顺手改数值「让它过」→ 违反 HC-007。

### 4.6 完成后必须回报什么

25 条的结论分布与逐条编号；31 条的结论分布；四条负向判据的原文证据；未验证项清单与替代读数；跑过的命令与关键日志行。

---

## Phase 5 · 文档回灌与检查器（M5，F-017 ~ F-019）

### 5.1 本阶段要做什么

把实测值回灌 02 / 03 / 04 / 05；把 `verify_project.py` 改成 1.20.1 的期望源；产出三份记录。

### 5.2 前置条件

- Phase 4 的 E-4-1 ~ E-4-4 全部成立。

### 5.3 任务清单

| F 号 | 内容 | 人日 |
|---|---|---|
| F-017 | 六件套回灌（数字面与判据） | 1.0 |
| F-018 | 检查器 1.20.1 期望源（91 / 11 类 / config 175） | 2.0 |
| F-019 | 验收 / 边界 / 签字三份记录 | 3.0 |

### 5.4 出口判据

- **E-5-1** `verify_project.py` 打印 `91 / 91`、`175`、`321`、`FAIL 0`。
- **E-5-2** 负向自测：把 `EXPECTED_COUNTS` 的 `items` 改成 27 → 当场 FAIL；改回 28 → 恢复。
- **E-5-3** 三份记录齐全，`#1`~`#25` 与 `B-01`~`B-12` 一条不漏。
- **E-5-4** 文档里的每个数字都能在检查器输出或日志里找到对应读数。

### 5.5 常见翻车点

- 检查器把 91 写死而不是「从文档现算」→ 下一版又假绿。
- 记录里只写「通过」不给读数。

### 5.6 完成后必须回报什么

检查器四段结论行原文；负向自测的前后读数；三份记录的章节结构与结论分布；文档回灌了哪些行。

---

## Phase 6 · 打包与签字（M6，F-020 ~ F-021）

### 6.1 本阶段要做什么

先跑 SRG 重映射产出可分发 jar（`build/libs/…-srg.jar`；NG7 7.0.171 没有 reobf 任务），再产出 `dist/` 发布件与安装说明，签红线与数字面。

### 6.2 前置条件

- Phase 5 的 E-5-1 ~ E-5-4 全部成立。

### 6.3 任务清单

| F 号 | 内容 | 人日 |
|---|---|---|
| F-020 | `dist/` + README + 安装说明 | 1.0 |
| F-021 | 最终签字（红线 + 数字面） | 1.0 |

### 6.4 出口判据

- **E-6-1** `dist/farmstorm-0.2.4+mc1.20.1.jar` 存在，SHA256 记录在案；内容必须是 SRG 重映射产物（与 `build/libs/farmstorm-0.2.4+mc1.20.1-srg.jar` 同字节，均 **1,138,762 B**），开发用的官方名 jar（1,129,491 B）不得进 `dist/`。
- **E-6-2** 安装说明里同一段出现「1.20.1」与「各放一份」；另有一段出现「协议串」与「1」。
- **E-6-3** 签字里五条红线各 1 行读数：Mixin 0、包数 2、主线未改、无兼容层、数值未动。
- **E-6-4** 数字面 8 项齐：91 / 175 / 60 / 3 / 321 × 2 / 2 / 16 / 26。

### 6.5 常见翻车点

- 直接把 `build/libs/…jar` 复制进 `dist/` → 官方名 jar，真机 `NoSuchMethodError`（实测崩在 `StringRepresentable.fromEnum`）；必须先跑 `remap-1.20.1.cmd`。
- 安装说明只写 1.20.1 却没写「同一 modid 同名版本不能混放」→ 玩家报「装了两份只有一份生效」。

### 6.6 完成后必须回报什么

jar 名与 SHA256；安装说明的两段原文；签字的八项读数；`git status` 的干净度（若用版本库）。

---

## 9. 跨 Phase 台账

| # | 横跨的动作 | 起点 | 终点 | 违反后果 |
|---|---|---|---|---|
| 1 | 注册面 92 → 91 | Phase 2 一并落地 F-009 的组件删除（**本分支无中间态窗口**，03 号 §0.3 / A-001） | Phase 2 收尾即 **91（11 类）** | 自检与检查器必须在同一批改到 91；留 92 会与实测打假。**实测**：Phase 1~2 收尾自检已是 91 |
| 2 | 协议串 | Phase 2 / F-008（骨架） | Phase 3 / F-011（真实收发） | 骨架期间客户端收不到包是预期；门禁是 E-3-4 |
| 3 | 组件 / 附件 | Phase 3 / F-009、F-010 | Phase 4 / E-4-2 的 #18 / #19 | 补 NBT 但不补恢复原值 → 怪永久不动 |
| 4 | 检查器与数字面 | Phase 3 / F-009 | Phase 5 / F-018 | 中间期检查器红属已知，必须在 F-018 收口 |

---

> 本文自检：6 个 Phase、每节 6 小节、出口判据共 `6+6+8+4+4+4 = 32` 条，全部可执行；跨 Phase 台账 4 行。
