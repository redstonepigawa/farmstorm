package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.entity.TurretBullet;
import com.dashpig.farmstorm.entity.thrown.BambooFirecrackerProjectile;
import com.dashpig.farmstorm.entity.thrown.BrownMushroomPuffProjectile;
import com.dashpig.farmstorm.entity.thrown.CompressedPuffedWheatProjectile;
import com.dashpig.farmstorm.entity.thrown.FriendChivesProjectile;
import com.dashpig.farmstorm.entity.thrown.MelonGrenadeProjectile;
import com.dashpig.farmstorm.entity.thrown.MelonSliceProjectile;
import com.dashpig.farmstorm.entity.thrown.MolotovProjectile;
import com.dashpig.farmstorm.entity.thrown.PuffedWheatProjectile;
import com.dashpig.farmstorm.entity.thrown.RedMushroomPuffProjectile;
import com.dashpig.farmstorm.entity.thrown.ToxicUbiServerProjectile;
import com.dashpig.farmstorm.entity.thrown.UbiServerProjectile;
import com.dashpig.farmstorm.entity.thrown.VibrationBeanProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * 13 个实体类型（TECH §3.4）：12 个投掷物 + 1 个炮台导弹。
 * 全部 MISC、投影 0.25 格（导弹 0.3）、tracking 4 格、每 10 tick 同步一次。
 */
public final class FarmstormEntityTypes {
    public static final DeferredRegister<EntityType<?>> REGISTER =
            DeferredRegister.create(Registries.ENTITY_TYPE, Farmstorm.MOD_ID);

    public static final RegistryObject<EntityType<PuffedWheatProjectile>> PUFFED_WHEAT =
            projectile("puffed_wheat", PuffedWheatProjectile::new);
    public static final RegistryObject<EntityType<UbiServerProjectile>> UBI_SERVER =
            projectile("ubi_server", UbiServerProjectile::new);
    public static final RegistryObject<EntityType<ToxicUbiServerProjectile>> TOXIC_UBI_SERVER =
            projectile("toxic_ubi_server", ToxicUbiServerProjectile::new);
    public static final RegistryObject<EntityType<MelonSliceProjectile>> MELON_SLICE_BOMB =
            projectile("melon_slice_bomb", MelonSliceProjectile::new);
    public static final RegistryObject<EntityType<MelonGrenadeProjectile>> MELON_GRENADE =
            projectile("melon_grenade", MelonGrenadeProjectile::new);
    public static final RegistryObject<EntityType<BambooFirecrackerProjectile>> BAMBOO_FIRECRACKER =
            projectile("bamboo_firecracker", BambooFirecrackerProjectile::new);
    public static final RegistryObject<EntityType<RedMushroomPuffProjectile>> RED_MUSHROOM_PUFF =
            projectile("red_mushroom_puff", RedMushroomPuffProjectile::new);
    public static final RegistryObject<EntityType<BrownMushroomPuffProjectile>> BROWN_MUSHROOM_PUFF =
            projectile("brown_mushroom_puff", BrownMushroomPuffProjectile::new);

    // v0.0.2 的两条（TECH §2.2 #8/#9）：参数与上面 8 个逐字一致，行为分别在 F-69 / F-68。
    public static final RegistryObject<EntityType<MolotovProjectile>> MOLOTOV =
            projectile("molotov", MolotovProjectile::new);
    public static final RegistryObject<EntityType<CompressedPuffedWheatProjectile>> COMPRESSED_PUFFED_WHEAT =
            projectile("compressed_puffed_wheat", CompressedPuffedWheatProjectile::new);

    // v0.0.3 的两条（TECH §2.2 #8/#9）：投掷参数与上面 10 个逐字一致（MISC / 0.25×0.25 / tracking 4 / interval 10）。
    public static final RegistryObject<EntityType<FriendChivesProjectile>> FRIEND_CHIVES =
            projectile("friend_chives", FriendChivesProjectile::new);
    public static final RegistryObject<EntityType<VibrationBeanProjectile>> VIBRATION_BEAN =
            projectile("vibration_bean", VibrationBeanProjectile::new);

    public static final RegistryObject<EntityType<TurretBullet>> TURRET_BULLET =
            REGISTER.register("turret_bullet", () -> EntityType.Builder
                    .<TurretBullet>of(TurretBullet::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(Farmstorm.id("turret_bullet").toString()));

    private FarmstormEntityTypes() {}

    private static <T extends com.dashpig.farmstorm.entity.ThrownCropEntity> RegistryObject<EntityType<T>> projectile(
            String id, EntityType.EntityFactory<T> factory) {
        return REGISTER.register(id, () -> EntityType.Builder
                .of(factory, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(Farmstorm.id(id).toString()));
    }
}
