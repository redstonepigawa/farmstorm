package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * 1 个 {@code VillagerProfession}（D-196 / 48 号 §2.3.4、§2.3.6）：本模组第一个职业注册器，全仓此前 0 处。
 * <p><b>口径外第 2 类</b>：职业走原版注册表 {@code Registries.VILLAGER_PROFESSION}（{@code Registries:198}），
 * 不属于 98 的那 12 类 ⇒ 注册面仍是 98，{@code Farmstorm#logRegistrySelfCheck} 的 12 类断言与
 * {@code tools/verify_project.py} 的期望面都不因本类改动一个字。
 * <p><b>注册顺序</b>：{@code Farmstorm} 构造器里 {@code FarmstormPoiTypes.REGISTER} 必须排在本类 {@code REGISTER} 之前
 * （48 号 §2.3.6）——本类的两个谓词引用的 {@code ResourceKey} 由那一类持有。
 */
public final class FarmstormVillagerProfessions {
    public static final DeferredRegister<VillagerProfession> REGISTER =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, Farmstorm.MOD_ID);

    /**
     * 植物学家（D-196）：六字段按 48 号 §2.3.4 定死。
     * <p>① {@code name = "botanist"} 只用于 {@code toString()}（{@code VillagerProfession:52-55}）；玩家看到的职业名走语言键
     * {@code entity.minecraft.villager.farmstorm.botanist}，由 {@code Villager#getTypeName}（{@code Villager:743-749}）按注册表 key 拼，
     * 与本字段无关。
     * <p>② ③ 两个谓词<b>只认</b> {@code FarmstormPoiTypes.BOTANIST_KEY}，形态抄原版（{@code VillagerProfession:58}，两个谓词传同一个 key）：
     * <b>不按方块、不按 {@code ownerUuid}、不按距离</b>（裁定 #8 / RT-65：有主核也能当工作台）。加「必须无主 / 必须己方」的判据
     * 会让把核放在基地的玩家<b>永远</b>招不到植物学家；写 {@code VillagerProfession.ALL_ACQUIRABLE_JOBS} 则会把原版 13 种工作站全变成它的候选。
     * <p>④ ⑤ {@code requestedItems} / {@code secondaryPoi} 都是空集：前者影响 {@code Villager#wantsToPickUp}（{@code Villager:839}），
     * 空集 = 不捡地上的东西；后者只有农民用（{@code Villager:838} 的 {@code VILLAGER_FARMLANDS} 判定），植物学家没有副工作点。
     * <p>⑥ {@code workSound = null}：本版<b>不新增音效</b>（{@code sound_events} 仍 3）；{@code null} 是安全的——
     * {@code Villager#playWorkSound}（{@code :582-584}）把它交给 {@code LivingEntity#makeSound(@Nullable SoundEvent)}，为 null 时不响。
     * 借原版农民音效会让玩家以为它是农民。
     */
    public static final RegistryObject<VillagerProfession> BOTANIST =
            REGISTER.register("botanist", () -> new VillagerProfession(
                    "botanist",
                    holder -> holder.is(FarmstormPoiTypes.BOTANIST_KEY),
                    holder -> holder.is(FarmstormPoiTypes.BOTANIST_KEY),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null));

    private FarmstormVillagerProfessions() {}
}
