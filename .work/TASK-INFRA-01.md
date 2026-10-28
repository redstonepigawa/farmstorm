# 任务书 INFRA-01 · 修好本仓库的 Gradle 构建（唯一目标）

你只做一件事：让 `cmd /c ""D:\project_farm\.tools\gw.cmd" build"` 在**不修改任何 Java 源码**的前提下成功结束，产出 `build/libs/farmstorm-0.0.1+mc1.21.1.jar`。

## 边界
- 可改：`D:\project_farm\.tools\*`、`build.gradle`、`settings.gradle`、`gradle.properties`。
- 不可改：`src/main/java/**`（当前只有一个 Farmstorm.java，保持能编译即可）、`docs/**`、`plan.txt`。
- 工作目录 `D:\project_farm`。所有命令用 pwsh 工具执行。

## 已知环境事实（已核实，不用重新验证）
1. JDK 21 有两份：作者装的 `C:\Program Files\Java\jdk-21.0.12.1`，和仓库自带 `D:\project_farm\.tools\jdk21`（21.0.2）。`gw.cmd` 用后者。
2. Gradle 8.10 在 `D:\project_farm\.tools\gradle-8.10`；`GRADLE_USER_HOME=D:\project_farm\.tools\gradle-home`（必须留在工作区内，否则 native-platform 加载失败）。
3. 出向 TLS 被按 SNI 阻断 `maven.neoforged.net`，已由 `.tools\neoforge-proxy.mjs`（127.0.0.1:8899）绕开，`.tools\init.gradle` 负责把仓库 URL 重写到代理。`gw.cmd` 会自动拉起代理。这套已验证可用（NeoForge 制品、mojang-meta 都能下）。
4. 系统 Schannel 不可用（`SEC_E_NO_CREDENTIALS`），所以 curl/.NET 走 https 会失败；**Java 和 Node 的 TLS 栈正常**，不要据此判断「没网」。

## 当前卡点（已验证到这一步）
`./gradlew build` 走到 `:createMinecraftArtifacts`（ModDevGradle 2.0.148 + NeoFormRuntime 2.0.31）时失败：

```
 ERROR Line: 1, error while writing net.minecraft.world.ticks.package-info: null in /net/minecraft/world/ticks/package-info.java
Caused by: java.io.IOException: Compilation failed
    at net.neoforged.neoform.runtime.actions.RecompileSourcesActionWithJDK.run
```

产物 `build/tmp/neoformruntime/*_recompile/output.jar` 只有 22 字节（空 zip），说明**第一个 class 条目就写不进去**。

根因线索（已用最小复现证实）：
- Java 侧 `java.nio.file.Files.isWritable(<任意路径>)` 在这台机器上**恒为 false**，连 `C:\Users\DASHpig` 这种明确可写的位置也是 false（JDK 21.0.2 与 21.0.12.1 都一样）。
- JDK 的 zipfs 依据 `Files.isWritable` 决定 zip 是否只读，于是 `FileSystems.newFileSystem(...)` 得到只读文件系统，写入抛 `java.nio.file.ReadOnlyFileSystemException`（该异常**没有 message**，正好对应上面日志里的 `: null`）。
- 最小复现脚本已在 `D:\project_farm\.scratch\Z.java`（写入 zipfs）与 `W.java`（打印 isWritable），编译产物在 `.scratch\zout`，可直接跑。
- 普通文件写入（`Files.writeString`、.NET zip、PowerShell 写文件）**都正常**，只有 zipfs 路径坏。

已排除：JDK 版本（21.0.2/21.0.12.1 同样失败）、中文 locale（已强制 en_US）、磁盘空间（D 盘剩 159 GB）、并行度（`-XX:ActiveProcessorCount=2` 无效）、陈旧 daemon（已 `--no-daemon` + 清 build/）、沙箱权限级别（提权到 Medium integrity 后仍失败）。

## 有用的抓手
- NFRT 的 `--home-dir` 缓存里有各步骤中间产物；`recompile` 步骤从未成功过，所以每次都重跑。如果你能让 `Files.isWritable` 对某个目录返回 true，把 NFRT 的 work-dir / home-dir 指过去即可。
- `D:\project_farm` 的 ACL 里有一条 `Everyone:(CI)(DENY)(DC)` 和一条 `Mandatory Label\Low Mandatory Level:(OI)(CI)(NW)`，工作区各对象带 Low 完整性标签。会话自带技能 `diagnose-windows-sandbox-acl` 可用（脚本在 `C:\Users\DASHpig\AppData\Local\Temp\dsh-acl-skill-eFQzpG\scripts\diagnose-windows-sandbox-acl.ps1`），它需要 `sandbox_permissions: danger-full-access` 才能跑，会打印 RECAP；我跑过一次但没有任何输出、也没生成报告，你可以重新跑并把输出重定向到文件看看到底发生了什么。
- 你可以在 pwsh 调用上带 `sandbox_permissions: "danger-full-access"` + 中文 justification 提权（实测能把进程从 Low 提到 Medium integrity）。长期方案优先选不需要每次提权的。
- 备选路线（任选，能过就行）：换 MDG 版本；给 NFRT 换 work-dir；用 junction 把 `build/` 指到别的盘；用 JDK 21.0.12.1；用 gradle 的 `-D` 或环境变量影响 zipfs；甚至自己在 NFRT 缓存里补齐 `recompile` 步骤的中间产物（名字规律见 `.tools\gradle-home\caches\neoformruntime\intermediate_results\`）。

## 验收
1. `cmd /c ""D:\project_farm\.tools\gw.cmd" build"` → `BUILD SUCCESSFUL`，且 `build/libs/farmstorm-0.0.1+mc1.21.1.jar` 存在。
2. 连跑两次都成功（第二次应为缓存命中、明显更快）。
3. `cmd /c ""D:\project_farm\.tools\gw.cmd" runServer"` 能起到日志出现 `Done (` —— 如果这一步也被同类问题挡住，说明清楚并给出你验证过的替代证据。
4. 你的改动全部落在允许的文件里，且**不影响后续在沙箱内正常跑构建**（如果你引入了必须每次提权的步骤，明确写出来）。

## 回报
必须包含：根因结论（一句话）、你改了什么（逐文件）、上面 4 条验收的命令原文与关键输出行、以及**后续所有子代理该用的构建命令**（如果不是 `gw.cmd`，给出替代命令）。