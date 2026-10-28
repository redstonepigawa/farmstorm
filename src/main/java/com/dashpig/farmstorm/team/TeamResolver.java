package com.dashpig.farmstorm.team;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * 己方判定抽象（D-06）。v0.0.1 用原版计分板队伍实现，后续换 FTB Teams 时不动任何调用点。
 * C-9：部署物只记身份、不记队伍；每次触发 / 索敌的瞬间都实时解析归属者的当前队伍。
 */
public interface TeamResolver {

    /** 归属身份：UUID 必填；name 仅用于离线时在 Scoreboard 成员表反查（可为 null）。不含任何队伍信息。 */
    record Owner(UUID uuid, @Nullable String name) {}

    /** 己方判定：在触发瞬间实时解析 owner 的当前队伍，再与 target 的当前队伍比对。 */
    boolean isAlly(ServerLevel level, Owner owner, Player target);

    /** 实时队伍名（在线 / 离线都可用）；无队伍返回 empty。 */
    Optional<String> teamName(ServerLevel level, Owner owner);
}
