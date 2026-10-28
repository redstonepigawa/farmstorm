# FIX-4 证据（转换器真正能转换 + 下界之星槽位）

## 1. 根因复核（成立）
`client/ConverterScreen.java` 全文没有任何按钮控件 —— 界面上没有可点的东西，
服务端 `ConverterMenu#clickMenuButton` 永远收不到 `ServerboundContainerButtonClickPacket`。
服务端链路本身是通的（`ConverterMenu#clickMenuButton` → `ConverterBlockEntity#tryConvert`，21 条配方 JSON 齐全）。

## 2. 改了什么
| 文件 | 改动 |
|---|---|
| `client/ConverterScreen.java` | `init()` 里 `addRenderableWidget(Button.builder(...))`：bounds=(`BUTTON_X=8`,`BUTTON_Y=42`,160,18)，文案 `gui.farmstorm.converter.convert`，tooltip 用 `gui.farmstorm.converter.tooltip`；`onPress` → `minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ConverterMenu.BUTTON_CONVERT)`（原版包，不新增网络包）。新增 `containerTick()` 每 tick 用 `menu.canConvert()`（ContainerData[1]）刷新 `active`。`renderLabels` 把 `gui.farmstorm.converter.xp_cost`（读 ContainerData[0]）右对齐画在标题同一行的右端。 |
| `menu/ConverterMenu.java` | 新增催化剂 `Slot`（容器槽 1，`mayPlace` 只收 `minecraft:nether_star`）；三个 GUI 槽改排 y=22 一行：催化剂(44,22) → 输入(80,22) → 预览(116,22)；槽位 id 变为 0/1/2，`PLAYER_INVENTORY_START=3`；`quickMoveStack` 扩到三槽（预览只读；背包尝试顺序 输入→催化剂）；新增 `xpCost()` / `canConvert()` 两个 GUI 数据访问器。 |
| `blockentity/ConverterBlockEntity.java` | `SLOT_COUNT` 1→2，新增 `SLOT_CATALYST=1` 与 `catalyst` 字段；Container 契约（getItem/removeItem/removeItemNoUpdate/setItem/clearContent/isEmpty/getContainerSize）按两槽重写；存档 `saveAdditional`/`loadAdditional`/`getUpdateTag` 增加 `Catalyst` 标签；`plan()` 步骤 4 的催化剂判定改为「槽位 + 背包（原逻辑）」合计；`tryConvert` 先扣槽位（`takeStoredCatalyst`）再按原逻辑扣背包/副手。 |
| `tools/gen_textures.py`（**只动 GUI 段**） | 常量 `GUI_CATALYST_SLOT=(44,22)`、`GUI_INPUT_SLOT=(80,22)`、`GUI_PREVIEW_SLOT=(116,22)`、`GUI_DIVIDER_Y=18`、`GUI_CATALYST_ARROW=(62,30,76)`、`GUI_ARROW=(99,30,113)`、`GUI_BUTTON=(8,42,167,59)`；`draw_gui_background()` 补催化剂槽格子 + 槽中菱形提示、催化→输入箭头、按钮矩形底（带边框）。方块贴图段未触碰。 |
| `textures/gui/converter.png` | 重新生成：176x166，884 B，md5 `433A70D0B1913C433A8FAB8326717D10`（原 `E46C1A7C5A9CEA416D27F327CCEBD1D7`）。 |

## 3. 像素坐标（按钮与槽位）
| 元素 | 面板内坐标 | 说明 |
|---|---|---|
| 催化剂槽 | 内容区 (44,22)..(59,37)，方框 43..60 / 21..38 | `ConverterMenu.CATALYST_X/Y` = `GUI_CATALYST_SLOT` |
| 输入槽 | (80,22) | `INPUT_X/INPUT_Y`（**由 35 上移到 22**，给按钮腾出整条空带） |
| 预览槽 | (116,22) | `PREVIEW_X/PREVIEW_Y` |
| 转换按钮 | x 8..167 / y 42..59（宽 160、高 18） | `ConverterScreen.BUTTON_*` ≡ `GUI_BUTTON` |
| 物品栏刻线 / 标签 / 背包 / 快捷栏 | y66 / y72 / y84 / y142 | 与 FIX-3 完全一致，未动 |

几何校验（`.work/check-gui-geometry-fix4.py`，Pillow 从 PNG 里反向找出槽位方框再和 Java/Python 常量三方比对）：

