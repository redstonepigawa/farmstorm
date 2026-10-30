# v0.0.3 文档完整性清单 / Integrity manifest

生成时间：本轮交付后。用途：实现侧读到「截断」时，用本表逐项核对——
**字节数、行数、sha256、首行、末行**五项只要对得上，文件就是完整的，读到一半是读取端的限制。

| 目录 | 文件 | 字节 | 行数 | sha256(前16) | 末行末 40 字 |
|---|---|---|---|---|---|
| zh | `16-DECISIONS-0.0.3.md` | 69027 | 619 | `3f9fae5af4d43193` | …ct.py` 的实际输出；除此之外的计数都在上表里逐条指向施工事实卡或既有决策。 |
| zh | `17-DESIGN-0.0.3.md` | 87895 | 1032 | `3cb89b4e1e453617` | … 抛异常的源码事实，见 `18-TECH-0.0.3.md` §1.4(b)）。 |
| zh | `18-TECH-0.0.3.md` | 107998 | 966 | `3d4c1934afbce2fc` | …→ #10；RT-22 → #15 / #16；RT-23 → #1 / #2。 |
| zh | `19-TASKS-0.0.3.md` | 96602 | 910 | `4084ec9494ba175d` | …在对应的任务验收里写死；四处均随上游订正关闭，任务一律按 §0.4 的裁决执行。 |
| zh | `20-PROMPT-MAIN-0.0.3.md` | 56728 | 380 | `88c7a98e7db95ae5` | ….md`，`17-DESIGN` 指 `17-DESIGN-0.0.3.md`。 |
| zh | `21-PROMPT-TASKS-0.0.3.md` | 93643 | 627 | `4083f6172fb5887c` | …本文件不自行改标题、不自行改编号、不自行加任务，也不改 60.0 人日这个合计。 |
| en | `16-DECISIONS-0.0.3.md` | 84134 | 622 | `7693394a016cbfb6` | …None. |
| en | `17-DESIGN-0.0.3.md` | 107034 | 1037 | `3ca1fcb26d0f41dc` | …code fragments are reproduced unchanged. |
| en | `18-TECH-0.0.3.md` | 127147 | 974 | `9f60696d1ba90c32` | …e, value and range stays byte-identical. |
| en | `19-TASKS-0.0.3.md` | 114020 | 921 | `4452cdbd86978450` | …is written as “upstream currently says”. |
| en | `20-PROMPT-MAIN-0.0.3.md` | 69518 | 387 | `d7302dbb8954eab9` | …code fragments are reproduced unchanged. |
| en | `21-PROMPT-TASKS-0.0.3.md` | 113191 | 639 | `7a50e527b351e180` | …, §0.5, §1, each phase subtotal and §9). |

## 首行（用于确认读到的是同一份文件）

- `zh/16-DECISIONS-0.0.3.md` → # 16 · v0.0.3 决策记录（Decision Record）
- `zh/17-DESIGN-0.0.3.md` → # 17 · v0.0.3 设计增量（Design Delta）
- `zh/18-TECH-0.0.3.md` → # 18 · v0.0.3 技术规格（Technical Specification）
- `zh/19-TASKS-0.0.3.md` → # 19 · v0.0.3 任务拆分与开发大纲（Task Breakdown & Roadmap）
- `zh/20-PROMPT-MAIN-0.0.3.md` → # 20 · v0.0.3 主提示词
- `zh/21-PROMPT-TASKS-0.0.3.md` → # 21 · v0.0.3 分阶段任务清单（Task Prompt）
- `en/16-DECISIONS-0.0.3.md` → # 16 · v0.0.3 Decision Record
- `en/17-DESIGN-0.0.3.md` → # 17 · v0.0.3 Design Delta
- `en/18-TECH-0.0.3.md` → # 18 · v0.0.3 Technical Specification
- `en/19-TASKS-0.0.3.md` → # 19 · v0.0.3 Task Breakdown & Roadmap
- `en/20-PROMPT-MAIN-0.0.3.md` → # 20 · v0.0.3 Main Prompt
- `en/21-PROMPT-TASKS-0.0.3.md` → # 21 · v0.0.3 Phased Task List (Task Prompt)

## 分片读取建议（单份 57 KB ~ 108 KB，若读取工具按体量截断，按此分片）

| 文件 | 建议分片（按行 offset/limit） |
|---|---|
| `zh/16-DECISIONS-0.0.3.md` | 619 行 → 1+400 / 401+219 |
| `zh/17-DESIGN-0.0.3.md` | 1032 行 → 1+400 / 401+400 / 801+232 |
| `zh/18-TECH-0.0.3.md` | 966 行 → 1+400 / 401+400 / 801+166 |
| `zh/19-TASKS-0.0.3.md` | 910 行 → 1+400 / 401+400 / 801+110 |
| `zh/20-PROMPT-MAIN-0.0.3.md` | 380 行 → 1+380 |
| `zh/21-PROMPT-TASKS-0.0.3.md` | 627 行 → 1+400 / 401+227 |
| `en/16-DECISIONS-0.0.3.md` | 622 行 → 1+400 / 401+222 |
| `en/17-DESIGN-0.0.3.md` | 1037 行 → 1+400 / 401+400 / 801+237 |
| `en/18-TECH-0.0.3.md` | 974 行 → 1+400 / 401+400 / 801+174 |
| `en/19-TASKS-0.0.3.md` | 921 行 → 1+400 / 401+400 / 801+121 |
| `en/20-PROMPT-MAIN-0.0.3.md` | 387 行 → 1+387 |
| `en/21-PROMPT-TASKS-0.0.3.md` | 639 行 → 1+400 / 401+239 |

## ⚠️ 读中文文档必须显式指定 UTF-8（否则会误判为「截断」）

12 份文档都是**无 BOM 的 UTF-8**（与仓库里既有的 00–15 一致）。
在中文 Windows 上，PowerShell 的 `Get-Content` 对无 BOM 文件默认按 **GBK / 代码页 936** 解码，后果是：

1. **乱码**：`主提示词` 会显示成 `涓绘彁绀鸿瘝`；
2. **行数变少**：UTF-8 的 3 字节序列被 GBK 成对吞掉，行数会少 2 ~ 4 成（20 号 380 行会数成 223 行），看起来像「文件被截断」。

**英文文档几乎全是 ASCII，任何 8 位解码都能读对**——这就是「英文正常、中文全缺」的全部原因，与文件本身无关。

正确的读法（任选）：

```powershell
# 方案 A：读的时候指定编码（不改文件，推荐）
Get-Content -LiteralPath $path -Encoding utf8

# 方案 B：用 .NET，永远按真实字节切行
[IO.File]::ReadAllLines($path)   # 自动正确识别 UTF-8
```

判据：`[IO.File]::ReadAllLines($path).Count` 必须等于本清单的「行数」列；若 `Get-Content` 数出来更少，就是编码没指定，不是文件缺内容。
