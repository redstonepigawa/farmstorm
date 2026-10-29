# F-82 / F-83 / F-84 作者实测确认（2026-10-29 20:11–20:14）

## 结论：全部符合预期，作者评价「甚至超出预期」

测试方式：调度者启动 dev 服务端（`run-p13` / 25631）+ 客户端 quickplay 进世界，作者本人用 OP 身份在真客户端内实测，
随后自行退出（日志 `Stopping!`，exit 0，`crash-reports/` 零新增）。

## 逐项

| 项 | 结果 |
|---|---|
| **F-83 红色剪影** | 贴合本体、位置正确（`b025b8b` 去掉重复相机旋转后） |
| **F-82 灰度** | 世界变灰；HUD 保持原色（符合 V-56 的裁决） |
| **F-84 循环音** | 有声，左右移动不再卡顿（V-57 的 `relative`/坐标修复生效） |
| 稳定性 | 无崩溃；拴绳生物场景亦不崩（V-58 的顶点格式过滤） |

## 支撑日志（`run-p13/logs/latest.log`）

```
[20:03:58] Dev joined the game
[20:03:59] 狂野猎人 循环音 实例 Dev relative=true
[20:03:59] 狂野猎人 循环音 起 Dev
[20:11:53] Set own game mode to Creative Mode
[20:13:01] 灰度 PostChain 加载 farmstorm:shaders/post/grayscale.json
[20:14:39] 灰度 PostChain 卸载
[20:14:41] Stopping!
```

## 附带修掉的一处噪声

`WARN EffectInstance: Shader farmstorm:grayscale could not find uniform named InSize` ——
fsh 只用 `DiffuseSampler`，`InSize`/`OutSize` 被 GLSL 链接期优化掉，而 program JSON 仍声明它们。
已删这两个声明，只保留 `ProjMat`（`blit.vsh` 真正需要）。提交 `c4bf235`。
