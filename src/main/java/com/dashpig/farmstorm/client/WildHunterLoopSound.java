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

    private WildHunterLoopSound(Player owner) {
        super(FarmstormSounds.WILD_HUNTER_LOOP.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.owner = owner;
        this.looping = true;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        follow();
        updateVolume();
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
            instance.updateVolume();
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

    private void follow() {
        this.x = owner.getX();
        this.y = owner.getEyeY();
        this.z = owner.getZ();
    }

    private void updateVolume() {
        Player local = Minecraft.getInstance().player;
        if (local == null) {
            volume = 0.0F;
            return;
        }
        double distance = Math.sqrt(local.distanceToSqr(owner.getX(), owner.getEyeY(), owner.getZ()));
        volume = (float) Mth.clamp(1.0 - distance / WildHunterClientState.loopRange(), 0.0, 1.0);
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
