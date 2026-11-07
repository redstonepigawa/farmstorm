# 04 号 1.20.1 移植主提示词

> 投喂方式：本文件**只投一次**，在任何 Phase 之前；之后按 05 号逐 Phase 投喂。
> 阅读顺序：`00-DECISIONS` → `01-DESIGN` → `02-TECH` → `03-TASKS` → 本文。冲突时按 00 号（唯一真相源）裁决。
> 本文的地位：**常驻约束**——不回答「这一轮做什么」（那是 03 / 05 号的事），只回答「哪些做法一定错」。

---

## 0. 怎么用

开工前读一遍本文，收工时按 §5 逐条对号。本文与 05 号的分工是硬的：05 号说「这一轮做 F-009」，本文说「做 F-009 时不许把 NBT 键改名」。

**五条最贵的读法**

1. **这不是重构，是换接口**：玩法一行不改，接口整层换。凡「顺手优化」都属于越界。
2. **命名空间是 `net.minecraftforge.*`**，不是 `net.neoforged.*`。后者在 1.20.1 根本不存在（P-09）。
3. **构建链是 NG7**，不是 moddev（P-01）。坐标是 `net.neoforged:forge:1.20.1-47.1.106`（P-02）。
4. **JDK 17 已经在本机**（Mojang 自带的 `java-runtime-beta`，含 javac），不要去下载（P-05）。
5. **注册面从 92 变 91**，因为 `data_component_type` 在 1.20.1 不存在（D-009 / D-013）。

---

## 1. 事实卡（12 行，全部来自探针实测）

| # | 事实 | 证据 |
|---|---|---|
| 1 | moddev 2.0.148 不支持 1.20.1 | P-01 |
| 2 | 1.20.1 坐标 `net.neoforged:forge:1.20.1-47.1.106` | P-02（pom 200） |
| 3 | NG7 `7.0.171` + NeoForm 1.20.1 全链路通过 | P-03 |
| 4 | 现有镜像代理零改动可服务 1.20.1 | P-04 |
| 5 | JDK 17 = `%APPDATA%/.minecraft/runtime/java-runtime-beta`（17.0.15，含 javac） | P-05 |
| 6 | `installations.paths` 必须逗号分隔 | P-06 |
| 7 | 仓库外构建受沙箱限制（Gradle 子进程被拒写） | P-07 |
| 8 | 垂直切片（方块 + 方块实体 + SimpleChannel）能编译 | P-08（jar 9,933 B） |
| 9 | 1.20.1 用 `net.minecraftforge.*` | P-09（26 个编译错误实证） |
| 10 | 三处非纯前缀改名：`DeferredHolder→RegistryObject`、`sendToPlayer→send(PacketDistributor...)`、`EventBusSubscriber` 变内部类 | P-10 |
| 11 | `runServer` 卡在 `DownloadAssets` | P-11（待解决，F-003） |
| 12 | 元数据是 `META-INF/mods.toml` | P-12 |

---

## 2. 硬约束 HC-001 ~ HC-010

- **HC-001 · 命名空间清零**：`grep -c "net.neoforged." src/main/java` = **0**；不留兼容包、不留别名类。判定：一条 grep。
- **HC-002 · 注册面 91 / 11 类**：`Farmstorm#logSelfCheck` 与 `verify_project.py` 的 `EXPECTED_COUNTS` 同批改到 91；`data_component_type` 命中 0。判定：启动自检整行 + 检查器两行。
- **HC-003 · 协议串 "1"**：代码里 "1" 命中 1、"3" 命中 0；包数仍 2。判定：grep + 发送点计数。
- **HC-004 · 数据组件必须真落地**：物品 NBT 键 `FarmstormTempEnchants` 存在且到期可移除；不许「先删功能后补」。判定：`/data get` 读数。
- **HC-005 · 附件必须真落地**：实体持久 NBT `FarmstormAiSuspend`（`FarmstormAttachments` 工具类，14 个存取点；**不走 Capability**）；停手恢复写回**原值**。判定：`/data get entity` + 行为抽验。
- **HC-006 · 21 条配方与 16 条进度数量不变**：目录计数 + 0 条解析错。判定：目录计数 + 日志。
- **HC-007 · 玩法零改动**：25 条验收逐条有读数；任何数值偏离都算失败。判定：02 号 §7。
- **HC-008 · 元数据与包格式**：`META-INF/mods.toml`（不是 `neoforge.mods.toml`）、`pack_format 15`。判定：读文件。
- **HC-009 · 版本号不改**：`mod_version=0.2.4`，产物名 `farmstorm-0.2.4+mc1.20.1.jar`（D-004）。判定：gradle.properties + build/libs。
- **HC-010 · 存档不写迁移**：不许出现「读 1.21.1 世界」的兼容分支（D-014）。判定：grep 数据版本检测类调用。

