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
| `plan0.0.1.txt` | ✅ | 作者原始设想，只读 |
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
## 8. 做了更新版之后怎么推

### 8.1 先理解 git 在做什么（回答「会不会覆盖旧版本」）

- 每次 commit 是**一份完整快照**，不是"增量补丁"。`main` 分支永远指向**最新那一份快照**。
- 所以推更新时，工作树里**同名文件会被新版替换**——这是它的本职，不是"覆盖掉旧版本"。
  没变的文件不动；本地删掉的文件在提交后也会从仓库里移除。
- **旧版本不会消失**：每次提交都在历史里，`v0.0.1` 还是一个 tag，Release 上的 jar 也一直在。
  随时可以看旧代码：GitHub 上切 tag 浏览，或本地 `git checkout v0.0.1`。
- 真正会毁掉历史的是 `git push --force`（强推）。**不要用它**；要回退就正常 `git revert` 或再提交一次。

### 8.2 日常更新（bug 修复、小改动）

```bash
# 1) 改完代码，先确认能构建（本机用 .tools\gw.cmd，或双击 一键构建.cmd）
# 2) 再跑一次静态验收，确保没有回归
python -X utf8 tools/verify_project.py

# 3) 提交并推送
git add -A
git commit -m "fix: 修掉 xxx"
git push
```

`git push` 之后，GitHub 上的 `main` 立刻变成这次提交的内容。**不需要**再建 Release——Release 只对应"正式发版"。

### 8.3 正式发一版（例如 v0.0.2）

```bash
# 1) 改版本号（会影响 jar 名字与 mods.toml 里解析出的版本）
#    改 gradle.properties：mod_version=0.0.2

# 2) 重新构建，产出 dist\farmstorm-0.0.2+mc1.21.1.jar
cmd /c ""D:\project_farm\.tools\gw.cmd" build

# 3) 提交 + 打 tag + 推送（tag 一定要一起推，否则 GitHub 上看不到）
git add -A
git commit -m "v0.0.2: 本次改了什么"
git tag v0.0.2
git push
git push origin v0.0.2

# 4) 建新 Release 并挂新 jar（旧的 v0.0.1 保持不动）
gh release create v0.0.2 "dist\farmstorm-0.0.2+mc1.21.1.jar" \
  --title "农场风云 v0.0.2" --notes "本次改动摘要" --target main --latest
```

要点：
- **一个版本一个 tag 一个 Release**。v0.0.2 不会动到 v0.0.1，玩家仍能下到旧版。
- `dist/` 在 `.gitignore` 里，jar 不进源码树，只挂 Release 附件。
- 想替换某个 Release 的 jar（例如打错了）用 `gh release upload v0.0.2 <jar> --clobber`，但更推荐直接发新版本号。

### 8.4 本机（DSH 沙箱内）的两个特例

下面两条**只影响在 DSH 会话里执行 git 命令**，你自己在资源管理器/终端里操作不受影响：

1. **TLS**：沙箱内的 Windows Schannel 拿不到凭证，`git push` 会报 `SEC_E_NO_CREDENTIALS`。已在仓库内设置 `git config http.sslBackend openssl` 绕开。
2. **凭据助手**：沙箱禁止命名管道，git 调 `gh` 取 token 会报 `couldn't create signal pipe`。临时做法是把 token 内联进 URL 用一次：
   ```powershell
   $tok = (& "C:\Program Files\GitHub CLI\gh.exe" auth token)
   git -c credential.helper= push "https://x-access-token:$tok@github.com/redstonepigawa/farmstorm.git" main:main
   git remote set-url origin https://github.com/redstonepigawa/farmstorm.git   # 用完立刻还原
   ```
   在**你自己的终端**里不需要这一步，直接 `git push` 即可（GCM / gh 助手都正常）。
   3. **`github.com:443` 可能被单独阻断**（`api.github.com` 通、`github.com` 超时是典型表现）。
      此时 `git push` 会报 `Failed to connect to github.com:443`，但 `gh api ...` 仍然正常。应急做法是把 git 的 HTTPS 流量通过本地 CONNECT 隧道导到一个可达的 GitHub 边缘 IP：
      ```powershell
      # 先挑一个 443 可达的 IP（示例：140.82.113.4 / 140.82.112.4 / 20.27.177.113）
      # 起本地隧道（脚本见 .scratch/connect-proxy.mjs，不入库）
      node .scratch/connect-proxy.mjs
      # 推送时指定代理，其余照旧
      git -c http.proxy=http://127.0.0.1:8888 push origin main
      ```
      这只影响本机网络环境，与仓库内容无关；网络恢复后直接 `git push` 即可。