```
贴图 176x166；Screen IMAGE=176x166 BUTTON=(x=8,y=42,w=160,h=18)
贴图上找到 39 个槽位矩形，Menu 声明 39 个 Slot
  催化剂槽 贴图=找到 Menu=(44, 22) 命中 含下界之星菱形提示
  输入槽   贴图=找到 Menu=(80, 22) 命中
  预览槽   贴图=找到 Menu=(116, 22) 命中
[OK] 全部 39 个槽位坐标逐像素一致（最大偏差 0）
[OK] 贴图实际槽位与 tools/gen_textures.py 的常量逐像素一致
按钮矩形：Java=(8,42)-(167,59)  生成器=(8, 42, 167, 59)  → 逐像素重合
[OK] 按钮 (y=42..59) 在槽位带（内容区底边 38）之下、物品栏分隔线 y=66 与标签 y=72 之上；三个槽位不与按钮重叠
FAIL 0
```

## 4. 截图（runClient 真跑，`-PfsRunDir=run-fix4`，854x480）
| 文件 | 内容 |
|---|---|
| `.work/fix4-shots/fix4-gui-shot.png` | 界面打开：下界之星×2（催化剂槽，能看到菱形提示）→ 小麦（输入槽）→ 膨胀麦（预览槽），下方整条 `Convert` 按钮为可用态，标题行右侧 `Experience cost: 3`，物品栏 3×9 + 快捷栏全部对齐 |
| `.work/fix4-shots/fix4-after-convert-shot.png` | 按一次后：输入/预览槽清空、按钮**变灰**（ContainerData[1]=0）、催化剂槽的 2 颗星**未被误吃**、背包里多出 1 个膨胀麦、右上角弹出 `farmstorm` 进度提示（tryConvert 内的 `award(first_conversion)`） |
| `.work/fix4-shots/fix4-star-shot.png` | 场景二按之前：下界之星×2 + 原版火把花 → 预览槽显示改良火把花 |
| `.work/fix4-shots/fix4-after-star-shot.png` | 按一次后：催化剂槽 2→**1** 颗星、背包里出现改良火把花、按钮变灰、弹出 `The Star …`（`nether_star_toll`）进度提示 |

## 5. 功能断言实际日志（`.work/fix4-probe-log.txt`，全 21 行，摘自 `run-fix4/logs/latest.log`）
临时钩子做的事：单机世界 → 玩家脚下放转换器 → 往槽里塞物品 + 给 3 点经验 → `ServerPlayer#openMenu`
（与 `ConverterBlock#useWithoutItem` 同一条路径）→ 从 `screen.children()` 里找到那个 `Button` → **真的调 `button.onPress()`**
（= `handleInventoryButtonClick` → 原版按钮包 → 服务端 `clickMenuButton` → `tryConvert`）。

```
server-setup pos=-3, 79, -4 input=minecraft:wheat x1 catalyst=minecraft:nether_star x2 xp=3 puffedWheat=0 starsInInventory=0
converter screen ready, slots=39 convertible=true xpCost=3
press#1 button bounds=(133,79,160x18) active=true message=Convert
press#1 onPress() -> handleInventoryButtonClick(containerId=1, button=0)
assert#1 PASS | input=<empty> catalyst=minecraft:nether_star x2 xp=0 puffedWheat=1 starsInInventory=0 canConvert=0 xpCost=3
server-setup#2 pos=0, 79, 0 input=minecraft:torchflower x1 catalyst=minecraft:nether_star x2 xp=3 starsInInventory=0 awakenedTorchflower=0 canConvert=1
press#2 button bounds=(133,79,160x18) active=true message=Convert
press#2 onPress() -> handleInventoryButtonClick(containerId=3, button=0)
assert#2 PASS | input=<empty> catalyst=minecraft:nether_star x1 xp=0 awakenedTorchflower=1 starsInInventory=0
DONE
```

对照任务书 §3.3：输入槽清空 ✓｜背包多 1 个 `farmstorm:puffed_wheat` ✓｜经验 3→0 ✓｜（服务端 `tryConvert` 成功由三者同时成立 + 进度授予共同证明）。
对照任务书 §3.4：产出 `farmstorm:awakened_torchflower` ✓｜槽里的下界之星 2→1 ✓｜背包里星数为 0 ⇒ **只能来自槽位**，证明「优先扣槽位」✓。
按钮坐标 (133,79) = `leftPos(125)+8` / `topPos(37)+42`（GUI 缩放 2 → 逻辑分辨率 427×240），与贴图上的按钮矩形同一相对位置。

