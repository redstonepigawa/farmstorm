# 红色剪影「与生物不重叠」问题的记录（1.20.1 移植参考）

> 来源：1.21.1 主线 `D:/project_farm/.work/NOTE-wild-hunter-outline-V50.md`（2026-11-07 查证，基线 v0.2.4）。
> 用途：**移植到 1.20.1 时的参考**。原记录里的文档号与行号属于 1.21.1 主线（52~57 号那一套），本工作区编号从 00 起，引用时按 `00-DECISIONS`~`05-PROMPT-TASKS` 换算。
> 移植要点（三处会因为版本变化而需要重新核对）：① `RenderGuiEvent` / `RenderLevelStageEvent` 的包名在 1.20.1 是 `net.minecraftforge.client.event.*`；② `RenderGuiEvent` 的参数取法按 `02-TECH-1.20.1.md` §3 的「非纯改前缀」清单处理；③ 顶点格式过滤用的 `DefaultVertexFormat.NEW_ENTITY` 与 `RenderType.entityTranslucentEmissive` 在 1.20.1 同名，但 `LivingEntityRenderer` 的两处变换行号会漂移 —— **不要照抄行号，照抄「两处变换」这个事实**。

---

# 红色剪影「与生物不重叠」问题的记录（供 session-d15e665a 使用）

> 用途：跨会话交接。查证时间 2026-11-07，基线 v0.2.4（1.21.1 / NeoForge 21.1）。
> 一句话：这个问题在 v0.0.2（狂野苹果 → 狂野猎人阶段）被记为 **V-50 / V-52 / V-57**，原因与修法在文档和代码注释里都有。

---

## 1. 文档在哪

| 位置 | 内容 |
|---|---|
| `docs/zh/12-TECH-0.0.2.md:855-921` | 红色剪影的完整技术记录：首选方案 5 条、代码骨架、过滤表、**「三个必须照抄的细节（V-50）」**、「为什么让渲染器自己再画一遍（V-52）」、「为什么重画放在 GUI 阶段（V-57）」 |
| `docs/zh/12-TECH-0.0.2.md:909-917` | V-50 表格本体：哪个变换漏了会倒挂、哪个会悬空 1.5 格 |
| `docs/zh/13-TASKS-0.0.2.md:421-430` | 任务 **F-83** 的验收：「剪影与本体轮廓贴合、没有倒挂、没有悬空 1.5 格」 |
| `docs/zh/15-PROMPT-TASKS-0.0.2.md:295` | 阶段出口清单里同样的判据（含「带拴绳的生物」这一崩溃场景的回放） |
| `src/main/java/com/dashpig/farmstorm/client/WildHunterOutline.java:108-111` | 代码注释直接写了「相机旋转被应用两次」的原因 |
| `docs/zh/12-TECH-0.0.2.md:919` | 方法论陷阱：像素指标无法判「像不像」，必须人工看图 |

---

## 2. 原因：三种失效都表现成「红影和生物对不上」

### ① 相机旋转被应用两次（最直接）

`WildHunterOutline.java:108-111` 注释原话：

> `// modelview 已经是相机旋转（capturedModelView），所以 PoseStack 只做世界平移。`
> `// 之前这里还 mulPose(cameraRotation)，等于把旋转应用了两次 → 剪影画到错误位置。`

机制：`AFTER_ENTITIES` 阶段捕获的 `RenderSystem` modelview 矩阵**已经含相机旋转**；重画时再 `mulPose(cameraRotation)` 就转了两遍 ⇒ 剪影被画到错误方位。

**修法**：`RenderGuiEvent.Pre` 里用**新建的 `PoseStack`**，只做 `translate(entity.getX()-cameraPos.x, entity.getY()-cameraPos.y, entity.getZ()-cameraPos.z)`，不再乘任何相机旋转（`12-TECH:861` 第 5 点）。

### ② `RenderLivingEvent.Post` 拿到的 poseStack 已不是模型坐标系

