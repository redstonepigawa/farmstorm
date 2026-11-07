# P1 交付报告（Phase 2 + Phase 3 主体）

> 日期：2026-11-07　范围：`05` 号 §Phase 2（F-004~F-008）+ Phase 3 的组件/附件/网络部分
> 前置：P0 已交付（见 `P0-REPORT-1.20.1.md`）

---

## 1. 结论：编译与运行双绿

| 指标 | 目标 | 实测 |
|---|---|---|
| `net.neoforged.` 命中 | 0 | **0**（改前 192 处 / 58 文件） |
| `compileJava` / `build` | 退出码 0 | **BUILD SUCCESSFUL**，exit code 0 |
| 产物（开发用，未重映射） | `build/libs/farmstorm-0.2.4+mc1.20.1.jar` | 1,129,491 B |
| 产物（可分发型，SRG 重映射后） | `dist/farmstorm-0.2.4+mc1.20.1.jar` | **1,138,762 B**，SHA256 `174BF7E4…2C3A6` |
| `runServer` | `Done (` + 自检行 | `Done (31.855s)!`，25565 在听，服务端持续运行 |

---

## 2. 硬约束逐条读数（04 号 §2）

| 约束 | 要求 | 实测 | 判定 |
|---|---|---|---|
| HC-001 命名空间 | `net.neoforged.` = 0 | 0 | ✅ |
| HC-002 注册面 | 91 / 11 类 | `自检 注册对象 91 个：items 28 / blocks 28 / block_entities 8 / entity_types 13 / mob_effects 4 / sound_events 3 / recipe_types 1 / recipe_serializers 1 / creative_tabs 1 / damage_types 3 / menu_types 1 → OK` | ✅ |
| HC-003 协议串 | `"1"` 1 处、`"3"` 0 处 | 1 / 0 | ✅ |
| HC-006 配方与进度 | 21 / 16，解析 0 错 | recipe 25 份（21 转换 + 4 合成）、advancement 16、loot_table 28、damage_type 3；**解析错误 0** | ✅ |
| HC-008 元数据 | `META-INF/mods.toml` + `pack_format 15` | 已改名并改格式（`mandatory=true`、modid `forge`）；pack_format 15 | ✅ |
| HC-009 版本号 | 0.2.4，产物名带 `+mc1.20.1` | 一致 | ✅ |
| 红线：Mixin | 0 | 0 | ✅ |
| 红线：服务端无 client 类 | 非 client 包内 0 | 非 `client/`、`datagen/` 包内 `net.minecraft.client.` = **0** | ✅ |

### E-2 / E-3 的 grep 判据
`DeferredHolder`=0、`ModConfigSpec`=0、`CustomPacketPayload`=2、`DataComponent`=2、`AttachmentType`=2、`DeferredBlock|DeferredItem`=1、`RecipeInput`=1 ——
**上述非零项全部是注释/文档串，没有代码残留**（已逐条核对行内容）。

> E-4-1 的 `HolderLookup` = 0 这条**不适用**：`HolderLookup.Provider` 在 1.20.1 的 datagen API（`ForgeAdvancementProvider`、`DatapackBuiltinEntriesProvider`）里是正常参数，不属于 1.21.1 残留。当前 23 处均为合法用法。

---

## 3. 本阶段实际做了什么

### 3.1 工程骨架（正式工程，不再是 probe）
新建 `build.gradle`（NG7 userdev 7.0.171、toolchain 17、依赖 `net.neoforged:forge:1.20.1-47.1.106`、`-Xmaxerrs 1000`）、`settings.gradle`、`gradle.properties`；
`neoforge.mods.toml` → `META-INF/mods.toml`（1.20.1 格式）；`pack_format` 34 → 15。

