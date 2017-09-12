package de.superioz.moo.network.common;

import de.superioz.moo.api.common.ObjectWrapper;
import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.common.punishment.BanReason;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.PermissionUtil;
import de.superioz.moo.network.packets.PacketPlayerBan;
import de.superioz.moo.network.packets.PacketPlayerKick;
import de.superioz.moo.network.queries.MooQueries;
import de.superioz.moo.network.queries.ResponseStatus;

import java.util.*;

/**
 * Wrapper class for {@link PlayerData}
 */
public class MooPlayer extends ObjectWrapper<MooPlayer, PlayerData> implements PermissionHolder {

    public MooPlayer(PlayerData wrappedObject) {
        super(wrappedObject == null ? PlayerData.NON_EXISTENT : wrappedObject);
    }

    @Override
    public void update() {
        this.wrappedObject = MooCache.getInstance().getPlayerMap().get(getUniqueId());
    }

    /**
     * Gets the uniqueid of the player
     *
     * @return The uuid
     */
    public UUID getUniqueId() {
        return wrappedObject.getUuid();
    }

    /**
     * Gets the name of the player
     *
     * @return The name
     */
    public String getName() {
        return wrappedObject.getLastName();
    }

    /**
     * Gets the ip of the player
     *
     * @return The ip
     */
    public String getIp() {
        return wrappedObject.getLastIp();
    }

    /**
     * Gets the group of the player
     *
     * @return The group
     */
    public MooGroup getGroup() {
        String groupName = wrappedObject.getGroup();
        return MooCache.getInstance().getGroupMap().get(groupName);
    }

    /**
     * Gets the rank of the player
     *
     * @return The rank
     */
    public int getRank() {
        return wrappedObject.getRank();
    }

    /**
     * Gets the current server of the player
     *
     * @return The server
     */
    public MooServer getCurrentServer() {
        return MooCache.getInstance().getServer(server -> server.getName().equals(wrappedObject.getCurrentServer()));
    }

    /**
     * Gets the id of the current proxy
     *
     * @return The proxy
     */
    public int getCurrentProxy() {
        return wrappedObject.getCurrentProxy();
    }

    /**
     * Gets the total online time
     *
     * @return The time
     */
    public Long getTotalOnline() {
        return getCurrentOnline() + wrappedObject.getTotalOnline();
    }

    /**
     * Gets the time the player joined
     *
     * @return The time (0 = not online)
     */
    public long getJoined() {
        return wrappedObject.getJoined();
    }

    /**
     * Gets the time he is currently online
     *
     * @return The time
     */
    public Long getCurrentOnline() {
        return System.currentTimeMillis() - getJoined();
    }

    /**
     * Gets the amount of coins the player got
     *
     * @return The coins
     */
    public Long getCoins() {
        return wrappedObject.getCoins();
    }

    /**
     * Gets the amount of banpoints the player got
     *
     * @return The ban points
     */
    public int getBanPoints() {
        return wrappedObject.getBanPoints();
    }

    /**
     * Gets the private permissions of this user
     *
     * @return The list of permissions
     */
    public List<String> getPrivatePermissions() {
        return wrappedObject.getExtraPerms();
    }

    /**
     * Gets the permissions of the user (group + private)
     *
     * @return The list/set of permissions
     */
    public HashSet<String> getAllPermissions() {
        HashSet<String> l = PermissionUtil.getAllPermissions(getGroup().unwrap(), MooProxy.getRawGroups());
        if(l == null) l.addAll(getPrivatePermissions());
        return l;
    }

    /*
    ===================
    SETTER
    ===================
     */