`12-TECH:909-917` 的 V-50 表：

| 漏掉的变换 | 原版源码 | 后果 |
|---|---|---|
| `poseStack.scale(-1, -1, 1)` | `LivingEntityRenderer:101` | 红模型**上下左右倒挂** |
| `poseStack.translate(0, -1.501, 0)` | `LivingEntityRenderer:103` | 红模型**悬空 1.5 格** |
| 光照传 `LightTexture.FULL_BRIGHT` | —（BRIEF §7.2「任意亮度可见」） | 暗处剪影跟着变暗 |

根因：`Post` 事件在 `popPose()`（`:137`）**之后**（`:139`）才触发，事件给的 poseStack 已不是模型坐标系，手动重放就会漏这两步。

**修法（V-52）**：不手动重放变换链，改成给 `renderer.render(...)` 包一层按顶点格式过滤的 `MultiBufferSource` **再画一遍** —— 变换链由渲染器自己执行，几何必然重合；代价是 `render()` 内部会再触发 `RenderLivingEvent`，所以 `rendering` 重入守卫是**必需**的（`WildHunterOutline.java:42-43`）。

### ③ z-fighting

几何完全重合时会与本体闪烁、边缘被吃掉 ⇒ 重画时统一放大 **1.008 倍**（`WildHunterOutline.java:41` 的 `OUTLINE_SCALE`）。

---

## 3. 同一次收敛里的两个相关坑（不是重叠问题，但同一处代码）

- **颜色必须烘进贴图**：`textures/misc/wild_hunter_red.png` 是 1×1 RGBA (255, 38, 38, 115) = D-62 的 `(1, 0.15, 0.15)` + 0.45 透明度。`entityTranslucentEmissive` 的着色器**故意忽略 ColorModulator**，`RenderSystem.setShaderColor` 对它无效（`12-TECH:858`）。
- **不能无条件替换 `MultiBufferSource`**：拴绳 `RenderType.LEASH` 用 `POSITION_COLOR_LIGHTMAP`（无 UV0/UV1/Normal），塞进 entity 格式缓冲区会 `IllegalStateException: Missing elements in vertex` **直接崩客户端**（v0.0.2 实测崩过两次）。必须按 `type.format() == DefaultVertexFormat.NEW_ENTITY` 过滤，其余原样放行（`WildHunterOutline.java:53-63`、V-58）。

---

## 4. 为什么当年指标没抓到

`12-TECH:919`（V-50 方法论陷阱）：

> 「中心区红色像素最大值」这类指标在模型**倒挂且悬空**时同样成立 —— 它只能证明「**有没有红**」，不能判断「**像不像**」。剪影的验收必须配**人工看图**。

所以 F-83 的验收里那条「剪影与本体轮廓贴合、没有倒挂、没有悬空 1.5 格」是**人工看图项**；自动指标只用来兜「完全没画」。

---

## 5. 现状

`src/main/java/com/dashpig/farmstorm/client/WildHunterOutline.java`（128 行，v0.2.4 未变）当前形态：

- `RenderLevelStageEvent.Stage.AFTER_ENTITIES` 只**捕获**投影 / modelview / 相机 / partialTick，不画几何；
- `RenderGuiEvent.Pre` 里恢复这三样 + 新建 PoseStack 只做世界平移 + 逐只 `render(...)` + `endBatch()`（这样剪影画在灰度之后，不会被灰度链吃掉，V-57）；
- `OUTLINE_SCALE = 1.008` 抗 z-fighting；`rendering` 布尔守卫防递归；`shellSource(...)` 按顶点格式过滤；颜色烘在 1×1 红贴图里。

结论：**该问题已修复**，且「文档 V-50 表 + 代码注释」双处可查。若要升格成一条正式决策号，可按本仓库的编号规矩补一条 D（1.21.1 主线下一号是 D-255；1.20.1 移植工作区从 D-001 起）。
