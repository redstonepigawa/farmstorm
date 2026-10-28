package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

/**
 * 2 个伤害类型条目（TECH §6.7 / §3.7）与 2 条并入 minecraft 的伤害类型标签。
 * 伤害类型是数据包注册表条目，没有 DeferredRegister（R-02），因此 JSON 由
 * {@link #REGISTRY_BUILDER} 交给 DatapackBuiltinEntriesProvider 生成（R-03）。
 */
public final class ModDamageTypeProvider extends DamageTypeTagsProvider {
    /** 交给 DatapackBuiltinEntriesProvider 的注册表 bootstrap。 */
    public static final RegistrySetBuilder REGISTRY_BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypeProvider::bootstrap);

    /**
     * @param lookupProvider 必须包含本 mod 的伤害类型（用 DatapackBuiltinEntriesProvider#getRegistryProvider），
     *                       否则标签条目会被当成缺失引用
     */
    public ModDamageTypeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        // 标签引用的是注册表条目而不是资源文件，故不传 ExistingFileHelper
        super(output, lookupProvider, Farmstorm.MOD_ID, null);
    }

    private static void bootstrap(BootstrapContext<DamageType> context) {
        // 参数顺序照 DamageType 的 record 分量：message_id、scaling、exhaustion、effects（§3.7）
        context.register(FarmstormDamageTypes.VOID_BOLT, new DamageType("farmstorm.void_bolt",
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F, DamageEffects.HURT));
        context.register(FarmstormDamageTypes.CROP_BLAST, new DamageType("farmstorm.crop_blast",
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.HURT));
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        // 爆炸保护 / 抗性附魔据此生效（§3.7）
        tag(DamageTypeTags.IS_PROJECTILE).add(FarmstormDamageTypes.VOID_BOLT);
        tag(DamageTypeTags.IS_EXPLOSION).add(FarmstormDamageTypes.CROP_BLAST);
    }
}
