package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormPoiTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

/**
 * 把 {@code farmstorm:botanist} 补进原版标签 {@code minecraft:acquirable_job_site}（D-196 / 48 号 §2.3.5；RT-64 的主形态）。
 * <p>这条标签不加<b>不会报错</b>，只会让村民<b>永不认领</b>：无业村民的 {@code POTENTIAL_JOB_SITE} 谓词是
 * {@code VillagerProfession.ALL_ACQUIRABLE_JOBS}，而它就等于这张标签（48 号 §2.3.5 第 1 ~ 3 步的代码路径）。
 * <p>因此直接继承抽象 {@link TagsProvider}，<b>不</b>继承原版 {@code PoiTypeTagsProvider}——后者的 {@code addTags}
 * 会连带重写 {@code minecraft:village} 与 {@code minecraft:bee_home}，那两条与认领无关、不该由我们写。
 * <p>命名空间是 {@code minecraft}（我们只<b>补</b>一条），产物落在
 * {@code data/minecraft/tags/point_of_interest_type/acquirable_job_site.json}；写成 {@code data/farmstorm/...} 等于没加。
 */
public final class ModPoiTypeTagsProvider extends TagsProvider<PoiType> {
    public ModPoiTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        // 标签引用的是注册表条目而不是资源文件，故与 ModBlockTagsProvider 同形：第四个参数之后不传 ExistingFileHelper
        super(output, Registries.POINT_OF_INTEREST_TYPE, lookupProvider, Farmstorm.MOD_ID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        // §0.5 死命令 2：POI 那条标签用原版的 PoiTypeTags，不动 FarmstormTags（后者仍是 7 个 key）
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(FarmstormPoiTypes.BOTANIST_KEY);
    }
}
