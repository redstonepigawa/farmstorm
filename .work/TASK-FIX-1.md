# 任务书 FIX-1 · 投掷物客户端渲染器缺失导致崩溃（最高优先级）

> 作者实测崩溃，报告在 `D:\project_farm\错误报告-2026-10-28_0.57.26.zip`（内含 `crash-2026-10-28_00.57.20-client.txt`、`latest.log`、`debug.log`）。

## 1. 根因（调度者已定位，你复核即可）

```
java.lang.NullPointerException: Cannot invoke "EntityRenderer.shouldRender(...)" because "entityrenderer" is null
  at net.minecraft.client.renderer.entity.EntityRenderDispatcher.shouldRender(EntityRenderDispatcher.java:136)
  at net.minecraft.client.renderer.LevelRenderer.renderLevel(LevelRenderer.java:1003)
```

`src/main/java/com/dashpig/farmstorm/client/FarmstormClient.java` 的 `onRegisterRenderers` **只注册了 `TURRET_BULLET` 一个**，8 个投掷物实体类型（`puffed_wheat` / `ubi_server` / `toxic_ubi_server` / `melon_slice_bomb` / `melon_grenade` / `bamboo_firecracker` / `red_mushroom_puff` / `brown_mushroom_puff`）一个渲染器都没注册 → 只要世界里存在任何一个被扔出的作物实体，客户端渲染就来 NPE 崩游戏。

## 2. 修复要求

1. 在 `FarmstormClient.onRegisterRenderers` 里给 **全部 9 个**实体类型注册渲染器（用 `FarmstormEntityTypes` 的字段，不要写死字符串）。
2. 8 个投掷物用原版 `net.minecraft.client.renderer.entity.ThrownItemRenderer<>`（和原版雪球/鸡蛋一致，直接用物品模型渲染）。
3. `TURRET_BULLET` 保持现状（已有 `TurretBulletRenderer`）。
4. **自检闭环**：在注册方法末尾用一次遍历断言「9 个 entity type 全部有渲染器」，或者用 `Farmstorm.LOGGER.info` 打印已注册的 9 个 ID（只在客户端日志出现），保证以后再加实体不会漏。
5. 只改 `client/` 包下的文件；**不要碰** registry/、entity/、explosion/、behavior/、datagen/、config/。

## 3. 验证（必须做，不能只说「已注册」）

1. `cmd /c ""D:\project_farm\.tools\gw.cmd" compileJava` 与 `build` 绿，零 warning。
2. `cmd /c ""D:\project_farm\.tools\gw.cmd" runServer -PfsRunDir=run-fix1` 起服 `Done (`，0 ERROR —— 确认没把客户端类带进服务端。
3. **真跑一次客户端**（这是本任务的关键）：`cmd /c ""D:\project_farm\.tools\gw.cmd" runClient -PfsRunDir=run-fix1`。本机有显示器，客户端能真起来。要求：
   - 让它在后台跑 3~5 分钟，确认日志出现主菜单/世界加载完成、且**没有**上面那条 NPE；
   - 如果启动需要交互（离线登录名等），把你能拿到的证据如实写出来，不要伪造；
   - 结束进程后把 `run-fix1/logs/latest.log` 里与 renderer 有关的行贴回。
4. 若无法在无人工干预下进入世界，就明确写「崩溃路径未复现验证」，并给出你已做到的替代证据（例如：9 个 ID 的注册日志 + 客户端主菜单正常加载）。**不许写「已验证不崩」**。

## 4. 回报
按 `05-PROMPT-MAIN.md §7` 模板；必须写清：崩溃根因复核结论、你注册的 9 行、runClient 的实际结果（成/败/未跑到）、以及剩余风险。
