package de.superioz.moo.api.database.objects;

import de.superioz.moo.api.database.object.DbKey;
import de.superioz.moo.api.util.SimpleSerializable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ServerPattern extends SimpleSerializable {

    /**
     * The name of the pattern (e.g.: "lobby")
     */
    @DbKey
    public String name;

    /**
     * The type of the pattern (e.g.: LOBBY, GAME, whatever you want)
     */
    @DbKey
    public String type;

    /**
     * The priority to start the server (high priority -> lowest priority)
     */
    @DbKey
    public Integer priority;

    /**
     * The minimum amount of players to do something
     */
    @DbKey
    public Integer min;

    /**
     * The maximum amount of players to be able to stay on this server
     */
    @DbKey
    public Integer max;

    /**
     * The amount of ram ("512M", "1024M", ..)
     */
    @DbKey
    public String ram;

}
