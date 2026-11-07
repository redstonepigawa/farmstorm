package com.dashpig.farmstorm.util;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.team.TeamResolver;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * 部署物归属的读写（C-9 / HC-08）。
 * 这里只有 ownerUuid + ownerName 两个字段：队伍信息一律不落盘，判定时实时解析。
 * 归属相关代码全部走这里，是为了让「什么时候读盘、什么时候查队伍」只有一处实现。
 */
public final class DeployedOwnership {
    /** NBT 键名（本模组自有格式）。 */
    public static final String TAG_OWNER_UUID = "ownerUuid";
    public static final String TAG_OWNER_NAME = "ownerName";

    private DeployedOwnership() {}

    /** 写入归属：只写两个键，不写任何队伍快照（C-9）。 */
    public static void write(CompoundTag tag, @Nullable UUID ownerUuid, @Nullable String ownerName) {
        if (ownerUuid != null) {
            tag.putUUID(TAG_OWNER_UUID, ownerUuid);
        } else {
            tag.remove(TAG_OWNER_UUID);
        }
        if (ownerName != null && !ownerName.isEmpty()) {
            tag.putString(TAG_OWNER_NAME, ownerName);
        } else {
            tag.remove(TAG_OWNER_NAME);
        }
    }

    @Nullable
    public static UUID readUuid(CompoundTag tag) {
        return tag.hasUUID(TAG_OWNER_UUID) ? tag.getUUID(TAG_OWNER_UUID) : null;
    }

    @Nullable
    public static String readName(CompoundTag tag) {
        return tag.contains(TAG_OWNER_NAME) ? tag.getString(TAG_OWNER_NAME) : null;
    }

    /** 归属者是否对 target 友好；无归属者（/setblock、结构生成）时对所有人都是「非己方」。 */
    public static boolean isAlly(ServerLevel level, @Nullable UUID ownerUuid, @Nullable String ownerName, Player target) {
        if (ownerUuid == null) {
            return false;
        }
        return Farmstorm.TEAMS.isAlly(level, new TeamResolver.Owner(ownerUuid, ownerName), target);
    }
}
