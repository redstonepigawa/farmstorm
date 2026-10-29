package com.dashpig.farmstorm.registry;

import com.dashpig.farmstorm.Farmstorm;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 2 个 sound_events（TECH §2.2 #12~#13、§3.4），对应 {@code assets/farmstorm/sounds.json} 的两个事件。
 * 两个都用 {@code createVariableRangeEvent}：可听距离要由 sounds.json 的
 * {@code attenuation_distance}（16 格，D-76）与客户端 {@code SoundInstance} 一起决定，
 * 固定距离事件会把半径钉死在服务端。
 */
public final class FarmstormSounds {
    public static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(Registries.SOUND_EVENT, Farmstorm.MOD_ID);

    /** D-76：狂野猎人循环音，可听距离 16 格由客户端 SoundInstance 与 sounds.json 共同决定。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> WILD_HUNTER_LOOP =
            REGISTER.register("wild_hunter_loop",
                    () -> SoundEvent.createVariableRangeEvent(Farmstorm.id("wild_hunter_loop")));

    /** D-66：桑吉尔蔗触发音，单次播放。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> ZANGIEF_PILEDRIVER =
            REGISTER.register("zangief_piledriver",
                    () -> SoundEvent.createVariableRangeEvent(Farmstorm.id("zangief_piledriver")));

    private FarmstormSounds() {}
}
