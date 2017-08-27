package de.superioz.moo.api.database.objects;

import de.superioz.moo.api.database.object.DbKey;
import de.superioz.moo.api.util.SimpleSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServerPattern extends SimpleSerializable {

    public static final String DEFAULT_TYPE = "game";
    public static final Integer DEFAULT_PRIORITY = 0;
    public static final Integer DEFAULT_MIN = 0;
    public static final Integer DEFAULT_MAX = 32;
    public static final String DEFAULT_RAM = "";

    /**
     * The name of the pattern (e.g.: "lobby")
     */
    @DbKey
    private String name;

    /**
     * The type of the pattern (e.g.: LOBBY, GAME, whatever you want)
     */
    @DbKey
    private String type;

    /**
     * The priority to start the server (high priority -> lowest priority)
     */
    @DbKey
    private Integer priority;

    /**
     * The minimum amount of players to do something
     */
    @DbKey
    private Integer min;

    /**
     * The maximum amount of players to be able to stay on this server
     */
    @DbKey
    private Integer max;

    /**
     * The amount of ram ("512M", "1024M", ..)
     */
    @DbKey
    private String ram;

}
