package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * 1 个 job site（D-196 / 48 号 §2.3.6）：本模组第一个 {@code PoiType} 注册器，全仓此前 0 处。
 * <p><b>口径外第 3 类</b>：{@code PoiType} 走原版注册表 {@code Registries.POINT_OF_INTEREST_TYPE}（{@code Registries:177}），
 * 不属于 98 的那 12 类 ⇒ 注册面仍是 98，{@code Farmstorm#logRegistrySelfCheck} 的 12 类断言与
 * {@code tools/verify_project.py} 的期望面都不因本类改动一个字。
 */
public final class FarmstormPoiTypes {
    public static final DeferredRegister<PoiType> REGISTER =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Farmstorm.MOD_ID);

    /** POI 与职业同名（48 号 §0.3 第 1 项的原版惯例：{@code minecraft:farmer} 既是 POI 也是职业）；供职业谓词与标签 provider 引用。 */
    public static final ResourceKey<PoiType> BOTANIST_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Farmstorm.id("botanist"));

    /**
     * 职业方块 = 据点核心（D-196 / 48 号 §2.3.2、§2.3.3）。
     * <p>匹配集是 {@code defaultBlockState()} 的<b>单例</b>：{@code OutpostCoreBlock} 没有 {@code createBlockStateDefinition}、
     * 没有任何 {@code Property} ⇒ 该方块只有 1 个 state（全部 blockstate 就是它）；
     * 且 {@code PoiStateSet} 按引用比较，必须传这个单例。
     * <p>{@code maxTickets = 1} 就是「一核一村民」（裁定 #9）的机制本体；{@code validRange = 1} 与全部原版工作站一致。
     */
    public static final RegistryObject<PoiType> BOTANIST =
            REGISTER.register("botanist",
                    () -> new PoiType(Set.of(FarmstormBlocks.OUTPOST_CORE.get().defaultBlockState()), 1, 1));

    private FarmstormPoiTypes() {}
}
