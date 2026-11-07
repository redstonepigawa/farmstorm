# farmstorm 1.20.1（移植工作区）

> 本工作区与 1.21.1 主线 `D:/project_farm` **并存、互不干扰**。
> 基线：1.21.1 的 **v0.2.4 已实现态**（151 个 Java 文件 / 注册面 92 / server config 175 / 语言键 321 × 2）。

## 目录

```
D:/project_farm1.20.1/
├── README.md                  ← 你在这里
├── HANDOVER-1.20.1.md         ← 移植交接文档（含探针实测的 12 条事实与 65 符号搬运表）
├── .gitignore
├── docs/zh/                   ← 六件套（编号从 00，已写完：DECISIONS / DESIGN / TECH / TASKS / PROMPT-MAIN / PROMPT-TASKS）
└── probe/                     ← 可编译的 1.20.1 骨架（NG7+作物方块+方块实体+SimpleChannel）
```

## 作者已拍板（移植期）

| # | 决定 | 影响 |
|---|---|---|
| 1 | 协议串从 **`"1"`** 起 | 与 1.21.1 的 `"3"` 区分；混装直接拒绝，不崩 |
| 2 | **数据组件那条线不砍**（完整移植 A 路线） | 临时附魔与 `ai_suspend` 都要在 1.20.1 保住语义（**实际两处都走 NBT**：物品 `FarmstormTempEnchants` / 实体持久 NBT `FarmstormAiSuspend`；未用 Capability），P3 按上限 12 人日 |
| 3 | **存档不迁移** | 只支持新世界；1.21.1 世界打不开写进已知限制 |
| 4 | 文档仍按**六件套**，编号**从 00 起** | `00-DECISIONS` / `01-DESIGN` / `02-TECH` / `03-TASKS` / `04-PROMPT-MAIN` / `05-PROMPT-TASKS`；`D`/`F`/`Phase`/`HC`/`N`/`RT`/`A` 各系列也从 001 重起，不与 1.21.1 续接 |
| 5 | **模组版本号不改**（仍 `0.2.4`） | 两个平台靠产物名 `+mc1.20.1` / `+mc1.21.1` 区分；同实例只会加载其中一份，安装说明要写清「按 MC 版本各放一份」。`mods.toml` 的 MC 依赖范围分别是 `[1.20.1,1.21)` 与 `[1.21.1,1.22)` |

## 工具链（不复制，复用主线）

构建必须用主线的工具链，**不要**在本工作区另建 Gradle/JDK：

```powershell
$env:JAVA_HOME='D:/project_farm/.tools/jdk21'
$env:GRADLE_USER_HOME='D:/project_farm/.tools/gradle-home'
$env:TEMP='D:/project_farm/.tools/tmp'; $env:TMP=$env:TEMP
$env:JAVA_TOOL_OPTIONS='-Duser.language=en -Duser.country=US -XX:ActiveProcessorCount=2'
& 'D:/project_farm/.tools/gradle-8.10/bin/gradle.bat' --init-script 'D:/project_farm/.tools/init.gradle' -p <工作区目录> --no-watch-fs build
```

- 镜像代理：`node D:/project_farm/.tools/neoforge-proxy.mjs`（监听 `127.0.0.1:8899`，**构建期间必须常驻**）。
- JDK 17：`%APPDATA%/.minecraft/runtime/java-runtime-beta`（完整 17.0.15，含 javac），写进 `org.gradle.java.installations.paths`（**逗号分隔**）。
- java.base 补丁：`D:/project_farm/.tools/jdk-patch/classes`（本机 `Files.isWritable` 恒 false，必须 `--patch-module`）。这条要写进 `gradle.properties` 的 `org.gradle.jvmargs`，**不要**用 `JDK_JAVA_OPTIONS` 环境变量——后者会被 Gradle 的 toolchain 探测子进程继承，JDK 17 会被判成 Invalid。

## 进度与下一步（按 HANDOVER 的 P0~P6）

**已完成**

1. **P0** ~~把 `probe/` 跑通 build，并解决 `runServer` 卡在 `DownloadAssets`~~ → 结论见 `P0-REPORT-1.20.1.md`；dev 环境另有两个坑已写进 02 号 §2（服端配置在 `world/serverconfig/forge-server.toml`、`advertiseDedicatedServerToLan=false`）。
2. **六件套**：`docs/zh/00`~`05` 六份齐（D-001~D-014 / R1~R7 + B-01~B-12 / 25 条验收 + RT-001~RT-010 / F-001~F-021 + Phase 1~6 + A-001~A-006 / HC-001~HC-010 + N-001~N-008 + 10 条陷阱 / 6 个 Phase 共 32 条出口判据）。
3. **Phase 1~3（构建链 / 命名空间搬迁 / 五个子系统）**：`net.neoforged.` 命中 0；`build` 出 `build/libs/farmstorm-0.2.4+mc1.20.1.jar`（1,129,491 B），SRG 重映射出 `-srg.jar` 与 `dist/…jar`（均 1,138,762 B）。实测读数：**151** 个 Java 文件 / **16,080** 行 / 注册面 **91（11 类）** / server config **175** / 语言键 **321 × 2** / 配方 **25**（21 条 `converting` + 4 条合成）/ 进度 **16** / 战利品表 **28** / damage_type **3** / `pack_format` **15**。

**后续阶段**（判据见 `HANDOVER-1.20.1.md` §7~§8、03 号与 05 号）

4. **Phase 4**：F-015 存档签名（`RegistryFriendlyByteBuf` 已 0，`RecipeInput` 还剩 1 处注释命中）+ F-016 行为回归（25 条验收 + 31 条边界）。
5. **Phase 5**：F-017 文档回灌 / F-018 检查器 1.20.1 期望源（`verify_project.py` 目前仍是 92 口径）/ F-019 三份记录。
6. **Phase 6**：F-020 打包（`remap-1.20.1.cmd` → `dist/`）与 F-021 签字。

## 红线（沿用主线规矩）

- Mixin **0**。
- 不改 1.21.1 主线的任何东西（`D:/project_farm` 只读用它的工具链）。
- 服务端侧不出现 `net.minecraft.client.*`；CLIENT 配置独立成 `farmstorm-client.toml`。
- 客户端预测与服务端四道闸的分工不许混。
