# F-99 · #6 声音事件验证 + 清理（补测，接前一份 f99-evidence.md）

## #6 两个声音事件能被资源管理器读到 —— 通过（带反面对照）

判据要求的动作：`/playsound` 两条各一次，服务端回 `Played sound …`，客户端日志无 `Unable to play unknown soundEvent`。

第一次用固定坐标 `100.5 7.0 96.5` 跑时服务端回 `The sound is too far away to be heard`（玩家不在那），
改用 `execute as Dev at @s run playsound …` 在玩家自身位置播放：

```text
> /execute as Dev at @s run playsound farmstorm:wild_hunter_loop master @s ~ ~ ~ 1 1   @21:04:31
    "Played sound farmstorm:wild_hunter_loop to Dev"
> /execute as Dev at @s run playsound farmstorm:zangief_piledriver master @s ~ ~ ~ 1 1   @21:04:31
    "Played sound farmstorm:zangief_piledriver to Dev"
> /execute as Dev at @s run playsound farmstorm:no_such_sound master @s ~ ~ ~ 1 1   @21:04:31
    "Played sound farmstorm:no_such_sound to Dev"
```

客户端日志：
```text
[21:04:31] [Render thread/INFO]  [ChatComponent]: [System] [CHAT] [Dev: Played sound farmstorm:wild_hunter_loop …]
[21:04:31] [Render thread/INFO]  [ChatComponent]: [System] [CHAT] [Dev: Played sound farmstorm:zangief_piledriver …]
[21:04:31] [Render thread/WARN]  [SoundEngine]: Unable to play unknown soundEvent: farmstorm:no_such_sound
```

**反面对照成立**：故意播一个不存在的 id 才会触发 `Unable to play unknown soundEvent`；
我们的两个事件**没有**任何此类告警 → 两个 sound event 都被客户端资源管理器正确读到。✅

## #17 数字口径（按调度者最终裁决）

**302 / JSON 195**，注明「含 F-98 三条进度 JSON（`first_molotov` / `zangief_hit` / `first_wild_hunter`）」。
先前「+3 里有 2 个是 PostChain 资源」的猜测**不成立**——那两个 JSON 本来就在 299 里（Lane C 测 299 时灰度已改完、GLSL 已是 3）。

## `post_effect/.gitkeep` 清理

- 已删除：`git status` 显示 ` D src/main/resources/assets/farmstorm/post_effect/.gitkeep`；
- 重新 `build` exit 0；jar 内 `post_effect` 现在只有 `grayscale.json`（不再有 `.gitkeep`）；
- **另外还剩两个 `.gitkeep`**（不是这次要求删的，仅如实报告）：
  `assets/farmstorm/shaders/core/.gitkeep`、`assets/farmstorm/textures/misc/.gitkeep` —— 这两个目录都已非空，同样可从 jar 里去掉；要不要删由调度者定（都还没删）。

## 收尾状态

- 服务端 `/stop`；客户端已退出（`Get-NetTCPConnection :25631` 无连接）；场上 0 个 java；
- `src` 侧 `*Probe*` = 1（**Lane B 的 `client/F9XProbe.java`**，非我文件，未动）；`build/classes` 与 jar 内同样只剩它；
  我自己的探针（ProbeV67 / ProbeF85 / ProbeF82）此前均已删净。