    /**
     * Sets the permissions of the player
     *
     * @param permissions The permissions
     * @return The status
     */
    public ResponseStatus setPermissions(HashSet<String> permissions) {
        if(!exists()) return ResponseStatus.NOK;
        List<String> l = new ArrayList<>();
        permissions.forEach(s -> {
            if(Validation.PERMISSION.matches(s)) l.add(s);
        });

        if(checkLaziness()) {
            return MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_EXTRA_PERMS, l);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the group of this player
     *
     * @param group The new group
     * @return The status
     */
    public ResponseStatus setGroup(Group group) {
        if(!exists()) return ResponseStatus.NOK;
        ResponseStatus status = setGroup(group.getName());
        wrappedObject.setRank(group.getRank());

        return status;
    }

    public ResponseStatus setGroup(MooGroup group) {
        return setGroup(group.unwrap());
    }

    private ResponseStatus setGroup(String groupName) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setGroup(groupName);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_GROUP, groupName);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the new server the player is currently online<br>
     * <b>USE AT OWN RISK</b>
     *
     * @param newServerName The new server name
     * @return The status
     */
    public ResponseStatus setCurrentServer(String newServerName) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setCurrentServer(newServerName);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_SERVER, newServerName);
        }
        return ResponseStatus.OK;
    }

    /**
     * Sets the coins of the player
     *
     * @param coins The coins
     * @return The status
     */
    public ResponseStatus setCoins(long coins) {
        if(!exists()) return ResponseStatus.NOK;
        wrappedObject.setCoins(coins);

        // MooQueries
        if(checkLaziness()) {
            return MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_COINS, coins);
        }
        return ResponseStatus.OK;
    }

    /*
    ===================
    OTHERS
    ===================
     */

    /**
     * Checks if the player exists
     *
     * @return The result
     */
    public boolean exists() {
        return wrappedObject != null && wrappedObject.getUuid() != null;
    }

    public boolean nexists() {
        return !exists();
    }

    /**
     * Checks if the player is online
     *
     * @return The result
     */
    public boolean isOnline() {
        return exists() && wrappedObject.getJoined() != 0;
    }

    /**
     * Checks if the player is banned atm
     *
     * @return The result
     */
    public boolean isBanned() {
        return getCurrentBan() != null;
    }

    /**
     * Gets the current ban of this player (null = not banned)
     *
     * @return The ban or null
     */
    public Ban getCurrentBan() {
        return MooQueries.getInstance().getBan(getUniqueId());
    }

    /**
     * Gets the bans of the player already archived
     *
     * @return The list of bans
     */
    public List<Ban> getBanArchive() {
        return MooQueries.getInstance().getBanArchive(getUniqueId());
    }

    /**
     * Gets the profile of this player
     *
     * @return The profile
     */
    public PlayerProfile getProfile() {
        return new PlayerProfile(unwrap(), getCurrentBan(), getBanArchive());
    }

    @Override
    public List<String> getPermissions() {
        return getPrivatePermissions();
    }

    /**
     * Adds given permissions to the player
     *
     * @param permissions The permissions
     * @return The status
     */
    @Override
    public ResponseStatus addPermission(List<String> permissions) {
        HashSet<String> currentPermissions = getAllPermissions();
        int size = currentPermissions.size();
        for(String s : permissions) {
            if(Validation.PERMISSION.matches(s)) {
                currentPermissions.add(s);
            }
        }
        if(currentPermissions.size() == size) return ResponseStatus.BAD_REQUEST;
        return setPermissions(currentPermissions);
    }

    public ResponseStatus addPermission(String... permissions) {
        return addPermission(Arrays.asList(permissions));
    }

    /**
     * Removes given permissions from the player
     *
     * @param permissions The permissions
     * @return The status
     */
    @Override
    public ResponseStatus removePermission(List<String> permissions) {
        HashSet<String> currentPermissions = getAllPermissions();
        int size = currentPermissions.size();
        for(String s : permissions) {
            if(Validation.PERMISSION.matches(s)) {
                currentPermissions.remove(s);
            }
        }
        if(currentPermissions.size() == size) return ResponseStatus.BAD_REQUEST;
        return setPermissions(currentPermissions);
    }

    public ResponseStatus removePermission(String... permissions) {
        return removePermission(Arrays.asList(permissions));
    }

    /**
     * Clears the permissions
     *
     * @return The status
     */
    @Override
    public ResponseStatus clearPermission() {
        if(unwrap().getExtraPerms().isEmpty()) return ResponseStatus.NOT_FOUND;
        return setPermissions(new HashSet<>());
    }

    /**
     * Kicks a player
     *
     * @param from    Who kicks the player?
     * @param message The message
     * @return The status
     */
    public ResponseStatus kick(UUID from, String message) {
        // check if the executor is allowed to do that
        if(from != null) {
            MooPlayer executor = MooProxy.getPlayer(from);

            if(executor.exists() && (executor.getGroup().getRank() < getGroup().getRank())) {
                return ResponseStatus.FORBIDDEN;
            }
        }

        return PacketMessenger.transferToResponse(new PacketPlayerKick(from, getUniqueId(), message)).getStatus();
    }

    /**
     * Bans the player
     *
     * @param from      The executor of the ban (null = console)
     * @param banReason The reason for the ban
     * @param duration  The duration of the ban
     * @return The status
     */
    public ResponseStatus ban(UUID from, BanReason banReason, long duration) {
        // check if the player is already banned
        if(isBanned()) {
            return ResponseStatus.CONFLICT;
        }

        // check if the executor is allowed to do that
        if(from != null) {
            MooPlayer executor = MooProxy.getPlayer(from);

            if(executor.exists() && (executor.getGroup().getRank() < getGroup().getRank())) {
                return ResponseStatus.FORBIDDEN;
            }
        }

        return PacketMessenger.transferToResponse(
                new PacketPlayerBan(from, getName(), banReason.getBanCategory(), banReason.getName(), duration,
                        LanguageManager.get("ban-message-temp"), LanguageManager.get("ban-message-perm"))
        ).getStatus();
    }

    public ResponseStatus ban(UUID from, BanReason banReason) {
        return ban(from, banReason, 0L);
    }
}
