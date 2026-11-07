package com.dashpig.farmstorm.team;

import com.dashpig.farmstorm.config.FarmstormConfig;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

/**
 * v0.0.1 实现：原版计分板队伍。
 * 按 ownerName 查成员表而不是按实体查，因为成员表在归属者离线时依然有效（§4.2）。
 */
public final class ScoreboardTeamResolver implements TeamResolver {

    /** teams.untamedFallback：无队伍玩家的降级口径（G-8 / D-31）。 */
    public static final String FALLBACK_SELF = "self";
    public static final String FALLBACK_PUBLIC = "public";

    @Override
    public Optional<String> teamName(ServerLevel level, Owner owner) {
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = owner.name() == null ? null : scoreboard.getPlayersTeam(owner.name());
        return team == null ? Optional.empty() : Optional.of(team.getName());
    }

    /** 纯委托（F-187）：唯一实现是下面的 Owner × Owner 重载。 */
    @Override
    public boolean isAlly(ServerLevel level, Owner owner, Player target) {
        return isAlly(level, owner, new Owner(target.getUUID(), target.getScoreboardName()));
    }

    /**
     * 己方判定的唯一实现（F-187 / D-138）：两侧都只吃 UUID + 名字，因此 {@code target} 离线也判得出。
     *
     * <p>逻辑与原 Player 版逐条等价——原版用 {@code Entity#getTeam()}，它的实现就是
     * {@code scoreboard.getPlayersTeam(getScoreboardName())}，与 {@link #teamName} 同一个查询。
     */
    @Override
    public boolean isAlly(ServerLevel level, Owner owner, Owner target) {
        if (owner.uuid().equals(target.uuid())) {
            return true;                                  // 部署者本人永远免疫自家陷阱
        }
        Optional<String> ownerTeamName = teamName(level, owner);
        if (ownerTeamName.isEmpty()) {
            // D-31 / G-8：无队伍时按 untamedFallback 决定口径；public 等于「谁都是己方」，
            // 只在服主显式改成 public 时成立，默认为 self（其余玩家全部敌对）。
            return FALLBACK_PUBLIC.equals(FarmstormConfig.untamedFallback());
        }
        Optional<String> targetTeamName = teamName(level, target);
        return targetTeamName.isPresent() && ownerTeamName.get().equals(targetTeamName.get());
    }
}
