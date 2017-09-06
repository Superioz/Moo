package de.superioz.moo.network.common;

import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.common.punishment.BanCategory;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.database.query.DbQueryNode;
import de.superioz.moo.api.database.query.DbQueryUnbaked;
import de.superioz.moo.api.redis.MooCache;
import de.superioz.moo.api.utils.CollectionUtil;
import de.superioz.moo.api.utils.PermissionUtil;
import de.superioz.moo.network.exception.MooInputException;
import de.superioz.moo.network.exception.MooOutputException;
import de.superioz.moo.network.packets.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Consumer;

/**
 * Similar to {@link Queries} but with more specific methods for managing the data<br>
 * It also uses the {@link MooCache} to fetch e.g. cached groups
 * <br>
 * This class inherits every method you need for managing player/group data per packets
 *
 * @see MooCache
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MooQueries {

    private static MooQueries instance;

    public static synchronized MooQueries getInstance() {
        if(instance == null) {
            instance = new MooQueries();
        }
        return instance;
    }

    /**
     * Sends statusChange to the cloud
     *
     * @param data  The data
     * @param state The state
     * @param meta  The meta
     */
    public void changePlayerState(PlayerData data, PacketPlayerState.State state, String meta, Consumer<Response> callback) {
        PacketMessenger.message(new PacketPlayerState(data, state, meta), callback);
    }

    public void changePlayerState(PlayerData data, PacketPlayerState.State state, Consumer<Response> callback) {
        changePlayerState(data, state, "", callback);
    }

    public void changePlayerState(PlayerData data, PacketPlayerState.State state) {
        changePlayerState(data, state, "", response -> {
        });
    }

    /**
     * Gets playerData from given playerName
     *
     * @param playerName the name
     * @return The respond
     */
    public PlayerData getPlayerData(String playerName) {
        try {
            return Queries.get(DatabaseType.PLAYER, playerName, PlayerData.class);
        }
        catch(MooInputException e) {
            return null;
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData data = MooCache.getInstance().getPlayerMap().get(uuid);
        if(data != null) {
            return data;
        }

        try {
            return Queries.get(DatabaseType.PLAYER, uuid, PlayerData.class);
        }
        catch(MooInputException e) {
            return null;
        }
    }

    /**
     * Information about player
     *
     * @param key The key (name | uuid)
     * @return The respond
     */
    public PlayerProfile getPlayerProfile(String key) {
        try {
            Response response = PacketMessenger.transferToResponse(new PacketPlayerProfile(key));
            response.checkState();
            PlayerProfile info = PlayerProfile.fromPacketData(response.getMessageAsList());

            if(info != null) {
                if(info.getCurrentBan() == null) {
                    if(!checkBan(info.getCurrentBan())) info.setCurrentBan(null);
                }
            }
            return info;
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public PlayerProfile getPlayerProfile(UUID uuid) {
        return getPlayerProfile(uuid.toString());
    }

    /**
     * Get ping of the player
     *
     * @param playerName The playername
     * @return The respond
     */
    public Integer getPlayerPing(String playerName) {
        try {
            return PacketMessenger.transferToResponse(new PacketRequest(PacketRequest.Type.PING, playerName)).toPrimitive(Integer.class);
        }
        catch(MooInputException e) {
            return null;
        }
    }

    public Integer getPlayerPing(UUID uuid) {
        return getPlayerPing(uuid.toString());
    }

    /**
     * Modifies the playerData
     *
     * @param id       The id
     * @param modifier The modifier
     * @param type     The type of modification
     * @param param    The parameter
     * @return The packetRespond
     */
    public ResponseStatus modifyPlayerData(Object id, DbModifier modifier, DbQueryNode.Type type, Object param) {
        return modifyPlayerData(id, new DbQuery(PlayerData.class).add(modifier, type, param));
    }

    public ResponseStatus modifyPlayerData(Object id, DbModifier modifier, Object param) {
        return modifyPlayerData(id, new DbQuery(PlayerData.class).add(modifier, DbQueryNode.Type.EQUATE, param));
    }

    public ResponseStatus modifyPlayerData(Object id, DbQuery query) {
        return Queries.modify(DatabaseType.PLAYER, id, query).getStatus();
    }

    public ResponseStatus modifyPlayerData(Object id, DbQueryUnbaked query) {
        return Queries.modify(DatabaseType.PLAYER, id, query).getStatus();
    }

    /**
     * Sends a message
     *
     * @param type    The type of the message
     * @param message The message to send
     * @param param   Notes mean e.g. the playerName or the permission
     * @return The respond
     */
    public ResponseStatus sendMessage(PacketPlayerMessage.Type type, String message, String param, boolean colored, boolean formatted) {
        return PacketMessenger.transferToResponse(new PacketPlayerMessage(type, message, param, colored, formatted)).getStatus();
    }

    public ResponseStatus sendMessage(PacketPlayerMessage.Type type, String message, String param) {
        return sendMessage(type, message, param, true, true);
    }

    /**
     * Simply sets the targets group to given group
     *
     * @param target   The target
     * @param newGroup The newGroup
     * @return The response (CONFLICT if the group is the same)
     */
    public ResponseStatus rankPlayer(PlayerData target, Group newGroup) {
        if(target.getGroup().equalsIgnoreCase(newGroup.getName())) {
            return new Response(ResponseStatus.CONFLICT).getStatus();
        }
        return modifyPlayerData(target.getUuid(),
                DbQueryUnbaked.newInstance(DbModifier.PLAYER_GROUP, newGroup.getName())
                        .equate(DbModifier.PLAYER_RANK, newGroup.getRank()));
    }

    /**
     * Ranks the player but without specifying the new group
     *
     * @param target The target playerData
     * @param steps  The steps to take either up or down the group ladder
     * @param up     Up or down the ladder? (true = up; false = down)
     * @return The response (CONFLICT if the group is the same)
     */
    public ResponseStatus rankPlayer(PlayerData target, int steps, boolean up) {
        return rankPlayer(target, getGroup(target, steps, up, true));
    }

    /**
     * Gets the group permissions of the given player (additional: his extra perms)
     *
     * @param data      The playerData
     * @param withExtra Should the extra perms of the player be added too?
     * @return The list of permissions
     */
    public HashSet<String> getPlayerPermissions(PlayerData data, boolean withExtra) {
        try {
            HashSet<String> permissions = new HashSet<>(getGroup(data.getGroup()).getPermissions());
            if(withExtra) {
                permissions.addAll(data.getExtraPerms());
            }
            return permissions;
        }
        catch(NullPointerException ex) {
            return new HashSet<>();
        }
    }

    /*
    =========================
    PUNISHMENT SYSTEM
    =========================
     */

    /**
     * Checks if the ban is active
     *
     * @param ban The ban
     * @return The result (false = not banned ; true = banned)
     */
    public boolean checkBan(Ban ban) {
        if(ban == null) return false;
        if(ban.until() <= System.currentTimeMillis()) {
            archiveBan(ban);
            return false;
        }
        return true;
    }

    /**
     * Gets the ban of the player from playerName
     *
     * @param playerName The playerName
     * @return The respond of the request
     */
    public Ban getBan(String playerName) {
        try {
            Ban ban = Queries.get(DatabaseType.BAN, playerName);
            if(!checkBan(ban)) return null;
            return ban;
        }
        catch(MooInputException e) {
            return null;
        }
    }

    public Ban getBan(UUID uuid) {
        return getBan(uuid.toString());
    }

    /**
     * Gets the archived bans of the player from playerName
     *
     * @param playerName The playerName
     * @return The respond of the request
     */
    public List<Ban> getBanArchive(String playerName) {
        try {
            return Queries.newInstance(DatabaseType.BAN_ARCHIVE).filter(playerName).execute().toComplexes(Ban.class);
        }
        catch(MooInputException e) {
            return new ArrayList<>();
        }
    }

    public List<Ban> getBanArchive(UUID uuid) {
        return getBanArchive(uuid.toString());
    }

    /**
     * Archives the ban
     *
     * @param ban The ban
     */
    public void archiveBan(Ban ban) {
        Queries.delete(DatabaseType.BAN, ban.getBanned());
        Queries.create(DatabaseType.BAN, ban);
    }

    /**
     * Removes the ban and removes the ban points from the banned player
     *
     * @param ban The ban
     */
    public ResponseStatus unban(Ban ban) {
        try {
            ResponseStatus status = Queries.delete(DatabaseType.BAN, ban.getBanned()).getStatus();

            // removes the banpoints of the ban from the player
            if(ban.getBanPoints() != null && ban.getBanPoints() > 0) {
                this.modifyPlayerData(ban.getBanned(), DbQueryUnbaked.newInstance().subtract(DbModifier.PLAYER_BANPOINTS, ban.getBanPoints()));
            }
            return status;
        }
        catch(Exception e) {
            return ResponseStatus.INTERNAL_ERROR;
        }
    }

    /**
     * Bans the player
     *
     * @param executor           The executor (either null for console or uuid for player)
     * @param target             The target to be banned
     * @param tempMessagePattern The message pattern for a temp ban
     * @param permMessagePattern The message pattern for a perm ban
     * @return The respond
     */
    public ResponseStatus ban(UUID executor, String target, BanCategory banSubType, String reason,
                              String tempMessagePattern, String permMessagePattern) {
        return PacketMessenger.transferToResponse(
                new PacketPlayerBan(executor, target, banSubType, reason, tempMessagePattern, permMessagePattern)
        ).getStatus();
    }

    public ResponseStatus ban(UUID executor, String target, BanCategory banSubType, String reason, Long duration,
                              String tempMessagePattern, String permMessagePattern) {
        return PacketMessenger.transferToResponse(
                new PacketPlayerBan(executor, target, banSubType, reason, duration, tempMessagePattern, permMessagePattern)
        ).getStatus();
    }

    /*
    =========================
    PERMISSION SYSTEM
    =========================
     */

    /**
     * Updates permissions for given uniqueId. This method is used when group or playerdata
     * has been changed of one player, so that the permissions are up to date at every time.
     * It is recommended to not use this method very often (player join and on change data)
     * otherwise it could put a lot of stress onto the server (it's the same with every
     * database/cache actions anyway).
     *
     * @param uuid The uniqueId
     * @return The result
     */
    public boolean updatePermission(UUID uuid) {
        PlayerData data = MooCache.getInstance().getPlayerMap().get(uuid);
        if(data == null) return false;
        String groupName = data.getGroup();

        // list the group out of the cache
        // if the group doesnt exist create a "default" group
        Group group = MooCache.getInstance().getGroupMap().get(groupName);
        if(group == null) {
            group = new Group();
            group.setName(Group.DEFAULT_NAME);

            try {
                this.createGroup(new Group(groupName));
            }
            catch(MooOutputException e) {
                e.printStackTrace();
            }

            // creates the group
            // MooCache.getInstance().getGroupMap().fastPutAsync(groupName, group);

            // sets the player's group
            this.rankPlayer(data, group);
        }

        List<String> permissions = new ArrayList<>(data.getExtraPerms());
        permissions.addAll(PermissionUtil.getAllPermissions(group, MooCache.getInstance().getGroupMap().values()));
        MooCache.getInstance().getPlayerPermissionMap().putAsync(data.getUuid(), permissions);
        return true;
    }

    /**
     * Gets the color of the group with given name
     *
     * @param groupName The name of group
     * @return The color as string
     */
    public String getGroupColor(String groupName) {
        Group group = getGroup(groupName);
        if(group == null) return "&r";
        return group.getColor();
    }

    /**
     * Gets the group with given name
     *
     * @param groupName The groupName
     * @return The group
     */
    public Group getGroup(String groupName) {
        if(groupName == null) return null;

        if(MooCache.getInstance().getGroupMap().size() > 0) {
            return MooCache.getInstance().getGroupMap().get(groupName);
        }
        try {
            return Queries.get(DatabaseType.GROUP, groupName, Group.class);
        }
        catch(MooInputException e) {
            return null;
        }
    }

    public Group getGroup(UUID playerUniqueId) {
        PlayerData data = MooCache.getInstance().getPlayerMap().get(playerUniqueId);
        if(data == null) return null;

        return getGroup(data.getGroup());
    }

    /**
     * Gets the group of the player but shifted {@code steps} steps either up or down
     *
     * @param data           The playerData
     * @param steps          The steps to be shifted
     * @param up             Up or down the ladder? (true = up; false = down)
     * @param ignoreInfinite If the value '-1' should be ignored as infinite
     * @return The group
     */
    public Group getGroup(PlayerData data, int steps, boolean up, boolean ignoreInfinite) {
        if(steps < 0 && ignoreInfinite) steps *= -1;
        if(steps != -1) steps--;

        Group currentGroup = getGroup(data.getGroup());
        List<Group> groupList = CollectionUtil.filterList(MooQueries.getInstance().listGroups(),
                e -> up ? e.getRank() > currentGroup.getRank() : e.getRank() < currentGroup.getRank(),
                (o1, o2) -> o1.getRank().compareTo(o2.getRank()) * (up ? 1 : -1));

        // if the steps value is -1 (= infinite) then walk down/up the whole ladder
        if(!ignoreInfinite && steps == -1) {
            steps = groupList.size() - 1;
        }
        return CollectionUtil.getEntrySafely(groupList, steps, currentGroup);
    }

    public Group getGroup(PlayerData data, int steps, boolean up) {
        return getGroup(data, steps, up, false);
    }

    /**
     * Get all groups currently stored (or fetch it from the cloud)
     *
     * @return The list of groups
     */
    public List<Group> listGroups() {
        Collection<Group> groups = MooCache.getInstance().getGroupMap().values();
        if(groups != null && !groups.isEmpty()) {
            return new ArrayList<>(groups);
        }
        try {
            return Queries.list(DatabaseType.GROUP, Group.class);
        }
        catch(MooInputException e) {
            //
        }
        return new ArrayList<>();
    }

    /**
     * Creates given group<br>
     * Groupname is needed!
     *
     * @param group The group
     * @return The status of the creation
     */
    public ResponseStatus createGroup(Group group) {
        return Queries.create(DatabaseType.GROUP, group).getStatus();
    }

    public ResponseStatus deleteGroup(String groupName) {
        return Queries.delete(DatabaseType.GROUP, groupName).getStatus();
    }

    public ResponseStatus modifyGroup(String groupName, DbQuery query) {
        return Queries.modify(DatabaseType.GROUP, groupName, query).getStatus();
    }

    public ResponseStatus modifyGroup(String groupName, DbQueryUnbaked query) {
        return modifyGroup(groupName, query.bake(Group.class));
    }

}
