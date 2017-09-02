package de.superioz.moo.network.common;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.network.redis.MooCache;
import lombok.Getter;

import java.util.UUID;

/**
 * Wrapper class for playerData
 */
public class MooPlayer {

    /**
     * Instead of extending we use this, so that we can't access all setters ..
     */
    @Getter
    private PlayerData wrappedData;

    private MooPlayer() {
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

    /*
    ===================
    SETTER
    ===================
     */

    /**
     * Sets the group of this player
     *
     * @param group The group
     */
    public void setGroup(Group group) {
        setGroup(group.getName());
        wrappedData.setRank(group.getRank());

        // MooQueries
        MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_RANK, group.getRank());
    }

    private void setGroup(String groupName) {
        wrappedData.setGroup(groupName);

        // MooQueries
        MooQueries.getInstance().modifyPlayerData(getUniqueId(), DbModifier.PLAYER_GROUP, groupName);
    }

}
