package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * F-84 的循环音（D-76 / D-53）：{@code isLooping() = true} 的客户端 {@link SoundInstance}，
 * <b>只服务本地玩家本人</b>。
 *
 * <p><b>别人的循环音不走这里</b>：由服务端在 {@code combat/WildHunterAura#broadcastLoop} 里用原版
 * {@code ClientboundSoundEntityPacket}（**绑定实体**，声音跟着持有者走）广播给附近的其它玩家（V-85）。
 * <p>曾经这里还有一条「给其他带效果的玩家也建实例」的分支，但它依赖「客户端能看到别人身上的效果」——
 * 前提不成立：{@code ClientboundUpdateMobEffectPacket} 只发给持有者本人，B 侧 {@code A.hasEffect()} 恒为 false，
 * 那条分支一次都不会执行（留着只会误导下一个人，并可能在将来效果同步补上后与服务端广播叠成两层）。
 *
 * <p>本人的声像为什么用 {@code relative = true}：相对声源没有世界位置，也就没有「听者→声源」向量，
 * 从根上消除左右声像抖动（V-53 的成果，别改回世界坐标）。
 * <p>分类是 {@link SoundSource#PLAYERS} 不是 MUSIC（D-76），因此不会切断原版背景音乐。
 */
public final class WildHunterLoopSound extends AbstractTickableSoundInstance {
    private static WildHunterLoopSound instance;

    private final Player owner;

    private WildHunterLoopSound(Player owner) {
        super(FarmstormSounds.WILD_HUNTER_LOOP.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.owner = owner;
        this.looping = true;
        // 相对听者定位：声源不随世界移动 ⇒ 没有「听者→声源」向量 ⇒ 没有左右声像抖动（V-53）。
        this.relative = true;
        // relative 声源的坐标是**相对听者的偏移**，不是世界坐标：必须归零，
        // 且不能再叠 LINEAR 衰减，否则源被丢到几百格外 → 增益归零 → 完全没声音。
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        Farmstorm.LOGGER.info("狂野猎人 循环音 实例 {} relative=true", owner.getScoreboardName());
    }

    /** 每客户端刻：本地玩家带效果就维持一条实例，效果没了就停。 */
    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        Player local = minecraft.player;
        if (local == null || minecraft.level == null) {
            clear();
            return;
        }
        if (!local.hasEffect(FarmstormMobEffects.WILD_HUNTER)) {
            clear();
            return;
        }
        if (instance == null) {
            instance = new WildHunterLoopSound(local);
            minecraft.getSoundManager().play(instance);
            Farmstorm.LOGGER.info("狂野猎人 循环音 起 {}", local.getScoreboardName());
        }
    }

    /** 死亡 / 换维度 / 退出世界 / 效果结束：停掉，不留残留循环音。 */
    public static void clear() {
        if (instance == null) {
            return;
        }
        Minecraft.getInstance().getSoundManager().stop(instance);
        Farmstorm.LOGGER.info("狂野猎人 循环音 停 {}", instance.owner.getScoreboardName());
        instance = null;
    }

    @Override
    public void tick() {
        if (!owner.isAlive() || !owner.hasEffect(FarmstormMobEffects.WILD_HUNTER)) {
            stop();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
