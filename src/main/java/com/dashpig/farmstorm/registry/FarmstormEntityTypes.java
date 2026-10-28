package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.entity.TurretBullet;
import com.dashpig.farmstorm.entity.thrown.BambooFirecrackerProjectile;
import com.dashpig.farmstorm.entity.thrown.BrownMushroomPuffProjectile;
import com.dashpig.farmstorm.entity.thrown.MelonGrenadeProjectile;
import com.dashpig.farmstorm.entity.thrown.MelonSliceProjectile;
import com.dashpig.farmstorm.entity.thrown.PuffedWheatProjectile;
import com.dashpig.farmstorm.entity.thrown.RedMushroomPuffProjectile;
import com.dashpig.farmstorm.entity.thrown.ToxicUbiServerProjectile;
import com.dashpig.farmstorm.entity.thrown.UbiServerProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 9 个实体类型（TECH §3.4）：8 个投掷物 + 1 个炮台导弹。
 * 全部 MISC、投影 0.25 格（导弹 0.3）、tracking 4 格、每 10 tick 同步一次。
 */
public final class FarmstormEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTER =
            DeferredRegister.create(Registries.ENTITY_TYPE, Farmstorm.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<PuffedWheatProjectile>> PUFFED_WHEAT =
            projectile("puffed_wheat", PuffedWheatProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<UbiServerProjectile>> UBI_SERVER =
            projectile("ubi_server", UbiServerProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ToxicUbiServerProjectile>> TOXIC_UBI_SERVER =
            projectile("toxic_ubi_server", ToxicUbiServerProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<MelonSliceProjectile>> MELON_SLICE_BOMB =
            projectile("melon_slice_bomb", MelonSliceProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<MelonGrenadeProjectile>> MELON_GRENADE =
            projectile("melon_grenade", MelonGrenadeProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BambooFirecrackerProjectile>> BAMBOO_FIRECRACKER =
            projectile("bamboo_firecracker", BambooFirecrackerProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<RedMushroomPuffProjectile>> RED_MUSHROOM_PUFF =
            projectile("red_mushroom_puff", RedMushroomPuffProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BrownMushroomPuffProjectile>> BROWN_MUSHROOM_PUFF =
            projectile("brown_mushroom_puff", BrownMushroomPuffProjectile::new);

    public static final DeferredHolder<EntityType<?>, EntityType<TurretBullet>> TURRET_BULLET =
            REGISTER.register("turret_bullet", () -> EntityType.Builder
                    .<TurretBullet>of(TurretBullet::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(Farmstorm.id("turret_bullet").toString()));

    private FarmstormEntityTypes() {}

    private static <T extends com.dashpig.farmstorm.entity.ThrownCropEntity> DeferredHolder<EntityType<?>, EntityType<T>> projectile(
            String id, EntityType.EntityFactory<T> factory) {
        return REGISTER.register(id, () -> EntityType.Builder
                .of(factory, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(Farmstorm.id(id).toString()));
    }
}
