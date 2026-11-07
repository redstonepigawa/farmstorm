package com.dashpig.farmstorm.datagen;

import com.dashpig.farmstorm.Farmstorm;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;

/**
 * DataGen 汇总入口（TECH §2 datagen 分包）。
 * 数据一律由 Provider 生成（N-10 / §4.7），不手写同名 JSON。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class FarmstormDataGen {
    private FarmstormDataGen() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // 伤害类型是注册表条目：JSON 由 DatapackBuiltinEntriesProvider 落成（R-03）
        DatapackBuiltinEntriesProvider datapackEntries = new DatapackBuiltinEntriesProvider(
                output, lookupProvider, ModDamageTypeProvider.REGISTRY_BUILDER, Set.of(Farmstorm.MOD_ID));
        generator.addProvider(event.includeServer(), datapackEntries);
        // 标签必须能查到刚生成的伤害类型，因此用带补丁的 lookup 构造该 Provider
        generator.addProvider(event.includeServer(),
                new ModDamageTypeProvider(output, datapackEntries.getRegistryProvider()));

        ModBlockTagsProvider blockTags = new ModBlockTagsProvider(output, lookupProvider);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(output, lookupProvider, blockTags));
        // D-196 / 48 号 §2.3.5：POI 要进 minecraft:acquirable_job_site，否则村民永不认领（RT-64），且不报错；
        // 不挂这行 Provider 永不被调度，产物不会出现（48 号 §3 #21）。
        generator.addProvider(event.includeServer(), new ModPoiTypeTagsProvider(output, lookupProvider));

        // 方块模型必须先于物品模型生成：物品模型的 parent 指向刚生成的方块模型，
        // Provider 按注册顺序串行执行，顺序反了会触发「Model does not exist」。
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, event.getExistingFileHelper()));

        // ---- M5-2：转换配方 / 战利品表 / 进度 / 语言 ----
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
        generator.addProvider(event.includeServer(), ModLootTableProvider.create(output));
        // 1.20.1 的 ForgeAdvancementProvider 必须拿到 ExistingFileHelper（1.21 的 AdvancementProvider 不需要）
        generator.addProvider(event.includeServer(),
                ModAdvancementProvider.create(output, lookupProvider, event.getExistingFileHelper()));
        // 两份语言文件由同一张条目表按列写出，键集天然一致（TECH §7：216 口径见 ModLangProvider 类注释）
        generator.addProvider(event.includeClient(), new ModLangProvider(output, true));
        generator.addProvider(event.includeClient(), new ModLangProvider(output, false));
    }
}
