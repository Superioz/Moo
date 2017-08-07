package de.superioz.moo.api.events;

import de.superioz.moo.api.event.Event;
import de.superioz.moo.api.modules.RedisModule;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RedisConnectionEvent implements Event {

    /**
     * The redisModule who changed redis connection status
     */
    private RedisModule redisModule;

    /**
     * The status of the connection
     * Either true=connected or false=not connected
     */
    private boolean connectionActive;

}
