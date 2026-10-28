# 任务书 FIX-4 · 转换器真正能转换 + 下界之星槽位

> 作者实测：GUI 排版已正常，但**无法转换**，且**没有放催化剂（下界之星）的槽位**。

## 1. 已确诊的根因（你复核即可）

**`client/ConverterScreen.java` 里没有任何按钮**。全文只有一处提及：「转换按钮随 M4 的 tryConvert 一起接入」——但从来没加过。所以玩家在界面上**没有任何可点击的东西**，服务端的 `ConverterMenu#clickMenuButton` 永远收不到请求。这就是「无法转换」。

（服务端链路本身是通的：`ConverterMenu.clickMenuButton` 只认 `BUTTON_CONVERT=0` 并调 `ConverterBlockEntity#tryConvert`；21 条配方 JSON 也都在。）

## 2. 交付物

### 2.1 加一个真正可点的「转换」按钮（`client/ConverterScreen.java`）
- 在 `init()` 里 `addRenderableWidget(Button.builder(...))`，位置放两个槽位下方、`物品栏` 标签上方的空白区（面板 176×166、槽在 y=35、物品栏标签在 y≈72，自己量一下别压住）。
- 点击 → `this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ConverterMenu.BUTTON_CONVERT)`（走原版包，**不新增网络包**，HC-03/C-6）。
- 按钮文案：`gui.farmstorm.converter.convert`；建议同时把 `gui.farmstorm.converter.xp_cost`（「消耗经验：%s」）画在按钮旁或按钮 tooltip 里，数值读 `ContainerData[0]`。
- 不可转换时按钮置灰（`ContainerData[1]` 为 0）——注意 `AbstractContainerScreen` 每 tick 会更新 data，按钮 active 状态要跟着刷新（重写 `containerTick()` 或 `render()` 里更新）。

### 2.2 加「催化剂槽」（作者明确要求，**这是对 DESIGN §3.1.2「单槽位」的有意偏离**）
- `ConverterBlockEntity`：`SLOT_COUNT` 1 → 2，新增 `SLOT_CATALYST = 1`；第二个槽位同样要存档 / `loadAdditional` / 破坏时掉落（`onRemove` 已走 `Containers.dropContents`，确认覆盖）。
- `tryConvert` 的催化剂来源改为：**优先扣槽位里的**，不够再按现有逻辑扫玩家背包（保留旧行为作兜底，避免破坏既有配方数据）。
- `ConverterMenu`：加一个 `Slot`（`mayPlace` 只接受 `minecraft:nether_star`），坐标别和输入/预览槽重叠；`quickMoveStack` 相应扩展到三个槽。
- 槽位坐标必须与 GUI 贴图上画的格子**逐像素对齐**（上一个 bug 就是错位）。

### 2.3 GUI 贴图补背景（`tools/gen_textures.py` 的 GUI 段）
- 176×166，补上：催化剂槽的格子、按钮的矩形底（带边框，能看出可点）。
- **只用定点替换改 GUI 那一段**，不要整文件重写（另一个代理同时在改这个文件的方块贴图段）。
- 改完连跑两次必须幂等；其余 86 张 PNG 的 md5 不得变化。

## 3. 验证（硬要求：必须证明「真的转换成功」）

1. `gw.cmd compileJava` / `build` 绿、零 warning。
2. **截图**：`runClient -PfsRunDir=run-fix4` 打开转换器界面截图，确认「按钮可见 + 催化剂槽可见 + 与贴图对齐」。
3. **功能实测（关键）**：用临时钩子把界面上的按钮真的按一次（直接调 `button.onPress()` 或 `handleInventoryButtonClick`），前置：槽里放 1 个小麦 + 玩家身上有 3 点经验。断言：
   - 服务端日志/探针出现 `tryConvert` 成功；
   - 输入槽清空；
   - 玩家背包多出 1 个 `farmstorm:puffed_wheat`；
   - 经验减少 3。
4. **下界之星路径**：槽里放 1 个下界之星 + 输入原版火把花，按一次按钮，断言产出一个 `farmstorm:awakened_torchflower` 且槽里的下界之星被扣 1。
5. 临时钩子交付前**删净**，并自查 `build/classes` 无 `*Probe*.class`、src 全树无本次新增的 probe/debug 痕迹。

## 4. 边界
只改：`client/ConverterScreen.java`、`menu/ConverterMenu.java`、`blockentity/ConverterBlockEntity.java`、`tools/gen_textures.py` 的 GUI 段、`textures/gui/converter.png`。不要碰其它 Java 包、registry、datagen、`textures/block/**`。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；必须含：按钮与槽位的像素坐标、两张截图、3 与 4 两条功能断言的实际日志、以及「单槽位 → 双槽位」这个设计偏离的说明。