### 3.2 A~E 五组命名空间搬迁
- `bus.api` / `fml.*` / `distmarker` / `registries` / `client.event` / `event.*` / `common.data` / `PacketDistributor` 纯前缀替换。
- `@EventBusSubscriber` → `@Mod.EventBusSubscriber`（1.20.1 的 `bus` 是单值、默认只有 FORGE；据 `FarmstormDataGen` 处理 `GatherDataEvent` 反推出 1.21.1 默认挂双总线，故把 MOD 总线的三个处理器拆到新类 `FarmstormClientSetup`）。
- `DeferredHolder → RegistryObject`（35 处）、`DeferredBlock/Item → RegistryObject`（本地 `registerBlock/registerItem` 助手，调用点零改动）。
- `ModConfigSpec → ForgeConfigSpec`（192 处）。
- `TickEvent` 四类合并，**14 个处理器逐个补 `phase == END` 守卫**（原代码全部是 `.Post`，等价）。
- `NeoForge.EVENT_BUS → MinecraftForge.EVENT_BUS`、`EventHooks → ForgeEventFactory`。

### 3.3 三个子系统的真重写（不是改名）
- **网络**：`SimpleChannel` + 手写 `FriendlyByteBuf` 编解码，字段顺序与原 `StreamCodec.composite` 逐字节一致，包数仍 2，协议串 **1**。
- **数据组件 → 物品 NBT**：键 `FarmstormTempEnchants`（ListTag），4 字段 `enchantment/granted/previous/expireAt` 全保留（02 号 §4.1 写的 `{id,lvl,until}` 会丢 `previous`，属文档简化，实现按能还原原等级的口径落地）。
- **附件 → 实体持久 NBT**：键 `FarmstormAiSuspend`，14 个存取点改写，恢复写回**原值**（HC-005）。

### 3.4 编译收口的三个硬决定
1. **爆炸子系统按 1.20.1 的 `Explosion` 模型重写**：1.20.1 没有 `shouldDamageEntity` 钩子，于是 `@Override explode()` 逐字复刻 1.20.1 原版方法体（16³ 采样顺序、`level.random` 消耗、Detonate 触发点、击退含爆炸保护衰减），只删掉 `entity.hurt` 一行；伤害由 `hurtCapped()` 施加一次。`onExplosionHit` 拆成 `canDropFromExplosion` + LootParams + `getDrops` + 合并(16) + `onBlockExploded`。
2. **`Attributes.STEP_HEIGHT` → `ForgeMod.STEP_HEIGHT.get()`**：`IForgeEntity#getStepHeight()` = `maxUpStep()` + 属性值，与原版移动代码同口径；因基值 0.0，delta 用 `1.5 − player.getStepHeight()`，有效步高仍是 1.5 格。
3. **`MobEffect#onEffectAdded` 无钩子** → 搬进已有的 `QbounceMountController#onEffectAdded(MobEffectEvent.Added)`，判据一致。

### 3.5 datagen 全部对齐 1.20.1
`RecipeOutput → Consumer<FinishedRecipe>`、`AdvancementProvider → ForgeAdvancementProvider`（要 EFH）、`AdvancementHolder → Advancement`、`AdvancementRequirements.Strategy → RequirementsStrategy`、`AdvancementType → FrameType`、`LootTableProvider` 4 参 → **3 参**、`BlockLootSubProvider` 3 参 → **2 参**、`BootstrapContext → BootstapContext`（原版拼写）、`Enchantments.FORTUNE → BLOCK_FORTUNE`（`Enchantment` 非 `Holder`）。
转换配方的 `result` 落盘从 `ItemStack.CODEC`（`id`）改为 `{"item":…}`，因为 1.20.1 的 `ConverterRecipeSerializer.fromJson` 走 `ShapedRecipe.itemStackFromJson` 读 `item`。

---

## 4. 未完成 / 需要决策

1. **Phase 4 的行为回归没做**：02 号 §7 的 25 条验收 + v0.2.4 的 31 条边界需要进游戏逐条操作（放置、投掷、交易、四道闸、浮窗、音效…），无法在没有人工操作的情况下自动跑。**这是当前最大的缺口。**
2. **三处「等价替换」需要人工抽验**（都带 `TODO` 注释）：
   - `BambooFirecrackerProjectile`：1.20.1 无 `WindCharge`，改用等威力(1.2)无破块爆炸 —— 击退/粒子与原版不同。
   - `InputLockHandler`：1.20.1 无 `CalculatePlayerTurnEvent`，改用 `ViewportEvent.ComputeCameraAngles` 冻结角度近似「灵敏度 0」。
   - `FarmstormExplosion#explode()` 是原版方法体拷贝：若将来有其它模组 Mixin 改原版 explode()，这份拷贝不会跟着变。