## 6. 贴图生成（幂等 + 只动 GUI 段）
```
python -X utf8 tools/gen_textures.py --vanilla-assets "D:\minecraft\.minecraft\versions\26.3-NeoForge_26.3.0.99\26.3-NeoForge_26.3.0.99.jar" --out <临时目录>
run1 vs run2 diff = 0（91 张逐张 md5 相等）
src 树 91 张 vs 全量新跑 91 张 diff = 0
改动前基线 91 张 vs 改动后 91 张 diff = 2 行 ⇒ 只有 gui/converter.png 变了（含 item/bamboo_firecracker.png 在内其余 90 张 md5 不变）
```
GUI 图单独由 `draw_gui_background()` 生成后写入 `src/main/resources`（不整目录重跑，避免与并行代理的方块贴图段互相覆盖）；
全量两遍是在临时目录里跑的，用于证明幂等与「其余 PNG 不变」。**必须带 26.3 客户端 jar**，用 1.21.1 jar 会让 bamboo_firecracker 的 md5 变。

## 7. 设计偏离说明：单槽位 → 双槽位
DESIGN §3.1.2 写的是「单槽位转换器」。任务书 FIX-4 §2.2 明确要求加催化剂槽，属于**作者主动要求的偏离**，落地口径：
- 槽 0 = 输入，槽 1 = 催化剂（**只收 `minecraft:nether_star`**），槽 2 = 只读预览；均使用原 `Container` 契约，未新增 MenuType / 网络包 / registry 对象。
- 催化剂的**费用判定**仍是服务端权威：`ConverterBlockEntity#requiredCatalyst` 不动（正向读 `converter.netherStarForwardCost`，反向由 `converter.netherStarOnReverse` 决定）；
  只把「从哪儿扣」改成**槽位优先、不够再扫背包/副手**（旧行为完整保留，既有配方与整合包数据不受影响）。
- 槽位只收下界之星是 GUI 层的语法糖：若某个数据包把催化剂声明成别的物品，槽里放不进去，但转换仍会按旧逻辑从背包扣 —— 不会因此拒绝转换。
- 破坏方块时不吞物品：`ConverterBlock#onRemove` → `Containers.dropContents(level, pos, container)`，
  该方法按 `getContainerSize()` / `getItem(i)` 遍历（`Containers.java` 源码确认），`SLOT_COUNT=2` 后第二槽自动被覆盖。

## 8. 收尾自查
- `gw.cmd build` → `BUILD SUCCESSFUL in 8s`；本轮 `compileJava` 零 warning 零 note；
  （整批重编译时会出现一条既有 note「ModAdvancementProvider.java uses or overrides a deprecated API」，本轮未触碰该文件，历史 `.scratch/rs.log` 里同样存在。）
- `python -X utf8 tools/verify_project.py` → `结论：FAIL 0 项，WARN 0 项`（117 配置键 / 62 注册对象 / 218 语言键 / 无 Mixin·TODO·调试残留·空 catch）。
- 临时钩子 `client/Fix4Probe.java` 已删除；`src` 全树 grep `FIX4|Fix4Probe|fix4world` = 0 命中；`build/classes` 无 `Fix4Probe*.class`。
  （`build/classes` 里当前还剩**另一个代理**的 `Fix5ProbeClient.class`，不是本次交付物。）
- 临时目录 `.work/tmp-fix4`、`.scratch/mcsrc` 已删除；`run-fix4/`（客户端运行目录）保留，不进交付物。
- 交付物 `build/libs/farmstorm-0.0.1+mc1.21.1.jar` 332,837 B。

## 9. 未决问题
- 无阻断项。两条口径提示：
- 经验成本画在**标题同一行的右端**（截图里是 `Experience cost: 3`），不是紧贴按钮：按钮占满 8..167 的整条，
  左侧再塞文案会挤到面板外（en_us 该句约 100px），右对齐标题行是本版式唯一放得下且不与标题相撞的位置。
- `ConverterScreen` 里 `gui.farmstorm.converter.tooltip`（「放入原版或改良作物进行 1:1 转换」）
  因为槽位上移到 y=22、标题行右端让给了经验成本，改为**按钮 tooltip**（悬停可见），不再常驻画在面板上；
  en_us 下该句约 280px 本来就画不下（FIX-3 已有「超宽不画」的处理）。

## 10. 待办提示
- `farmstorm:converter` 的 `ConverterScreen` 现在只有 `BUTTON_CONVERT=0` 一个按钮；将来若加「反向」等操作，务必仍走
  `AbstractContainerMenu#clickMenuButton`（HC-03 / C-6），不要新增自定义包。
