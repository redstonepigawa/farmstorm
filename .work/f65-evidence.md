# F-65 证据（v0.0.2 · 5 物品 / 2 方块 / CONVERTER_FUEL + V-03 / V-02）

HEAD: bb74fef ｜ 未 commit ｜ 改动文件 4 个（见 git diff --stat）

## 1. 五条 /give @s（探针 + FakePlayer 命令源，run B）
RCON 控制台没有实体，同一条命令在 RCON 里只会得到 "No player was found"（见 §6）；
因此用 FakePlayer 作为命令源的玩家上下文执行**逐字相同**的命令，返回原文：

    [13:37:44] [minecraft/MinecraftServer]: Gave 1 [item.farmstorm.molotov] to [Minecraft]
    [farmstorm/]: [探针] /give @s farmstorm:molotov 1 → 成功（返回值 1）
    [13:37:44] [minecraft/MinecraftServer]: Gave 1 [block.farmstorm.zangief_cane] to [Minecraft]
    [farmstorm/]: [探针] /give @s farmstorm:zangief_cane 1 → 成功（返回值 1）
    [13:37:44] [minecraft/MinecraftServer]: Gave 1 [item.farmstorm.wild_apple] to [Minecraft]
    [farmstorm/]: [探针] /give @s farmstorm:wild_apple 1 → 成功（返回值 1）
    [13:37:44] [minecraft/MinecraftServer]: Gave 1 [item.farmstorm.compressed_puffed_wheat] to [Minecraft]
    [farmstorm/]: [探针] /give @s farmstorm:compressed_puffed_wheat 1 → 成功（返回值 1）
    [13:37:44] [minecraft/MinecraftServer]: Gave 1 [block.farmstorm.compressed_hay_block] to [Minecraft]
    [farmstorm/]: [探针] /give @s farmstorm:compressed_hay_block 1 → 成功（返回值 1）

## 2. 两条 /setblock（RCON，无探针的最终构建 run C）

    > setblock ~ ~ ~ farmstorm:compressed_hay_block
      "Changed the block at 80, 71, -144\n"
    > setblock ~ ~1 ~ farmstorm:zangief_cane
      "Changed the block at 80, 72, -144\n"

（先清空同坐标才重复，否则同状态 /setblock 会返回 "Could not set the block"——原版行为，与注册无关。）

## 3. /data get block 原文

带 owner（探针以玩家身份部署于 83, 65, -144，重启后仍能读到 → NBT 往返成立）：

    "83, 65, -144 has the following block data: {detached: 0b, fuseTicks: 0, ownerUuid: [I; 1103637639, 2063286308, -1168698414, -912478601], pendingWaves: [], ownerName: \"[Minecraft]\", placedGameTime: 1815L, x: 83, y: 65, z: -144, id: \"farmstorm:fang_trap_cactus\"}"

/setblock 放的那一格（BE 已挂上，但 /setblock 不走 setPlacedBy，因此无 owner —— 预期）：

    "80, 72, -144 has the following block data: {detached: 0b, fuseTicks: 0, pendingWaves: [], placedGameTime: 0L, x: 80, y: 72, z: -144, id: \"farmstorm:fang_trap_cactus\"}"

    [farmstorm/]: [探针] 桑吉尔蔗 83, 65, -144 kind=ZANGIEF_CANE 归属=[Minecraft] → OK

BE 的 id 是 fang_trap_cactus（一个 type 收两个方块，V-03 生效）；kind 是 ZANGIEF_CANE（代码侧 deployedOf 分流生效）。

## 4. 幸运浆果池 16/16（V-02 收口）

16 行逐条 OK + 汇总行：

    [farmstorm/]: [探针] 幸运浆果池 16/16 → OK

自检仍在（run C，无探针构建）：

    [farmstorm/]: 自检 幸运浆果池 16 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ OK

探针删净：`Get-ChildItem -Recurse -Filter '*Probe*'` → src 0 / build\\classes 0 / jar 内 0。

## 5. 硬门禁

    > Task :build
    BUILD SUCCESSFUL in 8s
    5 actionable tasks: 2 executed, 1 from cache, 2 up-to-date
    exit=0 ；warning/WARN/deprecat 行数 = 0

产物：build/libs/farmstorm-0.0.2+mc1.21.1.jar（无 Probe 条目）。

## 6. verify_project.py（**与任务书预期不同**）

    == 1. config：文档 117 键 vs 生成的 toml ==
      文档口径键数: 117
      生成 toml 键数: 117
      [OK]   117 个配置键与文档逐字一致（117 键）
    == 2. 64 个注册 ID 逐字核对（全源码树） ==
      [OK]   items: 22 个 ID 齐全
      [OK]   blocks: 20 个 ID 齐全
      [OK]   block_entity_type: 6 个 ID 齐全
      [OK]   entity_type: 9 个 ID 齐全
      已核 registry 对象: 62 / 64
    == 3. 语言文件键集一致 ==
      [OK]   lang 键集一致，共 222 键
    == 4. 禁令扫描 ==
      [OK]   无 Mixin / TODO / 调试残留 / 空 catch
    结论：FAIL 0 项，WARN 0 项（exit 0）

原因（读脚本第 133–147 行）：第 2 段只做**子集检查**——文档里那 64 个 v0.0.1 ID 是否都能在源码树里找到字面量，
从不校验"多出来"的 ID、也不校验各 registry 的总数。所以本轮 +7 个对象（items 22→27、blocks 20→22）它看不见，
既不会红也不会绿到新对象上。真正的 0.0.2 口径升级仍是后续单独一件活；本轮没改脚本、没改文档。

## 7. 未验证

- **视觉未验证**：zangief_cane 的"世界无可见几何体"依赖 ModBlockStateProvider 的 DEPLOYED_INVISIBLE + 空模型，
  属 datagen（F-96/F-97），本轮只验服务端侧；compressed_hay_block 的贴图/模型同属 datagen。
- 创造栏 farmstorm:main 里 5 条新物品的**客户端可见性**未验证（内容由 creativeTabItems() 决定：4 作物走 CROP_ITEMS，
  compressed_hay_block 显式追加）。

## 8. RCON 控制台的 @s 限制（为什么 /give 只能用探针）

    > give @s farmstorm:molotov 1
      "No player was found\n"

RCON 源是控制台、没有实体，@s 无从解析；这不是物品缺失（同一条命令在玩家命令源下返回 "Gave 1 [item.farmstorm.molotov] to [Minecraft]"）。
