package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.behavior.DeployedKind;
import com.dashpig.farmstorm.blockentity.ConverterBlockEntity;
import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.blockentity.OutpostCoreBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 7 个 BlockEntityType（TECH §3.3）：v0.0.4 的 6 个 + v0.1.0 的 1 个据点核心（D-119）。
 * 部署物的 5 个类型都用同一个 DeployedCropBlockEntity 类，靠 BlockEntityType 区分（RT-04）。
 * v0.0.3 的两个新部署方块挂进既有 type 的合法方块集合，type 总数当时仍是 6（§2.2 注 3）。
 */
public final class FarmstormBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Farmstorm.MOD_ID);

    /** 一个 type 收两个方块：BLAST_CARROT 与 v0.0.3 的近防菌共用它（§2.2 注 3，仍是 6 个 type）。 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeployedCropBlockEntity>> BLAST_CARROT =
            REGISTER.register("blast_carrot", () -> BlockEntityType.Builder
                    .of(DeployedCropBlockEntity::new, FarmstormBlocks.BLAST_CARROT.get(),
                            FarmstormBlocks.POINT_DEFENSE_FUNGUS.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeployedCropBlockEntity>> PUMPKIN_TURRET =
            REGISTER.register("pumpkin_turret", () -> BlockEntityType.Builder
                    .of(DeployedCropBlockEntity::new, FarmstormBlocks.PUMPKIN_TURRET.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeployedCropBlockEntity>> AWAKENED_TORCHFLOWER =
            REGISTER.register("awakened_torchflower", () -> BlockEntityType.Builder
                    .of(DeployedCropBlockEntity::new, FarmstormBlocks.AWAKENED_TORCHFLOWER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeployedCropBlockEntity>> AWAKENED_PITCHER_PLANT =
            REGISTER.register("awakened_pitcher_plant", () -> BlockEntityType.Builder
                    .of(DeployedCropBlockEntity::new, FarmstormBlocks.AWAKENED_PITCHER_PLANT.get()).build(null));
    /** 一个 type 收三个方块：ZANGIEF_CANE、FANG_TRAP_CACTUS 与 v0.0.3 的震动豆共用它（代码侧靠 {@link #deployedOf} 分流）。 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeployedCropBlockEntity>> FANG_TRAP_CACTUS =
            REGISTER.register("fang_trap_cactus", () -> BlockEntityType.Builder
                    .of(DeployedCropBlockEntity::new, FarmstormBlocks.FANG_TRAP_CACTUS.get(),
                            FarmstormBlocks.ZANGIEF_CANE.get(), FarmstormBlocks.VIBRATION_BEAN.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConverterBlockEntity>> CONVERTER =
            REGISTER.register("converter", () -> BlockEntityType.Builder
                    .of(ConverterBlockEntity::new, FarmstormBlocks.CONVERTER.get()).build(null));
    /**
     * v0.1.0 的第 7 个 type（D-119；24 号 §2.2 注 1）：据点核心既不是部署方块、也没有 DeployedKind，
     * 挂进既有 6 个里的任何一个都会在方块实体构造期崩，因此这 +1 是那 +1 个方块的必然结果。
     * 单方块例，与 converter 同形。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OutpostCoreBlockEntity>> OUTPOST_CORE =
            REGISTER.register("outpost_core", () -> BlockEntityType.Builder
                    .of(OutpostCoreBlockEntity::new, FarmstormBlocks.OUTPOST_CORE.get()).build(null));

    private FarmstormBlockEntities() {}

    /** 部署方块按 kind 取自己的 BlockEntityType：5 个类型共用一个 BE 类，构造期必须知道自己是哪一个（RT-04）。 */
    public static BlockEntityType<DeployedCropBlockEntity> deployedOf(DeployedKind kind) {
        return switch (kind) {
            case BLAST_CARROT -> BLAST_CARROT.get();
            case PUMPKIN_TURRET -> PUMPKIN_TURRET.get();
            case AWAKENED_TORCHFLOWER -> AWAKENED_TORCHFLOWER.get();
            case AWAKENED_PITCHER_PLANT -> AWAKENED_PITCHER_PLANT.get();
            case FANG_TRAP_CACTUS -> FANG_TRAP_CACTUS.get();
            case ZANGIEF_CANE -> FANG_TRAP_CACTUS.get();        // 复用既有 type：block_entities 仍 6
            case POINT_DEFENSE_FUNGUS -> BLAST_CARROT.get();     // 同上：不为新方块新建 type（§2.2 注 3）
            case VIBRATION_BEAN -> FANG_TRAP_CACTUS.get();       // 同上
        };
    }
}
