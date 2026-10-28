# 07 · 发布到 GitHub（作者自己推）

> 本机**没有安装 git**，也没有任何 GitHub 凭据，因此仓库已整理成「可直接 push」的状态，由作者在自己的环境执行。

## 1. 仓库设置

| 项 | 值 |
|---|---|
| 名称 | `farmstorm` |
| 可见性 | **私有** |
| 初始化 | **不要**勾选 README / .gitignore / LICENSE，保持空仓库，否则首次 push 会冲突 |

## 2. 前置：安装 Git for Windows

本机 `git` 不在 PATH，也没有 `.git` 目录。到你自己的机器（或先在本机装好）执行：

```powershell
winget install --id Git.Git -e
# 装完重开一个终端，git --version 应能输出
```

## 3. 推送

在仓库根目录（本机为 `D:\project_farm`）执行：

```bash
git init -b main
git add .
git status --short          # 应约 485 个文件；确认没有 build/ run/ .tools/ dist/ 与两个 .zip
git commit -m "farmstorm v0.0.1: 59 任务 / 10 Phase 全量实现"

git remote add origin https://github.com/<你的用户名>/farmstorm.git
git push -u origin main
```

首次 push 会要凭据。推荐用 **Personal Access Token**（fine-grained，只给该仓库 Contents: Read and write）当密码，或装 Git Credential Manager 走浏览器授权。

## 4. 把 jar 挂成 Release 附件

jar **不作为源码树的一部分**（避免二进制进入版本历史），而是挂在 Release 上：

1. GitHub → 该仓库 → Releases → Draft a new release
2. Tag：`v0.0.1`，标题 `农场风云 v0.0.1`
3. 附件：拖入 `dist/farmstorm-0.0.1+mc1.21.1.jar`（336 KB）
4. 正文可直接用 `dist/安装说明.txt` 的内容（玩家安装说明 + 已知限制）

## 5. 会被提交 / 被排除的内容

| 内容 | 是否提交 | 说明 |
|---|---|---|
| `src/`（347 个文件：Java + 资源 + 生成物） | ✅ | 主体 |
| `docs/zh`、`docs/en`（14 份） | ✅ | 设计文档，**不要改动**（HC-06） |
| `plan.txt` | ✅ | 作者原始设想，只读 |
| `tools/`（材质生成脚本 + 两个校验脚本） | ✅ | 可复现材质与验收 |
| `.work/`（110 个：裁决记录 R-01~R-55、59 份任务书、验收截图、探针日志） | ✅ | **决策留痕**，后续维护的依据 |
| `README.md` / `build.gradle` / `settings.gradle` / `gradle.properties` / `*.cmd` | ✅ | 构建入口 |
| `.tools/` | ❌ | 本机工具链（JDK / Gradle 8.10 / NeoForge 镜像代理 / java.base 补丁），几百 MB |
| `build/`、`run/`、`run-*/`、`.gradle/`、`dist/` | ❌ | 构建与运行产物 |
| `src/generated/resources/.cache/` | ❌ | DataGen 哈希缓存 |
| `dsh-session-*.zip`、`错误报告-*.zip` | ❌ | 会话转录与崩溃报告，非项目内容且含本机信息 |

编译所需的**生成物**（`src/generated/resources` 的 169 个 JSON）是提交的；`.cache` 子目录不提交。

## 6. 构建环境（本机特有，换机器请跳过）

本机为了跑通构建做了三处**只存在于 `.tools/`** 的适配，未污染源码与构建脚本：

1. **NeoForge Maven 镜像代理**：`maven.neoforged.net` 在本机按 SNI 被阻断，`.tools/neoforge-proxy.mjs` 起本地只读代理绕开；`.tools/init.gradle` 负责重写仓库 URL。
2. **java.base 补丁**：本机 JDK 的 ACL 判定恒返回 false → `Files.isWritable` 恒 false → JDK zipfs 变只读 → NeoForm 的 recompile 步骤必崩。补丁 class 经 `JDK_JAVA_OPTIONS=--patch-module java.base=...` 注入。
3. **`GRADLE_USER_HOME` 必须在工作区内**，否则 native-platform 加载失败。

构建入口固定为 `.tools/gw.cmd`（一键脚本 `一键构建.cmd` / `启动客户端.cmd` / `启动服务器.cmd` 都调它）。**换一台正常机器时**：删掉 `gw.cmd` 里 `JDK_PATCH_DIR` 那两行、把 `--init-script` 指向去掉，即可直接用 `gradlew`。

## 7. 许可证

`.gitignore` 与文档均已就绪，但**许可证仍未定**（README 里写着「未定 / TBD by the author」）。私有仓库不受影响；一旦转公开，没有 LICENSE 等于「保留所有权利」。要开源请补一份（MIT / Apache-2.0 / LGPL 等），并在 README 与 `neoforge.mods.toml` 的 `license` 字段同步。
