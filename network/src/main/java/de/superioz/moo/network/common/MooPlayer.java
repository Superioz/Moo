package de.superioz.moo.network.common;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.common.punishment.BanReason;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.redis.MooCache;
import de.superioz.moo.api.utils.PermissionUtil;
import de.superioz.moo.network.packets.PacketPlayerBan;
import de.superioz.moo.network.packets.PacketPlayerKick;
import de.superioz.moo.network.server.MooProxy;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Wrapper class for playerData
 */
public class MooPlayer {

    /**
     * Instead of extending we use this, so that we can't access all setters ..
     */
    private PlayerData wrappedData;

    /**
     * The current lazy mode, if >0 it will not update the data
     * automatically in the database after it went down to 0 again.
     */
    private int lazy = 0;

    public MooPlayer(PlayerData data) {
        if(data == null) {
            data = PlayerData.NON_EXISTENT;
        }
        this.wrappedData = data;
    }

    /**
     * Sets the lazy state to true
     *
     * @return This
     */
    public synchronized MooPlayer lazyLock(int level) {
        if(level < 1) level = 1;
        lazy = level;
        return this;
    }

    public synchronized MooPlayer lazyLock() {
        return lazyLock(1);
    }

    /**
     * Returns the lazy state and changes it as well if it's true
     *
     * @return The lazy state
     */
    private synchronized boolean checkLaziness() {
        if(lazy > 0) {
            lazy--;
        }
        return lazy > 0;
    }

    /**
     * Unwraps this class, meaning only returning the wrapped data
     *
     * @return The wrapped data
     */
    public PlayerData unwrap() {
        return wrappedData;
    }

    /**
     * Gets the uniqueid of the player
     *
     * @return The uuid
     */
    public UUID getUniqueId() {
        return wrappedData.getUuid();
    }

    /**
     * Gets the name of the player
     *
     * @return The name
     */
    public String getName() {
        return wrappedData.getLastName();
    }

    /**
     * Gets the ip of the player
     *
     * @return The ip
     */
    public String getIp() {
        return wrappedData.getLastIp();
    }

    /**
     * Gets the group of the player
     *
     * @return The group
     */
    public Group getGroup() {
        String groupName = wrappedData.getGroup();
        return MooCache.getInstance().getGroupMap().get(groupName);
    }

    /**
     * Gets the rank of the player
     *
     * @return The rank
     */
    public int getRank() {
        return wrappedData.getRank();
    }

    /**
     * Gets the current server of the player
     *
     * @return The server
     */
    public MooServer getCurrentServer() {
        return MooCache.getInstance().getServer(server -> server.getName().equals(wrappedData.getCurrentServer()));
    }

    /**
     * Gets the id of the current proxy
     *
     * @return The proxy
     */
    public int getCurrentProxy() {
        return wrappedData.getCurrentProxy();
    }

    /**
     * Gets the total online time
     *
     * @return The time
     */
    public Long getTotalOnline() {
        return getCurrentOnline() + wrappedData.getTotalOnline();
    }

    /**
     * Gets the time the player joined
     *
     * @return The time (0 = not online)
     */
    public long getJoined() {
        return wrappedData.getJoined();
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
        return wrappedData.getCoins();
    }

    /**
     * Gets the amount of banpoints the player got
     *
     * @return The ban points
     */
    public int getBanPoints() {
        return wrappedData.getBanPoints();
    }

    /**
     * Gets the private permissions of this user
     *
     * @return The list of permissions
     */
    public List<String> getPrivatePermissions() {
        return wrappedData.getExtraPerms();
    }

    /**
     * Gets the permissions of the user (group + private)
     *
     * @return The list/set of permissions
     */
    public Set<String> getPermissions() {
        Set<String> l = PermissionUtil.getAllPermissions(getGroup(), MooCache.getInstance().getGroupMap().values());
        if(l == null) l.addAll(getPrivatePermissions());
        return l;
    }

    /*
    ===================
    SETTER
    ===================
     */

    /**
     * Sets the group of this player
     *
     * @param group The new group
     */
    public MooPlayer setGroup(Group group) {
        setGroup(group.getName());
        wrappedData.setRank(group.getRank());

        return this;
    }

    private void setGroup(String groupName) {
        wrappedData.setGroup(groupName);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_GROUP, groupName);
        }
    }

    /**
     * Sets the new server the player is currently online<br>
     * <b>USE AT OWN RISK</b>
     *
     * @param newServerName The new server name
     */
    public MooPlayer setCurrentServer(String newServerName) {
        wrappedData.setCurrentServer(newServerName);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_SERVER, newServerName);
        }
        return this;
    }

    /**
     * Sets the coins of the player
     *
     * @param coins The coins
     */
    public MooPlayer setCoins(long coins) {
        wrappedData.setCoins(coins);

        // MooQueries
        if(checkLaziness()) {
            MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_COINS, coins);
        }
        return this;
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
        return wrappedData.getUuid() != null;
    }

    /**
     * Checks if the player is online
     *
     * @return The result
     */
    public boolean isOnline() {
        return exists() && wrappedData.getJoined() != 0;
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
            MooPlayer executor = MooProxy.getInstance().getPlayer(from);

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
            MooPlayer executor = MooProxy.getInstance().getPlayer(from);

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
