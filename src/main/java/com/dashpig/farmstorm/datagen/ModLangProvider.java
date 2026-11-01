package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 语言文件（TECH §7）：zh_cn 与 en_us 各 **299** 键（v0.0.3 口径）。
 *
 * <p>键集完全一致由构造方式保证：两种语言都从同一张 {@link #ENTRIES} 表按列写盘，
 * 不存在「两份键各自维护」的可能。**条目构成（v0.1.0 实测，改表时必须同步本段）**：
 * 32 item + 26 block + 4 effect + 3 subtitles + 166 config + 36 advancement + 6 death
 * + 16 tooltip + 13 entity + 8 gui + 1 container + 1 itemGroup + 1 command + 2 message = **315**。
 *
 * <p>沿革：v0.0.1 是 222 键（22/20/9/1/1/8/16/4/24/117），v0.0.2 收到 263，
 * v0.0.3 收到 299（其中 16 条 `farmstorm.configuration.*` 由 F-132 的 runData 落地、
 * 另 18 条由 F-134 补齐：item 4 / block 3 / effect 2 / subtitles 1 / advancements 6 / death 2；
 * 末 2 条 entity 由 F-135 补漏：friend_chives / vibration_bean）。
 *
 * <p>tooltip 计 16（FIX-7）：§2.3 的六个类别各一个键（{@code tooltip.farmstorm.category.*}）
 * 取代原先 {@code DeployedKind} 的三个 {@code tooltip.farmstorm.type.*} 单值键——类别是多值集合，
 * 一个作物要能同时显示「炮台类、buff类」；再加一个 {@code tooltip.farmstorm.category_separator}
 * 让中英各自用自己的分隔符（「、」/ 「, 」）。这条沿革保留在 v0.0.1 的口径里（218 - 3 + 6 + 1 = 222）。
 *
 * <p>v0.1.0 的 F-139 再加 **11** 条 {@code farmstorm.configuration.*}（12 行 config 里第 10 行只改既有键的默认值），
 * 两份语言文件到 310 键、config 到 166；F-160 再加 **1** 条指令反馈 {@code command.farmstorm.farmlandlimit.set}
 * （TECH §7.3），到 **311** 键；F-157 再加 **2** 条动作栏提示（{@code message.farmstorm.farmland.no_core}
 * / {@code message.farmstorm.farmland.quota_full}，24 号 §6.3 的第 2、3 步），到 **313** 键；
 * F-169 再加 **2** 条（{@code block.farmstorm.outpost_core} / {@code item.farmstorm.outpost_core}），
 * 到 **315** 键（299 + 16，TECH §9.4 的增量列，中英键集逐字相同）。
 *
 * <p>中文名逐字照抄 plan0.0.1.txt（含全角标点）；英文名按 DEC C-10：注册 ID 的 Title Case。
 * 155 个 config 键的路径与 {@code run/config/farmstorm-server.toml} 的段/键逐段对应，
 * 该 TOML 由 {@code FarmstormConfig} 的真实 spec 写出，可做机械比对。
 */
public final class ModLangProvider extends LanguageProvider {
    /** 一条语言条目：键 + 中文 + 英文（中文与英文永远同键）。 */
    private record Entry(String key, String zhCn, String enUs) {}

    private static final List<Entry> ENTRIES = buildEntries();

    private final boolean chinese;

    /**
     * @param chinese true 写 zh_cn.json，false 写 en_us.json
     */
    public ModLangProvider(PackOutput output, boolean chinese) {
        super(output, Farmstorm.MOD_ID, chinese ? "zh_cn" : "en_us");
        this.chinese = chinese;
    }

    @Override
    protected void addTranslations() {
        for (Entry entry : ENTRIES) {
            add(entry.key(), this.chinese ? entry.zhCn() : entry.enUs());
        }
    }

    private static List<Entry> buildEntries() {
        List<Entry> entries = new ArrayList<>();
        // ---- 22 个物品（TECH §3.1）----
        item(entries, "puffed_wheat", "膨胀麦", "Puffed Wheat");
        item(entries, "blast_carrot", "胡罗贝", "Blast Carrot");
        item(entries, "ubi_server", "育碧服务器", "Ubi Server");
        item(entries, "toxic_ubi_server", "毒育碧服务器", "Toxic Ubi Server");
        item(entries, "power_beet", "甜菜！", "Power Beet");
        item(entries, "melon_slice_bomb", "瓜片子", "Melon Slice Bomb");
        item(entries, "melon_grenade", "瓜！", "Melon Grenade");
        item(entries, "pumpkin_turret", "man瓜", "Pumpkin Turret");
        item(entries, "awakened_torchflower", "激活的火把花", "Awakened Torchflower");
        item(entries, "awakened_pitcher_plant", "激活的瓶子草", "Awakened Pitcher Plant");
        item(entries, "fang_trap_cactus", "刺球", "Fang Trap Cactus");
        item(entries, "bamboo_firecracker", "爆竹", "Bamboo Firecracker");
        item(entries, "red_mushroom_puff", "？！菇菇！？", "Red Mushroom Puff");
        item(entries, "brown_mushroom_puff", "？！棕色菇菇色棕！？", "Brown Mushroom Puff");
        item(entries, "lucky_berry", "幸运浆果", "Lucky Berry");
        item(entries, "puffed_wheat_seeds", "膨胀麦种子", "Puffed Wheat Seeds");
        item(entries, "power_beet_seeds", "甜菜！种子", "Power Beet Seeds");
        item(entries, "melon_grenade_seeds", "瓜！种子", "Melon Grenade Seeds");
        item(entries, "pumpkin_turret_seeds", "man瓜种子", "Pumpkin Turret Seeds");
        item(entries, "awakened_torchflower_seeds", "激活的火把花种子", "Awakened Torchflower Seeds");
        item(entries, "awakened_pitcher_plant_pod", "激活的瓶子草豆荚", "Awakened Pitcher Plant Pod");
        item(entries, "converter", "农产品转换器", "Converter");
        // ---- v0.0.2 的 5 个新物品（TECH §7.3 #1–#5，文案逐字）----
        item(entries, "molotov", "莫洛托夫！", "Molotov");
        item(entries, "zangief_cane", "桑吉尔蔗", "Zangief Cane");
        item(entries, "wild_apple", "狂野苹果", "Wild Apple");
        item(entries, "compressed_puffed_wheat", "压缩膨胀麦", "Compressed Puffed Wheat");
        item(entries, "compressed_hay_block", "压缩干草块", "Compressed Hay Block");
        // ---- v0.0.3 的 4 个新物品（F-134 / 18-TECH §9.4）----
        item(entries, "friend_chives", "朋友的韭", "Friend Chives");
        item(entries, "chive_seeds", "朋友的韭种子", "Chive Seeds");
        item(entries, "point_defense_fungus", "近防菌", "Point Defense Fungus");
        item(entries, "vibration_bean", "震动豆", "Vibration Bean");
        // ---- v0.1.0 的 1 个新物品（F-169 / 24-TECH §9.4）：据点核心的 BlockItem 名 ----
        item(entries, "outpost_core", "据点核心", "Outpost Core");
        // ---- 20 个方块（TECH §3.2；作物方块与作物物品同名，前缀不同不冲突）----
        block(entries, "puffed_wheat_crop", "膨胀麦", "Puffed Wheat");
        block(entries, "blast_carrot_crop", "胡罗贝", "Blast Carrot");
        block(entries, "ubi_server_crop", "育碧服务器", "Ubi Server");
        block(entries, "toxic_ubi_server_crop", "毒育碧服务器", "Toxic Ubi Server");
        block(entries, "power_beet_crop", "甜菜！", "Power Beet");
        block(entries, "melon_grenade_stem", "瓜！", "Melon Grenade");
        block(entries, "pumpkin_turret_stem", "man瓜", "Pumpkin Turret");
        block(entries, "awakened_torchflower_crop", "激活的火把花", "Awakened Torchflower");
        block(entries, "awakened_pitcher_plant_crop", "激活的瓶子草", "Awakened Pitcher Plant");
        block(entries, "fang_trap_cactus_block", "刺球", "Fang Trap Cactus");
        block(entries, "bamboo_firecracker_stalk", "爆竹", "Bamboo Firecracker");
        block(entries, "red_mushroom_puff_block", "？！菇菇！？", "Red Mushroom Puff");
        block(entries, "brown_mushroom_puff_block", "？！棕色菇菇色棕！？", "Brown Mushroom Puff");
        block(entries, "lucky_berry_bush", "幸运浆果", "Lucky Berry");
        block(entries, "blast_carrot", "胡罗贝", "Blast Carrot");
        block(entries, "pumpkin_turret", "man瓜", "Pumpkin Turret");
        block(entries, "awakened_torchflower", "激活的火把花", "Awakened Torchflower");
        block(entries, "awakened_pitcher_plant", "激活的瓶子草", "Awakened Pitcher Plant");
        block(entries, "fang_trap_cactus", "刺球", "Fang Trap Cactus");
        block(entries, "converter", "农产品转换器", "Converter");
        // ---- v0.0.2 的 2 个新方块（TECH §7.3 #6–#7）----
        block(entries, "zangief_cane", "桑吉尔蔗", "Zangief Cane");
        block(entries, "compressed_hay_block", "压缩干草块", "Compressed Hay Block");
        // ---- v0.0.3 的 3 个新方块（F-134 / 18-TECH §9.4）----
        block(entries, "friend_chives_crop", "朋友的韭", "Friend Chives");
        block(entries, "point_defense_fungus", "近防菌", "Point Defense Fungus");
        block(entries, "vibration_bean", "震动豆", "Vibration Bean");
        // ---- v0.1.0 的 1 个新方块（F-169 / 24-TECH §9.4）：据点核心的方块名 ----
        block(entries, "outpost_core", "据点核心", "Outpost Core");
        // ---- 13 个实体类型（TECH §3.4）----
        entity(entries, "puffed_wheat", "膨胀麦", "Puffed Wheat");
        entity(entries, "ubi_server", "育碧服务器", "Ubi Server");
        entity(entries, "toxic_ubi_server", "毒育碧服务器", "Toxic Ubi Server");
        entity(entries, "melon_slice_bomb", "瓜片子", "Melon Slice Bomb");
        entity(entries, "melon_grenade", "瓜！", "Melon Grenade");
        entity(entries, "bamboo_firecracker", "爆竹", "Bamboo Firecracker");
        entity(entries, "red_mushroom_puff", "？！菇菇！？", "Red Mushroom Puff");
        entity(entries, "brown_mushroom_puff", "？！棕色菇菇色棕！？", "Brown Mushroom Puff");
        entity(entries, "turret_bullet", "炮台导弹", "Turret Bullet");
        // ---- v0.0.2 的 2 个新投掷物实体（F-66；实体名会出现在召唤反馈与死亡信息里）----
        entity(entries, "molotov", "莫洛托夫！", "Molotov");
        entity(entries, "compressed_puffed_wheat", "压缩膨胀麦", "Compressed Puffed Wheat");
        // ---- v0.0.3 的 2 个新投掷物实体（F-135）：实体显示名与同名物品一致。
        // 漏掉 entity(...) 时 /summon 反馈与死亡信息会退化成裸键 entity.farmstorm.*。----
        entity(entries, "friend_chives", "朋友的韭", "Friend Chives");
        entity(entries, "vibration_bean", "震动豆", "Vibration Bean");
        // ---- 2 个 mob_effect（TECH §7.3 #8–#9；隐藏效果的键只用于 /effect 补全）----
        plain(entries, "effect.farmstorm.wild_hunter", "狂 野 猎 人", "Wild Hunter");
        plain(entries, "effect.farmstorm.zangief_piledriver", "桑吉尔蔗打桩", "Zangief Piledriver");
        // ---- v0.0.3 的 2 个新 mob_effect（F-134 / 18-TECH §9.4；时长由施加方写入，见 §8.1）----
        plain(entries, "effect.farmstorm.q_bounce", "Q弹", "Q-Bounce");
        plain(entries, "effect.farmstorm.confusion", "混乱", "Confusion");
        // ---- 2 条字幕（TECH §7.3 #10–#11；键名必须与 sounds.json 的 subtitle 逐字一致）----
        plain(entries, "subtitles.farmstorm.wild_hunter_loop", "狩猎主题响起", "Hunting theme plays");
        plain(entries, "subtitles.farmstorm.zangief_piledriver", "打桩机启动", "Piledriver hits");
        // ---- v0.0.3 的 1 条新字幕（F-134 / 18-TECH §9.4；键名与 sounds.json 的 subtitle 逐字一致）----
        plain(entries, "subtitles.farmstorm.friend_chives", "朋友的韭命中", "Chives hit");
        // ---- 固定键：创造标签页 / GUI / 提示 / 死亡信息（TECH §7.2 逐条）----
        plain(entries, "itemGroup.farmstorm.main", "农场风云", "Farmstorm");
        plain(entries, "container.farmstorm.converter", "农产品转换器", "Converter");
        plain(entries, "gui.farmstorm.converter.convert", "转换", "Convert");
        plain(entries, "gui.farmstorm.converter.xp_cost", "消耗经验：%s", "Experience cost: %s");
        plain(entries, "gui.farmstorm.converter.no_recipe", "无法转换", "Cannot convert");
        plain(entries, "gui.farmstorm.converter.need_catalyst", "缺少催化剂：%s", "Missing catalyst: %s");
        plain(entries, "gui.farmstorm.converter.not_enough_xp", "经验不足", "Not enough experience");
        plain(entries, "gui.farmstorm.converter.forward", "原版 → 改良", "Vanilla → Modified");
        plain(entries, "gui.farmstorm.converter.reverse", "改良 → 原版", "Modified → Vanilla");
        plain(entries, "gui.farmstorm.converter.tooltip", "放入原版或改良作物进行 1:1 转换", "Insert a vanilla or modified crop to convert 1:1");
        plain(entries, "tooltip.farmstorm.deployed_by", "部署者：%s", "Deployed by: %s");
        plain(entries, "tooltip.farmstorm.attached_face", "附着面：%s", "Attached face: %s");
        plain(entries, "tooltip.farmstorm.floating", "悬空", "Floating");
        plain(entries, "tooltip.farmstorm.team", "队伍：%s", "Team: %s");
        plain(entries, "tooltip.farmstorm.turret_range", "索敌范围：%s 格（切比雪夫）", "Targeting range: %s blocks (Chebyshev)");
        plain(entries, "tooltip.farmstorm.aura_radius", "光环半径：%s 格", "Aura radius: %s blocks");
        plain(entries, "tooltip.farmstorm.temporary_enchant", "临时附魔（%s 秒后消失）", "Temporary enchantment (expires in %s s)");
        plain(entries, "tooltip.farmstorm.cannot_break", "非己方玩家无法破坏", "Cannot be broken by other players");
        plain(entries, "tooltip.farmstorm.trap_armed", "已布置", "Armed");
        // ---- 6 个类别（01-DESIGN §2.3）+ 1 个类别分隔符（中英各自的顿号 / 逗号）----
        plain(entries, "tooltip.farmstorm.category.trap", "陷阱类", "Trap");
        plain(entries, "tooltip.farmstorm.category.turret", "炮台类", "Turret");
        plain(entries, "tooltip.farmstorm.category.demolition", "破障类", "Demolition");
        plain(entries, "tooltip.farmstorm.category.buff", "buff类", "Buff");
        plain(entries, "tooltip.farmstorm.category.construction", "建造类", "Construction");
        plain(entries, "tooltip.farmstorm.category.damage", "伤害类", "Damage");
        plain(entries, "tooltip.farmstorm.category_separator", "、", ", ");
        plain(entries, "death.attack.farmstorm.void_bolt", "%1$s 被虚空撕碎", "%1$s was torn apart by the void");
        plain(entries, "death.attack.farmstorm.void_bolt.player", "%1$s 被 %2$s 的 man瓜 击落", "%1$s was shot down by %2$s's Pumpkin Turret");
        plain(entries, "death.attack.farmstorm.crop_blast", "%1$s 被作物炸飞了", "%1$s was blown up by a crop");
        plain(entries, "death.attack.farmstorm.crop_blast.player", "%1$s 被 %2$s 的作物炸飞了", "%1$s was blown up by %2$s's crop");
        // ---- v0.0.3 的 1 个新伤害类型 → 2 键（F-134 / 18-TECH §9.4；message_id = farmstorm.zangief_slam）----
        plain(entries, "death.attack.farmstorm.zangief_slam", "%1$s 被桑吉尔蔗砸进了地里", "%1$s was slammed into the ground by a Zangief Cane");
        plain(entries, "death.attack.farmstorm.zangief_slam.player", "%1$s 被 %2$s 的桑吉尔蔗砸进了地里",
                "%1$s was slammed into the ground by %2$s's Zangief Cane");
        // ---- 12 条进度 × (title + description)（DEC C-7）----
        advancement(entries, "root", "农场风云", "Farmstorm", "获得任意一件改良作物", "Obtain any modified crop");
        advancement(entries, "converter", "装个转换器", "Get a Converter", "放置一台农产品转换器", "Place a Crop Converter");
        advancement(entries, "first_conversion", "双向奔赴", "Two-Way Trip", "在转换器内完成一次转换", "Complete one conversion in a Crop Converter");
        advancement(entries, "nether_star_toll", "星之代价", "The Star's Toll", "消耗下界之星完成一次激活的火把花或瓶子草转换", "Spend a nether star on an Awakened Torchflower or Awakened Pitcher Plant conversion");
        advancement(entries, "seed_cycle", "闭环自给", "Self-Sufficient Cycle", "收获一次成熟改良作物，并同时拾取作物与播种物", "Harvest a mature modified crop and pick up both the crop and its seed");
        advancement(entries, "first_throw", "出手", "First Throw", "投掷任意一件投掷型改良作物", "Throw any throwable modified crop");
        advancement(entries, "first_deploy", "落子", "First Seed", "部署任意一件部署型改良作物", "Deploy any deployable modified crop");
        advancement(entries, "demolition", "未授权拆墙", "Unauthorized Demolition", "用破障类作物的爆炸破坏至少一个方块", "Break at least one block with a block-breaking crop explosion");
        advancement(entries, "turret_hit", "别站那儿", "Don't Stand There", "被 man瓜 的导弹命中一次", "Get hit by a Pumpkin Turret missile");
        advancement(entries, "aura_bath", "光环洗浴", "Aura Bath", "从激活的火把花或瓶子草获得一次光环效果", "Receive an aura effect from an Awakened Torchflower or Awakened Pitcher Plant");
        advancement(entries, "fang_dance", "最扎人的一集", "The Itchiest Episode", "触发一次「赢过输过没死过」", "Trigger a Fang Trap Cactus once");
        advancement(entries, "lucky_jackpot", "老实发育，很枯燥吧", "Playing It Safe Is Boring", "食用幸运浆果并抽中 0.1% 的创造模式分支", "Eat a Lucky Berry and hit the 0.1% creative mode branch");
        // ---- v0.0.2 的 3 条新进度（DEC A-09 的短句；TECH §7.3 #12–#17）----
        // 进度 JSON 由 F-98 生成，键先在这里立好，F-98 落地时不必再改语言表。
        advancement(entries, "first_molotov", "火了！", "It's Lit!", "初次投出莫洛托夫！", "Throw a Molotov for the first time");
        advancement(entries, "zangief_hit", "手摇重 360", "Hand-Cranked 360", "桑吉尔蔗首次命中一名玩家", "Land a Zangief Cane on a player for the first time");
        advancement(entries, "first_wild_hunter", "狂野猎人", "Wild Hunter", "首次获得「狂 野 猎 人」效果", "Obtain the Wild Hunter effect for the first time");
        // ---- v0.0.3 的 3 条新进度（F-134；显示名取 17-DESIGN §10.1 表第 16–18 行的定稿值）----
        advancement(entries, "first_q_bounce", "弹起来了", "Q-Bounce", "首次获得「Q弹」效果", "Obtain the Q-Bounce effect for the first time");
        advancement(entries, "point_defense_intercept", "一颗都不放", "Not One Through", "近防菌首次拦下一枚非己方投掷物",
                "Have a Point Defense Fungus intercept a foreign projectile for the first time");
        advancement(entries, "vibration_bean_trigger", "留个纪念", "A Souvenir", "首次踩中震动豆并完成结算",
                "Trigger a Vibration Bean for the first time");
        // ---- 139 个配置项（键路径 = run/config/farmstorm-server.toml 的段/键）----
        config(entries, "converter.xpCost", "每次转换扣除的经验点数", "Experience points spent per conversion");
        config(entries, "converter.netherStarForwardCost", "正向转换额外消耗的下界之星个数", "Nether stars consumed per forward conversion");
        config(entries, "converter.netherStarOnReverse", "反向转换是否也消耗下界之星", "Whether reverse conversions consume a nether star too");
        config(entries, "stack.throwable", "投掷型作物的堆叠上限", "Stack size of throwable crops");
        config(entries, "stack.other", "部署型 / 食用型 / 播种物的堆叠上限", "Stack size of deployable, edible and seed items");
        config(entries, "deploy.turretMiningTicks", "炮台硬挖时长（tick）", "Turret hard-mining duration (ticks)");
        config(entries, "deploy.supportMustBeFullOpaque", "是否强制依附方块为完整不透明方块", "Require a full opaque support block");
        config(entries, "deploy.dropOnSupportLost", "依附方块消失且不悬空时是否掉落本体", "Drop the item when the support block is lost");
        config(entries, "teams.resolver", "己方判定实现（scoreboard / ftbteams）", "Ally resolution backend (scoreboard / ftbteams)");
        config(entries, "teams.untamedFallback", "无队伍玩家的降级口径", "Fallback rule for players without a team");
        config(entries, "explosion.sliceResistThreshold", "瓜片子爆炸无法摧毁的抗性阈值", "Blast resistance above which Melon Slice Bomb cannot break blocks");
        config(entries, "explosion.grenadeResistThreshold", "瓜！爆炸无法摧毁的抗性阈值", "Blast resistance above which Melon Grenade cannot break blocks");
        config(entries, "throwable.lifetimeTicks", "投掷物实体的寿命上限（tick）", "Projectile lifetime (ticks)");
        config(entries, "food.eatTicks", "食用型作物的食用时长（tick）", "Eating duration of edible crops (ticks)");
        config(entries, "puffed_wheat.dryEdge", "干态替换立方体边长", "Replacement cube edge length when dry");
        config(entries, "puffed_wheat.wetEdge", "遇水扩容后的立方体边长", "Replacement cube edge length when wet");
        config(entries, "blast_carrot.triggerRadius", "陷阱触发的切比雪夫半径", "Trap trigger Chebyshev radius");
        config(entries, "blast_carrot.explosionPower", "爆炸威力", "Explosion power");
        config(entries, "blast_carrot.maxDamage", "胡罗贝爆炸的伤害上限", "Damage cap of the Blast Carrot explosion");
        config(entries, "blast_carrot.nausea.level", "反胃等级（1 = I 级）", "Nausea level (1 = level I)");
        config(entries, "blast_carrot.nausea.durationTicks", "反胃时长（0 = 不施加）", "Nausea duration in ticks (0 = not applied)");
        config(entries, "ubi_server.explosionPower", "爆炸威力", "Explosion power");
        config(entries, "ubi_server.maxDamage", "伤害上限", "Damage cap");
        config(entries, "toxic_ubi_server.explosionPower", "爆炸威力", "Explosion power");
        config(entries, "toxic_ubi_server.maxDamage", "伤害上限", "Damage cap");
        config(entries, "toxic_ubi_server.cloudRadius", "剧毒药水云半径", "Poison cloud radius");
        config(entries, "toxic_ubi_server.cloudDurationTicks", "剧毒药水云时长（tick）", "Poison cloud duration (ticks)");
        config(entries, "melon_slice_bomb.explosionPower", "爆炸威力", "Explosion power");
        config(entries, "melon_slice_bomb.maxDamage", "伤害上限", "Damage cap");
        config(entries, "melon_grenade.spawnCount", "落点上方生成的瓜片子个数", "Melon slices spawned above the impact point");
        config(entries, "melon_grenade.spawnOffsetY", "生成位置相对落点的垂直偏移", "Vertical offset from the impact point");
        config(entries, "melon_grenade.horizontalJitter", "x/z 轴随机偏移上界", "Random offset bound on the x/z axes");
        config(entries, "melon_grenade.explosionPower", "爆炸威力", "Explosion power");
        config(entries, "melon_grenade.maxDamage", "伤害上限", "Damage cap");
        config(entries, "pumpkin_turret.targetRadius", "索敌切比雪夫半径", "Targeting Chebyshev radius");
        config(entries, "pumpkin_turret.projectileDamage", "导弹命中造成的虚空伤害点数", "Void damage dealt by each missile hit");
        config(entries, "pumpkin_turret.knockbackLevel", "命中时施加的击退等级", "Knockback level applied on hit");
        config(entries, "pumpkin_turret.fireIntervalTicks", "开火间隔（tick）", "Fire interval (ticks)");
        config(entries, "awakened_torchflower.auraRadius", "光环欧氏半径", "Aura Euclidean radius");
        config(entries, "awakened_torchflower.refreshTicks", "光环刷新间隔（tick）", "Aura refresh interval (ticks)");
        config(entries, "awakened_torchflower.fireAspectLevel", "手持剑时授予的火焰附加等级", "Fire Aspect level granted to held swords");
        config(entries, "awakened_torchflower.particlesPerRefresh", "每次刷新生成的火焰粒子数", "Flame particles generated per refresh");
        config(entries, "awakened_torchflower.strength.level", "力量等级（1 = I 级）", "Strength level (1 = level I)");
        config(entries, "awakened_torchflower.strength.durationTicks", "力量时长（0 = 不施加）", "Strength duration in ticks (0 = not applied)");
        config(entries, "awakened_torchflower.resistance.level", "抗性提升等级（1 = I 级）", "Resistance level (1 = level I)");
        config(entries, "awakened_torchflower.resistance.durationTicks", "抗性提升时长（0 = 不施加）", "Resistance duration in ticks (0 = not applied)");
        config(entries, "awakened_torchflower.haste.level", "急迫等级（1 = I 级）", "Haste level (1 = level I)");
        config(entries, "awakened_torchflower.haste.durationTicks", "急迫时长（0 = 不施加）", "Haste duration in ticks (0 = not applied)");
        config(entries, "awakened_torchflower.speed.level", "速度等级（1 = I 级）", "Speed level (1 = level I)");
        config(entries, "awakened_torchflower.speed.durationTicks", "速度时长（0 = 不施加）", "Speed duration in ticks (0 = not applied)");
        config(entries, "awakened_torchflower.water_breathing.level", "水下呼吸等级（1 = I 级）", "Water Breathing level (1 = level I)");
        config(entries, "awakened_torchflower.water_breathing.durationTicks", "水下呼吸时长（0 = 不施加）", "Water Breathing duration in ticks (0 = not applied)");
        config(entries, "awakened_torchflower.night_vision.level", "夜视等级（1 = I 级）", "Night Vision level (1 = level I)");
        config(entries, "awakened_torchflower.night_vision.durationTicks", "夜视时长（0 = 不施加）", "Night Vision duration in ticks (0 = not applied)");
        config(entries, "awakened_pitcher_plant.auraRadius", "光环欧氏半径", "Aura Euclidean radius");
        config(entries, "awakened_pitcher_plant.refreshTicks", "光环刷新间隔（tick）", "Aura refresh interval (ticks)");
        config(entries, "awakened_pitcher_plant.clearHarmful", "是否移除范围内玩家的全部负面效果", "Remove all harmful effects from players in range");
        config(entries, "awakened_pitcher_plant.particlesPerRefresh", "每次刷新生成的爱心粒子数", "Heart particles generated per refresh");
        config(entries, "awakened_pitcher_plant.regeneration.level", "生命恢复等级（1 = I 级）", "Regeneration level (1 = level I)");
        config(entries, "awakened_pitcher_plant.regeneration.durationTicks", "生命恢复时长（0 = 不施加）", "Regeneration duration in ticks (0 = not applied)");
        config(entries, "awakened_pitcher_plant.saturation.level", "饱和等级（1 = I 级）", "Saturation level (1 = level I)");
        config(entries, "awakened_pitcher_plant.saturation.durationTicks", "饱和时长（0 = 不施加）", "Saturation duration in ticks (0 = not applied)");
        config(entries, "awakened_pitcher_plant.absorption.level", "伤害吸收等级（1 = I 级）", "Absorption level (1 = level I)");
        config(entries, "awakened_pitcher_plant.absorption.durationTicks", "伤害吸收时长（0 = 不施加）", "Absorption duration in ticks (0 = not applied)");
        config(entries, "fang_trap_cactus.triggerRadius", "触发用欧氏距离", "Trigger Euclidean distance");
        config(entries, "fang_trap_cactus.outerFangCount", "外圈尖牙个数", "Outer fang count");
        config(entries, "fang_trap_cactus.outerDelayTicks", "外圈尖牙延迟（tick）", "Outer fang delay (ticks)");
        config(entries, "fang_trap_cactus.outerRadius", "外圈尖牙分布半径", "Outer fang distribution radius");
        config(entries, "fang_trap_cactus.innerFangCount", "内圈尖牙个数", "Inner fang count");
        config(entries, "fang_trap_cactus.innerDelayTicks", "内圈尖牙延迟（tick）", "Inner fang delay (ticks)");
        config(entries, "fang_trap_cactus.innerRadius", "内圈尖牙分布半径", "Inner fang distribution radius");
        config(entries, "bamboo_firecracker.spawnIntervalTicks", "生成一个风弹的间隔（tick）", "Wind charge spawn interval (ticks)");
        config(entries, "bamboo_firecracker.durationTicks", "生成持续总时长（tick）", "Total spawn duration (ticks)");
        config(entries, "red_mushroom_puff.radius", "范围效果欧氏半径", "Effect Euclidean radius");
        config(entries, "red_mushroom_puff.nausea.level", "反胃等级（1 = I 级）", "Nausea level (1 = level I)");
        config(entries, "red_mushroom_puff.nausea.durationTicks", "反胃时长（0 = 不施加）", "Nausea duration in ticks (0 = not applied)");
        config(entries, "red_mushroom_puff.blindness.level", "失明等级（1 = I 级）", "Blindness level (1 = level I)");
        config(entries, "red_mushroom_puff.blindness.durationTicks", "失明时长（0 = 不施加）", "Blindness duration in ticks (0 = not applied)");
        config(entries, "brown_mushroom_puff.radius", "范围效果欧氏半径", "Effect Euclidean radius");
        config(entries, "brown_mushroom_puff.weakness.level", "虚弱等级（1 = I 级）", "Weakness level (1 = level I)");
        config(entries, "brown_mushroom_puff.weakness.durationTicks", "虚弱时长（0 = 不施加）", "Weakness duration in ticks (0 = not applied)");
        config(entries, "brown_mushroom_puff.slowness.level", "缓慢等级（1 = I 级）", "Slowness level (1 = level I)");
        config(entries, "brown_mushroom_puff.slowness.durationTicks", "缓慢时长（0 = 不施加）", "Slowness duration in ticks (0 = not applied)");
        config(entries, "lucky_berry.nutrition", "营养值", "Nutrition");
        config(entries, "lucky_berry.saturationModifier", "饱和度修正", "Saturation modifier");
        config(entries, "lucky_berry.randomCropCount", "随机生成改良作物的个数", "Number of random modified crops generated");
        config(entries, "lucky_berry.lootChestCount", "生成的末地城战利品宝箱个数", "Number of End City loot chests generated");
        config(entries, "lucky_berry.smallExplosionPower", "小爆炸的威力", "Power of the small explosion");
        config(entries, "lucky_berry.creativeDurationTicks", "创造模式的持续时长（tick）", "Creative mode duration (ticks)");
        config(entries, "lucky_berry.regeneration.level", "生命恢复等级（1 = I 级，25% 分支）", "Regeneration level (1 = level I, 25% branch)");
        config(entries, "lucky_berry.regeneration.durationTicks", "生命恢复时长（0 = 不施加）", "Regeneration duration in ticks (0 = not applied)");
        config(entries, "lucky_berry.strength.level", "力量等级（1 = I 级，1.9% 分支）", "Strength level (1 = level I, 1.9% branch)");
        config(entries, "lucky_berry.strength.durationTicks", "力量时长（0 = 不施加）", "Strength duration in ticks (0 = not applied)");
        config(entries, "lucky_berry.resistance.level", "抗性提升等级（1 = I 级，1.9% 分支）", "Resistance level (1 = level I, 1.9% branch)");
        config(entries, "lucky_berry.resistance.durationTicks", "抗性提升时长（0 = 不施加）", "Resistance duration in ticks (0 = not applied)");
        config(entries, "lucky_berry.jump_boost.level", "跳跃提升等级（1 = I 级，1.9% 分支）", "Jump Boost level (1 = level I, 1.9% branch)");
        config(entries, "lucky_berry.jump_boost.durationTicks", "跳跃提升时长（0 = 不施加）", "Jump Boost duration in ticks (0 = not applied)");
        config(entries, "lucky_berry.speed.level", "速度等级（1 = I 级，1.9% 分支）", "Speed level (1 = level I, 1.9% branch)");
        config(entries, "lucky_berry.speed.durationTicks", "速度时长（0 = 不施加）", "Speed duration in ticks (0 = not applied)");
        config(entries, "lucky_berry.chance.lightning", "概率：在食用者位置生成一道闪电（%）", "Chance: strike lightning at the eater (%)");
        config(entries, "lucky_berry.chance.random_crops", "概率：在食用者身边生成随机改良作物（%）", "Chance: spawn random modified crops around the eater (%)");
        config(entries, "lucky_berry.chance.regeneration", "概率：获得生命恢复分支（%）", "Chance: regeneration branch (%)");
        config(entries, "lucky_berry.chance.loot_chest", "概率：生成末地城战利品宝箱（%）", "Chance: spawn End City loot chests (%)");
        config(entries, "lucky_berry.chance.small_explosion", "概率：发生一次小爆炸（%）", "Chance: trigger a small explosion (%)");
        config(entries, "lucky_berry.chance.charged_creeper", "概率：生成一只闪电苦力怕（%）", "Chance: spawn a charged creeper (%)");
        config(entries, "lucky_berry.chance.godlike_buff", "概率：获得 600 秒的神级增益（%）", "Chance: 600-second godlike buff (%)");
        config(entries, "lucky_berry.chance.creative_mode", "概率：切换为创造模式（%）", "Chance: switch to creative mode (%)");
        config(entries, "power_beet.nutrition", "营养值", "Nutrition");
        config(entries, "power_beet.saturationModifier", "饱和度修正", "Saturation modifier");
        config(entries, "power_beet.strength.level", "力量等级（1 = I 级）", "Strength level (1 = level I)");
        config(entries, "power_beet.strength.durationTicks", "力量时长（0 = 不施加）", "Strength duration in ticks (0 = not applied)");
        config(entries, "power_beet.speed.level", "速度等级（1 = I 级）", "Speed level (1 = level I)");
        config(entries, "power_beet.speed.durationTicks", "速度时长（0 = 不施加）", "Speed duration in ticks (0 = not applied)");
        config(entries, "power_beet.haste.level", "急迫等级（1 = I 级）", "Haste level (1 = level I)");
        config(entries, "power_beet.haste.durationTicks", "急迫时长（0 = 不施加）", "Haste duration in ticks (0 = not applied)");
        config(entries, "power_beet.resistance.level", "抗性提升等级（1 = I 级）", "Resistance level (1 = level I)");
        config(entries, "power_beet.resistance.durationTicks", "抗性提升时长（0 = 不施加）", "Resistance duration in ticks (0 = not applied)");
        // ---- v0.0.2 的 4 个新分组 22 条（F-61 / TECH §7.3 #18–#39，文案逐字照 §7.3）----
        config(entries, "molotov.effectRadius", "落点球半径（格）", "Impact sphere radius (blocks)");
        config(entries, "molotov.fireChance", "每个候选方块被点火的概率（%）", "Chance (%) to place fire on each candidate block");
        config(entries, "molotov.triggerExplosionPower", "落点触发爆炸的威力（0 = 只当触发器）", "Power of the landing trigger explosion (0 = trigger only)");
        config(entries, "wild_apple.nutrition", "营养值", "Nutrition");
        config(entries, "wild_apple.saturationModifier", "饱和度修正", "Saturation modifier");
        config(entries, "wild_hunter.durationTicks", "效果基础时长（tick）", "Base effect duration (ticks)");
        config(entries, "wild_hunter.killExtensionTicks", "每次击杀延长的时长（tick）", "Duration added per kill (ticks)");
        config(entries, "wild_hunter.killHealPoints", "每次击杀回复的生命点数", "Health restored per kill");
        config(entries, "wild_hunter.searchRadius", "索敌球半径（格）", "Target search radius (blocks)");
        config(entries, "wild_hunter.searchAngleDegrees", "索敌锥全顶角（度，60 = 半角 30°）", "Search cone full apex angle (degrees; 60 = 30 half-angle)");
        config(entries, "wild_hunter.dashTicks", "吸附冲刺跨越的刻数", "Ticks the dash takes");
        config(entries, "wild_hunter.speedBonusPercent", "移动速度加成（%）", "Movement speed bonus (%)");
        config(entries, "wild_hunter.attackSpeedBonusPercent", "攻击速度加成（%）", "Attack speed bonus (%)");
        config(entries, "wild_hunter.attackDamageBonusPercent", "攻击伤害加成（%）", "Attack damage bonus (%)");
        config(entries, "wild_hunter.highlightRadius", "红色剪影的最大距离（格）", "Maximum silhouette distance (blocks)");
        config(entries, "wild_hunter.loopSoundRange", "循环音的可听距离（格）", "Looping sound audible range (blocks)");
        config(entries, "zangief_cane.triggerRadius", "触发球半径（格）", "Trigger sphere radius (blocks)");
        config(entries, "zangief_cane.stageDelayTicks", "相位 0 的等待刻数", "Ticks spent in phase 0");
        // 作者实测改版：相位 1 由「漂浮」改成「快速升高」、相位 2 由「伺服下坠」改成「自然坠落 + 摔伤」；
        // 键**只改名不增删**，config 键总数仍是 139。
        config(entries, "zangief_cane.riseHeightBlocks", "相位 1 向上抬升的高度（格）", "Height (blocks) the player is lifted in phase 1");
        config(entries, "zangief_cane.riseTicks", "相位 1 抬完所用的刻数", "Ticks used to lift the player in phase 1");
        config(entries, "zangief_cane.spinDelayTicks", "视角自转开始前的倒转时长（刻）", "Ticks of upside-down before the spin starts");
        config(entries, "zangief_cane.spinDegreesPerTick", "水平自转速度（度/刻）", "Horizontal spin speed (degrees/tick)");
        // ---- v0.0.3 的 4 个新分组 12 条 + 两组追加 4 条 = 16 条（F-104 / 18-TECH §8.1）----
        // §8.1 的 19 行里有 3 行是「改既有默认值」（fang_trap_cactus 的 triggerRadius / outerDelayTicks /
        // innerDelayTicks），它们的语言键已在上面写过、§8.1 表下注要求逐字保留，因此不重复登记。
        config(entries, "qbounce.poseMode", "坐姿分支（render = 只改渲染层，mount = 服务端挂隐形坐骑）",
                "Pose branch (render = rendering only, mount = server-side invisible mount)");
        config(entries, "qbounce.rotateDegreesPerTick", "持续旋转的角速度（度/刻）", "Continuous rotation speed (degrees/tick)");
        config(entries, "qbounce.squashAmplitude", "压缩拉伸幅度（模型原尺寸的比例，0.15 = 15 %）",
                "Squash-stretch amplitude (ratio of the original model size; 0.15 = 15 %)");
        config(entries, "qbounce.squashPeriodTicks", "压缩拉伸一个完整周期的刻数", "Ticks of one full squash-stretch cycle");
        config(entries, "friend_chives.hitRadius", "落点作用球半径（格）", "Impact sphere radius (blocks)");
        config(entries, "friend_chives.effectTicks", "Q弹 时长（刻）", "Q-Bounce duration in ticks");
        config(entries, "friend_chives.soundRange", "命中音的可闻半径（格）", "Hit sound audible range (blocks)");
        config(entries, "point_defense_fungus.interceptRadius", "拦截作用球半径（格）", "Intercept sphere radius (blocks)");
        config(entries, "point_defense_fungus.maxIntercepts", "拦截次数上限（第 N 次后静默移除）",
                "Intercept limit (silently removed after the Nth intercept)");
        config(entries, "point_defense_fungus.interceptParticles", "拦截图效的粒子数", "Intercept particle count");
        config(entries, "vibration_bean.triggerRadius", "触发距离（格，严格小于）",
                "Trigger distance (blocks, strictly less than)");
        config(entries, "vibration_bean.confusionTicks", "混乱时长（刻）", "Confusion duration in ticks");
        config(entries, "fang_trap_cactus.slownessDurationTicks", "缓慢时长（刻）", "Slowness duration in ticks");
        config(entries, "fang_trap_cactus.slownessLevel", "缓慢等级（1 = I 级）", "Slowness level (1 = level I)");
        config(entries, "zangief_cane.slamDamage", "落地固定伤害（点）", "Fixed landing damage (points)");
        config(entries, "zangief_cane.cancelFallDamage", "相位 2 期间取消自然坠落伤害",
                "Cancel natural fall damage during phase 2");
        // ---- v0.1.0 的 11 条新 config 键（F-139；24-TECH §8.1 的 12 行里第 10 行是改既有默认值，键不增）----
        config(entries, "growth.minSeconds", "成熟总时长下界（秒）", "Maturity duration lower bound (seconds)");
        config(entries, "growth.maxSeconds", "成熟总时长上界（秒）", "Maturity duration upper bound (seconds)");
        config(entries, "trap.immunityTicks", "触发后陷阱大类免疫的刻数（100 刻 = 5 秒）",
                "Ticks of trap-category immunity after a trigger (100 ticks = 5 s)");
        config(entries, "trap.dustIntervalTicks", "陷阱 dust 粒子的间隔（刻，100 刻 = 5 秒）",
                "Ticks between trap dust bursts (100 ticks = 5 s)");
        config(entries, "trap.maxTrapsPerPlayer", "每玩家陷阱名额（第 N+1 个挤掉最早的一个）",
                "Traps allowed per player (the next one evicts the oldest)");
        config(entries, "farmland.limitPerPlayer", "耕地额度的服务端默认值（0 = 开局谁也耕不了地）",
                "Server-side default farmland quota (0 = nobody can till at first)");
        config(entries, "outpost_core.farmlandBonus", "每个据点核心增加的耕地上限", "Farmland quota added per outpost core");
        config(entries, "outpost_core.maxPerPlayer", "每玩家可放置的据点核心数", "Outpost cores each player may place");
        config(entries, "outpost_core.boundaryParticleIntervalTicks", "边界粒子刷新间隔（刻，40 刻 = 2 秒）",
                "Boundary particle interval in ticks (40 ticks = 2 s)");
        config(entries, "melon_grenade.submunitionSpeed", "瓜片子的径向初速（格/刻）",
                "Radial launch speed of the melon slices (blocks/tick)");
        config(entries, "compressed.replaceOnlyAirAndFluid", "落地方块只替换空气与流体",
                "Landing block replaces only air and fluids");
        // ---- v0.1.0 的指令反馈（F-160 / TECH §7.3）：1 键，回显新额度 + 「本进程内有效、重启回 config 默认」，不带玩家名 ----
        plain(entries, "command.farmstorm.farmlandlimit.set",
                "耕地额度已设为 %s（本进程内有效，重启回 config 默认）",
                "Farmland quota set to %s (effective in this process only; a restart falls back to the config default)");
        // ---- v0.1.0 的 2 条动作栏提示（F-157 / 24 号 §6.3 的第 2、3 步）----
        plain(entries, "message.farmstorm.farmland.no_core", "附近没有己方据点核心", "No allied outpost core nearby");
        plain(entries, "message.farmstorm.farmland.quota_full", "耕地额度已满：%s/%s", "Farmland quota full: %s/%s");
        return List.copyOf(entries);
    }

    private static void item(List<Entry> entries, String id, String zhCn, String enUs) {
        entries.add(new Entry("item.farmstorm." + id, zhCn, enUs));
    }

    private static void block(List<Entry> entries, String id, String zhCn, String enUs) {
        entries.add(new Entry("block.farmstorm." + id, zhCn, enUs));
    }

    private static void entity(List<Entry> entries, String id, String zhCn, String enUs) {
        entries.add(new Entry("entity.farmstorm." + id, zhCn, enUs));
    }

    /** itemGroup / container / gui / tooltip / death 这类键名本身就完整的条目。 */
    private static void plain(List<Entry> entries, String key, String zhCn, String enUs) {
        entries.add(new Entry(key, zhCn, enUs));
    }

    private static void advancement(List<Entry> entries, String path, String titleZhCn, String titleEnUs,
                                    String descriptionZhCn, String descriptionEnUs) {
        entries.add(new Entry("advancements.farmstorm." + path + ".title", titleZhCn, titleEnUs));
        entries.add(new Entry("advancements.farmstorm." + path + ".description", descriptionZhCn, descriptionEnUs));
    }

    /** 配置项：路径与 {@code FarmstormConfig} 的 {@code tr(path)} 逐字一致（§7.3 第 4 条）。 */
    private static void config(List<Entry> entries, String path, String zhCn, String enUs) {
        entries.add(new Entry("farmstorm.configuration." + path, zhCn, enUs));
    }
}
