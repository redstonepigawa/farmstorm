# FIX-3 证据（转换器 GUI 错乱）

## 1. 根因（唯一根因，已用字节码实证）
`GuiGraphics.blit(ResourceLocation, int, int, int, int, int, int)`（6 参）的字节码：
```
4: iconst_0    // blitOffset
5: iload 4     // uOffset
11: iload 6    // uWidth
15: sipush 256
18: sipush 256   // ← texWidth/texHeight 硬写 256
21: invokevirtual blit:(Lnet/minecraft/resources/ResourceLocation;IIIFFIIII)V
```
贴图是 176x166，被当成 256x256 采样：只采到源图左上 (121.0, 107.6)，再放大 1.455x / 1.542x 铺满 176x166。
后果（脚本 `.work/check-gui-geometry.py` 算出来的错位量）：

| 元素 | 槽位框被画在 | 真正物品格 | 错位 |
|---|---|---|---|
| 输入槽 | (115,52) | (80,35) | dx=+35 dy=+17 |
| 预览槽 | (167,52) | (116,35) | dx=+51 dy=+17 |
| 背包首格 | (10,128) | (8,84) | dx=+2 dy=+44 |
| 快捷栏首格 | (10,217) | (8,142) | dy=+75（掉到面板外） |

## 2. 怀疑点 B 不成立
`converter.png` 里程序化找到 38 个 16x16 槽位内容区，与 `ConverterMenu` 的 38 个 Slot 坐标**逐像素相等，最大偏差 0**：
input (80,35)、preview (116,35)、背包 (8,84)+18*(c,r)、快捷栏 (8,142)+18c。`ConverterMenu` 未改。

## 3. 验证等级
- 几何验证：`.work/check-gui-geometry.py` → FAIL 0（贴图/生成器常量/Menu 常量三方一致）。
- 截图验证（runClient 真跑，run-fix3，单人世界）：
  - `.work/fix3-shots/fix3-gui-shot.png` 修复后：面板/刻线/输入槽(麦)→箭头→预览槽(胡萝卜)/物品栏标签/3x9/快捷栏全部在位，物品图标正落在框内。
  - `.work/fix3-shots/fix3-gui-broken-shot.png` 修复前（同一屏复刻 6 参 blit）：背景放大、刻线穿到面板中间、槽位框整体右下偏移、快捷栏框掉出面板 —— 即作者说的「完全是乱的」。
  - `.work/fix3-shots/fix3-world-shot.png` 部署方块：空中石台上 man瓜（原版南瓜外观）可见、火把花（十字模型）可见、胡罗贝位置为空（隐形是设计）。

## 4. 改动清单
| 文件 | 改动 |
|---|---|
| `client/ConverterScreen.java` | 6 参 blit → 9 参（显式 176x166 UV）；补 `inventoryLabelY=72` 与「物品栏」标签；提示语整句超宽时不画（en_us 约 280px 会压出面板） |
| `tools/gen_textures.py` | GUI 段新增标题刻线 y=28、背包刻线 y=66、输入→预览箭头；其余 86 张输出 md5 不变，两次运行幂等 |
| `textures/gui/converter.png` | 重新生成（176x166，786 B，md5 E46C1A7C5A9CEA416D27F327CCEBD1D7） |
| `menu/ConverterMenu.java` | **未改**（坐标本来就对） |

## 5. 收尾
- `gw.cmd compileJava` / `build` 绿、零 warning；jar 323,108 B。
- `python -X utf8 tools/verify_project.py` → FAIL 0，WARN 0。
- `grep -i "probe|debug|screenshot|setScreen|Fix3"` over src/main/java → 仅剩既有 3 处（LANDING_PROBE 常量、debugHeartbeat 开关、LOGGER.debug）。
- 临时钩子两文件（Fix3ProbeClient / Fix3ProbeServer）与编译产物均已删除，`build/classes` 无 `*Probe*.class`。
