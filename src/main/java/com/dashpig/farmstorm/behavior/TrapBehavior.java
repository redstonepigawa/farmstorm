package com.dashpig.farmstorm.behavior;

import com.dashpig.farmstorm.blockentity.DeployedCropBlockEntity;
import com.dashpig.farmstorm.config.FarmstormConfig;
import com.dashpig.farmstorm.trap.TrapImmunity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * 陷阱类行为（TECH §4.6）：一次触发即结算。
 * 触发对象是**非己方玩家与敌对生物**（D-114 修改了 D-07 的「只对玩家」）；己方判定在 findTrigger 里实时解析
 * （HC-08 / C-9），生物没有队伍归属因此恒按非己方（D-114 第 4 条）。
 */
public interface TrapBehavior extends DeployedBehavior {

    /**
     * 触发扫描的粗筛立方：以**方块自身那一格**为基准、按同一个半径外扩（F-148 的第 2 步）。
     * <p>基准取整格而不是方块中心：切比雪夫 / 欧氏 / 球形三条判据比较的对象里，前两条用 {@code blockPosition()}、
     * 第三条用实体位置，而实体可以站在这一格靠外的那半边，整格基准才能把它们全包进来（复核仍由各自的 metric 做）。
     */
    static AABB searchBox(BlockPos pos, double radius) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0).inflate(radius);
    }

    /** dust 的取值（DESIGN §7.4）：中性土色、scale 1.0、每次 2 个。 */
    Vector3f DUST_COLOR = new Vector3f(0.62F, 0.56F, 0.44F);
    float DUST_SCALE = 1.0F;
    int DUST_COUNT = 2;
    /** 贴地但不嵌进方块：y 抬 0.15，平面散布 0.25、竖向 0.10，速度 0（静止扩散）。 */
    double DUST_Y_OFFSET = 0.15;
    double DUST_XZ_SPREAD = 0.25;
    double DUST_Y_SPREAD = 0.10;

    @Override
    default int scanInterval() {
        return 10;
    }

    Optional<LivingEntity> findTrigger(DeployedCropBlockEntity be);

    void trigger(DeployedCropBlockEntity be, LivingEntity target);

    /**
     * 扫描入口的三步固定为 {@code findTrigger → 免疫闸 → trigger}（D-112 第 2 条）：
     * 被闸住时本次扫描直接跳过，不 consume、不施效果、不写免疫。
     *
     * <p>dust 排在免疫闸**之前**：粒子是「这里有个东西」的提示，与有没有人踩无关（D-113 / TECH §5.2）。
     * 本默认实现被四个陷阱共用，{@code ZangiefCaneTrap} 覆盖的 {@code tick} 也会先调 {@code super.tick}，
     * 因此免疫闸与 dust 是「一处写、四处生效」。
     */
    @Override
    default void tick(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        emitDust(be);
        findTrigger(be)
                .filter(target -> TrapImmunity.passGate(level, target))
                .ifPresent(target -> trigger(be, target));
    }

    /**
     * 部署态每 {@code trap.dustIntervalTicks}（默认 100 刻 = 5 秒）在自身位置撒 2 个 {@code minecraft:dust}（D-113）。
     *
     * <p>相位复用既有错峰的同一算式 {@code floorMod(pos.hashCode(), interval)}：因为 100 是主扫描间隔 10 的整数倍，
     * 且 {@code phase100 mod 10 = stagger10}，「{@code (now + phase100) % 100 == 0}」成立的那一 tick
     * 必然同时满足主扫描的 10 刻门，dust 不会被错峰跳过（TECH §5.2 的相位行）。
     *
     * <p>只有实现本接口的 4 个陷阱 kind 会走到这里；炮台走 {@link TurretBehavior}、光环走 {@link AuraProvider}，
     * 是平行的另外两个接口，天然不会被发到（D-113 第 4 条）。广播用原版 {@code ClientboundLevelParticlesPacket}
     * 的 32 格口径，不新增自定义网络包（C-6 / D-50）。
     */
    default void emitDust(DeployedCropBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level)) {
            return;
        }
        int interval = FarmstormConfig.trapDustIntervalTicks.get();
        if (interval <= 0) {
            return;                                          // 防 floorMod 除零；config 范围 20~1200 不会取到 0
        }
        BlockPos pos = be.getBlockPos();
        long now = level.getGameTime();
        if (Math.floorMod(now + Math.floorMod(pos.hashCode(), interval), interval) != 0) {
            return;
        }
        level.sendParticles(new DustParticleOptions(DUST_COLOR, DUST_SCALE),
                pos.getX() + 0.5, pos.getY() + DUST_Y_OFFSET, pos.getZ() + 0.5,
                DUST_COUNT, DUST_XZ_SPREAD, DUST_Y_SPREAD, DUST_XZ_SPREAD, 0.0);
    }
}
