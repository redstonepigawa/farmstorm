package com.dashpig.farmstorm;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.event.FarmstormGameEvents;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormCreativeTabs;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import com.dashpig.farmstorm.registry.FarmstormDataComponents;
import com.dashpig.farmstorm.registry.FarmstormEntityTypes;
import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.registry.FarmstormRecipes;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import com.dashpig.farmstorm.team.ScoreboardTeamResolver;
import com.dashpig.farmstorm.team.TeamResolver;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.DistanceMetrics;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
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

    public Farmstorm(IEventBus modBus, ModContainer container) {
        FarmstormItems.REGISTER.register(modBus);
        FarmstormBlocks.REGISTER.register(modBus);
        FarmstormBlockEntities.REGISTER.register(modBus);
        FarmstormEntityTypes.REGISTER.register(modBus);
        FarmstormMenus.REGISTER.register(modBus);
        // RecipeType 与 RecipeSerializer 是两个 registry，因此 FarmstormRecipes 有两个 register（§3.5）。
        FarmstormRecipes.TYPE_REGISTER.register(modBus);
        FarmstormRecipes.SERIALIZER_REGISTER.register(modBus);
        FarmstormDataComponents.REGISTER.register(modBus);
        FarmstormCreativeTabs.REGISTER.register(modBus);
        // 2 个 mob_effects 与 2 个 sound_events（F-64 / TECH §2.2 #10~#13）：D-50 的相位通道与 D-66/D-76 的音频。
        FarmstormMobEffects.REGISTER.register(modBus);
        FarmstormSounds.REGISTER.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, FarmstormConfig.SPEC);

        // 配置加载 / 重载后重建派生缓存（幸运浆果千分比表、爆炸阈值快照）。
        modBus.addListener(FarmstormConfig::onLoading);
        modBus.addListener(FarmstormConfig::onReloading);

        // game 总线：玩家 tick / 登录登出 / 爆炸拦截 / 爆炸批处理（F-15 ~ F-17）。
        FarmstormGameEvents.register(NeoForge.EVENT_BUS);

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
        boolean taxonomyOk = CropProfile.ALL.size() == 19
                && throwable == 10 && deployable == 6 && edible == 3 && demolition == 4;
        LOGGER.info("自检 作物 {} 种：投掷型 {} / 部署型 {} / 食用型 {}；破障类 {} → {}",
                CropProfile.ALL.size(), throwable, deployable, edible, demolition, taxonomyOk ? "OK" : "FAIL");

        // D-55 + D-56：池从「ALL 减幸运浆果」的自动推导改成显式排除三件，19 - 3 = 16（TECH §11 #3）。
        int poolSize = CropProfile.LUCKY_BERRY_POOL.size();
        boolean poolOk = poolSize == 16 && CropProfile.LUCKY_BERRY_POOL.stream().noneMatch(profile ->
                profile == CropProfile.LUCKY_BERRY || profile == CropProfile.WILD_APPLE
                        || profile == CropProfile.COMPRESSED_PUFFED_WHEAT);
        LOGGER.info("自检 幸运浆果池 {} 种（排除 lucky_berry / wild_apple / compressed_puffed_wheat）→ {}",
                poolSize, poolOk ? "OK" : "FAIL");
    }

    /**
     * 注册对象自检（F-66 / TECH §11 #2）：64 → 77 的那 12 类**当场数**（各 DeferredRegister 的条目数），
     * 相加不等于 77、或任何一类的条目数与 §2.1 的小计不符，就打 FAIL——不写死常量再自己跟自己比。
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
        int dataComponents = FarmstormDataComponents.REGISTER.getEntries().size();
        int menuTypes = FarmstormMenus.REGISTER.getEntries().size();
        int total = items + blocks + blockEntities + entityTypes + mobEffects + soundEvents + recipeTypes
                + recipeSerializers + creativeTabs + damageTypes + dataComponents + menuTypes;
        boolean ok = total == 77
                && items == 27 && blocks == 22 && blockEntities == 6 && entityTypes == 11
                && mobEffects == 2 && soundEvents == 2 && recipeTypes == 1 && recipeSerializers == 1
                && creativeTabs == 1 && damageTypes == 2 && dataComponents == 1 && menuTypes == 1;
        LOGGER.info("自检 注册对象 {} 个：items {} / blocks {} / block_entities {} / entity_types {} / mob_effects {} / "
                        + "sound_events {} / recipe_types {} / recipe_serializers {} / creative_tabs {} / damage_types {} / "
                        + "data_components {} / menu_types {} → {}",
                total, items, blocks, blockEntities, entityTypes, mobEffects, soundEvents, recipeTypes,
                recipeSerializers, creativeTabs, damageTypes, dataComponents, menuTypes, ok ? "OK" : "FAIL");
    }

    /**
     * 数据驱动的伤害类型没有 DeferredRegister，只能把声明出来的 ResourceKey 逐个列出来数。
     * <p>新增伤害类型时**必须**同时加进这个列表：总数 77 的断言会因此失败，不会静默漂移。
     * 不用反射，是因为 {@code getDeclaredFields()} 依赖字段形状，改名或换个同类型常量都会悄悄改变结果。
     */
    private static int damageTypeCount() {
        return List.of(FarmstormDamageTypes.CROP_BLAST, FarmstormDamageTypes.VOID_BOLT).size();
    }

    /** 1.21 起 ResourceLocation 构造器私有，统一走这个入口（TECH §4.1）。 */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
