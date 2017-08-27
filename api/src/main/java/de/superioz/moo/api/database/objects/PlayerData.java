package de.superioz.moo.api.database.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.object.DbKey;
import de.superioz.moo.api.util.SimpleSerializable;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlayerData extends SimpleSerializable {

    /**
     * The uuid of the player
     */
    @DbKey
    private UUID uuid;

    /**
     * The name the player last played with
     */
    @DbKey
    private String lastName;

    /**
     * The ip the player last played with
     */
    @DbKey
    private String lastIp;

    /**
     * The {@link Group} of the player
     */
    @DbKey
    private String group;

    /**
     * The {@link Group} rank of the player
     */
    @DbKey
    private Integer rank;

    /**
     * The server where the player is online atm
     */
    @DbKey
    @Setter
    private String currentServer;

    /**
     * The proxy where the player has connected to
     */
    @DbKey
    @Setter
    private Integer currentProxy;

    /**
     * Timestamp of the last online time (Last online = Leave Time)
     */
    @DbKey
    private Long lastOnline;

    /**
     * Timestamp of the first online time
     */
    @DbKey
    private Long firstOnline;

    /**
     * Total online time of the player (in ms)
     */
    @DbKey
    private Long totalOnline;

    /**
     * Timestamp when the player joined the server (0 = not online)
     */
    @DbKey
    private Long joined;

    /**
     * Extra permissions of the player. Additional to {@link Group#permissions}
     */
    @DbKey
    private List<String> extraPerms = new ArrayList<>();

    /**
     * Coins amount of the player
     */
    @DbKey
    private Long coins;

    /**
     * Ban points amount of the player
     */
    @DbKey
    private Integer banPoints;

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
