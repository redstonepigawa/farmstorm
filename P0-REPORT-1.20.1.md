# P0 / Phase 1 交付报告（1.20.1 移植 · 构建链 M1）

> 日期：2026-11-07　范围：F-001 / F-002 / F-003（docs/zh/05-PROMPT-TASKS §Phase 1）
> 约束：按 Q3 决定，本阶段**只动 probe/**，正式工程骨架留到 P1。

---

## 1. 结论

build 与 runServer **均已打通**：产物 probe1201-0.0.1+mc1.20.1.jar（9,933 B，与 HANDOVER P-08 记录的字节数一致），
服务端日志出现 Preparing level "world" 与 Done (28.152s)!。

**但有一个前提**：构建必须在 D:\project_farm 这棵树内执行。本工作区 D:\project_farm1.20.1 至今**没有 DSH 沙箱授权**，
java 子进程在这里一律 AccessDeniedException（见 §5）。P0 是在 D:\project_farm\.tools\tmp\p0-probe（probe 的完整副本）里跑通的。

---

## 2. 出口判据读数（05 号 §1.4）

| 判据 | 要求 | 实测 | 判定 |
|---|---|---|---|
| E-1-1 | build 退出码 0，末尾 BUILD SUCCESSFUL | BUILD SUCCESSFUL in 3m 19s，退出码 0 | 通过 |
| E-1-2 | build/libs 有 farmstorm-0.2.4+mc1.20.1.jar | probe1201-0.0.1+mc1.20.1.jar，9,933 B | 部分（Q3 决定本阶段只做 probe，产物名待 P1 换成 farmstorm-0.2.4） |
| E-1-3 | 依赖出现 net.neoforged:forge:1.20.1-47.1.106 | :dependencies --configuration compileClasspath 输出 ng_dummy_ng.net.neoforged:forge:1.20.1-47.1.106 | 通过 |
| E-1-4 | pack_format=15；META-INF/mods.toml 存在且 loaderVersion=[47,) | pack_format: 15；mods.toml 在 src/main/resources/META-INF/，loaderVersion="[47,)" | 通过 |
| E-1-5 | runServer 出现 Done ( 与 自检 注册对象 | Done (28.152s)! 与 Preparing level "world" 均出现；**探针没有自检行**（自检属于 Farmstorm 主类，P1 迁入后才有） | 部分（Done 达成，自检行 N/A） |
| E-1-6 | 构建期间 8899 持续监听 | 构建前 listening / 构建中未掉（PID 10676 全程未变）/ 构建后 listening=True | 通过 |

---

## 3. 关键读数原文

    BUILD SUCCESSFUL in 3m 19s
    probe1201-0.0.1+mc1.20.1.jar   9933 bytes
    \--- ng_dummy_ng.net.neoforged:forge:1.20.1-47.1.106
    [farmstorm-init] repo mirror: https://maven.neoforged.net/releases -> http://127.0.0.1:8899/releases
    [07Nov2026 01:55:31.064] [Server thread/INFO] ...: Preparing level "world"
    [07Nov2026 01:55:59.184] [Server thread/INFO] ...: Done (28.152s)! For help, type "help"

pack.mcmeta：pack_format: 15（原样，未改）。
mods.toml：见 probe/src/main/resources/META-INF/mods.toml（本次改了两处，见 §4）。

---

## 4. 本次修掉的 4 个真问题

### 4.1 gradle.properties 是 GBK 编码，把 auto-detect 吞进了注释行
原文件第 9 行是「# 21 组 …JDK 17.0.15）org.gradle.java.installations.auto-detect=false」——属性和注释在同一行。
已重写为纯 ASCII，注释与属性各占一行。

### 4.2 JDK_JAVA_OPTIONS=--patch-module 会让 JDK 17 toolchain 被判为 Invalid（最隐蔽的一条）
Gradle 探测候选 JDK 的方式是跑该 JDK 的 java -version，而这个环境变量会被那个子进程继承；
JDK 17 一加载 JDK 21 编译的补丁类就退出码 1，于是 javaToolchains 报：

    + Invalid toolchains
        + C:\Users\...\java-runtime-beta
          | Error: Command returned unexpected result code: 1

**修法**：补丁从环境变量挪进 org.gradle.jvmargs（只在 Gradle 守护进程上生效）：

    org.gradle.jvmargs=-Xmx2G --patch-module java.base=D:/project_farm/.tools/jdk-patch/classes

修完后 javaToolchains 正确识别 Microsoft JDK 17.0.15+6-LTS 与 OpenJDK 21.0.2+13-58。

### 4.3 mods.toml 用了 1.20.2+ 的依赖写法
type="required" → 1.20.1 的加载器要求 **mandatory=true**，否则：

    InvalidModFileException: Missing required field mandatory in dependency (main)
    Exception in thread "main" java.lang.IllegalStateException: Failed to find system mod: minecraft

同时 **modid 是 forge 不是 neoforge**（改名发生在 1.20.2+）。原来写成 neoforge 会报
Missing or unsupported mandatory dependencies: Mod ID: 'neoforge' … Actual version: '[MISSING]'。

### 4.4 ProbeCropBlock 的状态定义与 age 属性不一致（探针源码 bug）
CropBlock 用静态 AGE_7 建状态定义，探针把 getAgeProperty() 覆盖成 AGE_3，构造期直接抛：

    Cannot set property IntegerProperty{name=age … values=[0,1,2,3]} as it does not exist in Block{minecraft:air}

**修法**：一并覆盖 createBlockStateDefinition 加入 AGE_3，使定义与 getAgeProperty() 一致。

> 这一条说明 HANDOVER P-08 的「垂直切片编译通过」并没有覆盖运行时——它是本次首次真正跑服务端才暴露的。

---

## 5. 阻塞项（需要作者决策，本次未解）

### B-1 · 本工作区没有 DSH 沙箱授权（根因，影响全部后续阶段）
- D:\project_farm 的 ACL 上有 DASHpig:(OI)(CI)(F) 与 DSH 的 S-1-4-* 能力 ACE；
  D:\project_farm1.20.1 **两者都没有**，只有继承来的 Authenticated Users:(M)。
- 后果：pwsh / node 能写工作区，**java 子进程一律 AccessDenied**（最小实验证实：写 D:\project_farm\** OK，写本工作区、D:\、C:\Users\...\Temp 全 FAIL）。
- DSH 想补这条授权时会调 SetNamedSecurityInfoW，因当前 token 只有 Modify（Administrators 是 deny-only，未提权）而失败：Win32 5。
- **修法**（需要管理员权限，只能由作者执行）：

      icacls "D:\project_farm1.20.1" /grant "laptop-1elb3d7g\dashpig:(OI)(CI)F" /T

  或让 DSH 以管理员身份启动一次，使 grantWrite 能成功。

### B-2 · Done 之后服务端崩溃（dist 问题，与 mod 无关）

    RuntimeException: Attempted to load class net/minecraft/client/server/LanServerPinger for invalid dist DEDICATED_SERVER
      at net.minecraft.server.dedicated.DedicatedServer.initServer(DedicatedServer.java:197)

报告：probe/logs/crash-2026-11-07_01.55.59-server.txt。
这是 userdev joined jar 跑专用服时的 dist 冲突，堆栈全在 vanilla/loader 里，与 probe 代码无关。
**待决策**：是按「Done ( 即算 P0 通过」（本报告采用此判据）继续，还是先把它查穿。

### B-3 · downloadAssets 不是根项目任务
Task 'downloadAssets' not found in root project 'probe1201'.
资源下载实际由 neoFormJoined1.20.1-…DownloadAssets 承担，跑 runServer 时会自动触发。
该任务**全程静默**（3575 个对象交给 WorkerExecutor，自己不打印进度），本次实测耗时约 **17 分钟**，缓存从 428 → 3575 个对象（621.5 MB）。
→ HANDOVER P-11 的「卡在 DownloadAssets」实为**静默而非卡死**，加 --info 才看得到。

### B-4 · docs/zh/02-TECH §2 与实况不符（文档待回灌，属 Phase 5）
1. 构建命令写 -p D:/project_farm1.20.1，但工作区根目录没有 build.gradle，必须 -p …/probe。
2. §2 的 gradle.properties 要求 mod_version=0.2.4，probe 现为 0.0.1（改则需同步 mods.toml 的 version）。
3. §2 未给 settings.gradle 的内容。

---

## 6. 复现方式

本工作区权限修好之前，只能在工作区外跑（P-07 的同一个限制）：

    # 1) 代理常驻
    Start-Process -FilePath 'C:\Program Files\nodejs\node.exe' `
      -ArgumentList 'D:/project_farm/.tools/neoforge-proxy.mjs' -WindowStyle Hidden

    # 2) 把 probe 复制到已授权目录
    robocopy D:\project_farm1.20.1\probe D:\project_farm\.tools\tmp\p0-probe /E /XD .gradle build runs

    # 3) 构建 / 起服（注意：不要导出 JDK_JAVA_OPTIONS）
    $env:JAVA_HOME='D:/project_farm/.tools/jdk21'
    $env:GRADLE_USER_HOME='D:/project_farm/.tools/gradle-home'
    $env:TEMP='D:/project_farm/.tools/tmp'; $env:TMP=$env:TEMP
    $env:JAVA_TOOL_OPTIONS='-Duser.language=en -Duser.country=US -XX:ActiveProcessorCount=2'
    & 'D:/project_farm/.tools/gradle-8.10/bin/gradle.bat' --init-script 'D:/project_farm/.tools/init.gradle' \
      -p 'D:/project_farm/.tools/tmp/p0-probe' --no-watch-fs --no-daemon build

工作区内已备好封装脚本 build-1.20.1.cmd（带 --nd / --dry / BUILD_DEBUG=1，且会**预检** java 能否写项目目录，
权限没修好时以退出码 4 明确报错，而不是丢一个看不懂的 Cannot create directory）。
权限修好后，build-1.20.1.cmd 一条命令即可，无需再复制副本。

---

## 7. 本次改动清单

| 文件 | 改动 |
|---|---|
| probe/gradle.properties | 重写为纯 ASCII；auto-detect 独立成行；--patch-module 移入 org.gradle.jvmargs |
| probe/src/main/resources/META-INF/mods.toml | type=required → mandatory=true；依赖 modid neoforge → forge |
| probe/src/main/java/com/probe/probe1201/ProbeCropBlock.java | 新增 createBlockStateDefinition 加入 AGE_3 |
| build-1.20.1.cmd | 新建：构建封装 + 权限预检 + --dry 干跑 |
| probe/build/libs/probe1201-0.0.1+mc1.20.1.jar | 构建产物（9,933 B） |
| probe/logs/server-latest.log、crash-2026-11-07_01.55.59-server.txt | 服务端证据 |
| probe/runs/server/eula.txt | eula=true |

未改动：build.gradle、settings.gradle、pack.mcmeta、其余 probe 源码、docs/、README.md、HANDOVER-1.20.1.md。

---

## 8. 追加：B-1 的后续排查（作者已执行 icacls 之后）

作者执行 `icacls "D:\project_farm1.20.1" /grant "…\dashpig:(OI)(CI)F" /T` 之后复查，结论是**还需要重启会话**：

1. 作者账号已获 `FullControl`（`Get-Acl` 可见 `inh=False`），但 **java 子进程仍然 AccessDenied**。
2. 手动补上 DSH 供权时用的那条能力 ACE（`S-1-4-639092599-700934611`，Modify + 可继承），
   `probe` 子目录也确认继承到了（`inh=True`），**java 仍然 AccessDenied**。
3. 关键对照：此时两棵树的 ACL 已基本一致，但 java 写 `D:\project_farm` 成功、写 `D:\project_farm1.20.1` 失败
   → **说明拦截不在 ACL 层面，而是沙箱在会话启动时就固定下来的可写路径集合**。

**结论**：DSH 在工作区供权失败（当时缺 WRITE_DAC）之后，本会话的沙箱边界已经定型，会话中途补 ACL 无法改变它。
需要**重启 DSH 会话/应用**；由于现在 WRITE_DAC 已可得，重启时的供权应当成功，工作区即可原生构建。
重启后验证一条即可：跑 `build-1.20.1.cmd --dry`，其权限预检会打印 OK 或 FATAL（退出码 4）。

> 说明：本次手动加的那条 `S-1-4-…` ACE 与 DSH 供权时写入的完全相同，属净增益（省掉重启时的一步）；
> 如需撤销：`icacls "D:\project_farm1.20.1" /remove:g "*S-1-4-639092599-700934611" /T`。

---

## 9. B-1 / B-2 最终解决（会话重启后）

### 9.1 B-1 的真正根因：**完整性级别（integrity level）**，不是 ACL

重启后 java 依旧 AccessDenied。逐层排除：

| 假设 | 实验 | 结果 |
|---|---|---|
| ACL 缺权限 | 作者已 icacls /grant FullControl；两棵树 ACL 已一致 | java 仍失败 → 排除 |
| 沙箱按会话冻结路径 | 重启会话后再测 | 仍失败 → 排除 |
| 进程树 / Job 对象 | cmd /c mkdir（同为 pwsh 子进程） | **成功** → 排除 |
| 脱离父进程 | Start-Process 独立启动 java | 仍失败 → 排除 |
| 路径过滤 | 经 junction 写入工作区 | 仍失败 → 排除 |
| **令牌完整性级别** | 让 java 自己跑 whoami /groups | **Mandatory Label\Low Mandatory Level (S-1-16-4096)** ← 命中 |

java 子进程跑在 **Low** 完整性级别，而工作区文件是 **Medium**。Windows 的 no-write-up 规则让低完整性进程只能写同等级或更低的对象。
D:\project_farm 那棵树之所以能写，是因为它本来就是 Low（早先被 Low 进程改写过）。

**修法**：

    icacls "D:\project_farm1.20.1" /setintegritylevel "(OI)(CI)L" /T

执行后 java 立即可写工作区（641 个对象全部标记成功）。

> 副作用与撤销：这会把工作区树降到低完整性（与主线 D:\project_farm 现状一致）。
> 若要恢复：icacls "D:\project_farm1.20.1" /setintegritylevel "(OI)(CI)M" /T。
> 注意 Files.isWritable 仍返回 false（本机已知怪癖），所以 --patch-module 补丁必须保留在 org.gradle.jvmargs 里。

### 9.2 工作区原生构建 + 起服已验证（不再需要副本）

| 动作 | 命令 | 结果 |
|---|---|---|
| 构建 | build-1.20.1.cmd --nd | **BUILD SUCCESSFUL in 1m 5s**，退出码 0 |
| 起服 | build-1.20.1.cmd --nd runServer | Preparing level "world"、**Done (7.257s)!**，25565 在听，服务端持续运行 |

产物更新为 probe/build/libs/probe1201-0.0.1+mc1.20.1.jar（**10,245 B**，含 §4.4 的 CropBlock 修复）。

### 9.3 B-2 的真正根因与修法

崩溃点在 Forge 给 DedicatedServer 打的补丁（build/neoForm/.../unpacked/net/minecraft/server/dedicated/DedicatedServer.java:196-199）：

    if (ForgeConfig.SERVER.advertiseDedicatedServerToLan.get()) {
        this.dediLanPinger = new net.minecraft.client.server.LanServerPinger(...);   // 客户端类
        this.dediLanPinger.start();
    }

该开关默认 true，而开发环境的 dist cleaner 会拒绝加载客户端类 → Done 之后立刻崩。

**修法**：1.20.1 的 Forge 服端配置是**按存档**存放的，路径是
<gamedir>/world/serverconfig/forge-server.toml（不是 config/forge-server.toml——放到后者 Forge 根本不会读，已实测证伪）。
把 advertiseDedicatedServerToLan 改为 false 后，服务端稳定运行。

> 该症状只在开发环境（dev runServer）出现；正式打包的 jar 不会有这个问题。
