package de.superioz.moo.api.database.objects;

import de.superioz.moo.api.common.ServerType;
import de.superioz.moo.api.database.object.DbKey;
import de.superioz.moo.api.util.SimpleSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pattern of a server. I got some ideas of handling servers (priority, min, slots) from @Doppelnull.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServerPattern extends SimpleSerializable {

    public static final String DEFAULT_TYPE = "game";
    public static final Integer DEFAULT_PRIORITY = 0;
    public static final Integer DEFAULT_MIN = 0;
    public static final Integer DEFAULT_MAX = 12;
    public static final Integer DEFAULT_SLOTS = 32;
    public static final String DEFAULT_RAM = "";

    /**
     * The name of the pattern (e.g.: "lobby")
     */
    @DbKey
    private String name;

    /**
     * The type of the pattern (e.g.: LOBBY, GAME, whatever you want)
     * difference between LOBBY & GAME: Lobbies have to be handled differently
     */
    @DbKey
    private String type;

    /**
     * The priority to start the server (high priority -> lowest priority)
     * Simply idea, but nonetheless thanks to
     */
    @DbKey
    private Integer priority;

    /**
     * The maximum amount of servers to be active simultaneosly
     */
    @DbKey
    private Integer max;

    /**
     * The minimum amount of servers to be active simultaneosly
     */
    @DbKey
    private Integer min;

    /**
     * The player slots (These are fake tho, just for the waiting lobby)
     */
    @DbKey
    private Integer slots;

    /**
     * The amount of ram ("512M", "1024M", ..)
     */
    @DbKey
    private String ram;

    /**
     * Gets the server type type
     *
     * @return The server type object
     */
    public ServerType getServerType() {
        return ServerType.fromName(getType());
    }

}
