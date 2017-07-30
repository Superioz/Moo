package de.superioz.moo.api.database.object;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.DbKey;
import de.superioz.moo.api.util.SimpleSerializable;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PlayerData extends SimpleSerializable {

    /**
     * The uuid of the player
     */
    @DbKey
    public UUID uuid;

    /**
     * The name the player last played with
     */
    @DbKey
    public String lastName;

    /**
     * The ip the player last played with
     */
    @DbKey
    public String lastip;

    /**
     * The {@link Group} of the player
     */
    @DbKey
    public String group;

    /**
     * The {@link Group} rank of the player
     */
    @DbKey
    public int rank;

    /**
     * The server where the player is online atm
     */
    @DbKey
    @Setter
    public String currentServer;

    /**
     * The proxy where the player has connected to
     */
    @DbKey
    @Setter
    public int currentProxy;

    /**
     * Timestamp of the last online time (Last online = Leave Time)
     */
    @DbKey
    public Long lastOnline;

    /**
     * Timestamp of the first online time
     */
    @DbKey
    public Long firstOnline;

    /**
     * Total online time of the player (in ms)
     */
    @DbKey
    public Long totalOnline;

    /**
     * Timestamp when the player joined the server (0 = not online)
     */
    @DbKey
    public Long joined;

    /**
     * Extra permissions of the player. Additional to {@link Group#permissions}
     */
    @DbKey
    public List<String> extraPerms = new ArrayList<>();

    /**
     * Coins amount of the player
     */
    @DbKey
    public Long coins;

    /**
     * Ban points amount of the player
     */
    @DbKey
    public Integer banPoints;

    /**
     * Gets the time the player is currently online
     *
     * @return The amount as long
     */
    public Long getCurrentOnline() {
        return System.currentTimeMillis() - joined;
    }

    /**
     * Gets the total online time
     *
     * @return The amount as long
     */
    public Long getTotalOnline() {
        return getCurrentOnline() + totalOnline;
    }

}
