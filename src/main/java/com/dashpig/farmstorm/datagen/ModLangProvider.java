package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 语言文件（TECH §7）：zh_cn 与 en_us 各 222 键。
 *
 * <p>键集完全一致由构造方式保证：两种语言都从同一张 {@link #ENTRIES} 表按列写盘，
 * 不存在「两份键各自维护」的可能。条目构成：
 * 22 item + 20 block + 9 entity + 1 itemGroup + 1 container + 8 gui + 16 tooltip
 * + 4 death + 24 advancement + 117 config。
 *
 * <p>tooltip 计 16（FIX-7）：§2.3 的六个类别各一个键（{@code tooltip.farmstorm.category.*}）
 * 取代原先 {@code DeployedKind} 的三个 {@code tooltip.farmstorm.type.*} 单值键——类别是多值集合，
 * 一个作物要能同时显示「炮台类、buff类」；再加一个 {@code tooltip.farmstorm.category_separator}
 * 让中英各自用自己的分隔符（「、」/ 「, 」）。218 - 3 + 6 + 1 = 222。
 *
 * <p>中文名逐字照抄 plan.txt（含全角标点）；英文名按 DEC C-10：注册 ID 的 Title Case。
 * 117 个 config 键的路径与 {@code run/config/farmstorm-server.toml} 的段/键逐段对应，
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
        item(entries, "fang_trap_cactus", "赢过输过没死过", "Fang Trap Cactus");
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
        block(entries, "fang_trap_cactus_block", "赢过输过没死过", "Fang Trap Cactus");
        block(entries, "bamboo_firecracker_stalk", "爆竹", "Bamboo Firecracker");
        block(entries, "red_mushroom_puff_block", "？！菇菇！？", "Red Mushroom Puff");
        block(entries, "brown_mushroom_puff_block", "？！棕色菇菇色棕！？", "Brown Mushroom Puff");
        block(entries, "lucky_berry_bush", "幸运浆果", "Lucky Berry");
        block(entries, "blast_carrot", "胡罗贝", "Blast Carrot");
        block(entries, "pumpkin_turret", "man瓜", "Pumpkin Turret");
        block(entries, "awakened_torchflower", "激活的火把花", "Awakened Torchflower");
        block(entries, "awakened_pitcher_plant", "激活的瓶子草", "Awakened Pitcher Plant");
        block(entries, "fang_trap_cactus", "赢过输过没死过", "Fang Trap Cactus");
        block(entries, "converter", "农产品转换器", "Converter");
        // ---- 9 个实体类型（TECH §3.4）----
        entity(entries, "puffed_wheat", "膨胀麦", "Puffed Wheat");
        entity(entries, "ubi_server", "育碧服务器", "Ubi Server");
        entity(entries, "toxic_ubi_server", "毒育碧服务器", "Toxic Ubi Server");
        entity(entries, "melon_slice_bomb", "瓜片子", "Melon Slice Bomb");
        entity(entries, "melon_grenade", "瓜！", "Melon Grenade");
        entity(entries, "bamboo_firecracker", "爆竹", "Bamboo Firecracker");
        entity(entries, "red_mushroom_puff", "？！菇菇！？", "Red Mushroom Puff");
        entity(entries, "brown_mushroom_puff", "？！棕色菇菇色棕！？", "Brown Mushroom Puff");
        entity(entries, "turret_bullet", "炮台导弹", "Turret Bullet");
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
        // ---- 117 个配置项（键路径 = run/config/farmstorm-server.toml 的段/键）----
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
