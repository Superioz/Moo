package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.network.redis.RedisConnectionEvent;

/**
 * This class listens on the cloud being connected to redis
 */
public class RedisConnectionListener implements EventListener {

    @EventHandler
    public void onRedisConnection(RedisConnectionEvent event) {
        if(!event.isConnectionActive()) {
            // we can ignore this, it should only happen if the cloud disables, and then the
            // network is rip either way
            return;
        }

        // erm, dunno?
    }

}
