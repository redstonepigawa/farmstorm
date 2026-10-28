package com.dashpig.farmstorm;

import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.event.FarmstormGameEvents;
import com.dashpig.farmstorm.registry.FarmstormBlockEntities;
import com.dashpig.farmstorm.registry.FarmstormBlocks;
import com.dashpig.farmstorm.registry.FarmstormCreativeTabs;
import com.dashpig.farmstorm.registry.FarmstormDataComponents;
import com.dashpig.farmstorm.registry.FarmstormEntityTypes;
import com.dashpig.farmstorm.registry.FarmstormItems;
import com.dashpig.farmstorm.registry.FarmstormMenus;
import com.dashpig.farmstorm.registry.FarmstormRecipes;
import com.dashpig.farmstorm.team.ScoreboardTeamResolver;
import com.dashpig.farmstorm.team.TeamResolver;
import com.dashpig.farmstorm.util.CropProfile;
import com.dashpig.farmstorm.util.DistanceMetrics;
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

        container.registerConfig(ModConfig.Type.SERVER, FarmstormConfig.SPEC);

        // 配置加载 / 重载后重建派生缓存（幸运浆果千分比表、爆炸阈值快照）。
        modBus.addListener(FarmstormConfig::onLoading);
        modBus.addListener(FarmstormConfig::onReloading);

        // game 总线：玩家 tick / 登录登出 / 爆炸拦截 / 爆炸批处理（F-15 ~ F-17）。
        FarmstormGameEvents.register(NeoForge.EVENT_BUS);

        logSelfCheck();
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
        boolean taxonomyOk = CropProfile.ALL.size() == 15
                && throwable == 8 && deployable == 5 && edible == 2 && demolition == 4;
        LOGGER.info("自检 作物 {} 种：投掷型 {} / 部署型 {} / 食用型 {}；破障类 {} → {}",
                CropProfile.ALL.size(), throwable, deployable, edible, demolition, taxonomyOk ? "OK" : "FAIL");
    }

    /** 1.21 起 ResourceLocation 构造器私有，统一走这个入口（TECH §4.1）。 */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
