package com.dashpig.farmstorm;

import com.dashpig.farmstorm.combat.MobAiSuspension;
import com.dashpig.farmstorm.config.FarmstormClientConfig;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.event.FarmstormGameEvents;
import com.dashpig.farmstorm.event.VillagerTradeEvents;
import com.dashpig.farmstorm.network.FarmstormNetwork;
import com.dashpig.farmstorm.registry.FarmstormAttachments;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormCreativeTabs;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.registry.FarmstormEntityTypes;
import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.registry.FarmstormPoiTypes;
import com.dashpig.farmstorm.registry.FarmstormRecipes;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import com.dashpig.farmstorm.registry.FarmstormVillagerProfessions;
import com.dashpig.farmstorm.team.ScoreboardTeamResolver;
import com.dashpig.farmstorm.team.TeamResolver;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.CropTier;
import com.dashpig.farmstorm.util.DistanceMetrics;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模组入口（TECH §4.1）。
 * D-40 / HC-04：不使用字节码注入，全部需求用继承 + 事件总线 + 原生扩展点完成。
 */
@Mod(Farmstorm.MOD_ID)
public final class Farmstorm {
    public static final String MOD_ID = "farmstorm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final TeamResolver TEAMS = new ScoreboardTeamResolver();

    /**
     * 只收 {@code IEventBus} 一个参数。
     *
     * <p><b>不要加第二参 {@code ModContainer}</b>：NeoForge 47 认这个位置，原版 Forge 47 不认，
     * 结果 FML 匹配不到构造函数、回退去找无参构造，直接抛
     * {@code NoSuchMethodException: com.dashpig.farmstorm.Farmstorm.<init>()}（1.20.1-Forge_47.4.28 实测）。
     * 配置注册走 {@code ModLoadingContext}，本来也不需要 container。
     */
    public Farmstorm(IEventBus modBus) {
        FarmstormItems.REGISTER.register(modBus);
        FarmstormBlocks.REGISTER.register(modBus);
        FarmstormBlockEntities.REGISTER.register(modBus);
        FarmstormEntityTypes.REGISTER.register(modBus);
        FarmstormMenus.REGISTER.register(modBus);
        // RecipeType 与 RecipeSerializer 是两个 registry，因此 FarmstormRecipes 有两个 register（§3.5）。
        FarmstormRecipes.TYPE_REGISTER.register(modBus);
        FarmstormRecipes.SERIALIZER_REGISTER.register(modBus);
        FarmstormCreativeTabs.REGISTER.register(modBus);
        // 2 个 mob_effects 与 2 个 sound_events（F-64 / TECH §2.2 #10~#13）：D-50 的相位通道与 D-66/D-76 的音频。
        FarmstormMobEffects.REGISTER.register(modBus);
        FarmstormSounds.REGISTER.register(modBus);
        // 口径外的第 13 类（F-252 / 裁定 #17 / A-73）：实体附件 farmstorm:ai_suspend。
        // 1.20.1 没有 AttachmentType 注册表，改落实体持久 NBT（见 FarmstormAttachments），无需注册到总线。
        FarmstormPoiTypes.REGISTER.register(modBus);
        FarmstormVillagerProfessions.REGISTER.register(modBus);

        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, FarmstormConfig.SPEC);
        // F-201：CLIENT 3 键单独一份 config/farmstorm-client.toml。**不进** FarmstormConfig——
        // 并进去会让 server toml 从 166 变 169（30 号 §5.5 第 4 条）。本类只依赖 ForgeConfigSpec，服务端也加载得动。
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, FarmstormClientConfig.SPEC);
        modBus.addListener(FarmstormClientConfig::onLoading);

        // F-199：自定义包。1.20.1 没有 RegisterPayloadHandlersEvent，改在 FMLCommonSetupEvent 里建 SimpleChannel。
        modBus.addListener(FarmstormNetwork::onCommonSetup);

        // 配置加载 / 重载后重建派生缓存（幸运浆果千分比表、爆炸阈值快照）。
        modBus.addListener(FarmstormConfig::onLoading);
        modBus.addListener(FarmstormConfig::onReloading);

        // game 总线：玩家 tick / 登录登出 / 爆炸拦截 / 爆炸批处理（F-15 ~ F-17）。
        FarmstormGameEvents.register(MinecraftForge.EVENT_BUS);
        // F-223：农民交易三项。事件在数据包重载（TagsUpdatedEvent）里对每个职业各抛一次，
        // 与上面同一条 game 总线；只处理 VillagerProfession.FARMER。
        VillagerTradeEvents.register(MinecraftForge.EVENT_BUS);
        // F-253：AI 挂起的每刻对账（EventPriority.HIGHEST，先于 mount 挂载的 NORMAL）、载入结算、停服结算。
        MobAiSuspension.register(MinecraftForge.EVENT_BUS);

        logSelfCheck();
        logRegistrySelfCheck();
        LOGGER.info("Farmstorm loading");
    }

    /**
     * 启动自检（M2-A 验收 2）：距离度量与幸运浆果权重表。
     * 打日志而不是抛异常——这两条错了说明实现错了，但也不该让服务器起不来。
     */
    private static void logSelfCheck() {
        double chebyshev = DistanceMetrics.chebyshev(3, 1, 3);
        double euclidean = DistanceMetrics.euclidean(3, 4, 0);
        int[] permille = FarmstormConfig.luckyBerryPermille();
        int weightSum = 0;
        for (int weight : permille) {
            weightSum += weight;
        }
        LOGGER.info("自检 chebyshev(3,1,3)={}（期望 3）→ {}", chebyshev, chebyshev == 3.0 ? "OK" : "FAIL");
        LOGGER.info("自检 euclidean(3,4,0)={}（期望 5.0）→ {}", euclidean, euclidean == 5.0 ? "OK" : "FAIL");
        LOGGER.info("自检 幸运浆果权重和={}（期望 1000，即 config 层百分比和 100）→ {}",
                weightSum, weightSum == 1000 ? "OK" : "FAIL");

        // 型与类是正交的两维（DESIGN §2.2 / §2.3）：型单值、类多值。
        // 这一行是「有人改了标签忘了改行为」的哨兵——数字一变就说明 DEC §4 那张表被改过了。
        int throwable = 0;
        int deployable = 0;
        int edible = 0;
        int demolition = 0;
        for (CropProfile profile : CropProfile.ALL) {
            switch (profile.type()) {
                case THROWABLE -> throwable++;
                case DEPLOYABLE -> deployable++;
                case EDIBLE -> edible++;
            }
            if (profile.canBreakBlocks()) {
                demolition++;
            }
        }
        boolean taxonomyOk = CropProfile.ALL.size() == 22
                && throwable == 12 && deployable == 7 && edible == 3 && demolition == 2;
        LOGGER.info("自检 作物 {} 种：投掷型 {} / 部署型 {} / 食用型 {}；破障类 {} → {}",
                CropProfile.ALL.size(), throwable, deployable, edible, demolition, taxonomyOk ? "OK" : "FAIL");

        // D-149：三档 11 / 7 / 4 —— 交易（D-157）、培养器准入（D-162）、幸运浆果池（D-156）三处都读同一个
        // tier()，错一条就三处一起错（RT-49）。这三个数只在这里断言；下游一律 filter，不写第二份字面清单（A-63）。
        int common = 0;
        int rare = 0;
        int epic = 0;
        for (CropProfile profile : CropProfile.ALL) {
            switch (profile.tier()) {
                case COMMON -> common++;
                case RARE -> rare++;
                case EPIC -> epic++;
            }
        }
        boolean tierOk = common == 11 && rare == 7 && epic == 4;
        LOGGER.info("自检 作物品质 普通 {} / 稀有 {} / 史诗 {} → {}",
                common, rare, epic, tierOk ? "OK" : "FAIL");

        // D-156 + A-63：池由 tier() 推导（只普通档），断言值与池的构造**同一次提交**改（RT-50）——
        // 前半条数条数（= 普通档全集），后半条保留「不含三条」的另半条（D-156 原文要求保留，见 36 号 §8 #35）。
        int poolSize = CropProfile.LUCKY_BERRY_POOL.size();
        boolean poolOk = poolSize == 11 && CropProfile.LUCKY_BERRY_POOL.stream().noneMatch(profile ->
                profile == CropProfile.LUCKY_BERRY || profile == CropProfile.WILD_APPLE
                        || profile == CropProfile.COMPRESSED_PUFFED_WHEAT);
        LOGGER.info("自检 幸运浆果池 {} 种（按 tier() 只留 COMMON，不含 lucky_berry / wild_apple / compressed_puffed_wheat）→ {}",
                poolSize, poolOk ? "OK" : "FAIL");
    }

    /**
     * 注册对象自检（F-66 / F-103 / TECH §11 #2）：64 → 77 → 90 → 98 的那 12 类**当场数**（各 DeferredRegister 的条目数），
     * 相加不等于 98、或任何一类的条目数与 36 号 §1.2 的 v0.2.0 列不符，就打 FAIL——不写死常量再自己跟自己比。
     * 伤害类型是数据驱动（R-02：代码侧只有 ResourceKey、没有 DeferredRegister），
     * 因此这一类数的是 {@link FarmstormDamageTypes} 里声明的静态 ResourceKey 常量本身。
     */
    private static void logRegistrySelfCheck() {
        int items = FarmstormItems.REGISTER.getEntries().size();
        int blocks = FarmstormBlocks.REGISTER.getEntries().size();
        int blockEntities = FarmstormBlockEntities.REGISTER.getEntries().size();
        int entityTypes = FarmstormEntityTypes.REGISTER.getEntries().size();
        int mobEffects = FarmstormMobEffects.REGISTER.getEntries().size();
        int soundEvents = FarmstormSounds.REGISTER.getEntries().size();
        int recipeTypes = FarmstormRecipes.TYPE_REGISTER.getEntries().size();
        int recipeSerializers = FarmstormRecipes.SERIALIZER_REGISTER.getEntries().size();
        int creativeTabs = FarmstormCreativeTabs.REGISTER.getEntries().size();
        int damageTypes = damageTypeCount();
        int menuTypes = FarmstormMenus.REGISTER.getEntries().size();
        int total = items + blocks + blockEntities + entityTypes + mobEffects + soundEvents + recipeTypes
                + recipeSerializers + creativeTabs + damageTypes + menuTypes;
        // HC-002（docs/zh/04 §2）：1.20.1 没有 DataComponentType，注册面从 92/12 类降到 91/11 类。
        boolean ok = total == 91
                && items == 28 && blocks == 28 && blockEntities == 8 && entityTypes == 13
                && mobEffects == 4 && soundEvents == 3 && recipeTypes == 1 && recipeSerializers == 1
                && creativeTabs == 1 && damageTypes == 3 && menuTypes == 1;
        LOGGER.info("自检 注册对象 {} 个：items {} / blocks {} / block_entities {} / entity_types {} / mob_effects {} / "
                        + "sound_events {} / recipe_types {} / recipe_serializers {} / creative_tabs {} / damage_types {} / "
                        + "menu_types {} → {}",
                total, items, blocks, blockEntities, entityTypes, mobEffects, soundEvents, recipeTypes,
                recipeSerializers, creativeTabs, damageTypes, menuTypes, ok ? "OK" : "FAIL");
    }

    /**
     * 数据驱动的伤害类型没有 DeferredRegister，只能把声明出来的 ResourceKey 逐个列出来数。
     * <p>新增伤害类型时**必须**同时加进这个列表：总数 92 的断言会因此失败，不会静默漂移。
     * 不用反射，是因为 {@code getDeclaredFields()} 依赖字段形状，改名或换个同类型常量都会悄悄改变结果。
     */
    private static int damageTypeCount() {
        return List.of(FarmstormDamageTypes.CROP_BLAST, FarmstormDamageTypes.VOID_BOLT,
                FarmstormDamageTypes.ZANGIEF_SLAM).size();
    }

    /** 统一入口（TECH §4.1）；1.20.1 的构造器仍公开，直接用。 */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
