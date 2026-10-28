# 任务书 M5-0 · 数据包地基（damage_type + tags），解锁 M2-B / M3

> 先读 `.work/PLAN.md` §0/§4/§6/§7。这是一个**小任务**，目标只有一个：让 `farmstorm:crop_blast` 与 `farmstorm:void_bolt` 两个伤害类型、以及 5 个 tag 真实存在于数据包中。
> 背景：M2-A 实测发现 `FarmstormDamageTypes.cropBlast()/voidBolt()` 走 `getHolderOrThrow`，**两个 damage_type JSON 不存在时任何爆炸都会抛异常**。M2-B 与 M3 全部被它卡住。

## 1. 必读
1. `docs/zh/02-TECH-SPEC.md` §6.7（伤害类型 2 个、并入 minecraft 标签 2 个、方块标签 3 个、物品标签 2 个的确切内容）、§3.7（两个伤害类型的属性）。
2. `docs/zh/00-DECISIONS.md` D-39（自定义伤害类型 2 个）。
3. `docs/zh/05-PROMPT-MAIN.md` §4.7（资源一律 DataGen，禁止手写同名 JSON —— 见 `.work/PLAN.md` R-03）。

## 2. 交付物（只写 `datagen/` 下的文件 + 生成的 `src/generated/resources/`）
1. `datagen/ModDamageTypeProvider.java` — 生成 `data/farmstorm/damage_type/void_bolt.json` 与 `crop_blast.json`，字段逐字按 TECH §6.7：
   - `void_bolt`：`exhaustion 0.0`、`message_id "farmstorm.void_bolt"`、`scaling "when_caused_by_living_non_player"`、`effects "hurt"`
   - `crop_blast`：`exhaustion 0.1`、`message_id "farmstorm.crop_blast"`、同 scaling / effects
   同时生成 `data/minecraft/tags/damage_type/is_projectile.json`（含 `farmstorm:void_bolt`）与 `is_explosion.json`（含 `farmstorm:crop_blast`）。
2. `datagen/ModBlockTagsProvider.java` — 生成 `data/farmstorm/tags/block/` 三个：
   - `deployed_crops.json` = 5 个部署方块（`blast_carrot` / `pumpkin_turret` / `awakened_torchflower` / `awakened_pitcher_plant` / `fang_trap_cactus`）
   - `explosion_immune.json` = `awakened_torchflower` / `awakened_pitcher_plant`
   - `blast_proof.json` = 空列表起步（RT-09 给整合包作者的豁免口子）
   并生成 `data/minecraft/tags/block/mineable/pickaxe.json`（TECH §6.7：`converter` + 5 个部署方块）。
3. `datagen/ModItemTagsProvider.java` — 生成 `data/farmstorm/tags/item/modified_crops.json`（15 个作物物品）与 `seeds.json`（6 个播种物）。
4. `datagen/FarmstormDataGen.java` — 把上面 3 个 provider 接进 `GatherDataEvent`（**注意**：`@EventBusSubscriber` 不要加 `bus` 属性，见 `.work/PLAN.md` R-13）。

## 3. 约束
- tag 里的 5/2/6/15 这些条目一律用 `FarmstormTags` 常量 + `FarmstormBlocks`/`FarmstormItems` 的注册字段引用，**不要手写字符串 ID**（ID 必须来自注册对象，避免拼错）。但生成的 JSON 内容必须与 TECH §6.7 完全一致。
- 生成物必须落在 `src/generated/resources/`（`build.gradle` 已把该目录挂到 main 资源）。
- 不写 `src/main/resources/data/**`（会与 DataGen 抢所有权）。
- 不改 `registry/**` / `config/**` / `explosion/**` / `entity/**`。

## 4. 验收
1. `gw.cmd runData` → BUILD SUCCESSFUL，且 `src/generated/resources` 下出现上面 **9 个 JSON**（2 damage type + 2 minecraft damage_type tag + 3 farmstorm block tag + 1 minecraft block tag + 2 farmstorm item tag = 10 个文件，逐个列出）。
2. 逐文件贴出内容（每个不超过 12 行），确认字段名与 TECH §6.7 逐字一致。
3. `gw.cmd build` 绿；`gw.cmd runServer` 出现 `Done (` 且日志出现 `Loaded ... damage_type` 之类无缺失告警；再贴一条证据证明 `farmstorm:crop_blast` 现在能取到 holder（例如临时一行 `/damage` 或启动自检日志，跑完删掉）。
4. `python tools/verify_project.py` 仍 FAIL 0。

## 5. 回报
按 `05-PROMPT-MAIN.md §7` 模板；额外给出生成文件清单与每个文件的字节数。