---

## 3. 禁止行为 N-001 ~ N-008

- **N-001** 不许用 `moddev` 插件试 1.20.1（P-01 已证伪）。
- **N-002** 不许为了少改文件而造 `net.neoforged` 兼容包 / `DeferredHolder` 别名。
- **N-003** 不许把 `RegistryObject` 包成自定义 `DeferredHolder` 包装器（类型擦除后行为不一致）。
- **N-004** 不许改 1.21.1 主线 `D:/project_farm` 的任何文件（只读用工具链）。
- **N-005** 不许在客户端做归属判定或调 `canInsert`（B-09 / B-10）。
- **N-006** 不许新增网络包（保持 2 个）。
- **N-007** 不许改玩法数值（等级经验、产出数量、耐久、冷却）。
- **N-008** 不许在 `runServer` 卡住时用「本地文件手动塞 assets」这种不可复现手段糊过去——必须写清做法并复现。

---

## 4. 陷阱清单（10 条）

| # | 症状 | 原因 | 正确做法 |
|---|---|---|---|
| 1 | 依赖解析报找不到 `net.neoforged:neoforge:47.1.106` | 用了 moddev 或漏了 MC 前缀 | 用 NG7 + `net.neoforged:forge:1.20.1-47.1.106` |
| 2 | `Directory ...jdk21;...java-runtime-beta does not exist` | paths 用了分号 | 改逗号 |
| 3 | 构建立刻 `Connection refused` | 镜像代理没常驻 | 先起 `neoforge-proxy.mjs` 并确认 8899 在听 |
| 4 | 外部目录 `Cannot create directory` | 沙箱限制 | 构建会话以本工作区为工作目录 |
| 5 | 26 个「包不存在」错误 | 还在用 `net.neoforged.*` | 按 02 号 §3 逐符号搬 |
| 6 | `DeferredHolder` 找不到 | 1.20.1 只有 `RegistryObject` | 全量替换，不留别名 |
| 7 | `sendToPlayer` 找不到 | 1.20.1 用 `send(PacketDistributor...)` | 按 02 号 §4.3 改 |
| 8 | 浮窗/连线收不到 | 信道版本串或编解码不一致 | 两端打印版本串；先骨架（F-008）后行为（F-011） |
| 9 | 进度拿不到 | `Advancement.Builder` 的 requirements 语义不同 | 逐条对 v0.2.4 的 json 核 |
| 10 | 停手后怪物永久不动 | 恢复时没写回**原值** | 持久 NBT 里存原 `noAi`，恢复按原值写 |

---

## 5. 交付清单（8 项，逐条勾）

- [ ] 1 构建：`build` 退出码 0；产物 `farmstorm-0.2.4+mc1.20.1.jar`。
- [ ] 2 命名空间：`net.neoforged.` 命中 0；构建绿。
- [ ] 3 注册面：自检 `91 … → OK`；检查器 `91 / 91`。
- [ ] 4 网络：包数 2、协议串 "1"、客户端能看到连线与浮窗。
- [ ] 5 组件/附件：两个 NBT 键落地，语义与 v0.2.4 一致。
- [ ] 6 配方/进度：21 / 16，解析 0 错。
- [ ] 7 玩法：25 条验收 + 31 条边界逐条有读数。
- [ ] 8 文档与打包：六件套 + 三份记录 + `dist/` 与安装说明（含「按 MC 版本各放一份」）。

> 本文自检：事实卡 12 行、HC 10 条、N 8 条、陷阱 10 条、交付清单 8 项，全部可执行。
