# F-94 / F-95 / F-96 视觉证据（一趟客户端取证）

客户端：`run-p14c`（quickplay 连 127.0.0.1:25651）｜服务端：`run-p14`（25651 / rcon 25661，`FS_RUN_LOCK=run-p14`）
取证方式：游戏内 `Screenshot.grab`（读 framebuffer，与窗口焦点无关，V-34）＋ 临时探针 `F9XProbe`（**已删净**，见 §5）

---

## 1. F-94 ③ · 两条 `/playsound`

```
> playsound farmstorm:wild_hunter_loop master @p 100.5 7.0 96.5 1 1
    => Played sound farmstorm:wild_hunter_loop to Dev
> playsound farmstorm:zangief_piledriver master @p 100.5 7.0 96.5 1 1
    => Played sound farmstorm:zangief_piledriver to Dev
> playsound farmstorm:definitely_missing master @p 100.5 7.0 96.5 1 1     ← 反面对照
    => Played sound farmstorm:definitely_missing to Dev
```
服务端对三条都回「Played」（原版不校验事件是否存在），**判据看客户端日志**：
```
run-p14c/logs/latest.log:
[20:39:03] [Render thread/WARN] [SoundEngine/SOUNDS]: Unable to play unknown soundEvent: farmstorm:definitely_missing
```
- 反面对照**报错** ✔ ⇒ 检测路径是活的；
- 我们的两条**没有任何** `Unable to play unknown soundEvent` ✔ ⇒ 客户端认这两个事件、能解码；
- 同时**没有任何** `Missing subtitle translation` 行 ✔（顺带验证本轮语言补齐生效）。

**限制（照实写）**：「有没有真的听到」只能由人确认；我这边只能给「命令成功 + 客户端无解码/事件错误」。
首次尝试用 `~ ~ ~` 相对坐标，rcon 源点在原点导致 16 格外 → 回「The sound is too far away to be heard」，
已改显式坐标 `100.5 7.0 96.5` 重跑（上面三段即重跑结果）。

## 2. F-94 ④ · 效果图标在 HUD 上不是紫黑

- 截图：`.work/f9x-2-hud-effect.png`（`/effect give @p farmstorm:wild_hunter 120 0 false` 之后）
- 放大：`.work/f9x-2-hud-icon-zoom.png`（右上角 6 倍最近邻放大）
- **看图**：右上角方框内出现红/粉＋白灰图案的图标，**不是紫黑棋盘格**。
- 提醒：该帧整体偏灰是**预期**的——`wild_hunter` 在身时灰度 pass（D-61）整屏去色，同帧的红色剪影（D-62）同理。

## 3. F-95 ② ＋ F-96 ④ · 物品图标

- 截图：`.work/f9x-1-inventory.png`（创造模式 + `/give` 5 件新物品后打开背包/创造栏界面）
- 放大：`.work/f9x-1-inventory-zoom.png`（底部两行 4 倍最近邻放大）
- **看图**：底部快捷栏 5 格依次是 ①莫洛托夫（瓶＋布条，带绿「＋」）②桑吉尔蔗（绿色甘蔗形，带「＋」）
  ③狂野苹果（红苹果，带「＋」）④压缩膨胀麦（黄褐草捆，带「＋」）⑤压缩干草块（灰阶草捆）
  —— **五张全部有内容，没有一张是紫黑方块**；4 个新作物图标右下角都有区分原版/改良的绿色「＋」。
- 旁证：`/give` 的返回直接显示解析后的名字 `[Molotov]` / `[Zangief Cane]` / `[Wild Apple]` /
  `[Compressed Puffed Wheat]` / `[Compressed Hay Block]` —— 名字来自语言键（缺键会显示裸键）。

## 4. F-95 ④ · 无可见几何体 ＋ 破坏粒子用自己的 PNG

### 4.1 无几何体
- 截图：`.work/f9x-3-block-invisible.png`（`/setblock 100 6 100 farmstorm:zangief_cane` 后，玩家站 3 格外看它）
- **看图**：该位置只有一圈方块**选中框**，框内**没有任何几何体**；同时 `execute if block …` 为 `Test passed`（方块确实在）。

### 4.2 破坏粒子
- 截图：`.work/f9x-8-break-particles.png`；放大 `.work/f9x-8-particles-zoom.png`
- 做法：`/gamerule doTileDrops false` + `/setblock 100 6 100 minecraft:air destroy`（关掉落以排除掉落物实体干扰）
- **看图**：原方块位置炸出一簇**绿色碎片**，前几帧可见扩散/下落。
- 量化（连拍 12 帧统计目标区绿色像素）：
```
442 → 440 → 438 → 438 → 314 → 167 → 150 → 146 → 138 → 163 → 165 → 97
```
  逐帧衰减 ⇒ 瞬态粒子（不是实体）；颜色为绿色，与 `textures/block/zangief_cane.png` 一致；
  该方块模型 `models/block/zangief_cane.json` **只有 `textures.particle`**（无 elements）⇒
  这张 PNG 的唯一消费者就是破坏粒子，粒子是绿的 ⇒ 用的就是它。

## 5. 探针清理（V-40 红线，三处计数）
```
src 里 *Probe*        : 0
build/classes *Probe* : 0（F9XProbe*.class 已删）
jar 内 Probe 条目     : 0（farmstorm-0.0.2+mc1.21.1.jar，共 481 条目）
```
步骤：删 `src/.../client/F9XProbe.java` + 开关文件 `run-p14c/f9x-shot.token` + 陈旧 class → **增量 build**（没 clean，V-41）→ 上述计数。
收尾：服务端 rcon `stop`、客户端只杀自己那一个 PID（V-37）；`.tools` 两把锁都不存在；`run-p14` / `run-p14c` 目录保留。

## 6. 关于「两个客户端共存时地形不显示」

本轮**没能做这个对照**：按排期 Lane A 不起客户端，场上前前后后只有我这**一个**客户端。
我这台客户端全程**地形正常**（所有截图都能看到石台、地形边界与天空），没有出现「地形不显示」。
若作者那边能复现，说明不是「只要有第二个客户端就必然复现」；这一条我给不出结论，只留证：单客户端 + run-p14 服务端未复现。
