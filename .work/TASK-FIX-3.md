# 任务书 FIX-3 · 转换器 GUI 界面错乱

> 作者实测：打开转换台后「UI 界面完全是乱的」。作者有真客户端，所以这条必须用**看得见的方式**验证，不能只靠读代码。

## 1. 已知事实与两个高置信度怀疑点

`src/main/java/com/dashpig/farmstorm/client/ConverterScreen.java` 现在只有 3 个成员：`IMAGE_WIDTH=176` / `IMAGE_HEIGHT=166` / `renderBg` / `renderLabels`。

### 怀疑点 A（最可能）：`renderBg` 用了 6 参 `blit`，UV 取样错
```java
graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
```
6 参重载内部按 **256×256** 贴图算 UV；我们的 `textures/gui/converter.png` 是 **176×166**。结果就是背景被按错的比例采样 → 看起来「糊成一团」。
修法：换成带纹理尺寸的重载，例如
```java
graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
```
（1.21.1 还有 `blit(ResourceLocation, int,int,int,int,int,int,int,int)` 与 `blitSprite` 等，以实际 API 为准，先 `javap` 或读源码确认签名再写。）

### 怀疑点 B：`ConverterMenu` 的槽位坐标与 GUI 贴图上画的槽位对不上
- 贴图是 `tools/gen_textures.py` 生成的，槽位是按 **input 80,35 / preview 116,35 / 背包 8,84 9×3 / 快捷栏 8,142 pitch 18** 画的（生成脚本里的注释）。
- `ConverterMenu` 的 `Slot` 坐标必须与这组数字**逐像素一致**；差一格就会「槽位悬空/压字」。
- 另外 `titleLabelY` 默认是 6，但 GUI 顶部若有画标题栏，文字可能压在槽位上。

## 2. 你的任务
1. 先读 `client/ConverterScreen.java`、`menu/ConverterMenu.java`、`tools/gen_textures.py` 里画 GUI 的那段，把「贴图上的槽位像素坐标」与「代码里的 Slot 坐标」列成一张对照表，逐项比对。
2. 修掉发现的所有不一致（UV 重载、槽位坐标、标题位置、预览槽是否该画、`inventoryLabelY` 等）。
3. **贴图本身不对也可以改贴图**：`tools/gen_textures.py` 的 GUI 生成段允许你重画（176×166，标准箱子式：顶部标题区 + 中间两个槽 + 下方 3×9 背包 + 快捷栏，边框与槽位要有明显对比），改完重跑脚本即可。
4. 只改 `client/ConverterScreen.java`、`menu/ConverterMenu.java`、`tools/gen_textures.py` 与由此生成的 PNG；**不要碰**其它 Java 包、registry、datagen 逻辑。

## 3. 验证（硬要求：要产出**看得见的证据**）

本机有显示器，`runClient` 能真跑（FIX-1 已验证过：可以自动连接本机服务端，`-PfsRunDir=run-fix3` 隔离目录）。请按下面顺序努力，**至少做到第 2 级**：

1. **几何验证（必做）**：用 Pillow 读 `converter.png`，把它的槽位矩形、标题区、边框位置**程序化**找出来，与 `ConverterMenu` 的槽位坐标逐项打印比对，输出「一致 / 偏差 N 像素」。
2. **截图验证（目标）**：加一个**临时**客户端调试钩子（交付前必须删干净）：进入世界后延迟若干 tick 自动 `Minecraft#setScreen(new ConverterScreen(...))`，再用 `net.minecraft.client.Screenshot.grab(...)` 把窗口截到文件（例如 `run-fix3/gui-shot.png`）。得到截图后用 read_image 自己看一眼，确认「背景、两个槽、背包 3×9、快捷栏、标题」都在位、没有错位/糊图。
   - 要开一个真实容器菜单才能构造 `ConverterScreen`；可以在世界里放一个 `farmstorm:converter` 方块，用临时钩子直接构造 `ConverterMenu(id, inventory, ContainerLevelAccess.NULL)` 或走 `player.openMenu`。以能编译跑通为准。
3. 如果截图路线确实走不通（例如拿不到 RenderTarget），**如实说明卡在哪**，并退回第 1 级 + 把「你改动的像素级对照表」作为交付证据。不要写「已验证」当你没看见。

## 4. 收尾硬要求
- 临时调试钩子、临时截图代码、临时 init script **必须全部删除**；交付后 `src` 全树 `grep -i "probe|debug|screenshot|setScreen"` 不得留下本次新增痕迹。
- `gw.cmd compileJava` / `build` 绿、零 warning；`python -X utf8 tools/verify_project.py`（用 bundled python）FAIL 0 / WARN 0。
- 若改了 `tools/gen_textures.py`，连跑两次必须幂等（md5 一致）。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；必须包含：槽位对照表、你判定「乱」的真正原因、验证等级（几何 / 截图）与相应证据、以及临时代码已清理的自查命令与输出。
