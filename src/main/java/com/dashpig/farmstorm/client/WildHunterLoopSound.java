package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.registry.FarmstormMobEffects;
import com.dashpig.farmstorm.registry.FarmstormSounds;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * F-84 的循环音（D-76 / D-53）：客户端 {@code SoundInstance}，{@code isLooping() = true}，
 * 每 tick 把位置更新到「带着效果的玩家」身上，可听距离读 {@link WildHunterClientState#loopRange()}（16 格）。
 * <p>为什么不在服务端每刻重发包：效果的存在本身就已经告诉每个客户端「该播」——原版
 * {@code ClientboundUpdateMobEffectPacket} 会把效果同步给所有追踪该玩家的客户端，
 * 所以别人（B）也能听到 A 的循环音，而服务端一包都不用发。
 * <p>分类是 {@link SoundSource#PLAYERS} 不是 MUSIC（D-76），因此不会切断原版背景音乐。
 */
public final class WildHunterLoopSound extends AbstractTickableSoundInstance {
    private static final Map<UUID, WildHunterLoopSound> ACTIVE = new Object2ObjectOpenHashMap<>();

    private final Player owner;
    /** 自己的实例（relative=true）不需要也不应该 follow：相对声源的位置没有意义。 */
    private boolean selfSourced;

    private WildHunterLoopSound(Player owner) {
        super(FarmstormSounds.WILD_HUNTER_LOOP.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.owner = owner;
        this.looping = true;
        // 自己的光环用相对听者定位：声源不随世界移动 → 根本没有「听者→声源」向量，也就没有左右声像抖动。
        // 别人的光环保持世界坐标 + LINEAR 衰减，这样才听得出方向（sounds.json 已声明 attenuation_distance: 16）。
        boolean self = owner == Minecraft.getInstance().player;
        this.relative = self;
        this.selfSourced = self;
        Farmstorm.LOGGER.info("狂野猎人 循环音 实例 {} relative={}", owner.getScoreboardName(), self);
        this.volume = 1.0F;
        this.pitch = 1.0F;
        if (self) {
            // relative 声源的坐标是**相对听者的偏移**，不是世界坐标：必须归零，
            // 且不能再叠 LINEAR 衰减，否则源被丢到几百格外 → 增益归零 → 完全没声音。
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.x = 0.0;
            this.y = 0.0;
            this.z = 0.0;
        } else {
            // 别人的光环：世界坐标 + 引擎按 sounds.json 的 attenuation_distance: 16 衰减。
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            syncPosition();
        }
    }

    /** 每客户端刻：给效果在身的玩家各维持一条实例，并按 16 格衰减算音量。 */
    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player local = minecraft.player;
        if (level == null || local == null) {
            clear();
            return;
        }
        for (Player player : level.players()) {
            if (!player.hasEffect(FarmstormMobEffects.WILD_HUNTER)) {
                stopFor(player);
                continue;
            }
            WildHunterLoopSound instance = ACTIVE.get(player.getUUID());
            if (instance == null) {
                instance = new WildHunterLoopSound(player);
                ACTIVE.put(player.getUUID(), instance);
                minecraft.getSoundManager().play(instance);
                Farmstorm.LOGGER.info("狂野猎人 循环音 起 {}", player.getScoreboardName());
            }
            instance.follow();
        }
        Iterator<Map.Entry<UUID, WildHunterLoopSound>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, WildHunterLoopSound> entry = iterator.next();
            Player owner = level.getPlayerByUUID(entry.getKey());
            if (owner == null || !owner.hasEffect(FarmstormMobEffects.WILD_HUNTER)) {
                Minecraft.getInstance().getSoundManager().stop(entry.getValue());
                Farmstorm.LOGGER.info("狂野猎人 循环音 停 {}", entry.getValue().owner.getScoreboardName());
                iterator.remove();
            }
        }
    }

    /** 死亡 / 换维度 / 退出世界：全部停掉，不留残留循环音。 */
    public static void clear() {
        for (WildHunterLoopSound instance : ACTIVE.values()) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
        ACTIVE.clear();
    }

    private static void stopFor(Player player) {
        WildHunterLoopSound instance = ACTIVE.remove(player.getUUID());
        if (instance != null) {
            Farmstorm.LOGGER.info("狂野猎人 循环音 停 {}", player.getScoreboardName());
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
    }

    /** 只在移动超过 0.1 格时才写源坐标，减少每刻 AL_POSITION 抖动。 */
    private void follow() {
        if (selfSourced) {
            return;
        }
        double dx = owner.getX() - this.x;
        double dy = owner.getEyeY() - this.y;
        double dz = owner.getZ() - this.z;
        if (dx * dx + dy * dy + dz * dz < 0.01) {
            return;
        }
        syncPosition();
    }

    private void syncPosition() {
        this.x = owner.getX();
        this.y = owner.getEyeY();
        this.z = owner.getZ();
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
