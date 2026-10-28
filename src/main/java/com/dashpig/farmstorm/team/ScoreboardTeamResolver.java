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

    @Override
    public boolean isAlly(ServerLevel level, Owner owner, Player target) {
        if (owner.uuid().equals(target.getUUID())) {
            return true;                                  // 部署者本人永远免疫自家陷阱
        }
        Optional<String> ownerTeamName = teamName(level, owner);
        if (ownerTeamName.isEmpty()) {
            // D-31 / G-8：无队伍时按 untamedFallback 决定口径；public 等于「谁都是己方」，
            // 只在服主显式改成 public 时成立，默认为 self（其余玩家全部敌对）。
            return FALLBACK_PUBLIC.equals(FarmstormConfig.untamedFallback());
        }
        PlayerTeam targetTeam = target.getTeam();          // 1.21：Entity#getTeam()
        return targetTeam != null && ownerTeamName.get().equals(targetTeam.getName());
    }
}