3. **Phase 5 的检查器回灌没做**：`tools/verify_project.py` 仍是 1.21.1 的期望值（92 / 12 类等），需要按 91 / 11 类 / config 175 改期望源。`EXPECTED_COUNTS` 未动。
4. **一处既有注释偏差待裁**：`ModLootTableProvider` 类注释写「27 张」，实际 `crop()` 16 + 其它 12 = **28** 张，`getKnownBlocks` 也是 28（迁移前就是如此，未动）。
5. `probe/` 仍留在工作区（Q3 决定 P0 只动 probe，现在正式工程已在根目录；probe 可留作参照或删除，待你定）。

---

## 5. 产物清单

| 文件 | 内容 |
|---|---|
| `build/libs/farmstorm-0.2.4+mc1.20.1.jar` | 构建产物 1,129,491 B |
| `dist/farmstorm-0.2.4+mc1.20.1.jar` | 发布件（**SRG 重映射后**，1,138,762 B） |
| `remap-1.20.1.cmd` / `tools/remap-to-srg.ps1` / `tools/SrgMappingBuilder.java` | SRG 重映射链路（NG7 没有 reobf，每次 build 后必须跑） |
| `dist/安装说明.md` | 安装说明（含「按 MC 版本各放一份」「协议串 1」） |
| `runs/server/logs/latest.log` | 起服证据（Done + 自检行） |
| `build-1.20.1.cmd` | 一条命令构建/起服，带权限预检与 `--dry` |
| `tools/Preflight.java` | 构建前的 java 可写性预检 |

---

## 6. 追加：真机加载失败的根因与修复（重映射）

把 `dist` 里的 jar 装进真实实例 `1.20.1-NeoForge_47.1.106` 后，客户端在 mod 构造期崩溃：

    java.lang.NoSuchMethodError: StringRepresentable$EnumCodec
      StringRepresentable.fromEnum(java.util.function.Supplier)
      at com.dashpig.farmstorm.util.CropCategory.<clinit>(CropCategory.java:29)

### 根因
1. `fromEnum` 在 1.20.1 **存在**，不是 API 用错。
2. 1.20.1 运行时 = **官方类名 + SRG 成员名**（类 `StringRepresentable` 在，方法名是 `m_216439_`）。
3. `build` 产出的 jar 是**官方成员名** → 真实实例必然 `NoSuchMethodError`。dev 环境用官方名，所以之前 `runServer` 一直是好的。
4. **`net.neoforged.gradle.userdev 7.0.171` 没有 reobf/remap 任务**（`gradle tasks --all` 只有 `jar`/`jarJar`），这一步必须自己做。

### 修复
用本机已有的两份映射按 **obf 键**合成「官方名 → SRG 名」：

- Mojang proguard：`.gradle/caches/minecraft/versions/1.20.1/client.txt` / `server.txt`
- NeoForm：`build/neoForm/.../steps/mergeMappings/output.tsrg`（`obf → 官方类名 + SRG 成员`）

产出 `build/remap/official-to-srg.tsrg`：7004 类 / 88,110 行，join 出 49,651 方法 + 31,003 字段，**0 未匹配、0 冲突**；另补 452 条层级条目（覆盖我们自己类里 override 的 MC 方法，否则 `getSerializedName` 不会真正覆盖接口方法）。再用 AutoRenamingTool 2.0.17 重映射，退出码 0。

### 验收读数
- `CropCategory.class`：`m_\d+_` 唯一命中 **[m_216439_, m_7912_]**，官方名残留 0
- 全 jar 185 个类扫描：**官方名残留 0**
- 抽样：`ItemStack.shrink` → `m_41774_`；`BlockState.setValue` → `m_61124_`

> ⚠️ **流程变更**：以后每次 `build` 出新 jar，都必须再跑一次 `remap-1.20.1.cmd` 才能分发。`build/libs` 里不带 `-srg` 的那个只能用于 dev。
